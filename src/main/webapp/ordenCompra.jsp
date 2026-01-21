<%--
    Document   : ordenCompra
    Created on : 14/01/2026
    Author     : Miguel
--%>

<!--bloque de codigo que hace que las paginas JSP solo sean accesibles si el
usuario inicio sesion, se debe agregar esta validacion en cada una de las vistas JSP-->
<%@ page import="modelo.Usuario" %>
<%
    HttpSession sessionObj = request.getSession(false);
    if (sessionObj == null || sessionObj.getAttribute("usuario") == null) {
        response.sendRedirect("login.jsp");
    }

    Usuario usuario = (Usuario) sessionObj.getAttribute("usuario");
%>

<!--obtener la fecha actual y formatearla-->
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@page import="modelo.OrdenCompra"%>
<%
    // para obtener la fecha formateada de la orden
    OrdenCompra ordenCompra = (OrdenCompra) request.getAttribute("ordenCompra");
    String fechaFormateada = "";
    if (ordenCompra != null && ordenCompra.getFecha() != null) {
        Date fecha = ordenCompra.getFecha();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        fechaFormateada = formato.format(fecha);
    }
%>

<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<html>
    <!--incluir los scripts y estilos en el header-->
    <jsp:include page="header.jsp" />
    <head>
        <title>Orden de Compra</title>
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
        <!--Incluir menu superior-->
        <jsp:include page="menuSuperior.jsp" />
        <div id="layoutSidenav">
            <!--Incluir menu lateral-->
            <jsp:include page="menuLateral.jsp" />
            <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <!-- Titulo y botones -->
                        <div class="row mb-4">
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <span style="height: 100%; width: 100%; background-color: yellow">
                                    <h1 style="text-align: center">
                                        <strong>ORDEN DE COMPRA</strong></h1></span>
                            </div>
                            <!--linea debajo del titulo-->
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                            <!-- Modal Presupuestos -->
                            <div class="modal fade" id="modalPresupuestos" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-dialog modal-xl">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Presupuestos Disponibles</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                                <table id="tablaModalPresupuestos" class="table table-bordered table-striped">
                                                    <thead>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Presupuesto</th>
                                                            <th class="text-bg-dark text-center">Nro Pedido</th>
                                                            <th class="text-bg-dark text-center">Proveedor</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="PresupuestosConDet" items="${listaPresupuestosConDetalle}">
                                                            <tr class="${PresupuestosConDet.getEstado() eq 'Anulado' ? 'table-danger' : (PresupuestosConDet.getEstado() eq 'Orden Generada' ? 'table-secondary' : '')}">
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getIdPresupuesto()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getPedidoCompra().getIdPedido()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getProveedor().getRazonSocial()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getFecha()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getEstado()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getListaArticulos()}</td>
                                                                <td align="center" valign="middle" class="text-center">
                                                                <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                                                    <input type="hidden" name="idPresupuestoCab" value="${PresupuestosConDet.getIdPresupuesto()}">
                                                                    <button name="accion" value="CargarPresupuesto" type="submit" class="btn btn-primary"
                                                                            <c:if test="${PresupuestosConDet.getEstado() eq 'Anulado' || PresupuestosConDet.getEstado() eq 'Orden Generada'}">
                                                                                <c:out value="disabled='disabled'"/></c:if>>Seleccionar</button>
                                                                </form>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Presupuesto</th>
                                                            <th class="text-bg-dark text-center">Nro Pedido</th>
                                                            <th class="text-bg-dark text-center">Proveedor</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </tfoot>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a href="PresupuestoServlet?menu=Presupuesto&accion=ListarModal" class="btn btn-success">Ir a Presupuestos</a>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Modal Ordenes de Compra -->
                            <div class="modal fade" id="modalOrdenesCompra" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-dialog modal-xl">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Ordenes de Compra</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                                <table id="tablaModalOrdenesCompra" class="table table-bordered table-striped">
                                                    <thead>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Orden</th>
                                                            <th class="text-bg-dark text-center">Nro Presupuesto</th>
                                                            <th class="text-bg-dark text-center">Proveedor</th>
                                                            <th class="text-bg-dark text-center">Sucursal</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="OrdenCompraConDet" items="${listaOrdenesCompraConDetalle}">
                                                            <tr class="${OrdenCompraConDet.getEstado() eq 'Anulado' ? 'table-danger' : (OrdenCompraConDet.getEstado() eq 'Completado' || OrdenCompraConDet.getEstado() eq 'Aprobado' ? 'table-success' : '')}">
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getIdOrdenCompra()}</td>
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getPresupuesto().getIdPresupuesto()}</td>
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getProveedor().getRazonSocial()}</td>
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getSucursal().getDescripcion()}</td>
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getFecha()}</td>
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getEstado()}</td>
                                                                <td align="center" valign="middle" class="text-center">${OrdenCompraConDet.getListaArticulos()}</td>
                                                                <td align="center" valign="middle" class="text-center">
                                                                    <c:choose>
                                                                        <c:when test="${OrdenCompraConDet.getEstado() eq 'Pendiente'}">
                                                                            <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                                                                <input type="hidden" name="idOrdenCompra" value="${OrdenCompraConDet.getIdOrdenCompra()}">
                                                                                <button name="accion" value="CargarOrdenCompra" type="submit" class="btn btn-primary">Seleccionar</button>
                                                                            </form>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <button class="btn btn-secondary" disabled
                                                                                    title="${OrdenCompraConDet.getEstado() eq 'Anulado' ? 'Orden anulada' : 'Orden ya procesada'}">Seleccionar</button>
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Orden</th>
                                                            <th class="text-bg-dark text-center">Nro Presupuesto</th>
                                                            <th class="text-bg-dark text-center">Proveedor</th>
                                                            <th class="text-bg-dark text-center">Sucursal</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </tfoot>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Modal Sucursales -->
                            <div class="modal fade" id="modalSucursales" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Sucursales</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                                <table id="tablaModalSucursales" class="table table-bordered table-striped">
                                                    <thead>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Sucursal</th>
                                                            <th class="text-bg-dark text-center">Descripcion</th>
                                                            <th class="text-bg-dark text-center">Direccion</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="listSucursales" items="${listaSucursales}">
                                                            <tr>
                                                                <td class="text-center">${listSucursales.getIdSucursal()}</td>
                                                                <td class="text-center">${listSucursales.getDescripcion()}</td>
                                                                <td class="text-center">${listSucursales.getDireccion()}</td>
                                                                <td class="text-center">
                                                                    <a href="OrdenCompraServlet?menu=OrdenCompra&accion=CargarSucursal&idSucursal=${listSucursales.getIdSucursal()}"
                                                                       class="btn btn-primary">Seleccionar</a>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Sucursal</th>
                                                            <th class="text-bg-dark text-center">Descripcion</th>
                                                            <th class="text-bg-dark text-center">Direccion</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </tfoot>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-auto">
                                <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                    <input type="hidden" name="idUsuario" value="<%= usuario.getIdUsuario() %>">
                                    <button name="accion" value="Nuevo" type="submit" class="btn btn-success">Nuevo</button>
                                    <button name="accion" value="BuscarPresupuesto" type="button" data-bs-toggle="modal"
                                            data-bs-target="#modalPresupuestos" class="btn btn-info text-white"
                                            <c:if test="${newIdOrdenCompra == null}"><c:out value="disabled='disabled'"/></c:if>>Buscar Presupuesto</button>
                                    <a href="" data-bs-toggle="modal" data-bs-target="#modalOrdenesCompra" class="btn btn-info text-white">Buscar Orden</a>
                                    <button name="accion" value="Aprobar" type="submit" class="btn btn-primary"
                                       <c:if test="${ordenCompra.getIdOrdenCompra() == null || ordenCompra.getEstado() eq 'Aprobado' || ordenCompra.getEstado() eq 'Anulado'}">
                                           <c:out value="disabled='disabled'"/></c:if>>Aprobar</button>
                                    <button name="accion" value="Anular" type="button" data-bs-toggle="modal"
                                        data-bs-target="#modalAnular${ordenCompra.getIdOrdenCompra()}" class="btn btn-danger"
                                        <c:if test="${ordenCompra.getIdOrdenCompra() == null || ordenCompra.getEstado() eq 'Anulado'}">
                                            <c:out value="disabled='disabled'"/></c:if>>Anular</button>
                                </form>
                                <!-- Modal de confirmacion para anular -->
                                <div class="modal fade" id="modalAnular${ordenCompra.getIdOrdenCompra()}" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                  <div class="modal-dialog modal-dialog-centered">
                                    <div class="modal-content">
                                      <div class="modal-header">
                                        <h1 class="modal-title fs-5">Confirmacion</h1>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                      </div>
                                      <div class="modal-body">
                                        Desea anular la orden de compra?
                                      </div>
                                      <div class="modal-footer">
                                        <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                        <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                          <input type="hidden" name="accion" value="Anular">
                                          <input type="hidden" name="id" value="${ordenCompra.getIdOrdenCompra()}">
                                          <button type="submit" class="btn btn-primary">Si</button>
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
                                        <label style="white-space: nowrap;" class="me-2">Orden N:</label>
                                        <input type="text" value="${ordenCompra.getIdOrdenCompra() == null ? newIdOrdenCompra : ordenCompra.getIdOrdenCompra()}" class="form-control" disabled="">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Usuario:</label>
                                        <input type="text" value="${ordenCompra.getUsuario().getUsername()}"
                                               class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha:</label>
                                        <input value="<%=fechaFormateada %>" class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Estado:</label>
                                        <input type="text" value="${ordenCompra.getEstado()}" class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label style="white-space: nowrap;" class="me-2">Presupuesto N:</label>
                                        <input type="text" value="${ordenCompra.getPresupuesto().getIdPresupuesto()}" class="form-control" disabled="">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label style="white-space: nowrap;" class="me-2">Pedido N:</label>
                                        <input type="text" value="${ordenCompra.getPedidoCompra().getIdPedido()}" class="form-control" disabled="">
                                    </div>
                                </div>

                                <!-- Proveedor -->
                                <div class="row" style="margin-top: 23px">
                                    <div class="col-md-1">
                                        <input type="text" placeholder="Id. Prov" value="${proveedorSeleccionado.getIdProveedor()}"
                                               class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-3">
                                        <input type="text" placeholder="Razon Social" value="${proveedorSeleccionado.getRazonSocial()}"
                                               class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2">
                                        <input type="text" placeholder="Ruc" value="${proveedorSeleccionado.getRuc()}" class="form-control"
                                               disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Sucursal:</label>
                                        <form action="OrdenCompraServlet?menu=OrdenCompra&accion=CargarSucursal" method="POST">
                                            <select name="idSucursal" class="form-select" onchange="this.form.submit()"
                                                <c:if test="${newIdOrdenCompra == null && ordenCompra.getIdOrdenCompra() == null}">
                                                    disabled</c:if>>
                                                <option value="">Seleccionar sucursal</option>
                                                <c:forEach var="suc" items="${listaSucursales}">
                                                    <option value="${suc.getIdSucursal()}" ${suc.getIdSucursal() == sucursalSeleccionada.getIdSucursal() ? 'selected' : ''}>
                                                        ${suc.getDescripcion()}</option>
                                                </c:forEach>
                                            </select>
                                        </form>
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label style="white-space: nowrap;" class="me-2">Condición:</label>
                                        <form action="OrdenCompraServlet?menu=OrdenCompra&accion=GuardarCondicionCompra" method="POST">
                                            <select name="condicionCompra" class="form-select" onchange="this.form.submit()"
                                                <c:if test="${newIdOrdenCompra == null && ordenCompra.getIdOrdenCompra() == null}">
                                                    disabled</c:if>>
                                                <option value="">Seleccionar...</option>
                                                <option value="Contado" ${ordenCompra.getCondicionCompra() eq 'Contado' ? 'selected' : ''}>Contado</option>
                                                <option value="Credito" ${ordenCompra.getCondicionCompra() eq 'Credito' ? 'selected' : ''}>Crédito</option>
                                            </select>
                                        </form>
                                    </div>
                                </div>

                                <!-- Observacion -->
                                <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                    <div class="row" style="margin-top: 23px">
                                        <div class="col-md-8">
                                            <label class="me-2">Observacion:</label>
                                            <input type="text" name="txtObservacion" value="${ordenCompra.getObservacion()}"
                                                   class="form-control" placeholder="Observaciones..."
                                                   <c:if test="${newIdOrdenCompra == null && ordenCompra.getIdOrdenCompra() == null}">
                                                       <c:out value="disabled='disabled'"/></c:if>>
                                        </div>
                                        <div class="col-md-2 d-flex align-items-end">
                                            <button type="submit" name="accion" value="GuardarObservacion" class="btn btn-secondary"
                                                <c:if test="${newIdOrdenCompra == null && ordenCompra.getIdOrdenCompra() == null}">
                                                    <c:out value="disabled='disabled'"/></c:if>>Aplicar</button>
                                        </div>
                                    </div>
                                </form>

                                <!-- Articulo seleccionado para editar -->
                                <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                    <div class="row" style="margin-top: 23px">
                                        <div class="col-md-1">
                                            <input type="text" name="txtIdArticulo" placeholder="Id. Art" value="${ordenCompraDetSeleccionado.getArticulo().getIdArticulo()}"
                                                   class="form-control" disabled="true">
                                        </div>
                                        <div class="col-md-4">
                                            <input type="text" placeholder="Descripcion" value="${ordenCompraDetSeleccionado.getArticulo().getDescripcion()}"
                                                   class="form-control" disabled="true">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="number" name="txtCantidad" min="1"
                                                   value="${ordenCompraDetSeleccionado.getCantidad()}"
                                                   <c:if test="${ordenCompraDetSeleccionado.getArticulo().getIdArticulo() == null}">
                                                       <c:out value="disabled='disabled'"/></c:if> placeholder="Cantidad" class="form-control" required="true">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="number" name="txtPrecioCompra" min="1" value="${ordenCompraDetSeleccionado.getPrecioCompra()}"
                                                   <c:if test="${ordenCompraDetSeleccionado.getArticulo().getIdArticulo() == null}">
                                                       <c:out value="disabled='disabled'"/></c:if> placeholder="Precio Compra" class="form-control" required="true">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="hidden" name="idArt" value="${ordenCompraDetSeleccionado.getArticulo().getIdArticulo()}">
                                            <button type="submit" name="accion" value="ModificarArticuloDetalle"
                                                class="btn btn-warning" <c:if test="${ordenCompraDetSeleccionado.getArticulo().getIdArticulo() == null}">
                                                    <c:out value="disabled='disabled'"/></c:if>>Modificar
                                            </button>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </div>

                        <!-- Tabla de Articulos -->
                        <div class="row mb-4">
                            <div class="col custom-card">
                                <div class="card-body">
                                    <table id="tablaOrdenCompraDetalle" class="table table-bordered">
                                        <thead>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Articulo</th>
                                                <th class="text-bg-dark text-center">Descripcion</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center">Precio de compra (Gs.)</th>
                                                <th class="text-bg-dark text-center">Subtotal (Gs.)</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <c:set var="totalOrden" value="0" />
                                            <c:forEach var="listaOrdenDet" items="${listaOrdenCompraDetalle}">
                                                <c:set var="subtotal" value="${listaOrdenDet.getCantidad() * listaOrdenDet.getPrecioCompra()}" />
                                                <c:set var="totalOrden" value="${totalOrden + subtotal}" />
                                                <tr>
                                                    <td class="text-center">${listaOrdenDet.getArticulo().getIdArticulo()}</td>
                                                    <td class="text-center">${listaOrdenDet.getArticulo().getDescripcion()}</td>
                                                    <td class="text-center">${listaOrdenDet.getCantidad()}</td>
                                                    <td class="text-center">${listaOrdenDet.getPrecioCompra()}</td>
                                                    <td class="text-center">${subtotal}</td>
                                                    <td class="text-center">
                                                        <a href="OrdenCompraServlet?menu=OrdenCompra&accion=EditarArticuloList&idArt=${listaOrdenDet.getArticulo().getIdArticulo()}"
                                                           class="btn btn-warning btn-sm">Editar</a>
                                                        <a href="OrdenCompraServlet?menu=OrdenCompra&accion=EliminarArticuloList&idArt=${listaOrdenDet.getArticulo().getIdArticulo()}"
                                                           class="btn btn-danger btn-sm">Eliminar</a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Articulo</th>
                                                <th class="text-bg-dark text-center">Descripcion</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center">Precio de compra (Gs.)</th>
                                                <th class="text-bg-dark text-center">Subtotal (Gs.)</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                                <div class="card-footer d-flex justify-content-between align-items-center">
                                    <div>
                                        <button type="button" href="#" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#modalGenerarOrden"
                                           <c:if test="${newIdOrdenCompra == null || proveedorSeleccionado == null || sucursalSeleccionada == null || empty listaOrdenCompraDetalle}">
                                               <c:out value="disabled='disabled'"/></c:if>>Guardar</button>
                                        <!-- Modal de confirmacion para guardar -->
                                        <div class="modal fade" id="modalGenerarOrden" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                          <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content">
                                              <div class="modal-header">
                                                <h1 class="modal-title fs-5">Confirmacion</h1>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                              </div>
                                              <div class="modal-body">
                                                Desea generar una nueva orden de compra?
                                              </div>
                                              <div class="modal-footer">
                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                <form action="OrdenCompraServlet?menu=OrdenCompra" method="POST">
                                                  <input type="hidden" name="accion" value="PersistirOrdenCompra">
                                                  <button type="submit" class="btn btn-primary">Si</button>
                                                </form>
                                              </div>
                                            </div>
                                          </div>
                                        </div>

                                        <a href="OrdenCompraServlet?menu=OrdenCompra&accion=Cancelar" class="btn btn-danger">Cancelar</a>
                                    </div>
                                    <div class="d-flex align-items-center">
                                        <label class="mb-0 me-2"><strong>Total:</strong></label>
                                        <input type="text" style="font-weight: bold; width: 150px;" readonly="true"
                                               value="Gs. ${totalOrden}" class="form-control">
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
            // Inicializamos la tabla de presupuestos
            $(document).ready(function () {
                $('#tablaModalPresupuestos').DataTable({
                    initComplete: function () {
                        this.api()
                                .columns()
                                .every(function () {
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
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    autoWidth: false,
                    columns: [
                        {width: '5%'},
                        {width: '5%'},
                        null,
                        null,
                        null,
                        {width: '25%'},
                        null
                    ]
                });
            });

            // Inicializamos la tabla de ordenes de compra
            $(document).ready(function () {
                $('#tablaModalOrdenesCompra').DataTable({
                    initComplete: function () {
                        this.api()
                                .columns()
                                .every(function () {
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
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    autoWidth: false,
                    columns: [
                        {width: '5%'},
                        {width: '5%'},
                        null,
                        null,
                        null,
                        null,
                        {width: '20%'},
                        null
                    ]
                });
            });

            // Inicializamos la tabla de sucursales
            $(document).ready(function () {
                $('#tablaModalSucursales').DataTable({
                    initComplete: function () {
                        this.api()
                                .columns()
                                .every(function () {
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
                    autoWidth: false,
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    columns: [{width: '10%'}, null, null, null]
                });
            });

            // Tabla principal de detalle
            $(document).ready(function () {
                $('#tablaOrdenCompraDetalle').DataTable({
                    dom: 'Bfrtip',
                    buttons: [
                        'copy',
                        'excelHtml5',
                        'pdfHtml5',
                        'print'
                    ],
                    initComplete: function () {
                        this.api()
                                .columns()
                                .every(function () {
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
                    autoWidth: false,
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    columns: [{width: '8%'}, {width: '30%'}, {width: '10%'}, {width: '15%'}, {width: '15%'}, null]
                });
            });
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
                </button>
            </div>
        </c:if>
        <script>
            setTimeout(function () {
                var mensaje = document.getElementById('mensaje');
                if (mensaje) {
                    mensaje.style.opacity = '0';
                    setTimeout(function () {
                        mensaje.style.display = 'none';
                    }, 1000);
                }
            }, 7000);
        </script>
    </body>
</html>
