<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Messages"/>
<%@ include file="/WEB-INF/views/header.jspf" %>
<h3 class="mb-3">Interaction History</h3>
<div class="row g-4">
  <div class="col-lg-4">
    <div class="card shadow-sm"><div class="card-body">
      <h5>Send a message</h5>
      <c:choose>
        <c:when test="${empty contacts}">
          <p class="text-muted mb-0">
            <c:choose>
              <c:when test="${sessionScope.user.shelter}">You can message adopters once they apply for one of your pets.</c:when>
              <c:otherwise>No shelters are available to message yet.</c:otherwise>
            </c:choose>
          </p>
        </c:when>
        <c:otherwise>
          <form method="post" action="${pageContext.request.contextPath}/app/messages">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="petId" value="<c:out value='${petId}'/>">
            <div class="mb-3"><label class="form-label">To</label>
              <select name="receiverId" class="form-select" required>
                <c:forEach var="u" items="${contacts}">
                  <option value="${u.id}" ${selectedTo == u.id ? 'selected' : ''}><c:out value="${u.name}"/></option>
                </c:forEach>
              </select></div>
            <div class="mb-3"><label class="form-label">Message</label>
              <textarea name="content" class="form-control" rows="4" maxlength="1000" required></textarea></div>
            <button class="btn btn-dark w-100">Send</button>
          </form>
        </c:otherwise>
      </c:choose>
    </div></div>
  </div>
  <div class="col-lg-8">
    <div class="table-responsive shadow-sm">
      <table class="table align-middle mb-0">
        <thead class="table-light"><tr><th>When</th><th>From &rarr; To</th><th>Message</th><th></th></tr></thead>
        <tbody>
          <c:forEach var="m" items="${messages}">
            <tr>
              <td class="text-nowrap small"><fmt:formatDate value="${m.sentAt}" pattern="dd MMM, HH:mm"/></td>
              <td><c:out value="${m.senderName}"/> &rarr; <c:out value="${m.receiverName}"/>
                <c:if test="${not empty m.petName}"><div class="small text-muted">Re: <c:out value="${m.petName}"/></div></c:if></td>
              <td><c:out value="${m.content}"/></td>
              <td>
                <c:if test="${m.receiverId == sessionScope.user.id}">
                  <a class="btn btn-outline-dark btn-sm" href="${pageContext.request.contextPath}/app/messages?to=${m.senderId}<c:if test='${not empty m.petId}'>&petId=${m.petId}</c:if>">Reply</a>
                </c:if>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty messages}"><tr><td colspan="4" class="text-center text-muted py-4">No messages yet.</td></tr></c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>
<%@ include file="/WEB-INF/views/footer.jspf" %>
