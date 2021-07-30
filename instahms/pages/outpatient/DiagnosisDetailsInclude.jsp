<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO"%>
<%
	JSONSerializer js = new JSONSerializer().exclude("class");
	List diagnosis_status = new DiagnosisStatusDAO().listAll();
	request.setAttribute("diagnosis_status_names", diagnosis_status);
	request.setAttribute("diagnosis_status_names_json", js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(diagnosis_status)));
	request.setAttribute("codes", js.serialize(request.getAttribute("favcodesList")));
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@page import="flexjson.JSONSerializer"%>
<%@page import="java.util.List"%>
<%@page import="com.insta.hms.master.DiagnosisStatus.DiagnosisStatusDAO"%>
<%@page import="com.insta.hms.common.ConversionUtils"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@page import="com.insta.hms.master.DoctorMaster.DoctorMasterDAO"%>
<%@page import="com.insta.hms.usermanager.UserDAO"%>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<%-- <c:set var="currentDate" value="${currentDate}"/> --%>
<c:set var="generic_prefs" value='<%= GenericPreferencesDAO.getAllPrefs() %>'/>
<c:set var="OspDiagnosisSectionMandatory" value='<%= RegistrationPreferencesDAO.getRegistrationPreferences().getDiagnosis_for_osp_registration() %>'/>
<c:set var="mod_mrd_icd" value="${preferences.modulesActivatedMap.mod_mrd_icd}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="diagnosisSectionMandatory" value='<%= new SystemGeneratedSectionsDAO().findByKey("section_id", -6).get("section_mandatory")%>' />
<script>
	var diagnosis_status_names_json = ${diagnosis_status_names_json};
	var mod_mrd_icd = '${mod_mrd_icd}';
	var defaultDiagnosisCodeType = '${ifn:cleanJavaScript(defaultDiagnosisCodeType)}';
	var diagnosis_detatils_form = '${ifn:cleanJavaScript(param.form_name)}'
	YAHOO.util.Event.onContentReady("content", initDiagnosisDetails);
	var searchType = '${not empty param.searchType ? param.searchType : 'visit'}';
	var visit_type = '${searchType == 'visit' ? 'patient.visit_type' : ''}';
	var op_type = '${searchType == 'visit' ? 'patient.op_type' : ''}';
	var diag_user_doctor_id = '${user_doctor_id}';
	var screenid = '${screenId}';
	var codeslist = '${codes}';
	var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
	var currentDate = new Date(<%= (new java.util.Date()).getTime() %>);
	var currentDateAndTime = '<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${currentDate}"/>';
	var diagnosisSectionMandatory = ${diagnosisSectionMandatory};
	var ospDiagnosisSectionMandatory = ${OspDiagnosisSectionMandatory == 'M'};
</script>
<insta:js-bundle prefix="patient.diagnosis"/>

<c:set var="titleKey1">
<insta:ltext key="patient.diagnosis.adddialog.headerkey"/>
</c:set>

<c:choose>
	<c:when test="${screenId == 'op_prescribe'}">
		<input type="hidden" id="diag_consulting_doctor_id" value="${consultation_bean.doctor_name}"/>
		<input type="hidden" id="diag_consulting_doctor_name" value="${consultation_bean.doctor_full_name}"/>
	</c:when>
	<c:otherwise>
		<input type="hidden" id="diag_consulting_doctor_id" value="${searchType == 'visit' ? patient.doctor  : '' }"/>
		<input type="hidden" id="diag_consulting_doctor_name" value="${searchType == 'visit'  ?  patient.doctor_name : '' }"/>
	</c:otherwise>
</c:choose>

<legend class="fieldSetLabel" style="margin-top: 10px"><insta:ltext key="patient.diagnosis.grid.header"/>
	<c:choose>
	    <c:when test="${screenId == 'out_pat_reg'}">
	         <c:if test="${OspDiagnosisSectionMandatory == 'M'}">
               <span class="star">*</span>
           </c:if>
      </c:when>
      <c:otherwise>
           <c:if test="${diagnosisSectionMandatory}">
           		 <span class="star">*</span>
           </c:if>
	    </c:otherwise>
	</c:choose>
</legend>
<table class="detailList dialog_displayColumns" id="diagnosisDetailsTable" style="margin-top: 8px">
	<tr>
		<th><insta:ltext key="patient.diagnosis.grid.entered_datetime"/>
		<th><insta:ltext key="patient.diagnosis.grid.doctor"/></th>
		<th><insta:ltext key="patient.diagnosis.grid.diagtype"/></th>
		<th><insta:ltext key="patient.diagnosis.grid.code_type"/></th>
		<th><insta:ltext key="patient.diagnosis.grid.code"/></th>
		<th><insta:ltext key="patient.diagnosis.grid.description"/></th>
		<th><insta:ltext key="patient.diagnosis.grid.status"/></th>
		<th><insta:ltext key="patient.diagnosis.grid.remarks"/></th>
		<th style="width: 16px;text-align: center"></th>
		<th style="width: 16px;text-align: center"></th>
	</tr>
	<c:set var="numDiagnosis" value="${fn:length(diagnosis_details)}"/>
	<c:forEach begin="1" end="${numDiagnosis+1}" var="i" varStatus="loop">
		<c:set var="diagnosis" value="${diagnosis_details[i-1].map}"/>
		<c:if test="${empty diagnosis}">
			<c:set var="style" value='style="display:none"'/>
		</c:if>
		<tr ${style}>
			<td>
				<img src="${cpath}/images/empty_flag.gif"/>
				<fmt:formatDate var="diagnosis_datetime" pattern="dd-MM-yyyy HH:mm" value="${diagnosis.diagnosis_datetime}"/>
				<label>${diagnosis_datetime}</label>
				<input type="hidden" name="diagnosis_id" value="${(!empty param.screen_id && param.screen_id == 'opconsultation') ? '_' : diagnosis.id}"/>
				<input type="hidden" name="diagnosis_code" value="${diagnosis.icd_code}"/>
				<input type="hidden" name="diagnosis_code_from_db" value="${diagnosis.icd_code}">
				<input type="hidden" name="diagnosis_type" value="${diagnosis.diag_type}" />
				<input type="hidden" name="diagnosis_type_from_db" value="${diagnosis.diag_type}" />
				<input type="hidden" name="diagnosis_description" value="${diagnosis.description}"/>
				<input type="hidden" name="diagnosis_year_of_onset" value="${diagnosis.year_of_onset}"/>
				<input type="hidden" name="diagnosis_status_name" value="${diagnosis.diagnosis_status_name}"/>
				<input type="hidden" name="diagnosis_status_id" value="${diagnosis.diagnosis_status_id}"/>
				<input type="hidden" name="diagnosis_remarks" value="${ifn:cleanHtmlAttribute(diagnosis.remarks)}"/>
				<input type="hidden" name="diagnosis_datetime" value="${diagnosis_datetime}"/>
				<input type="hidden" name="diagnosis_doctor_id" value="${diagnosis.doctor_id}"/>
				<input type="hidden" name="diagnosis_doctor_name" value="${diagnosis.doctor_name}"/>
				<input type="hidden" name="diagnosis_entered_by" value="${diagnosis.username}"/>
				<input type="hidden" name="diagnosis_code_type" value="${diagnosis.code_type}"/>
				<input type="hidden" name="is_year_of_onset_mandatory" value="${diagnosis.is_year_of_onset_mandatory}"/>
				<input type="hidden" name="health_authority" value="${diagnosis.health_authority}"/>
				<input type="hidden" name="diagnosis_deleted" value="false" />
				<input type="hidden" name="diagnosis_edited" value="false" />
				<c:set var="diag_fav" value="${fn:contains(favcodesList, diagnosis.icd_code)}" />
				<input type="hidden" name="diagnosis_favourite" id="diagnosis_favourite" value="${diag_fav eq true ? 'Y' :'N' }" />
				<input type="hidden" name="diagcodeortypeedited" value="false" />
			</td>
			<td><insta:truncLabel value="${diagnosis.doctor_name}" length="20"/></td>
			<td><label>${diagnosis.diag_type == 'P' ? 'Principal' : (diagnosis.diag_type == 'V' ? 'Reason For Visit' : 'Secondary')}</label></td>
			<td><insta:truncLabel value="${diagnosis.code_type}" length="10"/>
			<td><insta:truncLabel value="${diagnosis.icd_code}" length="20"/></td>
			<td><insta:truncLabel value="${diagnosis.description}" length="30"/></td>
			<td><insta:truncLabel value="${diagnosis.diagnosis_status_name}" length="20"/></td>
			<td><insta:truncLabel value="${diagnosis.remarks}" length="30"/></td>
			<td style="width: 16px; text-align: center">
			    <c:choose>
			    	<c:when test="${diagnosis.sent_for_approval == true }">
			    		<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button" />
			    	</c:when>
			    	<c:otherwise>
						<a href="javascript:Cancel Diagnosis" onclick="return cancelDiagnosis(this);" title="Cancel Diagnosis" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
			    	</c:otherwise>
			    </c:choose>
			</td>
			<td style="width: 16px; text-align: center">
				<c:choose>
					<c:when test="${diagnosis.sent_for_approval == true }">
						<img src="${cpath}/icons/Edit1.png" class="button" />
					</c:when>
					<c:otherwise>
						<a name="diagnosisEditAnchor" href="javascript:Edit Diagnosis Details" onclick="return showEditDignosisDialog(this);"
							title="Edit Diagnosis Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</c:forEach>
</table>
<table class="addButton" style="height: 25px;">
	<tr>
		<td colspan="${param.displayPrvsDiagnosisBtn == true ? 5 : 6}">&nbsp;</td>
		<c:choose>
			<c:when test="${screenId eq 'op_prescribe' }">
				<td style="text-align: right;"><insta:screenlink screenId="cons_diagnosis_audit_log" label="Diagnosis Audit Log" addPipe="false" target="_blank"
					extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_diagnosis_details_audit_log_view&mr_no=${patient.mr_no}"/></td>
			</c:when>
			<c:when test="${screenId eq 'visit_summary' }">
				<td style="text-align: right;"><insta:screenlink screenId="ipf_diagnosis_audit_log" label="Diagnosis Audit Log" addPipe="false" target="_blank"
					extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_diagnosis_details_audit_log_view&mr_no=${patient.mr_no}"/></td>
			</c:when>
			<c:when test="${screenId eq 'patient_generic_form_list' }">
				<td style="text-align: right;"><insta:screenlink screenId="genf_diagnosis_audit_log" label="Diagnosis Audit Log" addPipe="false" target="_blank"
					extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_diagnosis_details_audit_log_view&mr_no=${patient.mr_no}"/></td>
			</c:when>
			<c:when test="${screenId eq 'ot_record' }">
				<td style="text-align: right;"><insta:screenlink screenId="otrecord_diagnosis_audit_log" label="Diagnosis Audit Log" addPipe="false" target="_blank"
					extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_diagnosis_details_audit_log_view&mr_no=${patient.mr_no}"/></td>
			</c:when>
		</c:choose>
		<td style="width: 16px;text-align: center">
			<button type="button" name="btnAddDiagnosis" id="btnAddDiagnosis" title="${titleKey1}"
				onclick="showAddDiagnosisDialog(this); return false;"
				accessKey="D" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
		</td>
		<td style="width: 16px;text-align: center; display: ${param.displayPrvsDiagnosisBtn == true ? 'table-cell' : 'none'}">
			<button type="button" name="btnShowPreviousDiagnosis" id="btnShowPreviousDiagnosis" title="Show Previous Diagnosis Details"
				onclick="getPreviousDiagnosis(this, '${patient.mr_no}', '${consultation_bean.patient_id}'); return false;"
				class="imgButton"><img src="${cpath}/icons/Send.png"></button>
		</td>
	</tr>
</table>

<div id="addDiagnosisDialog" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.diagnosis.adddialog.header"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.adddialog.doctor"/>: </td>
					<td>
						<div id="d_diagnosis_doctor_ac">
							<input type="text" id="d_diagnosis_doctor" value=""/>
							<div id="d_diagnosis_doctor_container" class="scrolForContainer" style="width: 250px"></div>
							<input type="hidden" id="d_diagnosis_doctor_id" value="" />
						</div>
					</td>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.adddialog.diagtype"/>: </td>
					<td>
						<select class="dropdown" id="d_diagnosis_type">
							<option value="">-- Select --</option>
							<option value="P">Principal</option>
							<option value="S">Secondary</option>
							<option value="V">Reason For Visit</option>
						</select>
					</td>

				</tr>
				<tr>
					<td class="formlabel" valign="top" ><div style="padding-top: 5px"><insta:ltext key="patient.diagnosis.adddialog.code"/>: </div></td>
					<td colspan="3">
						<div id="d_diagnosis_ac" style="float:left;width: 445px">
							<input type="text" id="d_diagnosis_code"/>
							<div id="d_diagnosis_code_container" class="scrolForContainer" style="width: 445px"></div>
						</div><div style="float:left;padding-left:3px;"><span class="star">*</span></div>
						<div style="clear: both"/>
						<div id="favouritesDiv" style="margin-top: 5px; display: ${screenId == 'op_prescribe' || ((screenId == 'visit_summary' || screenId == 'ot_record') && not empty user_doctor_id) ? 'block' : 'none'}">
							<input type="checkbox" name="d_show_favourites" id="d_show_favourites" onchange="reInitializeCodeAc(true);"/>&nbsp;<insta:ltext key="patient.diagnosis.adddialog.show_favourite"/>
							<input type="checkbox" name="d_add_favourite" id="d_add_favourite" />&nbsp; <insta:ltext key="patient.diagnosis.adddialog.add_favourite" />
							<input type="hidden" name="d_add_favourite_checkbox" id="d_add_favourite_checkbox" />
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.adddialog.description"/>: </td>
					<td class="forminfo" colspan="3">
						<label id="d_diagnosis_description_label"></label>
						<textarea id="d_diagnosis_description" style="display: none" rows="2" cols="50"></textarea>
						<input type="hidden" id="d_diagnosis_description_hidden" value=""/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="ui.label.year.of.onset"/>: </td>
					<td class="forminfo" colspan="3">
						<label id="d_diagnosis_year_of_onset_label"></label>
						<input type="number" id="d_diagnosis_year_of_onset" style="width: 70px" maxlength="4"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel" id="d_diag_usr_label_td"><insta:ltext key="patient.diagnosis.adddialog.enteredby"/>: </td>
					<td class="forminfo" id="d_diag_usr_value_td">
						${ifn:cleanHtml(userid)}
						<input type="hidden" id="d_diagnosis_entered_by" value="${ifn:cleanHtmlAttribute(userid)}"/>
					</td>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.adddialog.diagnosis_datetime"/>: </td>
					<td>
						<insta:datewidget id="d_diagnosis_date" name="d_diagnosis_date"/>
						<input type="text" class="timefield" id="d_diagnosis_time"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.adddialog.status"/>: </td>
					<td>
						<select class="dropdown" id="d_diagnosis_status_name">
							<option value="">-- Select --</option>
							<c:forEach items="${diagnosis_status_names}" var="diagnosis_status">
								<c:if test="${diagnosis_status.map.status == 'A'}">
									<option value="${diagnosis_status.map.diagnosis_status_id}">${diagnosis_status.map.diagnosis_status_name}</option>
								</c:if>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.adddialog.remarks"/>: </td>
					<td colspan="3">
						<textarea id="d_diagnosis_remarks" rows="2" cols="50"></textarea>
					</td>
				</tr>
			</table>
			<table style="margin-top: 10">
				<tr>
					<td>
						<button type="button" id="d_diagnosis_add_btn" >
							<insta:ltext key="patient.diagnosis.adddialog.btn.add"/>
						</button>
						<c:set var="cancelBtn">
							<insta:ltext key="patient.diagnosis.adddialog.btn.cancel"/>
						</c:set>
						&nbsp;<input type="button" id="d_diagnosis_cancel_btn" value="${cancelBtn}"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
<div id="editDiagnosisDialog" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.diagnosis.editdialog.header"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.editdialog.doctor"/></td>
					<td>
						<div id="ed_diagnosis_doctor_ac">
							<input type="text" id="ed_diagnosis_doctor" value="" onchange="setDiagnosisEdited();"/>
							<div id="ed_diagnosis_doctor_container" class="scrolForContainer" style="width: 250px"></div>
							<input type="hidden" id="ed_diagnosis_doctor_id" value=""/>
						</div>
					</td>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.editdialog.diagtype"/>: </td>
					<td class="forminfo">
						<input type="hidden" id="diagnosisEditRowId" value=""/>
						<input type="hidden" id="ed_diagnosis_code_type" value=""/>
						<select class="dropdown" id="ed_diagnosis_type" onchange="setDiagnosisEdited();">
							<option value="">-- Select --</option>
							<option value="P">Principal</option>
							<option value="S">Secondary</option>
							<option value="V">Reason For Visit</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel" valign="top"><div style="padding-top: 5px"><insta:ltext key="patient.diagnosis.editdialog.code"/>: <div></td>
					<td colspan="3">
						<div id="ed_diagnosis_ac" style="float:left;width: 445px">
							<input type="text" id="ed_diagnosis_code" onchange="setDiagnosisEdited()"/>
							<div id="ed_diagnosis_code_container" class="scrolForContainer" style="width: 445px"></div>
						</div><div style="float:left;padding-left:3px;"><span class="star">*</span></div>
						<div style="clear: both"/>
						<div id="favouritesDiv" style="margin-top: 5px; display: ${screenId == 'op_prescribe' || ((screenId == 'visit_summary' || screenId == 'ot_record') && not empty user_doctor_id) ? 'block' : 'none'}">
							<input type="checkbox" name="ed_show_favourites" id="ed_show_favourites" onchange="reInitializeCodeAc(false);"/>&nbsp;<insta:ltext key="patient.diagnosis.adddialog.show_favourite" />
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.editdialog.description"/>:</td>
					<td class="forminfo" colspan="3">
						<label id="ed_diagnosis_description_label"></label>
						<textarea id="ed_diagnosis_description" style="display: none" rows="2" cols="50" onchange="setDiagnosisEdited()"></textarea>
						<input type="hidden" id="ed_diagnosis_description_hidden" value="" />
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="ui.label.year.of.onset" />:</td>
					<td class="forminfo" colspan="3">
						<label id="ed_diagnosis_year_of_onset_label"></label>
						<input type="number" name="ed_diagnosis_year_of_onset" id="ed_diagnosis_year_of_onset" style="width: 70px" maxlength="4" onchange="setDiagnosisEdited()" />
					</td>
				</tr>
				<tr>
					<td class="formlabel" id="ed_diag_usr_label_td"><insta:ltext key="patient.diagnosis.editdialog.enteredby"/>: </td>
					<td class="forminfo" id="ed_diag_usr_value_td"><label id="ed_entered_by_label"></label></td>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.editdialog.diagnosis_datetime"/>: </td>
					<td>
						<insta:datewidget id="ed_diagnosis_date" name="ed_diagnosis_date" extravalidation="setDiagnosisEdited();"/>
						<input type="text" class="timefield" id="ed_diagnosis_time" onchange="setDiagnosisEdited();"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.editdialog.status"/>: </td>
					<td>
						<select class="dropdown" id="ed_diagnosis_status_name" onchange="setDiagnosisEdited()">
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.diagnosis.editdialog.remarks"/>: </td>
					<td colspan="3">
						<textarea id="ed_diagnosis_remarks" rows="2" cols="50" onchange="setDiagnosisEdited()"></textarea>
					</td>
				</tr>
			</table>
			<table style="margin-top: 10">
				<tr>
					<td>
						<button type="button" id="ed_diagnosis_ok_btn" >
							<insta:ltext key="patient.diagnosis.editdialog.btn.ok"/>
						</button>
						<c:set var="ed_cancel_btn">
							<insta:ltext key="patient.diagnosis.editdialog.btn.cancel"/>
						</c:set>
						<c:set var="ed_previous_btn">
							<insta:ltext key="patient.diagnosis.editdialog.btn.previous"/>
						</c:set>
						<c:set var="ed_next_btn">
							<insta:ltext key="patient.diagnosis.editdialog.btn.next"/>
						</c:set>
						<input type="button" id="ed_diagnosis_cancel_btn" value="${ed_cancel_btn}"/>
						<input type="button" id="ed_diagnosis_previous_btn" value="${ed_previous_btn}" />
						<input type="button" id="edit_diagnosis_next_btn" value="${ed_next_btn}"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>

<div id="previousDiagnosisDetailsDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.diagnosis.prvsdialog.header"/></legend>
			<div style="margin-top: 10px">
				<div style="float: left">
					<insta:ltext key="ui.label.mrno"/> : <label id="mrNoLabel" style="font-weight: bold;"></label>
				</div>
				<div id="diagnosisprogressbar" style="float: left; margin-left: 15px; font-weight: bold">
					<insta:ltext key="patient.diagnosis.grid.loading"/>
				</div>
				<div style="float:right; margin-right: 10px" id="diagnosisPaginationDiv" name="paginationDiv">
				</div>
			</div>
			<div style="clear:both"></div>
			<div class="resultList" style="width: 850px; margin-top: 10px" >
				<table class="resultList" id="previousDiagnosisTable" cellspacing="0" cellpadding="0" style="margin: top: 10px;width: 850px">
					<tr>
					    <th>Select</th>
						<th><insta:ltext key="patient.diagnosis.grid.patientid"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.diagnosis_datetime"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.doctor"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.diagtype"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.code_type"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.code"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.description"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.status"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.entered_by"/></th>
						<th><insta:ltext key="patient.diagnosis.grid.remarks"/></th>
					</tr>
					<tr style="display:none">
					    <td>
					    	<label></label>
					    	<input type="hidden" name="prev_diagnosis_id"/>
					    	<input type="hidden" name="prev_diagnosis_type"/>
					    	<input type="hidden" name="prev_diagnosis_code"/>
					    	<input type="hidden" name="prev_diagnosis_description"/>
					    	<input type="hidden" name="prev_diagnosis_status_name"/>
					    	<input type="hidden" name="prev_diagnosis_remarks"/>
					    	<input type="hidden" name="prev_diagnosis_datetime"/>
					    	<input type="hidden" name="prev_diagnosis_doctor_id"/>
					    	<input type="hidden" name="prev_diagnosis_doctor_name"/>
					    	<input type="hidden" name="prev_diagnosis_status_id"/>
							<input type="hidden" name="prev_diagnosis_code_type"/>
							<input type="hidden" name="prev_year_of_onset"/>
							<input type="hidden" name="prev_is_year_of_onset_mandatory"/>
							<input type="hidden" name="prev_health_authority"/>
					    </td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label class="wrapword"></label></td>
					</tr>
					<tr style="display: none; background-color:#FFC">
						<td colspan="7"><img src="${cpath}/images/alert.png"/> <insta:ltext key="patient.diagnosis.grid.noprevioushistory"/>.</td>
					</tr>
				</table>
			</div>
		</fieldset>
		<table >
			<tr>
			    <td><input type="button" id="click_Ok" name="click_Ok" value="Ok"></td>
				<td><input type="button" name="previousDiagnosis_btn" id="previousDiagnosis_btn" value="Close"/></td>
			</tr>
		</table>
	</div>
</div>
