<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Print Templates - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>

	<style>
		table.dashboard td {
			white-space: normal;
			padding: 3px 6px 3px 6px;
		}
	</style>

		<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/PrintTemplates.do?method=show',
				onclick: null,
				description: "View and/or Edit Area details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init();">
	<h1>Print Templates</h1>

	<insta:feedback-panel/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<th>Template Name</th>
				<th>User Name</th>
				<th>Customized</th>
				<th>Reason for Customization</th>
			</tr>
			<c:forEach items="${printTemplates}" var="template" varStatus="st">
				<c:choose>
					<c:when test="${template.map.template_type == 'L'}">
						<c:set var="title" value="Laboratory Print Template"/>
					</c:when >
					<c:when test="${template.map.template_type == 'R'}">
						<c:set var="title" value="Radiology Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'D'}">
						<c:set var="title" value="Discharge Summary HVF Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PE'}">
						<c:set var="title" value="Pharmacy Estimate Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'S'}">
						<c:set var="title" value="Service Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'RP'}">
						<c:set var="title" value="Pharmacy Retail Credit Payment Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'RPB'}">
						<c:set var="title" value="Pharmacy Retail Credit Bill Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'ORD'}">
						<c:set var="title" value="Order Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PO'}">
						<c:set var="title" value="Purchase Order Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'GRN'}">
						<c:set var="title" value="GRN Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'GTPASS'}">
						<c:set var="title" value="Gate Pass Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'PWACT'}">
						<c:set var="title" value="Patient Ward Activites Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'INDENT'}">
						<c:set var="title" value="Indent Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'TRANSFER'}">
						<c:set var="title" value="Stock Transfer Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'PAPERPRINT'}">
						<c:set var="title" value="Samples Paper Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'APPINDENT'}">
						<c:set var="title" value="Approve Indent Print Template "/>
					</c:when>
					<c:when test="${template.map.template_type == 'TSheet'}">
						<c:set var="title" value="Treatment Sheet Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'CI'}">
						<c:set var="title" value="Clinical Information Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'CL'}">
						<c:set var="title" value="Clinical Lab Results Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'RETURNNOTE'}">
						<c:set var="title" value="Store Items Return Note" />
					</c:when>
					<c:when test="${template.map.template_type == 'APP_PRINT'}">
						<c:set var="title" value="Appointment Print Template"/>
					</c:when >
					<c:when test="${template.map.template_type == 'Triage'}">
						<c:set var="title" value="Triage Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Voucher'}">
						<c:set var="title" value="Voucher Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'REGBARCODE'}">
						<c:set var="title" value="Registration Bar Code Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'WORKSHEET'}">
						<c:set var="title" value="Sample Work Sheet Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'ITMBARCODE'}">
						<c:set var="title" value="Item Bar Code Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'Assessment'}">
						<c:set var="title" value="Initial Assessment Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'PrgNotes'}">
						<c:set var="title" value="Patient Progress Notes Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'TRMT_QUOTATION'}">
						<c:set var="title" value="Dental Treatment Quotation" />
					</c:when>
					<c:when test="${template.map.template_type == 'DENTAL_SUPPLIER_PRINT'}">
						<c:set var="title" value="Dental Supplier Print" />
					</c:when>
					<c:when test="${template.map.template_type == 'WEB_LAB'}">
						<c:set var="title" value="Web Based Laboratory Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'WEB_RAD'}">
						<c:set var="title" value="Web Based Radiology Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'API_LAB'}">
						<c:set var="title" value="API Based Laboratory Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'API_RAD'}">
						<c:set var="title" value="API Based Radiology Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'DoctorOrder'}">
						<c:set var="title" value="Physician Order Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'DoctorNotes'}">
						<c:set var="title" value="Doctors Notes Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'NurseNotes'}">
						<c:set var="title" value="Nurses Notes Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'VisitSummaryRecord'}">
						<c:set var="title" value="IP Record Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PATIENT_RESPONSE_PRINT'}">
						<c:set var="title" value="Patient Survey Response Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'ConsultationDetails'}">
						<c:set var="title" value="Consultation Details in Discharge Summary"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PATIENT_INDENT'}">
						<c:set var="title" value="Patient Indent Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PENDING_PRESC'}">
						<c:set var="title" value="Patient Pending Prescription Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Medication_Chart'}">
						<c:set var="title" value="Medication Chart Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'OTDetails'}">
						<c:set var="title" value="OT Record in Discharge Sumarry"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PatientIssuePrintTemplate'}">
						<c:set var="title" value="Patient Issue Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PatientIssueReturnPrintTemplate'}">
						<c:set var="title" value="Patient Issue Return print template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Process_indent'}">
						<c:set var="title" value="Process indent Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Vital'}">
						<c:set var="title" value="Vital Measurement Print Template" />
					</c:when>
					<c:when test="${template.map.template_type == 'UserIssuePrintTemplate'}">
						<c:set var="title" value="User/Department/Ward Issue Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'UserIssueReturnPrintTemplate'}">
						<c:set var="title" value="User/Department/Ward Return Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'WorkOrderPrintTemplate'}">
						<c:set var="title" value="Work Order Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Discharge_Medication'}">
						<c:set var="title" value="Discharge Medication Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'PriorAuth'}">
						<c:set var="title" value="Prior Auth Prescription Print"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Investigation'}">
						<c:set var="title" value="Investigation Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'Vaccination'}">
						<c:set var="title" value="Vaccination Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'IpEmrSummaryRecord'}">
						<c:set var="title" value="IP EMR Print Template"/>
					</c:when>	
					<c:when test="${template.map.template_type == 'PatientNotes'}">
						<c:set var="title" value="Patient Notes Print Template"/>
					</c:when>
					<c:when test="${template.map.template_type == 'VitalsChart'}">
						<c:set var="title" value="Vitals Chart Print Template"/>
					</c:when>									
				</c:choose>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{template_type : '${template.map.template_type}', title : '${title}', customized : '${not empty template.map.print_template_content?'true':'false'}'},'');" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1}</td>
					<td>
						${title}
					</td>
					<td>${template.map.user_name}</td>
					<td>${not empty template.map.print_template_content?'Yes':'No'}</td>
					<td><c:out value="${template.map.reason}"/></td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>
