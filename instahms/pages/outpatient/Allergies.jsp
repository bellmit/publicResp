<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<fmt:parseNumber var="sectionId" type="number" value="${param.section_id}" />
<c:set var="finalized" value="${not empty section_finalize_status ? section_finalize_status[(sectionId).intValue()].finalized : 'N'}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stn_right_access"
	value="${((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) && finalized != 'Y')}"/>
<script>
	YAHOO.util.Event.onContentReady("content", initAllergies);
	var allergy_detatils_form = '${ifn:cleanJavaScript(param.form_name)}'
	var allergyTypes ='${ifn:convertToDeepSerializedJSON(allergy_types)}';
	var allowFreeTextEntries = "${allowFreeTextInAllergies}";
</script>

	<legend class="fieldSetLabel" style="margin-top: 8px">Allergies
		<c:forEach var="stn" items="${sys_generated_section}">
			<c:if test="${stn.section_mandatory && stn.section_id == param.section_id && stn_right_access}">
				<span class="star">*</span>
			</c:if>
		</c:forEach>
		<c:if test="${!(roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id))}">
			<i>[Read-Only]</i>
		</c:if>
		<c:if test="${finalized == 'Y'}">
			<i>[Finalized]</i>
		</c:if>
	</legend>
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="allergiesTable" border="0" style="margin-top: 8px">
		<tr>
			<th>Type</th>
			<th>Allergy</th>
			<th>Onset</th>
			<th>Reaction</th>
			<th>Severity</th>
			<th>Status</th>
			<th style="width: 16px;"></th>
			<th style="width: 16px"></th>
		</tr>
		<c:set var="numallergies" value="${fn:length(allergies)}"/>
		<c:forEach begin="1" end="${numallergies+1}" var="i" varStatus="loop">
			<c:set var="allergy" value="${allergies[i-1].map}"/>
			<c:if test="${empty allergy}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<tr ${style}>
				<td>
					<input type="hidden" name="allergy_id" value="${allergy.allergy_id}"/>
					<input type="hidden" name="allergy_type_code" value="${allergy.allergy_type}"/>
					<input type="hidden" name="allergy_type" value="${allergy.allergy_type_id}"/>
					<input type="hidden" name="allergen_code_id" value="${allergy.allergen_code_id}"/>
					<input type="hidden" name="generic_code" value="${allergy.generic_code}"/>
					<input type="hidden" name="mr_no" value="${allergy.mr_no}"/>
					<input type="hidden" name="allergy" value="<c:out value='${allergy.allergy}'/>" />
					<input type="hidden" name="reaction" value="<c:out value='${allergy.reaction}'/>"/>
					<input type="hidden" name="onset_date" value="<c:out value='${allergy.onset_date}'/>"/>
					<input type="hidden" name="status" value="${allergy.status}"/>
					<input type="hidden" name="severity" value="${allergy.severity}"/>
					<input type="hidden" name="delAllergy" id="delAllergy" value="false" />
					<input type="hidden" name="Allergy_edited" value='false'/>
					
					<c:choose>
					<c:when test="${not empty allergy.allergy_type_id}">
					<c:forEach var="allergytype" items="${allergy_types}" >
					<fmt:parseNumber var="allergyTypeId" value="${allergytype.allergy_type_id}" integerOnly="true"/>
					<fmt:parseNumber var="selectedAllergyTypeId" value="${allergy.allergy_type_id}" integerOnly="true"/>
					<c:if test="${allergyTypeId eq selectedAllergyTypeId}">
					<label>${allergytype.allergy_type_name}</label>
					</c:if>
					</c:forEach>
					</c:when>
					<c:otherwise>
					<label>No Known Allergies</label>
					</c:otherwise>
					</c:choose>
					
				</td>
				<td>
					<insta:truncLabel value="${allergy.allergy}" length="50"/>
				</td>
				<td>
					<insta:truncLabel value="${allergy.onset_date}" length="15"/>
				</td>
				<td>
					<insta:truncLabel value="${allergy.reaction}" length="100"/>
				</td>
				<td>
					<c:choose>
						<c:when test="${allergy.severity == 'Mild'}"><label>Mild</label></c:when>
						<c:when test="${allergy.severity == 'Moderate'}"><label>Moderate</label></c:when>
						<c:when test="${allergy.severity == 'Severe'}"><label>Severe</label></c:when>
						<c:when test="${allergy.severity == 'Unknown'}"><label>Unknown</label></c:when>
						<c:otherwise><label></label></c:otherwise>
					</c:choose>
				</td>
				<td>
					<label>${allergy.status == 'A' ? 'Active' : 'Inactive'}</label>
				</td>
				
				<c:if test="${stn_right_access}">
					<td style="text-align: center">
						<a href="javascript:Cancel Allergy" onclick="return cancelAllergy(this);" title="Cancel Allergy" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
					</td>
					<td style="text-align: center">
						<a name="_editAllergyAnchor" href="javascript:Edit Allergy" onclick="return showEditAllergyDialog(this);"
							title="Edit Allergy Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
	<table class="addButton" style="width:100%;white-space:nowrap;">
		<tr>
			<td style="width:100%;"></td>
			<c:choose>
				<c:when test="${screenId eq 'triage_form' }">
					<td><insta:screenlink screenId="triage_allergy_audit_log" label="Allergy Audit Log" addPipe="false" target="_blank"
						extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_allergies_audit_log_triage_view&mr_no=${patient.mr_no}&section_item_id=${param.consultation_id}&item_type=CONS"/></td>
				</c:when>

				<c:when test="${screenId eq 'op_prescribe' }">
					<td><insta:screenlink screenId="cons_allergy_audit_log" label="Allergy Audit Log" addPipe="false" target="_blank"
						extraParam="?_method=getAuditLogDetails&al_table=patient_allergies_audit_log_cons_view&mr_no=${patient.mr_no}&patient_id=${patient.patient_id}&section_item_id=${param.consultation_id}&item_type=CONS"/></td>
				</c:when>

				<c:when test="${screenId eq 'visit_summary' }">
					<td><insta:screenlink screenId="ipf_allergy_audit_log" label="Allergy Audit Log" addPipe="false" target="_blank"
					extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_allergies_audit_log_ipf_view&mr_no=${patient.mr_no}&section_item_id=&item_type="/></td>
				</c:when>

				<c:when test="${screenId eq 'patient_generic_form_list' }">
					<c:choose>
						<c:when test="${param.generic_form_id == '' || param.generic_form_id == null}">
							<td><insta:screenlink screenId="genf_allergy_audit_log" label="Allergies Audit Log" addPipe="false" target="_blank"
							extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_allergies_audit_log_genf_view&mr_no=${patient.mr_no}&section_item_id=&item_type=GEN&generic_form_id=0"/>
						</c:when>
						<c:otherwise>
							<td><insta:screenlink screenId="genf_allergy_audit_log" label="Allergies Audit Log" addPipe="false" target="_blank"
							extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=patient_allergies_audit_log_genf_view&mr_no=${patient.mr_no}&section_item_id=&item_type=GEN&generic_form_id=${param.generic_form_id}"/></td>
						</c:otherwise>
					</c:choose>
				</c:when>
			</c:choose>
			<c:if test="${stn_right_access}">
				<td>
					<button type="button" name="btnAddAllergy" id="btnAddAllergy" title="Add Allergy"
						onclick="showAddAllergyDialog(this); return false;"
						class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</c:if>
			<td style="padding-right:5px;">Finalize</td>
			<td> <span><input class="finalize" id="${param.section_id}_finalized" type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${param.section_id});"  
																								${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																								((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) ? '' : 'disabled')}></input>
																								<input type="hidden" value="${finalized}" name="${param.section_id}_finalized" ></span> </td>
		</tr>
	</table>

<div id="addAllergyDialog" style="display: none">
	<div class="bd">
		<div id="addAllergyDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Allergy</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Allergy Type:</td>
						<td><insta:selectdb name="d_allergy_type" id="d_allergy_type"
								onchange="handleAllergyTypeChangeOnAdd();"
								table="allergy_type_master" displaycol="allergy_type_name"
								valuecol="allergy_type_id" filtercol="status" filtervalue="A"
								orderby="allergy_type_id" dummyvalue="No Known Allergies"
								dummyvalueId="" style="width: 138px;" /></td>
						<td class="formlabel">Status:</td>
						<td><select class="dropdown" id="d_status">
								<option value="A">Active</option>
								<option value="I">InActive</option>
						</select></td>
					</tr>
					<tr>
						<td class="formlabel">Onset:</td>
						<td><input type="text" name="d_onset_date" id="d_onset_date"
							value="" maxlength="50"></td>
						<td class="formlabel">Severity:</td>
						<td><select class="dropdown" id="d_severity"
							onchange="setAllergiesEdited();">
								<option value="">-- Select --</option>
								<option value="Mild">Mild</option>
								<option value="Moderate">Moderate</option>
								<option value="Severe">Severe</option>
								<option value="Unknown">Unknown</option>
						</select></td>
					</tr>
					<tr>
						<td class="formlabel">Allergy:</td>
						<td colspan="3">
							<div id="allergy_wrapper" class="autocomplete">
								<input type="text" name="d_allergy" id="d_allergy" class="field"
									style="width: 440px" />
								<div id="allergy_dropdown" style="width: 440px"></div></div>
								<input type="hidden" name="d_allergy_id" id="d_allergy_id" value="" />
								<input type="hidden" name="d_allergen_code_id" id="d_allergen_code_id" value="" />
								<input type="hidden" name="d_generic_code" id="d_generic_code" value="" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Reaction:</td>
						<td colspan="3"><textarea name="d_reaction" id="d_reaction"
								rows="3" cols="59"></textarea></td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="Add_bt" id="Add_bt" accesskey="A">
						<b><u>A</u></b>dd
					</button> <input type="button" name="Close_bt" value="Close" id="Close_bt" />
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editAllergyDialog" style="display: none">
	<input type="hidden" name="editAllergyRowId" id="editAllergyRowId" value=""/>
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Edit Allergy</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Allergy Type:</td>
					<td><insta:selectdb name="ed_allergy_type"
							id="ed_allergy_type" onchange="setAllergiesEdited(); handleAllergyTypeChangeOnEdit();"
							table="allergy_type_master" displaycol="allergy_type_name"
							valuecol="allergy_type_id" filtercol="status" filtervalue="A"
							orderby="allergy_type_id" dummyvalue="No Known Allergies"
							dummyvalueId="" style="width: 138px;" />
				    </td>
					<td class="formlabel">Status:</td>
					<td><select class="dropdown" id="ed_status"
						onchange="setAllergiesEdited();">
							<option value="A" selected="selected">Active</option>
							<option value="I">InActive</option>
					</select></td>
				</tr>
				<tr>
					<td class="formlabel" >OnSet: </td>
					<td><input type="text" name="ed_onset_date" id="ed_onset_date" value="" maxlength="50" onchange="setAllergiesEdited();" ></td>
					<td class="formlabel">Severity: </td>
						<td>
							<select class="dropdown" id="ed_severity" onchange="setAllergiesEdited();">
								<option value="">-- Select --</option>
								<option value="Mild">Mild</option>
								<option value="Moderate">Moderate</option>
								<option value="Severe">Severe</option>
								<option value="Unknown">Unknown</option>
							</select>
						</td>
				</tr>
				<tr>
					<td class="formlabel">Allergy: </td>
						<td colspan="3">
                                        <div id="ed_allergy_wrapper" class="autocomplete">
                                            <input type="text" name="ed_allergy" id="ed_allergy"
                                                class="field" style="width: 440px" onchange="setAllergiesEdited();"/>
                                            
                                        <div id="ed_allergy_dropdown" style="width: 440px"></div>
                                        <input type="hidden" name="ed_allergy_id" id="ed_allergy_id" value=""/>
                                        <input type="hidden" name="ed_allergen_code_id" id="ed_allergen_code_id" value=""/>
                                        <input type="hidden" name="ed_generic_code" id="ed_generic_code" value=""/>
                                        </div>
                                    </td>
				</tr>
				<tr>
					<td class="formlabel">Reaction: </td>
					<td colspan="3"><textarea name="ed_reaction" id="ed_reaction" rows="3" cols="59" onchange="setAllergiesEdited();" ></textarea></td>
				</tr>
			</table>
			<table style="margin-top: 10">
				<tr>
					<td>
						<input type="button" id="edit_allergy_Ok" name="editok" value="Ok">
						<input type="button" id="edit_allergy_Cancel" name="cancel" value="Cancel" />
						<input type="button" id="edit_allergy_Previous" name="previous" value="<<Previous" />
						<input type="button" id="edit_allergy_Next" name="next" value="Next>>"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
