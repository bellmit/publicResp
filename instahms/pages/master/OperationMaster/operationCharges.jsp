<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Surgery/Procedure Definition - Insta HMS</title>
		<insta:link type="js" file="hmsvalidation.js" />
		<insta:link type="js" file="masters/charges_common.js" />
		<insta:link type="js" file="masters/operation.js" />
		<insta:link type="js" file="masters/editCharges.js"/>
		<insta:link type="js" file="ajax.js" />
		<insta:link type="js" file="masters/orderCodes.js" />

		<script>
		   Insta.masterData=${opLists};
		   var masterJobCount = '${masterJobCount}';
		</script>
	</head>
		<body class="yui-skin-sam" onload="fillRatePlanDetails('operations','op_id','${bean.map.op_id}');">
				<h1 style="float:left">Surgery/Procedure Charges</h1>
				<c:url var="searchUrl" value="/master/OperationMaster.do"/>
				<insta:findbykey keys="operation_name,op_id" fieldName="op_id" method="showCharges" url="${searchUrl}"
			    	 extraParamKeys="org_id" extraParamValues="${bean.map.org_id}"/>

			<form action="OperationMaster.do" name="showform" method="GET">		<%-- for rate plan change --%>
				<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}" />
				<input type="hidden" name="op_id" value="${bean.map.op_id}"/>
				<input type="hidden" name="org_id" value=""/>
				<input type="hidden" name="Referer" value="${empty param.Referer ? header.Referer : param.Referer}"/>
			</form>

			<form name="chargesForm" method="POST" action="OperationMaster.do">
				<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}" />
				<input type="hidden" name="op_id" value="${bean.map.op_id}"/>
				<input type="hidden" name="Referer" value="${empty param.Referer ? header.Referer : param.Referer}"/>

					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Surgery/Procedure Details</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Surgery/Procedure Name:</td>
								<td class="forminfo">${bean.map.operation_name}</td>
								<td class="formlabel">Rate Sheet:</td>
								<td>
									<insta:selectdb name="org_id" value="${bean.map.org_id}"
										table="organization_details" valuecol="org_id" orderby="org_name"
										displaycol="org_name" onchange="changeRatePlanAddShow();"
										filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Treatment Code Type:</td>
								<td>
										<insta:selectdb name="code_type" table="mrd_supported_codes" value="${bean.map.code_type}"
										valuecol="code_type" displaycol="code_type" dummyvalue="--Select--" filtervalue="Treatment"
										filtercol="code_category"/>
								</td>
								<td class="formlabel">Rate Plan Code:</td>
								<td><input type="text" name="item_code" maxlength="600" value="${bean.map.item_code}"/></td>
							</tr>
						</table>
					</fieldset>

				<div class="resultList">
					<fieldset class="fieldsetBorder">
					<legend class="fieldsetLabel">Rate Details</legend>
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
						<tr id="audit_log_row">
							<td></td>
							<c:forEach var="bed" items="${bedTypes}">
								<td style="width: 2em; overflow: hidden">
									<insta:screenlink
									extraParam="?_method=getAuditLogDetails&op_id=${bean.map.op_id}&org_id=${bean.map.org_id}&bed_type=${bed}&operation_name=${ifn:encodeUriComponent(bean.map.operation_name)}&al_table=operation_charges_audit_log_view"
										screenId="operations_audit_log" label="Ch Audit Log"/>
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
					</fieldset>
				</div>

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
									<img src='${pageContext.request.contextPath}/images/blue_flag.gif'>Overridden
								</td>
							</tr>
						</table>
					</fieldset>
				</div>

				<table class="screenActions">
				<tr>
					<td>
						<button type="button" name="update" accesskey="U" onclick="submitCharges();"><b><u>U</u></b>pdate</button>
					</td>
					<td>&nbsp;|&nbsp;</td>
					<td><a href="${pageContext.request.contextPath}/master/OperationMaster.do?_method=show&op_id=${bean.map.op_id}&org_id=${bean.map.org_id}" >Surgery/Procedure Details</a></td>
					<td>&nbsp;|&nbsp;</td>
					<td>
						<a href="${pageContext.request.contextPath}/master/OperationMaster.do?_method=list&status=A&sortOrder=operation_name&sortReverse=false">Surgeries/Procedure List</a>
					</td>
				</tr>
				</table>
			</form>
		<script>
			var derivedRatePlanDetails = ${derivedRatePlanDetails};
		</script>
	</body>
</html>