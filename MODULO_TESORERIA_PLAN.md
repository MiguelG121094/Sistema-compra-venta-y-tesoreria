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

**Pendiente:** **Orden de Pago (§C — próximo paso)**, Débitos/Créditos (§D), Fondo Fijo (§E),
Conciliación (§F), y el resto de referenciales/entidades. El modelo (POJOs) sigue 100% completo.

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

### C. Orden de pago *(donde sale el dinero — ⏳ PRÓXIMO PASO, EN DISEÑO)*

**Tablas:** `orden_pago_cabecera`, `orden_pago_detalle`, `forma_pago_cabecera`, `forma_pago_detalle`,
`cuenta`, `cheque`.

**Prototipo** (`webapp/Images/Prototipo Orden de pago.png`). Es como Factura/Provisión pero para la OP:
- **Botones:** Nuevo · Buscar Orden de pago · Buscar Provisión · Anular.
- **Cabecera:** Nro OP · Recibo Nro (`ord_pag_nro_recibo`) · Fecha · Estado · Sucursal ·
  **Banco** + **Nro. de cuenta** + **Moneda** (la `cuenta` bancaria de donde sale la plata) ·
  **Tipo de cambio** · **Cheque Nro** · **Provisión Nro** · **Detalle de Pago** (la forma de pago) ·
  Razón Social.
- **Detalle** = las facturas de la provisión, **SOLO LECTURA** (Item, Nro factura, Importe total,
  Saldo pendiente, Importe a pagar, Plazo). Sin Editar/Eliminar (vienen fijas de la provisión).
- **Importe Total a Pagar** + Generar / Cancelar.

**Flujo:**
1. Nuevo → Buscar Provisión → carga sus facturas (detalle) + proveedor (Razón Social) + Provisión Nro.
2. Completar cabecera: Recibo, Sucursal, **Cuenta bancaria** (Banco+Nro cuenta), Moneda, Tipo de cambio,
   forma de pago (Detalle de Pago) y Cheque Nro (si aplica).
3. **Generar** → crea la OP + su detalle + la forma de pago, y **descuenta el `cta_pag_saldo`** de cada
   factura de la provisión (transaccional). `ord_pag_tipo_pago` = reposición FF vs otros gastos.

**Decisiones (sesión 2026-07-17):**

| # | Decisión | Resultado |
|---|---|---|
| C1 | ¿Una OP paga toda la provisión o una factura? | ✅ **Toda la provisión** (varias facturas). ⚠️ Implica ajustar `orden_pago_detalle` (ver abajo). |
| C2 | ¿La OP inserta el movimiento en la conciliación? | ✅ **NO.** La OP solo queda como movimiento; la **conciliación es un módulo periódico posterior** que jala las OPs del período (no hay cabecera de conciliación al momento de la OP). Ver ejemplo abajo. |
| C3 | Formas de pago | Por el prototipo, **una forma de pago por OP** (Detalle de Pago), no mixtas. |
| C4 | Cheque | **Solo referencia** (Cheque Nro en `forma_pag_referencia`), sin gestionar chequera/emisión. La emisión real de cheques se hace en su módulo aparte. Ver ejemplo abajo. |

> **Estado:** las 4 decisiones fueron acordadas verbalmente; **falta que el usuario confirme y
> elija la PK de `orden_pago_detalle`** (ver ⚠️) antes de construir.

**⚠️ Ajuste de esquema requerido — `orden_pago_detalle`:** como una OP paga **varias** facturas,
necesita **varias filas**. Hoy su **PK es solo `id_orden_pago`** (1 fila por OP). Hay que ajustarla en
Power Architect, opción **recomendada**: agregar un serial **`id_orden_pago_det`** como PK (consistente
con `provision_cuenta_pagar_detalle`); alternativa: PK compuesta `(id_orden_pago, id_cta_pagar,
id_fact_comp_cab)`. **Pendiente de confirmar con el usuario.**

**Ejemplo — Conciliación (por qué la OP NO la inserta):**
```
1. Generás OP #10: pagás 150.000 con transferencia desde la cuenta Itaú (01/07).
   → queda como orden_pago_cabecera (un egreso del banco). El saldo de las facturas se descuenta acá.
2. A fin de mes armás la CONCILIACIÓN de Itaú (período 01/07–31/07): creás conciliacion_bancaria
   (cabecera, con saldo inicial/final y saldo del extracto). Ese módulo trae los movimientos:
       conciliacion_bancaria_detalle:
         item 1 | id_orden_pago=10 | tipo='Deb'  | 150.000 | conciliado=false
         item 2 | id_debitos=5     | tipo='Deb'  | 20.000  | conciliado=false  (comisión banco)
         item 3 | id_creditos=8    | tipo='Cred' | 500.000 | conciliado=true   (depósito)
   → marcás cada ítem contra el extracto.
```
`conciliacion_bancaria_detalle` depende de una `conciliacion_bancaria` (cabecera) que **no existe** al
guardar la OP → por eso la OP no inserta ahí; el módulo de Conciliación (§F) levanta las OPs del período.

**Ejemplo — Cheque:**
```
Opción A (elegida) — Solo referencia:
   OP #10, forma=Cheque, Cheque Nro=1234567
   → forma_pago_detalle: forma_pag_cheque=150.000, forma_pag_referencia='1234567', id_cuenta=Itaú
   → NO se crea registro en la tabla 'cheque'.

Opción B (descartada por ahora) — Emitir cheque real:
   → toma una chequera de Itaú, saca el próximo nro, crea registro en 'cheque'
     (chq_a_la_orden='Paresa', chq_estado='Emitido'...), enlaza orden_pago_cabecera.id_cheque.
   → requiere el ABM de Chequera/Cheque primero.
```

**Componentes a crear:** `OrdenPagoDAO` (hoy solo CRUD → **transaccional** con descuento de saldo +
inserción de detalle N filas), `OrdenPagoDetalleDAO`, `FormaPagoCabeceraDAO`/`FormaPagoDetalleDAO`,
`OrdenPagoService` (orquesta OP + detalle + forma de pago + descuento del saldo de cada factura),
`OrdenPagoServlet` (Session+Token, calcado de Provisión/Factura), `ordenPago.jsp` (el prototipo).
Reusar `CuentaService`/`MonedaService`/`EntidadFinancieraService` para los combos, `ProvisionCuentaPagarService`
para Buscar Provisión.

**Descuento del saldo:** al Generar, por cada factura de la provisión hacer
`cta_pag_saldo -= importe_a_pagar` (usar/agregar un método en `CuentaPagarDAO`, recalculando estado con
`calcularEstadoPorSaldo`, en la misma transacción). La cuenta que estaba `En provision` pasa a
`Pendiente`/`Cancelado` según el saldo resultante.

> Al poblar `orden_pago_detalle`, **`tienePagosAplicados` empieza a funcionar de verdad**: el guard de
> "no editar/anular factura con pagos aplicados" (ver `NOTA_CREDITO_DEBITO_PLAN.md` §8.4) detecta el pago.

> **📍 DÓNDE NOS QUEDAMOS (para retomar):** Provisión terminada y probada. Orden de Pago **en diseño**:
> prototipo analizado, decisiones C1–C4 acordadas. **Antes de codear falta:** (1) que el usuario
> **confirme las 4 decisiones**, (2) que **elija la PK de `orden_pago_detalle`** y la ajuste en Power
> Architect. Con eso se construye el DAO/Service/Servlet/JSP de la OP como está descrito arriba.

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
| 6 | Formas de pago por OP | **Una** forma de pago por OP (según prototipo), cheque **solo referencia** | ⏳ A confirmar con el usuario (§C, C3/C4) |
| 7 | PK de `orden_pago_detalle` | Agregar serial `id_orden_pago_det` (una OP → N facturas de la provisión) | ⏳ A confirmar + ajustar en Power Architect (§C ⚠️) |
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

**C. Orden de pago** — ⏳ PRÓXIMO (ver §C; bloqueado hasta confirmar C3/C4 + PK de `orden_pago_detalle`)
- [ ] ⚠️ **Ajustar PK de `orden_pago_detalle`** en Power Architect (serial `id_orden_pago_det`) — N facturas por OP
- [ ] `OrdenPagoDAO` → transaccional con **descuento de `cta_pag_saldo`** + recálculo de estado (método en `CuentaPagarDAO`)
- [ ] `OrdenPagoDetalleDAO`, `FormaPagoCabeceraDAO`, `FormaPagoDetalleDAO`
- [ ] `OrdenPagoService` (orquesta OP + detalle N filas + forma de pago + descuento de saldo). **NO** inserta conciliación
- [ ] `OrdenPagoServlet` (Session+Token) + `ordenPago.jsp` (prototipo); validar **sin efectivo**, **provisión previa obligatoria**
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
- **`orden_pago_detalle`** tiene PK = solo `id_orden_pago` (1 detalle por orden), pese a listar la
  cuenta a pagar — revisar si el diseño requiere N cuentas por orden (posible ajuste de PK).
- **`fondo_fijo_rendicion(_detalle)`** sin secuencia serial en la PK.
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

El módulo está **verde en capa web** (solo POJOs + `CuentaPagarDAO`). El camino corto hacia la
**conciliación bancaria** pedida es: **Referenciales → Provisión → Orden de Pago → Débitos/Créditos →
Conciliación**, reutilizando en cada paso el patrón transaccional Session+Token de Compras.
