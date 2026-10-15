<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Browse Pets"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<div class="hero p-4 mb-4">
  <h2 class="mb-1">Find your new best friend</h2>
  <p class="mb-3">Browse pets from verified shelters and apply to adopt online.</p>
  <form method="get" action="${pageContext.request.contextPath}/pets" class="row g-2">
    <div class="col-md-6"><input name="q" class="form-control" placeholder="Search by name, breed or description" value="<c:out value='${q}'/>"></div>
    <div class="col-md-3">
      <select name="species" class="form-select">
        <option value="">All species</option>
        <c:forEach var="s" items="${speciesList}">
          <option value="<c:out value='${s}'/>" ${s.toLowerCase() == species.toLowerCase() ? 'selected' : ''}><c:out value="${s}"/></option>
        </c:forEach>
      </select>
    </div>
    <div class="col-md-3"><button class="btn btn-dark w-100">Search</button></div>
  </form>
</div>
<c:choose>
  <c:when test="${empty pets}">
    <div class="alert alert-secondary">No pets match your search right now.</div>
  </c:when>
  <c:otherwise>
    <div class="row g-3">
      <c:forEach var="p" items="${pets}">
        <div class="col-sm-6 col-lg-3">
          <div class="card pet-card h-100 shadow-sm">
            <div class="card-body text-center">
              <div class="pet-emoji mb-2">
                <c:choose>
                  <c:when test="${p.species.toLowerCase() == 'dog'}">&#128021;</c:when>
                  <c:when test="${p.species.toLowerCase() == 'cat'}">&#128008;</c:when>
                  <c:when test="${p.species.toLowerCase() == 'rabbit'}">&#128007;</c:when>
                  <c:when test="${p.species.toLowerCase() == 'bird'}">&#128038;</c:when>
                  <c:otherwise>&#128062;</c:otherwise>
                </c:choose>
              </div>
              <h5 class="card-title mb-0"><c:out value="${p.name}"/></h5>
              <div class="text-muted small"><c:out value="${p.species}"/><c:if test="${not empty p.breed}"> &middot; <c:out value="${p.breed}"/></c:if></div>
              <div class="small mt-1"><c:out value="${p.age}"/> yr<c:if test="${p.age != 1}">s</c:if> old</div>
              <div class="small text-muted mt-1">&#127968; <c:out value="${p.shelterName}"/></div>
            </div>
            <div class="card-footer bg-white border-0 pb-3">
              <a class="btn btn-outline-dark btn-sm w-100" href="${pageContext.request.contextPath}/pets?id=${p.petId}">View details</a>
            </div>
          </div>
        </div>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/footer.jspf" %>
