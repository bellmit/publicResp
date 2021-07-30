<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Consultation Charges - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js"/>
	<insta:link type="js" file="masters/editCharges.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script>
		function chgRatePlan(){
			var orgId = document.ConsultationChargesForm.org_id.value;
			document.showForm.org_id.value = orgId;
			document.showForm.submit();
		}
	</script>
</head>

<body onload="fillRatePlanDetails('consultation','consultation_type_id',${consultationTypeBean.map.consultation_type_id});">
	<h1>Consultation Charges</h1>
	<insta:feedback-panel />
	<form name="ConsultationChargesForm">
		<input type="hidden" name="_method" value="update">
		<input type="hidden" name="consultation_type_id" value="${consultationTypeBean.map.consultation_type_id}">
		<input type="hidden" id="orgApplicable" value="${consultOrgBean.map.applicable }">

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Consultation Charge Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Consultation Type:</td>
					<td><b>${consultationTypeBean.map.consultation_type}</b></td>
					<td class="formlabel">Rates For Rate Sheet :</td>
					<td>
						<insta:selectdb name="org_id" value="${consultOrgBean.map.org_id}"
							table="organization_details" valuecol="org_id"
							displaycol="org_name"  onchange="return chgRatePlan();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"
							orderby="org_name"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Treatment Code Type:</td>
					<td>
						<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
							displaycol="code_type"	filtervalue="Consultations" filtercol="code_category" dummyvalue="--Select--"
							value="${consultOrgBean.map.code_type}"/>
					 </td>
					 <td class="formlabel">Item Code:</td>
					 <td><input type="text" name="item_code" value="${consultOrgBean.map.item_code}"/></td>
				</tr>
			</table>
		</fieldset>

		<fieldset class="fieldsetBorder">
			<div class="resultList">
			<table class="dataTable" id="consultationCharges" align="center">
				<tr>
					<th>Bed Types</th>
					<c:forEach var="bed" items="${bedTypes}">
						<th style="width: 2em; overflow: hidden">${bed}</th>
						<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
					</c:forEach>
				</tr>
				<tr>
					<td style="text-align: right">Charge:</td>
					<c:forEach var="bed" items="${bedTypes}" varStatus="k">
						<c:set var="i" value="${k.index}"/>
						<td>
							<input type="text" name="charge" id="charge${i}" class="number validate-decimal"
								value="${ifn:afmt(requestScope.charges[bed].charge)}" onblur="validateDiscount('charge','discount','${i}')"/>
						</td>
					</c:forEach>
					<input type="hidden" name="ids" value="${i+1}">
				</tr>
				<tr>
					<td style="text-align: right">Discount:</td>
					<c:forEach var="bed" items="${bedTypes}" varStatus="k">
						<c:set var="i" value="${k.index}"/>
						<td>
							<input type="text" name="discount" id="discount${i}" class="number validate-decimal"
								value="${ifn:afmt(requestScope.charges[bed].discount)}" onblur="validateDiscount('charge','discount','${i}');"/>
						</td>
					</c:forEach>
				</tr>
				<c:if test="${not empty bedTypes}">
					<tr>
						<td>Apply Charges To All</td>
						<td><input type="checkbox" name="checkbox" onclick="fillValues('consultationCharges', this);" /></td>
						<c:forEach begin="2" end="${fn:length (bedTypes)}">
							<td>&nbsp;</td>
						</c:forEach>
					</tr>
				</c:if>
			</table>
			</div>
		</fieldset>

		<div id="ratePlanDiv" style="display:none">
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
				<table class="dashBoard" id="ratePlanTbl">
					<tr class="header">
						<td>Include</td>
						<td>Rate Plan</td>
						<td>Discount / Markup</td>
						<td>Variation %</td>
						<td>&nbsp;</td>
					</tr>
					<tr id="" style="display: none">
				</table>
				<table class="screenActions" width="100%">
					<tr>
						<td align="right">
							<img src='${cpath}/images/blue_flag.gif'>Overridden
						</td>
					</tr>
				</table>
			</fieldset>
		</div>

		<c:url var="url" value="consultCharges.do">
			<c:param name="_method" value="list"/>
		</c:url>
		<c:url var="editDetailsURL" value="consultCharges.do">
			<c:param name="_method" value="editTypes"/>
			<c:param name="consultation_type_id" value="${consultationTypeBean.map.consultation_type_id}"></c:param>
			<c:param name="org_id" value="ORG0001"></c:param>
		</c:url>
		<table class="screenActions">
		<tr>
			<td>
				<button type="submit" name="Save" accesskey="S" ><b><u>S</u></b>ave</button> |
				<a href="${editDetailsURL }">Edit Consultation Details</a>|
				<a href="<c:out value='${url}'/>">Consultation Charges List</a>
			</td>
		</tr>
	</table>


	</form>
	<form action="${cpath}/master/consultCharges.do" name="showForm" method="GET">
		<input type="hidden" name="_method" value="edit" />
		<input type="hidden" name="consultation_type_id" value="${consultationTypeBean.map.consultation_type_id}"/>
		<input type="hidden" name="org_id" id="org_id" value="${consultOrgBean.map.org_id}"/>
	</form>
	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
	</script>
</body>

</html>
