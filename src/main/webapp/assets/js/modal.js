// Modal genérico para los formularios flotantes del panel.
// Uso desde una vista:  <button data-modal-url="/equipos/crear?modal=1" data-modal-title="Nuevo Equipo">
// Uso desde código:     abrirModal('/equipos/crear?modal=1', 'Nuevo Equipo', { textoBoton: 'Guardar' })
(function () {
    'use strict';

    let modalEl = null;
    let cuerpo = null;
    let tituloEl = null;
    let botonGuardar = null;
    let instancia = null;
    let textoBoton = '<i class="ri-checkbox-circle-line me-1"></i> Guardar';

    function prepararElementos() {
        if (modalEl) {
            return !!cuerpo;
        }
        modalEl = document.getElementById('modalFormulario');
        if (!modalEl) {
            return false;
        }
        cuerpo = document.getElementById('modalFormularioCuerpo');
        tituloEl = document.getElementById('modalFormularioTitulo');
        botonGuardar = document.getElementById('modalFormularioGuardar');
        return !!cuerpo;
    }

    function escapar(texto) {
        const div = document.createElement('div');
        div.textContent = texto == null ? '' : String(texto);
        return div.innerHTML;
    }

    /** Contenedor donde se muestran los errores devueltos por el servidor. */
    function contenedorErrores() {
        let cont = cuerpo.querySelector('.modal-errores');
        if (!cont) {
            cont = document.createElement('div');
            cont.className = 'modal-errores mb-4';
            cuerpo.insertBefore(cont, cuerpo.firstChild);
        }
        return cont;
    }

    function mostrarErrores(errores) {
        const cont = contenedorErrores();
        let mensajeHtml;
        if (!errores || !errores.length) {
            mensajeHtml = 'No se pudo completar la operación. Inténtelo de nuevo.';
            cont.innerHTML = '<div class="alert alert-danger mb-0">' + mensajeHtml + '</div>';
        } else {
            cont.innerHTML = '<div class="alert alert-danger"><ul class="mb-0">'
                + errores.map(function (e) { return '<li>' + escapar(e) + '</li>'; }).join('')
                + '</ul></div>';
            mensajeHtml = errores.join('<br>');
        }

        // Mostrar también con SweetAlert2 si está disponible
        if (typeof Swal !== 'undefined') {
            Swal.fire({
                icon: 'error',
                title: '¡Error!',
                html: '<p class="mb-0" style="font-size:0.95rem;">' + mensajeHtml + '</p>',
                confirmButtonText: '<i class="ri-check-line me-1"></i> Entendido',
                confirmButtonColor: '#004d3d',
                customClass: { popup: 'swal-uncp-popup' }
            });
        }

        cont.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    /** Lee los bloques <script type="application/json" data-global="NOMBRE"> del fragmento. */
    function activarDatosGlobales(raiz) {
        raiz.querySelectorAll('script[type="application/json"][data-global]').forEach(function (bloque) {
            try {
                window[bloque.getAttribute('data-global')] = JSON.parse(bloque.textContent);
            } catch (e) {
                console.error('No se pudo leer el bloque de datos del formulario:', e);
            }
        });
    }

    function urlConAjax(url) {
        const absoluta = new URL(url, window.location.href);
        absoluta.searchParams.set('ajax', '1');
        return absoluta.toString();
    }

    /** La misma URL pero sin el parámetro del fragmento (para abrir la página completa). */
    function urlCompleta(url) {
        const absoluta = new URL(url, window.location.href);
        absoluta.searchParams.delete('modal');
        return absoluta.pathname + absoluta.search;
    }

    function enviar(form) {
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            var primerInvalido = form.querySelector(':invalid');
            var detalle = 'Por favor complete todos los campos obligatorios del formulario.';
            if (primerInvalido) {
                var label = form.querySelector('label[for="' + primerInvalido.id + '"]');
                var nombre = label ? label.textContent.replace('*', '').trim() : primerInvalido.name;
                if (primerInvalido.value.trim() === '') {
                    detalle = 'El campo <strong>"' + (nombre || 'requerido') + '"</strong> no puede estar vacío.';
                } else if (primerInvalido.validationMessage) {
                    detalle = '<strong>' + (nombre || 'Campo') + '</strong>: ' + primerInvalido.validationMessage;
                }
                primerInvalido.focus();
            }
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    icon: 'warning',
                    title: '¡Formulario incompleto!',
                    html: '<p class="text-muted mb-0" style="font-size:0.95rem;">' + detalle + '</p>',
                    confirmButtonText: 'Entendido',
                    confirmButtonColor: '#eab308',
                    customClass: { popup: 'swal-uncp-popup' }
                });
            }
            return;
        }
        form.classList.add('was-validated');

        // Se envía como application/x-www-form-urlencoded (igual que un formulario normal):
        // con multipart/form-data el contenedor no expone los parámetros a getParameter().
        const datos = new URLSearchParams();
        new FormData(form).forEach(function (valor, clave) {
            if (typeof valor === 'string') {
                datos.append(clave, valor);
            }
        });
        datos.append('ajax', '1');

        if (botonGuardar) {
            botonGuardar.disabled = true;
            botonGuardar.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Enviando...';
        }

        fetch(urlConAjax(form.getAttribute('action') || form.action), {
            method: 'POST',
            body: datos.toString(),
            credentials: 'same-origin',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'application/json',
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            }
        })
            .then(function (r) {
                return r.json().catch(function () {
                    return { ok: false, errores: ['El servidor no devolvió una respuesta válida. Recargue la página e inténtelo de nuevo.'] };
                });
            })
            .then(function (data) {
                if (data && data.ok) {
                    cerrar();
                    // Mostrar SweetAlert de éxito antes de recargar
                    if (typeof Swal !== 'undefined') {
                        Swal.fire({
                            icon: 'success',
                            title: '¡Operación exitosa!',
                            text: data.mensaje || 'Los cambios se guardaron correctamente.',
                            confirmButtonText: '<i class="ri-check-line me-1"></i> Entendido',
                            confirmButtonColor: '#004d3d',
                            timer: 2000,
                            timerProgressBar: true,
                            customClass: { popup: 'swal-uncp-popup' }
                        }).then(function () {
                            window.location.reload();
                        });
                    } else {
                        window.location.reload();
                    }
                    return;
                }
                mostrarErrores(data ? data.errores : null);
            })
            .catch(function () {
                mostrarErrores(['No se pudo conectar con el servidor. Inténtelo de nuevo.']);
            })
            .finally(function () {
                if (botonGuardar) {
                    botonGuardar.disabled = false;
                    botonGuardar.innerHTML = textoBoton;
                }
            });
    }

    function cerrar() {
        if (instancia) {
            instancia.hide();
        }
    }

    /**
     * Abre el modal con el formulario indicado.
     * @param {string} url        URL del fragmento (con ?modal=1).
     * @param {string} titulo     Título del modal.
     * @param {object} [opciones] { textoBoton: 'Guardar', sinBoton: false }
     */
    window.abrirModal = function (url, titulo, opciones) {
        opciones = opciones || {};
        if (!prepararElementos()) {
            window.location.href = urlCompleta(url);
            return;
        }

        textoBoton = opciones.textoBoton
            ? '<i class="ri-checkbox-circle-line me-1"></i> ' + opciones.textoBoton
            : '<i class="ri-checkbox-circle-line me-1"></i> Guardar';

        if (tituloEl) {
            tituloEl.textContent = titulo || 'Formulario';
        }
        if (botonGuardar) {
            botonGuardar.innerHTML = textoBoton;
            botonGuardar.style.display = opciones.sinBoton ? 'none' : '';
            botonGuardar.disabled = false;
        }
        cuerpo.innerHTML = '<div class="text-center py-6">'
            + '<div class="spinner-border text-primary" role="status"><span class="sr-only">Cargando...</span></div>'
            + '</div>';

        if (!instancia) {
            instancia = new UI.Modal(modalEl);
        }
        instancia.show();

        fetch(url, {
            credentials: 'same-origin',
            cache: 'no-store',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
            .then(function (r) {
                if (!r.ok) {
                    throw new Error('HTTP ' + r.status);
                }
                return r.text();
            })
            .then(function (html) {
                cuerpo.innerHTML = html;
                activarDatosGlobales(cuerpo);
                // Ejecutar scripts del fragmento (para menús/tabs dinámicos).
                cuerpo.querySelectorAll('script').forEach(function (bloque) {
                    if (bloque.getAttribute('type') === 'application/json') return;
                    if (bloque.src) {
                        const nuevo = document.createElement('script');
                        nuevo.src = bloque.src;
                        document.head.appendChild(nuevo);
                    } else {
                        try { (0, eval)(bloque.textContent); } catch (e) { console.error(e); }
                    }
                });
                const form = cuerpo.querySelector('form');
                if (form) {
                    form.classList.add('needs-validation');
                }
            })
            .catch(function () {
                cuerpo.innerHTML = '<div class="alert alert-danger mb-0">'
                    + '<i class="ri-alert-line me-1"></i> No se pudo cargar el formulario. Inténtelo de nuevo.'
                    + '</div>';
            });
    };

    // Botones con data-modal-url en cualquier página.
    document.addEventListener('click', function (event) {
        const boton = event.target.closest ? event.target.closest('[data-modal-url]') : null;
        if (!boton) {
            return;
        }
        event.preventDefault();
        window.abrirModal(
            boton.getAttribute('data-modal-url'),
            boton.getAttribute('data-modal-title') || 'Formulario',
            { textoBoton: boton.getAttribute('data-modal-boton') || null, sinBoton: boton.getAttribute('data-modal-sinboton') === '1' }
        );
    });

    // Envío de los formularios que viven dentro del modal.
    document.addEventListener('submit', function (event) {
        const form = event.target;
        if (!form || form.tagName !== 'FORM' || !modalEl || !modalEl.contains(form)) {
            return;
        }
        event.preventDefault();
        enviar(form);
    });

    document.addEventListener('DOMContentLoaded', function () {
        if (!prepararElementos() || !botonGuardar) {
            return;
        }
        botonGuardar.addEventListener('click', function () {
            const form = cuerpo.querySelector('form');
            if (!form) {
                return;
            }
            if (typeof form.requestSubmit === 'function') {
                form.requestSubmit();
            } else {
                form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
            }
        });
    });
})();
