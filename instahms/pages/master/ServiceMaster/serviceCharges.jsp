<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Service Charges - Insta HMS</title>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link type="js" file="masters/editCharges.js"/>
	<insta:link type="js" file="masters/service.js" />
	<insta:link type="js" file="masters/orderCodes.js" />

	<script>
		 Insta.masterData=${serviceList};
	</script>
</head>
	<body class="yui-skin-sam" onload="fillRatePlanDetails('services','service_id','${bean.map.service_id}');">
		<c:url var="searchUrl" value="/master/ServiceMaster.do"/>
		 <h1 style="float:left">Service Charges</h1>
		<insta:findbykey keys="service_name,service_id" fieldName="service_id" method="showCharges" url="${searchUrl}"
			extraParamKeys="org_id" extraParamValues="${bean.map.org_id}"/>
		<insta:feedback-panel/>
		<form action="ServiceMaster.do" name="showChargesForm" method="GET">
			<input type="hidden" name="_method" value="showCharges"/>
			<input type="hidden" name="service_id" value="${bean.map.service_id}"/>
			<input type="hidden" name="org_id" value=""/>

		</form>

		<form action="ServiceMaster.do" name="chargesForm" method="POST">
			<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}"/>
			<input type="hidden" name="service_id" value="${bean.map.service_id}"/>
			<fieldset class="fieldsetBorder">
			<legend class="fieldsetLabel">Service Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Service Name:</td>
					<td class="forminfo">${bean.map.service_name}</td>
					<td class="formlabel">Rate Sheet:</td>
					<td>
						<c:choose>
							<c:when test="${param._method eq 'showCharges'}">
								<insta:selectdb name="org_id" value="${bean.map.org_id}"
									table="organization_details" valuecol="org_id"
									displaycol="org_name" orderby="org_name" onchange="changeRatePlanAddShow();"
									filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
							</c:when>
							<c:otherwise>
								<label class="forminfo">GENERAL</label>
								<input type="hidden" name="org_id" value="ORG0001">
							</c:otherwise>
						</c:choose>
					</td>
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
				<fieldset class="fieldsetBorder">
					<legend class="fieldsetLabel">Rate Details</legend>
					<table class="formtable">
						<tr>
							<td colspan="6">
								<table class="dataTable" id="serviceCharges" cellpadding="0" cellspacing="0">
									<tr>
										<th>Bed Types</th>
										<c:forEach var="bed" items="${bedTypes}">
											<th style="width: 2em; overflow: hidden">${bed}</th>
											<input type="hidden" name="bed_type" value="<c:out value='${bed}'/>"/>
										</c:forEach>
									</tr>

									<tr>
										<td>Charge</td>
										<c:forEach var="bed" items="${bedTypes}" varStatus="k">
											<c:set var="i" value="${k.index}"/>
											<td>
												<input type="text" name="unit_charge" id="unit_charge${i}" class="number validate-decimal"
												value="${ifn:afmt(charges[bed])}" onblur="validateDiscount('unit_charge','discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'serviceCharges');">
											</td>
										</c:forEach>
										<input type="hidden" name="ids" value="${i+1}">
									</tr>

									<tr>
										<td>Discount</td>
										<c:forEach var="bed" items="${bedTypes}" varStatus="k">
											<c:set var="i" value="${k.index}"/>
											<td>
												<input type="text" name="discount" id="discount${i}" class="number validate-decimal"
												value="${ifn:afmt(discounts[bed])}" onblur="validateDiscount('unit_charge','discount','${i}')" onkeypress="return nextFieldOnTab(event, this, 'serviceCharges');">
											</td>
										</c:forEach>
									</tr>
									<tr id="audit_log_row">
										<td>&nbsp;</td>
										<c:forEach var="bed" items="${bedTypes}">
											<td style="width: 2em; overflow: hidden">
												<insta:screenlink screenId="services_audit_log"
													extraParam="?_method=getAuditLogDetails&service_id=${bean.map.service_id}&bed_type=${bed}&org_id=${bean.map.org_id}&service_name=${ifn:encodeUriComponent(bean.map.service_name)}&al_table=service_master_charges_audit_log_view" label="Ch Audit Log"/>
											</td>
										</c:forEach>
									</tr>
									<tr>
										<c:if test="${not empty bedTypes}">
										   <td style="text-align: right">Apply Charges To All</td>
										   <td><input type="checkbox" name="checkbox" onclick="fillValues('serviceCharges', this);"/></td>
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
								<img src='${cpath}/images/blue_flag.gif'>Overridden
							</td>
						</tr>
					</table>
				</fieldset>
			</div>

			<table class="screenActions">
				<tr>
					<td>
						<button type="button" name="update" accesskey="U" onclick="doSaveCharges();"><b><u>U</u></b>pdate</button>
					</td>
					<td>&nbsp;|&nbsp;</td>
					<td><a href="${pageContext.request.contextPath}/master/ServiceMaster.do?_method=show&service_id=${bean.map.service_id}&org_id=${bean.map.org_id}">Service Details</a></td>
					<td>&nbsp;|&nbsp;</td>
					<td>
						<a href="${pageContext.request.contextPath}/master/ServiceMaster.do?_method=list&status=A&sortOrder=service_name&sortReverse=false">Services List</a>
					</td>
				</tr>
			</table>
		</form>
		<script>
			var derivedRatePlanDetails = ${derivedRatePlanDetails};
		</script>
	</body>
</html>
