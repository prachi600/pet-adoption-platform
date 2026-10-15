<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Login"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<div class="row justify-content-center">
  <div class="col-md-5">
    <div class="card shadow-sm"><div class="card-body p-4">
      <h3 class="mb-3">Welcome back</h3>
      <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="mb-3"><label class="form-label">Email</label>
          <input type="email" name="email" class="form-control" value="<c:out value='${param.email}'/>" required autofocus></div>
        <div class="mb-3"><label class="form-label">Password</label>
          <input type="password" name="password" class="form-control" required></div>
        <button class="btn btn-dark w-100">Login</button>
      </form>
      <p class="mt-3 mb-0 text-center">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
    </div></div>
    <div class="card mt-3 border-0 bg-transparent"><div class="card-body small text-muted">
      <strong>Demo accounts</strong><br>
      Admin: admin@petadopt.com / Admin@123<br>
      Shelter: shelter@petadopt.com / Shelter@123<br>
      Adopter: adopter@petadopt.com / Adopter@123
    </div></div>
  </div>
</div>
<%@ include file="/WEB-INF/views/footer.jspf" %>
