<%--
    Document   : provision
    Provisión de pago (Tesorería), cableada a ProvisionCuentaPagarServlet.
    Patrón formulario único + JS (Session+Token).
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
        <title>Provisión de Pago</title>
        <style>
            .custom-card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
            .border-section { border-top: 2px solid #dee2e6; margin: 16px 0; padding-top: 16px; }
            .custom-table { width: 100%; border-collapse: collapse; }
            .custom-table th, .custom-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            .custom-table th { background-color: #e9ecef; font-weight: bold; }
            .btn-responsive { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
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
                                <h1 style="text-align: center"><strong>PROVISIÓN DE PAGO</strong></h1>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 15px 0;"></div>
                        </div>

                        <!-- Botones principales -->
                        <div class="row mb-3">
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="ProvisionCuentaPagarServlet?menu=ProvisionCuentaPagar&accion=Nuevo" class="btn btn-success">Nuevo</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-success" disabled title="No tiene permisos">Nuevo</button>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalBuscarProvision">Buscar Provisión</button>
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalListaCuentas"
                                        <c:if test="${empty token or not esNuevo or empty proveedorSeleccionado}">disabled</c:if>>Lista de Cuentas a Pagar</button>
                                <c:choose>
                                    <c:when test="${not empty idProvisionExistente and provision.estado ne 'Anulado' and puedeBorrar}">
                                        <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular">Anular</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-danger" disabled>Anular</button>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button" class="btn btn-info text-white" disabled title="Disponible con el módulo Fondo Fijo">Buscar rendición Fondo Fijo</button>
                            </div>
                        </div>

                        <!-- Formulario principal -->
                        <form id="formPrincipal" method="post" action="ProvisionCuentaPagarServlet">
                            <input type="hidden" name="menu" value="ProvisionCuentaPagar">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Generar">
                            <input type="hidden" name="index" id="indexLinea" value="">
                            <input type="hidden" name="idProveedor" id="idProveedorHidden" value="">
                            <input type="hidden" name="idCtaPagar" id="idCtaPagarHidden" value="">
                            <input type="hidden" name="idFactura" id="idFacturaHidden" value="">

                            <!-- Cabecera -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="row mb-3">
                                        <div class="col-md-1">
                                            <div class="mb-3 mb-md-0 h-100">
                                                <button type="button" id="btnBuscarProveedor" data-bs-toggle="modal" data-bs-target="#modalBuscarProveedor"
                                                        style="overflow: hidden; text-overflow: ellipsis;" title="Buscar Proveedor"
                                                        class="btn btn-outline-primary w-100 h-100 btn-responsive"
                                                        <c:if test="${empty token or not esNuevo}">disabled</c:if>>Buscar Proveedor</button>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="razonSocial" type="text" placeholder="Razón Social" readonly value="${proveedorSeleccionado.razonSocial}" />
                                                <label for="razonSocial">Razón Social</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fecha" type="text" placeholder="Fecha" readonly
                                                       value="<fmt:formatDate value='${provision.fecha}' pattern='dd/MM/yyyy'/>" />
                                                <label for="fecha">Fecha</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="estado" type="text" placeholder="Estado" readonly
                                                       value="${empty provision.estado ? 'Pendiente' : provision.estado}" />
                                                <label for="estado">Estado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fondoFijo" type="text" placeholder="Fondo Fijo" readonly value="No" />
                                                <label for="fondoFijo">Fondo Fijo</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Editor de línea (solo al crear) -->
                            <c:if test="${not empty token and esNuevo}">
                                <div class="border-section"></div>
                                <div class="row mb-3">
                                    <div class="col custom-card">
                                        <div class="row">
                                            <div class="col-md-3">
                                                <input type="text" class="form-control" placeholder="Comprobante N°" readonly
                                                       value="${cuentaEnEditor.facturaCompra.numero}">
                                            </div>
                                            <div class="col-md-3">
                                                <input type="text" class="form-control" placeholder="Fecha de emisión" readonly
                                                       value="<fmt:formatDate value='${cuentaEnEditor.facturaCompra.fechaEmision}' pattern='dd/MM/yyyy'/>">
                                            </div>
                                            <div class="col-md-3">
                                                <input type="text" inputmode="numeric" name="importe" id="importeInput" class="form-control"
                                                       placeholder="Importe a pagar" value="${importeEditor}" oninput="formatMiles(this);"
                                                       title="Para saldo a favor (NC) el importe es negativo">
                                            </div>
                                            <div class="col-md-2">
                                                <button type="button" class="btn btn-success w-100" onclick="agregarLinea();"
                                                        <c:if test="${empty cuentaEnEditor or not puedeInsertar}">disabled</c:if>>Agregar</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Detalle -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="table-responsive">
                                        <table id="tablaDetalleProvision" class="table table-bordered table-sm custom-table">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Item</th>
                                                    <th class="text-bg-dark text-center">Nro. de factura</th>
                                                    <th class="text-bg-dark text-center">Importe total</th>
                                                    <th class="text-bg-dark text-center">Saldo pendiente</th>
                                                    <th class="text-bg-dark text-center">Importe a pagar</th>
                                                    <th class="text-bg-dark text-center">Plazo de pago</th>
                                                    <c:if test="${not empty token and esNuevo}">
                                                        <th class="text-bg-dark text-center">Acción</th>
                                                    </c:if>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="det" items="${listaDetalle}" varStatus="st">
                                                    <tr>
                                                        <td class="text-center">${st.index + 1}</td>
                                                        <td class="text-center">${det.cuentaPagar.facturaCompra.numero}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${det.cuentaPagar.monto}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${det.cuentaPagar.saldo}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${det.monto}" pattern="#,##0"/></td>
                                                        <td class="text-center">${empty det.cuentaPagar.plazo ? '-' : det.cuentaPagar.plazo} días</td>
                                                        <c:if test="${not empty token and esNuevo}">
                                                            <td class="text-center">
                                                                <c:if test="${puedeInsertar}">
                                                                    <a href="ProvisionCuentaPagarServlet?menu=ProvisionCuentaPagar&accion=EditarLinea&token=${token}&index=${st.index}"
                                                                       class="btn btn-warning btn-sm">Editar</a>
                                                                    <button type="button" class="btn btn-danger btn-sm" data-bs-toggle="modal" data-bs-target="#modalEliminarLinea"
                                                                            onclick="document.getElementById('indexLinea').value=${st.index};">Eliminar</button>
                                                                </c:if>
                                                            </td>
                                                        </c:if>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>

                                    <div class="row mt-2">
                                        <div class="col-md-6">
                                            <c:if test="${not empty token and esNuevo and puedeInsertar}">
                                                <button type="button" class="btn btn-success" onclick="generarProvision();">Generar</button>
                                            </c:if>
                                            <c:if test="${not empty token}">
                                                <a href="ProvisionCuentaPagarServlet?menu=ProvisionCuentaPagar&accion=Cancelar&token=${token}" class="btn btn-danger">Cancelar</a>
                                            </c:if>
                                        </div>
                                        <div class="col-md-6 text-end">
                                            <h5>Total a pagar: <fmt:formatNumber value="${totalProvision}" pattern="#,##0"/></h5>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar Proveedor -->
                        <div class="modal fade" id="modalBuscarProveedor" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar Proveedor</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaProveedores" class="table table-bordered table-striped">
                                            <thead><tr>
                                                <th class="text-bg-dark text-center">Razón Social</th>
                                                <th class="text-bg-dark text-center">RUC</th>
                                                <th class="text-bg-dark text-center no-search">Acción</th>
                                            </tr></thead>
                                            <tbody>
                                                <c:forEach var="prov" items="${listaProveedores}">
                                                    <tr>
                                                        <td>${prov.razonSocial}</td>
                                                        <td class="text-center">${prov.ruc}</td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal"
                                                                    onclick="seleccionarProveedor(${prov.idProveedor});">Seleccionar</button>
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
                        <div class="modal fade" id="modalListaCuentas" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Cuentas a Pagar del proveedor</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaCuentas" class="table table-bordered table-striped">
                                            <thead><tr>
                                                <th class="text-bg-dark text-center">Nro. de factura</th>
                                                <th class="text-bg-dark text-center">Importe total</th>
                                                <th class="text-bg-dark text-center">Saldo pendiente</th>
                                                <th class="text-bg-dark text-center">Plazo</th>
                                                <th class="text-bg-dark text-center no-search">Acción</th>
                                            </tr></thead>
                                            <tbody>
                                                <c:forEach var="cta" items="${listaCuentasPagar}">
                                                    <tr class="${cta.saldo < 0 ? 'table-info' : ''}">
                                                        <td class="text-center">${cta.facturaCompra.numero}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${cta.monto}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${cta.saldo}" pattern="#,##0"/></td>
                                                        <td class="text-center">${empty cta.plazo ? '-' : cta.plazo} días</td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal"
                                                                    onclick="seleccionarCuenta(${cta.idCuentaPagar}, ${cta.facturaCompra.idFacturaCompra});">Seleccionar</button>
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

                        <!-- Modal Buscar Provisión -->
                        <div class="modal fade" id="modalBuscarProvision" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar Provisión</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaProvisiones" class="table table-bordered table-striped">
                                            <thead><tr>
                                                <th class="text-bg-dark text-center">N°</th>
                                                <th class="text-bg-dark text-center">Proveedor</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center">Estado</th>
                                                <th class="text-bg-dark text-center no-search">Acción</th>
                                            </tr></thead>
                                            <tbody>
                                                <c:forEach var="p" items="${listaProvisiones}">
                                                    <tr class="${p.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                        <td class="text-center">${p.idProvisionCuentaPagar}</td>
                                                        <td>${p.proveedor.razonSocial}</td>
                                                        <td class="text-center"><fmt:formatDate value="${p.fecha}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center">${p.estado}</td>
                                                        <td class="text-center">
                                                            <a href="ProvisionCuentaPagarServlet?menu=ProvisionCuentaPagar&accion=CargarProvision&idProvision=${p.idProvisionCuentaPagar}"
                                                               class="btn btn-primary btn-sm">Seleccionar</a>
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

                        <!-- Modal Confirmar Anular -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea anular esta provisión?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularProvision();">Sí, Anular</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Eliminar línea -->
                        <div class="modal fade" id="modalEliminarLinea" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea eliminar esta línea?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="confirmarEliminarLinea();">Sí, Eliminar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </main>
            </div>
        </div>

        <script>
            function setAccion(a) { document.getElementById('accionPrincipal').value = a; }
            function limpiarMascaras(form) {
                $(form).find('.mask-miles').each(function () { $(this).val($(this).cleanVal()); });
            }
            // Separador de miles/millones que preserva el signo (para el saldo a favor negativo)
            function formatMiles(el) {
                var v = el.value.replace(/[^\d-]/g, '');
                var neg = v.charAt(0) === '-';
                v = v.replace(/-/g, '').replace(/\B(?=(\d{3})+(?!\d))/g, '.');
                el.value = (neg ? '-' : '') + v;
            }
            function seleccionarProveedor(id) {
                document.getElementById('idProveedorHidden').value = id;
                setAccion('CargarProveedor');
                document.getElementById('formPrincipal').submit();
            }
            function seleccionarCuenta(idCta, idFact) {
                document.getElementById('idCtaPagarHidden').value = idCta;
                document.getElementById('idFacturaHidden').value = idFact;
                setAccion('SeleccionarCuenta');
                document.getElementById('formPrincipal').submit();
            }
            function agregarLinea() {
                var imp = document.getElementById('importeInput');
                if (imp) { imp.value = imp.value.replace(/\./g, ''); }  // quitar puntos de miles antes de enviar
                setAccion('AgregarLinea');
                document.getElementById('formPrincipal').submit();
            }
            function confirmarEliminarLinea() {
                setAccion('EliminarLinea');
                document.getElementById('formPrincipal').submit();
            }
            function generarProvision() {
                setAccion('Generar');
                document.getElementById('formPrincipal').submit();
            }
            function anularProvision() {
                setAccion('Anular');
                document.getElementById('formPrincipal').submit();
            }

            $(document).ready(function () {
                var imp = document.getElementById('importeInput');
                if (imp) { formatMiles(imp); }   // formatear el importe precargado (incluye negativos)
                $('#tablaDetalleProvision').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaProveedores').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaCuentas').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaProvisiones').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
            });
        </script>

        <!-- Tooltip de ayuda sobre "Buscar Proveedor" al abrir una provisión nueva
             (lo dispara el servlet: request.setAttribute("mostrarTooltip", true) + "mensajeTooltip",
             igual que el de la sucursal en pedidoCompra.jsp) -->
        <c:if test="${mostrarTooltip}">
            <script>
                document.addEventListener("DOMContentLoaded", function () {
                    var btn = document.getElementById('btnBuscarProveedor');
                    if (!btn) { return; }
                    var tooltip = new bootstrap.Tooltip(btn, {
                        title: "${mensajeTooltip}",
                        placement: "auto"
                    });
                    tooltip.show();

                    // ocultar el tooltip después de 3 segundos
                    setTimeout(function () {
                        tooltip.hide();
                    }, 3000);
                });
            </script>
        </c:if>

        <!-- Mensajes Toastr -->
        <c:if test="${not empty Message}">
            <script>
                toastr.options = { positionClass: "toast-top-right", closeButton: true, timeOut: 5000, progressBar: true };
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
