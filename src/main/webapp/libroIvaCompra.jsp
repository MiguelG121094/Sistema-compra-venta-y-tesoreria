<%--
    Document   : libroIvaCompra
    Informe de Libro de Compras (Ley 125/91). Filtro de período y tipo de comprobante,
    y la grilla con el formato del informe: un renglón por fila de libro_iva_compra.
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
        <title>Libro de Compras</title>
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
                                <h1 style="text-align: center"><strong>LIBRO DE COMPRAS LEY 125/91</strong></h1></span>
                        </div>
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <!-- Datos de la empresa: van en duro hasta que exista una tabla de empresa -->
                        <div class="d-flex justify-content-between mb-3">
                            <div><strong>INVERSIONES ALCAR S.A.</strong></div>
                            <div><strong>RUC:</strong> 80104885-0</div>
                            <div>
                                <strong>DESDE:</strong> <fmt:formatDate value="${desdeFecha}" pattern="dd/MM/yyyy"/>
                                <strong>HASTA:</strong> <fmt:formatDate value="${hastaFecha}" pattern="dd/MM/yyyy"/>
                            </div>
                        </div>

                        <!-- Filtros -->
                        <div class="card mb-4">
                            <div class="card-body">
                                <form action="LibroIvaCompraServlet?menu=LibroIvaCompra" method="POST">
                                    <div class="row">
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fechaDesde" name="fechaDesde"
                                                       type="date" value="${fechaDesde}" required />
                                                <label for="fechaDesde">Fecha desde</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fechaHasta" name="fechaHasta"
                                                       type="date" value="${fechaHasta}" required />
                                                <label for="fechaHasta">Fecha hasta</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="origen" name="origen">
                                                    <option value="" ${empty origen ? 'selected' : ''}>Todos</option>
                                                    <option value="FACTURA" ${origen eq 'FACTURA' ? 'selected' : ''}>Factura</option>
                                                    <option value="NOTA_CRED" ${origen eq 'NOTA_CRED' ? 'selected' : ''}>Nota de Crédito</option>
                                                    <option value="NOTA_DEBI" ${origen eq 'NOTA_DEBI' ? 'selected' : ''}>Nota de Débito</option>
                                                </select>
                                                <label for="origen">Comprobante</label>
                                            </div>
                                        </div>
                                        <div class="col-md-3 d-flex align-items-center">
                                            <button type="submit" class="btn btn-primary">Consultar</button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>

                        <!-- Grilla -->
                        <div class="card mb-4">
                            <div class="card-body table-responsive">
                                <table id="tablaLibroIva" class="table table-bordered table-striped">
                                    <thead>
                                        <tr>
                                            <th class="text-bg-dark text-center">N° Comprobante</th>
                                            <th class="text-bg-dark text-center">Fecha</th>
                                            <th class="text-bg-dark text-center">Tipo</th>
                                            <th class="text-bg-dark text-center">Timbrado</th>
                                            <th class="text-bg-dark text-center">R.U.C.</th>
                                            <th class="text-bg-dark text-center">Razón Social</th>
                                            <th class="text-bg-dark text-center">Gravadas 10%</th>
                                            <th class="text-bg-dark text-center">I.V.A. 10%</th>
                                            <th class="text-bg-dark text-center">Gravadas 5%</th>
                                            <th class="text-bg-dark text-center">I.V.A. 5%</th>
                                            <th class="text-bg-dark text-center">Exentas</th>
                                            <th class="text-bg-dark text-center">TOTAL</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="l" items="${listaLibroIva}">
                                            <tr>
                                                <td>${l.numeroComprobante}</td>
                                                <td class="text-center"><fmt:formatDate value="${l.fecha}" pattern="dd/MM/yyyy"/></td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${l.origen eq 'NOTA_CRED'}">NOTA DE CRÉDITO</c:when>
                                                        <c:when test="${l.origen eq 'NOTA_DEBI'}">NOTA DE DÉBITO</c:when>
                                                        <c:otherwise>FACTURA</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center">${l.timbrado}</td>
                                                <td class="text-center">${l.proveedor.ruc}</td>
                                                <td>${l.proveedor.razonSocial}</td>
                                                <td class="text-end"><fmt:formatNumber value="${l.gravada10}" pattern="#,##0"/></td>
                                                <td class="text-end"><fmt:formatNumber value="${l.iva10}" pattern="#,##0"/></td>
                                                <td class="text-end"><fmt:formatNumber value="${l.gravada5}" pattern="#,##0"/></td>
                                                <td class="text-end"><fmt:formatNumber value="${l.iva5}" pattern="#,##0"/></td>
                                                <td class="text-end"><fmt:formatNumber value="${l.exenta}" pattern="#,##0"/></td>
                                                <td class="text-end"><fmt:formatNumber value="${l.total}" pattern="#,##0"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                    <tfoot>
                                        <tr>
                                            <th colspan="6" class="text-end">T O T A L   G E N E R A L</th>
                                            <th class="text-end"><fmt:formatNumber value="${totales.gravada10}" pattern="#,##0"/></th>
                                            <th class="text-end"><fmt:formatNumber value="${totales.iva10}" pattern="#,##0"/></th>
                                            <th class="text-end"><fmt:formatNumber value="${totales.gravada5}" pattern="#,##0"/></th>
                                            <th class="text-end"><fmt:formatNumber value="${totales.iva5}" pattern="#,##0"/></th>
                                            <th class="text-end"><fmt:formatNumber value="${totales.exenta}" pattern="#,##0"/></th>
                                            <th class="text-end"><fmt:formatNumber value="${totales.total}" pattern="#,##0"/></th>
                                        </tr>
                                    </tfoot>
                                </table>
                            </div>
                        </div>
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
                $('#tablaLibroIva').DataTable({
                    dom: 'Bfrtip', // Permite usar botones de exportación
                    buttons: [
                        'copy', // Copiar al portapapeles
                        'excelHtml5', // Exportar a Excel
                        'pdfHtml5', // Exportar a PDF
                        'print' // Imprimir
                    ],
                    // El pie lleva los totales, no tiene que ordenarse ni buscarse con las filas
                    order: [],
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
