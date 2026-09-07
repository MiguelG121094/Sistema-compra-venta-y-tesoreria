# Conciliación bancaria — flujo de la pantalla, paso a paso

Documento de uso: qué hace la pantalla en cada momento, campo por campo, y **por qué** lo hace así —
tanto por cómo funciona una conciliación bancaria de verdad como por cómo está armada la base de datos.

Está escrito para poder pasarlo a la especificación de caso de uso, igual que §E.1 del plan de tesorería
hizo con el fondo fijo.

- El **análisis y las decisiones de diseño** están en [`CONCILIACION_BANCARIA_PLAN.md`](CONCILIACION_BANCARIA_PLAN.md).
- El **encuadre dentro del módulo** está en [`MODULO_TESORERIA_PLAN.md`](MODULO_TESORERIA_PLAN.md) §F.
- Cubre el requerimiento **3.10**.

**Archivos:** `ConciliacionBancariaServlet.java` · `ConciliacionBancariaService.java` ·
`ConciliacionBancariaDAO.java` · `conciliacionBancaria.jsp`

---

## 1. Qué es una conciliación bancaria

### 1.1 Dos versiones de la misma plata

De una cuenta bancaria existen siempre **dos registros paralelos**:

| | Quién lo lleva | Cuándo anota el movimiento |
|---|---|---|
| **El libro** | la empresa (este sistema) | cuando **ocurre la operación**: el día que se emite el cheque, que se carga el débito |
| **El extracto** | el banco | cuando **el dinero se mueve en la cuenta**: el día que el cheque se presenta al cobro |

Los dos son correctos y casi nunca dan el mismo número, porque **anotan en momentos distintos**.
Conciliar es explicar esa diferencia movimiento por movimiento, hasta que no quede nada sin explicar.

### 1.2 Las partidas conciliatorias

Un movimiento que está en un registro y todavía no en el otro se llama **partida conciliatoria**. Son
las que explican la diferencia:

| Situación | Efecto |
|---|---|
| Cheque emitido y todavía no cobrado | el libro ya lo restó, el banco no → **el extracto está más alto** |
| Depósito hecho y todavía no acreditado | el libro ya lo sumó, el banco no → **el extracto está más bajo** |
| Comisión que el banco cobró y nadie cargó | el banco ya lo restó, el libro no → hay que **cargarla como débito** |

Las dos primeras se resuelven solas con el tiempo: el mes que viene el cheque se cobra. La tercera no:
es información que al sistema le falta y hay que darle de alta.

### 1.3 El cheque es el caso central

Un cheque tiene **alrededor de un mes** para presentarse al banco. Se emite el 28 de agosto y puede
cobrarse el 15 de setiembre. Esto obliga a dos cosas que dan forma a toda la pantalla:

1. Al conciliar agosto, ese cheque aparece **sin tildar**: el libro lo tiene, el extracto no.
2. Al conciliar setiembre, **tiene que volver a aparecer**, aunque no sea un movimiento de setiembre.

A eso lo llamamos **el arrastre**, y es la diferencia entre esta pantalla y una simple lista de
movimientos del mes.

---

## 2. La estructura de datos detrás de la pantalla

### 2.1 Las dos tablas

```
conciliacion_bancaria                       -- la cabecera: una cuenta, un período
    id_conc_bancaria            serial
    id_cuenta                   NOT NULL    -- se concilia cuenta por cuenta
    conc_bancaria_fecha_desde   NOT NULL
    conc_bancaria_fecha_hasta   NOT NULL
    conc_bancaria_fecha         NOT NULL    -- cuándo se hizo
    conc_bancaria_saldo_inicial NOT NULL    -- saldo del libro al abrir el período
    conc_bancaria_saldo_final   NOT NULL    -- saldo del libro al cerrarlo
    conc_banc_saldo_banco       NOT NULL    -- el que dice el extracto
    conc_bancaria_estado                    -- 'Vigente' / 'Anulado'
    conc_bancaria_tipo_cambio               -- al cierre; sólo cuenta en moneda extranjera

conciliacion_bancaria_detalle               -- un ítem por movimiento
    id_conc_bancaria            NOT NULL  ┐ PK compuesta
    conc_bancaria_nro_item      NOT NULL  ┘ (sin serial: el número lo pone la aplicación, 1..N)
    id_creditos                 NULL      ┐
    id_debitos                  NULL      │ el ítem apunta a UNO de estos
    id_forma_pago_det           NULL      │
    id_orden_pago               NULL      ┘ (acompaña al anterior, para describirlo)
    conc_bancaria_descripcion   NOT NULL
    conc_bancaria_monto         NOT NULL
    conc_bancaria_tipo          NOT NULL   -- 'Cred' / 'Deb' / 'Ch'
    conc_bancaria_conciliado    BOOLEAN NOT NULL
```

### 2.2 La conciliación es una foto, no un acumulador

El comentario de la tabla dice que *"cuando se guarda una orden de pago viene y guarda también en la
conciliación"*. **Ese diseño no se implementó, y no podría funcionar:** el detalle cuelga de una
cabecera que es *por cuenta y por período*, y en el momento de generar la orden de pago esa cabecera
todavía no existe — nadie sabe en qué conciliación va a caer ese cheque, ni si se va a conciliar.

Va al revés: **la conciliación se arma cuando se concilia.** Elegís cuenta y período, y el sistema sale
a buscar los movimientos. El detalle es la **foto** de lo que se decidió ese día.

Por eso `conc_bancaria_descripcion` y `conc_bancaria_monto` se guardan copiados en el detalle en vez de
leerse siempre del origen: son el valor que tenía el movimiento cuando se concilió.

### 2.3 Lo que se concilia es la forma de pago, no la orden de pago

Una orden de pago puede pagarse con **una transferencia de Itaú y dos cheques de Ueno**. En el extracto
de Itaú aparece *una línea*, no la orden entera. Conciliar por orden de pago daría montos que no existen
en ningún extracto.

La unidad real es la fila de `forma_pago_detalle`: tiene su propia cuenta, su propio monto y su propia
fecha. Por eso el detalle tiene **dos enlaces** para este caso:

- `id_forma_pago_det` → **el movimiento**, lo que se concilia.
- `id_orden_pago` → sólo para describirlo (mostrar el número de OP y el proveedor).

### 2.4 El saldo vive en las conciliaciones

La tabla `cuenta` **no tiene columna de saldo**. No hay un "saldo del sistema" que actualizar, y ningún
movimiento suma ni resta nada en ningún lado.

El saldo existe únicamente acá: cada conciliación guarda con qué saldo abrió y con cuál cerró, y **el
cierre de una es la apertura de la siguiente**. De ahí salen dos reglas de la pantalla: el saldo inicial
no se carga a mano, y los períodos de una cuenta no pueden tener huecos ni solaparse.

---

## 3. El flujo, paso a paso

### Paso 0 — La pantalla en reposo

Se entra por **Tesorería → Conciliación Bancaria**. Sin nada abierto, la pantalla está **inerte**: la
cabecera y la grilla están deshabilitadas y sólo se puede usar **Nuevo** y **Buscar Conciliación**.

> **Por qué.** Es el mismo patrón de Provisión, Orden de Pago y Rendición: el documento en edición vive
> en la sesión bajo un *token*, y sin token no hay documento que tocar. Eso permite tener dos pestañas
> con dos conciliaciones distintas sin que se mezclen.

### Paso 1 — Nuevo

Abre una conciliación en blanco, genera el token y habilita la cabecera. La fecha de la conciliación
queda tomada del día y el **Estado** se muestra como `Vigente`.

### Paso 2 — Banco

Combo con los bancos que **tienen alguna cuenta registrada**, no el referencial completo de entidades
financieras.

> **Por qué.** El banco no se guarda en ningún lado: sirve nada más para llegar a la cuenta. Ofrecer un
> banco sin cuentas sólo lleva a un combo de cuentas vacío. Mismo criterio que en Débitos y Créditos.

### Paso 3 — Nro de cuenta

Se habilita al elegir el banco y muestra sólo las cuentas de ese banco. **Elegir la cuenta dispara un
envío al servidor** (acción `CargarCuenta`), que trae tres cosas:

| Campo | De dónde sale |
|---|---|
| **Moneda** | de la cuenta (`cuenta.id_moneda`). Sólo se muestra |
| **Saldo inicial** | el `conc_bancaria_saldo_final` de la última conciliación **vigente** de esa cuenta; **0** si es la primera |
| **Fecha desde** | el día siguiente al `conc_bancaria_fecha_hasta` de esa misma conciliación; **vacío** si es la primera |

> **Por qué va al servidor.** Los dos últimos no se pueden calcular en la pantalla: hay que ir a buscar
> la conciliación anterior de esa cuenta. Hasta que no hay cuenta elegida, no hay saldo ni período que
> mostrar.

> **Por qué la moneda no se carga.** Es un dato de la cuenta, no de la conciliación. Repetirlo abre la
> puerta a que diga una cosa distinta de la cuenta. Mismo criterio que en la orden de pago.

### Paso 4 — Tipo de cambio al cierre

Opcional. Sólo tiene sentido en una cuenta en moneda extranjera, para poder valuar el saldo en guaraníes
al cierre del período.

> **Por qué no se convierte nada.** La conciliación es **por cuenta**, y todos los movimientos de una
> cuenta están en su moneda: los importes se comparan entre sí sin convertir. El tipo de cambio se
> guarda como dato del cierre, no se usa para calcular los saldos de esta pantalla.

### Paso 5 — El período

| Campo | Comportamiento |
|---|---|
| **Fecha desde** | de **sólo lectura**, ya cargada, si la cuenta tiene conciliaciones anteriores. Editable únicamente en la primera conciliación de esa cuenta |
| **Fecha hasta** | siempre lo elige el usuario. Es el cierre del período, normalmente el último día del mes del extracto |

> **Por qué el desde no se elige.** Como el saldo se encadena (§2.4), el período tiene que arrancar
> exactamente donde terminó el anterior. Si quedara un hueco, los movimientos de esos días no los
> conciliaría nadie y el saldo inicial arrastraría plata que no está explicada; si se solaparan, los
> movimientos se contarían dos veces. Dejarlo fijo evita el error en vez de avisarlo después.

### Paso 6 — Cargar movimientos

Es el botón que arma la grilla. **A partir de acá la cabecera queda congelada**: banco, cuenta y período
pasan a sólo lectura. Para cambiarlos hay que **Cancelar** y empezar de nuevo.

> **Por qué se congela.** Los movimientos de la grilla dependen de la cuenta y del período. Si se
> cambiaran después de armarla, lo tildado dejaría de corresponder con lo que se está mirando.

**Qué consulta hace.** Para la cuenta elegida, trae de tres orígenes:

| Origen | Filtro | Tipo |
|---|---|---|
| `forma_pago_detalle` | de esta cuenta, con fecha, de una OP **no anulada**, cheque no anulado | `Ch` si tiene cheque, `Deb` si es transferencia |
| `debitos` | de esta cuenta, estado `Vigente` | `Deb` |
| `creditos` | de esta cuenta, estado `Vigente` | `Cred` |

**El corte es sólo la fecha hasta.** No se filtra por fecha desde, y eso es a propósito:

```
la grilla  =  los movimientos del período  +  los anteriores que quedaron sin conciliar
```

Es el arrastre de §1.3. Un cheque de agosto que en agosto no se tildó tiene que volver a aparecer en
setiembre, y en octubre, hasta que se cobre.

**Cómo sabe el sistema qué sigue pendiente.** Un movimiento está pendiente mientras **no figure en el
detalle de ninguna conciliación que no esté anulada**. No se pregunta por `forma_pag_estado` ni por
`chq_estado`:

- **Débitos y créditos no tienen estado de conciliación** (`debitos_estado` es Vigente/Anulado, que es
  otro eje). Para ellos no hay alternativa, así que hacerlo igual para los tres orígenes sale gratis.
- Preguntando contra el detalle, **el arrastre no depende de que un `UPDATE` haya salido bien**. El dato
  es el hecho mismo de haber sido conciliado.

Si no hay nada pendiente, la pantalla lo avisa y la grilla queda vacía. Igual se puede grabar: un mes sin
movimientos es una conciliación válida.

### Paso 7 — La grilla, columna por columna

| Columna | Qué muestra | De dónde sale |
|---|---|---|
| **Conciliado** | el tilde | `conc_bancaria_conciliado` |
| **Emisión** | fecha de emisión | cheque: `chq_fecha_emision`; los demás: su propia fecha |
| **Fecha** | fecha del movimiento | `forma_pag_fecha` / `debitos_fecha` / `creditos_fecha` |
| **Número** | número del documento del sistema | OP: `ord_pag_numero`; débito/crédito: su nro de comprobante |
| **Detalle** | el concepto | OP: razón social del proveedor; débito/crédito: `debitos_detalle` / `creditos_detalle` |
| **Banco** / **Cuenta** | los de la cabecera | `cuenta` |
| **Nro. Doc.** | número del papel | cheque: `chq_numero`; transferencia: `forma_pag_referencia`; débito/crédito: su comprobante |
| **Tipo** | `Deb` / `Cred` / `Ch` | `conc_bancaria_tipo` |
| **Importe** | el monto | `forma_pag_monto` / `debito_monto` / `credito_monto` |

**Por qué dos fechas.** En un débito o un crédito coinciden. En un cheque no: se emite en un período y se
cobra en otro. Ver las dos juntas es lo que hace visible el arrastre — un cheque con Emisión de agosto
apareciendo en la conciliación de setiembre se explica solo.

**El filtro "Mostrar"** (Todos / Débitos / Créditos / Cheques) filtra la grilla sin ir al servidor. Sirve
sobre todo para revisar los cheques, que son los que se arrastran.

**La grilla no tiene paginado, tiene scroll.** Es a propósito: los tildes viajan como campos del
formulario y el paginado saca del documento las filas que no se ven, así que se perderían los tildes de
las otras páginas al grabar.

### Paso 8 — Tildar contra el extracto

Cada fila nace con un tilde por defecto, y ese default no es cosmético:

| Tipo | Nace | Por qué |
|---|---|---|
| `Deb` (transferencia) y `Cred` | **tildado** | se cargan cuando el movimiento **ya ocurrió** en el banco: están conciliados por definición |
| `Ch` (cheque) | **destildado** | el cheque se emitió, pero puede no haberse presentado todavía |

El usuario abre el extracto del banco y corrige: **tilda** los cheques que sí figuran, **destilda** lo
que el banco no muestre.

Lo que queda **sin tildar es la explicación de la diferencia** — son las partidas conciliatorias de §1.2.

### Paso 9 — Los saldos

Se carga un solo campo, **Saldo según extracto**, y la pantalla recalcula los otros tres en el momento,
cada vez que se tilda o se destilda algo.

| Campo | Cómo se calcula |
|---|---|
| **Saldo inicial** | viene encadenado del período anterior (paso 3) |
| **Saldo según extracto** | lo carga el usuario, copiado del papel del banco |
| **Extracto ajustado** | `saldo extracto + Σ créditos sin tildar − Σ (débitos + cheques) sin tildar` |
| **Saldo según libro** | `saldo inicial + Σ créditos del período − Σ (débitos + cheques) del período` |
| **Diferencia** | `extracto ajustado − saldo según libro` |

**En cero, la conciliación cuadra.** El camino es el del informe de resumen: se parte del saldo del
extracto, se lo ajusta con las partidas conciliatorias y se tiene que llegar al saldo del libro.

**Dos detalles que importan:**

- **Al saldo del libro entran sólo los movimientos del período, no los arrastrados.** El libro anotó ese
  cheque el mes en que se emitió; contarlo de nuevo el mes en que el banco lo cobra sería restarlo dos
  veces. En el extracto ajustado, en cambio, entran **todos** los que estén sin tildar, vengan del
  período o de antes: son partidas conciliatorias igual.
- **La diferencia no se guarda.** No hay columna y no hace falta: es la resta de dos números que sí se
  guardan.

### Paso 10 — Grabar

Pide confirmación y guarda todo en **una sola transacción**:

1. **La cabecera**, en estado `Vigente`. El saldo inicial se **vuelve a leer** de la conciliación
   anterior y el saldo final se **recalcula** en el servidor: son los dos números que encadenan un
   período con el siguiente y no pueden depender de lo que llegue de la pantalla.
2. **El detalle**, numerado 1..N, con los ítems **tildados y sin tildar**. Los que quedaron sin tildar
   son parte del documento: son la explicación de la diferencia.
3. **Los estados de lo conciliado:**
   - `forma_pag_estado` → `'Conciliado'` en lo tildado, `'Pendiente'` en lo que no.
   - `chq_estado` → `'Cobrado'` en los cheques tildados. Es el único lugar del sistema donde ese estado
     se usa: hasta que se tilda en una conciliación, un cheque está `Emitido` o `Entregado`.
   - Los débitos y los créditos **no se tocan**: no tienen estado de conciliación, su tilde vive
     solamente en el detalle.

> **Se puede grabar con diferencia distinta de cero.** Es deliberado: los ítems sin tildar son
> justamente su explicación, y como el saldo final sale del **libro** y no del extracto, una conciliación
> que no cuadra no ensucia el encadenado — el saldo que pasa al mes siguiente sigue siendo el contable.

Grabada, la pantalla vuelve al estado inerte del paso 0.

---

## 4. Las otras acciones

### Buscar Conciliación

Modal con todas las conciliaciones —anuladas incluidas—, con banco, cuenta, período, saldo según libro y
estado. **Cargar** la abre en modo lectura: se ve la cabecera y la grilla tal como se grabaron, con los
tildes deshabilitados, y se habilita **Anular**.

### Anular

**Marca, no borra.** La cabecera pasa a `'Anulado'` y la conciliación queda como historia, con su detalle
intacto. En la misma transacción se revierte todo lo que el grabado había cerrado:

| Se había puesto | Vuelve a |
|---|---|
| `forma_pag_estado` = `'Conciliado'` | `'Pendiente'` |
| `chq_estado` = `'Cobrado'` | `'Entregado'` si el cheque tiene fecha de entrega; si no, `'Emitido'` |

> El estado anterior del cheque no se guarda en ningún lado, pero se deduce: `chq_fecha_entrega` dice si
> llegó a entregarse. Un cheque anulado no se toca, nunca estuvo conciliado.

Como los ítems de una conciliación anulada dejan de contar, **sus movimientos vuelven a aparecer** en la
próxima conciliación de esa cuenta. No hay que hacer nada más.

**Sólo se puede anular la última conciliación vigente de la cuenta.** Si se anulara una del medio, todas
las posteriores quedarían partiendo de un saldo inicial que ya no existe. Para deshacer varias hay que ir
de la más nueva a la más vieja.

### Cancelar

Descarta lo que está en edición y vuelve al estado inerte. No escribe nada.

---

## 5. Reglas y validaciones

| # | Regla | Dónde se controla |
|---|---|---|
| 1 | Hay que elegir cuenta antes de cargar movimientos | pantalla y servlet |
| 2 | Hay que indicar la fecha hasta | servlet |
| 3 | La fecha desde no puede ser posterior a la hasta | servlet y Service |
| 4 | El período arranca el día siguiente al cierre de la última conciliación de la cuenta | Service (la pantalla ya lo deja fijo) |
| 5 | El saldo del extracto es obligatorio para grabar | pantalla y Service |
| 6 | El tipo de cambio, si se carga, tiene que ser mayor a cero | servlet |
| 7 | Cada ítem tiene que tener monto, tipo y un origen | Service |
| 8 | El saldo inicial y el final los calcula el servidor, no la pantalla | Service |
| 9 | No se puede anular dos veces la misma conciliación | Service, con `SELECT ... FOR UPDATE` |
| 10 | Sólo se anula la última vigente de la cuenta | Service |
| 11 | Los permisos son del módulo `tesoreria`: cargar y grabar piden alta, anular pide baja | `AuthorizationFilter` y pantalla |

---

## 6. Casos típicos

**Un cheque emitido en agosto que el banco todavía no cobró.** Aparece en agosto como `Ch` destildado y
se deja así. El extracto ajustado le resta ese importe al saldo del banco y la conciliación cuadra. En
setiembre vuelve a aparecer, arrastrado. Cuando el banco lo cobre, se tilda: el cheque pasa a `Cobrado`,
deja de arrastrarse y no vuelve nunca más.

**Un depósito que el banco no acreditó.** Se destilda el `Cred`. El ajuste va para el otro lado: se le
suma al saldo del banco.

**El banco muestra una comisión que el sistema no tiene.** No se carga desde acá. Se va a **Cargar otros
débitos**, se registra con fecha dentro del período, y al volver a armar la grilla aparece como un `Deb`
más, tildado. Ver §7.

**Un débito o crédito arrastrado.** Vuelve a aparecer **tildado por defecto**, porque el default es por
tipo y no recuerda cómo se dejó el mes anterior. Si el banco sigue sin mostrarlo, hay que destildarlo de
nuevo.

**La diferencia no da cero.** Se puede grabar igual. Los ítems sin tildar quedan como la explicación de
lo que hay, y el resto es una diferencia real que hay que buscar: casi siempre es un movimiento que el
banco tiene y el sistema no.

**Hay que rehacer una conciliación ya grabada.** Se busca, se anula y se hace de nuevo. Los movimientos
vuelven a estar disponibles solos.

---

## 7. Lo que la pantalla no hace, y por qué

**No da de alta ningún movimiento.** Todo lo que mueve plata entra por su propia pantalla: Orden de Pago,
Cargar otros débitos, Cargar otros créditos. Permitir el alta desde acá sería más cómodo pero duplica el
punto de carga, y un movimiento cargado dos veces aparece dos veces en la grilla.

Es la misma regla que hace funcionar el arranque de una cuenta: **una empresa que ya venía conciliando
fuera del sistema carga su saldo de arranque como un crédito**, con fecha dentro del primer período. Entra
a la grilla como un movimiento más y la primera conciliación cierra contra el extracto, sin necesidad de
ningún campo especial de saldo inicial.

**No emite el informe de resumen.** El resumen —el que tiene las planillas de cheques pendientes de cobro
y depósitos no acreditados, con pie de firmas— es un **informe de una conciliación ya grabada**, no una
pantalla de carga. Va en §H del plan de tesorería. Dos de sus cuatro planillas salen directo de lo que
esta pantalla guarda: los `Ch` sin tildar y los `Cred` sin tildar.

**No mueve ningún saldo de ninguna cuenta**, porque la cuenta bancaria no tiene saldo (§2.4).
