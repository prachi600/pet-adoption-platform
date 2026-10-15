<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Admin Dashboard"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<h3 class="mb-3">Admin Dashboard</h3>
<div class="row g-3 mb-4">
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Users</div><div class="fs-3"><c:out value="${statUsers}"/></div></div></div></div>
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Listings to review</div><div class="fs-3"><c:out value="${statPendingPets}"/></div></div></div></div>
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Pending applications</div><div class="fs-3"><c:out value="${statPendingApps}"/></div></div></div></div>
  <div class="col-6 col-md-3"><div class="card stat-card shadow-sm"><div class="card-body"><div class="text-muted small">Pets adopted</div><div class="fs-3"><c:out value="${statAdopted}"/></div></div></div></div>
</div>
<ul class="nav nav-pills mb-3">
  <li class="nav-item"><a class="nav-link ${tab == 'users' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/admin?tab=users">User Management</a></li>
  <li class="nav-item"><a class="nav-link ${tab == 'pets' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/admin?tab=pets">Pet Listings</a></li>
  <li class="nav-item"><a class="nav-link ${tab == 'applications' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/admin?tab=applications">Application Tracking</a></li>
  <li class="nav-item"><a class="nav-link ${tab == 'settings' ? 'active' : ''}" href="${pageContext.request.contextPath}/app/admin?tab=settings">System Settings</a></li>
</ul>

<c:if test="${tab == 'users'}">
  <div class="card shadow-sm mb-4"><div class="card-body">
    <h5>Add a user</h5>
    <form method="post" action="${pageContext.request.contextPath}/app/admin" class="row g-2">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}">
      <input type="hidden" name="action" value="createUser">
      <div class="col-md-3"><input name="name" class="form-control" placeholder="Name" required></div>
      <div class="col-md-3"><input name="email" type="email" class="form-control" placeholder="Email" required></div>
      <div class="col-md-2"><input name="password" type="password" minlength="6" class="form-control" placeholder="Password" required></div>
      <div class="col-md-2">
        <select name="role" class="form-select"><option value="ADOPTER">Adopter</option><option value="SHELTER">Shelter</option><option value="ADMIN">Admin</option></select>
      </div>
      <div class="col-md-2"><button class="btn btn-dark w-100">Create</button></div>
    </form>
  </div></div>
  <div class="table-responsive shadow-sm">
    <table class="table align-middle mb-0">
      <thead class="table-light"><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th></th></tr></thead>
      <tbody>
        <c:forEach var="u" items="${users}">
          <tr>
            <td><c:out value="${u.id}"/></td>
            <td><input form="uf-${u.id}" name="name" class="form-control form-control-sm" value="<c:out value='${u.name}'/>"></td>
            <td><input form="uf-${u.id}" name="email" type="email" class="form-control form-control-sm" value="<c:out value='${u.email}'/>"></td>
            <td>
              <select form="uf-${u.id}" name="role" class="form-select form-select-sm">
                <option value="ADOPTER" ${u.role == 'ADOPTER' ? 'selected' : ''}>Adopter</option>
                <option value="SHELTER" ${u.role == 'SHELTER' ? 'selected' : ''}>Shelter</option>
                <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>Admin</option>
              </select>
            </td>
            <td>
              <select form="uf-${u.id}" name="status" class="form-select form-select-sm">
                <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                <option value="SUSPENDED" ${u.status == 'SUSPENDED' ? 'selected' : ''}>Suspended</option>
              </select>
            </td>
            <td class="small"><fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy"/></td>
            <td class="text-nowrap">
              <form id="uf-${u.id}" method="post" action="${pageContext.request.contextPath}/app/admin" class="d-inline">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="userId" value="${u.id}">
                <button name="action" value="updateUser" class="btn btn-outline-dark btn-sm">Save</button>
                <button name="action" value="deleteUser" class="btn btn-outline-danger btn-sm"
                        onclick="return confirm('Delete this user and all of their data?')">Delete</button>
              </form>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</c:if>

<c:if test="${tab == 'pets'}">
  <div class="table-responsive shadow-sm">
    <table class="table align-middle mb-0">
      <thead class="table-light"><tr><th>Pet</th><th>Species / Breed</th><th>Age</th><th>Shelter</th><th>Status</th><th>Action</th></tr></thead>
      <tbody>
        <c:forEach var="p" items="${pets}">
          <tr>
            <td><c:out value="${p.name}"/></td>
            <td><c:out value="${p.species}"/> <span class="text-muted"><c:out value="${p.breed}"/></span></td>
            <td><c:out value="${p.age}"/></td>
            <td><c:out value="${p.shelterName}"/></td>
            <td><t:statusBadge status="${p.status}"/></td>
            <td>
              <c:if test="${p.status == 'PENDING'}">
                <form method="post" action="${pageContext.request.contextPath}/app/admin" class="d-inline">
                  <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                  <input type="hidden" name="action" value="reviewPet">
                  <input type="hidden" name="petId" value="${p.petId}">
                  <button name="decision" value="approve" class="btn btn-success btn-sm">Approve</button>
                  <button name="decision" value="reject" class="btn btn-outline-danger btn-sm">Reject</button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty pets}"><tr><td colspan="6" class="text-center text-muted py-4">No pet listings yet.</td></tr></c:if>
      </tbody>
    </table>
  </div>
</c:if>

<c:if test="${tab == 'applications'}">
  <h5>Pending applications <span class="badge text-bg-warning"><c:out value="${pendingList.size()}"/></span></h5>
  <div class="table-responsive shadow-sm mb-4">
    <table class="table mb-0">
      <thead class="table-light"><tr><th>#</th><th>Pet</th><th>Applicant</th><th>Applied</th></tr></thead>
      <tbody>
        <c:forEach var="a" items="${pendingList}">
          <tr><td><c:out value="${a.applicationId}"/></td><td><c:out value="${a.petName}"/></td><td><c:out value="${a.fullName}"/></td>
            <td><fmt:formatDate value="${a.appliedAt}" pattern="dd MMM yyyy, HH:mm"/></td></tr>
        </c:forEach>
        <c:if test="${empty pendingList}"><tr><td colspan="4" class="text-center text-muted py-3">Nothing pending.</td></tr></c:if>
      </tbody>
    </table>
  </div>
  <h5>Processed applications</h5>
  <div class="table-responsive shadow-sm">
    <table class="table mb-0">
      <thead class="table-light"><tr><th>#</th><th>Pet</th><th>Applicant</th><th>Applied</th><th>Result</th></tr></thead>
      <tbody>
        <c:forEach var="a" items="${processedList}">
          <tr><td><c:out value="${a.applicationId}"/></td><td><c:out value="${a.petName}"/></td><td><c:out value="${a.fullName}"/></td>
            <td><fmt:formatDate value="${a.appliedAt}" pattern="dd MMM yyyy, HH:mm"/></td><td><t:statusBadge status="${a.status}"/></td></tr>
        </c:forEach>
        <c:if test="${empty processedList}"><tr><td colspan="5" class="text-center text-muted py-3">Nothing processed yet.</td></tr></c:if>
      </tbody>
    </table>
  </div>
</c:if>

<c:if test="${tab == 'settings'}">
  <div class="row g-4">
    <div class="col-lg-6">
      <div class="card shadow-sm"><div class="card-body">
        <h5>System settings</h5>
        <form method="post" action="${pageContext.request.contextPath}/app/admin">
          <input type="hidden" name="csrf" value="${sessionScope.csrf}">
          <input type="hidden" name="action" value="updateSettings">
          <div class="mb-3"><label class="form-label">Site name</label>
            <input name="site_name" class="form-control" value="<c:out value='${settings["site_name"]}'/>" required></div>
          <div class="mb-3"><label class="form-label">Adoption fee</label>
            <input name="adoption_fee" class="form-control" value="<c:out value='${settings["adoption_fee"]}'/>" required></div>
          <div class="mb-3"><label class="form-label">Max pending applications per adopter</label>
            <input name="max_pending_applications" type="number" min="1" max="20" class="form-control" value="<c:out value='${settings["max_pending_applications"]}'/>" required></div>
          <div class="form-check form-switch mb-3">
            <input class="form-check-input" type="checkbox" name="applications_open" id="open" ${settings['applications_open'] == 'true' ? 'checked' : ''}>
            <label class="form-check-label" for="open">Accept new adoption applications</label>
          </div>
          <button class="btn btn-dark">Save settings</button>
        </form>
      </div></div>
    </div>
    <div class="col-lg-6">
      <div class="card shadow-sm"><div class="card-body">
        <h5>Recent notifications <small class="text-muted">(sent by background threads)</small></h5>
        <c:choose>
          <c:when test="${empty notifications}"><p class="text-muted mb-0">No notifications sent since the server started.</p></c:when>
          <c:otherwise>
            <ul class="list-group list-group-flush small">
              <c:forEach var="n" items="${notifications}"><li class="list-group-item"><c:out value="${n}"/></li></c:forEach>
            </ul>
          </c:otherwise>
        </c:choose>
      </div></div>
    </div>
  </div>
</c:if>
<%@ include file="/WEB-INF/views/footer.jspf" %>
