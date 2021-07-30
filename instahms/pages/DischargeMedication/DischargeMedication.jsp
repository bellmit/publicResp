<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib  tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<fmt:formatDate pattern="dd-MM-yyyy" value="<%=new java.util.Date()%>" var="current_date"/>

<html>
<head>
	<title><insta:ltext key="patient.discharge.medication.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="i18nSupport" content="true"/>
	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script type="text/javascript">
	    var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var doctorsJson = <%= request.getAttribute("doctorsJSON") %>;
		var prescriptions_by_generics = '${prescriptions_by_generics}';
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var useGenerics = (use_store_items == 'Y' && prescriptions_by_generics == 'true');
		var medDosages = <%= request.getAttribute("medDosages") %>;
		var presInstructions = <%= request.getAttribute("presInstructions") %>;
		var health_authority = '${patient.health_authority}';
		var centerId = ${centerId};
		var planId = "${patient.plan_id}";
		var showChargesAllRatePlan = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
		var itemFormList = <%= request.getAttribute("itemFormList") %>;
		var cpath = "${cpath}";
		var visit_type = "${patient.visit_type}";
		var routesListJson = ${routes_list_json};
		var isDoctor = <%= request.getAttribute("isDoctor") %>;
		
	</script>
	<insta:js-bundle prefix="patient.addoreditadmissiondetails"/>
	<insta:link type="js" file="DischargeMedication/dischargemedication.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
</head>

<body class="yui-skin-sam" onload="ajaxForPrintUrls();${not empty patient ? 'init()' : 'getActiveOnlyPatients()'}">
<h1 style="float:left"><insta:ltext key="patient.discharge.medication.header"/></h1>

<c:set var="visitType"/>
<c:choose>
	<c:when test="${param.visit_type == 'IP'}">
		<c:set var="visitType" value="i"/>
	</c:when>
	<c:when test="${param.visit_type == 'OP'}">
		<c:set var="visitType" value="o"/>
	</c:when>
	<c:otherwise>
		<c:set var="visitType" value="all"/>
	</c:otherwise>
</c:choose>

<insta:patientsearch fieldName="visit_id" showStatusField="true" buttonLabel="Find" 
	searchUrl="dischargeMedication.do" searchMethod="getDischargeMedicationScreen" searchType="visit" visitType="${visitType}"/>
	
<insta:feedback-panel />
<c:if test="${not empty param.visit_id && empty feature_not_applicable}">
<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>
<form action="dischargeMedication.do" name="dischargemedicationform" method="POST" autocomplete="off">
	<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}"/>
	<input type="hidden" name="tpa_id" id="tpa_id" value="${patient.primary_sponsor_id}"/>
	<input type="hidden" name="bed_type" id="bed_type" value="${empty patient.alloc_bed_type ? patient.bill_bed_type : patient.alloc_bed_type}"/>
	<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}"/>
	<input type="hidden" name="medication_id" id="medication_id" value="${not empty dischargeMedicationBean ? dischargeMedicationBean.map.medication_id : ''}"/>
	<input type="hidden" name="_method" id="_method" value="saveDischargeMedication"/>
	<input type="hidden" name="isPrint" value="N"/>
	<table class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.discharge.medication.doctor"/>:</td>
		    <td colspan="5">
				<div style="width: 138px; padding-top: 10px">
					<input type="text" id="doctor_name" name="doctor_name" value="${not empty dischargeMedicationBean ? dischargeMedicationBean.map.doctor_name : defaultDoctorName}" style="width: 13em"/>
					<input type="hidden" id="doctor_id" name="doctor_id" value="${not empty dischargeMedicationBean ? dischargeMedicationBean.map.doctor_id : defaultDoctor}"/>
					<div class="scrolForContainer" id="doctorAcDropdown" style="width: 50em;"></div>
				</div>
				<span class="star" style="position: relative; top: -18px; left: 158px" >*</span>
		   </td>
		</tr>
	</table>
	<legend class="fieldSetLabel">Medication</legend>
	<div class="resultList">
		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%" style="margin-top: 8px">
			<tr>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.medicine.name"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.form"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.strength"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.admin.strength"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.details"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.route"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.instructions"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.special.instructions"/></th>
				<th><insta:ltext key="patient.discharge.medication.details.grid.label.qty"/></th>
				<th style="width: 16px;"></th>
				<th style="width: 16px"></th>
			</tr>
			<c:set var="numMedications" value="${fn:length(medicationDetails)}"/>
			<c:forEach begin="1" end="${numMedications+1}" var="i" varStatus="loop">
				<c:set var="medication" value="${medicationDetails[i-1].map}"/>
				<c:set var="item_issued" value="${not empty medication.issued ? medication.issued : 'N'}"/>
				<c:set var="item_issued" value="${item_issued == 'PA' or item_issued == 'O' ? 'O' : item_issued}"/>
				
				<c:if test="${empty medication}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<tr ${style}>
					<td>
						<c:choose>
							<c:when test="${empty medication.item_id}">
								<insta:truncLabel value="${medication.generic_name}" length="20"/>
							</c:when>
							<c:otherwise>
								<insta:truncLabel value="${medication.item_name}" length="20"/>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="item_prescribed_id" value="${medication.item_prescribed_id}"/>
						<c:choose>
							<c:when test="${empty medication.item_id}">
								<input type="hidden" name="item_name" value="<c:out value='${medication.generic_name}'/>"/>
								<input type="hidden" name="item_id" value="${medication.generic_code}"/>
							</c:when>
							<c:otherwise>
								<input type="hidden" name="item_name" value="<c:out value='${medication.item_name}'/>"/>
								<input type="hidden" name="item_id" value="${medication.item_id}"/>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="admin_strength" value="${medication.admin_strength}"/>
						<input type="hidden" name="strength" value="${medication.strength}"/>
						<input type="hidden" name="frequency" value="${medication.frequency}"/>
						<input type="hidden" name="duration" value="${medication.duration}"/>
						<input type="hidden" name="duration_units" value="${medication.duration_units}"/>
						<input type="hidden" name="medicine_quantity" value="${medication.medicine_quantity}"/>
						<input type="hidden" name="item_remarks" value="${medication.item_remarks}"/>
						<input type="hidden" name="special_instr" value="${medication.special_instr}"/>
						<input type="hidden" name="item_master" value="${medication.master}"/>
						<input type="hidden" name="delItem" id="delItem" value="false" />
						<input type="hidden" name="issued" value="${item_issued}"/>
						<input type="hidden" name="generic_code" value="${medication.generic_code}"/>
						<input type="hidden" name="generic_name" value="${medication.generic_name}"/>
						<input type="hidden" name="granular_units" value="${medication.granular_units}"/>
						<input type="hidden" name="edited" value='false'/>
						<input type="hidden" name="pkg_size" value=""/>
						<input type="hidden" name="drug_code" value="${medication.drug_code}"/>
						<input type="hidden" name="pkg_price" value=""/>
						<input type="hidden" name="route_id" value="${medication.route_id}"/>
						<input type="hidden" name="route_name" value="${medication.route_name}"/>
						<input type="hidden" name="consumption_uom" value="${medication.consumption_uom}"/>
						<input type="hidden" name="cons_uom_id" value="${medication.cons_uom_id}"/>
						<input type="hidden" name="qty_in_stock" value=""/>
						<input type="hidden" name="item_form_id" value="${medication.item_form_id == 0 ? '' : medication.item_form_id}"/>
						<input type="hidden" name="item_strength" value="${medication.item_strength}"/>
						<input type="hidden" name="item_strength_units" value="${medication.item_strength_units}"/>
					</td>
					<td>
						<insta:truncLabel value="${medication.item_form_name}" length="15"/>
					</td>
					<td>
						<insta:truncLabel value="${medication.item_strength} ${medication.unit_name}" length="15"/>
					</td>
					<td>
						<insta:truncLabel value="${medication.admin_strength}" length="15"/>
					</td>
					<td>
						<c:if test="${(not empty medication.medicine_dosage or not empty medication.duration)}">
							<insta:truncLabel value="${medication.medicine_dosage} / ${medication.duration} ${medication.duration_units} " length="20"/>
						</c:if>
					</td>
					<td>
						<label>
							${medication.route_name}
						</label>
					</td>
					<td>
						<insta:truncLabel value="${medication.item_remarks}" length="20"/>
					</td>
					<td>
						<insta:truncLabel value="${medication.special_instr}" length="20"/>
					</td>
					<td>
						<label>
								${medication.medicine_quantity}
						</label>
					</td>
					<td style="text-align: center">
						<c:choose>
							<c:when test="${item_issued == 'O'}">
								<img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
							</c:when>
							<c:otherwise>
								<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align: center">
						<input type="hidden" name="delPayment" id="delPayment" value="false" />
						<a name="_editAnchor" href="javascript:Edit" onclick="return showEditItemDialog(this);"
							title="Edit Medication Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<table class="addButton" style="height:25px;">
		<tr>
			<td style="text-align: right">
				<button type="button" name="btnAddItem" id="btnAddItem" title="Add Medication (Alt_Shift_+)"
					onclick="showAddItemDialog(this); return false;"
					accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
			</td>
			<td style="width: 16px;text-align: right">
				<button type="button" name="btnAddItem" id="btnAddItem" title="Copy All Visit Medicines To Discharge Medication"
					onclick="getAllVisitPrescribedMedicines(this); return false;"
					class="imgButton"><img src="${cpath}/icons/Send.png"></button>
			</td>
		</tr>
	</table>
	
<div class="screenActions" style="float: left">
	<button name="Save" value="Save" accessKey="S" onclick="return validateSubmit('N');"><b><u>S</u></b>ave</button>&nbsp;
	<button name="saveandPrint" accessKey="P" id="saveandPrint" onclick="return validateSubmit('Y');">Save & <b><u>P</u></b>rint</button>&nbsp;
	<c:if test="${patient.visit_type == 'i'}">
		<a href="${cpath}/ipemr/index.htm#/filter/default/patient/${patient.mr_no}/ipemr/visit/${patient.patient_id}?retain_route_params=true">
		 <insta:ltext key="ui.label.rename.ipemr"/>
		</a>
	</c:if>
 
</div>
<div class="clrboth"></div>

<div id="addItemDialog" style="display: none">
	<div class="bd">
		<div id="addItemDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.header"/></legend>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.fieldset.medicine.details"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel" ><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.medicine"/>: </td>
							<td colspan="3">
								<div style="float: left;">
									<div id="itemAutocomplete" style="padding-bottom: 20px; width: 350px">
										<input type="text" id="d_itemName" name="d_itemName" >
										<div id="itemContainer" style="width: 550px" class="scrolForContainer"></div>
										<input type="hidden" name="d_item_master" id="d_item_master" value=""/>
										<input type="hidden" name="d_generic_name" id="d_generic_name" value=""/>
										<input type="hidden" name="d_generic_code" id="d_generic_code" value=""/>
										<input type="hidden" name="d_drug_code" id="d_drug_code" value=""/>
										<input type="hidden" name="d_package_size" id="d_package_size" value=""/>
										<input type="hidden" name="d_price" id="d_price" value=""/>
										<input type="hidden" name="d_item_id" id="d_item_id" value=""/>
										<input type="hidden" name="d_qty_in_stock" id="d_qty_in_stock" value=""/>
										<input type="hidden" name="d_granular_units" id="d_granular_units" value=""/>
									</div>
								</div>

							</td>
						</tr>
						<tr >
							<td class="formlabel" ><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.generic"/>: </td>
							<td colspan="5"><a id="genericNameAnchor_dialog" style="display: none"></a></td>
						</tr>
						<tr id="d_drug_code_show">
							<td class="formlabel" ><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.drug.code"/>: </td>
							<td colspan="5"><b><label id="d_drug_code_label" ></label></b></td>
						</tr>
						<tr>
							<td class="formlabel" ><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.pkz.size"/>: </td>
							<td><b><label id="d_pkg_size_label"></label></b></td>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.price"/>: </td>
							<td ><b><label id="d_price_label"></label></b></td>
						</tr>
						<tr id="d_itemFormRow">
							<td class="formlabel" ><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.item.form"/>: </td>
							<td><insta:selectdb name="d_item_form_id" id="d_item_form_id" table="item_form_master"
								displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
								dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'd');"/>
							</td>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.strength"/></td>
							<td >
								<div style="width: 138px">
									<input type="text" name="d_item_strength" id="d_item_strength" value="" style="width: 60px;">
									<insta:selectdb name="d_item_strength_units" id="d_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
											dummyvalue="Select --" style="width: 60px;"/>
								</div>
							</td>
							<td class="formlabel"></td>
							<td></td>
						</tr>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.fieldset.management"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.admin.strength"/>: </td>
							<td>
								<input type="text" name="d_admin_strength" id="d_admin_strength" value="" maxlength="98"/>
							</td>
							<td></td>
							<td></td>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.route"/>: </td>
							<td>
								<select id="d_medicine_route" class="dropdown">
									<option value="">-- Select --</option>
								</select>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.no.of.units"/>: <span class="star">*</span></td>
							<td colspan="5">
								<input type="text" name="d_strength" id="d_strength" value="" onchange="setAutoGeneratedInstruction('d');"/>
								<insta:selectdb name="d_cons_uom_id" id="d_cons_uom_id" table="consumption_uom_master"
								displaycol="consumption_uom" valuecol="cons_uom_id" filtercol="status" filtervalue="A" orderby="consumption_uom"
								dummyvalue="-- Select --" dummyvalueId="" onchange="modifyUOMLabel(this, 'd'); setAutoGeneratedInstruction('d');"/><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.granular.units"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.duration"/>: </td>
							<td>
								<input type="text" name="d_duration" id="d_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);"
									onchange="calcQty('d'); setAutoGeneratedInstruction('d'); validateDuration('d');"/>
							</td>
							<td colspan="2" >
								<div style="width: 190px">
									<input type="radio" name="d_duration_units" value="D" onchange="calcQty('d'); setAutoGeneratedInstruction('d'); "><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.duration.units.options.days"/>
									<input type="radio" name="d_duration_units" value="W" onchange="calcQty('d'); setAutoGeneratedInstruction('d'); "><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.duration.units.options.weeks"/>
									<input type="radio" name="d_duration_units" value="M" onchange="calcQty('d'); setAutoGeneratedInstruction('d'); "><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.duration.units.options.months"/>
								</div>
							</td>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.frequency"/>: </td>
							<td>
								<div id="frequencyAutoComplete" style="width: 138px">
									<input type="text" name="d_frequency" id="d_frequency" maxlength="150" onchange="setAutoGeneratedInstruction('d');">
									<div id="frequencyContainer"></div>
									<input type="hidden" name="d_per_day_qty" id="d_per_day_qty" value=""/>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.total.qty"/>: </td>
							<td>
								<input type="text" name="d_qty" id="d_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/>
							</td>
							<td colspan="2"><label id="d_consumption_uom_label"></label></td>
						</tr>
						<tr>
							<td class="formlabel" ><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.instructions"/>: </td>
							<td colspan="5">
								<div id="remarksAutoComplete" style="width: 500px">
									<input type="text" name="d_remarks" id="d_remarks" value="" style="width: 500px">
									<div id="remarksContainer" class="scrolForContainer"></div>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.discharge.medication.add.medicine.dialog.label.special.instructions"/>: </td>
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
			<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.header"/></legend>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.fieldset.medicine.details"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.medicine"/>: </td>
						<td colspan="3">
							<label id="ed_itemNameLabel" style="font-weight: bold"></label>
							<input type="hidden" id="ed_itemName" name="ed_itemName" value="">
							<input type="hidden" id="ed_item_master" name="ed_item_master" value=""/>
							<input type="hidden" name="ed_package_size" id="ed_package_size" value=""/>
							<input type="hidden" name="ed_price" id="ed_price" value=""/>
							<input type="hidden" name="ed_item_id" id="ed_item_id" value=""/>
							<input type="hidden" name="ed_non_hosp_medicine" id="ed_non_hosp_medicine" value=""/>
							<input type="hidden" name="ed_granular_units" id="ed_granular_units" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.generic"/>: </td>
						<td colspan="5"><a id="genericNameAnchor_editdialog" style="display:none"></a></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.drug.code"/>: </td>
						<td colspan="5"><b><label id="ed_drug_code_label" ></label></b></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.pkz.size"/>: </td>
						<td><b><label id="ed_pkg_size_label"></label></b></td>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.price"/>: </td>
						<td><b><label id="ed_price_label"></label></b></td>
					</tr>
					<tr id="ed_itemFormRow">
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.item.form"/>: </td>
						<td><insta:selectdb name="ed_item_form_id" id="ed_item_form_id" table="item_form_master"
							displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
							dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'ed');"/>
						</td>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.strength"/></td>
						<td>
							<div style="width: 138px">
								<input type="text" name="ed_item_strength" id="ed_item_strength" value="" style="width: 60px" onchange="setEdited();">
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
				<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.fieldset.management"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.admin.strength"/>: </td>
						<td>
							<input type="text" name="ed_admin_strength" id="ed_admin_strength" value="" onchange="setEdited();" maxlength="98"/>
						</td>
						<td></td>
						<td></td>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.route"/>: </td>
						<td id="ed_route_dropdown_td">
							<select id="ed_medicine_route" class="dropdown">
								<option value="">-- Select --</option>
							</select>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.no.of.units"/>: </td>
						<td colspan="5">
							<input type="text" name="ed_strength" id="ed_strength" value="" onchange="setEdited(); setAutoGeneratedInstruction('ed');"/>
							<insta:selectdb name="ed_cons_uom_id" id="ed_cons_uom_id" table="consumption_uom_master"
								displaycol="consumption_uom" valuecol="cons_uom_id" filtercol="status" filtervalue="A" orderby="consumption_uom"
								dummyvalue="-- Select --" dummyvalueId="" onchange="modifyUOMLabel(this, 'ed');setAutoGeneratedInstruction('ed');setEdited();"/><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.granular.units"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.duration"/>: </td>
						<td><input type="text" name="ed_duration" id="ed_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed'); validateDuration('ed');" /></td>
						<td colspan="2">
							<div style="width: 190px">
								<input type="radio" name="ed_duration_units" value="D" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed'); "><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.duration.units.options.days"/>
								<input type="radio" name="ed_duration_units" value="W" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed'); "><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.duration.units.options.weeks"/>
								<input type="radio" name="ed_duration_units" value="M" onchange="setEdited(); calcQty('ed'); setAutoGeneratedInstruction('ed'); "><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.duration.units.options.months"/>
							</div>
						</td>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.frequency"/>: </td>
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
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.total.qty"/>: </td>
						<td >
							<input type="text" name="ed_qty" id="ed_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited();"/>
						</td>
						<td colspan="2"><label id="ed_consumption_uom_label"></label></td>
						<td class="formlabel"></td>
						<td></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.instructions"/>: </td>
						<td colspan="5">
							<div id="ed_remarksAutoComplete" style="width: 500px">
								<input type="text" name="ed_remarks" id="ed_remarks" value="" style="width: 500px" onchange="setEdited();">
								<div id="ed_remarksContainer" class="scrolForContainer"></div>
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.edit.medicine.dialog.label.special.instructions"/>: </td>
						<td colspan="5"><textarea name="ed_special_instruction" id="ed_special_instruction" style="width: 500px;" cols="50" rows="2" onchange="setEdited();"></textarea></td>
					</tr>
				</table>
				<table style="margin-top: 10">
					<tr>
						<td>
							<input type="button" id="editOk" name="editok" value="Ok" ${isPharmacist eq 'true' ? 'disabled' : ''}>
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
			<legend class="fieldSetLabel"><insta:ltext key="patient.discharge.medication.genericnamedetails"/></legend>
			<table border="0" class="formtable">
				<tr height="10px"></tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.discharge.medication.generic"/>&nbsp;<insta:ltext key="patient.discharge.medication.name"/>: </td>
					<td class="forminfo" style="width:8em"><b><label id="gen_generic_name"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.discharge.medication.classification"/>: </td>
					<td class="forminfo" style="width:25em"><b><label id="classification_name"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.discharge.medication.sub_classification"/>:</td>
						<td class="forminfo" style="width:25em"><b><label id="sub_classification_name"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.discharge.medication.standardadultdose"/>:</td>
						<td class="forminfo" style="width:25em"><b><label id="standard_adult_dose"></label>	</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.discharge.medication.criticality"/>:</td>
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
<div id="patientAllVisitPrescribedMedicinesDiv" style="display: none">
	<div class="hd">Copy Visit Prescribed Medicines</div>
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">All Prescribed Medicines</legend>
			<div style="margin-top: 10px">
				<div id="medProgressbar" style="margin-top: 10px; font-weight: bold">
					Loading.. please wait..
				</div>
				<div style="float:right; margin-right: 10px" id="medicationPaginationDiv" name="paginationDiv">
				</div>
			</div>
			<div style="clear:both"></div>
			<div style="clear:both"></div>
			<div class="resultList" style="width: 780px" >
				<table class="resultList" id="visitMedicinesTable" cellspacing="0" cellpadding="0" style="margin: top: 10px;width: 780px">
					<tr>
						<th width="20px">Select</th>
						<th>Medicine Name</th>
						<th>Form</th>
						<th>Strength</th>
						<th>Admin Strength</th>
						<th>Details</th>
						<th>Route</th>
						<th>Instructions</th>
						<th>Special Instructions</th>
						<th>Qty</th>
					</tr>
					<tr style="display:none">
						<td></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td><label></label></td>
						<td>
							<label></label>

							<input type="hidden" name="med_item_name" value=""/>
							<input type="hidden" name="med_item_id" value=""/>
							<input type="hidden" name="med_strength" value=""/>
							<input type="hidden" name="med_admin_strength" value=""/>
							<input type="hidden" name="med_granular_units" value=""/>
							<input type="hidden" name="med_drug_code" value=""/>
							<input type="hidden" name="med_frequency" value=""/>
							<input type="hidden" name="med_duration" value=""/>
							<input type="hidden" name="med_duration_units" value=""/>
							<input type="hidden" name="med_medicine_quantity" value=""/>
							<input type="hidden" name="med_item_remarks" value=""/>
							<input type="hidden" name="med_special_instr" value=""/>
							<input type="hidden" name="med_item_master" value=""/>
							<input type="hidden" name="med_generic_code" value=""/>
							<input type="hidden" name="med_generic_name" value=""/>
							<input type="hidden" name="med_edited" value='false'/>
							<input type="hidden" name="med_delItem" id="delItem" value="false" />
							<input type="hidden" name="med_route_id" value=""/>
							<input type="hidden" name="med_route_name" value=""/>
							<input type="hidden" name="med_consumption_uom" value=""/>
							<input type="hidden" name="med_cons_uom_id" value=""/>
							<input type="hidden" name="med_item_form_id" value=""/>
							<input type="hidden" name="med_item_form_name" value=""/>
							<input type="hidden" name="med_item_strength" value=""/>
							<input type="hidden" name="med_item_strength_units" value=""/>
							<input type="hidden" name="med_item_strength_unit_name" value=""/>
						</td>
					</tr>
					<tr style="display: none; background-color:#FFC">
						<td colspan="10"><img id="npId" src="${cpath}/images/alert.png"/> No prescription available.</td>
					</tr>
				</table>
			</div>
		</fieldset>
		<table >
			<tr>
				<td><input type="button" id="ok_btn" name="ok_btn" value="Ok"></td>
				<td><input type="button" name="close_btn" id="close_btn" value="Close"/></td>
			</tr>
		</table>
	</div>
</div>
</form>
</c:if>
</body>
</html>