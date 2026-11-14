<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="f" uri="http://universidad.edu/presta/funciones" %>
<c:if test="${not empty errores}">
    <div class="alert alert-danger">
        <ul class="mb-0">
            <c:forEach var="e" items="${errores}"><li>${f:h(e)}</li></c:forEach>
        </ul>
    </div>
</c:if>
