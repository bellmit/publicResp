<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<style>
	span.note {
	background:url("/icons/std-cancel.png") ;
	color:#FFFFFF;
	display:block;
	font-size:12px;
	font-weight:bold;
	height:25px;
	left:0;
	letter-spacing:-1px;
	line-height:25px;
	position:absolute;
	text-align:center;
	text-decoration:none;
	text-shadow:0 1px 1px rgba(0, 0, 0, 0.15);
	top:-2px;
	width:25px;
	z-index:10;
	}

	span.note.black{
	background-position: -150px 0;
	}

	</style>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link file="jquery-1.4.4.min.js" type="script"/>
	<insta:link file="annotate/jquery.annotate.js" type="script"/>
	<insta:link file="opthalmology/doctoreyeexam.js" type="script"/>
	<insta:link file="opthalmology/optometristexam.js" type="script" />
	<insta:link type="script" file="outpatient/prescribe.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>

<insta:js-bundle prefix="patient.consultation"/>
<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
</head>
	<body onload="initItemDialog(); initDosageAutoComplete(); initEditItemDialog();">
	<insta:patientdetails visitid="${param.patient_id}"/>
	<form action="${cpath}/opthalmology/DoctorEyeExam.do" name="prescribeForm" method="POST">

		<input type="hidden" name="_method" value="saveDoctorEyeExamDetails" />
		<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
		<input type="hidden" name="doctor_id" value="${ifn:cleanHtmlAttribute(param.doctor_id)}" />
		<input type="hidden" name="consult_id" value="${consultId}" />
		<input type="hidden" name="overall_exam_id" value="${eyeBean.map.overall_exam_id }" />
		<input type="hidden" name="lens_exam_id" value="${lensBean.map.lens_exam_id}" />
		<input type="hidden" name="fundus_exam_id" value="${fundusBean.map.fundus_exam_id}" />

		<input type="hidden" name="insertOrupdate" value="${not empty mainBean ? mainBean.map.doctor_exam_id : 'insert'}" />

		<c:set var="date"><fmt:formatDate value="${presentDate}" pattern="dd-MM-yyyy hh:mm" /></c:set>
				<input type="hidden" name="start_time" value="${ifn:cleanHtmlAttribute(date)}" />
				<input type="hidden" name="end_time" value="${ifn:cleanHtmlAttribute(date)}" />

		<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}"/>
		<table >
		<tr>
			<td style="padding-left: 0px">
				<div id="CollapsiblePanel1" class="CollapsiblePanel" style="padding-bottom:9px;">
					<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
						<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Eye Examination &gt;&gt;</font></div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;  padding-left: 7px">
								<img src="${cpath}/images/down.png" />
							</div>
						<div class="clrboth"></div>
					</div>
				<fieldset class="fieldsetBorder">
				<table width="50%" align="center" style="padding-bottom: 8px">
					<tr>
						<td><h1>Right Eye</h1></td>
					</tr>
					<tr  >
						<td ></td>
						<td ><img src="../icons/eyepart1.png" style="width: 400px;padding-left: 28px"/></td>
						<td valign="middle"><input type="text" name="" id="" value="${eyeBean.map.choroid_re}" maxlength="50"/></td>
					</tr>

					<tr>
						<td><input type="text" name="cornea_re" value="${eyeBean.map.cornea_re}" maxlength="50"/></td>
						<td ><div id="righteye"><img src="../icons/eyepart2.png" style="width: 424px;padding-left: 20px" /></div></td>
						<td valign="top"><input type="text" name="retina_re" value="${eyeBean.map.retina_re}" maxlength="50" /></td>
					</tr>
					<tr>
						<td><input type="text" name="pupil_re" value="${eyeBean.map.pupil_re}" maxlength="50" /></td>
						<td><img src="../icons/eyepart3.png" style="width: 429px;padding-left: 11px"></td>
						<td valign="top"><input type="text" name="fovea_re" value="${eyeBean.map.fovea_re}" maxlength="50"/></td>
					</tr>
					<tr >
						<td valign="bottom"><input type="text" name="lens_re" value="${eyeBean.map.lens_re}" maxlength="50"/></td>
						<td><img src="../icons/eyepart4.png" style="width: 433px; padding-left: 12px" /></td>
					</tr>
					<tr>
						<td></td>
						<td><img src="../icons/eyepart5.png" style="width: 425px;padding-left: 11px" /></td>
					</tr>
					<tr>
						<td valign="top"><input type="text" name="iris_re" value="${eyeBean.map.iris_re}" maxlength="50" /></td>
						<td><img src="../icons/eyepart6.png" style="width: 427px; padding-left: 25px" ></td>
					</tr>
					<tr>
						<td  ></td>
						<td ><img src="../icons/eyepart7.png" style="width: 143px;padding-left: 163px"/></td>
						<td  ></td>
					</tr>
					<tr >
						<td colspan="2" width="60%" style="padding-left: 27px;"> <input type="text" name="ciliary_re" value="${eyeBean.map.ciliary_re}" maxlength="50" /></td>
						<td colspan="2" style="padding-right: 34px"><input type="text" name="optnerve_re" value="${eyeBean.map.optnerve_re}" maxlength="50"/></td>
					</tr>
				</table>


				<fieldset class="fieldsetBorder">
				<table width="50%" align="center">
					<tr>
						<td><h1>Left Eye</h1></td>
					</tr>
					<tr  >
						<td ></td>
						<td ><img src="../icons/eyepart1.png" style="width: 400px;padding-left: 28px"/></td>
						<td valign="middle"><input type="text" name="choroid_le" id="" value="${eyeBean.map.choroid_le}" maxlength="50"/></td>
					</tr>
					<tr>
						<td><input type="text" name="cornea_le" value="${eyeBean.map.cornea_le}" maxlength="50"/></td>
						<td ><img src="../icons/eyepart2.png" style="width: 424px;padding-left: 20px"/></td>
						<td valign="top"><input type="text" name="retina_le" value="${eyeBean.map.retina_le}" maxlength="50"/></td>
					</tr>
					<tr>
						<td><input type="text" name="pupil_le" value="${eyeBean.map.pupil_le}" maxlength="50"/></td>
						<td><img src="../icons/eyepart3.png" style="width: 429px;padding-left: 11px"></td>
						<td valign="top"><input type="text" name="fovea_le" value="${eyeBean.map.fovea_le}" maxlength="50"/></td>
					</tr>
					<tr >
						<td valign="bottom"><input type="text" name="lens_le" value="${eyeBean.map.lens_le}" maxlength="50" /></td>
						<td><img src="../icons/eyepart4.png" style="width: 433px; padding-left: 12px" /></td>
					</tr>
					<tr>
						<td></td>
						<td><img src="../icons/eyepart5.png" style="width: 425px;padding-left: 11px" /></td>
					</tr>
					<tr>
						<td valign="top"><input type="text" name="iris_le" value="${eyeBean.map.iris_le}" maxlength="50"/></td>
						<td><img src="../icons/eyepart6.png" style="width: 427px; padding-left: 25px" ></td>
					</tr>
					<tr>
						<td  ></td>
						<td ><img src="../icons/eyepart7.png" style="width: 143px;padding-left: 163px"/></td>
						<td  ></td>
					</tr>
					<tr >
						<td colspan="2" width="60%" style="padding-left: 27px;"> <input type="text" name="ciliary_le" value="${eyeBean.map.ciliary_le}" maxlength="50" /></td>
						<td colspan="2" style="padding-right: 34px"><input type="text" name="optnerve_le" value="${eyeBean.map.optnerve_le}" maxlength="50"/></td>
					</tr>
				</table>
				<table width="100%">
					<tr>
						<td class="formlabel">Notes:</td>
					</tr>
					<tr>
						<td><textarea name="eye_notes" id="" style="width: 100%;height: 60px" onblur="return restrictLength(this, 200)">${eyeBean.map.notes}</textarea></td>
					</tr>
				</table>
				</fieldset>
			</div>
			</td>
			</tr>
		</table>
		</fieldset>

		<table >
		<tr>
			<td style="padding-left: 0px">
				<div id="CollapsiblePanel2" class="CollapsiblePanel" style="padding-bottom:9px;">
					<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
						<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Lens Examination >></font></div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;  padding-left: 7px">
								<img src="${cpath}/images/down.png" />
							</div>
						<div class="clrboth"></div>
					</div>

				<fieldset class="fieldsetBorder">
				<table align="center" width="100%">
				<tr >
					<td><h1>Lens Examination</h1></td>
				</tr>
				<tr >
					<td><h1 style="padding-left: 193px">RE</h1></td>
					<td>&nbsp;</td>
					<td><h1 style="padding-left: 123px">LE</h1></td>
				</tr>
				<tr >
					<td>
						<img src="${cpath}/icons/lens1.png" style="width: 85px; height: 250px; padding-left: 140px" />
					</td>
					<td>&nbsp;</td>
					<td>
						<img src="${cpath}/icons/lens1.png" style="width: 85px; height: 250px; padding-left: 70px"/>
					</td>
				</tr>
				</table>
				<table  class="formtable">
					<tr>
						<td style="padding-left: 40px">Notes</td>
						<td>&nbsp;</td>
						<td style="padding-left: 40px">Notes</td>

					</tr>
					<tr>
						<td style="padding-left: 40px"><textarea name="lens_re_notes" id="" style="width: 90%; height: 60px;" onblur="return restrictLength(this, 200)">${lensBean.map.lens_re_notes}</textarea></td>
						<td>&nbsp;</td>
						<td style="padding-left: 40px"><textarea name="lens_le_notes" id="" style="width: 90%; height: 60px;" onblur="return restrictLength(this, 200)">${lensBean.map.lens_le_notes}</textarea></td>
					</tr>
				</table>

				</fieldset>
				</div>
			</td>
			</tr>
		</table>

		<table >
		<tr>
			<td style="padding-left: 0px">
				<div id="CollapsiblePanel3" class="CollapsiblePanel" style="padding-bottom:9px;">
					<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
						<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Fundus Examination >></font></div>
							<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;  padding-left: 7px">
								<img src="${cpath}/images/down.png" />
							</div>
						<div class="clrboth"></div>
					</div>

				<fieldset class="fieldsetBorder">
				<table width="100%" align="center">
				<tr><td><h1>Fundus Examination</h1></td>
				<tr>
					<td><h1 style="padding-left: 195px">RE</h1></td>
					<td>&nbsp;</td>
					<td><h1 style="padding-left: 195px">LE</h1></td>
				</tr>
				<tr>

					<td>
						<img src="../icons/Fundus1.png" style="width: 300px;padding-left: 70px" />
					</td>
					<td>&nbsp;</td>
					<td>
						<img src="../icons/Fundus1.png" style="width: 300px;padding-left: 70px" />
					</td>
					<td>&nbsp;</td>
				</tr>
				</table>
				<table class="formtable" width="100%">

				<tr>
					<td class="formlabel">Optic disk</td>
					<td><input type="text" name="opticdisk_re" id="" value="${fundusBean.map.opticdisk_re}" maxlength="50" /></td>
					<td class="formlabel">Optic disk</td>
					<td><input type="text" name="opticdisk_le" id="" value="${fundusBean.map.opticdisk_le}" maxlength="50"/></td>

				</tr>
				<tr>
					<td class="formlabel">CD ratio</td>
					<td><input type="text" name="cdratio_re" id="" class="number" value="${fundusBean.map.cdratio_re}" maxlength="50"/></td>
					<td class="formlabel">CD ratio</td>
					<td><input type="text" name="cdratio_le" id="" class="number" value="${fundusBean.map.cdratio_le}" maxlength="50"/></td>
				</tr>
				<tr>
					<td class="formlabel">Notes</td>
					<td> <textarea name="notes_re" id="" style="width: 60%; height: 40px" onblur ="return restrictLength(this, 200)">${fundusBean.map.notes_re}</textarea></td>
					<td class="formlabel">Notes</td>
					<td> <textarea name="notes_le" id="" style="width: 60%; height: 40px" onblur="return restrictLength(this, 200)">${fundusBean.map.notes_le}</textarea> </td>
				</tr>


				</table>

			</fieldset>
			</div>
			</td>
			</tr>
		</table>
		<table width="100%">
			<tr>
				<td>Overall Doctor Notes</td>
			</tr>
			<tr>
				<td><textarea name="doctor_notes" id="" style="width: 100%; height: 60px" onblur="return restrictLength(this, 200)">${mainBean.map.doctor_notes}</textarea></td>
			</tr>
		</table>
		<table>
			<tr><td>&nbsp;</td></tr>
			<tr><td>Consultation Complete:</td>
				<td><input type="checkbox" name="statusCheckbox"  id="statusCheckbox" ${otMainBean.map.status == 'S' ? 'checked' : ''} onclick="setStatus(this, 'S');"
						${otMainBean.map.status == 'S' ? 'disabled' : ''}/></td>
				<input type="hidden" name="status" id="status" value="${otMainBean.map.status}" />
			</tr>
			<tr><td>&nbsp;</td></tr>
		</table>



		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Prescription</legend>
		<table class="detailList" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%">
			<tr>
				<th>Type</th>
				<th>Name</th>
				<th>Details</th>
				<th>Qty</th>
				<th>Remarks</th>
				<th style="width: 15px">&nbsp;</th>
				<th style="width: 15px">&nbsp;</th>
			</tr>
			<c:set var="numPrescriptions" value="${fn:length(prescriptions)}"/>
			<c:forEach begin="1" end="${numPrescriptions+1}" var="i" varStatus="loop">
				<c:set var="prescription" value="${prescriptions[i-1].map}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${not empty issued}">grey</c:when>
						<c:when test="${empty issued}">empty</c:when>
					</c:choose>
				</c:set>
				<c:if test="${empty prescription}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<tr ${style}>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<label>${prescription.item_type}</label>
						<input type="hidden" name="item_prescribed_id" value="${prescription.item_prescribed_id}"/>
						<input type="hidden" name="itemType" value="${prescription.item_type}"/>
						<input type="hidden" name="item_name" value="${prescription.item_name}"/>
						<input type="hidden" name="medicine_dosage" value="${prescription.medicine_dosage}"/>
						<input type="hidden" name="medicine_days" value="${prescription.medicine_days}"/>
						<input type="hidden" name="medicine_quantity" value="${prescription.medicine_quantity}"/>
						<input type="hidden" name="item_remarks" value="${prescription.item_remarks}"/>
						<input type="hidden" name="item_master" value="${prescription.master}"/>
						<input type="hidden" name="delItem" id="delItem" value="false" />
						<input type="hidden" name="generic_code" id="generic_code" value="${prescription.generic_code}"/>
						<input type="hidden" name="generic_name" id="generic_name" value="${prescription.generic_name}"/>
						<input type="hidden" name="ispackage" id="ispackage" value="${prescription.ispackage}"/>
						<input type="hidden" name="issued" value="${not empty prescription.issued ? prescription.issued : 'N'}"/>
						<input type="hidden" name="edited" value='false'/>
					</td>
					<td>
						<label>${prescription.item_name}</label>

					</td>
					<td>
						<label><c:if test="${prescription.item_type == 'Medicine'}">
									${prescription.medicine_dosage} / ${prescription.medicine_days} ${not empty prescription.medicine_days ? 'days' : ''}
								</c:if>
						</label>
					</td>
					<td>
						<label><c:if test="${prescription.item_type == 'Medicine'}">
								${prescription.medicine_quantity}
								</c:if>
						</label>
					</td>
					<td>
						<label>${prescription.item_remarks}</label>
					</td>
					<td style="text-align: center">
						<c:choose>
							<c:when test="${prescription.issued != 'Y'}">
								<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</c:when>
							<c:otherwise>
								<img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
							</c:otherwise>
						</c:choose>
					</td>
					<td style="text-align: center">
						<input type="hidden" name="delPayment" id="delPayment" value="false" />
						<c:choose>
							<c:when test="${prescription.issued != 'Y'}">
						   		<a name="_editAnchor" href="javascript:Edit" onclick="return showEditItemDialog(this);" title="Edit Item Details">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</c:when>
							<c:otherwise>
									<img src="${cpath}/icons/Edit1.png" class="button"  />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:forEach>
			<tr>
				<td colspan="6" style="text-align: right">&nbsp;</td>
				<td style="text-align: center">
					<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Item (Alt_Shift_+)"
						onclick="showAddItemDialog(this); return false;"
						accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
		</table>
		</fieldset>
		<fieldset class="fieldSetBorder" style="margin-top: 5px">


		<div class="screenActions">
			<input type="submit" value="Save"  />
			|
			<a href="${cpath }/opthalmology/OpthalmologyTestsList.do?_method=list&status=">Patient List</a>
			| <a href="${cpath}/opthalmology/OpthalmologyTestsList.do?_method=show&patient_id=${ifn:cleanURL(param.patient_id)}&mr_no=${ifn:cleanURL(param.mr_no)}&doctor_id=${ifn:cleanURL(param.doctor_id)}">Optometrist Exam</a>
			| <a href="${cpath}/emr/EMRMainDisplay.do?_method=list&VisitId=${ifn:cleanURL(param.patient_id)}&mr_no=${ifn:cleanURL(param.mr_no)}">Latest test Reports</a>
			| <a href="${cpath}/emr/EMRMainDisplay.do?_method=list&mr_no=${ifn:cleanURL(param.mr_no)}">EMR View</a>
		</div>

		<div id="addItemDialog" style="display: none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Prescription</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Type: </td>
						<td><select id="d_itemType" name="d_itemType" onchange="onItemChange();" class="dropdown">
							<option value="Medicine">Medicine</option>
							<option value="Test">Test</option>
							<option value="Service">Service</option>
							<option value="Doctor">Doctor Consultation</option>
						</select></td>
					</tr>
					<tr>
						<td class="formlabel">Item: </td>
						<td>
							<div id="itemAutocomplete" style="padding-bottom: 20px;">
								<input type="text" id="d_itemName" name="d_itemName" >
								<div id="itemContainer" style="width: 350px" class="scrolForContainer"></div>
								<input type="hidden" name="d_item_master" id="d_item_master" value=""/>
								<input type="hidden" name="d_generic_name" id="d_generic_name" value=""/>
								<input type="hidden" name="d_generic_code" id="d_generic_code" value=""/>
								<input type="hidden" name="d_ispackage" id="d_ispackage" value=""/>
							</div>
						</td>
						<td class="formlabel">Dosage: </td>
						<td>
							<div id="dosageAutoComplete" style="padding-bottom: 20px">
								<input type="text" name="d_dosage" id="d_dosage" >
								<div id="dosageContainer"></div>
								<input type="hidden" name="d_per_day_qty" id="d_per_day_qty" value=""/>
							</div>
						</td>
					</tr>
					<tr style="display: none" id="dGenericNameRow">
						<td class="formlabel">Generic Name: </td>
						<td colspan="3"><a id="genericNameAnchor_dialog" style="display: none"></a></td>
					</tr>
					<tr>
						<td class="formlabel">Days: </td>
						<td><input type="text" name="d_days" id="d_days" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="return calcQty(event, 'd');"/></td>
						<td class="formlabel">Quantity: </td>
						<td><input type="text" name="d_qty" id="d_qty" value="" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
					</tr>
					<tr>
						<td class="formlabel" >Remarks: </td>
						<td colspan="3"><input type="text" name="d_remarks" id="d_remarks" value="" style="width: 475"></td>
					</tr>
				</table>
				<table style="margin-top: 10">
					<tr>
						<td>
							<button type="button" name="Add" id="Add" accessKey="A">
								<b><u>A</u></b>dd
							</button>
							<input type="button" name="Close" value="Close" id="Close"/>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
	<div id="editItemDialog" style="display: none">
		<input type="hidden" name="editRowId" id="editRowId" value=""/>
		<div class="bd">
			<input type="hidden" name="editRowId" id="editRowId" value=""/>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Prescription</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Type: </td>
						<td>
							<label id="ed_itemTypeLabel" style="font-weight: bold"></label>
							<input type="hidden" id="ed_itemType" name="ed_itemType" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Item: </td>
						<td>
							<label id="ed_itemNameLabel" style="font-weight: bold"></label>
							<input type="hidden" id="ed_itemName" name="ed_itemName" value="">
							<input type="hidden" id="ed_item_master" name="ed_item_master" value=""/>
							<input type="hidden" id="ed_ispackage" name="ed_ispackage" value=""/>
						</td>
						<td class="formlabel">Dosage: </td>
						<td>
							<div id="dosageAutoComplete" style="padding-bottom: 20px">
								<input type="text" name="ed_dosage" id="ed_dosage" value="" />
								<input type="hidden" name="ed_dosage_hidden" id="d_dosage_hidden" value=""/>
								<input type="hidden" name="ed_per_day_qty" id="d_per_day_qty" value=""/>
								<div id="ed_dosageContainer"></div>
							</div>
						</td>
					</tr>
					<tr style="display: none" id="edGenericNameRow">
						<td class="formlabel">Generic Name: </td>
						<td colspan="3"><a id="genericNameAnchor_editdialog" style="display:none"></a></td>
					</tr>
					<tr>
						<td class="formlabel">Days: </td>
						<td><input type="text" name="ed_days" id="ed_days" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="return calcQty(event, 'ed');"/></td>
						<td class="formlabel">Quantity: </td>
						<td><input type="text" name="ed_qty" id="ed_qty" value="" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
					</tr>
					<tr>
						<td class="formlabel">Remarks: </td>
						<td colspan="3"><input type="text" name="ed_remarks" id="ed_remarks" value="" style="width: 475px;"></td>
					</tr>
				</table>
				<table style="margin-top: 10">
					<tr>
						<td>
							<input type="button" id="editPrevious" name="previous" value="<<Previous" id="previous"/>
							<input type="button" id="editNext" name="next" value="Next>>" id="next"/>
							<input type="button" id="editClose" name="close" value="Close" id="close"/>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>

	</form>
	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
		var CollapsiblePanel2 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel2", {contentIsOpen:false});
		var CollapsiblePanel3 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel3", {contentIsOpen:false});

		var medDosages = ${medDosages};
	</script>
	</body>
</html>