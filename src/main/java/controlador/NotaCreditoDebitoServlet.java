/*
 * Servlet único para Nota de Crédito y Débito de Compra.
 * Enruta a NC o ND según el parámetro tipoNota ("credito" | "debito").
 * Patrón Session + Token (thread-safe, multi-pestaña), calcado de FacturaCompraServlet.
 * Ver NOTA_CREDITO_DEBITO_PLAN.md §7.
 */
package controlador;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

@WebServlet(name = "NotaCreditoDebitoServlet", urlPatterns = {"/NotaCreditoDebitoServlet"})
public class NotaCreditoDebitoServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotaCreditoDebitoServlet.class.getName());

    private static final String SESSION_PREFIX = "notaCreditoDebito_";
    private static final String JSP_NOTA = "notaCreditoDebito.jsp";

    // Services (stateless)
    private final NotaCreditoCompraService notaCreditoService = new NotaCreditoCompraService();
    private final NotaDebitoCompraService notaDebitoService = new NotaDebitoCompraService();
    private final FacturaCompraService facturaCompraService = new FacturaCompraService();
    private final FacturaCompraDetalleService facturaCompraDetalleService = new FacturaCompraDetalleService();
    private final ArticuloService articuloService = new ArticuloService();
    private final TipoImpuestoService tipoImpuestoService = new TipoImpuestoService();
    private final DepositoService depositoService = new DepositoService();

    // ==================== ESTADO DEL DOCUMENTO ====================

    /**
     * Estado de trabajo de la nota. Usa NotaCreditoCompra / NotaCreditoCompraDetalle como
     * tipos de trabajo (superconjunto): para una ND se mapean al guardar/anular.
     */
    private static class NotaState implements Serializable {
        private static final long serialVersionUID = 1L;

        String tipoNota = "credito";                 // "credito" | "debito"
        NotaCreditoCompra nota = new NotaCreditoCompra();
        List<NotaCreditoCompraDetalle> listaDetalle = new ArrayList<>();

        FacturaCompra facturaReferenciada;
        Proveedor proveedorSeleccionado;
        Sucursal sucursalSeleccionada;               // heredada de la factura (solo lectura)
        String condicionHeredada;                    // condición de la factura (solo lectura)

        NotaCreditoCompraDetalle detalleSeleccionado;
        Integer indexSeleccionado;
        boolean esNuevo = false;
        Long idNotaExistente;                        // seteado al cargar una nota para anular

        // Listas para combos/modales
        List<FacturaCompra> listaFacturas;
        List<TipoImpuesto> listaTipoImpuesto;
        List<Deposito> listaDepositos;
        List<NotaCreditoCompra> listaNotasCredito;
        List<NotaDebitoCompra> listaNotasDebito;
    }

    /** DTO de totales de IVA (no persistido). */
    private static class TotalesIva {
        long total;
        long iva10;
        long iva5;
        long gravada10;
        long gravada5;
        long exenta;
    }

    // ==================== HELPERS DE SESIÓN ====================

    private String generarToken() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private NotaState obtenerEstado(HttpSession session, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        return (NotaState) session.getAttribute(SESSION_PREFIX + token);
    }

    private void guardarEstado(HttpSession session, String token, NotaState estado) {
        session.setAttribute(SESSION_PREFIX + token, estado);
    }

    private void limpiarEstado(HttpSession session, String token) {
        if (token != null) {
            session.removeAttribute(SESSION_PREFIX + token);
        }
    }

    private NotaState obtenerEstadoORedireccionar(HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String token) throws IOException {
        NotaState estado = obtenerEstado(session, token);
        if (estado == null) {
            mostrarMensaje(request, "Sesión expirada, inicie de nuevo", "alert-warning");
            response.sendRedirect("NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=ListarModal");
        }
        return estado;
    }

    private void cargarDatosParaVista(HttpServletRequest request, NotaState estado, String token) {
        request.setAttribute("token", token);
        request.setAttribute("tipoNota", estado.tipoNota);
        request.setAttribute("nota", estado.nota);
        request.setAttribute("listaDetalle", estado.listaDetalle);
        request.setAttribute("facturaReferenciada", estado.facturaReferenciada);
        request.setAttribute("proveedorSeleccionado", estado.proveedorSeleccionado);
        request.setAttribute("sucursalHeredada", estado.sucursalSeleccionada);
        request.setAttribute("condicionHeredada", estado.condicionHeredada);
        request.setAttribute("detalleSeleccionado", estado.detalleSeleccionado);
        request.setAttribute("indexSeleccionado", estado.indexSeleccionado);
        request.setAttribute("esNuevo", estado.esNuevo);
        request.setAttribute("idNotaExistente", estado.idNotaExistente);
        request.setAttribute("listaFacturas", estado.listaFacturas);
        request.setAttribute("listaTipoImpuesto", estado.listaTipoImpuesto);
        request.setAttribute("listaDepositos", estado.listaDepositos);
        request.setAttribute("listaNotasCredito", estado.listaNotasCredito);
        request.setAttribute("listaNotasDebito", estado.listaNotasDebito);
        calcularImpuestos(request, estado.listaDetalle);
    }

    private void mostrarMensaje(HttpServletRequest request, String mensaje, String tipoAlert) {
        request.setAttribute("Message", mensaje);
        request.setAttribute("tipoAlert", tipoAlert);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        request.getRequestDispatcher(vista).forward(request, response);
    }

    // ==================== PROCESO PRINCIPAL ====================

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String menu = request.getParameter("menu");
        String accion = request.getParameter("accion");
        String token = request.getParameter("token");

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        if (!"NotaCreditoDebito".equals(menu)) {
            response.sendRedirect("error.jsp");
            return;
        }

        if (accion == null) {
            accion = "ListarModal";
        }

        // Validación de permisos server-side (attributes seteados por AuthorizationFilter)
        Boolean puedeInsertar = (Boolean) request.getAttribute("puedeInsertar");
        Boolean puedeBorrar = (Boolean) request.getAttribute("puedeBorrar");
        switch (accion) {
            case "Nuevo":
            case "CargarFactura":
            case "AgregarLinea":
            case "EditarArticulo":
            case "ActualizarArticulo":
            case "EliminarArticulo":
            case "Guardar":
                if (puedeInsertar == null || !puedeInsertar) {
                    mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                    try {
                        accionListarModal(request, response);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error listando notas", e);
                    }
                    return;
                }
                break;
            case "Anular":
                if (puedeBorrar == null || !puedeBorrar) {
                    mostrarMensaje(request, "No tiene permisos para realizar esta acción", "alert-danger");
                    try {
                        accionListarModal(request, response);
                    } catch (SQLException e) {
                        LOGGER.log(Level.SEVERE, "Error listando notas", e);
                    }
                    return;
                }
                break;
            default:
                break;
        }

        try {
            switch (accion) {
                case "Nuevo":
                    accionNuevo(request, response, session, usuario);
                    break;
                case "CambiarTipoNota":
                    accionCambiarTipoNota(request, response, session, token);
                    break;
                case "CargarFactura":
                    accionCargarFactura(request, response, session, token);
                    break;
                case "CargarNota":
                    accionCargarNota(request, response, session);
                    break;
                case "AgregarLinea":
                    accionAgregarLinea(request, response, session, token);
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
                case "ListarModal":
                default:
                    accionListarModal(request, response);
                    break;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en NotaCreditoDebitoServlet", e);
            mostrarMensaje(request, "Error de base de datos: " + e.getMessage(), "alert-danger");
            forward(request, response, JSP_NOTA);
        }
    }

    // ==================== ACCIONES ====================

    private void accionListarModal(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        request.setAttribute("token", "");
        request.setAttribute("listaFacturas", facturaCompraService.listarFacturasCompra());
        request.setAttribute("listaNotasCredito", notaCreditoService.listarNotasCreditoCompra());
        request.setAttribute("listaNotasDebito", notaDebitoService.listarNotasDebitoCompra());
        forward(request, response, JSP_NOTA);
    }

    private void accionNuevo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Usuario usuario) throws ServletException, IOException, SQLException {

        String tipo = request.getParameter("tipoNota");
        if (tipo == null || tipo.isEmpty()) {
            tipo = "credito";
        }

        String nuevoToken = generarToken();
        NotaState estado = new NotaState();
        estado.tipoNota = tipo;
        estado.esNuevo = true;
        estado.nota.setUsuario(usuario);
        estado.nota.setFechaCarga(new Date());
        estado.nota.setEstado("Pendiente");

        estado.listaFacturas = facturaCompraService.listarFacturasCompra();
        estado.listaTipoImpuesto = tipoImpuestoService.listarTipoImpuesto();
        estado.listaDepositos = depositoService.listarDepostio();
        estado.listaNotasCredito = notaCreditoService.listarNotasCreditoCompra();
        estado.listaNotasDebito = notaDebitoService.listarNotasDebitoCompra();

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);
        forward(request, response, JSP_NOTA);
    }

    private void accionCambiarTipoNota(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);
        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_NOTA);
    }

    /**
     * Carga la factura referenciada: hereda proveedor, sucursal y condición (solo lectura)
     * y trae sus líneas al detalle (con su depósito, para la devolución de stock).
     */
    private void accionCargarFactura(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);

        String idFactStr = request.getParameter("idFactura");
        if (idFactStr != null && !idFactStr.isEmpty()) {
            FacturaCompra fact = facturaCompraService.getFacturaCompra(Long.parseLong(idFactStr));
            if (fact != null) {
                if ("Anulado".equals(fact.getEstado())) {
                    mostrarMensaje(request, "No se puede emitir una nota sobre una factura anulada", "alert-warning");
                } else {
                    estado.facturaReferenciada = fact;
                    estado.proveedorSeleccionado = fact.getProveedor();
                    estado.sucursalSeleccionada = fact.getSucursal();
                    estado.condicionHeredada = fact.getCondicion();

                    // Heredar las líneas de la factura (el usuario luego ajusta cantidades / quita)
                    estado.listaDetalle.clear();
                    List<FacturaCompraDetalle> fdet =
                        facturaCompraDetalleService.listarDetallesPorFactura(fact.getIdFacturaCompra());
                    if (fdet != null) {
                        for (FacturaCompraDetalle fd : fdet) {
                            NotaCreditoCompraDetalle det = new NotaCreditoCompraDetalle();
                            det.setArticulo(fd.getArticulo());
                            det.setCantidad(fd.getCantidad());
                            det.setMonto(fd.getPrecioCompra());
                            det.setDescripcion(fd.getDescripcion());
                            det.setTipoImpuesto(fd.getTipoImpuesto());
                            det.setDeposito(fd.getDeposito());
                            estado.listaDetalle.add(det);
                        }
                    }
                    mostrarMensaje(request,
                        "Factura cargada. Ajuste las cantidades a lo devuelto o agregue líneas financieras.",
                        "alert-info");
                }
            }
        }

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_NOTA);
    }

    /** Carga una nota existente (por id + tipo) para verla/anularla. */
    private void accionCargarNota(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws ServletException, IOException, SQLException {
        String idStr = request.getParameter("idNota");
        String tipo = request.getParameter("tipoNotaCargar");
        if (idStr == null || idStr.isEmpty() || tipo == null) {
            accionListarModal(request, response);
            return;
        }
        Long id = Long.parseLong(idStr);

        String nuevoToken = generarToken();
        NotaState estado = new NotaState();
        estado.tipoNota = tipo;
        estado.esNuevo = false;
        estado.idNotaExistente = id;
        estado.listaFacturas = facturaCompraService.listarFacturasCompra();
        estado.listaTipoImpuesto = tipoImpuestoService.listarTipoImpuesto();
        estado.listaDepositos = depositoService.listarDepostio();
        estado.listaNotasCredito = notaCreditoService.listarNotasCreditoCompra();
        estado.listaNotasDebito = notaDebitoService.listarNotasDebitoCompra();

        if ("debito".equals(tipo)) {
            NotaDebitoCompra nd = notaDebitoService.getNotaDebitoCompra(id);
            if (nd == null) { accionListarModal(request, response); return; }
            estado.nota = mapFromDebito(nd);
            estado.facturaReferenciada = nd.getFacturaCompra();
            estado.proveedorSeleccionado = nd.getProveedor();
            if (nd.getFacturaCompra() != null) {
                estado.sucursalSeleccionada = nd.getFacturaCompra().getSucursal();
                estado.condicionHeredada = nd.getFacturaCompra().getCondicion();
            }
            List<NotaDebitoCompraDetalle> dets = notaDebitoService.listarDetallesPorNota(id);
            if (dets != null) {
                for (NotaDebitoCompraDetalle d : dets) {
                    estado.listaDetalle.add(mapDebitoDetalleToWorking(d));
                }
            }
        } else {
            NotaCreditoCompra nc = notaCreditoService.getNotaCreditoCompra(id);
            if (nc == null) { accionListarModal(request, response); return; }
            estado.nota = nc;
            estado.facturaReferenciada = nc.getFacturaCompra();
            estado.proveedorSeleccionado = nc.getProveedor();
            if (nc.getFacturaCompra() != null) {
                estado.sucursalSeleccionada = nc.getFacturaCompra().getSucursal();
                estado.condicionHeredada = nc.getFacturaCompra().getCondicion();
            }
            List<NotaCreditoCompraDetalle> dets = notaCreditoService.listarDetallesPorNota(id);
            if (dets != null) {
                estado.listaDetalle.addAll(dets);
            }
        }

        guardarEstado(session, nuevoToken, estado);
        cargarDatosParaVista(request, estado, nuevoToken);
        forward(request, response, JSP_NOTA);
    }

    private void accionAgregarLinea(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);

        NotaCreditoCompraDetalle det = leerLineaEditor(request);
        if (det != null) {
            estado.listaDetalle.add(det);
            mostrarMensaje(request, "Línea agregada", "alert-success");
        } else {
            mostrarMensaje(request, "Complete cantidad y monto de la línea", "alert-warning");
        }
        estado.detalleSeleccionado = null;
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_NOTA);
    }

    private void accionEditarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);

        String idxStr = request.getParameter("index");
        if (idxStr != null && !idxStr.isEmpty()) {
            int idx = Integer.parseInt(idxStr);
            if (idx >= 0 && idx < estado.listaDetalle.size()) {
                estado.indexSeleccionado = idx;
                estado.detalleSeleccionado = estado.listaDetalle.get(idx);
            }
        }

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_NOTA);
    }

    private void accionActualizarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);

        if (estado.indexSeleccionado != null
                && estado.indexSeleccionado >= 0
                && estado.indexSeleccionado < estado.listaDetalle.size()) {
            NotaCreditoCompraDetalle nueva = leerLineaEditor(request);
            if (nueva != null) {
                estado.listaDetalle.set(estado.indexSeleccionado, nueva);
                mostrarMensaje(request, "Línea actualizada", "alert-success");
            }
        }
        estado.detalleSeleccionado = null;
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_NOTA);
    }

    private void accionEliminarArticulo(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);

        String idxStr = request.getParameter("index");
        if (idxStr != null && !idxStr.isEmpty()) {
            int idx = Integer.parseInt(idxStr);
            if (idx >= 0 && idx < estado.listaDetalle.size()) {
                estado.listaDetalle.remove(idx);
            }
        }
        estado.detalleSeleccionado = null;
        estado.indexSeleccionado = null;

        guardarEstado(session, token, estado);
        cargarDatosParaVista(request, estado, token);
        forward(request, response, JSP_NOTA);
    }

    private void accionGuardar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;
        leerDatosCabecera(request, estado);

        // Validaciones
        if (estado.facturaReferenciada == null) {
            mostrarMensaje(request, "Debe seleccionar la factura referenciada", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }
        if ("Anulado".equals(estado.facturaReferenciada.getEstado())) {
            mostrarMensaje(request, "La factura referenciada está anulada", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }
        if (estado.listaDetalle.isEmpty()) {
            mostrarMensaje(request, "Debe agregar al menos una línea", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }
        if (estado.nota.getNumero() == null || estado.nota.getNumero().isEmpty()) {
            mostrarMensaje(request, "Debe ingresar el número de comprobante", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }
        if (estado.nota.getFechaEmision() == null) {
            mostrarMensaje(request, "Debe ingresar la fecha de emisión", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }
        if (estado.nota.getTimbrado() == null || estado.nota.getTimbrado() <= 0) {
            mostrarMensaje(request, "Debe ingresar el timbrado", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }

        // Validar fecha de vencimiento del timbrado estrictamente mayor a hoy (igual que Factura de Compra)
        if (estado.nota.getFechaVenciTimbrado() != null) {
            java.util.Calendar calVenc = java.util.Calendar.getInstance();
            calVenc.setTime(estado.nota.getFechaVenciTimbrado());
            calVenc.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calVenc.set(java.util.Calendar.MINUTE, 0);
            calVenc.set(java.util.Calendar.SECOND, 0);
            calVenc.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar calActual = java.util.Calendar.getInstance();
            calActual.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calActual.set(java.util.Calendar.MINUTE, 0);
            calActual.set(java.util.Calendar.SECOND, 0);
            calActual.set(java.util.Calendar.MILLISECOND, 0);

            if (!calVenc.after(calActual)) {
                mostrarMensaje(request, "La fecha de vencimiento del timbrado debe ser mayor a la fecha actual", "alert-warning");
                cargarDatosParaVista(request, estado, token);
                forward(request, response, JSP_NOTA);
                return;
            }
        }

        // Referencias de cabecera
        estado.nota.setProveedor(estado.proveedorSeleccionado);
        estado.nota.setFacturaCompra(estado.facturaReferenciada);

        // Calcular IVA y armar libro IVA con montos POSITIVOS (el Service aplica el signo)
        TotalesIva totales = calcularTotalesIva(estado.listaDetalle);
        LibroIvaCompra libroIva = construirLibroIvaNota(totales, estado.nota.getFechaEmision());

        if ("debito".equals(estado.tipoNota)) {
            NotaDebitoCompra nd = mapAsDebito(estado.nota);
            List<NotaDebitoCompraDetalle> dets = mapDetallesDebito(estado.listaDetalle);
            Long id = notaDebitoService.guardarNotaDebitoCompleta(nd, dets, libroIva);
            limpiarEstado(session, token);
            mostrarMensaje(request, "Nota de Débito guardada. ID: " + id, "alert-success");
        } else {
            Long id = notaCreditoService.guardarNotaCreditoCompleta(estado.nota, estado.listaDetalle, libroIva);
            limpiarEstado(session, token);
            mostrarMensaje(request, "Nota de Crédito guardada. ID: " + id, "alert-success");
        }
        accionListarModal(request, response);
    }

    private void accionAnular(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        NotaState estado = obtenerEstadoORedireccionar(request, response, session, token);
        if (estado == null) return;

        if (estado.idNotaExistente == null) {
            mostrarMensaje(request, "No hay una nota cargada para anular", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }
        if ("Anulado".equals(estado.nota.getEstado())) {
            mostrarMensaje(request, "La nota ya está anulada", "alert-warning");
            cargarDatosParaVista(request, estado, token);
            forward(request, response, JSP_NOTA);
            return;
        }

        // TODO: guard "crédito ya neteado en una provisión" (§8.2/§6) cuando exista el módulo de provisión.

        long total = 0L;
        for (NotaCreditoCompraDetalle d : estado.listaDetalle) {
            if (d.getCantidad() != null && d.getMonto() != null) {
                total += d.getCantidad() * d.getMonto();
            }
        }

        estado.nota.setFacturaCompra(estado.facturaReferenciada);
        if ("debito".equals(estado.tipoNota)) {
            NotaDebitoCompra nd = mapAsDebito(estado.nota);
            nd.setIdNotaDebitoCompra(estado.idNotaExistente);
            notaDebitoService.anularNotaDebitoCompleta(nd, total);
        } else {
            estado.nota.setIdNotaCreditoCompra(estado.idNotaExistente);
            notaCreditoService.anularNotaCreditoCompleta(estado.nota, total);
        }

        limpiarEstado(session, token);
        mostrarMensaje(request, "Nota anulada correctamente", "alert-success");
        accionListarModal(request, response);
    }

    private void accionCancelar(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, String token) throws ServletException, IOException, SQLException {
        limpiarEstado(session, token);
        accionListarModal(request, response);
    }

    // ==================== LECTURA DE FORMULARIO ====================

    private void leerDatosCabecera(HttpServletRequest request, NotaState estado) {
        String tipoNota = request.getParameter("tipoNota");
        if (tipoNota != null && !tipoNota.isEmpty()) {
            estado.tipoNota = tipoNota;
        }

        String numero = request.getParameter("numeroComprobante");
        if (numero != null && !numero.isEmpty()) {
            estado.nota.setNumero(numero);
        }

        String timbradoStr = request.getParameter("timbrado");
        if (timbradoStr != null && !timbradoStr.isEmpty()) {
            try {
                estado.nota.setTimbrado(Integer.parseInt(timbradoStr));
            } catch (NumberFormatException e) {
                // ignorar
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String fechaEmisionStr = request.getParameter("fechaEmision");
            if (fechaEmisionStr != null && !fechaEmisionStr.isEmpty()) {
                estado.nota.setFechaEmision(sdf.parse(fechaEmisionStr));
            }
            String fechaVencStr = request.getParameter("fechaVencTimbrado");
            if (fechaVencStr != null && !fechaVencStr.isEmpty()) {
                estado.nota.setFechaVenciTimbrado(sdf.parse(fechaVencStr));
            }
        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Error al parsear fecha en leerDatosCabecera", e);
        }

        String motivo = request.getParameter("motivo");
        if (motivo != null) {
            estado.nota.setMotivo(motivo);
        }
        String observacion = request.getParameter("observacion");
        if (observacion != null) {
            estado.nota.setObservacion(observacion);
        }
    }

    /**
     * Lee la fila del editor de línea. Devuelve null si falta cantidad o monto.
     * Línea con artículo + depósito = devolución (mueve stock en NC);
     * línea sin artículo = financiera (descuento/recargo).
     */
    private NotaCreditoCompraDetalle leerLineaEditor(HttpServletRequest request) throws SQLException {
        String cantidadStr = request.getParameter("cantidad");
        String montoStr = request.getParameter("monto");
        if (cantidadStr == null || cantidadStr.isEmpty() || montoStr == null || montoStr.isEmpty()) {
            return null;
        }

        NotaCreditoCompraDetalle det = new NotaCreditoCompraDetalle();
        det.setCantidad(Long.parseLong(cantidadStr));
        det.setMonto(Long.parseLong(montoStr));
        det.setDescripcion(request.getParameter("descripcion"));

        String idArticuloStr = request.getParameter("idArticulo");
        if (idArticuloStr != null && !idArticuloStr.isEmpty()) {
            det.setArticulo(articuloService.getArticulo(Long.parseLong(idArticuloStr)));
        }
        String idImpStr = request.getParameter("idTipoImpuesto");
        if (idImpStr != null && !idImpStr.isEmpty()) {
            det.setTipoImpuesto(tipoImpuestoService.getTipoImpuesto(Long.parseLong(idImpStr)));
        }
        String idDepStr = request.getParameter("idDeposito");
        if (idDepStr != null && !idDepStr.isEmpty()) {
            det.setDeposito(depositoService.getDeposito(Long.parseLong(idDepStr)));
        }
        return det;
    }

    // ==================== CÁLCULO DE IVA ====================

    private TotalesIva calcularTotalesIva(List<NotaCreditoCompraDetalle> listaDetalle) {
        TotalesIva totales = new TotalesIva();
        for (NotaCreditoCompraDetalle detalle : listaDetalle) {
            long subtotal = detalle.getCantidad() * detalle.getMonto();
            detalle.setSubtotal(subtotal);
            totales.total += subtotal;

            detalle.setGravada10(0L);
            detalle.setIva10(0L);
            detalle.setGravada5(0L);
            detalle.setIva5(0L);
            detalle.setExenta(0L);

            String descImpuesto = "";
            if (detalle.getArticulo() != null && detalle.getArticulo().getTipoImpuesto() != null) {
                descImpuesto = detalle.getArticulo().getTipoImpuesto().getDescripcion();
            } else if (detalle.getTipoImpuesto() != null) {
                descImpuesto = detalle.getTipoImpuesto().getDescripcion();
            }
            if (descImpuesto == null) {
                descImpuesto = "";
            }

            if (descImpuesto.contains("10")) {
                long iva = subtotal / 11;
                detalle.setIva10(iva);
                detalle.setGravada10(subtotal - iva);
                totales.iva10 += iva;
                totales.gravada10 += subtotal - iva;
            } else if (descImpuesto.contains("5")) {
                long iva = subtotal / 21;
                detalle.setIva5(iva);
                detalle.setGravada5(subtotal - iva);
                totales.iva5 += iva;
                totales.gravada5 += subtotal - iva;
            } else {
                detalle.setExenta(subtotal);
                totales.exenta += subtotal;
            }
        }
        return totales;
    }

    private void calcularImpuestos(HttpServletRequest request, List<NotaCreditoCompraDetalle> listaDetalle) {
        TotalesIva totales = calcularTotalesIva(listaDetalle);
        request.setAttribute("totalGeneral", totales.total);
        request.setAttribute("totalIva10", totales.iva10);
        request.setAttribute("totalIva5", totales.iva5);
        request.setAttribute("totalExenta", totales.exenta);
    }

    private LibroIvaCompra construirLibroIvaNota(TotalesIva t, Date fecha) {
        LibroIvaCompra libro = new LibroIvaCompra();
        libro.setFecha(fecha != null ? fecha : new Date());
        libro.setIva5(t.iva5);
        libro.setIva10(t.iva10);
        libro.setGravada5(t.gravada5);
        libro.setGravada10(t.gravada10);
        libro.setExenta(t.exenta);
        libro.setTotal(t.total);
        libro.setEstado("Activo");
        return libro;
    }

    // ==================== MAPEO CRÉDITO <-> DÉBITO ====================

    private NotaDebitoCompra mapAsDebito(NotaCreditoCompra n) {
        NotaDebitoCompra d = new NotaDebitoCompra();
        d.setNumero(n.getNumero());
        d.setTimbrado(n.getTimbrado());
        d.setFechaVenciTimbrado(n.getFechaVenciTimbrado());
        d.setFechaEmision(n.getFechaEmision());
        d.setFechaCarga(n.getFechaCarga());
        d.setEstado(n.getEstado());
        d.setObservacion(n.getObservacion());
        d.setUsuario(n.getUsuario());
        d.setProveedor(n.getProveedor());
        d.setFacturaCompra(n.getFacturaCompra());
        d.setMotivo(n.getMotivo());
        return d;
    }

    private NotaCreditoCompra mapFromDebito(NotaDebitoCompra d) {
        NotaCreditoCompra n = new NotaCreditoCompra();
        n.setNumero(d.getNumero());
        n.setTimbrado(d.getTimbrado());
        n.setFechaVenciTimbrado(d.getFechaVenciTimbrado());
        n.setFechaEmision(d.getFechaEmision());
        n.setFechaCarga(d.getFechaCarga());
        n.setEstado(d.getEstado());
        n.setObservacion(d.getObservacion());
        n.setUsuario(d.getUsuario());
        n.setProveedor(d.getProveedor());
        n.setFacturaCompra(d.getFacturaCompra());
        n.setMotivo(d.getMotivo());
        return n;
    }

    private List<NotaDebitoCompraDetalle> mapDetallesDebito(List<NotaCreditoCompraDetalle> src) {
        List<NotaDebitoCompraDetalle> out = new ArrayList<>();
        for (NotaCreditoCompraDetalle c : src) {
            out.add(new NotaDebitoCompraDetalle(null, c.getArticulo(), c.getCantidad(),
                    c.getMonto(), c.getDescripcion(), c.getTipoImpuesto()));
        }
        return out;
    }

    private NotaCreditoCompraDetalle mapDebitoDetalleToWorking(NotaDebitoCompraDetalle d) {
        NotaCreditoCompraDetalle w = new NotaCreditoCompraDetalle();
        w.setId(d.getId());
        w.setArticulo(d.getArticulo());
        w.setCantidad(d.getCantidad());
        w.setMonto(d.getMonto());
        w.setDescripcion(d.getDescripcion());
        w.setTipoImpuesto(d.getTipoImpuesto());
        return w;
    }

    // ==================== doGet / doPost ====================

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
