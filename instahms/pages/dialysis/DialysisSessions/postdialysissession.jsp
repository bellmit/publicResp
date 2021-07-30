<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
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
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="patient.dialysis.sessions.postdialysissession.title"/></title>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.commonvalidations.toolbar");
		var use_store_items = '${genericPrefs.prescription_uses_stores}';
		var mr_no = '${ifn:cleanJavaScript(param.mr_no)}';
	</script>
	<insta:link type="js" file="hmsvalidation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<insta:link type="script" file="datetest.js"/>
	<insta:link type="script" file="dialysis/dialysissessions.js"/>
	<insta:link type="script" file="dialysis/drugAdministered.js"/>

	<insta:js-bundle prefix="dialysis.drugadministered"/>


</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<c:set var="OrderedOptions">
 <insta:ltext key="patient.dialysis.sessions.Ordered"/>,
 <insta:ltext key="patient.dialysis.sessions.prepared"/>,
 <insta:ltext key="patient.dialysis.sessions.inprogress"/>,
 <insta:ltext key="patient.dialysis.sessions.completed"/>,
 <insta:ltext key="patient.dialysis.sessions.closed"/>
</c:set>

<c:set var="normalOptions">
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.normal"/>,
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.discontinued"/>,
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.cancelled"/>
</c:set>

<c:set var="closed">
 <insta:ltext key="patient.dialysis.sessions.closed"/>
 </c:set>
 
 <c:set var="fillText">
 <insta:ltext key="patient.dialysis.sessions.fill"/>
 </c:set>

<c:set var="selectDigitOptions">
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/>,
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.1"/>,
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.2"/>,
 <insta:ltext key="patient.dialysis.sessions.postdialysissession.3"/>
</c:set>
<c:set var="saveButton">
 <insta:ltext key="patient.dialysis.prescriptions.save"/>
</c:set>
<c:set var="cancelButton">
 <insta:ltext key="patient.dialysis.prescriptions.cancel"/>
</c:set>
<c:set var="okButton">
 <insta:ltext key="patient.dialysis.prescriptions.ok"/>
</c:set>

<script>
	var cpath = ${cpath};
</script>
<body onload="setPostWtValues();setHeparinInfused();setPatencyCheckBoxValues(document.postDialysis);initDrugDialog();initStandingtreatments();" class="yui-skin-sam">
<div class="pageHeader"><insta:ltext key="patient.dialysis.sessions.postdialysissession.pageHeader"/></div>
<insta:feedback-panel/>
<insta:patientgeneraldetails  mrno="${mr_no}" addExtraFields="true" showClinicalInfo="true"/>
<form name="postDialysis" method="post" action="${cpath}/dialysis/PostDialysisSessions.do">
<input type="hidden" name="_method" value="update">
<input type="hidden" name="order_id" value="${ifn:cleanHtmlAttribute(order_id)}">
<input type="hidden" name="access_type_id" id="access_type_id" value="${postSesDetails.access_type_id}">
<input type="hidden" name="access_patency" id="access_patency" value="${postSesDetails.access_patency}">
<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.sessiondetails"/></legend>
	<table border="0" class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.status"/></td>
			<td >
				<insta:selectoptions name="status" value="${postSesDetails.status}"
											opvalues="O,P,I,F,C" optexts="${OrderedOptions}"/><span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.closingattendant"/></td>
			<c:set var="endAttendant"  value="${empty postSesDetails.end_attendant ? logedin_user: postSesDetails.end_attendant}" />
			<td >
				<select name="end_attendant" id="end_attendant" class="dropdown">
					<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
					<c:forEach items="${users}" var="attendant">
						<option value="${attendant.map.emp_username}" ${endAttendant == attendant.map.emp_username ? 'selected' : ''}>
							${attendant.map.emp_username}
						</option>
					</c:forEach>
				</select>
				</select><span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.completionstatus"/></td>
			<td>
				<insta:selectoptions name="completion_status" value="${postSesDetails.completion_status}"
											opvalues="N,D,X" optexts="${normalOptions}"/><span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.endtime"/></td>
			<c:set var="endDate"><fmt:formatDate value="${postSesDetails.end_time}" pattern="dd-MM-yyyy"/></c:set>
			<c:set var="endTime"><fmt:formatDate value="${postSesDetails.end_time}" pattern="HH:mm"/></c:set>
			<c:set var="startDateTime"><fmt:formatDate value="${postSesDetails.start_time}" pattern="dd-MM-yyyy HH:mm"/></c:set>
			<td>
				<insta:datewidget name="enddate" id="enddate" btnPos="left" value="${endDate}"/>
				<input type="text" name="endtime" id="endtime" class="timeField" value="${endTime}"/>
				<input type="hidden" name="end_time" id="end_time" value="${endDateTme}"/>
				<input type="hidden" name=startDateTime id="startDateTime" value="${startDateTime}"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.duration"/></td>
			<td>${postSesDetails.est_duration}</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.nextdialysisdate"/></td>
			<td>
			<c:set var="nxtDialysisDate" ><fmt:formatDate value="${postSesDetails.nxt_dialysis_date}" pattern="dd-MM-yyyy"/></c:set>
			<div style="float: left"><insta:datewidget name="nxt_dialysis_date" valid="future"  btnPos="left" value="${nxtDialysisDate}"/></div></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.shift"/></td>
				<td><insta:selectoptions name="shift"
								opvalues="null,1,2,3" optexts="${selectDigitOptions}"  value="${postSesDetails.shift}"  />
			</td>
		</tr>
	</table>
	</fieldSet>
	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.patientcondition"/></legend>
	<table border="0" class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.bpsitting"/></td>
			<td>
				<input type="text" name="fin_bp_high_sit" id ="fin_bp_high_sit" value="${postSesDetails.fin_bp_high_sit}" class="number"  onkeypress="return enterNumOnly(event)"/> / <input type="text" name="fin_bp_low_sit" id ="fin_bp_low_sit" value="${postSesDetails.fin_bp_low_sit}" class="number"  onkeypress="return enterNumOnly(event)"/><span class="star"> * </span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.bpstanding"/></td>
			<td >
				<input type="text" name="fin_bp_high_stand" id ="fin_bp_high_stand" value="${postSesDetails.fin_bp_high_stand}" class="number"  onkeypress="return enterNumOnly(event)"/> / <input type="text" name="fin_bp_low_stand" id ="fin_bp_low_stand" value="${postSesDetails.fin_bp_low_stand}" class="number"  onkeypress="return enterNumOnly(event)"/><span class="star"> * </span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.respiration"/></td>
			<td>
				<input type="text" name="fin_respiration" id ="fin_respiration" value="${postSesDetails.fin_respiration}" class="number"  onkeypress="return enterNumOnly(event)"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.pulsesitting"/></td>
			<td>
				<input type="text" name="fin_pulse_sit" id ="fin_pulse_sit" value="${postSesDetails.fin_pulse_sit}" class="number"  onkeypress="return enterNumOnly(event)"/><span class="star"> * </span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.pulsestanding"/></td>
			<td>
				<input type="text" name="fin_pulse_stand" id ="fin_pulse_stand" value="${postSesDetails.fin_pulse_stand}" class="number"  onkeypress="return enterNumOnly(event)"/><span class="star"> * </span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.temperature"/></td>
			<td>
				<input type="text" name="fin_temperature" id ="fin_temperature" value="${postSesDetails.fin_temperature}" class="number"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.measuredwt"/></td>
			<td>
				<input type="text" name="fin_total_wt" id ="fin_total_wt" value="${postSesDetails.fin_total_wt}" class="number" onblur="setPostWtValues();" onkeypress="return enterNumOnlyANDdot(event)"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.wheelchairwt"/></td>
			<td>
				<input type="text" name="fin_wheelchair_wt" id ="fin_wheelchair_wt" value="${postSesDetails.fin_wheelchair_wt}" class="number" onblur="setPostWtValues();" onkeypress="return enterNumOnlyANDdot(event)"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.prosthetic"/></td>
			<td>
				<input type="text" name="fin_prosthetic_wt" id ="fin_prosthetic_wt" value="${postSesDetails.fin_prosthetic_wt}" class="number" onblur="setPostWtValues();" onkeypress="return enterNumOnlyANDdot(event)"/>
				<input type="hidden" name= "fin_real_wt" id="fin_real_wt" value=""/>
			</td>

		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.condition"/></br><insta:ltext key="patient.dialysis.sessions.postdialysissession.assessment"/></td>
			<td ><textarea name="fin_patient_cond" >${postSesDetails.fin_patient_cond}</textarea></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.comment"/></td>
			<td>
				<select name="prolonged_bleeding_at_sites" id="prolonged_bleeding_at_sites" class="dropdown">
					<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
					<option value="Y" ${postSesDetails.prolonged_bleeding_at_sites eq 'Y' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.postdialysissession.yes"/></option>
					<option value="N" ${postSesDetails.prolonged_bleeding_at_sites eq 'N' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.postdialysissession.no"/></option>
				</select>
			</td>
		</tr>
	</table>
	</fieldSet>
	<fieldSet class="fieldSetBorder" ><legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.dialysisdetails"/></legend>
	<table border="0"class="formtable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.weightlost"/></td>
			<td >
				<label id="weightLoss">${postSesDetails.total_wt_loss}</label>
				<input type="hidden" name="total_wt_loss" id ="total_wt_loss" value="" />
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.fluidremoved"/></td>
			<td >
				<input type="text" name="target_wt_removal" id ="target_wt_removal" value="${postSesDetails.target_wt_removal}" class="number" onkeypress="return enterNumOnlyANDdot(event)"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.ufrate"/></td>
			<td >
				<input type="text" name="fluid_in_wt" id ="fluid_in_wt" value="${postSesDetails.fluid_in_wt}" class="number" onkeypress="return enterNumOnlyANDdot(event)"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.heparinleft"/></td>
			<td>
				<input type="text" name="heparin_left" id ="heparin_left" value="${postSesDetails.heparin_left}" class="number" onblur="setHeparinInfused();"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.totalheparininfused"/></td>
			<td>
				<label id="heparinInfused">${postSesDetails.total_heparin}</label>
				<input type="hidden" name="total_heparin" id ="total_heparin" value="" class="number" onkeypress="return enterNumOnlyANDdot(event)"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.dialyzerrating"/></td>
			<td>
				<insta:selectdb name="fin_dialyzer_rating_id" table="dialyzer_ratings" valuecol="dialyzer_rating_id"
												displaycol="dialyzer_rating" filtered="true" value="${postSesDetails.fin_dialyzer_rating_id}" dummyvalue="----select---" /><span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class=formlabel><insta:ltext key="patient.dialysis.sessions.postdialysissession.bruitthrill"/></td>
			<td >
				<div style="display: none" id="access_patency_group1">
					NF<input type="checkbox" name="patencyNf" value=""  ${postSesDetails.fin_patency_nf == 'Y' ? 'checked' : '' }/>
					RF<input type="checkbox" name="patencyRf" value=""  ${postSesDetails.fin_patency_rf == 'Y' ? 'checked' : '' }/>
					<input type="hidden" name="fin_patency_nf" id="patency_nf" value="">
					<input type="hidden" name="fin_patency_rf" id="patency_rf" value="">
				</div>
				<div style="display: none" id="access_patency_group2">
					<%--Bruit<input type="checkbox" name=patencyBruit value=""  ${postSesDetails.fin_patency_bruit == 'Y' ? 'checked' : '' }/>
					Thrill<input type="checkbox" name="patencyThrill" value=""  ${postSesDetails.fin_patency_thrill == 'Y' ? 'checked' : '' }/>--%>
					<select name="fin_patency_bruit_thrill" id="fin_patency_bruit_thrill" class="dropdown">
								<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
								<option value="Y" ${postSesDetails.fin_patency_bruit_thrill eq 'Y' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.postdialysissession.yes"/></option>
								<option value="O" ${postSesDetails.fin_patency_bruit_thrill eq 'O' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.postdialysissession.no"/></option>
								<option value="N" ${postSesDetails.fin_patency_bruit_thrill eq 'N' ? 'selected' : ''}><insta:ltext key="patient.dialysis.sessions.postdialysissession.notapplicable"/></option>
					</select><span class="star">*</span>
					<input type="hidden" name="fin_patency_bruit" id="patency_bruit" value="">
					<input type="hidden" name="fin_patency_thrill" id="patency_thrill" value="">
				</div>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.minimumbp"/></td>
			<td>
				<input type="text" name="min_bp_high" id ="min_bp_high"  value= "${postSesDetails.intra_min_bp_high}" class="number" readonly/> /
				<input type="text" name="min_bp_low" id ="min_bp_low" value= "${postSesDetails.intra_min_bp_low}" class="number" readonly/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.minimumbptime"/></td>
			<td>

			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.dialysisodometer"/></td>
			<td><input type="text" name="fin_odometer_reading" value="${postSesDetails.fin_odometer_reading}" onkeypress="return enterNumOnlyzeroToNine(event)" class="number"></td>
			<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.completionnotes"/></td>
			<td><textarea name="completion_notes">${postSesDetails.completion_notes}</textarea></td>
			<td ><insta:ltext key="patient.dialysis.sessions.postdialysissession.notes"/></td>
			<td><textarea name="post_session_notes">${postSesDetails.post_session_notes}</textarea></td>

		</tr>
	</table>
	</fieldSet>
	<table>
		<tr>
			<td colspan="6" style="padding-left: 777px">
				<insta:ltext key="patient.dialysis.sessions.postdialysissession.fillfrombill"/> <input type="button" value="${fillText}" onclick="fillFromBill();" />
			</td>
		</tr>
	</table>
	<fieldSet class="fieldsetborder">
	<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.drugsadministered"/></legend>
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="siTable" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
		<tr>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.prescdate"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.name"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.dosage"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.route"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.remarks"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.quantity"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.batchno"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.expirydate"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.administeredby"/></th>
			<th><insta:ltext key="patient.dialysis.sessions.postdialysissession.doctor"/></th>
			<th style="width: 16px"></th>
			<th style="width: 16px"></th>
		</tr>
		<c:set var="numTreatmentChart" value="${fn:length(treatmentChart)}"/>
		<c:forEach begin="1" end="${numTreatmentChart+1}" var="i" varStatus="loop">
			<c:set var="treatment" value="${treatmentChart[i-1]}"/>
			<%--<c:set var="flagColor">
				<c:choose>
					<c:when test="${treatment.discontinued == 'Y'}">grey</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
			</c:set>--%>
			<c:if test="${empty treatment}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
		 <tr ${style}><%--  class="${treatment.discontinued == 'Y' ? 'discontinued' : ''    --%>
				<td>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${treatment.prescribed_date}" var="prescribed_date"/>
					${prescribed_date}
					<input type="hidden" name="s_prescribed_date" value="${prescribed_date}"/>
					<input type="hidden" name="s_prescription_id" value="${treatment.drug_administered_id}"/>
					<input type="hidden" name="s_item_name" value="<c:out value='${treatment.medicine_name}'/>"/>
					<input type="hidden" name="s_item_id" value="${treatment.medicine_id}"/>
					<input type="hidden" name="s_medicine_dosage" value="${ifn:cleanHtmlAttribute(treatment.dosage)}"/>
					<input type="hidden" name="s_item_remarks" value="${ifn:cleanHtmlAttribute(treatment.remarks)}"/>
					<input type="hidden" name="s_batch_no" value="${treatment.batch_no}" />
					<input type="hidden" name="s_delItem" id="s_delItem" value="false" />
					<input type="hidden" name="s_edited" value='false'/>
					<input type="hidden" name="s_route_id" value="${treatment.route_id}"/>
					<input type="hidden" name="s_route_name" value="${treatment.route_name}"/>
					<input type="hidden" name="s_qty_in_stock" value=""/>
					<input type="hidden" name="s_quantity" value="${treatment.quantity}"/>
					<input type="hidden" name="s_expdate" value="${treatment.expiry_date}"/>
					<input type="hidden" name="s_staff" value="${treatment.staff}"/>
					<input type="hidden" name="s_doctor" value="${treatment.doctor_id}"/>

				<td>
					<insta:truncLabel value="${treatment.medicine_name}" length="20"/>
				</td>
				<td>
					<label>${ifn:cleanHtmlAttribute(treatment.dosage)}</label>

				</td>
				<td>
					<label>${treatment.route_name}</label>

				</td>
				<td>
					<insta:truncLabel value="${treatment.remarks}" length="30"/>
				</td>
				<td>
					<label>${treatment.quantity}</label>
				</td>
				<td>
					<label>${treatment.batch_no}</label>
				</td>
				<td>
					<label>${treatment.expiry_date}</label>
				</td>
				<td>
					<label>${treatment.staff}</label>
				</td>
				<td>
					<label>${treatment.doctor_name}</label>
				</td>
				<td style="text-align: center; ">
					<a name="trashCanAnchor" href="javascript:Cancel Item"  onclick="return cancelSIItem(this);" title='<insta:ltext key="patient.dialysis.sessions.postdialysissession.cancelitem"/>' >
						<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
					</a>
				</td>
				<td style="text-align: center">
					<a name="si_editAnchor" href="javascript:Edit" onclick="return showEditSIDialog(this);" title='<insta:ltext key="patient.dialysis.sessions.postdialysissession.edititem"/>'>
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
				<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="patient.dialysis.sessions.postdialysissession.add.treatment"/>'
					onclick="showAddSIDialog(this); return false;"
					accesskey="O" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</td>
			</tr>
	</table>
</fieldset>


	<c:url value="PreDialysisSessions.do" var="preurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="dialysisprescId" value="${postSesDetails.prescription_id}"/>
		<c:param name="order_id" value="${order_id}"/>
		<c:param name="visit_center" value="${visit_center}"/>
	</c:url>
	<c:url value="IntraDialysisSessions.do" var="intraurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="order_id" value="${order_id}"/>
	</c:url>
	<c:url value="/Service/Services.do" var="consumablesUrl">
		<c:param name="_method" value="serviceDetails"/>
		<c:param name="prescription_id" value="${param.order_id}"/>
	</c:url>
	<c:url value="DialysisSessionReport.do" var="sessionReport">
		<c:param name="method" value="getSessionReport"/>
		<c:param name="mr_no" value="${mr_no}"/>
		<c:param name="order_id" value="${order_id}"/>
	</c:url>
	<c:url value="DialysisPrescriptions.do" var="prescrurl">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
	</c:url>
	<c:url var="treatmentUrl" value="/dialysis/DialysisMedications.do">
		<c:param name="_method" value="show"/>
		<c:param name="mr_no" value="${mr_no}"/>
	</c:url>
	<c:url var="progressNtsUrl" value="/progress/PatientProgress.do">
		<c:param name="_method" value="show" />
		<c:param name="mr_no" value="${mr_no}" />
	</c:url>
	<div class="screenActions">
		<input type="submit" name="save" id="save" value="${saveButton}" onclick="return funPostSubmitValues();">
		| <a href="javascript:void(0)" onclick="funPostCancel();"><insta:ltext key="patient.dialysis.sessions.postdialysissession.dialysissessions"/></a>
		| <a href='<c:out value="${prescrurl}"/>'><insta:ltext key="patient.dialysis.sessions.postdialysissession.prescription"/></a>
		| <a href='<c:out value="${preurl}"/>'><insta:ltext key="patient.dialysis.sessions.postdialysissession.predialysis"/></a> | <a href='<c:out value="${intraurl}"/>'><insta:ltext key="patient.dialysis.sessions.postdialysissession.intradialysis"/></a>
		| <a href='<c:out value="${consumablesUrl}"/>' onclick="return checkConsumablesExist('${not empty ConsumablesDetails}');"><insta:ltext key="patient.dialysis.sessions.postdialysissession.consumables"/></a>
		| <a href ='<c:out value="${sessionReport}"/>' target="_blank"><insta:ltext key="patient.dialysis.sessions.postdialysissession.sessionreport"/></a>
		| <a href='<c:out value="${treatmentUrl}"/>'><insta:ltext key="patient.dialysis.sessions.postdialysissession.patientprescription"/></a>
		| <a href='<c:out value="${progressNtsUrl}"/>'><insta:ltext key="patient.dialysis.sessions.postdialysissession.progressnotes"/></a>
	</div>

	<div id="addSIDialog" style="display: none">
	<div class="bd">
		<div id="addSIDialogFieldsDiv">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.adddrugs"/></legend>
				<table class="formtable">

					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.medicinename"/>:</td>
						<td colspan="3">
							<div id="s_d_itemAutocomplete" style="padding-bottom: 10px; width: 443px">
								<input type="text" id="s_d_itemName" name="s_d_itemName">
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
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.route"/>:</td>
						<td><select name="s_d_medicine_route" id="s_d_medicine_route" class="dropdown">
								<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
								<c:forEach var="route" items="${routes}">
									<option value="${route.route_id}">${route.route_name}</option>
								</c:forEach>
							</select>
						</td>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.dosageperadm"/>:</td>
						<td>
							<input type="text" name="s_d_dosage" id="s_d_dosage" style="width: 100px">
							<label id="s_d_medicineUOM"></label><font style="color: red">*</font>
							<img class="imgHelpText" src="${cpath}/images/help.png" title="Dosage per Administration"/>
							<inpu typ="hidden" name="s_d_consumption_uom" id="s_d_consumption_uom" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel" ><insta:ltext key="patient.dialysis.sessions.postdialysissession.remarks"/>:</td>
						<td colspan="3"><input type="text" name="s_d_remarks" id="s_d_remarks" value="" style="width: 443px"/></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.batchno"/>:</td>
						<td><input type="text" name="s_d_batchno" id="s_d_batchno"/></td>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.expirydate"/>:</td>
						<td><input type="text" name="s_d_expirydate" id="s_d_expirydate" style="width: 90px;"/><label style="float: right">(MM-YYYY)</label></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.quantity"/>:</td>
						<td><input type="text" name="s_d_quantity" id="s_d_quantity" style="width: 100px;" onkeypress="return enterNumAndDot(event);"/></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.administeredby"/>:</td>
						<td><select name="s_d_staff" id="s_d_staff" class="dropdown">
							<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
								<c:forEach var="staffNames" items="${clinicalStaff}" varStatus="st">
									<option value="${staffNames.map.emp_username}">${staffNames.map.emp_username}</option>
								</c:forEach>
							</select>
						</td>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.doctor"/>:</td>
						<td><insta:selectdb name="s_d_doctors" id="s_d_doctors" table="doctors" displaycol="doctor_name" valuecol="doctor_id" dummyvalue="---Select---" dummyvalueId="" orderby="doctor_name"/></td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="SIAdd" id="SIAdd" ><insta:ltext key="patient.dialysis.sessions.postdialysissession.add"/></button>
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
				<legend class="fieldSetLabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.editdrugs"/></legend>
				<table class="formtable">
					<tr>
						<%--<td class="formlabel">Type: </td>
						<td>
							<label id="s_ed_itemTypeLabel" style="font-weight: bold"></label>
							<input type="hidden" id="s_ed_itemType" name="s_ed_itemType" value=""/>
						</td>--%>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.prescdate"/></td>
						<td class="forminfo"><label id="s_ed_presc_date"></label></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.medicinename"/> </td>
						<td colspan="3">
						<div id="s_ed_itemAutocomplete" style="padding-bottom: 10px; width: 443px">
							<input type="text" id="s_ed_itemNameLabel" name="s_ed_itemNameLabel" value="" />
							<div id="s_ed_itemContainer" style="width: 350px" class="scrolForContainer"></div>
							<input type="hidden" id="s_ed_itemName" name="s_ed_itemName" value="">
							<input type="hidden" id="s_ed_item_master" name="s_ed_item_master" value=""/>
							<input type="hidden" id="s_ed_ispackage" name="s_ed_ispackage" value=""/>
							<input type="hidden" id="s_ed_item_id" name="s_ed_item_id" value=""/>
						</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.route"/> </td>
						<td>
							<select id="s_ed_medicine_route" name="s_ed_medicine_route" class="dropdown">
								<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/> </option>
								<c:forEach var="route" items="${routes}">
									<option value="${route.route_id}">${route.route_name}</option>
								</c:forEach>
							</select>
						</td>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.dosage"/>  </td>
						<td>
							<input type="text" name="s_ed_dosage" id="s_ed_dosage" value="" style="width: 100px" onchange="setSIEdited()" />
							<label id="s_ed_medicineUOM"></label><font style="color: red">*</font>
							<img class="imgHelpText" src="${cpath}/images/help.png" title="Dosage per Administration"/>
							<input type="hidden" name="s_ed_consumption_uom" id="s_ed_consumption_uom" value=""/>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.remarks"/> </td>
						<td colspan="3"><input type="text" name="s_ed_remarks" id="s_ed_remarks" onchange="setSIEdited()" value="" style="width: 443px;"></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.batchno"/></td>
						<td><input type="text" name="s_ed_batchno" id="s_ed_batchno"/></td>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.expirydate"/></td>
						<td><input type="text" name="s_ed_expirydate" id="s_ed_expirydate" style="width: 90px;"/><label style="float: right">(MM-YYYY)</label></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.quantity"/> </td>
						<td><input type="text" name="s_ed_quantity" id="s_ed_quantity" style="width: 100px;" onkeypress="return enterNumAndDot(event);"/></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.administeredby"/></td>
						<td><select name="s_ed_staff" id="s_ed_staff" class="dropdown">
						<option value=""><insta:ltext key="patient.dialysis.sessions.postdialysissession.select"/></option>
							<c:forEach var="staffNames" items="${clinicalStaff}" varStatus="st">
								<option value="${staffNames.map.emp_username}">${staffNames.map.emp_username}</option>
							</c:forEach>
							</select>
						</td>
							<td class="formlabel"><insta:ltext key="patient.dialysis.sessions.postdialysissession.doctor"/></td>
							<td><insta:selectdb name="s_ed_doctors" id="s_ed_doctors" table="doctors" displaycol="doctor_name" valuecol="doctor_id" dummyvalue="---Select---" dummyvalueId=""/></td>
					</tr>
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
	var preDialysisWt = '${postSesDetails.in_real_wt}';
	var heparinBolus = '${postSesDetails.heparin_bolus}';
	var heparinStart = '${postSesDetails.heparin_start}';
	var isFinalized = '${isFinalized}';
	var AccessTypeDetailsJson = ${AccessTypeDetailsJson};
</script>

</body>
</html>
