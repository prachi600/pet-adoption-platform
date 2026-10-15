<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Register"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<div class="row justify-content-center">
  <div class="col-md-7">
    <div class="card shadow-sm"><div class="card-body p-4">
      <h3 class="mb-3">Create your account</h3>
      <form method="post" action="${pageContext.request.contextPath}/register">
        <div class="mb-3">
          <label class="form-label d-block">I want to</label>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="role" id="r1" value="ADOPTER" ${param.role != 'SHELTER' ? 'checked' : ''}>
            <label class="form-check-label" for="r1">Adopt a pet (Adopter)</label>
          </div>
          <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" name="role" id="r2" value="SHELTER" ${param.role == 'SHELTER' ? 'checked' : ''}>
            <label class="form-check-label" for="r2">List pets (Shelter)</label>
          </div>
        </div>
        <div class="row">
          <div class="col-md-6 mb-3"><label class="form-label">Full name</label>
            <input name="name" class="form-control" value="<c:out value='${param.name}'/>" required></div>
          <div class="col-md-6 mb-3"><label class="form-label">Email</label>
            <input type="email" name="email" class="form-control" value="<c:out value='${param.email}'/>" required></div>
          <div class="col-md-6 mb-3"><label class="form-label">Password (min 6 characters)</label>
            <input type="password" name="password" class="form-control" minlength="6" required></div>
          <div class="col-md-6 mb-3"><label class="form-label">Confirm password</label>
            <input type="password" name="confirmPassword" class="form-control" required></div>
        </div>
        <div id="shelterFields" style="display:none">
          <hr><h6>Shelter details</h6>
          <div class="row">
            <div class="col-md-6 mb-3"><label class="form-label">Shelter name</label>
              <input name="shelterName" class="form-control" value="<c:out value='${param.shelterName}'/>"></div>
            <div class="col-md-6 mb-3"><label class="form-label">City</label>
              <input name="city" class="form-control" value="<c:out value='${param.city}'/>"></div>
            <div class="col-md-6 mb-3"><label class="form-label">Phone</label>
              <input name="phone" class="form-control" value="<c:out value='${param.phone}'/>"></div>
            <div class="col-md-6 mb-3"><label class="form-label">Address</label>
              <input name="address" class="form-control" value="<c:out value='${param.address}'/>"></div>
          </div>
        </div>
        <button class="btn btn-dark w-100">Register</button>
      </form>
      <p class="mt-3 mb-0 text-center">Already registered? <a href="${pageContext.request.contextPath}/login">Login</a></p>
    </div></div>
  </div>
</div>
<script>
  (function () {
    var box = document.getElementById('shelterFields');
    function sync() { box.style.display = document.getElementById('r2').checked ? 'block' : 'none'; }
    document.getElementById('r1').addEventListener('change', sync);
    document.getElementById('r2').addEventListener('change', sync);
    sync();
  })();
</script>
<%@ include file="/WEB-INF/views/footer.jspf" %>
