<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<script>
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	var gConsultationId = '${ifn:cleanJavaScript(param.consultation_id)}';
	var latest_vital_reading_json = ${latest_vital_reading_json};
	var height_weight_params = ${height_weight_params};
	var gVitalReadingsExists = ${vital_reading_exists};
	var delete_vitals = ${roleId == 1 || roleId == 2 || actionRightsMap['delete_vitals'] eq 'A'};
	var edit_vitals = ${roleId == 1 || roleId == 2 || actionRightsMap['edit_vitals'] eq 'A'};
</script>
<div class="resultList">
<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" style="margin-top: 10px" id="vitalsTable" border="0" >
	<tr>
		<th>Date</th>
		<th>Time</th>
		<c:set var="noOfReadings" value="3"/>
		<c:forEach var="columnBean" items="${all_fields}">
			<c:set var="noOfReadings" value="${noOfReadings+1}"/>
			<th>${columnBean.map.param_label}<c:if test="${not empty columnBean.map.param_uom}"> (${columnBean.map.param_uom})</c:if>
			<c:if test="${paramType eq 'I/O' }">,Remarks</c:if></th>
		</c:forEach>
		<th style="width: 15px"></th>
		<th style="width: 15px"></th>
	</tr>
	<c:set var="numReadings" value="${fn:length(vital_readings)}"/>
	<c:set var="vitalReadingId" value="0"/>
	<c:forEach begin="1" end="${numReadings+1}" var="i" varStatus="loop">
		<c:set var="reading" value="${vital_readings[i-1]}"/>
		<c:set var="index" value="${i-1}"/>
		<fmt:formatDate pattern="dd-MM-yyyy" value="${reading.dateTime}" var="readingDate"/>
		<fmt:formatDate pattern="HH:mm" value="${reading.dateTime}" var="readingTime"/>
		<c:if test="${empty reading}">
			<c:set var="index" value=""/>
			<c:set var="style" value='style="display:none"'/>
		</c:if>
		<tr ${style}>
			<input type="hidden" name="h_vital_reading_id" value="${reading.vitalReadingId}"/>
			<input type="hidden" name="h_reading_date" value="${readingDate}"/>
			<input type="hidden" name="h_reading_time" value="${readingTime}"/>
			<input type="hidden" name="vital_edited" value="false"/>
			<input type="hidden" name="h_userName" value="${reading.userName}" />

			<td>
				<label>${readingDate}</label>
			</td>
			<td>${readingTime}</td>
			<c:forEach var="columnBean" items="${all_fields}">
				<td>
					<c:set var="paramValue" value=""/>
					<c:set var="prefColorCode" value=""/>
					<c:set var="paramRemarks" value=""/>
					<c:forEach var="values" items="${reading.readings}">
						<c:if test="${columnBean.map.param_id == values.paramId}">
							<c:set var="paramValue" value="${values.paramValue}"/>
							<c:set var="paramRemarks" value="${values.paramRemarks}"/>
							<c:set var="prefColorCode" value="${values.colorCode}"/>
							<c:set var="isColor" value="${paramType eq 'V' and (not empty prefColorCode ? (prefColorCode ne prefColorCodes.map.normal_color_code) : false)}"/>
							<label style="color: ${not empty prefColorCode ? (prefColorCode eq prefColorCodes.map.normal_color_code ? 'grey' : prefColorCode) : 'grey'}; font-weight: ${isColor ? 'bold' : ''}">
							${values.paramValue}<c:if test="${paramType eq 'I/O' and values.paramRemarks !=''}">,(<insta:truncLabel value="${values.paramRemarks}" length="20"/>)</c:if></label>
						</c:if>
					</c:forEach>
				</td>
				<c:if test="${not empty reading}">
					<input type="hidden" name="h_param_id${ifn:cleanHtmlAttribute(index)}" value="${columnBean.map.param_id}"/>
					<input type="hidden" name="h_param_value${ifn:cleanHtmlAttribute(index)}" value="${paramValue}">
					<input type="hidden" name="h_mandatory_in_tx${ifn:cleanHtmlAttribute(index)}" value="${columnBean.map.mandatory_in_tx}"/>
					<input type="hidden" name="h_param_label${ifn:cleanHtmlAttribute(index)}" value="${columnBean.map.param_label}"/>
					<input type="hidden" name="colorCode${ifn:cleanHtmlAttribute(index)}" id="${columnBean.map.param_id}${ifn:cleanHtmlAttribute(index)}colorCode" value="${not empty prefColorCode ? prefColorCode : prefColorCodes.map.normal_color_code}"/>
					<input type="hidden" name="isExpression${ifn:cleanHtmlAttribute(index)}" value="${not empty columBean.map.expr_for_calc_result}"/>
					<input type="hidden" name="h_param_remarks${ifn:cleanHtmlAttribute(index)}" value="${paramRemarks}"/>
				</c:if>
			</c:forEach>
			<c:choose>
				<c:when test="${roleId == 1 || roleId == 2 || actionRightsMap['delete_vitals'] eq 'A' || empty reading.vitalReadingId}">
					<td style="text-align: center">
						<input type="hidden" name="delVitalItem" id="delVitalItem" value="false" />
						<a href="javascript:Cancel Item" onclick="return cancelVitalItem(this);" title="Cancel Item" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
					</td>
				</c:when>
				<c:otherwise>
					<td style="text-align: center">
					<input type="hidden" name="delVitalItem" id="delVitalItem" value="false" />
						<a title="Cancel Item" >
							<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button" />
						</a>
					</td>
				</c:otherwise>
			</c:choose>
			<td style="text-align: center">
					<c:choose>
						<c:when test="${(roleId != 1 && roleId != 2 && actionRightsMap['edit_vitals'] eq 'N') || reading.vitalStatus == 'F'}">
							<a name="_editAnchor" href="javascript:Edit" onclick=""
								title="Edit Vital Details">
								<img src="${cpath}/icons/Edit1.png" class="button" />
							</a>
						</c:when>
						<c:otherwise>
							<a name="_editAnchor" href="javascript:Edit" onclick="return showEditVitalDialog(this);"
								title="Edit Vital Details">
								<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</c:otherwise>
					</c:choose>
			</td>
		</tr>
	</c:forEach>
</table>
<table width="100%" class="addButton">
	<tr>
		<td style="text-align: right">
			<c:choose>
				<c:when test="${paramType eq 'V'}">
					<c:choose>
						<c:when test="${screenId eq 'triage_form'}">
							<insta:screenlink screenId="vital_tri_form_audit_log" label="Vitals Audit Log" addPipe="false" target="_blank"
											  extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_vitals_audit_log_view&consultation_id=${param.consultation_id}
											  &mr_no=${patient.mr_no}&visit_id=${patient.patient_id}"/>
						</c:when>
						<c:when test="${screenId eq 'initial_assessment'}">
							<insta:screenlink screenId="vital_ia_form_audit_log" label="Vitals Audit Log" addPipe="false" target="_blank"
											  extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_vitals_audit_log_view&consultation_id=${param.consultation_id}
											  &mr_no=${patient.mr_no}&visit_id=${patient.patient_id}"/>
						</c:when>
						<c:when test="${screenId eq 'op_prescribe'}">
							<insta:screenlink screenId="vital_cons_form_audit_log" label="Vitals Audit Log" addPipe="false" target="_blank"
											  extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_vitals_audit_log_view&consultation_id=${param.consultation_id}
											  &mr_no=${patient.mr_no}"/>
						</c:when>
						<c:when test="${param.visitType eq 'i'}">
							<insta:screenlink screenId="vital_form_audit_log" label="Vitals Audit Log" addPipe="false" target="_blank"
											  extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_vitals_audit_log_view&consultation_id=${param.consultation_id}
											  &mr_no=${patient.mr_no}&visit_id=${patient.patient_id}"/>
						</c:when>
					</c:choose>
				</c:when>
				<c:when test="${screenId eq 'patient_generic_form_list' }">
					<insta:screenlink screenId="vital_gen_form_audit_log" label="Vitals Audit Log" addPipe="false" target="_blank"
									  extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_vitals_audit_log_view&consultation_id=${param.consultation_id}
									  &mr_no=${patient.mr_no}&visit_id=${patient.patient_id}"/>
				</c:when>
			</c:choose>
		</td>
		<td style="text-align: center; width: 15px">
			<c:choose>
				<c:when test="${(roleId != 1 && roleId != 2 && actionRightsMap['add_vitals'] eq 'N')}">
					<button type="button" name="btnAddItem" id="btnAddItem"
						title="Add Vitals (Alt_Shift_+)"
						onclick=""
						class="imgButton">
						<img src="${cpath}/icons/Add1.png">
					</button>
				</c:when>
				<c:otherwise>
					<button type="button" name="btnAddItem" id="btnAddItem"
						title="Add Vitals (Alt_Shift_+)"
						onclick="showAddVitalDialog(this); return false;" accesskey="+"
						class="imgButton">
						<img src="${cpath}/icons/Add.png">
					</button>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
</table>
</div>
<div id="addVitalDialog" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Add ${paramType eq 'V' ? 'Vital':'Intake/Output'} Reading</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Date </td>
					<td><insta:datewidget name="add_reading_date" id="add_reading_date" valid="past" value="today" btnPos="topleft"/>
					</td>
					<td>Time
						<input name="add_reading_time" id="add_reading_time" size="5"
							value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'
							onblur="setTime(this)" onchange="setVitalEdited();" class="number"
							maxlength="5" >
					</td>
				</tr>
			</table>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">${paramType eq 'V' ? 'Vital':'Intake/Output'} Parameters</legend>
			<table class="formtable">
				<tr>
					<td style="text-align: ${paramType eq 'V' ? 'center' : 'left' }"><b>Parameters</b></td>
					<td style="text-align: left;"><b>Values</b></td>
					<td><b>Remarks</b></td>
					<c:if test="${paramType eq 'V'}">
						<td style="text-align: left;"><b>Reference Ranges</b></td>
					</c:if>
				</tr>
				<c:forEach var="columnBean" items="${all_fields}">
					<c:set var="refBean" value=""/>
					<c:forEach var="refRanges" items="${referenceList}">
						<c:if test="${columnBean.map.param_id == refRanges.map.param_id}">
							<c:set var="refBean" value="${refRanges}"/>
						</c:if>
					</c:forEach>
					<tr>
						<td style="text-align: ${paramType eq 'V' ? 'center' : 'left' };">${columnBean.map.param_label}
							<c:if test="${not empty columnBean.map.param_uom}"> (${columnBean.map.param_uom})</c:if>
							<c:if test="${columnBean.map.mandatory_in_tx == 'Y'}">
								<span class="star">*</span>
							</c:if>
						</td>
						<td>
							<c:choose>
							<c:when test="${not empty refBean}">
								<input style="width: 100px; background-color: ${(paramType eq 'V' && not empty columnBean.map.expr_for_calc_result) ? prefColorCodes.map.normal_color_code : ''}" 
									type="text" name="add_param_value" id="edit${columnBean.map.param_id}" value="" maxlength="30"
									onchange="setSiviarity(this, '${refBean.map.min_normal_value}','${refBean.map.max_normal_value}',
													'${refBean.map.min_critical_value}','${refBean.map.max_critical_value}',
													'${refBean.map.min_improbable_value}','${refBean.map.max_improbable_value}');
													return validateSystemVitals(this, '${columnBean.map.system_vital}', '${columnBean.map.param_label}');"
													${(paramType eq 'V' && not empty columnBean.map.expr_for_calc_result) ? "readonly" : ""}  />
							</c:when>
							<c:otherwise>
								<input style="width: 100px;" type="text" name="add_param_value" id="${columnBean.map.param_id}" value="" maxlength="30"
									onchange="setSiviarity(this, '', '' , '', '', '', ''); return validateSystemVitals(this, '${columnBean.map.system_vital}', '${columnBean.map.param_label}');"/>
							</c:otherwise>
							</c:choose>
							<input type="hidden" name="add_param_id" value="${columnBean.map.param_id}"/>
							<input type="hidden" name="add_param_label" value="${columnBean.map.param_label}"/>
							<input type="hidden" name="add_mandatory_in_tx" value="${columnBean.map.mandatory_in_tx}"/>
						</td>
						<td><input type="text" name="add_param_remarks" value=""/></td>
						<c:if test="${paramType eq 'V'}">
							<td>
								<label name="reference_range_txt" >	<insta:truncLabel value="${empty refBean ? '' : refBean.map.reference_range_txt}" length="35"/></label>
							</td>
						</c:if>
						
					</tr>
				</c:forEach>
				</table>
				</fieldset>
				<table>
					<tr>
						<td align="left">Entered By:
							<c:set var="user" value="${userid}"/>
							<label>${isSharedLogIn == 'Y' ?'': ifn:cleanHtml(user)}</label>
							<input type="hidden" name="user_name" id="user_name" value="${ifn:cleanHtmlAttribute(userid)}"/>
						</td>
					</tr>
				</table>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" name="vital_add_Ok" id="vital_add_Ok" value="OK"/>
						<input type="button" name="vital_add_Close" id="vital_add_Close" value="Close"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
<div id="editVitalDialog" style="display: none">
	<input type="hidden" name="vitalEditRowId" value=""/>
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Edit ${paramType eq 'V' ? 'Vital':'Intake/Output'} Reading</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Date </td>
					<td><insta:datewidget name="edit_reading_date" id="edit_reading_date" valid="past" value="today" btnPos="topleft" extravalidation="setVitalEdited();"/></td>
					<td>Time
						<input name="edit_reading_time" id="edit_reading_time" size="5"
							value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'
							onblur="setTime(this)" onchange="setVitalEdited();" class="number"
							maxlength="5" >
					</td>
				</tr>
				</table>
				<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">${paramType eq 'V' ? 'Vital':'Intake/Output'} Parameters</legend>
				<table class="formtable">
				<tr>
					<td style="text-align: ${paramType eq 'V' ? 'center' : 'left' }"><b>Parameters</b></td>
					<td style="text-align: left;"><b>Values</b></td>
					<td><b>Remarks</b></td>
					<c:if test="${paramType eq 'V'}">
						<td style="text-align: left;"><b>Reference Ranges</b></td>
					</c:if>
				</tr>
				<c:forEach var="columnBean" items="${all_fields}">
				<c:set var="editRefBean" value=""/>
					<c:forEach var="refRanges" items="${referenceList}">
						<c:if test="${columnBean.map.param_id == refRanges.map.param_id}">
							<c:set var="editRefBean" value="${refRanges}"/>
						</c:if>
					</c:forEach>
						<tr>
							<td style="text-align: ${paramType eq 'V' ? 'center' : 'left' }">${columnBean.map.param_label}
								<c:if test="${not empty columnBean.map.param_uom}"> (${columnBean.map.param_uom})</c:if>
								<c:if test="${columnBean.map.mandatory_in_tx == 'Y'}">
									<span class="star">*</span>
								</c:if>
							</td>
							<td>
								<c:choose>
								<c:when test="${not empty editRefBean}">
									<input style="width: 100px;" type="text" name="edit_param_value" value="" id="${columnBean.map.param_id}" value="" maxlength="30"
										onchange="setValueModified(this);setVitalEdited();setSiviarity(this, '${editRefBean.map.min_normal_value}','${editRefBean.map.max_normal_value}',
														'${editRefBean.map.min_critical_value}','${editRefBean.map.max_critical_value}',
														'${editRefBean.map.min_improbable_value}','${editRefBean.map.max_improbable_value}');
														 return validateSystemVitals(this, '${columnBean.map.system_vital}', '${columnBean.map.param_label}');"
														${(paramType eq 'V' && not empty columnBean.map.expr_for_calc_result) ? "readonly" : ""}/>
								</c:when>
								<c:otherwise>
									<input style="width: 100px;" type="text" name="edit_param_value" value="" maxlength="30" id="${columnBean.map.param_id}" value=""
										onchange="setValueModified(this);setVitalEdited();setSiviarity(this, '', '' , '', '', '', ''); return validateSystemVitals(this, '${columnBean.map.system_vital}', '${columnBean.map.param_label}');"/>
								</c:otherwise>
								</c:choose>
							</td>
							<td>
								<input type="text" name="edit_param_remarks" value=""/>
								<input type="hidden" name="edit_param_label" value="${columnBean.map.param_label}"/>
								<input type="hidden" name="edit_param_value_modified" value="N"/>
								<input type="hidden" name="edit_param_id" value="${columnBean.map.param_id}"/>
								<input type="hidden" name="hidden_edit_reading_date" id="hidden_edit_reading_date" value=""/>
							</td>
							<c:if test="${paramType eq 'V'}">
								<td>
									<label name="reference_range_txt" >	<insta:truncLabel value="${empty editRefBean ? '' : editRefBean.map.reference_range_txt}" length="35"/></label>
								</td>
							</c:if>
						</tr>
				</c:forEach>
				</table>
				<table>
					<tr>
						<td class="formlabel">Entered By:</td>
						<td>
							<label id="ed_vital_entered_by_label"></label>
							<input type="hidden" name="ed_user_name" id="ed_user_name" />
						</td>
					</tr>
				</table>
			</fieldset>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" id="vital_edit_Ok" name="vital_edit_Ok" value="Ok"/>
						<input type="button" id="vital_edit_Cancel" name="vital_edit_Cancel" value="Cancel"/>
						<input type="button" id="vital_edit_Previous" name="vital_previous" value="<<Previous" />
						<input type="button" id="vital_edit_Next" name="vital_next" value="Next>>" />
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>