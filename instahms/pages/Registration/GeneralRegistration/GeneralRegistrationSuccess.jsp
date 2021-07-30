<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@ page isELIgnored="false"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="registration.patient.success.title"/></title>
<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<style type="text/css">
	.forminfo {font-weight: bold}
</style>
<script>
	var contextPath = '${cpath}';
</script>
</head>

<body>
<div class="pageHeader"> <insta:ltext key="registration.patient.success.generalregistration"/></div>

<div style="margin-top: 3em" align="center"><insta:feedback-panel/></div>

<table class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="registration.patient.success.hospital.id"/>:</td>
		<td class="forminfo">${ifn:cleanHtml(param.mr_no)}</td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
		<td class="forminfo">${ifn:cleanHtml(param.full_name)}</td>
	</tr>
	<tr>
		<td></td>
	</tr>
	<tr>
		<td></td>
	</tr>
</table>

<table align="center" cellspacing="5" cellpadding="5" border="0">
	<tr>

		<td align="center"> <a
			href="${pageContext.request.contextPath}/pages/registration/GenerateRegistrationCard.do?mrno=${ifn:cleanURL(param.mr_no)}&patid=No&orgName=${patientvisitdetails.map.org_name}"
    		target="_blank"> <insta:ltext key="registration.patient.success.print.registration.card.link"/></a>&nbsp;|&nbsp;</td>
    	<td align="center"> <a
			href="${pageContext.request.contextPath}/pages/registration/GenerateRegistrationBarCode.do?method=execute&mrno=${ifn:cleanURL(param.mr_no)}&barcodeType=Reg"
			target="_blank"> <insta:ltext key="registration.patient.success.print.registration.bar.code.link"/></a>&nbsp;|&nbsp;</td>
				<c:if test="${not empty param.mr_no && preferences.modulesActivatedMap['mod_hie'] eq 'Y'}">
			<td align="center">
			  <a style="cursor: pointer;" onclick="return openConsentUploadDocumentPopUp('${param.mr_no}','${genericPrefs.upload_limit_in_mb}')"><insta:ltext key="js.label.hie.consent"/></a>&nbsp;|&nbsp;</td>
      	</c:if>
    	<td align="center"> <a
			href="${pageContext.request.contextPath}/Registration/GeneralRegistration.do?_method=show"> <insta:ltext key="registration.patient.success.back.to.generalregistration.link"/> </a></td>

	</tr>
</table>

</body>
</html>

