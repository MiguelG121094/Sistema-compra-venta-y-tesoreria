# Conciliación bancaria — análisis y plan

Documento propio del sub-módulo, como `NOTA_CREDITO_DEBITO_PLAN.md`. Cierra el requerimiento **3.10**
y es el objetivo final del módulo de Tesorería (§F de `MODULO_TESORERIA_PLAN.md`).

**Estado al 2026-09-04:** analizado, sin implementar. Las tablas y los POJOs existen y están
alineados; falta todo el resto (DAOs, Service, Servlet, JSP).

---

## 1. Qué resuelve

Conciliar es **cruzar lo que el sistema dice que pasó en una cuenta bancaria contra lo que dice el
extracto del banco**, en un período, y explicar la diferencia.

El resultado de una conciliación es una respuesta a: *"mi sistema dice que en la cuenta de Itaú tengo
X y el banco dice Y — ¿por qué?"*. La diferencia casi nunca es un error: son **partidas
conciliatorias**, movimientos registrados de un lado y todavía no del otro. El caso típico es el
cheque emitido y entregado que el proveedor no fue a cobrar: el sistema ya lo descontó, el banco
todavía no.

Es el último sub-módulo porque necesita que existan las tres fuentes de movimiento, y las tres ya
están: **órdenes de pago** (§C), **débitos** y **créditos** (§D).

---

## 2. Estructura actual

### 2.1 Tablas

```
conciliacion_bancaria                       -- cabecera: una cuenta, un período
    id_conc_bancaria            serial
    id_cuenta                   NOT NULL    -- se concilia cuenta por cuenta
    conc_bancaria_fecha_desde   NOT NULL    -- período: desde
    conc_bancaria_fecha_hasta   NOT NULL    -- período: hasta
    conc_bancaria_fecha         NOT NULL    -- cuándo se hizo la conciliación
    conc_bancaria_saldo_inicial NOT NULL    -- encadenado: el saldo final de la conciliación anterior
    conc_bancaria_saldo_final   NOT NULL    -- calculado por el sistema
    conc_banc_saldo_banco       NOT NULL    -- el del extracto, lo carga el usuario
    conc_bancaria_estado        VARCHAR(20) -- 'Vigente' / 'Anulado' — columna nueva (D1), ver §2.3

conciliacion_bancaria_detalle               -- los ítems del período
    id_conc_bancaria            NOT NULL  ┐ PK compuesta
    conc_bancaria_nro_item      NOT NULL  ┘ (no hay serial: el nro lo asigna la aplicación)
    id_creditos                 NULL      ┐
    id_debitos                  NULL      │ el ítem apunta a UNA de estas
    id_orden_pago               NULL      │
    id_forma_pago_det           NULL      ┘
    conc_bancaria_descripcion   NOT NULL
    conc_bancaria_monto         NOT NULL
    conc_bancaria_tipo          NOT NULL   -- 'Cred' / 'Deb' / 'Ch' (§6)
    conc_bancaria_conciliado    BOOLEAN NOT NULL
```

### 2.2 POJOs

`ConciliacionBancariaDetalle` **está completo y alineado** con la tabla, incluidos los cuatro enlaces.
A `ConciliacionBancaria` se le agregó `estado`, la columna nueva de D1.

### 2.3 Lo que falta

`ConciliacionBancariaDAO` (+ detalle), `ConciliacionBancariaService`, `ConciliacionBancariaServlet`,
`conciliacionBancaria.jsp`, el registro en `AuthorizationFilter` y el link del menú.

⚠️ **Requiere un ALTER en la BD** — mismo caso que `debitos_estado` / `creditos_estado`:

```sql
ALTER TABLE public.conciliacion_bancaria ADD COLUMN conc_bancaria_estado VARCHAR(20);
UPDATE public.conciliacion_bancaria SET conc_bancaria_estado = 'Vigente';
```

---

## 3. Dos cosas que hay que entender antes de escribir código

### 3.1 El comentario de la tabla describe un diseño que no es el que se implementó

`conciliacion_bancaria` dice: *"cuando se guarda una orden de pago viene y guarda tambien en la
conciliacion la orden de pago que se hizo en cheque o transferencia"*. **Eso no es lo que hace el
sistema, y no podría hacerlo:** el detalle de conciliación cuelga de una cabecera que es *por cuenta y
por período*, y cuando se genera la OP esa cabecera no existe todavía. Por eso `OrdenPagoService`
nunca escribió en conciliación, que fue una decisión consciente.

El diseño correcto es al revés: **la conciliación es una foto que se arma cuando se concilia.** Se
elige cuenta y período, y el sistema sale a buscar los movimientos. El detalle no se alimenta desde
los otros módulos, se llena en el momento.

### 3.2 La unidad que se concilia NO es la orden de pago: es la forma de pago

Una OP puede pagarse con una transferencia de la cuenta de Itaú y dos cheques de la cuenta de Ueno. En
el extracto de Itaú aparece **una** línea, no la OP entera. Por eso el detalle tiene los dos enlaces:

- `id_forma_pago_det` → **el movimiento real**: `forma_pago_detalle` tiene `id_cuenta`,
  `forma_pag_monto` y `forma_pag_fecha`, que es exactamente lo que hay que cruzar.
- `id_orden_pago` → el documento del que salió, para poder describirlo en la grilla.

Conciliar por OP daría montos que no existen en ningún extracto y rompería la cuenta apenas alguien
pague una OP con dos cuentas distintas.

---

## 4. De dónde salen los movimientos del período

Para una cuenta y un rango de fechas, el sistema tiene que traer tres cosas. Todo lo necesario ya se
graba: `OrdenPagoServlet` completa `forma_pag_fecha` con la fecha de emisión de la OP y deja
`forma_pag_estado` en `'Pendiente'`, justamente esperando a este módulo.

| Origen | Filtro | Tipo | Enlace del ítem |
|---|---|---|---|
| `forma_pago_detalle` | `id_cuenta` = la cuenta, `forma_pag_fecha` en el rango, OP no anulada | `Deb` | `id_forma_pago_det` + `id_orden_pago` |
| `debitos` | `id_cuenta` = la cuenta, `debitos_fecha` en el rango, estado `'Vigente'` | `Deb` | `id_debitos` |
| `creditos` | `id_cuenta` = la cuenta, `creditos_fecha` en el rango, estado `'Vigente'` | `Cred` | `id_creditos` |

**No hace falta convertir monedas.** La conciliación es por cuenta, y todos los movimientos de una
cuenta están en su moneda. El tipo de cambio (`forma_pag_tipo_cambio`, `debitos_tipo_cambio`) sirve
para llevar a guaraníes, que es otro problema. El ejemplo lo confirma: la primera fila es un débito
*"OC $66.000 TC 6.045"*, una compra de dólares con su cotización, y el importe de la grilla está en la
moneda de la cuenta.

⚠️ **Esta tabla queda corta:** falta el arrastre de los movimientos que quedaron sin conciliar en
períodos anteriores. Ver §6.1.

---

## 5. Los saldos: se parte del extracto y se llega al libro

**Acá tenía el cálculo al revés.** Una conciliación bancaria no suma movimientos para llegar a un
saldo: **arranca en el saldo del extracto y lo ajusta con las partidas conciliatorias hasta llegar al
saldo del libro.** Es el formato del ejemplo `resumen_conciliacion_ejemplo.jpg`:

```
SALDO SEGÚN EXTRACTO BANCARIO                                    1.478.195
  MENOS:
    Cheques emitidos no cobrados en el banco
    Notas de crédito bancarias no contabilizadas
    Depósitos no contabilizados
  MÁS:
    Depósitos no acreditados por el banco
    Cheques no contabilizados
    Otros gastos bancarios no documentados
    Débitos de tarjeta por error
    Cheques adelantados no cobrados
SALDO SEGÚN LIBRO                                                1.478.195
```

Mapeo con las columnas de la cabecera:

| Columna | Qué es |
|---|---|
| `conc_banc_saldo_banco` | **Saldo según extracto bancario** — lo carga el usuario |
| `conc_bancaria_saldo_final` | **Saldo según libro** — al que se llega ajustando |
| `conc_bancaria_saldo_inicial` | Saldo del libro al comienzo del período — **lo trae el sistema**, no el usuario (D2) |

Las partidas conciliatorias **son los ítems que quedaron sin tildar**. La conciliación cierra cuando
el saldo ajustado coincide con el del libro; lo que no se explica es una diferencia real.

> `cuenta` no tiene columna de saldo, así que no hay un "saldo del sistema" que actualizar. El saldo
> vive únicamente en las conciliaciones, encadenado de una a la siguiente.

### 5.1 El encadenado del saldo inicial y el arranque

`conc_bancaria_saldo_inicial` sale del `conc_bancaria_saldo_final` de la **última conciliación vigente
de esa cuenta**. Si no hay ninguna, arranca en cero. El campo va de sólo lectura en la pantalla.

**Cómo arranca una empresa que ya venía conciliando fuera del sistema:** el saldo con el que llega no
se carga en la conciliación, se carga como un **crédito** en la pantalla de créditos, con fecha dentro
del primer período. Así entra a la grilla como un movimiento más, tildado, y la primera conciliación
cierra contra el extracto sin ningún campo de arranque especial.

Es la misma regla que D4: **todo lo que mueve plata entra por débitos o créditos.** La conciliación no
da de alta nada.

---

## 6. Tres tipos, no dos — y el arrastre de los cheques

El comentario de `conc_bancaria_tipo` dice `'Cred'=Crédito, 'Deb'=Débito, **etc**`. Ese "etc" es el
tercer tipo, que se ve en la columna TIP del ejemplo: **`Ch` = cheque**.

El cheque es un tipo propio y no un débito más porque **se concilia distinto**:

| Tipo | Qué es | Al armar la grilla |
|---|---|---|
| `Cr` | Crédito / depósito | **Nace tildado** |
| `Db` | Débito / transferencia | **Nace tildado** |
| `Ch` | Cheque emitido | **Nace destildado** |

El motivo es de negocio: un débito o un crédito se cargan cuando ya ocurrieron en el banco, así que
están conciliados por definición. **Un cheque tiene alrededor de un mes para cobrarse**: al conciliar
a fin de mes puede no haberse presentado todavía.

### 6.1 El arrastre entre períodos

Esto es lo más importante que aportan los ejemplos y no estaba contemplado:

> Un cheque que no se tildó en agosto **tiene que volver a aparecer en la conciliación de setiembre**,
> y así hasta que se cobre o se anule.

O sea que la grilla **no es "los movimientos del período"**, es:

```
movimientos del período  +  los de períodos anteriores que quedaron sin conciliar
```

Esto se puede resolver sin columnas nuevas: un movimiento está pendiente mientras su
`forma_pag_estado` siga en `'Pendiente'` (nace así en `OrdenPagoServlet`) o, para los cheques, mientras
`chq_estado` no sea `'Cobrado'`. Al tildar y grabar, esos estados se cierran y el movimiento deja de
arrastrarse. Es exactamente para lo que esos dos campos estaban esperando.

---

## 7. La grilla, según el ejemplo

`conciliacion_ejemplo.jpg` muestra una pantalla ya en uso. Sus columnas:

| Columna | De dónde sale en nuestro esquema |
|---|---|
| **CO** | checkbox → `conc_bancaria_conciliado` |
| **EMISIÓN** | cheque: `chq_fecha_emision`; débito/crédito: su fecha |
| **FECHA** | la del movimiento — `forma_pag_fecha`, `debitos_fecha`, `creditos_fecha` |
| **NÚMERO** | cheque/transferencia: `ord_pag_numero`; débito/crédito: su nro de comprobante |
| **DETALLE** | concepto — `debitos_detalle` / `creditos_detalle`; para la OP, el proveedor |
| **BANCO / CUENTA** | de `cuenta` → entidad financiera y número |
| **NRO. DOC.** | cheque: `chq_numero`; débito/crédito: `*_nro_comprobante` |
| **TIP** | `Db` / `Cr` / `Ch` |
| **IMPORTE** | `forma_pag_monto` / `debito_monto` / `credito_monto` |

**Dos fechas y no una.** Emisión y fecha del movimiento coinciden casi siempre, pero en un cheque no:
se emite en una y se cobra en otra. Es lo que hace visible el arrastre.

Debajo, la pantalla del ejemplo tiene: buscador, rango de fechas, filtro **Mostrar** (Débitos /
Créditos / Cheques / Todos), selector de banco y cuenta, y ordenamiento por columna. Buscar y ordenar
los cubre DataTables sin código; el filtro por tipo es un `<select>` que filtra la grilla.

> Las columnas BANCO y CUENTA son redundantes para nosotros: nuestra conciliación es **por cuenta**, así
> que van en la cabecera y no en cada fila.

---

## 8. El resumen es un informe aparte

`resumen_conciliacion_ejemplo.jpg` no es otra pantalla de carga: es **el informe** de una conciliación
ya grabada, con formato contable y pie de firmas ("Hecho por" / "Revisado por"). Se genera a partir de
lo guardado, no se carga.

Además del cuadro de saldos de §5, lleva cuatro planillas de detalle:

| Planilla | Qué lista | ¿Lo tenemos? |
|---|---|---|
| **Cheques pendientes de cobro** | Cheque N°, Fecha, **Portador**, Importe | ✅ Los ítems `Ch` sin tildar. El portador es `chq_a_la_orden` |
| **Depósitos no acreditados por el banco** | Comprobante N°, Fecha, Importe | ✅ Los ítems `Cr` sin tildar |
| **Depósitos no contabilizados** | Comp. N°, Fecha, Importe | ❌ Están en el banco y no en el sistema |
| **Cheques no contabilizados** | Cheque N°, Fecha, Importe | ❌ Ídem |

**Las dos primeras salen solas de lo que ya vamos a tener.** Las dos últimas, y las líneas "no
contabilizado" del cuadro de saldos, son por definición movimientos que el banco tiene y el sistema
no. Con nuestro módulo eso se resuelve **cargándolos como débito o crédito** y volviendo a conciliar:
ahí dejan de ser partida conciliatoria. Que en el ejemplo estén todas en cero muestra justamente un
mes bien conciliado.

Esto reordena el alcance: **el informe es parte de §H (Informes)**, no de esta pantalla. Conviene
dejarlo para después de que la conciliación funcione, pero diseñar la cabecera y el detalle sabiendo
que este es el resultado que se espera.

---

## 9. Flujo propuesto de la pantalla

Patrón documento, como la Orden de Pago: **Nuevo / Buscar / Grabar / Cancelar**, con Session+Token
porque hay una grilla que se sostiene entre pedidos.

1. **Nuevo** habilita la cabecera. El usuario elige **cuenta** y **período** (desde / hasta). Al
   elegir la cuenta, el sistema trae el **saldo inicial** encadenado (§5.1), de sólo lectura.
2. Al confirmar el período, el sistema arma la grilla con los movimientos de §4 **más los pendientes
   arrastrados** (§6.1). Los `Db` y `Cr` vienen tildados; los `Ch`, destildados.
3. El usuario carga el **saldo del extracto** y ajusta los tildes contra el papel del banco: tilda los
   cheques que se cobraron y destilda lo que el banco no muestre. Si el extracto trae algo que el
   sistema no tiene, se carga en **Débitos / Créditos** y se vuelve a armar la grilla (D4).
4. La pantalla recalcula en vivo el **saldo según libro** y la **diferencia**.
5. **Grabar** guarda cabecera y detalle —los ítems sin tildar incluidos, que son la explicación de la
   diferencia— con la cabecera en `'Vigente'`, y cierra los estados de lo conciliado:
   `forma_pag_estado` → `'Conciliado'`, `chq_estado` → `'Cobrado'`.

### 9.1 Anulación (D1)

Anular **marca, no borra**: la cabecera pasa a `'Anulado'` y la conciliación queda como historia. En la
misma transacción hay que **revertir todo lo que el grabado cerró**, o los movimientos no vuelven a
arrastrarse nunca más:

| Se cerró | Vuelve a |
|---|---|
| `forma_pag_estado` = `'Conciliado'` | `'Pendiente'` |
| `chq_estado` = `'Cobrado'` | `'Entregado'` si el cheque tiene `chq_fecha_entrega`; si no, `'Emitido'` |

> El estado anterior del cheque no se guarda en ningún lado, pero se deduce: `chq_fecha_entrega` dice
> si llegó a entregarse. Un `'Anulado'` no se toca — ese cheque nunca estuvo conciliado.

**Sólo se anula la última conciliación vigente de la cuenta.** Como el saldo inicial se encadena
(§5.1), anular una del medio dejaría a todas las posteriores partiendo de un saldo que ya no existe.
La validación va en el Service, contra la fecha `hasta` de la última vigente.

---

## 10. Decisiones

**Todas cerradas al 2026-09-05.** Los ejemplos resolvieron D3, D5 y D6; las tres que quedaban las
definió Miguel:

| # | Decisión | Estado |
|---|---|---|
| **D3** | Al tildar un ítem de cheque, ¿el cheque pasa a `'Cobrado'`? | ✅ **Sí.** Es lo que hace que el cheque deje de arrastrarse al mes siguiente, y el único lugar del sistema donde ese estado tiene sentido |
| **D5** | ¿El extracto se importa o se tilda a mano? | ✅ **A mano.** La pantalla del ejemplo trabaja así, con buscador y filtro por tipo |
| **D6** | ¿Tres tipos en `conc_bancaria_tipo`? | ✅ **Sí**: `Cred` / `Deb` / `Ch`, con el tildado automático de §6 |
| **D1** | ¿La cabecera necesita un estado para anular o reabrir? | ✅ **Sí.** `conc_bancaria_estado` (`'Vigente'` / `'Anulado'`), agregada al modelo. Anular marca y revierte los estados que el grabado cerró — §9.1 |
| **D2** | ¿El saldo inicial se encadena desde la conciliación anterior o lo carga el usuario? | ✅ **Se encadena**, y el campo va de sólo lectura. La empresa que ya venía conciliando carga su saldo de arranque como un **crédito** — §5.1 |
| **D4** | ¿Se puede cargar desde acá un movimiento que el banco muestra y el sistema no? | ✅ **No.** Para eso están Débitos y Créditos; desde la conciliación no se da de alta nada. Un alta duplicado es justo lo que hace que un movimiento aparezca dos veces en la grilla |

---

## 11. Riesgos y particularidades

- **PK compuesta sin serial** en el detalle: `conc_bancaria_nro_item` lo asigna la aplicación (1..N
  dentro de la conciliación). No es un problema, pero hay que recordarlo al insertar.
- **Anular una OP o un débito después de conciliarlo.** Si el movimiento ya está en una conciliación
  vigente, anularlo la deja mintiendo. Hoy nada lo impide: `OrdenPagoService` y
  `MovimientoBancarioServlet` no miran la conciliación. Lo razonable es que la anulación falle si el
  movimiento está en un detalle de una conciliación `'Vigente'`, y que primero se anule la
  conciliación (§9.1).
- **Períodos solapados o con huecos.** Con el saldo encadenado (D2) esto pasa a ser una validación
  obligatoria, no una comodidad: el `desde` tiene que ser el día siguiente al `hasta` de la última
  conciliación vigente de la cuenta. Si no, el saldo inicial arrastra movimientos que nadie concilió.
- **El arrastre depende de los estados.** Si algo deja `forma_pag_estado` o `chq_estado` mal, un
  movimiento se arrastra para siempre o desaparece antes de tiempo. Son los dos campos a cuidar.
- **Montos `INTEGER`**: aplica lo mismo que al resto del módulo (§8 del plan de tesorería).
- **`conc_bancaria_tipo`** es texto libre, sin constraint. Definir las constantes en un solo lugar.

---

## 12. Componentes a construir

1. `ConciliacionBancariaDAO` — cabecera y detalle en el mismo DAO, como `ProvisionCuentaPagarDAO`.
   Incluye la consulta de movimientos del período **con arrastre** (§4 y §6.1) y el `saldo_final` de la
   conciliación anterior.
2. `ConciliacionBancariaService` — transaccional: guarda cabecera + detalle y cierra los estados de lo
   conciliado (`forma_pag_estado`, `chq_estado`) en una sola unidad. También `anular()`, que marca la
   cabecera y revierte esos mismos estados (§9.1), con la validación de que sea la última vigente.
3. `ConciliacionBancariaServlet` — Session+Token, calcado de `OrdenPagoServlet`.
4. `conciliacionBancaria.jsp` — cabecera, grilla con checkbox y filtro por tipo, y recuadro de saldos.
5. Registro en `AuthorizationFilter` (módulo `tesoreria`) y link en `menuLateral.jsp`.
6. *(Después, con §H)* el informe de resumen con el formato de `resumen_conciliacion_ejemplo.jpg`.

---

## 13. Ejemplos de referencia

`src/main/webapp/Images/conciliacion_ejemplo.jpg` — pantalla de carga de un sistema en uso.
`src/main/webapp/Images/resumen_conciliacion_ejemplo.jpg` — el informe que se espera como resultado.
