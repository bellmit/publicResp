<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<fmt:parseNumber var="sectionId" type="number" value="${param.section_id}" />
<c:set var="pacValidityDays" value='<%=GenericPreferencesDAO.getAllPrefs().get("pac_validity_days") %>'/>
<c:set var="finalized" value="${not empty section_finalize_status ? section_finalize_status[(sectionId).intValue()].finalized : 'N'}"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stn_right_access"
	value="${((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) && finalized != 'Y')}"/>
<script>
	YAHOO.util.Event.onContentReady("content", initPreAnaCheck);
	var pre_anaesthesthetic_form = '${ifn:cleanJavaScript(param.form_name)}'
	var pre_anaesthesthetic_doctors_json = ${doctors_json};
	var pac_user_doctor_id = '${user_doctor_id}';
	var validityDays = ${pacValidityDays};
	var searchType = '${not empty param.searchType ? param.searchType : 'visit'}';
	var visit_type = '${searchType == 'visit' ? 'patient.visit_type' : ''}';
	var op_type = '${searchType == 'visit' ? 'patient.op_type' : ''}';
	var screenid = '${screenId}';
</script>
<insta:js-bundle prefix="patient.pre.anaesthesthetic"/>
<c:if test="${screenId == 'op_prescribe'}">
	<input type="hidden" id="pac_consulting_doctor_id" value="${consultation_bean.doctor_name}"/>
	<input type="hidden" id="pac_consulting_doctor_name" value="${consultation_bean.doctor_full_name}"/>
</c:if>
	<table width="100%">
		<tr>
			<legend class="fieldSetLabel" style="margin-top: 10px">
				<td align="left">
					<font style="font-weight: bold"><insta:ltext key="patient.pre.anaesthesthetic.grid.header"/></font>
					<c:forEach var="stn" items="${sys_generated_section}">
						<c:if test="${stn.section_mandatory && stn.section_id == param.section_id && stn_right_access}">
							<span class="star">*</span>
						</c:if>
					</c:forEach>
					<c:if test="${!(roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id))}">
						<b><i>[Read-Only]</i></b>
					</c:if>
					<c:if test="${finalized == 'Y'}">
						<b><i>[Finalized]</i></b>
					</c:if>
				</td>
			</legend>
			<td align="right"><input type="checkbox" name="hide_invalid" id="hide_invalid" checked onchange="filterResults(this);"/>
				<font style="font-weight: bold"><insta:ltext key="patient.pre.anaesthesthetic.hide.invalid"/></font>
			</td>
		</tr>
	</table>
	
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="preAnaesthestheticTable" border="0" width="100%" style="margin-top: 8px">
		<tr>
			<th style="width: 200px"><insta:ltext key="patient.pre.anaesthesthetic.grid.Doctor"/></th>
			<th style="width: 120px"><insta:ltext key="patient.pre.anaesthesthetic.grid.PACConduDate"/></th>
			<th style="width: 120px"><insta:ltext key="patient.pre.anaesthesthetic.grid.PACValidityDate"/></th>
			<th style="width: 30px"><insta:ltext key="patient.pre.anaesthesthetic.grid.OutCome"/></th>
			<th><insta:ltext key="patient.pre.anaesthesthetic.grid.Remarks"/></th>
			<th style="width: 16px;"></th>
			<th style="width: 16px"></th>
		</tr>
		<c:set var="numPreAnaesthesthetic" value="${fn:length(preAnaesthestheticList)}"/>
		<c:forEach begin="1" end="${numPreAnaesthesthetic+1}" var="i" varStatus="loop">
			<c:set var="preAnaes" value="${preAnaesthestheticList[i-1].map}"/>
			<c:if test="${empty preAnaes}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<tr ${style}>
				<td>
					<fmt:formatDate var="preAnaes_condu_datetime" pattern="dd-MM-yyyy HH:mm" value="${preAnaes.pac_date}"/>
					<fmt:formatDate var="preAnaes_validity_datetime" pattern="dd-MM-yyyy HH:mm" value="${preAnaes.pac_validity}"/>
					<input type="hidden" name="patient_pac_id" value="${preAnaes.patient_pac_id}"/>
					<input type="hidden" name="conducted_date" value="${preAnaes_condu_datetime}"/>
					<input type="hidden" name="validity_date" value="${preAnaes_validity_datetime}"/>
					<input type="hidden" name="pac_doctor_id" value="${preAnaes.doctor_id}"/>
					<input type="hidden" name="pac_doctor_name" value="${preAnaes.doctor_name}"/>
					<input type="hidden" name="pac_remarks" value="<c:out value='${preAnaes.patient_pac_remarks}'/> "/>
					<input type="hidden" name="pac_status" value="${preAnaes.status}"/>
					<input type="hidden" name="delPreAnaes" id="delPreAnaes" value="false" />
					<input type="hidden" name="preAnaes_edited" value='false'/>
					<insta:truncLabel value="${preAnaes.doctor_name}" length="20"/>
				</td>
				<td>
					<label>${preAnaes_condu_datetime}</label>
				</td>
				<td>
					<label>${preAnaes_validity_datetime}</label>
				</td>
				<td>
					<c:choose>
						<c:when test="${preAnaes.status == 'F'}"><label><insta:ltext key="patient.pre.anaesthesthetic.grid.fit"/></label></c:when>
						<c:when test="${preAnaes.status == 'U'}"><label><insta:ltext key="patient.pre.anaesthesthetic.grid.unfit"/></label></c:when>
					</c:choose>
				</td>
				<td>
					<insta:truncLabel value="${preAnaes.patient_pac_remarks}" length="80"/>
				</td>
				<td style="text-align: center">
					<c:choose>
						<c:when test="${screenId != 'op_prescribe' || (not empty preAnaes && preAnaes.doctor_id ne consultation_bean.doctor_name)}">
							<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button"  />
						</c:when>
						<c:otherwise>
							<c:if test="${stn_right_access}">
								<a href="javascript:Cancel Pre Anaesthesthetic Checkup" onclick="return cancelPreAnaes(this);" title='<insta:ltext key="patient.pre.anaesthesthetic.grid.cancle.image.title"/>' >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</c:if>
						</c:otherwise>
					</c:choose>
				</td>
				<td style="text-align: center">
					<c:choose>
						<c:when test="${screenId != 'op_prescribe' || (not empty preAnaes && preAnaes.doctor_id ne consultation_bean.doctor_name)}">
							<img src="${cpath}/icons/Edit1.png" class="imgDelete button" />
						</c:when>
						<c:otherwise>
							<c:if test="${stn_right_access}">
								<a name="_editPreAnaesAnchor" href="javascript:Edit Pre Anaesthesthetic Checkup" onclick="return showEditPreAnaesDialog(this);"
									title='<insta:ltext key="patient.pre.anaesthesthetic.grid.edit.image.title"/>'>
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</c:if>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</c:forEach>
	</table>
	<table class="addButton" style="width: 100%; white-space: nowrap;">
		<tr>
			<td style="width:100%;"></td>
			<c:if test="${stn_right_access}">
				<td>
					<c:choose>
						<c:when test="${screenId == 'op_prescribe'}">
							<button type="button" name="btnAddPreAnaes" id="btnAddPreAnaes" title='<insta:ltext key="patient.pre.anaesthesthetic.grid.add.button.title"/>'
								onclick="showAddPreAnaesDialog(this); return false;"
								class="imgButton"><img src="${cpath}/icons/Add.png"></button>
						</c:when>
						<c:otherwise>
							<img src="${cpath}/icons/Add1.png" class="imgDelete button" />
						</c:otherwise>
					</c:choose>
				</td>
			</c:if>
			<td style="padding-right:5px;">Finalize</td>
			<td> <span><input class="finalize" id="${param.section_id}_finalized"  type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${param.section_id});"  
																								${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																								((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) ? '' : 'disabled')}></input>
																								<input type="hidden" value="${finalized}" name="${param.section_id}_finalized" ></span> </td>
		</tr>
	</table>

<div id="addPreAnaesDialog" style="display: none">
	<div class="bd">
		<div id="addPreAnaesDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="patient.pre.anaesthesthetic.adddialog.header"/></legend>
				<table class="formtable">
					<tr>
						<td align="center">
							<input type="hidden" name="d_patient_pac_id" id="d_patient_pac_id" value=""/>
							<input type="radio" name="d_anaesthesthetic_status" id="d_anaesthesthetic_status" value="F" checked >
							<insta:ltext key="patient.pre.anaesthesthetic.adddialog.fit"/>
						</td>
						<td>
							<input type="radio" name="d_anaesthesthetic_status" id="d_anaesthesthetic_status" value="U" >
							<insta:ltext key="patient.pre.anaesthesthetic.adddialog.unfit"/>
						</td>
						<td class="formlabel"><insta:ltext key="patient.pre.anaesthesthetic.adddialog.Doctor"/> :</td>
						<td>
							<div id="d_pac_doctor_ac">
								<input type="text" id="d_pac_doctor_name" value=""/>
								<div id="d_pac_doctor_container" class="scrolForContainer" style="width: 250px"></div>
								<input type="hidden" id="d_pac_doctor_id" value="" />
							</div>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="patient.pre.anaesthesthetic.adddialog.conducted.date"/> :</td>
						<td>
							<insta:datewidget id="d_conducted_date" name="d_conducted_date"/>
							<input type="text" class="timefield" id="d_conducted_time"/>
							<span class="star">*</span>
						</td>
						<td class="formlabel"><insta:ltext key="patient.pre.anaesthesthetic.adddialog.validit.date"/> :</td>
						<td>
							<insta:datewidget id="d_validity_date" name="d_validity_date" />
							<input type="text" class="timefield" id="d_validity_time" name="d_validity_time" />
							<span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td colspan="4" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">
						<insta:ltext key="patient.pre.anaesthesthetic.adddialog.remarks"/> :</font>
							<span class="star">*</span>
						</td>
					</tr>
					<tr>
						<td colspan="4" align="center">
							<textarea name="d_pac_remarks" id="d_pac_remarks" style="width: 553px; height: 119px;" ></textarea>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="Add_PreAnaes_bt" id="Add_PreAnaes_bt" accesskey="A" >
						<b><u><insta:ltext key="patient.pre.anaesthesthetic.adddialog.A"/></u></b><insta:ltext key="patient.pre.anaesthesthetic.adddialog.dd"/>
					</button>
					<input type="button" name="Close_PreAnaes_bt" value='<insta:ltext key="patient.pre.anaesthesthetic.adddialog.close"/>' id="Close_PreAnaes_bt"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editPreAnaesDialog" style="display: none">
	<input type="hidden" name="editPreAnaesRowId" id="editPreAnaesRowId" value=""/>
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="patient.pre.anaesthesthetic.editdialog.header"/></legend>
			<table class="formtable">
				<tr>
					<td align="center">
						<input type="radio" name="ed_anaesthesthetic_status" id="ed_anaesthesthetic_status" value="F" onchange="setPreAnaesEdited();">
						<insta:ltext key="patient.pre.anaesthesthetic.editdialog.fit"/>
						<input type="hidden" name="ed_patient_pac_id" id="ed_patient_pac_id" value=""/>
					</td>
					<td>
						<input type="radio" name="ed_anaesthesthetic_status" id="ed_anaesthesthetic_status" value="U" onchange="setPreAnaesEdited();">
						<insta:ltext key="patient.pre.anaesthesthetic.editdialog.unfit"/>
					</td>
					<td class="formlabel"><insta:ltext key="patient.pre.anaesthesthetic.editdialog.doctor"/> : </td>
					<td>
						<div id="ed_pac_doctor_ac">
							<input type="text" id="ed_pac_doctor_name" value=""/>
							<div id="ed_pac_doctor_container" class="scrolForContainer" style="width: 250px"></div>
							<input type="hidden" id="ed_pac_doctor_id" value="" />
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.pre.anaesthesthetic.editdialog.conducted.date"/> : </td>
					<td>
						<insta:datewidget id="ed_conducted_date" name="ed_conducted_date" extravalidation="setPreAnaesEdited();"/>
						<input type="text" class="timefield" id="ed_conducted_time" onchange="setPreAnaesEdited();"/>
						<span class="star">*</span>
					</td>
					<td class="formlabel"><insta:ltext key="patient.pre.anaesthesthetic.editdialog.valid.date"/> : </td>
					<td>
						<insta:datewidget id="ed_validity_date" name="ed_validity_date" extravalidation="setPreAnaesEdited();"/>
						<input type="text" class="timefield" id="ed_validity_time" name="ed_validity_time" onchange="setPreAnaesEdited();"/>
						<span class="star">*</span>
					</td>
				</tr>
				<tr>
					<td colspan="4" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold"><insta:ltext key="patient.pre.anaesthesthetic.editdialog.remarks"/> :</font>
					<span class="star">*</span>
					</td>
				</tr>
				<tr>
					<td colspan="4" align="center">
						<textarea name="ed_pac_remarks" id="ed_pac_remarks" rows="10" cols="59" onchange="setPreAnaesEdited();" ></textarea>
					</td>
				</tr>
			</table>
			<table style="margin-top: 10">
				<tr>
					<td>
						<input type="button" id="edit_preAnaes_Ok" name="editok" value='<insta:ltext key="patient.pre.anaesthesthetic.editdialog.Ok"/>'>
						<input type="button" id="edit_preAnaes_Cancel" name="cancel" value='<insta:ltext key="patient.pre.anaesthesthetic.editdialog.Cancel"/>' />
						<input type="button" id="edit_preAnaes_Previous" name="previous" value='<insta:ltext key="patient.pre.anaesthesthetic.editdialog.Previous"/>' />
						<input type="button" id="edit_preAnaes_Next" name="next" value='<insta:ltext key="patient.pre.anaesthesthetic.editdialog.Next"/>'/>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>
