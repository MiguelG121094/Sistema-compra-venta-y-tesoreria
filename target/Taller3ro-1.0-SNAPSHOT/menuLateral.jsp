<%-- 
    Document   : menuLateral
    Created on : 8/03/2025, 12:35:21 AM
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
<body>
    <div id="layoutSidenav_nav">
        <nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
            <div class="sb-sidenav-menu">
                <div class="nav">
                    <div class="sb-sidenav-menu-heading">Vista principal</div>
                    <a class="nav-link" href="base.jsp">
                        <div class="sb-nav-link-icon"><i class="fas fa-tachometer-alt"></i></div>
                        Menú principal
                    </a>
                    <div class="sb-sidenav-menu-heading">Módulo Compras</div>
                    <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapsePaginasCompras" aria-expanded="false" aria-controls="collapsePaginasCompras">
                        <div class="sb-nav-link-icon"><i class="fas fa-book-open"></i></div>
                        Referenciales
                        <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                    </a>
                    <div class="collapse" id="collapsePaginasCompras" aria-labelledby="headingTwo" data-bs-parent="#sidenavAccordion">
                        <nav class="sb-sidenav-menu-nested nav accordion" id="sidenavAccordeonPaginasCompra">
                            <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#paginasCollapseReferencialesCompra" aria-expanded="false" aria-controls="paginasCollapseReferencialesCompra">
                                Referenciales
                                Compras
                                <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                            </a>
                            <div class="collapse" id="paginasCollapseReferencialesCompra" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordeonPaginasCompra">
                                <nav class="sb-sidenav-menu-nested nav">
                                    <a class="nav-link" href="TipoArticuloServlet?menu=TipoArticulo&accion=Listar">Tipo de articulo</a>
                                    <a class="nav-link" href="marca.jsp">Marca</a>
                                    <a class="nav-link" href="articulo.jsp">Articulos</a>
                                </nav>
                            </div>
                            <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#pagesCollapseError" aria-expanded="false" aria-controls="pagesCollapseError">
                                Modelos base para vista
                                <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                            </a>
                            <div class="collapse" id="pagesCollapseError" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordeonPaginasCompra">
                                <nav class="sb-sidenav-menu-nested nav">
                                    <a class="nav-link" href="baseReferenciales.jsp">Base Referencial</a>
                                    <a class="nav-link" href="baseMovimientos.jsp">Base Movimiento</a>
                                    <!--<a class="nav-link" href="500.html">500 Page</a>-->
                                </nav>
                            </div>
                        </nav>
                    </div>
                    <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapseCompraMoviminetos" aria-expanded="false" aria-controls="collapseCompraMoviminetos">
                        <div class="sb-nav-link-icon"><i class="fas fa-columns"></i></div>
                        Movimientos
                        <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                    </a>
                    <div class="collapse" id="collapseCompraMoviminetos" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordion">
                        <nav class="sb-sidenav-menu-nested nav">
                            <a class="nav-link" href="PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal">Pedido de compra</a>
                            <a class="nav-link" href="PresupuestoServlet?menu=Presupuesto&accion=ListarModal">Presupuesto del proveedor</a>
                            <a class="nav-link" href="layout-sidenav-light.html">Orden de compra</a>
                            <a class="nav-link" href="facturaCompra.jsp">Factura compra</a>
                            <a class="nav-link" href="layout-sidenav-light.html">Notas de Crédito y Débito</a>
                        </nav>
                    </div>
                    
                    <div class="sb-sidenav-menu-heading">Módulo Tesorería</div>
                    <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapsePages" aria-expanded="false" aria-controls="collapsePages">
                        <div class="sb-nav-link-icon"><i class="fas fa-book-open"></i></div>
                        Referenciales
                        <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                    </a>
                    <div class="collapse" id="collapsePages" aria-labelledby="headingTwo" data-bs-parent="#sidenavAccordion">
                        <nav class="sb-sidenav-menu-nested nav accordion" id="sidenavAccordionPages">
                            <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#pagesCollapseAuth" aria-expanded="false" aria-controls="pagesCollapseAuth">
                                Referenciales
                                Tesorería
                                <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                            </a>
                            <div class="collapse" id="pagesCollapseAuth" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordionPages">
                                <nav class="sb-sidenav-menu-nested nav">
                                    <a class="nav-link" href="tipoArticulo.jsp">Agregar Banco/a>
                                    <a class="nav-link" href="register.html">Agregar Cuenta</a>
                                    <!--<a class="nav-link" href="password.html">Forgot Password</a>-->
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
                    <a class="nav-link collapsed" href="#" data-bs-toggle="collapse" data-bs-target="#collapseLayouts" aria-expanded="false" aria-controls="collapseLayouts">
                        <div class="sb-nav-link-icon"><i class="fas fa-columns"></i></div>
                        Movimientos
                        <div class="sb-sidenav-collapse-arrow"><i class="fas fa-angle-down"></i></div>
                    </a>
                    <div class="collapse" id="collapseLayouts" aria-labelledby="headingOne" data-bs-parent="#sidenavAccordion">
                        <nav class="sb-sidenav-menu-nested nav">
                            <a class="nav-link" href="layout-static.html">Provision de Cta. Pagar</a>
                            <a class="nav-link" href="layout-sidenav-light.html">Orden de Pago</a>
<!--                            <a class="nav-link" href="layout-sidenav-light.html">Orden de compra</a>
                            <a class="nav-link" href="layout-sidenav-light.html">Factura compra</a>
                            <a class="nav-link" href="layout-sidenav-light.html">Notas de Crédito y Débito</a>-->
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
            <div class="sb-sidenav-footer align-content-center" style="text-align: center">
                <div class="small">Logueado como: </div>
                <strong><%= usuario.getUsername().toUpperCase()%></strong>
                <!--Sistema Compras y Tesorería-->
            </div>
        </nav>
    </div>
</body>
