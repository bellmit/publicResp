<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html:html>
<head>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/addOrganization.js" />

<script>
	var cpath = '${pageContext.request.contextPath}';
</script>

<style type="text/css">
  #myAutoComplete{
	 width:15em; /* set width here or else widget will expand to fit its container */
     padding-bottom:2em;
  }
</style>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add/Edit Rate Sheet - Insta HMS</title>
</head>

<body class="formtable" onload="document.forms[0].orgName.focus();enableDateValidity();" class="yui-skin-sam">
<h1>Add/Edit Rate Sheet</h1>
<insta:feedback-panel/>
<html:form action="/pages/masters/hosp/admin/Organ.do" onsubmit="return validate()">
	<input type="hidden" name="_method" value="${requestScope._method}">
	<html:hidden property="editOrgId"/>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Rate Sheet Details</legend>
			<table class="formtable">
			<tr>
				<td class="formlabel">Rate Sheet Name :</td>
				<td class="forminfo">
					<html:text property="orgName" maxlength="100" styleId="orgName"  onblur="capWords(orgName)"/>
				</td>
				<td class="formlabel">Status :</td>
				<td class="forminfo">
					<html:select property="status" styleId="status" styleClass="dropdown">
						<html:option value="A">Active</html:option>
						<html:option value="I">Inactive</html:option>
					</html:select>
				</td>
				<td class="formlabel">Address :</td>
				<td rowspan="4" valign="top">
					<html:textarea property="address" styleId="address" ></html:textarea>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Contact Person :</td>
				<td class="forminfo">
					<html:text property="contactPerson" styleId="contactPerson" onblur="capWords(contactPerson)" maxlength="70"/>
				</td>
				<td class="formlabel">Phone :</td>
				<td class="forminfo">
					<html:text property="phone" maxlength="15"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel">Email :</td>
				<td class="forminfo">
					<html:text property="email" styleId="email" onblur="capWords(email)" maxlength="70"/>
				</td>
				<c:if test="${mod_reward_points}">
				<td class="formlabel">Eligible to Earn Points:</td>
				<td class="forminfo">
					<html:select property="eligible_to_earn_points" styleId="eligible_to_earn_points" styleClass="dropdown">
						<html:option value="N">No</html:option>
						<html:option value="Y">Yes</html:option>
					</html:select>
				</td>
				</c:if>
			</tr>
			<tr>
			<c:if test="${param._method == 'getNewOrganiaztionScreen'}">
				<td class="formlabel">Copy Rates From :</td>
				<td class="forminfo">
					<insta:selectdb name="baseOrgId" id="baseOrgId" table="organization_details" valuecol="org_id"
						value="${OMasterForm.baseOrgId}" displaycol="org_name" orderby="org_name"
						filtercol="is_rate_sheet" filtervalue="Y" dummyvalue="--Select--" dummyvalueId=""/>
				</td>
			</c:if>
				<td style="text-align: right">Has Date Validity
					<html:checkbox property="hasDateValidity" onchange="enableDateValidity();"></html:checkbox>
				</td>
				<td colspan="3">
					<div id="dateValidDiv" style="visibility: hidden">
						<table>
						<tr>
							<td class="formlabel">Valid From :</td>
							<fmt:parseDate value="${OMasterForm.fromDate}" pattern="yyyy-MM-dd" var="dt"/>
							<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="frm"/>
							<fmt:parseDate value="${OMasterForm.toDate}" pattern="yyyy-MM-dd" var="dt"/>
							<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="to"/>
							<td><insta:datewidget name="fromDate"  btnPos="left" value="${frm}"/></td>
							<td class="formlabel">To :</td>
							<td><insta:datewidget name="toDate"  btnPos="left" value="${to}"/></td>
						</tr>
						</table>
					</div>
				</td>
			</tr>
			<tr></tr>
			</table>
		</fieldset>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Pharmacy Sales:</legend>
			<table class="formtable">
			<tr>
				<td class="formlabel">Store Tariff</td>
				<td class="forminfo">
					<insta:selectdb name="store_rate_plan_id" table="store_rate_plans" valuecol="store_rate_plan_id"
						displaycol="store_rate_plan_name" orderby="store_rate_plan_name" value="${orgDetails.map.store_rate_plan_id}"
						dummyvalue="Default" dummyvalueId=""/>
				</td>
				<td class="formlabel">Discount Percentage:</td>
				<td class="forminfo">
					<html:text property="discperc" onblur="validateDiscount();" />
				</td>

				<input type="hidden" name="vatApplicable" value="${vatApplicable}" />
				<input type="hidden" name="cessApplicable" value="${cessApplicable}" />
				<c:if test="${not empty vatApplicable}">
					<c:set var="style" value="${vatApplicable eq 'Y' and cessApplicable eq 'Y'}" />
				</c:if>
				<c:if test="${not empty param.vatApplicable}">
					<c:set var="style" value="${param.vatApplicable eq 'Y' and param.cessApplicable eq 'Y'}" />
				</c:if>

				<c:choose>
					<c:when test="${style}">
						<td class="formlabel" style="display: ">Discount Type:</td>
						<td class="forminfo" style="display: ${style ? '' : 'none'}">
							<html:select property="discType" styleClass="dropdown">
								<html:option value="E">Excluding Tax</html:option>
								<html:option value="I">Including Tax</html:option>
							</html:select>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel">&nbsp;</td>
						<td class="forminfo">&nbsp;</td>
					</c:otherwise>
				</c:choose>
			</tr>
			</table>
		</fieldset>
		<c:if test="${param._method != 'getNewOrganiaztionScreen'}">
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
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=anesthesia&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Consultation Types</td>
									<td>${consultationExcluded}/${consultationTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=consultation&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Diagnostic Tests</td>
									<td>${testExcluded}/${testTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=diagnostics&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Dynamic Packages</td>
									<td>${dynaExcluded}/${dynaTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=dynapackages&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Surgeries/Procedures</td>
									<td>${opeExcluded}/${opeTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=operations&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Packages</td>
									<td>${packExcluded}/${packTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=packages&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
								<tr>
									<td>Services</td>
									<td>${serviceExcluded}/${serviceTotal}</td>
									<td><a href="<c:out value='${url}?_method=getExcludeChargesScreen&chargeCategory=services&org_id=${orgDetails.map.org_id}
										&org_name=${ifn:cleanURL(orgDetails.map.org_name)}&status=A&applicable=false&applicable@cast=y'/>">Edit Exclusions</a></td>
								</tr>
							</table>
					</fieldset>
					</td>
					<td style="width:75px"></td>
					<td valign="top">
					<fieldSet class="fieldSetBorder"><legend class="fieldSetLabel">Edit Master Charges</legend>
						<table class="dashboard">
							<tr class="header">
								<td>Masters</td>
								<td>&nbsp;</td>
							</tr>
							<tr>
								<td>Anaesthesia Types</td>
								<td><a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=list&status=A&sortReverse=false&sortOrder=anesthesia_type_name&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Consultation Types</td>
								<td><a href="${cpath}/master/consultCharges.do?_method=list&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Doctors</td>
								<td><a href="${cpath}/master/DoctorMaster.do?_method=list&status=A&sortOrder=doctor_name&sortReverse=false&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Diagnostic Tests</td>
								<td><a href="${cpath}/pages/masters/hosp/diagnostics/addtest.do?_method=listTests&status=A&sortOrder=test_name&sortReverse=false&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Dynamic Packages</td>
								<td><a href="${cpath}/master/DynaPackage.do?_method=list&sortReverse=false&status=A&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Dietary Master</td>
								<td><a href="${cpath}/dietary/DietaryMaster.do?_method=list&searchStatus=A&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Equipment</td>
								<td><a href="${cpath}/master/equipment.htm?status=A&sortReverse=false&sortOrder=equipment_name&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Surgeries/Procedures</td>
								<td><a href="${cpath}/master/OperationMaster.do?_method=list&status=A&sortOrder=operation_name&sortReverse=false&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Surgery/Procedure Rooms</td>
								<td><a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=getTheatMast&status=A&sortReverse=false&orgId=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Packages</td>
								<td><a href="${cpath}/master/packages/index.htm#/packages?_method=getPackageListScreen&package_active=A&sortReverse=false&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Registration Charges</td>
								<td><a href="${cpath}/master/RegistrationCharges.do?method=show&orgId=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
							<tr>
								<td>Services</td>
								<td><a href="${cpath}/master/ServiceMaster.do?_method=list&status=A&sortOrder=service_name&sortReverse=false&org_id=${orgDetails.map.org_id}">Edit Charges</a></td>
							</tr>
						</table>
					</fieldset>
					</td>
				</tr>
			</table>
			</c:if>

		<div style="clear: both"></div>
		<table class="screenActions">
			<tr >
				<td><button type="submit" accesskey="S" property="Save" onclick="return validateDates();"><b><u>S</u></b>ave</button></td>
				<c:if test="${param._method=='editOrganiaztionDetails'}">
					<td>&nbsp;|&nbsp;</td>
					<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/pages/masters/hosp/admin/Organ.do?_method=getNewOrganiaztionScreen'">Add</a></td>
				</c:if>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0);" onclick="doClose();">Rate Sheets List</a></td>
			</tr>
		</table>

</html:form>

</body>

</html:html>
