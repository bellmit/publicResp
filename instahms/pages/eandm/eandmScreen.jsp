<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<head>
		<title>E&M Calculator</title>

		<insta:link type="script" file="hmsvalidation.js"/>
		<insta:link type="script" file="eandmCalculator/eandmCalculation.js" />
		<style type="text/css">
			.scrolForContainer .yui-ac-content {
				 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
			    _height:18em; max-width:30em; width:30em;/* ie6 */
			}
		</style>
		<script>
			var cpath = '${cpath}';
			var consultationId = '${ifn:cleanJavaScript(param.consultationId)}';
			var consulItemCodes = ${allowdConsulItemCodes};
			var consultationSupportedCodeType = '${ifn:cleanJavaScript(consultationSupportedCodeType)}';
		</script>
	</head>
		<body onload="init();ajaxForPrintUrls();">
			<h1>E&M Calculator</h1>
			<insta:feedback-panel/>
			<insta:patientdetails visitid="${consultationBean.map.patient_id}" showClinicalInfo="true"/>
			<form action="eandmcalculator.do" name="eandmscreen" method="GET" autocomplete="off">

				<input type="hidden" name="patientType" id="patientType" value="${patient.visit_type}"/>
				<input type="hidden" name="mrNo" id="mrNo" value="${patient.mr_no}" />
				<input type="hidden" name="org_id" id="org_id" value="${empty patient.org_id ? 'ORG0001' :patient.org_id}"/>
				<input type="hidden" name="_method" id="_method" value="getScreen" />
				<input type="hidden" name="print" id="print" value="false"/>
				<input type="hidden" name="consultationId" value="${ifn:cleanHtmlAttribute(param.consultationId)}" />
				<input type="hidden" name="consultationStatus" id="consultationStatus" value="${consultationBean.map.status}">
				<input type="hidden" name="billStatus" id="billStatus" value="${consultationBean.map.bill_status}"/>
				<fieldset class="fieldSetBorder">
					<table class="formtable" width="100%">
						<tr>
							<td class="formlabel">Calculation last finalized by: </td>
							<td class="forminfo">${eandmBean.map.user_name}</td>
							<td class="formlabel">Date: </td>
							<td class="forminfo"><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${eandmBean.map.code_finalized_date}"/></td>
						</tr>
						<tr>
							<td class="formlabel" colspan="3"></td>
							<td><input type="button" value="Clear Calculation" name="clear_calc" onclick="clearMdmAndEmCode();"/></td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" >
				<legend	class="fieldSetLabel">Visit Type</legend>
					<table class="formtable" width="100%">
						<tr>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel" style="padding-bottom: 12px">Visit Type:</td>
							<td style="text-align: left; padding-bottom: 12px">
								<select name="visitTypes" id="visitTypes" class="dropdown" onchange="clearMdmAndEmCode();">
									<option value="">-- Select --</option>
									<option value="N" ${eandmBean.map.visit_type == 'N' ? 'selected' : ''}>New</option>
									<option value="E" ${eandmBean.map.visit_type == 'E' ? 'selected' : ''}>Established</option>
									<option value="M" ${eandmBean.map.visit_type == 'M' ? 'selected' : ''}>Emergency</option>
								</select>
							</td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" style="margin-top: 10px">
				<legend	class="fieldSetLabel">History and Physical Examination counts</legend>
					<table class="formtable">
						<c:set var="hpiCount" value="${countedMap['HPI']}" />
						<c:set var="rosCount" value="${countedMap['ROS']}" />
						<c:set var="pfshCount" value="${countedMap['PFSH']}" />
						<c:set var="peCount" value="${countedMap['PE']}" />

						<input type="hidden" name="hpiCount" id="hpiCount" value="${hpiCount}" />
						<input type="hidden" name="rosCount" id="rosCount" value="${rosCount}" />
						<input type="hidden" name="pfshCount" id="pfshCount" value="${pfshCount}" />
						<input type="hidden" name="peCount" id="peCount" value="${peCount}" />

						<tr>
							<td class="formlabel">HPI:</td>
							<td>${hpiCount}</td>
							<td class="formlabel">ROS:</td>
							<td>${rosCount}</td>
							<td class="formlabel">PFSH:</td>
							<td>${pfshCount}</td>
							<td class="formlabel">History Count:</td>
							<td>${hpiCount + rosCount + pfshCount}</td>
						</tr>
						<tr>
							<td class="formlabel" colspan="7">Physical Examination Count:</td>
							<td>${peCount}</td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" style="margin-top: 10px">
				<legend	class="fieldSetLabel">Treatment Options</legend>
					<table class="formtable">

						<tr>
							<td  class="formlabel">Problem Status :</td>
							<td >
								<select name="problemStatus" id="problemStatus" style="width: 189px" class="dropdown" onchange="validateTreatmentCount();clearMdmAndEmCode()">
									<option value="">-- Select --</option>
									<option value="SM" ${eandmBean.map.problem_status eq 'SM' ? 'selected' : ''}>Self-limited or minor (stable, improved or worsening)</option>
									<option value="EI" ${eandmBean.map.problem_status eq 'EI' ? 'selected' : ''}>Established Problem (to examiner) stable, improved</option>
									<option value="EW" ${eandmBean.map.problem_status eq 'EW' ? 'selected' : ''}>Established problem (to examiner) worsening</option>
									<option value="NN" ${eandmBean.map.problem_status eq 'NN' ? 'selected' : ''}>New problem (to examiner) no additional workup planned</option>
									<option value="NW" ${eandmBean.map.problem_status eq 'NW' ? 'selected' : ''}>New problem (to examiner), Additional workup planned</option>
								</select>
							</td>
							<td  class="formlabel">Treatment Options Count :</td>
							<td >
								<input type="text" class="number" name="treatmentCount" id="treatmentCount"
									onblur="validateTreatmentCount();clearMdmAndEmCode();" onkeypress="return enterNumOnlyzeroToNine(event)" value="${eandmBean.map.treatment_options_count}" />
							</td>
						</tr>
						<tr>
							<td colspan="3" class="formlabel">Calculated Treatment Options Count:</td>
							<td><label id="calcOptionsCount">${eandmBean.map.calculated_treatment_options_count}</label></td>
							<input type="hidden" name="calcTreatmentOptionsCount" id="calcTreatmentOptionsCount" value="${eandmBean.map.calculated_treatment_options_count}"/>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" style="margin-top: 10px">
				<legend	class="fieldSetLabel">Risk Evaluation</legend>
					<table class="formtable">
						<tr>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
						</tr>
						<tr>
							<td colspan="2" class="formlabel">Risk of complications,Morbidity and/ mortality :</td>
							<td colspan="2">
								<select name="risk" id="risk" class="dropdown">
									<option value="">-- Select --</option>
									<option value="1" ${eandmBean.map.risk_count eq '1' ? 'selected' : ''}>Minimum</option>
									<option value="2" ${eandmBean.map.risk_count eq '2' ? 'selected' : ''}>Low</option>
									<option value="3" ${eandmBean.map.risk_count eq '3' ? 'selected' : ''}>Moderate</option>
									<option value="4" ${eandmBean.map.risk_count eq '4' ? 'selected' : ''}>High</option>
								</select>
							</td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" style="margin-top: 10px">
				<legend	class="fieldSetLabel">Amount and Complexity of Data Review</legend>
					<table class="formtable">
						<tr>
							<td style="height: 2px" width="17.5%">&nbsp;</td>
							<td style="height: 2px" width="7.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
							<td style="height: 2px" width="12.5%">&nbsp;</td>
						</tr>
						<tr>
							<td colspan="8" style="paddin-bottom: 25px">
								<input type="checkbox" class="complexity" name="complexity1" id="complexity1" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="1" ${not empty eandmBean.map.complexity1 ? 'checked' : ''}>Review and/or order of any Laboratory tests</br>
								<input type="checkbox" class="complexity" name="complexity2" id="complexity2" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="1" ${not empty eandmBean.map.complexity2 ? 'checked' : ''}>Review and or Order of tests in Radiology</br>
								<input type="checkbox" class="complexity" name="complexity3" id="complexity3" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="1" ${not empty eandmBean.map.complexity3 ? 'checked' : ''}>Review and or order of tests in the Medicine section of CPT </br>
								<input type="checkbox" class="complexity" name="complexity4" id="complexity4" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="1" ${not empty eandmBean.map.complexity4 ? 'checked' : ''}>Discussion of Test results with the performing physician </br>
								<input type="checkbox" class="complexity" name="complexity5" id="complexity5" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="1" ${not empty eandmBean.map.complexity5 ? 'checked' : ''}>Decision to obtain old records and or obtain history from someone other than patient</br>
								<input type="checkbox" class="complexity" name="complexity6" id="complexity6" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="2" ${not empty eandmBean.map.complexity6 ? 'checked' : ''}>Review and summarization of old records and/ or obtaining history from </br>
   															 <label style="padding-left: 19px">some one other than the patient and/or discussion of case with another health care provider</label> </br>
								<input type="checkbox" class="complexity" name="complexity7" id="complexity7" onclick="calcComplexityCount(this);clearMdmAndEmCode();"
									value="2" ${not empty eandmBean.map.complexity7 ? 'checked' : ''}>Independent visualization of image, specimen or tracing</br>
							</td>
						</tr>
						<tr>
							<td style="text-align: left; padding-top: 19px;" colspan="1">Data&nbsp;Complexity&nbsp;Count:</td>
							<td style="padding-top: 19px; text-align: left" colspan="1"><label  id="complexityCount">${eandmBean.map.data_amount_complexity_count}</label></td>
							<input type="hidden" name="complexityCountHiddenVal" id="complexityCountHiddenVal" value="${eandmBean.map.data_amount_complexity_count}" />
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder" style="margin-top: 10px">
					<table class="formtable" width="100%">
						<tr>
							<td colspan="6">
								<input type="button" value="Final E&M code" onclick="calcMdmAndEmcode();"/>
								<input type="hidden" name="mdmHiddenVal" id="mdmHiddenVal" value="${eandmBean.map.mdm_value}" />
							</td>
						</tr>
						<tr>
							<c:set var="finalMdmValue">
								<c:choose>
									<c:when test="${eandmBean.map.mdm_value eq 1}">Straight Forward</c:when>
									<c:when test="${eandmBean.map.mdm_value eq 2}">Low</c:when>
									<c:when test="${eandmBean.map.mdm_value eq 3}">Moderate</c:when>
									<c:when test="${eandmBean.map.mdm_value eq 4}">High</c:when>
								</c:choose>
							</c:set>
							<td class="formlabel" >Final MDM Value :</td>
							<td class="forminfo" ><label id="mdmValLabel">${finalMdmValue}</label></td>
							<td class="formlabel" >E&amp;M Code :</td>
							<td class="forminfo"><label id="eandmcode">${eandmBean.map.em_code}</label></td>
							<input type="hidden" name="eandmCodeHiddenVal" id="eandmCodeHiddenVal" value="${eandmBean.map.em_code}" />
							<td class="formlabel">Consultation Type: </td>
							<td class="forminfo">
								<input type="hidden" name="base_consultation_type_id" id="base_consultation_type_id" value="${consultationBean.map.head}"/>
								<input type="hidden" name="consultation_type_id" id="consultation_type_id" value="${consultationBean.map.head}"/>
								<div id="consultTypeLabelDiv">
									<label id="consult_type_label">${consultationBean.map.consultation_type}</label>
								</div>
								<div style="display: none" id="consultTypeDropDiv">
									<select class="dropdown" name="consult_type_drop" id="consult_type_drop" onblur="return setConsultationType(this);">
									</select>
								</div>
							</td>
						</tr>
						<tr>
							<td colspan="6">
								<input type="checkbox" name="finalize_n_update" id="finalize_n_update" onclick="enableFields(this)" ${not empty eandmBean.map.finalized_user_name ? 'checked disabled' : ''}> Finalize and Update Codification
								<input type="hidden" name="h_finalize_n_update" id="h_finalize_n_update" value="${not empty eandmBean.map.finalized_user_name ? 'true' : 'false'}"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Codification E&M Code: </td>
							<td>
								<div id="itemCodeDiv" style="padding-bottom: 20px">
									<input type="text" name="item_code" id="item_code" value="${eandmBean.map.finalized_em_code}" style="width: 80px" disabled="disabled"/>
									<div id="itemCodeContainer" class="scrolForContainer"/>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Remarks</td>
							<td colspan="5"><textarea cols="100" rows="3" name="remarks" id="remarks" disabled="disabled">${eandmBean.map.remarks}</textarea></td>
						</tr>
					</table>
				</fieldset>
				<div class="screenActions">
					<button type="button" id="save" accesskey="s" onclick="validate(false);" ${empty param.consultationId || consultationBean.map.bill_status != 'A' ? 'disabled' : ''}><label><u><b>S</b></u>ave</label></button> |
					<button type="button" id="printBtn" accesskey="p" onclick="validate(true);" ${empty param.consultationId || consultationBean.map.bill_status != 'A' ? 'disabled' : ''} ><label>Save & <u><b>P</b></u>rint</label></button>
					<c:url var="consultScreenUrl" value="/outpatient/OpPrescribeAction.do">
						<c:param name="_method" value="list"/>
						<c:param name="consultation_id" value="${param.consultationId}" />
					</c:url>
					|
					<a href='<c:out value="${consultScreenUrl}"/>'>Consultation Screen</a>
					<insta:screenlink screenId="update_mrd" addPipe="true" label="Codification" extraParam="?_method=getMRDUpdateScreen&patient_id=${consultationBean.map.patient_id}"/>
				</div>
			</form>
		</body>
</html>