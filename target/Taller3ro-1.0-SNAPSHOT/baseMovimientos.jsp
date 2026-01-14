<%-- 
    Document   : base
    Created on : 7/03/2025, 10:37:36 PM
    Author     : Miguel
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
<%@ page contentType="text/html;charset=UTF-8" language="java"  pageEncoding="UTF-8" %>
<html>
    <!--incluir los scripts y estilos en el header-->
    <jsp:include page="header.jsp" />
    <head>
        <title>Pedido de compra</title>
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
                background-color: #f8f9fa;
            }
        </style>
    </head>
    <body class="sb-nav-fixed">
        <!--Incluir menu lateral-->
        <jsp:include page="menuSuperior.jsp" />
        <div id="layoutSidenav">
            <!--Incluir menu lateral-->
            <jsp:include page="menuLateral.jsp" />
            <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <!-- Título y botones -->
                        <div class="row mb-4"><!--titulo de la pagina--> 
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <span style="height: 100%; width: 100%; background-color: yellow">
                                    <h1 style="text-align: center">
                                        <strong>PEDIDO DE COMPRA</strong></h1></span>
                            </div>
                            <!--linea debajo del titulo-->
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                            
                            
                            <!-- Modal Pedidos-->
                            <div class="modal fade" id="modalPedidos" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Pedidos de Compra</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                        <table id="tablaModalPedidos" class="table table-bordered table-striped">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <tr>
                                                <td class="text-center">1</td>
                                                <td class="text-center">Winner SRL</td>
                                                <td class="text-center">Coca Cola</td>
                                                <td class="text-center">12.000</td>
                                                <td class="text-center">05/11/2024</td>
                                                <td class="text-center">
                                                    <a href="Controlador?menu=RegistrarVenta&accion=BuscarCliente&idCliente=${c.getId()}" class="btn btn-primary">Seleccionar</a>

                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center">2</td>
                                                <td class="text-center">Despensa GyG</td>
                                                <td class="text-center">Jugo</td>
                                                <td class="text-center">10.000</td>
                                                <td class="text-center">04/11/2024</td>
                                                <td class="text-center">
                                                    <a href="Controlador?menu=RegistrarVenta&accion=BuscarCliente&idCliente=${c.getId()}" class="btn btn-primary">Seleccionar</a>

                                                </td>
                                            </tr>
                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center">Acciones</th>
                                            </tr>
                                        </tbody>
                                    </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a href="Controlador?menu=Producto&accion=Listar" target="miContenedor" class="btn btn-success">Agregar Artículo</a>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                                                    
                            <!-- Modal Articulo-->
                            <div class="modal fade" id="modalArticulos" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Artículo</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                        <table id="tablaModalArticulos" class="table table-bordered table-striped">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center">Acciones</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <tr>
                                                <td class="text-center">1</td>
                                                <td class="text-center">Winner SRL</td>
                                                <td class="text-center">Coca Cola</td>
                                                <td class="text-center">12.000</td>
                                                <td class="text-center">05/11/2024</td>
                                                <td class="text-center">
                                                    <a href="Controlador?menu=RegistrarVenta&accion=BuscarCliente&idCliente=${c.getId()}" class="btn btn-primary">Seleccionar</a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center">2</td>
                                                <td class="text-center">Despensa GyG</td>
                                                <td class="text-center">Jugo</td>
                                                <td class="text-center">10.000</td>
                                                <td class="text-center">04/11/2024</td>
                                                <td class="text-center">
                                                    <a href="Controlador?menu=RegistrarVenta&accion=BuscarCliente&idCliente=${c.getId()}" class="btn btn-primary">Seleccionar</a>
                                                </td>
                                            </tr>
<!--                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                            </tr>
                                        </tbody>-->
                                    </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a href="Controlador?menu=Producto&accion=Listar" target="miContenedor" class="btn btn-success">Agregar Artículo</a>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-auto">
                                <button class="btn btn-success">Nuevo</button>
                                <a href="" data-bs-toggle="modal" data-bs-target="#modalPedidos" class="btn btn-info text-white">Buscar Pedido</a>
                                <button class="btn btn-danger">Anular</button>
                            </div>
                        </div>

                        <!-- Cabecera -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <h3>Cabecera</h3>
<!--                                <div class="row">
                                    <div class="col-md-6">
                                        <label>Pedido N°:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-6">
                                        <label>Usuario:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                </div>
                                <div class="row mt-2">
                                    <div class="col-md-6">
                                        <label>Fecha:</label>
                                        <input type="date" class="form-control">
                                    </div>
                                    <div class="col-md-6">
                                        <label>Sucursal:</label>
                                        <select class="form-control">
                                            <option>Seleccionar sucursal</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="row mt-2">
                                    <div class="col-md-6">
                                        <label>Estado:</label>
                                        <input type="text" class="form-control" readonly>
                                    </div>
                                </div>-->
<!--                                <div class="row" style="margin-top: 23px">
                                    <div class="col-md-1">
                                        <label>Pedido N°:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-2">
                                        <label>Usuario:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-2">
                                        <label>Fecha:</label>
                                    <input type="date" class="form-control">
                                    </div>
                                    <div class="col-md-4">
                                        <label>Sucursal:</label>
                                    <select class="form-control">
                                        <option>Seleccionar sucursal</option>
                                    </select>
                                    </div>
                                    <div class="col-md-2">
                                        <label>Estado:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                </div>-->

                                <div class="row" style="margin-top: 23px">
                                    <div class="col-md-1 d-flex align-items-center">
                                        <label style="white-space: nowrap;" class="me-2">Pedido N°:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Usuario:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha:</label>
                                        <input type="date" class="form-control">
                                    </div>
                                    <div class="col-md-4 d-flex align-items-center">
                                        <label class="me-2">Sucursal:</label>
                                        <select class="form-control">
                                            <option>Seleccionar sucursal</option>
                                        </select>
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Estado:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                </div>

<!--<div class="row">-->
<!--                                    <div class="card-body" style="margin-top: 20px; margin-left: 20px">
                                        <div class="row mb-3">
                                            <div class="col-auto" style="margin-top: 10px">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <a href="" data-bs-toggle="modal" data-bs-target="#modalProducto" class="btn btn-outline-primary">Buscar Producto</a>
                                                </div>
                                            </div>
                                            <div class="col-md-1">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <input class="form-control" id="inputFirstName" type="text" placeholder="Id. Articulo" />
                                                    <label for="inputFirstName">Id. Articulo</label>
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="form-floating">
                                                    <input class="form-control" id="inputLastName" type="text" placeholder="Descripción" />
                                                    <label for="inputLastName">Descripción</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="inputDeposito" type="text" placeholder="Depósito" />
                                                    <label for="inputDeposito">Depósito</label>
                                                </div>
                                            </div>
                                            <div class="col-auto" style="margin-top: 10px">
                                                <div class="form-floating mb-3 mb-md-0">
                                                    <button class="btn btn-success">Agregar Artículo</button>
                                                </div>
                                            </div>
                                        </div>
                                    </div>-->

<!--                                    <div class="row">
                                        <div class="col-auto" style="margin-top: 23px">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <a href="" data-bs-toggle="modal" data-bs-target="#modalProducto" class="btn btn-outline-primary">Buscar Producto</a>
                                            </div>
                                        </div>
                                        <div class="col-md-1">
                                            <label>Id. Artículo:</label>
                                            <input type="text" class="form-control">
                                        </div>
                                        <div class="col-md-4">
                                            <label>Descripción:</label>
                                            <input type="text" class="form-control">
                                        </div>
                                        <div class="col-md-1">
                                            <label>Cantidad:</label>
                                            <input type="number" class="form-control">
                                        </div>
                                        <div class="col-md-2">
                                            <label>Depósito:</label>
                                            <select class="form-control">
                                                <option>Seleccionar depósito</option>
                                            </select>
                                        </div>
                                        <div class="col-md-2"  style="margin-top: 23px">
                                            <button class="btn btn-success">Agregar Artículo</button>
                                        </div>
                                    </div>-->

                                <div class="row" style="margin-top: 23px">
                                    <div class="col-auto">
                                        <div class="form-floating mb-3 mb-md-0">
                                            <a href="" data-bs-toggle="modal" data-bs-target="#modalArticulos" class="btn btn-outline-primary">Buscar Producto</a>
                                        </div>
                                    </div>
                                    <div class="col-md-1">
                                        <input type="text" placeholder="Id. Artículo" class="form-control">
                                    </div>
                                    <div class="col-md-4">
                                        <input type="text" placeholder="Descripción" class="form-control">
                                    </div>
                                    <div class="col-md-1">
                                        <input type="number" placeholder="Cantidad" class="form-control">
                                    </div>
                                    <div class="col-md-2">
                                        <select class="form-control">
                                            <option>Seleccionar depósito</option>
                                        </select>
                                    </div>
                                    <div class="col-md-2">
                                        <button class="btn btn-success">Agregar Artículo</button>
                                    </div>
                                </div>
                                <!--</div>-->
                            </div>
                        </div>

                        <!--Buscar Artículo--> 
<!--                        <div class="row mb-4">
                            <div class="col custom-card">
                                <h2>Buscar Artículo</h2>
                                <div class="row">
                                    <div class="col-auto" style="margin-top: 23px">
                                        <div class="form-floating mb-3 mb-md-0">
                                            <a href="" data-bs-toggle="modal" data-bs-target="#modalProducto" class="btn btn-outline-primary">Buscar Producto</a>
                                        </div>
                                    </div>
                                    <div class="col-md-1">
                                        <label>Id. Artículo:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-4">
                                        <label>Descripción:</label>
                                        <input type="text" class="form-control">
                                    </div>
                                    <div class="col-md-1">
                                        <label>Cantidad:</label>
                                        <input type="number" class="form-control">
                                    </div>
                                    <div class="col-md-2">
                                        <label>Depósito:</label>
                                        <select class="form-control">
                                            <option>Seleccionar depósito</option>
                                        </select>
                                    </div>
                                    <div class="col-md-2"  style="margin-top: 23px">
                                        <button class="btn btn-success">Agregar Artículo</button>
                                    </div>
                                </div>
                                <div class="row mt-2">
                                    <div class="col">
                                        <button class="btn btn-success">Agregar Artículo</button>
                                    </div>
                                </div>
                            </div>
                        </div>-->

                        <!-- Tabla de Artículos -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <!--<h2>Artículos Agregados</h2>-->
                                <!--<div class="card mb-4">-->
                                <div class="card-body">
                                    <table id="tablaPrueba2" class="table table-bordered">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <tr>
                                                <td class="text-center">1</td>
                                                <td class="text-center">Winner SRL</td>
                                                <td class="text-center">Coca Cola</td>
                                                <td class="text-center">12.000</td>
                                                <td class="text-center">05/11/2024</td>
                                                <td class="text-center">
                                                    <a href="Controlador?menu=RegistrarVenta&accion=BuscarCliente&idCliente=${c.getId()}" class="btn btn-danger">Eliminar</a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center">2</td>
                                                <td class="text-center">Despensa GyG</td>
                                                <td class="text-center">Jugo</td>
                                                <td class="text-center">10.000</td>
                                                <td class="text-center">04/11/2024</td>
                                                <td class="text-center">
                                                    <a href="Controlador?menu=RegistrarVenta&accion=BuscarCliente&idCliente=${c.getId()}" class="btn btn-danger">Eliminar</a>
                                                </td>
                                            </tr>
                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                                <th class="text-bg-dark text-center">Acciones</th>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>    
                                <div class="card-footer d-flex  justify-content-between align-items-center">
                                    <div>
                                        <button id="btnGenrerarVenta" class="btn btn-success" type="submit" name="accion" value="GenerarVenta" class=""/>Generar Venta</button>

                                        <a href="" class="btn btn-danger">Cancelar</a>
                                    </div>                            
                                    <div class="d-flex align-items-center">
                                        <label class="mb-0">Total a pagar: </label>
                                        <input type="text" style="font-weight: bold" readonly="true" name="txttotal" value="Gs. " 
                                               class="form-control ml-2" placeholder="Total a pagar">
                                    </div>
                                </div>
                            </div>
                            <!--</div>-->
                        </div>
                        </body>
                </main>
                <footer class="py-4 bg-light mt-auto">
                    <div class="container-fluid px-4">
                        <div class="d-flex align-items-center justify-content-between small">
                            <div class="text-muted">Copyright &copy; Your Website 2023</div>
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
//          Inicializamos la tabla de pedidos
            $(document).ready(function () {
                $('#tablaModalPedidos').DataTable({
//                    dom: 'Bfrtip', // Permite usar botones de exportación
//                    buttons: [
//                        'copy', // Copiar al portapapeles
//                        'excelHtml5', // Exportar a Excel
//                        'pdfHtml5', // Exportar a PDF
//                        'print' // Imprimir
//                    ],
                    //Inicializador del buscadr por cada columna
                    initComplete: function () {
                        this.api()
                            .columns()
                            .every(function () {
                            var column = this;
                            var title = column.footer().textContent;

                            // Verifica si la columna es "Acciones" o contiene en el class no-search
                            if (title !== "Acciones" && !$(column.header()).hasClass('no-search') && !$(column.footer()).hasClass('no-search')) {
                                // Crea un input y añade el event listener
                                $('<input style="width: 100%" type="text" placeholder="Buscar ' + title + '" />')
                                    .appendTo($(column.footer()).empty())
                                    .on('keyup change clear', function () {
                                        if (column.search() !== this.value) {
                                            column.search(this.value).draw();
                                        }
                                    });
                            } else {
                                // Si es la columna "Acciones" o tiene la clase 'no-search', no crea el input
                                $(column.footer()).empty(); // Limpia el footer si es necesario
                            }
                        });
                    },
                    //Lenguaje que apunta al idioma
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    //Tamaño de las olumnas
                    //                columns: [{ width: '80%' }, null, null, null, null]
                });
            });
            //Otra manera de inicializar la tabla y ponerle lenguaje
            //                var table = new DataTable('#tablaPrueba', {
            //                language: {
            //                    url: "DataTables 2/es-ES.json",
            //                },
            //                });
            
//          Inicializamos la tabla de articulos
            $(document).ready(function () {
                $('#tablaModalArticulos').DataTable({
//                    dom: 'Bfrtip', // Permite usar botones de exportación
//                    buttons: [
//                        'copy', // Copiar al portapapeles
//                        'excelHtml5', // Exportar a Excel
//                        'pdfHtml5', // Exportar a PDF
//                        'print' // Imprimir
//                    ],
                    //Inicializador del buscadr por cada columna
                    initComplete: function () {
                        this.api()
                                .columns()
                                .every(function () {
                                    var column = this;
                                    var title = column.footer().textContent;

                                    // Crea un input y add event listener
                                    $('<input style="width: 100%" type="text" placeholder="Buscar ' + title + '" />')
                                            .appendTo($(column.footer()).empty())
                                            .on('keyup change clear', function () {
                                                if (column.search() !== this.value) {
                                                    column.search(this.value).draw();
                                                }
                                            });
                                });
                    },
                    //Lenguaje que apunta al idioma
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    //Tamaño de las olumnas
                    //                columns: [{ width: '80%' }, null, null, null, null]
                });
            });
            //Otra manera de inicializar la tabla y ponerle lenguaje
            //                var table = new DataTable('#tablaPrueba', {
            //                language: {
            //                    url: "DataTables 2/es-ES.json",
            //                },
            //                });

            //Tabla 2
            $(document).ready(function () {
                $('#tablaPrueba2').DataTable({
                    dom: 'Bfrtip', // Permite usar botones de exportación
                    buttons: [
                        'copy', // Copiar al portapapeles
                        'excelHtml5', // Exportar a Excel
                        'pdfHtml5', // Exportar a PDF
                        'print' // Imprimir
                    ],
                    //Inicializador del buscadr por cada columna
                    initComplete: function () {
                        this.api()
                            .columns()
                            .every(function () {
                                var column = this;
                                var title = column.footer().textContent;

                                // Verifica si la columna es "Acciones" o contiene en el class no-search
                                if (title !== "Acciones" && !$(column.header()).hasClass('no-search') && !$(column.footer()).hasClass('no-search')) {
                                    // Crea un input y añade el event listener
                                    $('<input style="width: 100%" type="text" placeholder="Buscar ' + title + '" />')
                                        .appendTo($(column.footer()).empty())
                                        .on('keyup change clear', function () {
                                            if (column.search() !== this.value) {
                                                column.search(this.value).draw();
                                            }
                                        });
                                } else {
                                    // Si es la columna "Acciones" o tiene la clase 'no-search', no crea el input
                                    $(column.footer()).empty(); // Limpia el footer si es necesario
                                }
                        });
                    },
                    //Lenguaje que apunta al idioma
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    //Tamaño de las olumnas
                    //                columns: [{ width: '80%' }, null, null, null, null]
                });
            });
            //Otra manera de inicializar la tabla y ponerle lenguaje
            //                var table = new DataTable('#tablaPrueba', {
            //                language: {
            //                    url: "DataTables 2/es-ES.json",
            //                },
            //                });
        </script>

        <!--codigo para mostrar mensaje-->
        <% String Message = (String) request.getAttribute("Message");%>
        <% String tipoAlert = (String) request.getAttribute("tipoAlert");%>
        <c:if test="${not empty Message}">
            <div id="mensaje" class="alert <%= tipoAlert != null ? tipoAlert : "alert-info"%>"
                 style="position:absolute; top: 80px; right: 10px; opacity: 80%; transition: opacity 1s ease; min-width: 200px;" role="alert">
                <%= Message%>
                <button type="button" style="border: none; width: 25px; height: 25px; float:right; display:inline-block; padding:0px 5px;" 
                        class="btn <%= tipoAlert != null ? tipoAlert + " btn-close" : "alert-info"%>" data-bs-dismiss="alert" aria-label="Close">
                    <!--<a href="#" class="close" data-dismiss="alert" aria-label="close"></a>-->
                </button>
            </div>
        </c:if>
        <script>
            //    setTimeout("document.getElementById('mensaje').style.visibility='hidden'", 7000);
            setTimeout(function () {
                var mensaje = document.getElementById('mensaje');
                mensaje.style.opacity = '0'; //cambia la opacidad a 0
                setTimeout(function () {
                    mensaje.style.display = 'none'; //se oculta el div después de la transición
                }, 1000);
            }, 7000); // Espera 7 segundos antes de comenzar a desvanecer
        </script>   
    </body>
</html>
