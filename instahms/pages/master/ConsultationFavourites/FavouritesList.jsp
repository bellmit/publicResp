<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<title>Manage Consultation Favourites-Insta HMS</title>
	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script>
		var prescriptions_by_generics = '${prescriptions_by_generics}';
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var medDosages = ${medDosages};
		var presInstructions = <%= request.getAttribute("presInstructions") %>;
		var itemFormList = <%= request.getAttribute("itemFormList") %>;
		var cpath = '${cpath}';
		var routesListJson = ${routes_list_json};
		var max_centers = '${genericPrefs.max_centers_inc_default}';
		var centerId = '${center_id_js}';
	</script>
	<insta:link type="script" file="master/ConsultationFavourites/consultation_favourites.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>

</head>
<body onload="init();">
	<h1>Manage Consultation Favourites - ${doctor_bean.map.doctor_name}</h1>
	<table width="100%">
		<tr>
			<% 	int max_centers = (Integer) request.getAttribute("Max_centers_inc_default");
				int centerId = (Integer) request.getAttribute("center_id_js");

				if (max_centers>1 && centerId == 0)
					request.setAttribute("error", "Manage Consultation Favourites allowed only for center users.");

			%>
			<td><insta:feedback-panel/></td>
		</tr>
	</table>
	<form name="favouritesForm" action="ConsultationFavourites.do" method="POST">
		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(param.doctor_id)}"/>
		<div class="resultList">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Favourites</legend>
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%">
					<tr>
						<th>Type</th>
						<th class="number" width="50px">Display Order</th>
						<th>Name</th>
						<th>Form</th>
						<th>Strength</th>
						<th>Admin Strength</th>
						<th>Details</th>
						<th>Route</th>
						<th>Instructions</th>
						<th>Special Instructions</th>
						<th>Qty</th>
						<th style="width: 16px;"></th>
						<th style="width: 16px"></th>
					</tr>
					<c:set var="numfavourites" value="${fn:length(all_favourites)}"/>
					<c:forEach begin="1" end="${numfavourites+1}" var="i" varStatus="loop">
						<c:set var="favourite" value="${all_favourites[i-1].map}"/>
						<c:if test="${empty favourite}">
							<c:set var="style" value='style="display:none"'/>
						</c:if>
						<tr ${style}>
							<td>
								<img src="${cpath}/images/empty_flag.gif"/>
								<label>${favourite.item_type}
									<c:if test="${favourite.item_type == 'Medicine' && favourite.non_hosp_medicine}">
										[Non Hosp]
									</c:if>
								</label>
								<input type="hidden" name="favourite_id" value="${favourite.favourite_id}"/>
								<input type="hidden" name="itemType" value="${favourite.item_type}"/>
								<c:choose>
									<c:when test="${favourite.item_type == 'Medicine' && empty favourite.item_id && not empty favourite.generic_name}">
										<input type="hidden" name="item_name" value="<c:out value='${favourite.generic_name}'/>"/>
										<input type="hidden" name="item_id" value="${favourite.generic_code}"/>
									</c:when>
									<c:otherwise>
										<input type="hidden" name="item_name" value="<c:out value='${favourite.item_name}'/>"/>
										<input type="hidden" name="item_id" value="${favourite.item_id}"/>
									</c:otherwise>
								</c:choose>
								<input type="hidden" name="admin_strength" value="${favourite.admin_strength}"/>
								<input type="hidden" name="granular_units" value="${favourite.granular_units}"/>
								<input type="hidden" name="strength" value="${favourite.strength}"/>
								<input type="hidden" name="frequency" value="${ifn:cleanHtmlAttribute(favourite.frequency)}"/>
								<input type="hidden" name="duration" value="${favourite.duration}"/>
								<input type="hidden" name="duration_units" value="${favourite.duration_units}"/>
								<input type="hidden" name="medicine_quantity" value="${favourite.medicine_quantity}"/>
								<input type="hidden" name="item_remarks" value="${favourite.item_remarks}"/>
								<input type="hidden" name="special_instr" value="${ifn:cleanHtmlAttribute(favourite.special_instr)}"/>
								<input type="hidden" name="item_master" value="${favourite.master}"/>
								<input type="hidden" name="ispackage" id="ispackage" value="${favourite.ispackage}"/>
								<input type="hidden" name="generic_code" value="${favourite.generic_code}"/>
								<input type="hidden" name="generic_name" value="${favourite.generic_name}"/>
								<input type="hidden" name="edited" value='false'/>
								<input type="hidden" name="delItem" id="delItem" value="false" />
								<input type="hidden" name="route_id" value="${favourite.route_id}"/>
								<input type="hidden" name="route_name" value="${favourite.route_name}"/>
								<input type="hidden" name="cons_uom_id" value="${favourite.cons_uom_id}"/>
								<input type="hidden" name="item_form_id" value="${favourite.item_form_id == 0 ? '' : favourite.item_form_id}"/>
								<input type="hidden" name="item_strength" value="${favourite.item_strength}"/>
								<input type="hidden" name="item_strength_units" value="${favourite.item_strength_units}"/>
								<input type="hidden" name="display_order" value="${favourite.display_order}"/>
								<input type="hidden" name="non_hosp_medicine" value="${favourite.non_hosp_medicine}"/>
								<input type="hidden" name="presc_by_generics" value="${(favourite.item_type == 'Medicine' && empty favourite.item_id) ? 'true' : 'false'}" />
							</td>
							<td class="number">${favourite.display_order}</td>
							<td>
								<c:choose>
									<c:when test="${favourite.item_type == 'Medicine' && empty favourite.item_id && not empty favourite.generic_name}">
										<insta:truncLabel value="${favourite.generic_name}" length="20"/>
									</c:when>
									<c:otherwise>
										<insta:truncLabel value="${favourite.item_name}" length="20"/>
									</c:otherwise>
								</c:choose>
							</td>
							<td>
								<insta:truncLabel value="${favourite.item_form_name}" length="15"/>
							</td>
							<td>
								<insta:truncLabel value="${favourite.item_strength} ${favourite.unit_name}" length="15"/>
							</td>
							<td>
								<insta:truncLabel value="${favourite.admin_strength}" length="15"/>
							</td>
							<td>
								<c:if test="${(favourite.item_type == 'Medicine' || favourite.item_type == 'NonHospital') &&
												(not empty favourite.medicine_dosage or not empty favourite.duration)}">
											<insta:truncLabel value="${favourite.medicine_dosage} / ${favourite.duration} ${favourite.duration_units}" length="20"/>
								</c:if>
							</td>
							<td>
								<label>
									<c:if test="${favourite.item_type == 'Medicine'}">
										${favourite.route_name}
									</c:if>
								</label>
							</td>
							<td>
								<insta:truncLabel value="${favourite.item_remarks}" length="30"/>
							</td>
							<td>
								<insta:truncLabel value="${favourite.special_instr}" length="30"/>
							</td>
							<td>
								<label><c:if test="${favourite.item_type == 'Medicine'}">
										${favourite.medicine_quantity}
										</c:if>
								</label>
							</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</td>
							<td style="text-align: center">
								<input type="hidden" name="delPayment" id="delPayment" value="false" />
								<a name="_editAnchor" href="javascript:Edit" onclick="return showEditItemDialog(this);"
									title="Edit One Time favourite Details">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</td>
						</tr>
					</c:forEach>
				</table>
				<table class="addButton" style="height: 25px;">
					<tr>
						<td >&nbsp;</td>
						<td style="width: 16px; text-align: center">
							<button type="button" name="btnAddItem" id="btnAddItem" title="Add Favourites (Alt_Shift_+)"
								onclick="return showAddItemDialog(this); return false;"
								accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table class="screenActions">
			<tr>
				<td>
					<input type="button" name="save" value="Save" onclick="return validateOnSave();" />
					<c:url value="/master/DoctorMaster.do" var="doctorListUrl">
						<c:param name="_method" value="list"/>
						<c:param name="status" value="A"/>
						<c:param name="sortOrder" value="doctor_name"/>
						<c:param name="sortReverse" value="false"/>
						<c:param name="org_id" value="ORG0001"/>
					</c:url>
					| <a href="${doctorListUrl}" title="Doctors List">Doctors List</a>
				</td>
			</tr>
		</table>
	</form>
	<div id="addItemDialog" style="display: none">
		<div class="bd">
			<div id="addItemDialogFields">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Add Item</legend>
					<fieldset>
						<legend class="fieldSetLabel">Item Type</legend>
						<table>
							<tr>
								<td ><input type="radio" name="d_itemType" value="Medicine" checked onclick="onItemChange()"> Medicine</td>
								<td ><input type="radio" name="d_itemType" value="Inv." onclick="onItemChange()"> Investigation</td>
								<td ><input type="radio" name="d_itemType" value="Service" onclick="onItemChange()"> Service</td>
							</tr>
							<tr>
								<td ><input type="radio" name="d_itemType" value="Operation" onclick="onItemChange()"> Operation</td>
								<td><input type="radio" name="d_itemType" value="Doctor" onclick="onItemChange()"> Doctor Consultation</td>
								<td><input type="radio" name="d_itemType" value="NonHospital" onclick="onItemChange()"> Non Hospital Items</td>
							</tr>
						</table>
					</fieldset>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Item Details</legend>
						<table class="formtable">
							<tr id="d_non_hosp_medicine_div">
								<td class="formlabel">Non Hospital: </td>
								<td><input type="checkbox" id="d_non_hosp_medicine" value="NM" onchange="clearFieldsWhenChanged();"></td>
							</tr>
							<tr>
								<td class="formlabel">Item: </td>
								<td colspan="5">
									<div style="float: left;">
										<div id="itemAutocomplete" style="padding-bottom: 20px; width: 300px">
											<input type="text" id="d_itemName" name="d_itemName" >
											<div id="itemContainer" style="width: 500px" class="scrolForContainer"></div>
											<input type="hidden" name="d_item_master" id="d_item_master" value=""/>
											<input type="hidden" name="d_generic_name" id="d_generic_name" value=""/>
											<input type="hidden" name="d_generic_code" id="d_generic_code" value=""/>
											<input type="hidden" name="d_ispackage" id="d_ispackage" value=""/>
											<input type="hidden" name="d_package_size" id="d_package_size" value=""/>
											<input type="hidden" name="d_price" id="d_price" value=""/>
											<input type="hidden" name="d_item_id" id="d_item_id" value=""/>
											<input type="hidden" name="d_granular_units" id="d_granular_units" value=""/>
										</div>
									</div>
								</td>
							</tr>
							<tr >
								<td class="formlabel">Generic: </td>
								<td colspan="5"><a id="genericNameAnchor_dialog" style="display: none"></a></td>
							</tr>
							<tr id="d_itemFormRow">
								<td class="formlabel">Item Form: </td>
								<td><insta:selectdb name="d_item_form_id" id="d_item_form_id" table="item_form_master"
									displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
									dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'd');"/>
								</td>
								<td class="formlabel">Strength</td>
								<td>
									<div style="width: 138px">
										<input type="text" name="d_item_strength" id="d_item_strength" value="" style="width: 60px">
										<insta:selectdb name="d_item_strength_units" id="d_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
											dummyvalue="Select --" style="width: 60px"/>
									</div>
								</td>
								<td class="formlabel"></td>
								<td></td>
							</tr>
						</table>
					</fieldset>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Management</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Admin Strength: </td>
								<td>
									<input type="text" name="d_admin_strength" id="d_admin_strength" value="" maxlength="98"/>
								</td>
								<td></td>
								<td></td>
								<td class="formlabel">Route: </td>
								<td>
									<select id="d_medicine_route" class="dropdown">
										<option value="">-- Select --</option>
									</select>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Number Of Units (Dosage): </td>
								<td colspan="5">
									<input type="text" name="d_strength" id="d_strength" value="" onchange="calcQty('d'); setAutoGeneratedInstruction('d');"/>
									<insta:selectdb name="d_cons_uom_id" id="d_cons_uom_id" table="consumption_uom_master" value="${d_cons_uom_id}"
          											displaycol="consumption_uom" valuecol="cons_uom_id" orderby="consumption_uom"
          											dummyvalue="-- Select --" dummyvalueId="" onchange="modifyUOMLabel(this, 'd'); setAutoGeneratedInstruction('d');"/>Granular Units
									<!-- <input type="text" name="d_consumption_uom" id="d_consumption_uom" value="" onchange="modifyUOMLabel(this, 'd'); setAutoGeneratedInstruction('d');"/>Granular Units --> 
								</td>
							</tr>
							<tr>
								<td class="formlabel">Duration: </td>
								<td>
									<input type="text" name="d_duration" id="d_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="calcQty('d'); setAutoGeneratedInstruction('d');" />
								</td>
								<td colspan="2" >
									<div style="width: 190px">
										<input type="radio" name="d_duration_units" value="D" onchange="calcQty('d'); setAutoGeneratedInstruction('d');">Days
										<input type="radio" name="d_duration_units" value="W" onchange="calcQty('d'); setAutoGeneratedInstruction('d');">Weeks
										<input type="radio" name="d_duration_units" value="M" onchange="calcQty('d'); setAutoGeneratedInstruction('d');">Months
									</div>
								</td>
								<td class="formlabel">Frequency: </td>
								<td>
									<div id="frequencyAutoComplete" style="width: 138px">
										<input type="text" name="d_frequency" id="d_frequency"  maxlength="150" onchange="setAutoGeneratedInstruction('d');">
										<div id="frequencyContainer"></div>
										<input type="hidden" name="d_per_day_qty" id="d_per_day_qty" value=""/>
									</div>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Total Qty: </td>
								<td>
									<input type="text" name="d_qty" id="d_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/>
								</td>
								<td colspan="2"><label id="d_consumption_uom_label"></label></td>
								<td class="formlabel">Display Order: </td>
								<td><input type="text" name="d_display_order" id="d_display_order" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
							</tr>
							<tr>
								<td class="formlabel" >Instructions: </td>
								<td colspan="5">
									<div id="remarksAutoComplete" style="width: 500px">
										<input type="text" name="d_remarks" id="d_remarks" value="" style="width: 500px">
										<div id="remarksContainer" class="scrolForContainer"></div>
									</div>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Special Instructions: </td>
								<td colspan="5"><textarea name="d_special_instruction" id="d_special_instruction" style="width: 500px;" cols="50" rows="2" ></textarea></td>
							</tr>
						</table>
				</fieldset>
			</div>
			<table style="margin-top: 10">
				<tr>
					<td>
						<button type="button" name="Add" id="Add" accesskey="A" >
							<b><u>A</u></b>dd
						</button>
						<input type="button" name="Close" value="Close" id="Close"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="editItemDialog" style="display: none">
		<input type="hidden" name="editRowId" id="editRowId" value=""/>
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Item</legend>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Item Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Type: </td>
							<td colspan="5">
								<label id="ed_itemTypeLabel" style="font-weight: bold"></label>
								<input type="hidden" id="ed_itemType" name="ed_itemType" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Item: </td>
							<td colspan="5">
								<label id="ed_itemNameLabel" style="font-weight: bold"></label>
								<input type="hidden" id="ed_itemName" name="ed_itemName" value="">
								<input type="hidden" id="ed_item_master" name="ed_item_master" value=""/>
								<input type="hidden" id="ed_ispackage" name="ed_ispackage" value=""/>
								<input type="hidden" name="ed_package_size" id="ed_package_size" value=""/>
								<input type="hidden" name="ed_price" id="ed_price" value=""/>
								<input type="hidden" name="ed_item_id" id="ed_item_id" value=""/>
								<input type="hidden" name="ed_non_hosp_medicine" id="ed_non_hosp_medicine" value=""/>
								<input type="hidden" name="ed_granular_units" id="ed_granular_units" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Generic: </td>
							<td colspan="5"><a id="genericNameAnchor_editdialog" style="display:none"></a></td>
						</tr>
						<tr id="ed_itemFormRow">
							<td class="formlabel">Item Form: </td>
							<td><insta:selectdb name="ed_item_form_id" id="ed_item_form_id" table="item_form_master"
								displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
								dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'ed');"/>
							</td>
							<td class="formlabel">Strength</td>
							<td>
								<div style="width: 138px">
									<input type="text" name="ed_item_strength" id="ed_item_strength" value="" style="width: 60px">
									<insta:selectdb name="ed_item_strength_units" id="ed_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
										dummyvalue="Select --" style="width: 60px" onchange="setEdited();"/>
								</div>
							</td>
							<td class="formlabel"></td>
							<td></td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Management</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Admin Strength: </td>
							<td>
								<input type="text" name="ed_admin_strength" id="ed_admin_strength" value="" onchange="setEdited();" maxlength="98"/>
							</td>
							<td></td>
							<td></td>
							<td class="formlabel">Route: </td>
							<td><b><label id="ed_medicine_route"></label></b></td>
						</tr>
						<tr>
							<td class="formlabel">Number Of Units (Dosage): </td>
							<td colspan="5">
								<input type="text" name="ed_strength" id="ed_strength" value="" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');"/>
								<insta:selectdb name="ed_cons_uom_id" id="ed_cons_uom_id" table="consumption_uom_master" value="${ed_cons_uom_id}"
          											displaycol="consumption_uom" valuecol="cons_uom_id" orderby="consumption_uom"
          											dummyvalue="-- Select --" dummyvalueId="" onchange="modifyUOMLabel(this, 'ed');setEdited(); setAutoGeneratedInstruction('ed');"/>Granular Units
								<!-- <input type="text" name="ed_consumption_uom" id="ed_consumption_uom" value="" onchange="modifyUOMLabel(this, 'ed');setEdited(); setAutoGeneratedInstruction('ed');"/> Granular Units -->
							</td>
						</tr>
						<tr>
							<td class="formlabel">Duration: </td>
							<td><input type="text" name="ed_duration" id="ed_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');" /></td>
							<td colspan="2">
								<div style="width: 190px">
									<input type="radio" name="ed_duration_units" value="D" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');">Days
									<input type="radio" name="ed_duration_units" value="W" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');">Weeks
									<input type="radio" name="ed_duration_units" value="M" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed');">Months
								</div>
							</td>
							<td class="formlabel">Frequency: </td>
							<td>
								<div id="ed_frequencyAutoComplete" style="width: 138px;">
									<input type="text" name="ed_frequecy" id="ed_frequency" value="" maxlength="150" onchange="setEdited(); setAutoGeneratedInstruction('ed'); "/>
									<input type="hidden" name="ed_frequency_hidden" id="ed_frequency_hidden" value=""/>
									<input type="hidden" name="ed_per_day_qty" id="ed_per_day_qty" value=""/>
									<div id="ed_frequencyContainer"></div>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Total Qty: </td>
							<td >
								<input type="text" name="ed_qty" id="ed_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited();"/>
							</td>
							<td colspan="2"><label id="ed_consumption_uom_label"></label></td>
							<td class="formlabel">Display Order: </td>
							<td><input type="text" name="ed_display_order" id="ed_display_order" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
						</tr>
						<tr>
							<td class="formlabel">Instructions: </td>
							<td colspan="5">
								<div id="ed_remarksAutoComplete" style="width: 500px">
									<input type="text" name="ed_remarks" id="ed_remarks" value="" style="width: 500px" onchange="setEdited();">
									<div id="ed_remarksContainer" class="scrolForContainer"></div>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Special Instructions: </td>
							<td colspan="5"><textarea name="ed_special_instruction" id="ed_special_instruction" style="width: 443px;" cols="50" rows="2" onchange="setEdited();"></textarea></td>
						</tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<input type="button" id="editOk" name="editok" value="Ok">
								<input type="button" id="editCancel" name="cancel" value="Cancel" />
								<input type="button" id="editPrevious" name="previous" value="<<Previous" />
								<input type="button" id="editNext" name="next" value="Next>>"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</fieldset>
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
</body>
</html>