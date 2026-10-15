<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Adopter Dashboard"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<h3 class="mb-3">Adopter Dashboard</h3>
<div class="row g-3 mb-4">
  <div class="col-md-4"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">My applications</div><div class="fs-3"><c:out value="${totalApps}"/></div></div></div></div>
  <div class="col-md-4"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Pending</div><div class="fs-3"><c:out value="${pendingApps}"/></div></div></div></div>
  <div class="col-md-4"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Approved</div><div class="fs-3"><c:out value="${approvedApps}"/></div></div></div></div>
</div>
<ul class="nav nav-pills mb-3">
  <li class="nav-item"><a class="nav-link ${tab == 'applications' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/adopter?tab=applications">My Applications</a></li>
  <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/pets">Browse Pets</a></li>
  <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/app/messages">Interaction History</a></li>
</ul>

<c:if test="${tab == 'apply'}">
  <div class="card shadow-sm mb-4"><div class="card-body">
    <h5>Adoption application for <c:out value="${pet.name}"/></h5>
    <p class="text-muted small"><c:out value="${pet.species}"/> &middot; <c:out value="${pet.shelterName}"/></p>
    <form method="post" action="${pageContext.request.contextPath}/app/adopter">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">
      <input type="hidden" name="action" value="apply">
      <input type="hidden" name="petId" value="${pet.petId}">
      <div class="row">
        <div class="col-md-6 mb-3"><label class="form-label">Full name</label>
          <input name="fullName" class="form-control" value="<c:out value='${sessionScope.user.name}'/>" required></div>
        <div class="col-md-6 mb-3"><label class="form-label">Phone</label>
          <input name="phone" class="form-control" placeholder="e.g. 9876543210" required></div>
        <div class="col-12 mb-3"><label class="form-label">Address</label>
          <input name="address" class="form-control" required></div>
        <div class="col-12 mb-3"><label class="form-label">Why do you want to adopt <c:out value="${pet.name}"/>?</label>
          <textarea name="reason" class="form-control" rows="4" maxlength="1000" required></textarea></div>
      </div>
      <button class="btn btn-dark">Submit application</button>
      <a class="btn btn-link" href="${pageContext.request.contextPath}/app/adopter">Cancel</a>
    </form>
  </div></div>
</c:if>

<h5>My Applications</h5>
<div class="table-responsive shadow-sm">
  <table class="table table-hover align-middle mb-0">
    <thead class="table-light"><tr><th>#</th><th>Pet</th><th>Applied on</th><th>Status</th></tr></thead>
    <tbody>
      <c:forEach var="a" items="${applications}">
        <tr>
          <td><c:out value="${a.applicationId}"/></td>
          <td><a href="${pageContext.request.contextPath}/pets?id=${a.petId}"><c:out value="${a.petName}"/></a></td>
          <td><fmt:formatDate value="${a.appliedAt}" pattern="dd MMM yyyy, HH:mm"/></td>
          <td><t:statusBadge status="${a.status}"/></td>
        </tr>
      </c:forEach>
      <c:if test="${empty applications}"><tr><td colspan="4" class="text-center text-muted py-4">You have not applied for any pet yet. <a href="${pageContext.request.contextPath}/pets">Browse pets</a></td></tr></c:if>
    </tbody>
  </table>
</div>
<%@ include file="/WEB-INF/views/footer.jspf" %>
