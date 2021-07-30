<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page import="com.insta.hms.common.Encoder" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="hijricalendar" value="<%= GenericPreferencesDAO.getGenericPreferences().getHijriCalendar()%>" />

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title><insta:ltext key="registration.patient.newbornregistration.newbornregistration"/></title>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="registration/registrationCommon.js"/>
	<insta:link type="js" file="ipservices/newbornRegistration.js"/>

<script>
var hijriPref = '${hijricalendar}';
var defaultBillType = '${centerPrefs.map.pref_default_ip_bill_type}';
var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
</script>
<insta:js-bundle prefix="registration.patient"/>
<insta:js-bundle prefix="registration.newbornregistration"/>
</head>

<body onload="init()" class="yui-skin-sam">
<h1><insta:ltext key="registration.patient.newbornregistration.newbornregistration"/></h1>

<insta:feedback-panel/>
<insta:patientdetails  visitid="${motherdetails.map.patient_id}" fieldSetTitle="Mother Details"/>
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.patient.newbornregistration.otherdetails"/></legend>
	<table class="patientdetails" style="width:100%">
		 <tr>
		 	<fmt:formatDate var="rdate" value="${motherdetails.map.reg_date}" pattern="dd-MM-yyyy"/>
 			<fmt:formatDate var="rtime" value="${motherdetails.map.reg_time}" pattern="HH:mm"/>
 			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.reg"/>.&nbsp;<insta:ltext key="registration.patient.newbornregistration.date.and.time"/>:</td>
			<td class="forminfo"> ${rdate} ${rtime}</td>
 			<td class="formlabel"></td>
			<td></td>
			<td class="formlabel"></td>
			<td></td>
       	</tr>
	</table>
</fieldset>

<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>

<form name="mainform" action="Ipservices.do" method="post">

<input type="hidden" name="_method" value="register" />
<input type="hidden" name="mrNo" value="${motherdetails.map.mr_no}"/>
<input type="hidden" name="patientid" value="${motherdetails.map.patient_id}"/>
<input type="hidden" name="patientbed" value="${motherdetails.map.bill_bed_type}"/>
<input type="hidden" name="patientorg" value="${motherdetails.map.org_id}"/>
<input type="hidden" name="patientDept" value="${motherdetails.map.dept_id}"/>
<input type="hidden" name="patientWard" value="${motherdetails.map.reg_ward_id}"/>
<input type="hidden" name="patientRegDate" value="${rdate}">
<input type="hidden" name="patientRegTime" value="${rtime}">


<jsp:useBean id="currentDate" class="java.util.Date"/>
<fmt:formatDate var="dateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="timeVal" value="${currentDate}" pattern="HH:mm"/>

<input type="hidden" name="date" id="date" value="${dateVal}">
<input type="hidden" name="time" id="time" value="${timeVal}">

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.patient.newbornregistration.babyregistration"/></legend>
	  <table class="formtable">
    	<tr>
      		<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.name"/>:</td>
      		<td class="forminfo">
      			<table>
      				<tr>
      					<td>
      						<select style="width: 5em" name="salutation" id="salutation" class="dropdown">
								<option value="SALU0000"><insta:ltext key="ui.label.bo"/></option>
								<option value="SALU0008" ><insta:ltext key="ui.label.baby"/></option>
							</select>
      					</td>
      					<td>
      						<input type="text"  name="firstName" id="firstName" size="18" class="text-input" value="${motherdetails.map.patient_name}" >
			        	</td>
			        	<td>
			        		<input type="text"  name="lastName" id="lastName" size="18"
			        		class="text-input" value="${motherdetails.map.middle_name} ${motherdetails.map.last_name}">
      					</td>
      					<td>
      						<span class="star">*</span>
      					</td>
      				</tr>
      			</table>
      		</td>
	        <td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.gender"/>:</td>
			<td>
				<select style="width: 8em" name="gender" id="gender" class="dropdown">
				  <option value=""><insta:ltext key="registration.patient.newbornregistration.genderoption"/></option>
				  <option value="M" ><insta:ltext key="registration.patient.newbornregistration.male"/></option>
				  <option value="F"><insta:ltext key="registration.patient.newbornregistration.female"/></option>
				</select>
				<span class="star">*</span>
				
			</td>
			<c:if test="${not empty regPref.nationality}">
				<td class="formlabel">${ifn:cleanHtml(regPref.nationality)}:</td>
				<td>
					<insta:selectdb name="nationalityId" id="nationalityId" table="country_master"
							class="field" style="width:140px;" dummyvalue="${dummyvalue}"
							size="1" valuecol="country_id" displaycol="country_name" usecache="true"/>

					<c:if test="${not empty regPref.nationality_validate && regPref.nationality_validate eq 'A'}">
						<span class="star">*</span>
					</c:if>
				</td>
			</c:if>
	    </tr>
   		<tr>
      		<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.date.and.timeofbirth"/>:</td>
			<td class="forminfo">
						<%-- <insta:datewidget name="dateOfBirth" value="${dateVal}" valid="past" btnPos="left" /> --%>
						<input type="hidden" name="dateOfBirth" value="${dateVal}" />
						<input type="text" class="field" style="width:30px;" size="2"
							maxlength="2"  name="dobDay" onkeypress="return enterNumOnly(event)" 
						 	id="dobDay" value="DD" onFocus="if (this.value == 'DD') { this.value = ''}" 
							onBlur="if (this.value == '') { this.value = 'DD'} else { if(hijriPref == 'Y') gregorianToHijri(); }" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("gregDay")) %>"
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'} required/>
						<input type="text" class="field" style="width:25px;" size="2"
							maxlength="2" name="dobMonth" id="dobMonth" value="MM" 
							onFocus="if (this.value == 'MM') { this.value = ''}"
							onBlur="if (this.value == '') { this.value = 'MM'} else { if(hijriPref == 'Y') gregorianToHijri(); }" onkeypress="return enterNumOnly(event)" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("gregMonth")) %>" 
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'} required/>
						<input type="text" class="field" size="4" maxlength="4" name="dobYear" id="dobYear"
							style="width:40px;"
							onblur="if (this.value == '') { this.value = 'YY' } else { return checkBabyDOBYear(); }"
							 value="YY" onkeypress="return enterNumOnly(event)"  
							onFocus="if (this.value == 'YY') { this.value = ''}" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("gregYear")) %>"
							${(empty patient || actionRightsMap.edit_first_name == 'A' || roleId == 1 || roleId == 2) ? '':'readonly'} required/>
							<insta:ltext key="registration.patient.newbornregistration.date.and.timeofbirth.and"/>
						<input type="text" name="timeOfBirth" class="timefield" value="${timeVal}" maxlength="5"/>
						<span class="star">*</span>						
						<td class="formlabel"><insta:ltext key="ui.label.delivery.type"/>:</td>
						<td>
							<select name="deliveryType" id="deliveryType" class="dropdown" onchange="disableEnableIndicationForCS();">
							<option value="">--Select--</option>
								<option value="N"><insta:ltext key="storemgmt.stocktransfer.addshow.normal"/></option>
								<option value="C" ><insta:ltext key="ui.label.delivery.caesarean"/></option>
							</select>
						</td>						
						<td class="formlabel"><insta:ltext key="ui.label.indication.for.caesarean.section"/>:</td>
						<td>
							<insta:selectdb name="caesareanIndicationId"
								table="indication_for_caesarean_section" valuecol="id"
								value=""
								displaycol="indication"
								dummyvalue="${dummyvalue}" dummyvalueId=""
								orderby="indication" />
						</td>
				<tr>
								<c:if test="${hijricalendar=='Y'}">
								<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.date.and.timeofbirth.hijri"/>:</td>
								<td style="width: 120px; white-space: nowrap;">
								<input type="text" class="field" style="width:30px;" size="2"
										maxlength="2"  name="dobHDay" onkeypress="return enterNumOnly(event)" 
									 	id="dobHDay" value="<insta:ltext key="registration.patient.show.dd.text"/>" onFocus="if (this.value == '<insta:ltext key="registration.patient.show.dd.text"/>') { this.value = ''}"
										onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.dd.text"/>'} else { if(hijriPref == 'Y') hijriToGregorian(); }" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("hijriDay")) %>">


									<input type="text" class="field" style="width:30px;" size="2"
										maxlength="2" name="dobHMonth" id="dobHMonth" value="<insta:ltext key="registration.patient.show.mm.text"/>"
										onFocus="if (this.value == '<insta:ltext key="registration.patient.show.mm.text"/>') { this.value = ''}"
										onBlur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.mm.text"/>'} else { if(hijriPref == 'Y') hijriToGregorian();}" onkeypress="return enterNumOnly(event)" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("hijriMonth")) %>">

									<input type="text" class="field" size="4" maxlength="4" name="dobHYear" id="dobHYear"
										style="width:40px;" onblur="if (this.value == '') { this.value = '<insta:ltext key="registration.patient.show.yyyy.text"/>' } else { if(hijriPref == 'Y') hijriToGregorian();}"
										 value="<insta:ltext key="registration.patient.show.yyyy.text"/>" onkeypress="return enterNumOnly(event)" 
										onFocus="if (this.value == '<insta:ltext key="registration.patient.show.yyyy.text"/>') { this.value = ''}" value="<%= Encoder.cleanHtmlAttribute((String)request.getParameter("hijriYear")) %>"><%-- <insta:ltext key="registration.patient.commonlabel.or.within.brackets"/> --%>
								
							    	<img title='<insta:ltext key="patient.registration.hijri.calendar.range.note"/>' src="/instahms/images/help.png" class="imgHelpText">
								
								</td>
								</c:if>
			</td
    	</tr>
	  </table>
</fieldset>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.patient.newbornregistration.bedallocation"/></legend>
	  <table class="formtable">
	  <table class="formtable" style="margin-top: 5px;">
		<tr>
			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.department"/>:</td>
			<td>
				<select name="babyAttendingDeptIP" id="babyAttendingDeptIP" class="dropdown" onchange="onChangeDepartment()">
					<option value=""><insta:ltext key="registration.patient.newbornregistration.departmentoption"/></option>
					<c:forEach items="${arrdeptDetails}" var="deptDetails">
						<option value="${deptDetails.DEPT_ID}">${deptDetails.DEPT_NAME}</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.doctor"/>:</td>
			<td colspan="2" valign="top">
				<%-- Doctor - Department Autocomplete --%>

				<div id="doc_dept_wrapper" style="width: 250px;">
				<input type="text" name="doctor_name" id="doctor_name" style="width: 250px;"/>
				<div id="doc_dept_dropdown"></div>
				<input type="hidden" name="babyAttendingDocIP" id="babyAttendingDocIP">
			</td>
			<td></td>
			<td></td>
			<td></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.bedtype"/>:</td>
			<td>
				<select name="bedtype" id="bedtype" class="dropdown" onchange="onChangeBedType(this, document.mainform.ward)">
					<option value=""><insta:ltext key="registration.patient.newbornregistration.selectoption"/></option>
				</select>
			</td>
			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.ward"/>:</td>
			<td>
				<select name="ward" id="ward" class="dropdown">
					<option value=""><insta:ltext key="registration.patient.newbornregistration.selectoption"/></option>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.paymenttype"/>:</td>
			<td>
				  <select name="billtype" id="billtype" class="dropdown">
					<option value="C"><insta:ltext key="registration.patient.newbornregistration.billlater"/></option>
					<option value="P"><insta:ltext key="registration.patient.newbornregistration.billnow"/></option>
				  </select>
			</td>
			<td class="formlabel"><insta:ltext key="registration.patient.newbornregistration.remarks"/>:</td>
			<td>
				<input type="text" name="childBirthRemarks" id="childBirthRemarks"/>
			</td>
        </tr>
    </table>
</fieldset>

<div class="screenActions">
	<button type="button" class="button" name="register" id="register"
		accesskey="R" onclick="validate()"><b><u><insta:ltext key="registration.patient.newbornregistration.r"/></u></b><insta:ltext key="registration.patient.newbornregistration.egister"/></button>
	<input type="reset" class="button" value="Reset"/>
</div>

<br/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="registration.patient.newbornregistration.babydetails"/></legend>
	<table>
	<c:forEach var="baby" items="${existBaby}">
		<tr>
			<td class="fieldSetLabel" style="width :10em">${baby.MR_NO}</td>
			<td class="fieldSetLabel" style="width :10em">${baby.PATIENT_ID}</td>
			<td class="fieldSetLabel" style="width :20em">${baby.PATIENT_NAME} ${baby.MIDDLE_NAME}</td>
			<td class="fieldSetLabel" style="width :10em">${baby.DATEOFBIRTH}</td>
			<td class="fieldSetLabel" style="width :10em">${baby.TIMEOFBIRTH}</td>
			<td class="fieldSetLabel" style="width :10em">${baby.PATIENT_GENDER}</td>
			<td class="fieldSetLabel" style="width :20em">&nbsp;</td>
		</tr>
	</c:forEach>
	</table>
</fieldset>
</form>
<script>
	var bedCharges = ${bedChargesJson};
	var nationalityLabel = '${regPref.nationality}';
	var nationalityValidate	 = '${regPref.nationality_validate}';
	var jDocDeptNameList = <%= request.getAttribute("docDeptNameList") %>;
</script>
</body>
</html>
