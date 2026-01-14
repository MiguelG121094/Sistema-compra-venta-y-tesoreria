<%-- 
    Document   : menuSuperior
    Created on : 8/03/2025, 12:43:38 AM
    Author     : Miguel
--%>

<head>
    <!--Agrega estilo al div de sugerencia para el buscador-->
    <style>
        #suggestions {
            border: 1px solid #ccc;
            max-height: 150px;
            overflow-y: auto;
            display: none;
            position: absolute;
            border: 1px solid #ccc;
            background-color: white;
        }
        #suggestions div {
            padding: 8px;
            cursor: pointer;
        }
        #suggestions div:hover {
            background-color: #f1f1f1;
        }
    </style>
</head>
<body>
    <nav class="sb-topnav navbar navbar-expand navbar-dark bg-dark">
        <!-- Navbar Brand-->
        <a class="navbar-brand ps-3" href="MenuPrincipal.jsp">Sistema <br>Compras y Tesorería</a>   
        <!-- Sidebar Toggle-->
        <button class="btn btn-link btn-sm order-1 order-lg-0 me-4 me-lg-0" id="sidebarToggle" href="#!"><i class="fas fa-bars"></i></button>
        <!-- Navbar Search-->
        <form class="d-none d-md-inline-block form-inline ms-auto me-0 me-md-3 my-2 my-md-0">
            <div class="search-container">
            <div class="input-group">
                <input id="searchInput" class="form-control" type="text" placeholder="Buscar Página" />
                <div class="input-group-append">
                    <button class="btn btn-primary" id="btnNavbarSearch" type="button"><i class="fas fa-search"></i></button>
                </div>
            </div>
            <div id="suggestions"></div> <!-- Lista de sugerencias -->
        </div>
        </form>
        <!-- Navbar-->
        <ul class="navbar-nav ms-auto ms-md-0 me-3 me-lg-4">
            <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" id="navbarDropdown" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false"><i class="fas fa-user fa-fw"></i></a>
                <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
                    <li><a class="dropdown-item" href="#!">Configuración</a></li>
                    <li><a class="dropdown-item" href="registrarUsuario.jsp">Agregar Usuario</a></li>
                    <li><hr class="dropdown-divider" /></li>
                    <li><a class="dropdown-item" href="LogoutServlet">Cerrar sesión</a></li>
                </ul>
            </li>
        </ul>
    </nav>
    
    <!--Script para el buscador del menuSuperior-->
    <script>
        const suggestionsDiv = document.getElementById('suggestions');

        // Mostrar sugerencias mientras el usuario escribe
        searchInput.addEventListener('input', function() {
            const query = searchInput.value.trim().toLowerCase();
            suggestionsDiv.innerHTML = ''; // Limpiar sugerencias anteriores

            if (query) {
                const filteredSuggestions = Object.keys(searchMap).filter(key => 
                    key.toLowerCase().includes(query)
                );

                if (filteredSuggestions.length > 0) {
                    filteredSuggestions.forEach(suggestion => {
                        const div = document.createElement('div');
                        div.textContent = suggestion;
                        div.addEventListener('click', () => {
                            searchInput.value = suggestion; // Autocompletar el input
                            suggestionsDiv.style.display = 'none'; // Ocultar sugerencias
                        });
                        suggestionsDiv.appendChild(div);
                    });
                    suggestionsDiv.style.display = 'block'; // Mostrar sugerencias
                } else {
                    suggestionsDiv.style.display = 'none'; // Ocultar si no hay coincidencias
                }
            } else {
                suggestionsDiv.style.display = 'none'; // Ocultar si el input está vacío
            }
        });
//      Agregar todas las paginas aqui para que el buscador le pueda ver
        var searchMap = {
        'inicio': 'MenuPrincipal.jsp',
        'tipo de articulos': 'TipoArticuloServlet?menu=TipoArticulo&accion=Listar',
        'pedido de compra': 'PedidoCompraServlet?menu=PedidoCompra&accion=Listar',
        'base': 'base.jsp',
        'error': 'error.jsp',
        // Agrega más términos y URLs según sea necesario
        };

        document.getElementById('btnNavbarSearch').addEventListener('click', function() {
        var searchQuery = document.getElementById('searchInput').value.trim().toLowerCase();

        if (searchQuery) {
            if (searchMap[searchQuery]) {
                window.location.href = searchMap[searchQuery];
            } else {
                alert('No se encontraron resultados para "' + searchQuery + '".');
            }
        } else {
            alert('Por favor, ingresa un término de búsqueda.');
        }
        });
    </script>
</body>
