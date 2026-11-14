// Funciones JavaScript generales

// Cierra las alertas visibles después de 5 segundos. Se puede volver a llamar
// cuando se insertan alertas nuevas (por ejemplo, dentro de un modal).
window.autoCerrarAlertas = function () {
    document.querySelectorAll('.alert:not([data-auto-cerrada])').forEach(function (alert) {
        alert.setAttribute('data-auto-cerrada', '1');
        
        setTimeout(function () {
            UI.cerrarAlerta(alert);
        }, 5000);
    });
};

document.addEventListener('DOMContentLoaded', function() {

    window.autoCerrarAlertas();
    
    // Confirmación para acciones de eliminar
    // (Manejado por SweetAlert2 en sweetalert-config.js)
    
    // Validación de formularios con Bootstrap (delegado: también aplica a los formularios
    // que se cargan dentro de un modal).
    document.addEventListener('submit', function(event) {
        const form = event.target;
        if (!form || form.tagName !== 'FORM' || !form.classList.contains('needs-validation')) {
            return;
        }
        if (!form.checkValidity()) {
            event.preventDefault();
        }
        form.classList.add('was-validated');
    });
    
    // Rango de fechas: la fecha de fin nunca puede ser anterior a la de inicio.
    // Un mismo campo de inicio puede alimentar varios destinos (el formulario del
    // admin usa "fecha_devolucion_esperada" y el de solicitud del usuario "fecha_devolucion").
    const RANGOS_FECHAS = {
        'fecha_prestamo': ['fecha_devolucion_esperada', 'fecha_devolucion'],
        'fecha_inicio': ['fecha_fin']
    };
    document.addEventListener('change', function(event) {
        const origen = event.target;
        if (!origen || !origen.id) {
            return;
        }
        const idsDestino = RANGOS_FECHAS[origen.id];
        if (!idsDestino) {
            return;
        }
        const raiz = origen.closest('form') || document;
        idsDestino.forEach(function (idDestino) {
            const destino = raiz.querySelector('#' + idDestino);
            if (!destino || !origen.value) {
                return;
            }
            destino.min = origen.value;
            if (destino.value && destino.value < origen.value) {
                destino.value = origen.value;
            }
        });
    });
    
    // Función para formatear fechas
    window.formatDate = function(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };
    
    // Buscar en tablas
    const searchInputs = document.querySelectorAll('.table-search');
    searchInputs.forEach(function(input) {
        input.addEventListener('keyup', function() {
            const searchTerm = this.value.toLowerCase();
            const table = this.closest('.card').querySelector('table tbody');
            if (table) {
                const rows = table.querySelectorAll('tr');
                rows.forEach(function(row) {
                    const text = row.textContent.toLowerCase();
                    row.style.display = text.includes(searchTerm) ? '' : 'none';
                });
            }
        });
    });

    // Menú lateral deslizante (off-canvas) para móvil y tablet
    const sidebar = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarClose = document.getElementById('sidebarClose');
    const sidebarOverlay = document.getElementById('sidebarOverlay');

    function openSidebar() {
        if (!sidebar) return;
        sidebar.classList.add('show');
        sidebarOverlay.classList.add('show');
        document.body.classList.add('sidebar-open');
    }

    function closeSidebar() {
        if (!sidebar) return;
        sidebar.classList.remove('show');
        sidebarOverlay.classList.remove('show');
        document.body.classList.remove('sidebar-open');
    }

    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', openSidebar);
    }
    if (sidebarClose) {
        sidebarClose.addEventListener('click', closeSidebar);
    }
    if (sidebarOverlay) {
        sidebarOverlay.addEventListener('click', closeSidebar);
    }
    // Cerrar el menú al pasar a escritorio
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 992) {
            closeSidebar();
        }
    });
});

// Función para exportar tablas a CSV
function exportTableToCSV(tableId, filename) {
    const table = document.getElementById(tableId);
    if (!table) return;
    
    let csv = [];
    const rows = table.querySelectorAll('tr');
    
    rows.forEach(row => {
        // Se omiten las filas de mensajes (tabla vacía / búsqueda sin coincidencias)
        // y las ocultas por los filtros, pero se incluyen las ocultas solo por paginación.
        const ocultaPorFiltro = row.style.display === 'none' && !row.classList.contains('pagina-oculta');
        if (ocultaPorFiltro || row.id === 'filaSinResultados' || row.classList.contains('fila-mensaje')) return;
        const cols = row.querySelectorAll('td, th');
        const rowData = [];
        cols.forEach(col => {
            rowData.push('"' + col.innerText.replace(/"/g, '""') + '"');
        });
        csv.push(rowData.join(','));
    });
    
    const csvContent = csv.join('\n');
    // BOM para que Excel reconozca acentos y caracteres especiales correctamente
    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    
    if (navigator.msSaveBlob) {
        navigator.msSaveBlob(blob, filename);
    } else {
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}

// Función para exportar tablas a PDF con un diseño organizado (título, fecha, tabla con encabezado a color y numeración de página)
function exportTableToPDF(tableId, title, filename) {
    const table = document.getElementById(tableId);
    if (!table) return;

    if (!window.jspdf || typeof window.jspdf.jsPDF !== 'function') {
        if (typeof window.Swal !== 'undefined') {
            Swal.fire({ icon: 'warning', title: 'Error', text: 'No se pudo cargar el generador de PDF. Verifica tu conexión a internet e inténtalo de nuevo.', confirmButtonColor: '#004d3d' });
        } else {
            alert('No se pudo cargar el generador de PDF. Verifica tu conexión a internet e inténtalo de nuevo.');
        }
        return;
    }

    const { jsPDF } = window.jspdf;

    // Recolectar encabezados, ignorando la columna "Acciones" (botones, no aporta al PDF)
    const headCells = table.querySelectorAll('thead th');
    let accionesIndex = -1;
    const headRow = [];
    headCells.forEach(function (th, idx) {
        const text = th.innerText.trim();
        if (text.toLowerCase() === 'acciones') {
            accionesIndex = idx;
            return;
        }
        headRow.push(text);
    });

    // Recolectar filas de datos
    const bodyRows = [];
    table.querySelectorAll('tbody tr').forEach(function (tr) {
        // Se incluyen las filas ocultas solo por paginación; se omiten las filtradas.
        const ocultaPorFiltro = tr.style.display === 'none' && !tr.classList.contains('pagina-oculta');
        if (ocultaPorFiltro || tr.id === 'filaSinResultados' || tr.classList.contains('fila-mensaje')) return;
        const cells = tr.querySelectorAll('td');
        if (!cells.length) return;
        const row = [];
        cells.forEach(function (td, idx) {
            if (idx === accionesIndex) return;
            row.push(td.innerText.trim().replace(/\s+/g, ' '));
        });
        row.length && bodyRows.push(row);
    });

    const orientation = headRow.length > 5 ? 'landscape' : 'portrait';
    const doc = new jsPDF({ orientation: orientation, unit: 'mm', format: 'a4' });

    try {
        // Título del documento
        doc.setFontSize(16);
        doc.setTextColor(26, 60, 110);
        doc.setFont(undefined, 'bold');
        doc.text(title, 14, 15);

        // Subtítulo con fecha de generación
        doc.setFontSize(10);
        doc.setTextColor(110, 110, 110);
        doc.setFont(undefined, 'normal');
        const fechaGeneracion = new Date().toLocaleDateString('es-ES', {
            year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });
        doc.text('Generado el ' + fechaGeneracion, 14, 21);

        const opcionesTabla = {
            head: [headRow],
            body: bodyRows,
            startY: 26,
            theme: 'grid',
            headStyles: {
                fillColor: [26, 60, 110],
                textColor: 255,
                fontStyle: 'bold',
                halign: 'center',
                fontSize: 9
            },
            bodyStyles: { fontSize: 8.5, valign: 'middle' },
            alternateRowStyles: { fillColor: [244, 247, 250] },
            styles: { cellPadding: 2.5, lineColor: [220, 224, 229], lineWidth: 0.1 },
            margin: { left: 14, right: 14 },
        };

        // Compatibilidad: la mayoría de versiones exponen doc.autoTable(),
        // pero si el CDN cambia y solo queda la función independiente, se usa como respaldo.
        if (typeof doc.autoTable === 'function') {
            doc.autoTable(opcionesTabla);
        } else if (typeof window.jspdf.autoTable === 'function') {
            window.jspdf.autoTable(doc, opcionesTabla);
        } else {
            throw new Error('El complemento de tablas para PDF no está disponible.');
        }

        // Pie de página con numeración
        const pageCount = doc.internal.getNumberOfPages();
        for (let i = 1; i <= pageCount; i++) {
            doc.setPage(i);
            const pageSize = doc.internal.pageSize;
            const pageWidth = pageSize.getWidth();
            const pageHeight = pageSize.getHeight();
            doc.setFontSize(8);
            doc.setTextColor(150, 150, 150);
            doc.text('Sistema de Préstamo de Equipos', 14, pageHeight - 8);
            doc.text('Página ' + i + ' de ' + pageCount, pageWidth - 14, pageHeight - 8, { align: 'right' });
        }

        doc.save(filename);
    } catch (err) {
        console.error('Error al generar el PDF:', err);
        if (typeof window.Swal !== 'undefined') {
            Swal.fire({ icon: 'error', title: 'Error al generar PDF', text: 'Ocurrió un error al generar el PDF. Intenta nuevamente o usa la exportación a Excel.', confirmButtonColor: '#004d3d' });
        } else {
            alert('Ocurrió un error al generar el PDF. Intenta nuevamente o usa la exportación a Excel.');
        }
    }
}