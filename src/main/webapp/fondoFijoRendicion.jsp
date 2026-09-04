<%--
    Document   : fondoFijoRendicion
    Rendición de fondo fijo (Tesorería), cableada a FondoFijoRendicionServlet.
    Patrón formulario único + JS (Session+Token), calcado de provision.jsp / ordenPago.jsp.

    Atributos de request esperados del servlet:
      token (String), esNuevo (boolean), idRendicionExistente (Long)
      rendicion (FondoFijoRendicion): numeroRendicion, fechaEmisionRendicion, estado
      fondoFijoSeleccionado (FondoFijo): responsable, montoAsignado
      listaDetalle (List<FondoFijoRendicionDetalle>) -> facturas rendidas
      totalRendicion (Long) = Σ monto rendido
      listaFondosFijos (modal de responsables), listaCuentasPagar (modal de cuentas a pagar),
      listaRendiciones (modal de búsqueda)
    Acciones (accionPrincipal): Nuevo, CargarResponsable, AgregarLinea, EliminarLinea,
      Generar, CargarRendicion, Anular, Cancelar
    La pantalla arranca INERTE: sin token sólo se puede usar Nuevo y Buscar Rendición. El
    responsable se elige primero; recién con uno cargado se habilita la lista de cuentas a pagar.
--%>
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
        <title>Rendición de Fondo Fijo</title>
        <style>
            .custom-card {
                border: 1px solid #ddd;
                border-radius: 8px;
                padding: 16px;
                margin-bottom: 16px;
            }
            .section-title {
                background-color: #e9ecef;
                padding: 8px 12px;
                margin: 0 0 12px 0;
                border-radius: 4px;
                font-weight: bold;
            }
        </style>
    </head>
    <body class="sb-nav-fixed">
        <jsp:include page="menuSuperior.jsp" />
        <div id="layoutSidenav">
            <jsp:include page="menuLateral.jsp" />
            <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <!-- Título -->
                        <div class="row mb-2">
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <h1 style="text-align: center"><strong>RENDICIÓN DE FONDO FIJO</strong></h1>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 15px 0;"></div>
                        </div>

                        <c:set var="anulada" value="${not empty rendicion.estado and rendicion.estado eq 'Anulado'}" />

                        <!-- Botones principales -->
                        <div class="row mb-3">
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="FondoFijoRendicionServlet?menu=RendicionFondoFijo&accion=Nuevo" class="btn btn-success">Nuevo</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-success" disabled title="No tiene permisos">Nuevo</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-auto">
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalBuscarRendicion">Buscar Rendición</button>
                            </div>
                            <div class="col-auto">
                                <c:set var="ctasBloqueadas" value="${empty token or not esNuevo or empty fondoFijoSeleccionado}" />
                                <span class="d-inline-block" tabindex="0"
                                      <c:if test="${ctasBloqueadas}">title="${empty token or not esNuevo ? 'Presione Nuevo para iniciar una rendición' : 'Primero seleccione el responsable'}"</c:if>>
                                    <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalCuentasPagar"
                                            <c:if test="${ctasBloqueadas}">disabled style="pointer-events: none;"</c:if>>Lista de Cuentas a Pagar</button>
                                </span>
                            </div>
                            <div class="col-auto">
                                <c:set var="anularBloqueado" value="${empty idRendicionExistente or anulada or not puedeBorrar}" />
                                <span class="d-inline-block" tabindex="0"
                                      <c:if test="${anularBloqueado}">title="${not puedeBorrar ? 'No tiene permisos' : (anulada ? 'La rendición ya está anulada' : 'Cargue una rendición para anularla')}"</c:if>>
                                    <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular"
                                            <c:if test="${anularBloqueado}">disabled style="pointer-events: none;"</c:if>>Anular</button>
                                </span>
                            </div>
                        </div>

                        <form id="formPrincipal" method="post" action="FondoFijoRendicionServlet">
                            <input type="hidden" name="menu" value="RendicionFondoFijo">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Generar">
                            <input type="hidden" name="index" id="indexLinea" value="">
                            <input type="hidden" name="idFondoFijo" id="idFondoFijo" value="">
                            <input type="hidden" name="idCtaPagar" id="idCtaPagar" value="">
                            <input type="hidden" name="idFacturaCompra" id="idFacturaCompra" value="">

                            <!-- Cabecera -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="row mb-3 align-items-center">
                                        <div class="col-md-2">
                                            <span class="d-inline-block" tabindex="0"
                                                  <c:if test="${empty token or not esNuevo}">title="Presione Nuevo para iniciar una rendición"</c:if>>
                                                <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalResponsables"
                                                        <c:if test="${empty token or not esNuevo}">disabled style="pointer-events: none;"</c:if>>Buscar Responsable</button>
                                            </span>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control" id="responsable" type="text" placeholder="Responsable" readonly
                                                       value="${fondoFijoSeleccionado.responsable}" />
                                                <label for="responsable">Responsable</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="montoAsignado" type="text" placeholder="Monto asignado" readonly
                                                       value="<fmt:formatNumber value='${fondoFijoSeleccionado.montoAsignado}' pattern='#,##0'/>" />
                                                <label for="montoAsignado">Monto asignado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="fechaEmision" type="text" placeholder="Fecha Emisión" readonly
                                                       value="<fmt:formatDate value='${rendicion.fechaEmisionRendicion}' pattern='dd/MM/yyyy'/>" />
                                                <label for="fechaEmision">Fecha Emisión</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="nroRendicion" type="text" placeholder="Nro Rendición" readonly
                                                       value="${rendicion.numeroRendicion}" />
                                                <label for="nroRendicion">Nro Rendición</label>
                                            </div>
                                        </div>
                                        <div class="col-md-1">
                                            <div class="form-floating">
                                                <input class="form-control" id="estado" type="text" placeholder="Estado" readonly
                                                       value="${rendicion.estado}" />
                                                <label for="estado">Estado</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Detalle -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="section-title">Detalle de rendición</div>
                                    <div class="table-responsive">
                                        <table id="tablaDetalleRendicion" class="table table-bordered">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Item</th>
                                                    <th class="text-bg-dark text-center">Nro. de factura</th>
                                                    <th class="text-bg-dark text-center">Proveedor</th>
                                                    <th class="text-bg-dark text-center">Fecha</th>
                                                    <th class="text-bg-dark text-center">Total</th>
                                                    <c:if test="${esNuevo}">
                                                        <th class="text-bg-dark text-center">Acción</th>
                                                    </c:if>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="det" items="${listaDetalle}" varStatus="st">
                                                    <tr>
                                                        <td class="text-center">${st.index + 1}</td>
                                                        <td class="text-center">${det.cuentaPagar.facturaCompra.numero}</td>
                                                        <td>${det.cuentaPagar.facturaCompra.proveedor.razonSocial}</td>
                                                        <td class="text-center"><fmt:formatDate value="${det.cuentaPagar.facturaCompra.fechaEmision}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${det.montoRendido}" pattern="#,##0"/></td>
                                                        <c:if test="${esNuevo}">
                                                            <td class="text-center">
                                                                <button type="button" class="btn btn-danger btn-sm"
                                                                        onclick="eliminarLinea(${st.index});">Eliminar</button>
                                                            </td>
                                                        </c:if>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>

                                    <div class="row mt-3">
                                        <div class="col-auto">
                                            <c:choose>
                                                <c:when test="${puedeInsertar}">
                                                    <span class="d-inline-block" tabindex="0"
                                                          <c:if test="${empty token or not esNuevo}">title="Presione Nuevo para iniciar una rendición"</c:if>>
                                                        <button type="button" class="btn btn-success" onclick="generarRendicion();"
                                                                <c:if test="${empty token or not esNuevo}">disabled style="pointer-events: none;"</c:if>>Generar</button>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-success" disabled title="No tiene permisos">Generar</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="col-auto">
                                            <a href="FondoFijoRendicionServlet?menu=RendicionFondoFijo&accion=Cancelar&token=${token}" class="btn btn-danger">Cancelar</a>
                                        </div>
                                        <div class="col-md-4 ms-auto">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="totalRendicion" type="text" placeholder="Total Rendición" readonly
                                                       value="<fmt:formatNumber value='${totalRendicion}' pattern='#,##0'/>" />
                                                <label for="totalRendicion">Total Rendición</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar Responsable -->
                        <div class="modal fade" id="modalResponsables" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Responsables de fondo fijo</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaResponsables" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id</th>
                                                    <th class="text-bg-dark text-center">Responsable</th>
                                                    <th class="text-bg-dark text-center">Monto asignado</th>
                                                    <th class="text-bg-dark text-center">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="ff" items="${listaFondosFijos}">
                                                    <tr>
                                                        <td class="text-center">${ff.idFondoFijo}</td>
                                                        <td>${ff.responsable}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${ff.montoAsignado}" pattern="#,##0"/></td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal"
                                                                    onclick="cargarResponsable(${ff.idFondoFijo});">Seleccionar</button>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="modal-footer"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button></div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Lista de Cuentas a Pagar -->
                        <div class="modal fade" id="modalCuentasPagar" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-xl">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Cuentas a pagar de fondo fijo</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaCuentasPagar" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Nro. de factura</th>
                                                    <th class="text-bg-dark text-center">Proveedor</th>
                                                    <th class="text-bg-dark text-center">Fecha</th>
                                                    <th class="text-bg-dark text-center">Importe total</th>
                                                    <th class="text-bg-dark text-center">Saldo</th>
                                                    <th class="text-bg-dark text-center">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="cta" items="${listaCuentasPagar}">
                                                    <tr>
                                                        <td class="text-center">${cta.facturaCompra.numero}</td>
                                                        <td>${cta.facturaCompra.proveedor.razonSocial}</td>
                                                        <td class="text-center"><fmt:formatDate value="${cta.facturaCompra.fechaEmision}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${cta.monto}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${cta.saldo}" pattern="#,##0"/></td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal"
                                                                    onclick="agregarLinea(${cta.idCuentaPagar}, ${cta.facturaCompra.idFacturaCompra});">Seleccionar</button>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="modal-footer"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button></div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Buscar Rendición -->
                        <div class="modal fade" id="modalBuscarRendicion" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-xl">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar Rendición</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaRendiciones" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Nro</th>
                                                    <th class="text-bg-dark text-center">Responsable</th>
                                                    <th class="text-bg-dark text-center">Fecha</th>
                                                    <th class="text-bg-dark text-center">Estado</th>
                                                    <th class="text-bg-dark text-center">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="ren" items="${listaRendiciones}">
                                                    <tr>
                                                        <td class="text-center">${ren.numeroRendicion}</td>
                                                        <td>${ren.fondoFijo.responsable}</td>
                                                        <td class="text-center"><fmt:formatDate value="${ren.fechaEmisionRendicion}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${ren.estado eq 'Anulado'}"><span class="badge bg-danger">Anulado</span></c:when>
                                                                <c:otherwise><span class="badge bg-success">${ren.estado}</span></c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-center">
                                                            <a href="FondoFijoRendicionServlet?menu=RendicionFondoFijo&accion=CargarRendicion&id=${ren.idFondoFijoRendicion}"
                                                               class="btn btn-primary btn-sm">Cargar</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="modal-footer"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button></div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Generar -->
                        <div class="modal fade" id="modalConfirmarGenerar" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea generar esta rendición?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-success" onclick="confirmarGenerar();">Sí, Generar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Anular -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea anular esta rendición?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularRendicion();">Sí, Anular</button>
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
            function setAccion(a) {
                document.getElementById('accionPrincipal').value = a;
            }
            function enviar() {
                document.getElementById('formPrincipal').submit();
            }
            function cargarResponsable(idFondoFijo) {
                document.getElementById('idFondoFijo').value = idFondoFijo;
                setAccion('CargarResponsable');
                enviar();
            }
            function agregarLinea(idCtaPagar, idFacturaCompra) {
                document.getElementById('idCtaPagar').value = idCtaPagar;
                document.getElementById('idFacturaCompra').value = idFacturaCompra;
                setAccion('AgregarLinea');
                enviar();
            }
            function eliminarLinea(index) {
                document.getElementById('indexLinea').value = index;
                setAccion('EliminarLinea');
                enviar();
            }
            // El caso de uso pide confirmación antes de guardar.
            function generarRendicion() {
                new bootstrap.Modal(document.getElementById('modalConfirmarGenerar')).show();
            }
            function confirmarGenerar() {
                setAccion('Generar');
                enviar();
            }
            function anularRendicion() {
                setAccion('Anular');
                enviar();
            }

            $(document).ready(function () {
                $('#tablaDetalleRendicion').DataTable({language: {url: "DataTables 2/es-ES.json"}});
                $('#tablaResponsables').DataTable({language: {url: "DataTables 2/es-ES.json"}});
                $('#tablaCuentasPagar').DataTable({language: {url: "DataTables 2/es-ES.json"}});
                $('#tablaRendiciones').DataTable({language: {url: "DataTables 2/es-ES.json"}});
            });
        </script>

        <!-- Mensajes Toastr -->
        <c:if test="${not empty Message}">
            <script>
                toastr.options = {positionClass: "toast-top-right", closeButton: true, timeOut: 5000, progressBar: true};
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
