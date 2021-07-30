<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Luteal Phase Support - Insta HMS</title>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="script" file="ivf/cyclecompletion.js" />
	<insta:link type="script" file="ivf/lutealSupport.js"/>
	<insta:link type="script" file="DialysisMedications/treatmentChart.js"/>
	<insta:link type="script" file="ivf/ivfsessions.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<script>
	var use_store_items = '${genericPrefs.prescription_uses_stores}';
</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<body onload="init();" "yui-skin-sam">
<div class="pageHeader">Luteal Phase Support</div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${param.mr_no}" addExtraFields="true"/>
<form name="treatmentForm" method="post" action="${cpath}/IVF/IVFCycleCompletion.do" autocomplete="off">
<input type="hidden" name="_method" value="saveLutealDetails"/>
<input type="hidden" name="ivf_cycle_id" id="ivf_cycle_id" value="${ifn:cleanHtmlAttribute(param.ivf_cycle_id)}"/>
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
<input type="hidden" name="patient_id" id="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
	<input type="hidden" name="org_id" id="org_id" value="" />
	<fieldset>
		<table class="dashboard" id="LHLTable">
			<tr class="header">
				<td>Date</td>
				<td>E2</td>
				<td>Prog</td>
				<td>BetaHCG</td>
				<td>&nbsp;</td>
			</tr>
			<tr id="" style="display: none">
			</tr>
			<tr>
				<td colspan="4"></td>
				<td>
					<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRowLHL(this)" >
						<img src="${cpath}/icons/Add.png" align="right"/>
					</button>
				</td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Patient Prescription</legend>
		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="siTable" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
						<tr>
							<th>Presc Date</th>
							<th>Type</th>
							<th>Name</th>
							<th>Form</th>
							<th>Strength</th>
							<th>Dosage</th>
							<th>Frequency</th>
							<th>Days</th>
							<th>Route</th>
							<th>Remarks</th>
							<th style="width: 16px"></th>
							<th style="width: 16px"></th>
						</tr>
						<c:set var="numTreatmentChart" value="${fn:length(treatmentChart)}"/>
						<c:forEach begin="1" end="${numTreatmentChart+1}" var="i" varStatus="loop">
							<c:set var="treatment" value="${treatmentChart[i-1].map}"/>
							<c:set var="flagColor">
								<c:choose>
									<c:when test="${treatment.discontinued == 'Y'}">grey</c:when>
									<c:otherwise>empty</c:otherwise>
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
									<a name="trashCanAnchor" href="javascript:Cancel Item" onclick="return cancelSIItem(this);" title="Cancel Item" >
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
								<button type="button" name="btnAddItem" id="btnAddItem" title="Add Standing treatment (Alt_Shift_O)"
									onclick="showAddSIDialog(this); return false;"
									accesskey="O" class="imgButton"><img src="${cpath}/icons/AddBlue.png"></button>
							</td>
						</tr>
					</table>
	</fieldset>


	<div class="screenActions">
		<input type="button" value="Save" class="button" onclick="return funValidateAndSubmit();" />
		| <a href="${cpath}/IVF/IVFCycleCompletion.do?_method=show&mr_no=${ifn:cleanURL(param.mr_no)}&patient_id=${ifn:cleanURL(param.patient_id)}
		&ivf_cycle_id=${ifn:cleanURL(param.ivf_cycle_id)}">Cycle Completion</a>
	</div>

	<div id="addSIDialog" style="display: none">
				<div class="bd">
					<div id="addSIDialogFieldsDiv">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Add Treatment</legend>
							<table class="formtable">
								<tr>
									<td class="formlabel">Type: </td>
									<td><select id="s_d_itemType" name="s_d_itemType" onchange="onSIItemChange();" class="dropdown">
										<option value="Medicine">Medicine</option>
									</select></td>
									<td class="formlabel">Prescription Date:</td>
									<td>
										<insta:datewidget name="s_d_prescribed_date" id="s_d_prescribed_date" value="today"/>
									</td>
								</tr>
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
								<tr >
									<td class="formlabel">Item Form: </td>
									<td><insta:selectdb name="s_d_item_form_id" id="s_d_item_form_id" table="item_form_master"
										displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
										dummyvalue="-- Select --" dummyvalueId="" disabled="disabled"/>
									</td>
									<td class="formlabel">Strength</td>
									<td><input type="text" name="s_d_item_strength" id="s_d_item_strength" value="" disabled="disabled"></td>
								</tr>
								<tr>
									<td class="formlabel">Route: </td>
									<td><select name="s_d_medicine_route" id="s_d_medicine_route" class="dropdown">
											<option value="">-- Select --</option>
										</select>
									</td>
									<td class="formlabel">Dosage per ADM.: </td>
									<td>
										<input type="text" name="s_d_dosage" id="s_d_dosage" style="width: 100px">
										<label id="s_d_medicineUOM"></label><font style="color: red">*</font>
										<img class="imgHelpText" src="${cpath}/images/help.png" title="Dosage per Administration"/>
										<inpu typ="hidden" name="s_d_consumption_uom" id="s_d_consumption_uom" value=""/>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Frequency: </td>
									<td>
										<select id="s_d_frequency_name" name="s_d_frequency_name" class="dropdown">
											<option value=""></option>
											<c:forEach var="frequency" items="${frequencies}">
												<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
											</c:forEach>
											<option value="0">Custom</option>
										</select>
										<font style="color: red;">*</font>
									</td>
									<td class="formlabel">Days: </td>
									<td><input type="text" name="s_d_interval" id="s_d_interval" style="width: 100px" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
								</tr>
								<tr>
									<td class="formlabel" >Remarks: </td>
									<td colspan="3"><input type="text" name="s_d_remarks" id="s_d_remarks" value="" style="width: 443px"/></td>
								</tr>
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
					<legend class="fieldSetLabel">Edit Treatment</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Type: </td>
							<td>
								<label id="s_ed_itemTypeLabel" style="font-weight: bold"></label>
								<input type="hidden" id="s_ed_itemType" name="s_ed_itemType" value=""/>
							</td>
<%-- 							<td class="formlabel">Presc. Date</td>
							<td class="forminfo"><label id="s_ed_presc_date"></label></td>--%>
							<td class="formlabel">Prescription Date:</td>
							<td><insta:datewidget name="s_ed_presc_date" id="s_ed_presc_date" value="today"/></td>
						</tr>
						<tr>
							<td class="formlabel">Item: </td>
							<td colspan="3">
								<label id="s_ed_itemNameLabel" style="font-weight: bold"></label>
								<input type="hidden" id="s_ed_itemName" name="s_ed_itemName" value="">
								<input type="hidden" id="s_ed_item_master" name="s_ed_item_master" value=""/>
								<input type="hidden" id="s_ed_ispackage" name="s_ed_ispackage" value=""/>
								<input type="hidden" id="s_ed_item_id" name="s_ed_item_id" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Hospital Item: </td>
							<td ><b><label id="s_ed_itemMasterType"></label></b></td>
							<td class="formlabel">Generic: </td>
							<td ><a id="s_ed_genericNameAnchor_editdialog" style="display:none"></a></td>
						</tr>
						<tr >
							<td class="formlabel">Item Form: </td>
							<td><insta:selectdb name="s_ed_item_form_id" id="s_ed_item_form_id" table="item_form_master"
								displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
								dummyvalue="-- Select --" dummyvalueId="" disabled="disabled"/>
							</td>
							<td class="formlabel">Strength</td>
							<td><input type="text" name="s_ed_item_strength" id="s_ed_item_strength" value="" disabled="disabled"></td>
						</tr>
						<tr>
							<td class="formlabel">Route: </td>
							<td><b><label id="s_ed_medicine_route"></label></td>
							<td class="formlabel">Dosage per ADM.: </td>
							<td>
								<input type="text" name="s_ed_dosage" id="s_ed_dosage" value="" style="width: 100px" onchange="setSIEdited()"/>
								<label id="s_ed_medicineUOM"></label><font style="color: red">*</font>
								<img class="imgHelpText" src="${cpath}/images/help.png" title="Dosage per Administration"/>
								<input type="hidden" name="s_ed_consumption_uom" id="s_ed_consumption_uom" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Frequency: </td>
							<td>
								<select id="s_ed_frequency_name" name="s_ed_frequency" class="dropdown" onchange="setSIEdited();">
									<option value=""></option>
									<c:forEach var="frequency" items="${frequencies}">
										<option value="${frequency.map.recurrence_daily_id}">${frequency.map.display_name}</option>
									</c:forEach>
									<option value="0">Custom</option>
								</select>
								<font style="color: red;">*</font>
							</td>
							<td class="formlabel">Days: </td>
							<td><input type="text" name="s_ed_interval" id="s_ed_interval" onchange="setSIEdited()" style="width: 100px" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
						</tr>
						<tr>
							<td class="formlabel">Remarks: </td>
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
</form>
<script>
		var harmonelevelsDetails = ${lutealHarmoneDetails};
</script>
</body>
</html>