# Sistema de Compra, Venta y Tesorería

## Descripción General

Sistema ERP desarrollado en Java EE para la gestión integral de:
- **Compras**: Pedidos, presupuestos, órdenes de compra, facturas de compra
- **Ventas**: Pedidos, facturas de venta, notas de remisión
- **Tesorería**: Cuentas bancarias, cheques, cobros, pagos, conciliación bancaria (maneja todo el flujo de dinero)
- **Inventario**: Artículos, stock, ajustes de stock
- **Seguridad**: Usuarios, grupos, permisos por módulo

> **Alcance fiscal/contable:** el sistema **no incluye contabilidad** (asientos, plan de cuentas, balances, estados financieros). Solo roza el área fiscal mediante el **Libro IVA de Compra y de Venta** (más Timbrado y Tipo de Comprobante como soporte). Hasta ahí llega el alcance contable.

---

## Arquitectura del Proyecto

```
src/main/java/
├── modelo/           # Entidades (POJOs)
├── controlador/      # Servlets
├── service/          # Servicios REST (JAX-RS)
└── conexion/         # Conexión a base de datos

src/main/webapp/
├── *.jsp             # Vistas JSP
├── Bootstrap 5.3.3/  # Framework CSS
├── DataTables 2/     # Plugin para tablas
├── Theme/            # Estilos personalizados
└── toastr/           # Notificaciones
```

---

## Base de Datos

**Motor**: PostgreSQL
**Archivo SQL**: `Base de datos Taller 3ro.sql`

### Total de Tablas: 85

---

## Estado de las Entidades Java

### Entidades Completas (85/85 - 100%)

#### Módulo de Compras
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| PedidoCompra | pedido_compra_cabecera | ✅ |
| PedidoCompraDetalle | pedido_compra_detalle | ✅ |
| Presupuesto | presupuesto_cabecera | ✅ |
| PresupuestoDetalle | presupuesto_detalle | ✅ |
| OrdenCompra | orden_compra_cabecera | ✅ |
| OrdenCompraDetalle | orden_compra_detalle | ✅ |
| FacturaCompra | factura_compra_cabecera | ✅ |
| FacturaCompraDetalle | factura_compra_detalle | ✅ |
| NotaCreditoCompra | nota_credito_compra_cabecera | ✅ |
| NotaCreditoCompraDetalle | nota_credito_compra_detalle | ✅ |
| NotaDebitoCompra | nota_debito_compra_cabecera | ✅ |
| NotaDebitoCompraDetalle | nota_debito_compra_detalle | ✅ |
| NotaRemisionCompra | nota_remision_cabecera | ✅ |
| NotaRemisionCompraDetalle | nota_remision_detalle | ✅ |

#### Módulo de Ventas
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| PedidoVenta | pedido_venta_cabecera | ✅ |
| PedidoVentaDetalle | pedido_venta_detalle | ✅ |
| FacturaVenta | factura_venta_cabecera | ✅ |
| FacturaVentaDetalle | factura_venta_detalle | ✅ |
| NotaRemisionVenta | nota_remision_venta_cabecera | ✅ |
| NotaRemisionVentaDetalle | nota_remision_venta_detalle | ✅ |
| NotaCreditoVenta | nota_credito_venta_cabecera | ✅ |
| NotaCreditoVentaDetalle | nota_credito_venta_detalle | ✅ |
| NotaDebitoVenta | nota_debito_venta_cabecera | ✅ |
| NotaDebitoVentaDetalle | nota_debito_venta_detalle | ✅ |

#### Módulo de Tesorería - Cuentas
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| CuentaPagar | cuenta_pagar | ✅ |
| CuentaCobrar | cuenta_cobrar | ✅ |
| ProvisionCuentaPagar | provision_cuenta_pagar | ✅ |
| ProvisionCuentaPagarDetalle | provision_cuenta_pagar_detalle | ✅ |

#### Módulo de Tesorería - Bancos
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| Moneda | moneda | ✅ |
| TipoEntidadFinanciera | tipo_entidad_financiera | ✅ |
| EntidadFinanciera | entidad_financiera | ✅ |
| TipoCuenta | tipo_cuenta | ✅ |
| Cuenta | cuenta | ✅ |
| ConciliacionBancaria | conciliacion_bancaria | ✅ |
| ConciliacionBancariaDetalle | conciliacion_bancaria_detalle | ✅ |
| Debito | debitos | ✅ |
| Credito | creditos | ✅ |

#### Módulo de Tesorería - Cheques
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| TipoCheque | tipo_cheque | ✅ |
| Chequera | chequera | ✅ |
| Cheque | cheque | ✅ |
| ChequeRecibido | cheque_recibido | ✅ |
| Titular | titular | ✅ |

#### Módulo de Tesorería - Tarjetas
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| TipoTarjeta | tipo_tarjeta | ✅ |
| Tarjeta | tarjeta | ✅ |

#### Módulo de Tesorería - Cobros y Pagos
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| Cobro | cobro | ✅ |
| CobroDetalle | cobro_detalle | ✅ |
| CobroTarjeta | cobro_tarjeta | ✅ |
| CobroCheque | cobro_cheque | ✅ |
| FormaCobro | forma_cobro_cabecera | ✅ |
| FormaCobroDetalle | forma_cobro_detalle | ✅ |
| OrdenPago | orden_pago_cabecera | ✅ |
| OrdenPagoDetalle | orden_pago_detalle | ✅ |
| FormaPagoCabecera | forma_pago_cabecera | ✅ |
| FormaPagoDetalle | forma_pago_detalle | ✅ |

#### Módulo de Tesorería - Caja
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| Caja | caja | ✅ |
| AperturaCierreCaja | apertura_cierre_caja | ✅ |
| ArqueoCaja | arqueo_caja | ✅ |
| RecaudacionDepositar | recaudaciones_depositar | ✅ |
| RecaudacionDepositarDetalle | recaudaciones_depositar_detalle | ✅ |

#### Módulo de Tesorería - Fondo Fijo
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| FondoFijo | fondo_fijo | ✅ |
| FondoFijoRendicion | fondo_fijo_rendicion | ✅ |
| FondoFijoRendicionDetalle | fondo_fijo_rendicion_detalle | ✅ |

#### Módulo de Inventario
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| Articulo | articulo | ✅ |
| TipoArticulo | tipo_articulo | ✅ |
| Marca | marca | ✅ |
| Presentacion | presentacion | ✅ |
| TipoImpuesto | impuesto | ✅ |
| Grupo | grupo | ✅ |
| Deposito | deposito | ✅ |
| Stock | stock | ✅ |
| MotivoAjuste | motivo_ajuste | ✅ |
| AjusteStockCabecera | ajuste_stock_cabecera | ✅ |
| AjusteStockDetalle | ajuste_stock_detalle | ✅ |

#### Soporte Fiscal (no es contabilidad)
> El sistema **no implementa contabilidad**. Estas entidades solo cubren el aspecto fiscal hasta el Libro IVA:

| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| LibroIvaVenta | libro_iva_venta | ✅ |
| LibroIvaCompra | libro_iva_compra | ✅ |
| TipoComprobante | tipo_comprobante | ✅ |
| Timbrado | timbrado | ✅ |

#### Módulo de Personas
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| Persona | persona | ✅ |
| TipoEntidad | tipo_entidad | ✅ |
| Cliente | cliente | ✅ |
| Proveedor | proveedor | ✅ |
| Sucursal | sucursal | ✅ |

#### Módulo de Seguridad
| Entidad | Tabla SQL | Estado |
|---------|-----------|--------|
| Usuario | usuario | ✅ |
| Modulo | modulo | ✅ |
| Permiso | permiso | ✅ |

---

## Pendientes por Desarrollar

### 1. DAOs (Data Access Objects)

**Stock**: Nota — se decidió que el stock se gestiona vía **triggers PL/pgSQL** en PostgreSQL (ver `Procedimientos y Triggers para BD.sql`), no vía DAO en Java. Por eso `StockDAO` no figura en pendientes.

Faltan crear los DAOs para las siguientes entidades:

```
[x] PermisoDAO ✅ (creado 2026-02-06)
[x] LibroIvaCompraDAO ✅ (creado en refactor transaccional de Factura Compra)
[ ] LibroIvaVentaDAO
[ ] NotaRemisionCompraDAO
[ ] NotaRemisionCompraDetalleDAO
[x] MonedaDAO ✅ (referenciales bancarios, 2026-07)
[ ] TipoEntidadFinancieraDAO
[x] EntidadFinancieraDAO ✅ (2026-07)
[x] TipoCuentaDAO ✅ (2026-07)
[x] CuentaDAO ✅ (2026-07)
[x] TipoChequeDAO ✅ (combo de la Orden de Pago, 2026-07)
[x] ChequeraDAO ✅ (lectura de chequera/rango + próximo N° de cheque, 2026-07)
[x] ChequeDAO ✅ (emisión/anulación/lectura de cheque, 2026-07)
[ ] ChequeRecibidoDAO
[ ] TitularDAO
[ ] MotivoAjusteDAO
[ ] AjusteStockCabeceraDAO
[ ] AjusteStockDetalleDAO
[ ] ModuloDAO
[x] FormaPagoCabeceraDAO ✅ (catálogo cheque/transferencia, 2026-07)
[x] FormaPagoDetalleDAO ✅ (N formas por OP, con cheque y tipo de cambio, 2026-07)
[ ] FormaCobroDAO
[ ] FormaCobroDetalleDAO
[ ] CobroDetalleDAO
[x] OrdenPagoDetalleDAO ✅ (N facturas por OP, FK compuesta a cuenta_pagar, 2026-07)
[ ] TipoTarjetaDAO
[ ] TarjetaDAO
[ ] ConciliacionBancariaDAO
[ ] ConciliacionBancariaDetalleDAO
[ ] DebitoDAO
[ ] CreditoDAO
[ ] RecaudacionDepositarDAO
[ ] RecaudacionDepositarDetalleDAO
[ ] TipoComprobanteDAO
[x] ProvisionCuentaPagarDAO ✅ (cabecera + detalle en el mismo DAO, 2026-07)
[—] ProvisionCuentaPagarDetalleDAO — no se creó: el detalle vive dentro de `ProvisionCuentaPagarDAO`
[ ] FondoFijoDAO
[ ] FondoFijoRendicionDAO
[ ] FondoFijoRendicionDetalleDAO
[ ] CobroTarjetaDAO
[ ] CobroChequeDAO
[ ] ArqueoCajaDAO
```

### 2. Services (REST)
Faltan crear los servicios REST para las nuevas entidades:

```
[x] PermisoService ✅ (creado 2026-02-06)
[x] LibroIvaCompraService ✅ (creado en refactor transaccional)
[ ] LibroIvaVentaService
[x] MonedaService ✅ (2026-07)
[x] EntidadFinancieraService ✅ (2026-07)
[x] CuentaService ✅ (2026-07)
[x] TipoCuentaService · FormaPagoCabeceraService · TipoChequeService · ChequeraService ✅ (combos de tesorería, 2026-07)
[x] ProvisionCuentaPagarService · OrdenPagoService ✅ (transaccionales, 2026-07)
[ ] ChequeService (hoy `ChequeDAO` se usa dentro de la transacción de la OP; no hace falta un Service propio hasta que haya un ABM de cheques)
[ ] AjusteStockService
[ ] ConciliacionBancariaService
[ ] FondoFijoService
[ ] ArqueoCajaService
... (y demás servicios)
```

### 3. Servlets (Controladores)
Faltan crear los controladores para las nuevas funcionalidades:

```
[x] CuentaServlet ✅ (cuentas bancarias, 2026-07 — reemplaza el "CuentaBancariaServlet" planeado)
[x] ProvisionCuentaPagarServlet ✅ (2026-07)
[x] OrdenPagoServlet ✅ (2026-07 — Session+Token, pendiente de prueba end-to-end)
[ ] ChequeServlet (ABM de cheques/chequeras; hoy los cheques se emiten desde la Orden de Pago)
[ ] DebitoCreditoServlet (movimientos bancarios)
[ ] StockServlet
[ ] AjusteStockServlet
[ ] ConciliacionBancariaServlet
[ ] FondoFijoServlet
[ ] ArqueoCajaServlet
... (y demás servlets)
```

### 4. Vistas JSP
Faltan crear las vistas para las nuevas funcionalidades:

```
[⚠] notaRemision.jsp        (vista inicial creada, sin servlet aún)
[x] notaCreditoDebito.jsp   ✅ (Nota Crédito/Débito Compra, cableada a `NotaCreditoDebitoServlet` — 2026-07)
[x] cuenta.jsp              ✅ (cuentas bancarias, 2026-07 — reemplaza el "cuentaBancaria.jsp" planeado)
[x] provision.jsp           ✅ (provisión de cuenta a pagar, 2026-07)
[x] ordenPago.jsp           ✅ (orden de pago con carrito de formas de pago, 2026-07)
[ ] cheque.jsp
[ ] stock.jsp
[ ] ajusteStock.jsp
[ ] conciliacionBancaria.jsp
[ ] fondoFijo.jsp
[ ] arqueoCaja.jsp
[ ] libroIva.jsp
... (y demás vistas)
```

### 5. Funcionalidades por Implementar

#### Compras
- [x] Triggers de stock al guardar/anular Factura Compra ✅ (2026-03)
- [x] Libro IVA Compra con preservación al anular ✅ (2026-03)
- [x] Sincronización Cuenta a Pagar al editar/anular Factura ✅ (2026-03)
- [x] Validación de plazo en facturas a crédito ✅ (2026-04)
- [⚠] Nota de Remisión Compra (vista inicial creada)
- [x] Nota Crédito Compra ✅ (2026-07 — `NotaCreditoDebitoServlet` + `notaCreditoDebito.jsp`; ajusta `cuenta_pagar` con **saldo negativo permitido** (Enfoque 1 c/ neteo en la provisión), fila propia en el Libro IVA y triggers de stock por devolución. Validación de **cantidad devuelta ≤ comprada** ✅ 2026-08-13 — ver [plan](NOTA_CREDITO_DEBITO_PLAN.md) §5.3)
- [x] Nota Débito Compra ✅ (2026-07 — mismo servlet/vista; aumenta el saldo de `cuenta_pagar` y registra su fila en el Libro IVA; no mueve stock)

#### Tesorería *(plan detallado: [MODULO_TESORERIA_PLAN.md](MODULO_TESORERIA_PLAN.md))*
- [x] CRUD de Cuentas Bancarias ✅ (2026-07 — `CuentaServlet` + `cuenta.jsp`, con seed de moneda/entidad financiera/tipo de cuenta)
- [x] Provisión de Cuenta a Pagar ✅ (2026-07 — reserva las cuentas y netea el saldo a favor de las NC; ver plan §B)
- [x] Orden de Pago ✅ (2026-07 — N formas de pago mixtas transferencia/cheque multi-cuenta, descuenta `cta_pag_saldo`, consume la provisión y anula con reversa total; ver plan §C). **Probada end-to-end el 2026-08-17.**
- [x] Emisión de Cheques ✅ (2026-07 — se emiten desde la Orden de Pago, con N° tomado del rango de la chequera)
- [x] Entrega de cheques al proveedor ✅ (2026-08-17 — se registra desde la Orden de Pago: estado `'Entregado'`, `chq_fecha_entrega`, `chq_entregado_a` y el N° de recibo, todo en una transacción; ver plan §G2)
- [ ] Débitos / Créditos bancarios (plan §D) — **próximo**
- [x] Gestión de Chequeras ✅ (2026-08-31 — `ChequeraServlet` + `chequera.jsp`, calcados de Cuentas Bancarias; validan solapamiento de rangos y muestran el consumo de la chequera; ver plan §G1)
- [ ] Gestión de Fondo Fijo + Rendición (plan §E)
- [ ] Conciliación Bancaria (plan §F — el objetivo final)
- [ ] Recepción de Cheques, Arqueo de Caja, Recaudaciones a Depositar *(lado cobros — requiere Ventas; plan §9)*
- [ ] UI de Cuenta a Pagar (backend ya integrado con Factura Compra)

#### Inventario
- [x] Control de Stock por Depósito vía triggers PL/pgSQL ✅ (2026-03)
- [ ] UI de visualización de stock
- [ ] Ajustes de Stock (entrada/salida) — UI
- [ ] Stock mínimo/máximo con alertas

#### Soporte Fiscal (no hay módulo de contabilidad)
- [x] Libro IVA Compras (backend integrado en Factura Compra) ✅
- [ ] Libro IVA Ventas
- [ ] UI consulta Libro IVA
- _Fuera de alcance: asientos contables, plan de cuentas, balances y estados financieros._

#### Seguridad
- [ ] Gestión de Módulos (UI para ABM de módulos)
- [ ] Asignación de Permisos por Grupo (UI para editar permisos)
- [x] Control de acceso por módulo (CRUD) ✅ (implementado 2026-02-06 con AuthorizationFilter)

---

## Flujos de Negocio Principales

### Flujo de Compras
```
Pedido Compra → Presupuesto (cotización) → Orden Compra → Factura Compra → Cuenta a Pagar
```

### Flujo de Ventas
```
Pedido Venta → Factura Venta → Nota Remisión → Cuenta a Cobrar
```

### Flujo de Pagos
```
Cuenta a Pagar → Provisión → Orden de Pago (cheque/transferencia)
```

### Flujo de Cobros
```
Cuenta a Cobrar → Cobro (efectivo/cheque/tarjeta) → Recaudación → Depósito Bancario
```

### Flujo de Fondo Fijo
```
Factura Fondo Fijo → Rendición → Provisión → Orden de Pago (reposición)
```

---

## Notas Técnicas

### Patrón de Entidades
Todas las entidades siguen el patrón POJO:
- Constructor vacío
- Constructor con ID
- Constructor completo
- Getters y Setters

### Nomenclatura
- **Entidades Java**: PascalCase (ej: `PedidoCompra`)
- **Tablas SQL**: snake_case (ej: `pedido_compra_cabecera`)
- **Claves compuestas**: Entidades sin ID único usan objetos como PK

### Relaciones
- Las relaciones se manejan con objetos completos, no solo IDs
- Ejemplo: `private Proveedor proveedor;` en lugar de `private Long idProveedor;`

---

## Historial de Cambios

### 2026-08-31 — ABM de chequeras

Cierra el pendiente §G1 del plan de tesorería, que era el único que podía cortar la operación: las
chequeras venían del seed y, al agotarse el rango, la emisión de cheques de una orden de pago fallaba
con *"Chequera agotada"* sin forma de cargar otra desde la aplicación.

`ChequeraServlet` + `chequera.jsp` están calcados de Cuentas Bancarias, registrados en
`AuthorizationFilter` bajo el módulo `tesoreria` y enlazados desde el menú. No hizo falta tocar la base:
`chequera` y su POJO ya existían.

Más allá del alta simple, se resolvieron tres agujeros que la base no cubre (no tiene ningún `CHECK`):

- **Solapamiento de rangos.** El próximo número de cheque se calcula por chequera y no por cuenta, así
  que dos chequeras de la misma cuenta con rangos pisados emitirían dos cheques con el mismo número en
  el mismo banco. Se rechaza al insertar y al actualizar.
- **Rango contra lo ya emitido.** Al editar, el rango nuevo tiene que contener los cheques ya emitidos.
- **Eliminar.** `cheque.id_chequera` es FK `ON DELETE NO ACTION`: se corta antes con un mensaje claro en
  vez del error crudo de PostgreSQL.

La grilla muestra *Emitidos*, *Próximo N°* y *Disponibles*, con badge amarillo cuando quedan 10 o menos
y rojo cuando está agotada, para que el agotamiento se vea antes de que falle una orden de pago. Los
disponibles salen de `MAX(chq_numero)` y no del conteo, porque un número anulado no se reutiliza.

Las validaciones viven en `ChequeraService`, adentro de la transacción. De paso se corrigió que
`ChequeraDAO` no hidrataba la cuenta: el combo de chequera de la Orden de Pago mostraba la serie sin el
nombre del banco.

---

### 2026-08-17 al 28 — Entrega de cheques al proveedor y ajustes de la Orden de Pago

**Entrega de cheques (requerimiento 3.3).** Cierra el pendiente §G2 del plan de tesorería. Se agregaron
a `cheque` las columnas `chq_fecha_entrega` y `chq_entregado_a`, porque el estado `'Entregado'` solo no
alcanzaba: `chq_a_la_orden` dice a nombre de quién se emite el cheque, no quién lo retiró. La entrega se
registra desde la propia Orden de Pago, con un modal que recibe los cheques que se marcan (los diferidos
se retiran en otro momento) y que aparece cuando alguna forma de pago tiene cheque.

El **N° de recibo se movió a este momento**: lo emite el proveedor al cobrar, así que al generar la OP
todavía no existe. La cabecera nace en 0, el campo queda readonly y se completa al registrar la entrega,
en la misma transacción, porque entrega y recibo son el mismo acto administrativo.

- `ChequeDAO.registrarEntrega` excluye los anulados en el `WHERE` en vez de pisarles el estado, para que
  `'Anulado'` gane si la OP se anula después de la entrega.
- `OrdenPagoService.registrarEntregaCheques` es re-ejecutable: volver a guardar corrige una entrega mal
  cargada en vez de fallar. Rechaza la entrega sobre una OP anulada.
- La acción exige permiso de **edición**, no de alta: modifica una OP ya generada.

**Otros ajustes.** Se probó la Orden de Pago de punta a punta y se confirmó `forma_pag_tipo_cambio`
contra Power Architect (2026-08-17). Se agregó el seed de `forma_pago_cabecera` (Cheque / Transferencia)
y se corrigió `creditos.id_cobro` a nullable. En la vista (2026-08-27/28) se sacaron los textos
aclaratorios, se completó el modal de confirmación para eliminar una forma de pago —que se abre encima
del modal de formas y lo deja abierto— y se pasaron sus campos a etiqueta flotante.

---

### 2026-07-17 al 26 — Módulo de Tesorería: referenciales, Provisión y Orden de Pago

Se arranca el módulo de Tesorería siguiendo [`MODULO_TESORERIA_PLAN.md`](MODULO_TESORERIA_PLAN.md)
(fases A, B y C del plan). Todo calcado del patrón Session+Token / Service transaccional de
`FacturaCompraServlet`.

- **Referenciales bancarios (§A):** `CuentaServlet` + `cuenta.jsp` (ABM de cuentas bancarias), con
  `MonedaDAO`/`Service`, `TipoCuentaDAO`/`Service` y `EntidadFinancieraDAO`/`Service` para los combos.
  Seed cargado en `Inserts inciales.sql` (moneda, tipo/entidad financiera, tipo de cuenta, `cuenta`,
  `tipo_cheque`, `chequera`).
- **Provisión de cuenta a pagar (§B):** `ProvisionCuentaPagarDAO` (cabecera + detalle) +
  `ProvisionCuentaPagarService` transaccional + `ProvisionCuentaPagarServlet` + `provision.jsp`. La
  provisión **solo agrupa/reserva** (deja las cuentas en `'En provision'` **sin tocar el saldo**) y
  **netea el saldo a favor** de las Notas de Crédito (valida neto ≥ 0). `CuentaPagarDAO` gana
  `listarCuentasPagarPorProveedor` (saldo ≠ 0, incluye negativos), `marcarEnProvision`,
  `revertirProvision` y `calcularEstadoPorSaldo`; además se corrige la persistencia de `cta_pag_plazo`.
- **Orden de Pago (§C):** es donde **sale el dinero**. Esquema ajustado en Power Architect (seriales en
  `orden_pago_detalle`/`forma_pago_detalle`, `id_cheque` movido al detalle de formas, `id_cheque`/
  `id_cuenta` fuera de la cabecera) y POJOs alineados. `OrdenPagoService` hace todo en **una
  transacción**: inserta OP + detalle + N formas de pago, **emite los cheques reales** tomando el N°
  del rango de la chequera, **descuenta `cta_pag_saldo`** de cada factura y marca la provisión como
  `'Procesada'`. Incluye **guard anti doble-pago** (bloquea la provisión con `FOR UPDATE` y exige
  `'Pendiente'`) y **`anularOrdenPagoCompleta`** con reversa total (devuelve el saldo, anula los cheques
  y reactiva la provisión). La OP **no** inserta en conciliación: eso lo hará el módulo periódico (§F).
  Capa web: `ordenPago.jsp` (formulario único + JS, con **carrito de formas de pago** mixtas
  transferencia/cheque multi-cuenta) y `OrdenPagoServlet` (Session+Token). Se crean los combos que
  faltaban: `FormaPagoCabeceraDAO`/`Service`, `TipoChequeDAO`/`Service`, `ChequeraService`.
- **Decisión de moneda/tipo de cambio (2026-07-25):** la deuda (`factura_compra`/`cuenta_pagar`) no
  tiene moneda → es **Gs implícito**, así que `id_moneda` y `ord_pag_tipo_cambio` **se quitaron de
  `orden_pago_cabecera`**: la moneda la define la **cuenta bancaria** de cada forma de pago y el tipo de
  cambio pasó a `forma_pago_detalle.forma_pag_tipo_cambio` (por instrumento).
- **Permisos/menú:** `CuentaServlet`, `ProvisionCuentaPagarServlet` y `OrdenPagoServlet` registrados en
  `AuthorizationFilter` bajo el módulo `tesoreria`, con sus links en `menuLateral.jsp`.

Pendiente: **probar la Orden de Pago de punta a punta** (compilar/desplegar y validar el descuento de
saldo, el cheque emitido y la anulación), confirmar el nombre de columna `forma_pag_tipo_cambio` contra
Power Architect, y seguir con Débitos/Créditos (§D) → Fondo Fijo (§E) → Conciliación (§F).

### 2026-07-16 — Nota de Crédito / Débito de Compra (implementación completa)

Se implementa la capa Java/JSP planificada en [`NOTA_CREDITO_DEBITO_PLAN.md`](NOTA_CREDITO_DEBITO_PLAN.md),
cerrando el circuito de ajustes sobre una factura de compra ya emitida:

- **`NotaCreditoDebitoServlet`** (Session+Token, calcado de `FacturaCompraServlet`) enruta NC y ND desde
  un único formulario (`tipoNota`), y `notaCreditoDebito.jsp` queda **cableada**: busca la factura real,
  hereda sus líneas (cantidades editables a lo devuelto), calcula el IVA, hereda sucursal y condición
  como solo-lectura y condiciona los botones por permiso. Registrado en `AuthorizationFilter` (módulo
  `compra`).
- **DAOs/Services transaccionales** de NC y ND (`guardar/anular...Completa`, el Service dueño de la
  conexión), que en una sola transacción persisten la nota + su detalle y aplican los dos efectos:
  - **`cuenta_pagar`** vía `CuentaPagarDAO.ajustarSaldoPorNota(...)` (`SELECT ... FOR UPDATE`): la NC
    resta y la ND suma. Se adopta el **Enfoque 1 con neteo en la provisión** → el saldo **puede quedar
    negativo** ("Saldo a favor") y se consume después en la provisión del proveedor.
  - **`libro_iva_compra`**: **fila propia por nota** (`insertarLibroIvaNota`, con el discriminador
    `libro_iva_comp_origen` y FK a la cabecera de la nota) + `anularPorNotaCredito`/`anularPorNotaDebito`.
- **Stock por devolución:** `id_deposito` (nullable) en `nota_credito_compra_detalle` + triggers espejo
  de los de factura (`trg_nota_credito_compra_detalle_stock_ins` resta al insertar,
  `trg_nota_credito_compra_estado_anular` repone al anular). El discriminador es el **depósito por
  línea**: con depósito = devolución física (mueve stock), sin depósito = ajuste financiero. La ND no
  mueve stock.
- **Guards en Factura de Compra:** se reemplaza la heurística `saldo < monto` por
  `CuentaPagarDAO.tienePagosAplicados` (EXISTS sobre pagos) y se agrega el bloqueo **"anulá las notas
  activas antes de editar/anular la factura"** (`tieneNotaActivaPorFactura`).

Pendiente: **correr los triggers de stock contra la base** para verificarlos end-to-end (plan §9). La
validación de cantidad devuelta ≤ comprada se implementó el 2026-08-13.

### 2026-06 — Refactor de esquema para Nota de Crédito/Débito (schema v2 unificado)

Se actualiza `Base de datos Taller 3ro.sql` (se descarta el archivo intermedio "v2", queda un único
schema canónico) con los cambios de esquema del plan [`NOTA_CREDITO_DEBITO_PLAN.md`](NOTA_CREDITO_DEBITO_PLAN.md):

- **`nota_credito/debito_compra_cabecera`**: `numero` pasa de INTEGER a VARCHAR; `observacion` deja
  de ser NOT NULL. Sucursal y condición **no** se agregan (se heredan de la factura referenciada).
- **`nota_credito/debito_compra_detalle`**: PK autoincremental (`id_nota_*_det`); `id_articulo`
  ahora nullable (admite gastos/servicios); nuevas columnas `id_impuesto` (NOT NULL, con FK) y
  `nota_*_descripcion`.
- **`libro_iva_compra`**: `id_fact_comp_cab` nullable, PK simple; nuevas columnas
  `id_nota_cred_comp_cab`, `id_nota_debi_comp_cab` (FKs reales a las cabeceras de nota) y
  `libro_iva_comp_origen` (discriminador FACTURA/NOTA_CRED/NOTA_DEBI). `libro_iva_comp_estado` queda
  como VARCHAR sin default → debe setearse explícitamente desde el código.
- **Correcciones de stock (fuera del plan de NC/ND):** `factura_compra_detalle` gana la columna
  `id_deposito` + FK a `deposito` (el trigger de stock ya la referenciaba pero no existía en el
  esquema anterior); en `stock`, `stk_cantidad_minima/maxima` pasan a `DEFAULT 0` y
  `stk_stock_actual` a nullable, alineado con el UPSERT del trigger.

Pendiente: la decisión de cómo `cuenta_pagar` refleja las notas (ver §4 del plan) y toda la capa
Java/servlet/JSP de NC/ND.

### 2026-05 — Nota de Crédito / Débito Compra (vista inicial)

Se crea `notaCreditoDebito.jsp`, una vista combinada para Nota de Crédito y Débito de Compra, con estilo unificado al de `facturaCompra.jsp` (form-floating, layout de cards) y adaptada su sección de artículos. Se agrega entrada en el menú principal y lateral para su acceso. **Aún no tiene servlet ni integración de backend**: es un esqueleto de vista para futura implementación (botón "Buscar Artículo" comentado por ahora).

El diseño de la implementación (cambios de BD, efecto en Cuenta a Pagar y Libro IVA, flujos y checklist) está documentado en [`NOTA_CREDITO_DEBITO_PLAN.md`](NOTA_CREDITO_DEBITO_PLAN.md).

### 2026-05 — Triggers de Stock en PostgreSQL

Se agrega archivo `Procedimientos y Triggers para BD.sql` con triggers PL/pgSQL que mantienen el stock al guardar y anular facturas de compra. Decisión de arquitectura: el stock se actualiza en BD vía triggers, no vía DAO Java.

**Triggers creados:**
- `trg_factura_compra_detalle_stock_ins` (AFTER INSERT en `factura_compra_detalle`): UPSERT atómico con `ON CONFLICT (id_deposito, id_articulo)`. Auto-crea la fila de stock con min/max=0 si no existe. Ignora detalles sin artículo o sin depósito (caso gasto/fondo fijo).
- `trg_factura_compra_estado_anular` (AFTER UPDATE OF estado en `factura_compra_cabecera`): revierte el stock cuando la factura pasa a `Anulado`. Idempotente: solo actúa en la transición no-anulado → Anulado.

**Reglas de negocio asumidas por los triggers:**
- Una factura anulada no se puede des-anular.
- Los detalles de una factura guardada son inmutables (no se editan cantidades, no se eliminan ni agregan líneas).

### 2026-04 — Validación de Plazo en Crédito

`FacturaCompraServlet`: cuando la condición es `Crédito`, el plazo es obligatorio y debe ser mayor a 0. Validación server-side antes de persistir.

### 2026-03 — Refactor Transaccional + Libro IVA + Sincronización Cuenta a Pagar

Refactor mayor del módulo Factura de Compra para garantizar atomicidad y trazabilidad fiscal/contable.

**Cambios principales:**
- `FacturaCompraDAO`, `PedidoCompraDAO`, `PresupuestoDAO`, `OrdenCompraDAO`: métodos transaccionales (control explícito de `setAutoCommit(false)` + `commit/rollback`).
- `LibroIvaCompra` + `LibroIvaCompraDAO` + `LibroIvaCompraService`: registro fiscal generado dentro de la misma transacción que la factura.
- **Preservación de trazabilidad al anular**: al anular una factura no se borran las filas del Libro IVA, se preservan con el estado actualizado. Deduplicación del cálculo de impuestos (un solo lugar de cálculo).
- **Sincronización Cuenta a Pagar**: al editar o anular una factura, su Cuenta a Pagar asociada se actualiza/cancela. Validación de que no haya pagos aplicados antes de permitir anular. Ajuste automático de fecha de vencimiento cuando la condición es Contado.
- Manejo de nulls y validaciones reforzadas en todo el módulo.
- Todas las 85 entidades del modelo implementan `Serializable` (necesario para guardar `State` en sesión de forma segura).

### 2026-02 — Nota de Remisión (vista inicial)

Se crea `notaRemision.jsp` con estilo unificado al de `facturaCompra.jsp` (form-floating, layout de cards). Se agrega entrada en el menú principal. **Aún no tiene servlet ni DAOs**: es un esqueleto de vista para futura implementación.

### 2026-02-06

#### Sistema de Permisos ReadOnly por Módulo
Implementación completa del sistema de control de acceso basado en las tablas `permiso`, `grupo` y `modulo` existentes en BD.

**Archivos nuevos:**
| Archivo | Descripción |
|---------|-------------|
| `modelo/PermisoDAO.java` | DAO con `getPermiso(idGrupo, idModulo)` y `listarPermisosByGrupo(idGrupo)` |
| `service/PermisoService.java` | Service wrapper del DAO |
| `controlador/AuthorizationFilter.java` | Filter `@WebFilter` que intercepta servlets de negocio |

**Archivos modificados:**
| Archivo | Cambios |
|---------|---------|
| `controlador/LoginServlet.java` | Carga `Map<String, Permiso>` en session al hacer login |
| `controlador/FacturaCompraServlet.java` | Validación server-side de permisos antes del switch de acciones |
| `controlador/PedidoCompraServlet.java` | Validación server-side de permisos antes del switch de acciones |
| `controlador/PresupuestoServlet.java` | Validación server-side de permisos antes del switch de acciones |
| `controlador/OrdenCompraServlet.java` | Validación server-side de permisos antes del switch de acciones |
| `facturaCompra.jsp` | Botones Nuevo, Guardar, Anular, Agregar, Editar, Eliminar condicionados con `<c:if>`/`<c:choose>` |
| `pedidoCompra.jsp` | Botones condicionados con flags de permisos |
| `presupuesto.jsp` | Botones condicionados con flags de permisos |
| `ordenCompra.jsp` | Botones condicionados con flags de permisos |

**Flujo del sistema:**
```
Login → PermisoService.listarPermisosByGrupo() → Map<String, Permiso> en session
  → AuthorizationFilter intercepta request
    → Si leer=false → redirect a MenuPrincipal con mensaje de error
    → Si leer=true → setea puedeInsertar/puedeEditar/puedeBorrar como request attributes
      → Servlet valida permisos server-side antes de procesar acción de escritura
      → JSP condiciona botones según flags de permisos
```

**Mapeo URL → Módulo:**
| Servlet | Módulo BD |
|---------|-----------|
| FacturaCompraServlet | compra |
| PedidoCompraServlet | compra |
| PresupuestoServlet | compra |
| OrdenCompraServlet | compra |

**Clasificación de acciones por permiso:**
- `puedeInsertar`: Nuevo, AgregarArticulo, Guardar/PersistirPedido/PersistirPresupuesto/PersistirOrdenCompra
- `puedeEditar`: EditarArticulo, ActualizarArticulo, EditarPrecioArticuloList, ModificarArticuloDetalle, Aprobar
- `puedeBorrar`: EliminarArticulo/EliminarArticuloList, Anular

---

### 2026-01-26

#### Patrón Session + Token en FacturaCompraServlet
El servlet utiliza un patrón de estado en sesión con token único:
```java
private static class FacturaCompraState implements Serializable {
    FacturaCompra facturaCompra = new FacturaCompra();
    List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
    Proveedor proveedorSeleccionado;
    Sucursal sucursalSeleccionada;
    OrdenCompra ordenCompraSeleccionada;
    List<TipoImpuesto> listaTipoImpuesto;
    // ... más campos
}
```

#### Cambios en Modal de Presupuestos (ordenCompra.jsp)
- Agregado campo `ordenCompraCompleta` en modelo `Presupuesto`
- Query con `EXISTS` en `PresupuestoDAO.listarPresupuestoConDetalles()` para verificar si presupuesto ya tiene orden de compra
- Presupuestos con orden asociada se muestran en verde y no son seleccionables

#### Columnas Adicionales en Modales
- **ordenCompra.jsp**: Agregada columna "Nro Pedido" en modal de órdenes de compra
- **facturaCompra.jsp**: Agregada columna "Tipo Factura" en modal de facturas

#### Campos Comentados en ordenCompra.jsp
```jsp
<%-- Observacion (comentado)
<form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
    ...observación y botón Aplicar...
</form>
--%>

<%-- Articulo seleccionado para editar (comentado)
<form>...campos de edición...</form>
--%>

<%-- Botón Editar en tabla (comentado) --%>
```

#### Factura de Compra - Soporte para Gasto/Fondo Fijo

**Cambios en FacturaCompraDetalle.java:**
```java
private TipoImpuesto tipoImpuesto;
// + getter/setter
// + constructor con tipoImpuesto
```

**Cambios en FacturaCompraDetalleDAO.java:**
- Lectura de `id_impuesto` en `listarDetallesPorFactura()`
- Escritura de `id_impuesto` en `insertarDetalle()`

**Cambios en TipoImpuestoDAO.java:**
```java
public List<TipoImpuesto> listarTipoImpuesto() throws SQLException
```

**Cambios en TipoImpuestoService.java:**
```java
public List<TipoImpuesto> listarTipoImpuesto() throws SQLException
```

**Cambios en FacturaCompraServlet.java:**
- Agregado `TipoImpuestoService`
- Carga de `listaTipoImpuesto` en estado
- Lectura de `idTipoImpuesto` en `accionAgregarArticulo()`

**Cambios en facturaCompra.jsp:**
- Select de impuesto para facturas de gasto/fondo fijo (líneas 307-314)
- Cálculo de IVA usa `detalle.tipoImpuesto` cuando no hay artículo (línea 399)

#### Cálculo de IVA en JSP
```jsp
<%-- Obtener descripcion del impuesto (del articulo o del detalle) --%>
<c:set var="descImpuesto" value="${not empty detalle.articulo ?
    detalle.articulo.tipoImpuesto.descripcion :
    detalle.tipoImpuesto.descripcion}" />

<c:choose>
    <c:when test="${fn:contains(descImpuesto, '10')}">
        <c:set var="iva10" value="${subtotal / 11}" />
        <c:set var="gravada10" value="${subtotal - iva10}" />
    </c:when>
    <c:when test="${fn:contains(descImpuesto, '5')}">
        <c:set var="iva5" value="${subtotal / 21}" />
        <c:set var="gravada5" value="${subtotal - iva5}" />
    </c:when>
    <c:otherwise>
        <c:set var="exenta" value="${subtotal}" />
    </c:otherwise>
</c:choose>
```

#### Base de Datos - Cambio en factura_compra_detalle
```sql
ALTER TABLE public.factura_compra_detalle
ADD COLUMN id_impuesto INTEGER;

ALTER TABLE public.factura_compra_detalle
ADD CONSTRAINT impuesto_factura_compra_detalle_fk
FOREIGN KEY (id_impuesto)
REFERENCES public.impuesto (id_impuesto);
```

#### Estilos de Estado en Modales
```jsp
<%-- Verde: Completado/Procesado --%>
<tr class="${doc.estado eq 'Completado' ? 'table-success' : ''}">

<%-- Rojo: Anulado --%>
<tr class="${doc.estado eq 'Anulado' ? 'table-danger' : ''}">

<%-- Sin color: Pendiente (seleccionable) --%>
```

### 2026-01-14
- Análisis completo de tablas SQL vs entidades Java
- Creación de 43 nuevas entidades para completar el mapeo
- Cobertura de entidades: 100% (85/85)

---

## Archivos Clave Modificados en Sesión 2026-01-26

| Archivo | Cambios |
|---------|---------|
| `Presupuesto.java` | Campo `ordenCompraCompleta` |
| `PresupuestoDAO.java` | Query con EXISTS para verificar orden asociada |
| `FacturaCompraDetalle.java` | Campo `tipoImpuesto` + constructor |
| `FacturaCompraDetalleDAO.java` | Lectura/escritura de `id_impuesto` |
| `TipoImpuestoDAO.java` | Método `listarTipoImpuesto()` |
| `TipoImpuestoService.java` | Método `listarTipoImpuesto()` |
| `FacturaCompraServlet.java` | Carga de `listaTipoImpuesto`, lectura de impuesto en agregar artículo |
| `facturaCompra.jsp` | Select de impuesto, cálculo IVA con tipoImpuesto del detalle |
| `ordenCompra.jsp` | Columna Nro Pedido, campos comentados, modal presupuestos |

---

## Próximos Módulos a Implementar

### 1. Nota de Remisión Compra (en progreso)
La vista JSP ya existe (`notaRemision.jsp`), falta implementar el servlet, DAOs y service.
- Patrón a usar: Session + Token (referencia: `FacturaCompraServlet`)

### 2. Módulo de Stock (UI)
El stock ya se mantiene automáticamente por triggers PL/pgSQL, falta la UI de consulta y ajustes manuales.
- Entidades: Stock, Deposito, MotivoAjuste, AjusteStockCabecera, AjusteStockDetalle
- Requiere: DAOs (consulta), Services, Servlet, JSP

### 3. Módulo de Ventas
Todos los backends (Pedido, Factura, Nota Crédito/Débito/Remisión) tienen modelo + DAO + Service. Falta la capa UI completa.
- Reusar patrón de Factura Compra (Session+Token, transaccional, Libro IVA, triggers de stock)

### 4. Módulo de Tesorería (en progreso — ver [MODULO_TESORERIA_PLAN.md](MODULO_TESORERIA_PLAN.md))
Gestión financiera completa: cuentas bancarias, cheques, cobros, pagos, caja, fondo fijo, conciliación bancaria.
- ✅ **Hechos:** Referenciales bancarios (§A), Provisión de Cuenta a Pagar (§B) y **Orden de Pago** (§C,
  con emisión de cheques y descuento de saldo — pendiente de prueba end-to-end).
- ⏳ **Camino que resta hacia la conciliación:** Débitos/Créditos (§D) → Fondo Fijo + rendición (§E) →
  **Conciliación bancaria** (§F, el objetivo final).
- El lado **cobros/caja/arqueo/recaudaciones** (§9 del plan) queda para cuando se aborde Ventas.

---

## Otros Pendientes

1. **Crear DAOs** para las entidades de tesorería restantes (ya están los del flujo Cuenta a Pagar → Provisión → Orden de Pago; faltan los de débitos/créditos, fondo fijo, conciliación y todo el lado cobros/caja)
2. **Crear Services REST** para exponer las operaciones
3. **Crear vistas JSP** para las nuevas funcionalidades
4. ~~**Implementar sistema de permisos** por módulo~~ ✅ (implementado 2026-02-06)
5. ~~**Refactor transaccional Factura Compra + Libro IVA**~~ ✅ (implementado 2026-03)
6. ~~**Triggers de stock**~~ ✅ (implementado 2026-05)
7. **Migrar PedidoCompra, Presupuesto, OrdenCompra al patrón Session+Token** — actualmente usan variables de instancia, lo que genera bug de concurrencia en uso multiusuario (ver `ARQUITECTURA_SERVLETS.md`).
8. **Eliminar `facturaCompra_old.jsp`** una vez confirmado que no se necesita como referencia.
9. **Completar Factura de Compra** - Sección de artículos del catálogo (actualmente comentada).
10. ~~**Correr `Procedimientos y Triggers para BD.sql` contra la base.**~~ ✅ Corridos el 2026-08-17.
    Recordatorio: recrear un trigger no reprocesa lo ya cargado, así que las NC emitidas antes de esa
    fecha no movieron stock.
11. ~~**Subir el esquema con `creditos.id_cobro` nullable.**~~ ✅ Subido el 2026-08-17; ya se puede
    registrar un depósito bancario sin módulo de Cobros. Ver `MODULO_TESORERIA_PLAN.md` §D.
12. **Anular un cheque individual** (`ChequeServlet` + `cheque.jsp`): hoy sólo se anulan en cascada al
    anular la orden de pago, y el caso real —cheque mal impreso, extraviado o rechazado, sin deshacer
    el pago— no tiene camino. Es lo único que queda de §G: la **entrega al proveedor** se implementó el
    2026-08-17 y el **ABM de chequeras** el 2026-08-31. Ver `MODULO_TESORERIA_PLAN.md` §G3.
13. **Informes.** No hay nada implementado ni planificado: sin código, sin librería en el `pom.xml` y
    sin definición de qué informes ni en qué formato. Ver `MODULO_TESORERIA_PLAN.md` §H.
14. ~~**Probar la Orden de Pago de punta a punta.**~~ ✅ Probada el 2026-08-17: el circuito corre y la
    provisión pasa a `'Procesada'`. También se confirmó el nombre `forma_pag_tipo_cambio` contra
    Power Architect.
15. **Migrar los importes de `INTEGER` a `BIGINT`.** Decisión 2026-08-17: se mantienen en `INTEGER`
    por ahora, pero el techo son ~2.147 millones de Gs y conviene migrar antes de tener volumen real
    de datos. Ver `MODULO_TESORERIA_PLAN.md` §8.
16. **Concurrencia en Pedido, Presupuesto y Orden de Compra.** Decisión 2026-08-17: se mantiene el
    patrón de variables de instancia por ahora, asumiendo uso mono-usuario. Si el sistema pasa a
    usarse por más de una persona a la vez, migrar a Session+Token deja de ser opcional: los datos
    se mezclan entre usuarios. Ver `ARQUITECTURA_SERVLETS.md`.
