<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- Fragmento del formulario de devolución: se usa en prestamos/devolver.jsp y dentro del modal del panel. --%>

<t:errores/>
<form method="POST" action="${ctx}/prestamos/devolver" class="needs-validation" novalidate>
    <t:csrf/>
    <div class="mb-4">
        <label for="prestamo_id" class="form-label font-bold">Préstamo Activo *</label>
        <select class="form-select" id="prestamo_id" name="prestamo_id" required>
            <option value="">Seleccionar préstamo...</option>
            <c:forEach var="p" items="${prestamosActivos}">
                <option value="${p.id}" ${prestamoSel == p.id ? 'selected' : ''}>${f:h(p.codigoInventario)} - ${f:h(p.equipoNombre)} - ${f:h(p.usuarioNombre)}</option>
            </c:forEach>
        </select>
        <div class="invalid-feedback">Seleccione un préstamo.</div>
        <c:if test="${empty prestamosActivos}">
            <div class="alert alert-info mt-2"><i class="ri-information-line me-1"></i> No hay préstamos activos.</div>
        </c:if>
    </div>

    <div class="grid grid-cols-12 gap-x-6">
        <div class="col-span-12 md:col-span-6 mb-4">
            <label for="fecha_devolucion_real" class="form-label font-bold">Fecha de Devolución Real *</label>
            <input type="datetime-local" class="form-control" id="fecha_devolucion_real" name="fecha_devolucion_real" required value="${fechaRealValor}">
        </div>
        <div class="col-span-12 md:col-span-6 mb-4">
            <label for="estado_equipo" class="form-label font-bold">Estado del Equipo al Devolver</label>
            <select class="form-select" id="estado_equipo" name="estado_equipo">
                <option value="disponible">Disponible</option>
                <option value="mantenimiento">Enviar a Mantenimiento</option>
            </select>
        </div>
    </div>

    <div class="mb-4">
        <label for="observaciones" class="form-label font-bold">Observaciones de Devolución</label>
        <textarea class="form-control" id="observaciones" name="observaciones" rows="3" placeholder="Estado del equipo, accesorios, daños, etc.">${f:h(param.observaciones)}</textarea>
    </div>

    <c:if test="${not esModal}">
    <div class="flex justify-end gap-2 pt-4 border-t acciones-formulario">
        <a href="${ctx}/prestamos/vencidos" class="btn btn-secondary">Cancelar</a>
        <button type="submit" class="btn btn-success"><i class="ri-checkbox-circle-line me-1"></i> Confirmar Devolución</button>
    </div>
    </c:if>
</form>
