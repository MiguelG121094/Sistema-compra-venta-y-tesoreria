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

<!--obtenerr la fecha actual y formatearla-->
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="modelo.PedidoCompra"%>
<%
    // para obtener la fecah formateada del pedido
    PedidoCompra pedidoCompra = (PedidoCompra) request.getAttribute("pedidoCompra");
    String fechaFormateada = "";
    if (pedidoCompra != null) {
        Date fecha = pedidoCompra.getFecha();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        fechaFormateada = formato.format(fecha); //fehca formateada
    }
%>

<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

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
                                <div class="modal-dialog modal-dialog modal-xl">
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
                                                            <th class="text-bg-dark text-center">Nro Pedido</th>
                                                            <th class="text-bg-dark text-center">Solicitante</th>
                                                            <th class="text-bg-dark text-center">Sucursal</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="PedCompra" items="${listPedCompraConDetalle}">
                                                            <tr class="${PedCompra.getEstado() eq 'Anulado' ? 'table-danger' : ''}">
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getIdPedido()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getUsuario().getPersona().getNombre()} 
                                                                    ${PedCompra.getUsuario().getPersona().getApellido()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getSucursal().getDescripcion()}</td>
                                                                <td align="center" valign="middle" class="text-center">
                                                                    <fmt:formatDate value="${PedCompra.getFecha()}" pattern="dd-MM-yyyy" /></td>
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getEstado()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getListaArticulos()}</td>
                                                                <td align="center" valign="middle" class="text-center">
                                                                <form action="PedidoCompraServlet?menu=PedidoCompra" method="POST">
                                                                    <input type="hidden" name="idPedCompraCab" value="${PedCompra.getIdPedido()}">
                                                                    <button name="accion" value="CargarPedidoCompra" type="submit" class="btn btn-primary" 
                                                                            <c:if test="${PedCompra.getEstado() eq 'Anulado'}"><c:out value="disabled='disabled'"/></c:if>>Seleccionar</button>
                                                                </form>
<!--                                                                    <a href="PedidoCompraServlet?menu=PedidoCompra&accion=CargarPedidoCompra&idPedCompraCab=${PedCompra.getIdPedido()}"
                                                                        class="btn btn-primary disable">Seleccionar</a>-->
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Pedido</th>
                                                            <th class="text-bg-dark text-center">Solicitante</th>
                                                            <th class="text-bg-dark text-center">Sucursal</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
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
                                                            <th class="text-bg-dark text-center">Nro Art.</th>
                                                            <th class="text-bg-dark text-center">Tipo de Articulo</th>
                                                            <th class="text-bg-dark text-center">Marca</th>
                                                            <th class="text-bg-dark text-center">Dscripción</th>
                                                            <th class="text-bg-dark text-center">Presentación</th>
                                                            <th class="text-bg-dark text-center">Precio compra</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>

                                                    <tbody>
                                                        <c:forEach var="listAticulos" items="${listaAticulos}">
                                                            <tr>
                                                                <td class="text-center">${listAticulos.getIdArticulo()}</td>
                                                                <td class="text-center">${listAticulos.getTipoArticulo().getDescripcion()}</td>
                                                                <td class="text-center">${listAticulos.getMarca().getDescripcion()}</td>
                                                                <td class="text-center">${listAticulos.getDescripcion()}</td>
                                                                <td class="text-center">${listAticulos.getDescripcion()}</td>
                                                                <td class="text-center">${listAticulos.getPrecioCompra()}</td>
                                                                <td class="text-center">
                                                                    <a href="PedidoCompraServlet?menu=PedidoCompra&accion=CargarArticulo&idArt=${listAticulos.getIdArticulo()}" 
                                                                       class="btn btn-primary">Seleccionar</a>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Art.</th>
                                                            <th class="text-bg-dark text-center">Tipo de Articulo</th>
                                                            <th class="text-bg-dark text-center">Marca</th>
                                                            <th class="text-bg-dark text-center">Dscripción</th>
                                                            <th class="text-bg-dark text-center">Presentación</th>
                                                            <th class="text-bg-dark text-center">Precio compra</th>
                                                            <th class="text-bg-dark text-center">Acciones</th>
                                                        </tr>
                                                    </tfoot>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a href="MenuPrincipal.jsp" class="btn btn-success">Agregar Artículo</a>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-auto">
                                <form action="PedidoCompraServlet?menu=PedidoCompra" method="POST">
                                    <input type="hidden" name="idUsuario" value="<%= usuario.getIdUsuario() %>">
                                    <button name="accion" value="Nuevo" type="submit" class="btn btn-success">Nuevo</button>
                                    <a href="" data-bs-toggle="modal" data-bs-target="#modalPedidos" class="btn btn-info text-white">Buscar Pedido</a>
                                    <button name="accion" value="Anular" type="button" data-bs-toggle="modal" 
                                        data-bs-target="#modalAnular${pedidoCompra.getIdPedido()}" class="btn btn-danger" 
                                        <c:if test="${pedidoCompra.getIdPedido() == null}"><c:out value="disabled='disabled'"/></c:if>>Anular</button>
                                </form>
                                <!-- Modal de confirmacion sin javascript  -->
                                <div class="modal fade" id="modalAnular${pedidoCompra.getIdPedido()}" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                  <div class="modal-dialog modal-dialog-centered">
                                    <div class="modal-content">
                                      <div class="modal-header">
                                        <h1 class="modal-title fs-5">Confirmación</h1>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                      </div>
                                      <div class="modal-body">
                                        ¿Desea anular el pedido de compra?
                                      </div>
                                      <div class="modal-footer">
                                        <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                        <form action="PedidoCompraServlet?menu=PedidoCompra" method="POST">
                                          <input type="hidden" name="accion" value="Anular">
                                          <input type="hidden" name="id" value="${pedidoCompra.getIdPedido()}">
                                          <button type="submit" class="btn btn-primary">Sí</button>
                                        </form>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                            </div>
                        </div>

                        <!-- Cabecera -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <h3>Cabecera</h3>

                                <div class="row" style="margin-top: 23px">
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label style="white-space: nowrap;" class="me-2">Pedido N°:</label>
                                        <input type="text" value="${pedidoCompra.getIdPedido() == null ? newIdPedido : pedidoCompra.getIdPedido()}" class="form-control" disabled="">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Usuario:</label>
                                        <input type="text" value="${pedidoCompra.getUsuario().getUsername()}"
                                               class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha:</label>
                                        <input value="<%=fechaFormateada %>" class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Sucursal:</label>
                                            <form id="fromProcesarSucursal" action="PedidoCompraServlet?menu=PedidoCompra&accion=CargarDeposito" method="POST">
                                            <!-- select de Sucursales -->
                                            <!--si tiene cargado el id pedido y la lista de pedido detalle está vacia habilita, 
                                            pero si el id pedido está cargado y la lista de detalle pedido tiene algo cargado desabilita-->
                                            <select data-bs-toggle="tooltip" data-bs-custom-class="custom-tooltip"
                                                    id="selectSucursal" name="idSucursal" class="form-control" onchange="this.form.submit()"
                                                <c:choose>
                                                    <c:when test="${(not empty pedidoCompra.idPedido and empty listPedCompDetalle) 
                                                                    or (not empty newIdPedido and empty listPedCompDetalle)}">
                                                    </c:when>
                                                    <c:otherwise>
                                                        disabled
                                                    </c:otherwise>
                                                </c:choose>>
                                                <option value="">Seleccionar sucursal</option>
                                                    <c:forEach var="sucursal" items="${listaSucursales}">
                                                        <option value="${sucursal.getIdSucursal()}" ${sucursal.getIdSucursal() == idSucursalSeleccionada ? 'selected' : ''}>
                                                            ${sucursal.getDescripcion()}</option>
                                                    </c:forEach>
                                            </select>
                                            </form>
                                                <!--script para iniciallizar tooltipo y mostrar desde el servlet 
                                                (servlet PedidoCompraServlet -> request.setAttribute("mensajeTooltip", "Debe seleccionar una sucursal");)-->
                                                <script>
                                                    // inicializar tooltips general para el vista completa
                                                    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                                                    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                                                        return new bootstrap.Tooltip(tooltipTriggerEl);
                                                    });
                                                </script>
                                                <!--script para mostrar el tooltip -->
                                                <c:if test="${mostrarTooltip}">
                                                <script>
                                                    document.addEventListener("DOMContentLoaded", function() {
                                                        //el id del elemento en el cual se quiere mostrar el tooltip
                                                        var tooltip = new bootstrap.Tooltip(document.getElementById('selectSucursal'), {
                                                            title: "${mensajeTooltip}",
                                                            placement: "top"
                                                        });
                                                        tooltip.show(); //mostrar el tooltip

                                                        //ocultar el tooltip después de 3 segundos
                                                        setTimeout(function() {
                                                            tooltip.hide();
                                                        }, 3000);
                                                    });
                                                </script>
                                                </c:if>
    
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Estado:</label>
                                        <input type="text" value="${pedidoCompra.getEstado()}" class="form-control" disabled="true">
                                    </div>
                                </div>

                                <form action="PedidoCompraServlet?menu=PedidoCompra" method="POST">
                                    <div class="row" style="margin-top: 23px">
                                        <div class="col-auto">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <button type="button" href="" data-bs-toggle="modal" data-bs-target="#modalArticulos" class="btn btn-outline-primary"
                                                        <c:if test="${pedidoCompra.getIdPedido() == null && newIdPedido == null && listaDepositos.size()<=0 || empty listaDepositos}">
                                                            <c:out value="disabled='disabled'"/></c:if>>Buscar Artículo</button>
                                            </div>
                                        </div>
                                        <div class="col-md-1">
                                            <input type="text" name="txtIdArticulo" placeholder="Id. Artículo" value="${artACargar.getIdArticulo()}" class="form-control" disabled="true">
                                        </div>
                                        <div class="col-md-4">
                                            <input type="text" placeholder="Descripción" value="${artACargar.getDescripcion()}" class="form-control"  disabled="true">
                                        </div>
                                        <div class="col-md-1">
                                            <input type="number" name="txtCantidad" min="1" <c:if test="${artACargar.getIdArticulo() == null}"><c:out 
                                                            value="disabled='disabled'"/></c:if>  placeholder="Cantidad" class="form-control" required="true">
                                        </div>
                                        <div class="col-md-2">
                                            <select id="selectDeposito" required="true" name="idDeposito" class="form-control" ${empty listaDepositos ? 'disabled' : ''}
                                                    <c:if test="${artACargar.getIdArticulo() == null}"><c:out 
                                                            value="disabled='disabled'"/></c:if>>
                                                <option value="">Seleccione un depósito</option>
                                                <c:forEach var="depositos" items="${listaDepositos}">
                                                    <option value="${depositos.getIdDeposito()}" ${depositos.getIdDeposito() == idDepositoSeleccionado ? 'selected' : ''}>
                                                        ${depositos.getDescripcion()}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-2">
                                            <button type="submit" name="accion" value="AgregarArticuloADetalle"
                                                class="btn btn-success" <c:if test="${artACargar.getIdArticulo() == null}"><c:out 
                                                value="disabled='disabled'"/></c:if>>Agregar Artículo
                                            </button>  
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>

                        <!-- Tabla de Artículos -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <!--<h2>Artículos Agregados</h2>-->
                                <!--<div class="card mb-4">-->
                                <div class="card-body">
                                    <table id="tablaPrueba2" class="table table-bordered">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Articulo</th>
                                                <th class="text-bg-dark text-center">Descripción</th>
                                                <th class="text-bg-dark text-center">Deposito destino</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <c:forEach var="listaPedCompDetalle" items="${listPedCompDetalle}">
                                                <tr>
                                                    <td class="text-center">${listaPedCompDetalle.getArticulo().getIdArticulo()}</td>
                                                    <td class="text-center">${listaPedCompDetalle.getArticulo().getDescripcion()}</td>
                                                    <td class="text-center">${listaPedCompDetalle.getDeposito().getDescripcion()}</td>
                                                    <td class="text-center">${listaPedCompDetalle.getCantidad()}</td>
                                                    <td class="text-center">
                                                        <a href="PedidoCompraServlet?menu=PedidoCompra&accion=EliminarArticuloList&idArt=${listaPedCompDetalle.getArticulo().getIdArticulo()}" 
                                                           class="btn btn-danger">Eliminar</a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Articulo</th>
                                                <th class="text-bg-dark text-center">Descripción</th>
                                                <th class="text-bg-dark text-center">Deposito destino</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </tfoot>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="card-footer d-flex  justify-content-between align-items-center">
                                    <div>
                                        <!--<button id="btnGenrerarVenta" class="btn btn-success" type="submit" name="accion" value="GenerarVenta" class=""/>Generar Pedido</button>-->
                                        <button href="#" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#modalGenerarPedido${listaPedCompDetalle}"
                                                <c:if test="${newIdPedido == null || empty listPedCompDetalle}"><c:out value="disabled='disabled'"/></c:if>>Guardar</button>
                                        <!-- Modal de confirmacion sin javascript  -->
                                        <div class="modal fade" id="modalGenerarPedido${listaPedCompDetalle}" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                          <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content">
                                              <div class="modal-header">
                                                <h1 class="modal-title fs-5">Confirmación</h1>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                              </div>
                                              <div class="modal-body">
                                                ¿Desea generar un nuevo pedido de compra?
                                              </div>
                                              <div class="modal-footer">
                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                <form action="PedidoCompraServlet?menu=PedidoCompra" method="POST">
                                                  <input type="hidden" name="accion" value="PersistirPedido">
                                                  <input type="hidden" name="id" value="${listaPedCompDetalle}">
                                                  <button type="submit" class="btn btn-primary">Sí</button>
                                                </form>
                                              </div>
                                            </div>
                                          </div>
                                        </div>

                                        <a href="PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal" class="btn btn-danger">Cancelar</a>
                                    </div>                            
<!--                                    <div class="d-flex align-items-center">
                                        <label class="mb-0">Total a pagar: </label>
                                        <input type="text" style="font-weight: bold" readonly="true" name="txttotal" value="Gs. " 
                                               class="form-control ml-2" placeholder="Total a pagar">
                                    </div>-->
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
                    // Asegurar que los anchos no sean sobrescritos
                    autoWidth: false,
                    // Tamaño de las columnas
                    columns: [
                        {width: '3%'}, // Primer columna
                        null, // Segunda columna
                        null, // Tercera columna
                        null, // Cuarta columna
                        null, // Quinta columna
                        {width: '27%'}, // Sexta columna
                        null // Séptima columna
                    ]
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
                    columns: [null, null, null, null, null, null,null]
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
                 style="position:absolute; top: 80px; right: 10px; opacity: 100%; transition: opacity 1s ease; min-width: 200px;" role="alert">
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
