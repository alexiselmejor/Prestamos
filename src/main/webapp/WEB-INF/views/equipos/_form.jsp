<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- Fragmento del formulario de equipos: se usa en equipos/form.jsp y dentro del modal del panel. --%>
<c:set var="vCodigo" value="${esEdicion ? equipo.codigoInventario : param.codigo_inventario}"/>
<c:set var="vNombre" value="${esEdicion ? equipo.nombre : param.nombre}"/>
<c:set var="vDescripcion" value="${esEdicion ? equipo.descripcion : param.descripcion}"/>
<c:set var="vUbicacion" value="${esEdicion ? equipo.ubicacion : param.ubicacion}"/>
<c:set var="vCategoria" value="${esEdicion ? equipo.categoriaId : param.categoria_id}"/>
<c:set var="vEstado" value="${esEdicion ? equipo.estado : param.estado}"/>

<t:errores/>
<form method="POST" action="${ctx}/equipos/${esEdicion ? 'editar' : 'crear'}${esEdicion ? '?id=' : ''}${esEdicion ? equipo.id : ''}" class="needs-validation" novalidate>
    <t:csrf/>
    <div class="grid grid-cols-12 gap-x-6">
        <div class="col-span-12 md:col-span-6 mb-4">
            <label for="codigo_inventario" class="form-label">Código de Inventario *</label>
            <input type="text" class="form-control" id="codigo_inventario" name="codigo_inventario" required value="${f:h(vCodigo)}">
            <div class="invalid-feedback">El código es obligatorio.</div>
        </div>
        <div class="col-span-12 md:col-span-6 mb-4">
            <label for="nombre" class="form-label">Nombre del Equipo *</label>
            <input type="text" class="form-control" id="nombre" name="nombre" required value="${f:h(vNombre)}">
            <div class="invalid-feedback">El nombre es obligatorio.</div>
        </div>
    </div>

    <div class="mb-4">
        <label for="descripcion" class="form-label">Descripción</label>
        <textarea class="form-control" id="descripcion" name="descripcion" rows="3" placeholder="Características del equipo...">${f:h(vDescripcion)}</textarea>
    </div>

    <div class="grid grid-cols-12 gap-x-6">
        <div class="col-span-12 md:col-span-4 mb-4">
            <label for="categoria_id" class="form-label">Categoría *</label>
            <select class="form-select" id="categoria_id" name="categoria_id" required>
                <option value="">Seleccionar...</option>
                <c:forEach var="cat" items="${categorias}">
                    <option value="${cat.id}" ${vCategoria == cat.id ? 'selected' : ''}>${f:h(cat.nombre)}</option>
                </c:forEach>
            </select>
            <div class="invalid-feedback">Seleccione una categoría.</div>
        </div>
        <div class="col-span-12 md:col-span-4 mb-4">
            <label for="ubicacion" class="form-label">Ubicación</label>
            <input type="text" class="form-control" id="ubicacion" name="ubicacion" placeholder="Ej: Laboratorio 2" value="${f:h(vUbicacion)}">
        </div>
        <div class="col-span-12 md:col-span-4 mb-4">
            <label for="estado" class="form-label">${esEdicion ? 'Estado' : 'Estado Inicial'}</label>
            <select class="form-select" id="estado" name="estado">
                <option value="disponible" ${vEstado == 'disponible' ? 'selected' : ''}>Disponible</option>
                <c:if test="${esEdicion}"><option value="prestado" ${vEstado == 'prestado' ? 'selected' : ''}>Prestado</option></c:if>
                <option value="mantenimiento" ${vEstado == 'mantenimiento' ? 'selected' : ''}>En mantenimiento</option>
                <option value="dado_de_baja" ${vEstado == 'dado_de_baja' ? 'selected' : ''}>Dado de baja</option>
            </select>
        </div>
    </div>

    <c:if test="${not esModal}">
    <div class="flex justify-end gap-2 acciones-formulario">
        <a href="${ctx}/equipos/listar" class="btn btn-secondary">Cancelar</a>
        <button type="submit" class="btn btn-primary"><i class="ri-save-line me-1"></i> ${esEdicion ? 'Actualizar Equipo' : 'Guardar Equipo'}</button>
    </div>
    </c:if>
</form>
