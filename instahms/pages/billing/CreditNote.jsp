<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head>
	<insta:js-bundle prefix="billing.creditNote"/>
	<insta:js-bundle prefix="billing.salucro"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<title><insta:ltext key="js.billing.creditNote.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<c:set var="bill" value="${billDetails.bill}"/>
	<c:set var="no_of_credit_debit_card_digits" value="${genPrefs.no_of_credit_debit_card_digits}"/>
	<c:set var="isInsuranceBill" value="${bill.is_tpa && (bill.restrictionType == 'N' || bill.restrictionType == 'P')}"/>
	<c:set var="isNonInsuredBillOfInsPatient" value="${patient.primary_sponsor_id != null && patient.primary_sponsor_id != '' && patient.org_id != bill.billRatePlanId }" />
	<c:set var="availableDeposits" value='${depositDetails.map.total_deposits -	depositDetails.map.total_set_offs + depositDetails.map.deposit_set_off}'/>
	<c:set var="availableRewardPoints"
	value='${rewardPointDetails.map.total_points_earned - rewardPointDetails.map.total_points_redeemed - rewardPointDetails.map.total_open_points_redeemed + rewardPointDetails.map.points_redeemed}'/>
	<script>
		var origBillStatus = '${ifn:cleanJavaScript(bill.status)}';
		var isInsuranceBill= ${isInsuranceBill};
		var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
		var billOrgId  = '${bill.billRatePlanId}';
		var patientBedType = <insta:jsString value="${patient.bill_bed_type}"/>;
		var patientSponsorCreditNotePath = '${urlRightsMap.create_patient_credit_note == "A" && bill.patientWriteOff != "A" ? "PatientCreditNote" : "SponsorCreditNote"}';
		var screenRightsForPatientCreditNote ='${urlRightsMap.create_patient_credit_note}';
		var screenRightsForSponsorCreditNote ='${urlRightsMap.create_sponsor_credit_note}';
		var primaryClosureType='${ifn:cleanJavaScript(bill.primaryClosureType)}';
		var secondaryClosureType= '${ifn:cleanJavaScript(bill.secondaryClosureType)}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="billing/creditNote.js"/>
	<insta:link type="script" file="billingDynaPkg.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billPaymentCommon.js"/>
</head>


<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="points_redemption_rate" value="${not empty genPrefs.points_redemption_rate ? genPrefs.points_redemption_rate : 0}"/>
<c:set var="existingReceipts" value="${bill.totalReceipts}"/>
<c:set var="existingRecdAmount" value="${bill.claimRecdAmount}"/>
<c:set var="totalAmount" value="${bill.totalAmount}" />
<c:set var="existingSponsorReceipts" value="${bill.totalPrimarySponsorReceipts + bill.totalSecondarySponsorReceipts}"/>
<c:set var="patientSponsorCreditNotePath" value='${urlRightsMap.create_patient_credit_note == "A" && bill.patientWriteOff != "A"? "PatientCreditNote" : "SponsorCreditNote"}'/>
<c:set var="billPrintRights" value="${urlRightsMap.bill_print}"/>
<c:set var="editEmailRights" value="${urlRightsMap.bill_email}"/>



<body onload="init();ajaxForPrintUrls();hidePaymentModeForDeposit();" class="yui-skin-sam">
<c:set var="all2"><insta:ltext key="billing.patientbill.details.all.in.brackets"/></c:set>
<c:set var="priclaimamt"><insta:ltext key="billing.patientbill.details.prisponsor.or.claimamount"/></c:set>
<c:set var="secclaimamt"><insta:ltext key="billing.patientbill.details.secsponsor.or.claimamount"/></c:set>
<c:set var="sponserclaimamt"><insta:ltext key="billing.patientbill.details.sponsor.or.claimamount"/></c:set>
<c:set var="patientamt"><insta:ltext key="billing.patientbill.details.patientamount"/></c:set>

<table width="100%">
	<tr>
		<td width="100%"><h1><insta:ltext key="js.billing.creditNote.title"/></h1></td>
	</tr>
</table>

<insta:patientdetails patient="${patient}" />
<form name="mainform" method="POST" action="${cpath}/billing/${patientSponsorCreditNotePath}.do" autocomplete="off">
	<input type="hidden" name="_method" value="saveCreditNoteDetails">
	<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(bill.billNo)}">
	<input type="hidden" name="billingcounterId" value="${billingcounterId}">
	<input type="hidden" name="patientWriteOff" id="patientWriteOff" value="${bill.patientWriteOff}">
	<input type="hidden" name="sponsorWriteOff" id="sponsorWriteOff" value="${bill.sponsorWriteOff}">
	<input type="hidden" name="visitId" id="visitId" value="${patient.patient_id}">
	<c:set var="isPhBillNowReturns" value="${bill.billType == 'P' && bill.restrictionType == 'P' && bill.totalAmount < 0}"/>
	<c:set var="hasRewardPointsEligibility" value="${(!isInsuranceBill && (availableRewardPoints > 0 || (not empty rewardPointDetails.map.points_redeemed && rewardPointDetails.map.points_redeemed != 0)) && !isPhBillNowReturns)}" />
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="js.billing.creditNote.details"/></legend>
		<table class="formtable" width="100%">
			<tr>
				<td align="right">
				<input type="radio" value="new" name="patientCreditNote" id="patientCreditNote" onclick="setCreditType('P')"
					${patientSponsorCreditNotePath eq 'PatientCreditNote' && bill.patientWriteOff != 'A' ?"checked":""}/>
				<input type="hidden" name="creditType" id="creditType" value="">
				</td><td align="left">
				<label for="patientCreditNote"><insta:ltext key="js.billing.creditNote.patientCreditNote"/></label>
				</td>
				<td align="right">
				<input type="radio" value="new" name="sponsorCreditNote" id="sponsorCreditNote" onclick="setCreditType('S')"
					${patientSponsorCreditNotePath eq 'SponsorCreditNote' && bill.sponsorWriteOff != 'A' ?"checked":""}/></td>
				<td align="left">	
				<label for="sponsorCreditNote"><insta:ltext key="js.billing.creditNote.sponsorCreditNote"/></label>
				</td>
				<td align="right">	
				<label><insta:ltext key="js.billing.creditNote.sponsorType"></insta:ltext></label>:
				</td><td align="left">
				<select id="sponsor_type" name="sponsor_type" class="dropDown"
					onChange="onChangeOfSponsorType();" disabled>
					<option value="">--Select--</option>
					<option value="Primary"><insta:ltext key="js.billing.creditNote.sponsorTypePrimary"/></option>
					<option value="Secondary"><insta:ltext key="js.billing.creditNote.sponsorTypeSecondary"/></option>
				</select>
				</td>
				</tr><tr>
				<td align="right">
				<insta:ltext key="js.billing.creditNote.creditNoteReasons"/>:
				</td><td align="left">
					<input type="text" value="" name="creditNoteReasons" id="creditNoteReasons" maxlength="100" >
				</td>
				<td align="right"><insta:ltext key="js.billing.creditNote.creditNoteRemarks"/>:
				</td><td align="left">
					<input type="text" value="" name="creditNoteRemarks" id="creditNoteRemarks" maxlength="100">
				</td>
			</tr>
		</table>
	</fieldset>
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
		
						<td></td>
						<td>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="chargesTable" border="0" width="100%">
		<tr bgcolor="#8FBC8F" id="chRow0">
			<th style="width: 80px"><insta:ltext key="billing.patientbill.details.date"/></th>
			<th><insta:ltext key="billing.patientbill.details.head"/></th>
			<th style="width: 60px" title="${ifn:cleanHtmlAttribute(code)}"><insta:ltext key="billing.patientbill.details.code"/></th>
			<th style="width: 130px"><insta:ltext key="billing.patientbill.details.description"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.rate"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.qty"/></th>
			<th class="number" style="width: 10px"></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.disc"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.amt"/></th>
			<c:choose>
				<c:when test="${isInsuranceBill}">
					<c:choose>
						<c:when test="${multiPlanExists}">
							<th class="number" title="${priclaimamt}"><insta:ltext key="billing.patientbill.details.prisponsor"/></th>
							<th class="number" title="${secclaimamt}"><insta:ltext key="billing.patientbill.details.secsponsor"/></th>
							<th class="number" title="${patientamt}"><insta:ltext key="billing.patientbill.details.patient"/></th>
							<th class="number"><insta:ltext key="billing.patientbill.patient.creditamt"/></th>
							<th class="number"><insta:ltext key="billing.patientbill.primary.sponsor.creditamt"/></th>
							<th class="number"><insta:ltext key="billing.patientbill.secondary.sponsor.creditamt"/></th>
							<th><insta:ltext key="js.billing.creditNote.creditamt"/></th>
						</c:when>
						<c:otherwise>
								<th class="number" title="${sponseramt}"><insta:ltext key="billing.patientbill.details.sponsor"/></th>
								<th class="number" title="${patientamt}"><insta:ltext key="billing.patientbill.details.patient"/></th>
								<th class="number"><insta:ltext key="billing.patientbill.patient.creditamt"/></th>
								<th class="number"><insta:ltext key="billing.patientbill.sponsor.creditamt"/></th>
								<th><insta:ltext key="js.billing.creditNote.creditamt"/></th>
						</c:otherwise>
					</c:choose>
				</c:when>
				<c:otherwise>
						<th class="number"><insta:ltext key="billing.patientbill.patient.creditamt"/></th>
						<th><insta:ltext key="js.billing.creditNote.creditamt"/></th>
				</c:otherwise>
			</c:choose>
		</tr>
		<c:set var="rounded" value="N"/>
		<c:set var="totalClaimAmount" value="0"/>
		<c:set var="numCharges" value="${fn:length(billDetails.charges)}"/>
		<c:set var="patCharges" value="${fn:length(pat_charge_amt)}"/>
		<c:set var="priCharges" value="${fn:length(pri_charge_amt)}"/>
		<c:set var="secCharges" value="${fn:length(sec_charge_amt)}"/>
		<c:set var="priClaimRecvListLength" value="${fn:length(pri_claim_recieved_list)}"/>
		<c:set var="secClaimRecvListLength" value="${fn:length(sec_claim_recieved_list)}"/>

		<%-- we add one hidden row with a null charge for use as a template to clone from --%>
		<c:forEach begin="1" end="${numCharges+1}" var="i" varStatus="loop">
			<c:set var="charge" value="${billDetails.charges[i-1]}"/>
			<c:set var="pat_item_charges" value="0.00"/>
			<c:set var="pri_item_charges" value="0.00"/>
			<c:set var="sec_item_charges" value="0.00"/>
			<c:set var="pri_claim_recieved_amt" value="0.00"/>
			<c:set var="sec_claim_recieved_amt" value="0.00"/>
			<c:set var="style" value='style=""'/>
			<c:if test="${empty charge}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<c:if test="${charge.status=='X'}">
				<c:set var="style" value='style="display:none"'/>
			</c:if>
			<c:if test="${charge.chargeHead == 'PHMED' || charge.chargeHead == 'PHRET' || charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHCRET'}">
					<c:set var="style" value='style="display:none"'/>
			</c:if>
			<%-- <c:if test="${charge.chargeHead == 'MARPKG' || charge.chargeHead == 'MARDRG' || charge.chargeHead == 'MARPDM'}">
					<c:set var="style" value='style="display:none"'/>
			</c:if> --%>
			<tr ${style}>
				<td>
					<label>
						<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/> 
					</label>
					<input type="hidden" name="packageId" value="" />
					<input type="hidden" name="postedDate"
						value="<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>" />
					<input type="hidden" name="postedTime"
						value="<fmt:formatDate value="${charge.postedDate}" pattern="HH:mm"/>" />
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
					<input type="hidden" name="discount" value='${charge.discount}'>
					<input type="hidden" name="isClaimLocked" value='${charge.isClaimLocked}'>
					<input type="hidden" name="creditNoteTotalAmt" id= "creditNoteTotalAmt" />
					<input type ="hidden" name="isEdited" id="isEdited" value=""/>
					<input type ="hidden" name="claimStatus" id="claimStatus" value='${charge.claimStatus}'/>
					<input type ="hidden" name="sponsorTaxAmt" id="sponsorTaxAmt" value='${charge.sponsorTaxAmt}'/>
					<c:forEach begin="1" end="${patCharges+1}" var="j" varStatus="loop">
						<c:if test='${charge.chargeId eq pat_charge_amt[j-1].get("orig_charge_id")}'>
							<c:set var="pat_item_charges" value='${pat_charge_amt[j-1].get("tot_charge_amt") }'/>
						</c:if>
					</c:forEach>
					<input type ="hidden" name="patItemChargeAmt" id="patItemChargeAmt" value='${pat_item_charges}'/>
					
					<c:forEach begin="1" end="${priCharges+1}" var="j" varStatus="loop">
						<c:if test='${charge.chargeId eq pri_charge_amt[j-1].get("orig_charge_id")}'>
							<c:set var="pri_item_charges" value='${pri_charge_amt[j-1].get("tot_charge_amt") }'/>
						</c:if>
					</c:forEach>
					<input type ="hidden" name="priItemChargeAmt" id="priItemChargeAmt" value='${pri_item_charges}'/>
					
					<c:forEach begin="1" end="${secCharges+1}" var="j" varStatus="loop">
						<c:if test='${charge.chargeId eq sec_charge_amt[j-1].get("orig_charge_id")}'>
							<c:set var="sec_item_charges" value='${sec_charge_amt[j-1].get("tot_charge_amt") }'/>
						</c:if>
					</c:forEach>
					<input type ="hidden" name="secItemChargeAmt" id="secItemChargeAmt" value='${sec_item_charges}'/>
					
					
					<c:forEach begin="1" end="${priClaimRecvListLength+1}" var="j" varStatus="loop">
						<c:if test='${charge.chargeId eq pri_claim_recieved_list[j-1].get("charge_id")}'>
							<c:set var="pri_claim_recieved_amt" value='${pri_claim_recieved_list[j-1].get("claim_recieved") }'/>
						</c:if>
					</c:forEach>
					
					<c:forEach begin="1" end="${secClaimRecvListLength+1}" var="j" varStatus="loop">
						<c:if test='${charge.chargeId eq sec_claim_recieved_list[j-1].get("charge_id")}'>
							<c:set var="sec_claim_recieved_amt" value='${sec_claim_recieved_list[j-1].get("claim_recieved") }'/>
						</c:if>
					</c:forEach>
					<input type ="hidden" name="priClaimRecievedAmt" id="priClaimRecievedAmt" value='${pri_claim_recieved_amt}'/>
					<input type ="hidden" name="secClaimRecievedAmt" id="secClaimRecievedAmt" value='${sec_claim_recieved_amt}'/>
					
				</td>

				<td>
					<label>${charge.actRatePlanItemCode}</label>
					<input type="hidden" name="actRatePlanItemCode" value='${charge.actRatePlanItemCode}'>
					<input type="hidden" name="codeType" value='${charge.codeType}'>
				</td>

				<td>
					<div style="width:130px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${charge.actDescription}">
						${charge.actDescription}
					</div>
					<input type="hidden" name="description"
						value="${fn:escapeXml(charge.actDescription)}" >
					<input type="hidden" name="descriptionId"
						value="${charge.actDescriptionId}" >
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
					<label>${charge.amount + charge.taxAmt}</label>
					<input type="hidden" name="amt" value='${charge.amount + charge.taxAmt}' />
					<input type="hidden" name="amount_included" value="${charge.amount_included}"/>
					<input type="hidden" name="serviceChrgApplicable" value='${charge.serviceChrgApplicable}' />
					<c:if test="${!isInsuranceBill}">
						<input type="hidden" name="patientamt" value='${charge.amount}' />
					</c:if>
				</td>

				<c:if test="${isInsuranceBill}">
					<td class="number">
						<c:choose>
							<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
								<label>${ifn:afmt(0)}</label>
							</c:when>
							<c:otherwise>
								<label>${ifn:afmt(charge.claimAmounts[0])}</label>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="insClaimAmt" value='${charge.claimAmounts[0]}' />
						<input type="hidden" name="priInsClaimAmt" value='${charge.claimAmounts[0]}' />
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
								<c:otherwise>
									<label>${ifn:afmt(charge.claimAmounts[1])}</label>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="secInsClaimAmt" value='${charge.claimAmounts[1]}' />
							<input type="hidden" name="secIncludeInClaim" value='${charge.includeInClaimCalc[1]}' />
							<input type="hidden" name="insClaimable" value="${charge.insurancePayable}"/>
							<input type="hidden" name="insClaimTaxable" value="${charge.insuranceClaimTaxable}"/>
						</td>
					</c:if>

					<td class="number">
						<c:set var="patamt" value="${ifn:afmt(0)}"></c:set>
						<c:choose>
							<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
								<label>${ifn:afmt(0)}</label>
								<c:set var="patamt" value="${ifn:afmt(0)}"></c:set>
							</c:when>
							<c:when test="${charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHMED'}">
								<label>${(charge.amount + charge.returnAmt) - charge.claimAmounts[0] - charge.claimAmounts[1]}</label>
								<c:set var="patamt" value="${(charge.amount + charge.returnAmt) - charge.claimAmounts[0] - charge.claimAmounts[1]}"></c:set>
							</c:when>
							<c:otherwise>
								<label>${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}</label>
								<c:set var="patamt" value="${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}"></c:set>
							</c:otherwise>
						</c:choose>
						<input type="hidden" name="patientamt" value='${patamt}' />
						<input type="hidden" name="returnAmt" value='${charge.returnAmt}' />
						<input type="hidden" name="returnInsuranceClaimAmt" value='${charge.returnInsuranceClaimAmt}' />
						<input type="hidden" name="insDeductionAmt" value='${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}' />
					</td>
					
					<td class="number">
						<label>${pat_item_charges}</label>
					</td>
					<td class="number">
						<label>${pri_item_charges}</label>
					</td>
					<c:if test="${multiPlanExists}">
						<td class="number">
							<label>${sec_item_charges}</label>
						</td>
					</c:if>
					
					<input type="hidden" name="preAuthId" value='${charge.preAuthIds[0]}' >
					<input type="hidden" name="preAuthModeId" value='${charge.preAuthModeIds[0]}' >
					<c:if test="${multiPlanExists}">
						<input type="hidden" name="secPreAuthId" value='${charge.preAuthIds[1]}' >
						<input type="hidden" name="secPreAuthModeId" value='${charge.preAuthModeIds[1]}' >
					</c:if>
				<td class="number" style="width:80px">
				<c:if test="${patientSponsorCreditNotePath eq 'PatientCreditNote'}">
					<c:set var="amtdisable" value="${ifn:afmt(patamt) gt ifn:afmt(0) ? '':'readOnly'}"></c:set>
				</c:if>
				<c:if test="${patientSponsorCreditNotePath eq 'SponsorCreditNote'}">
					<c:set var="amtdisable" value="${ifn:afmt(charge.claimAmounts[0]) gt ifn:afmt(0) ? '':'readOnly'}"></c:set>
				</c:if>
				<c:if test="${amtdisable ne 'readOnly' && (billingcounterId == null || billingcounterId == '')}">
					<c:set var="amtdisable" value="readOnly"/>
				</c:if>
					<input type="text" name="pricreditNote" id="pricreditNote" style="width:80px" onkeypress="return enterNumOnly(event)" 
						value= "" onchange="setCreditamtValue(this);" ${amtdisable}>
				</td>
				</c:if>
				
				<c:if test="${!isInsuranceBill}">
				<c:set var="amtdisable" value="${ifn:afmt(charge.amount) gt ifn:afmt(0) ? '':'readOnly'}"></c:set>
				<c:if test="${amtdisable ne 'readOnly' && (billingcounterId == null || billingcounterId == '')}">
					<c:set var="amtdisable" value="readOnly"/>
				</c:if>
					<td class="number">
						<label>${pat_item_charges}</label>
					</td>
					<td class="number" style="width:80px">			
						<input type="text" name="pricreditNote" id="pricreditNote" style="width:80px" onkeypress="return enterNumOnly(event)" 
							value= "" onchange="setCreditamtValue(this);" ${amtdisable}>	
					</td>
				</c:if>
			</tr>
			
			<c:if test="${charge.status != 'X'}">
				<c:set var="totalNetAmount" value="${totalNetAmount + charge.amount}"/>
				<c:set var="totalDiscount" value="${totalDiscount + charge.discount}"/>
				<c:if test="${isInsuranceBill}">
					<c:set var="totalClaimAmount" value="${totalClaimAmount + charge.insuranceClaimAmount}"/>
				</c:if>
			</c:if>

			<c:if test="${charge.chargeHead == 'ROF'}">
				<c:set var="rounded" value="Y"/>
			</c:if>

		</c:forEach>
	</table>
	</div>
	<fieldset class="fieldSetBorder">
	  <legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.totals"/></legend>
		<table width="${width}" align="right" class="infotable">
	   		<tr style="display:none" id ="filterRow">
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.filterednetamt"/>:</td>
				<td class="forminfo">
					<label id="lblFilteredNetAmt">0.00</label>
				</td>
	
				<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">
	
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<c:if test="${isInsuranceBill}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.filtereddiscounts"/>:</td>
				<td class="forminfo">
					<label id="lblFilteredDisc">0.00</label>
				</td>
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.filteredamount"/>:</td>
				<td class="forminfo">
					<label id="lblFilteredAmount">0.00</label>
				</td>
		   	</tr>
	
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.billedamount"/>
						      <insta:ltext key="salesissues.sales.details.incl.tax.in.brackets"/>:</td>
				<td class="forminfo">
					<label id="lblTotBilled">0.00</label>
				</td>
	
				<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<c:if test="${isInsuranceBill}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.discounts"/>:</td>
				<td class="forminfo">
					<label id="lblTotDisc">0.00</label>
				</td>
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.netamount"/>:</td>
				<td class="forminfo">
					<label id="lblTotAmt">0.00</label>
				</td>
		 	</tr>
	
	 		<tr>
	 			<td class="formlabel"><insta:ltext key="js.billing.creditnote.creditpatientamount"/>:</td>
				<td class="forminfo">
					<label id="lblPatientCreditAmount">0.00</label>
				</td>
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.patientamount"/>:</td>
				<td class="forminfo">
					<label id="lblPatientAmount">0.00</label>
				</td>
				<c:if test="${isInsuranceBill}">
					<td class="formlabel"></td>
					<td class="forminfo"></td>
				</c:if>
	
				<td class="formlabel">
					<c:if test="${not empty bill}">
						<a href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=R&payment_type=F">
					</c:if>
						<insta:ltext key="billing.patientbill.details.patient"/>&nbsp;<insta:ltext key="billing.patientbill.details.payments"/>:
					<c:if test="${not empty bill}">
						</a>
					</c:if>
				</td>
				<td class="forminfo">
					<label id="lblExistingReceipts">0.00</label>
				</td>
	
				<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">
				<c:choose>
					<c:when test="${availableDeposits > 0 && hasRewardPointsEligibility}">
						<td class="formlabel" style="width:140px;"><insta:ltext key="billing.patientbill.details.deposit"/> + <insta:ltext key="billing.patientbill.details.pointsamt"/>:</td>
						<td class="forminfo" style="width:80px;">
							<label id="lblDepositsSetOff">0.00 </label>&nbsp;+&nbsp;<label id="lblRewardPointsAmt"> 0.00</label>
						</td>
					</c:when>
					<c:when test="${availableDeposits > 0}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.depositssetoff"/>:</td>
						<td class="forminfo">
							<label id="lblDepositsSetOff">0.00</label>
						</td>
					</c:when>
					<c:when test="${hasRewardPointsEligibility}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.pointsamt"/>:</td>
						<td class="forminfo">
							<label id="lblRewardPointsAmt">0.00</label>
						</td>
						</c:when>
					<c:otherwise>
					</c:otherwise>
				</c:choose>
				</c:if>
	
				<c:choose>
					<c:when test="${isInsuranceBill}">
						<c:set var="patDueAmt" value="${bill.totalAmount - bill.totalClaim - bill.totalReceipts}"/>
						<c:set var="spoDueAmt" value="${bill.totalClaim - bill.totalPrimarySponsorReceipts - bill.totalSecondarySponsorReceipts}"/>
	
					</c:when>
					<c:otherwise>
						<c:set var="patDueAmt" value="${bill.totalAmount - bill.totalReceipts}"/>
						<c:set var="spoDueAmt" value="0.00"/>
					</c:otherwise>
				</c:choose>
				<td class="formlabel">
					<c:choose>
						<c:when test="${bill.patientWriteOff == 'M' && (patDueAmt > 0.00 || patDueAmt < 0.00)}">
							<b><span style="color:red">*</span></b><insta:ltext key="billing.patientbill.details.patientdue"/>:
						</c:when>
						<c:otherwise>
							<insta:ltext key="billing.patientbill.details.patientdue"/>:
						</c:otherwise>
					</c:choose>
				</td>
				<td class="forminfo">
					<label id="lblPatientDue">0.00</label>
				</td>
			</tr>
	
			<tr>
				<td class="formlabel">&nbsp;</td>
			    <td class="forminfo">&nbsp;</td>
	
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				
				<c:if test="${isInsuranceBill}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<td class="formlabel">Net Patient Credit Note Amt:</td>
				<td class="forminfo">
					<label id='netpatientCreditNote' >${creditNoteDetails.total_amount - creditNoteDetails.total_claim}</label>
					<input type="hidden" name="netpatientCreditNoteAmt" id="netpatientCreditNoteAmt" value="${creditNoteDetails.total_amount - creditNoteDetails.total_claim}"/>
				</td>
				
				 <td class="formlabel">Net Patient Due:</td>
				<td class="forminfo" id="lblNetPatientDue">0.00</td>
				<td>
				<input type="hidden" name="depositSetOff" id="depositSetOff" style="width: 55px"
						${( bill.status == 'X' || bill.status == 'C' ) ? 'readOnly' : ''}
					    value='${depositDetails.map.deposit_set_off}' onchange="return onChangeDeposits(),resetTotals();" />
				<input type="hidden" name="rewardPointsRedeemedAmount" id="rewardPointsRedeemedAmount" style="width: 55px"
							readOnly value='${rewardPointDetails.map.points_redeemed_amt}' />
				</td>				
			</tr>			
							
			<c:if test="${isInsuranceBill || isOtherHospitalSponsorBill}">
				<tr>
				
					<td class="formlabel"><insta:ltext key="js.billing.creditnote.creditsponsoramount"/>:</td>
					<td class="forminfo">
						<label id="lblSponsorCreditAmount">0.00</label>
					</td>
				
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td class="forminfo">
						<label id="lblTotInsAmt">0.00</label>
					</td>
	
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.remittances"/>:</td>
					<td class="forminfo">
						<label id="lblSponsorRecdAmount">0.00</label>
					</td>
	
					<td class="formlabel">
					<a href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=S"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.payments"/>:</a>
					</td>
					<td class="forminfo">
						<label id="lblSponsorReceipts">0.00</label>
					</td>
	
					<c:if test="${availableDeposits > 0}">
						<td class="formlabel">&nbsp;</td>
						<td class="forminfo">&nbsp;</td>
					</c:if>
	
					<td class="formlabel">
						<c:choose>
							<c:when test="${bill.sponsorWriteOff == 'M' && spoDueAmt > 0.00}">
								<b><span style="color:red">*</span></b><insta:ltext key="billing.patientbill.details.sponsordue"/>:
							</c:when>
							<c:otherwise>
								<insta:ltext key="billing.patientbill.details.sponsordue"/>:
							</c:otherwise>
						</c:choose>
	
					</td>
					<td class="forminfo">
						<label id="lblSponsorDue">0.00</label>
					</td>
				</tr>
				
				<tr>
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
	
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
	
					<td class="formlabel">Net Sponsor Credit Note Amount:</td>
					<td class="forminfo">
						<c:choose>
							<c:when test="${creditNoteDetails.total_claim == '' || creditNoteDetails.total_claim == null }">
								<label id='netsponsorCreditNote' >0.00</label>
								<input type="hidden" name="netsponsorCreditNoteAmt" id="netsponsorCreditNoteAmt" value="0.00"/>
							</c:when>
							<c:otherwise>
								<label id='netsponsorCreditNote' >${creditNoteDetails.total_claim}</label>
								<input type="hidden" name="netsponsorCreditNoteAmt" id="netsponsorCreditNoteAmt" value="${creditNoteDetails.total_claim}"/>
							</c:otherwise>
						</c:choose>
						
					</td>
	
					<td class="formlabel">Net Sponsor Due:</td>
					<td class="forminfo" id="lblNetSponsorDue">0.00</td>
				</tr>
			</c:if>
			<c:if test="${(bill.patientWriteOff == 'A' &&  (patDueAmt > 0.00 || patDueAmt < 0.00)) || (bill.sponsorWriteOff == 'A' && (spoDueAmt > 0.00 || spoDueAmt < 0.00))}" >
				<tr>
					<c:if test="${bill.patientWriteOff == 'A' &&  (patDueAmt > 0.00 || patDueAmt < 0.00)}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.writeOffamt"/>:</td>
						<td class="forminfo">
							<label id="lblWrittenOffAmt">0.00</label>
						</td>
					</c:if>
					<c:if test="${bill.sponsorWriteOff == 'A' && (spoDueAmt > 0.00 || spoDueAmt < 0.00)}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.spnrWriteOffAmt"/>:</td>
						<td class="forminfo">
							<label id="lblSpnrWrittenOffAmt">0.00</label>
						</td>
					</c:if>
				</tr>
			</c:if>
		</table>
	</fieldset>	
	
	<!-- Generating the receipt-->
	<c:choose>
		<c:when test="${patientSponsorCreditNotePath == 'PatientCreditNote' && billingcounterId != null && billingcounterId != ''}">
			<c:set var="paymentDisplay">block</c:set>
		</c:when>
		<c:otherwise>
			<c:set var="paymentDisplay">none</c:set>
		</c:otherwise>
	</c:choose>
	<dl class="accordion" style="margin-bottom: 10px;display:${paymentDisplay}" id="payments">
		<dt>
			<span><insta:ltext key="billing.patientbill.details.payments"/></span>
		</dt>
		<dd id="payDD" class="open">
			<div class="bd">
				<c:set var="paymentSelValue" value="F"/>
				<insta:billPaymentDetails formName="mainform" isBillNowPayment="true" defaultPaymentType="${paymentSelValue}" />
			</div>
		</dd>
	</dl>

	<c:if test="${not empty bill}">
		<table>
			<tr>
				<td align="right">
					<c:if test="${patientSponsorCreditNotePath eq 'PatientCreditNote'}">
					<button type="button" id="saveButton"  onclick="return doSave();"><insta:ltext key="js.billing.creditNote.payandclosebutton"/></button>
					</c:if>
					<c:if test="${patientSponsorCreditNotePath eq 'SponsorCreditNote'}">
					<button type="button" id="saveButton"  onclick="return doSave();"><insta:ltext key="js.billing.creditNote.saveandclosebutton"/></button>
					</c:if>
					&nbsp;
					<button type="button" id="cancelButton"  onclick="return doCancel();"><insta:ltext key="js.billing.creditNote.cancelbutton"/></button>
				</td>
			</tr>
			<c:if test="${bill.billType != 'M' && bill.billType != 'R' && (billPrintRights == 'A' || roleId == 1 || roleId == 2)}">					
					<c:if test="${bill.billType == 'C' || isInsuranceBill}">
						<input type="hidden" name="printBill" id="printSelect" value="${genPrefs.billLaterPrintDefault}"/>
						<input type="hidden" name="printType" id="printType" value="${genPrefs.default_printer_for_bill_later}"/>
					</c:if>
					<c:if test="${bill.billType == 'P' && !isInsuranceBill}">
						<input type="hidden" name="printBill" id="printSelect" value="${genPrefs.billNowPrintDefault}"/>
						<input type="hidden" name="printType" id="printType" value="${genPrefs.default_printer_for_bill_now}"/>
					</c:if>
			</c:if>	
		</table>
	</c:if>
</form>

<script>
	var multiPlanExists = ${not empty multiPlanExists ? multiPlanExists : false};
	var planBean = <%= request.getAttribute("planBeanJSON") %> ;
	var priSponsorType = '${patient.sponsor_type}';
	var secSponsorType = '${patient.sec_sponsor_type}';
	var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
	var existingSponsorReceipts = ${empty existingSponsorReceipts ? 0 : existingSponsorReceipts};
	var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
	var totalBilledAmount = ${empty totalAmount ? 0 : totalAmount};
	var sponsorBillNo = '${ifn:cleanJavaScript(bill.sponsorBillNo)}';
	var visitType = '${ifn:cleanJavaScript(bill.visitType)}';
	var billingcounterId = '${billingcounterId}';
	var points_redemption_rate = ${empty points_redemption_rate ? 0 : points_redemption_rate};
	var specificPaymentModeList = '${specificPaymentModeList}';
	var templateList = <%= request.getAttribute("templateListJSON") %>;
	var patientDueAmt =${patDueAmt};
	var sponsorDueAmt =${spoDueAmt};
</script>
</body>
</html>

