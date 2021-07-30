<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="flexjson.JSONSerializer"%>
<%@page import="com.insta.hms.common.ConversionUtils"%>
<%@page import="com.insta.hms.master.DoctorMaster.DoctorMasterDAO"%>
<%@page import="com.insta.hms.usermanager.UserDAO"%>
<%@page import="com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO"%>
<%@page import="org.apache.commons.beanutils.BasicDynaBean"%>
<%@page import="com.insta.hms.instaforms.AbstractInstaForms"%>
<%@page import="com.insta.hms.instaforms.PatientSectionDetailsDAO"%>

<fmt:parseNumber var="sectionId" type="number" value="${param.section_id}" />
<c:set var="finalized" value="${not empty section_finalize_status ? section_finalize_status[(sectionId).intValue()].finalized : 'N'}"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stn_right_access"
	value="${((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) && finalized != 'Y')}"/>
<style type="text/css">
.myAutoComplete{
		 width:12em; /* set width here or else widget will expand to fit its container */
	     padding-bottom:2em;
	  }
</style>
<script>
	YAHOO.util.Event.onContentReady("content", initAntenatalDetails);
	var antenatal_details_form = '${ifn:cleanJavaScript(param.form_name)}'
	var antenatal_doctors_json = ${doctors_json};
	var antenatal_user_doctor_id = '${user_doctor_id}';
</script>
<c:if test="${patient.visit_type == 'o'}">
	<input type="hidden" id="an_consulting_doctor_id" value="${antenatal_bean_doctor_id}"/>
	<input type="hidden" id="an_consulting_doctor_name" value="${antenatal_bean_doctor_name}"/>
</c:if>
<fieldSet class="fieldSetBorder">
	<legend class="fieldSetLabel" style="margin-top: 10px">Antenatal Details
		<c:forEach var="stn" items="${sys_generated_section}">
			<c:if test="${stn.section_mandatory && stn.section_id == param.section_id && stn_right_access}">
				<span class="star">*</span>
			</c:if>
		</c:forEach>
		<c:if test="${!(roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id))}">
			<i>[Read-Only]</i>
		</c:if>
		<c:if test="${finalized == 'Y'}">
			<i>[Finalized]</i>
		</c:if>
	</legend>
	<div class="resultList">
	<table class="detailList dialog_displayColumns" id="antenatalDetailsTable" cellspacing="0" cellpadding="0" border="0" width="100%" style="margin-top: 8px">
		<tr>
			<th>Visit Date</th>
			<th>Gestation Age</th>
			<th>Height of Fundus (cm)</th>
			<th>Presentation</th>
			<th>Relation of PP to Brim</th>
			<th>Foetal Heart (bpm)</th>
			<th>Urine</th>
			<th>BP (mmHg)</th>
			<th>Weight (Kg)</th>
			<th>Prescription Summary</th>
			<th>Consulting Doctor</th>
			<th>Next Visit Date</th>
			<th style="width: 16px;"></th>
			<th style="width: 16px;"></th>
		</tr>
	<c:set var="numAntenatalDetails" value="${fn:length(antenatalinfo)}"/>
	<c:forEach begin="1" end="${numAntenatalDetails+1}" var="i" varStatus="loop">
		<c:set var="antenataldetails" value="${antenatalinfo[i-1].map}"/>
		<c:if test="${empty antenataldetails}">
			<c:set var="style" value='style="display:none"'/>
		</c:if>
		<tr ${style}>
			<td>
				<img src="${cpath}/images/empty_flag.gif"/>
				<fmt:formatDate var="antenatal_visit_date" pattern="dd-MM-yyyy" value="${antenataldetails.visit_date}"/>
				<fmt:formatDate var="antenatal_next_visit_date" pattern="dd-MM-yyyy" value="${antenataldetails.next_visit_date}"/>
				<input type="hidden" name="antenatal_id" value="${antenataldetails.antenatal_id}"/>
				<input type="hidden" name="antenatal_visit_date" value="${antenatal_visit_date}"/>
				<input type="hidden" name="antenatal_next_visit_date" value="${antenatal_next_visit_date}"/>
				<input type="hidden" name="mr_no" value="${antenataldetails.mr_no}"/>
				<input type="hidden" name="antenatal_doctor_id" value="${antenataldetails.doctor_id}"/>
				<input type="hidden" name="antenatal_doctor_name" value="${antenataldetails.doctor_name}"/>
				<input type="hidden" name="antenatal_gestation_age" value="<c:out value='${antenataldetails.gestation_age}'/>"/>
				<input type="hidden" name="antenatal_height_fundus" value="<c:out value='${antenataldetails.height_fundus}'/>"/>
				<input type="hidden" name="antenatal_presentation" value="<c:out value='${antenataldetails.presentation}'/>"/>
				<input type="hidden" name="antenatal_rel_pp_brim" value="<c:out value='${antenataldetails.rel_pp_brim}'/>"/>
				<input type="hidden" name="antenatal_foetal_heart" value="<c:out value='${antenataldetails.foetal_heart}'/>"/>
				<input type="hidden" name="antenatal_urine" value="<c:out value='${antenataldetails.urine}'/>"/>
				<input type="hidden" name="antenatal_systolic_bp" value="<c:out value='${antenataldetails.systolic_bp}'/>"/>
				<input type="hidden" name="antenatal_diastolic_bp" value="<c:out value='${antenataldetails.diastolic_bp}'/>"/>
				<input type="hidden" name="antenatal_weight" value="<c:out value='${antenataldetails.weight}'/>"/>
				<input type="hidden" name="antenatal_prescription_summary" value="<c:out value='${antenataldetails.prescription_summary}'/>"/>
				<input type="hidden" name="antenatal_deleted" id="antenatal_deleted" value="false" />
				<input type="hidden" name="antenatal_edited" value="false" />
				<label>${antenatal_visit_date}</label>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.gestation_age}" length="20"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.height_fundus}" length="50"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.presentation}" length="30"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.rel_pp_brim}" length="50"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.foetal_heart}" length="30"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.urine}" length="20"/>
			</td>
			<td>
				<c:set var="systolic_bp">
					${antenataldetails.systolic_bp}<c:if  test="${not empty antenataldetails.diastolic_bp}">/</c:if>${antenataldetails.diastolic_bp}
				</c:set>
				<insta:truncLabel value="${systolic_bp}" length="15"/>
			</td> 
	
			<td>
				<insta:truncLabel value="${antenataldetails.weight}" length="20"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.prescription_summary}" length="30"/>
			</td>
			<td>
				<insta:truncLabel value="${antenataldetails.doctor_name}" length="20"/>
			</td>
			<td>
				<insta:truncLabel value="${antenatal_next_visit_date}" length="30"/>
			</td>
			<c:if test="${stn_right_access}">
				<td style="width: 16px; text-align: center">
					<a href="javascript:Cancel Antenatal" onclick="return cancelAntenatalDetails(this);" title="Cancel Antenatal" >
						<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
					</a>
				</td>
				<td style="width: 16px; text-align: center">
					<a name="antenatalEditAnchor" href="javascript:Edit Antenatal Details" onclick="return showEditAntenatalDialog(this);"
						title="Edit Antenatal Details">
						<img src="${cpath}/icons/Edit.png" class="button" />
					</a>
				</td>
			</c:if>
		</tr>
	</c:forEach>
</table>
<table class="addButton" style="width:100%; white-space:nowrap;">
	<tr>
		<td style="width:100%;"></td>
		<c:if test="${stn_right_access}">
			<td>
				<button type="button" name="btnAddAntenatal" id="btnAddAntenatal" title="Add Antenatal Details"
					onclick="showAddAntenatalDialog(this); return false;"
					class="imgButton"><img src="${cpath}/icons/Add.png"></button>
			</td>
		</c:if>
		<td style="padding-right:5px;">Finalize</td>
		<td> <span><input class="finalize" id="${param.section_id}_finalized"  type="checkbox" value="true" ${ finalized == 'Y' ? "checked" : "" } onclick="changeFinalized(this,${param.section_id});"  
																								${finalized == 'Y' ? ((roleId == 1 || roleId == 2 || actionRightsMap.undo_section_finalization == 'A') ? '' : 'disabled') : 
																								((roleId == 1 || roleId == 2 || fn:contains(section_rights, param.section_id)) ? '' : 'disabled')}></input>
																								<input type="hidden" value="${finalized}" name="${param.section_id}_finalized" ></span> </td>
	</tr>
</table>
</div>
</fieldSet>
<div id="addAntenatalDialog" style="display: none">
	<div class="bd">
		<div id="addAntenatalDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Antenatal Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Visit Date: </td>
							<td>
								<input type="hidden" name="d_antenatal_id" id="d_antenatal_id"/>
								<insta:datewidget name="d_antenatal_visit_date" id="d_antenatal_visit_date" btnPos="left"/>
							</td>
							<td class="formlabel">Gestation Age: </td>
							<td style="width:200px;" align="left">
								<input type="text" name="d_antenatal_gestation_age" id="d_antenatal_gestation_age"
								placeholder="enter age in weeks" style="width:11.6em" />&nbsp;
							</td>
						</tr>

					<tr>
						<td class="formlabel">Height Of Fundus: </td>
						<td style="width:240px;" align="left">
							<input type="text" name="d_antenatal_height_fundus" id="d_antenatal_height_fundus" style="width:11.6em"/> &nbsp;cm
						</td>
						<td class="formlabel">Presentation: </td>
						<td>
							<input type="text" name="d_antenatal_presentation" id="d_antenatal_presentation" style="width:11.6em"/>
						</td>
					</tr>

					<tr>
						<td class="formlabel">Relation Of PP To Brim: </td>
						<td>
							<input type="text" name="d_antenatal_relation_pp_brim" id="d_antenatal_relation_pp_brim"/>
						</td>

						<td class="formlabel">Foetal Heart: </td>
						<td style="width:200px;" align="left">
							<input type="text" name="d_antenatal_foetal_heart" id="d_antenatal_foetal_heart" style="width:11.6em"/>&nbsp;bpm
						</td>
					</tr>

					
					<tr>
						<td class="formlabel">Urine: </td>
						<td>
							<input type="text" name="d_antenatal_urine" id="d_antenatal_urine"/>
						</td>
						<td class="formlabel">Systolic BP: </td>
						<td style="width:60px" align="left">
							<input type="text" name="d_antenatal_systolic_bp" id="d_antenatal_systolic_bp" style="width:11.6em"/>&nbsp;mmHg
						</td>
					</tr>
					<tr>
						<td class="formlabel">Diastolic BP: </td>
						<td style="width:260px" align="left">
							<input type="text" name="d_antenatal_diastolic_bp" id="d_antenatal_diastolic_bp" style="width:11.6em"/>&nbsp;mmHg
						</td>
						
						<td class="formlabel">Weight: </td>
						<td style="width:200px;" align="left">
							<input type="text" name="d_antenatal_weight" id="d_antenatal_weight" style="width:11.6em"/>&nbsp;Kg
						</td>
					</tr>

					<tr>																		
						<td class="formlabel">Prescription Summary: </td>
						<td>
							<input type="text" name="d_antenatal_prescription_summary" id="d_antenatal_prescription_summary" style="width:11.6em"/>
						</td>	
						
						<td class="formlabel">Consulting Doctor: </td>
						<td>
							<div id="d_antenatal_doctor_ac" class="myAutoComplete">
								<input type="text" id="d_antenatal_doctor" value="${antenatal_bean_doctor_name}"/>
								<div id="d_antenatal_doctor_container" class="scrolForContainer" style="width:250px"></div>
								<input type="hidden" id="d_antenatal_doctor_id" value="" />
							</div>
						</td>				
					</tr>

					<tr>																
						<td class="formlabel">Next Visit Date: </td>
						<td>
								<insta:datewidget name="d_next_visit_date" valid="future" id="d_next_visit_date" btnPos="left"/>
						</td>
					</tr>
					</table>
		</fieldset>
	</div>
	<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="antenatalDetails_add_btn" id="antenatalDetails_add_btn" accessKey="A">
						<b><u>A</u></b>dd
					</button>
					<input type="button" name="antenatalDetails_cancel_btn" value="Close" id="antenatalDetails_cancel_btn"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editAntenatalDialog" style="display: none">
<input type="hidden" name="editAntenatalRowId" id="editAntenatalRowId" value=""/>
	<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Antenatal Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Visit Date: </td>
							<td>
								<input type="hidden" name="ed_antenatal_id" id="ed_antenatal_id"  extravalidation="setAntenatalDetailsEdited();"/>
								<insta:datewidget name="ed_antenatal_visit_date" id="ed_antenatal_visit_date" btnPos="left" onchange="setAntenatalDetailsEdited();"/>
							</td>
							<td class="formlabel">Gestation Age: </td>
							<td style="width:240px;" align="left">
								<input type="text" name="ed_antenatal_gestation_age" id="ed_antenatal_gestation_age"
								placeholder="Gestation age in Weeks" style="width:11.6em"  onchange="setAntenatalDetailsEdited();"/>&nbsp;
							</td>
						</tr>

					<tr>
						<td class="formlabel">Height Of Fundus: </td>
						<td style="width:240px;" align="left">
							<input type="text" name="ed_antenatal_height_fundus" id="ed_antenatal_height_fundus" style="width:11.6em" onchange="setAntenatalDetailsEdited();"/>&nbsp;cm
						</td>
						<td class="formlabel">Presentation: </td>
						<td>
							<input type="text" name="ed_antenatal_presentation" id="ed_antenatal_presentation" onchange="setAntenatalDetailsEdited();"/>
						</td>
					</tr>

					<tr>
						<td class="formlabel">Relation Of PP To Brim: </td>
						<td>
							<input type="text" name="ed_antenatal_relation_pp_brim" id="ed_antenatal_relation_pp_brim" onchange="setAntenatalDetailsEdited();"/>
						</td>

						<td class="formlabel">Foetal Heart: </td>
						<td style="width:200px;" align="left">
							<input type="text" name="ed_antenatal_foetal_heart" id="ed_antenatal_foetal_heart" style="width:11.6em" onchange="setAntenatalDetailsEdited();"/>&nbsp;bpm
						</td>
					</tr>

					<tr>
						<td class="formlabel">Urine: </td>
						<td>
							<input type="text" name="ed_antenatal_urine" id="ed_antenatal_urine" onchange="setAntenatalDetailsEdited();"/>
						</td>

						<td class="formlabel">Systolic BP: </td>	
						<td style="width:60px"align="left">
							<input type="text" name="ed_antenatal_systolic_bp" id="ed_antenatal_systolic_bp" style="width:11.6em" onchange="setAntenatalDetailsEdited();"/>&nbsp;mmHg
						</td>
					</tr>
					<tr>
						<td class="formlabel">Diastolic BP: </td>
						<td style="width:240px"align="left">
							<input type="text" name="ed_antenatal_diastolic_bp" id="ed_antenatal_diastolic_bp" style="width:11.6em" onchange="setAntenatalDetailsEdited();"/>&nbsp;mmHg
						</td>
						
						<td class="formlabel">Weight: </td>
						<td style="width:200px;" align="left">
							<input type="text" name="ed_antenatal_weight" id="ed_antenatal_weight" style="width:11.6em"  onchange="setAntenatalDetailsEdited();"/>&nbsp;kg
						</td>			
					</tr>

				<tr>																		
						<td class="formlabel">Prescription Summary: </td>
						<td>
							<input type="text" name="ed_antenatal_prescription_summary" id="ed_antenatal_prescription_summary" style="width:11.6em" onchange="setAntenatalDetailsEdited();"/>
						</td>	
						
						<td class="formlabel">Consulting Doctor: </td>
						<td>
							<div id="ed_antenatal_doctor_ac" class="myAutoComplete">
								<input type="text" id="ed_antenatal_doctor"/>
								<div id="ed_antenatal_doctor_container" class="scrolForContainer" style="width:250px"></div>
								<input type="hidden" id="ed_antenatal_doctor_id" value="" />
							</div>
						</td>				
					</tr>

					<tr>
						
						<td class="formlabel">Next Visit Date: </td>
						<td>
								<insta:datewidget name="ed_next_visit_date" valid="future" id="ed_next_visit_date" btnPos="left" extravalidation="setAntenatalDetailsEdited();"/>
						</td>
					</tr>
					</table>
				<table style="margin-top: 10">
					<tr>
						<td>
							<input type="button" id="edit_AntenatalDetails_Ok" name="editok" value="Ok">
							<input type="button" id="edit_AntenatalDetails_Cancel" name="cancel" value="Cancel" />
							<input type="button" id="edit_AntenatalDetails_Previous" name="previous" value="<<Previous" />
							<input type="button" id="edit_AntenatalDetails_Next" name="next" value="Next>>"/>
						</td>
					</tr>
				</table>
	</fieldset>
</div>
</div>

