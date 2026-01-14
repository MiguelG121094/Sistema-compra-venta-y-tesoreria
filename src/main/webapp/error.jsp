<%-- 
    Document   : error
    Created on : 8/03/2025, 01:28:55 AM
    Author     : Miguel
--%>

<!--bloque de codigo que le dice que no guarde en cache los datos para que al volver atras no 
haga como que al sesion sigue activa-->
<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // Prohibir el almacenamiento en caché
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <!--incluir los scripts y estilos en el header-->
    <jsp:include page="header.jsp" />
    <head>
        <title>Error</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <div class="container text-center mt-5">
        <div class="row">
            <div class="col-md-12">
                <div class="error-template">
                    <h1 class="display-3">¡Oops!</h1>
                    <h2>Ha ocurrido un error</h2>
                    <div class="error-details">
                        Lo sentimos, ocurrió un error inesperado. Por favor, inténtalo nuevamente más tarde.
                    </div>
                    <div class="error-actions mt-4">
                        <a href="MenuPrincipal.jsp" class="btn btn-primary btn-lg">
                            <i class="fas fa-home"></i> Ir al Inicio
                        </a>
                        <a href="mailto:miguelg94@hotmail.com?subject=Problemas%20para%20acceder%20al%20sistema&body=Escriba%20su%20mensaje%20aquí"
                           class="btn btn-secondary btn-lg">
                            <i class="fas fa-envelope"></i> Contactar vía Mail
                        </a>
                        <a href="http://api.whatsapp.com/send?phone=+595984334170&text=Hola, necesito asistencia técnica"
                           class="btn btn-success btn-lg" target="_blank">
                            <i class="fa-brands fa-whatsapp"></i> Contactar vía Whatsapp
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </body>
</html>
