<%-- 
    Document   : base
    Created on : 7/03/2025, 10:37:36 PM
    Author     : Miguel
--%>

<!--bloque de codigo que le dice que no guarde en cache los datos para que al volver atras no 
haga como que al sesion sigue activa-->
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // Prohibir el almacenamiento en caché
%>

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
        <title>Mi título de página aquí</title>
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
                            <span style="height: 100%; width: 100%; background-color: yellow"><h1 style="text-align: center"><strong>MI TITULO</strong></h1></span>
                        </div>
                        <!--linea debajo del titulo-->
                        <!--<hr style="border: 2px solid black; background-color: black;">--> 
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div> 
                        
<!--                        <h1 class="mt-4">Dashboard</h1>
                        <ol class="breadcrumb mb-4">
                            <li class="breadcrumb-item active">Dashboard</li>
                        </ol>-->
                        <div class="row">
                            <div class="col-xl-3 col-md-6">
                                <div class="card bg-primary text-white mb-4">
                                    <div class="card-body"><h1 style="margin-left: 10px;margin-top: 5px">
                                            <strong>Compra <span style="float: right;font-size: 64px;opacity: 0.8" >
                                                    <i class="fa-solid fa-cart-shopping"></i></span></strong></h1></div>
                                    <div class="card-footer d-flex align-items-center justify-content-between">
                                        <a class="small text-white stretched-link" href="#">View Details</a>
                                        <div class="small text-white"><i class="fas fa-angle-right"></i></div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xl-3 col-md-6">
                                <div class="card bg-warning text-white mb-4">
                                    <div class="card-body"><h1 style="margin-left: 10px;margin-top: 5px">
                                            <strong>Tesorería <span style="float: right;font-size: 64px;opacity: 0.8" >
                                                    <i class="fa-solid fa-hand-holding-dollar"></i></span></strong></h1></div>
                                    <div class="collapse card-footer d-flex align-items-center justify-content-between"  aria-labelledby="headingTwo" data-bs-parent="#sidenavAccordion" >
                                        <nav class="sb-sidenav-menu-nested nav accordion" id="TesorerianavAccordionPages">
                                            <a class="nav-link collapsed small text-white stretched-link" data-bs-toggle="collapse" data-bs-target="#TesoreriaPagesCollapse"
                                               aria-expanded="false" aria-controls="TesoreriaPagesCollapse" href="#">View Details</a>
                                            <div class="small text-white"><i class="fas fa-angle-right"></i></div>
                                            <div class="collapse" id="TesoreriaPagesCollapse" aria-labelledby="headingOne" data-bs-parent="#TesorerianavAccordionPages">
                                                <nav class="sb-sidenav-menu-nested nav">
                                                    <a class="nav-link text-white" href="login.html">Login</a>
                                                    <a class="nav-link text-white" href="register.html">Register</a>
                                                    <a class="nav-link text-white" href="password.html">Forgot Password</a>
                                                    <a class="nav-link text-white" href="login.html">Login</a>
                                                    <a class="nav-link text-white" href="register.html">Register</a>
                                                    <a class="nav-link text-white" href="password.html">Forgot Password</a>
                                                </nav>
                                            </div>
                                        </nav>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xl-3 col-md-6">
                                <div class="card bg-success text-white mb-4">
                                    <div class="card-body"><h1 style="margin-left: 10px;margin-top: 5px">
                                            <strong>Venta <span style="float: right;font-size: 64px;opacity: 0.8" >
                                                    <i class="fa-solid fa-money-bill"></i></span></strong></h1></div>
                                    <div class="card-footer d-flex align-items-center justify-content-between">
                                        <a class="small text-white stretched-link" href="#">View Details</a>
                                        <div class="small text-white"><i class="fas fa-angle-right"></i></div>
                                    </div>
                                </div>
                            </div>
                            <div class="col-xl-3 col-md-6">
                                <div class="card bg-danger text-white mb-4">
                                    <div class="card-body"><h1 style="margin-left: 10px;margin-top: 5px">
                                            <strong>Stock <span style="float: right;font-size: 64px;opacity: 0.8" >
                                                    <i class="fa-solid fa-boxes-stacked"></i></span></strong></h1></div>
                                    <div class="card-footer d-flex align-items-center justify-content-between">
                                        <a class="small text-white stretched-link" href="#">View Details</a>
                                        <div class="small text-white"><i class="fas fa-angle-right"></i></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-xl-6">
                                <div class="card mb-4">
                                    <div class="card-header">
                                        <i class="fas fa-chart-area me-1"></i>
                                        Ejemplo de campos y boton
                                    </div>
                                    <div class="card-body">

                                        <div class="card-header"><h3 class="text-center font-weight-light my-4">Create Account</h3></div>
                                        <div class="card-body">
                                            <form>
                                                <div class="row mb-3">
                                                    <div class="col-md-6">
                                                        <div class="form-floating mb-3 mb-md-0">
                                                            <input class="form-control" id="inputFirstName" type="text" placeholder="Enter your first name" />
                                                            <label for="inputFirstName">First name</label>
                                                        </div>
                                                    </div>
                                                    <div class="col-md-6">
                                                        <div class="form-floating">
                                                            <input class="form-control" id="inputLastName" type="text" placeholder="Enter your last name" />
                                                            <label for="inputLastName">Last name</label>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="form-floating mb-3">
                                                    <input class="form-control" id="inputEmail" type="email" placeholder="name@example.com" />
                                                    <label for="inputEmail">Email address</label>
                                                </div>
                                                <div class="row mb-3">
                                                    <div class="col-md-6">
                                                        <div class="form-floating mb-3 mb-md-0">
                                                            <input class="form-control" id="inputPassword" type="password" placeholder="Create a password" />
                                                            <label for="inputPassword">Password</label>
                                                        </div>
                                                    </div>
                                                    <div class="col-md-6">
                                                        <div class="form-floating mb-3 mb-md-0">
                                                            <input class="form-control" id="inputPasswordConfirm" type="password" placeholder="Confirm password" />
                                                            <label for="inputPasswordConfirm">Confirm Password</label>
                                                        </div>
                                                    </div>
                                                </div>
                                                <div class="mt-4 mb-0">
                                                    <div class="d-grid"><a class="btn btn-primary btn-block" href="login.html">Create Account</a></div>
                                                </div>
                                            </form>
                                        </div>
                                        <div class="card-footer text-center py-3">
                                            <div class="small"><a href="login.html">Have an account? Go to login</a></div>
                                        </div>

                                    </div>
                                </div>
                            </div>

                            <div class="col-xl-6">
                                <div class="card mb-4">
                                    <div class="card-header">
                                        <i class="fas fa-chart-bar me-1"></i>
                                        Ejemplo de DataTable 1
                                    </div>
                                    <div class="card-body">
                                        <table id="tablaPrueba1" class="table table-bordered">
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
                                                <tr class="table-danger">
                                                    <td class="text-center">1</td>
                                                    <td class="text-center">Winner SRL</td>
                                                    <td class="text-center">Coca Cola</td>
                                                    <td class="text-center">12000</td>
                                                    <td class="text-center">05/11/2024</td>
                                                </tr>
                                                <tr class="table-success">
                                                    <td class="text-center">2</td>
                                                    <td class="text-center">Despensa GyG</td>
                                                    <td class="text-center">Jugo</td>
                                                    <td class="text-center">10000</td>
                                                    <td class="text-center">04/11/2024</td>
                                                </tr>
                                                <tr class="table-primary">
                                                    <td class="text-center">3</td>
                                                    <td class="text-center">Winner SRL</td>
                                                    <td class="text-center">Vino</td>
                                                    <td class="text-center">18000</td>
                                                    <td class="text-center">21/11/2024</td>
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
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="card mb-4">
                            <div class="card-header">
                                <i class="fas fa-table me-1"></i>
                                Ejemplo de DataTable 1
                            </div>
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

        <!--codigo para mostrar mensaje-->
        <% String Message = (String) request.getAttribute("Message");%>
        <% String tipoAlert = (String) request.getAttribute("tipoAlert");%>
    <c:if test="${not empty Message}">
        <div id="mensaje" class="alert <%= tipoAlert != null ? tipoAlert : "alert-info"%>"
             style="position:absolute; top: 80px; right: 10px; opacity: 90%; transition: opacity 1s ease; min-width: 200px;" role="alert">
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
