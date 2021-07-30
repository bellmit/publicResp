<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><insta:ltext key="patient.dialysis.sessions.noprescriptions.title"/></title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body >
<div class="pageHeader"><insta:ltext key="patient.dialysis.sessions.noprescriptions.pageHeader"/></div>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
<form>
	<table width="100%">
		<tr height="20" />
		<tr align="center">
			<td><b><insta:ltext key="patient.dialysis.sessions.noprescriptions.message1"/></br>
			<insta:ltext key="patient.dialysis.sessions.noprescriptions.message2"/></b></td>
		</tr>
	</table>
	<table width="100%">
		<tr height="20" />
		<tr align="center">
			<td>
				<a href="DialysisPrescriptions.do?_method=list&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.noprescriptions.pres.list"/></a> |
				<a href="DialysisPrescriptions.do?_method=add&mr_no=${ifn:cleanURL(param.mr_no)}"><insta:ltext key="patient.dialysis.sessions.noprescriptions.new.pres"/></a>
			</td>
		</tr>
	</table>
</form>
</body>
</html>
