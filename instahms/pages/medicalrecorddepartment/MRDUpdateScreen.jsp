<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
<title>Edit Codes - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="date_go.js"/>

<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value='<%= GenericPreferencesDAO.getAllPrefs().getMap() %>'/>

<jsp:useBean id="consulBillStatus" class="java.util.HashMap"/>
<c:set target="${consulBillStatus}" property="A" value="Open"/>
<c:set target="${consulBillStatus}" property="F" value="Finalized"/>
<c:set target="${consulBillStatus}" property="C" value="Closed"/>
<c:set target="${consulBillStatus}" property="X" value="Cancelled"/>

<jsp:useBean id="consulStatus" class="java.util.HashMap"/>
<c:set target="${consulStatus}" property="A" value="Not Completed"/>
<c:set target="${consulStatus}" property="P" value="Not Completed"/>
<c:set target="${consulStatus}" property="C" value="Completed"/>
<c:set target="${consulStatus}" property="U" value="Not Completed"/>
<c:set target="${consulStatus}" property="X" value="Cancelled"/>

<jsp:useBean id="presentOnAdmission" class="java.util.HashMap"/>
<c:set target="${presentOnAdmission}" property="Y" value="Yes"/>
<c:set target="${presentOnAdmission}" property="N" value="No"/>
<c:set target="${presentOnAdmission}" property="U" value="Unknown"/>
<c:set target="${presentOnAdmission}" property="W" value="Clinically Undetermined"/>
<c:set target="${presentOnAdmission}" property="1" value="Unreported/Not Used"/>
<c:set target="${presentOnAdmission}" property="OP" value="Outpatient Claim"/>

<style type="text/css">
		.scrolForContainer .yui-ac-content{
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
		    _height:18em; max-width:30em; width:30em;/* ie6 */
		}
		.diagScrolForContainer .yui-ac-content{
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
		    _height:18em; /* ie6 */
		}
		.diagnosis_overlay { border:1px dotted black; background-color: FFCC99; }

	  .toolTip {
	    display: none;
	    position : absolute;
	    width: 300px;
	    border-color: #808080;
    	border-style: solid;
    	border-width: 1px ;
	    -moz-box-shadow: 5px 5px 5px #ccc;
	  	-webkit-box-shadow: 5px 5px 5px #ccc;
	  	box-shadow: 5px 5px 5px #ccc;
	  }

div#container{width: 500px; margin:0 auto}
p{margin: 0 0 1.7em; }


div.tt{
    position:relative;
    z-index:24;
	font-weight:bold;
    text-decoration:none;
}

div.tt span{ display: none; }

a.tt{
    position:relative;
    z-index:24;
	font-weight:bold;
    text-decoration:none;
}
a.tt span{ display: none; }

	/*background:; ie hack, something must be changed in a for ie to execute it*/
	div.tt:hover{ z-index:25;  background:;}
	div.tt:hover span.tooltip{
	    display:block;
	    position:absolute;
	    top:0px; left:0;
		padding: 15px 0 0 0;
		width:200px;
	    text-align: center;
		filter: alpha(opacity:90);
		KHTMLOpacity: 0.90;
		MozOpacity: 0.90;
		opacity: 0.90;
	}

	div.tt:hover span.top{
		display: block;
		padding: 30px 8px 0;
	    background: url(${cpath}/icons/bubble.gif) no-repeat top;
	}
	div.tt:hover span.middle{ /* different middle bg for stretch */
		display: block;
		padding: 0 8px;
		background: url(${cpath}/icons/bubble_filler.gif) repeat bottom;
	}
	div.tt:hover span.bottom{
		display: block;
		padding:3px 8px 10px;
	    background: url(${cpath}/icons//bubble.gif) no-repeat bottom;
	}
.ceedcircle {
     -webkit-border-radius:8px;
     -moz-border-radius:8px;
     border-radius:8px;
     border:1px solid #ccc;
     width:8px;
     height:8px;
     display: inline-block;
}

.yellow {
    background-color: yellow;
}

.black {
    background-color: #000000;
}

.grey {
    background-color: #A9A9A9;
}

.red {
    background-color: red;
}

.green {
    background-color: green;
}

.orange {
    background-color: #FFA500;
}

</style>

<script>
	var cpath = '${cpath}';
	var diagCodeType = '${diagCodeType}';
	var opType = '${opType}';
	var consulItemCodes = ${allowdConsulItemCodes};
	var orgID = '${orgID}' ;
	var encntrStartAndEndReqd = '${regPref.encntr_start_and_end_reqd}';
	var encntrTypeReqd = '${regPref.encntr_type_reqd}';
	var patientType = '${visitType}';
	var observationCodeTypesList = ${observationCodeTypeList};
	var isModMrdIcdEnabled = '${preferences.modulesActivatedMap.mod_mrd_icd}';
	var modcoderReview = '${preferences.modulesActivatedMap.mod_coder_claim_review}'; 
	var testResultsData = ${testResultsData};
	var mrdSupportedCodeTypes = ${mrdSupportedCodeTypes};
	var visitTpaBillsCnt = ${visitTpaBillsCount};
	var defaultFollowupEandmCode = '${regPref.default_followup_eandm_code}';
	var supportedConsCodeTypes = '${consultationSupportedCodeTypes}';
	var tooth_numbering_system = '${genericPrefs.tooth_numbering_system}';
	var eclaimXMLSchema = '${healthAuthorityPrefs.health_authority}';
	var isBaby = ${isBaby};
	var weightValue = '<%=request.getAttribute("weightValue") %>';
	var weightUom = '<%=request.getAttribute("weightUom") %>';
	var drgChargeId = '<%=request.getAttribute("drgChargeId") %>';
	var isChildPatient = ${isChildPatient};
	var isCustomFieldsExist = ${isCustomFieldsExist};
    var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
    var ceedstatus = ${ceedstatus};
    var ceedresponsemap = ${ceedResponseMapJson};
    var has_right_to_view_ceed_comments = ${actionRightsMap.view_ceed_response_comments == 'A'};
    var reviewsCount = ${reviewsCount};
    var maxOrderDateTime = '${patientEncCodes.max_order_date}';
</script>
<insta:link type="js" file="medicalrecorddepartment/mrdupdatescreen.js"/>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
<insta:js-bundle prefix="coder.claim.reviews.validation"/>
</head>
<body onload="init();">	
	<div id="con"></div>
	<h1 style="float: left">Edit Codes</h1>
	<c:url var="searchUrl" value="/pages/medicalrecorddepartment/MRDUpdate.do" />
	<insta:patientsearch searchType="visit" searchUrl="${searchUrl}" buttonLabel="Find" searchMethod="getMRDUpdateScreen"
	fieldName="patient_id"/>
	<c:set var="isModMrdIcdEnabled" value="${preferences.modulesActivatedMap.mod_mrd_icd}"/>
	<insta:feedback-panel/>

<div id="primaryInsurancePhotoDialog" style="display:none;visibility:hidden;" ondblclick="handlePrimaryInsurancePhotoDialogCancel();">
	<div class="bd" id="bd2" style="padding-top: 0px;">
		<table  style="text-align:top;vetical-align:top;" width="100%">
			<tr>
				<td>
					<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
						<legend class="fieldSetLabel">Insurance Card</legend>
								<c:choose>
									<c:when test="${isPrimaryInsuranceCardAvailable eq true }">
										<embed id="pinsuranceImage" height="450px" width="500px" style="overflow:auto"
											src="${cpath}/Registration/GeneralRegistrationPlanCard.do?_method=viewInsuranceCardImage&visitId=${patient_id}"/>
									</c:when>
									<c:otherwise>
										No Insurance Card Available
									</c:otherwise>
								</c:choose>
					 </fieldset>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="Close" style="cursor:pointer;" onclick="handlePrimaryInsurancePhotoDialogCancel();"/>
				</td>
			</tr>
		</table>
	</div>
</div>
<form name="MRDUpdateForm"  method="post" action="./MRDUpdate.do">
<input type="hidden" name="_method" id="method" value="saveMrd">

	<c:choose>
		<c:when test="${empty param.patient_id}">
			<insta:patientdetails visitid="${ifn:cleanHtml(param.patient_id)}" showClinicalInfo="true"/>
		</c:when>
		<c:otherwise>
			<insta:patientdetails visitid="${ifn:cleanHtml(patient_id)}" showClinicalInfo="true"/>
			<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Other Details</legend>
				<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<td class="formlabel">Admission&nbsp;Date:</td>
						<td class="forminfo">
							<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>&nbsp;
							<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>
						</td>

						<td class="formlabel">Discharge&nbsp;Date:</td>
						<td class="forminfo">
							<fmt:formatDate value="${patient.discharge_date}" pattern="dd-MM-yyyy"/>&nbsp;
							 <fmt:formatDate value="${patient.discharge_time}" pattern="HH:mm"/>
						</td>

						<c:if test="${regPref.oldRegNumField != '' && regPref.oldRegNumField != null}">
							<td class="formlabel">${regPref.oldRegNumField}:</td>
							<td class="forminfo">${patient.oldmrno}</td>
						</c:if>
					</tr>
					<tr>
						<td class="formlabel">Prior Auth No:</td>
						<td class="forminfo">
							<input type="text" name="primary_prior_auth_id" id="primary_prior_auth_id" value="${primary_prior_auth_id}"/></td>
						<c:if test="${regPref.caseFileSetting != null && regPref.caseFileSetting != '' && regPref.caseFileSetting == 'Y'}">
							<td class="formlabel">Case File No:</td>
							<td class="forminfo">${patient.casefile_no}</td>
						</c:if>
						<c:if test="${not empty regPref.government_identifier_label}">
							<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_label)}:</td>
							<td class="forminfo">${patient.government_identifier}</td>
						</c:if>
					</tr>
						<tr>
						<c:choose>
						<c:when test="${pri_eligibility_authorization_status != null  && pri_eligibility_authorization_status != ''}">
							<td class="formlabel">Eligibility Authorization Status:</td>
							<td class="forminfo">
								<c:if test="${pri_eligibility_authorization_status == 1}">Taken</c:if>
								<c:if test="${pri_eligibility_authorization_status == 2}">Not Taken</c:if>
								<c:if test="${pri_eligibility_authorization_status == 3}">Not Applicable</c:if>
							</td>
						</c:when>
						<c:otherwise>
								<td></td>
								<td></td>
						</c:otherwise>
						</c:choose>
						<c:choose>
						<c:when test="${(pri_eligibility_reference_number != null  && pri_eligibility_reference_number != '')
									|| (pri_eligibility_authorization_remarks != null  && pri_eligibility_authorization_remarks != '')}">
							<c:choose>
							<c:when test="${pri_eligibility_authorization_status == 1}">
							<td class="formlabel">Eligibility Reference Number:</td>
							<td class="forminfo">${pri_eligibility_reference_number}</td>
							</c:when>
							<c:otherwise>
							<td class="formlabel">Authorization Remarks:</td>
							<td class="forminfo">${pri_eligibility_authorization_remarks}</td>
							</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<td></td>
							<td></td>
						</c:otherwise>
						</c:choose>
							<c:choose>
								<c:when test="${isPrimaryInsuranceCardAvailable eq true}">
									<td class="formlabel">View Insurance Card:</td>
									<td class="forminfo">
										<div style="display: inline;">
										<button id="_p_plan_card" title="Uploaded Insurance Card..."
											style="cursor: pointer;"
											onclick="javascript:showPrimaryInsurancePhotoDialog();"
											type="button">..</button>
										</div>
									</td>
								</c:when>
								<c:otherwise>
									<td></td>
									<td></td>
								</c:otherwise>
							</c:choose>
						</tr>

					</table>
			</fieldset>

			<input type="hidden" name="mrNo" id="mrNo"value="${patient.mr_no}">
			<input type="hidden" name="patId" id="patId" value="${ifn:cleanHtml(patient.patient_id)}">
			<input type="hidden" name="disDate" value="${patient.discharge_date}">
			<input type="hidden" name="use_drg" value="${patient.use_drg}">
			<input type="hidden" name="ceedcheck" id="ceedcheck" value="N"/>
			<input type="hidden" name="ceedchecktype" id="ceedchecktype" value="2"/>
		<table style="border-collapse: collapse;width:100%;">
			<tr>
				<td>
					<table class="formtable">
						<tr>
							<td class="formlabel">Remarks:</td>
							<td class="forminfo">
								<input type="text" name="codification_remarks" id="codification_remarks" value="${codification_remarks}"
										maxlength="100" />
							</td>
							<td class="formlabel">Blood Group:</td>
							<td>
								<insta:selectdb displaycol="custom_value" name="custom_list4_value" tabindex="1" value="${patient.custom_list4_value}"
									table="custom_list4_master" valuecol="custom_value" style="width: 8em" dummyvalue="..Select.."/>
							</td>


							<td class="formlabel">Discharge Type:</td>
									<c:if test="${patient.visit_type ne 'i'}">
										<td class="forminfo">${dis.discharge_type}</td>
									</c:if>
								<c:if test="${patient.visit_type == 'i'}">
									<td class="forminfo" >
										<insta:selectoptions name="discharge_type" value="${dis.discharge_type}"
											opvalues="Normal,Absconded,Death,DAMA,Referred To"
											optexts="Normal, Absconded, Death, DAMA, Reffered To" onchange="displayReferredTo(this);"/>
									</td>

									<td class="formlabel">
									<label id="refToHospLabel" style="display: ${dis.discharge_type=='Referred To'? 'block' : 'none'}">Hospital:<label>
									<label id="deathDateLabel" style="display: ${dis.discharge_type=='Death'? 'inline' : 'none'}">Death Date:</label>
									</td>
									<td>
										<div id="refToHospDiv" style="display: ${dis.discharge_type=='Referred To'? 'block' : 'none'}">
											<input type="text" name="referred_to" id="referred_to" value="${dis.referred_to}"
												style="width: 15em;"/>
										</div>
										<div id="deathDateDiv" style="display: ${dis.discharge_type=='Death'? 'block' : 'none'}">
											<table style="white-space:nowrap" cellspacing="0" cellpadding="0">
												<tr>
													<td style="padding: 0px"><c:set var="deathDate" value="${empty dis.death_date ? now : dis.death_date}"/>
														<insta:datewidget name="death_date" id="death_date" valueDate="${deathDate}" calButton="true" tabindex="10"/>
													</td>
													<td style="padding: 0px"><c:set var="deathTime" value="${empty dis.death_time ? now : dis.death_time}"/>
										        		<c:set var="deathTimeStr"><fmt:formatDate value="${deathTime}" pattern="HH:mm"/></c:set>
														<input type="text" class="timefield" id="death_time" name="death_time"  value="${deathTimeStr}" tabindex="15"/>
													</td>
												</tr>
											</table>
										</div>
									</td>
									<td class="formlabel"><label id="deathReasonLabel" style="display: ${dis.discharge_type=='Death'? 'block' : 'none'}">Death Reason: </td>
									<td>
										<div id="deathReasonDiv" style="display: ${dis.discharge_type=='Death'? 'block' : 'none'}">
											<insta:selectdb name="death_reason_id" id="death_reason_id" table="death_reason_master" displaycol="reason" valuecol="reason_id"
													value="${dis.death_reason_id}"  dummyvalue="..Select.." orderby="reason" filtered="true"/>
										</div>
									</td>
								</c:if>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td width="100%">&nbsp;</td>
			</tr>
			
			<tr>
				<td width="100%">
					<c:set var="consDisplay"> ${empty patientDrConsultn ? "none" : "inline"} </c:set>
					<fieldset class="fieldSetBorder" style="display:'${consDisplay}'">
						<legend class="fieldSetLabel">Doctor Consultation Codes</legend>
						<table class="detailList" id="consulcodes" >
							<tr>
							<th>Code</th>
							<th>Code Description</th>
							<th>Consultation Type</th>
							<th>Doctor Name-[Dept. Name]</th>
							<th>Dept. Est. Status</th>
							<th>Cons. Status</th>
							<th>Bill /Status</th>
							<th></th>
							<th></th>
							</tr>
							<c:forEach items="${patientDrConsultn}" varStatus="status" var="patconsultation">
								<tr id="consulRow${status.index}">
									<td>
										<label id ="consulCode${status.index}">
											${patconsultation.map.item_code}(${patconsultation.map.code_type eq null || patconsultation.map.code_type eq ''? 'E&M' : patconsultation.map.code_type})
										</label>
										<c:if test="${patconsultation.map.code_type eq null || patconsultation.map.code_type eq '' || patconsultation.map.code_type eq 'E&M'}">
											<c:if test="${preferences.modulesActivatedMap.mod_emcalc == 'Y'}">
												<c:url var="eandmCalcUrl" value="/eandmcalculator.do">
													<c:param name="consultationId" value="${patconsultation.map.consultation_id}"/>
													<c:param name="_method" value="getScreen"/>
												</c:url>
												<br/><a href="${eandmCalcUrl}" >E&M calculator</a>
											</c:if>
										</c:if>
									</td>
									<td>
										<insta:truncLabel id="consulDesc${status.index}" value="${patconsultation.map.code_desc}" length="30"/>
									</td>
									<td> <label id ="consulType${status.index}"> ${patconsultation.map.consultation_type} </label></td>
									<td> <label id ="consulDoc${status.index}"> <insta:truncLabel value="${patconsultation.map.doctor_name}-[${patconsultation.map.dept_name}]" length="25"/> </label></td>
									<td> <label id ="consulDep${status.index}"> ${patconsultation.map.established_type} </label></td>

									<c:choose>
										<c:when test="${patconsultation.map.consultation_status ne 'C'}">
											<td>
												${consulStatus[patconsultation.map.consultation_status]}
												<insta:screenlink target="_blank" screenId="op_prescribe"
													extraParam="?_method=list&consultation_id=${patconsultation.map.consultation_id}"
													label="Edit" addPipe="false"/>
												<c:if test="${patconsultation.map.op_type eq 'F'}"> (Followup) </c:if>
											</td>
										</c:when>
										<c:otherwise>
											<td>
												<label id="consulStatus${status.index}">
												${consulStatus[patconsultation.map.consultation_status]} <c:if test="${patconsultation.map.op_type eq 'F'}"> (Followup) </c:if>
												</label></td>
										</c:otherwise>
									</c:choose>
									<c:choose>
										<c:when test="${patconsultation.map.bill_status ne 'A'}">
											<td>
												<insta:screenlink target="_blank" screenId="credit_bill_collection"
													extraParam="?_method=getCreditBillingCollectScreen&billNo=${patconsultation.map.bill_no}"
													label="${patconsultation.map.bill_no} /${consulBillStatus[patconsultation.map.bill_status]}" addPipe="false"/>
											</td>
										</c:when>
										<c:otherwise>
											<td>
												<label id="consulBillStatus${status.index}">
												${patconsultation.map.bill_no} /${consulBillStatus[patconsultation.map.bill_status]}
												</label>
											</td>
										</c:otherwise>
									</c:choose>
									<td>
										<c:url var="consPrintUrl" value="/print/printConsultation.json">
											<c:param name="consultation_id" value="${patconsultation.map.consultation_id}"/>
										</c:url>
										<a href="#" onclick="return openConsultationPrint(this, '${consPrintUrl}');" target="_blank" title="Print Consultation">Print</a>
									</td>
									<td style="text-align:right;" >
										<input type="hidden" name="base_consultation_type" value="${patconsultation.map.consultation_type}" />
										<input type="hidden" name="base_consultation_type_id" value="${patconsultation.map.consultation_type_id}" />

										<input type="hidden" name="consultation_bill_status" value="${patconsultation.map.bill_status}"/>
										<input type="hidden" name="consultation_status" value="${patconsultation.map.consultation_status}"/>

										<input type="hidden" name="consulRowId" id="consulRowId${status.index}" value= "${status.index}"/>
										<input type="hidden" name="consultation_type" value="${patconsultation.map.consultation_type}" />
										<input type="hidden" name="consultation_type_id" value="${patconsultation.map.consultation_type_id}" />
										<input type="hidden" name="doctor_id" value="${patconsultation.map.doctor_id}" />
										<input type="hidden" name="item_code" value="${patconsultation.map.item_code}"/>
										<input type="hidden" name="consul_code_type" value="${patconsultation.map.code_type}"/>
										<input type="hidden" name="consultation_id" value="${patconsultation.map.consultation_id}" />
										<input type="hidden" name="consul_code_desc" value="${patconsultation.map.code_desc}" />
										<input type="hidden" name="head" value="${patconsultation.map.consultation_type_id}" />
										<input type="hidden" name="vitalCharge_id" value="${patconsultation.map.charge_id}" />

										<c:set var="vitalObsCnt" value="0"/>
										<div id="vitalObservations.${patconsultation.map.charge_id}" style="display: inline">
											<c:forEach items="${patientVitalObservations}" varStatus="obsStatus" var="obs">
												<c:if test="${obs.charge_id eq patconsultation.map.charge_id}">
												    <c:set var="vitalObsCnt" value="${vitalObsCnt+1}"/>
													<input type="hidden" name="vitalObserCode.${patconsultation.map.charge_id}" id="vitalObserCode.${patconsultation.map.charge_id}${vitalObsCnt}" value="${obs.code}"/>
													<input type="hidden" name="vitalObserType.${patconsultation.map.charge_id}" id="vitalObserType.${patconsultation.map.charge_id}${vitalObsCnt}" value="${obs.observation_type}"/>
													<input type="hidden" name="vitalObserValue.${patconsultation.map.charge_id}" id="vitalObserValue.${patconsultation.map.charge_id}${vitalObsCnt}" value="<c:out value='${obs.value}'/>"/>
													<input type="hidden" name="vitalObserValueType.${patconsultation.map.charge_id}" id="vitalObserValueType.${patconsultation.map.charge_id}${vitalObsCnt}" value="${obs.value_type}"/>
													<input type="hidden" name="vitalObserValueEditable.${patconsultation.map.charge_id}" id="vitalObserValueEditable.${patconsultation.map.charge_id}${vitalObsCnt}" value="${obs.value_editable}"/>
													<input type="hidden" name="vitalObserCodeDesc.${patconsultation.map.charge_id}" id="vitalObserCodeDesc.${patconsultation.map.charge_id}${vitalObsCnt}" value="${obs.code_desc}"/>
												</c:if>
											</c:forEach>
											<input type="hidden" id="vitalObserIndex.${patconsultation.map.charge_id}" value="${vitalObsCnt}"/>
											<c:if test="${vitalObsCnt > 0}"> <img id="vitalObserImg.${patconsultation.map.charge_id}" src='${cpath}/images/yellow_flag.gif'> </c:if>
										</div>
										<c:choose>
										<c:when test="${codeDone ne 'Y'}">
											<a href="javascript:void(0)" onclick="return openConsulEditDialog(this, '${status.index}')" title="Add/Edit Consultation codes">
												<img src="${cpath}/icons/Edit.png"/>
											</a>
										</c:when>
										<c:otherwise>
											<img src="${cpath}/icons/Edit1.png" />
										</c:otherwise>
										</c:choose>
									</td>
								</tr>
							</c:forEach>
						</table>
						<table width="100%" style="margin-top: 10px">
							<tr>
								<td width="100%" >
									<div style="float: right">
										<label>Consultation Print Settings: </label>
										<c:set var="consTemplateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
										<c:set var="consTemplateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>
										<c:if test="${not empty consPrintTemplates}">
											<c:forEach var="temp" items="${consPrintTemplates}">
												<c:set var="consTemplateValues" value="${consTemplateValues},${temp.template_name}"/>
												<c:set var="consTemplateTexts" value="${consTemplateTexts},${temp.template_name}"/>
											</c:forEach>
										</c:if>
										<insta:selectoptions name="consPrintTemplate" id="consTemplateList" opvalues="${consTemplateValues}"
															optexts="${consTemplateTexts}" dummyvalue="-- Select Template --" value=""/>
										<insta:selectdb name="consPrinterId" id="consPrinterId" table="printer_definition" class="dropdown"
															valuecol="printer_id"  displaycol="printer_definition_name"
															value="${consPrinterId}"/>
									</div>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
			<tr>
				<td>
					<c:if test="${(opType == 'M' or opType == 'F' or opType == 'D' or opType == 'R') and not empty prevDiagnoses}" >
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Previous Main Visit Diagnosis</legend>
								<table width="100%" class="formtable" id="prevdiagnosiscodes">
									<c:forEach items="${prevDiagnoses}" varStatus="pstatus" var="ppatdiagnosis">
									<tr>
										<td class="formlabel" style="white-space: nowrap">
											<c:if test="${ppatdiagnosis.map.diag_type eq 'P'}" >Primary Diagnosis(Type):</c:if>
											<c:if test="${ppatdiagnosis.map.diag_type eq 'S'}" >Secondary Diagnosis(Type):</c:if>
											<c:if test="${ppatdiagnosis.map.diag_type eq 'A'}" >Admitting Diagnosis(Type):</c:if>
											<c:if test="${ppatdiagnosis.map.diag_type eq 'V'}" >Reason For Visit(Type):</c:if>
										</td>
										<td class="forminfo">${ppatdiagnosis.map.icd_code}(${ppatdiagnosis.map.code_type}) </td>

										<td class="formlabel">Code Description:</td>
										<td colspan="2" class="forminfo">${ppatdiagnosis.map.code_desc}</td>
									</tr>
								</c:forEach>
								</table>
						</fieldset>
					</c:if>
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="ui.label.diagnosis.details.entered.by.doctor"/></legend>
					<table width="100%" class="formtable" id="docEnteredDiagnosis">
					<c:choose>
						<c:when test="${not empty docEnteredDiagnosis }">
								<c:forEach items="${docEnteredDiagnosis}" varStatus="pristatus" var="pripatdiagnosis">
										<c:if test="${pripatdiagnosis.map.diag_type eq 'P'}">
										<tr>
											<td class="formlabel" style="white-space: nowrap">
												<c:if test="${pripatdiagnosis.map.diag_type eq 'P'}" >Primary Diagnosis(Type):</c:if>
											</td>
											<td class="forminfo">${pripatdiagnosis.map.icd_code}(${pripatdiagnosis.map.code_type}) </td>
											<td class="formlabel">Code Description:</td>
											<td colspan="2" class="forminfo">${pripatdiagnosis.map.code_desc}</td>
											<c:if test="${patient.visit_type eq 'i'}">
												<td class="formlabel">Present on Admission:</td>
												<td colspan="2" class="forminfo">${presentOnAdmission[pripatdiagnosis.map.present_on_admission]}</td>
											</c:if>
										</tr>
										</c:if>
									</c:forEach>
									<c:forEach items="${docEnteredDiagnosis}" varStatus="status"
										var="patdiagnosis">
										<c:if test="${patdiagnosis.map.diag_type ne 'P'}">
											<tr>
												<td class="formlabel" style="white-space: nowrap"><c:if
														test="${patdiagnosis.map.diag_type eq 'S'}">Secondary Diagnosis(Type):</c:if>
													<c:if test="${patdiagnosis.map.diag_type eq 'A'}">Admitting Diagnosis(Type):</c:if>
													<c:if test="${patdiagnosis.map.diag_type eq 'V'}">Reason For Visit(Type):</c:if>
												<td class="forminfo">${patdiagnosis.map.icd_code}(${patdiagnosis.map.code_type})
												</td>
												<td class="formlabel">Code Description:</td>
												<td colspan="2" class="forminfo">${patdiagnosis.map.code_desc}</td>
												<c:if test="${patient.visit_type eq 'i'}">
													<td class="formlabel">Present on Admission:</td>
													<td colspan="2" class="forminfo">${presentOnAdmission[patdiagnosis.map.present_on_admission]}</td>
												</c:if>
											</tr>
										</c:if>
									</c:forEach>
						</c:when>
						<c:otherwise>
							<tr>
								<td colspan="2" class="forminfo">No diagnosis codes entered by doctor.</td>
								<td></td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
						</c:otherwise>
					</c:choose>
					</table>
					</fieldset>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel"><insta:ltext key="ui.label.code.diagnosis.details"/></legend>

							<table width="100%" class="formtable" id="diagnosiscodes">
							<c:forEach items="${patientDiagnosis}" varStatus="pristatus" var="pripatdiagnosis">
								<c:if test="${pripatdiagnosis.map.diag_type eq 'P'}">
								<tr>
									<td class="formlabel" style="white-space: nowrap">
										<c:if test="${pripatdiagnosis.map.diag_type eq 'P'}" >Primary Diagnosis(Type):</c:if>
										<input type="hidden" name="diag_type" value="${pripatdiagnosis.map.diag_type}" />
										<input type="hidden" name="present_on_admission" value="${pripatdiagnosis.map.present_on_admission}"/>
										<input type="hidden" name="year_of_onset" value="${pripatdiagnosis.map.year_of_onset}"/>
										<input type="hidden" name="diag_type_from_db" value="${pripatdiagnosis.map.diag_type}" />
										<input type="hidden" name="icd_code" value="${pripatdiagnosis.map.icd_code}"/>
										<input type="hidden" name="icd_code_from_db" value="${pripatdiagnosis.map.icd_code}"/>
										<input type="hidden" name="diagcodeortypeedited" value="false"/>
										<input type="hidden" name="diag_code_type" value="${pripatdiagnosis.map.code_type}"/>
										<input type="hidden" name="description" value="${pripatdiagnosis.map.code_desc}"/>
										<input type="hidden" name="id" value="${pripatdiagnosis.map.id}" />
										<input type="hidden" name="master_desc" value="${pripatdiagnosis.map.master_desc}"/>
										<input type="hidden" name="deleted" value="false" />
									</td>
									<td class="forminfo">${pripatdiagnosis.map.icd_code}(${pripatdiagnosis.map.code_type}) </td>

									<td class="formlabel">Code Description:</td>
									<td colspan="2" class="forminfo">${pripatdiagnosis.map.code_desc}</td>
									<c:if test="${patient.visit_type eq 'i'}">
										<td class="formlabel">Present on Admission:</td>
										<td colspan="2" class="forminfo">${presentOnAdmission[pripatdiagnosis.map.present_on_admission]}</td>
									</c:if>
								</tr>
								</c:if>
							</c:forEach>
							<c:forEach items="${patientDiagnosis}" varStatus="status" var="patdiagnosis">
								<c:if test="${patdiagnosis.map.diag_type ne 'P'}">
								<tr>
									<td class="formlabel" style="white-space: nowrap">
										
										<c:if test="${patdiagnosis.map.diag_type eq 'S'}" >Secondary Diagnosis(Type):</c:if>
										<c:if test="${patdiagnosis.map.diag_type eq 'A'}" >Admitting Diagnosis(Type):</c:if>
										<c:if test="${patdiagnosis.map.diag_type eq 'V'}" >Reason For Visit(Type):</c:if>
										<input type="hidden" name="diag_type" value="${patdiagnosis.map.diag_type}" />
										<input type="hidden" name="present_on_admission" value="${patdiagnosis.map.present_on_admission}"/>
										<input type="hidden" name="year_of_onset" value="${patdiagnosis.map.year_of_onset}"/>
										<input type="hidden" name="diag_type_from_db" value="${patdiagnosis.map.diag_type}" />
										<input type="hidden" name="icd_code" value="${patdiagnosis.map.icd_code}"/>
										<input type="hidden" name="icd_code_from_db" value="${patdiagnosis.map.icd_code}"/>
										<input type="hidden" name="diagcodeortypeedited" value="false"/>
										<input type="hidden" name="diag_code_type" value="${patdiagnosis.map.code_type}"/>
										<input type="hidden" name="description" value="${patdiagnosis.map.code_desc}"/>
										<input type="hidden" name="id" value="${patdiagnosis.map.id}" />
										<input type="hidden" name="master_desc" value="${patdiagnosis.map.master_desc}" />
										<input type="hidden" name="deleted" value="false" />
										
									</td>
									<td class="forminfo">${patdiagnosis.map.icd_code}(${patdiagnosis.map.code_type}) </td>

									<td class="formlabel">Code Description:</td>
									<td colspan="2" class="forminfo">${patdiagnosis.map.code_desc}</td>
									<c:if test="${patient.visit_type eq 'i'}">
										<td class="formlabel">Present on Admission:</td>
										<td colspan="2" class="forminfo">${presentOnAdmission[patdiagnosis.map.present_on_admission]}</td>
									</c:if>
								</tr>
								</c:if>
							</c:forEach>
							
							<tr id="codeRow" style="display:none">
								<td class="formlabel" style="white-space: nowrap">
									<input type="hidden" name="diag_type" value="" />
									<input type="hidden" name="present_on_admission" value="" />
									<input type="hidden" name="year_of_onset" value=""/>
									<input type="hidden" name="diag_type_from_db" value="" />
									<input type="hidden" name="icd_code" value=""/>
									<input type="hidden" name="icd_code_from_db" value=""/>
									<input type="hidden" name="diagcodeortypeedited" value=""/>
									<input type="hidden" name="diag_code_type" value=""/>
									<input type="hidden" name="description" value=""/>
									<input type="hidden" name="id" value="" />
									<input type="hidden" name="master_desc" value="" />
									<input type="hidden" name="sent_for_approval" value="" />
									<input type="hidden" name="deleted" value="" />
								</td>
								<td class="forminfo"></td>
								<td class="formlabel">Code Description:</td>
								<td colspan="2" class="forminfo"></td>
								<c:if test="${patient.visit_type eq 'i'}">
									<td class="formlabel">Present on Admission:</td>
									<td colspan="2" class="forminfo"></td>
								</c:if>
								<td class="deleteText" style="color: red; display: none;"> Deleted </td>
							</tr>

							<tr>
								<td colspan="8" align="right">
									<c:if test="${codeDone ne 'Y'}">
										<a href="javascript:void(0)" onclick="return openDiagAddOrEditDialog(this)" title="Add/Edit Diagnosis codes">
											<img src="${cpath}/icons/Edit.png"/>
										</a>
									</c:if>
									<c:if test="${codeDone eq 'Y'}">
										<img src="${cpath}/icons/Edit1.png" />
									</c:if>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
			<tr>
				<td>
					<c:if test="${not empty patientEncCodes}">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Encounter Codes</legend>
						<table class="formtable" id="encounter">
							<tr>
                                <td class="forminfo">Encounter&nbsp;Type:</td>
                                <td class="formlabel" style="width: 60px;">Code/Desc:</td>
								<td class="forminfo" colspan="2">${patientEncCodes.encounter_type} - ${patientEncCodes.enc_type_desc}</td>
                                <td style="text-align: right;">
									<c:if test="${codeDone ne 'Y'}">
										<a href="javascript:void(0)" onclick="return openEditEncDialog(this);" title="Edit Encounter codes" id="encounterHref">
											<img src="${cpath}/icons/Edit.png"/>
										</a>
									</c:if>
									<c:if test="${codeDone eq 'Y'}">
										<img src="${cpath}/icons/Edit1.png" />
									</c:if>
									<input type="hidden" name="encounter_type" value="${patientEncCodes.encounter_type}"/>
									<input type="hidden" name="encounter_start_type" value="${patientEncCodes.encounter_start_type}"/>
									<input type="hidden" name="encounter_end_type" value="${patientEncCodes.encounter_end_type}"/>
									<input type="hidden" name="encounter_start_source" value="${patientEncCodes.transfer_source}"/>
									<input type="hidden" name="encounter_start_source_id" value="${patientEncCodes.transfer_source_id}"/>
									<input type="hidden" name="encounter_end_destination" value="${patientEncCodes.transfer_destination}"/>
									<input type="hidden" name="encounter_end_destination_id" value="${patientEncCodes.transfer_destination_id}"/>
									<input type="hidden" name="encounter_type_desc" value="${patientEncCodes.enc_type_desc}"/>
									<input type="hidden" name="encounter_start_type_desc" value="${patientEncCodes.enc_start_type_desc}"/>
									<input type="hidden" name="encounter_end_type_desc" value="${patientEncCodes.enc_end_type_desc}"/>
                                    <input type="hidden" name="encounter_start_date" value="${patientEncCodes.encounter_start_date}"/>
                                    <input type="hidden" name="encounter_start_time" value="${patientEncCodes.encounter_start_time}"/>
                                    <input type="hidden" name="encounter_end_date" value="${patientEncCodes.encounter_end_date}"/>
                                    <input type="hidden" name="encounter_end_time" value="${patientEncCodes.encounter_end_time}"/>
                                    <input type="hidden" name="is_enc_end_overridden" id="is_enc_end_overridden" value="${patientEncCodes.is_enc_end_overridden}"/>
                                    <input type="hidden" name="insurance_id" value="${patientEncCodes.insurance_id}"/>
									<input type="hidden" name="patient_id" 
									value="${ifn:cleanHtml(param.patient_id)}"/>
								</td>
							</tr>
                            <tr>
                                <td class="forminfo">Encounter&nbsp;Start:</td>
                                <td class="formlabel" style="width: 60px;">Code/Desc:</td>
                                <td class="forminfo">${patientEncCodes.encounter_start_type} - ${patientEncCodes.enc_start_type_desc }</td>
                                <fmt:formatDate var="enc_start_date" value="${patientEncCodes.encounter_start_date}" pattern="dd-MM-yyyy"/>
                                <fmt:formatDate var="enc_start_time" value="${patientEncCodes.encounter_start_time}" pattern="HH:mm"/>
                                <td class="formlabel" style="text-align: right;">Date&Time:</td>
                                <td class="forminfo"><div title="${enc_start_date} ${enc_start_time}">${enc_start_date}&nbsp;${enc_start_time}</div></td>
                            </tr>
                            <tr>
                                <td class="forminfo">Encounter&nbsp;End:</td>
                                <td class="formlabel" style="width: 60px;">Code/Desc:</td>
                                <td class="forminfo" >${patientEncCodes.encounter_end_type} - ${patientEncCodes.enc_end_type_desc }</td>
                                <fmt:formatDate var="enc_end_date" value="${patientEncCodes.encounter_end_date}" pattern="dd-MM-yyyy"/>
                                <fmt:formatDate var="enc_end_time" value="${patientEncCodes.encounter_end_time}" pattern="HH:mm"/>
                                <td class="formlabel" style="text-align: right;">Date&Time:</td>
                                <td class="forminfo"><div title="${enc_end_date} ${enc_end_time}">${enc_end_date}&nbsp;${enc_end_time}</div></td>
                            </tr>
						</table>
					</fieldset>
					</c:if>
				</td>
			</tr>
			<tr>
				<td>
					<c:if test="${not empty patientTrtCodes}">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Treatment Codes</legend>
						<table class="detailList dialog_displayColumns" id="treatment">
							<tr>
								<th>Bill No</th>
								<th>Activity Date</th>
								<th>Order#</th>
								<th>Conduction Status</th>
								<c:choose>
                                    <c:when test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
                                    <th>CEED Check Status</th>
                                     </c:when>
                                </c:choose>
								<th>Type</th>
								<th>Department</th>
								<th>Item</th>
								<th>Code Type</th>
								<th>Code</th>
								<th></th>
							</tr>
							<c:forEach items="${patientTrtCodes}" varStatus="status" var="trt">
							<c:if test="${not ((trt.submission_batch_type =='I' && trt.charge_head == 'PKGPKG') || (trt.submission_batch_type =='P' && trt.charge_head != 'PKGPKG'))}">
								<tr>
									<td>
										<a onclick="" title="" target="_blank"
													href="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen&amp;billNo=${trt.bill_no}">${trt.bill_no}</a>
										<c:choose>
											<c:when test="${trt.is_tpa == true}">(Ins)</c:when>
											<c:otherwise>(Non-Ins)</c:otherwise>
										</c:choose>
										 <input type="hidden" name="act_rate_plan_item_code" value="${trt.act_rate_plan_item_code}"/>
										<input type="hidden" name="trt_code_type" value="${trt.code_type}"/>
										<input type="hidden" name="_act_des_id" value="${trt.act_des_id}"/>
										<input type="hidden" name="act_description" value="${ifn:cleanHtml(trt.act_description)}"/>
										<input type="hidden" name="charge_id" value="${trt.charge_id}"/>
										<input type="hidden" name="trtDrug" value="false"/>
										<input type="hidden" name="prior_auth_id" value="${trt.prior_auth_id}"/>
										<input type="hidden" name="prior_auth_mode_id" value="${trt.prior_auth_mode_id}"/>
										<input type="hidden" name="bill_no" value="${trt.bill_no}"/>
										<input type="hidden" name="is_tpa" value="${trt.is_tpa}"/>
										<input type="hidden" name="trt_mster_desc" value="${trt.master_desc}"/>
										<input type="hidden" name="tooth_num_reqd" value="${trt.tooth_num_reqd}"/>
										<input type="hidden" name="primary_auth_id" value="${trt.primary_auth_id}" />
										<input type="hidden" name="primary_auth_mode_id" value="${trt.primary_auth_mode_id}" />
										<input type="hidden" name="primary_charge_id" value="${trt.primary_charge_id}" />
										<input type="hidden" name="primary_claim_id" value="${trt.primary_claim_id}" />
										<input type="hidden" name="primary_bill_no" value="${trt.primary_bill_no}" />
										<input type="hidden" name="secondary_auth_id" value="${trt.secondary_auth_id}" />
										<input type="hidden" name="secondary_auth_mode_id" value="${trt.secondary_auth_mode_id}" />
										<input type="hidden" name="secondary_charge_id" value="${trt.secondary_charge_id}" />
										<input type="hidden" name="secondary_claim_id" value="${trt.secondary_claim_id}" />
										<input type="hidden" name="secondary_bill_no" value="${trt.secondary_bill_no}" />

										<div id="observations.${trt.charge_id}" style="display: inline">
										<c:set var="obsCnt" value="0"/>
											<c:forEach items="${patientTrtObservations}" varStatus="obsStatus" var="obs">
												<c:if test="${obs.charge_id eq trt.charge_id}">
												    <c:set var="obsCnt" value="${obsCnt+1}"/>
													<input type="hidden"  name="obserCode.${trt.charge_id}" id="obserCode.${trt.charge_id}${obsCnt}" value="${obs.code}"/>
													<input type="hidden" name="obserType.${trt.charge_id}" id="obserType.${trt.charge_id}${obsCnt}" value="${obs.observation_type}"/>
													<input type="hidden" name="obserValue.${trt.charge_id}" id="obserValue.${trt.charge_id}${obsCnt}" value="${obs.value}"/>
													<input type="hidden" name="obserValueType.${trt.charge_id}" id="obserValueType.${trt.charge_id}${obsCnt}" value="${obs.value_type}"/>
													<input type="hidden" name="obserValueEditable.${trt.charge_id}" id="obserValueEditable.${trt.charge_id}${obsCnt}" value="${obs.value_editable}"/>
													<input type="hidden" name="obserCodeDesc.${trt.charge_id}" id="obserCodeDesc.${trt.charge_id}${obsCnt}" value="${obs.code_desc}"/>
													<input type="hidden" name="obserSponsorId.${trt.charge_id}" id="obserSponsorId.${trt.charge_id}${obsCnt}" value="${obs.sponsor_id}"/>
													<input type="hidden" name="obserDocumentId.${trt.charge_id}" id="obserDocumentId.${trt.charge_id}${obsCnt}" value="${obs.document_id}"/>
													<input type="hidden" name="obserFile.${trt.charge_id}" value="" type="file" id="obserFile.${trt.charge_id}${obsCnt}"/>
												</c:if>
											</c:forEach>
											<input type="hidden" id="obserIndex.${trt.charge_id}" value="${obsCnt}"/>
											<c:if test="${obsCnt > 0}"> <img id="obserImg.${trt.charge_id}" src='${cpath}/images/yellow_flag.gif'> </c:if>
										</div>
									</td>
									<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${trt.activity_start_datetime}"/></td>
									<td>${trt.order_number}</td>
									<td>${trt.conducted_status == 'C' || trt.conducted_status == 'Y' ? 'Conducted' : trt.conducted_status == 'P' ? 'In Progress' : 'Not Conducted'}</td>
									<c:choose>
                                        <c:when test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
                                            <td>
                                                <c:choose>
                                                    <c:when test="${!ceedstatus}">
                                                        <div class="ceedcircle black"></div>&nbsp;Not Initiated
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:choose>                
                                                            <c:when test="${ceedResponseMap.containsKey(trt.charge_id)}">
                                                                <c:set var="rank" value="Normal"/>
                                                                <c:set var="circle_color" value="green"/>
                                                                <c:forEach items="${ceedResponseMap[trt.charge_id]}" var="response">
                                                                    <c:choose>
                                                                        <c:when test="${response.claim_edit_rank == 'A'}">
                                                                            <c:set var="rank" value="Alert"/>
                                                                            <c:set var="circle_color" value="red"/>
                                                                        </c:when>
                                                                        <c:when test="${response.claim_edit_rank == 'R' && rank != 'Alert'}">
                                                                            <c:set var="rank" value="Review"/>
                                                                            <c:set var="circle_color" value="yellow"/>
                                                                        </c:when>
                                                                        <c:when test="${response.claim_edit_rank == 'E'}">
                                                                            <c:set var="rank" value="Error"/>
                                                                            <c:set var="circle_color" value="orange"/>
                                                                         </c:when>
                                                                        <c:when test="${response.claim_edit_rank == 'NA'}">
                                                                            <c:set var="rank" value="Not Applicable"/>
                                                                            <c:set var="circle_color" value="grey"/>
                                                                        </c:when>
                                                                    </c:choose>
                                                                </c:forEach>
                                                                <div class="ceedcircle ${circle_color}" ></div>&nbsp;${rank}
                                                            </c:when>
                                                            <c:otherwise>
                                                                <div class="ceedcircle black"></div>&nbsp;Not Initiated
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:otherwise>
                                                </c:choose>                           
                                            </td>
                                        </c:when>
                                    </c:choose>
									<td>${trt.chargehead_name}</td>
									<td><insta:truncLabel  value="${trt.dept_name}" length="25"/></td>
									<td><insta:truncLabel  value="${trt.act_description}" length="25"/></td>
									<td>${trt.code_type}</td>
									<td>${trt.act_rate_plan_item_code}</td>
									<td style="text-align: right;">
										<c:if test="${codeDone ne 'Y'}">
											<a href="javascript:void(0)" onclick="return openEditTrtDialog(this);" title="Edit treatment code">
												<img src="${cpath}/icons/Edit.png" />
											</a>
										</c:if>
										<c:if test="${codeDone eq 'Y'}">
											<img src="${cpath}/icons/Edit1.png" />
										</c:if>
									</td>
								<%-- 	<td>
									<input type="file" id="uploadFile${trtCodeCnt}" name="uploadFile[${trtCodeCnt}]" 
										class="testFileUpload" style="width: 200px;" multiple/>
									</td> --%>
								</tr>
							</c:if>
							</c:forEach>
						</table>
					</fieldset>
					</c:if>
				</td>
			</tr>
			<c:if test="${not empty drgCode && useDRG}">
			<tr>
				<td>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">DRG Code</legend>
						<table class="formtable" id="drg">
							<tr>
								<td class="formlabel">DRG Bill No.:
									<a title="DRG Coded Bill No." target="_blank"
									   href="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen&amp;billNo=${drgCode.drg_bill_no}">${drgCode.drg_bill_no}</a>
									<input type="hidden" name="drg_code" value="${drgCode.drg_code}"/>
									<input type="hidden" name="drg_description" value="${drgCode.drg_description}"/>
									<input type="hidden" name="drg_charge_id" value="${drgCode.drg_charge_id}"/>
									<input type="hidden" name="drg_bill_no" value="${drgCode.drg_bill_no}"/>
									<input type="hidden" name="drg_bill_status" value="${drgCode.drg_bill_status}"/>

									<div id="drgObservations.${drgCode.drg_charge_id}" style="display: inline">
										<c:set var="drgObsCnt" value="0"/>
										<c:forEach items="${patientDrgObservations}" varStatus="drgObsStatus" var="drgObs">
											<c:if test="${drgObs.charge_id eq drgCode.drg_charge_id}">
												<c:set var="drgObsCnt" value="${drgObsCnt+1}"/>
												<input type="hidden"  name="drgObserCode.${drgCode.drg_charge_id}" id="drgObserCode.${drgCode.drg_charge_id}${drgObsCnt}" value="${drgObs.code}"/>
												<input type="hidden" name="drgObserType.${drgCode.drg_charge_id}" id="drgObserType.${drgCode.drg_charge_id}${drgObsCnt}" value="${drgObs.observation_type}"/>
												<input type="hidden" name="drgObserValue.${drgCode.drg_charge_id}" id="drgObserValue.${drgCode.drg_charge_id}${drgObsCnt}" value="${drgObs.value}"/>
												<input type="hidden" name="drgObserValueType.${drgCode.drg_charge_id}" id="drgObserValueType.${drgCode.drg_charge_id}${drgObsCnt}" value="${drgObs.value_type}"/>
												<input type="hidden" name="drgObserValueEditable.${drgCode.drg_charge_id}" id="drgObserValueEditable.${drgCode.drg_charge_id}${drgObsCnt}" value="${drgObs.value_editable}"/>
												<input type="hidden" name="drgObserCodeDesc.${drgCode.drg_charge_id}" id="drgObserCodeDesc.${drgCode.drg_charge_id}${drgObsCnt}" value="${drgObs.code_desc}"/>
											</c:if>
										</c:forEach>
										<input type="hidden" id="drgObserIndex.${drgCode.drg_charge_id}" value="${drgObsCnt}"/>
										<c:if test="${drgObsCnt > 0}"> <img id="drgObserImg.${drgCode.drg_charge_id}" src='${cpath}/images/yellow_flag.gif'> </c:if>
									</div>
								</td>
								<td class="formlabel">DRG Code:</td>
								<td class="forminfo" colspan="3">${drgCode.drg_code} - ${drgCode.drg_description}</td>
								<td class="formlabel">Code Type: ${drgCode.drg_code_type}</td>
								</td>
								<td style="text-align: right;">
									<c:if test="${codeDone ne 'Y'}">
										<a href="javascript:void(0)" onclick="return openEditDRGDialog(this);" title="Edit DRG Code">
											<img src="${cpath}/icons/Edit.png"/>
										</a>
									</c:if>
									<c:if test="${codeDone eq 'Y'}">
										<img src="${cpath}/icons/Edit1.png" />
									</c:if>
								</td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
			</c:if>

			<c:if test="${not empty perdiemCode && usePerdiem}">
			<tr>
				<td>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Perdiem Code</legend>
						<table class="formtable" id="perdiem">
							<tr>
								<td class="formlabel">Perdiem Bill:
									<a title="Perdiem Coded Bill No." target="_blank"
									   href="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen&amp;billNo=${perdiemCode.perdiem_bill_no}">${perdiemCode.perdiem_bill_no}</a>
								</td>
								<td class="formlabel">Perdiem Code:</td>
								<td class="forminfo" colspan="3">
									<insta:truncLabel value="${perdiemCode.per_diem_code} ( ${perdiemCode.per_diem_description} )" length="60"/>
								</td>
								<td class="formlabel">Code Type: </td>
								<td class="forminfo">${perdiemCode.per_diem_code_type} </td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
			</c:if>
		</table>
		<div style="display: ${not empty ceedbean ? 'block' : 'none'}">
		  <font >
		  <c:choose>
		      <c:when test="${ceedbean.map.service_type == 'M'}">
		      Medical Necessity Check
		      </c:when>
		      <c:when test="${ceedbean.map.service_type == 'C'}">
		      Coding Check
		      </c:when>
		  </c:choose>
		  Last Run Date/Time: 
		  <b><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${ceedbean.map.response_datetime}"/></b>
		  </font>
                <img class="imgHelpText" src="${cpath}/images/help.png" 
                    title='NOTE: If there have been any changes to the diagnosis / prescribed activities please make a fresh CEED check submission'/>
		</div>
		<br>
		<table>
			<tr>
				<td>
					Finalize All Bills: <input type="checkbox" name="finalizeAll" id="finalizeAll"
						<c:if test="${finalizeAll eq 'Y'}">checked disabled</c:if>  />
					<c:if test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
					Ceed Check Type:
					   <select class="dropdown" id="ceed_check_type" name="ceed_check_type" >
					       <option value="2" ${ceedbean.map.service_type == 'M' ? 'selected' : ''}>Medical Necessity</option>
					       <option value="1" ${ceedbean.map.service_type == 'C' ? 'selected' : ''}>Coding</option>
					   </select>
					</c:if>
					Codification Status:
						<select class="dropdown" id="codification_status" name="codification_status" ${codification_status == 'V' ? 'disabled' : ''}>
							<option value="">-- Select --</option>
							<option value="P" ${codification_status == 'P' ? 'selected' : ''}>In-Progress</option>
							<option value="C" ${codification_status == 'C' ? 'selected' : ''}>Completed</option>
							<option value="R" ${codification_status == 'R' ? 'selected' : ''}>Completed-Needs Verification</option>
							<option value="V" ${codification_status == 'V' ? 'selected' : ''}>Verified and Closed</option>
						</select>
						<input type ="hidden" id="last_codification_status" value="${codification_status}" />
				</td>
			</tr>
		</table>
		<table class="screenActions">
			<tr>
				<td>
					<c:choose>
					<c:when test="${codification_status eq 'V'}">
						<button type="button" accesskey="R" name="reopen" value="Reopen" onclick="reopenCodification();"
						<c:if test="${roleId ne '1' and roleId ne '2' and actionRightsMap['reopen_codification'] ne 'A'}">disabled</c:if> >
						<label><b><u>R</u></b>eopen</label></button>&nbsp;
					</c:when>
					<c:otherwise>
						<button type="button" accesskey="A" name="save" value="Save" onclick="return validate();">
						<label>S<b><u>A</u></b>ve</label></button>&nbsp;
						<c:if test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'}" >
                            <button type="button" name="send" id="SaveAndCodeCheck" accessKey="R" onclick="return validate(true);">
                            <insta:ltext key="patient.outpatientlist.consult.details.save.and"/> <b><u><insta:ltext key="patient.outpatientlist.consult.details.r"/></u></b><insta:ltext key="patient.outpatientlist.consult.details.runcodecheckmrd"/></button>&nbsp;
                        </c:if>
					</c:otherwise>
					</c:choose>
					<label>|</label>
					<a href="${cpath}/pages/medicalrecorddepartment/MRDUpdate.do?_method=view&patient_id=${patient.patient_id}"
					   target="_blank">View Report</a>
					<label>|</label>
					 <input type="hidden" name="referer" value="${referer}"/>
					 <a href="${referer}">Back</a>
					 <c:forEach items="${consultations}" var="consultation">
					 	  | <a href="${cpath}/emr/print.do?method=printClinicalInfo&consultation_id=${consultation.map.consultation_id}"
								title="Print Clinical Information Sheet" target="_blank">${consultation.map.doctor_full_name} - <fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${consultation.map.visited_date}"/> - Clinical Info</a>
					 </c:forEach>
					  <c:forEach items="${salesBillList}" var="sale">
					 	  | <a href="${cpath}/pages/stores/editSales.do?_method=getSaleDetails&duplicate=true&saleId=${sale.map.sale_id}&saleId=&printerId=0&billNo=${sale.map.bill_no}&visitId=${sale.map.visit_id}&printerType=0&pharm_bill_type=${sale.map.pharm_bill_type}&visitType=${sale.map.visit_type}&bill_visit_type=${sale.map.visit_type}"
								title="Sales Bill" target="_blank">${sale.map.sale_id} - Sales Info</a>
					 </c:forEach>
					 <insta:screenlink screenId="visit_emr_screen" extraParam="?_method=list&visit_id=${patient.patient_id}"
						target="_blank" label="Visit EMR Search" addPipe="true"/>
					 <insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}&filterType=visits"
						target="_blank" label="Patient EMR Search(Visit Based)" addPipe="true"/>
					 <insta:screenlink screenId="emr_screen" extraParam="?_method=list&mr_no=${patient.mr_no}&filterType=docType&fromDate=&toDate="
						target="_blank" label="Patient EMR Search(Document Based)" addPipe="true"/>
					 <c:if test="${not empty preferences.modulesActivatedMap.mod_coder_claim_review && preferences.modulesActivatedMap.mod_coder_claim_review == 'Y'}">
						 <label>|</label>
  					 	<a href="${cpath}/coderreviews/list.htm?patient_id=${param.patient_id}&mr_no=${patient.mr_no}&_mysearch=nosearch&status=open&status=inprogress&sortOrder=assigned_to_role_name"  
						 label="Coder Claim Reveiws" addPipe="true">Coder Claim Reviews(<span id="reviewCountId">${reviewsCount}</span>)</a>
					</c:if>
					<insta:screenlink screenId="edit_visit_details" extraParam="?_method=getPatientVisitDetails&patient_id=${ifn:cleanURL(patient.patient_id)}&ps_status=active"
						target="_blank" label="Edit Visit Details" addPipe="true"/>
					<insta:screenlink screenId="reg_general" extraParam="?_method=show&patient_id=${ifn:cleanURL(patient.patient_id)}
						&mr_no=${patient.mr_no}&visit_type=${patient.visit_type}&mrno=${patient.mr_no}&visitId=${ifn:cleanURL(patient.patient_id)}"
						target="_blank" label="Edit Patient Details" addPipe="true"/>
					<c:if test="${patient.visit_type == 'o' && patient.use_perdiem == 'N'}">
						|<a href="${cpath}/insurance/showInsuranceDetails.htm?visitId=${ifn:cleanURL(patient.patient_id)}" target="_blank"><insta:ltext key="registration.patient.label.add.or.editinsurance"/></a>
					</c:if>
					<c:if test="${patient.visit_type == 'i' || patient.use_perdiem == 'Y'}">
						|<a href="${pageContext.request.contextPath}/editVisit/changeTPA.do?_method=changeTpa&visitId=${ifn:cleanURL(patient.patient_id)}" target="_blank"><insta:ltext key="registration.categorychange.details.add.or.editinsurance"/></a>
					</c:if>
				</td>
			 </tr>
		</table>
		<c:if test="${not empty patientTrtCodes || not empty patientVitalObservations}">
		<div class="legend" style="display:'block'" >
			<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
			<div class="flagText">Observations Present</div>
		</div>
		</c:if>
	</form>
	</c:otherwise>
	</c:choose>

	<jsp:include  page="/pages/medicalrecorddepartment/MRDCodesEditDialogs.jsp" />

	<script>
		if( document.getElementById('CollapsiblePanel1') ) {
			var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
		}
	</script>
 	</body>
</html>
