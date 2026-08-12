# Escaneo de códigos desde el celular

Permite usar la cámara de un celular Android como lector de códigos de barras / QR: el
operador escanea con el teléfono y el código aparece al instante en una pantalla abierta
en la PC.

No requiere instalar ninguna aplicación (ni en el celular ni en la PC), no tiene costo de
licencia y no agrega dependencias Maven: JSR-356 WebSocket ya viene en GlassFish/Payara.

Estado actual: **pantalla de prueba aislada**, para validar el circuito y descubrir qué
formatos de código usa el catálogo antes de integrarlo a un módulo real.

> **Plataforma.** Payara 5 / Java EE 8: el endpoint usa `javax.websocket` (JSR-356). Al
> pasar a Payara 6 o superior (Jakarta EE 9+) hay que renombrar los imports `javax.*` a
> `jakarta.*` en `ScanEndpoint`; el resto del código no cambia.

---

## 1. Cómo funciona

El problema de fondo es que el servidor tiene que **empujar** un dato hacia una pantalla que
ya está abierta. Con REST eso no se puede sin andar consultando cada pocos segundos; por eso
se usa un WebSocket, que mantiene la conexión abierta en ambos sentidos.

Los dos extremos se vinculan con un **token**, y nada más que con eso:

```
   PC (scannerTest.jsp)                                Celular (scannerMovil.jsp)
           |                                                      |
           |  1. abre WS  /ws/scan/{token}/pc                     |
           |----------------------------------------->            |
           |                                                      |
           |  2. muestra un QR con la URL del celular             |
           |     (incluye el token)                               |
           |            . . . . . . . . . . . . . . . . . . . . . >|  3. escanea el QR
           |                                                      |     una sola vez
           |                                                      |
           |            4. abre WS /ws/scan/{token}/movil         |
           |            <-----------------------------------------|
           |                                                      |
           |            5. cada código leído viaja como JSON      |
           |            <-----------------------------------------|
           |  6. ScanEndpoint lo reenvía a la PC con ese token    |
           |                                                      |
           |            7. ack de vuelta -> beep + vibración      |
           |            ----------------------------------------->|
```

Puntos de diseño que conviene conocer:

- **El servidor no interpreta el código.** El celular arma el JSON y `ScanEndpoint` lo
  reenvía textual. Por eso no hace falta ninguna librería JSON del lado Java: para agregar
  campos al mensaje se toca sólo el JavaScript.
- **Un receptor por token, varios emisores.** Si la PC recarga la página, la conexión nueva
  reemplaza a la vieja. Pueden emparejarse varios celulares contra la misma pantalla.
- **Confirmación de entrega.** Si la pantalla de la PC no está, el celular recibe un ack
  negativo y avisa en rojo con un sonido distinto. Sin eso el operador escanearía veinte
  ítems contra una conexión muerta sin enterarse.
- **Latido.** `isOpen()` no alcanza para saber si la pantalla sigue ahí: cuando la PC se
  queda sin red o se suspende sin cerrar el TCP, la sesión figura abierta durante minutos
  y el ack diría que sí cuando el código en realidad se perdió. Por eso los dos navegadores
  mandan `{"tipo":"latido"}` cada 8 segundos y el servidor descarta la pantalla que dejó de
  latir. Ver el alcance exacto en **1.1**.
- **Anti-rebote.** La cámara ve el mismo código varias veces por segundo mientras el
  operador apunta a la etiqueta. El código queda bloqueado **mientras esté a la vista**, y
  se libera 2 segundos después de dejar de verse (constante `MS_ANTIREBOTE`). Medir desde
  el último avistaje y no desde la última carga es lo que evita que el mismo producto entre
  cada 2 segundos con el teléfono quieto.
- **Reconexión automática.** El celular se bloquea, cambia de Wi-Fi o el navegador suspende
  la pestaña; ambos extremos reintentan cada 2 segundos y muestran el estado en pantalla.

### 1.1 Las tres constantes del latido

Se mueven juntas y el orden entre ellas es lo que importa:

| Constante | Dónde | Valor | Qué pasa si se toca |
|---|---|---|---|
| `MS_LATIDO` | los dos JSP | 8 s | Cada cuánto late el navegador. Bajarlo no mejora nada; subirlo obliga a subir las otras dos. |
| `MS_LATIDO_VENCIDO` | `ScanEndpoint` | 75 s | Sin latido por más de esto, la pantalla se da por muerta y se cierra. |
| `MS_TIMEOUT_INACTIVIDAD` | `ScanEndpoint` | 90 s | `setMaxIdleTimeout`: corte del contenedor para sesiones sin nada de tráfico. Libera las abandonadas. |

**Por qué 75 segundos y no 25**, que sería lo natural con un latido de 8: Chrome estrangula
los timers de las pestañas en segundo plano y, después de 5 minutos oculta, un `setInterval`
pasa a correr **una vez por minuto**. Con una tolerancia corta, una pantalla minimizada pero
perfectamente viva quedaría marcada como muerta y estaría reconectándose todo el tiempo. El
margen tiene que quedar por encima de ese minuto, y el timeout del contenedor por encima del
margen.

**Lo que esto deja afuera.** Si la PC muere de forma sucia, hay una ventana de hasta 75
segundos en la que el celular todavía recibe `ack ok:true` y el código se pierde. Es un
techo conocido en vez del comportamiento anterior, que podía durar minutos, pero no es cero.
Llevarlo a cero requiere el otro enfoque: que el ack positivo lo emita **la pantalla** al
recibir el código y el servidor sólo lo reenvíe. Ahí no hay constante de tiempo que
sintonizar y además es inmune al estrangulamiento, porque los mensajes entrantes de un
WebSocket no se estrangulan como los timers. Vale la pena si el escaneo llega a usarse en
una carga larga y desatendida.

---

## 2. Archivos

| Archivo | Rol |
|---|---|
| `src/main/java/controlador/ws/ScanEndpoint.java` | Endpoint WebSocket. Empareja por token y reenvía. Payara lo publica solo por la anotación `@ServerEndpoint`, no hay que registrar nada. |
| `src/main/webapp/scannerTest.jsp` | Pantalla de la PC. Genera el token, muestra el QR, lista los códigos recibidos y permite desvincular. Requiere login. |
| `src/main/webapp/scannerMovil.jsp` | Página que se abre en el celular. Cámara, lectura, envío y botón de detener. **Exceptuada del login.** |
| `src/main/webapp/scanner/qrcode.min.js` | Generador de QR (qrcodejs, MIT, 20 KB). |
| `src/main/webapp/scanner/zxing.min.js` | Lector alternativo (ZXing, Apache 2.0, 330 KB). Se descarga **sólo** si el navegador no trae `BarcodeDetector`. |
| `src/main/java/controlador/AuthFilter.java` | Modificado: 5 líneas que exceptúan `scannerMovil.jsp` y `/ws/scan/` del login. |

---

## 3. Requisito de HTTPS — leer antes de probar

**El navegador sólo entrega la cámara en un "contexto seguro": HTTPS o `localhost`.**

Esto no es opcional ni configurable desde la aplicación. Entrando desde el celular a
`http://192.168.1.50:8080`, Chrome **no va a pedir permiso de cámara: simplemente no la va
a dar**. Es la causa número uno de que este tipo de proyecto se trabe.

La página lo detecta y muestra las instrucciones en pantalla en vez de fallar en silencio,
pero la solución hay que aplicarla en cada celular.

### 3.1 Qué dirección IP usar

Es la IP de **la PC donde corre Payara**, vista desde la red local. No la del celular.

La lógica: el flag le dice a Chrome del celular "confiá en este origen aunque sea HTTP", y
el origen es la dirección del servidor al que el teléfono se conecta.

En la PC donde corre Payara, en `cmd`:

```
ipconfig
```

`ipconfig` suele listar media docena de adaptadores y varios tienen Dirección IPv4. La regla
para elegir entre todos, en una línea:

> **Sirve el único adaptador que tiene "Puerta de enlace predeterminada" con un valor.**

Esa es la marca de que el adaptador tiene salida hacia afuera de la máquina. Todos los demás
son redes que la PC inventó para sí misma: tienen IP, pero no llevan a ninguna parte.

**Cuáles NO sirven**, aunque aparezcan con IPv4:

| Descartar | Por qué |
|---|---|
| `VMware Network Adapter VMnet1` / `VMnet8`, típicamente `192.168.x.1` | Redes de las máquinas virtuales de VMware. **Empiezan con `192.168.` igual que la buena**, así que sólo se distinguen por la puerta de enlace vacía. |
| `VirtualBox Host-Only Network`, `192.168.56.x` | Igual, de VirtualBox. |
| `vEthernet (WSL)` / `vEthernet (Default Switch)`, típicamente `172.x.x.x` | Redes virtuales de Windows (WSL, Hyper-V). |
| `127.0.0.1` | Es la propia máquina. |
| Cualquiera marcado *Desconectado* | Sin uso. |

Pista extra: si la IP del adaptador **termina en `.1`**, casi siempre es una red virtual. El
`.1` suele ser el router de la red; que tu PC *sea* el `.1` significa que esa red la creó
ella misma. La de la PC en la red real es un número cualquiera asignado por el router
(`192.168.3.23`, `192.168.1.50`).

> Cuidado con la regla vieja de "la que empieza con `192.168.`": VMware y VirtualBox usan
> ese mismo rango. Sin mirar la puerta de enlace no alcanza.

**Verificar antes de seguir.** Desde el celular, en el mismo Wi-Fi, abrir:

```
http://192.168.1.50:8080/Taller3ro/
```

Si carga el login del sistema, la IP es correcta y el firewall deja pasar. Si no carga, no
tiene sentido configurar el flag: primero hay que resolver eso (ver el **Anexo A**, o
confirmar que el celular está en el Wi-Fi y no con datos móviles).

**Ese mismo valor va en dos lugares, idéntico:**

1. El flag de Chrome (3.2): `http://192.168.1.50:8080`
2. El campo "Dirección del servidor" de `scannerTest.jsp` (sección 4): `http://192.168.1.50:8080`

Si no coinciden, el QR apunta a un origen que el flag no cubre y la cámara sigue bloqueada.

### 3.2 Para desarrollar y probar: habilitar el origen en Chrome

En **cada celular**, una sola vez:

1. Abrir Chrome y entrar a:

   ```
   chrome://flags/#unsafely-treat-insecure-origin-as-secure
   ```

2. En el cuadro de texto de **"Insecure origins treated as secure"**, escribir el origen
   exacto del servidor:

   ```
   http://192.168.1.50:8080
   ```

3. Cambiar el desplegable de al lado de `Disabled` a **`Enabled`**.
4. Tocar **`Relaunch`** abajo para reiniciar Chrome.

Detalles que hacen fallar esto:

- El origen tiene que ser **exacto**: esquema, IP y puerto. Sin barra al final.
  `http://192.168.1.50:8080` sirve; `192.168.1.50:8080` o `http://192.168.1.50:8080/` no.
- Si la IP del servidor cambia (DHCP), hay que actualizar el flag. Conviene fijarle IP fija
  al servidor o reservarla en el router.
- Se pueden poner varios orígenes separados por coma.
- Es una opción de desarrollo. Para producción, usar HTTPS de verdad (siguiente punto).

### 3.3 Para producción: HTTPS real con mkcert

Un certificado autofirmado común no alcanza: Chrome en Android sigue bloqueando la cámara.
Hace falta un certificado que el celular reconozca como válido. `mkcert` genera una CA propia
que se instala una vez en cada teléfono y resuelve el tema definitivamente.

**En el servidor:**

```bash
# 1. Instalar mkcert y crear la CA local
mkcert -install

# 2. Emitir el certificado para la IP (o el nombre) del servidor
mkcert 192.168.1.50

# 3. Convertirlo al formato del keystore de Payara
openssl pkcs12 -export \
    -in 192.168.1.50.pem -inkey 192.168.1.50-key.pem \
    -out cert.p12 -name s1as -passout pass:changeit
```

**Cargarlo en Payara** (`s1as` es el alias que Payara espera para el certificado del
servidor; hay que borrar el que viene por defecto antes de importar):

```bash
cd $PAYARA_HOME/glassfish/domains/domain1/config

keytool -delete -alias s1as -keystore keystore.jks -storepass changeit

keytool -importkeystore \
    -srckeystore cert.p12 -srcstoretype PKCS12 -srcstorepass changeit \
    -destkeystore keystore.jks -deststorepass changeit -alias s1as
```

Reiniciar el dominio. El listener HTTPS de Payara es `http-listener-2`, puerto **8181**, así
que la aplicación queda en `https://192.168.1.50:8181/Taller3ro`.

**En cada celular**, instalar la CA de mkcert:

1. Copiar al teléfono el archivo `rootCA.pem` (está en la carpeta que informa
   `mkcert -CAROOT`) y **renombrarlo a `rootCA.crt`** — Android no reconoce la extensión
   `.pem` en el instalador.
2. *Ajustes → Seguridad → Cifrado y credenciales → Instalar un certificado → Certificado de CA*.
3. Android va a advertir que la red podría ser monitoreada. Es normal para una CA propia.

> Desde Android 7, las CA instaladas por el usuario no son válidas para las apps, pero
> **Chrome sí las acepta para navegar**, que es lo único que se necesita acá.

Con esto ya no hace falta el flag y el QR queda con una URL estable.

**El certificado queda atado a la IP.** `mkcert 192.168.1.50` emite el certificado *para esa
dirección*. Si el router la reasigna por DHCP, Chrome deja de validarlo y hay que reemitir el
certificado y recargar el keystore — no alcanza con actualizar el QR. Es el argumento más
fuerte para fijarle IP estática al servidor o reservársela en el router **antes** de emitirlo.
Si el servidor va a tener nombre de red, conviene emitirlo para el nombre en vez de la IP.

### 3.4 Si el celular no llega al servidor

Todo lo anterior asume que el teléfono ya carga el sistema por la IP de la red. Cuando no
carga nada, el problema es de acceso —firewall, perfil de red, aislamiento del router— y el
diagnóstico paso a paso está en el **Anexo A**, al final del documento.

---

## 4. Uso

1. **En la PC**, ya logueado, abrir:

   ```
   http://localhost:8080/Taller3ro/scannerTest.jsp
   ```

2. Revisar **"Dirección del servidor"**: la IP por la que el celular ve al servidor, con
   puerto — `http://192.168.1.50:8080`.

   La pantalla la completa sola con la del adaptador que tiene salida a la red, que es la
   correcta en la enorme mayoría de los casos. Si no funciona, el campo es un desplegable:
   tiene todas las direcciones del equipo para elegir a mano. **Nunca puede quedar
   `localhost`**, que para el teléfono es el propio teléfono. El valor elegido se guarda en
   el navegador para la próxima vez.

   Cómo identificar la correcta entre varias y cuáles descartar: **sección 3.1**. Tiene que
   ser exactamente la misma que cargaste en el flag de Chrome.

3. El QR se regenera automáticamente. **Escanearlo con la cámara del celular** (la app de
   cámara de Android ya detecta QR y ofrece abrir el link).

4. En el celular, tocar **"Iniciar escaneo"** y dar permiso de cámara.

5. Apuntar a un producto. Debería sonar un beep, vibrar, y el código aparecer en la tabla
   de la PC.

Si el QR no se puede escanear, el link también está en texto abajo, con botón de copiar.

### 4.1 Terminar

**En el celular, "Detener"** (el mismo botón de "Iniciar escaneo", que cambia). Apaga la
cámara, corta la conexión y deja el botón listo para arrancar de nuevo. Conviene usarlo al
terminar: si no, la cámara sigue encendida hasta que se cierre la pestaña — calienta el
teléfono, gasta batería y sigue leyendo lo que tenga enfrente mientras el operador camina
con él en la mano.

**En la PC, "Desvincular celulares"** corta a los emparejados y genera un token nuevo sin
recargar la página. Los teléfonos avisan en rojo *"desvinculado por la PC"* y dejan de
reintentar; para volver a usarlos hay que escanear el QR nuevo. Sirve cuando el operador se
va con el teléfono, cuando hay que pasarle el escaneo a otro equipo, o ante la duda de qué
teléfono quedó conectado. Si hay celulares emparejados, pide confirmación.

> Cerrar o recargar la pantalla de la PC produce el mismo corte, sólo que además pierde la
> tabla de códigos recibidos. El botón existe para no tener que hacer eso.

Después de desvincular, la propia pantalla se reconecta sola con el token nuevo: el estado
pasa un par de segundos por "reconectando..." y vuelve a "conectado". Es lo normal.

---

## 5. Qué mirar en esta prueba

La pantalla muestra una columna **"Formato"** con el tipo de código detectado. Ése es el
objetivo del ejercicio: escanear varios productos del catálogo y anotar qué formatos
aparecen realmente.

Hoy están habilitados todos estos, a propósito, para descubrirlo:

```
ean_13, ean_8, upc_a, upc_e, code_128, code_39, itf
```

Lo esperable en productos comerciales es **`ean_13`** (y `upc_a` en importados de EE.UU.).
Una vez confirmado, conviene reducir la lista a los que se usan de verdad: el lector se
vuelve más rápido y baja mucho la chance de una lectura errónea. Se edita la constante
`FORMATOS` en `scannerMovil.jsp`.

**`qr_code` está fuera de la lista a propósito**, aunque el escaneo empiece con un QR. Ese
QR lo lee la app de cámara de Android para abrir la página, no este lector. Ningún producto
se identifica por QR, pero hay QR por todos lados: promociones impresas en el envase y, sobre
todo, **el propio QR de emparejamiento en la pantalla de la PC**, que está justo enfrente del
operador y entra en el encuadre con sólo levantar un poco el teléfono. Con `qr_code` habilitado
eso se cargaba como si fuera un código de producto.

### 5.1 Varios códigos en el mismo encuadre

`BarcodeDetector.detect()` devuelve **todos** los códigos que ve, no uno. Pasa más seguido de
lo que parece: envases con el EAN y un QR al lado, o dos productos vecinos en góndola.

Se elige el que tenga el centro más cerca del centro de la imagen, que es donde el operador
está apuntando y donde la interfaz dibuja la mira. Antes se tomaba el primero de la lista,
que no sigue ningún orden útil.

Consecuencia práctica: **encuadrar importa**. Si entran dos productos, gana el que esté en
la mira, no el de mejor contraste ni el que el sistema haya detectado primero.

---

## 6. Integrarlo a un módulo real

El trabajo está pensado para que el traslado sea casi mecánico:

1. **Reusar el token del patrón Session+Token.** En `scannerTest.jsp` el token se genera al
   vuelo; en un módulo real se usa el token de 8 caracteres del documento en edición (el que
   ya maneja `FacturaCompraServlet`). Así el escaneo cae exactamente en la factura abierta,
   sin ningún concepto nuevo de emparejamiento.

2. **Copiar el bloque de WebSocket** de `scannerTest.jsp` — está delimitado entre los
   comentarios `BLOQUE WEBSOCKET` / `FIN BLOQUE WEBSOCKET`.

3. **Reescribir sólo `procesarCodigo(valor, formato)`**, que está marcada como
   `PUNTO DE INTEGRACION`. En vez de agregar una fila a la tabla de prueba, debe buscar el
   artículo por código de barras y agregarlo al detalle del documento.

Para el paso 3 hace falta una columna de código de barras en `articulo` (si todavía no
existe) y una búsqueda por ese campo en el DAO correspondiente.

Esa columna necesita **índice único**. Único porque si dos artículos comparten código el
escaneo carga cualquiera de los dos sin avisar nada — es una falla silenciosa y muy molesta
de diagnosticar después, cuando el error ya está en documentos emitidos. Índice porque la
búsqueda corre una vez por cada código escaneado, en el medio de la carga. Va a permitir
nulos: no todos los artículos del catálogo van a tener código cargado, y en la mayoría de
los motores varios `NULL` conviven sin violar el `UNIQUE`.

`ScanEndpoint` no necesita ningún cambio: ya rutea por token, sea cual sea su origen.

---

## 7. Seguridad

**Estado actual — adecuado para LAN y para esta etapa de prueba, no para exponer a internet.**

`scannerMovil.jsp` y `/ws/scan/` están exceptuados del login en `AuthFilter`, porque el
operador no va a loguearse en el teléfono. La credencial de hecho es el token, que sólo se
obtiene escaneando el QR de una pantalla abierta por un usuario ya autenticado.

La limitación concreta: **quien conozca o adivine un token puede inyectar códigos** en esa
pantalla. Con tokens de 8 caracteres hexadecimales, en una red local, el riesgo es bajo —
pero el sistema no lo impide.

Endurecimiento cuando haga falta, en orden de conveniencia:

1. **Validar la `HttpSession` en el handshake** con un `ServerEndpointConfig.Configurator`
   que implemente `modifyHandshake()`, y verificar ahí que el token pertenece a una sesión
   viva y logueada. Es lo que cierra el agujero de raíz.
2. **Caducar los tokens**: hoy viven mientras la pantalla esté abierta. Conviene además
   invalidarlos a los N minutos sin uso. Lo que ya existe es la **revocación manual**: el
   botón "Desvincular celulares" (4.1) corta a los emparejados y rota el token en el acto.
   No reemplaza a la caducidad automática, pero le da al operador una forma de cortar sin
   perder la pantalla.
3. **No exponer el puerto fuera de la LAN.** Si en algún momento se publica, HTTPS deja de
   ser opcional: sin `wss://` los códigos viajan en texto plano.

---

## 8. Problemas frecuentes

| Síntoma | Causa más probable |
|---|---|
| El botón "Iniciar escaneo" muestra el aviso de cámara no disponible | Falta el flag de Chrome del punto 3.2, o el origen quedó mal escrito (puerto, barra final). |
| Desde el celular no carga nada del sistema | Acceso de red bloqueado. Recorrer el diagnóstico paso a paso del **Anexo A** (firewall, perfil de red, aislamiento del router). |
| El QR abre la página pero queda en "reconectando..." | Específico del WebSocket: firewall, antivirus con inspección de red o proxy en el medio. Ver el final del **Anexo A**. |
| El celular abre `localhost` y no carga nada | Quedó `localhost` en "Dirección del servidor". Poner la IP real del equipo (3.1). |
| El celular abre una IP que existe pero no responde nada | La sugerida es de una red virtual (VMware, VirtualBox, WSL). Desplegar el campo "Dirección del servidor" y elegir la del adaptador con puerta de enlace (3.1). |
| Escanea, suena el beep, pero no llega a la PC | La pantalla de la PC se cerró o recargó. El celular debería mostrar el aviso en rojo. |
| El mismo producto entra varias veces seguidas | Subir `MS_ANTIREBOTE` en `scannerMovil.jsp`: es el tiempo que el código tiene que estar fuera de cámara antes de poder entrar de nuevo. |
| Entra un código con formato `qr_code` | Había un QR en el encuadre: el del envase, o el de emparejamiento en la pantalla de la PC. Ya no debería pasar, `qr_code` salió de `FORMATOS` (punto 5). |
| Entra el código del producto de al lado | Apuntar con el código dentro de la mira: cuando hay varios en la imagen gana el más centrado (5.1). |
| Entra un código que no existe, con formato `itf` o `code_39` | Lectura fantasma. Son los dos formatos sin dígito verificador obligatorio; si el catálogo no los usa, sacarlos de `FORMATOS` (punto 5). |
| Lee lento o confunde códigos | Reducir `FORMATOS` a los que usa el catálogo (punto 5). |
| Códigos de barras finos no se leen | Mejorar la luz y acercar. Los EAN-13 impresos chicos necesitan buen enfoque; si es sistemático, subir el `width/height` ideal en `abrirCamara()`. |
| El celular se apaga solo mientras escanea | Es el bloqueo de pantalla de Android. La reconexión automática lo cubre, pero conviene subir el tiempo de espera del teléfono. |
| El teléfono se calienta o se queda sin batería | Quedó la cámara encendida. Tocar **"Detener"** al terminar en vez de dejar la pestaña abierta (4.1). |
| El celular dice "desvinculado por la PC" | Alguien tocó "Desvincular celulares" en la pantalla. Escanear el QR nuevo para volver a emparejar (4.1). |
| La pantalla de la PC pasa sola a "reconectando..." cada tanto | El latido no está llegando a tiempo: red inestable, o la pestaña estuvo mucho rato en segundo plano. Si es sistemático, subir `MS_LATIDO_VENCIDO` y `MS_TIMEOUT_INACTIVIDAD` en `ScanEndpoint`, en ese orden (1.1). |

---

## 9. Alternativa considerada y descartada

**Barcode to PC** (app de terceros: celular + app de escritorio, inyecta el código como si se
tecleara) resolvía esto sin programar nada. Se descartó porque **es de pago**: la versión
gratuita da 300 escaneos en total, y después requiere licencia por equipo.

Aparte del costo, el enfoque de emulación de teclado tiene dos límites que esta solución no
tiene: el código va a donde esté el foco del cursor (si el operador hace clic en otra ventana,
se escribe ahí) y hay que instalar y mantener la app de escritorio en cada PC.

**Vale la pena tener presente** que un lector de código de barras Bluetooth barato hace lo
mismo que la emulación de teclado, sin licencia ni celular, y lee más rápido. Si el flujo se
reduce a "escanear dentro de un campo que ya existe", puede ser la opción más económica y
robusta. El celular conviene cuando se necesita movilidad real o mostrarle algo al operador
en la pantalla del teléfono.

---

## Anexo A — Acceso al servidor desde la red

Cómo lograr que el celular llegue al servidor. Sólo hace falta leerlo si el teléfono no
carga el sistema; si ya carga, el punto 3 alcanza.

**El servidor no necesita ninguna configuración especial.** Desplegando desde NetBeans, el
listener HTTP de GlassFish/Payara escucha en `0.0.0.0`, o sea en todas las interfaces de red,
no sólo en `localhost`. Que uno lo abra como `localhost` es simplemente la dirección que usa
la propia máquina; el servidor igual atiende por la IP de la red.

Cuando el celular no llega, el bloqueo casi nunca está en GlassFish sino en el sistema
operativo o en el router.

### La prueba que define todo

Desde el celular, conectado al mismo Wi-Fi, abrir en el navegador:

```
http://192.168.1.50:8080/Taller3ro/
```

- **Carga el login del sistema** → la red está bien. Seguir con el flag de Chrome (3.2).
- **No carga** → recorrer el diagnóstico de abajo en orden. No tiene sentido tocar el flag
  ni el QR hasta que esto funcione.

### Diagnóstico paso a paso

**Paso 1 — Confirmar que GlassFish está escuchando en toda la red**

En la PC del servidor, en `cmd`:

```
netstat -ano | findstr :8080
```

| Resultado | Significado |
|---|---|
| `0.0.0.0:8080` | Correcto: escucha en todas las interfaces. Pasar al paso 2. |
| `127.0.0.1:8080` | Sólo local. Hay que corregir el listener (ver más abajo). |
| No aparece nada | GlassFish no está corriendo o no arrancó bien. Revisar la salida de NetBeans. |

Si quedó atado a `127.0.0.1`, se corrige en la consola de administración
(`http://localhost:4848`) → *Configurations → server-config → Network Config → Network
Listeners → http-listener-1* → campo **Address**, poner `0.0.0.0` y reiniciar el dominio.
Es raro que pase, pero conviene descartarlo primero porque es de un vistazo.

**Paso 2 — Abrir el puerto en el Firewall de Windows**

Es la causa más frecuente por lejos. La primera vez que arranca GlassFish, Windows muestra
el cartel de *"¿Permitir que Java acceda a la red?"*. Si se canceló, o se dejó marcada sólo
la casilla de "Redes públicas", el puerto queda bloqueado para el resto de la red.

Abrir PowerShell **como administrador** y ejecutar:

```powershell
New-NetFirewallRule -DisplayName "GlassFish 8080" -Direction Inbound `
    -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
```

Para verificar que quedó creada:

```powershell
Get-NetFirewallRule -DisplayName "GlassFish 8080" | Format-List DisplayName, Enabled, Direction, Action
```

> Con HTTPS (3.3) el puerto a habilitar es el **8181**, no el 8080. Si se usan los dos,
> hay que crear las dos reglas.

**Paso 3 — Verificar que la red esté clasificada como "Privada"**

Si Windows clasificó el Wi-Fi como *Pública*, bloquea las conexiones entrantes de forma
mucho más agresiva y la regla del paso 2 (creada con `-Profile Private`) no se aplica.

```powershell
Get-NetConnectionProfile
```

El campo `NetworkCategory` tiene que decir `Private`. Si dice `Public`, se cambia en
*Configuración → Red e Internet → Wi-Fi → Propiedades → Perfil de red → Red privada*.

**Paso 4 — Descartar el aislamiento de clientes del router**

Si el firewall ya está abierto y la red es privada pero el celular sigue sin llegar, puede
ser *AP isolation* (aislamiento de clientes): el router impide que los dispositivos se vean
entre sí aunque estén en el mismo Wi-Fi.

Es lo habitual en **redes de invitados** y frecuente en redes de oficina. Cómo confirmarlo:

1. Desde el celular, hacer ping a la IP del servidor (con cualquier app de ping) o intentar
   abrir cualquier otro servicio de esa máquina.
2. Si nada de la PC responde pero ambos tienen internet, es aislamiento de clientes.

Se desactiva en la configuración del router (suele llamarse *AP Isolation*, *Client
Isolation* o *Aislamiento de AP*). Si es una red de invitados, la solución simple es pasar
ambos dispositivos a la red principal.

**Paso 5 — Confirmar que el celular está en el Wi-Fi y no en datos móviles**

Suena obvio, pero es un clásico: con la pantalla apagada un rato, algunos Android cortan el
Wi-Fi y siguen por datos. Desde datos móviles la IP `192.168.x.x` no existe.

Verificar el ícono de Wi-Fi y, si hace falta, desactivar los datos móviles mientras se prueba.

**Paso 6 — Revisar que la IP no haya cambiado**

Si el router asigna direcciones por DHCP, la IP del servidor puede cambiar al reiniciar la
PC. Eso rompe a la vez el flag de Chrome, el QR, cualquier acceso guardado y —si ya se pasó
a HTTPS— el certificado de mkcert, que se emite para una dirección concreta (3.3).

Volver a correr `ipconfig` (3.1) y comparar. Para que deje de pasar, conviene reservarle la
IP a esa máquina en el router o configurarle IP estática — sobre todo pensando en el pasaje
a servidor local.

### Síntoma particular: carga la página pero queda en "reconectando..."

Si `scannerMovil.jsp` abre bien pero el estado nunca pasa a "conectado", el problema no es
de acceso general sino específicamente de la conexión WebSocket. Causas posibles:

- El firewall dejó pasar la petición HTTP inicial pero bloquea la conexión persistente.
  Revisar que la regla del paso 2 sea de protocolo TCP y sin restricción de programa.
- Hay un proxy o antivirus con inspección de red en el medio, que corta las conexiones
  WebSocket. Probar desactivando temporalmente la inspección HTTP del antivirus.
- Se está entrando por HTTPS pero el WebSocket intenta salir por `ws://` (o al revés). La
  página elige el esquema automáticamente, así que esto sólo pasa si hay un proxy inverso
  en el medio que termina el TLS.
