<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Rate Plan Overrides - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/addTheatre.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/orderCodes.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<script>
	var cpath = '${cpath}';
	function onSave(){
		if(!validateAllDiscounts()) return false;
		document.otform.submit();
	}
	function validateAllDiscounts() {
		var len = document.otform.ids.value;
		var valid = true;
		for(var i=0;i<len;i++) {
			valid = valid && validateDiscount('daily_charge','daily_charge_discount',i);
			valid = valid && validateDiscount('min_charge','min_charge_discount',i);
			valid = valid && validateDiscount('incr_charge','incr_charge_discount',i);
			valid = valid && validateDiscount('slab_1_charge' ,'slab_1_charge_discount', i);
		}
		if(!valid) return false;
		else return true;
	}


</script>
</head>
<body>
<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>

<form name="otform" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="overRideOtCharges" />
	<input type="hidden" name="theatre_id" value="${ifn:cleanHtmlAttribute(param.theatre_id)}"/>
	<input type="hidden" name="chargeCategory" value="operationTheatre"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

	<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
	<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">OT Charge Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Theatre Name:</td>
			<td class="forminfo">${bean.map.theatre_name}</td>
		</tr>
		<tr>
			<tr>
				<td class="formlabel">Unit Size (minutes):</td>
				<td class="forminfo">${bean.map.duration_unit_minutes}</td>
				<td class="formlabel">Min Duration (units):</td>
				<td class="forminfo">${bean.map.min_duration}</td>
			</tr>
			<tr>
				<td class="formlabel">Slab 1 Threshold (units):</td>
				<td class="forminfo">${bean.map.slab_1_threshold}</td>
				<td class="formlabel">Incr Duration (units):</td>
				<td class="forminfo">${bean.map.incr_duration}</td>
			</tr>
		</tr>
	</table>
<div class="resultList">
	<table class="dataTable" id="otCharges">
		<tr>
				<th>Bed Types</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden">${bed}</th>
					<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
				</c:forEach>
			</tr>
			<tr>
				<td style="text-align: right">Daily Charge</td>
				<c:forEach var="bed" items="${bedTypes}" varStatus="k">
					<c:set var="i" value="${k.index}"/>
					<td>
						<input type="text" name="daily_charge" id="daily_charge${i}"
						class="number validate-decimal"
						value="${ifn:afmt(charges[bed].daily_charge)}"
						onblur="validateDiscount('daily_charge','daily_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
						<input type="hidden" name="ids" value="${i+1}">
					</tr>
					<tr>
						<td style="text-align: right">Daily Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="daily_charge_discount" id="daily_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed].daily_charge_discount)}"
								onblur="validateDiscount('daily_charge','daily_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
					</tr>
			<tr>
				<td style="text-align: right">Min Charge</td>
				<c:forEach var="bed" items="${bedTypes}" varStatus="k">
					<c:set var="i" value="${k.index}"/>
					<td>
						<input type="text" name="min_charge" id="min_charge${i}"
						class="number validate-decimal"
						value="${ifn:afmt(charges[bed].min_charge)}"
						onblur="validateDiscount('min_charge','min_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
						<input type="hidden" name="ids" value="${i+1}">
					</tr>
					<tr>
						<td style="text-align: right">Min Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="min_charge_discount" id="min_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed].min_charge_discount)}"
								onblur="validateDiscount('min_charge','min_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Slab 1 Charge</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="slab_1_charge" id="slab_1_charge${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed].slab_1_charge)}"
								onblur="validateDiscount('slab_1_charge','slab_1_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
					</tr>
					<tr>
						<td style="text-align: right">Slab 1 Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="slab_1_charge_discount" id="slab_1_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed].slab_1_charge_discount)}"
								onblur="validateDiscount('slab_1_charge','slab_1_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
						<td style="text-align: right">Incr Charge</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="incr_charge" id="incr_charge${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed].incr_charge)}"
								onblur="validateDiscount('incr_charge','incr_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
					</tr>
					<tr>
						<td style="text-align: right">Incr Charge Discount</td>
						<c:forEach var="bed" items="${bedTypes}" varStatus="k">
							<c:set var="i" value="${k.index}"/>
							<td>
								<input type="text" name="incr_charge_discount" id="incr_charge_discount${i}"
								class="number validate-decimal"
								value="${ifn:afmt(charges[bed].incr_charge_discount)}"
								onblur="validateDiscount('incr_charge','incr_charge_discount','${i}')"
								onkeypress="return nextFieldOnTab(event, this, 'otCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
					   <c:if test="${not empty bedTypes}">
					     <td style="text-align: right">Copy Charges to all Bed Types</td>
					     <td>
					       <input type="checkbox" name="checkbox" onclick="fillValues('otCharges', this);"/>
					     </td>
					      <c:forEach begin="2" end="${fn:length (bedTypes)}">
					       <td>&nbsp;</td>
					     </c:forEach>
			   </c:if>
			</tr>
	</table>
</div>
</fieldset>

	<div class="screenActions" align="left">
		<button type="button" name="Save" accesskey="S" onclick="onSave();"><b><u>S</u></b>ave</button>
		<c:choose>
			<c:when test="${fromItemMaster eq 'false'}">
				| <a href="${cpath}/pages/masters/ratePlan.do?_method=getOtChargesListScreen&org_id=${ifn:cleanURL(org_id)}&org_name=${ifn:cleanURL(org_name)}">OT Charges List</a>
			</c:when>
			<c:otherwise>
				<c:set var="url" value="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=showCharges"/>
				| <a href="<c:out value='${url}&theatreId=${ifn:cleanURL(param.theatre_id)}&orgId=${ifn:cleanURL(baseRateSheet)}'/>">OT Charges</a>
			</c:otherwise>
		</c:choose>
	</div>
</form>
</body>
</html>