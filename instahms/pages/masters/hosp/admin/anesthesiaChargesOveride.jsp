<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
<insta:link type="js" file="masters/addAnaesthesiatype.js"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/orderCodes.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<script>
	var cpath = '${cpath}';
	function onSave(){
		if(!validateAllDiscounts()) return false;
		document.anaesthesiaform.submit();
	}

	function validateAllDiscounts() {
		var len = document.anaesthesiaform.ids.value;
		var valid = true;
		for(var i=0;i<len;i++) {
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
<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>

<form name="anaesthesiaform" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="overRideAnesthesiaCharges" />
	<input type="hidden" name="anesthesia_type_id" value="${ifn:cleanHtmlAttribute(param.anesthesia_type_id)}"/>
	<input type="hidden" name="chargeCategory" value="anesthesia"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

	<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
	<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Anaesthesia Charge Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Anaesthesia Type Name:</td>
			<td class="forminfo">${bean.map.anesthesia_type_name}</td>
			<td class="formlabel">Treatment Code Type:</td>
			<td>
					<insta:selectdb name="code_type" table="mrd_supported_codes" value="${bean.map.code_type}"
					valuecol="code_type" displaycol="code_type" dummyvalue="--Select--" filtervalue="Treatment"
					filtercol="code_category"/>
			</td>
			<td class="formlabel">Rate Plan Code:</td>
			<td><input type="text" name="item_code" maxlength="20" value="${bean.map.item_code}"/></td>
		</tr>
		<tr>
			<td class="formlabel">Unit Size (minutes):</td>
			<td class="forminfo">${bean.map.duration_unit_minutes}</td>
			<td class="formlabel">Minimum Duration (units):</td>
			<td class="forminfo">${bean.map.min_duration}</td>
		</tr>
		<tr>
			<td class="formlabel">Slab 1 Threshold (units):</td>
			<td class="forminfo">${bean.map.slab_1_threshold}</td>
			<td class="formlabel">Incr Duration (units):</td>
			<td class="forminfo">${bean.map.incr_duration}</td>
		</tr>
	</table>
</fieldset>

<div class="resultList">
	<table class="dataTable" id="anaesthesiaCharges">
		<tr>
				<th>Bed Types</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden">${bed}</th>
					<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
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
								onkeypress="return nextFieldOnTab(event, this, 'anaesthesiaCharges');"/>
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
								onkeypress="return nextFieldOnTab(event, this, 'anaesthesiaCharges');"/>
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
								onkeypress="return nextFieldOnTab(event, this, 'anaesthesiaCharges');"/>
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
								onkeypress="return nextFieldOnTab(event, this, 'anaesthesiaCharges');"/>
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
								onkeypress="return nextFieldOnTab(event, this, 'anaesthesiaCharges');"/>
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
								onkeypress="return nextFieldOnTab(event, this, 'anaesthesiaCharges');"/>
							</td>
						</c:forEach>
					</tr>

					<tr>
					   <c:if test="${not empty bedTypes}">
					     <td style="text-align: right">Copy Charges to all Bed Types</td>
					     <td>
					       <input type="checkbox" name="checkbox" onclick="fillValues('anaesthesiaCharges', this);"/>
					     </td>
					      <c:forEach begin="2" end="${fn:length (bedTypes)}">
					       <td>&nbsp;</td>
					     </c:forEach>
			   </c:if>
			</tr>
	</table>
</div>

	<div class="screenActions" align="left">
		<button type="button" name="Save" accesskey="S" onclick="onSave();"><b><u>S</u></b>ave</button>
		<c:choose>
			<c:when test="${fromItemMaster eq 'false'}">
				| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=anesthesia&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Anaesthesia Charges List</a>
			</c:when>
			<c:otherwise>
				<c:set var="url" value="${cpath}/master/AnaesthesiaTypeMaster.do?_method=showCharges"/>
				| <a href="<c:out value='${url}&anesthesia_type_id=${ifn:cleanURL(param.anesthesia_type_id)}&org_id=${ifn:cleanURL(baseRateSheet)}'/>">Anaesthesia Charges</a>
			</c:otherwise>
		</c:choose>
	</div>
</form>
</body>
</html>
