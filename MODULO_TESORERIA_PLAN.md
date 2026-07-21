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

**Servlets de tesorería existentes:** `CuentaServlet`, `ProvisionCuentaPagarServlet` — ambos
registrados en `AuthorizationFilter` (módulo `tesoreria`); links del menú "Módulo Tesorería"
actualizados (Cuentas Bancarias, Provisión de Cta. Pagar).

**Esquema de la OP listo (2026-07-21):** ajustes de Power Architect aplicados y verificados (seriales en
`orden_pago_detalle`/`forma_pago_detalle`, `id_cheque` movido a `forma_pago_detalle`, `id_cheque`/`id_cuenta`
quitados de la cabecera, `conciliacion_bancaria_detalle.id_forma_pago_det` nullable) + seed de
`cuenta`/`tipo_cheque`/`chequera` cargado. Ver §C.

**Pendiente:** **Orden de Pago (§C — en construcción)**, Débitos/Créditos (§D), Fondo Fijo (§E),
Conciliación (§F), y el resto de referenciales/entidades. ⚠️ **3 POJOs desalineados** con la BD nueva
(`OrdenPago`, `OrdenPagoDetalle`, `FormaPagoDetalle`) — a corregir antes de codear los DAOs (ver §11).

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

Son ABM (alta/baja/modificación) simples. **Son prerrequisito** porque una Orden de Pago referencia
`id_cuenta` (banco de donde sale la plata) y `id_moneda`; una `cuenta` referencia
`entidad_financiera` + `tipo_cuenta` + `moneda`.

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

### C. Orden de pago *(donde sale el dinero — ✅ ESQUEMA LISTO, EN CONSTRUCCIÓN)*

**Tablas:** `orden_pago_cabecera`, `orden_pago_detalle`, `forma_pago_cabecera`, `forma_pago_detalle`,
`cheque`, `chequera`, `tipo_cheque`, `cuenta`.

**Prototipo** (`webapp/Images/Prototipo Orden de pago.png`). Es como Factura/Provisión pero para la OP:
- **Botones:** Nuevo · Buscar Orden de pago · Buscar Provisión · Anular.
- **Cabecera:** Nro OP · Recibo Nro (`ord_pag_nro_recibo`) · Fecha · Estado · Sucursal · **Moneda** ·
  **Tipo de cambio** · **Provisión Nro** · **Tipo de pago** (reposición FF / otros gastos) · Razón Social.
  *(La cuenta bancaria y el/los cheque ya NO van en la cabecera → van en cada forma de pago.)*
- **Detalle de facturas** = las de la provisión, **SOLO LECTURA** (Item, Nro factura, Importe total,
  Saldo pendiente, Importe a pagar, Plazo). Sin Editar/Eliminar (vienen fijas de la provisión).
- **Bloque de FORMAS DE PAGO** (tipo carrito, **N filas**): por cada una → Tipo (Transferencia/Cheque),
  **Monto**, **Cuenta bancaria** (de dónde sale), Referencia; si es **Cheque** → Chequera + Tipo de cheque
  + Fecha de pago/vencimiento (el Nro se toma solo del rango de la chequera). Σ montos = total de la OP.
- **Importe Total a Pagar** + Generar / Cancelar.

**Flujo:**
1. Nuevo → **Buscar Provisión** → carga sus facturas (detalle SOLO LECTURA) + proveedor (Razón Social) + Provisión Nro.
2. Completar cabecera: Recibo, Sucursal, Moneda, Tipo de cambio, Tipo de pago (`ord_pag_tipo_pago` = reposición FF / otros gastos).
3. **Cargar una o varias formas de pago** (mixto: p. ej. parte transferencia + parte cheque(s), incluso
   de cuentas distintas). Por cada línea de cheque se emite un cheque real de la chequera.
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

**✅ Ajustes de esquema aplicados (Power Architect, 2026-07-21):**
- ✅ `orden_pago_detalle.id_orden_pago_det` **serial** (N facturas por OP). Mantiene `id_cta_pagar` + `id_fact_comp_cab` (FK compuesta) + `orden_pag_det_monto`.
- ✅ `forma_pago_detalle.id_forma_pago_det` **serial**; se **quitaron** `forma_pag_cheque` y `forma_pag_transferencia` (queda solo `forma_pag_monto` + `id_forma_pago_cab` para el tipo); se **agregó** `id_cheque` **NULLABLE** (FK → `cheque`).
- ✅ `orden_pago_cabecera`: se **quitaron** `id_cheque` y `id_cuenta` (ambos migran al detalle).
- ✅ `conciliacion_bancaria_detalle.id_forma_pago_det` → **NULLABLE** (los ítems de débito/crédito no tienen forma de pago).
- ✅ Seed cargado en `Inserts inciales.sql`: `cuenta` (Itaú/Ueno Gs, Itaú Ahorro USD), `tipo_cheque` (A la vista / Diferido), `chequera` (Itaú 1000001–1000050, Ueno 2000001–2000050).

**Los 5 pasos transaccionales de `guardarOrdenPagoCompleta(...)`** (todo en UNA tx — el Service es dueño de la conexión):
```
1. INSERT orden_pago_cabecera                → id_orden_pago (serial)
2. Por cada factura de la provisión:
     INSERT orden_pago_detalle (SOLO LECTURA, viene de la provisión)
3. Por cada forma de pago:
     - si CHEQUE: próximo nro del rango de la chequera → INSERT cheque → obtiene id_cheque
     - INSERT forma_pago_detalle (id_forma_pago_cab, monto, id_cuenta, referencia, id_cheque|NULL)
4. Por cada factura: cta_pag_saldo -= importe_pagado; recalcular estado (calcularEstadoPorSaldo)
     ('En provision' → 'Cancelado' / 'Pendiente' / 'Saldo a favor')
5. (opcional) marcar la provisión como procesada/con OP
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

**Componentes a crear:**
- `OrdenPagoDAO` — **reescribir**: quitar el CRUD aislado de cabecera; insert de cabecera sin `id_cheque`/`id_cuenta`; parte de la tx.
- `OrdenPagoDetalleDAO` — insert N filas (con FK compuesta a `cuenta_pagar`).
- `FormaPagoDetalleDAO` — insert N formas (con `id_cheque` nullable).
- `ChequeDAO` — insert cheque + `proximoNumero(idChequera)`; `ChequeraDAO` — leer chequera/rango.
- `CuentaPagarDAO` — **método nuevo** `descontarSaldo(idCtaPagar, idFactComp, importe)` + recálculo de estado con `calcularEstadoPorSaldo` (misma tx).
- `OrdenPagoService` — dueño de la tx, orquesta los 5 pasos. `FormaPagoCabeceraService` (combo Cheque/Transferencia), `TipoChequeService`, `ChequeraService` para los combos.
- `OrdenPagoServlet` (Session+Token, calcado de Provisión/Factura) + `ordenPago.jsp` (prototipo).
  Reusar `CuentaService`/`MonedaService`/`EntidadFinancieraService` (combos) y `ProvisionCuentaPagarService` (Buscar Provisión).
- Registrar `/OrdenPagoServlet` en `AuthorizationFilter` (módulo `tesoreria`) + link en `menuLateral.jsp`.

**⚠️ Entidades (POJOs) a alinear ANTES de codear los DAOs** (ver §11):
- `OrdenPago` — **quitar** `idCheque` y `idCuenta` (columnas eliminadas de la cabecera).
- `OrdenPagoDetalle` — **agregar** `idOrdenPagoDet` (nuevo serial PK).
- `FormaPagoDetalle` — **quitar** `transferencia` y `cheque` (montos eliminados); **agregar** referencia a `Cheque` (FK `id_cheque` nullable).
- `Cheque`, `Chequera`, `FormaPagoCabecera` — ✅ ya alineadas.

> Al poblar `orden_pago_detalle`, **`tienePagosAplicados` empieza a funcionar de verdad**: el guard de
> "no editar/anular factura con pagos aplicados" (ver `NOTA_CREDITO_DEBITO_PLAN.md` §8.4) detecta el pago.

> **📍 DÓNDE NOS QUEDAMOS (para retomar):** Provisión terminada y probada. **Esquema de la OP LISTO**
> (todos los ajustes de Power Architect aplicados y verificados; seed de chequera cargado). **Próximo
> paso:** (1) alinear los 3 POJOs (`OrdenPago`, `OrdenPagoDetalle`, `FormaPagoDetalle`), (2) construir
> DAOs + `OrdenPagoService` (5 pasos) + Servlet + `ordenPago.jsp` como está descrito arriba.

---

### D. Movimientos bancarios: débitos y créditos

**Tablas:** `debitos` (egresos: comisiones, GA banco, débitos automáticos), `creditos` (ingresos:
depósitos/boletas; enlaza `id_cobro` del lado ventas).

ABM sobre una `cuenta`. Alimentan la conciliación (son movimientos del banco que no vienen de una OP).

**Componentes:** `DebitoDAO`/`CreditoDAO` + Services + Servlet(s) + JSP(s). ABM relativamente simple.

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
| 9 | Montos `INTEGER` | Riesgo de overflow (~2.147 mill.) — preexistente, se mantiene | Aceptado |

---

## 6. Orden de implementación recomendado

1. ✅ **Referenciales bancarios** (A) — moneda, tipo entidad, entidad financiera, tipo cuenta, **cuenta**. *(HECHO.)*
2. ✅ **Provisión de cuenta a pagar** (B) — con **neteo del saldo a favor** de NC. *(HECHO — cierra el circuito de NC/ND.)*
3. ⏳ **Orden de pago** (C) — descuento de saldo + una forma de pago (cheque solo referencia). *(PRÓXIMO — bloqueado hasta confirmar C3/C4 y ajustar PK de `orden_pago_detalle`.)*
4. **Débitos / Créditos** (D) — ABM de movimientos bancarios.
5. **Fondo Fijo + rendición** (E) — reutiliza B y C.
6. **Conciliación bancaria** (F) — el objetivo (jala OPs + débitos + créditos del período).
7. *(Fase posterior)* **Lado cobros/caja** (§9).

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

**C. Orden de pago** — 🔨 EN CONSTRUCCIÓN (esquema ✅ listo; ver §C)
- [x] ✅ Esquema en Power Architect: seriales `id_orden_pago_det` / `id_forma_pago_det`, `id_cheque` movido a `forma_pago_detalle`, `id_cheque`/`id_cuenta` fuera de la cabecera, `conciliacion_bancaria_detalle.id_forma_pago_det` nullable
- [x] ✅ Seed de `cuenta` / `tipo_cheque` / `chequera` en `Inserts inciales.sql`
- [ ] ⚠️ **Alinear POJOs** (§11): `OrdenPago` (quitar idCheque/idCuenta), `OrdenPagoDetalle` (agregar idOrdenPagoDet), `FormaPagoDetalle` (quitar transferencia/cheque, agregar Cheque)
- [ ] `OrdenPagoDAO` → **reescribir** transaccional (cabecera sin idCheque/idCuenta) + `OrdenPagoDetalleDAO` (N filas)
- [ ] `FormaPagoDetalleDAO` (con `id_cheque` nullable) + `ChequeDAO` (insert + `proximoNumero`) + `ChequeraDAO`
- [ ] `CuentaPagarDAO.descontarSaldo(...)` + recálculo de estado con `calcularEstadoPorSaldo`
- [ ] `OrdenPagoService` (dueño de la tx, orquesta los 5 pasos). **NO** inserta conciliación
- [ ] `OrdenPagoServlet` (Session+Token) + `ordenPago.jsp`; validar **sin efectivo**, **provisión previa obligatoria**, **Σ formas = monto OP**, **Nro cheque en rango**
- [ ] Registrar `/OrdenPagoServlet` en `AuthorizationFilter` (módulo `tesoreria`) + link en `menuLateral.jsp`

**D. Débitos / Créditos**
- [ ] `DebitoDAO`/`CreditoDAO` + Services + Servlets + JSPs (ABM)

**E. Fondo Fijo**
- [ ] Revisar secuencia (PK no serial) de `fondo_fijo_rendicion(_detalle)`
- [ ] `FondoFijoDAO`, `FondoFijoRendicionDAO` (+detalle), Services, Servlet, JSP

**F. Conciliación**
- [ ] `ConciliacionBancariaDAO` (+detalle), Service, Servlet, JSP

**Transversal**
- [ ] Actualizar los links de "Módulo Tesorería" en `menuLateral.jsp` (hoy apuntan a `.html` placeholders)
- [ ] Permisos del módulo `tesoreria` en cada servlet nuevo

---

## 8. Particularidades y riesgos

- **PK/FK compuesta** `(id_cta_pagar, id_fact_comp_cab)`: los detalles de provisión, orden de pago y
  rendición la referencian juntos. Cuidado al insertar/consultar.
- ~~`orden_pago_detalle` con PK solo `id_orden_pago`~~ → ✅ **resuelto**: ahora tiene serial `id_orden_pago_det` (N facturas por OP).
- **`fondo_fijo_rendicion(_detalle)`** sin secuencia serial en la PK (pendiente para §E).
- **Cheque desde chequera:** el próximo `chq_numero` debe validarse dentro de `[chequera_desde_nro, chequera_hasta_nro]`; controlar "chequera agotada" (§C).
- **Descuento de saldo greenfield**: hoy **nada** descuenta `cta_pag_saldo` al pagar; se diseña desde
  cero en la Orden de Pago, en la **misma transacción** que la OP.
- **Nombres de constraint** con doble token (`orden_pagoorden_pago_cabecera_fk`) — cosmético, las FKs
  son correctas.
- **Montos `INTEGER`** (overflow) — preexistente.

---

## 9. Fase posterior — lado Cobros (ventas)

Parte de Tesorería pero ligada al ciclo de **ventas**, fuera del foco inmediato (pagos):
`cobro`, `cobro_detalle`, `cobro_cheque`, `cobro_tarjeta`, `forma_cobro_cabecera/detalle`, `caja`,
`apertura_cierre_caja`, `arqueo_caja`, `recaudaciones_depositar(_detalle)`, `cheque_recibido`,
`titular`, `tarjeta`, `tipo_tarjeta`. Flujo: cobro (efectivo/cheque/tarjeta) → caja/arqueo →
recaudación a depositar → **crédito bancario** → conciliación. Se planifica cuando se aborde Ventas.

---

## 10. Resumen

| Fase | Sub-módulo | Depende de |
|---|---|---|
| 1 | Referenciales bancarios (cuenta, entidad, moneda…) | — |
| 2 | Provisión de cuenta a pagar (+ neteo NC) | cuenta_pagar (✅) |
| 3 | Orden de pago + formas de pago (descuenta saldo) | Provisión, Cuenta bancaria |
| 4 | Débitos / Créditos | Cuenta bancaria |
| 5 | Fondo Fijo + rendición | Provisión, Orden de pago |
| 6 | Conciliación bancaria | OP, Débitos, Créditos |
| 7 | (Cobros/Caja) | Ventas |

Con **Referenciales** y **Provisión** ya terminados y el **esquema de la OP listo**, el camino corto hacia la
**conciliación bancaria** pedida es: **Referenciales → Provisión → Orden de Pago → Débitos/Créditos →
Conciliación**, reutilizando en cada paso el patrón transaccional Session+Token de Compras.

---

## 11. Alineación de entidades (POJOs) con la BD nueva

Verificación tras los ajustes de esquema del 2026-07-21 (Power Architect). Las entidades del flujo OP→Conciliación:

| POJO | Estado | Acción requerida |
|---|---|---|
| `OrdenPago` | ⚠️ **Desalineado** | **Quitar** `idCheque` y `idCuenta` (campos + constructor + getters/setters) — esas columnas se eliminaron de `orden_pago_cabecera`. |
| `OrdenPagoDetalle` | ⚠️ **Incompleto** | **Agregar** `idOrdenPagoDet` (Long) — nuevo serial PK `id_orden_pago_det`. Ya mapea `monto`/`cuentaPagar`/`facturaCompra`/`ordenPago`. |
| `FormaPagoDetalle` | ⚠️ **Desalineado** | **Quitar** `transferencia` y `cheque` (Long, montos que ya no existen); **agregar** una referencia `Cheque cheque` (o `Long idCheque`) para la FK `id_cheque` nullable. Conserva `monto`/`estado`/`referencia`/`cuenta`/`fecha`/`formaPagoCabecera`/`ordenPago`. |
| `Cheque` | ✅ **Alineado** | Coincide 1:1 con `cheque` (numero, fechaEmision, estado, chequera, aLaOrden, observacion, tipoCheque, fechaPago, fechaVencimiento, usuario). |
| `Chequera` | ✅ **Alineado** | Coincide con `chequera` (cuenta, serie, desdeNumero, hastaNumero). |
| `FormaPagoCabecera` | ✅ **Alineado** | Coincide con `forma_pago_cabecera` (idFormaPagoCabecera, descripcion). |
| `CuentaPagar` | ✅ **Alineado** | Con `plazo` ya agregado (sesión previa). |

> **Los DAOs viejos también quedan desalineados** (referencian columnas eliminadas): `OrdenPagoDAO`
> (usa `id_cheque`/`id_cuenta` en sus SQL — se reescribe completo en §C) y cualquier query sobre
> `forma_pago_detalle` que use `forma_pag_cheque`/`forma_pag_transferencia`. Al no existir aún
> `FormaPagoDetalleDAO`, solo hay que crearlo alineado.
