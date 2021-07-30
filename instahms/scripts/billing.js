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
var fromOnload = false;
var sponsorsMap = {};

var userLimit =0;
var userLimitPaise = 0;
var usrLimitForCalc = 0;
var authChange = false;
var discChange = false;
var qtyChange = false;
var rateChange = false;
totDiscPaise = 0;
totAmtPaise = 0;
totTaxPaise = 0;
totInsTaxPaise = 0;
totInsAmtPaise = 0;
totCreditsPaise = 0;
totAmtDuePaise = 0;
totSpnrAmtDuePaise = 0;
totInsuranceClaimAmtPaise = 0;
totSecInsuranceClaimAmtPaise = 0;
totPriInsuranceClaimAmtPaise = 0;

initialPriInsuranceClaimAmt = 0;
initialSecInsuranceClaimAmt= 0;
var tempPriSponsorsSettlement=0;
var tempSecSponsorsSettlement=0;
var loyaltyOffersDialog=null;
var gPackageChargeDetails = {};
var chargeRefMap = {};
const PACKAGE_CHARGE_DIST_SCALE = 1000;
var signatureDialog=null;
var nexusToken = '<%= session.getAttribute("nexus_token")%>';
var signaturePad;
var newSignature;

var couponRedemptionDialog = null;

window.addEventListener("message", (event)=>{ 
    console.log(event.data);
    if(event.origin == "https://mompww.erstaging.com"){
        if(event.data != undefined && event.data != null){
            var data = JSON.parse(event.data);
			if(data.status != null && data.status == "Success" && data.OFFERCODE != null){
				var easyRewardResponse = data.OFFERCODE;
				var isValidDiscountCategory = false;
				var discountCategoryValue;
				var discountCategory = document.getElementById("discountCategory");
				for (i = 0; i < discountCategory.options.length; i++) {
					if(discountCategory.options[i].text == easyRewardResponse){
						isValidDiscountCategory  = true;
						discountCategoryValue = discountCategory.options[i].value;
					}
				}
				if(!isValidDiscountCategory){
					alert("Invalid Discount Plan");
				}else{
					document.getElementById("discountCategory").value = discountCategoryValue;
					onChangeDiscountCategory();
				}
			}
        }
    }
});

var pendingActionErrorFunctionMap = {
	'updatePendingActionCompletionStatus': [true],
};

// Splits amount as per given proportions.
// Ensures sum of splitted amounts equals the original amount
function splitAmount(amount, proportions, greedy) {
	if(!proportions || !proportions.length){
		return;
	}
	let sum = proportions.reduce( (acc, proportion) => acc + proportion, 0.0);
	let splits = proportions.map( proportion => getAmount((proportion/sum) * amount));
	let splitsSum = splits.reduce((acc, split) => acc + split, 0.0);
	if(greedy){
		splits[0] = getAmount(splits[0] + amount-splitsSum);
	}else{
		splits[splits.length-1] = getAmount(splits[splits.length-1] + amount-splitsSum);
	}

	return splits;
}

function handleSubPageCancelActionNewFlow(subPageWindow) {
	const opener = subPageWindow.opener;
	if (opener) {
		subPageWindow.open('','instaOldBilling').focus();
		subPageWindow.close();
	}
}

function handleSubPageConfirmActionNewFlow(subPageWindow) {
  const opener = subPageWindow.opener;
  if (opener) {
    subPageWindow.open('','instaOldBilling').focus();
    updateIFrameParentHelper({ 'refreshPage': [] });
    opener.location.reload();
    subPageWindow.close();
  }
}

function updateIFrameParentHelper(fnMap) {
	const parentWindow = window.top;
	const activity = parentWindow.activity;
	if (!activity) return;
	Object.keys(fnMap).forEach(function (fnName) {
		var iFrameParentUpdaterMethod = window.updateIFrameParent[fnName];
		if (!iFrameParentUpdaterMethod) return;
		iFrameParentUpdaterMethod.call(null, fnMap[fnName]);
	});
}

function getChargeClosure(chargeRefMap, key) {
	if(!key || !chargeRefMap){
		return null;
	}
	if(!chargeRefMap[key]){
		return [key]
	}

	return chargeRefMap[key].reduce((acc,childCharge) => acc.concat(getChargeClosure(chargeRefMap, childCharge)), [key]);
}

//TODO use these function everywhere else
function getPackageChargeDetails(packageChargeId){
	let comprisingCharges = getChargeClosure(chargeRefMap, packageChargeId)
	return comprisingCharges.map(chargeId => {
		let row = getChargeRowByChargeId(chargeId);
		return {

			rate : parseFloat(getElementByName(row, 'rate').value),
			amt : parseFloat(getElementByName(row, 'amt').value),
			disc : parseFloat(getElementByName(row, 'disc').value),
			conducting_doctor_disc : parseFloat(getElementByName(row, 'disc').value),
			prescribed_doctor_disc : parseFloat(getElementByName(row, 'disc').value),
			referral_doctor_disc : parseFloat(getElementByName(row, 'disc').value),
			hospital_disc : parseFloat(getElementByName(row, 'disc').value),
			tax_amt : parseFloat(getElementByName(row, 'tax_amt').value),
			qty : parseFloat(getElementByName(row, 'qty').value),
			// if (editform.eClaimAmt != null) {
			sponsor_tax : getElementByName(row, 'sponsor_tax') ? parseFloat(getElementByName(row, 'sponsor_tax').value) : null,
			insClaimAmt : getElementByName(row, 'insClaimAmt') ? parseFloat(getElementByName(row, 'insClaimAmt').value) : null,
			priInsClaimAmt : getElementByName(row, 'priInsClaimAmt') ? parseFloat(getElementByName(row, 'priInsClaimAmt').value) : null,
			secInsClaimAmt : getElementByName(row, 'secInsClaimAmt') ? parseFloat(getElementByName(row, 'secInsClaimAmt').value) : null,
			
			charge_id : getElementByName(row, 'chargeId').value,
			chargeHead : getElementByName(row, 'chargeHeadId').value,
			// };
		};
	});
}

function getConsolidatedPackageChargeDetails(packageChargeId){
	var comprisingCharges = getChargeClosure(chargeRefMap, packageChargeId)
	return comprisingCharges.reduce((acc, chargeId) => {

		let row = getChargeRowByChargeId(chargeId);
		acc.rate += getAmount(getElementByName(row, 'amt').value) + getAmount(getElementByName(row, 'disc').value);
		acc.amt += getAmount(getElementByName(row, 'amt').value);
		acc.disc += getAmount(getElementByName(row, 'disc').value);
		acc.tax_amt += getAmount(getElementByName(row, 'tax_amt').value);
		acc.overall_discount_amt += getAmount(getElementByName(row, 'overall_discount_amt').value);
		acc.hospital_disc += getAmount(getElementByName(row, 'hosp_discount_amt').value);
		acc.prescribed_doctor_disc += getAmount(getElementByName(row, 'pres_dr_discount_amt').value);
		acc.conducting_doctor_disc += getAmount(getElementByName(row, 'dr_discount_amt').value);
		acc.referral_doctor_disc += getAmount(getElementByName(row, 'ref_discount_amt').value);

		let taxAmt1Element = document.getElementById(chargeId + '_tax_amt0');
		if(taxAmt1Element){
			acc.tax_amt0 += getAmount(taxAmt1Element.value);
		}

		let taxAmt2Element = document.getElementById(chargeId + '_tax_amt1');
		if(taxAmt2Element){
			acc.tax_amt1 += getAmount(taxAmt2Element.value);
		}
		
		let priSponsorAmtElement = getElementByName(row, 'priInsClaimAmt');
		if(priSponsorAmtElement){
			acc.priInsClaimAmt += getAmount(priSponsorAmtElement.value);
		}

		let secSponsorAmtElement = getElementByName(row, 'secInsClaimAmt');
		if(secSponsorAmtElement){
			acc.secInsClaimAmt += getAmount(secSponsorAmtElement.value);
		}
		
		let secSponsorTaxAmtElement = getElementByName(row, 'secInsClaimTaxAmt');
		if(secSponsorTaxAmtElement){
			acc.secInsClaimTaxAmt += getAmount(secSponsorTaxAmtElement.value);
		}

		let priSponsorTaxAmtElement = getElementByName(row, 'priInsClaimTaxAmt');
		if(priSponsorTaxAmtElement){
			acc.priInsClaimTaxAmt += getAmount(priSponsorTaxAmtElement.value);
		}


		if (editform.eClaimAmt != null) {
			acc.sponsor_tax += getAmount(getElementByName(row, 'sponsor_tax').value);
			acc.insClaimAmt += getAmount(getElementByName(row, 'insClaimAmt').value);
		}
		return acc;
	}, {rate: 0.0, amt: 0.0, tax_amt: 0.0, disc: 0.0, sponsor_tax: 0.0, insClaimAmt: 0.0, overall_discount_amt: 0.0,
		hospital_disc: 0.0, prescribed_doctor_disc: 0.0, conducting_doctor_disc: 0.0, referral_doctor_disc: 0.0,
		tax_amt0: 0.0, tax_amt1: 0.0, priInsClaimAmt: 0.0, secInsClaimAmt: 0.0, priInsClaimTaxAmt: 0.0,
		secInsClaimTaxAmt: 0.0
	})
}

// Gets PKGPKG charge for a given component charge
function isPackageSelected(chargeId){
	let parentChargeId = Object.keys(chargeRefMap).find((key)=>{
		return chargeRefMap[key].includes(chargeId);
	})

	if(parentChargeId){
		let parentIndex = getRowChargeIndex(getChargeRowByChargeId(parentChargeId));
		return getIndexedFormElement(mainform, "discountCheck",parentIndex).checked;
	}
}

//TODO chage function name
function setPackagesDisplay(){
	gPackageChargeDetails = {};
	chargeRefMap = {};
	var num = getNumCharges();
   	var table = document.getElementById("chargesTable");

	var packageMarginCharges = [];
	var chargeDetails = {};

   	for (var i=1; i<=num; i++) {
		var row = table.rows[i];
		var chargeId = getElementByName(row, 'chargeId').value;
		// var packageId = getElementByName(row, 'packageId').value;
		var billDisplayType = getElementByName(row, 'billDisplayType').value;
		var chargeGroup = getElementByName(row, 'chargeGroupId').value;
		var chargeHead = getElementByName(row, 'chargeHeadId').value;
		var chargeRef = getElementByName(row, 'chargeRef').value;
		var rate = getElementByName(row, 'rate').value;
		var disc = getElementByName(row, 'disc').value;
		var amt = getElementByName(row, 'amt').value;
		var tax_amt = getElementByName(row, 'tax_amt').value;
		if (editform.eClaimAmt != null) {
			var sponsor_tax = getElementByName(row, 'sponsor_tax').value;
			var insClaimAmt = getElementByName(row, 'insClaimAmt').value;
		}

		if(chargeRef){
			if(chargeRefMap[chargeRef]){
				chargeRefMap[chargeRef].push(chargeId);
			} else {
				chargeRefMap[chargeRef] = [chargeId];
			}
		}

		if(chargeHead == 'PKGPKG'){
			packageMarginCharges.push(chargeId);
		}

		if(chargeGroup == 'PKG'){
			chargeDetails[chargeId] = {
				rate : rate,
				disc: disc,
				amt: amt,
				tax_amt:tax_amt,
				sponsor_tax: sponsor_tax,
				insClaimAmt: insClaimAmt,
			};
		}
		//hides package components
		if(chargeGroup == 'PKG' && chargeHead !== 'PKGPKG' && chargeHead != 'MARPKG'){
			row.style.display = "none";
		}

	}

	for (var i=1; i<=num; i++) {
		var row = table.rows[i];
		var chargeId = getElementByName(row, 'chargeId').value;
		var billDisplayType = getElementByName(row, 'billDisplayType').value;
		var chargeGroup = getElementByName(row, 'chargeGroupId').value;
		var chargeHead = getElementByName(row, 'chargeHeadId').value;
		var chargeRef = getElementByName(row, 'chargeRef').value;

		if(chargeHead == 'PKGPKG' ){
			

			var packageChargeDetails = getConsolidatedPackageChargeDetails(chargeId);
			
			gPackageChargeDetails[chargeId] = packageChargeDetails;
			if(fromOnload){
				getElementByName(row, "savedRate").value = packageChargeDetails.rate;
			}
			setNodeText(row.cells[RATE_COL], formatAmountValue(packageChargeDetails.rate));
			setNodeText(row.cells[AMT_COL], formatAmountValue(packageChargeDetails.amt));
			setNodeText(row.cells[DISC_COL], formatAmountValue(packageChargeDetails.disc));
			setNodeText(row.cells[TAX_COL], formatAmountValue(packageChargeDetails.tax_amt));
			if (editform.eClaimAmt != null) {
				if(multiPlanExists){
					setNodeText(row.cells[CLAIM_COL], formatAmountValue(packageChargeDetails.insClaimAmt));
					setNodeText(row.cells[SEC_CLAIM_COL], formatAmountValue(packageChargeDetails.secInsClaimAmt));
					setNodeText(row.cells[CLAIM_TAX], formatAmountValue(packageChargeDetails.sponsor_tax));
					setNodeText(row.cells[DED_COL], formatAmountValue(packageChargeDetails.amt - packageChargeDetails.priInsClaimAmt - packageChargeDetails.secInsClaimAmt));
					setNodeText(row.cells[DED_CLAIM_COL], formatAmountValue(packageChargeDetails.tax_amt - packageChargeDetails.sponsor_tax));
					
				}else{
					setNodeText(row.cells[CLAIM_TAX], formatAmountValue(packageChargeDetails.sponsor_tax));
					setNodeText(row.cells[CLAIM_COL], formatAmountValue(packageChargeDetails.insClaimAmt));
					setNodeText(row.cells[DED_COL], formatAmountValue(packageChargeDetails.amt - packageChargeDetails.priInsClaimAmt - packageChargeDetails.secInsClaimAmt));
					setNodeText(row.cells[DED_CLAIM_COL], formatAmountValue(packageChargeDetails.tax_amt - packageChargeDetails.sponsor_tax));
				}
			}
		}


	}

}

function init() {
	fromOnload = true;
	mainform = document.mainform;
	editform = document.editform;
	addform = document.addform;

	if ( (roleId == 1) || (roleId == 2) ) {
		addToBillRights = 'A'; allowDiscount = 'A';
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
	RATE_COL = i++; QTY_COL = i++; UNITS_COL = i++;	DISC_COL = i++;	AMT_COL = i++; TAX_COL = i++;

	if (editform.eClaimAmt != null) {
		if(multiPlanExists) {
			CLAIM_COL = i++;
			SEC_CLAIM_COL = i++;
			CLAIM_TAX = i++;
			DED_COL = i++;
			DED_CLAIM_COL = i++;
			PRE_AUTH_COL = i++;
			SEC_PRE_AUTH_COL = i++;
		} else {
			CLAIM_COL = i++;
			CLAIM_TAX = i++;
			DED_COL = i++;
			DED_CLAIM_COL = i++;
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
	loadDiscAuthArray();
	enableDischargeDate();
	loadBillStatus();
	if(document.mainform.billStatus.value == 'F')
		setOpenDateFldStatus();

	/*
	 * Normally, we should be setting these in the JSP, but since it is invisible, it is OK to
	 * do it here so that the logic is not duplicated here as well as JSP
	 */
	setAllDiscountTitles();

	initDiscountAuthorizerAutoComplete("overallDiscByName","overallDiscByName_dropdown", "overallDiscBy",
			oAutoCompOverall);
	initDiscountAuthorizerAutoComplete("discConductDocName","discConductDocName_dropdown", "discConductDoc",
			oAutoCompConduct);
	initDiscountAuthorizerAutoComplete("discPrescDocName","discPrescDocName_dropdown", "discPrescDoc",
			oAutoCompPresc);
	initDiscountAuthorizerAutoComplete("discRefDocName","discRefDocName_dropdown", "discRefDoc",
			oAutoCompRef);
	initDiscountAuthorizerAutoComplete("discHospUserName","discHospUserName_dropdown", "discHospUser",
			oAutoCompHosp);

	initEditChargeDialog();
	if ( allowAdd )
		initPkgValueCapDialog();

	var doctors = { "doctors": jDoctors };
	var anaList = filterList(jDoctors, 'dept_id', 'DEP0002');
	var anaesthetists = { "doctors": anaList };
	var discauths = { "discount_authorizer": jDiscountAuthorizers };
	var patientId = document.getElementById('visitId').value;

	var orderableItems;
	if (typeof enabledOrderableItemApi !== 'undefined' && enabledOrderableItemApi === true) {
		orderableItems = null;
	} else {
		orderableItems = rateplanwiseitems;
	}

	if (document.getElementById("orderDialogAddDialog") != null) {
		addOrderDialog = new Insta.AddOrderDialog('btnAddItem', orderableItems, null, onAddCharge,
				doctors, anaesthetists, visitType, patientBedType, patientOrgId,
				prescribingDocName, prescribingDoctor, regDate, regTime,
				allowBackDateBillActivities,fixedOtCharges,discauths, forceSubGroupSelection,regPref,anaeTypes,
				patientId,document.mainform.billNo.value );
		addOrderDialog.allowQtyEdit = true;
		addOrderDialog.setInsurance(isTpa, planId);
		addOrderDialog.billOpenDate = openDate+" "+openTime;
		gIsInsurance = isTpa;
	}

	eConductedAutoComp = initConductingDoctorAutoComplete(doctors);

	if (hasDynaPackage) {
		var dynaPkgs = {"dynaPkgs": jDynaPkgNameList};
		initDynaPackageNames(dynaPkgs);
		initDynaPkgValueCapDialog();
		if (document.mainform.dynaPkgId.value != '' && document.mainform.dynaPkgId.value != 0) {
			document.getElementById("dynaPkgBtnTable").style.display = "table";
			document.getElementById("dynaPkgFilterTable").style.display = "table";
			setSelectedIndex(document.mainform.printBill, 'CUSTOM-BUILTIN_HTML');
		}else {
			document.getElementById("dynaPkgBtnTable").style.display = "none";
			document.getElementById("dynaPkgFilterTable").style.display = "none";
		}
	}
	if (isPerDiemPrimaryBill) {
		initPerdiemInclusionsDialog();
		if (document.mainform.per_diem_code) setPerdiemInclDetails();
	}
	initProcedureNames();
	if (document.mainform.dynaPkgName) setPkgValueCaps();
	hasPlanCopayLimit = hasPlanVisitCopayLimit(planBean);

	//resetTotals(false, false);
	/* setFocus(); Called from iframe on load in new UX */
	mrno = document.getElementById("mrno").value;
	!empty(mrno) ? patientInfo = getVisitDoctorConsultationDetails(mrno) : null ;
	document.mainform.showCancelled.checked=true;
	onChangeFilter();
	loadTemplates(null);
	highlightMarkedOnes('onload');
	showAlertRemarks();

	var billStatus = document.mainform.billStatus.value;
	if(allowDiscount != 'N' && billStatus != 'C' && billStatus != 'X')
		selectDiscountAuth();

	if(document.mainform.discountCategory != undefined)
		selectDiscountCategory();
	if(document.mainform.primaryTotalClaim != undefined)
		initialPriInsuranceClaimAmt=document.mainform.primaryTotalClaim.value;
	if(document.mainform.secondaryTotalClaim != undefined)
		initialSecInsuranceClaimAmt=document.mainform.secondaryTotalClaim.value;

	if(isTpa && limitType == 'R' && !empty(isPrimaryBill) && isPrimaryBill == 'Y'){
		var priCaseRateAutoComp = initCaseRateDetails('caserateCode_1', 'caseRateOneDropDown', 1);

		if(caseRateCount == 2){
			var secCaseRateAutoComp = initCaseRateDetails('caserateCode_2','caseRateTwoDropDown',2);
		}

		initCaseRateLimitsDialog();

		resetCaseRateTotals();
	}
	
	setPackagesDisplay();

	fromOnload = false;
}

function initCaseRateDetails(field, dropDown, caseRateNo) {

	var visitID = document.getElementById('visitId').value;
	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/billing/BillAction.do");
	var queryParams = "_method=getCaseRateDetails&visitId="+visitID+"&caseRateNo="+caseRateNo;
    dataSource.scriptQueryAppend = queryParams;

    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    dataSource.responseSchema = {
        resultsList: "result",
        fields: [
	                {key: "code"},
	                {key: "code_description"},
	                {key: "code_type"},
	                {key: "case_rate_id"}
                ]
    };

    var oAutoComp = new YAHOO.widget.AutoComplete(field, dropDown, dataSource);

    oAutoComp.minQueryLength = 1;
    oAutoComp.forceSelection = true;
    oAutoComp.allowBrowserAutocomplete = false;
    oAutoComp.resultTypeList = false;
    oAutoComp.maxResultsDisplayed = 50;
    var reArray = [];
    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
        var escapedComp = Insta.escape(sQuery);
        reArray[0] = new RegExp('^' + escapedComp, 'i');
        reArray[1] = new RegExp("\\s" + escapedComp, 'i');

        var title = oResultData.code + ' / ' + oResultData.code_description;
        var det = highlight(oResultData.code + ' / ' + oResultData.code_description, reArray);

        var span = document.createElement('span');
    	span.setAttribute("title", title);
    	span.innerHTML = det;
    	var div = document.createElement('div');
    	div.appendChild(span);
    	return div.innerHTML;
    };
    oAutoComp.setHeader(' Code / Description ');

    oAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
    	YAHOO.util.Dom.get("caserateCode_"+caseRateNo).value = aArgs[2].code+'/'+aArgs[2].code_description;
    	YAHOO.util.Dom.get("case_rate_id"+caseRateNo).value = aArgs[2].case_rate_id;
    });

    oAutoComp.selectionEnforceEvent.subscribe(function(aArgs) {
		YAHOO.util.Dom.get("caserateCode_"+caseRateNo).value = '';
		YAHOO.util.Dom.get("case_rate_id"+caseRateNo).value = '';
	});

    return oAutoComp;
}

var caseRateDetDialog;
function initCaseRateLimitsDialog() {
	document.getElementById('caseRateDetDialog').style.display = 'block';
	caseRateDetDialog = new YAHOO.widget.Dialog('caseRateDetDialog', {
		width:"200px",
    	context:["caseRateDetDialog","tr","br"],
        visible: false,
        modal: true,
        constraintoviewport: true,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handleCaseRateLimitsDialogCancel,
                                               scope:caseRateDetDialog,
                                               correctScope:true } );
    caseRateDetDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
    caseRateDetDialog.cancelEvent.subscribe(handleCaseRateLimitsDialogCancel);
    caseRateDetDialog.render();
}

function handleCaseRateLimitsDialogCancel(){
	var crTable = document.getElementById("caseRateDetailsTab");
	var len = crTable.rows.length;
	for(var i=len-1; i>0; i--){
		crTable.deleteRow(i);
	}
	caseRateDetDialog.hide();
}

function showCaseRateDetDialog(caseRateNo){
	var caseRate ='';
	var caseRateButton = null;
	if(caseRateNo == 1){
		caseRate = document.getElementById("case_rate_id1").value;
		caseRateButton = document.getElementById("caseRateOneDet");
	}else if(caseRateNo == 2){
		caseRate = document.getElementById("case_rate_id2").value;
		caseRateButton = document.getElementById("caseRateTwoDet");
	}
	var caseRateCatLimits = getCaseRateCategoryLimits(caseRateNo);

	if(null != caseRateCatLimits){
		var crTable = document.getElementById("caseRateDetailsTab");
		var totAmtPaise = 0;
		for(var i=0; i<caseRateCatLimits.length; i++){
			var len = crTable.rows.length;
			var templateRow = crTable.rows[len-1];
			var row = '';
			row = templateRow.cloneNode(true);
			YAHOO.util.Dom.insertAfter(row, templateRow);

			var el = row.cells[0];
			el.setAttribute("class", "formlabel");
			setNodeText(el, caseRateCatLimits[i].insurance_category_name);
			el = row.cells[1];
			el.setAttribute("class", "formlabel");
			setNodeText(el, caseRateCatLimits[i].amount);

			totAmtPaise += getPaise(caseRateCatLimits[i].amount);
		}

		var totRow = '';
		var tbLen = crTable.rows.length;
		var tempRow = crTable.rows[tbLen-1];
		totRow = tempRow.cloneNode(true);
		YAHOO.util.Dom.insertAfter(totRow, tempRow);

		var el = totRow.cells[0];
		el.setAttribute("class", "formlabel");
		setNodeText(el, "Total");
		el = totRow.cells[1];
		el.setAttribute("class", "formlabel");
		setNodeText(el, formatAmountPaise(totAmtPaise));

	}
	if (caseRateButton != null) {
		caseRateDetDialog.cfg.setProperty("context", [caseRateButton, "tr", "br"], false);
		caseRateDetDialog.show();
	}

}

function getCaseRateCategoryLimits(caseRateNo){
	var visitID = document.getElementById('visitId').value;

	var caseRateId = null;

	if(caseRateNo == 1){
		caseRateId = document.getElementById("case_rate_id1").value;
	}else if(caseRateNo == 2){
		caseRateId = document.getElementById("case_rate_id2").value;
	}

	var url =cpath+"/billing/BillAction.do?_method=getCaseRateCategoryLimits&case_rate_id="+caseRateId+"&visit_id="+visitID;

	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			return eval(reqObject.responseText);
		}
	}
	return null;
}

function processCaseRate(){

	if (chargesAdded > 0 || chargesEdited > 0) {
		showMessage("js.billing.dynapackage.newchargesaddedoredited.savebeforeprocessing");
		return false;
	}

	var priCaseRate = document.getElementById("case_rate_id1").value;
	var secCaseRate = null;

	if(caseRateCount == 2){
		var secCaseRate = document.getElementById("case_rate_id2").value;
		if(priCaseRate == '' && secCaseRate != null && secCaseRate != ''){
			updateIFrameParentHelper(pendingActionErrorFunctionMap);
			showMessage("js.billing.billlist.caserate.process.validation");
			return false;
		}
	}

	document.mainform._method.value = "processCaseRate";
	document.mainform.submit();
}

function resetCaseRateTotals(){
	var visitId = document.getElementById('visitId').value;
	var billNo = document.mainform.billNo.value;
	var caseRateTotals = getCaseRateTotals(visitId, billNo);

	var priCaseRate = document.getElementById("case_rate_id1").value;
	var secCaseRate = null;
	if(caseRateCount == 2){
		secCaseRate	= document.getElementById("case_rate_id2").value;
	}

	var crTbl = document.getElementById('caseRateTotTbl');

	if(null != priCaseRate && priCaseRate != ''){

		var cr1Div = document.getElementById("caseRate1Div");
		cr1Div.style.display = 'block';
		var cr1Tbl = document.getElementById("caseRate1Tbl");

		var priCaseRateTotMap = caseRateTotals[priCaseRate];

		var caseRate1TotCell = document.getElementById("caseRate1Tot");

		setCaseRateCategoryAmounts(cr1Tbl, priCaseRateTotMap, caseRate1TotCell);
	}

	if(null != secCaseRate && secCaseRate != ''){

		var cr2Div = document.getElementById("caseRate2Div");
		cr2Div.style.display = 'block';
		var cr2Tbl = document.getElementById("caseRate2Tbl");

		var secCaseRateTotMap = caseRateTotals[secCaseRate];

		var caseRate2TotCell = document.getElementById("caseRate2Tot");

		setCaseRateCategoryAmounts(cr2Tbl, secCaseRateTotMap, caseRate2TotCell);
	}

}

function setCaseRateCategoryAmounts(tbl, caseRateTotMap, caseRateTotCell){
	var caseRateTotalPaise = 0;

	var orderedCaseRateMap = {};
	Object.keys(caseRateTotMap).sort().forEach(function(key) {
		orderedCaseRateMap[key] = caseRateTotMap[key];
	});


	for (var crCat in orderedCaseRateMap){
	    if (orderedCaseRateMap.hasOwnProperty(crCat)) {
	    	var catRow = tbl.insertRow(tbl.rows.length);
	    	var catCell = catRow.insertCell(-1);
	    	catCell.appendChild(makeLabel(null, crCat));
	    	var amtCell = catRow.insertCell(-1);
	    	amtCell.appendChild(makeLabel(null, formatAmountPaise(getPaise(orderedCaseRateMap[crCat]))));
	    	caseRateTotalPaise += getPaise(orderedCaseRateMap[crCat]);
	    }
	}
	caseRateTotCell.innerHTML = formatAmountPaise(caseRateTotalPaise);
}

function getCaseRateTotals(visitId, billNo){
	var url =cpath+"/billing/BillAction.do?_method=getCaseRateAmountInBill&visitId="+visitId+"&bill_no="+billNo;

	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			eval("var caseRateTotals =" + reqObject.responseText);
			return caseRateTotals;
		}
	}
	return null;
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

/*
 * Forward only bill status changes.
 */
function loadBillStatus() {
	var billStatusObj = mainform.billStatus;
	var len = 0;
	var currentBillStatus = 0;
	if ((billType != 'C' && !billNowTpa) || origBillStatus == 'C' || origBillStatus == 'X') {
		// we don't have a dropdown, only a hidden field is there. Nothing more to do
		return;
	}
	for (var i=0;i<billStatusList.length; i++) {
		if(billStatusList[i].status == origBillStatus) {
			currentBillStatus = billStatusList[i].value;
		}
	}

	billStatusObj.length = 0;
	for(var i=0;i<billStatusList.length; i++) {
		if(billStatusList[i].value >= currentBillStatus) {
			billStatusObj.length = len + 1;
			var option  = new Option(billStatusList[i].text, billStatusList[i].status);
			billStatusObj[len] = option;
			len = len+1;
		}
	}
	setSelectedIndex(billStatusObj,origBillStatus);
}

function setFocus() {
	if (origBillStatus == '') {
		document.getElementById("billNo").focus();
	} else if (billType == 'P' && !billNowTpa && document.getElementById("payDD") != null) {
		document.getElementsByName('totPayingAmt')[0].focus();
	} else if (document.getElementById("btnAddItem")) {
		document.getElementById("btnAddItem").focus();
	} else document.getElementById("billNo").focus();
}

function enableDisableInputs() {

	if ((origBillStatus != "A") || (visitType =='r')
			|| (allowDiscount != 'A')) {
		if (document.mainform.itemDiscPer != null) {
			document.mainform.itemDiscPer.disabled = true;
			document.mainform.itemDiscPerApply.disabled = true;
			document.mainform.itemDiscPerAdd.disabled = true;
		}
	}
	if ((visitType =='r')) {
		if (document.getElementById("saveButton")!=null)
			document.getElementById("saveButton").disabled=true;
		if (document.getElementById("printButton")!=null)
			document.getElementById("printButton").disabled=true;
		if (document.getElementById("printSelect")!=null)
			document.getElementById("printSelect").disabled=true;
	}

	if ((origBillStatus == 'C') || (origBillStatus == 'X')) {
		if(document.mainform.billStatus) document.mainform.billStatus.disabled = true;
		if (document.mainform.primaryClaimStatus)
			document.mainform.primaryClaimStatus.readonly = true;
		if (document.mainform.secondaryClaimStatus)
			document.mainform.secondaryClaimStatus.readonly = true;
		if (document.mainform.claimRecdAmount)
			document.mainform.claimRecdAmount.readOnly = true;
		document.mainform.billDiscountAuth.readOnly = true;
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
	var cannotcancel = getIndexedValue("hasActivity", i) == 'true'
		|| chargeHead == 'PHMED' || chargeHead == 'PHRET'
		|| chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
		|| chargeHead == 'MARPKG' || chargeHead == 'MARPDM'
		|| chargeHead == 'MARDRG' || chargeHead == 'OUTDRG'
		|| chargeHead == 'BPDRG' || chargeHead =='ADJDRG'
		|| chargeHead == 'APDRG' ;

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
	if (cannotcancel) {
		trashSrc = cpath + '/icons/delete_disabled.gif';		// grey trash icon
	} else if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else if (origBillStatus != 'A'){
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
	// disable all item edits if the bill is not open, or if visit type is r/t
	if ((origBillStatus != 'A') || (visitType =='r') )
		return false;
	if (editBillRights != 'A')
		return false;
	return true;
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
         || chargeHead == 'MARDRG' || chargeHead == 'OUTDRG'
         || chargeHead == 'BPDRG' || chargeHead == 'ADJDRG'
         || chargeHead == 'APDRG') {
		return false;
	}

	if(editDate != 'A' && roleId != 1 && roleId !=2)
		return false;

	return true;
}

function isDeletable(i) {
	if (!isBillItemEditable())
		return false;
	if (cancelElementsInBillRights != 'A')
		return false;
	if (getIndexedValue("hasActivity", i) == 'true')
		return false;
	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
			|| (chargeHead == 'INVITE' && getIndexedValue("hasActivity", i) == 'true') || chargeHead == 'INVRET') {
		return false;
	}

	var pkgId = document.mainform.dynaPkgId;
	if (chargeHead == 'MARPKG') {
		if (pkgId && pkgId.value != '' && pkgId.value != 0) return false;
	}

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

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'BIDIS' || chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX'
			|| chargeHead == 'MARPKG' || chargeHead == 'MARPDM'
			|| chargeHead == 'MARDRG' || chargeHead == 'OUTDRG'
			|| chargeHead == 'ADJDRG' || chargeHead == 'BPDRG'
			|| chargeHead == 'APDRG' ) {
		return false;
	}
	return true;
}

function isQtyEditable(i) {
	if (!isBillItemEditable())
		return false;

	if (getIndexedValue("hasActivity", i) == 'true')
		return false;
	if (getIndexedValue("delCharge", i) == 'true')
		return false;
	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'BIDIS' || chargeHead == 'PHCMED' || chargeHead == 'PHCRET'
			|| chargeHead == 'INVITE' || chargeHead == 'INVRET'
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX'
			|| chargeHead == 'MARPKG' || chargeHead == 'MARPDM'
			|| chargeHead == 'MARDRG' || chargeHead == 'OUTDRG'
			|| chargeHead == 'BPDRG' || chargeHead == 'ADJDRG'
			|| chargeHead == 'APDRG') {
		return false;
	}
	return true;
}

function isDiscountEditable(i) {
	if ((origBillStatus != 'A') || (visitType =='r') )
		return false;
	if (allowDiscount != 'A')
		return false;
	if (getIndexedValue("delCharge", i) == 'true')
		return false;
	if (getIndexedValue("allowDiscount", i) == 'false')
		return false;

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'PHCMED' || chargeHead == 'PHCRET' || chargeHead == 'PHMED' || chargeHead == 'PHRET'
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX'
			|| chargeHead == 'MARPKG' || chargeHead == 'MARPDM'
			|| chargeHead == 'MARDRG' || chargeHead == 'OUTDRG'
			|| chargeHead == 'BPDRG' || chargeHead == 'APDRG'
			|| chargeHead == 'ADJDRG') {
		return false;
	}
	return true;
}

function isDiscountCatgoryApplicable(discountCategory,i){

	if(discountCategory != undefined && ( visitDiscountPlanId > 0 || !discountCategory.disabled )){
		if (getIndexedValue("allowDiscount", i) == 'true')
			return true;
	}
	return false;
}

function isConductedByEditable(i) {
	if (!isBillItemEditable())
		return false;
	if (getIndexedValue("docPaymentId", i) != '')	// is the doctor already paid?
		return false;
	if (getIndexedValue("activityConducted", i) == 'N')		// does it require conduction?
		return false;
	return true;
}

function isChargeAmountIncludedEditable(i) {

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
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX' || chargeHead == 'PHCMED'
			|| chargeHead == 'PHCRET' ) {
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
			|| chargeHead == 'BSTAX' || chargeHead == 'CSTAX' || chargeHead == 'PHCMED'
			|| chargeHead == 'PHCRET' ) {
		return false;
	}

	if (getIndexedValue("delCharge", i) == 'true')
		return false;

	return true;
}

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
			|| chargeHead == 'MARPDM' || chargeHead == 'BPDRG'
			|| chargeHead == 'APDRG' || chargeHead == 'ADJDRG'
			|| chargeHead == 'INVRET')
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
	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'INVRET' || chargeHead == 'MARDRG' || chargeHead == 'OUTDRG' || chargeHead == 'MARPDM'
		|| chargeHead == 'BPDRG' || chargeHead == 'APDRG' || chargeHead == 'ADJDRG') {
		return false;
	}
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

function validateDiscountWithDynaPackage() {
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

/*
 * Calculate additional discount based on the Amount column, add it to existing discount
 */
function onAddItemDiscPer() {

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
	var isDiscLimitReached = false ;
	for (var i=0;i<getNumCharges();i++) {
		if (getIndexedValue("chargeHeadId",i) == 'BIDIS' || getIndexedValue("chargeHeadId",i) == 'PKGPKG')
			continue;
		if (!isDiscountEditable(i))
			continue;
		if (!(getIndexedFormElement(mainform, "discountCheck",i).checked || isPackageSelected(getIndexedValue("chargeId",i))))
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
		if(isUserLimitExceedInAddDisc(i, discPaise)) {
			isDiscLimitReached = true;
			continue;
		}
		if(checkUserDiscountLimit(i,discPaise)) {
			isDiscLimitReached = false;
			alert(getString("js.billing.billlist.discountnot.greaterthanuserlimit")+userLimit);
			break;
		}
		setIndexedValue("isSystemDiscount", i,'N');
		setIndexedValue("isSystemDiscountOld", i,'N');
		setNodeText(getChargeRow(i).cells[DISC_COL], formatAmountPaise(discPaise));
		discObj.value = formatAmountPaise(discPaise);
		setIndexedValue("overall_discount_amt", i, formatAmountPaise(discPaise));

		var claim = 0;
		if (editform.eClaimAmt != null)
			claim = getPaise(getIndexedValue("insClaimAmt", i));

		onChangeDiscount(i);

		if (editedAmounts(i, claim))
			claimAmtEdited = true;

		var chargeId = getIndexedValue("chargeId",i);

		var subGroupIds = document.getElementsByName(chargeId+"_sub_group_id");

		var subGroupIdValues = [];

		for(var l=0; l<subGroupIds.length; l++){
			subGroupIdValues[l] = subGroupIds[l].value;
		}

		getTaxDetails(getChargeRow(i), getIndexedValue("chargeId", i), getIndexedValue("chargeHeadId",i), getIndexedValue("chargeGroupId",i),
				getIndexedValue("descriptionId",i), getIndexedValue("amt",i), getIndexedValue( "consultation_type_id",i),
				getIndexedValue( "op_id",i), subGroupIdValues, mr, billNumber);
	}
	if(isDiscLimitReached) {
		alert(getString("js.billing.billlist.discountnot.greaterthan.user.permissible.apply")+userPermissibleDiscount+"% for all items selected.");
	}

	var visitID = document.getElementById('visitId').value;
	if(tpaBill) {
		getBillChargeClaims(visitID, document.mainform);
	}

	resetTotals(claimAmtEdited, false);
	// setPackagesDisplay();
}

function selectedItemsForDiscount() {
	for (var i=0;i<getNumCharges();i++) {
		if (getIndexedFormElement(mainform, "discountCheck",i).checked)
		return true;
	}
	return false;
}

function onChangeFilter(filterObj) {
	var filterGroup = mainform.filterServiceGroup.value;
	var filterHead = mainform.filterChargeHead.value;
	var filterPackage = mainform.filterPackage;

	filterCharges();
	resetSelectedDiscountItems();
	resetTotals(false, false);

	if (filterGroup != '' || filterHead != '' || (filterPackage != null && filterPackage.value != '') ) {

		if (filterObj) YAHOO.util.Dom.addClass(filterObj, 'filterActive');
		document.getElementById("filterRow").style.display = 'table-row';

	}else {
		if (filterObj) YAHOO.util.Dom.removeClass(filterObj, 'filterActive');
		document.getElementById("filterRow").style.display = 'none';
	}

	//to re select discount plan eligible charges.
	if(document.mainform.discountCategory != undefined)
		selectDiscountCategory();
	
	setPackagesDisplay();
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

function checkNegative(){
	var rateObj = editform.eRate;
	var id = editform.editRowId.value;
	var savedPaise = getIndexedPaise("savedRate", id);
	if(rateObj.value < 0)
	{
		alert("Rate cannot be negative");
		rateObj.value = formatAmountPaise(savedPaise);
		return false;
	}
	return true;
}

function setEditedAmounts(i, row, rate, qty, disc, amt, claim, deduction, taxAmt, taxClaim, taxDeduction) {
	var table = YAHOO.util.Dom.getAncestorByTagName(row, 'table');
	setIndexedValue("rate", i, formatAmountValue(rate));
	setNodeText(row.cells[RATE_COL], formatAmountValue(rate));
	setIndexedValue("qty",i, formatAmountValue(qty, true));
	setNodeText(row.cells[QTY_COL], formatAmountValue(qty, true));
	setIndexedValue("disc",i, formatAmountValue(disc));
	setIndexedValue("overall_discount_amt", i, formatAmountValue(disc));
	setNodeText(row.cells[DISC_COL], formatAmountValue(disc), 17, getDiscountTitle(i));
	setIndexedValue("amt",i, formatAmountValue(amt));
	setNodeText(row.cells[AMT_COL], formatAmountValue(amt));

	var chargeGroup = getIndexedValue("chargeGroupId", i)

	if(chargeGroup != 'MED' && chargeGroup != 'RET'){
		setIndexedValue("tax_amt",i, formatAmountValue(taxAmt));
		setNodeText(row.cells[TAX_COL], formatAmountValue(taxAmt));
	}

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
			if(taxClaim != undefined)
				setNodeText(row.cells[CLAIM_TAX], formatAmountValue(taxClaim));
			if(taxDeduction != undefined)
				setNodeText(row.cells[DED_CLAIM_COL], formatAmountValue(taxDeduction));
		}
		setIndexedValue("insClaimAmt", i, formatAmountValue(claim));
		setIndexedValue("priInsClaimAmt", i, formatAmountValue(claim));
		setIndexedValue("insDeductionAmt", i, formatAmountValue(deduction));
		if(taxClaim != undefined)
			setIndexedValue("sponsor_tax", i, formatAmountValue(taxClaim));
	}

	var chargeHead = getIndexedValue("chargeHeadId", i);
	if (chargeHead == 'BIDIS' || chargeHead == 'ROF' || chargeHead == 'CSTAX' || chargeHead == 'BSTAX') {
		var dynaPkgId = (document.mainform.dynaPkgId != null) ? (document.mainform.dynaPkgId.value) : 0;
		if (!empty(dynaPkgId) && dynaPkgId != 0) {
			setIndexedValue("chargeExcluded", i, "Y");
			setIndexedValue("dynaPackageExcluded", i, "Y");
		} else {
			setIndexedValue("chargeExcluded", i, "N");
			setIndexedValue("dynaPackageExcluded", i, "N");
		}
	}

	if ( !fromOnload ){
		setIndexedValue("edited", i, 'true');
		chargesEdited++;
	}
	setRowStyle(i);

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

	var secPreAuthId = editform.eSecPreAuthId.value;
	var secPreAuthMode = editform.eSecPreAuthModeId.value;

	setNodeText(row.cells[SEC_PRE_AUTH_COL],secPreAuthId);
	setIndexedValue("secPreAuthId", i, secPreAuthId);
	setIndexedValue("secPreAuthModeId", i, secPreAuthMode);

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

function validateChargeHeadExists(chargehd) {
	for (var i=0;i<getNumCharges();i++) {
		var chargeHead = getIndexedValue("chargeHeadId",i);
		if (chargeHead == chargehd) {
			return i;
		}
	}
	return 0;
}

function onAddCharge(order) {
	if (order.itemType == 'Bed' || order.itemType == 'Equipment') {
		if (order.remarks == undefined || order.remarks == '') {
			order.remarks = order.fromDate + ' to ' + order.toDate;
		}
	}

	if (order.itemType == 'Direct Charge' && order.itemId == 'CSTAX') {
		if (tpaBill) {
			var id = validateChargeHeadExists('CSTAX');
			if (id != 0) {
				var msg=getString("js.billing.billlist.claimservicetaxincluded");
				msg+="\n";
				msg+=getString("js.billing.billlist.ifcancelled.uncanceltorecalculatetax");
				alert(msg);
				return false;
			}
			// need to calculate the amount and description based on values in the screen.
			var charge = order.rateDetails[0];
			charge.actDescription =getString('js.billing.billlist.servicetax.claim');
		} else {
			showMessage("js.billing.billlist.claimservicetax.notapplicable");
			return true;
		}
	}

	if (order.itemType == 'Direct Charge' && order.itemId == 'BSTAX') {
		var id = validateChargeHeadExists('BSTAX');
		if (id != 0) {
			var msg=getString("js.billing.billlist.servicechargeincluded");
			msg+="\n";
			msg+=getString("js.billing.billlist.ifcancelled.uncanceltorecalculatecharge");
			alert(msg);
			return false;
		}
		// need to calculate the amount and description based on values in the screen.
		var charge = order.rateDetails[0];
		charge.actDescription = getString('js.billing.billlist.servicetax.totalamount');
	}

	if (allowDiscount != 'A' && order.itemType == 'Direct Charge' && order.itemId == 'BIDIS') {
		showMessage("js.billing.billlist.notauthorizedtogivediscount");
		return false;
	}
	if(order.itemType == 'Direct Charge' && order.itemId == 'BIDIS') {
		var billChargeDisc = order.rateDetails[0];
		if(getPaise(userLimit) - getPaise(billChargeDisc.discount) < 0) {
			alert(getString("js.billing.billlist.discountnot.greaterthanuserlimit")+userLimit);
			return false;
		}
	}
	if (order.rateDetails == null)
		return true;

	var refId = "";
	var chargeId = "";
	for (var i=0; i<order.rateDetails.length; i++) {
		var charge = order.rateDetails[i];

		charge.prescribingDrId = order.presDoctorId;
		charge.payeeDoctorId = empty(order.condDoctorId) ? charge.payeeDoctorId : order.condDoctorId;

		charge.eligible_to_redeem_points =
			empty(order.eligible_to_redeem_points) ? charge.eligible_to_redeem_points : order.eligible_to_redeem_points;
		charge.redemption_cap_percent =
			empty(order.redemption_cap_percent) ? charge.redemption_cap_percent : order.redemption_cap_percent;

		if(order.itemType == 'Operation') {
			charge.op_id = order.itemId;
		}

		if (charge.chargeHead == 'MISOTC' || charge.chargeHead == 'BIDIS') {
			// need to calculate the insurance claim amount ourselves
			var chargeHead = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead);
			charge.insuranceCategoryId = chargeHead.INSURANCE_CATEGORY_ID;

			var amtPaise = getPaise(charge.actRate)*charge.actQuantity - getPaise(charge.discount);
			charge.amount = getPaiseReverse(amtPaise);
			charge.insuranceClaimAmount = getPaiseReverse(
					getClaimAmount(charge.chargeHead, amtPaise,getPaise(charge.discount), charge.insuranceCategoryId));

		} else if (charge.chargeHead == 'CSTAX') {
			var chargeHead = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead);
			charge.insuranceCategoryId = chargeHead.INSURANCE_CATEGORY_ID;
			var amtPaise = getPaise(charge.actRate)*charge.actQuantity - getPaise(charge.discount);
			charge.insuranceClaimAmount = amtPaise;

		} else if (charge.chargeHead == 'BSTAX') {
			var chargeHead = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead);
			charge.insuranceCategoryId = chargeHead.INSURANCE_CATEGORY_ID;
			var amtPaise = getPaise(charge.actRate)*charge.actQuantity - getPaise(charge.discount);
			charge.insuranceClaimAmount = amtPaise;
		}

		if (i > 0)
			refId = chargeId;

		var	id = addChargeToTable(charge, refId,order.preAuthNo,
				order.fromDate.trim()+" "+order.fromTime.trim(),
				order.toDate.trim()+" "+order.toTime.trim(),order.packageId, order.preAuthModeNo,
				order.secPreAuthNo, order.secPreAuthModeNo, order.presDate);

		if (i == 0)
			chargeId = id;
	}

	if (order.itemType == 'Operation')
		clearOperAnaesthesiaDetailsTable();

	return true;
}

function clearOperAnaesthesiaDetailsTable() {
	var table= document.getElementById('anaestiatistTypeTable');
	if(table && table.rows.length > 0) {
		for (var i=table.rows.length-1;i>1;i--) {
			table.deleteRow(i);
		}
	}
	document.getElementById('anesthesia_type0').value = '';
	document.getElementById('anes_start_date0').value = '';
	document.getElementById('anes_end_date0').value = '';
	document.getElementById('anes_start_time0').value = '';
	document.getElementById('anes_end_time0').value = '';
	document.getElementById('addOpAnaesDetailsFieldSet').style.display  = 'none';
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
		actRatePlanItemCode: actRatePlanItemCode, orderNumber: 0, allowDiscount: true,
		allowRateVariation: true, discauthId: "",
		conducting_doc_mandatory: false, chargeExcluded: "N", consultation_type_id: 0,op_id:"",
		insuranceCategoryId: -1, insuranceClaimAmount: 0, codeType: "",
		eligible_to_redeem_points: "N", redemption_cap_percent: 0};

	return addChargeToTable(charge, null, null);
}

function addChargeToTable(charge, refId, preAuthId) {
	addChargeToTable(charge, refId, preAuthId, "", "","",null,null,null,null);
}

function addChargeToTable(charge, refId, preAuthId, fromDate, toDate,multiVisitPackageId, preAuthMode, secPreAuthId, secPreAuthMode, presDate) {
	var chargeHead = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead);
	var headText = chargeHead.CHARGEHEAD_NAME;
	var insPayable = chargeHead.INSURANCE_PAYABLE;
	var insClaimTaxable = chargeHead.CLAIM_SERVICE_TAX_APPLICABLE;
	var serviceChrgApplicable = chargeHead.SERVICE_CHARGE_APPLICABLE;
	var groupText = findInList(jChargeGroups, "CHARGEGROUP_ID", charge.chargeGroup).CHARGEGROUP_NAME;
	
	var chMappedSerSubGroupId = chargeHead.SERVICE_SUB_GROUP_ID;

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
	if(null == charge.isSystemDiscount || charge.isSystemDiscount == 'Y') {
		setHiddenValue(id, "isSystemDiscount",'Y');
		setHiddenValue(id, "isSystemDiscountOld",'Y');
	} else {
		setHiddenValue(id, "isSystemDiscount",'N');
		setHiddenValue(id, "isSystemDiscountOld",'N');
		var userLimitForItem = formatAmountPaise((getPaise(charge.actRate)*charge.actQuantity*userPermissibleDiscount)/100);
		var discountOfItem = formatAmountValue(charge.discount);
		if(Number(userLimitForItem) < Number(discountOfItem)){
			alert(getString("js.billing.billlist.discountnot.greaterthanuserlimit")+userLimit);
			charge.discount =0;
		} else if(Number(userLimit) + Number(userLimitForItem) -Number(discountOfItem) < 0){
			alert(getString("js.billing.billlist.discountnot.greaterthanuserlimit")+userLimit);
			charge.discount =0;
		}
	}
	if(charge.chargeHead == "BIDIS" ) {
		setHiddenValue(id, "isSystemDiscount",'N');
		setHiddenValue(id, "isSystemDiscountOld",'N');
	}
	var rateStr = formatAmountValue(charge.actRate);
	var discStr = formatAmountValue(charge.discount);
	var amtStr = formatAmountPaise(getPaise(charge.actRate)*charge.actQuantity - getPaise(charge.discount));
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
	setHiddenValue(id, "packageId", charge.packageId);
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
	setHiddenValue(id, "allowDiscount", charge.allowDiscount);
	setHiddenValue(id, "allowRateVariation", charge.allowRateVariation);
	setHiddenValue(id, "discount_auth_dr", 0);
	setHiddenValue(id, "discount_auth_pres_dr", 0);
	setHiddenValue(id, "discount_auth_ref", 0);
	setHiddenValue(id, "discount_auth_hosp", 0);
	setHiddenValue(id, "consultation_type_id", charge.consultation_type_id);
	setHiddenValue(id, "op_id", charge.op_id);
	setHiddenValue(id, "insuranceCategoryId", charge.insuranceCategoryId);
	setHiddenValue(id, "service_sub_group_id", charge.serviceSubGroupId);
	
	if (charge.chargeHead == 'ROF' || charge.chargeHead == 'MARPKG') {
		setHiddenValue(id, "service_sub_group_id", chMappedSerSubGroupId);
	}
	
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

	if (charge.discount > 0) {
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
	} else {
		setHiddenValue(id, "overall_discount_auth_name", '');
		setHiddenValue(id, "overall_discount_auth", 0);
		setHiddenValue(id, "overall_discount_amt", 0);
	}

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
		setHiddenValue(id, "dynaPackageExcluded", "N");

	} else if (charge.chargeHead == 'ROF' || charge.chargeHead == 'BIDIS'
				|| charge.chargeHead == 'CSTAX' || charge.chargeHead == 'BSTAX') {
		// round offs, service tax and discounts are usually outside the package
		(!empty(packageId) && packageId != 0)
			? setHiddenValue(id, "chargeExcluded", "Y")
			: setHiddenValue(id, "chargeExcluded", "N");
		(!empty(packageId) && packageId != 0)
			? setHiddenValue(id, "dynaPackageExcluded", "Y")
			: setHiddenValue(id, "dynaPackageExcluded", "N");

		setHiddenValue(id, "amount_included", 0);
		setHiddenValue(id, "qty_included", 0);
	} else {
		// use supplied value
		(!empty(packageId) && packageId != 0)
			? setHiddenValue(id, "chargeExcluded", "Y")
			: setHiddenValue(id, "chargeExcluded", "N");
		(!empty(packageId) && packageId != 0)
			? setHiddenValue(id, "dynaPackageExcluded", "Y")
			: setHiddenValue(id, "dynaPackageExcluded", "N");

		// TODO: Need to set if automatic update of package processing to be done.
		setHiddenValue(id, "amount_included", 0);
		setHiddenValue(id, "qty_included", 0);
	}

	setRowStyle(id);

	row.className = "added";

	var claimAmtEdited = false;

	var visitID = document.getElementById('visitId').value;
	chargesAdded++;

	var discountCategory = document.mainform.discountCategory;

	if(discountCategory != undefined && discountCategory.value != 0 && discountCategory.value != '') {
		selectDiscountCategory();
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

	var subGroupIdValues = [];
	getTaxDetails(row, id, charge.chargeHead, charge.chargeGroup, charge.actDescriptionId, amtStr, charge.consultation_type_id, charge.op_id, subGroupIdValues,
			mr, billNumber);

	if(tpaBill) {
		var insPayable = findInList(jChargeHeads, "CHARGEHEAD_ID", charge.chargeHead).INSURANCE_PAYABLE;
		getBillChargeClaims(visitID, document.mainform);

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

	//enable/disable mark for writeoff checkbox
	enableWriteOff();
	if (billStatus == 'C') {
		// also set payment status as paid and claim status as Received.
		setSelectedIndex(paymentStatusObj, 'P');
		paymentStatusObj.disabled = true;
		onChangePaymentStatus();

		if (priClaimStatusObj != null) {
			setSelectedIndex(priClaimStatusObj, 'C');
			priClaimStatusObj.readonly = true;
			document.mainform.priClaimStatusCheck.value='C';
			if (priClaimAmountObj != null)
				priClaimAmountObj.readOnly = true;
			onChangeClaimStatus();
		}

		if (secClaimStatusObj != null) {
			setSelectedIndex(secClaimStatusObj, 'C');
			secClaimStatusObj.readonly = true;
			document.mainform.secClaimStatusCheck.value='C';
			if (secClaimAmountObj != null)
				secClaimAmountObj.readOnly = true;
			onChangeClaimStatus();
		}

	} else {
		paymentStatusObj.disabled = false;
		if (priClaimStatusObj != null) {
			document.mainform.priClaimStatusCheck.value='O';
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
			document.mainform.secClaimStatusCheck.value='O';
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
	if (allowBackDateBillActivities == 'A' || roleId == 1 || roleId == 2) {
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
		if (document.mainform.primaryClaimStatus != null && document.mainform.primaryClaimStatus.value == 'C'
				&& ( document.mainform.secondaryClaimStatus == null
				 || (document.mainform.secondaryClaimStatus != null
				 		&& document.mainform.secondaryClaimStatus.value == 'C'))) {
			var ok = confirm("Bill is finalized and Claim Status is Received. The bill can be closed\n" +
					"Do you want to close the bill?");
			if (ok) {
				setSelectedIndex(document.mainform.billStatus, 'C');
			}
		}
	}
}

function onChangeClaimStatus() {
	if (document.mainform.primaryClaimStatus.value == 'C'
			&& ( document.mainform.secondaryClaimStatus == null
				 || (document.mainform.secondaryClaimStatus != null
				 		&& document.mainform.secondaryClaimStatus.value == 'C'))) {
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
function getDiscountTitle(i) {
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

	if ((origBillStatus == 'A') && (mainform.dynaPkgId != undefined && mainform.dynaPkgId != null)
			&& (mainform.dynaPkgId.value != '') && (mainform.dynaPkgId.value != 0)) {
		setPkgValueCaps();
		setPackageMarginAmount();
	}

	var claimableTotalPaise = 0;
	var serviceChargeableTotalPaise = 0;
	var patientDueAmount = 0;

	totDiscPaise = 0;
	userLimitPaise = 0;
	usrLimitForCalc = 0;
	authChange = false;
	discChange = false;
	qtyChange = false;
	rateChange = false;
	totAmtPaise = 0;
	totTaxPaise = 0;
	totInsTaxPaise = 0;
	totPatTaxPaise = 0;
	totAmtDuePaise = 0;
	totInsAmtPaise = 0;
	totSpnrAmtDuePaise = 0;
	totInsuranceClaimAmtPaise = 0;
	totPriInsuranceClaimAmtPaise = 0;
	totSecInsuranceClaimAmtPaise = 0;
	totCopayAmtPaise = 0;

	subTotAmtPaise = 0;
	subTotDiscPaise = 0;
	subTotTaxPaise = 0;

	eligibleRewardPointsPaise = 0;
	var table = document.getElementById("chargesTable");

	// calculate the totals: exclude claim and round off.
	for (var i=0;i<getNumCharges();i++) {
		var delCharge = getIndexedFormElement(mainform, "delCharge", i);
		if (delCharge && "true" == delCharge.value) {
			continue;
		}

		var chargeHead = getIndexedValue("chargeHeadId", i);
		if (chargeHead == 'ROF' || chargeHead == 'CSTAX' ) {
			// we'll deal with it later.
			if( chargeHead == 'ROF' ){
                userLimitPaise += getIndexedPaise("amt",i)*userPermissibleDiscount/100;
			}
			continue;
		}
		if(chargeHead != 'PHCMED' && chargeHead != 'PHCRET' && chargeHead != 'BIDIS') {
			if(getIndexedValue("isSystemDiscount", i)=='N'){
				var userDiscLimitPaise = (getIndexedPaise("disc", i) + getIndexedPaise("amt",i) + getIndexedPaise("tax_amt",i))*userPermissibleDiscount/100;
				var userGivenDiscPaise = getIndexedPaise("disc", i);
				if(userGivenDiscPaise <= userDiscLimitPaise) {
					userLimitPaise += (((getIndexedPaise("disc", i) + getIndexedPaise("amt",i) + getIndexedPaise("tax_amt",i))*userPermissibleDiscount)/100) - getIndexedPaise("disc", i);
				}
			} else {
				userLimitPaise += (getIndexedPaise("amt",i) + getIndexedPaise("tax_amt",i))*userPermissibleDiscount/100;
			}
		} else {
			if(chargeHead == 'BIDIS') {
				userLimitPaise -= getIndexedPaise("disc", i);
			} else {
				userLimitPaise += (getIndexedPaise("tax_amt",i)+getIndexedPaise("amt",i))*userPermissibleDiscount/100;
			}
		}
		totDiscPaise += getIndexedPaise("disc", i);
		totAmtPaise += getIndexedPaise("amt",i);
		totTaxPaise += getIndexedPaise("tax_amt",i);
		totInsTaxPaise += getIndexedPaise("sponsor_tax",i);

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
			subTotTaxPaise += getIndexedPaise("tax_amt",i);
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

				// add the service charge amount to the total
				if(serChrgInsPayable == 'Y' && isTpa)
					totInsAmtPaise += servAmtPaise;

				totAmtPaise += servAmtPaise;

				if (serChrgInsPayable == 'Y' && isTpa)
					totInsuranceClaimAmtPaise += servAmtPaise;
			}
		}

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
			var newInsRoundOffPaise = getRoundOffPaise(totInsAmtPaise + totInsTaxPaise);
			var newRoundOffPaise = getRoundOffPaise(totAmtPaise + totTaxPaise);

			if (newRoundOffPaise != roundOffPaise || (newInsRoundOffPaise != insRoundOffPaise && isTpa)) {
				// reset claim amounts
				claimAmtEdited = claimAmtEdited && (newInsRoundOffPaise != insRoundOffPaise);

				// update the row
				var row = getChargeRow(roundOffRowId);
				var roundOff = formatAmountPaise(newRoundOffPaise);
				var insRoundOff = formatAmountPaise(newInsRoundOffPaise);
				var patientRoundOff = formatAmountPaise(newRoundOffPaise - newInsRoundOffPaise);

				setEditedAmounts(roundOffRowId, row, roundOff, 1, 0, roundOff, insRoundOff, patientRoundOff, 0, 0, 0);
				roundOffPaise = newRoundOffPaise;
				insRoundOffPaise = newInsRoundOffPaise;
			}
		}
		if(isTpa){
			setIndexedValue("priIncludeInClaim", roundOffRowId, "false");
			if(multiPlanExists){
				setIndexedValue("secIncludeInClaim", roundOffRowId, "false");
			}
		}


		// add the round off to the total
		totAmtPaise += roundOffPaise;
		totInsAmtPaise += insRoundOffPaise;
		totInsuranceClaimAmtPaise += insRoundOffPaise;
		totPriInsuranceClaimAmtPaise += insRoundOffPaise;
	}

	totCopayAmtPaise += (totAmtPaise - totInsuranceClaimAmtPaise);

	totPatTaxPaise += (totTaxPaise - totInsTaxPaise);

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
	var depositSetOffPaise		= getElementPaise(document.mainform.originalDepositSetOff);
	var rewardPointsPaise		= getElementPaise(document.mainform.rewardPointsRedeemedAmount);

	// If Incoming other hospital bill - part of sponsor consolidated bill then totInsAmtPaise = totAmtPaise
	if (sponsorBillNo != null && sponsorBillNo != '' && visitType == 't' && billType == 'C')
		totInsAmtPaise = totAmtPaise;

	totAmtDuePaise = totAmtPaise + totTaxPaise - totInsAmtPaise - totInsTaxPaise - depositSetOffPaise - rewardPointsPaise - existingReceiptsPaise;
	if ((totAmtDuePaise == 0 && totInsuranceClaimAmtPaise == 0) && billDeductionPaise != 0)
		totAmtDuePaise += billDeductionPaise;
	// Eligible reward points amount.
	var strEligibleAmt = formatAmountPaise(eligibleRewardPointsPaise-rewardPointsPaise); //Substract the already redeemed points/amount
	setNodeText("lblAvailableRewardPointsAmount", strEligibleAmt);

	// Reset the max eligible amount to be redeemed.
	availableRewardPointsAmount = strEligibleAmt;

	// filter totals row : billed amount, discount, net amount
	var strSubNetAmount = formatAmountPaise((subTotAmtPaise + subTotDiscPaise));
	var strSubTotDiscount = formatAmountPaise(subTotDiscPaise);
	var strSubTotal = formatAmountPaise(subTotAmtPaise + subTotTaxPaise);
	var strSubTax = formatAmountPaise(subTotTaxPaise);
	setNodeText("lblFilteredAmount", applySeparator(strSubTotal));
	setNodeText("lblFilteredDisc", applySeparator(strSubTotDiscount));
	setNodeText("lblFilteredTax", applySeparator(strSubTax))
	setNodeText("lblFilteredNetAmt", strSubNetAmount, 0, strSubTotal + " - " + strSubTotDiscount);

	// row 1: billed amount, discount, net amount
	var strTotBilled = formatAmountPaise((totAmtPaise + totDiscPaise));
	var strTotDiscount = formatAmountPaise(totDiscPaise);
	var strNetAmount = formatAmountPaise(totAmtPaise + totTaxPaise);
	var strTaxAmount = formatAmountPaise(totTaxPaise);
	userLimit = formatAmountPaise(userLimitPaise);
	setNodeText("lblTotBilled", applySeparator(strTotBilled));
	setNodeText("lblTotDisc", applySeparator(strTotDiscount));
	setNodeText("lblTotTaxAmt", applySeparator(strTaxAmount))
	setNodeText("lblTotAmt", applySeparator(strNetAmount), 0, strTotBilled + " - " + strTotDiscount);

	//Ins 3.0 var hasUnallocatedAmt = (totInsuranceClaimAmtPaise != 0
								//&& ((totInsuranceClaimAmtPaise - maxClaimAmountPaise) != billDeductionPaise));
	//Ins 3.0 var unallocPaise = totInsuranceClaimAmtPaise - maxClaimAmountPaise;

	// row 2: Patient Amount, [Deposit Set Off], Patient Payments, Patient Due
	var strPatientAmount = 0;
	//Ins 3.0 if (hasUnallocatedAmt)	strPatientAmount = formatAmountPaise(totCopayAmtPaise + unallocPaise);
	// else

	strPatientAmount = formatAmountPaise(totCopayAmtPaise + billDeductionPaise);

	var strPatientTax = 0;

	strPatientTax = formatAmountPaise(totPatTaxPaise);

	var strDepositSetOff = formatAmountPaise(depositSetOffPaise);
	var strRewardPointsAmt = formatAmountPaise(rewardPointsPaise);
	var strPatientDue = formatAmountPaise(totAmtDuePaise );
	var strReceipts = formatAmountValue(existingReceipts);
	var strDeduction = formatAmountPaise(billDeductionPaise);
	var strTotalDeposit = formatAmountPaise(getPaise(totalDeposit));
	var strPatientCredit = formatAmountPaise(getPaise(patientCredit));
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
	if(getPaise(totalDeposit)) {
		setNodeText("lblTotalDeposit", applySeparator(strTotalDeposit));
	}
	if (document.getElementById("lblTotInsAmt") != null)
		setNodeText("lblPatientAmount", applySeparator(strPatientAmount), 0, titlePatientAmt);
	else
		setNodeText("lblPatientAmount", applySeparator(strPatientAmount));

	setNodeText("lblPatientTax", applySeparator(strPatientTax));
	setNodeText("lblDepositsSetOff", applySeparator(strDepositSetOff));
	setNodeText("lblRewardPointsAmt", applySeparator(strRewardPointsAmt));
	setNodeText("lblExistingReceipts", applySeparator(strReceipts));
	setNodeText("lblPatientDue", applySeparator(strPatientDue), 0, titlePatientDue);
	setNodeText("lblPatientCredit", applySeparator(strPatientCredit));
	if(document.mainform.patientCreditNoteAmt != null)
		setNodeText("lblNetPatientDue", applySeparator(formatAmountPaise(getPaise(parseFloat(strPatientDue) + parseFloat(document.mainform.patientCreditNoteAmt.value)))));
	var smscheckBox=document.getElementById("patientDuesmsTd");
	if(smscheckBox!=null){
		if(totAmtDuePaise == 0 || (document.mainform.patientCreditNoteAmt != null && formatAmountPaise(getPaise(parseFloat(strPatientDue) + parseFloat(document.mainform.patientCreditNoteAmt.value)))<=0)){
				smscheckBox.style.display = "none";
		}else{
			smscheckBox.style.display = "block";
		}
	}
	if(document.getElementById("lblNetPatientDue")!= null)
		setNodeText("lblWrittenOffAmt", applySeparator(formatAmountPaise(getPaise(parseFloat(strPatientDue) + parseFloat(document.mainform.patientCreditNoteAmt.value)))));
	else
		setNodeText("lblWrittenOffAmt", applySeparator(strPatientDue));
	// row 3: Sponsor Amount, Sponsor payments, Sponsor Due

	var strInsAmt = formatAmountPaise((totInsAmtPaise));
	var strInsTaxAmt = formatAmountPaise((totInsTaxPaise));
	var strSponsorReceipts = formatAmountValue(existingSponsorReceipts);
	var strSponsorRecdAmount = formatAmountValue(existingRecdAmount);
	totSpnrAmtDuePaise = totInsAmtPaise + totInsTaxPaise - tpaReceiptsPaise;
	var strSponsorDue = formatAmountPaise(totSpnrAmtDuePaise);
	setNodeText("lblTotInsAmt", applySeparator(strInsAmt), 0, strNetAmount + " - " + strPatientDue);
	setNodeText("lblSponsorTax",applySeparator(strInsTaxAmt));
	setNodeText("lblSponsorRecdAmount", applySeparator(strSponsorRecdAmount));
	setNodeText("lblSponsorReceipts", applySeparator(strSponsorReceipts));
	setNodeText("lblSponsorDue", applySeparator(strSponsorDue), 0, strInsAmt + " - " + strSponsorRecdAmount+ " - " + strSponsorReceipts);
	if(document.mainform.sponsorCreditNoteAmt != null){
		setNodeText("lblNetSponsorDue", applySeparator(formatAmountPaise(getPaise(parseFloat(strSponsorDue) + parseFloat(document.mainform.sponsorCreditNoteAmt.value)))));
		if(document.getElementById("lblNetSponsorDue") != null)
			setNodeText("lblSpnrWrittenOffAmt", applySeparator(formatAmountPaise(getPaise(parseFloat(strSponsorDue) + parseFloat(document.mainform.sponsorCreditNoteAmt.value)))));
		else
			setNodeText("lblSpnrWrittenOffAmt", applySeparator(strSponsorDue));
	} else {
		setNodeText("lblSpnrWrittenOffAmt", applySeparator(strSponsorDue));
	}
	resetPayments();
	setPackagesDisplay();
	//requires sponsor due, hence is called here after sponsordue is set.
	enableWriteOff();
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
	if(((billType == 'P') && (origBillStatus == 'A')) && document.mainform.patientCreditNoteAmt != null){
		resetTotalsForCreditNotePayments();
	}else{
		resetTotalsForPayments();
	}
	resetTotalsForSpnsrPayments();
	resetTotalsForDepositPayments();
	resetTotalsForPointsRedeemed();
	// for bill now bill, auto-set the amount to be paid by patient.
	if (((billType == 'P') && (origBillStatus == 'A'))) {
		// if a single payment mode exists, update that with the due amount automatically
		setTotalPayAmount();
	}
}

function getTotalAmount() {
	return getPaise(removeSeparator(document.getElementById("lblPatientAmount").innerHTML));
}

function getTotalAmountDue() {
	return getPaise(removeSeparator(document.getElementById("lblPatientDue").innerHTML));
}
function getTotalNetAmountDue() {
	return getPaise(removeSeparator(document.getElementById("lblNetPatientDue").innerHTML));
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
		return getPaise(removeSeparator(document.getElementById("lblSponsorDue").innerHTML));
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
	//valid = valid && !isBillLocked();
	valid = valid && validatePrescribingDoctor();
	valid = valid && validateDRGCode();
	valid = valid && validatePerdiemCodeChange();
	valid = valid && validateAllNumerics();
	// valid = valid && validateBillRemarks();
	valid = valid && validateBillLabelForBillLaterBills();
	valid = valid && validateCounter();
	valid = valid && validatePaymentRefund();
	valid = valid && validatePaymentTagFields();
	valid = valid && validateClaimStatus();
	valid = valid && validateBillDeduction();
	valid = valid && validateBillFinalizationAndPaymentStatus(false);
	if (allowBillFinalization != 'Y' && origBillStatus != 'F' && document.mainform.billStatus.value == "F") {
		valid = valid && validateBillFinalizationAndPaymentStatus(true);
	}
	valid = valid && validatePatientPayment();
	if ( !eClaimModule ){
		valid = valid && closeClaimStatus();
	}
	valid = valid && validateSponsorPayment();
	valid = valid && validateSponsorBillClose();
	valid = valid && validateCancel();
	valid = valid && validatePayDates();
	valid = valid && validateDiscountAuth();
	valid = valid && validatePaymentType();
	valid = valid && validateDischaregDateTime();
	valid = valid && validatePrimaryClaimAmt();
	valid = valid && validateSecondaryClaimAmt();
	valid = valid && validateTotalClaimAmt();
	valid = valid && validateFinalizedDateAndTime();
	valid = valid && checkConductingDoctor();
	valid = valid && checkFinalization();
	valid = valid && validateDynaPkg();
	//valid = valid && validateInsuranceApprovalAmount();
	valid = valid && validateBillRewardPointsRedeemed();
	valid = valid && validateBillOpenDate();
	valid = valid && validateActivityDate();
	valid = valid && validateCustomFields();
	valid = valid && validateVisitLevelLimits();
	valid = valid && validateOKtoDischargeDate();
	valid = valid && checkTransactionLimitValue();
	valid = valid && checkCreditLimitRule();
	valid = valid && checkPayDateWithBillDate();
	valid = valid && checkDepositExistsAndNotUsed();
	valid = valid && checkPendingTestAndConsultationForBill();
	if(income_tax_cash_limit_applicability == 'Y') {
		var visitId = document.getElementById('visitId').value;
		var mrno = (visitType != 't' && visitType != 'r' ? document.getElementById('mrno').value : null);
			valid = valid && checkCashLimitValidation(mrno,visitId);
	}
	return valid;
}

function validateDRGCode() {
	var billStatus = document.mainform.billStatus.value;
	if (isTpa && !empty(useDRG) && useDRG == 'Y'
		&& (billType == 'C') && (isPrimaryBill == 'Y')
		&& (billStatus == 'F' || billStatus == 'C') && !hasDRGCode) {
		var msg=getString("js.billing.billlist.drgcodeisrequired");
		msg+="\n";
		msg+=getString("js.billing.billlist.adddrgcodetofinalize.closethebill");
		alert(msg);
		return false;
	}
	return true;
}

function isBillLocked() {
	//Check if charge amount is changed OR Any refund receipt is added and If bill is locked 
	// Then, Do not allow to save.
	var isChargesEdited = isNewOrEditedItemsExist(true);
	var hasRefundAmount = (getPayingAmountPaise('refund') < 0) ? true : false;
	var billNo = document.mainform.billNo.value;

	if (isChargesEdited || hasRefundAmount) {
	  var url = cpath + '/billing/getisbilllocked.json?bill_no=' + billNo;

      let ajaxReqObject = newXMLHttpRequest();
	  ajaxReqObject.open("GET",url.toString(), false); //synchronous call
	  ajaxReqObject.send(null);
	  if (ajaxReqObject.readyState == 4) {
	    if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) ) {
	  	  let response = JSON.parse(ajaxReqObject.responseText);
	  	  if (response.isLocked){
	  		alert("Bill save is inprogress, Try after sometime.");
		    return true;
		  }
	  	}
	  }
	}
	return false;
}


function checkCreditLimitRule() {
	var newOrEditItemsExist = isNewOrEditedItemsExist(false);
	//Credit limit rule is applicable for IP visits only
	if(visitType != 'i' || !newOrEditItemsExist || creditLimitDetailsJSON == undefined || creditLimitDetailsJSON == null) {
		return true;
	}

	/**
	 * bill now non tpa bill will get saved along with payment. so we no need to check IP credit limit
	 */
	if(billType == 'P' && !billNowTpa) {
		return true;
	}

	var actualAvailableCreditLimit = creditLimitDetailsJSON.availableCreditLimit;
	var actualVisitDue = creditLimitDetailsJSON.visitPatientDue;

	// visitPatientDuePaise = visit patient excluded bill due and current bill due will make the visit patient due
	var visitPatientDuePaise = getTotalAmountDue() + getPaise(exclVisitPatientDue);

	//get the paying amount
	var payingAmtPaise = getPayingAmountPaise('patient') + getPayingAmountPaise('refund');

	if(payingAmtPaise != null && payingAmtPaise != undefined && payingAmtPaise > 0) {
		visitPatientDuePaise = visitPatientDuePaise - payingAmtPaise;
	}

	//Available Credit Limit = Sanctioned Credit Limit + Sum(Patient Deposits-General) + Sum(Patient Deposits-IP) - Patient Dues.
	var availableCreditLimit = creditLimitDetailsJSON.availableCreditLimitWithoutDue;
	availableCreditLimit = parseFloat(availableCreditLimit) - parseFloat(formatAmountPaise(visitPatientDuePaise));

	if(ip_credit_limit_rule == 'B') {
		if(!(availableCreditLimit >= 0)) {
			var msg=getString("js.billing.billlist.and.below.currentoutstanding");
			msg+=' '+ formatAmountValue(actualVisitDue);
			msg+="\n";
			msg+=getString("js.billing.billlist.ipcreditlimitis");
			msg+=' '+ formatAmountValue(actualAvailableCreditLimit);
			alert(msg);
			return false;
		}
	} else if (ip_credit_limit_rule == 'W') {
		if(!(availableCreditLimit >= 0)) {
			var msg=getString("js.billing.billlist.and.below.currentoutstanding");
			msg+=' '+ formatAmountValue(actualVisitDue) ;
			msg+="\n";
			msg+=getString("js.billing.billlist.ipcreditlimitis");
			msg+=' '+ formatAmountValue(actualAvailableCreditLimit);
			msg+="\n";
			msg+=getString("js.billing.billlist.doyouwanttoproceed");
			var ok = confirm(msg);
			if(!ok)
				return false;
		}
	}
	return true;
}

function isNewOrEditedItemsExist(onlyEdited = false) {
	var chargeIdList = document.getElementsByName("chargeId");
	var editedList = document.getElementsByName("edited");
	for (var i=0; i<chargeIdList.length; i++) {
		if(((chargeIdList[i].value).startsWith("_") && !onlyEdited) || (editedList[i].value) == "true") {
			return true;
		}
	}
	return false;
}

function validateVisitLevelLimits() {
	var billStatus = document.mainform.billStatus.value;
	if(billStatus == 'F' || billStatus == 'C') {
		if(trimAll(visitLevelAlertMsgs) != ''){
			var ok = confirm(visitLevelAlertMsgs + " Do you want to proceed or not..");
			if(!ok)
				return false;
		}

		if(visitAdjExists){
			var ok = confirm("Visit level limits are not adjusted. Do you want to proceed or not..");
			if(!ok)
				return false;
		}
	}
	return true;
}


function validatePerdiemCodeChange() {
	var perdiemCodeObj = mainform.per_diem_code;
	var existingPerdiemCodeObj = mainform.existing_per_diem_code;

	var perdiemCode = !empty(perdiemCodeObj) ? perdiemCodeObj.value : "";
	var existingPerdiemCode = !empty(existingPerdiemCodeObj) ? existingPerdiemCodeObj.value : "";

	if (perdiemCodeObj != null) {
		if (perdiemCode != existingPerdiemCode) {

			var msg=getString("js.billing.billlist.perdiemcodeischanged");
			msg+="\n";
			msg+=getString("js.billing.billlist.changeperdiemcodeaftersave");
			msg+="\n";
			msg+=getString("js.billing.billlist.resettingback.originalperdiemcode");
			alert(msg);
			setSelectedIndex(perdiemCodeObj, existingPerdiemCode);
		}
	}

	var billStatus = document.mainform.billStatus.value;
	if (isTpa && !empty(usePerdiem) && usePerdiem == 'Y'
		&& (billType == 'C') && (isPrimaryBill == 'Y')
		&& (billStatus == 'F' || billStatus == 'C')
		&& (perdiemCodeObj != null && perdiemCodeObj.value == "")) {
		var msg=getString("js.billing.billlist.patientisaperdiempatient");
		msg+="\n";
		msg+=getString("js.billing.billlist.addperdiemcode.finalizeorclosethebill");
		alert(msg);
		return false;
	}
	return true;
}

function validateInsuranceApprovalAmount(){

	var billStatus = document.mainform.billStatus.value;

	if(billStatus != 'F') return true;

	var priInsApprovalAmt = 0;
	var secInsApprovalAmt = 0;
	var priAprAmtObj = document.mainform.primaryApprovalAmount;
	var secAprAmtObj = document.mainform.secondaryApprovalAmount;

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

	if (priAprAmtObj == null)
		return true;		// not an insurance bill


	if (!validateAmount(priAprAmtObj, getString("js.billing.billlist.approvalamount.validamount")))
		return false;

	if (secAprAmtObj != null) {
		if (!validateAmount(secAprAmtObj, getString("js.billing.billlist.approvalamount.validamount")))
		return false;
	}

	if (trimAll(priAprAmtObj.value) == '' && (secAprAmtObj == null || (secAprAmtObj != null && trimAll(secAprAmtObj.value) == ''))) {
		var ok = confirm(" Warning: Approved amount is not given, \n" +
				" validations against approved amount will be disabled. \n " +
				"For blanket approvals, please specify the amount as 0. \n " +
				"Do you want to continue to Save?");
		if (!ok) {
			priAprAmtObj.focus();
			return false;
		} else {
			// no further validations based on approval amount.
			return true;
		}
	}

	priInsApprovalAmt =  getPaise(priAprAmtObj.value);
	secInsApprovalAmt =  secAprAmtObj != null ? getPaise(secAprAmtObj.value) : 0;

	/*
	 * Validate that primary / secondary claim is not more than primary / secondary approval amount
	 * Note: 0 means blanket approval.
	 */
	if ((priInsApprovalAmt != 0) &&  (priClaimPaise > priInsApprovalAmt)) {
		if(!onChangePrimaryApprovalAmt()) return false;
		var msg=getString("js.billing.billlist.primarysponsorclaimamt.notgreaterprimaryamt");
		msg+="\n" ;
		msg+=getString("js.billing.billlist.checkprimaryapprovedamount");
		alert(msg);
		priAprAmtObj.focus();
		return false;
	}

	if (secAprAmtObj != null && (secInsApprovalAmt != 0) &&  (secClaimPaise > secInsApprovalAmt)) {
		if(!onChangeSecondaryApprovalAmt()) return false;
		var msg=getString("js.billing.billlist.secondarysponsorclaimamt.notgreatersecondaryamt");
		msg+="\n";
		msg+=getString("js.billing.billlist.checksecondaryapprovedamount");
		alert(msg);
		secAprAmtObj.focus();
		return false;
	}

	var insApprovalAmt = priInsApprovalAmt + secInsApprovalAmt;

	/*
	 * Validate that the total of all bill's approval amount does not exceed visit approval amt
	 * Note: visit approval = 0 means blanket approval, so no validation per bill is required.
	 */
	if (visitApprovalAmount != 0) {
		if (insApprovalAmt == 0) {
			showMessage("js.billing.billlist.unlimitedapprovedamount.notallowed");
			priAprAmtObj.focus();
			return false;
		}
		var allBillsApprovalAmount = getPaise(otherBillsApprovalTotal) + insApprovalAmt;
		if (allBillsApprovalAmount > getPaise(visitApprovalAmount)) {
			var msg=getString("js.billing.billlist.totalofallbillsapprovedamount");
			msg+=getPaiseReverse(allBillsApprovalAmount);
			msg+=getString("js.billing.billlist.exceedsvisitapprovedamount");
			msg+=visitApprovalAmount;
			msg+= ")";
			alert(msg);
			priAprAmtObj.focus();
			return false;
		}
	}
	return true;
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

function hidePaymentModeType(modeObj) {
	var selPaymentModeValue = modeObj.value;
	var selPaymentModeId = modeObj.id;
	if(selPaymentModeValue === '-8' || selPaymentModeValue === '-6' || selPaymentModeValue === '-7' || selPaymentModeValue === '-9') {
		var numPayments = getNumOfPayments();
		for (i=0; i<numPayments; i++){
			var paymentModeId = "paymentModeId"+i;
			var paymentModelValue = $("#"+paymentModeId+" option:selected").val();

			if(selPaymentModeId == paymentModeId)
				continue;

			if(selPaymentModeValue ==  paymentModelValue) {
				alert("The payment mode is already selected \nPlease select some other payment mode");
				$("#"+selPaymentModeId).val(-1);
				$("#"+selPaymentModeId).trigger("change");
			}

		}
	}
}


function showRewardPoints(modeObj) {
	var paymentModeValue = modeObj.value;
	var index=modeObj.id.substr(-1);
	var paymentModeName = $(modeObj).children(':selected').text();
	var paymentType = getIndexedFormElement(documentForm, "paymentType", index).value;
	if ( (paymentModeValue == -2 || paymentModeValue == -3
			|| paymentModeValue == -5 || paymentModeValue == -9) && paymentType == 'refund') {
		alert('Amount cannot be Refund by ' + paymentModeName);
		$(modeObj).val(-1); //reset to cash
		return false;
	}
	if(paymentModeValue === '-9') {
		$(modeObj).closest("tr").find(".redeemPointsTD").show();
		$("#totPayingAmt"+index).closest("td").find(".redeemPointsTD").show();
		$("#totPayingAmt"+index).val("");
		$("#totPayingAmt"+index).prop("readonly",true);
	}else{
		$(modeObj).closest("tr").find(".redeemPointsTD").hide();
		$("#totPayingAmt"+index).closest("td").find(".redeemPointsTD").hide();
		$("#totPayingAmt"+index).prop("readonly",false);
	}
}

function onChangeRedeemPoints(modeObj) {
	var paymentModeValue = modeObj.value;
	var index=modeObj.id.substr(-1);
	onChangeRewardPoints(document.getElementById('rewardPointsRedeemed'+index),document.getElementById('totPayingAmt'+index));
	resetTotals();
}

function filterPaymentModes() {
	var numPayments = getNumOfPayments();
	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;
		if(!isMvvPackage) {
			$("#"+paymentModeId+" option[value='-8']").remove();
		}
		if(visitType === 'i') {
			if(((availableDeposits - ipDeposits) <= 0 && generalDepositSetOff == 0) || (availableDeposits > 0 && isMvvPackage)) {
				$("#"+paymentModeId+" option[value='-6']").remove();
			}
		} else {
			if(((availableDeposits) <= 0  && generalDepositSetOff == 0) || (availableDeposits > 0 && isMvvPackage)) {
				$("#"+paymentModeId+" option[value='-6']").remove();
			}
		}

		if((ipDeposits == 0 && ipDepositSetOff == 0) || (ipDeposits > 0 && isMvvPackage) || (showIpDesposit === 'false')) {
			$("#"+paymentModeId+" option[value='-7']").remove();
		}

		if(!hasRewardPointsEligibility) {
			$("#"+paymentModeId+" option[value='-9']").remove();
		}
	}
}

function validateReopen(patientWriteOff) {
	var billStatus = document.mainform.billStatus.value;
	var patientCreditNoteAmt = 0;
	if (document.mainform.patientCreditNoteAmt)
		patientCreditNoteAmt = getPaise(document.mainform.patientCreditNoteAmt.value);

	if ( (null != document.mainform.reopenReason) && (
		('' == trimAll(document.mainform.reopenReason.value)) ||
		(trimAll(document.mainform.oldreopenReason.value) == trimAll(document.mainform.reopenReason.value)))) {
		showMessage("js.billing.billlist.enterreopenreasonforreopeningbill");
		document.mainform.reopenReason.focus();
		return false;
	} else if((patientWriteOff == 'A' || patientWriteOff == 'M' || billStatus == 'C') && formatAmountPaise(totAmtDuePaise + patientCreditNoteAmt) != 0.00){
		var ok = confirm("Warning: Reopening a written off bill will cancel a write off.\n" +
					"Do you want to proceed?");
			if (!ok)
				return false;
			return true;
	}else {
		return true;
	}
}

function validateBillRemarks() {
	var billStatus = document.mainform.billStatus.value;
	if (billStatus == "X") {
		return validateCancelReason();

	} else {
		var selectedLblId = document.getElementById('billLabel').options[document.getElementById('billLabel').selectedIndex].value;
		var billRemarks = document.getElementById('billRemarks').value;
		if (selectedLblId != '-1') {
			if (!empty(billLabelMasterJson)) {
				for (var i=0; i<billLabelMasterJson.length; i++) {
					var master = billLabelMasterJson[i];
					if (master.bill_label_id == selectedLblId) {
						if (master.remarks_reqd == 'Y' && billRemarks == '') {
							showMessage("js.billing.billlist.enterbillremarks");
							document.mainform.billRemarks.focus();
							return false;
						} else {
							return true;
						}
					}
				}
			}
		}
		return true;
	}
}
function validateDiscountAuth() {
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
}

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
	if (document.mainform.originalDepositSetOff)
		depositSetOff = getPaise(document.mainform.originalDepositSetOff.value);

	var rewardPointsAmount = 0;
	if (document.mainform.rewardPointsRedeemedAmount)
		rewardPointsAmount = getPaise(document.mainform.rewardPointsRedeemedAmount.value);

	var patientAmt = totAmtPaise + totTaxPaise - depositSetOff - rewardPointsAmount - totInsAmtPaise - totInsTaxPaise;
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

	var patientCreditNoteAmt = 0;
	if (document.mainform.patientCreditNoteAmt)
		patientCreditNoteAmt = getPaise(document.mainform.patientCreditNoteAmt.value);

	if(patientAmt == existingReceiptAmt + payingAmt - patientCreditNoteAmt) {
		document.mainform.paymentForceClose.value = "Y";
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

	if (document.mainform.primaryClaimStatus.value == 'C'
			&& document.mainform.billStatus.value == 'A') {
		var errMsg = getString("js.billing.billlist.claimnotbeinsent.billstatusopen1")
					+ billNumber + getString("js.billing.billlist.claimnotbeinsent.billstatusopen2");
		alert(errMsg);
		document.mainform.primaryClaimStatus.focus();
		return false;
	}

	if (document.mainform.secondaryClaimStatus != null
			&& document.mainform.secondaryClaimStatus.value == 'C'
			&& document.mainform.billStatus.value == 'A') {
		var errMsg = getString("js.billing.billlist.claimnotbeinsent.billstatusopen1")
				+ billNumber + getString("js.billing.billlist.claimnotbeinsent.billstatusopen2");
		alert(errMsg);
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
 * Validate that the Sponsor Amt equals the sponsor credits + paying amount
 */
function validateSponsorPayment() {

	if( document.mainform.billStatus.value != "C" ){
		return true;
	}
	document.mainform.claimForceClose.value = "N";
	var priClaimStatus = document.mainform.primaryClaimStatus;
	var secClaimStatus = document.mainform.secondaryClaimStatus;
	if (priClaimStatus == null || priClaimStatus.value != 'C'
		|| (secClaimStatus != null && secClaimStatus.value != 'C'))
		return true;
	
	var existingReceipts = (existingSponsorReceipts==0) ? getPaise(existingRecdAmount) : getPaise(existingSponsorReceipts);
	
	var payingAmt = getPayingAmountPaise('sponsor') + getPayingTDSAmountPaise();

	if (totInsAmtPaise + totInsTaxPaise == existingReceipts + payingAmt)
		return true;

	var sponsorCreditNoteAmt = 0;
	if (document.mainform.sponsorCreditNoteAmt)
		sponsorCreditNoteAmt = getPaise(document.mainform.sponsorCreditNoteAmt.value);

	if (totInsAmtPaise + totInsTaxPaise == existingReceipts - sponsorCreditNoteAmt)
		return true;

	if (sponsorWriteOff == 'A' ||(checkClaimStatusForWriteOff && null != document.getElementById("sponsor_writeoff") &&
			document.getElementById("sponsor_writeoff").value =="M" && getTotalSpnsrAmountDue() < 0)) {
		document.mainform.claimForceClose.value = "Y";
	}else {
		showMessage("js.billing.billlist.tpadueamountnotwrittenoff");
		document.mainform.claimForceClose.value = "N";
		return false;
	}
	return true;
}

function closeClaimStatus(){
	var laterPriInsuranceClaimAmt=0;
	var laterSecInsuranceClaimAmt=0;
	if(document.mainform.primaryTotalClaim != undefined)
		laterPriInsuranceClaimAmt=document.mainform.primaryTotalClaim.value;
	if(document.mainform.secondaryTotalClaim != undefined)
		laterSecInsuranceClaimAmt=document.mainform.secondaryTotalClaim.value;
	var finalPriInsuranceClaimAmt=laterPriInsuranceClaimAmt-initialPriInsuranceClaimAmt;
	var finalSecInsuranceClaimAmt=laterSecInsuranceClaimAmt-initialSecInsuranceClaimAmt;
	var totalPriInsuranceClaimAmt=priSponsorAmt+finalPriInsuranceClaimAmt;
	var totalSecInsuranceClaimAmt=secSponsorAmt+finalSecInsuranceClaimAmt;
	checkAllPayment();
	var totalPriReceipt=primarySponsorsReceipt+tempPriSponsorsSettlement+priRecdAmt;
	var totalSecReceipt=secondarySponsorsReceipt+tempSecSponsorsSettlement+secRecdAmt;
	var priClaimStatus = document.mainform.primaryClaimStatus;
	var secClaimStatus = document.mainform.secondaryClaimStatus;
	var sponsorWriteOffVal=document.getElementById("sponsor_writeoff");

	if(priClaimStatus!=null && priClaimStatus.value == 'C' && !(totalPriReceipt>=totalPriInsuranceClaimAmt)){

		var resultValue=(priSponsorAmt-primarySponsorsReceipt).toFixed(2);	//primary claim due amount

		if((null!=sponsorWriteOffVal && sponsorWriteOffVal.value =="M") && (resultValue<=laterPriInsuranceClaimAmt)){
			return true;
		}

		if(resultValue <= laterPriInsuranceClaimAmt && sponsorWriteOff == 'A')
			return true;

		alert("Primary Sponsor due amount is not written off. Cannot close the Primary Claim.");
		return false;
	}
	if(secClaimStatus!=null && secClaimStatus.value == 'C' && !(totalSecReceipt>=totalSecInsuranceClaimAmt)){
		var resultValue=(secSponsorAmt-secondarySponsorsReceipt).toFixed(2);	//secondary claim due amount

		if((null!=sponsorWriteOffVal && sponsorWriteOffVal.value =="M") && (resultValue <= laterSecInsuranceClaimAmt)){
			return true;
		}

		if(resultValue <= laterSecInsuranceClaimAmt && sponsorWriteOff == 'A')
			return true;

		alert("Secondary Sponsor due amount is not written off. Cannot close the Secondary Claim.");
		return false;
	}
	return true;
}

function checkAllPayment() {
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(document.mainform, "paymentType", i);
			var amtObj = getIndexedFormElement(document.mainform, "totPayingAmt", i);
			var type = paymentObj.value;

			if ( (null != amtObj) && (amtObj.value != "") ) {
				if (type=='pri_sponsor_receipt_settlement' || type=='pri_sponsor_receipt_advance') {
					tempPriSponsorsSettlement=Number(tempPriSponsorsSettlement) + Number(amtObj.value);
				}
				if (type=='sec_sponsor_receipt_settlement' || type=='sec_sponsor_receipt_advance') {
					tempSecSponsorsSettlement=Number(tempSecSponsorsSettlement) + Number(amtObj.value);
				}
			}
		}
	}
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

/*
 * Finalized date delinked from discharge date.
 * Finalized date related changes according to user back date rights.
 * Ref. Bug # 13097.
 */
function validateFinalizedDateAndTime() {
	var valid = true;
	var finalizedDateObj = document.mainform.finalizedDate;
	var finalizedTimeObj = document.mainform.finalizedTime;
	var billStatus = document.mainform.billStatus.value;

	var curDate = (gServerNow != null) ? gServerNow : new Date();
    var finalizedDtTime  = getDateTime(finalizedDate, finalizedTime);
    var regdatetime =  getDateTime(regDate, regTime);

	if (allowBackDateBillActivities != 'A') {
		if(origBillStatus == 'A' && (billStatus == 'F' || billStatus == 'C')) {
			if(finalizedDate != '' && finalizedTime != '') {
				if(curDate.getTime() != finalizedDtTime.getTime()) {
					var ok = confirm("Finalized date will be changed to current date on save. Are you sure?");
					if(!ok) {
						finalizedDateObj.value = finalizedDate;
						finalizedTimeObj.value = finalizedTime;
						return false;
					}else {
						finalizedDateObj.value = formatDate(curDate, "ddmmyyyy", "-");
						finalizedTimeObj.value = formatTime(curDate, false);
						return true;
					}
				}
			}
		}
	} else {
		if(billType == 'C' && (billStatus == 'F' || billStatus == 'C')) {
			var finalizedDtTime  = getDateTime(finalizedDateObj.value, finalizedTimeObj.value);
			valid = valid && validateRequired(finalizedDateObj, getString("js.billing.billlist.finalizeddate.required"));
			valid = valid && validateRequired(finalizedTimeObj, getString("js.billing.billlist.finalizedtime.required"));
			valid = valid && doValidateDateField(finalizedDateObj,"past");
			if(!valid) return false;
			valid = valid && validateTime(finalizedTimeObj);
			if(!valid) return false;
			var lessregdatetime = daysDiff(regdatetime, finalizedDtTime);
			if (lessregdatetime <  0){
				 var ok = confirm(getString("js.billing.billlist.date.finalizeddatelesserthanadmissiondate")+"("+regDate+" "+regTime+")."+getString("js.billing.billlist.continue"));
				if (!ok) {
					finalizedDateObj.focus();
					return false;
				}
			}
		}
	}

    //validate finalize date not to be less than bill open date
	valid = valid && !isFinalizedDateLessThanOpenDate();
    if ( !valid && (origBillStatus == 'A' && (billStatus == 'F' || billStatus == 'C')) ) {
    	showMessage("js.billing.billlist.finalizeddate.can.not.be.less.than.opendate");
    	finalizedDateObj.focus();
    	return false;
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

function checkPendingTestAndConsultationForBill() {
	var messageText = "";
	var labTestExist = false;
	var radTestExist = false;
	var consultationExist = false;
	var labTestMessage ="";
	var radTestMessage = "";
	var consultationMessage = "";

	var billStatus = document.mainform.billStatus.value;
	if ((billStatus == 'F' || billStatus == 'C')
			&& (pendingtestsforbill.length > 0 || pendingconsultationforbill.length > 0)) {
		for (var i = 0; i < pendingtestsforbill.length; i++) {
			if (pendingtestsforbill[i].type == "Laboratory") {
				if (!labTestExist) {
					labTestExist = true;
					labTestMessage = "Lab Tests - "
							+ pendingtestsforbill[i].item_name;
				} else {
					labTestMessage += ", " + pendingtestsforbill[i].item_name;
				}
			} else {
				if (!radTestExist) {
					radTestExist = true;
					radTestMessage = "Radiology Tests - "
							+ pendingtestsforbill[i].item_name;
				} else {
					radTestMessage += ", " + pendingtestsforbill[i].item_name;
				}
			}
		}

		for (var i = 0; i < pendingconsultationforbill.length; i++) {
			if (!consultationExist) {
				consultationExist = true;
				consultationMessage = "Consultation - "
						+ pendingconsultationforbill[i].item_name;
			} else {
				consultationMessage += ", "
						+ pendingconsultationforbill[i].item_name;
			}
		}
		if ( labTestExist && typeOfPendingActivitiesRequired.includes("L") ) {
			messageText = "Lab";
		}
		if (radTestExist && typeOfPendingActivitiesRequired.includes("R") ) {
			messageText = messageText != "" ? messageText + "/Radiology" : "Radiology";
		}
		if (consultationExist && typeOfPendingActivitiesRequired.includes("C") ) {
			messageText = messageText != "" ? messageText + "/Consultation"
					: "Consultation";
		}

		if ( messageText == '' ){
			return true;
		}
		
		var finalMessage = "Some of the " + messageText + " orders are still not completed, Do you still want to continue? ";
		if( labTestExist && typeOfPendingActivitiesRequired.includes("L") ) {
			finalMessage += "\n"+labTestMessage
		}
		if( radTestExist && typeOfPendingActivitiesRequired.includes("R") ) {
			finalMessage += "\n"+radTestMessage
		}
		if( consultationExist && typeOfPendingActivitiesRequired.includes("C") ) {
			finalMessage += "\n"+consultationMessage;
		}
		var ok = confirm(finalMessage);
		if (!ok) {
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

function hasPackageExcludedCharges() {
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


/*
 * On clicking Pay And Close (available only for prepaid bill),
 * set status to close and do same as save.
 */
function doPayAndClose() {
	// clicking on Pay And Close is same as save with status set to close
	var origIndex = document.mainform.billStatus.value;
	document.mainform.billStatus.value = 'C';
	document.mainform.paymentStatus.value = 'P';
	if (document.mainform.primaryClaimStatus)
		document.mainform.primaryClaimStatus.value = 'C';

	if (document.mainform.secondaryClaimStatus)
		document.mainform.secondaryClaimStatus.value = 'C';

	var isValid = validateSave();
	isValid = isValid && validateBillRemarks();
	isValid = isValid && doPaytmTransactions();
	if (!isValid) {
		// restore the original bill status
		document.mainform.billStatus.value = origIndex;
	}
	if (isValid) {
		enableFormValues();
		document.mainform.buttonAction.value = 'payclose';
		document.getElementById("payClose").disabled = true;

		document.mainform.submit();
	} else {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
	}
}

function doPayAndSave() {
	document.mainform.paymentStatus.value = 'P';
	var valid = validateSave();
	valid = valid && validateBillRemarks();
	valid = valid && doPaytmTransactions();
	if (valid) {
		document.mainform.buttonAction.value = 'paysave';
		document.getElementById("payAndSaveButton").disabled = true;
		document.mainform.submit();
	} else {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
	}
	return true;
}

function validateOKtoDischargeDate() {

	var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund');
	var clickedOkToDischarge = $('#okToDischarge').val();
	var patientdue = getTotalAmountDue();
	if(clickedOkToDischarge == 'Y' && billType == 'C' && (patientdue - payingAmt) > 0) {
		var ok = confirm("There is pending patient due and is marked for OK to Discharge.");
		if(!ok)
			return false;
	}

	var regDateTime = getDateTime(regDate, regTime);
	if(document.mainform.dischargeDate == null || document.mainform.dischargeTime == null ||
			document.mainform.dischargeDate.value == null || document.mainform.dischargeTime.value == null
		    || document.mainform.dischargeDate.value == "" || document.mainform.dischargeTime.value == "") {
		return true;
	}
	var disDateTime = getDateTimeFromField(document.mainform.dischargeDate,
			document.mainform.dischargeTime);
	if (clickedOkToDischarge == 'Y' && disDateTime < regDateTime) {
		alert("OK To Discharge date/time cannot be earlier than Registration date/time (" + regDate + " " + regTime + ")");
		document.mainform.disdate.focus();
		return false;
	}

	return true;
}

function doSave() {
	var valid = validateSave();
	valid = valid && validateBillRemarks();
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
	valid = valid && doPaytmTransactions();
	if (valid) {
		enableFormValues();
		document.mainform.buttonAction.value = 'save';
		if(document.getElementById("saveButton"))
			document.getElementById("saveButton").disabled = true;
		document.mainform.submit();
	} else {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
	}
	return true;
}

function doReopen(patientWriteOff) {
	var valid = validateReopen(patientWriteOff);
	if (valid) {
		document.mainform.buttonAction.value = 'reopen';
		document.mainform.submit();
	} else {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
	}
}

function writeOffPatientAmt() {
	var writeOffRemarks = document.getElementById("writeOffRemarks").value;

	if(null != writeOffRemarks && writeOffRemarks != '' && trimAll(writeOffRemarks) != trimAll(document.getElementById("oldWriteOffRemarks").value)){
		document.mainform._method.value = "markForWriteoff";
		document.mainform.submit();
	}else{
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
		showMessage("js.billing.billlist.enterwriteoffremarks.markbillwriteoff");
		document.mainform.writeOffRemarks.focus();
		return false;
	}
}

function doBillCancel() {
	document.mainform.billStatus.value = 'X';
	var valid = validateSave();
	valid = valid && validateCancelReason();
	if (valid) {
		document.mainform.buttonAction.value = 'cancel';
		document.mainform.submit();
	} else {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
	}
}

function requestForcancellation() {
	document.mainform._method.value = "requestForBillCancellation";
	document.mainform.submit();
}

/*
function processPlanvisitCopay() {

	if (chargesAdded > 0 || chargesEdited > 0) {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
		alert("New charges have been added or edited. Please save before processing visit copay.");
		return false;
	}

	var ok = confirm(" Do you want to process bill visit copay?");
	if (!ok) {
		updateIFrameParentHelper(pendingActionErrorFunctionMap);
		return false;
	}

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
		if (document.mainform.dischargeDate != null) {
			document.mainform.dischargeDate.value = null;
			document.mainform.dischargeTime.value = null;
		}
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
	if (!empty(billType) && (optionParts[0] == 'CUSTOM' || optionParts[0] == "CUSTOMEXP")) {
		url +="&billType="+billType.substring(parseInt(optionParts[0].length)+1,billType.length);
	} else {
		url +="&billType="+optionParts[1];
	}
	window.open(url);
}

function onChangeDeposits() {

	if (!validateDepositSetOff(document.mainform.depositSetOff, document.mainform.originalDepositSetOff))
		return false;
	if (!ajaxCallForDepositValidation())
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

function onChangeRewardPoints(rewardPointsRedeemed, rewardPointsRedeemedAmount) {
	// Clears Payments also.
	if (!validateRewardPoints(rewardPointsRedeemed, rewardPointsRedeemedAmount, document.mainform.originalPointsRedeemed))
		return false;

	return true;
}

// Does not clears payments.
function validateBillRewardPointsRedeemed() {

	resetRedeemedPoints();
	return true;
}

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

	editform.discConductDocName.disabled = !(splitEnabled);
	editform.discConductDocRs.disabled = !(splitEnabled);
	editform.discPrescDocName.disabled = !(splitEnabled);
	editform.discPrescDocRs.disabled = !(splitEnabled);
	editform.discRefDocName.disabled = !(splitEnabled);
	editform.discRefDocRs.disabled = !(splitEnabled);
	editform.discHospUserName.disabled = !(splitEnabled);
	editform.discHospUserRs.disabled = !(splitEnabled);
}

function onChangeDiscountType(type) {
	if (editform.discountType.checked) {
		enableDisableDisc(true, '');
	}else {
		enableDisableDisc(true, 'overall');
		editform.overallDiscRs.readOnly = false;
	}
}

function onChangeDiscount(i) {
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

	//var newAmtPaise = changedRate*changedQty - changedDisc;
	 var newAmtPaise = 0;

	 if(changedDisc >= (changedRate*changedQty)){
	     var changedDisc1 = changedRate*changedQty ;
	     setIndexedValue("disc", i, formatAmountPaise(changedDisc1));
	     setNodeText(getChargeRow(i).cells[DISC_COL], formatAmountPaise(changedDisc1));
	    } else {
	      newAmtPaise = changedRate*changedQty - changedDisc;
	  }

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

function resetDiscountTotalRs() {
	var totalDiscRs = 0;
	if (editform.discountType.checked) {
		totalDiscRs += getElementIdPaise("discConductDocRs");
		totalDiscRs += getElementIdPaise("discPrescDocRs");
		totalDiscRs += getElementIdPaise("discRefDocRs");
		totalDiscRs += getElementIdPaise("discHospUserRs");
	}
	editform.totalDiscRs.value = formatAmountPaise(totalDiscRs);
	recalcEditChargeAmount();
}

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

function selectDiscountAuth() {
	mainform.billDiscountAuth.value = mainform.discountAuthName.value;
	var disclbl = document.getElementById("discountAuthlbl");
	var discAuthName = mainform.discountAuthName.options[mainform.discountAuthName.selectedIndex].text;
	discAuthName = ( discAuthName == '--Select--') ? '' : discAuthName;
	if (disclbl != undefined)
		document.getElementById("discountAuthlbl").textContent = discAuthName;

	if(document.mainform.itemDiscPer && document.mainform.itemDiscPer.value != 0)
		document.mainform.discountCategory.disabled = true;
	else if (origBillStatus != 'C' && origBillStatus != 'X' && origBillStatus != 'F' && !(disableDiscCategory) && (discountPlanAllowRt != 'N') )
		document.mainform.discountCategory.disabled = false;
}



function resetItemDiscounts(discCategory) {
	var chargeHeadIds = document.getElementsByName("chargeHeadId");
	var discElements = document.mainform.discountCheck;
	var oldDisc = document.mainform.oldDisc;
	var oldOverallDiscountAuth = document.mainform.old_overall_discount_auth;
	var isSystemDiscountOld = document.mainform.isSystemDiscountOld;
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
						setIndexedValue("isSystemDiscount", i, isSystemDiscountOld[i].value);

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

function initDiscountAuthorizerAutoComplete(authName,authName_dropdown,authId,oAutoComp) {
	var disAuthJson = {result: jDiscountAuthorizers};

	oAuthDS  = new YAHOO.util.LocalDataSource(disAuthJson);
	oAuthDS.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	oAuthDS.responseSchema = {
		resultsList : "result",
		fields: [{key: "disc_auth_name"},
				 {key: "disc_auth_id"}]
		};

	oAutoComp = new YAHOO.widget.AutoComplete(authName, authName_dropdown, oAuthDS);

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 5;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList= false;
	oAutoComp._bItemSelected = true;

	oAutoComp.textboxBlurEvent.subscribe(function() {
		var discAuthName = YAHOO.util.Dom.get(authName).value;
		if(discAuthName == '') {
			YAHOO.util.Dom.get(authId).value = "";
		}
	});
	oAutoComp.itemSelectEvent.subscribe(function() {
		var discAuthName = YAHOO.util.Dom.get(authName).value;
		if(discAuthName != '') {
			for ( var i=0 ; i< jDiscountAuthorizers.length; i++){
				if(discAuthName == jDiscountAuthorizers[i]["disc_auth_name"]){
					YAHOO.util.Dom.get(authId).value = jDiscountAuthorizers[i]["disc_auth_id"];
					break;
				}
			}
		}else{
			YAHOO.util.Dom.get(authId).value = "";
		}
	});
}

function initConductingDoctorAutoComplete(list) {

	var ds = new YAHOO.util.LocalDataSource(list, { queryMatchContains : true });
	ds.responseSchema = { resultsList : "doctors",
		fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete("eConducted", 'eConducted_dropdown', ds);

	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp._bItemSelected = true;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;
	autoComp.selectionEnforceEvent.subscribe(clearConductedId);
	autoComp.itemSelectEvent.subscribe(onChangeConducted);
	return autoComp;
}

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
	var billDiscountExists = false;
    for (var i=0; i<getNumCharges(); i++) {
            if (getIndexedValue("chargeHeadId",i) == 'BIDIS' && getIndexedValue("disc",i) >0) {
                    billDiscountExists = true;
                    break;
            }

    }
    for (var i=0;i<getNumCharges();i++) {
            if (billDiscountExists && getIndexedValue("disc",i) >0  && i==id && getIndexedValue("chargeHeadId",i) != 'BIDIS') {
                    showMessage("js.billing.billlist.billdiscount.remove");
                    return false;
            }
    }
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
				setIndexedValue("dynaPackageExcluded", id, "Y");
			}else {
				setIndexedValue("chargeExcluded", id, chargeExcluded);
				setIndexedValue("dynaPackageExcluded", id, chargeExcluded);
			}
		}else {
			setIndexedValue("chargeExcluded", id, "N");
			setIndexedValue("dynaPackageExcluded", id, "N");
		}
	}

	var visitID = document.getElementById('visitId').value;

	var subGroupIds = document.getElementsByName(chargeId+"_sub_group_id");

	var subGroupIdValues = [];

	for(var l=0; l<subGroupIds.length; l++){
		subGroupIdValues[l] = subGroupIds[l].value;
	}

	//On adding the Cancel Item again in the billing screen,to avoid the duplication in bill charge tax
	if (oldDeleted == 'false'){
	  getTaxDetails(row, chargeId, getIndexedValue("chargeHeadId",id), getIndexedValue("chargeGroupId",id),
			getIndexedValue("descriptionId",id), getIndexedValue("amt",id) , getIndexedValue( "consultation_type_id",id),
			getIndexedValue( "op_id",id), subGroupIdValues, mr, billNumber);
	}

	if(tpaBill) {
		var chargeHead = getIndexedValue("chargeHeadId",id);
		if(chargeHead != 'ROF'){
			getBillChargeClaims(visitID, document.mainform);
		}
	}

	resetTotals(claimAmtEdited, false);

	if(document.mainform.showCancelled.checked)
		onChangeFilter();

	if(chargeHead == 'ROF' && newDeleted == 'true'){
		document.getElementsByName("roundOff")[0].checked = false;
	}

	return false;
}

/*
 * Show the discount dialog. Note that we never disable the discount button, so that the user
 * can read the details at the least. All disabling will be done inside the dialog, for items
 * where the discount is not applicable.
 */
function showDiscounts(id) {

	var discType = 'overall';

	var deleted = getIndexedValue("delCharge",id);
	var chargeHead = getIndexedValue("chargeHeadId",id);
	var chargeId = getIndexedValue("chargeId",id);
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

	editform.discountType.disabled = !(discountsEnabled);
	enableDisableDisc(discountsEnabled, discType);

	editform.overallDiscByName.value = getIndexedValue("overall_discount_auth_name",id);
	editform.overallDiscBy.value = getIndexedValue("overall_discount_auth",id);

	if (getIndexedValue("overall_discount_auth",id) == -1) {
		if (chargeHead == 'PKGPKG' && gPackageChargeDetails[chargeId] != null){
			editform.overallDiscRs.value = gPackageChargeDetails[chargeId].disc;
		} else{
			editform.overallDiscRs.value = getIndexedValue("disc",id);
		}
	}else {
		if(chargeHead == 'PKGPKG' && gPackageChargeDetails[chargeId] != null){
			editform.overallDiscRs.value = gPackageChargeDetails[chargeId].overall_discount_amt;
		} else{
			editform.overallDiscRs.value = getIndexedValue("overall_discount_amt",id);
		}
	}

	editform.discConductDocName.value = getIndexedValue("discount_auth_dr_name",id);
	editform.discConductDoc.value = getIndexedValue("discount_auth_dr",id);

	editform.discPrescDocName.value = getIndexedValue("discount_auth_pres_dr_name",id);
	editform.discPrescDoc.value = getIndexedValue("discount_auth_pres_dr",id);

	editform.discRefDocName.value = getIndexedValue("discount_auth_ref_name",id);
	editform.discRefDoc.value = getIndexedValue("discount_auth_ref",id);

	editform.discHospUserName.value = getIndexedValue("discount_auth_hosp_name",id);
	editform.discHospUser.value = getIndexedValue("discount_auth_hosp",id);

	if(chargeHead == 'PKGPKG' && gPackageChargeDetails[chargeId] != null){
		editform.discConductDocRs.value = gPackageChargeDetails[chargeId].conducting_doctor_disc;
		editform.discPrescDocRs.value = gPackageChargeDetails[chargeId].prescribed_doctor_disc;
		editform.discRefDocRs.value = gPackageChargeDetails[chargeId].referral_doctor_disc;
		editform.discHospUserRs.value = gPackageChargeDetails[chargeId].hospital_disc;
	}else{
		editform.discConductDocRs.value = getIndexedValue("dr_discount_amt",id);
		editform.discPrescDocRs.value = getIndexedValue("pres_dr_discount_amt",id);
		editform.discRefDocRs.value = getIndexedValue("ref_discount_amt",id);
		editform.discHospUserRs.value = getIndexedValue("hosp_discount_amt",id);
	}

	setTotalDiscount(discType);

	return discType;
}
function setTotalDiscount(discType) {
	var totalDiscRs = 0;
	if (discType == 'split') {
		totalDiscRs += getElementIdPaise("discConductDocRs");
		totalDiscRs += getElementIdPaise("discPrescDocRs");
		totalDiscRs += getElementIdPaise("discRefDocRs");
		totalDiscRs += getElementIdPaise("discHospUserRs");

		editform.totalDiscRs.value = formatAmountPaise(totalDiscRs);
		editform.overallDiscRs.value = formatAmountPaise(totalDiscRs);
	}
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


function validateDiscounts(id) {

	var chargeHead = getIndexedValue("chargeHeadId",id);
	var chargeId = getIndexedValue("chargeId",id);
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

		var totalSplitDiscRs = Number(discConDrRs) + Number(discPresDrRs) + Number(discRefDrRs) + Number(discRefDrRs);
		if(totalSplitDiscRs <= 0){
			showMessage("js.billing.billlist.split.discount.amount");
			return false;
		}
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

		if(chargeHead == 'PKGPKG'){
			getChargeClosure(chargeRefMap, chargeId).forEach(componentChargeId => {
				let componentRowIndex = getRowChargeIndex(getChargeRowByChargeId(componentChargeId));

				let discConDrRs = getAmount(editform['discConductDocRs_' + componentChargeId].value);
				let discPresDrRs = getAmount(editform['discPrescDocRs_' + componentChargeId].value);
				let discRefDrRs = getAmount(editform['discRefDocRs_' + componentChargeId].value);
				let discHospDrRs = getAmount(editform['discHospUserRs_' + componentChargeId].value);
				// let discOverallRs = getAmount(editform.overallDiscRs.value);
			
				
				setIndexedValue("discount_auth_dr_name",componentRowIndex, discConDrName);
				setIndexedValue("discount_auth_dr",componentRowIndex, discConDr);
				setIndexedValue("dr_discount_amt",componentRowIndex, discConDrRs);
		
				setIndexedValue("discount_auth_pres_dr_name",componentRowIndex, discPresDrName);
				setIndexedValue("discount_auth_pres_dr",componentRowIndex, discPresDr);
				setIndexedValue("pres_dr_discount_amt",componentRowIndex, discPresDrRs);
		
				setIndexedValue("discount_auth_ref_name",componentRowIndex, discRefDrName);
				setIndexedValue("discount_auth_ref",componentRowIndex, discRefDr);
				setIndexedValue("ref_discount_amt",componentRowIndex, discRefDrRs);
		
				setIndexedValue("discount_auth_hosp_name",componentRowIndex, discHospDrName);
				setIndexedValue("discount_auth_hosp",componentRowIndex, discHospDr);
				setIndexedValue("hosp_discount_amt",componentRowIndex, discHospDrRs);
		
				setIndexedValue("overall_discount_auth_name",componentRowIndex, '');
				setIndexedValue("overall_discount_auth",componentRowIndex, 0);
				setIndexedValue("overall_discount_amt",componentRowIndex, 0);
			})
		}else{

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
		}

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

		if(chargeHead == 'PKGPKG'){
			
			getChargeClosure(chargeRefMap, chargeId).forEach(componentChargeId => {

				let componentRowIndex = getRowChargeIndex(getChargeRowByChargeId(componentChargeId));

				
				setIndexedValue("overall_discount_auth_name",componentRowIndex, discOverallName);
				setIndexedValue("overall_discount_auth",componentRowIndex, discOverall);
		
				setIndexedValue("discount_auth_dr_name",componentRowIndex, '');
				setIndexedValue("discount_auth_dr",componentRowIndex, 0);
				setIndexedValue("dr_discount_amt",componentRowIndex, 0);
		
				setIndexedValue("discount_auth_pres_dr_name",componentRowIndex, '');
				setIndexedValue("discount_auth_pres_dr",componentRowIndex, 0);
				setIndexedValue("pres_dr_discount_amt",componentRowIndex, 0);
		
				setIndexedValue("discount_auth_ref_name",componentRowIndex, '');
				setIndexedValue("discount_auth_ref",componentRowIndex, 0);
				setIndexedValue("ref_discount_amt",componentRowIndex, 0);
		
				setIndexedValue("discount_auth_hosp_name",componentRowIndex, '');
				setIndexedValue("discount_auth_hosp",componentRowIndex, 0);
				setIndexedValue("hosp_discount_amt",componentRowIndex, 0);
			});
		} else {
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
		}


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
	editChargeDialog.cancelEvent.subscribe(onEditCancel);
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
	var temprow = row;
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


	if (fieldEdited){
		if(!onEditSubmit(true)){
			deleteTaxRows(temprow);
		}
	}else{
		deleteTaxRows(temprow);
	}

	/*if (fieldEdited)
		onEditSubmit(true);
	else
		deleteTaxRows(temprow);*/

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
	editform.eQty.readOnly = !isQtyEditable(id);
	editform.eRate.readOnly = !isRateEditable(id);
	editform.eRemarks.readOnly = !isItemRemarksEditable(id);
	editform.eUserRemarks.readOnly = !isItemRemarksEditable(id);
	editform.eCode.readOnly = !isItemCodeEditable(id);
	editform.overallDiscRs.readOnly = !isDiscountEditable(id) || isSplitDiscount(id);
	editform.eConducted.disabled = !isConductedByEditable(id);
	editform.eItemRemarks.readOnly = !isItemRemarksEditable(id);

	if (hasDynaPackage) {
		editform.eAmtIncluded.readOnly = !isChargeAmountIncludedEditable(id);
		editform.eQtyIncluded.readOnly = !isChargeQuantityIncludedEditable(id);
		editform.ePackageFinalized.disabled = !isChargeAmountIncludedEditable(id);
	}

	if (editform.eClaimAmt != null) {
		editform.eClaimAmt.readOnly = !isClaimEditable(id);
	}
	var subGrpId = getIndexedValue("service_sub_group_id", id);
	var subGrp = findInList(allServiceSubgroupsList,'service_sub_group_id',subGrpId);
	var grpId = subGrp.service_group_id;
	var grp = findInList(serviceGroupsJSON,'service_group_id',grpId);
	var chargeId = getIndexedValue("chargeId", id);
	var chargeHeadId = getIndexedValue("chargeHeadId", id);

	document.getElementById("eChargeGroup").textContent = getIndexedValue("chargeGroupName", id);
	document.getElementById("eChargeHead").textContent = getIndexedValue("chargeHeadName", id);
	document.getElementById("eDescription").textContent = getIndexedValue("description", id);
	document.getElementById("eserviceGroup").textContent = grp.service_group_name;
	document.getElementById("eserviceSubGroup").textContent = subGrp.service_sub_group_name;

	var itemDesc = getItemDescription(id);
	document.getElementById("eItemDescription").textContent = itemDesc;

	editform.eCondDocRequired.value =  getIndexedValue("conducting_doc_mandatory",id);
	editform.ePostedDate.value =  getIndexedValue("postedDate",id);
	editform.ePostedTime.value =  getIndexedValue("postedTime",id);
	editform.eCode.value = getIndexedValue("actRatePlanItemCode", id);
	var payeeDocId = getIndexedValue("payeeDocId", id);
	editform.eConductedId.value = payeeDocId;
	if (payeeDocId != '') {
		var condDoctor = findInList(jDoctors, 'doctor_id', payeeDocId);
		if(condDoctor == null){//assuming that doctor is inactive
			editform.eConducted.disabled = true;
			editform.eConducted.value = '';
		}else
			editform.eConducted.value = condDoctor.doctor_name;
		eConductedAutoComp._bItemSelected = true;
	} else {
		editform.eConducted.value = '';
	}

	if (hasDynaPackage) {
		editform.eAmtIncluded.value = getIndexedValue("amount_included", id);
		editform.eQtyIncluded.value = getIndexedValue("qty_included", id);
		editform.eAmtIncludedOrig.value = getIndexedValue("amount_included", id);
		editform.eQtyIncludedOrig.value = getIndexedValue("qty_included", id);

		editform.ePackageFinalizedOrig.value = getIndexedValue("packageFinalized", id);
		editform.ePackageFinalized.checked = getIndexedValue("packageFinalized", id)== "Y"? "checked":"";
	}
	editform.eRemarks.readOnly = false;
	editform.eRemarks.value = getIndexedValue("remarks", id);
	if (getIndexedValue("chargeGroupId", id) === 'MED' || getIndexedValue("chargeGroupId", id) === 'ITE') {
		editform.eRemarks.readOnly = true;
	}
	editform.eUserRemarks.value = getIndexedValue("userRemarks", id);
	editform.eItemRemarks.value = getIndexedValue("itemRemarks", id);
	if (chargeHeadId == 'PKGPKG'){
		editform.eQty.value = getIndexedValue("qty", id)
		editform.eQty.readOnly = true;
	}
	if(chHead == 'PKGPKG'){
		editform.eRate.value =  gPackageChargeDetails[chargeId].rate;
		editform.eAmt.value = gPackageChargeDetails[chargeId].amt;
		editform.eQty.value = getIndexedValue("qty", id)
		editform.eQty.readOnly = true;
	}else{
		editform.eRate.value = getIndexedValue("rate", id);
		editform.eAmt.value = getIndexedValue("amt", id);
		editform.eQty.value = getIndexedValue("qty", id);
	}


	if (getIndexedFormElement(mainform, "insClaimAmt", id) != null) {
		if(chargeHead == 'PKGPKG'){
			editform.eClaimAmt.value = formatAmountValue(gPackageChargeDetails[chargeId].insClaimAmt);
			editform.eDeductionAmt.value = formatAmountPaise((getPaise(editform.eAmt.value) - getPaise(editform.eClaimAmt.value)));
		}else{
			editform.eClaimAmt.value = getIndexedValue("insClaimAmt",id);
			editform.eDeductionAmt.value = formatAmountPaise((getPaise(editform.eAmt.value) - getPaise(editform.eClaimAmt.value)));
		}
		editform.ePreAuthId.value = getIndexedValue("preAuthId",id);
		editform.ePreAuthModeId.value = getIndexedValue("preAuthModeId",id);
		editform.ePriIncludeInClaimCalc.value =  getIndexedValue("priIncludeInClaim",id);
	}

	if(multiPlanExists && isTpa){
		if(chargeHead == 'PKGPKG'){
			editform.eClaimAmt.value = formatAmountValue(gPackageChargeDetails[chargeId].priInsClaimAmt);
			editform.eSecClaimAmt.value = formatAmountValue(gPackageChargeDetails[chargeId].secInsClaimAmt);
			editform.eDeductionAmt.value = formatAmountPaise((getPaise(editform.eAmt.value) - getPaise(editform.eClaimAmt.value) - getPaise(editform.eSecClaimAmt.value)));

		}else{
			editform.eClaimAmt.value = getIndexedValue("priInsClaimAmt",id);
			editform.eSecClaimAmt.value = getIndexedValue("secInsClaimAmt",id);
			editform.eDeductionAmt.value = formatAmountPaise((getPaise(editform.eAmt.value) - getPaise(editform.eClaimAmt.value) - getPaise(editform.eSecClaimAmt.value)));
		}
		editform.ePreAuthId.value = getIndexedValue("preAuthId",id);
		editform.ePreAuthModeId.value = getIndexedValue("preAuthModeId",id);
		editform.eSecPreAuthId.value = getIndexedValue("secPreAuthId",id);
		editform.eSecPreAuthModeId.value = getIndexedValue("secPreAuthModeId",id);
		editform.eSecIncludeInClaimCalc.value =  getIndexedValue("SecIncludeInClaim",id);
		if(editform.eSecClaimAmt != null){
			editform.eSecClaimAmt.readOnly = !isClaimEditable(id);
		}
	}

	if(isTpa)
		editform.eClaimLocked.value = getIndexedValue("isClaimLocked", id);

	YAHOO.lutsr.accordion.collapse(document.getElementById("discountDD"));
	var discType = showDiscounts(id);
	if (discType == 'split' && getPaise(getIndexedValue("disc", id)) > 0) {
		document.getElementById("splitDiscountImg").style.display = 'block';
		editform.discountType.checked = true;
	}else {
		document.getElementById("splitDiscountImg").style.display = 'none';
		editform.discountType.checked = false;
	}

	YAHOO.lutsr.accordion.collapse(document.getElementById("itemDetailsDD"));
	if (empty(getIndexedValue("userRemarks", id)) && empty(itemDesc))
		document.getElementById("itemDetailsImg").style.display = 'none';
	else
		document.getElementById("itemDetailsImg").style.display = 'block';

		showTaxDetails(row,id);

	if(chargeHead == 'PKGPKG') {
			getChargeClosure(chargeRefMap, chargeId).forEach(componentChargeId => {
			let rowId = getRowChargeIndex(getChargeRowByChargeId(componentChargeId));
			let rateElement = makeHidden('eRate_' + componentChargeId, 'eRate_' + componentChargeId, getIndexedValue('rate', rowId));
			rateElement.type="text";
			let amountElement = makeHidden('eAmt_' + componentChargeId, 'eAmt_' + componentChargeId, getIndexedValue('amt', rowId));
			rateElement.type="text";
			amountElement.type="text";
			let discountElement = makeHidden('overallDiscRs_' + componentChargeId, 'overallDiscRs_' + componentChargeId, getIndexedValue('disc', rowId));
			discountElement.className = 'make-orange'
			discountElement.type='text';
			
			let referralDoctorDiscountElement = makeHidden('discRefDocRs_' + componentChargeId,'discRefDocRs_' + componentChargeId, getIndexedValue("ref_discount_amt", rowId) );
			let conductingDoctorDiscountElement = makeHidden('discConductDocRs_' + componentChargeId,'discConductDocRs_' + componentChargeId, getIndexedValue("dr_discount_amt", rowId) );
			let prescribingDoctorDiscountElement = makeHidden('discPrescDocRs_' + componentChargeId,'discPrescDocRs_' + componentChargeId, getIndexedValue("pres_dr_discount_amt", rowId) );
			let hospitalDiscountElement = makeHidden('discHospUserRs_' + componentChargeId,'discHospUserRs_' + componentChargeId, getIndexedValue("hosp_discount_amt", rowId) );
			
			referralDoctorDiscountElement.type = 'text';
			conductingDoctorDiscountElement.type = 'text';
			prescribingDoctorDiscountElement.type = 'text';
			hospitalDiscountElement.type = 'text';
			
			if(isTpa){
				if(multiPlanExists){
					let priClaimAmtElement = makeHidden('eClaimAmt_' + componentChargeId,'eClaimAmt_' + componentChargeId, getIndexedValue("priInsClaimAmt", rowId) );
					let secClaimAmtElement = makeHidden('eSecClaimAmt_' + componentChargeId,'eSecClaimAmt_' + componentChargeId, getIndexedValue("secInsClaimAmt", rowId) );
					priClaimAmtElement.type="text";
					secClaimAmtElement.type="text";
					document.getElementById('package-component-details').append(priClaimAmtElement);
					document.getElementById('package-component-details').append(secClaimAmtElement);
				}else{
					let claimAmtElement = makeHidden('eClaimAmt_' + componentChargeId,'eClaimAmt_' + componentChargeId, getIndexedValue("priInsClaimAmt", rowId) );
					claimAmtElement.type="text";
					document.getElementById('package-component-details').append(claimAmtElement);
				}
			}

			document.getElementById('package-component-details').append(rateElement);
			document.getElementById('package-component-details').append(amountElement);
			document.getElementById('package-component-details').append(discountElement);
			document.getElementById('package-component-details').append(referralDoctorDiscountElement);
			document.getElementById('package-component-details').append(conductingDoctorDiscountElement);
			document.getElementById('package-component-details').append(prescribingDoctorDiscountElement);
			document.getElementById('package-component-details').append(hospitalDiscountElement);
		});
	}

	editChargeDialog.cfg.setProperty("context", [row.cells[EDIT_COL], "tr", "bl"], false);
	editChargeDialog.show();
	editform.eRemarks.focus();
	return false;
}

function showTaxDetails(row, id){
	var tbl = document.getElementById("editChgTbl");
	var chargeId	= getIndexedValue("chargeId", id);
	var chargeHead = getIndexedValue("chargeHeadId", id);
	var chargeGroup = getIndexedValue("chargeGroupId", id);

	var isNew = (chargeId.substring(0,1) == '_');
	if(isNew) chargeId = id;

	var taxSubGrps = document.getElementsByName(chargeId+"_sub_group_id");
	var taxRates = document.getElementsByName(chargeId+"_tax_rate");
	var taxAmts = document.getElementsByName(chargeId+"_tax_amt");
	var taxGrps = document.getElementsByName(chargeId+"_item_group_id");

	for(var i=0; i<taxSubGrps.length; i++){
		var len = tbl.rows.length;
		var taxRow = tbl.insertRow(tbl.rows.length);
		taxRow.setAttribute("id", "taxRow"+i);

		var cell1 = document.createElement("td");

		var subGrp = document.createElement("select");

		subGrp.setAttribute("name", "sub_group_id");
		subGrp.setAttribute("id", "sub_group_id"+i);
		subGrp.setAttribute("class", "dropdown");

		for(var k=0; k<taxSubGroupsList.length; k++){
			if(taxSubGroupsList[k].item_group_id == taxGrps[i].value){
				var option = document.createElement("option");
				option.setAttribute("value", taxSubGroupsList[k].item_subgroup_id);
				option.innerHTML = taxSubGroupsList[k].item_subgroup_name;
				subGrp.appendChild(option);
			}
		}

		setSelectedIndex(subGrp, taxSubGrps[i].value);

		subGrp.setAttribute("onChange", "checkSubGrpValidity(this.value,"+i+", '"+chargeId+"',"+id+")");
		if(!isBillItemEditable() || (allowTaxEditRights != 'A' && !(roleId == 1 || roleId == 2)))
			subGrp.setAttribute("disabled", true);

		if((chargeGroup == 'PKG' && chargeHead == 'PKGPKG') || chargeGroup != 'PKG'){
			cell1.appendChild(subGrp);
		}

		var cell2 = document.createElement("td");

		var taxRate = document.createElement("input");

		taxRate.setAttribute("type", "text");
		taxRate.setAttribute("name", "tax_rate");
		taxRate.setAttribute("id", "tax_rate"+i);
		taxRate.setAttribute("readOnly", true);
		taxRate.setAttribute("value", taxRates[i].value);

		cell2.appendChild(taxRate);

		var cell3 = document.createElement("td");

		var taxAmt = document.createElement("input");

		taxAmt.setAttribute("type", "text");
		taxAmt.setAttribute("name", "e_tax_amt");

		
		//get composed charges here and make hidden elements
		// move this block to appropriate place
		if(chargeHead == 'PKGPKG'){
			getChargeClosure(chargeRefMap, chargeId).forEach(componentChargeId => {
				let taxAmt = getElementIdAmount(componentChargeId + '_tax_amt' + i);
				let componentTaxAmtElement = makeHidden("e_tax_amt_" + componentChargeId + '_' + i, "e_tax_amt_" + componentChargeId + '_' + i);
				componentTaxAmtElement.type = "text";
				componentTaxAmtElement.value = taxAmt;
				componentTaxAmtElement.className = "make-violet";
				document.getElementById('package-component-details').append(componentTaxAmtElement);
			});
		}
			taxAmt.setAttribute("id", "e_tax_amt"+i);
		taxAmt.setAttribute("readOnly", true);
		if(chargeHead == 'PKGPKG') {
			taxAmt.setAttribute("value", formatAmountValue(gPackageChargeDetails[chargeId]['tax_amt' + i]));
		}else{
			taxAmt.setAttribute("value", taxAmts[i].value);
		}

		cell3.appendChild(taxAmt);

		var cellLbl1 = document.createElement("td");
		cellLbl1.setAttribute("class", "formlabel");
		cellLbl1.innerHTML = "Tax Sub Group:";

		var cellLbl2 = document.createElement("td");
		cellLbl2.setAttribute("class", "formlabel");
		cellLbl2.innerHTML = "Tax Rate:";

		var cellLbl3 = document.createElement("td");
		cellLbl3.setAttribute("class", "formlabel");
		cellLbl3.innerHTML = "Tax Amt:";

		taxRow.appendChild(cellLbl1);
		taxRow.appendChild(cell1);
		taxRow.appendChild(cellLbl2);
		taxRow.appendChild(cell2);
		taxRow.appendChild(cellLbl3);
		taxRow.appendChild(cell3);

	}

}

function setTaxAmt(subGrpId, taxId,id){
	var amtPaise = editform.eAmt.value;
	var chargeGroup = getIndexedValue("chargeGroupId", id);
	var actDescriptionId = getIndexedValue("descriptionId", id);
	var chargeHead = getIndexedValue("chargeHeadId", id);
	var chargeId = getIndexedValue("chargeId", id);
	var chgId = (chargeId.substring(0,1) == "_") ? id : chargeId;
	var consId = getIndexedValue( "consultation_type_id",id);
	var opId = getIndexedValue( "op_id",id);

	var subGrpIds = document.getElementsByName("sub_group_id");
	var subGrpIdValues = [];

	for(var i=0; i<subGrpIds.length; i++){
		subGrpIdValues[i] = subGrpIds[i].value;
	}

	if(chargeHead == 'PKGPKG'){
		let componentChargeIds = getChargeClosure(chargeRefMap, chargeId);

		let rowId = getRowChargeIndex(getChargeRowByChargeId(chargeId));
		let chargeGroup = getIndexedValue("chargeGroupId", rowId);
		let actDescriptionId = getIndexedValue("descriptionId", rowId);
		let chargeHead = getIndexedValue("chargeHeadId", rowId);
		let consId = getIndexedValue( "consultation_type_id",rowId);
		let opId = getIndexedValue( "op_id",rowId);
		let componentAmounts =  componentChargeIds.map(
			componentChargeId => document.getElementById('eAmt_' + componentChargeId).value
			);
		getPackageTaxDetails(taxId, componentChargeIds, chargeHead, chargeGroup, actDescriptionId, componentAmounts, consId, opId, subGrpIdValues, mr, billNumber,true);


		packageTax = componentChargeIds.reduce((acc, componentChargeId) => {
			return acc + getAmount(document.getElementById("e_tax_amt_" + componentChargeId + "_" + taxId ).value);
		}, 0);
		taxAmtObj.value = getAmount(packageTax);
		// });
	}else{
		getTaxDetails(taxId, chgId, chargeHead, chargeGroup, actDescriptionId, amtPaise, consId, opId, subGrpIdValues, mr, billNumber,true);
	}

	setEdited();

}

function checkSubGrpValidity(subGrpId, id, chargeId, rowId){
	var existingSubGrpValue = document.getElementsByName(chargeId+"_sub_group_id")[id].value;
	var rowId = getRowChargeIndex(getChargeRowByChargeId(chargeId));
	for(var k=0; k<taxSubGroupsList.length; k++){
		if(taxSubGroupsList[k].item_subgroup_id == subGrpId){
			if(!empty(taxSubGroupsList[k].validity_end)){
				var endDate = formatDate(new Date(taxSubGroupsList[k].validity_end), 'ddmmyyyy','-');
				var curDate = new Date();
				curDate = formatDate(curDate, 'ddmmyyyy','-');
				if(getDateDiff(curDate, endDate) < 0 ){
					alert(getString("js.billing.billlist.sub.group.expired"));
					document.getElementById("sub_group_id"+id).value = existingSubGrpValue;
					subGrpId = existingSubGrpValue;
				}
			}
		}
	}
	setTaxAmt(subGrpId, id, rowId);
	setInsCalcReq();
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


function recalcChargeAmount(chargeDetails) {
	var oldAmountPaise = getPaise(chargeDetails.eAmt);
	var rate = chargeDetails.eRate;
	var qty = chargeDetails.eQty;
	var discount = chargeDetails.discount;
	var id = chargeDetails.rowId;
    var calculatedAmounts = {};

	if (rate == "") { rate = 0; }
	if (qty == "") { qty = 0; }
	if (discount == "") { discount = 0; }

	if (!isSignedAmount(rate) || !isSignedAmount(qty) || !isSignedAmount(discount)) {
		return;
	}

	var changedRate = getPaise(rate);
	var changedQty = getAmount(qty);
	var changedDisc = getPaise(discount);

	var discountCategory = document.mainform.discountCategory;
	if(discountCategory != undefined && discountCategory.value != '' && discountCategory.value != 0 && ( !discChange )){
		var discCategoryAppl =  isDiscCategoryApplicable(id);
		if(discCategoryAppl && getIndexedValue("allowDiscount", id) == 'true'){

			var discountPlanDetails;
			if(document.mainform.discountCategory.value != ''){
				discountPlanDetails = getDiscountPlanDetails(document.mainform.discountCategory.value);
			}

			var discountRule = getDiscountRule(id,discountPlanDetails);
			var discountPer = 0;
			if ( discountRule == undefined ) {
				discountPer = 0;
				changedDisc = 0;
			}

			if ( discountRule != undefined ) {

				if ( discountRule.discount_type == 'P' ) {
					discountPer = getAmount(discountRule.discount_value);
					changedDisc = ((changedRate * changedQty) * discountPer) / 100;
				} else {
					//changedDisc = getPaise(discountRule.discount_value);
					if(changedDisc >= (changedRate*changedQty)){
						changedDisc = changedRate * changedQty;
					} else {
						changedDisc = getPaise(discountRule.discount_value);
					}

				}
			}

            calculatedAmounts.overallDiscRs = formatAmountPaise(changedDisc);
		}
	}

	var newAmtPaise = changedRate*changedQty - changedDisc;

	// update the claim amount if Insurance bill
	if (newAmtPaise > 0 && editform.eClaimAmt) {
		if (!isSignedAmount(chargeDetails.eClaimAmt))
			return;

		var chargeHead = getIndexedValue("chargeHeadId", id);
		var insuranceCategoryId = getIndexedValue("insuranceCategoryId", id);
		var firstOfCategory = getIndexedValue("firstOfCategory", id);
		var newClaimPaise = getClaimAmount(chargeHead, newAmtPaise,changedDisc,insuranceCategoryId, firstOfCategory);
		var row = getChargeRow(id);
		var newPatientPaise = 0;

		var oldClaimPaise = getPaise(chargeDetails.eClaimAmt);
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

        calculatedAmounts.eClaimAmt = formatAmountPaise(newClaimPaise);
        calculatedAmounts.eDeductionAmt = formatAmountPaise(newPatientPaise);
	}

	if (newAmtPaise == 0 &&  editform.eClaimAmt){
        calculatedAmounts.eClaimAmt = formatAmountPaise(getPaise(0));
        calculatedAmounts.eDeductionAmt = formatAmountPaise(getPaise(0));
	}
	// set the new amount
    calculatedAmounts.eAmt = formatAmountPaise(newAmtPaise);

	if (hasDynaPackage && !editform.eQtyIncluded.readOnly && getAmount(chargeDetails.eQtyIncluded) != 0) {
		// var amtIncludedObj = chargeDetails.eAmtIncluded;
		var qtyIncluded = chargeDetails.eQtyIncluded;

		var includedQty = getAmount(qtyIncluded);
		var amtPaiseIncluded = (newAmtPaise * includedQty) / changedQty;
		calculatedAmounts.amtIncluded = formatAmountPaise(amtPaiseIncluded);
	}


	
	return calculatedAmounts;
}

function getChargeDetails() {
	chargeDetails = {
		eAmt : editform.eAmt.value,
		eRate : editform.eRate.value,
		discount : (editform.discountType.checked) ? editform.totalDiscRs.value : editform.overallDiscRs.value,
		eQty: editform.eQty.value,
		rowId: editform.editRowId.value,
		eClaimAmt: editform.eClaimAmt.value
	}


}

function recalcEditChargeAmount() {
	var oldAmountPaise = getPaise(editform.eAmt.value);
	var rateObj = editform.eRate;
	var qtyObj = editform.eQty;
	var discObj = (editform.discountType.checked) ? editform.totalDiscRs : editform.overallDiscRs;
	var id = editform.editRowId.value;
	var chargeHead = getIndexedValue("chargeHeadId", id);
	var chargeId = getIndexedValue("chargeId", id);
	var isSplitDiscount = editform.discountType.checked;

	if(chargeHead == 'PKGPKG'){

		comprisedCharges = getChargeClosure(chargeRefMap, chargeId);
		// TODO name it better
		let calculatedValues = []
		comprisedCharges.forEach((componentChargeId) => {
			let componentChargeRow = getChargeRowByChargeId(componentChargeId);
			let componentRowId = getRowChargeIndex(componentChargeRow);

			let discount = 0;

			if(isSplitDiscount){
				discount = getElementIdAmount('discHospUserRs_'  + componentChargeId) + getElementIdAmount('discConductDocRs_' + componentChargeId) + 
					getElementIdAmount('discRefDocRs_' + componentChargeId) + getElementIdAmount('discPrescDocRs_' + componentChargeId);
			}else{
				discount =  getElementIdAmount("overallDiscRs_" + componentChargeId)
			}

			let chargeDetails = {
				eAmt: getElementIdAmount("eAmt_" + componentChargeId),
				eRate: getElementIdAmount("eRate_" + componentChargeId),
				eQty: getIndexedValue("qty", componentRowId),
				rowId: id,
				eClaimAmt: getIndexedValue("insClaimAmt", id),
				discount: discount
			}
			
			calculatedValues.push(recalcChargeAmount(chargeDetails));

			

			console.log('calculated values', calculatedValues[calculatedValues.length - 1]);
		})

		var subGrpIds = document.getElementsByName("sub_group_id");
		for(var i=0; i<subGrpIds.length; i++){
			setTaxAmt(subGrpIds[i].value,id, i);
		}

		displayValues = calculatedValues.reduce((acc,calculatedValue) => {
			acc['eAmt'] += getAmount(calculatedValue['eAmt']);
			return acc;
		}, {eAmt : 0});

		editform.eAmt.value = getAmount(displayValues['eAmt']);

		return;
	}
	if (rateObj.value == "") { rateObj.value = 0; }
	if (qtyObj.value == "") { qtyObj.value = 0; }
	if (discObj.value == "") { discObj.value = 0; }

	if (!isSignedAmount(rateObj.value) || !isSignedAmount(qtyObj.value) || !isSignedAmount(discObj.value)) {
		return;
	}

	var changedRate = getPaise(rateObj.value);
	var changedQty = getAmount(qtyObj.value);
	var changedDisc = getPaise(discObj.value);

	var discountCategory = document.mainform.discountCategory;
	if(discountCategory != undefined && discountCategory.value != '' && discountCategory.value != 0 && ( !discChange )){
		var discCategoryAppl =  isDiscCategoryApplicable(id);
		if(discCategoryAppl && getIndexedValue("allowDiscount", id) == 'true'){

			var discountPlanDetails;
			if(document.mainform.discountCategory.value != ''){
				discountPlanDetails = getDiscountPlanDetails(document.mainform.discountCategory.value);
			}

			var discountRule = getDiscountRule(id,discountPlanDetails);
			var discountPer = 0;
			if ( discountRule == undefined ) {
				discountPer = 0;
				changedDisc = 0;
			}

			if ( discountRule != undefined ) {

				if ( discountRule.discount_type == 'P' ) {
					discountPer = getAmount(discountRule.discount_value);
					changedDisc = ((changedRate * changedQty) * discountPer) / 100;
				} else {
					//changedDisc = getPaise(discountRule.discount_value);
					if(changedDisc >= (changedRate*changedQty)){
						changedDisc = changedRate*changedQty;
					} else {
						changedDisc = getPaise(discountRule.discount_value);
					}

				}
			}
			//adjust discounts here, maybe
			editform.overallDiscRs.value = formatAmountPaise(changedDisc);
		}
	}

	var newAmtPaise = changedRate*changedQty - changedDisc;

	// update the claim amount if Insurance bill
	if (newAmtPaise > 0 && editform.eClaimAmt) {
		if (!isSignedAmount(editform.eClaimAmt.value))
			return;

		// var chargeHead = getIndexedValue("chargeHeadId", id);
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

	if (hasDynaPackage && !editform.eQtyIncluded.readOnly && getAmount(editform.eQtyIncluded.value) != 0) {
		var amtIncludedObj = editform.eAmtIncluded;
		var qtyIncludedObj = editform.eQtyIncluded;

		var includedQty = getAmount(qtyIncludedObj.value);
		var amtPaiseIncluded = (newAmtPaise * includedQty) / changedQty;
		amtIncludedObj.value = formatAmountPaise(amtPaiseIncluded);
	}

	var subGrpIds = document.getElementsByName("sub_group_id");
	for(var i=0; i<subGrpIds.length; i++){
		setTaxAmt(subGrpIds[i].value,i,id);
	}
}

function setDeductionAmt(type) {
	// set the deduction amount as a result of change in claim amount directly edited
	var claimPaise = 0;
	if(typeof editform.eSecClaimAmt === "undefined") {
		claimPaise = getPaise(editform.eClaimAmt.value);
	} else {
		claimPaise = getPaise(editform.eClaimAmt.value) + getPaise(editform.eSecClaimAmt.value);
	}

	var amtPaise = getPaise(editform.eAmt.value);
	//check if insurance claim amount is greater that rate
	var isOldClaimValid = amtPaise<0 &&  claimPaise<0 ? ((amtPaise)<=(claimPaise)) : (amtPaise>= claimPaise);
	if(isOldClaimValid) {
	    editform.eDeductionAmt.value = formatAmountPaise(amtPaise - claimPaise);
	} else {
        var id = editform.editRowId.value;
        priClaim = getPaise(getIndexedValue("insClaimAmt", id));
        secClaim = getPaise(getIndexedValue("secInsClaimAmt", id));
        alert("Claim amount cannot be greater than rate " + formatAmountPaise(amtPaise));
        editform.eClaimAmt.value = formatAmountPaise(priClaim);
        editform.eSecClaimAmt.value = formatAmountPaise(secClaim);
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

function checkAssociatedCharges(id) {
	var chargeId = getIndexedValue("chargeId",id);
	var chargeHead = getIndexedValue("chargeHeadId",id);
	var chargeRef = getIndexedValue("chargeRef",id);
	var packageFinalized = getIndexedValue("packageFinalized",id);
	for (var i=0;i<getNumCharges();i++) {
		var delCharge = getIndexedFormElement(mainform, "delCharge", i);

		if (delCharge && "true" == delCharge.value)
			continue;

		if (empty(getIndexedValue("chargeRef",i)))
			continue;

		if (getIndexedValue("chargeHeadId",i) == 'BYBED')
			continue;

		if (getIndexedFormElement(mainform, "chargeRef", i).value == chargeId) {
			setIndexedValue("packageFinalized", i, packageFinalized);
			setIndexedValue("edited", i, 'true');
			setRowStyle(i);
			chargesEdited++;
		}
	}
}

function onEditSubmit(navigate) {

	var id = editform.editRowId.value;
	var chargeHead = getIndexedValue("chargeHeadId",id);
	if(!checkNegative())
	{
		return false;
	}
	if(!validatedisc(id))
	{
		return false;
	}
	var savedPostedDate = getIndexedValue("saved_posted_date",id);
	var savedPostedTime = getIndexedValue("saved_posted_time",id);

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
	if (editform.eCondDocRequired == 'Y' &&  editform.eConducted != null
		&& !editform.eConducted.disabled && editform.eConductedId.value == '') {
		showMessage("js.billing.billlist.conductingdoctor.required");
		editform.eConducted.focus();
		return false;
	}

	var packageFinalizedObj = editform.ePackageFinalized;

	var amtIncludedObj = editform.eAmtIncluded;
	var origAmtIncludedObj = editform.eAmtIncludedOrig;

	var qtyIncludedObj = editform.eQtyIncluded;
	var origQtyIncludedObj = editform.eQtyIncludedOrig;

	if (chargeHead == 'INVRET') {
		if (amtIncludedObj != null && !amtIncludedObj.readOnly)
			if (!validateSignedAmount(amtIncludedObj, getString("js.billing.billlist.includedamount.validamount")))
				return false;
		if (qtyIncludedObj != null && !qtyIncludedObj.readOnly)
			if (!validateSignedAmount(qtyIncludedObj, getString("js.billing.billlist.includedquantity.validamount")))
				return false;
	}else {
		if (amtIncludedObj != null && !amtIncludedObj.readOnly)
			if (!validateAmount(amtIncludedObj, getString("js.billing.billlist.includedamount.validamount")))
				return false;
		if (qtyIncludedObj != null && !qtyIncludedObj.readOnly)
			if (!validateAmount(qtyIncludedObj, getString("js.billing.billlist.includedquantity.validamount")))
				return false;
	}

	if (hasDynaPackage) {

		var pkgMarginClaimable = 'N';
		var pkgMarginRowId = getPackageMarginRowId();

		if (pkgMarginRowId != null) {
			if (typeof(CLAIM_COL) != 'undefined')
				pkgMarginClaimable = getIndexedFormElement(mainform, "insClaimable", pkgMarginRowId).value;

		if (chargeHead == 'INVRET') {
			if (isChargeQuantityIncludedEditable(id)) {
				if (Math.abs(getAmount(editform.eQty.value)) < Math.abs(getAmount(qtyIncludedObj.value))) {
					var msg=getString("js.billing.billlist.includedquantity.notgreaterthanquantity");
					msg+=formatAmountValue(editform.eQty.value);
					msg+=" )";
					alert(msg);
					qtyIncludedObj.focus();
					return false;
				}
			}
		}else {
			// Validate quantity included only when qty is editable.
			if (isChargeQuantityIncludedEditable(id)) {
				if (getAmount(editform.eQty.value) < getAmount(qtyIncludedObj.value)) {
					var msg=getString("js.billing.billlist.includedquantity.notgreaterthanquantity");
					msg+=formatAmountValue(editform.eQty.value);
					msg+=" )";
					alert(msg);
					qtyIncludedObj.focus();
					return false;
				}
			}
		}

		// Validate amount for charges for which amount or qty is editable.
		if (isChargeAmountIncludedEditable(id) || isChargeQuantityIncludedEditable(id)) {
			if (isTpa && pkgMarginClaimable) {

				if (chargeHead == 'INVRET') {
					if (Math.abs(getPaise(editform.eClaimAmt.value)) < Math.abs(getPaise(amtIncludedObj.value))) {
						alert(getString("js.billing.billlist.includedamount.notgreaterthanclaimamount") + formatAmountValue(editform.eClaimAmt.value) +" )");
						amtIncludedObj.focus();
						return false;
					}
				}else {
					if (getPaise(editform.eClaimAmt.value) < getPaise(amtIncludedObj.value)) {
						alert(getString("js.billing.billlist.includedamount.notgreaterthanclaimamount") + formatAmountValue(editform.eClaimAmt.value) +" )");
						amtIncludedObj.focus();
						return false;
					}
				}
			}else {
				if (chargeHead == 'INVRET') {
					if (Math.abs(getPaise(editform.eAmt.value)) < Math.abs(getPaise(amtIncludedObj.value))) {
						alert(getString("js.billing.billlist.includedamount.notgreaterthanamount") + formatAmountValue(editform.eAmt.value) +" )");
						amtIncludedObj.focus();
						return false;
					}
				}else {
					if (getPaise(editform.eAmt.value) < getPaise(amtIncludedObj.value)) {
						alert(getString("js.billing.billlist.includedamount.notgreaterthanamount") + formatAmountValue(editform.eAmt.value) +" )");
						amtIncludedObj.focus();
						return false;
					}
				}
			}
		}
		// Included qty is zero if amount included is zero.
		qtyIncludedObj.value = (getPaise(amtIncludedObj.value) == 0) ? 0 : qtyIncludedObj.value ;

			if ( chargeHead == 'INVITE' ) {
				//include returns proportionately
				var agnstAmt =  getPaise(getIndexedValue("amt", id));
				if (agnstAmt != 0) {
					var amtIncludedPaise = getPaise(amtIncludedObj.value);
					includeReferenceCharges(getIndexedValue("chargeId",id),(100*amtIncludedPaise/agnstAmt));
				} else {
					includeReferenceCharges(getIndexedValue("chargeId",id),0);
				}
			}
		}
	}

	var postedDateObj = editform.ePostedDate;
	var postedTimeObj = editform.ePostedTime;

	if (!doValidateDateField(postedDateObj, 'past')) {
		postedDateObj.focus();
		return false;
	}
	if(allowBackDate != 'A') {
		if (savedPostedDate == "")
			savedPostedDate = formatDate(getServerDate(), "ddmmyyyy", "-");
		if (savedPostedTime == "")
			savedPostedTime = formatTime(getServerDate(), false);

		if(!doValidateDateFieldWithValiddate(postedDateObj,postedTimeObj,'past',savedPostedDate,savedPostedTime)) {
			postedDateObj.focus();
			return false;
		}
	}
	if (!validateTime(postedTimeObj)) {
		postedTimeObj.focus();
		return false;
	}
	if(!validatePriorAuthMode(editform.ePreAuthId, editform.ePreAuthModeId,null,null)){
		return false;
	}
	if(isUserLimitExceeded(id)) {
		alert(getString("js.billing.billlist.discountnot.greaterthan.user.permissible.edit")+userPermissibleDiscount+"% after editing item details.");
		editform.overallDiscRs.value = getIndexedFormElement(mainform, "disc", id).value;
		editform.eQty.value = getIndexedValue("qty", id);
		editform.eAmt.value = getIndexedValue("amt", id);
		editform.eRate.value = getIndexedValue("rate", id);
		return false;
	}
	var row = getChargeRow(id);

	if (!validateDiscounts(id))
		return false;

	resetDiscountTotalRs();

	var disc = 0;

	if (editform.discountType.checked) {
		disc = formatAmountValue(editform.totalDiscRs.value);
	}else {
		disc = formatAmountValue(editform.overallDiscRs.value);
	}
	if(checkDiscLimitEditCharge(id, getPaise(disc))) {
		alert(getString("js.billing.billlist.discountnot.greaterthanuserlimit")+userLimit);
		editform.overallDiscRs.value = getIndexedFormElement(mainform, "disc", id).value;
		editform.eQty.value = getIndexedValue("qty", id);
		editform.eAmt.value = getIndexedValue("amt", id);
		editform.eRate.value = getIndexedValue("rate", id);
		return false;
	}
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

	var packageId = getIndexedValue("packageId",id);

	if (chargeHead=='PHCMED' || chargeHead=='PHCRET' || chargeHead=='PHMED' || chargeHead=='PHRET')	{
		row.cells[REMARKS_COL].innerHTML = '<a target="#" title="No.'+chargeRemarks+'"'
							+ 'href='+cpath+'/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&printerId=0&duplicate=true&saleId='
							+ chargeRemarks+'>'
							+ backupremarks+'</a>';

	}if (chargeHead == 'INVITE'&& packageId)	{
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
		setNodeText(row.cells[PRE_AUTH_COL], editform.ePreAuthId.value, 10);
		setIndexedValue("preAuthId",id, editform.ePreAuthId.value);
		setIndexedValue("preAuthModeId",id, editform.ePreAuthModeId.value);

		var table = YAHOO.util.Dom.getAncestorByTagName(row, 'table');
		var chargeHeadId = getElementByName(row,"chargeHeadId").value;
		deduction = formatAmountPaise(getPaise(amt)-getPaise(claim));
	}

	var oldClaim = 0;
	if (editform.eClaimAmt != null)
		oldClaim = getPaise(getIndexedValue("insClaimAmt", id));

	if(chargeHead == 'PKGPKG'){
		let chargeId = getIndexedValue("chargeId", id);
		getChargeClosure(chargeRefMap, chargeId).forEach(componentChargeId => {
			let componentRow = getChargeRowByChargeId(componentChargeId);
			let componentRowId = getRowChargeIndex(componentRow);
			let componentRate = editform['eRate_' + componentChargeId].value;
			let componentAmount = editform['eAmt_' + componentChargeId].value;
			let priClaimAmount = "";
			let secClaimAmount = "";
			if(isTpa){
				priClaimAmount = editform['eClaimAmt_' + componentChargeId].value;
				if(multiPlanExists){
					secClaimAmount = editform['eSecClaimAmt_' + componentChargeId].value;
				}
			}
			let componentDeduction = formatAmountPaise(getPaise(componentAmount) - getPaise(priClaimAmount));
			let componentDiscount = "0";
			if(editform.discountType.checked){
				componentDiscount = getElementIdAmount('discHospUserRs_'  + componentChargeId) + getElementIdAmount('discConductDocRs_' + componentChargeId) + 
					getElementIdAmount('discRefDocRs_' + componentChargeId) + getElementIdAmount('discPrescDocRs_' + componentChargeId);
			}else{
				componentDiscount = editform['overallDiscRs_' + componentChargeId].value;
			}
			// let componentDiscount = editform['overallDiscRs_' + componentChargeId].value;
			let componentQuantity = getIndexedValue('qty', componentRowId);
			setEditedAmounts(componentRowId, componentRow, componentRate, componentQuantity, componentDiscount, componentAmount, priClaimAmount, componentDeduction,/*0,0,0*/);
			if(multiPlanExists && isTpa) {
				if(secClaimAmount != null) {
					componentDeduction = formatAmountPaise(getPaise(componentAmount)-getPaise(priClaimAmount)-getPaise(secClaimAmount));
				}
				setSecondaryPlanAmounts(componentRowId, editform, componentRow ,componentAmount,priClaimAmount,componentDeduction,secClaimAmount);
			}

			if(isTpa){
				setIndexedValue("isClaimLocked", componentRowId, editform.eClaimLocked.value);
				setIndexedValue("priIncludeInClaim", componentRowId, editform.ePriIncludeInClaimCalc.value);
				if(multiPlanExists){
					setIndexedValue("secIncludeInClaim", componentRowId, editform.eSecIncludeInClaimCalc.value);
				}
			}
		});
		
	}else{
		setEditedAmounts(id, row, rate, qty, disc, amt, claim, deduction);
		if(multiPlanExists && isTpa) {
			var secClaimAmt = 0;
			if(undefined != editform.eSecClaimAmt ||  editform.eSecClaimAmt != null) {
				secClaimAmt = editform.eSecClaimAmt.value;
				deduction = formatAmountPaise(getPaise(amt)-getPaise(claim)-getPaise(secClaimAmt));
			}
			setSecondaryPlanAmounts(id, editform, row ,amt,claim,deduction,secClaimAmt);
		}
	}


	if (editform.eConducted.value = '')
		setIndexedValue("payeeDocId", id, '');
	else
		setIndexedValue("payeeDocId", id, editform.eConductedId.value);

	if (hasDynaPackage) {
		if (isChargeQuantityIncludedEditable(id) && getAmount(qtyIncludedObj.value) != 0) {

			var includedQty = getAmount(qtyIncludedObj.value);
			var amtPaiseIncluded = (getPaise(amt) * includedQty) / getAmount(qty);

			setIndexedValue("qty_included", id, formatAmountValue(qtyIncludedObj.value, true));
			setIndexedValue("amount_included", id, formatAmountPaise(amtPaiseIncluded));

		}else if (isChargeAmountIncludedEditable(id)) {
			setIndexedValue("qty_included", id, formatAmountValue(0, true));
			setIndexedValue("amount_included", id, formatAmountValue(amtIncludedObj.value));
		}

		setIndexedValue("packageFinalized", id, packageFinalized);
		checkAssociatedCharges(id);
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
			setIndexedValue("dynaPackageExcluded", id, "Y");

		}else {
			var amountToIncludePaise = getIndexedPaise("amt", id);

			var chargeExcluded = "P";
			if (amtIncludedObj != null) {
				if (getPaise(amtIncludedObj.value) == amountToIncludePaise)
					chargeExcluded = "N";
				else if (getPaise(amtIncludedObj.value) == 0)
					chargeExcluded = "Y";
			}
			setIndexedValue("chargeExcluded", id, chargeExcluded);
			setIndexedValue("dynaPackageExcluded", id, chargeExcluded);
		}
	}else {
		setIndexedValue("chargeExcluded", id, "N");
		setIndexedValue("dynaPackageExcluded", id, "N");
	}

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);
	chargesEdited++;
	filterCharges();
	if (hasDynaPackage) {
		onChangePkgAmtQty(origAmtIncludedObj.value, origQtyIncludedObj.value, id);
	}
	var chargeGroup = getIndexedValue("chargeGroupId", id);
	var actDescriptionId = getIndexedValue("descriptionId", id);

	var chargeId = getIndexedValue("chargeId", id);
	var chgId = (chargeId.substring(0,1) == "_") ? id : chargeId;
	var consId = getIndexedValue( "consultation_type_id",id);
	var opId = getIndexedValue( "op_id",id);

	var subGrpIds = document.getElementsByName("sub_group_id");
	var subGrpIdValues = [];

	for(var i=0; i<subGrpIds.length; i++){
		subGrpIdValues[i] = subGrpIds[i].value;
	}
	if(chargeHead == 'PKGPKG'){
		let componentChargeIds = getChargeClosure(chargeRefMap, chargeId);
		let componentAmounts =  componentChargeIds.map(componentChargeId => document.getElementById('eAmt_' + componentChargeId).value);
		document.getElementById("package-component-details").innerHTML = "";

		getPackageTaxDetails(id, componentChargeIds, chargeHead, chargeGroup, actDescriptionId, componentAmounts, consId, opId, subGrpIdValues, mr, billNumber);
		
	}else{
		getTaxDetails(row, chgId, chargeHead, chargeGroup, actDescriptionId, amt, consId, opId, subGrpIdValues, mr, billNumber);
	}


	var insCalcReq = document.getElementById("eInsCalcReq").value;
	var visitID = document.getElementById('visitId').value;
	if(insCalcReq == 'true' && tpaBill){
		getBillChargeClaims(visitID, document.mainform);
	}
	
	resetTotals(editedAmounts(id, oldClaim), false);

	YAHOO.util.Dom.removeClass(row, 'editing');
	deleteTaxRows(row);
	if (!navigate)
		editChargeDialog.hide();
	var editImg = row.cells[EDIT_COL].children[0];
	editImg.focus();
}

function setTaxDetailsToGrid(row, chargeId){

	var tblSubGrps = document.getElementsByName(chargeId+"_sub_group_id");
	var tblTaxrates = document.getElementsByName(chargeId+"_tax_rate");
	var tblTaxAmts = document.getElementsByName(chargeId+"_tax_amt");

	var taxSubGrpIds = document.getElementsByName("sub_group_id");
	var taxRates = document.getElementsByName("tax_rate");
	var taxAmts = document.getElementsByName("e_tax_amt");

	var totalTaxPaise = 0;
	for(i=0; i<taxSubGrpIds.length; i++){
		tblSubGrps[i].value = taxSubGrpIds[i].value;
		tblTaxrates[i].value = taxRates[i].value;
		tblTaxAmts[i].value = taxAmts[i].value;
		totalTaxPaise = totalTaxPaise + getPaise(taxAmts[i].value);
	}

	var totalTax = document.getElementById("total_tax_"+chargeId);
	totalTax.value = formatAmountPaise(totalTaxPaise);

	setNodeText(row.cells[TAX_COL], formatAmountPaise(totalTaxPaise));

}

function onEditCancel(){
	authChange = false;
	discChange = false;
	qtyChange = false;
	rateChange = false;
	document.getElementById('package-component-details').innerHTML="";
	var id = editform.editRowId.value;
	var row = getChargeRow(id);
	YAHOO.util.Dom.removeClass(row, 'editing');
	editChargeDialog.hide();
	deleteTaxRows(row);
	var editImg = row.cells[EDIT_COL].children[0];
	editImg.focus();
}

function deleteTaxRows(row){
	var tbl = document.getElementById("editChgTbl");
	var id = getRowChargeIndex(row);
	var chargeId = getIndexedValue("chargeId", id);

	if (null != chargeId && "" != chargeId && undefined != chargeId)
		chargeId = (chargeId.substring(0, 1) == '_') ? id : chargeId;

	var taxRowsCnt = getIndexedValue("taxesCnt_"+chargeId, id);

	if(taxRowsCnt != undefined){
		for(var i=0; i<taxRowsCnt; i++){
			var taxrow = document.getElementById("taxRow"+i);
			if(null != taxrow){
				tbl.deleteRow(taxrow.rowIndex);
			}
		}
	}
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

function selectAllForDiscounts() {
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

function chkRateVariation() {

	var id = editform.editRowId.value;
	// var chargeHead = getIndexedValue("chargeHeadId", id);
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
	 
	 // add package charge delta validation here
}

function getChargeRowByChargeId(chargeIds){
	var table = document.getElementById("chargesTable");
	if(!(chargeIds instanceof Array)){
		chargeIds = [chargeIds];
		var individual = true;
	}
	chargeRows =  chargeIds.map(childChargeId => Array.from(table.rows).find(row => getElementByName(row, 'chargeId') && getElementByName(row, 'chargeId').value == childChargeId));
	if(individual){
		return chargeRows[0];
	}else{
		return chargeRows;
	}
}


// split amounts to package contents
// TODO merge into recalcEditCharges if possible
function adjustPackageCharges() {
	var id = editform.editRowId.value;
	var chargeId = getIndexedValue("chargeId", id);
	var chargeHead = getIndexedValue("chargeHeadId", id);

	//These amounts are for the margin charge
	var rate = getIndexedValue("rate", id);
	var disc = getIndexedValue("disc", id);
	var conductingDoctorDisc = getIndexedValue("dr_discount_amt", id);
	var prescribingDoctorDisc = getIndexedValue("pres_dr_discount_amt", id);
	var referralDoctorDisc = getIndexedValue("ref_discount_amt", id);
	var hospitalDisc = getIndexedValue("hosp_discount_amt", id);

	
	let isSplitDiscount = editform.discountType.checked;

	if(chargeHead != 'PKGPKG'){
		return true;
	}

	let chargeRowsDetails = getPackageChargeDetails(chargeId);

	billedChargeSum = chargeRowsDetails.reduce((acc, charge) => charge.chargeHead !== 'PKGPKG' ? acc + charge.amt : acc, 0);

	if(billedChargeSum == 0){
		billedChargeSum = chargeRowsDetails.length - 1;
		chargeRowsDetails.forEach(charge => {
			if(charge.chargeHead !== 'PKGPKG')
				charge.rate = 1
			});
	}
	
	var rateObj = editform.eRate;
	var qtyObj = editform.eQty;
	
	var changedConductingDoctorDiscount = getPaise(editform.discConductDocRs.value);
	var changedReferralDoctorDiscount = getPaise(editform.discRefDocRs.value);
	var changedPrescribingDoctorDiscount = getPaise(editform.discPrescDocRs.value);
	var changedHospitalDiscount = getPaise(editform.discHospUserRs.value);
	if(isTpa){
		var changedPriClaimAmt = getPaise(editform.eClaimAmt.value);
		if(multiPlanExists){
			var changedSecClaimAmt = getPaise(editform.eSecClaimAmt.value);
		}
	}

	

	var discObj = (editform.discountType.checked) ? editform.totalDiscRs : editform.overallDiscRs;
	
	var changedRate = getPaise(rateObj.value) - getPaise(rate);
	// var changedQty = getAmount(qtyObj.value);
	var changedDisc = getPaise(discObj.value) - getPaise(disc);
	
	
	if(isSplitDiscount){
		var totalSplitDisc = changedConductingDoctorDiscount + changedPrescribingDoctorDiscount + changedReferralDoctorDiscount + changedHospitalDiscount;
		var marginConductingDoctorDiscount = getPaise(disc) * changedConductingDoctorDiscount/ totalSplitDisc;
		var marginPrescribingDoctorDiscount = getPaise(disc) * changedPrescribingDoctorDiscount/ totalSplitDisc;
		var marginReferralDoctorDiscount = getPaise(disc) * changedReferralDoctorDiscount/ totalSplitDisc;
		var marginHospitalDiscount = getPaise(disc) * changedHospitalDiscount/ totalSplitDisc;

		changedConductingDoctorDiscount -= marginConductingDoctorDiscount;
		changedPrescribingDoctorDiscount -= marginPrescribingDoctorDiscount;
		changedReferralDoctorDiscount -= marginReferralDoctorDiscount;
		changedHospitalDiscount -= marginHospitalDiscount;

		editform['discHospUserRs_' + chargeId].value = formatAmountPaise(marginHospitalDiscount);
		editform['discConductDocRs_' + chargeId].value = formatAmountPaise(marginConductingDoctorDiscount);
		editform['discRefDocRs_' + chargeId].value = formatAmountPaise(marginReferralDoctorDiscount);
		editform['discPrescDocRs_' + chargeId].value = formatAmountPaise(marginPrescribingDoctorDiscount);
	}
	
	
	let packageComponentDetails = document.getElementById("package-component-details");

	chargeRowsDetails.forEach(charge => {
		if(charge.chargeHead !== 'PKGPKG'){
			charge.rate = changedRate * charge.amt / billedChargeSum;
			if(!isSplitDiscount){
				charge.disc = changedDisc * charge.amt / billedChargeSum;
			} else {
				charge.hospital_disc = changedHospitalDiscount * charge.amt / billedChargeSum;
				charge.prescribed_doctor_disc = changedPrescribingDoctorDiscount * charge.amt / billedChargeSum;
				charge.referral_doctor_disc = changedReferralDoctorDiscount * charge.amt / billedChargeSum;
				charge.conducting_doctor_disc = changedConductingDoctorDiscount * charge.amt / billedChargeSum;
				charge.disc = charge.hospital_disc + charge.prescribed_doctor_disc + charge.referral_doctor_disc + charge.conducting_doctor_disc;
			}
			
			charge.priInsClaimAmt = changedPriClaimAmt * charge.amt / billedChargeSum;
			charge.secInsClaimAmt = changedSecClaimAmt * charge.amt / billedChargeSum;
			document.getElementById('eAmt_' + charge.charge_id).value = formatAmountPaise(charge.rate - charge.disc);
			document.getElementById('eRate_' + charge.charge_id).value = formatAmountPaise(charge.rate / charge.qty);
			
			if(isTpa){
				document.getElementById('eClaimAmt_' + charge.charge_id).value = formatAmountPaise(charge.priInsClaimAmt);
				if(multiPlanExists){
					document.getElementById('eSecClaimAmt_' + charge.charge_id).value = formatAmountPaise(charge.secInsClaimAmt);
				}
			}
			if(!isSplitDiscount){
				document.getElementById('overallDiscRs_' + charge.charge_id).value = formatAmountPaise(charge.disc);
			}else{
				editform['discHospUserRs_' + charge.charge_id].value = formatAmountPaise(charge.hospital_disc);
				editform['discConductDocRs_' + charge.charge_id].value = formatAmountPaise(charge.conducting_doctor_disc);
				editform['discRefDocRs_' + charge.charge_id].value = formatAmountPaise(charge.referral_doctor_disc);
				editform['discPrescDocRs_' + charge.charge_id].value = formatAmountPaise(charge.prescribed_doctor_disc);

			}
		}
	})


	

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

function validateBillOpenDate(){
    var valid = true;

    valid = valid && validateRequired(document.mainform.opendate, getString("js.billing.billlist.opendate.req"));
	if(!valid) {
		document.mainform.opendate.focus();
		return false;
	}

	valid = valid && validateRequired(document.mainform.opentime, getString("js.billing.billlist.opentime.req"));
	if(!valid) {
		document.mainform.opentime.focus();
		return false;
	}

	var regDtTime =   getDateTime(regDate, regTime);
    var openDtTime  = getDateTime(document.mainform.opendate.value, document.mainform.opentime.value);

	if (!doValidateDateField(document.mainform.opendate, 'past')) {
		document.mainform.opendate.focus();
		return false;
	}

	if (!validateTime(document.mainform.opentime)) {
		document.mainform.opentime.focus();
		return false;
	}

	if(trim(document.mainform.opentime.value.length)!=5){
		showMessage("js.billing.billlist.opentime.format");
		regTimeObj.focus();
		return false;
	}


    if(openDtTime.getTime() < regDtTime.getTime()) {
    	alert(getString('js.billing.billlist.opendate.validation'));
    	document.mainform.opendate.focus();
		return false;
    }


    return true;
}

function setOpenDateFldStatus(){
	document.mainform.opendate.readOnly = true;
	document.mainform.opentime.readOnly = true;
}

function validateActivityDate(){
	var valid = true;
	for (var i=0;i<getNumCharges();i++) {
		var postedDate = getIndexedFormElement(mainform, 'postedDate',i).value;
		var postedTime = getIndexedFormElement(mainform, 'postedTime',i).value;
		var cancelled = getIndexedValue("delCharge", i) == 'true';
		var postedDtTime  = getDateTime(postedDate, postedTime);

		var openDate = document.mainform.opendate.value;
		var openTime = document.mainform.opentime.value;
		var openDtTime  = getDateTime(openDate,openTime );

		if ( cancelled ){
			continue;
		}

		if(postedDtTime < openDtTime) {
			valid = false;
			break;
		}
	}

	if ( !valid ){
		alert(getString('js.billing.billlist.activity.posted.date'));
		document.mainform.opendate.focus();
	}
	return valid;
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


function setDiscEdited(obj){
	discEdited = ( obj.name == 'overallDiscRs' );
}

function validatedisc(id){
	var chargeHead = getIndexedValue("chargeHeadId", id);
	if(editform.eRate.value>=0 && chargeHead != "BIDIS"){

		if( editform.eQty.value < 0 ){
			if ( editform.overallDiscRs.value != 0 && editform.overallDiscRs.value > editform.eRate.value * editform.eQty.value ){
				alert(getString("js.billing.billlist.discountnot.greaterthanamount"));
				editform.overallDiscRs.value = getIndexedFormElement(mainform, "disc", id).value;
				editform.eQty.value = getIndexedValue("qty", id);
				editform.eAmt.value = getIndexedValue("amt", id);
				return false;
			}
		} else {

			if(editform.eRate.value * editform.eQty.value < editform.overallDiscRs.value){
				alert(getString("js.billing.billlist.discountnot.greaterthanamount"));
				editform.overallDiscRs.value = getIndexedFormElement(mainform, "disc", id).value;
				editform.eQty.value = getIndexedValue("qty", id);
				editform.eAmt.value = getIndexedValue("amt", id);
				return false;
			}
		}



		if(editform.discountType.checked && editform.totalDiscRs.value > editform.eRate.value * editform.eQty.value){
			alert(getString("js.billing.billlist.discountnot.greaterthanamount"));
			editform.discConductDocRs.value = formatAmountPaise(getIndexedValue("discount_auth_dr", id));
			editform.discPrescDocRs.value  = formatAmountPaise(getIndexedValue("discount_auth_pres_dr", id));
			editform.discRefDocRs.value = formatAmountPaise(getIndexedValue("discount_auth_ref", id));
			editform.discHospUserRs.value = formatAmountPaise(getIndexedValue("discount_auth_hosp", id));
			editform.totalDiscRs.value = formatAmountPaise(getIndexedValue("discount_auth_hosp", id));
			editform.eAmt.value = getIndexedValue("amt", id);
			return false;
		}
		}
	return true;
}

function closeEmailDiv(){
	$(".emailDialog").css("display", "none");
}
function showemaildialog(billTypeDisplay){
	if($("#emailDialog").is(':visible') || $("#emailDialogStatus").is(':visible')){
		$("#emailDialog").css("display", "none");
		$("#emailDialogStatus").css("display", "none");
	}
	else{
	var offsets = $('#emailButton').offset();
	var top = offsets.top;
	var left = offsets.left;
	$(".emailDialog").css("top",top-170);
	$(".emailDialog").css("left",left-256);
	document.getElementById("emailVal").value=patientEmailId;
	$("#emailDialogStatus").css("display", "none");
	$("#emailDialog").css("display", "block");
	if($("#emailVal").val() == ''){
		eventTracking('Bill over mail','email-id is blank when email button is clicked','Visit Type-' + billTypeDisplay );
	}
	eventTracking('Bill over mail','Email button is clicked','Visit Type-' + billTypeDisplay );

	validateEmail();
	}
}
$(window).resize(function() {
	if ($('#emailButton').offset() != undefined) {
		var offsets = $('#emailButton').offset();
		var top = offsets.top;
		var left = offsets.left;
		$(".emailDialog").css("top", top - 170);
		$(".emailDialog").css("left", left - 256);
	}
});
$(document).click(function() {
	if ($('#emailButton').offset() != undefined) {
		var offsets = $('#emailButton').offset();
		var top = offsets.top;

		var left = offsets.left;
		$(".emailDialog").css("top", top - 170);
		$(".emailDialog").css("left", left - 256);
	}
});
function validateEmail(){
	var emailIdValue= $("#emailVal").val();
	var filter=/\w{1,}[@][\w-]{1,}([.]([\w-]{1,})){1,3}$/
		///^\w+([-+.'][^\s]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/;
		///^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i
	if (filter.test(emailIdValue)){
		$("#emailVal").css("border","1px #D8D8D8 solid");
		$("#sendMailButton").prop("disabled",false);
		$("#errormsg").css("visibility", "hidden");
	}
	else{
		if (emailIdValue != ''){
			$("#errormsg").css("visibility", "visible");
		}
		else{
			$("#errormsg").css("visibility", "hidden");
		}
		$("#sendMailButton").prop("value","Save & Send");
		$("#emailVal").css("border","1px #D0021B solid");
		$("#sendMailButton").prop("disabled",true);
	}
}
function updateSendMail(newemail,initialEmail,billTypeDisplay){
	document.getElementById("emailStatusVal").innerHTML = "Sending...";
	$("#emailDialogStatus").css("display","block");
	$("#emailDialog").css("display","none");
	var Template = document.getElementById("printSelect");
	var Templatevalue = Template.options[Template.selectedIndex].value;
	var printType = document.getElementsByName("printType")[0];
	var printTypeValue = printType.options[printType.selectedIndex].value;

	if(initialEmail != '' && initialEmail != null && newemail != initialEmail){
		eventTracking('Bill over mail','email-id is edited when some value existed','Visit Type-' + billTypeDisplay );
	}
	eventTracking('Bill over mail','Send button is clicked','Visit Type-' + billTypeDisplay  );

	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "//billing/BillAction.do?_method=updateAndSendEmail&newEmail="+newemail+"&patientMrno="+mr+"&template="+Templatevalue+"&printType="+printTypeValue+"&billNo="+billNumber;
	ajaxobj.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
    	try{
	    	var json = JSON.parse(this.responseText);
	    	if(json['isUpdated'] == 'true'){
	    	patientEmailId=newemail;
	    	}
	    	if(json['result'] == 'true'){
	    		document.getElementById("emailStatusVal").innerHTML = "Message Sent Successfully to Patient's Registered Email.";
	    	}
	    	else{
	    		document.getElementById("emailStatusVal").innerHTML = "Message Sending Failed.<br>Please Contact Your System Administrator.";
	    	}
    	}
    	catch(e){
    		document.getElementById("emailStatusVal").innerHTML = "Message Sending Failed.<br>Please Contact Your System Administrator.";
    	}
	}
	}
	ajaxobj.open("POST", url.toString(), true);
	ajaxobj.send(null);
}

$(document).keyup(function(e) {
    if (e.which == 27) {
    	$(".emailDialog").css("display", "none");
    }
});
$(function() {
	$('#emailVal').keypress(function(event) {
		if (event.keyCode == 13) {
			event.preventDefault();
			if (!($('#sendMailButton').is(':disabled'))) {
				$('#sendMailButton').click();
			}
		}
	});
});

function enableWriteOff(){
	if(document.getElementById("billStatus").value == 'F'
		&& null != document.getElementById("claim_closure_type") && getTotalSpnsrAmountDue() > 0) {
		document.getElementById("claim_closure_type").disabled = false;
	}
}

function setWriteOffFlag(){
	if(null != document.getElementById("claim_closure_type")){
		if(document.getElementById("claim_closure_type").checked)
			document.getElementById("sponsor_writeoff").value ="M";
		else
			document.getElementById("sponsor_writeoff").value ="";
	}
}

function doValidateDateFieldWithValiddate(dateInput,timeInput, validType,validDate,validTime) {

    var dateStr = dateInput.value;
    if (dateStr == "") {
        return false;
    }

    var errorStr = validateDateFormat(dateStr);
    if (errorStr != null) {
        alert(errorStr);
        dateInput.focus();
        return false;
    }

    dateStr = cleanDateStr(dateStr, validType);
    var newPostedDate = mergeDateAndTime(dateStr, timeInput.value);
    var oldPostedDate = mergeDateAndTime(validDate, validTime);
    if(newPostedDate == null && oldPostedDate == null){
    	dateInput.focus();
    	return false;
    } else
    	errorStr = validateDateStrWithValidDate(newPostedDate, oldPostedDate);

    if (errorStr != null) {
        alert(errorStr);
        dateInput.focus();
        return false;
    }

    dateInput.value = dateStr;        // the cleaned/converted string
    return true;
}
function validateDateStrWithValidDate(dt, validdate) {

    if (dt < validdate) {
		return getString("js.billing.billlist.orderdateshouldnotlessthanposteddate");
	} else
		return null;
}

function loadLoyaltyOffersDialog() {
    var dialog = document.getElementById("loyaltyOffersDialog");
    dialog.style.display = 'block';
    loyaltyOffersDialog = new YAHOO.widget.Dialog("loyaltyOffersDialog", {
        width : "630px",
        visible : false,
        modal : true,
        constraintoviewport : true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
            { fn:handleLoyaltyOfferCancel,
              scope:loyaltyOffersDialog,
              correctScope:true } );
    loyaltyOffersDialog.cfg.queueProperty("keylisteners",[escKeyListener]);
    loyaltyOffersDialog.cancelEvent.subscribe(handleLoyaltyOfferCancel);
    loyaltyOffersDialog.render();
}

function signatureModal() {
	var dialog = document.getElementById("signatureDialog");
    dialog.style.display = 'block';
    signatureDialog = new YAHOO.widget.Dialog("signatureDialog", {
        width : "630px",
        visible : false,
        modal : true,
        constraintoviewport : true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
            { fn:handleSignatureCancel,
              scope:signatureDialog,
              correctScope:true } );
			  signatureDialog.cfg.queueProperty("keylisteners",[escKeyListener]);
			  signatureDialog.cancelEvent.subscribe(handleSignatureCancel);
			  signatureDialog.render();
}

function handleLoyaltyOfferCancel() {
	loyaltyOffersDialog.hide();
}
function handleSignatureCancel() {
	signatureDialog.hide();
}

function showLoyaltyOffersPopup(url,loyaltyHeader){
	document.getElementById("headerLoyaltyOffers").innerHTML =loyaltyHeader;
    if (loyaltyOffersDialog != null) {
    	var loyaltyOfferLocation= document.getElementById("billStatus");
    	 $('#offerIframe').attr('src', url);
    	loyaltyOffersDialog.cfg.setProperty("context", [ loyaltyOfferLocation, "tl", "tl" ], false);
    	loyaltyOffersDialog.show();
    }
}
// This script should be run only after the canvas is created.
// Handling for high DPI screen.
function resizeCanvas() {
	var canvas = document.getElementById("billSignatureCanvas");
	var ctx = canvas.getContext('2d');
	var ratio =  Math.max(window.devicePixelRatio || 1, 1);
	canvas.width = canvas.offsetWidth * ratio;
	canvas.height = canvas.offsetHeight * ratio;
	ctx.scale(ratio, ratio);

		//On every resized reload the data
	getBase64Image(newSignature ? newSignature: signature,function(data){
		if(data && data.startsWith("data:image")) {
			signaturePad.fromDataURL(data);
			document.getElementById("startCaptureBtnCanvas").disabled = true;
		}
	});
	//signaturePad.clear(); // otherwise isEmpty() might return incorrect value
}
function signaturePopup(header) {
	newSignature = "";
	triggerResize();
	signatureModal();
	resizeCanvas();
	document.getElementById("headerSignature").innerHTML =header;
    if (signatureDialog != null) {
    	var signatureDialogLocation= document.getElementById("billRemarks");
		 signatureDialog.cfg.setProperty("context", [ signatureDialogLocation, "tl", "tl" ], false);
		 signatureDialog.show();
	}
	canvasSignatureSetup();
}

function canvasSignatureSetup() {
	// Create a signature pad instance 
	signaturePad = new SignaturePad(document.getElementById("billSignatureCanvas"), {maxWidth: 2,
		dotSize: 2,
		minDistance: 2,
		drawOnly: true,
		backgroundColor: 'rgba(255, 255, 255)',
		penColor: 'rgb(0, 0, 0)',
		onBegin: () => {
			document.getElementById("captureModified").innerText="Signature Modified!"
		}, 
		onEnd: () => {
			newSignature = signaturePad.toDataURL('image/png');
		}
	});
	//Need this when you have multiple canvas signatures on a single screen
	window.addEventListener("resize", resizeCanvas);
	resizeCanvas();	
}

function ajaxCallForDepositValidation(){
	//var depositSetOffObj = document.mainform.depositSetOff
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/billing/BillAction.do?_method=depositsSetOffAjax&mr_no=" + mr+"&bill_number="+billNumber;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return callbackFunc(ajaxobj.responseText);
		}
	}

return true;

}
function callbackFunc(responseText) {
	eval("returnedData =" + responseText);
	if (returnedData != null) {
		var avlblIPDeposits = 0;
		var deposit_details = returnedData.depositDetails;
		var currentAvailableDeposits =roundAmt(deposit_details.total_deposits-deposit_details.total_set_offs+deposit_details.deposit_set_off);
		if(availableDeposits != currentAvailableDeposits){
			alert("Displayed Deposit Availability in this screen and Actual Deposit available with selected MrNo are not matching. " +
			"There are latest updates on deposit availability status. Please refresh your screen and verify the data before you continue your action..");
			document.mainform.depositSetOff.value = document.mainform.originalDepositSetOff.value;
			document.mainform.depositSetOff.focus();
			return false;
		}
		var ip_deposit_details = returnedData.ipDepositDetails;
		if(ip_deposit_details != null){
			avlblIPDeposits =roundAmt(ip_deposit_details.total_ip_deposits - ip_deposit_details.total_ip_set_offs + deposit_details.ip_deposit_set_off);
			if(ipDeposits != avlblIPDeposits){
				alert("Displayed Deposit Availability in this screen and Actual Deposit available with selected MrNo are not matching. " +
				"There are latest updates on deposit availability status. Please refresh your screen and verify the data before you continue your action.");
				document.mainform.depositSetOff.value = document.mainform.originalDepositSetOff.value;
				document.mainform.depositSetOff.focus();
				return false;
			}
		}
	}
	return true;
}


function validateBillLabelForBillLaterBills() {
	var newBillStatus = document.mainform.billStatus.value;
	if(bill_label_for_bill_later_bills == 'Y' && billType == 'C'){
		if (newBillStatus == 'C' || newBillStatus == 'F') {
			var selectedLblId = document.getElementById('billLabel').options[document.getElementById('billLabel').selectedIndex].value;
			if (selectedLblId == '-1') {
				showMessage("js.billing.billlist.billlabelforbilllaterbills");
				document.mainform.billLabelId.focus();
				return false;
			}
		}
	}
	return true;
}

function checkPayDateWithBillDate() {
	var payDates = getPayDates();
	var isValid = true;
	var billOpenDate = getBillOPenDateTime();
	for (var i = 0; i < payDates.length; i++) {
		if (payDates[i] < billOpenDate) {
			isValid = false;
			showMessage("js.billing.billlist.billOpenPayDate.validation");
			break;
		}
	}
	return isValid;
}

function handlePackageClick(chargeId) {
	updateIFrameParent.showPackageDetails(billCharges, chargeId);
}
var previousScrollPosition = window.pageYOffset;
window.onscroll = throttle(function() {
  var currentScrollPosition = window.pageYOffset;
  if (currentScrollPosition > 50) {
    document.getElementsByClassName('hidden-header-scroll-area')[0].style.display = 'block';
  } else {
    document.getElementsByClassName('hidden-header-scroll-area')[0].style.display = 'none';
  }
  if ((previousScrollPosition  > currentScrollPosition) && (currentScrollPosition > 50)) {
      updateIFrameParentHelper({ 'updateShowStickyHeader': { showStickyHeader: true, scrollPosition: currentScrollPosition }});
  } else {
      updateIFrameParentHelper({ 'updateShowStickyHeader': { showStickyHeader: false, scrollPosition: currentScrollPosition }});
  }
  previousScrollPosition  = currentScrollPosition;
}, 100);

function throttle(fn, threshhold, scope) {
  threshhold || (threshhold = 250);
  var last,
      deferTimer;
  return function () {
    var context = scope || this;

    var now = Date.now(),
        args = arguments;
    if (last && now < last + threshhold) {
      // hold on to it
      clearTimeout(deferTimer);
      deferTimer = setTimeout(function () {
        last = now;
        fn.apply(context, args);
      }, threshhold);
    } else {
      last = now;
      fn.apply(context, args);
    }
  };
}

function captureSignature(btnElement) {
	$.ajax ({
		url: "//127.0.0.1:9876/devices/signaturepads/capture",
		"headers": { 
			"x-insta-nexus-token": nexusToken,
			"x-insta-nexus-user": userid,
		},
		beforeSend: function() {
			signaturePad.clear();
			document.getElementById("captureModified").innerText="Check Signature Pad!";
			btnElement.disabled = true;
		},
		error: function() {
			document.getElementById("captureModified").innerText="";
			window.alert('Please Make sure the nexus app is running and is configured for singnature pad support.');
			btnElement.disabled = false;
		},
		success: function(data) {
			if(data.status === 'captured'){
				document.getElementById('fieldImgSrc').value=data.image.split(',')[1]; 
				document.getElementById('fieldImgText').value= data.image;
				newSignature = data.image;
				document.getElementById("captureModified").innerText="Signature captured. Click Save.";
				btnElement.disabled = false;
				
				signaturePad.fromDataURL(data.image);
				document.getElementById("startCaptureBtnCanvas").disabled = false;
				return true;
			} else if(data.status === 'capturing_cancelled') {
				btnElement.disabled = false;
			} else {
				window.alert('Please Make sure the Signature pad is connected');
				btnElement.disabled = false;
			}
		}
	});
	return false;
}

function captureCanvasSignature(signaturePad) {
	const sign = signaturePad.toDataURL('image/png');
	document.getElementById('fieldImgSrc').value= sign.split(',')[1];;
	document.getElementById('fieldImgText').value= sign;
	document.getElementById("startCaptureBtnCanvas").disabled = true;
	document.getElementById("captureModified").innerText = "";
	document.getElementById("saveButton").click();
	return false;
}

function clearCanvasSignature(signaturePad) {
	signaturePad.clear();
	newSignature = "";
	document.getElementById("billSignatureExternal").src="";
	document.getElementById("startCaptureBtnCanvas").disabled = false;
	document.getElementById("startCaptureBtnExternal").disabled = false;
	return false;
}

function getBase64Image(url, callback) {
		var xhr = new XMLHttpRequest();
		xhr.onload = function() {
			var reader = new FileReader();
			reader.onloadend = function() {
				callback(reader.result);
			}
			reader.readAsDataURL(xhr.response);
		};
		xhr.open('GET', url);
		xhr.responseType = 'blob';
		xhr.send();
	  //return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
	}

//Have to trigger resize on laod so that the canvas signatures are resized according to the screen dpi.
function triggerResize() {
	var evt = window.document.createEvent('UIEvents'); 
	evt.initUIEvent('resize', true, false, window, 0); 
	window.dispatchEvent(evt);
}

function validateBillFinalizationAndPaymentStatus(billFinalize) {
	if (document.mainform.paymentStatus.value == 'U' && !billFinalize) {
		return true;
	}

	var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund');

	var depositSetOff = 0;
	if (document.mainform.originalDepositSetOff) {
		depositSetOff = getPaise(document.mainform.originalDepositSetOff.value);
	}

	var rewardPointsAmount = 0;
	if (document.mainform.rewardPointsRedeemedAmount) {
		rewardPointsAmount = getPaise(document.mainform.rewardPointsRedeemedAmount.value);	
	}

	var patientAmt = totAmtPaise + totTaxPaise - depositSetOff - rewardPointsAmount - totInsAmtPaise - totInsTaxPaise;
	var existingReceiptAmt = getPaise(existingReceipts);
	
	var patientCreditNoteAmt = 0;
	if (document.mainform.patientCreditNoteAmt) {
		patientCreditNoteAmt = getPaise(document.mainform.patientCreditNoteAmt.value);
	}
	
	if (patientAmt != (existingReceiptAmt + payingAmt - patientCreditNoteAmt)) {
		if (billFinalize) {
			var msg1=getString("js.billing.billlist.bill.finalization.notallowed");
			alert(msg1);
		} else {
			var msg1=getString("js.billing.billlist.bill.payment.staus.paid.notallowed");
			alert(msg1);
			var paymentStatusObj = document.mainform.paymentStatus;
			setSelectedIndex(paymentStatusObj, 'U');
		}
		return false;
	} 
	return true;
}

function couponRedemption(){
	var url =cpath+"/billing/BillAction.do?_method=getCouponRedemptionWidgetUrl&bill_number="+billNumber+"&mobile_number="+patientPhone;

	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {

			var json = JSON.parse(reqObject.responseText);			
			if(json.widgetUrl == null){
				alert(json.errorMsg);
				return false;
			}
			var url=json.widgetUrl;
			couponRedemptionPopup(url);
			document.getElementById("couponRedemptionIframe").src= url;
			couponRedemptionDialog.show();
			return;
			
		}
	}
	return;
}

function couponRedemptionIframeDialog() {
	var dialog = document.getElementById("couponRedemptionDialog");
    dialog.style.display = 'block';
    couponRedemptionDialog = new YAHOO.widget.Dialog("couponRedemptionDialog", {
		width : "850px",
		height : "650px",
        visible : false,
        modal : true,
        constraintoviewport : true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
            { fn:handleCouponRedemptionCancel,
              scope:couponRedemptionDialog,
              correctScope:true } );
			  couponRedemptionDialog.render();
			  couponRedemptionDialog.cfg.queueProperty("keylisteners",[escKeyListener]);
			  couponRedemptionDialog.cancelEvent.subscribe(handleCouponRedemptionCancel);
			  couponRedemptionDialog.render();
}

function handleCouponRedemptionCancel() {
	couponRedemptionDialog.hide();
}

function couponRedemptionPopup(url) {
	couponRedemptionIframeDialog();
	document.getElementById("headerCouponRedemption").innerHTML ="Coupon Redemption";
	document.getElementById("couponRedemptionIframe").innerHTML =url;
    if (couponRedemptionDialog != null) {
        couponRedemptionDialog.show();
	}
}
