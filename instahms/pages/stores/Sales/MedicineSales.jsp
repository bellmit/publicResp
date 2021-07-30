<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ page import="com.insta.hms.pbmauthorization.PBMApprovalsDAO" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<c:choose><c:when test="${sale_return == true}">
	<c:set var="includeZeros" value="Y"/>
</c:when><c:otherwise>
	<c:set var="includeZeros" value="${prefs.stock_negative_sale != 'D' ? 'Y' : 'N'}"/>
</c:otherwise></c:choose>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>

<c:set var="points_redemption_rate" value="${not empty prefs.points_redemption_rate ? prefs.points_redemption_rate : 0}"/>
<c:set var="redemption_cap_percent" value="${redemption_cap_percent}"/>
<c:set var="genPrefs" value="<%= GenericPreferencesDAO.getGenericPreferences() %>" />
<c:set var="blockIp" value="${genPrefs.restrictInactiveIpVisit}" />
<c:set var="pbmPriceThreshold" value="${genPrefs.pbmPriceThreshold}" />
<c:set var="no_of_credit_debit_card_digits" value="${genPrefs.no_of_credit_debit_card_digits}"/>
<c:set var="incomeTaxCashLimitApplicability" value="${genPrefs.incomeTaxCashLimitApplicability}"/>
<c:set var="cashTransactionLimit" value="${cashTransactionLimit}"/>
<c:set var="title2">
	<insta:ltext key="patientdetails.common.tag.title2"/>
</c:set>
<c:set var="newLineChar" value="\n"/>

<html>
<head>
	<title><insta:ltext key="salesissues.sales.details.sales"/><c:if test="${sale_return}"> <insta:ltext key="salesissues.sales.details.returns"/></c:if><c:if test="${transaction eq 'estimate'}"> <insta:ltext key="salesissues.sales.details.estimate"/> </c:if> - <insta:ltext key="salesissues.sales.details.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="i18nSupport" content="true"/>
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="instaautocomplete.js" />
	<insta:link type="js" file="stores/storeMedicinesAjax.js" />
	<insta:link type="js" file="stores/sales.js" />
	<insta:link type="js" file="Insurance/insuranceCalculation.js" />
	<insta:link type="js" file="stores/loginDialog.js" />
	<insta:link type="js" file="billPaymentCommon.js" />
	<insta:link type="js" file="instadate.js" />
	<insta:link type="js" file="stores/storeshelper.js"/>
	<insta:link type="script" file="sockjs.min.js"/>
	<insta:link type="script" file="stomp.min.js"/>
	<insta:link type="js" file="phoneNumberUtil.js"/>
	<insta:link type="js" file="stores/sales_discount_rule.js"/>

	<%-- expiry affects medicine cache --%>
	<c:set var="prefShowHospAmts" value="${prefs.show_hosp_amts_pharmasales}"/>
	<c:set var="prefVat" value="${prefs.vat_applicable}"/>
	<c:set var="prefbarcode" value="${prefs.barcode_for_item}"/>
	<c:set var="memberIdLabel" value="${regPref.member_id_label}"/>
	<c:set var="salesStore" value="${param.indentStore == null ? pharmacyStoreId : param.indentStore}"/>
	<c:choose>
		<c:when test="${not empty pbm_store_id}">
			<script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${stock_ts}&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=${includeZeros}&retailable=Y&billable=Y&issueType=CR&storeId=${pbm_store_id}&mts=${master_timestamp}">
			</script>
		</c:when>
		<c:when test="${not empty pharmacyStoreId}">
			<script src="${cpath}/pages/stores/getMedicinesInStock.do?ts=${stock_ts}&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=${includeZeros}&retailable=Y&billable=Y&issueType=CR&storeId=${salesStore}&mts=${master_timestamp}">
			</script>
		</c:when>
	</c:choose>

	<style>
		td.return {color: red;}
		td.estimate {color: blue;}
		<c:if test="${sale_return}">
			.yui-ac-content li { color: red; }
		</c:if>
		<c:if test="${transaction == 'estimate'}">
			.yui-ac-content li { color: blue; }
		</c:if>
		#psContainer .yui-ac-content {
			max-height:30em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			width: 15em;
			padding-bottom: 2em;
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}

		table.infotable td.forminfo { width: 50px; text-align: right }
		table.infotable td.formlabel { width: 95px; text-align: right }
		tr#billDetailsInner td, tr#billDetailsInnerHosp td { height: auto; }
	</style>

	
	<script>
		var gRoleId = '${ifn:cleanJavaScript(roleId)}';
		var gIsReturn = ${sale_return == 'true'};
		var gRefundRights = '${actionRightsMap.allow_refund}';
		var gStockNegative = '${prefs.stock_negative_sale}';
		var gAllowExpiredSale = '${prefs.sale_expiry}';
		var gExpiryWarnDays = '${prefs.warn_expiry}';
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var gUserCounterId = '${pharmacyCounterId}';
		var gUserStoreId = '${pharmacyStoreId}';
		var gUserCounterName = '${pharmacyCounterName}';
		var hdrugAlertNeeded = '${prefs.pharmacy_schedule_h_alert}';
		var seperatePharmacyCreditBill = '${prefs.seperate_pharmacy_credit_bill}';
		var gTransaction = '${transaction}';
		var modEclaimErx = '${mod_eclaim_erx}';
		var gStoreAccess = '${multiStoreAccess}';
		var saleMargin = '${prefs.pharmacy_sale_margin_in_per}';
		var jStores = <%= request.getAttribute("storesJSON") %>;
		var gIsEstimate = ${transaction eq 'estimate'};
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var allowRaiseBill = '${allowed_raise_bill}';
		var counter = '${empty counter_id? 'N':'Y'}';
		var isSalesStore = '${is_sales_store}';
		var points_redemption_rate = ${empty points_redemption_rate ? 0 : points_redemption_rate};
		var redemption_cap_percent = ${empty redemption_cap_percent ? 0 : redemption_cap_percent};
		var medDosages = ${medDosages};
		var presInstructions = <%= request.getAttribute("presInstructions") %>;

		var gRetailCreditSaleRights= '${actionRightsMap.allow_retail_credit_sales}';
		var gRetailSaleRights= '${actionRightsMap.allow_retail_sales}';
		var prescvisit_id = '${ifn:cleanJavaScript(param.visit_id)}';
		var patientPbmPrescId = '${ifn:cleanJavaScript(pbm_presc_id)}';
		var patstatus = '${ifn:cleanJavaScript(param.patstatus)}';
		var gPatientTypeDefault = '${prefs.pharmacy_patient_type}';
		var gAllowRateIncrease = '${actionRightsMap.allow_rateincrease}';
		var gAllowRateDecrease = '${actionRightsMap.allow_ratedecrease}';
		var gAllowRateChange = ((gAllowRateIncrease == 'A') || (gAllowRateDecrease == 'A')) ? 'A' : 'N';
		var gAllowDiscounts = '${actionRightsMap.allow_discount}';
		var prefVAT = '${prefVat}';
		var prefBarCode = '${prefbarcode}';
		var isSharedLogIn = '${ifn:cleanJavaScript(isSharedLogIn)}';
		var actionId = '${actionId}';
		var allowBillNowInsurance = '${prefs.allow_bill_now_insurance}';
		var ratePlanForNonInsuredBill = '${centerPrefs.map.pref_rate_plan_for_non_insured_bill}';
		var returnValidDays = '${prefs.return_validity}';
		var multiStoreAccess = ${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')};

		var cpath = '${cpath}';
		var pharmaReturnRestricted = '${prefs.pharma_return_restricted}';
		var genericNames = JSON.parse('${ifn:cleanJavaScript(genericNames)}');
		var jRetailDocNames = <%= request.getAttribute("retailDocNames") %>;
		var jPrescribedDocNames = <%= request.getAttribute("prescribedDocNames") %>;
		var showHospAmts = '${prefs.show_hosp_amts_pharmasales}';
		var selectBillLater = '${prefs.default_bill_later_creditsales}';
		var allowDecimalsForQty = '${prefs.allow_decimals_in_qty_for_issues}';
		var ignoreItemReturnValidityDays = ${roleId ==1 || roleId==2 || actionRightsMap.allow_pharmacy_item_return_after_validity_days == 'A' };
		var indentStore = '${param.indentStore == null ? -4 : ifn:cleanJavaScript(param.indentStore)}';
		var includeZeros = '${includeZeros}';
		var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
		var storeMedicineAjaxUrlParamQueryStr = '&hosp=${ifn:cleanURL(sesHospitalId)}&includeZeroStock=${includeZeros}&retailable=Y&billable=Y&issueType=CR';
		var allCenterPrefsJson = <%= request.getAttribute("allCenterPrefsJson") %>;
		var pIndentNo = '${ifn:cleanJavaScript(param.patient_indent_no) }';
		var corporateInsurance = '${prefs.corporate_insurance}';
		var blockIp = '${blockIp}';
		var pbmPriceThreshold = '${pbmPriceThreshold}';
		var insurExpireAllow = '${prefs.check_insu_card_exp_in_sales}';//added for insurance expire vaidation.
		var discountPlansJSON = '${discountPlansJSON}';
		var medicinesNameList = '${ifn:cleanJavaScript(pbm_threshold_medicines)}';
		var dischargePrinterId = '${dischargePrinterId}';
		var billNumber = 'medicine_sales'; //Used as reference to Paytm transaction
		var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
		var income_tax_cash_limit_applicability = '${incomeTaxCashLimitApplicability}';
		var cashTransactionLimitAmt =  ${empty cashTransactionLimit ? 0 : cashTransactionLimit};
		var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
		var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
		var defaultCountryCode = '${ifn:cleanJavaScript(defaultCountryCode)}';
		var regPref = ${regPrefJSON};
		var govtIdentifierTypesJSON = <%=request.getAttribute("govtIdentifierTypesJSON")%>;
		var ip_credit_limit_rule = '${prefs.ip_credit_limit_rule}';
		var smartCardEnabled = "${centerPrefs.map.pref_smart_card_enabled}";
		var smartCardIDPattern = "${centerPrefs.map.smart_card_id_pattern}";
		var allowTaxEditRights ='${actionRightsMap.allow_tax_subgroup_edit}';
		var discCategoryNeeded = ${(roleId == 1) || (roleId == 2) ||
		(actionRightsMap.allow_discount_plans_in_bill ne 'N')};
		
		var ipDepositSetOff = 0;
		var generalDepositSetOff = 0;
		var availableDeposits = 0;
		var ipDeposits = 0
		var availableRewardPoints = 0;
		var availableRewardPointsAmount = 0;
		var billRewardPoints = 0;
		var isMvvPackage  = 0;
		var visitType = '';
		var hasRewardPointsEligibility = false;
		if(counter == 'Y' && gUserCounterId > 0) {
			enableBankDetails(document.getElementById('paymentModeId0'));
		}
	</script>
	<insta:js-bundle prefix="billing.salucro"/>
	<insta:js-bundle prefix="sales.issues"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
</head>

<body onload="init();ajaxForPrintUrls();loadLoyaltyDialog();loadProcessPaymentDialog();filterPaymentModes();" class="yui-skin-sam">
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="pbmapproved">
 <insta:ltext key="salesissues.sales.details.pbmapprovedamt"/>
</c:set>
<c:set var="pbmstatus">
 <insta:ltext key="salesissues.sales.details.pbmstatus"/>
</c:set>
<c:set var="returnText">
 <insta:ltext key="salesissues.sales.details.return"/>
</c:set>
<c:set var="saleText">
 <insta:ltext key="salesissues.sales.details.sale"/>
</c:set>
<c:set var="applyText">
 <insta:ltext key="salesissues.sales.details.applyText"/>
</c:set>
<c:set var="closeText">
 <insta:ltext key="salesissues.sales.details.closeText"/>
</c:set>
<c:set var="okText">
 <insta:ltext key="salesissues.sales.details.okText"/>
</c:set>
<c:set var="cancelText">
 <insta:ltext key="salesissues.sales.details.cancelText"/>
</c:set>
<c:set var="searchText">
 <insta:ltext key="salesissues.sales.details.searchText"/>
</c:set>
<c:set var="clearText">
 <insta:ltext key="salesissues.sales.details.clearText"/>
</c:set>

<div id="storecheck" style="display: block;" >
<form method="POST" action="MedicineSales.do" name="salesform" id="salesform" autocomplete="off">
	<input type="hidden" name="method" value="makeSale">
	<input type="hidden" name="creditBillNo" value="">
	<input type="hidden" name="existingCustomer" value="N">
	<input type="hidden" name="retailCustomerId" value="">
	<input type="hidden" name="counterId" id="counterId" value="${pharmacyCounterId}">
	<input type="hidden" id="dialogId" value=""/>
	<input type="hidden" name="itemIdentification" value=""/>
	<input type="hidden" name="returnPatientType" value=""/>
	<input type="hidden" name="isSharedLogIn" value="${ifn:cleanHtmlAttribute(isSharedLogIn)}"/>
	<input type="hidden" name="authUser" value=""/>
	<input type="hidden" name="patient_indent_no_param" value="${ifn:cleanHtmlAttribute(param.patient_indent_no)}"/>
	<input type="hidden" name="pbm_presc_id" value="${ifn:cleanHtmlAttribute(pbm_presc_id)}"/>
	<input type="hidden" name="erx_pbm_presc_id" value="${ifn:cleanHtmlAttribute(erx_pbm_presc_id)}"/>
	<input type="hidden" name="discountPlanId" id="discountPlanId" value=""/>

<div id="genericSearchDialog" style="display: none;" >
	<div class="bd">
		<fieldset class="fieldsetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.searchitemusinggenericname"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.name"/> :</td>
					<td valign="top">
						<div id="medicine_name_wrapper" >
							<input type="text" name="medicine_name" id="medicine_name"  class="field" style="width:245px"/>
							<div id="medicinename_dropdown" style="width:245px"></div>
						</div>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.generic"/>&nbsp;<insta:ltext key="salesissues.sales.details.name"/>:</td>
					<td valign="top">
						<div id="generic_name_wrapper" >
							<input type="text" name="generic_name" id="generic_name" class="field" style="width:245px"/>
							<div id="generic_name_dropdown" style="width:245px"></div>
						</div>
					</td>
				</tr>
				<tr height="10px"></tr>
				<tr height="10px">
					<td colspan="2" align="right">
						<input type="button" name="" value="${searchText}" onclick="searchMedicine();"/>
						<input type="button" name="" value="${clearText}" onclick="clearFeilds();"/>
					</td>
				</tr>
			</table>
			<div id="resultsDiv" align="right">
				<select name="results" id="results" multiple="multiple" style="width:30em;height: 10em" class="listbox"></select>
			</div>
		</fieldset>
		<table>
			<tr>
				<td><input type="button" value="${okText}" onclick="setMedicine();"/></td>
				<td><input type="button" value="${cancelText}" onclick="closeGenericSearchDialog()"/></td>
			</tr>
		</table>
	</div>
</div>

<div id="itemSearchDialog" style="display: none;">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.additem"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%"  id="medtable">
				<tr>
					<c:choose>
					 <c:when test="${prefbarcode eq 'Y'}">
					 <td class="formLabel" valign="middle" colspan="2" style="text-align:left;" >
					 	<insta:ltext key="salesissues.sales.details.itembarcode"/>:&nbsp;
					 </td>
					<td valign="top"colspan="2">
						<div id="medicine_wrapper" style="width: 20em; padding-bottom:0.2em">
							<input type="text" name="medicine" id="medicine" style="width: 20.5em; color:${color}">
							<div id="medicine_dropdown" style="width: 35em; padding-bottom:0.2em"></div>
						</div>
					</td>
					</c:when>
					</c:choose>
					<c:choose>
						 <c:when test="${prefbarcode eq 'Y'}">
						 	<td>
							<input type="button" name="searchGen" id = "searchGen" value=".." style="display: ${sale_return ? 'none' : 'block' }"
							title="Search for Equivalent Drugs by Generic Names" onclick="showSearchWindow();"/>
							<input type="hidden" name="addMedicineId" value=""/>
							</td>
							</tr>
							<tr>
						 	<td class="formLabel"><insta:ltext key="salesissues.sales.details.barcode"/>:</td>
							<td ><label id="barCodeId" ></label></td>
						 </c:when>
						 <c:otherwise>
							<td class="formLabel" valign="middle" colspan="2" style="text-align:left;">
							 	<insta:ltext key="salesissues.sales.details.itembarcode"/>:&nbsp;
							</td>
						 	<td valign="top"colspan="2">
								<div id="medicine_wrapper" style="width: 20em; padding-bottom:0.2em">
									<input type="text" name="medicine" id="medicine" style="width: 20.5em; color:${color}">
									<div id="medicine_dropdown" style="width: 35em; padding-bottom:0.2em"></div>
								</div>
								<label type="hidden" id="barCodeId" ></label></td>
								<td>
								<input type="button" name="searchGen" id = "searchGen" value=".." style="display: ${sale_return ? 'none' : 'block' }"
								title="Search for Equivalent Drugs by Generic Names" onclick="showSearchWindow();"/>
								<input type="hidden" name="addMedicineId" value=""/>
								</td>
						 </c:otherwise>
					 </c:choose>
				</tr>
				<c:if test="${sale_return}">
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.batch"/>:</td>
						<td class="yui-skin-sam" colspan="4">
							<div id="batch_wrapper">
								<input type="text" name="batch_no" id="batch_no" style="width: 15em; padding-bottom:0.2em; color:${color}">
								<div id="batch_dropdown"></div>
							</div>
						</td>
					</tr>
				</c:if>
				<tr>
					<td class="formLabel"><insta:ltext key="salesissues.sales.details.itemcode"/>:</td>
					<td>
						<label id="orderDialogItemCode"></label>
					</td>
					<td class="formLabel"><insta:ltext key="salesissues.sales.details.generic"/>:</td>
					<td colspan="2">
						<div id="medicine_wrapper">
							<b><label id="genLabel" ></label></b>
							<input type="hidden" name="genericName" value="" id="genericName">
						</div>
					</td>
					<td style="text-align:right;">
						<input type="button" name="loadGenInfo" id = "loadGenInfo" value=".." style="display: ${sale_return ? 'none' : 'block' }"
						title="Generic information" onclick="loadGenericWindow();"/>
					</td>
				</tr>
				<tr>
					<td class="formLabel">
						<insta:ltext key="salesissues.sales.details.category"/>:
					</td>
					<td class="formInfo" colspan="2">
						<b><label id="category" ></label></b>
					</td>
					<td class="formLabel">
						<insta:ltext key="salesissues.sales.details.manufacturer"/>:
					</td>
					<td class="formInfo">
						<b><label id="manfName"></label></b>
					</td>
				</tr>
				<tr id="add_tax_groups"></tr>
				<tr>
					<td class="formLabel">
						<insta:ltext key="salesissues.sales.details.pkgtype"/>:
					</td>
					<td colspan="2">
						<b><label id="packageType"></label></b>
					</td>

					<td class="formLabel">
						${not sale_return? 'Avbl.':''} <insta:ltext key="salesissues.sales.details.qty"/>:
					</td>
					<td>
						<b><label id="medAvblQty"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formLabel">${sale_return? returnText:saleText} <insta:ltext key="salesissues.sales.details.qty"/>:</td>
					<td colspan="2">
						<input type="text" autocomplete="off" name="addQty" id="addQty" class="number" style="width: 15em;" onkeypress="return onKeyPressAddQty(event)" style="width: 10em;" />
						<b><label id="issueUnits"></label></b>
					</td>
					<td  class="formLabel">
						<insta:ltext key="salesissues.sales.details.saleuom"/>:
					</td>
					<td>
						<select class="dropdown" name="sale_unit" id="sale_unit">

						</select>
					</td>
				</tr>
				<tr id="prim_preAuthRow">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.primarypriorauthno"/>:</td>
					<td class="forminfo" colspan="2">
						<input type="text" name="prior_auth_id" id="prior_auth_id" maxlength="25" style="width: 15em;"/>
					</td>
					<td class="formlabel">
						<insta:ltext key="salesissues.sales.details.primarypriorauthmode"/>:
					</td>
					<td class="forminfo">
					 	<insta:selectdb  name="prior_auth_mode_id" id="prior_auth_mode_id"  value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
					</td>
				</tr>

				<tr id="sec_preAuthRow" style="display : none;">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.secondarypriorauthno"/>:</td>
					<td class="forminfo" colspan="2">
						<input type="text" name="sec_prior_auth_id" id="sec_prior_auth_id" maxlength="25" style="width: 15em;"/>
					</td>
					<td class="formlabel">
						<insta:ltext key="salesissues.sales.details.secondarypriorauthmode"/>:
					</td>
					<td class="forminfo">
					 	<insta:selectdb  name="prior_auth_mode_id" id="prior_auth_mode_id"  value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="-- Select --"/>
					</td>
				</tr>
				<c:if test="${mod_eclaim_erx}">
					<tr id="erxReferenceRow">
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.erx.request.info.erx.activity.id"/>:</td>
						<td class="forminfo" colspan="2">
							<input type="text" name="erx_activity_id" id="erx_activity_id" maxlength="50" style="width: 15em;"/>
						</td>
					</tr>
				</c:if>
				
				<tr id="coverdbyinsurancestatusid">
					<td class="formLabel">
						<insta:ltext key="salesissues.sales.details.coverdbyinsurance"/>:
					</td>
					<td class="formInfo" colspan="2">
						<b><label id="coverdbyinsurance" ></label></b>
						<input type="hidden" name="coverdbyinsuranceflag" value="" id="coverdbyinsuranceflag">
					</td>
					
				</tr>
			</table>
		</fieldset>
	<%-- for prescription adding new fields--%>
	<div class="title CollapsiblePanelTab" onclick="return toggleManagement()" id="managementAccordion" style="margin-bottom:10px;font-weight:bold;">
		<div style="float:left;clear: none;display:inline-block;margin:5px 0 0 10px;">
			<span style="color:black;float:left;width:600px;" id="title">Management
			<img src="${cpath}/images/up.png" id="up" style="display:none;float:right;"/>
			<img src="${cpath}/images/down.png" id="down" style="display:block;float:right;"/></span>
		</div>
	</div>
	<div id="presMang" style="display:none;">
		<fieldset class="fieldSetBorder">
			<table class="formtable" >
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.dosage"/>: </td>
					<td colspan="3">
						<input type="text" name="d_strength" id="d_strength" value="" onchange=";"/>
						<input type="text" name="d_consumption_uom" id="d_consumption_uom" value="" onchange="modifyUOMLabel(this, 'd');"/> Units
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.frequency"/>:</td>
					<td>
						<div id="frequencyAutoComplete" style="width: 138px">
							<input type="text" name="d_frequency" id="d_frequency" maxlength="150" >
							<div id="frequencyContainer" style="width: 300px;"></div>
							<input type="hidden" name="d_per_day_qty" id="d_per_day_qty" value=""/>
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.duration"/>: </td>
					<td>
						<input type="text" name="d_duration" id="d_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="calcQty(event, 'd');"/>
					</td>
					<td colspan="2" >
						<div style="width: 190px">
							<input type="radio" name="d_duration_units" value="D" onchange="calcQty(event, 'd');">Days
							<input type="radio" name="d_duration_units" value="W" onchange="calcQty(event, 'd');">Weeks
							<input type="radio" name="d_duration_units" value="M" onchange="calcQty(event, 'd');">Months
			
						</div>
					</td>
					<td class="formlabel"/>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.totalqty"/>: </td>
					<td>
						<input type="text" name="d_qty" id="d_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/>
					</td>
					<td colspan="2"><label id="d_consumption_uom_label"></label></td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.routeOfAdmin"/>: </td>
					<td>
						<select id="d_medicine_route" class="dropdown">
						<option value="">-- Select --</option>
			
						</select>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.doctorremarks"/>:</td>
					<td colspan="5">
						<div id="remarksAutoComplete" style="width: 500px">
							<input type="text" name="d_doc_remarks" id="d_doc_remarks" value="" style="width: 500px">
							<div id="remarksContainer" class="scrolForContainer"></div>
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.specialInstructions"/>:</td>
					<td colspan="5">
						<textarea name="d_special_instruction" id="d_special_instruction" style="width: 500px;" cols="50" rows="2" ></textarea>
					</td>
				</tr>
				<tr>
					<td class="formlabel" ><insta:ltext key ="salesissues.sales.details.otherremarks"/>: </td>
					<td colspan="5"><input type="text" name="d_remarks" id="d_remarks" value="" style="width: 500px"></td>
				</tr>
				<tr>
					<td class ="formlabel"><insta:ltext key="salesissues.sales.details.warninglabel"/>:</td>
					<td>
					<insta:selectdb name="warn_label" id ="warn_label" table="label_master"
									valuecol="label_id" displaycol="label_short" filtercol="status" filtervalue="A"
										 dummyvalue="-- Select --" dummyvalueId="" />
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
	<%-- buttons --%>
	<table>
		<tr>
			<td>
				<button type="button" accesskey="A" onclick="return onManualAddMedicine();">
					<b><u><insta:ltext key="salesissues.sales.details.a"/></u></b><insta:ltext key="salesissues.sales.details.dd"/>
				</button>
			</td>
			<td><input type="button" value="${closeText}" onclick="closeItemSearchDialog()"/></td>
		</tr>
	</table>
	</div>
</div>
<div id="genericInfoDialog" style="display: none;">
	<div class="bd">
		<fieldset class="fieldsetBorder">
			<legend class="fieldSetLabel"> <insta:ltext key="salesissues.sales.details.genericdetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.genericname"/> :</td>
					<td ><b><label id="gen_generic_name"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.classification"/> :</td>
					<td class="forminfo"><b><label id="classification_name"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.sub_classification"/> :</td>
					<td class="forminfo"><b><label id="sub_classification_name"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.standardadultdose"/> :</td>
					<td class="forminfo"><b><label id="standard_adult_dose"></label></b>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.criticality"/> :</td>
					<td class="forminfo"><b><label id="criticality"></label></b>
					</td>
				</tr>
			</table>
		</fieldset>
		<table>
			<tr>
				<td>
					<input type="button" name="genericInfoClose" value="${closeText}" onclick="closeGenericInfoDialog()"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<c:choose>
	<c:when test="${sale_return == 'true' || sale_return == true }">
		<h1 style="color: red;"><insta:ltext key="salesissues.sales.details.salesreturns"/> </h1>
		<input type="hidden" name="salesReturn" value="true">
	</c:when>
	<c:when test="${transaction == 'estimate'}">
		<h1 style="color: blue;"><insta:ltext key="salesissues.sales.details.pharmacyestimate"/></h1>
		<input type="hidden" name="estimate" value="true"/>
	</c:when>
	<c:otherwise>
		<input type="hidden" name="salesReturn" value="false">
		<h1>${not empty pbm_presc_id ? 'PBM' : ''} <insta:ltext key="salesissues.sales.details.sales"/></h1>
 	</c:otherwise>
</c:choose>

<insta:feedback-panel/>
<div id="hidmsg">
	<span class="resultMessage">${ifn:cleanHtml(message)}</span>
</div>

<c:set var="blockOBillOpt">${not sale_return || sale_return && prefs.pharma_return_restricted =='Y'? 'none' : 'block'}</c:set>
<c:set var="retCustBlock">${not sale_return || sale_return && prefs.pharma_return_restricted =='N'? 'none' : 'block'}</c:set>
<c:set var="blockOBill">${sale_return && prefs.pharma_return_restricted =='Y' ? 'block' : 'none'}</c:set>
<c:set var="blockNewItem">${sale_return && prefs.pharma_return_restricted =='Y'? 'none' : 'block'}</c:set>

<fieldset class="fieldSetBorder">
	<%-- one row, 3 columns, one each for patient type, sale type, counter etc.--%>
	<table style="margin-left: 4px;" width="99%">
		<tr>
			<td valign="top">				<%-- column 1: Return type and patient type --%>
				<table border="0">
					<tr>
						<td valign="top" id="patientType" style="padding-top: 4px"><insta:ltext key="salesissues.sales.details.patienttype"/>:</td>
						<td>
							<table cellspacing="0" cellpadding="0">
								<tr id="retailRow">
									<td colspan="3">
										<input type="radio" accesskey="R" name="salesType" id="salesType_retail"
										value="retail" onclick="onChangeSalesType()"
										${prefs.pharmacy_patient_type == 'R' ? 'checked' : ''}>
										<label for="salesType_retail"><b><u><insta:ltext key="salesissues.sales.details.r"/></u></b><insta:ltext key="salesissues.sales.details.etail"/></label>
									</td>
								</tr>

								<tr id="retailCreditRow">
									<td colspan="3">
										<input type="radio" name="salesType" accesskey="C" id="salesType_credit_retail"
										value="retailCredit" onclick="onChangeSalesType()"
										${prefs.pharmacy_patient_type == 'C' ? 'checked' : ''}>
										<label for="salesType_credit_retail"><insta:ltext key="salesissues.sales.details.retail"/> <b><u><insta:ltext key="salesissues.sales.details.c"/></u></b><insta:ltext key="salesissues.sales.details.redit"/></label>
									</td>
								</tr>

								<c:if test="${sale_return}">
									<tr id="returnBillRow">
										<td>
											<input type="radio" name="salesType" accesskey="B" id="salesType_bill_no"
											value="returnBill" onclick="onChangeSalesType()"
											${prefs.pharmacy_patient_type != 'H' ? 'checked' : ''}>
											<label for="salesType_bill_no"><b><u><insta:ltext key="salesissues.sales.details.b"/></u></b><insta:ltext key="salesissues.sales.details.illno"/></label>
										</td>
										<td style="padding: 0 5px 0 5px">
											<input type="text" name="rbillNo" size="8" onchange="onChangeReturnBillNo()"
											style="width: 100px">
										</td>
										<td></td>
									</tr>
								</c:if>

								<tr>
									<td>
										<input type="radio" name="salesType" accesskey="H" id="salesType_hospital"
										value="hospital" onclick="onChangeSalesType()"
										${prefs.pharmacy_patient_type == 'H' ? 'checked' : ''}>
										<label for="salesType_hospital"><b><u><insta:ltext key="salesissues.sales.details.h"/></u></b><insta:ltext key="salesissues.sales.details.ospitalpatient"/></label>
									</td>
									<td style="padding: 0 5px 0 5px">
										<div style="height: 21px;">
											<div id="psAutocomplete" style="width: 100px; padding-bottom: 8px">
												<input type="text" name="searchVisitId" id="searchVisitId" style="width: 100px"/>
												<div id="psContainer" style="width: 38em"></div>
											</div>
										</div>
									</td>
									<td >
										<input type="checkbox" name="ps_status" value="active" onchange="reInitializeAc();"
										${param.ps_status == 'active' ? 'checked' : ''}><insta:ltext key="salesissues.sales.details.activeonly"/>
									</td>
									<td style="padding: 0 5px 0 5px" id="readCardButton">
										<c:if test="${centerPrefs.map.pref_smart_card_enabled == 'Y'}">
										<c:if test="${not empty regPref.government_identifier_label}">
											<insta:analytics tagType="button" type="button" name="readCard" id="readCard" clickevent="return readFromCard();" style="width:100px"
											 	category="Sales" action="Read Card" label="${ ga_page_label }">
												<img src="${cpath}/images/CardIcon.png" width="23" height="18" align="left"/>
												<span style="margin-top:1px;position:absolute;margin-left:-23px; " >ReadData</span>
											</insta:analytics>				
										</c:if>	
										</c:if>	
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>

			<td valign="top">
				<input type="hidden" name="saleBasis" value="M">
			</td>

			<td align="right" valign="top" style="padding-right:50px;">		<%-- column 3: counter etc. --%>
				<table border="0">
					<c:if test="${(roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A')}">
						<tr>
							<td style="text-align:left;padding-bottom: 0.5em"><insta:ltext key="salesissues.sales.details.date"/>:&nbsp;</td>
							<td style="padding-bottom: 0.5em">
								<insta:datewidget name="payDate" valid="past" value="today" btnPos="left"/>
								<input type="text" name="payTime" style="width:5em"
										value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'/>
								<span class="star">*</span>
							</td>
						</tr>
					</c:if>

					<tr>
						<td style="text-align:left;padding-bottom: 0.5em"><insta:ltext key="salesissues.sales.details.store"/>:&nbsp;</td>
						<c:choose>
							<c:when test="${not empty pbm_store_id}">
								<td>
									<b>${pbm_store_name}</b>
									<input type = "hidden" name="phStore" id="phStore" value="${pbm_store_id}" />
								</td>
							</c:when>
							<c:when test="${roleId ==1 || roleId==2 || (multiStoreAccess == 'A')}">
								<td style="padding-bottom: 0.5em">
								<insta:userstores username="${userid}" elename="phStore" onchange="onChangeStore();"
									 id="phStore" onlySalesStores="Y"/>
								</td>
							</c:when>
							<c:otherwise>
								<td>
									<b>${ifn:cleanHtml(dept_name)}</b>
									<input type = "hidden" name="phStore" id="phStore" value="${ifn:cleanHtmlAttribute(dept_id)}" />
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
					<tr>
						<td style="text-align:left;padding-bottom: 0.5em"><insta:ltext key="salesissues.sales.details.counter"/>:&nbsp;</td>
						<td style="padding-bottom: 0.5em" >
							<label id="counter_label"><b>${pharmacyCounterName}</b></label>
						</td>
					</tr>

				</table>
			</td>
		</tr>
	</table>
</fieldset>

<div id="custDetails" style="display: ${retCustBlock}">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"> <insta:ltext key="salesissues.sales.details.retailcustomerdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.name"/>:</td>
				<td><input type="text" name="custName" maxlength="50" onblur="capWords(custName)"
						onkeypress="nextFieldOnEnter(event)" onFocus="nextfield ='custDoctorName';"><span class="star">*</span></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.doctor"/>:</td>
				<td>
					<div id="doctor_wrapper" style="width: 17em; padding-bottom:0.2em">
					<input type="text" name="custDoctorName" id="custDoctorName" style="width: 17em; " onblur="capWords(custDoctorName)"
					 maxlength="50" onkeypress="nextFieldOnEnter(event)" onFocus="nextfield ='medicine';">
					<div id="doctor_dropdown"></div>
				</div>
				<span class="star" style="padding-left:205px;">*</span>
				</td >
				<td style="width: 76px; text-align: right;"><insta:ltext key="salesissues.sales.details.mobileno"/>:</td>
				<td style="width: 190px;">
					<div style="margin-top: 12px;">
						<input type="hidden" id="cust_patient_phone_valid" value="N"/>
						<input type="hidden" name="retailPatientMobileNo" id="retailPatientMobileNo" />
						<input type="text" class="field" id="retailPatientMobileNoField" 
							onkeypress="return enterNumOnlyzeroToNinePlus(event)" onblur="validateMobileNumber(event)"
								style="width: 9.6em; padding-top: 1px" />
						<img class="imgHelpText" id="retail_patient_phone_help" src="${cpath}/images/help.png" />
					</div>	
					<div style="width: 200px;">
						<span style="visibility: hidden; padding-left: 1px; color: #f00" id="retail_patient_phone_error"></span>
					</div>
				</td>
			</tr>
			<tr>	
				<c:if test="${not empty fn:trim(regPref.nationality)}">
					<td class="formlabel">${ifn:cleanHtml(regPref.nationality)}:</td>
					<td>
						<insta:selectdb name="nationalityId" id="nationalityId" table="country_master"
							class="field" style="width:140px;" dummyvalue="-- Select --"
								size="1" valuecol="country_id" displaycol="country_name" usecache="true" onchange="onChangeOfNationalityId();"/>
						<input type="hidden" id="rNationalityId" name="rNationalityId"/>
					</td>
				</c:if>
				<c:if test="${not empty regPref.government_identifier_type_label}">
					<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_type_label)}:</td>
					<td>
						<select name="identifierId" id="identifierId" class="dropdown" style="width:137px;" onchange="setGovtPattern();">
							<option value="">--Select--</option>
							<c:forEach items="${govtIdentifierTypes}" var="govtIdTypes">
								<option value="${govtIdTypes.map.identifier_id}" ${govtIdTypes.map.default_option eq 'Y' ? 'selected' : ''}>
										${govtIdTypes.map.remarks}</option>
							</c:forEach>
						</select>
						<input type="hidden" id="rIdentifierId" name="rIdentifierId"/>
					</td>
				</c:if>
				<c:if test="${not empty regPref.government_identifier_label}">
					<td class="formlabel">${ifn:cleanHtml(regPref.government_identifier_label)}:</td>
					<td>
						<input type="text" name="governmentIdentifier" id="governmentIdentifier" maxlength="50" />
					</td>
				</c:if>
			</tr>
		</table>
	</fieldset>
</div>

<div id="custRetailDetails" style="display:none">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.retailcreditcustomerdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.name"/>:</td>
				<td valign="top">
					<div id="custRetailName_wrapper" style="width: 17em; padding-bottom:0.2em;">
						<input type="text" name="custRetailCreditName" id="custRetailCreditName" onblur="capWords(custRetailCreditName)" maxlength="50"
							style="width: 16em" onkeypress="nextFieldOnEnter(event)" onFocus="nextfield ='custRetailCreditDocName';">
						<div id="custRetailName_dropdown"></div>
					</div><span class="star" style="padding-left:192px;">*</span>
				</td>

				<td class="formlabel"><insta:ltext key="salesissues.sales.details.doctor"/>:</td>
				<td valign="top">
					<div id="creditdoctor_wrapper" style="width: 17em; padding-bottom:0.2em">
					<input type="text" name="custRetailCreditDocName" id="custRetailCreditDocName" style="width: 17em; " onblur="capWords(custRetailCreditDocName)"
					 maxlength="50" onkeypress="nextFieldOnEnter(event)" onFocus="nextfield ='custRetailSponsor';">
					<div id="creditdoctor_dropdown"></div><span class="star"  style="padding-left:205px;">*</span>
				</div>
				</td>
			</tr>
			<tr>
			 <td></td>
			 <td></td>
			 <td></td>
			 <td></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.sponsor"/>:</td>
				<td>
					<div id="custRetailSponsor_wrapper">
						<input type="text" name="custRetailSponsor" id="custRetailSponsor" maxlength="50"
								style="width: 16em" onkeypress="nextFieldOnEnter(event)" onFocus="nextfield ='custRCreditPhoneNoField';">
						<div id="custRetailSponsor_dropdown"></div>
					</div>
				</td>

				<td style="width: 76px; text-align: right;"><insta:ltext key="salesissues.sales.details.mobileno"/>:</td>
				<td style="width: 190px;">
					<div style="margin-top: 12px;">
					<div>
						<input type="hidden" id="cust_retail_patient_phone_valid" value="N"/>
						<input type="hidden" id="custRCreditPhoneNo" name="custRCreditPhoneNo"/>
						<input type="text" class="field" id="custRCreditPhoneNoField" name="custRCreditPhoneNoField"
							 onkeypress="return enterNumOnlyzeroToNinePlus(event);nextFieldOnEnter(event)" onblur="validateMobileNumber(event)"
								style="width: 9.6em; padding-top: 1px"  onFocus="nextfield ='custRCreditLimit';"/>
						<span class="star">*</span>		
						<img class="imgHelpText" id="retail_credit_patient_phone_help" src="${cpath}/images/help.png" />
					</div>
					<div>
						<span style="visibility: hidden; padding-left: 1px; color: #f00" id="retail_credit_patient_phone_error"></span>
					</div>
					</div>
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.creditlimit"/>:</td>
				<td>
					<input type="text" name="custRCreditLimit" id="custRCreditLimit" maxlength="20"	onchange="validateCreditLimit()"
					onkeypress="nextFieldOnEnter(event)" onFocus="nextfield ='medicine';"><span class="star">*</span>
				</td>
			</tr>
		</table>
	</fieldset>
</div>

<div id="patientDetails" style="display:none">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.patientdetails"/></legend>
		<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="ui.label.mrno"/>:</td>
				<td style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;width:20%;">
					<div id="photoId" style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;font-weight:bold;">
						<div id="patientMrno" style="float:left;padding-right:5px;"></div>
						<img id="pd_viewPhotoIcon" style="float:left; vertical-align:middle;cursor:pointer;" onclick="initPatientPhotoDialog();showPatientPhotoDialog();setPatientPhotoDialogWidth();" >
					</div>
					<input name="mrno" type="hidden">
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.name"/>:</td>
				<td class="forminfo"><div id="patientName"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.age.or.gender"/>:</td>
				<td class="forminfo"><div id="patientAgeSex"></div></td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.visitno"/>:</td>
				<td class="forminfo">
					<div id="patientVisitNo"></div>
					<input name="visitStatus" type="hidden">
					<input name="visitId" type="hidden"><input name="visitType" type="hidden">
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.dept"/>:</td>
				<td class="forminfo"><div id="patientDept"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.doctor"/>:</td>
				<td valign="top" style="padding-bottom: 0px; padding-top: 0px">
					<div id="patientdoctor_wrapper" style="width: 14em; padding-bottom:0.2em">
						<input type="text" name="patientDoctor" id="patientDoctor" style="width: 14em; " maxlength="100" >
						<div id="patientdoctor_dropdown" style="width: 25em"></div>
					</div><span class="star"  style="padding-left:168px;">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.adm.or.regdate"/>:</td>
				<td class="forminfo"><div id="admitDate"><div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.referredby"/>:</td>
				<td class="forminfo"><div id="referredBy"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.bed"/>:</td>
				<td class="forminfo"><div id="patientBedType"></div>
					<input name="planId" id="planId" type="hidden" value="0">
					<input name="isTpa" id="isTpa" type="hidden">
					<input name="secPlanId" id="secPlanId" type="hidden" value="0">
				</td>
			</tr>
			<tr id="patientDetailsTPARow" style="display:none">
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.rateplan"/>:</td>
				<td class="forminfo"><div id="ratePlan"></div></td>
				<td id="tpaDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.tpa.or.sponsor"/>:</td>
				<td class="forminfo" style="width: 100%"><div id="tpaName"></div></td>
				<td class="formlabel">
					<label id="patientDetailsInsCoLbl"><insta:ltext key="salesissues.sales.details.insuranceco"/>:</label>
					<label id="patientDetailsCorpCoLbl"><insta:ltext key="salesissues.sales.details.sponsorco"/>:</label>
				</td>
				<td class="forminfo"><div id="patientInsuranceCo"></div></td>
			</tr>
			<tr id="patientDetailsTPAExtRow" style="display:none">
				<td class="formlabel">${ifn:cleanHtml(memberIdLabel)}:</td>
				<td style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;width:20%;">
					<div  style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;font-weight:bold;">
						<div id="policyId" style="float:left;padding-right:40px;padding-top: 3%;width:80%;"></div>
						<img id="pri_cardIcon" style="float:left; vertical-align:middle;cursor:pointer;padding-top: 2%;width:15%;" onclick="showPatientInsuranceDialog();" >
					</div>
				</td>
				<td class="formlabel" id="networkPlanTypeLblCell"><insta:ltext key="salesissues.sales.details.network.or.plantype"/>:</td>
				<td class="forminfo" id="networkPlanTypeValueCell"><div id="planType"></div></td>
				<td id="tpaDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.planname"/>:</td>
				<td style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;width:20%;">
					<div  style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;font-weight:bold;">
						<div id="planname" style="float:left;padding-right:5px;padding-top: 2%;width:80%;"></div>
							
						<button id="pd_planButton" title="${title2}" style="float:left; vertical-align:middle;cursor:pointer;width:15%;"
						onclick="javascript:initPatientPlanDetailsDialog();showPatientPlanDetailsDialog();" type="button" > .. </button>
					</div>	
				</td> 
				<td class="forminfo"><input type="hidden" name="policyExpireDays" id="policyExpireDays" value=""/></td><!--added for insurance expire vaidation  -->
				<td class="forminfo"><div id="priCategoryPayable" style="display:none"></div>
			</tr>
			<tr id="priCorporateRow" style="display:none">
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.rateplan"/>:</td>
				<td class="forminfo"><div id="corpRatePlan"></div></td>
				<td id="pcorpDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.corporatename"/>:</td>
				<td class="forminfo"><div id="corpName"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.employeeid"/>:</td>
				<td class="forminfo"><div id="employeeId"></div></td>
			</tr>
			<tr id="secCorporateRow" style="display:none">
				<td class="formlabel"></td>
				<td class="forminfo"></td>
				<td id="scorpDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.seccorporatename"/>:</td>
				<td class="forminfo"><div id="scorpName"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.employeeid"/>:</td>
				<td class="forminfo"><div id="semployeeId"></div></td>
			</tr>
			<tr id="priNationalRow" style="display:none">
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.rateplan"/>:</td>
				<td class="forminfo"><div id="natRatePlan"></div></td>
				<td id="pnationalDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.sponsorname"/>:</td>
				<td class="forminfo"><div id="natName"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.nationalid"/>:</td>
				<td class="forminfo"><div id="pnatId"></div></td>
			</tr>
			<tr id="secNationalRow" style="display:none">
				<td class="formlabel"></td>
				<td class="forminfo"></td>
				<td id="snationalDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.secsponsorname"/>:</td>
				<td class="forminfo"><div id="snatName"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.nationalid"/>:</td>
				<td class="forminfo"><div id="snatId"></div></td>
			</tr>
			<tr id="patientDetailsSecTPARow" style="display:none">
				<td class="formlabel"></td>
				<td class="forminfo"></td>
				<td id="tpaDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.sectpa.or.sponsor"/>:</td>
				<td class="forminfo"><div id="secTpaName"></div></td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.insuranceco"/>:</td>
				<td class="forminfo"><div id="secPatientInsuranceCo"></div></td>
			</tr>
			<tr id="secPatientDetailsTPAExtRow" style="display:none">
				<td class="formlabel">${ifn:cleanHtml(memberIdLabel)}:</td>
				<td style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;width:20%;">
					<div  style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;font-weight:bold;">
						<div id="secPolicyId" style="float:left;padding-right:40px;padding-top: 3%;width:80%;"></div>
						<img id="sec_cardIcon" style="float:left; vertical-align:middle;cursor:pointer;padding-top: 2%;width:15%;" onclick="showPatientInsuranceSecDialog();" >
					</div>
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.network.or.plantype"/>:</td>
				<td class="forminfo"><div id="secPlanType"></div></td>
				<td id="tpaDetails" class="formlabel"><insta:ltext key="salesissues.sales.details.planname"/>:</td>
				<td>
				
					<div  style="verflow: hidden;text-overflow: ellipsis;white-space: nowrap;font-weight:bold;">
						<div id="secPlanname" style="float:left;padding-right:5px;padding-top: 2%;width:80%;"></div>
								
						<button id="pd_planButton" title="${title2}" style="float:left; text-align:right; vertical-align:middle;cursor:pointer;width:15%"
								onclick="javascript:initPatientSecPlanDetailsDialog();showPatientSecPlanDetailsDialog();" type="button" > .. </button>
					</div>
				</td>
			</tr>

		</table>
	</fieldset>
</div>
<c:if test="${mod_eclaim_erx && !sale_return}">
	<div id="insuranceDetails" style="display:none">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="ui.label.insurance.details"/></legend>
			<table class="insuranceDetails" cellpadding="0" cellspacing="0" width="100%" border="0">
				<tr>
					<td class="formlabel" style="text-align:right; padding-right:5px; width: 14%;"><insta:ltext key="patient.discharge.medication.erx.request.info.erx.ref.no"/> :</td>
					<td style="width: 20%;">
						<input type="text" name="erxReferenceNo" id="erxReferenceNo" maxlength="50" >
					</td>
					<td class="formlabel" style="text-align:right; padding-right:5px;"><insta:ltext key="ui.label.external.pbm"/> :</td>
					<td>
						<input type="checkbox" name="isExternalPbm" id="isExternalPbm" checked>
					</td>
					<td></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel" style="text-align:right; padding-right:5px; width: 14%;"><insta:ltext key="salesissues.sales.details.primarypriorauthno"/> :</td>
					<td style="width: 20%;">
						<input type="text" name="priPriorAuthNo" id="priPriorAuthNo" maxlength="100" >
					</td>
					<td class="formlabel" style="text-align:right; padding-right:5px;"><insta:ltext key="salesissues.sales.details.primarypriorauthmode"/>:</td>
						<td class="forminfo">
							<insta:selectdb  id="priPriorAuthMode" name="priPriorAuthMode" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="${select}"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel" style="text-align:right; padding-right:5px; width: 14%;"><insta:ltext key="salesissues.sales.details.secondarypriorauthno"/> :</td>
					<td style="width: 20%;">
						<input type="text" name="secPriorAuthNo" id="secPriorAuthNo" maxlength="100" >
					</td>
					<td class="formlabel" style="text-align:right;"><insta:ltext key="salesissues.sales.details.secondarypriorauthmode"/>:</td>
						<td class="forminfo">
							<insta:selectdb  id="secPriorAuthMode" name="secPriorAuthMode" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="${select}"/>
					</td>
					<td style="width: 20%;">
						<button type="button" id="copyPriorAuthDetails" onClick="copyPriorAuthToItems();"><insta:ltext key="ui.label.copy.prior.auth.to.items"/></button>
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</c:if>
	


	<fieldset class="fieldSetBorder" id="prescFieldSet" style="display: none">
		<legend class="fieldSetLabel">${not empty pbm_presc_id ? 'PBM' : ''} <insta:ltext key="salesissues.sales.details.prescription.or.indentdetails"/></legend>
		<div id="prescDetailsDiv" style="display:none">
			<table id="prescInfo">
				<tr>
					<th align="left" style="padding-right: 5px"><insta:ltext key="salesissues.sales.details.indent.or.doctor"/></th>
					<th align="left" style="padding-right: 5px">${not empty pbm_presc_id ? 'PBM' : ''} <insta:ltext key="salesissues.sales.details.prescription.or.indent"/></th>
					<th align="left"><insta:ltext key="salesissues.sales.details.dispensestatus"/></th>
					<c:if test="${not empty pbm_presc_id}">
						<th align="left" style="padding-left: 10px"><insta:ltext key="salesissues.sales.details.pbmapprovalstatus"/></th>
						<th align="left" style="padding-left: 10px"><insta:ltext key="salesissues.sales.details.pbmstatus"/></th>
					</c:if>
				</tr>
				<tr id="prescTemplateRow" style="display:none" >
					<td style="padding-right: 5px"></td>
					<td style="padding-right: 5px"><a target="_blank" href=""><insta:ltext key="salesissues.sales.details.view"/></a></td>
					<td>
						<select name="dispensedMedicine" class="dropdown">
							<c:if test="${!sale_return}">
								<option value="all"><insta:ltext key="salesissues.sales.details.closeall"/></option>
							</c:if>
							<option value="partiall"><insta:ltext key="salesissues.sales.details.closedispensed"/></option>
							<option value="full" selected><insta:ltext key="salesissues.sales.details.closefullydispensed"/></option>
						</select>
						<input type="hidden" name="consultationId"/>
						<input type="hidden" name="patientIndentNoRef"/>
						<input type="hidden" name="dischargeId"/>
					</td>
					<c:if test="${not empty pbm_presc_id}">
						<td style="padding-left: 10px"><label id="pbmApprovalStatuslbl"></label></td>
						<td style="padding-left: 10px"><label id="pbmStatuslbl"></label></td>
					</c:if>
				</tr>
			</table>
		</div>
		<table id="unavblMedicines"></table>
	</fieldset>


<%--
	Main item list table starts here
--%>
<c:set var="numColumns" value="15"/>
<div class="resultList" style="margin: 10px 0px 5px 0px;">
<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="medList" border="0">
	<tr>
		<th style="width: 1em;">#</th>
		<th style="width: 20em;padding-left: 20px;"><insta:ltext key="salesissues.sales.details.item"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.itemcode"/></th>
		<th><insta:ltext key="salesissues.sales.details.control.type.name"/></th>
		<th><insta:ltext key="salesissues.sales.details.batch"/></th>
		<th><insta:ltext key="salesissues.sales.details.expiry.bin.qty"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.mrp.pkg.in.brackets"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.pkgsize"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.unitrate"/></th>
		<c:if test="${not empty pbm_presc_id}">
			<th style="text-align:right"><insta:ltext key="salesissues.sales.details.issueqty"/></th>
		</c:if>
		<th style="text-align:right">${sale_return? returnText:saleText} <insta:ltext key="salesissues.sales.details.qty"/></th>
		<th>${sale_return? returnText:saleText} <insta:ltext key="salesissues.sales.details.uom"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.discount"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.amount"/></th>
		<c:if test="${prefVat eq 'Y'}" >
			<th style="text-align:right"><insta:ltext key="salesissues.sales.details.tax"/></th>
			<c:set var="numColumns" value="${numColumns + 1}"/>
		</c:if>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.patamt"/></th>
		<th style="text-align:right"><insta:ltext key="salesissues.sales.details.pattaxamt"/></th>
		<th style="text-align:right;"><insta:ltext key="salesissues.sales.details.pri.claim"/></th>
		<th style="text-align:right;"><insta:ltext key="salesissues.sales.details.pri.taxclaim"/></th>
		<th style="text-align:right;"><insta:ltext key="salesissues.sales.details.pri.priorauth"/></th>
		<c:if test="${not empty pbm_presc_id}">
			<th title=' <insta:ltext key="salesissues.sales.details.pbmapprovedamt"/>' style="text-align:right"><insta:ltext key="salesissues.sales.details.apprd"/></th>
			<th title=' <insta:ltext key="salesissues.sales.details.pbmstatus"/>' style="text-align:right"><insta:ltext key="salesissues.sales.details.status"/></th>
		</c:if>
		<th style="text-align:right;"><insta:ltext key="salesissues.sales.details.sec.claim"/></th>
		<th style="text-align:right;"><insta:ltext key="salesissues.sales.details.sec.taxclaim"/></th>
		<th style="text-align:right;"><insta:ltext key="salesissues.sales.details.sec.priorauth"/></th>
		<c:if test="${mod_eclaim_erx}">
			<th style="text-align:right;"><insta:ltext key="patient.discharge.medication.erx.request.info.erx.activity.id"/></th>
		</c:if>
		<th width="20px"></th>
		<th width="20px"></th>
	</tr>

	<c:set var="itemClass" value="${sale_return ? 'return' : transaction eq 'estimate' ? 'estimate' : ''}"/>

	<%-- template row for copying: for newly added items. For quick testing on LAF, remove display:none --%>
	<tr id="templateRow" style="display: none">
		<td>
			<label>1</label>
		</td>
		<td class="${itemClass}">
			<input type="hidden" name="temp_charge_id" value=""/>
			<input type="hidden" name="medicineId" value=""/>
			<input type="hidden" name="itemCode" value=""/>
			<input type="hidden" name="controlTypeName" value=""/>
			<input type="hidden" name="expiry" value=""/>
			<input type="hidden" name="origRate" value=""/>
			<input type="hidden" name="taxPer" value=""/>
			<input type="hidden" name="pkgUnit" value=""/>
			<input type="hidden" name="consultId" value=""/>
			<input type="hidden" name="medName" value="" id ="medName"/>
			<input type="hidden" name="medPrescribedId" value=""/>
			<input type="hidden" name="medPBMPrescId" value=""/>
			<input type="hidden" name="medPBMStatus" value=""/>
			<input type="hidden" name="batchNo" value=""/>
			<input type="hidden" name="identification" value=""/>
			<input type="hidden" name="pkgmrp" value=""/>
			<input type="hidden" name="pkgcp" value=""/>
			<input type="hidden" name="userQty" value=""/>		<%-- quantity in user selected units --%>
			<input type="hidden" name="qty" value=""/>        <%-- quantity in issue units --%>
			<input type="hidden" name="issueUnits" value="I"/>
			<input type="hidden" name="medDiscWithoutInsurance" value=""/>
			<input type="hidden" name="medDiscWithInsurance" value=""/>
			<input type="hidden" name="medDisc" value=""/>
			<input type="hidden" name="medDiscType" value=""/>
			<input type="hidden" name="medDiscRS" value=""/>
			<input type="hidden" name="amt" value=""/>
			<input type="hidden" name="insuranceCategoryId" value=""/>
			<input type="hidden" name="billingGroupId" value=""/>
			<input type="hidden" name="itemCategoryId" value=""/>
			<input type="hidden" name="tax" value=""/>
			<input type="hidden" name="orgTaxAmt" value=""/>
			<input type="hidden" name="patCalcAmt" value=""/>
			<input type="hidden" name="claimAmt" value=""/>
			<input type="hidden" name="preAuthId" value=""/>
			<input type="hidden" name="preAuthModeId" value="1"/>
			<input type="hidden" name="primclaimAmt" value=""/>
			<input type="hidden" name="primpreAuthId" value=""/>
			<input type="hidden" name="primpreAuthModeId" value="1"/>
			<input type="hidden" name="priIncludeInClaim" value=""/>
			<input type="hidden" name="secclaimAmt" value=""/>
			<input type="hidden" name="secpreAuthId" value=""/>
			<input type="hidden" name="secpreAuthModeId" value="1"/>
			<input type="hidden" name="erxActivityId" value=""/>
			<input type="hidden" name="secIncludeInClaim" value=""/>
			<input type="hidden" name="indent_item_id" value=""/>
			<input type="hidden" name="itemBatchId" value=""/>
			<input type="hidden" name="frequency" value=""/>
			<input type="hidden" name="duration" value=""/>
			<input type="hidden" name="durationUnit" value=""/>
			<input type="hidden" name="dosage" value=""/>
			<input type="hidden" name="dosageUnit" value=""/>
			<input type="hidden" name="doctorRemarks" value=""/>
			<input type="hidden" name="special_instr" value=""/>
			<input type="hidden" name="salesRemarks" value=""/>
			<input type="hidden" name="warningLabel" value=""/>
			<input type="hidden" name="routeOfAdmin" value=""/>
			<input type="hidden" name="routeName" value=""/>
			<input type="hidden" name="totalQty" value=""/>
			<input type="hidden" name="consCheck" value=""/>
			<input type="hidden" name="approvedQty" value=""/>
			<input type="hidden" name="pbmPaymentAmt" value=""/>
			<input type="hidden" name="is_claim_locked" value="false"/>
			<!-- added for insurance expire vaidation. -->
			<input type="hidden" name="freqCount" value=""/>
			<input type="hidden" name="allowedQty" value=""/>
			<input type="hidden" name="insuranceExpired" value="false"/>
			<input type="hidden" name="total_issed_qty" value=""/>
			<input type="hidden" name="pbmitemrate" value=""/>
			<input type="hidden" name="approvedNetAmount" value=""/>
			<!-- End -->
			<!-- added for insurance exclusions. -->
			<input type="hidden" name="cat_payable" value="t"/>
			<input type="hidden" name="insurance_cat_payable" value="t"/>
			<input type="hidden" name="item_excluded_from_doctor" value="${prescription.item_excluded_from_doctor}"/>
			<input type="hidden" name="item_excluded_from_doctor_remarks" value="${prescription.item_excluded_from_doctor_remarks}"/>
			<!--added for discharge medication -->
			<input type="hidden" name="medicationId" value=""/>
			<!-- Added for store tariff -->
			<input type="hidden" name="visit_selling_price" id="visit_selling_price"/>
			<input type="hidden" name="store_selling_price" id="store_selling_price"/>
			<input type="hidden" name="selling_price_hid" id="selling_price_hid"/>
			<input type="hidden" name="patientTaxAmt" id="patient_tax_amt"/>
			<input type="hidden" name="priClaimTaxAmt" id="priClaimTaxAmt"/>
			<input type="hidden" name="secClaimTaxAmt" id="secClaimTaxAmt"/>
			<c:forEach items="${groupList}" var="group">
				<input type="hidden" name="taxname${group.item_group_id}" value="" />
				<input type="hidden" name="taxrate${group.item_group_id}" value="" />
				<input type="hidden" name="taxamount${group.item_group_id}" value="" />
				<input type="hidden" name="taxsubgroupid${group.item_group_id}" value="" />
			</c:forEach>
			
			<img src="${cpath}/images/empty_flag.gif"/>
			<label><insta:ltext key="salesissues.sales.details.medicineitemnameassample"/></label>
		</td>
		<td style="text-align:right"></td>
		<td></td>
		<td>
			<img src="${cpath}/images/empty_flag.gif"/>
			<label>0000000</label>
		</td>
		<td></td>
		<td style="text-align:right"></td>
		<td style="text-align:right"></td>
		<td style="text-align:right"></td>
		<td style="text-align:right"></td>
		<td></td>
		<td style="text-align:right"></td>
		<td style="text-align:right"></td>
		<c:if test="${prefVat eq 'Y'}" >
			<td style="text-align:right"></td>
		</c:if>
		<td style="text-align:right"></td>
		<td style="text-align:right"></td>
		<td style="text-align:right"></td>
		<c:if test="${not empty pbm_presc_id}">
			<td style="text-align:right"></td>
			<td style="text-align:right"></td>
		</c:if>
		<td style="text-align:right;"></td>
		<td style="text-align:right;"></td>
		<td style="text-align:right;"></td>
		<td style="text-align:right;"></td>
		<td style="text-align:right;"></td>
		<c:if test="${mod_eclaim_erx}">
			<td style="text-align:right;"></td>
		</c:if>
		<td width="20px">
			<a href="javascript:Delete Item" onclick="deleteItem(this);return false;" title='<insta:ltext key="salesissues.sales.details.deleteitem"/>'>
				<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
			</a>
		</td>
		<td style="text-align: center">
			<a href="javascript:void(0)" onclick="openEditDialogBox(this); return false;" title='<insta:ltext key="salesissues.sales.details.edititemdetails"/>'>
				<img src="${cpath}/icons/Edit.png" class="button" name="edit"/>
			</a>
		</td>
	</tr>

	<c:set var="hasDiscountRights"
		value="${(roleId == 1) || (roleId == 2) || (actionRightsMap.allow_discount ne 'N')}"/>

</table>
</div>
<table class="addButton">
	<tr>
		<td>
			<table class="footerTable">
				<tr>
				<td>
				<insta:ltext key="salesissues.sales.details.discountcategory"/>:
				<select name="discountCategory" id="discountCategory" class="dropdown"
					 value="" onchange="onChangeDiscountCategory();" ${sale_return ? 'disabled' : ''}>
					<option value=""> -- Select -- </option>
					<c:forEach var="discCat" items="${discountCategories}">
						<option value="${discCat.map.discount_cat_id}">${discCat.map.discount_cat}</option>
					</c:forEach>
				</select>
				</td>
				<td>
					<insta:ltext key="salesissues.sales.details.itemdiscount"/>:&nbsp;&nbsp;
					<input type="text" name="itemDiscPer" id="itemDiscPer" value="0" size="3"
						onchange="return validateDiscPer();" style="width:60px" ${hasDiscountRights ? '' : 'readOnly'}>
					<input type="hidden" name="itemDiscType" value="I"/>
					%<input type="button" value="${applyText}" name="itemDiscPerApply" onclick="onApplyItemDiscPer();"
						${hasDiscountRights ? '' : 'disabled'}>
					</td>
					<c:if test="${sale_return ne true && sale_return ne 'true' && transaction ne 'estimate'}">
						<td>
							<insta:ltext key="salesissues.sales.details.discountauth"/>:&nbsp;&nbsp;
							<select name="discountAuthName" id="discountAuthName" class="dropdown" onchange="selectDiscountAuth();">
								<option value="">-- Select --</option>
								<c:forEach var="discAuth" items="${discountAuthorizers}">
									<option value="${discAuth.map.disc_auth_id}">${discAuth.map.disc_auth_name}</option>
								</c:forEach>
							</select>
							<input type="hidden" name="ratePlanDiscount" id="ratePlanDiscount" value=""/>
					     </td>
				     </c:if>
		     	</tr>
	     	</table>
	    </td>
		<td style="width: 20px">
			<c:choose>
				<c:when test="${not empty pbm_presc_id}">
					<button type="button" id="addButton" class="imgButton">
						<img src="${cpath}/icons/Add1.png">
					</button>
				</c:when>
				<c:otherwise>
					<button type="button" id="addButton" title=' <insta:ltext key="salesissues.sales.details.addnewitem"/>'
						onclick="openItemSearchDialog(this); return false;" accesskey="+" class="imgButton">
						<img src="${cpath}/icons/Add.png">
					</button>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>

</table>

<fieldset class="fieldSetBorder" style="margin-top: 5px">
	<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.billdiscountsandtotals"/></legend>
	<table width="100%">
		<tr>
			<td valign="top" width="40%">
				<table class="smallformtable">
					<tr>
						<td class="formlabel" style="width: 100px"><insta:ltext key="salesissues.sales.details.billdiscount"/>:</td>
						<td style="width: 200px">
							<select name="discType" tabindex="100" class="dropdown" style="width: 100px"
										onchange="onChangeBillDiscountType();">
								<option value="">${select}</option>
								<option value="percent-inc">% </option>
								<option value="amt" ><insta:ltext key="salesissues.sales.details.amount"/></option>
							</select>
							<input type="text" tabindex="-1" name="disPer" id="disPer" readonly="readonly"
								style="width: 45px"; onchange="onChangeDiscountPer()"/>%&nbsp;
							<input type="text" tabindex="-1" name="disAmt" id="disAmt" readonly="readonly"
								style="width: 55px"; onchange="onChangeDiscountAmt()"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel" style="width: 100px"><insta:ltext key="salesissues.sales.details.roundoff"/>:</td>
						<td>
							<input type="checkbox" tabindex="-1" name="roundoff" onclick="onChangeRoundOff();"
									${prefs.pharma_auto_roundoff == 'Y' ? 'checked' : ''}>
							<input type="hidden" name="roundOffAmt">
						</td>
					</tr>
				</table>
			</td>

			<td align="right" valign="top">
				<table width="500px" align="right" class="infotable">
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.billedamount"/>:</td>
						<td class="forminfo">
							<label id="lblBilledAmount">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.itemdiscounts"/>:</td>
						<td class="forminfo">
							<label id="lblItemDiscounts">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.itemtotal"/>:</td>
						<td class="forminfo">
							<label id="lblItemTotal">${defaultValue}</label>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.billdiscounts"/>:</td>
						<td class="forminfo">
							<label id="lblBillDiscount">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.roundoff"/>:</td>
						<td class="forminfo">
							<label id="roundOffAmt">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.billleveltotal"/>:</td>
						<td class="forminfo">
							<label id="lblBillLevelAmt">${defaultValue}</label>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.patientportion"/>:</td>
						<td class="forminfo">
							<label id="lblPatAmount">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.pri.claim"/>:</td>
						<td class="forminfo">
							<label id="lblpriClaimAmount">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.sec.claim"/>:</td>
						<td class="forminfo">
							<label id="lblsecClaimAmount">${defaultValue}</label>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.patienttaxamount"/>:</td>
						<td class="forminfo">
							<label id="lblPatTaxAmount">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.pri.taxclaim"/>:</td>
						<td class="forminfo">
							<label id="lblpriClaimTaxAmount">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.sec.taxclaim"/>:</td>
						<td class="forminfo">
							<label id="lblsecClaimTaxAmount">${defaultValue}</label>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.grandtotal"/>:</td>
						<td class="forminfo">
							<label id="grandTotal">${defaultValue}</label>
						</td>
						<td colspan="4" align="right">
						<span id="retiredEligibilityCheck"  style="display:none">
							<insta:externalLinks screenId="${screenId}" centerId="${centerId}" mrNo="${theForm.mrno.value}" onClickAction="javascript:initRetiredEligibilityDetailsDialog(); showRetiredEligibilityDetails();"/>
						</span>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</fieldset>

<div id="retiredEligibilityDetailsDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="sales.familypharmacyutil.dialog.retiredemployeeeligibilitydetails"/></legend>
		<table class="formtable">
		<thead><tr id="retiredDiagHeader">
				<th class="forminfo" style="width:10px;text-align:left;"><insta:ltext key="sales.familypharmacyutil.dialog.mrno"/></th>
				<th class="forminfo" style="width:10px;text-align:left;"><insta:ltext key="sales.familypharmacyutil.dialog.name"/></th>
				<th class="forminfo" style="width:10px;text-align:left;"><insta:ltext key="sales.familypharmacyutil.dialog.employeeid"/></th>
				<th class="forminfo" style="width:10px;text-align:right;"><insta:ltext key="sales.familypharmacyutil.dialog.utilizedamount"/></th>
		</tr>
		</thead>
		<tbody id="retiredEligibilityDetailsTable"></tbody>
		<tr><td colspan="4"><div><hr></div></td></tr>
        <tr>
        	<td colspan="3"  class="forminfo" style="width:10px;text-align:left;">
        		<insta:ltext key="sales.familypharmacyutil.dialog.totalmedicineamountutilized"/>
        	</td>
        	<td class="forminfo" style="width:10px;text-align:right;">
        		<insta:ltext key="sales.familypharmacyutil.dialog.rs"/>
        		<b id="totalmedicineamountutilized" ></b>
        	</td>
        </tr>
        <tr>
        	<td colspan="3"  class="forminfo" style="width:10px;text-align:left;">
        		<insta:ltext key="sales.familypharmacyutil.dialog.balancemedicinelimit"/>
        	</td>
        	<td class="forminfo" style="width:10px;text-align:right;">
        		<insta:ltext key="sales.familypharmacyutil.dialog.rs"/>
        		<b id="balancemedicinelimit"></b>
        	</td>
        </tr>
        <tr>
        	<td colspan="4" class="forminfo" align="right"><input type="button" value="${okText}" style="cursor:pointer;" onclick="handleRetiredEligibilityDetailsDialogCancel()"/>
        	</td>
        </tr>
		</table>
	</fieldset>
</div>
</div>

<c:set var="totalPh" value="0" />
<c:set var="advancePh" value="0" />
<c:set var="refundAmtPh" value="0" />
<c:set var="totAmtduePh" value="0" />

<div style="display:${transaction eq 'estimate' ? 'none' :'block' }">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.paymentdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">

			<tr>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.bill"/> <u><b><insta:ltext key="salesissues.sales.details.t"/></b></u><insta:ltext key="salesissues.sales.details.ype"/>:</td>
				<td class="forminfo">
					<select name="billType" accesskey="T" id="billType" disabled class="dropdown"
							onchange="onChangeBillType();enableBankDetails(document.getElementById('paymentModeId0'));">
						<option value="BN"><insta:ltext key="salesissues.sales.details.billnow"/></option>
						<option value="BL"><insta:ltext key="salesissues.sales.details.billlater"/></option>
					</select>
				</td>
				<td class="formlabel">
					<label id="remarksTd"><insta:ltext key="salesissues.sales.details.remarks"/>:</label>
				</td>
				<td class="forminfo">
					<label id="allUserRemarksTd">
						<input type="text" name="allUserRemarks" id="allUserRemarks"/>
					</label>
				</td>
				<td class="formlabel">
					<label id="narrationTd"><insta:ltext key="salesissues.sales.details.narration"/>:</label>
				</td>
				<td class="forminfo">
					<label id="paymentRemarksTd">
						<input type="text" name="paymentRemarks" id="paymentRemarks"/>
					</label>
				</td>
			</tr>
			<tr id="payRefsTr">
				<td colspan="6">
					<insta:billPaymentDetails formName="salesform" dateCounterRequired="false" 
					isBillNowPayment="true" isRefundPayment="${sale_return}"
					hasRewardPointsEligibility="false" availableRewardPoints="0"
					availableRewardPointsAmount="0" origBillStatus="" />
				</td>
			</tr>

			<tr>
			  <td colspan="4">
				<table width="100%" class="formtable">
					<tr id="deposit" style="display: none">
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.max.deposit.available"/>:</td>
						<td class="forminfo">
							<input type="hidden" id="depositsetoff" name="depositsetoff" style="width: 75px;"
								onchange="onChangeDepositSetOff();" onkeypress="return enterNumAndDot(event);">
							<c:if test="${!sale_return}">
								<b> <insta:ltext key="salesissues.sales.details.max"/>: <label id="maxAmtLabel" style="width: 55px;"></label></b>
							</c:if>
						</td>
						<c:if test="${sale_return}">
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.setoffinorigbill"/>:</td>
							<td class="forminfo" id="depositsetoffAmt"></td>
						</c:if>
					</tr>

					<tr id="ipdeposit" style="display:none">

						<td class="formlabel" ><insta:ltext key="salesissues.sales.details.max.deposit.available"/>:</td>
						<td class="forminfo" colspan="3">
							<input type="hidden" id="ipdepositsetoff" name="ipdepositsetoff" style="width: 75px;"
								onchange="onChangeIPDepositsetoff();" onkeypress="return enterNumAndDot(event);">
							<c:if test="${!sale_return}">
								<label id="ipDepAmtLabel" style="width: 55px;"></label>
								<input type="hidden" name="totAvlDep" id="totAvlDep"/>
								<%-- <insta:radio name="depositType" radioValues="i,g" value="i" radioText="IP Deposit,General Deposit" radioIds="I,G"
									onclick="return onChangeIPDepositsetoff();" /> --%>
							</c:if>
						</td>

					</tr>
				</table>
			 </td>
			 <td colspan="2">
				<table>
					<tr id="rewardPoints" style="display: none">
						<%-- <td class="formlabel"><insta:ltext key="salesissues.sales.details.rewardpointsredeemed"/>:</td> --%>
						<td class="forminfo">
							<input type="hidden" id="rewardPointsRedeemed" name="rewardPointsRedeemed" class="number"
								onchange="onChangeRewardPoints();" onkeypress="return enterNumAndDot(event);">
							<b> <insta:ltext key="salesissues.sales.details.max.reward.points"/>: <label id="maxRewardPointsLabel" style="width: 55px;"></label></b>
						</td>
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.pointsredeemedamount"/>:</td>
						<td class="forminfo">
							<input type="text" id="rewardPointsRedeemedAmount" name="rewardPointsRedeemedAmount"
								class="number" readOnly>
							<b> <insta:ltext key="salesissues.sales.details.eligible"/>: <label id="maxRewardPointsAmountLabel" style="width: 55px;"></label></b>
						</td>
					</tr>
				</table>
			  </td>
			</tr>
			<tr id="billDetailsInner" style="display: none">
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.ph.netamount"/>:</td>
				<td class="forminfo"><label id="lblPhBilled">${defaultValue}</label></td>

				<td class="formlabel"><insta:ltext key="salesissues.sales.details.ph.totalcredits"/>:</td>
				<td class="forminfo">
					<label id="lblPhCredits">${defaultValue}</label>
					<input type="hidden" id="billno" name="billno"/>
				</td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.ph.outstandingamt"/>:</td>
				<td class="forminfo"><label id="lblPhOutstanding">${defaultValue}</label></td>
			</tr>

			<c:if test="${prefShowHospAmts eq 'Y'}">
				<tr id="billDetailsInnerHosp" style="display: none">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.hosp.netamount"/>:</td>
					<td class="forminfo"><label id="lblTotBilled">${defaultValue}</label></td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.hosp.totalcredits"/>:</td>
					<td class="forminfo"><label id="lblTotalCredits">${defaultValue}</label></td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.hosp.outstandingamt"/>:</td>
					<td class="forminfo"><label id="lblHospOutstanding">${defaultValue}</label></td>
				</tr>
			</c:if>
		</table>
	</fieldset>
</div>

<div class="screenActions" style="float:left;display:inline">
	<button type="button" id="PayAndPrint" name="save" accesskey="P" class="button" onclick="return onClickSave()"><label> <u><b><insta:ltext key="salesissues.sales.details.p"/></b></u><insta:ltext key="salesissues.sales.details.ay"/> &amp; <insta:ltext key="salesissues.sales.details.print"/></label></button>
	<button type="button" id="reset" class="button" onclick="return onClickReset();"><label><insta:ltext key="salesissues.sales.details.clear"/></label></button>
	<span style="display:none" id="retailCreditPaymentDiv" >
		| <a href='./RetailpendingSalesBill.do?_method=getRetailPendingSaleList' onfocus="creditRetail(this)" target="_blank"><insta:ltext key="salesissues.sales.details.retailcreditpayment"/></a>
	</span>
	<c:if test="${!mod_eclaim_erx && mod_eclaim_pbm}">
		<span style="display:none;float:right;padding-top:2px;" id="addNewPBMPrescDiv" >
		<insta:screenlink target="_blank" screenId="pbm_presc"
			extraParam="?_method=getPBMPrescriptionScreen"
			label="Add New PBM Prescription" addPipe="true"/>
		</span>
	</c:if>
</div>

<div class="legend">
<table class="legend" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
		<c:choose>
			<c:when test="${!sale_return}">
				<td class="formlabel" align="right"><insta:ltext key="salesissues.sales.details.billpresc"/>:</td>
			</c:when>
			<c:otherwise>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.billprint"/>: </td>
			</c:otherwise>
		</c:choose>
		<td class="forminfo">
		<insta:selectdb name="printerType" table="printer_definition"
			valuecol="printer_id" displaycol="printer_definition_name"
			value="${bean.map.printer_id}" />
		</td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<c:if test="${transaction ne 'estimate'}">
		<c:if test="${!sale_return}">
			<c:if test="${salePrintItems eq 'BILLPRESCLABEL'  || salePrintItems eq 'BILLLABEL'}">
			<tr>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel"><insta:ltext key="salesissues.sales.details.billpresclabel"/>:</td>
				<td class="forminfo">
				<insta:selectdb name="labelPrinterType" table="printer_definition"
					valuecol="printer_id" displaycol="printer_definition_name"
					value="${prescBean.map.printer_id}" />
			   </td>
			</tr>
			</c:if>
		</c:if>
	</c:if>
</table>
</div>
</form>
<div style="clear:both"></div>
<div class="legend">

	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText"><insta:ltext key="salesissues.sales.details.expireditem"/></div>
	<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
	<div class="flagText"><insta:ltext key="salesissues.sales.details.itemnearingexpiry"/></div>
	<div class="flag"><img src='${cpath}/images/brown_flag.gif'></div>
	<div class="flagText">Sale quantity exceeds insurance validity</div>
	<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
	<div class="flagText"><insta:ltext key="salesissues.sales.details.notcoverdbyprimaryinsurance"/></div>
</div>

<div id="loginDiv" style="display: none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.logindetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.userid"/>: </td>
					<td><input type="text" name="login_user" id="login_user"/></td>
					<td class="formlabel">&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.password"/>: </td>
					<td><input type="password" name="login_password" id="login_password" onkeypress="return submitOnEnter(event);"/></td>
					<td class="formlabel">&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td><input type="button" name="submitForm" id="submitForm" value="Submit" />
						<input type="button" name="cancelSubmit" id="cancelSubmit" value="Cancel" />
					</td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>



<form name="editForm">
<div id="edititemdialog" style="display: none; width: 500px;" >
		<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="salesissues.sales.details.edititemdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<td class="formlabel">
					<insta:ltext key="salesissues.sales.details.item"/>:</td>
					<td class="forminfo">
						<label id="item_name"></label>
						<input type="hidden" name="medicineId">
						<input type="hidden" name="medPkgsize">
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.batch"/>:</td>
					<td class="yui-skin-sam">
						<div id="batch_wrapper">
							<input type="text" name="batch_no" id="edit_batch_no" style="width: 11.5em" >
							<div id="edit_batch_dropdown"></div>
						</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.mrp.pkg.in.brackets"/>:</td>
					<td >
						<input type="text" name="mrp" disabled onchange="onChangeEditAmounts();"/>
						<input type="hidden" name="ed_visit_selling_price" id="ed_visit_selling_price"/>
						<input type="hidden" name="ed_store_selling_price" id="ed_store_selling_price"/>
						<input type="hidden" name="ed_selling_price_hid" id="ed_selling_price_hid"/>
					</td>
					<c:choose>
						<c:when test="${not empty pbm_presc_id}">
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.issue.qty"/>:</td>
							<td><input type="text" name="issueQty" readOnly="readOnly" /></td>
						</c:when>
						<c:otherwise>
							<td></td><td></td>
						</c:otherwise>
					</c:choose>
				</tr>
				<tr id="costPriceRow">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.costprice.pkg"/>:</td>
					<td>
						<input type="text" name="cp" disabled onchange="onChangeEditAmounts();"/>
					</td>
					<td></td><td></td>
				</tr>
				<tr>
					<td class="formlabel">${sale_return? returnText:saleText} <insta:ltext key="salesissues.sales.details.qty"/>:</td>
					<td>
						<input type="text" name="saleqty" ${not empty pbm_presc_id ? 'readOnly' : ''}
						onchange="onChangeEditAmounts();"/>
					</td>
					<td class="formlabel">${sale_return? returnText:saleText} <insta:ltext key="salesissues.sales.details.uom"/>:</td>
					<td>
						<select class="dropdown" name="eSaleUnit" onchange="onChangeEditAmounts();"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.discount.percentage"/>:</td>
					<td>
						<input type="text" name="discountper" onchange="onChangeEditDiscount();"/>
					</td>
					<input type="hidden" name="discounttype" value="I">
				</tr>
				<tr id="edit_tax_groups"></tr>
				<tr id="prim_pre_auth_row">
					<td class="formlabel" style="padding-left:20px;"><insta:ltext key="salesissues.sales.details.primarypriorauthno"/>:</td>
					<td class="forminfo">
						<input type="text" name="editPrior_auth_id" id="editPrior_auth_id" />
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.primarypriorauthmode"/>:</td>
					<td class="forminfo">
						<insta:selectdb  id="editPrior_auth_mode_id" name="editPrior_auth_mode_id" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="${select}"/>
					</td>
				</tr>
				<tr id="prim_claim_row">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.primaryclaimamt"/>:</td>
					<td class="forminfo">
						<input type="text" id="edlg_claim_amt" name="edlg_claim_amt" ${not empty pbm_presc_id ? 'readOnly' : ''}
								onChange="onChangeClaimAmt();"/>
						<input type="hidden" name="ed_priIncludeInClaim" value="" id="ed_priIncludeInClaim">
						<input type="hidden" id="ed_priInsClaimTaxAmt"/>
					</td>
					<c:if test="${not empty pbm_presc_id}">
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.status"/>:</td>
						<td class="forminfo">
							<label id="editPBM_status"> </label>
						</td>
					</c:if>
				</tr>
				<c:if test="${mod_eclaim_erx}">
					<tr id="ed_erxReferenceRow">
						<td class="formlabel"><insta:ltext key="patient.discharge.medication.erx.request.info.erx.activity.id"/>:</td>
						<td class="forminfo" colspan="2">
							<input type="text" name="ed_erx_activity_id" id="ed_erx_activity_id" maxlength="50" style="width: 15em;"/>
						</td>
					</tr>
				</c:if>

				<tr id="sec_pre_auth_row"  style="display:none;">
					<td class="formlabel" style="padding-left:20px;"><insta:ltext key="salesissues.sales.details.secondarypriorauthno"/>:</td>
					<td class="forminfo">
						<input type="text" name="editPrior_auth_id" id="editPrior_auth_id" />
					</td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.secondarypriorauthmode"/>:</td>
					<td class="forminfo">
						<insta:selectdb  id="editPrior_auth_mode_id" name="editPrior_auth_mode_id" value="" table="prior_auth_modes" valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false" dummyvalue="${select}"/>
					</td>
				</tr>
				<tr id="sec_claim_row" style="display:none;">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.secondaryclaimamt"/>:</td>
					<td class="forminfo">
						<input type="text" id="edlg_claim_amt" name="edlg_claim_amt" ${not empty pbm_presc_id ? 'readOnly' : ''}
								onChange="onChangeClaimAmt();"/>
						<input type="hidden" name="ed_secIncludeInClaim" value="" id="ed_secIncludeInClaim">
						<input type="hidden" id="ed_secInsClaimTaxAmt" />
					</td>
					<c:if test="${not empty pbm_presc_id}">
						<td class="formlabel"><insta:ltext key="salesissues.sales.details.status"/>:</td>
						<td class="forminfo">
							<label id="editPBM_status"> </label>
						</td>
					</c:if>
				</tr>
				<tr id="ins_amt_row">
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.totalamount"/>:</td>
					<td class="forminfo"><label id="edlg_amt"></label></td>
					<td class="formlabel"><insta:ltext key="salesissues.sales.details.patientamt"/>:</td>
					<td class="forminfo">
						<label id="edlg_pat_amt"></label>
					</td>
				</tr>
				<tr id="coverdbyinsurancestatus">
					<td class="formLabel">
						<insta:ltext key="salesissues.sales.details.coverdbyinsurance"/>:
					</td>
					<td class="formInfo" colspan="2">
						<b><label id="ed_coverdbyinsurance" ></label></b>
						<input type="hidden" name="ed_coverdbyinsuranceflag" value="" id="ed_coverdbyinsuranceflag">
					</td>
					
				</tr>
				<tr id="isDoctorExcluded">
                        <td class="formLabel">
                            <insta:ltext key="salesissues.sales.details.isDoctorExcluded"/>:
                        </td>
                        <td class="formInfo" >
                            <b><label id="ed_isDoctorExcluded" ></label></b>
                            <input type="hidden" name="ed_isDoctorExcludedFlag" value="" id="ed_isDoctorExcludedFlag">
                        </td>

                        <td class="formLabel">
                            <insta:ltext key="salesissues.sales.details.doctorExclusionRemarks"/>:
                        </td>
                        <td class="formInfo">
                            <b><label id="ed_doctorExclusionRemarks" ></label></b>
                            <input type="hidden" name="ed_doctorExclusionRemarksValue" value="" id="ed_doctorExclusionRemarksValue">
                        </td>

                </tr>

			</table>
		</fieldset>
		<div class="title CollapsiblePanelTab" id="editManagementAccordion" onclick="return editToggleManagement()" style="margin-bottom:10px;font-weight:bold;">
			<div style="float:left;clear: none;display:inline-block;margin:5px 0 0 10px;">
				<span style="color:black;float:left;width:650px;" id="edittitle">Management
				<img src="${cpath}/images/up.png" id="editup" style="display:none;float:right;"/>
				<img src="${cpath}/images/down.png" id="editdown" style="display:block;float:right;"/></span>
			</div>
		</div>
		<div id="presEdit" style="display: none">
		<div id="editPresMang" style="display:none;">
			<fieldset class="fieldSetBorder">
				<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.dosage"/>: </td>
							<td colspan="3">
								<input type="text" name="ed_dosage" id="ed_dosage" value="" onchange="; setEdited();"/>
								<input type="text" name="ed_consumption_uom" id="ed_consumption_uom" value="" onchange="modifyUOMLabel(this, 'ed'); setEdited();"/> Units
							</td>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.frequency"/>:</td>
							<td>
								<div id="ed_frequencyAutoComplete" style="width: 138px;">
									<input type="text" name="ed_frequency" id="ed_frequency" value="" maxlength="150"/>
									<input type="hidden" name="ed_frequency_hidden" id="ed_frequency_hidden" value=""/>
									<input type="hidden" name="ed_per_day_qty" id="ed_per_day_qty" value=""/>
									<div id="ed_frequencyContainer" style="width: 300px;"></div>
								</div>
							</td>
						</tr>
							<tr>
								<td class="formlabel"><insta:ltext key="salesissues.sales.details.duration"/>: </td>
								<td>
									<input type="text" name="ed_duration" id="ed_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onblur="calcQty(event, 'ed'); ;"/>
								</td>
								<td colspan="2" >
									<div style="width: 190px">
										<input type="radio" name="ed_duration_units" value="D" onchange="calcQty(event, 'ed');">Days
										<input type="radio" name="ed_duration_units" value="W" onchange="calcQty(event, 'ed');">Weeks
										<input type="radio" name="ed_duration_units" value="M" onchange="calcQty(event, 'ed');">Months
									</div>
								</td>
								<td class="formlabel"/>
								<td></td>
							</tr>
							<tr>
		
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.totalqty"/>: </td>
							<td >
								<input type="text" name="ed_qty" id="ed_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited();"/>
							</td>
							<td colspan="2"><label id="ed_consumption_uom_label"></label></td>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.routeOfAdmin"/>:</td>
							<td><input type="text" name ="ed_routeOfAdmin"  id="ed_routeOfAdmin" value="" title=""/>
								<input type="hidden" name="routeOfAdmin" value=""/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.doctorremarks"/>:</td>
							<td colspan="5">
								<div id="ed_remarksAutoComplete" style="width: 500px">
									<input type="text" name="ed_doc_remarks" id="ed_doc_remarks" value="" style="width: 500px" onchange="setEdited();">
									<div id="ed_remarksContainer" class="scrolForContainer"></div>
								</div>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.specialInstructions"/>: </td>
							<td colspan="5"><textarea name="ed_special_instruction" id="ed_special_instruction" style="width: 500px;" cols="50" rows="2" onchange="setEdited();"></textarea></td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.otherremarks"/>:</td>
							<td colspan="5">
							<input type="text" name="ed_remarks" id ="ed_remarks" style="width: 500px;" onchange="onChangeEditAmounts();"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="salesissues.sales.details.warninglabel"/>:</td>
							<td>
								<insta:selectdb name="ewarn_label" id ="ewarn_label" table="label_master"
									valuecol="label_id" displaycol="label_short" filtercol="status" filtervalue="A"
												 dummyvalue="-- Select --" dummyvalueId=""/>
							</td>
						</tr>
				</table>
			</fieldset>
		</div>
	</div>
	<table>
		<tr>
			<td >
				<input type="button" value="${okText}" onclick="closeEditDialogBox();"/>
				<input type="button" value="Cancel" onclick="cancelEditDialogBox();"/>
			</td>
		</tr>
	</table>
	</div>
</div>

<div id="patientPlanDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientPlanDetailsDialogCancel();">
		<div class="bd" id="bd1" style="padding-top: 0px;">
			<table class="formTable" align="center" id="pd_planDialogTable" style="width:480px;">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
						<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patientdetails.common.tag.plansummary"/></legend>
								<table class="formTable" align="center" style="width:480px;">
									<tr>
										<td>
											<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
												<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.exclusions"/></legend>
													<p id="plan_exclusions" style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">
														${fn:replace('<label id="plan_exclusions"></label>', newLineChar, "<br />")}
													</p>										
											 </fieldset>
										</td>
									</tr>
									<tr>
										<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.notes"/></legend>
												<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">
												 	${fn:replace('<label id="plan_notes"></label>', newLineChar, "<br />")}
												</p>									
										</fieldset>
										</td>
									</tr>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientPlanDetailsDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="patientSecPlanDetailsDialog" style="display:none;visibility:hidden;" ondblclick="handlePatientSecPlanDetailsDialogCancel();">
		<div class="bd" id="bd1" style="padding-top: 0px;">
			<table class="formTable" align="center" id="pd_secplanDialogTable" style="width:480px;">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="width:480px;white-space: normal;">
						<legend class="fieldSetLabel" style="white-space: normal;"><insta:ltext key="patientdetails.common.tag.plansummary"/></legend>
								<table class="formTable" align="center" style="width:480px;">
									<tr>
										<td>
											<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
												<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.exclusions"/></legend>					
													<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;">
														${fn:replace('<label id="sec_plan_exclusions"></label>', newLineChar, "<br />")}
													</p>										
											 </fieldset>
										</td>
									</tr>
									<tr>
										<td>
										<fieldset class="fieldSetBorder" style="width:450px;white-space: normal;">
											<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.plan.notes"/></legend>
												<p style="width:450px;word-wrap:break-word;line-height: 14px;white-space: normal;"> 
													${fn:replace('<label id="sec_plan_notes"></label>', newLineChar, "<br />")}
												</p>	
										</fieldset>
										</td>
									</tr>
								</table>
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientSecPlanDetailsDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="patientPhotoDialog" style="display:none;visibility:hidden;" width="630%" ondblclick="handlePatientPhotoDialogCancel();">
		<div class="bd" id="bd2" style="padding-top: 0px;">
			<table  style="text-align:top;vetical-align:top;" width="100%">
				<tr>
					<td>
						<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
							<legend class="fieldSetLabel"><insta:ltext key="patientdetails.common.tag.patient.photo"/></legend>
								<img id="pd_patientImage" alt="No Patient Photo Available" height="354px" width="500px" src="" />			
						 </fieldset>
					</td>
				</tr>
				<tr>
					<td align="left">
						<input type="button" value="Close" style="cursor:pointer;" onclick="handlePatientPhotoDialogCancel();"/>
					</td>
				</tr>
			</table>
		</div>
	</div> 
	<div id="primaryInsurancePhotoDialog" style="display:none;visibility:hidden; "ondblclick="handlePrimaryInsurancePhotoDialogCancel();">
	<div class="bd" id="bd2" style="padding-top: 0px;">
		<table style="text-align:top;vertical-align:top;" width="100%">
			<tr>
				<td>
				<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
					<legend class="fieldSetLabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.primarysponsordocument"/></legend>
						<img id="pinsuranceImage" alt="No Uploaded Document Available" height="450px" width="500px" style="overflow:auto" src="" />
				</fieldset>
				</td>
			</tr>
			<tr>
				<td align="left"><input type="button" value="Close" style="cursor:pointer;"
					onclick="handlePrimaryInsurancePhotoDialogCancel();"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="secondaryInsurancePhotoDialog" style="display:none;visibility:hidden;"
	ondblclick="handlePrimaryInsurancePhotoDialogCancel();">
	<div class="bd" id="sbd2" style="padding-top: 0px;">
		<table style="text-align:top;vertical-align:top;" width="100%">
			<tr>
				<td>
				<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
					<legend class="fieldSetLabel"><insta:ltext key="billing.changetpa.add.editpatientinsurance.secondarysponsordocument"/></legend>
						<img id="sinsuranceImage" alt="No Uploaded Document Available" height="450px" width="500px" style="overflow:auto" src="" />
				</fieldset>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="Close" style="cursor:pointer;"
					onclick="handlesecondaryInsurancePhotoDialogCancel();" />
				</td>
			</tr>
		</table>
	</div>
</div>

</form>
	<script>
	//displays list of medicine names crossing pbmPriceThreshold set in genPrefs
	if(medicinesNameList!= "")
		alert("There is a large difference between requested and approved price for" + medicinesNameList );
	</script>
 <jsp:include page="/pages/dialogBox/processPaymentDialog.jsp"></jsp:include>
 <jsp:include page="/pages/dialogBox/loyaltyDialog.jsp"></jsp:include>
</body>
</html>
