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
	<title><insta:ltext key="laboratory.transfersamples.details.sampletransferdetails"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="diagnostics/transfer_sample_details.js"/>
	<insta:js-bundle prefix="laboratory.transfersample"/>
	<script type="text/javascript">
		var curDate = '<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>';
		var curTime = '<fmt:formatDate pattern="HH:mm" value="${currentDate}"/>';
		var sampleDate = ${sampleDate};
	</script>
</head>

<body>
	<c:set var="select">
		<insta:ltext key="selectdb.dummy.value"/>
	</c:set>			
	<h1><insta:ltext key="laboratory.transfersamples.details.sampletransferdetails"/></h1>
	<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />
	<c:set var="actionURL" value="${cpath}/Laboratory/TransferSamples.do"/>
	<c:set var="method" value="saveTransferSamplesDetails"/>
	<fmt:formatDate var="dateVal" value="${bean.map.transfer_time}" pattern="dd-MM-yyyy"/>
	<fmt:formatDate var="timeVal" value="${bean.map.transfer_time}" pattern="HH:mm"/>
	<c:if test="${not empty incomingSampleRegistrationBean.map.patient_gender}">
		<c:set var="gender" value="${incomingSampleRegistrationBean.map.patient_gender == 'M' ? 'Male' : 'Female'}"/>
	</c:if>	
	<c:choose>
		<c:when test="${not empty patient}">
			<insta:patientdetails visitid="${transferSampleDetailsBean.map.patient_id}"/>
		</c:when>
		<c:otherwise>
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
		</c:otherwise>
	</c:choose>
	<form name="transferSamplesDetailsForm" action="${actionURL}">
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
	<c:set var="isOutSourceSelected" value="false" />
	<c:if test="${not empty transferSampleDetailsBean.map.outsource_dest_id}">
		<c:set var="isOutSourceSelected" value="true" />
	</c:if>		
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.transfersamples.details.sampleinformation"/></legend>
			<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.sampleno"/>:</td>
				<td class="forminfo">${transferSampleDetailsBean.map.sample_sno}</td>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.sampletype"/>:</td>
				<td class="forminfo">${transferSampleDetailsBean.map.sample_type}</td>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.samplecollectiondate"/>:</td>
				<td class="forminfo"><fmt:formatDate value="${transferSampleDetailsBean.map.sample_date}" pattern="dd-MM-yyyy HH:mm"/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.samplequantity"/>:</td>
				<td class="forminfo">${transferSampleDetailsBean.map.sample_qty}</td>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.outsource"/>:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${transferSampleDetailsBean.map.sample_transfer_status == 'T' || isOutSourceSelected || empty outSourceList}">							
							<insta:truncLabel value="${transferSampleDetailsBean.map.outsource_name}" length="15"/>
							<input type="hidden" name="outsource_id" id="outsource_id" value="${transferSampleDetailsBean.map.outsource_dest_id}" />																			
						</c:when>
						<c:otherwise>
							<select name="outsource_id" id="outsource_id" class="dropdown noToolbar" style="width: 100px;">
					   			<option value="">${select}</option>
								<c:forEach items="${outSourceList}" var="outSource">
									<option value="${outSource.OUTSOURCE_DEST_ID}">
										${outSource.OUTSOURCE_NAME}
									</option>
								</c:forEach>
							</select>							
						</c:otherwise>
					</c:choose>				
					<input type="hidden" name="outsource_dest_id" id="outsource_dest_id" value=""/>	
					<input type="hidden" name="sg_test_id" id="sg_test_id" value="${transferSampleDetailsBean.map.test_id}"/>
					<input type="hidden" name="sg_prescribed_id" id="sg_prescribed_id" value="${transferSampleDetailsBean.map.prescribed_id}"/>
					<input type="hidden" name="sg_sample_no" id="sg_sample_no" value="${transferSampleDetailsBean.map.sg_sample_no}"/>
					<input type="hidden" name="sg_sample_type_id" id="sg_sample_type_id" value="${transferSampleDetailsBean.map.sg_sample_type_id}"/>																	
				</td>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.samplesource"/>:</td>
				<td class="forminfo">${transferSampleDetailsBean.map.source_center_name}</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.samplecollectioncenter"/>:</td>
				<td class="forminfo">${transferSampleDetailsBean.map.collection_center}</td>
			</tr>
			</table>
		</fieldset>
		<fieldset class="fieldSetBorder">
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">
						<input type="checkbox" name="_sample_transferred" onclick="return transferSampleTime()" ${bean.map.sample_transfer_status == 'T' ? 'checked' : ''} />
						<input type="hidden" name="sampleCollectionId" value="${bean.map.sample_collection_id}" />
						<input type="hidden" name="outsourceDestPrescribedId" value="${internalLabSamplesBean.map.prescribed_id}" />
						<input type="hidden" name="patient_id" value="${bean.map.patient_id}" />
					</td>
					<td><insta:ltext key="laboratory.transfersamples.details.sampletransferred"/></td>
					<td colspan="5" class="formlabel">&nbsp;</td>
				</tr>
				<tr style="display: ${not empty bean.map.transfer_batch_id ? 'table-row' : 'none'}">
					<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.transfer_batch_id"/>: </td>
					<td class="forminfo">${bean.map.transfer_batch_id}</td>
				</tr>
				<tr>
					<c:set var="transferUser" value="${bean.map.transfer_user != null ? bean.map.transfer_user : transferUser}"/>
					<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.transferuser"/>:</td>
					<td><label>${ifn:cleanHtml(transferUser)}</label></td>
					<input type="hidden" name="transferUser" id="transferUser" value="${ifn:cleanHtmlAttribute(transferUser)}"/>
					<td colspan="4" class="formlabel">&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.transfertime"/>:</td>
					<td>
						<insta:datewidget name="transferDate" value="${dateVal}" valid="past" btnPos="left"/>
						<input type="text" name="transferTime" class="timefield" value="${timeVal}" maxlength="5"/>
						<td colspan="4" class="formlabel">&nbsp;</td>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.transfersamples.details.otherdetails"/>:</td>
					<td>
						<textarea rows="2" cols="20" id="transferOtherDetails" name="transferOtherDetails">${bean.map.transfer_other_details}</textarea>
					</td>
					<td colspan="4" class="formlabel">&nbsp;</td>
				</tr>
			</table>
		</fieldset>

		<table class="screenAction">
			<tr>
				<c:if test="${internalLabSamplesBean.map.sample_receive_status == 'R' 
				|| (sampleAssertion eq true && transferSampleDetailsBean.map.sample_status ne 'A')
				|| ((not empty transferSampleDetailsBean.map.sample_split_status) && transferSampleDetailsBean.map.sample_split_status eq 'P')}">				
					<c:set var="btndisabled" value="disabled"/>
				</c:if>
				<td><button type="button" id="saveButton" accessKey="S" ${btndisabled} onclick="return doSaveTransferDetails();"><insta:ltext key="laboratory.transfersamples.details.save"/></button></td>
				<td>&nbsp;&nbsp;</td>
				<td>
					<insta:screenlink screenId="lab_transfer_sample_barcode" label="Laboratory Transfer Samples Barcode" addPipe="true"
						extraParam="?_method=searchBySample" />
					<insta:screenlink screenId="lab_transfer_sample_manual" label="Laboratory Transfer Samples Manual" addPipe="true"
						extraParam="?_method=list&sample_transfer_status=P&sortOrder=sample_date&date_range=week" />
				</td>
			</tr>
		</table>
	</form>
</body>
</html>