<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="registration.patient.success.title"/></title>
<insta:link type="js" file="genericdocuments/patientgeneraldocuments.js"/>
<insta:link type="js" file="registration/registration.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<style type="text/css">
	.forminfo {font-weight: bold}
</style>
<script>
	var patientBilPopUp = '${ifn:cleanJavaScript(param.patientBillPopUp)}';
	var billNo = '${ifn:cleanJavaScript(param.billNo)}';

</script>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="common.message"/>
</head>

<body onload="ajaxForPrintUrls();openPatientBill();">
<h1><insta:ltext key="registration.patient.success.header"/></h1>
<input type="hidden" id="billno" value="${ifn:cleanHtmlAttribute(billNo)}">
<div align="center"><insta:feedback-panel/></div>

<jsp:useBean id="currentDate" class="java.util.Date"/>
<table class="formtable" width="100%">
	<tr>
		<td valign="top" >
			<fieldset class="fieldSetBorder" id="regInfo">
				<legend class="fieldSetLabel"><insta:ltext key="registration.patient.success.fieldset.header.details"/></legend>
				<table>
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.success.hospital.id"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.mr_no}</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.success.patient.id"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.patient_id}</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.full_name}</td>
					</tr>

					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.success.consulting.department"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.dept_name}</td>
					</tr>
					<c:if test="${regPref.hospUsesUnits != null && regPref.hospUsesUnits == 'Y' &&
													regPref.deptUnitSetting != null && regPref.deptUnitSetting != ''}">
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.success.unit"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.unit_name}</td>
					</tr>
					</c:if>
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.success.consulting.doctor"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.doctor_name}</td>
					</tr>
					<c:if test="${regPref.caseFileSetting != null && regPref.caseFileSetting != '' && regPref.caseFileSetting == 'Y'}">
					<tr>
						<td class="formlabel"><insta:ltext key="registration.patient.success.case.file.no"/>:</td>
						<td class="forminfo">${patientvisitdetails.map.casefile_no}</td>
					</tr>
					</c:if>
					<c:if test="${regPref.oldRegNumField != '' && regPref.oldRegNumField != null}">
					<tr>
						<td class="formlabel">${ifn:cleanHtml(regPref.oldRegNumField)}:</td>
						<td class="forminfo">${patientvisitdetails.map.oldmrno}</td>
					</tr>
					</c:if>
					<c:if test="${patientvisitdetails.map.visit_type eq 'o' && tokenRights eq 'Y'}">
						<tr>
							<td class="formlabel"><insta:ltext key="registration.patient.success.token.no"/>:</td>
							<td class="forminfo">${ifn:cleanHtml(tokenNo)}</td>
						</tr>
					</c:if>
				</table>
			</fieldset>
		</td>
		<td valign="top">
			<c:if test="${not empty regTemplates}">
					<table align="center" class="datatable" width="100%" cellpadding="0" cellspacing="0">
						<tr>
							<th><insta:ltext key="registration.patient.success.registration.forms"/></th>
							<th></th>
						</tr>

						<c:forEach var="template" items="${regTemplates}">
							<tr>
								<td>${template.map.template_name}</td>
								<td>
									<a href="${cpath}/pages/RegistrationDocuments.do?
										_method=add&mr_no=${patientvisitdetails.map.mr_no}&template_id=${template.map.template_id}
										&format=${template.map.format}&patient_id=${patientvisitdetails.map.patient_id}
										&mlc_doc_name=${template.map.template_name}
										&documentType=reg&print=${ifn:cleanURL(print)}&format=${format}&raeMode${rawMode}&printerId=${ifn:cleanURL(printerId)}" target="_blank">
										<insta:ltext key="registration.patient.success.add.form"/>
									</a>
								</td>
							</tr>
						</c:forEach>
					</table>
					<br/>
					<table align="center" class="datatable" width="100%" cellpadding="0" cellspacing="0">
						<tr>
							<th><insta:ltext key="registration.patient.success.autogenerated.forms"/></th>
							<th></th>
						</tr>
						<c:forEach var="doc" items="${autoRegDocs}">
							<tr>
								<td>${doc.map.doc_name}</td>
								<td><a target="_blank" href= "${cpath}/pages/RegistrationDocuments/RegistrationDocumentsPrint.do?
								_method=print&allFields=N&doc_id=${doc.map.doc_id}&template_id=${doc.map.template_id}
								&format=${doc.map.doc_format}&patient_id=${patientvisitdetails.map.patient_id}
								&printerId=4&access_rights=U&username=admin"><insta:ltext key="registration.patient.success.print.form"/></a></td>
							</tr>
						</c:forEach>
					</table>
				<br/>
			</c:if>
		</td>
	</tr>
</table>
<c:set var="proceedToBillingLink">
	<insta:ltext key="registration.patient.success.proceed.to.billing.link"/>
</c:set>
<c:set var="orderLink">
	<insta:ltext key="registration.patient.success.order.link"/>
</c:set>
<c:set var="phramacysalesLink">
	<insta:ltext key="registration.patient.success.pharmacy.sales.link"/>
</c:set>
<c:set var="backToRegistrationLink">
	<insta:ltext key="registration.patient.success.back.to.registration.link"/>
</c:set>
<table class="screenActions">
	<tr>
		<c:if test="${urlRightsMap.credit_bill_collection != 'N' && screenId != 'out_pat_reg'}">
			<td><insta:screenlink
				screenId="credit_bill_collection"
				extraParam="?_method=getCreditBillingCollectScreen&billNo=${billNo}&showPayments=1"
				label="${proceedToBillingLink}" />&nbsp;|&nbsp;</td>
		</c:if>
		<c:if test="${urlRightsMap['prescribe'] == 'A'}">
			<td><insta:screenlink screenId="prescribe"
				extraParam="?method=getPrescriptionScreen&mrno=${patientvisitdetails.map.mr_no}&patientid=${patientvisitdetails.map.patient_id}
				&dept=${patientvisitdetails.map.dept_id}&bedno=${patientvisitdetails.map.bill_bed_type}
				&orgid=${patientvisitdetails.map.org_id}&doctor=${patientvisitdetails.map.doctor}
				&patientWard=${patientvisitdetails.map.alloc_ward_name}&doctorname=${patientvisitdetails.map.doctor_name}
				&deptname=${patientvisitdetails.map.dept_name}&age=${patientvisitdetails.map.age}
				&gender=${patientvisitdetails.map.patient_gender}&bed=${patientvisitdetails.map.bill_bed_type}"
				label="${orderLink}" />&nbsp;|&nbsp;</td>
		</c:if>
		<td><a
			href="${cpath}/pages/registration/GenerateRegistrationCard.do?patid=${patientvisitdetails.map.patient_id}
			&orgId=${patientvisitdetails.map.org_id}"
			target="_blank"><insta:ltext key="registration.patient.success.print.registration.card.link"/></a>&nbsp;|&nbsp;</td>
		<td align="center"><a
			href="${cpath}/pages/registration/GenerateRegistrationBarCode.do?method=execute&mrno=${patientvisitdetails.map.mr_no}&barcodeType=Reg&visitId=${patientvisitdetails.map.patient_id}"
			target="_blank"><insta:ltext key="registration.patient.success.print.registration.bar.code.link"/></a>&nbsp;|&nbsp;</td>
		<c:if test="${genPrefs.sampleFlowRequired == 'Y'}">
		<c:choose>
			<c:when test="${urlRightsMap.lab_pending_samples_search == 'A'}">
				<c:set var="collectSampleUrl" value="PendingSamplesSearch"/>
			</c:when>
			<c:otherwise>
				<c:set var="collectSampleUrl" value="PendingSamples"/>
			</c:otherwise>
		</c:choose>
			<td><a
			href="${cpath}/Laboratory/${collectSampleUrl }.do?_method=getSampleCollectionScreen&visitid=
				${patientvisitdetails.map.patient_id}&title=Generate Sample Barcode&default_sample_status=P"
			target="_blank"><insta:ltext key="registration.patient.success.genarate.sample.barcode.label"/></a>&nbsp;|&nbsp;</td>
		</c:if>
		<c:if test="${screenId == 'out_pat_reg' && urlRightsMap.new_op_order != 'N'}">
			<td>
				<insta:screenlink screenId="new_op_order"
				extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(patientvisitdetails.map.mr_no)}/order/visit/${ifn:encodeUriComponent(patientvisitdetails.map.patient_id)}?retain_route_params=true"
				label="${orderLink}" />
				&nbsp;|&nbsp;
			</td>
		</c:if>
		<c:if test="${screenId == 'out_pat_reg' && urlRightsMap.pharma_sales != 'N'}">
			<td>
				<insta:screenlink screenId="pharma_sales" extraParam="?method=getSalesScreen&visit_id=${patientvisitdetails.map.patient_id}" label="Pharmacy Sales" />&nbsp;|&nbsp;
			</td>
		</c:if>
			<td>
				<a style="cursor: pointer;" onclick="openNewUploadDocumentPopUp(
				'${patientvisitdetails.map.mr_no}',
				'${patientvisitdetails.map.patient_id}',
				'${screenId}', '${genericPrefs.upload_limit_in_mb}')" />
					Upload Documents
				</a>&nbsp;|&nbsp;
			</td>
     <c:if test="${not empty patientvisitdetails.map.mr_no && preferences.modulesActivatedMap['mod_hie'] eq 'Y'}">
			  <td>
			  <a style="cursor: pointer;" onclick="return openConsentUploadDocumentPopUp('${patientvisitdetails.map.mr_no}','${genericPrefs.upload_limit_in_mb}')"><insta:ltext key="js.label.hie.consent"/></a>&nbsp;|&nbsp;</td>
      </c:if>

		<c:choose>
			<c:when test="${not empty referer}">
				<td ><a href="<c:out value='${referer}'/>"> <insta:ltext key="registration.patient.success.scheduler.link"/></a></td>
			</c:when>
			<c:otherwise>
				<td>
					<c:if test="${screenId == 'ip_registration'}">
						<insta:screenlink screenId="ip_registration" extraParam="?_method=getdetails" label="${backToRegistrationLink}" />
					</c:if>
					<c:if test="${screenId == 'out_pat_reg'}">
						<insta:screenlink screenId="out_pat_reg" extraParam="?_method=getdetails" label="${backToRegistrationLink}" />
					</c:if>
				</td>
			</c:otherwise>
		</c:choose>
	</tr>
</table>

</body>
</html>

