var chargesAdded = 0;
var chargesEdited = 0;
var billStatusList = null;

var mainform;
var editform;
var addOrderDialog;
var oAutoCompDisc;
var oAutoCompOverall;
var oAutoCompConduct;
var oAutoCompPresc;
var oAutoCompRef;
var oAutoCompHosp;
var eConductedAutoComp;
var patientInfo = null;
var gIsInsurance = false;
var hasPlanCopayLimit = false;
var visitLevelAlertMsgs = "";
var discEdited = false;
var sponsorsMap = {};

totDiscPaise = 0;
totAmtPaise = 0;
totInsAmtPaise = 0;
totCreditsPaise = 0;
totAmtDuePaise = 0;
totSpnrAmtDuePaise = 0;
totInsuranceClaimAmtPaise = 0;
totSecInsuranceClaimAmtPaise = 0;
totPriInsuranceClaimAmtPaise = 0;

function init() {
	mainform = document.mainform;
	editform = document.editform;
	addform = document.addform;

	if ( (roleId == 1) || (roleId == 2) ) {
		addToBillRights = 'A';
		allowRateIncr = 'A'; allowBackDate = 'A';
		cancelBillRights = 'A'; cancelElementsInBillRights = 'A';
		editBillRights = 'A';
		allowRefundRights = 'A';
		allowRateDcr = 'A';
	}

	// column indexes
	var i=0;
	SEL_COL=i++; DATE_COL = i++; ORDER_COL = i++; HEAD_COL = i++; CODE_COL = i++;
	DESC_COL = i++; REMARKS_COL = i++;
	RATE_COL = i++; QTY_COL = i++; UNITS_COL = i++;	DISC_COL = i++;	AMT_COL = i++;

	if (editform.eClaimAmt != null) {
		if(multiPlanExists) {
			CLAIM_COL = i++;
			SEC_CLAIM_COL = i++;
			DED_COL = i++;
			PRE_AUTH_COL = i++;
			SEC_PRE_AUTH_COL = i++;
		} else {
			CLAIM_COL = i++;
			DED_COL = i++;
			PRE_AUTH_COL = i++;
		}
	}
	if (cancelElementsInBillRights == 'A' || roleId == 1 || roleId ==2) {
		TRASH_COL = i++;
	}
	EDIT_COL = i++;

	// applicable status values for the bill status dropdown
	if(cancelBillRights == 'A' && (billCancelRequiresApproval == 'N' || (billCancelRequiresApproval == 'Y' && cancellationApprovalStatus == 'A'))) {
		billStatusList = [{status:'A',value:1,text:'Open'},
					   {status:'F',value:2,text:'Finalized'},
					   {status:'C',value:3,text:'Closed'},
					   {status:'X',value:4,text:'Cancelled'}];
	} else {
		billStatusList = [{status:'A',value:1,text:'Open'},
					   {status:'F',value:2,text:'Finalized'},
					   {status:'C',value:3,text:'Closed'}];
	}

	enableDisableInputs();
//	loadDiscAuthArray();
	enableDischargeDate();
	/*
	 * Normally, we should be setting these in the JSP, but since it is invisible, it is OK to
	 * do it here so that the logic is not duplicated here as well as JSP
	 */
//	setAllDiscountTitles();

//	initDiscountAuthorizerAutoComplete("overallDiscByName","overallDiscByName_dropdown", "overallDiscBy",
//			oAutoCompOverall);

	initEditChargeDialog();

	var doctors = { "doctors": jDoctors };
	var anaList = filterList(jDoctors, 'dept_id', 'DEP0002');
	var anaesthetists = { "doctors": anaList };
	var discauths = { "discount_authorizer": jDiscountAuthorizers };
	var patientId = document.getElementById('visitId').value;
	if (document.getElementById("orderDialogAddDialog") != null) {
		addOrderDialog = new Insta.AddOrderDialog('btnAddItem', rateplanwiseitems, null, onAddCharge,
				doctors, anaesthetists, visitType, patientBedType, patientOrgId,
				prescribingDocName, prescribingDoctor, regDate, regTime,
				allowBackDateBillActivities,fixedOtCharges,discauths, forceSubGroupSelection,regPref,anaeTypes,
				patientId,document.mainform.billNo.value );
		addOrderDialog.allowQtyEdit = true;
		addOrderDialog.setInsurance(isTpa, planId);
		addOrderDialog.billOpenDate = openDate+" "+openTime;
		gIsInsurance = isTpa;
	}

	initProcedureNames();
	if (document.mainform.dynaPkgName) setPkgValueCaps();
	hasPlanCopayLimit = hasPlanVisitCopayLimit(planBean);

	//resetTotals(false, false);
	mrno = document.getElementById("mrno").value;
	!empty(mrno) ? patientInfo = getVisitDoctorConsultationDetails(mrno) : null ;
	document.mainform.showCancelled.checked=true;
	onChangeFilter();
	highlightMarkedOnes('onload');
	showAlertRemarks();


}

function submitForm() {
	var billNo = document.mainform.billNo.value;
	document.billCSVdownloadForm.billNo.value = billNo;
	document.billCSVdownloadForm.template_id.value = document.mainform.printBill.value;
	document.billCSVdownloadForm.printerId.value = document.mainform.printType.value;
	document.billCSVdownloadForm.submit();
}

function enableDisableInputs() {

/*	if ((origBillStatus != "A") || (visitType =='r')
			|| (allowDiscount != 'A')) {
		if (document.mainform.itemDiscPer != null) {
			document.mainform.itemDiscPer.disabled = true;
			document.mainform.itemDiscPerApply.disabled = true;
			document.mainform.itemDiscPerAdd.disabled = true;
		}
	}*/
	if ((visitType =='r')) {
		if (document.getElementById("saveButton")!=null)
			document.getElementById("saveButton").disabled=true;
	}

	if ((origBillStatus == 'C') || (origBillStatus == 'X')) {
		if(document.mainform.billStatus) document.mainform.billStatus.disabled = true;
		if (document.mainform.primaryClaimStatus)
			document.mainform.primaryClaimStatus.readonly = true;
		if (document.mainform.secondaryClaimStatus)
			document.mainform.secondaryClaimStatus.readonly = true;
		if (document.mainform.claimRecdAmount)
			document.mainform.claimRecdAmount.readOnly = true;
	}

	if (billType == 'P' && !billNowTpa){
		// disable only if pay & close is not available -- which means there is no counter.
		if (document.mainform.payClose != undefined && document.mainform.saveButton != undefined)
			document.mainform.saveButton.disabled=true;
	}

	if ( billType == 'M') {
		if (document.mainform.saveButton != undefined) {
			document.mainform.saveButton.disabled=true;
		}
	}
}

function setRowStyle(i) {
	var row = getChargeRow(i);
	var chargeId = getIndexedValue("chargeId", i);
	var flagImgs = row.cells[DATE_COL].getElementsByTagName("img");
	var trashImgs = null;
	if (typeof(TRASH_COL) != 'undefined')
		trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (chargeId.substring(0,1) == "_");
	var cancelled = getIndexedValue("delCharge", i) == 'true';
	var excluded = (getIndexedValue("chargeExcluded", i) == 'Y');
	var partialExcluded = (getIndexedValue("chargeExcluded", i) == 'P');
	var eligible = (hasRewardPointsEligibility && (getIndexedValue("eligible_to_redeem_points", i) == 'Y'));
	var edited = getIndexedValue("edited", i) == 'true';
	var chargeHead = getIndexedValue("chargeHeadId", i);

	/*
	 * Pre-saved state is shown using background colours. The pre-saved states can be:
	 *  - Normal: no background
	 *  - Added: Greenish background
	 *  - Modified: Yellowish background
	 *    (includes cancelled, which is a change in the status attribute)
	 *
	 * Attributes are shown using flags. The only attribute indicated is the cancelled
	 * attribute, using a red flag.
	 *
	 * Possible actions using the trash icon are:
	 *  - Cancel/Delete an item: Normal trash icon.
	 *    (newly added items are deleted, saved items are cancelled)
	 *  - Un-cancel an item: Trash icon with a cross
	 *  - The item cannot be cancelled: Grey trash icon.
	 */

	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else if (excluded) {
		flagSrc = cpath + '/images/blue_flag.gif';
	} else if (partialExcluded) {
		flagSrc = cpath + '/images/blue_flag.gif';
	} else if (eligible) {
		flagSrc = cpath + '/images/green_flag.gif';
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else if (origBillStatus != 'A' && origBillStatus != 'F'){
		trashSrc = cpath + '/icons/delete_disabled.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function isBillItemEditable() {
	var flag=true;
	// disable all item edits if the bill is not open, or if visit type is r/t
	//if ((origBillStatus != 'A') || (visitType =='r') )
	//	 flag=false;
	//if (editBillRights != 'A')
	//	flag= false;
	//if(origBillStatus == 'A' || origBillStatus == 'F' || (visitType !='r')){
//		flag= true;
	//}
	return flag;
}

function isPostedDateEditable(i) {
	if (!isBillItemEditable())
		return false;
	if (billType != 'C' && !billNowTpa)
		return false;
	if (getIndexedValue("delCharge", i) == 'true')
		return false;

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'BIDIS' || chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
         || chargeHead == 'INVITE' || chargeHead == 'INVRET'
         || chargeHead == 'MARPKG' || chargeHead == 'MARPDM'
         || chargeHead == 'MARDRG' || chargeHead == 'OUTDRG') {
		return false;
	}
	return true;
}

function isDeletable(i) {
	if (!isBillItemEditable())
		return false;
	if (cancelElementsInBillRights != 'A')
		return false;
	return true;
}

function isRateEditable(i) {
	if (!isBillItemEditable())
		return false;
	//Editing item rate in bill based on action right and item level inc/decr
	if (allowRateIncr != 'A' && allowRateDcr != 'A'
			&& getIndexedValue("allowRateIncrease", i) == 'false'
			&& getIndexedValue("allowRateDecrease", i) == 'false'){
			return false;
	}else if (allowRateIncr != 'A' && allowRateDcr != 'A'
			&& getIndexedValue("allowRateIncrease", i) == 'true'
			&& getIndexedValue("allowRateDecrease", i) == 'true'){
			return false;
	}else{
		if (allowRateIncr == 'A' && allowRateDcr == 'A'
			&& getIndexedValue("allowRateIncrease", i) == 'false'
			&& getIndexedValue("allowRateDecrease", i) == 'false')
			return false;
	}
	if (getIndexedValue("delCharge", i) == 'true')
		return false;

	return true;
}
function isQtyEditable(i) {
	if (!isBillItemEditable())
		return false;

	if (getIndexedValue("delCharge", i) == 'true')
		return false;
	return true;
}



/*function isDiscountCatgoryApplicable(discountCategory,i){

	if(discountCategory != undefined && ( visitDiscountPlanId > 0 || !discountCategory.disabled )){
		if (getIndexedValue("allowDiscount", i) == 'true')
			return true;
	}
	return false;
}
*/
function isConductedByEditable(i) {
	if (!isBillItemEditable())
		return false;
	if (getIndexedValue("docPaymentId", i) != '')	// is the doctor already paid?
		return false;
	if (getIndexedValue("activityConducted", i) == 'N')		// does it require conduction?
		return false;
	return true;
}

/*function isChargeAmountIncludedEditable(i) {

	if(allowDynaPkgIncExc != 'A')
		return false;

	if ((origBillStatus != 'A') || (visitType =='r') )
		return false;

	if (empty(dynaPackageProcessed) || dynaPackageProcessed =='N')
		return false;

	var packageIdObj = mainform.dynaPkgId;
	if (packageIdObj == null || empty(packageIdObj.value) || packageIdObj.value == 0 ||
				existingDynaPackageId != packageIdObj.value )
		return false;

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'MARPKG' || chargeHead == 'BIDIS' || chargeHead == 'ROF'
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX' || chargeHead == 'PHCMED' || chargeHead == 'PHCRET' ) {
		return false;
	}

	if (getIndexedValue("delCharge", i) == 'true')
		return false;

	return true;
}

function isChargeQuantityIncludedEditable(i) {

	if(allowDynaPkgIncExc != 'A')
		return false;

	if ((origBillStatus != 'A') || (visitType =='r') )
		return false;

	if (empty(dynaPackageProcessed) || dynaPackageProcessed =='N')
		return false;

	var packageIdObj = mainform.dynaPkgId;
	if (packageIdObj == null || empty(packageIdObj.value) || packageIdObj.value == 0 ||
				existingDynaPackageId != packageIdObj.value )
		return false;

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'MARPKG' || chargeHead == 'BIDIS' || chargeHead == 'ROF'
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX' || chargeHead == 'PHCMED' || chargeHead == 'PHCRET' ) {
		return false;
	}

	if (getIndexedValue("delCharge", i) == 'true')
		return false;

	return true;
}
*/
function isClaimEditable(i) {
	// claim is editable only for open bills
	if ((origBillStatus != 'A') || (visitType =='r') || (visitType == 't'))
		return false;
	if (editBillRights != 'A')
		return false;
	if (getIndexedValue("delCharge", i) == 'true')
		return false;
	//Change added for bug # 37140 && 37150

	/* INS 3.0 if (hasPlanCopayLimit && restrictionType == 'N')
		return false; */
	if (getIndexedValue("insClaimable", i) == 'N')
		return false;
	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'BSTAX' || chargeHead == 'CSTAX'
			|| chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
			|| chargeHead == 'MARDRG'  || chargeHead == 'OUTDRG'
			|| chargeHead == 'MARPDM')
		return false;
	if (restrictionType == 'P')
		return false;
	return true;
}

function isItemRemarksEditable(i) {
	// remarks is editable even for finalized/settled bills
	if ((origBillStatus == 'C') || (origBillStatus == 'X') || (visitType =='r'))
		return false;
	if (editBillRights != 'A')
		return false;
	if (getIndexedValue("delCharge", i) == 'true')
		return false;
	return true;
}

function isItemCodeEditable(i) {
	// Rate plan code is editable even for finalized/settled bills only if not Inventory return
	if ((origBillStatus == 'C') || (origBillStatus == 'X') || (visitType =='r') )
		return false;
	if (editBillRights != 'A')
		return false;
	if (getIndexedValue("delCharge", i) == 'true')
		return false;
	return true;
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

/*function validateDiscountWithDynaPackage() {
	if ((origBillStatus == 'A') && (mainform.dynaPkgId != null)
			&& (mainform.dynaPkgId.value != '') && (mainform.dynaPkgId.value != 0)) {
		var ok = confirm(" Warning: Bill has dyna package. \n " +
								"Further discounts are not allowed for included charges. \n " +
				 				"Do you want to proceed ? ");
		if (!ok) {
			return false;
		}
	}
	return true;
}

function onChangeBillDiscPer() {
	var discPer = getAmount(document.mainform.billDiscPer.value);
	var totDiscPaise = totAmtPaise * discPer / 100;
	document.mainform.billDiscAmount.value = formatAmountPaise(totDiscPaise);
}
*/
/*
 * Calculate additional discount based on the Amount column, add it to existing discount
 */
/*function onAddItemDiscPer() {

	if (!validateDiscountWithDynaPackage())
		return false;

	if (!selectedItemsForDiscount()) {
		showMessage("js.billing.billlist.selectcharge.adddiscount");
		return false;
	}
	var discPer = getAmount(document.mainform.itemDiscPer.value);
	if (discPer > 100) {
		showMessage("js.billing.billlist.discountnot.greaterthan100percent");
		return;
	}

	var claimAmtEdited = false;

	for (var i=0;i<getNumCharges();i++) {
		if (getIndexedValue("chargeHeadId",i) == 'BIDIS')
			continue;

		if (!getIndexedFormElement(mainform, "discountCheck",i).checked)
			continue;

		if(getIndexedValue("chargeGroupId",i) == 'RET' || getIndexedValue("chargeGroupId",i) == 'MED') {
			continue;
		}

		var amtObj = getIndexedFormElement(mainform, "amt",i);
		var returnAmtObj = getIndexedFormElement(mainform, "returnAmt",i);
		var discObj = getIndexedFormElement(mainform, "disc", i);
		if (amtObj.value == "") { amtObj.value = 0; }
		if (returnAmtObj.value == "") { returnAmtObj.value = 0; }
		if (discObj.value == "") { discObj.value = 0; }

		var amtPaise = getPaise(amtObj.value);
		var returnAmtPaise = getPaise(returnAmtObj.value);
		var discPaise = getPaise(discObj.value);

		if (discPaise > 0) {
			setIndexedValue("overall_discount_auth_name", i, '');
			setIndexedValue("overall_discount_auth", i, '0');
			setIndexedValue("overall_discount_amt", i, '');
		}

		if(getIndexedValue("chargeGroupId",i) == 'ITE') {
			discPaise = discPaise + ( (amtPaise + returnAmtPaise) * discPer / 100);
		} else {
			discPaise = discPaise + (amtPaise * discPer / 100);
		}

		setNodeText(getChargeRow(i).cells[DISC_COL], formatAmountPaise(discPaise));
		discObj.value = formatAmountPaise(discPaise);
		setIndexedValue("overall_discount_amt", i, formatAmountPaise(discPaise));

		var claim = 0;
		if (editform.eClaimAmt != null)
			claim = getPaise(getIndexedValue("insClaimAmt", i));

		onChangeDiscount(i);

		if (editedAmounts(i, claim))
			claimAmtEdited = true;
	}
	resetTotals(claimAmtEdited, false);
}

function selectedItemsForDiscount() {
	for (var i=0;i<getNumCharges();i++) {
		if (getIndexedFormElement(mainform, "discountCheck",i).checked)
		return true;
	}
	return false;
}
*/
function onChangeFilter(filterObj) {
	var filterGroup = mainform.filterServiceGroup.value;
	var filterHead = mainform.filterChargeHead.value;
	var filterPackage = mainform.filterPackage;

	filterCharges();
	//resetSelectedDiscountItems();
	resetTotals(false, false);

	if (filterGroup != '' || filterHead != '' || (filterPackage != null && filterPackage.value != '') ) {

		if (filterObj) YAHOO.util.Dom.addClass(filterObj, 'filterActive');
		document.getElementById("filterRow").style.display = 'table-row';

	}else {
		if (filterObj) YAHOO.util.Dom.removeClass(filterObj, 'filterActive');
		document.getElementById("filterRow").style.display = 'none';
	}

	//to re select discount plan eligible charges.

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
		if (deleted == 'true' && document.mainform.showCancelled.checked)
			show = false;
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

function editedAmounts(i, claim) {
	var origClaim = 0;

	if (editform.eClaimAmt != null) {
		if (isPharmacyReturns(i)) {
			claim = 0;
			origClaim = 0;
		}else {
			origClaim = getPaise(getIndexedValue('insClaimAmt',i));
		}
	}
	return (origClaim != claim);
}

function setEditedAmounts(i, row, rate, qty, disc, amt, claim, deduction) {
	var table = YAHOO.util.Dom.getAncestorByTagName(row, 'table');
	setIndexedValue("rate", i, formatAmountValue(rate));
	setNodeText(row.cells[RATE_COL], formatAmountValue(rate));
	setIndexedValue("qty",i, formatAmountValue(qty, true));
	setNodeText(row.cells[QTY_COL], formatAmountValue(qty, true));
	setIndexedValue("disc",i, formatAmountValue(disc));
//	setIndexedValue("overall_discount_amt", i, formatAmountValue(disc));
//	setNodeText(row.cells[DISC_COL], formatAmountValue(disc), 10, getDiscountTitle(i));
	setIndexedValue("amt",i, formatAmountValue(amt));
	setNodeText(row.cells[AMT_COL], formatAmountValue(amt));

	if (editform.eClaimAmt != null) {
		if (isPharmacyReturns(i)) {
			claim = 0;
			deduction = 0;
		}
		if (isPharmacySales(i)) {
			// hidden and DB value is always original claim amount
			// but display is net claim (ie, after adding return Claim Amt).
			var amtPaise = getPaise(amt);
			var claimPaise = getPaise(claim);
			var returnAmtPaise = getIndexedPaise("returnAmt",i);
			var returnClaimPaise = getIndexedPaise("returnInsuranceClaimAmt",i);

			setNodeText(row.cells[CLAIM_COL], formatAmountPaise(claimPaise + returnClaimPaise));
			setNodeText(row.cells[DED_COL], formatAmountPaise(
					(amtPaise+returnAmtPaise) - (claimPaise+returnClaimPaise) ));
		} else {
			setNodeText(row.cells[CLAIM_COL], formatAmountValue(claim));
			setNodeText(row.cells[DED_COL], formatAmountValue(deduction));
		}
		setIndexedValue("insClaimAmt", i, formatAmountValue(claim));
		setIndexedValue("priInsClaimAmt", i, formatAmountValue(claim));
		setIndexedValue("insDeductionAmt", i, formatAmountValue(deduction));
	}

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'BIDIS' || chargeHead == 'ROF' || chargeHead == 'CSTAX' || chargeHead == 'BSTAX') {
		var dynaPkgId = (document.mainform.dynaPkgId != null) ? (document.mainform.dynaPkgId.value) : 0;
		if (!empty(dynaPkgId) && dynaPkgId != 0)
			setIndexedValue("chargeExcluded", i, "Y");
		else
			setIndexedValue("chargeExcluded", i, "N");
	}

	setIndexedValue("edited", i, 'true');
	setRowStyle(i);

	chargesEdited++;
}


function setSecondaryPlanAmounts(i, editform, row ,amt,primaryClaim,deduction,secondaryClaim) {

	if (isPharmacyReturns(i)) {
			primaryClaim = 0;
			secondaryClaim = 0;
			deduction = 0;
		}
	if (editform.eSecClaimAmt != null) {
		if (isPharmacySales(i)) {
			// hidden and DB value is always original claim amount
			// but display is net claim (ie, after adding return Claim Amt).
			var amtPaise = getPaise(amt);
			var priClaimPaise = getPaise(primaryClaim);
			var secClaimPaise = getPaise(secondaryClaim);
			var returnAmtPaise = getIndexedPaise("returnAmt",i);
			var returnClaimPaise = getIndexedPaise("returnInsuranceClaimAmt",i);

			setNodeText(row.cells[CLAIM_COL], formatAmountPaise(priClaimPaise + returnClaimPaise));
			setNodeText(row.cells[SEC_CLAIM_COL], formatAmountPaise(secClaimPaise + returnClaimPaise));
			setNodeText(row.cells[DED_COL], formatAmountPaise(
					(amtPaise+returnAmtPaise) - (priClaimPaise+returnClaimPaise+secClaimPaise) ));
		} else {
			setNodeText(row.cells[CLAIM_COL], formatAmountValue(primaryClaim));
			setNodeText(row.cells[SEC_CLAIM_COL], formatAmountValue(secondaryClaim));
			setNodeText(row.cells[DED_COL], formatAmountValue(deduction));
		}

	setIndexedValue("insClaimAmt", i, formatAmountValue(primaryClaim));
	setIndexedValue("priInsClaimAmt", i, formatAmountValue(primaryClaim));
	setIndexedValue("secInsClaimAmt", i, formatAmountValue(secondaryClaim));
	setIndexedValue("insDeductionAmt", i, formatAmountValue(deduction));
	setIndexedValue("priInsClaimAmt", i, formatAmountValue(primaryClaim));

//	var secPreAuthId = editform.eSecPreAuthId.value;
//	var secPreAuthMode = editform.eSecPreAuthModeId.value;

//	setNodeText(row.cells[SEC_PRE_AUTH_COL],secPreAuthId);
//	setIndexedValue("secPreAuthId", i, secPreAuthId);
//	setIndexedValue("secPreAuthModeId", i, secPreAuthMode);

	}
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(mainform, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


/*
 * Add ONE charge row to the table, always adds at the end of the list of charges,
 * and returns the ID of the row that was added.
 */
function addToTable(group, head, descId, desc, rate, qty, units, actRatePlanItemCode) {

	var charge = { chargeGroup: group, chargeHead: head, actDepartmentId: null,
		actDescriptionId: descId, actDescription: desc,
		actRate: rate, actQuantity: qty, actUnit: units, discount: 0,
		actRemarks: "", payeeDoctorId: "", presDoctorId: "", actItemCode: "",
		actRatePlanItemCode: actRatePlanItemCode, orderNumber: 0,
		allowRateVariation: true, discauthId: "",
		conducting_doc_mandatory: false, chargeExcluded: "N", consultation_type_id: 0,op_id:"",
		insuranceCategoryId: -1, insuranceClaimAmount: 0, codeType: "",
		eligible_to_redeem_points: "N", redemption_cap_percent: 0};

	return addChargeToTable(charge, null, null);
}

function addChargeToTable(charge, refId, preAuthId) {
	addChargeToTable(charge, refId, preAuthId, "", "","",null,null,null, null);
}

function addChargeToTable(charge, refId, preAuthId, fromDate, toDate,multiVisitPackageId, preAuthMode, secPreAuthId, secPreAuthMode, presDate) {
	var chargeHead = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead);
	var headText = chargeHead.CHARGEHEAD_NAME;
	var insPayable = chargeHead.INSURANCE_PAYABLE;
	var insClaimTaxable = chargeHead.CLAIM_SERVICE_TAX_APPLICABLE;
	var serviceChrgApplicable = chargeHead.SERVICE_CHARGE_APPLICABLE;
	var groupText = findInList(jChargeGroups, "CHARGEGROUP_ID", charge.chargeGroup).CHARGEGROUP_NAME;

	var rateStr = formatAmountValue(charge.actRate);
	var discStr = formatAmountValue(charge.discount);
	var amtStr = formatAmountPaise(getPaise(charge.actRate)*charge.actQuantity - getPaise(charge.discount));
	if (refId == null) refId = "";

	if (charge.actRemarks == undefined) charge.actRemarks = "";

	var packageId = (mainform.dynaPkgId != null) ? mainform.dynaPkgId.value : "";
	var id = getNumCharges();
   var table = document.getElementById("chargesTable");
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   row.id="chRow"+id;

	var cell = null;
	var inp = null;

	var postedDate;
	var postedTime;

	if (billType == 'P' && !billNowTpa && finalizedDate != '') {
		postedDate = finalizedDate;
		postedTime = finalizedTime;
	} else {//order date should be posted date if its not a plain bill now bill
		if( ( typeof(presDate) == 'undefined' || presDate == '' )  || ( billType == 'P' && !billNowTpa ) ){
			postedDate = curDate;
			postedTime = curTime;
		}else{
			postedDate = presDate != '' ? presDate.split(" ")[0] : curDate;
			postedTime = presDate != '' ? presDate.split(" ")[1] : curTime;
		}
	}

	setNodeText(row.cells[DATE_COL], postedDate);
	setNodeText(row.cells[ORDER_COL], charge.orderNumber);
	setNodeText(row.cells[HEAD_COL], headText);
	setNodeText(row.cells[CODE_COL], charge.actRatePlanItemCode, 10);
	setNodeText(row.cells[DESC_COL], charge.actDescription, 30, charge.actDescription);
	setNodeText(row.cells[REMARKS_COL], charge.actRemarks, 16);
	setNodeText(row.cells[RATE_COL], rateStr);
	setNodeText(row.cells[QTY_COL], charge.actQuantity.toString());
	setNodeText(row.cells[UNITS_COL], charge.actUnit);
	setNodeText(row.cells[DISC_COL], discStr);
	setNodeText(row.cells[AMT_COL], amtStr);
	setHiddenValue(id, "packageId", multiVisitPackageId);
	setHiddenValue(id, "postedDate", postedDate);
	setHiddenValue(id, "postedTime", postedTime);
	setHiddenValue(id, "orderNumber", charge.orderNumber);
	setHiddenValue(id, "chargeHeadName", headText);
	setHiddenValue(id, "chargeHeadId", charge.chargeHead);
	setHiddenValue(id, "chargeGroupName", groupText);
	setHiddenValue(id, "chargeGroupId", charge.chargeGroup);
	setHiddenValue(id, "chargeId", "_" + id);
	setHiddenValue(id, "chargeRef", "_" + refId);
	setHiddenValue(id, "departmentId", charge.actDepartmentId);
	setHiddenValue(id, "hasActivity", "false");
	setHiddenValue(id, "description", charge.actDescription);
	setHiddenValue(id, "descriptionId", charge.actDescriptionId);
	setHiddenValue(id, "actItemCode", charge.actItemCode);
	setHiddenValue(id, "actRatePlanItemCode", charge.actRatePlanItemCode);
	setHiddenValue(id, "codeType", charge.codeType);
	setHiddenValue(id, "payeeDocId", charge.payeeDoctorId);
	setHiddenValue(id, "prescDocId", charge.prescribingDrId);
	setHiddenValue(id, "remarks", charge.actRemarks);
	setHiddenValue(id, "originalRate", charge.actRate.toString());
	setHiddenValue(id, "rate", charge.actRate.toString());
	setHiddenValue(id, "savedRate", charge.actRate.toString());
	setHiddenValue(id, "qty", charge.actQuantity.toString());
	setHiddenValue(id, "units", charge.actUnit);
	setHiddenValue(id, "disc", charge.discount.toString());
	setHiddenValue(id, "oldDisc", charge.discount.toString());
	setHiddenValue(id, "amt", amtStr.toString());
	setHiddenValue(id, "delCharge", "false");
	//setHiddenValue(id, "allowDiscount", charge.allowDiscount);
	setHiddenValue(id, "allowRateVariation", charge.allowRateVariation);
	setHiddenValue(id, "discount_auth_dr", 0);
	setHiddenValue(id, "discount_auth_pres_dr", 0);
	setHiddenValue(id, "discount_auth_ref", 0);
	setHiddenValue(id, "discount_auth_hosp", 0);
	setHiddenValue(id, "consultation_type_id", charge.consultation_type_id);
	setHiddenValue(id, "op_id", charge.op_id);
	setHiddenValue(id, "insuranceCategoryId", charge.insuranceCategoryId);
	setHiddenValue(id, "service_sub_group_id", charge.serviceSubGroupId);
	setHiddenValue(id, "preAuthId", preAuthId);
	setHiddenValue(id, "preAuthModeId", preAuthMode);
	setHiddenValue(id, "secPreAuthId", secPreAuthId);
	setHiddenValue(id, "secPreAuthModeId", secPreAuthMode);
	setHiddenValue(id, "firstOfCategory", charge.firstOfCategory);
	setHiddenValue(id, "from_date", fromDate);
	setHiddenValue(id, "to_date", toDate);
	setHiddenValue(id, "allowRateIncrease", charge.allowRateIncrease);
	setHiddenValue(id, "allowRateDecrease", charge.allowRateDecrease);
	setHiddenValue(id, "serviceChrgApplicable", serviceChrgApplicable);
	setHiddenValue(id, "isClaimLocked", charge.isClaimLocked);

	setHiddenValue(id, "eligible_to_redeem_points", charge.eligible_to_redeem_points);
	setHiddenValue(id, "redemption_cap_percent", charge.redemption_cap_percent);
	setHiddenValue(id, "redeemed_points", 0);
	setHiddenValue(id, "max_redeemable_points", 0);

	// required for filtering, mainly. This is not saved in the db.
	var subGrp = findInList(allServiceSubgroupsList,'service_sub_group_id', charge.serviceSubGroupId);
	if (subGrp != null)
		setHiddenValue(id, "service_group_id", subGrp.service_group_id);

	setHiddenValue(id, "conducting_doc_mandatory",
			charge.conducting_doc_mandatory == null || charge.conducting_doc_mandatory == '' ? 'N' :
			charge.conducting_doc_mandatory);

	/*if (charge.discount > 0) {
		if (charge.chargeHead != "BIDIS") {
			setHiddenValue(id, "overall_discount_auth_name", "Rate Plan Discount");
			setHiddenValue(id, "overall_discount_auth", -1);
			setHiddenValue(id, "old_overall_discount_auth", -1);
		}
		if (charge.chargeHead == "BIDIS" && charge.discauthId != null && charge.discauthId != '') {
			var discauthorizer = findInList(jDiscountAuthorizers, 'disc_auth_id', charge.discauthId);
			setHiddenValue(id, "overall_discount_auth_name", discauthorizer.disc_auth_name);
			setHiddenValue(id, "overall_discount_auth", charge.discauthId);
			setHiddenValue(id, "old_overall_discount_auth", charge.discauthId);
		}
		setHiddenValue(id, "overall_discount_amt", charge.discount);
	} else {*/
		setHiddenValue(id, "overall_discount_auth_name", '');
		setHiddenValue(id, "overall_discount_auth", 0);
		setHiddenValue(id, "overall_discount_amt", 0);
//	}

	if (tpaBill) {
		//setHiddenValue(id, "insClaimAmt", charge.insuranceClaimAmount.toString());
		setHiddenValue(id, "insClaimable", insPayable);
		setHiddenValue(id, "insClaimTaxable", insClaimTaxable);
	}

	if (charge.chargeHead == 'MARPKG') {
		// package margin is always within the package
		setHiddenValue(id, "chargeExcluded", "N");
		setHiddenValue(id, "amount_included", charge.amount);
		setHiddenValue(id, "qty_included", 0);

	} else if (charge.chargeHead == 'ROF' || charge.chargeHead == 'BIDIS'
				|| charge.chargeHead == 'CSTAX' || charge.chargeHead == 'BSTAX') {
		// round offs, service tax and discounts are usually outside the package
		(!empty(packageId) && packageId != 0)
			? setHiddenValue(id, "chargeExcluded", "Y")
			: setHiddenValue(id, "chargeExcluded", "N");

		setHiddenValue(id, "amount_included", 0);
		setHiddenValue(id, "qty_included", 0);
	} else {
		// use supplied value
		(!empty(packageId) && packageId != 0)
			? setHiddenValue(id, "chargeExcluded", "Y")
			: setHiddenValue(id, "chargeExcluded", "N");

		// TODO: Need to set if automatic update of package processing to be done.
		setHiddenValue(id, "amount_included", 0);
		setHiddenValue(id, "qty_included", 0);
	}

	setRowStyle(id);

	row.className = "added";

	var claimAmtEdited = false;

	var visitID = document.getElementById('visitId').value;
	chargesAdded++;

/*	var discountCategory = document.mainform.discountCategory;

	if(discountCategory != undefined && discountCategory.value != 0 && discountCategory.value != '') {
		//selectDiscountCategory();
		var itemDiscountPer = 0;
		if(document.mainform.itemDiscPer != undefined)
			itemDiscountPer = document.mainform.itemDiscPer.value;

		// check for exclusions from plan
		for(var i=0; i<jPolicyNameList.length; i++) {
			var planMap = jPolicyNameList[i];
			var planCatId = planMap.insurance_category_id;
			var planPatType = planMap.patient_type;

			if(planCatId == charge.insuranceCategoryId && planPatType == visitType) {
				if(tpaBill) {
					var is_category_payable = planMap.category_payable;
					if(is_category_payable == 'Y') {
						applyItemDiscPer(itemDiscountPer,id);
					}
				} else {
					applyItemDiscPer(itemDiscountPer,id);
				}
			}
		}
	}
*/
	if(tpaBill) {
		var insPayable = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead).INSURANCE_PAYABLE;
	//	getBillChargeClaims(visitID, document.mainform);

		if(null == planList || planList.length <= 0) {
			if(insPayable == 'Y') {
				setNodeText(row.cells[CLAIM_COL], formatAmountPaise(getPaise(charge.amount)));
				setHiddenValue(id, "insClaimAmt", formatAmountPaise(getPaise(charge.amount)));
			}
		}

		if(getPaise(getIndexedValue("insClaimAmt", id)) !=0 ) claimAmtEdited = true;
	}

	resetTotals(claimAmtEdited, false);

	// Calculate sponsor claim amount if Claim Service Tax is added.
	if (charge.chargeHead == 'CSTAX') {
		var claim = getPaise(getIndexedValue("insClaimAmt", id));
		if (claim != 0) resetTotals(true, false);
	}

	return id;
}

function setPatientAmtsForRowDel(delRow) {
	var table = YAHOO.util.Dom.getAncestorByTagName(delRow, 'table');
	var numRows = getNumCharges();
	for(var j=0; j<getNumCharges(); j++){
		var tablRow = table.rows[j];
		var patAmtToBeDltd = parseInt(delRow.cells[DED_COL].textContent);
		if(delRow !=  tablRow && patAmtToBeDltd==getActualClaim(delRow) && getIndexedFormElement(mainform, "delCharge", i).value!= "true") {
			var currTablCategory = getElementByName(tablRow, "insuranceCategoryId").value;
			var currTablPatAmt = parseInt(tablRow.cells[DED_COL].textContent);
			var categoryToBedeltd = getElementByName(delRow, 'insuranceCategoryId').value;
			if(currTablCategory == categoryToBedeltd) {
				var chargeHead = getElementByName(tablRow, "chargeHeadId").value;
				var insuranceCategoryId = getElementByName(tablRow, "insuranceCategoryId").value;
				var newAmtPaise =  getPaise(parseInt(tablRow.cells[AMT_COL].textContent));
				var newDiscPaise =  getPaise(parseInt(tablRow.cells[DISC_COL].textContent));
				var newClaimPaise = getClaimAmount(chargeHead, newAmtPaise,newDiscPaise,insuranceCategoryId, true);
				var newPatientPaise = newAmtPaise - newClaimPaise;
				getElementByName(tablRow,"insClaimAmt").value = formatAmountPaise(newClaimPaise);
				getElementByName(tablRow,"insDeductionAmt").value = formatAmountPaise(newPatientPaise);
				setNodeText(tablRow.cells[CLAIM_COL], formatAmountPaise(newClaimPaise));
				setNodeText(tablRow.cells[DED_COL], formatAmountPaise(newPatientPaise));
				break;
			}
		}
	}
}

function getClaimAmount(head, amtPaise, newDiscPaise , insuranceCategoryId) {
	getClaimAmount(head, amtPaise, insuranceCategoryId, true);
}

function getClaimAmount(head, amtPaise, newDiscPaise,  insuranceCategoryId, firstOfCategory) {
	var insPayable = findInList(jChargeHeads, "CHARGEHEAD_ID", head).INSURANCE_PAYABLE;
	if (insPayable != 'Y')
		return 0;

	var ipOpApplicable = (visitType == 'i') ? "ip_applicable" : "op_applicable";

	var planDetails = null;
	if (jPolicyNameList!=null && jPolicyNameList.length>0 ) {
		planDetails = findInList3(jPolicyNameList, "insurance_category_id",
				insuranceCategoryId, "patient_type", visitType, ipOpApplicable, "Y" );
	}

	if (planDetails == null) {
		// no full amount is claimable
		return amtPaise;
	}

	var patientCatDednPaise = getPaise(planDetails.patient_amount_per_category);
	var patientDednPaise = getPaise(planDetails.patient_amount);
	var patientPer = planDetails.insurance_payable == 'Y'?planDetails.patient_percent:100;
	var patientCapPaise = getPaise(planDetails.patient_amount_cap);
	var is_copay_pc_on_post_discnt_amt = planDetails.is_copay_pc_on_post_discnt_amt;
	var chgPercAmt = is_copay_pc_on_post_discnt_amt == 'N'?  amtPaise+newDiscPaise : amtPaise;

	var patientPortionPaise =  patientDednPaise+ (chgPercAmt*patientPer/100);
	if(firstOfCategory == true){
		patientPortionPaise += patientCatDednPaise;
	}

	if (head != 'BIDIS' && patientCapPaise != null && patientCapPaise != 0) {
		if (patientPortionPaise > patientCapPaise)
			patientPortionPaise = patientCapPaise;
	}

	var claimAmtPaise = amtPaise - patientPortionPaise;

	return claimAmtPaise;
}

function onChangeBillStatus() {
	var billStatus = document.mainform.billStatus.value;

	var paymentStatusObj = document.mainform.paymentStatus;
	var priClaimStatusObj = document.mainform.primaryClaimStatus;
	var secClaimStatusObj = document.mainform.secondaryClaimStatus;

	var priClaimAmountObj = document.mainform.primaryTotalClaim;
	var secClaimAmountObj = document.mainform.secondaryTotalClaim;

	if (billStatus == 'C') {
		// also set payment status as paid and claim status as Received.
		setSelectedIndex(paymentStatusObj, 'P');
		paymentStatusObj.disabled = true;
		onChangePaymentStatus();

		if (priClaimStatusObj != null) {
			setSelectedIndex(priClaimStatusObj, 'R');
			priClaimStatusObj.readonly = true;
			if (priClaimAmountObj != null)
				priClaimAmountObj.readOnly = true;
			onChangeClaimStatus();
		}

		if (secClaimStatusObj != null) {
			setSelectedIndex(secClaimStatusObj, 'R');
			secClaimStatusObj.readonly = true;
			if (secClaimAmountObj != null)
				secClaimAmountObj.readOnly = true;
			onChangeClaimStatus();
		}

	} else {
		paymentStatusObj.disabled = false;
		if (priClaimStatusObj != null) {
			if (billStatus == 'A') {
				setSelectedIndex(priClaimStatusObj, 'O');
				priClaimStatusObj.readonly = true;
				if (priClaimAmountObj != null)
					priClaimAmountObj.readOnly = false;
			} else {
				priClaimStatusObj.readonly = false;
				if (priClaimAmountObj != null)
					priClaimAmountObj.readOnly = true;
			}
		}

		if (secClaimStatusObj != null) {
			if (billStatus == 'A') {
				setSelectedIndex(secClaimStatusObj, 'O');
				secClaimStatusObj.readonly = true;
				if (secClaimAmountObj != null)
					secClaimAmountObj.readOnly = false;
			} else {
				secClaimStatusObj.readonly = false;
				if (secClaimAmountObj != null)
					secClaimAmountObj.readOnly = true;
			}
		}
	}

	// Back date Allowed
	if (allowBackDate == 'A' || roleId == 1 || roleId == 2) {
		if (origBillStatus == 'A' && (billStatus == 'F' || billStatus == 'C')) {
			document.mainform.finalizedDate.readOnly = false;
			document.mainform.finalizedTime.readOnly = false;
			if (finalizedDate == '') {
				var curDate = (gServerNow != null) ? gServerNow : new Date();
				document.mainform.finalizedDate.value = formatDate(curDate, "ddmmyyyy", "-");
				document.mainform.finalizedTime.value = formatTime(curDate, false);
			}
		} else {
			document.mainform.finalizedDate.readOnly = true;
			document.mainform.finalizedDate.value = finalizedDate;
			document.mainform.finalizedTime.readOnly = true;
			document.mainform.finalizedTime.value = finalizedTime;
		}
	}
	return true;
}

function onChangePaymentStatus() {

	var paymentStatus = document.mainform.paymentStatus.value;
	var okToDischarge = document.mainform._okToDischarge;

	if (dischargeType == 'adt' && okToDischarge != null && !okToDischarge.disabled) {
		if (paymentStatus == 'P' &&
				pendingEquipmentFinalization == 'Finalized' && pendingBedFinalization == 'Finalized') {
			okToDischarge.checked = true;
			setDischargeVars(true);
		} else if (paymentStatus == 'U') {
			okToDischarge.checked = false;
			setDischargeVars(true);
		}
	}

	if ((paymentStatus == 'P') && (document.mainform.billStatus.value == 'F')) {
		if (document.mainform.primaryClaimStatus != null && document.mainform.primaryClaimStatus.value == 'R'
				&& ( document.mainform.secondaryClaimStatus == null
				 || (document.mainform.secondaryClaimStatus != null
				 		&& document.mainform.secondaryClaimStatus.value == 'R'))) {
			var ok = confirm("Bill is finalized and Claim Status is Received. The bill can be closed\n" +
					"Do you want to close the bill?");
			if (ok) {
				setSelectedIndex(document.mainform.billStatus, 'C');
			}
		}
	}
}

function onChangeClaimStatus() {
	if (document.mainform.primaryClaimStatus.value == 'R'
			&& ( document.mainform.secondaryClaimStatus == null
				 || (document.mainform.secondaryClaimStatus != null
				 		&& document.mainform.secondaryClaimStatus.value == 'R'))) {
		// if claim status is set Received, and bill is finalized and payment status is Paid,
		//ask the user if they want to close the bill.
		if (document.mainform.billStatus.value == 'F' && document.mainform.paymentStatus.value == 'P') {
			var ok = confirm("Bill is finalized and Payment status is Paid. The bill can be closed\n" +
					"Do you want to close the bill?");
			if (ok) {
				setSelectedIndex(document.mainform.billStatus, 'C');
			}
		}
	}
}


/*
 * Construct the title for the discounts, describing the split of discounts
 */
/*function getDiscountTitle(i) {
	var title = "";

	var discObj = getIndexedFormElement(mainform, "disc", i);
	if (getPaise(discObj.value) == 0)
		return title;

	if ((getIndexedValue('overall_discount_auth',i) != 0) && (getIndexedValue('overall_discount_amt',i)>0)) {
		title = 'Overall discount given by ' + getIndexedValue('overall_discount_auth_name',i) +
			': ' + getIndexedValue('overall_discount_amt',i);
	} else {
		if ((getIndexedValue('discount_auth_dr',i) != 0) && (getIndexedValue('dr_discount_amt',i)>0)) {
			title = 'Conducting doctor discount given by '
				+ getIndexedValue('discount_auth_dr_name',i) + ': ' + getIndexedValue('dr_discount_amt',i);
		}

		if ((getIndexedValue('discount_auth_pres_dr',i) != 0) && (getIndexedValue('pres_dr_discount_amt',i)>0)) {
			title = title + '; Prescribing doctor discount given by '
				+ getIndexedValue('discount_auth_pres_dr_name',i) + ': '
				+ getIndexedValue('pres_dr_discount_amt',i);
		}

		if ((getIndexedValue('discount_auth_ref',i) != 0) && (getIndexedValue('ref_discount_amt',i)>0)) {
			title = title + '; Referal discount given by '
				+ getIndexedValue('discount_auth_ref_name',i) + ': '+getIndexedValue('ref_discount_amt',i);
		}

		if ((getIndexedValue('discount_auth_hosp',i) != 0) && (getIndexedValue('hosp_discount_amt',i)>0)) {
			title = title + '; Hospital discount given by '
				+ getIndexedValue('discount_auth_hosp_name',i) + ': '+getIndexedValue('hosp_discount_amt',i);
		}
	}
	return title;
}

function setAllDiscountTitles() {
	var numCharges = getNumCharges();
	for (var i=0; i<numCharges; i++) {
		var row = getChargeRow(i);
		var labelNode = row.cells[DISC_COL].getElementsByTagName("label")[0];
		var title = getDiscountTitle(i);
		if (title != '') {
			labelNode.setAttribute("title", getDiscountTitle(i));
		}
	}
}


function isSplitDiscount(i) {
	if (getIndexedValue("discount_auth_dr", i) == 0 &&
			getIndexedValue("discount_auth_pres_dr", i) == 0 &&
			getIndexedValue("discount_auth_ref", i) == 0 &&
			getIndexedValue("discount_auth_hosp", i) == 0 ) {
		return false;
	} else {
		return true;
	}
}
*/
function getNumCharges() {
	// header, add row, hidden template row: totally 3 extra
	return document.getElementById("chargesTable").rows.length-2;
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

function calculateRoundOff() {
	var roundOffRowId = 0;

	for (var i=0;i<getNumCharges();i++) {
		if (getIndexedFormElement(mainform, "chargeHeadId", i).value == 'ROF') {
			roundOffRowId = i;
			break;
		}
	}

	var rofRowId = 0;

	if (roundOffRowId == 0 && document.mainform.roundOff.checked == true) {
		rofRowId = addToTable("DIS", "ROF", null, "", 0, 1, "");
		var rowId = getNumCharges()-1;
		var row = getChargeRow(rowId);
		var img = document.createElement("img");
		img.setAttribute("src", cpath + "/icons/Edit1.png");
		img.setAttribute("class", "button");
		for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
			row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
		}
		row.cells[EDIT_COL].appendChild(img);
	}

	if (roundOffRowId > 0) {
		rofRowId = roundOffRowId;
		cancelCharge(getIndexedFormElement(mainform, "chargeHeadId", roundOffRowId));
	}

	resetTotals(false, false);

	var claim = 0;
	if (editform.eClaimAmt != null)
		claim = getPaise(getIndexedValue("insClaimAmt", rofRowId));
	if (claim != 0) resetTotals(true, false);
}

function getNoOfDaysOfStay() {
	var curDate = (gServerNow != null) ? gServerNow : new Date();
    var finalizedDtTime  = null;
    if (!empty(finalizedDate)) {
    	finalizedDtTime  = getDateTime(finalizedDate, finalizedTime);
    }
    if (empty(finalizedDtTime)) finalizedDtTime = curDate;

    var openDtTime  = getDateTime(openDate, openTime);
    var noOfDays = daysDiff(openDtTime, finalizedDtTime);

    noOfDays = parseInt(noOfDays);
    noOfDays = (noOfDays == 0 && isTpa) ? 1 : noOfDays;

	// if (noOfHours > 0) noOfDays = noOfDays + 1;

	return noOfDays;
}

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

function getNationalSponsorAmount(totInsAmtPaise, noOfDays, sponsorId) {
	var claimAmtPaise = 0;
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

	if (!empty(visitType) && visitType == 'i') {
		if (!empty(isPrimaryBill) && isPrimaryBill == 'Y') {
			if (empty(perDayReimbursementPaise) || perDayReimbursementPaise == 0) {
				claimAmtPaise = ((totInsAmtPaise - perVisitCoPayIPPaise) > 0) ? (totInsAmtPaise - perVisitCoPayIPPaise) : totInsAmtPaise ;
				//claimAmtPaise = totInsAmtPaise - perVisitCoPayIPPaise;
			}else {
				var overallClaimAmtPaise = (perDayReimbursementPaise * noOfDays) - perVisitCoPayIPPaise;
				claimAmtPaise = (totInsAmtPaise <= overallClaimAmtPaise) ? totInsAmtPaise :
											(overallClaimAmtPaise > 0 ? overallClaimAmtPaise : 0);
			}
		}else {
			claimAmtPaise = ((totInsAmtPaise - perVisitCoPayIPPaise) > 0) ? (totInsAmtPaise - perVisitCoPayIPPaise) : totInsAmtPaise ;
		}
	}else {
		if ((!empty(isPrimaryBill) && isPrimaryBill == 'Y')
				 || (parseInt(visitTpaBills) == 1) || (parseInt(visitTpaBills) > 1 &&
						((!empty(firstTpaBill) && firstTpaBill == document.mainform.billNo.value)))) {

			claimAmtPaise = ((totInsAmtPaise - perVisitCoPayOPPaise) > 0) ? (totInsAmtPaise - perVisitCoPayOPPaise) : totInsAmtPaise ;
		}else {
			claimAmtPaise = totInsAmtPaise;
		}
	}
	return claimAmtPaise;
}

function getDeductionAmount(sponsorId) {
	var deductionPaise = 0;
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
	if (opType != 'F' && opType != 'D') {
		if ((!empty(isPrimaryBill) && isPrimaryBill == 'Y')
				 || (parseInt(visitTpaBills) == 1) || (parseInt(visitTpaBills) > 1 &&
						((!empty(firstTpaBill) && firstTpaBill == document.mainform.billNo.value)))) {

			if (!empty(visitType) && visitType == 'i') {
				deductionPaise = perVisitCoPayIPPaise;
			}else {
				deductionPaise = perVisitCoPayOPPaise;
			}
		}
	}
	return deductionPaise;
}

function isPerDayReimApplicable(sponsorId) {
	var applicable = false;
	var natSpnsr;
	if(sponsorsMap.sponsorId) {
		natSpnsr = sponsorsMap.sponsorId;
	} else {
		natSpnsr = getSponsorDetails(sponsorId);
		sponsorsMap.sponsorId = natSpnsr;
	}
	var perDayReimbursementPaise = !empty(natSpnsr) && !empty(natSpnsr.per_day_rate) ? getPaise(natSpnsr.per_day_rate) : 0;
	if (opType != 'F' && opType != 'D') {
		if ((!empty(isPrimaryBill) && isPrimaryBill == 'Y')
				 || (parseInt(visitTpaBills) == 1) || (parseInt(visitTpaBills) > 1 &&
						((!empty(firstTpaBill) && firstTpaBill == document.mainform.billNo.value)))) {
			if (!empty(visitType) && visitType == 'i') {
				applicable = (perDayReimbursementPaise != 0);
			}
		}
	}
	return applicable;
}

// Recalculate the sponsor amounts only if the claim amounts are edited.
function setTotalClaimAmounts(totInsPaise) {

	var deductionPaise = 0;
	if (document.getElementById("insuranceDeduction") != null)
		deductionPaise = getElementPaise(document.getElementById("insuranceDeduction"));

	var priApprovalPaise = 0;
	if (document.getElementById("primaryApprovalAmount") != null)
		priApprovalPaise = getElementPaise(document.getElementById("primaryApprovalAmount"));

	var secApprovalPaise = 0;
	if (document.getElementById("secondaryApprovalAmount") != null)
		secApprovalPaise = getElementPaise(document.getElementById("secondaryApprovalAmount"));

	var noOfDays = getNoOfDaysOfStay();

	/**
		If sponsor type is national, the initial sponsor amount is calculated as:
	      If per-day reimbursement is 0 or null:
	            total claim - per visit co-pay
	      If per-day reimbursement is a valid value:
	            minimum of (total claim) OR (number of days * per-day reimbursement - per visit co-pay)
	      If sponsor type is corporate or insurance, then the initial sponsor amount is = total claim

		An example of National sponsor calculations when total_claim = 4000,
		co-pay = 100, per-day reimbursement is 1000, and 3 days of stay:

	    number of days * per-day = 3000
	    After reducing co-pay, this is 3000 - 100 = 2900
	    Minimum of total claim and 2900 is 2900
	    Thus, sponsor amount is 2900

		An example of National sponsor calculations
		when total_claim = 4000, co-pay = 100, per-day reimbursement is 0, and 3 days of stay:

		total claim - per visit co-pay = 4000 - 100 = 3900
	*/

	var priDeductionPaise = getDeductionAmount(priSponsorId);
	var secDeductionPaise = getDeductionAmount(secSponsorId);

	var priPerDayApplicable = isPerDayReimApplicable(priSponsorId);
	var secPerDayApplicable = isPerDayReimApplicable(secSponsorId);

	var priClaimPaise = 0;
	var secClaimPaise = 0;

	// Primary & Secondary Sponsor may be of National or not National.
	// Check for National
	if ((!empty(priSponsorType) && priSponsorType == 'N') || (!empty(secSponsorType) && secSponsorType == 'N')) {
		if (!empty(priSponsorType) && priSponsorType == 'N') {

			// If not follow up then calculate the sponsor amount as per rules mentioned above.
			if (opType != 'F' && opType != 'D') {
				priClaimPaise = getNationalSponsorAmount(totInsPaise, noOfDays, priSponsorId);
			}else {
				priClaimPaise = totInsPaise;
			}

			if (!priPerDayApplicable && priClaimPaise > 0)
				priClaimPaise = priClaimPaise + priDeductionPaise;

			// Miminum of approval and sponsor amount is considered.
			priClaimPaise = (priClaimPaise <= priApprovalPaise || priApprovalPaise == 0) ? priClaimPaise : priApprovalPaise;

			if (!empty(secSponsorType) && secSponsorType == 'N') {
				// If not follow up then calculate the sponsor amount as per rules mentioned above.
				if (opType != 'F' && opType != 'D') {
					secClaimPaise = getNationalSponsorAmount((totInsPaise - priClaimPaise), noOfDays, secSponsorId);
				}else {
					secClaimPaise = ((totInsPaise - priClaimPaise) > 0) ? (totInsPaise - priClaimPaise) : 0;
				}
			}else {
				secClaimPaise = ((totInsPaise - priClaimPaise) > 0) ? (totInsPaise - priClaimPaise) : 0;
			}

			if (visitType == 'i' && !secPerDayApplicable && secClaimPaise > 0)
				secClaimPaise = secClaimPaise + secDeductionPaise;

			// Miminum of approval and sponsor amount is considered.
			secClaimPaise = (secClaimPaise <= secApprovalPaise || secApprovalPaise == 0) ? secClaimPaise : secApprovalPaise;

		}else if (!empty(secSponsorType) && secSponsorType == 'N') {

			// If not follow up then calculate the sponsor amount as per rules mentioned above.
			if (opType != 'F' && opType != 'D') {
				secClaimPaise = getNationalSponsorAmount(totInsPaise, noOfDays, secSponsorId);
			}else {
				secClaimPaise = totInsPaise;
			}

			if (!secPerDayApplicable && secClaimPaise > 0)
				secClaimPaise = secClaimPaise + secDeductionPaise;

			// Miminum of approval and sponsor amount is considered.
			secClaimPaise = (secClaimPaise <= secApprovalPaise || secApprovalPaise == 0) ? secClaimPaise : secApprovalPaise;

			if (!empty(priSponsorType) && priSponsorType == 'N') {
				// If not follow up then calculate the sponsor amount as per rules mentioned above.
				if (opType != 'F' && opType != 'D') {
					priClaimPaise = getNationalSponsorAmount((totInsPaise - secClaimPaise), noOfDays, priSponsorId);
				}else {
					priClaimPaise = ((totInsPaise - secClaimPaise) > 0) ? (totInsPaise - secClaimPaise) : 0;
				}
			}else {
				priClaimPaise = ((totInsPaise - secClaimPaise) > 0) ? (totInsPaise - secClaimPaise) : 0;
			}

			if (visitType == 'i' && !priPerDayApplicable && priClaimPaise > 0)
				priClaimPaise = priClaimPaise + priDeductionPaise;

			// Miminum of approval and sponsor amount is considered.
			priClaimPaise = (priClaimPaise <= priApprovalPaise || priApprovalPaise == 0) ? priClaimPaise : priApprovalPaise;
		}
	}else {
		if (!empty(priSponsorType) && priSponsorType == 'I') {

			priClaimPaise = (totInsPaise <= priApprovalPaise || priApprovalPaise == 0) ? totInsPaise : priApprovalPaise;
			if (priPerDayApplicable && priClaimPaise > 0)
				priClaimPaise = priClaimPaise + priDeductionPaise;
			secClaimPaise = ((totInsPaise - priClaimPaise) > 0) ? (totInsPaise - priClaimPaise) : 0;
			if (visitType == 'i' && !secPerDayApplicable && secClaimPaise > 0)
				secClaimPaise = secClaimPaise + secDeductionPaise;
			secClaimPaise = (secClaimPaise <= secApprovalPaise || secApprovalPaise == 0) ? secClaimPaise : secApprovalPaise;

		}else if (!empty(secSponsorType) && secSponsorType == 'I') {

			secClaimPaise = (totInsPaise <= secApprovalPaise || secApprovalPaise == 0) ? totInsPaise : secApprovalPaise;
			if (!secPerDayApplicable && secClaimPaise > 0)
				secClaimPaise = secClaimPaise + secDeductionPaise;
			priClaimPaise = ((totInsPaise - secClaimPaise) > 0) ? (totInsPaise - secClaimPaise) : 0;
			if (visitType == 'i' && !priPerDayApplicable && priClaimPaise > 0)
				priClaimPaise = priClaimPaise + priDeductionPaise;
			priClaimPaise = (priClaimPaise <= priApprovalPaise || priApprovalPaise == 0) ? priClaimPaise : priApprovalPaise;

		}else {
			priClaimPaise = (totInsPaise <= priApprovalPaise || priApprovalPaise == 0) ? totInsPaise : priApprovalPaise;
			if (!priPerDayApplicable && priClaimPaise > 0)
				priClaimPaise = priClaimPaise + priDeductionPaise;
			secClaimPaise = ((totInsPaise - priClaimPaise) > 0) ? (totInsPaise - priClaimPaise) : 0;
			if (visitType == 'i' && !secPerDayApplicable && secClaimPaise > 0)
				secClaimPaise = secClaimPaise + secDeductionPaise;
			secClaimPaise = (secClaimPaise <= secApprovalPaise || secApprovalPaise == 0) ? secClaimPaise : secApprovalPaise;
		}
	}

	if (document.getElementById("primaryTotalClaim") != null) {
		document.getElementById("primaryTotalClaim").value = formatAmountPaise(priClaimPaise);
	}
	if (document.getElementById("secondaryTotalClaim") != null) {
		document.getElementById("secondaryTotalClaim").value = formatAmountPaise(secClaimPaise);
	}
}


// Recalculate the sponsor amounts only if the claim amounts are edited.
function setPlanTotalClaimAmounts(totInsPaise, claimAmtEdited, deductionEdited) {

	var deductionPaise = 0;
	if (document.getElementById("insuranceDeduction") != null)
		deductionPaise = getElementPaise(document.getElementById("insuranceDeduction"));

	var priApprovalPaise = 0;
	if (document.getElementById("primaryApprovalAmount") != null)
		priApprovalPaise = getElementPaise(document.getElementById("primaryApprovalAmount"));

	var noOfDays = getNoOfDaysOfStay();

	var priDeductionPaise = deductionPaise;
	totInsPaise = totInsPaise + priDeductionPaise;

	var visitCoPayPaise = null;

	if ((!empty(isPrimaryBill) && isPrimaryBill == 'Y')
			 || (parseInt(visitTpaBills) == 1) || (parseInt(visitTpaBills) > 1 &&
					((!empty(firstTpaBill) && firstTpaBill == document.mainform.billNo.value)))) {

		if (!empty(visitType) && visitType == 'i')
			visitCoPayPaise = !empty(planBean.ip_visit_copay_limit) ? getPaise(planBean.ip_visit_copay_limit) : 0;
		else
			visitCoPayPaise = !empty(planBean.op_visit_copay_limit) ? getPaise(planBean.op_visit_copay_limit) : 0;
	}

	var priClaimPaise = totInsPaise;
	var secClaimPaise = 0;

	// Miminum of approval and sponsor amount is considered.
	priClaimPaise = (priClaimPaise <= priApprovalPaise || priApprovalPaise == 0) ? priClaimPaise : priApprovalPaise;

	if (deductionEdited) {

			priClaimPaise = (priClaimPaise != 0) ? priClaimPaise - priDeductionPaise : priClaimPaise;

	}else if (claimAmtEdited) {
		var billCopayPaise = totAmtPaise - priClaimPaise;

		// Consider minimum of bill copay amount (or) plan copay amount.
		priDeductionPaise = (visitCoPayPaise != 0) ? ((billCopayPaise-visitCoPayPaise) > 0 ? visitCoPayPaise : billCopayPaise) : visitCoPayPaise;
		priDeductionPaise = claimAmtEdited ? deductionPaise : priDeductionPaise;

		priClaimPaise = (priClaimPaise != 0) ? priClaimPaise - priDeductionPaise : priClaimPaise;

		if (document.getElementById("insuranceDeduction") != null) {
			document.getElementById("insuranceDeduction").value = formatAmountPaise(priDeductionPaise);
		}
	}
	if (document.getElementById("primaryTotalClaim") != null) {
		document.getElementById("primaryTotalClaim").value = formatAmountPaise(priClaimPaise);
	}
	if (document.getElementById("secondaryTotalClaim") != null) {
		document.getElementById("secondaryTotalClaim").value = formatAmountPaise(secClaimPaise);
	}
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

// Redeeming points calculation chargewise.
function getTotalMaxPointsRedeemable() {
	var billRewardPointsRedeemed = 0;
	if (document.mainform.rewardPointsRedeemed != null)
		billRewardPointsRedeemed = getAmountDecimal(document.mainform.rewardPointsRedeemed.value, 2);

	var maxPointsRedeemable = 0;
	if (billRewardPointsRedeemed > 0) {
		for (var i=0;i<getNumCharges();i++) {
			var maxPoints = getAmountDecimal(getIndexedValue("max_redeemable_points",i), 2);
			maxPointsRedeemable += maxPoints;
		}
	}
	return maxPointsRedeemable;
}

function resetRedeemedPoints() {
	var billRewardPoints = 0;
	if (document.mainform.rewardPointsRedeemed != null)
		billRewardPoints = getAmountDecimal(document.mainform.rewardPointsRedeemed.value, 2);

	var totalMaxPoints = getTotalMaxPointsRedeemable();
	if (billRewardPoints == totalMaxPoints) {
		resetFloorMaxRedeemedPoints();
	}else {
		resetRoundMaxRedeemedPoints();
	}
}

function resetRoundMaxRedeemedPoints() {

	var billRewardPoints = 0;
	if (document.mainform.rewardPointsRedeemed != null)
		billRewardPoints = getAmountDecimal(document.mainform.rewardPointsRedeemed.value, 2);

	var totalMaxPoints = getTotalMaxPointsRedeemable();
	var numLen = getNumCharges();
	var totRedeemedPoints = 0;

	// Exclude round off.
	for (var i=0;i<numLen;i++) {

		var originalPoints = getAmountDecimal(getIndexedValue("redeemed_points", i), 2);
		setHiddenValue(i, "redeemed_points", 0);

		var delCharge = getIndexedFormElement(mainform, "delCharge", i);
		if (delCharge && "true" == delCharge.value) {
			continue;
		}

		var chargeHead = getIndexedValue("chargeHeadId", i);
		if (chargeHead == 'ROF') {
			continue;
		}

		var eligibleToRedeem = getIndexedValue("eligible_to_redeem_points", i);
		if (eligibleToRedeem == 'Y') {
			var maxPoints = getAmountDecimal(getIndexedValue("max_redeemable_points",i), 2);
			var redeemedPoints = 0;

			if (billRewardPoints > 0) {
				if (maxPoints > 0) {
					if (billRewardPoints >= totRedeemedPoints) {
						var nextIndex = i+1;
						if (nextIndex < numLen && hasNextEligible(numLen, nextIndex)) {
							redeemedPoints = Math.round(billRewardPoints * maxPoints / totalMaxPoints);
							totRedeemedPoints += redeemedPoints;
						}else {
							redeemedPoints = (billRewardPoints - totRedeemedPoints);
							totRedeemedPoints += redeemedPoints;
						}
					}else {
						redeemedPoints = (billRewardPoints - totRedeemedPoints);
						totRedeemedPoints += redeemedPoints;
					}
				}
			}
			setHiddenValue(i, "redeemed_points", redeemedPoints);
			if (originalPoints != redeemedPoints)
				setIndexedValue("edited", i, 'true');
		}
	}
}

function hasNextEligible(numLen, index) {
	for (var i=index;i<numLen;i++) {
		var eligibleToRedeem = getIndexedValue("eligible_to_redeem_points", i);
		if (eligibleToRedeem == 'Y')
			return true;
	}
	return false;
}

function resetFloorMaxRedeemedPoints() {

	var billRewardPointsRedeemed = 0;
	if (document.mainform.rewardPointsRedeemed != null)
		billRewardPointsRedeemed = getAmountDecimal(document.mainform.rewardPointsRedeemed.value, 2);

	// Exclude round off.
	for (var i=0;i<getNumCharges();i++) {

		var originalRedeemedPoints = getAmountDecimal(getIndexedValue("redeemed_points", i), 2);
		setHiddenValue(i, "redeemed_points", 0);

		var delCharge = getIndexedFormElement(mainform, "delCharge", i);
		if (delCharge && "true" == delCharge.value) {
			continue;
		}

		var chargeHead = getIndexedValue("chargeHeadId", i);
		if (chargeHead == 'ROF') {
			continue;
		}

		var eligibleToRedeem = getIndexedValue("eligible_to_redeem_points", i);
		if (eligibleToRedeem == 'Y') {
			var maxPointsRedeemable = 0;
			if (billRewardPointsRedeemed > 0) {
				maxPointsRedeemable = getAmountDecimal(getIndexedValue("max_redeemable_points",i), 2);
				setHiddenValue(i, "redeemed_points", maxPointsRedeemable);
			}
			if (originalRedeemedPoints != maxPointsRedeemable)
				setIndexedValue("edited", i, 'true');
		}
	}
}

/*
 * Re-calculate all the totals: discount, amount, update amount due etc.
 * These are all global varibles to be referred elsewhere when required.
 * claimAmtEdited is true/false i.e to recalculate sponsor amounts.
 */
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
		// Claim amount without deduction
		totInsuranceClaimAmtPaise += getIndexedPaise("insClaimAmt",i);

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

			setHiddenValue(i, "max_redeemable_points", maxPointsRedeemable);
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
	var tpaReceiptsPaise		= (existingSponsorReceipts == 0) ? getPaise(existingRecdAmount) : getPaise(existingSponsorReceipts);
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
	setNodeText("lblTotInsAmt", strInsAmt, 0, strNetAmount + " - " + strPatientDue);
	setNodeText("lblSponsorRecdAmount", strSponsorRecdAmount);
	setNodeText("lblSponsorReceipts", strSponsorReceipts);
	setNodeText("lblSponsorDue", strSponsorDue, 0, strInsAmt + " - " + strSponsorRecdAmount+ " - " + strSponsorReceipts);
	setNodeText("lblSpnrWrittenOffAmt", strSponsorDue, 0);

	resetPayments();
}

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 *
 * resetTotalsForSpnsrPayments() --  This function calls getTotalSpnsrAmountDue()
 * to set the total_SpnsrAmtDuePaise for validations in tag.
 */
function resetPayments() {
	resetTotalsForPayments();
	resetTotalsForSpnsrPayments();
	resetTotalsForDepositPayments();
	resetTotalsForPointsRedeemed();

	// for bill now bill, auto-set the amount to be paid by patient.
	if ((billType == 'P') && (origBillStatus == 'A')) {
		// if a single payment mode exists, update that with the due amount automatically
		setTotalPayAmount();
	}
}

function getTotalAmount() {
	return getPaise(document.getElementById("lblPatientAmount").innerHTML);
}

function getTotalAmountDue() {
	return getPaise(document.getElementById("lblPatientDue").innerHTML);
}

function getTotalDepositAmountAvailable() {
	return getPaise(availableDeposits);
}

function getTotalRewardPointsAvailable() {
	return availableRewardPoints;
}

function getTotalRewardPointsAmountAvailable() {
	return getPaise(availableRewardPointsAmount);
}

function getTotalSpnsrAmountDue() {
	if (document.getElementById("lblSponsorDue") != null)
		return getPaise(document.getElementById("lblSponsorDue").innerHTML);
	else
		return 0;
}

function getIndexedRow(tableId, index) {
	var tab = document.getElementById(tableId);
	var row = tab.rows[index];
	return row;
}

var varTempPayingAmt = 0;
var tempPaidAmt = 0;

function validateSave() {
	var valid = true;
	var status = true;
	valid = valid && validatePrescribingDoctor();
	valid = valid && validateAllNumerics();
	valid = valid && validateClaimStatus();
	valid = valid && validateBillDeduction();
	valid = valid && validatePatientPayment();
	valid = valid && validateSponsorBillClose();
	valid = valid && validateCancel();
	valid = valid && validatePayDates();
//	valid = valid && validateDiscountAuth();
	valid = valid && validatePaymentType();
	valid = valid && validateDischaregDateTime();
	valid = valid && validatePrimaryClaimAmt();
	valid = valid && validateSecondaryClaimAmt();
	valid = valid && validateTotalClaimAmt();
	valid = valid && checkConductingDoctor();
	valid = valid && checkFinalization();
	valid = valid && validateCustomFields();
	return valid;
}


function onChangeBillDeduction() {
	if (!validateDeductionAmount())
		return false;
	if (totInsuranceClaimAmtPaise != 0)
		resetTotals(true, true);
	else
		resetTotals(false, true);
	return true;
}

function validateDeductionAmount() {
	var insuDedAmtObj = document.getElementById("insuranceDeduction");
	if (insuDedAmtObj != null) {
		if (!validateAmount(insuDedAmtObj, getString("js.billing.billlist.patientdeduction.avalidamount")))
			return false;
	}
	return true;
}

function validateBillDeduction() {
	if (!validateDeductionAmount())
		return false;

	var billStatus = document.mainform.billStatus.value;
	var insuDedAmtObj = document.getElementById("insuranceDeduction");

	if(insuDedAmtObj != null) {
		var deductionPaise  = getPaise(insuDedAmtObj.value);

		if (deductionPaise > totInsuranceClaimAmtPaise && (billStatus == 'F' || billStatus == 'C')) {
			var ok = confirm(" Warning: Patient deduction cannot be greater than claim amount. \n " +
				 				 "Do you want to proceed ? ");
			if (!ok) {
				insuDedAmtObj.focus();
				return false;
			}
		}
	}
	return true;
}

function onChangePrimaryClaimAmt() {
	if (!validatePrimaryClaimAmt())
		return false;
	resetTotals(false, false);
	return true;
}

function validatePrimaryClaimAmt() {
	var priClaimAmtObj = document.mainform.primaryTotalClaim;
	if(priClaimAmtObj != null) {
		if (!validateSignedAmount(priClaimAmtObj, getString("js.billing.billlist.primarysponsoramount.validamount")))
			return false;
	}
	return true;
}

function onChangeSecondaryClaimAmt() {
	if (!validateSecondaryClaimAmt())
		return false;
	resetTotals(false, false);
	return true;
}

function validateSecondaryClaimAmt() {
	var secClaimAmtObj = document.mainform.secondaryTotalClaim;
	if(secClaimAmtObj != null) {
		if (!validateSignedAmount(secClaimAmtObj, getString("js.billing.billlist.secondarysponsoramount.validamount")))
			return false;
	}
	return true;
}

function getSponsorTotalClaimAmt() {
	var priClaimAmtObj = document.mainform.primaryTotalClaim;
	var secClaimAmtObj = document.mainform.secondaryTotalClaim;

	var priClaimPaise = 0;
	if(priClaimAmtObj != null) {
		priClaimPaise = getElementPaise(priClaimAmtObj);
	}

	var secClaimPaise = 0;
	if(secClaimAmtObj != null) {
		secClaimPaise = getElementPaise(secClaimAmtObj);
	}

	return priClaimPaise + secClaimPaise;
}

function validateTotalClaimAmt() {
	var priClaimAmtObj = document.mainform.primaryTotalClaim;

	var totalClaimPaise = getSponsorTotalClaimAmt();

	if (totalClaimPaise > totInsAmtPaise) {
		var msg=getString("js.billing.billlist.sponsoramount");
		msg+=formatAmountPaise(totalClaimPaise);
		msg+=")";
		msg+=getString("js.billing.billlist.notbegreaterthanclaimamount");
		msg+=formatAmountPaise(totInsAmtPaise);
		msg+=")";
		msg+="\n";
		msg+=getString("js.billing.billlist.checksponsoramount");
		alert(msg);
		priClaimAmtObj.focus();
		return false;
	}
	return true;
}

function validatePaymentType() {
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(document.mainform, "paymentType", i);
			var amtObj = getIndexedFormElement(document.mainform, "totPayingAmt", i);

			var type = paymentObj.value;
			if ( (null != amtObj) && (amtObj.value != "") ) {
				if (type == '') {
					showMessage("js.billing.billlist.selectpaymenttype");
					paymentObj.focus();
					return false;
				}
			}
		}
	}
	return true;
}

/*function validateDiscountAuth() {
	if (totDiscPaise!= 0) {
		for (var i=0; i<getNumCharges(); i++) {
			var chargeHead = getIndexedValue("chargeHeadId",i);
			if (chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
					|| chargeHead == 'PHMED' || chargeHead == 'PHRET') {
				// no discount auth required for these items.
				continue;
			}

			var discObj = getIndexedFormElement(mainform, "disc", i);
			if (getPaise(discObj.value) > 0) {
				if ((parseInt(getIndexedValue("overall_discount_auth",i)) > 0
						|| parseInt(getIndexedValue("overall_discount_auth",i)) == -1)
						|| parseInt(getIndexedValue("discount_auth_dr",i)) > 0
						|| parseInt(getIndexedValue("discount_auth_pres_dr",i)) > 0
						|| parseInt(getIndexedValue("discount_auth_ref",i)) > 0
						|| parseInt(getIndexedValue("discount_auth_hosp",i)) > 0) {
					// allow

				} else if ((mainform.discountAuthName && '' == trimAll (mainform.discountAuthName.value))
					&& (mainform.discountCategory && '' == trimAll(mainform.discountCategory.value))) {
					showMessage("js.billing.billlist.selectdiscountauthorizer.discounts");
					document.mainform.discountAuthName.focus();
					return false;
				} else if(document.getElementById("billDiscountAuth").value == -1) {
				   showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
				   document.getElementById("billDiscountAuth").value='';
				   document.getElementById("discountAuthName").value='';
				   document.mainform.discountAuthName.focus();
				   return false;
				}
			}
		}
	}
	return true;
}*/

/*
 * If payment status is set to Paid, validate that the net payments is
 * equal to patient portion of billed amount. For Bill Later bill, we allow
 * it with a warning if write-off rights is A. For Bill Now bill, we don't
 * allow it.
 */
function validatePatientPayment() {
	document.mainform.paymentForceClose.value = "N";
	var billNo = document.mainform.billNo.value;

	if (document.mainform.paymentStatus.value == 'U')
		return true;

	var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund');

	var depositSetOff = 0;
	if (document.mainform.depositSetOff)
		depositSetOff = getPaise(document.mainform.depositSetOff.value);

	var rewardPointsAmount = 0;
	if (document.mainform.rewardPointsRedeemedAmount)
		rewardPointsAmount = getPaise(document.mainform.rewardPointsRedeemedAmount.value);

	var patientAmt = totAmtPaise - depositSetOff - rewardPointsAmount - totInsAmtPaise;
	var existingReceiptAmt = getPaise(existingReceipts);

	var paidAmt = existingReceiptAmt + payingAmt;

	if (patientAmt == existingReceiptAmt + payingAmt) {

		// Bill later bill when closed should be settled if payment is done.
		if (payingAmt != 0 && document.mainform.billStatus.value == "C"
				&& billType == 'C' && !validateSettlement()) {
			var msg=getString("js.billing.billlist.totalbillamount.patientpaidamountsnotmatch");
			msg+="\n";
			msg+=getString("js.billing.billlist.settlerefundamount.closethebill");
			alert(msg);
			return false;
		}
		return true;
	}

	if (document.mainform.billStatus.value == "C") {

		if (billType == 'C' && patientWriteOff == 'A') {
			document.mainform.paymentForceClose.value = "Y";
		} else if (billType == 'C') {
			showMessage("js.billing.billlist.patientdueamount.notwrittenoff");
			document.mainform.paymentForceClose.value = "N";
			return false;
		} else {
			// write-off not allowed for prepaid bills at all
			var msg=getString("js.billing.billlist.totalbillamount.patientpaidamountsnotmatch");
			msg+="\n";
			msg+=getString("js.billing.billlist.notclosethebill.setpaymentstatustopaid");
			alert(msg);
			document.mainform.paymentForceClose.value = "N";
			return false;
		}
	}
	return true;
}

function validateSponsorBillClose() {
	if (document.mainform.billStatus.value == "C") {
		if (sponsorBillNo != null && sponsorBillNo != '') {
			var msg=getString("js.billing.billlist.thisbillbelongstosponsor");
			msg+=sponsorBillNo;
			msg+="\n";
			msg+=getString("js.billing.billlist.cancelordelete.theclaimbilltoclose");
			alert(msg);
			return false;
		}
	}
	return true;
}

/*
 * Claim cannot be sent/received when bill is still open (ie, must  be finalized)
 */
function validateClaimStatus() {
	if (document.mainform.primaryClaimStatus == null)
		return true;

	if (document.mainform.primaryClaimStatus.value != 'O'
			&& document.mainform.billStatus.value == 'A') {
		showMessage("js.billing.billlist.claimnotbeinsent.billstatusopen");
		document.mainform.primaryClaimStatus.focus();
		return false;
	}

	if (document.mainform.secondaryClaimStatus != null
			&& document.mainform.secondaryClaimStatus.value != 'O'
			&& document.mainform.billStatus.value == 'A') {
		showMessage("js.billing.billlist.claimnotbeinsent.billstatusopen");
		document.mainform.secondaryClaimStatus.focus();
		return false;
	}

	if (document.mainform.primaryClaimStatus.value == 'O' &&
		(document.mainform.secondaryClaimStatus == null
		  || (document.mainform.secondaryClaimStatus != null
		  		&& document.mainform.secondaryClaimStatus.value == 'O'))) {

		var numPayments = getNumOfPayments();
		if (numPayments > 0) {
			for (var i=0; i<numPayments; i++) {
				var paymentObj = getIndexedFormElement(document.mainform, "paymentType", i);
				var type = paymentObj.value;
				if (type == 'sponsor_receipt_advance' || type == 'sponsor_receipt_settlement') {
					var msg=getString("js.billing.billlist.claimnotopenstate.sponsorpaymentreceived");
					msg+="\n " ;
					msg+=getString("js.billing.billlist.marktheclaimstatus.assentreceived");
					alert(msg);
					if (document.mainform.primaryClaimStatus.value == 'O')
						document.mainform.primaryClaimStatus.focus();

					if (document.mainform.secondaryClaimStatus != null
							&& document.mainform.secondaryClaimStatus.value == 'O')
						document.mainform.secondaryClaimStatus.focus();
					return false;
				}
			}
		}
	}
	return true;
}



/*
 * Validate that when cancelling a bill, the sum total of payments towards
 * the bill is 0. When any amounts have been paid/from the patient, they
 * need to be squared off.
 */
function validateCancel() {
	var newBillStatus = document.mainform.billStatus.value;
	if (newBillStatus == 'X') {
		// cannot cancel when any active charge element has an activity associated
		// with it, that is not cancelled

		var numCharges = getNumCharges();
		for (var i=0; i<numCharges; i++) {
			var hasActivity = getIndexedFormElement(mainform, 'hasActivity',i);
			if (hasActivity != null && hasActivity.value == 'true') {
				// check if the activity is cancelled
				if ((getIndexedFormElement(document.mainform,"delCharge",i).value!="true")) {
					var msg=getString("js.billing.billlist.billnotbecancelled");
					msg+=getString("js.billing.billlist.whichareactive.cancelactivity");
					alert(msg);
					return false;
				}
			}
		}

		// Check if bill is part of Sponsor Consolidated Bill or has any Sponsor Receipts.
		if (sponsorBillNo != null && sponsorBillNo != '') {
			var msg=getString("js.billing.billlist.thisbillbelongs.sponsorconsolidatedbill");
			msg+=sponsorBillNo;
			msg+="\n";
			msg+=getString("js.billing.billlist.cancelordelete.theclaimbilltocancel");
			alert(msg);
			return false;
		}

		if (sponsorBillorReceipt != null && sponsorBillorReceipt != '') {
			showMessage("js.billing.billlist.billhassponsorreceipts");
			return false;
		}

		// paying amount should offset existing credits, excluding insurance amount
		var existingCreditAmt = getPaise(existingReceipts);
		var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund') + getPayingAmountPaise('sponsor');
		var totalCredits =  payingAmt + existingCreditAmt;
		if (formatAmountValue(totalCredits) != 0) {
			var hint;
			if (existingCreditAmt == 0) {
				hint = getString("js.billing.billlist.hintsetpatientpayment");
			} else if (existingCreditAmt > 0) {
				hint = getString("js.billing.billlist.hintrefundrs") + formatAmountPaise(existingCreditAmt) +
					getString("js.billing.billlist.thepatientandtryagain");
			} else {
				hint = getString("js.billing.billlist.hintaddapatientpayment") + formatAmountPaise(existingCreditAmt)
					+ getString("js.billing.billlist.andtryagain");
			}
			var msg=getString("js.billing.billlist.billnotbecancelled.ifoutstandingpayments");
			msg+="\n";
			msg+=hint;
			alert(msg);
			return false;
		}
	}
	return true;
}


function validateEquipmentAndBedFinalization() {
	var billStatus = document.mainform.billStatus.value;
	if ((billType == 'C' || billNowTpa) && (billStatus == 'F' || billStatus == 'C')) {
		if (pendingEquipmentFinalization != 'Finalized') {
			showMessage("js.billing.billlist.notfinalizebill.someequipmentsfinalized");
			return false;
		}
		if (pendingBedFinalization != 'Finalized') {
			showMessage("js.billing.billlist.notfinalizebill.bedfinalizationdone");
			return false;
		}
	}
	return true;
}

// When ever bill is finalized or closed, validate if package processing is done.
function validateDynaPackageProcessing() {
	if (empty(mainform.dynaPkgId))
		return true;

	var pkgMarginRowId = getPackageMarginRowId();
	if (pkgMarginRowId != null) {

		var packageId = mainform.dynaPkgId.value;
		if (!empty(packageId) && packageId != 0) {

			var billStatus = document.mainform.billStatus.value;
			if (origBillStatus == 'A' && (billStatus == 'F' || billStatus == 'C')) {

				var msg = "";
				if ((!empty(dynaPackageProcessed) && dynaPackageProcessed =='N')
						|| (document.mainform.dynaPkgProcessed != null && trim(document.mainform.dynaPkgProcessed.value) == 'N'))
					msg +=getString("js.billing.billlist.packageprocessingnotdone");

				//if (msg == "" && hasPackageExcludedCharges())
					//msg += " There are some charges which are not included in package. "

				if (msg != "") {
					var ok = confirm("  Warning:" + msg + " \n "
						  +" Please check package process before finalize/closing the bill. \n "
						  +" Do you want to continue to Save?");
					if (!ok)
						return false;
				}
			}
		}
	}
	return true;
}

// When ever bill is printed (Open/Finalized/Closed), validate if package processing is not done before.
function validateDynaPackagePrint() {
	var pkgMarginRowId = getPackageMarginRowId();
	if (pkgMarginRowId != null) {

		var packageId = mainform.dynaPkgId.value;
		if (!empty(packageId) && packageId != 0) {
			var msg = "";

			var pkgMarginRowId = getPackageMarginRowId();
			var chargeExcluded = getIndexedValue("chargeExcluded", pkgMarginRowId);
			var marginQty	 	 = getAmount(getIndexedValue("qty", pkgMarginRowId));

			if (chargeExcluded == "Y") {
				if (marginQty == 0 || dynaPkgProcessedBefore != 'true') {

					msg += getString("js.billing.billlist.packageprocessingnotdone");

					if (msg != "") {
						var ok = confirm("  Warning:" + msg + " \n "
							  +" Please check package process before printing the bill. \n "
							  +" Do you want to continue to Print?");
						if (!ok)
							return false;
					}
				}
			}
		}
	}
	return true;
}

/*function hasPackageExcludedCharges() {
	for (var i=0;i<getNumCharges();i++) {
		var delCharge = getIndexedFormElement(mainform, "delCharge", i);
		var chargeExcluded = getIndexedValue("chargeExcluded", i);

		if (delCharge && "true" == delCharge.value)
			continue;

		if (!isChargeAmountIncludedEditable(i))
			continue;

		if (chargeExcluded == "Y")
			return true;
	}
	return false;
}
*/
function checkFinalization() {
	if(!validateEquipmentAndBedFinalization())
		return false;
	if(!validateDynaPackageProcessing())
		return false;
	else return true;
}

function checkConductingDoctor() {
	var billStatus = document.mainform.billStatus.value;
	if (billStatus == 'F' || billStatus == 'C') {
		var numCharges = getNumCharges();
		for (var i=0; i<numCharges; i++) {
			var condDoctorObj = getIndexedFormElement(mainform, 'payeeDocId',i);
			var condDoctorRequired = getIndexedFormElement(mainform, 'conducting_doc_mandatory',i);
			var description = getIndexedFormElement(mainform, 'description',i);
			var chargeHeadName = getIndexedFormElement(mainform, 'chargeHeadName',i);
			var postedDate = getIndexedFormElement(mainform, 'postedDate',i);
			var docEditable = isConductedByEditable(i);
			var delCharge = getIndexedFormElement(mainform, "delCharge", i);

			if (delCharge && "true" == delCharge.value)
				continue;

			if (docEditable && condDoctorObj != null && condDoctorObj.value == '' && condDoctorRequired != null
						&& condDoctorRequired.value == 'Y') {
				var msg=getString("js.billing.billlist.conductingdoctorrequired");
				msg+=chargeHeadName.value;
				msg+="(";
				msg+=description.value
				msg+=") date: ";
				msg+=postedDate.value;
				alert(msg);
				return false;

			}
		}
	}
	return true;
}



function doSave() {
	var delsta=document.getElementsByName('delCharge');
	for (var i=0; i<delsta.length; i++) {
		if (delsta[i].value == 'true'){
			setIndexedValue("isClaimLocked", i, 'false');
		}
	}

	var valid = validateSave();
	//valid = valid && validateBillRemarks();
	var billStatus = document.getElementById('billStatus').value;
	if (valid && billType == 'C' && restrictionType == 'N' && (billStatus == 'F' || billStatus == 'C')) {
		var ajaxReqObject = new XMLHttpRequest();
		var url = cpath+"/billing/BillAction.do?_method=checkPackageDocuments&patient_id="+document.getElementById('visitId').value;
		ajaxReqObject.open("POST", url.toString(), false);
		ajaxReqObject.send(null);

		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("var result="+ajaxReqObject.responseText);
				if (!result.success) {
					var ok = confirm(getString('js.billing.billlist.pkg.required.documents') + "\n * "+result.documents.join("\n * "));
					if (!ok)
						return false;
				}
			}
		}
	}
	if (valid) {
		document.mainform.buttonAction.value = 'save';
		document.getElementById("saveButton").disabled = true;
		document.mainform.submit();
	}
	return true;
}



function doBillCancel() {
	document.mainform.billStatus.value = 'X';
	var valid = validateSave();
	valid = valid && validateCancelReason();
	if (valid) {
		document.mainform.buttonAction.value = 'cancel';
		document.mainform.submit();
	}
}

function requestForcancellation() {
	document.mainform._method.value = "requestForBillCancellation";
	document.mainform.submit();
}

/*
function processPlanvisitCopay() {

	if (chargesAdded > 0 || chargesEdited > 0) {
		alert("New charges have been added or edited. Please save before processing visit copay.");
		return false;
	}

	var ok = confirm(" Do you want to process bill visit copay?");
	if (!ok)
		return false;

	document.mainform.buttonAction.value = 'visitCopayProcess';
	document.mainform.submit();
	return true;
}*/

function onChangeOkToDischarge() {

	if (pendingEquipmentFinalization != 'Finalized') {
		showMessage("js.billing.billlist.someequipmentfinalized");
		return false;
	}
	if (pendingBedFinalization != 'Finalized') {
		showMessage("js.billing.billlist.bedfinalizationnotdone");
		return false;
	}

	setDischargeVars(true);
	return true;
}

function onChangeDischarge() {

	if (document.mainform._okToDischarge.checked) {
		if (!empty(otherUnpaidBills)) {
			// Trying to discharge a patient.
			if (otherUnpaidBills.length > 0) {
				var billNos = "";
				for (var i=0; i< otherUnpaidBills.length; i++) {
					billNos += otherUnpaidBills[i].bill_no + ' ';
				}
				var ok = confirm("There are other bills (" + billNos + ") for this patient which need action." +
						"Are you sure you want to discharge?");
				if (!ok)
					return false;
			}

			if (pendingtests == 'true') {
				// just a warning
				var ok = confirm("This visit has some pending tests. Are you sure you want to discharge?");
				if (!ok)
					return false;
			}
		}
	} else {
		// trying to undischarge: nothing to be done
	}

	// set the hidden variable, enable date etc.
	setDischargeVars(true);
	return true;
}

function setDischargeVars(setDischargeDate) {

	if (document.mainform._okToDischarge.checked == null)
		return;

	enableDischargeDate();

	if (document.mainform._okToDischarge.checked) {
		document.mainform.okToDischarge.value = 'Y';
		if (setDischargeDate && document.mainform.dischargeDate != null) {
			document.mainform.dischargeDate.value = curDate;
			document.mainform.dischargeTime.value = curTime;
		}
	} else {
		document.mainform.okToDischarge.value = 'N';
	}
	return true;
}

function enableDischargeDate() {
	if (document.mainform._okToDischarge != null && document.mainform.dischargeDate != null) {
		var checked = document.mainform._okToDischarge.checked;
		document.mainform.dischargeDate.disabled = !checked;
		document.mainform.dischargeTime.disabled = !checked;
	}
}

function validateDischaregDateTime() {
	if (document.mainform.dischargeDate == null)
		return true;

	var disDate = document.mainform.dischargeDate;
	var disTime = document.mainform.dischargeTime;

	if (jModulesActivated.mod_ipservices != 'Y' &&
			billType == 'C' && visitType == 'i' &&
			null != disDate && null != disTime) {
		if((document.mainform._okToDischarge != null)
			&& (!document.mainform._okToDischarge.disabled)
			&&  document.mainform._okToDischarge.checked){

			if (disDate.value == "" ) {
				showMessage("js.billing.billlist.selectdischargedate");
				disDate.focus();
				return false;
			}
			if (disTime.value == "" ) {
				showMessage("js.billing.billlist.enterdischargetime");
				disTime.focus();
				return false;
			}
			if (!doValidateDateField(disDate)) return false;
			if (!validateTime(disTime)) return false;

			if( getDateDiff(regDate,disDate.value) < 0){
				var msg=getString("js.billing.billlist.dischargedategreater.equaltoadmissiondate");
				msg+=regDate;
				msg+=")";
				alert(msg);
				return false;
			}
		}
	}
	return true;
}

function billPrint(option,userNameInBillPrint) {
	if (chargesAdded > 0 || chargesEdited > 0) {
		showMessage("js.billing.billlist.newchargesaddedoredited.savebeforeprinting");
		return false;
	}

	if (!validateDynaPackagePrint())
		return false;

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
	url +="&billType="+optionParts[1];
	window.open(url);
}

function onChangeDeposits() {

	if (!validateDepositSetOff(document.mainform.depositSetOff, document.mainform.originalDepositSetOff))
		return false;

	if(ipDepositExists == 'true' && visitType == 'i'){

		var depositSetOff = document.mainform.depositSetOff.value;
		var totalAvailablebalance = availableDeposits;
		var totalIPAvailablebalance = ipDeposits;
		var totalGeneralAvailablebal = totalAvailablebalance - totalIPAvailablebalance;

		if(document.getElementsByName("depositType")[0].checked){
			if(depositSetOff > totalIPAvailablebalance)
				alert("Deposit set off amount is more than available IP deposit, So remaining amount will be set off against general deposit..");
		}else if(document.getElementsByName("depositType")[1].checked){
			if(depositSetOff > totalGeneralAvailablebal)
				alert("Deposit set off amount is more than available general deposit, So remaining amount will be set off against IP deposit..");
		}
	}

	return true;
}

function onChangeRewardPoints() {
	// Clears Payments also.
	if (!validateRewardPoints(document.mainform.rewardPointsRedeemed,
			document.mainform.rewardPointsRedeemedAmount, document.mainform.originalPointsRedeemed))
		return false;

	return true;
}


/*
function validateDiscPer() {
	if(!document.getElementById('itemDiscPer').disabled && (document.getElementById('disableDiscCategory1').value == 'true')){
		if(document.mainform.itemDiscPer.value != 0)
			document.mainform.discountCategory.disabled = true;
		else
			document.mainform.discountCategory.disabled = false;
	}

	if (!validateDecimal(document.mainform.itemDiscPer, getString("js.billing.billlist.discountpercent.validnumber"), 2))
		return false;
	return true;
}

function enableDisableDisc(discountsEnabled, discType) {
	var overallEnabled = (discType == 'overall') && discountsEnabled ;
	var splitEnabled = (discType != 'overall') && discountsEnabled ;

	editform.overallDiscByName.disabled = !(overallEnabled);
	editform.overallDiscRs.disabled = !(overallEnabled);

//	editform.discConductDocName.disabled = !(splitEnabled);
//	editform.discConductDocRs.disabled = !(splitEnabled);
//	editform.discPrescDocName.disabled = !(splitEnabled);
//	editform.discPrescDocRs.disabled = !(splitEnabled);
//	editform.discRefDocName.disabled = !(splitEnabled);
//	editform.discRefDocRs.disabled = !(splitEnabled);
//	editform.discHospUserName.disabled = !(splitEnabled);
//	editform.discHospUserRs.disabled = !(splitEnabled);
}

function onChangeDiscountType(type) {
	if (editform.discountType.checked) {
		enableDisableDisc(true, '');
	}else {
		enableDisableDisc(true, 'overall');
	}
}*/

/*function onChangeDiscount(i) {
	// recalculate the row net amount. Assume discount validation is already done.
	var rateObj = getIndexedFormElement(mainform, "rate", i);
	var qtyObj = getIndexedFormElement(mainform, "qty", i);
	var discObj = getIndexedFormElement(mainform, "disc", i);

	if (rateObj.value == "") { rateObj.value = 0; }
	if (qtyObj.value == "") { qtyObj.value = 0; }
	if (discObj.value == "") { discObj.value = 0; }

	var changedRate = getPaise(rateObj.value);
	var changedQty = getAmount(qtyObj.value);
	var changedDisc = getPaise(discObj.value);

	var newAmtPaise = changedRate*changedQty - changedDisc;

	setIndexedValue("amt", i, formatAmountPaise(newAmtPaise));
	setNodeText(getChargeRow(i).cells[AMT_COL], formatAmountPaise(newAmtPaise));

	// recalculate the insurance amount for the new amount
	if (editform.eClaimAmt != null) {
		var chargeHead = getIndexedValue("chargeHeadId", i);
		var insuranceCategoryId = getIndexedValue("insuranceCategoryId", i);
		var firstOfCategory = getIndexedValue("firstOfCategory", i);

		var claimAmounts = [];
		var billNo = document.mainform.billNo.value;
		var claimAmt = 0;
		var patAmt = 0;
		var remainingAmt = formatAmountPaise(newAmtPaise);
		var insPayable = findInList(jChargeHeads, "CHARGEHEAD_ID", chargeHead).INSURANCE_PAYABLE;

		if(tpaBill) {
			if (insPayable != 'Y') {
				for(var j=0; j<planList.length; j++){
					claimAmounts[j] = 0;
				}
				patAmt = formatAmountPaise(newAmtPaise);
			} else {
				var discAmt = formatAmountPaise(changedDisc);
				for(var j=0; j<planList.length; j++){
					discAmt = j == 0 ? discAmt : 0.00;
					claimAmt = calculateClaimAmount(remainingAmt,discAmt,insuranceCategoryId,firstOfCategory,
						visitType,billNo,planList[j].plan_id, insPayable);
					remainingAmt = remainingAmt - claimAmt;
					patAmt = remainingAmt;
					claimAmounts[j] = claimAmt;
				}
			}

			var row = getChargeRow(i);
			setNodeText(row.cells[CLAIM_COL], formatAmountPaise(getPaise(claimAmounts[0])));
			setNodeText(row.cells[DED_COL], formatAmountPaise(getPaise(patAmt)));

			setIndexedValue("insClaimAmt", i, formatAmountPaise(getPaise(claimAmounts[0])));
			setIndexedValue("priInsClaimAmt", i, formatAmountPaise(getPaise(claimAmounts[0])));
			setIndexedValue("insDeductionAmt", i, formatAmountPaise(getPaise(patAmt)));

			if(null == planList || planList.length <= 0) {
				if(insPayable == 'Y') {
					setNodeText(row.cells[CLAIM_COL], formatAmountPaise(newAmtPaise));
					setIndexedValue("insClaimAmt", i, formatAmountPaise(newAmtPaise));
				}
			}

			if(multiPlanExists) {
				setNodeText(row.cells[SEC_CLAIM_COL], formatAmountPaise(getPaise(claimAmounts[1])));
				setIndexedValue("secInsClaimAmt", i, formatAmountPaise(getPaise(claimAmounts[1])));
			}
		}
	}
	setIndexedValue("edited", i, 'true');
	setRowStyle(i);
	chargesEdited++;
}
*/
function resetDiscountTotalRs() {
	var totalDiscRs = 0;
/*	if (editform.discountType.checked) {
		totalDiscRs += getElementIdPaise("discConductDocRs");
		totalDiscRs += getElementIdPaise("discPrescDocRs");
		totalDiscRs += getElementIdPaise("discRefDocRs");
		totalDiscRs += getElementIdPaise("discHospUserRs");
	}*/
//	editform.totalDiscRs.value = formatAmountPaise(totalDiscRs);
//	recalcEditChargeAmount();
}
/*
function getDiscountTotalRs() {
	var totalDiscRs = 0;
	if (editform.discountType.checked) {
		totalDiscRs += getElementIdPaise("discConductDocRs");
		totalDiscRs += getElementIdPaise("discPrescDocRs");
		totalDiscRs += getElementIdPaise("discRefDocRs");
		totalDiscRs += getElementIdPaise("discHospUserRs");
	}
	return totalDiscRs;
}


function resetItemDiscounts(discCategory) {
	var chargeHeadIds = document.getElementsByName("chargeHeadId");
	var discElements = document.mainform.discountCheck;
	var oldDisc = document.mainform.oldDisc;
	var oldOverallDiscountAuth = document.mainform.old_overall_discount_auth;
	for(var i=0; i < getNumCharges(); i++){

		var discountPlanDetails = filterList(discountPlansJSON, 'discount_plan_id', discCategory);
		var insuCatId = getIndexedValue ("insuranceCategoryId", i);
		var descrId = getIndexedValue ("descriptionId", i)

		for (var j=0 ; j< discountPlanDetails.length; j++){
			var item = discountPlanDetails[j];
			if( (item["applicable_type"] == 'C' &&  chargeHeadIds[i].value == item["applicable_to_id"] )
					|| ( item["applicable_type"] == 'N' && insuCatId == item["applicable_to_id"] )
					|| ( item["applicable_type"] == 'I' && descrId == item["applicable_to_id"] ) ){
				chargeHeadItemExists = true;
				if ( chargeHeadItemExists ) break;
			}
		}
	}

	applyItemDiscPer(0,0);

	var claimAmtEdited = false;
	for(var i=0; i < getNumCharges(); i++){

		if(getIndexedValue("chargeGroupId",i) == 'RET' || getIndexedValue("chargeGroupId",i) == 'MED' ||
					getIndexedValue("chargeGroupId",i) == 'ITE') {
			continue;
		}

		var discObj = getIndexedFormElement(mainform, "disc", i);
		if(oldDisc[i].value != 0 && oldOverallDiscountAuth[i].value == -1) {
			if(discountPlanDetails !=null && discountPlanDetails.length > 0) {
				for (var j=0 ; j< discountPlanDetails.length; j++){
					var item = discountPlanDetails[j];
					if( (item["applicable_type"] == 'C' &&  chargeHeadIds[i].value == item["applicable_to_id"] )
							|| ( item["applicable_type"] == 'N' && insuCatId == item["applicable_to_id"] )
							|| ( item["applicable_type"] == 'I' && descrId == item["applicable_to_id"] ) ){

						setNodeText(getChargeRow(i).cells[DISC_COL], oldDisc[i].value);
						discObj.value = oldDisc[i].value;
						setIndexedValue("overall_discount_amt", i, oldDisc[i].value);
						setIndexedValue("overall_discount_auth", i, oldOverallDiscountAuth[i].value);

						var claim = 0;
						if (editform.eClaimAmt != null)
							claim = getPaise(getIndexedValue("insClaimAmt", i));

						onChangeDiscount(i);

						if (editedAmounts(i, claim))
							claimAmtEdited = true;

					}
				}
			}
		}
	}

	resetTotals(claimAmtEdited, false);

}


var disAuthArray = [];
var disAuthIndex = [];

function loadDiscAuthArray() {
	if(jDiscountAuthorizers !=null && jDiscountAuthorizers.length > 0) {
		disAuthArray.length = jDiscountAuthorizers.length;
		disAuthIndex.length = jDiscountAuthorizers.length;
		for ( i=0 ; i< jDiscountAuthorizers.length; i++){
			var item = jDiscountAuthorizers[i];
			disAuthArray[i] = item["disc_auth_name"];
			disAuthIndex[item["disc_auth_name"]] = i;
		}
	}
}
*/




function onChangeConducted(sType, aArgs) {
	var doctor = aArgs[2];
	document.editform.eConductedId.value = doctor[1];
}

function clearConductedId() {
	document.editform.eConductedId.value = '';
}


function alertPatientAmtAdjustOnDelete(obj){
	var row = getThisRow(obj);
	var table =  YAHOO.util.Dom.getAncestorByTagName(row, 'table');
	var id = getRowChargeIndex(row);
	if (tpaBill && getIndexedValue ("firstOfCategory", id)=="true" && planId>0) {
		var insuranceCategoryName = '';
		var patientFixedAmt = '';
		if(null != jPolicyNameList) {
			for(var j=0; j< jPolicyNameList.length; j++){
				var policyObj = jPolicyNameList[j];
				if(policyObj.insurance_category_id == getIndexedValue ("insuranceCategoryId", id)){
					patientFixedAmt = policyObj.patient_amount;
					insuranceCategoryName = policyObj.insurance_category_name;
				}
			}
		}
		if(insuranceCategoryName!=''){
			var continueDelete = confirm ("Deleting the first item of Insurance Item Category: "+insuranceCategoryName+
			"\nThe Patient Co-Pay Fixed Amount Per Category may need to be adjusted,\nfor additional items of the same category...");
			if(!continueDelete)
				return false;
		}
	}
	return true;
}

function setCategoryValuesOnDelete(obj){
	var row = getThisRow(obj);
	var table =  YAHOO.util.Dom.getAncestorByTagName(row, 'table');
	var id = getRowChargeIndex(row);
	if (tpaBill && getIndexedValue ("firstOfCategory", id)=="true" && planId>0) {
		getElementByName(row , "firstOfCategory").value = "false";
		for(var ii=0; ii<table.rows.length;ii++) {
			if(table.rows[ii]!= row && !isNaN(getElementByName(table.rows[ii], "insuranceCategoryId").value)
				&& getElementByName(row,"insuranceCategoryId").value == getElementByName(table.rows[ii], "insuranceCategoryId").value
				&& getElementByName(table.rows[ii], "firstOfCategory").value == "false" && getIndexedValue("delCharge", getRowChargeIndex(table.rows[ii])) !="true"){
				var index = getRowChargeIndex(table.rows[ii]);
				getElementByName(table.rows[ii] , "firstOfCategory").value = "true";
				var chargeHeadId = getIndexedValue("chargeHeadId", index);
				var insuranceCategoryId = getIndexedValue("insuranceCategoryId", index);
				var firstOfCategory = getIndexedValue("firstOfCategory", index);
				var rate = getIndexedValue("rate",index);
				var discount = getIndexedValue("disc",index);
				var qty = getIndexedValue("qty",index);
				var amountPaise = getPaise(rate)*qty - getPaise(discount);

				var claimPaise = getClaimAmount(chargeHeadId, amountPaise, getPaise(discount), insuranceCategoryId, true);
				var patientAmtPaise = amountPaise - claimPaise;
				setEditedAmounts(index, table.rows[ii], rate, qty, discount, formatAmountPaise(amountPaise),
						formatAmountPaise(claimPaise), formatAmountPaise(patientAmtPaise));
				resetTotals(true, false);
				break;
			}
		}

	}
}



function resetCategoryValuesOnUndelete(obj){
	var row = getThisRow(obj);
	var table =  YAHOO.util.Dom.getAncestorByTagName(row, 'table');
	var id = getRowChargeIndex(row);
	for(var ii=0; ii<table.rows.length;ii++) {
		if(table.rows[ii]!= row && getElementByName(row,"insuranceCategoryId").value == getElementByName(table.rows[ii], "insuranceCategoryId").value){
			getElementByName(row, "firstOfCategory").value = "false";
			var index = getRowChargeIndex(row);
			var chargeHeadId = getIndexedValue("chargeHeadId", index);
			var insuranceCategoryId = getIndexedValue("insuranceCategoryId", index);
			var firstOfCategory = getIndexedValue("firstOfCategory", index);
			var rate = getIndexedValue("rate",index);
			var discount = getIndexedValue("disc",index);
			var qty = getIndexedValue("qty",index);
			var amountPaise = getPaise(rate)*qty - getPaise(discount);

			var claimPaise = getClaimAmount(chargeHeadId, amountPaise,getPaise(discount), insuranceCategoryId, false);
			var patientAmtPaise = amountPaise - claimPaise;
			setEditedAmounts(index, row, rate, qty, discount, formatAmountPaise(amountPaise),
			formatAmountPaise(claimPaise), formatAmountPaise(patientAmtPaise));
			resetTotals(true, false);
			break;
		}
	}
}

function cancelCharge(obj) {
	var row = getThisRow(obj);
	var table =  YAHOO.util.Dom.getAncestorByTagName(row, 'table');
	var id = getRowChargeIndex(row);
	if (!isDeletable(id))
		return false;

	var oldDeleted =  getIndexedValue("delCharge", id);
	var chargeId = getIndexedValue("chargeId", id);
	var isNew = (chargeId.substring(0,1) == '_');

	var claimAmtEdited = false;

	if (isNew) {

		var claim = 0;
		if (editform.eClaimAmt != null)
			claim = getPaise(getIndexedValue("insClaimAmt", id));

		if (claim != 0) claimAmtEdited = true;

		// just delete the row, no need to mark it as deleted
		//if (tpaBill) setPatientAmtsForRowDel(row);
		if(!alertPatientAmtAdjustOnDelete(obj)) return false;
		//setCategoryValuesOnDelete(obj);
		row.parentNode.removeChild(row);
		chargesAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
			editform.eQty.value = 1;
			setIndexedValue("qty", id, 1);
			setIndexedValue("amt", id, getIndexedValue("rate", id));
			setNodeText(row.cells[QTY_COL], formatAmountValue(1));
			setNodeText(row.cells[AMT_COL], getIndexedValue("rate", id));
			if(isTpa){
				var currTablPatAmt = getIndexedValue("rate", id);
				var clamAmt = parseInt(row.cells[CLAIM_COL].textContent);
				if(clamAmt > 0){
					currTablPatAmt=currTablPatAmt- clamAmt;
				}
				if(multiPlanExists){
					var secClamAmt = parseInt(row.cells[SEC_CLAIM_COL].textContent);
					if(secClamAmt >0){
						currTablPatAmt=currTablPatAmt- secClamAmt;
					}
				}
				setNodeText(row.cells[DED_COL], formatAmountValue(currTablPatAmt));
			}


			//setNodeText(row.cells[CLAIM_COL], formatAmountPaise(priClaimPaise + returnClaimPaise));
			//setNodeText(row.cells[SEC_CLAIM_COL], formatAmountPaise(secClaimPaise + returnClaimPaise));
			//setNodeText(row.cells[DED_COL], formatAmountPaise(
			//		(amtPaise+returnAmtPaise) - (priClaimPaise+returnClaimPaise+secClaimPaise) ));
			//resetCategoryValuesOnUndelete(obj);

			// the quantity may have been made 0 on save. Restore it to 1 on an un-cancel.
			// TODO: needs re-calc of amounts as well.
		} else {
			newDeleted = 'true';
			if(!alertPatientAmtAdjustOnDelete(obj)) return false;

			//setCategoryValuesOnDelete(obj)
		}

		var claim = 0;
		if (editform.eClaimAmt != null)
			claim = getPaise(getIndexedValue("insClaimAmt", id));

		if (claim != 0) claimAmtEdited = true;

		setIndexedValue("delCharge", id, newDeleted);
		setIndexedValue("edited", id, "true");
		setRowStyle(id);
		chargesEdited++;

		// set the same status to all referenced charges
		var numCharges = getNumCharges();
		for (var i=0; i<numCharges; i++) {
			var ref = getIndexedValue("chargeRef", i);
			if (ref == chargeId) {
				setIndexedValue("delCharge", i, newDeleted);
				setIndexedValue("edited", i, "true");
				setRowStyle(i);
				chargesEdited++;

				claim = 0;
				if (editform.eClaimAmt != null)
					claim = getPaise(getIndexedValue("insClaimAmt", i));

				if (claim != 0) claimAmtEdited = true;
			}
		}

		var amtIncludedObj = getIndexedFormElement(mainform, "amount_included", id);

		var amountToIncludePaise = getIndexedPaise("amt", id);
		var amountToInclude = formatAmountPaise(amountToIncludePaise);

		var chargeExcluded = "P";
		if (amtIncludedObj != null) {
			if (getPaise(amtIncludedObj.value) == amountToIncludePaise)
				chargeExcluded = "N";
			else if (getPaise(amtIncludedObj.value) == 0)
				chargeExcluded = "Y";
		}

		var chargeHead = getIndexedValue("chargeHeadId", id);
		var dynaPkgId = (document.mainform.dynaPkgId != null) ? (document.mainform.dynaPkgId.value) : 0;
		if (!empty(dynaPkgId) && dynaPkgId != 0) {
			if (chargeHead == 'BIDIS' || chargeHead == 'ROF' || chargeHead == 'CSTAX' || chargeHead == 'BSTAX') {
				setIndexedValue("chargeExcluded", id, "Y");
			}else {
				setIndexedValue("chargeExcluded", id, chargeExcluded);
			}
		}else {
			setIndexedValue("chargeExcluded", id, "N");
		}
	}
	//setIndexedValue("isClaimLocked", id, 'false');
	var visitID = document.getElementById('visitId').value;
	resetTotals(claimAmtEdited, false);

	if(document.mainform.showCancelled.checked)
		onChangeFilter();

	return false;
}

/*
 * Show the discount dialog. Note that we never disable the discount button, so that the user
 * can read the details at the least. All disabling will be done inside the dialog, for items
 * where the discount is not applicable.
 */
/*
function showDiscounts(id) {

	var discType = 'overall';

	var deleted = getIndexedValue("delCharge",id);
	var chargeHead = getIndexedValue("chargeHeadId",id);
	var discountsEnabled = isDiscountEditable(id);

	if (getIndexedValue("discount_auth_dr",id) == 0 &&
		getIndexedValue("discount_auth_pres_dr",id) == 0 &&
		getIndexedValue("discount_auth_ref",id) == 0 &&
		parseInt(getIndexedValue("discount_auth_hosp",id)) == 0 ) {
		discType = 'overall';

	} else if((parseInt(getIndexedValue("overall_discount_auth",id)) > 0) ||
			(parseInt(getIndexedValue("overall_discount_auth",id)) == -1)) {
		discType = 'overall';

	} else if(parseInt(getIndexedValue("overall_discount_auth",id)) == 0) {
		discType = 'split';

	} else if(getIndexedValue("overall_discount_auth",id) == '') {
		discType = 'overall';
	}

	//enableDisableDisc(discountsEnabled, discType);

	editform.overallDiscByName.value = getIndexedValue("overall_discount_auth_name",id);
	editform.overallDiscBy.value = getIndexedValue("overall_discount_auth",id);

	if (getIndexedValue("overall_discount_auth",id) == -1) {
		editform.overallDiscRs.value = getIndexedValue("disc",id);
	}else {
		editform.overallDiscRs.value = getIndexedValue("overall_discount_amt",id);
	}

	//resetDiscountTotalRs();

	return discType;
}

function validateSignedDiscount() {
	if (editform.discountType.checked) {
		if (!editform.discConductDocRs.readOnly)
			if (!validateSignedAmount(editform.discConductDocRs, getString("js.billing.billlist.discount.validamount")))
				return false;
		if (!editform.discPrescDocRs.readOnly)
			if (!validateSignedAmount(editform.discPrescDocRs, getString("js.billing.billlist.discount.validamount")))
				return false;
		if (!editform.discRefDocRs.readOnly)
			if (!validateSignedAmount(editform.discRefDocRs, getString("js.billing.billlist.discount.validamount")))
				return false;
		if (!editform.discHospUserRs.readOnly)
			if (!validateSignedAmount(editform.discHospUserRs, getString("js.billing.billlist.discount.validamount")))
				return false;
	}else {
		if (!editform.overallDiscRs.readOnly)
			if (!validateSignedAmount(editform.overallDiscRs, getString("js.billing.billlist.discount.validamount")))
				return false;
	}
	return true;
}

function validateUnSignedDiscount() {
	if (editform.discountType.checked) {
		if (!editform.discConductDocRs.readOnly)
			if (!validateAmount(editform.discConductDocRs, getString("js.billing.billlist.discount.validamount")))
				return false;
		if (!editform.discPrescDocRs.readOnly)
			if (!validateAmount(editform.discPrescDocRs, getString("js.billing.billlist.discount.validamount")))
				return false;
		if (!editform.discRefDocRs.readOnly)
			if (!validateAmount(editform.discRefDocRs, getString("js.billing.billlist.discount.validamount")))
				return false;
		if (!editform.discHospUserRs.readOnly)
			if (!validateAmount(editform.discHospUserRs, getString("js.billing.billlist.discount.validamount")))
				return false;
	}else {
		if (!editform.overallDiscRs.readOnly)
			if (!validateAmount(editform.overallDiscRs, getString("js.billing.billlist.discount.validamount")))
				return false;
	}
	return true;
}
*/
/*
function validateDiscounts(id) {

	var chargeHead = getIndexedValue("chargeHeadId",id);
	if (chargeHead != 'INVRET' && !isPharmacyReturns(id)) {
		if (!validateUnSignedDiscount())
			return false;
	}else {
		if (!validateSignedDiscount())
			return false;
	}

	var discConDrName = editform.discConductDocName.value;
	var discConDr = editform.discConductDoc.value;
	var discConDrRs = getAmount(editform.discConductDocRs.value);

	var discPresDrName = editform.discPrescDocName.value;
	var discPresDr = editform.discPrescDoc.value;
	var discPresDrRs = getAmount(editform.discPrescDocRs.value);

	var discRefDrName = editform.discRefDocName.value;
	var discRefDr = editform.discRefDoc.value;
	var discRefDrRs = getAmount(editform.discRefDocRs.value);

	var discHospDrName = editform.discHospUserName.value;
	var discHospDr = editform.discHospUser.value;
	var discHospDrRs = getAmount(editform.discHospUserRs.value);

	var discOverallName = editform.overallDiscByName.value;
	var discOverall = editform.overallDiscBy.value;
	var discOverallRs = getAmount(editform.overallDiscRs.value);

	var title = '';

	if (editform.discountType.checked) {

		if(discConDrName != '' && discConDrRs == 0) {
			showMessage("js.billing.billlist.enterconductingdoctor.discountamount");
			editform.discConductDocRs.focus();
			return false;
		}
		if(discConDrName == '' && discConDrRs > 0) {
			showMessage("js.billing.billlist.enterconductingdoctor.discountauth");
			editform.discConductDocName.focus();
			return false;
		}
		if ((getPaise(discConDrRs) != getPaise(getIndexedValue("oldDisc",id))) && (getPaise(discConDrRs) != 0) && discConDr == -1) {
			showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
			editform.discConductDocName.focus();
			return false;
		}
		if(discPresDrName != '' && discPresDrRs == 0) {
			showMessage("js.billing.billlist.enterprescribingdoctor.discountamount");
			editform.discPrescDocRs.focus();
			return false;
		}
		if(discPresDrName == '' && discPresDrRs > 0) {
			showMessage("js.billing.billlist.enterprescribingdoctor.discountauth");
			editform.discPrescDocName.focus();
			return false;
		}
		if ((getPaise(discPresDrRs) != getPaise(getIndexedValue("oldDisc",id))) && (getPaise(discPresDrRs) != 0) && discPresDr == -1) {
			showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
			editform.discPrescDocName.focus();
			return false;
		}
		if(discRefDrName != '' && discRefDrRs == 0) {
			showMessage("js.billing.billlist.enterreferrerdiscountamount");
			editform.discRefDocRs.focus();
			return false;
		}
		if(discRefDrName == '' && discRefDrRs > 0) {
			showMessage("js.billing.billlist.enterreferrerdiscountauth");
			editform.discRefDocName.focus();
			return false;
		}
		if ((getPaise(discRefDrRs) != getPaise(getIndexedValue("oldDisc",id))) && (getPaise(discRefDrRs) != 0) && discRefDr == -1) {
			showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
			editform.discRefDocName.focus();
			return false;
		}
		if(discHospDrName != '' && discHospDrRs == 0) {
			showMessage("js.billing.billlist.enterhospitaldiscountamount");
			editform.discHospUserRs.focus();
			return false;
		}
		if(discHospDrName == '' && discHospDrRs > 0) {
			showMessage("js.billing.billlist.enterhospitaldiscountauth");
			editform.discHospUserName.focus();
			return false;
		}
		if ((getPaise(discHospDrRs) != getPaise(getIndexedValue("oldDisc",id))) && (getPaise(discHospDrRs) != 0) && discHospDr == -1) {
			showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
			editform.discHospUserName.focus();
			return false;
		}

		var discObj = getIndexedFormElement(mainform, "disc", id);
		var origDiscPaise = getPaise(discObj.value);
		var chargeExcluded = getIndexedValue("chargeExcluded", id);

		if (chargeExcluded != 'Y' && getDiscountTotalRs() != origDiscPaise) {
			var amtObj = editform.eAmt;
			var claimObj = editform.eClaimAmt;
			var amtIncludedObj = editform.eAmtIncluded;
			var amountToIncludePaise = getPaise(amtObj.value);
			if (amtIncludedObj != null) {
				var amountExcludedPaise = amountToIncludePaise - getPaise(amtIncludedObj.value);
				if (amountExcludedPaise != 0 && getDiscountTotalRs() > amountExcludedPaise) {
					if (!validateDiscountWithDynaPackage())
						return false;
				}
			}
		}

		setIndexedValue("discount_auth_dr_name",id, discConDrName);
		setIndexedValue("discount_auth_dr",id, discConDr);
		setIndexedValue("dr_discount_amt",id, discConDrRs);

		setIndexedValue("discount_auth_pres_dr_name",id, discPresDrName);
		setIndexedValue("discount_auth_pres_dr",id, discPresDr);
		setIndexedValue("pres_dr_discount_amt",id, discPresDrRs);

		setIndexedValue("discount_auth_ref_name",id, discRefDrName);
		setIndexedValue("discount_auth_ref",id, discRefDr);
		setIndexedValue("ref_discount_amt",id, discRefDrRs);

		setIndexedValue("discount_auth_hosp_name",id, discHospDrName);
		setIndexedValue("discount_auth_hosp",id, discHospDr);
		setIndexedValue("hosp_discount_amt",id, discHospDrRs);

		setIndexedValue("overall_discount_auth_name",id, '');
		setIndexedValue("overall_discount_auth",id, 0);
		setIndexedValue("overall_discount_amt",id, 0);

		if (discConDr != '' && discConDrRs > 0) {
			title = getString("js.billing.billlist.conductingdoctor.discountgivenby")+discConDrName+" is: "+discConDrRs;
		}
		if(discPresDr != '' && discPresDrRs > 0) {
			title = title + getString("js.billing.billlist.prescribingdoctor.discountgivenb")+discPresDrName+" is: "+discPresDrRs;
		}
		if(discRefDr != '' && discRefDrRs > 0) {
			title = title + getString("js.billing.billlist.referrerdiscountgiven")+discRefDrName+" is: "+discRefDrRs;
		}
		if(discHospDr != '' && discHospDrRs > 0) {
			title = title + getString("js.billing.billlist.hospitaldiscountgiven")+discHospDrName+" is: "+discHospDrRs;
		}
	}else {
		if (discOverallName != '' && discOverallRs == 0) {
			showMessage("js.billing.billlist.enteroveralldiscountamount");
			editform.overallDiscRs.focus();
			return false;
		}
		if ((getPaise(discOverallRs) != getPaise(getIndexedValue("oldDisc",id))) && (getPaise(discOverallRs) != 0) && discOverall == -1) {
			showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
			editform.overallDiscByName.focus();
			return false;
		}

		var discObj = getIndexedFormElement(mainform, "disc", id);
		var origDiscPaise = getPaise(discObj.value);
		var chargeExcluded = getIndexedValue("chargeExcluded", id);

		if (chargeExcluded != 'Y' && getPaise(discOverallRs) != origDiscPaise) {
			var amtObj = editform.eAmt;
			var claimObj = editform.eClaimAmt;
			var amtIncludedObj = editform.eAmtIncluded;
			var amountToIncludePaise = getPaise(amtObj.value);
			if (amtIncludedObj != null) {
				var amountExcludedPaise = amountToIncludePaise - getPaise(amtIncludedObj.value);
				if (amountExcludedPaise != 0 && getPaise(discOverallRs) > amountExcludedPaise) {
					if (!validateDiscountWithDynaPackage()) {
						editform.overallDiscRs.focus();
						return false;
					}
				}
			}
		}

		setIndexedValue("overall_discount_auth_name",id, discOverallName);
		setIndexedValue("overall_discount_auth",id, discOverall);
		setIndexedValue("overall_discount_amt",id, discOverallRs);

		setIndexedValue("discount_auth_dr_name",id, '');
		setIndexedValue("discount_auth_dr",id, 0);
		setIndexedValue("dr_discount_amt",id, 0);

		setIndexedValue("discount_auth_pres_dr_name",id, '');
		setIndexedValue("discount_auth_pres_dr",id, 0);
		setIndexedValue("pres_dr_discount_amt",id, 0);

		setIndexedValue("discount_auth_ref_name",id, '');
		setIndexedValue("discount_auth_ref",id, 0);
		setIndexedValue("ref_discount_amt",id, 0);

		setIndexedValue("discount_auth_hosp_name",id, '');
		setIndexedValue("discount_auth_hosp",id, 0);
		setIndexedValue("hosp_discount_amt",id, 0);

		title = "Overall discount given by "+discOverallName+" is:" +discOverallRs;
	}

	var ratePaise = getIndexedPaise("rate",id);
	var qty = getIndexedPaise("qty",id)/100;
	var chrgAmt = ratePaise * qty;
	if ((getIndexedValue("chargeHeadId",id) == 'BIDIS') && discOverall == -1) {
		showMessage("js.billing.billlist.discountauthorizer.notberateplandiscount");
		editform.overallDiscByName.focus();
		return false;
	}

	return true;
}
*/
function initEditChargeDialog() {
	var dialogDiv = document.getElementById("editChargeDialog");
	dialogDiv.style.display = 'block';
	editChargeDialog = new YAHOO.widget.Dialog("editChargeDialog",{
			width:"810px",
			text: "Edit Charge",
			context :["chargesTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onEditCancel,
	                                                scope:editChargeDialog,
	                                                correctScope:true } );
	editChargeDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editChargeDialog.render();
}


function showEditDialog(obj, chHead, rem) {
	enableNextPrev();
	return showEditChargeDialog(obj, chHead, rem);
}

function enableNextPrev() {
	editform.prevBtn.disabled = false;
	editform.nextBtn.disabled = false;
}

var fieldEdited = false;

function setEdited() {
	fieldEdited = true;
}

function setInsCalcReq(){
	document.getElementById("eInsCalcReq").value='true';
}

function showNextOrPrevCharge(navigate) {
	var id = editform.editRowId.value;
	var row = getChargeRow(id);

	var remarks = getIndexedValue("remarks", id).substring(0, 4);
	var chargeHead = getIndexedValue("chargeHeadId", id);

	if (navigate == 'next')
		id++;
	else if(navigate == 'prev')
		id--;

	if (id >= 0 && getIndexedValue("chargeHeadId", id) != null &&
				getIndexedValue("chargeHeadId", id) != ''){
		YAHOO.util.Dom.removeClass(row, 'editing');
		row = getChargeRow(id);

		var checkid = id;
		if (navigate == 'next') {
			checkid--;
		}else if(navigate == 'prev') {
			checkid++;
		}
		if (checkid >= 0 && getIndexedValue("chargeHeadId", checkid) != null &&
				getIndexedValue("chargeHeadId", checkid) != '') {
			if (navigate == 'next')
				editform.prevBtn.disabled = false;
			else if(navigate == 'prev')
				editform.nextBtn.disabled = false;
		}
	}else {
		if (navigate == 'next')
			editform.nextBtn.disabled = true;
		else if(navigate == 'prev')
			editform.prevBtn.disabled = true;
	}
	if (fieldEdited)
		onEditSubmit(true);
	fieldEdited = false;
	showEditChargeDialog(row, chargeHead, remarks);
}

var chargeHead;
var chargeRemarks;

function showEditChargeDialog(obj, chHead, rem) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	chargeHead=chHead;
	chargeRemarks=rem;

	YAHOO.util.Dom.addClass(row, 'editing');
	editform.editRowId.value = id;

	editform.ePostedDate.readOnly = !isPostedDateEditable(id);
	editform.ePostedTime.readOnly = !isPostedDateEditable(id);
	//editform.eQty.readOnly = !isQtyEditable(id);
	editform.eRate.readOnly = !isRateEditable(id);
	//editform.eClaimAmt.readOnly = !isQtyEditable(id);
	editform.eRemarks.readOnly = !isItemRemarksEditable(id);
	editform.eUserRemarks.readOnly = !isItemRemarksEditable(id);
	editform.eCode.readOnly = !isItemCodeEditable(id);
//	editform.overallDiscRs.readOnly = !isDiscountEditable(id) || isSplitDiscount(id);
//	editform.eConducted.disabled = !isConductedByEditable(id);
	editform.eItemRemarks.readOnly = !isItemRemarksEditable(id);

//	if (hasDynaPackage) {
//		editform.eAmtIncluded.readOnly = !isChargeAmountIncludedEditable(id);
//		editform.eQtyIncluded.readOnly = !isChargeQuantityIncludedEditable(id);
//		editform.ePackageFinalized.disabled = !isChargeAmountIncludedEditable(id);
//	}
	if(isTpa){
		var delCharge = getIndexedFormElement(mainform, "delCharge", id);
		if (editform.eClaimAmt != null && !(delCharge && "true" == delCharge.value)) {
			editform.eClaimAmt.readOnly = false;
		}else{
			editform.eClaimAmt.readOnly = true;
		}
	}
	var subGrpId = getIndexedValue("service_sub_group_id", id);
	var subGrp = findInList(allServiceSubgroupsList,'service_sub_group_id',subGrpId);
	var grpId = subGrp.service_group_id;
	var grp = findInList(serviceGroupsJSON,'service_group_id',grpId);

	document.getElementById("eChargeGroup").textContent = getIndexedValue("chargeGroupName", id);
	document.getElementById("eChargeHead").textContent = getIndexedValue("chargeHeadName", id);
	document.getElementById("eDescription").textContent = getIndexedValue("description", id);
	document.getElementById("eserviceGroup").textContent = grp.service_group_name;
	document.getElementById("eserviceSubGroup").textContent = subGrp.service_sub_group_name;

	var itemDesc = getItemDescription(id);
	document.getElementById("eItemDescription").textContent = itemDesc;

	editform.ePostedDate.value =  getIndexedValue("postedDate",id);
	editform.ePostedTime.value =  getIndexedValue("postedTime",id);
	editform.eCode.value = getIndexedValue("actRatePlanItemCode", id);
	var payeeDocId = getIndexedValue("payeeDocId", id);
	editform.eRemarks.value = getIndexedValue("remarks", id);
	editform.eUserRemarks.value = getIndexedValue("userRemarks", id);
	editform.eItemRemarks.value = getIndexedValue("itemRemarks", id);
	editform.eRate.value = getIndexedValue("rate", id);
	editform.eQty.value = getIndexedValue("qty", id);
	editform.eAmt.value = getIndexedValue("amt", id);

	if (getIndexedFormElement(mainform, "insClaimAmt", id) != null) {
		editform.eClaimAmt.value = getIndexedValue("insClaimAmt",id);
		editform.eDeductionAmt.value = formatAmountPaise((getPaise(editform.eAmt.value) - getPaise(editform.eClaimAmt.value)));
	}

	if(multiPlanExists && isTpa){
		editform.eClaimAmt.value = getIndexedValue("priInsClaimAmt",id);
		editform.eSecClaimAmt.value = getIndexedValue("secInsClaimAmt",id);
		editform.eDeductionAmt.value = formatAmountPaise((getPaise(editform.eAmt.value) - getPaise(editform.eClaimAmt.value) - getPaise(editform.eSecClaimAmt.value)));
//		editform.ePreAuthId.value = getIndexedValue("preAuthId",id);
//		editform.ePreAuthModeId.value = getIndexedValue("preAuthModeId",id);
//		editform.eSecPreAuthId.value = getIndexedValue("secPreAuthId",id);
//		editform.eSecPreAuthModeId.value = getIndexedValue("secPreAuthModeId",id);
		editform.eSecIncludeInClaimCalc.value =  getIndexedValue("SecIncludeInClaim",id);
		if(editform.eSecClaimAmt != null && !(delCharge && "true" == delCharge.value)){
			editform.eSecClaimAmt.readOnly = false;
		}else{
			editform.eSecClaimAmt.readOnly = true;
		}
	}

	if(isTpa)
		editform.eClaimLocked.value = getIndexedValue("isClaimLocked", id);

//	YAHOO.lutsr.accordion.collapse(document.getElementById("discountDD"));
//	var discType = showDiscounts(id);

	YAHOO.lutsr.accordion.collapse(document.getElementById("itemDetailsDD"));
	if (empty(getIndexedValue("userRemarks", id)) && empty(itemDesc))
		document.getElementById("itemDetailsImg").style.display = 'none';
	else
		document.getElementById("itemDetailsImg").style.display = 'block';

	editChargeDialog.cfg.setProperty("context", [row.cells[EDIT_COL], "tr", "bl"], false);
	editChargeDialog.show();
	editform.eRemarks.focus();
	return false;
}

var cachedItemDescription = [];
function getItemDescription(id) {

	var chargeHead	= getIndexedValue("chargeHeadId", id);
	var chargeGroup	= getIndexedValue("chargeGroupId", id);
	var chargeId	= getIndexedValue("chargeId", id);
	var chargeRef	= getIndexedValue("chargeRef", id);

	var itemDesc = '';
	var itemChargeId = chargeId;

	if (!empty(chargeGroup) &&
			(chargeGroup == 'OPE' || chargeGroup == 'SNP' || chargeGroup == 'DIA')) {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/billing/BillAction.do?_method=getItemDescription&chargeGroup=' + chargeGroup;

		if (chargeGroup == 'OPE') {
			if (chargeHead == 'SACOPE') {
				itemChargeId = chargeId;
			}else {
				var operation_ch_id = '';
				if (chargeHead == 'TCOPE') {
					operation_ch_id = chargeId;

				}else {
					for (var j=0;j<getNumCharges();j++) {
						var charge_id	= getIndexedValue("chargeId", j);
						var charge_head	= getIndexedValue("chargeHeadId", j);
						var charge_ref	= getIndexedValue("chargeRef", j);
						if (charge_id == chargeRef && charge_head == 'TCOPE') {
							operation_ch_id = charge_id;
							break;
						}
					}
				}
				for (var i=0;i<getNumCharges();i++) {
					var chId	= getIndexedValue("chargeId", i);
					var cHead	= getIndexedValue("chargeHeadId", i);
					var cRef	= getIndexedValue("chargeRef", i);
					if (cRef == operation_ch_id && cHead == 'SACOPE') {
						itemChargeId = chId;
						break;
					}
				}
			}
		}

		if (!empty(itemChargeId)) {
			url += '&itemChargeId=' + itemChargeId ;
		}

		if (cachedItemDescription[itemChargeId] == undefined) {
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var description =" + ajaxobj.responseText);
						if (!empty(description)) {
							itemDesc = description;
							cachedItemDescription[itemChargeId] = itemDesc;
						}
					}
				}
			}
		}else {
			itemDesc = cachedItemDescription[itemChargeId];
		}
	}
	return itemDesc;
}

function isDiscCategoryApplicable(id) {
	var row = getChargeRow(id);
	var discountCheck = getElementByName(row,"discountCheck");
	if(discountCheck.disabled && discountCheck.checked)
		return true;
	else
		return false;
}

function recalcEditChargeAmount() {
	var oldAmountPaise = getPaise(editform.eAmt.value);
	var rateObj = editform.eRate;
	var qtyObj = editform.eQty;
//	var discObj = (editform.discountType.checked) ? editform.totalDiscRs : editform.overallDiscRs;
	var id = editform.editRowId.value;

	if (rateObj.value == "") { rateObj.value = 0; }
	if (qtyObj.value == "") { qtyObj.value = 0; }
//	if (discObj.value == "") { discObj.value = 0; }

	if (!isSignedAmount(rateObj.value) || !isSignedAmount(qtyObj.value) ) {
		return;
	}

	var changedRate = getPaise(rateObj.value);
	var changedQty = getAmount(qtyObj.value);

	changedDisc=0;
	var newAmtPaise = changedRate*changedQty - changedDisc;
//	var newAmtPaise = changedRate*changedQty ;

	// update the claim amount if Insurance bill
	if (newAmtPaise > 0 && editform.eClaimAmt) {
		if (!isSignedAmount(editform.eClaimAmt.value))
			return;

		var chargeHead = getIndexedValue("chargeHeadId", id);
		var insuranceCategoryId = getIndexedValue("insuranceCategoryId", id);
		var firstOfCategory = getIndexedValue("firstOfCategory", id);
		var newClaimPaise = getClaimAmount(chargeHead, newAmtPaise,changedDisc,insuranceCategoryId, firstOfCategory);
		var row = getChargeRow(id);
		var newPatientPaise = 0;

		var oldClaimPaise = getPaise(editform.eClaimAmt.value);
		var isOldClaimValid = newAmtPaise<0 && oldClaimPaise<0 ? ((-newAmtPaise)>=(-oldClaimPaise)) : (newAmtPaise>= oldClaimPaise);
		if(isOldClaimValid) {
			newClaimPaise = oldClaimPaise;
			newPatientPaise = newAmtPaise - oldClaimPaise;
		}else {
			newClaimPaise = oldClaimPaise;
			newPatientPaise = newAmtPaise -  newClaimPaise;
		}
		var table = YAHOO.util.Dom.getAncestorByTagName(row, 'table');
		var chargeHeadId = getElementByName(row,"chargeHeadId").value;

		editform.eClaimAmt.value = formatAmountPaise(newClaimPaise);
		editform.eDeductionAmt.value = formatAmountPaise(newPatientPaise);
	}

	if (newAmtPaise == 0 &&  editform.eClaimAmt){
		editform.eClaimAmt.value = formatAmountPaise(getPaise(0));
		editform.eDeductionAmt.value = formatAmountPaise(getPaise(0));
	}
	// set the new amount
	editform.eAmt.value = formatAmountPaise(newAmtPaise);
}

function setDeductionAmt(type) {
	// set the deduction amount as a result of change in claim amount directly edited
	var claimPaise = getPaise(editform.eClaimAmt.value);
	var amtPaise = getPaise(editform.eAmt.value);
	var isOldClaimValid = amtPaise<0 &&  claimPaise<0 ? ((-amtPaise)>=(-claimPaise)) : (amtPaise>= claimPaise);
	if(isOldClaimValid)
		editform.eDeductionAmt.value = formatAmountPaise(amtPaise - claimPaise);
	else {
		editform.eClaimAmt.value = formatAmountPaise(claimPaise);
		editform.eDeductionAmt.value = formatAmountPaise(amtPaise - claimPaise);
	}

	editform.eClaimLocked.value = true;

	if(type == 'P' && editform.ePriIncludeInClaimCalc.value == 'N'){
		editform.ePriIncludeInClaimCalc.value = 'Y';
	}

	if(type == 'S' && editform.eSecIncludeInClaimCalc.value == 'N'){
		editform.eSecIncludeInClaimCalc.value = 'Y'
	}

	recalcEditChargeAmount();
}

function getActualClaim(row) {
 	var chargehead = getElementByName(row,"chargeHeadId").value;
 	var amtPaise = getPaise(getElementByName(row,"amt").value);
 	var discPaise = getPaise(getElementByName(row,"disc").value);
 	var insuranceCategoryId = getElementByName(row,"insuranceCategoryId").value
 	return formatAmountPaise(getClaimAmount(chargehead, amtPaise,discPaise,insuranceCategoryId ,true));
}



function onEditSubmit(navigate) {
	var id = editform.editRowId.value;
	var chargeHead = getIndexedValue("chargeHeadId",id);

	if (!editform.eRate.readOnly)
		if (!validateSignedAmount(editform.eRate, getString("js.billing.billlist.ratevalidamount")))
			return false;

	if (chargeHead != 'INVRET' && !isPharmacyReturns(id)) {
		if (!editform.eQty.readOnly)
			if (!validateAmount(editform.eQty, getString("js.billing.billlist.quantityvalidamount")))
				return false;
		if (editform.eClaimAmt != null && !editform.eClaimAmt.readOnly)
			if (!validateSignedAmount(editform.eClaimAmt, getString("js.billing.billlist.claimvalidamount")))
				return false;
	}else {
		if (!editform.eQty.readOnly)
			if (!validateSignedAmount(editform.eQty, getString("js.billing.billlist.quantityvalidamount")))
				return false;
		if (editform.eClaimAmt != null && !editform.eClaimAmt.readOnly)
			if (!validateSignedAmount(editform.eClaimAmt, getString("js.billing.billlist.claimvalidamount")))
				return false;
	}

	if (editform.eClaimAmt != null && !editform.eClaimAmt.readOnly)
		if (!validateSignedAmount(editform.eClaimAmt, getString("js.billing.billlist.claimvalidamount")))
			return false;

	var packageFinalizedObj = editform.ePackageFinalized;
	var postedDateObj = editform.ePostedDate;
	var postedTimeObj = editform.ePostedTime;

	if (!doValidateDateField(postedDateObj, 'past')) {
		postedDateObj.focus();
		return false;
	}
	if (!validateTime(postedTimeObj)) {
		postedTimeObj.focus();
		return false;
	}

	var row = getChargeRow(id);

	//resetDiscountTotalRs();

	var disc = 0;

	var packageFinalized = (packageFinalizedObj != null && packageFinalizedObj.checked)?"Y":"N";
	var backupremarks=row.cells[REMARKS_COL].textContent;
	var postedDate = postedDateObj.value;
	var postedTime = postedTimeObj.value;
	var rate = editform.eRate.value;
	var qty = editform.eQty.value;

	var amt = editform.eAmt.value;
	var remarks = editform.eRemarks.value;
	var userRemarks = editform.eUserRemarks.value;
	var itemRemarks = editform.eItemRemarks.value;
	var code = editform.eCode.value;
	var deduction = amt; var claim = 0;
	if (editform.eClaimAmt != null) {
		deduction = editform.eDeductionAmt.value;
		claim = editform.eClaimAmt.value;
	}
	setIndexedValue("postedDate",id, postedDate);
	setIndexedValue("postedTime",id, postedTime);
	setNodeText(row.cells[DATE_COL], postedDate);
	setIndexedValue("remarks", id, remarks);
	setIndexedValue("userRemarks", id, userRemarks);
	setIndexedValue("itemRemarks", id, itemRemarks);
	if (chargeHead=='PHCMED' || chargeHead=='PHCRET' || chargeHead=='PHMED' || chargeHead=='PHRET')	{
		row.cells[REMARKS_COL].innerHTML = '<a target="#" title="No.'+chargeRemarks+'"'
							+ 'href='+cpath+'/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&printerId=0&duplicate=true&saleId='
							+ chargeRemarks+'>'
							+ backupremarks+'</a>';

	}if (chargeHead == 'INVITE')	{
		row.cells[REMARKS_COL].innerHTML = '<a target="#" title="No.'+chargeRemarks+'"'
					+ 'href='+cpath+'/DirectReport.do?report=StoreStockPatientIssues&issNo='
					+ chargeRemarks+'>'
					+ backupremarks+'</a>';
	}else {
		setNodeText(row.cells[REMARKS_COL], remarks, 16);
	}

	setIndexedValue("actRatePlanItemCode", id, code);
	setNodeText(row.cells[CODE_COL], code, 10);

	if (editform.eClaimAmt != null) {
		var table = YAHOO.util.Dom.getAncestorByTagName(row, 'table');
		var chargeHeadId = getElementByName(row,"chargeHeadId").value;
		deduction = formatAmountPaise(getPaise(amt)-getPaise(claim));
	}

	var oldClaim = 0;
	if (editform.eClaimAmt != null)
		oldClaim = getPaise(getIndexedValue("insClaimAmt", id));

	setEditedAmounts(id, row, rate, qty, disc, amt, claim, deduction);

	if(multiPlanExists && isTpa) {
		var secClaimAmt = 0;
		if(editform.eSecClaimAmt != null) {
			secClaimAmt = editform.eSecClaimAmt.value;
			deduction = formatAmountPaise(getPaise(amt)-getPaise(claim)-getPaise(secClaimAmt));
		}
		setSecondaryPlanAmounts(id, editform, row ,amt,claim,deduction,secClaimAmt);
	}

	if(isTpa){
		setIndexedValue("isClaimLocked", id, editform.eClaimLocked.value);
		setIndexedValue("priIncludeInClaim", id, editform.ePriIncludeInClaimCalc.value);
		if(multiPlanExists){
			setIndexedValue("secIncludeInClaim", id, editform.eSecIncludeInClaimCalc.value);
		}
	}

	var chargeHead = getIndexedValue("chargeHeadId", id);
	var dynaPkgId = (document.mainform.dynaPkgId != null) ? (document.mainform.dynaPkgId.value) : 0;
	if (!empty(dynaPkgId) && dynaPkgId != 0) {
		if (chargeHead == 'BIDIS' || chargeHead == 'ROF' || chargeHead == 'CSTAX' || chargeHead == 'BSTAX') {
			setIndexedValue("chargeExcluded", id, "Y");

		}else {
			var amountToIncludePaise = getIndexedPaise("amt", id);

			var chargeExcluded = "P";
			setIndexedValue("chargeExcluded", id, chargeExcluded);
		}
	}else {
		setIndexedValue("chargeExcluded", id, "N");
	}

	setIndexedValue("edited", id, 'true');
	setIndexedValue("isClaimLocked", id, 'true');
	setRowStyle(id);
	chargesEdited++;
	filterCharges();
	var insCalcReq = document.getElementById("eInsCalcReq").value;
	var visitID = document.getElementById('visitId').value;

	resetTotals(editedAmounts(id, oldClaim), false);
	setIndexedValue("isClaimLocked", id, 'true');
	YAHOO.util.Dom.removeClass(row, 'editing');
	if (!navigate)
		editChargeDialog.hide();
	var editImg = row.cells[EDIT_COL].childNodes[1];
	editImg.focus();
}

function onEditCancel(){
	var id = editform.editRowId.value;
	var row = getChargeRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	editChargeDialog.hide();
	var editImg = row.cells[EDIT_COL].childNodes[1];
	editImg.focus();
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

function initProcedureNames() {
	var procedureNameArray = [];
	if(jProcedureNameList !=null && jProcedureNameList.length > 0) {
		procedureNameArray.length = jProcedureNameList.length;
		for ( i=0 ; i< jProcedureNameList.length; i++){
			var item = jProcedureNameList[i];
			procedureNameArray[i] = item["procedure_code"]+"-"+item["procedure_name"];
		}
	}
	if(document.mainform.procedure_name != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(procedureNameArray);
			oAutoComp = new YAHOO.widget.AutoComplete('procedure_name', 'pro_dropdown', dataSource);
			oAutoComp.maxResultsDisplayed = 20;
			oAutoComp.queryMatchContains = true;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.forceSelection = false;
			oAutoComp.textboxBlurEvent.subscribe(function() {
			var proName = YAHOO.util.Dom.get('procedure_name').value;
				if(proName == '' && !YAHOO.util.Dom.get('procedure_name').readOnly) {
					document.mainform.primaryApprovalAmount.value = "";
					document.mainform.procedure_no.value = 0;
				}
			});
			oAutoComp.itemSelectEvent.subscribe(function() {
				var pName = YAHOO.util.Dom.get('procedure_name').value;
				if(pName != '') {
					for ( var i=0 ; i< jProcedureNameList.length; i++){
						if(pName == jProcedureNameList[i]["procedure_code"]+"-"+jProcedureNameList[i]["procedure_name"]){
							document.mainform.primaryApprovalAmount.value = jProcedureNameList[i]["procedure_limit"];
							document.mainform.procedure_no.value = jProcedureNameList[i]["procedure_no"];
							break;
						}
					}
				}else{
					document.mainform.primaryApprovalAmount.value = "";
					document.mainform.procedure_no.value = 0;
				}
			});
		}
	}
}

/*function selectAllForDiscounts() {
	var disCheckElmts = document.mainform.discountCheck;
	if (document.getElementById("discountAll").checked)	{
		for(var i=0;i<disCheckElmts.length;i++) {
			if (getThisRow(disCheckElmts[i]).style.display == "none"){}
			else disCheckElmts[i].checked=true;
		}
	} else {
		for(var i=0;i<disCheckElmts.length;i++) {
			disCheckElmts[i].checked=false;
		}
	}
}

function resetSelectedDiscountItems() {
	document.getElementById("discountAll").checked = false;
	selectAllForDiscounts();
}
*/
function chkRateVariation() {
	var id = editform.editRowId.value;
	var savedPaise = getIndexedPaise("savedRate", id);
	var rateObj = editform.eRate;
	var allowItemRateIncr = getIndexedValue("allowRateIncrease", id);
	var allowItemRateDcr  = getIndexedValue("allowRateDecrease", id);

	if ( getPaise(rateObj.value) > savedPaise &&
		 (allowRateIncr != 'A' || getIndexedValue("allowRateIncrease", id) == 'false' ) ) {
		 	var msg=getString("js.billing.billlist.notauthorized.increasetherateabove");
		 	msg+=formatAmountPaise(savedPaise);
		 	alert(msg);
		 	rateObj.value = formatAmountPaise(savedPaise);
		 	rateObj.focus();
		 	return false;
 	}

 	if ( getPaise(rateObj.value) < savedPaise &&
 	    ( allowRateDcr != 'A' || getIndexedValue("allowRateDecrease", id) == 'false' ) ) {
 	    	var msg=getString("js.billing.billlist.notauthorized.decreasetheratebelow");
 	    	msg+=formatAmountPaise(savedPaise);
 	    	alert(msg);
 	    	rateObj.value = formatAmountPaise(savedPaise);
		 	rateObj.focus();
		 	return false;
 	}
}

function validateCancelReason() {
	if ( (null != document.mainform.cancelReason) && (
		('' == trimAll(document.mainform.cancelReason.value)) ||
		(trimAll(document.mainform.oldCancelReason.value) == trimAll(document.mainform.cancelReason.value)))) {
		showMessage("js.billing.billlist.entercancelreason.forcancellingbill");
		document.mainform.cancelReason.focus();
		return false;
	} else {
		return true;
	}
}

function showAlertRemarks() {

	var remarks = billRemarks;

	if (!empty(billLabelMasterJson)) {
		for (var i=0; i<billLabelMasterJson.length; i++) {
			var bill = billLabelMasterJson[i];
			if (bill.bill_label_id == billLabelId){

				if (bill.alert == 'Y' && remarks != '' && origBillStatus == 'A') {
					intializeAlertDialog();
					document.getElementById('alertLabelName').innerHTML = bill.bill_label_name;
					document.getElementById('alertLabelRemarks').innerHTML = remarks;
					showAlertRemarksDialog.show();
				} else {
					return;
				}
			}
		}
	}
}

var alertdialogDiv;
function intializeAlertDialog() {
	alertdialogDiv = document.getElementById("showAlertRemarksDialog");
	alertdialogDiv.style.display = 'block';
	showAlertRemarksDialog = new YAHOO.widget.Dialog("showAlertRemarksDialog",{
			width:"310px",
			text: "Show Alert",
			context :["chargesTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:showAlertRemarksDialog.cancel,
	                                                scope:showAlertRemarksDialog,
	                                                correctScope:true } );
	showAlertRemarksDialog.cfg.queueProperty("keylisteners", escKeyListener);

	showAlertRemarksDialog.render();
}

function highlightMarkedOnes(from) {

	var selectedLabelId = null;
	if (from == 'onload')
		selectedLabelId = billLabelId;
	else
		selectedLabelId = document.getElementById('billLabel').options[document.getElementById('billLabel').selectedIndex].value;
	if (!empty(billLabelMasterJson)) {
		if (selectedLabelId == -1) {
			YAHOO.util.Dom.removeClass(document.getElementById('billLabel'), 'billLabel');
			return;
		}
		for (var i=0; i<billLabelMasterJson.length; i++) {
			var billLabelMap = billLabelMasterJson[i];
			if (billLabelMap.bill_label_id == selectedLabelId ) {
				if (billLabelMap.highlight == 'Y') {
					YAHOO.util.Dom.addClass(document.getElementById('billLabel'), 'billLabel');
				} else {
					YAHOO.util.Dom.removeClass(document.getElementById('billLabel'), 'billLabel');
				}
			}
		}
	}
}

function onChangePrimaryApprovalAmt() {
	var priAprAmtObj = document.mainform.primaryApprovalAmount;

	if (priAprAmtObj == null || trimAll(priAprAmtObj.value) == '')
		return true;

	if(priPlanExists == 'true'){
		var priApprovalAmtPaise = getElementPaise(document.getElementById("primaryApprovalAmount"));
		var priClaimAmtPaise = getElementPaise(document.getElementById("primaryTotalClaim"));
		if(priApprovalAmtPaise != 0 && priClaimAmtPaise > priApprovalAmtPaise) {
			var msg=getString("js.billing.billlist.primarysponsoramountnot.greaterthanprimaryapprovalamt");
			msg+="\n";
			msg+=getString("js.billing.billlist.adjustitemlevelclaimamounts");
			alert(msg);
			return false;
		}
	}
	return true;
}

function onChangeSecondaryApprovalAmt() {
	var secAprAmtObj = document.mainform.secondaryApprovalAmount;

	if (secAprAmtObj == null || trimAll(secAprAmtObj.value) == '')
		return true;

	if(secPlanExists == 'true'){
		var secApprovalAmtPaise = getElementPaise(document.getElementById("secondaryApprovalAmount"));
		var secClaimAmtPaise = getElementPaise(document.getElementById("secondaryTotalClaim"));
		if(secApprovalAmtPaise != 0 && secClaimAmtPaise > secApprovalAmtPaise) {
			var msg=getString("js.billing.billlist.secondarysponsoramountnot.greatersecondaryapproval");
			msg+="\n";
			msg+=getString("js.billing.billlist.adjustitemlevelclaimamounts");
			alert(msg);
			return false;
		}
	}
	return true;
}

function validatePrescribingDoctor() {

	if (gOnePrescDocForOP == 'Y' && patientType == 'o') {
		var consultingDoctor = null;
		consultingDoctor = consultingDocName;
		var prescribedDoctorIDs = document.getElementsByName('prescDocId');
		var prescDoctor = null;

		for (var i=0; i<prescribedDoctorIDs.length; i++) {
			if (!empty(prescribedDoctorIDs[i].value)) {
				var doctor = findInList(doctorsList, 'doctor_id', prescribedDoctorIDs[i].value);
				prescDoctor = doctor['doctor_name'];
				if (empty(consultingDoctor))
					consultingDoctor = 	prescDoctor;
				if (consultingDoctor != prescDoctor) {
					alert(getString('js.order.common.one.prescribingdoctor.required'));
					return false;
				}
			}
		}
		return true;
	}
	return true;
}






function validateCustomFields(){
	var valid = true;
	var billStatus = document.mainform.billStatus.value;
	if ( billStatus == 'F' && isCustomFieldsExist == 'false' && is_Baby == 'true'){
		alert(getString('js.billing.billlist.mandatory.patient.information'));
		valid = false;
	}
	if ( billStatus == 'C' && isCustomFieldsExist == 'false' && is_Baby == 'true'){
		alert(getString('js.billing.billlist.mandatory.patient.information.closed'));
		valid = false;
	}
	return valid;
}

//returns bill open date & time,use ful for date comparision
function getBillOPenDateTime(){
	var openDate = document.mainform.opendate.value;
	var openTime = document.mainform.opentime.value;
	return getDateTime(openDate,openTime );
}

function isFinalizedDateLessThanOpenDate(){
	 var billOpenDateTime = getBillOPenDateTime();
	 var finalizedDateObj = document.mainform.finalizedDate;
	 var finalizedTimeObj = document.mainform.finalizedTime;
     var finalizedDtTime  = getDateTime(finalizedDateObj.value, finalizedTimeObj.value);

     return finalizedDtTime < billOpenDateTime;
}


function isAddedCharge(index){
	var chargeId = getIndexedValue("chargeId", index);
	return (chargeId.substring(0,1) == "_");
}
