<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="mb-4 flex items-center gap-3">
    <div class="bg-primary-subtle text-primary rounded-full inline-flex p-3">
        <i class="ri-user-3-line" style="font-size: 1.5rem;"></i>
    </div>
    <div>
        <h5 class="font-bold text-dark mb-0">${f:h(usuarioHistorial.nombre)}</h5>
        <small class="text-muted">${f:h(usuarioHistorial.email)}</small>
    </div>
</div>

<!-- Prestando -->
<div class="card shadow-sm border-0 mb-4">
    <div class="card-header bg-white py-3 px-4 flex flex-wrap justify-between items-center gap-2">
        <h6 class="mb-0 text-dark font-bold"><i class="ri-play-circle-line me-2 text-success text-xl"></i> Prestando</h6>
        <div class="flex gap-2">
            <button type="button" class="btn-export-excel" onclick="exportTableToCSV('histPrestando', 'historial_prestando.csv')" title="Exportar a Excel (CSV)"><i class="ri-file-excel-2-line"></i> Excel</button>
            <button type="button" class="btn-export-pdf" onclick="exportTableToPDF('histPrestando', 'Historial - Prestando', 'historial_prestando.pdf')" title="Exportar a PDF"><i class="ri-file-pdf-2-line"></i> PDF</button>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" id="histPrestando">
                <thead>
                    <tr>
                        <th class="ps-4">Equipo</th>
                        <th>Código</th>
                        <th>Fecha Préstamo</th>
                        <th>Devolución Esperada</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <c:set var="hayPrestando" value="false"/>
                    <c:forEach var="p" items="${prestamosHistorial}">
                        <c:if test="${empty p.fechaDevolucionReal}">
                            <c:set var="hayPrestando" value="true"/>
                            <tr>
                                <td class="ps-4"><div class="font-semibold text-dark">${f:h(p.equipoNombre)}</div></td>
                                <td><span class="code-pill">${f:h(p.codigoInventario)}</span></td>
                                <td>${f:fecha(p.fechaPrestamo, 'dd/MM/yyyy HH:mm')}</td>
                                <td>${f:fecha(p.fechaDevolucionEsperada, 'dd/MM/yyyy')}</td>
                                <td><span class="badge ${p.estado == 'vencido' ? 'badge-vencido' : 'badge-prestado'}">${f:cap(p.estado)}</span></td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:if test="${!hayPrestando}">
                        <tr><td colspan="5" class="text-center text-muted py-6">No tiene equipos en préstamo actualmente.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        <div class="card-footer bg-white py-2 px-4">
            <nav><ul class="pagination pagination-sm mb-0" id="pagHistPrestando"></ul></nav>
        </div>
    </div>
</div>

<!-- Devueltos -->
<div class="card shadow-sm border-0">
    <div class="card-header bg-white py-3 px-4 flex flex-wrap justify-between items-center gap-2">
        <h6 class="mb-0 text-dark font-bold"><i class="ri-arrow-go-back-line me-2 text-success text-xl"></i> Devueltos</h6>
        <div class="flex gap-2">
            <button type="button" class="btn-export-excel" onclick="exportTableToCSV('histDevueltos', 'historial_devueltos.csv')" title="Exportar a Excel (CSV)"><i class="ri-file-excel-2-line"></i> Excel</button>
            <button type="button" class="btn-export-pdf" onclick="exportTableToPDF('histDevueltos', 'Historial - Devueltos', 'historial_devueltos.pdf')" title="Exportar a PDF"><i class="ri-file-pdf-2-line"></i> PDF</button>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" id="histDevueltos">
                <thead>
                    <tr>
                        <th class="ps-4">Equipo</th>
                        <th>Código</th>
                        <th>Fecha Préstamo</th>
                        <th>Devuelto el</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <c:set var="hayDevueltos" value="false"/>
                    <c:forEach var="p" items="${prestamosHistorial}">
                        <c:if test="${not empty p.fechaDevolucionReal}">
                            <c:set var="hayDevueltos" value="true"/>
                            <tr>
                                <td class="ps-4"><div class="font-semibold text-dark">${f:h(p.equipoNombre)}</div></td>
                                <td><span class="code-pill">${f:h(p.codigoInventario)}</span></td>
                                <td>${f:fecha(p.fechaPrestamo, 'dd/MM/yyyy HH:mm')}</td>
                                <td>${f:fecha(p.fechaDevolucionReal, 'dd/MM/yyyy HH:mm')}</td>
                                <td><span class="badge badge-disponible">Devuelto</span></td>
                            </tr>
                        </c:if>
                    </c:forEach>
                    <c:if test="${!hayDevueltos}">
                        <tr><td colspan="5" class="text-center text-muted py-6">No tiene equipos devueltos.</td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
        <div class="card-footer bg-white py-2 px-4">
            <nav><ul class="pagination pagination-sm mb-0" id="pagHistDevueltos"></ul></nav>
        </div>
    </div>
</div>

<script>
(function () {
    function paginar(idTabla, idPag, porPagina) {
        var tabla = document.getElementById(idTabla);
        var cont = document.getElementById(idPag);
        if (!tabla || !cont) return;
        var filas = Array.from(tabla.querySelectorAll('tbody tr'));
        var total = Math.ceil(filas.length / porPagina);
        if (total <= 1) { cont.style.display = 'none'; return; }
        var pagina = 1;
        function render() {
            filas.forEach(function (fila, idx) {
                fila.style.display = (idx >= (pagina - 1) * porPagina && idx < pagina * porPagina) ? '' : 'none';
            });
            cont.innerHTML = '';
            for (var i = 1; i <= total; i++) {
                var li = document.createElement('li');
                li.className = 'page-item' + (i === pagina ? ' active' : '');
                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'page-link';
                btn.textContent = i;
                (function (p) { btn.addEventListener('click', function () { pagina = p; render(); }); })(i);
                li.appendChild(btn);
                cont.appendChild(li);
            }
        }
        render();
    }
    paginar('histPrestando', 'pagHistPrestando', 2);
    paginar('histDevueltos', 'pagHistDevueltos', 2);
})();
</script>