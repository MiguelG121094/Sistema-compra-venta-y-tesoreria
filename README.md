# Sistema de Compra, Venta y Tesorería

Sistema ERP desarrollado en Java EE para la gestión integral de procesos de compras, ventas y tesorería empresarial.

---

## Descripción General

Este sistema permite gestionar el ciclo completo de operaciones comerciales de una empresa, desde la solicitud de compra hasta el pago a proveedores, y desde la venta hasta el cobro a clientes. Incluye módulos de tesorería para el control de cuentas bancarias, cheques, cajas y fondos.

> **Alcance:** el sistema **no incluye un módulo de contabilidad**. El flujo de dinero se gestiona íntegramente en **Tesorería**, y el aspecto fiscal/contable solo se cubre hasta el **Libro IVA de Compra y de Venta** (más Timbrado y Tipo de Comprobante como soporte). No hay asientos contables, plan de cuentas, balances ni estados financieros.

---

## Tecnologías Utilizadas

| Capa | Tecnología |
|------|------------|
| **Backend** | Java EE 8, Servlets, JAX-RS |
| **Frontend** | JSP, Bootstrap 5.3.3, DataTables 2 |
| **Base de Datos** | PostgreSQL (con triggers PL/pgSQL para stock) |
| **Servidor** | GlassFish / Payara |
| **Build** | Maven |
| **Notificaciones** | Toastr.js |
| **JS auxiliar** | jQuery Mask (máscaras de input) |

---

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE PRESENTACIÓN                    │
│                   (JSP + Bootstrap + DataTables)             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE CONTROL                         │
│                        (Servlets)                            │
│   PedidoCompraServlet, PresupuestoServlet, OrdenCompra...   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      CAPA DE SERVICIOS                       │
│                       (Services)                             │
│   PedidoCompraService, PresupuestoService, OrdenCompra...   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE ACCESO A DATOS                    │
│                         (DAOs)                               │
│     PedidoCompraDAO, PresupuestoDAO, OrdenCompraDAO...      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      BASE DE DATOS                           │
│                       PostgreSQL                             │
│              (Pool de conexiones GlassFish)                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Módulos del Sistema

### 1. Módulo de Compras
Gestiona todo el proceso de adquisición de mercaderías.

| Entidad | Descripción |
|---------|-------------|
| Pedido de Compra | Solicitud interna de mercadería |
| Presupuesto | Cotización recibida del proveedor |
| Orden de Compra | Autorización formal de compra |
| Factura de Compra | Documento fiscal del proveedor |
| Nota de Crédito Compra | Devoluciones al proveedor |
| Nota de Débito Compra | Cargos adicionales del proveedor |
| Nota de Remisión Compra | Comprobante de recepción |

### 2. Módulo de Ventas
Gestiona el proceso de comercialización de productos.

| Entidad | Descripción |
|---------|-------------|
| Pedido de Venta | Solicitud del cliente |
| Factura de Venta | Documento fiscal emitido |
| Nota de Crédito Venta | Devoluciones del cliente |
| Nota de Débito Venta | Cargos adicionales al cliente |
| Nota de Remisión Venta | Comprobante de entrega |

### 3. Módulo de Tesorería
Gestiona el flujo de dinero y las obligaciones financieras.

| Submódulo | Entidades |
|-----------|-----------|
| **Cuentas** | Cuenta a Pagar, Cuenta a Cobrar, Cuenta Bancaria |
| **Bancos** | Entidad Financiera, Tipo Cuenta, Crédito, Débito |
| **Cheques** | Chequera, Cheque, Cheque Recibido, Tipo Cheque |
| **Tarjetas** | Tarjeta, Tipo Tarjeta |
| **Cobros** | Cobro, Cobro Detalle, Cobro Cheque, Cobro Tarjeta |
| **Pagos** | Orden de Pago, Provisión Cuenta Pagar |
| **Caja** | Caja, Apertura/Cierre Caja, Arqueo Caja |
| **Fondo Fijo** | Fondo Fijo, Rendición, Detalle Rendición |

### 4. Módulo de Inventario
Gestiona el stock de mercaderías.

| Entidad | Descripción |
|---------|-------------|
| Artículo | Producto o mercadería |
| Stock | Existencias por depósito |
| Depósito | Almacén físico |
| Ajuste de Stock | Correcciones de inventario |
| Tipo Artículo | Clasificación de productos |
| Marca | Marca del producto |
| Presentación | Forma de presentación |
| Grupo | Agrupación de artículos |

### 5. Módulo de Personas
Gestiona las entidades relacionadas con personas.

| Entidad | Descripción |
|---------|-------------|
| Persona | Datos básicos de personas |
| Cliente | Compradores |
| Proveedor | Vendedores/Suministradores |
| Sucursal | Puntos de venta/operación |
| Usuario | Usuarios del sistema |

### 6. Soporte Fiscal (no es un módulo de contabilidad)
El sistema **no implementa contabilidad** (asientos, plan de cuentas, balances). Solo roza el área fiscal mediante las siguientes entidades de apoyo:

| Entidad | Descripción |
|---------|-------------|
| Timbrado | Autorización fiscal |
| Tipo Comprobante | Clasificación de documentos |
| Libro IVA Compra | Registro fiscal de compras (hasta aquí llega el alcance contable) |
| Libro IVA Venta | Registro fiscal de ventas (hasta aquí llega el alcance contable) |

---

## Flujos de Trabajo

### Flujo de Compras

```
┌─────────────────────┐
│  PEDIDO DE COMPRA   │  ← Usuario solicita mercadería
│     (Pendiente)     │
└──────────┬──────────┘
           │ Se envía a proveedores para cotizar
           ▼
┌─────────────────────┐
│    PRESUPUESTO      │  ← Proveedor envía cotización
│     (Pendiente)     │     con precios y condiciones
└──────────┬──────────┘
           │ Se aprueba el mejor presupuesto
           ▼
┌─────────────────────┐
│  ORDEN DE COMPRA    │  ← Autorización formal al proveedor
│     (Pendiente)     │     para que despache mercadería
└──────────┬──────────┘
           │ Proveedor entrega mercadería y factura
           ▼
┌─────────────────────┐
│ FACTURA DE COMPRA   │  ← Documento fiscal recibido
│     (Pendiente)     │     Se registra la obligación
└──────────┬──────────┘
           │ Se genera deuda con el proveedor
           ▼
┌─────────────────────┐
│   CUENTA A PAGAR    │  ← Obligación de pago
│  (Pendiente Pago)   │     Vencimiento según plazo
└──────────┬──────────┘
           │ Se realiza el pago
           ▼
┌─────────────────────┐
│   ORDEN DE PAGO     │  ← Autorización de pago
│     (Ejecutado)     │     Cheque/Transferencia/Efectivo
└─────────────────────┘
```

### Flujo de Ventas

```
┌─────────────────────┐
│   PEDIDO DE VENTA   │  ← Cliente solicita productos
│     (Pendiente)     │
└──────────┬──────────┘
           │ Se prepara y factura
           ▼
┌─────────────────────┐
│  FACTURA DE VENTA   │  ← Documento fiscal emitido
│     (Pendiente)     │     Contado o Crédito
└──────────┬──────────┘
           │ Si es crédito, se genera derecho de cobro
           ▼
┌─────────────────────┐
│  CUENTA A COBRAR    │  ← Derecho de cobro
│ (Pendiente Cobro)   │     Vencimiento según plazo
└──────────┬──────────┘
           │ Cliente realiza el pago
           ▼
┌─────────────────────┐
│       COBRO         │  ← Recepción del pago
│     (Cobrado)       │     Efectivo/Cheque/Tarjeta
└─────────────────────┘
```

### Flujo de Tesorería - Pagos

```
┌─────────────────────┐
│  CUENTAS A PAGAR    │  ← Deudas pendientes
│     (Múltiples)     │
└──────────┬──────────┘
           │ Se seleccionan facturas a pagar
           ▼
┌─────────────────────┐
│    PROVISIÓN DE     │  ← Agrupación de deudas
│   CUENTA A PAGAR    │     para pago conjunto
└──────────┬──────────┘
           │ Se autoriza el pago
           ▼
┌─────────────────────┐
│   ORDEN DE PAGO     │  ← Instrucción de pago
│     (Aprobada)      │     Formas: Cheque y/o Transferencia
│                     │     (sin efectivo — eso va por Fondo Fijo)
└──────────┬──────────┘
           │ Se ejecuta el pago
           ▼
┌─────────────────────┐
│   PAGO REALIZADO    │  ← Actualización de saldos
│                     │     Cuentas saldadas
└─────────────────────┘
```

### Flujo de Tesorería - Cobros

```
┌─────────────────────┐
│  CUENTAS A COBRAR   │  ← Créditos pendientes
│     (Múltiples)     │
└──────────┬──────────┘
           │ Cliente realiza pago
           ▼
┌─────────────────────┐
│       COBRO         │  ← Recepción de pago
│  (Forma de cobro)   │     Efectivo/Cheque/Tarjeta
└──────────┬──────────┘
           │ Si es cheque, va a cartera
           ▼
┌─────────────────────┐
│   RECAUDACIÓN A     │  ← Cheques/Efectivo a depositar
│     DEPOSITAR       │
└──────────┬──────────┘
           │ Se deposita en banco
           ▼
┌─────────────────────┐
│ DEPÓSITO BANCARIO   │  ← Fondos en cuenta bancaria
│                     │
└─────────────────────┘
```

---

## Estructura del Proyecto

```
Sistema-compra-venta-y-tesoreria/
├── src/
│   └── main/
│       ├── java/
│       │   ├── conexion/
│       │   │   └── Conexion.java          # Pool de conexiones
│       │   ├── controlador/
│       │   │   ├── AuthFilter.java        # Filtro de autenticación
│       │   │   ├── LoginServlet.java
│       │   │   ├── PedidoCompraServlet.java
│       │   │   ├── PresupuestoServlet.java
│       │   │   ├── OrdenCompraServlet.java
│       │   │   └── ...
│       │   ├── modelo/
│       │   │   ├── Articulo.java
│       │   │   ├── ArticuloDAO.java
│       │   │   ├── PedidoCompra.java
│       │   │   ├── PedidoCompraDAO.java
│       │   │   └── ... (85 entidades)
│       │   └── service/
│       │       ├── ArticuloService.java
│       │       ├── PedidoCompraService.java
│       │       └── ...
│       ├── resources/
│       │   └── META-INF/
│       │       └── persistence.xml
│       └── webapp/
│           ├── Bootstrap 5.3.3/
│           ├── DataTables 2/
│           ├── Theme/
│           ├── toastr/
│           ├── WEB-INF/
│           │   ├── web.xml
│           │   └── glassfish-web.xml
│           ├── pedidoCompra.jsp
│           ├── presupuesto.jsp
│           ├── ordenCompra.jsp
│           ├── facturaCompra.jsp
│           └── ...
├── pom.xml
├── Base de datos Taller 3ro.sql               # Schema canónico
├── Procedimientos y Triggers para BD.sql      # Triggers PL/pgSQL para stock
├── README.md
├── DOCUMENTACION_PROYECTO.md
├── ARQUITECTURA_SERVLETS.md
└── NOTA_CREDITO_DEBITO_PLAN.md                 # Plan de implementación NC/ND de compra
```

---

## Estado de Implementación

### Módulo de Compras

| Componente | Modelo | DAO | Service | Servlet | JSP | Estado |
|------------|:------:|:---:|:-------:|:-------:|:---:|--------|
| Pedido de Compra | ✅ | ✅ | ✅ | ✅ | ✅ | **Completo** |
| Presupuesto | ✅ | ✅ | ✅ | ✅ | ✅ | **Completo** |
| Orden de Compra | ✅ | ✅ | ✅ | ✅ | ✅ | **Completo** |
| Factura de Compra | ✅ | ✅ | ✅ | ✅ | ✅ | **Completo** (con triggers stock + Libro IVA + Cuenta a Pagar) |
| Nota Crédito Compra | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ Completo (`NotaCreditoDebitoServlet` + `notaCreditoDebito.jsp`, con validación de cantidad devuelta ≤ comprada; triggers de stock corridos contra la BD el 2026-08-17 — ver [plan](NOTA_CREDITO_DEBITO_PLAN.md)) |
| Nota Débito Compra | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ Completo (mismo servlet/vista que la Nota de Crédito) |
| Nota Remisión Compra | ✅ | ❌ | ❌ | ❌ | ⚠️ | Parcial (vista inicial) |
| Cuenta a Pagar | ✅ | ✅ | ✅ | ❌ | ❌ | Backend listo (integrado con Factura Compra) |

### Módulo de Ventas

| Componente | Modelo | DAO | Service | Servlet | JSP | Estado |
|------------|:------:|:---:|:-------:|:-------:|:---:|--------|
| Pedido de Venta | ✅ | ✅ | ✅ | ❌ | ❌ | Pendiente |
| Factura de Venta | ✅ | ✅ | ✅ | ❌ | ❌ | Pendiente |
| Nota Crédito Venta | ✅ | ✅ | ✅ | ❌ | ❌ | Pendiente |
| Nota Débito Venta | ✅ | ✅ | ✅ | ❌ | ❌ | Pendiente |
| Nota Remisión Venta | ✅ | ✅ | ✅ | ❌ | ❌ | Pendiente |

### Módulo de Tesorería

| Componente | Modelo | DAO | Service | Estado |
|------------|:------:|:---:|:-------:|--------|
| Caja | ✅ | ✅ | ✅ | Backend listo |
| Apertura/Cierre Caja | ✅ | ✅ | ✅ | Backend listo |
| Cobro | ✅ | ✅ | ✅ | Backend listo |
| Cuenta a Cobrar | ✅ | ✅ | ✅ | Backend listo |
| Cuenta a Pagar | ✅ | ✅ | ✅ | Backend listo (sincronizado con Factura Compra) |
| Cuenta bancaria (+ moneda, tipo de cuenta, entidad financiera) | ✅ | ✅ | ✅ | ✅ **Completo** — ABM con `CuentaServlet` + `cuenta.jsp` |
| Provisión de Cuenta a Pagar | ✅ | ✅ | ✅ | ✅ **Completo** — `ProvisionCuentaPagarServlet` + `provision.jsp` (netea el saldo a favor de las NC) |
| Orden de Pago (+ formas de pago, cheque, chequera) | ✅ | ✅ | ✅ | ✅ **Completo** — `OrdenPagoServlet` + `ordenPago.jsp`; descuenta el saldo, emite cheques y anula con reversa total. Probada end-to-end (2026-08-17) |
| Débitos / Créditos bancarios | ✅ | ❌ | ❌ | Próximo (alimentan la conciliación) |
| Depósitos bancarios (boletas) | ✅ | ❌ | ❌ | Pendiente — es el mismo ABM que Créditos; `creditos.id_cobro` ya es nullable |
| Gestión de cheques (ABM chequera, entrega, anulación individual) | ✅ | ⚠️ | ❌ | Pendiente — hoy los cheques sólo se emiten/anulan desde la Orden de Pago |
| Informes | — | — | — | Pendiente — sin planificar (ver `MODULO_TESORERIA_PLAN.md` §H) |
| Fondo Fijo + Rendición | ✅ | ❌ | ❌ | Pendiente |
| Conciliación Bancaria | ✅ | ❌ | ❌ | Pendiente (objetivo final del módulo) |
| Timbrado | ✅ | ✅ | ✅ | Backend listo |
| Libro IVA Compra | ✅ | ✅ | ✅ | Backend listo (integrado en Factura Compra) |

### Módulo de Seguridad

| Componente | Estado |
|------------|--------|
| AuthFilter (autenticación por sesión) | ✅ Completo |
| AuthorizationFilter (permisos por módulo: leer/insertar/editar/borrar) | ✅ Completo |
| Permiso DAO + Service | ✅ Completo |
| Gestión UI de Módulos / Permisos | ❌ Pendiente |

**Leyenda:** ✅ Completo | ⚠️ Parcial | ❌ Pendiente

---

## Configuración del Entorno

### Requisitos Previos
- JDK 8 o superior
- Maven 3.6+
- PostgreSQL 12+
- GlassFish 5+ o Payara 5+

### Configuración de Base de Datos

1. Crear base de datos en PostgreSQL:
```sql
CREATE DATABASE taller_3ro_compras_tesoreria;
```

2. Ejecutar script de creación del schema:
```bash
psql -U postgres -d taller_3ro_compras_tesoreria -f "Base de datos Taller 3ro.sql"
```

3. Ejecutar script de triggers (actualización automática de stock):
```bash
psql -U postgres -d taller_3ro_compras_tesoreria -f "Procedimientos y Triggers para BD.sql"
```

### Configuración de GlassFish

1. Acceder a la consola: `http://localhost:4848/`

2. Crear JDBC Connection Pool:
   - Resources → JDBC → JDBC Connection Pools
   - Pool Name: `PostgreSQLPool`
   - Resource Type: `javax.sql.DataSource`
   - Database Driver Vendor: `PostgreSQL`

3. Configurar propiedades del pool:
   - `serverName`: localhost
   - `portNumber`: 5432
   - `databaseName`: taller_3ro_compras_tesoreria
   - `user`: postgres
   - `password`: [tu_contraseña]

4. Crear JDBC Resource:
   - Resources → JDBC → JDBC Resources
   - JNDI Name: `jdbc/MiDataSource`
   - Pool Name: `PostgreSQLPool`

### Compilación y Despliegue

```bash
# Compilar
mvn clean install

# El archivo WAR se genera en: target/Taller3ro-1.0-SNAPSHOT.war

# Desplegar en GlassFish desde la consola o:
asadmin deploy target/Taller3ro-1.0-SNAPSHOT.war
```

---

## Seguridad

- Autenticación basada en sesiones
- Filtro de autenticación (`AuthFilter.java`) para proteger recursos
- Filtro de autorización (`AuthorizationFilter.java`) con permisos por módulo (leer/insertar/editar/borrar)
- Permisos cargados al login en `Map<String, Permiso>` y validados server-side antes de cada acción
- Botones de JSP condicionados con `<c:if>` según permisos
- Validación de sesión en cada JSP
- Timeout de sesión: 30 minutos

---

## Contribuidores

- Miguel - Desarrollo principal

---

## Licencia

Proyecto privado - Todos los derechos reservados
