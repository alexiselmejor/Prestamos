/*
 * ui.js — comportamiento de dropdowns, pestañas, alertas y ventanas modales.
 * Reemplaza a bootstrap.bundle.js (el sistema usa ahora Tailwind CSS).
 *
 * Atributos soportados:
 *   data-toggle="dropdown"                      botón que abre/cierra el .dropdown-menu hermano
 *   data-auto-close="outside"                   el menú solo se cierra al hacer clic fuera
 *   data-toggle="pill" data-target="#panel"     pestaña (el panel es un .tab-pane)
 *   data-dismiss="alert" | "modal"              cierra la alerta / el modal contenedor
 *
 * API pública (window.UI):
 *   new UI.Modal(elemento).show() / .hide()
 *   UI.cerrarAlerta(elemento)
 */
(function () {
    'use strict';

    /* ---------- Dropdowns ---------- */
    function cerrarMenus(excepto) {
        document.querySelectorAll('.dropdown-menu.show').forEach(function (menu) {
            if (menu === excepto) return;
            menu.classList.remove('show');
            var boton = menu.parentElement && menu.parentElement.querySelector('[data-toggle="dropdown"]');
            if (boton) boton.setAttribute('aria-expanded', 'false');
        });
    }

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-toggle="dropdown"]');
        if (boton) {
            e.preventDefault();
            var menu = boton.parentElement.querySelector('.dropdown-menu');
            if (!menu) return;
            var abrir = !menu.classList.contains('show');
            cerrarMenus(abrir ? menu : null);
            menu.classList.toggle('show', abrir);
            boton.setAttribute('aria-expanded', abrir ? 'true' : 'false');
            return;
        }
        // Clic dentro de un menú con data-auto-close="outside": no se cierra
        var dentro = e.target.closest('.dropdown');
        if (dentro) {
            var disparador = dentro.querySelector('[data-toggle="dropdown"]');
            if (disparador && disparador.getAttribute('data-auto-close') === 'outside'
                && e.target.closest('.dropdown-menu')) {
                return;
            }
        }
        cerrarMenus(null);
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') cerrarMenus(null);
    });

    /* ---------- Pestañas ---------- */
    document.addEventListener('click', function (e) {
        var pestana = e.target.closest('[data-toggle="pill"], [data-toggle="tab"]');
        if (!pestana) return;
        e.preventDefault();
        var panel = document.querySelector(pestana.getAttribute('data-target'));
        if (!panel) return;
        var lista = pestana.closest('.nav');
        if (lista) {
            lista.querySelectorAll('[data-toggle="pill"], [data-toggle="tab"]').forEach(function (b) {
                b.classList.remove('active');
                b.setAttribute('aria-selected', 'false');
            });
        }
        pestana.classList.add('active');
        pestana.setAttribute('aria-selected', 'true');
        var contenedor = panel.parentElement;
        contenedor.querySelectorAll(':scope > .tab-pane').forEach(function (p) {
            p.classList.remove('show', 'active');
        });
        panel.classList.add('show', 'active');
    });

    /* ---------- Alertas ---------- */
    function cerrarAlerta(alerta) {
        if (!alerta || !alerta.isConnected) return;
        alerta.classList.remove('show');
        setTimeout(function () { alerta.remove(); }, 160);
    }

    document.addEventListener('click', function (e) {
        var cerrar = e.target.closest('[data-dismiss="alert"]');
        if (cerrar) cerrarAlerta(cerrar.closest('.alert'));
    });

    /* ---------- Modal ---------- */
    function Modal(elemento) {
        this.el = elemento;
        this.fondo = null;
    }

    Modal.prototype.show = function () {
        var el = this.el;
        var self = this;
        if (el.classList.contains('show')) return;
        this.fondo = document.createElement('div');
        this.fondo.className = 'modal-backdrop fade';
        document.body.appendChild(this.fondo);
        document.body.classList.add('modal-open');
        el.style.display = 'block';
        el.removeAttribute('aria-hidden');
        el.setAttribute('aria-modal', 'true');
        void el.offsetWidth; // fuerza el reflujo para que anime
        el.classList.add('show');
        this.fondo.classList.add('show');
        var foco = el.querySelector('[autofocus], input:not([type=hidden]), select, textarea');
        if (foco) setTimeout(function () { foco.focus(); }, 150);

        el.addEventListener('mousedown', el._uiFuera = function (e) {
            if (e.target === el) self.hide();
        });
        document.addEventListener('keydown', el._uiTecla = function (e) {
            if (e.key === 'Escape') self.hide();
        });
    };

    Modal.prototype.hide = function () {
        var el = this.el;
        var fondo = this.fondo;
        if (!el.classList.contains('show')) return;
        el.classList.remove('show');
        if (fondo) fondo.classList.remove('show');
        el.removeEventListener('mousedown', el._uiFuera);
        document.removeEventListener('keydown', el._uiTecla);
        setTimeout(function () {
            el.style.display = '';
            el.setAttribute('aria-hidden', 'true');
            el.removeAttribute('aria-modal');
            if (fondo) fondo.remove();
            document.body.classList.remove('modal-open');
            el.dispatchEvent(new CustomEvent('hidden.ui.modal'));
        }, 300);
    };

    document.addEventListener('click', function (e) {
        var cerrar = e.target.closest('[data-dismiss="modal"]');
        if (!cerrar) return;
        var el = cerrar.closest('.modal');
        if (el && el._uiInstancia) el._uiInstancia.hide();
    });

    // Cada modal creado se registra para que data-dismiss lo encuentre
    var Original = Modal;
    function ModalRegistrado(elemento) {
        if (elemento._uiInstancia) return elemento._uiInstancia;
        var m = new Original(elemento);
        elemento._uiInstancia = m;
        return m;
    }

    window.UI = {
        Modal: ModalRegistrado,
        cerrarAlerta: cerrarAlerta
    };
})();
