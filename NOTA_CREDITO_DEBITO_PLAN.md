# Plan de Implementación — Nota de Crédito y Débito de Compra

> Documento de diseño previo a la implementación. Define los cambios de base de datos,
> las reglas de negocio (efecto en **Cuenta a Pagar** y **Libro IVA Compra**) y el diseño
> de la capa Java (servlet/DAO/service) siguiendo el patrón de **Factura de Compra**.
>
> Estado: **propuesta** — pendiente de aprobación antes de tocar código.

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

## 3. Discrepancias vista ↔ BD y cambios de esquema propuestos

| # | Problema | Cambio propuesto | Prioridad |
|---|---|---|---|
| 1 | `nota_*_comp_numero` es INTEGER, pero el comprobante es `000-000-0000000` | `ALTER ... TYPE VARCHAR(20)` | Alta |
| 2 | El detalle no tiene `id_impuesto` → no se puede recalcular IVA al releer | Agregar `id_impuesto INTEGER` + FK a `impuesto` | Alta |
| 3 | El detalle no tiene descripción (para ítems sin artículo o texto libre) | Agregar `nota_*_descripcion VARCHAR` y permitir `id_articulo` NULL | Media |
| 4 | `id_articulo` NOT NULL y en PK → no admite gastos/servicios ni repetidos | Reemplazar PK por columna autoincremental `id_nota_*_det` (igual que factura) | Media |
| 5 | La vista pide **Sucursal**, las notas no tienen `id_sucursal` | Agregar `id_sucursal INTEGER` + FK, **o** quitar el campo de la vista | A decidir |
| 6 | La vista pide **Condición de compra**, las notas no la tienen | Agregar `nota_*_condicion` + `nota_*_plazo`, **o** quitar de la vista | A decidir |
| 7 | `observacion` es NOT NULL pero la vista no lo expone (solo "Motivo") | Hacer `observacion` NULL, **o** agregar el campo a la vista | Media |
| 8 | Las notas no impactan `cuenta_pagar` ni `libro_iva_compra` | Ver §4 y §5 | **Crítica** |

> **Recomendación sobre #5 y #6:** la sucursal y la condición se heredan de la factura
> referenciada; sugiero **mostrarlas como solo-lectura** tomadas de la factura y **no**
> guardarlas en la nota (evita duplicar datos). Si se decide guardarlas, agregar las columnas.

### 3.1 Script de cambios (borrador, sujeto a las decisiones de arriba)

```sql
-- #1 número como texto de comprobante
ALTER TABLE public.nota_credito_compra_cabecera
    ALTER COLUMN nota_cred_comp_numero TYPE VARCHAR(20);
ALTER TABLE public.nota_debito_compra_cabecera
    ALTER COLUMN nota_debi_comp_numero TYPE VARCHAR(20);

-- #2 impuesto por línea (para recalcular IVA 10/5/exenta)
ALTER TABLE public.nota_credito_compra_detalle ADD COLUMN id_impuesto INTEGER;
ALTER TABLE public.nota_debito_compra_detalle  ADD COLUMN id_impuesto INTEGER;
ALTER TABLE public.nota_credito_compra_detalle
    ADD CONSTRAINT impuesto_nota_credito_compra_detalle_fk
    FOREIGN KEY (id_impuesto) REFERENCES public.impuesto (id_impuesto);
ALTER TABLE public.nota_debito_compra_detalle
    ADD CONSTRAINT impuesto_nota_debito_compra_detalle_fk
    FOREIGN KEY (id_impuesto) REFERENCES public.impuesto (id_impuesto);

-- #3/#4 (opcional) descripción libre + PK autoincremental al estilo factura_compra_detalle
-- (requiere recrear la PK; evaluar impacto en datos existentes)

-- #7 observación opcional (si no se agrega a la vista)
ALTER TABLE public.nota_credito_compra_cabecera
    ALTER COLUMN nota_cred_comp_observacion DROP NOT NULL;
ALTER TABLE public.nota_debito_compra_cabecera
    ALTER COLUMN nota_debi_comp_observacion DROP NOT NULL;
```

> El manejo del Libro IVA (§5) puede requerir cambios adicionales de esquema según la opción elegida.

---

## 4. Efecto en Cuenta a Pagar

### 4.1 Principio

La nota ajusta el **saldo** (`cta_pag_saldo`) de la `cuenta_pagar` de la factura referenciada,
**dentro de la misma transacción** que persiste la nota (mismo patrón que la sincronización ya
implementada en Factura de Compra — en Java, **no** por trigger).

- **NC:** `cta_pag_saldo = cta_pag_saldo - monto_nota`
- **ND:** `cta_pag_saldo = cta_pag_saldo + monto_nota`

`cta_pag_monto` se mantiene **inmutable** (= total original de la factura, valor fiscal de
referencia). El "monto vigente" se deriva del saldo y el historial de notas.

### 4.2 Recálculo de estado

- Si tras una **NC** el `cta_pag_saldo` llega a `0` → estado `Cancelado` / `Pagado`.
- Si una **ND** vuelve a poner saldo `> 0` sobre una cuenta saldada → vuelve a `Pendiente`.

### 4.3 Reglas y validaciones (cerradas)

1. La factura referenciada **no puede estar anulada**.
2. La factura debe tener una `cuenta_pagar` asociada con saldo gestionable.
3. **NC:** `monto_nota` no puede ser mayor que el `cta_pag_saldo` vigente
   (no se permite saldo negativo — ver caso borde en §6).
4. No reducir el saldo por debajo de lo **ya pagado** (validar pagos/provisiones aplicados,
   igual que la validación previa a anular una factura).

### 4.4 Anulación de la nota (reversa idempotente)

Al anular la nota, **revertir exactamente** su ajuste:

- NC anulada → `cta_pag_saldo = cta_pag_saldo + monto_nota`
- ND anulada → `cta_pag_saldo = cta_pag_saldo - monto_nota`

La nota **no se borra**: pasa a estado `Anulado` y se preserva (trazabilidad, igual que Libro IVA
en factura). La reversa solo actúa en la transición no-anulado → `Anulado`.

---

## 5. Efecto en Libro IVA Compra

Hoy `libro_iva_compra` está atado 1:1 a la factura (`id_fact_comp_cab`). El IVA de las notas
**no se registra**, lo que deja el libro fiscalmente incompleto: una NC debe **restar** IVA/gravadas
y una ND debe **sumar**.

### 5.1 Opción recomendada — fila de Libro IVA por nota

Permitir que el libro registre también el origen "nota", agregando una referencia opcional a la nota:

```sql
ALTER TABLE public.libro_iva_compra ADD COLUMN id_nota_cred_comp_cab INTEGER;
ALTER TABLE public.libro_iva_compra ADD COLUMN id_nota_debi_comp_cab INTEGER;
ALTER TABLE public.libro_iva_compra ADD COLUMN libro_iva_comp_origen VARCHAR(10)
    DEFAULT 'FACTURA';   -- FACTURA | NOTA_CRED | NOTA_DEBI
-- FKs opcionales a las cabeceras de nota
```

- **NC:** inserta una fila con montos **negativos** (resta del total del período).
- **ND:** inserta una fila con montos **positivos**.
- Origen marcado para los informes (la factura sigue como `FACTURA`).
- Al **anular** la nota → la(s) fila(s) de libro IVA de esa nota pasan a `Anulado`
  (preservación, igual que con la factura).

> **Por qué montos con signo y filas separadas:** mantiene cada comprobante (factura, NC, ND) como
> un registro independiente y trazable en el libro, que es como se presenta fiscalmente. Evita
> "editar" la fila de la factura (que debe quedar como el comprobante original).

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
| **NC > saldo pendiente** (factura paga total/parcial) | Generaría un **saldo a favor** del comprador, que el modelo actual de `cuenta_pagar` no contempla. **Bloquear** la NC con mensaje claro. Implementar "crédito a favor" como mejora posterior (tabla/columna aparte). | A confirmar |
| **NC sobre factura de contado** (saldo 0) | Cae en el caso anterior → bloquear por ahora. | A confirmar |
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

- [ ] Confirmar decisiones pendientes de §3 (#5, #6) y §6 (casos borde).
- [ ] Aplicar `ALTER TABLE` de §3 y §5 al schema (`Base de datos Taller 3ro.sql`) y a la BD real.
- [ ] Ajustar entidades `NotaCreditoCompra(Detalle)` / `NotaDebitoCompra(Detalle)` (impuesto, número VARCHAR).
- [ ] Volver transaccionales `NotaCreditoCompraDAO` / `NotaDebitoCompraDAO`.
- [ ] Agregar ajuste de saldo + recálculo de estado en `CuentaPagarDAO`.
- [ ] Reusar `LibroIvaCompraDAO` para filas origen NOTA (insert con signo + anulación).
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
| `cuenta_pagar` | Deuda con el proveedor | Ajustar `saldo` (∓) + recalcular estado; reversa al anular |
| `libro_iva_compra` | Registro fiscal | Fila nueva con montos con signo (origen NOTA); `Anulado` al anular |

Con esto, **factura, cuenta a pagar, libro IVA y la nota** quedan sincronizados, reversibles y
trazables, alineados con el patrón ya consolidado en Factura de Compra.
