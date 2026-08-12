package controlador.ws;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;   // OJO: NO javax.ws.rs.PathParam (JAX-RS).
import javax.websocket.server.ServerEndpoint;

/**
 * Puente de escaneo celular -> pantalla de la PC.
 *
 * <p><b>Idea general.</b> El operador tiene una pantalla abierta en la PC y quiere que
 * los codigos que escanea con la camara del celular aparezcan ahi. Como el servidor
 * necesita <i>empujar</i> el dato hacia una pantalla ya abierta, REST no alcanza:
 * hace falta una conexion persistente. Eso es este WebSocket.
 *
 * <p><b>Emparejamiento por token.</b> Ambos extremos se conectan a la misma URL usando
 * el mismo token. El token es lo unico que los vincula:
 *
 * <pre>
 *   PC      -> ws://servidor:8080/ctx/ws/scan/{token}/pc
 *   Celular -> ws://servidor:8080/ctx/ws/scan/{token}/movil
 * </pre>
 *
 * La PC muestra un QR con la URL de la pagina del celular (token incluido); el celular
 * lo escanea UNA vez y a partir de ahi todo lo que lea viaja a esa pantalla.
 *
 * <p><b>El servidor no interpreta el codigo.</b> El celular manda un JSON ya armado y
 * este endpoint lo reenvia tal cual a la PC. Asi no hace falta ninguna libreria JSON
 * del lado del servidor: si maniana queres agregar campos al mensaje, tocas solo el JS.
 * La unica excepcion es reconocer el latido, que se descarta en vez de reenviarse.
 *
 * <p><b>Latido.</b> Ambos navegadores mandan {@code {"tipo":"latido"}} cada pocos
 * segundos. Sirve para no confiar en {@code isOpen()}, que sigue dando true cuando la
 * conexion murio sucia; ver {@link #pantallaViva(String)}.
 *
 * <p><b>Integracion futura.</b> En esta pantalla de prueba el token se genera al vuelo.
 * Cuando esto se monte sobre un modulo real (ej. Factura de Compra), el token a usar es
 * el mismo token de 8 chars del patron Session+Token, y asi el escaneo cae exactamente
 * en el documento que el operador tiene en edicion.
 *
 * <p><b>Nota de seguridad.</b> Hoy alcanza con conocer el token para inyectar escaneos.
 * En LAN aislada es aceptable para probar; el endurecimiento (validar la HttpSession en
 * el handshake) esta documentado en SCANNER_MOVIL.md.
 *
 * @see <a href="file:../../../../../SCANNER_MOVIL.md">SCANNER_MOVIL.md</a>
 */
@ServerEndpoint("/ws/scan/{token}/{rol}")
public class ScanEndpoint {

    /** Rol de quien RECIBE los codigos (la pantalla de la PC). */
    private static final String ROL_PC = "pc";

    /** Claves con las que guardamos datos en la Session del WebSocket. */
    private static final String PROP_TOKEN = "token";
    private static final String PROP_ROL = "rol";
    private static final String PROP_LATIDO = "latido";

    /**
     * Cada cuanto manda latido el navegador. Es informativo: el valor real esta en el JS
     * (constante MS_LATIDO de scannerTest.jsp y scannerMovil.jsp) y los tres tienen que
     * moverse juntos.
     */
    private static final long MS_LATIDO = 8_000;

    /**
     * Sin latido por mas de esto, damos la pantalla por muerta aunque isOpen() diga lo
     * contrario.
     *
     * <p><b>Por que 75 s y no 25.</b> Chrome estrangula los timers de las pestanias en
     * segundo plano: despues de 5 minutos oculta, un setInterval pasa a correr una vez
     * por minuto. Con una tolerancia corta, una pantalla minimizada pero perfectamente
     * viva quedaria marcada como muerta y estaria reconectandose todo el tiempo. El
     * margen tiene que quedar por encima de ese minuto.
     */
    private static final long MS_LATIDO_VENCIDO = 75_000;

    /**
     * Corte del contenedor para sesiones sin nada de trafico. Es la red de seguridad que
     * libera las sesiones abandonadas; el control fino lo hace {@link #MS_LATIDO_VENCIDO}.
     * Tiene que ser mayor que ese, o cortaria antes pestanias estranguladas pero vivas.
     */
    private static final long MS_TIMEOUT_INACTIVIDAD = 90_000;

    /**
     * token -> sesion de la pantalla de la PC que espera codigos.
     * Un solo receptor por token: si la PC recarga la pagina, la conexion nueva
     * reemplaza a la vieja.
     */
    private static final Map<String, Session> PANTALLAS = new ConcurrentHashMap<>();

    /**
     * token -> celulares emparejados. Puede haber mas de uno escaneando contra la
     * misma pantalla (dos operadores cargando la misma factura, por ejemplo).
     * Solo lo usamos para mostrar el estado "N celular(es) conectado(s)" en la PC.
     */
    private static final Map<String, Set<Session>> MOVILES = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------- ciclo de vida

    @OnOpen
    public void onOpen(Session sesion,
                       @PathParam("token") String token,
                       @PathParam("rol") String rol) {

        // Guardamos token y rol en la propia sesion: los callbacks @OnMessage/@OnClose
        // reciben la Session pero NO los @PathParam, asi que hay que dejarlos anotados aca.
        sesion.getUserProperties().put(PROP_TOKEN, token);
        sesion.getUserProperties().put(PROP_ROL, rol);
        sesion.getUserProperties().put(PROP_LATIDO, System.currentTimeMillis());

        // Sin esto una sesion que murio sucia (PC sin red, celular suspendido) queda
        // ocupando lugar en los mapas hasta que se caiga el TCP, que puede tardar minutos.
        sesion.setMaxIdleTimeout(MS_TIMEOUT_INACTIVIDAD);

        if (ROL_PC.equals(rol)) {
            PANTALLAS.put(token, sesion);
            // Puede que el celular se haya conectado primero: avisamos el estado real.
            avisarEstadoALaPantalla(token);
        } else {
            // computeIfAbsent + newSetFromMap: el Set tambien tiene que ser concurrente,
            // porque varios celulares pueden conectarse al mismo tiempo.
            MOVILES.computeIfAbsent(token,
                    k -> Collections.newSetFromMap(new ConcurrentHashMap<Session, Boolean>()))
                   .add(sesion);

            // Le decimos al celular si ya hay una pantalla esperandolo, para que
            // muestre "emparejado" o "esperando a la PC" sin tener que escanear a ciegas.
            boolean hayPantalla = pantallaViva(token) != null;
            enviar(sesion, "{\"tipo\":\"estado\",\"emparejado\":" + hayPantalla + "}");
            avisarEstadoALaPantalla(token);
        }
    }

    /**
     * Un codigo escaneado. Solo puede venir del celular; lo reenviamos textual a la PC.
     *
     * <p>El mensaje que manda el celular ya viene armado como
     * {@code {"tipo":"codigo","valor":"7791234567890","formato":"ean_13"}}.
     */
    @OnMessage
    public void onMessage(String mensaje, Session sesion) {
        // Cualquier mensaje entrante vale como senial de vida, incluido el latido.
        sesion.getUserProperties().put(PROP_LATIDO, System.currentTimeMillis());

        String token = (String) sesion.getUserProperties().get(PROP_TOKEN);
        String rol = (String) sesion.getUserProperties().get(PROP_ROL);

        // El latido no es un dato: no se reenvia ni se confirma. Ya cumplio su funcion
        // arriba, con la marca de tiempo.
        if (esLatido(mensaje)) {
            return;
        }

        // La pantalla no deberia mandar nada mas que latidos; si lo hace, lo ignoramos.
        if (ROL_PC.equals(rol)) {
            return;
        }

        Session pantalla = pantallaViva(token);
        if (pantalla == null) {
            // Sin esta respuesta el operador escanearia 20 items contra una pantalla
            // cerrada sin enterarse. El celular usa el ack para avisar en rojo.
            enviar(sesion, "{\"tipo\":\"ack\",\"ok\":false,\"msg\":\"La pantalla de la PC no esta conectada\"}");
            return;
        }

        enviar(pantalla, mensaje);
        enviar(sesion, "{\"tipo\":\"ack\",\"ok\":true}");
    }

    @OnClose
    public void onClose(Session sesion) {
        String token = (String) sesion.getUserProperties().get(PROP_TOKEN);
        String rol = (String) sesion.getUserProperties().get(PROP_ROL);
        if (token == null) {
            return;
        }

        if (ROL_PC.equals(rol)) {
            // remove(clave, valor) y no remove(clave): si la PC recargo, la sesion nueva
            // ya ocupo el lugar en el mapa y el @OnClose tardio de la vieja no debe borrarla.
            PANTALLAS.remove(token, sesion);
        } else {
            Set<Session> celulares = MOVILES.get(token);
            if (celulares != null) {
                celulares.remove(sesion);
                if (celulares.isEmpty()) {
                    MOVILES.remove(token, celulares);   // evita que el mapa crezca sin limite
                }
            }
            avisarEstadoALaPantalla(token);
        }
    }

    @OnError
    public void onError(Session sesion, Throwable error) {
        // Caso tipico y esperable: el celular se bloquea o cambia de Wi-Fi y la conexion
        // muere de forma abrupta. No es un error de la aplicacion, solo lo dejamos en el log.
        System.out.println("[ScanEndpoint] conexion caida: " + error.getMessage());
    }

    // ---------------------------------------------------------------------- auxiliares

    /**
     * La pantalla de ese token, o null si no hay ninguna o si dejo de dar seniales de vida.
     *
     * <p><b>Por que no alcanza isOpen().</b> Si la PC se queda sin red, se suspende o se
     * cuelga sin cerrar el TCP, la sesion sigue figurando abierta durante minutos: el
     * celular recibiria {@code ack ok:true} mientras el codigo se pierde, que es
     * exactamente lo que el ack existe para evitar. Por eso ademas exigimos un latido
     * reciente, que el navegador manda cada {@link #MS_LATIDO} ms.
     */
    private Session pantallaViva(String token) {
        Session pantalla = PANTALLAS.get(token);
        if (!estaViva(pantalla)) {
            return null;
        }
        Long ultimoLatido = (Long) pantalla.getUserProperties().get(PROP_LATIDO);
        if (ultimoLatido == null
                || (System.currentTimeMillis() - ultimoLatido) > MS_LATIDO_VENCIDO) {
            descartarPantalla(token, pantalla);
            return null;
        }
        return pantalla;
    }

    /**
     * Da de baja una pantalla que dejo de latir: la saca del mapa y cierra la sesion zombi.
     * Cerrarla ademas de sacarla hace que el navegador, si en realidad seguia vivo del otro
     * lado, reciba el onclose y se reconecte solo.
     */
    private void descartarPantalla(String token, Session pantalla) {
        PANTALLAS.remove(token, pantalla);
        try {
            pantalla.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "sin latido"));
        } catch (IOException e) {
            System.out.println("[ScanEndpoint] no se pudo cerrar la pantalla sin latido: " + e.getMessage());
        }
    }

    /**
     * Unico caso en que el servidor mira el contenido de un mensaje: distinguir el latido
     * del dato. El codigo escaneado sigue viajando sin que nadie lo interprete aca.
     */
    private boolean esLatido(String mensaje) {
        return mensaje != null && mensaje.contains("\"tipo\":\"latido\"");
    }

    /** Le informa a la pantalla cuantos celulares tiene emparejados en este momento. */
    private void avisarEstadoALaPantalla(String token) {
        Session pantalla = pantallaViva(token);
        if (pantalla == null) {
            return;
        }
        Set<Session> celulares = MOVILES.get(token);
        int cantidad = (celulares == null) ? 0 : celulares.size();
        enviar(pantalla, "{\"tipo\":\"estado\",\"moviles\":" + cantidad + "}");
    }

    private boolean estaViva(Session sesion) {
        return sesion != null && sesion.isOpen();
    }

    /**
     * Envio asincrono y a prueba de fallos.
     *
     * <p>Usamos getAsyncRemote() para no bloquear el hilo del contenedor esperando a un
     * cliente lento. El try/catch cubre la carrera inevitable entre isOpen() y el envio:
     * la conexion puede caerse justo en el medio.
     */
    private void enviar(Session sesion, String mensaje) {
        if (!estaViva(sesion)) {
            return;
        }
        try {
            sesion.getAsyncRemote().sendText(mensaje);
        } catch (IllegalStateException e) {
            System.out.println("[ScanEndpoint] no se pudo enviar, sesion cerrandose: " + e.getMessage());
        }
    }
}
