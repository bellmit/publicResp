<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Rate Plan Overrides - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="masters/charges_common.js"/>
<insta:link type="script" file="widgets.js" />
<c:set value="${pageContext.request.contextPath}" var="cpath" />
<script>
	var cpath = '${cpath}';
</script>
</head>
<body>
	 <h1>Rate Plan Overrides - ${bean.map.org_name}</h1>


<form name="testForm" action="${cpath}/pages/masters/ratePlan.do">
	<input type="hidden" name="_method" value="overRideDiagnosticCharges" />
	<input type="hidden" name="test_id" value="${ifn:cleanHtmlAttribute(param.test_id)}"/>
	<input type="hidden" name="chargeCategory" value="diagnostics"/>
	<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
	<input type="hidden" name="org_name" value="${ifn:cleanHtmlAttribute(org_id)}"/>

	<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
	<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Test Name :</td>
				<td class="forminfo"> ${bean.map.test_name} </td>
				<td class="formlabel">Treatment Code Type</td>
				<td>
					<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
					displaycol="code_type" filtercol="code_category" filtervalue="Treatment" dummyvalue="--Select--"
					value="${bean.map.code_type}" />
				</td>
				<td class="formlabel">Rate Plan Code :</td>
				<td>
					<input type="text" name="item_code" id="item_code" value="${bean.map.item_code}" />
				</td>
			</tr>
		</table>
	</fieldset>
	<div class="resultList">
		<table class="dataTable" id="testCharges" align="center">
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
					<td><input type="checkbox" name="checkbox" onclick="fillValues('testCharges', this);" /></td>
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
					<button type="submit" name="Save" accesskey="U"><b><u>S</u></b>ave</button>
					<c:choose>
						<c:when test="${fromItemMaster eq 'false'}">
							| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=diagnostics&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Test Charges List</a>
						</c:when>
						<c:otherwise>
							<c:set var="url" value="${cpath}/master/addeditdiagnostics/editcharge.htm?"/>
							| <a href="<c:out value='${url}&testid=${ifn:cleanURL(param.test_id)}&orgId=${ifn:cleanURL(baseRateSheet)}'/>">Test Charges</a>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>
</form>
</body>
</html>
