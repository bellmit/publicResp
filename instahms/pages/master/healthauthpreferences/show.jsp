<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<html>
	<head>
		<title>Health Authority Preferences - Insta Hms</title>
		<insta:link type="js" file="hmsvalidation.js" />
		<c:set var="cpath" value="${pageContext.request.contextPath}" />
		<c:set var="pagePath" value="<%=URLRoute.HEALTH_AUTH_PREFERENCES_PATH %>"/>
	</head>
	<body>
	<form action="update.htm" method="POST" >

	<div class="pageHeader">Health Authority Preferences</div>
	<div>
		<fieldset class="fieldSetBorder">
			<table width="100%" class="formtable">
				<tr>
					<td class="formlabel">Health Authority:</td>
					<td >
						<select name="healthAuthority" id="healthAuthority" disabled="true" class="dropdown">
							<option value="Default" ${bean.health_authority =='Default' ? 'selected':''}>Default</option>
							<option value="HAAD" ${bean.health_authority =='HAAD' ? 'selected':''}>HAAD</option>
							<option value="DHA" ${bean.health_authority =='DHA' ? 'selected':''}>DHA</option>
						</select>
						<input type = "hidden" name="health_authority" value ="${bean.health_authority}" />
						<img class="imgHelpText" title="Allowed Health Authority for Eclaim -- HAAD/DHA" src="${cpath}/images/help.png"/>
					</td>
					<td class="formlabel">Default Diagnosis Code type:</td>
					<td>					
						<select name="diagnosis_code_type" id="diagnosis_code_type" class="dropdown">
							<option value="">..Code Type..</option>
							<c:forEach items="${defaultDiagnosisCodeType}" var="diagCodeType" >
								<option value="${diagCodeType.code_type}" ${bean.diagnosis_code_type == diagCodeType.code_type ? 'selected':''}>${diagCodeType.code_type}</option>
							</c:forEach>
						</select>						
						<img class="imgHelpText" title="For Accumed Claim -- Diagnosis code types are ICD9 or ICD10.
							For HAAD Claim -- Diagnosis code types is ICD. " src="${cpath}/images/help.png"/>
					</td>
				</tr>
				<tr>
				    <td class="formlabel">Prescription by Generics: </td>
                    <td>
                        <select name="prescriptions_by_generics" id="prescriptions_by_generics" class="dropdown">
                        <option value="Y" ${bean.prescriptions_by_generics =='Y' ? 'selected':''}>Yes</option>
                        <option value="N" ${bean.prescriptions_by_generics =='N' ? 'selected':''}>No</option>
                        </select>
                    </td>
					<td class="formlabel">Drug Code Type:</td>					
					<td>
						<select name="drug_code_type" id="drug_code_type" class="listbox" multiple>
							<c:forEach items="${drugCodeType}" var="drugCode" >
							<c:set var="attr" value=""/>
								<c:forTokens items="${bean.drug_code_type}" delims="," var="selectedData">								
									<c:if test="${selectedData==drugCode.code_type}">
										<c:set var="attr" value="selected='true'"/>								
									</c:if>				
								</c:forTokens>
								<option value="${drugCode.code_type}" ${attr}>${drugCode.code_type}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
				    <td class="formlabel">Base Rate Plan: </td>
                    <td>
	                    <select name="base_rate_plan" id="base_rate_plan" class="dropdown">
							<option value="">-- Select --</option>
								<c:forEach var="ratePlan" items="${baseRatePlanList}">
									<option value="${ratePlan.org_id}" ${bean.base_rate_plan eq ratePlan.org_id ? 'selected' :''}>${ratePlan.org_name}</option>
								</c:forEach>
						</select>
						<img class="imgHelpText" title="The base rate plan configured will be used to calculate the outlier payment for DRG claims." src="${cpath}/images/help.png"/>
                    </td>
				</tr>
				<c:choose>
					<c:when test="${bean.health_authority == 'HAAD'}">
						<tr>
							<td class="formlabel">Prescribing Doctor As Ordering Clinician : </td>
							<td>
								<select name="presc_doctor_as_ordering_clinician" id="presc_doctor_as_ordering_clinician" class="dropdown">
									<option value="Y" ${bean.presc_doctor_as_ordering_clinician =='Y' ? 'selected':''}>Yes</option>
									<option value="N" ${bean.presc_doctor_as_ordering_clinician =='N' ? 'selected':''}>No</option>
								</select>
							</td>
						</tr>
					</c:when>
					<c:otherwise>
						<input type="hidden" name="presc_doctor_as_ordering_clinician" id="presc_doctor_as_ordering_clinician" value="N"/>
					</c:otherwise>
				</c:choose>
					<tr>
						<td class="formlabel">Consultation Code type:</td>
						<td><select name="consultation_code_types"
							id="consultation_code_types" class="listbox" multiple>
								<c:forEach items="${consultationCodeType}" var="consultCodeType">
									<c:set var="attr" value="" />
									<c:forTokens items="${bean.consultation_code_types}" delims=","
										var="selectedData">
										<c:if test="${selectedData==consultCodeType.code_type}">
											<c:set var="attr" value="selected='true'" />
										</c:if>
									</c:forTokens>
									<option value="${consultCodeType.code_type}" ${attr}>${consultCodeType.code_type}</option>
								</c:forEach>
						</select></td>

						
					</tr>
				</table>
		</fieldset>
	</div>
	<div class="clrboth"></div>
	<div class="fltL MrgTop10" >
		<button type="submit" accesskey="S" ><b><u>S</u></b>ave</button>
		<a href="${cpath}/${pagePath}/list.htm?sortOrder=health_authority">Health Authority List</a>
		
	</div>
	</form>
</body>
</html>