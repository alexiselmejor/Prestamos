<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="grid grid-cols-12 gap-x-6 justify-center">
    <div class="col-span-12 md:col-span-8">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white"><h5 class="mb-0 font-bold"><i class="ri-calendar-schedule-line me-2 text-success"></i> Crear Nueva Reserva</h5></div>
            <div class="card-body">
                <jsp:include page="_crear.jsp"/>
            </div>
        </div>
    </div>
</div>

</t:layout>
