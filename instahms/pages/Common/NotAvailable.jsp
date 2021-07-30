<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
	<title>Unlicenced - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>

<body>
	<br/><br/>
	<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" 
			class="brB brT brL brR">
		<table>
			<tr>
				<td style="padding-right: 10px; vertical-align: top"><img src="${cpath}/images/error.png"/></td>
				<td>
					The requested screen/report is unavailable because your licence does not
					include this functionality. Please contact Insta Health Solutions customer support
					to resolve this.
				</td>
			</tr>
		</table>
	</div>
	<div>
		<input type="button" name="close" value="Close" onclick="javascript:window.close();">
	</div>
</body>

</html>

