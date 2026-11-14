<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- Fragmento del formulario de reserva: se usa en reservas/crear.jsp y dentro del modal del panel. --%>

<t:errores/>
<form method="POST" action="${ctx}/reservas/crear" class="needs-validation" novalidate>
    <t:csrf/>
    <div class="mb-4">
        <label for="equipo_id" class="form-label">Equipo a Reservar *</label>
        <select class="form-select" id="equipo_id" name="equipo_id" required>
            <option value="">Seleccionar equipo...</option>
            <c:forEach var="eqp" items="${equiposDisponibles}">
                <option value="${eqp.id}" ${param.equipo_id == eqp.id ? 'selected' : ''}>${f:h(eqp.codigoInventario)} - ${f:h(eqp.nombre)}</option>
            </c:forEach>
        </select>
        <div class="invalid-feedback">Seleccione un equipo.</div>
        <c:if test="${empty equiposDisponibles}">
            <div class="alert alert-warning mt-2"><i class="ri-alert-line"></i> No hay equipos disponibles para reservar.</div>
        </c:if>
    </div>

    <div class="grid grid-cols-12 gap-x-6">
        <div class="col-span-12 md:col-span-6 mb-4">
            <label for="fecha_inicio" class="form-label">Fecha de Inicio *</label>
            <input type="datetime-local" class="form-control" id="fecha_inicio" name="fecha_inicio" required
                   min="${minInicio}" value="${f:h(empty param.fecha_inicio ? valorInicio : param.fecha_inicio)}">
            <div class="invalid-feedback">Debe ser hoy o una fecha futura.</div>
        </div>
        <div class="col-span-12 md:col-span-6 mb-4">
            <label for="fecha_fin" class="form-label">Fecha de Fin *</label>
            <input type="datetime-local" class="form-control" id="fecha_fin" name="fecha_fin" required
                   min="${minFin}" value="${f:h(param.fecha_fin)}">
            <div class="invalid-feedback">Debe ser posterior al inicio y a la hora actual.</div>
        </div>
    </div>

    <div class="alert alert-info">
        <i class="ri-information-line"></i> La reserva estará pendiente hasta que un encargado la confirme.
    </div>

    <c:if test="${not esModal}">
    <div class="flex justify-end gap-2 acciones-formulario">
        <a href="${ctx}/reservas/listar" class="btn btn-secondary">Cancelar</a>
        <button type="submit" class="btn btn-primary"><i class="ri-calendar-check-line"></i> Crear Reserva</button>
    </div>
    </c:if>
</form>
