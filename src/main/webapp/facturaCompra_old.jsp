<%-- 
    Document   : facturaCompra
    Created on : 7/03/2025, 10:37:36 PM
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
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>
                            
                            <!-- Botones principales -->
                            <div class="col-auto">
                                <button class="btn btn-success">Nuevo</button>
                                <a href="" data-bs-toggle="modal" data-bs-target="#modalPedidos" class="btn btn-info text-white">Buscar Orden de Compra</a>
                                <a href="" data-bs-toggle="modal" data-bs-target="#modalFacturas" class="btn btn-info text-white">Buscar Factura Compra</a>
                                <button class="btn btn-danger">Anular</button>
                            </div>
                        </div>
                        
                        <form method="post" action="facturaCompra.jsp">
                        <!-- Cabecera -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <h3>Cabecera</h3>
                                <div class="row" style="margin-top: 23px">
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Usuario:</label>
                                        <input type="text" class="form-control" value="">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha:</label>
                                        <input type="date" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Estado:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <script>
                                    $(document).ready(function(){
                                        $('#nOrdenCompra').mask('A.AAA.AAA.AAA'); //la letra A es para numero y letra
                                      });
                                    </script>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Orden de Compra N°:</label>
                                        <input id="nOrdenCompra" type="text" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Sucursal:</label>
                                        <select class="form-control">
                                            <option>Seleccionar sucursal</option>
                                        </select>
                                    </div>
                                </div>

                        <!-- Información del Proveedor -->
                                <div class="row" style="margin-top: 23px">
                                    <div class="col-auto d-flex align-items-center">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <button type="button" href="" data-bs-toggle="modal" 
                                                        data-bs-target="#modalProveedores" class="btn btn-outline-primary">Buscar Proveedor</button>
                                            </div>
                                        </div>
<!--                                    <div class="col-md-1 d-flex align-items-center">
                                            <input type="text" name="txtIdProveedor" placeholder="Id. Proveedor" value=""
                                                   class="form-control" disabled="true">
                                        </div>-->
                                        <div class="col-md-2 d-flex align-items-center">
                                            <input type="text" placeholder="Razon Social" value="" 
                                                   class="form-control">
                                        </div>
                                        <div class="col-md-1 d-flex align-items-center">
                                            <input type="text" placeholder="Ruc" value="" class="form-control"
                                                   required="true">
                                        </div>
                                    
                                    <!--mascara para el ruc de plugin de jquery-->
                                    <script>
                                    $(document).ready(function(){
                                        $('#ruc').mask('000-000-0000000');
                                      });
                                    </script>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Comprobante N°:</label>
                                        <input id="ruc" type="text" class="form-control" placeholder="000-000-0000000">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha de emisión:</label>
                                        <input type="date" class="form-control">
                                    </div>
                                    <c:set var="esCredito" value="${param.condicionCompra == 'credito'}" />
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Condición de la compra:</label>
                                        <select class="form-control" id="condicionCompra" name="condicionCompra" onchange="this.form.submit()">
                                            <option value="contado" <c:if test="${!esCredito}">selected</c:if>>Contado</option>
                                            <option value="credito" <c:if test="${esCredito}">selected</c:if>>Crédito</option>
                                        </select>
                                    </div>
                                </div>
                                    
<!--                                    <div class="row" style="margin-top: 23px">
     Timbrado 
    <div class="col-md-2 mb-2">
        <label class="form-label small fw-bold">Timbrado:</label>
        <input type="text" class="form-control form-control-sm">
    </div>
    
     Fecha de inicio 
    <div class="col-md-2 mb-2">
        <label class="form-label small fw-bold">Fecha de inicio:</label>
        <input type="date" class="form-control form-control-sm">
    </div>
    
     Fecha de vencimiento 
    <div class="col-md-2 mb-2">
        <label class="form-label small fw-bold">Fecha de vencimiento:</label>
        <input type="date" class="form-control form-control-sm">
    </div>
    
     Plazo de condición 
    <div class="col-md-2 mb-2">
        <label class="form-label small fw-bold">Plazo de condición:</label>
        <div class="input-group input-group-sm">
            <input type="number" id="plazoCredito" name="plazoCredito" class="form-control" 
                   value="<c:out value='${param.plazoCredito != null ? param.plazoCredito : "0"}' />"
                   <c:if test="${!esCredito}">disabled="disabled"</c:if>>
            <span class="input-group-text">días</span>
        </div>
    </div>
    
     Tipo de Factura 
    <div class="col-md-2 mb-2">
        <label class="form-label small fw-bold">Tipo de Factura:</label>
        <select class="form-control form-control-sm">
            <option>Seleccionar tipo de factura</option>
            <option>Factura</option>
            <option>Factura Fondo Fijo</option>
        </select>
    </div>
</div>-->
                                
                                <div class="row" style="margin-top: 23px">
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Timbrado:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha de inicio:</label>
                                        <input type="date" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha de vencimiento:</label>
                                        <input type="date" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Plazo de condición:</label>
                                            <input type="number" id="plazoCredito" name="plazoCredito" class="form-control" 
                                                value="<c:out value='${param.plazoCredito != null ? param.plazoCredito : "0"}' />"
                                                <c:if test="${!esCredito}">disabled="disabled"</c:if>>
                                            <span class="input-group-text">días</span>
                                    </div>
                                            
                                    <!-- Determinar si el tipo de factura es "gasto" -->
                                    <c:set var="esGasto" value="${param.tipoFactura == 'gasto'}" />
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Tipo de Factura:</label>
                                        <select class="form-control" id="tipoFactura" name="tipoFactura" onchange="this.form.submit()">
                                            <option>Seleccionar tipo de factura</option>
                                            <option value="compraArt" <c:if test="${param.tipoFactura == 'compraArt'}">selected</c:if>>Factura Compra de Artículos</option>
                                            <option value="fondoFijo" <c:if test="${param.tipoFactura == 'fondoFijo'}">selected</c:if>>Factura Fondo Fijo</option>
                                            <option value="gasto" <c:if test="${param.tipoFactura == 'gasto'}">selected</c:if>>Factura de Gasto</option>
                                        </select>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Línea separadora -->
                        <div class="border-section"></div>

                        <!-- Búsqueda de Artículos -->
                        <!--mostrar el div de agregar articulo si es faactura de fondo fjo o factura por otro gasto--> 
                        <c:if test="${param.tipoFactura == 'fondoFijo' or param.tipoFactura == 'gasto'}">
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <div class="row" style="margin-top: 10px">
<!--                                    <div class="col-auto">
                                        <a href="" data-bs-toggle="modal" data-bs-target="#modalArticulos" class="btn btn-outline-primary">Buscar Artículo</a>
                                    </div>
                                    <div class="col-md-1">
                                        <input type="text" placeholder="Id. Artículo" class="form-control">
                                    </div>-->
                                    <div class="col-md-3">
                                        <input type="text" placeholder="Descripción" class="form-control">
                                    </div>
<!--                                    <div class="col-md-2">
                                        <select class="form-control">
                                            <option>Depósito Asu 1</option>
                                            <option>Depósito Asu 2</option>
                                            <option>Depósito Central</option>
                                        </select>
                                    </div>-->
                                    <div class="col-md-1">
                                        <input type="number" placeholder="Cantidad" class="form-control">
                                    </div>
                                    <div class="col-md-2">
                                        <input type="number" placeholder="Precio de compra" class="form-control">
                                    </div>
                                    <div class="col-md-2">
                                        <a href="Controlador?menu=Producto&accion=Listar" target="miContenedor" class="btn btn-success">Agregar Artículo</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                        </c:if>
                        </form>

                        <!-- Tabla de Artículos -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <div class="table-responsive">
                                    <table id="tablaArticulosFactura" class="table table-bordered table-sm custom-table">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Artículo</th>
                                                <th class="text-bg-dark text-center">Descripción</th>
                                                <th class="text-bg-dark text-center">Depósito</th>
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
                                                <td class="text-center">Depósito Asu 1</td>
                                                <td class="text-center">24</td>
                                                <td class="text-center">5,000</td>
                                                <td class="text-center">120,000</td>
                                                <td class="text-center">109,091</td>
                                                <td class="text-center">10,909</td>
                                                <td class="text-center">0</td>
                                                <td class="text-center">0</td>
                                                <td class="text-center">0</td>
                                                <td class="text-center">
                                                    <button class="btn btn-danger btn-sm">Eliminar</button>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center">3</td>
                                                <td class="text-center">Licor de Coco</td>
                                                <td class="text-center">Depósito Asu 1</td>
                                                <td class="text-center">10</td>
                                                <td class="text-center">8,000</td>
                                                <td class="text-center">80,000</td>
                                                <td class="text-center">72,727</td>
                                                <td class="text-center">7,273</td>
                                                <td class="text-center">0</td>
                                                <td class="text-center">0</td>
                                                <td class="text-center">0</td>
                                                <td class="text-center">
                                                    <button class="btn btn-danger btn-sm">Eliminar</button>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                                
                                <!-- Botones finales -->
                                <div class="row mt-3">
                                    <div class="col-md-6">
                                        <button class="btn btn-success">Guardar</button>
                                        <button class="btn btn-secondary">Cancelar</button>
                                    </div>
                                    <div class="col-md-6 text-end">
                                        <h5>Total: Gs. 200,000</h5>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal para Buscar Facturas (nuevo modal) -->
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

                        <!-- Los modales existentes para Pedidos y Artículos se mantienen igual -->
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
                                                        <th class="text-bg-dark text-center">Total</th>
                                                        <th class="text-bg-dark text-center">Fecha</th>
                                                        <th class="text-bg-dark text-center">Estado</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr>
                                                        <td class="text-center">OC-001</td>
                                                        <td class="text-center">Distribuidora ABC</td>
                                                        <td class="text-center">200,000</td>
                                                        <td class="text-center">05/03/2025</td>
                                                        <td class="text-center">Pendiente</td>
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

                        <!-- Modal Articulos (actualizado) -->
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

                // Las inicializaciones existentes para tablas modalPedidos y modalArticulos se mantienen
                $('#tablaModalPedidos').DataTable({
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

        <!-- Código para mostrar mensajes (se mantiene igual) -->
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
        <script>
        $(function () {
            // Success Type
            $('#ts-success').on('click', function () {
                toastr.success('Have fun storming the castle!', 'Miracle Max Says');
            });

            // Success Type
            $('#ts-info').on('click', function () {
                toastr.info('We do have the Kapua suite available.', 'Turtle Bay Resort');
            });

            // Success Type
            $('#ts-warning').on('click', function () {
                toastr.warning('My name is Inigo Montoya. You killed my father, prepare to die!');
            });

            // Success Type
            $('#ts-error').on('click', function () {
                toastr.error('I do not think that word means what you think it means.', 'Inconceivable!');
            });
        });
    </script>
    </body>
</html>