<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<title><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="js" file="outpatient/changeconsultingdoctor.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<style>
		.scrolForContainer .yui-ac-content{
			max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
	</style>
	<script>
		var doctorlist = ${AllDoctorsList};
	</script>
	<insta:js-bundle prefix="outpatient.consultation.mgmt"/>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();">
	<h1><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.changeconsultingdoctor"/></h1>
	<insta:feedback-panel/>
	<insta:patientdetails visitid="${consultation_bean.map.patient_id}" showClinicalInfo="true"/>
	<form name="changedoctorform" action="ChangeConsultingDoctor.do" method="POST">
		<input type="hidden" name="_method" value="changeConsultingDoctor"/>
		<input type="hidden" name="consultation_id" value="${ifn:cleanHtmlAttribute(param.consultation_id)}"/>
		<input type="hidden" name="doctor_charge_type" value="${billBean.map.doctor_charge_type}"/>
		<input type="hidden" name="modified_doctor_id" id="modified_doctor_id" value=""/>
		<input type="hidden" name="modified_doctor_dept_id" id="modified_doctor_dept_id" value=""/>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.consultingdoctor"/>: </td>
					<td class="forminfo">${consultation_bean.map.doctor_full_name}</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
				</tr>
				
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.consultdepartment"/>: </td>
					<td class="forminfo">${consultation_bean.map.dept_name}</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
				</tr>
				<c:if test="${not empty package_bean.map.package_name}"> 
					<tr>
						<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.consultpackagename"/>: </td>
						<td class="forminfo">${package_bean.map.package_name}</td>
						<td class="formlabel"></td>
						<td>&nbsp;</td>
						<td class="formlabel"></td>
						<td>&nbsp;</td>
					</tr>
				</c:if>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.changedoctorto"/>: </td>
					<td class="forminfo">
						<div id="doctorAC" class="autocomplete" style="width: 15em;">
							<input id="modifiedDoctor" name="modifiedDoctor" type="text" style="width: 15em;" />
							<div id="doctorContainer" class="scrolForContainer" style="width: 300px;"></div>
						</div>
					</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.updateadmittingdoctor"/>: </td>
					<td >
						<input type="checkbox" name="update_admitting_doctor" id="update_admitting_doctor" value="Y" checked>
					</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.billstatus"/>: </td>
					<td class="forminfo">
						<c:choose>
							<c:when test="${billBean.map.status == 'A'}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.open"/></c:when>
							<c:when test="${billBean.map.status == 'F'}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.finalized"/></c:when>
							<c:when test="${billBean.map.status == 'C'}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.closed"/></c:when>
							<c:when test="${billBean.map.status == 'X'}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.cancelled"/></c:when>
						</c:choose>
					</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.paymentstatus"/>: </td>
					<td class="forminfo">
						<c:choose>
							<c:when test="${billBean.map.payment_status == 'U'}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.unpaid"/></c:when>
							<c:when test="${billBean.map.payment_status == 'P'}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.paid"/></c:when>
						</c:choose>
					</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
					<td class="formlabel"></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</fieldset>
		<table class="screenActions">
			<tr>
				<td><input type="button" name="save" value="Save" onclick="return changeDoctor();"
						${billBean.map.consultation_status == 'C'
							|| billBean.map.consultation_status == 'P'
							|| consultation_bean.map.initial_assessment_status == 'P' ? 'disabled' : ''}/>
					<c:url var="dashboardUrl" value="/outpatient/OpListAction.do">
						<c:param name="_method" value="list"/>
						<c:param name="status" value="A"/>
						<c:param name="status" value="P"/>
						<c:param name="visit_status" value="A"/>
						<c:param name="sortReverse" value="true"/>
					</c:url>
					| <a href="${dashboardUrl}"><insta:ltext key="patient.outpatientlist.changeconsultingdoctor.details.patientlist"/></a></td>
			</tr>
		</table>
	</form>
</body>
</html>