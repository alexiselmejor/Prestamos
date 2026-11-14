// Buscador en vivo de usuarios del formulario de préstamos.
// Los datos vienen en <script type="application/json" data-global="USUARIOS_PRESTAMO">.
// Los eventos se delegan en "document" para que funcione también dentro del modal.
(function () {
    'use strict';

    function listaUsuarios() {
        const bloque = document.querySelector('script[type="application/json"][data-global="USUARIOS_PRESTAMO"]');
        if (bloque) {
            try {
                const datos = JSON.parse(bloque.textContent);
                if (Array.isArray(datos)) {
                    return datos;
                }
            } catch (e) {
                console.error('No se pudo leer la lista de usuarios:', e);
            }
        }
        return window.USUARIOS_PRESTAMO || [];
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text || '';
        return div.innerHTML;
    }

    function piezas(raiz) {
        return {
            listaUsuarios: listaUsuarios(),
            inputBusqueda: raiz.querySelector('#usuario_busqueda'),
            inputUsuarioId: raiz.querySelector('#usuario_id'),
            listaResultados: raiz.querySelector('#listaResultadosUsuarios'),
            cardSeleccionado: raiz.querySelector('#cardUsuarioSeleccionado'),
            btnLimpiar: raiz.querySelector('#btnLimpiarUsuario'),
            txtNombre: raiz.querySelector('#txtNombreUsuario'),
            txtDni: raiz.querySelector('#txtDniUsuario'),
            txtCelular: raiz.querySelector('#txtCelularUsuario'),
            txtEmail: raiz.querySelector('#txtEmailUsuario')
        };
    }

    function filtrarUsuarios(p, query) {
        query = query.trim().toLowerCase();
        if (!query) {
            p.listaResultados.style.display = 'none';
            return;
        }

        const filtrados = p.listaUsuarios.filter(function (u) {
            const nombre = (u.nombre || '').toLowerCase();
            const dni = (u.dni || '').toLowerCase();
            const celular = (u.celular || '').toLowerCase();
            const email = (u.email || '').toLowerCase();
            return nombre.includes(query) || dni.includes(query) || celular.includes(query) || email.includes(query);
        });

        if (filtrados.length === 0) {
            p.listaResultados.innerHTML = `
                <div class="list-group-item text-muted py-4 text-center">
                    <i class="ri-user-search-line text-2xl block mb-1"></i>
                    No se encontró ningún usuario con "<strong>${escapeHtml(query)}</strong>".
                    <br><small>Verifica el DNI, apellidos o número celular.</small>
                </div>
            `;
            p.listaResultados.style.display = 'block';
            return;
        }

        let html = '';
        filtrados.slice(0, 10).forEach(function (u) {
            const dniBadge = u.dni ? `<span class="badge bg-light text-dark border font-mono ms-2">DNI: ${escapeHtml(u.dni)}</span>` : '';
            const celText = u.celular ? `<span class="me-4"><i class="ri-phone-line text-success me-1"></i>${escapeHtml(u.celular)}</span>` : '';
            const emailText = u.email ? `<span><i class="ri-mail-line text-secondary me-1"></i>${escapeHtml(u.email)}</span>` : '';

            html += `
                <button type="button" class="list-group-item list-group-item-action py-2 px-4 item-usuario" data-id="${u.id}">
                    <div class="flex justify-between items-center">
                        <strong class="text-dark">${escapeHtml(u.nombre)}</strong>
                        ${dniBadge}
                    </div>
                    <div class="text-[0.875em] text-muted mt-1">
                        ${celText}
                        ${emailText}
                    </div>
                </button>
            `;
        });

        p.listaResultados.innerHTML = html;
        p.listaResultados.style.display = 'block';
    }

    function seleccionarUsuario(p, u) {
        p.inputUsuarioId.value = u.id;
        p.inputBusqueda.value = u.nombre + (u.dni ? ' (DNI: ' + u.dni + ')' : '');

        p.txtNombre.textContent = u.nombre;
        p.txtDni.textContent = u.dni || 'No registrado';
        p.txtCelular.textContent = u.celular || 'No registrado';
        if (p.txtEmail) p.txtEmail.textContent = u.email || '';

        p.cardSeleccionado.style.display = 'block';
        p.btnLimpiar.style.display = 'block';
        p.listaResultados.style.display = 'none';
        p.inputBusqueda.classList.remove('is-invalid');
    }

    function limpiarUsuario(p) {
        p.inputUsuarioId.value = '';
        p.inputBusqueda.value = '';
        p.cardSeleccionado.style.display = 'none';
        p.btnLimpiar.style.display = 'none';
        p.listaResultados.style.display = 'none';
        p.inputBusqueda.focus();
    }

    document.addEventListener('input', function (event) {
        const input = event.target.closest ? event.target.closest('#usuario_busqueda') : null;
        if (!input) {
            return;
        }
        const p = piezas(input.closest('form') || document);
        if (!p.inputUsuarioId || !p.listaResultados) {
            return;
        }
        p.inputUsuarioId.value = '';
        p.cardSeleccionado.style.display = 'none';
        p.btnLimpiar.style.display = input.value ? 'block' : 'none';
        filtrarUsuarios(p, input.value);
    });

    document.addEventListener('focusin', function (event) {
        const input = event.target.closest ? event.target.closest('#usuario_busqueda') : null;
        if (!input || !input.value) {
            return;
        }
        const p = piezas(input.closest('form') || document);
        if (!p.inputUsuarioId || !p.listaResultados || p.inputUsuarioId.value) {
            return;
        }
        filtrarUsuarios(p, input.value);
    });

    document.addEventListener('click', function (event) {
        const limpiar = event.target.closest ? event.target.closest('#btnLimpiarUsuario') : null;
        if (limpiar) {
            const p = piezas(limpiar.closest('form') || document);
            if (p.inputUsuarioId) {
                limpiarUsuario(p);
            }
            return;
        }

        const item = event.target.closest ? event.target.closest('.item-usuario') : null;
        if (item) {
            const p = piezas(item.closest('form') || document);
            const id = parseInt(item.getAttribute('data-id'));
            const usuario = p.listaUsuarios.find(function (u) { return parseInt(u.id) === id; });
            if (usuario) {
                seleccionarUsuario(p, usuario);
            }
            return;
        }

        // Si se hace clic fuera del buscador, se ocultan los resultados.
        const contenedor = document.getElementById('contenedorBusquedaUsuario');
        if (contenedor && !contenedor.contains(event.target)) {
            const lista = document.getElementById('listaResultadosUsuarios');
            if (lista) {
                lista.style.display = 'none';
            }
        }
    });
})();
