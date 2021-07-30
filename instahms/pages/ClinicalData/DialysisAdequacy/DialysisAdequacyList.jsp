<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title><insta:ltext key="clinicaldata.dialysisadequacy.list.dialysisadequacylistinstahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<script language="javascript" type="text/javascript">
var cpath = '<%= request.getContextPath()%>';
</script>
</head>
<body  class="setMargin yui-skin-sam" onload="">
<c:choose>
<c:when test="${param._method=='list' && empty param.mr_no}">
	<h1 style="float: left"><insta:ltext key="clinicaldata.dialysisadequacy.list.dialysisadequacydetails"/></h1>
	<c:url var="url" value="/clinical/DialysisAdequacy.do"/>
	<insta:patientsearch fieldName="mr_no" searchUrl="${url}" searchMethod="show" searchType="mrNo" />
	<form name="adequacyForm" action="${cpath}/clinical/DialysisAdequacy.do" method="post">
	<input type="hidden" name="_searchMethod" value="show"/>
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:when>
<c:otherwise>
	<form name="adequacyForm" action="${cpath}/clinical/DialysisAdequacy.do" method="post">
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}">
</c:otherwise>
</c:choose>
<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>

</body>
</html>