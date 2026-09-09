<%--
    Document   : conciliacionBancaria
    Conciliación bancaria (Tesorería, §F del plan), cableada a ConciliacionBancariaServlet.
    Patrón formulario único + JS (Session+Token), calcado de fondoFijoRendicion.jsp / ordenPago.jsp.
    La grilla sigue el formato de Images/conciliacion_ejemplo.jpg.

    Atributos de request esperados del servlet:
      token (String), esNuevo (boolean), idConciliacionExistente (Long), movimientosCargados (boolean)
      conciliacion (ConciliacionBancaria): cuenta, fechaDesde, fechaHasta, saldoInicial,
        saldoBanco, saldoFinal, estado, tipoCambio
      listaDetalle (List<ConciliacionBancariaDetalle>) -> movimientos del período + arrastrados
      saldoLibro, saldoAjustado, diferencia (Long) calculados en el servlet
      listaCuentas, listaEntidades (combos), listaConciliaciones (modal de búsqueda)
    Acciones (accionPrincipal): Nuevo, CargarCuenta, CargarMovimientos, Grabar,
      CargarConciliacion, Anular, Cancelar
    La pantalla arranca INERTE: sin token sólo se puede usar Nuevo y Buscar Conciliación. Elegida la
    cuenta llegan el saldo inicial y la fecha desde encadenados; con el período se arma la grilla y
    desde ahí la cabecera queda fija.
--%>
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
        <title>Conciliación Bancaria</title>
        <style>
            .custom-card {
                border: 1px solid #ddd;
                border-radius: 8px;
                padding: 16px;
                margin-bottom: 16px;
            }
            .section-title {
                background-color: #e9ecef;
                padding: 8px 12px;
                margin: 0 0 12px 0;
                border-radius: 4px;
                font-weight: bold;
            }
        </style>
    </head>
    <body class="sb-nav-fixed">
        <jsp:include page="menuSuperior.jsp" />
        <div id="layoutSidenav">
            <jsp:include page="menuLateral.jsp" />
            <div id="layoutSidenav_content">
                <main>
                    <div class="container-fluid px-4">
                        <!-- Título -->
                        <div class="row mb-2">
                            <div style="text-align: center; background-color: #dadada; border-radius: 10px; border: 2px solid black; margin-top: 20px;">
                                <h1 style="text-align: center"><strong>CONCILIACIÓN BANCARIA</strong></h1>
                            </div>
                            <div style="border-bottom: 1px solid black; width: 100%; margin: 15px 0;"></div>
                        </div>

                        <c:set var="anulada" value="${not empty conciliacion.estado and conciliacion.estado eq 'Anulado'}" />
                        <c:set var="editable" value="${not empty token and esNuevo}" />
                        <c:set var="cabeceraAbierta" value="${editable and not movimientosCargados}" />

                        <c:set var="vBanco" value="${not empty param.banco ? param.banco : conciliacion.cuenta.entidadFinanciera.idEntidadFinanciera}" />
                        <c:set var="vCuenta" value="${not empty conciliacion.cuenta ? conciliacion.cuenta.idCuenta : param.idCuenta}" />
                        <c:set var="vTipoCambio" value="${not empty conciliacion.tipoCambio ? conciliacion.tipoCambio : param.tipoCambio}" />
                        <c:set var="vSaldoBanco" value="${not empty conciliacion.saldoBanco ? conciliacion.saldoBanco : param.saldoBanco}" />
                        <c:set var="vFechaDesde"><c:choose><c:when test="${not empty conciliacion.fechaDesde}"><fmt:formatDate value="${conciliacion.fechaDesde}" pattern="yyyy-MM-dd"/></c:when><c:otherwise>${param.fechaDesde}</c:otherwise></c:choose></c:set>
                        <c:set var="vFechaHasta"><c:choose><c:when test="${not empty conciliacion.fechaHasta}"><fmt:formatDate value="${conciliacion.fechaHasta}" pattern="yyyy-MM-dd"/></c:when><c:otherwise>${param.fechaHasta}</c:otherwise></c:choose></c:set>

                        <!-- Botones principales -->
                        <div class="row mb-3">
                            <div class="col-auto">
                                <c:choose>
                                    <c:when test="${puedeInsertar}">
                                        <a href="ConciliacionBancariaServlet?menu=ConciliacionBancaria&accion=Nuevo" class="btn btn-success">Nuevo</a>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button" class="btn btn-success" disabled title="No tiene permisos">Nuevo</button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="col-auto">
                                <button type="button" class="btn btn-info text-white" data-bs-toggle="modal" data-bs-target="#modalBuscarConciliacion">Buscar Conciliación</button>
                            </div>
                            <div class="col-auto">
                                <c:set var="anularBloqueado" value="${empty idConciliacionExistente or anulada or not puedeBorrar}" />
                                <span class="d-inline-block" tabindex="0"
                                      <c:if test="${anularBloqueado}">title="${not puedeBorrar ? 'No tiene permisos' : (anulada ? 'La conciliación ya está anulada' : 'Cargue una conciliación para anularla')}"</c:if>>
                                    <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalConfirmarAnular"
                                            <c:if test="${anularBloqueado}">disabled style="pointer-events: none;"</c:if>>Anular</button>
                                </span>
                            </div>
                        </div>

                        <form id="formPrincipal" method="post" action="ConciliacionBancariaServlet">
                            <input type="hidden" name="menu" value="ConciliacionBancaria">
                            <input type="hidden" name="token" value="${token}">
                            <input type="hidden" name="accion" id="accionPrincipal" value="Grabar">
                            <input type="hidden" id="saldoLibroRaw" value="${saldoLibro}">

                            <!-- Cabecera -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="row mb-3">
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="banco" name="banco" onchange="filtrarCuentas();"
                                                        <c:if test="${not cabeceraAbierta}">disabled</c:if>>
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="ent" items="${listaEntidades}">
                                                        <option value="${ent.idEntidadFinanciera}"
                                                                <c:if test="${vBanco == ent.idEntidadFinanciera}">selected</c:if>>${ent.nombre}</option>
                                                    </c:forEach>
                                                </select>
                                                <label for="banco">Banco</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <select class="form-control" id="cuenta" name="idCuenta" onchange="cargarCuenta();"
                                                        <c:if test="${not cabeceraAbierta}">disabled</c:if>>
                                                    <option value="">Seleccionar...</option>
                                                    <c:forEach var="cta" items="${listaCuentas}">
                                                        <option value="${cta.idCuenta}"
                                                                data-banco="${cta.entidadFinanciera.idEntidadFinanciera}"
                                                                data-moneda="${cta.moneda.descripcion}"
                                                                <c:if test="${vCuenta == cta.idCuenta}">selected</c:if>>${cta.numero}</option>
                                                    </c:forEach>
                                                </select>
                                                <label for="cuenta">Nro de cuenta</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="moneda" type="text" placeholder="Moneda" readonly
                                                       value="${conciliacion.cuenta.moneda.descripcion}" />
                                                <label for="moneda">Moneda</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="tipoCambio" name="tipoCambio" type="text" inputmode="decimal"
                                                       placeholder="Tipo de cambio al cierre" value="${vTipoCambio}"
                                                       <c:if test="${not editable}">readonly</c:if> />
                                                <label for="tipoCambio">Tipo de cambio al cierre</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fechaDesde" name="fechaDesde" type="date"
                                                       placeholder="Fecha desde" value="${vFechaDesde}"
                                                       <c:if test="${not cabeceraAbierta or not empty conciliacion.fechaDesde}">readonly</c:if> />
                                                <label for="fechaDesde">Fecha desde</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating mb-3 mb-md-0">
                                                <input class="form-control" id="fechaHasta" name="fechaHasta" type="date"
                                                       placeholder="Fecha hasta" value="${vFechaHasta}"
                                                       <c:if test="${not cabeceraAbierta}">readonly</c:if> />
                                                <label for="fechaHasta">Fecha hasta</label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row align-items-center">
                                        <div class="col-md-3">
                                            <span class="d-inline-block" tabindex="0"
                                                  <c:if test="${not cabeceraAbierta}">title="${empty token ? 'Presione Nuevo para iniciar una conciliación' : 'Los movimientos ya están cargados'}"</c:if>>
                                                <button type="button" class="btn btn-primary" onclick="cargarMovimientos();"
                                                        <c:if test="${not cabeceraAbierta}">disabled style="pointer-events: none;"</c:if>>Cargar movimientos</button>
                                            </span>
                                        </div>
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="saldoInicial" type="text" placeholder="Saldo inicial" readonly
                                                       data-bs-toggle="tooltip"
                                                       title="Saldo según libro con el que cerró la conciliación anterior de esta cuenta. Lo trae el sistema."
                                                       value="<fmt:formatNumber value='${conciliacion.saldoInicial}' pattern='#,##0'/>" />
                                                <label for="saldoInicial">Saldo inicial</label>
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <div class="form-floating">
                                                <input class="form-control" id="estado" type="text" placeholder="Estado" readonly
                                                       value="${not empty conciliacion.estado ? conciliacion.estado : ''}" />
                                                <label for="estado">Estado</label>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Movimientos -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="section-title">Movimientos del período</div>

                                    <div class="row mb-3">
                                        <div class="col-md-3">
                                            <div class="form-floating">
                                                <select class="form-control" id="filtroTipo" onchange="filtrarPorTipo();">
                                                    <option value="">Todos</option>
                                                    <option value="Ch">Cheques</option>
                                                    <option value="Transf">Transferencias</option>
                                                    <option value="Deb">Débitos</option>
                                                    <option value="Cred">Créditos</option>
                                                </select>
                                                <label for="filtroTipo">Mostrar</label>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="table-responsive">
                                        <table id="tablaMovimientos" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Conciliado</th>
                                                    <th class="text-bg-dark text-center">Emisión</th>
                                                    <th class="text-bg-dark text-center">Fecha</th>
                                                    <th class="text-bg-dark text-center">Número</th>
                                                    <th class="text-bg-dark text-center">Detalle</th>
                                                    <th class="text-bg-dark text-center">Banco</th>
                                                    <th class="text-bg-dark text-center">Cuenta</th>
                                                    <th class="text-bg-dark text-center">Nro. Doc.</th>
                                                    <th class="text-bg-dark text-center">Tipo</th>
                                                    <th class="text-bg-dark text-center">Importe</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="det" items="${listaDetalle}" varStatus="st">
                                                    <%-- La fecha de emisión de un cheque no es la del movimiento: se emite en un
                                                         período y se cobra en otro, que es lo que hace visible el arrastre. --%>
                                                    <c:set var="fechaMovimiento" value="${not empty det.formaPagoDetalle ? det.formaPagoDetalle.fecha : (not empty det.debito ? det.debito.fecha : det.credito.fecha)}" />
                                                    <c:set var="fechaEmision" value="${not empty det.formaPagoDetalle.cheque ? det.formaPagoDetalle.cheque.fechaEmision : fechaMovimiento}" />
                                                    <c:set var="numero" value="${not empty det.ordenPago ? det.ordenPago.numero : (not empty det.debito ? det.debito.numeroComprobante : det.credito.numeroComprobante)}" />
                                                    <c:set var="nroDocumento" value="${not empty det.formaPagoDetalle.cheque ? det.formaPagoDetalle.cheque.numero : (not empty det.formaPagoDetalle ? det.formaPagoDetalle.referencia : numero)}" />
                                                    <tr>
                                                        <td class="text-center">
                                                            <input type="checkbox" class="form-check-input chkConciliado"
                                                                   name="conciliado_${st.index}" id="conciliado_${st.index}"
                                                                   data-tipo="${det.tipo}" data-monto="${det.monto}"
                                                                   onchange="recalcularSaldos();"
                                                                   <c:if test="${det.conciliado}">checked</c:if>
                                                                   <c:if test="${not editable}">disabled</c:if> />
                                                        </td>
                                                        <td class="text-center" data-order="<fmt:formatDate value='${fechaEmision}' pattern='yyyy-MM-dd'/>"><fmt:formatDate value="${fechaEmision}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center" data-order="<fmt:formatDate value='${fechaMovimiento}' pattern='yyyy-MM-dd'/>"><fmt:formatDate value="${fechaMovimiento}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center">${numero}</td>
                                                        <td>${det.descripcion}</td>
                                                        <td>${conciliacion.cuenta.entidadFinanciera.nombre}</td>
                                                        <td class="text-center">${conciliacion.cuenta.numero}</td>
                                                        <td class="text-center">${nroDocumento}</td>
                                                        <td class="text-center">${det.tipo}</td>
                                                        <td class="text-end" data-order="${det.monto}"><fmt:formatNumber value="${det.monto}" pattern="#,##0"/></td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>

                            <!-- Saldos -->
                            <div class="row mb-3">
                                <div class="col custom-card">
                                    <div class="section-title">Saldos</div>
                                    <div class="row mb-3">
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="saldoBanco" name="saldoBanco" type="text"
                                                       inputmode="numeric" placeholder="Saldo según extracto"
                                                       value="${vSaldoBanco}" oninput="recalcularSaldos();"
                                                       data-bs-toggle="tooltip"
                                                       title="Saldo que muestra el extracto del banco al cierre del período. Es el único saldo que se carga a mano."
                                                       <c:if test="${not editable or not movimientosCargados}">readonly</c:if> />
                                                <label for="saldoBanco">Saldo según extracto</label>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="noCobrado" type="text" placeholder="Menos: no cobrado por el banco" readonly
                                                       data-bs-toggle="tooltip"
                                                       title="Cheques, transferencias y débitos sin tildar: el libro ya los descontó y el banco todavía no. Se le restan al extracto."
                                                       value="<fmt:formatNumber value='${noCobrado}' pattern='#,##0'/>" />
                                                <label for="noCobrado">Menos: no cobrado por el banco</label>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="noAcreditado" type="text" placeholder="Más: depósitos no acreditados" readonly
                                                       data-bs-toggle="tooltip"
                                                       title="Depósitos y créditos sin tildar: el libro ya los sumó y el banco todavía no los acreditó. Se le suman al extracto."
                                                       value="<fmt:formatNumber value='${noAcreditado}' pattern='#,##0'/>" />
                                                <label for="noAcreditado">Más: depósitos no acreditados</label>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="saldoAjustado" type="text" placeholder="Extracto ajustado" readonly
                                                       data-bs-toggle="tooltip"
                                                       title="Saldo del extracto corregido por las dos partidas de arriba. Si la conciliación cierra, es igual al saldo según libro."
                                                       value="<fmt:formatNumber value='${saldoAjustado}' pattern='#,##0'/>" />
                                                <label for="saldoAjustado">Extracto ajustado</label>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control text-end" id="saldoLibro" type="text" placeholder="Saldo según libro" readonly
                                                       data-bs-toggle="tooltip"
                                                       title="Saldo inicial más los créditos y menos los débitos, transferencias y cheques del período. Es el saldo que se encadena a la conciliación siguiente."
                                                       value="<fmt:formatNumber value='${saldoLibro}' pattern='#,##0'/>" />
                                                <label for="saldoLibro">Saldo según libro</label>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="form-floating">
                                                <input class="form-control text-end fw-bold" id="diferencia" type="text" placeholder="Diferencia" readonly
                                                       data-bs-toggle="tooltip"
                                                       title="Extracto ajustado menos saldo según libro. En cero la conciliación cuadra; si no, hay un movimiento que el banco tiene y el sistema no."
                                                       value="<fmt:formatNumber value='${diferencia}' pattern='#,##0'/>" />
                                                <label for="diferencia">Diferencia</label>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="row mt-3">
                                        <div class="col-auto">
                                            <c:choose>
                                                <c:when test="${puedeInsertar}">
                                                    <span class="d-inline-block" tabindex="0"
                                                          <c:if test="${not editable or not movimientosCargados}">title="${empty token or not esNuevo ? 'Presione Nuevo para iniciar una conciliación' : 'Primero cargue los movimientos del período'}"</c:if>>
                                                        <button type="button" class="btn btn-success" onclick="grabarConciliacion();"
                                                                <c:if test="${not editable or not movimientosCargados}">disabled style="pointer-events: none;"</c:if>>Grabar</button>
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-success" disabled title="No tiene permisos">Grabar</button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="col-auto">
                                            <a href="ConciliacionBancariaServlet?menu=ConciliacionBancaria&accion=Cancelar&token=${token}" class="btn btn-danger">Cancelar</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>

                        <!-- Modal Buscar Conciliación -->
                        <div class="modal fade" id="modalBuscarConciliacion" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-xl">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Buscar Conciliación</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body table-responsive">
                                        <table id="tablaConciliaciones" class="table table-bordered table-striped">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id</th>
                                                    <th class="text-bg-dark text-center">Banco</th>
                                                    <th class="text-bg-dark text-center">Cuenta</th>
                                                    <th class="text-bg-dark text-center">Desde</th>
                                                    <th class="text-bg-dark text-center">Hasta</th>
                                                    <th class="text-bg-dark text-center">Saldo según libro</th>
                                                    <th class="text-bg-dark text-center">Estado</th>
                                                    <th class="text-bg-dark text-center">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="con" items="${listaConciliaciones}">
                                                    <tr>
                                                        <td class="text-center">${con.idConciliacionBancaria}</td>
                                                        <td>${con.cuenta.entidadFinanciera.nombre}</td>
                                                        <td class="text-center">${con.cuenta.numero}</td>
                                                        <td class="text-center"><fmt:formatDate value="${con.fechaDesde}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-center"><fmt:formatDate value="${con.fechaHasta}" pattern="dd/MM/yyyy"/></td>
                                                        <td class="text-end"><fmt:formatNumber value="${con.saldoFinal}" pattern="#,##0"/></td>
                                                        <td class="text-center">
                                                            <c:choose>
                                                                <c:when test="${con.estado eq 'Anulado'}"><span class="badge bg-danger">Anulado</span></c:when>
                                                                <c:otherwise><span class="badge bg-success">${con.estado}</span></c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td class="text-center">
                                                            <a href="ConciliacionBancariaServlet?menu=ConciliacionBancaria&accion=CargarConciliacion&id=${con.idConciliacionBancaria}"
                                                               class="btn btn-primary btn-sm">Cargar</a>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                    <div class="modal-footer"><button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cerrar</button></div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Grabar -->
                        <div class="modal fade" id="modalConfirmarGrabar" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea grabar esta conciliación?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-success" onclick="confirmarGrabar();">Sí, Grabar</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Modal Confirmar Anular -->
                        <div class="modal fade" id="modalConfirmarAnular" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered">
                                <div class="modal-content">
                                    <div class="modal-header bg-danger text-white"><h5 class="modal-title">Confirmación</h5>
                                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div>
                                    <div class="modal-body"><p>¿Está seguro que desea anular esta conciliación?</p></div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">No</button>
                                        <button type="button" class="btn btn-danger" onclick="anularConciliacion();">Sí, Anular</button>
                                    </div>
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
            function setAccion(a) {
                document.getElementById('accionPrincipal').value = a;
            }
            function enviar() {
                document.getElementById('formPrincipal').submit();
            }
            function cargarCuenta() {
                setAccion('CargarCuenta');
                enviar();
            }
            function cargarMovimientos() {
                setAccion('CargarMovimientos');
                enviar();
            }
            function grabarConciliacion() {
                new bootstrap.Modal(document.getElementById('modalConfirmarGrabar')).show();
            }
            function confirmarGrabar() {
                setAccion('Grabar');
                enviar();
            }
            function anularConciliacion() {
                setAccion('Anular');
                enviar();
            }

            // El combo de banco sólo filtra: lo que se guarda es la cuenta.
            function filtrarCuentas() {
                var banco = document.getElementById('banco');
                var cuentas = document.getElementById('cuenta');
                var sinBanco = (banco.value === '');
                if (!banco.disabled) {
                    cuentas.disabled = sinBanco;
                }
                if (sinBanco) {
                    cuentas.value = '';
                }
                for (var i = 0; i < cuentas.options.length; i++) {
                    var op = cuentas.options[i];
                    if (!op.value) {
                        continue;
                    }
                    var visible = (!sinBanco && op.getAttribute('data-banco') === banco.value);
                    op.hidden = !visible;
                    op.disabled = !visible;
                    if (!visible && op.selected) {
                        cuentas.value = '';
                    }
                }
                mostrarMoneda();
            }
            // La moneda no se carga: sale de la cuenta elegida, igual que en la orden de pago.
            function mostrarMoneda() {
                var cuentas = document.getElementById('cuenta');
                var op = cuentas.options[cuentas.selectedIndex];
                document.getElementById('moneda').value = (op && op.value) ? (op.getAttribute('data-moneda') || '') : '';
            }

            function filtrarPorTipo() {
                var valor = document.getElementById('filtroTipo').value;
                // La columna 8 es Tipo; ^...$ para que 'Ch' no arrastre a 'Cred'.
                $('#tablaMovimientos').DataTable().column(8)
                        .search(valor === '' ? '' : '^' + valor + '$', true, false).draw();
            }

            function formatearMonto(n) {
                return n.toLocaleString('es-PY', {maximumFractionDigits: 0});
            }
            /*
             * El saldo según libro no depende de los tildes —son los movimientos del período— así
             * que viene calculado del servidor. Lo que cambia al tildar es el extracto ajustado:
             * los ítems sin tildar son las partidas conciliatorias.
             */
            function recalcularSaldos() {
                var crudo = (document.getElementById('saldoBanco').value || '').replace(/[^\d-]/g, '');
                var extracto = parseInt(crudo, 10);
                if (isNaN(extracto)) {
                    extracto = 0;
                }
                // Las dos partidas conciliatorias: lo sin tildar que el banco todavia no muestra.
                var noCobrado = 0, noAcreditado = 0;
                var checks = document.getElementsByClassName('chkConciliado');
                for (var i = 0; i < checks.length; i++) {
                    if (checks[i].checked) {
                        continue;
                    }
                    var monto = parseInt(checks[i].getAttribute('data-monto'), 10) || 0;
                    if (checks[i].getAttribute('data-tipo') === 'Cred') {
                        noAcreditado += monto;
                    } else {
                        noCobrado += monto;
                    }
                }
                var ajustado = extracto - noCobrado + noAcreditado;
                var libro = parseInt(document.getElementById('saldoLibroRaw').value, 10) || 0;
                document.getElementById('noCobrado').value = formatearMonto(noCobrado);
                document.getElementById('noAcreditado').value = formatearMonto(noAcreditado);
                document.getElementById('saldoAjustado').value = formatearMonto(ajustado);
                document.getElementById('diferencia').value = formatearMonto(ajustado - libro);
            }

            $(document).ready(function () {
                filtrarCuentas();
                // Tooltips de Bootstrap
                var tips = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                tips.forEach(function (el) {
                    new bootstrap.Tooltip(el);
                });
                /*
                 * Sin paginado a propósito: los tildes viajan como checkbox del formulario y
                 * DataTables saca del DOM las filas de las páginas que no se ven, así que
                 * paginando se perderían los de las demás páginas al grabar.
                 */
                $('#tablaMovimientos').DataTable({
                    language: {url: "DataTables 2/es-ES.json"},
                    paging: false,
                    scrollY: '45vh',
                    scrollCollapse: true,
                    columnDefs: [{orderable: false, targets: 0}]
                });
                $('#tablaConciliaciones').DataTable({language: {url: "DataTables 2/es-ES.json"}});
            });
        </script>

        <!-- Mensajes Toastr -->
        <c:if test="${not empty Message}">
            <script>
                toastr.options = {positionClass: "toast-top-right", closeButton: true, timeOut: 5000, progressBar: true};
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
