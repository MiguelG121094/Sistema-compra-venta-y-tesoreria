# Plan de Implementación — Nota de Crédito y Débito de Compra

> Documento de diseño. Define los cambios de base de datos, las reglas de negocio (efecto en
> **Cuenta a Pagar** y **Libro IVA Compra**) y el diseño de la capa Java (servlet/DAO/service)
> siguiendo el patrón de **Factura de Compra**.
>
> **Estado (2026-10-29):** ✅ **IMPLEMENTADO**, con un **cambio de enfoque en `cuenta_pagar`**.
> Hasta el 2026-10 la nota mutaba el saldo de la cuenta a pagar de la factura (Enfoque 1). Desde el
> 2026-10-29 **cada nota tiene su propia fila en `cuenta_pagar`** (Enfoque 2, §4): la factura queda
> intacta, la ND es una deuda más y la NC es un saldo a favor que se consume en la provisión. El
> cambio exige correr `Migracion cuenta pagar por comprobante.sql` (§4.6).
>
> El resto sigue como estaba: esquema de §3 y §5.1 aplicado, `NotaCreditoDebitoServlet`
> (Session+Token) + `notaCreditoDebito.jsp`, Services transaccionales, triggers de stock de §5.3
> corridos contra la base (2026-08-17) y validación de cantidad devuelta ≤ comprada (2026-08-13).
> Lo que resta son las **pruebas funcionales** de §9.

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

> ⚠️ Esta sección describe el punto de partida (el prototipo previo a la implementación). Para el
> estado actual, ver el recuadro del encabezado y el checklist de §9.

### 2.1 Vista (`notaCreditoDebito.jsp`)

Al escribirse este plan era un prototipo estático (sin servlet, datos de ejemplo, sucursales fijas).
Hoy está **cableada al `NotaCreditoDebitoServlet`**. Es una **vista combinada**: un único formulario
con un select `tipoNota` (`credito` / `debito`) que decide a qué tabla va.

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

`NotaCreditoCompra` / `NotaDebitoCompra` cuentan con **Modelo + DAO + Service**; el **Servlet**
(`NotaCreditoDebitoServlet`) y el cableado de la **vista** se implementaron después (2026-07-16), con
los DAOs/Services transaccionales que aplican el efecto en `cuenta_pagar` y `libro_iva_compra`.

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

> ✅ **Todo aplicado en `Base de datos Taller 3ro.sql`** (los puntos #1–#4, #7 y también el #9). Los
> #5 y #6 se resolvieron **no agregando columnas** (se heredan de la factura). El #8 deriva a §4/§5.

| # | Problema | Cambio | Estado en el schema |
|---|---|---|---|
| 1 | `nota_*_comp_numero` era INTEGER, pero el comprobante es `000-000-0000000` | `nota_*_comp_numero VARCHAR` | ✅ **Aplicado** |
| 2 | El detalle no tenía `id_impuesto` → no se podía recalcular IVA al releer | `id_impuesto INTEGER NOT NULL` + FK a `impuesto` | ✅ **Aplicado** (FK incluida) |
| 3 | El detalle no tenía descripción (ítems sin artículo o texto libre) | `nota_*_descripcion VARCHAR` + `id_articulo` nullable | ✅ **Aplicado** |
| 4 | `id_articulo` NOT NULL y en PK → no admitía gastos/servicios ni repetidos | PK autoincremental `id_nota_*_det` (igual que factura) | ✅ **Aplicado** |
| 5 | La vista pide **Sucursal**, las notas no tienen `id_sucursal` | **Decisión:** no se guarda → se hereda de la factura (solo-lectura) | ✅ **Resuelto** (no se agregó columna) |
| 6 | La vista pide **Condición de compra**, las notas no la tienen | **Decisión:** no se guarda → se hereda de la factura (solo-lectura) | ✅ **Resuelto** (no se agregó columna) |
| 7 | `observacion` era NOT NULL pero la vista no lo expone (solo "Motivo") | `observacion` nullable | ✅ **Aplicado** |
| 8 | Las notas no impactan `cuenta_pagar` ni `libro_iva_compra` | Ver §4 (Enfoque 2, fila propia por nota) y §5 (aplicado) | ✅ Implementado — `libro_iva` y `cuenta_pagar` con una fila por comprobante |
| 9 | El detalle de NC no tiene depósito → el trigger de devolución no sabe de qué depósito restar | `id_deposito INTEGER` nullable + FK a `deposito` en `nota_credito_compra_detalle` | ✅ **Aplicado** (ver §5.3). El servlet lo expone como combo por línea (se hereda de la factura y se puede cambiar) |

> **Sobre #5 y #6:** confirmado que la sucursal y la condición se **heredan de la factura
> referenciada** y se muestran como solo-lectura en la vista; **no** se guardan en la nota (se
> evita duplicar datos). Por eso el schema no agregó esas columnas.

---

## 4. Efecto en Cuenta a Pagar

> ✅ **DECISIÓN (2026-10-29) — Enfoque 2: una fila de `cuenta_pagar` por comprobante.** Reemplaza
> al Enfoque 1 (la nota mutaba el saldo de la cuenta a pagar de la factura), que se usó desde
> 2026-07 hasta 2026-10. Los motivos del cambio están en §4.1 y el Enfoque 1 queda como historia en
> §4.7.

### 4.1 Por qué se dejó el Enfoque 1

- **La nota pisaba el estado de la cuenta de la factura.** `ajustarSaldoPorNota` recalculaba el
  estado solo por el saldo, así que una factura 'En provision' o 'Rendida' volvía a
  Pendiente/Cancelado/Saldo a favor: reaparecía en el listado de la provisión (se podía provisionar
  dos veces) y la OP descontaba un importe que ya no coincidía con el saldo.
- **El saldo mezclaba tres comprobantes.** No había forma de saber qué parte era factura y qué parte
  nota, ni de cruzar la cuenta a pagar con el libro IVA, que sí lleva una fila por comprobante.
- **No se podía bloquear con precisión la anulación de una NC ya usada** (quedó como `TODO` en el
  servlet): el neteo consumía una fila compartida con la factura.

### 4.2 Modelo

Cada comprobante es su propia fila, y todas cuelgan de la factura que la nota corrige:

| `cta_pag_origen` | FK de la nota | Monto y saldo inicial | Estado inicial | `cta_pag_fecha_venci` |
|---|---|---|---|---|
| `FACTURA` | — | + total de la factura | Pendiente | la de la factura |
| `NOTA_DEBI` | `id_nota_debi_comp_cab` | + total de la ND | Pendiente | emisión de la ND |
| `NOTA_CRED` | `id_nota_cred_comp_cab` | − total de la NC | Saldo a favor | emisión de la NC |

- **`id_fact_comp_cab` sigue NOT NULL también en las filas de nota**, con la factura que la nota
  corrige. Por eso **la PK compuesta `(id_cta_pagar, id_fact_comp_cab)` no cambia** y las FKs de
  `provision_cuenta_pagar_detalle`, `orden_pago_detalle` y `fondo_fijo_rendicion_detalle` sirven tal
  cual. Es la diferencia con el Enfoque 2 como estaba planteado originalmente, que dejaba
  `id_fact_comp_cab` nullable y obligaba a rehacer la PK y esas tres FKs.
- **Sin `id_proveedor`**: el proveedor sale de la factura (el de la nota es el mismo), así que el
  listado por proveedor de la provisión sigue igual.
- **Montos con signo, igual que el libro IVA.** El `cta_pag_monto` de la fila de la nota es el
  `libro_iva_comp_total` de la fila de libro IVA de esa nota: los dos registros se cruzan por la FK
  de la nota y tienen que dar el mismo importe.
- **El vencimiento de la nota es su fecha de emisión.** La ND es exigible desde que se emite; la NC
  no vence, pero la columna es NOT NULL y la emisión es la fecha desde la que el crédito se puede
  aplicar. Las notas no llevan `cta_pag_plazo`.
- **La fila de la factura ya no la toca ninguna nota**: su monto y su saldo son los de la factura y
  solo los mueven la OP y la edición/anulación de la propia factura.

Estados: saldo > 0 → Pendiente, = 0 → Cancelado, < 0 → Saldo a favor (`calcularEstadoPorSaldo`, sin
cambios), más 'En provision', 'Rendida' y 'Anulado', que valen para cualquier fila.

### 4.3 Consumo del saldo a favor — en la provisión

Se mantiene el neteo en la provisión que ya existía: la provisión es por proveedor, acepta líneas
negativas y exige neto ≥ 0. Lo que cambia es **qué fila** se elige: la línea negativa es ahora la
fila propia de la NC.

```
Proveedor X
Fila   Origen      Monto       Saldo
F#20   FACTURA     1.000.000    100.000   (ya se pagaron 900.000)
NC#5   NOTA_CRED    -300.000   -300.000   Saldo a favor
F#25   FACTURA       500.000    500.000

Provisión:  F#20 +100.000 | NC#5 -300.000 | F#25 +500.000   → neto 300.000
OP por 300.000  →  F#20 saldo 0 | NC#5 saldo 0 (Cancelado) | F#25 saldo 0
```

- El crédito se puede aplicar **en parte** (la línea va entre el saldo y −1, regla que ya existía) y
  el resto queda en la fila de la NC para una provisión futura.
- **Trazabilidad:** la fila de la NC baja hacia 0 a medida que se aplica, y el detalle de la
  provisión dice contra qué facturas. No se agregó la tabla `aplicacion_credito` (Opción B del
  análisis original): el detalle de la provisión ya registra cada aplicación y una tabla aparte
  duplicaría ese dato. Queda como mejora si un informe necesitara "qué crédito pagó qué factura"
  línea a línea.
- La provisión **solo cambia el estado de la factura** ('Procesada' al provisionar, 'Pendiente' al
  anular) **en las líneas de origen FACTURA**: provisionar la NC de una factura no la marca como
  procesada.

### 4.4 Anulación de la nota

- Se bloquea si **su fila** ya entró al circuito: estado 'En provision' o 'Rendida', o saldo distinto
  del monto (se aplicó total o parcialmente en una OP). La validación corre en el Service, dentro de
  la transacción y con la fila bloqueada (`FOR UPDATE`), igual que la anulación de la provisión.
- Para anular una nota ya usada se va al revés del circuito: anular la OP → anular la provisión →
  anular la nota.
- Si pasa, la fila de la nota queda 'Anulado' (no se borra) y la de la factura no se toca.

### 4.5 Efectos en el código existente

| Lugar | Antes | Ahora |
|---|---|---|
| `CuentaPagarDAO.getByFactura` / `anularPorFactura` / `tienePagosAplicados` | `WHERE id_fact_comp_cab = ?` (una fila por factura) | solo la fila `cta_pag_origen = 'FACTURA'` |
| `FacturaCompraDAO.listarFacturasCompra` (columna Estado de pago) | `LEFT JOIN cuenta_pagar` | el JOIN toma solo la fila FACTURA (si no, la factura con notas se repetía en la grilla) |
| `LibroIvaCompraDAO.anularPorFactura` / `eliminarPorFactura` | alcanzaban también las filas de las notas | solo el origen FACTURA |
| `CuentaPagarDAO.ajustarSaldoPorNota` | NC/ND mutaban el saldo de la factura | **eliminado**; lo reemplazan `insertarCuentaPagarNota` y `anularPorNotaCredito/Debito` |
| Listados de provisión, OP y rendición | mostraban el número de la factura | muestran el comprobante (`NC 001-001-0000005` para una nota) con `CuentaPagar.getComprobante()` |
| `ProvisionCuentaPagarService` | marcaba 'Procesada' la factura de cada línea | solo en las líneas de origen FACTURA |
| Rendición de fondo fijo | — | sin cambios: la NC/ND de una factura de fondo fijo aparece en el listado de la rendición y descuenta (o suma) en la reposición del responsable |

### 4.6 Esquema y migración

```sql
ALTER TABLE public.cuenta_pagar ADD COLUMN cta_pag_origen VARCHAR(10) NOT NULL DEFAULT 'FACTURA';
ALTER TABLE public.cuenta_pagar ADD COLUMN id_nota_cred_comp_cab INTEGER;
ALTER TABLE public.cuenta_pagar ADD COLUMN id_nota_debi_comp_cab INTEGER;
-- + FKs a nota_credito_compra_cabecera y nota_debito_compra_cabecera
```

El `DEFAULT 'FACTURA'` deja bien a las filas existentes y al INSERT de la factura sin tocarlo.

Las notas vigentes ya habían mutado el saldo de su factura con el Enfoque 1.
`Migracion cuenta pagar por comprobante.sql` deshace eso y les crea su fila:

1. ALTER + FKs.
2. Consulta previa: notas vigentes cuya factura ya entró en una provisión no anulada. Esas se revisan
   a mano, porque el saldo de la factura incluye el neteo de la nota y separarlo no siempre deja las
   dos filas "limpias".
3. Devuelve a la fila de la factura el efecto de sus notas vigentes (`saldo − Σ total con signo`) y
   recalcula su estado si estaba Pendiente/Cancelado/Saldo a favor.
4. Inserta una fila por nota vigente, con el total de su fila de libro IVA.

Las notas anuladas no llevan fila: su efecto ya estaba revertido.

### 4.7 Enfoque 1 (descartado el 2026-10-29)

Se conserva como historia. La nota hacía `UPDATE` del `cta_pag_saldo` de la cuenta de la factura (NC
resta, ND suma; podía quedar negativo = saldo a favor) y el negativo se neteaba en la provisión. Se
eligió en 2026-07 porque no tocaba el esquema; los problemas que motivaron el cambio están en §4.1.

---

## 5. Efecto en Libro IVA Compra

> ✅ **Esquema aplicado en `Base de datos Taller 3ro.sql`.** `libro_iva_compra` ya tiene las
> columnas `id_nota_cred_comp_cab`, `id_nota_debi_comp_cab` y `libro_iva_comp_origen`, con
> `id_fact_comp_cab` nullable, PK simple (`id_libro_iva_compra`) y las **FKs reales** a ambas
> cabeceras de nota. La lógica Java que inserta y anula estas filas está hecha (§8). Desde el
> 2026-10-29 `cuenta_pagar` sigue el mismo patrón (§4.2): el total de la fila de libro IVA de una
> nota es el monto de su fila de cuenta a pagar.
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
> nota). Se prioriza el cambio de esquema más chico.

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

**Cambio de esquema — ✅ aplicado** en `Base de datos Taller 3ro.sql`:
```sql
ALTER TABLE public.nota_credito_compra_detalle ADD COLUMN id_deposito INTEGER;   -- nullable
ALTER TABLE public.nota_credito_compra_detalle
    ADD CONSTRAINT deposito_nota_credito_compra_detalle_fk
    FOREIGN KEY (id_deposito) REFERENCES public.deposito (id_deposito);
```
`id_deposito` **nullable**: las líneas financieras lo dejan en NULL. Es el espejo exacto del
`id_deposito` que ya se agregó a `factura_compra_detalle`. Las dos funciones + triggers descritos arriba
están escritos en `Procedimientos y Triggers para BD.sql`
(`fn_nota_credito_compra_detalle_stock_insert` / `fn_nota_credito_compra_estado_anular_stock`) y
se corrieron contra la base el 2026-08-17.

**Reglas de negocio asumidas (iguales que factura):**
- Una NC anulada no se des-anula.
- Los detalles de una NC guardada son inmutables.
- ✅ **IMPLEMENTADO (2026-08-13)** — Validación (Java, al guardar): **cantidad devuelta ≤ cantidad
  comprada**, acumulando las NC previas. Sin esto el trigger restaba igual y dejaba el stock en
  negativo.

  **Cómo funciona.** `NotaCreditoDebitoServlet.validarCantidadesDevueltas(...)` corre en
  `accionGuardar`, junto al resto de las validaciones y **antes** de abrir la transacción; si algo
  no entra, muestra el mensaje y vuelve a la vista con el documento intacto. El tope por artículo
  es `comprada − ya devuelta en notas activas`:
  - `FacturaCompraDetalleDAO.obtenerCantidadesCompradasPorArticulo(idFactura)` — suma las líneas
    de la factura por artículo (ignora las de gasto, que no tienen artículo).
  - `NotaCreditoCompraDAO.obtenerCantidadesDevueltasPorArticulo(idFactura)` — suma las líneas de
    las NC **no anuladas** de esa factura. Las anuladas no cuentan porque el trigger ya repuso
    esa mercadería y vuelve a estar devolvible.
  - Se acumula además dentro de la misma nota, porque el mismo artículo puede venir en dos líneas.
  - Las líneas **sin artículo** (NC financiera) no tienen tope: no mueven stock.

  Los dos métodos de Service **propagan la `SQLException`** en vez de devolver un mapa vacío: es
  el mismo criterio que los guards de §8.4 — ante un error de BD no se puede contestar "no hay
  devoluciones previas" y dejar pasar una devolución excedida.

  **Alcance de la validación** (dos límites conocidos y aceptados):
  1. El tope es **por artículo, no por artículo+depósito**. Devolver desde un depósito distinto al
     que recibió la compra pasa la validación y puede dejar negativo el stock de ese depósito. Lo
     que se garantiza es la regla de negocio "no devolver más de lo comprado".
  2. No hay bloqueo entre la consulta y el guardado: dos NC simultáneas sobre la misma factura
     podrían pasar ambas. Es la misma ventana que tienen todas las validaciones de esa pantalla;
     cerrarla requeriría mover el chequeo adentro de la transacción con `SELECT ... FOR UPDATE`.

**Nota de Débito:** no mueve stock (es financiera: intereses, fletes, gastos) → **sin trigger de
stock**. Si alguna vez se necesitara una ND por mercadería, se trataría como una factura, no como ND.

> **División transaccional:** el stock lo mueve el **trigger** (en BD), mientras que `cuenta_pagar` y
> `libro_iva_compra` los mueve el **código Java** transaccional. Es la misma división que ya usa
> Factura de Compra (stock por trigger, plata/fiscal por Java).

---

## 6. Casos borde y decisiones

| Caso | Comportamiento | Estado |
|---|---|---|
| **NC > saldo de la factura** (factura pagada total o parcialmente) | **Permitida.** La NC nace con su propia fila en 'Saldo a favor'; la fila de la factura no se toca. | ✅ Cerrado |
| **NC sobre factura de contado** | Igual: fila propia en 'Saldo a favor', se netea en una provisión del proveedor. | ✅ Cerrado |
| **NC/ND sobre una factura 'En provision' o 'Rendida'** | **Permitida.** Antes pisaba el estado de la factura; ahora la nota tiene su fila y la factura sigue en su circuito. | ✅ Cerrado (2026-10-29) |
| **Neto de una provisión < 0** | **Bloquear.** Un crédito solo se aplica si en la misma provisión hay deuda suficiente del mismo proveedor. | ✅ Cerrado |
| **Anular una NC/ND ya usada** | **Bloquear** si su fila está 'En provision'/'Rendida' o su saldo ≠ monto (§4.4). | ✅ Implementado (2026-10-29) |
| **Proveedor con saldo a favor sin deuda** | La fila de la NC queda en 'Saldo a favor' hasta una provisión futura del mismo proveedor. | ✅ Cerrado (limitación aceptada) |
| **El proveedor reembolsa la NC en plata** | No está modelado: el ingreso se carga como Crédito bancario y la fila de la NC queda en 'Saldo a favor'. Haría falta una acción que la cierre contra ese crédito. | Pendiente |
| **ND sobre factura de contado o ya pagada** | Permitida: fila propia 'Pendiente', vence en la fecha de emisión de la ND. | ✅ Cerrado |
| **Factura anulada** | No se permite emitir NC/ND. | ✅ Cerrado |
| **Ítems sin artículo (gasto/servicio)** | Habilitados por los cambios #3/#4 de §3 (cantidad 1, monto libre). | ✅ Cerrado |
| **¿Trigger o Java?** | `cuenta_pagar` y `libro_iva` → Java transaccional. Stock → trigger (§5.3). | ✅ Cerrado |
| **NC financiera vs devolución** | Por línea: con `id_deposito` = devolución (mueve stock); sin depósito = financiera. | ✅ Cerrado (§5.3) |
| **Cantidad devuelta > comprada** | Bloquear, acumulando las NC previas. | ✅ Implementado (2026-08-13) |
| **Número de nota duplicado** | No hay UNIQUE sobre (proveedor, timbrado, número): la misma nota se puede cargar dos veces. | Pendiente |

---

## 7. Diseño de la capa Java

### 7.1 Patrón

Calcado de **Factura de Compra**:

- **Session + Token:** estado en una clase `State` serializable, guardada en sesión con un token UUID.
- **Formulario único + JS:** un solo `<form>` con `accion` hidden que el JS cambia antes del submit.
- **Service transaccional:** `setAutoCommit(false)` + `commit/rollback`; nota + detalle + fila de
  `cuenta_pagar` + fila de `libro_iva_compra` en **una sola transacción**.
- **Permisos** server-side (módulo `compra`) vía `AuthorizationFilter`, y botones con `<c:if>`.

### 7.2 Componentes

| Componente | Rol |
|---|---|
| `NotaCreditoDebitoServlet` | Controlador único; enruta a NC o ND según `tipoNota`. |
| `NotaCreditoCompraService` / `NotaDebitoCompraService` | Dueños de la transacción de guardar y anular. |
| `NotaCreditoCompraDAO` / `NotaDebitoCompraDAO` | Cabecera y detalle de la nota. |
| `CuentaPagarDAO` | `insertarCuentaPagarNota` (fila de la nota) y `anularPorNotaCredito/Debito` (valida y anula su fila). |
| `LibroIvaCompraDAO` | `insertarLibroIvaNota` y `anularPorNotaCredito/Debito`. |
| `CuentaPagar` (POJO) | `origen`, `notaCredito`, `notaDebito` y `getComprobante()` para los listados. |
| `notaCreditoDebito.jsp` | Vista combinada NC/ND. |

### 7.3 Acciones del servlet (por permiso)

- `puedeInsertar`: Nuevo, CargarFactura, AgregarLinea, EditarArticulo/ActualizarArticulo, Guardar
- `puedeBorrar`: Anular

### 7.4 Mapeo URL → módulo

`NotaCreditoDebitoServlet` → módulo `compra`.

---

## 8. Flujos

### 8.1 Guardar NC/ND

```
1. Validar: factura no anulada, número, timbrado, fecha de emisión, al menos una línea;
   NC: cantidad devuelta ≤ comprada.
2. BEGIN TRANSACTION
     a. INSERT nota_*_cabecera (estado 'Pendiente')
     b. INSERT nota_*_detalle (con id_impuesto por línea; el trigger resta stock en la NC)
     c. INSERT libro_iva_compra (origen NOTA_CRED/NOTA_DEBI, montos con signo)
     d. INSERT cuenta_pagar de la nota:
          id_fact_comp_cab = factura referenciada, origen NOTA_CRED/NOTA_DEBI, FK de la nota,
          monto = saldo = total del libro IVA (NC negativo, ND positivo),
          estado = calcularEstadoPorSaldo (NC 'Saldo a favor', ND 'Pendiente'),
          vencimiento = fecha de emisión de la nota
   COMMIT  (rollback ante cualquier error)
```

### 8.2 Anular NC/ND

```
1. Validar que la nota no esté ya anulada.
2. BEGIN TRANSACTION
     a. SELECT fila cuenta_pagar de la nota FOR UPDATE
        si estado 'En provision' / 'Rendida', o saldo ≠ monto → rechazar con mensaje
     b. UPDATE cuenta_pagar SET estado = 'Anulado'     (la fila de la nota)
     c. UPDATE nota_*_cabecera SET estado = 'Anulado'  (la NC repone stock por trigger)
     d. UPDATE libro_iva_compra SET estado = 'Anulado' (filas de esta nota)
   COMMIT
```

Una nota vigente sin fila en `cuenta_pagar` (no debería haber ninguna después de correr la
migración) no se puede anular: el Service la rechaza con un mensaje.

### 8.3 Provisión con neteo del saldo a favor

```
1. Elegir proveedor → sus cuentas a pagar con saldo ≠ 0 (facturas, ND y NC; sin 'En provision',
   'Rendida', 'Anulado' ni fondo fijo).
2. Seleccionar líneas: deuda → importe > 0; NC → importe < 0 (total o parcial).
   Regla por línea: signo(importe) = signo(saldo) y |importe| ≤ |saldo|.
3. Validar neto ≥ 0.
4. BEGIN TRANSACTION
     a. INSERT provision_cuenta_pagar + detalle
     b. cada fila → 'En provision'; la factura → 'Procesada' solo si la línea es de origen FACTURA
   COMMIT
5. La OP se emite por el neto y descuenta el importe de cada fila (la de la NC sube hacia 0).
```

### 8.4 Guardas de la factura frente a las notas

- `CuentaPagarDAO.tienePagosAplicados(idFactura)` mira si la **fila de la factura** está en alguna
  provisión u OP (reemplazó a la heurística `saldo < monto` en `FacturaCompraServlet`).
- **"Anular las notas antes que la factura":** `tieneNotaActivaPorFactura` bloquea editar y anular
  la factura si tiene notas vigentes, porque anularla dejaría a la nota huérfana (su libro IVA, su
  stock y su cuenta a pagar quedarían vivos).
- Los guards propagan la `SQLException`: un error de BD no debe habilitar una operación destructiva.

---

## 9. Checklist de implementación

- [x] ~~Cambios de esquema de §3 (#1–#4, #7, #9) y §5.1 (libro IVA)~~ ✅
- [x] ~~Sucursal y condición heredadas de la factura (§3 #5/#6)~~ ✅
- [x] ~~Entidades, DAOs y Services transaccionales de NC/ND~~ ✅
- [x] ~~Libro IVA: fila por nota con signo + anulación por FK de la nota~~ ✅
- [x] ~~Triggers de stock de la NC, corridos contra la BD (2026-08-17)~~ ✅
- [x] ~~Validación cantidad devuelta ≤ comprada (2026-08-13)~~ ✅
- [x] ~~Provisión: líneas negativas y neto ≥ 0~~ ✅ (ver `MODULO_TESORERIA_PLAN.md` §B)
- [x] ~~Guards de la factura: `tienePagosAplicados` + "anular las notas antes que la factura"~~ ✅
- [x] ~~Servlet, vista y permisos~~ ✅
- [x] ~~**Enfoque 2 (2026-10-29):** columnas `cta_pag_origen` + FKs de nota en `cuenta_pagar`~~ ✅ en
      `Base de datos Taller 3ro.sql` y `Migracion cuenta pagar por comprobante.sql`.
- [x] ~~Fila propia de `cuenta_pagar` al guardar la nota y anulación con guard (§4.4)~~ ✅
- [x] ~~Consultas de la factura restringidas al origen FACTURA (`cuenta_pagar` y `libro_iva_compra`)~~ ✅
- [x] ~~Provisión: 'Procesada' solo para líneas de factura; listados con el comprobante~~ ✅
- [ ] **Correr `Migracion cuenta pagar por comprobante.sql`** (revisar antes la consulta del paso 2).
- [ ] Pruebas: NC parcial, NC > saldo, NC sobre factura en provisión, neteo en provisión (neto ≥ 0
      y bloqueo de neto < 0), aplicación parcial del crédito, ND sobre factura pagada, anulación de
      nota libre, **bloqueo de anular nota provisionada o pagada**, factura anulada.
- [ ] Pendientes de §6: reembolso de la NC en plata, número de nota duplicado.

---

## 10. Resumen

| Tabla | Rol en NC/ND | Acción |
|---|---|---|
| `nota_*_compra_cabecera/detalle` | Documento en sí | INSERT al guardar; 'Anulado' al anular |
| `factura_compra_cabecera` | Comprobante referenciado | Solo lectura; validar no anulada |
| `cuenta_pagar` | Deuda / saldo a favor | **Fila propia por nota** (origen + FK de la nota, monto con signo); la fila de la factura no se toca; 'Anulado' al anular la nota si no entró al circuito |
| `provision_cuenta_pagar(_detalle)` | Consumo del saldo a favor | La fila de la NC entra como línea negativa; neto ≥ 0 |
| `libro_iva_compra` | Registro fiscal | Fila por nota con montos con signo; 'Anulado' al anular. Su total = el monto de la fila de `cuenta_pagar` de la nota |
| `stock` (trigger) | Devolución | NC con depósito → resta; repone al anular. ND → sin efecto |

---

## 11. Relación con la conciliación bancaria

**Sin cambios.** La conciliación trabaja con formas de pago (transferencias y cheques de la OP),
débitos y créditos bancarios; ninguna de sus consultas lee `cuenta_pagar` ni las notas. Lo mismo el
saldo de las cuentas bancarias (`ConciliacionBancariaService.obtenerSaldos`).

El efecto de una NC llega al banco por un solo camino, y es el correcto: la NC baja el **neto de la
provisión**, la OP se emite por ese neto y sus formas de pago suman ese importe, así que sale menos
plata y la conciliación ve exactamente lo que salió. La ND, al revés, sube el neto.

El único caso que tocaría al banco es el reembolso en plata de una NC (§6, pendiente): ese ingreso
se registra como Crédito bancario y se concilia como cualquier crédito, pero hoy no cierra la fila
de la NC en `cuenta_pagar`.
