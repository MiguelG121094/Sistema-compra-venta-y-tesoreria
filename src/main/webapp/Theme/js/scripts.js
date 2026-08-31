/*!
    * Start Bootstrap - SB Admin v7.0.7 (https://startbootstrap.com/template/sb-admin)
    * Copyright 2013-2023 Start Bootstrap
    * Licensed under MIT (https://github.com/StartBootstrap/startbootstrap-sb-admin/blob/master/LICENSE)
    */
    //
// Scripts
//

window.addEventListener('DOMContentLoaded', event => {

    // Toggle the side navigation
    const sidebarToggle = document.body.querySelector('#sidebarToggle');
    if (sidebarToggle) {
        // Uncomment Below to persist sidebar toggle between refreshes
        // if (localStorage.getItem('sb|sidebar-toggle') === 'true') {
        //     document.body.classList.toggle('sb-sidenav-toggled');
        // }
        sidebarToggle.addEventListener('click', event => {
            event.preventDefault();
            document.body.classList.toggle('sb-sidenav-toggled');
            localStorage.setItem('sb|sidebar-toggle', document.body.classList.contains('sb-sidenav-toggled'));
        });
    }

});

// Highlight active menu item - using jQuery for better compatibility
$(document).ready(function() {
    var currentPath = window.location.pathname;

    // Get the page/servlet name from current URL
    var currentPage = currentPath.substring(currentPath.lastIndexOf('/') + 1);

    // Se cuenta cuantas entradas del menu apuntan a cada pagina.
    var entradasPorPagina = {};
    $('.sb-sidenav-menu .nav-link').each(function() {
        var linkHref = $(this).attr('href');
        if (!linkHref || linkHref === '#') {
            return;
        }
        var pagina = linkHref.split('?')[0].toLowerCase();
        entradasPorPagina[pagina] = (entradasPorPagina[pagina] || 0) + 1;
    });

    // Find and activate the matching menu link
    $('.sb-sidenav-menu .nav-link').each(function() {
        var linkHref = $(this).attr('href');

        // Skip collapse toggles
        if (!linkHref || linkHref === '#') {
            return;
        }

        // Get the page/servlet name from the link
        var linkPage = linkHref.split('?')[0];

        // Check if this link matches the current page
        if (!currentPage || !linkPage || currentPage.toLowerCase() !== linkPage.toLowerCase()) {
            return;
        }

        // Varias entradas pueden apuntar al mismo servlet y distinguirse solo por sus parametros,
        // como otros debitos y otros creditos. Comparar la pagina las pintaria a las dos, y el
        // query no sirve porque despues de un POST la URL no lo trae: en ese caso la activa la
        // marca menuLateral.jsp desde el servidor, que si sabe cual se esta viendo.
        if (entradasPorPagina[linkPage.toLowerCase()] > 1) {
            return;
        }

        // Add active class
        $(this).addClass('active');
    });

    // Se despliegan los acordeones que contienen la entrada activa, la haya marcado este script
    // o la propia vista.
    $('.sb-sidenav-menu .nav-link.active').parents('.collapse').each(function() {
        $(this).addClass('show');

        // Update the toggle button
        var collapseId = $(this).attr('id');
        var toggleBtn = $('[data-bs-target="#' + collapseId + '"]');
        if (toggleBtn.length) {
            toggleBtn.removeClass('collapsed');
            toggleBtn.attr('aria-expanded', 'true');
        }
    });
});
