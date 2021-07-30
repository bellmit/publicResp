<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

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
		<insta:link type="js" file="masters/addEquipmet.js" />
		<insta:link type="js" file="masters/charges_common.js" />
		<insta:link type="css" file="widgets.css" />
		<insta:link type="script" file="widgets.js" />
		<insta:link type="js" file="ajax.js" />
		<insta:link type="js" file="masters/orderCodes.js" />
		<c:set value="${pageContext.request.contextPath}" var="cpath" />
		<script>
			var cpath = '${cpath}';
			function onSave(){
				var form = document.equipmentform;
				var taxCharge = form.tax;
				if(taxCharge.length == undefined){
					 if(parseFloat(taxCharge.value) >100){
						alert("Tax(%) can not be more than 100");
						taxCharge.focus();
						return false;
					 }
				}else{
					for(var i=0; i<taxCharge.length;i++){
						if(parseFloat(taxCharge[i].value) > 100){
							alert("one of the Tax(%) field is more than 100");
							taxCharge[i].focus();
							return false;
						}
					}
				}
				if(!validateAllDiscounts()) return false;
				form.submit();
			}
			function validateAllDiscounts() {
				var len = document.equipmentform.ids.value;
				var valid = true;
				for(var i=0;i<len;i++) {
					valid = valid && validateDiscount('daily_charge','daily_charge_discount',i);
					valid = valid && validateDiscount('min_charge','min_charge_discount',i);
					valid = valid && validateDiscount('incr_charge','incr_charge_discount',i);
					valid = valid && validateDiscount('slab_1_charge','slab_1_charge_discount',i);
				}
				if(!valid) return false;
				else return true;
			}
		</script>
	</head>
	<body>
		<form method="POST" action="${cpath}/pages/masters/ratePlan.do" name="equipmentform" >
			<h1>Rate Plan Overrides - ${ifn:cleanHtml(org_name)}</h1>
			<input type="hidden" name="equip_id" id="equip_id" value="${bean.map.eq_id}">
			<input type="hidden" name="_method" value="overRideEquipmentCharges">
			<input type="hidden" name="chargeCategory" value="equipment"/>
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
			<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_name)}"/>

			<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
			<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Equipment Charges</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Equipment Name:</td>
						<td class="forminfo">${bean.map.equipment_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Unit Size (Minutes):</td>
						<td class="forminfo">${bean.map.duration_unit_minutes}</td>
						<td class="formlabel">Minimum Duration (units):</td>
						<td class="forminfo">${bean.map.min_duration}</td>
					</tr>
					<tr>
						<td class="formlabel">Slab 1 Threshold (units):</td>
						<td class="forminfo">${bean.map.slab_1_threshold}</td>
						<td class="formlabel">Incr. Duration (units):</td>
						<td class="forminfo">${bean.map.incr_duration}</td>
					</tr>
				</table>
				<div class="resultList">
					<table class="dataTable" id="equipmentCharges" align="left">
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
								</td>
							</c:forEach>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
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
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
								</td>
							</c:forEach>
						</tr>
						<tr>
							<td style="text-align: right">Tax(%)</td>
							   <c:forEach var="bed" items="${bedTypes}" varStatus="k">
							     <c:set var="i" value="${k.index}"/>
								 <td><input type="text" name="tax" class="number validate-decimal" id="tax${i}"
									value="${ifn:afmt(charges[bed].tax)}"
									onkeypress="return nextFieldOnTab(event, this, 'equipmentCharges');"/>
								 </td>
							  </c:forEach>
						</tr>
						<tr>
						   <c:if test="${not empty bedTypes}">
						     <td style="text-align: right">Copy charges to all Beds</td>
						     <td>
						       <input type="checkbox" name="checkbox" onclick="fillValues('equipmentCharges', this);"/>
						     </td>
						      <c:forEach begin="2" end="${fn:length (bedTypes)}">
						       <td>&nbsp;</td>
						     </c:forEach>
						   </c:if>
						</tr>
					</table>
				</div>
			</fieldset>
			<table class="screenActions">
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="onSave();"><b><u>S</u></b>ave</button>
						<c:choose>
							<c:when test="${fromItemMaster eq 'false'}">
								| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=equipment&org_id=${ifn:cleanURL(org_id)}&org_name=${ifn:cleanURL(org_name)}">Equipment Charges List</a>
							</c:when>
							<c:otherwise>
								<c:set var="url" value="${cpath}/master/equipment/editcharge.htm?"/>
								| <a href="<c:out value='${url}equip_id=${bean.map.eq_id}&org_id=${ifn:cleanURL(baseRateSheet)}'/>">Equipment Charges</a>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
