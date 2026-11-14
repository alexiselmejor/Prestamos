// Buscador en los desplegables: convierte los <select data-buscable> en un
// desplegable cuya propia caja sirve para buscar (Tom Select, tema Bootstrap 5):
// al hacer clic se abre la lista y al escribir se filtra. El <select> original
// se conserva para que el formulario se envíe igual que antes.
(function () {
    'use strict';

    if (typeof window.TomSelect !== 'function') {
        return;
    }

    var SELECTOR = 'select[data-buscable]';
    var inicializados = new WeakSet();
    var instancias = [];

    /** Destruye las instancias cuyo <select> ya no está en la página (contenido del modal reemplazado). */
    function destruirDesconectadas() {
        for (var i = instancias.length - 1; i >= 0; i--) {
            var ts = instancias[i];
            var select = ts.input;
            if (!select || !document.body.contains(select)) {
                try {
                    ts.destroy();
                } catch (e) {
                    // La instancia ya no es utilizable: se ignora.
                }
                instancias.splice(i, 1);
            }
        }
    }

    function inicializar(raiz) {
        if (!raiz || !raiz.querySelectorAll) {
            return;
        }
        destruirDesconectadas();

        raiz.querySelectorAll(SELECTOR).forEach(function (select) {
            if (inicializados.has(select)) {
                return;
            }
            inicializados.add(select);

            var ts = new TomSelect(select, {
                placeholder: select.getAttribute('data-placeholder') || 'Buscar...',
                allowEmptyOption: false,
                maxOptions: 60,
                onInitialize: function () {
                    this.wrapper.classList.add('buscador-select');
                }
            });

            instancias.push(ts);

            // Refleja la validación de Bootstrap en el control visible.
            select.addEventListener('invalid', function () {
                ts.wrapper.classList.add('is-invalid');
            });
            select.addEventListener('change', function () {
                ts.wrapper.classList.remove('is-invalid');
            });
        });
    }

    // Página completa (por ejemplo /prestamos/registrar).
    document.addEventListener('DOMContentLoaded', function () {
        inicializar(document);
    });

    // Formularios cargados por AJAX dentro del modal genérico.
    var cuerpoModal = document.getElementById('modalFormularioCuerpo');
    if (cuerpoModal) {
        new MutationObserver(function () {
            inicializar(cuerpoModal);
        }).observe(cuerpoModal, { childList: true, subtree: true });
    }
})();
