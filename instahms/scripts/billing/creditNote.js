var hasPlanCopayLimit;
var creditType ='P';

function init(){
	hasPlanCopayLimit = hasPlanVisitCopayLimit(planBean);

	if(!isInsuranceBill){
		document.getElementById('sponsorCreditNote').disabled=true;
	}
	if(patientSponsorCreditNotePath == 'PatientCreditNote'){
		document.getElementById('creditType').value='P';
	}else if(patientSponsorCreditNotePath == 'SponsorCreditNote'){
		document.getElementById('creditType').value='S';
		creditType ='S'
		setSelectedIndex(document.mainform.sponsor_type, "Primary");
		if(multiPlanExists)
			document.getElementById('sponsor_type').disabled=false ;
	}
	
	var specificPaymentModeJSON = JSON.parse(specificPaymentModeList);
	var paymentModeObj = document.getElementById("paymentModeId0");
	paymentModeObj.length = specificPaymentModeJSON.length -1;
	
	for(var i=0; i< specificPaymentModeJSON.length ; i++) {
		if(specificPaymentModeJSON[i].status == 'A') {
			var option  = new Option(specificPaymentModeJSON[i].payment_mode, specificPaymentModeJSON[i].mode_id);
			paymentModeObj[i] = option;
		}
	}
	enableBankDetails(paymentModeObj);
	enableCommissionDetails(paymentModeObj);	
	
	var paymentTypeObj = document.getElementById("paymentType");
	paymentTypeObj.length = 0;
	var option  = new Option("Refund", "refund");
	paymentTypeObj[0] = option;
	
	resetTotals(false, false);
	
	var patientWriteOff = document.getElementById('patientWriteOff').value;
	var sponsorWriteOff = document.getElementById('sponsorWriteOff').value;
	
	if(screenRightsForPatientCreditNote != 'A' || patientWriteOff == 'A')
		document.getElementById('patientCreditNote').disabled=true;
	if(screenRightsForSponsorCreditNote != 'A' || sponsorWriteOff == 'A')
		document.getElementById('sponsorCreditNote').disabled=true;


	if(null != primaryClosureType && !empty(primaryClosureType) && primaryClosureType !='W')
		document.getElementById('sponsorCreditNote').disabled=true;
	if(null != secondaryClosureType && !empty(secondaryClosureType) && secondaryClosureType !='W')
		document.getElementById('sponsorCreditNote').disabled=true;
	if((null == primaryClosureType || empty(primaryClosureType)) && (null == secondaryClosureType || empty(secondaryClosureType)))
		document.getElementById('sponsorCreditNote').disabled=true;
	
	if((null == primaryClosureType || empty(primaryClosureType)) 
			&& (null != secondaryClosureType && !empty(secondaryClosureType) && secondaryClosureType =='W')){
		setSelectedIndex(document.mainform.sponsor_type, "Secondary");
		document.getElementById('sponsor_type').disabled=true ;
	}
	if((null == secondaryClosureType || empty(secondaryClosureType)) 
			&& (null != secondaryClosureType && !empty(secondaryClosureType) && secondaryClosureType =='W')){
		setSelectedIndex(document.mainform.sponsor_type, "Primary");
		document.getElementById('sponsor_type').disabled=true ;
	}
	if(sponsorDueAmt == 0){
		document.getElementById('sponsor_type').disabled=true ;
		document.getElementById('sponsorCreditNote').disabled=true;
	}

	if(isInsuranceBill && document.getElementById("lblNetSponsorDue") != null){
		var netpatdue = formatAmountPaise(getPaise(document.getElementById("lblNetSponsorDue").innerHTML));
		if(netpatdue <= 0){
			document.getElementById('sponsorCreditNote').disabled=true;
			document.getElementById('sponsor_type').disabled=true ;
		}
	}
	
	var val = document.getElementById('creditType').value;
	var creditNoteObj='pricreditNote';
	//Disabling when credit note item level amount has crossed actual cost 
	var table = document.getElementById("chargesTable");	
	var num = getNumCharges();
	for(var i = 1;i<=num;i++) {
		var row = table.rows[i];
		if(val == 'P') {
			var amt= parseFloat(getElementByName(row,"patientamt").value);
			var patItemCrditAmt = parseFloat(getElementByName(row,"patItemChargeAmt").value);
			if((amt + patItemCrditAmt) <= 0){
				getElementByName(row,creditNoteObj).readOnly = true;
				getElementByName(row,creditNoteObj).value = "";
			}
		} else {
			var amt= parseFloat(getElementByName(row,"priInsClaimAmt").value);
			var priClaimRecvAmt = parseFloat(getElementByName(row,"priClaimRecievedAmt").value);
			var priItemCrditAmt = parseFloat(getElementByName(row,"priItemChargeAmt").value);
			if((amt - priClaimRecvAmt + priItemCrditAmt) <= 0){
				getElementByName(row,creditNoteObj).readOnly = true;
				getElementByName(row,creditNoteObj).value = "";
			}
		}
		
    }
}

function onChangeFilter(filterObj) {
	var filterGroup = mainform.filterServiceGroup.value;
	var filterHead = mainform.filterChargeHead.value;
	var filterPackage = mainform.filterPackage;

	filterCharges();
	resetTotals(false, false);

	if (filterGroup != '' || filterHead != '' || (filterPackage != null && filterPackage.value != '') ) {

		if (filterObj) 
			YAHOO.util.Dom.addClass(filterObj, 'filterActive');
		
		document.getElementById("filterRow").style.display = 'table-row';

	}else {
		if (filterObj) 
			YAHOO.util.Dom.removeClass(filterObj, 'filterActive');
		document.getElementById("filterRow").style.display = 'none';
	}
}

function filterCharges() {
	var num = getNumCharges();
   var table = document.getElementById("chargesTable");
   var filterGroup = mainform.filterServiceGroup.value;
	var filterHead = mainform.filterChargeHead.value;
	var filterPackage = mainform.filterPackage;
	for (var i=1; i<=num; i++) {
		var row = table.rows[i];
		var chargeGroup = getElementByName(row, 'service_group_id').value;
		var chargeHead = getElementByName(row, 'chargeHeadId').value;
		var deleted = getElementByName(row, 'delCharge').value;
		var excluded = getElementByName(row, 'chargeExcluded');
		var show = true;
		if ((filterGroup != "") && (filterGroup != chargeGroup))
			show = false;
		if ((filterHead != "") && (filterHead != chargeHead))
			show = false;
		//if (deleted == 'true' && document.mainform.showCancelled.checked)
			//show = false;
		if (filterPackage != null && filterPackage.value != '') {
			if (excluded != null && excluded.value == 'Y' && filterPackage.value == 'Included')
				show = false;
			if (excluded != null && excluded.value == 'N' && filterPackage.value == 'Excluded')
				show = false;
		}
		if (show) {
			row.style.display = "";
		} else {
			row.style.display = "none";
		}
	}
}

function getNumCharges() {
	// header, add row, hidden template row: totally 3 extra
	return document.getElementById("chargesTable").rows.length-2;
}


function resetTotals(claimAmtEdited, deductionEdited) {

	if ((origBillStatus == 'A') && (mainform.dynaPkgId != null)
			&& (mainform.dynaPkgId.value != '') && (mainform.dynaPkgId.value != 0)) {
		setPkgValueCaps();
		setPackageMarginAmount();
	}

	var claimableTotalPaise = 0;
	var serviceChargeableTotalPaise = 0;
	var patientDueAmount = 0;

	totDiscPaise = 0;
	totAmtPaise = 0;
	totAmtDuePaise = 0;
	totInsAmtPaise = 0;
	totSpnrAmtDuePaise = 0;
	totInsuranceClaimAmtPaise = 0;
	totPriInsuranceClaimAmtPaise = 0;
	totSecInsuranceClaimAmtPaise = 0;
	totCopayAmtPaise = 0;

	subTotAmtPaise = 0;
	subTotDiscPaise = 0;

	eligibleRewardPointsPaise = 0;

	var table = document.getElementById("chargesTable");
	
	// calculate the totals: exclude claim and round off.
	for (var i=0;i<getNumCharges();i++) {
		var delCharge = getIndexedFormElement(mainform, "delCharge", i);
		if (delCharge && "true" == delCharge.value) {
			continue;
		}
		var chargeHead = getIndexedValue("chargeHeadId", i);
		if (chargeHead == 'ROF' || chargeHead == 'CSTAX' || chargeHead == 'BSTAX') {
			// we'll deal with it later.
			continue;
		}

		totDiscPaise += getIndexedPaise("disc", i);
		totAmtPaise += getIndexedPaise("amt",i);
		totInsAmtPaise += getIndexedPaise("insClaimAmt",i);
		totInsAmtPaise += getIndexedPaise("sponsorTaxAmt",i);
		// Claim amount without deduction
		totInsuranceClaimAmtPaise += getIndexedPaise("insClaimAmt",i);
		totInsuranceClaimAmtPaise += getIndexedPaise("sponsorTaxAmt",i);

		totPriInsuranceClaimAmtPaise += getIndexedPaise("priInsClaimAmt",i);

		if(multiPlanExists) {
			totSecInsuranceClaimAmtPaise += getIndexedPaise("secInsClaimAmt",i);
			totInsAmtPaise =totPriInsuranceClaimAmtPaise + totSecInsuranceClaimAmtPaise;
			totInsuranceClaimAmtPaise =totPriInsuranceClaimAmtPaise + totSecInsuranceClaimAmtPaise;
			patientDueAmount += getIndexedPaise("insDeductionAmt",i);
		}
		
		if (table.rows[i+1].style.display != 'none') {
			// Package filter exists.
			var filterPackage = mainform.filterPackage;
			if (filterPackage != null && filterPackage.value != '') {
				if (filterPackage.value == 'Included') {
					subTotAmtPaise += getIndexedPaise("amount_included",i);

				}else if (filterPackage.value == 'Excluded') {
					subTotAmtPaise += (getIndexedPaise("amt",i) - getIndexedPaise("amount_included",i));

				}else {
					subTotAmtPaise += getIndexedPaise("amt",i);
				}
			}else {
				// No Package filter.
				subTotAmtPaise += getIndexedPaise("amt",i);
			}
			subTotDiscPaise += getIndexedPaise("disc",i);
		}

		if (!isPharmacyReturns(i)) {
			if (getIndexedFormElement(mainform, "insClaimTaxable", i) != null) {
				if (getIndexedFormElement(mainform, "insClaimTaxable",i).value == 'Y')
					claimableTotalPaise += getIndexedPaise("insClaimAmt",i);
			}
		}

		if (getIndexedFormElement(mainform, "serviceChrgApplicable", i) != null) {
			if (getIndexedFormElement(mainform, "serviceChrgApplicable",i).value == 'Y')
				serviceChargeableTotalPaise += getIndexedPaise("amt",i);
		}
		
		var eligibleToRedeem = getIndexedValue("eligible_to_redeem_points", i);
		var redemptionCapPer = getIndexedValue("redemption_cap_percent", i);
		redemptionCapPer = empty(redemptionCapPer) ? 0 : redemptionCapPer;
		if (eligibleToRedeem == 'Y') {
			var chAmtPaise = getIndexedPaise("amt",i);
			var eligibleAmtPaise = chAmtPaise * redemptionCapPer / 100;
			var eligibleAmt = formatAmountPaise(eligibleAmtPaise);
			var maxPointsRedeemable = Math.floor(eligibleAmt / points_redemption_rate);
			var calculatedEligibleAmtPaise =  getPaise(eligibleAmt - (eligibleAmt % points_redemption_rate));
			//setHiddenValue(i, "max_redeemable_points", maxPointsRedeemable);
			eligibleRewardPointsPaise += calculatedEligibleAmtPaise;
			
		}
	}

	var billDeductionPaise = 0;
	if (document.getElementById("insuranceDeduction") != null)
		billDeductionPaise = getElementPaise(document.getElementById("insuranceDeduction"));

	if (claimAmtEdited)
		totInsAmtPaise -= billDeductionPaise;

	// calculate (and update if bill is open) the service charge if there is one.
	var serChargeRowId = getChargeHeadRowId('BSTAX');
	if (serChargeRowId != null) {
		var servAmtPaise = getIndexedPaise("amt", serChargeRowId);
		var serChrgInsPayable = 'N';
		if (origBillStatus == 'A') {
			var newServAmtPaise = Math.round(serviceChargePer * serviceChargeableTotalPaise / 100);
			if (newServAmtPaise != servAmtPaise) {
				// reset claim amounts
				claimAmtEdited = claimAmtEdited && (newServAmtPaise != servAmtPaise);
				// update the row
				var row = getChargeRow(serChargeRowId);
				var serchrg = formatAmountPaise(newServAmtPaise);
				var remarks = "" + serviceChargePer + "% on " + formatAmountPaise(serviceChargeableTotalPaise);

				var chargeHead = findInList(jChargeHeads, "CHARGEHEAD_ID", 'BSTAX');
				serChrgInsPayable = chargeHead.INSURANCE_PAYABLE;

				setEditedAmounts(serChargeRowId, row, serchrg, 1, 0, serchrg, (serChrgInsPayable == 'Y'?serchrg:0), serchrg);
				setNodeText(row.cells[REMARKS_COL], remarks, 16);
				setIndexedValue("remarks", serChargeRowId, remarks);
				servAmtPaise = newServAmtPaise;
			}
		}
		// add the service charge amount to the total
		if(serChrgInsPayable == 'Y' && isTpa)
			totInsAmtPaise += servAmtPaise;

		totAmtPaise += servAmtPaise;

		if (serChrgInsPayable == 'Y' && isTpa)
			totInsuranceClaimAmtPaise += servAmtPaise;
	}

	// calculate (and update if bill is open) the claim tax amount if there is one.
	var insClaimRowId = getChargeHeadRowId('CSTAX');
	if (insClaimRowId != null) {
		var taxInPaise = getIndexedPaise("insClaimAmt", insClaimRowId);
		if (origBillStatus == 'A') {
			var newTaxInPaise = Math.round(claimServiceTaxPer * claimableTotalPaise / 100);
			if (newTaxInPaise != taxInPaise) {
				// reset claim amounts
				claimAmtEdited = claimAmtEdited && (newTaxInPaise != taxInPaise);
				// update the row
				var row = getChargeRow(insClaimRowId);
				var insTax = formatAmountPaise(newTaxInPaise);
				var remarks = "" + claimServiceTaxPer + "% on " + formatAmountPaise(claimableTotalPaise);
				setEditedAmounts(insClaimRowId, row, insTax, 1, 0, insTax, insTax, 0);
				setNodeText(row.cells[REMARKS_COL], remarks, 16);
				setIndexedValue("remarks", insClaimRowId, remarks);
				taxInPaise = newTaxInPaise;
			}
		}
		// add the claim amount to the total
		totInsAmtPaise += taxInPaise;
		totAmtPaise += taxInPaise;
		totInsuranceClaimAmtPaise += taxInPaise;
	}

	// calculate (and update if bill is open) update the round off if there is one charge row for it
	// The round off calculation is done separately for total and insurance.
	var roundOffRowId = getChargeHeadRowId('ROF');
	if (roundOffRowId != null) {
		var roundOffPaise = getIndexedPaise("amt", roundOffRowId);
		var insRoundOffPaise = getIndexedPaise("insClaimAmt", roundOffRowId);
		if (origBillStatus == 'A') {
			var newRoundOffPaise = getRoundOffPaise(totAmtPaise);
			var newInsRoundOffPaise = getRoundOffPaise(totInsAmtPaise);
			if (newRoundOffPaise != roundOffPaise || (newInsRoundOffPaise != insRoundOffPaise && isTpa)) {
				// reset claim amounts
				claimAmtEdited = claimAmtEdited && (newInsRoundOffPaise != insRoundOffPaise);
				// update the row
				var row = getChargeRow(roundOffRowId);
				var roundOff = formatAmountPaise(newRoundOffPaise);
				var insRoundOff = formatAmountPaise(newInsRoundOffPaise);
				var patientRoundOff = formatAmountPaise(newRoundOffPaise - newInsRoundOffPaise);
				setEditedAmounts(roundOffRowId, row, roundOff, 1, 0, roundOff, insRoundOff, patientRoundOff);
				roundOffPaise = newRoundOffPaise;
				insRoundOffPaise = newInsRoundOffPaise;
			}
		}
		// add the round off to the total
		totAmtPaise += roundOffPaise;
		totInsAmtPaise += insRoundOffPaise;
		totInsuranceClaimAmtPaise += insRoundOffPaise;
		totPriInsuranceClaimAmtPaise += insRoundOffPaise;
	}

	totCopayAmtPaise += (totAmtPaise - totInsuranceClaimAmtPaise);

	var billStatusObj = document.mainform.billStatus;
	// Sponsor amounts are calculated in the order of National, Insurance, Corporate.
	// Recalculate the sponsor amounts only if the claim amounts are edited.
	if (claimAmtEdited) {
		//Ins 3.0 if (hasPlanCopayLimit)
			//Ins 3.0 setPlanTotalClaimAmounts(totInsAmtPaise, claimAmtEdited, deductionEdited);
		//Ins 3.0 else
			//Ins 3.0 setTotalClaimAmounts(totInsAmtPaise);

	}else if (deductionEdited) {
		totInsAmtPaise = totInsuranceClaimAmtPaise;
		//Ins 3.0 if (hasPlanCopayLimit)
			//Ins 3.0 setPlanTotalClaimAmounts(totInsuranceClaimAmtPaise, claimAmtEdited, deductionEdited);
		//Ins 3.0 else
			//Ins 3.0 setTotalClaimAmounts(totInsuranceClaimAmtPaise);
		totInsAmtPaise -= billDeductionPaise;
	}
	setTotalClaimTitles();

	/*var priTotClaimPaise = 0;
	if (document.getElementById("primaryTotalClaim") != null) {
		priTotClaimPaise = getElementPaise(document.getElementById("primaryTotalClaim"));
	}

	var secTotClaimPaise = 0;
	if (document.getElementById("secondaryTotalClaim") != null) {
		secTotClaimPaise = getElementPaise(document.getElementById("secondaryTotalClaim"));
	}

	var priApprovalAmountPaise = getElementPaise(document.getElementById("primaryApprovalAmount"));
	var secApprovalAmountPaise = getElementPaise(document.getElementById("secondaryApprovalAmount"));
	if(multiPlanExists){
		if (document.getElementById("secondaryTotalClaim") != null) {
			totSecInsuranceClaimAmtPaise = (totSecInsuranceClaimAmtPaise < secApprovalAmountPaise || secApprovalAmountPaise == 0)
				? totSecInsuranceClaimAmtPaise : secApprovalAmountPaise;
			secTotClaimPaise = totSecInsuranceClaimAmtPaise;

			document.getElementById("secondaryTotalClaim").value = formatAmountPaise(secTotClaimPaise);
		}
	}*/

	//Ins 3.0 var maxClaimAmountPaise = priTotClaimPaise + secTotClaimPaise;
	//Ins 3.0 var unallocatedClaimPaise = totInsAmtPaise - maxClaimAmountPaise;

	//Ins 3.0 totInsAmtPaise -= unallocatedClaimPaise;

	if(document.getElementById("primaryTotalClaim") != null)
		document.getElementById("primaryTotalClaim").value  = formatAmountPaise(totPriInsuranceClaimAmtPaise);

	if(multiPlanExists && document.getElementById("secondaryTotalClaim") != null){
		document.getElementById("secondaryTotalClaim").value = formatAmountPaise(totSecInsuranceClaimAmtPaise);
	}

	var existingReceiptsPaise	= getPaise(existingReceipts);
	var tpaReceiptsPaise		= (existingSponsorReceipts==0) ? getPaise(existingRecdAmount) : getPaise(existingSponsorReceipts);
	var depositSetOffPaise		= getElementPaise(document.mainform.depositSetOff);
	var rewardPointsPaise		= getElementPaise(document.mainform.rewardPointsRedeemedAmount);

	// If Incoming other hospital bill - part of sponsor consolidated bill then totInsAmtPaise = totAmtPaise
	if (sponsorBillNo != null && sponsorBillNo != '' && visitType == 't' && billType == 'C')
		totInsAmtPaise = totAmtPaise;

	totAmtDuePaise = totAmtPaise - totInsAmtPaise - depositSetOffPaise - rewardPointsPaise - existingReceiptsPaise;
	if ((totAmtDuePaise == 0 && totInsuranceClaimAmtPaise == 0) && billDeductionPaise != 0)
		totAmtDuePaise += billDeductionPaise;

	// Eligible reward points amount.
	var strEligibleAmt = formatAmountPaise(eligibleRewardPointsPaise);
	setNodeText("lblAvailableRewardPointsAmount", strEligibleAmt);

	// Reset the max eligible amount to be redeemed.
	availableRewardPointsAmount = strEligibleAmt;

	// filter totals row : billed amount, discount, net amount
	var strSubNetAmount = formatAmountPaise((subTotAmtPaise + subTotDiscPaise));
	var strSubTotDiscount = formatAmountPaise(subTotDiscPaise);
	var strSubTotal = formatAmountPaise(subTotAmtPaise);
	setNodeText("lblFilteredAmount", strSubTotal);
	setNodeText("lblFilteredDisc", strSubTotDiscount);
	setNodeText("lblFilteredNetAmt", strSubNetAmount, 0, strSubTotal + " - " + strSubTotDiscount);

	// row 1: billed amount, discount, net amount
	var strTotBilled = formatAmountPaise((totAmtPaise + totDiscPaise));
	var strTotDiscount = formatAmountPaise(totDiscPaise);
	var strNetAmount = formatAmountPaise(totAmtPaise);
	setNodeText("lblTotBilled", strTotBilled);
	setNodeText("lblTotDisc", strTotDiscount);
	setNodeText("lblTotAmt", strNetAmount, 0, strTotBilled + " - " + strTotDiscount);

	//Ins 3.0 var hasUnallocatedAmt = (totInsuranceClaimAmtPaise != 0
								//&& ((totInsuranceClaimAmtPaise - maxClaimAmountPaise) != billDeductionPaise));
	//Ins 3.0 var unallocPaise = totInsuranceClaimAmtPaise - maxClaimAmountPaise;

	// row 2: Patient Amount, [Deposit Set Off], Patient Payments, Patient Due
	var strPatientAmount = 0;
	//Ins 3.0 if (hasUnallocatedAmt)	strPatientAmount = formatAmountPaise(totCopayAmtPaise + unallocPaise);
	// else

	strPatientAmount = formatAmountPaise(totCopayAmtPaise + billDeductionPaise);
	var strDepositSetOff = formatAmountPaise(depositSetOffPaise);
	var strRewardPointsAmt = formatAmountPaise(rewardPointsPaise);
	var strPatientDue = formatAmountPaise(totAmtDuePaise );
	var strReceipts = formatAmountValue(existingReceipts);
	var strDeduction = formatAmountPaise(billDeductionPaise);

	var titlePatientAmt = formatAmountPaise(totCopayAmtPaise);
	if (document.getElementById("lblTotInsAmt") != null) {
		//Ins 3.0 if (hasUnallocatedAmt)	titlePatientAmt += " + " + formatAmountPaise(unallocPaise);
		// else
		titlePatientAmt += " + " + strDeduction;
	}

	var titlePatientDue = strPatientDue;
	titlePatientDue += " - " + strReceipts;

	if (document.getElementById("lblDepositsSetOff") != null)
		titlePatientDue += " - " + strDepositSetOff;
	if (document.getElementById("lblRewardPointsAmt") != null)
		titlePatientDue += " - " + strRewardPointsAmt;

	if (document.getElementById("lblTotInsAmt") != null)
		setNodeText("lblPatientAmount", strPatientAmount, 0, titlePatientAmt);
	else
		setNodeText("lblPatientAmount", strPatientAmount);

	if(document.getElementById("netpatientCreditNoteAmt") != null) {
		var patientCreditNoteValue = parseFloat(document.getElementById("netpatientCreditNoteAmt").value);
		var netAmount = parseFloat(strPatientDue) + patientCreditNoteValue;
		setNodeText("lblNetPatientDue", formatAmountPaise(getPaise(netAmount)));
	}
	
	setNodeText("lblDepositsSetOff", strDepositSetOff);
	setNodeText("lblRewardPointsAmt", strRewardPointsAmt);
	setNodeText("lblExistingReceipts", strReceipts);
	setNodeText("lblPatientDue", strPatientDue, 0, titlePatientDue);
	setNodeText("lblWrittenOffAmt", strPatientDue, 0, titlePatientDue);
	// row 3: Sponsor Amount, Sponsor payments, Sponsor Due

	var strInsAmt = formatAmountPaise((totInsAmtPaise));
	var strSponsorReceipts = formatAmountValue(existingSponsorReceipts);
	var strSponsorRecdAmount = formatAmountValue(existingRecdAmount);
	totSpnrAmtDuePaise = totInsAmtPaise - tpaReceiptsPaise;
	var strSponsorDue = formatAmountPaise(totSpnrAmtDuePaise);
	
	if(document.getElementById("netsponsorCreditNoteAmt") != null) {
		var sponsorCreditNoteValue = parseFloat(document.getElementById("netsponsorCreditNoteAmt").value);
		var netAmount =formatAmountPaise(getPaise(parseFloat(strSponsorDue) + sponsorCreditNoteValue));
		setNodeText("lblNetSponsorDue", netAmount);
	}
	
	setNodeText("lblTotInsAmt", strInsAmt, 0, strNetAmount + " - " + strPatientDue);
	setNodeText("lblSponsorRecdAmount", strSponsorRecdAmount);
	setNodeText("lblSponsorReceipts", strSponsorReceipts);
	setNodeText("lblSponsorDue", strSponsorDue, 0, strInsAmt + " - " + strSponsorRecdAmount+ " - " + strSponsorReceipts);
	setNodeText("lblSpnrWrittenOffAmt", strSponsorDue, 0);
}


function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges() + 1;
}

function getChargeRow(i) {
	i = parseInt(i);
	var table = document.getElementById("chargesTable");
	return table.rows[i + getFirstItemRow()];
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(mainform, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function isPharmacyReturns(i) {
	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'PHCRET' || chargeHead == 'PHRET') {
		return true;
	}
	return false;
}

function isPharmacySales(i) {
	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'PHCMED' || chargeHead == 'PHMED') {
		return true;
	}
	return false;
}

function getChargeHeadRowId(chargeHead) {
	var headRowId = null;
	for (var i=0;i<getNumCharges();i++) {
		var delCharge = getIndexedFormElement(mainform, "delCharge", i);

		if (delCharge && "true" == delCharge.value)
			continue;

		if (getIndexedFormElement(mainform, "chargeHeadId", i).value == chargeHead) {
			headRowId = i;
			break;
		}
	}
	return headRowId;
}

function setTotalClaimTitles() {

	if (hasPlanCopayLimit) {
		setSponsorWithPlanTitle(planBean, document.getElementById("primaryTotalClaim"));

	}else {
		if (!empty(priSponsorType) && priSponsorType == 'N') {
			setSponsorTitle(priSponsorId, document.getElementById("primaryTotalClaim"));
		}

		if (!empty(secSponsorType) && secSponsorType == 'N') {
			setSponsorTitle(secSponsorId, document.getElementById("secondaryTotalClaim"));
		}
	}
}

function setSponsorWithPlanTitle(plan, obj) {

	var visitCoPayPaise = null;
	var visitTypeTxt = visitType == 'o' ? 'OP' : 'IP';
	if (visitType == 'o')
		visitCoPayPaise = !empty(plan.op_visit_copay_limit) ? getPaise(plan.op_visit_copay_limit) : 0;

	else if (visitType == 'i')
		visitCoPayPaise = !empty(plan.ip_visit_copay_limit) ? getPaise(plan.ip_visit_copay_limit) : 0;

	if (obj != null && restrictionType == 'N') {
		obj.title =
			' Plan '+visitTypeTxt+' Visit Copay Limit : '+formatAmountPaise(visitCoPayPaise);
	}
}

var sponsorMap = {}

function getSponsorDetails(sponsorId) {
	var xhttp = new XMLHttpRequest();
	var url = cpath + '/master/tpas/getsponsordetails.json?sponsor_id=' + sponsorId;
	xhttp.open("GET", url, false);
	xhttp.send(null);
	if (xhttp.readyState == 4) {
		if ( (xhttp.status == 200) && (xhttp.responseText != null ) ) {
			return eval('('+xhttp.responseText.sponsor_details+')');
		}
	}
	return null;
}

function setSponsorTitle(sponsorId, obj) {
	var noOfDays = getNoOfDaysOfStay();
	var natSpnsr;
	if(sponsorsMap.sponsorId) {
		natSpnsr = sponsorsMap.sponsorId;
	} else {
		natSpnsr = getSponsorDetails(sponsorId);
		sponsorsMap.sponsorId = natSpnsr;
	}
	var perDayReimbursementPaise = !empty(natSpnsr) && !empty(natSpnsr.per_day_rate) ? getPaise(natSpnsr.per_day_rate) : 0;
	var perVisitCoPayOPPaise = !empty(natSpnsr) && !empty(natSpnsr.per_visit_copay_op) ? getPaise(natSpnsr.per_visit_copay_op) : 0;
	var perVisitCoPayIPPaise = !empty(natSpnsr) && !empty(natSpnsr.per_visit_copay_ip) ? getPaise(natSpnsr.per_visit_copay_ip) : 0;

	if (obj != null) {
		if (opType != 'F' && opType != 'D') {
			if (!empty(visitType) && visitType == 'i') {
				if (!empty(isPrimaryBill) && isPrimaryBill == 'Y') {
					obj.title =
						getString('js.billing.billlist.perdayreimbursement')+formatAmountPaise(perDayReimbursementPaise) +
						' \n'+getString('js.billing.billlist.pervisitcopay')+formatAmountPaise(perVisitCoPayIPPaise) +
						' \n '+getString('js.billing.billlist.noofdays')+ noOfDays;
				}else {
					obj.title = getString('js.billing.billlist.pervisitcopay')+formatAmountPaise(perVisitCoPayIPPaise);
				}
			}else {
				if ((!empty(isPrimaryBill) && isPrimaryBill == 'Y')
						 || (parseInt(visitTpaBills) == 1) || (parseInt(visitTpaBills) > 1 &&
								((!empty(firstTpaBill) && firstTpaBill == document.mainform.billNo.value)))) {

					obj.title =
						getString('js.billing.billlist.pervisitcopay')+formatAmountPaise(perVisitCoPayOPPaise) +
						' \n'+getString('js.billing.billlist.noofdays')+ noOfDays;
				}else {
					obj.title = getString("js.billing.billlist.sponsoramount")+formatAmountPaise(getPaise(obj.value));
				}
			}
		}else {
			if (!empty(visitType) && visitType == 'i') {
				if (!empty(isPrimaryBill) && isPrimaryBill == 'Y') {
					obj.title =
						getString('js.billing.billlist.perdayreimbursement')+formatAmountPaise(perDayReimbursementPaise) +
						' \n'+getString("js.billing.billlist.sponsoramount")+formatAmountPaise(getPaise(obj.value)) +
						' \n'+getString('js.billing.billlist.noofdays')+ noOfDays;
				}else {
					obj.title = getString("js.billing.billlist.sponsoramount")+formatAmountPaise(getPaise(obj.value));
				}
			}else {
				obj.title = getString("js.billing.billlist.sponsoramount")+formatAmountPaise(getPaise(obj.value));
			}
		}
	}
}

function hasPlanVisitCopayLimit(plan) {
	var hasVisitCopayLimit = false;
	if (empty(plan))
		return hasVisitCopayLimit;

	if (visitType == 'o')
		hasVisitCopayLimit = (!empty(plan.op_visit_copay_limit) && getPaise(plan.op_visit_copay_limit) != 0);

	else if (visitType == 'i')
		hasVisitCopayLimit = (!empty(plan.ip_visit_copay_limit) && getPaise(plan.ip_visit_copay_limit) != 0);

	return hasVisitCopayLimit;
}


function setCreditamtValue(obj){
	var table = document.getElementById("chargesTable");
	var creditAmt=parseFloat(0);
	var num = getNumCharges();
	for(var i = 1;i<=num;i++)
    {
		var row = table.rows[i];
		if(!empty(getElementByName(row,"pricreditNote").value)){
			creditAmt += parseFloat(getElementByName(row,"pricreditNote").value);
			setHiddenValue(row, 'isEdited','t');
		}
    }
	
	var patientDue = document.getElementById('lblPatientDue').innerHTML;
	patientDue = parseFloat(patientDue);
	
	if(document.getElementById('lblSponsorDue') != null ) {
		var sponsorDue = document.getElementById('lblSponsorDue').innerHTML;
		sponsorDue = parseFloat(sponsorDue);
	}
	
	var netPatientCreditNoteAmt = document.getElementById('netpatientCreditNoteAmt').value;
	netPatientCreditNoteAmt = parseFloat(netPatientCreditNoteAmt);
	
	if(document.getElementById('netsponsorCreditNoteAmt')!= null) {
		var netSponsorCreditNoteAmt = document.getElementById('netsponsorCreditNoteAmt').value;
		netSponsorCreditNoteAmt = parseFloat(netSponsorCreditNoteAmt);
	}
	
	
	if(creditType == 'S') {
		document.getElementById('lblSponsorCreditAmount').textContent =formatAmountPaise(getPaise(creditAmt));
		document.getElementById('netsponsorCreditNote').textContent = formatAmountPaise(getPaise(netSponsorCreditNoteAmt - formatAmountPaise(getPaise(creditAmt))));
		document.getElementById('lblNetSponsorDue').textContent = formatAmountPaise(getPaise(sponsorDue + netSponsorCreditNoteAmt - formatAmountPaise(getPaise(creditAmt))));
		document.getElementById('creditNoteTotalAmt').value=formatAmountPaise(getPaise(creditAmt));
	}
	else {
		
		document.getElementById('lblPatientCreditAmount').textContent =formatAmountPaise(getPaise(creditAmt));
		document.getElementById('netpatientCreditNote').textContent = formatAmountPaise(getPaise(netPatientCreditNoteAmt - formatAmountPaise(getPaise(creditAmt))));
		document.getElementById('lblNetPatientDue').textContent = formatAmountPaise(getPaise(patientDue + netPatientCreditNoteAmt - formatAmountPaise(getPaise(creditAmt))));
		document.getElementById('creditNoteTotalAmt').value=formatAmountPaise(getPaise(creditAmt));

		if((patientDue + netPatientCreditNoteAmt - creditAmt) < 0){
			document.getElementById('totPayingAmt0').value = formatAmountPaise(getPaise(-1 * ((patientDue + netPatientCreditNoteAmt - creditAmt))));
		}
		else {
			document.getElementById('totPayingAmt0').value = "";
		}
		
	}
		
	
	
}

function doSave(){
	if (billingcounterId == null || billingcounterId == '') {
		showMessage("js.billing.creditNote.counterNotMapped");
		return false;
	}
	if(creditType == 'P' && !validatePaymentTagFields())
		return false;
	if(!validateCreditAmt())
		return false;
	document.mainform.submit();
}

function validateCreditAmt(){
	var table = document.getElementById("chargesTable");
	var num = getNumCharges();
	var totalAmt=parseFloat(0);

	for(var i = 1;i<=num;i++)
    {
		var row = table.rows[i];
		if(creditType == 'P'){
			if(!empty(getElementByName(row,"pricreditNote").value)){
				var creditAmt = parseFloat(getElementByName(row,"pricreditNote").value);
				var amt= parseFloat(getElementByName(row,"patientamt").value);
				var patItemCrditAmt = parseFloat(getElementByName(row,"patItemChargeAmt").value);
				if((getPaise(creditAmt)) > (getPaise(amt + patItemCrditAmt))){
					showMessage("js.billing.creditNote.cannotcreditnoteamountGreater");
					return false;
				}
				totalAmt += parseFloat(getElementByName(row,"pricreditNote").value);
			}	
		}
		if(creditType == 'S'){
			if(!empty(getElementByName(row,"pricreditNote").value)){
				var creditAmt = parseFloat(getElementByName(row,"pricreditNote").value);
				var amt;
				var sponsorCreditAmt;
				var claimRecvAmt
				if(getSelValue(document.getElementById("sponsor_type")) == 'Primary') {
					amt = parseFloat(getElementByName(row,"priInsClaimAmt").value);
					claimRecvAmt = parseFloat(getElementByName(row,"priClaimRecievedAmt").value);
					sponsorCreditAmt = parseFloat(getElementByName(row,"priItemChargeAmt").value);
				} else if(getSelValue(document.getElementById("sponsor_type")) == 'Secondary') {
					amt = parseFloat(getElementByName(row,"secInsClaimAmt").value);
					claimRecvAmt = parseFloat(getElementByName(row,"secClaimRecievedAmt").value);
					sponsorCreditAmt = parseFloat(getElementByName(row,"secItemChargeAmt").value);
				}
					
				if((getPaise(creditAmt)) > (getPaise(amt - claimRecvAmt + sponsorCreditAmt))){
					showMessage("js.billing.creditNote.cannotcreditnoteamountGreater");
					return false;
				}
				totalAmt += parseFloat(getElementByName(row,"pricreditNote").value);
			}
		}
    }
	if(totalAmt == 0){
		showMessage("js.billing.creditNote.enterValidCreditAmtForBillCreation");
		return false;
	}
	if(totalAmt > 0 && creditType == 'S'){
		var netpatdue = formatAmountPaise(getPaise(document.getElementById("lblNetSponsorDue").innerHTML));
		if(netpatdue < 0){
			showMessage("js.billing.creditNote.validSponsorCreditAmtForBillCreation");
			return false;
		}
	}

	return true;
}
function setCreditType(val){
	document.getElementById('creditType').value=val;
	var amtObj;
	var creditNoteObj='pricreditNote';
	
	var patientDue = document.getElementById('lblPatientDue').innerHTML;
	patientDue = parseFloat(patientDue);
	
	if(document.getElementById('lblSponsorDue') != null ) {
		var sponsorDue = document.getElementById('lblSponsorDue').innerHTML;
		sponsorDue = parseFloat(sponsorDue);
	}
	
	var netPatientCreditNoteAmt = document.getElementById('netpatientCreditNoteAmt').value;
	netPatientCreditNoteAmt = parseFloat(netPatientCreditNoteAmt);
	
	if(document.getElementById('netsponsorCreditNoteAmt')!= null) {
		var netSponsorCreditNoteAmt = document.getElementById('netsponsorCreditNoteAmt').value;
		netSponsorCreditNoteAmt = parseFloat(netSponsorCreditNoteAmt);
	}
	
	
	if(val == 'P'){
		document.getElementById('sponsorCreditNote').checked= false;
		creditType="P";
		amtObj='patientamt';		
		if(billingcounterId != null && billingcounterId != '')
			$('#payments').css("display","block");
		setSelectedIndex(document.mainform.sponsor_type, "");
		if(multiPlanExists){
			document.getElementById('sponsor_type').disabled=true ;
		}
		document.getElementById("saveButton").innerHTML=getString("js.billing.creditNote.payandclosebutton"); 
		
		if(document.getElementById('netsponsorCreditNote')!= null) {
			document.getElementById('netsponsorCreditNote').textContent = netSponsorCreditNoteAmt;
		}
		
		if(document.getElementById('lblNetSponsorDue')!= null) {
			document.getElementById('lblNetSponsorDue').textContent = formatAmountPaise(getPaise(sponsorDue + netSponsorCreditNoteAmt));
		}
		
		document.getElementById('netpatientCreditNote').textContent = netPatientCreditNoteAmt;
		document.getElementById('lblNetPatientDue').textContent = formatAmountPaise(getPaise(patientDue + netPatientCreditNoteAmt));
	}
	if(val == 'S'){
		document.getElementById('patientCreditNote').checked=false;
		creditType="S";
		amtObj='priInsClaimAmt';
		if(billingcounterId != null && billingcounterId != '')
			$('#payments').css("display","none");
		setSelectedIndex(document.mainform.sponsor_type, "Primary");
		if(multiPlanExists){
			document.getElementById('sponsor_type').disabled=false ;
		}
		document.getElementById("saveButton").innerHTML=getString("js.billing.creditNote.saveandclosebutton"); 
		
		document.getElementById('netpatientCreditNote').textContent = netPatientCreditNoteAmt;
		document.getElementById('lblNetPatientDue').textContent = formatAmountPaise(getPaise(patientDue + netPatientCreditNoteAmt));
		document.getElementById('netsponsorCreditNote').textContent = netSponsorCreditNoteAmt;
		document.getElementById('lblNetSponsorDue').textContent = formatAmountPaise(getPaise(sponsorDue + netSponsorCreditNoteAmt));
	}
	
	var table = document.getElementById("chargesTable");	
	var num = getNumCharges();
	for(var i = 1;i<=num;i++)
    {
		var row = table.rows[i];
		var amt = parseFloat(getElementByName(row,amtObj).value);
		if(amt > parseFloat(0)){
			getElementByName(row,creditNoteObj).readOnly = false;
			getElementByName(row,creditNoteObj).value = "";
		}else{
			getElementByName(row,creditNoteObj).readOnly = true;
			getElementByName(row,creditNoteObj).value = "";
		}
		
		var claimStatus = getElementByName(row,"claimStatus").value;
		if(claimStatus == 'C' && amtObj != 'patientamt') {
			getElementByName(row,creditNoteObj).readOnly = true;
			getElementByName(row,creditNoteObj).value = "";
		}
		
		//Disabling when credit note item level amount has crossed actual cost 
		if(val == 'P') {
			var amt= parseFloat(getElementByName(row,"patientamt").value);
			var patItemCrditAmt = parseFloat(getElementByName(row,"patItemChargeAmt").value);
			if((amt + patItemCrditAmt) <= 0){
				getElementByName(row,creditNoteObj).readOnly = true;
				getElementByName(row,creditNoteObj).value = "";
			}
		} else {
			var amt= parseFloat(getElementByName(row,"priInsClaimAmt").value);
			var priItemCrditAmt = parseFloat(getElementByName(row,"priItemChargeAmt").value);
			var priClaimRecvAmt = parseFloat(getElementByName(row,"priClaimRecievedAmt").value);
			if((amt - priClaimRecvAmt + priItemCrditAmt) <= 0){
				getElementByName(row,creditNoteObj).readOnly = true;
				getElementByName(row,creditNoteObj).value = "";
			}
		}
		
    }
	document.getElementById('lblSponsorCreditAmount').textContent =formatAmountPaise(getPaise(0));
	document.getElementById('lblPatientCreditAmount').textContent =formatAmountPaise(getPaise(0));
}

function setHiddenValue(row, name, value) {
	var el = getElementByName(row, name);
	if (el)
		el.value = value;
}

$(document).ready(function() {
	//$('#addPaymentMode,#deletePayMode,#payRefsCardTr,#payRefsBankTr,#payRefsTr').css("display","none");
	$('#addPaymentMode,#deletePayMode').css("display","none");
	$('#totPayingAmt0').prop('readonly', true);
});


function onChangeOfSponsorType(){
	var table = document.getElementById("chargesTable");	
	var num = getNumCharges();
	var creditNoteObj='pricreditNote';
	if(getSelValue(document.getElementById("sponsor_type")) == ''){	
		for(var i = 1;i<=num;i++)
	    {
			var row = table.rows[i];
			getElementByName(row,creditNoteObj).readOnly = true;
			getElementByName(row,creditNoteObj).value = "";			
	    }
		showMessage("js.billing.creditNote.selectionOfSponsorIsMandatory");
	}else{
		for(var i = 1;i<=num;i++) {
			var row = table.rows[i];
			if(getSelValue(document.getElementById("sponsor_type")) == 'Primary'){			
				var amt = parseFloat(getElementByName(row,"priInsClaimAmt").value);
				var priClaimRecvAmt = parseFloat(getElementByName(row,"priClaimRecievedAmt").value);
				var priItemCrditAmt = parseFloat(getElementByName(row,"priItemChargeAmt").value);
				if(amt > parseFloat(0)){
					getElementByName(row,creditNoteObj).readOnly = false;
					getElementByName(row,creditNoteObj).value = "";
				}else{
					getElementByName(row,creditNoteObj).readOnly = true;
					getElementByName(row,creditNoteObj).value = "";
				}
				
				if((amt - priClaimRecvAmt + priItemCrditAmt) <= 0){
					getElementByName(row,creditNoteObj).readOnly = true;
					getElementByName(row,creditNoteObj).value = "";
				}
			}
			else if(getSelValue(document.getElementById("sponsor_type")) == 'Secondary'){			
				var amt = parseFloat(getElementByName(row,"secInsClaimAmt").value);
				var secClaimRecvAmt = parseFloat(getElementByName(row,"secClaimRecievedAmt").value);
				var secItemCrditAmt = parseFloat(getElementByName(row,"secItemChargeAmt").value);
				if(amt > parseFloat(0)){
					getElementByName(row,creditNoteObj).readOnly = false;
					getElementByName(row,creditNoteObj).value = "";
				}else{
					getElementByName(row,creditNoteObj).readOnly = true;
					getElementByName(row,creditNoteObj).value = "";
				}
				
				if((amt - secClaimRecvAmt + secItemCrditAmt) <= 0){
					getElementByName(row,creditNoteObj).readOnly = true;
					getElementByName(row,creditNoteObj).value = "";
				}
			}
			
			var claimStatus = getElementByName(row,"claimStatus").value;
			if(claimStatus == 'C') {
				getElementByName(row,creditNoteObj).readOnly = true;
				getElementByName(row,creditNoteObj).value = "";
			}
	    }		
	}
}

function billPrint(option,userNameInBillPrint) {

	var billNo = document.mainform.billNo.value;
	var visitId = document.mainform.visitId.value;
	var printerType = document.mainform.printType.value;
	//var optionParts = option.split("-");
	var billType = document.mainform.printBill.value;
	var optionParts  = billType.split("-");

	var url = cpath + "/pages/Enquiry/billprint.do?_method=";
	if (optionParts[0] == 'BILL')
		url += "billPrint";
	else if (optionParts[0] == 'EXPS')
		url += "expenseStatement";
	else if (optionParts[0] == 'PHBI')
		url += "pharmaBreakupBill";
	else if (optionParts[0] == 'PHEX')
		url += "pharmaExpenseStmt";
	else if (optionParts[0] == "CUSTOM")
		url += "billPrintTemplate";
	else if(optionParts[0] == 'CUSTOMEXP'){
		url += "visitExpenceStatement";
	}else	{
		alert("Unknown bill print type: " + optionParts[0]);
		return false;
	}
	url += "&billNo="+billNo;		// will be ignored for expense statement
	url += "&visitId="+visitId;		// will be ignored for bills
	url += "&printUserName="+userNameInBillPrint;
	if (optionParts[1])
		url += "&detailed="+optionParts[1];

	if (optionParts[2])
		url += "&option="+optionParts[2];
	url += "&printerType="+printerType;
	if (!empty(billType) && (optionParts[0] == 'CUSTOM' || optionParts[0] == "CUSTOMEXP")) {
		url +="&billType="+billType.substring(parseInt(optionParts[0].length)+1,billType.length);
	} else {
		url +="&billType="+optionParts[1];
	}
	window.open(url);
}

function loadTemplates(obj) {
	var templateName = '';
	var subParts;
	if (obj != null) {
		templateName = obj.value;

		if (!empty(templateName)) {
			subParts = templateName.split("-");
		}
		var disabled = true;
		if (!empty(templateName) && (subParts[0] == 'CUSTOM' || subParts[0] == "CUSTOMEXP")) {
			subPart = templateName.substring(parseInt(subParts[0].length)+1,templateName.length);

		var template = findInList(templateList, "template_name", subPart);
		if (template && !empty(template.download_content_type))
			disabled = false;
		}
		document.getElementById('downloadButton').disabled = disabled;
	}
}

function submitForm() {
	var billNo = document.mainform.billNo.value;
	document.billCSVdownloadForm.billNo.value = billNo;
	document.billCSVdownloadForm.template_id.value = document.mainform.printBill.value;
	document.billCSVdownloadForm.printerId.value = document.mainform.printType.value;
	document.billCSVdownloadForm.submit();
}

