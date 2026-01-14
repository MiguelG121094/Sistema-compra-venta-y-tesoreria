<%-- 
    Document   : index
    Created on : 5/11/2024, 10:17:01 PM
    Author     : Miguel
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Mi título de página aquí</title>
        <!-- JQuery -->
        <script src="DataTables 2/js/jquery-3.7.1.js" type="text/javascript"></script>

        <!-- Bootstrap (CSS y JS) -->
        <script src="Bootstrap 5.3.3/js/bootstrap.bundle.min.js" type="text/javascript"></script>
        <link href="Bootstrap 5.3.3/css/bootstrap.css" rel="stylesheet" type="text/css"/>

        <!-- DataTables (CSS y JS principales) -->
        <script src="DataTables 2/js/dataTables.js" type="text/javascript"></script>
        <script src="DataTables 2/js/dataTables.bootstrap5.js" type="text/javascript"></script>
        <link href="DataTables 2/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
        <link href="DataTables 2/css/dataTables.bootstrap5.css" rel="stylesheet" type="text/css"/>

        <!-- Botones para exportación en DataTables (CSS y JS) -->
        <script src="DataTables 2/js/dataTables.buttons.js" type="text/javascript"></script>
        <script src="DataTables 2/js/buttons.bootstrap5.js" type="text/javascript"></script>
        <script src="DataTables 2/js/jszip.min.js" type="text/javascript"></script>
        <script src="DataTables 2/js/pdfmake.min.js" type="text/javascript"></script>
        <script src="DataTables 2/js/vfs_fonts.js" type="text/javascript"></script>
        <script src="DataTables 2/js/buttons.html5.min.js" type="text/javascript"></script>
        <script src="DataTables 2/js/buttons.print.min.js" type="text/javascript"></script>
        <link href="DataTables 2/css/buttons.bootstrap5.css" rel="stylesheet" type="text/css"/>

        <!-- Archivos para Tema (CSS y JS) -->
        <link href="Theme/css/style.min.css" rel="stylesheet" type="text/css"/>
        <link href="Theme/css/styles.css" rel="stylesheet" type="text/css"/>
        <script src="Theme/js/fontawesome-all.js" type="text/javascript"></script>
        <script src="Theme/js/scripts.js" type="text/javascript"></script>
        <!--<script src="Theme/js/Chart.min.js" type="text/javascript"></script>-->
        <!--<script src="Theme/assets/demo/chart-area-demo.js" type="text/javascript"></script>
        <script src="Theme/assets/demo/chart-bar-demo.js" type="text/javascript"></script>-->
        <!--<script src="Theme/js/simple-datatables.min.js" type="text/javascript"></script>-->
        <!--<script src="Theme/js/datatables-simple-demo.js" type="text/javascript"></script>-->
    </head>
    <body class="sb-nav-fixed">
        <nav class="sb-topnav navbar navbar-expand navbar-dark bg-dark">
            <!-- Navbar Brand-->
            <a class="navbar-brand ps-3" href="index.html">Start Bootstrap</a>
            <!-- Sidebar Toggle-->
            <button class="btn btn-link btn-sm order-1 order-lg-0 me-4 me-lg-0" id="sidebarToggle" href="#!"><i class="fas fa-bars"></i></button>
            <!-- Navbar Search-->
            <form class="d-none d-md-inline-block form-inline ms-auto me-0 me-md-3 my-2 my-md-0">
                <div class="input-group">
                    <input class="form-control" type="text" placeholder="Search for..." aria-label="Search for..." aria-describedby="btnNavbarSearch" />
                    <button class="btn btn-primary" id="btnNavbarSearch" type="button"><i class="fas fa-search"></i></button>
                </div>
            </form>
            <!-- Navbar-->
            <ul class="navbar-nav ms-auto ms-md-0 me-3 me-lg-4">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" id="navbarDropdown" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false"><i class="fas fa-user fa-fw"></i></a>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                        <li><a class="dropdown-item" href="#!">Settings</a></li>
                        <li><a class="dropdown-item" href="#!">Activity Log</a></li>
                        <li><hr class="dropdown-divider" /></li>
                        <li><a class="dropdown-item" href="#!">Logout</a></li>
                    </ul>
                </li>
            </ul>
        </nav>
        <div id="layoutSidenav">
            <div id="layoutSidenav_nav">
                <nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
                    <div class="sb-sidenav-menu">
                        <div class="nav">
                            <div class="sb-sidenav-menu-heading">Core</div>
                            <a class="nav-link" href="index.html">
                                <div class="sb-nav-link-icon"><i class="fas fa-tachometer-alt"></i></div>
                                Dashboard
                            </a>
                            <div class="sb-sidenav-menu-heading">Interface</div>
                            <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapseLayouts" aria-expanded="false" aria-controls="collapseLayouts">
                                <div class="sb-nav-link-icon"><i class="fas fa-columns"></i></div>
                                Layouts
                                <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                            </a>
                            <div class="collapse" id="collapseLayouts" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordion">
                                <nav class="sb-sidenav-menu-nested nav">
                                    <a class="nav-link" href="layout-static.html">Static Navigation</a>
                                    <a class="nav-link" href="layout-sidenav-light.html">Light Sidenav</a>
                                </nav>
                            </div>
                            <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapsePages" aria-expanded="false" aria-controls="collapsePages">
                                <div class="sb-nav-link-icon"><i class="fas fa-book-open"></i></div>
                                Pages
                                <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                            </a>
                            <div class="collapse" id="collapsePages" aria-labelledby="headingTwo" data-bs-parent="#sidenavAccordion">
                                <nav class="sb-sidenav-menu-nested nav accordion" id="sidenavAccordionPages">
                                    <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#pagesCollapseAuth" aria-expanded="false" aria-controls="pagesCollapseAuth">
                                        Authentication
                                        <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                                    </a>
                                    <div class="collapse" id="pagesCollapseAuth" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordionPages">
                                        <nav class="sb-sidenav-menu-nested nav">
                                            <a class="nav-link" href="login.html">Login</a>
                                            <a class="nav-link" href="register.html">Register</a>
                                            <a class="nav-link" href="password.html">Forgot Password</a>
                                        </nav>
                                    </div>
                                    <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#pagesCollapseError" aria-expanded="false" aria-controls="pagesCollapseError">
                                        Error
                                        <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                                    </a>
                                    <div class="collapse" id="pagesCollapseError" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordionPages">
                                        <nav class="sb-sidenav-menu-nested nav">
                                            <a class="nav-link" href="401.html">401 Page</a>
                                            <a class="nav-link" href="404.html">404 Page</a>
                                            <a class="nav-link" href="500.html">500 Page</a>
                                        </nav>
                                    </div>
                                </nav>
                            </div>
                            <div class="sb-sidenav-menu-heading">Addons</div>
                            <a class="nav-link" href="charts.html">
                                <div class="sb-nav-link-icon"><i class="fas fa-chart-area"></i></div>
                                Charts
                            </a>
                            <a class="nav-link" href="tables.html">
                                <div class="sb-nav-link-icon"><i class="fas fa-table"></i></div>
                                Tables
                            </a>
                        </div>
                    </div>
                    <div class="sb-sidenav-footer">
                        <div class="small">Logged in as:</div>
                        Start Bootstrap
                    </div>
                </nav>
            </div>
        <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <h1 class="mt-4">Dashboard</h1>
                        <ol class="breadcrumb mb-4">
                            <li class="breadcrumb-item active">Dashboard</li>
                        </ol>
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
                                    <div class="card-footer d-flex align-items-center justify-content-between">
                                        <a class="small text-white stretched-link" href="#">View Details</a>
                                        <div class="small text-white"><i class="fas fa-angle-right"></i></div>
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
                                        Area Chart Example
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
                                        Bar Chart Example
                                    </div>
                                    <div class="card-body">
                                        
                                    <div class="card-header"><h3 class="text-center font-weight-light my-4">Login</h3></div>
                                    <div class="card-body">
                                        <form>
                                            <div class="form-floating mb-3">
                                                <input class="form-control" id="inputEmail" type="email" placeholder="name@example.com" />
                                                <label for="inputEmail">Email address</label>
                                            </div>
                                            <div class="form-floating mb-3">
                                                <input class="form-control" id="inputPassword" type="password" placeholder="Password" />
                                                <label for="inputPassword">Password</label>
                                            </div>
                                            <div class="form-check mb-3">
                                                <input class="form-check-input" id="inputRememberPassword" type="checkbox" value="" />
                                                <label class="form-check-label" for="inputRememberPassword">Remember Password</label>
                                            </div>
                                            <div class="d-flex align-items-center justify-content-between mt-4 mb-0">
                                                <a class="small" href="password.html">Forgot Password?</a>
                                                <a class="btn btn-primary" href="index.html">Login</a>
                                            </div>
                                        </form>
                                    </div>
                                    <div class="card-footer text-center py-3">
                                        <div class="small"><a href="register.html">Need an account? Sign up!</a></div>
                                    </div>
                                        
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        
                        <div class="row">
                            <div class="col-xl-6">
                                <div class="card mb-4">
                                    <div class="card-header">
                                        <i class="fas fa-chart-area me-1"></i>
                                        Area Chart Example
                                    </div>
                                    <div class="card-body">
                                    
                                    </div>
                                </div>
                            </div>
                        
                        
                        
                        <div class="col-xl-6">
                                <div class="card mb-4">
                                    <div class="card-header">
                                        <i class="fas fa-chart-bar me-1"></i>
                                        Bar Chart Example
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
                                DataTable Example
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
            
  </body>
</html>
