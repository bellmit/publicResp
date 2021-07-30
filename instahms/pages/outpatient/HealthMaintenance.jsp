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
	YAHOO.util.Event.onContentReady("content", initHealthMaint);
	var health_maintenance_form = '${ifn:cleanJavaScript(param.form_name)}'
	var healthMaints_doctors_json = ${doctors_json};
	var health_user_doctor_id = '${user_doctor_id}';
</script>
<c:if test="${patient.visit_type == 'o'}">
	<input type="hidden" id="health_consulting_doctor_id" value="${consultation_bean.doctor_name}"/>
	<input type="hidden" id="health_consulting_doctor_name" value="${consultation_bean.doctor_full_name}"/>
</c:if>
<div class="resultList">
	<legend class="fieldSetLabel" style="margin-top: 10px">Health Maintenance
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
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="healthMaintsTable" border="0" width="100%" style="margin-top: 8px">
		<tr>
			<th style="width: 70px">Date</th>
			<th>Activity</th>
			<th>Doctor</th>
			<th>Due By</th>
			<th>Remarks</th>
			<th>Status</th>
			<th style="width: 16px;"></th>
			<th style="width: 16px"></th>
		</tr>
		<c:set var="numHealthMaints" value="${fn:length(healthMaints)}"/>
		<c:forEach begin="1" end="${numHealthMaints+1}" var="i" varStatus="loop">
			<c:set var="healthMaint" value="${healthMaints[i-1].map}"/>
			<c:if test="${empty healthMaint}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<tr ${style}>
				<td>
					<fmt:formatDate var="healthMaint_datetime" pattern="dd-MM-yyyy HH:mm" value="${healthMaint.recorded_date}"/>
					<input type="hidden" name="health_maint_id" value="${healthMaint.health_maint_id}"/>
					<input type="hidden" name="recorded_date" value="${healthMaint_datetime}"/>
					<input type="hidden" name="healthMain_doctor_id" value="${healthMaint.doctor_id}"/>
					<input type="hidden" name="healthMain_doctor_name" value="${healthMaint.doctor_name}"/>
					<input type="hidden" name="mr_no" value="${healthMaint.mr_no}"/>
					<input type="hidden" name="due_by" value="<c:out value='${healthMaint.due_by}'/>" />
					<input type="hidden" name="activity" value="<c:out value='${healthMaint.activity}'/>" />
					<input type="hidden" name="healthMain_remarks" value="<c:out value='${healthMaint.remarks}'/> "/>
					<input type="hidden" name="healthMain_status" value="${healthMaint.status}"/>
					<input type="hidden" name="delHealthMaint" id="delHealthMaint" value="false" />
					<input type="hidden" name="healthMaint_edited" value='false'/>
					<label>${healthMaint_datetime}</label>
				</td>
				<td>
					<insta:truncLabel value="${healthMaint.activity}" length="40"/>
				</td>
				<td>
					<insta:truncLabel value="${healthMaint.doctor_name}" length="20"/>
				</td>
				<td>
					<insta:truncLabel value="${healthMaint.due_by}" length="15"/>
				</td>
				<td>
					<insta:truncLabel value="${healthMaint.remarks}" length="40"/>
				</td>
				<td>
					<c:choose>
						<c:when test="${healthMaint.status == 'D'}"><label>Due</label></c:when>
						<c:when test="${healthMaint.status == 'C'}"><label>Completed</label></c:when>
						<c:when test="${healthMaint.status == 'X'}"><label>Cancelled</label></c:when>
						<c:when test="${healthMaint.status == 'O'}"><label>Other</label></c:when>
					</c:choose>
				</td>
				<c:if test="${stn_right_access}">
					<td style="text-align: center">
						<a href="javascript:Cancel Health Maintenance" onclick="return cancelHealthMaint(this);" title="Cancel Health Maintenance" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
					</td>
					<td style="text-align: center">
						<a name="_editHealthMaintAnchor" href="javascript:Edit Health Maint" onclick="return showEditHealthMaintDialog(this);"
							title="Edit Health Maintenance Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
	<table class="addButton" style="width:100%; white-space:nowrap;">
		<tr>
			<td style="width:100%;"></td>
			<c:if test="${stn_right_access}">
				<td>
					<button type="button" name="btnAddHealthMaint" id="btnAddHealthMaint" title="Add Health Maintenance"
						onclick="showAddHealthMaintDialog(this); return false;"
						class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</c:if>
			<td style="padding-right:5px;">Finalize</td>
			<td> <span><input class="finalize" id="${param.section_id}_finalized"  type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${param.section_id});"  
																								${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																								((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) ? '' : 'disabled')}></input>
																								<input type="hidden" value="${finalized}" name="${param.section_id}_finalized" ></span> </td>
		</tr>
	</table>
</div>

<div id="addHealthMaintDialog" style="display: none">
	<div class="bd">
		<div id="addHealthMaintDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Health Maintenance</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Activity<span style="color:  #FF2C00;"> *</span>: </td>
						<input type="hidden" name="d_health_maint_id" id="d_health_maint_id" value=""/>
						<td colspan="3"><input type="text" name="d_activity" id="d_activity" value="" maxlength="1000" style="width: 440px"></td>
					</tr>
					<tr>
						<td class="formlabel">Doctor<span style="color:  #FF2C00;"> *</span>: </td>
						<td>
							<div id="d_healthMaint_doctor_ac" >
								<input type="text" id="d_healthMaint_doctor" value=""/>
								<div id="d_healthMaint_doctor_container" class="scrolForContainer" style="width: 250px"></div>
								<input type="hidden" id="d_healthMaint_doctor_id" value="" />
							</div>
						</td>
						<td class="formlabel">Status: </td>
						<td>
							<select class="dropdown" id="d_healthMain_status">
								<option value="">-- Select --</option>
								<option value="D">Due</option>
								<option value="C">Completed</option>
								<option value="X">Cancelled</option>
								<option value="O">Other</option>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel" >Date: </td>
						<td>
							<insta:datewidget id="d_recorded_date" name="d_recorded_date"/>
							<input type="text" class="timefield" id="d_recorded_time"/>
						</td>
						<td class="formlabel" >Due By : </td>
						<td><input type="text" name="d_due_by" id="d_due_by" value="" maxlength="50"></td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="3">
							<textarea name="d_healthMain_remarks" id="d_healthMain_remarks" rows="2" cols="59" ></textarea>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="Add_HealthMaint_bt" id="Add_HealthMaint_bt" accesskey="A" >
						<b><u>A</u></b>dd
					</button>
					<input type="button" name="Close_HealthMaint_bt" value="Close" id="Close_HealthMaint_bt"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editHealthMaintDialog" style="display: none">
	<input type="hidden" name="editHealthMaintRowId" id="editHealthMaintRowId" value=""/>
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Edit Health Maintenance</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Activity<span style="color:  #FF2C00;"> *</span>: </td>
					<input type="hidden" name="ed_health_maint_id" id="ed_health_maint_id" value=""/>
					<td colspan="3">
						<input type="text" name="ed_activity" id="ed_activity" value="" maxlength="1000" style="width: 440px" onchange="setHealthMaintEdited();">
					</td>
				</tr>
				<tr>
					<td class="formlabel">Doctor<span style="color:  #FF2C00;"> *</span>: </td>
					<td>
						<div id="ed_healthMaint_doctor_ac">
							<input type="text" id="ed_healthMaint_doctor" value="" onchange="setHealthMaintEdited();" />
							<div id="ed_healthMaint_doctor_container" class="scrolForContainer" style="width: 250px"></div>
							<input type="hidden" id="ed_healthMaint_doctor_id" value="" />
						</div>
					</td>
					<td class="formlabel">Status: </td>
					<td>
						<select class="dropdown" id="ed_healthMain_status" onchange="setHealthMaintEdited();" >
							<option value="">-- Select --</option>
							<option value="D">Due</option>
							<option value="C">Completed</option>
							<option value="X">Cancelled</option>
							<option value="O">Other</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel" >Date: </td>
					<td>
						<insta:datewidget id="ed_recorded_date" name="ed_recorded_date" extravalidation="setHealthMaintEdited();"/>
						<input type="text" class="timefield" id="ed_recorded_time" onchange="setHealthMaintEdited();"/>
					</td>
					<td class="formlabel" >Due By : </td>
					<td>
						<input type="text" name="ed_due_by" id="ed_due_by" value="" maxlength="50" onchange="setHealthMaintEdited();">
					</td>
				</tr>
				<tr>
					<td class="formlabel">Remarks: </td>
					<td colspan="3">
						<textarea name="ed_healthMain_remarks" id="ed_healthMain_remarks" rows="2" cols="59" onchange="setHealthMaintEdited();" ></textarea>
					</td>
				</tr>
			</table>
			<table style="margin-top: 10">
				<tr>
					<td>
						<input type="button" id="edit_HealthMaint_Ok" name="editok" value="Ok">
						<input type="button" id="edit_HealthMaint_Cancel" name="cancel" value="Cancel" />
						<input type="button" id="edit_HealthMaint_Previous" name="previous" value="<<Previous" />
						<input type="button" id="edit_HealthMaint_Next" name="next" value="Next>>"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
