<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%--
	convenience for including the standard billNo prefix and length.
	Used in conjunction with addPrefix (common.js) for auto-completing a bill no.
--%>
<c:set var="billNoPrefix">
	<c:choose>
		<c:when test="${empty preferences.billNoPrefix}">BL</c:when>
		<c:otherwise>${ifn:cleanJavaScript(preferences.billNoPrefix)}</c:otherwise>
	</c:choose>
</c:set>

<c:set var="billNoDigits">
	<c:choose>
		<c:when test="${empty preferences.billNoDigits}">6</c:when>
		<c:otherwise>${preferences.billNoDigits}</c:otherwise>
	</c:choose>
</c:set>

<script>
	var gBillNoPrefix = '${billNoPrefix}';
	var gBillNoDigits = ${billNoDigits};
</script>



