<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page pageEncoding="UTF-8"  isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<%	String strVisitType= request.getAttribute("VisitType")!=null?(String)request.getAttribute("VisitType"):"";
%>
<html>
<head>
<title><insta:ltext key="registration.dischargesummary.details.dischargesummary.instahms"/></title>
<link rel="File-List" href="main_files/filelist.xml">
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="date_go.js"/>
<insta:link type="script" file="tiny_mce/tiny_mce.js" />
<insta:link type="script" file="editor.js" />
<insta:link type="script" file="/dischargesummary/dischargesummary.js" />
<insta:link type="script" file="multifile_alltypesupload.js" />
<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>

<script>
	/* initialize the tinyMCE editor */
	initEditor("templateContent", '${cpath}', '${printPrefs.font_name}', '${printPrefs.font_size}',
		"${cpath}/pages/GenericDocuments/PatientGeneralImageAction.do?_method=getImageListJS&patient_id=${ifn:cleanJavaScript(param.patient_id)}");
	var disDoctorId = <insta:jsString value="${empty dis ? patient.doctor_name : dis.doctor_name}"/>;
	var format = '${ifn:cleanJavaScript(param.format)}';
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
</script>

<style type="text/css">
	.plain {height:20px; vertical-align:middle;}

	#myAutoComplete {
	 width:15em; /* set width here or else widget will expand to fit its container */
     padding-bottom:2em;
	}

	#myAutoComplete1 {
	 width:15em; /* set width here or else widget will expand to fit its container */
     padding-bottom:2em;
	}
	tr.deleted {background-color: #F2DCDC; color: gray; }
</style>
<insta:js-bundle prefix="registration.patient"/>
</head>

<c:set var="bodyWidth" value="${printPrefs.page_width - printPrefs.left_margin - printPrefs.right_margin}"/>

<body onload="${ifn:cleanJavaScript(param.chooseTemplate) ? 'document.patientSearch.patient_id.focus();' : 'init();'}" class="yui-skin-sam">

<h1 style="float: left; padding-bottom: 4px"><insta:ltext key="registration.dischargesummary.details.dischargesummary"/></h1>
<c:url var="searchUrl" value="/dischargesummary/discharge.do" />
<c:if test="${param.chooseTemplate}">
<insta:patientsearch searchType="visit" searchUrl="${searchUrl}" buttonLabel="Find" searchMethod="addOrEdit"
	fieldName="patient_id" showStatusField="true"/>
</c:if>
<insta:feedback-panel/>
<!-- TODO: remove param.msg  -->
<div class="resultMessage">${ifn:cleanHtml(param.msg)}</div>
<div style="float: left">
	<insta:patientdetails visitid="${param.patient_id}" showClinicalInfo="true"/>
</div>
<c:choose>
<c:when test="${empty param.patient_id}">
</c:when>
<c:when test="${param.chooseTemplate}">
	<div class="dark bold" style="margin-top: 10px"><insta:ltext key="registration.dischargesummary.details.chooseatemplate"/></div>
	<table class="dataTable " cellspacing="0" cellpadding="0" style="margin-top: 10px">
		<tr>
	   		<th><insta:ltext key="registration.dischargesummary.details.templatename"/></th>
	   		<th><insta:ltext key="registration.dischargesummary.details.format"/></th>
		</tr>
		<c:forEach var="record" items="${templates}">
			<tr>
				<td>
					<c:url var="addUrl" value="discharge.do">
						<c:param name="_method" value="add"/>
						<c:param name="form_id" value="${record.id}"/>
						<c:param name="format" value="${record.format}"/>
						<c:param name="patient_id" value="${param.patient_id}"/>
					</c:url>
					<a href='<c:out value="${addUrl}"/>' title='<insta:ltext key="registration.dischargesummary.details.adddischargesummaryreport"/>'>${record.caption}</a>
				</td>
				<td>
					<c:if test="${record.format == 'F'}"><insta:ltext key="registration.dischargesummary.details.fixedfields.hvf"/></c:if>
					<c:if test="${record.format == 'T'}"><span style="color: brown"><insta:ltext key="registration.dischargesummary.details.richtexttemplate"/></span></c:if>
					<c:if test="${record.format == 'P'}"><span class="pdfform"><insta:ltext key="registration.dischargesummary.details.pdfformtemplate"/></span></c:if>
				</td>
			</tr>
		</c:forEach>
		<tr>
			<c:url var="addUrl" value="discharge.do">
				<c:param name="_method" value="add"/>
				<c:param name="patient_id" value="${param.patient_id}"/>
				<c:param name="format" value="U"/>
			</c:url>
			<td><a href='<c:out value="${addUrl}"/>'><insta:ltext key="registration.dischargesummary.details.uploadfile"/></a></td>
			<td><span style="color: black"><insta:ltext key="registration.dischargesummary.details.anydocument"/></span></td>
		</tr>
	</table>
</c:when>
<c:otherwise>
	<c:set var="doc_id" value="${not empty param.doc_id ? param.doc_id : docid}"/>
	<!-- show only when user wants to add or edit -->
	<form name="dischargeSummaryForm"  method="post" enctype="multipart/form-data" >
	<input type="hidden" name="hasRights" id="hasRights"/>
	<input type="hidden" name="doctorId" id="doctorId" value=""/>
	<input type="hidden" name="docid" id="docid" value="${doc_id}"/>
	<input type="hidden" name="scat" id="scat" value="${patient.mr_no}">
	<input type="hidden" name="mrNo" id="mrNo" value="${patient.mr_no}">
	<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}"/>
	<input type="hidden" name="formId" value="${ifn:cleanHtmlAttribute(param.form_id)}"/>
	<input type="hidden" name="templateId" value="${ifn:cleanHtmlAttribute(param.templateId)}"/>
	<input type="hidden" name="patAdmissionTime" value="${patient.reg_time}">
	<input type="hidden" name="patAdmissionDate" value='<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>'>
	<input type="hidden" name="visitType" value="${patient.visit_type}"/>
	<input type="hidden" name="dischargeStatus" value="${dis.discharge_flag}">

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="registration.dischargesummary.details.dischargedetails"/></legend>
		<table width="100%"  class="formtable">
			<tr>
				<td class="formlabel"><c:if test="${param.format != 'U'}"><insta:ltext key="registration.dischargesummary.details.template"/>:</c:if></td>
				<td class="forminfo">${templateCaption}</td>
				<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.dischargingdoctor"/>:</td>

				<td class="yui-skin-sam forminfo">
					<div id="myAutoComplete">

						<input type="text" id="disDoctorId" name="disDoctorId" size="27"  maxlength="50" class="field" value="${dis.doctor_name}"
								tabindex="5" onkeypress="return clearOnEnter(event, this.id);"/>
						<div id="disDocContainer"></div>
					</div>
				</td>
				<c:if test="${param.format == 'F' || param.format == 'T'}">
					<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.lastupdatedby"/>: </td>
					<td class="forminfo">${lastUpdatedBy}</td>
				</c:if>
			</tr>
			<jsp:useBean id="now" class="java.util.Date"/>
			<tr>
				<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.admissiondate"/>:</td>
				<td class="forminfo"><fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/></td>
				<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.billingdischargedate"/>:</td>
				<td class="forminfo">
					<fmt:formatDate value="${dis.discharge_date}" pattern="dd-MM-yyyy"/>
					<fmt:formatDate value="${dis.discharge_time}" pattern="HH:mm"/>
				</td>
				<td class="formlabel">${not empty dis.discharge_finalized_user?'<span class="dark bold">Finalized</span>':'Finalize:'}</td>
				<td ><input type="checkbox" name="finalized" id="finalized"
						${not empty dis.discharge_finalized_user?'checked':''}
						style="display: ${not empty dis.discharge_finalized_user?'none':'inline'};"
						tabindex="22"/>
					<input type="hidden" name="finalizedUser" value="${dis.discharge_finalized_user}"/>
					<fmt:formatDate var="finalizeddate" value="${dis.discharge_finalized_date}" pattern="dd-MM-yyyy"/>
					<input type="hidden" name="finalizedDate" value="${finalizeddate}"/>
					<input type="hidden" name="finalizedTime" value="${dis.discharge_finalized_time}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.dischargesummary.dischargedate"/>: </td>
				<td >
					<table style="white-space:nowrap" cellspacing="0" cellpadding="0">
						<tr>
							<td style="padding: 0px;"><c:set var="disDateForSummary" value="${empty dis.disch_date_for_disch_summary ? now : dis.disch_date_for_disch_summary}"/>
								<insta:datewidget name="dischDateForDischSummary" id="dischDateForDischSummary" valueDate="${disDateForSummary}" calButton="true" tabindex="10"/>
							</td>
							<td style="padding: 0px"><c:set var="disTimeForSummary" value="${empty dis.disch_time_for_disch_summary ? now : dis.disch_time_for_disch_summary}"/>
								<c:set var="disTimeStr"><fmt:formatDate value="${disTimeForSummary}" pattern="HH:mm"/></c:set>
								<input type="text" id="dischTimeForDischSummary" name="dischTimeForDischSummary" class="timefield" value="${disTimeStr}" tabindex="15"/>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<c:if test="${patient.visit_type == 'i'}">
				<tr>
					<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.dischargetype"/>:</td>
					<td class="forminfo">
						${dis.discharge_type}
					</td>
					<!-- ---Show Remarks -->
					<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.dischargeRemarks"/>:</td>
					<td class="forminfo">
						<div style="width:180px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${dis.discharge_remarks}">${dis.discharge_remarks}</div>
					</td>
					<td class="formlabel">
						<label id="refToHospLabel" style="display: ${dis.discharge_type=='Referred To'? 'block' : 'none'}"><insta:ltext key="registration.dischargesummary.details.hospital"/>:</label>
						<label id="deathReasonLabel" style="display: ${dis.discharge_type=='Death'? 'block' : 'none'}"><insta:ltext key="registration.dischargesummary.details.deathreason"/>:</label>
					</td>
					<td class="forminfo" >
						<div id="refToHospDiv" style="display: ${dis.discharge_type=='Referred To'? 'block' : 'none'}">
							${dis.referred_to}
						</div>
						<div id="deathReasonDiv" style="display: ${dis.discharge_type=='Death'? 'block' : 'none'}">
							${dis.death_reason}
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel">
						<label id="deathDateLabel" style="display: ${dis.discharge_type=='Death'? 'inline' : 'none'}"><insta:ltext key="registration.dischargesummary.details.deadonarrival"/>:</label>
					</td>
					<td class="forminfo" >
						<div style="display: ${dis.discharge_type=='Death'? 'block' : 'none'}">
							${dis.dead_on_arrival eq 'Y' ?'Yes' : 'No'}
						</div>
					</td>
					<td class="formlabel"><label id="deathDateLabel" style="display: ${dis.discharge_type=='Death'? 'inline' : 'none'}"><insta:ltext key="registration.dischargesummary.details.deathdate"/>:</label>
					<td class="forminfo">
						<div id="deathDateDiv" style="display: ${dis.discharge_type== 'Death'? 'block' : 'none'}">
							<table style="white-space:nowrap" cellspacing="0" cellpadding="0">
								<tr>
									<td class="forminfo">
									<fmt:formatDate value="${dis.death_date }" pattern="dd-MM-yyyy" var="deathDate"/>
									<fmt:formatDate value="${dis.death_time}" pattern="HH:mm" var="deathTime"/>
									${deathDate} ${deathTime}	 
									</td>
								</tr>
							</table>
						</div>
				</tr>
			</c:if>
		</table>
	</fieldset>
	<c:if test="${param.format != 'P'}">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="registration.dischargesummary.details.dischargesummaryreport"/></legend>
			<c:choose>
				<c:when test="${param.format == 'F'}">
					<table class="formTable" id="dischargesummary" width="100%">
						<c:if test="${not empty FormFieldsValuesFromDB}">
							<%int k=1; %>
							<c:forEach var="formFieldsValuesFromDBId" items="${FormFieldsValuesFromDB}">
								<tr>
									<td width="25%" class="formlabel">${formFieldsValuesFromDBId.map.caption}:</td>
									<td width="75%" >
										<input type="hidden" name="caption<%=k%>" id="caption<%=k%>"
											value='${formFieldsValuesFromDBId.map.caption}'/>
										<%-- class nonPrintableChars is used in commons.js file which will replaces the nonprintable char with space.--%>
										<textarea class="txtAreaborder" rows="${formFieldsValuesFromDBId.map.no_of_lines}" cols="85"
											name="${formFieldsValuesFromDBId.map.field_id}"
											id="field<%=k%>"
											tabindex="<%=(k+25)%>"/><c:out value="${formFieldsValuesFromDBId.map.field_value}" /></textarea>
									</td>
								</tr>
								<%k++; %>
							</c:forEach>
						</c:if>

						<c:if test="${empty FormFieldsValuesFromDB}">
							<c:if test="${not empty requestScope.FormFieldsFromDB}">
								<%int l=1; %>
								<c:forEach var="frmfield" items="${FormFieldsFromDB}">
									<tr>
										<td width="25%" class="formlabel">${frmfield.CAPTION}:</td>
										<td width="75%" >
											<input type="hidden" style="font-style: italic" name="caption<%=l%>" id="caption<%=l%>" value='${frmfield.CAPTION}'/>
											<%-- class nonPrintableChars is used in commons.js file which will replaces the nonprintable char with space.--%>
											<textarea class="txtAreaborder" rows='${frmfield.NO_OF_LINES}' cols="85"
												name="${frmfield.FIELD_ID}" id="field<%=l%>" tabindex="<%=(l+25)%>"/>${frmfield.DEFAULT_TEXT}</textarea>
										</td>
									</tr>
									<%l++; %>
								</c:forEach>
							</c:if>
						</c:if>
					</table>
				</c:when>

				<c:when test="${param.format == 'T'}">
				<table border="0" width="98%" >
					<tr>
						<td >
							<textarea id="templateContent" name="templateContent" style="width: ${bodyWidth}pt; height: 500;">
								<c:out value="${templateContent}"/>
							</textarea>
						</td>
					</tr>
				</table>
				</c:when>

				<c:when test="${param.format == 'U'}">
					<table class="formTable">
						<c:if test="${not empty uploadedFileDetails}">
							<tr>
								<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.currentlyuploadeddocument"/>:</td>
								<td >
									<a href="?_method=getImageContent&docid=${uploadedFileDetails.docid}" target="_blank">
										${uploadedFileDetails.contentfilename}
									</a>
								</td>
							</tr>
						</c:if>
						<tr>
							<td class="formlabel"><insta:ltext key="registration.dischargesummary.details.uploadanewfile"/>:</td>
							<td colspan="2" >
								<input type="file" name="theFile" tabindex="150" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
							</td>
						</tr>
					</table>
				</c:when>
			</c:choose>
		</fieldset>
	</c:if>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="registration.dischargesummary.details.followupdetails"/></legend>
		<table border="0" >
			<tr>
				<td>
					<table width="100%" id="followUpDetailsTab" cellspacing="0" cellpadding="0" class="delActionTable">
						<tr class="header">
							<td class="first" ><fmt:message key="doctor" /></td>
							<td ><fmt:message key="dateofvisit" /></td>
							<td ><fmt:message key="remarks" /></td>
							<td >&nbsp;</td>
			            </tr>
					</table>
				</td>
				<td valign="bottom" style="padding-left: 5px;">
					<input type="button" name="moredoctorvisit" class="plus"
							id="moredoctorvisit" value='+'
							onclick="return addRowToDocotrVisitTable();" tabindex="160"/>
				</td>
			</tr>
		</table>
	</fieldset>

	<table id="innerfollowupTab"></table>

	<table class="screenActions" style="float: left">
		<tr>
			<td class="label" align="center">
				<c:choose >
					<c:when test="${param.format == 'U'}">
						<button type="button" name="Discharge" class="button" tabindex="400" accessKey="S" onclick="onClickUploadSave()">
							<b><u><insta:ltext key="registration.dischargesummary.details.s"/></u></b><insta:ltext key="registration.dischargesummary.details.ave"/></button>
					</c:when>
					<c:when test="${param.format == 'P'}">
						<button type="button" name="Discharge" class="button" tabindex="410" accessKey="N" onclick="return onClickSave();">
							<b><u><insta:ltext key="registration.dischargesummary.details.n"/></u></b><insta:ltext key="registration.dischargesummary.details.ext"/></button>
					</c:when>
					<c:otherwise >
						<!-- if the format is HVF or Rich Text Template -->
						<button type="button" name="Discharge" class="button" tabindex="430"  accessKey="S" onclick="return onClickSave()">
							<b><u><insta:ltext key="registration.dischargesummary.details.s"/></u></b><insta:ltext key="registration.dischargesummary.details.ave_and_print"/></button>
					</c:otherwise>

				</c:choose>
				<c:if test="${not empty doc_id && empty dis.discharge_finalized_user}">
					<button type="submit" name="Delete" onclick="return deleteDischargeSummary();" accessKey="D">
						<b><u><insta:ltext key="registration.dischargesummary.details.d"/></u></b><insta:ltext key="registration.dischargesummary.details.elete"/></button>
				</c:if>
				<c:if test="${not empty doc_id && param.format != 'U'}">
					<c:url var="printUrl" value="dischargesummaryPrint.do">
						<c:param name="_method" value="print"/>
						<c:param name="patient_id" value="${param.patient_id}"/>
					</c:url>
					<button type="button" name="Print" onclick="printDischargeSummary('${ifn:cleanJavaScript(printUrl)}');" accessKey="P">
						<b><u><insta:ltext key="registration.dischargesummary.details.p"/></u></b><insta:ltext key="registration.dischargesummary.details.rint"/></button>
				</c:if>
			</td>
		</tr>
	</table>
	<div style="float: right; margin-top: 10px; display ${param.format != 'U' and param.format != 'P' ? 'block' : 'none'}">
		<insta:selectdb name="printerId" table="printer_definition"
				valuecol="printer_id"  displaycol="printer_definition_name"
				value="${showPrinter}"/>
	</div>
	<script>
		var dischargeDoctors =${allDoctorList};
		var disFollowUpDetails = ${disFollowUpDetails};
	</script>
</c:otherwise>
</c:choose>
</form>
</body>
<%=session.getAttribute("medicalDetailsContent")%>

</html>
