<%@ page pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true" />
<c:if test="${referrer} != null">
  <meta name="referrer" content="${referrer}" />
</c:if>
<meta name="decorator" content="${decorator}" />
<title>${title} - Insta HMS</title>
<c:if test="${hasFroala}">
  <insta:ui type="css" file="font-awesome.min.css" />
  <script id="fr-fek">
    try {
      (function (k) {
        localStorage.FEK=k;
        t=document.getElementById('fr-fek');
        t.parentNode.removeChild(t);
      })('5Ve1VCQWf1EOQFb1NCg1==');
    } catch(e) {}
  </script>
</c:if>
<style>
  select[name="modifiedCenterId"] {
    height: 216px;
    margin-top: -4px;
    font-size: 12px;
  }
</style>
</head>

<body>
  <div id="app"></div>
  <script>
    <c:forEach items="${globalVars}" var="var">
      ${var.key} = "${var.value}";
    </c:forEach>
    isSettingsPage = ${isSettingsPage};
    contextPath = "${pageContext.request.contextPath}";
    publicPath = contextPath + "/ui/";
    window.sentryOptions = ${sentryOptions};
  </script>
  <insta:ui type="js" file="${language}-manifest.js" />
  <insta:ui type="js" file="${language}-vendor.js" />
  <insta:ui type="js" file="${language}-${bundle}.js" />
</body>
