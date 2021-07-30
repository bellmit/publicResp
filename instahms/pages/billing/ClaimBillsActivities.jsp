<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="billstatusDisplay" class="java.util.HashMap"/>
<c:set target="${billstatusDisplay}" property="A" value="Open"/>
<c:set target="${billstatusDisplay}" property="F" value="Finalized"/>
<c:set target="${billstatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="chargeClaimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${chargeClaimStatusDisplay}" property="empty" value="Open"/>
<c:set target="${chargeClaimStatusDisplay}" property="red" value="Denied"/>
<c:set target="${chargeClaimStatusDisplay}" property="green" value="Denial Accepted"/>
<c:set target="${chargeClaimStatusDisplay}" property="grey" value="Closed"/>

<jsp:useBean id="denialCodeStatusDisplay" class="java.util.HashMap"/>
<c:set target="${denialCodeStatusDisplay}" property="A" value="Active"/>
<c:set target="${denialCodeStatusDisplay}" property="I" value="Retired"/>

<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>
<head>
	<title>Claim ${claim.map.claim_id} Activities</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<script>
		var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
		var updateMRDRights  = '${urlRightsMap.update_mrd}';
		var visitEMRRights   = '${urlRightsMap.visit_emr_screen}';
		var claimStatus		= '${claim.map.status}';
		var claimBatchStatus = '${claim.map.claim_batch_status}';
		var actualClaimStatus = '${claim.map.claim_status}';
		var usesDRG 			= '${claim.map.use_drg}';
		var usesPerdiem		= '${claim.map.use_perdiem}';
		var claimId         = '${claim.map.claim_id}';
		var allowDenialAccepted = '${((actionRightsMap.allow_denial_acceptance == 'A')||(roleId==1)||(roleId==2))}';
		var isInterCompAllowed = <%= request.getAttribute("isInterCompAllowed") %>;
		var hasExcessAmtNotDenialAcceptExist = <%= request.getAttribute("hasExcessAmtNotDenialAcceptExist") %>;
	</script>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js" />
	<insta:link type="script" file="billing/claimreconciliation.js"/>
	<insta:link type="script" file="billing/claimbillsactivities.js"/>
	<script>
	/* These are declared in claimreconciliation.js */
	maxResubCount = (('${claim.map.max_resubmission_count}' == '') ? '' : '${claim.map.max_resubmission_count}'-1);
	resubmissionCount = '${claim.map.resubmission_count}';
	</script>
</head>

<body onload="initForm();" class="yui-skin-sam">

<form name="claimForm" action="claimReconciliation.do">
	<input type="hidden" name="_method" value="getClaimBillsActivities">
	<table width="100%">
		<tr>
			<td width="100%"><h1>Claim</h1></td>
			<td>Claim&nbsp;No:&nbsp;</td>
			<td><input type="text" name="claim_id" id="claim_id" style="width: 80px"></td>
			<td><input type="submit" class="button" value="Find"></td>
		</tr>
	</table>
</form>

<div><insta:feedback-panel/></div>

<c:set var="patient_share" value="${claim.map.use_drg == 'Y' ? claim.map.deduction : claim.map.patient_share}"/>
<c:set var="patient_net" value="${claim.map.use_drg == 'Y' ? (claim.map.net - patient_share) : claim.map.net}"/>

<div id="insurancePhotoDialog" style="display:none;visibility:hidden;" ondblclick="handleInsurancePhotoDialogCancel();">
	<div class="bd" id="bd2" style="padding-top: 0px;">
		<table  style="text-align:top;vetical-align:top;" width="100%">
			<tr>
				<td>
					<fieldset class="fieldSetBorder" style="text-align:center;margin-right:4px;">
						<legend class="fieldSetLabel">Insurance Card</legend>
								<c:choose>
									<c:when test="${isInsuranceCardAvailable eq true }">
										<embed id="insuranceImage" height="450px" width="500px" style="overflow:auto"
											src="${cpath}/Registration/GeneralRegistrationPlanCard.do?_method=viewInsuranceCardImage&visitId=${claim.map.patient_id}"/>
									</c:when>
									<c:otherwise>
										No Insurance Card Available
									</c:otherwise>
								</c:choose>
					 </fieldset>
				</td>
			</tr>
			<tr>
				<td align="left">
					<input type="button" value="Close" style="cursor:pointer;" onclick="handleInsurancePhotoDialogCancel();"/>
				</td>
			</tr>
		</table>
	</div>
</div>
<insta:patientdetails visitid="${claim.map.patient_id}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Other Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Emirates Id:</td>
			<td class="forminfo">${claim.map.emirates_id_number}</td>
			<td class="formlabel">Provider Id:</td>
			<td class="forminfo">${service_reg_no}</td>
			<c:choose>
				<c:when test="${isInsuranceCardAvailable eq true}">
					<td class="formlabel">View Insurance Card:</td>
					<td class="forminfo">
						<button id="_plan_card" title="Uploaded Insurance Card..." style="cursor:pointer;" onclick="javascript:showInsurancePhotoDialog();" type="button"> .. </button>
					</td>
				</c:when>
				<c:otherwise>
					<td></td>
					<td></td>
				</c:otherwise>
			</c:choose>
		</tr>
	</table>
</fieldset>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Submission/Resubmission Batch Details</legend>
	<table class="formtable"  width="100%">
		<tr>
			<c:forEach items="${submissionBatchList}" var="subId">
					<tr>
						<td class="formlabel">Submission Id:</td>
						<td class="forminfo">${subId.map.submission_batch_id}</td>
						<td class="formlabel">Creation Date:</td>
						<td class="forminfo">${subId.map.created_date}</td>
					</tr>
				</c:forEach>
		</tr>
	</table>
</fieldset>

<form name="mainform" method="post" action="claimReconciliation.do">
<input type="hidden" name="_method" value="">
<input type="hidden" name="claim_id" id="claim_id" value="${claim.map.claim_id}">
<input type="hidden" name="_submission_batch_id" value="${claim.map.last_submission_batch_id}">
<input type="hidden" name="screen" value="activity">

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Claim Details</legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
		<tr>
			<td class="formlabel">Claim Id:</td>
			<td class="forminfo">${claim.map.claim_id}</td>
			<td class="formlabel" style="width: 13%;">Latest Submission Id:</td>

			<td class="forminfo">
				<c:if test="${claim.map.status ne 'M'}">
					${claim.map.last_submission_batch_id}
				</c:if>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Gross:</td>
			<td class="forminfo">${claim.map.gross}</td>
			<td class="formlabel">Patient Share:</td>
			<td class="forminfo">${patient_share}</td>
			<td class="formlabel">Net:</td>
			<td class="forminfo">${patient_net}</td>
		</tr>
		<tr>
			<td class="formlabel">Claim Status:</td>
			<td class="forminfo">${claim.map.claim_status}</td>
			<td class="formlabel">Closure Type:</td>
			<td class="forminfo">
				<c:choose>
					<c:when test="${(claim.map.claim_batch_status ne 'D' 
									&& (claim.map.status eq 'B' || claim.map.status eq 'O')) 
									|| !((actionRightsMap.allow_denial_acceptance == 'A')||(roleId==1)||(roleId==2))
									|| claim.map.status eq 'C'}">
						<insta:selectoptions name="closure_type" id="closure_type"
						optexts="-- Select --,Fully Received,Denial Accepted,Write Off/Credit Note"
						opvalues=" ,F,D,W" value="${claim.map.closure_type}" disabled="true"/>
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="closure_type" id="closure_type"
						optexts="-- Select --,Fully Received,Denial Accepted,Write Off/Credit Note"
						opvalues=" ,F,D,W" value="${claim.map.closure_type}" onchange="disableOrEnableClaimRejReason();"/>
					</c:otherwise>
				</c:choose>

				<input type="hidden" name="old_closure_type" id="old_closure_type"
					value="${claim.map.closure_type}" />
			</td>
			<td class="formlabel">Rejection Reason:</td>
			<td class="forminfo">
				<insta:selectdb name="claim_rejection_reasons_drpdn" id="claim_rejection_reasons_drpdn"
								onchange="" value="${claim.map.rejection_reason_category_id}"
								table="rejection_reason_categories" style="width:137px;"
								dummyvalue="--Select--" valuecol="rejection_reason_category_id"
								displaycol="rejection_reason_category_name"
								orderby="rejection_reason_category_name" disabled="true"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Remarks:</td>
			<td class="forminfo">
				<input type="text" name="action_remarks" id="action_remarks"
					value="${claim.map.action_remarks}" maxlength="200"
					${(not empty claim.map.status && claim.map.claim_batch_status ne 'D' && (claim.map.status eq 'B' || claim.map.status eq 'O')) || claim.map.status eq 'C' ? 'disabled' :''}/>
				<input type="hidden" name="old_action_remarks" id="old_action_remarks"
					value="${claim.map.action_remarks}" />
			</td>
			<td class="formlabel">Denial Remarks:</td>
			<td class="forminfo">
				<input type="text" name="denial_remarks" id="denial_remarks"
					value="${claim.map.denial_remarks}" maxlength="200"
					${(not empty claim.map.status && claim.map.claim_batch_status ne 'S') ? 'disabled' :''}/>
				<input type="hidden" name="old_denial_remarks" id="old_denial_remarks"
					value="${claim.map.denial_remarks}" />
			</td>
		</tr>
	</table>
</fieldset>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="ui.label.diagnosis.details.entered.by.doctor"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
			<c:choose>
				<c:when test="${not empty diagnosisList}">
					<c:forEach items="${diagnosisList}" var="diag">
						<tr>
							<td class="formlabel">Diagnosis Type:</td>
							<td class="forminfo">${diag.map.diag_type} (${diag.map.code_type})</td>
							<td class="formlabel">Diagnosis Code:</td>
							<td class="forminfo">${diag.map.icd_code}</td>
							<td class="formlabel">Diagnosis Code:</td>
							<td class="forminfo">
								<div title="${diag.map.code_desc}">
								<insta:truncLabel value="${diag.map.code_desc}" length="60"/>
								</div>
							</td>
						</tr>
					</c:forEach>
				</c:when>
				<c:otherwise>
						<tr>
							<td></td>
							<td colspan="2" class="forminfo">No diagnosis codes available.</td>
							<td></td>
							<td></td>
							<td></td>
						</tr>
				</c:otherwise>
			</c:choose>
	
		</table>
	</fieldset>

<!-- account group == 1 ensures this section is only shown for hospital claims and not pharmacy claims. -->
<c:if test="${claim.map.account_group == 1}">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="ui.label.code.diagnosis.details"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
			<c:choose>
				<c:when test="${not empty coderDiagnosisList}">
					<c:forEach items="${coderDiagnosisList}" var="diag">
						<tr>
							<td class="formlabel">Diagnosis Type:</td>
							<td class="forminfo">${diag.map.diag_type} (${diag.map.code_type})</td>
							<td class="formlabel">Diagnosis Code:</td>
							<td class="forminfo">${diag.map.icd_code}</td>
							<td class="formlabel">Diagnosis Code:</td>
							<td class="forminfo">
								<div title="${diag.map.code_desc}">
								<insta:truncLabel value="${diag.map.code_desc}" length="60"/>
								</div>
							</td>
						</tr>
					</c:forEach>
				</c:when>
				<c:otherwise>
						<tr>
							<td></td>
							<td colspan="2" class="forminfo">No Coder edited diagnosis codes available.</td>
							<td></td>
							<td></td>
							<td></td>
						</tr>
				</c:otherwise>
			</c:choose>
	
		</table>
	</fieldset>
</c:if>
<c:set var="totalAmount" value="0"/>
<c:set var="totalDiscount" value="0"/>
<c:set var="totalGrossAmount" value="0"/>
<c:set var="totalPatientAmount" value="0"/>
<c:set var="totalNetAmount" value="0"/>
<c:set var="totalRecdAmount" value="0"/>

<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="billsTable" border="0" width="100%">
		<tr>
			<th style="width: 30px">Bill No</th>
			<th style="width: 30px">Charge Id</th>
			<th style="width: 40px">Date</th>
			<th style="width: 30px">Group</th>
			<th style="width: 30px">Head</th>
			<th style="width: 30px">Activity</th>
			<th style="width: 30px">Clinician</th>
			<th style="width: 10px" class="number">Rate</th>
			<th style="width: 10px" class="number">Qty</th>
			<th style="width: 10px" class="number">Disc</th>
			<th style="width: 10px" class="number">Gross</th>
			<th style="width: 10px" class="number">Pat / Oth.Spnsr</th>
			<th style="width: 10px" class="number">Net</th>
			<th style="width: 10px"></th>
			<th style="width: 10px" class="number">Recd.</th>
			<th style="width: 10px">Status</th>
			<th style="width: 15px" title="Denial Code">Denial Code</th>
			<th style="width: 15px">Denial Code Type</th>
			<th style="width: 50px">Description</th>
			<th style="width: 50px">Example</th>
			<th style="width: 15px">Code Status</th>
			<th style="width: 15px">Code Type</th>
			<th style="width: 15px">Code</th>
			<th style="width: 10px"></th>
		</tr>
		<c:set var="rowindex" value="0"/>
		<c:forEach items="${billsList}" var="b">
			<c:set var="bill" value="${b.bill}"/>
			<c:set var="billTypeDisplay">
			<c:choose>
				<c:when test="${bill.map.bill_type == 'P' && bill.map.restriction_type == 'P'}">Bill Now (Pharmacy)</c:when>
				<c:when test="${bill.map.bill_type == 'C' && bill.map.restriction_type == 'P'}">Bill Later (Pharmacy)</c:when>
				<c:when test="${bill.map.bill_type == 'P'}">Bill Now</c:when>
				<c:when test="${bill.map.bill_type == 'C'}">Bill Later</c:when>
				<c:otherwise>Other</c:otherwise>
			</c:choose>
		</c:set>
			<c:set var="charges" value="${b.charges}"/>
			<tr>
				<c:set var="rowindex" value="${rowindex+1}"/>
				<td style="font-weight: bold;" colspan="24">
					<a href="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen&billNo=${bill.map.bill_no}">
					${bill.map.bill_no} (${billstatusDisplay[bill.map.status]}/${billTypeDisplay})
					</a>
				</td>
			</tr>
			<c:forEach items="${charges}" var="charge" varStatus="st">
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${charge.map.claim_status == 'C'}">grey</c:when>
						<c:when test="${charge.map.claim_status == 'D' && (charge.map.closure_type == 'D' || charge.map.closure_type == 'M')}">green</c:when>
						<c:when test="${charge.map.claim_status == 'D'}">red</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<c:if test="${charge.map.net >= 0 && charge.map.codification_supported == 'Y'}">
					<tr>
						<c:set var="rowindex" value="${rowindex+1}"/>
						<td></td>
						<td>${charge.map.activity_charge_id}</td>
						<td><fmt:formatDate value="${charge.map.item_posted_date}" pattern="dd-MM-yyyy HH:mm"/></td>
						<td><insta:truncLabel value="${charge.map.chargegroup_name}" length="10"/></td>
						<td><insta:truncLabel value="${charge.map.chargehead_name}" length="10"/></td>
						<td><insta:truncLabel value="${charge.map.act_description}" length="15"/></td>
						<td><insta:truncLabel value="${charge.map.doctor_name}" length="10"/></td>
						<td class="number">${charge.map.rate}</td>
						<td class="number">${ifn:afmt(charge.map.quantity)}</td>
						<td class="number">${charge.map.discount}</td>
						<td class="number">${charge.map.amount}</td>
						<td class="number">${charge.map.amount - charge.map.net}</td>
						<td class="number">
							<c:set var="charge_net" value="${charge.map.charge_head == 'MARDRG' ? charge.map.net - patient_share : charge.map.net}"/>
							${charge_net}
						</td>
						<td>
							<a href="javascript:Edit" onclick="return showEditItemDialog(this);" title="Edit Claim Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</td>
						<td class="number">
							${charge.map.claim_recd_total}

							<input type="hidden" name="bill_no" id="bill_no${rowindex}" value="${bill.map.bill_no}" />
							<input type="hidden" name="charge_id" id="charge_id${rowindex}" value="${charge.map.charge_id}" />
							<input type="hidden" name="charge_group_name" id="charge_group_name${rowindex}" value="${charge.map.chargegroup_name}" />
							<input type="hidden" name="charge_head_name" id="charge_head_name${rowindex}" value="${charge.map.chargehead_name}" />
							<input type="hidden" name="activity_name" id="activity_name${rowindex}" value="${charge.map.act_description}" />
							<input type="hidden" name="charge_head" id="charge_head${rowindex}" value="${charge.map.charge_head}" />
							<input type="hidden" name="sale_item_id" id="sale_item_id${rowindex}" value="${charge.map.sale_item_id}" />
							<input type="hidden" name="activity_charge_id" id="activity_charge_id${rowindex}" value="${charge.map.activity_charge_id}"/>
							<input type="hidden" name="bill_claim_status" id="bill_claim_status${rowindex}" value="${claim.map.status}" />
							<input type="hidden" name="claim_activity_id" id="claim_activity_id${rowindex}" value="${charge.map.claim_activity_id}"/>
							<input type="hidden" name="${charge.map.claim_activity_id}" id="act_${rowindex}" value="${rowindex}"/>
							<input type="hidden" name="item_denial_accepted" id="item_denial_accepted${rowindex}" value="${charge.map.closure_type}" />
							<input type="hidden" name="orig_item_denial_accepted" id="orig_item_denial_accepted${rowindex}" value="${charge.map.closure_type}" />
							<input type="hidden" name="item_rej_reason" id="item_rej_reason${rowindex}" value="${charge.map.rejection_reason_category_id}" />
							<input type="hidden" name="orig_item_rej_reason" id="orig_item_rej_reason${rowindex}" value="${charge.map.rejection_reason_category_id}" />
							<input type="hidden" name="bill_status" id="bill_status${rowindex}" value="${bill.map.status}" />
							<input type="hidden" name="activity_claim_status" id="activity_claim_status${rowindex}" value="${charge.map.claim_status}" />
							<input type="hidden" name="item_qty" id="item_qty${rowindex}" value="${charge.map.quantity}"/>
							<input type="hidden" name="item_package_unit" id="item_package_unit${rowindex}" value="${charge.map.package_unit}"/>
							<input type="hidden" name="orig_item_rate" id="orig_item_rate${rowindex}" value="${charge.map.rate}" />
							<input type="hidden" name="item_rate" id="item_rate${rowindex}" value="${charge.map.rate}" />
							<input type="hidden" name="item_disc" id="item_disc${rowindex}" value="${charge.map.discount}" />
							<input type="hidden" name="item_amount" id="item_amount${rowindex}" value="${charge.map.amount}"/>
							<input type="hidden" name="orig_item_amount" id="orig_item_amount${rowindex}" value="${charge.map.amount}"/>
							<input type="hidden" name="item_return_amount" id="item_return_amount${rowindex}" value="${charge.map.return_amt}" />
							<input type="hidden" name="patient_amount" id="patient_amount${rowindex}" value="${charge.map.amount - charge.map.net}"/>
							<input type="hidden" name="claim_net_amount" id="claim_net_amount${rowindex}" value="${charge_net}"/>
							<input type="hidden" name="orig_claim_net_amount" id="orig_claim_net_amount${rowindex}" value="${charge_net}"/>
							<input type="hidden" name="claim_recd_amount" id="claim_recd_amount${rowindex}" value="${charge.map.claim_recd_total}"/>
							<input type="hidden" name="edited" id="edited${rowindex}" value="false" />
							<input type="hidden" name="claimAmtEdited" id="claimAmtEdited${rowindex}" value="false" />
							<input type="hidden" name="rowIndexValue" id="rowIndexValue${rowindex}" value="${rowindex}" />

						</td>
						<td>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							${chargeClaimStatusDisplay[flagColor]}
						</td>
						<td>${charge.map.denial_code}</td>
						<td>${charge.map.denial_code_type}</td>
						<td><insta:truncLabel value="${empty charge.map.denial_desc ? charge.map.denial_remarks : charge.map.denial_desc}" length="15"/></td>
						<td><insta:truncLabel value="${charge.map.example}" length="15"/></td>
						<td>${denialCodeStatusDisplay[charge.map.denial_code_status]}</td>
						<td>${charge.map.code_type}</td>
						<td>${charge.map.item_code}</td>

						<c:set var="totalAmount" value="${totalAmount + charge.map.amount + charge.map.discount}"/>
						<c:set var="totalDiscount" value="${totalDiscount + charge.map.discount}"/>
						<c:set var="totalGrossAmount" value="${totalGrossAmount + charge.map.amount}"/>
						<c:set var="totalPatientAmount" value="${totalPatientAmount + (charge.map.amount - charge_net)}"/>
						<c:set var="totalNetAmount" value="${totalNetAmount + charge_net}"/>
						<c:set var="totalRecdAmount" value="${totalRecdAmount + charge.map.claim_recd_total}"/>
						
						<td>
							<a href="javascript:void(0)" onclick="return showItemHistory(this);" title="View Item History">
								History
							</a>
						</td>
					</tr>
				</c:if>
			</c:forEach>
		</c:forEach>
	</table>
</div>

<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel">Claim Totals</legend>
	<table width="840" align="left" class="infotable">
   		<tr>

   		<td class="formlabel">Total Amt:</td>
			<td class="forminfo" align="right">
				<label id="lblBillsAmount">${totalAmount + drgAdjAmt + notConsumedAmt + notConsumedDiscount}</label> 
			</td>

			<td class="formlabel">Total Discount:</td>
			<td class="forminfo" align="right">
				<label id="lblBillsDiscount">${totalDiscount + notConsumedDiscount}</label>
			</td>

			<td class="formlabel">Total Gross Amt:</td>
			<td class="forminfo" align="right">
				<label id="lblBillsGrossAmount">${totalGrossAmount + drgAdjAmt + notConsumedAmt}</label>
			</td>

			<td class="formlabel">Total Pat / Oth Spnsr Amt:</td>
			<td class="forminfo" align="right">
				<c:if test="${creditNoteDetails != null && creditNoteDetails.bill_no != null}">
					<label id="lblBillsPatientAmt">${totalPatientAmount + creditNoteDetails.total_amount - creditNoteDetails.total_claim + drgAdjAmt + notConsumedAmt}</label>
				</c:if>
				<c:if test="${creditNoteDetails == null}">
					<label id="lblBillsPatientAmt">${totalPatientAmount + drgAdjAmt + notConsumedAmt}</label>
				</c:if>
			</td>

			<td class="formlabel">Total Claim Net Amt:</td>
			<td class="forminfo" align="right">
				<label id="lblClaimNetAmt">${totalNetAmount}</label>
			</td>

			<td></td>
			<td></td>
	   	</tr>

		<tr>
			<td class="formlabel">Total Tax:</td>
			<td class="forminfo" align="right">${claim.map.total_tax + notConsumedTaxAmt}</td>
			<td class="formlabel">Total Claim Tax:</td>
			<td class="forminfo" align="right">${claim.map.total_claim_tax}</td>
			<td class="formlabel">Total Pat/Other Tax:</td>
			<td class="forminfo" align="right">${claim.map.total_tax + notConsumedTaxAmt - claim.map.total_claim_tax}</td>
			<c:if test="${creditNoteDetails != null && creditNoteDetails.bill_no != null}">
			<td class="formlabel"><insta:ltext key="billing.patientbill.details.patientCreditNoteAmt"/>:</td>
				<td class="forminfo">
					 ${creditNoteDetails.total_amount - creditNoteDetails.total_claim} 
					 <input type="hidden" name="patientCreditNoteAmt" id="patientCreditNoteAmt" value="${creditNoteDetails.total_amount - creditNoteDetails.total_claim}"/> 
			</td>
			</c:if>
			<c:if test="${creditNoteDetails == null}">
			<td></td><td></td>
			</c:if>
			
			<td class="formlabel">Total Claim Recd. Amt:</td>
			<td class="forminfo" align="right">
				<label id="lblBillsRecdAmount">${totalRecdAmount}</label>
			</td>
			<td></td>
			<td></td>
	   	</tr>
	 </table>
</fieldset>


<fieldset class="fieldSetBorder">
<table class="formtable" align="center">
	<tr>
		<c:choose>
			<c:when test="${not empty claim.map.claim_status && claim.map.claim_status eq 'Denied'}">
				<td class="formlabel">
					<input type="checkbox" name="resubActionChk" id="resubActionChk" onclick="enableResubmissionFields();"
						value="markForResubmission" accesskey="M" disabled="false">
				</td>
				<td class="forminfo">Mark for Resubmission</td>
			</c:when>
			<c:when test="${not empty claim.map.status && claim.map.status eq 'M' }">
				<td class="formlabel">
					<input type="checkbox" name="resubActionChk" id="resubActionChk" onclick="enableResubmissionFields();"
						value="unmarkForResubmission" accesskey="M" disabled="false">
				</td>
				<td class="forminfo">Unmark for Resubmission</td>
			</c:when>
		</c:choose>
		<td class="formlabel">Resubmission type:</td>
		<td class="forminfo">
			<c:choose>
				<c:when test="${not empty claim.map.status && claim.map.status ne 'M'}">
					<c:choose>
                    	<c:when test="${healthAuthority == 'DHA'}">
                    		<insta:selectoptions name="_resubmission_type" id="_resubmission_type"
                    			value="${claim.map.resubmission_type}" dummyvalue="-- Select --"
                    			optexts="correction,internal complaint,legacy,reconciliation"
                    			opvalues="correction,internal complaint,legacy,reconciliation" disabled="true"/>
                    	</c:when>
                    	<c:otherwise>
                    		<insta:selectoptions name="_resubmission_type" id="_resubmission_type"
                    			value="${claim.map.resubmission_type}" dummyvalue="-- Select --"
                    			optexts="correction,internal complaint,legacy"
                    			opvalues="correction,internal complaint,legacy" disabled="true"/>
                    	</c:otherwise>
                    </c:choose>
				</c:when>
				<c:otherwise>
					<c:choose>
                    	<c:when test="${healthAuthority == 'DHA'}">
                    		<insta:selectoptions name="_resubmission_type" id="_resubmission_type"
                    		    value="${claim.map.resubmission_type}" dummyvalue="-- Select --"
                    			optexts="correction,internal complaint,legacy,reconciliation"
                    			opvalues="correction,internal complaint,legacy,reconciliation"/>
                    	</c:when>
                    	<c:otherwise>
                    	    <insta:selectoptions name="_resubmission_type" id="_resubmission_type"
                    			value="${claim.map.resubmission_type}" dummyvalue="-- Select --"
                    			optexts="correction,internal complaint,legacy"
                    			opvalues="correction,internal complaint,legacy"/>
                    	</c:otherwise>
                    </c:choose>
				</c:otherwise>
			</c:choose>
			<input type="hidden" name="_old_resubmission_type" id="_old_resubmission_type"
					value="${claim.map.resubmission_type}" />
		</td>
		<td class="formlabel">Comments:</td>
		<td class="forminfo">
			<textarea name="_comments" id= "_comments" title="Comments for resubmission" rows="2" cols="60"
				${(not empty claim.map.status && claim.map.status ne 'M') ? 'disabled' :''}
				>${claim.map.comments}</textarea>
			<textarea style="display:none" name="_old_comments"
				id="_old_comments">${claim.map.comments}</textarea>
		</td>
	</tr>
</table>
</fieldset>

<table class="screenActions">
	<tr>
		<td>
			<button type="button" name="actionBtn" value=""
				accessKey="S" onclick="return doSave();"><b><u>S</u></b>ave</button>
			<label>|</label>
			<c:choose>
				<c:when test="${claim.map.claim_status ne 'Closed' }">
					<c:choose>
						<c:when test="${not empty claim.map.last_submission_batch_id 
							&& claim.map.status eq 'B' && claim.map.claim_batch_status eq 'O'
							&& claim.map.resubmission_count gt 0}">
								<input type="checkbox" name="actionChk" id="actionChk"
									value="removeFromResubmission" accesskey="R">
								<label><b><u>R</u></b>emove from Resubmission</label> &nbsp;
								<label>|</label>
						</c:when>
						<c:when test="${not empty claim.map.last_submission_batch_id 
							&& (claim.map.resubmission_count == 0 || empty claim.map.resubmission_count)
							&& empty claim.map.resubmission_type && empty claim.map.comments
							&& claim.map.status eq 'B' && claim.map.claim_batch_status eq 'O'}">
								<input type="checkbox" name="actionChk" id="actionChk"
									value="removeFromSubmission" accesskey="R">
								<label><b><u>R</u></b>emove from Submission</label> &nbsp;
								<label>|</label>
						</c:when>					
						<c:when test="${(empty claim.map.last_submission_batch_id || claim.map.last_submission_batch_id == '')  
							&& (claim.map.resubmission_count == 0 || empty claim.map.resubmission_count) 
							&& empty claim.map.resubmission_type && empty claim.map.comments 
							&& claim.map.status eq 'O' && empty claim.map.claim_batch_status}">
								<input type="checkbox" name="actionChk" id="actionChk"
									value="addToSubmission" accesskey="A">
								<label><b><u>A</u></b>dd to Submission</label> &nbsp;
								<label>|</label>
						</c:when>
						<c:when test="${claim.map.status eq 'M'}">
									<input type="checkbox" name="actionChk" id="actionChk"
										value="addToResubmission" accesskey="R">
									<label>Add to <b><u>R</u></b>esubmission</label> &nbsp;
									<label>|</label>
									<c:if test="${mod_eclaim}">
										<a href="./claimReconciliation.do?_method=addOrEditAttachment&claim_id=${claim.map.claim_id}">Add/Edit Attachment</a>
									</c:if>
									<label>|</label>
						</c:when>
						<c:when test="${claim.map.claim_batch_status eq 'S'}">
							<input type="checkbox" name="actionChk" id="actionChk"
								value="markAsDenied" accesskey="D">
							<label><b><u>M</u></b>ark As Denied</label> &nbsp;
							<label>|</label>
						</c:when>		
						<c:when test="${claim.map.claim_batch_status eq 'D'}">
							<c:if test="${mod_eclaim}">
								<a href="./claimReconciliation.do?_method=addOrEditAttachment&claim_id=${claim.map.claim_id}">Add/Edit Attachment</a>
								<label>|</label>
							</c:if>
						</c:when>		
						<c:when test="${(empty resubmissionId || resubmissionId == '') && claim.map.status eq 'R'}">
							<input type="checkbox" name="actionChk" id="actionChk"
								value="addToResubmission" accesskey="R">
							<label>Add to <b><u>R</u></b>esubmission</label> &nbsp;
							<label>|</label>
							<c:if test="${mod_eclaim}">
								<a href="./claimReconciliation.do?_method=addOrEditAttachment&claim_id=${claim.map.claim_id}">Add/Edit Attachment</a>
							</c:if>
							<label>|</label>
						</c:when>
				</c:choose>
			</c:when>
		</c:choose>


			<a href="./claimReconciliation.do?_method=list&claim_status=Denied&claim_status=ForResub.&sortOrder=claim_id&sortReverse=true&date_range=month">Reconciliations</a>

			<c:if test="${mod_eclaim}">
			<insta:screenlink screenId="update_mrd" extraParam="?_method=getMRDUpdateScreen&patient_id=${claim.map.patient_id}"
						target="_blank" label="Codification" addPipe="true"/>
			</c:if>
			<insta:screenlink screenId="visit_emr_screen" extraParam="?_method=list&visit_id=${claim.map.patient_id}"
						target="_blank" label="Visit EMR Search" addPipe="true"/>
		</td>
	 </tr>
</table>

<div class="legend">
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText">Closed</div>
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText">Denied</div>
	<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
	<div class="flagText">Denial Accepted</div>
	<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
	<div class="flagText">Open</div>
</div>

</form>

<form name="claimeditform">
<input type="hidden" id="claimEditRowId" value=""/>

<div id="claimEditDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Edit Claim Amount</legend>
		<table class="smallformtable">
			<tr>
				<td class="formlabel">Charge&nbsp;Group:</td>
				<td class="forminfo" colspan="2"> <label id="claim_item_group"></label> </td>
				<td class="formlabel">Charge&nbsp;Head:</td>
				<td class="forminfo" colspan="2"> <label id="claim_item_head"></label> </td>
			</tr>
			<tr>
				<td class="formlabel">Activity:</td>
				<td class="forminfo" colspan="5"> <label id="claim_item_activity"></label> </td>
			</tr>
			<tr>
				<td class="formlabel">Rate:</td>
				<td class="forminfo"> <label id="claim_item_rate"></label> </td>
				<td class="formlabel">Qty:</td>
				<td class="forminfo"> <label id="claim_item_qty"></label><label id="claim_item_pkg_unit"></label></td>
				<td class="formlabel">Orig&nbsp;Rate:</td>
				<td class="forminfo"> <label id="claim_item_orig_rate"></label> </td>
			</tr>
			<tr>
				<td class="formlabel">Discount:</td>
				<td class="forminfo"> <label id="claim_item_disc"></label> </td>
				<td class="formlabel">Amount:</td>
				<td class="forminfo"> <label id="claim_item_amount"></label> </td>
				<td class="formlabel">Orig&nbsp;Amt:</td>
				<td class="forminfo"> <label id="claim_item_orig_amount"></label> </td>
			</tr>
			<tr>
				<td class="formlabel">Patient&nbsp;Amt:</td>
				<td class="forminfo"> <label id="claim_item_pat_amount"></label> </td>
				<td class="formlabel">Claim&nbsp;Amt:</td>
				<td class="forminfo">
					<input type="text" name="claim_item_net_amount" id="claim_item_net_amount"
						onchange="recalcItemAmount()" class="numeric"
						style="width:80px;"/>
				</td>
				<td class="formlabel">Orig&nbsp;Claim:</td>
				<td class="forminfo"> <label id="claim_item_orig_net_amount"></label> </td>
			</tr>
			<tr>
				<td class="formlabel">Denial&nbsp;Acceptance:</td>
				<td>
					<c:choose>
						<c:when test="${((actionRightsMap.allow_denial_acceptance == 'A')||(roleId==1)||(roleId==2))}">
							<input type="checkbox" name="denialCheck" id="denialCheck"  onclick="disableOrEnableRejReason();" disabled/>
						</c:when>
						<c:otherwise>
							<input type="checkbox" name="denialCheck" id="denialCheck" disabled/>
						</c:otherwise>
					</c:choose>
				</td>
				<td class="formlabel">Rejection&nbsp;Reason:</td>
				<td>
					<insta:selectdb name="rejection_reasons_drpdn" id="rejection_reasons_drpdn"
									onchange="" value=""
									table="rejection_reason_categories" style="width:137px;"
									dummyvalue="--Select--" valuecol="rejection_reason_category_id"
									displaycol="rejection_reason_category_name"
									orderby="rejection_reason_category_name" disabled="true"/>
				</td>
			</tr>
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

<form name="claimHistoryForm">
<input type="hidden" id="claimHistoryRowId" value=""/>

<div class="resultList" id="claimHistoryDialog" style="display:none">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">View Claim History</legend>
		<table class="resultList" id="claimHistTblId">
      <tr>
        <th>Type</th>
        <th>Submission ID/Payment Reference</th>
        <th>Submission Date/Transaction Date</th>
        <th>Activity ID</th>
        <th>Qty</th>
        <th>Claim Amount</th>
        <th>VAT</th>
        <th>VAT(%)</th>
        <th>Approved</th>
        <th>Status</th>
        <th>Denial Code</th>
        <th>Res.Sub Type</th>
      </tr>
      <tr style="display:none">
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      	<td><label></label></td>
      </tr>
		</table>
	</fieldset>
	<table>
		<tr>
			<td><input type="button" onclick="onViewClaimHistryCancel()" value="Cancel"/></td>
		</tr>
	</table>
</div>
</div>
</form>

</body>
</html>