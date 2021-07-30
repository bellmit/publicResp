<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Error - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
</head>
	<body>
		<br/>
		<insta:feedback-panel/>
		<c:if test="${not empty referer}">
			<a href="<c:out value='${referer}' />">Back</a>
		</c:if>
	</body>
</html>
