<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="grid grid-cols-12 gap-x-6 justify-center">
    <div class="col-span-12 md:col-span-8">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-4">
                <h5 class="mb-0 text-success font-bold"><i class="ri-arrow-left-right-line me-1"></i> Registrar Préstamo de Equipo</h5>
            </div>
            <div class="card-body p-6">
                <jsp:include page="_registrar.jsp"/>
            </div>
        </div>
    </div>
</div>

</t:layout>
