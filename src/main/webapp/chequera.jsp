<%--
    Document   : chequera
    ABM de chequeras (Tesorería). Visual de referencial (form + tabla), calcado de
    cuenta.jsp y cableado a ChequeraServlet. La grilla muestra el consumo del rango
    para que el agotamiento se vea antes de que falle la emisión de una orden de pago.
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
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
    <jsp:include page="header.jsp" />
    <head>
        <title>Chequeras</title>
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
                                <h1 style="text-align: center"><strong>CHEQUERAS</strong></h1></span>
                        </div>
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <%-- Si el alta o la edición se rechazan, el formulario se rearma con lo que vino en
                             el request y no queda vacío. Sólo si además falló: cuando sale bien tiene que
                             quedar limpio para cargar el siguiente. Un Eliminar rechazado también manda el
                             id, y no tiene que dejar el formulario en modo edición. --%>
                        <c:set var="rearmar" value="${(param.accion eq 'Insertar' or param.accion eq 'Actualizar') and tipoAlert ne 'alert-success'}" />
                        <c:set var="vId" value="${rearmar and not empty param.id ? param.id : chequeraEdit.getIdChequera()}" />
                        <c:set var="vCuenta" value="${rearmar ? param.idCuenta : chequeraEdit.getCuenta().getIdCuenta()}" />
                        <c:set var="vSerie" value="${rearmar ? param.serie : chequeraEdit.getSerie()}" />
                        <c:set var="vDesde" value="${rearmar ? param.desdeNumero : chequeraEdit.getDesdeNumero()}" />
                        <c:set var="vHasta" value="${rearmar ? param.hastaNumero : chequeraEdit.getHastaNumero()}" />
                        <c:set var="editando" value="${not empty vId}" />

                        <div class="d-flex">
                            <!-- Formulario -->
                            <div class="col-sm-2" style="width: 33%;">
                                <div class="card">
                                    <div class="card-body">
                                        <form action="ChequeraServlet?menu=Chequera" method="POST">
                                            <input type="hidden" name="id" value="${vId}">

                                            <div class="mb-3">
                                                <label class="form-label">Cuenta bancaria</label>
                                                <select class="form-control" name="idCuenta" required="true">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="cta" items="${listaCuentas}">
                                                        <option value="${cta.getIdCuenta()}"
                                                            ${vCuenta == cta.getIdCuenta() ? 'selected' : ''}>
                                                            ${cta.getEntidadFinanciera().getNombre()} - ${cta.getNumero()} (${cta.getMoneda().getDescripcion()})
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Serie</label>
                                                <input type="text" inputmode="numeric" class="form-control" name="serie"
                                                       value="${vSerie}" required="true">
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Desde N°</label>
                                                <input type="text" inputmode="numeric" class="form-control" name="desdeNumero"
                                                       value="${vDesde}" required="true">
                                            </div>

                                            <div class="mb-3">
                                                <label class="form-label">Hasta N°</label>
                                                <input type="text" inputmode="numeric" class="form-control" name="hastaNumero"
                                                       value="${vHasta}" required="true">
                                            </div>

                                            <button type="submit" name="accion" value="Insertar" class="btn btn-success"
                                                    <c:if test="${editando or not puedeInsertar}"><c:out value="disabled='disabled'"/></c:if>>Agregar</button>

                                            <button type="submit" name="accion" value="Actualizar" class="btn btn-primary"
                                                    <c:if test="${not editando or not puedeEditar}"><c:out value="disabled='disabled'"/></c:if>>Actualizar</button>

                                            <a href="ChequeraServlet?menu=Chequera&accion=Cancelar" class="btn btn-danger">Cancelar</a>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- Tabla -->
                            <div class="col-sm-7" style="margin-left: 33px;width: 65%;">
                                <div class="card">
                                    <div class="card-body">
                                        <table id="tablaChequeras" class="table table-bordered">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id</th>
                                                    <th class="text-bg-dark text-center">Cuenta</th>
                                                    <th class="text-bg-dark text-center">Serie</th>
                                                    <th class="text-bg-dark text-center">Desde</th>
                                                    <th class="text-bg-dark text-center">Hasta</th>
                                                    <th class="text-bg-dark text-center">Emitidos</th>
                                                    <th class="text-bg-dark text-center">Próximo</th>
                                                    <th class="text-bg-dark text-center">Disponibles</th>
                                                    <th class="text-bg-dark text-center">Acciones</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="chq" items="${listaChequeras}">
                                                    <tr>
                                                        <td class="text-center">${chq.getIdChequera()}</td>
                                                        <td class="text-center">${chq.getCuenta().getEntidadFinanciera().getNombre()} - ${chq.getCuenta().getNumero()}</td>
                                                        <td class="text-center">${chq.getSerie()}</td>
                                                        <td class="text-center">${chq.getDesdeNumero()}</td>
                                                        <td class="text-center">${chq.getHastaNumero()}</td>
                                                        <td class="text-center">${chq.getEmitidos()}</td>
                                                        <td class="text-center">${empty chq.proximoNumero ? '—' : chq.getProximoNumero()}</td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${chq.getDisponibles() == 0}">
                                                                    <span class="badge bg-danger">Agotada</span>
                                                                </c:when>
                                                                <c:when test="${chq.getDisponibles() le 10}">
                                                                    <span class="badge bg-warning text-dark">${chq.getDisponibles()}</span>
                                                                </c:when>
                                                                <c:otherwise>${chq.getDisponibles()}</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-center">
                                                            <c:if test="${puedeEditar}">
                                                                <a href="ChequeraServlet?menu=Chequera&accion=Editar&id=${chq.getIdChequera()}" class="btn btn-warning">Editar</a>
                                                            </c:if>
                                                            <c:if test="${puedeBorrar}">
                                                                <a href="#" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalEliminar${chq.getIdChequera()}">Eliminar</a>
                                                                <!-- Modal de confirmación (sin javascript) -->
                                                                <div class="modal fade" id="modalEliminar${chq.getIdChequera()}" tabindex="-1" aria-hidden="true">
                                                                    <div class="modal-dialog modal-dialog-centered">
                                                                        <div class="modal-content">
                                                                            <div class="modal-header">
                                                                                <h1 class="modal-title fs-5">Confirmación</h1>
                                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                            </div>
                                                                            <div class="modal-body">
                                                                                ¿Seguro que quiere eliminar la chequera <strong>serie ${chq.getSerie()} (${chq.getDesdeNumero()}-${chq.getHastaNumero()})</strong>?
                                                                            </div>
                                                                            <div class="modal-footer">
                                                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                                                <form action="ChequeraServlet?menu=Chequera" method="POST">
                                                                                    <input type="hidden" name="accion" value="Eliminar">
                                                                                    <input type="hidden" name="id" value="${chq.getIdChequera()}">
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
                $('#tablaChequeras').DataTable({
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
