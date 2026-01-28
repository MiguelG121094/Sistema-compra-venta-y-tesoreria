<%--
    Document   : facturaCompra
    Created on : 7/03/2025, 10:37:36 PM
    Author     : Miguel
    Updated    : Implementación con Session + Token pattern
--%>

<!--bloque de codigo que hace que las páginas JSP solo sean accesibles si el
usuario inicio sesion, se debe agregar esta validación en cada una de las vistas JSP-->
<%@ page import="modelo.Usuario" %>
<%
    HttpSession sessionObj = request.getSession(false);
    if (sessionObj == null || sessionObj.getAttribute("usuario") == null) {
        response.sendRedirect("login.jsp");
    }

    Usuario usuario = (Usuario) sessionObj.getAttribute("usuario");
%>

<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%--
    Taglib fn (JSTL Functions): Proporciona funciones para manipular strings.
    Funciones disponibles: fn:contains, fn:containsIgnoreCase, fn:startsWith,
    fn:endsWith, fn:indexOf, fn:length, fn:substring, fn:trim, fn:toUpperCase, etc.

    En este JSP se usa fn:contains para verificar si la descripcion del tipo de
    impuesto contiene "10" o "5" y asi calcular el IVA correspondiente.
    Ejemplo: ${fn:contains(texto, 'buscar')} retorna true/false
--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ page contentType="text/html;charset=UTF-8" language="java"  pageEncoding="UTF-8" %>
<html>
    <jsp:include page="header.jsp" />
    <head>
        <title>Factura Compra</title>
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
                                        <strong>FACTURA COMPRA</strong></h1></span>
                            </div>

                            <!--linea debajo del titulo-->
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                            <!-- Botones principales -->
                            <div class="col-auto">
                                <a href="FacturaCompraServlet?menu=FacturaCompra&accion=Nuevo" class="btn btn-success">Nuevo</a>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalPedidos"
                                        class="btn btn-info text-white"
                                        <c:if test="${empty token}">disabled</c:if>>Buscar Orden de Compra</button>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalFacturas"
                                        class="btn btn-info text-white">Buscar Factura Compra</button>
                                <c:if test="${not empty token and not esNuevo}">
                                    <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular">Anular</button>
                                </c:if>
                                <c:if test="${empty token or esNuevo}">
                                    <button class="btn btn-danger" disabled>Anular</button>
                                </c:if>
                            </div>
                        </div>

                        <!-- Formulario principal con token -->
                        <form id="formPrincipal" method="post" action="FacturaCompraServlet">
                            <input type="hidden" name="menu" value="FacturaCompra">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Guardar">

                        <!-- Cabecera -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <h3>Cabecera</h3>
                                <div class="card-body">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="usuario" type="text" placeholder="Usuario"
                                                           value="${facturaCompra.usuario.username}" readonly />
                                                    <label for="usuario">Usuario</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="fechaCarga" type="date" placeholder="Fecha"
                                                           value="<fmt:formatDate value='${facturaCompra.fechaCarga}' pattern='yyyy-MM-dd'/>" readonly />
                                                    <label for="fechaCarga">Fecha Carga</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="estado" type="text" placeholder="Estado"
                                                           value="${not empty facturaCompra.estado ? facturaCompra.estado : 'Pendiente'}" readonly />
                                                    <label for="estado">Estado</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <select class="form-control" id="sucursal" name="idSucursal"
                                                            onchange="cambiarSucursal();" <c:if test="${not esNuevo}">disabled</c:if>>
                                                        <option value="">Seleccionar Sucursal</option>
                                                        <c:forEach var="suc" items="${listaSucursales}">
                                                            <option value="${suc.idSucursal}"
                                                                <c:if test="${sucursalSeleccionada.idSucursal == suc.idSucursal}">selected</c:if>>
                                                                ${suc.descripcion}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                    <label for="sucursal">Sucursal</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="nOrdenCompra" type="text" placeholder="Orden de compra"
                                                           value="${ordenCompraSeleccionada.idOrdenCompra}" readonly />
                                                    <label for="nOrdenCompra">Orden de compra N°</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Información del Proveedor -->
                                <div class="card-body">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-1">
                                                <div class="mb-3 mb-md-0">
                                                    <button type="button" data-bs-toggle="modal" style="overflow: hidden; text-overflow: ellipsis;"
                                                            title="Buscar Proveedor"
                                                            data-bs-target="#modalProveedores" class="btn btn-outline-primary w-100 btn-responsive"
                                                            <c:if test="${not esNuevo}">disabled</c:if>>Buscar Proveedor
                                                    </button>
                                                </div>
                                            </div>
                                            <div class="col-md-3">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="razonSocial" type="text" placeholder="Razon Social"
                                                           value="${proveedorSeleccionado.razonSocial}" readonly />
                                                    <label for="razonSocial">Razon Social</label>
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
                                                .form-floating #comprobanteN::placeholder {
                                                    opacity: 0;
                                                }
                                                .form-floating #comprobanteN:focus::placeholder,
                                                .form-floating #comprobanteN:not(:placeholder-shown)::placeholder {
                                                    opacity: 0.8;
                                                    color: #6c757d;
                                                }
                                            </style>
                                            <script>
                                                $(document).ready(function(){
                                                    $('#comprobanteN').mask('000-000-0000000');
                                                });
                                            </script>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="comprobanteN" name="numeroComprobante" type="text"
                                                           placeholder="000-000-0000000" value="${facturaCompra.numero}" />
                                                    <label for="comprobanteN">Comprobante N°</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="fechaEmision" name="fechaEmision" type="date" placeholder="Fecha de emision"
                                                           value="<fmt:formatDate value='${facturaCompra.fechaEmision}' pattern='yyyy-MM-dd'/>" />
                                                    <label for="fechaEmision">Fecha de emision</label>
                                                </div>
                                            </div>
                                            <c:set var="esGasto" value="${facturaCompra.tipoFactura == 'gasto'}" />
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <select class="form-control" id="tipoFactura" name="tipoFactura" onchange="cambiarTipoFactura();">
                                                        <option value="">Seleccionar tipo de factura</option>
                                                        <option value="compraArt" <c:if test="${facturaCompra.tipoFactura == 'compraArt'}">selected</c:if>>Factura Compra de Artículos</option>
                                                        <option value="fondoFijo" <c:if test="${facturaCompra.tipoFactura == 'fondoFijo'}">selected</c:if>>Factura Fondo Fijo</option>
                                                        <option value="gasto" <c:if test="${facturaCompra.tipoFactura == 'gasto'}">selected</c:if>>Factura de Gasto</option>
                                                    </select>
                                                    <label for="tipoFactura" class="me-2">Tipo de Factura</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="card-body">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="timbrado" name="timbrado" type="text" placeholder="Timbrado"
                                                           value="${facturaCompra.timbrado}" />
                                                    <label for="timbrado">Timbrado</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="fechaVencTimb" name="fechaVencTimbrado" type="date" placeholder="Fecha Venc. Timbrado"
                                                           value="<fmt:formatDate value='${facturaCompra.fechaVenciTimbrado}' pattern='yyyy-MM-dd'/>" />
                                                    <label for="fechaVencTimb">Fecha Venc. Timbrado</label>
                                                </div>
                                            </div>
                                            <c:set var="esCredito" value="${facturaCompra.condicion == 'Credito'}" />
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <select class="form-control" id="condicionCompra" name="condicion" onchange="cambiarCondicion();">
                                                        <option value="Contado" <c:if test="${!esCredito}">selected</c:if>>Contado</option>
                                                        <option value="Credito" <c:if test="${esCredito}">selected</c:if>>Crédito</option>
                                                    </select>
                                                    <label for="condicionCompra" class="me-2">Condición de compra</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input type="number" id="plazoCredito" name="plazo" class="form-control"
                                                           value="${facturaCompra.plazo != null ? facturaCompra.plazo : 0}"
                                                           <c:if test="${!esCredito}">disabled</c:if>
                                                           placeholder="Plazo en días" title="Plazo en días">
                                                    <label for="plazoCredito">Plazo de condición (días)</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Línea separadora -->
                        <div class="border-section"></div>
                        </form>

                        <!-- Búsqueda de Artículos - para factura de gasto o fondo fijo -->
                        <c:if test="${facturaCompra.tipoFactura == 'fondoFijo' or facturaCompra.tipoFactura == 'gasto'}">
                        <form id="formAgregarGasto" method="post" action="FacturaCompraServlet">
                            <input type="hidden" name="menu" value="FacturaCompra">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" value="AgregarArticulo">
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="row" style="margin-top: 10px">
                                        <div class="col-md-3">
                                            <input type="text" name="descripcion" placeholder="Descripción" class="form-control">
                                        </div>
                                        <div class="col-md-1">
                                            <input type="number" name="cantidad" placeholder="Cantidad" class="form-control" value="1">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="number" name="precioCompra" placeholder="Precio de compra" class="form-control">
                                        </div>
                                        <div class="col-md-2">
                                            <select name="idTipoImpuesto" class="form-control">
                                                <option value="">Seleccionar Impuesto</option>
                                                <c:forEach var="imp" items="${listaTipoImpuesto}">
                                                    <option value="${imp.idTipoImpuesto}">${imp.descripcion}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-2">
                                            <button type="submit" class="btn btn-success">Agregar Artículo</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                        </c:if>

                        <%-- Búsqueda de Artículos - para factura de compra de articulos
                        <c:if test="${facturaCompra.tipoFactura == 'compraArt' or empty facturaCompra.tipoFactura}">
                        <form id="formAgregarArticulo" method="post" action="FacturaCompraServlet">
                            <input type="hidden" name="menu" value="FacturaCompra">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" value="AgregarArticulo">
                            <input type="hidden" name="idArticulo" id="idArticuloAgregar" value="">
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="row" style="margin-top: 10px">
                                        <div class="col-auto">
                                            <button type="button" data-bs-toggle="modal" data-bs-target="#modalArticulos" class="btn btn-outline-primary">Buscar Artículo</button>
                                        </div>
                                        <div class="col-md-3">
                                            <input type="text" id="descripcionArticulo" placeholder="Descripción del artículo" class="form-control" readonly>
                                        </div>
                                        <div class="col-md-1">
                                            <input type="number" name="cantidad" placeholder="Cantidad" class="form-control" value="1">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="number" name="precioCompra" id="precioCompraArticulo" placeholder="Precio de compra" class="form-control">
                                        </div>
                                        <div class="col-md-2">
                                            <button type="submit" class="btn btn-success">Agregar Artículo</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                        </c:if>
                        --%>

                        <!-- Tabla de Artículos -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <div class="table-responsive">
                                    <table id="tablaArticulosFactura" class="table table-bordered table-sm custom-table">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Descripción</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center">Precio de compra</th>
                                                <th class="text-bg-dark text-center">Sub. Total</th>
                                                <th class="text-bg-dark text-center">Gravada 10%</th>
                                                <th class="text-bg-dark text-center">IVA 10%</th>
                                                <th class="text-bg-dark text-center">Gravada 5%</th>
                                                <th class="text-bg-dark text-center">IVA 5%</th>
                                                <th class="text-bg-dark text-center">Exenta</th>
                                                <c:if test="${fn:contains(facturaCompra.tipoFactura, 'gasto') or fn:contains(facturaCompra.tipoFactura, 'fondoFijo')}">
                                                    <th class="text-bg-dark text-center no-search">Acciones</th>
                                                </c:if>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:set var="totalGeneral" value="0" />
                                            <c:set var="totalIva10" value="0" />
                                            <c:set var="totalIva5" value="0" />
                                            <c:set var="totalExenta" value="0" />
                                            <c:forEach var="detalle" items="${listaFacturaCompraDetalle}" varStatus="status">
                                                <c:set var="subtotal" value="${detalle.cantidad * detalle.precioCompra}" />
                                                <c:set var="totalGeneral" value="${totalGeneral + subtotal}" />

                                                <%--
                                                    Calcular IVA según el tipo de impuesto del artículo.
                                                    Se usa fn:contains para verificar si la descripcion contiene "10" o "5".
                                                    Ejemplo: Si descripcion es "IVA 10%", fn:contains(descripcion, '10') = true
                                                    - IVA 10%: El impuesto es 1/11 del subtotal (subtotal incluye IVA)
                                                    - IVA 5%: El impuesto es 1/21 del subtotal
                                                    - Exenta: No tiene IVA
                                                --%>
                                                <c:set var="gravada10" value="0" />
                                                <c:set var="iva10" value="0" />
                                                <c:set var="gravada5" value="0" />
                                                <c:set var="iva5" value="0" />
                                                <c:set var="exenta" value="0" />

                                                <%-- Obtener descripcion del impuesto (del articulo o del detalle) --%>
                                                <c:set var="descImpuesto" value="${not empty detalle.articulo ? detalle.articulo.tipoImpuesto.descripcion : detalle.tipoImpuesto.descripcion}" />

                                                <c:choose>
                                                    <%-- fn:contains verifica si el primer string contiene el segundo --%>
                                                    <c:when test="${fn:contains(descImpuesto, '10')}">
                                                        <c:set var="iva10" value="${subtotal / 11}" />
                                                        <c:set var="gravada10" value="${subtotal - iva10}" />
                                                        <c:set var="totalIva10" value="${totalIva10 + iva10}" />
                                                    </c:when>
                                                    <c:when test="${fn:contains(descImpuesto, '5')}">
                                                        <c:set var="iva5" value="${subtotal / 21}" />
                                                        <c:set var="gravada5" value="${subtotal - iva5}" />
                                                        <c:set var="totalIva5" value="${totalIva5 + iva5}" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="exenta" value="${subtotal}" />
                                                        <c:set var="totalExenta" value="${totalExenta + exenta}" />
                                                    </c:otherwise>
                                                </c:choose>

                                                <tr>
                                                    <td class="text-center">
                                                        <c:choose>
                                                            <c:when test="${not empty detalle.articulo}">
                                                                ${detalle.articulo.descripcion}
                                                            </c:when>
                                                            <c:otherwise>
                                                                ${detalle.descripcion}
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${detalle.cantidad}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${detalle.precioCompra}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${subtotal}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${gravada10}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${iva10}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${gravada5}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${iva5}" pattern="#,###"/>
                                                    </td>
                                                    <td class="text-center">
                                                        <fmt:formatNumber value="${exenta}" pattern="#,###"/>
                                                    </td>
                                                    <c:if test="${fn:contains(facturaCompra.tipoFactura, 'gasto') or fn:contains(facturaCompra.tipoFactura, 'fondoFijo')}">
                                                        <td class="text-center">
                                                            <a href="FacturaCompraServlet?menu=FacturaCompra&accion=EditarArticulo&token=${token}&index=${status.index}"
                                                               class="btn btn-warning btn-sm">Editar</a>
                                                            <button type="button" class="btn btn-danger btn-sm"
                                                                    data-bs-toggle="modal" data-bs-target="#modalEliminar${status.index}">Eliminar</button>
                                                            <!-- Modal de confirmación para eliminar artículo -->
                                                            <div class="modal fade" id="modalEliminar${status.index}" tabindex="-1" aria-hidden="true">
                                                                <div class="modal-dialog modal-dialog-centered">
                                                                    <div class="modal-content">
                                                                        <div class="modal-header bg-warning">
                                                                            <h5 class="modal-title">Confirmación</h5>
                                                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                        </div>
                                                                        <div class="modal-body">
                                                                            ¿Está seguro de eliminar este artículo del detalle?
                                                                        </div>
                                                                        <div class="modal-footer">
                                                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                                                            <form action="FacturaCompraServlet" method="POST">
                                                                                <input type="hidden" name="menu" value="FacturaCompra">
                                                                                <input type="hidden" name="accion" value="EliminarArticulo">
                                                                                <input type="hidden" name="token" value="${token}">
                                                                                <input type="hidden" name="index" value="${status.index}">
                                                                                <button type="submit" class="btn btn-danger">Sí, Eliminar</button>
                                                                            </form>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </td>
                                                    </c:if>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>

                                <!-- Botones finales -->
                                <div class="row mt-3">
                                    <div class="col-md-6">
                                        <c:if test="${not empty token}">
                                            <button type="button" class="btn btn-success" onclick="guardarFactura();">Guardar</button>
                                            <a href="FacturaCompraServlet?menu=FacturaCompra&accion=Cancelar&token=${token}"
                                               class="btn btn-secondary">Cancelar</a>
                                        </c:if>
                                    </div>
                                    <div class="col-md-6 text-end">
                                        <h5>Total: Gs. <fmt:formatNumber value="${totalGeneral}" pattern="#,###"/></h5>
                                        <small>IVA 10%: <fmt:formatNumber value="${totalIva10}" pattern="#,###"/> |
                                               IVA 5%: <fmt:formatNumber value="${totalIva5}" pattern="#,###"/> |
                                               Exenta: <fmt:formatNumber value="${totalExenta}" pattern="#,###"/></small>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal para Buscar Facturas -->
                        <div class="modal fade" id="modalFacturas" tabindex="-1" aria-labelledby="modalFacturasLabel" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Facturas de Compra</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalFacturas" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">ID</th>
                                                        <th class="text-bg-dark text-center">N° Factura</th>
                                                        <th class="text-bg-dark text-center">Tipo</th>
                                                        <th class="text-bg-dark text-center">Proveedor</th>
                                                        <th class="text-bg-dark text-center">RUC</th>
                                                        <th class="text-bg-dark text-center">Estado</th>
                                                        <th class="text-bg-dark text-center">Fecha</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="fac" items="${listaFacturasCompra}">
                                                        <tr class="${fac.estado == 'Anulado' ? 'table-danger' : (fac.estado == 'Completado' ? 'table-success' : '')}">
                                                            <td class="text-center">${fac.idFacturaCompra}</td>
                                                            <td class="text-center">${fac.numero}</td>
                                                            <td class="text-center">${fac.tipoFactura}</td>
                                                            <td class="text-center">${fac.proveedor.razonSocial}</td>
                                                            <td class="text-center">${fac.proveedor.ruc}</td>
                                                            <td class="text-center">${fac.estado}</td>
                                                            <td class="text-center">
                                                                <fmt:formatDate value="${fac.fechaCarga}" pattern="dd/MM/yyyy"/>
                                                            </td>
                                                            <td class="text-center">
                                                                <c:choose>
                                                                    <c:when test="${fac.estado == 'Anulado'}">
                                                                        <button class="btn btn-secondary btn-sm" disabled>Seleccionar</button>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <a href="FacturaCompraServlet?menu=FacturaCompra&accion=CargarFactura&idFactura=${fac.idFacturaCompra}"
                                                                           class="btn btn-primary btn-sm">Seleccionar</a>
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

                        <!-- Modal para Buscar Ordenes de Compra -->
                        <div class="modal fade" id="modalPedidos" tabindex="-1" aria-labelledby="modalPedidosLabel" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Ordenes de Compra</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalPedidos" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">N° Orden</th>
                                                        <th class="text-bg-dark text-center">Proveedor</th>
                                                        <th class="text-bg-dark text-center">Sucursal</th>
                                                        <th class="text-bg-dark text-center">Condición</th>
                                                        <th class="text-bg-dark text-center">Fecha</th>
                                                        <th class="text-bg-dark text-center">Estado</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="orden" items="${listaOrdenesCompra}">
                                                        <tr class="${orden.estado == 'Anulado' ? 'table-danger' : (orden.estado == 'Completado' ? 'table-success' : '')}">
                                                            <td class="text-center">${orden.idOrdenCompra}</td>
                                                            <td class="text-center">${orden.proveedor.razonSocial}</td>
                                                            <td class="text-center">${orden.sucursal.descripcion}</td>
                                                            <td class="text-center">${orden.condicionCompra}</td>
                                                            <td class="text-center">
                                                                <fmt:formatDate value="${orden.fecha}" pattern="dd/MM/yyyy"/>
                                                            </td>
                                                            <td class="text-center">${orden.estado}</td>
                                                            <td class="text-center">
                                                                <c:choose>
                                                                    <c:when test="${orden.estado == 'Completado' || orden.estado == 'Anulado'}">
                                                                        <button class="btn btn-secondary btn-sm" disabled
                                                                                title="${orden.estado == 'Completado' ? 'Orden ya completada' : 'Orden anulada'}">Seleccionar</button>
                                                                    </c:when>
                                                                    <c:when test="${not empty token}">
                                                                        <a href="FacturaCompraServlet?menu=FacturaCompra&accion=CargarOrdenCompra&token=${token}&idOrden=${orden.idOrdenCompra}"
                                                                           class="btn btn-primary btn-sm">Seleccionar</a>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <button class="btn btn-primary btn-sm" disabled
                                                                                title="Primero debe crear una nueva factura">Seleccionar</button>
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

                        <!-- Modal Proveedores -->
                        <div class="modal fade" id="modalProveedores" tabindex="-1" aria-labelledby="modalProveedoresLabel" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Proveedores</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalProveedores" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">ID</th>
                                                        <th class="text-bg-dark text-center">Razón Social</th>
                                                        <th class="text-bg-dark text-center">RUC</th>
                                                        <th class="text-bg-dark text-center">Teléfono</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="prov" items="${listaProveedores}">
                                                        <tr>
                                                            <td class="text-center">${prov.idProveedor}</td>
                                                            <td class="text-center">${prov.razonSocial}</td>
                                                            <td class="text-center">${prov.ruc}</td>
                                                            <td class="text-center">${prov.telefono}</td>
                                                            <td class="text-center">
                                                                <a href="FacturaCompraServlet?menu=FacturaCompra&accion=CargarProveedor&token=${token}&idProveedor=${prov.idProveedor}"
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

                        <!-- Modal Articulos -->
                        <div class="modal fade" id="modalArticulos" tabindex="-1" aria-labelledby="modalArticulosLabel" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Artículos</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalArticulos" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">Id. Artículo</th>
                                                        <th class="text-bg-dark text-center">Descripción</th>
                                                        <th class="text-bg-dark text-center">Precio Compra</th>
                                                        <th class="text-bg-dark text-center">Tipo Impuesto</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="art" items="${listaArticulos}">
                                                        <tr>
                                                            <td class="text-center">${art.idArticulo}</td>
                                                            <td class="text-center">${art.descripcion}</td>
                                                            <td class="text-center">
                                                                <fmt:formatNumber value="${art.precioCompra}" pattern="#,###"/>
                                                            </td>
                                                            <td class="text-center">${art.tipoImpuesto.descripcion}</td>
                                                            <td class="text-center">
                                                                <button type="button" class="btn btn-primary btn-sm"
                                                                        onclick="seleccionarArticulo(${art.idArticulo}, '${art.descripcion}', ${art.precioCompra});"
                                                                        data-bs-dismiss="modal">Seleccionar</button>
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

                        <!-- Modal de confirmación para Anular Factura -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-labelledby="modalConfirmarAnularLabel" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white">
                                        <h5 class="modal-title" id="modalConfirmarAnularLabel">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <p>¿Está seguro que desea anular esta factura?</p>
                                        <p class="text-muted small">Esta acción revertirá los estados de los documentos relacionados a "Pendiente".</p>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <form action="FacturaCompraServlet" method="POST">
                                            <input type="hidden" name="menu" value="FacturaCompra">
                                            <input type="hidden" name="accion" value="Anular">
                                            <input type="hidden" name="token" value="${token}">
                                            <button type="submit" class="btn btn-danger">Sí, Anular</button>
                                        </form>
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
            // Funciones para manejar acciones del formulario
            function guardarFactura() {
                document.getElementById('accionPrincipal').value = 'Guardar';
                document.getElementById('formPrincipal').submit();
            }

            function cambiarSucursal() {
                document.getElementById('accionPrincipal').value = 'CambiarSucursal';
                document.getElementById('formPrincipal').submit();
            }

            function cambiarCondicion() {
                var condicion = document.getElementById('condicionCompra').value;
                var plazoInput = document.getElementById('plazoCredito');

                if (condicion === 'Credito') {
                    plazoInput.disabled = false;
                } else {
                    plazoInput.disabled = true;
                    plazoInput.value = 0;
                }

                document.getElementById('accionPrincipal').value = 'CambiarCondicion';
                document.getElementById('formPrincipal').submit();
            }

            function cambiarTipoFactura() {
                document.getElementById('accionPrincipal').value = 'CambiarTipoFactura';
                document.getElementById('formPrincipal').submit();
            }

            function seleccionarArticulo(idArticulo, descripcion, precioCompra) {
                document.getElementById('idArticuloAgregar').value = idArticulo;
                document.getElementById('descripcionArticulo').value = descripcion;
                document.getElementById('precioCompraArticulo').value = precioCompra;
            }

            // Inicialización de DataTables para todas las tablas
            $(document).ready(function () {
                // Tabla principal de artículos en factura
                $('#tablaArticulosFactura').DataTable({
                    initComplete: function () {
                        this.api().columns().every(function () {
                            var column = this;
                            var title = column.footer().textContent;
                            if (title !== "Acciones" && !$(column.header()).hasClass('no-search') && !$(column.footer()).hasClass('no-search')) {
                                $('<input style="width: 100%" type="text" placeholder="Buscar ' + title + '" />')
                                    .appendTo($(column.footer()).empty())
                                    .on('keyup change clear', function () {
                                        if (column.search() !== this.value) {
                                            column.search(this.value).draw();
                                        }
                                    });
                            } else {
                                $(column.footer()).empty();
                            }
                        });
                    },
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal de facturas
                $('#tablaModalFacturas').DataTable({
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal de pedidos/ordenes
                $('#tablaModalPedidos').DataTable({
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal de proveedores
                $('#tablaModalProveedores').DataTable({
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal de artículos
                $('#tablaModalArticulos').DataTable({
                    language: { url: "DataTables 2/es-ES.json" }
                });
            });
        </script>

        <!-- Código para mostrar mensajes -->
        <% String Message = (String) request.getAttribute("Message");%>
        <% String tipoAlert = (String) request.getAttribute("tipoAlert");%>
        <c:if test="${not empty Message}">
            <div id="mensaje" class="alert <%= tipoAlert != null ? tipoAlert : "alert-info"%>"
                 style="position:absolute; top: 80px; right: 10px; opacity: 80%; transition: opacity 1s ease; min-width: 200px;" role="alert">
                <%= Message%>
                <button type="button" style="border: none; width: 25px; height: 25px; float:right; display:inline-block; padding:0px 5px;"
                        class="btn <%= tipoAlert != null ? tipoAlert + " btn-close" : "alert-info"%>" data-bs-dismiss="alert" aria-label="Close">
                </button>
            </div>
        </c:if>
        <script>
            setTimeout(function () {
                var mensaje = document.getElementById('mensaje');
                if(mensaje) {
                    mensaje.style.opacity = '0';
                    setTimeout(function () {
                        mensaje.style.display = 'none';
                    }, 1000);
                }
            }, 7000);
        </script>
    </body>
</html>
