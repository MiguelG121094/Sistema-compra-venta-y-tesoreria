# Arquitectura de Servlets - Sistema de Compra, Venta y Tesoreria

## Contexto del Problema

### Arquitectura Actual (PedidoCompra, Presupuesto, OrdenCompra)

Los servlets actuales utilizan **variables de instancia** para mantener el estado:

```java
@WebServlet(name = "PresupuestoServlet", urlPatterns = {"/PresupuestoServlet"})
public class PresupuestoServlet extends HttpServlet {

    // PROBLEMA: Variables de instancia compartidas entre TODOS los usuarios
    private Presupuesto presupuesto = new Presupuesto();
    private List<PresupuestoDetalle> listaPresupuestoDetalle;
    private Proveedor proveedor;
    private PedidoCompra pedidoCompra;
    // ... más variables
}
```

### Problemas de esta Arquitectura

1. **No es thread-safe**: Los servlets son singleton, una sola instancia sirve a todos los usuarios
2. **Mezcla de datos**: Si dos usuarios trabajan simultáneamente, los datos se sobrescriben
3. **Bug en producción**: Usuario A selecciona proveedor X, Usuario B selecciona proveedor Y, Usuario A ve proveedor Y
4. **Código repetitivo**: Cada acción debe pasar manualmente todos los datos al request

### Ejemplo del Problema

```
Usuario A: Selecciona Pedido #100
Usuario B: Selecciona Pedido #200
Usuario A: Ve Pedido #200 (datos de Usuario B)
```

---

## Nueva Arquitectura Propuesta: Session + Token

### Concepto

- Cada documento en edición tiene un **token único**
- El estado se guarda en la **sesión del usuario** usando el token como clave
- Permite múltiples documentos abiertos en diferentes pestañas
- Thread-safe y sin conflictos entre usuarios

### Diagrama de Flujo

```
┌─────────────────────────────────────────────────────────────────────┐
│                         NAVEGADOR                                    │
├─────────────────────────────────────────────────────────────────────┤
│  Pestaña 1 (Factura A)          │  Pestaña 2 (Factura B)            │
│  token = "abc12345"             │  token = "xyz98765"               │
│  <input type="hidden"           │  <input type="hidden"             │
│   name="token"                  │   name="token"                    │
│   value="abc12345">             │   value="xyz98765">               │
└─────────────────────────────────────────────────────────────────────┘
                    │                           │
                    ▼                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         SERVLET                                      │
│  String token = request.getParameter("token");                       │
│  FacturaState estado = (FacturaState) session.getAttribute(          │
│      "facturaCompra_" + token);                                      │
└─────────────────────────────────────────────────────────────────────┘
                    │                           │
                    ▼                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      HttpSession                                     │
├─────────────────────────────────────────────────────────────────────┤
│  "facturaCompra_abc12345" → FacturaState { factura A, detalles A }  │
│  "facturaCompra_xyz98765" → FacturaState { factura B, detalles B }  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Implementación de Referencia: FacturaCompraServlet

### Clase de Estado (encapsula todo el estado del documento)

```java
/**
 * Clase que encapsula todo el estado de trabajo de una factura.
 * Se guarda en sesión con un token único.
 * Implementa Serializable para permitir persistencia de sesión.
 */
private static class FacturaCompraState implements Serializable {
    private static final long serialVersionUID = 1L;

    FacturaCompra facturaCompra = new FacturaCompra();
    List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
    Proveedor proveedorSeleccionado;
    Sucursal sucursalSeleccionada;
    OrdenCompra ordenCompraSeleccionada;
    FacturaCompraDetalle detalleSeleccionado;  // Para edición de artículo
    boolean esNuevo = false;

    // Datos para modales (se cargan una vez)
    List<OrdenCompra> listaOrdenesCompra;
    List<FacturaCompra> listaFacturasCompra;
    List<Proveedor> listaProveedores;
    List<Sucursal> listaSucursales;
    List<Articulo> listaArticulos;
    List<TipoImpuesto> listaTipoImpuesto;  // Para facturas de gasto/fondo fijo
}
```

### Métodos Helper para Manejo de Sesión

```java
// Prefijo para las claves de sesión
private static final String SESSION_PREFIX = "facturaCompra_";

/**
 * Genera un nuevo token único para un documento
 */
private String generarToken() {
    return UUID.randomUUID().toString().substring(0, 8);
}

/**
 * Obtiene el estado de la factura desde la sesión usando el token
 */
private FacturaCompraState obtenerEstado(HttpSession session, String token) {
    if (token == null || token.isEmpty()) {
        return null;
    }
    return (FacturaCompraState) session.getAttribute(SESSION_PREFIX + token);
}

/**
 * Guarda el estado de la factura en la sesión
 */
private void guardarEstado(HttpSession session, String token, FacturaCompraState estado) {
    session.setAttribute(SESSION_PREFIX + token, estado);
}

/**
 * Elimina el estado de la sesión (al guardar, cancelar o cerrar)
 */
private void limpiarEstado(HttpSession session, String token) {
    if (token != null) {
        session.removeAttribute(SESSION_PREFIX + token);
    }
}

/**
 * Pasa los datos del estado a los request attributes para el JSP
 */
private void cargarDatosParaVista(HttpServletRequest request, FacturaCompraState estado, String token) {
    request.setAttribute("token", token);
    request.setAttribute("facturaCompra", estado.facturaCompra);
    request.setAttribute("listaFacturaCompraDetalle", estado.listaDetalle);
    request.setAttribute("proveedorSeleccionado", estado.proveedorSeleccionado);
    request.setAttribute("sucursalSeleccionada", estado.sucursalSeleccionada);
    request.setAttribute("ordenCompraSeleccionada", estado.ordenCompraSeleccionada);
    request.setAttribute("detalleSeleccionado", estado.detalleSeleccionado);
    request.setAttribute("esNuevo", estado.esNuevo);

    // Listas para modales
    request.setAttribute("listaOrdenesCompra", estado.listaOrdenesCompra);
    request.setAttribute("listaFacturasCompra", estado.listaFacturasCompra);
    request.setAttribute("listaProveedores", estado.listaProveedores);
    request.setAttribute("listaSucursales", estado.listaSucursales);
    request.setAttribute("listaArticulos", estado.listaArticulos);
    request.setAttribute("listaTipoImpuesto", estado.listaTipoImpuesto);
}
```

### Servlet Completo

```java
package controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import modelo.*;
import service.*;

@WebServlet(name = "FacturaCompraServlet", urlPatterns = {"/FacturaCompraServlet"})
public class FacturaCompraServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(FacturaCompraServlet.class.getName());

    // Prefijo para las claves de sesión
    private static final String SESSION_PREFIX = "facturaCompra_";

    // Services (estos sí pueden ser de instancia, son stateless)
    private final FacturaCompraService facturaCompraService = new FacturaCompraService();
    private final FacturaCompraDetalleService facturaCompraDetalleService = new FacturaCompraDetalleService();
    private final OrdenCompraService ordenCompraService = new OrdenCompraService();
    private final OrdenCompraDetalleService ordenCompraDetalleService = new OrdenCompraDetalleService();
    private final PedidoCompraService pedidoCompraService = new PedidoCompraService();
    private final PresupuestoService presupuestoService = new PresupuestoService();
    private final CuentaPagarService cuentaPagarService = new CuentaPagarService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final SucursalService sucursalService = new SucursalService();
    private final ArticuloService articuloService = new ArticuloService();
    private final TipoImpuestoService tipoImpuestoService = new TipoImpuestoService();

    // ==================== CLASE INTERNA PARA ESTADO DEL DOCUMENTO ====================

    private static class FacturaCompraState {
        FacturaCompra facturaCompra = new FacturaCompra();
        List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
        Proveedor proveedorSeleccionado;
        Sucursal sucursalSeleccionada;
        OrdenCompra ordenCompraSeleccionada;
        boolean esNuevo = false;

        List<OrdenCompra> listaOrdenesCompra;
        List<Proveedor> listaProveedores;
        List<Sucursal> listaSucursales;
    }

    // ==================== MÉTODOS HELPER PARA SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private FacturaCompraState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (FacturaCompraState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, FacturaCompraState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    private void cargarDatosParaVista(HttpServletRequest request, FacturaCompraState estado, String token) {
        request.setAttribute("token", token);
        request.setAttribute("facturaCompra", estado.facturaCompra);
        request.setAttribute("listaFacturaCompraDetalle", estado.listaDetalle);
        request.setAttribute("proveedorSeleccionado", estado.proveedorSeleccionado);
        request.setAttribute("sucursalSeleccionada", estado.sucursalSeleccionada);
        request.setAttribute("ordenCompraSeleccionada", estado.ordenCompraSeleccionada);
        request.setAttribute("esNuevo", estado.esNuevo);
        request.setAttribute("listaOrdenesCompra", estado.listaOrdenesCompra);
        request.setAttribute("listaProveedores", estado.listaProveedores);
        request.setAttribute("listaSucursales", estado.listaSucursales);
    }

    // ==================== PROCESO PRINCIPAL ====================

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");
        String accion = request.getParameter("accion");
        String token = request.getParameter("token");

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (!"FacturaCompra".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }

        // La implementación actual usa métodos delegados para cada acción
        // Esto mejora la legibilidad y mantenibilidad del código
        try {
            switch (accion) {
                case "Nuevo":
                    accionNuevo(request, response, session, usuario);
                    break;
                case "ListarModal":
                    accionListarModal(request, response);
                    break;
                case "CargarFactura":
                    accionCargarFactura(request, response, session);
                    break;
                case "CargarOrdenCompra":
                    accionCargarOrdenCompra(request, response, session, token);
                    break;
                case "CargarProveedor":
                    accionCargarProveedor(request, response, session, token);
                    break;
                case "CambiarSucursal":
                    accionCambiarSucursal(request, response, session, token);
                    break;
                case "CambiarCondicion":
                    accionCambiarCondicion(request, response, session, token);
                    break;
                case "CambiarTipoFactura":
                    accionCambiarTipoFactura(request, response, session, token);
                    break;
                case "AgregarArticulo":
                    accionAgregarArticulo(request, response, session, token);
                    break;
                case "EditarArticulo":
                    accionEditarArticulo(request, response, session, token);
                    break;
                case "ActualizarArticulo":
                    accionActualizarArticulo(request, response, session, token);
                    break;
                case "EliminarArticulo":
                    accionEliminarArticulo(request, response, session, token);
                    break;
                case "Guardar":
                    accionGuardar(request, response, session, token);
                    break;
                case "Anular":
                    accionAnular(request, response, session, token);
                    break;
                case "Cancelar":
                    accionCancelar(request, response, session, token);
                    break;
                default:
                    accionListarModal(request, response);
                    break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en FacturaCompraServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            forward(request, response, JSP_FACTURA);
        }
    }

    // ==================== ACCIONES (Métodos Delegados) ====================

    /**
     * Crear nueva factura de compra
     */
    private void accionNuevo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Usuario usuario) throws ServletException, IOException, SQLException {

        String nuevoToken = generarToken();
        FacturaCompraState estado = new FacturaCompraState();

        estado.esNuevo = true;
        estado.facturaCompra.setUsuario(usuario);
        estado.facturaCompra.setFechaCarga(new Date());
        estado.facturaCompra.setEstado("Pendiente");

        // Cargar datos para modales
        estado.listaFacturasCompra = facturaCompraService.listarFacturasCompra();
        estado.listaOrdenesCompra = ordenCompraService.listarOrdenesCompraConDetalles();
        estado.listaProveedores = proveedorService.listarProveedores();
        estado.listaSucursales = sucursalService.listarSucursles();
        estado.listaArticulos = articuloService.listarArticulo();
        estado.listaTipoImpuesto = tipoImpuestoService.listarTipoImpuesto();

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);

        forward(request, response, JSP_FACTURA);
    }

    /**
     * Cambiar tipo de factura (mercadería, gasto, fondo fijo)
     * Se usa para mostrar/ocultar campos según el tipo
     */
    private void accionCambiarTipoFactura(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        leerDatosFormulario(request, estado);

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    /**
     * Agregar artículo al detalle
     * Soporta artículos con código (mercadería) y sin código (gastos con descripción libre)
     */
    private void accionAgregarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {

        FacturaCompraState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        leerDatosFormulario(request, estado);

        String idArticuloStr = request.getParameter("idArticulo");
        String cantidadStr = request.getParameter("cantidad");
        String precioStr = request.getParameter("precioCompra");
        String descripcion = request.getParameter("descripcion");
        String idTipoImpuestoStr = request.getParameter("idTipoImpuesto");

        if (idArticuloStr == null || idArticuloStr.isEmpty()) {
            // Factura de gasto: solo descripción, sin artículo
            if (descripcion != null && !descripcion.isEmpty() && cantidadStr != null && precioStr != null) {
                FacturaCompraDetalle detalle = new FacturaCompraDetalle();
                detalle.setDescripcion(descripcion);
                detalle.setCantidad(Long.parseLong(cantidadStr));
                detalle.setPrecioCompra(Long.parseLong(precioStr));

                // Asignar tipo de impuesto para cálculo de IVA
                if (idTipoImpuestoStr != null && !idTipoImpuestoStr.isEmpty()) {
                    TipoImpuesto tipoImpuesto = tipoImpuestoService.getTipoImpuesto(Long.parseLong(idTipoImpuestoStr));
                    detalle.setTipoImpuesto(tipoImpuesto);
                }

                estado.listaDetalle.add(detalle);
                mostrarMensaje(request, "Artículo agregado", "alert-success");
            }
        } else {
            // Factura con artículo codificado
            Long idArticulo = Long.parseLong(idArticuloStr);
            Articulo articulo = articuloService.getArticulo(idArticulo);

            if (articulo != null && cantidadStr != null && precioStr != null) {
                FacturaCompraDetalle detalle = new FacturaCompraDetalle();
                detalle.setArticulo(articulo);
                detalle.setCantidad(Long.parseLong(cantidadStr));
                detalle.setPrecioCompra(Long.parseLong(precioStr));
                estado.listaDetalle.add(detalle);
                mostrarMensaje(request, "Artículo agregado correctamente", "alert-success");
            }
        }

        estado.detalleSeleccionado = null;
        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_FACTURA);
    }

    // ... otros métodos delegados (CargarFactura, CargarOrdenCompra, etc.)

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
```

---

## Cambios en el JSP

### Token Oculto en Formularios

Cada formulario debe incluir el token:

```jsp
<%-- Token oculto en cada formulario --%>
<form action="FacturaCompraServlet?menu=FacturaCompra&accion=CambiarCondicion" method="POST">
    <input type="hidden" name="token" value="${token}">
    <select name="condicion" class="form-select" onchange="this.form.submit()">
        <option value="">Seleccionar...</option>
        <option value="Contado" ${facturaCompra.condicion eq 'Contado' ? 'selected' : ''}>Contado</option>
        <option value="Credito" ${facturaCompra.condicion eq 'Credito' ? 'selected' : ''}>Crédito</option>
    </select>
</form>
```

### Links con Token

```jsp
<%-- Links también necesitan el token --%>
<a href="FacturaCompraServlet?menu=FacturaCompra&accion=CargarOrdenCompra&idOrden=${orden.idOrdenCompra}&token=${token}"
   class="btn btn-primary btn-sm">Seleccionar</a>
```

### Botones de Acción

```jsp
<%-- Botón Guardar --%>
<form action="FacturaCompraServlet?menu=FacturaCompra&accion=Guardar" method="POST">
    <input type="hidden" name="token" value="${token}">
    <button type="submit" class="btn btn-success">Guardar</button>
</form>

<%-- Botón Cancelar --%>
<a href="FacturaCompraServlet?menu=FacturaCompra&accion=Cancelar&token=${token}"
   class="btn btn-secondary">Cancelar</a>
```

---

## Flujo de Acciones: JSP → JavaScript → Servlet

### Dos Enfoques de Manejo de Acciones

El proyecto utiliza dos enfoques diferentes para enviar acciones al servlet:

| Enfoque | Usado en | Características |
|---------|----------|-----------------|
| **Múltiples Formularios** | pedidoCompra, presupuesto, ordenCompra | Cada acción tiene su propio `<form>` |
| **Formulario Único + JS** | facturaCompra | Un form principal con funciones JavaScript |

---

### Enfoque 1: Múltiples Formularios (pedidoCompra, presupuesto, ordenCompra)

#### Estructura HTML

```jsp
<!-- Formulario 1: Cambiar sucursal -->
<form action="PedidoCompraServlet?menu=PedidoCompra&accion=CargarDeposito" method="POST">
    <select name="idSucursal" onchange="this.form.submit()">
        <option value="1">Sucursal Central</option>
        <option value="2">Sucursal Norte</option>
    </select>
</form>

<!-- Formulario 2: Botones de acción -->
<form action="PedidoCompraServlet?menu=PedidoCompra" method="POST">
    <button name="accion" value="Nuevo" type="submit">Nuevo</button>
    <button name="accion" value="Guardar" type="submit">Guardar</button>
</form>

<!-- Formulario 3: Agregar artículo -->
<form action="PedidoCompraServlet?menu=PedidoCompra&accion=AgregarArticulo" method="POST">
    <input name="idArticulo" value="123">
    <input name="cantidad" value="10">
    <button type="submit">Agregar</button>
</form>
```

#### Diagrama de Flujo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              NAVEGADOR                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│  1. Usuario cambia el select de sucursal                                    │
│     ┌─────────────────────────────────────────┐                             │
│     │ <select onchange="this.form.submit()">  │                             │
│     │   <option value="2">Sucursal Norte      │  ← Usuario selecciona       │
│     │ </select>                               │                             │
│     └─────────────────────────────────────────┘                             │
│                          │                                                   │
│  2. onchange dispara this.form.submit()                                     │
│                          │                                                   │
│  3. Se envía solo este formulario:                                          │
│     POST /PedidoCompraServlet?menu=PedidoCompra&accion=CargarDeposito       │
│     Body: idSucursal=2                                                      │
│                          │                                                   │
│     ⚠️ PROBLEMA: Si había datos en otros forms, NO se envían                │
└──────────────────────────┼──────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SERVLET                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│  protected void processRequest(request, response) {                         │
│                                                                             │
│      // 4. Obtener parámetros de la URL (query string)                      │
│      String menu = request.getParameter("menu");      // "PedidoCompra"     │
│      String accion = request.getParameter("accion");  // "CargarDeposito"   │
│                                                                             │
│      // 5. Obtener parámetros del body (form data)                          │
│      String idSucursal = request.getParameter("idSucursal");  // "2"        │
│                                                                             │
│      // 6. Procesar según la acción                                         │
│      switch (accion) {                                                      │
│          case "CargarDeposito":                                             │
│              // Cargar depósitos de la sucursal seleccionada                │
│              List<Deposito> depositos = depositoService.listarPorSucursal(  │
│                  Long.parseLong(idSucursal));                               │
│              request.setAttribute("listaDepositos", depositos);             │
│              break;                                                         │
│      }                                                                      │
│                                                                             │
│      // 7. Redirigir a la vista                                             │
│      request.getRequestDispatcher("pedidoCompra.jsp").forward(req, res);    │
│  }                                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Ventajas y Desventajas

| Ventajas | Desventajas |
|----------|-------------|
| No requiere JavaScript | Pérdida de datos en otros formularios |
| Fácil de entender el flujo | Necesita hiddens duplicados (token, ids) |
| Cada form es independiente | Más código HTML repetido |
| Funciona sin JS habilitado | Difícil validar todos los campos juntos |

---

### Enfoque 2: Formulario Único con JavaScript (facturaCompra)

#### Estructura HTML

```jsp
<!-- UN SOLO formulario principal que contiene todo -->
<form id="formPrincipal" method="post" action="FacturaCompraServlet">
    <!-- Campos ocultos para control -->
    <input type="hidden" name="menu" value="FacturaCompra">
    <input type="hidden" name="token" value="${token}">
    <input type="hidden" name="accion" id="accionPrincipal" value="Guardar">

    <!-- Sección: Cabecera -->
    <select id="sucursal" name="idSucursal" onchange="cambiarSucursal();">
        <option value="1">Sucursal Central</option>
        <option value="2">Sucursal Norte</option>
    </select>

    <select id="condicionCompra" name="condicion" onchange="cambiarCondicion();">
        <option value="Contado">Contado</option>
        <option value="Credito">Crédito</option>
    </select>

    <input id="plazoCredito" name="plazo" value="30">

    <!-- Sección: Artículos -->
    <input name="idArticulo" id="idArticuloAgregar" value="">
    <input name="cantidad" value="10">
    <input name="precioCompra" value="50000">

    <!-- Sección: Observaciones -->
    <textarea name="observacion">Nota de prueba</textarea>

    <!-- Botones -->
    <button type="button" onclick="guardarFactura()">Guardar</button>
    <button type="button" onclick="agregarArticulo()">Agregar Artículo</button>
</form>
```

#### Funciones JavaScript

```javascript
// Cada función:
// 1. Cambia el valor del input hidden "accion"
// 2. Envía el formulario completo

function guardarFactura() {
    document.getElementById('accionPrincipal').value = 'Guardar';
    document.getElementById('formPrincipal').submit();
}

function cambiarSucursal() {
    document.getElementById('accionPrincipal').value = 'CambiarSucursal';
    document.getElementById('formPrincipal').submit();
}

function cambiarCondicion() {
    var condicion = document.getElementById('condicionCompra').value;
    var plazoInput = document.getElementById('plazoCredito');

    // Lógica del lado del cliente (opcional)
    if (condicion === 'Credito') {
        plazoInput.disabled = false;
    } else {
        plazoInput.disabled = true;
        plazoInput.value = 0;
    }

    document.getElementById('accionPrincipal').value = 'CambiarCondicion';
    document.getElementById('formPrincipal').submit();
}

function agregarArticulo() {
    document.getElementById('accionPrincipal').value = 'AgregarArticulo';
    document.getElementById('formPrincipal').submit();
}

// Para seleccionar artículo desde modal (no envía form, solo llena campos)
function seleccionarArticulo(idArticulo, descripcion, precioCompra) {
    document.getElementById('idArticuloAgregar').value = idArticulo;
    document.getElementById('descripcionArticulo').value = descripcion;
    document.getElementById('precioCompraArticulo').value = precioCompra;
    // El modal se cierra y el usuario hace clic en "Agregar"
}
```

#### Diagrama de Flujo Detallado

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              NAVEGADOR                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. Estado inicial del formulario:                                          │
│     ┌─────────────────────────────────────────────────────────────────┐     │
│     │ <input type="hidden" name="accion" id="accionPrincipal"         │     │
│     │        value="Guardar">  ← Valor por defecto                    │     │
│     │                                                                 │     │
│     │ <select id="condicionCompra" value="Contado">                   │     │
│     │ <input id="plazoCredito" value="30">                            │     │
│     │ <input name="idSucursal" value="1">                             │     │
│     │ <textarea name="observacion">Nota de prueba</textarea>          │     │
│     └─────────────────────────────────────────────────────────────────┘     │
│                                                                             │
│  2. Usuario cambia "Condición" a "Crédito"                                  │
│     → Dispara: onchange="cambiarCondicion();"                               │
│                          │                                                   │
│                          ▼                                                   │
│  3. Se ejecuta la función JavaScript:                                       │
│     ┌─────────────────────────────────────────────────────────────────┐     │
│     │ function cambiarCondicion() {                                   │     │
│     │     // Cambia el hidden de "Guardar" a "CambiarCondicion"       │     │
│     │     document.getElementById('accionPrincipal').value =          │     │
│     │         'CambiarCondicion';                                     │     │
│     │                                                                 │     │
│     │     // Envía TODO el formulario                                 │     │
│     │     document.getElementById('formPrincipal').submit();          │     │
│     │ }                                                               │     │
│     └─────────────────────────────────────────────────────────────────┘     │
│                          │                                                   │
│  4. Se envía el formulario completo:                                        │
│     POST /FacturaCompraServlet                                              │
│     Body (form-urlencoded):                                                 │
│       menu=FacturaCompra                                                    │
│       token=abc12345                                                        │
│       accion=CambiarCondicion    ← Cambiado por JS                          │
│       idSucursal=1               ← Se envía aunque no cambió                │
│       condicion=Credito          ← El nuevo valor                           │
│       plazo=30                   ← Se envía aunque no cambió                │
│       observacion=Nota de prueba ← Se envía aunque no cambió                │
│                                                                             │
│     ✅ VENTAJA: Todos los datos del formulario se envían juntos             │
└──────────────────────────┼──────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              SERVLET                                         │
├─────────────────────────────────────────────────────────────────────────────┤
│  protected void processRequest(request, response) {                         │
│                                                                             │
│      // 5. Obtener parámetros (todos vienen del body del form)              │
│      String menu = request.getParameter("menu");      // "FacturaCompra"    │
│      String token = request.getParameter("token");    // "abc12345"         │
│      String accion = request.getParameter("accion");  // "CambiarCondicion" │
│                                                                             │
│      // 6. Recuperar estado de la sesión usando el token                    │
│      HttpSession session = request.getSession();                            │
│      FacturaCompraState estado = obtenerEstado(session, token);             │
│                                                                             │
│      if (estado == null) {                                                  │
│          // Token inválido o sesión expirada                                │
│          mostrarMensaje(request, "Sesión expirada", "alert-warning");       │
│          response.sendRedirect("...?accion=ListarModal");                   │
│          return;                                                            │
│      }                                                                      │
│                                                                             │
│      // 7. Procesar según la acción                                         │
│      switch (accion) {                                                      │
│          case "CambiarCondicion":                                           │
│              accionCambiarCondicion(request, response, session, token);     │
│              break;                                                         │
│          case "Guardar":                                                    │
│              accionGuardar(request, response, session, token);              │
│              break;                                                         │
│          // ... otras acciones                                              │
│      }                                                                      │
│  }                                                                          │
│                                                                             │
│  // 8. Método delegado para la acción específica                            │
│  private void accionCambiarCondicion(request, response, session, token) {   │
│                                                                             │
│      FacturaCompraState estado = obtenerEstado(session, token);             │
│                                                                             │
│      // 9. Leer TODOS los datos del formulario para no perderlos            │
│      leerDatosFormulario(request, estado);                                  │
│      // Esto incluye: condicion, plazo, observacion, idSucursal, etc.       │
│                                                                             │
│      // 10. Aplicar lógica específica de esta acción                        │
│      if ("Contado".equals(estado.facturaCompra.getCondicion())) {           │
│          estado.facturaCompra.setPlazo(0);                                  │
│      }                                                                      │
│                                                                             │
│      // 11. Guardar estado actualizado en sesión                            │
│      guardarEstado(session, token, estado);                                 │
│                                                                             │
│      // 12. Preparar datos para la vista                                    │
│      cargarDatosParaVista(request, estado, token);                          │
│                                                                             │
│      // 13. Mostrar la vista nuevamente                                     │
│      forward(request, response, "facturaCompra.jsp");                       │
│  }                                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         JSP (Respuesta)                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│  14. El JSP renderiza con los datos actualizados:                           │
│                                                                             │
│      - ${token} = "abc12345" (mismo token, misma sesión de trabajo)         │
│      - ${facturaCompra.condicion} = "Credito"                               │
│      - ${facturaCompra.plazo} = 30                                          │
│      - ${facturaCompra.observacion} = "Nota de prueba"                      │
│                                                                             │
│  15. El usuario ve el formulario actualizado sin perder ningún dato         │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Método leerDatosFormulario (Clave para no perder datos)

```java
/**
 * Lee todos los datos del formulario y los guarda en el estado.
 * IMPORTANTE: Se debe llamar en CADA acción para preservar los datos
 * que el usuario ya ingresó pero que no son relevantes para esta acción.
 */
private void leerDatosFormulario(HttpServletRequest request, FacturaCompraState estado) {

    // Número de comprobante
    String numeroComprobanteStr = request.getParameter("numeroComprobante");
    if (numeroComprobanteStr != null && !numeroComprobanteStr.isEmpty()) {
        estado.facturaCompra.setNumero(numeroComprobanteStr);
    }

    // Timbrado
    String timbradoStr = request.getParameter("timbrado");
    if (timbradoStr != null && !timbradoStr.isEmpty()) {
        estado.facturaCompra.setTimbrado(Integer.parseInt(timbradoStr));
    }

    // Fechas
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String fechaEmisionStr = request.getParameter("fechaEmision");
    if (fechaEmisionStr != null && !fechaEmisionStr.isEmpty()) {
        estado.facturaCompra.setFechaEmision(sdf.parse(fechaEmisionStr));
    }

    // Condición de compra
    String condicion = request.getParameter("condicion");
    if (condicion != null && !condicion.isEmpty()) {
        estado.facturaCompra.setCondicion(condicion);
    }

    // Plazo
    String plazoStr = request.getParameter("plazo");
    if (plazoStr != null && !plazoStr.isEmpty()) {
        estado.facturaCompra.setPlazo(Integer.parseInt(plazoStr));
    }

    // Tipo de factura
    String tipoFactura = request.getParameter("tipoFactura");
    if (tipoFactura != null && !tipoFactura.isEmpty()) {
        estado.facturaCompra.setTipoFactura(tipoFactura);
    }

    // Observación
    String observacion = request.getParameter("observacion");
    if (observacion != null) {
        estado.facturaCompra.setObservacion(observacion);
    }
}
```

#### Ventajas y Desventajas

| Ventajas | Desventajas |
|----------|-------------|
| Todos los datos se envían siempre | Requiere JavaScript habilitado |
| No se pierden datos al cambiar selects | Envía datos innecesarios al servidor |
| Fácil agregar validación JS antes de enviar | Debugging más complejo |
| Menos código HTML (un solo form) | Necesita IDs únicos para cada elemento |
| Mejor integración con Session+Token | Si falla el JS, no funciona |

---

### Comparación Visual

```
MÚLTIPLES FORMULARIOS:                    FORMULARIO ÚNICO + JS:

┌─────────────────────┐                   ┌─────────────────────────────────┐
│ Form 1: Sucursal    │                   │ Form Principal                  │
│ ┌─────────────────┐ │                   │ ┌─────────────────────────────┐ │
│ │ idSucursal=2    │─┼──► Servlet        │ │ hidden: accion="Guardar"    │ │
│ └─────────────────┘ │    (solo esto)    │ │ idSucursal=2                │ │
└─────────────────────┘                   │ │ condicion=Credito           │ │
                                          │ │ plazo=30                    │ │
┌─────────────────────┐                   │ │ observacion=...             │ │
│ Form 2: Condición   │                   │ └─────────────────────────────┘ │
│ ┌─────────────────┐ │                   │              │                  │
│ │ condicion=Cred. │─┼──► Servlet        │              ▼                  │
│ └─────────────────┘ │    (solo esto)    │      cambiarCondicion()         │
└─────────────────────┘                   │              │                  │
                                          │              ▼                  │
┌─────────────────────┐                   │      accion = "CambiarCond"     │
│ Form 3: Guardar     │                   │              │                  │
│ ┌─────────────────┐ │                   │              ▼                  │
│ │ (vacío)         │─┼──► Servlet        │      form.submit() ─────────────┼──► Servlet
│ └─────────────────┘ │                   │      (TODO se envía)            │    (todo junto)
└─────────────────────┘                   └─────────────────────────────────┘
```

---

### Recomendación

Para el patrón **Session + Token** implementado en facturaCompra:

- **Usar Formulario Único + JS** es más apropiado porque:
  1. El token siempre se envía con cada petición
  2. Los datos del formulario no se pierden entre acciones
  3. El método `leerDatosFormulario()` captura todo el estado actual
  4. Facilita la validación antes de enviar

Para módulos más simples sin estado complejo (como ABM básicos):

- **Múltiples Formularios** puede ser suficiente y más simple de mantener

---

## Estilo de Vista: form-floating

El nuevo estándar para las vistas será usando `form-floating` de Bootstrap:

```jsp
<div class="col-md-2">
    <div class="form-floating mb-3 mb-md-0">
        <input class="form-control" id="usuario" type="text"
               placeholder="Usuario" value="${usuario.nombre}" disabled />
        <label for="usuario">Usuario</label>
    </div>
</div>

<div class="col-md-2">
    <div class="form-floating mb-3 mb-md-0">
        <select class="form-control" id="condicion" name="condicion">
            <option value="">Seleccionar...</option>
            <option value="Contado">Contado</option>
            <option value="Credito">Crédito</option>
        </select>
        <label for="condicion">Condición de Compra</label>
    </div>
</div>
```

### Diferencia con estilo anterior

| Estilo Anterior (pedidoCompra) | Estilo Nuevo (facturaCompra) |
|-------------------------------|------------------------------|
| `d-flex align-items-center` | `form-floating` |
| Label al lado del input | Label flotante dentro del input |
| `<label class="me-2">` | `<label for="id">` |

---

## Alternativa: JSF + Managed Beans

### Concepto

JSF (JavaServer Faces) es un framework MVC que maneja el estado automáticamente mediante **Managed Beans** (o CDI Beans). El estado se gestiona según el **scope** del bean:

```java
@Named("facturaCompraBean")
@ViewScoped  // El estado vive mientras el usuario esté en la misma vista
public class FacturaCompraBean implements Serializable {

    private FacturaCompra facturaCompra = new FacturaCompra();
    private List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
    private Proveedor proveedorSeleccionado;

    // Getters y setters...

    public void cargarOrdenCompra(Long idOrden) {
        // Lógica de negocio
    }

    public String guardar() {
        // Guardar en BD
        return "lista?faces-redirect=true"; // Navegación
    }
}
```

### Scopes Disponibles en JSF

| Scope | Duración | Uso típico |
|-------|----------|------------|
| `@RequestScoped` | Una petición HTTP | Formularios simples |
| `@ViewScoped` | Mientras esté en la misma página | **Formularios complejos (ideal para facturas)** |
| `@SessionScoped` | Toda la sesión del usuario | Carrito de compras, usuario logueado |
| `@ApplicationScoped` | Toda la aplicación | Configuraciones, catálogos |
| `@ConversationScoped` | Controlado manualmente | Wizards multi-página |

### Ejemplo Completo con JSF

**Bean (FacturaCompraBean.java):**

```java
package bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import modelo.*;
import service.*;

@Named("facturaCompraBean")
@ViewScoped
public class FacturaCompraBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Inyección de dependencias (CDI)
    @Inject
    private FacturaCompraService facturaCompraService;

    @Inject
    private OrdenCompraService ordenCompraService;

    @Inject
    private ProveedorService proveedorService;

    @Inject
    private SucursalService sucursalService;

    // Estado del documento - se mantiene automáticamente por el scope
    private FacturaCompra facturaCompra = new FacturaCompra();
    private List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
    private OrdenCompra ordenSeleccionada;
    private Proveedor proveedorSeleccionado;
    private Sucursal sucursalSeleccionada;

    // Listas para combos/modales
    private List<OrdenCompra> listaOrdenes;
    private List<Proveedor> listaProveedores;
    private List<Sucursal> listaSucursales;

    private boolean esNuevo = true;

    /**
     * Se ejecuta al crear el bean (cuando se carga la vista)
     */
    @PostConstruct
    public void init() {
        // Cargar datos iniciales
        listaOrdenes = ordenCompraService.listarPendientes();
        listaProveedores = proveedorService.listarProveedores();
        listaSucursales = sucursalService.listarSucursales();

        // Inicializar factura
        facturaCompra.setFechaCarga(new Date());
        facturaCompra.setEstado("Pendiente");
    }

    /**
     * Cargar una factura existente para edición
     */
    public void cargarFactura(Long idFactura) {
        facturaCompra = facturaCompraService.getFacturaCompra(idFactura);
        listaDetalle = facturaCompraService.listarDetallesPorFactura(idFactura);
        proveedorSeleccionado = facturaCompra.getProveedor();
        sucursalSeleccionada = facturaCompra.getSucursal();
        ordenSeleccionada = facturaCompra.getOrdenCompra();
        esNuevo = false;
    }

    /**
     * Seleccionar orden de compra desde modal
     * Los datos se mantienen automáticamente - no hay que "pasar" nada
     */
    public void seleccionarOrden(OrdenCompra orden) {
        this.ordenSeleccionada = orden;
        this.facturaCompra.setOrdenCompra(orden);
        this.facturaCompra.setProveedor(orden.getProveedor());
        this.facturaCompra.setSucursal(orden.getSucursal());
        this.facturaCompra.setCondicion(orden.getCondicionCompra());
        this.proveedorSeleccionado = orden.getProveedor();
        this.sucursalSeleccionada = orden.getSucursal();

        // Cargar detalles de la orden
        // ...

        agregarMensaje(FacesMessage.SEVERITY_INFO, "Orden cargada",
            "Se cargó la orden de compra #" + orden.getIdOrdenCompra());
    }

    /**
     * Cambiar condición de compra
     * Se llama automáticamente con AJAX cuando cambia el select
     */
    public void cambiarCondicion() {
        if ("Contado".equals(facturaCompra.getCondicion())) {
            facturaCompra.setPlazo(0);
        }
        // No hay que hacer nada más, el estado se mantiene
    }

    /**
     * Agregar artículo al detalle
     */
    public void agregarArticulo(Articulo articulo, int cantidad, double precio) {
        FacturaCompraDetalle detalle = new FacturaCompraDetalle();
        detalle.setArticulo(articulo);
        detalle.setCantidad(cantidad);
        detalle.setPrecioCompra(precio);
        listaDetalle.add(detalle);
    }

    /**
     * Eliminar artículo del detalle
     */
    public void eliminarArticulo(FacturaCompraDetalle detalle) {
        listaDetalle.remove(detalle);
    }

    /**
     * Guardar factura en BD
     */
    public String guardar() {
        // Validaciones
        if (listaDetalle.isEmpty()) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Validación",
                "Debe agregar al menos un artículo");
            return null; // Quedarse en la misma página
        }

        if (proveedorSeleccionado == null) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Validación",
                "Debe seleccionar un proveedor");
            return null;
        }

        try {
            Long idInsertado = facturaCompraService.insertarFacturaCompra(
                facturaCompra, listaDetalle);

            agregarMensaje(FacesMessage.SEVERITY_INFO, "Éxito",
                "Factura guardada con ID: " + idInsertado);

            // Navegar a la lista (el bean se destruye automáticamente)
            return "listaFacturasCompra?faces-redirect=true";

        } catch (Exception e) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Error",
                "No se pudo guardar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cancelar y volver a la lista
     */
    public String cancelar() {
        // El bean se destruye automáticamente al navegar
        return "listaFacturasCompra?faces-redirect=true";
    }

    /**
     * Calcular total de la factura
     */
    public double getTotal() {
        return listaDetalle.stream()
            .mapToDouble(d -> d.getCantidad() * d.getPrecioCompra())
            .sum();
    }

    private void agregarMensaje(FacesMessage.Severity severity, String titulo, String detalle) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(severity, titulo, detalle));
    }

    // ==================== GETTERS Y SETTERS ====================

    public FacturaCompra getFacturaCompra() {
        return facturaCompra;
    }

    public void setFacturaCompra(FacturaCompra facturaCompra) {
        this.facturaCompra = facturaCompra;
    }

    public List<FacturaCompraDetalle> getListaDetalle() {
        return listaDetalle;
    }

    public void setListaDetalle(List<FacturaCompraDetalle> listaDetalle) {
        this.listaDetalle = listaDetalle;
    }

    public OrdenCompra getOrdenSeleccionada() {
        return ordenSeleccionada;
    }

    public void setOrdenSeleccionada(OrdenCompra ordenSeleccionada) {
        this.ordenSeleccionada = ordenSeleccionada;
    }

    public List<OrdenCompra> getListaOrdenes() {
        return listaOrdenes;
    }

    public List<Proveedor> getListaProveedores() {
        return listaProveedores;
    }

    public List<Sucursal> getListaSucursales() {
        return listaSucursales;
    }

    public Proveedor getProveedorSeleccionado() {
        return proveedorSeleccionado;
    }

    public void setProveedorSeleccionado(Proveedor proveedorSeleccionado) {
        this.proveedorSeleccionado = proveedorSeleccionado;
    }

    public Sucursal getSucursalSeleccionada() {
        return sucursalSeleccionada;
    }

    public void setSucursalSeleccionada(Sucursal sucursalSeleccionada) {
        this.sucursalSeleccionada = sucursalSeleccionada;
    }

    public boolean isEsNuevo() {
        return esNuevo;
    }
}
```

**Vista JSF (facturaCompra.xhtml):**

```xhtml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://xmlns.jcp.org/jsf/html"
      xmlns:f="http://xmlns.jcp.org/jsf/core"
      xmlns:p="http://primefaces.org/ui"
      xmlns:ui="http://xmlns.jcp.org/jsf/facelets">

<h:head>
    <title>Factura Compra</title>
</h:head>

<h:body>
    <h:form id="formFactura">

        <!-- Mensajes globales -->
        <p:growl id="mensajes" showDetail="true" />

        <!-- NO HAY TOKENS OCULTOS - JSF maneja el estado automáticamente -->

        <div class="card">
            <div class="card-header">
                <h3>Factura de Compra</h3>
            </div>
            <div class="card-body">

                <!-- Fila 1: Datos básicos -->
                <div class="row mb-3">
                    <div class="col-md-2">
                        <p:outputLabel value="Fecha:" for="fecha" />
                        <p:calendar id="fecha" value="#{facturaCompraBean.facturaCompra.fechaCarga}"
                                    pattern="dd/MM/yyyy" disabled="true" />
                    </div>
                    <div class="col-md-2">
                        <p:outputLabel value="Estado:" for="estado" />
                        <p:inputText id="estado" value="#{facturaCompraBean.facturaCompra.estado}"
                                     disabled="true" />
                    </div>
                    <div class="col-md-2">
                        <p:outputLabel value="Condición:" for="condicion" />
                        <p:selectOneMenu id="condicion" value="#{facturaCompraBean.facturaCompra.condicion}">
                            <f:selectItem itemLabel="Seleccionar..." itemValue="" />
                            <f:selectItem itemLabel="Contado" itemValue="Contado" />
                            <f:selectItem itemLabel="Crédito" itemValue="Credito" />
                            <!-- AJAX automático: actualiza el panel de plazo al cambiar -->
                            <p:ajax event="change"
                                    listener="#{facturaCompraBean.cambiarCondicion}"
                                    update="panelPlazo" />
                        </p:selectOneMenu>
                    </div>
                    <div class="col-md-2">
                        <p:outputPanel id="panelPlazo">
                            <p:outputLabel value="Plazo (días):" for="plazo" />
                            <p:inputNumber id="plazo" value="#{facturaCompraBean.facturaCompra.plazo}"
                                           disabled="#{facturaCompraBean.facturaCompra.condicion eq 'Contado'}" />
                        </p:outputPanel>
                    </div>
                </div>

                <!-- Fila 2: Proveedor -->
                <div class="row mb-3">
                    <div class="col-md-2">
                        <p:commandButton value="Buscar Orden" type="button"
                                         onclick="PF('dlgOrdenes').show()" />
                    </div>
                    <div class="col-md-3">
                        <p:outputLabel value="Proveedor:" />
                        <p:inputText value="#{facturaCompraBean.proveedorSeleccionado.razonSocial}"
                                     disabled="true" />
                    </div>
                    <div class="col-md-2">
                        <p:outputLabel value="RUC:" />
                        <p:inputText value="#{facturaCompraBean.proveedorSeleccionado.ruc}"
                                     disabled="true" />
                    </div>
                </div>

                <!-- Tabla de detalles -->
                <p:dataTable id="tablaDetalles" value="#{facturaCompraBean.listaDetalle}" var="det"
                             emptyMessage="No hay artículos agregados">
                    <p:column headerText="Artículo">
                        <h:outputText value="#{det.articulo.descripcion}" />
                    </p:column>
                    <p:column headerText="Cantidad">
                        <h:outputText value="#{det.cantidad}" />
                    </p:column>
                    <p:column headerText="Precio">
                        <h:outputText value="#{det.precioCompra}">
                            <f:convertNumber pattern="#,##0" />
                        </h:outputText>
                    </p:column>
                    <p:column headerText="Subtotal">
                        <h:outputText value="#{det.cantidad * det.precioCompra}">
                            <f:convertNumber pattern="#,##0" />
                        </h:outputText>
                    </p:column>
                    <p:column headerText="Acciones">
                        <p:commandButton icon="pi pi-trash"
                                         action="#{facturaCompraBean.eliminarArticulo(det)}"
                                         update="tablaDetalles panelTotal" />
                    </p:column>
                </p:dataTable>

                <!-- Total -->
                <p:outputPanel id="panelTotal" styleClass="text-end mt-3">
                    <h4>Total: Gs.
                        <h:outputText value="#{facturaCompraBean.total}">
                            <f:convertNumber pattern="#,##0" />
                        </h:outputText>
                    </h4>
                </p:outputPanel>

                <!-- Botones -->
                <div class="mt-3">
                    <p:commandButton value="Guardar" action="#{facturaCompraBean.guardar}"
                                     styleClass="btn btn-success" update="mensajes" />
                    <p:commandButton value="Cancelar" action="#{facturaCompraBean.cancelar}"
                                     styleClass="btn btn-secondary" immediate="true" />
                </div>
            </div>
        </div>

        <!-- Modal de Órdenes de Compra -->
        <p:dialog header="Seleccionar Orden de Compra" widgetVar="dlgOrdenes" modal="true"
                  width="800" height="400">
            <p:dataTable value="#{facturaCompraBean.listaOrdenes}" var="orden">
                <p:column headerText="N° Orden">
                    <h:outputText value="#{orden.idOrdenCompra}" />
                </p:column>
                <p:column headerText="Proveedor">
                    <h:outputText value="#{orden.proveedor.razonSocial}" />
                </p:column>
                <p:column headerText="Fecha">
                    <h:outputText value="#{orden.fecha}">
                        <f:convertDateTime pattern="dd/MM/yyyy" />
                    </h:outputText>
                </p:column>
                <p:column headerText="Acción">
                    <!-- Al hacer clic, se ejecuta el método y se actualiza el formulario -->
                    <p:commandButton value="Seleccionar"
                                     action="#{facturaCompraBean.seleccionarOrden(orden)}"
                                     update="formFactura"
                                     oncomplete="PF('dlgOrdenes').hide()" />
                </p:column>
            </p:dataTable>
        </p:dialog>

    </h:form>
</h:body>
</html>
```

### Configuración Necesaria para JSF

**pom.xml (dependencias Maven):**

```xml
<!-- JSF -->
<dependency>
    <groupId>javax.faces</groupId>
    <artifactId>javax.faces-api</artifactId>
    <version>2.3</version>
    <scope>provided</scope>
</dependency>

<!-- CDI -->
<dependency>
    <groupId>javax.enterprise</groupId>
    <artifactId>cdi-api</artifactId>
    <version>2.0</version>
    <scope>provided</scope>
</dependency>

<!-- PrimeFaces (componentes UI ricos) -->
<dependency>
    <groupId>org.primefaces</groupId>
    <artifactId>primefaces</artifactId>
    <version>12.0.0</version>
</dependency>
```

**web.xml:**

```xml
<!-- Servlet de JSF -->
<servlet>
    <servlet-name>Faces Servlet</servlet-name>
    <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>Faces Servlet</servlet-name>
    <url-pattern>*.xhtml</url-pattern>
</servlet-mapping>

<!-- Configuración de JSF -->
<context-param>
    <param-name>javax.faces.PROJECT_STAGE</param-name>
    <param-value>Development</param-value>
</context-param>
```

**faces-config.xml (en WEB-INF):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<faces-config xmlns="http://xmlns.jcp.org/xml/ns/javaee"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
              http://xmlns.jcp.org/xml/ns/javaee/web-facesconfig_2_3.xsd"
              version="2.3">

    <!-- Navegación declarativa (opcional) -->
    <navigation-rule>
        <from-view-id>/facturaCompra.xhtml</from-view-id>
        <navigation-case>
            <from-outcome>lista</from-outcome>
            <to-view-id>/listaFacturasCompra.xhtml</to-view-id>
            <redirect />
        </navigation-case>
    </navigation-rule>

</faces-config>
```

### Ventajas de JSF sobre Servlets

| Aspecto | Servlet + JSP | JSF + Managed Beans |
|---------|---------------|---------------------|
| Manejo de estado | Manual (tokens, session) | **Automático (scopes)** |
| Thread-safe | Debe implementarse | **Automático** |
| AJAX | Manual con JavaScript | **Integrado (p:ajax, f:ajax)** |
| Validación | Manual en servlet | **Anotaciones (@NotNull, @Size)** |
| Conversión de tipos | Manual (Integer.parseInt) | **Automático** |
| Componentes UI | HTML básico + Bootstrap | **PrimeFaces (calendarios, tablas, diálogos)** |
| Navegación | Manual (redirect, forward) | **Declarativa** |
| Binding de datos | Manual (request.getParameter) | **Automático (EL expressions)** |
| Código repetitivo | Mucho | **Mínimo** |

### Desventajas de JSF

1. **Curva de aprendizaje**: Requiere entender el ciclo de vida de JSF
2. **Migración**: Las vistas JSP deben reescribirse como XHTML
3. **Complejidad inicial**: Más configuración
4. **Diferente paradigma**: Component-based vs Action-based

---

## Comparación de las Tres Arquitecturas

| Aspecto | Variables de Instancia | Session + Token | JSF + Beans |
|---------|----------------------|-----------------|-------------|
| Thread-safe | No | Sí | Sí |
| Múltiples usuarios | Conflictos | Sin problemas | Sin problemas |
| Múltiples pestañas | Conflictos | Cada una independiente | Cada una independiente |
| Código repetido | Mucho | Poco | Mínimo |
| Memoria | Baja | Media | Media |
| Limpieza | No hay | Manual | Automática |
| Complejidad | Baja | Media | Media-Alta |
| Curva aprendizaje | Baja | Baja | Alta |
| AJAX | Manual | Manual | Integrado |
| Componentes UI | Básicos | Básicos | Ricos (PrimeFaces) |
| Migración desde actual | N/A | Fácil | Difícil |

---

## Plan de Migración

### Estado actual

| Módulo | Patrón | Estado |
|--------|--------|--------|
| FacturaCompra | Session + Token + Formulario único + JS | ✅ Implementado (referencia) |
| PedidoCompra | Variables de instancia (legacy, bug concurrencia) | ⏳ Pendiente migrar |
| Presupuesto | Variables de instancia (legacy, bug concurrencia) | ⏳ Pendiente migrar |
| OrdenCompra | Variables de instancia (legacy, bug concurrencia) | ⏳ Pendiente migrar |
| NotaRemision | — | Solo vista JSP, sin servlet aún |
| FacturaVenta y resto | — | Backend listo, sin UI |

### Fase 1: FacturaCompra ✅ COMPLETADO
- Implementado con patrón Session + Token
- Estilo form-floating
- Refactor transaccional (DAOs con `setAutoCommit(false)` + commit/rollback)
- Integración con Libro IVA y Cuenta a Pagar
- Triggers PostgreSQL para stock
- Sirve como referencia para otros módulos

### Fase 2: Migrar módulos existentes (pendiente)
- PedidoCompra
- Presupuesto
- OrdenCompra
- (Y aplicar el patrón a los nuevos: FacturaVenta, NotaCredito/Debito, NotaRemision, etc.)

### Consideraciones
- Los módulos legacy funcionan pero **tienen bug de concurrencia activo** — usuarios concurrentes pueden ver datos mezclados.
- Migrar es prioritario si el sistema se va a usar multiusuario.

> **Decisión (2026-08-17): se mantiene el patrón legacy por ahora.** Se asume uso **mono-usuario**,
> así que el bug de concurrencia no se manifiesta y la migración queda postergada. Lo que sí se
> corrigió es un síntoma derivado: `PresupuestoServlet` no limpiaba todas sus variables de instancia
> al hacer "Nuevo", y el pedido y el artículo del documento anterior reaparecían en cuanto se tocaba
> la condición de compra.
>
> **Cuándo deja de ser opcional:** en cuanto una segunda persona use el sistema al mismo tiempo. Ahí
> los datos se mezclan entre usuarios (usuario A carga un pedido, usuario B abre otro, A ve el de B)
> y no hay forma de mitigarlo sin migrar. Conviene tenerlo presente antes de poner el sistema en
> producción con varios operadores.
>
> **Trampa a recordar mientras siga así:** cada variable de instancia nueva que se agregue a estos
> servlets hay que acordarse de limpiarla en la acción "Nuevo", o reaparece en la siguiente acción
> que la reenvíe a la vista.
- Priorizar módulos más usados y los que comparten flujo con FacturaCompra.

---

## Funcionalidades Específicas

### Facturas de Gasto y Fondo Fijo

Las facturas de compra pueden ser de tres tipos:
- **Mercadería**: Requiere selección de artículos codificados
- **Gasto**: Permite descripción libre sin artículo, con selección manual de impuesto
- **Fondo Fijo**: Similar a gasto, para rendiciones de fondo fijo

Para gastos y fondo fijo:
1. El botón "Buscar Artículo" está oculto
2. Se muestra un select para elegir el tipo de impuesto (10%, 5%, Exento)
3. El detalle se guarda con `tipoImpuesto` en lugar de usar el del artículo
4. El cálculo de IVA usa `detalle.tipoImpuesto.descripcion`

**En el JSP (facturaCompra.jsp):**
```jsp
<!-- Mostrar select de impuesto solo para gasto/fondo fijo -->
<c:if test="${fn:contains(facturaCompra.tipoFactura, 'gasto') or fn:contains(facturaCompra.tipoFactura, 'fondoFijo')}">
    <select name="idTipoImpuesto" class="form-control">
        <option value="">Seleccionar Impuesto</option>
        <c:forEach var="imp" items="${listaTipoImpuesto}">
            <option value="${imp.idTipoImpuesto}">${imp.descripcion}</option>
        </c:forEach>
    </select>
</c:if>

<!-- Cálculo de IVA (usa tipoImpuesto del detalle si no hay artículo) -->
<c:set var="descImpuesto" value="${not empty detalle.articulo ? detalle.articulo.tipoImpuesto.descripcion : detalle.tipoImpuesto.descripcion}" />
```

**En el modelo (FacturaCompraDetalle.java):**
```java
private TipoImpuesto tipoImpuesto;  // Para gastos sin artículo

public FacturaCompraDetalle(FacturaCompra facturaCompra, Articulo articulo, Long cantidad,
        Long precioCompra, String descripcion, TipoImpuesto tipoImpuesto) {
    // Constructor con tipoImpuesto
}
```

**En el DAO (FacturaCompraDetalleDAO.java):**
```java
// INSERT incluye id_impuesto
String sql = "INSERT INTO factura_compra_detalle (..., id_impuesto) VALUES (..., ?)";

// SELECT incluye id_impuesto
Long idImpuesto = rs.getLong("id_impuesto");
TipoImpuesto tipoImpuesto = idImpuesto != 0 ? tipoImpuestoDAO.getTipoImpuesto(idImpuesto) : null;
```

---

## Sistema de Permisos y Autorización

### Modelo de Datos

El sistema utiliza 3 tablas pre-existentes en la BD:

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│  grupo   │     │ permiso  │     │  modulo  │
├──────────┤     ├──────────┤     ├──────────┤
│ id_grupo │◄────┤ id_grupo │     │ id_modulo│
│ descripcion│   │ id_modulo├────►│ descripcion│
│          │     │ permi_leer│    └──────────┘
│          │     │ permi_insertar│
│          │     │ permi_borrar│
│          │     │ permi_editar│
└──────────┘     └──────────┘
```

- Cada **usuario** pertenece a un **grupo** (ej: Administradores, Tesorería)
- Cada **grupo** tiene **permisos** CRUD por cada **módulo** (ej: compra, venta, tesorería)
- Los módulos ABM (TipoArticulo, Usuario) no tienen restricción

### Arquitectura: AuthorizationFilter

El sistema implementa un patrón de **Filter + Request Attributes** para controlar permisos:

```
┌──────────────────────────────────────────────────────────────────┐
│                          REQUEST                                  │
│  GET /FacturaCompraServlet?menu=FacturaCompra&accion=Nuevo       │
└──────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                    AuthFilter (autenticación)                      │
│  @WebFilter("/*")                                                 │
│  - Verifica que exista usuario en session                         │
│  - Si no hay sesión → redirect a login                            │
└──────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                AuthorizationFilter (autorización)                  │
│  @WebFilter({"/FacturaCompraServlet", "/PedidoCompraServlet",    │
│              "/PresupuestoServlet", "/OrdenCompraServlet"})       │
│                                                                   │
│  1. Extrae nombre del servlet de la URI                           │
│  2. Busca el módulo en URL_MODULO map (ej: "compra")              │
│  3. Lee Map<String, Permiso> de la session                        │
│  4. Si leer=false → redirect a MenuPrincipal con error            │
│  5. Si leer=true → setea request attributes:                      │
│     - puedeInsertar = permiso.getInsertar()                       │
│     - puedeEditar = permiso.getEditar()                           │
│     - puedeBorrar = permiso.getBorrar()                           │
│  6. chain.doFilter()                                              │
└──────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                         SERVLET                                    │
│  Validación server-side antes del switch:                         │
│                                                                   │
│  Boolean puedeInsertar = (Boolean) request.getAttribute(...)      │
│  switch (accion) {                                                │
│      case "Nuevo": case "Guardar":                                │
│          if (!puedeInsertar) → rechazar con mensaje de error      │
│      case "EditarArticulo":                                       │
│          if (!puedeEditar) → rechazar con mensaje de error        │
│      case "Anular": case "EliminarArticulo":                      │
│          if (!puedeBorrar) → rechazar con mensaje de error        │
│  }                                                                │
│  // Si pasa validación → ejecutar acción normalmente              │
└──────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────┐
│                           JSP                                      │
│  Condicionar botones con JSTL:                                    │
│                                                                   │
│  <c:choose>                                                       │
│    <c:when test="${puedeInsertar}">                                │
│      <button class="btn btn-primary">Nuevo</button>               │
│    </c:when>                                                      │
│    <c:otherwise>                                                  │
│      <button class="btn btn-primary" disabled                     │
│              title="No tiene permisos">Nuevo</button>             │
│    </c:otherwise>                                                 │
│  </c:choose>                                                      │
│                                                                   │
│  Botones de búsqueda/lectura → siempre visibles                  │
└──────────────────────────────────────────────────────────────────┘
```

### Carga de Permisos en Login

En `LoginServlet.java`, después de autenticar al usuario:

```java
// Cargar permisos del grupo del usuario en session
PermisoService permisoService = new PermisoService();
List<Permiso> permisos = permisoService.listarPermisosByGrupo(usuario.getGrupo().getIdGrupo());
Map<String, Permiso> mapaPermisos = new HashMap<>();
for (Permiso p : permisos) {
    mapaPermisos.put(p.getModulo().getDescripcion(), p);
}
session.setAttribute("permisos", mapaPermisos);
// Resultado: {"compra": Permiso(...), "venta": Permiso(...), "tesoreria": Permiso(...)}
```

### Mapeo URL → Módulo en AuthorizationFilter

```java
private static final Map<String, String> URL_MODULO = new HashMap<>();
static {
    URL_MODULO.put("FacturaCompraServlet", "compra");
    URL_MODULO.put("PedidoCompraServlet", "compra");
    URL_MODULO.put("PresupuestoServlet", "compra");
    URL_MODULO.put("OrdenCompraServlet", "compra");
    // Futuros: FacturaVentaServlet → "venta", CajaServlet → "tesoreria", etc.
}
```

### Clasificación de Acciones por Permiso

| Permiso | Acciones |
|---------|----------|
| `puedeInsertar` | Nuevo, AgregarArticulo, Guardar, PersistirPedido, PersistirPresupuesto, PersistirOrdenCompra |
| `puedeEditar` | EditarArticulo, ActualizarArticulo, EditarPrecioArticuloList, ModificarArticuloDetalle, Aprobar |
| `puedeBorrar` | EliminarArticulo, EliminarArticuloList, Anular |

### Patrón de Condicionamiento en JSP

**Botones con `<c:choose>` (reemplaza completamente):**
```jsp
<c:choose>
    <c:when test="${puedeInsertar}">
        <a href="...?accion=Nuevo" class="btn btn-primary">Nuevo</a>
    </c:when>
    <c:otherwise>
        <button class="btn btn-primary" disabled title="No tiene permisos para insertar">Nuevo</button>
    </c:otherwise>
</c:choose>
```

**Botones con atributo disabled inline (mantiene estructura):**
```jsp
<button type="submit" class="btn btn-success"
    <c:if test="${not puedeInsertar}">disabled title="No tiene permisos"</c:if>>
    Guardar
</button>
```

**Condición compuesta (botón habilitado solo si tiene permiso Y el estado lo permite):**
```jsp
<button type="submit" class="btn btn-danger"
    ${facturaCompra.estado ne 'Procesado' or not puedeBorrar ? 'disabled' : ''}>
    Anular
</button>
```

### Extensibilidad

Para agregar un nuevo módulo al sistema de permisos:

1. **BD**: Insertar registro en tabla `permiso` con el `id_grupo` e `id_modulo` correspondiente
2. **AuthorizationFilter**: Agregar entrada en `URL_MODULO` (ej: `URL_MODULO.put("CajaServlet", "tesoreria")`)
3. **Servlet**: Agregar validación de permisos antes del switch de acciones
4. **JSP**: Condicionar botones de escritura con los flags `puedeInsertar`, `puedeEditar`, `puedeBorrar`

No requiere cambios en LoginServlet ni en PermisoService (ya cargan todos los módulos).

---

## Notas Adicionales

### Limpieza de Sesión
Se recomienda implementar un `HttpSessionListener` para limpiar estados huérfanos:

```java
@WebListener
public class SessionCleanupListener implements HttpSessionListener {
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // Los atributos se limpian automáticamente al destruir la sesión
        // Pero se puede agregar logging aquí si es necesario
    }
}
```

### Timeout de Sesión
Configurar en `web.xml`:
```xml
<session-config>
    <session-timeout>30</session-timeout> <!-- 30 minutos -->
</session-config>
```

---

## Capa Transaccional (DAOs)

A partir del refactor de marzo 2026, los DAOs de los módulos principales de compras manejan transacciones explícitas para garantizar atomicidad entre cabecera, detalles, Libro IVA y Cuenta a Pagar.

**Patrón:**

```java
public Long insertarFacturaCompra(FacturaCompra factura, List<FacturaCompraDetalle> detalles) throws SQLException {
    Connection conn = null;
    try {
        conn = Conexion.obtenerConexion();
        conn.setAutoCommit(false);

        Long idFactura = insertarCabecera(conn, factura);
        insertarDetalles(conn, idFactura, detalles);     // dispara triggers de stock
        insertarLibroIva(conn, idFactura, factura);
        insertarCuentaPagar(conn, idFactura, factura);

        conn.commit();
        return idFactura;
    } catch (SQLException e) {
        if (conn != null) conn.rollback();
        throw e;
    } finally {
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
```

**DAOs que ya siguen este patrón:**
- `FacturaCompraDAO`
- `PedidoCompraDAO`
- `PresupuestoDAO`
- `OrdenCompraDAO`

**Reglas implícitas en el modelo de negocio:**
- Una factura anulada **no se puede des-anular**.
- Los detalles de una factura guardada son **inmutables** (no se editan cantidades, no se eliminan ni agregan líneas). Esto justifica que los triggers de stock solo cubran INSERT (detalle) y UPDATE de estado (cabecera → 'Anulado').

---

*Documento creado: Enero 2026*
*Última actualización: Mayo 2026*

---

## Historial de Cambios

| Fecha | Cambio |
|-------|--------|
| Enero 2026 | Documento inicial con patrón Session + Token |
| Enero 2026 | Actualizado FacturaCompraState con campos adicionales (listaTipoImpuesto, listaArticulos, etc.) |
| Enero 2026 | Documentada funcionalidad de facturas de gasto/fondo fijo con selección de impuesto |
| Enero 2026 | Actualizado patrón a "Switch-Case con Métodos Delegados" |
| Febrero 2026 | Documentado Sistema de Permisos y Autorización (AuthorizationFilter, flujo completo, patrones JSP) |
| Marzo 2026 | Agregada sección "Capa Transaccional" describiendo el refactor de DAOs con commit/rollback explícito |
| Mayo 2026 | Actualizado Plan de Migración con estado real; documentadas reglas de inmutabilidad de detalles (justifica triggers de stock) |
