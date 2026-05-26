<%--
    Document   : notaCreditoDebito
    Created on : 15/03/2025
    Author     : Miguel
--%>

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
                                <button type="button" class="btn btn-success">Nuevo</button>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalBuscarNota"
                                        class="btn btn-info text-white">Buscar Nota de Crédito o Débito</button>
                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalBuscarFactura"
                                        class="btn btn-info text-white">Buscar Factura Compra</button>
                                <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular">Anular</button>
                            </div>
                        </div>

                        <!-- Formulario principal -->
                        <form id="formPrincipal" method="post" action="notaCreditoDebito.jsp">

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
                                                               value="<%= usuario != null ? usuario.getUsername() : "" %>" readonly />
                                                        <label for="usuario">Usuario</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="fecha" name="fecha" type="date" placeholder="Fecha" />
                                                        <label for="fecha">Fecha</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="estado" type="text" placeholder="Estado"
                                                               value="Pendiente" readonly />
                                                        <label for="estado">Estado</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <select class="form-control" id="sucursal" name="sucursal">
                                                            <option value="">Seleccionar Sucursal</option>
                                                            <option>Asunción - Sajonia</option>
                                                            <option>Asunción - Mercado 4</option>
                                                            <option>Central - Lambaré</option>
                                                        </select>
                                                        <label for="sucursal">Sucursal</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="idFactura" name="idFactura" type="text" placeholder="ID Factura" />
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
                                                        <input class="form-control" id="razonSocial" name="razonSocial" type="text" placeholder="Razón Social" />
                                                        <label for="razonSocial">Razón Social</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="ruc" name="ruc" type="text" placeholder="RUC" />
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
                                                               placeholder="000-000-0000000" />
                                                        <label for="comprobanteN">Comprobante N°</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="fechaEmision" name="fechaEmision" type="date" placeholder="Fecha de emisión" />
                                                        <label for="fechaEmision">Fecha de emisión</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="motivo" name="motivo" type="text" placeholder="Motivo" />
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
                                                               min="0" max="99999999"
                                                               oninput="if(this.value.length>8)this.value=this.value.slice(0,8)" />
                                                        <label for="timbrado">Timbrado</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <input class="form-control" id="fechaVencimiento" name="fechaVencimiento" type="date" placeholder="Fecha de vencimiento" />
                                                        <label for="fechaVencimiento">Fecha de vencimiento</label>
                                                    </div>
                                                </div>
                                                <c:set var="esCredito" value="${param.condicionCompra == 'credito'}" />
                                                <div class="col-md-2">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <select class="form-control" id="condicionCompra" name="condicionCompra">
                                                            <option value="contado" <c:if test="${!esCredito}">selected</c:if>>Contado</option>
                                                            <option value="credito" <c:if test="${esCredito}">selected</c:if>>Crédito</option>
                                                        </select>
                                                        <label for="condicionCompra" class="me-2">Condición de compra</label>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="form-floating mb-3 mb-md-0">
                                                        <select class="form-control" id="tipoNota" name="tipoNota">
                                                            <option value="">Seleccionar tipo de nota</option>
                                                            <option value="credito">Nota de Crédito</option>
                                                            <option value="debito">Nota de Débito</option>
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

                            <!-- Búsqueda y agregado de artículos -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="row align-items-end" style="margin-top: 10px">
                                        <div class="col-md-1">
                                            <div class="mb-3 mb-md-0">
                                                <button type="button" data-bs-toggle="modal" data-bs-target="#modalArticulos"
                                                        class="btn btn-outline-primary w-100 btn-responsive"
                                                        style="overflow: hidden; text-overflow: ellipsis;"
                                                        title="Buscar Artículo">Buscar Artículo</button>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="idArticulo" name="idArticulo" type="text" placeholder="Id. Artículo" readonly />
                                                <label for="idArticulo">Id. Artículo</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="descripcionArticulo" type="text" placeholder="Descripción" readonly />
                                                <label for="descripcionArticulo">Descripción</label>
                                            </div>
                                        </div>
                                        <div class="col-md-1">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control mask-miles" id="cantidad" name="cantidad" type="text" inputmode="numeric"
                                                       placeholder="Cantidad" value="1" />
                                                <label for="cantidad">Cantidad</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control mask-miles" id="precioCompra" name="precioCompra" type="text" inputmode="numeric"
                                                       placeholder="Precio de compra" />
                                                <label for="precioCompra">Precio de compra</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <button type="button" class="btn btn-success w-100" style="height: 58px;">Agregar Artículo</button>
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
                                                <tr>
                                                    <td class="text-center">2</td>
                                                    <td class="text-center">Jugo de Naranja</td>
                                                    <td class="text-center">24</td>
                                                    <td class="text-center">5,000</td>
                                                    <td class="text-center">120,000</td>
                                                    <td class="text-center">109,091</td>
                                                    <td class="text-center">10,909</td>
                                                    <td class="text-center">0</td>
                                                    <td class="text-center">0</td>
                                                    <td class="text-center">0</td>
                                                    <td class="text-center">
                                                        <button type="button" class="btn btn-warning btn-sm">Editar</button>
                                                        <button type="button" class="btn btn-danger btn-sm">Eliminar</button>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="text-center">3</td>
                                                    <td class="text-center">Licor de Coco</td>
                                                    <td class="text-center">10</td>
                                                    <td class="text-center">8,000</td>
                                                    <td class="text-center">80,000</td>
                                                    <td class="text-center">72,727</td>
                                                    <td class="text-center">7,273</td>
                                                    <td class="text-center">0</td>
                                                    <td class="text-center">0</td>
                                                    <td class="text-center">0</td>
                                                    <td class="text-center">
                                                        <button type="button" class="btn btn-warning btn-sm">Editar</button>
                                                        <button type="button" class="btn btn-danger btn-sm">Eliminar</button>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>

                                    <!-- Botones finales -->
                                    <div class="row mt-3">
                                        <div class="col-md-6">
                                            <button type="button" class="btn btn-success">Guardar</button>
                                            <button type="button" class="btn btn-danger">Cancelar</button>
                                        </div>
                                        <div class="col-md-6 text-end">
                                            <h5>Total: 200,000</h5>
                                            <small>IVA 10%: 18,182 | IVA 5%: 0 | Exenta: 0</small>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar Nota de Crédito o Débito -->
                        <div class="modal fade" id="modalBuscarNota" tabindex="-1" aria-labelledby="modalBuscarNotaLabel" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Nota de Crédito o Débito</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalBuscarNota" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">N° Nota</th>
                                                        <th class="text-bg-dark text-center">Tipo</th>
                                                        <th class="text-bg-dark text-center">Proveedor</th>
                                                        <th class="text-bg-dark text-center">RUC</th>
                                                        <th class="text-bg-dark text-center">Total</th>
                                                        <th class="text-bg-dark text-center">Fecha</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr>
                                                        <td class="text-center">NC-001</td>
                                                        <td class="text-center">Nota de Crédito</td>
                                                        <td class="text-center">Distribuidora ABC</td>
                                                        <td class="text-center">80012345-1</td>
                                                        <td class="text-center">200,000</td>
                                                        <td class="text-center">15/03/2025</td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm">Seleccionar</button>
                                                        </td>
                                                    </tr>
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
                        <div class="modal fade" id="modalBuscarFactura" tabindex="-1" aria-labelledby="modalBuscarFacturaLabel" aria-hidden="true">
                            <div class="modal-dialog modal-lg">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">Buscar Factura de Compra</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="table-responsive">
                                            <table id="tablaModalBuscarFactura" class="table table-bordered table-striped">
                                                <thead>
                                                    <tr>
                                                        <th class="text-bg-dark text-center">N° Factura</th>
                                                        <th class="text-bg-dark text-center">Proveedor</th>
                                                        <th class="text-bg-dark text-center">RUC</th>
                                                        <th class="text-bg-dark text-center">Total</th>
                                                        <th class="text-bg-dark text-center">Fecha</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr>
                                                        <td class="text-center">001-001-0000001</td>
                                                        <td class="text-center">Distribuidora ABC</td>
                                                        <td class="text-center">80012345-1</td>
                                                        <td class="text-center">200,000</td>
                                                        <td class="text-center">07/03/2025</td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm">Seleccionar</button>
                                                        </td>
                                                    </tr>
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

                        <!-- Modal Buscar Artículos -->
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
                                                        <th class="text-bg-dark text-center">Precio</th>
                                                        <th class="text-bg-dark text-center">Stock</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr>
                                                        <td class="text-center">2</td>
                                                        <td class="text-center">Jugo de Naranja</td>
                                                        <td class="text-center">5,000</td>
                                                        <td class="text-center">100</td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal">Seleccionar</button>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-center">3</td>
                                                        <td class="text-center">Licor de Coco</td>
                                                        <td class="text-center">8,000</td>
                                                        <td class="text-center">50</td>
                                                        <td class="text-center">
                                                            <button type="button" class="btn btn-primary btn-sm" data-bs-dismiss="modal">Seleccionar</button>
                                                        </td>
                                                    </tr>
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
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-labelledby="modalConfirmarAnularLabel" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white">
                                        <h5 class="modal-title" id="modalConfirmarAnularLabel">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <p>¿Está seguro que desea anular esta nota?</p>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger">Sí, Anular</button>
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
            $(document).ready(function () {
                // Máscara de puntos de miles para campos numéricos
                $('.mask-miles').mask('#.##0', {reverse: true});

                // Tabla principal de artículos de la nota
                $('#tablaArticulosNota').DataTable({
                    initComplete: function () {
                        this.api().columns().every(function () {
                            var column = this;
                            var title = column.footer() ? column.footer().textContent : '';
                            if (title !== "Acciones" && !$(column.header()).hasClass('no-search') && (!column.footer() || !$(column.footer()).hasClass('no-search'))) {
                                if (column.footer()) {
                                    $('<input style="width: 100%" type="text" placeholder="Buscar ' + title + '" />')
                                        .appendTo($(column.footer()).empty())
                                        .on('keyup change clear', function () {
                                            if (column.search() !== this.value) {
                                                column.search(this.value).draw();
                                            }
                                        });
                                }
                            } else if (column.footer()) {
                                $(column.footer()).empty();
                            }
                        });
                    },
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal buscar nota
                $('#tablaModalBuscarNota').DataTable({
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal buscar factura
                $('#tablaModalBuscarFactura').DataTable({
                    language: { url: "DataTables 2/es-ES.json" }
                });

                // Tabla modal buscar artículos
                $('#tablaModalArticulos').DataTable({
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
