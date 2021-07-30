<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/charges_common.js"/>
<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>

<script>
	var defaultValue = ${defaultValue};
	var prefDecimalDigits = ${prefDecimalDigits};
	var itemGroupList = ${itemGroupListJson};
	var itemSubGroupList = ${itemSubGroupListJson};
	
	function submitForm() {
		document.showItemRateForm.store_tariff.value = document.itemRateForm.store_tariff.value;
		document.showItemRateForm.submit();
	}
</script>

<style type="text/css">
  #myAutoComplete{
	 width:15em; /* set width here or else widget will expand to fit its container */
     padding-bottom:2em;
  }
</style>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Edit Item Rates - Insta HMS</title>
</head>

<body class="formtable" class="yui-skin-sam" onload="itemsubgroupinit();">
<h1>Edit Item Rates</h1>
<insta:feedback-panel/>
<form name="showItemRateForm" method="GET">
			<input type="hidden" name="_method" value="show"/>
			<input type="hidden" name="medicine_id" value="${bean.map.medicine_id }"/>
			<input type="hidden" name="store_tariff" value=""/>

</form>
<form name="itemRateForm">
	<input type="hidden" name="_method" value="update">
	<input type="hidden" name="editOrgId"/>

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Item Details</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Item Name :</td>
				<td>
					${bean.map.medicine_name }
					<input type="hidden" name="medicine_id" value="${bean.map.medicine_id }"/>
				</td>
				<td class="formlabel">Package Size:</td>
				<td>${bean.map.issue_base_unit }</td>
				<td class="formlabel">Store Tariff :</td>
				<td><insta:selectdb name="store_tariff" id="store_tariff" table="store_rate_plans" value="${store_tariff}" valuecol="store_rate_plan_id" displaycol="store_rate_plan_name"
							filtered="true" onchange="return submitForm();" orderby="store_rate_plan_name"/></td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Selling Prices (Pkg)</legend>
		<table width="100%" class="dataTable" id="chargesTable">
				<tr>
					<th>Store Tariffs</th>
					<th>Tax Basis</th>
					<th>Selling Price Expression</th>
				</tr>
				<tr>
					<td class="formlabel">${itemRates.map.store_rate_plan_name}
						<input type="hidden" name="store_rate_plan_name" value="${itemRates.map.store_rate_plan_name}"/>
						<input type="hidden" name="store_rate_plan_id" value="${itemRates.map.store_rate_plan_id }" />
					</td>
					<td>
						<insta:selectoptions name="tax_type" value="${itemRates.map.tax_type}"
							opvalues="MB,M,CB,C" optexts="MRP Based(with bonus),MRP Based(without bonus),CP Based(with bonus),CP Based(without bonus)" />
					</td>
					<td><textarea name="selling_price_expr" cols="80" rows="1" maxlength="1000" onchange="onChangeCheckValue(this)">${!empty itemRates.map.selling_price_expr ? itemRates.map.selling_price_expr : defaultValue}</textarea></td>
				</tr>
		</table>
	</fieldset>
	<dl class="accordion" style="margin-bottom: 10px;">
			<dt>
				<span>Expression Help</span>
				<div class="clrboth"></div>
			</dt>
			<dd id="expr_tokens">
				<div class="bd">
					<table class="resultList">
						<tr>
							<td colspan="6"><b> Sample expression:</b>
							<#if mrp &lt;120>\${average_cp+25*average_cp}<#else>\${average_cp+15*average_cp} &lt;/#if>
							</td>
						</tr>
						<tr>
							<td colspan="6"><b>Available Tokens:</b></td>
						</tr>
						<tr>
							<c:forEach items="${exprTokens}" var="token" varStatus="it">
							<c:if test="${(it.index % 6) == 0}">
							</tr><tr>
							</c:if>
							<td>${token}</td>
							</c:forEach>
						</tr>
					</table>
				</div>
			</dd>
		</dl>
	<div style="clear: both"></div>
	<insta:taxations/>
	<table class="screenActions">
		<tr >
			<input  type="submit" accesskey="S" name="Save" value="Save"/>
			<insta:screenlink addPipe="true" screenId="stores_item_rates_mas" label="Store Item Rates" extraParam="?_method=list"/>
		</tr>
	</table>

</form>
</body>
</html>
