<%-- 
    Document   : registarUsuario
    Created on : 7/03/2025, 05:59:17 PM
    Author     : Miguel
--%>

<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
    <head>
    </head>
    <!--incluir los scripts y estilos en el header-->
    <jsp:include page="header.jsp" />
    <body>

        <div class="container mt-4 col-lg-4">
            <div class="card col-sm-10">
                <div class="card-body">

                    <div class="card-header"><h3 class="text-center font-weight-light my-4">Registrar Usuario</h3></div>
                    <div class="card-body">
                        <form action="UsuarioServlet?menu=Usuario" method="POST">
                            <div class="form-floating mb-3">
                                <input class="form-control" id="user" name="user" type="text" placeholder="Usuario" required="true"/>
                                <label for="user">Usuario</label>
                            </div>
                            <div class="row mb-3">
                                <div class="col-md-6">
                                    <div class="form-floating mb-3 mb-md-0">
                                        <input class="form-control" id="pass" name="pass" type="password" placeholder="Contraseña" required="true" />
                                        <label for="pass">Contraseña</label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-floating mb-3 mb-md-0">
                                        <input class="form-control" id="confirmPass" type="password" placeholder="Confirmar contraseña" required="true"/>
                                        <label for="confirmPass">Confirmar contraseña</label>
                                    </div>
                                </div>
                            </div>
                            <div class="mt-4 mb-0">
                                <div class="d-grid"><button type="submit" name="accion" value="Registrar" class="btn btn-primary btn-block">Solicitar usuario</button></div>
                                <!--<div class="d-grid"><a class="btn btn-primary btn-block" href="login.html">Solicitar usuario</a></div>-->
                            </div>
                        </form>
                    </div>
                    <div class="card-footer text-center py-3">
                        <div class="small"><a href="login.jsp">Volver al Login</a></div>
                    </div>

                </div>
            </div>
        </div>

    <!--codigo para mostrar mensaje-->
    <% String Message = (String) request.getAttribute("Message");%>
    <% String tipoAlert = (String) request.getAttribute("tipoAlert");%>
    <c:if test="${not empty Message}">
        <div id="mensaje" class="alert <%= tipoAlert != null ? tipoAlert : "alert-info"%>"
             style="position:absolute; top: 30px; right: 10px; opacity: 80%; transition: opacity 1s ease; min-width: 200px;" role="alert">
            <%= Message%>
            <button type="button" style="border: none; width: 25px; height: 25px; float:right; display:inline-block; padding:0px 5px;" 
                    class="btn <%= tipoAlert != null ? tipoAlert + " btn-close" : "alert-info"%>" data-bs-dismiss="alert" aria-label="Close">
                <!--<a href="#" class="close" data-dismiss="alert" aria-label="close"></a>-->
            </button>
        </div>
    </c:if>
    <script>
//        setTimeout("document.getElementById('mensaje').style.visibility='hidden'", 7000);
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
