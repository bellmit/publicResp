<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="masters/ratePlan.js" />
	<script>
		var cpath = '${pageContext.request.contextPath}';
		var rateSheetList = ${rateSheetList};
	</script>

	<title>Add/Edit Rate Plan - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>

<body class="yui-skin-sam" onload="init();">

<h1>Add/Edit Rate Plan</h1>

<div><insta:feedback-panel /></div>

<form name="RatePlan" action="${cpath}/pages/masters/ratePlan.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'addRatePlanDetails' ? 'createRatePlan' : 'updateRatePlanDetails'}">
	<input type="hidden" name="org_id" id="org_id" value="${ratePlan.map.org_id}" />

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Basic Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Rate Plan Name:</td>
						<td><input type="text" name="org_name" id="org_name" value="${ratePlan.map.org_name}"/></td>
					<td class="formlabel">Status:</td>
						<td><insta:selectoptions name="status" id="status"
						opvalues="A,I" optexts="Active,Inactive" value="${ratePlan.map.status}"/></td>
					<td class="formlabel">Address :</td>
					<td class="forminfo">
						<textarea name="org_address" id="org_address">${ratePlan.map.org_address}</textarea>
					</td>

				</tr>
				<tr>
					<td class="formlabel">Contact Person :</td>
					<td class="forminfo">
						<input type="text" name="org_contact_person" id="org_contact_person" value="${ratePlan.map.org_contact_person}"/>
					</td>
					<td class="formlabel">Phone :</td>
					<td class="forminfo">
						<input type="text" name="org_phone" id="org_phone" value="${ratePlan.map.org_phone}" maxlength="15"/>
					</td>
					<td class="formlabel">Email :</td>
					<td class="forminfo">
						<input type="text" name="org_mailid" id="org_mailid" value="${ratePlan.map.org_mailid }"/>
					</td>
				</tr>
				<tr>
						<td class="formlabel">Store Tariff</td>
						<td class="forminfo">
							<insta:selectdb name="store_rate_plan_id" table="store_rate_plans" valuecol="store_rate_plan_id"
								displaycol="store_rate_plan_name" orderby="store_rate_plan_name" value="${ratePlan.map.store_rate_plan_id}"
								dummyvalue="Default" dummyvalueId=""/>
						</td>
						<td class="formlabel">Pharmacy Discount Percentage :</td>
						<td class="forminfo">
							<input type="text" name="pharmacy_discount_percentage" id="pharmacy_discount_percentage" value="${ratePlan.map.pharmacy_discount_percentage }"/>
						</td>
					
						<c:if test="${mod_reward_points}">
							<td class="formlabel">Eligible to Earn Points:</td>
							<td class="forminfo">
								<insta:selectoptions name="eligible_to_earn_points" id="eligible_to_earn_points"
									opvalues="N,Y" optexts="No,Yes" value="${ratePlan.map.eligible_to_earn_points}"/>
							</td>
						</c:if>
				</tr>
				<tr>
					<td class="formlabel">Has Date Validity</td>
					<td>
					<input type="checkbox" name="has_date_validity" id="has_date_validity" ${ratePlan.map.has_date_validity eq true ? 'checked' : ''}
						onclick="enableDateValidity();"/>
					</td>
					<td colspan="3">
						<div id="dateValidDiv" style="visibility: hidden">
							<table>
							<tr>
								<td class="formlabel">Valid From :</td>
								<fmt:parseDate value="${ratePlan.map.valid_from_date}" pattern="yyyy-MM-dd" var="dt"/>
								<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="frm"/>
								<fmt:parseDate value="${ratePlan.map.valid_to_date}" pattern="yyyy-MM-dd" var="dt"/>
								<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
								<td><insta:datewidget name="fromDate"  btnPos="left" value="${frm}"/></td>
								<td class="formlabel">To :</td>
								<td><insta:datewidget name="toDate"  btnPos="left" value="${to}"/></td>
							</tr>
							</table>
						</div>
					</td>
				</tr>
			</table>
		</fieldset>
		<fieldSet class="fieldSetBorder">
			<legend class="fieldSetLabel">Base Rate Sheets:</legend>
				<table class="dashboard" id="baseRateTbl" cellpadding="0" cellspacing="0">
					<tr class="header">
						<td>Rate Sheet</td>
						<td>Discount / Markup</td>
						<td>Variation %</td>
						<td>Round Off Amount</td>
						<td>Priority</td>
						<td>&nbsp;</td>
					</tr>
					<tr id="" style="display: none">
					</tr>
					<tr>
						<td colspan="5"></td>
						<td>
							<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="AddRow(this)" >
								<img src="${cpath}/icons/Add.png" align="right"/>
							</button>
						</td>
					</tr>
				</table>
		</fieldSet>
		<c:if test="${param._method != 'addRatePlanDetails'}">
			<!-- <fieldSet class="fieldSetBorder"><legend class="fieldSetLabel">Exclusions / Overrides</legend>  -->
			<table>
				<tr>
					<td valign="top">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Exclusions</legend>
							<table class="dashboard">
								<c:set var="url" value="${cpath}/pages/masters/ratePlan.do"/>
								<tr class="header">
									<td>Charge Category</td>
									<td>Items Excluded </td>
									<td>&nbsp;</td>
								</tr>
								<tr>
									<td>Anaesthesia Types</td>
									<td>${anesthesiaExcluded}/${anesthesiaTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=anesthesia
										&org_id=${ratePlan.map.org_id}&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Consultation Types</td>
									<td>${consultationExcluded}/${consultationTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=consultation
										&org_id=${ratePlan.map.org_id}&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Diagnostic Tests</td>
									<td>${testExcluded}/${testTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=diagnostics&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Dynamic Packages</td>
									<td>${dynaExcluded}/${dynaTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=dynapackages&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Surgeries/Procedures</td>
									<td>${opeExcluded}/${opeTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=operations&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Packages</td>
									<td>${packExcluded}/${packTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=packages&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Services</td>
									<td>${serviceExcluded}/${serviceTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=services&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
							</table>
					</fieldset>
					</td>
					<td style="width:75px"></td>
					<td valign="top">
					<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Overrides</legend>
						<table class="dashboard">
							<c:set var="url" value="${cpath}/pages/masters/ratePlan.do"/>
								<tr class="header">
									<td>Charge Category</td>
									<td>Items Overridden</td>
									<td>&nbsp;</td>
									<td>&nbsp;</td>
								</tr>
								<tr>
									<td>Anaesthesia Types</td>
									<td>${anesthesiaOverrided}/${anesthesiaTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=anesthesia&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=exportChargesToXls&org_id=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Consultation Types</td>
									<td>${consultationOverrided}/${consultationTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=consultation&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/consultCharges.do?_method=exportConsultationCharges&orgId=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Diagnostic Tests</td>
									<td>${testOverrided}/${testTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=diagnostics&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/pages/masters/hosp/diagnostics/addtest.do?_method=exportTestChargesCSV&orgId=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Surgeries/Procedures</td>
									<td>${opeOverrided}/${opeTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=operations&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/OperationMaster.do?_method=exportChargesToXls&org_id=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Packages</td>
									<td>${packOverrided}/${packCount}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=packages&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&type=P&package_active=A&is_override=Y'/>">Edit Overrides</a></td>
									<td>&nbsp;</td>
								</tr>
								<tr>
									<td>Services</td>
									<td>${serviceOverrided}/${serviceTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=services&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/ServiceMaster.do?_method=exportChargesToXls&org_id=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Doctor</td>
									<td>${doctorOverrided}/${doctorTotal}</td>
									<td><a href="<c:out value='${url}?_method=getDoctorChargesList&org_id=${ratePlan.map.org_id}&org_name=${ifn:cleanURL(ratePlan.map.org_name)}
										&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/DoctorMaster.do?_method=exportChargesToXls&org_id=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Registration Charges</td>
									<td>${regChargesOverided}/1</td>
									<td><a href="<c:out value='${url}?_method=getRegistrationChargesScreen&org_id=${ratePlan.map.org_id}&org_name=${ifn:cleanURL(ratePlan.map.org_name)}
										&status=A&fromItemMaster=false&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/RegistrationCharges.do?method=exportRegChargesToXls&orgId=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Equipment</td>
									<td>${equipOverrided}/${equipTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=equipment&org_id=${ratePlan.map.org_id}&status=A
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/EquipmentMaster.do?_method=exportChargesToXls&org_id=${ratePlan.map.org_id}">Download</a></td>
								</tr>
								<tr>
									<td>Surgery/Procedure Rooms</td>
									<td>${theatreOverrided}/${theatreTotal}</td>
									<td><a href="<c:out value='${url}?_method=getOtChargesListScreen&org_id=${ratePlan.map.org_id}&status=A
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&is_override=Y'/>">Edit Overrides</a></td>
									<td>&nbsp;</td>
								</tr>
								<tr>
									<td>Dynamic Packages</td>
									<td>${dynaOverrided}/${dynaTotal}</td>
									<td><a href="<c:out value='${url}?_method=getChargesListScreen&chargeCategory=dynapackages&org_id=${ratePlan.map.org_id}
										&org_name=${ifn:cleanURL(ratePlan.map.org_name)}&status=A&is_override=Y'/>">Edit Overrides</a></td>
									<td><a href="${cpath}/master/DynaPackage.do?_method=exportChargesToXls&org_id=${ratePlan.map.org_id}">Download Charges</a></td>
								</tr>
								<tr>
									<td>Dyna Packages Category Limits</td>
									<td>&nbsp;</td>
									<td>&nbsp;</td>
									<td><a href="${cpath}/master/DynaPackage.do?_method=exportLimitsToCsv&org_id=${ratePlan.map.org_id}">Download Category Limits</a></td>
								</tr>
								<tr>
									<td>Bed Type</td>
									<td>${bedTypeOverrided}/${bedtypeTotal}</td>
									<td><a href="${cpath}/pages/masters/insta/admin/newbedmaster.do?method=getChargesList&orgId=${ratePlan.map.org_id}&is_override=Y">Edit Overrides</a></td>
									<td><a href="${cpath}/pages/masters/insta/admin/newbedmaster.do?method=exportBedchargesToCsv&orgId=${ratePlan.map.org_id}">Download Bed Charges</a></td>
								</tr>
								<tr>
									<td>ICU Bed Charges</td>
									<td>&nbsp;</td>
									<td>&nbsp;</td>
									<td><a href="${cpath}/pages/masters/insta/admin/newbedmaster.do?method=exportIcuBedChargesToCsv&orgId=${ratePlan.map.org_id}">Download ICU Bed Charges</a></td>
								</tr>
						</table>
					</fieldset>
					</td>
				</tr>
			</table>
			<!-- </fieldSet> -->
			</c:if>

		<div class="screenActions">
			<input type="button" name="Save" value="Save" onclick="return validate();"/>
			| <a href="${cpath}/pages/masters/ratePlan.do?_method=getRatePlanDetails&sortOrder=org_name&sortReverse=false
			&status=A">Rate Plan List</a>
		</div>

</form>
	<script>
			var baseRateSheetsDetails  = ${baseRateSheetsDetails};
	</script>
</body>
</html>