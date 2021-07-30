<%@ page import="com.insta.hms.core.insurance.URLRoute"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<% 
    String userid = (String) session.getAttribute("userid");
%>
<c:set var="bill" value="${billDetails.bill}"/>
<c:set var="points_redemption_rate" value="${not empty genPrefs.points_redemption_rate ? genPrefs.points_redemption_rate : 0}"/>
<c:set var="no_of_credit_debit_card_digits" value="${genPrefs.no_of_credit_debit_card_digits}"/>
<c:set var="billLabelForBillLaterBills" value="${genPrefs.billLabelForBillLaterBills}"/>
<c:set var="incomeTaxCashLimitApplicability" value="${genPrefs.incomeTaxCashLimitApplicability}"/>
<c:set var="priSponsorAmt" value="${pri_sponsor_amt}"/>
<c:set var="secSponsorAmt" value="${sec_sponsor_amt}"/>
<c:set var="primarySponsorsReceipt" value="${pri_sponsors_receipt}"/>
<c:set var="secondarySponsorsReceipt" value="${sec_sponsors_receipt}"/>
<c:set var="priRecdAmt" value="${pri_recd_amt}"/>
<c:set var="secRecdAmt" value="${sec_recd_amt}"/>
<c:set var="cashTransactionLimitAmt" value="${cashTransactionLimit}"/>
<c:set var="existingReceipts" value="${bill.totalReceipts}"/>
<c:set var="existingRecdAmount" value="${bill.claimRecdAmount}"/>
<c:set var="totalAmount" value="${bill.totalAmount}" />
<c:set var="existingSponsorReceipts" value="${bill.totalPrimarySponsorReceipts + bill.totalSecondarySponsorReceipts}"/>
<c:set var="availableDeposits" value='${depositDetails.map.total_deposits -	depositDetails.map.total_set_offs}'/>
<c:set var="totalDeposit" value='${depositDetails.map.total_deposits}'/>
<c:set var="patientCredit" value='${creditNoteDetails.total_amount - creditNoteDetails.total_claim}'/>
<c:set var="ipDeposits" value='${ipDepositDetails.map.total_ip_deposits - ipDepositDetails.map.total_ip_set_offs - ipDepositDetails.map.total_ip_set_offs_non_ip_bill}'/>
<c:set var="ipDepositsSetOffNonIpBills" value='${ipDepositDetails.map.total_ip_set_offs_non_ip_bill}'/>
<c:set var="availableRewardPoints"
	value='${rewardPointDetails.map.total_points_earned - rewardPointDetails.map.total_points_redeemed - rewardPointDetails.map.total_open_points_redeemed}'/>
<c:set var="availableRewardPointsAmount" value="${availableRewardPoints * points_redemption_rate}"/>
<c:set var="userIsallowedtoEditOpenDate" value="${actionRightsMap.allow_edit_bill_open_date == 'A' || roleId == 1 || roleId ==2}"/>
<c:set var="allowEasyRewardzCouponRedemption" value="${actionRightsMap.allow_easyrewardz_coupon_redemption == 'A' || roleId == 1 || roleId ==2}"/>
<c:set var='easyRewardzModule' value="${preferences.modulesActivatedMap['mod_easy_rewards_coupon'] eq 'Y'}"/>
<c:set var='eClaimModule' value="${preferences.modulesActivatedMap['mod_eclaim'] eq 'Y'}"/>
<c:set var='pendingPrescriptionModule' value="${preferences.modulesActivatedMap['mod_pat_pending_prescription'] eq 'Y'}"/>
<c:set var='isMessagingModule' value="${preferences.modulesActivatedMap['mod_messaging'] eq 'Y'}"/>
<c:set var="createSponsorCreditNote" value='${urlRightsMap.patient_writeoff == "A"}'/>
<c:set var="permissibleDiscountPercenatge" value ='${permissibleDiscountPercenatge}'/>
<c:set var="allowBillFinalization" value ='${allowBillFinalization}'/>
<c:set var="dischargeType">
	<c:choose>
		<c:when test="${bill.billType == 'P'}">none</c:when>
		<c:when test="${bill.visitType == 'i' && preferences.modulesActivatedMap['mod_adt'] eq 'Y'}">adt</c:when>
		<c:when test="${bill.isPrimaryBill == 'N'}">none</c:when>
		<c:otherwise>bill</c:otherwise>
	</c:choose>
</c:set>
<c:set var="billPrintRights" value="${urlRightsMap.bill_print}"/>
<c:set var="editEmailRights" value="${urlRightsMap.bill_email}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:useBean id="currentDate" class="java.util.Date"/>

<jsp:useBean id="claimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${claimStatusDisplay}" property="O" value="Open"/>
<c:set target="${claimStatusDisplay}" property="B" value="Batched"/>
<c:set target="${claimStatusDisplay}" property="C" value="Closed"/>
<c:set target="${claimStatusDisplay}" property="M" value="Marked For Resubmission"/>

<%@page import="flexjson.JSONSerializer"%>
<html>
<head>
	<title><insta:ltext key="billing.patientbill.details.billtitle"/> ${ifn:cleanHtml(bill.billNo)} - <insta:ltext key="billing.patientbill.details.instahms"/></title>
	<c:if test="${isNewUX == 'Y'}">
		<script>
			window.name = "instaOldBilling";
			// this variable is used taxations.tag
			var itemGroupList = ${itemGroupListJson};
			var itemSubGroupList = ${itemSubGroupListJson};
		</script>
	</c:if>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>

	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<%@ page import="com.insta.hms.billing.BillDetails" %>
	<%@ page import="java.util.ArrayList" %>
	<c:set var="separator_type" value='<%=GenericPreferencesDAO.getAllPrefs().get("separator_type")%>'/>
	<c:set var="bill_cancellation_requires_approval" value='<%=GenericPreferencesDAO.getAllPrefs().get("bill_cancellation_requires_approval")%>'/>
	<c:set var="currency_format" value='<%=GenericPreferencesDAO.getAllPrefs().get("currency_format")%>'/>
	<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />
	

	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing.js"/>
	<insta:link type="script" file="billingDynaPkg.js"/>
	<insta:link type="script" file="billingPerdiem.js"/>
	<insta:link type="script" file="billPaymentCommon.js"/>
	<insta:link type="script" file="sockjs.min.js"/>
	<insta:link type="script" file="stomp.min.js"/>
	<insta:link type="script" file="Insurance/insuranceCalculation.js"/>
	<insta:link type="script" file="billing/billingtax.js" />
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="orderdialog.js" />
	<insta:link type="script" file="ordertable.js" />
	<insta:link type="script" file="signature_pad.min.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="css" file="email.css"/>
	<insta:link type="js" file="dashboardsearch.js" />
	<insta:link type="js" file="doctorConsultations.js" />
	<insta:link type="js" file="billing/billing_discount_rule.js" />
	<insta:link type="js" file="instadate.js"/>
	<c:if test="${isNewUX == 'Y'}">
		<insta:link type="css" file="billing/newBillingUX.css"/>
	</c:if>
	

	<%-- send directBilling as empty if we have rights for billing anything, otherwise, send it as Y
	so that we only get items which are set to directBilling=Y --%>
	<c:set var="directBilling"
		value="${(actionRightsMap.addtobill_charges != 'N' || roleId == 1 || roleId ==2) ? '' : 'Y'}"/>

	<c:set var="isInsuranceBill" value="${bill.is_tpa && (bill.restrictionType == 'N' || bill.restrictionType == 'P')}"/>
	<c:set var="isNonInsuredBillOfInsPatient" value="${patient.primary_sponsor_id != null && patient.primary_sponsor_id != '' && patient.org_id != bill.billRatePlanId && !bill.is_tpa}" />

	<c:set var="orderItemsUrl" value="${cpath}/patients/orders/getorderableitem.json?"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&direct_billing=${directBilling}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&org_id=${patient.org_id}&visit_type=${patient.visit_type}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&center_id=${patient.center_id}"/>
	<c:if test="${isInsuranceBill}">
		<c:set var="orderItemsUrl" value="${orderItemsUrl}&tpa_id=${patient.primary_sponsor_id}&plan_id=${patient.plan_id}"/>	
	</c:if>
	<c:if test="${!isInsuranceBill}">
		<c:set var="orderItemsUrl" value="${orderItemsUrl}&tpa_id=0&tpa_id=-1&plan_id=0&plan_id=-1"/>	
	</c:if>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&dept_id=${patient.dept_id}&gender_applicability=${patient.patient_gender}"/>
	<c:if test="${not empty patient.raw_age_text}">
		<c:set var="orderItemsUrl" value="${orderItemsUrl}&age_text=${patient.raw_age_text}"/>	
	</c:if>
	<c:if test="${empty patient.raw_age_text and not empty patien.age_text}">
		<c:set var="orderItemsUrl" value="${orderItemsUrl}&age_text=${patient.age_text}"/>	
	</c:if>
	<c:choose>
		<c:when test="${isNewUX == 'Y'}">
			<c:set var="newUXExtraParam" value="&isNewUX=Y"/>
		</c:when>
		<c:otherwise>
			<c:set var="newUXExtraParam" value="" />
		</c:otherwise>
	</c:choose>

	<style type="text/css">
		table.infotable td.forminfo { width: 40px; text-align: right }
		table.infotable td.formlabel { width: 110px; text-align: right }
		select.filterActive { color: blue; }
		.scrollForContainer .yui-ac-content{
			 overflow:auto;overflow-x:auto;width:400px;  /* scrolling */
		}
		select.billLabel {color: red;}
		.package-component-details {
			display: none;
		}
		
		//Hide the chargesTable on load.
		#chargesTable { display: none; }

	</style>

	<insta:js-bundle prefix="billing.billlist"/>
	<insta:js-bundle prefix="billing.salucro"/>
	<insta:js-bundle prefix="billing.dynapackage"/>
	<insta:js-bundle prefix="billing.billingprediem"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<insta:js-bundle prefix="order.common"/>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="init();ajaxForPrintUrls();loadLoyaltyDialog();loadLoyaltyOffersDialog();loadProcessPaymentDialog();filterPaymentModes();" class="yui-skin-sam">
  <div class="hidden-header-scroll-area"></div>
<c:set var="primaryclaimstatus">
<insta:ltext key="billing.patientbill.details.open"/>,
<insta:ltext key="billing.patientbill.details.sent"/>,
<insta:ltext key="billing.patientbill.details.closed"/>
</c:set>
<c:set var="paymentstatus">
<insta:ltext key="billing.patientbill.details.unpaid"/>,
<insta:ltext key="billing.patientbill.details.paid"/>
</c:set>
<c:set var="filterpackage">
<insta:ltext key="billing.patientbill.details.included"/>,
<insta:ltext key="billing.patientbill.details.excluded"/>
</c:set>
<c:set var="editbillremittance">
<insta:ltext key="billing.patientbill.details.editbillremittance"/>
</c:set>
<c:set var="editpharmacyitemamount">
<insta:ltext key="billing.patientbill.details.editpharmacyitemamount"/>
</c:set>
<c:set var="editclaimtitle">
<insta:ltext key="billing.patientbill.details.editclaim"/>
</c:set>
<c:set var="editpatientinsurancetitle">
<insta:ltext key="billing.patientbill.details.editpatientinsurance"/>
</c:set>
<c:set var="addpatientinsurancetitle">
<insta:ltext key="billing.patientbill.details.addpatientinsurance"/>
</c:set>
<c:set var="changeprimarytitle">
<insta:ltext key="billing.patientbill.details.changeprimary"/>
</c:set>
<c:set var="changerateplantype">
<insta:ltext key="billing.patientbill.details.changerateplan.bedtype"/>
</c:set>
<c:set var="tobillater">
<insta:ltext key="billing.patientbill.details.tobilllater"/>
</c:set>
<c:set var="disconnectins">
<insta:ltext key="billing.patientbill.details.disconnectinsurance"/>
</c:set>
<c:set var="connectins">
<insta:ltext key="billing.patientbill.details.connectinsurance"/>
</c:set>
<c:set var="priclaimamt">
<insta:ltext key="billing.patientbill.details.prisponsor.or.claimamount"/>
</c:set>
<c:set var="secclaimamt">
<insta:ltext key="billing.patientbill.details.secsponsor.or.claimamount"/>
</c:set>
<c:set var="sponserclaimamt">
<insta:ltext key="billing.patientbill.details.sponsor.or.claimamount"/>
</c:set>
<c:set var="patientamt">
<insta:ltext key="billing.patientbill.details.patientamount"/>
</c:set>
<c:set var="code">
<insta:ltext key="billing.editpharmacyitemamounts.items.code"/>
</c:set>
<c:set var="all">
<insta:ltext key="billing.patientbill.details.all"/>
</c:set>
<c:set var="all2">
<insta:ltext key="billing.patientbill.details.all.in.brackets"/>
</c:set>
<c:set var="all1">
<insta:ltext key="billing.patientbill.details.included"/>,
<insta:ltext key="billing.patientbill.details.excluded"/>
</c:set>
<c:set var="addbutton">
<insta:ltext key="billing.patientbill.details.add"/>
</c:set>
<c:set var="applybutton">
<insta:ltext key="billing.patientbill.details.apply"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<form name="billNoForm" action="BillAction.do">
	<input type="hidden" name="buttonAction" value="save">
	<input type="hidden" name="_method" value="getCreditBillingCollectScreen">
	<input type="hidden" name="plan_id" value="${plan_id}">
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="billing.patientbill.details.patientbill"/></h1></td>

			<td><insta:ltext key="billing.patientbill.details.bill"/>&nbsp;<insta:ltext key="billing.patientbill.details.no"/>:&nbsp;</td>
			<td><input type="text" name="billNo" id="billNo" style="width: 80px"></td>
			<td><input name="getDetails" type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<div><insta:feedback-panel/></div>

<c:if test="${bill.visitType == 'r'}">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.customerdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
			<tr>
				<td><insta:ltext key="billing.patientbill.details.customername"/>:</td>
				<td class="forminfo">${retailCustomer.customer_name}</td>
				<td><insta:ltext key="billing.patientbill.details.sponsor"/>:</td>
				<td class="forminfo">${retailCustomer.sponsor_name}</td>
			</tr>
		</table>
	</fieldset>
</c:if>

<c:if test="${bill.visitType == 't'}">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.incomingtestdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
			<tr>
				<td><insta:ltext key="billing.patientbill.details.hospitalname"/>:</td>
				<td class="forminfo">${incomingCustomer.map.hospital_name}</td>
				<td><insta:ltext key="ui.label.patient.name"/>:</td>
				<td class="forminfo">${incomingCustomer.map.patient_name}</td>
				<td><insta:ltext key="billing.patientbill.details.age.or.gender"/>:</td>
				<td class="forminfo">${incomingCustomer.map.age_text}${fn:toLowerCase(incomingCustomer.map.age_unit)} / ${incomingCustomer.map.gender}</td>
			</tr>
			<c:if test="${bill.billRatePlanName != 'GENERAL' }">
			<tr>
				<td><insta:ltext key="billing.patientbill.details.rateplan"/>:</td>
				<td class="forminfo">${ifn:cleanHtml(bill.billRatePlanName)}</td>
			</tr>
			</c:if>
		</table>
	</fieldset>
</c:if>
<div class="">
	<c:if test="${not (bill.visitType == 'r' || bill.visitType == 't')}">
		<insta:patientdetails patient="${patient}" />
	</c:if>
</div>

<form name="mainform" method="post" action="BillAction.do" autocomplete="off">
<input type="hidden" name="fieldImgText" id="fieldImgText" value=""/>
<input type="hidden" name="fieldImgSrc" id="fieldImgSrc" value="" />
<input type="hidden" name="_method" value="saveBillDetails">
<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(bill.billNo)}">
<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(isNewUX)}" />
<input type="hidden" name="buttonAction" value="save">
<input type="hidden" name="billDiscountAuth" id="billDiscountAuth" value="${bill.billDiscountAuth}"/>
<input type="hidden" name="billDiscountCategory" id="billDiscountCategory" value="${bill.billDiscountCategory}"/>
<input type="hidden" name="billOpenedDate" value="<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>">
<input type="hidden" name="billOpenedTime" value="<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/>">
<input type="hidden" name="modTime" value="${bill.modTime.getTime()}"/>
<input type="hidden" name="secondarySponsorExists" value="${not empty patient.secondary_sponsor_id ? 'Y' :'N'}"/>

<input type="hidden" name="paymentForceClose" value="N"/>
<input type="hidden" name="claimForceClose" value="N"/>
<input type="hidden" name="eClaimModule" value="${eClaimModule}"/>

<input type="hidden" name="dynaPkgProcessed" value="${ifn:cleanHtmlAttribute(bill.dynaPkgProcessed)}"/>

<input type="hidden" name="mrNo" value="${patient.mr_no}">
<input type="hidden" name="mrno" id="mrno" value="${patient.mr_no}">
<input type="hidden" name="nok_contact"  value="${patient.patcontactperson}">
<c:choose>
	<c:when test="${bill.visitType == 't'}">
		<input type="hidden" name="visitId" id="visitId" value="${incomingCustomer.map.incoming_visit_id}">
	</c:when>
	<c:otherwise>
		<input type="hidden" name="visitId" id="visitId" value="${patient.patient_id}">
	</c:otherwise>
</c:choose>

<input type="hidden" name="organizationId" id="organizationId" value="${patient.org_id}">
<input type="hidden" name="bedType" id="bedType" value="${patient.bill_bed_type}">
<input type="hidden" name="doctorId" id="doctorId" value ="${patient.doctor}"/>
<input type="hidden" name="referralDocId" id="referralDocId" value ="${patient.reference_docto_id}"/>
<input type="hidden" name="originalDepositSetOff" id="originalDepositSetOff" value='${depositDetails.map.deposit_set_off}'/>
<input type="hidden" name="originalPointsRedeemed" id="originalPointsRedeemed" value='${rewardPointDetails.map.points_redeemed}'/>
<input type="hidden" name="existing_per_diem_code" id="existing_per_diem_code" value ="${patient.per_diem_code}"/>
<input type="hidden" name="rewardPointsRedeemedAmount" id="rewardPointsRedeemedAmount" value='${rewardPointDetails.map.points_redeemed_amt}' />

<%-- bill type display --%>
<c:set var="billTypeDisplay">
	<c:choose>
		<c:when test="${bill.billType == 'P' && bill.restrictionType == 'P'}"><insta:ltext key="billing.patientbill.details.billnow_ph"/></c:when>
		<c:when test="${bill.billType == 'C' && bill.restrictionType == 'P'}"><insta:ltext key="billing.patientbill.details.billlater_ph"/></c:when>
		<c:when test="${bill.billType == 'P'}"><insta:ltext key="billing.patientbill.details.billnow"/></c:when>
		<c:when test="${bill.billType == 'C'}"><insta:ltext key="billing.patientbill.details.billlater"/></c:when>
		<c:otherwise><insta:ltext key="billing.patientbill.details.other"/></c:otherwise>
	</c:choose>
</c:set>

<c:set var="cancellationStatusDisplay">
	<c:choose>
		<c:when test="${bill.cancellationApprovalStatus == 'S'}"><insta:ltext key="billing.patientbill.details.requestsent"/></c:when>
		<c:when test="${bill.cancellationApprovalStatus == 'A'}"><insta:ltext key="billing.patientbill.details.approved"/></c:when>
		<c:when test="${bill.cancellationApprovalStatus == 'R'}"><insta:ltext key="billing.patientbill.details.rejected"/></c:when>
		<c:otherwise><insta:ltext key=""/></c:otherwise>
	</c:choose>
</c:set>

<c:if test="${multiVisitBill == 'Y'}">
	<c:set var="billTypeDisplay" value="${billTypeDisplay}-Pkg"/>
</c:if>

<c:if test="${bill.billType == 'C' && bill.isPrimaryBill == 'N'}">
	<c:set var="billTypeDisplay" value="Sec. ${billTypeDisplay}"/>
</c:if>

<c:set var="isOtherHospitalSponsorBill"
	value="${not empty bill.sponsorBillNo && bill.billType eq 'C' && bill.visitType == 't' && bill.restrictionType == 'N'}"/>

<c:set var="extraColumns" value="${isInsuranceBill ? 2 : 0}"/>
<c:if test="${((actionRightsMap.cancel_elements_in_bill == 'A')||(roleId==1)||(roleId==2))}">
	<c:set var="extraColumns" value="${extraColumns + 1}"/>
</c:if>

<c:choose>
	<c:when test="${(ip_credit_limit_rule == 'B') && (bill.visitType == 'i')}">
		<c:set var="allowAdd" value="${ ((actionRightsMap.edit_bill == 'A') || (roleId==1) || (roleId==2))
			&& (bill.status == 'A')
			&& (bill.visitType !='t') && (bill.visitType !='r') && (bill.restrictionType != 'P') && (multiVisitBill != 'Y')
			&& (creditLimitDetailsMap.availableCreditLimit > 0)}"/>
	</c:when>
	<c:otherwise>
		<c:set var="allowAdd" value="${ ((actionRightsMap.edit_bill == 'A') || (roleId==1) || (roleId==2))
			&& (bill.status == 'A')
			&& (bill.visitType !='t') && (bill.visitType !='r') && (bill.restrictionType != 'P') && (multiVisitBill != 'Y')}"/>
	</c:otherwise>
</c:choose>

<c:set var="isPhBillNowReturns" value="${bill.billType == 'P' && bill.restrictionType == 'P' && bill.totalAmount < 0}"/>
<c:set var="hasRewardPointsEligibility" value="${(!isInsuranceBill && (availableRewardPoints > 0 || (not empty rewardPointDetails.map.points_redeemed && rewardPointDetails.map.points_redeemed != 0)) && !isPhBillNowReturns)}" />

<c:set var="remarks" value=""/>
<c:set var="hasDynaPackage" value="${(bill.billType  != 'P') && ((bill.visitType == 'i') || (bill.visitType == 'o')) && not empty dynaPkgNameList}"/>
<c:set var="isPerDiemPrimaryBill" value="${not empty patient.use_perdiem && patient.use_perdiem == 'Y'
				 && bill.is_tpa && bill.billType == 'C' && bill.isPrimaryBill == 'Y' && not empty perdiemCodesList}"/>
<c:set var="hasDRGCode" value="${not empty drgCodeMap && not empty drgCodeMap.drg_charge_id}"/>

<c:set var="priClaimEditable" value="${bill.primaryPlanId != null &&  bill.primaryPlanId != '' ? true : bill.status != 'A'}"/>
<c:set var="secClaimEditable" value="${bill.secondaryPlanId != null && bill.secondaryPlanId != '' ? true : bill.status != 'A'}"/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.billdetails"/></legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
		<%-- Information row: Bill no (type), Open date (by), Billing Bed Type --%>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.billno.type.in.brackets"/>:</td>
			<td class="forminfo">${ifn:cleanHtml(bill.billNo)} (${billTypeDisplay})
				<input type="hidden" name="billType" value="${ifn:cleanHtmlAttribute(bill.billType)}"/>
			</td>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.opendate.by.in.brackets"/>:</td>
			<td class="forminfo">
			<c:set var="opendate"><fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/></c:set>
			<c:set var="opentime"><fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/></c:set>
			<c:choose>
				<c:when test="${userIsallowedtoEditOpenDate}">
					<insta:datewidget name="opendate" value="${opendate}"
							id="opendate" btnPos="left" onchange="doValidateDateField(this,'past');" />
					<input type="text" size="4" name="opentime"
						value="${opentime}" class="timefield"/>(${ifn:cleanHtml(bill.openedBy)})
				</c:when>
				<c:otherwise>
					<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>
					<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/> (${ifn:cleanHtml(bill.openedBy)})
					<input type="hidden" name="opendate" value="${opendate}"/>
					<input type="hidden" name="opentime" value="${opentime}"/>
				</c:otherwise>
			</c:choose>
			</td>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.billingbedtype"/>:</td>
			<td class="forminfo">${patient.bill_bed_type}</td>
		</tr>

                <%-- One row for: Multivisit Package Name, Package Status --%>
                <c:if test="${multiVisitBill == 'Y' && not empty mvpackage}">
                <tr>
		        <td class="formlabel"><insta:ltext key="billing.patientbill.details.packagename"/>:</td>
 			<td class="forminfo">${mvpackage.map.package_name}</td>
                        <td class="formlabel"><insta:ltext key="billing.patientbill.details.packagestatus"/>:</td>
                        <td class="forminfo">
			<c:choose>
			    <c:when test="${mvpackage.map.status == 'C'}"><insta:ltext key="billing.patientbill.details.completed"/></c:when>
			    <c:when test="${mvpackage.map.status == 'P'}"><insta:ltext key="billing.patientbill.details.inprogress"/></c:when>
			    <c:when test="${mvpackage.map.status == 'X'}"><insta:ltext key="billing.patientbill.details.cancelled"/></c:when>
			    <c:otherwise><insta:ltext key="billing.patientbill.details.unknown"/></c:otherwise>
			</c:choose>
			</td>
                </tr>
                </c:if>

		<%-- Status row: Status, Finalized Date, [Discharge stuff for credit bill] --%>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.billstatus"/>:</td>
			<c:choose>
				<c:when test="${(bill.billType=='P' && !bill.is_tpa) || bill.status == 'C' || bill.status == 'X'}">
					<td class="forminfo">
						<b>${statusDisplay[bill.status]}</b>
						<input type="hidden" name="billStatus" id="billStatus" value="${ifn:cleanHtmlAttribute(bill.status)}" />
					</td>
				</c:when>
				<c:otherwise>
					<td>
						<select name="billStatus" id="billStatus" onchange="onChangeBillStatus()" class="dropdown">
						</select>
					</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.paymentstatus"/>:</td>
			<td class="forminfo">
				<c:choose>
					<c:when test="${(bill.billType=='P' && !bill.is_tpa) || bill.status == 'C' || bill.status == 'X'}">
						<b>${paymentStatusDisplay[bill.paymentStatus]}</b>
						<input type="hidden" name="paymentStatus" value="${ifn:cleanHtmlAttribute(bill.paymentStatus)}" />
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="paymentStatus" value="${bill.paymentStatus}"
				 		opvalues="U,P" optexts="${paymentstatus}" onchange="onChangePaymentStatus()"/>
					</c:otherwise>
				</c:choose>
			</td>
			<c:if test="${dischargeType == 'bill'}">
				<td class="formlabel">
					<input type="checkbox" name="_okToDischarge" style="margin: 0px; padding: 0px"
						${patient.discharge_flag == 'D' ? 'checked disabled' : ''}
						onclick="return onChangeDischarge()">
					<input type="hidden" name="okToDischarge" id="okToDischarge"
						value="${patient.discharge_flag == 'D' ? 'Y' : 'N'}"/>
					<label for="_okToDischarge"><insta:ltext key="billing.patientbill.details.discharge"/></label>
				</td>
			</c:if>
			<c:if test="${dischargeType == 'adt'}">
				<td class="formlabel">
					<input type="checkbox" name="_okToDischarge" style="margin: 0px; padding: 0px"
						onclick="return onChangeOkToDischarge()"
						${bill.okToDischarge == 'Y' ? 'checked' : ''}
						${patient.discharge_flag == 'D' && bill.status != 'A' ? 'disabled' : ''}/>
					<input type="hidden" name="okToDischarge" id="okToDischarge" value="${ifn:cleanHtmlAttribute(bill.okToDischarge)}"/>
					<label for="_okToDischarge"><insta:ltext key="billing.patientbill.details.oktodischarge"/></label>
				</td>
			</c:if>
			<c:if test="${dischargeType != 'none'}">
				<c:choose>
				<c:when test="${bill.isPrimaryBill == 'Y'}">
					<c:choose>
						<c:when test="${bill.visitType == 'i'}">
							<td>
								<fmt:formatDate var="disdate" pattern="dd-MM-yyyy" value="${bill.financialDisDate}"/>
								<fmt:formatDate var="distime" pattern="HH:mm" value="${bill.financialDisTime}"/>
								<insta:datewidget name="dischargeDate" id="dischargeDate" value="${disdate}" btnPos="left"/>
								<input type="text" size="4" id="dischargeTime" name="dischargeTime" value="${distime}"
									onblur="validateDischaregDateTime()" class="timefield" />
								</td>
					</c:when>
					<c:otherwise>
						<td class="forminfo">
							<fmt:formatDate var="disdate" pattern="dd-MM-yyyy" value="${patient.discharge_date}"/>
							<fmt:formatDate var="distime" pattern="HH:mm" value="${patient.discharge_time}"/>
							<insta:datewidget name="dischargeDate" id="dischargeDate" value="${disdate}" btnPos="left"/>
								<input type="text" size="4" id="dischargeTime" name="dischargeTime" value="${distime}"
									onblur="validateDischaregDateTime()" class="timefield" />
						</td>
					</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
					<td class="forminfo">
						<fmt:formatDate pattern="dd-MM-yyyy" value="${patient.discharge_date}"/>
						<fmt:formatDate pattern="HH:mm" value="${patient.discharge_time}"/>
					</td>
				</c:otherwise>
				</c:choose>
			</c:if>
		</tr>

		<%-- [One row for insurance bills: Procedure, Sponsor Consolidated Bill No: quite rare] --%>
		<c:if test="${isInsuranceBill && ((not empty procedureLimitList) || (not empty bill.sponsorBillNo))}">
			<tr>
				<c:set var="procedureName" value="${bill.procedureCode}-${bill.procedureName}"/>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.procedure"/>: </td>
				<td valign="top" colspan="3">
					<div id="pro_wrapper" style="width: 455px;">
						<input type="text" name="procedure_name" id="procedure_name" style="width: 455px;"
							value="${procedureName != '-'? procedureName:''}"/>
						<div id="pro_dropdown"></div>
						<input type="hidden" name="procedure_no" id="procedure_no" value="${bill.procedureNo}">
					</div>
				</td>
				<c:if test="${not empty bill.sponsorBillNo}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsorbillno"/>:</td>
					<td class="forminfo">
						<a href="${cpath}/pages/BillDischarge/allocateSponsorBill.do?_method=view&sponsor_bill_no=${ifn:cleanURL(bill.sponsorBillNo)}"
							title='<insta:ltext key="billing.patientbill.details.sponsorbillno1"/>' >${ifn:cleanHtml(bill.sponsorBillNo)}</a>
					</td>
				</c:if>
			</tr>
		</c:if>

		<%-- [One row for insurance bills: Claim status, Approval Amount, Deduction] --%>
		<c:if test="${isInsuranceBill}">
			<c:if test="${not empty patient.primary_sponsor_id}">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.pri.claimstatus"/>:</td>
					<td class="forminfo">
						<c:if test="${bill.status == 'A'}">
							<c:set var="claimStatusDisabled"><insta:ltext key="billing.patientbill.details.disabled"/>="1"</c:set>
						</c:if>
						<c:if test="${!eClaimModule}">
							<select class="dropdown" name="primaryClaimStatus"  id="primaryClaimStatus" ${bill.status == 'A' ? 'readonly':''}>
								<option value="O" ${bill.primaryClaimStatus!='C' ? 'selected':''}>Open</option>
								<option value="C" ${bill.primaryClaimStatus=='C' ? 'selected':''}>Closed</option>
							</select>
							<input type="hidden" name="priClaimStatusCheck" id="priClaimStatusCheck" value="">
						</c:if>
						<c:if test="${eClaimModule}">
							${claimStatusDisplay[bill.primaryClaimStatus]}		
							<input type="hidden" name="primaryClaimStatus"	 id="primaryClaimStatus" value="${bill.primaryClaimStatus}"/>
							<input type="hidden" name="priClaimStatusCheck" id="priClaimStatusCheck" value="">			
						</c:if>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.pri.approvalamount"/>:</td>
					<td class="forminfo">
						<input type="text" name="primaryApprovalAmount" id="primaryApprovalAmount" value="${patient.primary_approval_limit}"
						onchange="return onChangePrimaryApprovalAmt();" readOnly/>
					</td>
					<td class="formlabel">
						<insta:ltext key="billing.patientbill.details.pri.sponsoramount${(not empty primarySponsorMap && primarySponsorMap.get('claim_amount_includes_tax').equals('Y')) ? '.without.tax' : ''}"/>:
					</td>
					<td>
						<input type="text" name="primaryTotalClaim" id="primaryTotalClaim" ${priClaimEditable ? 'readOnly' : ''}
						value="${billDetails.bill.primaryTotalClaim}" onchange="return onChangePrimaryClaimAmt();" />
					</td>
				</tr>
			</c:if>
			<c:if test="${not empty patient.secondary_sponsor_id}">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.sec.claimstatus"/>:</td>
					<td class="forminfo">
						<c:if test="${bill.status == 'A'}">
							<c:set var="claimStatusDisabled"><insta:ltext key="billing.patientbill.details.disabled"/>="1"</c:set>
						</c:if>
						<c:if test="${!eClaimModule}">
						<select class="dropdown" name="secondaryClaimStatus"  id="secondaryClaimStatus" ${bill.status == 'A' ? 'readonly':''}>
								<option value="O" ${bill.secondaryClaimStatus!='C' ? 'selected':''}>Open</option>
								<option value="C" ${bill.secondaryClaimStatus=='C' ? 'selected':''}>Closed</option>
						</select>
						<input type="hidden" name="secClaimStatusCheck" id="secClaimStatusCheck" value="">
						</c:if>
						<c:if test="${eClaimModule}">
							${claimStatusDisplay[bill.secondaryClaimStatus]}
							<input type="hidden" name="secondaryClaimStatus" id="secondaryClaimStatus" value="${bill.secondaryClaimStatus}"/>
							<input type="hidden" name="secClaimStatusCheck" id="secClaimStatusCheck" value="">
						</c:if>
					</td>

					<td class="formlabel"><insta:ltext key="billing.patientbill.details.sec.approvalamount"/>:</td>
					<td class="forminfo">
						<input type="text" name="secondaryApprovalAmount" id="secondaryApprovalAmount" value="${patient.secondary_approval_limit}"
						onchange="return onChangeSecondaryApprovalAmt();" readOnly/>
					</td>
					<td class="formlabel">						<insta:ltext key="billing.patientbill.details.sec.sponsoramount${(not empty secondarySponsorMap && secondarySponsorMap.get('claim_amount_includes_tax').equals('Y')) ? '.without.tax' : ''}"/>:
:</td>
					<td>
						<input type="text" name="secondaryTotalClaim" id="secondaryTotalClaim"  ${secClaimEditable ? 'readOnly' : ''}
						value="${billDetails.bill.secondaryTotalClaim}" onchange="return onChangeSecondaryClaimAmt();" />
					</td>
				</tr>
			</c:if>
		</c:if>
		
		<c:if test="${isInsuranceBill && limitType == 'R' && bill.isPrimaryBill == 'Y'}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.case.rate.one"/>:</td>
				<td class ="forminfo">
					<div id="caseRateOneAutoComp" style="padding-bottom: 20px" class="autoComplete">
						<input type="text" name="caserateCode_1" id="caserateCode_1" value="${caseRateDetails.primary_case_rate}" ${(bill.status != 'A') ? 'disabled' : ''} />
						<div id="caseRateOneDropDown" class="scrolForContainer"></div>
					</div>
					<input type="hidden" name="case_rate_id" id="case_rate_id1" value="${caseRateDetails.primary_case_rate_id}"/>
					<input type="button" name="caseRateOneDet" id="caseRateOneDet" title='<insta:ltext key="billing.patientbill.details.caserateonedet"/>'
					onclick="showCaseRateDetDialog(1);" value="..." class="button"/>
				</td>
				<c:if test="${caseRateCount == '2'}">
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.case.rate.two"/>:</td>
				<td class ="forminfo">
					<table>
						<tr>
							<td>
								<div id="caseRateTwoAutoComp" style="padding-bottom: 20px" class="autoComplete">
									<input type="text" name="caserateCode_2" id="caserateCode_2" value="${caseRateDetails.secondary_case_rate}" ${(bill.status != 'A') ? 'disabled' : ''} />
									<div id="caseRateTwoDropDown" class="scrolForContainer"></div>
								</div>
								<input type="hidden" name="case_rate_id" id="case_rate_id2" value="${caseRateDetails.secondary_case_rate_id}">
							</td>
							<td style="padding-left:0px;">
								<input type="button" name="caseRateTwoDet" id="caseRateTwoDet" title='<insta:ltext key="billing.patientbill.details.caseratetwodet"/>'
								onclick="showCaseRateDetDialog(2);" value="..." class="button"/>
							</td>
						</tr>
					</table>
				</td>
				</c:if>
			</tr>
		</c:if>

		<%-- [One row for insurance bills: Deduction, insured patient non-insured bill rate plan] --%>
		<c:if test="${isInsuranceBill || isNonInsuredBillOfInsPatient}">
		<tr>
			<c:if test="${isInsuranceBill}">
				<td class="formlabel" style="display:none"><insta:ltext key="billing.patientbill.details.patientdeduction"/>:</td>
				<td style="display:none"><input type="text" name="insuranceDeduction" id="insuranceDeduction"
					${bill.status != 'A' || (bill.restrictionType == 'N' && hasPlanVisitCopayLimit) ? 'readOnly' : ''}
					value="${billDetails.bill.insuranceDeduction}" onchange="return onChangeBillDeduction();" />
				</td>
			</c:if>
			<c:if test="${isNonInsuredBillOfInsPatient}">
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.insurancebill"/>:</td>
				<td class="forminfo"><insta:ltext key="billing.patientbill.details.no"/></td>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.billingrateplan"/>:</td>
				<td class="forminfo">${ifn:cleanHtml(bill.billRatePlanName)}</td>
			</c:if>
		</tr>
		</c:if>
		<%-- [One row for: Dynamic package name, package charge] --%>
		<c:if test="${hasDynaPackage}">
		<tr>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.packagename"/>:</td>
			<td class="forminfo">
				<table>
					<tr>
						<td style="padding-left:0px;">
							<div id="dynapkg_wrapper" class="autoComplete">
					    		<input type="text" name="dynaPkgName" id="dynaPkgName" maxlength="50"
					    				value="${ifn:cleanHtmlAttribute(bill.dynaPkgName)}" ${(bill.status != 'A') ||  !(actionRightsMap.allow_edit_dyna_package == 'A' || roleId == 1 || roleId ==2) ? 'disabled' : ''} />
						    	<div id="dynaPkgName_dropdown" class="scrollForContainer"></div>
					     	</div>
					     	<input type="hidden" name="dynaPkgId" id="dynaPkgId" value="${bill.dynaPkgId}">
						</td>
						<td style="padding-left:0px;">
							<input type="button" name="btnValueCap" id="btnValueCap" title='<insta:ltext key="billing.patientbill.details.dynapackagecategorydetails"/>'
							onclick="showValueCapDialog();" value="..."
							accesskey="O" class="button"/>
						</td>
					</tr>
				</table>
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.packagecharge"/>:</td>
			<td class="forminfo">
				<input type="text" name="dynaPkgCharge" id="dynaPkgCharge" value="${bill.dynaPkgCharge}"
					onchange="return onChangePkgCharge();">
				<input type="hidden" name="oldDynaPkgCharge" id="oldDynaPkgCharge" value="${bill.dynaPkgCharge}">
			</td>
			<c:if test="${!eClaimModule && bill.status == 'F' && createSponsorCreditNote && isInsuranceBill}">
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.pri.markclaimforwriteoffcreditnote"/>:</td>
				<td class="forminfo">
					<input type="checkbox" id="claim_closure_type" 
						name="claim_closure_type" onchange="setWriteOffFlag();" 
							${bill.sponsorWriteOff == 'M'? 'checked':'' } ${primaryclaimstatus == 'Closed' ? '':'disabled'}/>
					<input type="hidden" id="sponsor_writeoff" name="sponsor_writeoff" value=""/>
			</td>
			</c:if>
		</tr>
		</c:if>
		<c:if test="${!hasDynaPackage && !eClaimModule && bill.status == 'F' && createSponsorCreditNote && isInsuranceBill}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.pri.markclaimforwriteoffcreditnote"/>:</td>
				<td class="forminfo">
					<input type="checkbox" id="claim_closure_type" 
						name="claim_closure_type" onchange="setWriteOffFlag();" 
							${bill.sponsorWriteOff == 'M'? 'checked':'' } ${primaryclaimstatus == 'Closed' ? '':'disabled'}/>
					<input type="hidden" id="sponsor_writeoff" name="sponsor_writeoff" value=""/>
			</td>
			</tr>
		</c:if>

		<%-- One row for: Label, Remarks, Finalized Date --%>

		<tr>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.label"/>:</td>
			<td>
				<select name="billLabelId" id="billLabel" class="dropdown" onchange="highlightMarkedOnes();">
					<option value="-1" ${bill.billLabelId eq -1 ? 'selected' : ''}>${dummyvalue}</option>
					<c:forEach var="label" items="${billLabelMasterMap}">
						<option value="${label.bill_label_id}" ${label.bill_label_id eq bill.billLabelId ?'selected':'' }>${label.bill_label_name}</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.remarks"/>:</td>
			<td class="forminfo">
				<input type="hidden" name="oldRemarks" id="oldRemarks" value="${ifn:cleanHtmlAttribute(bill.billRemarks)}"/>
				<input type="text" name="billRemarks" id="billRemarks" value="${ifn:cleanHtmlAttribute(bill.billRemarks)}"
						maxlength="100" />
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.finalizeddate"/>:</td>
			<td>
				<c:set var="finaldate"><fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/></c:set>
				<c:set var="finaltime"><fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/></c:set>
				<c:choose>
					<c:when test="${actionRightsMap.modify_bill_finalized_date == 'A' || roleId == 1 || roleId ==2}">
						<insta:datewidget name="finalizedDate" value="${finaldate}"
							id="f_date" btnPos="left" readOnly="true"/>
						<input type="text" size="4" name="finalizedTime" readOnly="true"
							value="${finaltime}" class="timefield"/>
					</c:when>
					<c:otherwise>
						<fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/>
						<fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/>
						<input type="hidden" name="finalizedDate" value="${finaldate}"/>
						<input type="hidden" name="finalizedTime" value="${finaltime}"/>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>

		<%-- One row for: Cancel Reason, Reopen Reason, Discount Auth --%>
		<c:if test="${actionRightsMap.cancel_bill eq 'A' || actionRightsMap.bill_reopen eq 'A' || roleId == 1 || roleId ==2
				|| not empty bill.cancelReason || not empty bill.reopenReason || not empty bill.discountAuthName}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.cancelreason"/>:</td>
				<td>
					<input type="hidden" name="oldCancelReason" value="${ifn:cleanHtmlAttribute(bill.cancelReason)}" />
					<input type="text" name="cancelReason" value="${ifn:cleanHtmlAttribute(bill.cancelReason)}" />
				</td>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.reopenreason"/>:</td>
				<td>
					<input type="hidden" name="oldreopenReason" value="${ifn:cleanHtmlAttribute(bill.reopenReason)}" />
					<input type="text" name="reopenReason" value="${ifn:cleanHtmlAttribute(bill.reopenReason)}" />
				</td>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.discountsauth"/>:</td>
				<td class="forminfo"><label id="discountAuthlbl">${ifn:cleanHtml(bill.discountAuthName)}</label></td>
			</tr>
		</c:if>
		<tr>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.writeoffremarks"/>:</td>
			<td>
				<input type="text" name=writeOffRemarks id="writeOffRemarks" value="${ifn:cleanHtmlAttribute(bill.writeOffRemarks)}"
						maxlength="200" />
				<input type="hidden" name="oldWriteOffRemarks" id="oldWriteOffRemarks" value="${ifn:cleanHtmlAttribute(bill.writeOffRemarks)}"/>
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.spnrWriteoffremarks"/>:</td>
			<td>
				<input type="text" name="spnrWriteOffRemarks" id="spnrWriteOffRemarks" value="${ifn:cleanHtmlAttribute(bill.spnrWriteOffRemarks)}"
						maxlength="200" />
				<input type="hidden" name="oldSpnrWriteOffRemarks" id="oldSpnrWriteOffRemarks" value="${ifn:cleanHtmlAttribute(bill.spnrWriteOffRemarks)}"/>
			</td>
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.discscategory"/>:</td>
			<td class="forminfo"><label id="discountCatLbl">${ifn:cleanHtmlAttribute(bill.discountCategoryName)}</label></td>
		</tr>
		<tr>
			<c:if test="${bill.cancellationApprovalStatus == 'S' || bill.cancellationApprovalStatus == 'A' || bill.cancellationApprovalStatus == 'R'}">
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.cancellationstatus"/>:</td>
				<td class="forminfo"><label id="cancelApprovalStatus">${cancellationStatusDisplay}</label></td>
				<c:if test="${bill.cancellationApprovalStatus == 'A'}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.approvedby"/>:</td>
					<td class="forminfo"><label id="cancelApprovedBy">${ifn:cleanHtml(bill.cancellationApprovedBy)}</label></td>
				</c:if>
				<c:if test="${bill.cancellationApprovalStatus == 'R'}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.rejectedby"/>:</td>
					<td class="forminfo"><label id="cancelApprovedBy">${ifn:cleanHtml(bill.cancellationApprovedBy)}</label></td>
				</c:if>
			</c:if>
		</tr>
	</table>
</fieldset>


<div id="dynavalueCapDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.dynapackagecategorylimitdetails"/></legend>
		<table class="formtable" id="packageDetailsTab">
			<tr>
				<td class="forminfo" style="width:10px;text-align:right;"><insta:ltext key="billing.patientbill.details.category"/></td>
				<td class="forminfo" style="width:10px;text-align:right;"><insta:ltext key="billing.patientbill.details.amtlimit"/></td>
				<td class="forminfo" style="width:10px;text-align:right;"><insta:ltext key="billing.patientbill.details.qtylimit"/></td>
				<td class="forminfo" style="width:10px;text-align:center;"><insta:ltext key="billing.patientbill.details.pkgincluded"/></td>
			</tr>
		    <tr>
				<td class="formlabel" style="width:10px;"><label></label></td>
				<td class="formlabel" style="width:10px;text-align:right;"><label>0.00</label></td>
				<td class="formlabel" style="width:10px;text-align:right;"><label>0.00</label></td>
				<td class="formlabel" style="width:10px;text-align:center;"><label></label></td>
			</tr>
		</table>
	</fieldset>
</div>
</div>

<div id="caseRateDetDialog" style="display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.caseratelimitdetails"/></legend>
			<table class="formtable" id="caseRateDetailsTab">
			<tr>
				<td class="forminfo" style="width:10px;text-align:center;"><insta:ltext key="billing.patientbill.details.category"/></td>
				<td class="forminfo" style="width:10px;text-align:right;"><insta:ltext key="billing.patientbill.details.amtlimit"/></td>
			</tr>
			</table>
		</fieldset>
	</div>
</div>

<div id="perdiemInclusionsDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.perdiemdetails"/></legend>
		<table class="formtable">
			<tr><td class="forminfo"><insta:ltext key="billing.patientbill.details.charge"/></td> <td><label id="perdiemChargeLabel"></label></td></tr>
			<tr><td class="forminfo"><insta:ltext key="billing.patientbill.details.copay.percentage.in.brackets"/></td> <td><label id="perdiemCopayPerLabel"></label></td></tr>
			<tr><td class="forminfo"><insta:ltext key="billing.patientbill.details.copay"/></td> <td><label id="perdiemCopayAmtLabel"></label></td></tr>
		</table>
		<table class="formtable" id="perdiemDetailsTab">
			<tr>
				<td class="forminfo" style="width:10px;text-align:right;">#</td>
				<td class="forminfo" style="width:10px;text-align:left;padding-left:10px;"><insta:ltext key="billing.patientbill.details.includedsubgroups"/></td>
			</tr>
		    <tr>
				<td class="formlabel" style="width:10px;text-align:right;"><label>0</label></td>
				<td class="formlabel" style="width:10px;text-align:left;padding-left:10px;"><label></label></td>
			</tr>
		</table>
	</fieldset>
</div>
</div>

<table width="100%">
<tr>
	<td width="80%">
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.filter"/>&nbsp;<insta:ltext key="billing.patientbill.details.service"/>&nbsp;<insta:ltext key="billing.patientbill.details.groups"/>: </td>
				<td>
					<insta:selectdb name="filterServiceGroup" table="service_groups" valuecol="service_group_id"
					displaycol="service_group_name" orderby="display_order" dummyvalue="${all2}" dummyvalueId=""
					onchange="onChangeFilter(this);"/>
				</td>

				<td class="formlabel"><insta:ltext key="billing.patientbill.details.filter"/>&nbsp;<insta:ltext key="billing.patientbill.details.charge"/>&nbsp;<insta:ltext key="billing.patientbill.details.heads"/>:</td>
				<td>
					<insta:selectdb name="filterChargeHead" table="chargehead_constants" valuecol="chargehead_id"
					displaycol="chargehead_name" filtered="false" orderby="display_order" dummyvalue="${all2}" dummyvalueId=""
					onchange="onChangeFilter(this);"/>
				</td>

				<td class="formlabel"><insta:ltext key="billing.patientbill.details.hide"/>&nbsp;<insta:ltext key="billing.patientbill.details.cancelled"/>:</td>
				<td class="forminfo">
					<input type="checkbox" name="showCancelled" onclick="onChangeFilter();"/>
				</td>
			</tr>
		</table>
	</td>
	<td width="20%">
		<table id="dynaPkgFilterTable" class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
		<tr>
			<c:if test="${hasDynaPackage}">
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.package"/>:</td>
				<td class="forminfo">
					<insta:selectoptions name="filterPackage" optexts="${all1}"
					opvalues="Included,Excluded" dummyvalue="${all}" value="" onchange="onChangeFilter(this);"/>
				</td>
			</c:if>
		</tr>
		</table>
	</td>
</tr>
</table>
<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="chargesTable" border="0" width="100%">
		<tr bgcolor="#8FBC8F" id="chRow0">
			<th><input type="checkbox" onclick="selectAllForDiscounts()" id="discountAll"/></th>
			<th style="width: 80px"><insta:ltext key="billing.patientbill.details.date"/></th>
			<th style="width: 30px"><insta:ltext key="billing.patientbill.details.ord"/>#</th>
			<th><insta:ltext key="billing.patientbill.details.head"/></th>
			<th style="width: 60px" title="${ifn:cleanHtmlAttribute(code)}"><insta:ltext key="billing.patientbill.details.code"/></th>
			<th style="width: 130px"><insta:ltext key="billing.patientbill.details.description"/></th>
			<th><insta:ltext key="billing.patientbill.details.details"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.rate"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.qty"/></th>
			<th class="number" style="width: 10px"></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.disc"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.amt"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.tax"/></th>
			<c:if test="${isInsuranceBill}">
				<c:choose>
					<c:when test="${multiPlanExists}">
						<th class="number" title="${priclaimamt}"><insta:ltext key="billing.patientbill.details.prisponsor"/></th>
						<th class="number" title="${secclaimamt}"><insta:ltext key="billing.patientbill.details.secsponsor"/></th>
						<th class="number" title="${sponseramt}"><insta:ltext key="billing.patientbill.details.totalsponsortax"/></th>
						<th class="number" title="${patientamt}"><insta:ltext key="billing.patientbill.details.patient"/></th>
						<th class="number"><insta:ltext key="billing.patientbill.details.patient.tax"/></th>
						<th><insta:ltext key="billing.patientbill.details.pripriorauthno"/></th>
						<th><insta:ltext key="billing.patientbill.details.secpriorauthno"/></th>
					</c:when>
					<c:otherwise>
							<th class="number" title="${sponseramt}"><insta:ltext key="billing.patientbill.details.sponsor"/></th>
							<th class="number" title="${sponseramt}"><insta:ltext key="billing.patientbill.details.sponsortax"/></th>
							<th class="number" title="${patientamt}"><insta:ltext key="billing.patientbill.details.patient"/></th>
							<th class="number"><insta:ltext key="billing.patientbill.details.patient.tax"/></th>
							<th><insta:ltext key="billing.patientbill.details.priorauthno"/></th>
					</c:otherwise>
				</c:choose>

			</c:if>
			<c:if test="${((actionRightsMap.cancel_elements_in_bill == 'A')||(roleId==1)||(roleId==2))}">
				<th style="width:24px"></th>		<%-- trash icon --%>
			</c:if>
			<th style="width:24px"></th>		<%-- edit icon --%>
		</tr>

		<c:set var="rounded" value="N"/>
		<c:set var="totalClaimAmount" value="0"/>
		<c:set var="numCharges" value="${fn:length(billDetails.charges)}"/>

		<%-- we add one hidden row with a null charge for use as a template to clone from --%>
		<c:forEach begin="1" end="${numCharges+1}" var="i" varStatus="loop">
			<c:set var="charge" value="${billDetails.charges[i-1]}"/>

			<c:set var="flagColor">
				<c:choose>
					<c:when test="${charge.status == 'X'}"><insta:ltext key="billing.patientbill.details.red"/></c:when>
					<c:when test="${charge.chargeExcluded == 'Y'}"><insta:ltext key="billing.patientbill.details.blue"/></c:when>
					<c:when test="${charge.chargeExcluded == 'P'}"><insta:ltext key="billing.patientbill.details.yellow"/></c:when>
					<c:when test="${hasRewardPointsEligibility && charge.eligible_to_redeem_points == 'Y'}"><insta:ltext key="billing.patientbill.details.green"/></c:when>
					<c:otherwise><insta:ltext key="billing.patientbill.details.empty"/></c:otherwise>
				</c:choose>
			</c:set>

			<c:if test="${empty charge}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			
			<c:set var="discChkDisabled" value=""/>
			<c:if test="${charge.chargeHead == 'OUTDRG' || charge.chargeHead == 'BPDRG'  || 
				charge.chargeHead == 'ADJDRG' || charge.chargeHead == 'APDRG'}">
				<c:set var="discChkDisabled" value="disabled"/>
			</c:if>

			<tr ${style}>
				<td><input type="checkbox" name="discountCheck" ${discChkDisabled} /></td>
				<td>
					<img src="${cpath}/images/${flagColor}_flag.gif"/>
					<label>
						<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>
					</label>
					<input type="hidden" name="packageId" value="${charge.packageId}" />
					<input type="hidden" name="postedDate"
						value="<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>" />
					<input type="hidden" name="postedTime"
						value="<fmt:formatDate value="${charge.postedDate}" pattern="HH:mm"/>" />
					<input type="hidden" name="billDisplayType" value="${charge.billDisplayType}" />
				</td>
				<td><label>${charge.orderNumber}</label>
					<input type="hidden" name="orderNumber" value='${charge.orderNumber}'>
				</td>
				<td>
					<label title="${charge.chargeHeadName}">${charge.chargeHeadName}</label>
					<input type="hidden" name="chargeHeadName" value='${charge.chargeHeadName}'>
					<input type="hidden" name="chargeGroupName" value='${charge.chargeGroupName}'>
					<input type="hidden" name="chargeGroupId" value='${charge.chargeGroup}'>
					<input type="hidden" name="chargeHeadId" value='${charge.chargeHead}'>
					<input type="hidden" name="chargeId" value='${charge.chargeId}' >
					<input type="hidden" name="chargeRef" value='${charge.chargeRef}' >
					<input type="hidden" name="departmentId" value='${charge.actDepartmentId}'>
					<input type="hidden" name="hasActivity" value='${charge.hasActivity}'>
					<input type="hidden" name="payeeDocId" value='${charge.payeeDoctorId}'>
					<input type="hidden" name="docPaymentId" value='${charge.docPaymentId}'>
					<input type="hidden" name="activityConducted" value='${charge.activityConducted}'>
					<input type="hidden" name="prescDocId" value='${charge.prescribingDrId}'>
					<input type="hidden" name="actItemCode" value='${charge.actItemCode}'>
					<input type="hidden" name="edited" value='false'>
					<input type="hidden" name="remarks" value="${charge.actRemarks}"/>
					<input type="hidden" name="userRemarks" value="${charge.userRemarks}"/>
					<input type="hidden" name="itemRemarks" value="${charge.itemRemarks}"/>
					<input type="hidden" name="allowDiscount" value='${charge.allowDiscount}' >
					<input type="hidden" name="allowRateVariation" value='${charge.allowRateVariation}' >
					<input type="hidden" name="service_sub_group_id" value='${charge.serviceSubGroupId}' >
					<input type="hidden" name="service_group_id" value='${charge.serviceGroupId}' >
					<input type="hidden" name="serviceGroupName" value='${charge.serviceGroupName}' >
					<input type="hidden" name="serviceSubGroupName" value='${charge.serviceSubGroupName}' >
					<input type="hidden" name="conducting_doc_mandatory" value='${charge.conducting_doc_mandatory}' >
					<input type="hidden" name="delCharge" value="${charge.status=='X'?'true':'false'}"	/>
					<input type="hidden" name="chargeExcluded" value="${charge.chargeExcluded}"/>
					<input type="hidden" name="packageFinalized" value="${charge.packageFinalized}"/>
					<input type="hidden" name="consultation_type_id" value='${charge.consultation_type_id}' >
					<input type="hidden" name="op_id" value='${charge.op_id}' >
					<input type="hidden" name="from_date" value='${charge.from_date}' />
					<input type="hidden" name="to_date" value='${charge.to_date}' />
					<input type="hidden" name="insuranceCategoryId" value='${charge.insuranceCategoryId}' >
					<input type="hidden" name="pseudoPatientAmt" value='${charge.amount - charge.insuranceClaimAmount}' >
					<input type="hidden" name="firstOfCategory" value='${charge.firstOfCategory}' >
					<input type="hidden" name="allowRateIncrease" value="${charge.allowRateIncrease }"/>
					<input type="hidden" name="allowRateDecrease" value="${charge.allowRateDecrease }"/>
					<input type="hidden" name="eligible_to_redeem_points" value='${charge.eligible_to_redeem_points}' >
					<input type="hidden" name="redemption_cap_percent" value='${charge.redemption_cap_percent}' >
					<input type="hidden" name="redeemed_points" value='${charge.redeemed_points}' >
					<input type="hidden" name="max_redeemable_points" value='0' >
					<input type="hidden" name="amount" value='${charge.amount}'>
					<input type="hidden" name="returnAmt" value='${charge.returnAmt}'>
					<input type="hidden" name="discount" value='${charge.discount}'>
					<input type="hidden" name="isClaimLocked" value='${charge.isClaimLocked}'>
					<input type="hidden" name="saved_posted_date" value='<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>'>
					<input type="hidden" name="saved_posted_time" value='<fmt:formatDate value="${charge.postedDate}" pattern="HH:mm"/>'>
					<input type="hidden" name="dynaPackageExcluded" value="${charge.dynaPackageExcluded}"/>
				</td>

				<td>
					<label>${charge.actRatePlanItemCode}</label>
					<input type="hidden" name="actRatePlanItemCode" value='${charge.actRatePlanItemCode}'>
					<input type="hidden" name="codeType" value='${charge.codeType}'>
				</td>

				<td>
					<c:set var="packageOnClick" value="" />
					<c:set var="packageLinkStyle" value="" />
					<c:if test="${charge.chargeGroupName == 'Packages'}">
						<c:set var="packageOnClick" value="handlePackageClick('${charge.chargeId}')" />
						<c:set var="packageLinkStyle" value="color: #336699; cursor: pointer;" />
					</c:if>

					<div style="width:130px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; ${packageLinkStyle}" title="${ifn:cleanHtml(charge.actDescription)}"
						onclick="${packageOnClick}">
						${charge.actDescription}
					</div>
					<input type="hidden" name="description"
						value="${fn:escapeXml(charge.actDescription)}" >
					<input type="hidden" name="descriptionId"
						value="${charge.actDescriptionId}" >
				</td>

				<td>
					<c:choose>
						<c:when test="${charge.chargeHead == 'PHMED' || charge.chargeHead == 'PHRET' || charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHCRET'}">
							<c:set var="remLen" value="${fn:length(charge.actRemarks)}"/>
							<c:set var="remarks" value="${fn:substring(charge.actRemarks,4,remLen)}"/>
							<a target="#" title="${charge.actRemarks}"
							 href="${cpath}/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&printerId=0&duplicate=true&saleId=${fn:substring(charge.actRemarks,4,remLen)}">
								<c:out value="${fn:substring(charge.actRemarks,0,15)}"/>
							</a>
						</c:when>
						<c:when test="${charge.chargeHead eq 'INVITE' && charge.packageId eq null}">
							<c:set var="remLen" value="${fn:length(charge.actRemarks)}"/>
							<c:set var="remarks" value="${fn:substring(charge.actRemarks,4,remLen)}"/>
							<a target="#" title="${charge.actRemarks}"
							 href="${cpath}/DirectReport.do?report=StoreStockPatientIssues&issNo=${fn:substring(charge.actRemarks,4,remLen)}">
								<c:out value="${fn:substring(charge.actRemarks,0,15)}"/>
							</a>
						</c:when>
						<c:otherwise>
							<c:set var="remarks" value=""/>
							<insta:truncLabel value="${charge.actRemarks}" length="16"/>
						</c:otherwise>
					</c:choose>
				</td>

				<td class="number">
					<label>${charge.actRate}</label>
					<input type="hidden" name="rate" value="${charge.actRate}"/>
					<input type="hidden" name="originalRate" value='${charge.originalRate}' />
					<input type="hidden" name="savedRate" value="${charge.actRate}"/>
				</td>

				<td class="number">
					<label>${charge.actQuantity}</label>
					<input type="hidden" name="qty" value="${charge.actQuantity}"/>
					<input type="hidden" name="returnQty" value="${charge.returnQty}"/>
					<input type="hidden" name="qty_included" value="${charge.qty_included}"/>
				</td>

				<td>
					<label>${charge.actUnit}</label>
					<input type="hidden" name="units" value="${charge.actUnit}"/>
				</td>

				<!-- discounts title hidden field value are setted here -->
				<td class="number">
					<label>${charge.discount}</label>
					<input type="hidden" name="disc" value="${charge.discount}"/>
					<input type="hidden" name="oldDisc" value="${charge.discount}"/>
					<input type="hidden" name="overall_discount_auth_name"
						value="${charge.overall_discount_auth_name}"/>
					<input type="hidden" name="old_overall_discount_auth"
						value="${charge.overall_discount_auth}"/>
					<input type="hidden" name="overall_discount_auth"
						value="${charge.overall_discount_auth}"/>
					<input type="hidden" name="isSystemDiscount"
						value="${charge.isSystemDiscount}"/>
					<input type="hidden" name="isSystemDiscountOld"
						value="${charge.isSystemDiscount}"/>
					<input type="hidden" name="overall_discount_amt"
						value="${charge.overall_discount_amt}"/>
			       <input type="hidden" name="discount_auth_dr_name"
						value="${charge.discount_auth_dr_name}"/>
					<input type="hidden" name="discount_auth_dr"
						value="${charge.discount_auth_dr}"/>
					<input type="hidden" name="dr_discount_amt"
						value="${charge.dr_discount_amt}"/>

					<input type="hidden" name="discount_auth_pres_dr_name"
						value="${charge.discount_auth_pres_dr_name}"/>
					<input type="hidden" name="discount_auth_pres_dr"
						value="${charge.discount_auth_pres_dr}"/>
					<input type="hidden" name="pres_dr_discount_amt"
						value="${charge.pres_dr_discount_amt}"/>

					<input type="hidden" name="discount_auth_ref_name"
						value="${charge.discount_auth_ref_name}"/>
					<input type="hidden" name="discount_auth_ref"
						value="${charge.discount_auth_ref}"/>
					<input type="hidden" name="ref_discount_amt"
						value="${charge.ref_discount_amt}"/>

					<input type="hidden" name="discount_auth_hosp_name"
						value="${charge.discount_auth_hosp_name}"/>
					<input type="hidden" name="discount_auth_hosp"
						value="${charge.discount_auth_hosp}"/>
					<input type="hidden" name="hosp_discount_amt"
						value="${charge.hosp_discount_amt}"/>
				</td>
				<td class="number">
					<label>${charge.amount}</label>
					<input type="hidden" name="amt" value='${charge.amount}' />
					<input type="hidden" name="amount_included" value="${charge.amount_included}"/>
					<input type="hidden" name="serviceChrgApplicable" value='${charge.serviceChrgApplicable}' />
					
					<c:set var="numTaxes" value="${fn:length(charge.billChargeTaxes)}"/>
					<c:set var="chargeId" value="${charge.chargeId}"/>
					<c:set var="totalTaxAmount" value="0"/>
					<c:forEach begin="1" end="${numTaxes}" var="k" varStatus="loop">
						<c:set var="chgtax" value="${charge.billChargeTaxes[k-1].map}"/>
						<input type="hidden" name="${chargeId}_charge_tax_id" id="${chargeId}_charge_tax_id${k-1}" value="${chgtax.charge_tax_id}"/>
						<input type="hidden" name="${chargeId}_sub_group_id" id="${chargeId}_sub_group_id${k-1}" value="${chgtax.item_subgroup_id}"/>
						<input type="hidden" name="${chargeId}_item_group_id" id="${chargeId}_item_group_id${k-1}" value="${chgtax.item_group_id}"/>
						<input type="hidden" name="${chargeId}_tax_amt" id="${chargeId}_tax_amt${k-1}" value="${chgtax.tax_amount}"/>
						<input type="hidden" name="${chargeId}_original_tax_amt" id="${chargeId}_original_tax_amt${k-1}" value="${chgtax.original_tax_amt}"/>
						<input type="hidden" name="${chargeId}_tax_rate" id="${chargeId}_tax_rate${k-1}" value="${chgtax.tax_rate}"/>
						
						
						<c:set var="totalTaxAmount" value="${totalTaxAmount + chgtax.tax_amount}"/>
					</c:forEach>
					<input type="hidden" name="total_tax_${chargeId}" id="total_tax_${chargeId}"  value="${totalTaxAmount}"/>
					<input type="hidden" name="taxesCnt_${chargeId}" id="taxesCnt_${chargeId}" value="${numTaxes}" />
				</td>
				<td class="number">
					<label>${charge.taxAmt}</label>
					<input type="hidden" name="tax_amt" value='${charge.taxAmt}'/>
					<input type="hidden" name="original_tax_amt" value='${charge.originalTaxAmt}'/>
				</td>

				<c:if test="${isInsuranceBill}">
					<td class="number">
						<c:choose>
							<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
								<label>${ifn:afmt(0)}</label>
							</c:when>
							<%--<c:when test="${charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHMED'}">
									<label>${charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt}</label>
							</c:when>--%>
							<c:otherwise>
								<label>${ifn:afmt(charge.claimAmounts[0])}</label>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="insClaimAmt" value='${charge.claimAmounts[0]}' />
						<input type="hidden" name="priInsClaimAmt" value='${charge.claimAmounts[0]}' />
						<input type="hidden" name="priInsClaimTaxAmt" value='${charge.sponsorTaxAmounts[0]}' />
						<input type="hidden" name="priIncludeInClaim" value='${charge.includeInClaimCalc[0]}' />
						<input type="hidden" name="insClaimable" value="${charge.insurancePayable}"/>
						<input type="hidden" name="insClaimTaxable" value="${charge.insuranceClaimTaxable}"/>
					</td>
					
					<c:if test="${multiPlanExists}">
						<td class="number">
							<c:choose>
								<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
									<label>${ifn:afmt(0)}</label>
								</c:when>
							<%--
								<c:when test="${charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHMED'}">
									<label>${charge.insuranceClaimAmount + charge.returnInsuranceClaimAmt}</label>
								</c:when>
							--%>
								<c:otherwise>
									<label>${ifn:afmt(charge.claimAmounts[1])}</label>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="secInsClaimAmt" value='${charge.claimAmounts[1]}' />
							<input type="hidden" name="secInsClaimTaxAmt" value='${charge.sponsorTaxAmounts[1]}' />
							<input type="hidden" name="secIncludeInClaim" value='${charge.includeInClaimCalc[1]}' />
							<input type="hidden" name="insClaimable" value="${charge.insurancePayable}"/>
							<input type="hidden" name="insClaimTaxable" value="${charge.insuranceClaimTaxable}"/>
						</td>
					</c:if>
					
					<td class="number">
						<label>${ifn:afmt(charge.sponsorTaxAmounts[0] + charge.sponsorTaxAmounts[1])}</label>
						<input type="hidden" name="sponsor_tax" value='${charge.sponsorTaxAmounts[0] + charge.sponsorTaxAmounts[1]}' />
					</td>

					<td class="number">
						<c:choose>
							<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
								<label>${ifn:afmt(0)}</label>
							</c:when>
							<c:when test="${charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHMED'}">
								<label>${(charge.amount + charge.returnAmt) - charge.claimAmounts[0] - charge.claimAmounts[1]}</label>
							</c:when>
							<c:otherwise>
								<label>${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}</label>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="returnInsuranceClaimAmt" value='${charge.returnInsuranceClaimAmt}' />
						<input type="hidden" name="insDeductionAmt" value='${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}' />
					</td>
					
					<td class="number">
						<c:choose>
							<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
								<label>${ifn:afmt(0)}</label>
							</c:when>
							<c:when test="${charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHMED'}">
								<label>${(charge.taxAmt + charge.returnTaxAmt) - charge.sponsorTaxAmt}</label>
							</c:when>
							<c:otherwise>
								<label>${charge.taxAmt - charge.sponsorTaxAmt}</label>
							</c:otherwise>
						</c:choose>
					</td>
					
					<td>
						<label>${charge.preAuthIds[0]}</label>
						<input type="hidden" name="preAuthId" value='${charge.preAuthIds[0]}' >
						<input type="hidden" name="preAuthModeId" value='${charge.preAuthModeIds[0]}' >
					</td>
					<c:if test="${multiPlanExists}">
						<td>
							<label>${charge.preAuthIds[1]}</label>
							<input type="hidden" name="secPreAuthId" value='${charge.preAuthIds[1]}' >
							<input type="hidden" name="secPreAuthModeId" value='${charge.preAuthModeIds[1]}' >
						</td>
					</c:if>
				</c:if>

				<c:if test="${((actionRightsMap.cancel_elements_in_bill == 'A')||(roleId==1)||(roleId==2))}">
					<td style="text-align: center">
						<c:choose>
							<c:when test="${charge.hasActivity
								|| charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHCRET'
								|| charge.chargeHead == 'PHMED'  || charge.chargeHead == 'PHRET'
								|| charge.chargeHead == 'MARPKG' || charge.chargeHead == 'MARPDM'
								|| charge.chargeHead == 'MARDRG' || charge.chargeHead == 'OUTDRG' 
								|| charge.chargeHead == 'BPDRG'  || charge.chargeHead == 'ADJDRG' 
								|| charge.chargeHead == 'APDRG' }">
								<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button"/>
							</c:when>
							<c:when test="${charge.status == 'X'}">
								<a href="javascript:Un-Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.undocancel"/>'>
									<img src="${cpath}/icons/undo_delete.gif" class="imgDelete button"/>
								</a>
							</c:when>
							<c:otherwise>
								<c:choose>
									<c:when test="${bill.status == 'A'}">
										<a href="javascript:Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.cancelitem"/>'>
											<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
										</a>
									</c:when>
									<c:otherwise>
										<a href="javascript:Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.cancelitem"/>'>
											<img src="${cpath}/icons/delete_disabled.gif" class="imgDelete button"/>
										</a>
									</c:otherwise>
								</c:choose>
							</c:otherwise>
						</c:choose>
					</td>
				</c:if>

				<td style="text-align: center">
					<c:choose>
						<c:when test="${charge.chargeHead == 'ROF' || charge.chargeHead == 'ADJDRG'}">
							<img src="${cpath}/icons/Edit1.png" class="button" />
						</c:when>
						<c:when test="${!(charge.chargeHead == 'BPDRG' || charge.chargeHead == 'APDRG' || charge.chargeHead == 'OUTDRG') && hasDRGCode}">
							<img src="${cpath}/icons/Edit1.png" class="button" />
						</c:when>
						<c:otherwise>
							<a href="javascript:Edit" onclick="return showEditDialog(this,'${charge.chargeHead}','${remarks}');" title='<insta:ltext key="billing.patientbill.details.edititemdetails"/>'>
							<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</c:otherwise>
					</c:choose>
				</td>
			</tr>

			<c:if test="${charge.status != 'X'}">
				<c:set var="totalNetAmount" value="${totalNetAmount + charge.amount}"/>
				<c:set var="totalDiscount" value="${totalDiscount + charge.discount}"/>
				<c:if test="${isInsuranceBill}">
					<c:set var="totalClaimAmount" value="${totalClaimAmount + charge.insuranceClaimAmount}"/>
				</c:if>
			</c:if>

			<c:if test="${(charge.chargeHead == 'ROF' && charge.status != 'X')}">
				<c:set var="rounded" value="Y"/>
			</c:if>

		</c:forEach>

		<c:set var="totalClaimAmount" value="${totalClaimAmount - billDeduction}"/>
		<c:if test="${totalClaimAmount < 0}">
			<c:set var="totalClaimAmount" value="0"/>
		</c:if>
		<c:if test="${isOtherHospitalSponsorBill}">
			<c:set var="totalClaimAmount" value="${totalNetAmount}"/>
		</c:if>

		<%-- one row for apply discounts and add button --%>
		<c:set var="discButtonNeeded" value="${ (bill.status != 'C') && (bill.status != 'X') &&
				((actionRightsMap.allow_discount == 'A')||(roleId==1)||(roleId==2)) }"/>
		<c:set var="discCategoryBtnNeeded" value="${bill.status != 'C' && bill.status != 'X'}"/>
		<c:set var="canChangeDiscountPlan" value="${!bill.is_tpa || visitDiscountPlanId == 0 || bill.billDiscountCategory == 0}"/>
		<c:set var="disableDiscCategory" value="${canChangeDiscountPlan && actionRightsMap.allow_discount_plans_in_bill != 'N' && (bill.status != 'C' && bill.status != 'X' && bill.status != 'F') && (bill.billDiscountAuth == 0 || bill.billDiscountAuth == '')?'':'disabled'}"/>
		<input  type="hidden"  id="disableDiscCategory1" value="${actionRightsMap.allow_discount_plans_in_bill != 'N'}">
	</table>
	<table class="addButton">
		<tr>
			<td>
				<%-- we need the row to be present for getNumCharges calculation to be correct,
				even if the buttons are not being shown. --%>
				<table class="footerTable">
					<tr>
						<c:if test="${discCategoryBtnNeeded}">
							<td>
							<insta:ltext key="billing.patientbill.details.discountCategory"/>:
							<select name="discountCategory" id="discountCategory" class="dropdown" onchange="onChangeDiscountCategory();" ${disableDiscCategory}>
								<option value="">${dummyvalue}</option>
								<c:forEach var="discCat" items="${discountCategoires}">
  							 		<option value="${discCat.map.discount_cat_id}" ${discCat.map.discount_cat_id == bill.billDiscountCategory?'selected':'' }>${discCat.map.discount_cat}</option>
 								</c:forEach>
							</select>
							<img class="imgHelpText" title="<insta:ltext key="billing.patientbill.details.discountdescription"/>" src="${cpath}/images/help.png"/>
							</td>
							<c:if test="${discCategoryBtnNeeded && !discButtonNeeded}">
								<td>
									<input type="button" value="Apply" name="itemDiscPerApply"
									title='<insta:ltext key="billing.patientbill.details.replaceexistingdiscount"/>' onclick="onApplyItemDiscPer();" ${disableDiscCategory}>
								</td>
							</c:if>
						</c:if>
						<c:if test="${discButtonNeeded}">
						<td>
							<insta:ltext key="billing.patientbill.details.discountonselecteditems"/>:
							<input type="text" name="itemDiscPer" id="itemDiscPer" value="0" size="3"
							onchange="return validateDiscPer();" style="text-align:right;width:60px" />%
						</td>
						<td><insta:ltext key="billing.patientbill.details.discountauth"/>:</td>
						<td>
							<select name="discountAuthName" id="discountAuthName" class="dropdown" onchange="selectDiscountAuth();">
								<option value="">${dummyvalue}</option>
								<c:forEach var="discAuth" items="${discountAuthorizers}">
									<option value="${discAuth.map.disc_auth_id}" ${discAuth.map.disc_auth_id == bill.billDiscountAuth?'selected':'' }>${discAuth.map.disc_auth_name}</option>
								</c:forEach>
							</select>
						</td>
					     <td>
							<input type="button" value="${applybutton}" name="itemDiscPerApply"
									title='<insta:ltext key="billing.patientbill.details.replaceexistingdiscount"/>' onclick="onApplyItemDiscPer();">
							<input type="button" value="${addbutton}" name="itemDiscPerAdd"
									title='<insta:ltext key="billing.patientbill.details.addtoexistingdiscounts"/>' onclick="onAddItemDiscPer();">
						</td>
						</c:if>
						<c:if test="${ centerPrefs.map.no_of_tax_groups  >= 1 }" >
						</tr>
						<tr>
						<td><insta:ltext key="billing.patientbill.details.bulk.tax"/>:
							<select name="_bulk_item_subgroup_id" id="_bulk_item_subgroup_id" class="dropdown" onchange="onchangeBulkTaxSubGroup(this);" >
								<option value="0">${dummyvalue}</option>
								<c:forEach var="taxsubgrp" items="${taxSubGroups}">
									<option value="${taxsubgrp.map.item_subgroup_id}">${taxsubgrp.map.item_subgroup_name}</option>
								</c:forEach>
							</select>
						</td>
						</c:if>
						<td><insta:ltext key="billing.patientbill.details.roundoff"/>:
							<input type="checkbox" name="roundOff" onclick="calculateRoundOff()"
								${rounded == 'Y'? 'checked':''} ${rounded == 'Y'? 'disabled':''}
								${bill.status != 'A' ? 'disabled':''} />
						</td>
						<%--
						<c:if test="${bill.is_tpa && bill.restrictionType == 'N' && hasPlanVisitCopayLimit}">
						<td title="Process Visit Copay">
							<input type="button" onclick="return processPlanvisitCopay()" value="Process Copay" ${bill.status != 'A' || bill.paymentStatus != 'U' ? 'disabled':''}/>
							<img class="imgHelpText" title="Please note: Process Visit Copay resets patient amounts and patient deduction is calculated."
					 		src="${cpath}/images/help.png"/>
						</td>
						</c:if>
						--%>
					</tr>
				</table>
			</td>
			<td align="center" style="width:16px">
				<c:if test="${allowAdd}">
					<button type="button" name="btnAddItem" id="btnAddItem" title='<insta:ltext key="billing.patientbill.details.addnewitem"/>'
						onclick="addOrderDialog.start(this, false, ''); return false;"
						accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
				</c:if>
			</td>
		</tr>
	</table>
	
	<c:if test="${hasDynaPackage}">
		<table class="addButton" id="dynaPkgBtnTable">
		<tr>
			<td>
				<table class="footerTable">
					<tr>
						<td>
							<button name="btnIncludePkg" id="btnIncludePkg" title='<insta:ltext key="billing.patientbill.details.includetheselecteditems"/>'
								onclick="includeIntoDynaPkg(); return false;" ${bill.status != 'A' ? 'disabled':''} ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}> <insta:ltext key="billing.patientbill.details.includeinpkg"/></button>
						</td>
						<td>
							<button name="btnExcludePkg" id="btnExcludePkg" title='<insta:ltext key="billing.patientbill.details.excludetheselecteditems"/>'
								onclick="excludeFromDynaPkg(); return false;" ${bill.status != 'A' ? 'disabled':''} ${actionRightsMap.allow_dyna_package_include_exclude != 'A' ? 'disabled':''}> <insta:ltext key="billing.patientbill.details.excludefrompkg"/></button>
						</td>
						<td>
							<input type="button" onclick="return processPackageCharges()" value="Package Process" ${bill.status != 'A' ? 'disabled':''}/>
							<img class="imgHelpText" title='<insta:ltext key="billing.patientbill.details.longernote"/>'
					 		src="${cpath}/images/help.png"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		</table>
	</c:if>
	<c:if test="${isPerDiemPrimaryBill}">
		<table class="addButton" id="perdiemBtnTable">
			<tr>
				<td>
				<table class="footerTable">
					<tr>
						<td class="forminfo"> <insta:ltext key="billing.patientbill.details.perdiemcode"/>: </td>
						<td>
							<select name="per_diem_code" id="per_diem_code" class="dropdown"
									${bill.status != 'A' || bill.paymentStatus != 'U' ? 'disabled':''}  onchange="setPerdiemInclDetails();">
								<option value="">${dummyvalue}</option>
									<c:forEach items="${perdiemCodesList}" var="perdiemcode">
										<option value="${perdiemcode.per_diem_code}"
										 ${patient.per_diem_code == perdiemcode.per_diem_code ? 'selected' : ''}
										 style="width:300px;"
										 onmouseover='this.title = "${fn:escapeXml(perdiemcode.per_diem_description)}"'>
										 ${perdiemcode.per_diem_description}</option>
									</c:forEach>
							</select>
						</td>
						<td style="padding-left:0px;">
							<input type="button" name="btnPerdiemIncl" id="btnPerdiemIncl" title='<insta:ltext key="billing.patientbill.details.perdiemdetails"/>'
							onclick="showPerDiemInclDialog();" value="..."
							accesskey="I" class="button"/>
						</td>
						<td>
							<input type="button" onclick="return perdiemProcessing()" value="Process Using Perdiem" ${bill.status != 'A' || bill.paymentStatus != 'U' ? 'disabled':''}/>
							<img class="imgHelpText" title='<insta:ltext key="billing.patientbill.details.longertext"/>'
					 		src="${cpath}/images/help.png"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		</table>
	</c:if>
	<c:if test="${isInsuranceBill && limitType == 'R' && bill.isPrimaryBill == 'Y' && bill.status == 'A'}">
		<table class="addButton" id="caseRateButtonTable">
		<tr>
			<td>
				<table class="footerTable">
					<tr>
						<td>
							<button type="button" id="process-case-rate-button" onclick="return processCaseRate();" ><insta:ltext key="billing.patientbill.details.processCaseRate"/></button>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		</table>
	</c:if>
</div>

<c:set var="width" value="640"/>
<c:if test="${(totalDeposit > 0 || hasRewardPointsEligibility) && isInsuranceBill}">
	<c:set var="width" value="800"/>
</c:if>
<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.totals"/></legend>
	<table  align="right" class="infotable">
   		<tr style="display:none" id ="filterRow">

			<td>
				<div class="formlabel" style="float: left; padding-right:3px" ><insta:ltext key="billing.patientbill.details.filterednetamt"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblFilteredNetAmt">0.00</label>
				</div>
			</td>

			<c:if test="${totalDeposit > 0 || hasRewardPointsEligibility}">
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
			</c:if>

			<c:if test="${isInsuranceBill}">
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
			</c:if>

			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.filtereddiscounts"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblFilteredDisc">0.00</label>
				</div>
			</td>
			
			<td>
				<div class="formlabel"><insta:ltext key="billing.patientbill.details.filteredtax"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblFilteredTax">0.00</label>
				</div>
			</td>

			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.filteredamount"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblFilteredAmount">0.00</label>
				</div>
			</td>
	   	</tr>

		<tr>
			<td>
				<div class="formlabel" style="float: left">&nbsp;</div>
				<div class="forminfo textApperance" style="float:left">&nbsp;</div>
			</td>
			
 			<td>
 				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.billedamount"/>: </div>
 				<div class="forminfo textApperance" style="float:left">
	 				<label id= "lblTotBilled" >0.00</label>
 				</div>
 			</td>

			<c:if test="${totalDeposit > 0 || hasRewardPointsEligibility}">
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
			</c:if>

			<c:if test="${isInsuranceBill}">
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
			</c:if>

			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.discounts"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblTotDisc">0.00</label>
				</div>
			</td>
			
			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.taxamount"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblTotTaxAmt">0.00</label>
				</div>
			</td>

			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.netamount"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblTotAmt">0.00</label>
				</div>
			</td>
	 	</tr>
	 	

 		<tr>

			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.totalDeposit"/>: </div>
				<div class="forminfo textApperance" style="float:left"><%-- ${depositDetails.map.total_deposits==0?'0.00':depositDetails.map.total_deposits} --%>
					<label id="lblTotalDeposit">0.00</label>
				</div>
			</td>
			
			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.patientamount"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblPatientAmount">0.00</label>
				</div>
			</td>
			
			<td>
				<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.patienttax"/>: </div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblPatientTax">0.00</label>
				</div>
			</td>
			
			<!--
			<c:if test="${isInsuranceBill}">
				<td class="formlabel">Patient Deduction:</td>
				<td class="forminfo">
					<label id="lblPatientDeduction">0.00</label>
				</td>
			</c:if>
			 -->
			<c:if test="${isInsuranceBill}">
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
			</c:if>

			<td>
				<div class="formlabel" style="float: left; padding-right:3px">
					<c:if test="${not empty bill}">
						<a target="${isNewUX == 'Y' ? '_blank': ''}" href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=R&payment_type=F&is_deposit=false">
					</c:if>
						<insta:ltext key="billing.patientbill.details.patient"/>&nbsp;<insta:ltext key="billing.patientbill.details.payments"/>: 
					<c:if test="${not empty bill}">
						</a>
					</c:if>
				</div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblExistingReceipts">0.00</label>
				</div>
			</td>

			<c:if test="${totalDeposit > 0 || hasRewardPointsEligibility}">
			<c:choose>
				<c:when test="${totalDeposit > 0 && hasRewardPointsEligibility}">
					<td>		
						<div class="formlabel" style="float: left; padding-right:3px">
							<a target="${isNewUX == 'Y' ? '_blank': ''}" href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=R&payment_type=F&is_deposit=true">
								<insta:ltext key="billing.patientbill.details.deposit"/></a> + <insta:ltext key="billing.patientbill.details.pointsamt"/>: </div>
						<div class="forminfo textApperance" style="float:left">
							<label id="lblDepositsSetOff">0.00 </label>&nbsp;+&nbsp;<label id="lblRewardPointsAmt"> 0.00</label>
						</div>
					</td>
				</c:when>
				<c:when test="${totalDeposit > 0}">
					<td>
						<div class="formlabel" style="float: left; padding-right:3px">
							<c:if test="${not empty bill.depositSetOff && bill.depositSetOff > 0}">
								<a target="${isNewUX == 'Y' ? '_blank': ''}" href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=R&payment_type=F&is_deposit=true">
							</c:if>
								<insta:ltext key="billing.patientbill.details.depositssetoff"/>: </div>
							<c:if test="${not empty bill.depositSetOff && bill.depositSetOff > 0}">
								</a>
							</c:if>
						<div class="forminfo textApperance" style="float:left">
							<label id="lblDepositsSetOff">0.00</label>
						</div>
					</td>
				</c:when>
				<c:when test="${hasRewardPointsEligibility}">
					<td>
						<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.pointsamt"/>: </div>
						<div class="forminfo textApperance" style="float:left">
							<label id="lblRewardPointsAmt">0.00</label>
						</div>
					</td>
					</c:when>
				<c:otherwise>
				</c:otherwise>
			</c:choose>
			</c:if>

			<c:choose>
				<c:when test="${isInsuranceBill}">
					<c:set var="patDueAmt" value="${bill.totalAmount + bill.totalTax - bill.totalClaim - bill.totalReceipts}"/>
					<c:set var="spoDueAmt" value="${bill.totalClaim + bill.totalClaimTax - bill.totalPrimarySponsorReceipts - bill.totalSecondarySponsorReceipts}"/>

				</c:when>
				<c:otherwise>
					<c:set var="patDueAmt" value="${bill.totalAmount + bill.totalTax - bill.totalReceipts}"/>
					<c:set var="spoDueAmt" value="0.00"/>
				</c:otherwise>
			</c:choose>
			<td>
				<div class="formlabel" style="float: left; padding-right:3px">
					<c:choose>
						<c:when test="${bill.patientWriteOff == 'M' && (patDueAmt > 0.00 || patDueAmt < 0.00)}">
							<b><span style="color:red">*</span></b><insta:ltext key="billing.patientbill.details.patientdue"/>: 
						</c:when>
						<c:otherwise>
							<insta:ltext key="billing.patientbill.details.patientdue"/>: 
						</c:otherwise>
					</c:choose>
				</div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblPatientDue">0.00</label>
				</div>
			</td>
		</tr>
		<c:if test="${creditNoteDetails != null && creditNoteDetails.bill_no != null}">
			<tr>
				
				<c:if test="${isInsuranceBill}">
					<td>
						<div class="formlabel" style="float: left">&nbsp;</div>
						<div class="forminfo textApperance" style="float:left">&nbsp;</div>
					</td>
				</c:if>
			
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				
				<td>
					<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.patientCreditNoteAmt"/>: </div>
					<div class="forminfo textApperance" style="float:left">
						<%-- ${creditNoteDetails.total_amount - creditNoteDetails.total_claim} --%>
						<label id="lblPatientCredit">0.00</label>
						<input type="hidden" name="patientCreditNoteAmt" id="patientCreditNoteAmt" value="${creditNoteDetails.total_amount - creditNoteDetails.total_claim}"/>
					</div>
				</td>
				
				<td>
					<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.netPatientDue"/>: </div>
					<div class="forminfo textApperance" style="float:left">
						<label id="lblNetPatientDue">0.00</label>
					</div>
				</td>
				<%-- <c:if test="${isInsuranceBill}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsorCreditNoteAmt"/>:</td>
					<td class="forminfo">
						${creditNoteDetails.total_claim}
						<input type="hidden" name="sponsorCreditNoteAmt" id="sponsorCreditNoteAmt" value="${creditNoteDetails.total_claim}"/>
					</td>
				</c:if> --%>
			</tr>
		</c:if>
		<c:if test="${isInsuranceBill || isOtherHospitalSponsorBill}">
			<tr>
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				
				<td>
					<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.amount"/>: </div>
					<div class="forminfo textApperance" style="float:left">
						<label id="lblTotInsAmt">0.00</label>
					</div>
				</td>
				
				<td>
					<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.sponsortax"/>: </div>
					<div class="forminfo textApperance" style="float:left">
						<label id="lblSponsorTax">0.00</label>
					</div>
				</td>

				<td>
					<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.remittances"/>: </div>
					<div class="forminfo textApperance" style="float:left">
						<label id="lblSponsorRecdAmount">0.00</label>
					</div>
				</td>

				<td>
					<div class="formlabel" style="float: left; padding-right:3px">
						<a target="${isNewUX == 'Y' ? '_blank': ''}" href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=S"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.payments"/> :</a>
					</div>
					<div class="forminfo textApperance" style="float:left">
						<label id="lblSponsorReceipts">0.00</label>
					</div>
				</td>

				<c:if test="${totalDeposit > 0}">
					<td>
						<div class="formlabel" style="float: left">&nbsp;</div>
						<div class="forminfo textApperance" style="float:left">&nbsp;</div>
					</td>
				</c:if>

				<td>
				<div class="formlabel" style="float: left; padding-right:3px">
					<c:choose>
						<c:when test="${bill.sponsorWriteOff == 'M' && spoDueAmt > 0.00}">
							<b><span style="color:red">*</span></b><insta:ltext key="billing.patientbill.details.sponsordue"/>: 
						</c:when>
						<c:otherwise>
							<insta:ltext key="billing.patientbill.details.sponsordue"/>: 
						</c:otherwise>
					</c:choose>
				</div>
				<div class="forminfo textApperance" style="float:left">
					<label id="lblSponsorDue">0.00</label>
				</div>
				</td>
			</tr>
		</c:if>
		<c:if test="${creditNoteDetails != null && creditNoteDetails.bill_no != null}">
			<tr>
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				
				<c:if test="${isInsuranceBill}">
					<td>
						<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.sponsorCreditNoteAmt"/>: </div>
						<div class="forminfo textApperance" style="float:left">
							${creditNoteDetails.total_claim}
							<input type="hidden" name="sponsorCreditNoteAmt" id="sponsorCreditNoteAmt" value="${creditNoteDetails.total_claim}"/>
						</div>
					</td>
					<td>
						<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.netSponsorDue"/>: </div>
						<div class="forminfo textApperance" style="float:left">
							<label id="lblNetSponsorDue">0.00</label>
						</div>
					</td>	
				</c:if>		
			</tr>
		</c:if>
		<c:if test="${(bill.patientWriteOff == 'A' &&  (patDueAmt > 0.00 || patDueAmt < 0.00)) || (bill.sponsorWriteOff == 'A' && (spoDueAmt > 0.00 || spoDueAmt < 0.00))}" >
			<tr>
				<td>
					<div class="formlabel" style="float: left">&nbsp;</div>
					<div class="forminfo textApperance" style="float:left">&nbsp;</div>
				</td>
				<c:if test="${bill.patientWriteOff == 'A' &&  (patDueAmt > 0.00 || patDueAmt < 0.00)}">
					<td>
						<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.writeOffamt"/>: </div>
						<div class="forminfo textApperance" style="float:left">
							<label id="lblWrittenOffAmt">0.00</label>
						</div>
					</td>
				</c:if>
				<c:if test="${bill.sponsorWriteOff == 'A' && (spoDueAmt > 0.00 || spoDueAmt < 0.00)}">
					<td>
						<div class="formlabel" style="float: left; padding-right:3px"><insta:ltext key="billing.patientbill.details.spnrWriteOffAmt"/>: </div>
						<div class="forminfo textApperance" style="float:left">
							<label id="lblSpnrWrittenOffAmt">0.00</label>
						</div>
					</td>
				</c:if>
			</tr>
		</c:if>
		
		

		<!-- <c:if test="${bill.patientWriteOff == 'M'}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.markedForWriteOff"/>:</td>
				<td class="forminfo">
					<insta:ltext key="billing.patientbill.details.markedForWriteOffYes"/>
				</td>
				<td>
					<img class="imgHelpText" src="${cpath}/images/help.png"
						title="Marked For Patient Writeoff" style="float: left"/>
				</td>
			</tr>
		</c:if> -->
	</table>
</fieldset>

<c:if test="${isInsuranceBill && limitType == 'R' && bill.isPrimaryBill == 'Y' && null != caseRateDetails}">
	<fieldset>
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.caseRateTotals"/></legend>
		<table class="formtable" id="caseRateTotTbl">
			<tr>
				<td>
					<div id="caseRate1Div" style="display:none;">
						<table id="caseRate1Tbl" class="formtable">
							<tr>
								<td class="forminfo">Case Rate1</td>
								<td class="forminfo" id="caseRate1Tot"></td>
							</tr>
						</table>
					</div>
				</td>
				<td>
					<div id="caseRate2Div" style="display:none;">
						<table id="caseRate2Tbl" class="formtable">
							<tr>
								<td class="forminfo">Case Rate2</td>
								<td class="forminfo" id="caseRate2Tot"></td>
							</tr>
						</table>
					</div>
				</td>
			</tr>
		</table>
	</fieldset>
</c:if>

<c:if test="${totalDeposit > 0}">
	<fieldset>
  		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.depositDetails"/></legend>
  		<table class="formtable">
 			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.depositsBalance"/>: </td>
				<td class="forminfo" style="width:210px">
					<c:choose >
						<c:when test="${bill.visitType == 'i' && ipDepositExists == 'true'}">
							<b>(General: ${availableDeposits-ipDeposits}, IP: ${ipDeposits})</b>
						</c:when>
						<c:when test="${bill.visitType != 'i' && null!= depositDetails.map.package_id && depositDetails.map.package_id > 0}">
            <b>${depositDetails.map.package_unallocated_amount}</b>
						</c:when>
						<c:otherwise>
							<b>${availableDeposits+ipDepositsSetOffNonIpBills}</b>
						</c:otherwise>
					</c:choose>
				</td>
				<c:if test="${bill.visitType != 'i' && null!= depositDetails.map.package_id && depositDetails.map.package_id > 0}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.applicableto"/>:</td>
					<td class="forminfo"><b>${depositDetails.map.package_name}</b></td>
				</c:if>
				<c:if test="${bill.visitType == 'i' && ipDepositExists == 'true'}">
					<td style="width:330px">
					    General Deposit (${bill.depositSetOff-bill.ipDepositSetOff}), IP Deposit (${bill.ipDepositSetOff})
					</td>
				</c:if>
			</tr>
  		</table>
	</fieldset>
</c:if>
			
<c:if test="${billingcounterId != null && billingcounterId != ''}">
<c:if test="${(not empty bill) && (bill.status == 'A' || bill.status == 'F') && (empty bill.sponsorBillNo)}">
<c:set var="showPayments">
<c:choose>
<c:when test="${param.showPayments == 1}"><insta:ltext key="billing.patientbill.details.true"/></c:when> <%-- link from registration: auto open --%>
<c:when test="${actionRightsMap.edit_bill == 'N'}"><insta:ltext key="billing.patientbill.details.true"/></c:when>
<c:otherwise><insta:ltext key="billing.patientbill.details.false"/></c:otherwise>
</c:choose>
</c:set>

<dl class="accordion" style="margin-bottom: 10px;">
	<dt>
		<span><insta:ltext key="billing.patientbill.details.payments"/></span>
		<div class="clrboth"></div>
	</dt>
	<dd id="payDD" class="${bill.billType == 'P' ? 'open' : ''}">
		<div class="bd">

		<c:set var="paymentSelValue" value=""/>
		<c:if test="${(totalNetAmount - existingReceipts - totalClaimAmount) > 0}">
			<c:set var="paymentSelValue" value="A"/>
		</c:if>
		<c:if test="${(totalNetAmount - existingReceipts - totalClaimAmount) == 0}">
			<c:set var="paymentSelValue" value="R"/>
		</c:if>

		<c:set var="bnow" value="${bill.billType == 'P'}"/>
		<c:set var="insp" value="${bill.billType != 'P' || (bill.billType == 'P' && isInsuranceBill)}"/>
		<c:set var="prisnp" value="${not empty patient.primary_sponsor_id && isInsuranceBill}"/>
		<c:set var="secsnp" value="${not empty patient.secondary_sponsor_id && isInsuranceBill}"/>
		<c:set var="primarySponsor" value="${patient.primary_sponsor_id}"/>
		<c:set var="secondarySponsor" value="${patient.secondary_sponsor_id}"/>

		 <insta:billPaymentDetails formName="mainform"  isBillNowPayment="${bnow}" defaultPaymentType="${paymentSelValue}"
		 	isInsuredPayment="${insp}" isPrimarySponsorPayment="${prisnp}" isSecondarySponsorPayment="${secsnp}" 
		 	primarySponsor="${primarySponsor}" secondarySponsor="${secondarySponsor}" hasRewardPointsEligibility="${hasRewardPointsEligibility}"
		 	availableRewardPoints="${availableRewardPoints}" availableRewardPointsAmount="${availableRewardPointsAmount}" origBillStatus="${bill.status}" />
		</div>
	</dd>
</dl>
</c:if>
</c:if>

<%-- Determine what are the available templates to use for printing. --%>
<c:choose>
	<c:when test="${bill.billType =='C'}">
		<c:set var="billPrintDefault" value="${genPrefs.billLaterPrintDefault}"/>
	</c:when>
	<c:otherwise>
		<c:set var="billPrintDefault" value="${genPrefs.billNowPrintDefault}"/>
	</c:otherwise>
</c:choose>

<c:if test="${not empty bill}">
	<table cellpadding="0" cellspacing="0"  border="0" width="100%" id="txx">
	<c:if test="${bill.visitType=='i' && bill.billType !='P' && patientDueSMSStatus=='A' && isMessagingModule && patient.discharge_flag!='D' && patDueAmt> 0.00}">
		<td id="patientDuesmsTd"><input type="checkbox" name="patientDueSMS" id="patientDueSMS" value="Y" style="margin-bottom:10px"/><insta:ltext key="billing.patientbill.details.sendPatientDueSMS"/>
		<img class="imgHelpText" title="<insta:ltext key="billing.patientbill.details.sendPatientDueSMSDescription"/>" src="${cpath}/images/help.png"/>						
	</c:if>
		<c:choose>
			<c:when test="${preferences.modulesActivatedMap['mod_ins_ext'] eq 'Y'}">
				<tr>
					<td align="left">
					<c:if test="${(bill.billType == 'C')  && bill.visitType != 'r'}">
						<%-- Save button for Credit Bills only --%>
						<c:if test="${bill.status != 'X' && bill.status != 'C'}">
							<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u><insta:ltext key="billing.patientbill.details.s"/></u></b><insta:ltext key="billing.patientbill.details.ave"/></button>
						</c:if>

						<%-- Add a reopen button for non-open bills (F Status) --%>
						<c:if test="${((actionRightsMap.bill_reopen == 'A')||(roleId==1)||(roleId==2))
								&& bill.status != 'A'
								&& genPrefs.allowBillReopen == 'Y'
								&& (bill.restrictionType == 'N' || bill.restrictionType == 'T' || (bill.restrictionType == 'P' && bill.is_tpa))}">
							<button type="button" id="reopen-button" onclick="doReopen('${ifn:cleanJavaScript(bill.patientWriteOff)}');" accessKey="R"><b><u><insta:ltext key="billing.patientbill.details.r"/></u></b><insta:ltext key="billing.patientbill.details.eopen"/></button>
						</c:if>
					</c:if>
					</td>
				</tr>
			</c:when>
			<c:otherwise>
				<tr id="bill-actions-print-options-row">
					<td align="left">
						<c:if test="${(bill.billType == 'C')  && bill.visitType != 'r'}">
							<%-- Save button for Credit Bills only --%>
							<c:if test="${bill.status != 'X' && bill.status != 'C'}">
								<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u><insta:ltext key="billing.patientbill.details.s"/></u></b><insta:ltext key="billing.patientbill.details.ave"/></button>
							</c:if>

							<%-- Add a reopen button for non-open bills (F Status) --%>
							<c:if test="${((actionRightsMap.bill_reopen == 'A')||(roleId==1)||(roleId==2))
									&& bill.status != 'A'
									&& genPrefs.allowBillReopen == 'Y'								
									&& (bill.restrictionType == 'N' || bill.restrictionType == 'T' || (bill.restrictionType == 'P' && bill.is_tpa))}">
								<button type="button" id="reopen-button" onclick="doReopen('${ifn:cleanJavaScript(bill.patientWriteOff)}');" accessKey="R"><b><u><insta:ltext key="billing.patientbill.details.r"/></u></b><insta:ltext key="billing.patientbill.details.eopen"/></button>
							</c:if>

						</c:if>

						<%-- For Prepaid Insured Bills default is Pay and Save, with additional Save/Reopen --%>
						<c:if test="${(bill.billType == 'P' && isInsuranceBill)  && bill.visitType != 'r'}">
							<c:if test="${bill.status == 'A' && bill.paymentStatus != 'P'}">
								<c:if test="${billingcounterId != null && billingcounterId != ''}">
									<button type="button" id="payAndSaveButton" accessKey="Y" onclick="return doPayAndSave();"><insta:ltext key="billing.patientbill.details.pa"/><b><u><insta:ltext key="billing.patientbill.details.y"/></u></b><insta:ltext key="billing.patientbill.details.andsave"/></button>
								</c:if>
							</c:if>
							<c:if test="${bill.status != 'X' && bill.status != 'C'}">
								<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u><insta:ltext key="billing.patientbill.details.s"/></u></b><insta:ltext key="billing.patientbill.details.ave"/></button>
							</c:if>
							<c:if test="${((actionRightsMap.bill_reopen == 'A')||(roleId==1)||(roleId==2))
										&& genPrefs.allowBillReopen == 'Y'								
										&& (bill.status != 'A') && (bill.restrictionType == 'N' || (bill.restrictionType == 'P' && bill.is_tpa))}">
								<button type="button" id="reopen-button" onclick="doReopen('${ifn:cleanJavaScript(bill.patientWriteOff)}');" accessKey="R"><b><u><insta:ltext key="billing.patientbill.details.r"/></u></b><insta:ltext key="billing.patientbill.details.eopen"/></button>
							</c:if>
						</c:if>

						<%-- For prepaid bills, the default is Pay and Close, with additional Save/Cancel/Reopen --%>
						<c:if test="${(bill.billType == 'P' && !isInsuranceBill) && bill.visitType != 'r'}">
							<c:if test="${bill.status == 'A'}">
								<c:if test="${billingcounterId != null && billingcounterId != ''}">
									<button type="button" id="payClose" onclick="return doPayAndClose();" accessKey="C">
										<insta:ltext key="billing.patientbill.details.payand"/> <b><u><insta:ltext key="billing.patientbill.details.c"/></u></b><insta:ltext key="billing.patientbill.details.lose"/></button>
									<%-- no print button if Pay and Close is available --%>
									<c:set var="disablePrint" value="true"/>
								</c:if>
								<%-- Save is available, but enabled only for refunds, or if billing counter is not there --%>
								<button type="button" id="saveButton" onclick="return doSave();" accessKey="S">
									<label><b><u><insta:ltext key="billing.patientbill.details.s"/></u></b><insta:ltext key="billing.patientbill.details.ave"/></label></button>
								<%-- Cancel for cancelling bills, also a button --%>
								<c:if test="${(actionRightsMap.cancel_bill == 'A') ||(roleId==1)||(roleId==2)}">
									<c:choose>
										<c:when test="${bill_cancellation_requires_approval == 'Y' && bill.cancellationApprovalStatus != 'A'}">
										</c:when>
										<c:otherwise>
											<button type="button" id="cancel-button" onclick="return doBillCancel();" ><insta:ltext key="billing.patientbill.details.cancelbill"/></button>
										</c:otherwise>
									</c:choose>

								</c:if>
							</c:if>
							<c:if test="${((actionRightsMap.bill_reopen == 'A')||(roleId==1)||(roleId==2))
											&& (bill.status != 'A')
											&& genPrefs.allowBillReopen == 'Y'								
											&& ( bill.restrictionType == 'N' || bill.restrictionType == 'T' ) }">
								<button type="button" id="reopen-button" onclick="doReopen('${ifn:cleanJavaScript(bill.patientWriteOff)}');" accessKey="R"><b><u><insta:ltext key="billing.patientbill.details.r"/></u></b><insta:ltext key="billing.patientbill.details.eopen"/></button>
							</c:if>
						</c:if>
						<c:choose>
							<c:when test="${isInsuranceBill}">
								<c:set var="patDueAmt" value="${bill.totalAmount + bill.totalTax - bill.primaryTotalClaim - bill.secondaryTotalClaim - bill.totalReceipts}"/>
								<c:set var="spoDueAmt" value="${bill.primaryTotalClaim + bill.secondaryTotalClaim + bill.totalClaimTax - bill.totalPrimarySponsorReceipts - bill.totalSecondarySponsorReceipts}"/>

							</c:when>
							<c:otherwise>
								<c:set var="patDueAmt" value="${bill.totalAmount + bill.totalTax - bill.totalReceipts}"/>
								<c:set var="spoDueAmt" value="0.00"/>
							</c:otherwise>
						</c:choose>

						<c:if test="${bill_cancellation_requires_approval == 'Y' && bill.cancellationApprovalStatus != 'S'
							&& bill.cancellationApprovalStatus != 'A' && bill.restrictionType != 'P' && bill.status != 'C'
							&& (actionRightsMap.cancel_bill == 'A' || roleId == 1 || roleId == 2)}">
							<button type="button" id="request-for-bill-cancellation-button" onclick="return requestForcancellation();" ><insta:ltext key="billing.patientbill.details.requestForBillCancellation"/></button>
						</c:if>
						<c:if test="${bill.status == 'F' && ((patDueAmt + (creditNoteDetails.total_amount - creditNoteDetails.total_claim)) > 0.00 || (patDueAmt + (creditNoteDetails.total_amount - creditNoteDetails.total_claim)) < 0.00)  && bill.patientWriteOff == 'N'}">
							<insta:screenlink buttonId="mark-for-writeoff-button" screenId="patient_writeoff" label="billing.patientbill.details.arkForWriteOff"
								accessKey="M" accessKeyLabel ="billing.patientbill.details.m"
								type="button" onClickValidation="writeOffPatientAmt();"/>
						</c:if>
						<c:if test="${not empty loyaltyOfferURL && bill.visitType!='t' && bill.visitType!='r' && bill.visitType!='i'}">
						<a id="loyaltyOfferButton" onclick="showLoyaltyOffersPopup(loyaltyURL,loyaltyHeader)" style="cursor:pointer">| View Offers</a>
						</c:if>
						
						<insta:screenlink target="_blank" screenId="bill_audit_log"
							extraParam="?_method=getAuditLogDetails&bill_no=${bill.billNo}&al_table=bill_audit_view&mr_no=${patient.mr_no}"
								label="Audit Log" addPipe="true" id="audit-log-button" />
						<c:if test="${bill.status == 'A' && not empty patient.primary_sponsor_id && !bill.is_tpa && allowBillInsurance}">
							<insta:screenlink screenId="connect_disconnect_tpa" addPipe="true" label="Connect Insurance"
								extraParam="?_method=getTPAConnectDisconnectScreen&visitId=${patient.patient_id}&billNo=${bill.billNo}${newUXExtraParam}"
								title="${connectins}" target="${isNewUX == 'Y' ? '_blank': ''}" id="connect-insurance-button"
							    rel="opener"/>
						</c:if>
						<c:if test="${bill.status == 'A' && not empty patient.primary_sponsor_id && bill.is_tpa && allowBillInsurance}">
							<insta:screenlink screenId="connect_disconnect_tpa" addPipe="true" label="Disconnect Insurance"
								extraParam="?_method=getTPAConnectDisconnectScreen&visitId=${patient.patient_id}&billNo=${bill.billNo}${newUXExtraParam}"
								title="${disconnectins}" target="${isNewUX == 'Y' ? '_blank': ''}" id="disconnect-insurance-button"
								rel="opener"
							/>
						</c:if>
						<c:if test="${actionRightsMap.allow_credit_bill_later eq 'A' || roleId == 1 || roleId ==2}">
							<c:if test="${bill.status == 'A' && bill.billType == 'P' && bill.restrictionType == 'N'}">
								<insta:screenlink screenId="change_billtype" addPipe="true" label="To Bill Later"
									extraParam="?_method=getChangeBillTypeScreen&visitId=${bill.billType}&billNo=${bill.billNo}${newUXExtraParam}"
									title="${tobilllater}" id="to-bill-later-button"
									target="${isNewUX == 'Y' ? '_blank': ''}" />
							</c:if>
						</c:if>
						<c:if test="${bill.billType == 'C' && bill.restrictionType == 'N'}">
							<insta:screenlink screenId="change_billprimary" addPipe="true" label="Change Primary"
								extraParam="?_method=getScreen&billNo=${bill.billNo}${newUXExtraParam}"
								title="${changeprimarytitle}" target="${isNewUX == 'Y' ? '_blank': ''}"
								id="change-primary-button" />
						</c:if>
						<c:if test="${empty patient.primary_sponsor_id && bill.visitType != 't' && bill.visitType != 'r' && allowBillInsurance 
							&& patient.visit_type == 'o' && patient.use_perdiem == 'N'}">
								<c:if test="${urlRightsMap.change_visit_tpa == 'A'}">
									<c:set var="pagepath" value="<%=URLRoute.EDIT_INSURANCE_URL%>"/>
									<c:url var="opAddUrl" value="${pagepath}/showInsuranceDetails.htm">
										<c:param name="visitId" value="${bill.visitId}"/>
										<c:param name="billNo" value="${bill.billNo}"/>
									</c:url>
									<a
                                        id="add-patient-insurance-button"
										href="${opAddUrl}${newUXExtraParam}"
										title="${addpatientinsurancetitle}"
										target="${isNewUX == 'Y' ? '_blank': ''}"
									> | Add Patient Insurance</a>
								</c:if>
						</c:if>
						<c:if test="${empty patient.primary_sponsor_id && bill.visitType != 't' && bill.visitType != 'r' && allowBillInsurance 
							&& patient.visit_type == 'i'}">
							<insta:screenlink screenId="change_visit_tpa" addPipe="true" label="Add Patient Insurance"
								extraParam="?_method=changeTpa&visitId=${bill.visitId}&billNo=${bill.billNo}${newUXExtraParam}"
								title="${addpatientinsurancetitle}"
								target="${isNewUX == 'Y' ? '_blank': ''}"
                                id="add-patient-insurance-button"
                            />
						</c:if>
						<c:if test="${bill.status == 'F'}">
							<a id="signature-button" onclick="signaturePopup('Patient Signature')" style="cursor:pointer">| <insta:ltext key="billing.patientbill.details.patient.signature"/></a>
						</c:if>
					</td>
					<td align="right">
					<c:set var="emailEnableAndRight" value="${editEmailRights=='A' && manualEmailStatus=='A'}"/>
						<c:if test="${bill.billType != 'M' && bill.billType != 'R' && (billPrintRights == 'A' || emailEnableAndRight==true || roleId == 1 || roleId == 2)}">
				 			<%-- We need a print button along with the print type and printer --%>
							<select name="printBill" id="printSelect" class="dropdown"
									onchange="loadTemplates(this)">
								<c:forEach var="template" items="${availableTemplates}">
									<option value="${fn:escapeXml(template.map.template_id)}"
									${(template.map.template_id == billPrintDefault) ? 'selected' : ''}>
										<c:out value="${template.map.template_name}"/>
									</option>
								</c:forEach>
							</select>

							<c:if test="${bill.billType == 'C' || isInsuranceBill}">
								<insta:selectdb name="printType" table="printer_definition"
									valuecol="printer_id"  displaycol="printer_definition_name" orderby="printer_definition_name"
									value="${genPrefs.default_printer_for_bill_later}"/>
							</c:if>
							<c:if test="${bill.billType == 'P' && !isInsuranceBill}">
								<insta:selectdb name="printType" table="printer_definition"
									valuecol="printer_id"  displaycol="printer_definition_name" orderby="printer_definition_name"
									value="${genPrefs.default_printer_for_bill_now}"/>
							</c:if>
						</c:if>
						<c:if test="${not disablePrint && billPrintRights == 'A'}">
								<button type="button" id="printButton" accessKey="P"
									onclick="return billPrint('${billPrintDefault}','${genPrefs.userNameInBillPrint}')">
									<b><u><insta:ltext key="billing.patientbill.details.p"/></u></b><insta:ltext key="billing.patientbill.details.rint"/></button>
								</c:if>
								<c:if test="${editEmailRights=='A' && bill.restrictionType=='N' && manualEmailStatus=='A' && isMessagingModule}">
								<button type="button" id="emailButton" onclick="showemaildialog('${billTypeDisplay}');">
									<b><insta:ltext key="billing.patientbill.details.email"/></b></button>
							</c:if>
							<c:if test="${not disablePrint && billPrintRights == 'A'}">
								<button type="button" id="downloadButton" accessKey="D" onclick="submitForm();" disabled>
								<b><u><insta:ltext key="billing.patientbill.details.d"/></u></b><insta:ltext key="billing.patientbill.details.ownload"/></button>
							</c:if>
					</td>
				</tr>
				<tr>
					<td>
					<c:set var="orderMrNo" value="${patient.mr_no}" />
					<c:if test="${not empty patient.original_mr_no}">
						<c:set var="orderMrNo" value="${patient.original_mr_no}" />
					</c:if>
					<c:if test="${patient.visit_status == 'A' && bill.status == 'A' && patient.visit_type == 'o'}">
								<insta:screenlink screenId="new_op_order" addPipe="true" label="Order"
									extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(orderMrNo)}/order/visit/${ifn:encodeUriComponent(patient.visit_id)}?retain_route_params=true"/>
					</c:if>
					<c:if test="${patient.visit_status == 'A' && bill.status == 'A' && patient.visit_type == 'i'}">
								<insta:screenlink screenId="new_ip_order" addPipe="true" label="Order"
									extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(orderMrNo)}/order/visit/${ifn:encodeUriComponent(patient.visit_id)}?retain_route_params=true"/>
					</c:if>
					<c:if test="${bill.status == 'A' && bill.restrictionType == 'N' && patient.visit_status == 'A'}">
							<insta:screenlink screenId="patient_inventory_issue" addPipe="true" label="Issue"
								extraParam="/add.htm?visit_id=${patient.patient_id}&bill_no=${bill.billNo}${newUXExtraParam}" id="issue-button"
								target="${isNewUX == 'Y' ? '_blank': ''}" />
					</c:if>
					<c:if test="${bill.status == 'A' && bill.restrictionType == 'N' && patient.visit_status == 'A' && allowEasyRewardzCouponRedemption && easyRewardzModule}">
						<a id="coupon-redemption-button" onclick="couponRedemption()" style="cursor:pointer">| <insta:ltext key="billing.patientbill.details.coupon.redemption"/></a>
					</c:if>
					<c:if test="${not empty patient.primary_sponsor_id && bill.is_tpa eq true && allowBillInsurance 
						&& patient.visit_type == 'o' && patient.use_perdiem == 'N'}">
								<c:if test="${urlRightsMap.change_visit_tpa == 'A'}">
									<c:set var="pagepath" value="<%=URLRoute.EDIT_INSURANCE_URL%>"/>
									<c:url var="opUrl" value="${pagepath}/showInsuranceDetails.htm">
										<c:param name="visitId" value="${bill.visitId}"/>
										<c:param name="billNo" value="${bill.billNo}"/>
									</c:url>
									<a
                                        id="edit-patient-insurance-button"
                                        href="${opUrl}${newUXExtraParam}"
                                        title="${editpatientinsurancetitle}"
                                        target="${isNewUX == 'Y' ? '_blank': ''}"
                                    > | Edit Patient Insurance</a>
								</c:if>
									<insta:screenlink screenId="edit_claim" addPipe="true" label="Edit Claim"
									extraParam="?_method=getClaims&bill_no=${bill.billNo}"
									title="${editclaimtitle}" id="edit-claim-button"
									target="${isNewUX == 'Y' ? '_blank': ''}" />

					</c:if>
					<c:if test="${not empty patient.primary_sponsor_id && bill.is_tpa eq true && allowBillInsurance 
						&& (patient.visit_type == 'i' || patient.use_perdiem == 'Y')}">
									<insta:screenlink screenId="change_visit_tpa" addPipe="true" label="Edit Patient Insurance"
									extraParam="?_method=changeTpa&visitId=${bill.visitId}&billNo=${bill.billNo}${newUXExtraParam}"
									title="${editpatientinsurancetitle}"
                                    target="${isNewUX == 'Y' ? '_blank': ''}"
                                    id="edit-patient-insurance-button"
                                    />

									<insta:screenlink screenId="edit_claim" addPipe="true" label="Edit Claim"
									extraParam="?_method=getClaims&bill_no=${bill.billNo}"
									title="${editclaimtitle}" id="edit-claim-button"
									target="${isNewUX == 'Y' ? '_blank': ''}" />

					</c:if>
					<c:if test="${bill.status == 'A' && bill.restrictionType == 'N'}">
						<insta:screenlink screenId="change_visit_org" addPipe="true" label="Change Rate Plan/Bed Type"
									extraParam="?_method=updateRatePlan&visitId=${bill.visitId}&billNo=${bill.billNo}${newUXExtraParam}"
									title="${changerateplantype}" target="${isNewUX == 'Y' ? '_blank': ''}"
									id="change-rate-plan-bed-type-button" />
					</c:if>
					<c:if test="${not empty patient.alloc_bed_name && patient.visit_status == 'A' }">
						<insta:screenlink addPipe="true" screenId="ip_bed_details" label="Bed Details"
								extraParam="?method=getIpBedDetailsScreen&patientid=${patient.patient_id}"
								id="bed-details-button" target="${isNewUX == 'Y' ? '_blank': ''}"
						/>
					</c:if>

					</td>
				</tr>
				<tr>
					<td>
					<c:if test="${isInsuranceBill}">
						<insta:screenlink screenId="bill_remittance" addPipe="true" label="Edit Bill Remittance"
									extraParam="?_method=getBillRemittance&billNo=${bill.billNo}"
									title="${editbillremittance}"/>
					</c:if>
					<c:if test="${bill.restrictionType == 'N' || (bill.billType != 'C' && bill.restrictionType == 'P' && isInsuranceBill)}">
						<insta:screenlink screenId="edit_pharmacy_item_amount" addPipe="true" label="Edit Pharmacy Item Amount "
									extraParam="?_method=editPharmacyItemAmount&visitId=${bill.visitId}&billNo=${bill.billNo}${newUXExtraParam}"
									title="${editpharmacyamount}" id="edit-pharmacy-item-amount-button" target="${isNewUX == 'Y' ? '_blank': ''}"/>
					</c:if>
					</td>
				</tr>
				<tr>
					<td>
						<c:set var="creditNoteAcressRight">
							<c:choose>
								<c:when test="${ifn:afmt(bill.totalClaim) eq ifn:afmt(0) && (bill.patientWriteOff == 'A' || urlRightsMap.create_patient_credit_note != 'A')}">
									false
								</c:when>
								<c:when test="${ifn:afmt(bill.totalAmount + bill.totalTax - bill.totalClaim - bill.totalClaimTax) eq ifn:afmt(0) && (bill.sponsorWriteOff == 'N' || bill.sponsorWriteOff == 'A' || urlRightsMap.create_sponsor_credit_note != 'A')}">
									false
								</c:when>
								<c:when test="${bill.restrictionType == 'P'}">
									false
								</c:when>
								<c:when test="${bill.patientWriteOff == 'A' && (bill.sponsorWriteOff == 'A' || bill.sponsorWriteOff == 'N')}">
									false
								</c:when>
								<c:when test="${bill.patientWriteOff == 'A' && urlRightsMap.create_sponsor_credit_note != 'A'}">
									false
								</c:when>
								<c:when test="${(bill.sponsorWriteOff == 'N' || bill.sponsorWriteOff == 'A') && urlRightsMap.create_patient_credit_note != 'A'}">
									false
								</c:when>
								<c:when test="${urlRightsMap.create_patient_credit_note == 'A' || urlRightsMap.create_sponsor_credit_note == 'A'}">
									true
								</c:when>
							</c:choose>
						</c:set>
				
						<c:choose>
							<c:when test="${urlRightsMap.create_patient_credit_note == 'A' && (bill.status  == 'C' || bill.status == 'F') && bill.totalAmount > 0 && creditNoteAcressRight == 'true'}">
								<c:choose>
									<c:when test="${bill.patientWriteOff != 'A'}">
										| <a target="${isNewUX == 'Y' ? '_blank': ''}" id="create-credit-note-button" href="${cpath}/billing/PatientCreditNote.do?_method=getCreditNoteScreen&billNo=${bill.billNo}" >
										<insta:ltext key="js.billing.billlist.createcreditnote"></insta:ltext></a>
									</c:when>
									<c:when test="${bill.sponsorWriteOff != 'N' && bill.sponsorWriteOff != 'A'}">
										| <a target="${isNewUX == 'Y' ? '_blank': ''}" id="create-credit-note-button" href="${cpath}/billing/SponsorCreditNote.do?_method=getCreditNoteScreen&billNo=${bill.billNo}" >
										<insta:ltext key="js.billing.billlist.createcreditnote"></insta:ltext></a>
									</c:when>
								</c:choose>
							</c:when>
							<c:when test="${urlRightsMap.create_sponsor_credit_note == 'A'  && (bill.status  == 'C' || bill.status == 'F') && bill.totalAmount > 0 && creditNoteAcressRight == 'true'}">
								<c:choose>
									<c:when test="${bill.sponsorWriteOff != 'N' && bill.sponsorWriteOff != 'A'}">
										| <a target="${isNewUX == 'Y' ? '_blank': ''}" id="create-credit-note-button" href="${cpath}/billing/SponsorCreditNote.do?_method=getCreditNoteScreen&billNo=${bill.billNo}" >
										<insta:ltext key="js.billing.billlist.createcreditnote"></insta:ltext></a>
									</c:when>
									<c:when test="${bill.patientWriteOff != 'A'}">
										| <a target="${isNewUX == 'Y' ? '_blank': ''}" id="create-credit-note-button" href="${cpath}/billing/PatientCreditNote.do?_method=getCreditNoteScreen&billNo=${bill.billNo}" >
										<insta:ltext key="js.billing.billlist.createcreditnote"></insta:ltext></a>
									</c:when>
								</c:choose>
							</c:when>
						</c:choose>
						<c:if test="${pendingPrescriptionModule}">
							<insta:screenlink screenId="patient_pending_prescription" addPipe="true" label="Pending Prescriptions"
									extraParam="/index.htm#/pendingPrescription?mr_no=${patient.mr_no}" title="Pending Prescriptions" 
									id="patient_pending_prescription" target="${isNewUX == 'Y' ? '_blank': ''}" />
						</c:if>
					</td>
				</tr>
			</c:otherwise>
		</c:choose>
	</table>
</c:if>
	<div id="showAlertRemarksDialog" style="display: none">
		<div class="bd">
		<fieldset class="fieldSetBorder">
			<table width="100%" >
				<tr>
					<td style="width: 17px"><insta:ltext key="billing.patientbill.details.label"/>:</td>
					<td><label id="alertLabelName"></label></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td style="width: 18px"><insta:ltext key="billing.patientbill.details.remarks"/>:</td>
					<td><label id="alertLabelRemarks"></label></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</fieldset>
		</div>
	</div>
</form>

<form name="editform">
<input type="hidden" name="fieldImgText" id="fieldImgText" value=""/>
<input type="hidden" name="fieldImgSrc" id="fieldImgSrc" value="" />
<input type="hidden" id="editRowId" value=""/>
<input type="hidden" name="eDeductionAmt" id="eDeduction" value=""/>
<input type="hidden" name="eInsCalcReq" id="eInsCalcReq" value=""/>
<div id="editChargeDialog" style="display:none">
	<div class="bd">
		<div id="editFormFields">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.edititemchargedetails"/></legend>
			<table class="formtable" id="editChgTbl">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.chargegroup"/>:</td>
					<td class="forminfo"><label id="eChargeGroup"></label></td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.chargehead"/>:</td>
					<td class="forminfo"><label id="eChargeHead"></label></td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.description"/>:</td>
					<td class="forminfo" colspan="3"><label id="eDescription"></label></td>
				</tr>

				<tr>
					<td colspan="6">
					<div id="itemDetails" style="margin-bottom:10px;">
						<dl class="accordion">
						  <dt>
					    	<div style="float:left; margin-right: 5px; display:block;" id="itemDetailsImg">
								<img width="16" height="16" src="<%=request.getContextPath()%>/images/arrow_down.png">
							</div>
							<div style="float:left"><insta:ltext key="billing.patientbill.details.otheritemdetails"/></div>
						    <div class="clrboth"></div>
						  </dt>
						  <dd id="itemDetailsDD">
							<table class="formtable" width="100px">
								<tr>
							    	<td class="formlabel"><insta:ltext key="billing.patientbill.details.servicegroup"/>:</td>
							    	<td class="forminfo"><label id="eserviceGroup"></td>
							    	<td class="formlabel"><insta:ltext key="billing.patientbill.details.service"/>&nbsp;<insta:ltext key="billing.patientbill.details.sub"/>&nbsp;<insta:ltext key="billing.patientbill.details.group"/>:</td>
							    	<td class="forminfo"><label id="eserviceSubGroup"></td>
							    	<td class="formlabel"></td>
							    	<td class="forminfo">&nbsp;</td>
							    	<td class="formlabel"></td>
							    	<td class="forminfo">&nbsp;</td>
				    			</tr>
				    			<tr>
				    				<td class="formlabel"><insta:ltext key="billing.patientbill.details.itemdescription"/>:</td>
									<td class="forminfo" colspan="3"><label id="eItemDescription"></label></td>
				    			</tr>
				    			<tr>
									<td class="formlabel"><insta:ltext key="billing.patientbill.details.itemremarks"/>:</td>
									<td colspan="3"><input type="text" style="width:416px" name="eItemRemarks" onchange="setEdited()"></td>
								</tr>
							</table>
						</dd>
						</dl>
					</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.date"/> & <insta:ltext key="billing.patientbill.details.time"/>:</td>
					<td>
						<fmt:formatDate var="posteddate" pattern="dd-MM-yyyy" value="${currentDate}"/>
						<fmt:formatDate var="postedtime" pattern="HH:mm" value="${currentDate}"/>

						<insta:datewidget name="ePostedDate" value="${posteddate}" valid="past" btnPos="topleft" onchange="setEdited()"/>
						<input type="text" size="4" name="ePostedTime" value="${postedtime}" class="timefield" onchange="setEdited()"/> 
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.rateplancode"/>:</td>
					<td>
						<input type="text" name="eCode" maxlength="50" onchange="setEdited()"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.conductedby"/>:</td>
					<td valign="top">
						<div id="eConducted_wrapper" class="autoComplete">
							<input type="text" name="eConducted" id="eConducted" onchange="setEdited()"/>
							<div id="eConducted_dropdown"></div>
						</div>
						<input type="hidden" name="eConductedId"/>
						<input type="hidden" name="eCondDocRequired"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.details"/>:</td>
					<td colspan="3"><input type="text" style="width:428px" name="eRemarks" onchange="setEdited()"></td>
					<c:if test="${hasDynaPackage}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.amtincluded"/>:</td>
						<td>
							<input type="text" name="eAmtIncluded" onchange="recalcEditChargeAmount();setEdited();"/>
							<input type="hidden" name="eAmtIncludedOrig">
						</td>
					</c:if>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.remarks"/>:</td>
					<td colspan="3"><input type="text" style="width:428px" name="eUserRemarks" onchange="setEdited()"></td>
					<c:if test="${hasDynaPackage}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.qtyincluded"/>:</td>
						<td>
							<input type="text" name="eQtyIncluded" onchange="recalcEditChargeAmount();setEdited();"/>
							<input type="hidden" name="eQtyIncludedOrig">
						</td>
					</c:if>
				</tr>
				<tr>
					<td class="formlabel">
						<c:choose>
							<c:when test="${multiPlanExists}">
								<insta:ltext key="billing.patientbill.details.pripriorauthno"/>:
							</c:when>
							<c:otherwise>
								<insta:ltext key="billing.patientbill.details.priorauthno"/>:
							</c:otherwise>
						</c:choose>
					</td>
					<td><input type="text" name="ePreAuthId" onchange="setEdited();">
					</td>
						<td class="formlabel" >
						<c:choose>
							<c:when test="${multiPlanExists}">
								<insta:ltext key="billing.patientbill.details.pripriorauthmode"/>:
							</c:when>
							<c:otherwise>
								<insta:ltext key="billing.patientbill.details.priorauthmode"/>:
							</c:otherwise>
						</c:choose>
					</td>
					<td>
						<insta:selectdb  name="ePreAuthModeId" value="" table="prior_auth_modes"
							valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
							onchange="setEdited()" dummyvalue="${dummyvalue}"/>
					</td>
					<c:if test="${hasDynaPackage}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.pkgfinalized"/>:</td>
						<td>
							<input type="checkbox" name="ePackageFinalized" onchange="setEdited();"/>
							<input type="hidden" name="ePackageFinalizedOrig">
						</td>
					</c:if>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.rate"/>:</td>
					<td>
						<input type="text" name="eRate" onchange="chkRateVariation();adjustPackageCharges('${chargeId}');recalcEditChargeAmount();setEdited();setInsCalcReq();rateChanged();"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.qty"/>:</td>
					<td>
						<input type="text" name="eQty" onchange="recalcEditChargeAmount();setEdited();setInsCalcReq();qtyChanged();"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td>
						<input type="text" name="eAmt" readonly="1"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.discountauth"/>:</td>
					<td valign="top">
						<div id="disoverall_wrapper" class="autoComplete">
							<input type="text" name="overallDiscByName" id="overallDiscByName"
								size="21" maxlength="50" onchange="setEdited();discAuthChanged();"/>
				    		<div id="overallDiscByName_dropdown"></div>
			    		</div>
			    		<input type="hidden" name="overallDiscBy" id="overallDiscBy"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.discount"/>:</td>
					<td>
						<input type="text" name="overallDiscRs" id="overallDiscRs"
								onchange="adjustPackageCharges();setDiscEdited(this);discChanged();recalcEditChargeAmount();setEdited();setInsCalcReq();"/>
					</td>
					<c:if test="${isInsuranceBill}">
						<td class="formlabel">
							<c:choose>
								<c:when test="${multiPlanExists}">
									<insta:ltext key="billing.patientbill.details.priclaim"/>:
								</c:when>
								<c:otherwise>
									<insta:ltext key="billing.patientbill.details.claim"/>:
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<input type="text" name="eClaimAmt" onchange="setDeductionAmt('P');adjustPackageCharges();setEdited();setInsCalcReq();"/>
							<input type="hidden" name="eClaimLocked"/>
							<input type="hidden" name="ePriIncludeInClaimCalc"/>
						</td>
					</c:if>
				</tr>
				<c:if test="${multiPlanExists}">
					<tr>
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.secpriorauthno"/>:</td>
						<td>
							<input type="text" name="eSecPreAuthId" onchange="setEdited();">
						</td>
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.secpriorauthmode"/>:</td>
						<td>
							<insta:selectdb  name="eSecPreAuthModeId" value="" table="prior_auth_modes"
							valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
							onchange="setEdited()" dummyvalue="${dummyvalue}"/>
						</td>
						<c:if test="${isInsuranceBill}">
							<td class="formlabel"><insta:ltext key="billing.patientbill.details.secclaim"/>:</td>
							<td>
								<input type="text" name="eSecClaimAmt" onchange="setDeductionAmt('S');adjustPackageCharges();setEdited();setInsCalcReq();"/>
								<input type="hidden" name="eSecIncludeInClaimCalc"/>
							</td>
						</c:if>
					</tr>
				</c:if>
			</table>
			<div id="package-component-details" class="package-component-details">

			</div>
		</fieldset>

		<div id="discountTable" style="margin-bottom:10px;">
		<dl class="accordion">
		  <dt>
	    	<div style="float:left; margin-right: 5px; display:block;" id="splitDiscountImg">
				<img width="16" height="16" src="<%=request.getContextPath()%>/images/arrow_down.png">
			</div>
			<div style="float:left"><insta:ltext key="billing.patientbill.details.splitdiscountdetails"/></div>
		    <div class="clrboth"></div>
		  </dt>
		  <dd id="discountDD">
			<table class="formtable" width="100px">
				<tr>
					<td class="formlabel">
						<input type="checkbox" name="discountType" value="split"
							onclick="onChangeDiscountType('')" ${bill.status != 'A'? 'disabled':''}
							${roleId != '1' && roleId != '2' && actionRightsMap.allow_discount != 'A'? 'disabled':''}/>
						<insta:ltext key="billing.patientbill.details.splitdiscount"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.conductingdoctordiscountauth"/>:</td>
					<td valign="top">
						<div id="disconduct_wrapper" class="autoComplete">
		    				<input type="text" name="discConductDocName" id="discConductDocName"
		    					size="21" maxlength="50" onchange="setEdited()"/>
			    			<div id="discConductDocName_dropdown"></div>
		    			</div>
		    			<input type="hidden" name="discConductDoc" id="discConductDoc"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td>
						<input type="text" name="discConductDocRs" id="discConductDocRs" onchange="adjustPackageCharges();setEdited();setInsCalcReq();discChanged();"
							onblur="return resetDiscountTotalRs();"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.prescribingdoctordiscountauth"/>:</td>
					<td valign="top">
						<div id="discpresc_wrapper" class="autoComplete">
							<input type="text" name="discPrescDocName" id="discPrescDocName"
								size="21" maxlength="50" onchange="setEdited()"/>
				    		<div id="discPrescDocName_dropdown"></div>
			    		</div>
			    		<input type="hidden" name="discPrescDoc" id="discPrescDoc"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td>
						<input type="text" name="discPrescDocRs" id="discPrescDocRs" onchange="adjustPackageCharges();setEdited();setInsCalcReq();discChanged();"
							onblur="return resetDiscountTotalRs();"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.referrerdiscountauth"/>:</td>
					<td valign="top">
						<div id="discref_wrapper" class="autoComplete">
		    				<input type="text" name="discRefDocName" id="discRefDocName"
		    					size="21" maxlength="50" onchange="setEdited()"/>
			    			<div id="discRefDocName_dropdown"></div>
		    			</div>
		    			<input type="hidden" name="discRefDoc" id="discRefDoc"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td>
						<input type="text" name="discRefDocRs" id="discRefDocRs" onchange="adjustPackageCharges();setEdited();setInsCalcReq();discChanged();"
							onblur="return resetDiscountTotalRs();"/>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.hospitaldiscountauth"/>:</td>
					<td valign="top">
						<div id="dischosp_wrapper" class="autoComplete">
			   				<input type="text" name="discHospUserName" id="discHospUserName"
			   					size="21" maxlength="50" onchange="setEdited()"/>
			    			<div id="discHospUserName_dropdown"></div>
		    			</div>
		    			<input type="hidden" name="discHospUser" id="discHospUser"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td>
						<input type="text" name="discHospUserRs" id="discHospUserRs" onchange="adjustPackageCharges();setEdited();setInsCalcReq();discChanged();"
							onblur="return resetDiscountTotalRs();"/>
					</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.totaldiscount"/>:</td>
					<td><input type="text" name="totalDiscRs" id="totalDiscRs" readonly/></td>
				</tr>
			</table>
		</dd>
		</dl>
		</div>
		</div>
		<table>
			<tr>
				<td><input type="button" onclick="onEditSubmit(false)" value="OK" /></td>
				<td><input type="button" onclick="onEditCancel()" value="Cancel"/></td>
				<td><input type="button" onclick="showNextOrPrevCharge('prev')" name="prevBtn" value="<<Prev"/></td>
				<td><input type="button" onclick="showNextOrPrevCharge('next')" name="nextBtn" value="Next>>"/></td>
			</tr>
		</table>
	</div>
</div>
</form>

<c:if test="${allowAdd}">
	<insta:AddOrderDialog visitType="${patient.visit_type}" includeOtDocCharges="Y"/>
</c:if>
<table width="100%"><tr>
	<c:if test="${not empty bill}">
	<c:if test="${(bill.patientWriteOff == 'M' && (patDueAmt > 0.00 || patDueAmt < 0.00) )|| (bill.sponsorWriteOff == 'M' && (spoDueAmt > 0.00 || spoDueAmt < 0.00))}">
		<td width="50%" style="float:left" align="left">
			<div class="legend" style="float:left">
			<div class="flag"><b><span style="color:red">*&nbsp;&nbsp;</span></b></div>
			<div class="flagText">Marked For Write Off</div>
			</div>
		</td>
	</c:if>
	</c:if>
	<td width="50%">
		<div class="legend">
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="billing.patientbill.details.cancelled"/></div>
		<c:if test="${hasDynaPackage}">
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText"><insta:ltext key="billing.patientbill.details.excludedfrompkg"/></div>
			<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
			<div class="flagText"><insta:ltext key="billing.editpharmacyitemamounts.items.partiallyincluded"/></div>
		</c:if>
		<c:if test="${hasRewardPointsEligibility}">
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText"><insta:ltext key="billing.patientbill.details.eligibletoredeem"/></div>
		</c:if>
		</div>
	</td>
	</tr>
</table>
<div id="emailDialog" class="emailDialog" style="background-color:#F5F5F5;">
			<div class="emailHeader" style="background-color:#D8D8D8;">
			  <div class="emailHeading">
			  	<b><insta:ltext key="billing.patientbill.details.sendpatientbill"/></b>
			  </div> 
			  <span id="closeButton" onClick="closeEmailDiv();">
			  	<img src="${cpath}/images/close-button.png" class="close-button">
			  </span>
			</div>
			<div id="emailContent" class="emailContent">
				<form>
					<table class="emailTable">
						<tr><td style="color:#9B9B9B">
							<insta:ltext key="billing.patientbill.details.tosendemail"/>
						<tr><td>
							<input type="text" id="emailVal" oninput ="validateEmail();" value="${patientEmail}" placeholder='<insta:ltext key="billing.patientbill.details.noemailfound"/>'>
						<tr><td>
							<div id="errormsg"><insta:ltext key="billing.patientbill.details.invalidemail"/></div>
						<tr><td align="right">
							<input type="button" value="Cancel" onClick="closeEmailDiv();">
							<input type="button" class="blue-btn" id="sendMailButton" value="  Send  " 
							onClick='updateSendMail(document.getElementById("emailVal").value,"${ifn:cleanHtml(patientEmail)}","${billTypeDisplay}");'>
					</table>
				</form>
			</div>	
</div>
<div id="emailDialogStatus" class="emailDialog" style="background-color:#F5F5F5;">
			<div class="emailHeader" style="background-color:#D8D8D8;">
			  <div class="emailHeading">
			  	<b><insta:ltext key="billing.patientbill.details.sendpatientbill"/></b>
			  </div> 
			  <span id="closeButton" onClick="closeEmailDiv();">
			  	<img src="${cpath}/images/close-button.png" class="close-button">
			  </span>
			</div>
			<div id="emailStatus" class="emailContent">
			<div id="emailStatusVal"></div>
			</div>	
</div>
<a href="#" id="email-tour" style="display: none;"></a>
<div style="display: none;" >
  <ul id="email-tour-steps">
    <li data-id=".step-1" data-position="top">
      <h2><insta:ltext key="billing.patientbill.details.introemail"/></h2>
      <p><insta:ltext key="billing.patientbill.details.tourheading"/><br>
      <insta:ltext key="billing.patientbill.details.gopaperless"/></p>
    </li>
  </ul>
</div>
<form name="billCSVdownloadForm" action="BillAction.do">
	<input type="hidden" name="billNo" value=""/>
	<input type="hidden" name="_method" value="downloadCSVFile"/>
	<input type="hidden" name="template_id" value=""/>
	<input type="hidden" name="printerId" value=""/>

</form>
<div id="loyaltyOffersDialog" style="display:none">
<div class="hd" id="headerLoyaltyOffers"></div>
	<div class="bd">
	<iframe id="offerIframe" width="600px" height="400px"></iframe>
	</div>
</div>
<div id="signatureDialog" style="display:none">
	<div class="hd" id="headerSignature"> </div>
		<div class="bd" style="width:100%; height:100%">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"> <insta:ltext key="billing.patientbill.details.patient.signature"/> </legend>
				<table class="formtable" width="100%" id="fieldsTable">	
						<tr>
							<td class="fieldSetBorder" style="width:435px;">
								<div id="signatureFieldDiv" 
								    style="width:100%;height:150px;">								
									<c:set var="imageFieldUrl" value="${bill.billSignature}"/>
										<img style="display: none; max-height: 100%;"
										src='<c:out value="${imageFieldUrl}"/>' name="billSignatureExternal" id="billSignatureExternal" />
										<canvas  style="width:100%; height:100%;"
												name="billSignatureCanvas" id="billSignatureCanvas" />
										</div>
							</td>
							<td class="formlabel" style="width:150px" valign="top">
								<div id="externalButton" style="padding: 10px;">
									<button id="startCaptureBtnExternal" name="startCaptureBtnExternal"
									value="connectAndClear" onclick="return captureSignature(this);"
									style="width:95%" class="button">Read Signature Pad</button>
								</div>
								<div id="canvasButton" style="padding: 10px;">
									<button id="startCaptureBtnCanvas" name="startCaptureBtnCanvas" onclick="captureCanvasSignature(signaturePad);"
									style="width:45%" class="button">Save</button>
									<button id="clearCaptureBtn" name="clearCaptureBtn" onclick="return clearCanvasSignature(signaturePad);"
									style="width:45%" class="button">Clear</button>
								</div>
								<div style="width:95%" id="captureModified" />
							</td>
						</tr>
			</table>
		</fieldset>
	</div>
</div>
<div id="couponRedemptionDialog" style="display:none">
	<div class="hd" id="headerCouponRedemption"></div>
		<div class="bd">
		<iframe id="couponRedemptionIframe" width="800px" height="600px"></iframe>
	</div>
</div>
<script>
    showLoader();
    $(document).ready(function(){
	  hideLoader();
    });
    <%-- global variables relating to this bill --%>
    var patientCredit = '${patientCredit}';
	var enabledOrderableItemApi = true;
 
	var signature = '${imageFieldUrl}';
	var bill_label_for_bill_later_bills = '${billLabelForBillLaterBills}';
	var orderableItemUrl = '${orderItemsUrl}';
	var totalDeposit = '${totalDeposit}';
	var showChargesAllRatePlan = 'A'; // hardcoded to 'A', because, in bill screen, item amount is a must see column
	var origBillStatus = '${ifn:cleanJavaScript(bill.status)}';
	var billType = '${ifn:cleanJavaScript(bill.billType)}';
	var visitType = '${ifn:cleanJavaScript(bill.visitType)}';
	var isPrimaryBill = '${ifn:cleanJavaScript(bill.isPrimaryBill)}';
	var billRatePlanId = '${ifn:cleanJavaScript(bill.billRatePlanId)}';
	var points_redemption_rate = ${empty points_redemption_rate ? 0 : points_redemption_rate};
	var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
	var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
	var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
	var totalBilledAmount = ${empty totalAmount ? 0 : totalAmount};
	var existingSponsorReceipts = ${empty existingSponsorReceipts ? 0 : existingSponsorReceipts};

	
	var priSponsorAmt = ${empty priSponsorAmt ? 0 : priSponsorAmt};
	var secSponsorAmt = ${empty secSponsorAmt ? 0 : secSponsorAmt};
	var primarySponsorsReceipt = ${empty primarySponsorsReceipt ? 0 : primarySponsorsReceipt};
	var secondarySponsorsReceipt = ${empty secondarySponsorsReceipt ? 0 : secondarySponsorsReceipt};
	var priRecdAmt = ${empty priRecdAmt ? 0 : priRecdAmt};
    var secRecdAmt = ${empty secRecdAmt ? 0 : secRecdAmt};
    var cashTransactionLimitAmt =  ${empty cashTransactionLimitAmt ? 0 : cashTransactionLimitAmt};
    var ipDepositSetOff = ${empty bill.ipDepositSetOff ? 0 : bill.ipDepositSetOff};
    var generalDepositSetOff = ${bill.depositSetOff-bill.ipDepositSetOff};
	
	var availableDeposits = ${empty availableDeposits ? 0 : availableDeposits};
	var ipDeposits = ${empty ipDeposits ? 0 : ipDeposits};
	var userPermissibleDiscount = ${permissibleDiscountPercenatge};
	var allowBillFinalization = '${allowBillFinalization}';
	var availableRewardPoints = ${empty availableRewardPoints ? 0 : availableRewardPoints};
	var availableRewardPointsAmount = ${empty availableRewardPointsAmount ? 0 : availableRewardPointsAmount};
	var billRewardPoints = ${empty bill ? 0 : bill.rewardPointsRedeemed};
	var openDate = '<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>';
	var openTime = '<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/>';
	var finalizedDate = '<fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/>';
	var finalizedTime = '<fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/>';
	var curDate = '<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>';
	var curTime = '<fmt:formatDate pattern="HH:mm" value="${currentDate}"/>';
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	var gPrintReceiptPrinterType = '${ifn:cleanJavaScript(param.printerType)}';
	var roleId = '${ifn:cleanJavaScript(roleId)}';
	var addToBillRights = '${actionRightsMap.addtobill_charges}';
	var allowDiscount = '${actionRightsMap.allow_discount}';
	var allowRateChange = '${actionRightsMap.allow_ratechange}';
	var allowRateIncr = '${actionRightsMap.allow_rateincrease}';
	var allowRateDcr = '${actionRightsMap.allow_ratedecrease}';
	var allowDynaPkgIncExc = '${actionRightsMap.allow_dyna_package_include_exclude}';
	var allowBackDate = '${actionRightsMap.allow_backdate}';
	var allowBackDateBillActivities = "${roleId == 1 || roleId == 2 ? 'A' :actionRightsMap.allow_back_date_bill_activities}";
	var editDate = '${actionRightsMap.edit_date}';
	var editBillRights = '${actionRightsMap.edit_bill}';
	var allowTaxEditRights ='${actionRightsMap.allow_tax_subgroup_edit}';
	var cancelBillRights = '${actionRightsMap.cancel_bill}';
	var cancelElementsInBillRights = '${actionRightsMap.cancel_elements_in_bill}';
	var allowRefundRights = '${actionRightsMap.allow_refund}';
	var customTemplate = '${ifn:cleanJavaScript(param.customTemplate)}';
	var userNameInBillPrint = '${genPrefs.userNameInBillPrint}';
	var claimServiceTaxPer = '${genPrefs.serviceTaxOnClaimAmount}';
	var patientDept = '${patient.dept_id}';
	var patientOrgId = '${patient.org_id}';
	var billOrgId  = '${bill.billRatePlanId}';
	var patientCategoryId = '${patient.patient_category_id}';
	var patientBedType = <insta:jsString value="${patient.bill_bed_type}"/>;
	var otherUnpaidBills = <%= request.getAttribute("otherUnpaidBillsJSON") %> ;
	var dischargeStatus = '${patient.discharge_flag}';
	var dischargeDate = '${patient.discharge_date}';
	var dischargeType = '${dischargeType}';
	var prescribingDocName = '';
	var prescribingDoctor = '';
	var fixedOtCharges = '${genPrefs.fixedOtCharges}';
	gPrescDocRequired = '${genPrefs.prescribingDoctorRequired}';
	var gOnePrescDocForOP = '${genPrefs.op_one_presc_doc}';
	<c:if test="${genPrefs.default_prescribe_doctor == 'Y'}">
		prescribingDocName = <insta:jsString value="${patient.doctor_name}"/>;
		prescribingDoctor = '${patient.doctor}';
	</c:if>
	var consultingDocName = <insta:jsString value="${patient.doctor_name}"/>;
	var patientType = '${patient.visit_type}';
	var regDate = '<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>';
	var regTime = '<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>';
	var adtmodule = '${preferences.modulesActivatedMap['mod_adt']}';
	var mod_adv_packages = '${preferences.modulesActivatedMap['mod_adv_packages']}';
	var sponsorBillorReceipt = '${ifn:cleanJavaScript(sponsor_bill_receipt)}';
	var sponsorBillNo = '${ifn:cleanJavaScript(bill.sponsorBillNo)}';
	var restrictionType = '${ifn:cleanJavaScript(bill.restrictionType)}';
	var pendingEquipmentFinalization = '${pendingEquipmentFinalization}';
	var pendingBedFinalization = '${pendingBedFinalization}';
	mealTimingsRequired = true;
	equipTimingsRequired = true;
	var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
	var separator_type = '${ifn:cleanJavaScript(genPrefs.separator_type)}';
	var currency_format = '${genPrefs.currency_format}';
	var serviceGroupsJSON = <%= request.getAttribute("serviceGroupsJSON") %> ;
	var servicesSubGroupsJSON = <%= request.getAttribute("servicesSubGroupsJSON") %> ;
	var doctorConsultationTypes = <%= request.getAttribute("doctorConsultationTypes") %> ;
	var allDoctorConsultationTypes = <%= request.getAttribute("allDoctorConsultationTypes") %> ;
	var allServiceSubgroupsList = <%= request.getAttribute("allServiceSubgroupsList") %> ;
	var regPref = <%= request.getAttribute("regPrefJSON") %> ;
	var doctorsList =  <%= request.getAttribute("doctorsJSON") %> ;
	var billPrintDefault = (billType == 'C') ? '${genPrefs.billLaterPrintDefault}' : '${genPrefs.billNowPrintDefault}';

	var serviceChargePer = '${genPrefs.serviceChargePercent}';
	var dynaPkgProcessedBefore = '${dynaPkgProcessedBefore}';
	var dynaPackageProcessed = '${ifn:cleanJavaScript(bill.dynaPkgProcessed)}';
	var existingDynaPackageId = '${bill.dynaPkgId}';
	var allowBillInsurance = '${allowBillInsurance}';
	var isTpa       = ${empty bill ? false : bill.is_tpa};
	var billNowTpa  = billType == 'P' && isTpa && (restrictionType == 'N' || restrictionType == 'P');
	var tpaBill     = isTpa && (restrictionType == 'N' || restrictionType == 'P');
	var visitPrimaryApprovalAmount = 0;
	var visitSecondaryApprovalAmount = 0;
	var visitApprovalAmount = 0;
	var otherBillsApprovalTotal = 0;
	var planId = ${empty patient.plan_id ? 0 : patient.plan_id};
	var initialNumberOfCharges = ${fn:length(billDetails.charges)}

	<c:if test="${isInsuranceBill}">
		visitPrimaryApprovalAmount = ${empty patient.primary_insurance_approval ? 0 : patient.primary_insurance_approval};
		visitSecondaryApprovalAmount = ${empty patient.secondary_insurance_approval ? 0 : patient.secondary_insurance_approval};
		visitApprovalAmount = visitPrimaryApprovalAmount + visitSecondaryApprovalAmount;
		otherBillsApprovalTotal = ${empty bill ? 0 : billsApprovalTotal - bill.primaryApprovalAmount - bill.secondaryApprovalAmount};
	</c:if>
	var allOrdersJSON = null;

	var planBean = <%= request.getAttribute("planBeanJSON") %> ;
	var drgBean = <%= request.getAttribute("drgBeanJSON") %> ;

	var priSponsorType = '${patient.sponsor_type}';
	var secSponsorType = '${patient.sec_sponsor_type}';
	var priSponsorId = '${patient.primary_sponsor_id}';
	var secSponsorId = '${patient.secondary_sponsor_id}';
	var priTpaName = <insta:jsString value="${patient.tpa_name}"/>;
	var secTpaName = <insta:jsString value="${patient.sec_tpa_name}"/>;
	var opType = '${patient.op_type}';
	var visitTpaBills = '${visitTpaBills}';
	var firstTpaBill = '${ifn:cleanJavaScript(firstTpaBill.billNo)}';

	var billLabelMasterJson = <%= new JSONSerializer().serialize(request.getAttribute("billLabelMasterMap"))%>;
	var billLabelId = '${bill.billLabelId}';
	var billRemarks = <insta:jsString value="${bill.billRemarks}"/>;
	var multiVisitBill = '${multiVisitBill}';
	var perdiemPlanBean = <%= request.getAttribute("perdiemPlanBeanJSON") %> ;

	var priPlanExists = '${bill.primaryPlanId != null &&  bill.primaryPlanId != '' }';
	var secPlanExists = '${bill.secondaryPlanId != null && bill.secondaryPlanId != ''}';

	var patientWriteOff = '${ifn:cleanJavaScript(bill.patientWriteOff)}';
	var sponsorWriteOff = '${ifn:cleanJavaScript(bill.sponsorWriteOff)}';
	var billCancelRequiresApproval = '${bill_cancellation_requires_approval}';
	var cancellationApprovalStatus = '${ifn:cleanJavaScript(bill.cancellationApprovalStatus)}';
	var screenid = '';
	var ipDepositExists = '${ipDepositExists}';
	var showIpDesposit = '${bill.visitType == 'i'}';
	var sponserType=  '<%= request.getAttribute("sponserType") %>' ;
	var discountPlansJSON = ${discountPlansJSON};
	var isCustomFieldsExist = '${isCustomFieldsExist}';
	var is_Baby = '${is_Baby}';
	var visitAdjExists = '${visitAdjExists}';
	var visitDiscountPlanId = ${visitDiscountPlanId};
	
	var limitType = '${limitType}';
	var caseRateCount = '${caseRateCount}';
	
	var billNumber= '${bill.billNo }';
	var mr = '${patient.mr_no}';
	var patientEmailId = "${patientEmail}";
	var patientPhone='${patientPhone}';
	var patientName=<insta:jsString value="${patient.patient_name}"/>;
	if( patientPhone.substring(0, 3)=='+91')
		patientPhone=patientPhone.substring(3);
	if( patientPhone.substring(0, 2)=='91')
		patientPhone=patientPhone.substring(2);
	var loyaltyURL = '${loyaltyOfferURL}'+"?store_code="+'${storeCode}'+"&&mobile="+patientPhone;
	var today = new Date();
	var todayDate = today.getDate();
	var todayMonth= gMonthShortNames[today.getMonth()];
	var loyaltyHeader="Offers for "+patientName+" - Today ("+todayDate+"-"+todayMonth+")";
	var manualEmailStatus = '${manualEmailStatus}';
	var patientDueSMSStatus = '${patientDueSMSStatus}';
	var eClaimModule = ${preferences.modulesActivatedMap['mod_eclaim'] == 'Y'};
	var createSponsorCreditNote= ${urlRightsMap.patient_writeoff == "A"};
	var checkClaimStatusForWriteOff = ${!eClaimModule && bill.status == 'F' && createSponsorCreditNote && isInsuranceBill};
	var ipCreditLimitAmount = ${not empty bill.ipCreditLimitAmount ? bill.ipCreditLimitAmount : 0};
	var ip_credit_limit_rule = <insta:jsString value="${ip_credit_limit_rule}"/>;
	var exclVisitPatientDue = ${exclVisitPatientDue};
	var creditLimitDetailsJSON = <%=request.getAttribute("creditLimitDetailsJSON")%>;
	var isMvvPackage = ${multiVisitBill == 'Y' && not empty mvpackage ? true : false }; 

	
	var jModulesActivated = <%= request.getAttribute("modulesActivatedJSON") %>;
	var jChargeHeads = <%= request.getAttribute("chargeHeadsJSON") %>;
	var jChargeGroups = <%= request.getAttribute("chargeGroupsJSON") %>;
	var pendingtests = '${pendingtest}';
	var pendingtestsforbill = <%= request.getAttribute("pendingtestsforbill") %>;
	var pendingconsultationforbill = <%= request.getAttribute("pendingconsultationforbill") %>;
	var jDiscountAuthorizers = <%= request.getAttribute("discountAuthorizersJSON") %>;
	var jdiscountCategories = <%= request.getAttribute("discountCategoriesJSON") %>;
	var jProcedureNameList = <%= request.getAttribute("procedureNameList") %>;
	var jDynaPkgDetailsList = <%= request.getAttribute("dynaPkgDetailsJSON") %>;
	var jDynaPkgNameList = <%= request.getAttribute("dynaPkgNameJSON") %>;
	var jPerdiemCodeDetailsList = <%= request.getAttribute("perdiemCodeDetailsJSON") %>;
	var jPolicyNameList = <%= request.getAttribute("policyCharges") %>;
	var planList = <%= request.getAttribute("planList") %>;
	var jDoctors = <%= request.getAttribute("doctorsJSON") %>;
	var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
	var jForeignCurrencyList = <%= request.getAttribute("foreignCurrencyListJSON") %>;
	var taxSubGroupsList = <%= request.getAttribute("taxSubGroupsJSON") %>;
	var currencyRowCount = !empty(jForeignCurrencyList) ? 1 : 0;
	var paymentRowsUncloned = 2;
	var paymentRows = 7 + currencyRowCount;//commisssion Amount,commission Percentage added as extra row
	var printURL = '${pop_up}';
	totAmtPaise = ${totalNetAmount*100};
	var hasDynaPackage = billType != 'P' && (visitType == 'i' || visitType == 'o') && jDynaPkgNameList != null && jDynaPkgNameList.length > 0;
	<c:if test="${not empty anaeTypesJSON}">
		var anaeTypes = ${anaeTypesJSON};
	</c:if>
	var useDRG = '${patient.use_drg}';
	var hasDRGCode = ${hasDRGCode};
	var usePerdiem = '${patient.use_perdiem}';
	var isPerDiemPrimaryBill = ${isPerDiemPrimaryBill};
	var isPhBillNowReturns = (billType == 'P' && restrictionType == 'P' && totAmtPaise < 0);
	var hasRewardPointsEligibility = (!isTpa && (availableRewardPoints > 0 || billRewardPoints != 0) && (!isPhBillNowReturns));
	var templateList = <%= request.getAttribute("templateListJSON") %>;
	var multiPlanExists = ${not empty multiPlanExists ? multiPlanExists : false};
	var allowAdd = ${allowAdd};
	var disableDiscCategory = ${!canChangeDiscountPlan};
	var discountPlanAllowRt = '${actionRightsMap.allow_discount_plans_in_bill}';
	var jPrimaryCaseRateDetails = <%= request.getAttribute("primaryCaseRateDetails") %>;
	var jSecondaryCaseRateDetails = <%= request.getAttribute("secondaryCaseRateDetails") %>;
	var billCharges = <%= (request.getAttribute("billDetails") != null
		&& ((BillDetails) request.getAttribute("billDetails")).getCharges() != null)
	? new JSONSerializer().serialize(((BillDetails) request.getAttribute("billDetails")).getCharges())
	: new JSONSerializer().serialize(new ArrayList()) %>;
	var income_tax_cash_limit_applicability = '${incomeTaxCashLimitApplicability}';
	var jVisitIssueReturnReferences = ${visit_issue_return_references_JSON};
	var typeOfPendingActivitiesRequired = '${ genPrefs.billPendingValidationActivityTypes}';
</script>
    <jsp:include page="/pages/dialogBox/loyaltyDialog.jsp"></jsp:include>
    <jsp:include page="/pages/dialogBox/processPaymentDialog.jsp"></jsp:include>
<script>  
    //Show The #chargesTable block after page is ready.
	function initShowChargesTable() {
        $("#chargesTable").css("display","table");
	}

	$(document).ready(function() {
		if(initialNumberOfCharges && initialNumberOfCharges > 100){
		  let chargesTableDelay = 1000
		  if(initialNumberOfCharges > 200) {
			chargesTableDelay += 3000;
		  }
		  setTimeout('initShowChargesTable()', chargesTableDelay);
		}else{
			initShowChargesTable();
		}
    });
   
</script>  
</body>
</html>


