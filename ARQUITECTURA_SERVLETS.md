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
 */
private static class FacturaCompraState {
    FacturaCompra facturaCompra = new FacturaCompra();
    List<FacturaCompraDetalle> listaDetalle = new ArrayList<>();
    Proveedor proveedorSeleccionado;
    Sucursal sucursalSeleccionada;
    OrdenCompra ordenCompraSeleccionada;
    boolean esNuevo = false;

    // Datos para modales (se pueden cargar bajo demanda)
    List<OrdenCompra> listaOrdenesCompra;
    List<Proveedor> listaProveedores;
    List<Sucursal> listaSucursales;
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
    request.setAttribute("esNuevo", estado.esNuevo);

    // Listas para modales
    request.setAttribute("listaOrdenesCompra", estado.listaOrdenesCompra);
    request.setAttribute("listaProveedores", estado.listaProveedores);
    request.setAttribute("listaSucursales", estado.listaSucursales);
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
    private final OrdenCompraService ordenCompraService = new OrdenCompraService();
    private final ProveedorService proveedorService = new ProveedorService();
    private final SucursalService sucursalService = new SucursalService();

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

        try {
            switch (accion) {

                // ==================== INICIAR NUEVO DOCUMENTO ====================
                case "Nuevo":
                    String nuevoToken = generarToken();
                    FacturaCompraState nuevoEstado = new FacturaCompraState();
                    nuevoEstado.esNuevo = true;
                    nuevoEstado.facturaCompra.setUsuario(usuario);
                    nuevoEstado.facturaCompra.setFechaCarga(new Date());
                    nuevoEstado.facturaCompra.setEstado("Pendiente");

                    // Cargar datos para modales
                    nuevoEstado.listaOrdenesCompra = ordenCompraService.listarOrdenesCompraConDetalles();
                    nuevoEstado.listaProveedores = proveedorService.listarProveedores();
                    nuevoEstado.listaSucursales = sucursalService.listarSucursles();

                    guardarEstado(session, nuevoToken, nuevoEstado);
                    cargarDatosParaVista(request, nuevoEstado, nuevoToken);

                    request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                    break;

                // ==================== CARGAR DOCUMENTO EXISTENTE ====================
                case "Cargar":
                    Long idFactura = Long.parseLong(request.getParameter("idFactura"));
                    String tokenCargar = generarToken();
                    FacturaCompraState estadoCargar = new FacturaCompraState();

                    estadoCargar.facturaCompra = facturaCompraService.getFacturaCompra(idFactura);
                    estadoCargar.listaDetalle = facturaCompraService.listarDetallesPorFactura(idFactura);
                    estadoCargar.proveedorSeleccionado = estadoCargar.facturaCompra.getProveedor();
                    estadoCargar.sucursalSeleccionada = estadoCargar.facturaCompra.getSucursal();
                    estadoCargar.ordenCompraSeleccionada = estadoCargar.facturaCompra.getOrdenCompra();
                    estadoCargar.esNuevo = false;
                    estadoCargar.listaSucursales = sucursalService.listarSucursles();

                    guardarEstado(session, tokenCargar, estadoCargar);
                    cargarDatosParaVista(request, estadoCargar, tokenCargar);

                    request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                    break;

                // ==================== SELECCIONAR ORDEN DE COMPRA ====================
                case "CargarOrdenCompra":
                    FacturaCompraState estadoOrden = obtenerEstado(session, token);
                    if (estadoOrden == null) {
                        mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-danger");
                        response.sendRedirect("FacturaCompraServlet?menu=FacturaCompra&accion=ListarModal");
                        return;
                    }

                    Long idOrden = Long.parseLong(request.getParameter("idOrden"));
                    OrdenCompra ordenCompra = ordenCompraService.getOrdenCompra(idOrden);

                    if (ordenCompra != null) {
                        estadoOrden.ordenCompraSeleccionada = ordenCompra;
                        estadoOrden.facturaCompra.setOrdenCompra(ordenCompra);
                        estadoOrden.facturaCompra.setProveedor(ordenCompra.getProveedor());
                        estadoOrden.facturaCompra.setSucursal(ordenCompra.getSucursal());
                        estadoOrden.facturaCompra.setCondicion(ordenCompra.getCondicionCompra());
                        estadoOrden.proveedorSeleccionado = ordenCompra.getProveedor();
                        estadoOrden.sucursalSeleccionada = ordenCompra.getSucursal();

                        mostrarMensaje(request, "Orden de compra cargada", "alert-success");
                    }

                    guardarEstado(session, token, estadoOrden);
                    cargarDatosParaVista(request, estadoOrden, token);

                    request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                    break;

                // ==================== CAMBIAR CONDICIÓN DE COMPRA ====================
                case "CambiarCondicion":
                    FacturaCompraState estadoCond = obtenerEstado(session, token);
                    if (estadoCond == null) {
                        response.sendRedirect("FacturaCompraServlet?menu=FacturaCompra&accion=ListarModal");
                        return;
                    }

                    String condicion = request.getParameter("condicion");
                    estadoCond.facturaCompra.setCondicion(condicion);

                    if ("Contado".equals(condicion)) {
                        estadoCond.facturaCompra.setPlazo(0);
                    }

                    cargarDatosParaVista(request, estadoCond, token);
                    request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                    break;

                // ==================== GUARDAR FACTURA ====================
                case "Guardar":
                    FacturaCompraState estadoGuardar = obtenerEstado(session, token);
                    if (estadoGuardar == null) {
                        mostrarMensaje(request, "Sesión expirada", "alert-danger");
                        response.sendRedirect("FacturaCompraServlet?menu=FacturaCompra&accion=ListarModal");
                        return;
                    }

                    // Validaciones
                    if (estadoGuardar.listaDetalle.isEmpty()) {
                        mostrarMensaje(request, "Debe agregar al menos un artículo", "alert-warning");
                        cargarDatosParaVista(request, estadoGuardar, token);
                        request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                        return;
                    }

                    // Guardar en BD
                    Long idInsertado = facturaCompraService.insertarFacturaCompra(
                        estadoGuardar.facturaCompra,
                        estadoGuardar.listaDetalle
                    );

                    if (idInsertado != null) {
                        // LIMPIAR SESIÓN después de guardar exitosamente
                        limpiarEstado(session, token);

                        mostrarMensaje(request, "Factura guardada correctamente. ID: " + idInsertado, "alert-success");
                        response.sendRedirect("FacturaCompraServlet?menu=FacturaCompra&accion=ListarModal");
                    } else {
                        mostrarMensaje(request, "Error al guardar la factura", "alert-danger");
                        cargarDatosParaVista(request, estadoGuardar, token);
                        request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                    }
                    break;

                // ==================== CANCELAR / LIMPIAR ====================
                case "Cancelar":
                    limpiarEstado(session, token);
                    response.sendRedirect("FacturaCompraServlet?menu=FacturaCompra&accion=ListarModal");
                    break;

                // ==================== LISTAR (PANTALLA INICIAL) ====================
                case "ListarModal":
                default:
                    List<FacturaCompra> listaFacturas = facturaCompraService.listarFacturasCompra();
                    List<OrdenCompra> listaOrdenes = ordenCompraService.listarOrdenesCompraConDetalles();

                    request.setAttribute("listaFacturasCompra", listaFacturas);
                    request.setAttribute("listaOrdenesCompra", listaOrdenes);

                    request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
                    break;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en FacturaCompraServlet", e);
            mostrarMensaje(request, "Error: " + e.getMessage(), "alert-danger");
            request.getRequestDispatcher("facturaCompra.jsp").forward(request, response);
        }
    }

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

### Fase 1: FacturaCompra (nuevo)
- Implementar con el nuevo patrón Session + Token
- Usar estilo form-floating
- Servir como referencia para otros módulos

### Fase 2: Migrar módulos existentes (opcional, futuro)
- PedidoCompra
- Presupuesto
- OrdenCompra
- FacturaVenta
- NotaCredito/Debito

### Consideraciones
- Los módulos existentes funcionan (con el bug de concurrencia)
- Migrar solo si hay tiempo o si se reportan problemas
- Priorizar módulos más usados

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

*Documento creado: Enero 2026*
*Última actualización: Enero 2026*
