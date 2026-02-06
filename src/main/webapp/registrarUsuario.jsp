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

    <!-- Código para mostrar mensajes con Toastr -->
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
