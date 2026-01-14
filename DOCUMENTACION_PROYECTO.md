# Sistema de Compra, Venta y Tesorería

## Descripción General

Sistema ERP desarrollado en Java EE para la gestión integral de:
- **Compras**: Pedidos, presupuestos, órdenes de compra, facturas de compra
- **Ventas**: Pedidos, facturas de venta, notas de remisión
- **Tesorería**: Cuentas bancarias, cheques, cobros, pagos, conciliación bancaria
- **Inventario**: Artículos, stock, ajustes de stock
- **Seguridad**: Usuarios, grupos, permisos por módulo

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

#### Módulo de Contabilidad
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
Faltan crear los DAOs para las nuevas entidades:

```
[ ] MonedaDAO
[ ] TipoEntidadFinancieraDAO
[ ] EntidadFinancieraDAO
[ ] TipoCuentaDAO
[ ] CuentaDAO
[ ] TipoChequeDAO
[ ] ChequeraDAO
[ ] ChequeDAO
[ ] ChequeRecibidoDAO
[ ] TitularDAO
[ ] StockDAO
[ ] MotivoAjusteDAO
[ ] AjusteStockCabeceraDAO
[ ] AjusteStockDetalleDAO
[ ] ModuloDAO
[ ] PermisoDAO
[ ] FormaPagoCabeceraDAO
[ ] FormaPagoDetalleDAO
[ ] FormaCobroDAO
[ ] FormaCobroDetalleDAO
[ ] CobroDetalleDAO
[ ] OrdenPagoDetalleDAO
[ ] TipoTarjetaDAO
[ ] TarjetaDAO
[ ] ConciliacionBancariaDAO
[ ] ConciliacionBancariaDetalleDAO
[ ] DebitoDAO
[ ] CreditoDAO
[ ] RecaudacionDepositarDAO
[ ] RecaudacionDepositarDetalleDAO
[ ] TipoComprobanteDAO
[ ] ProvisionCuentaPagarDAO
[ ] ProvisionCuentaPagarDetalleDAO
[ ] FondoFijoDAO
[ ] FondoFijoRendicionDAO
[ ] FondoFijoRendicionDetalleDAO
[ ] CobroTarjetaDAO
[ ] CobroChequeDAO
[ ] ArqueoCajaDAO
[ ] LibroIvaVentaDAO
[ ] LibroIvaCompraDAO
[ ] NotaRemisionCompraDAO
[ ] NotaRemisionCompraDetalleDAO
```

### 2. Services (REST)
Faltan crear los servicios REST para las nuevas entidades:

```
[ ] MonedaService
[ ] EntidadFinancieraService
[ ] CuentaService
[ ] ChequeService
[ ] StockService
[ ] AjusteStockService
[ ] PermisoService
[ ] ConciliacionBancariaService
[ ] FondoFijoService
[ ] ArqueoCajaService
[ ] LibroIvaService
... (y demás servicios)
```

### 3. Servlets (Controladores)
Faltan crear los controladores para las nuevas funcionalidades:

```
[ ] CuentaBancariaServlet
[ ] ChequeServlet
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
[ ] cuentaBancaria.jsp
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

#### Tesorería
- [ ] CRUD de Cuentas Bancarias
- [ ] Gestión de Chequeras y Cheques
- [ ] Emisión de Cheques
- [ ] Recepción de Cheques
- [ ] Conciliación Bancaria
- [ ] Gestión de Fondo Fijo
- [ ] Rendición de Fondo Fijo
- [ ] Arqueo de Caja
- [ ] Recaudaciones a Depositar

#### Inventario
- [ ] Control de Stock por Depósito
- [ ] Ajustes de Stock (entrada/salida)
- [ ] Stock mínimo/máximo con alertas

#### Contabilidad
- [ ] Libro IVA Ventas
- [ ] Libro IVA Compras
- [ ] Reportes fiscales

#### Seguridad
- [ ] Gestión de Módulos
- [ ] Asignación de Permisos por Grupo
- [ ] Control de acceso por módulo (CRUD)

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

### 2026-01-14
- Análisis completo de tablas SQL vs entidades Java
- Creación de 43 nuevas entidades para completar el mapeo
- Cobertura de entidades: 100% (85/85)

---

## Próximos Pasos Recomendados

1. **Crear DAOs** para las nuevas entidades
2. **Crear Services REST** para exponer las operaciones
3. **Implementar módulo de Stock** (alta prioridad para inventario)
4. **Implementar módulo de Tesorería** (cuentas bancarias, cheques)
5. **Crear vistas JSP** para las nuevas funcionalidades
6. **Implementar sistema de permisos** por módulo
