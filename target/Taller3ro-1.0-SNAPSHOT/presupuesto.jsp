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
<%@page import="modelo.Presupuesto"%>
<%
    // para obtener la fecah formateada del pedido
    Presupuesto presupuesto = (Presupuesto) request.getAttribute("presupuesto");
    String fechaFormateada = "";
    if (presupuesto != null) {
        Date fecha = presupuesto.getFecha();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        fechaFormateada = formato.format(fecha); //fehca formateada
    }
%>

<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"  pageEncoding="UTF-8" %>
<html>
    <!--incluir los scripts y estilos en el header-->
    <jsp:include page="header.jsp" />
    <head>
        <title>Presupuesto</title>
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
                                        <strong>PRESUPUESTO DE COMPRA</strong></h1></span>
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
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getFecha()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getEstado()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PedCompra.getListaArticulos()}</td>
                                                                <td align="center" valign="middle" class="text-center">
                                                                <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                                                    <input type="hidden" name="idPedCompraCab" value="${PedCompra.getIdPedido()}">
                                                                    <button name="accion" value="CargarPedidoCompra" type="submit" class="btn btn-primary" 
                                                                            <c:if test="${PedCompra.getEstado() eq 'Anulado'}"><c:out value="disabled='disabled'"/></c:if>>Seleccionar</button>
                                                                </form>
<!--                                                                    <a href="PresupuestoServlet?menu=Presupuesto&accion=CargarPedidoCompra&idPedCompraCab=${PedCompra.getIdPedido()}"
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
                                            <a href="PedidoCompraServlet?menu=PedidoCompra&accion=ListarModal" class="btn btn-success">Agregar Pedido</a>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Modal Presupuesto-->
                            <div class="modal fade" id="modalPresupuestos" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-dialog modal-xl">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Presupuestos</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                                <table id="tablaModalPresupuestos" class="table table-bordered table-striped">
                                                    <thead>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Prsupuesto</th>
                                                            <th class="text-bg-dark text-center">Nro Pedido Asociado</th>
                                                            <th class="text-bg-dark text-center">Solicitante</th>
                                                            <th class="text-bg-dark text-center">Proveedor</th>
                                                            <th class="text-bg-dark text-center">Fecha</th>
                                                            <th class="text-bg-dark text-center">Estado</th>
                                                            <th class="text-bg-dark text-center">Detalle</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="PresupuestosConDet" items="${listaPresupuestosConDetalle}">
                                                            <tr class="${Presupuestos.getEstado() eq 'Anulado' ? 'table-danger' : ''}">
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getIdPresupuesto()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getPedidoCompra().getIdPedido()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getUsuario().getPersona().getNombre()} 
                                                                    ${PresupuestosConDet.getUsuario().getPersona().getApellido()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getProveedor().getRazonSocial()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getFecha()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getEstado()}</td>
                                                                <td align="center" valign="middle" class="text-center">${PresupuestosConDet.getListaArticulos()}</td>
                                                                <td align="center" valign="middle" class="text-center">
                                                                <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                                                    <input type="hidden" name="idPedCompraCab" value="${PresupuestosConDet.getIdPresupuesto()}">
                                                                    <button name="accion" value="CargarPresupuesto" type="submit" class="btn btn-primary" 
                                                                            <c:if test="${PedCompra.getEstado() eq 'Anulado'}"><c:out value="disabled='disabled'"/></c:if>>Seleccionar</button>
                                                                </form>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Prsupuesto</th>
                                                            <th class="text-bg-dark text-center">Nro Pedido Asociado</th>
                                                            <th class="text-bg-dark text-center">Solicitante</th>
                                                            <th class="text-bg-dark text-center">Proveedor</th>
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
                            
                            <!-- Modal Proveedor-->
                            <div class="modal fade" id="modalProveedores" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h1 class="modal-title fs-5" id="exampleModalLabel">Proveedor</h1>
                                            <button type="button" class="btn btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="table-responsive">
                                                <table id="tablaModalProveedores" class="table table-bordered table-striped">
                                                    <thead>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Proveedor.</th>
                                                            <th class="text-bg-dark text-center">Razón Social</th>
                                                            <th class="text-bg-dark text-center">Ruc</th>
                                                            <th class="text-bg-dark text-center">Dirección</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </thead>

                                                    <tbody>
                                                        <c:forEach var="listProveedores" items="${listaProveedores}">
                                                            <tr>
                                                                <td class="text-center">${listProveedores.getIdProveedor()}</td>
                                                                <td class="text-center">${listProveedores.getNombreComercial()}</td>
                                                                <td class="text-center">${listProveedores.getRuc()}</td>
                                                                <td class="text-center">${listProveedores.getDireccion()}</td>
                                                                <td class="text-center">
                                                                    <a href="PresupuestoServlet?menu=Presupuesto&accion=CargarProveedor&idProvee=${listProveedores.getIdProveedor()}" 
                                                                       class="btn btn-primary">Seleccionar</a>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                    </tbody>
                                                    <tfoot>
                                                        <tr>
                                                            <th class="text-bg-dark text-center">Nro Proveedor.</th>
                                                            <th class="text-bg-dark text-center">Razón Social</th>
                                                            <th class="text-bg-dark text-center">Ruc</th>
                                                            <th class="text-bg-dark text-center">Dirección</th>
                                                            <th class="text-bg-dark text-center no-search">Acciones</th>
                                                        </tr>
                                                    </tfoot>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a href="MenuPrincipal.jsp" class="btn btn-success">Agregar Proveedor</a>
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="col-auto">
                                <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                        <input type="hidden" name="idUsuario" value="<%= usuario.getIdUsuario() %>">
                                    <button name="accion" value="Nuevo" type="submit" class="btn btn-success">Nuevo</button>
                                    <button name="accion" value="BuscarPedido" type="button" data-bs-toggle="modal" 
                                            data-bs-target="#modalPedidos" class="btn btn-info text-white"
                                            <c:if test="${newIdPresupuesto == null}"><c:out value="disabled='disabled'"/></c:if>>Buscar Pedido</button>
                                    <a href="" data-bs-toggle="modal" data-bs-target="#modalPresupuestos" class="btn btn-info text-white">Buscar Presupuesto</a>
                                    <button href="" class="btn btn-primary"
                                       <c:if test="${presupuesto.getIdPresupuesto() == null}"><c:out value="disabled='disabled'"/></c:if>>Aprobar</button>
                                    <button name="accion" value="Anular" type="button" data-bs-toggle="modal" 
                                        data-bs-target="#modalAnular${presupuesto.getIdPresupuesto()}" class="btn btn-danger" 
                                        <c:if test="${presupuesto.getIdPresupuesto() == null}"><c:out value="disabled='disabled'"/></c:if>>Anular</button>
                                </form>
                                <!-- Modal de confirmacion sin javascript  -->
                                <div class="modal fade" id="modalAnular${presupuesto.getIdPresupuesto()}" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
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
                                        <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                          <input type="hidden" name="accion" value="Anular">
                                          <input type="hidden" name="id" value="${presupuesto.getIdPresupuesto()}">
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
                                        <label style="white-space: nowrap;" class="me-2">Presupuesto N°:</label>
                                        <input type="text" value="${presupuesto.getIdPresupuesto() == null ? newIdPresupuesto : presupuesto.getIdPresupuesto()}" class="form-control" disabled="">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Usuario:</label>
                                        <input type="text" value="${presupuesto.getUsuario().getUsername()}"
                                               class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Fecha:</label>
                                        <input value="<%=fechaFormateada %>" class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label class="me-2">Estado:</label>
                                        <input type="text" value="${presupuesto.getEstado()}" class="form-control" disabled="true">
                                    </div>
                                    <div class="col-md-2 d-flex align-items-center">
                                        <label style="white-space: nowrap;" class="me-2">Pedido N°:</label> 
                                        <input type="text" value="${presupuesto.getPedidoCompra().getIdPedido()}" class="form-control" disabled="">
                                    </div>
                                </div>

                                <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                    <div class="row" style="margin-top: 23px">
                                        <div class="col-auto">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <button type="button" href="" data-bs-toggle="modal" data-bs-target="#modalProveedores" class="btn btn-outline-primary"
                                                        <c:if test="${presupuesto.getIdPresupuesto() == null && newIdPresupuesto == null && presupuesto.getPedidoCompra().getIdPedido() == null}">
                                                            <c:out value="disabled='disabled'"/></c:if>>Buscar Proveedor</button>
                                            </div>
                                        </div>
                                        <div class="col-md-1">
                                            <input type="text" name="txtIdProveedor" placeholder="Id. Proveedor" value="${proveedorSeleccionado.getIdProveedor()}"
                                                   class="form-control" disabled="true">
                                        </div>
                                        <div class="col-md-3">
                                            <input type="text" placeholder="Razon Social" value="${proveedorSeleccionado.getRazonSocial()}" 
                                                   class="form-control" disabled="true">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="text" placeholder="Ruc" value="${proveedorSeleccionado.getRuc()}" class="form-control"
                                                   required="true" disabled="true">
                                        </div>
                                    </div>
                                </form>
                                    
                                <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                    <div class="row" style="margin-top: 23px">
                                        <div class="col-md-1">
                                            <input type="text" name="txtIdArticulo" placeholder="Id. Artículo" value="${presupuestoDetSeleccionado.getArticulo().getIdArticulo()}" 
                                                   class="form-control" disabled="true">
                                        </div>
                                        <div class="col-md-4">
                                            <input type="text" placeholder="Descripción" value="${presupuestoDetSeleccionado.getArticulo().getDescripcion()}" 
                                                   class="form-control"  disabled="true">
                                        </div>
                                        <div class="col-md-1">
                                            <input type="number" name="txtCantidad" min="1" max="${presupuestoDetSeleccionado.getCantidad()}" 
                                                   value="${presupuestoDetSeleccionado.getCantidad()}"
                                                   <c:if test="${presupuestoDetSeleccionado.getArticulo().getIdArticulo() == null}">
                                                       <c:out value="disabled='disabled'"/></c:if>  placeholder="Cantidad" class="form-control" required="true">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="number" name="txtPrecioCompra" min="1"  value="${presupuestoDetSeleccionado.getPrecioCompra()}"
                                                   <c:if test="${presupuestoDetSeleccionado.getArticulo().getIdArticulo() == null}">
                                                       <c:out value="disabled='disabled'"/></c:if>  placeholder="Precio de Compra" class="form-control" required="true">
                                        </div>
                                        <div class="col-md-2">
                                            <input type="hidden" name="idArt" value="${presupuestoDetSeleccionado.getArticulo().getIdArticulo()}">
                                            <button type="submit" name="accion" value="AgregarArticuloADetalle"
                                                class="btn btn-warning" <c:if test="${presupuestoDetSeleccionado.getArticulo().getIdArticulo() == null}"><c:out 
                                                value="disabled='disabled'"/></c:if>>Modificar
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
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center">Precio de compra (Gs.)</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </thead>

                                        <tbody>
                                            <c:forEach var="listaPresuDetalle" items="${listaPresupuestoDetalle}">
                                                <tr>
                                                    <td class="text-center">${listaPresuDetalle.getArticulo().getIdArticulo()}</td>
                                                    <td class="text-center">${listaPresuDetalle.getArticulo().getDescripcion()}</td>
                                                    <td class="text-center">${listaPresuDetalle.getCantidad()}</td>
                                                    <td class="text-center">${listaPresuDetalle.getPrecioCompra()}</td>
                                                    <td class="text-center">
                                                        <a href="PresupuestoServlet?menu=Presupuesto&accion=EditarPrecioArticuloList&idArt=${listaPresuDetalle.getArticulo().getIdArticulo()}" 
                                                           class="btn btn-warning">Editar</a>
                                                        <a href="PresupuestoServlet?menu=Presupuesto&accion=EliminarArticuloList&idArt=${listaPresuDetalle.getArticulo().getIdArticulo()}" 
                                                           class="btn btn-danger">Eliminar</a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <tfoot>
                                            <tr>
                                                <th class="text-bg-dark text-center">Id. Articulo</th>
                                                <th class="text-bg-dark text-center">Descripción</th>
                                                <th class="text-bg-dark text-center">Cantidad</th>
                                                <th class="text-bg-dark text-center">Precio de compra (Gs.)</th>
                                                <th class="text-bg-dark text-center no-search">Acciones</th>
                                            </tr>
                                        </tfoot>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="card-footer d-flex justify-content-between align-items-center">
                                    <div>
                                        <button type="button" href="#" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#modalGenerarPresupuesto${listaPresuDetalle}"
                                           <c:if test="${newIdPresupuesto == null || proveedorSeleccionado == null || empty listaPresupuestoDetalle}"><c:out value="disabled='disabled'"/></c:if>>Guardar</button>
                                        <!-- Modal de confirmacion sin javascript  -->
                                        <div class="modal fade" id="modalGenerarPresupuesto${listaPresuDetalle}" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                          <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content">
                                              <div class="modal-header">
                                                <h1 class="modal-title fs-5">Confirmación</h1>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                              </div>
                                              <div class="modal-body">
                                                ¿Desea generar un nuevo presupuesto de compra?
                                              </div>
                                              <div class="modal-footer">
                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                <form action="PresupuestoServlet?menu=Presupuesto" method="POST">
                                                  <input type="hidden" name="accion" value="PersistirPresupuesto">
                                                  <input type="hidden" name="id" value="${listaPresuDetalle}">
                                                  <button type="submit" class="btn btn-primary">Sí</button>
                                                </form>
                                              </div>
                                            </div>
                                          </div>
                                        </div>

                                        <a href="PresupuestoServlet?menu=Presupuesto&accion=ListarModal" class="btn btn-danger">Cancelar</a>
                                    </div>                            
<!--                                    <div class="d-flex align-items-center">
                                        <label class="mb-0">Total: </label>
                                        <input type="text" style="font-weight: bold" readonly="true" name="txttotal" value="Gs. " 
                                               class="form-control ml-2" placeholder="Total">
                                    </div>-->
                                </div>
                            </div>
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

//          Inicializamos la tabla de presupuestos
                $(document).ready(function () {
                $('#tablaModalPresupuestos').DataTable({
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
                        null, // Sexta columna
                        {width: '27%'}, // Septima columna
                        null // Octava columna
                    ]
                });
            });
            
//          Inicializamos la tabla de proveedores
            $(document).ready(function () {
                $('#tablaModalProveedores').DataTable({
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
                    // Asegurar que los anchos no sean sobrescritos
                    autoWidth: false,
                    //Lenguaje que apunta al idioma
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    //Tamaño de las olumnas
                    columns: [{width: '8%'}, {width: '27%'}, null, null, null]
                });
            });
            //Otra manera de inicializar la tabla y ponerle lenguaje
            //                var table = new DataTable('#tablaPrueba', {
            //                language: {
            //                    url: "DataTables 2/es-ES.json",
            //                },
            //                });

            //Tabla 3
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
                    // Asegurar que los anchos no sean sobrescritos
                    autoWidth: false,
                    //Lenguaje que apunta al idioma
                    language: {
                        url: "DataTables 2/es-ES.json",
                    },
                    //Tamaño de las olumnas
                    columns: [{width: '8%'}, {width: '27%'}, {width: '15%'}, null, null]
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
