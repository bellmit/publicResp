<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title>
<insta:ltext key="patient.admissionrequest.cancel.admission.request.title"/>
</title>
<insta:link type="script" file="date_go.js"/>
<insta:js-bundle prefix="patient.canceladmissionrequest"/>
<script>
	function validateCancel() {
		var cancelledOn = document.CancelAdmissionRequestFrom.cancellation_date;
		var cancellationRemarks = document.CancelAdmissionRequestFrom.cancel_remarks;
		if(empty(cancelledOn.value)) {
			showMessage("js.patient.canceladmissionrequest.validation.cacellation.date.required");
			cancelledOn.focus();
			return false;
		}
		if(empty(cancellationRemarks.value)) {
			showMessage("js.patient.canceladmissionrequest.validation.cacellation.cancellationremarks.required");
			cancellationRemarks.focus();
			return false;
		}
		return true;
	}
</script>
</head>
<body>
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<h1><insta:ltext key="patient.admissionrequest.cancel.admission.request.header"/></h1>
<insta:feedback-panel />
<insta:patientgeneraldetails mrno="${param.mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form action="admissionrequest.do" method="POST" name="CancelAdmissionRequestFrom">
<input type="hidden" name="_method" value="cancelAdmissionRequest" />
<input type="hidden" name="adm_request_id" value="${ifn:cleanHtmlAttribute(param.adm_request_id)}" />
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
<jsp:useBean id="today" class="java.util.Date"/>
<fmt:formatDate var="today" value="${today}" pattern="dd-MM-yyyy"/>
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.admissionrequest.cancel.admission.request.cancelledby"/>:&nbsp;</td>
			<td class="forminfo">${not empty admissionBean ? admissionBean.cancelled_by : userName}
			   <input type="hidden" name="cancelledBy" id="cancelledBy" value="${not empty admissionBean ? admissionBean.cancelled_by : userName}"/>
			 </td>
			<td class="formlabel"><insta:ltext key="patient.admissionrequest.cancel.admission.request.cancellationdate"/>:</td>
			<c:set var="cancellationDate">
				<fmt:formatDate value="${admissionBean.cancelled_on}" pattern="dd-MM-yyyy"/>
			</c:set>
			<td><insta:datewidget name="cancellation_date" valid="past" value="${empty admissionBean ? today : cancellationDate}" /></td>
			<td class="formlabel">&nbsp;</td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.admissionrequest.cancel.admission.request.cancellationremarks"/>:&nbsp;</td>
			<td class="forminfo" colspan="3">
			   <input type="text" name="cancel_remarks" id="cancel_remarks" value="${admissionBean.cancellation_remarks}" style="width:600px;" maxlength="500"/>
			 </td>
		</tr>
	</table>
	<div class="screenActions">
		<button type="submit" name="submit"  onclick="return validateCancel();" ${admissionBean.status == 'X' ? 'disabled' : ''}>
			<b><u><insta:ltext key="patient.admissionrequest.cancel.admission.request.c"/></u></b><insta:ltext key="patient.admissionrequest.cancel.admission.request.ancel"/>
		</button>
		| <a href="${cpath}/pages/registration/admissionrequest.do?_method=getAdmissionRequestList">
			<insta:ltext key="patient.addeditadmissionrequest.admission.request.list.link"/>
		  </a>
	</div>

</form>
</body>
</html>
