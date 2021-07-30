<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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
</script>
</head>

<body>
	<h1>Rate Plan Overrides - ${ifn:cleanHtml(orgName)}</h1>

	<html:form action="/pages/masters/insta/admin/newbedmaster.do?"
	onsubmit="return validate()">
	<input type="hidden" value="overrideBedCharges" name="method" />
	<html:hidden property="orgId" />
	<html:hidden property="bedtype" value="${bedType}"/>
	<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
	<input type="hidden" name="fromItemMaster" value="${ifn:cleanHtmlAttribute(fromItemMaster)}"/>
	<input type="hidden" name="baseRateSheet" value="${ifn:cleanHtmlAttribute(baseRateSheet)}"/>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Bed Type Details</legend>

			<table class="formtable" cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<td class="formlabel">Bed Type:</td>
					<td class="forminfo">${ifn:cleanHtml(bedType)}</td>
					<td class="formlabel">Treatment Code Type:</td>
					<td>
						<insta:selectdb name="codeType" table="mrd_supported_codes" valuecol="code_type"
						displaycol="code_type" filtercol="code_category" filtervalue="Treatment" dummyvalue="--Select--"
						value="${codeType}" />
					</td>
					<td class="formlabel">Rate Plan Code :</td>
					<td><html:text property="orgItemCode" maxlength="600" value="${orgItemCode}"/></td>
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
								<td style="text-align: right;">Initial Payment</td>
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
		<table class="screenActions">
			<tr>
				<td>
					<button type="submit" name="Save" accesskey="U"><b><u>S</u></b>ave</button>


					<c:choose>
						<c:when test="${fromItemMaster eq 'false'}">
							| <a href="${cpath}/pages/masters/insta/admin/newbedmaster.do?method=getChargesList&orgId=${ifn:cleanURL(orgId)}">Bed Charges List</a>
						</c:when>
						<c:otherwise>
							<c:set var="url" value="${cpath}/pages/masters/insta/admin/newbedmaster.do?method=getEditChargesScreen"/>
							| <a href="<c:out value='${url}&bedType=${ifn:cleanURL(bedType)}&orgId=${ifn:cleanURL(baseRateSheet)}'/>">Bed Charges</a>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>
		</table>
</html:form>
</body>
</html>
