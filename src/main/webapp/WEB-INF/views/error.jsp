<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Something went wrong"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<div class="text-center py-5">
  <div style="font-size:4rem">&#128575;</div>
  <h3>Oops, something went wrong</h3>
  <p class="text-muted">
    <c:choose>
      <c:when test="${not empty message}"><c:out value="${message}"/></c:when>
      <c:when test="${requestScope['javax.servlet.error.status_code'] == 404}">We could not find that page.</c:when>
      <c:otherwise>An unexpected error occurred. Please try again.</c:otherwise>
    </c:choose>
  </p>
  <a class="btn btn-dark" href="${pageContext.request.contextPath}/pets">Back to home</a>
</div>
<%@ include file="/WEB-INF/views/footer.jspf" %>
