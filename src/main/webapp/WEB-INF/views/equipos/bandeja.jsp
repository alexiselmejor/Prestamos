<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="mb-4">
    <h4 class="mb-0 font-bold"><i class="ri-inbox-line me-2 text-success"></i> Bandeja de Solicitudes de Equipos</h4>
    <small class="text-muted">Atiende las solicitudes de los usuarios</small>
</div>

<div class="card shadow-sm border-0">
    <div class="card-header bg-white py-4 px-6 flex justify-between items-center flex-wrap gap-2">
        <h6 class="mb-0 text-dark font-bold"><i class="ri-inbox-line me-2 text-success text-xl"></i> Bandeja de Solicitudes de Equipos</h6>
        <div class="flex gap-2">
            <button type="button" class="btn-export-excel" onclick="exportTableToCSV('tablaSolicitudes', 'solicitudes_equipos.csv')" title="Exportar a Excel (CSV)">
                <i class="ri-file-excel-2-line"></i> Excel
            </button>
            <button type="button" class="btn-export-pdf" onclick="exportTableToPDF('tablaSolicitudes', 'Bandeja de Solicitudes de Equipos', 'solicitudes_equipos.pdf')" title="Exportar a PDF">
                <i class="ri-file-pdf-2-line"></i> PDF
            </button>
        </div>
    </div>
    <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover align-middle mb-0" id="tablaSolicitudes">
                <thead>
                    <tr>
                        <th class="ps-6">Usuario</th>
                        <th>Equipo Solicitado</th>
                        <th>Fecha Préstamo</th>
                        <th>Fecha Devolución</th>
                        <th>Oficina y Área</th>
                        <th>Observaciones</th>
                        <th>Estado</th>
                        <th class="text-right pe-6">Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="sol" items="${solicitudes}">
                        <tr>
                            <td class="ps-6">
                                <div class="font-bold"><c:choose><c:when test="${empty sol.usuarioNombre}">Usuario #${sol.usuarioId}</c:when><c:otherwise>${f:h(sol.usuarioNombre)}</c:otherwise></c:choose></div>
                                <small class="text-muted">${f:h(sol.usuarioEmail)}</small>
                            </td>
                            <td><span class="font-semibold text-dark">${f:h(sol.equipoSolicitado)}</span></td>
                            <td>${f:fecha(sol.fechaPrestamo, 'dd/MM/yyyy HH:mm')}</td>
                            <td>${f:fecha(sol.fechaDevolucion, 'dd/MM/yyyy HH:mm')}</td>
                            <td>
                                <div class="text-[0.875em] text-muted font-bold uppercase" style="font-size: 0.7rem; letter-spacing: 0.5px;">${f:h(sol.area)}</div>
                                <div class="font-semibold">${f:h(sol.oficina)}</div>
                            </td>
                            <td><small class="text-muted">${empty sol.observaciones ? '-' : f:h(sol.observaciones)}</small></td>
                            <td>
                                <c:choose>
                                    <c:when test="${sol.estado == 'aprobada'}">
                                        <span class="badge badge-disponible"><i class="ri-checkbox-circle-fill"></i> Aprobada</span>
                                    </c:when>
                                    <c:when test="${sol.estado == 'rechazada'}">
                                        <span class="badge badge-vencido"><i class="ri-close-circle-fill"></i> Rechazada</span>
                                    </c:when>
                                    <c:when test="${sol.estado == 'devuelta'}">
                                        <span class="badge badge-disponible"><i class="ri-arrow-go-back-line"></i> Devuelta</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-mantenimiento"><i class="ri-time-line"></i> Pendiente</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td class="text-right pe-6">
                                <c:choose>
                                    <c:when test="${sol.estado == 'pendiente'}">
                                        <c:if test="${not esAdmin and solicitudesRegistrables.contains(sol.id)}">
                                            <button type="button" class="btn-action-icon btn-action-edit me-1" title="Registrar préstamo desde esta solicitud"
                                                    data-modal-url="${ctx}/prestamos/registrar?modal=1&amp;solicitud_id=${sol.id}"
                                                    data-modal-title="Registrar Préstamo"
                                                    data-modal-boton="Registrar Préstamo">
                                                <i class="ri-add-circle-line"></i>
                                            </button>
                                        </c:if>
                                        <c:if test="${esAdmin}">
                                            <form method="POST" action="${ctx}/equipos/bandeja" class="inline">
                                                <t:csrf/>
                                                <input type="hidden" name="solicitud_id" value="${sol.id}">
                                                <input type="hidden" name="accion" value="aprobar_solicitud">
                                                <button type="submit" class="btn-action-icon btn-action-edit me-1" title="Aprobar solicitud"><i class="ri-check-line"></i></button>
                                            </form>
                                            <form method="POST" action="${ctx}/equipos/bandeja" class="inline">
                                                <t:csrf/>
                                                <input type="hidden" name="solicitud_id" value="${sol.id}">
                                                <input type="hidden" name="accion" value="rechazar_solicitud">
                                                <button type="submit" class="btn-action-icon btn-action-delete me-1" title="Rechazar solicitud"><i class="ri-close-line"></i></button>
                                            </form>
                                        </c:if>
                                        <form method="POST" action="${ctx}/equipos/bandeja" class="inline form-eliminar" data-confirm="¿Está seguro de eliminar esta solicitud? Esta acción no se puede deshacer.">
                                            <t:csrf/>
                                            <input type="hidden" name="solicitud_id" value="${sol.id}">
                                            <input type="hidden" name="accion" value="eliminar_solicitud">
                                            <button type="submit" class="btn-action-icon btn-action-delete" title="Eliminar solicitud"><i class="ri-delete-bin-line"></i></button>
                                        </form>
                                    </c:when>
                                    <c:when test="${sol.estado == 'aprobada'}">
                                        <c:if test="${not esAdmin and solicitudesRegistrables.contains(sol.id)}">
                                            <button type="button" class="btn-action-icon btn-action-edit me-1" title="Registrar préstamo desde esta solicitud"
                                                    data-modal-url="${ctx}/prestamos/registrar?modal=1&amp;solicitud_id=${sol.id}"
                                                    data-modal-title="Registrar Préstamo"
                                                    data-modal-boton="Registrar Préstamo">
                                                <i class="ri-add-circle-line"></i>
                                            </button>
                                        </c:if>
                                        <form method="POST" action="${ctx}/equipos/bandeja" class="inline form-eliminar" data-confirm="¿Está seguro de eliminar esta solicitud? Esta acción no se puede deshacer.">
                                            <t:csrf/>
                                            <input type="hidden" name="solicitud_id" value="${sol.id}">
                                            <input type="hidden" name="accion" value="eliminar_solicitud">
                                            <button type="submit" class="btn-action-icon btn-action-delete" title="Eliminar solicitud"><i class="ri-delete-bin-line"></i></button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted text-[0.875em] me-1">Atendido</span>
                                        <form method="POST" action="${ctx}/equipos/bandeja" class="inline form-eliminar" data-confirm="¿Está seguro de eliminar esta solicitud? Esta acción no se puede deshacer.">
                                            <t:csrf/>
                                            <input type="hidden" name="solicitud_id" value="${sol.id}">
                                            <input type="hidden" name="accion" value="eliminar_solicitud">
                                            <button type="submit" class="btn-action-icon btn-action-delete" title="Eliminar solicitud"><i class="ri-delete-bin-line"></i></button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty solicitudes}">
                        <tr><td colspan="8" class="text-center text-muted py-12">
                            <i class="ri-inbox-line text-muted opacity-50" style="font-size: 3rem;"></i>
                            <p class="mt-4 mb-0">No hay solicitudes registradas.</p>
                        </td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

</t:layout>