<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title><insta:ltext key="patient.dialysis.medication.show.title"/></title>
	<insta:link type="script" file="DialysisMedications/treatmentChart.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="" />
	<meta name="i18nSupport" content="true"/>
	<style>
		.status_I {
			background-color: #dbe7f6;
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script>
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var mrNo = '${ifn:cleanJavaScript(param.mr_no)}';
	</script>
	<insta:js-bundle prefix="dialysismedications.treatmentchart"/>
</head>

	<body onload="initStandingtreatments();">

	<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
	</c:set>

		<h1 style="float: left"><insta:ltext key="patient.dialysis.medication.show.h1"/></h1>

		<insta:patientsearch searchType="" searchUrl="DialysisMedications.do" buttonLabel="Find"
			searchMethod="show" fieldName="mr_no" />

	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" showClinicalInfo="true"/>

	<form action="DialysisMedications.do" method="POSt" name="treatmentForm">
		<input type="hidden" name="_method" value="saveTreatmentChart" />
		<input type="hidden" name="org_id" id="org_id" value="" />
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
			<!-- starts HERE -->
				<div class="resultList">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.medication.show.patientpresc"/></legend>
				<%--	<div id="discontiuedDiv">
						<label>Hide Discontinued: </label>
						<input type="checkbox" name="hideDiscontinued" onclick="return hideDiscontinuedItems(this);">
					</div>--%>
					<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="siTable" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
						<tr>
							<th><insta:ltext key="patient.dialysis.medication.show.prescdate"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.type"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.name"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.form"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.strength"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.dosage"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.frequency"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.days"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.route"/></th>
							<th><insta:ltext key="patient.dialysis.medication.show.remarks"/></th>
							<th style="width: 16px"></th>
							<th style="width: 16px"></th>
						</tr>
						<c:set var="numTreatmentChart" value="${fn:length(treatmentChart)}"/>
						<c:forEach begin="1" end="${numTreatmentChart+1}" var="i" varStatus="loop">
							<c:set var="treatment" value="${treatmentChart[i-1].map}"/>
							<c:set var="flagColor">
								<c:choose>
									<c:when test="${treatment.discontinued == 'Y'}"><insta:ltext key="patient.dialysis.medication.show.grey"/></c:when>
									<c:otherwise><insta:ltext key="patient.dialysis.medication.show.empty"/></c:otherwise>
								</c:choose>
							</c:set>
							<c:if test="${empty treatment}">
								<c:set var="style" value='style="display:none"'/>
							</c:if>
							<tr ${style}>
								<td>
									<fmt:formatDate pattern="dd-MM-yyyy" value="${treatment.prescribed_date}" var="prescribed_date"/>
									<label>${prescribed_date}</label>
									<input type="hidden" name="s_prescribed_date" value="${prescribed_date}"/>
									<input type="hidden" name="s_prescription_id" value="${treatment.prescription_id}"/>
									<input type="hidden" name="s_item_name" value="<c:out value='${treatment.item_name}'/>"/>
									<input type="hidden" name="s_item_id" value="${treatment.item_id}"/>
									<input type="hidden" name="s_medicine_dosage" value="${treatment.medicine_dosage}"/>
									<input type="hidden" name="s_item_remarks" value="${ifn:cleanHtmlAttribute(treatment.remarks)}"/>
									<input type="hidden" name="s_item_master" value="${treatment.master}"/>
									<input type="hidden" name="s_freq_type" value="${treatment.freq_type}"/>
									<input type="hidden" name="s_recurrence_daily_id" value="${treatment.recurrence_daily_id}"/>
									<input type="hidden" name="s_consumption_uom" value="${treatment.consumption_uom}"/>
									<input type="hidden" name="s_delItem" id="s_delItem" value="false" />
									<input type="hidden" name="s_generic_code" value="${treatment.generic_code}"/>
									<input type="hidden" name="s_generic_name" value="${treatment.generic_name}"/>
									<input type="hidden" name="s_ispackage" value="${treatment.ispackage}"/>
									<input type="hidden" name="s_discontinued" value="${not empty treatment.discontinued ? treatment.discontinued : 'N'}">
									<input type="hidden" name="s_edited" value='false'/>
									<input type="hidden" name="s_route_id" value="${treatment.route_id}"/>
									<input type="hidden" name="s_route_name" value="${treatment.route_name}"/>
									<input type="hidden" name="s_days" value="${treatment.days}"/>
									<input type="hidden" name="s_qty_in_stock" value=""/>
									<input type="hidden" name="s_item_form_id" value="${treatment.item_form_id}">
									<input type="hidden" name="s_item_strength" value="${treatment.item_strength}">
									<c:choose>
										<c:when test="${treatment.type == 'M' || treatment.type == 'O'}">
											<c:set var="s_itemType" value="Medicine"/>
										</c:when>
									</c:choose>
									<input type="hidden" name="s_itemType" value="${s_itemType}"/>
								</td>
								<td>
									<label>${s_itemType}</label>
								</td>
								<td>
									<insta:truncLabel value="${treatment.item_name}" length="20"/>
								</td>
								<td>
									<insta:truncLabel value="${treatment.item_form_name}" length="20"/>
								</td>
								<td>
									<insta:truncLabel value="${treatment.item_strength}" length="20"/>
								</td>
								<td>
									<label>${treatment.medicine_dosage}</label>

								</td>
								<td>
									<label>${treatment.display_name}</label>
								</td>
								<td>
									<label>${treatment.days}</label>
								</td>
								<td>
									<label>${treatment.route_name}</label>

								</td>
								<td>
									<insta:truncLabel value="${treatment.remarks}" length="30"/>
								</td>
								<td style="text-align: center; ">
									<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelSIItem(this);" title='<insta:ltext key="patient.dialysis.medication.show.cancelitem"/>' >
										<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
									</a>
								</td>
								<td style="text-align: center">
									<a name="si_editAnchor" href="javascript:Edit" onclick="return showEditSIDialog(this);" title='<insta:ltext key="patient.dialysis.medication.show.edititem"/>'>
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
								<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="patient.dialysis.medication.show.add.treatment"/>'
									onclick="showAddSIDialog(this); return false;"
									accesskey="O" class="imgButton"><img src="${cpath}/icons/AddBlue.png"></button>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
			<div class="screenActions" style="float: left">
				<input type="submit" name="Save" value="Save" onclick="return validateSubmit();"/> |
				Patient<input type="radio" name="prescriptionTO" value="patient" ${filterType eq 'patient' ? 'checked' : ''}/>
				Visit<input type="radio" name="prescriptionTO" id="prescriptionTOvisit" value="visit" ${filterType eq 'visit' ? 'checked' : ''}/>
					<select class="dropdown" name="visitId" id="visitId">
						<option value=""><insta:ltext key="patient.dialysis.medication.show.visit"/></option>
						<c:forEach var="visit" items="${visitsList}">
							<option value="${visit.map.patient_id}" class="${visit.map.status == 'I' ? 'status_I' : ''}" ${filtervisitId eq visit.map.patient_id ? 'selected' : ''}>
									${visit.map.patient_id}(<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.map.reg_date}"/>)
							</option>
						</c:forEach>
					</select>
			</div>
			<fieldset style="width: 320px; float: right;" class="fieldsetborder"><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.medication.show.filter"/></legend>
				<div style="float: right;">
					<insta:ltext key="patient.dialysis.medication.show.patient"/><input type="radio" name="filterType" value="patient" ${filterType eq 'patient' ? 'checked' : ''}/>
					<insta:ltext key="patient.dialysis.medication.show.visit1"/><input type="radio" name="filterType" value="visit" ${filterType eq 'visit' ? 'checked' : ''}/>
					<select class="dropdown" name="filtervisitId" id="filtervisitId">
						<option value=""><insta:ltext key="patient.dialysis.medication.show.visit"/></option>
						<c:forEach var="visit" items="${visitsList}">
								<option value="${visit.map.patient_id}" ${filtervisitId eq visit.map.patient_id ? 'selected' : ''}>
								${visit.map.patient_id}(<fmt:formatDate pattern="dd-MM-yyyy" value="${visit.map.reg_date}"/>)
							</option>
						</c:forEach>
					</select> |
					<input type="submit" name="filter" value="Filter" onclick="return submitFilter();"/>
				</div>
			</fieldset>

			<div id="addSIDialog" style="display: none">
				<div class="bd">
					<div id="addSIDialogFieldsDiv">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.medication.show.addtreatment"/></legend>
							<table class="formtable">
								<tr>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.type"/>:</td>
									<td><select id="s_d_itemType" name="s_d_itemType" onchange="onSIItemChange();" class="dropdown">
										<option value="Medicine"><insta:ltext key="patient.dialysis.medication.show.medicine"/></option>
									</select></td>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.prescriptiondate"/></td>
									<td>
										<insta:datewidget name="s_d_prescribed_date" id="s_d_prescribed_date" value="today"/>
									</td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.item"/></td>
									<td colspan="3">
										<div id="s_d_itemAutocomplete" style="padding-bottom: 10px; width: 443px">
											<input type="text" id="s_d_itemName" name="s_d_itemName" >
											<div id="s_d_itemContainer" style="width: 500px" class="scrolForContainer"></div>
											<input type="hidden" name="s_d_item_master" id="s_d_item_master" value=""/>
											<input type="hidden" name="s_d_generic_name" id="s_d_generic_name" value=""/>
											<input type="hidden" name="s_d_generic_code" id="s_d_generic_code" value=""/>
											<input type="hidden" name="s_d_ispackage" id="s_d_ispackage" value=""/>
											<input type="hidden" name="s_d_item_id" id="s_d_item_id" value=""/>
											<input type="hidden" name="s_d_qty_in_stock" id="s_d_qty_in_stock" value=""/>
										</div>
										<font style="color: red; margin-left: 444px">*</font>
									</td>
								</tr>
								<tr >
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.hospitalitem"/></td>
									<td ><b><label id="s_d_itemMasterType"></label></b></td>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.generic"/></td>
									<td ><a id="s_d_genericNameAnchor_dialog" style="display: none"></a></td>
								</tr>
								<tr >
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.itemform"/></td>
									<td><insta:selectdb name="s_d_item_form_id" id="s_d_item_form_id" table="item_form_master"
										displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
										dummyvalue="${dummyvalue}" dummyvalueId="" disabled="disabled"/>
									</td>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.strength"/>:</td>
									<td><input type="text" name="s_d_item_strength" id="s_d_item_strength" value="" disabled="disabled"></td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.route"/>:</td>
									<td><select name="s_d_medicine_route" id="s_d_medicine_route" class="dropdown">
											<option value="">${dummyvalue}</option>
										</select>
									</td>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.dosageperadm"/></td>
									<td>
										<input type="text" name="s_d_dosage" id="s_d_dosage" style="width: 100px">
										<label id="s_d_medicineUOM"></label><font style="color: red">*</font>
										<img class="imgHelpText" src="${cpath}/images/help.png" title='<insta:ltext key="patient.dialysis.medication.show.dpatitle"/>'/>
										<inpu typ="hidden" name="s_d_consumption_uom" id="s_d_consumption_uom" value=""/>
									</td>
								</tr>
								<tr>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.frequency"/>:</td>
									<td>
										<select id="s_d_frequency_name" name="s_d_frequency_name" class="dropdown">
											<option value=""></option>
											<c:forEach var="frequency" items="${frequencies}">
												<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
											</c:forEach>
											<option value="0"><insta:ltext key="patient.dialysis.medication.show.custom"/></option>
										</select>
										<font style="color: red;">*</font>
									</td>
									<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.days"/>:</td>
									<td><input type="text" name="s_d_interval" id="s_d_interval" style="width: 100px" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
								</tr>
								<tr>
									<td class="formlabel" ><insta:ltext key="patient.dialysis.medication.show.remarks"/>:</td>
									<td colspan="3"><input type="text" name="s_d_remarks" id="s_d_remarks" value="" style="width: 443px"/></td>
								</tr>
							</table>
						</fieldset>
					</div>
					<table style="margin-top: 10">
						<tr>
							<td>
								<button type="button" name="SIAdd" id="SIAdd" ><insta:ltext key="patient.dialysis.medication.show.add"/></button>
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
					<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.medication.show.edittreatment"/></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.type"/></td>
							<td>
								<label id="s_ed_itemTypeLabel" style="font-weight: bold"></label>
								<input type="hidden" id="s_ed_itemType" name="s_ed_itemType" value=""/>
							</td>
<%-- 							<td class="formlabel">Presc. Date</td>
							<td class="forminfo"><label id="s_ed_presc_date"></label></td>--%>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.prescriptiondate"/></td>
							<td><insta:datewidget name="s_ed_presc_date" id="s_ed_presc_date" value="today"/></td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.item"/></td>
							<td colspan="3">
								<label id="s_ed_itemNameLabel" style="font-weight: bold"></label>
								<input type="hidden" id="s_ed_itemName" name="s_ed_itemName" value="">
								<input type="hidden" id="s_ed_item_master" name="s_ed_item_master" value=""/>
								<input type="hidden" id="s_ed_ispackage" name="s_ed_ispackage" value=""/>
								<input type="hidden" id="s_ed_item_id" name="s_ed_item_id" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.hospitalitem"/></td>
							<td ><b><label id="s_ed_itemMasterType"></label></b></td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.generic"/> </td>
							<td ><a id="s_ed_genericNameAnchor_editdialog" style="display:none"></a></td>
						</tr>
						<tr >
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.itemform"/> </td>
							<td><insta:selectdb name="s_ed_item_form_id" id="s_ed_item_form_id" table="item_form_master"
								displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
								dummyvalue="${dummyvalue}" dummyvalueId="" disabled="disabled"/>
							</td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.strength"/></td>
							<td><input type="text" name="s_ed_item_strength" id="s_ed_item_strength" value="" disabled="disabled"></td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.route"/></td>
							<td><b><label id="s_ed_medicine_route"></label></td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.dosageperadm"/></td>
							<td>
								<input type="text" name="s_ed_dosage" id="s_ed_dosage" value="" style="width: 100px" onchange="setSIEdited()"/>
								<label id="s_ed_medicineUOM"></label><font style="color: red">*</font>
								<img class="imgHelpText" src="${cpath}/images/help.png" title='<insta:ltext key="patient.dialysis.medication.show.dpatitle"/>'/>
								<input type="hidden" name="s_ed_consumption_uom" id="s_ed_consumption_uom" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.frequency"/></td>
							<td>
								<select id="s_ed_frequency_name" name="s_ed_frequency" class="dropdown" onchange="setSIEdited();">
									<option value=""></option>
									<c:forEach var="frequency" items="${frequencies}">
										<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
									</c:forEach>
									<option value="0"><insta:ltext key="patient.dialysis.medication.show.custom"/></option>
								</select>
								<font style="color: red;">*</font>
							</td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.days"/></td>
							<td><input type="text" name="s_ed_interval" id="s_ed_interval" onchange="setSIEdited()" style="width: 100px" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.remarks"/></td>
							<td colspan="3"><input type="text" name="s_ed_remarks" id="s_ed_remarks" onchange="setSIEdited()" value="" style="width: 443px;"></td>
						</tr>
					<%-- 	<tr>
							<td class="formlabel">Discountinue: </td>
							<td><input type="checkbox" name="s_ed_discontinue" id="s_ed_discontinue" onchange="setSIEdited()"/></td>
						</tr>--%>
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
	<div id="genericNameDisplayDialog"  style="visibility:hidden">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.medication.show.genericnamedetails"/></legend>
				<table border="0" class="formtable">
					<tr height="10px"></tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.genericname"/> </td>
						<td class="forminfo" style="width:8em"><b><label id="gen_generic_name"></label></b>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.classification"/> </td>
						<td class="forminfo" style="width:25em"><b><label id="classification_name"></label>	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.subclassification"/></td>
							<td class="forminfo" style="width:25em"><b><label id="sub_classification_name"></label>	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.standardadultdose"/></td>
							<td class="forminfo" style="width:25em"><b><label id="standard_adult_dose"></label>	</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.medication.show.criticality"/></td>
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
			<!-- ends HERE -->
		</form>
	</body>
</html>