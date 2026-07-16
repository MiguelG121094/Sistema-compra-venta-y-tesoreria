<%--
    Document   : notaCreditoDebito
    Vista de Nota de Crédito / Débito de Compra, cableada a NotaCreditoDebitoServlet.
    Diseño original + funcionalidad (patrón formulario único + JS, Session+Token).
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
            .custom-card {
                border: 1px solid #ddd;
                border-radius: 8px;
                padding: 16px;
                margin-bottom: 16px;
            }
            .custom-table {
                width: 100%;
                border-collapse: collapse;
            }
            .custom-table th, .custom-table td {
                border: 1px solid #ddd;
                padding: 8px;
                text-align: left;
            }
            .custom-table th {
                background-color: #e9ecef;
                font-weight: bold;
            }
            .section-title {
                background-color: #e9ecef;
                padding: 8px 12px;
                margin: 0;
                border-radius: 4px;
                font-weight: bold;
            }
            .form-group-compact {
                margin-bottom: 8px;
            }
            .border-section {
                border-top: 2px solid #dee2e6;
                margin: 16px 0;
                padding-top: 16px;
            }
            .btn-responsive {
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
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
                        <!-- Título y botones -->
                        <div class="row mb-4">
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <span style="height: 100%; width: 100%;">
                                    <h1 style="text-align: center">
                                        <strong>NOTA DE CRÉDITO Y DÉBITO</strong></h1></span>
                            </div>

                            <!-- línea debajo del titulo -->
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                            <!-- Botones principales -->
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=Nuevo&tipoNota=credito"
                                           class="btn btn-success">Nuevo</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-success" disabled title="No tiene permisos">Nuevo</button>
                                    </c:otherwise>
                                </c:choose>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalBuscarNota"
                                        class="btn btn-info text-white">Buscar Nota de Crédito o Débito</button>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalBuscarFactura"
                                        class="btn btn-info text-white"
                                        <c:if test="${empty token or not esNuevo or not puedeInsertar}">disabled</c:if>>Buscar Factura Compra</button>
                                <c:choose>
                                    <c:when test="${not empty idNotaExistente and nota.estado ne 'Anulado' and puedeBorrar}">
                                        <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular">Anular</button>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-danger" disabled>Anular</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Formulario principal -->
                        <form id="formPrincipal" method="post" action="NotaCreditoDebitoServlet">
                            <input type="hidden" name="menu" value="NotaCreditoDebito">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Guardar">
                            <input type="hidden" name="index" id="indexArticulo" value="${indexSeleccionado}">
                            <input type="hidden" name="idFactura" id="idFacturaHidden" value="">
                            <input type="hidden" name="idArticulo" id="idArticuloHidden" value="${detalleSeleccionado.articulo.idArticulo}">
                            <input type="hidden" name="idDeposito" id="idDepositoHidden" value="${detalleSeleccionado.deposito.idDeposito}">

                            <!-- Cabecera -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <h3>Cabecera</h3>

                                    <!-- Datos básicos -->
                                    <div class="card-body">
                                        <div class="card-body">
                                            <div class="row mb-3">
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="usuario" type="text" placeholder="Usuario"
                                                               value="<%= usuario.getUsername() %>" readonly />
                                                        <label for="usuario">Usuario</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="fecha" type="text" placeholder="Fecha"
                                                               value="<fmt:formatDate value='${nota.fechaCarga}' pattern='dd/MM/yyyy'/>" readonly />
                                                        <label for="fecha">Fecha</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="estado" type="text" placeholder="Estado"
                                                               value="${empty nota.estado ? 'Pendiente' : nota.estado}" readonly />
                                                        <label for="estado">Estado</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <select class="form-control" id="sucursal" disabled>
                                                            <option>${sucursalHeredada.descripcion}</option>
                                                        </select>
                                                        <label for="sucursal">Sucursal</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="idFactura" type="text" placeholder="ID Factura"
                                                               value="${facturaReferenciada.numero}" readonly />
                                                        <label for="idFactura">ID Factura N°</label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Información del Proveedor / Factura asociada -->
                                    <div class="card-body">
                                        <div class="card-body">
                                            <div class="row mb-3">
                                                <div class="col-md-3">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="razonSocial" type="text" placeholder="Razón Social"
                                                               value="${proveedorSeleccionado.razonSocial}" readonly />
                                                        <label for="razonSocial">Razón Social</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="ruc" type="text" placeholder="RUC"
                                                               value="${proveedorSeleccionado.ruc}" readonly />
                                                        <label for="ruc">RUC</label>
                                                    </div>
                                                </div>
                                                <style>
                                                    .form-floating #comprobanteN::placeholder { opacity: 0; }
                                                    .form-floating #comprobanteN:focus::placeholder,
                                                    .form-floating #comprobanteN:not(:placeholder-shown)::placeholder {
                                                        opacity: 0.8; color: #6c757d;
                                                    }
                                                </style>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="comprobanteN" name="numeroComprobante" type="text"
                                                               placeholder="000-000-0000000" value="${nota.numero}"
                                                               <c:if test="${empty token}">disabled</c:if> />
                                                        <label for="comprobanteN">Comprobante N°</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="fechaEmision" name="fechaEmision" type="date" placeholder="Fecha de emisión"
                                                               value="<fmt:formatDate value='${nota.fechaEmision}' pattern='yyyy-MM-dd'/>"
                                                               <c:if test="${empty token}">disabled</c:if> />
                                                        <label for="fechaEmision">Fecha de emisión</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="motivo" name="motivo" type="text" placeholder="Motivo"
                                                               value="${nota.motivo}" <c:if test="${empty token}">disabled</c:if> />
                                                        <label for="motivo">Motivo</label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Timbrado, condición y tipo de nota -->
                                    <div class="card-body">
                                        <div class="card-body">
                                            <div class="row mb-3">
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="timbrado" name="timbrado" type="number" placeholder="Timbrado"
                                                               min="0" max="99999999" value="${nota.timbrado}"
                                                               oninput="if(this.value.length>8)this.value=this.value.slice(0,8)"
                                                               <c:if test="${empty token}">disabled</c:if> />
                                                        <label for="timbrado">Timbrado</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="fechaVencimiento" name="fechaVencTimbrado" type="date" placeholder="Fecha de vencimiento"
                                                               value="<fmt:formatDate value='${nota.fechaVenciTimbrado}' pattern='yyyy-MM-dd'/>"
                                                               <c:if test="${empty token}">disabled</c:if> />
                                                        <label for="fechaVencimiento">Fecha de vencimiento</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <select class="form-control" id="condicionCompra" disabled>
                                                            <option>${condicionHeredada}</option>
                                                        </select>
                                                        <label for="condicionCompra" class="me-2">Condición de compra</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <select class="form-control" id="tipoNota" name="tipoNota" onchange="cambiarTipoNota();"
                                                                <c:if test="${empty token or not esNuevo}">disabled</c:if>>
                                                            <option value="">Seleccionar tipo de nota</option>
                                                            <option value="credito" ${tipoNota eq 'credito' ? 'selected' : ''}>Nota de Crédito</option>
                                                            <option value="debito" ${tipoNota eq 'debito' ? 'selected' : ''}>Nota de Débito</option>
                                                        </select>
                                                        <label for="tipoNota" class="me-2">Tipo de nota</label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Línea separadora -->
                            <div class="border-section"></div>

                            <%-- Botón Buscar Artículo comentado por ahora — la NC/ND no agrega artículos de catálogo,
                                 modifica los que vienen de la factura o agrega líneas financieras. --%>

                            <!-- Modificación / agregado de líneas -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="row" style="margin-top: 10px">
                                        <div class="col-md-3">
                                            <input type="text" name="descripcion" placeholder="Descripción" class="form-control"
                                                   value="${detalleSeleccionado.descripcion}">
                                        </div>
                                        <div class="col-md-1">
                                            <input type="text" inputmode="numeric" name="cantidad" placeholder="Cantidad" class="form-control mask-miles"
                                                   value="${not empty detalleSeleccionado ? detalleSeleccionado.cantidad : 1}">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="text" inputmode="numeric" name="monto" placeholder="Precio de compra" class="form-control mask-miles"
                                                   value="${detalleSeleccionado.monto}">
                                        </div>
                                        <div class="col-md-2">
                                            <select name="idTipoImpuesto" class="form-control">
                                                <option value="">Seleccionar Impuesto</option>
                                                <c:forEach var="imp" items="${listaTipoImpuesto}">
                                                    <option value="${imp.idTipoImpuesto}"
                                                        ${detalleSeleccionado.tipoImpuesto.idTipoImpuesto == imp.idTipoImpuesto ? 'selected' : ''}>
                                                        ${imp.descripcion}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-2">
                                            <c:choose>
                                                <c:when test="${not empty detalleSeleccionado}">
                                                    <button type="button" class="btn btn-warning" onclick="actualizarLinea();"
                                                            <c:if test="${empty token or not esNuevo or not puedeInsertar}">disabled</c:if>>Modificar</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-warning" onclick="agregarLinea();"
                                                            <c:if test="${empty token or not esNuevo or not puedeInsertar}">disabled</c:if>>Agregar</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Tabla de Artículos (Detalle de la nota) -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="table-responsive">
                                        <table id="tablaArticulosNota" class="table table-bordered table-sm custom-table">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id. Artículo</th>
                                                    <th class="text-bg-dark text-center">Descripción</th>
                                                    <th class="text-bg-dark text-center">Cantidad</th>
                                                    <th class="text-bg-dark text-center">Precio de compra</th>
                                                    <th class="text-bg-dark text-center">Sub. Total</th>
                                                    <th class="text-bg-dark text-center">Gravada 10%</th>
                                                    <th class="text-bg-dark text-center">IVA 10%</th>
                                                    <th class="text-bg-dark text-center">Gravada 5%</th>
                                                    <th class="text-bg-dark text-center">IVA 5%</th>
                                                    <th class="text-bg-dark text-center">Exenta</th>
                                                    <th class="text-bg-dark text-center no-search">Acciones</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="detalle" items="${listaDetalle}" varStatus="st">
                                                    <tr>
                                                        <td class="text-center">${detalle.articulo.idArticulo}</td>
                                                        <td>${detalle.descripcionDisplay}</td>
                                                        <td class="text-center"><fmt:formatNumber value="${detalle.cantidad}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.monto}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.subtotal}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.gravada10}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.iva10}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.gravada5}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.iva5}" pattern="#,##0"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${detalle.exenta}" pattern="#,##0"/></td>
                                                        <td class="text-center">
                                                            <c:if test="${not empty token and esNuevo and puedeInsertar}">
                                                                <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=EditarArticulo&token=${token}&index=${st.index}"
                                                                   class="btn btn-warning btn-sm">Editar</a>
                                                                <button type="button" class="btn btn-danger btn-sm" onclick="eliminarLinea(${st.index});">Eliminar</button>
                                                            </c:if>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty listaDetalle}">
                                                    <tr><td colspan="11" class="text-center text-muted">Sin líneas</td></tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>

                                    <!-- Botones finales -->
                                    <div class="row mt-3">
                                        <div class="col-md-6">
                                            <c:choose>
                                                <c:when test="${not empty token and esNuevo and puedeInsertar}">
                                                    <button type="button" class="btn btn-success" onclick="guardarNota();">Guardar</button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-success" disabled>Guardar</button>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${not empty token}">
                                                <a href="NotaCreditoDebitoServlet?menu=NotaCreditoDebito&accion=Cancelar&token=${token}"
                                                   class="btn btn-danger">Cancelar</a>
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

                        <!-- Modal Buscar Nota de Crédito o Débito -->
                        <div class="modal fade" id="modalBuscarNota" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Nota de Crédito o Débito</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalBuscarNota" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">N° Nota</th>
                                                        <th class="text-bg-dark text-center">Tipo</th>
                                                        <th class="text-bg-dark text-center">Proveedor</th>
                                                        <th class="text-bg-dark text-center">Estado</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="n" items="${listaNotasCredito}">
                                                        <tr class="${n.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                            <td class="text-center">${n.numero}</td>
                                                            <td class="text-center">Nota de Crédito</td>
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
                                                            <td class="text-center">Nota de Débito</td>
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
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Buscar Factura Compra -->
                        <div class="modal fade" id="modalBuscarFactura" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Factura de Compra</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalBuscarFactura" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">N° Factura</th>
                                                        <th class="text-bg-dark text-center">Estado</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
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
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal de confirmación para Anular -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white">
                                        <h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                    </div>
                                    <div class="modal-body">
                                        <p>¿Está seguro que desea anular esta nota?</p>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularNota();">Sí, Anular</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </main>
                <footer class="py-4 bg-light mt-auto">
                    <div class="container-fluid px-4">
                        <div class="d-flex align-items-center justify-content-between small">
                            <div class="text-muted">Copyright &copy; Your Website 2025</div>
                            <div>
                                <a href="#">Privacy Policy</a>
                                &middot;
                                <a href="#">Terms &amp; Conditions</a>
                            </div>
                        </div>
                    </div>
                </footer>
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
                $('#tablaModalBuscarNota').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
                $('#tablaModalBuscarFactura').DataTable({ language: { url: "DataTables 2/es-ES.json" } });
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
