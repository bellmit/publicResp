<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%--
	convenience for including the standard mrno prefix and length.
	Used in conjunction with addPrefix (common.js) for auto-completing an MRNO.
--%>
<c:set var="mrnoPrefix">
	<c:choose>
		<c:when test="${empty preferences.mrNoPrefix}">MR</c:when>
		<c:otherwise>${ifn:cleanJavaScript(preferences.mrNoPrefix)}</c:otherwise>
	</c:choose>
</c:set>

<c:set var="mrnoDigits">
	<c:choose>
		<c:when test="${empty preferences.mrNoDigits}">6</c:when>
		<c:otherwise>${preferences.mrNoDigits}</c:otherwise>
	</c:choose>
</c:set>

<script>
	var gMrNoPrefix = '${mrnoPrefix}';
	var gMrNoDigits = ${mrnoDigits};
</script>


