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
        <title>Tipos de artículos</title>
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
                            <span style="height: 100%; width: 100%; background-color: yellow">
                                <h1 style="text-align: center">
                                    <strong>TIPO DE ARTÍCULO</strong></h1></span>
                        </div>
                        <!--linea debajo del titulo--> 
                        <div style="border-bottom: 1px solid black; width: 100%; margin: 20px 0;"></div>

                        <div class="d-flex">
                            <!-- Formulario -->
                            <div class="col-sm-2" style="width: 33%;">
                                <div class="card">
                                    <div class="card-body">
                                    <form action="TipoArticuloServlet?menu=TipoArticulo" method="POST">
                                        <div class="mb-3">
                                            <label for="txtnombre" class="form-label">Descripción</label>
                                            <input type="text" class="form-control" id="txtdescripcion" name="txtdescripcion" 
                                                   value="${tipoArt.getDescripcion()}" required="true">
                                        </div>

                                        <button type="submit" name="accion" value="Insertar" class="btn btn-success"
                                                <c:if test="${tipoArt.getDescripcion() != null}"><c:out value="disabled='disabled'"/></c:if>>Agregar</button>

                                        <button type="submit" name="accion" value="Actualizar" class="btn btn-primary"
                                                <c:if test="${tipoArt.getDescripcion() == null}"><c:out value="disabled='disabled'"/></c:if>>Actualizar</button>

                                        <a href="TipoArticuloServlet?menu=TipoArticulo&accion=Cancelar" class="btn btn-danger">Cancelar</a>

                                    </form>
                                </div>
                            </div>
                                </div>

                            <!-- Tabla -->
<!--                            <div class="card col-8" style="margin-left: 30px;width: 65%;">
                                <div class="card-body" style="height: 10px">-->
                            <div class="col-sm-7" style="margin-left: 33px;width: 65%;">
                                <div class="card">
                                    <div class="card-body">
                                        <table id="tablaPrueba2" class="table table-bordered">
                                            <thead>
                                                <tr>
                                                    <th class="text-bg-dark text-center">Id. Tipo Articulo</th>
                                                    <th class="text-bg-dark text-center">Descripción</th>
                                                    <th class="text-bg-dark text-center">Acciones</th>
                                                </tr>
                                            </thead>

                                            <tbody>
                                                <c:forEach var="ta" items="${tipoArticulo}">
                                                    <tr>
                                                        <td class="text-center">${ta.getIdTipoArticulo()}</td>
                                                        <td class="text-center">${ta.getDescripcion()}</td>
                                                        <td class="text-center">

                                                            <!--para enviar por metodo POST el id del tipo articulo, el id no se va a ver en la URL-->
    <!--                                                        <form action="TipoArticuloServlet?menu=TipoArticulo" method="POST">
                                                            <input type="hidden" name="id" value="${ta.getIdTipoArticulo()}">
                                                            <button type="submit" name="accion" value="Editar" class="btn btn-warning">Editar</button>
                                                            <button type="submit" name="accion" value="Eliminar" class="btn btn-danger">Eliminar</button>
                                                            </form>-->

                                                            <!--boton Editar-->
                                                            <!--para enviar por metodo GET el id del tipo articulo, el id se va a ver en la URL-->
                                                            <a href="TipoArticuloServlet?menu=TipoArticulo&accion=Editar&id=${ta.getIdTipoArticulo()}" class="btn btn-warning">Editar</a>
                                                            
                                                            <!--boton Eliminar-->
                                                            <!--eliminar directamente sin el modal de confirmacion por metodo GET-->
    <!--                                                        <a href="TipoArticuloServlet?menu=TipoArticulo&accion=Eliminar&id=${ta.getIdTipoArticulo()}" class="btn btn-danger" >Eliminar</a>-->

                                                            <!--eliminar tipo de articulo con modal de confirmacion (con javascript)-->
    <!--                                                        <a href="#" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#exampleModal" onclick="setEliminarId('${ta.getIdTipoArticulo()}')">Eliminar</a>
                                                             Modal de Confirmación con javascript 
                                                            <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                                              <div class="modal-dialog">
                                                                <div class="modal-content">
                                                                  <div class="modal-header">
                                                                    <h1 class="modal-title fs-5" id="exampleModalLabel">Confirmación</h1>
                                                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                  </div>
                                                                  <div class="modal-body">
                                                                    ¿Seguro que quiere eliminar el Tipo de artículo?
                                                                  </div>
                                                                  <div class="modal-footer">
                                                                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                                    <a id="confirmarEliminar" href="#" class="btn btn-primary">Sí</a>
                                                                  </div>
                                                                </div>
                                                              </div>
                                                            </div>-->
                                                             <!--Script para asignar dinámicamente el id--> 
    <!--                                                        <script>
                                                              function setEliminarId(id) {
                                                                document.getElementById("confirmarEliminar").href = "TipoArticuloServlet?menu=TipoArticulo&accion=Eliminar&id=" + id;
                                                              }
                                                            </script>-->

                                                        <!--eliminar tipo de articulo con modal de confirmacion (con javascript)-->
                                                        <a href="#" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#modalEliminar${ta.getIdTipoArticulo()}">Eliminar</a>
                                                        <!-- Modal de confirmacion sin javascript  -->
                                                        <div class="modal fade" id="modalEliminar${ta.getIdTipoArticulo()}" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
                                                          <div class="modal-dialog">
                                                            <div class="modal-content">
                                                              <div class="modal-header">
                                                                <h1 class="modal-title fs-5">Confirmación</h1>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                              </div>
                                                              <div class="modal-body">
                                                                ¿Seguro que quiere eliminar el Tipo de artículo <strong>${ta.getDescripcion()}</strong>?
                                                              </div>
                                                              <div class="modal-footer">
                                                                <button type="button" class="btn btn-danger" data-bs-dismiss="modal">No</button>
                                                                <form action="TipoArticuloServlet?menu=TipoArticulo" method="POST">
                                                                  <input type="hidden" name="accion" value="Eliminar">
                                                                  <input type="hidden" name="id" value="${ta.getIdTipoArticulo()}">
                                                                  <button type="submit" class="btn btn-primary">Sí</button>
                                                                </form>
                                                              </div>
                                                            </div>
                                                          </div>
                                                        </div>

                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
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
