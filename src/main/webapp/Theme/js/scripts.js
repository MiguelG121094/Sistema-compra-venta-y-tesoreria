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
    var currentUrl = window.location.href;
    var currentPath = window.location.pathname;

    // Get the page/servlet name from current URL
    var currentPage = currentPath.substring(currentPath.lastIndexOf('/') + 1);

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
        if (currentPage && linkPage && currentPage.toLowerCase() === linkPage.toLowerCase()) {
            // Add active class
            $(this).addClass('active');

            // Expand all parent collapse elements
            $(this).parents('.collapse').each(function() {
                $(this).addClass('show');

                // Update the toggle button
                var collapseId = $(this).attr('id');
                var toggleBtn = $('[data-bs-target="#' + collapseId + '"]');
                if (toggleBtn.length) {
                    toggleBtn.removeClass('collapsed');
                    toggleBtn.attr('aria-expanded', 'true');
                }
            });
        }
    });
});
