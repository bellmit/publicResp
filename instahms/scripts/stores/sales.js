/*
 * Javascript functions for use with MedicineSales.jsp
 */

var gOnChangeBillType = false;
var gTaxMap = '';
var salesType;			//Global Declration
var gPatientType = '';
var returnType = '';
var itemAutoComp;
var oAutoComp1;
var oAutoComp2;
var oAutoComp3;
var batchAutoComp;
var gPatientInfo=null;
var patientPrescription=false;
var erxDetails;

var gDefaultDiscountPer = 0;
var gDefaultDiscountType = 'E';
var gMedicineBatches = {};			// stores a medicine_id based hash of available stock
var gMedicineNameIds = {};			// stores medicine name => id for ROAOB
var gMedicineAjaxInProgress = false;
var gStoreCounter = "";
var gStoreCounterName = "";
var storeAccountGroup = "";
var gRetailSponsors = null;
var gRowUnderEdit = null;
var gBilledItems = new Array();
var gIsInsuranceBill = false;
var theForm;
var gStoreSaleUnit = '';
var itemNamesArray = '';
var gDoRoundOff = true;
var gStoreRatePlanId  = 0;
var qtyEditable = true;
var medicineAddedToGrid = false;
var returnIndentItemsJSON;
var oldMedicineId = "";
var serItemUserQty = 0;
var itemQtyMap = {};
var gIsOnload = true;
var primaryInsurancePhotoDialog;
var secondaryInsurancePhotoDialog;
var gItemTaxGroups = {};
var gItemTaxSubGroups = {};
var govtId_pattern = '';
var gVisitCreditLimitAmt = 0;
var gVisitPatientDue = 0;
var gcreditLimitDetailsJSON;

var saleTaxURL = cpath + '/sales/gettaxdetails.json';
var saleReturnTaxURL = cpath + '/pages/stores/MedicineSales.do?method=getSaleItemTaxdetails';
var insuranceHasDP = false;
var insuranceDP = null;
var billDP = null;
var billDPstatus = false;

function getStatus() {
	var status = '';
	if (document.salesform.ps_status) {
		var els = document.salesform.ps_status;
		if (els.checked && !els.disabled)
			status = els.value;
	}
	status = (status == '' ? 'all' : status);
	return status;
}
var psAc = null;

function reInitializeAc() {
	if (psAc != null) {
		psAc.destroy();
	}
	var searchType = 'visit';
	var visitType = 'visitType';
	var status = '';
	if (blockIp == 'I') {
		searchType = 'blockInactiveIp';
	}
	if (blockIp == 'O') {
		searchType = 'blockInactiveOp';
	}
	if (blockIp == 'B') {
		searchType = 'blockBoth';
	}
	psAc = Insta.initVisitAcSearch(cpath, 'searchVisitId', 'psContainer', getStatus(),'all', onSelectPatient, null, null, searchType);
}

var inited = false;
function init() {
	if (inited)
		return;
	inited = true;

	var i=0;
	SLNO_COL = i++; ITEM_COL = i++; ITEM_CODE_COL = i++; CONTROL_TYPE_COL = i++; BATCH_COL = i++; EXPIRY_COL = i++; MRP_COL = i++;

	CP_COL = -1;

	PKG_SIZE_COL = i++; UNIT_RATE_COL = i++; 
	
	var pbmPrescId = document.salesform.pbm_presc_id.value;
	
	if (!empty(pbmPrescId) && pbmPrescId != 0) {
		ISSUE_QTY_COL = i++;
	}
	
	USER_QTY_COL = i++; UOM_COL = i++; DISCOUNT_COL = i++;
	TOTAL_COL = i++;
	TAX_COL = i++;

	PAT_AMT_COL = i++;
	PAT_TAX_AMT_COL = i++;
	PRIM_CLAIM_AMT_COL = i++;
	PRIM_CLAIM_TAX_AMT_COL = i++;
	PRIM_PRE_AUTH_NO_COL = i++;

	if (!empty(pbmPrescId) && pbmPrescId != 0) {
		PBM_APPRD_AMT_COL = i++;
		PBM_STATUS_COL = i++;
	}

	SEC_CLAIM_AMT_COL = i++;
	SEC_CLAIM_TAX_AMT_COL = i++;
	SEC_PRE_AUTH_NO_COL = i++;
	ERX_REF_NO_COL = i++;
	theForm = document.salesform;

	if (document.getElementById('deposit') != null)
		document.getElementById('deposit').style.display="none";

	if (document.getElementById('ipdeposit') != null)
		document.getElementById('ipdeposit').style.display="none";

	reInitializeAc();

	if (gIsReturn) {
		returnType = 'ROAOB';
		setReturnType();
	}

	setSalesType();
	setStore();
	setBillOptions();
	setIndentStore();

	onChangeBillDiscountType();

	initDoctorAutoComplete();
	initCreditDoctorAutoComplete();
	initPatientDoctorAutoComplete();
	initSearchDialog();
	initItemSearchDialog();
	initGenericInfoDialog();
	initItemEditDialog();
	checkstoreallocation();

	if (prescvisit_id != '') {
		document.getElementById('salesType_hospital').checked = true;
		setSalesType();
		if (patstatus == 'I')
			theForm.ps_status.checked = false;
		theForm.searchVisitId.value = prescvisit_id;
		theForm.pbm_presc_id.value = patientPbmPrescId;
		psAc._bItemSelected = true;
		onSelectPatient();
	}

	// Set store medicines itemsarray when pbm presc.
	var pbmPrescId = theForm.pbm_presc_id.value;
	if (!empty(pbmPrescId) && pbmPrescId != 0)
		onChangeStore();

	// on pressing F5, existing form values (even hidden ones, eg, in template row) are being
	// set with the value before the refresh was pressed. Need to clear it, or it get saved.
	theForm.medicineId.value = "";
	//disable item level and bill level discounts if non-admin user does not have the right
	if ( (gRoleId != 1) && (gRoleId != 2) && (gAllowDiscounts == 'N')){
		document.getElementById("itemDiscPer").disabled="true";
		theForm.discType.disabled="true";
		document.getElementById("disPer").readonly="";
		document.getElementById("disAmt").readonly="";
	}

	initMedicineAutoComplete();
	initFrequencyAutoComplete();
	initInstructionAutoComplete();
	initFrequencyAutoCompleteEditBox();
	initEditInstructionAutoComplete();
	initLoginDialog();
	loadCenterPrefs();
	initPatientInsuranceDetailsDialog();
	initPatientInsuranceSecDetailsDialog();
	if(!gIsReturn) {
		getSubgroups();
		taxGroupInit("add_tax_groups");
		taxGroupInit("edit_tax_groups");
	}
	setHelpText();
}

function loadCenterPrefs() {
	var storeId = document.salesform.phStore;
	if(!empty(storeId.value)) {
		var list = findInList(jStores,"dept_id",storeId.value);
		var centerId = null != list ? list.center_id : 0;
		var list1 = findInList(allCenterPrefsJson,"center_id",centerId);
		if(list1 != null) {
			if(!empty(list1.pref_rate_plan_for_non_insured_bill)) {
				ratePlanForNonInsuredBill = list1.pref_rate_plan_for_non_insured_bill;
			} else {
				list1 = findInList(allCenterPrefsJson,"center_id",0);
				ratePlanForNonInsuredBill = list1.pref_rate_plan_for_non_insured_bill
			}
		}
	}
}

function resetForm() {
	clearPatientDetails();
	clearCustomerDetails();
	clearRetailCreditDetails();
	deleteRows();
	setSalesType();
	setStore();
	setBillOptions();
	clearPrescriptionDetails();
	clearBillPaymentDetails();
	clearRemarks();
	clearRetailDetails();
}
function clearRemarks() {
	var userRemObj = document.getElementsByName("allUserRemarks");
	var payRemObj = document.getElementsByName("paymentRemarks");
	if (userRemObj.length > 0) {
		for (var i=0; i < userRemObj.length; i++) {
			if(!empty(userRemObj[i])) userRemObj[i].value = "";
			if(!empty(payRemObj[i])) payRemObj[i].value = "";
		}
	}
}

function clearPrescriptionDetails() {
	var table = document.getElementById('prescInfo');
	var numItems = table.rows.length;
	for (var i=1; i<numItems-1; i++) {
		table.deleteRow(1);
	}
	document.getElementById('prescDetailsDiv').style.display = 'none';
	document.getElementById('prescFieldSet').style.display="none";
}

function initMedicineAutoComplete() {
   	if (itemAutoComp != undefined) {
		itemAutoComp.destroy();
		itemAutoComp = undefined;
	}

	var dataSource;
	if (gIsReturn && returnType == 'ROAOB') {
		itemNamesArray = gBilledItems;
		dataSource = new YAHOO.widget.DS_JSArray(itemNamesArray);
		 	dataSource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "cust_item_code_barcode_with_name"},{key : "medicine_name"},{key : "issue_units"},{key : "package_uom"} ]
	 	};
	} else {
		if (jMedicineNames == null) {
			// not yet ready
			return;
		}
		itemNamesArray = jMedicineNames;
		dataSource = new YAHOO.widget.DS_JSArray(itemNamesArray);
		 	dataSource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "cust_item_code_barcode_with_name"},{key : "medicine_name"},{key : "item_code"},{key : "issue_units"},{key : "package_uom"} ],
			numMatchFields: 3
	 	};//we use this fields args to get medicine name,make sure onSelectMedicine() function works fine with any order or key name changes in this.
	}
	itemAutoComp = new YAHOO.widget.AutoComplete('medicine', 'medicine_dropdown', dataSource);
	itemAutoComp.maxResultsDisplayed = 25;
	itemAutoComp.allowBrowserAutocomplete = false;
	itemAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	itemAutoComp.typeAhead = false;
	itemAutoComp.useShadow = false;
	itemAutoComp.minQueryLength = 1;
	itemAutoComp.forceSelection = true;
	itemAutoComp.animVert = false;
	itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	//itemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
			var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
			if (!empty(oResultData[2]))
				highlightedValue += " ["+oResultData[2]+"]";
			return highlightedValue ;
		}

	itemAutoComp.itemSelectEvent.subscribe(onSelectMedicine);
}

function getItemBarCodeDetails (val) {
	if (val == '') {
		resetMedicineDetails();
		document.salesform.medicine.value = '';
		return;
	}
	var flag = false;
	for (var m=0;m<itemNamesArray.length;m++) {
	     var item = itemNamesArray[m];
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, itemAutoComp);
	     	document.salesform.medicine.value = itmName;
	     	itemAutoComp._bItemSelected = true;
	    	onSelectMedicine();
//			itemAutoComp._selectItem(elNewItem);
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	resetMedicineDetails();
	 	document.salesform.medicine.value = '';
	 }
	setSalesDefaultQty();
}

function setSalesDefaultQty(){
	if(theForm.addQty.value == '' && prefBarCode == 'Y')
		theForm.addQty.value = 1;
}


/**
*  DoctorName AC
*
*/
function initDoctorAutoComplete() {

   	if (oAutoComp1 != undefined) {
		oAutoComp1.destroy();
	}

	dataSource = new YAHOO.util.LocalDataSource(jRetailDocNames)
	dataSource.queryMatchContainsWordBegining = true;
	dataSource.responseSchema = {fields : ["DOCTOR_NAME"]};

	oAutoComp1 = new YAHOO.widget.AutoComplete('custDoctorName', 'doctor_dropdown', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
	oAutoComp1.formatResult = Insta.autoHighlightWordBeginnings;
}

/**
*  CreditDoctorName AC
*
*/
function initCreditDoctorAutoComplete() {

   	if (oAutoComp2 != undefined) {
		oAutoComp2.destroy();
	}
	dataSource = new YAHOO.util.LocalDataSource(jRetailDocNames);
	dataSource.responseSchema = {fields : ["DOCTOR_NAME"]};

	oAutoComp2 = new YAHOO.widget.AutoComplete('custRetailCreditDocName', 'creditdoctor_dropdown', dataSource);
	dataSource.queryMatchContainsWordBegining = true;
	oAutoComp2.maxResultsDisplayed = 5;
	oAutoComp2.allowBrowserAutocomplete = false;
	oAutoComp2.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp2.typeAhead = false;
	oAutoComp2.useShadow = false;
	oAutoComp2.minQueryLength = 0;
	oAutoComp2.animVert = false;
	oAutoComp2.formatResult = Insta.autoHighlight;
}

/**
*  patientDoctorName AC
*
*/
function initPatientDoctorAutoComplete() {

   	if (oAutoComp3 != undefined) {
		oAutoComp3.destroy();
	}

    dataSource = new YAHOO.util.LocalDataSource(jPrescribedDocNames)
    dataSource.responseSchema = {fields: ["DOCTOR_NAME"]};

	oAutoComp3 = new YAHOO.widget.AutoComplete('patientDoctor', 'patientdoctor_dropdown', dataSource);
	dataSource.queryMatchContains = true;
	oAutoComp3.maxResultsDisplayed = 5;
	oAutoComp3.allowBrowserAutocomplete = false;
	oAutoComp3.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp3.typeAhead = false;
	oAutoComp3.useShadow = false;
	oAutoComp3.minQueryLength = 0;
	oAutoComp3.forceSelection = true;
	oAutoComp3.animVert = false;
	oAutoComp3.formatResult = Insta.autoHighlight;
}

var rcAuto = null;
function initRetailCreditAutoComplete() {

	if (rcAuto != null) {
		rcAuto.destroy();
		rcAuto = null;
	}
	var ds = new YAHOO.util.XHRDataSource(cpath + '/pages/stores/MedicineSalesAjax.do');
	ds.scriptQueryAppend = "method=getActiveRetailCustomersJSON&storeId="+document.getElementById('phStore').value;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "customer_name"},
					{key : "bill_no"},
					{key : "phone_no"},
					{key : "credit_limit"},
					{key : "sponsor_name"},
					{key : "customer_id"},
					{key : "bill_amount"},
					{key : "refund"}
				 ]
	};

	rcAuto = new YAHOO.widget.AutoComplete('custRetailCreditName', 'custRetailName_dropdown', ds);

	rcAuto.maxResultsDisplayed = 18;
	rcAuto.allowBrowserAutocomplete = false;
	rcAuto.prehighlightClassName = "yui-ac-prehighlight";
	rcAuto.typeAhead = false;
	rcAuto.useShadow = false;
	rcAuto.minQueryLength = 2;
	rcAuto.animVert = false;
	rcAuto.resultTypeList = false;

	rcAuto.itemSelectEvent.subscribe(onSelectRetailCredit);
	rcAuto.unmatchedItemSelectEvent.subscribe(onSelectRetailCredit);
}

function initRetailSponsorAutoComplete() {
	var localDs = new YAHOO.util.LocalDataSource(gRetailSponsors);

	localDs.responseSchema = {fields : ["sponsor_name"]};
	rcsAuto = new YAHOO.widget.AutoComplete('custRetailSponsor', 'custRetailSponsor_dropdown', localDs);

	rcsAuto.maxResultsDisplayed = 18;
	rcsAuto.allowBrowserAutocomplete = false;
	rcsAuto.prehighlightClassName = "yui-ac-prehighlight";
	rcsAuto.typeAhead = false;
	rcsAuto.useShadow = false;
	rcsAuto.minQueryLength = 0;
	rcsAuto.animVert = false;
}

function loadRetailSponsors() {
	// Get the list of available "Active" retail credit customers using an ajax call and
	var url="MedicineSalesAjax.do?method=getRetailSponsorsJSON";
	Ajax.get(url, function(data, status) {
			gRetailSponsors = eval("(" + data + ")");
			initRetailSponsorAutoComplete();
			});
}

function resetRetailCustomerDetails() {
	theForm.creditBillNo.value="";
	theForm.custRetailCreditDocName.value="";
	theForm.custRetailCreditDocName.readOnly=false;
	theForm.custRCreditPhoneNoField.value="";
	theForm.custRCreditPhoneNoField.readOnly=false;
	theForm.custRCreditLimit.value="";
	theForm.custRCreditLimit.readOnly=false;

	document.getElementById('retailCreditPaymentDiv').style.display="none";	// will get enabled for existing cust only

	theForm.existingCustomer.value='false';
}

var retailCredillBillAmt = 0;
var retailCreditLimit = 0;
var gretailCustomerName = null;
var gretailCustomerObj = null;
function onSelectRetailCredit(oSelf, oArgs) {

	var rCustomerName = theForm.custRetailCreditName.value;
	var rcObj = oArgs[2];

	if (gretailCustomerName == rCustomerName && gretailCustomerObj != null) {
		rcObj = gretailCustomerObj;
	}

	if ((rCustomerName != "") && (rcObj != null)) {
		gretailCustomerName = rCustomerName;
		gretailCustomerObj = rcObj;
		// existing customer
		theForm.creditBillNo.value = rcObj.bill_no;
		theForm.custRCreditPhoneNoField.value = rcObj.phone_no;
		theForm.custRCreditLimit.value = rcObj.credit_limit;
		theForm.custRetailSponsor.value = rcObj.sponsor_name;

		theForm.custRCreditLimit.readOnly = true;
		theForm.custRetailSponsor.readOnly = true;

		theForm.existingCustomer.value = 'true';
		document.getElementById('retailCreditPaymentDiv').style.display = "inline";

		// todo: clean this up, it is a global variable
		customerId=rcObj.customer_id;
		theForm.retailCustomerId.value = customerId;
		discount=null;		// todo: should not be required
		retailCredillBillAmt = rcObj.bill_amount-rcObj.refund;
		retailCreditLimit = rcObj.credit_limit;

	} else {
		gretailCustomerName = null;
		gretailCustomerObj = null;
		theForm.custRetailCreditDocName.readOnly = false;
		theForm.custRCreditPhoneNoField.readOnly = false;
		theForm.custRCreditLimit.readOnly = false;
		theForm.custRetailSponsor.readOnly = false;

		theForm.existingCustomer.value = 'false';
		document.getElementById('retailCreditPaymentDiv').style.display="none";
		resetRetailCustomerDetails();
		retailCredillBillAmt = 0;
		retailCreditLimit = 0;
	}
}

function refreshItems(){
	deleteRows();
}

function setRetailPatientDetails(patient) {

	var retail = patient.retailDetails;
	var doctor = patient.doctorName;
	if (retail.is_credit == 'Y') {
		theForm.custRetailCreditName.value = retail.customer_name;
		theForm.custRetailCreditDocName.value = doctor;
		theForm.custRetailSponsor.value = retail.sponsor_name;
		theForm.custRCreditPhoneNoField.value = retail.phone_no;
		theForm.custRCreditLimit.value = retail.credit_limit;
		theForm.creditBillNo.value = patient.retailBillNo;

		theForm.custRetailCreditName.readOnly = true;
		theForm.custRetailCreditDocName.readOnly = true;
		theForm.custRetailSponsor.readOnly = true;
		theForm.custRCreditPhoneNoField.readOnly = true;
		theForm.custRCreditLimit.readOnly = true;
		if (gIsReturn) {
			$("#retail_credit_patient_phone_help").hide();
		}

	} else {
		theForm.custName.value = retail.customer_name;
		theForm.custDoctorName.value = doctor;
		theForm.retailPatientMobileNoField.value = retail.phone_no;
		if(regPref.nationality) {
			theForm.nationalityId.value = retail.nationality_id;
			theForm.rNationalityId.value = retail.nationality_id;
			theForm.nationalityId.disabled = true;
		}
		if (regPref.government_identifier_type_label) {
			theForm.identifierId.value = retail.identifier_id;
			theForm.rIdentifierId.value = retail.identifier_id;
			theForm.identifierId.disabled = true;
		}
		if (regPref.government_identifier_label) {
			theForm.governmentIdentifier.value = retail.government_identifier;
			theForm.governmentIdentifier.readOnly = true;
		}

		theForm.custName.readOnly = true;
		theForm.custDoctorName.readOnly = true;
		theForm.retailPatientMobileNoField.readOnly = true;
		if (gIsReturn) {
			$("#retail_patient_phone_help").hide();
		}
	}
}
var sponsor_type;
var sec_sponsor_type;
var policyId;
var sec_policyId;
var patientVisitId;
/*
 * Show the patient details in the patient section for the given patient object
 */
function setPatientDetails(patient) {

	patientDetailsPlanDetails = gPatientInfo.patient_details_plan_details;
	var mName = patient.middle_name == null ? '' : patient.middle_name;
	var lName = patient.last_name == null ? '' : patient.last_name;

	var pd_viewPhotoIcon = document.getElementById('pd_viewPhotoIcon');
	pd_viewPhotoIcon.style.display = "block";
	patient_photo_available = patient.patient_photo_available;

	 var patient_gender = patient.patient_gender;

	if(patient_gender == "M" && patient_photo_available == "Y")
		pd_viewPhotoIcon.src = cpath+'/images/man-icon.png';
	if(patient_gender == "M" && patient_photo_available == "N")
		pd_viewPhotoIcon.src = cpath+'/images/man-icon1.png';
	if(patient_gender == "F" && patient_photo_available == "Y")
		pd_viewPhotoIcon.src = cpath+'/images/woman-icon.png';
	if(patient_gender == "F" && patient_photo_available == "N")
		pd_viewPhotoIcon.src = cpath+'/images/woman-icon1.png';
	if(patient_gender == "O" && patient_photo_available == "Y")
		pd_viewPhotoIcon.src = cpath+'/images/genericuser-icon.png';
	if(patient_gender == "O" && patient_photo_available == "N")
		pd_viewPhotoIcon.src = cpath+'/images/genericuser-icon1.png';
	if(patient_gender == "C" && patient_photo_available == "Y")
		pd_viewPhotoIcon.src = cpath+'/images/genericuser-icon.png';
	if(patient_gender == "C" && patient_photo_available == "N")
		pd_viewPhotoIcon.src = cpath+'/images/genericuser-icon1.png';


	var pri_cardIcon = document.getElementById('pri_cardIcon');
	var sec_cardIcon = document.getElementById('sec_cardIcon');


	setNodeText('patientMrno', patient.mr_no, null, patient.mr_no);
	setNodeText('patientName', patient.patient_name + ' ' + mName + ' ' + lName, null,
		patient.patient_name + ' ' + mName + ' ' + lName);
	var patientAgeSex = patient.age_text +
		(patient.dateofbirth == null ? '' : " (" + formatDate(new Date(patient.dateofbirth)) + ")")
		+ ' / ' + patient.patient_gender;
	setNodeText('patientAgeSex', patientAgeSex, null, patientAgeSex);

	var patVisitNo = patient.patient_id;
	patientVisitId = patient.patient_id;
	patVisitNo = patVisitNo + (patient.use_drg == 'Y' ? ' (DRG)' : '');
	patVisitNo = patVisitNo + (patient.use_perdiem == 'Y' ? ' (Perdiem)' : '');
	setNodeText('patientVisitNo', patVisitNo, null, patVisitNo);
	setNodeText('patientDept', patient.dept_name, null, patient.dept_name);
	gVisitCreditLimitAmt = (patient.ip_credit_limit_amount != null && patient.ip_credit_limit_amount != '') ? patient.ip_credit_limit_amount : 0;
	gVisitPatientDue = parseFloat(gPatientInfo.visitTotalPatientDue);
	gcreditLimitDetailsJSON = gPatientInfo.creditLimitDetailsJSON;

	var referredBy = patient.refdoctorname == null ? "" : patient.refdoctorname;
	setNodeText('referredBy', referredBy, null, referredBy);
	var admitDate = formatDate(new Date(patient.reg_date)) + " " + formatTime(new Date(patient.reg_time));
	setNodeText('admitDate', admitDate, null, admitDate);

	theForm.mrno.value = patient.mr_no;
	theForm.visitId.value = patient.patient_id;
    theForm.visitType.value = patient.visit_type;
    theForm.visitStatus.value = patient.visit_status;
    document.getElementById("planId").value = patient.plan_id;
    document.getElementById("isTpa").value = patient.primary_sponsor_id == null ? 'N' : 'Y';

    if (!visitWithPlan()){
    	document.getElementById("planId").value = '0';
    }
    if ( !empty(gPatientInfo.indentsList) && gPatientInfo.indentsList.length > 0 ){
		document.getElementById("patientDoctor").value = gPatientInfo.indentsList[gPatientInfo.indentsList.length - 1].prescribing_doctor_name != null ? gPatientInfo.indentsList[gPatientInfo.indentsList.length - 1].prescribing_doctor_name : patient.doctor_name;
    }else{
    	document.getElementById("patientDoctor").value = patient.doctor_name != null ? patient.doctor_name : referredBy;
    }
    var tpaName = patient.tpa_name == null ? '' : patient.tpa_name;
    var orgId = patient.org_id == null ? '' : patient.org_id;
    var orgName = patient.org_name == null ? '' : patient.org_name;
    if (tpaName != '' || (orgId != '' && orgName != 'GENERAL'))  {
    	if(patient.sponsor_type)	{
    		document.getElementById("patientDetailsTPARow").style.display = 'table-row';
    		document.getElementById('tpaDetails').style.display = corporateInsurance == 'Y' ? 'none' : 'table-cell';
    		document.getElementById('tpaName').parentNode.style.display = corporateInsurance == 'Y' ? 'none' : 'block';
    	} else {
    		document.getElementById("patientDetailsTPARow").style.display = 'none';
    	}
    	if(patient.sec_sponsor_type)	{
    		document.getElementById("patientDetailsSecTPARow").style.display = 'table-row';
    	} else {
    		 document.getElementById("patientDetailsSecTPARow").style.display = 'none';
    	}
		document.getElementById('patientDetailsInsCoLbl').style.display = corporateInsurance == 'N' ? 'block' : 'none';
    	document.getElementById('patientDetailsCorpCoLbl').style.display = corporateInsurance == 'Y' ? 'block' : 'none';

	} else {

		if(patient.sec_sponsor_type) {
			document.getElementById("patientDetailsSecTPARow").style.display = 'table-row';
		} else {
			document.getElementById("patientDetailsSecTPARow").style.display = 'none';
		}
    	document.getElementById("patientDetailsTPARow").style.display = 'none';
    }

	var planIndex = 0;
    var insurance_category = patient.insurance_category == null ? 0 : patient.insurance_category;
    var planId = !visitWithPlan() ? 0 : patientDetailsPlanDetails[0].plan_id;
    var secplanId = patient.sec_sponsor_type != 'I' || planId == 0 ? 0 :
    	( (visitWithPlan() && hasMoreThanOnePlan()) ? patientDetailsPlanDetails[1].plan_id
    	:( visitWithPlan() ? patientDetailsPlanDetails[0].plan_id : 0));
    if (insurance_category != 0 || planId != 0) {
		if(patient.sponsor_type)	{
    		document.getElementById("patientDetailsTPAExtRow").style.display = 'table-row';
    	} else {
    		if ( secplanId != 0 ) {
	    		document.getElementById("secPatientDetailsTPAExtRow").style.display = 'table-row';
	    		document.getElementById("patientDetailsTPAExtRow").style.display = 'none';
    		}else{
    			document.getElementById("secPatientDetailsTPAExtRow").style.display = 'none';
    		}
    	}
    } else {
    	document.getElementById("patientDetailsTPAExtRow").style.display = 'none';
		document.getElementById("secPatientDetailsTPAExtRow").style.display = 'none';
    }

   if(patient.sponsor_type)	{
	   sponsor_type = patient.sponsor_type;
   		if( visitWithPlan() ){
   			planIndex = patientDetailsPlanDetails.length;
   			setNodeText('patientInsuranceCo', patientDetailsPlanDetails[0].insurance_co_name == null ? "" : patientDetailsPlanDetails[0].insurance_co_name, null,
		  		patientDetailsPlanDetails[0].insurance_co_name == null ? "" : patientDetailsPlanDetails[0].insurance_co_name);
		  	setNodeText('tpaName', patientDetailsPlanDetails[0].tpa_name == null ? "" : patientDetailsPlanDetails[0].tpa_name, null,
		  		patientDetailsPlanDetails[0].tpa_name == null ? "" : patientDetailsPlanDetails[0].tpa_name);
		  	setNodeText('planType', patientDetailsPlanDetails[0].plan_type_name == null ? "" : patientDetailsPlanDetails[0].plan_type_name, null,
		  		patientDetailsPlanDetails[0].plan_type_name == null ? "" : patientDetailsPlanDetails[0].plan_type_name);
		  	setNodeText('planname', patientDetailsPlanDetails[0].plan_name == null ? "" : patientDetailsPlanDetails[0].plan_name, 20,
		  		patientDetailsPlanDetails[0].plan_name == null ? "" : patientDetailsPlanDetails[0].plan_name);
		  	setNodeText('policyId', patientDetailsPlanDetails[0].member_id == null ? "" : patientDetailsPlanDetails[0].member_id, 22,
		  		patientDetailsPlanDetails[0].member_id == null ? "" : patientDetailsPlanDetails[0].member_id);
		  	if(patientDetailsPlanDetails[0].member_id != null)
		  	pri_cardIcon.src = cpath+'/images/CardIcon.png';
		  	//added for insurance expire vaidation.
		  	if(insurExpireAllow == "Y") {
		  		var expireDate = patientDetailsPlanDetails[0].policy_validity_end == null ? "" : patientDetailsPlanDetails[0].policy_validity_end;
		  		var currentDate = new Date();
				if(expireDate!="" && expireDate!=null) {
					var daysDiffs = 0;
					var expireDateFormat = new Date(parseInt(expireDate));
					daysDiffs = parseInt(Math.round(daysDiff(new Date(currentDate.getFullYear(),currentDate.getMonth(),currentDate.getDate()),new Date(expireDateFormat.getFullYear(),expireDateFormat.getMonth(),expireDateFormat.getDate()))));
					if(daysDiffs < 0) {
						daysDiffs = 0;
					} else if(daysDiffs == 0 || daysDiffs > 0){
						daysDiffs = daysDiffs + 1;
					}
					document.getElementById("policyExpireDays").value = daysDiffs;
				}
		  	}
		  	//setting Discount_plan_id
			if(patientDetailsPlanDetails[0].discount_plan_id != null) {
				insuranceHasDP = true;
				insuranceDP = patientDetailsPlanDetails[0].discount_plan_id;
			}
			//End

   		} else {
		    setNodeText('patientInsuranceCo', patient.insurance_co_name == null ? "" : patient.insurance_co_name, null,
		  		patient.insurance_co_name == null ? "" : patient.insurance_co_name);
		  	setNodeText('tpaName', patient.tpa_name == null ? "" : patient.tpa_name, null,
		  		patient.tpa_name == null ? "" : patient.tpa_name);
		  	setNodeText('planType', patient.plan_type_name == null ? "" : patient.plan_type_name, null,
		  		patient.plan_type_name == null ? "" : patient.plan_type_name);
		  	setNodeText('planname', patient.plan_name == null ? "" : patient.plan_name, 20,
		  		patient.plan_name == null ? "" : patient.plan_name);
		  	setNodeText('policyId', patient.member_id == null ? "" : patient.member_id, 22,
		  		patient.member_id == null ? "" : patient.member_id);
		  	if(patient.member_id != null)
		  	pri_cardIcon.src = cpath+'/images/CardIcon.png';
		  	//added for insurance expire vaidation.
			if(insurExpireAllow == "Y") {
			  	var expireDate = patientDetailsPlanDetails[0].policy_validity_end == null ? "" : patientDetailsPlanDetails[0].policy_validity_end;
			  	var currentDate = new Date();
				if(expireDate!="" && expireDate!=null) {
					var daysDiffs = 0;
					var expireDateFormat = new Date(parseInt(expireDate));
					daysDiffs = parseInt(Math.round(daysDiff(new Date(currentDate.getFullYear(),currentDate.getMonth(),currentDate.getDate()),new Date(expireDateFormat.getFullYear(),expireDateFormat.getMonth(),expireDateFormat.getDate()))));
					if(daysDiffs < 0) {
						daysDiffs = 0;
					} else if(daysDiffs == 0 || daysDiffs > 0){
						daysDiffs = daysDiffs + 1;
					}
					document.getElementById("policyExpireDays").value = daysDiffs;
				}
			}
			//End
	  	}
  		setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null,
  			patient.org_name == null ? "" : patient.org_name);

  		document.getElementById('networkPlanTypeLblCell').style.display = corporateInsurance == 'N' ? 'table-cell' : 'none';
  		document.getElementById('networkPlanTypeValueCell').style.display = corporateInsurance == 'N' ? 'table-cell' : 'none';
   }

   planIndex = ( planIndex == 0 ? (patientDetailsPlanDetails.length >= 1 ? patientDetailsPlanDetails.length : 0) : ( patientDetailsPlanDetails.length > 1 ? patientDetailsPlanDetails.length : 0));

   if(patient.sec_sponsor_type)	{
	   sec_sponsor_type = patient.sponsor_type;
   		if( visitWithPlan() && planIndex > 0 ) {
   			 setNodeText('secPatientInsuranceCo', patientDetailsPlanDetails[planIndex-1].insurance_co_name == null ? "" : patientDetailsPlanDetails[planIndex-1].insurance_co_name, null,
		  		patientDetailsPlanDetails[planIndex-1].insurance_co_name == null ? "" : patientDetailsPlanDetails[planIndex-1].insurance_co_name);
		  	setNodeText('secTpaName', patientDetailsPlanDetails[planIndex-1].tpa_name == null ? "" : patientDetailsPlanDetails[planIndex-1].tpa_name, null,
		  		patientDetailsPlanDetails[planIndex-1].tpa_name == null ? "" : patientDetailsPlanDetails[planIndex-1].tpa_name);
		  	setNodeText('secPlanType', patientDetailsPlanDetails[planIndex-1].plan_type_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_type_name, null,
		  		patientDetailsPlanDetails[planIndex-1].plan_type_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_type_name);
		  	setNodeText('secPlanname', patientDetailsPlanDetails[planIndex-1].plan_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_name, 20,
		  		patientDetailsPlanDetails[planIndex-1].plan_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_name);
		  	setNodeText('secPolicyId', patientDetailsPlanDetails[planIndex-1].member_id == null ? "" : patientDetailsPlanDetails[planIndex-1].member_id, 22,
		  		patientDetailsPlanDetails[planIndex-1].member_id == null ? "" : patientDetailsPlanDetails[planIndex-1].member_id);
		  	if(patientDetailsPlanDetails[planIndex-1].member_id != null)
		  	sec_cardIcon.src = cpath+'/images/CardIcon.png';
   		} else {
		    setNodeText('secPatientInsuranceCo', patient.sec_insurance_co_name == null ? "" : patient.sec_insurance_co_name, null,
		  		patient.sec_insurance_co_name == null ? "" : patient.sec_insurance_co_name);
		  	setNodeText('secTpaName', patient.sec_tpa_name == null ? "" : patient.sec_tpa_name, null,
		  		patient.sec_tpa_name == null ? "" : patient.sec_tpa_name);
		  	setNodeText('secPlanType', patient.plan_type_name == null ? "" : patient.plan_type_name, null,
		  		patient.plan_type_name == null ? "" : patient.plan_type_name);
		  	setNodeText('secPlanname', patient.plan_name == null ? "" : patient.plan_name, 20,
		  		patient.plan_name == null ? "" : patient.plan_name);
		  	setNodeText('secPolicyId', patient.member_id == null ? "" : patient.member_id, 22,
		  		patient.member_id == null ? "" : patient.member_id);
		  	if(patient.member_id != null)
		  	sec_cardIcon.src = cpath+'/images/CardIcon.png';
  		}
   }

   if( visitWithPlan() && planIndex > 0 ){
   		document.getElementById("secPatientDetailsTPAExtRow").style.display = 'table-row';
   } else {
   		document.getElementById("secPatientDetailsTPAExtRow").style.display = 'none';
   }


	if (patient.visit_type == 'i') {
		var bed_type = patient.alloc_bed_type == null
			||patient.alloc_bed_type == '' ?patient.bill_bed_type:patient.alloc_bed_type+'/'+patient.alloc_bed_name;
		setNodeText('patientBedType', bed_type, null, bed_type);
	} else setNodeText('patientBedType', '', null, '');
	
	onChangeBillDiscountType();
	gDefaultDiscountPer = patient.pharmacy_discount_percentage;
	gDefaultDiscountType = patient.pharmacy_discount_type;
	

	if (document.getElementById('addNewPBMPrescDiv') != null) {
		document.getElementById('addNewPBMPrescDiv').style.display = "none";
		if (patient.visit_type == 'o' && !empty(planId) && planId != 0) {
			var pbmHref = document.getElementById('addNewPBMPrescDiv').getElementsByTagName("a");
			if (pbmHref && pbmHref[0])
				pbmHref[0].href = pbmHref[0].href + "&patient_id="+patient.patient_id;
			document.getElementById('addNewPBMPrescDiv').style.display = "block";
		}
	}

	if (!empty(gPatientInfo.pbmPrescDetails)) {
		setPbmPrescriptionDetails();
	}

	var plan_exclusions = document.getElementById('plan_exclusions');
	var plan_notes = document.getElementById('plan_notes');
	var sec_plan_exclusions = document.getElementById('sec_plan_exclusions');
	var sec_plan_notes = document.getElementById('sec_plan_notes');
	if(patient.sponsor_type){
		if(patient.primary_sponsor_id != '' && planIndex >= 0){
			plan_exclusions.innerText = patient.plan_exclusions == null ? "" : patient.plan_exclusions ;
			plan_notes.innerText = patient.plan_notes == null ? "" : patient.plan_notes ;
		}
	}

	if(patient.sec_sponsor_type){
		if(patient.secondary_sponsor_id != '' && planIndex >= 0){
			sec_plan_exclusions.innerText = patientDetailsPlanDetails[planIndex-1].plan_exclusions == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_exclusions;
			sec_plan_notes.innerText = patientDetailsPlanDetails[planIndex-1].plan_notes == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_notes;
		}
	}
	var pinsuranceImage = document.getElementById('pinsuranceImage');
	pinsuranceImage.src ='';
	var sinsuranceImage = document.getElementById('sinsuranceImage');
	sinsuranceImage.src ='';
}

/**
 * Sets the PBM prescription details in the JSP.
 *
 */
function setPbmPrescriptionDetails () {

	var pbmdet = gPatientInfo.pbmPrescDetails;
	var erxRefernceNumber = pbmdet.erx_reference_no;
	var prescStatus = empty(pbmdet.pbm_presc_status) ? "" : pbmdet.pbm_presc_status;
	var prescStatusTxt = "";
	if (prescStatus == 'O')
		prescStatusTxt = 'Open';
	else if (prescStatus == 'S')
		prescStatusTxt = 'Sent';
	else if (prescStatus == 'D')
		prescStatusTxt = 'Denied';
	else if (prescStatus == 'R')
		prescStatusTxt = 'ForResub';
	else if (prescStatus == 'C')
		prescStatusTxt = 'Closed';

	if (document.getElementById('pbmStatuslbl') != null)
		document.getElementById('pbmStatuslbl').textContent = prescStatusTxt;

	var prescApprStatus = empty(pbmdet.approval_status) ? "" : pbmdet.approval_status;
	var prescApprStatusTxt = prescStatus == 'S' ? "Pending..." : "";
	if (prescApprStatus == 'F')
		prescApprStatusTxt = 'Fully Approved';
	else if (prescApprStatus == 'P')
		prescApprStatusTxt = 'Partially Approved';
	else if (prescApprStatus == 'R')
		prescApprStatusTxt = 'Fully Rejected';

	if (document.getElementById('pbmApprovalStatuslbl') != null)
		document.getElementById('pbmApprovalStatuslbl').textContent = prescApprStatusTxt;

	if (document.getElementById('erxReferenceNo') != null)
		document.getElementById('erxReferenceNo').textContent = erxRefernceNumber;

}

/**
 * Add medicines from indent.
 *
 * @param patientIndents
 */
function addMedicinesFromIndents(patientIndents){
	var msg = "";
	var itemsAdded = 0;
	for (var p=0; p<patientIndents.length; p++) {
		var indent = patientIndents[p];
		var medicineId = indent.medicine_id;
		var medBatches = gMedicineBatches[medicineId];
		if(gIsInsuranceBill) {
			var catPayableInfo = getCatPayableStatus(medicineId, false);
			if(catPayableInfo != null && catPayableInfo != undefined) {
				indent.calFlag = (catPayableInfo.plan_category_payable!= null && catPayableInfo.plan_category_payable!= undefined)? catPayableInfo.plan_category_payable:"";
				indent.priCatPayable = (catPayableInfo.pri_cat_payable!= null && catPayableInfo.pri_cat_payable!= undefined)? catPayableInfo.pri_cat_payable:"";
			}
		}
		if (medBatches == null || medBatches.length == 0) {
			msg += msg.endsWith("\n") ? "" : "\n";
			msg += getString("js.sales.issues.warning.nostockavailable")+
				indent.indent_medicine_name + "': " + indent.qty;
			continue;
		}

		var selBatch = medBatches[0];
		var billable = selBatch.billable;
		var retailable = selBatch.retailable;
		if (billable && retailable) {
			msg += addMedicines(medicineId, indent.qty, indent.uom, false,
					null, indent, false);
			msg += msg.endsWith("\n") ? "" : "\n";
			itemsAdded++;
		}
	}
	if (msg != "" && msg != '\n')
		alert(msg);
	
	var pbmPrescId = theForm.pbm_presc_id.value;
	var isPbmPresc = !empty(pbmPrescId) && pbmPrescId != 0;

	if(itemsAdded > 0  && gIsInsuranceBill && !isPbmPresc)
		onClickProcessIns('salesform');
}

/*
 *	Add medicines of OP patinet's Medicine Prescription tp the item list of sales
 */

function addMedicinesFromPrescription(patientPrescriptions){
	var msg = "";
	for (var p=0; p<patientPrescriptions.length; p++) {
		var prescription = patientPrescriptions[p];
		var medicineId = prescription.medicineId;
		var medicineName = prescription.medicineName;
		var medBatches = gMedicineBatches[medicineId];
		if(gIsInsuranceBill) {
			var catPayableInfo = getCatPayableStatus(medicineId, false);
			if(catPayableInfo != null && catPayableInfo != undefined) {
				prescription.priCatPayable = (catPayableInfo.pri_cat_payable!= null && catPayableInfo.pri_cat_payable!= undefined)? catPayableInfo.pri_cat_payable:"";
				prescription.calFlag = (catPayableInfo.plan_category_payable!= null && catPayableInfo.plan_category_payable!= undefined)?catPayableInfo.plan_category_payable:"";
			}
		}
		if (medBatches == null || medBatches.length == 0) {
			msg += msg.endsWith("\n") ? "" : "\n";
			msg += getString("js.sales.issues.warning.nostockavailable");
			msg+=prescription.medicine_name ;
			msg+= "': ";
		    msg+= prescription.qty;
		    alert(msg);
			continue;
		}

		var billable = medBatches[0].billable;
		var retailable = medBatches[0].retailable
		patientPrescription = true;
		if (billable && retailable) {
			setMedicineUOMOptions(theForm.sale_unit,medicineName);
			msg += addMedicines(medicineId, prescription["qty"], theForm.sale_unit.value, (gStoreSaleUnit != 'I'),
					prescription, false);
			msg += msg.endsWith("\n") ? "" : "\n";
		}
		clearItemSearchFields();

	}
	//added for insurance expire vaidation..
	var policyExpireDays = document.getElementById("policyExpireDays").value;
	if(insurExpireAllow == "Y" && policyExpireDays!="" && gIsInsuranceBill) {
		var msg1 = "";
		var status = true;
		var oldMedicineName = "";
		var allowedArray = document.getElementsByName("insuranceExpired");
		for (var i=0;i<allowedArray.length;i++) {
			var statusOfInsurance = eval(allowedArray[i].value);
			if(statusOfInsurance) {
				if(oldMedicineName != document.getElementsByName("medName")[i].value) {
					oldMedicineName = document.getElementsByName("medName")[i].value;
					msg1 += document.getElementsByName("medName")[i].value+", ";
					status = false;
				}
			}
		}
		if(!status) {
			msg1 = msg1.trim();
			msg1 = msg1.substring(0, msg1.length-1);
			alert(msg1+"\nThe above medicines have sale quantity which exceeds the insurance validity.");
		}

	}
	//End
	if (msg != "" && msg != '\n')
		alert(msg)
}

function clearPatientDetails() {
	setNodeText('patientMrno', '', null, '');
	var pd_viewPhotoIcon = document.getElementById('pd_viewPhotoIcon');
	pd_viewPhotoIcon.style.display = "none";
	setNodeText('patientName', '', null, '');
	setNodeText('patientAgeSex', '', null, '');
	setNodeText('referredBy', '', null, '');
	setNodeText('admitDate', '', null, '');
	setNodeText('patientVisitNo', '', null, '');
	setNodeText('patientDept', '', null, '');
	setNodeText('patientDoctor', '', null, '');
	setNodeText('patientBedType', '', null, '');
	setNodeText('patientInsuranceCo', '', null, '');
	setNodeText('tpaName', '', null, '');
	setNodeText('ratePlan', '', null, '');
	setNodeText('planType', '', null, '');
	setNodeText('planname', '', null, '');
	setNodeText('policyId', '', null, '');

	document.getElementById("patientDetailsTPARow").style.display = 'none';
	document.getElementById("patientDetailsTPAExtRow").style.display = 'none';
	document.getElementById("priCorporateRow").style.display = 'none';
	document.getElementById("priNationalRow").style.display = 'none';
	document.getElementById("secCorporateRow").style.display = 'none';
	document.getElementById("secNationalRow").style.display = 'none';
	document.getElementById("patientDetailsSecTPARow").style.display = 'none';
    document.getElementById("secPatientDetailsTPAExtRow").style.display = 'none';
	theForm.mrno.value = '';
	theForm.visitId.value = '';
	theForm.visitType.value = '';
	theForm.visitStatus.value = '';
	theForm.searchVisitId.value = '';
	document.getElementById("planId").value = '0';
	gPatientInfo = null;
	if(!gIsReturn && !gIsEstimate)
		setSelectedIndex(theForm.discountAuthName,'');

	if (document.getElementById('addNewPBMPrescDiv') != null)
		document.getElementById('addNewPBMPrescDiv').style.display = "none";
}

function clearRetailCreditDetails() {
	theForm.custRetailCreditName.value="";
	theForm.custRetailCreditDocName.value="";
	theForm.custRetailSponsor.value="";
	theForm.custRetailSponsor.readOnly=false;
	theForm.custRetailCreditDocName.readOnly=false;
	theForm.custRCreditPhoneNoField.value="";
	theForm.custRCreditPhoneNoField.readOnly=false;
	theForm.custRCreditLimit.value="";
	theForm.custRCreditLimit.readOnly=false;
	theForm.custName.value="";
	theForm.custDoctorName.value="";
}

function clearRetailDetails() {
	theForm.custName.value="";
	theForm.custDoctorName.value="";
	if( theForm.retailPatientMobileNoField ){
		theForm.retailPatientMobileNoField.value="";
	}
	if(regPref.nationality) {
		theForm.nationalityId.value="";
	}
	if (regPref.government_identifier_type_label) {
		theForm.identifierId.value="";
	}
	if (regPref.government_identifier_label) {
		theForm.governmentIdentifier.value="";
	}

	theForm.retailPatientMobileNoField="";
}

function clearCustomerDetails() {
	clearRetailCreditDetails();
	clearPatientDetails();
	clearRetailDetails();

	clearPhoneErrors($('#retailPatientMobileNoField'), $('#retail_patient_phone_error'));
	clearPhoneErrors($('#custRCreditPhoneNoField'), $('#retail_credit_patient_phone_error'));
}

/*
 * Clear medicine details in the Add dialog
 */
function resetMedicineDetails() {
	document.getElementById('category').innerHTML = "";
	document.getElementById('manfName').innerHTML = "";
	document.getElementById('packageType').innerHTML = "";
	if (document.getElementById('medAvblQty') != null)
		document.getElementById('medAvblQty').innerHTML = "";
	document.getElementById('issueUnits').innerHTML = "";
	document.getElementById('barCodeId').innerHTML = "";
	theForm.sale_unit.options.length = 0;
	theForm.addMedicineId.value = "";
	theForm.addQty.value = "";
	theForm.prior_auth_id.value = "";
}

/*
 * Deleting all row items from the grid
 */
function deleteRows() {
	var itemDetailsTableObj = document.getElementById("medList");
	var numItems = getNumItems();
	var firstItemRow = getFirstItemRow();

	for (var i=0; i<numItems; i++) {
		itemDetailsTableObj.deleteRow(firstItemRow);
	}

	document.getElementById("lblBilledAmount").innerHTML=parseFloat(0).toFixed(decDigits);
	gItemBilledPaise = 0;
	document.getElementById("lblItemDiscounts").innerHTML=parseFloat(0).toFixed(decDigits);
	gItemDiscountTotalPaise = 0;
	document.getElementById("lblItemTotal").innerHTML=parseFloat(0).toFixed(decDigits);
	gItemTotalPaise = 0;
	document.getElementById("lblBillDiscount").innerHTML=parseFloat(0).toFixed(decDigits);
	gBillDiscountPaise = 0;
	document.getElementById("roundOffAmt").innerHTML=parseFloat(0).toFixed(decDigits);
	gRoundOffPaise = 0;
	document.getElementById("grandTotal").innerHTML=parseFloat(0).toFixed(decDigits);
	gGrandTotalPaise = 0;
	gItemTaxTotalPaise = 0;

	theForm.disPer.value="";
	theForm.disAmt.value="";
}

/*
 * Number of "item" rows in the table (excluding title and total rows)
 * If the table changes, we change only the following functions. The rest of
 * the script should use only these functions to get the list of medicines etc.
 */
function getNumItems() {
	var itemDetailsTableObj = document.getElementById("medList");
	return itemDetailsTableObj.rows.length - 2;	// one for header, one for template row
}

function getFirstItemRow() {
	return 1;
}

function initBatchAutoComplete(input, container, allBatches, selectHandler, autoSelect, unitsObj) {
	if (batchAutoComp != undefined) {
		batchAutoComp.destroy();
	}
	var sortedBatches = new Array();
	if (allBatches) {
		for (var i=0; i<allBatches.length; i++) {
			sortedBatches.push(allBatches[i]);
		}
		sortedBatches.sort(function(b1, b2) {
				return (b1.batch_no<b2.batch_no) ? -1 : (b1.batch_no>b2.batch_no) ? 1 : 0;
		});
	}

	var localDs = new YAHOO.util.LocalDataSource(sortedBatches);
	localDs.responseSchema = {fields : ["batch_no"]};
	batchAutoComp = new YAHOO.widget.AutoComplete(input, container, localDs);
    batchAutoComp.useShadow = true;
	batchAutoComp.animVert = false;
    batchAutoComp.minQueryLength = 0;
    batchAutoComp.forceSelection = true;
    batchAutoComp.formatResult = function(result, query) {
		var details;
		for (var i in sortedBatches) {
			if (result[0] == sortedBatches[i].batch_no) {
				var batchesQty = sortedBatches[i].qty;
				// display of quantity available for stock is based on store and not user.
				// otherwise, we need to keep changing the display as the user changes the choice.
				if (gStoreSaleUnit == 'P') {
					batchesQty = (batchesQty/sortedBatches[i].issue_base_unit).toFixed(2);
				}
				var bin = sortedBatches[i].bin;
				if ((bin == null) || (bin == ""))
					bin = "--";
				details = sortedBatches[i].batch_no + "/" + formatExpiry(sortedBatches[i].exp_dt) +
					"/" + bin + "/(<b>" + batchesQty + "</b>)";
			}
		}
		return details;
	}

	// if there is only one batch, select that.
	if (autoSelect && (sortedBatches.length == 1)) {
		document.getElementById(input).value = sortedBatches[0].batch_no;
		batchAutoComp._bItemSelected = true;
	}

	if (selectHandler)
		batchAutoComp.itemSelectEvent.subscribe(selectHandler);

	if (document.activeElement == document.getElementById(input)) {
		// we already have focus: intimate the autocomp that this is the case
		batchAutoComp._onTextboxFocus(null, batchAutoComp);
	}
	if (gIsReturn && returnType == 'ROAOB' && pIndentNo != '') {
		document.getElementById(input).disabled = true;
	}
}

/*
 * Add the medicine, batch, qty into the grid. All validations must be done prior
 * to calling this function.
 */
function addMedicineToGrid(medicineId, userQty, units,batch, prescription) {
	addMedicineToGrid(medicineId, userQty, units, batch, prescription);
}
function addMedicineToGrid(medicineId, userQty, units, batch, prescription, indents, processInsurance) {
	var indentItemId = '';
	var patientIndentNo = '';
	document.getElementById("PayAndPrint").disabled = true;
	showLoader();
	
	if ( indents != null ) {
		indentItemId = indents.indent_item_id;
		patientIndentNo = indents.patient_indent_no;
	}

	var itemListTable = document.getElementById("medList");
	var templateRow = document.getElementById("templateRow");
	var row = templateRow.cloneNode(true);

	row.style.display = '';
	row.id = 'medRow' + row.rowIndex;
	row.className = 'medRow';
	itemListTable.tBodies[0].insertBefore(row, templateRow);
	var slno = getNumItems();
	if(indents != null && gIsInsuranceBill) {
		setHiddenValue(row, "cat_payable" ,(indents["calFlag"]!=null && indents["calFlag"]!=undefined)?indents["calFlag"]:"");
		setHiddenValue(row, "insurance_cat_payable" ,(indents["calFlag"]!=null && indents["calFlag"]!=undefined)?indents["calFlag"]:"");
	}
	if(indents != null && gIsInsuranceBill && indents["priCatPayable"] != null && indents["priCatPayable"] != undefined) {
		setNodeText('priCategoryPayable', indents["priCatPayable"], null,indents["priCatPayable"]);
	}
	if ( indents != null && returnType == 'ROAOB' ) {//partial indent returns are not allowed,so dont give Trash Icon
		row.cells[15].innerHTML = "";
	}

	var consutationId = '';
	var medicationId = '';
	var pbmPrescId = '';
	var pbmPriorAuthId = '', pbmPriorAuthModeId = '', pbmStatus = '';
	var pbmPatientShare = 0, pbmPaymentAmount = 0;
	var pbmRate = 0, pbmDiscount = 0, pbmAmount = 0, pbmClaimNet = 0, pbmClaimNetApproved = 0;
	var pbmUserUnits = '', pbmUserUOM ='';
	var prsDuration='', prsFrequency ='' ,prsDoctorRemark ='';
	var prsDosage ='', prsOtherRemark ='', prsWarnLabel ='';
	var prsDosageUnit ='', prsRouteOfAdmin ='', prsDurationUnit ='';
	var prsToalQty ='', prsRouteName = '', prsTotalQty = '', prsSpecialInstruction = '';
	var prsMedicineSoldQty = '';
	var prsIsDoctorExcluded = '';

	var approvedQty = '';
	if (prescription != null) {
		consutationId = prescription["consutationId"];
		if(consutationId == undefined || consutationId == null || consutationId == '') {
			medicationId = prescription["medicationId"];
		}

		pbmPrescId = !empty(prescription["pbmPrescId"]) ? prescription["pbmPrescId"] : '';
		pbmPriorAuthId = !empty(prescription["pbmPriorAuthId"]) ? prescription["pbmPriorAuthId"] : '';
		pbmPriorAuthModeId = !empty(prescription["pbmPriorAuthModeId"]) ? prescription["pbmPriorAuthModeId"] : '';

		pbmStatus = !empty(prescription["pbmStatus"]) ? prescription["pbmStatus"] : '';

		pbmRate = !empty(prescription["rate"]) ? prescription["rate"] : 0;
		pbmDiscount = !empty(prescription["discount"]) ? prescription["discount"] : '';

		pbmUserUnits = !empty(prescription["user_unit"]) ? prescription["user_unit"] : '';
		pbmUserUOM = pbmUserUnits == 'P' ? prescription["package_uom"] : prescription["issue_units"];

		pbmAmount = !empty(prescription["amount"]) ? prescription["amount"] : 0;
		pbmClaimNet = !empty(prescription["claim_net_amount"]) ? prescription["claim_net_amount"] : 0;
		pbmClaimNetApproved = !empty(prescription["claim_net_approved_amount"]) ? prescription["claim_net_approved_amount"] : 0;

		pbmPatientShare = !empty(prescription["pbmPatientShare"]) ? prescription["pbmPatientShare"] : 0;
		pbmPaymentAmount = !empty(prescription["pbmPaymentAmount"]) ? prescription["pbmPaymentAmount"] : 0;


   		// sales prescription fields

		prsDuration = !empty(prescription["duration"]) ? prescription["duration"] : 0;
		prsDurationUnit = !empty(prescription["durationUnit"]) ? prescription["durationUnit"] : '';
		prsFrequency = !empty(prescription["frequency"]) ? prescription["frequency"] : 0;
		prsDosage = !empty(prescription["dosage"]) ? prescription["dosage"] : 0;
		prsMedicineSoldQty = !empty(prescription["total_issed_qty"]) ? prescription["total_issed_qty"] : 0;
		prsDoctorRemark = !empty(prescription["doctorRemarks"]) ? prescription["doctorRemarks"] : '';
		prsSpecialInstruction = !empty(prescription["special_instr"]) ? prescription["special_instr"] : '';
		prsOtherRemark = !empty(prescription["other_remarks"]) ? prescription["other_remarks"] : '';
		prsWarnLabel = !empty(prescription["warning_label"]) ? prescription["warning_label"] : '';
		prsRouteOfAdmin = !empty(prescription["route"])? prescription["route"] : '';
		prsDosageUnit = !empty(batch.consumption_uom) ? batch.consumption_uom : '';
        prsIsDoctorExcluded = !empty(prescription["item_excluded_from_doctor"])?
        prescription["item_excluded_from_doctor"] : '';
        if(prsIsDoctorExcluded == 'MI' || prsIsDoctorExcluded == 'N' || prsIsDoctorExcluded == 'false') {
            prsIsDoctorExcluded = false;
        }
        if(prsIsDoctorExcluded == 'ME' || prsIsDoctorExcluded == 'Y' || prsIsDoctorExcluded == 'true'
         || prsIsDoctorExcluded == 'NA') {
                    prsIsDoctorExcluded = true;
        }

        prsDoctorExclusionRemark = !empty(prescription["item_excluded_from_doctor_remarks"])?
        prescription["item_excluded_from_doctor_remarks"] : '';

		if(prsRouteOfAdmin == '') {
			prsRouteName = '';
		} else {
			prsRouteName = !empty(prescription["routeName"])? prescription["routeName"] : '';
		}
		prsTotalQty = !empty(prescription["totalQty"])? prescription["totalQty"] : 0;

		if(gIsInsuranceBill) {
			setHiddenValue(row, "cat_payable" ,(prescription["calFlag"]!=null&&prescription["calFlag"]!=undefined)?prescription["calFlag"]:"");
			setHiddenValue(row, "insurance_cat_payable" ,(prescription["calFlag"]!=null&&prescription["calFlag"]!=undefined)?prescription["calFlag"]:"");
			if(prescription["item_excluded_from_doctor"] == undefined || prescription["item_excluded_from_doctor"] == null) {
			    prescription["item_excluded_from_doctor"] = "";
			}
			if(prescription["item_excluded_from_doctor_remarks"] == undefined || prescription["item_excluded_from_doctor_remarks"] == null) {
			    prescription["item_excluded_from_doctor_remarks"] = ""
			}
			setHiddenValue(row, "item_excluded_from_doctor", prescription["item_excluded_from_doctor"]);
			setHiddenValue(row, "item_excluded_from_doctor_remarks", (prescription["item_excluded_from_doctor_remarks"]));
			if (modEclaimErx && prescription["erxReferenceNo"]) {
				// auto populate erx details
				setNodeText(row.cells[ERX_REF_NO_COL], prescription["erxActivityId"]);
				setHiddenValue(row, "erxActivityId", prescription["erxActivityId"]);
				var erxReferenceField = document.getElementById("erxReferenceNo");
				erxReferenceField.value = prescription["erxReferenceNo"];// erx reference for all activities is the same
			}
		}

		if(gIsInsuranceBill && prescription["priCatPayable"] != null && prescription["priCatPayable"] != undefined) {
			setNodeText('priCategoryPayable', prescription["priCatPayable"], null, prescription["priCatPayable"]);
		}

		if(!gIsReturn) {
			for(var key in gItemTaxGroups) {
				if (gItemTaxGroups.hasOwnProperty(key)) {
					if(gItemTaxGroups[key].item_group_id){
						var taxGroupId = gItemTaxGroups[key].item_group_id;
						if(prescription['taxsubgroupid'+taxGroupId]) {
							var taxSubGroupId = prescription['taxsubgroupid'+taxGroupId];
							setHiddenValue(row, 'taxsubgroupid'+taxGroupId , taxSubGroupId);
						}
					}
				}
			}
		}

	}
	var isPbmPresc = (!empty(pbmPrescId) && pbmPrescId != 0);

	/*
	 * Add the category based discount and the rate plan based discount to get total
	 * discount percentage applicable to this item: but this is not to be done for returns
	 * against original bill, where the discount already includes the rateplan discount
	 */

	var medDiscount, medDiscountType;
	if ( isPbmPresc && prescription != null ) {
		// For PBM Presc. discount type is vat/tax excluded
		// and discount per is from item category plus rate plan discount percentage.
		medDiscount = batch.meddisc;
		setHiddenValue(row, "approvedNetAmount" ,!empty(prescription["approvednet"]) ? prescription["approvednet"] : '');
		medDiscount += gDefaultDiscountPer;
		medDiscountType = 'E';
		approvedQty = !empty(prescription["qty"])? prescription["qty"] : 0;

	}else if (returnType == 'ROAOB') {
		// discount percentage, type, amount is as per original sale
		medDiscount = batch.discount_per;
		medDiscountType = batch.discount_type;

	} else {
		medDiscount = batch.meddisc;		// from category
		if (gPatientType == 'hospital') {
			// add rate plan discount percentage, also discount type is from rate plan, regardless of category
			medDiscount += gDefaultDiscountPer;
			medDiscountType = gDefaultDiscountType;
		} else {
			medDiscountType = 'E';
		}
	}

	var saleType = gIsReturn ? "return" : "sale";

	setHiddenValue(row, "medDiscWithoutInsurance", medDiscount);	// percentage
	setHiddenValue(row, "medDiscWithInsurance", medDiscount);	// percentage

	var insuranceCategoryId = batch.insurance_category_id;
	var itemCategoryId = batch.med_category_id;
	var priCatPayable = document.getElementById("priCategoryPayable").title;
	var discountPlanDetailsJSON = JSON.parse(discountPlansJSON);
	var discountCategory = document.salesform.discountCategory.value;

	if(billDP != null && gIsInsuranceBill) {
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {

			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];

				if ( item.applicable_type == 'N' &&  item.discount_plan_id == billDP && insuranceCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'C' &&  item.discount_plan_id == billDP && item.applicable_to_id == "PHCMED" ) {
					setHiddenValue(row, "medDiscWithInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'S' && item.discount_plan_id == billDP && itemCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithInsurance", item["discount_value"]);	// percentage
					break;
				}
			}
		}
	} else if(billDP != null && !gIsInsuranceBill) {
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {

			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];

				if ( item.applicable_type == 'N' &&  item.discount_plan_id == billDP && insuranceCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithoutInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'C' &&  item.discount_plan_id == billDP && item.applicable_to_id == "PHCMED" ) {
					setHiddenValue(row, "medDiscWithoutInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'S' && item.discount_plan_id == billDP && itemCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithoutInsurance", item["discount_value"]);	// percentage
					break;
				}
			}
		}
	} else if(gIsInsuranceBill &&priCatPayable != null && priCatPayable != undefined && insuranceHasDP
	    && ((priCatPayable == 'Y' && prsIsDoctorExcluded != true) || (priCatPayable == 'N' && prsIsDoctorExcluded == false))) {
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {

			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];

				if ( item.applicable_type == 'N' &&  item.discount_plan_id == insuranceDP && insuranceCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'C' &&  item.discount_plan_id == insuranceDP && item.applicable_to_id == "PHCMED" ) {
					setHiddenValue(row, "medDiscWithInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'S' && item.discount_plan_id == insuranceDP && itemCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithInsurance", item["discount_value"]);	// percentage
					break;
				}
			}
		}
	} else if(billDP == null && discountCategory != "") {
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {

			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];

				if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountCategory && insuranceCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithoutInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'C' &&  item.discount_plan_id == discountCategory && item.applicable_to_id == "PHCMED" ) {
					setHiddenValue(row, "medDiscWithoutInsurance", item["discount_value"]);	// percentage
					break;
				} else if (item.applicable_type == 'S' && item.discount_plan_id == discountCategory && itemCategoryId == item.applicable_to_id ) {
					setHiddenValue(row, "medDiscWithoutInsurance", item["discount_value"]);	// percentage
					break;
				}
			}
		}
	}

	if(gIsInsuranceBill && saleType == 'sale') {
		medDiscount = 	getElementByName(row, 'medDiscWithInsurance').value;
	}

	if (batch.issue_base_unit == 0) batch.issue_base_unit = 1;		// sanity, since we use this to divide.

	/*
	 * set all hidden values and node texts
	 */
	// batch related properties
	var pkgCp = batch.package_cp;

	var isSaleInIssueUnits = (units == '');
	var allowedQty;
	if(itemQtyMap[batch.medicine_id] != undefined) {
		serItemUserQty = itemQtyMap[batch.medicine_id]+ userQty;
	} else {
		serItemUserQty = userQty;
	}
	/*if(oldMedicineId != batch.medicine_id) {
		oldMedicineId = batch.medicine_id;
		if(itemQtyMap[batch.medicine_id] != undefined) {
			if(!empty(batch.qtyForBatch)) {
				serItemUserQty = itemQtyMap[batch.medicine_id]+ batch.qtyForBatch;
			} else {
				serItemUserQty = itemQtyMap[batch.medicine_id]+ userQty;
			}
		} else {
			serItemUserQty = batch.qtyForBatch;
		}
	} else if(!empty(batch.qtyForBatch)){
		if(itemQtyMap[batch.medicine_id] != undefined) {
			serItemUserQty = itemQtyMap[batch.medicine_id] + batch.qtyForBatch;
		} else {
			serItemUserQty = serItemUserQty + batch.qtyForBatch;
		}

	} else {
		if(itemQtyMap[batch.medicine_id] != undefined) {
			serItemUserQty = itemQtyMap[batch.medicine_id] + userQty;
		} else {
			serItemUserQty = serItemUserQty + userQty;
		}

	}*/

	//added for insurance expire vaidation..
	var policyExpireDays = document.getElementById("policyExpireDays").value;

	if(insurExpireAllow == "Y" && policyExpireDays != "" && gIsInsuranceBill) {
		var freq = 1;
		if(prescription !=  null && (!empty(prescription["freqCount"]) || prescription["freqCount"]!=undefined) ) {
			freq = parseInt(prescription["freqCount"]);
		} else if(prsFrequency!= null || prsFrequency!= undefined || prsFrequency!= ""){
			freq = parseInt(getPerDayFreq(prsFrequency));
		}

		if(prescription != null && (!empty(prescription["allowedQuantity"]) || prescription["allowedQuantity"]!=undefined) ) {
			allowedQty = prescription["allowedQuantity"];
			//allowedQty = (allowedQty - prsMedicineSoldQty);
			setHiddenValue(row, "insuranceExpired" , false);
			/*if(serItemUserQty > 0) {
				if(parseInt(serItemUserQty) > parseInt(allowedQty)) {
					setHiddenValue(row, "insuranceExpired" , true);
				} else {
					setHiddenValue(row, "insuranceExpired" , false);
				}
			}*/


		} else {
			allowedQty = Math.round(parseInt(policyExpireDays)*freq);
			allowedQty = (allowedQty - prsMedicineSoldQty);
			if(serItemUserQty > 0 ) {
				if(parseInt(serItemUserQty) > allowedQty) {
					setHiddenValue(row, "insuranceExpired" , true);
				} else {
					setHiddenValue(row, "insuranceExpired" , false);
				}
			}
			itemQtyMap[batch.medicine_id+"_aq"] = allowedQty;
			itemQtyMap[batch.medicine_id] = serItemUserQty;
		}

		setHiddenValue(row, "freqCount" ,freq);
		setHiddenValue(row, "allowedQty" , allowedQty);

	}
	//End
	if(serItemUserQty > 0 && gIsInsuranceBill) {
		//patientPrescription = true;
		setBatchProperties(row, batch, batch.selling_price, pkgCp, isSaleInIssueUnits, allowedQty, serItemUserQty );

	}else {
		setBatchProperties(row, batch, batch.selling_price, pkgCp, isSaleInIssueUnits, allowedQty, userQty );
	}

	// only hidden values
	setHiddenValue(row, "temp_charge_id", "_"+slno);
	if(consutationId == undefined || consutationId == null || consutationId == '') {
		setHiddenValue(row, "medicationId", medicationId);
	} else {
		setHiddenValue(row, "consultId", consutationId);
	}

	setHiddenValue(row, "medName", batch.medicine_name);
	setHiddenValue(row, 'itemCode', batch.item_code);
	setHiddenValue(row, 'medicineId', batch.medicine_id);
	setHiddenValue(row, "insuranceCategoryId", batch.insurance_category_id);
	setHiddenValue(row, "billingGroupId", batch.billing_group_id);
	setHiddenValue(row, "itemCategoryId", batch.med_category_id);
	setHiddenValue(row, "medPBMPrescId", pbmPrescId);
	setHiddenValue(row, "frequency" ,prsFrequency);
	setHiddenValue(row, "duration" ,prsDuration);
	setHiddenValue(row, "durationUnit" ,prsDurationUnit);
	setHiddenValue(row, "dosage" ,prsDosage);
	setHiddenValue(row, "dosageUnit" ,prsDosageUnit);
	setHiddenValue(row, "doctorRemarks" ,prsDoctorRemark);
	setHiddenValue(row, "special_instr" ,prsSpecialInstruction);
	setHiddenValue(row, "salesRemarks" ,prsOtherRemark);
	setHiddenValue(row, "warningLabel" ,prsWarnLabel);
	setHiddenValue(row,	"routeOfAdmin" ,prsRouteOfAdmin);
	setHiddenValue(row, "routeName" ,prsRouteName);
	setHiddenValue(row, "totalQty" ,prsTotalQty);
	setHiddenValue(row, "total_issed_qty" ,prsMedicineSoldQty);
	setHiddenValue(row, "issueUnits", isPbmPresc ? (pbmUserUnits == 'P' ? pbmUserUOM : '') : units);

	if (isPbmPresc) {

		var pbmStatusTxt = "";
		if (pbmStatus == 'O')
			pbmStatusTxt = 'Open';
		else if (pbmStatus == 'C')
			pbmStatusTxt = 'Closed';
		else if (pbmStatus == 'D')
			pbmStatusTxt = 'Denied';

		if (pbmStatus == 'C')
			row.cells[PBM_STATUS_COL].innerHTML = "<font color='green'>"+pbmStatusTxt+"</font>";
		else if (pbmStatus == 'D')
			row.cells[PBM_STATUS_COL].innerHTML = "<font color='red'>"+pbmStatusTxt+"</font>";
		else if (pbmStatus == 'O')
			setNodeText(row.cells[PBM_STATUS_COL], pbmStatusTxt);

		setHiddenValue(row, "approvedQty" , approvedQty);
		setHiddenValue(row, "medPBMStatus", pbmStatus);

		setNodeText(row.cells[PRIM_PRE_AUTH_NO_COL], pbmPriorAuthId);
		setHiddenValue(row, "primpreAuthId", pbmPriorAuthId);
		setHiddenValue(row, "primpreAuthModeId", pbmPriorAuthModeId);

		setNodeText(row.cells[UNIT_RATE_COL], parseFloat(pbmRate).toFixed(decDigits));
		setHiddenValue(row, "pbmitemrate", pbmRate);

		setNodeText(row.cells[UOM_COL], pbmUserUOM);

		setHiddenValue(row, 'medDiscRS', pbmDiscount);
		setNodeText(row.cells[DISCOUNT_COL], pbmDiscount);

		var pbmpatientamt = 0;
		var pbmclaimamt = 0;


		if (pbmStatus == 'D') {
			pbmpatientamt = formatAmountPaise(getPaise(pbmAmount));
			// TODO: is this approved or 0 for denied?
			//pbmclaimamt = formatAmountPaise(getPaise(pbmClaimNetApproved));
			pbmclaimamt = 0;

		}else if (pbmStatus == 'C') {
			// TODO: is this patient share or amount-claim?
			pbmpatientamt = formatAmountPaise(getPaise(pbmPatientShare));
			//pbmpatientamt = formatAmountPaise(getPaise(pbmAmount) - getPaise(pbmClaimNet)) ;
			pbmclaimamt = formatAmountPaise(getPaise(pbmClaimNetApproved));

		}else if (pbmStatus == 'O') {
			pbmpatientamt = formatAmountPaise(getPaise(pbmAmount) - getPaise(pbmClaimNet)) ;
			pbmclaimamt = formatAmountPaise(getPaise(pbmClaimNet));
		}

		setNodeText(row.cells[PAT_AMT_COL], pbmpatientamt);
		setHiddenValue(row, 'patCalcAmt', pbmpatientamt);

		setNodeText(row.cells[PRIM_CLAIM_AMT_COL], formatAmountPaise(getPaise(pbmPaymentAmount)));
		setHiddenValue(row, 'claimAmt', formatAmountPaise(getPaise(pbmPaymentAmount)));
		setHiddenValue(row, 'primclaimAmt', empty(pbmPaymentAmount) ? 0 : formatAmountPaise(getPaise(pbmPaymentAmount)));

		setNodeText(row.cells[PBM_APPRD_AMT_COL], formatAmountPaise(getPaise(pbmPaymentAmount)));
		setHiddenValue(row, 'pbmPaymentAmt', empty(pbmPaymentAmount) ? 0 : pbmPaymentAmount);
		
		setNodeText(row.cells[ISSUE_QTY_COL], Math.round(batch.issue_base_unit * userQty));
	}else {
		setNodeText(row.cells[PRIM_PRE_AUTH_NO_COL], getElementByName(document.getElementById("prim_preAuthRow"),'prior_auth_id').value);
		setHiddenValue(row, "primpreAuthId", getElementByName(document.getElementById("prim_preAuthRow"),'prior_auth_id').value);
		setHiddenValue(row, "primpreAuthModeId", getElementByName(document.getElementById("prim_preAuthRow"),'prior_auth_mode_id').value);

		if (document.getElementById("erx_activity_id") != null && document.getElementById("erx_activity_id") != ""
			&& getElementByName(document.getElementById("erxReferenceRow"),'erx_activity_id').value != "") {
			setNodeText(row.cells[ERX_REF_NO_COL], getElementByName(document.getElementById("erxReferenceRow"),'erx_activity_id').value)
			setHiddenValue(row, "erxActivityId", getElementByName(document.getElementById("erxReferenceRow"),'erx_activity_id').value);
		}

		if ( hasMoreThanOnePlan() ) {
			setNodeText(row.cells[SEC_PRE_AUTH_NO_COL], getElementByName(document.getElementById("sec_preAuthRow"),'sec_prior_auth_id').value);
			getElementByName(row, 'secpreAuthId').value = getElementByName(document.getElementById("sec_preAuthRow"),'sec_prior_auth_id').value;
			getElementByName(row, 'secpreAuthModeId').value = getElementByName(document.getElementById("sec_preAuthRow"),'prior_auth_mode_id').value;
		}
	}

	// medicine name
	var medTitle = batch.medicine_name + "/" +batch.category + "/" +
		batch.manf_mnemonic + "/" + batch.package_type;
	setNodeText(row.cells[SLNO_COL], slno);
	setNodeText(row.cells[ITEM_COL], batch.medicine_name, 25, medTitle);
	setNodeText(row.cells[ITEM_CODE_COL], batch.item_code);

	/* if (storeItemControlType != 'Normal')
		row.cells[CONTROL_TYPE_COL].innerHTML = "<font color='blue'>"+storeItemControlType+"</font>";
	else
		setNodeText(row.cells[CONTROL_TYPE_COL], storeItemControlType, 10);
	setHiddenValue(row, "controlTypeName", batch.control_type_name); */

	setNodeText(row.cells[USER_QTY_COL], userQty);
	setHiddenValue(row, "userQty", userQty);
	
	setHiddenValue(row, "medDisc", medDiscount);	// percentage
	setHiddenValue(row, "medDiscType", medDiscountType);	// Excl or Incl VAT (E or I)

	//Added for store tariff expresssion support.
	setHiddenValue(row, "visit_selling_price", batch.visit_selling_expr);
	setHiddenValue(row, "store_selling_price", batch.store_selling_expr);
	setHiddenValue(row, "selling_price_hid", batch.selling_price!=null ? batch.selling_price : 0);
	var sellingPrice = getSellingPrice(batch.medicine_id, batch.item_batch_id,
			batch.visit_selling_expr,
					batch.store_selling_expr,
							batch.selling_price!=null ? batch.selling_price : 0, userQty);
	setHiddenValue(row, "pkgmrp" , sellingPrice);
	setHiddenValue(row, "origRate" , sellingPrice);
	setNodeText(row.cells[MRP_COL], parseFloat(sellingPrice).toFixed(decDigits));
	//end.

	/*
	 * Calculated values: unit rate, discount, total, tax amount: set in recalcRowAmounts
	 */
	//set patient amounts as hidden fields
	setHiddenValue(row, "insuranceCategoryId", batch.insurance_category_id);

	// trigger off amount calculations
	if (isPbmPresc)
		reCalcRowAmounts(row, true, processInsurance, undefined, prescription); // keep claim amounts when pbm presc.
	else
		reCalcRowAmounts(row, false, processInsurance, undefined, prescription);
	//this for displaying controltype message if it added to grid.
	medicineAddedToGrid = true;
	setTotals();
	document.getElementById("PayAndPrint").disabled = false;
	hideLoader();

}


/*
 * Open the edit dialog box: initialize dialog form values by copying
 * from the row's input values.
 */
function openEditDialogBox (obj) {
	var rowObj = findAncestor(obj,'TR');
	gRowUnderEdit = rowObj;
	var form = document.editForm;
	if(gIsReturn ){
		document.getElementById("editManagementAccordion").style.display="none";
		document.getElementById("editPresMang").style.display="none";
	}
	var medicineId = getElementByName(rowObj,'medicineId').value;
	var batchNo = getElementByName(rowObj,'batchNo').value;
	setMedicineUOMOptions(document.editForm.eSaleUnit,getElementByName(rowObj,'medName').value);//load UOM
	form.eSaleUnit.value = getElementByName(rowObj, 'issueUnits').value;
	var allBatches = gMedicineBatches[medicineId];
	initBatchAutoComplete('edit_batch_no', 'edit_batch_dropdown', allBatches,
			onSelectEditBatch, false, null);
	form.batch_no.readOnly = false;

	setLabel(document.getElementById('item_name'),getElementByName(rowObj,'medName').value);
	form.medicineId.value = getElementByName(rowObj, 'medicineId').value;

	document.getElementById('ed_consumption_uom').disabled = true;
	document.getElementById('ed_doc_remarks').disabled = true;
	document.getElementById('ed_special_instruction').disabled = true;
	document.getElementById('ed_qty').disabled = true;
	document.getElementById('ed_routeOfAdmin').disabled = true;

	var frequency = getElementByName(rowObj,'frequency').value;
	var duration  = getElementByName(rowObj,'duration').value;
	var dosage	  = getElementByName(rowObj,'dosage').value;
	var doctorRemarks = getElementByName(rowObj,'doctorRemarks').value;
	var special_instr = getElementByName(rowObj,'special_instr').value;
	var salesRemark = getElementByName(rowObj, 'salesRemarks').value;
	var routeName = !empty(getElementByName(rowObj, 'routeName').value)?getElementByName(rowObj, 'routeName').value :'';
	var durationUnit = getElementByName(rowObj, 'durationUnit').value;
	var warningLabel = getElementByName(rowObj, 'warningLabel').value;
	var dosageUnit = getElementByName(rowObj, 'dosageUnit').value;
	var totalQty = getElementByName(rowObj,'totalQty').value;
	var consultId = getElementByName(rowObj,'consultId').value;
	var consultCheck =getElementByName(rowObj,'consCheck').value;
	var cat_payable = getElementByName(rowObj,'cat_payable').value;
	var priIncludeInClaim = getElementByName(rowObj,'priIncludeInClaim').value;
	var secIncludeInClaim = getElementByName(rowObj,'secIncludeInClaim').value;
	var visitSellingPrice = getElementByName(rowObj,'visit_selling_price').value;
	var storeSellingPrice = getElementByName(rowObj,'store_selling_price').value;
	var sellingPriceHid = getElementByName(rowObj,'selling_price_hid').value;
	var erxActivityId = getElementByName(rowObj,'erxActivityId').value;
    var isDoctorExcluded = getElementsByName(rowObj, 'item_excluded_from_doctor')[0].value;
    var doctorExclusionRemarks = getElementsByName(rowObj, 'item_excluded_from_doctor_remarks')[0].value;
	document.getElementById('ewarn_label').value = warningLabel;
	document.getElementById('ed_remarks').value = salesRemark;
	document.getElementById('ed_routeOfAdmin').value = routeName;
	document.getElementById('ed_routeOfAdmin').title = routeName;
	document.getElementById('ed_consumption_uom').value = dosageUnit;
	document.getElementById('ed_consumption_uom_label').textContent = dosageUnit;
	document.getElementById('ed_visit_selling_price').value = visitSellingPrice;
	document.getElementById('ed_store_selling_price').value = storeSellingPrice;
	document.getElementById('ed_selling_price_hid').value = sellingPriceHid;
	if (document.getElementById('ed_erx_activity_id')) {
		document.getElementById('ed_erx_activity_id').value = erxActivityId;
	}
	document.getElementById('ed_priIncludeInClaim').value = priIncludeInClaim;
	document.getElementById('ed_secIncludeInClaim').value = secIncludeInClaim;

	if(!gIsReturn) {
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				if(gItemTaxGroups[key].item_group_id) {
					var groupId = gItemTaxGroups[key].item_group_id;
					var taxSubGroupId = getElementByName(rowObj,'taxsubgroupid'+groupId).value;
					if(document.getElementById('ed_taxsubgroupid'+groupId))
						document.getElementById('ed_taxsubgroupid'+groupId).value = taxSubGroupId;
				}
			}
		}
	}
	document.getElementById('ed_isDoctorExcludedFlag').value = isDoctorExcluded;
	if(isDoctorExcluded == "true" || isDoctorExcluded == "Y") {
	    document.getElementById('ed_isDoctorExcluded').innerText = 'Excluded';
	    document.getElementById('isDoctorExcluded').style = "visibility:normal";
	} else if (isDoctorExcluded == "false" || isDoctorExcluded == "N") {
	    document.getElementById('ed_isDoctorExcluded').innerText = 'Included';
	    document.getElementById('isDoctorExcluded').style = "visibility:normal";
	} else {
	    document.getElementById('ed_isDoctorExcluded').innerText = '';
	    document.getElementById('isDoctorExcluded').style = "visibility:hidden";
	}

	document.getElementById('ed_doctorExclusionRemarksValue').value = doctorExclusionRemarks;
	document.getElementById('ed_doctorExclusionRemarks').innerText = (doctorExclusionRemarks != null ? doctorExclusionRemarks : "");
	if(cat_payable != undefined && cat_payable != null && gIsInsuranceBill) {
		document.getElementById('coverdbyinsurancestatus').style.display = 'table-row';
		if(cat_payable == "f") {
			document.getElementById('ed_coverdbyinsurance').textContent = "No";
			document.getElementById('ed_coverdbyinsurance').style.color = "red";
		} else {
			document.getElementById('ed_coverdbyinsurance').textContent = "Yes";
			document.getElementById('ed_coverdbyinsurance').style.color = "#666666";
		}
		document.getElementById('ed_coverdbyinsuranceflag').value = cat_payable;
	} else {
		document.getElementById('coverdbyinsurancestatus').style.display = 'none';
	}
	if(frequency == 0) {
		document.getElementById('ed_frequency').value = '';
		} else {
		document.getElementById('ed_frequency').value = frequency;
	}
	if(dosage == 0){
		document.getElementById('ed_dosage').value = '';
		} else {
		document.getElementById('ed_dosage').value = dosage;
	}
	if(duration == 0) {
		document.getElementById('ed_duration').value = '';
		} else {
		document.getElementById('ed_duration').value = duration;
	}
	document.getElementById('ed_doc_remarks').value = doctorRemarks;
	document.getElementById('ed_special_instruction').value = special_instr;
	var els = document.getElementsByName("ed_duration_units");
		for (var k=0; k<els.length; k++) {
			if (els[k].value == durationUnit) {
				els[k].checked = true;
				break;
			}
		}

	if(consultId != '' && consultCheck == '') {
			calcQty(this.event,'ed');
		} else if(totalQty == 0) {
				document.getElementById('ed_qty').value = '';
		} else {
				document.getElementById('ed_qty').value = totalQty;

		}

	form.batch_no.value	= batchNo;
	if (batchAutoComp) {
		batchAutoComp._sInitInputValue = batchNo;
		batchAutoComp._bItemSelected = true;
	}

	form.mrp.value = formatAmountValue(getElementByName(rowObj,'pkgmrp').value, false);

	if ((gAllowRateChange == 'A') || (gRoleId == 1 || gRoleId == 2)){
			form.mrp.disabled = false;
	} else{
		form.mrp.disabled = true;
	}
	document.getElementById('costPriceRow').style.display = 'none';

	if ((gAllowDiscounts != 'N') || (gRoleId == 1 || gRoleId == 2)){
		form.discountper.disabled = false;
	} else{
		form.discountper.disabled = true;
	}

	form.discountper.value = getElementByName(rowObj,'medDisc').value;

//	if( !gIsInsuranceBill) {
//		form.discountper.value = getElementByName(rowObj, 'medDiscWithoutInsurance').value;
//	}

	setSelectedIndex(form.discounttype, getElementByName(rowObj,'medDiscType').value);

	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var medPBMStatus = getElementByName(rowObj, 'medPBMStatus').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	if (getElementByName(rowObj, 'identification').value == 'S') {
		form.saleqty.value = '1';
		form.saleqty.readOnly = true;
	} else {
		form.saleqty.value = getElementByName(rowObj,'userQty').value;
		form.saleqty.readOnly = false;
	}

	if (isPbmPresc) {
		// Allow to edit values including qty to be edited
		// when pbm presc. status is denied.
		if (medPBMStatus == 'D') {
			form.saleqty.readOnly = false;
			form.mrp.readOnly = false;
			form.cp.readOnly = false;
			form.discountper.readOnly = false;
			form.discounttype.disabled = false;
			getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').readOnly = false;
			getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').readOnly = false;

			if (form.eSaleUnit != null)
				form.eSaleUnit.disabled = false;

		}else {
			form.saleqty.readOnly = true;
			form.mrp.readOnly = true;
			form.cp.readOnly = true;
			form.discountper.readOnly = true;
			form.discounttype.disabled = true;
			getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').readOnly = true;
			if (medPBMStatus == 'C')
				getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').readOnly = true;
			else
				getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').readOnly = false;

			if (form.eSaleUnit != null)
				form.eSaleUnit.disabled = true;
		}
		
		var userQty = getElementByName(rowObj,'userQty').value;
		var pkgSize = getElementByName(rowObj,'pkgUnit').value;
		
		form.issueQty.value = Math.round(userQty * pkgSize);

	}else if (gIsReturn && returnType == 'ROAOB') {
		// don't allow edit of mrp/cp/discount. Only qty is editable.
		form.mrp.readOnly = true;
		form.cp.readOnly = true;
		form.discountper.readOnly = true;
		form.discounttype.disabled = true;
		if (pIndentNo != '') {
			form.batch_no.disabled = true;
		}
	} else {
		form.mrp.readOnly = false;
		form.cp.readOnly = false;
		form.discountper.readOnly = false;
		form.discounttype.disabled = false;
	}

	if (document.getElementById("editPBM_status") != null) {
		var pbmStatusTxt = rowObj.cells[PBM_STATUS_COL].textContent;
		if (medPBMStatus == 'C')
			document.getElementById("editPBM_status").innerHTML = "<font color='green'>"+pbmStatusTxt+"</font>";
		else if (medPBMStatus == 'D')
			document.getElementById("editPBM_status").innerHTML = "<font color='red'>"+pbmStatusTxt+"</font>";
		else if (medPBMStatus == 'O')
			document.getElementById("editPBM_status").innerHTML = pbmStatusTxt;
	}

	if ( !qtyEditable ) {
		form.saleqty.readOnly = true;//store is not allowed to partially fulfil return indents
		document.getElementById("edit_batch_no").disabled = true;
	}


	//multi-payer
	setPrimClaimRelatedValues(rowObj);
	if ( hasMoreThanOnePlan() )
		setSecClaimRelatedValues(rowObj);
	document.getElementById('edlg_pat_amt').innerHTML = formatAmountPaise(getPaise(getElementByName(rowObj,'patCalcAmt').value)
			+ getPaise(getElementByName(rowObj,'patientTaxAmt').value));//patientTaxAmt
	document.getElementById('edlg_amt').innerHTML = getElementByName(rowObj,'amt').value;

	//document.getElementById("editPrior_auth_id").value = rowObj.cells[PRIM_PRE_AUTH_NO_COL].textContent;
	//setSelectedIndex(document.getElementById("editPrior_auth_mode_id"),
	//		getElementByName(rowObj,'preAuthModeId').value);

	//multi-payer
	changePrimInsuRowDisplay(gIsInsuranceBill ? '' : 'none');
	if ( hasMoreThanOnePlan() )
		changeSecInsuRowDisplay(gIsInsuranceBill ? '' : 'none');

	YAHOO.util.Dom.addClass(rowObj, 'editing');

	var button = getElementByName(rowObj,'edit');
	document.getElementById("edititemdialog").style.display = 'block';
	edititemdialog.cfg.setProperty("context", [button, "tr", "br"], false);
	edititemdialog.show();
	form.batch_no.focus();
	if(gIsReturn){
		document.getElementById("presEdit").style.display = "none";
	} else if(gTransaction){
		document.getElementById("presEdit").style.display ="none";
	} else{
		document.getElementById("presEdit").style.display = "block";
	}
}

/*
 * Close the dialog box: copy back into row, and recalculate totals.
 */
function closeEditDialogBox() {
	var rowObj = gRowUnderEdit;
	var form = document.editForm;

	var oldAllowQty = eval(getElementByName(rowObj,'allowedQty').value);
	var medicineId = eval(getElementByName(rowObj,'medicineId').value);
	var totalPresSoldQty = eval(getElementByName(rowObj,'total_issed_qty').value);
	var oldInsuranceExpired = eval(getElementByName(rowObj,'insuranceExpired').value);
	var oldFrequency = getElementByName(rowObj,'frequency').value;
	var durationUnit = getElementByName(rowObj,'durationUnit').value;
	var durationVal = getElementByName(rowObj,'duration').value;
	var oldFreqCount = getElementByName(rowObj,'freqCount').value;
	var oldUserQty = getElementByName(rowObj,'userQty').value;
	var consultId = getElementByName(rowObj,'consultId').value;
	var visitSellingPrice = form.ed_visit_selling_price.value;
	var storeSellingPrice = form.ed_store_selling_price.value;
	var sellingPriceHid = form.ed_selling_price_hid.value;

	if (!validatePriorAuthMode(getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id'),
		getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_mode_id'),null, null)){
		return false
	}

	if(hasMoreThanOnePlan() && !validatePriorAuthMode(getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_id'),
		getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_mode_id'),null, null)){
		return false;
	}

	valid = validateEdits(rowObj);

	if (!valid)
		return false;

	var batch = getBatch(form.medicineId.value, form.batch_no.value);
	var userQty = form.saleqty.value;


	var duration_radio_els = document.getElementsByName('ed_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	var allowedQty;
	var flagStatus = false;
	//added for insurance expire vaidation.
	var policyExpireDays = document.getElementById("policyExpireDays").value;
	if(insurExpireAllow == "Y" && policyExpireDays != "" && gIsInsuranceBill && consultId != "" && consultId != undefined) {
		var freq = 1;
		/*if(oldFrequency != (form.ed_frequency.value!=""?form.ed_frequency.value:0)) {
			if(form.ed_frequency.value!= null || form.ed_frequency.value!= undefined || form.ed_frequency.value!= ""){
				freq = getPerDayFreq(form.ed_frequency.value);
			}
			allowedQty = Math.round(parseInt(policyExpireDays)*freq);
			allowedQty = allowedQty - (totalPresSoldQty)
			setHiddenValue(rowObj, "freqCount" ,freq);
			setHiddenValue(rowObj, "allowedQty" , allowedQty);
			setHiddenValue(rowObj, "insuranceExpired" , false);
			if(parseInt(userQty) > allowedQty) {
				setHiddenValue(rowObj, "insuranceExpired" , true);
			}

		} else */
		if(parseInt(oldUserQty) == parseInt(userQty)) {
			allowedQty = itemQtyMap[medicineId+"_aq"];
			serItemUserQty = itemQtyMap[batch.medicine_id];
			setHiddenValue(rowObj, "freqCount" ,oldFreqCount);
			setHiddenValue(rowObj, "allowedQty" , oldAllowQty);
			if(oldInsuranceExpired == true) {
				if(parseInt(allowedQty) < parseInt(serItemUserQty)) {
					setHiddenValue(rowObj, "insuranceExpired" , true);
				} else {
					setHiddenValue(rowObj, "insuranceExpired" , false);
				}
			} else {
				setHiddenValue(rowObj, "insuranceExpired" , oldInsuranceExpired);
			}


		} else if(parseInt(oldUserQty) != parseInt(userQty)) {
			allowedQty = itemQtyMap[medicineId+"_aq"];
			setHiddenValue(rowObj, "freqCount" ,oldFreqCount);
			setHiddenValue(rowObj, "allowedQty" , allowedQty);
			setHiddenValue(rowObj, "insuranceExpired" , false);
			serItemUserQty = itemQtyMap[batch.medicine_id];
			serItemUserQty = serItemUserQty - oldUserQty;
			serItemUserQty = serItemUserQty + parseInt(userQty);
			itemQtyMap[batch.medicine_id] = serItemUserQty;
			if(parseInt(allowedQty) < parseInt(serItemUserQty)) {
				setHiddenValue(rowObj, "insuranceExpired" , true);
			}
			flagStatus = true;
			itemQtyMap[medicineId+"_aq"] = allowedQty;
		}
	}
	//End
	setNodeText(rowObj.cells[USER_QTY_COL], userQty);
	setHiddenValue(rowObj, "userQty", userQty);
	getElementByName(rowObj, 'medDisc').value = form.discountper.value;

	if(!gIsInsuranceBill) {
		getElementByName(rowObj, 'medDiscWithoutInsurance').value = form.discountper.value;
	} else {
		getElementByName(rowObj, 'medDiscWithInsurance').value = form.discountper.value;
	}

	getElementByName(rowObj, 'medDiscType').value = form.discounttype.value;
	getElementByName(rowObj, 'issueUnits').value = form.eSaleUnit.value;
	getElementByName(rowObj, 'warningLabel').value = form.ewarn_label.value;
	getElementByName(rowObj, 'salesRemarks').value = form.ed_remarks.value;
	getElementByName(rowObj, 'doctorRemarks').value = form.ed_doc_remarks.value;
	getElementByName(rowObj, 'special_instr').value = form.ed_special_instruction.value;
	getElementByName(rowObj, 'duration').value = form.ed_duration.value;
	getElementByName(rowObj, 'dosage').value = form.ed_dosage.value;
	getElementByName(rowObj, 'frequency').value = form.ed_frequency.value;
	getElementByName(rowObj, 'totalQty').value = form.ed_qty.value;
	getElementByName(rowObj, 'consCheck').value = 'noCheck';
	var isDoctorExcluded = getElementsByName(rowObj, 'item_excluded_from_doctor')[0].value;
    var doctorExclusionRemarks = getElementsByName(rowObj, 'item_excluded_from_doctor_remarks')[0].value;

    if(isDoctorExcluded == "true" || isDoctorExcluded == "Y") {
        document.getElementById('ed_isDoctorExcluded').innerText = 'Yes';
        document.getElementById('isDoctorExcluded').style = "visibility:normal";
    } else if (isDoctorExcluded == "false" || isDoctorExcluded == "N") {
        document.getElementById('ed_isDoctorExcluded').innerText = 'No';
        document.getElementById('isDoctorExcluded').style = "visibility:normal";
    } else {
        document.getElementById('ed_isDoctorExcluded').innerText = '';
        document.getElementById('isDoctorExcluded').style = "visibility:hidden";
    }

    document.getElementById('ed_doctorExclusionRemarksValue').value = doctorExclusionRemarks;
    document.getElementById('ed_doctorExclusionRemarks').innerText = (doctorExclusionRemarks != null ? doctorExclusionRemarks : "");
	if(!gIsReturn) {
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				if(gItemTaxGroups[key].item_group_id) {
					var taxGroupId = gItemTaxGroups[key].item_group_id;
					if(document.getElementById('ed_taxsubgroupid'+taxGroupId)) {
						var taxSubGroupId = document.getElementById('ed_taxsubgroupid'+taxGroupId).value;
						if(getElementByName(rowObj, 'taxsubgroupid'+taxGroupId))
							getElementByName(rowObj, 'taxsubgroupid'+taxGroupId).value = taxSubGroupId;
					}
				}
			}
		}
	}

	if(!empty(duration_units)){
		getElementByName(rowObj,'durationUnit').value = duration_units;

	} else {
		getElementByName(rowObj, 'durationUnit').value = '';
	}

	if (document.getElementById("ed_erx_activity_id") != null && document.getElementById("ed_erx_activity_id") != "") {
		setNodeText(rowObj.cells[ERX_REF_NO_COL], getElementByName(document.getElementById("ed_erxReferenceRow"),'ed_erx_activity_id').value)
		setHiddenValue(rowObj, "erxActivityId", getElementByName(document.getElementById("ed_erxReferenceRow"),'ed_erx_activity_id').value);
	}

	var primCalAmount = getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').value;
	var secCalAmount = 0.00;
	setNodeText(rowObj.cells[PRIM_PRE_AUTH_NO_COL], getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').value);
	getElementByName(rowObj, 'primpreAuthId').value = getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').value;
	getElementByName(rowObj, 'primpreAuthModeId').value = getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_mode_id').value;
	getElementByName(rowObj, 'primclaimAmt').value = primCalAmount;
	if(isNotNullValue('ed_priInsClaimTaxAmt'))
		getElementByName(rowObj, 'priClaimTaxAmt').value = getFieldValue('ed_priInsClaimTaxAmt');

	if ( hasMoreThanOnePlan() ) {
		setNodeText(rowObj.cells[SEC_PRE_AUTH_NO_COL], getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_id').value);
		getElementByName(rowObj, 'secpreAuthId').value = getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_id').value;
		getElementByName(rowObj, 'secpreAuthModeId').value = getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_mode_id').value;
		secCalAmount = getElementByName(document.getElementById("sec_claim_row"),'edlg_claim_amt').value;
		getElementByName(rowObj, 'secclaimAmt').value = secCalAmount;
		if(isNotNullValue('ed_secInsClaimTaxAmt'))
			getElementByName(rowObj, 'secClaimTaxAmt').value = getFieldValue('ed_secInsClaimTaxAmt');
	}
	if(gIsInsuranceBill) {
		if(getAmount(primCalAmount) > 0 || getAmount(secCalAmount) > 0 ) {
			getElementByName(rowObj, 'cat_payable').value = "t";
			getElementByName(rowObj, 'insurance_cat_payable').value = "t";
		} else {
			getElementByName(rowObj, 'cat_payable').value = "f";
			getElementByName(rowObj, 'insurance_cat_payable').value = "f";
		}

		if(getAmount(primCalAmount) > 0 && form.ed_priIncludeInClaim.value == 'N'){
			setHiddenValue(rowObj, "priIncludeInClaim", "Y");
		}

		if(getAmount(secCalAmount) > 0 && form.ed_secIncludeInClaim.value == 'N'){
			setHiddenValue(rowObj, "secIncludeInClaim", "Y");
		}
	}

	var amt = getElementByName(rowObj,'amt').value;
	var mrp = getElementAmount(form.mrp);
	var cp = getElementAmount(form.cp);

	var isSaleInIssueUnits = (form.eSaleUnit.value == '');
	if(flagStatus && gIsInsuranceBill) {
		setBatchProperties(rowObj, batch, mrp, cp, isSaleInIssueUnits, allowedQty, serItemUserQty);
	} else {
		setBatchProperties(rowObj, batch, mrp, cp, isSaleInIssueUnits, allowedQty, userQty);
	}


	/*
	 * Recalulcate the row amounts and totals: true indicates keep
	 * the existing claim amounts.
	 */
	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	// Do not recalculate in case of a pbm prescription
	var item_excluded_from_doctor = form.ed_isDoctorExcludedFlag.value;
	var item_excluded_from_doctor_remarks = form.ed_doctorExclusionRemarksValue.value;
	if (!isPbmPresc)
		reCalcRowAmounts(rowObj, true,true, undefined, {item_excluded_from_doctor , item_excluded_from_doctor_remarks});

	setTotals();

	if(gIsInsuranceBill && !gIsReturn && !isPbmPresc)
		onClickProcessIns('salesform');

	edititemdialog.cancel();
}

/**
 * When an item is added, or when any item input (like MRP/qty) is edited we recalculate
 * the following dependent variables and set them in the grid here:
 *   total amount
 *   discount amount (based on discount percentage)
 *   tax amount
 *   unit rate
 *
 * The calculations depend on the type of sale (CP/MRP) as well as the type of tax
 * (sale price includes or excludes tax). Because of this calculation, we cannot
 * accept discount in terms of amount from the user. We can only accept % and calculate
 * the actual amount.
 *
 * Assumes all inputs are validated before calling.
 * Added function call to get Patient Amts as well. In case of insurance patients,
 * amt that the patient has to pay will be different from the amt on the bill.
 * For non-insurance cases, the patient amt will be defaulted to the billed amt.
 */
function reCalcRowAmounts(rowObj, keepClaimAmount, processInsurance,location, prescription ) {


	if(!gIsReturn && location == undefined) {
		if(!gIsInsuranceBill) {
			setHiddenValue(rowObj, "medDisc", getElementByName(rowObj, 'medDiscWithoutInsurance').value);
		} else {
			setHiddenValue(rowObj, "medDisc", getElementByName(rowObj, 'medDiscWithInsurance').value);
		}
	}

	var itemDetails = {
		medicineId : getElementByName(rowObj, 'medicineId').value,
		batchNo: getElementByName(rowObj, 'batchNo').value,
		medDisc : getElementByName(rowObj, 'medDisc').value,
		pbmitemrate : getElementByName(rowObj, 'pbmitemrate').value,
		pbmdiscount : getElementByName(rowObj,'medDiscRS').value,
		medDiscType : getElementByName(rowObj, 'medDiscType').value,
		units : getElementByName(rowObj, 'issueUnits').value,
		userQty : getElementByName(rowObj, 'userQty').value,
		mrp : getElementByName(rowObj, 'pkgmrp').value,
		cp : getElementByName(rowObj, 'pkgcp').value,
		// return values
		issueQty : "0",
		userUOM : "",
		amount: "0",
		tax: getElementByName(rowObj, 'tax').value,
		discount: "0",
		claimAmt: getElementByName(rowObj, 'claimAmt').value,
		primClaimAmt: getElementByName(rowObj, 'primclaimAmt').value,
		secClaimAmt: getElementByName(rowObj, 'secclaimAmt').value,
		priClaimTaxAmt: getElementByName(rowObj, 'priClaimTaxAmt').value,
		secClaimTaxAmt: getElementByName(rowObj, 'secClaimTaxAmt').value,
		patientTaxAmt: getElementByName(rowObj, 'patientTaxAmt').value,
		patientAmt: getElementByName(rowObj, 'patCalcAmt').value,
		pbmApprovedAmt: "",
		medPBMId: getElementByName(rowObj, 'medPBMPrescId').value,
		unitRate: "0",
		approvedQty: getElementByName(rowObj, 'approvedQty').value,
		pbmPaymentAmt: getElementByName(rowObj, 'pbmPaymentAmt').value,
		item_excluded_from_doctor: prescription != null? prescription.item_excluded_from_doctor : "",
		item_excluded_from_doctor_remarks : prescription != null ? prescription.item_excluded_from_doctor_remarks : "",
		item_batch_id:getElementByName(rowObj, 'itemBatchId').value
	};

	var editedTaxSubGroups = [];
	if(!gIsReturn) {
		//Edited tax sub groups.
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				if(gItemTaxGroups[key].item_group_id) {
					var taxGroupId = gItemTaxGroups[key].item_group_id;
					if(getElementByName(rowObj, 'taxsubgroupid'+taxGroupId)) {
						var taxSubGroupId = getElementByName(rowObj, 'taxsubgroupid'+taxGroupId).value;
						if(taxSubGroupId && taxSubGroupId!= null && taxSubGroupId != '')
							editedTaxSubGroups.push(taxSubGroupId);
					}
				}
			}
		}
	}
	itemDetails.editTaxSubGroups = editedTaxSubGroups;

	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);
	// If the medicine is a PBM prescription then the claim amount remains not edited always.
	if (isPbmPresc){
		itemDetails.pbmApprovedAmt = rowObj.cells[PBM_APPRD_AMT_COL].textContent;
		reCalcAmts(itemDetails, isPbmPresc);
		setNodeText(rowObj.cells[PBM_APPRD_AMT_COL], itemDetails.pbmApprovedAmt);
		if(null!= itemDetails.pbmdiscount && "" != itemDetails.pbmdiscount && itemDetails.pbmdiscount != 0)
			setHiddenValue(rowObj, 'medDisc', formatAmountPaise(getPaise((itemDetails.pbmdiscount/getElementByName(rowObj, 'approvedNetAmount').value)*100)));
		else
			setHiddenValue(rowObj, 'medDisc', itemDetails.pbmdiscount);
	}
	else
		reCalcAmts(itemDetails, keepClaimAmount);

	var existQty = empty(getElementByName(rowObj, 'qty').value) ? 1 : getElementByName(rowObj, 'qty').value;

	if(isPbmPresc) {
		setHiddenValue(rowObj, "qty", Math.round(itemDetails.issueQty));
	} else {
		setHiddenValue(rowObj, "qty", itemDetails.issueQty);
	}
	
	//setNodeText(rowObj.cells[TOTAL_COL], itemDetails.amount);

	setHiddenValue(rowObj, 'amt', itemDetails.amount);
	setNodeText(rowObj.cells[TOTAL_COL], itemDetails.amount);


	setHiddenValue(rowObj, 'medDiscRS', itemDetails.discount == ''? formatAmountValue(0):itemDetails.discount);
	setNodeText(rowObj.cells[DISCOUNT_COL], itemDetails.discount == ''? formatAmountValue(0):itemDetails.discount);

	setHiddenValue(rowObj, 'taxPer', itemDetails.taxPer);
	setHiddenValue(rowObj, 'orgTaxAmt', itemDetails.orgTaxAmt);

	for(var j=0; j < groupListJSON.length; j++) {
		var itemGroupId = groupListJSON[j].item_group_id;
		if(itemDetails['taxrate'+itemGroupId] != undefined && itemDetails['taxrate'+itemGroupId] != 'undefined') {
			setHiddenValue(rowObj, 'taxrate'+itemGroupId, itemDetails['taxrate'+itemGroupId]);
		}
		if(itemDetails['taxamount'+itemGroupId] != undefined && itemDetails['taxamount'+itemGroupId] != 'undefined') {
			setHiddenValue(rowObj, 'taxamount'+itemGroupId, itemDetails['taxamount'+itemGroupId]);
		}
		if(itemDetails['taxsubgroupid'+itemGroupId] != undefined && itemDetails['taxsubgroupid'+itemGroupId] != 'undefined') {
			setHiddenValue(rowObj, 'taxsubgroupid'+itemGroupId, itemDetails['taxsubgroupid'+itemGroupId]);
		}
	}
	setHiddenValue(rowObj, 'tax', itemDetails.tax);
	setNodeText(rowObj.cells[TAX_COL], parseFloat(itemDetails.tax).toFixed(decDigits));

	setNodeText(rowObj.cells[UNIT_RATE_COL], parseFloat(itemDetails.unitRate).toFixed(decDigits));

	var patAmt = 0;
	var patTaxAmt = 0;
	var priClaimTaxAmt = 0;
	var secClaimTaxAmt = 0;
	var taxAmt = 0;

	priClaimTaxAmt = empty(itemDetails.priClaimTaxAmt) ? 0 : (parseFloat(itemDetails.priClaimTaxAmt));
	secClaimTaxAmt = empty(itemDetails.secClaimTaxAmt) ? 0 : (parseFloat(itemDetails.secClaimTaxAmt));
	taxAmt = empty(itemDetails.tax) ? 0: itemDetails.tax;
	patTaxAmt = formatAmountPaise(Math.round(getPaise(taxAmt)) - Math.round(getPaise(priClaimTaxAmt)) - Math.round(getPaise(secClaimTaxAmt)));
	patAmt = formatAmountPaise(empty(itemDetails.patientAmt) ? 0 : (Math.round(getPaise(itemDetails.patientAmt)) - Math.round(getPaise(taxAmt))));

	setNodeText(rowObj.cells[PAT_AMT_COL], (parseFloat(patAmt)+parseFloat(patTaxAmt)).toFixed(decDigits));
	setHiddenValue(rowObj, 'patCalcAmt', patAmt);

	setNodeText(rowObj.cells[PAT_TAX_AMT_COL], patTaxAmt);
	setHiddenValue(rowObj, "patientTaxAmt", patTaxAmt);

	setNodeText(rowObj.cells[PRIM_CLAIM_AMT_COL], (parseFloat(itemDetails.primClaimAmt)+parseFloat(priClaimTaxAmt)).toFixed(decDigits));
	setHiddenValue(rowObj, 'claimAmt', parseFloat(itemDetails.primClaimAmt).toFixed(decDigits));
	setHiddenValue(rowObj, 'primclaimAmt', empty(itemDetails.primClaimAmt) ? 0 : parseFloat(itemDetails.primClaimAmt).toFixed(decDigits));

	setHiddenValue(rowObj, "priClaimTaxAmt", parseFloat(priClaimTaxAmt).toFixed(decDigits));
	setNodeText(rowObj.cells[PRIM_CLAIM_TAX_AMT_COL], parseFloat(priClaimTaxAmt).toFixed(decDigits));

	if ( hasMoreThanOnePlan() ) {
		setNodeText(rowObj.cells[SEC_CLAIM_AMT_COL], (parseFloat(itemDetails.secClaimAmt)+parseFloat(secClaimTaxAmt)).toFixed(decDigits));
		setNodeText(rowObj.cells[SEC_CLAIM_TAX_AMT_COL], parseFloat(secClaimTaxAmt).toFixed(decDigits));

		setHiddenValue(rowObj, 'secclaimAmt', parseFloat(itemDetails.secClaimAmt).toFixed(decDigits));
		setHiddenValue(rowObj, "secClaimTaxAmt", parseFloat(secClaimTaxAmt).toFixed(decDigits));
	}


	setNodeText(rowObj.cells[MRP_COL], parseFloat(itemDetails.mrp).toFixed(decDigits));
	setHiddenValue(rowObj, 'pkgmrp', itemDetails.mrp);

	// todo: check why required.
	if (0 > gGrandTotalPaise) {
		theForm.disAmt.value = "";
		theForm.disAmt.focus();
	}

// calling insurance 3.0 calculator when re-calculations happened.
	if(gIsInsuranceBill && !gIsReturn && !isPbmPresc && processInsurance)
		onClickProcessIns('salesform');
}

function calculateTaxes(){

	if(gIsReturn){
		return;
	}

	var newurl = cpath + '/sales/getallitemstaxdetails.json';
	var taxObjList = [];
	var mr_no = getFieldHtml('patientMrno');

	var priSponsorId = '';
	if(gPatientInfo && gPatientInfo.patient_details_plan_details) {
		for(planDetails in gPatientInfo.patient_details_plan_details) {
			var planDetailsArray = gPatientInfo.patient_details_plan_details[planDetails];
			if(planDetailsArray.priority == 1) {
				priSponsorId = planDetailsArray.sponsor_id;
			}
		}
	}
	var saleTypes = document.getElementsByName('salesType');
	var saleTransactionType = '';
	for(var i=0; i<saleTypes.length; i++) {
		if(saleTypes[i].checked){
			saleTransactionType = saleTypes[i].value;
		}
	}

	var nationalityId = '';
	if(saleTransactionType == 'retail')
		nationalityId = getFieldValue('nationalityId');

	var table = document.getElementById("medList");
	for (var i=1;i<=getNumItems();i++) {
		var rowObj = table.rows[i];
		var medicineId = getElementByName(rowObj, 'medicineId').value;
		var units = getElementByName(rowObj, 'issueUnits').value;
		var userQty = getElementByName(rowObj, 'userQty').value;
		var mrp = getElementByName(rowObj, 'pkgmrp').value;
		var medDisc = getElementByName(rowObj, 'medDisc').value;
		var batchNo = getElementByName(rowObj, 'batchNo').value;
		var userQty = getElementByName(rowObj, 'userQty').value;
		var medDiscType = getElementByName(rowObj, 'medDiscType').value;
		var itemBatchId = getElementByName(rowObj, 'itemBatchId').value;

		var batch = getBatch(medicineId,batchNo);
		var pkgSize = batch.issue_base_unit;
		var taxType = batch.tax_type;

		var mrpPaise = getPaise(mrp);
		var discountPer = getAmount(medDisc);
		var issueQty = (units == '') ? userQty : userQty * pkgSize;

		var editedTaxSubGroups = '';
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				if(gItemTaxGroups[key].item_group_id) {
					var taxGroupId = gItemTaxGroups[key].item_group_id;
					if(getElementByName(rowObj, 'taxsubgroupid'+taxGroupId)) {
						var taxSubGroupId = getElementByName(rowObj, 'taxsubgroupid'+taxGroupId).value;
						if(taxSubGroupId && taxSubGroupId!= null && taxSubGroupId != '') {
							editedTaxSubGroups = editedTaxSubGroups + ',' +taxSubGroupId;
						}
					}
				}
			}
		}

		var taxitem = {
				quantity : issueQty,
				amount : formatAmountPaise(mrpPaise),
				package_unit : pkgSize,
				medicine_id: medicineId,
				disc:discountPer,
				discount_type: medDiscType,
				basis:taxType,
				is_tpa:gIsInsuranceBill,
				mr_no:mr_no,
				tpa_id:priSponsorId,
				nationality_id:nationalityId,
				item_batch_id:itemBatchId,
				patient_id : theForm.searchVisitId.value,
			};

		if(editedTaxSubGroups && editedTaxSubGroups!=null && editedTaxSubGroups.length > 0){
			taxitem['item_subgroup_id'] = editedTaxSubGroups;
		}
		taxObjList.push(taxitem);
	}

	var url = newurl;
	var params = {};
	params["taxList"] = taxObjList;
	var jsonParams = JSON.stringify(params) ;
	var xhr = newXMLHttpRequest();
	xhr.open('POST', url, false);
	xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
	xhr.send(jsonParams);

	if (xhr.readyState>3 && xhr.status==200) {
		gTaxMap = JSON.parse(xhr.responseText);
	}

}

function setTaxResponse(itemBatchId){

	var taxDetails = {
			vatRate: 0,
			vatAmt: 0,
			netAmt: 0,
			discountAmt: 0,
			original_tax: 0
	};

	if(gTaxMap  == ''){
		return taxDetails;
	}

	var itemMap = gTaxMap[itemBatchId];
	var taxMap = itemMap['tax_details'];
	var vatAmt = 0;
	var vatRate = 0;
	var originalTax = 0;
	var netAmt = 0;
	var discountAmt = 0;

	if(isNotNullObj(itemMap.net_amount))
		netAmt = itemMap.net_amount
	if(isNotNullObj(itemMap.discount_amount))
		discountAmt = itemMap.discount_amount;

	for(var i=0; i<taxMap.length; i++) {
	    for(var j=0; j < subgroupNamesList.length; j++) {
	    	var itemTaxDetails = taxMap[i];
	    	if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id] && taxMap[i][subgroupNamesList[j].item_subgroup_id] != null) {
	    		var val = taxMap[i][subgroupNamesList[j].item_subgroup_id];
	    		var itemGroupId = subgroupNamesList[j].item_group_id;

	    		vatAmt += parseFloat(val.amount);
			    vatRate += parseFloat(val.rate);
			    originalTax += parseFloat(val.original_tax_amt !=null ? val.original_tax_amt : 0.00 );
	    	}
		}
	}

	originalTax = vatAmt;

	taxDetails['vatRate'] = vatRate;
	taxDetails['vatAmt'] = vatAmt;
	taxDetails['netAmt'] = netAmt;
	taxDetails['discountAmt'] = discountAmt;
	taxDetails['original_tax'] = originalTax;

	return taxDetails;
}

/*
 * On any change in Edit dialog box, the display values for patient and claim
 * amounts are recalculated and shown.
 */
function onChangeEditAmounts() {
	recalcEditDialogAmts(false);
}

function onChangeEditDiscount() {
	recalcEditDialogAmts(false);
}

function onChangeClaimAmt() {
	var isValid = recalcEditDialogAmts(true);

	//As per insurance 3.0, while editing claim amount we have to set is_claim_locked flag to true
	//since it is edited
	var row = gRowUnderEdit;

	if (isValid)
		setHiddenValue(row, "is_claim_locked", "true");
}

function recalcEditDialogAmts(keepClaimAmt) {
	var rowObj = gRowUnderEdit;
	var form = document.editForm;

	valid = validateEdits(rowObj);
	if (!valid)
		return false;

	var itemDetails = {
		medicineId : form.medicineId.value,
		batchNo: form.batch_no.value,
		medDisc : form.discountper.value,
		medDiscType : form.discounttype.value,
		units : form.eSaleUnit.value,
		userQty : form.saleqty.value,
		mrp : form.mrp.value,
		cp : form.cp.value,
		// return values
		issueQty: "0",
		userUOM: "",
		amount: "0",
		tax: "0",
		discount: "0",
		claimAmt: getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').value,
		primClaimAmt: getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').value,
		secClaimAmt: getElementByName(document.getElementById("sec_claim_row"),'edlg_claim_amt').value,
		cat_payable: document.getElementById("ed_coverdbyinsuranceflag")!= undefined?document.getElementById("ed_coverdbyinsuranceflag").value:"",
		medPBMId: getElementByName(rowObj, 'medPBMPrescId').value,
		item_excluded_from_doctor : document.getElementById("ed_isDoctorExcludedFlag")!= undefined?document.getElementById("ed_isDoctorExcludedFlag").value:"",
		item_excluded_from_doctor_remarks : document.getElementById("ed_doctorExclusionRemarksValue")!= undefined?document.getElementById("ed_doctorExclusionRemarksValue").value:"",
		patientAmt: "0",
		unitRate: "0",
		item_batch_id:"0"
	}


	/*
	 * Calculate the total and patient amounts
	 */
	reCalcAmts(itemDetails, keepClaimAmt);

	getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').value = formatAmountValue(itemDetails.primClaimAmt);
	setFieldValue('ed_priInsClaimTaxAmt', itemDetails.priClaimTaxAmt);

	//multi-payer
	if ( hasMoreThanOnePlan() ) {
		getElementByName(document.getElementById("sec_claim_row"),'edlg_claim_amt').value = formatAmountValue(itemDetails.secClaimAmt);
		setFieldValue('ed_secInsClaimTaxAmt', itemDetails.secClaimAmt);
	}

	setElementText('edlg_pat_amt', formatAmountValue(itemDetails.patientAmt));
	setElementText('edlg_amt', formatAmountValue(itemDetails.amount));

	return true;
}


/*
 * Calculate the final amounts for a given item, discount, sale price.
 * All inputs/outputs are in strings, suitable for setting in the page,
 * but not for calculations without conversion to amounts/paise.
 * Claim Amount can be calculated, or set based on user input.
 */
function reCalcAmts(it, keepClaimAmt) {
	document.getElementById("PayAndPrint").disabled = true;
	showLoader();

	var mrpPaise = getPaise(it.mrp);
	var totalCpPaise = getPaise(it.cp);		// inclusive of tax
	var userQty = getAmount(it.userQty);
    var discountPer = getAmount(it.medDisc);

	// following are batch attributes, retrieved from batch details stored
	var batch = getBatch(it.medicineId, it.batchNo);
	var taxPer = 0;
	var pkgSize = batch.issue_base_unit;
	var taxType = batch.tax_type;

	var patAmtPaise = getPaise(batch.patient_amount);
	var patCatAmtPaise = getPaise(batch.patient_amount_per_category);
	var patPer = batch.patient_percent;
	var patPerPostDiscount = batch.is_copay_pc_on_post_discnt_amt;
	var patCapPaise = getPaise(batch.patient_amount_cap);
	var claimable = batch.claimable;					// when plan is 0
	var insurancePayable = batch.insurance_payable;		// when plan is non-0
	var itemInsuCatId = batch.insurance_category_id;

	// calculate the following values based on the above.
	var taxPaise = 0;		// tax amount
	var discountPaise = 0;	// discount amount
	var iamtPaise = 0;		// pre-discount amount, inclusive of tax
	var amtPaise = 0;		// final amount
	var unitRatePaise = 0;	// per unit rate, salePrice/pkgSize
	var issueQty = (it.units == '') ? it.userQty : userQty * pkgSize;
	var claimAmtPaise = 0;
	var primclaimAmtPaise = 0;
	var secclaimAmtPaise = 0;
	var patientAmtPaise = 0;
	var claimAmts = new Array();
	var claimTaxAmts = new Array();
	var patAmts = new Array();
	var amts = new Array();
	var patAmtCaps = new Array();
	var isDoctorExcluded = "";
	var doctorExclusionRemarks = "";
	if(it.item_excluded_from_doctor) {
        if(it.item_excluded_from_doctor == "MI" || it.item_excluded_from_doctor == "N") {
            isDoctorExcluded = "false";
        }
        if(it.item_excluded_from_doctor == "ME" || it.item_excluded_from_doctor == "Y"
         || it.item_excluded_from_doctor == "NA") {
            isDoctorExcluded = "true";
        }
	doctorExclusionRemarks = it.item_excluded_from_doctor_remarks;
	}


	// calculate the discount based on discount type etc.
	// Always calculate based on issue qty, then divide by pkgSize at the end for
	// better accuracy and avoiding float (many decimal places) problems.
	// normal (MRP Based) sale
	iamtPaise = (mrpPaise*issueQty/pkgSize).toFixed(0);
	unitRatePaise = Math.round(mrpPaise/pkgSize);
	//We are blocking this because of UAE taxation, we will enable this once we fix for GST taxation.
	/*var item = {
		quantity : issueQty,
		amount : formatAmountPaise(unitRatePaise),
		package_unit : pkgSize,
		medicine_id: it.medicineId,
	};
	var taxDetails = setTaxDetails(item, it);
	taxPer = taxDetails.vatRate;*/
	/*if (!taxType.match(/^C/)) {
		// pharma item: discount is based on adj MRP or MRP depending on discountType
		if (it.medDiscType != 'I') {
			// discount excluding VAT: this is normal
			var adjMrpPaise = (mrpPaise * 100) / (100 + taxPer);
			discountPaise = issueQty * discountPer * adjMrpPaise / pkgSize / 100;
		} else {
			// discount includes VAT, on the total amount
			discountPaise = issueQty * discountPer * mrpPaise / pkgSize / 100;
		}
		discountPaise = Math.round(discountPaise);

		// tax is standard, on the initial amount (before discounting)
		//taxPaise = iamtPaise - iamtPaise * 100 / (100 + taxPer);
		//taxPaise = Math.round(taxPaise);

	} else {
		// non-pharma item: deduct discount (on retail price) before calculating tax
		// discount Type is not considered here, assumed to be inclusive of VAT always.
		discountPaise = issueQty * discountPer * mrpPaise / pkgSize / 100 ;
		discountPaise = Math.round(discountPaise);

		// tax applies on net amount
		var netAmt = (iamtPaise - discountPaise);
		//taxPaise = netAmt - netAmt * 100 / (100 + taxPer);
		//taxPaise = Math.round(taxPaise);
	}*/
	var mr_no = getFieldHtml('patientMrno');
	var saleTypes = document.getElementsByName('salesType');
	var saleTransactionType = '';
	for(var i=0; i<saleTypes.length; i++) {
		if(saleTypes[i].checked){
			saleTransactionType = saleTypes[i].value;
		}
	}
	/*var saleTransactionType = saleTypes.find(function(saleType) { return saleType.checked });
	saleTransactionType = saleTransactionType ? saleTransactionType.value : '';*/

	var priSponsorId = '';
	if(gPatientInfo && gPatientInfo.patient_details_plan_details) {
		for(planDetails in gPatientInfo.patient_details_plan_details) {
			var planDetailsArray = gPatientInfo.patient_details_plan_details[planDetails];
			if(planDetailsArray.priority == 1) {
				priSponsorId = planDetailsArray.sponsor_id;
			}
		}
	}
	var nationalityId = '';
	if(saleTransactionType == 'retail')
		nationalityId = getFieldValue('nationalityId');

	var item = {
		quantity : issueQty,
		amount : formatAmountPaise(mrpPaise),
		package_unit : pkgSize,
		medicine_id: it.medicineId,
		disc:discountPer,
		discount_type:it.medDiscType,
		basis:taxType,
		is_tpa:gIsInsuranceBill,
		mr_no:mr_no,
		tpa_id:priSponsorId,
		nationality_id:nationalityId,
		patient_id : theForm.searchVisitId.value,
	};
	if(it.editTaxSubGroups && it.editTaxSubGroups!=null && it.editTaxSubGroups.length > 0){
		item['item_subgroup_id'] = it.editTaxSubGroups;
	}
	var taxDetails = {
		discountAmt:0,
		netAmt:0,
		vatAmt:0,
		vatRate:0
	};
	var saleType = 'S';
	if(gIsReturn) {
		item.sale_item_id = batch.sale_item_id;
		saleType = 'R';
	}
	//Call tax calculater to get Item Tax details.
	if ( gIsReturn ) {
        taxDetails =  setTaxDetails(item, it, saleType);
    } else {
    	if ( gTransaction == 'estimate' ) {
    		taxDetails = setTaxDetails(item, it, saleType);
    	} else {
    		taxDetails = !gOnChangeBillType ? setTaxDetails(item, it, saleType) : setTaxResponse(it.item_batch_id) ;
    	}
    }
	discountPaise = getPaise(parseFloat(taxDetails.discountAmt));
	// final amount is initial amount - discount
	//amtPaise = netAmt - discountPaise + getPaise(parseFloat(taxDetails.vatAmt));
	amtPaise = getPaise(parseFloat(taxDetails.netAmt)) - discountPaise;

	it.amount = parseFloat(formatAmountPaise(amtPaise)).toFixed(decDigits);
	it.discount = formatAmountPaise(discountPaise);
//	it.tax = parseFloat(taxDetails.vatAmt).toFixed(decDigits);
	it.tax = parseFloat(taxDetails.vatAmt);
	//it.orgTaxAmt = parseFloat(taxDetails.vatAmt).toFixed(decDigits);
	it.taxPer = taxDetails.vatRate;
	it.unitRate = formatAmountPaise(unitRatePaise);
	it.issueQty = issueQty;
	it.userUOM = (it.units == '') ? batch.issue_units : batch.master_package_uom;

//	if(gIsReturn) {
		// it.orgTaxAmt = parseFloat(taxDetails.original_tax).toFixed(decDigits);
		it.orgTaxAmt = parseFloat(taxDetails.original_tax);
//	}

	// now calculate the claim portion
	var plan = theForm.planId.value;

	if (keepClaimAmt) {
		// keep the original claim amount, just validate if user input is OK
		// if it is PBM presc then keep the patient amount paise
		//re-calculate the amount,unit rate,MRP

		var isPbmPresc = (!empty(it.medPBMId) && it.medPBMId != 0);
		if(isPbmPresc){
			patientAmtPaise=getPaise(it.patientAmt);
			var pbmPaymentAmtPaise = getPaise(it.pbmPaymentAmt);
			//var pbmApprovedAmount = !empty(prescription["pbmPaymentAmount"]) ? prescription["pbmPaymentAmount"] : 0;
			amtPaise = patientAmtPaise + pbmPaymentAmtPaise;
			//it.mrp = formatAmountPaise(amtPaise / it.approvedQty);
			//it.unitRate = formatAmountPaise(getPaise(it.mrp) / pkgSize);
			it.unitRate = formatAmountPaise(getPaise(it.pbmitemrate));
			it.mrp = formatAmountPaise(getPaise(it.pbmitemrate*pkgSize));

			userQty = it.userQty;
			it.amount = formatAmountPaise(amtPaise * userQty/it.approvedQty);
			patientAmtPaise = patientAmtPaise * userQty/it.approvedQty;

			pbmApprovedAmtPaise = pbmPaymentAmtPaise * userQty/it.approvedQty;
			it.pbmApprovedAmt = formatAmountPaise(pbmApprovedAmtPaise);
			it.primClaimAmt = it.pbmApprovedAmt;
			//it.discount = '0.00';
			//discountPer = '0.00';
			//it.medDisc = '0.00';
			it.discount = formatAmountPaise(getPaise(it.pbmdiscount*userQty/it.approvedQty));
			discountPer = it.discount;
			it.medDisc = it.discount;
		}
		else{
			primclaimAmtPaise = getPaise(it.primClaimAmt);
			secclaimAmtPaise = getPaise(it.secClaimAmt);
			if (primclaimAmtPaise > amtPaise || secclaimAmtPaise > amtPaise || (primclaimAmtPaise+secclaimAmtPaise > amtPaise)) {
				showMessage("js.sales.issues.claimamount.notmorethannetamount");
				return false;
			}

			patientAmtPaise = (amtPaise - (primclaimAmtPaise+secclaimAmtPaise));
			if ( (patCapPaise > 0) && (patientAmtPaise > patCapPaise) ) {
				var msg=getString("js.sales.issues.warning.patientamount.morethanpatientcapamount");
				msg+= formatAmountPaise(patCapPaise);
				alert(msg);
			}
		} //end of if-else of isPbmPresc

	} else {
		// calculate
		// start with entire amount is patient's (non-insurance)
		patientAmtPaise = amtPaise;
		primclaimAmtPaise = 0;
		secclaimAmtPaise = 0;
		it.primClaimAmt = 0;
		it.secClaimAmt  = 0;

		if (gIsInsuranceBill) {
			// need to get exact claim amount
			if (gIsReturn && (returnType == 'ROAOB')) {
				// returns always go by original claim amount: could have been edited by user
				var amtAfterClaim = amtPaise;
				primclaimAmtPaise = Math.round(getPaise(batch.insurance_claim_amt) * issueQty / batch.qty)

				//multi payer
				var batchclaimAmts = batch.claimAmts;
				var batchclaimTaxAmts = batch.claimTaxAmts;
				for(var i=0;i<batchclaimAmts.length;i++){
					claimAmts.push(Math.round(getPaise(batchclaimAmts[i]) * issueQty / batch.sale_qty));
					//claimTaxAmts.push(batchclaimTaxAmts[i] / batch.qty);
					patientAmtPaise = (amtAfterClaim - Math.round(getPaise(batchclaimAmts[i]) * issueQty / batch.sale_qty));
					//patientAmtPaise = patientAmtPaise - Math.round(getPaise(batchclaimTaxAmts[i]) * issueQty / batch.qty);
					amtAfterClaim = patientAmtPaise;
				}
				for(var i=0;i<batchclaimTaxAmts.length;i++){
					claimTaxAmts.push(Math.round(getPaise(batchclaimTaxAmts[i]) * issueQty / batch.qty));
				}

			} else if(visitWithPlan()){
			//multi-payer:visit with more than 1 plan
				var visitPlans = getpatPlanDetails();
				var amtAfterClaim = amtPaise;//this is before claim calculation
				for( var i = 0;i<visitPlans.length;i++ ){
					var planMasterDetails = getvisitPlansMasterDetails()[[visitPlans[i].plan_id]];
					var priIncludeInCalim = (editForm.ed_priIncludeInClaim!=undefined || editForm.ed_priIncludeInClaim!=null)?editForm.ed_priIncludeInClaim.value:"";
					var secIncludeInCalim = (editForm.ed_secIncludeInClaim!=undefined || editForm.ed_secIncludeInClaim!=null)?editForm.ed_secIncludeInClaim.value:"";
					for( var k = 0;k<planMasterDetails.length;k++ ){
						if( itemInsuCatId == planMasterDetails[k].insurance_category_id) {
							if ((planMasterDetails[k].insurance_payable == 'Y' && isDoctorExcluded == "") || (isDoctorExcluded == "false")){
								primclaimAmtPaise = 0;
								var copayBasis = planMasterDetails.is_copay_pc_on_post_discnt_amt == 'Y' ? amtAfterClaim : amtAfterClaim + discountPaise;
								if((it.cat_payable == 't' && (priIncludeInCalim == 'Y' && visitPlans[i].priority == 1)) || isDoctorExcluded == "false") {
									primclaimAmtPaise = calculateClaimAmount(visitPlans[i].plan_id,amtAfterClaim,theForm.visitType.value,itemInsuCatId, "false",formatAmountPaise(discountPaise));
								}
								if((it.cat_payable == 't' && (secIncludeInCalim == 'Y' && visitPlans[i].priority == 2)) || isDoctorExcluded == "false"){
									primclaimAmtPaise = calculateClaimAmount(visitPlans[i].plan_id,amtAfterClaim,theForm.visitType.value,itemInsuCatId, "false",formatAmountPaise(discountPaise));
								}
								patientAmtPaise = (amtAfterClaim - primclaimAmtPaise);

								claimAmts.push(primclaimAmtPaise);//claim amts array
								patAmts.push(patientAmtPaise);//pat amts array
								amts.push(amtAfterClaim);
								patAmtCaps.push(planMasterDetails[k].patient_amount_cap);
							} else {
								primclaimAmtPaise = 0;
								patientAmtPaise = amtAfterClaim;

								claimAmts.push(0);//claim amts array
								patAmts.push(amtAfterClaim);//pat amts array
								amts.push(amtAfterClaim);
								patAmtCaps.push(planMasterDetails[k].patient_amount_cap);
							}

						}
					}

					amtAfterClaim = patientAmtPaise;
					//claim is deducted from aptient amount,now the rest of patient amount is amount for next claim
				}

				for(var j = 0;j<claimAmts.length;j++){
					if (claimAmts[j]<0) {
						claimAmts[j] = 0;
						patAmts[j] = amts[j];
					} else if (patAmtCaps[j] > 0 && patAmts[j] >= patAmtCaps[j]) {
						patAmts[j] = patAmtCaps[j];
						claimAmts[j] = amts[j] - patAmts[j];
					}
				}
			} else if (claimable) {
				// if claimable, entire amount is claim
				primclaimAmtPaise = amtPaise;
				patientAmtPaise = 0;

				claimAmts.push(primclaimAmtPaise);//claim amts array
				patAmts.push(patientAmtPaise);//pat amts array
			}
			// else, equivalent to non-insurance
		}
	}

	it.claimAmt = formatAmountPaise(primclaimAmtPaise);
	it.patientAmt = formatAmountPaise(patientAmtPaise);
	for(var j = 0;j<claimAmts.length;j++){
		if ( j == 0 ) {
			it.primClaimAmt = formatAmountPaise(claimAmts[j]);
		} else {
			it.secClaimAmt  = formatAmountPaise(claimAmts[j]);
		}
	}
	for(var j = 0;j<claimTaxAmts.length;j++){
		if ( j == 0 ) {
			it.priClaimTaxAmt = formatAmountPaise(claimTaxAmts[j]);
		} else {
			it.secClaimTaxAmt = formatAmountPaise(claimTaxAmts[j]);
		}
	}
	document.getElementById("PayAndPrint").disabled = false;
	hideLoader();

}

function cancelEditDialogBox() {
	edititemdialog.cancel();
}

function onEditDialogCancel() {
	YAHOO.util.Dom.removeClass(gRowUnderEdit, 'editing');
}

/*
 * When batch changes (or is set for first time), set the batch related values.
 * to the grid based on the given batch.
 * mrp/cp are inputs since these can come from the edit dialog using user inputs.
 */
function setBatchProperties(rowObj, batch, mrp, pkgCP, isSaleInIssueUnits, allowedQty, userQty) {
	var pkgCPText = formatAmountValue("" + pkgCP);
	setHiddenValue(rowObj, 'pkgcp', pkgCPText);

	mrpText = formatAmountValue("" + mrp);
	setNodeText(rowObj.cells[MRP_COL], mrpText);
	setHiddenValue(rowObj, 'pkgmrp', mrpText);

	setNodeText(rowObj.cells[PKG_SIZE_COL], batch.issue_base_unit);
	setHiddenValue(rowObj, 'pkgUnit', batch.issue_base_unit);

	setHiddenValue(rowObj, 'expiry', formatExpiry(batch.exp_dt));
	setHiddenValue(rowObj, 'taxPer', 0);

	setHiddenValue(rowObj, 'itemCode', batch.item_code);

	// todo: float
	//setHiddenValue(rowObj, 'origRate',batch.selling_price);

	if (batch.control_type_name != 'Normal')
		rowObj.cells[CONTROL_TYPE_COL].innerHTML = "<font color='blue'>"+batch.control_type_name+"</font>";
	else
		setNodeText(rowObj.cells[CONTROL_TYPE_COL], batch.control_type_name, 10);
	setHiddenValue(rowObj, "controlTypeName", batch.control_type_name);

	setNodeText(rowObj.cells[BATCH_COL], batch.batch_no);
	setHiddenValue(rowObj, 'batchNo', batch.batch_no);
	setHiddenValue(rowObj, 'identification', batch.identification);
	setHiddenValue(rowObj, 'itemBatchId', batch.item_batch_id);

	var displayUnits =  isSaleInIssueUnits ? batch.issue_units : batch.master_package_uom;
	setNodeText(rowObj.cells[UOM_COL], displayUnits);

	var daysToExpire = batch.exp_dt != null ? getDaysToExpire(batch.exp_dt) : 'woexp';

	var flagColor = 'empty';
	if (daysToExpire <= 0) {
		flagColor = 'red';
	} else if (daysToExpire <= gExpiryWarnDays && patientPrescription) {
		flagColor = 'yellow';
	}

	var flagImg = rowObj.cells[BATCH_COL].getElementsByTagName("img")[0];
	flagImg.src = cpath + "/images/" + flagColor + "_flag.gif";

	var insuranceFlagImg = rowObj.cells[ITEM_COL].getElementsByTagName("img")[0];

	flagColor = 'empty';
	insuranceFlagImg.title = "";
	var policyExpireDays = document.getElementById("policyExpireDays").value;
	var insuraceExpiredValue = getElementByName(rowObj, "insuranceExpired").value;
	var calFlag = getElementByName(rowObj, "cat_payable").value;

	if(gIsInsuranceBill && calFlag != null && calFlag != undefined && calFlag == 'f') {
		flagColor = 'purple';
	} else if(policyExpireDays != "" && (insuraceExpiredValue == "true") && gIsInsuranceBill && patientPrescription) {
		flagColor = 'brown';
		insuranceFlagImg.title = "Claimable sale quantity is "+allowedQty;
	}
	insuranceFlagImg.src = cpath + "/images/" + flagColor + "_flag.gif";

	var stockAvlblQty = batch.qty;
	if (!isSaleInIssueUnits)
		stockAvlblQty = (stockAvlblQty/batch.issue_base_unit).toFixed(2);

	var bin = batch.bin;
	if (bin == null || bin == "")
		bin = "--";

	setNodeText(rowObj.cells[EXPIRY_COL], formatExpiry(batch.exp_dt) + "/" + bin + "/" + stockAvlblQty);

}

function getRowObject (id) {
	var itemListTable = document.getElementById("medList");
	return itemListTable.rows[id];
}

var gItemTotalPaise;
var gItemDiscountTotalPaise;
var gItemBilledPaise;
var gItemTaxTotalPaise;
var gBillDiscountPaise;
var gRoundOffPaise;
var gGrandTotalPaise;

function setTotals() {
	gItemTotalPaise = 0;
	gItemTaxTotalPaise = 0;
	gItemDiscountTotalPaise = 0;
	var gItemPatTotalPaise = 0;
	var gItemPatTaxTotalPaise = 0;
	var gItemClaimTotalPaise = 0;
	var gItemPriClaimTotalPaise = 0;
	var gItemPriClaimTaxTotalPaise = 0;
	var gItemSecClaimTotalPaise = 0;
	var gItemSecClaimTaxTotalPaise = 0;
	var numItems = getNumItems();
	for (var i=1; i<=numItems; i++) {
		var rowObj = getRowObject(i);
		gItemTaxTotalPaise += getElementPaise(getElementByName(rowObj,'tax'));
		gItemTotalPaise += getElementPaise(getElementByName(rowObj,'amt'));
		gItemPatTotalPaise += getElementPaise(getElementByName(rowObj, 'patCalcAmt'));
		gItemPatTaxTotalPaise += getElementPaise(getElementByName(rowObj, 'patientTaxAmt'));
		gItemClaimTotalPaise += (getElementPaise(getElementByName(rowObj, 'amt')) - getElementPaise(getElementByName(rowObj, 'patCalcAmt')));
		gItemDiscountTotalPaise += getElementPaise(getElementByName(rowObj,'medDiscRS'));

		//multi-payer
		gItemPriClaimTotalPaise += getElementPaise(getElementByName(rowObj, 'primclaimAmt'));
		gItemPriClaimTaxTotalPaise += getElementPaise(getElementByName(rowObj, 'priClaimTaxAmt'));
		gItemSecClaimTotalPaise += getElementPaise(getElementByName(rowObj, 'secclaimAmt'));
		gItemSecClaimTaxTotalPaise += getElementPaise(getElementByName(rowObj, 'secClaimTaxAmt'));
	}

	// bill discounts
	var doRoundOff = theForm.roundoff.checked;
	if(!gDoRoundOff) {
		theForm.roundoff.disabled = true;
		theForm.roundoff.checked = false;
		var doRoundOff = false;
	} else {
		theForm.roundoff.disabled = false;
	}
	gBillDiscountPaise = 0;
	//we will assume that bill discounts will only apply to patient portion of the bill
	if (theForm.discType.value == 'percent-inc' || theForm.discType.value == 'percent-exc') {
		// calculate the new amount
		var discountPer = getAmount(theForm.disPer.value);
		if (theForm.discType.value == 'percent-inc'){
			gBillDiscountPaise = discountPer * gItemTotalPaise / 100;
		} else {
			gBillDiscountPaise = discountPer * (gItemTotalPaise - gItemTaxTotalPaise)/ 100;
		}
		theForm.disAmt.value = getPaiseReverse(gBillDiscountPaise).toFixed(decDigits);
	} else {
		gBillDiscountPaise = getPaise(theForm.disAmt.value);
	}

	var gRoundOffPaise = 0;

	if (doRoundOff) {
		var total = gItemTotalPaise - gBillDiscountPaise;
		gRoundOffPaise = getRoundOffPaise(total);
	}

	gGrandTotalPaise = gItemTotalPaise - gBillDiscountPaise + gRoundOffPaise;
	gItemBilledPaise = gItemTotalPaise + gItemDiscountTotalPaise;

	gItemPatTotalPaise = gItemPatTotalPaise - gBillDiscountPaise + gRoundOffPaise; //we assume that bill level discounts and roundoffs are applicable only to patient portion

	document.getElementById("lblBilledAmount").innerHTML = getPaiseReverse(gItemBilledPaise).toFixed(decDigits);
	document.getElementById("lblItemDiscounts").innerHTML = getPaiseReverse(gItemDiscountTotalPaise).toFixed(decDigits);
	document.getElementById("lblItemTotal").innerHTML = getPaiseReverse(gItemTotalPaise).toFixed(decDigits);
	document.getElementById("lblBillDiscount").innerHTML = getPaiseReverse(gBillDiscountPaise).toFixed(decDigits);
	document.getElementById("roundOffAmt").innerHTML = getPaiseReverse(gRoundOffPaise).toFixed(decDigits);
	theForm.roundOffAmt.value = getPaiseReverse(gRoundOffPaise).toFixed(decDigits);
	document.getElementById("lblBillLevelAmt").innerHTML =
		(getPaiseReverse(gRoundOffPaise-gBillDiscountPaise)).toFixed(decDigits);
	document.getElementById("grandTotal").innerHTML = getPaiseReverse(gGrandTotalPaise).toFixed(decDigits);

	document.getElementById("lblPatAmount").innerHTML = (getPaiseReverse(gItemPatTotalPaise)+getPaiseReverse(gItemPatTaxTotalPaise)).toFixed(decDigits);
	document.getElementById("lblPatTaxAmount").innerHTML = getPaiseReverse(gItemPatTaxTotalPaise).toFixed(decDigits);
	document.getElementById("lblpriClaimAmount").innerHTML = (getPaiseReverse(gItemPriClaimTotalPaise)+getPaiseReverse(gItemPriClaimTaxTotalPaise)).toFixed(decDigits);
	document.getElementById("lblpriClaimTaxAmount").innerHTML = getPaiseReverse(gItemPriClaimTaxTotalPaise).toFixed(decDigits);
	document.getElementById("lblsecClaimAmount").innerHTML = (getPaiseReverse(gItemSecClaimTotalPaise)+getPaiseReverse(gItemSecClaimTaxTotalPaise)).toFixed(decDigits);
	document.getElementById("lblsecClaimTaxAmount").innerHTML = getPaiseReverse(gItemSecClaimTaxTotalPaise).toFixed(decDigits);

	var eligibleAmtPaise = gItemPatTotalPaise * redemption_cap_percent / 100;
	var eligibleAmt = formatAmountPaise(eligibleAmtPaise);

	var maxPointsRedeemable = Math.floor(eligibleAmt / points_redemption_rate);
	var calculatedEligibleAmtPaise =  getPaise(eligibleAmt - (eligibleAmt % points_redemption_rate));

	if (maxPoints>0) {
		document.getElementById('maxRewardPointsAmountLabel').innerHTML= formatAmountPaise(calculatedEligibleAmtPaise);
	}
	resetPayments();
}

/**
 * For billPaymentDetails tag, the following functions have to be defined.
 * resetTotalsForPayments() -- This function calls getTotalAmount() & getTotalAmountDue()
 * to set the total_AmtPaise and total_AmtDuePaise values for validations in tag.
 * And set the total payment amount.
 * getTotalDepositAmountAvailable() -- To get the deposit amount available.
 * getTotalRewardPointsAvailable() -- To get the reward points available.
 */
function resetPayments() {

	resetTotalsForPayments();
	resetTotalsForDepositPayments();
	resetTotalsForPointsRedeemed();

	// for bill now bill, auto-set the amount to be paid by patient.
	if ((theForm.billType.value == 'BN') || (theForm.billType.value == 'BN-I')) {
		// if a single payment mode exists, update that with the due amount automatically
		setTotalPayAmount();
	}
}

function getTotalAmount() {
	//var patTaxAmt = document.getElementById("lblPatTaxAmount").innerHTML?document.getElementById("lblPatTaxAmount").innerHTML:0;
	return parseFloat(getPaise(document.getElementById("lblPatAmount").innerHTML));
}

function getTotalAmountDue() {
	//var patTaxAmt = document.getElementById("lblPatTaxAmount").innerHTML?document.getElementById("lblPatTaxAmount").innerHTML:0;
	return parseFloat(getPaise(document.getElementById("lblPatAmount").innerHTML));
}

function getTotalDepositAmountAvailable() {
	if (!gIsReturn) {
		if (document.getElementById("maxAmtLabel") != null && !ipDepositExists) {
			return getPaise(document.getElementById("maxAmtLabel").innerHTML);
		}else if (document.getElementById("ipDepAmtLabel") != null && ipDepositExists) {
			return document.getElementById("totAvlDep").value;
		}else
			return 0;
	}else {
		if (document.getElementById("depositsetoffAmt") != null) {
			return getPaise(document.getElementById('depositsetoffAmt').innerHTML);
		}else
			return 0;
	}
}

function getTotalRewardPointsAvailable() {
	if (!gIsReturn) {
		if (document.getElementById("maxRewardPointsLabel") != null)
			return document.getElementById("maxRewardPointsLabel").innerHTML;
	}
	return 0;
}

function getTotalRewardPointsAmountAvailable() {
	if (!gIsReturn) {
		if (document.getElementById("maxRewardPointsAmountLabel") != null)
			return getPaise(document.getElementById("maxRewardPointsAmountLabel").innerHTML);
	}
	return 0;
}

function deleteItem(obj) {
	var rowObj = findAncestor(obj,'TR');
	document.getElementById("medList").deleteRow(rowObj.rowIndex);
	renumber();
	resetTempChargeId();
	var delItemQty = getElementByName(rowObj,'userQty').value;
	//serItemUserQty = serItemUserQty - parseInt(delItemQty);
	var delMedicineId = getElementByName(rowObj,'medicineId').value;
	itemQtyMap[delMedicineId] = itemQtyMap[delMedicineId] - parseInt(delItemQty);
	var rowObj = gRowUnderEdit;
	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	if(gIsInsuranceBill && !gIsReturn && !isPbmPresc)
		onClickProcessIns('salesform');
	setTotals();
}

function renumber() {
	var numItems = getNumItems();
	for (var i=1; i<=numItems; i++) {
		var row = getRowObject(i);
		setNodeText(row.cells[SLNO_COL], i);
	}
}

function resetTempChargeId() {
	var numItems = getNumItems();
	for (var i=1; i<=numItems; i++) {
		var row = getRowObject(i);
		setHiddenValue(row, "temp_charge_id", "_"+i);
	}
}

function onChangeSalesType() {
	clearCustomerDetails();
	deleteRows();
	document.getElementById("policyExpireDays").value = "";
	itemQtyMap = {};
	serItemUserQty = 0;
	oldMedicineId = '';
	setSalesType();
}

/*
 * show the appropriate section of the patient: hosp/retail/retailCredit
 */
function showPatientType() {
	if (gPatientType == 'retail') {
		// show the retail section, hide patient
		document.getElementById('custDetails').style.display="block";
		document.getElementById('patientDetails').style.display="none";
		document.getElementById('custRetailDetails').style.display="none";
		document.getElementById('prescDetailsDiv').style.display="none";
		document.getElementById('prescFieldSet').style.display="none";

	} else if(gPatientType == 'retailCredit') {
		// show the retail credit section, hide patient,reatil
		document.getElementById('custDetails').style.display="none";
		document.getElementById('patientDetails').style.display="none";
		document.getElementById('custRetailDetails').style.display="block";
		document.getElementById('prescDetailsDiv').style.display="none";
		document.getElementById('prescFieldSet').style.display="none";

	} else if(gPatientType == 'hospital') {
		// show the patient section, hide retail,retail Credit
		document.getElementById('patientDetails').style.display="block";
		if (document.getElementById('insuranceDetails')) {
			document.getElementById('insuranceDetails').style.display="block";
		}
		document.getElementById('custDetails').style.display="none";
		document.getElementById('custRetailDetails').style.display="none";
	}
}

function setPatientTypeAndSalesType(){
	salesType = getRadioSelection(theForm.salesType);
	if (salesType == 'returnBill')
		gPatientType = '';
	else
		gPatientType = salesType;

	if (document.getElementById('retailCreditPaymentDiv') != null)
	document.getElementById('retailCreditPaymentDiv').style.display="none";

	if (salesType == 'retail') {
		if(gRetailSaleRights != 'N'){
			showPatientType();
			theForm.searchVisitId.disabled = true;
			if(null != document.getElementById("readCard")) {
				document.getElementById("readCard").disabled = true;
			}
			setTimeout("theForm.custName.focus()",100);
		} else {
			showMessage("js.sales.issues.notauthorized.saleforretail");
			document.getElementById("salesType_credit_retail").checked=true;
			onChangeSalesType();
		}
	} else if (salesType == 'retailCredit') {
		//Check weather to allow to sale or not for retail credit.
		if((gRetailCreditSaleRights != 'N')||(gRoleId == 1 || gRoleId == 2 )){
			loadRetailSponsors();
			resetRetailCustomerDetails();
			initRetailCreditAutoComplete();
			showPatientType();
			theForm.searchVisitId.disabled = true;
			if(null != document.getElementById("readCard")) {
				document.getElementById("readCard").disabled = true;
			}
			setTimeout("theForm.custRetailCreditName.focus()", 100);
		} else {
			showMessage("js.sales.issues.notauthorized.saleforretailcredit");
			document.getElementById("salesType_hospital").checked=true;
			onChangeSalesType();
		}

	} else if(salesType == 'hospital') {
		showPatientType();
		theForm.searchVisitId.disabled = false;
		if(null != document.getElementById("readCard")) {
			document.getElementById("readCard").disabled = false;
		}
		if (gIsReturn) {
			theForm.rbillNo.disabled = true;
			theForm.rbillNo.value = "";
		}
		setTimeout("theForm.searchVisitId.focus()",100);

	} else if (salesType == 'returnBill') {
		showPatientType();
		// return against bill, disable the visit ID search
		theForm.rbillNo.disabled = false;
		theForm.searchVisitId.disabled = true;
		if(null != document.getElementById("readCard")) {
			document.getElementById("readCard").disabled = true;
		}
		setTimeout("theForm.rbillNo.focus()",100);
	}

	// set the store to the user's preferred store
	var defaultStore = gUserStoreId;
	if ( ( !gIsReturn && returnType != 'ROAOB' ) && ((defaultStore == null) || (defaultStore == "") ||
			( (defaultStore == 'InstaAdmin' || defaultStore == 'admin') && (gRoleId == 1 || gRoleId == 2) )) ){
		//defaultStore = '1';
		if (theForm.phStore.type == 'select-one') {
			setSelectedIndex(theForm.phStore, defaultStore);
			jMedicineNames = null;
			onChangeStore();
		}
	}
}

function readFromCard(){
	var ajaxReqObject = newXMLHttpRequest();
	var url = '//127.0.0.1:9876/devices/cardReader/read';
	var reqObject = newXMLHttpRequest();
	reqObject.open("GET", url.toString(), false);
	try {
		reqObject.send(null);
		if ((reqObject.readyState == 4) && (reqObject.status == 200) && (reqObject.responseText != null)) {
			smartCardDetails=reqObject.responseText;
			dt=JSON.parse(smartCardDetails);
			if(smartCardDetails != null && smartCardDetails !="" && dt['cardStatus'] == 'The Card Read Successfully'){
				if(null != dt["idNumber"]) {
					dt["idNumber"] = applyRegEx(dt["idNumber"],smartCardIDPattern);
				}
				document.getElementById("searchVisitId").value = dt["idNumber"];
				psAc.sendQuery(dt["idNumber"]);
				document.getElementById("searchVisitId").focus();
			} else if(dt['errorMessage'] != ""){
				alert(dt['errorMessage']);
			}
		} else {
			alert('Error in fetching card data.');
		}
	} catch (ex) {
		alert("InstaNexus App does not seem to be running, Please start the app and try again.");
	}
}

function applyRegEx(data,pattern) {
	var newData = "";
	if(null == pattern || null == data || pattern == "" || data == "")
		return data;
	var dataLen = data.length;
	var patternLen = pattern.length;
	if(patternLen < dataLen) {
		alert("Card data does not match defined pattern");
		return data;
	}
	var dataPos = 0;
	for(var i=0;i<patternLen;i++) {
		if(pattern[i]=='9') {
			if(dataPos < dataLen && data[dataPos] >='0' && data[dataPos] <='9') {
				newData = newData + data[dataPos];
				dataPos = dataPos + 1;
			}
			else {
				alert("Card data does not match defined pattern");
				return data;
			}
		}
		else if(pattern[i]=='x' || pattern[i]=='X') {
			if(dataPos < dataLen && (data[dataPos] >='a' && data[dataPos] <='z') || (data[dataPos] >='A' && data[dataPos] <='Z')) {
				newData = newData + data[dataPos];
				dataPos = dataPos + 1;
			}
			else {
				alert("Card data does not match defined pattern");
				return data;
			}
		}
		else {
			newData = newData + pattern[i];
		}
	}
	if (dataPos != dataLen) {
		alert("Card data does not match defined pattern");
		return data;
	}
	return newData;
}


function setSalesType() {
	setPatientTypeAndSalesType();
	clearCustomerDetails();
	setBillOptions();
	populateDepositRewardPointsDetails();
	setTotals();
	setRetiredEligibilitylink();
}

function deleteIndentRows(){
	var table = document.getElementById('prescInfo');

	var numItems = table.rows.length;
	for (var i=1; i<numItems-1; i++) {		// leave the heading (first) and template (last)
		table.deleteRow(1);
	}
	document.getElementById('prescFieldSet').style.display = 'none';
	document.getElementById('prescDetailsDiv').style.display="none";
}

function onChangeStore() {

	// need to clear the medicine list because the list of medicines available can change
	deleteRows();

	deleteIndentRows();

	// need to clear patient details and ask user to select again, since (esp for returns), we
	// allow to return items only to the same store, and list of items will change.
	clearCustomerDetails();

	if(theForm.rbillNo){
		theForm.rbillNo.value = '';
		clearCustomerDetails();
		deleteRows();
		setPatientTypeAndSalesType();
	}

	// load retail customers for selected store's center.
	initRetailCreditAutoComplete();

	setStore();

	// set new bill options: the counter is changed.
	setBillOptions();

	if (!gIsReturn || returnType != 'ROAOB') {
		// check and fetch the new set of medicines for store: chain it to init auto complete
		var storeId = theForm.phStore.value;
		if (itemAutoComp != undefined) {
			// make sure autocomp is not functional while we fetch the items
			itemAutoComp.destroy();
			itemAutoComp = undefined;
		}
		var storeIdObj = theForm.phStore;
		getMedicinesForStore(storeIdObj, initMedicineAutoComplete);
	}
	loadCenterPrefs();
}

function setIndentStore(){
	theForm.phStore.value =  ( indentStore == '-4'  ? theForm.phStore.value : indentStore );//defaults to indent store when it comes from patient indents list
}

function setStore() {
	var storeId = theForm.phStore.value;

	/*
	 * if the user's counter does not match the store's counter, clear the counterId
	 * value in the form, so that collecting money is disabled
	 */
	gStoreCounter = "";
	gStoreCounterName = "";
	gStoreSaleUnit = "";
	for (var i=0;i<jStores.length;i++) {
		if (jStores[i].DEPT_ID == storeId) {
			gStoreCounter = jStores[i].COUNTER_ID;
			gStoreCounterName = jStores[i].COUNTER_NO;
			storeAccountGroup = jStores[i].ACCOUNT_GROUP;
			gStoreSaleUnit = jStores[i].SALE_UNIT;
		}
	}

	if (gUserCounterId != "") {
		if (gStoreCounter == gUserCounterId) {
			theForm.counterId.value = gUserCounterId;
			document.getElementById("counter_label").className = "match";
		} else {
			theForm.counterId.value = "";
			document.getElementById("counter_label").className = "mismatch";
		}
	}

	var salesType = getRadioSelection(theForm.salesType);
}

function validateUserAccessToStore() {

	if ((gRoleId == 1) || (gRoleId == 2))
		return true;

	var defaultStore = gUserStoreId;
	if ( defaultStore == null || defaultStore == '' )
		defaultStore = 'GDEPT0003';
	var storeName = theForm.phStore.options[theForm.phStore.selectedIndex].text;
	var selectedStore = theForm.phStore.value;

	if ( ( gStoreAccess != 'A' ) && ( defaultStore != selectedStore ) ) {
		//theForm.phStore.value = defaultStore;
		var msg=getString("js.sales.issues.notauthorized.accessstore");
		msg+=storeName;
		alert(msg);
		return false;
	}
	return true;
}

function setBillOptions() {

	// defaults
	theForm.billType.length = 0;
	theForm.billType.disabled = false;

	var billNowText = "";
	var billNowInsText = "";
	var defaultToBillLater = (selectBillLater == 'Y');
	var numBillNows = 0;
	var numBillLaters = 0;

	if (theForm.counterId.value == "") {
		// no counter: allow only pending sales (money collected from a different counter)
		billNowText = "Raise Bill";
		billNowInsText = "Raise Bill (Insurance)";
	} else {
		// counter exists, allow only Bill Now (normal)
		billNowText = "Bill Now";
		billNowInsText = "Bill Now (Insurance)";
	}

	if (gPatientType == 'retail') {
		addOption(theForm.billType, billNowText, "BN");
		numBillNows += 1;

	} else if (gPatientType == 'retailCredit') {
		addOption(theForm.billType, "Add to Bill", "BL");

	} else if (gPatientType == 'hospital' && gPatientInfo != null && gIsReturn && returnType == 'ROAOB') {
		/*
		 * When returning against a bill (or homogenous types of bills against a visit) we allow only
		 * the same insurance as the original bill. We cannot return non-insurance for a
		 * insurance sale bill and vice-versa. Mixing the insurance of sale and return can cause
		 * problems in claims since we only claim the net amount in e-claims.
		 */
		if ( ((gPatientInfo.isTPA == null || gPatientInfo.isTPA) || visitWithPlan()) && allowBillNowInsurance == 'Y') {
			addOption(theForm.billType, billNowInsText, "BN-I");
			numBillNows += 1;
		}
		if ( (gPatientInfo.isTPA == null || !gPatientInfo.isTPA) || !visitWithPlan() ) {
			addOption(theForm.billType, billNowText, "BN");
			numBillNows += 1;
		}

		numBillLaters = addCreditBillOptions(gPatientInfo.bills, gPatientInfo.isTPA);

		if (gPatientInfo.billType == 'P') {
			defaultToBillLater = false;
		} else if (gPatientInfo.billType == 'C') {
			defaultToBillLater = true;	 // todo: get the orig bill number if possible, in case multiple
		} else {
			// mixed bill types: prefer bill later if patient is insured (mixed means insured, since at
			// least one bill with insurance is there.)
			if ( !visitWithPlan() ) {
				defaultToBillLater = true;
			}
			// else, leave it to prefs.
		}

	} else if (gPatientType == 'hospital' && gPatientInfo != null) {
		// normal sales or (return not against patient/bill)
		var numBillNows = 1;

		if (allowBillNowInsurance == 'Y'
				&&( hasMoreThanOnePlan() ||
					(gPatientInfo.patientDetails.primary_sponsor_id != null
					&& gPatientInfo.patientDetails.primary_sponsor_id != ''
					&& empty(gPatientInfo.patientDetails.secondary_sponsor_id)
					&& (gPatientInfo.patientDetails.use_drg == 'N')) )) {
			addOption(theForm.billType, billNowInsText, "BN-I");
			numBillNows += 1;
		}

		addOption(theForm.billType, billNowText, "BN");

		numBillLaters = addCreditBillOptions(gPatientInfo.bills, null);

		if ((gPatientInfo.seperateCreditBill == 'NOTEXISTS') && (theForm.visitStatus.value == 'A')
				&& (seperatePharmacyCreditBill == 'Y') && !gIsReturn) {
			// add a new bill option here itself
			addOption(theForm.billType, "Create New Bill", "BL");
			numBillLaters += 1;
		}

	} else {
		// either patientType is not determined yet (ROAOB, bill not selected)
		// or patientType is Hospital but patient not selected. Leave dropdown as empty
	}

	if (defaultToBillLater && numBillLaters > 0)
		theForm.billType.selectedIndex = numBillNows;  // choose first credit bill
	else
		theForm.billType.selectedIndex = 0;  // first bill now bill: prefer BN-I, which is added first
	gIsOnload = true;
	onChangeBillType();

}

function addCreditBillOptions(creditBills, isTpa) {
	var openCrdBills = 0;
	if (creditBills != null && creditBills.length > 0) {
		for (var i=0; i<creditBills.length; i++) {
			var bill = creditBills[i];
			if ((bill.status == 'A') && (isTpa == null || bill.is_tpa == isTpa) &&
					(bill.account_group == storeAccountGroup || seperatePharmacyCreditBill != 'Y')) {
				addOption(theForm.billType, "Add to bill: " + bill.bill_no, bill.bill_no);
				openCrdBills++;
			}
		}
	}
	return openCrdBills;
}

function showPayFields(show) {

	document.getElementById('payRefsTr').style.display = show ? 'table-row' : 'none';

	document.getElementById('narrationTd').style.display = show ? 'none' : '';
	document.getElementById('paymentRemarksTd').style.display = show ? 'none' : 'table-cell';
	// These 2 fields are hidden if billPaymentTag is used, hence make them enabled if payment is not done but
	// need remarks to be entered.
	getIndexedFormElement(theForm, "paymentRemarks", 0).disabled = show;

	var visitType = theForm.visitType.value;

	document.getElementById('deposit').style.display = (maxDeposit > 0 && !ipDepositExists) ? 'table-row' : 'none';
	document.getElementById('ipdeposit').style.display = (maxDeposit > 0 && ipDeposit > 0 && visitType == 'i' && ipDepositExists) ? 'table-row' : 'none';
	document.getElementById('rewardPoints').style.display = (maxPoints > 0) ? 'table-row' : 'none';
}

function showBillDetails(show) {
	var trBill = document.getElementById('billDetailsInner');
	var trBillHosp = document.getElementById('billDetailsInnerHosp');

	trBill.style.display = show ? 'table-row' : 'none';
	if (trBillHosp != null)
		trBillHosp.style.display = show ? 'table-row' : 'none';
}

function onChangeBillType() {

	gOnChangeBillType = true;
	gTaxMap = '';
	var storeRatePlanChanged = false;
	var insuranceChanged = false;
	var newStoreRatePlanId;
	var newInsurance;
	billDP = null;
	billDPstatus = false;

	var billno = theForm.billType.value;
	document.salesform.discountCategory.disabled = true;

	var selBill = null;
	if (gPatientInfo != null && !isNewBill()) {
		selBill = findInList(gPatientInfo.bills, 'bill_no', billno);
	}

	if (selBill != null) {
		newInsurance = selBill.is_tpa;
		newStoreRatePlanId = getStoreRatePlanId(selBill.bill_rate_plan_id);
		if(!gIsReturn && gTransaction != 'estimate')
			setDiscAuth(selBill.bill_rate_plan_id);
		document.salesform.creditBillNo.value = billno;
		var newDiscountPlan = selBill.discount_category_id;
		if (newDiscountPlan != null && newDiscountPlan != 0 && !gIsReturn) {
			billDP = newDiscountPlan;
			document.salesform.discountCategory.value = "";
			document.salesform.discountCategory.disabled = true;
			document.salesform.itemDiscPer.value = 0;
			document.salesform.itemDiscPer.disabled = true;
			document.salesform.itemDiscPerApply.disabled = true;
			billDPstatus = true;
		} else if(!gIsReturn && discCategoryNeeded){
			document.salesform.discountCategory.value = "";
			document.salesform.discountCategory.disabled = false;
			document.salesform.itemDiscPer.value = 0;
			document.salesform.itemDiscPer.disabled = false;
			document.salesform.itemDiscPerApply.disabled = false;
			billDPstatus = true;
		}
	} else {
		// this is a new bill: the rate plan will depend on type of patient and bill type chosen
		var ratePlanId;
		if (gPatientInfo != null && gPatientInfo.patientDetails != null) {
			if (billno == 'BN-I') {
				ratePlanId = gPatientInfo.patientDetails.org_id;
				newInsurance = true;
				if (!gIsReturn && insuranceHasDP) {
					document.salesform.discountCategory.disabled = true;
					document.salesform.itemDiscPer.disabled = true;
					document.salesform.itemDiscPerApply.disabled = true;
				} else if (!gIsReturn && discCategoryNeeded) {
					document.salesform.discountCategory.disabled = false;
					document.salesform.itemDiscPer.disabled = false;
					document.salesform.itemDiscPerApply.disabled = false;
				}
				document.salesform.discountCategory.value = "";
				document.salesform.itemDiscPer.value = 0;
				billDPstatus = true;
			} else {
				// BN: could be for an insured patient, if so use the preference
				var sponsor = gPatientInfo.patientDetails.primary_sponsor_id;
				if (sponsor != null && sponsor != '' && ratePlanForNonInsuredBill != '') {
					ratePlanId = ratePlanForNonInsuredBill;
				} else {
					ratePlanId = gPatientInfo.patientDetails.org_id;
				}
				newInsurance = false;
				if (!gIsReturn && discCategoryNeeded) {
					document.salesform.discountCategory.value = "";
					document.salesform.discountCategory.disabled = false;
					document.salesform.itemDiscPer.value = 0;
					document.salesform.itemDiscPer.disabled = false;
					document.salesform.itemDiscPerApply.disabled = false;
					billDPstatus = true;
				}
			}
		} else {
			// retail or retail credit
			ratePlanId = 'ORG0001';
			newInsurance = false;
			if (!gIsReturn && discCategoryNeeded) {
				document.salesform.discountCategory.value = "";
				document.salesform.discountCategory.disabled = false;
				document.salesform.itemDiscPer.value = 0;
				document.salesform.itemDiscPer.disabled = false;
				document.salesform.itemDiscPerApply.disabled = false;
				billDPstatus = true;
			}
		}
		newStoreRatePlanId = getStoreRatePlanId(ratePlanId);
		if(!gIsReturn && gTransaction != 'estimate')
			setDiscAuth(ratePlanId);
	}
	insuranceChanged = (gIsInsuranceBill != newInsurance);
	gIsInsuranceBill = newInsurance;
	storeRatePlanChanged = gStoreRatePlanId != newStoreRatePlanId;
	gStoreRatePlanId = newStoreRatePlanId;

	if (billDP) {
		document.getElementById("discountPlanId").value = billDP;
		document.salesform.discountCategory.value = billDP;
	} else if (gIsInsuranceBill && insuranceHasDP) {
		document.getElementById("discountPlanId").value = insuranceDP;
		document.salesform.discountCategory.value = insuranceDP;
	} else {
		document.getElementById("discountPlanId").value = "";
	}

	if (gTransaction == 'estimate') {
		displayButton('Estimate');
		return;
	}

	var origRoundOff = gDoRoundOff;
	if (billno != ''  && billno != 'BN' && billno != 'BN-I') {
		showPayFields(false);
		if (theForm.billType.length > 1) {
			addToPhBillTotals(billno);
			showBillDetails(true);
		}
		displayButton('AddToBill');
		gDoRoundOff = false;
		theForm.paymentModeId.options[0].selected=true;

	} else {
		if (theForm.counterId.value == "") {
			// bill now pending
			showPayFields(false);
			showBillDetails(false);
			displayButton('RaiseBill');
			gDoRoundOff = true;
			theForm.paymentModeId.options[0].selected=true;
		} else {
			// normal bill now
			gDoRoundOff = true;
			showPayFields(true);
			showBillDetails(false);
			if(theForm.searchVisitId.value!=""){
				populateDepositRewardPointsDetails();
				onChangeDepositSetOff();
				onChangeRewardPoints();
			}
			if (gIsReturn)
				displayButton('ReturnAndPrint');
			else
				displayButton('PayAndPrint');
		}
	}
	if(!gIsInsuranceBill) {
		var table = document.getElementById("medList");
		for (var i=1;i<=getNumItems();i++) {
			var row = table.rows[i];
			var medicineId = getElementByName(row,"medicineId").value;
			var insuranceFlagImg = row.cells[ITEM_COL].getElementsByTagName("img")[0];
			insuranceFlagImg.src = cpath + "/images/empty_flag.gif";
			setHiddenValue(row, 'cat_payable', 'f');
			setHiddenValue(row, 'priIncludeInClaim', 'N');
			setHiddenValue(row, 'secIncludeInClaim', 'N');
			setHiddenValue(row, 'priClaimTaxAmt', 0);
			setHiddenValue(row, 'secClaimTaxAmt', 0);
		}
	} else {
		var table = document.getElementById("medList");
		for (var i=1;i<=getNumItems();i++) {
			var row = table.rows[i];
			var insurance_cat_payable = getElementByName(row,"insurance_cat_payable").value;
			if(insurance_cat_payable === 't') {
				var insuranceFlagImg = row.cells[ITEM_COL].getElementsByTagName("img")[0];
				insuranceFlagImg.src = cpath + "/images/empty_flag.gif";
			}
			setHiddenValue(row, 'cat_payable', insurance_cat_payable);
		}
	}

	if (!gIsReturn && gIsInsuranceBill) {
		// go through all medicines and see if they are all claimable. If not, warn.
		var table = document.getElementById("medList");
		for (var i=1;i<=getNumItems();i++) {
			var row = table.rows[i];
			var medicineId = getElementByName(row,"medicineId").value;
			var batch = gMedicineBatches[medicineId][0];
			if (!batch.claimable) {
				var msg=getString("js.sales.issues.warning.theitem");
				msg +=" ";
				msg += batch.medicine_name;
				msg +=" ";
				msg +=getString("js.sales.issues.isnotclaimable");
				msg +="\n";
				msg +=getString("js.sales.issues.deletetheitem.donotwant.addthistothebill");
				alert(msg);
			}
		}
		gDoRoundOff = false;
	}

	if (gIsReturn && returnType == 'ROAOB') {
		// returns: rate plan changes will not affect the rates, they are dependent on sales only
		if (getNumItems() > 0 && insuranceChanged)
			reCalcAllRowAmounts();

	} else {
		if (storeRatePlanChanged)
			gMedicineBatches = {};		// discard any cached medicine rates. Need to refetch.

		if (getNumItems() > 0) {
			if (storeRatePlanChanged) {
				// kick off a fetch for the new item rates: that will initiate the recalc of all row amounts
				refreshMedicineBatches();
			} else if (billDPstatus) {
				applyItemDiscounts();
			} else if (insuranceChanged) {
				reCalcAllRowAmounts();
			}
		}
	}

	if (origRoundOff != gDoRoundOff) {
		setTotals();
	}

	showItemTablePatientAndClaimAmounts(gIsInsuranceBill);
	populateDepositRewardPointsDetails();
	resetPayments();
	// Insurance 3.0 changes if bill type change

	var rowObj = gRowUnderEdit;
	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	if (!storeRatePlanChanged) {
		calculateTaxes();

		reCalcAllRowAmounts();
	}
	if(gIsInsuranceBill && !gIsReturn && !isPbmPresc){
		onClickProcessIns('salesform');
	}
	if(!storeRatePlanChanged)
		clearDataGridInfo(insuranceChanged, gIsOnload);
	gIsOnload = false;
	gOnChangeBillType = false;
	gTaxMap = '';
	filterPaymentModes();
}

function isNewBill() {
	var billno = theForm.billType.value;
	return (billno == 'BN-I' || billno == 'BN' || billno == 'BL');
}

function getStoreRatePlanId(ratePlanId) {
	var org = getRatePlanDetails(ratePlanId);
	if (org == null || org.store_rate_plan_id == null)
		return 0;
	return org.store_rate_plan_id;
}

function setDiscAuth(ratePlanId) {
	setSelectedIndex(theForm.discountAuthName,'');
	var salesType = getRadioSelection(theForm.salesType);

	if(salesType == 'hospital' && theForm.searchVisitId.value != '') {
		var org = getRatePlanDetails(ratePlanId);
		if(null != org)
			theForm.ratePlanDiscount.value = org.pharmacy_discount_percentage;
		if(ratePlanId != null && org.pharmacy_discount_percentage != 0 && null != org.pharmacy_discount_percentage) {
			setSelectedIndex(theForm.discountAuthName,-1);
		}
	}
}

function selectDiscountAuth() {
	var salesType = getRadioSelection(theForm.salesType);
		if (salesType == 'hospital' && theForm.searchVisitId.value != '') {
			if(theForm.discountAuthName.value == -1 && theForm.ratePlanDiscount.value == 0) {
				var msg=getString("js.sales.issues.donotselectdiscountauth.rateplandiscount");
				msg+="\n";
				msg+=getString("js.sales.issues.sinceratepl.countis0inmaster");
				alert(msg);
				setSelectedIndex(theForm.discountAuthName,'');
			}
		} else {
			if(salesType == 'retail' || salesType == 'retailCredit') {
				if(theForm.discountAuthName.value == -1) {
					var msg=getString("js.sales.issues.donotselectdiscountauth.rateplandiscount");
					msg+="\n ";
					msg+=getString("js.sales.issues.forretail.retailcreditpatienttype");
					alert(msg);
					setSelectedIndex(theForm.discountAuthName,'');
				}
			}
		}
}

/*
 * Refresh the medicine batches cache, and recalculate all row amounts.
 * Called when the rate plan changes, thus forcing the old rates to be invalid.
 */
function refreshMedicineBatches() {
	// for all medicines in the grid, fetch the new batch details (including the selling price, which
	// depends on the rate plan. Called when the rate plan changes.
	var medicineParams = "";
	var table = document.getElementById("medList");
	for (var i=1;i<=getNumItems();i++) {
		var row = table.rows[i];
		var medicineId = getElementByName(row, 'medicineId').value;
		medicineParams += "&medicineId=" + medicineId;
	}

	var storeId = theForm.phStore.value;
	var planId = theForm.planId.value;
	var visitType = theForm.visitType.value;

	var includeZeroStock = 'Y';
	if (!gIsReturn && !gIsEstimate) {
		// don't fetch zero stock if stock negative sale is disallowed.
		if (gStockNegative == 'D')
			includeZeroStock = 'N';
	}

	var url="MedicineSalesAjax.do?method=getStockJSON&output=map" +
		"&deptId="+storeId+"&planId="+planId+"&visitType="+visitType +
		"&includeZeroStock="+includeZeroStock+"&storeRatePlanId="+gStoreRatePlanId +
		medicineParams;

	YAHOO.util.Connect.asyncRequest('GET', url,
			{success: onRefreshBatchesResponse, failure: {}});
}

function onRefreshBatchesResponse(response) {

	if (response.responseText == null)
		return;

	// the response is already in the form of gMedicineBatches, just replace it.
	gMedicineBatches = eval('(' + response.responseText + ')');
	// eval("gMedicineBatches = " + response.responseText);

	// set the mrp as the new selling price from eatch medicine-batch.
	var table = document.getElementById("medList");
	for (var i=1;i<=getNumItems();i++) {
		var row = table.rows[i];
		var medicineId = getElementByName(row, 'medicineId').value;
		var batchNo = getElementByName(row, 'batchNo').value;
		var batch = getBatch(medicineId, batchNo);
		setHiddenValue(row, 'pkgmrp', batch.selling_price);
		setHiddenValue(row, 'visit_selling_price', batch.visit_selling_expr);
		setHiddenValue(row, 'store_selling_price', batch.store_selling_expr);
		setHiddenValue(row, "origRate" , batch.store_selling_expr);
		setSellingPrice(batch.selling_price,i);
	}
	clearDataGridInfo(true, gIsOnload);
	gIsOnload = false;
	// recalculcate all row amounts based on new rates.
	calculateTaxes();
	reCalcAllRowAmounts();
}


function showItemTablePatientAndClaimAmounts(isInsurance) {
	var table = document.getElementById('medList');
	var numRows = table.rows.length;
	for (var i = 0; i < numRows; i++) {
		if (isInsurance) {
			table.rows[i].cells[PAT_AMT_COL].style.display = '';
			table.rows[i].cells[PAT_TAX_AMT_COL].style.display = '';
			table.rows[i].cells[PRIM_CLAIM_AMT_COL].style.display = '';
			table.rows[i].cells[PRIM_CLAIM_TAX_AMT_COL].style.display = '';
			table.rows[i].cells[PRIM_PRE_AUTH_NO_COL].style.display = '';
			document.getElementById('prim_preAuthRow').style.display = '';
			//multi-payer
			document.getElementById('sec_preAuthRow').style.display = hasMoreThanOnePlan() ? '' : 'none';
			table.rows[i].cells[SEC_CLAIM_AMT_COL].style.display = hasMoreThanOnePlan() ? '' : 'none';
			table.rows[i].cells[SEC_CLAIM_TAX_AMT_COL].style.display = hasMoreThanOnePlan() ? '' : 'none';
			table.rows[i].cells[SEC_PRE_AUTH_NO_COL].style.display = hasMoreThanOnePlan() ? '' : 'none';
		} else {
			table.rows[i].cells[PAT_AMT_COL].style.display = 'none';
			table.rows[i].cells[PAT_TAX_AMT_COL].style.display = 'none';
			table.rows[i].cells[PRIM_CLAIM_AMT_COL].style.display = 'none';
			table.rows[i].cells[PRIM_CLAIM_TAX_AMT_COL].style.display = 'none';
			table.rows[i].cells[PRIM_PRE_AUTH_NO_COL].style.display = 'none';
			document.getElementById('prim_preAuthRow').style.display = 'none';
			//multi-payer
			document.getElementById('sec_preAuthRow').style.display = 'none';
			table.rows[i].cells[SEC_CLAIM_AMT_COL].style.display = 'none';
			table.rows[i].cells[SEC_CLAIM_TAX_AMT_COL].style.display = 'none';
			table.rows[i].cells[SEC_PRE_AUTH_NO_COL].style.display = 'none';
		}
	}
}

function reCalcAllRowAmounts(){
	var table = document.getElementById("medList");
	for (var i=1;i<=getNumItems();i++) {
		var row = table.rows[i];
		reCalcRowAmounts(row, false,true);
	}
	setTotals();
}

var saveButtonLabels = {
	'ReturnAndPrint' : 'Return &amp; <u><b>P</b></u>rint',
	'AddToBill' : 'Add To Bill &amp; <u><b>P</b></u>rint',
	'RaiseBill' : 'Raise Bill &amp; <u><b>P</b></u>rint',
	'Estimate' : '<u><b>P</b></u>rint Estimate',
	'PayAndPrint' : '<u><b>P</b></u>ay &amp; Print'
};

function displayButton(buttonid) {
	var button = document.getElementById('PayAndPrint');
		button.innerHTML = saveButtonLabels[buttonid];
}

function onChangeReturnBillNo() {
	clearCustomerDetails();
	clearRetailCreditDetails();
	clearPatientDetails();
	deleteRows();

	var billNo = theForm.rbillNo.value;
	var url = "./MedicineSalesAjax.do?method=getPatientDetailsBill&saleId="+billNo;

	YAHOO.util.Connect.asyncRequest('GET', url,
			{success: onBillDetailsResponse, failure: onInvalidBillDetails});

	return null;
}

function onBillDetailsResponse(response) {

	if (response.responseText == null) {
		gPatientType = '';
		showMessage("js.sales.issues.billno.doesnotexist");
		return;
	}

	// set the global patient info variable to the received response
	eval("gPatientInfo = " + response.responseText);

	if (gPatientInfo == null) {
		gPatientType = '';
		showMessage("js.sales.issues.billno.doesnotexist");
		return;
	}

	if (gPatientInfo.store != theForm.phStore.value) {
		gPatientType = '';
		showMessage("js.sales.issues.billnotissuedfromstore.cannotreturn");
		return;
	}

	if (gPatientInfo.sale.returned_against_visit == 'Y') {
		gPatientType = '';
		var msg=getString("js.sales.issues.billhasbeenused.returnagainstthevisit");
		msg+="\n" ;
		msg+=getString("js.sales.issues.notusethisbilldirectly.returnagainst");
		alert(msg);
		return;
	}
	if (returnValidDays != null && returnValidDays != '' && !ignoreItemReturnValidityDays) {
		if (daysDiff(getDatePart(gPatientInfo.sale.sale_date), getDatePart(getServerDate())) > returnValidDays) {
			showMessage("js.sales.issues.billistooold.return");
			return;
		}
	}

	if (gPatientInfo.retail == 'N') {
		gPatientType = 'hospital';
		setPatientDetails(gPatientInfo.patientDetails);

	} else {
		if (gPatientInfo.retailDetails.is_credit == 'Y')
			gPatientType = 'retailCredit';
		else
			gPatientType = 'retail';
		setRetailPatientDetails(gPatientInfo);
	}
	showPatientType();
	setBillOptions();
	populateDepositRewardPointsDetails();
	setTotals();
	// save the value for backend to know the return patient type
	theForm.returnPatientType.value = gPatientType;

	var items = gPatientInfo.soldItemList;
	deleteRows();

	if (items.length == 0 ) {
		return false;
	}

	if (items[0].saleBasis == 'M'){
		document.getElementById("mrpPriceSale").checked = true;
		document.getElementById("costPriceSale").checked = false;
	} else if (items[0].saleBasis == 'C') {
		document.getElementById("mrpPriceSale").checked = false;
		document.getElementById("costPriceSale").checked = true;
	}

	salesType = getRadioSelection(theForm.salesType);

	setSoldItemsList(items);

	// show the Add dialog automatically.
	openItemSearchDialog(document.getElementById('addButton'));
}

/*
 * Set up the list of available medicine names and associated batch nos.
 */
function setSoldItemsList(items) {
	gBilledItems = new Array();
	gMedicineBatches = {};
	var j = 0;
	if (gPatientInfo.isTPA == null && items.length > 0) {
		// if returning against patient, isTPA and billType are not fetched. We need
		// to find this out based on each item's isTPA and bill type, which can be mixed type also.
		gPatientInfo.isTPA = items[0].is_tpa;
		gPatientInfo.billType = items[0].bill_type;
	}
	for (var i=0; i<items.length; i++) {
		var item = items[i];

		if (item.qty <= 0)		// ignore if quantity is not there
			continue;

		var name = item.medicine_name;
		var medicineId = item.medicine_id;
		gMedicineNameIds[name] = medicineId;		// save name=>id mapping

		var batches = gMedicineBatches[medicineId];
		if (batches == null) {
			batches = new Array();
			gMedicineBatches[medicineId] = batches;
			// save the medicine name in list of medicines, only first time we see the name
			gBilledItems[j++] = {
				medicine_id: medicineId,
				medicine_name : name,
				item_code : item.item_code,
				item_barcode_id : item.item_barcode_id,
				issue_units : item.issue_units,
				package_uom : item.master_package_uom,
				cust_item_code_with_name : item.cust_item_code_with_name,
				cust_item_code_barcode_with_name: item.cust_item_code_barcode_with_name
			}
		}
		// see if this batch is already there, if so, just add the qty. Otherwise,
		// push a new batch into the list of medicine's batches available.
		var existingclaimAmts = new Array();
		var existingclaimTaxAmts = new Array();
		var existing = findInList(batches, 'batch_no', item.batch_no);
		var isOriginalClaimAmountExist = true;
		if (existing) {
			existing.qty += item.qty;
			existing.insurance_claim_amt += item.insurance_claim_amt;
			existingclaimAmts = existing.claimAmts;
			existingclaimTaxAmts = existing.claimTaxAmts;
			var salesClaimList = (gPatientInfo.visit_sold_items_claim_details === undefined) ? [] : filterList(
				gPatientInfo.visit_sold_items_claim_details,'sale_item_id',item.sale_item_id);
			var claimAmts = new Array();
			var claimTaxAmts = new Array();
			//multi payer
			for(var k = 0;k<salesClaimList.length;k++){
				if(isNotNullObj(salesClaimList[k].org_insurance_claim_amount)) {
					claimAmts.push(existingclaimAmts[k]+parseFloat(salesClaimList[k].org_insurance_claim_amount));
				} else {
					isOriginalClaimAmountExist = false;
					claimAmts.push(existingclaimAmts[k]+parseFloat(salesClaimList[k].insurance_claim_amt));
				}
				if(salesClaimList[k] && salesClaimList[k].tax_amt && salesClaimList[k].tax_amt != null) {
					if(existingclaimTaxAmts && existingclaimTaxAmts[k] && existingclaimTaxAmts[k] != null) {
						if(salesClaimList[k] && salesClaimList[k].tax_amt && salesClaimList[k].tax_amt != null) {
							claimTaxAmts.push(existingclaimTaxAmts[k]+parseFloat(salesClaimList[k].tax_amt));
						} else {
							claimTaxAmts.push(existingclaimTaxAmts[k]);
						}
					} else {
						claimTaxAmts.push(salesClaimList[k].tax_amt);
					}
				}
			}
			if(!isOriginalClaimAmountExist) {
				existing.sale_qty += parseFloat(item.qty);
			} else {
				existing.sale_qty += item.sale_qty;
			}

			if(salesClaimList.length == 0)
				claimAmts.push(existing.insurance_claim_amt);
			existing.claimAmts = claimAmts;
			existing.claimTaxAmts = claimTaxAmts;
			existing.mrp = Math.min(existing.mrp, item.mrp);
			if (item.discount_per > existing.discount_per) {
				existing.discount_per = item.discount_per;
				existing.discount_type = item.discount_type;
			}
			existing.package_cp = Math.min(existing.package_cp, item.package_cp);
		} else {
			var claimAmts = new Array();
			var claimTaxAmts = new Array();
			var salesClaimList = filterList(gPatientInfo.visit_sold_items_claim_details,'sale_item_id',item.sale_item_id);
			if ( salesClaimList != null ) {
				for(var k = 0;k<salesClaimList.length;k++){
					if(isNotNullObj(salesClaimList[k].org_insurance_claim_amount)) {
						claimAmts.push(salesClaimList[k].org_insurance_claim_amount);
					} else {
						isOriginalClaimAmountExist = false;
						claimAmts.push(salesClaimList[k].insurance_claim_amt);
					}

					if(salesClaimList[k].tax_amt && salesClaimList[k].tax_amt != null ) {
						claimTaxAmts.push(salesClaimList[k].tax_amt);
					}
				}
				if(salesClaimList.length == 0)
					claimAmts.push(item.insurance_claim_amt);
			}
			items[i].claimAmts = claimAmts;
			items[i].claimTaxAmts = claimTaxAmts;
			if(!isOriginalClaimAmountExist) {
				items[i].sale_qty = items[i].qty;
			}
			batches.push(items[i]);
		}
		// get the billType (P/C/M) and isTPA (true/false/null) flags for multiple bills.
		if (gPatientInfo.isTPA != null && item.is_tpa != gPatientInfo.isTPA)
			gPatientInfo.isTPA = null;			// null indicates mixed insurance types
		if (gPatientInfo.billType != 'M' && item.bill_type != gPatientInfo.billType)
			gPatientInfo.billType = 'M';		// M indicates mixed billTypes
	}

	// initialize the medicine auto complete based on new set of medicine names
	initMedicineAutoComplete();
}

function isEmpty(obj) { for(var i in obj) { return false; } return true; }
/*
 * Fill the UOM options for a medicine: this will include
 * the distinct package UOMs across all batches.
 */
function setMedicineUOMOptions(obj, medicineName) {

	var med = findInList(itemNamesArray, 'medicine_name', medicineName);
	if ( med != null ) {
		// fill item issue units and package uom
		var uomOptList = [{uom_name: med.issue_units, uom_value: ''}];
		if ( med.issue_units != med.package_uom) {
			uomOptList.push({uom_name: med.package_uom, uom_value: med.package_uom});
		}
		loadSelectBox(obj, uomOptList, 'uom_name', 'uom_value');
		obj.selectedIndex = (uomOptList.length > 1) && (gStoreSaleUnit == 'P') ? 1 : 0;
	}
}

function onInvalidBillDetails() {
	showMessage("js.sales.issues.billno.doesnotexist");
	return;
}

function onSelectPatient() {
	var visitId = theForm.searchVisitId.value;
	if (visitId == "") {
		// user cleared or typed nothing in the visitId, but tabbed out. Clear, but
		// proceed to the next field normally (return true)
		clearPatientDetails();
		return true;
	}

	if (gIsReturn && returnType == 'ROAOB') {
		getPatientDetails(visitId, true);
	} else {
		getPatientDetails(visitId, false);
	}
	return true;
}

function getPatientDetails(visitId, includeItemsSold) {
	var storeId = theForm.phStore.value;
	var pbmPrescId = theForm.pbm_presc_id.value;
	var patientIndentNo = theForm.patient_indent_no_param.value;
	var saleType="";
	if (!gIsReturn) {
		saleType = gStockNegative;
    } else {
		saleType = "A";
	}

	var url = "./MedicineSalesAjax.do?method=getPatientDetails&visitId="+visitId
		+"&storeId="+storeId+"&saleType="+saleType+"&get_prescriptions=true&get_indents="+getAutoFillIndents();
	if (includeItemsSold) {
		url = url + "&includeSoldItems=Y";
	}

	url = url + ( !empty(pbmPrescId) && pbmPrescId != 0 ? "&pbm_presc_id="+pbmPrescId : "" );
	url = url + ( !empty(patientIndentNo) ? "&patient_indent_no="+patientIndentNo : "" );

	YAHOO.util.Connect.asyncRequest('GET', url, {success: onPatientDetailsResponse, failure: onInvalidMrno});

	return null;
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

/*
 * Response handler for patient details get ajax call
 */
 var patientDetailsPlanDetails = null;
function onPatientDetailsResponse(response) {

	// set the global patient info variable to the received response
	eval("gPatientInfo = " + response.responseText);

	var blockVisit = false;

	if ( gPatientInfo != null ){
		if (blockIp == 'I' && (gPatientInfo.patientDetails.visit_type == 'i' && gPatientInfo.patientDetails.visit_status == 'I')) {
			clearPatientDetails();
			blockVisit= true;
		}
		if (blockIp == 'O' && (gPatientInfo.patientDetails.visit_type == 'o' && gPatientInfo.patientDetails.visit_status == 'I')) {
			clearPatientDetails();
			blockVisit = true;
		}
		if (blockIp == 'B' && (gPatientInfo.patientDetails.visit_status == 'I')) {
			clearPatientDetails();
			blockVisit = true;
		}

		// ip deposit
		if (ipDepositExists && gPatientInfo.ipdeposit){
			ipDepositSetOff = gPatientInfo.ipdeposit.total_ip_set_offs;
			ipDeposits = gPatientInfo.ipdeposit.total_ip_deposits;
			showIpDesposit = true;
		} else {
			showIpDesposit = false;
		}

		// reward points
		if (gPatientInfo.rewardpoints && gPatientInfo.rewardpoints.total_points_earned > 0){
			hasRewardPointsEligibility = true;
		} else {
			hasRewardPointsEligibility = false;
		}

		// general deposit
		if(gPatientInfo.deposit) {
			generalDepositSetOff = gPatientInfo.deposit.total_deposit_set_off;
			availableDeposits = gPatientInfo.deposit.total_deposits - gPatientInfo.deposit.total_deposit_set_off ;
		}

		isMvvPackage  = 0;
		visitType = gPatientInfo.patientDetails.visit_type;


		addDepositPaymentModesinDropdown();
		filterPaymentModes();
	}

	if (gPatientInfo == null || blockVisit) {
		var visitId = theForm.searchVisitId.value;
		// invalid patient: clear and stay in the search box.
		var msg=getString("js.sales.issues.visitid");
		msg+=" ";
		msg+= visitId;
		msg+=" ";
		if ( blockVisit) {
			msg+=getString("js.sales.issues.notrefertoanactive.inactivepatient.visitid");
		} else{
			msg+=getString("js.sales.issues.notrefertoanactive.inactivepatient");
		}
		alert(msg);
		onInvalidMrno();
		return;
	}

	setReturnIndentItems(gPatientInfo.returnIndentItems);
	deleteRows();
	setPatientDetails(gPatientInfo.patientDetails);

	var prescId = 0;
	var pbmRequestType = "";
	var prescApprStatus = "";
	if (!empty(gPatientInfo.pbmPrescDetails)) {
		var pbmdet = gPatientInfo.pbmPrescDetails;
		prescId = empty(pbmdet.pbm_presc_id) ? 0 : pbmdet.pbm_presc_id;
		pbmRequestType = empty(pbmdet.pbm_request_type) ? "" : pbmdet.pbm_request_type;
		prescApprStatus = empty(pbmdet.approval_status) ? "" : pbmdet.approval_status;
	}

	if (prescApprStatus == 'P' || prescApprStatus == 'R') {
		var msg=getString("js.sales.issues.thispatienthaspartially.fullydeniedpbmprescription");
		msg+="\n ";
		msg+=getString("js.sales.issues.checkthemedicinesbeforesale");
		alert(msg);
	}

	if (prescId != 0 && (trim(prescApprStatus) == '' && pbmRequestType == 'Authorization')) {
		var ok = confirm(" This patient requires Prior Authorization.\n "+
						"Please get Prior Authorization from Insurance company/TPA.");
		if (!ok)
			return false;
	}

	if (gPatientInfo.PBMPriorAuthRequired == 'Y') {
		var ok = confirm(" This patient requires Prior Authorization.\n "+
						"Please get Prior Authorization from Insurance company/TPA. \n "+
						"Click on Add New PBM Prescription link below.");
		if (!ok)
			return false;
	}

	setRetiredEligibilitylink();
	populateDepositRewardPointsDetails();

	/*
	 * If patient has one or more prescriptions, use that to initialize the list
	 */
	if (gIsReturn) {
		if (returnType == 'ROAOB') {
			setSoldItemsList(gPatientInfo.soldItemList);
		}
		setBillOptions();// setSoldItemsList can affect the bill options
		if ( gPatientInfo.indentsList != undefined && gPatientInfo.indentsList.length ==  0 )//no add option for visit with pending indent returns
			openItemSearchDialog(document.getElementById('addButton'));

		if ( gPatientInfo.indentsList != undefined && gPatientInfo.indentsList.length > 0){
			var soldItems = gPatientInfo.soldItemsGrpByMedIdMap;
			// we get a map of medicine batches from the patient info itself.
			if (gPatientInfo.soldItemsGrpByMedIdMap != null) {
				for(var i = 0;i<soldItems.length;i++){
				 	var medId = soldItems[i].medicine_id;
					gMedicineBatches[medId] = new Array(soldItems[i]);
				}
			} else {
				gMedicineBatches = {};
			}

			if (gPatientInfo.patIndentDetails.length > 0){

				var table = document.getElementById('medList');

				var numItems = table.rows.length;
				for (var i=1; i<numItems-1; i++) {
					table.deleteRow(1);
				}

				addMedicinesFromIndents(gPatientInfo.patIndentDetails);
				qtyEditable = !gIsReturn;
				getThisCell(document.getElementById("addButton")).style.visibility = gIsReturn  ? 'hidden' : 'visible';//return indents alone
			}
			var table = document.getElementById('prescInfo');

			var numItems = table.rows.length;
			if ( gPatientInfo.indentsList.length > 0 ) {
				for (var i=1; i<numItems-1; i++) {		// append indents after prescriptions
					table.deleteRow(1);
				}
			}
			var numItems = table.rows.length;

			for (var i = 0; i<gPatientInfo.indentsList.length; i++){
				var templateRow = table.rows[numItems-1];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);

				// doctor name in first column
				setNodeText(row.cells[0], gPatientInfo.indentsList[i].patient_indent_no);

				// url to view presc in second col
				if (gPatientInfo.indentsList[i].indent_type == 'R') {
					var url = cpath + "/stores/PatientIndentViewReturn.do?_method=view&stop_doctor_orders=true";
					url += "&patient_indent_no="+ gPatientInfo.indentsList[i].patient_indent_no;
					url +="&patient_id="+ gPatientInfo.indentsList[i].visit_id;
				} else {
					var url = cpath + "/stores/PatientIndentView.do?_method=view&stop_doctor_orders=true";
					url += "&patient_indent_no="+ gPatientInfo.indentsList[i].patient_indent_no;
				}
				var anch = row.cells[1].getElementsByTagName("A")[0];
				anch.href = url;

				var headerRow = table.rows[0];
				headerRow.cells[2].style.display = 'none';
				row.cells[2].style.display = "none"//returns should dispense all indent items

				setHiddenValue(row.cells[2], "patientIndentNoRef", gPatientInfo.indentsList[i].patient_indent_no);
			}

			document.getElementById('prescDetailsDiv').style.display="block";
			showDiv = true;

			if (showDiv) {
				document.getElementById('prescFieldSet').style.display = 'block';
			} else {
				document.getElementById('prescFieldSet').style.display = 'none';
			}
			if (!showDiv) {
				document.getElementById('prescDetailsDiv').style.display="none";
				openItemSearchDialog(document.getElementById('addButton'));
			}

	}

		return;
	}

	setBillOptions();

	if (getAutoFillPrescription()&& gPatientInfo.prescriptions_exists) {

		// we get a map of medicine batches from the patient info itself.
		if (gPatientInfo.medBatches != null) {
			gMedicineBatches = gPatientInfo.medBatches;
		} else {
			gMedicineBatches = {};
		}

		if (gPatientInfo.presDetails.length > 0){
			addMedicinesFromPrescription(gPatientInfo.presDetails);
		}

		var showDiv = false;
		if (gPatientInfo.consultantList.length > 0) {
			var table = document.getElementById('prescInfo');

			var numItems = table.rows.length;
			for (var i=1; i<numItems-1; i++) {		// leave the heading (first) and template (last)
				table.deleteRow(1);
			}
			for (var i = 0; i<gPatientInfo.consultantList.length; i++){
				var templateRow = table.rows[1];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);

				// doctor name in first column
				setNodeText(row.cells[0], gPatientInfo.consultantList[i].doctor_name);

				// For Outside patient consultation id is 0.
				if (gPatientInfo.consultantList[i].consultation_id != 0) {
					// url to view presc in second col
					var url = cpath + "/print/printPresConsultation.json?";
					url += "consultation_id="+ gPatientInfo.consultantList[i].consultation_id;
					url +="&printerId="+dischargePrinterId;
					var anch = row.cells[1].getElementsByTagName("A")[0];
					anch.href = url;
				}else {
					var anch = row.cells[1].innerHTML = "";
				}
				// third col already has the dropdown. Only needs hidden value for consultation Id
				setHiddenValue(row, "consultationId", gPatientInfo.consultantList[i].consultation_id);
			}

			document.getElementById('prescDetailsDiv').style.display="block";
			document.getElementById("patientDoctor").value=gPatientInfo.consultantList[0].doctor_name;
			showDiv = true;

		}

		if (showDiv) {
			document.getElementById('prescFieldSet').style.display = 'block';
		} else {
			document.getElementById('prescFieldSet').style.display = 'none';
		}
		if (!showDiv) {
			document.getElementById('prescDetailsDiv').style.display="none";
			openItemSearchDialog(document.getElementById('addButton'));
		}

		var unavbl='';
		var unavblMedicinesTab = document.getElementById('unavblMedicines');

		var numItems1 = unavblMedicinesTab.rows.length;
		for (var i=0; i<numItems1; i++) {
			unavblMedicinesTab.deleteRow(0);
		}

	}
	if ( !empty(gPatientInfo.indentsList) && gPatientInfo.indentsList.length > 0 ){

			// we get a map of medicine batches from the patient info itself.
			if (gPatientInfo.medBatches != null) {
				gMedicineBatches = gPatientInfo.medBatches;
			} else {
				gMedicineBatches = {};
			}

			if (gPatientInfo.patIndentDetails.length > 0){

				var table = document.getElementById('medList');

				var numItems = table.rows.length;
				if ( gPatientInfo.consultantList.length == 0 ) {
					for (var i=1; i<numItems-1; i++) {
						table.deleteRow(1);
					}
				}

				addMedicinesFromIndents(gPatientInfo.patIndentDetails);
				qtyEditable = !gIsReturn;
				getThisCell(document.getElementById("addButton")).style.visibility = gIsReturn  ? 'hidden' : 'visible';//return indents alone
			}
			table = document.getElementById('prescInfo');

			numItems = table.rows.length;
			if ( gPatientInfo.consultantList.length == 0 ) {
				for (var i=1; i<numItems-1; i++) {		// append indents after prescriptions
					table.deleteRow(1);
				}
			}
			var numItems = table.rows.length;

			for (var i = 0; i<gPatientInfo.indentsList.length; i++){
				var templateRow = table.rows[numItems-1];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);

				// doctor name in first column
				setNodeText(row.cells[0], gPatientInfo.indentsList[i].patient_indent_no +" / "+ (gPatientInfo.indentsList[i].prescribing_doctor_name != null ? gPatientInfo.indentsList[i].prescribing_doctor_name : "") );

				// url to view presc in second col
				var url = cpath + "/stores/PatientIndentView.do?_method=view&stop_doctor_orders=true";
				url += "&patient_indent_no="+ gPatientInfo.indentsList[i].patient_indent_no;
				var anch = row.cells[1].getElementsByTagName("A")[0];
				anch.href = url;

				setHiddenValue(row.cells[2], "patientIndentNoRef", gPatientInfo.indentsList[i].patient_indent_no);
			}

			document.getElementById('prescDetailsDiv').style.display="block";
			showDiv = true;

			if (showDiv) {
				document.getElementById('prescFieldSet').style.display = 'block';
			} else {
				document.getElementById('prescFieldSet').style.display = 'none';
			}
			if (!showDiv) {
				document.getElementById('prescDetailsDiv').style.display="none";
				openItemSearchDialog(document.getElementById('addButton'));
			}

	}
	// Added For Discharge Medication Prescription
	if (getAutoFillPrescription()&& gPatientInfo.dischargeMedication_exists) {

		// we get a map of medicine batches from the patient info itself.
		if (gPatientInfo.medBatches != null) {
			gMedicineBatches = gPatientInfo.medBatches;
		} else {
			gMedicineBatches = {};
		}

		if (gPatientInfo.dischargeMedicationDetails.length > 0){
			addMedicinesFromPrescription(gPatientInfo.dischargeMedicationDetails);
		}

		var showDiv = false;
		if (gPatientInfo.dischargeMedicationList.length > 0) {
			var table = document.getElementById('prescInfo');

			var numItems = table.rows.length;
			if ( gPatientInfo.indentsList.length == 0 && gPatientInfo.consultantList.length == 0 ) {
				for (var i=1; i<numItems-1; i++) {		// append indents after prescriptions
					table.deleteRow(1);
				}
			}
			numItems = table.rows.length;
			for (var i = 0; i<gPatientInfo.dischargeMedicationList.length; i++){
				var templateRow = table.rows[numItems-1];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);

				// doctor name in first column
				setNodeText(row.cells[0], gPatientInfo.dischargeMedicationList[i].doctor_name);

				// For Outside patient consultation id is 0.
				if (gPatientInfo.dischargeMedicationList[i].medication_id != 0) {
					// url to view presc in second col
					var url = cpath + "/pages/dischargeMedicationPrint.do?_method=dischargeMedicationPrint"
					+"&patient_id="+patientVisitId
					+"&printerId="+dischargePrinterId;
					var anch = row.cells[1].getElementsByTagName("A")[0];
					anch.href = url;
				}else {
					var anch = row.cells[1].innerHTML = "";
				}
				// third col already has the dropdown. Only needs hidden value for consultation Id
				setHiddenValue(row, "dischargeId", gPatientInfo.dischargeMedicationList[i].medication_id);
			}

			document.getElementById('prescDetailsDiv').style.display="block";
			document.getElementById("patientDoctor").value=gPatientInfo.dischargeMedicationList[0].doctor_name;
			showDiv = true;

		}
		if (showDiv) {
			document.getElementById('prescFieldSet').style.display = 'block';
		} else {
			document.getElementById('prescFieldSet').style.display = 'none';
		}
		if (!showDiv) {
			document.getElementById('prescDetailsDiv').style.display="none";
			openItemSearchDialog(document.getElementById('addButton'));
		}

		var unavbl='';
		var unavblMedicinesTab = document.getElementById('unavblMedicines');

		var numItems1 = unavblMedicinesTab.rows.length;
		for (var i=0; i<numItems1; i++) {
			unavblMedicinesTab.deleteRow(0);
		}

	}
	if (getAutoFillPrescription() &&  gPatientInfo.dischargeMedication_exists && (gPatientInfo.prescriptions_exists || gPatientInfo.indentsList.length > 0)) {
		if (gPatientInfo.dischargeMedication_exists && gPatientInfo.presDetails.length > 0){
			alert(getString("js.sales.issues.dischargemedicationprescription"));
		}
		if (gPatientInfo.dischargeMedication_exists && gPatientInfo.patIndentDetails.length > 0){
			alert(getString("js.sales.issues.dischargemedicationindent"));
		}
	}

	if ( empty(gPatientInfo.indentsList) && !gPatientInfo.prescriptions_exists && !gPatientInfo.dischargeMedication_exists ){
		document.getElementById('prescFieldSet').style.display = "none";
		document.getElementById('prescDetailsDiv').style.display="none";
		openItemSearchDialog(document.getElementById('addButton'));
	}

	var rowObj = gRowUnderEdit;
	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	//Insurance3.0 : calculates the sponsor amount of sales items when doing sales through indents
	//(i.e. when page is redirecting from indents screen to sales screen.)
	if(gIsInsuranceBill && !gIsReturn && !isPbmPresc)
		onClickProcessIns('salesform');
}


/*
 * Take action when the user has typed an invalid visitId
 */
function onInvalidMrno() {
	clearPatientDetails();
	theForm.searchVisitId.value = "";
	// a timeout is required so that the alert before this does not
	// cause yet another onblur event to interfere with the normal process.
	setTimeout("theForm.searchVisitId.focus()", 0);
}
function setReturnIndentItems(returnIndentItems) {
	if(!empty(returnIndentItems))
		returnIndentItemsJSON = returnIndentItems;
}

/*
 * When the medicine name changes, go and fetch the other details like
 * manufacturer name, batch names available, and available quantity.
 */
function onSelectMedicine(oSelf, oArgs) {

	var medicineName = '';
	if ( oArgs != undefined ){
		medicineName = oArgs[2][1];
	} else {
		medicineName = theForm.medicine.value;
	}
	resetMedicineDetails();

	if (medicineName == "")
		return false;

	var med = findInList(itemNamesArray, 'medicine_name', medicineName);
	if (med == null) {
		showMessage("js.sales.issues.nostockavailable.thisitemintheselectedstore");
		return false;
	}

	var medicineId = med.medicine_id;
	setMedicineUOMOptions(theForm.sale_unit,medicineName);

	if (gIsReturn && (returnType == 'ROAOB')) {
		// no need to fetch the batches available, we already have all the batches allowed
		// from the bills against which the return is being made.
		var medicineId = gMedicineNameIds[medicineName];
		var medBatches = gMedicineBatches[medicineId];
		setSelectedMedicineDetails(medBatches);
		initBatchAutoComplete('batch_no', 'batch_dropdown', medBatches,
				onSelectReturnBatch, true, theForm.sale_unit);

	} else {
		// fetch the batches and corresponding qty available in stock
		var storeId = theForm.phStore.value;
		var saleType="";
		var planId = theForm.planId.value;
		var visitType = theForm.visitType.value;

		var includeZeroStock = 'Y';
		if (!gIsReturn && !gIsEstimate) {
			// don't fetch zero stock if stock negative sale is disallowed.
			if (gStockNegative == 'D')
				includeZeroStock = 'N';
		}

		var ajaxReqObject = newXMLHttpRequest();

		var url="MedicineSalesAjax.do?method=getStockJSON" +
			"&medicineId="+medicineId +
			"&deptId="+storeId+"&planId="+planId+"&visitType="+visitType +
			"&includeZeroStock="+includeZeroStock+"&storeRatePlanId="+gStoreRatePlanId;

		gMedicineAjaxInProgress = true;
		getResponseHandlerText(ajaxReqObject, handleMedicineStockResponse, url);
	}

	if (gIsReturn) {
		// we make them pick the batch: focus to batch_no is done in handleMedicineStockResponse.
		// doing it here interferes with initialization of the autocomp (Bug 24413)
	} else {
		// we ask for quantity and auto pick batches based on expiry
		setTimeout("theForm.addQty.focus()", 100);
	}
	theForm.medicine.value = medicineName;
	return true;
}

/*
 * The following is called when batch is selected in the Add dialog during adding
 * items for a return.
 */
function onSelectReturnBatch() {
	var batch = getBatch(theForm.addMedicineId.value, theForm.batch_no.value);
	setTimeout("theForm.addQty.focus()", 100);
}

/*
 * The following is called on selecting a medicine in the Add dialog box.
 * For returns, this is called as soon as the medicine is selected. For sales,
 * this is called as part of the ajax handler that handles the stock response.
 */
function setSelectedMedicineDetails(batches) {
	var item = batches[0];
	storeItemControlType = item.control_type_name;
	var manfname = item.manf_name.length > 16 ? item.manf_name.substring(0, 16) : item.manf_name;
	if(item.manf_name.length > 16){
		manfname = manfname+"..";
	}

	document.getElementById('genericName').value = item.generic_name;
	document.getElementById('orderDialogItemCode').innerHTML = item.item_code;
	document.getElementById('genLabel').innerHTML = (item.generic_name == null || item.generic_name == '') ? '' : item.generic_name;
	document.getElementById('category').innerHTML = item.category;
	//document.getElementById('manfName').innerHTML = item.manf_mnemonic;
	document.getElementById('manfName').innerHTML = manfname;
	document.getElementById('manfName').title = item.manf_name;
	document.getElementById('packageType').innerHTML = item.package_type;
	document.getElementById('d_consumption_uom').value = item.consumption_uom;
	document.getElementById('barCodeId').innerHTML = item.item_barcode_id;

	theForm.addMedicineId.value = item.medicine_id;
	theForm.itemIdentification.value = item.identification;
	//if(gIsInsuranceBill) {
		var catPayableInfo = getCatPayableStatus(item.medicine_id, false);
		if(catPayableInfo!= null && catPayableInfo != undefined ) {
			var calimbaleInfo = catPayableInfo.plan_category_payable != null? catPayableInfo.plan_category_payable:"";
			var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";
			setNodeText('priCategoryPayable', priCatPayable == null ? "" : priCatPayable, null,priCatPayable == null ? "" : priCatPayable);

			if(calimbaleInfo == 'f') {
				document.getElementById('coverdbyinsurance').innerHTML = "No";
				document.getElementById('coverdbyinsurance').style.color = "red";
			} else {
				document.getElementById('coverdbyinsurance').innerHTML = "Yes";
				document.getElementById('coverdbyinsurance').style.color = "#666666";
			}
			document.getElementById('coverdbyinsuranceflag').value = calimbaleInfo;
		}
	//}

	if ((gIsReturn && returnType != 'ROAOB') || gIsEstimate) {
		// quantity does not make any sense, don't show anything.
		document.getElementById('medAvblQty').innerHTML = '--';

	} else {
		/*
		 * Show the total qty of all batches put together, this shows:
		 *   sales: the total stock
		 *   return roaob: the total qty in bill
		 * In both cases, the units are in the store's preferred units. If preferred
		 * is package, differing package UOMs can cause confusion since 1 pack of 10
		 * + 1 pack of 15 will count as 2 packs. We may need to add units as well.
		 *
		 * In case of return, we could have shown the original sale unit, but this can
		 * cause confusion. It is anyway unlikely that the sale unit is different from
		 * the store's preference.
		 */
		var totalMedQty = 0;
		for (var i=0; i<batches.length; i++) {
			if (gStoreSaleUnit == 'P') {
				totalMedQty += batches[i].qty/batches[i].issue_base_unit;
			} else {
				totalMedQty += batches[i].qty;
			}
		}
		document.getElementById('medAvblQty').innerHTML =
			(gStoreSaleUnit == 'P') ? totalMedQty.toFixed(2) : totalMedQty;
	}
	//also set the insurance amts if available
}

function mapTaxSubGroup(taxSubGroup) {
	var itemTaxSubgroup = [] ;
	var ratePlanStoreTariffTaxSubgroup = [] ;
	var storeTariffTaxSubgroup = [] ;

	for(var i=0;i<taxSubGroup.length;i++) {
		if(null != taxSubGroup[i].item_subgroup_id) {
			itemTaxSubgroup[i] = {
					"item_group_id": taxSubGroup[i].item_subgroup_id,
					"item_group_name": taxSubGroup[i].item_group_name,
					"item_group_type_id": taxSubGroup[i].item_group_type_id,
					"item_group_type_name": taxSubGroup[i].item_group_type_name,
					"item_subgroup_id": taxSubGroup[i].item_subgroup_id,
					"item_subgroup_name": taxSubGroup[i].item_subgroup_name
				};
		}
		if(null != taxSubGroup[i].rate_plan_tariff_item_group_id) {
			ratePlanStoreTariffTaxSubgroup[i] = {
					"item_group_id": taxSubGroup[i].rate_plan_tariff_item_group_id,
					"item_group_name": taxSubGroup[i].rate_plan_tariff_group_name,
					"item_group_type_id": taxSubGroup[i].item_group_type_id,
					"item_group_type_name": taxSubGroup[i].item_group_type_name,
					"item_subgroup_id": taxSubGroup[i].rate_plan_tariff_subgroup_id,
					"item_subgroup_name": taxSubGroup[i].rate_plan_tariff_subgroup_name
				};
		}
		if(null != taxSubGroup[i].tariff_item_group_id) {
			storeTariffTaxSubgroup[i] = {
					"item_group_id": taxSubGroup[i].tariff_item_group_id,
					"item_group_name": taxSubGroup[i].tariff_group_name,
					"item_group_type_id": taxSubGroup[i].item_group_type_id,
					"item_group_type_name": taxSubGroup[i].item_group_type_name,
					"item_subgroup_id": taxSubGroup[i].tariff_subgroup_id,
					"item_subgroup_name": taxSubGroup[i].tariff_subgroup_name
				};
		}
	}
	if(ratePlanStoreTariffTaxSubgroup.length >0) {
		return ratePlanStoreTariffTaxSubgroup;
	} else if (storeTariffTaxSubgroup.length >0) {
		return storeTariffTaxSubgroup;
	} else {
		return itemTaxSubgroup
	}
}

/*
 * Response handler for the ajax call to retrieve medicine details like mfr and batches
 */
var storeItemControlType='';
function handleMedicineStockResponse(responseText) {

	if (responseText==null) return;
	if (responseText=="") return;
	document.getElementById("PayAndPrint").disabled = true;
	showLoader();

	var medBatches;
    eval("itemDetails = " + responseText);			// response is an array of item batches and tax sub groups
    theForm.itemIdentification.value = '';
    var medBatches = itemDetails.batch;
    var taxSubGroups = mapTaxSubGroup(itemDetails.subgroups);
    var emptyStk = false;
	if (medBatches.length > 0) {
		var medicineId = medBatches[0].medicine_id;
		gMedicineBatches[medicineId] = medBatches;
		setSelectedMedicineDetails(medBatches);

		var routeIds = medBatches[0].route_id.split(",");
		var routeNames = medBatches[0].route_name.split(",");
		var medicine_route_el = document.getElementById('d_medicine_route');
		medicine_route_el.length = 1; // clear the previously populated list
		var len = 1;
		for (var i=0; i<routeIds.length; i++) {
			if (routeIds[i].trim() != '') {
				medicine_route_el.length = len+1;
				medicine_route_el.options[len].value = routeIds[i].trim();
				medicine_route_el.options[len].text = routeNames[i];
				len++;
			}
		}
		medicine_route_el.selectedIndex = medicine_route_el.length == 2 ? 1 : 0;
		document.getElementById('d_consumption_uom_label').textContent = medBatches[0].consumption_uom;

		if(!gIsReturn) {
			// Added for edit tax subgroups
			for(var key in gItemTaxGroups) {
				if (gItemTaxGroups.hasOwnProperty(key)) {
					//Set subgroup dropdown.
					for(var subkey in gItemTaxSubGroups) {
						if (gItemTaxSubGroups.hasOwnProperty(subkey)) {
							for(var i=0; i<gItemTaxSubGroups[subkey].length ; i++) {
								if(gItemTaxSubGroups[subkey][i].item_group_id == gItemTaxGroups[key].item_group_id) {
									for(var j=0; j<taxSubGroups.length ; j++) {
										if(taxSubGroups[j].item_subgroup_id == gItemTaxSubGroups[subkey][i].item_subgroup_id) {
											document.getElementById('ad_taxsubgroupid'+gItemTaxGroups[key].item_group_id).value = taxSubGroups[j].item_subgroup_id;
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}

	} else {
		if (gIsReturn) {
			// can only happen if there are no sold items for a serial identified item
			var msg=getString("js.sales.issues.noreturnablestock.therequesteditem");
			msg+=theForm.medicine.value ;
			msg+= "\n";
			msg+=getString("js.sales.issues.ifitisaserialidentifieditem.atleastonesalemusthavedone");
			alert(msg);
			// todo: if it is a serial item, then, ensure there is 0 stock
		} else {
			var msg=getString("js.sales.issues.noreturnablestock.therequesteditem");
			msg+=theForm.medicine.value;
			alert(msg);
			emptyStk = true;
		}
		theForm.medicine.value = "";
		resetMedicineDetails();
	}
	if (gIsReturn){
		var allBatches = gMedicineBatches[theForm.addMedicineId.value];
		initBatchAutoComplete('batch_no', 'batch_dropdown', allBatches, onSelectReturnBatch,
				true, theForm.sale_unit);
		setTimeout(function() {document.salesform.batch_no.focus()}, 100);
	}
	gMedicineAjaxInProgress = false;
	document.getElementById("PayAndPrint").disabled = false;
	hideLoader();
}

var customerId;//Global Declaration
var discount;//Global Declaration

function onAddMedicine() {
	var valid = true;

	if (gMedicineAjaxInProgress) {
		// make them click again till the stock details have been fetched
		return false;
	}

	valid = valid && validateRequired(theForm.medicine, getString("js.sales.issues.selectmedicinetoadd"));
	valid = valid && validateRequired(theForm.addQty, getString("js.sales.issues.enterquantity"));

	if (!isValidNumber(theForm.addQty, allowDecimalsForQty)) return false;

	var userQty = getAmount(theForm.addQty.value);
	if (userQty == 0) {
		showMessage("js.sales.issues.quantity.notzero");
		theForm.addQty.focus();
		return false;
	}

	var identification = theForm.itemIdentification.value;
	var medicineId = theForm.addMedicineId.value;

	if (gIsReturn) {
		var batch = getBatch(medicineId, theForm.batch_no.value);
		if (!validateRequired(theForm.batch_no, getString("js.sales.issues.batchnumber.required")))
			return false;
		var issueQty = (theForm.sale_unit.value == '') ? userQty : userQty * batch.issue_base_unit;

		valid = valid && validateDuplicateEntry(batch, null);
		if (returnType == 'ROAOB')
			valid = valid && validateReturnQty(batch, issueQty);
		if (!valid) {
			theForm.batch_no.focus();
			return false;
		}

		// chk removed for Estimate due to Bug#19008
		if (identification == 'S') {
			if (userQty > 1) {
				showMessage("js.sales.issues.quantityshouldbe1.serialitems");
				theForm.addQty.focus();
				return false;
			}
		}
	}

	if (identification == 'S') {
		if (!validateInteger (theForm.addQty,getString("js.sales.issues.quantity.snotbedecimal.serialitems")))
			return false;
	}


	/*if (hdrugAlertNeeded == 'Y' && storeItemControlType != 'Normal' && !gIsReturn) {
		var msg=getString("js.sales.issues.medicinecontroltype");
		msg+=storeItemControlType;
		msg+=getString("js.sales.issues.onlysoldwithaprescription");
		msg+="\n";
		msg+=getString("js.sales.issues.verifythepatientsprescription");
		alert(msg);
	}*/


	if (!gIsReturn && gIsInsuranceBill) {
		var claimable = gMedicineBatches[medicineId][0].claimable;
		var preAuthID = theForm.prior_auth_id.value;
		var preAuthModeID = theForm.prior_auth_mode_id.value;
		if (!claimable) {
			var ok = confirm("The item being sold is not claimable from TPA/Sponsor.\n" +
					"Are you sure you want to add this item to the sale?");
			if (!ok)
				return false;
		}

		var preAuthReqd = gMedicineBatches[medicineId][0].prior_auth_required;
		if (preAuthReqd  != 'N' && preAuthID == '') {
			if (preAuthReqd == 'A') {
				showMessage("js.sales.issues.enterpreauthnumber");
				theForm.prior_auth_id.focus();
				return false;
			} else {
				var ok = confirm("This item may need a pre auth number\n Check plan details for more details.");
				if (!ok)
					return false;
			}
		}
	}
	var duration = theForm.d_duration.value;
	var dosage = theForm.d_strength.value;
	var dosage_unit = theForm.d_consumption_uom.value;
	var frequency = theForm.d_frequency.value;
	var doctor_remarks = theForm.d_doc_remarks.value;
	var special_instr = theForm.d_special_instruction.value;
	var warning_label = theForm.warn_label.value;
	var other_remarks = theForm.d_remarks.value;
	var route = theForm.d_medicine_route.value;
	var routeName = theForm.d_medicine_route.options[theForm.d_medicine_route.selectedIndex].text;
	var duration_radio_els = document.getElementsByName('d_duration_units');
	var totalQty = theForm.d_qty.value;
	var calFlag = theForm.coverdbyinsuranceflag.value;
	//var d_per_day_qty = parseInt(document.getElementById("d_per_day_qty").value);
	var duration_unit;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_unit = duration_radio_els[k].value;
			break;
		}
	}
	var prescription = {};
	prescription = {
			duration:duration,
			durationUnit:duration_unit,
			dosage:dosage,
			dosage_unit:dosage_unit,
			frequency:frequency,
			doctorRemarks:doctor_remarks,
			special_instr:special_instr,
			warning_label:warning_label,
			other_remarks:other_remarks,
			route:route,
			routeName:routeName,
			totalQty:totalQty,
			consutationId: null,
			freqtotals:1,//added for insurance expire vaidation.
			allowedQuantity:totalQty,//added for insurance expire vaidation.
			calFlag:calFlag
	};
	/*}*/
	if(!gIsReturn) {
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				var taxGroupId = gItemTaxGroups[key].item_group_id;
				if(document.getElementById('ad_taxsubgroupid'+taxGroupId)) {
					var taxSubGroupId = document.getElementById('ad_taxsubgroupid'+taxGroupId).value;
					prescription['taxsubgroupid'+taxGroupId] = taxSubGroupId;
				}
			}
		}
	}

	if (gIsReturn) {
		addMedicineToGrid(medicineId, userQty, theForm.sale_unit.value, batch, prescription);

	} else {
		// auto pick the batches and add one or more batches.
		var msg = addMedicines(medicineId, userQty, theForm.sale_unit.value, false,prescription, false, true);
		if (msg != '')
			alert(msg);
	}
	if (hdrugAlertNeeded == 'Y' && storeItemControlType != 'Normal' && !gIsReturn && medicineAddedToGrid) {
		//if next item is expired item, so it is not added to the grid(addMedicineToGrid(...) method not called).
		// in this case we no need to show the alert. so we are making this boolean variable to default
		medicineAddedToGrid = false; //resetting to default
		var msg=alert(getString("js.sales.issues.medicinecontroltype")+storeItemControlType+getString("js.sales.issues.canonlybesold.prescription") +"\n"+
				getString("js.sales.issues.verifythepatientsprescription"));
			}

	clearItemSearchFields();
	itemSearchDialog.align("tr", "tl");
	setFocus ();
}

/*
 * Add a medicine: Only medicineId is supplied, ie, no batch is supplied. This will
 * pick one or more batches from the list of batches automatically based on certain rules.
 * units can be '' (indicates issue units) or it can be the name of the packageUOM.
 * If multiple package sizes exist, only the matching size will be used for selling.
 * Even when specifying issue units, we can force the sale to be in packages by passing
 * forcePackage = true. This is useful if the store is using Package Units, but the
 * we are using from prescription (which can only be in issue units)
 *
 * While adding medicines using autopick batch, we show warnings but without a confirmation.
 * If the user is not happy with our selection, they can delete the items from grid anyway.
 */

 function addMedicines(medicineId, userQty, units, forcePackage, presciption){
 	addMedicines(medicineId, userQty, units, forcePackage, presciption, null, null)
 }
function addMedicines(medicineId, userQty, units, forcePackage, presciption, indents, processInsurance) {
	var allBatches = gMedicineBatches[medicineId];
	var medicineName = allBatches[0].medicine_name;

	/*
	 * The batches excludes zero stock if preferences disallows it. But for expiry,
	 * Even if it is disallowed to sell, we bring the batch here in order to alert the
	 * user saying expired items are available, but we are not allowed to sell.
	 *
	 * Also, the batch list is already sorted for our convenience:
	 *  Availability (ie, qty > 0 comes first)
	 *    Expiry Date
	 *      Available Quantity
	 */
	
	var remQty = userQty;
	var expiredQty = 0; var nearingExpiryQty = 0;
	var negativeQty = 0; var diffPkgQty = 0;
	for (b=0; b<allBatches.length; b++) {

		var batch = allBatches[b];
		var avlblQty = 0;

		// if batch is already in grid, skip
	    var dupExists = getDuplicateIndex(batch, -1);
		if (dupExists != -1)
			continue;
		if (units == '' && !forcePackage) {
//			avlblQty = batch.qty;
			avlblQty = getAvailableQuantity(batch);
		} else {
			// use only whole packages
			avlblQty = Math.floor(batch.qty/batch.issue_base_unit);
		}


		// If user selected Package UOM, ensure we pick only batches that have the same package UOM
		if ((units != '') && (units != batch.master_package_uom)) {
			diffPkgQty += avlblQty
			continue;
		}

		// check for expiry date for normal sales (ie, not estimate, and not negative stock)
		if ((gTransaction != 'estimate') && (avlblQty > 0)) {
			var daysToExpire = batch.exp_dt != null ? getDaysToExpire(batch.exp_dt) : gExpiryWarnDays+1;
			if (daysToExpire <= 0) {
				expiredQty += avlblQty;
				if (gAllowExpiredSale != 'Y') {
					continue;
				}
			} else if (daysToExpire <= gExpiryWarnDays) {
				nearingExpiryQty += avlblQty;
			}
		}

		var qtyForBatch = 0;
		if ((gStockNegative != 'D') && (avlblQty <= 0 || (b == allBatches.length - 1))) {
			// if stock is allowed to be negative, use up all of remQty for the last batch
			// or for the first batch where it is already 0 or negative.
			qtyForBatch = remQty;
			negativeQty = qtyForBatch - avlblQty;
		} else {

			//If quantity is in package UOM, multiply with batch.issue_base_unit to get the quantity for batch
			if (presciption != null && !empty(presciption["user_unit"]) && presciption["user_unit"] == 'P')
				qtyForBatch = Math.min(remQty * batch.issue_base_unit, avlblQty)/batch.issue_base_unit;
			else
				qtyForBatch = Math.min(remQty, avlblQty);

			qtyForBatch = qtyForBatch < 0 ? 0 : qtyForBatch;   // stock can be negative even otherwise.
      var pbmPrescId = theForm.pbm_presc_id.value;
      var isPbmPresc = !empty(pbmPrescId) && pbmPrescId != 0;
      if (allowDecimalsForQty == 'Y') {
        // round it off to 2 decimal places to avoid float problems
        var decMultiplier = 100;
        if (isPbmPresc) {
          decMultiplier = 10000;
        }
        qtyForBatch = Math.round(qtyForBatch * decMultiplier) / decMultiplier;
      }
		}
		batch.qtyForBatch = qtyForBatch;
		// add to grid
		if (qtyForBatch > 0)
			if (!empty(returnIndentItemsJSON) && Object.getOwnPropertyNames(returnIndentItemsJSON).length != 0) {
					var batchId = batch.item_batch_id;
					if (!empty(returnIndentItemsJSON[batchId])) {
						var dispenseStatus = returnIndentItemsJSON[batchId][0].dispense_status;
						if(dispenseStatus != 'C') {
							addMedicineToGrid(medicineId, qtyForBatch, units, batch, presciption, indents, processInsurance);
						} else {
							qtyForBatch = 0;
						}
					} else {
						qtyForBatch = 0;
					}
				} else {
					addMedicineToGrid(medicineId, qtyForBatch, units, batch, presciption, indents, processInsurance);
				}

		// if no more to be added finish up
    remQty = parseFloat((remQty - qtyForBatch).toFixed(4));
		if (remQty == 0)
			break;
	}

	var msg = "";
	if (remQty > 0) {
		msg += getString("js.sales.issues.storesuserissues.warning.insufficientquantity")+" '"+ medicineName + "': " + remQty.toFixed(2);
		if ((expiredQty > 0) && !gAllowExpiredSale) {
			msg += " (" + expiredQty + getString("js.sales.issues.storesuserissues.itemsavailable.pastexpirydate");
		}
		if (diffPkgQty > 0) {
			msg += " (" + diffPkgQty + getString("js.sales.issues.storesuserissues.itemsavailable.differentpackagesize");
		}
		msg += "\n";
	}

	if (negativeQty > 0 && gStockNegative != 'A') {
		msg += getString("js.sales.issues.storesuserissues.warning.insufficientquantity") +" '"+ medicineName + "': "
			+ negativeQty.toFixed(2);
		msg += " "+getString("js.sales.issues.storesuserissues.proceedingwithissue.causestockbecomenegative")+"\n";
	}

	if (nearingExpiryQty > 0) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor")+" " + medicineName +" "+ getString("js.sales.issues.storesuserissues.aresoontoexpire")+"\n";
	}

	if ((expiredQty > 0) && gAllowExpiredSale) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor")+" "+ medicineName +" "+ getString("js.sales.issues.storesuserissues.pastexpirydate");
	}

	return msg;
}

function getAvailableQuantity(batch) {
	var avlblQty =0;
	var batchId = batch.item_batch_id;
	if (!empty(returnIndentItemsJSON) && Object.getOwnPropertyNames(returnIndentItemsJSON).length != 0) {
		if (!empty(returnIndentItemsJSON[batchId])) {
				return returnIndentItemsJSON[batchId][0].qty_required - returnIndentItemsJSON[batchId][0].qty_received;
		}
	} else {
		if (!empty(batch)) {
			return batch.qty;
		}
	}
	return avlblQty;
}

function getBatch(medicineId, batchNo) {
	var allBatches = gMedicineBatches[medicineId];
	var batch = null;

	if (allBatches == null)		// allBatches is null for BillReturn (ROAOB)
		return null;

	for (var i = 0; i <allBatches.length; i++ ) {
		if ( (medicineId == allBatches[i].medicine_id) && (batchNo == allBatches[i].batch_no )) {
			var batch = allBatches[i];
			break;
		} else {
			continue;
		}
	}
	return batch;
}


// return the stock details of the item batch being added
function getAddingBatch() {
	var medicineId = theForm.addMedicineId.value;
	var allBatches = gMedicineBatches[medicineId];
	return allBatches[theForm.batch.selectedIndex-1];
}

/*
 * For convenience, trap the enter key on quantity and simulate an add
 */
function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		patientPrescription =false;
		onAddMedicine();
		return false;
	} else {
		return enterNumAndDot(e);
	}
}

function onChangeDiscountPer() {
    var valid = validateDiscountsPer();
	setTotals();
}

function onChangeDiscountAmt() {
	formatAmountObj(theForm.disAmt, true);
	var valid = validateDiscountsAmount();
	setTotals();
}

function onClickReset() {
	resetForm();
	return false;
}

function onChangeRoundOff() {
	setTotals();
}

function onChangeBillDiscountType() {

	var discType = theForm.discType.value;

	theForm.disPer.value = '';
	theForm.disAmt.value = '';

	if (discType == 'percent-inc' || discType == 'percent-exc') {
		theForm.disAmt.readOnly = true;
		theForm.disPer.readOnly = false;
	} else if (discType == 'amt') {
		theForm.disAmt.readOnly = false;
		theForm.disPer.readOnly = true;
	} else {
		theForm.disAmt.readOnly = true;
		theForm.disPer.readOnly = true;
	}
	setTotals();
}

function validateStockNegative(batch, issueQty, isPbmPresc) {
	// no check if preference is "allow", "warn" or is a return
	if (gIsReturn || gStockNegative != 'D')
		return true;
	
	if (isPbmPresc) {
		issueQty = Math.round(issueQty);
	}

	if (issueQty > batch.qty) {
		var msg = getString("js.sales.issues.insufficientstockforsaleof") +" "+ batch.medicine_name + getString("js.sales.issues.batch") +" "+ batch.batch_no + "). "
			+ getString("js.sales.issues.requested") +" "+ issueQty + getString("js.sales.issues.available")+" " + batch.qty + " " + batch.issue_units;
		alert(msg);
		return false;
	}
	return true;
}

function validateReturnQty(batch, issueQty) {

	if (issueQty > batch.qty) {
		var msg=getString("js.sales.issues.returnquantity.notbegreaterthanquantity.inoriginalbill");
		msg += batch.qty;
		msg += ")";
		alert(msg);
		return false;
	}
	return true;
}

function validateStockNegativeWarnings(batch, issueQty) {
	// no check for sales returns.
	if (gIsReturn || gTransaction == 'estimate' ) {
		return true;
	}

	// no check if preference is "allow" and sales.
	if (gStockNegative == 'A') {
		return true;
	}
    var availableQty = batch.qty;
    var qtyAfterSale = availableQty - issueQty;

	// check and warn negative stock
	if ((qtyAfterSale <0) && (gStockNegative == 'W')) {
		var msg = "Insufficient stock for sale of " + batch.medicine_name + "(Batch " + batch.batch_no + "). "
			"Requested: " + issueQty + "; Available: " + availableQty;
		var ok = confirm(msg + " Do you want to proceed?");
		if (!ok) return false;
	}
	return true;
}

function validateExpiry(batch) {
	// no check for sales returns
	if (gIsReturn || gTransaction == 'estimate' || batch.exp_dt == null)
		return true;

	var daysToExpire = getDaysToExpire(batch.exp_dt);

	if ((daysToExpire <= 0) && (gAllowExpiredSale != 'Y')) {
		alert("The item " + batch.medicine_name + "(Batch " + batch.batch_no + ") has passed expiry date. " +
				"\nThis item cannot be sold.");
		return false;
	}
	return true;
}

/*
 * Expiry warnings are shown only when the batch changes, and we don't need to warn
 * when saving. That's why this function is separated out from the previous one.
 */
function validateExpiryWarnings(batch) {
	// nothing to do if it is a sales return
	if (gIsReturn || gTransaction == 'estimate' || batch.exp_dt == null)
		return true;

	var daysToExpire = getDaysToExpire(batch.exp_dt);
	if ((daysToExpire <= 0) && (gAllowExpiredSale == 'Y')) {
		// expired, but allowed to sell.
		var ok = confirm("The item " + batch.medicine_name + "(Batch " + batch.batch_no + ") has passed " +
				" expiry date. \nAre you sure you want to sell this item batch?");
		if (!ok) return false;

	} else if (daysToExpire <= gExpiryWarnDays) {
		// going to expire soon
		var ok = confirm("The item " + batch.medicine_name + "(Batch " + batch.batch_no + ") is about " +
				" to expire in " + daysToExpire + " days.\nAre you sure you want to sell this item batch?");
		if (!ok) return false;
	}
	return true;
}

/*
 * Given expDt of a batch, get the number of days left for it to expire
 */
function getDaysToExpire(expDt) {
	// expDt in ms is first of the month _after_ which it is going to expire. Convert it to
	// the 1st of next month, the actual date when it is considered expired
	// Example: expDt = Sep 09, we store it as 1-Sep-09, but it is considered expired only
	// on or after 1 Oct 09.
	var dateOfExpiry = new Date(expDt);

	var diff = daysDiff(getDatePart(getServerDate()), dateOfExpiry);
	return diff;
}

function validateDuplicateEntry(batch, curRow) {
	var curRowIndex = (curRow != null) ? curRow.rowIndex : -1;
	var dupSlNo = getDuplicateIndex(batch, curRowIndex);
	if (dupSlNo != -1) {
		alert(getString("js.sales.issues.duplicateentry.theitem") + batch.medicine_name + " (batch " + batch.batch_no + ") "+
				getString("js.sales.issues.alreadyaddedtolist") + dupSlNo + ".\n\n" +
				getString("js.sales.issues.edittheexistingmedicine") +
				getString("js.sales.issues.choose.differentbatchnumber"));
		return false;
	}
	return true;
}

/*
 * Find if there is another entry of the same medicineId/Batch in the grid,
 * given the batch object and an index that is ourselves (so that we don't give a duplicate
 * error for ourselves
 */
function getDuplicateIndex(batch, selfIndex) {
	var numItems = getNumItems();
	var duplicateCount = 0;

	for (var i=1; i<=numItems; i++) {
		if (i == selfIndex)
			continue;

		var row = getRowObject(i);
		var itemMedicineId = getElementByName(row,'medicineId').value;
		var itemBatchNo = getElementByName(row,'batchNo').value;

		if ((itemMedicineId == batch.medicine_id) && (itemBatchNo == batch.batch_no))
			return i;
	}
	return -1;
}

function validateMedicineCount() {
	return (getNumItems() > 0);
}

/*
 * Handler called when a new batch is selected in the edit dialog
 */
function onSelectEditBatch() {

	var form = document.editForm;
	var medicineId = form.medicineId.value;
	var batchNo = form.batch_no.value;

	if (!validateBatchSel(medicineId, batchNo)) {
		form.batch_no.value = "";
		form.batch_no.focus();
		return false;
	}

	// set the cost price and MRP based on the selected batch.
	var batch = getBatch(medicineId, batchNo);
	form.cp.value = batch.package_cp;
	//form.mrp.value = batch.selling_price;
	//recalcEditDialogAmts(false);
	var rowObj = gRowUnderEdit;
	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	var itemBatchId = batch.item_batch_id;
	var sellingPrice = getSellingPrice(form.medicineId.value, itemBatchId,
			batch.visit_selling_expr, batch.store_selling_expr, batch.selling_price, form.saleqty.value);

	// Do not recalculate for a pbm prescription
	if (!isPbmPresc) {
		form.mrp.value = sellingPrice;
		recalcEditDialogAmts(false);
	}

	return true;
}

/*
 * Validate a new batch selected in the Edit dialog. Does expiry validations
 * as well as checks if there is an existing duplicate row in the table already for
 * the newly selected batch.
 *
 */
function validateBatchSel(medicineID, batchNo) {

	var batch = getBatch(medicineID, batchNo);
	var valid = true;
    valid = valid && validateDuplicateEntry(batch, gRowUnderEdit);
    valid = valid && validateExpiry(batch);
    valid = valid && validateExpiryWarnings(batch);

	/*
	 * validations that are in combination with quantity are done only on save of
	 * the dialog, not on select of the batch.
	 */
	return valid;
}


/*
 * Validates that the edited row is sane, and OK for submitting from the edit dialog.
 */
function validateEdits(rowObj) {
	var valid = true;
	var form = document.editForm;

	valid = valid && validateRequired(form.saleqty, getString("js.sales.issues.quantity.required"));
	if (!isValidNumberPBM(form.saleqty, allowDecimalsForQty)) return false;

	if (gTransaction == 'estimate')
		return true;

	/*
	 * Validations: validate all batch related stuff only on batch change. This includes
	 * Expiry warnings, insufficient quantity etc.
	 */
	valid = valid && validateRequired(form.batch_no, getString("js.sales.issues.batchnumber.required"));

	valid = valid && validateRequired(form.mrp, getString("js.sales.issues.mrp.required"));
	valid = valid && validateAmount(form.mrp, getString("js.sales.issues.mrp.adecimalnumber"));

	if (gIsInsuranceBill) {
		valid = valid && validateRequired(getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt'), getString("js.sales.issues.claimamount.required"));
		valid = valid && validateAmount(getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt'), getString("js.sales.issues.claim.adecimalnumber"));
	}
	var qty = getAmount(form.saleqty.value);
	if (qty <= 0) {
		showMessage("js.sales.issues.quantity.notbezeroorless");
		form.saleqty.focus();
		return false;
	}

	var batch = getBatch(form.medicineId.value, form.batch_no.value);
	var issueQty = (form.eSaleUnit.value == '') ? qty : qty * batch.issue_base_unit;
	
	var medPBMId = getElementByName(rowObj, 'medPBMPrescId').value;
	var isPbmPresc = (!empty(medPBMId) && medPBMId != 0);

	if (batch != null) {
		if (gIsReturn && returnType == "ROAOB") {
			valid = valid && validateReturnQty(batch, issueQty);
		} else if (!gIsReturn) {
			valid = valid && validateStockNegativeWarnings(batch, issueQty);
			valid = valid && validateStockNegative(batch, issueQty, isPbmPresc);
		}
		if (!valid) {
			form.saleqty.focus();
			return false;
		}
	} else {
		showMessage("js.sales.issues.invalidbatchselected");
		return false;
	}

	var origRate = getPaise(eval(getElementByName(rowObj,'origRate').value));
	var newRate =  getElementPaise(form.mrp);

	if (((gRoleId != 1) && (gRoleId != 2)) &&  (newRate < origRate) && (gAllowRateDecrease != 'A')) {
		var msg=getString("js.sales.issues.notauthorized.decreasetheratebelow");
		msg += formatAmountPaise(origRate);
		alert(msg);
		return false;
	}

	if (((gRoleId != 1) && (gRoleId != 2)) &&  (newRate > origRate) && (gAllowRateIncrease != 'A')) {
		var msg=getString("js.sales.issues.notauthorized.increasetherateabove");
		msg += formatAmountPaise(origRate);
		alert(msg);
		return false;
	}

	valid =  valid && validateDecimal(form.discountper, getString("js.sales.issues.discountpercentage.adecimalnumber"), 2);
	if (valid && (getAmount(form.discountper.value) > 100) ) {
		showMessage("js.sales.issues.discount.notbegreaterthan100");
		valid = false;
		form.discountper.focus();
	}

	if (!gIsReturn && gIsInsuranceBill) {
		var preAuthID = getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').value;
		if (gMedicineBatches[form.medicineId.value][0].prior_auth_required != 'N') {
			if ( preAuthID == '' ) {
				if ( gMedicineBatches[form.medicineId.value][0].prior_auth_required == 'A') {
					showMessage("js.sales.issues.enterpreauthnumber");
					getElementByName(document.getElementById("prim_pre_auth_row"),'editPrior_auth_id').focus();
					valid = false;
				}else {
					var ok = confirm("This item may need a pre auth number\n Check plan details for more details.");
					if (!ok)
						valid = false;
				}
			}
		}
	}

	var pbmStatusObj = document.getElementById("editPBM_status");
	var pbmStatustxt = pbmStatusObj != null ? pbmStatusObj.textContent : "";
	if (pbmStatustxt == 'Denied') {
		var claimPaise = getPaise(getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').value );
		if (claimPaise != 0) {
			showMessage("js.sales.issues.claimamountshouldbezero.denieditems");
			getElementByName(document.getElementById("prim_claim_row"),'edlg_claim_amt').focus();
			valid = false;
		}
	}

	return valid;
}

/*
 * Validate the list of medicines: checks whether there is at least one non-deleted medicines,
 * does not validate the row: assuming edit already validates it, and add does it sanely.
 */
function validateMedicineList() {
	var valid = true;
	valid = valid && validateMedicineCount();
	if (!valid) {
		showMessage("js.sales.issues.noitemtosave");
		return false;
	}
	return valid;
}

function validateBillType() {
	var billType = document.salesform.billType.value;
	if (billType == "") {
		showMessage("js.sales.issues.selectbilltype");
		return false;
	}

	return true;
}

/*
 * Validate that if refunding money from the counter, refund is allowed for the user
 */
function validateRefunds() {

	if (!gIsReturn)
		return true;

	// allow superusers
	if ((gRoleId == 1) || (gRoleId == 2))
		return true;

	// check only for bill now
	var billType = theForm.billType.value;
	if (billType != "BN")
		return true;

	// no check if no counter: this is a pending sales or bill later
	if (theForm.counterId.value == "")
		return true;

	if (gRefundRights != 'A') {
		showMessage("js.sales.issues.notauthorized.refundmoneyfromthecounter");
		return false;
	}

	return true;
}

function validateSalesType() {
	var salesType = getRadioSelection(theForm.salesType);
	if (salesType == 'retail') {
		return validateCustomer();
	} else if (salesType == 'hospital'){
		return validatePatient();
	} else if(salesType == 'retailCredit'){
	    return validateCreditCustmer();
	}
	return true;
}

function validateCustomer() {
	if (trimAll(theForm.custName.value) == '') {
		showMessage("js.sales.issues.customername.required");
		theForm.custName.value = '';
		theForm.custName.focus();
		return false;
	} else {
		theForm.custName.value = trimAll(theForm.custName.value);
	}

	if (!gIsReturn && trimAll(theForm.custDoctorName.value) == '') {
		showMessage("js.sales.issues.doctorname.required");
		theForm.custDoctorName.value = '';
		theForm.custDoctorName.focus();
		return false;
	} else {
		theForm.custDoctorName.value = trimAll(theForm.custDoctorName.value);
	}
	if (trimAll(theForm.retailPatientMobileNoField.value) != '') {
		theForm.retailPatientMobileNoField.value = trimAll(theForm.retailPatientMobileNoField.value);
	    if($("#cust_patient_phone_valid").val() == 'N') {
	    	alert(getString("js.sales.issues.mobileNumber.invalid"));
	    	theForm.retailPatientMobileNoField.focus();
	 	    return false;
	    }
	}
	return true;
}

function validateCreditCustmer() {
	if (trimAll(theForm.custRetailCreditName.value) == '') {
		showMessage("js.sales.issues.customername.required");
		theForm.custRetailCreditName.value = '';
		theForm.custRetailCreditName.focus();
		return false;
	} else theForm.custRetailCreditName.value = trimAll(theForm.custRetailCreditName.value);
	if (!gIsReturn && trimAll(theForm.custRetailCreditDocName.value) == '') {
		showMessage("js.sales.issues.doctorname.required");
		theForm.custRetailCreditDocName.value = '';
		theForm.custRetailCreditDocName.focus();
		return false;
	} else theForm.custRetailCreditDocName.value = trimAll(theForm.custRetailCreditDocName.value);
	  if (trimAll(theForm.custRCreditPhoneNoField.value) == '') {
		showMessage("js.sales.issues.phonenumber.required");
		theForm.custRCreditPhoneNoField.value = '';
		theForm.custRCreditPhoneNoField.focus();
		return false;
	} else {
		theForm.custRCreditPhoneNoField.value = trimAll(theForm.custRCreditPhoneNoField.value);

		validateMobileNumber();
	    if($("#cust_retail_patient_phone_valid").val() == 'N') {
	    	alert(getString("js.sales.issues.mobileNumber.invalid"));
	    	theForm.custRCreditPhoneNoField.focus();
	 	    return false;
	 	 }
	}
	if (trimAll(theForm.custRCreditLimit.value) == '') {
		showMessage("js.sales.issues.creditlimit.required");
		theForm.custRCreditLimit.value = '';
		theForm.custRCreditLimit.focus();
		return false;
	} else theForm.custRCreditLimit.value = trimAll(theForm.custRCreditLimit.value);

	var totalBillAmt = parseFloat(retailCredillBillAmt) + getPaiseReverse(gGrandTotalPaise);
	var valid = true;

	if ( totalBillAmt > parseFloat(theForm.custRCreditLimit.value) ) {
		valid = confirm("Bill Amount exceeds Credit Limit.\nDue Amt\t\t     :\t"+(retailCredillBillAmt).toFixed(decDigits)+"\nCurrent Sale Amt :\t"+ getPaiseReverse(gGrandTotalPaise).toFixed(decDigits) +"\nTotal Bill Amt\t     :\t"+(totalBillAmt).toFixed(decDigits)+"\nCredit Limit\t     :\t"+parseFloat(theForm.custRCreditLimit.value).toFixed(decDigits)+"\nProceed Anyway? ");
	}
	return valid;
}


function validatePatient() {
	var valid = true;
	valid = valid && validateRequired(theForm.visitId, getString("js.sales.issues.visitidisrequired.selectapatient"));
	if (!gIsReturn)
		valid = valid && validateRequired(theForm.patientDoctor, getString("js.sales.issues.doctorname.required"));
	return valid;
}

function validateCreditLimit(){
	var valid = validateAmount(theForm.custRCreditLimit, getString("js.sales.issues.creditlimit.eanumber"));
	if(!valid){
		theForm.custRCreditLimit.value="";
		theForm.custRCreditLimit.focus();
	}
	return valid;
}

function validateDiscountsPer(){
	var valid = true;
	valid =  valid && validateDecimal(theForm.disPer, getString("js.sales.issues.discount.adecimalnumber"));

	if (valid && (theForm.disPer.value > 100) ) {
		showMessage("js.sales.issues.discountpercentage.notbegreaterthan100");
		valid = false;
	}

	if (!valid) {
		theForm.disPer.value="";
		theForm.disPer.focus();
	}
	return valid;
}

function validateDiscountsAmount(){
	var valid = validateAmount(theForm.disAmt, getString("js.sales.issues.discountamount.adecimalnumber"));

	var disAmt = theForm.disAmt.value;
	// todo: this is not correct: grand Total already is discounted.
	if (valid && eval(parseFloat(disAmt) > getPaiseReverse(gGrandTotalPaise) )){
		showMessage("js.sales.issues.discountamount.notgreaterthangrandtotal");
		valid = false;
	}
	if (valid){
		theForm.disAmt.value = disAmt;
	}else {
		theForm.disAmt.value="";
		theForm.disAmt.focus();
	}

	return valid;
}

function validatePriorAuth() {
	var numItems = getNumItems();
	for (var i=1; i<=numItems; i++) {
		var rowObj = getRowObject(i);
		var priorAuthEle = getElementByName(rowObj,'preAuthId');
		var priorAuthModeEle = getElementByName(rowObj,'preAuthModeId');
		var medNameEle = getElementByName(rowObj,'medName');

		if (priorAuthEle != null && priorAuthModeEle != null) {
			var priorAuthValue = priorAuthEle.value;
			var priorAuthModeVal = priorAuthModeEle.value;

			var isPriorAuthEmpty = priorAuthValue == null || trim(priorAuthValue) == "";
			var isPriorAuthModeEmpty = priorAuthModeVal == null || trim(priorAuthModeVal) == "";

			if(!isPriorAuthEmpty && isPriorAuthModeEmpty) {
				var msg=getString("js.sales.issues.priorauthmode.required");
				msg+="\n";
				msg+=getString("js.sales.issues.selectthepriorauthmodefor");
				msg+="\n";
				msg+=medNameEle.value;
				alert(msg);
				return false;
			}

			if(isPriorAuthEmpty && !isPriorAuthModeEmpty) {
				setSelectedIndex(priorAuthModeEle, "");
			}
		}
	}
	return true;
}

function validateIPCreditLimit() {
	//Credit limit rule is applicable for IP visits only
	var visitType = theForm.visitType.value;
	// for billNow bills we need to pay full amount on the time of sale itself. so we no need to check for billNow bills
	// if store counter is mapped to user counter then only payment section will come. if not payment section will not come eventhough BN bill.
	//so user need to pay later in other counter. so in this case we need to check ip credit limit.
	var billType = theForm.billType.value;
	var phCounterId = document.salesform.counterId.value;
	if(visitType != 'i' || gIsReturn || (billType == 'BN' && phCounterId != null && phCounterId != '')
			|| (billType == 'BN-I' && phCounterId != null && phCounterId != '')
			|| gcreditLimitDetailsJSON == undefined || gcreditLimitDetailsJSON == null) {
		return true;
	}


	var newPatientAmt = 0;
	var newPatientAmtEl = document.getElementById('lblPatAmount');
	if(newPatientAmtEl != null) {
		newPatientAmt = newPatientAmtEl.textContent;
		if(newPatientAmt != null && newPatientAmt.trim() != '') {
			newPatientAmt = parseFloat(newPatientAmt.trim());
		}
	}

	var actualAvailableCreditLimit = gcreditLimitDetailsJSON.availableCreditLimit;
	var visitTotalNewPatDueAmt = formatAmountValue(newPatientAmt + gVisitPatientDue);
	var availableCreditLimit = gcreditLimitDetailsJSON.availableCreditLimitWithoutDue;
	availableCreditLimit = parseFloat(availableCreditLimit) - parseFloat(visitTotalNewPatDueAmt);

	if(ip_credit_limit_rule == 'B') {
		if(!(availableCreditLimit >= 0)) {
			var msg=getString("js.sales.issues.and.below.currentoutstanding");
			msg+=' '+ gVisitPatientDue;
			msg+="\n";
			msg+=getString("js.sales.issues.ipcreditlimitis");
			msg+=' '+ actualAvailableCreditLimit;
			alert(msg);
			return false;
		}
	} else if (ip_credit_limit_rule == 'W') {
		if(!(availableCreditLimit >= 0)) {
			var msg=getString("js.sales.issues.and.below.currentoutstanding");
			msg+=' '+ gVisitPatientDue;
			msg+="\n";
			msg+=getString("js.sales.issues.ipcreditlimitis");
			msg+=' '+ actualAvailableCreditLimit;
			msg+="\n";
			msg+=getString("js.sales.issues.doyouwanttoproceed");
			var ok = confirm(msg);
			if(!ok)
				return false;
		}
	}

	return true;
}

function validateAll() {
	var valid = true;
	valid = valid && validatePriorAuth();
	valid = valid && validateSalesType();
	valid = valid && checkRaiseBillAccess();
	valid = valid && validateBillType();
	valid = valid && validateRefunds();
	valid = valid && validateMedicineList();
	valid = valid && validatePayDates();
	valid = valid && validatePaymentRefund();
	valid = valid && validatePaymentTagFields();
	valid = valid && validateAllNumerics();
	valid = valid && validatePaymentAmount();
	valid = valid && checkDepositExistsAndNotUsed();
	var type = theForm.billType.value;
	if (income_tax_cash_limit_applicability == 'Y' && theForm.counterId.value != "" && (type == 'BN' || type == 'BN-I')){
		if (gPatientType != 'retail' && gPatientType != 'retailCredit'){
		var visitId = document.getElementsByName("visitId")[0].value;
		var mrno = document.getElementsByName("mrno")[0].value;
		valid = valid && checkCashLimitValidation(mrno,visitId);
		}else if (gPatientType == 'retail' || gPatientType == 'retailCredit'){
			valid = valid && checkRetailCashLimitValidation();
		}
	}
	if( document.salesform.payDate != undefined )
		valid = valid && validateSaleDate();

	if (!valid) return false;
	var totalAmountPaise = document.getElementById("grandTotal").innerHTML;
	var billType = theForm.billType.value;

	if(!gIsReturn && !gIsEstimate) {
		var billDiscount = document.getElementById("lblBillDiscount").innerHTML;
		var itemDiscount = document.getElementById("lblItemDiscounts").innerHTML;
		if((billDiscount != 0 || itemDiscount != 0 ) && theForm.discountAuthName.value == '') {
			showMessage("js.sales.issues.selectdiscountauth");
			return false;
		}
		if(billDiscount == 0 && itemDiscount == 0  && theForm.discountAuthName.value != '') {
			var cfrm = confirm("There is no bill level and item level discount,\n "+
				"So Discount Auth Selection is not needed.");
			setSelectedIndex(theForm.discountAuthName,'');
			if(!cfrm)
				return false;
		}
	}

	// Validate IP patient Credit Limit
	if (!validateIPCreditLimit()) {
		return false;
	}

	// bill later checks
	if ((billType != "BN") && (gPatientType!='retailCredit') && !gIsReturn) {
		var billNo = billType;
		var bill = null;
		for (var i=0; i<gPatientInfo.bills.length; i++) {
			if (gPatientInfo.bills[i].bill_no == billNo) {
				bill = gPatientInfo.bills[i];
				break;
			}
		}

		if (bill != null) {
			var balanceCreditAmount = getPaise(bill.approval_amount) + getPaise(bill.deposit_set_off)
			+ getPaise(bill.total_receipts) - getPaise(bill.total_amount);
		}
		if (totalAmountPaise > balanceCreditAmount) {
			var balanceDisplay = formatAmountValue(getPaiseReverse(balanceCreditAmount));
			var ok = confirm("Net advances/approved amounts in the bill (" +
					formatAmountValue(getPaiseReverse(balanceCreditAmount)) +
					") are not sufficient to include this sale\n" +
					"Do you want to continue to make the sale?");
			if (!ok)
				return false;
			return true;
		}
	}
	if(!doPaytmTransactions()){
		return false;
	}
	if (!validateGovernmentIdentifier()) return false;

	return true;
}

// prevent double click
var submitted = false;
function onClickSave() {
	if (submitted)
		return true;
	if (!validateAll())
		return false;
	if (isSharedLogIn == 'Y' && gTransaction != 'estimate')
		loginDialog.show();
	else {
		submitted = true;
		enableFormValues();
		theForm.submit();
		return true;
	}
}

function addOption(selectbox,text,value ) {
	var optn = document.createElement("OPTION");
	optn.text = text;
	optn.value = value;
	selectbox.options.add(optn);
}

function formatExpiry(dateMSecs) {
	if (dateMSecs == null) return '(---)';
	var dateObj = new Date(dateMSecs);
	var dateStr = formatDate(dateObj, 'monyyyy', '-');
	return dateStr;
}

function creditRetail(obj) {
    var billNo= document.forms[0].creditBillNo.value;
    obj.setAttribute("href",obj.href+"&billno="+billNo+"&discount="+discount+"&customerid="+customerId);
    return false;
}


function onManualAddMedicine() {
	if(!validatePriorAuthMode(getElementByName(document.getElementById("prim_preAuthRow"),'prior_auth_id'),
		getElementByName(document.getElementById("prim_preAuthRow"),'prior_auth_mode_id'),null, null)){
		return false;
	}

	if(hasMoreThanOnePlan && !validatePriorAuthMode(getElementByName(document.getElementById("sec_preAuthRow"),'sec_prior_auth_id'),
		getElementByName(document.getElementById("sec_preAuthRow"),'prior_auth_mode_id'),null, null)){
		return false;
	}
	if(!gIsInsuranceBill) {
		document.getElementById('coverdbyinsurancestatusid').style.display = 'none';
	} else {
		document.getElementById('coverdbyinsurancestatusid').style.display = 'block';
	}

	if (patientPrescription){
		var valid = true;

		if (validateMedicineCount()){
			valid = validateMedicineList();
		}
		if (valid){
			patientPrescription = false;
			onAddMedicine();
		}else{
			return false;
		}
	} else {
		onAddMedicine();
	}

	if(gIsInsuranceBill && !gIsReturn)
		onClickProcessIns('salesform');
}

function setReturnType() {

	var detailsDisabled = false;
	var billDisplay = 'none';
	var retailDisplay = '';

	if (returnType == "ROAOB") {
		detailsDisabled = true;
		billDisplay = '';
		retailDisplay = 'none';
		document.getElementById("patientType").innerHTML = 'Return Against:';
		document.salesform.itemDiscPer.disabled = true;
		document.salesform.itemDiscPerApply.disabled = true;
	} else {
		document.getElementById("patientType").innerHTML = 'Patient Type:';
		document.salesform.itemDiscPer.disabled = false;
		document.salesform.itemDiscPerApply.disabled = false;
	}

	theForm.custRetailCreditName.readOnly = detailsDisabled;
	theForm.custRetailCreditDocName.readOnly = detailsDisabled;
	theForm.custRetailSponsor.readOnly = detailsDisabled;
	theForm.custRCreditPhoneNoField.readOnly = detailsDisabled;
	theForm.custRCreditLimit.readOnly = detailsDisabled;

	theForm.custName.readOnly = detailsDisabled;
	theForm.custDoctorName.readOnly = detailsDisabled;

	if(regPref.nationality) {
		theForm.nationalityId.readOnly = detailsDisabled;
	}
	if (regPref.government_identifier_type_label) {
		theForm.identifierId.readOnly = detailsDisabled;
	}
	if (regPref.government_identifier_label) {
		theForm.governmentIdentifier.readOnly = detailsDisabled;
	}

	theForm.patientDoctor.readOnly = detailsDisabled;

	// show the Bill No and hide retail/retail credit
	document.getElementById("retailRow").style.display = retailDisplay;
	document.getElementById("retailCreditRow").style.display = retailDisplay;
	document.getElementById("returnBillRow").style.display = billDisplay;
}

function initSearchDialog(){
	clearFields();
	initGenericSearchDialog();
	initAutoNameSearch();
	initAutoGenericName();
}

function searchMedicine(){
		var dialogId = document.getElementById("dialogId").value;
		var medName = document.getElementById("medicine_name").value;
		var genericName = document.getElementById("generic_name").value;
		var storeId = document.getElementById("phStore").value;
		var saleType = gIsReturn ? "return" : "sale";
		if (medName == '' && genericName == ''){
			return false;
		}

		var ajaxReqObject = newXMLHttpRequest();
		var url="MedicineSalesAjax.do?method=getEquivalentMedicinesList&medicineName="+
		encodeURIComponent(medName)+"&storeId="+storeId+"&saleType="+saleType+"&genericName="+encodeURIComponent(genericName);

		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);

		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("var medDetails = "+ajaxReqObject.responseText);
				displaySearchResults(medDetails);
			}
		}
}

function displaySearchResults(medDetails){
	var results = document.getElementById("results");
	loadSelectBox(results, medDetails, "display_name", "medicine_name", "Select Item");
	if(results.options.length >0 ){
		for(var i = results.options.length - 1; i >= 0; i--){
			results.options[i].title = results.options[i].text;
		}
	}
	results.selectedIndex = 0;
}

function setMedicine() {
	var selectedMed = document.getElementById("results").value
	if (selectedMed == ''){
		showMessage("js.sales.issues.noitemselected");
		return false;
	}
	document.getElementById("medicine").value = selectedMed;
	var success = onSelectMedicine();
	if (success) {
		clearFields();
		genericSearchDialog.hide();
	}
}

function initGenericSearchDialog() {
	document.getElementById("genericSearchDialog").style.display = 'block';
	genericSearchDialog = new YAHOO.widget.Dialog("genericSearchDialog",
			{
				width:"510",
				context : ["searchGen", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	var escKeyListener = new YAHOO.util.KeyListener("genericSearchDialog", { keys:27 },
	                                              { fn:closeGenericSearchDialog} );
	genericSearchDialog.cfg.queueProperty("keylisteners", escKeyListener);
	genericSearchDialog.render();
}


function showSearchWindow(){
	var id = 1;
	var button = document.getElementById("searchGen");
	genericSearchDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	genericSearchDialog.show();
	theForm.medicine_name.focus();
}

function closeGenericSearchDialog() {
	genericSearchDialog.hide();
	// set focus to the parent's field
	setFocus ();
	return false;
}

var gen;

function initAutoNameSearch(){
	if (AutoComp != undefined) {
		AutoComp.destroy();
	}

	var dataSource = new YAHOO.util.XHRDataSource(cpath+"/pages/stores/MedicineSalesAjax.do");
    dataSource.scriptQueryAppend = "method=search";
    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

    dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "cust_item_code_with_name"},
					{key : "genericname"},
					{key : "medicinename"}
				 ]
	};

    var AutoComp = new YAHOO.widget.AutoComplete('medicine_name','medicinename_dropdown', dataSource);
    AutoComp.allowBrowserAutocomplete = false;
    AutoComp.prehighlightClassName = "yui-ac-prehighlight";
    AutoComp.typeAhead = false;
    AutoComp.useShadow = false;
    AutoComp.animVert = false;
    AutoComp.minQueryLength = 1;
    AutoComp.forceSelection = true;
    AutoComp.filterResults = Insta.queryMatchWordStartsWith;

	AutoComp.formatResult = Insta.autoHighlight;
	AutoComp.itemSelectEvent.subscribe(onSelectMedicineName = function (type, args){
		document.getElementById('generic_name').value = args[2][1];
		document.getElementById('medicine_name').value = args[2][2];
	});
}

function initAutoGenericName(){
	if (AutoComplete != undefined) {
		AutoComplete.destroy();
	}

	dataSource = new YAHOO.util.LocalDataSource({result : genericNames});
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "generic_name"} ]
		};

	//dataSource = new YAHOO.widget.DS_JSArray(genericNames);
	var AutoComplete = new YAHOO.widget.AutoComplete('generic_name','generic_name_dropdown', dataSource);

    AutoComplete.useShadow = true;
    AutoComplete.minQueryLength = 0;
    AutoComplete.allowBrowserAutocomplete = false;
    AutoComplete.filterResults = Insta.queryMatchWordStartsWith;
	AutoComplete.formatResult = Insta.autoHighlightWordBeginnings;
	AutoComplete.maxResultsDisplayed = 20;
	AutoComplete.itemSelectEvent.subscribe(onChangeGenericName = function (type, args){
		document.getElementById('medicine_name').value = '';
	});
}

function clearFields(){
	document.getElementById("medicine_name").value = '';
	document.getElementById("generic_name").value = '';
	clearGenericFields();
	var results = document.getElementById("results");
	results.length = 1;
}

function clearGenericFields(){
	document.getElementById('classification_name').innerHTML = '';
	document.getElementById('sub_classification_name').innerHTML = '';
	document.getElementById('standard_adult_dose').innerHTML = '';
	document.getElementById('criticality').innerHTML = '';
	document.getElementById('gen_generic_name').innerHTML = '';

}

function isEventEnter(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 ) {
		return true;
	}
	return false;
}

function nextFieldOnEnter(e) {
	if (isEventEnter(e)) {
		eval('document.salesform.' + nextfield + '.focus()');
	}
}

window.onfocus = function() {
	init();
}

function initItemSearchDialog() {
	document.getElementById("itemSearchDialog").style.display = 'block';

	itemSearchDialog = new YAHOO.widget.Dialog("itemSearchDialog",
	{
		width:"650px",
		context:["","tr","br"],
		constraintoviewport:true,
		modal:true,
		visible:false,

	});
	var escKeyListener = new YAHOO.util.KeyListener("itemSearchDialog", { keys:27 },
	                                              { fn:closeItemSearchDialog,
	                                                scope:itemSearchDialog,
	                                                correctScope:true } );
	itemSearchDialog.cfg.queueProperty("keylisteners", escKeyListener);
	itemSearchDialog.render();
}

function openItemSearchDialog(obj) {
	if (salesType == 'hospital' && theForm.mrno.value == '') {
		showMessage("js.sales.issues.selectapatient.beforeaddingitems");
		return;
	}

	if (returnType == 'ROAOB') {
		if (salesType == 'returnBill' && gPatientType == '') {
			showMessage("js.sales.issues.selectabill");
			return;
		}
		if (salesType == 'hospital' && theForm.mrno.value == '') {
			showMessage("js.sales.issues.selectapatient.beforeaddingitems");
			return;
		}
	}

	if(!gIsInsuranceBill) {
		document.getElementById('coverdbyinsurancestatusid').style.display = 'none';
		document.getElementById('coverdbyinsurance').innerHTML = '';
	} else {
		document.getElementById('coverdbyinsurancestatusid').style.display = 'block';
	}
	itemSearchDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);

	// clear everything for the new entry
	clearItemSearchFields();
	if(gIsReturn ){
		document.getElementById("managementAccordion").style.display="none";
		document.getElementById("presMang").style.display="none";
	}
	if(allowTaxEditRights != 'A' && !(gRoleId == 1 || gRoleId == 2)){
		var length = document.getElementsByName("subgroups").length;
		var i;
		for (i = 0; i <length; i++) {
		document.getElementsByName("subgroups")[i].disabled =true;
		}
	}
	itemSearchDialog.show();
	setFocus ();
}

function setFocus () {
	theForm.medicine.focus();
}

function clearItemSearchFields() {
	theForm.medicine.value = "";
	if (gIsReturn){
		theForm.batch_no.value = "";
	}
	 if(gTransaction){
		document.getElementById("presMang").style.display ="none";
		}
		else{
			document.getElementById("presEdit").style.display = "block";
	}
	resetMedicineDetails();
	document.getElementById("generic_name").value="";
	document.getElementById('genericName').value="";
	document.getElementById("orderDialogItemCode").innerHTML="";
	document.getElementById("genLabel").innerHTML="";
	if (document.getElementById("erx_activity_id"))
		document.getElementById("erx_activity_id").value="";
	document.getElementById("d_duration").value="";
	document.getElementsByName('d_duration_units')[0].checked = true;
	document.getElementById("d_strength").value="";
	document.getElementById("d_consumption_uom").disabled = true;
	document.getElementById("d_consumption_uom").value="";
	document.getElementById('d_consumption_uom_label').textContent = '';
	document.getElementById("d_frequency").value="";
	document.getElementById("d_qty").value="";
	document.getElementById("warn_label").selectedIndex=0;
	document.getElementsByName("warn_label")
	document.getElementById("d_doc_remarks").value="";
	document.getElementById("d_special_instruction").value="";
	document.getElementById("d_remarks").value="";
	document.getElementById("d_medicine_route").selectedIndex=0;
	document.getElementById("coverdbyinsurance").innerHTML = "";
	document.getElementById("coverdbyinsuranceflag").value = "";
	if(!gIsReturn) {
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				var taxGroupId = gItemTaxGroups[key].item_group_id;
				if(document.getElementById("ad_taxsubgroupid"+taxGroupId)) {
					document.getElementById("ad_taxsubgroupid"+taxGroupId).selectedIndex=0;
				}
				if(document.getElementById("ed_taxsubgroupid"+taxGroupId)) {
					document.getElementById("ed_taxsubgroupid"+taxGroupId).selectedIndex=0;
				}
			}
		}
	}
	clearGenericFields();
}

function closeItemSearchDialog() {
	clearFields();
	itemSearchDialog.hide();
}

function initGenericInfoDialog() {
	document.getElementById("genericInfoDialog").style.display = 'block';
	genericInfoDialog = new YAHOO.widget.Dialog("genericInfoDialog",
			{
				width:"500px",
				context : ["loadGenInfo", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
				visible: false
			} );
	var escKey = new YAHOO.util.KeyListener("genericInfoDialog", { keys:27 },
	                                              { fn:closeGenericInfoDialog } );
	genericInfoDialog.cfg.queueProperty("keylisteners", escKey);
	genericInfoDialog.render();
}

function checkstoreallocation() {
	if (gRoleId != 1 && gRoleId != 2) {
		var phStore = document.getElementById('phStore');
		// When PBM module is enabled, user stores are filtered based on store tariff.
		// So, even if user has multiStoreAccess, the store can be shown as label
		// if only one store with tariff exists.
		if (phStore != null) {
			if (multiStoreAccess && phStore.type == "select-one") {
				// here it is a select box.
				if (phStore.options.length == 0) {
					showMessage("js.sales.issues.noassignedstore.noraisebill.nocounter.donthaveanyaccesstothisscreen");
					document.getElementById("storecheck").style.display = 'none';
					return false;
				}
			} else {
				// here it is a hidden field
				if (phStore.value == '' || !isSalesAllowedStore()) {
					showMessage("js.sales.issues.noassignedstore.noraisebill.nocounter.donthaveanyaccesstothisscreen");
					document.getElementById("storecheck").style.display = 'none';
					return false;
				}
			}
		}
	}
	return true;
}

function checkRaiseBillAccess(){
	if (!allowedRaiseBill()) {
		showMessage("js.sales.issues.storenotallowed.raisebill");
		return false;
	}
	return true;
}

function allowedRaiseBill() {
	var storeId = document.getElementById("phStore").value;
	var billno = document.salesform.billType.options[document.salesform.billType.selectedIndex].value;
	var billnoText = document.salesform.billType.options[document.salesform.billType.selectedIndex].text;
	for (var i=0;i<jStores.length;i++) {
		if (jStores[i].DEPT_ID == storeId) {
			if ((billno == 'BN' || billno == 'BN-I' || billno == 'BL')
					&& (billnoText.indexOf('Raise Bill') != -1)
					&& jStores[i].ALLOWED_RAISE_BILL == 'N') return false;
			else return true;
		}
	}
	return true;
}

function isSalesAllowedStore() {
	var storeId = document.getElementById("phStore").value;
	for (var i=0;i<jStores.length;i++) {
		if (jStores[i].DEPT_ID == storeId) {
			if (jStores[i].IS_SALES_STORE == 'N') return false;
			else return true;
		}
	}
	return true;
}

function closeGenericInfoDialog() {
	genericInfoDialog.cancel();
	setFocus ();
}

function initItemEditDialog() {
	edititemdialog = new YAHOO.widget.Dialog("edititemdialog",
	{
		width:"700px",
		context:["","tr","br"],
		constraintoviewport:true,
		modal:true,
		visible:false
	});
	var escKeyListener = new YAHOO.util.KeyListener("edititemdialog", { keys:27 },
	                                              { fn:cancelEditDialogBox } );
	edititemdialog.cfg.queueProperty("keylisteners", escKeyListener);

	edititemdialog.cancelEvent.subscribe(onEditDialogCancel);
	edititemdialog.render();
}


/*
 * Response handler for the ajax call to retrieve generic details like classification and sub-classification
 */
function handleGenericResponse(responseText) {
	if (responseText==null) return;
	if (responseText=="") return;

	var genericDetails;
    eval("var genericDetails = " + responseText);			// response is an array of item batches

	if (genericDetails != null) {
		var genericId = genericDetails[0].GENERIC_CODE;
		document.getElementById('classification_name').innerHTML = decodeURIComponent(genericDetails[0].CLASSIFICATION_NAME);
		document.getElementById('sub_classification_name').innerHTML = decodeURIComponent(genericDetails[0].SUB_CLASSIFICATION_NAME);
		document.getElementById('standard_adult_dose').innerHTML = decodeURIComponent(genericDetails[0].STANDARD_ADULT_DOSE);
		document.getElementById('criticality').innerHTML = decodeURIComponent(genericDetails[0].CRITICALITY);
		document.getElementById('gen_generic_name').innerHTML = decodeURIComponent(genericDetails[0].GENERIC_NAME);

	}

}



function initGenericNameLoad() {
	var genericName = document.getElementById("genericName").value;
	if (genericName != "") {
		var ajaxReqObject = newXMLHttpRequest();
		var url="MedicineSalesAjax.do?method=getGenericJSON&genericName="+
		encodeURIComponent(genericName);

		getResponseHandlerText(ajaxReqObject, handleGenericResponse, url);
	}
}

function loadGenericWindow(){
	clearGenericFields();
	var id = 2;
	var button = document.getElementById("loadGenInfo");
	genericInfoDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	document.getElementById('gen_generic_name').textContent = document.getElementById('genericName').value;
	initGenericNameLoad();
	genericInfoDialog.show();
	theForm.genericInfoClose.focus();
}


/* This function will  populate patient deposit details
   if he have.
*/
var pharmacyDeposits=null;
var maxDeposit = 0;
var ipDeposits = 0;
var genDeposit = 0;
var ipDeposit = 0;
var ipDepositExists = false;
var pharmacyRewardPoints=null;
var maxPoints = 0;
function populateDepositRewardPointsDetails() {

	if ( !empty(gPatientInfo) ) {
		pharmacyDeposits = gPatientInfo.deposit;
		ipDeposits = gPatientInfo.ipdeposit;
		ipDepositExists = gPatientInfo.ipDepositExists;
		pharmacyRewardPoints = gPatientInfo.rewardpoints;
	}

	if (theForm.counterId.value == "") {
		// no counter: only raise bill is allowed
		document.getElementById('deposit').style.display="none";
		document.getElementById('ipdeposit').style.display="none";
		document.getElementById('rewardPoints').style.display="none";
		return;
	}

	/*
	 * If deposit object is not null then populate then max amount if it is sales.
	 */
	var billno = theForm.billType.value;
	var visitType = theForm.visitType.value;

	if ((!empty(ipDeposits) && ipDepositExists && visitType == 'i') && !gIsReturn && (billno == 'BN' || billno == 'BN-I')) {
		maxDeposit = getPaise(pharmacyDeposits.total_deposits) - getPaise(pharmacyDeposits.total_deposit_set_off);
		ipDeposit = getPaise(ipDeposits.total_ip_deposits) - getPaise(ipDeposits.total_ip_set_offs);
		genDeposit = maxDeposit - ipDeposit;
		if (maxDeposit>0 && ipDeposit>0) {
			document.getElementById('ipdeposit').style.display="table-row";
			document.getElementById('totAvlDep').value = maxDeposit;
			document.getElementById('ipDepAmtLabel').innerHTML= '(General:'+formatAmountPaise(genDeposit)+', IP: '+formatAmountPaise(ipDeposit)+')';
			isDepositThere=true;
		}else if(maxDeposit>0){
			document.getElementById('deposit').style.display="table-row";
			document.getElementById('maxAmtLabel').innerHTML= formatAmountPaise(maxDeposit);
			isDepositThere=true;
		}
	}else if ((!empty(pharmacyDeposits)) && !gIsReturn && (billno == 'BN' || billno == 'BN-I')) {
		maxDeposit = getPaise(pharmacyDeposits.total_deposits) - getPaise(pharmacyDeposits.total_deposit_set_off);
		if (maxDeposit>0) {
			document.getElementById('deposit').style.display="table-row";
			document.getElementById('maxAmtLabel').innerHTML= formatAmountPaise(maxDeposit);
			isDepositThere=true;
		}
	}else {
		theForm.depositsetoff.value="";
		theForm.ipdepositsetoff.value="";
		document.getElementById('deposit').style.display="none";
		document.getElementById('ipdeposit').style.display="none";
	}

	if ((!empty(pharmacyRewardPoints)) && !gIsReturn && (billno != 'BN-I')) {
		maxPoints = pharmacyRewardPoints.total_points_earned
					- (pharmacyRewardPoints.total_points_redeemed + pharmacyRewardPoints.total_open_points_redeemed);
		var maxPointsAmt = maxPoints * points_redemption_rate;
		if (maxPoints>0) {
			document.getElementById('rewardPoints').style.display="table-row";
			document.getElementById('maxRewardPointsLabel').innerHTML= maxPoints;
			document.getElementById('maxRewardPointsAmountLabel').innerHTML= formatAmountValue(maxPointsAmt);
		}
	} else {
		theForm.rewardPointsRedeemed.value="";
		theForm.rewardPointsRedeemedAmount.value="";
		document.getElementById('rewardPoints').style.display="none";
	}


	/*
	 * If it is sales return then get deposit amount for against the bill.
	 */
	if (gIsReturn && salesType == 'returnBill') {

		if (!empty(pharmacyDeposits)) {

			document.getElementById('deposit').style.display="table-row";
			setNodeText(document.getElementById('depositsetoffAmt'),
					formatAmountPaise(getPaise(pharmacyDeposits.total_deposit_set_off)));
			maxDeposit = formatAmountPaise(getPaise(pharmacyDeposits.total_deposit_set_off));

		} else {
			document.getElementById('deposit').style.display="none";
		}
	}
}

function onChangeDepositSetOff() {

	if (!validateDepositSetOff(theForm.depositsetoff))
		return false;
	if (!ajaxCallForDepositValidation())
		return false;
	return true;
}

function onChangeIPDepositsetoff() {
	theForm.depositsetoff.value = theForm.ipdepositsetoff.value;
	if (!validateDepositSetOff(theForm.ipdepositsetoff))
		return false;
	if (!ajaxCallForDepositValidation())
		return false;

	var visitType = theForm.visitType.value;
	if(!empty(ipDeposits) && ipDepositExists && visitType == 'i'){

		var depositSetOff = getPaise(theForm.ipdepositsetoff.value);

		var totalIPAvailablebalance = ipDeposit;
		var totalGeneralAvailablebal = genDeposit;

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

	if (!validateRewardPoints(theForm.rewardPointsRedeemed, theForm.rewardPointsRedeemedAmount))
		return false;

	return true;
}

function getAutoFillPrescription() {
	var storeId =document.getElementById("phStore").value;
	for (var i=0;i<jStores.length;i++) {
		if (jStores[i].DEPT_ID == storeId) {
			if (jStores[i].AUTO_FILL_PRESCRIPTIONS == 't') return true;
			else return false;
		}
	}
}

function getAutoFillIndents() {
	var storeId =document.getElementById("phStore").value;
	for (var i=0;i<jStores.length;i++) {
		if (jStores[i].DEPT_ID == storeId) {
			if (jStores[i].AUTO_FILL_INDENTS == 't') return true;
			else return false;
		}
	}
}

function setLabel (label,value) {
	label.textContent = '';
	label.innerHTML = value;
}

function setHiddenValue(row, name, value) {
	var el = getElementByName(row, name);
	if (el)
		el.value = value;
}

function clearFeilds() {
	document.salesform.medicine_name.value='';
	document.salesform.generic_name.value='';
    document.salesform.results.length=0;
}

/*
 * This function gets called when you click on Apply Discount button.
 * This will then apply the discount percentage entered, to every item in the grid
 */
function onApplyItemDiscPer() {
	var table = document.getElementById("medList");
	if (!validateDecimal(document.salesform.itemDiscPer,
	 getString("js.sales.issues.discountpercentage.adecimalnumber"), 2)) {
		document.salesform.itemDiscPer.focus();
		return;
	}

	if (getAmount(document.salesform.itemDiscPer.value) > 100) {
		showMessage("js.sales.issues.discount.notbegreaterthan100");
		document.salesform.itemDiscPer.focus();
		return;
	}

	for (var i=1;i<=getNumItems();i++) {
		var row = table.rows[i];
		var disc = getElementByName(row,"medDisc");
		var discType = getElementByName(row,"medDiscType");

		disc.value = getAmount(document.salesform.itemDiscPer.value);
		discType.value = document.salesform.itemDiscType.value;

		/*
		 * Call the recalc function for rowwise discount from here..it will check whether
		 * it is a MRP based or CP based sale and calculate and apply discount accordingly
		 */
		reCalcRowAmounts(row, false, true, "applyDisc");
	}
	/* call the setTotals method to calculate all the final totals*/
	setTotals();
}

/*
 * This function gets the bill details based on bill no. Called when the patient has a
 * separate pharma credit bill.
 */
function addToPhBillTotals(billno) {
	document.getElementById("billno").value = billno;
	var reqObject = newXMLHttpRequest();
	var url = "PhItemsCreditBill.do?_method=getBillItemDetails&billno="+billno;
	getResponseHandlerText(reqObject, handleBillDetailsResponse, url);
}

function handleBillDetailsResponse(responseText) {
	var totalPhAmt = 0;
	var totalPhDisc = 0;
	var totalPhNetAmt = 0;

	var totalHospAmt = 0;
	var totalHospDisc = 0;
	var totalHospNetAmt = 0;

	var billno;
	clearBillAmounts();
	if (responseText==null) return;
	if (responseText=="") return;
   	/* first get the pharma totals*/
   	eval("billdetails = "+responseText);
   	var details = billdetails.pharmabills;
    if ((details != null) && (details.length > 0)){
		for(var i=0;i<details.length;i++) {
			var billdetailrow = details[i];
			totalPhAmt = totalPhAmt+billdetailrow.amount;
			billno = billdetailrow.bill_no;
		}
	}

	/* Now get the Hospital amounts*/
	//eval("hospbills = "+responseText);
	var hospDetails = billdetails.hospdetails;
	var totalHospAmt = billdetails.hospAmt;
	var totalPhCredit = billdetails.pharmCredit;
	if (('undefined' == totalPhCredit) || (totalPhCredit == null)){
		totalPhCredit = 0;
	}
	var totalHospCredit = billdetails.hospCredit;
	if (('undefined' == totalHospCredit) || (totalHospCredit == null)){
		totalHospCredit = 0;
	}
	if (('undefined' == totalHospAmt) || (totalHospAmt == null)){
		totalHospAmt = 0;
	}
	var hospRecp = billdetails.hospRecp;
	var phOutstanding = totalPhAmt - totalPhCredit;
	var hospOutstanding = totalHospAmt - totalHospCredit;

	/* Finally, populate all the receipt amounts*/
	document.getElementById("lblPhCredits").innerHTML=(totalPhCredit).toFixed(decDigits);
	document.getElementById("lblPhOutstanding").innerHTML = phOutstanding.toFixed(decDigits);
	document.getElementById("lblPhBilled").innerHTML = (totalPhAmt).toFixed(decDigits);
	if (showHospAmts == 'Y'){
		document.getElementById("lblTotalCredits").innerHTML=(totalHospCredit).toFixed(decDigits);
		document.getElementById("lblHospOutstanding").innerHTML = hospOutstanding.toFixed(decDigits);
		document.getElementById("lblTotBilled").innerHTML = (totalHospAmt).toFixed(decDigits);
	}
}

function clearBillAmounts(){

	document.getElementById("lblPhCredits").innerHTML=parseFloat(0).toFixed(decDigits);
	document.getElementById("lblPhBilled").innerHTML = parseFloat(0).toFixed(decDigits);
	if (showHospAmts == 'Y'){
		document.getElementById("lblTotalCredits").innerHTML=parseFloat(0).toFixed(decDigits);
		document.getElementById("lblTotBilled").innerHTML = parseFloat(0).toFixed(decDigits);
	}

}

function openReceipt(type){
	var billno = document.getElementById("billno").value;
	var url;

	if (type == 'P'){
		url =cpath+"/billing/ReceiptList.do?_method=getReceipts&bill_no="+billno+"&payment_type=R&payment_type=F&counter_type=P";
	} else{
		url = cpath+"/billing/ReceiptList.do?_method=getReceipts&bill_no="+billno;

	}
    window.open(url);
}
function matches(mName, autocomplete) {
	var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
   	elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
	return elListItem;
}

function submitHandler() {
	theForm.authUser.value = document.getElementById('login_user').value;
	theForm.submit();
	return false;
}

//display claim related fields
function changePrimInsuRowDisplay(displayStyle){
	document.getElementById('prim_pre_auth_row').style.display = displayStyle;
	document.getElementById('prim_claim_row').style.display = displayStyle;
	document.getElementById('ins_amt_row').style.display = displayStyle;
}

function changeSecInsuRowDisplay(displayStyle){
	document.getElementById('sec_pre_auth_row').style.display = displayStyle;
	document.getElementById('sec_claim_row').style.display = displayStyle;
}

function setPrimClaimRelatedValues(rowObj){
	var primPriRow = document.getElementById("prim_pre_auth_row");
	var primClaimRow = document.getElementById("prim_claim_row");
	getElementByName(primClaimRow,'edlg_claim_amt').value = getElementByName(rowObj,'primclaimAmt').value;//rowObj.cells[PRIM_CLAIM_AMT_COL].textContent;
	getElementByName(primPriRow,'editPrior_auth_id').value = rowObj.cells[PRIM_PRE_AUTH_NO_COL].textContent;
	setSelectedIndex(getElementByName(primPriRow,'editPrior_auth_mode_id'),	getElementByName(rowObj,'primpreAuthModeId').value);

}

function setSecClaimRelatedValues(rowObj){

	var secPriRow = document.getElementById("sec_pre_auth_row");
	var secClaimRow = document.getElementById("sec_claim_row");

	getElementByName(secClaimRow,'edlg_claim_amt').value = getElementByName(rowObj,'secclaimAmt').value;//rowObj.cells[SEC_CLAIM_AMT_COL].textContent;
	getElementByName(secPriRow,'editPrior_auth_id').value = rowObj.cells[SEC_PRE_AUTH_NO_COL].textContent;
	setSelectedIndex(getElementByName(secPriRow,'editPrior_auth_mode_id'),	getElementByName(rowObj,'secpreAuthModeId').value);

}

function setSponsorPriorAuthHiddenValues(row,itemDetails){
	setNodeText(row.cells[SEC_PRE_AUTH_NO_COL],  getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_id').value);
	setHiddenValue(row, "secpreAuthId",  getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_id').value);
	setHiddenValue(row, "secpreAuthModeId", getElementByName(document.getElementById("sec_pre_auth_row"),'editPrior_auth_mode_id').value);
}

function visitWithPlan(){
	return ( gPatientInfo != null && !empty(gPatientInfo.patient_plan_details) && gPatientInfo.patient_plan_details.length >= 1 );
}

function hasMoreThanOnePlan(){
	return ( gPatientInfo != null && !empty(gPatientInfo.patient_plan_details) && gPatientInfo.patient_plan_details.length > 1 );
}

function getpatPlanDetails(){
	return ( visitWithPlan() ? gPatientInfo.patient_plan_details : null );
}

function getvisitPlansMasterDetails(){
	return ( visitWithPlan() ? gPatientInfo.visit_plans_master_details : null );
}

function calculateClaimAmount(planId,amt,visitType,categoryId, firstOfCategory,discount){
		var claimAmount = 0;
		var ajaxReqObject = newXMLHttpRequest();
		url = "./MedicineSalesAjax.do?method=getClaimAmount&plan_id="+planId+"&amount="+formatAmountPaise(amt)+"&visit_type="+visitType+"&category_id="+categoryId+"&foc="+firstOfCategory+"&discount="+discount;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					claimAmount = eval(ajaxReqObject.responseText);
			}
		}
		return getPaise(claimAmount);
}
//frequencyAutoComplet in sales and Quantity calculation
var dosageAutoComplete;
function initFrequencyAutoComplete() {
	if (dosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dosage_name"},
						{key : "per_day_qty"},
					 ]
		};
		// Instantiate first AutoComplete
		dosageAutoComplete = new YAHOO.widget.AutoComplete('d_frequency', 'frequencyContainer', ds);
		dosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		//dosageAutoComplete.typeAhead = true;
		dosageAutoComplete.useShadow = true;
		dosageAutoComplete.minQueryLength = 0;
		dosageAutoComplete.allowBrowserAutocomplete = false;
		dosageAutoComplete.maxResultsDisplayed = 20;

		dosageAutoComplete.itemSelectEvent.subscribe(setPerDayQty);
		dosageAutoComplete.unmatchedItemSelectEvent.subscribe(checkDosage);
		dosageAutoComplete.textboxChangeEvent.subscribe(clearQty);
	}
}
function setPerDayQty(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_per_day_qty').value = record.per_day_qty;
	calcQty('d');
}

function checkDosage() {
	document.getElementById('d_per_day_qty').value = '';
}

function clearQty(){
	document.getElementById('d_qty').value = '';
	calcQty('d');
}
var dosageAutoCompleteEdit =null;
//frequencyAutoComplete in editBox
function initFrequencyAutoCompleteEditBox() {
	if (dosageAutoCompleteEdit == null) {
		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dosage_name"},
						{key : "ed_per_day_qty"},
					 ]
		};
		// Instantiate first AutoComplete
		dosageAutoComplete = new YAHOO.widget.AutoComplete('ed_frequency', 'ed_frequencyContainer', ds);
		dosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		//dosageAutoComplete.typeAhead = true;
		dosageAutoComplete.useShadow = true;
		dosageAutoComplete.minQueryLength = 0;
		dosageAutoComplete.allowBrowserAutocomplete = false;
		dosageAutoComplete.maxResultsDisplayed = 20;

		dosageAutoComplete.itemSelectEvent.subscribe(setEPerDayQty);
		dosageAutoComplete.unmatchedItemSelectEvent.subscribe(checkEDosage);
		dosageAutoComplete.textboxChangeEvent.subscribe(clearEQty);
	}
}
function setEPerDayQty(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('ed_per_day_qty').value = record.ed_per_day_qty;
	calcQty('ed');
}

function checkEDosage() {
	document.getElementById('ed_per_day_qty').value = '';
}

function clearEQty(){
	document.getElementById('ed_qty').value = '';
	calcQty('ed');
}

function calcQty(event, idPrefix){
	var frequencyName = document.getElementById(idPrefix + '_frequency').value;
	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /[1-9]/;
	var regExp = new RegExp(validNumber);
	if (!validateMedBlockExceptQty("onchange", idPrefix)) return false;
	var perDayQty = null;
	for (var i=0; i<medDosages.length; i++) {
		var frequency = medDosages[i];
		if (frequencyName.trim().toLowerCase() == frequency.dosage_name.trim().toLowerCase()) {
			perDayQty = frequency.per_day_qty;
		}
	}
	if (perDayQty != null && !empty(duration)) {
		var duration_units_els = document.getElementsByName(idPrefix+'_duration_units');
		var duration_units = 'D';
		for (var j=0; j<duration_units_els.length; j++) {
			if (duration_units_els[j].checked) {
				duration_units = duration_units_els[j].value;
				break;
			}
		}
		var qty;
		if (duration_units == 'D'){
			qty = Math.ceil(duration * perDayQty);
		}else if (duration_units == 'W'){
			qty = Math.ceil((duration * 7) * perDayQty);
		}else if (duration_units == 'M'){
			qty = Math.ceil((duration * 30) * perDayQty);
		}
		if(perDayQty!= null || perDayQty!= undefined || perDayQty!="") {
			document.getElementById(idPrefix +'_per_day_qty').value = perDayQty;
		} else {
			document.getElementById(idPrefix +'_per_day_qty').value = 1;
		}
		document.getElementById(idPrefix + '_qty').value = qty;
	}
}

function validateMedBlockExceptQty(calledOn, idPrefix) {
    if(idPrefix == 'd'){
		var medicineName = document.getElementById('medicine').value;
	}
	else{
		var medicineName = document.getElementById('medName').value;
	}

	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);

	if (medicineName == '') {
		showMessage("js.sales.issues.enterthemedicinename");
		return false;
	}
	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		showMessage("js.sales.issues.duration.greaterthanzero.itshouldbeawholenumber");
		document.getElementById(idPrefix + '_duration').focus();
		return false
	}
	return true;
}

function modifyUOMLabel(obj, prefix) {
	document.getElementById(prefix+ '_consumption_uom_label').textContent = obj.value;
}
function setEdited() {
	fieldEdited = true;
}
var instructionAutoComplete = null;
function initInstructionAutoComplete() {
	if (instructionAutoComplete == null) {
		ds = new YAHOO.util.LocalDataSource({result : presInstructions});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "instruction_desc"}, ]
		};
		// Instantiate first AutoComplete
		instructionAutoComplete = new YAHOO.widget.AutoComplete('d_doc_remarks', 'remarksContainer', ds);
		instructionAutoComplete.minQueryLength = 1;
		instructionAutoComplete.allowBrowserAutocomplete = false;
		instructionAutoComplete.animVert = false;
		instructionAutoComplete.maxResultsDisplayed = 50;
		instructionAutoComplete.queryMatchContains = true;
		instructionAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
			return Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		};
		instructionAutoComplete.resultTypeList = false;
		instructionAutoComplete.autoSnapContainer = false;
		if (document.getElementById('d_doc_remarks').value != '') {
			instructionAutoComplete._bItemSelected = true;
			instructionAutoComplete._sInitInputValue = document.getElementById('d_doc_remarks').value;
		}
	}
}

var editInstructionAutoComplete = null; // remarks autocomplete for edit item dialog.
function initEditInstructionAutoComplete() {
	if (editInstructionAutoComplete == null) {
		ds = new YAHOO.util.LocalDataSource({result : presInstructions});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "instruction_desc"}, ]
		};
		// Instantiate first AutoComplete
		editInstructionAutoComplete = new YAHOO.widget.AutoComplete('ed_doc_remarks', 'ed_remarksContainer', ds);
		editInstructionAutoComplete.minQueryLength = 0;
		editInstructionAutoComplete.allowBrowserAutocomplete = false;
		editInstructionAutoComplete.animVert = false;
		editInstructionAutoComplete.maxResultsDisplayed = 50;
		editInstructionAutoComplete.queryMatchContains = true;
		editInstructionAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
			return Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		};
		editInstructionAutoComplete.resultTypeList = false;
		editInstructionAutoComplete.autoSnapContainer = false;
		if (document.getElementById('ed_doc_remarks').value != '') {
			editInstructionAutoComplete._bItemSelected = true;
			editInstructionAutoComplete._sInitInputValue = document.getElementById('ed_doc_remarks').value;
		}
	}
}
function validateSaleDate(){
	var saleDateObj = salesform.payDate;
	var valid = true;
	valid = valid && doValidateDateField(saleDateObj,'past');
	if ( gPatientInfo != null ) {
		var regDateTime = ( gPatientType != 'retail' && gPatientType != 'retailCredit' ?
				formatDate(new Date(gPatientInfo.patientDetails.reg_date)) + " " + formatTime(new Date(gPatientInfo.patientDetails.reg_time))
				                      :
				formatDate(new Date(gPatientInfo.retailDetails.visit_date) ));
		valid = valid && !isLessThanAdmitDate(saleDateObj);
		if ( !valid ){
			var msg = getString("js.sales.issues.no.sale.below.admission.date");
			msg = msg + " " +regDateTime;
			alert(msg);
			return false;
		}

		//for new bill sale date will become open date so no validation is required on bill open date
		if ( document.salesform.billType.value != 'BN' && document.salesform.billType.value != 'BN-I' && document.salesform.billType.value != 'BL' ) {
			valid = valid && !isLessThanBillOpenDate(saleDateObj);

			if ( !valid ) {
				var msg = getString("js.sales.issues.no.sale.below.bill.opendate");
				msg = msg + formatDate(getBillOpenDate())+ " " +formatTime(getBillOpenDate());
				alert(msg);
				return false;
			}

		}
	}

	return valid;
}

function isLessThanAdmitDate(saleDate){
	var regDateTime = ( gPatientType != 'retail' && gPatientType != 'retailCredit' ? getDateTime(
				formatDate(new Date(gPatientInfo.patientDetails.reg_date)),
				formatTime(new Date(gPatientInfo.patientDetails.reg_time)) )
				                      :
				new Date(gPatientInfo.retailDetails.visit_date) );
	var saleDateTime = getDateTimeFromField(saleDate, salesform.payTime);

	return saleDateTime < regDateTime;
}

function isLessThanBillOpenDate(saleDate){
	var selectedBillOpenDate = getBillOpenDate();
	var saleDateTime = getDateTimeFromField(saleDate, salesform.payTime);

	return saleDateTime < selectedBillOpenDate;
}

function getBillOpenDate(){

	if ( gPatientInfo == null || gPatientType == 'retail' || gPatientType == 'retailCredit' )
		return null;


	var billNo = theForm.billType.value;
	var bill = findInList(gPatientInfo.bills,'bill_no',billNo);


	var billOpenDate = null;
	if ((billNo == 'BN') || (billNo == 'BN-I') || (billNo == 'BL'))
		billOpenDate = getServerDate();
	else
		billOpenDate = getDateTime(bill.open_date.split(" ")[0],bill.open_date.split(" ")[1]);

	return billOpenDate;

}

function getPerDayFreq(frequency) {
	if(frequency == undefined) {
		return "1";
	}
	if(medDosages.length>0) {
		for(var count=0;count<medDosages.length;count++) {
			if(medDosages[count].dosage_name == frequency) {
				if(medDosages[count].per_day_qty == null) {
					return "1";
				} else {
					return medDosages[count].per_day_qty;
				}
			}
		}
	} else {
		return "1";
	}

}
function clearDataGridInfo(insuranceChanged, gIsOnload) {
	if(gPatientInfo!= null && gPatientInfo.presDetails != null && gPatientInfo.presDetails != undefined) {
		if ((gPatientInfo.presDetails.length > 0 && insuranceChanged) && !gIsOnload){
			deleteRows();
			var itemDetailsTableObj = document.getElementById("medList");
			var numItems = getNumItems();
			var firstItemRow = getFirstItemRow();

			for (var i=0; i<numItems; i++) {
				document.getElementsByName("freqCount")[i].value='';
				document.getElementsByName("allowedQty")[i].value='';
				document.getElementsByName("insuranceExpired")[i].value='false';
				document.getElementsByName("total_issed_qty")[i].value='';
			}
			oldMedicineId = '';
			serItemUserQty = 0;
			itemQtyMap = {};
			if (getAutoFillPrescription() && gPatientInfo.prescriptions_exists && gPatientInfo.presDetails.length > 0) {
				addMedicinesFromPrescription(gPatientInfo.presDetails);
			}
			if ( !empty(gPatientInfo.indentsList) && gPatientInfo.indentsList.length > 0 ){
				addMedicinesFromIndents(gPatientInfo.patIndentDetails);
			}
			if (getAutoFillPrescription() && gPatientInfo.dischargeMedication_exists && gPatientInfo.dischargeMedicationDetails.length > 0) {
				addMedicinesFromPrescription(gPatientInfo.dischargeMedicationDetails);
			}

		}
	}

}
/**
 * This method used to get Category Cliamable Staus based on medicineId, visitId and visit type
 *
 * @param medicineId
 * @param asyncStaus
 * @returns {String}
 */
function getCatPayableStatus(medicineId, asyncStatus) {
	var catPayableStatus = "";
	var ajaxReqObject = newXMLHttpRequest();
	var visitId = document.getElementsByName("visitId")[0].value;
	var visitType = document.getElementsByName("visitType")[0].value;
	var url = "MedicineSalesAjax.do?method=getInsuranceCategoryPayableStatus" +
		"&visitId="+visitId +
		"&medicineId="+medicineId+
		"&visitType="+visitType;

	ajaxReqObject.onreadystatechange = function() {
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("calimbaleInfo = " + ajaxReqObject.responseText)
				catPayableStatus = calimbaleInfo;
			}
		}
	}
	ajaxReqObject.open("GET",url.toString(), asyncStatus);
	ajaxReqObject.send(null);
	return catPayableStatus[0];
}

function getRatePlanDetails(orgId) {
	var orgDetails = null;
	var ajaxReqObject = newXMLHttpRequest();
	var url = "MedicineSalesAjax.do?method=getRatePlanDetails&orgId="+orgId;
	
	ajaxReqObject.open("GET",url.toString(), false);
	ajaxReqObject.send(null);
	
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			eval("ratePlanDetails = " + ajaxReqObject.responseText);
			orgDetails = ratePlanDetails;
		}
	}
	
	return orgDetails;
}

function initPatientInsuranceDetailsDialog() {

	primaryInsurancePhotoDialog = new YAHOO.widget.Dialog('primaryInsurancePhotoDialog', {
        width:"525px",
        visible: false,
        modal: true,
        constraintoviewport: true,
		close:false,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePrimaryInsurancePhotoDialogCancel,
                                               scope:primaryInsurancePhotoDialog,
                                               correctScope:true } );
    primaryInsurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
    primaryInsurancePhotoDialog.render();

}

function handlePrimaryInsurancePhotoDialogCancel(){
	 document.getElementById('primaryInsurancePhotoDialog').style.display='none';
	 document.getElementById('primaryInsurancePhotoDialog').style.visibility='hidden';
	 primaryInsurancePhotoDialog.cancel();
}

function initPatientInsuranceSecDetailsDialog(){

	secondaryInsurancePhotoDialog = new YAHOO.widget.Dialog('secondaryInsurancePhotoDialog', {
       width:"525px",
       visible: false,
       modal: true,
       constraintoviewport: true,
		close:false,
   });

   var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                            { fn:handlesecondaryInsurancePhotoDialogCancel,
                                              scope:secondaryInsurancePhotoDialog,
                                              correctScope:true } );
   secondaryInsurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
   secondaryInsurancePhotoDialog.render();
}

function handlesecondaryInsurancePhotoDialogCancel(){
	 document.getElementById('secondaryInsurancePhotoDialog').style.display='none';
	 document.getElementById('secondaryInsurancePhotoDialog').style.visibility='hidden';
	 secondaryInsurancePhotoDialog.cancel();
}

function setPatientPhotoDialogWidth(){
	var mrNo = document.getElementById('patientMrno').innerText;
	var patientPhotoDialog = document.getElementById('patientPhotoDialog');
	var pd_patientImage = document.getElementById('pd_patientImage');
	if(patient_photo_available == 'Y'){
		//patientPhotoDialog.style.width = '500px';
		pd_patientImage.src = cpath+'/Registration/GeneralRegistrationPatientPhoto.do?_method=viewPatientPhoto&mrno='+mrNo;
	}else{
		//patientPhotoDialog.style.width = '500px';
		pd_patientImage.src = "";
	}
}

function getPatientPolicyId() {
	var mrNo = document.getElementById('patientMrno').innerText;
	var PatientPolicyId = "";
	var ajaxReqObject = newXMLHttpRequest();

	var url = "MedicineSalesAjax.do?method=getPatientPolicyId" +"&visitId="+patientVisitId +"&mrNo="+mrNo;

	ajaxReqObject.open("GET",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			eval("PolicyId = " + ajaxReqObject.responseText)
			PatientPolicyId = PolicyId;
		}
	}
	return PatientPolicyId;
}

function setPatientInsurancePhotoDialog(sponsorIndex){

	var PolicyId = getPatientPolicyId();

	if(sponsorIndex == 'P'){
		var pinsuranceImage = document.getElementById('pinsuranceImage');
		pinsuranceImage.src = cpath+'/Registration/GeneralRegistrationPlanCard.do?_method=viewInsCardImageForVisit&visitId='+patientVisitId+'&sponsorIndex='+sponsorIndex+'&sponsorType='+sponsor_type+'+&patient_policy_id='+PolicyId[0].patient_policy_id;

	}else{
		var sinsuranceImage = document.getElementById('sinsuranceImage');
		sinsuranceImage.src = cpath+'/Registration/GeneralRegistrationPlanCard.do?_method=viewInsCardImageForVisit&visitId='+patientVisitId+'&sponsorIndex='+sponsorIndex+'&sponsorType='+sec_sponsor_type+'+&patient_policy_id='+PolicyId[1].patient_policy_id;
	}

}

function showPatientInsuranceDialog(){

	setPatientInsurancePhotoDialog("P");
	var button = document.getElementById('pri_cardIcon');
	primaryInsurancePhotoDialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById('primaryInsurancePhotoDialog').style.display='block';
	document.getElementById('primaryInsurancePhotoDialog').style.visibility='visible';

	primaryInsurancePhotoDialog.show();
}

function showPatientInsuranceSecDialog(){
	setPatientInsurancePhotoDialog("S");
	var button = document.getElementById('sec_cardIcon');
	secondaryInsurancePhotoDialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById('secondaryInsurancePhotoDialog').style.display='block';
	document.getElementById('secondaryInsurancePhotoDialog').style.visibility='visible';

	secondaryInsurancePhotoDialog.show();
}

function toggleManagement() {
	if(document.getElementById("presMang").style.display == "block") {
		document.getElementById("presMang").style.display="none";
		document.getElementById("down").style.display="block";
		document.getElementById("up").style.display="none";
	} else {
		document.getElementById("presMang").style.display="block";
		document.getElementById("up").style.display="block";
		document.getElementById("down").style.display="none";
	}
	return false;
}

function editToggleManagement() {
	if(document.getElementById("editPresMang").style.display == "block") {
		document.getElementById("editPresMang").style.display="none";
		document.getElementById("editdown").style.display="block";
		document.getElementById("editup").style.display="none";
	} else {
		document.getElementById("editPresMang").style.display="block";
		document.getElementById("editup").style.display="block";
		document.getElementById("editdown").style.display="none";
	}
	return false;
}

/**
 * Process store tariff expr and get selling price.
 */
function getSellingPrice(medicineId, itemBatchId, visitSellingPrice, storeSellingPrice, sellingPrice, qty) {
	if(medicineId != undefined && itemBatchId != undefined && (visitSellingPrice != undefined || storeSellingPrice != undefined)
			&& (visitSellingPrice != null|| storeSellingPrice != null) && (visitSellingPrice != 'null'|| storeSellingPrice != 'null')
			&& (visitSellingPrice != ''|| storeSellingPrice != '') && sellingPrice != undefined && qty != undefined) {

		var exprSellingPrice = sellingPrice;
		var storeId = document.getElementById("phStore").value;
		var url = cpath+'/sales/getsellingprice.json?itemBatchId='+itemBatchId+
				"&storeId="+storeId+"&qty="+qty+
				"&visitStoreRatePlanId="+gStoreRatePlanId+"&mrp="+sellingPrice+"&medicine_id="+medicineId;
		var useBatchMrp = false;
		for (var i=0;i<jStores.length;i++) {
			if (jStores[i].DEPT_ID == parseInt(storeId)) {
				if (jStores[i].USE_BATCH_MRP === 'Y') useBatchMrp = true;
			}
		}
		var ajaxReqObject = newXMLHttpRequest();
		if(!isNaN(visitSellingPrice) && (visitSellingPrice != undefined && visitSellingPrice != '' && visitSellingPrice != null && visitSellingPrice != 'null')) {
			exprSellingPrice =  visitSellingPrice;

		} else if((visitSellingPrice == undefined || visitSellingPrice == '' || visitSellingPrice == null || visitSellingPrice == 'null') && useBatchMrp) {
			exprSellingPrice = sellingPrice;

		} else if((visitSellingPrice == undefined || visitSellingPrice == '' || visitSellingPrice == null || visitSellingPrice == 'null')
				&& !isNaN(storeSellingPrice) && (storeSellingPrice != undefined && storeSellingPrice != '' && storeSellingPrice != null && storeSellingPrice != 'null')) {
			exprSellingPrice =  storeSellingPrice;

		} else if(isNaN(visitSellingPrice)) {
			url += "&is_visit_store_rate_plan_id=true";
			if (qty == undefined || qty == 0 || qty == '') {
				qty = 1;
			}
			ajaxReqObject.open("GET",url.toString(), false);
			ajaxReqObject.send(null);
			if (ajaxReqObject.readyState == 4) {
				if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					exprSellingPrice = eval(ajaxReqObject.responseText);
				}
			}
		} else if(isNaN(storeSellingPrice)) {
			if (qty == undefined || qty == 0 || qty == '') {
				qty = 1;
			}
			url += "&is_visit_store_rate_plan_id=false";
			ajaxReqObject.open("GET",url.toString(), false);
			ajaxReqObject.send(null);
			if (ajaxReqObject.readyState == 4) {
				if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					exprSellingPrice = JSON.parse(ajaxReqObject.responseText);
				}
			}
		}
		return exprSellingPrice;
	} else {
		if(sellingPrice != undefined) {
			return sellingPrice;
		} else {
			return 0.00;
		}
	}
}

/**
 * On Change Bill Type set the selling price.
 *
 * @param batchSellingPrice
 */
function setSellingPrice(batchSellingPrice,i) {
	var table = document.getElementById("medList");
	//for (var i=1;i<=getNumItems();i++) {
		var row = table.rows[i];
		var medicineId = getElementByName(row,"medicineId").value;
		var itemBatchId = getElementByName(row,"itemBatchId").value;
		var visitSellingPrice = getElementByName(row,"visit_selling_price").value;
		var storeSellingPrice = getElementByName(row,"store_selling_price").value;
		var sellingPriceHid = getElementByName(row,"selling_price_hid").value;
		var qty = getElementByName(row,"qty").value;
		var sellingPrice = getSellingPrice(medicineId, itemBatchId, visitSellingPrice,
				storeSellingPrice, batchSellingPrice != null ? batchSellingPrice : sellingPriceHid, qty);
		setHiddenValue(row, "pkgmrp" , sellingPrice);
		setHiddenValue(row, "origRate" , sellingPrice);
		setNodeText(row.cells[MRP_COL], parseFloat(sellingPrice).toFixed(decDigits));
	//}
}

// Only taxBasis C and CB are supported.
function setTaxDetails(taxItem, item, type) {
	var url = '';
	if(type == 'S')
		url = saleTaxURL;
	else
		url = saleReturnTaxURL;

	var vatRate = 0;
	var vatAmt = 0;
	var netAmt = 0;
	var discountAmt = 0;
	var originalTax=0;
	var response = null;
	if(!gOnChangeBillType || type != 'S' || gTransaction == 'estimate' )
		response = ajaxFormObj(taxItem, url, false);
	if (response != undefined) {
		var taxMap = response.tax_details;
		if(isNotNullObj(response.net_amount))
			netAmt = response.net_amount
		if(isNotNullObj(response.discount_amount))
			discountAmt = response.discount_amount;

		for(var i=0; i<taxMap.length; i++) {
		    for(var j=0; j < subgroupNamesList.length; j++) {
		    	if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id] && taxMap[i][subgroupNamesList[j].item_subgroup_id] != null) {
		    		var val = taxMap[i][subgroupNamesList[j].item_subgroup_id];
		    		var itemGroupId = subgroupNamesList[j].item_group_id;
		    		item['taxrate'+itemGroupId] = parseFloat(val.rate).toFixed(decDigits);
		    		item['taxamount'+itemGroupId] = parseFloat(val.amount);
		    		item['taxsubgroupid'+itemGroupId] = subgroupNamesList[j].item_subgroup_id;

		    		vatAmt += parseFloat(val.amount);
				    vatRate += parseFloat(val.rate);
				    originalTax += parseFloat(val.original_tax_amt);
		    	}
			}

		}
		if(type == 'S') {
			originalTax = vatAmt;
		}
	}
	var taxDetails = {
			vatRate:vatRate,
			vatAmt:vatAmt,
			netAmt:netAmt,
			discountAmt:discountAmt,
			original_tax:originalTax
	};
	return taxDetails;
}

function getSubgroups(){
	var url = cpath + "/sales/taxgroups.json";
	var response = ajaxGetFormObj(null, url, false);
	if(response && response != null) {
		gItemTaxGroups = response.item_groups?response.item_groups:'';
		gItemTaxSubGroups = response.item_subgroups?response.item_subgroups:'';
	}
}

function taxGroupInit(id) {
	var labelNameOptions = '';
	var count = 1;
	for(var key in gItemTaxGroups) {
		if (gItemTaxGroups.hasOwnProperty(key)) {
			// Set Groupname label.
			labelNameOptions += '<td class="formlabel">';
			labelNameOptions += gItemTaxGroups[key].item_group_name;
			labelNameOptions += ':</td>';
			labelNameOptions += '<td class="forminfo"';
			if(id == 'add_tax_groups') {
				labelNameOptions += ' colspan="2">';
				labelNameOptions += '<select name="subgroups" id="ad_taxsubgroupid'+gItemTaxGroups[key].item_group_id+'">';
			} else {
				labelNameOptions += ' colspan="1">';
				labelNameOptions += '<select name="subgroups" id="ed_taxsubgroupid'+gItemTaxGroups[key].item_group_id+'">';
			}

			labelNameOptions += '<option value="">--select--</option>';
			//Set subgroup dropdown.
			for(var subkey in gItemTaxSubGroups) {
				if (gItemTaxSubGroups.hasOwnProperty(subkey)) {

					for(var i=0; i<gItemTaxSubGroups[subkey].length ; i++) {
						if(gItemTaxSubGroups[subkey][i].item_group_id == gItemTaxGroups[key].item_group_id) {
							labelNameOptions += '<option value='+gItemTaxSubGroups[subkey][i].item_subgroup_id
							labelNameOptions += '>'+gItemTaxSubGroups[subkey][i].item_subgroup_name+'</option>';
						}
					}
				}
			}
			labelNameOptions += '</select>';
			if(count >= 2) {
				labelNameOptions += '</td>';
				labelNameOptions += '</tr>';
				labelNameOptions += '<tr>';
				count = 0;
			} else {
				labelNameOptions += '</td>';
			}
			count++;
		}
	}
	document.getElementById(id).outerHTML = labelNameOptions;
}

function ajaxCallForDepositValidation(){
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/pages/stores/MedicineSales.do?method=depositsSetOffAjax&mr_no=" + theForm.mrno.value+"&visit_type="+theForm.visitType.value;

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
		var currMaxDeposit = 0;
		var currIpDeposit = 0;
		var currGenDeposit = 0;
		pharmacyDeposits = returnedData.deposit;
		ipDeposits = returnedData.ipdeposit;
		ipDepositExists = returnedData.ipDepositExists;

		var billno = theForm.billType.value;
		var visitType = theForm.visitType.value;

		if ((!empty(ipDeposits) && ipDepositExists && visitType == 'i') && !gIsReturn && (billno == 'BN' || billno == 'BN-I')) {
			currMaxDeposit = getPaise(pharmacyDeposits.total_deposits) - getPaise(pharmacyDeposits.total_deposit_set_off);
			currIpDeposit = getPaise(ipDeposits.total_ip_deposits) - getPaise(ipDeposits.total_ip_set_offs);
			currGenDeposit = currMaxDeposit - currIpDeposit;

		}else if ((!empty(pharmacyDeposits)) && !gIsReturn && (billno == 'BN' || billno == 'BN-I')) {
			currMaxDeposit = getPaise(pharmacyDeposits.total_deposits) - getPaise(pharmacyDeposits.total_deposit_set_off);

		}
		if(currMaxDeposit !=maxDeposit || ipDeposit != currIpDeposit || genDeposit != currGenDeposit){
			alert("Displayed Deposit Availability in this screen and Actual Deposit available with selected MrNo are not matching. " +
			"There are latest updates on deposit availability status. Please refresh your screen and verify the data before you continue your action.");
			return false;
		}
	}
	return true;
}

function onChangeOfNationalityId() {
	deleteRows();
}

function setGovtPattern() {
	if (!empty(document.getElementById('identifierId'))) {
		var identifierTypeId = document.getElementById('identifierId') && document.getElementById('identifierId').value;
		govtId_pattern = '';
		if (!identifierTypeId) {
			return;
		}
		var selectedIdentifier = govtIdentifierTypesJSON.find(
			function(govtIdentifierType) {
				return identifierTypeId == govtIdentifierType.identifier_id;
		});
	}
	govtId_pattern = selectedIdentifier && selectedIdentifier.govt_id_pattern ? selectedIdentifier. govt_id_pattern : '';

}

function validateGovernmentIdentifier() {
	if (document.getElementById("governmentIdentifier") && trim(document.getElementById("governmentIdentifier").value) != ''
		&& !(FIC_checkField(" validate-govt-id ", document.getElementById("governmentIdentifier")))) {
				 document.getElementById("governmentIdentifier").focus();
				 alert(regPref.government_identifier_label + " "
						+ getString("js.sales.issues.enter.govt.invalid.string")
						+ ". " + " "
						+ getString("js.sales.issues.enter.govt.format.string")
						+ " : " + govtId_pattern);
			return false;
	}
	return true;
}

function setHelpText() {
	if (gRetailSaleRights != 'N') {
		getExamplePhoneNumber('+'+defaultCountryCode,$("#retail_patient_phone_help"),$("#retail_patient_phone_error"));
	}
	if (gRetailCreditSaleRights != 'N') {
		getExamplePhoneNumber('+'+defaultCountryCode,$("#retail_credit_patient_phone_help"),$("#retail_credit_patient_phone_error"));
	}
}

var phonePattern = new RegExp("^\\+?\\d{0,15}$");

function enterNumOnlyzeroToNinePlus(e) {

	if(e && e.target){
		var c = getEventChar(e);

		if(isCharControl(c)){
			return true;
		}


		let changedValue = e.target.value.split('');
		changedValue.splice(e.target.selectionStart, e.target.selectionEnd - e.target.selectionStart, String.fromCharCode(c) );
		changedValue = changedValue.join('');

		return phonePattern.test(changedValue);
	}
}

/* Copies prior auth information from insurance details subsection in sales screen
 * to all items in the medicine grid. Copies primary and secondary prior auth numbers and modes.
 * */
function copyPriorAuthToItems() {
	var priPriorAuthNo = document.getElementById("priPriorAuthNo").value;
	var secPriorAuthNo = document.getElementById("secPriorAuthNo").value;
	var priPriorAuthMode = document.getElementById("priPriorAuthMode").value;
	var secPriorAuthMode = document.getElementById("secPriorAuthMode").value;
	var medicineRows = document.getElementsByClassName("medRow");
	if ((priPriorAuthNo != "" && priPriorAuthMode == "") || (secPriorAuthNo != "" && secPriorAuthMode == "")) {
		showMessage("js.sales.issues.auth.mode.and.prior.auth.number.are.required");
	} else {
		for (var i=0; i<medicineRows.length; i++) {
			var row = medicineRows[i];
			// copy primary sponsor prior auth information
			setNodeText(row.cells[PRIM_PRE_AUTH_NO_COL], priPriorAuthNo);
			setHiddenValue(row, "primpreAuthId", priPriorAuthNo);
			setHiddenValue(row, "primpreAuthModeId", priPriorAuthMode);
			// copy secondary sponsor prior auth information
			setNodeText(row.cells[SEC_PRE_AUTH_NO_COL], secPriorAuthNo);
			setHiddenValue(row, "secpreAuthId", secPriorAuthNo);
			setHiddenValue(row, "secpreAuthModeId", secPriorAuthMode);
		}
	}
}

function validateMobileNumber(eventDataObj) {
	if(eventDataObj && eventDataObj.target.readOnly){
		return;
	}

	if (gPatientType == 'retail') {
		var customerMobileNoObj=$("#retailPatientMobileNoField");
		var customerMobileErrorObj=$("#retail_patient_phone_error");
		var customerMobileValidObj= $("#cust_patient_phone_valid");
		var custMobileCountryCodeObj = $('<input>').attr('type','hidden');
		var patientPhone = $("#retailPatientMobileNo");
		var tempCustomerMobileNoObj = $('<input>').attr('type','hidden').attr('value',customerMobileNoObj.val());

		//use to get country code
		getNationalAndCountryCode(tempCustomerMobileNoObj, custMobileCountryCodeObj);
		clearErrorsAndValidatePhoneNumberSingleField(patientPhone,customerMobileValidObj,
			tempCustomerMobileNoObj,customerMobileNoObj,custMobileCountryCodeObj,customerMobileErrorObj,'N',eventDataObj);


	}
	if (gPatientType == 'retailCredit') {
		var customerMobileNoObj=$("#custRCreditPhoneNoField");
		var customerMobileErrorObj=$("#retail_credit_patient_phone_error");
		var customerMobileValidObj= $("#cust_retail_patient_phone_valid");
		var custMobileCountryCodeObj = $('<input>').attr('type','hidden');
		var patientPhone = $('#custRCreditPhoneNo')
		var tempCustomerMobileNoObj = $('<input>').attr('type','hidden').attr('value',customerMobileNoObj.val());


		getNationalAndCountryCode(tempCustomerMobileNoObj, custMobileCountryCodeObj);
		clearErrorsAndValidatePhoneNumberSingleField(patientPhone,customerMobileValidObj,
				tempCustomerMobileNoObj,customerMobileNoObj,custMobileCountryCodeObj,customerMobileErrorObj,'Y',eventDataObj);
	}
}
//Check the Cash Limit for Retail Patients
function checkRetailCashLimitValidation(){
	var numPayments = getNumOfPayments();
	if (numPayments <= 0) return true;
	var amount =0;
	for (i=0; i<numPayments; i++){
		var totPayingAmt = "totPayingAmt"+i;
		var paymentModeId = "paymentModeId"+i;
		var paymentModelValue = $("#"+paymentModeId+" option:selected").val();
		if (paymentModelValue == -1) {
			var cashAmount = $("#"+totPayingAmt+"").val();
			amount += getAmount(cashAmount);
		}
		if (amount != 0 && amount > cashTransactionLimitAmt){
			alert("Total cash in aggregate from this patient in a day reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");
			return false;
		}
	}
	return true;
}


var isRetDetlDialgInitzld = false;
var retiredEligibilityDetailsDialog;
function initRetiredEligibilityDetailsDialog() {
	if(!isRetDetlDialgInitzld) {
		retiredEligibilityDetailsDialog = new YAHOO.widget.Dialog('retiredEligibilityDetailsDialog', {
	    	context:["","tr","br", ["beforeShow", "windowResize"]],
	        width:"525px",
	        visible: false,
	        modal: true,
	        constraintoviewport: true,
			close :false,
	    });
		var escKeyListener = new YAHOO.util.KeyListener('retiredEligibilityDetailsDialog', { keys:27 },
	                                             { fn:handleRetiredEligibilityDetailsDialogCancel,
	                                               scope:retiredEligibilityDetailsDialog,
	                                               correctScope:true } );
	    retiredEligibilityDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
		isRetDetlDialgInitzld = true;
		retiredEligibilityDetailsDialog.render();
   }
}

function handleRetiredEligibilityDetailsDialogCancel(){
	 document.getElementById('retiredEligibilityDetailsDialog').style.display='none';
	 document.getElementById('retiredEligibilityDetailsDialog').style.visibility='hidden';
	 retiredEligibilityDetailsDialog.cancel();
}


function showRetiredEligibilityDetailsDialog(){
	document.getElementById('retiredEligibilityDetailsDialog').style.display='block';
	document.getElementById('retiredEligibilityDetailsDialog').style.visibility='visible';
	retiredEligibilityDetailsDialog.show();
}

function setRetiredEligibilitylink() {
	if (salesType == 'hospital' && visitType == 'o' && gPatientInfo.patientDetails.patient_category == 3) {
		document.getElementById('retiredEligibilityCheck').style.display='block';
	    document.getElementById('retiredEligibilityCheck').style.visibility='visible';
	} else {
		document.getElementById('retiredEligibilityCheck').style.display='none';
	    document.getElementById('retiredEligibilityCheck').style.visibility='hidden';
	}
}

function showRetiredEligibilityDetails() {
    var xhttp = new XMLHttpRequest();
    var url = cpath + '/common/commonAjaxRequest.do?_method=getFamilyAnnualPharmacyUtilization&mr_no=' + theForm.mrno.value;
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4 && this.status == 200) {
            var employees = eval('(' + xhttp.responseText + ')');
            document.getElementById("retiredEligibilityDetailsTable").innerHTML = createRetEliDetTable(employees);
            showRetiredEligibilityDetailsDialog();
        }
    }
    ;
    xhttp.open("GET", url.toString(), true);
    xhttp.send();
}

function createRetEliDetTable(employees) {
	var i;
	var sum = 0;
	var limit = 8000;
	var t="";	
	for (i = 0; i < employees.length ; i++) {
		t += "<tr><td class=\"forminfo\" style=\"width:10px;align:left\">" + employees[i].mr_no + "<\/td>";
		t += "<td class=\"forminfo\" style=\"width:10px;text-align:left;\">" + employees[i].patient_name + "<\/td>";
		t += "<td class=\"forminfo\" style=\"width:10px;text-align:left;\">" + employees[i].emp_id + "<\/td>";
		t += "<td class=\"forminfo\" style=\"width:10px;text-align:right;\">" + roundoff_2(employees[i].utilized_amount) + "<\/td>";
		t += "<\/tr>";
		sum += parseFloat(employees[i].utilized_amount);
	}
	document.getElementById("totalmedicineamountutilized").innerHTML = roundoff_2(sum);
	document.getElementById("balancemedicinelimit").innerHTML = roundoff_2(limit - sum);	
	return t;
}
function roundoff_2(num) {
    return (Math.round(num * 100) / 100).toFixed(2);
}

