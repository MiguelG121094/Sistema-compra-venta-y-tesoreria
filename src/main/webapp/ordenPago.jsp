<%--
    Document   : ordenPago
    Orden de Pago (Tesorería), cableada a OrdenPagoServlet.
    Patrón formulario único + JS (Session+Token), calcado de provision.jsp / facturaCompra.jsp.

    Atributos de request esperados del servlet:
      token (String), esNuevo (boolean), puedeInsertar/puedeBorrar (boolean)
      ordenPago (OrdenPago): numero, numeroRecibo, fechaEmision, estado, tipoCambio,
                             idProvisionCtaPagar, tipoPago, monto, proveedor.razonSocial
      sucursalSeleccionada (Sucursal), idMonedaSeleccionada (Long)
      listaDetalle (List<OrdenPagoDetalle>)  -> facturas de la provisión (SOLO LECTURA)
      listaFormasPago (List<FormaPagoDetalle>) -> carrito de formas de pago
      totalOrden (Long) = ord_pag_monto (Σ importe a pagar del detalle)
      sumaFormas (Long) = Σ montos de las formas cargadas
      listaSucursales, listaMonedas, listaCuentas, listaFormaPago (Cheque/Transferencia),
      listaChequeras, listaTipoCheque
      listaOrdenesPago (modal), listaProvisiones (modal)
      formaEnEditor (FormaPagoDetalle, opcional) para precargar el editor
    Acciones (accionPrincipal): Nuevo, CargarOrdenPago, CargarProvision, CambiarTipoPago,
      AgregarForma, EliminarForma, Generar, Anular, Cancelar
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
        <title>Orden de Pago</title>
        <style>
            .custom-card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; margin-bottom: 16px; }
            .border-section { border-top: 2px solid #dee2e6; margin: 16px 0; padding-top: 16px; }
            .custom-table { width: 100%; border-collapse: collapse; }
            .custom-table th, .custom-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            .custom-table th { background-color: #e9ecef; font-weight: bold; }
            .section-title { background-color: #e9ecef; padding: 8px 12px; margin: 0 0 12px 0; border-radius: 4px; font-weight: bold; }
            .btn-responsive { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
            .info-icon { cursor: help; color: #0d6efd; }
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
                                <h1 style="text-align: center"><strong>ORDEN DE PAGO</strong></h1>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 15px 0;"></div>
                        </div>

                        <!-- Botones principales -->
                        <div class="row mb-3">
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="OrdenPagoServlet?menu=OrdenPago&accion=Nuevo" class="btn btn-success">Nuevo</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-success" disabled title="No tiene permisos">Nuevo</button>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalBuscarOrdenPago">Buscar Orden de pago</button>
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalBuscarProvision"
                                        <c:if test="${empty token or not esNuevo}">disabled</c:if>>Buscar Provisión</button>
                                <c:choose>
                                    <c:when test="${not empty token and not esNuevo and ordenPago.estado ne 'Anulado' and puedeBorrar}">
                                        <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular">Anular</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-danger" disabled>Anular</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Formulario principal -->
                        <form id="formPrincipal" method="post" action="OrdenPagoServlet">
                            <input type="hidden" name="menu" value="OrdenPago">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Generar">
                            <input type="hidden" name="index" id="indexForma" value="">

                            <!-- Cabecera -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <h3>Cabecera</h3>
                                    <!-- Fila 1 -->
                                    <div class="row mb-3">
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="nroOP" type="text" placeholder="Nro de OP" readonly
                                                       value="${ordenPago.numero}" />
                                                <label for="nroOP">Nro de OP</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="recibo" name="recibo" type="number" min="0" placeholder="Recibo Nro"
                                                       value="${ordenPago.numeroRecibo}" <c:if test="${empty token or not esNuevo}">readonly</c:if> />
                                                <label for="recibo">Recibo Nro</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fecha" type="text" placeholder="Fecha" readonly
                                                       value="<fmt:formatDate value='${ordenPago.fechaEmision}' pattern='dd/MM/yyyy'/>" />
                                                <label for="fecha">Fecha</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="estado" type="text" placeholder="Estado" readonly
                                                       value="${empty ordenPago.estado ? (not empty token ? 'Pendiente' : '') : ordenPago.estado}" />
                                                <label for="estado">Estado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="sucursal" name="idSucursal"
                                                        <c:if test="${empty token or not esNuevo}">disabled</c:if>>
                                                    <option value="">Seleccionar Sucursal</option>
                                                    <c:forEach var="suc" items="${listaSucursales}">
                                                        <option value="${suc.idSucursal}"
                                                            <c:if test="${sucursalSeleccionada.idSucursal == suc.idSucursal}">selected</c:if>>${suc.descripcion}</option>
                                                    </c:forEach>
                                                </select>
                                                <label for="sucursal">Sucursal</label>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- Fila 2 -->
                                    <div class="row mb-3">
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="moneda" name="idMoneda"
                                                        <c:if test="${empty token or not esNuevo}">disabled</c:if>>
                                                    <option value="">Seleccionar Moneda</option>
                                                    <c:forEach var="mon" items="${listaMonedas}">
                                                        <option value="${mon.idMoneda}"
                                                            <c:if test="${idMonedaSeleccionada == mon.idMoneda}">selected</c:if>>${mon.descripcion}</option>
                                                    </c:forEach>
                                                </select>
                                                <label for="moneda">Moneda</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="tipoCambio" name="tipoCambio" type="text" inputmode="decimal"
                                                       placeholder="Tipo de cambio" value="${ordenPago.tipoCambio}"
                                                       <c:if test="${empty token or not esNuevo}">readonly</c:if> />
                                                <label for="tipoCambio">Tipo de cambio</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="provisionNro" type="text" placeholder="Provisión Nro" readonly
                                                       value="${ordenPago.idProvisionCtaPagar}" />
                                                <label for="provisionNro">Provisión Nro</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="tipoPago" name="tipoPago" onchange="cambiarTipoPago();"
                                                        <c:if test="${empty token or not esNuevo}">disabled</c:if>>
                                                    <option value="">Seleccionar...</option>
                                                    <option value="reposicionFF" <c:if test="${ordenPago.tipoPago == 'reposicionFF'}">selected</c:if>>Reposición Fondo Fijo</option>
                                                    <option value="otrosGastos" <c:if test="${ordenPago.tipoPago == 'otrosGastos'}">selected</c:if>>Otros gastos</option>
                                                </select>
                                                <label for="tipoPago">
                                                    Tipo de pago
                                                    <span class="info-icon" tabindex="0" data-bs-toggle="tooltip" data-bs-placement="top"
                                                          title="Indica el motivo de la OP: 'Reposición Fondo Fijo' repone la caja chica rendida; 'Otros gastos' es un pago normal de facturas provisionadas.">&#9432;</span>
                                                </label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="razonSocial" type="text" placeholder="Razón Social" readonly
                                                       value="${ordenPago.proveedor.razonSocial}" />
                                                <label for="razonSocial">Razón Social</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Detalle de orden de pago (facturas de la provisión, SOLO LECTURA) -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <p class="section-title">Detalle de orden de pago</p>
                                    <div class="table-responsive">
                                        <table id="tablaDetalleOP" class="table table-bordered table-sm custom-table">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Item</th>
                                                    <th class="text-bg-dark text-center">Nro. de factura</th>
                                                    <th class="text-bg-dark text-center">Importe total</th>
                                                    <th class="text-bg-dark text-center">Saldo pendiente</th>
                                                    <th class="text-bg-dark text-center">Importe a pagar</th>
                                                    <th class="text-bg-dark text-center">Plazo de pago</th>
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
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <small class="text-muted">Las facturas y sus importes vienen fijos de la provisión (solo lectura).</small>
                                </div>
                            </div>

                            <!-- FORMAS DE PAGO (carrito) -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <p class="section-title">Formas de Pago</p>

                                    <!-- Editor de forma (solo al crear, con provisión cargada) -->
                                    <c:if test="${not empty token and esNuevo and not empty listaDetalle}">
                                        <div class="row g-2 align-items-end mb-3">
                                            <div class="col-md-2">
                                                <label class="form-label mb-1">Tipo</label>
                                                <select class="form-control" id="tipoForma" name="idFormaPagoCab" onchange="toggleCamposCheque();">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="fp" items="${listaFormaPago}">
                                                        <option value="${fp.idFormaPagoCabecera}" data-desc="${fp.descripcion}"
                                                            <c:if test="${formaEnEditor.formaPagoCabecera.idFormaPagoCabecera == fp.idFormaPagoCabecera}">selected</c:if>>${fp.descripcion}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-3">
                                                <label class="form-label mb-1">Cuenta bancaria</label>
                                                <select class="form-control" id="cuentaForma" name="idCuenta">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="cta" items="${listaCuentas}">
                                                        <option value="${cta.idCuenta}"
                                                            <c:if test="${formaEnEditor.cuenta.idCuenta == cta.idCuenta}">selected</c:if>>
                                                            ${cta.entidadFinanciera.nombre} - ${cta.numero} (${cta.moneda.descripcion})</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label mb-1">Monto</label>
                                                <input type="text" inputmode="numeric" class="form-control mask-miles" id="montoForma" name="montoForma"
                                                       placeholder="Monto" value="${formaEnEditor.monto}">
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label mb-1">Referencia</label>
                                                <input type="text" class="form-control" id="referenciaForma" name="referenciaForma"
                                                       placeholder="Referencia" value="${formaEnEditor.referencia}">
                                            </div>
                                            <div class="col-md-3">
                                                <button type="button" class="btn btn-success w-100" onclick="agregarForma();">Agregar forma</button>
                                            </div>
                                        </div>

                                        <!-- Campos solo para CHEQUE -->
                                        <div class="row g-2 align-items-end mb-2" id="camposCheque" style="display:none;">
                                            <div class="col-md-3">
                                                <label class="form-label mb-1">Chequera</label>
                                                <select class="form-control" id="chequeraForma" name="idChequera">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="chq" items="${listaChequeras}">
                                                        <option value="${chq.idChequera}"
                                                            <c:if test="${formaEnEditor.cheque.chequera.idChequera == chq.idChequera}">selected</c:if>>
                                                            ${chq.cuenta.entidadFinanciera.nombre} - Serie ${chq.serie} (${chq.desdeNumero}-${chq.hastaNumero})</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label mb-1">Tipo de cheque</label>
                                                <select class="form-control" id="tipoChequeForma" name="idTipoCheque">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="tc" items="${listaTipoCheque}">
                                                        <option value="${tc.idTipoCheque}"
                                                            <c:if test="${formaEnEditor.cheque.tipoCheque.idTipoCheque == tc.idTipoCheque}">selected</c:if>>${tc.descripcion}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label mb-1">Fecha de pago</label>
                                                <input type="date" class="form-control" id="fechaPagoCheque" name="fechaPagoCheque"
                                                       value="<fmt:formatDate value='${formaEnEditor.cheque.fechaPago}' pattern='yyyy-MM-dd'/>">
                                            </div>
                                            <div class="col-md-2">
                                                <label class="form-label mb-1">Fecha de vencimiento</label>
                                                <input type="date" class="form-control" id="fechaVenciCheque" name="fechaVenciCheque"
                                                       value="<fmt:formatDate value='${formaEnEditor.cheque.fechaVencimiento}' pattern='yyyy-MM-dd'/>">
                                            </div>
                                            <div class="col-md-3">
                                                <small class="text-muted">El N° de cheque se asigna automáticamente del rango de la chequera al Generar.</small>
                                            </div>
                                        </div>
                                        <small class="text-muted">Solo se admite Cheque o Transferencia (sin efectivo). La suma de las formas debe igualar el Importe Total a Pagar.</small>
                                    </c:if>

                                    <!-- Tabla de formas cargadas -->
                                    <div class="table-responsive mt-2">
                                        <table id="tablaFormasPago" class="table table-bordered table-sm custom-table">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Item</th>
                                                    <th class="text-bg-dark text-center">Tipo</th>
                                                    <th class="text-bg-dark text-center">Cuenta bancaria</th>
                                                    <th class="text-bg-dark text-center">Monto</th>
                                                    <th class="text-bg-dark text-center">Instrumento / Referencia</th>
                                                    <c:if test="${not empty token and esNuevo}">
                                                        <th class="text-bg-dark text-center no-search">Acción</th>
                                                    </c:if>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="fp" items="${listaFormasPago}" varStatus="st">
                                                    <tr>
                                                        <td class="text-center">${st.index + 1}</td>
                                                        <td class="text-center">${fp.formaPagoCabecera.descripcion}</td>
                                                        <td class="text-center">${fp.cuenta.entidadFinanciera.nombre} - ${fp.cuenta.numero}</td>
                                                        <td class="text-end"><fmt:formatNumber value="${fp.monto}" pattern="#,##0"/></td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${not empty fp.cheque}">
                                                                    <c:choose>
                                                                        <c:when test="${not empty fp.cheque.numero}">Cheque N° ${fp.cheque.numero}</c:when>
                                                                        <c:otherwise>Cheque (N° al generar)</c:otherwise>
                                                                    </c:choose>
                                                                </c:when>
                                                                <c:otherwise>${fp.referencia}</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <c:if test="${not empty token and esNuevo}">
                                                            <td class="text-center">
                                                                <button type="button" class="btn btn-danger btn-sm" data-bs-toggle="modal" data-bs-target="#modalEliminarForma"
                                                                        onclick="document.getElementById('indexForma').value=${st.index};">Eliminar</button>
                                                            </td>
                                                        </c:if>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>

                                    <!-- Botones finales + total -->
                                    <div class="row mt-3">
                                        <div class="col-md-6">
                                            <c:if test="${not empty token and esNuevo and puedeInsertar}">
                                                <button type="button" class="btn btn-success" onclick="generarOrdenPago();">Generar</button>
                                            </c:if>
                                            <c:if test="${not empty token}">
                                                <a href="OrdenPagoServlet?menu=OrdenPago&accion=Cancelar&token=${token}" class="btn btn-danger">Cancelar</a>
                                            </c:if>
                                        </div>
                                        <div class="col-md-6 text-end">
                                            <div class="form-floating mb-1" style="max-width: 320px; margin-left: auto;">
                                                <input class="form-control text-end" id="importeTotal" type="text" readonly
                                                       value="<fmt:formatNumber value='${totalOrden}' pattern='#,##0'/>" />
                                                <label for="importeTotal">Importe Total a Pagar</label>
                                            </div>
                                            <small class="text-muted">Suma de formas cargadas:
                                                <strong><fmt:formatNumber value="${sumaFormas}" pattern="#,##0"/></strong>
                                                <c:if test="${not empty token and esNuevo}">
                                                    <c:choose>
                                                        <c:when test="${sumaFormas == totalOrden and totalOrden > 0}"><span class="text-success">&#10004; coincide</span></c:when>
                                                        <c:otherwise><span class="text-danger">&#33; debe igualar el total</span></c:otherwise>
                                                    </c:choose>
                                                </c:if>
                                            </small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar Orden de pago -->
                        <div class="modal fade" id="modalBuscarOrdenPago" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar Orden de Pago</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaOrdenesPago" class="table table-bordered table-striped">
                                            <thead><tr>
                                                <th class="text-bg-dark text-center">N°</th>
                                                <th class="text-bg-dark text-center">Proveedor</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center">Monto</th>
                                                <th class="text-bg-dark text-center">Estado</th>
                                                <th class="text-bg-dark text-center no-search">Acción</th>
                                            </tr></thead>
                                            <tbody>
                                                <c:forEach var="op" items="${listaOrdenesPago}">
                                                    <tr class="${op.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                        <td class="text-center">${op.idOrdenPago}</td>
                                                        <td>${op.proveedor.razonSocial}</td>
                                                        <td class="text-center"><fmt:formatDate value="${op.fechaEmision}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${op.monto}" pattern="#,##0"/></td>
                                                        <td class="text-center">${op.estado}</td>
                                                        <td class="text-center">
                                                            <a href="OrdenPagoServlet?menu=OrdenPago&accion=CargarOrdenPago&idOrdenPago=${op.idOrdenPago}"
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

                        <!-- Modal Buscar Provisión -->
                        <div class="modal fade" id="modalBuscarProvision" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar Provisión (Pendiente)</h5>
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
                                                    <tr>
                                                        <td class="text-center">${p.idProvisionCuentaPagar}</td>
                                                        <td>${p.proveedor.razonSocial}</td>
                                                        <td class="text-center"><fmt:formatDate value="${p.fecha}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center">${p.estado}</td>
                                                        <td class="text-center">
                                                            <a href="OrdenPagoServlet?menu=OrdenPago&accion=CargarProvision&idProvision=${p.idProvisionCuentaPagar}&token=${token}"
                                                               class="btn btn-primary btn-sm">Seleccionar</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                        <small class="text-muted">Solo se listan provisiones en estado "Pendiente" (aún no pagadas).</small>
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
                                        <p>¿Está seguro que desea anular esta orden de pago?</p>
                                        <p class="text-muted small">Se devolverá el saldo de las facturas, se anularán los cheques emitidos y la provisión volverá a estar disponible.</p>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularOrdenPago();">Sí, Anular</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Eliminar forma -->
                        <div class="modal fade" id="modalEliminarForma" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea eliminar esta forma de pago?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="confirmarEliminarForma();">Sí, Eliminar</button>
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
            // Muestra/oculta los campos de cheque según el tipo de forma seleccionado
            function toggleCamposCheque() {
                var sel = document.getElementById('tipoForma');
                var campos = document.getElementById('camposCheque');
                if (!sel || !campos) return;
                var desc = sel.options[sel.selectedIndex] ? (sel.options[sel.selectedIndex].getAttribute('data-desc') || '') : '';
                campos.style.display = (desc.toLowerCase() === 'cheque') ? 'flex' : 'none';
            }
            function cambiarTipoPago() {
                setAccion('CambiarTipoPago');
                document.getElementById('formPrincipal').submit();
            }
            function agregarForma() {
                limpiarMascaras(document.getElementById('formPrincipal'));
                setAccion('AgregarForma');
                document.getElementById('formPrincipal').submit();
            }
            function confirmarEliminarForma() {
                setAccion('EliminarForma');
                document.getElementById('formPrincipal').submit();
            }
            function generarOrdenPago() {
                limpiarMascaras(document.getElementById('formPrincipal'));
                setAccion('Generar');
                document.getElementById('formPrincipal').submit();
            }
            function anularOrdenPago() {
                setAccion('Anular');
                document.getElementById('formPrincipal').submit();
            }

            $(document).ready(function () {
                $('.mask-miles').mask('#.##0', { reverse: true });
                toggleCamposCheque();
                // Tooltips de Bootstrap
                var tips = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                tips.forEach(function (el) { new bootstrap.Tooltip(el); });
                $('#tablaDetalleOP').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaFormasPago').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaOrdenesPago').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaProvisiones').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
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
