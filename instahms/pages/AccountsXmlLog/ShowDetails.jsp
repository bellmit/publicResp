<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="accountxmlog.vouchererror.details.title"/></title>
</head>
<body>
	<h1><insta:ltext key="accountxmlog.vouchererror.details.responsedetails"/></h1>
	<textarea cols="130" rows="35">${file_data}</textarea>
	<c:url var="list" value="/pages/AccountsXmlLog/Log.do">
		<c:param name="_method" value="list"/>
	</c:url>
	<div style="margin-top: 10px">
		<a href="${list}" ><insta:ltext key="accountxmlog.vouchererror.details.accountingimport.exportlist"/></a>
	</div>
</body>
</html>