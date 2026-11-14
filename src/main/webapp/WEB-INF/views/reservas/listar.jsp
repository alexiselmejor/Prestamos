<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="flex justify-between items-center mb-6">
    <h4 class="mb-0 font-bold"><i class="ri-calendar-check-line me-2 text-success"></i> Reservas</h4>
    <c:if test="${not esEncargado}">
        <button type="button" class="btn btn-primary"
                data-modal-url="${ctx}/reservas/crear?modal=1"
                data-modal-title="Crear Nueva Reserva"
                data-modal-boton="Crear Reserva">
            <i class="ri-add-circle-line"></i> Nueva Reserva
        </button>
    </c:if>
</div>

<div class="card shadow-sm border-0">
    <div class="card-body p-4 md:p-6">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" id="tablaReservas">
                <thead>
                    <tr>
                        <th>Equipo</th>
                        <c:if test="${esEncargado}"><th>Usuario</th></c:if>
                        <th>Fecha Reserva</th><th>Fecha Inicio</th><th>Fecha Fin</th><th>Estado</th>
                        <c:if test="${esEncargado}"><th>Acciones</th></c:if>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="r" items="${reservas}">
                        <tr>
                            <td><strong>${f:h(r.codigoInventario)}</strong><br><small>${f:h(r.equipoNombre)}</small></td>
                            <c:if test="${esEncargado}">
                                <td>${f:h(r.usuarioNombre)}<br><small>${f:h(r.usuarioEmail)}</small></td>
                            </c:if>
                            <td>${f:fecha(r.fechaReserva, 'dd/MM/yyyy HH:mm')}</td>
                            <td>${f:fecha(r.fechaInicio, 'dd/MM/yyyy HH:mm')}</td>
                            <td>${f:fecha(r.fechaFin, 'dd/MM/yyyy HH:mm')}</td>
                            <td>
                                <span class="badge ${r.estado == 'pendiente' ? 'bg-warning text-dark' : (r.estado == 'confirmada' ? 'bg-info' : (r.estado == 'cancelada' ? 'bg-danger' : 'bg-success'))}">${f:cap(r.estado)}</span>
                            </td>
                            <c:if test="${esEncargado}">
                                <td class="whitespace-nowrap">
                                    <c:if test="${r.estado == 'pendiente'}">
                                        <form method="POST" action="${ctx}/reservas/cambiar-estado" class="inline">
                                            <t:csrf/><input type="hidden" name="id" value="${r.id}"><input type="hidden" name="estado" value="confirmada">
                                            <button type="submit" class="btn-action-icon btn-action-edit" title="Confirmar"><i class="ri-checkbox-circle-line"></i></button>
                                        </form>
                                        <form method="POST" action="${ctx}/reservas/cambiar-estado" class="inline">
                                            <t:csrf/><input type="hidden" name="id" value="${r.id}"><input type="hidden" name="estado" value="cancelada">
                                            <button type="submit" class="btn-action-icon btn-action-delete" title="Cancelar"><i class="ri-close-circle-line"></i></button>
                                        </form>
                                    </c:if>
                                    <c:if test="${r.estado == 'confirmada'}">
                                        <form method="POST" action="${ctx}/reservas/cambiar-estado" class="inline">
                                            <t:csrf/><input type="hidden" name="id" value="${r.id}"><input type="hidden" name="estado" value="completada">
                                            <button type="submit" class="btn-action-icon btn-action-key" title="Completar"><i class="ri-checkbox-line"></i></button>
                                        </form>
                                        <form method="POST" action="${ctx}/reservas/cambiar-estado" class="inline">
                                            <t:csrf/><input type="hidden" name="id" value="${r.id}"><input type="hidden" name="estado" value="cancelada">
                                            <button type="submit" class="btn-action-icon btn-action-delete" title="Cancelar"><i class="ri-close-circle-line"></i></button>
                                        </form>
                                    </c:if>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty reservas}">
                        <tr><td colspan="${esEncargado ? 7 : 6}" class="text-center text-muted py-4">
                            <i class="ri-calendar-line" style="font-size: 2rem;"></i>
                            <p class="mt-2">No hay reservas registradas</p>
                        </td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="flex flex-wrap gap-2 mt-4">
        <button class="btn btn-outline-success" onclick="exportTableToCSV('tablaReservas', 'reservas.csv')"><i class="ri-file-excel-2-line"></i> Exportar Excel</button>
        <button class="btn btn-outline-danger" onclick="exportTableToPDF('tablaReservas', 'Listado de Reservas', 'reservas.pdf')"><i class="ri-file-pdf-2-line"></i> Exportar PDF</button>
        </div>
    </div>
</div>

</t:layout>
