<%--
    Document   : notaCreditoDebito
    Vista de Nota de Crédito / Débito de Compra, cableada a NotaCreditoDebitoServlet.
    Patrón: formulario único + JS (Session+Token). Ver NOTA_CREDITO_DEBITO_PLAN.md §7.
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
        <title>Nota de Crédito y Débito</title>
        <style>
            .custom-card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
            .custom-table { width: 100%; border-collapse: collapse; }
            .custom-table th, .custom-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            .custom-table th { background-color: #e9ecef; font-weight: bold; }
            .border-section { border-top: 2px solid #dee2e6; margin: 16px 0; padding-top: 16px; }
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
                                <h1 style="text-align: center"><strong>NOTA DE CRÉDITO Y DÉBITO</strong></h1>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 15px 0;"></div>
                        </div>

                        <!-- Botones principales -->
                        <div class="row mb-3">
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=Nuevo&tipoNota=credito"
                                           class="btn btn-success">Nueva Nota de Crédito</a>
                                        <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=Nuevo&tipoNota=debito"
                                           class="btn btn-success">Nueva Nota de Débito</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-success" disabled title="No tiene permisos">Nueva Nota de Crédito</button>
                                        <button class="btn btn-success" disabled title="No tiene permisos">Nueva Nota de Débito</button>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button" class="btn btn-info text-white"
                                        data-bs-toggle="modal" data-bs-target="#modalBuscarNota">Buscar Nota</button>
                                <c:if test="${not empty idNotaExistente and nota.estado ne 'Anulado' and puedeBorrar}">
                                    <button type="button" class="btn btn-danger"
                                            data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular">Anular</button>
                                </c:if>
                            </div>
                        </div>

                        <!-- Formulario principal -->
                        <form id="formPrincipal" method="post" action="NotaCreditoDebitoServlet">
                            <input type="hidden" name="menu" value="NotaCreditoDebito">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Guardar">
                            <input type="hidden" name="index" id="indexArticulo" value="${indexSeleccionado}">
                            <input type="hidden" name="idFactura" id="idFacturaHidden" value="">

                            <!-- Cabecera -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <h4>Cabecera</h4>
                                    <div class="row mb-3">
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="usuario" type="text"
                                                       value="<%= usuario.getUsername() %>" readonly />
                                                <label for="usuario">Usuario</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="estado" type="text"
                                                       value="${empty nota.estado ? 'Pendiente' : nota.estado}" readonly />
                                                <label for="estado">Estado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <select class="form-control" id="tipoNota" name="tipoNota"
                                                        onchange="cambiarTipoNota();"
                                                        <c:if test="${empty token or not esNuevo}">disabled</c:if>>
                                                    <option value="credito" ${tipoNota eq 'credito' ? 'selected' : ''}>Nota de Crédito</option>
                                                    <option value="debito" ${tipoNota eq 'debito' ? 'selected' : ''}>Nota de Débito</option>
                                                </select>
                                                <label for="tipoNota">Tipo de nota</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <button type="button" class="btn btn-info text-white w-100 h-100"
                                                    data-bs-toggle="modal" data-bs-target="#modalBuscarFactura"
                                                    <c:if test="${empty token or not esNuevo or not puedeInsertar}">disabled</c:if>>
                                                Buscar Factura de Compra
                                            </button>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control" id="idFactura" type="text" readonly
                                                       value="${facturaReferenciada.numero}" />
                                                <label for="idFactura">Factura referenciada</label>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="row mb-3">
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control" id="razonSocial" type="text" readonly
                                                       value="${proveedorSeleccionado.razonSocial}" />
                                                <label for="razonSocial">Razón Social</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="ruc" type="text" readonly
                                                       value="${proveedorSeleccionado.ruc}" />
                                                <label for="ruc">RUC</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control" id="sucursal" type="text" readonly
                                                       value="${sucursalHeredada.descripcion}" />
                                                <label for="sucursal">Sucursal (de la factura)</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control" id="condicion" type="text" readonly
                                                       value="${condicionHeredada}" />
                                                <label for="condicion">Condición (de la factura)</label>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="row mb-3">
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control" id="comprobanteN" name="numeroComprobante" type="text"
                                                       placeholder="000-000-0000000" value="${nota.numero}"
                                                       <c:if test="${empty token}">disabled</c:if> />
                                                <label for="comprobanteN">Comprobante N°</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="fechaEmision" name="fechaEmision" type="date"
                                                       value="<fmt:formatDate value='${nota.fechaEmision}' pattern='yyyy-MM-dd'/>"
                                                       <c:if test="${empty token}">disabled</c:if> />
                                                <label for="fechaEmision">Fecha de emisión</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="timbrado" name="timbrado" type="number"
                                                       min="0" max="99999999" value="${nota.timbrado}"
                                                       oninput="if(this.value.length>8)this.value=this.value.slice(0,8)"
                                                       <c:if test="${empty token}">disabled</c:if> />
                                                <label for="timbrado">Timbrado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="fechaVencTimbrado" name="fechaVencTimbrado" type="date"
                                                       value="<fmt:formatDate value='${nota.fechaVenciTimbrado}' pattern='yyyy-MM-dd'/>"
                                                       <c:if test="${empty token}">disabled</c:if> />
                                                <label for="fechaVencTimbrado">Venc. Timbrado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control" id="motivo" name="motivo" type="text"
                                                       value="${nota.motivo}" <c:if test="${empty token}">disabled</c:if> />
                                                <label for="motivo">Motivo</label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-12">
                                            <div class="form-floating">
                                                <input class="form-control" id="observacion" name="observacion" type="text"
                                                       value="${nota.observacion}" <c:if test="${empty token}">disabled</c:if> />
                                                <label for="observacion">Observación</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Editor de línea -->
                            <c:if test="${not empty token and esNuevo}">
                                <div class="border-section"></div>
                                <input type="hidden" name="idArticulo" id="idArticuloHidden" value="${detalleSeleccionado.articulo.idArticulo}">
                                <div class="row mb-3">
                                    <div class="col custom-card">
                                        <div class="row">
                                            <div class="col-md-3">
                                                <input type="text" name="descripcion" placeholder="Descripción" class="form-control"
                                                       value="${detalleSeleccionado.descripcion}">
                                            </div>
                                            <div class="col-md-1">
                                                <input type="text" inputmode="numeric" name="cantidad" placeholder="Cantidad"
                                                       class="form-control mask-miles"
                                                       value="${not empty detalleSeleccionado ? detalleSeleccionado.cantidad : 1}">
                                            </div>
                                            <div class="col-md-2">
                                                <input type="text" inputmode="numeric" name="monto" placeholder="Monto (unit.)"
                                                       class="form-control mask-miles" value="${detalleSeleccionado.monto}">
                                            </div>
                                            <div class="col-md-2">
                                                <select name="idTipoImpuesto" class="form-control">
                                                    <option value="">Impuesto</option>
                                                    <c:forEach var="imp" items="${listaTipoImpuesto}">
                                                        <option value="${imp.idTipoImpuesto}"
                                                            ${detalleSeleccionado.tipoImpuesto.idTipoImpuesto == imp.idTipoImpuesto ? 'selected' : ''}>
                                                            ${imp.descripcion}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <c:if test="${tipoNota eq 'credito'}">
                                                <div class="col-md-2">
                                                    <select name="idDeposito" class="form-control" title="Depósito (solo devolución de mercadería)">
                                                        <option value="">Depósito</option>
                                                        <c:forEach var="dep" items="${listaDepositos}">
                                                            <option value="${dep.idDeposito}"
                                                                ${detalleSeleccionado.deposito.idDeposito == dep.idDeposito ? 'selected' : ''}>
                                                                ${dep.descripcion}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                </div>
                                            </c:if>
                                            <div class="col-md-2">
                                                <c:choose>
                                                    <c:when test="${not empty detalleSeleccionado}">
                                                        <button type="button" class="btn btn-warning w-100" onclick="actualizarLinea();"
                                                                <c:if test="${not puedeInsertar}">disabled</c:if>>Actualizar</button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button type="button" class="btn btn-primary w-100" onclick="agregarLinea();"
                                                                <c:if test="${not puedeInsertar}">disabled</c:if>>Agregar</button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </div>
                                        <small class="text-muted">
                                            Línea con artículo + depósito = devolución (mueve stock). Sin artículo = ajuste financiero (descuento/recargo).
                                        </small>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Detalle -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="table-responsive">
                                        <table class="table table-bordered table-sm custom-table">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Artículo</th>
                                                    <th class="text-bg-dark text-center">Descripción</th>
                                                    <th class="text-bg-dark text-center">Depósito</th>
                                                    <th class="text-bg-dark text-center">Cantidad</th>
                                                    <th class="text-bg-dark text-center">Monto</th>
                                                    <th class="text-bg-dark text-center">Sub. Total</th>
                                                    <th class="text-bg-dark text-center">Grav. 10%</th>
                                                    <th class="text-bg-dark text-center">IVA 10%</th>
                                                    <th class="text-bg-dark text-center">Grav. 5%</th>
                                                    <th class="text-bg-dark text-center">IVA 5%</th>
                                                    <th class="text-bg-dark text-center">Exenta</th>
                                                    <c:if test="${not empty token and esNuevo}">
                                                        <th class="text-bg-dark text-center">Acciones</th>
                                                    </c:if>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="detalle" items="${listaDetalle}" varStatus="st">
                                                    <tr>
                                                        <td class="text-center">${detalle.articulo.idArticulo}</td>
                                                        <td>${detalle.descripcionDisplay}</td>
                                                        <td>${detalle.deposito.descripcion}</td>
                                                        <td class="text-center"><fmt:formatNumber value="${detalle.cantidad}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.monto}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.subtotal}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.gravada10}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.iva10}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.gravada5}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.iva5}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.exenta}" pattern="#,##0"/></td>
                                                        <c:if test="${not empty token and esNuevo}">
                                                            <td class="text-center">
                                                                <c:if test="${puedeInsertar}">
                                                                    <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=EditarArticulo&token=${token}&index=${st.index}"
                                                                       class="btn btn-warning btn-sm">Editar</a>
                                                                    <button type="button" class="btn btn-danger btn-sm"
                                                                            onclick="eliminarLinea(${st.index});">Eliminar</button>
                                                                </c:if>
                                                            </td>
                                                        </c:if>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty listaDetalle}">
                                                    <tr><td colspan="12" class="text-center text-muted">Sin líneas</td></tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>

                                    <div class="row mt-2">
                                        <div class="col-md-6">
                                            <c:if test="${not empty token and esNuevo}">
                                                <c:choose>
                                                    <c:when test="${puedeInsertar}">
                                                        <button type="button" class="btn btn-success" onclick="guardarNota();">Guardar</button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button class="btn btn-success" disabled title="No tiene permisos">Guardar</button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                            <c:if test="${not empty token}">
                                                <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=Cancelar&token=${token}"
                                                   class="btn btn-secondary">Cancelar</a>
                                            </c:if>
                                        </div>
                                        <div class="col-md-6 text-end">
                                            <h5>Total: <fmt:formatNumber value="${totalGeneral}" pattern="#,##0"/></h5>
                                            <small>IVA 10%: <fmt:formatNumber value="${totalIva10}" pattern="#,##0"/> |
                                                   IVA 5%: <fmt:formatNumber value="${totalIva5}" pattern="#,##0"/> |
                                                   Exenta: <fmt:formatNumber value="${totalExenta}" pattern="#,##0"/></small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar Factura -->
                        <div class="modal fade" id="modalBuscarFactura" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Factura de Compra</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaFacturas" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">N° Factura</th>
                                                    <th class="text-bg-dark text-center">Estado</th>
                                                    <th class="text-bg-dark text-center no-search">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="f" items="${listaFacturas}">
                                                    <tr class="${f.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                        <td class="text-center">${f.numero}</td>
                                                        <td class="text-center">${f.estado}</td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${f.estado eq 'Anulado'}">
                                                                    <button class="btn btn-secondary btn-sm" disabled>Anulada</button>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal"
                                                                            onclick="seleccionarFactura(${f.idFacturaCompra});">Seleccionar</button>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Buscar Nota -->
                        <div class="modal fade" id="modalBuscarNota" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Nota de Crédito o Débito</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaNotas" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">N° Nota</th>
                                                    <th class="text-bg-dark text-center">Tipo</th>
                                                    <th class="text-bg-dark text-center">Proveedor</th>
                                                    <th class="text-bg-dark text-center">Estado</th>
                                                    <th class="text-bg-dark text-center no-search">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="n" items="${listaNotasCredito}">
                                                    <tr class="${n.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                        <td class="text-center">${n.numero}</td>
                                                        <td class="text-center">Crédito</td>
                                                        <td>${n.proveedor.razonSocial}</td>
                                                        <td class="text-center">${n.estado}</td>
                                                        <td class="text-center">
                                                            <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=CargarNota&idNota=${n.idNotaCreditoCompra}&tipoNotaCargar=credito"
                                                               class="btn btn-primary btn-sm">Seleccionar</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:forEach var="n" items="${listaNotasDebito}">
                                                    <tr class="${n.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                        <td class="text-center">${n.numero}</td>
                                                        <td class="text-center">Débito</td>
                                                        <td>${n.proveedor.razonSocial}</td>
                                                        <td class="text-center">${n.estado}</td>
                                                        <td class="text-center">
                                                            <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=CargarNota&idNota=${n.idNotaDebitoCompra}&tipoNotaCargar=debito"
                                                               class="btn btn-primary btn-sm">Seleccionar</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Anular -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white">
                                        <h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body"><p>¿Está seguro que desea anular esta nota?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularNota();">Sí, Anular</button>
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

            function cambiarTipoNota() {
                setAccion('CambiarTipoNota');
                document.getElementById('formPrincipal').submit();
            }
            function seleccionarFactura(idFactura) {
                document.getElementById('idFacturaHidden').value = idFactura;
                setAccion('CargarFactura');
                document.getElementById('formPrincipal').submit();
            }
            function agregarLinea() {
                limpiarMascaras(document.getElementById('formPrincipal'));
                setAccion('AgregarLinea');
                document.getElementById('formPrincipal').submit();
            }
            function actualizarLinea() {
                limpiarMascaras(document.getElementById('formPrincipal'));
                setAccion('ActualizarArticulo');
                document.getElementById('formPrincipal').submit();
            }
            function eliminarLinea(index) {
                if (confirm('¿Eliminar esta línea?')) {
                    document.getElementById('indexArticulo').value = index;
                    setAccion('EliminarArticulo');
                    document.getElementById('formPrincipal').submit();
                }
            }
            function guardarNota() {
                limpiarMascaras(document.getElementById('formPrincipal'));
                setAccion('Guardar');
                document.getElementById('formPrincipal').submit();
            }
            function anularNota() {
                setAccion('Anular');
                document.getElementById('formPrincipal').submit();
            }
            function limpiarMascaras(form) {
                $(form).find('.mask-miles').each(function () {
                    $(this).val($(this).cleanVal());
                });
            }

            $(document).ready(function () {
                $('.mask-miles').mask('#.##0', { reverse: true });
                $('#comprobanteN').mask('000-000-0000000');
                $('#tablaFacturas').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaNotas').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
            });
        </script>

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
