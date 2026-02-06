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
        <title>Artículo</title>
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
                        <!--titulo de la pagina--> 
                        <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                            <span style="height: 100%; width: 100%; background-color: yellow">
                                <h1 style="text-align: center">
                                    <strong>ARTÍCULO</strong></h1></span>
                        </div>
                        <!--linea debajo del titulo-->
                        <!--<hr style="border: 2px solid black; background-color: black;">--> 
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div> 

                        <!--                        <h1 class="mt-4">Dashboard</h1>
                                                <ol class="breadcrumb mb-4">
                                                    <li class="breadcrumb-item active">Dashboard</li>
                                                </ol>-->

                        <div class="d-flex">
                            <!-- Formulario -->
                            <div class="card col-sm-4">
                                <div class="card-body">
                                    <form action="Controlador?menu=Cliente" method="POST">
                                        <div class="mb-3">
                                            <label for="txtnombre" class="form-label">Nombre</label>
                                            <input type="text" class="form-control" id="txtnombre" name="txtnombre" value="" required="true">
                                        </div>
                                        <div class="mb-3">
                                            <label for="txtapellido" class="form-label">Apellidos</label>
                                            <input type="text" class="form-control" id="txtapellido" name="txtapellido" value="" required="true">
                                        </div>
                                        <div class="mb-3">
                                            <label for="txtcedula" class="form-label">Cedula</label>
                                            <input type="text" class="form-control" id="txtcedula" name="txtcedula" value="" required="true">
                                        </div>
                                        <div class="mb-3">
                                            <label for="txtdireccion" class="form-label">Direccion</label>
                                            <input type="text" class="form-control" id="txtdireccion" name="txtdireccion" value="" required="true">
                                        </div>
                                        <div class="mb-3">
                                            <label for="txttelefono" class="form-label">Telefono</label>
                                            <input type="text" class="form-control" id="txttelefono" name="txttelefono" value="" required="true">
                                        </div>
                                        <div class="mb-3">
                                            <label for="txtruc" class="form-label">Ruc</label>
                                            <input type="text" class="form-control" id="txtruc" name="txtruc" value="" required="true">
                                        </div>

                                        <button type="submit" name="accion" value="Insertar" class="btn btn-success">Agregar</button>

                                        <button type="submit" name="accion" value="Actualizar" class="btn btn-primary">Actualizar</button>

                                        <a href="TipoArticuloServlet?menu=tipoArticulo&accion=Cancelar" class="btn btn-danger">Cancelar</a>

                                    </form>
                                </div>
                            </div>

                            <!-- Tabla -->
<!--                            <div class="card col-8" style="margin-left: 30px;width: 65%;">
                                <div class="card-body" style="height: 10px">-->
                            <div class="col-sm-7" style="margin-left: 30px;width: 65%;">
                                <div class="card">
                                    <div class="card-body">
                                    <table id="tablaPrueba2" class="table table-bordered">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <tr>
                                                <td class="text-center">1</td>
                                                <td class="text-center">Winner SRL</td>
                                                <td class="text-center">Coca Cola</td>
                                                <td class="text-center">12.000</td>
                                                <td class="text-center">05/11/2024</td>
                                            </tr>
                                            <tr>
                                                <td class="text-center">2</td>
                                                <td class="text-center">Despensa GyG</td>
                                                <td class="text-center">Jugo</td>
                                                <td class="text-center">10.000</td>
                                                <td class="text-center">04/11/2024</td>
                                            </tr>
                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Venta</th>
                                                <th class="text-bg-dark text-center">Cliente</th>
                                                <th class="text-bg-dark text-center">Producto</th>
                                                <th class="text-bg-dark text-center">Total</th>
                                                <th class="text-bg-dark text-center">Fecha</th>
                                            </tr>
                                            </tbody>
                                    </table>
 <!--                               <table class="table table-hover table-bordered table-striped">
                                        <thead>
                                            <tr class="bg-dark">
                                                <th class="text-center text-light">Id</th>
                                                <th class="text-center text-light">Nombre</th>
                                                <th class="text-center text-light">Apellido</th>
                                                <th class="text-center text-light">Cédula</th>
                                                <th class="text-center text-light">Dirección</th>
                                                <th class="text-center text-light">Teléfono</th>
                                                <th class="text-center text-light">Ruc</th>
                                                <th class="text-center text-light">Acciones</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                    <%--<c:forEach var="c" items="${cliente}">--%>
                                        <tr>
                                            <td class="text-center">${c.getId()}</td>
                                            <td class="text-center">${c.getNombre()}</td>
                                            <td class="text-center">${c.getApellido()}</td>
                                            <td class="text-center">${c.getCi()}</td>
                                            <td class="text-center">${c.getDireccion()}</td>
                                            <td class="text-center">${c.getTelefono()}</td>
                                            <td class="text-center">${c.getRuc()}</td>
                                            <td class="text-center">
                                                <a href="Controlador?menu=Cliente&accion=Editar&id=${c.getId()}" class="btn btn-warning">Editar</a>
                                                <a href="Controlador?menu=Cliente&accion=Eliminar&id=${c.getId()}" class="btn btn-danger" >Eliminar</a>
                                            </td>
                                        </tr>
                                            <%--</c:forEach>--%>
                                        </tbody>
                                    </table>-->
                                    </div>
                                </div>
                            </div>
                    </div>
                </div>
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
            $(document).ready(function () {
                $('#tablaPrueba1').DataTable({
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
        </script>

        <!-- Código para mostrar mensajes con Toastr -->
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
