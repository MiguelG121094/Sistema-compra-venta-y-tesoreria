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
                background-color: #fff;
            }
            .section-title {
                background-color: #e9ecef;
                padding: 8px 12px;
                margin: 0 0 16px 0;
                border-radius: 4px;
                font-weight: bold;
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
            .form-floating > .form-control,
            .form-floating > .form-select {
                height: calc(3.5rem + 2px);
                padding: 1rem 0.75rem;
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
                        <div class="row mb-4">
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <span style="height: 100%; width: 100%;">
                                    <h1 style="text-align: center">
                                        <strong>NOTA DE CRÉDITO Y DÉBITO</strong>
                                    </h1>
                                </span>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>
                            
                            <!-- Botones principales -->
                            <div class="col-auto">
                                <button class="btn btn-success">Nuevo</button>
                                <a href="" data-bs-toggle="modal" data-bs-target="#modalBuscarNota" class="btn btn-info text-white">Buscar Nota de Crédito o Débito</a>
                                <a href="" data-bs-toggle="modal" data-bs-target="#modalBuscarFactura" class="btn btn-info text-white">Buscar Factura Compra</a>
                                <button class="btn btn-danger">Anular</button>
                            </div>
                        </div>

                        <form method="post" action="notaCreditoDebito.jsp">
                            <!-- Cabecera -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <h3 class="section-title">Cabecera</h3>
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="usuario" type="text" placeholder="Usuario" 
                                                           value="<%= usuario != null ? usuario.getNombre() : "" %>" readonly />
                                                    <label for="usuario">Usuario</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="fecha" type="date" placeholder="Fecha" />
                                                    <label for="fecha">Fecha</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="estado" type="text" placeholder="Estado" value="Pendiente" readonly />
                                                    <label for="estado">Estado</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <select class="form-control" id="sucursal" name="sucursal">
                                                        <option>Seleccionar Sucursal</option>
                                                        <option>Asunción - Sajonia</option>
                                                        <option>Asunción - Mercado 4</option>
                                                        <option>Central - Lambaré</option>
                                                    </select>
                                                    <label for="sucursal">Sucursal</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="idFactura" type="text" placeholder="ID Factura" />
                                                    <label for="idFactura">ID Factura N°</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Información del Proveedor y Factura -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="razonSocial" type="text" placeholder="Razón Social" />
                                                    <label for="razonSocial">Razón Social</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="ruc" type="text" placeholder="RUC" />
                                                    <label for="ruc">RUC</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="comprobanteN" type="text" placeholder="000-000-0000000" />
                                                    <label for="comprobanteN">Comprobante N°</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="fechaEmision" type="date" placeholder="Fecha de emisión" />
                                                    <label for="fechaEmision">Fecha de emisión</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="motivo" type="text" placeholder="Motivo" />
                                                    <label for="motivo">Motivo</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Timbrado y Condiciones -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="timbrado" type="text" placeholder="Timbrado" />
                                                    <label for="timbrado">Timbrado</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="fechaVencimiento" type="date" placeholder="Fecha de vencimiento" />
                                                    <label for="fechaVencimiento">Fecha de vencimiento</label>
                                                </div>
                                            </div>
                                            <c:set var="esCredito" value="${param.condicionCompra == 'credito'}" />
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <select class="form-control" id="condicionCompra" name="condicionCompra" onchange="this.form.submit()">
                                                        <option value="contado" <c:if test="${!esCredito}">selected</c:if>>Contado</option>
                                                        <option value="credito" <c:if test="${esCredito}">selected</c:if>>Crédito</option>
                                                    </select>
                                                    <label for="condicionCompra">Condición de compra</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <select class="form-control" id="tipoNota" name="tipoNota">
                                                        <option>Seleccionar tipo de nota</option>
                                                        <option value="credito">Nota de Crédito</option>
                                                        <option value="debito">Nota de Débito</option>
                                                    </select>
                                                    <label for="tipoNota">Seleccionar tipo de nota</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Búsqueda de Artículos -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="card-body">
                                        <div class="row mb-3 align-items-end">
                                            <div class="col-md-1">
                                                <div class="mb-3 mb-md-0">
                                                    <button type="button" data-bs-toggle="modal" 
                                                        data-bs-target="#modalArticulos" class="btn btn-outline-primary w-100">
                                                        <span class="d-none d-md-inline">Buscar Artículo</span>
                                                        <span class="d-md-none">Buscar</span>
                                                    </button>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="idArticulo" type="text" placeholder="Id. Artículo" />
                                                    <label for="idArticulo">Id. Artículo</label>
                                                </div>
                                            </div>
                                            <div class="col-md-3">
                                                <div class="form-floating">
                                                    <input class="form-control" id="descripcion" type="text" placeholder="Descripción" />
                                                    <label for="descripcion">Descripción</label>
                                                </div>
                                            </div>
                                            <div class="col-md-1">
                                                <div class="form-floating">
                                                    <input class="form-control" id="cantidad" type="number" placeholder="Cantidad" />
                                                    <label for="cantidad">Cantidad</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="precioCompra" type="number" placeholder="Precio de compra" />
                                                    <label for="precioCompra">Precio de compra</label>
                                                </div>
                                            </div>
                                            <div class="col-md-1">
                                                <button type="button" class="btn btn-success w-100" style="height: 58px;">Agregar</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Tabla de Artículos -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <div class="table-responsive">
                                    <table id="tablaArticulosNota" class="table table-bordered table-sm">
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
                                                    <button class="btn btn-warning btn-sm">Editar</button>
                                                    <button class="btn btn-danger btn-sm">Eliminar</button>
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
                                                    <button class="btn btn-warning btn-sm">Editar</button>
                                                    <button class="btn btn-danger btn-sm">Eliminar</button>
                                                </td>
                                            </tr>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <th colspan="4" class="text-end">Total:</th>
                                                <th class="text-center">200,000</th>
                                                <th class="text-center">181,818</th>
                                                <th class="text-center">18,182</th>
                                                <th class="text-center">0</th>
                                                <th class="text-center">0</th>
                                                <th class="text-center">0</th>
                                                <th></th>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>

                                <!-- Botones finales -->
                                <div class="row mt-3">
                                    <div class="col-md-6">
                                        <button class="btn btn-success">Guardar</button>
                                        <button class="btn btn-secondary">Cancelar</button>
                                    </div>
                                    <div class="col-md-6 text-end">
                                        <h5>Total: <strong>Gs. 200,000</strong></h5>
                                    </div>
                                </div>
                            </div>
                        </div>

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
                                                            <button class="btn btn-primary btn-sm">Seleccionar</button>
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
                                                            <button class="btn btn-primary btn-sm">Seleccionar</button>
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

                        <!-- Modal Artículos -->
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
                                                            <button class="btn btn-primary btn-sm">Seleccionar</button>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-center">3</td>
                                                        <td class="text-center">Licor de Coco</td>
                                                        <td class="text-center">8,000</td>
                                                        <td class="text-center">50</td>
                                                        <td class="text-center">
                                                            <button class="btn btn-primary btn-sm">Seleccionar</button>
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
                // Tabla principal de artículos
                $('#tablaArticulosNota').DataTable({
                    dom: 'Bfrtip',
                    buttons: [
                        'copy', 'excelHtml5', 'pdfHtml5', 'print'
                    ],
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

                // Tabla modal buscar nota
                $('#tablaModalBuscarNota').DataTable({
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

                // Tabla modal buscar factura
                $('#tablaModalBuscarFactura').DataTable({
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

                // Tabla modal artículos
                $('#tablaModalArticulos').DataTable({
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
            });
        </script>

        <!-- Código para mostrar mensajes -->
        <% String Message = (String) request.getAttribute("Message");%>
        <% String tipoAlert = (String) request.getAttribute("tipoAlert");%>
        <c:if test="${not empty Message}">
            <div id="mensaje" class="alert <%= tipoAlert != null ? tipoAlert : "alert-info"%>"
                 style="position:absolute; top: 80px; right: 10px; opacity: 80%; transition: opacity 1s ease; min-width: 200px; z-index: 9999;" role="alert">
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