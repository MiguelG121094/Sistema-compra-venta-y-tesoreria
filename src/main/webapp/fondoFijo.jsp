<%--
    Document   : fondoFijo
    ABM de fondo fijo (Tesorería, requerimiento 3.5). Visual de referencial (form + tabla),
    calcado de cuenta.jsp y cableado a FondoFijoServlet. Los fondos fijos que se cargan acá
    son los que después se eligen como responsable al rendir.
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
        <title>Fondo fijo</title>
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
                                <h1 style="text-align: center"><strong>FONDO FIJO</strong></h1></span>
                        </div>
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <%-- Si el alta o la edición se rechazan, el formulario se rearma con lo que vino en
                             el request y no queda vacío. Sólo si además falló: cuando sale bien tiene que
                             quedar limpio para cargar el siguiente. Un Eliminar rechazado también manda el
                             id, y no tiene que dejar el formulario en modo edición. --%>
                        <c:set var="rearmar" value="${(param.accion eq 'Insertar' or param.accion eq 'Actualizar') and tipoAlert ne 'alert-success'}" />
                        <c:set var="vId" value="${rearmar and not empty param.id ? param.id : fondoFijoEdit.getIdFondoFijo()}" />
                        <c:set var="vResponsable" value="${rearmar ? param.responsable : fondoFijoEdit.getResponsable()}" />
                        <c:set var="vMonto" value="${rearmar ? param.montoAsignado : fondoFijoEdit.getMontoAsignado()}" />
                        <c:set var="vProveedor" value="${rearmar ? param.idProveedor : fondoFijoEdit.getProveedor().getIdProveedor()}" />
                        <c:set var="vFecha"><c:choose><c:when test="${rearmar}">${param.fechaAsignacion}</c:when><c:otherwise><fmt:formatDate value="${fondoFijoEdit.getFechaAsignacion()}" pattern="yyyy-MM-dd"/></c:otherwise></c:choose></c:set>
                        <c:set var="editando" value="${not empty vId}" />

                        <div class="d-flex">
                            <!-- Formulario -->
                            <div class="col-sm-2" style="width: 33%;">
                                <div class="card">
                                    <div class="card-body">
                                        <form action="FondoFijoServlet?menu=FondoFijo" method="POST">
                                            <input type="hidden" name="id" value="${vId}">

                                            <div class="mb-3">
                                                <label class="form-label">Responsable</label>
                                                <input type="text" class="form-control" name="responsable" maxlength="50"
                                                       value="${vResponsable}" required="true">
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Monto asignado</label>
                                                <input type="text" inputmode="numeric" class="form-control" name="montoAsignado"
                                                       value="${vMonto}" required="true">
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Fecha de asignación</label>
                                                <input type="date" class="form-control" name="fechaAsignacion"
                                                       value="${vFecha}" required="true">
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Proveedor</label>
                                                <select class="form-control" name="idProveedor" required="true">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="prov" items="${listaProveedores}">
                                                        <option value="${prov.getIdProveedor()}"
                                                            ${vProveedor == prov.getIdProveedor() ? 'selected' : ''}>
                                                            ${prov.getRazonSocial()}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>

                                            <button type="submit" name="accion" value="Insertar" class="btn btn-success"
                                                    <c:if test="${editando or not puedeInsertar}"><c:out value="disabled='disabled'"/></c:if>>Agregar</button>

                                            <button type="submit" name="accion" value="Actualizar" class="btn btn-primary"
                                                    <c:if test="${not editando or not puedeEditar}"><c:out value="disabled='disabled'"/></c:if>>Actualizar</button>

                                            <a href="FondoFijoServlet?menu=FondoFijo&accion=Cancelar" class="btn btn-danger">Cancelar</a>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- Tabla -->
                            <div class="col-sm-7" style="margin-left: 33px;width: 65%;">
                                <div class="card">
                                    <div class="card-body">
                                        <table id="tablaFondosFijos" class="table table-bordered">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id</th>
                                                    <th class="text-bg-dark text-center">Responsable</th>
                                                    <th class="text-bg-dark text-center">Monto asignado</th>
                                                    <th class="text-bg-dark text-center">Fecha de asignación</th>
                                                    <th class="text-bg-dark text-center">Proveedor</th>
                                                    <th class="text-bg-dark text-center">Acciones</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="ff" items="${listaFondosFijos}">
                                                    <tr>
                                                        <td class="text-center">${ff.getIdFondoFijo()}</td>
                                                        <td class="text-center">${ff.getResponsable()}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${ff.getMontoAsignado()}" pattern="#,##0"/></td>
                                                        <td class="text-center"><fmt:formatDate value="${ff.getFechaAsignacion()}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center">${ff.getProveedor().getRazonSocial()}</td>
                                                        <td class="text-center">
                                                            <c:if test="${puedeEditar}">
                                                                <a href="FondoFijoServlet?menu=FondoFijo&accion=Editar&id=${ff.getIdFondoFijo()}" class="btn btn-warning">Editar</a>
                                                            </c:if>
                                                            <c:if test="${puedeBorrar}">
                                                                <a href="#" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalEliminar${ff.getIdFondoFijo()}">Eliminar</a>
                                                                <!-- Modal de confirmación (sin javascript) -->
                                                                <div class="modal fade" id="modalEliminar${ff.getIdFondoFijo()}" tabindex="-1" aria-hidden="true">
                                                                    <div class="modal-dialog modal-dialog-centered">
                                                                        <div class="modal-content">
                                                                            <div class="modal-header">
                                                                                <h1 class="modal-title fs-5">Confirmación</h1>
                                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                            </div>
                                                                            <div class="modal-body">
                                                                                ¿Seguro que quiere eliminar el fondo fijo de <strong>${ff.getResponsable()}</strong>?
                                                                            </div>
                                                                            <div class="modal-footer">
                                                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                                                <form action="FondoFijoServlet?menu=FondoFijo" method="POST">
                                                                                    <input type="hidden" name="accion" value="Eliminar">
                                                                                    <input type="hidden" name="id" value="${ff.getIdFondoFijo()}">
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
                $('#tablaFondosFijos').DataTable({
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
