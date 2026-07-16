# Plan de Implementación — Nota de Crédito y Débito de Compra

> Documento de diseño. Define los cambios de base de datos, las reglas de negocio (efecto en
> **Cuenta a Pagar** y **Libro IVA Compra**) y el diseño de la capa Java (servlet/DAO/service)
> siguiendo el patrón de **Factura de Compra**.
>
> **Estado (actualizado):** los cambios de esquema de §3 y §5.1 **ya están aplicados** en
> `Base de datos Taller 3ro.sql`. La decisión de **`cuenta_pagar` (§4)** ya está **tomada** →
> **Enfoque 1 con neteo en la provisión** (ver §4). Queda pendiente toda la **capa Java/JSP** (§7–§9).

---

## 1. Contexto y alcance

La **Nota de Crédito** y la **Nota de Débito** de compra son comprobantes que **ajustan** una
factura de compra ya emitida por el proveedor:

- **Nota de Crédito (NC):** el proveedor reconoce un menor importe (devolución de mercadería,
  descuento, error a favor del comprador). **Disminuye** la deuda.
- **Nota de Débito (ND):** el proveedor carga un importe adicional (intereses por mora, gastos,
  diferencia a favor del proveedor). **Aumenta** la deuda.

Ambas referencian **siempre** una factura de compra (`id_fact_comp_cab` NOT NULL) y, por lo tanto,
impactan en dos lugares ya existentes del sistema:

1. **`cuenta_pagar`** — el saldo de deuda con el proveedor.
2. **`libro_iva_compra`** — el registro fiscal del IVA de compra.

El alcance de este plan cubre **Nota de Crédito/Débito de Compra**. La variante de Venta queda fuera.

---

## 2. Estado actual

### 2.1 Vista (`notaCreditoDebito.jsp`)

Prototipo estático (sin servlet, datos de ejemplo, sucursales fijas). Es una **vista combinada**:
un único formulario con un select `tipoNota` (`credito` / `debito`) que decide a qué tabla va.

Campos de cabecera: usuario (sesión), fecha, estado, sucursal, ID factura, razón social, RUC,
comprobante N° (máscara `000-000-0000000`), fecha de emisión, motivo, timbrado, fecha de
vencimiento del timbrado, condición de compra, tipo de nota.

Campos de detalle: Id. Artículo, Descripción, Cantidad, Precio de compra, Sub. Total,
Gravada 10%, IVA 10%, Gravada 5%, IVA 5%, Exenta, select de Impuesto.

> El botón "Buscar Artículo" está comentado a propósito: la NC/ND **no agrega artículos nuevos**,
> solo modifica/selecciona los que vienen de la factura referenciada.

### 2.2 Base de datos (tablas existentes)

Hay **dos tablas de cabecera y dos de detalle** (no una combinada):

**`nota_credito_compra_cabecera`** / **`nota_debito_compra_cabecera`** (estructura espejo):

| Columna | Tipo | Nota |
|---|---|---|
| `id_nota_cred/debi_comp_cab` | INTEGER (serial, PK) | |
| `nota_cred/debi_comp_numero` | **INTEGER** | ⚠️ debería ser VARCHAR (ver §3) |
| `nota_cred/debi_comp_timbrado` | INTEGER NOT NULL | |
| `nota_cred/debi_comp_fecha_venci_timb` | DATE | |
| `nota_cred/debi_comp_fecha_emision` | DATE | |
| `nota_cred/debi_comp_fecha_carga` | DATE | |
| `nota_cred/debi_comp_estado` | VARCHAR(100) NOT NULL | |
| `nota_cred/debi_comp_observacion` | VARCHAR(255) **NOT NULL** | ⚠️ la vista no tiene este campo |
| `id_usuario` | INTEGER NOT NULL | FK usuario |
| `id_proveedor` | INTEGER NOT NULL | FK proveedor |
| `id_fact_comp_cab` | INTEGER NOT NULL | **FK factura referenciada** |
| `nota_cred_motivo` / `nota_debito_motivo` | VARCHAR(255) | |

**`nota_credito_compra_detalle`** / **`nota_debito_compra_detalle`**:

| Columna | Tipo | Nota |
|---|---|---|
| `id_articulo` | INTEGER **NOT NULL** | parte de PK ⚠️ no admite ítems sin artículo |
| `id_nota_*_comp_cab` | INTEGER NOT NULL | parte de PK |
| `nota_*_comp_cantidad` | INTEGER | |
| `nota_*_monto` | INTEGER | |

PK compuesta `(id_articulo, id_nota_*_comp_cab)`.

### 2.3 Capa Java existente

Según la documentación del proyecto, `NotaCreditoCompra` / `NotaDebitoCompra` ya cuentan con
**Modelo + DAO + Service**. Falta **Servlet** y conectar la **vista**.

### 2.4 Relaciones actuales

```
proveedor ─┐
usuario ───┤
           ▼
factura_compra_cabecera ──< factura_compra_detalle
   │  ▲
   │  └──── libro_iva_compra        (id_fact_comp_cab, FK — 1:1 con la factura)
   │  └──── cuenta_pagar            (id_fact_comp_cab, FK — deuda de la factura)
   │
   ├──< nota_credito_compra_cabecera ──< nota_credito_compra_detalle
   └──< nota_debito_compra_cabecera  ──< nota_debito_compra_detalle
```

`cuenta_pagar` y `libro_iva_compra` **solo** referencian la factura. Hoy **no existe vínculo**
entre las notas y estas dos tablas: ese es el centro de este plan.

---

## 3. Discrepancias vista ↔ BD y cambios de esquema

> ✅ **Aplicado en `Base de datos Taller 3ro.sql`** (los puntos #1–#4 y #7). Los #5 y #6 se
> resolvieron **no agregando columnas** (se heredan de la factura). El #8 deriva a §4/§5; el #9
> (stock, `id_deposito` en el detalle de NC) a §5.3 y está **pendiente**.

| # | Problema | Cambio | Estado en el schema |
|---|---|---|---|
| 1 | `nota_*_comp_numero` era INTEGER, pero el comprobante es `000-000-0000000` | `nota_*_comp_numero VARCHAR` | ✅ **Aplicado** |
| 2 | El detalle no tenía `id_impuesto` → no se podía recalcular IVA al releer | `id_impuesto INTEGER NOT NULL` + FK a `impuesto` | ✅ **Aplicado** (FK incluida) |
| 3 | El detalle no tenía descripción (ítems sin artículo o texto libre) | `nota_*_descripcion VARCHAR` + `id_articulo` nullable | ✅ **Aplicado** |
| 4 | `id_articulo` NOT NULL y en PK → no admitía gastos/servicios ni repetidos | PK autoincremental `id_nota_*_det` (igual que factura) | ✅ **Aplicado** |
| 5 | La vista pide **Sucursal**, las notas no tienen `id_sucursal` | **Decisión:** no se guarda → se hereda de la factura (solo-lectura) | ✅ **Resuelto** (no se agregó columna) |
| 6 | La vista pide **Condición de compra**, las notas no la tienen | **Decisión:** no se guarda → se hereda de la factura (solo-lectura) | ✅ **Resuelto** (no se agregó columna) |
| 7 | `observacion` era NOT NULL pero la vista no lo expone (solo "Motivo") | `observacion` nullable | ✅ **Aplicado** |
| 8 | Las notas no impactan `cuenta_pagar` ni `libro_iva_compra` | Ver §4 (Enfoque 1, decidido) y §5 (aplicado) | ✅ Decidido — `libro_iva` esquema listo, `cuenta_pagar` = Enfoque 1 c/ neteo |
| 9 | El detalle de NC no tiene depósito → el trigger de devolución no sabe de qué depósito restar | `id_deposito INTEGER` nullable + FK a `deposito` en `nota_credito_compra_detalle` | ⚠️ **Pendiente** (ver §5.3) |

> **Sobre #5 y #6:** confirmado que la sucursal y la condición se **heredan de la factura
> referenciada** y se muestran como solo-lectura en la vista; **no** se guardan en la nota (se
> evita duplicar datos). Por eso el schema no agregó esas columnas.

---

## 4. Efecto en Cuenta a Pagar

> ✅ **DECISIÓN TOMADA — Enfoque 1 con neteo en la provisión.**
> Se implementa el **Enfoque 1** (mutar el `cta_pag_saldo` de la `cuenta_pagar` de la factura), con
> una **flexibilización**: se **permite que el saldo quede negativo** (saldo a favor) y ese negativo
> **se resuelve en la provisión de cuenta a pagar**, no al emitir la nota. Se elige por ser el que
> **menos reestructuración de BD** implica (**cero cambios de esquema** en `cuenta_pagar`),
> reutilizando el mecanismo de provisión por proveedor que ya existe. El Enfoque 2 queda como
> alternativa **descartada** (documentada más abajo como fundamento de la decisión).

### ✅ Enfoque elegido — Enfoque 1 con neteo en la provisión

**Idea central.** La NC/ND ajusta el `cta_pag_saldo` de la `cuenta_pagar` de la factura referenciada,
dentro de la misma transacción que persiste la nota (en Java, igual que la sincronización ya
implementada en Factura de Compra). A diferencia del Enfoque 1 "puro" de más abajo, **no se bloquea**
la NC que supera el saldo: el saldo **puede quedar negativo** y ese negativo representa un **saldo a
favor** (crédito con el proveedor) que vive en la propia fila de la factura hasta consumirse.

**Dónde se subsana el negativo — en la provisión.** La `provision_cuenta_pagar` es **por proveedor**
(comentario en BD: *"primero se selecciona el proveedor y en base a este se traen sus cuentas a
pagar"*). Como la `cuenta_pagar` con saldo negativo pertenece a una factura de ese proveedor, aparece
en la misma lista. Al armar la provisión se seleccionan **varias** cuentas a pagar (varias facturas
del proveedor): la línea con saldo negativo entra en `provision_cuenta_pagar_detalle` con un
`prov_cta_pag_monto` **negativo** y **netea** contra las líneas positivas. El **neto de la provisión**
(Σ `prov_cta_pag_monto`) es lo que efectivamente se paga vía la orden de pago.

**Por qué encaja sin tocar el esquema:**
- `cta_pag_saldo` es `INTEGER NOT NULL` → **admite negativos** sin cambio de tipo ni de PK.
- `provision_cuenta_pagar` ya es **por proveedor** y `provision_cuenta_pagar_detalle` ya soporta
  **varias facturas**, cada una con su `prov_cta_pag_monto` → el neteo es natural.
- **No** se agregan columnas ni FKs a `cuenta_pagar` (a diferencia del Enfoque 2).

**Ejemplo (el mismo caso borde que antes solo resolvía el Enfoque 2):**
```
Factura F#20:  monto 1.000.000 | pagado 900.000 | saldo 100.000
NC#5 de 300.000  →  saldo F#20 = 100.000 - 300.000 = -200.000   (saldo a favor, permitido)

Provisión del proveedor X (se seleccionan sus cuentas a pagar):
  Línea   Origen del saldo        prov_cta_pag_monto
  F#20    saldo a favor (por NC)      -200.000
  F#25    factura nueva              +500.000
                                     ─────────
                       Neto a pagar:  +300.000   → la orden de pago paga 300.000
Tras liquidar:  F#20 saldo → 0   |   F#25 saldo → 0
```

**Cambios respecto del Enfoque 1 "puro":**

| Antes (Enfoque 1 puro) | Ahora (elegido) |
|---|---|
| NC > saldo → **bloquear** | NC > saldo → **permitida**, el saldo queda negativo |
| Saldo nunca < 0 | Saldo < 0 = **saldo a favor** (crédito con el proveedor) |
| El crédito no tenía dónde vivir | Vive en la fila de la factura y se **netea en la provisión** |

**Estados de `cuenta_pagar`** (solo código — `cta_pag_estado` es `VARCHAR`, sin cambio de esquema):
- `cta_pag_saldo > 0` → `Pendiente`
- `cta_pag_saldo = 0` → `Cancelado` / `Pagado`
- `cta_pag_saldo < 0` → **`Saldo a favor`** (estado nuevo)

**Reglas y validaciones:**
1. La factura referenciada **no puede estar anulada**.
2. **NC:** `saldo = saldo − monto_nota` (puede quedar < 0). **Ya no** hay tope `monto_nota <= saldo`.
3. **ND:** `saldo = saldo + monto_nota` (siempre suma, sin tope).
4. **En la provisión:** el **neto** de las líneas seleccionadas debe ser **≥ 0** (no se puede emitir
   una orden de pago por un monto negativo). Un crédito solo se aplica si en la **misma** provisión
   hay deuda suficiente del **mismo proveedor** para absorberlo.
5. El listado de "cuentas a pagar a provisionar" debe **incluir también las de saldo negativo** (hoy
   probablemente filtra `saldo > 0`): hay que **relajar ese filtro** para que el crédito sea
   seleccionable y se pueda netear.
6. Por línea del detalle: el signo de `prov_cta_pag_monto` **sigue** al signo del `saldo` y
   `|prov_cta_pag_monto| <= |saldo|` (permite aplicar el crédito **total o parcialmente**, igual que
   ya se puede provisionar una factura de forma parcial).

**Anulación de NC/ND (reversa idempotente):**
- NC anulada → `saldo = saldo + monto_nota`; ND anulada → `saldo = saldo − monto_nota`.
- **Bloquear la anulación si el crédito/débito ya fue consumido** (total o parcialmente) por una
  provisión u orden de pago — análogo a la regla existente "no anular factura con pagos aplicados".

**Limitaciones asumidas (aceptadas a cambio de no reestructurar la BD):**
- Un proveedor puede quedar con **saldo a favor "colgado"** en una factura si no tiene deuda contra
  la cual netear. Queda visible como `Saldo a favor` y se consume en una provisión **futura** del
  mismo proveedor (al entrar una nueva factura/ND). No se modela como cuenta corriente formal (eso
  era el Enfoque 2), y no hay una **tabla de aplicación** que registre "qué crédito pagó qué factura":
  la trazabilidad del neteo queda solo en el detalle de la provisión.
- El neteo es **por proveedor dentro de una provisión**: un crédito con un proveedor **no** puede
  aplicarse a facturas de **otro** proveedor (comportamiento correcto).

> Las dos secciones siguientes (Enfoque 1 "puro" y Enfoque 2) se conservan como el **análisis que
> fundamentó la decisión**. Lo que se implementa es lo descrito arriba en "Enfoque elegido".

### Enfoque 1 — Mutar el saldo de la `cuenta_pagar` de la factura *(más simple, con limitación)*

La nota ajusta el **saldo** (`cta_pag_saldo`) de la `cuenta_pagar` de la factura referenciada,
**dentro de la misma transacción** que persiste la nota (mismo patrón que la sincronización ya
implementada en Factura de Compra — en Java, **no** por trigger).

- **NC:** `cta_pag_saldo = cta_pag_saldo - monto_nota`
- **ND:** `cta_pag_saldo = cta_pag_saldo + monto_nota`

`cta_pag_monto` se mantiene **inmutable** (= total original de la factura, valor fiscal de
referencia). El "monto vigente" se deriva del saldo y el historial de notas.

**Recálculo de estado:**
- Si tras una **NC** el `cta_pag_saldo` llega a `0` → estado `Cancelado` / `Pagado`.
- Si una **ND** vuelve a poner saldo `> 0` sobre una cuenta saldada → vuelve a `Pendiente`.

**Reglas y validaciones:**
1. La factura referenciada **no puede estar anulada**.
2. La factura debe tener una `cuenta_pagar` asociada con saldo gestionable.
3. **NC:** `monto_nota` no puede ser mayor que el `cta_pag_saldo` vigente
   (no se permite saldo negativo).
4. No reducir el saldo por debajo de lo **ya pagado** (validar pagos/provisiones aplicados).

**Anulación (reversa idempotente):**
- NC anulada → `cta_pag_saldo = cta_pag_saldo + monto_nota`
- ND anulada → `cta_pag_saldo = cta_pag_saldo - monto_nota`

**Limitación principal:** una NC mayor al saldo (factura ya pagada total/parcial) generaría saldo
a favor, que este enfoque no puede representar → habría que **bloquearla** (ver §6). Devolver
mercadería *después* de pagar es un caso real y frecuente, así que esta limitación es relevante.

**Ejemplos del problema** (por qué el saldo no puede quedar negativo en este enfoque):

```
Caso normal (sin problema):
  Factura:        1.000.000
  Pagado:                 0
  Saldo:          1.000.000
  NC de:            200.000
  Saldo nuevo:      800.000   ✅ todo bien

Caso problemático (NC > saldo):
  Factura:        1.000.000
  Pagado:           900.000   (ya pagaste casi todo)
  Saldo:            100.000
  NC de:            300.000   (devolviste mercadería por 300.000)
  Saldo nuevo:     -200.000   ❌ negativo → los 200.000 son saldo a favor

Caso extremo (factura totalmente pagada):
  Saldo:                  0
  NC de:            300.000
  Saldo nuevo:     -300.000   ❌ todo el monto es saldo a favor
```

El `-200.000` significa que el proveedor ahora **te debe a vos** (o tenés crédito para futuras
compras). Eso ya no es deuda → `cuenta_pagar` (que modela deuda, indexada por factura) no tiene
dónde representarlo. De ahí la necesidad del Enfoque 2.

### Enfoque 2 — `cuenta_pagar` como mayor de cuenta corriente por proveedor *(preferido, más completo)*

En lugar de una sola fila por factura que las notas mutan, **cada comprobante es su propia línea**
en `cuenta_pagar` (factura, ND y NC). La tabla pasa de "una deuda por factura" a un **mayor de
cuenta corriente por proveedor**.

| Origen | Monto/Saldo | Significado |
|---|---|---|
| Factura | **+** positivo | deuda |
| Nota Débito | **+** positivo | deuda adicional |
| Nota Crédito | **−** negativo | **saldo a favor** (crédito con el proveedor) |

La **deuda neta con el proveedor** = suma de saldos de sus líneas no anuladas. El saldo a favor de
una NC queda vivo como línea negativa hasta consumirse contra una compra futura. **No hay que
bloquear nada**: el negativo tiene dónde vivir. Resuelve el caso borde "NC > saldo" de §6.

**Ejemplo:**
```
Proveedor X
Línea   Origen      Monto       Saldo
F#20    FACTURA     1.000.000   100.000     (ya pagaste 900.000)
NC#5    NOTA_CRED    -300.000   -300.000    (devolución por 300.000)
                                ─────────
                       Neto:    -200.000    → el proveedor te debe 200.000

Llega una compra nueva:
F#25    FACTURA       500.000   500.000
                                ─────────
                       Neto:     300.000    → pagás 300.000 (los 200.000 a favor descontaron)
```

**Cambios de esquema en `cuenta_pagar`:**
```sql
-- ya no depende solo de la factura
ALTER TABLE public.cuenta_pagar ALTER COLUMN id_fact_comp_cab DROP NOT NULL;
ALTER TABLE public.cuenta_pagar ADD COLUMN id_nota_cred_comp_cab INTEGER;
ALTER TABLE public.cuenta_pagar ADD COLUMN id_nota_debi_comp_cab INTEGER;
ALTER TABLE public.cuenta_pagar ADD COLUMN id_proveedor INTEGER;     -- para agrupar/netear por proveedor
ALTER TABLE public.cuenta_pagar ADD COLUMN cta_pag_origen VARCHAR(10) NOT NULL DEFAULT 'FACTURA';
ALTER TABLE public.cuenta_pagar ALTER COLUMN cta_pag_fecha_venci DROP NOT NULL;  -- una NC a favor no vence
-- FKs reales (nullable) a las cabeceras de nota, igual que en libro_iva_compra
```

Dos puntos clave:
1. **`id_proveedor` directo** en la tabla: hoy el proveedor se obtiene vía la factura, pero para
   netear por proveedor y aplicar el crédito de una NC a *otra* factura conviene tenerlo directo.
2. **La PK actual es compuesta** `(id_cta_pagar, id_fact_comp_cab)`. Si `id_fact_comp_cab` pasa a
   nullable, esa PK no sirve → dejar **`id_cta_pagar` como PK única** (ya es serial).

**Sub-decisión (también sin decidir) — cómo se consume el saldo a favor:**

- **Opción A — Neteo implícito.** Nunca se tocan las líneas; el "a pagar del proveedor" es siempre
  la suma. Simple, pero **no se sabe qué crédito pagó qué factura** (la NC queda siempre con su
  saldo original y no se ve que ya se usó).
- **Opción B — Aplicación explícita** *(recomendada por trazabilidad).* Al aplicar el crédito de
  una NC a una factura, se registra esa aplicación: la línea de la NC reduce su saldo hacia 0 y la
  factura reduce el suyo. Requiere una **tabla de aplicación** (`aplicacion_credito`:
  `id_cta_pagar_credito`, `id_cta_pagar_debito`, `monto`, `fecha`). Cada movimiento queda auditado.

  **Ejemplo paso a paso (Opción B):**
  ```
  NC#5 nace con saldo -300.000   (crédito a favor disponible)

    → aplica 100.000 a F#20   →  F#20 saldo 0          | NC#5 saldo -200.000
    → aplica 200.000 a F#25   →  F#25 saldo 300.000     | NC#5 saldo 0 (estado 'Aplicado')

  Cada aplicación queda registrada en aplicacion_credito (qué crédito pagó qué factura y cuánto).
  ```
  Con la Opción A esos saldos nunca cambian (la NC#5 quedaría siempre en -300.000); el neteo es
  implícito y no se ve qué factura consumió el crédito.

**Efectos colaterales a revisar (si se elige el Enfoque 2):**
- `orden_pago_detalle`, `provision_cuenta_pagar_detalle`, `fondo_fijo_rendicion_detalle`
  referencian `(id_cta_pagar, id_fact_comp_cab)`. Al cambiar la PK a solo `id_cta_pagar`, esas FKs
  se simplifican y hay que ajustarlas. Los pagos aplican a líneas de **deuda** (factura/ND), nunca
  a una NC (crédito).
- `FacturaCompraDAO` (sincronización existente): al crear la `cuenta_pagar` de una factura debe
  setear `id_proveedor`, `cta_pag_origen='FACTURA'` y signo positivo. Cambio menor.
- **Estados nuevos** para la línea de NC: ej. `Disponible` / `Aplicado` (cuando su saldo llega a
  0), distintos de los estados de deuda (`Pendiente` / `Cancelado`).

**Consistencia:** el Enfoque 2 deja `cuenta_pagar` y `libro_iva_compra` con el **mismo patrón**
("línea por comprobante con `origen` + FKs de nota"), lo cual es coherente.

### Estado de la decisión

| Decisión | Resultado | Estado |
|---|---|---|
| Enfoque de `cuenta_pagar` | **Enfoque 1** — mutar el saldo de la factura, permitiendo saldo negativo | ✅ **Decidido** |
| Consumo del saldo a favor | **Neteo en la provisión** de cuenta a pagar (líneas +/− del mismo proveedor) | ✅ **Decidido** |

> Decisión cerrada: se implementa el **Enfoque 1 con neteo en la provisión** (§4, "Enfoque elegido").
> Se prioriza **mínima reestructuración de BD** (cero cambios de esquema en `cuenta_pagar`) sobre la
> trazabilidad formal de cuenta corriente que ofrecía el Enfoque 2. El Enfoque 2 queda documentado
> como alternativa descartada.

---

## 5. Efecto en Libro IVA Compra

> ✅ **Esquema aplicado en `Base de datos Taller 3ro.sql`.** `libro_iva_compra` ya tiene las
> columnas `id_nota_cred_comp_cab`, `id_nota_debi_comp_cab` y `libro_iva_comp_origen`, con
> `id_fact_comp_cab` nullable, PK simple (`id_libro_iva_compra`) y las **FKs reales** a ambas
> cabeceras de nota. Falta solo la **lógica Java** que inserta/anula estas filas (§8).
>
> Nota: `libro_iva_comp_estado` quedó como `VARCHAR` **sin** `NOT NULL DEFAULT 'Activo'` (decisión
> tomada) → el estado debe setearse **explícitamente desde el código** al insertar, para no dejarlo
> en NULL.

Originalmente `libro_iva_compra` estaba atado 1:1 a la factura (`id_fact_comp_cab`) y el IVA de las
notas **no se registraba**, dejando el libro fiscalmente incompleto: una NC debe **restar**
IVA/gravadas y una ND debe **sumar**. El cambio de esquema descrito abajo resuelve eso.

### 5.1 Solución aplicada — fila de Libro IVA por nota

El libro registra también las notas: cada fila lleva una referencia a la nota que la generó (vía
**FK real**, no columna suelta) y un discriminador de origen. **DDL ya aplicado** (se conserva como
referencia):

```sql
ALTER TABLE public.libro_iva_compra ADD COLUMN id_nota_cred_comp_cab INTEGER;
ALTER TABLE public.libro_iva_compra ADD COLUMN id_nota_debi_comp_cab INTEGER;
ALTER TABLE public.libro_iva_compra ADD COLUMN libro_iva_comp_origen VARCHAR(10)
    NOT NULL DEFAULT 'FACTURA';   -- FACTURA | NOTA_CRED | NOTA_DEBI

-- FKs reales (nullable): una fila de factura las deja en NULL;
-- una de NC setea solo id_nota_cred; una de ND solo id_nota_debi.
ALTER TABLE public.libro_iva_compra
    ADD CONSTRAINT nota_credito_compra_cabecera_libro_iva_compra_fk
    FOREIGN KEY (id_nota_cred_comp_cab)
    REFERENCES public.nota_credito_compra_cabecera (id_nota_cred_comp_cab);
ALTER TABLE public.libro_iva_compra
    ADD CONSTRAINT nota_debito_compra_cabecera_libro_iva_compra_fk
    FOREIGN KEY (id_nota_debi_comp_cab)
    REFERENCES public.nota_debito_compra_cabecera (id_nota_debi_comp_cab);
```

**Función de cada columna:**

- `libro_iva_comp_origen` — discriminador del tipo de comprobante (`FACTURA` / `NOTA_CRED` /
  `NOTA_DEBI`). Sirve para **reportar, filtrar y agrupar** el libro por tipo sin lógica adicional,
  y deja el tipo declarado de forma explícita (no inferido del signo del monto). Es una
  desnormalización por conveniencia: se podría derivar de cuál FK está seteado, pero se prefiere
  tenerlo explícito para los informes.
- `id_nota_cred_comp_cab` / `id_nota_debi_comp_cab` — **FKs reales** a la cabecera de la nota.
  Son necesarias para el flujo de **anulación**: permiten encontrar las filas del libro generadas
  por una nota concreta (`WHERE id_nota_cred_comp_cab = ?`). Como una factura puede tener varias
  NC/ND, `id_fact_comp_cab` por sí solo no alcanza para identificar la nota.

**`id_fact_comp_cab` se conserva en las filas de nota:** como la nota siempre referencia una
factura, toda fila del libro (factura, NC o ND) sigue llevando el `id_fact_comp_cab` de esa
factura. Esto permite agrupar **toda la historia fiscal de una factura** (factura + sus notas) por
`id_fact_comp_cab`, y el `origen` distingue cada fila dentro de ese grupo.

Comportamiento:

- **NC:** inserta una fila con montos **negativos** (resta del total del período).
- **ND:** inserta una fila con montos **positivos**.
- Origen marcado para los informes (la factura sigue como `FACTURA`).
- Al **anular** la nota → la(s) fila(s) de libro IVA de esa nota pasan a `Anulado`
  (preservación, igual que con la factura).

> **Por qué montos con signo y filas separadas:** mantiene cada comprobante (factura, NC, ND) como
> un registro independiente y trazable en el libro, que es como se presenta fiscalmente. Evita
> "editar" la fila de la factura (que debe quedar como el comprobante original).

> **Nota de diseño:** es una asociación polimórfica modelada con dos columnas FK nullable. La
> alternativa de una sola columna genérica `id_comprobante` **no permitiría FK** (apuntaría a
> tablas distintas) y perdería integridad referencial, por eso se descarta.

### 5.2 Cálculo del IVA de la nota

Idéntico al de la factura (un único punto de cálculo, deduplicado):

- Subtotal por línea = `cantidad * nota_*_monto`, donde `monto` es el **importe unitario** de la línea:
  - **Con artículo** (devolución): `cantidad` = unidades devueltas, `monto` = precio unitario.
  - **Sin artículo** (NC financiera = descuento; ND = recargo/tarifa): `cantidad` = 1, `monto` = el
    importe libre. Igual que las líneas de gasto/fondo fijo de la factura. La línea sin artículo/
    depósito no mueve stock (el trigger de §5.3 la ignora).
- IVA 10% = `subtotal / 11`; Gravada 10% = `subtotal - iva10`.
- IVA 5% = `subtotal / 21`; Gravada 5% = `subtotal - iva5`.
- Exenta = `subtotal` (si el impuesto es exento).

Por eso es **imprescindible** el cambio #2 (impuesto por línea en el detalle de la nota).

---

## 5.3 Efecto en Stock — trigger al persistir la nota (decisión tomada)

> ✅ **Decisión:** el stock de una **devolución** se descuenta con un **trigger sobre
> `nota_credito_compra_detalle`** (simétrico al de factura), **no** vía el módulo `ajuste_stock`.
> Motivo: usar `ajuste_stock` exigía ~4 cambios de esquema (signo/tipo, `estado`, FK a la nota y PK
> con depósito); el trigger solo requiere **una** columna nueva (`id_deposito` en el detalle de la
> nota). Se prioriza mínima reestructuración, igual que en `cuenta_pagar` (§4).

**Mecanismo (espejo del trigger de factura):**

- `trg_nota_credito_compra_detalle_stock_ins` — AFTER INSERT en `nota_credito_compra_detalle`:
  **resta** stock (UPSERT en `stock` por `(id_deposito, id_articulo)`, `ON CONFLICT`), signo
  **opuesto** al del trigger de factura. **Ignora** las líneas **sin depósito** (o sin artículo).
- `trg_nota_credito_compra_estado_anular` — AFTER UPDATE OF estado en
  `nota_credito_compra_cabecera`: cuando la NC pasa a `Anulado`, **repone** el stock (reversa
  idempotente; solo en la transición no-anulado → Anulado).

**El discriminador es `id_deposito`, no un "tipo de NC".** Una línea con artículo **y** depósito =
devolución física → resta stock. Una línea **sin depósito** = ajuste financiero (descuento,
bonificación, error de precio) → no toca inventario. El control es **por línea**, exactamente como
mercadería vs gasto/fondo fijo en la factura. Así no hace falta un campo "tipo" en la NC.

**Cambio de esquema requerido (pendiente):**
```sql
ALTER TABLE public.nota_credito_compra_detalle ADD COLUMN id_deposito INTEGER;   -- nullable
ALTER TABLE public.nota_credito_compra_detalle
    ADD CONSTRAINT deposito_nota_credito_compra_detalle_fk
    FOREIGN KEY (id_deposito) REFERENCES public.deposito (id_deposito);
```
`id_deposito` **nullable**: las líneas financieras lo dejan en NULL. Es el espejo exacto del
`id_deposito` que ya se agregó a `factura_compra_detalle`.

**Reglas de negocio asumidas (iguales que factura):**
- Una NC anulada no se des-anula.
- Los detalles de una NC guardada son inmutables.
- Validación (Java, al guardar): **cantidad devuelta ≤ cantidad comprada** en la factura referenciada
  para ese artículo/depósito (acumulando NC previas), para no dejar stock inconsistente.

**Nota de Débito:** no mueve stock (es financiera: intereses, fletes, gastos) → **sin trigger de
stock**. Si alguna vez se necesitara una ND por mercadería, se trataría como una factura, no como ND.

> **División transaccional:** el stock lo mueve el **trigger** (en BD), mientras que `cuenta_pagar` y
> `libro_iva_compra` los mueve el **código Java** transaccional. Es la misma división que ya usa
> Factura de Compra (stock por trigger, plata/fiscal por Java).

---

## 6. Casos borde y decisiones pendientes

| Caso | Comportamiento | Estado |
|---|---|---|
| **NC > saldo pendiente** (factura paga total/parcial) | **Permitida.** `cta_pag_saldo` queda **negativo** (saldo a favor) en la fila de la factura; se **netea en la provisión** del proveedor contra otras facturas. Estado de la cuenta → `Saldo a favor`. | ✅ **Cerrado** (Enfoque 1 c/ neteo) |
| **NC sobre factura de contado** (saldo 0) | **Permitida.** Igual que arriba: el saldo queda negativo y se netea en la provisión. | ✅ **Cerrado** |
| **Neto de una provisión < 0** | **Bloquear.** El neto (Σ `prov_cta_pag_monto`) debe ser **≥ 0**: un crédito solo se aplica si hay deuda suficiente del mismo proveedor en esa provisión. | ✅ **Cerrado** |
| **Anular NC cuyo crédito ya se neteó** | **Bloquear** (crédito ya consumido en provisión/OP), análogo a "no anular factura con pagos aplicados". | ✅ **Cerrado** |
| **Proveedor con saldo a favor sin deuda** | Queda como `Saldo a favor` colgado en la factura; se consume en una provisión futura del mismo proveedor. No se aplica a otro proveedor. | ✅ **Cerrado** (limitación aceptada) |
| **ND sobre factura de contado** | Permitida: genera/aumenta saldo. Revisar si debe crear una nueva fecha de vencimiento. | A confirmar |
| **Factura anulada** | No permitir emitir NC/ND. | Cerrado |
| **Pagos ya aplicados** | La NC **sí** puede llevar el saldo por debajo de lo pagado → eso es justamente el **saldo a favor**, permitido. Lo que se valida es no **anular** una NC ya neteada (fila de arriba). | Actualizado (Enfoque 1 c/ neteo) |
| **Ítems sin artículo (gasto/servicio)** | Hoy imposible (id_articulo NOT NULL). Habilitar con cambios #3/#4 si se requiere. | A confirmar |
| **¿Trigger o Java?** | `cuenta_pagar` y `libro_iva` → **Java** transaccional (DAO/Service de la nota). **Stock → trigger** sobre `nota_credito_compra_detalle` (§5.3), espejo del de factura. | Cerrado |
| **NC financiera vs devolución** | Se distingue **por línea**: con `id_deposito` = devolución (mueve stock); sin depósito = financiera (no toca stock). Sin campo "tipo de NC". | ✅ **Cerrado** (§5.3) |
| **Cantidad devuelta > comprada** | **Bloquear**: validar devuelta ≤ comprada en la factura (acumulando NC previas), para no dejar stock inconsistente. | ✅ **Cerrado** (§5.3) |

---

## 7. Diseño de la capa Java

### 7.1 Patrón

Seguir el patrón de referencia de **Factura de Compra**:

- **Session + Token:** estado encapsulado en una clase `State` serializable, guardada en sesión
  con un token UUID (soporta múltiples pestañas, thread-safe).
- **Formulario único + JS:** un solo `<form>` con `accion` hidden que el JS cambia antes del submit.
- **DAO transaccional:** `setAutoCommit(false)` + `commit/rollback` explícito; nota + detalle +
  ajuste de `cuenta_pagar` + filas de `libro_iva_compra` en **una sola transacción**.
- **Validación de permisos** server-side (módulo `compra`) vía `AuthorizationFilter`, y botones
  condicionados con `<c:if>` en la JSP.

### 7.2 Componentes

| Componente | Acción |
|---|---|
| `NotaCreditoDebitoServlet` (nuevo) | Controlador único; enruta a NC o ND según `tipoNota`. |
| `NotaCreditoCompraDAO` / `NotaDebitoCompraDAO` (existen) | Volver transaccionales; agregar ajuste de `cuenta_pagar` y libro IVA. |
| `NotaCreditoCompraService` / `NotaDebitoCompraService` (existen) | Orquestación. |
| `LibroIvaCompraDAO` (existe) | Reusar para insertar/anular filas de origen NOTA. |
| `CuentaPagarDAO` | Método de ajuste de saldo + recálculo de estado (transaccional). |
| `notaCreditoDebito.jsp` | Conectar al servlet, reemplazar datos de ejemplo, condicionar botones. |

### 7.3 Acciones del servlet (clasificación por permiso)

- `puedeInsertar`: Nuevo, BuscarFactura (selección), ModificarArticulo (sobre líneas heredadas), Guardar
- `puedeBorrar`: Anular

### 7.4 Mapeo URL → módulo

`NotaCreditoDebitoServlet` → módulo `compra` (mismo que Factura/Pedido/Presupuesto/Orden).

---

## 8. Flujos

> Los flujos de abajo corresponden al **Enfoque 1 con neteo en la provisión** (decisión tomada, §4):
> la nota hace `UPDATE` del `cta_pag_saldo` de la factura (que **puede quedar negativo**) y el saldo
> a favor se **netea en la provisión** (§8.3). No hay cambios de esquema en `cuenta_pagar`.

### 8.1 Guardar NC/ND

```
1. Validar: factura no anulada, con cuenta_pagar asociada.
2. Sin tope de monto:
     - NC: resta al saldo (puede dejarlo negativo = saldo a favor). NO se bloquea NC > saldo.
     - ND: suma al saldo (sin tope superior).
3. BEGIN TRANSACTION
     a. INSERT nota_*_cabecera (estado 'Pendiente')
     b. INSERT nota_*_detalle (con id_impuesto por línea)
     c. INSERT libro_iva_compra (origen NOTA_CRED/NOTA_DEBI, montos con signo)
     d. UPDATE cuenta_pagar
            saldo  = saldo ∓ monto_nota            (NC resta, ND suma; admite < 0)
            estado = recalcular:
                       saldo > 0 → 'Pendiente'
                       saldo = 0 → 'Cancelado'
                       saldo < 0 → 'Saldo a favor'
   COMMIT  (rollback ante cualquier error)
4. Mensaje Toastr de éxito.
```

### 8.2 Anular NC/ND

```
1. Validar que la nota no esté ya anulada.
2. Validar que el crédito/débito de la nota NO haya sido consumido por una
   provisión u orden de pago (si ya se neteó, bloquear la anulación).
3. BEGIN TRANSACTION
     a. UPDATE nota_*_cabecera SET estado = 'Anulado'   (preservar, no borrar)
     b. UPDATE libro_iva_compra SET estado = 'Anulado'  (filas de esta nota)
     c. UPDATE cuenta_pagar
            saldo  = saldo ± monto_nota   (reversa exacta; NC suma, ND resta)
            estado = recalcular (Pendiente / Cancelado / Saldo a favor)
   COMMIT
4. Mensaje Toastr de éxito.
```

### 8.3 Provisión con neteo del saldo a favor

```
1. Elegir proveedor → traer sus cuenta_pagar con saldo ≠ 0
     (INCLUIR las de saldo negativo / 'Saldo a favor', no solo saldo > 0).
2. Seleccionar varias cuentas a pagar (facturas del proveedor):
     - líneas de deuda  → prov_cta_pag_monto > 0
     - línea(s) a favor → prov_cta_pag_monto < 0   (aplica el crédito total o parcial)
     Regla por línea: signo(prov_cta_pag_monto) = signo(saldo)  y  |monto| <= |saldo|.
3. Validar: neto = Σ prov_cta_pag_monto  >=  0   (si < 0, bloquear).
4. BEGIN TRANSACTION
     a. INSERT provision_cuenta_pagar (cabecera, por proveedor)
     b. INSERT provision_cuenta_pagar_detalle (una fila por cuenta a pagar seleccionada)
     c. (al liquidar la provisión / orden de pago) por cada línea:
            UPDATE cuenta_pagar SET saldo = saldo - prov_cta_pag_monto
              → la línea a favor sube hacia 0; las de deuda bajan hacia 0
            estado = recalcular
   COMMIT
5. La orden de pago se emite por el neto (>= 0).
```

> **Verificado en el código (2026-07-14):** hoy **ningún** DAO descuenta `cta_pag_saldo` al pagar.
> El flujo provisión → orden de pago → forma de pago **no está implementado**: `OrdenPagoDAO` solo
> hace CRUD de `orden_pago_cabecera` (no toca `cuenta_pagar` ni inserta detalle), y **no existen**
> `ProvisionCuentaPagarDAO`, `OrdenPagoDetalleDAO` ni `FormaPagoDetalleDAO` (solo los POJOs). Los
> únicos escritores de `cta_pag_saldo` son: la **creación** de la factura (`insertarCuentaPagar`,
> `saldo = monto`) y la **sincronización** al editar/anular factura (`actualizarCuentaPagar`,
> `anularPorFactura`). Por eso el descuento del saldo se **diseña desde cero** junto con la
> provisión: debe ocurrir en la **misma transacción** que inserta `provision_cuenta_pagar(_detalle)`
> (o la orden de pago, si se difiere el pago a ese paso), recalculando el estado de cada
> `cuenta_pagar`.

### 8.4 ⚠️ Conflicto que este enfoque introduce en el código existente

Al permitir que una NC deje `cta_pag_saldo < cta_pag_monto` **sin que haya habido un pago**, se rompe
una **heurística ya usada** en `FacturaCompraServlet` para detectar pagos:

```java
// FacturaCompraServlet — accionEditar (~línea 995) y accionAnular (~línea 1039)
if (cuentaPagar.getSaldo() < cuentaPagar.getMonto()) {
    // "la factura tiene pagos aplicados" → bloquea editar/anular la factura
}
```

Hoy `saldo < monto` se interpreta como *"hay pagos aplicados"*. Con este enfoque, una **NC** también
deja `saldo < monto` **sin** pago alguno → esa condición pasaría a **bloquear por error** la edición
o anulación de una factura que solo tiene una nota de crédito.

**Acción requerida al implementar:** reemplazar esa heurística por una señal explícita de pago
(p. ej. consultar `orden_pago_detalle` / `provision_cuenta_pagar_detalle` de la factura, o llevar un
campo de "monto pagado" aparte del ajuste por notas). Sin esto, NC + edición/anulación de factura
entran en conflicto.

### 8.5 Ajuste confirmado en `CuentaPagarDAO`

- `listarCuentasPagarPendientes()` hoy filtra **`WHERE cta_pag_saldo > 0`** → debe relajarse a
  `cta_pag_saldo <> 0` (excluyendo `Anulado`) para que las líneas con **saldo a favor** (negativo)
  aparezcan en la provisión y puedan netearse.
- `actualizarCuentaPagar()` ya hace `UPDATE ... cta_pag_saldo = ?` genérico → **sirve tal cual** para
  setear saldos negativos (el campo es `INTEGER`/`Long`, admite negativos). No requiere cambios de
  firma; sí un método/uso que aplique `saldo ∓ monto_nota` transaccionalmente.

---

## 9. Checklist de implementación

- [x] ~~Cambios de esquema de §3 (#1–#4, #7) y §5.1 (libro IVA)~~ ✅ aplicados en `Base de datos Taller 3ro.sql`.
- [x] ~~Decisiones §3 #5/#6~~ ✅ resueltas: sucursal y condición se heredan de la factura (no se guardan).
- [x] ~~Decidir el enfoque de `cuenta_pagar` (§4)~~ ✅ **Enfoque 1 con neteo en la provisión** (saldo negativo permitido; sin cambios de esquema en `cuenta_pagar`).
- [x] ~~Confirmar casos borde de §6~~ ✅ cerrados (ver §6).
- [x] ~~Ajustar entidades `NotaCreditoCompra(Detalle)` / `NotaDebitoCompra(Detalle)`~~ ✅ (número VARCHAR; detalle con impuesto/descripción/`id`/`monto` unitario; CC con `deposito`, DC sin).
- [x] ~~Volver transaccionales `NotaCreditoCompraDAO` / `NotaDebitoCompraDAO`~~ ✅ (detalle + `guardar/anular...Completa` en el Service, dueño de la tx, propaga excepción).
- [x] ~~`CuentaPagarDAO`: ajuste de saldo (∓ monto_nota, admite negativo) + recálculo de estado~~ ✅ `ajustarSaldoPorNota(...)`.
- [ ] Provisión: relajar `CuentaPagarDAO.listarCuentasPagarPendientes()` (hoy `WHERE cta_pag_saldo > 0`) para **incluir saldos negativos** (`<> 0`, sin `Anulado`); validar **neto ≥ 0**; permitir `prov_cta_pag_monto` negativo en el detalle.
- [ ] **Corregir la heurística de "pagos aplicados" en `FacturaCompraServlet`** (`saldo < monto`, ~líneas 995 y 1039): con NC eso ya no implica pago → cambiar por señal explícita (consultar `orden_pago_detalle`/`provision_cuenta_pagar_detalle` o un "monto pagado" separado). Ver §8.4.
- [x] ~~Reusar `LibroIvaCompraDAO` para filas origen NOTA (insert con signo + anulación)~~ ✅ `insertarLibroIvaNota` + `anularPorNotaCredito`/`anularPorNotaDebito` (filtran por FK de la nota, no por factura).
- [x] ~~**Stock (NC devolución, §5.3):** `id_deposito` + FK en `nota_credito_compra_detalle`; triggers `trg_nota_credito_compra_detalle_stock_ins` (resta) y `trg_nota_credito_compra_estado_anular` (repone), espejo de factura~~ ✅ escritos en `Base de datos Taller 3ro.sql` y `Procedimientos y Triggers para BD.sql` (falta correrlos contra la BD).
- [ ] Validar **cantidad devuelta ≤ comprada** (acumulando NC previas) al guardar la NC.
- [ ] Vista: permitir elegir **depósito por línea devuelta** (heredar de la factura); dejar NULL en líneas financieras.
- [x] ~~Crear `NotaCreditoDebitoServlet` (Session+Token, enrutado NC/ND, validación de permisos)~~ ✅ + registrado en `AuthorizationFilter` (módulo `compra`).
- [x] ~~Conectar `notaCreditoDebito.jsp`: buscar factura real, heredar líneas, calcular IVA, condicionar botones~~ ✅ (form único + JS, sucursal/condición read-only, IVA con `<fmt>`, modales reales, permisos; menú redirige por el servlet).
- [ ] Pruebas: NC parcial, NC total (saldo→0), **NC > saldo (saldo→negativo)**, **neteo en provisión (neto ≥ 0 y bloqueo de neto < 0)**, ND sobre saldada, anulación con reversa, **bloqueo de anular NC ya neteada**, factura anulada.
- [ ] Actualizar `README.md` y `DOCUMENTACION_PROYECTO.md` al completar.

---

## 10. Resumen

| Tabla | Rol en NC/ND | Acción |
|---|---|---|
| `nota_*_compra_cabecera/detalle` | Documento en sí | INSERT al guardar; estado `Anulado` al anular (preservar) |
| `factura_compra_cabecera` | Comprobante referenciado (inmutable) | Solo lectura; validar no anulada |
| `cuenta_pagar` | Deuda con el proveedor | ✅ **Enfoque 1 c/ neteo:** `UPDATE saldo` (∓ monto_nota, **admite negativo** = saldo a favor); estado `Pendiente` / `Cancelado` / `Saldo a favor`. **Sin cambios de esquema.** |
| `provision_cuenta_pagar(_detalle)` | Neteo del saldo a favor | Incluir cuentas con saldo negativo; línea con `prov_cta_pag_monto` negativo; **neto de la provisión ≥ 0** |
| `libro_iva_compra` | Registro fiscal | Esquema ✅ aplicado; falta lógica Java: fila nueva con montos con signo (origen NOTA); `Anulado` al anular |
| `stock` (vía trigger) | Inventario de la devolución | NC con línea de depósito → **resta** al persistir el detalle; **repone** al anular. NC financiera / ND → sin efecto. Requiere `id_deposito` en `nota_credito_compra_detalle` (§5.3) |

Con esto, **factura, cuenta a pagar, libro IVA y la nota** quedan sincronizados, reversibles y
trazables, alineados con el patrón ya consolidado en Factura de Compra.

> **Estado actual:** el esquema de §3 y §5.1 ya está aplicado en `Base de datos Taller 3ro.sql`;
> Sucursal/Condición se heredan de la factura (resuelto). **Decisión de `cuenta_pagar` cerrada:**
> **Enfoque 1 con neteo en la provisión** — la nota ajusta el `saldo` (puede quedar negativo = saldo
> a favor) y el crédito se netea al provisionar varias facturas del proveedor; **cero cambios de
> esquema en `cuenta_pagar`**. Lo siguiente es la **capa Java/JSP** (§7–§9).
