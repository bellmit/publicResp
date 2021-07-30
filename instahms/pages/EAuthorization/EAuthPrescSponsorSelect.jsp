<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
	<head>
		<title>Prior Auth Sponsor List - Insta HMS</title>
		<script type="text/javascript">
			function init() {
				if (!empty(document.getElementById('sponsor0')))
					document.getElementById('sponsor0').click();
			}

			function handleSponsorSelect(obj, index) {
				var selectedValue = obj.value;
				document.getElementById('priority').value = selectedValue;
				document.getElementById('insurance_co_id').value = document.getElementById('insurance_co'+index).value;
			}
			function doSave() {
				document.mainform.submit();
			}
		</script>
	</head>

<body onload="init()">

<h1 style="float: left">Add New Prior Auth Prescription</h1>
<c:url var="searchUrl" value="/EAuthorization/EAuthPresc.do" />
<insta:patientsearch searchType="visit" searchUrl="${searchUrl}"
buttonLabel="Find" searchMethod="getSponsorList" fieldName="patient_id"/>

<div><insta:feedback-panel/></div>
<insta:patientdetails visitid="${not empty preauthPrescBean ? preauthPrescBean.map.patient_id : param.patient_id}" />
<c:if test="${not empty patientInsurancePlanList}">
	<form name="mainform" action="./EAuthPresc.do" autocomplete="off">
	<input type="hidden" name="_method" value="getEAuthPrescriptionScreen">
	<input type="hidden" name="patient_id" value="${ifn:cleanHtmlAttribute(patient_id)}">
	<input type="hidden" name="priority" id="priority" value="">
	<input type="hidden" name="insurance_co_id" id="insurance_co_id" value="">
	<fieldset class="fieldSetBorder">
		<table id="sponsorDetails" class="formtable" width="100%">
			<c:forEach var="sponsorDetails" items="${patientInsurancePlanList}" varStatus="st">
				<tr>
					<td>
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">
								<input type="radio" name="sponsor" id="sponsor${st.index}" value="${sponsorDetails.map.priority}" onclick="handleSponsorSelect(this, ${st.index});">
									${sponsorDetails.map.priority == 1 ? 'Primary' : 'Secondary'} Sponsor Detail
							</legend>
							<table class="formtable" width="100%">
								<tr>
									<td class="formlabel">Rate Plan:</td>
									<td class="forminfo"><div title="${sponsorDetails.map.org_name}">${sponsorDetails.map.org_name}</div></td>
									<td class="formlabel"> ${sponsorDetails.map.priority == 1 ? 'Primary' : 'Secondary'} TPA/Sponsor:</td>
									<td class="forminfo"><div title="${sponsorDetails.map.tpa_name}">${sponsorDetails.map.tpa_name}</div></td>
									<td class="formlabel"> ${sponsorDetails.map.priority == 1 ? 'Primary' : 'Secondary'} Insurance Co.:</td>
									<td class="forminfo">
										<div title="${sponsorDetails.map.insurance_co_name}">${sponsorDetails.map.insurance_co_name}</div>
										<input type="hidden" id="insurance_co${st.index}" value="${sponsorDetails.map.insurance_co}">
									</td>
								</tr>
								<tr>
									<td class="formlabel">Member ID:</td>
									<td class="forminfo"><div title="${sponsorDetails.map.member_id}">${sponsorDetails.map.member_id}</div></td>
									<td class="formlabel">Net./Plan Type:</td>
									<td class="forminfo"><div title="${sponsorDetails.map.category_name}">${sponsorDetails.map.category_name}</div></td>
									<td class="formlabel"> Plan Name:</td>
									<td class="forminfo"><div title="${sponsorDetails.map.plan_name}">${sponsorDetails.map.plan_name}</div></td>
								</tr>
							</table>
						</fieldset>
					</td>
				</tr>
			</c:forEach>
		</table>
	</fieldset>
	<div class="screenActions" style="float: left">
		<button type="button" name="actionBtn" onclick="return doSave();">Add Prior Auth Presc.</button>
	</div>
	</form>
</c:if>
</body>
</html>