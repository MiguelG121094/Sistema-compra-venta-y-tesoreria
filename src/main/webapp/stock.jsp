<%--
    Document   : stock
    Puntos de reposición por depósito: la mínima y la máxima de cada artículo.
    Grilla editable que se graba entera con un solo botón. El stock actual va de sólo lectura:
    lo mueven el trigger de la factura de compra y el ajuste de stock.
--%>
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>
<%@ page import="modelo.Usuario" %>
<%
    HttpSession sessionObj = request.getSession(false);
    if (sessionObj == null || sessionObj.getAttribute("usuario") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Usuario usuario = (Usuario) sessionObj.getAttribute("usuario");
%>
<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
    <jsp:include page="header.jsp" />
    <head>
        <title>Stock</title>
    </head>
    <body class="sb-nav-fixed">
        <jsp:include page="menuSuperior.jsp" />
        <div id="layoutSidenav">
            <jsp:include page="menuLateral.jsp" />
            <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <!-- Título -->
                        <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                            <span style="height: 100%; width: 100%; background-color: yellow">
                                <h1 style="text-align: center"><strong>STOCK</strong></h1></span>
                        </div>
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <!-- Sucursal y depósito -->
                        <div class="card mb-4">
                            <div class="card-body">
                                <form action="StockServlet?menu=Stock" method="POST">
                                    <div class="row">
                                        <div class="col-md-5">
                                            <%-- Arranca en la sucursal del usuario y se puede cambiar:
                                                 es una pantalla de consulta, no un documento --%>
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="idSucursal" name="idSucursal"
                                                        onchange="this.form.submit();">
                                                    <c:forEach var="suc" items="${listaSucursales}">
                                                        <option value="${suc.getIdSucursal()}"
                                                            ${idSucursal == suc.getIdSucursal() ? 'selected' : ''}>
                                                            ${suc.getDescripcion()}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <label for="idSucursal">Sucursal</label>
                                            </div>
                                        </div>
                                        <div class="col-md-5">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="idDeposito" name="idDeposito"
                                                        onchange="this.form.submit();">
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="dep" items="${listaDepositos}">
                                                        <option value="${dep.getIdDeposito()}"
                                                            ${idDeposito == dep.getIdDeposito() ? 'selected' : ''}>
                                                            ${dep.getDescripcion()}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <label for="idDeposito">Depósito</label>
                                            </div>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>

                        <!-- Grilla -->
                        <c:if test="${not empty idDeposito}">
                            <form action="StockServlet?menu=Stock" method="POST">
                                <input type="hidden" name="accion" value="Guardar">
                                <input type="hidden" name="idSucursal" value="${idSucursal}">
                                <input type="hidden" name="idDeposito" value="${idDeposito}">

                                <div class="card mb-4">
                                    <div class="card-body table-responsive">
                                        <table id="tablaStock" class="table table-bordered w-100">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Artículo</th>
                                                    <th class="text-bg-dark text-center">Código</th>
                                                    <th class="text-bg-dark text-center">Stock actual</th>
                                                    <th class="text-bg-dark text-center">Cantidad mínima</th>
                                                    <th class="text-bg-dark text-center">Cantidad máxima</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="stk" items="${listaStock}" varStatus="pos">
                                                    <%-- Los índices van por posición: la grilla se graba entera --%>
                                                    <tr>
                                                        <td>
                                                            <input type="hidden" name="idArticulo" value="${stk.getArticulo().getIdArticulo()}">
                                                            ${stk.getArticulo().getDescripcion()}
                                                        </td>
                                                        <td class="text-center">${stk.getArticulo().getCodigo()}</td>
                                                        <td class="text-end">
                                                            <fmt:formatNumber value="${empty stk.getStockActual() ? 0 : stk.getStockActual()}" pattern="#,##0"/>
                                                        </td>
                                                        <td>
                                                            <input type="number" min="0" class="form-control form-control-sm text-end"
                                                                   name="minima_${pos.index}"
                                                                   value="${empty stk.getCantidadMinima() ? 0 : stk.getCantidadMinima()}"
                                                                   <c:if test="${not puedeEditar}">readonly</c:if>>
                                                        </td>
                                                        <td>
                                                            <input type="number" min="0" class="form-control form-control-sm text-end"
                                                                   name="maxima_${pos.index}"
                                                                   value="${empty stk.getCantidadMaxima() ? 0 : stk.getCantidadMaxima()}"
                                                                   <c:if test="${not puedeEditar}">readonly</c:if>>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="card-footer">
                                        <button type="submit" class="btn btn-success"
                                                <c:if test="${not puedeEditar}">disabled title="No tiene permisos"</c:if>>Guardar</button>
                                        <a href="StockServlet?menu=Stock&accion=Listar&idSucursal=${idSucursal}&idDeposito=${idDeposito}"
                                           class="btn btn-danger">Cancelar</a>
                                    </div>
                                </div>
                            </form>
                        </c:if>
                    </div>
                </main>
                <footer class="py-4 bg-light mt-auto">
                    <div class="container-fluid px-4">
                        <div class="d-flex align-items-center justify-content-between small">
                            <div class="text-muted">Copyright &copy; Sistema Compras y Tesorería 2025</div>
                            <div><a href="#">Privacy Policy</a> &middot; <a href="#">Terms &amp; Conditions</a></div>
                        </div>
                    </div>
                </footer>
            </div>
        </div>

        <script>
            $(document).ready(function () {
                $('#tablaStock').DataTable({
                    // Sin paginado: los campos de las otras páginas salen del DOM y no se grabarían.
                    paging: false,
                    scrollY: '55vh',
                    scrollCollapse: true,
                    autoWidth: false,
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
