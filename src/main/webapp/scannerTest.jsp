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
<%@page import="java.net.InetAddress"%>
<%
    /* Token de emparejamiento: es lo unico que vincula esta pantalla con el celular.
       8 caracteres, igual que el token del patron Session+Token del resto del sistema. */
    String token = UUID.randomUUID().toString().substring(0, 8);

    /* Mejor intento de adivinar la direccion por la que el CELULAR puede llegar al servidor.
       Ojo: si abris esta pantalla como "localhost", ese nombre no le sirve al telefono
       (para el, localhost es el propio telefono). Por eso el valor es editable en pantalla. */
    String hostSugerido = request.getServerName();
    if ("localhost".equals(hostSugerido) || "127.0.0.1".equals(hostSugerido)) {
        try {
            hostSugerido = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            hostSugerido = "";   // que lo complete el usuario a mano
        }
    }
    String origenSugerido = hostSugerido.isEmpty()
            ? ""
            : request.getScheme() + "://" + hostSugerido + ":" + request.getServerPort();
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
                                       value="<%= origenSugerido %>"
                                       placeholder="http://192.168.1.50:8080">
                                <div class="form-text">
                                    No puede ser <code>localhost</code>: para el celular, eso apunta a si mismo.
                                    Usa la IP del equipo en la red. Se recuerda para la proxima vez.
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
                                <code><%= token %></code>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-2">
                                <span class="text-muted small">Conexion</span>
                                <span id="estadoWs" class="badge text-bg-secondary">conectando...</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-2">
                                <span class="text-muted small">Celulares emparejados</span>
                                <span id="estadoMoviles" class="badge text-bg-secondary">0</span>
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
            var tabla = document.getElementById("tablaCodigos");
            var contador = document.getElementById("contador");
            var totalRecibidos = 0;
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

            refrescarQr();

            // ================= BLOQUE WEBSOCKET (esto es lo que se copia a un modulo real) =====

            var ws = null;
            var reintento = null;

            function conectar() {
                var proto = (location.protocol === "https:") ? "wss://" : "ws://";
                ws = new WebSocket(proto + location.host + CONTEXT_PATH + "/ws/scan/" + TOKEN + "/pc");

                ws.onopen = function () {
                    pintarEstado("conectado", "text-bg-success");
                };

                ws.onmessage = function (evento) {
                    var msg = JSON.parse(evento.data);

                    if (msg.tipo === "codigo") {
                        procesarCodigo(msg.valor, msg.formato);
                    } else if (msg.tipo === "estado" && typeof msg.moviles === "number") {
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
                    clearTimeout(reintento);
                    reintento = setTimeout(conectar, 2000);
                };

                ws.onerror = function () {
                    pintarEstado("error", "text-bg-danger");
                };
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
