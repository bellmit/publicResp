<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Service Definition - Insta HMS</title>
		<insta:link type="js" file="hmsvalidation.js" />
		<insta:link type="js" file="ajax.js" />
		<insta:link type="js" file="masters/charges_common.js" />
		<insta:link type="js" file="masters/service.js" />
		<insta:link type="js" file="masters/orderCodes.js" />
		<c:set var="cpath" value="${pageContext.request.contextPath}" />

		<script>
		function onSave() {
			if(!validateAllDiscounts()) return false;
			document.serviceForm.submit();
		}
		function validateAllDiscounts() {
			var len = document.serviceForm.ids.value;
			var valid = true;
			for(var i=0;i<len;i++) {
				valid = valid && validateDiscount('unit_charge','discount',i);
			}
			if(!valid) return false;
			else return true;
		}
		</script>

	</head>

		<body class="yui-skin-sam">
			<form method="POST" action="${cpath}/pages/masters/ratePlan.do" name="serviceForm" >
			<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>
			<input type="hidden" name="service_id" id="service_id" value="${bean.map.service_id}">
			<input type="hidden" name="_method" value="overRideServiceCharges">
			<input type="hidden" name="chargeCategory" value="services"/>
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

			<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
			<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Service Details</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Service Name:</td>
						<td class="forminfo">${bean.map.service_name}</td>
					</tr>
					<tr>
						<td class="formlabel">Treatment Code Type</td>
						<td>
							<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
							displaycol="code_type"	filtervalue="Treatment" filtercol="code_category" dummyvalue="--Select--"
							value="${bean.map.code_type}"/>
						<td class="formlabel">Rate Plan Code:</td>
						<td><input type="text" name="item_code" maxlength="600" value="${bean.map.item_code}"/></td>
					</tr>
					<tr>
					<td class="formlabel">PackageID:</td>
					<td><input type="text" name="special_service_code" maxlength="15" value="${bean.map.special_service_code}"/></td>
					<td class="formlabel">Package Contract Name:</td>
					<td><input type="text" name="special_service_contract_name" maxlength="100" value="${bean.map.special_service_contract_name}"/></td>
				</tr>
				</table>
			</fieldset>

			<div class="resultList">
				<table class="dataTable" id="serviceCharges" align="center">
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
								<input type="text" name="unit_charge" id="unit_charge${i}" class="number validate-decimal"
									value="${ifn:afmt(charges[bed].unit_charge)}" onblur="validateDiscount('unit_charge','discount','${i}')"/>
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
									value="${ifn:afmt(charges[bed].discount)}" onblur="validateDiscount('unit_charge','discount','${i}');"/>
							</td>
						</c:forEach>
					</tr>
					<c:if test="${not empty bedTypes}">
						<tr>
							<td>Apply Charges To All</td>
							<td><input type="checkbox" name="checkbox" onclick="fillValues('serviceCharges', this);" /></td>
							<c:forEach begin="2" end="${fn:length (bedTypes)}">
								<td>&nbsp;</td>
							</c:forEach>
						</tr>
					</c:if>
				</table>
			</div>

			<table class="screenActions">
				<tr>
					<td>
						<button type="button" name="Save" accesskey="S" onclick="onSave();"><b><u>S</u></b>ave</button>
						<c:choose>
							<c:when test="${fromItemMaster eq 'false'}">
								| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=services&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Service Charges List</a>
							</c:when>
							<c:otherwise>
								<c:set var="url" value="${cpath}/master/ServiceMaster.do?_method=showCharges"/>
								| <a href="<c:out value='${url}'/>&service_id=${bean.map.service_id}&org_id=${ifn:cleanURL(baseRateSheet)}">Service Charges</a>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
