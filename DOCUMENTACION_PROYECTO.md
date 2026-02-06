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
[x] PermisoDAO ✅ (creado 2026-02-06)
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
[x] PermisoService ✅ (creado 2026-02-06)
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

### 1. Módulo de Stock (siguiente)
Gestión de inventario: control de existencias por depósito, ajustes de stock (entrada/salida), stock mínimo/máximo con alertas.
- Entidades: Articulo, Stock, Deposito, MotivoAjuste, AjusteStockCabecera, AjusteStockDetalle
- Requiere: DAOs, Services, Servlet, JSP

### 2. Módulo de Tesorería (después de Stock)
Gestión financiera completa: cuentas bancarias, cheques, cobros, pagos, caja, fondo fijo, conciliación bancaria.
- Submódulos: Bancos, Cheques, Cobros/Pagos, Caja, Fondo Fijo, Conciliación
- Requiere: DAOs, Services, Servlets, JSPs para cada submódulo

---

## Otros Pendientes

1. **Crear DAOs** para las entidades restantes (~40 pendientes)
2. **Crear Services REST** para exponer las operaciones
3. **Crear vistas JSP** para las nuevas funcionalidades
4. ~~**Implementar sistema de permisos** por módulo~~ ✅ (implementado 2026-02-06)
5. **Completar Factura de Compra** - Sección de artículos del catálogo (actualmente comentada)
