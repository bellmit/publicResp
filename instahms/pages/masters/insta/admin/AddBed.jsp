<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insta HMS</title>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="masters/bedmaster.js" />
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	var cpath = '${cpath}';
	var inUse = false;

	<c:if test="${param._method == 'getEditChargesScreen'}">
		inUse = ${bedTypeInUse};
	</c:if>
	var itemGroupList = ${itemGroupListJson};
	var itemSubGroupList = ${itemSubGroupListJson};
	function validateForm() {
		var isInsuranceCatIdSelected = false;
		var insuranceCatId = document.getElementById('insurance_category_id');
		for (var i=0; i<insuranceCatId.options.length; i++) {
		  if (insuranceCatId.options[i].selected) {
			  isInsuranceCatIdSelected = true;
		  }
		}
		if (!isInsuranceCatIdSelected) {
			alert("Please select at least one insurance category");
			return false;
		} else {
			return validate();
		}
	}
</script>
</head>

<body onload="fillRatePlanDetails('${newbedmasterform.bedtype}');itemsubgroupinit();">
	<h1>Add/Edit Bed Details</h1>

	<insta:feedback-panel/>

	<html:form action="/pages/masters/insta/admin/newbedmaster.do?"
	onsubmit="return validateForm()">
	<input type="hidden" value="${requestScope.method}" name="method" />
	<html:hidden property="orgId" />
	<input type="hidden" value="${isIcuCategory}" name="isIcuCategory" />
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
	<input type="hidden" name="ICU" value="${ifn:cleanHtmlAttribute(param.ICU)}"/>

	<fieldset class="fieldSetBorder">

			<table class="formtable" cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<td class="formlabel">Bed Type:</td>
					<c:choose>
						<c:when test="${param.method=='getEditChargesScreen'}" >
							<td><html:text property="bedtype" styleId="bedtype" readonly="true"/></td>
						</c:when>
						<c:otherwise>
							<td><html:text property="bedtype" styleId="bedtype" onkeypress="return checkAmpersand(event);" onblur="checkDuplicate();"/></td>
						</c:otherwise>
					</c:choose>
					<td class="formlabel">Rates For Rate Sheet:</td>
					<td>
						<c:choose>
						<c:when test="${requestScope.method eq 'addNewBed' }">
							<html:text property="orgName" readonly="true"/>
						</c:when>
						<c:otherwise>
					 		<insta:selectdb name="ratePlan" value="${newbedmasterform.orgId}"
							table="organization_details" valuecol="org_id" orderby="org_name"
							displaycol="org_name"  onchange="changeRatePlan();"
							filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
						</c:otherwise>
						</c:choose>
					</td>
					<td class="formlabel">Insurance Category:</td>
					<td>
						<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel">Status:</td>
					<td>
						<html:select property="status" styleId="status" style="width:7em" styleClass="dropdown">
							<html:option value="A">Active</html:option>
							<html:option value="I">Inactive</html:option>
						</html:select>
					</td>
					<td class="formlabel">Display Order:</td>
					<td>
						<input type="text" name="displayOrder" class="number" size="3" value="${newbedmasterform.displayOrder}" />
					</td>
					<td class="formlabel">Allow Zero Claim Amount:</td>
					<td>
						<insta:selectoptions name="allowZeroClaimAmount" value="${empty newbedmasterform.allowZeroClaimAmount ? 'n' : newbedmasterform.allowZeroClaimAmount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Bill Bed Type:</td>
					<td >
						<insta:selectoptions name="billBedType" opvalues="Y,N" style="width:7em" onchange="return checkUsage(this);"
							optexts="Yes,No" value="${newbedmasterform.billBedType}" styleClass="dropdown" disabled="${newbedmasterform.bedtype eq 'GENERAL'}"/>
					</td>
					<td class="formlabel">Treatment Code Type:</td>
					<td>
						<insta:selectdb name="codeType" table="mrd_supported_codes" valuecol="code_type"
						displaycol="code_type" filtercol="code_category" filtervalue="Treatment" dummyvalue="--Select--"
						value="${codeType}" />
					</td>
					<td class="formlabel">Rate Plan Code :</td>
					<td><html:text property="orgItemCode" maxlength="600" value="${orgItemCode}"/></td>
				</tr>
				<tr>
					<td class="formlabel">Billing Group:</td>
					<td>
						<insta:selectdb  name="billingGroupId" id="billingGroupId"  value="${newbedmasterform.billingGroupId}" table="item_groups" valuecol="item_group_id"
							displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
					</td>
				</tr>
			</table>
		</fieldset>
		<div style="padding:5px 0 5px 0"></div>
		<div class="resultList">
		<fieldset class="fieldSetBorder">
			<table id="icuCharges" class="dataTable">
			<input type="hidden" name="noOfBedTypes" value="${bedTypesCount}" />
				<c:forEach var="entry" items="${output}">
					<c:choose>
						<c:when test="${entry.key eq 'BEDTYPES' }">
							<tr>
								<th>CHARGES</th>
								<c:forEach var="item" items="${entry.value}">
									<th>${ifn:cleanHtml(item)}</th>
									<input type="hidden" name="baseBed" value="${ifn:cleanHtmlAttribute(item)}" />
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'BED CHARGE'}">
							<tr>
								<td style="text-align: right;">Bed Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="bedCharge" id="bedCharge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');"	size="3" class="number validate-decimal"
										onblur="validateDiscount('bedCharge','bedChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
								<input type="hidden" name="ids" value="${i+1}">
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'BED CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Bed Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="bedChargeDiscount" id="bedChargeDiscount${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');"	size="3" class="number validate-decimal"
										onblur="validateDiscount('bedCharge','bedChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'NURSING CHARGE'}">
							<tr>
								<td style="text-align: right;">Nursing Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="nursingCharge" id="nursingCharge${i}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');"	value="${ifn:afmt(item)}" size="3" class="number validate-decimal"
										onblur="validateDiscount('nursingCharge','nursingChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'NURSING CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Nursing Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="nursingChargeDiscount" id="nursingChargeDiscount${i}"
										onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" value="${ifn:afmt(item)}" size="3" class="number validate-decimal"
										onblur="validateDiscount('nursingCharge','nursingChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'DUTY DOCTOR CHARGE'}">
							<tr>
								<td style="text-align: right;">Duty Doctor Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="dutyCharge" id="dutyCharge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');"	size="3" class="number validate-decimal"
										onblur="validateDiscount('dutyCharge','dutyChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'DUTY DOCTOR CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Duty Doctor Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="dutyChargeDiscount" id="dutyChargeDiscount${i}" value="${ifn:afmt(item)}"
										onkeypress="return nextFieldOnTab(event, this, 'icuCharges');"	size="3" class="number validate-decimal"
										onblur="validateDiscount('dutyCharge','dutyChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'PROFESSIONAL CHARGE'}">
							<tr>
								<td style="text-align: right;">Professional Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="profCharge" id="profCharge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('profCharge','profChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'PROFESSIONAL CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Professional Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="profChargeDiscount" id="profChargeDiscount${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('profCharge','profChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'HOURLY CHARGE'}">
							<tr>
								<td style="text-align: right;">Hourly Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="hourlyCharge" id="hourlyCharge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('hourlyCharge','hourlyChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'HOURLY CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Hourly Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="hourlyChargeDiscount" id="hourlyChargeDiscount${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('hourlyCharge','hourlyChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'INTIAL CHARGE'}">
							<tr>
								<td style="text-align: right;">Credit Limit</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="intialCharge" id="intialCharge${i}" value="${ifn:afmt(item)}"
										onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('intialCharge','intialChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key  eq 'LUXARY CHARGE'}">
							<tr>
								<td style="text-align: right;">Luxury Charge(%)</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<td><input type="text" name="luxaryCharge" id="luxaryCharge${k.index}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"></td>
								</c:forEach>
							</tr>
						</c:when>
						<c:when test="${entry.key eq 'DAYCARE SLAB 1 CHARGE'}">
							<tr>
								<td style="text-align: right;">Daycare Min Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="daycareSlab1Charge" id="daycareSlab1Charge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('daycareSlab1Charge','daycareSlab1ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'DAYCARE SLAB 1 CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Daycare Min Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="daycareSlab1ChargeDiscount" id="daycareSlab1ChargeDiscount${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('daycareSlab1Charge','daycareSlab1ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'DAYCARE SLAB 2 CHARGE'}">
							<tr>
								<td style="text-align: right;">Daycare Slab 1 Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="daycareSlab2Charge" id="daycareSlab2Charge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('daycareSlab2Charge','daycareSlab2ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'DAYCARE SLAB 2 CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Daycare Slab 1 Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="daycareSlab2ChargeDiscount" id="daycareSlab2ChargeDiscount${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('daycareSlab2Charge','daycareSlab2ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'DAYCARE SLAB 3 CHARGE'}">
							<tr>
								<td style="text-align: right;">Daycare Slab 2 Charge</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="daycareSlab3Charge" id="daycareSlab3Charge${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('daycareSlab3Charge','daycareSlab3ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>

						<c:when test="${entry.key eq 'DAYCARE SLAB 3 CHARGE DISCOUNT'}">
							<tr>
								<td style="text-align: right;">Daycare Slab 2 Charge Discount</td>
								<c:forEach var="item" items="${entry.value}" varStatus="k">
									<c:set var="i" value="${k.index}"/>
									<td><input type="text" name="daycareSlab3ChargeDiscount" id="daycareSlab3ChargeDiscount${i}" value="${ifn:afmt(item)}"
									onkeypress="return nextFieldOnTab(event, this, 'icuCharges');" size="3" class="number validate-decimal"
										onblur="validateDiscount('daycareSlab3Charge','daycareSlab3ChargeDiscount','${i}')"/>
									</td>
								</c:forEach>
							</tr>
						</c:when>
					</c:choose>
				</c:forEach>
				<c:if test="${not empty output}">
					<tr>
						<td style="text-align: right">Apply Charges To All</td>
						<td><input type="checkbox" name="checkbox" onclick="fillValues('icuCharges', this);"></td>
						<c:forEach begin="2" end="${fn:length (output.BEDTYPES)}">
							<td>&nbsp;</td>
						</c:forEach>
					</tr>
				</c:if>
			</table>
		</fieldset>
		</div>

	<c:if test="${requestScope.method eq 'addNewBed' }">
	<div style="padding:5px 0 5px 0"></div>
		<fieldset class="fieldSetBorder">
			<table class="dataTable"style="float: left" cellspacing="0" cellpadding="0" width="40%">
				<tr>
					<th colspan="4">Apply Charges</th>
				</tr>
				<tr>
					<td class="formlabel">Base BedType(default='GENERAL'):</td>
					<td>
						<select name="baseBeadForCharges" id="baseBeadForCharges" style="width:12em" class="dropdown">
							<option value="">--Base Bed--</option>
							<c:forEach var="item" items="${existingBeds}">
								<option value="${item.map.bed_type}">${item.map.bed_type}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Rate Variance From Base Bed:</td>
					<td><html:select property="variaceType" style="width:9em" styleClass="dropdown">
						<html:option value="Incr">Increase By</html:option>
						<html:option value="Decr">Decrease By</html:option>
					</html:select>&nbsp;
					<input type="text" size="3" name="varianceBy">% or ${currType}:
					<input type="text" size="3" name="varianceValue"></td>
				</tr>
				<tr>
					<td class="formlabel">Round to nearest  ${currType}:</td>
					<td>
						<html:select property="nearsetRoundofValue" style="width:5em" styleClass="dropdown">
							<html:option value="0">None</html:option>
							<html:option value="1">1</html:option>
							<html:option value="5">5</html:option>
							<html:option value="10">10</html:option>
							<html:option value="25">25</html:option>
							<html:option value="50">50</html:option>
							<html:option value="100">100</html:option>
						</html:select>
					</td>
				</tr>
			</table>
			<div style="float: left; valign: top">
				<img class="imgHelpText"
					 src="${cpath}/images/help.png"
					 title="To Add Charges for the new bed for Doctors, Diagnostics, Services, Operations,Operation theare & Equipment, select a Base bed type and define rates variance"/>
			</div>
		</fieldset>
	</c:if>


	<div id="ratePlanDiv" style="display:none">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Rate Plan List</legend>
			<table class="dashBoard" id="ratePlanTbl">
				<tr class="header">
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
<insta:taxations/>
	<table class="screenActions">
		<tr>
			<td><button type="submit" name="Save" accesskey="S"><b><u>S</u></b>ave</button></td>
			<c:if test="${param.method=='getEditChargesScreen'}">
			<td>&nbsp;|&nbsp;</td>
			<td>
				<c:url value="newbedmaster.do" var="addnewbed">
					<c:param name="method" value="getNewScreen"></c:param>
					<c:param name="bedType" value="New" />
				</c:url>
				<a href="${addnewbed}&ICU=N">Add Bed Type</a> &nbsp;|
				<a href="${addnewbed}&ICU=Y">Add ICU Bed Type</a>
			</td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Available BedTypes</a></td>
		</tr>
	</table>

</html:form>
<script>
	var derivedRatePlanDetails = ${derivedRatePlanDetails};
</script>
</body>
</html>
