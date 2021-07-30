<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>
<head>
	<title>Success - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>
	<body>
		<br/>
		<insta:feedback-panel/>
		<c:out value="${result}"></c:out>
		<table>
		<tr>
		<td>
			<c:url var="url" value="EMRAccessRight.do">
				<c:param name="_method" value="list"/>
				<c:param name="doc_type_id" value="1"/>
			</c:url>
			<a href="<c:out value='${url}' />">goto Access Rule List</a>
		</td>
		</tr>
		</table>
	</body>
</html>
