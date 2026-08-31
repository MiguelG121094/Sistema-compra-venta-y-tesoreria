<%--
    Document   : movimientoBancario
    Carga de otros débitos y créditos bancarios (Tesorería), cableada a MovimientoBancarioServlet.
    Una sola vista para los dos tipos: el servlet manda el tipo y sus textos.
    Cada tipo entra por su propia URL (DebitoServlet / CreditoServlet, ambas del mismo servlet),
    que llega en el atributo "ruta": el resaltado del menú compara el nombre del servlet, así que
    dos entradas a la misma URL se pintarían las dos como activas.

    Atributos de request esperados del servlet:
      tipo (String: "debito" | "credito"), etiqueta ("Débito" | "Crédito"), titulo (encabezado)
      movimiento (MovimientoBancario, opcional): el que está en pantalla, ya generado
      esNuevo (boolean): el formulario está habilitado para cargar uno nuevo
      listaCuentas, listaEntidades (combos), listaMovimientos (modal de búsqueda)
      puedeInsertar / puedeBorrar (boolean)
    Acciones (accionPrincipal): Nuevo, Cargar, Generar, Anular, Cancelar
    La pantalla arranca INERTE: sin "Nuevo" ni un movimiento cargado, los campos van deshabilitados.
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
        <title>Cargar ${etiqueta}s</title>
        <style>
            .custom-card {
                border: 1px solid #ddd;
                border-radius: 8px;
                padding: 16px;
                margin-bottom: 16px;
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
                                <h1 style="text-align: center"><strong>${titulo}</strong></h1>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 15px 0;"></div>
                        </div>

                        <c:set var="anulado" value="${not empty movimiento and movimiento.estado eq 'Anulado'}" />

                        <%-- Si el Generar se rechaza, la vista se rearma con lo que el usuario había
                             escrito (viene en el request) y no con el movimiento, que no llegó a existir. --%>
                        <c:set var="vBanco" value="${not empty param.banco ? param.banco : movimiento.cuenta.entidadFinanciera.idEntidadFinanciera}" />
                        <c:set var="vCuenta" value="${not empty param.idCuenta ? param.idCuenta : movimiento.cuenta.idCuenta}" />
                        <c:set var="vTipoCambio" value="${not empty param.tipoCambio ? param.tipoCambio : movimiento.tipoCambio}" />
                        <c:set var="vComprobante" value="${not empty param.comprobante ? param.comprobante : movimiento.numeroComprobante}" />
                        <c:set var="vDetalle" value="${not empty param.detalle ? param.detalle : movimiento.detalle}" />
                        <c:set var="vImporte" value="${not empty param.importe ? param.importe : movimiento.monto}" />
                        <c:set var="vFecha"><c:choose><c:when test="${not empty param.fecha}">${param.fecha}</c:when><c:otherwise><fmt:formatDate value="${movimiento.fecha}" pattern="yyyy-MM-dd"/></c:otherwise></c:choose></c:set>

                        <!-- Botones principales -->
                        <div class="row mb-3">
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="${ruta}?menu=MovimientoBancario&accion=Nuevo" class="btn btn-success">Nuevo</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-success" disabled title="No tiene permisos">Nuevo</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-auto">
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalBuscarMovimiento">Buscar ${etiqueta}</button>
                            </div>
                            <div class="col-auto">
                                <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular"
                                        <c:if test="${empty movimiento or anulado or not puedeBorrar}">disabled
                                            title="${anulado ? 'El movimiento ya está anulado' : (empty movimiento ? 'Cargue un movimiento para anularlo' : 'No tiene permisos')}"</c:if>>Anular</button>
                            </div>
                        </div>

                        <form id="formPrincipal" method="post" action="${ruta}">
                            <input type="hidden" name="menu" value="MovimientoBancario">
                            <input type="hidden" name="tipo" value="${tipo}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Generar">
                            <input type="hidden" name="id" value="${movimiento.id}">

                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <!-- Fila 1 -->
                                    <div class="row mb-3">
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="numero" type="text" placeholder="${etiqueta} Nro" readonly
                                                       value="${movimiento.id}" />
                                                <label for="numero">${etiqueta} Nro</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="banco" name="banco" onchange="filtrarCuentas();"
                                                        <c:if test="${not esNuevo}">disabled</c:if>>
                                                    <option value="">Todos</option>
                                                    <c:forEach var="ent" items="${listaEntidades}">
                                                        <option value="${ent.idEntidadFinanciera}"
                                                                <c:if test="${vBanco == ent.idEntidadFinanciera}">selected</c:if>>${ent.nombre}</option>
                                                    </c:forEach>
                                                </select>
                                                <label for="banco">Banco</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="cuenta" name="idCuenta" onchange="mostrarMoneda();"
                                                        <c:if test="${not esNuevo}">disabled</c:if>>
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="cta" items="${listaCuentas}">
                                                        <option value="${cta.idCuenta}"
                                                                data-banco="${cta.entidadFinanciera.idEntidadFinanciera}"
                                                                data-moneda="${cta.moneda.descripcion}"
                                                                <c:if test="${vCuenta == cta.idCuenta}">selected</c:if>>${cta.numero}</option>
                                                    </c:forEach>
                                                </select>
                                                <label for="cuenta">Nro de cuenta</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="moneda" type="text" placeholder="Moneda" readonly
                                                       value="${movimiento.cuenta.moneda.descripcion}" />
                                                <label for="moneda">Moneda</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="tipoCambio" name="tipoCambio" type="text" inputmode="decimal"
                                                       placeholder="Tipo de cambio" value="${vTipoCambio}"
                                                       <c:if test="${not esNuevo}">readonly</c:if> />
                                                <label for="tipoCambio">Tipo de cambio</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="estado" type="text" placeholder="Estado" readonly
                                                       value="${movimiento.estado}" />
                                                <label for="estado">Estado</label>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- Fila 2 -->
                                    <div class="row mb-3">
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="comprobante" name="comprobante" type="text" inputmode="numeric"
                                                       placeholder="Comprobante N°" value="${vComprobante}"
                                                       <c:if test="${not esNuevo}">readonly</c:if> />
                                                <label for="comprobante">Comprobante N°</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fecha" name="fecha" type="date"
                                                       placeholder="Fecha de emisión"
                                                       value="${vFecha}"
                                                       <c:if test="${not esNuevo}">readonly</c:if> />
                                                <label for="fecha">Fecha de emisión</label>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="detalle" name="detalle" type="text" maxlength="255"
                                                       placeholder="Detalle" value="${vDetalle}"
                                                       <c:if test="${not esNuevo}">readonly</c:if> />
                                                <label for="detalle">Detalle</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control mask-miles" id="importe" name="importe" type="text" inputmode="numeric"
                                                       placeholder="Importe" value="${vImporte}"
                                                       <c:if test="${not esNuevo}">readonly</c:if> />
                                                <label for="importe">Importe</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Botones de cierre -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="col-auto d-inline-block me-2">
                                        <c:choose>
                                            <c:when test="${puedeInsertar}">
                                                <button type="button" class="btn btn-success" onclick="generarMovimiento();"
                                                        <c:if test="${not esNuevo}">disabled title="Presione Nuevo para cargar un movimiento"</c:if>>Generar</button>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" class="btn btn-success" disabled title="No tiene permisos">Generar</button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="col-auto d-inline-block">
                                        <a href="${ruta}?menu=MovimientoBancario&accion=Cancelar" class="btn btn-danger">Cancelar</a>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar movimiento -->
                        <div class="modal fade" id="modalBuscarMovimiento" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-xl">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar ${etiqueta}</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaMovimientos" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-center">Nro</th>
                                                    <th class="text-center">Fecha</th>
                                                    <th class="text-center">Cuenta</th>
                                                    <th class="text-center">Comprobante</th>
                                                    <th class="text-center">Detalle</th>
                                                    <th class="text-center">Importe</th>
                                                    <th class="text-center">Estado</th>
                                                    <th class="text-center">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="mov" items="${listaMovimientos}">
                                                    <tr>
                                                        <td class="text-center">${mov.id}</td>
                                                        <td class="text-center"><fmt:formatDate value="${mov.fecha}" pattern="dd/MM/yyyy"/></td>
                                                        <td>${mov.cuenta.entidadFinanciera.nombre} - ${mov.cuenta.numero}</td>
                                                        <td class="text-center">${mov.numeroComprobante}</td>
                                                        <td>${mov.detalle}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${mov.monto}" pattern="#,##0"/></td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${mov.estado eq 'Anulado'}"><span class="badge bg-danger">Anulado</span></c:when>
                                                                <c:otherwise><span class="badge bg-success">Vigente</span></c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-center">
                                                            <a href="${ruta}?menu=MovimientoBancario&accion=Cargar&id=${mov.id}"
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

                        <!-- Modal Confirmar Anular -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body">
                                        <p>¿Está seguro que desea anular este movimiento?</p>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularMovimiento();">Sí, Anular</button>
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
            function limpiarMascaras(form) {
                $(form).find('.mask-miles').each(function () {
                    $(this).val($(this).cleanVal());
                });
            }
            // El combo de banco sólo filtra: lo que se guarda es la cuenta.
            function filtrarCuentas() {
                var banco = document.getElementById('banco').value;
                var cuentas = document.getElementById('cuenta');
                for (var i = 0; i < cuentas.options.length; i++) {
                    var op = cuentas.options[i];
                    if (!op.value) {
                        continue;
                    }
                    var visible = (banco === '' || op.getAttribute('data-banco') === banco);
                    op.hidden = !visible;
                    op.disabled = !visible;
                    if (!visible && op.selected) {
                        cuentas.value = '';
                    }
                }
                mostrarMoneda();
            }
            // La moneda no se carga: sale de la cuenta elegida, igual que en la orden de pago.
            function mostrarMoneda() {
                var cuentas = document.getElementById('cuenta');
                var op = cuentas.options[cuentas.selectedIndex];
                document.getElementById('moneda').value = (op && op.value) ? (op.getAttribute('data-moneda') || '') : '';
            }
            function generarMovimiento() {
                limpiarMascaras(document.getElementById('formPrincipal'));
                setAccion('Generar');
                document.getElementById('formPrincipal').submit();
            }
            function anularMovimiento() {
                setAccion('Anular');
                document.getElementById('formPrincipal').submit();
            }

            $(document).ready(function () {
                $('.mask-miles').mask('#.##0', {reverse: true});
                filtrarCuentas();
                $('#tablaMovimientos').DataTable({language: {url: "DataTables 2/es-ES.json"}});
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
