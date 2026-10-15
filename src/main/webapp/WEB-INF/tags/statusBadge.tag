<%@ tag body-content="empty" %>
<%@ attribute name="status" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
  <c:when test="${status == 'APPROVED' || status == 'ACTIVE'}"><span class="badge text-bg-success"><c:out value="${status}"/></span></c:when>
  <c:when test="${status == 'ADOPTED'}"><span class="badge text-bg-info"><c:out value="${status}"/></span></c:when>
  <c:when test="${status == 'PENDING'}"><span class="badge text-bg-warning"><c:out value="${status}"/></span></c:when>
  <c:otherwise><span class="badge text-bg-danger"><c:out value="${status}"/></span></c:otherwise>
</c:choose>
