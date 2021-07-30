<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<html>
<head>
	<title>Edit Dose Details</title>

	<insta:link type="js" file="/masters/vaccineMaster.js"/>
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="hmsvalidation.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<script>
		cpath = '${cpath}';
		var single_dose = '${vaccineBean.map.single_dose}';
		var inactivatedDoseNos = '${inactivatedDoseNos}';
	</script>
</head>
	<body onload="init()">
	<h1>Add/Edit Dose Details</h1>
	<form name="searchForm" method="GET">
		<input type="hidden" name="_method" value="showDossageDetails" />
		<input type="hidden" name="vaccine_id" value="${vaccineBean.map.vaccine_id}"/>
		<insta:search-lessoptions form="searchForm" >
			<div class="searchBasicOpts">
				<div class="sboField" style="height: 70px" class="last">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>

					</div>
				</div>
			</div>
		</insta:search-lessoptions>
	</form>
	<form name="dosageForm" action="vaccineMaster.do" method="POST" >
		<input type="hidden" name="_method" value="saveDosageValues" />
		<input type="hidden" name="vaccine_id" value="${vaccineBean.map.vaccine_id}"/>
		<input type="hidden" name="single_dose" value="${vaccineBean.map.single_dose}" />
		<table class="formtable"  width="100%">
			<tr>
				<td align="right" width="15%" style="padding-bottom: 30px; padding-top: 30px ">
					 <label style="font-size: 19px">Vaccine Name:</label>
				</td>
				<td align="left" width="15%">
					<label style="font-size: 19px">${vaccineBean.map.vaccine_name}</label>
				</td>
				<td width="35%">&nbsp;</td>
				<td width="35%">&nbsp;</td>
			</tr>
		</table>
		<fieldSet class="fieldsetborder">
		<legend class="fieldSetLabel">Dosages</legend>
			<table class="detailList dialog_displayColumns" id="dosageTable" style="empty-cells: show;margin-top: 5px">
				<tr>
					<th>Dose</th>
					<th>Recommended Age</th>
					<th>Notification Lead Time(days)</th>
					<th style="width: 16px"></th>
					<th style="width: 16px"></th>
				</tr>
				<c:set var="dosageListSize" value="${fn:length(dosageList)}" />
				<c:forEach begin="1" end="${dosageListSize + 1}" var="index">
					<c:set var="dosageBean" value="${dosageList[index-1]}" />
					<c:if test="${empty dosageBean}">
						<c:set var="style" value="style='display: none'"/>
					</c:if>
					<tr ${style} id="dosage_${dosageBean.dose_num}">
						<td>
							<c:choose>
								<c:when test="${dosageBean.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:when>
								<c:otherwise><img src='${cpath}/images/empty_flag.gif'></c:otherwise>
							</c:choose>
							<label>${dosageBean.dose_num}</label>
							<input type="hidden" name="dose_num" value="${dosageBean.dose_num}" />
							<input type="hidden" name="recommended_age" value="${dosageBean.recommended_age}" />
							<input type="hidden" name="age_units" value="${dosageBean.age_units}" />
							<input type="hidden" name="notification_lead_time_days" value="${dosageBean.notification_lead_time_days}" />
							<input type="hidden" name="status" value="${dosageBean.status}" />
							<input type="hidden" name="is_new" value="${empty dosageBean.vaccine_dose_id ? 'Y' : 'N'}" />
							<input type="hidden" name="is_edited" value="${not empty dosageBean.vaccine_dose_id ? 'Y' : 'N'}" />
							<input type="hidden" name="is_deleted" value="N" />
							<input type="hidden" name="vaccine_dose_id" value="${dosageBean.vaccine_dose_id}" />
						</td>
						<td><label>${dosageBean.recommended_age}&nbsp; ${dosageBean.recommended_age eq 1 ? (dosageBean.age_units eq 'W' ? 'Week' : (dosageBean.age_units eq 'M' ? 'Month' : 'Year')) : (dosageBean.age_units eq 'W' ? 'Weeks' : (dosageBean.age_units eq 'M' ? 'Months' : 'Years')) }</label></td>
						<td>${dosageBean.notification_lead_time_days}&nbsp;days</td>
						<td style="text-align: center">
							<a name="dosage_editAnchor" href="#Edit Dosage Details" onclick="return showEditDosageDialog(this);" title="Edit Dosage Details">
								<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</td>
						<td style="text-align: center">
                            <c:choose>
                                <c:when test="${index eq dosageListSize }">
                                    <a id="dosageDeleteLink_${dosageBean.dose_num}" name="dosage_editAnchor" href="#Delete Dosage Details" onclick="return showDeleteDosageDetailsDialog(this);" title="Delete Dosage Details">
                                        <img id="dosageDeleteIcon_${dosageBean.dose_num}" src="${cpath}/icons/Delete.png" class="button" />
                                    </a>
                                </c:when>
                                <c:otherwise>
                                    <a id="dosageDeleteLink_${dosageBean.dose_num}" style="pointer-events: none" name="dosage_editAnchor" href="#Delete Dosage Details" onclick="return showDeleteDosageDetailsDialog(this);" title="Delete Dosage Details">
                                        <img id="dosageDeleteIcon_${dosageBean.dose_num}" src="${cpath}/icons/Delete1.png" class="button" />
                                    </a>
                                </c:otherwise>
                            </c:choose>
                        </td>
					</tr>
				</c:forEach>
			</table>
			<table class="addButton" >
				<tr>
					<td></td>
					<td width="16px" style="text-align: center">
						<button type="button" name="btnAddItem" id="btnAddItem" title="Add Dosage(Alt_Shift_O)"
							onclick="showAddDosageDialog(this);"
							accesskey="O" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</td>
					</tr>
			</table>
		</fieldSet>
		<c:url var="vaccinesListURL" value="/master/vaccineMaster.do">
			<c:param name="_method" value="list" />
			<c:param name="sortOrder" value="display_order" />
			<c:param name="sortReverse" value="false" />
			<c:param name="status" value="A"/>
		</c:url>
		<div class="screenActions" style="float: left;">
			<input type="submit" name="submit" id="submit" onclick="return validate()" value="Save"/> |
			<a href="${vaccinesListURL}" >Vaccines List</a>
		</div>
		<div class="legend" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
		<div id="addDosageDialog" style="display: none">
			<div class="bd">
				<div id="addDosageFormFieldsDiv">
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Add Dosage</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Dose: </td>
								<td>
									<c:if test="${vaccineBean.map.single_dose eq 'N'}">
										<input type="text" name="d_a_dose" id="d_a_dose" class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
									</c:if>
								</td>
								<td></td>
							</tr>
							<tr>
								<td class="formlabel">Recommended Age: </td>
								<td colspan="2"><input type="text" name="d_a_recommended_age" id="d_a_recommended_age" onkeypress="return enterNumOnlyzeroToNine(event)" class="number"/>
									<input type="radio" name="d_a_age_in" id="d_a_age_in" checked value="W"/> Weeks
									<input type="radio" name="d_a_age_in" value="M" > Months
									<input type="radio" name="d_a_age_in" value="Y"> Years
								</td>
							</tr>
							<tr>
								<td class="formlabel" >Notification Lead Time: </td>
								<td>
									<input type="text" name="d_a_notification_lead_time" id="d_a_notification_lead_time" onkeypress="return enterNumOnlyzeroToNine(event)" class="number"/>
									<label>days</label>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><insta:selectoptions name="d_a_status" id="d_a_status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
							</tr>
						</table>
					</fieldset>
				</div>
				<table>
					<input type="button" name="DosageAdd" id="DosageAdd" value="Add"/>
					<input type="button" name="DosageCancel" id="DosageCancel" onclick="handleAddDosageCancel();" value="Cancel"/>
				</table>
			</div>
		</div>
		<div id="editDosageDialog" style="display: none">
			<div class="bd">
				<div id="editDosageDialogFields">
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Dosage</legend>
						<table class="formtable">
							<input type="hidden" name="dosage_edit_row_id" id="dosage_edit_row_id" value="" />
							<tr>
								<td class="formlabel">Dose: </td>
								<td>
									<c:if test="${vaccineBean.map.single_dose eq 'N'}">
										<input type="text" name="d_e_dose" id="d_e_dose" class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
									</c:if>
								</td>
								<td></td>
							</tr>
							<tr>
								<td class="formlabel">Recommended Age: </td>
								<td colspan="2"><input type="text" name="d_e_recommended_age" id="d_e_recommended_age" onkeypress="return enterNumOnlyzeroToNine(event)" class="number">
									<input type="radio" name="d_e_age_in" id="d_e_age_in" value="W"/> Weeks
									<input type="radio" name="d_e_age_in" value="M" > Months
									<input type="radio" name="d_e_age_in" value="Y"> Years
								</td>
							</tr>
							<tr>
								<td class="formlabel" >Notification Lead Time: </td>
								<td><input type="text" name="d_e_notification_lead_time" id="d_e_notification_lead_time" onkeypress="return enterNumOnlyzeroToNine(event)" class="number"/></td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><insta:selectoptions name="d_e_status" id="d_e_status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
							</tr>
						</table>
					</fieldset>
				</div>
				<table>
					<input type="button" name="ed_Add" id="ed_Add" value="Update"/>
					<input type="button" name="ed_Cancel" id="ed_Cancel" value="Cancel" onclick="handleEditDosageCancel()"/>
					<input type="button" id="ed_Previous" name="ed_Previous" value="<<Previous" />
					<input type="button" id="ed_Next" name="ed_Next" value="Next>>" />
				</table>
			</div>
		</div>
		<div id="deleteDosageDialog" style="display: none">
		    <div class="bd">
                <div id="deleteDosageDialogFields">
                    <fieldset class="fieldSetBorder">
                        <legend class="fieldSetLabel">Delete Dosage Dialog</legend>
                        <input type="hidden" name="dosage_delete_row_id" id="dosage_delete_row_id" value="" />
                        <p id="deleteText" style="padding-top: 10px; padding-bottom: 10px"></p>
                    </fieldset>
                </div>
                <table>
                    <input type="button" name="Confirm" id="deleteConfirm" value="Confirm"/>
                    <input type="button" id="Cancel" name="deleteCancel" value="Cancel" onclick="handleDeleteDosageCancel()" />
                </table>
            <div>
		</div>
	</form>

	</body>
</html>
