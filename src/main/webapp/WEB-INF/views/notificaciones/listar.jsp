<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="flex flex-wrap justify-between items-center gap-2 mb-6">
    <div>
        <h4 class="mb-1 font-bold"><i class="ri-notification-3-line me-2 text-success"></i> Notificaciones</h4>
        <p class="text-muted text-[0.875em] mb-0">
            <c:choose>
                <c:when test="${notificacionesNoLeidas == 0}">No tienes notificaciones sin leer.</c:when>
                <c:when test="${notificacionesNoLeidas == 1}">Tienes 1 notificación sin leer.</c:when>
                <c:otherwise>Tienes ${notificacionesNoLeidas} notificaciones sin leer.</c:otherwise>
            </c:choose>
        </p>
    </div>
    <form method="POST" action="${ctx}/notificaciones/marcar-todas" class="m-0">
        <t:csrf/>
        <input type="hidden" name="accion" value="marcar_todas">
        <button type="submit" class="btn btn-outline-primary btn-sm" ${notificacionesNoLeidas == 0 ? 'disabled' : ''}>
            <i class="ri-check-double-line me-1"></i> Marcar todas como leídas
        </button>
    </form>
</div>

<ul class="nav nav-pills mb-4" id="pestanasNotificaciones">
    <li class="nav-item">
        <a class="nav-link ${filtroNoLeidas ? '' : 'active'}" href="${ctx}/notificaciones">
            Todas <span class="badge bg-light text-dark ms-1">${notificacionesTodas}</span>
        </a>
    </li>
    <li class="nav-item">
        <a class="nav-link ${filtroNoLeidas ? 'active' : ''}" href="${ctx}/notificaciones?filtro=no-leidas">
            No leídas <span class="badge bg-light text-dark ms-1">${notificacionesNoLeidas}</span>
        </a>
    </li>
</ul>

<div class="card tarjeta-notificaciones">
    <div class="card-body p-0">
        <c:choose>
            <c:when test="${empty notificacionesVisibles}">
                <div class="notificaciones-vacio py-12">
                    <i class="ri-notification-off-line"></i>
                    <p class="notificaciones-vacio-titulo mb-1">Sin notificaciones</p>
                    <p class="text-[0.875em] text-muted mb-0">
                        <c:choose>
                            <c:when test="${filtroNoLeidas}">Ya revisaste todos tus avisos.</c:when>
                            <c:otherwise>Aquí verás los avisos de tus solicitudes, préstamos y de la plataforma.</c:otherwise>
                        </c:choose>
                    </p>
                </div>
            </c:when>
            <c:otherwise>
                <div class="list-group list-group-flush">
                    <c:forEach var="n" items="${notificacionesVisibles}">
                        <a href="${ctx}/notificaciones/ir?clave=${n.clave}&amp;url=${n.enlace}"
                           class="list-group-item list-group-item-action notificacion-item ${n.leida ? '' : 'no-leida'}">
                            <span class="notificacion-icono text-${n.tono}"><i class="${n.icono}"></i></span>
                            <span class="notificacion-texto">
                                <span class="notificacion-titulo">${f:h(n.titulo)}</span>
                                <span class="notificacion-mensaje">${f:h(n.mensaje)}</span>
                                <span class="notificacion-fecha">${f:hace(n.fecha)}</span>
                            </span>
                            <span class="notificacion-punto" aria-hidden="true"></span>
                        </a>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

</t:layout>
