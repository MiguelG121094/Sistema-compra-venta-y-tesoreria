<%--
    scannerTest.jsp - Pantalla de PRUEBA que recibe los codigos escaneados desde el celular.

    Es intencionalmente autonoma: no incluye base.jsp ni el menu lateral, para poder validar
    el circuito completo sin tocar ningun modulo en uso. Cuando el flujo este aprobado, lo
    unico que se traslada a un modulo real (ej. facturaCompra.jsp) es el bloque de WebSocket
    marcado mas abajo, reemplazando el token generado aca por el token Session+Token del
    documento en edicion.

    Esta pagina SI pasa por AuthFilter (requiere login). La que se abre en el celular
    (scannerMovil.jsp) esta exceptuada, porque el operador no va a loguearse en el telefono.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.UUID"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.LinkedHashSet"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.net.DatagramSocket"%>
<%@page import="java.net.InetAddress"%>
<%@page import="java.net.Inet4Address"%>
<%@page import="java.net.NetworkInterface"%>
<%!
    /**
     * La IP del adaptador por el que este equipo sale a la red.
     *
     * NO usamos InetAddress.getLocalHost(): devuelve lo que el sistema resuelva para el
     * nombre del equipo, sin ningun criterio de que adaptador sirve. En una PC con VMware,
     * VirtualBox o WSL suele contestar la IP de una red virtual (192.168.154.1 y parecidas),
     * que existe solo dentro de la maquina y a la que el celular no llega nunca.
     *
     * El truco: connect() sobre UDP no manda ni un byte, solo consulta la tabla de ruteo
     * para decidir por que interfaz saldria. Devuelve entonces la IP del adaptador que
     * tiene puerta de enlace, que es exactamente el criterio que sirve. No hace falta que
     * haya internet ni que 8.8.8.8 conteste: alcanza con que exista una ruta por defecto.
     */
    private String ipDeSalida() {
        DatagramSocket sonda = null;
        try {
            sonda = new DatagramSocket();
            sonda.connect(InetAddress.getByName("8.8.8.8"), 53);
            InetAddress local = sonda.getLocalAddress();
            if (local instanceof Inet4Address
                    && !local.isAnyLocalAddress() && !local.isLoopbackAddress()) {
                return local.getHostAddress();
            }
        } catch (Exception e) {
            // Sin ruta por defecto, o el socket no se pudo abrir: queda la lista de abajo.
        } finally {
            if (sonda != null) {
                sonda.close();
            }
        }
        return null;
    }

    /**
     * Todas las IPv4 de red local del equipo, para poder elegir a mano cuando lo de arriba
     * no acierta. Van las virtuales tambien: no hay forma confiable de reconocerlas por el
     * nombre, y mostrarlas ordenadas con la buena primero es mas util que ocultarlas.
     */
    private List<String> ipsLocales() {
        List<String> ips = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> adaptadores = NetworkInterface.getNetworkInterfaces();
            while (adaptadores.hasMoreElements()) {
                NetworkInterface adaptador = adaptadores.nextElement();
                if (!adaptador.isUp() || adaptador.isLoopback()) {
                    continue;   // descartados y sin uso no interesan
                }
                Enumeration<InetAddress> direcciones = adaptador.getInetAddresses();
                while (direcciones.hasMoreElements()) {
                    InetAddress dir = direcciones.nextElement();
                    if (dir instanceof Inet4Address && dir.isSiteLocalAddress()) {
                        ips.add(dir.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            // Sin permisos para enumerar: el campo queda editable a mano igual.
        }
        return ips;
    }
%>
<%
    /* Token de emparejamiento: es lo unico que vincula esta pantalla con el celular.
       8 caracteres, igual que el token del patron Session+Token del resto del sistema. */
    String token = UUID.randomUUID().toString().substring(0, 8);

    /* Mejor intento de adivinar la direccion por la que el CELULAR puede llegar al servidor.
       Ojo: si abris esta pantalla como "localhost", ese nombre no le sirve al telefono
       (para el, localhost es el propio telefono). Por eso el valor es editable en pantalla. */
    String hostSugerido = request.getServerName();
    if ("localhost".equals(hostSugerido) || "127.0.0.1".equals(hostSugerido)) {
        hostSugerido = ipDeSalida();
    }

    String esquema = request.getScheme() + "://";
    String puerto = ":" + request.getServerPort();

    String origenSugerido = (hostSugerido == null || hostSugerido.isEmpty())
            ? ""
            : esquema + hostSugerido + puerto;

    /* Alternativas para el desplegable, con la sugerida primero. LinkedHashSet: mantiene
       ese orden y de paso saca la repetida cuando la sugerida ya venia en la enumeracion. */
    Set<String> origenesPosibles = new LinkedHashSet<String>();
    if (!origenSugerido.isEmpty()) {
        origenesPosibles.add(origenSugerido);
    }
    for (String ip : ipsLocales()) {
        origenesPosibles.add(esquema + ip + puerto);
    }
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Prueba de escaneo desde el celular</title>
        <link href="Bootstrap 5.3.3/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
        <style>
            /* El codigo recien llegado parpadea en verde: el operador mira la pantalla,
               no el telefono, asi que necesita una confirmacion visual clara. */
            @keyframes destello { from { background-color: #d1e7dd; } to { background-color: transparent; } }
            .fila-nueva { animation: destello 1.2s ease-out; }
            #qr { display: inline-block; padding: 12px; background: #fff; border: 1px solid #dee2e6; border-radius: .5rem; }
            #qr img, #qr canvas { display: block; }
            .valor-codigo { font-family: monospace; font-size: 1.1rem; font-weight: 600; }
        </style>
    </head>
    <body class="bg-light">
        <div class="container py-4">

            <h3 class="mb-1">Prueba de escaneo desde el celular</h3>
            <p class="text-muted">
                Escanea el QR con la camara del celular, permiti el acceso a la camara y
                empeza a leer codigos: van a aparecer en la tabla de abajo.
            </p>

            <div class="row g-4">

                <%-- ------------------------------------------------ columna izquierda: emparejamiento --%>
                <div class="col-lg-5">
                    <div class="card shadow-sm">
                        <div class="card-header fw-semibold">1. Emparejar el celular</div>
                        <div class="card-body">

                            <div class="mb-3">
                                <label for="origen" class="form-label">
                                    Direccion del servidor <span class="text-muted">(la que ve el celular)</span>
                                </label>
                                <input type="text" class="form-control" id="origen"
                                       list="origenesPosibles" autocomplete="off"
                                       value="<%= origenSugerido %>"
                                       placeholder="http://192.168.1.50:8080">
                                <%-- Si el equipo tiene VMware, VirtualBox o WSL, aca aparecen
                                     tambien sus redes virtuales, que NO sirven. La primera de
                                     la lista es la del adaptador con salida a la red. --%>
                                <datalist id="origenesPosibles">
                                    <% for (String posible : origenesPosibles) { %>
                                    <option value="<%= posible %>"></option>
                                    <% } %>
                                </datalist>
                                <div class="form-text">
                                    No puede ser <code>localhost</code>: para el celular, eso apunta a si mismo.
                                    Usa la IP del equipo en la red. Se recuerda para la proxima vez.
                                    <% if (origenesPosibles.size() > 1) { %>
                                    Si la sugerida no funciona, desplega el campo: hay
                                    <%= origenesPosibles.size() %> direcciones en este equipo.
                                    <% } %>
                                </div>
                            </div>

                            <div class="text-center mb-3">
                                <div id="qr"></div>
                            </div>

                            <div class="mb-2">
                                <span class="text-muted small">O abri este link a mano en el celular:</span>
                                <div class="input-group input-group-sm mt-1">
                                    <input type="text" class="form-control font-monospace" id="urlMovil" readonly>
                                    <button class="btn btn-outline-secondary" type="button" id="btnCopiar">Copiar</button>
                                </div>
                            </div>

                            <hr>
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="text-muted small">Token de esta pantalla</span>
                                <code id="tokenActual"><%= token %></code>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-2">
                                <span class="text-muted small">Conexion</span>
                                <span id="estadoWs" class="badge text-bg-secondary">conectando...</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-2">
                                <span class="text-muted small">Celulares emparejados</span>
                                <span id="estadoMoviles" class="badge text-bg-secondary">0</span>
                            </div>

                            <%-- Corta los celulares emparejados y genera un token nuevo. Util
                                 cuando el operador se va con el telefono o hay que pasarle el
                                 escaneo a otro equipo. --%>
                            <button class="btn btn-outline-danger btn-sm w-100 mt-3" type="button"
                                    id="btnDesvincular">
                                Desvincular celulares
                            </button>
                            <div class="form-text">
                                Corta los celulares conectados y genera un c&oacute;digo nuevo.
                                Para volver a usarlos hay que escanear el QR otra vez.
                            </div>
                        </div>
                    </div>
                </div>

                <%-- ------------------------------------------------ columna derecha: codigos recibidos --%>
                <div class="col-lg-7">
                    <div class="card shadow-sm">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span class="fw-semibold">2. Codigos recibidos</span>
                            <div>
                                <span class="badge text-bg-primary me-2" id="contador">0</span>
                                <button class="btn btn-sm btn-outline-secondary" type="button" id="btnLimpiar">Limpiar</button>
                            </div>
                        </div>
                        <div class="card-body p-0">
                            <table class="table table-sm mb-0 align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th style="width:45%">Codigo</th>
                                        <th style="width:30%">Formato</th>
                                        <th style="width:25%">Hora</th>
                                    </tr>
                                </thead>
                                <tbody id="tablaCodigos">
                                    <tr id="filaVacia">
                                        <td colspan="3" class="text-center text-muted py-4">
                                            Esperando el primer escaneo...
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="alert alert-info mt-3 small mb-0">
                        <strong>La columna "Formato" es el objetivo de esta prueba.</strong>
                        Escanea varios productos de tu catalogo y anota que formatos aparecen
                        (normalmente <code>ean_13</code>). Con eso despues podes dejar activados
                        solo esos y el lector se vuelve mas rapido y preciso.
                    </div>
                </div>
            </div>
        </div>

        <script src="scanner/qrcode.min.js"></script>
        <script>
            <%--/* ==========================================================================
               NOTA JSP: en un .jsp NO se pueden usar template literals de JavaScript,
               porque la secuencia ${...} la intercepta el motor de EL del servidor antes
               de que el navegador vea el archivo. Por eso todo se concatena con "+".
               ========================================================================== */--%>

            var TOKEN = "<%= token %>";
            var CONTEXT_PATH = "${pageContext.request.contextPath}";

            var inputOrigen = document.getElementById("origen");
            var inputUrlMovil = document.getElementById("urlMovil");
            var estadoWs = document.getElementById("estadoWs");
            var estadoMoviles = document.getElementById("estadoMoviles");
            var tokenActual = document.getElementById("tokenActual");
            var tabla = document.getElementById("tablaCodigos");
            var contador = document.getElementById("contador");
            var totalRecibidos = 0;
            var movilesConectados = 0;
            var qr = null;

            // ----------------------------------------------------------- QR de emparejamiento

            /* Recordamos la direccion tipeada: la IP del servidor no cambia todos los dias
               y es tedioso reescribirla en cada prueba. */
            var origenGuardado = localStorage.getItem("scanner_origen");
            if (origenGuardado) {
                inputOrigen.value = origenGuardado;
            }

            function urlDelMovil() {
                var origen = inputOrigen.value.trim().replace(/\/+$/, "");   // sin barra final
                if (!origen) {
                    return "";
                }
                return origen + CONTEXT_PATH + "/scannerMovil.jsp?token=" + TOKEN;
            }

            function refrescarQr() {
                var url = urlDelMovil();
                inputUrlMovil.value = url;

                var contenedor = document.getElementById("qr");
                contenedor.innerHTML = "";
                if (!url) {
                    contenedor.innerHTML = '<span class="text-muted small">Complet&aacute; la direcci&oacute;n del servidor</span>';
                    return;
                }
                qr = new QRCode(contenedor, {
                    text: url,
                    width: 220,
                    height: 220,
                    correctLevel: QRCode.CorrectLevel.M
                });
            }

            inputOrigen.addEventListener("input", function () {
                localStorage.setItem("scanner_origen", inputOrigen.value.trim());
                refrescarQr();
            });

            document.getElementById("btnCopiar").addEventListener("click", function () {
                inputUrlMovil.select();
                document.execCommand("copy");   // sirve tambien sin HTTPS, a diferencia del clipboard API
            });

            /* Token nuevo desde el navegador, con el mismo formato que genera el JSP al
               cargar la pagina: 8 caracteres hexadecimales. */
            function nuevoToken() {
                var t = "";
                while (t.length < 8) {
                    t += Math.floor(Math.random() * 16).toString(16);
                }
                return t;
            }

            /* Desvincular: corta los celulares emparejados y cambia el token, sin recargar.
               El aviso al servidor va ANTES de cerrar nuestra conexion, porque es el servidor
               el que cierra a los celulares: desde aca no se puede tocar la conexion de otro. */
            document.getElementById("btnDesvincular").addEventListener("click", function () {
                if (movilesConectados > 0 &&
                        !confirm("Hay " + movilesConectados + " celular(es) emparejado(s). Van a " +
                                 "quedar desvinculados y habra que escanear el QR nuevo. Continuar?")) {
                    return;
                }

                if (ws && ws.readyState === WebSocket.OPEN) {
                    ws.send('{"tipo":"desvincular"}');
                }

                TOKEN = nuevoToken();
                tokenActual.textContent = TOKEN;
                refrescarQr();

                movilesConectados = 0;
                estadoMoviles.textContent = "0";
                estadoMoviles.className = "badge text-bg-secondary";

                /* Cerramos la nuestra tambien: el onclose la levanta sola un par de segundos
                   despues, y conectar() lee TOKEN recien en ese momento, o sea ya con el nuevo. */
                if (ws) {
                    ws.close();
                }
            });

            refrescarQr();

            // ================= BLOQUE WEBSOCKET (esto es lo que se copia a un modulo real) =====

            var ws = null;
            var reintento = null;
            var latido = null;

            /* Latido. El servidor da la pantalla por muerta si deja de recibirlo, y asi el
               celular se entera de que no hay nadie escuchando en vez de recibir un ack
               positivo contra una conexion que ya no existe. Tiene que ser bastante mas
               frecuente que MS_LATIDO_VENCIDO en ScanEndpoint. */
            var MS_LATIDO = 8000;

            function conectar() {
                var proto = (location.protocol === "https:") ? "wss://" : "ws://";
                ws = new WebSocket(proto + location.host + CONTEXT_PATH + "/ws/scan/" + TOKEN + "/pc");

                ws.onopen = function () {
                    pintarEstado("conectado", "text-bg-success");
                    latir();
                };

                ws.onmessage = function (evento) {
                    var msg = JSON.parse(evento.data);

                    if (msg.tipo === "codigo") {
                        procesarCodigo(msg.valor, msg.formato);
                    } else if (msg.tipo === "estado" && typeof msg.moviles === "number") {
                        movilesConectados = msg.moviles;
                        estadoMoviles.textContent = msg.moviles;
                        estadoMoviles.className = "badge " +
                                (msg.moviles > 0 ? "text-bg-success" : "text-bg-secondary");
                    }
                };

                /* La pantalla de la PC puede quedar abierta horas. Si el servidor se
                   reinicia o la red parpadea, hay que volver a conectarse solo: si no,
                   el operador escanea y no pasa nada, sin ninguna pista de por que. */
                ws.onclose = function () {
                    pintarEstado("reconectando...", "text-bg-warning");
                    clearInterval(latido);
                    clearTimeout(reintento);
                    reintento = setTimeout(conectar, 2000);
                };

                ws.onerror = function () {
                    pintarEstado("error", "text-bg-danger");
                };
            }

            /* clearInterval antes de crear el nuevo: cada reconexion pasa por aca y sin
               esto quedarian varios intervalos latiendo en paralelo. */
            function latir() {
                clearInterval(latido);
                latido = setInterval(function () {
                    if (ws && ws.readyState === WebSocket.OPEN) {
                        ws.send('{"tipo":"latido"}');
                    }
                }, MS_LATIDO);
            }

            function pintarEstado(texto, clase) {
                estadoWs.textContent = texto;
                estadoWs.className = "badge " + clase;
            }

            /* ---- PUNTO DE INTEGRACION ------------------------------------------------
               En un modulo real, el cuerpo de esta funcion es lo unico que cambia:
               en vez de agregar una fila a la tabla de prueba, buscas el articulo por
               codigo de barras y lo agregas al detalle del documento. */
            function procesarCodigo(valor, formato) {
                var vacia = document.getElementById("filaVacia");
                if (vacia) {
                    vacia.remove();
                }

                var fila = document.createElement("tr");
                fila.className = "fila-nueva";
                fila.innerHTML =
                        '<td class="valor-codigo"></td>' +
                        '<td><span class="badge text-bg-light border"></span></td>' +
                        '<td class="text-muted small"></td>';
                // textContent y no innerHTML: el codigo llega de afuera y no lo interpretamos como HTML.
                fila.children[0].textContent = valor;
                fila.children[1].firstChild.textContent = formato || "?";
                fila.children[2].textContent = new Date().toLocaleTimeString();

                tabla.prepend(fila);   // el ultimo escaneo siempre visible arriba
                contador.textContent = ++totalRecibidos;
            }
            // ================= FIN BLOQUE WEBSOCKET ============================================

            document.getElementById("btnLimpiar").addEventListener("click", function () {
                tabla.innerHTML = '<tr id="filaVacia"><td colspan="3" class="text-center text-muted py-4">' +
                        'Esperando el primer escaneo...</td></tr>';
                totalRecibidos = 0;
                contador.textContent = "0";
            });

            conectar();
        </script>
    </body>
</html>
