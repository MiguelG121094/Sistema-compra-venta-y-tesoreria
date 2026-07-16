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
El modelo (POJOs) está completo, pero **la lógica está casi toda por hacer**:

| Componente | Estado |
|---|---|
| **POJOs (modelo)** | ✅ 100% (todas las entidades existen) |
| **DAO con lógica** | Solo `CuentaPagarDAO` (`ajustarSaldoPorNota`, `tienePagosAplicados`, `getByFactura`, `listarCuentasPagarPendientes`) |
| **DAO CRUD** | `CuentaCobrarDAO`, `OrdenPagoDAO` (solo CRUD), `CobroDAO` (CRUD), `CajaDAO`, `AperturaCierreCajaDAO`, `DepositoDAO` |
| **Sin DAO/Service** | Provisión, OrdenPagoDetalle, FormaPago(Cab/Det), Moneda, TipoEntidadFinanciera, EntidadFinanciera, TipoCuenta, Cuenta, Débito, Crédito, Cheque, Chequera, TipoCheque, ChequeRecibido, Titular, FondoFijo, Rendición(+Detalle), Conciliación(+Detalle), Tarjeta, ArqueoCaja, RecaudacionDepositar |
| **Servlets de tesorería** | ❌ **Ninguno** |
| **JSPs de tesorería** | ❌ **Ninguno** (el menú "Módulo Tesorería" apunta a placeholders `.html` de la plantilla: `layout-static.html`, `layout-sidenav-light.html`, `register.html`) |

**Conclusión:** salvo `cuenta_pagar` (compartida con Compras/NC), **todo Tesorería está en cero de
capa web**. Es un módulo a construir prácticamente desde los POJOs.

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

> **Decisión abierta:** ¿la provisión **descuenta** el `cta_pag_saldo`, o solo lo **reserva** y el
> descuento ocurre en la Orden de Pago? Propuesta: la provisión **agrupa/reserva** (cambia estado de
> la `cuenta_pagar` a "En provisión") y el **descuento real del saldo ocurre en la Orden de Pago**
> (donde efectivamente sale el dinero). Confirmar.

---

### C. Orden de pago + formas de pago *(donde sale el dinero)*

**Tablas:** `orden_pago_cabecera`, `orden_pago_detalle`, `forma_pago_cabecera`, `forma_pago_detalle`,
`cuenta`, `cheque`.

**Flujo:**
1. Partir de una **provisión** existente (regla: no hay OP sin provisión).
2. Generar la **orden de pago** (monto = neto de la provisión; referencia `id_cuenta`, `id_moneda`,
   `id_proveedor`, `id_sucursal`).
3. Elegir **una o varias formas de pago** (`forma_pago_detalle`): **cheque** y/o **transferencia**
   (nunca efectivo). Cada forma referencia la `cuenta` (banco) y una `forma_pag_referencia`
   (nro de cheque / transferencia).
4. Si la forma es **cheque**: emitir el `cheque` (contra una `chequera` de la cuenta) y enlazarlo
   (`orden_pago_cabecera.id_cheque`).
5. **Descontar el `cta_pag_saldo`** de las cuentas a pagar incluidas (transaccional). ⚠️ Esta es la
   **lógica greenfield del descuento de saldo** — hoy no existe (nada descuenta el saldo al pagar).
6. Insertar el/los ítems en **`conciliacion_bancaria_detalle`** (ver §F): el pago resta del banco.

**`ord_pag_tipo_pago`** distingue **reposición de fondo fijo** vs **otros gastos** (viene del
`fact_comp_tipo_factura`).

**Componentes:** `OrdenPagoDAO` (hoy solo CRUD → volver **transaccional** con descuento de saldo),
`OrdenPagoDetalleDAO`, `FormaPagoCabeceraDAO` + `FormaPagoDetalleDAO`, `ChequeDAO`/`ChequeraDAO`,
`OrdenPagoService` (orquesta la transacción), `OrdenPagoServlet` (Session+Token), JSP.

> Al implementar esto, **`tienePagosAplicados` empieza a funcionar de verdad**: al poblar
> `orden_pago_detalle`, el guard de "no editar/anular factura con pagos aplicados" ya detecta pagos
> reales (ver `NOTA_CREDITO_DEBITO_PLAN.md` §8.4).

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
| 1 | Patrón de servlets | Session + Token + Service transaccional (calcar Factura de Compra) | Propuesta |
| 2 | ¿Dónde se descuenta `cta_pag_saldo`? | En la **Orden de Pago** (no en la provisión); la provisión solo reserva/agrupa | **A confirmar** |
| 3 | Neteo del saldo a favor de NC | En la **provisión** (filtro `saldo ≠ 0`, líneas negativas, neto ≥ 0) — ver NC plan §4 | ✅ Decidido (NC plan) |
| 4 | Estados de `cuenta_pagar` en el flujo | `Pendiente` → `En provisión` → `Cancelado`/`Pagado` (+ `Saldo a favor` de NC) | A confirmar |
| 5 | Conciliación: ¿la OP inserta el ítem, o se concilia manual? | La OP **inserta** el ítem en `conciliacion_bancaria_detalle` al guardarse (según el comment de BD) | A confirmar |
| 6 | Fondo Fijo: flujo | 2ª opción del comment: `FACTURA FF → RENDICIÓN → PROVISIÓN → OP` | A confirmar |
| 7 | Montos `INTEGER` | Riesgo de overflow (~2.147 mill.) — preexistente, se mantiene | Aceptado |

---

## 6. Orden de implementación recomendado

1. **Referenciales bancarios** (A) — moneda, tipo entidad, entidad financiera, tipo cuenta, **cuenta**. *(Prerrequisito.)*
2. **Provisión de cuenta a pagar** (B) — con **neteo del saldo a favor** de NC. *(Cierra el circuito de NC/ND.)*
3. **Orden de pago + formas de pago** (C) — descuento de saldo + cheque/transferencia + ítem de conciliación.
4. **Débitos / Créditos** (D) — ABM de movimientos bancarios.
5. **Fondo Fijo + rendición** (E) — reutiliza B y C.
6. **Conciliación bancaria** (F) — el objetivo.
7. *(Fase posterior)* **Lado cobros/caja** (§9).

---

## 7. Checklist de implementación (alto nivel)

**A. Referenciales bancarios**
- [ ] DAO+Service+Servlet+JSP: `Moneda`, `TipoEntidadFinanciera`, `EntidadFinanciera`, `TipoCuenta`, `Cuenta`

**B. Provisión**
- [ ] `CuentaPagarDAO`: método `listarCuentasPagarPorProveedor(idProveedor)` con **saldo ≠ 0** (incluye negativos)
- [ ] `ProvisionCuentaPagarDAO` (+ detalle) transaccional
- [ ] `ProvisionCuentaPagarService` (dueño de la tx)
- [ ] `ProvisionCuentaPagarServlet` (Session+Token) + JSP; validar **neto ≥ 0**
- [ ] Registrar `/ProvisionCuentaPagarServlet` en `AuthorizationFilter` (módulo `tesoreria`)

**C. Orden de pago**
- [ ] `OrdenPagoDAO` → transaccional con **descuento de `cta_pag_saldo`** + recálculo de estado
- [ ] `OrdenPagoDetalleDAO`, `FormaPagoCabeceraDAO`, `FormaPagoDetalleDAO`, `ChequeDAO`, `ChequeraDAO`
- [ ] `OrdenPagoService` (orquesta OP + formas + cheque + descuento + ítem conciliación)
- [ ] `OrdenPagoServlet` + JSP; validar **sin efectivo**, **provisión previa obligatoria**

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
