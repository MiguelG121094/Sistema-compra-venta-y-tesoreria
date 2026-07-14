# Plan de Implementación — Nota de Crédito y Débito de Compra

> Documento de diseño. Define los cambios de base de datos, las reglas de negocio (efecto en
> **Cuenta a Pagar** y **Libro IVA Compra**) y el diseño de la capa Java (servlet/DAO/service)
> siguiendo el patrón de **Factura de Compra**.
>
> **Estado (actualizado):** los cambios de esquema de §3 y §5.1 **ya están aplicados** en
> `Base de datos Taller 3ro.sql`. Queda pendiente la decisión de **`cuenta_pagar` (§4)** y toda la
> **capa Java/JSP** (§7–§9).

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
> resolvieron **no agregando columnas** (se heredan de la factura). Solo #8 deriva a §4/§5.

| # | Problema | Cambio | Estado en el schema |
|---|---|---|---|
| 1 | `nota_*_comp_numero` era INTEGER, pero el comprobante es `000-000-0000000` | `nota_*_comp_numero VARCHAR` | ✅ **Aplicado** |
| 2 | El detalle no tenía `id_impuesto` → no se podía recalcular IVA al releer | `id_impuesto INTEGER NOT NULL` + FK a `impuesto` | ✅ **Aplicado** (FK incluida) |
| 3 | El detalle no tenía descripción (ítems sin artículo o texto libre) | `nota_*_descripcion VARCHAR` + `id_articulo` nullable | ✅ **Aplicado** |
| 4 | `id_articulo` NOT NULL y en PK → no admitía gastos/servicios ni repetidos | PK autoincremental `id_nota_*_det` (igual que factura) | ✅ **Aplicado** |
| 5 | La vista pide **Sucursal**, las notas no tienen `id_sucursal` | **Decisión:** no se guarda → se hereda de la factura (solo-lectura) | ✅ **Resuelto** (no se agregó columna) |
| 6 | La vista pide **Condición de compra**, las notas no la tienen | **Decisión:** no se guarda → se hereda de la factura (solo-lectura) | ✅ **Resuelto** (no se agregó columna) |
| 7 | `observacion` era NOT NULL pero la vista no lo expone (solo "Motivo") | `observacion` nullable | ✅ **Aplicado** |
| 8 | Las notas no impactan `cuenta_pagar` ni `libro_iva_compra` | Ver §4 (pendiente) y §5 (aplicado) | ⚠️ Parcial — `libro_iva` listo, `cuenta_pagar` en §4 |

> **Sobre #5 y #6:** confirmado que la sucursal y la condición se **heredan de la factura
> referenciada** y se muestran como solo-lectura en la vista; **no** se guardan en la nota (se
> evita duplicar datos). Por eso el schema no agregó esas columnas.

---

## 4. Efecto en Cuenta a Pagar

> ⚠️ **SIN DECIDIR.** Hay **dos enfoques** sobre la mesa. Ninguno está aprobado todavía.
> El Enfoque 2 es el preferido por trazabilidad, pero implica más cambios de esquema y efectos
> colaterales. Decidir antes de implementar.

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

| Decisión | Opciones | Estado |
|---|---|---|
| Enfoque de `cuenta_pagar` | 1 (mutar saldo) vs 2 (mayor de cuenta corriente) | **Sin decidir** (preferencia: 2) |
| Consumo del saldo a favor (si Enfoque 2) | A (neteo implícito) vs B (aplicación explícita) | **Sin decidir** (preferencia: B) |

> Mientras no se decida, todo lo de arriba es propuesta. La implementación no debe arrancar hasta
> cerrar estas dos decisiones.

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

- Subtotal por línea = `cantidad * precio`.
- IVA 10% = `subtotal / 11`; Gravada 10% = `subtotal - iva10`.
- IVA 5% = `subtotal / 21`; Gravada 5% = `subtotal - iva5`.
- Exenta = `subtotal` (si el impuesto es exento).

Por eso es **imprescindible** el cambio #2 (impuesto por línea en el detalle de la nota).

---

## 6. Casos borde y decisiones pendientes

| Caso | Comportamiento propuesto | Estado |
|---|---|---|
| **NC > saldo pendiente** (factura paga total/parcial) | Genera un **saldo a favor** del comprador. Con el **Enfoque 1** (§4) hay que **bloquearla** (no tiene dónde vivir el negativo); con el **Enfoque 2** queda como línea de crédito y se consume en compras futuras. Depende del enfoque elegido en §4. | **Sin decidir** (ligado a §4) |
| **NC sobre factura de contado** (saldo 0) | Igual que el caso anterior: bloquear (Enfoque 1) o registrar crédito a favor (Enfoque 2). | **Sin decidir** (ligado a §4) |
| **ND sobre factura de contado** | Permitida: genera/aumenta saldo. Revisar si debe crear una nueva fecha de vencimiento. | A confirmar |
| **Factura anulada** | No permitir emitir NC/ND. | Cerrado |
| **Pagos ya aplicados** | No reducir saldo por debajo de lo pagado. | Cerrado |
| **Ítems sin artículo (gasto/servicio)** | Hoy imposible (id_articulo NOT NULL). Habilitar con cambios #3/#4 si se requiere. | A confirmar |
| **¿Trigger o Java?** | **Java**, transaccional, en el DAO/Service de la nota — por consistencia con la sincronización de `cuenta_pagar` ya existente en factura. | Cerrado |

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

> Los flujos de abajo están escritos para el **Enfoque 1** de §4 (mutar el saldo de la factura).
> Si se adopta el **Enfoque 2** (mayor de cuenta corriente), el paso de `cuenta_pagar` cambia de
> `UPDATE saldo` a **`INSERT` de una nueva línea** por la nota (con signo), y la anulación pasa a
> anular esa línea en vez de revertir el saldo. Ajustar cuando se cierre la decisión.

### 8.1 Guardar NC/ND

```
1. Validar: factura no anulada, con cuenta_pagar gestionable.
2. Validar monto_nota:
     - NC: monto_nota <= cta_pag_saldo  (y no por debajo de lo pagado)
     - ND: sin tope superior
3. BEGIN TRANSACTION
     a. INSERT nota_*_cabecera (estado 'Pendiente')
     b. INSERT nota_*_detalle (con id_impuesto por línea)
     c. INSERT libro_iva_compra (origen NOTA_CRED/NOTA_DEBI, montos con signo)
     d. UPDATE cuenta_pagar
            saldo  = saldo ∓ monto_nota
            estado = recalcular (Cancelado si saldo=0; Pendiente si saldo>0)
   COMMIT  (rollback ante cualquier error)
4. Mensaje Toastr de éxito.
```

### 8.2 Anular NC/ND

```
1. Validar que la nota no esté ya anulada.
2. BEGIN TRANSACTION
     a. UPDATE nota_*_cabecera SET estado = 'Anulado'   (preservar, no borrar)
     b. UPDATE libro_iva_compra SET estado = 'Anulado'  (filas de esta nota)
     c. UPDATE cuenta_pagar
            saldo  = saldo ± monto_nota   (reversa exacta)
            estado = recalcular
   COMMIT
3. Mensaje Toastr de éxito.
```

---

## 9. Checklist de implementación

- [x] ~~Cambios de esquema de §3 (#1–#4, #7) y §5.1 (libro IVA)~~ ✅ aplicados en `Base de datos Taller 3ro.sql`.
- [x] ~~Decisiones §3 #5/#6~~ ✅ resueltas: sucursal y condición se heredan de la factura (no se guardan).
- [ ] **Decidir el enfoque de `cuenta_pagar` (§4): Enfoque 1 vs 2, y si va Enfoque 2, Opción A vs B.** ← decisión grande pendiente
- [ ] Confirmar casos borde de §6 (ligados a la decisión de §4).
- [ ] Si Enfoque 2: aplicar cambios de esquema de `cuenta_pagar` (§4) y revisar FKs de pagos.
- [ ] Ajustar entidades `NotaCreditoCompra(Detalle)` / `NotaDebitoCompra(Detalle)` al esquema nuevo (impuesto, descripción, número VARCHAR, PK del detalle).
- [ ] Volver transaccionales `NotaCreditoCompraDAO` / `NotaDebitoCompraDAO`.
- [ ] `CuentaPagarDAO`: ajuste de saldo (Enfoque 1) o inserción de línea por comprobante (Enfoque 2).
- [ ] Reusar `LibroIvaCompraDAO` para filas origen NOTA (insert con signo + anulación; setear `estado` explícito).
- [ ] Crear `NotaCreditoDebitoServlet` (Session+Token, enrutado NC/ND, validación de permisos).
- [ ] Conectar `notaCreditoDebito.jsp`: buscar factura real, heredar líneas, calcular IVA, condicionar botones.
- [ ] Pruebas: NC parcial, NC total (saldo→0), ND sobre saldada, anulación con reversa, validación de tope, factura anulada.
- [ ] Actualizar `README.md` y `DOCUMENTACION_PROYECTO.md` al completar.

---

## 10. Resumen

| Tabla | Rol en NC/ND | Acción |
|---|---|---|
| `nota_*_compra_cabecera/detalle` | Documento en sí | INSERT al guardar; estado `Anulado` al anular (preservar) |
| `factura_compra_cabecera` | Comprobante referenciado (inmutable) | Solo lectura; validar no anulada |
| `cuenta_pagar` | Deuda con el proveedor | **Sin decidir (§4):** Enfoque 1 = ajustar `saldo` (∓); Enfoque 2 = línea propia por comprobante (mayor de cuenta corriente) |
| `libro_iva_compra` | Registro fiscal | Esquema ✅ aplicado; falta lógica Java: fila nueva con montos con signo (origen NOTA); `Anulado` al anular |

Con esto, **factura, cuenta a pagar, libro IVA y la nota** quedan sincronizados, reversibles y
trazables, alineados con el patrón ya consolidado en Factura de Compra.

> **Estado actual:** el esquema de §3 y §5.1 ya está aplicado en `Base de datos Taller 3ro.sql`;
> Sucursal/Condición se heredan de la factura (resuelto). **Decisiones abiertas:** (1) enfoque de
> `cuenta_pagar` — §4, Enfoque 1 vs 2; (2) consumo del saldo a favor si va Enfoque 2 — Opción A vs
> B. Preferencias actuales: Enfoque 2 + Opción B. Lo siguiente es la **capa Java/JSP** (§7–§9).
