<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
	<title>Doctor's Notes - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="wardactivities/doctorsnotes/doctorsnotes.js"/>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:link type="script" file="hmsvalidation.js"/>

	<script type="text/javascript">
		var doctors = ${doctors};
		var userRecord = ${userBean};
		var isSharedLogIn = '${isSharedLogIn}';
		var roleId = '${roleId}';
		var ipPrefs = ${ipPrefsJSON};
	</script>
	<style>
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			padding-bottom: 20px;
		}
	</style>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();">
	<h1>Doctor's Notes</h1>
	<insta:feedback-panel/>
	<c:choose >
		<c:when test="${not empty param.visit_id}">
		<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>

		<form name="DoctorsNotes" action="DoctorsNotes.do" method="POST" >
			<input type="hidden" name="_method" value="save"/>
			<input type="hidden" name="org_id" id="org_id" value="${patient.org_id}"/>
			<input type="hidden" name="patient_id" id="patient_id" value="${patient.patient_id}"/>
			<input type="hidden" name="note_id" id="note_id" value=""/>
			<input type="hidden" name="isSharedLogIn" value="${isSharedLogIn}"/>
			<input type="hidden" name="authUser" id="authUser" value=""/>
			<input type="hidden" name="loginUser" id="loginUser"  value="${userid}"/>
			<input type="hidden" name="patient_discharged" id="patient_discharged"
				value="${patient.visit_status == 'I' && patient.discharge_flag == 'D'}"/>

				<table cellspacing="0" cellpadding="0" id="noteDetails" border="0" width="100%">
					<tr><%--header row not used here --%></tr>
					<c:set var="numItems" value="${fn:length(doctornotes)}"/>
					<c:forEach begin="1" end="${numItems+1}" var="i">
						<c:set var="item" value="${doctornotes[i-1].map}"/>
						<c:if test="${empty item}">
							<c:set var="tr_style" value='style="display:none"'/>
						</c:if>

						<tr ${tr_style}>
							<td style="border: none">
								<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.creation_datetime}" var="creation_datetime"/>
								<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.mod_time}" var="mod_time"/>
								<input type="hidden" name="h_note_id" id="h_note_id${i}" value="${item.note_id}"/>
								<input type="hidden" name="h_note_num" id="h_note_num${i}" value="${item.note_num}"/>
								<input type="hidden" name="h_patient_id" id="h_patient_id${i}" value="${item.patient_id}"/>
								<input type="hidden" name="h_notes" id="h_notes${i}" value='<c:out value="${item.notes}"/>'/>
								<input type="hidden" name="h_billable_consultation" id="h_billable_consultation${i}" value="${item.billable_consultation}"/>
								<input type="hidden" name="h_consultation_type_id" id="h_consultation_type_id${i}" value="${item.consultation_type_id}"/>
								<input type="hidden" name="h_creation_datetime" id="h_creation_datetime${i}" value="${creation_datetime}"/>
								<input type="hidden" name="h_mod_time" id="h_mod_time${i}" value="${mod_time}"/>
								<input type="hidden" name="h_doctor_id" id="h_doctor_id${i}" value="${item.doctor_id}"/>
								<input type="hidden" name="h_doctor_name" id="h_doctor_name${i}" value="${item.doctor_name}"/>
								<input type="hidden" name="h_finalized" id="h_finalized${i}" value="${item.finalized}"/>
								<input type="hidden" name="h_highlighted" id="h_highlighted${i}" value="${item.highlighted}"/>
								<input type="hidden" name="h_charge_id" id="h_charge_id${i}" value="${item.charge_id}"/>
								<input type="hidden" name="h_mod_user" id="h_mod_user${i}" value="${item.mod_user}"/>

								<input type="hidden" name="h_edited" id="h_edited${i}" value="false"/>
								<input type="hidden" name="h_delete_note" id="h_delete_note${i}" value="N"/>

								<div id="noteResultList" >
									<div class="bd">
										<fieldset class="fieldSetBorder">
											<legend class="fieldSetLabel">&nbsp;<c:out value="${creation_datetime}"/>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;<c:out value="${item.doctor_name}"/>&nbsp; ( Entered By : <c:out value="${item.mod_user}"/>&nbsp;)&nbsp;&nbsp; <c:if test="${item.billable_consultation == 'Y'}"><img src="${cpath}/images/blue_flag.gif"/></c:if></legend>
											<table border="0" width="100%" class="${item.highlighted == 'Y' ? 'noteHighlight' : '' } ">
												<tr>
													<td colspan="3" style="border-top: none">
														<c:set var="notes" value="${ifn:breakContent(fn:escapeXml(item.notes))}"></c:set>
														<c:out value="${notes}" escapeXml="false" ></c:out>
													</td>
													<td width="20px" style="border-top: none"></td>
													<td width="20px" style="border-top: none"></td>
												</tr>
												<tr>
													<td colspan="3" style="border-top: none"></td>
													<td style="border-top: none; width: 30px">
														<c:choose>
															<c:when test="${item.finalized == 'Y'}">
																<img src="${cpath}/icons/Edit1.png" class="imgDelete button" />
															</c:when>
															<c:otherwise>
																<a name="_prescEditAnchor" href="#Edit Docotr Note" onclick="showEditDocNoteDialog(this,${i});"
																	title="Edit Doctor's Note">
																	<img src="${cpath}/icons/Edit.png" class="button" />
																</a>
															</c:otherwise>
														</c:choose>
													</td>
													<td style="border-top: none; width: 30px">
														<c:choose>
															<c:when test="${item.billable_consultation == 'Y' || item.finalized == 'Y'}">
																<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button"  />
															</c:when>
															<c:otherwise>
																<a href="#Cancel Docotr Note" onclick="cancelDoctorNote(this,${i});" title="Cancel Doctor's Note" >
																	<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
																</a>
															</c:otherwise>
														</c:choose>
													</td>
													<td style="border-top: none; width: 30px">
														<insta:screenlink screenId="doctor_notes_audit_log" extraParam="?_method=getAuditLogDetails&al_table=doctor_notes_audit_log&patient_id=${patient.patient_id}&note_num=${item.note_num}"
							                            target="_blank" label="Audit"/>
													</td>
												</tr>
											</table>
										</fieldset>
									</div>
								</div>
							</td>
						</tr>
					</c:forEach>
				</table>
			<div id="addNoteDialog"  style="display:none">
					<div class="bd">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Enter Doctor's Note</legend>
							<table border="0" class="formtable">
								<tr>
										<td class="formlabel" >Notes: </td>
										<td colspan="3"><textarea name="notes" id="notes" cols="80" rows="10"></textarea></td>
								</tr>
								<tr>
									<td class="formlabel">Doctor: </td>
									<td>
										<div id="doctorAutoComplete">
											<input type="text" name="doctor" id="doctor" value=""/>
											<input type="hidden" name="doctor_id" id="doctor_id" value=""/>
											<div id="doctorContainer" class="scrollForContainer" style="width: 350px"/>
										</div>
									</td>
									<td class="formlabel">Entered By: </td>
									<td>
										<c:set var="user" value="${userid}"/>
										<label>${isSharedLogIn == 'Y' ?'': user}</label>
										<input type="hidden" name="mod_user" id="mod_user" value="${isSharedLogIn == 'Y' ?'': user}"/>
									</td>
								</tr>
								<tr>
									<td class="formlabel" style="width: 100px">Date/Time: </td>
									<td style="width: 200px">
										<div style="display: inline;">
											<insta:datewidget name="entered_date" id="entered_date" value="today"/>
											<input type="text" name="entered_time" id="entered_time" style="width: 50px" value="${current_time}"/>
										</div>
									</td>
									<td><input type="checkbox" name="highlighted" id="highlighted" value="N" style="float: right;" onclick="return changeBackground();"/><div style="margin-top: 2px; float: right">Highlight Note </div></td>
									<td><input type="checkbox" name="chk_finalized" id="chk_finalized" value="N" onclick="return changeFinalized();"/><div style="margin-top: 2px; float: left">Finalize </div>
										<input type="hidden" name="finalized" id="finalized" value="N" />
									</td>
								</tr>
								<tr>
									<td></td>
									<td colspan="3">
									<input type="checkbox" name="chk_billable_consultation" id="chk_billable_consultation" value="Y" style="float: left;" onclick="return validateBillable();" checked/>Bill with consultation type as
									<input type="hidden" name="billable_consultation" id="billable_consultation" value="Y"/>
										<select name="consultation_type_id" id="consultation_type_id" class="dropdown">
											<option value="">-- Select --</option>
											<c:forEach var="consultationType" items="${consultationTypes}">
													<option value="${consultationType.map.consultation_type_id}">${consultationType.map.consultation_type}</option>
											</c:forEach>
										</select>
									</td>
								</tr>
							</table>
						</fieldset>
						<table>
								<tr>
							    	<td><input type="button" id="addNoteSaveBtn" value="Save" onclick="return onClickSave();"></td>
							    	<td><input type="button" id="addNoteCancelBtn" value="Cancel" onclick="return closeNote();"></td>
								</tr>
						</table>
					</div>
				</div>
				<table style="margin-top: 10px">
					<tr>
						<td>
							<input type="button" name="Add New Note" id="addNewNoteBtn" value="Add New Note" onclick="return addNoteForm();"/>
							<input type="button" name="Print" value="Print" onclick="emrPrintNotes();"/>
							<insta:screenlink screenId="patient_summary" extraParam="?_method=list&visit_id=${patient.patient_id}"
								label="IP Case Sheet" addPipe="true"/>
							<insta:screenlink screenId="ip_prescriptions" extraParam="?_method=list&visit_id=${patient.patient_id}"
									label="Doctor Order" addPipe="true"/>
						</td>
					</tr>
				</table>
				<div class="legend">
					<div class="flag"><img src="${cpath}/images/blue_flag.gif"/></div>
					<div class="flagText">Billed Notes</div>
				</div>
				<div id="editItemDialog" style="display: none">
					<input type="hidden" id="editRowId" name="editRowId" value=""/>
					<input type="hidden" name="ed_note_id" id="ed_note_id" value=""/>
					<div class="hd">Edit Notes details</div>
					<div class="bd">
						<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Enter Doctor's Note</legend>
							<table border="0" class="formtable">
								<tr>
										<td class="formlabel" >Notes: </td>
										<td colspan="3"><textarea name="ed_notes" id="ed_notes" cols="80" rows="10"></textarea></td>
								</tr>
								<tr>
									<td class="formlabel">Doctor: </td>
									<td>
										<div id="doctorAutoComplete">
											<input type="text" name="ed_doctor" id="ed_doctor"/>
											<input type="hidden" name="ed_doctor_id" id="ed_doctor_id" value=""/>
											<div id="ed_doctorContainer" class="scrollForContainer" style="width: 350px"/>
										</div>
									</td>
									<td class="formlabel">Entered By: </td>
									<td>
										<label id="ed_entered_by_label"></label>
										<input type="hidden" name="ed_mod_user" id="ed_mod_user" />
									</td>
								</tr>
								<tr>
									<td class="formlabel" style="width: 100px">Date/Time: </td>
									<td style="width: 200px">
										<div style="display: inline;">
											<insta:datewidget name="edEnteredDate" id="edEnteredDate" extravalidation="editDateTime()"/>
											<input type="hidden" name="ed_entered_date" id="ed_entered_date" value=""/>
											<input type="text" name="edEnteredTime" id="edEnteredTime" style="width: 50px" />
											<input type="hidden" name="ed_entered_time" id="ed_entered_time" value=""/>
										</div>
									</td>
									<td>
										<input type="checkbox" name="edit_highlighted" id="edit_highlighted" style="float: right;" onclick="return ed_changeBackground(${i});"/><div style="margin-top: 2px; float: right">Highlight Note </div>
										<input type="hidden" name="ed_highlighted" id="ed_highlighted" value="" />
									</td>
									<td>
										<input type="checkbox" name="edit_finalized" id="edit_finalized" onclick="return ed_changeFinalized();"/><div style="margin-top: 2px; float: left">Finalize </div>
										<input type="hidden" name="ed_finalized" id="ed_finalized" value="" />
									</td>
								</tr>
								<tr>
									<td></td>
									<td colspan="3">
									<input type="checkbox" name="ed_chk_billable_consultation" id="ed_chk_billable_consultation" style="float: left;" onclick="return ed_validateBillable();"/>Bill with consultation type as
										<input type="hidden" name="ed_billable_consultation" id="ed_billable_consultation" value=""/>
										<select name="ed_consultation_type_id" id="ed_consultation_type_id" class="dropdown">
											<option value="">-- Select --</option>
											<c:forEach var="consultationType" items="${consultationTypes}">
													<option value="${consultationType.map.consultation_type_id}">${consultationType.map.consultation_type}</option>
											</c:forEach>
										</select>
										<input type="hidden" name="consultationTypeId" id="consultationTypeId" value=""/>

									</td>
								</tr>
							</table>
						</fieldset>
						<table>
							<tr>
								<td>
									<input type="button" id="ed_Save" value="Save"/>
									<input type="button" id="ed_Cancel" value="Cancel"/>
								</td>
							</tr>
						</table>
					</div>
				</div>
		</form>
		<div id="loginDiv" style="display: none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Login Details</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">User ID: </td>
						<td><input type="text" name="login_user" id="login_user"/></td>
						<td class="formlabel">&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="formlabel">Password: </td>
						<td><input type="password" name="login_password" id="login_password" onkeypress="return submitOnEnter(event);"/></td>
						<td class="formlabel">&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
				<table style="margin-top: 10px">
					<tr>
						<td><input type="button" name="submitForm" id="submitForm" value="Submit" />
							<input type="button" name="cancelSubmit" id="cancelSubmit" value="Cancel" />
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
	</c:when>
    </c:choose>
</body>
</html>