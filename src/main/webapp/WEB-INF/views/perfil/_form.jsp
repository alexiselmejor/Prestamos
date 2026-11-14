<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<%-- Fragmento del cambio de contraseña: se usa en perfil.jsp y dentro del modal del panel. --%>

<c:if test="${esModal}">
<div class="grid grid-cols-12 gap-6 mb-2">
    <!-- Datos de la persona (lado izquierdo) -->
    <div class="col-span-12 md:col-span-5">
        <div class="card shadow-sm border-0 h-full">
            <div class="card-body text-center p-6">
                <div class="bg-primary-subtle text-primary rounded-full inline-flex p-4 mb-4">
                    <i class="ri-account-circle-line" style="font-size: 3rem;"></i>
                </div>
                <h5 class="font-bold text-dark mb-1">${f:h(usuarioActual.nombre)}</h5>
                <span class="badge bg-primary uppercase px-4 py-1 mb-4">Rol: ${f:h(usuarioActual.rol)}</span>

                <div class="text-left mt-4 pt-4 border-t text-[0.875em]">
                    <div class="mb-2">
                        <span class="text-muted block">Correo Institucional:</span>
                        <strong class="text-dark"><i class="ri-mail-line me-1"></i>${f:h(usuarioActual.email)}</strong>
                    </div>
                    <div class="mb-2">
                        <span class="text-muted block">DNI:</span>
                        <strong class="text-dark"><i class="ri-id-card-line me-1"></i>${empty usuarioActual.dni ? 'No registrado' : f:h(usuarioActual.dni)}</strong>
                    </div>
                    <div class="mb-2">
                        <span class="text-muted block">Celular:</span>
                        <strong class="text-dark"><i class="ri-phone-line me-1"></i>${empty usuarioActual.celular ? 'No registrado' : f:h(usuarioActual.celular)}</strong>
                    </div>
                    <div class="mb-2">
                        <span class="text-muted block">Miembro desde:</span>
                        <strong class="text-dark"><i class="ri-calendar-line me-1"></i>${f:fecha(usuarioActual.fechaRegistro, 'dd/MM/yyyy')}</strong>
                    </div>
                    <div>
                        <span class="text-muted block">Estado:</span>
                        <span class="badge bg-success">Cuenta Activa</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Cambio de contraseña (lado derecho) -->
    <div class="col-span-12 md:col-span-7">
</c:if>

<t:errores/>

<p class="text-muted text-[0.875em] mb-6">
    Ingresa tu contraseña actual y define tu nueva contraseña segura de al menos 8 caracteres.
</p>

<form method="POST" action="${ctx}/perfil" class="needs-validation" novalidate>
    <t:csrf/>
    <input type="hidden" name="accion" value="cambiar_password">

    <div class="mb-4">
        <label for="password_actual" class="form-label font-bold">Contraseña Actual *</label>
        <div class="input-group">
            <span class="input-group-text"><i class="ri-lock-line"></i></span>
            <input type="password" class="form-control" id="password_actual" name="password_actual" required placeholder="••••••••" autocomplete="current-password">
        </div>
    </div>

    <div class="mb-4">
        <label for="nueva_password" class="form-label font-bold">Nueva Contraseña *</label>
        <div class="input-group">
            <span class="input-group-text"><i class="ri-shield-keyhole-line"></i></span>
            <input type="password" class="form-control" id="nueva_password" name="nueva_password" required minlength="8" placeholder="Mínimo 8 caracteres" autocomplete="new-password">
        </div>
        <div class="form-text">Mínimo 8 caracteres. Se recomienda incluir letras y números.</div>
    </div>

    <div class="mb-6">
        <label for="confirmar_password" class="form-label font-bold">Confirmar Nueva Contraseña *</label>
        <div class="input-group">
            <span class="input-group-text"><i class="ri-shield-check-line"></i></span>
            <input type="password" class="form-control" id="confirmar_password" name="confirmar_password" required minlength="8" placeholder="Repite la nueva contraseña" autocomplete="new-password">
        </div>
    </div>

    <c:if test="${not esModal}">
    <div class="flex justify-end gap-2 pt-2 border-t acciones-formulario">
        <a href="${ctx}/dashboard" class="btn btn-secondary">Cancelar</a>
        <button type="submit" class="btn btn-primary"><i class="ri-save-line me-1"></i> Actualizar Contraseña</button>
    </div>
    </c:if>
</form>

<c:if test="${esModal}">
    </div>
</div>
</c:if>