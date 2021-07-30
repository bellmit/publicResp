<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/addOrganization.js" />
<insta:link type="js" file="masters/storerateplans.js" />
<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>

<script>
	var cpath = '${pageContext.request.contextPath}';
	var existingStoresRatePlansJSON = ${existingStoresRatePlansJSON};
	var RPListJSON = '';
	<c:if test="${ param._method == 'show' }">
		RPListJSON = ${RPListJSON};
	</c:if>
	var ratePlanName = '${bean.map.store_rate_plan_name }';
</script>

<style type="text/css">
  #myAutoComplete{
	 width:15em; /* set width here or else widget will expand to fit its container */
     padding-bottom:2em;
  }
</style>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Add/Edit Store Tariffs - Insta HMS</title>
</head>

<body class="formtable" class="yui-skin-sam">
<h1>Add/Edit Store Tariffs</h1>
<insta:feedback-panel/>
<form name="storerateplanform">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="editOrgId"/>

	<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Store Tariffs Details</legend>
		<table class="formtable" >
			<tr>
				<td class="formlabel">Tariff Name :</td>
				<td>
					<input type="text" name="store_rate_plan_name" id="store_rate_plan_name"
						maxlength="100"
						value="${bean.map.store_rate_plan_name }"/>
					<input type="hidden" name="store_rate_plan_id" value="${bean.map.store_rate_plan_id }"/>
				</td>
				<td class="formlabel">Status :</td>
				<td>
					<insta:selectoptions name="status"  opvalues="A,I"
						optexts="Active,Inactive" value="${bean.map.status}" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr >
				<td class="formlabel">Remarks:</td>
				<td><textarea cols="40" rows="2" name="remarks" >${bean.map.remarks }</textarea></td>
			</tr>
			<tr>
				<td class="formlabel">Copy From Tariff:</td>
				<td>
					<select name="cp_rate_plan_id" id="cp_rate_plan_id" ${param._method == 'show' ? 'disabled' : '' } class="dropdown">
						<option value="">--Select--</option>
						<c:forEach items="${existingStoresRatePlans}" var="rp">
							<c:if test="${ rp.map.status == 'A' }">
								<option value="${rp.map.store_rate_plan_id }">${rp.map.store_rate_plan_name }</option>
							</c:if>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
	</fieldset>

	<div style="clear: both"></div>
	<table class="screenActions">
		<tr >
			<button type="button" accesskey="S" property="Save" onclick="validate();"><b><u>S</u></b>ave</button>
			<c:if test="${param._method=='show'}">
				<insta:screenlink addPipe="true" screenId="stores_rate_plan_mas" label="Add" extraParam="?_method=add"/>
				<insta:screenlink addPipe="true" screenId="stores_item_rates_mas" label="Edit Rates" extraParam="?_method=list&store_rate_plan_id=${bean.map.store_rate_plan_id }&store_rate_plan_id@cast=y"/>
			</c:if>
			<insta:screenlink addPipe="true" screenId="stores_rate_plan_mas" label="Store Tariffs" extraParam="?_method=list"/>
		</tr>
	</table>

</form>
</body>
</html>
