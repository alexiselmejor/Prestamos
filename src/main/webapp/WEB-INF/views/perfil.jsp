<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="grid grid-cols-12 gap-x-6 justify-center">
    <div class="col-span-12 lg:col-span-8">

        <div class="grid grid-cols-12 gap-6">
            <!-- Información del usuario -->
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

            <!-- Cambio de contraseña -->
            <div class="col-span-12 md:col-span-7">
                <div class="card shadow-sm border-0 h-full">
                    <div class="card-header bg-white py-4">
                        <h5 class="mb-0 font-bold text-dark"><i class="ri-key-2-fill me-2 text-success"></i> Cambiar Mi Contraseña</h5>
                    </div>
                    <div class="card-body p-6">
                        <jsp:include page="/WEB-INF/views/perfil/_form.jsp"/>
                    </div>
                </div>
            </div>
        </div>

    </div>
</div>

</t:layout>
