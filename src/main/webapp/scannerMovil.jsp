<%--
    scannerMovil.jsp - Pagina que se abre EN EL CELULAR. Lee codigos con la camara y los
    manda por WebSocket a la pantalla de la PC que tenga el mismo token.

    IMPORTANTE - Esta pagina esta exceptuada del login en AuthFilter, porque el operador
    no va a loguearse en el telefono. La credencial de hecho es el token de la URL, que
    solo puede obtenerse escaneando el QR de una pantalla abierta por un usuario logueado.
    Ver la seccion de seguridad en SCANNER_MOVIL.md antes de exponer esto fuera de la LAN.

    IMPORTANTE - El navegador solo entrega la camara en contextos seguros (HTTPS o
    localhost). Entrando por http://192.168.x.x NO va a funcionar hasta habilitar el
    origen en chrome://flags/#unsafely-treat-insecure-origin-as-secure (ver el .md).
    La pagina detecta ese caso y lo explica en pantalla en vez de fallar en silencio.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    /* El token viene del QR. Lo limitamos a caracteres seguros antes de inyectarlo en el
       JavaScript de abajo: nunca confiamos en un parametro de la URL. */
    String tokenParam = request.getParameter("token");
    String tokenLimpio = (tokenParam == null) ? "" : tokenParam.replaceAll("[^A-Za-z0-9\\-]", "");
%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta charset="UTF-8">
        <%-- user-scalable=no: evita que el operador haga zoom sin querer mientras apunta --%>
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
        <title>Escaner</title>
        <link href="Bootstrap 5.3.3/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
        <style>
            body { background: #111; color: #eee; }
            /* La camara ocupa toda la pantalla: el operador apunta con el telefono,
               no mira botones. object-fit evita que la imagen salga deformada. */
            #video { width: 100%; height: 62vh; object-fit: cover; background: #000; border-radius: .5rem; }
            .mira {
                position: absolute; top: 50%; left: 8%; width: 84%; height: 90px;
                transform: translateY(-50%); border: 3px solid rgba(255,255,255,.85);
                border-radius: .5rem; pointer-events: none;
            }
            #ultimo { font-family: monospace; font-size: 1.35rem; font-weight: 700; word-break: break-all; }
            .panel { background: #1c1c1c; border-radius: .5rem; }
        </style>
    </head>
    <body>
        <div class="container-fluid p-2">

            <%-- Barra de estado: el operador tiene que poder ver de un vistazo si sigue
                 conectado. Sin esto puede escanear 20 items contra una conexion muerta. --%>
            <div class="d-flex justify-content-between align-items-center mb-2">
                <span id="estado" class="badge text-bg-secondary">iniciando...</span>
                <span class="badge text-bg-dark">leidos: <span id="contador">0</span></span>
            </div>

            <div id="zonaError" class="alert alert-warning d-none small"></div>

            <div class="position-relative">
                <video id="video" playsinline muted></video>
                <div class="mira"></div>
            </div>

            <div class="panel p-3 mt-2 text-center">
                <div class="text-muted small">ultimo codigo</div>
                <div id="ultimo">-</div>
                <div id="ultimoFormato" class="text-muted small mt-1">&nbsp;</div>
            </div>

            <button id="btnIniciar" class="btn btn-success btn-lg w-100 mt-3">Iniciar escaneo</button>
        </div>

        <script>
            /* Recordatorio: en .jsp no se pueden usar template literals de JS, porque ${...}
               lo consume el motor de EL del servidor. Todo va concatenado con "+". */

            var TOKEN = "<%= tokenLimpio %>";
            var CONTEXT_PATH = "${pageContext.request.contextPath}";

            /* Formatos habilitados. Arrancamos amplio a proposito para descubrir que usa
               el catalogo; una vez que sepas cuales aparecen, dejar solo esos hace que el
               lector sea mas rapido y tenga menos falsos positivos. */
            var FORMATOS = ["ean_13", "ean_8", "upc_a", "upc_e", "code_128", "code_39", "itf", "qr_code"];

            /* Ventana anti-rebote. La camara ve el mismo codigo ~30 veces por segundo:
               sin esto, un solo producto entraria decenas de veces. */
            var MS_ANTIREBOTE = 2000;

            var video = document.getElementById("video");
            var estado = document.getElementById("estado");
            var zonaError = document.getElementById("zonaError");
            var elUltimo = document.getElementById("ultimo");
            var elFormato = document.getElementById("ultimoFormato");
            var elContador = document.getElementById("contador");
            var btnIniciar = document.getElementById("btnIniciar");

            var ws = null, reintento = null, stream = null;
            var ultimoValor = "", ultimoMomento = 0, total = 0;
            var audioCtx = null;

            // ------------------------------------------------------------------ utilidades UI

            function pintarEstado(texto, clase) {
                estado.textContent = texto;
                estado.className = "badge " + clase;
            }

            function mostrarError(html) {
                zonaError.innerHTML = html;
                zonaError.classList.remove("d-none");
            }

            /* Feedback al escanear: sin beep + vibracion el operador no sabe si leyo
               y termina escaneando el mismo producto dos veces. */
            function avisar(exito) {
                if (navigator.vibrate) {
                    navigator.vibrate(exito ? 90 : [70, 60, 70]);
                }
                if (!audioCtx) {
                    return;
                }
                var osc = audioCtx.createOscillator();
                var vol = audioCtx.createGain();
                osc.connect(vol);
                vol.connect(audioCtx.destination);
                osc.frequency.value = exito ? 1200 : 380;
                vol.gain.setValueAtTime(0.18, audioCtx.currentTime);
                vol.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.14);
                osc.start();
                osc.stop(audioCtx.currentTime + 0.15);
            }

            // ------------------------------------------------------------------- WebSocket

            function conectar() {
                var proto = (location.protocol === "https:") ? "wss://" : "ws://";
                ws = new WebSocket(proto + location.host + CONTEXT_PATH + "/ws/scan/" + TOKEN + "/movil");

                ws.onopen = function () {
                    pintarEstado("conectado", "text-bg-success");
                };

                ws.onmessage = function (evento) {
                    var msg = JSON.parse(evento.data);

                    if (msg.tipo === "ack") {
                        /* El servidor confirma que el codigo llego a la pantalla. Si la PC
                           cerro la pagina, avisamos fuerte en vez de dejar que siga escaneando. */
                        if (!msg.ok) {
                            pintarEstado(msg.msg || "la PC no responde", "text-bg-danger");
                            avisar(false);
                        } else {
                            pintarEstado("conectado", "text-bg-success");
                        }
                    } else if (msg.tipo === "estado" && msg.emparejado === false) {
                        pintarEstado("esperando la pantalla de la PC", "text-bg-warning");
                    }
                };

                /* El celular se bloquea, cambia de Wi-Fi o el navegador suspende la pestania:
                   la conexion se cae en silencio. Reintento indefinido cada 2 segundos. */
                ws.onclose = function () {
                    pintarEstado("reconectando...", "text-bg-warning");
                    clearTimeout(reintento);
                    reintento = setTimeout(conectar, 2000);
                };
            }

            function enviarCodigo(valor, formato) {
                var ahora = Date.now();

                // Anti-rebote: mismo valor dentro de la ventana -> se ignora.
                if (valor === ultimoValor && (ahora - ultimoMomento) < MS_ANTIREBOTE) {
                    return;
                }
                ultimoValor = valor;
                ultimoMomento = ahora;

                elUltimo.textContent = valor;
                elFormato.textContent = formato || "";
                elContador.textContent = ++total;
                avisar(true);

                if (ws && ws.readyState === WebSocket.OPEN) {
                    ws.send(JSON.stringify({ tipo: "codigo", valor: valor, formato: formato }));
                } else {
                    pintarEstado("sin conexion - no se envio", "text-bg-danger");
                }
            }

            // --------------------------------------------------------------------- camara

            function abrirCamara() {
                /* facingMode "environment" = camara trasera. El ideal de resolucion ayuda a
                   leer codigos de barras finos (EAN-13 impreso chico) sin exigir de mas. */
                return navigator.mediaDevices.getUserMedia({
                    video: {
                        facingMode: { ideal: "environment" },
                        width: { ideal: 1280 },
                        height: { ideal: 720 }
                    },
                    audio: false
                }).then(function (s) {
                    stream = s;
                    video.srcObject = s;
                    return video.play();
                });
            }

            /* Camino principal: BarcodeDetector nativo de Chrome Android. Es el mas rapido
               porque la deteccion la hace el sistema operativo, no JavaScript. */
            function escanearConDetectorNativo() {
                var detector = new BarcodeDetector({ formats: FORMATOS });

                function ciclo() {
                    if (!stream) {
                        return;
                    }
                    detector.detect(video).then(function (codigos) {
                        if (codigos.length > 0) {
                            enviarCodigo(codigos[0].rawValue, codigos[0].format);
                        }
                    }).catch(function () {
                        /* Un frame suelto puede fallar (por ejemplo justo al rotar la
                           pantalla). No es motivo para cortar el escaneo. */
                    }).then(function () {
                        setTimeout(ciclo, 150);   // ~6 lecturas/seg: suficiente y no recalienta el telefono
                    });
                }
                ciclo();
            }

            /* Camino alternativo: si el navegador no trae BarcodeDetector, cargamos ZXing.
               Se descarga solo en ese caso (son 330 KB) para no penalizar a los equipos
               que si tienen soporte nativo. */
            function escanearConZxing() {
                var tag = document.createElement("script");
                tag.src = "scanner/zxing.min.js";
                tag.onload = function () {
                    var lector = new ZXing.BrowserMultiFormatReader();
                    lector.decodeFromVideoDevice(undefined, video, function (resultado) {
                        if (resultado) {
                            enviarCodigo(resultado.getText(), "zxing");
                        }
                    });
                    pintarEstado("conectado (modo compatible)", "text-bg-success");
                };
                tag.onerror = function () {
                    mostrarError("No se pudo cargar el lector alternativo.");
                };
                document.head.appendChild(tag);
            }

            // ----------------------------------------------------------------------- arranque

            btnIniciar.addEventListener("click", function () {
                btnIniciar.disabled = true;
                btnIniciar.textContent = "Escaneando...";

                /* El AudioContext debe crearse dentro de un gesto del usuario, si no el
                   navegador lo deja suspendido y nunca suena el beep. Por eso el boton. */
                try {
                    audioCtx = new (window.AudioContext || window.webkitAudioContext)();
                } catch (e) {
                    audioCtx = null;
                }

                /* getUserMedia solo existe en contexto seguro. Si la pagina se abrio por
                   http://192.168.x.x, aca es donde se nota: explicamos como habilitarlo
                   en lugar de mostrar un error incomprensible. */
                if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
                    mostrarError(
                        "<strong>El navegador no permite usar la c&aacute;mara en esta direcci&oacute;n.</strong><br>" +
                        "Solo la habilita por HTTPS o localhost. Para usarla por IP, en este celular abr&iacute;:<br>" +
                        "<code>chrome://flags/#unsafely-treat-insecure-origin-as-secure</code><br>" +
                        "agreg&aacute; <code>" + location.origin + "</code>, pon&eacute; <b>Enabled</b> y reinici&aacute; Chrome.");
                    btnIniciar.disabled = false;
                    btnIniciar.textContent = "Reintentar";
                    return;
                }

                abrirCamara().then(function () {
                    if ("BarcodeDetector" in window) {
                        escanearConDetectorNativo();
                    } else {
                        escanearConZxing();
                    }
                }).catch(function (e) {
                    mostrarError("<strong>No se pudo abrir la c&aacute;mara:</strong> " + e.name +
                                 ". Revis&aacute; que le hayas dado permiso a la p&aacute;gina.");
                    btnIniciar.disabled = false;
                    btnIniciar.textContent = "Reintentar";
                });
            });

            if (!TOKEN) {
                mostrarError("<strong>Falta el token.</strong> Esta p&aacute;gina se abre escaneando " +
                             "el QR de la pantalla de la PC, no a mano.");
                btnIniciar.disabled = true;
            } else {
                conectar();
            }
        </script>
    </body>
</html>
