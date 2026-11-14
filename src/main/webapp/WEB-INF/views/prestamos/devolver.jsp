<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<t:layout title="${pageTitle}">

<div class="grid grid-cols-12 gap-x-6 justify-center">
    <div class="col-span-12 md:col-span-8">
        <div class="card">
            <div class="card-header bg-white py-4"><h5 class="mb-0 font-bold"><i class="ri-checkbox-circle-line me-1 text-success"></i> Devolver Equipo</h5></div>
            <div class="card-body">
                <jsp:include page="_devolver.jsp"/>
            </div>
        </div>
    </div>
</div>

</t:layout>
