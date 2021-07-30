<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>'/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html:html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="i18nSupport" content="true"/>
<title><insta:ltext key="laboratory.assignouthouse.outhouselist.title"/></title>
 <script type="text/javascript">
function disabledSubmit(){
	if ( !validate() ){
		alert("Assign at least one test");
		return false;
	}else {
 		document.getElementById('save').disabled = true;
 	}
}

function validate() {
	var outHouse = document.getElementsByName("houtHouseId");
	var valid = (outHouse.length == 0) ;

	for ( var i=0;i<outHouse.length;i++ ) {
		valid  = outHouse[i].value != '' ;
		if ( valid )
			break;
	}

	return valid;
}
</script>
</head>
<body>
<h1><insta:ltext key="laboratory.assignouthouse.outhouselist.assignouthouse"/></h1>
<insta:feedback-panel/>
<c:choose>
	<c:when test="${ not empty patientvisitdetails }">
		<insta:patientdetails  visitid="${patientvisitdetails.map.patient_id}"/>
		<c:set var="visit_id"  value="${patientvisitdetails.map.patient_id}" />
	</c:when>
	<c:otherwise>
		<c:set var="visit_id"  value="${customer.map.incoming_visit_id}" />
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="laboratory.assignouthouse.outhouselist.patientdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
				<tr>
					<td class="formlabel"><insta:ltext key="ui.label.patient.name"/>:</td>
					<td class="forminfo">${custmer.map.patient_name}</td>
					<td class="formlabel"><insta:ltext key="laboratory.assignouthouse.outhouselist.fromlab"/>:</td>
					<td class="forminfo">${custmer.map.hospital_name}</td>
					<td class="formlabel">&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="laboratory.assignouthouse.outhouselist.patientvisit"/>:</td>
					<td class="forminfo">${custmer.map.incoming_visit_id}
						<c:set var="visit_id"  value="${custmer.map.incoming_visit_id}" />
					</td>
					<td class="formlabel"><insta:ltext key="laboratory.assignouthouse.outhouselist.age.gender"/>:</td>
					<td class="forminfo">${custmer.map.age_text}${fn:toLowerCase(custmer.map.age_unit)} / ${custmer.map.gender}</td>
				</tr>
			</table>
		</fieldset>
	</c:otherwise>
</c:choose>

<html:form method="POST" onsubmit="return disabledSubmit();">
	<input type="hidden" name="mrno" value="${patientvisitdetails.map.mr_no}" />
	<input type="hidden" name="_method" value="SaveOuthouseDetails" />
	<input type="hidden" name="visitid" value="${ifn:cleanHtmlAttribute(visit_id)}" />
	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(param.category)}">

	<table class="dashboard" width="40%" cellspacing="0" cellpadding="0">
		<tr>
			<th><insta:ltext key="laboratory.assignouthouse.outhouselist.testname"/></th>
			<th><insta:ltext key="laboratory.assignouthouse.outhouselist.outhousename"/></th>
		</tr>
		<c:set var="id" value="1" />
		<c:set var="prevPresId" value="" />

		<c:forEach var="test" items="${outHousetests}">
			<c:if test="${(prevPresId != test.PRESCRIBED_ID)}">
				<c:set var="prevPresId" value="${test.PRESCRIBED_ID}" />
				<c:set var="emptycheck" value="0" />
				<tr>
					<td>${test.TEST_NAME}<input type="hidden" name="htestId"
						id="htestId${id}" value="${test.TEST_ID}" /> <input
						type="hidden" name="hprescribedId" id="hprescribedId${id}"
						value="${test.PRESCRIBED_ID}" /></td>
					<td><select name="houtHouseId" id="houtHouseId${id}" class="dropdown">
						<option value="">---Select---</option>
						<c:set var="no_outhouse" value="true"/>
						<c:forEach var="outhouse" items="${outHousetests}">
							<c:if test="${(prevPresId eq outhouse.PRESCRIBED_ID)}">
								<c:choose>
									<c:when test="${outhouse.OUTSOURCE_DEST_ID eq ''}">
										<c:set var="no_outhouse" value="true"/>
									</c:when>
									<c:otherwise>
										<!-- 1) if center conecpt is disabled show all outhouses which are associated with tests only.
											 2) if center concept is enabled show all outhouses which are associated with
											 	tests as well as with patient center. -->
										<c:if test="${max_centers_inc_default == 1 || not empty outhouse.OUTSOURCE_CENTER}">
											<c:set var="no_outhouse" value="false"/>
											<option value="${outhouse.OUTSOURCE_DEST}">${outhouse.OUTSOURCE_NAME}</option>
										</c:if>
									</c:otherwise>
								</c:choose>
							</c:if>
						</c:forEach>
						<c:if test="${no_outhouse}">
							<option value="empty"><insta:ltext key="laboratory.assignouthouse.outhouselist.noouthouse"/></option>
						</c:if>
					</select></td>
				</tr>
				<c:set var="id" value="${id+1}" />
			</c:if>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${not empty outHousetests}" message='<insta:ltext key="laboratory.assignouthouse.outhouselist.notestsfound"/>'/>
	<div class="screenActions">
		<input type="submit" name="save" id="save" value="Save" ${ empty outHousetests ? 'disabled' : ''}>
		<c:choose>
			<c:when test="${category == 'DEP_LAB'}">
				<c:set var="url" value="${('Laboratory')}"/>
				<c:set var="reportListLink" value="Laboratory Reports List"/>
			</c:when>
			<c:otherwise>
				<c:set var="url" value="${('Radiology')}"/>
				<c:set var="reportListLink" value="Radiology Reports List"/>
			</c:otherwise>
		</c:choose>
		| <a href='<c:out value ="${cpath}/${ifn:cleanURL(url)}/schedules.do?_method=getScheduleList&category=${ifn:cleanURL(category)}&mr_no=${patientvisitdetails.map.mr_no}&patient_id=${not empty patientvisitdetails ? '' : visit_id}"/>'>${reportListLink}</a>
	</div>
</html:form>
</body>
</html:html>
