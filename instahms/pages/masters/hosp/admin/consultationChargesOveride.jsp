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
	<title>Rate Plan Overrides - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js"/>
	<c:set value="${pageContext.request.contextPath}" var="cpath" />
	<script>
		var cpath = '${cpath}';
	</script>
</head>

<body>
	<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>

	<form name="consultationform" action="${cpath}/pages/masters/ratePlan.do">
		<input type="hidden" name="_method" value="overRideConsultationCharges" />
		<input type="hidden" name="consultation_type_id" value="${ifn:cleanHtmlAttribute(param.consultation_type_id)}"/>
		<input type="hidden" name="chargeCategory" value="consultation"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

		<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
		<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Consultation Charge Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Consultation Type:</td>
					<td><b>${bean.map.consultation_type}</b></td>

				</tr>
				<tr>
					<td class="formlabel">Item Code:</td>
					<td><input type="text" name="item_code" value="${bean.map.item_code}"/></td>
					<td class="formlabel">Treatment Code Type:</td>
					<td>
						<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
							displaycol="code_type"	filtervalue="Consultations" filtercol="code_category" dummyvalue="--Select--"
							value="${bean.map.code_type}"/>
					 </td>
				</tr>
			</table>
		</fieldset>

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
							value="${ifn:afmt(charges[bed].charge)}" onblur="validateDiscount('charge','discount','${i}')"/>
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
							value="${ifn:afmt(charges[bed].discount)}" onblur="validateDiscount('charge','discount','${i}');"/>
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

		<div class="screenActions" align="left">
			<button type="submit" name="Save" accesskey="S" ><b><u>S</u></b>ave</button>
			<c:choose>
				<c:when test="${fromItemMaster eq 'false'}">
					| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=consultation&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Consultation Charges List</a>
				</c:when>
				<c:otherwise>
					<c:set var="url" value="${cpath}/master/consultCharges.do?_method=edit"/>
					| <a href="<c:out value='${url}&consultation_type_id=${ifn:cleanURL(param.consultation_type_id)}&org_id=${ifn:cleanURL(baseRateSheet)}'/>">Consultation Charges</a>
				</c:otherwise>
			</c:choose>
		</div>
	</form>
</body>

</html>
