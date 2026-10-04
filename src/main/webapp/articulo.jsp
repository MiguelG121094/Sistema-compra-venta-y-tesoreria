<%--
    Document   : articulo
    ABM de artículos (referencial de Compras). Visual de referencial (formulario + grilla),
    calcado de cuenta.jsp y cableado a ArticuloServlet. Sigue el prototipo
    Images/Prototipo-Articulo.png, más el código de barras que usa el escaneo desde el celular.
--%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%@ page import="modelo.Usuario" %>
<%
    HttpSession sessionObj = request.getSession(false);
    if (sessionObj == null || sessionObj.getAttribute("usuario") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Usuario usuario = (Usuario) sessionObj.getAttribute("usuario");
%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
    <jsp:include page="header.jsp" />
    <head>
        <title>Artículos</title>
    </head>
    <body class="sb-nav-fixed">
        <jsp:include page="menuSuperior.jsp" />
        <div id="layoutSidenav">
            <jsp:include page="menuLateral.jsp" />
            <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <!-- Título -->
                        <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                            <span style="height: 100%; width: 100%; background-color: yellow">
                                <h1 style="text-align: center"><strong>ARTÍCULO</strong></h1></span>
                        </div>
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <%-- Si el alta o la edición se rechazan, el formulario se rearma con lo que vino en
                             el request y no queda vacío. Sólo si además falló: cuando sale bien tiene que
                             quedar limpio para cargar el siguiente. --%>
                        <c:set var="rearmar" value="${(param.accion eq 'Insertar' or param.accion eq 'Actualizar') and tipoAlert ne 'alert-success'}" />
                        <c:set var="vId" value="${rearmar and not empty param.id ? param.id : articuloEdit.getIdArticulo()}" />
                        <c:set var="vDescripcion" value="${rearmar ? param.descripcion : articuloEdit.getDescripcion()}" />
                        <c:set var="vCodigo" value="${rearmar ? param.codigo : articuloEdit.getCodigo()}" />
                        <c:set var="vPrecioCompra" value="${rearmar ? param.precioCompra : articuloEdit.getPrecioCompra()}" />
                        <c:set var="vPrecioVenta" value="${rearmar ? param.precioVenta : articuloEdit.getPrecioVenta()}" />
                        <c:set var="vTipoArticulo" value="${rearmar ? param.idTipoArticulo : articuloEdit.getTipoArticulo().getIdTipoArticulo()}" />
                        <c:set var="vImpuesto" value="${rearmar ? param.idImpuesto : articuloEdit.getTipoImpuesto().getIdTipoImpuesto()}" />
                        <c:set var="vMarca" value="${rearmar ? param.idMarca : articuloEdit.getMarca().getIdMarca()}" />
                        <c:set var="vPresentacion" value="${rearmar ? param.idPresentacion : articuloEdit.getPresentacion().getIdPresentacion()}" />
                        <c:set var="vEstado" value="${rearmar ? param.estado : articuloEdit.getEstado()}" />
                        <c:set var="editando" value="${not empty vId}" />

                        <div class="row g-3">
                            <!-- Formulario -->
                            <div class="col-12 col-xl-4">
                                <div class="card">
                                    <div class="card-body">
                                        <form action="ArticuloServlet?menu=Articulo" method="POST">
                                            <input type="hidden" name="id" value="${vId}">

                                            <div class="mb-3">
                                                <label class="form-label">Descripción</label>
                                                <input type="text" maxlength="100" class="form-control" name="descripcion"
                                                       value="${vDescripcion}" required="true">
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Código de barras</label>
                                                <input type="text" maxlength="50" class="form-control" name="codigo"
                                                       value="${vCodigo}">
                                            </div>

                                            <div class="row">
                                                <div class="col-md-6 mb-3">
                                                    <label class="form-label">Precio de Compra</label>
                                                    <input type="text" inputmode="numeric" class="form-control mask-miles"
                                                           name="precioCompra" value="${vPrecioCompra}">
                                                </div>
                                                <div class="col-md-6 mb-3">
                                                    <label class="form-label">Precio de Venta</label>
                                                    <input type="text" inputmode="numeric" class="form-control mask-miles"
                                                           name="precioVenta" value="${vPrecioVenta}" required="true">
                                                </div>
                                            </div>

                                            <div class="row">
                                                <div class="col-md-6 mb-3">
                                                    <label class="form-label">Tipo de artículo</label>
                                                    <select class="form-control" name="idTipoArticulo">
                                                        <option value="">Seleccionar...</option>
                                                        <c:forEach var="tip" items="${listaTiposArticulo}">
                                                            <option value="${tip.getIdTipoArticulo()}"
                                                                ${vTipoArticulo == tip.getIdTipoArticulo() ? 'selected' : ''}>
                                                                ${tip.getDescripcion()}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                                <div class="col-md-6 mb-3">
                                                    <label class="form-label">Impuesto</label>
                                                    <select class="form-control" name="idImpuesto" required="true">
                                                        <option value="">Seleccionar...</option>
                                                        <c:forEach var="imp" items="${listaImpuestos}">
                                                            <option value="${imp.getIdTipoImpuesto()}"
                                                                ${vImpuesto == imp.getIdTipoImpuesto() ? 'selected' : ''}>
                                                                ${imp.getDescripcion()}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </div>

                                            <div class="row">
                                                <div class="col-md-6 mb-3">
                                                    <label class="form-label">Marca</label>
                                                    <select class="form-control" name="idMarca">
                                                        <option value="">Seleccionar...</option>
                                                        <c:forEach var="mar" items="${listaMarcas}">
                                                            <option value="${mar.getIdMarca()}"
                                                                ${vMarca == mar.getIdMarca() ? 'selected' : ''}>
                                                                ${mar.getDescripcion()}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                                <div class="col-md-6 mb-3">
                                                    <label class="form-label">Presentación</label>
                                                    <select class="form-control" name="idPresentacion">
                                                        <option value="">Seleccionar...</option>
                                                        <c:forEach var="pre" items="${listaPresentaciones}">
                                                            <option value="${pre.getIdPresentacion()}"
                                                                ${vPresentacion == pre.getIdPresentacion() ? 'selected' : ''}>
                                                                ${pre.getDescripcion()}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Estado</label>
                                                <select class="form-control" name="estado" required="true">
                                                    <option value="${estadoActivo}"
                                                        ${empty vEstado or vEstado eq estadoActivo ? 'selected' : ''}>${estadoActivo}</option>
                                                    <option value="${estadoInactivo}"
                                                        ${vEstado eq estadoInactivo ? 'selected' : ''}>${estadoInactivo}</option>
                                                </select>
                                            </div>

                                            <button type="submit" name="accion" value="Insertar" class="btn btn-success"
                                                    <c:if test="${editando or not puedeInsertar}"><c:out value="disabled='disabled'"/></c:if>>Agregar</button>

                                            <button type="submit" name="accion" value="Actualizar" class="btn btn-primary"
                                                    <c:if test="${not editando or not puedeEditar}"><c:out value="disabled='disabled'"/></c:if>>Actualizar</button>

                                            <a href="ArticuloServlet?menu=Articulo&accion=Cancelar" class="btn btn-danger">Cancelar</a>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- Tabla -->
                            <div class="col-12 col-xl-8">
                                <div class="card">
                                    <div class="card-body table-responsive">
                                        <table id="tablaArticulos" class="table table-bordered w-100">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id</th>
                                                    <th class="text-bg-dark text-center">Descripción</th>
                                                    <th class="text-bg-dark text-center">Código</th>
                                                    <th class="text-bg-dark text-center">Marca</th>
                                                    <th class="text-bg-dark text-center">Presentación</th>
                                                    <th class="text-bg-dark text-center">Tipo de artículo</th>
                                                    <th class="text-bg-dark text-center">Impuesto</th>
                                                    <th class="text-bg-dark text-center">Precio compra</th>
                                                    <th class="text-bg-dark text-center">Precio venta</th>
                                                    <th class="text-bg-dark text-center">Estado</th>
                                                    <th class="text-bg-dark text-center">Acciones</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="art" items="${listaArticulos}">
                                                    <tr class="${art.getEstado() eq estadoInactivo ? 'table-secondary' : ''}">
                                                        <td class="text-center">${art.getIdArticulo()}</td>
                                                        <td>${art.getDescripcion()}</td>
                                                        <td class="text-center">${art.getCodigo()}</td>
                                                        <td class="text-center">${art.getMarca().getDescripcion()}</td>
                                                        <td class="text-center">${art.getPresentacion().getDescripcion()}</td>
                                                        <td class="text-center">${art.getTipoArticulo().getDescripcion()}</td>
                                                        <td class="text-center">${art.getTipoImpuesto().getDescripcion()}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${art.getPrecioCompra()}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${art.getPrecioVenta()}" pattern="#,##0"/></td>
                                                        <td class="text-center">${art.getEstado()}</td>
                                                        <td class="text-center">
                                                            <c:if test="${puedeEditar}">
                                                                <a href="ArticuloServlet?menu=Articulo&accion=Editar&id=${art.getIdArticulo()}" class="btn btn-warning">Editar</a>
                                                            </c:if>
                                                            <c:if test="${puedeBorrar}">
                                                                <a href="#" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalEliminar${art.getIdArticulo()}">Eliminar</a>
                                                                <!-- Modal de confirmación (sin javascript) -->
                                                                <div class="modal fade" id="modalEliminar${art.getIdArticulo()}" tabindex="-1" aria-hidden="true">
                                                                    <div class="modal-dialog modal-dialog-centered">
                                                                        <div class="modal-content">
                                                                            <div class="modal-header">
                                                                                <h1 class="modal-title fs-5">Confirmación</h1>
                                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                            </div>
                                                                            <div class="modal-body">
                                                                                ¿Seguro que quiere eliminar el artículo <strong>${art.getDescripcion()}</strong>?
                                                                            </div>
                                                                            <div class="modal-footer">
                                                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                                                <form action="ArticuloServlet?menu=Articulo" method="POST">
                                                                                    <input type="hidden" name="accion" value="Eliminar">
                                                                                    <input type="hidden" name="id" value="${art.getIdArticulo()}">
                                                                                    <button type="submit" class="btn btn-primary">Sí</button>
                                                                                </form>
                                                                            </div>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </c:if>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>
                <footer class="py-4 bg-light mt-auto">
                    <div class="container-fluid px-4">
                        <div class="d-flex align-items-center justify-content-between small">
                            <div class="text-muted">Copyright &copy; Sistema Compras y Tesorería 2025</div>
                            <div><a href="#">Privacy Policy</a> &middot; <a href="#">Terms &amp; Conditions</a></div>
                        </div>
                    </div>
                </footer>
            </div>
        </div>

        <script>
            $(document).ready(function () {
                $('.mask-miles').mask('#.##0', {reverse: true});

                $('#tablaArticulos').DataTable({
                    // Sin esto DataTables calcula un ancho fijo en px al cargar y la grilla se
                    // desborda al cambiar el zoom o el tamaño de la ventana.
                    autoWidth: false,
                    language: { url: "DataTables 2/es-ES.json" }
                });
            });
        </script>

        <!-- Mensajes con Toastr -->
        <c:if test="${not empty Message}">
            <script>
                toastr.options = {
                    positionClass: "toast-top-right",
                    closeButton: true,
                    timeOut: 5000,
                    progressBar: true
                };
                <c:choose>
                    <c:when test="${tipoAlert == 'alert-success'}">toastr.success('${Message}');</c:when>
                    <c:when test="${tipoAlert == 'alert-danger'}">toastr.error('${Message}');</c:when>
                    <c:when test="${tipoAlert == 'alert-warning'}">toastr.warning('${Message}');</c:when>
                    <c:otherwise>toastr.info('${Message}');</c:otherwise>
                </c:choose>
            </script>
        </c:if>
    </body>
</html>
