<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.sql.SQLException"%>

<html>
<head>
<meta http-equiv="Content-Language" content="en-us">
	<meta name="i18nSupport" content="true"/>
<script>
	function toggle() {
		var el = document.getElementById('details');
		if (el) {
			var cur = el.style.display;
			if (cur == 'none') { el.style.display = 'block'; }
			else { el.style.display = 'none'; }
		}
	}
</script>
</head>
<body >

<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>

<table border="2" align="center" cellpadding="3" cellspacing="0" style="border-collapse: collapse" bordercolor="#111111" width="750"    height="100">
  <tr>
  <td height="20" bgcolor="#82B98E" align="center"><div align="center"><b><font color="white"><insta:ltext key="billing.accountexport.exception.dataerror"/></font></b></div></td>
  </tr>
  <tr>
    <td width="750" align="center" height="50"><bean:write name="exceptionMsg"/></td>
  </tr>
</table>

<p align="center"><input type="button" name="b" value="Back" class="button" onclick="history.back();"/></p>

<c:if test="${(patientAccess != true) && (doctorAccess != true)}">
<p align="center"><input type="button" value="Details" onclick="toggle()"></p>
<div id="details" style="display:none">
	<pre>{Exception}
		<%-- <bean:write name="Exception"/> --%>
		<c:forEach var="stack" items="${StackTrace}">
		<%-- <logic:iterate id="stack" name="StackTrace">
			<bean:write name="stack" property="className"/>.<bean:write name="stack" property="methodName"/> 
			(<bean:write name="stack" property="fileName"/>:<bean:write name="stack" property="lineNumber"/>)
		</logic:iterate> --%>
		{stack.className}.{stack.methodName}({stack.fileName}:{stack.lineNumber})
		</c:forEach>
	</pre>
</div>
</c:if>
</body>
</html>

