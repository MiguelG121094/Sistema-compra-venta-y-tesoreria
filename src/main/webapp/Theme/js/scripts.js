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

    // Highlight active menu item based on current URL
    const currentUrl = window.location.href.toLowerCase();
    const currentPath = window.location.pathname.toLowerCase();
    const urlParams = new URLSearchParams(window.location.search);
    const currentMenu = urlParams.get('menu');

    const sidenavLinks = document.querySelectorAll('.sb-sidenav-menu .nav-link');

    sidenavLinks.forEach(link => {
        const linkHref = link.getAttribute('href');

        // Skip collapse toggle links (they have # as href)
        if (!linkHref || linkHref === '#') {
            return;
        }

        let isActive = false;

        // Extract servlet/page name from link href
        const linkPath = linkHref.split('?')[0].toLowerCase();

        // Check if current path contains the link path (for servlets and jsp pages)
        if (linkPath && currentPath.includes(linkPath)) {
            isActive = true;
        }

        // Also check by menu parameter if present in the link
        if (!isActive && linkHref.includes('menu=')) {
            const linkMenu = linkHref.split('menu=')[1].split('&')[0];
            if (currentMenu && currentMenu.toLowerCase() === linkMenu.toLowerCase()) {
                isActive = true;
            }
        }

        if (isActive) {
            // Add active class to the link
            link.classList.add('active');

            // Expand all parent collapse elements
            let parent = link.parentElement;
            while (parent) {
                if (parent.classList.contains('collapse')) {
                    parent.classList.add('show');

                    // Find and update the toggle button for this collapse
                    const toggleBtn = document.querySelector(`[data-bs-target="#${parent.id}"]`);
                    if (toggleBtn) {
                        toggleBtn.classList.remove('collapsed');
                        toggleBtn.setAttribute('aria-expanded', 'true');
                    }
                }
                parent = parent.parentElement;
            }
        }
    });

});
