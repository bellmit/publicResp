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
<title>Anaesthesia Type Details - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/addAnaesthesiatype.js"/>
<insta:link type="js" file="masters/editCharges.js"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/orderCodes.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<script>
	var cpath = '${cpath}';
	Insta.masterData=${anaesthesiaTypeLists};
</script>
</head>
<body onload="fillRatePlanDetails('anesthesia','anesthesia_type_id','${bean.map.anesthesia_type_id}');">
	  <h1 style="float:left">Anaesthesia Charges</h1>
	  <c:url var="searchUrl" value="/master/AnaesthesiaTypeMaster.do"/>
	  <insta:findbykey keys="anesthesia_type_name,anesthesia_type_id" fieldName="anesthesia_type_id" method="showCharges" url="${searchUrl}"/>

	<form action="AnaesthesiaTypeMaster.do" name="showform" method="GET">		<%-- for rate plan change --%>
		<input type="hidden" name="_method" value="showCharges" />
		<input type="hidden" name="anesthesia_type_id" value="${bean.map.anesthesia_type_id}"/>
		<input type="hidden" name="org_id" value="${bean.map.org_id}"/>
		<input type="hidden" name="Referer" value="${empty param.Referer ? header.Referer : param.Referer}"/>
	</form>
	<form name=anaesthesiaChargesForm action="AnaesthesiaTypeMaster.do" method="POST">
		<input type="hidden" name="_method" value="${requestScope.method}" />
		<input type="hidden" name="anesthesia_type_id" value="${ifn:cleanHtmlAttribute(param.anesthesia_type_id)}"/>

		<insta:feedback-panel/>
		<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Anaesthesia Name:</td>
				<td class="forminfo">${bean.map.anesthesia_type_name}</td>
				<td class="formlabel">Rate Sheet:</td>
				<td>
					<insta:selectdb name="org_id" value="${bean.map.org_id}"
						table="organization_details" valuecol="org_id" orderby="org_name"
						displaycol="org_name" onchange="changeRatePlanAddShow();"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
				</td>
				<td class="formlabel">Treatment Code Type:</td>
				<td>
						<insta:selectdb name="code_type" table="mrd_supported_codes" value="${bean.map.code_type}"
						valuecol="code_type" displaycol="code_type" dummyvalue="--Select--" filtervalue="Treatment"
						filtercol="code_category"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Rate Plan Code:</td>
				<td><input type="text" name="item_code" maxlength="20" value="${bean.map.item_code}"/></td>
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
				<td class="formlabel">Base Unit (units):</td>
				<td class="forminfo">${bean.map.base_unit}</td>
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
				<button type="button" name="update" accesskey="U" onclick="validateChargesSubmit();"><b><u>U</u></b>pdate</button>
			</td>
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=show&anesthesia_type_id=${bean.map.anesthesia_type_id}&org_id=${bean.map.org_id}">Anaesthesia Type</a>
			</td>
			<c:if test="${param._method=='show'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=add">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td>
				<a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=list&status=A&sortOrder=anesthesia_type_name&sortReverse=false&org_id=${bean.map.org_id}">Anaesthesia Type List</a>
			</td>
		</tr>
	</table>

	</form>
	<script>
		var derivedRatePlanDetails = ${derivedRatePlanDetails};
	</script>
</body>
</html>