# Plan de Implementación — Módulo de Tesorería

> Documento de diseño y planificación del **módulo de Tesorería**. Define el alcance, el flujo
> esperado, el estado actual (BD + Java), y el plan de implementación por sub-módulos.
>
> **Límite con Compras:** el módulo de Compras termina en la **factura de compra** y su **cuenta a
> pagar** (la deuda). A partir de ahí empieza **Tesorería**: el flujo del dinero para saldar esas
> deudas y conciliar contra el banco.

---

## 1. Alcance y flujo esperado

Tesorería gestiona el **flujo de dinero** de la empresa. El foco de esta etapa es el **lado de
pagos** (egresos), que sigue esta cadena:

```
(COMPRAS: hasta acá)                         (TESORERÍA: desde acá)
factura_compra ─► cuenta_pagar ─► PROVISIÓN ─► ORDEN DE PAGO ─► FORMAS DE PAGO ─► CUENTA (banco)
   (la deuda)     (saldo x fact)  (x proveedor)  (autoriza)     (cheque/transf.)   (sale la plata)
                                                                      │
                        FONDO FIJO ────────────────────────┘         ▼
             (facturas FF → rendición → provisión → OP de reposición)  DÉBITOS / CRÉDITOS del banco
                                                                      │
                                                                      ▼
                                                          CONCILIACIÓN BANCARIA
                                                     (cruza OP + débitos + créditos vs extracto)
```

**Reglas de negocio clave** (tomadas de los `COMMENT` de la BD):
- *"No se puede generar una OP sin antes haber hecho una provisión de cuenta a pagar."*
- *"En la orden de pago **no se puede pagar con efectivo**"* → las formas de pago son **cheque** o
  **transferencia** (el efectivo se maneja por Fondo Fijo).
- *"Una orden de pago se puede pagar de **varias formas**"* (cheque + transferencia).
- *"Cuando se guarda una orden de pago, se guarda también en la conciliación... restando el monto
  que se pagó."*
- **Fondo Fijo:** `FACTURA FONDO FIJO → RENDICIÓN → PROVISIÓN → ORDEN PAGO` (reposición). El comment
  marca esta opción (sin pasar por "cuenta por pagar" intermedia) como la preferida.
- **Débitos** = egresos del banco (comisiones, gastos administrativos, débitos automáticos).
  **Créditos** = ingresos al banco (depósitos, boletas), enlazados a **cobros** (lado ventas).

> El **lado de cobros** (cobro / caja / arqueo / recaudaciones → créditos) pertenece a Tesorería pero
> corresponde al ciclo de **ventas**; se documenta como **fase posterior** (§9), fuera del foco
> inmediato pedido (pagos).

### 1.1 Trazabilidad de requerimientos

Mapeo de los requerimientos del módulo contra lo que existe (revisado 2026-08-31):

| # | Requerimiento | Estado | Dónde |
|---|---|---|---|
| 3.1 | Generar provisión de cuentas a pagar | ✅ Completo y probado | §B |
| 3.2 | Generar órdenes de pago | ✅ **Completo y probado** (2026-08-17) | §C |
| 3.3 | Registrar entrega de cheques a proveedores | ✅ **Completo** (2026-08-17) | §G2 |
| 3.4 | Procesos especiales (anular OP / anular cheques) | ⚠️ Parcial: anular OP ✅ (sin probar); anular un cheque suelto ❌ | §C.1 / §G3 |
| 3.5 | Asignar fondo fijo | ❌ Pendiente | §E |
| 3.6 | Rendir fondo fijo | ❌ Pendiente | §E |
| 3.7 | Registrar reposición de fondo fijo | ❌ Pendiente | §E |
| 3.8 | Cargar débitos y créditos | ✅ **Completo** (2026-08-31) | §D |
| 3.9 | Registrar depósitos (boletas bancarias) | ✅ **Completo** (2026-08-31) — mismo circuito que 3.8 | §D |
| 3.10 | Generar conciliación bancaria | ❌ Pendiente (objetivo final) | §F |
| 3.11 | Generar informes | ❌ Pendiente — **sin planificar hasta ahora** | §H |

Los requerimientos 3.3, 3.4 (mitad), 3.9 y 3.11 **no estaban cubiertos por ninguna sección** de este
plan; por eso se agregaron §G y §H y se amplió §D.

---

## 2. Estado actual

### 2.1 Base de datos
✅ **Todas las tablas del dominio existen** en `Base de datos Taller 3ro.sql` (~35 tablas de
tesorería). Ver §4 el detalle por sub-módulo.

### 2.2 Capa Java

**✅ Ya implementado (Tesorería):**

| Sub-módulo | Estado |
|---|---|
| **Referenciales → Cuenta bancaria** (ABM) | ✅ `CuentaDAO`/`CuentaService`/`CuentaServlet`/`cuenta.jsp`, más `MonedaDAO`/`Service`, `TipoCuentaDAO`/`Service`, `EntidadFinancieraDAO`/`Service` (para los combos). Seed cargado (moneda, tipo_entidad_financiera, entidad_financiera, tipo_cuenta). Servlet limpio (sin el bug de variables de instancia). |
| **Provisión de cuenta a pagar** | ✅ **Completo**: `ProvisionCuentaPagarDAO` + `ProvisionCuentaPagarService` (transaccional), `ProvisionCuentaPagarServlet` (Session+Token), `provision.jsp` (calcado del prototipo). Reserva las cuentas (`En provision`, sin tocar el saldo) y netea el saldo a favor de NC (valida **neto ≥ 0**). |
| **`CuentaPagarDAO`** (ampliado) | `ajustarSaldoPorNota` (lógica de estado en Java, `SELECT ... FOR UPDATE`), `tienePagosAplicados`, `listarCuentasPagarPorProveedor` (saldo ≠ 0, incluye negativos + N° factura + plazo + fecha emisión), `marcarEnProvision`, `revertirProvision`, helper `calcularEstadoPorSaldo`. Además: **persistencia de `cta_pag_plazo`** corregida (antes no se guardaba) y campo `plazo` en la entidad `CuentaPagar`. |
| **Orden de pago** | ✅ **Completa y probada end-to-end** (2026-08-17): esquema + POJOs alineados, `OrdenPagoDAO`/`OrdenPagoDetalleDAO`/`FormaPagoDetalleDAO`/`ChequeDAO`/`ChequeraDAO`, `OrdenPagoService` transaccional (guard anti doble-pago + anulación con reversa total), `ordenPago.jsp` y **`OrdenPagoServlet`** (Session+Token). Ver §C. |

**Servlets de tesorería existentes:** `CuentaServlet`, `ProvisionCuentaPagarServlet`,
`OrdenPagoServlet` — los tres registrados en `AuthorizationFilter` (módulo `tesoreria`); links del menú
"Módulo Tesorería" actualizados (Cuentas Bancarias, Provisión de Cta. Pagar, Orden de Pago).

**Esquema de la OP listo (2026-07-21):** ajustes de Power Architect aplicados y verificados (seriales en
`orden_pago_detalle`/`forma_pago_detalle`, `id_cheque` movido a `forma_pago_detalle`, `id_cheque`/`id_cuenta`
quitados de la cabecera, `conciliacion_bancaria_detalle.id_forma_pago_det` nullable) + seed de
`cuenta`/`tipo_cheque`/`chequera` cargado. Ajuste posterior (2026-07-25): **la moneda salió de la
cabecera de la OP y el tipo de cambio pasó a `forma_pago_detalle`** (ver C6 en §C). Ver §C.

**Pendiente:** **probar la Orden de Pago de punta a punta** (§C), Débitos/Créditos (§D), Fondo Fijo (§E),
Conciliación (§F), y el resto de referenciales/entidades. Los POJOs del flujo OP ya están alineados y
verificados contra el esquema (ver §11).

---

## 3. Patrones a reutilizar

Todo se construye calcando lo ya consolidado en **Compras**:
- **Session + Token** en los servlets (thread-safe, multi-pestaña) — referencia `FacturaCompraServlet`.
- **DAO/Service transaccional**: el **Service es dueño de la conexión** (`setAutoCommit(false)` +
  commit/rollback), construye los DAOs con esa conexión, y estos corren sin commit propio (igual que
  `CuentaPagarDAO.ajustarSaldoPorNota`).
- **Permisos** vía `AuthorizationFilter` (módulo `tesoreria`) + `<c:if>` en la JSP.
- **UTF-8** por `web.xml` (ya configurado), Toastr, máscaras de miles.

**Clave técnica — PK compuesta:** `cuenta_pagar` tiene PK **`(id_cta_pagar, id_fact_comp_cab)`**, y
esa dupla se propaga como **FK compuesta** a `provision_cuenta_pagar_detalle`, `orden_pago_detalle` y
`fondo_fijo_rendicion_detalle`. Los DAOs de esos detalles deben manejar **las dos columnas juntas**.

---

## 4. Sub-módulos y plan de implementación

### A. Referenciales bancarios *(prerrequisito de todo el flujo)*

**Tablas:** `moneda`, `tipo_entidad_financiera`, `entidad_financiera`, `tipo_cuenta`, `cuenta`
(+ `tipo_cheque`, `tipo_tarjeta` para más adelante).

Son ABM (alta/baja/modificación) simples. **Son prerrequisito** porque cada forma de pago de una Orden
de Pago referencia `id_cuenta` (el banco de donde sale la plata, que además **define la moneda** del
pago — ver C6 en §C); y una `cuenta` referencia `entidad_financiera` + `tipo_cuenta` + `moneda`.

**A implementar por cada uno:** DAO + Service + Servlet + JSP (ABM). Se puede agrupar en una o pocas
vistas de "Referenciales de Tesorería".

**Jerarquía:** `moneda`, `tipo_entidad_financiera`, `tipo_cuenta` (catálogos planos) →
`entidad_financiera` (banco) → `cuenta` (cuenta bancaria = tipo_cuenta + entidad + moneda + número).

> ✅ **HECHO — Cuenta bancaria.** ABM completo (`CuentaServlet` + `cuenta.jsp`, visual de referencial,
> servlet estilo FacturaCompra). Combos poblados por `MonedaService` / `TipoCuentaService` /
> `EntidadFinancieraService`. Seed de moneda (Guaraníes, Dólares), tipo_entidad_financiera
> (Banco/Financiera/Cooperativa), entidad_financiera (Itaú, Ueno, Atlas, Paraguayo Japonesa, Tu
> Financiera, Medalla, San Cristóbal) y tipo_cuenta (Corriente, Ahorro). Los demás referenciales
> (tipo_cheque, tipo_tarjeta) quedan para cuando se necesiten.

---

### B. Provisión de cuenta a pagar *(primer paso del flujo — prioridad)*

**Tablas:** `provision_cuenta_pagar` (cabecera, por proveedor), `provision_cuenta_pagar_detalle`
(facturas incluidas), `cuenta_pagar`.

**Flujo:** elegir **proveedor** → traer sus `cuenta_pagar` → seleccionar **varias facturas** →
guardar cabecera + detalle (cada línea con `prov_cta_pag_monto`).

**⭐ Integración con Nota de Crédito (decisión ya tomada — ver `NOTA_CREDITO_DEBITO_PLAN.md` §4/§8.3):**
Acá se **consume el "saldo a favor"** de las NC (Enfoque 1 con neteo). Por eso la provisión debe:
- **Relajar el filtro** de "cuentas a pagar del proveedor": traer las de **saldo ≠ 0** (incluidas las
  de **saldo negativo** = saldo a favor), no solo `saldo > 0`. Hoy `CuentaPagarDAO.listarCuentasPagarPendientes()`
  filtra `cta_pag_saldo > 0` → **hay que agregar un método que incluya negativos**.
- Permitir líneas con `prov_cta_pag_monto` **negativo** (el saldo a favor).
- Validar que el **neto** de la provisión (Σ montos) sea **≥ 0** (no se puede provisionar/pagar un
  monto negativo).

**Componentes a crear:** `ProvisionCuentaPagarDAO` + `...DetalleDAO` (o detalle dentro del mismo),
`ProvisionCuentaPagarService` (transaccional), `ProvisionCuentaPagarServlet` (Session+Token), JSP.

> ✅ **DECIDIDO:** la provisión **solo agrupa/reserva** (cambia el estado de la `cuenta_pagar` a
> `En provision`, **sin tocar el saldo**); el **descuento del saldo ocurre en la Orden de Pago**.
>
> ✅ **HECHO — Provisión completa.** `ProvisionCuentaPagarDAO` (insert cabecera/detalle, listar,
> getById + detalle, anular) + `ProvisionCuentaPagarService` transaccional (`guardarProvisionCompleta`
> reserva las cuentas; `anularProvisionCompleta` revierte con `revertirProvision`) +
> `ProvisionCuentaPagarServlet` (Session+Token: Nuevo, Buscar Proveedor, Lista de Cuentas a Pagar,
> editor de importe a pagar, Generar, Buscar/Anular) + `provision.jsp` (prototipo replicado, detalle
> como DataTable, importe con separador de miles que preserva el negativo del saldo a favor).
> El **neteo de NC** funciona: el listado incluye saldos negativos y valida neto ≥ 0.
> Botón "Buscar rendición Fondo Fijo" = placeholder deshabilitado (se hace con §E).

---

### C. Orden de pago *(donde sale el dinero — ✅ IMPLEMENTADA Y PROBADA)*

**Tablas:** `orden_pago_cabecera`, `orden_pago_detalle`, `forma_pago_cabecera`, `forma_pago_detalle`,
`cheque`, `chequera`, `tipo_cheque`, `cuenta`.

**Prototipo** (`webapp/Images/Prototipo Orden de pago.png` — ⚠️ **quedó viejo**: muestra un pago único
en la cabecera, y Moneda/Tipo de cambio que ya no van ahí). Es como Factura/Provisión pero para la OP:
- **Botones:** Nuevo · Buscar Orden de pago · Buscar Provisión · Anular.
- **Cabecera:** Nro OP · Recibo Nro (`ord_pag_nro_recibo`) · Fecha · Estado · Sucursal ·
  **Provisión Nro** · **Tipo de pago** (reposición FF / otros gastos) · Razón Social.
  *(La cuenta bancaria, el/los cheque, la moneda y el tipo de cambio ya NO van en la cabecera → van
  en cada forma de pago; ver C5 y C6.)*
- **Detalle de facturas** = las de la provisión, **SOLO LECTURA** (Item, Nro factura, Importe total,
  Saldo pendiente, Importe a pagar, Plazo). Sin Editar/Eliminar (vienen fijas de la provisión).
- **Bloque de FORMAS DE PAGO** (tipo carrito, **N filas**): por cada una → Tipo (Transferencia/Cheque),
  **Monto**, **Cuenta bancaria** (de dónde sale — ella define la moneda), **Tipo de cambio**, Referencia;
  si es **Cheque** → Chequera + Tipo de cheque + Fecha de pago/vencimiento (el Nro se toma solo del rango
  de la chequera). Σ montos = total de la OP.
- **Importe Total a Pagar** + Generar / Cancelar.

**Flujo:**
1. Nuevo → **Buscar Provisión** → carga sus facturas (detalle SOLO LECTURA) + proveedor (Razón Social) + Provisión Nro.
2. Completar cabecera: Recibo, Sucursal, Tipo de pago (`ord_pag_tipo_pago` = reposición FF / otros gastos).
3. **Cargar una o varias formas de pago** (mixto: p. ej. parte transferencia + parte cheque(s), incluso
   de cuentas distintas, cada una con su tipo de cambio). Por cada línea de cheque se emite un cheque
   real de la chequera.
4. **Generar** → transacción única (los 5 pasos de abajo): crea OP + detalle + formas de pago + cheques,
   y **descuenta el `cta_pag_saldo`** de cada factura de la provisión.

**Decisiones (confirmadas — sesión 2026-07-20/21):**

| # | Decisión | Resultado |
|---|---|---|
| C1 | ¿Una OP paga toda la provisión o una factura? | ✅ **Toda la provisión** (N facturas). Resuelto con serial `id_orden_pago_det`. |
| C2 | ¿La OP inserta el movimiento en la conciliación? | ✅ **NO.** La conciliación es un **módulo periódico posterior** (§F) que jala las OPs/débitos/créditos del período. Ver ejemplo abajo. |
| C3 | Formas de pago | ✅ **VARIAS por OP** (mixtas: transferencia + cheque(s)). `forma_pago_detalle` con N filas. **⟳ Revierte la decisión previa de "una sola forma".** |
| C4 | Cheque | ✅ **Cheque REAL desde chequera** (ya no "solo referencia"). Cada línea de cheque emite un registro en `cheque`, con el **Nro dentro del rango** de la `chequera`, y se enlaza vía `forma_pago_detalle.id_cheque`. La chequera viene del **seed** (ABM de Chequera queda para §G, más adelante). |
| C5 | Cuenta bancaria | ✅ **Multi-cuenta.** La cuenta de cada pago vive en `forma_pago_detalle.id_cuenta` (se quitó de la cabecera). Una OP puede pagar desde 2 cuentas distintas. |
| C6 | Moneda y tipo de cambio *(sesión 2026-07-25)* | ✅ **Fuera de la cabecera.** La deuda (`factura_compra`/`cuenta_pagar`) **no tiene moneda** → es **Gs implícito**, así que la cabecera no necesita `id_moneda`: la moneda de cada pago la define **la cuenta bancaria** de esa forma de pago. Y como cada instrumento puede liquidarse a una cotización distinta, el **tipo de cambio pasó a `forma_pago_detalle.forma_pag_tipo_cambio`**. Se **quitaron** `id_moneda` y `ord_pag_tipo_cambio` de `orden_pago_cabecera`. ✅ El nombre `forma_pag_tipo_cambio` fue **confirmado contra Power Architect** (2026-08-17). |

**✅ Ajustes de esquema aplicados (Power Architect, 2026-07-21):**
- ✅ `orden_pago_detalle.id_orden_pago_det` **serial** (N facturas por OP). Mantiene `id_cta_pagar` + `id_fact_comp_cab` (FK compuesta) + `orden_pag_det_monto`.
- ✅ `forma_pago_detalle.id_forma_pago_det` **serial**; se **quitaron** `forma_pag_cheque` y `forma_pag_transferencia` (queda solo `forma_pag_monto` + `id_forma_pago_cab` para el tipo); se **agregó** `id_cheque` **NULLABLE** (FK → `cheque`).
- ✅ `orden_pago_cabecera`: se **quitaron** `id_cheque` y `id_cuenta` (ambos migran al detalle).
- ✅ `conciliacion_bancaria_detalle.id_forma_pago_det` → **NULLABLE** (los ítems de débito/crédito no tienen forma de pago).
- ✅ Seed cargado en `Inserts inciales.sql`: `cuenta` (Itaú/Ueno Gs, Itaú Ahorro USD), `tipo_cheque` (A la vista / Diferido), `chequera` (Itaú 1000001–1000050, Ueno 2000001–2000050) y **`forma_pago_cabecera`** (`Cheque` = id 1, `Transferencia` = id 2 — sin efectivo). ⚠️ La descripción **`'Cheque'`** se compara por texto para decidir si la línea emite un cheque real (`OrdenPagoServlet.esFormaCheque` y el `toggleCamposCheque()` del JSP, que exige exactamente `'cheque'` en minúsculas): si se renombra, hay que ajustar ambos.
- ✅ **(2026-07-25)** `orden_pago_cabecera`: se **quitaron** `id_moneda` y `ord_pag_tipo_cambio`;
  `forma_pago_detalle`: se **agregó** `forma_pag_tipo_cambio` (DOUBLE PRECISION, nullable). Ver C6.
  ✅ Nombre confirmado contra Power Architect (2026-08-17).

**Los pasos transaccionales de `guardarOrdenPagoCompleta(...)`** (todo en UNA tx — el Service es dueño de la conexión). ✅ **Implementado** (ver §C.1 para el detalle de participantes/mensajes):
```
0. Bloquear la provisión (SELECT ... FOR UPDATE) y validar que esté 'Pendiente'
     → si es null (no existe) o != 'Pendiente' (ya 'Procesada'/'Anulado')  →  ABORTA (anti doble-pago)
1. INSERT orden_pago_cabecera                → id_orden_pago (serial)
2. Por cada factura de la provisión:
     INSERT orden_pago_detalle (SOLO LECTURA, viene de la provisión)
3. Por cada forma de pago:
     - si CHEQUE: próximo nro del rango de la chequera → INSERT cheque → obtiene id_cheque
     - INSERT forma_pago_detalle (id_forma_pago_cab, monto, id_cuenta, referencia, id_cheque|NULL)
4. Por cada factura: cta_pag_saldo -= importe_pagado; recalcular estado (calcularEstadoPorSaldo)
     ('En provision' → 'Cancelado' / 'Pendiente' / 'Saldo a favor')
5. UPDATE provision_cuenta_pagar SET estado = 'Procesada'   (la consume: no se puede volver a pagar)
   → commit   (rollback si cualquier paso falla)
```

**Lógica del próximo Nro de cheque (dentro del rango de la chequera):**
```
proximoNro = COALESCE(MAX(chq_numero) de esa chequera, chequera_desde_nro - 1) + 1
validar:  chequera_desde_nro ≤ proximoNro ≤ chequera_hasta_nro
   si se pasa de hasta_nro  →  error "Chequera agotada, cargá una nueva"
```

**Validaciones al Generar:**
- **Provisión previa obligatoria** (no hay OP sin provisión — regla de BD).
- **Sin efectivo:** solo formas Cheque/Transferencia.
- **Σ `forma_pag_monto` == `ord_pag_monto`** (las formas cubren exactamente el total de la OP).
- Neto ≥ 0.

**Ejemplo — pago mixto multi-cuenta (OP #10, total 650.000):**
```
orden_pago_cabecera: id=10, monto=650.000  (sin cuenta ni cheque)
orden_pago_detalle:  factura A (500.000) + factura B (150.000)   ← de la provisión, solo lectura
forma_pago_detalle:
  fila 1 │ Transferencia │ 300.000 │ id_cuenta=Itaú │ id_cheque=NULL │ ref='TRF-889'
  fila 2 │ Cheque        │ 200.000 │ id_cuenta=Itaú │ id_cheque=55 ──┼─► cheque #1000001
  fila 3 │ Cheque        │ 150.000 │ id_cuenta=Ueno │ id_cheque=56 ──┼─► cheque #2000001
                                     Σ = 650.000 == monto OP ✓
cuenta_pagar: saldo A 500.000→0 ('Cancelado'), saldo B 150.000→0 ('Cancelado')
```

**Ejemplo — Conciliación (por qué la OP NO la inserta):**
```
1. Generás OP #10 (arriba). Queda como movimiento; el saldo de las facturas se descuenta acá.
2. A fin de mes armás la CONCILIACIÓN de Itaú (período): creás conciliacion_bancaria (cabecera).
   Ese módulo trae los movimientos a conciliacion_bancaria_detalle:
       item 1 | id_forma_pago_det=1 | tipo='Deb'  | 300.000 | (transfer de la OP)
       item 2 | id_forma_pago_det=2 | tipo='Deb'  | 200.000 | (cheque de la OP)
       item 3 | id_debitos=5        | tipo='Deb'  | 20.000  | (comisión banco, sin forma_pago_det)
       item 4 | id_creditos=8       | tipo='Cred' | 500.000 | (depósito, sin forma_pago_det)
   → cada ítem referencia SOLO una FK (por eso id_forma_pago_det/id_debitos/id_creditos son nullable).
```
`conciliacion_bancaria_detalle` depende de una `conciliacion_bancaria` (cabecera) que **no existe** al
guardar la OP → por eso la OP no inserta ahí; el módulo de Conciliación (§F) levanta los movimientos del período.
Nota: la conciliación referencia **`id_forma_pago_det`** (nivel cuenta/instrumento), no `id_orden_pago` — así un pago multi-cuenta genera un ítem por banco.

---

### C.1 Flujos para diagrama de secuencia (Generar / Anular OP)

> Documentación de los dos flujos transaccionales de la Orden de Pago, con **participantes** y
> **mensajes ordenados**, pensada como insumo directo para los diagramas de secuencia. Refleja lo
> **implementado** en `OrdenPagoService` (2026-07-24). El "guard anti doble-pago" y la "anulación con
> reversa total" son los dos puntos que motivaron esta sección.

**Participantes (lifelines) comunes:**
`Servlet` (`OrdenPagoServlet`, ✅ implementado) · `OrdenPagoService` (dueño de la transacción) ·
`OrdenPagoDAO` · `OrdenPagoDetalleDAO` · `FormaPagoDetalleDAO` · `ChequeDAO` · `ChequeraDAO` ·
`CuentaPagarDAO` · `ProvisionCuentaPagarDAO` · `BD` (PostgreSQL).
Regla transversal: **el Service abre la conexión, hace `setAutoCommit(false)` y es el único que hace
`commit`/`rollback`**; todos los DAOs corren sobre esa misma `Connection`.

---

#### Flujo 1 — Generar OP: `guardarOrdenPagoCompleta(orden, detalles, formasPago)`

**Pre-condición (fuera de la tx):** `validar(...)` — provisión previa obligatoria
(`orden.idProvisionCtaPagar != null`), hay detalles y formas de pago, cada forma con
`monto > 0` y con cuenta bancaria, cada línea de **cheque** con chequera + tipo de cheque + usuario
(evita el NPE de emisión), y **Σ formas == monto de la OP**. Si algo falla → excepción, no se abre la tx.

```
Servlet → Service : guardarOrdenPagoCompleta(orden, detalles, formasPago)
Service → Service : validar(...)                              [si falla → throw, fin]
Service → BD      : getConnection(); setAutoCommit(false)     [inicio TX]

  paso 0 — GUARD ANTI DOBLE-PAGO (el fix del punto 1)
  Service → ProvisionCuentaPagarDAO : getEstadoBloqueado(idProvision)
  ProvisionCuentaPagarDAO → BD      : SELECT prov_cta_pag_estado ... FOR UPDATE   (bloquea la fila)
  BD → Service                      : estado
     alt estado == null            → throw "La provisión no existe"           → rollback
     alt estado != 'Pendiente'     → throw "no disponible (Procesada/Anulado)" → rollback
     (else continúa)

  paso 1 — Cabecera
  Service → OrdenPagoDAO : insertarOrdenPago(orden)
  OrdenPagoDAO → BD      : INSERT orden_pago_cabecera → id_orden_pago (serial)

  paso 2 — Detalle (N facturas de la provisión, solo lectura)
  loop por cada detalle:
    Service → OrdenPagoDetalleDAO : insertarDetalle(det, idOrden)
    OrdenPagoDetalleDAO → BD      : INSERT orden_pago_detalle (FK compuesta id_cta_pagar+id_fact_comp_cab)

  paso 3 — Formas de pago (+ emisión de cheque real por línea de cheque)
  loop por cada forma:
    alt la forma es CHEQUE:
      Service → ChequeraDAO : proximoNumeroCheque(idChequera)
      ChequeraDAO → BD      : SELECT COALESCE(MAX(chq_numero), desde-1)+1 ; valida ≤ hasta_nro
      BD → Service          : proximoNro         [si > hasta_nro → throw "Chequera agotada" → rollback]
      Service → ChequeDAO   : insertarCheque(cheque)   (defaults: estado 'Emitido', fechas, a la orden…)
      ChequeDAO → BD        : INSERT cheque → id_cheque
    Service → FormaPagoDetalleDAO : insertarFormaPago(fp, idOrden)   (id_cheque NULL si transferencia)
    FormaPagoDetalleDAO → BD      : INSERT forma_pago_detalle

  paso 4 — Descontar saldo de cada factura
  loop por cada detalle:
    Service → CuentaPagarDAO : descontarSaldo(idCtaPagar, idFactura, importe)
    CuentaPagarDAO → BD      : SELECT cta_pag_saldo ... FOR UPDATE
    CuentaPagarDAO → BD      : UPDATE cta_pag_saldo -= importe, cta_pag_estado = recalc
                              ('En provision' → 'Cancelado' | 'Pendiente' | 'Saldo a favor')

  paso 5 — Consumir la provisión (el fix del punto 1)
  Service → ProvisionCuentaPagarDAO : actualizarEstado(idProvision, 'Procesada')
  ProvisionCuentaPagarDAO → BD      : UPDATE provision_cuenta_pagar SET estado = 'Procesada'

Service → BD : commit()                                       [fin TX OK]
  (cualquier throw en 0..5 → Service → BD : rollback() ; se re-lanza la excepción)
Service → Servlet : idOrden
```

**Por qué evita el doble pago:** el `FOR UPDATE` del paso 0 **bloquea la fila de la provisión** hasta
el `commit`. Una segunda OP concurrente sobre la misma provisión queda esperando ese lock; cuando lo
obtiene, la provisión ya está en `'Procesada'` → la validación la rechaza. Sin este guard, dos OPs
podían descontar el saldo dos veces (pago duplicado). El `estado` es a la vez el **candado lógico** (una
provisión `'Procesada'` no se re-paga) y el que se **revierte** al anular.

---

#### Flujo 2 — Anular OP: `anularOrdenPagoCompleta(idOrdenPago)` (el fix del punto 2)

Reversa **simétrica** del Flujo 1: deshace saldo, cheques y la consumición de la provisión, todo en
UNA transacción. Es el equivalente al patrón de "anular Factura de Compra".

```
Servlet → Service : anularOrdenPagoCompleta(idOrdenPago)
Service → BD      : getConnection(); setAutoCommit(false)     [inicio TX]

  paso 1 — Cargar y validar
  Service → OrdenPagoDAO : getOrdenPago(idOrdenPago)
  OrdenPagoDAO → BD      : SELECT orden_pago_cabecera
  BD → Service           : orden
     alt orden == null            → throw "no existe"        → rollback
     alt orden.estado == 'Anulado'→ throw "ya está anulada"  → rollback

  paso 2 — Devolver el saldo de cada factura pagada
  Service → OrdenPagoDetalleDAO : listarPorOrden(idOrdenPago)
  OrdenPagoDetalleDAO → BD       : SELECT detalle JOIN cuenta_pagar JOIN factura
  loop por cada detalle:
    Service → CuentaPagarDAO : restaurarSaldoPorAnulacionOP(idCtaPagar, idFactura, importe)
    CuentaPagarDAO → BD      : SELECT cta_pag_saldo ... FOR UPDATE
    CuentaPagarDAO → BD      : UPDATE cta_pag_saldo += importe, cta_pag_estado = 'En provision'
                              (vuelve a quedar reservada por la provisión que se reactiva)

  paso 3 — Anular los cheques emitidos por esta OP
  Service → FormaPagoDetalleDAO : listarPorOrden(idOrdenPago)
  FormaPagoDetalleDAO → BD       : SELECT forma_pago_detalle
  loop por cada forma con id_cheque != NULL:
    Service → ChequeDAO : anularCheque(idCheque)
    ChequeDAO → BD      : UPDATE cheque SET chq_estado = 'Anulado'

  paso 4 — Anular la cabecera de la OP
  Service → OrdenPagoDAO : anularOrdenPago(idOrdenPago)
  OrdenPagoDAO → BD      : UPDATE orden_pago_cabecera SET ord_pag_estado = 'Anulado'

  paso 5 — Reactivar la provisión
  Service → ProvisionCuentaPagarDAO : actualizarEstado(idProvision, 'Pendiente')
  ProvisionCuentaPagarDAO → BD      : UPDATE provision_cuenta_pagar SET estado = 'Pendiente'

Service → BD : commit()                                       [fin TX OK]
  (cualquier throw en 1..5 → rollback ; se re-lanza la excepción)
```

**Simetría exacta (por qué es correcta la reversa):**

| Efecto | Generar (Flujo 1) | Anular (Flujo 2) |
|---|---|---|
| Saldo de la factura | `saldo -= importe` (`descontarSaldo`) | `saldo += importe` (`restaurarSaldoPorAnulacionOP`) |
| Estado de `cuenta_pagar` | `'En provision'` → recalc por saldo | vuelve a `'En provision'` (reservada) |
| Cheque | INSERT `cheque` (estado `'Emitido'`) | `chq_estado = 'Anulado'` |
| Cabecera OP | INSERT (estado `'Pendiente'`/activo) | `ord_pag_estado = 'Anulado'` |
| Provisión | `'Pendiente'` → `'Procesada'` | `'Procesada'` → `'Pendiente'` (reutilizable) |

> **Estados de la provisión (máquina de estados):** `Pendiente` (activa, lista para pagar) →
> `Procesada` (consumida por una OP) → y si se anula la OP, vuelve a `Pendiente`. `Anulado` es un
> estado terminal aparte (anulación de la propia provisión, no de la OP).
>
> **Nota de alcance:** los cheques se marcan `'Anulado'` (no se borran) por trazabilidad. No se
> "devuelve" el número al rango de la chequera: `proximoNumeroCheque` usa `MAX(chq_numero)`, así que un
> cheque anulado deja su número consumido (comportamiento bancario correcto: un talonario no reusa
> números). Si en el futuro se quisiera reusar, habría que excluir los anulados del `MAX`.

**Componentes — ✅ todos creados:**
- ✅ `OrdenPagoDAO` — reescrito: insert de cabecera sin `id_cheque`/`id_cuenta`/`id_moneda`/`ord_pag_tipo_cambio`, parte de la tx; + `obtenerProximoNumero()` (correlativo `ord_pag_numero` = `MAX + 1`, para mostrarlo al abrir la OP) y `anularOrdenPago`.
- ✅ `OrdenPagoDetalleDAO` — insert N filas (con FK compuesta a `cuenta_pagar`) + `listarPorOrden` hidratando factura/monto/saldo/plazo.
- ✅ `FormaPagoDetalleDAO` — insert N formas (con `id_cheque` y `forma_pag_tipo_cambio` nullables) + `listarPorOrden` **hidratando la cuenta bancaria y el cheque** (la pantalla muestra entidad financiera y N° de cheque emitido).
- ✅ `ChequeDAO` — `insertarCheque`, `anularCheque` y `getCheque` (para ver una OP guardada); `ChequeraDAO` — `getChequera`/`listarChequeras`/`proximoNumeroCheque` (con validación de rango).
- ✅ `CuentaPagarDAO` — `descontarSaldo(idCtaPagar, idFactComp, importe)` + `restaurarSaldoPorAnulacionOP(...)`, con recálculo de estado vía `calcularEstadoPorSaldo` (misma tx).
- ✅ `OrdenPagoService` — dueño de la tx, orquesta los 5 pasos (+ `anularOrdenPagoCompleta` y `obtenerProximoNumero`).
- ✅ **Combos:** `FormaPagoCabeceraDAO` + `FormaPagoCabeceraService` (Cheque/Transferencia), `TipoChequeDAO` + `TipoChequeService`, `ChequeraService` — los tres **creados en esta etapa** (no existían). Se reusan `SucursalService`, `CuentaService` (cuenta bancaria, que ya trae moneda y entidad financiera) y `ProvisionCuentaPagarService` con el nuevo `listarProvisionesPendientes()` (solo provisiones `'Pendiente'`, vía `ProvisionCuentaPagarDAO.listarProvisionesPorEstado`).
- ✅ `OrdenPagoServlet` (Session+Token, calcado de `FacturaCompraServlet`) + `ordenPago.jsp`.
- ✅ `/OrdenPagoServlet` registrado en `AuthorizationFilter` (módulo `tesoreria`) + link de `menuLateral.jsp` apuntado al servlet (antes iba directo al `.jsp`, salteando el filter de permisos).

**Entidades (POJOs) — ✅ alineadas y verificadas** (detalle en §11):
- ✅ `OrdenPago` — sin `idCheque`/`idCuenta` (y sin `idMoneda`/`tipoCambio`, por C6).
- ✅ `OrdenPagoDetalle` — con `idOrdenPagoDet` (serial PK).
- ✅ `FormaPagoDetalle` — sin los montos `transferencia`/`cheque`; con referencia a `Cheque` (FK nullable) y `tipoCambio` (C6).
- ✅ `Cheque`, `Chequera`, `FormaPagoCabecera` — ya estaban alineadas.

> Al poblar `orden_pago_detalle`, **`tienePagosAplicados` empieza a funcionar de verdad**: el guard de
> "no editar/anular factura con pagos aplicados" (ver `NOTA_CREDITO_DEBITO_PLAN.md` §8.4) detecta el pago.

**Capa web — `OrdenPagoServlet` (✅ 2026-07-26):** Session+Token calcado de `FacturaCompraServlet`
(clase `OrdenPagoState` serializable en sesión bajo `ordenPago_<token>`, switch-case con métodos
delegados, `leerDatosFormulario()` en cada acción para no perder la cabecera cargada). Implementa el
contrato documentado en el encabezado de `ordenPago.jsp`:

| Acción | Qué hace |
|---|---|
| `ListarModal` *(default)* | Lista OPs + provisiones **pendientes** para los modales de búsqueda. |
| `Nuevo` | Token nuevo, fecha de hoy, estado `'Pendiente'`, N° de OP = `obtenerProximoNumero()`, carga combos. |
| `CargarProvision` | Trae las facturas de la provisión como detalle (solo lectura), fija proveedor y `ord_pag_monto` = Σ, y **limpia las formas ya cargadas** (cambia el total). Si llega **sin token** arranca un documento nuevo (el botón "Buscar Provisión" está habilitado en la pantalla recién abierta). Rechaza provisiones que no estén `'Pendiente'`. |
| `CambiarTipoPago` | Relee la cabecera y vuelve a la vista (submit del `onchange`). |
| `AgregarForma` | Valida tipo/cuenta/monto (+ chequera y tipo de cheque si es cheque), arma la `FormaPagoDetalle` y, en las líneas de cheque, el `Cheque` con **`usuario` de la sesión**. Si falla, repuebla `formaEnEditor` y setea `abrirModalForma` para reabrir el modal con lo cargado. |
| `EliminarForma` | Quita la forma por `index`. |
| `Generar` | Valida provisión/sucursal/tipo de pago/formas y **Σ formas == total**, completa monto/estado/fecha/N°/recibo, re-asegura el `usuario` de cada cheque y delega en `guardarOrdenPagoCompleta`. |
| `Anular` | Delega en `anularOrdenPagoCompleta` (reversa total). |
| `Cancelar` | Descarta el estado de la sesión. |

Decisiones tomadas al implementarlo:
- **Al agregar** una forma se bloquea que Σ **supere** el total (el Service exige igualdad exacta al
  generar; así el error aparece antes y no al final).
- **`ord_pag_nro_recibo`** es `NOT NULL` pero el recibo lo da el proveedor y no siempre existe
  (compras al contado) → **0 = sin recibo**.
- **`forma_pag_estado`** nace en `'Pendiente'` = pendiente de conciliación bancaria (§F la cierra).
- Cambiar de provisión o generar/anular **invalida el documento en sesión** (se limpia el token).

> **📍 DÓNDE NOS QUEDAMOS (2026-08-31):** Provisión, **Orden de Pago** y **entrega de cheques**
> terminadas. El circuito corre, la provisión pasa a `'Procesada'` y el cheque queda `'Entregado'`.
> **Débitos y créditos** (§D) quedaron hechos el 2026-08-31, con lo que cierran 3.8 y 3.9. Con eso ya
> están cargados los tres orígenes de movimiento que la conciliación necesita: órdenes de pago,
> débitos y créditos. Lo próximo es **Fondo Fijo** (§E); de cheques queda la anulación individual (§G3).
>
> _Historial:_ **Débitos y créditos (D) COMPLETOS** (2026-08-31) — `MovimientoBancarioServlet` +
> `movimientoBancario.jsp`, una sola vista parametrizada por tipo. Hizo falta agregar dos columnas por
> tabla: el estado (para poder anular) y el tipo de cambio. Ver §D.
>
> _Historial:_ **ABM de chequeras (G1) COMPLETO** (2026-08-31) — `ChequeraServlet` + `chequera.jsp`
> calcados de Cuentas Bancarias, con control de solapamiento de rangos y del consumo de la chequera.
>
> _Historial:_ **Entrega de cheques (3.3) COMPLETA** (2026-08-17) — columnas `chq_fecha_entrega` y
> `chq_entregado_a`, estado `'Entregado'`, modal en la OP y el N° de recibo cargado en ese momento;
> el detalle está en §G2. La UI del modal de formas de pago se pulió el 2026-08-27/28 (confirmación
> para eliminar una línea y campos con etiqueta flotante).
>
> _Historial:_ **Orden de Pago COMPLETA**
> — esquema, POJOs, DAOs, `OrdenPagoService` transaccional (guard anti doble-pago, consumo de provisión,
> anulación con reversa total — ver §C.1), `ordenPago.jsp` y `OrdenPagoServlet`, más el registro en
> `AuthorizationFilter` y el menú. **Próximo paso: compilar y probar de punta a punta** (en el entorno
> WSL del análisis no hay `java` en el PATH; en Windows están JDK 8 en `C:\Program Files\Java\jdk1.8.0_202`,
> Adoptium 17/21 y Maven en `C:\Program Files\apache-maven-3.9.14`). El seed de `forma_pago_cabecera`,
> `tipo_cheque`, `chequera` y `cuenta` ya está en `Inserts inciales.sql`, así que los combos del carrito
> tienen datos; **si la base ya estaba creada, hay que correr solo los INSERT nuevos de
> `forma_pago_cabecera`**. Queda **confirmar `forma_pag_tipo_cambio` contra Power Architect**.
> Después de probar: **Débitos/Créditos (§D)**.

> **Idea no implementada:** prefijar el "Tipo de pago" (`ord_pag_tipo_pago`) desde
> `fact_comp_tipo_factura` de las facturas de la provisión (`'fondoFijo'` → reposición FF, si no otros
> gastos), como sugiere el comentario de la columna en el esquema. Hoy lo elige el usuario; requeriría
> traer el tipo de factura en el detalle de la provisión.

---

### D. Movimientos bancarios: débitos y créditos

**Tablas:** `debitos` (egresos: comisiones, GA banco, débitos automáticos), `creditos` (ingresos:
depósitos/boletas; enlaza `id_cobro` del lado ventas).

✅ **Implementado el 2026-08-31.** Cierra 3.8 y 3.9. Movimientos del banco que no vienen de una orden
de pago y que alimentan la conciliación.

**Componentes:** `DebitoDAO` / `CreditoDAO`, `DebitoService` / `CreditoService`, y **un solo**
`MovimientoBancarioServlet` + `movimientoBancario.jsp` parametrizados por tipo (`tipo=debito|credito`),
igual que `NotaCreditoDebitoServlet` hace con las notas: es el mismo formulario y lo único que cambia es
la tabla donde cae el movimiento. En el menú son dos entradas, así que el usuario ve dos pantallas.

Para que la vista pudiera ser una sola, `Debito` y `Credito` implementan la interfaz
`MovimientoBancario` (`getId`, `getMonto`, `getEstado`…): así la JSP no tiene que preguntar qué está
mirando para saber qué getter llamar.

**Dos columnas nuevas por tabla** (`debitos_estado` / `creditos_estado` y `debitos_tipo_cambio` /
`creditos_tipo_cambio`), porque el prototipo pedía cosas que el esquema no tenía dónde guardar:

- **Estado** (`VARCHAR(20)`, *Vigente* / *Anulado*): el prototipo tiene botón **Anular** y no había
  columna. Anular **marca, no borra**: `conciliacion_bancaria_detalle` referencia `id_debitos` e
  `id_creditos`, así que un movimiento ya conciliado no puede desaparecer. Es además el criterio del
  resto del módulo (cheque, orden de pago, provisión).
- **Tipo de cambio** (`DOUBLE PRECISION`, nullable): mismo criterio que `forma_pag_tipo_cambio` en la
  orden de pago. Una comisión sobre una cuenta en dólares necesita la cotización para entrar a la
  conciliación en guaraníes. Es opcional: en una cuenta en guaraníes se deja vacío.

**Decisiones de la vista:**

- **Moneda es readonly y sale de la cuenta**, no se carga ni se guarda: no hay `id_moneda` en estas
  tablas, y es la misma decisión que se tomó en la orden de pago (§C6).
- **Banco es un combo de filtro** que no se persiste: la tabla sólo guarda `id_cuenta`. Filtra las
  cuentas del combo de al lado, en el cliente, sin ida y vuelta al servidor.
- **Sin Session+Token.** El movimiento es una sola fila, no hay carrito que sostener entre pedidos, así
  que no hace falta el estado en sesión que sí usan la orden de pago y la provisión.
- **No mueven ningún saldo.** `cuenta` no tiene columna de saldo: el movimiento se cruza recién en la
  conciliación (§F).

**⭐ Acá también entra el registro de depósitos bancarios (requerimiento 3.9).** La boleta de
depósito **es** una fila de `creditos`: el propio esquema lo dice —
*"`creditos_nro_comprobante`: comprobante puede ser nro de boleta de deposito"*. O sea que 3.8 y
3.9 se resuelven con el mismo ABM, no son dos módulos.

> **Bloqueo resuelto (2026-08-17).** `creditos.id_cobro` era **`NOT NULL`**, y un cobro pertenece al
> ciclo de **ventas**, que todavía no tiene UI: con esa restricción no se podía registrar un depósito
> sin implementar Cobros antes. Ya está nullable y subido a la BD.
>
> Con `id_cobro` nullable quedan dos orígenes para un crédito: **con cobro** (depósito de una
> recaudación de ventas, cuando exista ese módulo) y **sin cobro** (depósito directo, transferencia
> recibida, capitalización de intereses). El ABM debe dejar el campo vacío por ahora.

---

### E. Fondo Fijo + rendición

**Tablas:** `fondo_fijo` (caja chica por responsable/proveedor), `fondo_fijo_rendicion` (agrupa varias
facturas FF, `nro_rendicion`), `fondo_fijo_rendicion_detalle` (FK compuesta a `cuenta_pagar`).

**Flujo:** factura con `fact_comp_tipo_factura = 'fondoFijo'` → `cuenta_pagar` (estado *RENDICIÓN
PENDIENTE*) → **rendición** (siempre por el **total**, son montos chicos) → **provisión** → **orden de
pago** de reposición. Reutiliza los sub-módulos B y C.

**Componentes:** `FondoFijoDAO`, `FondoFijoRendicionDAO` + `...DetalleDAO`, Services, Servlet, JSP.

> `fondo_fijo_rendicion` y su detalle **no tienen PK serial** (no declaran `nextval`) → el id se
> asigna manualmente o hay que agregar la secuencia. A revisar al implementar.

---

### F. Conciliación bancaria *(el objetivo final)*

**Tablas:** `conciliacion_bancaria` (cabecera por cuenta y período: saldo inicial/final/banco),
`conciliacion_bancaria_detalle` (ítems: FK nullable a `orden_pago_cabecera`, `debitos`, `creditos`,
`forma_pago_detalle`; `conc_bancaria_tipo` = 'Cred'/'Deb'; `conc_bancaria_conciliado` BOOLEAN).

Es la tabla puente que **cruza todos los movimientos** (órdenes de pago, débitos, créditos) contra el
**extracto del banco**, marca cada ítem como conciliado, y compara `saldo_final` calculado vs
`saldo_banco`.

**Componentes:** `ConciliacionBancariaDAO` + `...DetalleDAO`, Service, Servlet, JSP. Depende de que
existan OP, débitos y créditos (por eso va al final).

---

### G. Gestión de cheques *(parcial — G1 y G2 implementadas; G3 pendiente)*

> Sección agregada el 2026-08-13. §C la venía referenciando como "§G" (ABM de chequera) pero nunca
> se había escrito. Cubre además dos requerimientos que no estaban planificados en ningún lado:
> **3.3 (entrega de cheques)** y la mitad faltante de **3.4 (anulación de cheques)**.

**Tablas:** `cheque`, `chequera`, `tipo_cheque`.

El cheque nace **dentro de la Orden de Pago**: `OrdenPagoService` lo emite con estado `'Emitido'` y
número tomado del rango de la chequera, y desde la misma OP se registra su entrega al proveedor. No
existe todavía una pantalla propia de cheques. De las tres cosas que faltaban, **G2 ya está
implementada**, y **G1 también** desde el 2026-08-31; queda G3:

**G1. ABM de chequeras.** ✅ **Implementado el 2026-08-31.** Antes las chequeras venían solo del
**seed** (`Inserts inciales.sql`: Itaú 1000001–1000050, Ueno 2000001–2000050) y, al agotarse una, la
emisión fallaba con *"Chequera agotada"* sin forma de cargar otra desde la aplicación. `ChequeraServlet`
+ `chequera.jsp` están calcados de Cuentas Bancarias (`CuentaServlet` / `cuenta.jsp`), registrados en
`AuthorizationFilter` bajo el módulo `tesoreria` y enlazados desde el menú.

Lo que se resolvió más allá del alta simple:

- **Solapamiento de rangos.** `proximoNumeroCheque` calcula `MAX(chq_numero) + 1` **por chequera, no
  por cuenta**: dos chequeras de la misma cuenta con rangos que se pisan emitirían dos cheques con el
  mismo número en el mismo banco, y la base no tiene ningún `CHECK` que lo impida. El Service rechaza
  el solapamiento al insertar y al actualizar.
- **Rango contra lo ya emitido.** Al editar, el rango nuevo tiene que contener los cheques que la
  chequera ya emitió; si no, esos cheques quedarían fuera de su propia chequera y el próximo número
  saldría mal.
- **Eliminar.** `cheque.id_chequera` es FK `ON DELETE NO ACTION`, así que el borrado de una chequera
  con cheques fallaba con el error crudo de PostgreSQL. Ahora se corta antes, avisando cuántos cheques
  tiene.
- **Consumo visible.** La grilla muestra *Emitidos*, *Próximo N°* y *Disponibles*, con badge amarillo
  cuando quedan 10 o menos y rojo cuando está agotada. Los disponibles salen de `MAX(chq_numero)` y no
  del conteo, porque un número anulado no se reutiliza y consume rango igual.

De paso se corrigió que `ChequeraDAO` no hidrataba la cuenta: el combo de chequera de la Orden de Pago
mostraba la serie sin el nombre del banco.

Las validaciones viven en `ChequeraService`, **adentro de la transacción**, para que el control y el
guardado vean el mismo estado.

**G2. Registrar la entrega al proveedor (requerimiento 3.3).** ✅ **Implementado el 2026-08-17.** La
decisión que estaba pendiente se resolvió por las columnas: el estado `'Entregado'` solo no alcanzaba,
porque `chq_a_la_orden` es a nombre de quién se emite el cheque y no quién lo retiró. Se agregaron
`chq_fecha_entrega DATE` y `chq_entregado_a VARCHAR` a `cheque`. La entrega se registra **desde la
Orden de Pago** (botón "Registrar entrega de cheques", que aparece cuando alguna forma de pago tiene
cheque, no según el tipo de pago de la cabecera: una OP puede mezclar transferencia y cheque).

Cómo quedó:

- `ChequeDAO.registrarEntrega` graba fecha, receptor y estado `'Entregado'`, y **excluye los anulados
  en el `WHERE`** en vez de pisarles el estado, para que `'Anulado'` gane si la OP se anula después de
  la entrega.
- `OrdenPagoService.registrarEntregaCheques` corre todo en una transacción y rechaza la entrega sobre
  una OP anulada. Es **re-ejecutable**: volver a guardar corrige una entrega mal cargada en vez de
  fallar.
- El **N° de recibo se carga recién acá**. Lo emite el proveedor al cobrar, así que al generar la OP
  todavía no existe (`ord_pag_nro_recibo` nace en 0 y el campo de la pantalla queda readonly). Entrega
  y recibo son el mismo acto administrativo, por eso se guardan en la misma transacción.
- El modal recibe **los cheques que se marcan**, no asume que sean todos: una OP puede tener varios y
  los diferidos se retiran en otro momento.
- La acción exige permiso de **edición** (`puedeEditar`), no de alta: registrar la entrega modifica una
  OP ya generada.

**G3. Anulación de un cheque individual (mitad de 3.4).** Hoy los cheques sólo se anulan **en
cascada**, cuando se anula la OP entera (`anularOrdenPagoCompleta`, paso 3). El caso real —cheque
mal impreso, extraviado o rechazado, **sin** querer deshacer el pago— no tiene camino. Requiere
decidir qué pasa con la forma de pago que lo referencia: lo natural es anular ese cheque y emitir
uno nuevo de reemplazo sobre la misma `forma_pago_detalle`, dejando el anulado como trazabilidad.
Recordar que el número anulado **no se reutiliza** (`proximoNumeroCheque` usa `MAX(chq_numero)`),
que es el comportamiento bancario correcto.

**Componentes:** `ChequeServlet` + `cheque.jsp` (ya listados como pendientes en
`DOCUMENTACION_PROYECTO.md`), `ChequeraDAO` ampliado con alta/baja, `ChequeService`.

> **Rinde bien hacerlo temprano:** una sola pantalla cierra 3.3, completa 3.4 y saca el bloqueo de
> la chequera agotada.

---

### H. Informes *(pendiente — sin planificar)*

> Sección agregada el 2026-08-13. El requerimiento **3.11 (generar informes)** no figuraba en
> ningún documento del proyecto.

**Estado: greenfield total.** No hay una sola línea de código de reportes, ni librería en el
`pom.xml` (hoy sólo `javaee-api` y `postgresql`), ni un diseño de qué informes se esperan.

**A definir antes de poder estimarlo:**
1. **Qué informes.** Los candidatos naturales del módulo son: libro IVA compras por período,
   cuentas a pagar por proveedor y vencimiento, órdenes de pago por período, cheques emitidos /
   pendientes de entrega / a vencer, movimientos por cuenta bancaria, y la propia conciliación.
2. **Qué formato.** Tres caminos, de menor a mayor esfuerzo: (a) una JSP imprimible con CSS
   `@media print` — cero dependencias, lo más rápido; (b) exportar a CSV/Excel desde el servlet
   — sirve para que el contador lo trabaje aparte; (c) PDF con JasperReports o similar — el más
   prolijo y el único que da un formato fijo, pero agrega dependencia y curva de aprendizaje.
3. **Si los informes son un módulo aparte** en el sistema de permisos o cada informe cuelga de su
   módulo (`compra`, `tesoreria`).

**Recomendación:** arrancar por (a) sobre las consultas que ya existen en los DAOs, y reservar el
PDF para los informes que realmente se impriman y archiven.

---

## 5. Decisiones de diseño (a cerrar antes de implementar cada parte)

| # | Decisión | Propuesta | Estado |
|---|---|---|---|
| 1 | Patrón de servlets | Session + Token + Service transaccional (calcar Factura de Compra) | ✅ Decidido (aplicado en Cuenta y Provisión) |
| 2 | ¿Dónde se descuenta `cta_pag_saldo`? | En la **Orden de Pago** (no en la provisión); la provisión solo reserva/agrupa | ✅ Decidido — se implementa en la OP (§C) |
| 3 | Neteo del saldo a favor de NC | En la **provisión** (filtro `saldo ≠ 0`, líneas negativas, neto ≥ 0) — ver NC plan §4 | ✅ Decidido e implementado (Provisión) |
| 4 | Estados de `cuenta_pagar` en el flujo | `Pendiente` → `En provision` → `Cancelado`/`Saldo a favor` (recalculado por `calcularEstadoPorSaldo`) | ✅ Decidido e implementado |
| 5 | Conciliación: ¿la OP inserta el ítem, o se concilia después? | La OP **NO** inserta en `conciliacion_bancaria_detalle`; el **módulo de Conciliación** (§F) jala las OPs del período (no existe cabecera de conciliación al guardar la OP) | ✅ Decidido (revierte el comment de BD) — ver §C |
| 6 | Formas de pago por OP | **VARIAS** formas por OP (mixtas: transferencia + cheque(s), incluso multi-cuenta); cheque **real** emitido desde chequera | ✅ Decidido e implementado en esquema (§C, C3/C4/C5) |
| 7 | PK de `orden_pago_detalle` | Agregar serial `id_orden_pago_det` (una OP → N facturas de la provisión) | ✅ Aplicado en Power Architect (§C) |
| 10 | Cheque: ¿solo referencia o real? | **Cheque real** desde chequera (Nro dentro del rango); chequera por seed, ABM de chequera para §G | ✅ Decidido — `forma_pago_detalle.id_cheque` (§C, C4) |
| 11 | Cuenta de la OP: cabecera o detalle | En el **detalle** (`forma_pago_detalle.id_cuenta`) → multi-cuenta; se quitó de la cabecera | ✅ Decidido e implementado (§C, C5) |
| 8 | Fondo Fijo: flujo | 2ª opción del comment: `FACTURA FF → RENDICIÓN → PROVISIÓN → OP` | A confirmar |
| 9 | Montos `INTEGER` | Riesgo de overflow (~2.147 mill. de Gs) — preexistente. **Se mantiene por ahora** (decisión 2026-08-17), pero **conviene migrar a `BIGINT`** antes de tener volumen de datos real: ver §8 | Aceptado con reserva |

---

## 6. Orden de implementación recomendado

1. ✅ **Referenciales bancarios** (A) — moneda, tipo entidad, entidad financiera, tipo cuenta, **cuenta**. *(HECHO.)*
2. ✅ **Provisión de cuenta a pagar** (B) — con **neteo del saldo a favor** de NC. *(HECHO — cierra el circuito de NC/ND.)*
3. ✅ **Orden de pago** (C) — descuento de saldo + **N formas de pago mixtas** con cheque real desde chequera. *(HECHA — falta la prueba end-to-end.)*
4. ⏳ **Probar la Orden de Pago de punta a punta** — es el requerimiento 3.2 y todo lo que sigue se
   apoya en que funcione (el descuento de saldo, el cheque emitido y la reversa al anular). *(PRÓXIMO.)*
5. ~~**Débitos / Créditos** (D)~~ ✅ hecho el 2026-08-31; cerró **3.8 y 3.9** de una vez.
6. **Gestión de cheques** (G) — una pantalla cierra **3.3**, completa **3.4** y saca el bloqueo de
   la chequera agotada. Barata en relación a lo que resuelve.
7. **Fondo Fijo + rendición** (E) — **3.5, 3.6 y 3.7**; reutiliza B y C. Agregar antes las
   secuencias de `fondo_fijo_rendicion(_detalle)`.
8. **Conciliación bancaria** (F) — **3.10**, el objetivo (jala OPs + débitos + créditos del período).
9. **Informes** (H) — **3.11**, cuando esté definido el alcance y el formato.
10. *(Fase posterior)* **Lado cobros/caja** (§9).

---

## 7. Checklist de implementación (alto nivel)

**A. Referenciales bancarios** — ✅ HECHO
- [x] DAO+Service (`Cuenta` + `Moneda`, `TipoCuenta`, `EntidadFinanciera` para combos) + `CuentaServlet` + `cuenta.jsp`
- [x] Seed cargado (moneda, tipo_entidad_financiera, entidad_financiera, tipo_cuenta)

**B. Provisión** — ✅ HECHO
- [x] `CuentaPagarDAO`: `listarCuentasPagarPorProveedor(idProveedor)` con **saldo ≠ 0** (incluye negativos), + `marcarEnProvision`/`revertirProvision`/`calcularEstadoPorSaldo`
- [x] `ProvisionCuentaPagarDAO` (+ detalle) transaccional
- [x] `ProvisionCuentaPagarService` (dueño de la tx)
- [x] `ProvisionCuentaPagarServlet` (Session+Token) + `provision.jsp`; valida **neto ≥ 0**
- [x] Registrar `/ProvisionCuentaPagarServlet` en `AuthorizationFilter` (módulo `tesoreria`)

**C. Orden de pago** — ✅ IMPLEMENTADA (esquema + backend + capa web); **falta probarla end-to-end**
- [x] ✅ Esquema en Power Architect: seriales `id_orden_pago_det` / `id_forma_pago_det`, `id_cheque` movido a `forma_pago_detalle`, `id_cheque`/`id_cuenta` fuera de la cabecera, `conciliacion_bancaria_detalle.id_forma_pago_det` nullable
- [x] ✅ Seed de `cuenta` / `tipo_cheque` / `chequera` en `Inserts inciales.sql`
- [x] ✅ **Alinear POJOs** (§11): `OrdenPago` (sin idCheque/idCuenta), `OrdenPagoDetalle` (con idOrdenPagoDet), `FormaPagoDetalle` (con `Cheque`) — verificado contra el esquema
- [x] ✅ `OrdenPagoDAO` transaccional (cabecera sin idCheque/idCuenta) + `OrdenPagoDetalleDAO` (N filas, FK compuesta)
- [x] ✅ `FormaPagoDetalleDAO` (con `id_cheque` nullable) + `ChequeDAO` (insert + `anularCheque`) + `ChequeraDAO` (`proximoNumeroCheque` con validación de rango)
- [x] ✅ `CuentaPagarDAO.descontarSaldo(...)` + `restaurarSaldoPorAnulacionOP(...)`, recálculo con `calcularEstadoPorSaldo`
- [x] ✅ `OrdenPagoService` (dueño de la tx). **NO** inserta conciliación. Incluye **guard anti doble-pago** (paso 0, provisión `FOR UPDATE`) + **consumo de provisión** (paso 5, `'Procesada'`) + **`anularOrdenPagoCompleta`** con reversa total. Ver §C.1. Validación de cheque (chequera/tipo/usuario) evita NPE de emisión.
- [x] ✅ `ordenPago.jsp` — vista calcada de `facturaCompra.jsp`/`provision.jsp` (form único + JS, Session+Token). Cabecera SIN Banco/Cuenta/Cheque (movidos al carrito); **bloque "Formas de Pago" tipo carrito** (Tipo Cheque/Transferencia, cuenta bancaria, monto, referencia + campos de cheque condicionales: chequera/tipo/fechas, con N° auto al generar); detalle de facturas SOLO LECTURA; "Tipo de pago" (`ord_pag_tipo_pago`) con **tooltip**; validación visual Σ formas == Importe Total; modales Buscar OP / Buscar Provisión / confirmaciones. **El contrato de atributos/acciones que espera el servlet está documentado en el encabezado del JSP.**
- [x] ✅ **(2026-07-25)** Moneda/tipo de cambio fuera de la cabecera: `id_moneda` y `ord_pag_tipo_cambio` eliminados, `forma_pag_tipo_cambio` agregado en el detalle de formas — aplicado en esquema, entidades, DAOs y vista (ver C6)
- [x] ✅ **Combos que no existían**: `FormaPagoCabeceraDAO`+`Service`, `TipoChequeDAO`+`Service`, `ChequeraService`; + `OrdenPagoDAO.obtenerProximoNumero()`, `ProvisionCuentaPagarDAO.listarProvisionesPorEstado()` / `Service.listarProvisionesPendientes()`, `ChequeDAO.getCheque()` y la hidratación de cuenta/cheque en `FormaPagoDetalleDAO.listarPorOrden()`
- [x] ✅ `OrdenPagoServlet` (Session+Token) — implementado según el contrato del `ordenPago.jsp`: acciones `ListarModal, Nuevo, CargarOrdenPago, CargarProvision, CambiarTipoPago, AgregarForma, EliminarForma, Generar, Anular, Cancelar`; combos poblados (sucursal/cuentas/formaPago/chequeras/tipoCheque); valida **provisión previa `'Pendiente'`**, **Σ formas = monto OP** (y que no lo supere al agregar), y **setea `usuario` desde la sesión** en cada cheque. El "sin efectivo" lo garantiza el catálogo `forma_pago_cabecera` y el **N° de cheque en rango** lo valida `ChequeraDAO`
- [x] ✅ Registrado `/OrdenPagoServlet` en `AuthorizationFilter` (módulo `tesoreria`) + link en `menuLateral.jsp` (antes apuntaba al `.jsp` directo, salteando el filter)
- [x] ✅ **Probado end-to-end** (2026-08-17): se generó la OP, la provisión pasó a `'Procesada'` y el circuito respondió como estaba diseñado
- [x] ✅ Seed de `forma_pago_cabecera` (`Cheque` / `Transferencia`) agregado a `Inserts inciales.sql` — sin esas filas el combo "Tipo" del carrito salía vacío
- [x] ✅ Nombre `forma_pag_tipo_cambio` confirmado contra Power Architect (2026-08-17)

**D. Débitos / Créditos** *(cierra 3.8 y 3.9)*
- [x] ✅ **`creditos.id_cobro` nullable** — cambiado en Power Architect y subido a la BD (2026-08-17)
- [x] ✅ `DebitoDAO`/`CreditoDAO` + Services + `MovimientoBancarioServlet` + `movimientoBancario.jsp` (2026-08-31)
- [x] ✅ En el ABM de créditos, `id_cobro` queda vacío: el enlace con cobros llega recién con Ventas
- [x] ✅ Columnas nuevas `debitos_estado`/`creditos_estado` y `debitos_tipo_cambio`/`creditos_tipo_cambio`

**E. Fondo Fijo** *(cierra 3.5, 3.6 y 3.7)*
- [ ] Revisar secuencia (PK no serial) de `fondo_fijo_rendicion(_detalle)`
- [ ] `FondoFijoDAO`, `FondoFijoRendicionDAO` (+detalle), Services, Servlet, JSP

**F. Conciliación** *(cierra 3.10)*
- [ ] `ConciliacionBancariaDAO` (+detalle), Service, Servlet, JSP

**G. Gestión de cheques** *(cierra 3.3 y completa 3.4)*
- [x] ✅ **ABM de chequeras** (G1) — `ChequeraServlet` + `chequera.jsp` (2026-08-31), con control de solapamiento, de rango contra lo emitido y del consumo de la chequera
- [x] ✅ **Registrar entrega al proveedor** (G2) — implementado el 2026-08-17 con estado `'Entregado'` + `chq_fecha_entrega` / `chq_entregado_a`; se registra desde la OP y arrastra el N° de recibo
- [ ] **Anular un cheque individual** (G3) — hoy sólo se anulan en cascada al anular la OP
- [ ] `ChequeServlet` + `cheque.jsp` + `ChequeService`

**H. Informes** *(cierra 3.11)*
- [ ] Definir **qué informes** y **en qué formato** (JSP imprimible / CSV / PDF) — ver §H
- [ ] Decidir si son un módulo propio de permisos o cuelgan de `compra`/`tesoreria`
- [ ] Implementar

**Transversal**
- [ ] Actualizar los links restantes de "Módulo Tesorería" en `menuLateral.jsp` (los de Cuentas Bancarias, Provisión y Orden de Pago ya apuntan a sus servlets; el resto sigue en `.html` placeholders)
- [ ] Permisos del módulo `tesoreria` en cada servlet nuevo

---

## 8. Particularidades y riesgos

- **PK/FK compuesta** `(id_cta_pagar, id_fact_comp_cab)`: los detalles de provisión, orden de pago y
  rendición la referencian juntos. Cuidado al insertar/consultar.
- ~~`orden_pago_detalle` con PK solo `id_orden_pago`~~ → ✅ **resuelto**: ahora tiene serial `id_orden_pago_det` (N facturas por OP).
- **`fondo_fijo_rendicion(_detalle)`** sin secuencia serial en la PK (pendiente para §E).
- **Cheque desde chequera:** el próximo `chq_numero` debe validarse dentro de `[chequera_desde_nro, chequera_hasta_nro]`; controlar "chequera agotada" (§C). ~~Hoy no hay ABM de chequeras~~ → ✅ **resuelto** (2026-08-31, §G1): se cargan desde la aplicación y la grilla avisa el consumo del rango. Queda el riesgo de fondo: el próximo número se calcula **por chequera y no por cuenta**, así que dos rangos solapados de la misma cuenta darían el mismo número de cheque; por eso el ABM valida el solapamiento.
- ~~**`creditos.id_cobro`** era `NOT NULL` y bloqueaba el registro de depósitos~~ → ✅ **resuelto**: nullable y subido (2026-08-17).
- ~~**`cheque` no tiene columnas de entrega** (fecha ni receptor)~~ → ✅ **resuelto**: se agregaron `chq_fecha_entrega` y `chq_entregado_a` (2026-08-17). `chq_a_la_orden` sigue siendo a nombre de quién se emite, no quién retiró (§G2).
- **Descuento de saldo greenfield**: hoy **nada** descuenta `cta_pag_saldo` al pagar; se diseña desde
  cero en la Orden de Pago, en la **misma transacción** que la OP.
- **Nombres de constraint** con doble token (`orden_pagoorden_pago_cabecera_fk`) — cosmético, las FKs
  son correctas.
- **Montos `INTEGER`** (overflow) — preexistente. **Decisión 2026-08-17: se mantienen por ahora**,
  pero la recomendación es **migrar a `BIGINT`**. El techo de `INTEGER` es 2.147.483.647, o sea unos
  **2.147 millones de guaraníes**: una factura grande puede acercarse y un acumulado lo supera. Los
  campos afectados son todos los importes (`fact_comp_*`, `cta_pag_monto`/`saldo`, `ord_pag_monto`,
  `forma_pag_monto`, `prov_cta_pag_monto`, `presu_det_precio_compra`, `art_precio_*`, `stk_*`, etc.).
  Migrar es barato **ahora**, con poco volumen: `ALTER TABLE ... ALTER COLUMN ... TYPE BIGINT` y
  revisar los `Long`/`Integer` de los POJOs y DAOs. Con datos reales encima y reportes armados, el
  mismo cambio es mucho más caro y arriesgado.

---

## 9. Fase posterior — lado Cobros (ventas)

Parte de Tesorería pero ligada al ciclo de **ventas**, fuera del foco inmediato (pagos):
`cobro`, `cobro_detalle`, `cobro_cheque`, `cobro_tarjeta`, `forma_cobro_cabecera/detalle`, `caja`,
`apertura_cierre_caja`, `arqueo_caja`, `recaudaciones_depositar(_detalle)`, `cheque_recibido`,
`titular`, `tarjeta`, `tipo_tarjeta`. Flujo: cobro (efectivo/cheque/tarjeta) → caja/arqueo →
recaudación a depositar → **crédito bancario** → conciliación. Se planifica cuando se aborde Ventas.

---

## 10. Resumen

| Fase | Sub-módulo | Requerimiento | Depende de |
|---|---|---|---|
| 1 | Referenciales bancarios (cuenta, entidad, moneda…) | — | — |
| 2 | Provisión de cuenta a pagar (+ neteo NC) | 3.1 | cuenta_pagar (✅) |
| 3 | Orden de pago + formas de pago (descuenta saldo) | 3.2 | Provisión, Cuenta bancaria |
| 4 | Débitos / Créditos (+ depósitos) | 3.8, 3.9 | Cuenta bancaria, `id_cobro` nullable |
| 5 | Gestión de cheques (ABM chequera ✅, entrega ✅, anulación individual ❌) | 3.3, 3.4 | Orden de pago |
| 6 | Fondo Fijo + rendición | 3.5, 3.6, 3.7 | Provisión, Orden de pago |
| 7 | Conciliación bancaria | 3.10 | OP, Débitos, Créditos |
| 8 | Informes | 3.11 | Lo que se quiera informar |
| 9 | (Cobros/Caja) | — | Ventas |

Con **Referenciales**, **Provisión** y **Orden de Pago** ya implementados (fases 1 a 3), lo que resta
del camino hacia la **conciliación bancaria** es: **probar la OP → Débitos/Créditos → Cheques →
Fondo Fijo → Conciliación → Informes**, reutilizando en cada paso el patrón transaccional
Session+Token de Compras.

---

## 11. Alineación de entidades (POJOs) con la BD nueva

Verificación tras los ajustes de esquema del 2026-07-21 (Power Architect) y del 2026-07-25 (C6).
**✅ Alineación completada** (2026-07-24/25) — todas las entidades del flujo OP→Conciliación:

| POJO | Estado | Cómo quedó |
|---|---|---|
| `OrdenPago` | ✅ **Alineado** | Se **quitaron** `idCheque` e `idCuenta` (columnas eliminadas de la cabecera) y también `idMoneda`/`tipoCambio` (C6). Queda: `idOrdenPago`, `numero`, `fechaEmision`, `monto`, `estado`, `idProvisionCtaPagar`, `numeroRecibo`, `sucursal`, `tipoPago`, `proveedor`. |
| `OrdenPagoDetalle` | ✅ **Alineado** | Se **agregó** `idOrdenPagoDet` (serial PK `id_orden_pago_det`), más `monto`/`cuentaPagar`/`facturaCompra`/`ordenPago`. |
| `FormaPagoDetalle` | ✅ **Alineado** | Se **quitaron** los montos `transferencia`/`cheque`; se **agregó** la referencia `Cheque cheque` (FK `id_cheque` nullable) y `Double tipoCambio` (C6). Conserva `monto`/`estado`/`referencia`/`cuenta`/`fecha`/`formaPagoCabecera`/`ordenPago`. |
| `Cheque` | ✅ **Alineado** | Coincide 1:1 con `cheque` (numero, fechaEmision, estado, chequera, aLaOrden, observacion, tipoCheque, fechaPago, fechaVencimiento, usuario). |
| `Chequera` | ✅ **Alineado** | Coincide con `chequera` (cuenta, serie, desdeNumero, hastaNumero). |
| `FormaPagoCabecera` | ✅ **Alineado** | Coincide con `forma_pago_cabecera` (idFormaPagoCabecera, descripcion). |
| `CuentaPagar` | ✅ **Alineado** | Con `plazo` ya agregado (sesión previa). |

> **DAOs:** `OrdenPagoDAO` fue **reescrito** (sus SQL ya no usan `id_cheque`/`id_cuenta`/`id_moneda`/
> `ord_pag_tipo_cambio`) y `FormaPagoDetalleDAO` se creó alineado (`id_cheque` y
> `forma_pag_tipo_cambio` nullables). No quedan queries contra
> `forma_pag_cheque`/`forma_pag_transferencia`.
