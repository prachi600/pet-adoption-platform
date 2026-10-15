<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Shelter Dashboard"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<h3 class="mb-3">Shelter Dashboard <small class="text-muted fs-6"><c:out value="${sessionScope.user.shelterName}"/></small></h3>
<div class="row g-3 mb-4">
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Total pets</div><div class="fs-3"><c:out value="${stats['TOTAL']}"/></div></div></div></div>
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Awaiting approval</div><div class="fs-3"><c:out value="${stats['PENDING']}"/></div></div></div></div>
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Available</div><div class="fs-3"><c:out value="${stats['APPROVED']}"/></div></div></div></div>
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Adopted</div><div class="fs-3"><c:out value="${stats['ADOPTED']}"/></div></div></div></div>
</div>
<ul class="nav nav-pills mb-3">
  <li class="nav-item"><a class="nav-link ${tab == 'pets' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/shelter?tab=pets">Pet Listings</a></li>
  <li class="nav-item"><a class="nav-link ${tab == 'applications' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/shelter?tab=applications">Adoption Applications</a></li>
  <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/app/messages">Interaction History</a></li>
</ul>

<c:if test="${tab == 'pets'}">
  <div class="card shadow-sm mb-4"><div class="card-body">
    <h5>List a new pet</h5>
    <form method="post" action="${pageContext.request.contextPath}/app/shelter" class="row g-2">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">
      <input type="hidden" name="action" value="addPet">
      <div class="col-md-3"><input name="name" class="form-control" placeholder="Name" required></div>
      <div class="col-md-2"><input name="species" class="form-control" placeholder="Species (Dog, Cat...)" required></div>
      <div class="col-md-3"><input name="breed" class="form-control" placeholder="Breed"></div>
      <div class="col-md-2"><input name="age" type="number" min="0" max="40" class="form-control" placeholder="Age (yrs)" required></div>
      <div class="col-md-12"><textarea name="description" class="form-control" rows="2" maxlength="1000" placeholder="Describe the pet's personality and needs"></textarea></div>
      <div class="col-12"><button class="btn btn-dark">Submit for approval</button></div>
    </form>
  </div></div>

  <h5>My pets</h5>
  <div class="table-responsive shadow-sm">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>Name</th><th>Species</th><th>Breed</th><th>Age</th><th>Status</th><th>Listed</th></tr></thead>
      <tbody>
        <c:forEach var="p" items="${pets}">
          <tr>
            <td><c:out value="${p.name}"/></td><td><c:out value="${p.species}"/></td><td><c:out value="${p.breed}"/></td><td><c:out value="${p.age}"/></td>
            <td><t:statusBadge status="${p.status}"/></td>
            <td><fmt:formatDate value="${p.createdAt}" pattern="dd MMM yyyy"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty pets}"><tr><td colspan="6" class="text-center text-muted py-4">No pets listed yet.</td></tr></c:if>
      </tbody>
    </table>
  </div>
</c:if>

<c:if test="${tab == 'applications'}">
  <h5>Adoption applications</h5>
  <div class="table-responsive shadow-sm">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light"><tr><th>Pet</th><th>Applicant</th><th>Contact</th><th>Reason</th><th>Status</th><th>Action</th></tr></thead>
      <tbody>
        <c:forEach var="a" items="${applications}">
          <tr>
            <td><c:out value="${a.petName}"/></td>
            <td><c:out value="${a.fullName}"/><div class="small text-muted"><c:out value="${a.address}"/></div></td>
            <td><c:out value="${a.phone}"/><div class="small text-muted"><c:out value="${a.adopterEmail}"/></div></td>
            <td style="max-width:260px"><c:out value="${a.reason}"/></td>
            <td><t:statusBadge status="${a.status}"/></td>
            <td class="text-nowrap">
              <c:if test="${a.status == 'PENDING'}">
                <form method="post" action="${pageContext.request.contextPath}/app/shelter" class="d-inline">
                  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                  <input type="hidden" name="action" value="decide">
                  <input type="hidden" name="applicationId" value="${a.applicationId}">
                  <button name="decision" value="approve" class="btn btn-success btn-sm">Approve</button>
                  <button name="decision" value="reject" class="btn btn-outline-danger btn-sm">Reject</button>
                </form>
              </c:if>
              <a class="btn btn-link btn-sm" href="${pageContext.request.contextPath}/app/messages?to=${a.adopterId}&petId=${a.petId}">Message</a>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty applications}"><tr><td colspan="6" class="text-center text-muted py-4">No applications received yet.</td></tr></c:if>
      </tbody>
    </table>
  </div>
</c:if>
<%@ include file="/WEB-INF/views/footer.jspf" %>
