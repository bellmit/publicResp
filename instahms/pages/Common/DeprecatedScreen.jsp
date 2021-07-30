<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
	<title>Screen Deprecated - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>

<body>
	<h1>Screen Deprecated</h1>
	<div class="helpPanel">
		<table>
			<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
					This screen/report has been deprecated. The same functionality is now available
					as part of a new screen/report.<br/><br/>
					<b><insta:screenlink screenId="${screenId}" extraParam="${extraParam}" label="Click here"/></b> 
					for the new screen/report.
				</td>
			</tr>
		</table>
	</div>
</body>

</html>

