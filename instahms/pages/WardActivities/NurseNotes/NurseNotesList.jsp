<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>
<fmt:formatDate pattern="dd-MM-yyyy" value="<%=new java.util.Date()%>" var="current_date"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>


<html>
<head>
	<title>Nurse's Notes - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="wardactivities/nursenotes/nursenotes.js"/>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<script type="text/javascript">
		var userRecord = ${userBean};
		var isSharedLogIn = '${isSharedLogIn}';
		var roleId = '${roleId}';
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
	<h1>Nurse's Notes</h1>
	<insta:feedback-panel/>
	<c:choose >
		<c:when test="${not empty param.visit_id}">
		<insta:patientdetails visitid="${param.visit_id}" showClinicalInfo="true"/>

	<form name="NurseNotes" action="NurseNotes.do" method="POST" >
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
			<c:set var="numItems" value="${fn:length(nursenotes)}"/>
				<c:forEach begin="1" end="${numItems+1}" var="i">
					<c:set var="item" value="${nursenotes[i-1].map}"/>
					<c:if test="${empty item}">
						<c:set var="tr_style" value='style="display:none"'/>
					</c:if>

				<tr ${tr_style}>
					<td style="border: none">
						<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.creation_datetime}" var="creation_datetime"/>
						<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${item.mod_time}" var="mod_time"/>
						<fmt:formatDate pattern="dd-MM-yyyy" value="${item.creation_datetime}" var="creation_date"/>

						<input type="hidden" name="h_note_id" id="h_note_id${i}" value="${item.note_id}"/>
						<input type="hidden" name="h_note_num" id="h_note_num${i}" value="${item.note_num}"/>
						<input type="hidden" name="h_patient_id" id="h_patient_id${i}" value="${item.patient_id}"/>
						<input type="hidden" name="h_notes" id="h_notes${i}" value='<c:out value="${item.notes}"/>'/>
						<input type="hidden" name="h_creation_datetime" id="h_creation_datetime${i}" value="${creation_datetime}"/>
						<input type="hidden" name="h_mod_time" id="h_mod_time${i}" value="${mod_time}"/>
						<input type="hidden" name="h_finalized" id="h_finalized${i}" value="${item.finalized}"/>
						<input type="hidden" name="h_mod_user" id="h_mod_user${i}" value="${item.mod_user}"/>
						<input type="hidden" name="h_htover" id="h_htover${i}" value="${item.note_type}"/>
						<input type="hidden" name="h_edited" id="h_edited${i}" value="false"/>
						<input type="hidden" name="h_delete_note" id="h_delete_note${i}" value="N"/>

						<div id="noteResultList" >
							<div class="bd">
								<fieldset class="fieldSetBorder">
									<legend class="fieldSetLabel">&nbsp;<c:out value="${creation_datetime}"/>&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;(
										<c:choose>
											<c:when test="${item.note_type == 'H'}">Handed Over</c:when>
											<c:when test="${item.note_type == 'T'}">Taken Over</c:when>
											<c:otherwise>Entered</c:otherwise>
										</c:choose>
											By: <c:out value="${item.mod_user}"/>&nbsp;)
									</legend>
									<c:choose>
										<c:when test="${current_date == creation_date}">
											<c:set var="flag" value="true"/>
										</c:when>
										<c:otherwise>
											<c:set var="flag" value="false"/>
										</c:otherwise>
									</c:choose>
									<table border="0" width="100%" class="${( (item.note_type == 'H') && flag )? 'handoverHighlight' : ( (item.note_type == 'T') && flag ) ? 'takeoverHighlight' : '' } " >
										<tr>
											<td colspan="3" style="border-top: none">
												<c:set var="notes" value="${ifn:breakContent(fn:escapeXml(item.notes))}"></c:set>
												<c:out value="${notes}" escapeXml="false"></c:out>
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
														<a name="_prescEditAnchor" href="#Edit Nurse Note" onclick="showEditNurseNoteDialog(this,${i});"
															title="Edit Nurse's Note">
															<img src="${cpath}/icons/Edit.png" class="button" />
														</a>
													</c:otherwise>
												</c:choose>
											</td>
											<td style="border-top: none; width: 30px">
												<c:choose>
													<c:when test="${item.finalized == 'Y'}">
														<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button"  />
													</c:when>
													<c:otherwise>
														<a href="#Cancel Nurse Note" onclick="cancelNurseNote(this,${i});" title="Cancel Nurse's Note" >
															<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
														</a>
													</c:otherwise>
												</c:choose>
											</td>
											<td style="border-top: none; width: 30px">
											<insta:screenlink screenId="nurse_notes_audit_log" extraParam="?_method=getAuditLogDetails&patient_id=${patient.patient_id}&al_table=nurse_notes_audit_log&note_num=${item.note_num}"
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
						<legend class="fieldSetLabel">Enter Nurse's Note</legend>
						<table border="0" class="formtable">
							<tr>
									<td class="formlabel" >Notes: </td>
									<td colspan="3"><textarea name="notes" id="notes" cols="80" rows="10"></textarea></td>
							</tr>
							<tr>
								<td class="formlabel">Nurse: </td>
								<td>
									<c:set var="user" value="${userid}"/>
									<label>${isSharedLogIn == 'Y' ?'': user}</label>
									<input type="hidden" name="mod_user" id="mod_user" value="${isSharedLogIn == 'Y' ?'': user}"/>
								</td>
								<td><input type="checkbox" name="chk_finalized" id="chk_finalized" value="N" onclick="return changeFinalized();"/><div style="margin-top: 2px; float: left">Finalize </div>
									<input type="hidden" name="finalized" id="finalized" value="N" />
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
								<td class="formlabel" style="width: 100px"><div style="margin-top: 2px; float: left">Handover/Takeover:
									<insta:selectoptions name="htover" id="htover" opvalues="H,T"
										optexts="Handover,Takeover" value=""  dummyvalue="-- Select --" dummyvalueId=""/></div>
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
				</td>
			</tr>
		</table>
		<div id="editItemDialog" style="display: none">
			<input type="hidden" id="editRowId" name="editRowId" value=""/>
			<input type="hidden" name="ed_note_id" id="ed_note_id" value=""/>
			<div class="hd">Edit Notes details</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Enter Nurse's Note</legend>
					<table border="0" class="formtable">
						<tr>
								<td class="formlabel" >Notes: </td>
								<td colspan="3"><textarea name="ed_notes" id="ed_notes" cols="80" rows="10"></textarea></td>
						</tr>
						<tr>
							<td class="formlabel">Nurse: </td>
							<td>
								<label id="ed_entered_by_label"></label>
								<input type="hidden" name="ed_mod_user" id="ed_mod_user" />
							</td>
							<td>
								<input type="checkbox" name="edit_finalized" id="edit_finalized" onclick="return ed_changeFinalized();"/><div style="margin-top: 2px; float: left">Finalize </div>
								<input type="hidden" name="ed_finalized" id="ed_finalized" value="" />
							</td>
						</tr>
						<tr>
							<td class="formlabel" style="width: 100px">Date/Time: </td>
							<td style="width: 200px">
								<div style="display: inline;">
									<insta:datewidget name="ed_entered_date" id="ed_entered_date" />
									<input type="text" name="ed_entered_time" id="ed_entered_time" style="width: 50px" />
								</div>
							</td>
							<td class="formlabel">
								<div style="margin-top: 2px; float: left">Handover/Takeover:
									<insta:selectoptions name="ed_htover" id="ed_htover" opvalues="H,T"
										optexts="Handover,Takeover" value="" dummyvalue="-- Select --" dummyvalueId=""/>
								</div>
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