<%@tag pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="mrno" required="true" %>

<%@ attribute name="addExtraFields" required="false" %>		<%-- pass true if want to add extra fields --%>
<%@ attribute name="showClinicalInfo" required="false" %>
<%@ attribute name="fieldSetTitle" required="false" %>
<%@ attribute name="tableID" required="false" %>

<%--
	Example Usage:
	To display a patient general details demography, we would specify the tag as:
        <insta:patientgeneraldetails  mrno="${patient.mr_no}"/>

Parameter :
 mrno :  the mrno of a patient
 addBox (optional): set to false if you dont want a box around, it will add only the rows.
 addExtraFields (optional): set to true if you additional fields like Blood Group to be shown.
--%>

<%
java.util.List allAllergies = null;
java.util.Map patient = null;

if (patient == null && mrno != null) {
	patient =  com.insta.hms.Registration.PatientDetailsDAO.getPatientGeneralDetailsMap(mrno);
	request.setAttribute("patient", patient);
}

if (request.getAttribute("patient") != null) patient = (java.util.Map) request.getAttribute("patient");
if (patient != null) {
	allAllergies = com.insta.hms.outpatient.AllergiesDAO.getActiveAllergiesForPatient(patient.get("mr_no").toString());
}
request.setAttribute("allAllergies", allAllergies);
request.setAttribute("allAllergiesJSON", new flexjson.JSONSerializer().exclude("class").deepSerialize(com.insta.hms.common.ConversionUtils.copyListDynaBeansToMap(allAllergies)));

//Patient Custom Fields & List Values
com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO.patientDetailsCustomFields(showClinicalInfo);

%>

<c:if test="${empty addBox}"><c:set var="addBox" value="true"/></c:if>
<c:if test="${empty addExtraFields}"><c:set var="addExtraFields" value="false"/></c:if>
<c:if test="${empty showClinicalInfo}"><c:set var="showClinicalInfo" value="false"/></c:if>
<c:if test="${empty fieldSetTitle}"><c:set var="fieldSetTitle" value="Patient Details"/></c:if>

<c:set var="male">
	<insta:ltext key="patientdetails.common.tag.male"/>
</c:set>
<c:set var="female">
	<insta:ltext key="patientdetails.common.tag.female"/>
</c:set>
<c:set var="couple">
	<insta:ltext key="patientdetails.common.tag.couple"/>
</c:set>
<c:set var="activeStatus">
	<insta:ltext key="patientdetails.common.tag.active"/>
</c:set>
<c:set var="inactiveStatus">
	<insta:ltext key="patientdetails.common.tag.inactive"/>
</c:set>

<fieldset class="fieldSetBorder hide-patient-details">
<legend class="fieldSetLabel">${fieldSetTitle}</legend>
<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%" id="patientDetailsTab">

<tr>
	<td class="formlabel"><insta:ltext key="ui.label.mrno"/>:</td>
	<td class="forminfo"><div title="${patient.mr_no}">${patient.mr_no}</div></td>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.name"/>:</td>
	<td class="forminfo ${patient.vip_status=='Y' ? 'vipIndicator' : ''}">
		<div title="${patient.patient_name} ${patient.middle_name} ${patient.last_name}">
			${patient.patient_name} ${patient.middle_name} ${patient.last_name}
		</div>
	</td>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.age.gender"/>:</td>
	<td class="forminfo">
		<c:if test="${not empty patient.dateofbirth}">
			<c:set var="date_of_birth">(<fmt:formatDate value="${patient.dateofbirth}" pattern="dd-MM-yyyy"/>)
			</c:set>
		</c:if>
		<c:if test="${not empty patient.patient_gender && patient.patient_gender == 'M'}">
			<c:set var="gender" value="${male}"/>
		</c:if>
		<c:if test="${not empty patient.patient_gender && patient.patient_gender == 'F'}">
			<c:set var="gender" value="${female}"/>
		</c:if>
		<c:if test="${not empty patient.patient_gender && patient.patient_gender == 'C'}">
			<c:set var="gender" value="${couple}"/>
		</c:if>
		<div title="${patient.age_text} / ${gender} ${date_of_birth}">
			${patient.age_text} ${date_of_birth} / ${gender}
		<div>
	</td>
</tr>

<tr>
	<td class="formlabel"><insta:ltext key="patientdetails.common.tag.contactno"/>:</td>
	<td class="forminfo"><div title="${patient.patient_phone}">${patient.patient_phone}</div></td>
</tr>

<c:if test="${not empty customFields}">
	<c:set var="index" value="0"/>
	<c:forEach var="field" items="${customFields}" varStatus="status">
		<c:if test="${not empty patient[field.txColumnName]}">
			<c:if test="${index%3 == 0}">
				<tr>
			</c:if>
				<td class="formlabel"><insta:truncLabel value="${field.label}" length="10"/>:</td>
				<td class="forminfo"><div title="${patient[field.txColumnName]}">${patient[field.txColumnName]}</div></td>
			<c:if test="${index%3 == 2 || status.last}">
				</tr>
			</c:if>
			<c:set var="index" value="${index+1}"/>
		</c:if>
	</c:forEach>
</c:if>


<c:set var="noAllergies" value=""/>
<c:set var="medAllergies" value=""/>
<c:set var="foodAllergies" value=""/>
<c:set var="otherAllergies" value=""/>

<c:set var="haveNoAllergies" value="false"/>
<c:set var="haveMedAllergies" value="false"/>
<c:set var="haveFoodAllergies" value="false"/>
<c:set var="haveOtherAllergies" value="false"/>

<c:forEach var="allergy_bean" items="${allAllergies}">
	<c:set var="allergy" value="${allergy_bean.map}"/>
	<c:if test="${allergy.status == 'A'}">
		<c:choose>
			<c:when test="${allergy.allergy_type == 'N'}">
				<c:if test="${haveNoAllergies}">
					<c:set var="noAllergies" value="${noAllergies},"/>	
				</c:if>
				<c:set var="noAllergies" value="${noAllergies}${allergy.allergy}"/>
				<c:set var="haveNoAllergies" value="true"/>
			</c:when>
			<c:when test="${allergy.allergy_type == 'M'}">
				<c:if test="${haveMedAllergies}">
					<c:set var="medAllergies" value="${medAllergies},"/>	
				</c:if>
				<c:set var="medAllergies" value="${medAllergies}${allergy.allergy}"/>
				<c:set var="haveMedAllergies" value="true"/>
			</c:when>
			<c:when test="${allergy.allergy_type == 'F'}">
				<c:if test="${haveFoodAllergies}">
					<c:set var="foodAllergies" value="${foodAllergies},"/>	
				</c:if>
				<c:set var="foodAllergies" value="${foodAllergies}${allergy.allergy}"/>
				<c:set var="haveFoodAllergies" value="true"/>
			</c:when>
			<c:when test="${allergy.allergy_type == 'O'}">
				<c:if test="${haveOtherAllergies}">
					<c:set var="otherAllergies" value="${otherAllergies},"/>	
				</c:if>
				<c:set var="otherAllergies" value="${otherAllergies}${allergy.allergy}"/>
				<c:set var="haveOtherAllergies" value="true"/>
			</c:when>
		</c:choose>
	</c:if>
</c:forEach>

<c:if test="${showClinicalInfo == true && (haveNoAllergies || haveMedAllergies || haveFoodAllergies || haveOtherAllergies)}">
	<insta:js-bundle prefix="patientdetails"/>
	<tr>
		<td class="formlabel"><b><font id="allergiesLabel" style=""><insta:ltext key="patientdetails.common.tag.allergies"/>:</font></b></td>
		<td colspan="8" class="forminfo" id="AllergiesColId">
			<c:if test="${haveNoAllergies}">  <insta:ltext key="patientdetails.common.tag.noallergies"/> ${noAllergies}</c:if>
			<c:if test="${haveMedAllergies}">  <insta:ltext key="patientdetails.common.tag.med"/> ${medAllergies}</c:if>
			<c:if test="${haveFoodAllergies}"> / <insta:ltext key="patientdetails.common.tag.food"/> ${foodAllergies}</c:if>
			<c:if test="${haveOtherAllergies}"> / <insta:ltext key="patientdetails.common.tag.other"/>${otherAllergies}</c:if>
		</td>
	</tr>
	<script>
		var allAllergiesJSON = <%= request.getAttribute("allAllergiesJSON") %> ;
		var allergyColor = '';
		for (var i=0 ; i<allAllergiesJSON.length; i++) {
			if (allAllergiesJSON[i].allergy_type == 'N')
				allergyColor = 'black';
			else
				allergyColor = 'red';
		}
		document.getElementById('allergiesLabel').style.color = allergyColor;
		Insta.Tooltip = {
			ttipObj: null,
		};
		function createAllergyTooltip() {
			var allergyMessage = '' ;
			for (var i=0 ; i<allAllergiesJSON.length; i++ ) {
					allergyMessage = allergyMessage +getString("js.patientdetails.common.tag.allergy")+ allAllergiesJSON[i].allergy + "</br>" +
					getString("js.patientdetails.common.tag.onset")+ allAllergiesJSON[i].onset_date + "</br>" +
					getString("js.patientdetails.common.tag.reaction")+ allAllergiesJSON[i].reaction + "</br>" +
					getString("js.patientdetails.common.tag.status")+ (allAllergiesJSON[i].status == 'A' ? 'Active' : 'Inactive') + "</br>" +
									 "---------------------------------------" + "</br>";
			}
			Insta.Tooltip.ttipObj = new YAHOO.widget.Tooltip("tooltip",
			{ 	context: AllergiesColId,
				text:allergyMessage,
				hidedelay: 0,
				showdelay: 1000,
				autodismissdelay: 10000
			} );
		}
		createAllergyTooltip();
	</script>
</c:if>

	</table>
</fieldset>

<script type="text/javascript">
var departmentId = '';
var patientGender = '${patient.patient_gender}';
</script>
