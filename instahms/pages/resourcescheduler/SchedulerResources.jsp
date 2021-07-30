<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<c:set var="category" value="${category}"/>
<c:set var="selectedSchedules" value="${canlderList['headers']}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="primaryResource">
<c:choose>
	<c:when test="${category eq 'DOC'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.doctor"/></c:when>
	<c:when test="${category eq 'OPE'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.theatre"/></c:when>
	<c:when test="${category eq 'SNP'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.primaryresources"/></c:when>
	<c:when test="${category eq 'DIA'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.equipment"/></c:when>
</c:choose>
</c:set>
<c:set var="scheduledType">
<c:choose>
	<c:when test="${category eq 'DOC'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.doctor"/></c:when>
	<c:when test="${category eq 'OPE'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.surgery"/></c:when>
	<c:when test="${category eq 'SNP'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.service"/></c:when>
	<c:when test="${category eq 'DIA'}"><insta:ltext key="patient.resourcescheduler.schedulerresources.test"/></c:when>
</c:choose>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<input type="hidden" id="userCentId" name="userCentId" value="${userCenterId}"/>
<label id="datelbl" style="display:none"></label>
<input type="hidden" name="appointdate" id="appointdate" value=""/>

<fieldset class="fieldSetBorder" style="width:613px">
	<legend class="fieldSetLabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.patientdetails"/></legend>
	<table width="100%" class="schedulertable">
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.mrno"/></td>
			<td class="forminfo" colspan ="3" style="width:140px">
				<table width="100%">
				  <tr>
				  	<td style="padding: 0px 0px 0px 0px">
						<div id="mrnoAutoComplete">
							<input type="text" id="mrno" name="mrno" size="10"/>
							<div id="mrnoAcDropdown" style="width:400px;"></div>
						</div>
					</td>
					<td style="padding: 0px 0px 0px 0px">
						<span id="activePatient">
							<input type="checkbox" name="active_patient"
							id="active_patient" onclick="onActiveCheck();" checked/>
							&nbsp;<insta:ltext key="patient.resourcescheduler.schedulerresources.active"/>&nbsp;<insta:ltext key="patient.resourcescheduler.schedulerresources.only"/>
						</span>
					</td>
				  </tr>
				 </table>
			</td>
			<td class="formlabel">
				<label id="currentStatusField"><insta:ltext key="patient.resourcescheduler.schedulerresources.currentstatus"/></label>
			</td>
			<td class="forminfo" colspan="1" style="width:163px">
				<label id="currentStatusValue" style="width:20px;"><label id="currentstatus"></label></label>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.name"/></td>
			<td class="forminfo" colspan="2" style="width:140px">
				<input type="hidden" name="salutationName" value=""/>
				<input type="text" name="name" style="width:140px;" maxlength="50" id="patName"
				onblur="capWords(this)" onkeypress="if(getCode(event)==27) {dialog.cancel(); return true;}"/>
				<span class="star">&nbsp;*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.mobileno"/></td>
			<td class="forminfo" colspan="2">
				<div style="margin-top:12px;font-weight:normal">
					<div>
						<input type="hidden" id="patContact" name="contact"/>
						<input type="hidden" id="contact_valid" value="N"/>
						<select id="contact_country_code" class="dropdown" style="width:76px" name="contact_country_code">
							<c:if test="${empty defaultCountryCode}">				
									<option value='+' selected> - Select - </option>
							</c:if>
							<c:forEach items="${countryList}" var="list">
								<c:choose>
									<c:when test="${ list[0] == defaultCountryCode}">				
										<option value='+${list[0]}' selected> +${list[0]}(${ list[1]})  </option>
									</c:when>	
									<c:otherwise>
										<option value='+${list[0]}'> +${list[0]}(${list[1]})  </option>
									</c:otherwise>
								</c:choose>
														  
							</c:forEach>
						 </select>
						 <input type="text" class="field" id="contact_national" maxlength ="15" onkeypress="return enterNumOnlyzeroToNine(event)"
								 style="width:9.6em;padding-top:1px" />
							<span class="star">&nbsp;*</span>
							<img class="imgHelpText" id="contact_help" src="${cpath}/images/help.png"/>
					</div>
					<div>
						<span style="visibility:hidden;padding-left:10px;color:#f00" id="contact_error"></span>	
					</div>

				</div>
				
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.wardname"/></td>
			<td class="forminfo"><label id="wardName"></label></td>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.bedname"/></td>
			<td class="forminfo" colspan="3"><label id="bedName"></label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.complaint"/></td>
			<td colspan="5">
				<input type="text" name="complaint" id="complaint" style="width:400px;"/>
			</td>
		</tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder" style="width:613px">
	<legend class="fieldSetLabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.appointmentdetails"/></legend>
	<table width="100%" class="schedulertable">
		<tr>
		<td class="formlabel">${scheduledType}:</td>
		<td class="forminfo" >
		    <c:choose>
			    <c:when test="${category eq 'OPE'}">
			    	<div  id="snp_div" class="autoComplete" style="padding-bottom: 0em;width:160px">
						<input type="text" id="scNames" name="scNames" value="" style="width:400px;" onchange="checkFieldValue();"/>
						<input type="hidden" id="scheduleNameForAppointment" name="scheduleNameForAppointment" value=""/>
						<div id="scNameDropdown" style="width: 80em;"></div>
					</div>
			    </c:when>
			    <c:when test="${category eq 'DIA'}">
					<div id="snp_div" class="autoComplete" style="padding-bottom: 0em;width:160px">
						<input type="text" id="scNames" name="scNames" value="" style="width:400px;" onchange="checkFieldValue();"/>
						<input type="hidden" id="scheduleNameForAppointment" name="scheduleNameForAppointment" value=""/>
						<input type="hidden" id="conductingDoctorMandatory" name="conductingDoctorMandatory" value =""/>
						<div id="scNameDropdown" style="width: 40em;"></div>
					</div>
			    </c:when>
			    <c:when test="${category eq 'SNP'}">
					<div id="snp_div" class="autoComplete" style="padding-bottom: 0em;width:160px">
						<input type="text" id="scNames" name="scNames" value="" style="width:400px;" onchange="checkFieldValue();"/>
						<input type="hidden" id="scheduleNameForAppointment" name="scheduleNameForAppointment" value=""/>
						<div id="scNameDropdown" style="width: 50em;"></div>
					</div>
			    </c:when>
			    <c:when test="${category eq 'DOC'}">
					<select name="scheduleNameForAppointment" onchange="getResourceList();populateCenters(this,'add');" class="dropdown" id="scNames">
						  <option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
						  <c:forEach var="row" items="${selectedSchedules}">
						   <option value="${row['id']}">
							   ${row['schedulename']}
						   </option>
						 </c:forEach>
					</select>
			    </c:when>
			</c:choose>
			<c:choose>
				<c:when test="${category == 'SNP'}">
					<c:if test="${genPrefs.map.service_name_required == 'M'}">
						<span class="star">&nbsp;*</span>
					</c:if>
				</c:when>
				<c:when test="${category == 'OPE'}">
					<c:if test="${genPrefs.map.surgery_name_required == 'M'}">
						<span class="star">&nbsp;*</span>
					</c:if>
				</c:when>
				<c:otherwise>
					<span class="star">&nbsp;*</span>
				</c:otherwise>
			</c:choose>
	   </td>
	   <input type="hidden" name="centralResourceSchItemId" value=""/>
	 </tr>
	 <tr>
	 	<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.prescribing.doctor"/></td>
	    <td class="yui-skin-sam" valign="top">
			<div style="width: 138px">
			<input type="text" id="prescribing_doctor" name="prescribing_doctor" value="" style="width: 13em"/>
			<input type="hidden" id="presc_doc_id" name="presc_doc_id" value=""/>
				<div id="prescribing_doctorAcDropdown" style="width: 50em;"></div>
			</div>
	   </td>
			 
	 </tr>
	 <tr>
	 <td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.conducting.doctor"/></td>
	    <td class="yui-skin-sam" valign="top">
			<div style="width: 138px">
			<table>
			<tr>
				<td><input type="text" id="conducting_doctor" name="conducting_doctor" value="" style="width: 13em" /></td>
				<td><span class="star" id="cond_doc_star" style="visibility:visible;">*</span></td>
			</tr>
			</table>
			
			<input type="hidden" id="cond_doc_id" name="cond_doc_id" value=""/>
			<div id="conducting_doctorAcDropdown" style="width: 50em;"></div>
			</div>
	   	</td>
	   	
	   <c:choose>
	   		<c:when test="${category eq 'DOC'}">
	   			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.visittype"/></td>
	   			<td class="forminfo">
					<select name="scheduler_visit_type" id=" scheduler_visit_type" class="dropdown" style="width:135px">
						<option value="M"><insta:ltext key="patient.resourcescheduler.schedulerresources.main"/></option>
						<option value="F"><insta:ltext key="patient.resourcescheduler.schedulerresources.followup"/></option>
					</select>
				</td>
	   		</c:when>
	   		<c:otherwise>
	   			<td class="formlabel">${primaryResource}:</td>
	   			<td class="forminfo">
				  <select name="centralResource" id="centralResource" class="dropdown" style="width: 135px" onchange="changeCentralResourceValue(this);changeCenterValue(this)">
					  <option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
					  <c:forEach var="row" items="${selectedSchedules}">
					   <option value="${row['id']}">
						   ${row['schedulename']}
					   </option>
					 </c:forEach>
					</select>
					<span class="star">&nbsp;*</span>
				</td>
	   		</c:otherwise>
	   </c:choose>
	 </tr>
	   <c:choose >
		   	<c:when test="${category eq 'OPE'}">
		   		<c:if test="${mod_adv_ins}">
					<tr id="scheduler_prior_auth_row" style="display:none">
						<td class="formlabel">
							<insta:ltext key="patient.resourcescheduler.schedulerresources.priorauthno"/>
						</td>
						<td class="forminput">
							<input type="text" name="scheduler_prior_auth_no" id="scheduler_prior_auth_no" maxlength="50">
						</td>
						<td class="formlabel">
							<insta:ltext key="patient.resourcescheduler.schedulerresources.priorauthmode"/>
						</td>
						<td class="forminput">
							<insta:selectdb  name="scheduler_prior_auth_mode_id" id="scheduler_prior_auth_mode_id" value=""
								table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
								filtered="false" dummyvalue="${dummyvalue}"/>
						</td>
					</tr>
				</c:if>
			</c:when>
	   </c:choose>

	<tr>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.time"/>
		<label id="pastTime"></label></td>
		<td class="forminfo">
			<input type="hidden" value="" name="slotTime" id="slotTime"/>
			<select name="time" id="timeList" class="dropdown" onchange="slotTimeNull()" style="width:60px">
				<option value=''><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
				<c:forEach var="record" items="${timings.timeList}">
				   <c:set var="timeValue">
				   		<fmt:formatDate type="time" value="${record}" timeStyle="short"  pattern="HH:mm"/>
				   </c:set>
					<option value='${timeValue}'>${timeValue}</option>
				</c:forEach>
			</select>
			<span class="star">&nbsp;*</span>
		</td>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.duration"/></td>
		<td class="forminfo">
			<input type="text" maxlength="3" size="3" class="number" name="duration" value="${ifn:cleanHtmlAttribute(defaultDuration)}"
 					  onkeypress="return enterNumOnlyzeroToNine(event)">
 					  <span class="star">&nbsp;*</span>
		</td>
	</tr>
	<tr>
		<c:choose>
			<c:when test="${max_centers_inc_default == 1}">
				<input type="hidden" name="center_id" id="center_id" value="0"/>
				<input type="hidden" name="_center_id" id="_center_id" value="0">
			</c:when>
			<c:otherwise>
					<c:choose>
						<c:when test="${category == 'DOC'}">
							<c:choose>
								<c:when test="${userCenterId== 0}">
									<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.apptcenter"/> </td>
									<td class="forminfo">
									<div id="allcenter" style="display:block">
									<select class="dropdown" name="center_id" id="center_id" onchange="changeCenterId(this,'add')">
										<option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}">${center.map.center_name}</option>
										</c:forEach>
									</select>
									</div>
									<div id="belongcenter" style="display:none">
										<select class="dropdown" name="_center_id" id="_center_id" onchange="changeCenterId(this,'add')">
										</select>
									</div>
									<!-- <input type="hidden" name="ah_center_id" id="ah_center_id" value=""> -->
								</c:when>
								<c:otherwise>
									<input type="hidden" name="center_id" id="center_id" value="${userCenterId}"/>
									<input type="hidden" name="_center_id" id="_center_id" value="${userCenterId}">
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="center_id" id="center_id" value=""/>
							<input type="hidden" name="_center_id" id="_center_id" value="">
						</c:otherwise>
					</c:choose>
				</td>
			</c:otherwise>
		</c:choose>
		<input type="hidden" name="ah_center_id" id="ah_center_id" value="" />
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.remarks"/></td>
		<td class="forminfo" colspan="3">
			<input type="text" name="remarks" id="remarks" style="width:400px;"/>
		</td>
	</tr>
	<tr id="prevApptRow">
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.previousappointment"/></td>
		<td class="forminfo" colspan="3">
			<label id="prevAppointment"></label>
		</td>
	</tr>
	<tr id="prevNoShowRow">
		<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.noofnoshows"/></td>
		<td class="forminfo" colspan="3"><label id="noOfNoShows"></label></td>
	</tr>
</table>
<table width="100%">
	<tr>
		<td>
			<jsp:include page="SchedulerResourceDialog.jsp"/>
		</td>
	</tr>
</table>
<table style="width:550px">
	<tr>
		<td>
			<div id="recurringTable" style="margin-bottom:10px;width:550px;">
				<jsp:include page="RecurringAppointments.jsp"/>
			</div>
		</td>
	</tr>
</table>
</fieldset>

<fieldset class="fieldSetBorder" style="width:613px;">
	<legend class="fieldSetLabel" id="status_visit_label"><insta:ltext key="patient.resourcescheduler.schedulerresources.appointmentstatus"/></legend>
	<table width="100%" class="schedulertable">
		<tr id="statusRow" style="display:none">
			<td class="formlabel" id="addStatuslbl"><insta:ltext key="patient.resourcescheduler.schedulerresources.status"/></td>
			<td class="formlabel" style="display:none" id="changeStatuslbl"><insta:ltext key="patient.resourcescheduler.schedulerresources.changestatus"/></td>
			<td class="forminfo" style="width:140px">
				 <select name="status" class="dropdown" style="width:100px" onchange="onChangeStatus();">
					<option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select1"/></option>
				</select>
				<input type="hidden" name="editStatus" id="editStatus"/>
				<span class="star" id="statusStar">&nbsp;*</span>
			 </td>
			 <td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.cancelreason"/></td>
			 <td>
				<input type="text" name="cancel_reason" id="cancel_reason"  style="width:160px;" disabled/>
			</td>
		</tr>
		<tr id="visitIdRow" style="display:none">
			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.activevisit"/></td>
			<td class="forminfo" style="width:140px">
				<select name="patient_id" id="app_visit_id" class="dropdown" onchange="onChangeVisit();" disabled>
					<option value="None"><insta:ltext key="patient.resourcescheduler.schedulerresources.none"/></option>
				</select>
				<input type="hidden" name="visitType"/>
			</td>
			<c:choose>
				<c:when test="${category eq 'DOC'}">
					<td id="consultationTypeCell" style="display:none">
						<table>
							<tr>
					   			<td class="formlabel"><insta:ltext key="patient.resourcescheduler.schedulerresources.consultationtypes"/></td>
					   			<td class="forminfo">
									<select name="consultationTypes" id="consultationTypes" class="dropdown" style="width:135px">
										<option value=""><insta:ltext key="patient.resourcescheduler.schedulerresources.select"/></option>
										<c:forEach var="rowOP" items="${consultationTypeForOp}">
											<option value="${rowOP['consultation_type_id']}">
													   ${rowOP['consultation_type']}
											</option>
										</c:forEach>
									</select>
								</td>
							</tr>
						</table>
					</td>
					<td class="formlabel" id="emptyCell1">&nbsp;</td>
					<td class="forminfo" style="width:140px" id="emptyCell2">&nbsp;</td>
		   		</c:when>
			   	<c:otherwise>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" style="width:140px">&nbsp;</td>
				</c:otherwise>
			</c:choose>
		</tr>
	</table>
</fieldset>

<!-- Phone number validation -->

<insta:link type="js" file="select2.min.js"/>
<script>
(function(){
		var contact = $("#patContact");		
		var contactNational=$("#contact_national");
		var contactCountryCode=$("#contact_country_code");
		var contactHelp=$("#contact_help");
		var contactError =$("#contact_error");
		var contactValid = $("#contact_valid");
				
		contactCountryCode.select2();
		
		contactCountryCode.on("change",function(e){
			//get text for help menu and error text
		    getExamplePhoneNumber(this.value,contactHelp,contactError);
		});
		
		contactCountryCode.on('select2:select', function (e) {			
			contactNational.focus();
		});
		
		contactNational.on('blur',function(e,eventDataObj){
			clearErrorsAndValidatePhoneNumber(contact,contactValid,
					contactNational,contactCountryCode,contactError,'Y',eventDataObj);
		});
		// Get help text for patient_phone
		getExamplePhoneNumber(defaultCountryCode,contactHelp,contactError);
		
	})();
</script>

<!--  end of phonenumber -->