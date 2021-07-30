<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<c:set var="bill" value="${billDetails.bill}"/>
<c:set var="charges" value="${billDetails.charges}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<jsp:useBean id="claimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${claimStatusDisplay}" property="O" value="Open"/>
<c:set target="${claimStatusDisplay}" property="B" value="Batched"/>
<c:set target="${claimStatusDisplay}" property="M" value="For Resub."/>
<c:set target="${claimStatusDisplay}" property="C" value="Closed"/>


<c:set var="existingReceipts" value="${bill.totalReceipts}"/>
<c:set var="existingRecdAmount" value="${bill.claimRecdAmount}"/>
<c:set var="existingSponsorReceipts" value="${bill.totalPrimarySponsorReceipts + bill.totalSecondarySponsorReceipts}"/>
<c:set var="billDeposits" value="${bill.depositSetOff}"/>

<c:set var="isInsuranceBill" value="${bill.is_tpa}"/>
<c:set var="hasDynaPackage" value="${bill.dynaPkgId != 0}"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title>${ifn:cleanHtml(bill.billNo)} <insta:ltext key="billing.editpharmacyitemamounts.items.pharmacyitemamounts"/></title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="editPharmacyItemAmount.js"/>

<script type="text/javascript">
	var isTpa	= ${empty bill ? false : bill.is_tpa};
	var origBillStatus = '${ifn:cleanJavaScript(bill.status)}';
	var billType = '${ifn:cleanJavaScript(bill.billType)}';
	var restrictionType = '${ifn:cleanJavaScript(bill.restrictionType)}';
	var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
	var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
	var existingSponsorReceipts = ${empty existingSponsorReceipts ? 0 : existingSponsorReceipts};
	var billDeposits = ${empty billDeposits ? 0 : billDeposits};
	var roleId = '${roleId}';
	var drugCodeTypes = ${pharmaCodeTypesJSON};
	var drugCodesJson = ${pharmaCodesJSON};
	var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}');
</script>
<style>
	select.filterActive { color: blue; }
</style>
<insta:js-bundle prefix="billing.pharmacyamount"/>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="initForm();">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<form name="billForm" action="EditPharmacyItemAmount.do">
	<input type="hidden" name="_method" value="editPharmacyItemAmount">
	<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(isNewUX)}">
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="billing.editpharmacyitemamounts.items.editpharmacyitemamount"/></h1></td>
			<td><insta:ltext key="billing.editpharmacyitemamounts.items.bill"/>&nbsp;<insta:ltext key="billing.editpharmacyitemamounts.items.no"/>:&nbsp;</td>
			<td><input type="text" name="billNo" id="billNo" style="width: 80px"></td>
			<td><input type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<insta:feedback-panel/>

<div class="helpPanel">
<table>
	<tr>
		<td valign="top" style="width: 30px">
			<img src="${cpath}/images/information.png"/>
		</td>
		<td style="padding-bottom: 5px">
			<insta:ltext key="billing.editpharmacyitemamounts.common.info"/>
		</td>
	</tr>
</table>
</div>

<insta:patientdetails visitid="${patient.patient_id}"/>

<form name="mainform" method="post" action="EditPharmacyItemAmount.do">
<input type="hidden" name="_method" value="updatePharmacyItemAmount">
<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(isNewUX)}">
<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(bill.billNo)}">
<c:set var="hasMoreThanOneClaim" value="${isInsuranceBill && patient.secondary_sponsor_id != null && secondary_sponsor_id != ''}"/>

<%-- bill type display --%>
<c:set var="billTypeDisplay">
	<c:choose>
		<c:when test="${bill.billType == 'P' && bill.restrictionType == 'P'}"><insta:ltext key="billing.editpharmacyitemamounts.items.billnow_ph"/></c:when>
		<c:when test="${bill.billType == 'C' && bill.restrictionType == 'P'}"><insta:ltext key="billing.editpharmacyitemamounts.items.billlater_ph"/></c:when>
		<c:when test="${bill.billType == 'P'}"><insta:ltext key="billing.editpharmacyitemamounts.items.billnow"/></c:when>
		<c:when test="${bill.billType == 'C'}"><insta:ltext key="billing.editpharmacyitemamounts.items.billlater"/></c:when>
		<c:otherwise><insta:ltext key="billing.editpharmacyitemamounts.items.other"/></c:otherwise>
	</c:choose>
</c:set>

<c:if test="${bill.billType == 'C' && bill.isPrimaryBill == 'N'}">
	<c:set var="billTypeDisplay" value="Sec. ${billTypeDisplay}"/>
</c:if>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"><insta:ltext key="billing.editpharmacyitemamounts.items.billdetails"/></legend>
	<table class="formtable" width="100%">
		<%-- Information row: Bill no (type), Open date (by), Finalized Date --%>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.billno.type.in.brackets"/>:</td>
			<td class="forminfo">${ifn:cleanHtml(bill.billNo)} (${ifn:cleanHtml(billTypeDisplay)})
			</td>

			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.opendate.by.in.brackets"/>:</td>
			<td class="forminfo"> <fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/> (${ifn:cleanHtml(bill.openedBy)}) </td>

			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.finalizeddate"/>:</td>
			<td>
				<fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/>
				<fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.billstatus"/>:</td>
			<td class="forminfo"> <b>${statusDisplay[bill.status]}</b> </td>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.paymentstatus"/>:</td>
			<td class="forminfo"> <b>${paymentStatusDisplay[bill.paymentStatus]}</b> </td>
			<c:if test="${hasDynaPackage}">
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.packagename"/>:</td>
				<td class="forminfo"> <b>${ifn:cleanHtml(bill.dynaPkgName)}</b> </td>
			</c:if>
		</tr>
		<c:if test="${isInsuranceBill}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.patientdeduction"/>:</td>
				<td class="forminfo"> <b>${bill.insuranceDeduction}</b> </td>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.approvalamount"/>:</td>
				<td class="forminfo"> <b>${bill.primaryApprovalAmount}</b> </td>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.claimstatus"/>:</td>
				<td class="forminfo"> <b>${claimStatusDisplay[bill.primaryClaimStatus]}</b> </td>
			</tr>
		</c:if>
	</table>
</fieldset>

<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
	<tr>
		<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filter"/>&nbsp;<insta:ltext key="billing.editpharmacyitemamounts.items.category"/>: </td>
		<td>
			<select class="dropdown" name="filterItemCategory" onchange="onPharmacyFilterChange(this);">
				<option value=""><insta:ltext key="billing.editpharmacyitemamounts.items.all.in.brackets"/></option>
				<c:forEach items="${itemCategories}" var="itemcategory">
					<option value="${itemcategory}">${itemcategory}</option>
				</c:forEach>
			</select>
		</td>
		<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filter"/>&nbsp;<insta:ltext key="billing.editpharmacyitemamounts.items.item"/>&nbsp;<insta:ltext key="billing.editpharmacyitemamounts.items.name"/>:</td>
		<td>
			<select class="dropdown" name="filterItemName" onchange="onPharmacyFilterChange(this);">
				<option value=""><insta:ltext key="billing.editpharmacyitemamounts.items.all.in.brackets"/></option>
				<c:forEach items="${itemNames}" var="itemname">
					<option value="${ifn:cleanHtmlAttribute(itemname)}">${ifn:cleanHtml(itemname)}</option>
				</c:forEach>
			</select>
		</td>
		<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filter"/>&nbsp;<insta:ltext key="billing.editpharmacyitemamounts.items.sale"/>&nbsp;<insta:ltext key="billing.editpharmacyitemamounts.items.bill"/>:</td>
		<td>
			<select class="dropdown" name="filterItemBill" onchange="onPharmacyFilterChange(this);">
				<option value=""><insta:ltext key="billing.editpharmacyitemamounts.items.all.in.brackets"/></option>
				<c:forEach items="${itemBills}" var="itembill">
					<option value="${itembill}">${itembill}</option>
				</c:forEach>
			</select>
		</td>
	</tr>
</table>

<c:set var="totalAmount"  value="0"/>
<c:set var="totalTax"  value="0"/>
<c:set var="totalPatientAmount"   value="0"/>
<c:set var="totalPatientTax"   value="0"/>
<c:set var="totalClaimAmount" value="0"/>
<c:set var="totalClaimTax" value="0"/>
<c:set var="totalNetClaimAmount" value="0"/>
<c:set var="totalNetClaimTax" value="0"/>
<c:set var="i" value="0"/>

<fieldset class="fieldSetBorder">
<legend class="fieldSetLabel"><insta:ltext key="billing.editpharmacyitemamounts.items.pharmacysaleitems"/></legend>
<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%">
		<tr>
			<c:if test="${isInsuranceBill || hasDynaPackage}">
				<th style="width: 20px"><input type="checkbox" onclick="selectAllItems()" id="allItems"/></th>
			</c:if>
			<th style="width: 30px"><insta:ltext key="billing.editpharmacyitemamounts.items.posteddate"/></th>
			<th style="width: 15px"><insta:ltext key="billing.editpharmacyitemamounts.items.salebill"/></th>
			<th style="width: 40px"><insta:ltext key="billing.editpharmacyitemamounts.items.itemname"/></th>
			<th style="width: 20px"><insta:ltext key="billing.editpharmacyitemamounts.items.category"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.code"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.batch"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.expiry"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.mfr"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.pkgsize"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.unitrate"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.pkgrate"/></th>
			<th style="width: 10px"><insta:ltext key="billing.editpharmacyitemamounts.items.qty"/></th>
			<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.amount"/></th>
			<th style="width: 20px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.tax"/></th>
			<c:if test="${isInsuranceBill}">
				<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.patientamt"/></th>
				<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.patienttax"/></th>
				<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.pri.claim"/></th>
				<th style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.pri.claim.tax"/></th>
				<th id="sec_plan_grid_header" style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.sec.claim"/></th>
				<th id="sec_plan_grid_header_tax" style="width: 30px;text-align: right;"><insta:ltext key="billing.editpharmacyitemamounts.items.sec.claim.tax"/></th>
			</c:if>
			<th style="width: 10px;text-align: right;"></th>
		</tr>
		<c:forEach items="${saleItems}" var="item">
			<tr>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${item.charge_excluded == 'Y'}"><insta:ltext key="billing.editpharmacyitemamounts.items.blue"/></c:when>
						<c:when test="${item.charge_excluded == 'P'}"><insta:ltext key="billing.editpharmacyitemamounts.items.blue"/></c:when>
						<c:otherwise><insta:ltext key="billing.editpharmacyitemamounts.items.empty"/></c:otherwise>
					</c:choose>
				</c:set>

				<c:if test="${isInsuranceBill || hasDynaPackage}">
					<td> <input type="checkbox" name="itemCheck" value="${item.sale_item_id}"/>	</td>
				</c:if>
				<td>
					<img src="${cpath}/images/${flagColor}_flag.gif"/>
					<fmt:formatDate value="${item.sale_date}" pattern="dd-MM-yyyy"/>
				</td>
				<td>
					<label>${item.sale_id}</label>
					<input type="hidden" name="itemCategory" value="${item.category}" />
					<input type="hidden" name="itemName" value="${item.medicine_name}" />
					<input type="hidden" name="itemBill" value="${item.sale_id}" />
					<input type="hidden" name="edited" value="false" />

					<input type="hidden" name="itemCode" value="${item.item_code}" />
					<input type="hidden" name="itemCodeType" value="${item.code_type}" />
					<input type="hidden" name="itemQty" value="${item.issue_quantity + item.return_qty}" />
					<input type="hidden" name="itemAmount" value="${item.net_amount + item.return_amt}" />
					<input type="hidden" name="itemTax" value="${item.tax + item.return_tax_amt}" />
					
					<input type="hidden" name="patientAmt" value="${(item.net_amount + item.return_amt) - (item.insurance_claim_amt + item.return_insurance_claim_amt + item.tax + item.return_tax_amt)}" />
					<input type="hidden" name="insClaimAmt" value="${item.insurance_claim_amt + item.return_insurance_claim_amt}" />
					<input type="hidden" name="saleItemId" value="${item.sale_item_id}" />
					<input type="hidden" name="medicineId" value="${item.medicine_id}" />
					<input type="hidden" name="chargeExcluded" value="${item.charge_excluded}"/>
					<input type="hidden" name="packageFinalized" value="${item.package_finalized}"/>

					<input type="hidden" name="amountIncluded" value="${item.amount_included}"/>
					<input type="hidden" name="qtyIncluded" value="${item.qty_included}"/>
				</td>
				<td> <div style="width:100px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${item.medicine_name}">
					${item.medicine_name} </div>
				</td>
				<td> <div style="width:60px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${item.category}">
					${item.category} </div>
				</td>
				<td> ${item.item_code} </td>
				<td> ${item.batch_no} </td>
				<td> ${item.exp_dt_str} </td>
				<td> ${item.manf_mnemonic} </td>
				<td> ${item.package_unit} </td>
				<td> <fmt:formatNumber type="number" groupingUsed="false"  minFractionDigits="${after_decimal_digits}" maxFractionDigits="${after_decimal_digits}" value="${item.unit_rate_wrf}" /> </td>
				<td> ${item.pkg_rate} </td>
				<td> ${item.issue_quantity + item.return_qty} </td>
				<td class="number"><label>${item.net_amount + item.return_amt}</label></td>
				<td class="number"><label>${item.tax + item.return_tax_amt}</label></td>
				<c:if test="${isInsuranceBill}">
				<c:set var="saleClaims" value="${sales_claim_details[item.sale_item_id]}"/>
				<c:set var="numsalesClaims" value="${fn:length(sales_claim_details[item.sale_item_id])}"/>
				<c:set var="claimAmt" value="0"/>
				<c:set var="sponsorTaxAmt" value="0"/>
				<c:forEach begin="1" end="${numsalesClaims}" var="c" varStatus="loop">
						<c:set var="salesClaims_cal" value="${saleClaims[c-1]}"/>
						<c:set var="claimAmt" value="${claimAmt+salesClaims_cal.insurance_claim_amt + salesClaims_cal.return_insurance_claim_amt + salesClaims_cal.tax_amt}"/>
						<c:set var="sponsorTaxAmt" value="${sponsorTaxAmt + salesClaims_cal.tax_amt}"/>
				</c:forEach>
				<c:if test="${numsalesClaims == 0}">
					<c:set var="claimAmt" value="${claimAmt+item.insurance_claim_amt + item.return_insurance_claim_amt}"/>
				</c:if>

					<td class="number"><label>${(item.net_amount + item.return_amt) - claimAmt}</label></td>
					<td class="number"><label>${item.tax + item.return_tax_amt - sponsorTaxAmt}</label></td>
					<!-- display claim amounts of all sponsors -->
					<c:forEach begin="1" end="${numsalesClaims}" var="j" varStatus="loop">
						<c:set var="salesClaims" value="${saleClaims[j-1]}"/>
						<td class="number"><label>${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt + salesClaims.tax_amt}</label></td>
						<td class="number"><label>${salesClaims.tax_amt}</label></td>
						<c:set var="totalClaimTax" value="${totalClaimTax + salesClaims.tax_amt}"/>
						<c:if test="${j == 1}">
							<input type="hidden" name="pri_insClaimAmt" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
							<input type="hidden" name="pri_insClaimTax" value="${salesClaims.tax_amt}" />
							<input type="hidden" name="orig_pri_insClaimAmt" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
							<input type="hidden" name="pri_itemPreAuthId" value="${salesClaims.prior_auth_id}" />
							<input type="hidden" name="pri_itemPreAuthMode" value="${salesClaims.prior_auth_mode_id}" />
							<input type="hidden" name="pri_include_in_claim_calc" value="${salesClaims.include_in_claim_calc}" />
							<input type="hidden" name="priClaimId" value="${salesClaims_cal.claim_id}" />							
						</c:if>

						<c:if test="${j != 1}">
							<c:set var="hasMoreThanOneClaim" value="${j != 1}"/>
							<input type="hidden" name="sec_insClaimAmt" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
							<input type="hidden" name="sec_insClaimTax" value="${salesClaims.tax_amt}" />
							<input type="hidden" name="orig_sec_insClaimAmt" value="${salesClaims.insurance_claim_amt + salesClaims.return_insurance_claim_amt}" />
							<input type="hidden" name="sec_itemPreAuthId" value="${salesClaims.prior_auth_id}" />
							<input type="hidden" name="sec_itemPreAuthMode" value="${salesClaims.prior_auth_mode_id}" />
							<input type="hidden" name="sec_include_in_claim_calc" value="${salesClaims.include_in_claim_calc}" />
							<input type="hidden" name="secClaimId" value="${salesClaims_cal.claim_id}" />
						</c:if>
					</c:forEach>
					<c:if test="${numsalesClaims == 0}">
						<input type="hidden" name="pri_insClaimAmt" value="${item.insurance_claim_amt + item.return_insurance_claim_amt}" />
						<input type="hidden" name="pri_insClaimTax" value="${salesClaims.tax_amt}" />
						<input type="hidden" name="orig_pri_insClaimAmt" value="${item.insurance_claim_amt + item.return_insurance_claim_amt}" />
						<input type="hidden" name="pri_itemPreAuthId" value="${item.prior_auth_id}" />
						<input type="hidden" name="pri_itemPreAuthMode" value="${item.prior_auth_mode_id}" />

						<td class="number"><label>${item.insurance_claim_amt + item.return_insurance_claim_amt}</label></td>
					</c:if>
				</c:if>
				<td class="number">
					<a href="javascript:Edit" title="Edit Claim Details">
					<img src="${cpath}/icons/Edit.png" class="button" onclick="return showEditItemDialog(this);" />
					</a>
				</td>
				<c:set var="totalAmount" value="${totalAmount + (item.net_amount + item.return_amt)}"/>
				<c:set var="totalTax" value="${totalTax + (item.tax + item.return_tax_amt)}"/>
				<c:if test="${isInsuranceBill}">
					<c:set var="totalPatientAmount"  value="${totalPatientAmount + (item.net_amount + item.return_amt) - (item.insurance_claim_amt + item.return_insurance_claim_amt)}"/>
					<c:set var ="totalPatientTax" value ="${totalPatientTax + (item.tax + item.return_tax_amt - sponsorTaxAmt)}"/>
					<c:set var="totalClaimAmount" value="${totalClaimAmount + (item.insurance_claim_amt + item.return_insurance_claim_amt)}"/>
				</c:if>
			</tr>
		</c:forEach>
	</table>
	<c:if test="${isInsuranceBill || hasDynaPackage}">
		<table class="addButton">
			<tr>
				<td>
					<table class="footerTable">
						<tr>
							<c:if test="${isInsuranceBill && !hasMoreThanOneClaim}">
								<td>
									<button name="btnClaimable" id="btnClaimable" title='<insta:ltext key="billing.editpharmacyitemamounts.items.theselecteditemsclaimamountisaddedtoclaim"/>'
										onclick="addItemClaimAmount(); return false;" ${bill.status != 'A' ? 'disabled':''} > <insta:ltext key="billing.editpharmacyitemamounts.items.claim"/> </button>
								</td>
								<td>
									<button name="btnNonClaimable" id="btnNonClaimable" title='<insta:ltext key="billing.editpharmacyitemamounts.items.theselecteditemsclaimamountisremovedfromclaim"/>'
										onclick="removeItemClaimAmount(); return false;" ${bill.status != 'A' ? 'disabled':''}> <insta:ltext key="billing.editpharmacyitemamounts.items.notclaim"/> </button>
								</td>
								<c:if test="${hasDynaPackage}">
									<td>|</td>
								</c:if>
							</c:if>
							<c:if test="${hasDynaPackage}">
								<td>
									<button name="btnIncludePkg" id="btnIncludePkg" title='<insta:ltext key="billing.editpharmacyitemamounts.items.includetheselecteditemsintopackage"/>'
										onclick="includeIntoDynaPkg(); return false;" ${bill.status != 'A' ? 'disabled':''} ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}> <insta:ltext key="billing.editpharmacyitemamounts.items.includeinpkg"/></button>
								</td>
								<td>
									<button name="btnExcludePkg" id="btnExcludePkg" title='<insta:ltext key="billing.editpharmacyitemamounts.items.excludetheselecteditemsfrompackage"/>'
										onclick="excludeFromDynaPkg(); return false;" ${bill.status != 'A' ? 'disabled':''} ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}> <insta:ltext key="billing.editpharmacyitemamounts.items.excludefrompkg"/></button>
								</td>
							</c:if>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</c:if>
</div>
</fieldset>

<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totals"/></legend>
	<table width="900" align="left" class="infotable">
		<tr style="display:none" id ="filterRow">
			<c:choose>
				<c:when test="${isInsuranceBill}">
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredpatientamt"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblFilteredPatientAmt">0.00</label>
					</td>

					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredclaimamt"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblFilteredClaimAmt">0.00</label>
					</td>
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredclaimnetamt"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblFilteredNetClaimAmt">0.00</label>
					</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredamt"/>:</td>
			<td class="forminfo" align="right">
				<label id="lblFilteredAmount">0.00</label>
			</td>
   	</tr>
   	<tr style="display:none" id ="filterTaxRow">
			<c:choose>
				<c:when test="${isInsuranceBill}">
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredpatienttax"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblFilteredPatientTax">0.00</label>
					</td>

					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredclaimtax"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblFilteredClaimTax">0.00</label>
					</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.filteredtax"/>:</td>
			<td class="forminfo" align="right">
				<label id="lblFilteredTax">0.00</label>
			</td>
   	</tr>
  		<tr>
			<c:choose>
				<c:when test="${isInsuranceBill}">
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totalpatientamt"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblTotalPatientAmt">${ifn:afmt(totalPatientAmount - totalTax)}</label>
					</td>
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totalclaimamt"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblTotalClaimAmt">${ifn:afmt(totalClaimAmount)}</label>
					</td>
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totalclaimnetamt"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblTotalNetClaimAmt">${ifn:afmt(totalPatientAmount + totalClaimAmount)}</label>
					</td>
				</c:when>
				<c:otherwise>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel" style="width: 100"><insta:ltext key="billing.editpharmacyitemamounts.items.totalamt"/>:</td>
			<td class="forminfo" align="right" style="width: 100">
				<label id="lblTotalAmount">${ifn:afmt(totalAmount-totalTax)}</label>
			</td>
	   </tr>
	   <tr>
	   		<c:choose>
				<c:when test="${isInsuranceBill}">
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totalpatienttax"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblTotalPatientTax">${ifn:afmt(totalPatientTax)}</label>
					</td>
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totalclaimtax"/>:</td>
					<td class="forminfo" align="right">
						<label id="lblTotalClaimTax">${ifn:afmt(totalClaimTax)}</label>
					</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
				</c:when>
				<c:otherwise>
	   				<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo" align="right">&nbsp;</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.totaltax"/>:</td>
			<td class="forminfo" align="right">
				<label id="lblTotalTax">${ifn:afmt(totalTax)}</label>
			</td>
   		</tr>
   		<tr>
	   		<td class="formlabel">&nbsp;</td>
			<td class="forminfo" align="right">&nbsp;</td>
			<td class="formlabel">&nbsp;</td>
			<td class="forminfo" align="right">&nbsp;</td>
			<td class="formlabel">&nbsp;</td>
			<td class="forminfo" align="right">&nbsp;</td>
			<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.pkgincludedamt"/>:</td>
			<td class="forminfo" align="right">
				<label id="lblPkgIncludedAmount">0.00</label>
			</td>
		</tr>
	 </table>
</fieldset>

<table class="screenActions">
	<tr>
		<td>
			<c:set var="disabled" value=""/>
			<c:if test="${bill.status != 'A'}"> <c:set var="disabled"> disabled = "disabled" </c:set> </c:if>
			<button onclick="return updatePharmacyItemAmounts()" ${disabled} accesskey="S"><b><u><insta:ltext key="billing.editpharmacyitemamounts.items.s"/></u></b><insta:ltext key="billing.editpharmacyitemamounts.items.ave"/></button>
		</td>
		<td>
			<c:choose>
				<c:when test="${isNewUX == 'Y'}">
					<c:choose>
						<c:when test="${patient.visit_type == 'o'}">
							<c:set var="flow_type" value="opflow" />
							<c:set var="new_ux_screen_id" value="new_op_bill" />
						</c:when>
						<c:otherwise >
							<c:set var="flow_type" value="ipflow" />
							<c:set var="new_ux_screen_id" value="new_ip_bill" />
						</c:otherwise>
					</c:choose>
					<c:set var="billScreenLink" value="/index.htm#/filter/default/patient/${patient.mr_no}/billing/billNo/${bill.billNo}?retain_route_params=true"/>
					<insta:screenlink screenId="${new_ux_screen_id}" addPipe="true" label="View/Edit Bill: ${bill.billNo}"
									  extraParam="${billScreenLink}"
									  title="View/Edit Bill."/>
				</c:when>
				<c:otherwise>
					<insta:screenlink screenId="credit_bill_collection" addPipe="true" label="View/Edit Bill: ${bill.billNo}"
									  extraParam="?_method=getCreditBillingCollectScreen&billNo=${bill.billNo}"
									  title="View/Edit Bill."/>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
</table>
</form>

<form name="phitemamtform">
<input type="hidden" id="phItemAmtRowId" value=""/>

<div id="phItemAmtDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.editpharmacyitemamounts.items.edititemamounts"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.codetype"/>:</td>
				<td class="forminfo">
					<select name="code_type" id="code_type" class="dropDown">
						<option></option>
					</select>
				</td>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.code"/>:</td>
				<td class="forminfo">
					<div id="drugCodesAutoComplete" class="autoComplete">
						<input type="text" name="code_name" id="code_name" class="field" maxlength="40" />
						<div id="drugCodesContainer" style="width:240px;"></div>
					</div>
					<input type="hidden" name="code" id="code"/>
					<input type="hidden"  id="_currentId" class="numeric" />
					<input type="hidden"  id="_medicineId" class="numeric" />
					<input type="hidden" id="_priInsClaimTaxAmt" class="numeric"/>
					<input type="hidden" id="_secInsClaimTaxAmt" class="numeric"/>
				</td>
			</tr>
			<c:if test="${isInsuranceBill}">
			<tr id="pri_priauth_row">
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.pripriorauthno"/>:</td>
				<td class="forminfo">
					<input type="text" name="pre_auth_no" id="pre_auth_no" class="numeric" /></td>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.sec.priorauthmode"/>:</td>
				<td class="forminfo">
					<insta:selectdb name="pre_auth_mode" id="pre_auth_mode"
						table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
						filtered="false" dummyvalue="${dummyvalue}"/>
				</td>
			</tr>
			<tr id="pri_claim_row">
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.pri.claimamtwot"/>:</td>
				<td class="forminfo"> 
					<input type="text" name="item_claim_amount" id="item_claim_amount" class="numeric" onchange="includePriFlagCal(this)" onkeypress="return enterNumAndDotAndMinus(event);"/> 
					<input type="hidden" name="_priClaimId" id="_priClaimId" />
					<input type="hidden" name="pri_include_in_claim_calc_form" id="pri_include_in_claim_calc_form" value="" />
				</td>
			</tr>
			</c:if>
			<c:if test="${hasMoreThanOneClaim}">
				<tr id="sec_priauth_row">
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.sec.priorauthno"/>:</td>
					<td class="forminfo">
						<input type="text" name="pre_auth_no" id="pre_auth_no" class="numeric" />
						<input type="hidden" name="sec_plan" value="true"/></td>
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.sec.priorauthmode"/>:</td>
					<td class="forminfo">
						<insta:selectdb name="pre_auth_mode" id="pre_auth_mode"
							table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name"
							filtered="false" dummyvalue="${dummyvalue}"/>
					</td>
				</tr>
				<tr id="sec_claim_row">
					<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.sec.claimamt"/>:</td>
					<td class="forminfo"> 
						<input type="text" name="item_claim_amount" id="item_claim_amount" class="numeric" onchange="includeSecFlagCal(this)"/>
						<input type="hidden" name="_secClaimId" id="_secClaimId" />
						<input type="hidden" name="sec_include_in_claim_calc_form" id="sec_include_in_claim_calc_form" value="" />
					</td>
				</tr>
			</c:if>
			<tr>
				<td  class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.qty"/>:</td>
				<td  class="forminfo"> <label id="item_qty"></label> </td>
				<td  class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.amount"/>:</td>
				<td  class="forminfo"> <label id="item_amount"></label> </td>
			</tr>
			<c:if test="${isInsuranceBill}">
			<tr>
				<td  class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.patientamtwot"/>:</td>
				<td  class="forminfo"> <label id="item_pat_amount"></label> </td>
			</tr>
			</c:if>
			<c:if test="${hasDynaPackage}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.qtyincluded"/>:</td>
				<td>
					<input type="text" name="item_qty_included" onchange="recalcIncludedAmount();" ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}/>
				</td>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.amtincluded"/>:</td>
				<td>
					<input type="text" name="item_amount_included" onchange="recalcIncludedAmount();" ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.editpharmacyitemamounts.items.pkgfinalized"/>:</td>
				<td>
					<input type="checkbox" name="item_pkg_finalized" ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}/>
				</td>
			</tr>
			</c:if>
		</table>
	</fieldset>
	<table>
		<tr>
			<td><input type="button" onclick="onEditItemAmountSubmit()" value="OK" /></td>
			<td><input type="button" onclick="onEditItemAmountCancel()" value="Cancel"/></td>
			<td><input type="button" onclick="showPreviousEditItemDialog()" value="<<Prev"/></td>
			<td><input type="button" onclick="showNextEditItemDialog()" value="Next>>"/></td>
		</tr>
	</table>
</div>
</div>
</form>

<div class="legend">
	<c:if test="${hasDynaPackage}">
		<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
		<div class="flagText"><insta:ltext key="billing.editpharmacyitemamounts.items.excludedfrompkg"/></div>
	</c:if>
</div>

</body>

