<%-- 
    Document   : notaRemision
    Created on : 17/02/2026, 11:28:11 PM
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
        <title>Nota de Remisión</title>
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
                        <!-- Título -->
                        <div class="row mb-4">
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <span style="height: 100%; width: 100%;">
                                    <h1 style="text-align: center">
                                        <strong>NOTA DE REMISIÓN</strong>
                                    </h1>
                                </span>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>
                        </div>

                        <form method="post" action="notaRemision.jsp">
                            <!-- Cabecera - Primera fila -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <h3 class="section-title">Cabecera</h3>
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating">
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
                                                    <input class="form-control" id="emisor" type="text" placeholder="Emisor" />
                                                    <label for="emisor">Emisor</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Segunda fila - Depósito destino y Receptor -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <select class="form-control" id="depositoDestino" name="depositoDestino">
                                                        <option>Seleccionar depósito</option>
                                                        <option>Depósito Asunción 1</option>
                                                        <option>Depósito Asunción 2</option>
                                                        <option>Depósito Central</option>
                                                        <option>Depósito Encarnación</option>
                                                    </select>
                                                    <label for="depositoDestino">Depósito destino</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="receptor" type="text" placeholder="Receptor" />
                                                    <label for="receptor">Receptor</label>
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
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Tercera fila - Timbrado y fechas -->
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
                                                    <input class="form-control" id="fechaInicioTimb" type="date" placeholder="Fecha de inicio" />
                                                    <label for="fechaInicioTimb">Fecha de inicio</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="fechaVencimientoTimb" type="date" placeholder="Fecha de vencimiento" />
                                                    <label for="fechaVencimientoTimb">Fecha de vencimiento</label>
                                                </div>
                                            </div>
                                            <div class="col-md-4">
                                                <div class="form-floating">
                                                    <input class="form-control" id="obs" type="text" placeholder="Observaciones" />
                                                    <label for="obs">Observaciones</label>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Cuarta fila - Vehículo y Conductor -->
                            <div class="row mb-4">
                                <div class="col custom-card">
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="vehiculo" type="text" placeholder="Vehículo" />
                                                    <label for="vehiculo">Vehículo</label>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="form-floating">
                                                    <input class="form-control" id="conductor" type="text" placeholder="Conductor" />
                                                    <label for="conductor">Conductor</label>
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
                                            <div class="col-md-auto">
                                                <div class="mb-3 mb-md-0">
                                                    <button type="button" data-bs-toggle="modal" 
                                                        data-bs-target="#modalArticulos" class="btn btn-outline-primary">
                                                        <span class="d-none d-md-inline">Buscar Artículo</span>
                                                        <span class="d-md-none">Buscar</span>
                                                    </button>
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
                                                <button type="button" class="btn btn-success w-100">Agregar Producto</button>
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
                                    <table id="tablaArticulosRemision" class="table table-bordered table-sm">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Artículo</th>
                                                <th class="text-bg-dark text-center">Descripción</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td class="text-center">2</td>
                                                <td class="text-center">Jugo de Naranja</td>
                                                <td class="text-center">24</td>
                                                <td class="text-center">
                                                    <button class="btn btn-warning btn-sm">Editar</button>
                                                    <button class="btn btn-danger btn-sm">Eliminar</button>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="text-center">3</td>
                                                <td class="text-center">Licor de Coco</td>
                                                <td class="text-center">10</td>
                                                <td class="text-center">
                                                    <button class="btn btn-warning btn-sm">Editar</button>
                                                    <button class="btn btn-danger btn-sm">Eliminar</button>
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>

                                <!-- Botones finales -->
                                <div class="row mt-3">
                                    <div class="col-md-6">
                                        <button class="btn btn-success">Registrar</button>
                                        <button class="btn btn-secondary">Cancelar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal para Buscar Artículos -->
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
                                                        <th class="text-bg-dark text-center">Stock</th>
                                                        <th class="text-bg-dark text-center no-search">Acciones</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <tr>
                                                        <td class="text-center">2</td>
                                                        <td class="text-center">Jugo de Naranja</td>
                                                        <td class="text-center">100</td>
                                                        <td class="text-center">
                                                            <button class="btn btn-primary btn-sm">Seleccionar</button>
                                                        </td>
                                                    </tr>
                                                    <tr>
                                                        <td class="text-center">3</td>
                                                        <td class="text-center">Licor de Coco</td>
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
                // Inicializar tabla de artículos de remisión
                $('#tablaArticulosRemision').DataTable({
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

                // Inicializar modal de artículos
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
