<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="${pet.name}"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<a href="${pageContext.request.contextPath}/pets" class="btn btn-link px-0">&larr; Back to all pets</a>
<div class="card shadow-sm"><div class="card-body p-4">
  <div class="d-flex justify-content-between align-items-start">
    <div>
      <h2 class="mb-1"><c:out value="${pet.name}"/></h2>
      <div class="text-muted"><c:out value="${pet.species}"/><c:if test="${not empty pet.breed}"> &middot; <c:out value="${pet.breed}"/></c:if> &middot; <c:out value="${pet.age}"/> year(s) old</div>
    </div>
    <t:statusBadge status="${pet.status}"/>
  </div>
  <hr>
  <p><c:out value="${empty pet.description ? 'No description provided.' : pet.description}"/></p>
  <p class="mb-1"><strong>Shelter:</strong> <c:out value="${pet.shelterName}"/><c:if test="${not empty pet.shelterCity}">, <c:out value="${pet.shelterCity}"/></c:if></p>
  <div class="mt-4">
    <c:choose>
      <c:when test="${pet.status == 'ADOPTED'}"><div class="alert alert-info mb-0">This pet has found a home.</div></c:when>
      <c:when test="${empty sessionScope.user}">
        <a class="btn btn-dark" href="${pageContext.request.contextPath}/login">Login to adopt</a>
      </c:when>
      <c:when test="${sessionScope.user.adopter}">
        <a class="btn btn-dark" href="${pageContext.request.contextPath}/app/adopter?tab=apply&petId=${pet.petId}">Apply to adopt</a>
        <a class="btn btn-outline-dark" href="${pageContext.request.contextPath}/app/messages?to=${pet.shelterUserId}&petId=${pet.petId}">Message shelter</a>
      </c:when>
      <c:otherwise><div class="text-muted">Only adopter accounts can apply for a pet.</div></c:otherwise>
    </c:choose>
  </div>
</div></div>
<%@ include file="/WEB-INF/views/footer.jspf" %>
