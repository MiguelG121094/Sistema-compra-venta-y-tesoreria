<%-- 
    Document   : login
    Created on : 5/02/2025, 07:31:00 PM
    Author     : Miguel
--%>

<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java"  pageEncoding="UTF-8" %>

<!--decimos no-cache para que no pueda darle atras y volver a la sesion -->
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // Prohibir el almacenamiento en caché
%>

<!-- Recuperar las cookies del login-->
<%
    //obtenemos las cookies del request y almacenamos en la variable cookies
    Cookie[] cookies = request.getCookies();
    String savedUser = "";
    //String savedPass = "";
    boolean rememberChecked = false;

    if (cookies != null) {
        //recorremos la variable cookies y vemos si tiene algo cargado
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("user")) {
                savedUser = cookie.getValue();
            }
            //else if (cookie.getName().equals("pass")) {
            //    savedPass = cookie.getValue();
            //}
        }
    }

    //si hay usuario guardado marcar el checkbox de remeberMe
    if (!savedUser.isEmpty()) {
        rememberChecked = true;
    }

    //enviamos los datos obtenidos de la cookies e nuestros elemntos por el name de cada elemento
    request.setAttribute("savedUser", savedUser);
    //request.setAttribute("savedPass", savedPass);
    request.setAttribute("rememberChecked", rememberChecked);
%>
<html>
    <!--incluir los scripts y estilos en el header-->
    <jsp:include page="header.jsp" />
    <head>
        <title>Login</title>
    </head>
    <body>
        <!--<h2>Iniciar sesión</h2>-->

        <div class="container mt-4 col-lg-4">
            <div class="card col-sm-10">
                <div class="card-body">

                    <form action="LoginServlet" method="post">
                        <div class="card-header"><h3 class="text-center font-weight-light my-4">LOGIN</h3></div>
                        <div class="card-body">
                            <div class="form-floating mb-3">
                                <input class="form-control" id="inputUser" type="text" placeholder="Usuario" name="user" required 
                                       value="<%= request.getAttribute("savedUser") != null ? request.getAttribute("savedUser") : "" %>"/>
                                <label for="inputUser">Usuario</label>
                            </div>
                            <div class="form-floating mb-3">
                                <input class="form-control" id="inputPassword" type="password" placeholder="Contraseña" name="pass" required />
                                <label for="inputPassword">Contraseña</label>
                            </div>
                            <div class="form-check mb-3">
                                <input class="form-check-input" name="rememberMe" type="checkbox"
                                       <%= rememberChecked ? "checked" : "" %>/>
                                <label class="form-check-label" for="rememberMe">Recordar Usuario</label>
                            </div>
                            <div class="d-flex align-items-center justify-content-between mt-4 mb-0">
                                <!--<input class="btn btn-outline-dark" value="¿Olvidó su contraseña?" type="submit" value="Login">-->
                                <a class="small" href="#">¿Olvidó su contraseña?</a>
                                <input class="btn btn-primary" type="submit" value="Login">
                                <!--<a class="btn btn-primary" type="submit" href="index.html">Login</a>-->
                            </div>
                    </form>
                </div>
                <div class="card-footer text-center py-3">
                    <div class="small"><a href="registrarUsuario.jsp">Registrarse</a></div>
                </div>

            </div>
        </div>
    </div>

    <!--codigo para mostrar mensaje con Toastr-->
    <c:if test="${not empty Message}">
        <script>
            toastr.options = {
                "closeButton": true,
                "progressBar": true,
                "positionClass": "toast-top-right",
                "timeOut": "5000"
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
