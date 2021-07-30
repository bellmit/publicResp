<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<div class="resultList">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Standing Instructions</legend>
		<div id="discontiuedDiv">
			<label>Hide Discontinued: </label>
			<input type="checkbox" name="hideDiscontinued" onclick="return hideDiscontinuedItems(this);">
		</div>
		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="siTable" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
			<tr>
				<th>Presc Date</th>
				<th>Doctor</th>
				<th>Type</th>
				<th>Name</th>
				<th>Form</th>
				<th>Strength</th>
				<th>Dosage</th>
				<th>Frequency</th>
				<th>Route</th>
				<th>Remarks</th>
				<th style="width: 16px"></th>
				<th style="width: 16px"></th>
			</tr>
			<c:set var="numStandingInstructions" value="${fn:length(standing_instructions)}"/>
			<c:forEach begin="1" end="${numStandingInstructions+1}" var="i" varStatus="loop">
				<c:set var="instruction" value="${standing_instructions[i-1].map}"/>
				<c:set var="si_priorAuth" value="${not empty patient.primary_sponsor_id ? instruction.prior_auth_required : ''}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${instruction.discontinued == 'Y'}">grey</c:when>
						<c:when test="${si_priorAuth == 'A'}">blue</c:when>
						<c:when test="${si_priorAuth == 'S'}">green</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<c:if test="${empty instruction}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<tr ${style} class="${instruction.discontinued == 'Y' ? 'discontinued' : ''}">
					<td>
						<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${instruction.prescribed_date}" var="prescribed_date"/>
						${prescribed_date}
					</td>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<insta:truncLabel value="${instruction.doctor_name}" length="20"/>
						<input type="hidden" name="s_prescribed_date" value="${prescribed_date}"/>
						<input type="hidden" name="s_doctor_id" value="${instruction.doctor_id}"/>
						<input type="hidden" name="s_prescription_id" value="${instruction.prescription_id}"/>
						<input type="hidden" name="s_item_name" value="<c:out value='${instruction.item_name}'/>"/>
						<input type="hidden" name="s_item_id" value="${instruction.item_id}"/>
						<input type="hidden" name="s_medicine_dosage" value="${instruction.medicine_dosage}"/>
						<input type="hidden" name="s_item_remarks" value="${instruction.remarks}"/>
						<input type="hidden" name="s_item_master" value="${instruction.master}"/>
						<input type="hidden" name="s_freq_type" value="${instruction.freq_type}"/>
						<input type="hidden" name="s_recurrence_daily_id" value="${instruction.recurrence_daily_id}"/>
						<input type="hidden" name="s_repeat_interval" value="${instruction.repeat_interval}"/>
						<input type="hidden" name="s_consumption_uom" value="${instruction.consumption_uom}"/>
						<input type="hidden" name="s_order_lead_time" value="${instruction.order_lead_time}"/>
						<input type="hidden" name="s_delItem" id="s_delItem" value="false" />
						<input type="hidden" name="s_generic_code" value="${instruction.generic_code}"/>
						<input type="hidden" name="s_generic_name" value="${instruction.generic_name}"/>
						<input type="hidden" name="s_addActivity" value="${instruction.activity_exists}"/>
						<input type="hidden" name="s_ispackage" value="${instruction.ispackage}"/>
						<input type="hidden" name="s_discontinued" value="${not empty instruction.discontinued ? instruction.discontinued : 'N'}">
						<input type="hidden" name="s_edited" value='false'/>
						<input type="hidden" name="s_route_id" value="${instruction.route_id}"/>
						<input type="hidden" name="s_route_name" value="${instruction.route_name}"/>
						<input type="hidden" name="s_qty_in_stock" value=""/>
						<input type="hidden" name="s_priorAuth" value="${si_priorAuth}"/>
						<input type="hidden" name="s_item_form_id" value="${instruction.item_form_id == 0 ? '' : instruction.item_form_id}"/>
						<input type="hidden" name="s_item_strength" value="${instruction.item_strength}"/>
						<c:choose>
							<c:when test="${instruction.type == 'M' || instruction.type == 'O'}">
								<c:set var="s_itemType" value="Medicine"/>
							</c:when>
							<c:when test="${instruction.type == 'T'}">
								<c:set var="s_itemType" value="Inv."/>
							</c:when>
							<c:when test="${instruction.type == 'S'}">
								<c:set var="s_itemType" value="Service"/>
							</c:when>
							<c:when test="${instruction.type == 'D'}">
								<c:set var="s_itemType" value="Doctor"/>
							</c:when>
							<c:when test="${instruction.type == 'I'}">
								<c:set var="s_itemType" value="Instructions"/>
							</c:when>
							<c:when test="${instruction.type == 'N'}">
								<c:set var="s_itemType" value="NonHospital"/>
							</c:when>
						</c:choose>
						<input type="hidden" name="s_itemType" value="${s_itemType}"/>
					</td>
					<td>
						<label>${s_itemType}</label>
					</td>
					<td>
						<insta:truncLabel value="${instruction.item_name}" length="20"/>
					</td>
					<td>
						<insta:truncLabel value="${instruction.item_form_name}" length="15"/>
					</td>
					<td>
						<insta:truncLabel value="${instruction.item_strength}" length="15"/>
					</td>
					<td>
						<c:if test="${s_itemType == 'Medicine' || s_itemType == 'NonHospital'}">
							<label>${instruction.medicine_dosage}</label>
						</c:if>
					</td>
					<td>
						<label>${instruction.display_name}</label>
					</td>
					<td>
						<c:if test="${s_itemType == 'Medicine'}">
							<label>${instruction.route_name}</label>
						</c:if>
					</td>
					<td>
						<insta:truncLabel value="${instruction.remarks}" length="30"/>
					</td>
					<td style="text-align: center; ">
						<a name="trashCanAnchor" href="javascript:Cancel Item" style="display: none" onclick="return cancelSIItem(this);" title="Cancel Item" >
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
					</td>
					<td style="text-align: center">
						<a name="si_editAnchor" href="javascript:Edit" onclick="return showEditSIDialog(this);" title="Edit Item Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
		<table class="addButton" >
			<tr>
				<td></td>
				<td width="16px" style="text-align: center">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add Standing Instruction (Alt_Shift_O)"
						onclick="showAddSIDialog(this); return false;"
						accesskey="O" class="imgButton"><img src="${cpath}/icons/AddBlue.png"></button>
				</td>
			</tr>
		</table>
	</fieldset>
</div>
<div id="addSIDialog" style="display: none">
	<div class="bd">
		<div id="addSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Item</legend>
				<fieldset>
					<legend class="fieldSetLabel">Item Type</legend>
					<table>
						<tr>
							<td ><input type="radio" name="s_d_itemType" value="Medicine" checked onclick="onSIItemChange();"> Medicine</td>
							<td ><input type="radio" name="s_d_itemType" value="Inv." onclick="onSIItemChange();"> Investigation</td>
							<td ><input type="radio" name="s_d_itemType" value="Service" onclick="onSIItemChange();"> Service</td>
						</tr>
						<tr>
							<td><input type="radio" name="s_d_itemType" value="Doctor" onclick="onSIItemChange();"> Doctor Consultation</td>
							<td><input type="radio" name="s_d_itemType" value="NonHospital" onclick="onSIItemChange();"> Non Hospital Items</td>
							<c:if test="${visitType == 'i'}">
								<td><input type="radio" name="s_d_itemType" value="Instructions" onclick="onSIItemChange();">Doctor Instructions</td>
							</c:if>
						</tr>
					</table>
				</fieldset>
				<table class="formtable">
					<tr>
						<td class="formlabel">Item: </td>
						<td colspan="3">
							<div id="s_d_itemAutocomplete" style="padding-bottom: 10px; width: 443px">
								<input type="text" id="s_d_itemName" name="s_d_itemName" >
								<div id="s_d_itemContainer" style="width: 350px" class="scrolForContainer"></div>
								<input type="hidden" name="s_d_item_master" id="s_d_item_master" value=""/>
								<input type="hidden" name="s_d_generic_name" id="s_d_generic_name" value=""/>
								<input type="hidden" name="s_d_generic_code" id="s_d_generic_code" value=""/>
								<input type="hidden" name="s_d_ispackage" id="s_d_ispackage" value=""/>
								<input type="hidden" name="s_d_item_id" id="s_d_item_id" value=""/>
								<input type="hidden" name="s_d_qty_in_stock" id="s_d_qty_in_stock" value=""/>
								<input type="hidden" name="s_d_priorAuth" id="s_d_priorAuth" value=""/>
							</div>
							<font style="color: red; margin-left: 444px">*</font>
						</td>
					</tr>
					<tr >
						<td class="formlabel">Hospital Item: </td>
						<td ><b><label id="s_d_itemMasterType"></label></b></td>
						<td class="formlabel">Generic: </td>
						<td ><a id="s_d_genericNameAnchor_dialog" style="display: none"></a></td>
					</tr>
					<tr id="s_d_itemFormRow">
						<td class="formlabel">Item Form: </td>
						<td><insta:selectdb name="s_d_item_form_id" id="s_d_item_form_id" table="item_form_master"
							displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
							dummyvalue="-- Select --" dummyvalueId=""/>
						</td>
						<td class="formlabel">Strength</td>
						<td><input type="text" name="s_d_item_strength" id="s_d_item_strength" value=""></td>
					</tr>
					<tr id="s_d_prior_auth_row">
						<td class="formlabel">Prior Auth: </td>
						<td colspan="3"><b><label id="s_d_priorAuth_label"></label></b></td>
					</tr>
					<tr>
						<td class="formlabel">Route: </td>
						<td><select name="s_d_medicine_route" id="s_d_medicine_route" class="dropdown">
								<option value="">-- Select --</option>
							</select>
						</td>
						<td class="formlabel">Dosage per ADM.: </td>
						<td>
							<input type="text" name="s_d_dosage" id="s_d_dosage" style="width: 100px" onkeypress="return enterNumAndDot(event)">
							<label id="s_d_medicineUOM"></label><font style="color: red">*</font>
							<img class="imgHelpText" src="${cpath}/images/help.png" title="Dosage per Administration"/>
							<inpu typ="hidden" name="s_d_consumption_uom" id="s_d_consumption_uom" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel" >Remarks: </td>
						<td colspan="3"><input type="text" name="s_d_remarks" id="s_d_remarks" value="" style="width: 443px"/></td>
					</tr>
					<tr>
						<td class="formlabel">Frequency: </td>
						<td>
							<select id="s_d_frequency_name" name="s_d_frequency_name" class="dropdown" onchange="return toggleInterval('s_d_');">
								<option value=""></option>
								<c:forEach var="frequency" items="${frequencies}">
									<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
								</c:forEach>
								<option value="0">Custom</option>
							</select>
							<font style="color: red;">*</font>
						</td>
						<td class="formlabel">Interval (mts): </td>
						<td><input type="text" name="s_d_interval" id="s_d_interval" onkeypress="return enterNumOnlyzeroToNine(event);" disabled/></td>
					</tr>
					<tr>
						<td class="formlabel">Order Lead Time: </td>
						<td><input type="text" name="s_d_order_lead_time" id="s_d_order_lead_time"/></td>
					</tr>
					<c:if test="${preferences.modulesActivatedMap.mod_wardactivities == 'Y'}">
						<tr>
							<td class="formlabel">Add Activity: </td>
							<td><input type="checkbox" name="s_d_addActivity" id="s_d_addActivity" value="AddActivity"
								${visitType == 'i' ? 'checked' : ''}></td>
						</tr>
					</c:if>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="SIAdd" id="SIAdd" >Add</button>
					<input type="button" name="SIClose" value="Close" id="SIClose"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editSIDialog" style="display: none">
	<input type="hidden" name="s_ed_editRowId" id="s_ed_editRowId" value=""/>
	<div class="bd">
		<div id="editSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Item</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Type: </td>
						<td>
							<label id="s_ed_itemTypeLabel" style="font-weight: bold"></label>
							<input type="hidden" id="s_ed_itemType" name="s_ed_itemType" value=""/>
						</td>
						<td class="formlabel">Presc. Date</td>
						<td class="forminfo"><label id="s_ed_presc_date"></label></td>
					</tr>
					<tr>
						<td class="formlabel">Item: </td>
						<td colspan="3">
							<label id="s_ed_itemNameLabel" style="font-weight: bold"></label>
							<input type="hidden" id="s_ed_itemName" name="s_ed_itemName" value="">
							<input type="hidden" id="s_ed_item_master" name="s_ed_item_master" value=""/>
							<input type="hidden" id="s_ed_ispackage" name="s_ed_ispackage" value=""/>
							<input type="hidden" id="s_ed_item_id" name="s_ed_item_id" value=""/>
							<input type="hidden" name="s_ed_priorAuth" id="s_ed_priorAuth" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Hospital Item: </td>
						<td ><b><label id="s_ed_itemMasterType"></label></b></td>
						<td class="formlabel">Generic: </td>
						<td ><a id="s_ed_genericNameAnchor_editdialog" style="display:none"></a></td>
					</tr>
					<tr id="s_ed_itemFormRow">
						<td class="formlabel">Item Form: </td>
						<td><insta:selectdb name="s_ed_item_form_id" id="s_ed_item_form_id" table="item_form_master"
							displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
							dummyvalue="-- Select --" dummyvalueId=""/>
						</td>
						<td class="formlabel">Strength</td>
						<td><input type="text" name="s_ed_item_strength" id="s_ed_item_strength" value=""></td>
					</tr>
					<tr id="s_ed_prior_auth_row">
						<td class="formlabel">Prior Auth: </td>
						<td colspan="3"><b><label id="s_ed_priorAuth_label"></label></b></td>
					</tr>
					<tr>
						<td class="formlabel">Route: </td>
						<td><b><label id="s_ed_medicine_route"></label></td>
						<td class="formlabel">Dosage per ADM.: </td>
						<td>
							<input type="text" name="s_ed_dosage" id="s_ed_dosage" value="" style="width: 100px" onchange="setSIEdited()" onkeypress="return enterNumAndDot(event)"/>
							<label id="s_ed_medicineUOM"></label><font style="color: red">*</font>
							<img class="imgHelpText" src="${cpath}/images/help.png" title="Dosage per Administration"/>
							<input type="hidden" name="s_ed_consumption_uom" id="s_ed_consumption_uom" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Frequency: </td>
						<td>
							<select id="s_ed_frequency_name" name="s_ed_frequency" class="dropdown" onchange="return toggleInterval('s_ed_');setSIEdited();">
								<option value=""></option>
								<c:forEach var="frequency" items="${frequencies}">
									<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
								</c:forEach>
								<option value="0">Custom</option>
							</select>
							<font style="color: red;">*</font>
						</td>
						<td class="formlabel">Interval: </td>
						<td><input type="text" name="s_ed_interval" id="s_ed_interval" onchange="setSIEdited()" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="3"><input type="text" name="s_ed_remarks" id="s_ed_remarks" onchange="setSIEdited()" value="" style="width: 443px;"></td>
					</tr>
					<tr>
						<td class="formlabel">Order Lead Time: </td>
						<td><input type="text" name="s_ed_order_lead_time" id="s_ed_order_lead_time" onchange="setSIEdited()"/></td>
						<td class="formlabel">Discountinue: </td>
						<td><input type="checkbox" name="s_ed_discontinue" id="s_ed_discontinue" onchange="setSIEdited()"/></td>
					</tr>
					<c:if test="${preferences.modulesActivatedMap.mod_wardactivities == 'Y'}">
						<tr id="addActivityRow" >
							<td class="formlabel">Add Activity: </td>
							<td><input type="checkbox" name="s_ed_addActivity" id="s_ed_addActivity" value="AddActivity" onchange="setSIEdited()"></td>
						</tr>
					</c:if>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<input type="button" id="siOk" name="siok" value="Ok"/>
					<input type="button" id="siEditCancel" name="sicancel" value="Cancel" />
					<input type="button" id="siEditPrevious" name="siprevious" value="<<Previous" />
					<input type="button" id="siEditNext" name="sinext" value="Next>>" />
				</td>
			</tr>
		</table>

	</div>
</div>