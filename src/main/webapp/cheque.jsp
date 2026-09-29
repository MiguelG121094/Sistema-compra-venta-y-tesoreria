<%--
    Document   : cheque
    Gestión de cheques emitidos (Tesorería, §G3). Grilla de consulta con dos acciones:
    anular el cheque y registrar su entrega al proveedor. Los cheques se emiten desde la
    Orden de Pago, acá no se dan de alta.
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
        <title>Cheques</title>
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
                                <h1 style="text-align: center"><strong>CHEQUES</strong></h1></span>
                        </div>
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <div class="card mb-4">
                            <div class="card-body table-responsive">
                                <table id="tablaCheques" class="table table-bordered table-striped">
                                    <thead>
                                        <tr>
                                            <th class="text-bg-dark text-center">N°</th>
                                            <th class="text-bg-dark text-center">Banco</th>
                                            <th class="text-bg-dark text-center">Cuenta</th>
                                            <th class="text-bg-dark text-center">Emisión</th>
                                            <th class="text-bg-dark text-center">Pago</th>
                                            <th class="text-bg-dark text-center">A la orden</th>
                                            <th class="text-bg-dark text-center">Importe</th>
                                            <th class="text-bg-dark text-center">Estado</th>
                                            <th class="text-bg-dark text-center">O.P.</th>
                                            <th class="text-bg-dark text-center">Proveedor</th>
                                            <th class="text-bg-dark text-center">Entrega</th>
                                            <th class="text-bg-dark text-center">Retirado por</th>
                                            <th class="text-bg-dark text-center no-search">Acción</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="ch" items="${listaCheques}">
                                            <tr class="${ch.estado eq 'Anulado' ? 'table-danger' : ''}">
                                                <td class="text-center">${ch.numero}</td>
                                                <td>${ch.chequera.cuenta.entidadFinanciera.nombre}</td>
                                                <td class="text-center">${ch.chequera.cuenta.numero}</td>
                                                <td class="text-center"><fmt:formatDate value="${ch.fechaEmision}" pattern="dd/MM/yyyy"/></td>
                                                <td class="text-center"><fmt:formatDate value="${ch.fechaPago}" pattern="dd/MM/yyyy"/></td>
                                                <td>${ch.aLaOrden}</td>
                                                <td class="text-end"><fmt:formatNumber value="${ch.monto}" pattern="#,##0"/></td>
                                                <td class="text-center">${ch.estado}</td>
                                                <td class="text-center">${ch.ordenPago.numero}</td>
                                                <td>${ch.proveedor}</td>
                                                <td class="text-center"><fmt:formatDate value="${ch.fechaEntrega}" pattern="dd/MM/yyyy"/></td>
                                                <td>${ch.entregadoA}</td>
                                                <td class="text-center">
                                                    <c:if test="${ch.estado ne 'Anulado' and ch.estado ne 'Cobrado'}">
                                                        <button type="button" class="btn btn-primary btn-sm"
                                                                onclick="abrirModalEntrega(${ch.idCheque}, '${ch.numero}')">
                                                            Entrega
                                                        </button>
                                                        <button type="button" class="btn btn-danger btn-sm"
                                                                onclick="abrirModalAnular(${ch.idCheque}, '${ch.numero}')">
                                                            Anular
                                                        </button>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        <!-- Modal Registrar entrega -->
                        <div class="modal fade" id="modalEntrega" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <form action="ChequeServlet?menu=Cheque" method="POST">
                                        <input type="hidden" name="accion" value="RegistrarEntrega" />
                                        <input type="hidden" name="idCheque" id="entregaIdCheque" />
                                        <div class="modal-header">
                                            <h5 class="modal-title">Registrar entrega del cheque <span id="entregaNumero"></span></h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="form-floating mb-3">
                                                <input class="form-control" id="fechaEntrega" name="fechaEntrega" type="date" required />
                                                <label for="fechaEntrega">Fecha de entrega</label>
                                            </div>
                                            <div class="form-floating mb-3">
                                                <input class="form-control" id="entregadoA" name="entregadoA" type="text" maxlength="80" />
                                                <label for="entregadoA">Retirado por</label>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="submit" class="btn btn-primary">Guardar</button>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar anulación -->
                        <div class="modal fade" id="modalAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <form action="ChequeServlet?menu=Cheque" method="POST">
                                        <input type="hidden" name="accion" value="Anular" />
                                        <input type="hidden" name="idCheque" id="anularIdCheque" />
                                        <div class="modal-header bg-danger text-white">
                                            <h5 class="modal-title">Confirmación</h5>
                                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                                        </div>
                                        <div class="modal-body">
                                            <p>¿Está seguro que desea anular el cheque <span id="anularNumero"></span>?</p>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="submit" class="btn btn-danger">Anular</button>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                                        </div>
                                    </form>
                                </div>
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
            function abrirModalEntrega(idCheque, numero) {
                document.getElementById('entregaIdCheque').value = idCheque;
                document.getElementById('entregaNumero').textContent = numero;
                new bootstrap.Modal(document.getElementById('modalEntrega')).show();
            }

            function abrirModalAnular(idCheque, numero) {
                document.getElementById('anularIdCheque').value = idCheque;
                document.getElementById('anularNumero').textContent = numero;
                new bootstrap.Modal(document.getElementById('modalAnular')).show();
            }

            $(document).ready(function () {
                $('#tablaCheques').DataTable({
                    dom: 'Bfrtip', // Permite usar botones de exportación
                    buttons: [
                        'copy', // Copiar al portapapeles
                        'excelHtml5', // Exportar a Excel
                        'pdfHtml5', // Exportar a PDF
                        'print' // Imprimir
                    ],
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
