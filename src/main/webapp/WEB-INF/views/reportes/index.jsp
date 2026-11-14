<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<style>
    .flip-card { perspective: 1000px; height: 230px; }
    .flip-card-inner { position: relative; width: 100%; height: 100%; transition: transform 0.6s; transform-style: preserve-3d; }
    .flip-card:hover .flip-card-inner { transform: rotateY(180deg); }
    .flip-card-front, .flip-card-back {
        position: absolute; inset: 0; backface-visibility: hidden; border-radius: 1rem;
        padding: 1.25rem; display: flex; flex-direction: column; justify-content: center;
    }
    .flip-card-back { transform: rotateY(180deg); }
</style>

<div class="mb-6 flex flex-wrap justify-between items-center gap-2">
    <div>
        <h4 class="mb-1 font-bold"><i class="ri-file-chart-line text-success me-2"></i> Reportes de Usuarios</h4>
        <p class="text-muted mb-0">Pasa el cursor sobre una tarjeta para ver los datos de la persona. Haz clic para ver su historial de préstamos.</p>
    </div>
    <div class="input-group" style="max-width: 320px;">
        <span class="input-group-text bg-white"><i class="ri-search-line"></i></span>
        <input type="text" class="form-control" id="buscarUsuarioReporte" placeholder="Buscar usuario..." autocomplete="off">
    </div>
</div>

<div class="grid grid-cols-12 gap-4" id="contenedorUsuariosReporte">
    <c:forEach var="u" items="${usuariosReporte}">
        <div class="col-span-12 sm:col-span-6 lg:col-span-4 xl:col-span-3 tarjeta-usuario-reporte"
             data-nombre="${f:h(u.nombre)}" data-email="${f:h(u.email)}">
            <div class="flip-card cursor-pointer"
                 data-modal-url="${ctx}/reportes/usuario?id=${u.id}&amp;modal=1"
                 data-modal-title="Historial de préstamos - ${f:h(u.nombre)}"
                 data-modal-sinboton="1">
                <div class="flip-card-inner">
                    <!-- Frente -->
                    <div class="flip-card-front card shadow-sm border-0 bg-white text-center">
                        <div class="bg-primary-subtle text-primary rounded-full inline-flex p-4 mx-auto mb-3">
                            <i class="ri-user-3-line" style="font-size: 2rem;"></i>
                        </div>
                        <h6 class="font-bold text-dark mb-1">${f:h(u.nombre)}</h6>
                        <span class="badge bg-primary uppercase px-3 py-1">${f:h(u.rol)}</span>
                        <small class="text-muted mt-2">${f:h(u.email)}</small>
                    </div>
                    <!-- Reverso -->
                    <div class="flip-card-back card shadow-sm border-0 bg-primary text-white">
                        <div class="text-left text-[0.875em] space-y-1">
                            <div><i class="ri-mail-line me-1"></i> ${f:h(u.email)}</div>
                            <div><i class="ri-id-card-line me-1"></i> ${empty u.dni ? 'Sin DNI' : f:h(u.dni)}</div>
                            <div><i class="ri-phone-line me-1"></i> ${empty u.celular ? 'Sin celular' : f:h(u.celular)}</div>
                            <div><i class="ri-calendar-line me-1"></i> ${f:fecha(u.fechaRegistro, 'dd/MM/yyyy')}</div>
                        </div>
                        <div class="flex gap-2 mt-3">
                            <span class="badge bg-warning text-dark">Pendientes: ${pendientesPorUsuario[u.id]}</span>
                            <span class="badge bg-danger">Vencidos: ${vencidosPorUsuario[u.id]}</span>
                        </div>
                        <div class="mt-3 text-center">
                            <span class="btn btn-sm btn-light">Ver historial</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>
    <c:if test="${empty usuariosReporte}">
        <div class="col-span-12 text-center text-muted py-12">
            <i class="ri-group-line text-[3rem] block mb-2 opacity-50"></i>
            No hay usuarios registrados en el sistema.
        </div>
    </c:if>
</div>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const input = document.getElementById('buscarUsuarioReporte');
    if (!input) return;
    input.addEventListener('input', function () {
        const texto = input.value.trim().toLowerCase();
        document.querySelectorAll('.tarjeta-usuario-reporte').forEach(function (card) {
            const nombre = (card.getAttribute('data-nombre') || '').toLowerCase();
            const email = (card.getAttribute('data-email') || '').toLowerCase();
            card.style.display = (!texto || nombre.includes(texto) || email.includes(texto)) ? '' : 'none';
        });
    });
});
</script>

</t:layout>