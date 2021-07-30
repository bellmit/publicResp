<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>
<script>
	var prescriptionForm = '${ifn:cleanJavaScript(param.form_name)}';
	var screenId = '${ifn:cleanJavaScript(param.screen_id)}';
</script>
<c:if test="${param.screen_id == 'doctororder'}">
	<fieldset class="fieldSetBorder">
</c:if>
<c:if test="${param.screen_id == 'addnewadmissionrequest'}">
	<legend class="fieldSetLabel" style="margin-top: 10px">Admission Orders</legend>
	<div style="height: 10px;">&nbsp;</div>
</c:if>
	<div style="float: right;display:${param.screen_id == 'addnewadmissionrequest' ? 'none' : ''}">
		<input type="checkbox" name="hide_discontinued" id="hide_discontinued" checked onclick="hideDiscontinued();"/> Hide Discontinued
	</div>
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="prescDetails" border="0" width="100%">
		<tr>
			<th>Type</th>
			<th>Name</th>
			<th>Dosage</th>
			<th>Admin Strength</th>
			<th>Frequency</th>
			<th>Route</th>
			<th>Start</th>
			<th>End</th>
			<th>Remarks</th>
			<th style="width: 16px"></th>
			<th style="width: 16px"></th>
		</tr>
		<c:set var="numItems" value="${fn:length(prescriptions)}"/>
		<c:forEach begin="1" end="${numItems+1}" var="i">
			<c:set var="item" value="${prescriptions[i-1].map}"/>
			<c:if test="${empty item}">
				<c:set var="tr_style" value='style="display:none"'/>
			</c:if>
			<c:set var="flagColor" value="empty"/>
			<c:if test="${item.discontinued == 'Y'}">
				<c:set var="flagColor" value="grey"/>
			</c:if>
			<tr ${tr_style}>
				<td>
					<img src="${cpath}/images/${flagColor}_flag.gif"/>
					<label>
					<c:choose>
						<c:when test="${item.presc_type == 'M'}">
							Medicine
						</c:when>
						<c:when test="${item.presc_type == 'I'}">
							Investigation
						</c:when>
						<c:when test="${item.presc_type == 'S'}">
							Service
						</c:when>
						<c:when test="${item.presc_type == 'C'}">
							Consultation
						</c:when>
						<c:when test="${item.presc_type == 'OPE'}">
							Surgery
						</c:when>
						<c:when test="${item.presc_type == 'O'}">
							Others
						</c:when>
					</c:choose>
					</label>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.start_datetime}" var="start_datetime"/>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.end_datetime}" var="end_datetime"/>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.prescription_date}" var="prescription_datetime"/>

					<input type="hidden" name="h_prescription_id" value="${item.prescription_id}"/>
					<input type="hidden" name="h_presc_type" value="${item.presc_type}"/>
					<input type="hidden" name="h_item_id" value="${item.item_id}"/>
					<input type="hidden" name="h_item_name" value="${item.item_name}"/>
					<input type="hidden" name="h_generic_code" value="${item.generic_code}"/>
					<input type="hidden" name="h_generic_name" value="${item.generic_name}"/>
					<input type="hidden" name="h_med_dosage" value="${item.med_dosage}"/>
					<input type="hidden" name="h_med_form_id" value="${item.med_form_id}"/>
					<input type="hidden" name="h_med_strength" value="${item.med_strength}"/>
					<input type="hidden" name="h_med_strength_units" value="${item.med_strength_units}"/>
					<input type="hidden" name="h_med_route" value="${item.med_route}"/>
					<input type="hidden" name="h_med_route_name" value="${item.med_route_name}"/>
					<input type="hidden" name="h_consumption_uom" value="${item.consumption_uom}"/>
					<input type="hidden" name="h_prior_med" value="${item.prior_med}">

					<input type="hidden" name="h_admin_strength" value="${item.admin_strength}"/>
					<input type="hidden" name="h_freq_type" value="${item.freq_type}"/>
					<input type="hidden" name="h_recurrence_daily_id" value="${item.recurrence_daily_id}"/>
					<input type="hidden" name="h_repeat_interval" value="${item.repeat_interval}"/>
					<input type="hidden" name="h_repeat_interval_units" value="${item.repeat_interval_units}"/>
					<input type="hidden" name="h_start_datetime" value="${start_datetime}"/>
					<input type="hidden" name="h_end_datetime" value="${end_datetime}"/>
					<input type="hidden" name="h_no_of_occurrences" value="${item.no_of_occurrences}"/>
					<input type="hidden" name="h_end_on_discontinue" value="${item.end_on_discontinue}"/>
					<input type="hidden" name="h_discontinued" value="${item.discontinued}"/>
					<input type="hidden" name="h_remarks" value="${item.remarks}"/>

					<input type="hidden" name="h_org_freq_type" value="${item.freq_type}"/>
					<input type="hidden" name="h_org_recurrence_daily_id" value="${item.recurrence_daily_id}"/>
					<input type="hidden" name="h_org_repeat_interval" value="${item.repeat_interval}"/>
					<input type="hidden" name="h_org_repeat_interval_units" value="${item.repeat_interval_units}"/>
					<input type="hidden" name="h_org_start_datetime" value="${start_datetime}"/>
					<input type="hidden" name="h_org_end_datetime" value="${end_datetime}"/>
					<input type="hidden" name="h_org_no_of_occurrences" value="${item.no_of_occurrences}"/>
					<input type="hidden" name="h_org_end_on_discontinue" value="${item.end_on_discontinue}"/>

					<input type="hidden" name="h_doctor_id" value="${item.doctor_id}"/>
					<input type="hidden" name="h_doctor_name" value="${item.doctor_name}"/>
					<input type="hidden" name="h_entered_by" value="${item.entered_by}"/>
					<input type="hidden" name="h_prescription_date" value="${prescription_datetime}"/>

					<input type="hidden" name="h_edited" value="false"/>
					<input type="hidden" name="h_delete" value="false"/>
				</td>
				<td>
					<c:choose>
						<c:when test="${item.presc_type == 'M'}">
							<c:set var="item_name" value="${item.item_name}"/>
							<c:if test="${not empty item.med_form_name}">
								<c:set var="item_name" value="${item_name}/${item.med_form_name}"/>
							</c:if>
							<c:if test="${not empty item.med_strength}">
								<c:set var="item_name" value="${item_name}/${item.med_strength} ${item.unit_name}"/>
							</c:if>
						</c:when>
						<c:otherwise>
							<c:set var="item_name" value="${item.item_name}"/>
						</c:otherwise>
					</c:choose>
					<insta:truncLabel value="${item_name}" length="25"/>
				</td>
				<td>
					<insta:truncLabel value="${item.med_dosage}${item.consumption_uom}" length="10"/>
				</td>
				<td>
					<insta:truncLabel value="${item.admin_strength}" length="15"/>
				</td>
				<td>
					<c:choose>
						<c:when test="${item.repeat_interval_units == 'M'}">
							<c:set var="interval_units" value="Minutes"/>
						</c:when>
						<c:when test="${item.repeat_interval_units == 'H'}">
							<c:set var="interval_units" value="Hours"/>
						</c:when>
						<c:when test="${item.repeat_interval_units == 'D'}">
							<c:set var="interval_units" value="Days"/>
						</c:when>
					</c:choose>
					<c:set var="interval" value="${item.repeat_interval} ${interval_units}"/>
					<insta:truncLabel value="${item.freq_type == 'F' ? item.recurrence_name : interval}" length="10"/>
				</td>
				<td><insta:truncLabel value="${item.med_route_name}" length="10"/></td>
				<td><insta:truncLabel value="${start_datetime}" length="10"/></td>
				<td>
					<label>
						<c:choose>
							<c:when test="${not empty end_datetime}">
								${end_datetime}
							</c:when>
							<c:when test="${not empty item.no_of_occurrences}">
								${item.no_of_occurrences} time(s)
							</c:when>
							<c:when test="${not empty item.end_on_discontinue}">
								Till Discontinued
							</c:when>
						</c:choose>
					</label>
				</td>
				<td><insta:truncLabel value="${item.remarks}" length="20"/></td>
				<td style="width: 16px">
					<c:choose>
						<c:when test="${item.has_completed_activities || item.discontinued == 'Y'}">
							<img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
						</c:when>
						<c:otherwise>
							<a href="#Cancel Prescription" onclick="return cancelPrescription(this);" title="Cancel Prescription" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
						</c:otherwise>
					</c:choose>
				</td>
				<td style="width: 16px">
					<a name="_prescEditAnchor" href="#Edit Prescription Details" onclick="return showEditPrescDialog(this);"
						title="Edit Prescription Details">
						<img src="${cpath}/icons/Edit.png" class="button" />
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	<table class="addButton" style="height: 25px;">
		<tr>
			<td style="width: 16px;text-align: right">
				<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Item"
					onclick="showAddDialog(this); return false;"
					class="imgButton"><img src="${cpath}/icons/Add.png"></button>
			</td>
		</tr>
	</table>
<c:if test="${param.screen_id == 'doctororder'}">
</fieldset>
</c:if>
<div id="addItemDialog" style="display: none">
	<div class="hd">Add Admission Orders</div>
	<div class="bd">
		<div id="itemDiv">
			<table class="formtable">
				<tr>
					<td class="formlabel" style="width: 100px">Doctor: </td>
					<td>
						<div id="d_doctorAutoComplete">
							<input type="text" name="d_doctor" id="d_doctor" value=""/>
							<input type="hidden" name="d_doctor_id" id="d_doctor_id" value=""/>
							<div id="d_doctorContainer" class="scrollForContainer" style="width: 350px"/>
						</div>
					</td>
					<td class="formlabel" style="width: 100px">Presc. Date: </td>
					<td style="width: 200px">
						<div style="display: inline;">
							<insta:datewidget name="d_prescription_date" value="today"/>
							<input type="text" name="d_prescription_time" id="d_prescription_time" style="width: 50px" value="${current_time}"/>
						</div>
					</td>
					<td class="formlabel">Entered By: </td>
					<td>
						<c:set var="user" value="${userid}"/>
						<label>${isSharedLogIn == 'Y' ?'': ifn:cleanHtml(user)}</label>
						<input type="hidden" name="d_entered_by" id="d_entered_by" value="${isSharedLogIn == 'Y' ?'': ifn:cleanHtml(user)}"/>
					</td>
				</tr>
			</table>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Item</legend>
				<table class="formtable">
					<tr>
						<td style="width: 105px"><input type="radio" name="d_itemType" value="M" onclick="onItemChange()"/>
						Medicine</td>
						<td style="width: 120px"><input type="radio" name="d_itemType" value="I" onclick="onItemChange()"/>
						Investigation</td>
						<td style="width: 100px"><input type="radio" name="d_itemType" value="S" onclick="onItemChange()"/>
						Service</td>
						<td style="width: 120px"><input type="radio" name="d_itemType" value="CP" onclick="onItemChange()"/>
						Order Sets</td>
						<td style="width: 120px"><input type="radio" name="d_itemType" value="C" onclick="onItemChange()"/>
						Consultation</td>
						<td style="width: 155px"><input type="radio" name="d_itemType" value="OPE" onclick="onItemChange()"/>
						Surgery/Procedure</td>
						<td ><input type="radio" name="d_itemType" value="O" onclick="onItemChange()"/>
						Others</td>
					</tr>
					<tr>
						<td style="width: 100px" class="formlabel"><label id="d_itemDisplayLabel"></label></td>
						<td colspan="5">
							<div id="d_itemAutoComp">
								<input type="text" name="d_item_name" id="d_item_name" style="width: 400px"/>
								<input type="hidden" name="d_item_id" id="d_item_id"/>
								<div id="d_itemContainer" class="scrollForContainer"/>
							</div>
							<input type="hidden" name="d_generic_code" id="d_generic_code" value=""/>
							<input type="hidden" name="d_generic_name" id="d_generic_name" value=""/>
							<input type="hidden" name="d_qty_in_stock" id="d_qty_in_stock" value=""/>
							<input type="hidden" name="d_ispackage" id="d_ispackage" value=""/>
							<input type="hidden" name="d_pack_type" id="d_pack_type" value=""/>
						</td>
					</tr>
				</table>
			</fieldset>
			<div id='d_hide_ope'>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Item Details</legend>
				<table class="formtable" >
					<tr>
						<td class="formlabel">Generic: </td>
						<td colspan="5"><a id="d_generic_name_anchor" style="display: none"></a></td>
					</tr>
					<tr>
						<td class="formlabel" >Form: </td>
						<td>
							<insta:selectdb name="d_med_form_id" id="d_med_form_id" table="item_form_master"
								displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
								dummyvalue="-- Select --" dummyvalueId=""/>
						</td>
						<td class="formlabel">Strength: </td>
						<td >
							<input type="text" name="d_med_strength" id="d_med_strength" value="" style="width: 60px"/>
							<insta:selectdb name="d_med_strength_units" id="d_med_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
									dummyvalue="Select --" style="width: 60px"/>
						</td>
						<td class="formlabel">Route: </td>
						<td >
							<select id="d_med_route" class="dropdown">
								<option value="">-- Select --</option>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel" >Prior Medication: </td>
						<td ><input type="checkbox" id="d_prior_medication" value="Y"></td>
						<td class="formlabel" >Dosage: <label id="d_medicineUOM"></label></td>
						<td >
							<div id="d_dosageAutoComp">
								<input type="text" name="d_med_dosage" id="d_med_dosage"/>
								<div id="d_dosageContainer" class="scrollForContainer"/>
							</div>

							<input type="hidden" name="d_consumption_uom" id="d_consumption_uom" value=""/>
						</td>
						<td class="formlabel">Admin Strength: </td>
						<td>
							<input type="text" name="d_admin_strength" id="d_admin_strength" value="" maxlength="98"/>
						</td>
					</tr>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Frequency and Duration</legend>
				<table class="formtable">
					<tr>
						<td style="width: 100px" class="formlabel"><input type="radio" name="d_freq_type" value="F" checked onclick="toggleEndActivity('d')"/> Frequency</td>
						<td >
							<select class="dropdown" name="d_frequency" id="d_frequency" onclick="toggleEndActivity('d');">
								<option value="">-- Select --</option>
								<c:forEach var="frequency" items="${frequencies}">
									<option value="${frequency.map.recurrence_daily_id}"
										${frequency.map.recurrence_daily_id == -1 ? 'selected' : ''}>${frequency.map.display_name}</option>
								</c:forEach>
							</select>
						</td>
						<td class="formlabel" ><input type="radio" name="d_freq_type" value="R" onclick="toggleEndActivity('d')"> Repeat at interval: </td>
						<td colspan="3">
							<input type="text" name="d_repeat_interval" id="d_repeat_interval" class="number"/>
							<select name="d_repeat_interval_units" id="d_repeat_interval_units" class="dropdown" style="width: 70px;vertical-align: top">
								<option value="M">Minutes</option>
								<option value="H">Hours</option>
								<option value="D">Days</option>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel" style="width: 100px">Start Time: </td>
						<td>
							<div style="display: inline;">
								<insta:datewidget name="d_start_date" value="today"/>
								<input type="text" name="d_start_time" id="d_start_time" style="width: 50px" value="${current_time}"/>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel" style="width: 100px"><input type="radio" name="d_end_of_activity" value="E" onclick="clearEndActivityFields('d')">  End Date: </td>
						<td >
							<div style="display: inline;">
								<insta:datewidget name="d_end_date"/>
									<input type="text" name="d_end_time" id="d_end_time" style="width: 50px">
								</div>
							</td>
							<td class="formlabel"><input type="radio" name="d_end_of_activity" value="O" checked onclick="clearEndActivityFields('d')">  Occurences: </td>
							<td ><input type="text" name="d_no_of_occurrences" id="d_no_of_occurrences" value="1" class="number"></td>
							<td ><input type="radio" name="d_end_of_activity" value="D" onclick="clearEndActivityFields('d')">  Till Discontinued: </td>

						</tr>

					</table>
				</fieldset>
				</div>
				<fieldset class="fieldSetBorder">
					<table class="formtable">
						<tr>
							<td style="width: 70px" class="formlabel">Remarks: </td>
							<td >
								<textarea name="d_remarks" id="d_remarks" cols="60" rows="1"></textarea>
							</td>

						</tr>
					</table>
				</fieldset>
				<div style="margin-top: 5px;display:${param.screen_id == 'addnewadmissionrequest' ? 'none' : ''}">
					<div style="float: left"><input type="checkbox" name="d_discontinued" id="d_discontinued" value="Y" disabled/></div>
					<div style="padding-top: 3px;float: left">Discontinue Prescription</div>
				</div>
				<div style="clear: both"></div>
			</div>
			<div id="doctorVisitDiv" style="display: none">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Doctor Visits</legend>
					<table class="formtable" id="docVisits">
					</table>
				</fieldset>
			</div>
			<table style="margin-top: 5px">
				<tr>
					<td>
						<button type="button" name="d_Ok" id="d_Ok" accessKey="A">
							<u><b>A</b></u>dd
						</button>
						<input type="button" name="d_previous" id="d_previous" value="<<Previous"/>
						<button type="button" name="d_next" id="d_next" accessKey="N">
							<u><b>N</b></u>ext>>
						</button>
						<button type="button" name="d_Close" id="d_Close" accessKey="C">
							<u><b>C</b></u>lose
						</button>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div id="editItemDialog" style="display: none">
		<input type="hidden" id="editRowId" name="editRowId" value=""/>
		<div class="hd">Edit prescription details</div>
		<div class="bd">
			<table class="formtable">
				<tr>
					<td class="formlabel" style="width: 100px">Doctor: </td>
					<td>
						<div id="ed_doctorAutoComplete">
							<input type="text" name="ed_doctor" id="ed_doctor" value="" onchange="setFieldEdited();"/>
							<input type="hidden" name="ed_doctor_id" id="ed_doctor_id" value=""/>
							<div id="ed_doctorContainer" class="scrollForContainer" style="width: 350px"/>
						</div>
					</td>
					<td class="formlabel" style="width: 100px">Presc. Date: </td>
					<td style="width: 200px">
						<div style="display: inline;">
							<insta:datewidget name="ed_prescription_date" extravalidation="setFieldEdited();"/>
						<input type="text" name="ed_prescription_time" id="ed_prescription_time" style="width: 50px" onchange="setFieldEdited();"/>
					</div>
				</td>
				<td class="formlabel">Entered By: </td>
				<td>
					<label id="ed_ip_entered_by_label"></label>
					<input type="hidden" name="ed_entered_by" id="ed_entered_by" />
				</td>
			</tr>
		</table>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Item</legend>
			<table class="formtable">
				<tr>
					<td style="width: 60px" class="formlabel">Item Type: </td>
					<td><label id="ed_itemTypeLabel"></label></td>
				</tr>
				<tr>
					<td style="width: 60px" class="formlabel"><label id="ed_itemDisplayLabel"></label></td>
					<td colspan="5" style="width: 400px">
						<label id="ed_item_name" ></label>
						<input type="hidden" name="ed_itemType" id="ed_itemType" />
						<input type="hidden" name="ed_item_id" id="ed_item_id"/>
						<input type="hidden" name="ed_generic_code" id="ed_generic_code" value=""/>
						<input type="hidden" name="ed_generic_name" id="ed_generic_name" value=""/>
						<input type="hidden" name="ed_qty_in_stock" id="ed_qty_in_stock" value=""/>
						<input type="hidden" name="ed_ispackage" id="ed_ispackage" value=""/>
					</td>
				</tr>
			</table>
		</fieldset>
		<div id="ed_hide_ope">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Item Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Generic: </td>
					<td colspan="5"><a id="ed_generic_name_anchor" style="display: none"></a></td>
				</tr>
				<tr>
					<td class="formlabel" >Form: </td>
					<td>
						<insta:selectdb name="ed_med_form_id" id="ed_med_form_id" table="item_form_master"
							displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
							dummyvalue="-- Select --" dummyvalueId="" onchange="setFieldEdited();"/>
					</td>
					<td class="formlabel">Strength: </td>
					<td>
						<input type="text" name="ed_med_strength" id="ed_med_strength" value="" style="width: 60px" onchange="setFieldEdited();"/>
						<insta:selectdb name="ed_med_strength_units" id="ed_med_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
							dummyvalue="Select --" style="width: 60px" onchange="setFieldEdited();"/>
					</td>
					<td class="formlabel">Route: </td>
					<td >
						<select id="ed_med_route" name="ed_med_route" class="dropdown" onchange="setFieldEdited();">
							<option value="">-- Select --</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel" >Prior Medication: </td>
					<td ><input type="checkbox" id="ed_prior_medication" value="Y" onchange="setFieldEdited();"></td>
					<td class="formlabel" style="width: 150px">Dosage: <label id="ed_medicineUOM"></label></td>
					<td >
						<div id="ed_dosageAutoComp">
							<input type="text" name="ed_med_dosage" id="ed_med_dosage" onchange="setFieldEdited();"/>
							<div id="ed_dosageContainer" class="scrollForContainer"/>
						</div>

						<input type="hidden" name="ed_consumption_uom" id="ed_consumption_uom" value=""/>
					</td>
					<td class="formlabel">Admin Strength: </td>
						<td>
							<input type="text" name="ed_admin_strength" id="ed_admin_strength" value="" onchange="setFieldEdited();" maxlength="98"/>
						</td>
				</tr>
			</table>
		</fieldset>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Frequency and Duration</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel" style="width: 100px"><input type="radio" name="ed_freq_type" value="F" checked onclick="setFieldEdited();toggleEndActivity('ed');"/> Frequency</td>
					<td>
						<select class="dropdown" name="ed_frequency" id="ed_frequency" onchange="setFieldEdited();toggleEndActivity('ed');">
							<option value="">-- Select --</option>
							<c:forEach var="frequency" items="${frequencies}">
								<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
							</c:forEach>
						</select>
					</td>
					<td class="formlabel"><input type="radio" name="ed_freq_type" value="R" onclick="setFieldEdited();toggleEndActivity('ed')"> Repeat at interval: </td>
					<td colspan="3">
						<input type="text" name="ed_repeat_interval" id="ed_repeat_interval" onchange="setFieldEdited();" class="number"/>
						<select name="ed_repeat_interval_units" id="ed_repeat_interval_units" onchange="setFieldEdited();" class="dropdown" style="width: 70px;vertical-align: top">
							<option value="M">Minutes</option>
							<option value="H">Hours</option>
							<option value="D">Days</option>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel" style="width: 100px">Start Time</td>
					<td >
						<div style="display: inline;">
							<insta:datewidget name="ed_start_date" extravalidation="setFieldEdited();"/>
							<input type="text" name="ed_start_time" id="ed_start_time" style="width: 50px" onchange="setFieldEdited();"/>
						</div>
					</td>

				</tr>
				<tr>
					<td style="width: 100px" class="formlabel"><input type="radio" name="ed_end_of_activity" value="E" onclick="clearEndActivityFields('ed');setFieldEdited();">  End Date: </td>
					<td >
						<div style="display: inline;">
							<insta:datewidget name="ed_end_date" extravalidation="setFieldEdited();"/>
								<input type="text" name="ed_end_time" id="ed_end_time" style="width: 50px" onchange="setFieldEdited();">
							</div>
						</td>
						<td class="formlabel"><input type="radio" name="ed_end_of_activity" value="O" checked onclick="clearEndActivityFields('ed');setFieldEdited();">  Occurences: </td>
						<td ><input type="text" name="ed_no_of_occurrences" id="ed_no_of_occurrences" value="1" onchange="setFieldEdited();" class="number"></td>
						<td ><input type="radio" name="ed_end_of_activity" value="D" onclick="clearEndActivityFields('ed');setFieldEdited();">  Till Discontinued: </td>
					</tr>
				</table>
			</fieldset>
			</div>
			<fieldset class="fieldSetBorder">
				<table class="formtable">
					<tr>
						<td style="width: 70px" class="formlabel">Remarks: </td>
						<td >
							<textarea name="ed_remarks" id="ed_remarks" cols="60" rows="1" onchange="setFieldEdited();"></textarea>
						</td>
					</tr>
				</table>
			</fieldset>
			<div style="margin-top: 5px;display:${param.screen_id == 'addnewadmissionrequest' ? 'none' : ''}">
				<div style="float: left"><input type="checkbox" name="ed_discontinued" id="ed_discontinued" value="Y" onclick="setFieldEdited();"/></div>
				<div style="padding-top: 3px;float: left">Discontinue Prescription</div>
			</div>
			<div style="clear: both"></div>
			<table style="margin-top: 5px">
				<tr>
					<td>
						<input type="button" id="ed_Ok" value="Ok"/>
						<input type="button" id="ed_Close" value="Close"/>
						<input type="button" id="ed_Previous" value="<<Previous"/>
						<input type="button" id="ed_Next" value="Next>>"/>
					</td>
				</tr>
			</table>
		</div>
	</div>
<div id="genericNameDisplayDialog"  style="visibility:hidden">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Generic Name Details</legend>
			<table border="0" class="formtable">
				<tr height="10px"></tr>
				<tr>
					<td class="formlabel">Generic&nbsp;Name: </td>
					<td class="forminfo" style="width:8em"><b><label id="gen_generic_name"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Classification: </td>
					<td class="forminfo" style="width:25em"><b><label id="classification_name"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel">Sub-Classification:</td>
						<td class="forminfo" style="width:25em"><b><label id="sub_classification_name"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel">Standard Adult Dose:</td>
						<td class="forminfo" style="width:25em"><b><label id="standard_adult_dose"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel">Criticality:</td>
						<td class="forminfo" style="width:25em"><b><label id="criticality"></label>	</td>
				</tr>

				<tr height="10px"></tr>
			</table>
			<table>
				<tr>
			    	<td><input type="button" id="genericNameCloseBtn" value="Close"></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>