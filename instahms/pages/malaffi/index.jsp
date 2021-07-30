<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<head>
  <meta name="decorator" content="main" />
  <title>Malaffi</title>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<c:choose>
  <c:when test="${fn:length(errors) == 0}">
    <style>
      .fa-zero-width, .foottertxt {
        display: none;
      }
      .contentarea {
        padding: 0;
        height: 100vh;
      }
    </style>
    <form name="malaffiForm" action="${ssoUrl}" method="post">
      <input type="hidden" name="SAMLResponse" value="${xml}" />
    </form>
    <script>
      document.malaffiForm.submit();
    </script>
  </c:when>
  <c:otherwise>
    <div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR">
      <table>
        <tr>
          <td style="padding-right: 10px; vertical-align: top"><img src="${cpath}/images/error.png"/></td>
          <td>
            <p><insta:ltext key="ui.error.error.occurred" /></p>
            <c:forEach items="${errors}" var="error">
              <p>${error}</p>
            </c:forEach>
          </td>
        </tr>
      </table>
    </div>
  </c:otherwise>
</c:choose>