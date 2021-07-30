<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title>HMSError</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>

<body >

	<div style="margin-bottom:20px; margin-top: 20px; padding:10px 0 10px 10px; background-color:#FFC;" 
			class="brB brT brL brR" >
		<table>
			<tr>
				<td style="padding-right: 10px; vertical-align: top"><img src="${cpath}/images/error.png"/></td>
				<td>
					The requested operation caused an internal system error.<br/>
					<c:if test="${(patientAccess != true) && (doctorAccess != true)}">
						You may contact Customer Support and inform them of the error with the status code:${error.status}
						and message: ${error.displayMessage}.
						<br/>
					</c:if>
					${ifn:cleanHtml(msg)}
				</td>
			</tr>
		</table>
	</div>
	<div>
		<input type="button" name="back" value="Back" onclick="javascript:history.back();">
	</div>

</body>
</html>

