<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Rate Plan Overrides - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link type="js" file="masters/operation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="masters/orderCodes.js" />
	<c:set var="cpath" value="${pageContext.request.contextPath}" />

	<script>
		function validateAllDiscounts() {
			var len = document.operationForm.ids.value;
			var valid = true;
			for(var i=0;i<len;i++) {
				valid = valid && validateDiscount('surgeon_charge','surg_discount',i);
				valid = valid && validateDiscount('anesthetist_charge','anest_discount',i);
				valid = valid && validateDiscount('surg_asstance_charge','surg_asst_discount',i);
			}
			if(!valid)  {
				return false;
			}else {
				document.operationForm.submit();
			}
		}
	</script>

</head>

<body class="yui-skin-sam">
	<form method="POST" action="${cpath}/pages/masters/ratePlan.do" name="operationForm" >
		<input type="hidden" name="_method" value="overRideOperationCharges">
		<input type="hidden" name="op_id" value="${bean.map.op_id}"/>
		<input type="hidden" name="chargeCategory" value="dynapackages"/>
		<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>

		<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
		<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>
		<h1>Rate Plan Overrides - ${bean.map.org_name}</h1>

		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel">Operation Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Operation Name:</td>
					<td class="forminfo">${bean.map.operation_name}</td>
					<td class="formlabel">Treatment Code Type:</td>
					<td>
							<insta:selectdb name="code_type" table="mrd_supported_codes" value="${bean.map.code_type}"
							valuecol="code_type" displaycol="code_type" dummyvalue="--Select--" filtervalue="Treatment"
							filtercol="code_category"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Rate Plan Code:</td>
					<td><input type="text" name="item_code" maxlength="600" value="${bean.map.item_code}"/></td>
				</tr>
			</table>
		</fieldset>

		<div class="resultList">
			<table class="formtable">
				<tr>
				<td colspan="6">
					<table class="dataTable" id="operationCharges" cellpadding="0" cellspacing="0" >
						<tr>
							<th>Bed Types</th>
							<c:forEach var="bed" items="${bedTypes}">
								<th style="width: 2em; overflow: hidden">${bed}</th>
								<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
							</c:forEach>
						</tr>

						<tr>
							<td style="text-align: right;">Surgeon Charge</td>
							<c:forEach var="bed" items="${bedTypes}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td>
									<input type="text" name="surgeon_charge" id="surgeon_charge${i}"
									class="number validate-decimal"
									value="${ifn:afmt(charges[bed].surgeon_charge)}"
									onblur="validateDiscount('surgeon_charge','surg_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'operationCharges');"/>
								</td>
							</c:forEach>
							<input type="hidden" name="ids" value="${i+1}">
						</tr>
						<tr>
							<td style="text-align: right">Surgeon Charge Discount</td>
							<c:forEach var="bed" items="${bedTypes}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td>
									<input type="text" name="surg_discount" id="surg_discount${i}"
									class="number validate-decimal"
									value="${ifn:afmt(charges[bed].surg_discount)}"
									onblur="validateDiscount('surgeon_charge','surg_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'operationCharges');"/>
								</td>
							</c:forEach>
						</tr>

						<tr>
							<td style="text-align: right">Anaesthetist Charge</td>
							<c:forEach var="bed" items="${bedTypes}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td>
									<input type="text" name="anesthetist_charge" id="anesthetist_charge${i}"
									class="number validate-decimal"
									value="${ifn:afmt(charges[bed].anesthetist_charge)}"
									onblur="validateDiscount('anesthetist_charge','anest_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'operationCharges');"/>
								</td>
							</c:forEach>
						</tr>
						<tr>
							<td style="text-align: right">Anaesthetist Charge Discount</td>
							<c:forEach var="bed" items="${bedTypes}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td>
									<input type="text" name="anest_discount" id="anest_discount${i}"
									class="number validate-decimal"
									value="${ifn:afmt(charges[bed].anest_discount)}"
									onblur="validateDiscount('anesthetist_charge','anest_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'operationCharges');"/>
								</td>
							</c:forEach>
						</tr>

						<tr>
							<td style="text-align: right">Surg. Assistance Charge</td>
							<c:forEach var="bed" items="${bedTypes}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td>
									<input type="text" name="surg_asstance_charge" id="surg_asstance_charge${i}"
									class="number validate-decimal"
									value="${ifn:afmt(charges[bed].surg_asstance_charge)}"
									onblur="validateDiscount('surg_asstance_charge','surg_asst_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'operationCharges');"/>
								</td>
							</c:forEach>
						</tr>
						<tr>
							<td style="text-align: right">Surg. Assistance Charge Discount</td>
							<c:forEach var="bed" items="${bedTypes}" varStatus="k">
								<c:set var="i" value="${k.index}"/>
								<td>
									<input type="text" name="surg_asst_discount" id="surg_asst_discount${i}"
									class="number validate-decimal"
									value="${ifn:afmt(charges[bed].surg_asst_discount)}"
									onblur="validateDiscount('surg_asstance_charge','surg_asst_discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'operationCharges');"/>
								</td>
							</c:forEach>
						</tr>
						<tr>
						    <c:if test="${not empty bedTypes}">
						      <td style="text-align: right">Apply Charges To All</td>
							  <td><input type="checkbox" name="checkbox" onclick="fillValues('operationCharges', this);"/></td>
							   <c:forEach begin="2" end="${fn:length (bedTypes)}">
							     <td>&nbsp;</td>
							   </c:forEach>
							</c:if>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		</fieldset>
		</div>

		<table class="screenActions">
			<tr>
				<td>
					<button type="button" name="Save" accesskey="S" onclick="validateAllDiscounts();"><b><u>S</u></b>ave</button>
					<c:choose>
						<c:when test="${fromItemMaster eq 'false'}">
							| <a href="${cpath}/pages/masters/ratePlan.do?_method=getChargesListScreen&chargeCategory=operations&org_id=${ifn:cleanURL(org_id)}&org_name=${bean.map.org_name}">Operation Charges List</a>
						</c:when>
						<c:otherwise>
							<c:set var="url" value="${cpath}/master/OperationMaster.do?_method=showCharges"/>
							| <a href="<c:out value='${url}&op_id=${bean.map.op_id}&org_id=${ifn:cleanURL(baseRateSheet)}&operation_name=${bean.map.operation_name}'/>">Operation Charges</a>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>
	</form>
</body>
</html>
