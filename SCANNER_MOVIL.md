# Escaneo de códigos desde el celular

Permite usar la cámara de un celular Android como lector de códigos de barras / QR: el
operador escanea con el teléfono y el código aparece al instante en una pantalla abierta
en la PC.

No requiere instalar ninguna aplicación (ni en el celular ni en la PC), no tiene costo de
licencia y no agrega dependencias Maven: JSR-356 WebSocket ya viene en GlassFish/Payara.

Estado actual: **pantalla de prueba aislada**, para validar el circuito y descubrir qué
formatos de código usa el catálogo antes de integrarlo a un módulo real.

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
- **Confirmación de entrega.** Si la pantalla de la PC se cerró, el celular recibe un ack
  negativo y avisa en rojo con un sonido distinto. Sin eso el operador escanearía veinte
  ítems contra una conexión muerta sin enterarse.
- **Anti-rebote.** La cámara ve el mismo código unas 30 veces por segundo. Se ignora un
  valor repetido dentro de los 2 segundos (constante `MS_ANTIREBOTE`).
- **Reconexión automática.** El celular se bloquea, cambia de Wi-Fi o el navegador suspende
  la pestaña; ambos extremos reintentan cada 2 segundos y muestran el estado en pantalla.

---

## 2. Archivos

| Archivo | Rol |
|---|---|
| `src/main/java/controlador/ws/ScanEndpoint.java` | Endpoint WebSocket. Empareja por token y reenvía. Payara lo publica solo por la anotación `@ServerEndpoint`, no hay que registrar nada. |
| `src/main/webapp/scannerTest.jsp` | Pantalla de la PC. Genera el token, muestra el QR y lista los códigos recibidos. Requiere login. |
| `src/main/webapp/scannerMovil.jsp` | Página que se abre en el celular. Cámara, lectura y envío. **Exceptuada del login.** |
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

Buscar el adaptador en uso — *Adaptador de LAN inalámbrica Wi-Fi* o *Adaptador de Ethernet* —
y tomar el campo **Dirección IPv4**. Normalmente algo como `192.168.1.50`.

**Cuáles NO sirven.** Con WSL instalado, `ipconfig` lista varios adaptadores virtuales:

| Descartar | Por qué |
|---|---|
| `vEthernet (WSL)`, típicamente `172.x.x.x` | Red virtual interna de Windows; el celular no llega ahí. |
| `vEthernet (Default Switch)` | Igual, virtual. |
| `127.0.0.1` | Es la propia máquina. |
| Cualquiera marcado *Desconectado* | Sin uso. |

La correcta es casi siempre la que empieza con **`192.168.`**, la del adaptador por el que
la PC tiene internet.

**Verificar antes de seguir.** Desde el celular, en el mismo Wi-Fi, abrir:

```
http://192.168.1.50:8080/Taller3ro/
```

Si carga el login del sistema, la IP es correcta y el firewall deja pasar. Si no carga, no
tiene sentido configurar el flag: primero hay que resolver eso (ver 3.4, o confirmar que el
celular está en el Wi-Fi y no con datos móviles).

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

### 3.4 Firewall

El celular tiene que poder llegar al puerto de Payara. En Windows, la primera vez hay que
permitir el puerto (8080, o 8181 con HTTPS) en el Firewall de Windows para redes privadas.
Si el QR carga pero el estado queda en "reconectando...", casi siempre es el firewall
bloqueando la conexión WebSocket.

---

## 4. Uso

1. **En la PC**, ya logueado, abrir:

   ```
   http://localhost:8080/Taller3ro/scannerTest.jsp
   ```

2. En **"Dirección del servidor"**, poner la IP por la que el celular ve al servidor, con
   puerto: `http://192.168.1.50:8080`.

   > La pantalla intenta adivinarla, pero **no puede quedar `localhost`**: para el teléfono,
   > `localhost` es el propio teléfono. El valor se guarda en el navegador para la próxima vez.

   Cómo identificar la IP correcta y cuáles descartar: **sección 3.1**. Tiene que ser
   exactamente la misma que cargaste en el flag de Chrome.

3. El QR se regenera automáticamente. **Escanearlo con la cámara del celular** (la app de
   cámara de Android ya detecta QR y ofrece abrir el link).

4. En el celular, tocar **"Iniciar escaneo"** y dar permiso de cámara.

5. Apuntar a un producto. Debería sonar un beep, vibrar, y el código aparecer en la tabla
   de la PC.

Si el QR no se puede escanear, el link también está en texto abajo, con botón de copiar.

---

## 5. Qué mirar en esta prueba

La pantalla muestra una columna **"Formato"** con el tipo de código detectado. Ése es el
objetivo del ejercicio: escanear varios productos del catálogo y anotar qué formatos
aparecen realmente.

Hoy están habilitados todos estos, a propósito, para descubrirlo:

```
ean_13, ean_8, upc_a, upc_e, code_128, code_39, itf, qr_code
```

Lo esperable en productos comerciales es **`ean_13`** (y `upc_a` en importados de EE.UU.).
Una vez confirmado, conviene reducir la lista a los que se usan de verdad: el lector se
vuelve más rápido y baja mucho la chance de una lectura errónea. Se edita la constante
`FORMATOS` en `scannerMovil.jsp`.

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
   invalidarlos a los N minutos sin uso.
3. **No exponer el puerto fuera de la LAN.** Si en algún momento se publica, HTTPS deja de
   ser opcional: sin `wss://` los códigos viajan en texto plano.

---

## 8. Problemas frecuentes

| Síntoma | Causa más probable |
|---|---|
| El botón "Iniciar escaneo" muestra el aviso de cámara no disponible | Falta el flag de Chrome del punto 3.2, o el origen quedó mal escrito (puerto, barra final). |
| El QR abre la página pero queda en "reconectando..." | Firewall bloqueando el puerto, o el celular está en otra red (Wi-Fi vs datos móviles). |
| El celular abre `localhost` y no carga nada | Quedó `localhost` en "Dirección del servidor". Poner la IP real del equipo. |
| Escanea, suena el beep, pero no llega a la PC | La pantalla de la PC se cerró o recargó. El celular debería mostrar el aviso en rojo. |
| El mismo producto entra varias veces | Subir `MS_ANTIREBOTE` en `scannerMovil.jsp`. |
| Lee lento o confunde códigos | Reducir `FORMATOS` a los que usa el catálogo (punto 5). |
| Códigos de barras finos no se leen | Mejorar la luz y acercar. Los EAN-13 impresos chicos necesitan buen enfoque; si es sistemático, subir el `width/height` ideal en `abrirCamara()`. |
| El celular se apaga solo mientras escanea | Es el bloqueo de pantalla de Android. La reconexión automática lo cubre, pero conviene subir el tiempo de espera del teléfono. |

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
