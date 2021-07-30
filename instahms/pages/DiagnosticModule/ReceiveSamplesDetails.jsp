<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
 <%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<jsp:useBean id="currentDate" class="java.util.Date"/>


<html>
<head>
	<title><insta:ltext key="laboratory.receivesamples.details.samplereceivedetails"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="diagnostics/receive_sample_details.js"/>
	<insta:js-bundle prefix="laboratory.receivesample"/>
	<script type="text/javascript">
		var curDate = '<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>';
		var curTime = '<fmt:formatDate pattern="HH:mm" value="${currentDate}"/>';
	</script>
</head>

<body>
	<h1><insta:ltext key="laboratory.receivesamples.details.samplereceivedetails"/></h1>
	<c:set var="actionURL" value="${cpath}/Laboratory/ReceiveSamples.do"/>
	<c:set var="method" value="saveReceiveSamplesDetails"/>
	<fmt:formatDate var="dateVal" value="${bean.map.receipt_time}" pattern="dd-MM-yyyy"/>
	<fmt:formatDate var="timeVal" value="${bean.map.receipt_time}" pattern="HH:mm"/>
	<c:if test="${not empty incomingSampleRegistrationBean.map.patient_gender}">
		<c:set var="gender" value="${incomingSampleRegistrationBean.map.patient_gender == 'M' ? 'Male' : 'Female'}"/>
	</c:if>
	<form name="receiveSamplesDetailsForm" action="${actionURL}">
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.receivesamples.details.patientdetails"/></legend>
			<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="ui.label.mrno"/>:</td>
				<td class="forminfo">${incomingSampleRegistrationBean.map.mr_no}</td>
				<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
				<td class="forminfo">${incomingSampleRegistrationBean.map.patient_name}</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.visitid"/>:</td>
				<td class="forminfo">${incomingSampleRegistrationBean.map.incoming_visit_id}</td>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.agegender"/>:</td>
				<td class="forminfo">${incomingSampleRegistrationBean.map.patient_age}${incomingSampleRegistrationBean.map.age_unit}/${gender}</td>
			</tr>
			</table>
		</fieldset>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.receivesamples.details.sampleinformation"/></legend>
			<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.sampleno"/>:</td>
				<td class="forminfo">${receiveSampleDetailsBean.map.sample_sno}</td>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.sampletype"/>:</td>
				<td class="forminfo">${receiveSampleDetailsBean.map.sample_type}</td>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.samplecollectiondate"/>:</td>
				<td class="forminfo"><fmt:formatDate value="${receiveSampleDetailsBean.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.transfer_batch_id"/>: </td>
				<td class="forminfo">${receiveSampleDetailsBean.map.transfer_batch_id}</td>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.samplecollectioncenter"/>:</td>
				<td class="forminfo">${receiveSampleDetailsBean.map.center_name}</td>
				<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.samplesource"/>:</td>
				<td class="forminfo">${receiveSampleDetailsBean.map.source_center_name}</td>				
			</tr>
			</table>
		</fieldset>
		<fieldset class="fieldSetBorder">
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">
						<input type="checkbox" name="_sample_receive" onclick="return receiveSampleTime()" ${bean.map.sample_receive_status == 'R' ? 'checked' : ''} />
						<input type="hidden" name="sampleCollectionId" value="${bean.map.sample_collection_id}" />
						<input type="hidden" name="patient_id" value="${bean.map.patient_id}" />
					</td>
					<td><insta:ltext key="laboratory.receivesamples.details.samplereceived"/></td>
					<td colspan="5" class="formlabel">&nbsp;</td>
				</tr>
				<tr>
					<c:set var="receiptUser" value="${bean.map.receipt_user != null ? bean.map.receipt_user : receiptUser}"/>
					<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.receivinguser"/>:</td>
					<td><label>${ifn:cleanHtml(receiptUser)}</label></td>
					<input type="hidden" name="receiptUser" id="receiptUser" value="${ifn:cleanHtmlAttribute(receiptUser)}"/>
					<td colspan="4" class="formlabel">&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.receipttime"/>:</td>
					<td>
						<insta:datewidget name="receiptDate" value="${dateVal}" valid="past" btnPos="left"/>
						<input type="text" name="receiptTime" class="timefield" value="${timeVal}" maxlength="5"/>
						<td colspan="4" class="formlabel">&nbsp;</td>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.receivesamples.details.otherdetails"/>:</td>
					<td>
						<textarea rows="2" cols="20" id="receiptOtherDetails" name="receiptOtherDetails">${bean.map.receipt_other_details}</textarea>
					</td>
					<td colspan="4" class="formlabel">&nbsp;</td>
				</tr>
			</table>
		</fieldset>

		<table class="screenAction">
			<tr>
				<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />
				<c:set var="editDiasbled" value="${(sampleAssertion == true) && (bean.map.sample_status == 'A')}" />
				<c:choose>
					<c:when test="${sampleAssertion == true}">
						<c:choose>
							<c:when test="${editDiasbled}">
								<c:set var="btndisabled" value="disabled"/>
							</c:when>
							<c:otherwise>
								<c:set var="btndisabled" value=""/>
							</c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						<c:choose>
							<c:when test="${bean.map.sample_receive_status == 'R'}">
								<c:set var="btndisabled" value="disabled"/>
							</c:when>
							<c:otherwise>
								<c:set var="btndisabled" value=""/>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>
				<td><button type="button" id="saveButton" accessKey="S" ${btndisabled} onclick="return doSaveReceiveSampleDetails();"><insta:ltext key="laboratory.receivesamples.details.save"/></button></td>
				<td>&nbsp;&nbsp;</td>
				<td>
					<insta:screenlink screenId="lab_receive_sample_barcode" label="Laboratory Receive Samples Barcode" addPipe="true"
							extraParam="?_method=searchBySample" />
					<insta:screenlink screenId="lab_receive_sample_manual" label="Laboratory Receive Samples Manual" addPipe="true"
							extraParam="?_method=searchlist&sortOrder=sample_date&date_range=week&sample_receive_status=P" />
				</td>
			</tr>
		</table>
	</form>
</body>
</html>