var patientInfo = null;
var addOrderDialog;
var customFieldList = null;
var visitCustomFieldList = null;
var gIsInsurance = false;
var isOnLoad = false; // Global variable to restrict multiple calls to ratePlanChange() on onload.
var patientTodaysAppointmentDetails = null;
var package_id = null;
var channel_orders_added = false;
var smartCardDetails;
var isMemberidValidated = false;
var loadPrvPrescripitons = false;
var cachedBedCharges = {};
policynames = [];


function initCard(){

	initSmartCardConflictDialog();
	initPatDetailsFromSmartCardDialog();
	initPatDetailsFromSmartCardDialogErr();
}

function initRegistration() {
	isOnLoad = true;

	// initailize all required autocompletes.
	initOnLoadDetails();

	// reset all fields
	//clearRegDetails();

	// Set patientConfidentialityCategories options
	initializePatientGroupsList();

	// If default registration type is mrno selection,
	// set the default selection and reset all fields.
	setDefaultSelection();
	clearRegDetails();

	// Enable/disable patient category expiry date after
	//the patient category is set according to preference.
	categoryChange();
	setGovtPattern();
	if (isNewReg()) {
		document.mainform.salutation.focus();
	} else {
		document.mrnoform.mrno.focus();
	}
	// loading patient registration using mrno
    // MRNo will be present when request comes from qikwell
  if(empty(admissionReqId))
	  loadPatientDetailsUsingMrno(MRNo)

	// load patient registration from scheduler.
	loadSchedulerPatientDetails();
	if(isPatientPhoneMandate != 'Y'){
		if(screenId == 'out_pat_reg'){
			$(".patient_phone_star").hide();
		}
  		else if (mod_mobile != 'Y') {
			$(".patient_phone_star").hide();
		}
	}
	if(isPatientEmailMandate != 'Y'){
		if(screenId == 'out_pat_reg'){
			$(".patient_email_star").hide();
		}
		else if (mod_mobile != 'Y') {
			$(".patient_email_star").hide();
		}
	}

	//loadSchedulerPatientDetailsOnly();

	// load patient registration from admission request.
	if(!empty(admissionReqId))
		loadPatientAdmissionRequestDetails();

	if (trim(document.mrnoform.mrno.value) == "") {
		if (category != null && (category != 'SNP' || !empty(scheduleName))){
			loadPrvPrescripitons = true;
			loadSchedulerOrders();
		}
		estimateTotalAmount();
	}

	// reset after page is loaded
	isOnLoad = false;

	initPatientphotoViewDialog();
	initprimarySponsorDialog();
	initsecondarySponsorDialog();

	disableRegBillButton();

	enableRightsTochangeDynamicCopay();
	enableVisitClassification();
	initInsurancePlanDetailsDialog();

	//if(smartCardEnabled == 'Y'){
		initCard();
	//}
	loadUhidPatient();
	if(regPref.showReferralDoctorFilter == 'Y') {
		initRegistrationReferralDoctorFilterAutocompletes();
	}
	copyPasteImage("pastedPhoto",processPastedData);
	copyPasteImage("primary_sponsor_pastedPhoto",processPastedData);
	copyPasteImage("secondary_sponsor_pastedPhoto",processPastedData);
}


//Global variable for pasted image.
var pastedImages = new Object();
function processPastedData(elementId, source, blob, blobName, blobType) {
	var divElement = document.getElementById(elementId);
	divElement.innerHTML = "";
	var imgTag = document.createElement("img");

	imgTag.src = source;
	imgTag.onload  = function() {
	     var bb = createJpgImg(imgTag);
	      storeBlobRegistration(elementId, bb);
	      imgTag.setAttribute('style', 'max-width:100%;max-height:100%')
	      divElement.innerHTML = "";
	      divElement.appendChild(imgTag);
	}
}

function storeBlobRegistration(elementId, bb) {
	pastedImages[elementId] = {"blob": bb, "contentType": "image/jpeg"};
}

function enableVisitClassification(){
	var isVisitClassificationReq = healthAuthoPref.visitClassificationReq;
	var cells = document.getElementsByName('visitClassification');
	for(var i=0; i< cells.length; i++){
		cells[i].hidden = !isVisitClassificationReq;
	}
}

var myAutocomp;
function initOnLoadDetails() {
	if (screenid == "ip_registration") {
		initPkgValueCapDialog();
	}
	document.getElementById('CollapsiblePanel1').open = true;
	if (document.getElementById('VisitCollapsiblePanel1'))
		document.getElementById('VisitCollapsiblePanel1').open = true;

	// Custom fields
	getCustomFieldList();

	// Visit Custom fields
	getVisitCustomFieldList();

	// Create custom field list and filter according to display preferences.
	setCustomFieldList();

	// Create visit custom field list and filter according to display preferences.
	setVisitCustomFieldList();


	disableOrEnableRegisterBtns(false);

	// Init referral autocomplete
	referalDoctorAutoComplete('referred_by', 'referaldoctorName', 'referalNameContainer');

	// Init Order Dialog
	var doctors = {
		"doctors": doctorsList
	};


	myAutocomp = Insta.initMRNoAcSearch(contextPath, "mrno", "mrnoAcDropdown", "all", function (type, args) {
		getRegDetailsOnMrnoNoChange();
	}, function (type, args) {
		clearRegDetails();
	}, null, null, null, true);

	//myac.dataRequestEvent = function(ac, query, req) {
	//
	//}
	if (screenid == "ip_registration") {
		populateBedTypes();
		CollapsiblePanel1.open();
	}
	if (document.mainform.regBill != null) {
		if (empty(billingCounterId)) document.mainform.regBill.disabled = true;

		var disable = document.mainform.regBill.disabled;
		document.getElementById("paymentTab").style.display = disable ? 'none' : 'block';
	}

	if (creditBillScreen != "A") {
		if (document.mainform.editBill != null)
			document.mainform.editBill.disabled = true;
	}
	// If mod_adv_ins enabled, init policy auto complete
	if (isModAdvanceIns) {
		policyNoAutoComplete('P', gPatientPolciyNos);
		policyNoAutoComplete('S', gPatientPolciyNos);
	}

	//corporateNoAutoComplete('P', gPatientCorporateIds);	//11.3 optimisation
	//corporateNoAutoComplete('S', gPatientCorporateIds);	//11.3 optimisation

	//nationalNoAutoComplete('P', gPatientNationalIds);		//11.3 optimisation
	//nationalNoAutoComplete('S', gPatientNationalIds);		//11.3 optimisation

	initDoctorDept('');
	onfocus('mrno');
	initLightbox();
	initMlcDialog();
	customDialog();
	visitCustomDialog();
	hotKeys();
	initAutocompletes();
	setGovtPattern();

  /*ajaxDetails = getDetailsAjax();
	policynames = ajaxDetails.policynames;
	companyTpaList = ajaxDetails.companyTpaList;
	networkTypeSponsorIdListMap = ajaxDetails.networkTypeSponsorIdListMap;
  discountPlansJSON =ajaxDetails.discountPlansJSON;
  tpanames = ajaxDetails.tpanames;
  insuCatNames = ajaxDetails.insuCategoryNames;
  insuCompanyDetails = ajaxDetails.insuCompanyDetails;
  categoryJSON = ajaxDetails.categoryWiseRateplans;
  orgNamesJSON = ajaxDetails.orgNameJSON;*/

  ratePlanAjax= getRatePlanAjax();
  categoryJSON = ratePlanAjax.categoryWiseRateplans;
  orgNamesJSON = ratePlanAjax.orgNameJSON;


  // Load rate plan and tpa list according to category selected
	onChangeCategory();

	// Reg Pref. PatientCategory
	if (patientCategory != '' && patientCategory != null) {
		CategoryList();
	}

}

var smartConflict = false; 		//This should be false
//function conflictCheck(self, args){
//	  if(conflictCheckFlag==true) {
//		  var dt=JSON.parse(smartCardDetails);
//		  var cardDOB = dt["dateOfBirth"].substring(0,2) +"-"+ dt["dateOfBirth"].substring(3,5) +"-"+ dt["dateOfBirth"].substring(6,10);
//		  if(args[2].patient_name!=trim(dt["fName"]) || args[2].middle_name!=trim(dt["mName"]) || args[2].last_name!=trim(dt["lName"]) ||
//				  args[2].government_identifier!=dt["idNumber"] || args[2].dob.toString()!=cardDOB) {
//				smartConflict = true;
//				console.log("here1");
//		  }
//		  conflictCheckFlag = false;
//	}
//}

var mvDialog=null;
function initMvPackageDetailsDialog() {
	var dialog = document.getElementById('patientMvDetailsDialog');
	dialog.style.display = 'block';
	mvDialog = new YAHOO.widget.Dialog("patientMvDetailsDialog", {
		width: "700px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	YAHOO.util.Event.addListener('mv_prev', 'click', loadPreviousMvPackageDetails, mvDialog, true);
	YAHOO.util.Event.addListener('mv_next', 'click', loadNextMvPackageDetails, mvDialog, true);
	YAHOO.util.Event.addListener('mv_cancel', 'click', cancelMvDialog, mvDialog, true);

	var escKey = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelMvDialog,
	                                                scope:mvDialog,
	                                                correctScope:true } );


	mvDialog.cfg.setProperty("keylisteners", escKey);
	mvDialog.cancelEvent.subscribe(cancelMvDialog);
	mvDialog.render();
}

function cancelMvDialog() {
	clearMvDialog();
	gIndex = 0;
	mvDialog.hide();
}

function clearMvDialog() {
	var table1 = document.getElementById('pd_mvDialogTable');
	var table2 = document.getElementById('packageComponentsDetails');
	if(table1.rows.length > 0) {
		table1.deleteRow(0);
	}

	if(table2.rows.length > 0) {
		for(var i=table2.rows.length-1;i>=0;i--) {
			table2.deleteRow(i);
		}
	}
}

function showMvDialog(obj) {
	if (mvDialog != null) {
		mvDialog.cfg.setProperty("context", [obj, "tl", "tr"], false);
		loadMultiVisitPackageDetails(gPatMultiVisitComponentDetails,gPatMultiVisitConsumedDetails,gMvPackageIds,gIndex);
		mvDialog.show();
		if(gMvPackageIds.length == 1) {
			document.getElementById('mv_prev').disabled = true;
			document.getElementById('mv_next').disabled = true;
			document.mainform.mv_cancel.focus();
		} else if (gMvPackageIds.length > 1) {
			document.getElementById('mv_prev').disabled = true;
			document.getElementById('mv_next').disabled = false;
			document.mainform.mv_next.focus();
		}
	}
	return false;
}
var gIndex = 0;
function loadMultiVisitPackageDetails(patMultiVisitComponentDetails,patMultiVisitConsumedDetails,mvPackageIds,gIndex) {
	if(!empty(patMultiVisitComponentDetails) && !empty(patMultiVisitConsumedDetails)) {
		var mvPackageId = mvPackageIds[gIndex].package_id;
		var patMvComponentDetails = filterList(patMultiVisitComponentDetails,"package_id",mvPackageId);
		var patMvConsumedDetails = filterList(patMultiVisitConsumedDetails,"package_id",mvPackageId);
		var table1 = document.getElementById('pd_mvDialogTable');
		var table2 = document.getElementById('packageComponentsDetails');
		if(table2.rows.length < 1) {
			var row = table2.insertRow(-1);
			var header1 = document.createElement('th');
			header1.textContent = getString('js.registration.patient.label.item.name');
			row.appendChild(header1);
			var header2 = document.createElement('th');
			header2.textContent = getString('js.registration.patient.label.item.type');
			row.appendChild(header2);
			var header3 = document.createElement('th');
			header3.textContent = getString('js.registration.patient.label.total.qty');
			row.appendChild(header3);
			var header4 = document.createElement('th');
			header4.textContent = getString('js.registration.patient.label.available.qty');
			row.appendChild(header4);
		}
		var row = table1.insertRow(-1);
		var innerCell0 = row.insertCell(-1);
			innerCell0.setAttribute('class','formlabel');
			innerCell0.textContent = 'Package Name:'
		var innerCell1 = row.insertCell(-1);
			innerCell1.setAttribute('class','forminput');
			innerCell1.innerHTML = '<label style="font-style:bold">'+patMvComponentDetails[0].package_name+'</label>';
		var innerCell2 = row.insertCell(-1);
			innerCell2.setAttribute('class','formlabel');
			innerCell2.textContent = 'Package Status:';
		var innerCell3 = row.insertCell(-1);
			innerCell3.setAttribute('class','forminput');
			innerCell3.innerHTML = '<label style="font-style:bold">'+getString('js.registration.patient.label.mvpackage.status')+'</label>';
		for(var i=0;i<patMvComponentDetails.length;i++) {
			var activityId = patMvComponentDetails[i].activity_id;
			var itemType = patMvComponentDetails[i].item_type;
			var packObId = patMvComponentDetails[i].pack_ob_id;
			var activityDesc = patMvComponentDetails[i].activity_description;
			var displayText = activityDesc;
			var consultationTypeId = patMvComponentDetails[i].consultation_type_id;
			var itemTotalQty = patMvComponentDetails[i].activity_qty;
			var len = 35;

			if (len != null && len > 0) {
				if (activityDesc.length > len) {
					displayText = activityDesc.substring(0, len-2) + '...';
				}
				if (displayText == 'Doctor') {
					displayText = displayText + " (" + patMvComponentDetails[i].chargehead_name+ ")";
				}
			}

			var row = table2.insertRow(-1);
			var innerCell0 = row.insertCell(-1);
				innerCell0.innerHTML = '<label>'+displayText+'</label>';
			var innercell1 = row.insertCell(-1);
				innercell1.innerHTML = '<label>'+itemType+'</label>';

			var consumedQty = 0;
			for(var j=0;j<patMvConsumedDetails.length;j++) {
				if(!empty(consultationTypeId)) {
					if (consultationTypeId == patMvConsumedDetails[j].item_id) {
						consumedQty = patMvConsumedDetails[j].consumed_qty;
					}
				} else {
					if (activityId == patMvConsumedDetails[j].item_id) {
						consumedQty = patMvConsumedDetails[j].consumed_qty;
					}
				}
			}
			var itemAvailableQty = itemTotalQty-consumedQty;
			var innercell2 = row.insertCell(-1);
				innercell2.innerHTML = '<label>'+itemTotalQty+'</label>';
			var innercell3 = row.insertCell(-1);
				innercell3.innerHTML = '<label>'+itemAvailableQty+'</label>';
		}
	}
}

function loadPreviousMvPackageDetails() {
	gIndex--;
	clearMvDialog();
	loadMultiVisitPackageDetails(gPatMultiVisitComponentDetails,gPatMultiVisitConsumedDetails,gMvPackageIds,gIndex);
	if(gIndex == 0) {
		document.getElementById('mv_prev').disabled = true;
		document.getElementById('mv_next').disabled = false;
		document.mainform.mv_next.focus();// to make escape key work puting focus on enabaled button.
	} else {
		document.mainform.mv_next.disabled = false;
		document.mainform.mv_prev.disabled =  false;
		document.mainform.mv_prev.focus();
	}
}

function loadNextMvPackageDetails() {
	gIndex++;
	clearMvDialog();
	loadMultiVisitPackageDetails(gPatMultiVisitComponentDetails,gPatMultiVisitConsumedDetails,gMvPackageIds,gIndex);
	if(gIndex == gMvPackageIds.length-1) {
		document.mainform.mv_next.disabled = true;
		document.mainform.mv_prev.disabled =  false;
		document.mainform.mv_prev.focus();// to make escape key work puting focus on enabaled button.
	} else {
		document.mainform.mv_next.disabled = false;
		document.mainform.mv_prev.disabled =  false;
		document.mainform.mv_next.focus();
	}
}


// Set default registration type selection.

function setDefaultSelection() {
	if (screenid == "ip_registration") {
		if (ipDefaultSelection == "M")
			showMrnoSearch();

	} else {
		if (outsideDefaultSelection == "M")
			showMrnoSearch();
	}
}

function showMrnoSearch() {
	document.getElementById('regTyperegd').checked = true;
	document.getElementById('regTyperegd').focus();
	//clearRegDetailsOnLoad();
}

// Create custom field list and filter according to display preferences.
function setCustomFieldList() {
	customFieldList = [{
		display: regPref.custom_list1_show,
		label: custom_list1_name
	}, {
		display: regPref.custom_list2_show,
		label: custom_list2_name
	}, {
		display: regPref.custom_list3_show,
		label: custom_list3_name
	}, {
		display: regPref.custom_list4_show,
		label: custom_list4_name
	}, {
		display: regPref.custom_list5_show,
		label: custom_list5_name
	},{
		display: regPref.custom_list6_show,
		label: custom_list6_name
	},{
		display: regPref.custom_list7_show,
		label: custom_list7_name
	},{
		display: regPref.custom_list8_show,
		label: custom_list8_name
	},{
		display: regPref.custom_list9_show,
		label: custom_list9_name
	},{
		display: regPref.custom_list3_show,
		label: custom_list3_name
	}, {
		display: regPref.custom_field1_show,
		label: custom_field1_label
	}, {
		display: regPref.custom_field2_show,
		label: custom_field2_label
	}, {
		display: regPref.custom_field3_show,
		label: custom_field3_label
	}, {
		display: regPref.custom_field4_show,
		label: custom_field4_label
	}, {
		display: regPref.custom_field5_show,
		label: custom_field5_label
	}, {
		display: regPref.custom_field6_show,
		label: custom_field6_label
	}, {
		display: regPref.custom_field7_show,
		label: custom_field7_label
	}, {
		display: regPref.custom_field8_show,
		label: custom_field8_label
	}, {
		display: regPref.custom_field9_show,
		label: custom_field9_label
	}, {
		display: regPref.custom_field10_show,
		label: custom_field10_label
	}, {
		display: regPref.custom_field11_show,
		label: custom_field11_label
	}, {
		display: regPref.custom_field12_show,
		label: custom_field12_label
	}, {
		display: regPref.custom_field13_show,
		label: custom_field13_label
	}, {
		display: regPref.custom_field14_show,
		label: custom_field14_label
	},{
		display: regPref.custom_field15_show,
		label: custom_field15_label
	},{
		display: regPref.custom_field16_show,
		label: custom_field16_label
	}, {
		display: regPref.custom_field17_show,
		label: custom_field17_label
	}, {
		display: regPref.custom_field18_show,
		label: custom_field18_label
	}, {
		display: regPref.custom_field19_show,
		label: custom_field19_label
	}, {
		display: regPref.passport_no_show,
		label: regPref.passport_no
	}, {
		display: regPref.passport_validity_show,
		label: regPref.passport_validity
	}, {
		display: regPref.passport_issue_country_show,
		label: regPref.passport_issue_country
	}, {
		display: regPref.visa_validity_show,
		label: regPref.visa_validity
	}, {
		display: regPref.family_id_show,
		label: regPref.family_id
	}, {
		display: regPref.nationality_show,
		label: regPref.nationality
	}];

	filterCustomFields();
}

// Create visit custom field list filter according to display preferences.
function setVisitCustomFieldList() {
	visitCustomFieldList = [{
		display: regPref.visit_custom_list1_show,
		label: visit_custom_list1_name
	}, {
		display: regPref.visit_custom_list2_show,
		label: visit_custom_list2_name
	}, {
		display: regPref.visit_custom_field1_show,
		label: visit_custom_field1_name
	}, {
		display: regPref.visit_custom_field2_show,
		label: visit_custom_field2_name
	}, {
		display: regPref.visit_custom_field3_show,
		label: visit_custom_field3_name
	},{
		display: regPref.visit_custom_field4_show,
		label: visit_custom_field4_name
	},{
		display: regPref.visit_custom_field5_show,
		label: visit_custom_field5_name
	},{
		display: regPref.visit_custom_field6_show,
		label: visit_custom_field6_name
	},{
		display: regPref.visit_custom_field7_show,
		label: visit_custom_field7_name
	},{
		display: regPref.visit_custom_field8_show,
		label: visit_custom_field8_name
	}, {
		display: regPref.visit_custom_field9_show,
		label: visit_custom_field9_name
	}];

	displayVisitMoreButton();
	filterVisitCustomFields();
}

var gSelectedDoctorName = null;
var gSelectedDoctorId = null;
var gPatientCategoryRatePlan = null;

/* Clears the patient details: call when reg type is changed, or if mr no is changed */
function clearRegDetails() {
	// Clear previous patient details.
	clearPreviousPatientDetails();

	if (isModAdvanceIns) {
		policyNoAutoComplete('P', gPatientPolciyNos);
		policyNoAutoComplete('S', gPatientPolciyNos);
	}

	//corporateNoAutoComplete('P', gPatientCorporateIds);	//11.3 optimisation
	//corporateNoAutoComplete('S', gPatientCorporateIds);	//11.3 optimisation

	//nationalNoAutoComplete('P', gPatientNationalIds);		//11.3 optimisation
	//nationalNoAutoComplete('S', gPatientNationalIds);		//11.3 optimisation

	if (((allowNewRegistration == 'N') && (roleId != 1) && (roleId != 2))) {
		document.getElementById('regTypenew').checked = false;
		document.getElementById('regTypenew').disabled = true;
		document.getElementById('regTyperegd').checked = true;
	}
	if (document.getElementById('regTypenew').checked == true) {
		document.getElementById('prevVisitTag').style.display = 'none';
		document.getElementById('viewPhoto').style.display = 'none';
		document.getElementById('prevVisitTag').style.display = 'none';
		document.getElementById('patient_name').disabled = false;
		enableDisableDateOfBirthFields('dob', false);

		document.mainform.patient_gender.disabled = false;
		document.getElementById('prvsDoctor').textContent = '';
		document.getElementById('prvsDate').textContent = '';
		if (document.getElementById("autoGenCaseFileDiv") != null)
			document.getElementById("autoGenCaseFileDiv").style.display = 'table-cell';
		if (document.getElementById("caseFileIssuedDiv") != null)
			document.getElementById("caseFileIssuedDiv").style.display = 'none';
		if (screenid != "out_pat_reg" && document.mainform.op_type != null) {
			document.mainform.op_type.selectedIndex = 0;
			document.mainform.op_type.disabled = 0;
		}
	} else {
		document.getElementById('prevVisitTag').style.display = 'block';
		document.getElementById('patient_name').disabled = true;
		if (document.mainform.op_type) document.getElementById('op_type').disabled = false;
	}
	var newReg = isNewReg();
	document.getElementById("mrno").disabled = newReg;

	/* Clear the form values */
	document.mainform.reset();

	//Clear previously selected patient category, rateplan, tpa, plan
	gSelectedPatientCategory = (document.mainform.patient_category_id) ? null : "";
	gSelectedRatePlan = null;
	gSelectedTPA = null;
	gSelectedPlan = null;

	//Clear globally set patient category rate plan.
	gPatientCategoryRatePlan = null;

	//Clear globally set selected doctor.
	gSelectedDoctorName = null;
	gSelectedDoctorId = null;

	//clear the appointment if mrno selection is changed.
	//document.mainform.appointmentId.value = "";
	document.mainform.category.value = "";

	document.mrnoform.mlccheck.checked = false;
	populateMLCTemplates();
	document.mainform.mlc_template.options.selectedIndex = 0;
	onChangeMLCDoc();
	document.mainform.vip_check.checked = false;
	enableVipStatus();
	//Set default rate plan when Patient category is not enabled
	// and single rate plan exists
	if (document.mainform.organization.options.length == 2)
		setSelectedIndex(document.mainform.organization, "ORG0001");
	document.mainform.consFees.value = 0;
	document.mainform.opdocchrg.value = 0;
	if (document.getElementById("docConsultationFees") != null)
		document.getElementById("docConsultationFees").textContent = '';

	/* Blank out the mrno if new reg  */
	document.mrnoform.mrno.value = "";
	document.mainform.mrno.value = "";
	document.mainform.main_visit_id.value = "";
	document.mainform.resource_captured_from.value="register";
	document.mainform.age.value = getString("js.registration.patient.show.age.text");


	isDoctorChange = false;

	if (screenid != "out_pat_reg") {
		// Default the department if hospital has only one department.
		var deptObj = document.mainform.dept_name;
		if (deptObj.options.length == 2) {
			deptObj.options.selectedIndex = 1;
			DeptChange();
		}else
			deptObj.options.selectedIndex = 0;
		document.mainform.doctor_name.removeAttribute("title");
		if (docAutoComp == null || !isDoctorChange)
			initDoctorDept(document.mainform.dept_name.value);
	}

	if (allowMultipleActiveVisits == 'Y'
			&& document.mrnoform.close_last_active_visit != null) {
		document.mrnoform.close_last_active_visit.checked = false;
		setLastVisitToClose();
	}

	if (newReg) {
		document.mainform.salutation.focus();
		setDefaultCity();
	}else {
		document.mrnoform.mrno.focus();
	}

	if (document.mainform.oldmrno != null)
		document.mainform.oldmrno.readOnly = false;
	if (document.mainform.casefileNo != null)
		document.mainform.casefileNo.readOnly = false;
	if (document.mainform.oldRegAutoGenerate != null) {
		document.mainform.oldRegAutoGenerate.disabled = false;
		document.mainform.oldRegAutoGenerate.checked = false;
	}

	// If not new registration, make custom fields readonly based on preferences.
	markCustomFieldsReadonly(document.getElementById('regTyperegd').checked);

	// Bill type default selection and estimate amount.
	checkTypeOfReg();

	// If default patient category exists, load rate plan, TPA & again estimate amount.
	setPatientCategoryDefaultSelection();
	showHideCaseFile();

	if (!isOnLoad)
		ratePlanChange();

	// Set the patient category rate plan globally
	gPatientCategoryRatePlan = document.mainform.organization.value;
	gPatientMobileAccess = document.mainform.mobilePatAccess;
	if(gPatientMobileAccess)
	{
		if(gPatientMobileAccess.value == "Y"){
			$(".patient_phone_star").show();
			$(".patient_email_star").show();
		}else{
			if (isPatientPhoneMandate != 'Y') {
				$(".patient_phone_star").hide();
			}
			if (isPatientEmailMandate != 'Y') {
				$(".patient_email_star").hide();
			}
		}
	}

	clearTodaysAppointmentDetails();
	clearPhoneErrors($('#patient_phone_national'), $('#patient_phone_error'));
}

function clearTodaysAppointmentDetails() {
	var table = document.getElementById('patientDetailsTable');
	if(table && table.rows.length > 0)
		table.deleteRow(0);
	document.getElementById('patDetFieldset').setAttribute('style','height:80px;');
}


var visitType = ""; // Global declaration of visit type

function checkTypeOfReg() {
	var spnsrIndex = getMainSponsorIndex();
	var tpaObj = null;
	if (spnsrIndex == 'P')
		tpaObj = getPrimarySponsorObj();
	else if (spnsrIndex == 'S')
		tpaObj = getSecondarySponsorObj();

	var group = document.mrnoform.group.value;
	if (group == "ipreg") {
		visitType = "I";
		if (document.mainform.bill_type) setBillType(defaultIpBillType);
		document.getElementById('prevVisitTag').style.display = 'block';
		document.getElementById('bedAdvance').innerHTML = "";
		document.getElementById('availabelBeds').innerHTML = "";
	}
	if (group == "opreg") {
		visitType = "O";
		if (document.mainform.bill_type) setBillType(defaultOpBillType);
		if (document.getElementById('regTypenew').checked == true) {
			document.getElementById('prevVisitTag').style.display = 'none';
		} else {
			document.getElementById('prevVisitTag').style.display = 'block';
		}

		if (tpaObj != null) {
			if (tpaObj.value == "" && allowBillNowInsurance == 'true' ||
					(document.mainform.bill_type != null && document.mainform.bill_type.value == 'C')) {
				tpaObj.selectedIndex = 0;
			}
		}
	}
	// If new registration disable close previous active visit check box.
	var newReg = isNewReg();
	if (allowMultipleActiveVisits == 'Y' && document.mrnoform.close_last_active_visit != null)
		document.mrnoform.close_last_active_visit.disabled = newReg;

	//estimateTotalAmount();
}

function setBillType(type) {
	setSelectedIndex(document.mainform.bill_type, type);
	onBillTypeChange();
}

// Function called when bill type is changed in UI
function onChangeBillType() {
	loadPrvPrescripitons = false;
	onBillTypeChange();
	setAllDefaults();
	changeVisitType();
	estimateTotalAmount();
	loadPrvPrescripitons = true;
	loadPreviousUnOrderedPrescriptions();
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
}

function onBillTypeChange() {

	var priTpaObj = getPrimarySponsorObj();
	var secTpaObj = getSecondarySponsorObj();
	var priInsCompObj = getPrimaryInsuObj();
	var secInsCompObj = getSecondaryInsuObj();

	var newType = document.mainform.bill_type.value;
	if (newType == 'P') {
		if (allowBillNowInsurance == 'false') {

			var primarySponsorObj = document.getElementById("primary_sponsor");
			var secondarySponsorObj = document.getElementById("secondary_sponsor");

			if (primarySponsorObj != null) primarySponsorObj.value = "";
			if (secondarySponsorObj != null) secondarySponsorObj.value = "";

			resetPrimarySponsorChange();
			resetSecondarySponsorChange();

			if (priTpaObj != null) {
				priTpaObj.disabled = true;
				priTpaObj.selectedIndex = 0;
			}
			if (priInsCompObj != null) {
				priInsCompObj.selectedIndex = 0;
				priInsCompObj.disabled = true;
			}
			if (secTpaObj != null) {
				secTpaObj.disabled = true;
				secTpaObj.selectedIndex = 0;
			}
			if (secInsCompObj != null) {
				secInsCompObj.selectedIndex = 0;
				secInsCompObj.disabled = true;
			}
			disableOrEnableInsuranceFields(true);
		} else {
			if (priTpaObj != null) {
				priTpaObj.disabled = false;
				priTpaObj.selectedIndex = 0;
			}
			if (priInsCompObj != null) {
				priInsCompObj.removeAttribute("disabled");
				priInsCompObj.selectedIndex = 0;
			}
			if (secTpaObj != null) {
				secTpaObj.disabled = false;
				secTpaObj.selectedIndex = 0;
			}
			if (secInsCompObj != null) {
				secInsCompObj.removeAttribute("disabled");
				secInsCompObj.selectedIndex = 0;
			}
			disableOrEnableInsuranceFields(false);
		}
		if (allocBed == 'Y' && document.mainform.bed_id != undefined)
			changeAllocateSection(true);
	} else {
		disableOrEnableRegisterBtns(false);

		if (priTpaObj != null) {
			priTpaObj.disabled = false;
			priTpaObj.selectedIndex = 0;
		}
		if (priInsCompObj != null) {
			priInsCompObj.removeAttribute("disabled");
			priInsCompObj.selectedIndex = 0;
		}
		if (secTpaObj != null) {
			secTpaObj.disabled = false;
			secTpaObj.selectedIndex = 0;
		}
		if (secInsCompObj != null) {
			secInsCompObj.removeAttribute("disabled");
			secInsCompObj.selectedIndex = 0;
		}
		disableOrEnableInsuranceFields(false);

		if (allocBed == 'Y' && document.mainform.bed_id != undefined)
			changeAllocateSection(false);
	}
	disableRegBillButton();
}

// Set default patient category selection and load rate plan and tpa list accordingly.

function setPatientCategoryDefaultSelection() {
	if (screenid == "ip_registration") {
		if (document.mainform.patient_category_id != null && ipCategoryDefaultSelection != null) {
			setSelectedIndex(document.mainform.patient_category_id, ipCategoryDefaultSelection);
			onChangeCategory();
		}
	} else if(screenid == "out_pat_reg") {
		if (document.mainform.patient_category_id != null && ospCategoryDefaultSelection != null) {
			setSelectedIndex(document.mainform.patient_category_id, ospCategoryDefaultSelection);
			onChangeCategory();
		}
	} else {
		if (document.mainform.patient_category_id != null && opCategoryDefaultSelection != null) {
			setSelectedIndex(document.mainform.patient_category_id, opCategoryDefaultSelection);
			onChangeCategory();
		}
	}
	if (document.mainform.patient_category_id != null) {
		var catObj = document.mainform.patient_category_id;
		// if no default category found in registration preferences,
		// but found only one category in dropdown then default that category.
		if (catObj.value == '' && catObj.options.length == 2) {
			catObj.options.selectedIndex = 1;
			onChangeCategory();
		}
	}
}

/************ Admission Request Scripting ******************/
// Setting all the field values in registration screen from Admission Request Screen

function loadPatientAdmissionRequestInfo() {
	var form = document.mainform;
	if(admissionRequestDetails != null) {
		setSelectedIndex(form.dept_name, admissionRequestDetails.requesting_doctor_dept_id);
		DeptChange();
		form.doctor_name.value = admissionRequestDetails.requsting_docto_name;
		form.doctor.value = admissionRequestDetails.requesting_doc;
		form.ailment.value = admissionRequestDetails.chief_complaint;
	}
}

function loadPatientAdmissionRequestDetails() {
	var form = document.mainform;
	if(admissionRequestDetails != null) {
		var mrno = admissionRequestDetails.mr_no;
		if (!empty(mrno)) {
			document.getElementById('regTyperegd').checked = true;
			clearRegDetails();
			form.mrno.value = mrno;
			document.mrnoform.mrno.value = mrno;
			getRegDetails();
			form.adm_request_id.value = admissionRequestDetails.adm_request_id;
		}
	}
}

function loadPatientDetailsUsingMrno(mrno) {
	var form = document.mainform;
	if (!empty(mrno)) {
		document.getElementById('regTyperegd').checked = true;
		clearRegDetails();
		form.mrno.value = mrno;
		document.mrnoform.mrno.value = mrno;
		getRegDetails();
		if(doctorId != null && doctorId != "") {
		    setSelectedIndex(form.dept_name, docDeptId);
		    DeptChange();
		}
	} else {
		if(patientName != null && patientName != "") {
			var tmpStr = patientName;
			var stringLen = tmpStr.length;
			var valid = true;
			var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
			var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			for (i = 0; i < stringLen; i++){
				var c = tmpStr.charAt(i);
			    if((lowercaseLetters.indexOf(c) != -1) || (uppercaseLetters.indexOf(c) != -1) || ("-" == c) || (" " == c) || ("." == c) || ("'" == c) ) {
			    	continue;
			    } else {
			    	valid = false;
			    }
			}
		    if(valid) {
		    	document.mainform.patient_name.value = patientName;
    	        capWords(document.mainform.patient_name);
		    }
	    }
		if(salutation != null && salutation != "") {
			document.mainform.salutation.value = salutation;
			salutationChange();
		}
		if(patientMName != null && patientMName != "") {
			var tmpStr = patientMName;
			var stringLen = tmpStr.length;
			var valid = true;
			var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
			var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			for (i = 0; i < stringLen; i++){
				var c = tmpStr.charAt(i);
			    if((lowercaseLetters.indexOf(c) != -1) || (uppercaseLetters.indexOf(c) != -1) || ("-" == c) || (" " == c) || ("." == c) || ("'" == c) ) {
			    	continue;
			    } else {
			    	valid = false;
			    }
			}
		    if(valid) {
		    	document.mainform.middle_name.value = patientMName;
    	        capWords(document.mainform.middle_name);
		    }
	    }
		if(patientLName != null && patientLName != "") {
			var tmpStr = patientLName;
			var stringLen = tmpStr.length;
			var valid = true;
			var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
			var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			for (i = 0; i < stringLen; i++){
				var c = tmpStr.charAt(i);
			    if((lowercaseLetters.indexOf(c) != -1) || (uppercaseLetters.indexOf(c) != -1) || ("-" == c) || (" " == c) || ("." == c) || ("'" == c) ) {
			    	continue;
			    } else {
			    	valid = false;
			    }
			}
		    if(valid) {
    	        document.mainform.last_name.value = patientLName;
    	        capWords(document.mainform.last_name);
		    }
	    }
		if(salutation != null && salutation != "") {
			document.mainform.salutation.value = salutation;
			salutationChange();
		}
		if(patientMName != null && patientMName != "") {
			var tmpStr = patientMName;
			var stringLen = tmpStr.length;
			var valid = true;
			var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
			var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			for (i = 0; i < stringLen; i++){
				var c = tmpStr.charAt(i);
			    if((lowercaseLetters.indexOf(c) != -1) || (uppercaseLetters.indexOf(c) != -1) || ("-" == c) || (" " == c) || ("." == c) || ("'" == c) ) {
			    	continue;
			    } else {
			    	valid = false;
			    }
			}
		    if(valid) {
		    	document.mainform.middle_name.value = patientMName;
    	        capWords(document.mainform.middle_name);
		    }
	    }
		if(patientLName != null && patientLName != "") {
			var tmpStr = patientLName;
			var stringLen = tmpStr.length;
			var valid = true;
			var lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
			var uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			for (i = 0; i < stringLen; i++){
				var c = tmpStr.charAt(i);
			    if((lowercaseLetters.indexOf(c) != -1) || (uppercaseLetters.indexOf(c) != -1) || ("-" == c) || (" " == c) || ("." == c) || ("'" == c) ) {
			    	continue;
			    } else {
			    	valid = false;
			    }
			}
		    if(valid) {
    	        document.mainform.last_name.value = patientLName;
    	        capWords(document.mainform.last_name);
		    }
	    }
		if(patientDOB != null && patientDOB != "") {
			var tmpStr = patientDOB.split("-");
			var stringLen = tmpStr.length;
			var valid = true;
			var numbers = "0123456789";
			if((tmpStr.length != 3) || (tmpStr[0].length > 4) || (tmpStr[1].length > 2) || (tmpStr[2].length > 2))
				valid = false;
			if(valid) {
			    for(j = 0; j < 3; j++) {
			        for (i = 0; i < tmpStr[j].length; i++){
				        var c = tmpStr[j].charAt(i);
			            if(numbers.indexOf(c) != -1) {
			    	        continue;
			            } else {
			    	        valid = false;
			    	        break;
			            }
			        }
			    }
			}
		    if(valid) {
    	        document.mainform.dobDay.value = tmpStr[2];
    	        document.mainform.dobMonth.value = tmpStr[1];
    	        document.mainform.dobYear.value = tmpStr[0];
    	        calculateAgeAndHijri();
		    }
		}
		if(patientAge != null && patientAge != "") {
			var valid = true;
			var numbers = "0123456789";
			if(patientAge.length == 1 && numbers.indexOf(patientAge.charAt(0)) == -1)
				valid = false;
			if(numbers.indexOf(patientAge.charAt(patientAge.length-1)) == -1 && patientAge.charAt(patientAge.length-1) != 'D' && patientAge.charAt(patientAge.length-1) != 'M' && patientAge.charAt(patientAge.length-1) != 'Y')
				valid = false;
			for (i = 0; i < patientAge.length-1; i++){
		        var c = patientAge.charAt(i);
	            if(numbers.indexOf(c) != -1) {
	    	        continue;
	            } else {
	    	        valid = false;
	    	        break;
	            }
	        }
			if(valid) {
				if(numbers.indexOf(patientAge.charAt(patientAge.length-1)) != -1) {
					document.mainform.age.value = patientAge;
				} else {
    	            document.mainform.age.value = patientAge.substring(0, patientAge.length-1);
    	            document.mainform.ageIn.value = patientAge.charAt(patientAge.length-1);
				}
			}
		}
	    if(patientPhone != null && patientPhone != "") {
	    	//set country and national number of patient_phone
	    	insertNumberIntoDOM(patientPhone,$("#patient_phone"),$("#patient_phone_country_code"),
	    			$("#patient_phone_national"));
	    }
	    if(patientGender != null && patientGender != "") {
	    	var validValues = "MFCO";
	    	if(patientGender.length == 1 && validValues.indexOf(patientGender) != -1) {
	    		document.mainform.patient_gender.value = patientGender;
            }
	    }
	    if(nKinName != null && nKinName != "") {
	    	document.mainform.relation.value = nKinName;
	    }
	    if(patientAddress != null && patientAddress != "") {
	    	document.mainform.patient_address.value = patientAddress;
	    }
	    if(patientCity != null && patientCity != "") {
	    	$.ajax({
	    		"url" : cpath + "/master/cities/lookup.json",
	    		"data" : {
						"filterText": patientCity,
						"column_name": "city_id",
						"page_size": 10,
						"sort_order": "area_name",
						"contains": "false"
	    		},
	    		success : function(data, status, jqXHR) {
	    			if (!data.dtoList.length) {
	    				return;
	    			}
    				var city = data.dtoList[0];
		    		document.mainform.pat_city_name.value = city["city_name"];
		    		document.mainform.city_id.value = city["city_id"];
		    		document.getElementById("statelbl").textContent = city["state_name"];
		    		document.mainform.state_id.value = city["state_id"];
		    		document.mainform.country_id.value = city["country_id"];
		    		document.getElementById("countrylbl").textContent = city["country_name"];
		    		if(document.getElementById("districtlbl")) {
		    			document.getElementById("districtlbl").textContent = cityStateCountryJSON[i]["district_name"];
		    			document.mainform.district_id.value = cityStateCountryJSON[i]["district_id"];
		    		}
	    		}
	    	});
	    }
	    if(patientEmail != null && patientEmail != "") {
	    	document.mainform.email_id.value = patientEmail;
	    }
	    if(doctorId != null && doctorId != "") {
	        setSelectedIndex(document.mainform.dept_name, docDeptId);
	        DeptChange();
	        document.mainform.doctor.value = doctorId;
	        document.mainform.doctor_name.value = docName;
	        document.mainform.doctor_name.title = docName;
		    setDocRevistCharge(doctorId);
		    hasDoctor = true;
	    }
	}
    if(consDateTime != null && consDateTime != "") {
        d1=new Date(consDateTime);
        if(d1.getMonth() < 9) {
        	var dateString = d1.getDate() + "-0" + (d1.getMonth()+1) + "-" + d1.getFullYear();
        } else {
        	var dateString = d1.getDate() + "-" + (d1.getMonth()+1) + "-" + d1.getFullYear();
        }
        document.mainform.consDate.value = dateString;
    	var hrPart = d1.getHours();
    	var minPart = d1.getMinutes();
    	hrPart = (hrPart < 10) ? '0'+hrPart : hrPart;
    	minPart = (minPart < 10) ? '0'+minPart : minPart;
   		document.mainform.consTime.value = hrPart+":"+minPart;
    }
}

/*********** Scheduler registration scripting *****************************/


function getSchedulerPrimaryResource() {
	if (appointmentDetailsList != null && appointmentDetailsList != '') {
		for (var i = 0; i < appointmentDetailsList.length; i++) {
			var isPrimaryResource = appointmentDetailsList[i].primary_resource;
			if (isPrimaryResource) {
				return appointmentDetailsList[i];
			}
		}
	}
	return null;
}

// Setting all the field values in registration screen

function parsePatientName(patientName) {
	   var salutations = salutationJSON;
	   var salutation = '';
	   var firstName = '';
	   var middleName = '';
	   var lastName = '';
	   var genderValue = '';
	   var nameTokens = patientName.replace(/\s[^A-Z0-9]\s/gi, ' ').trim().split(' ');
	   var tokensLength = nameTokens.length;
	     salutations.forEach((element) => {
	       if (element.salutation.toLowerCase() === nameTokens[0].toLowerCase()) {
	         salutation = element.salutation_id;
	         genderValue = element.gender;
	         nameTokens.splice(0, 1);
	         tokensLength -= 1;
	       }
	     });
	     if (tokensLength === 1) {
	       firstName = nameTokens[0];
	     } else {
	       firstName = nameTokens[0];
	       lastName = nameTokens[tokensLength - 1];
	       middleName = nameTokens.slice(1, tokensLength - 1).join(' ');
	     }
	   return { 'salutation': salutation, 'firstName': firstName, 'middleName': middleName, 'lastName': lastName, 'genderValue': genderValue };
	 }

function loadSchedulerPatientDetails() {
	var form = document.mainform;
	var primaryResource = getSchedulerPrimaryResource();

	if (primaryResource != null) {
		var mrno = primaryResource.mr_no;
		var category = primaryResource.category;
		var appointmentId = primaryResource.appointment_id;

		// Existing patient
		if (!empty(mrno)) {
			document.getElementById('regTyperegd').checked = true;
			clearRegDetails();
			form.mrno.value = mrno;
			document.mrnoform.mrno.value = mrno;
			form.appointmentId.value = appointmentId;
			form.category.value = category;
			getRegDetails();

		}else {
			// New patient
			document.getElementById('regTypenew').checked = true;
			clearRegDetails();
			form.appointmentId.value = appointmentId;
			form.category.value = category;
			var patientNameObject = parsePatientName(primaryResource.patient_name);
			form.salutation.value = patientNameObject.salutation;
			form.patient_name.value = patientNameObject.firstName;
			form.middle_name.value = patientNameObject.middleName;
			form.last_name.value = patientNameObject.lastName;
			form.patient_gender.value = patientNameObject.genderValue;
			insertNumberIntoDOM(primaryResource.patient_contact,$("#patient_phone"),$("#patient_phone_country_code"),
	    			$("#patient_phone_national"));
			form.ailment.value = primaryResource.complaint;

		}

		// For Doctor scheduling, load the doctor details
		if (category == 'DOC') {
			setSelectedIndex(form.dept_name, primaryResource.dept_id);
			DeptChange();
			form.doctor_name.value = primaryResource.resourcename;
			form.doctor.value = primaryResource.res_sch_name;

			// Sets the consultation, op type and gets the doctor charge
			setDocRevistCharge(primaryResource.res_sch_name);
			resetDoctorAndReferralOnRevisit();
			changeVisitType();
			if (!empty(primaryResource.consultation_type_id) && primaryResource.consultation_type_id != 0)
				setSelectedIndex(document.mainform.doctorCharge,primaryResource.consultation_type_id);

			setAppointmentDateTimeAsConsultationDateTime(primaryResource);
			getDoctorCharge();
			loadPreviousUnOrderedPrescriptions();

			gSelectedDoctorName = document.mainform.doctor_name.value;
			gSelectedDoctorId = document.mainform.doctor.value;
			isDoctorChange = true;
			getRegDetails();
		}
	}
	else if (!empty(gAppointmentId) && empty(appointmentDetailsList)) {
		var mrno = apptDetailsExcludingOrders.mr_no;
		var appointmentId = apptDetailsExcludingOrders.appointment_id;

		// Existing patient
		if (!empty(mrno)) {
			document.getElementById('regTyperegd').checked = true;
			clearRegDetails();
			form.mrno.value = mrno;
			document.mrnoform.mrno.value = mrno;
			form.appointmentId.value = appointmentId;
			getRegDetails();

		} else {
			// New patient
			document.getElementById('regTypenew').checked = true;
			clearRegDetails();
			form.appointmentId.value = appointmentId;
			var patientNameObject = parsePatientName(apptDetailsExcludingOrders.patient_name);
			form.salutation.value = patientNameObject.salutation;
			form.patient_name.value = patientNameObject.firstName;
			form.middle_name.value = patientNameObject.middleName;
			form.last_name.value = patientNameObject.lastName;
			form.patient_gender.value = patientNameObject.genderValue;
			insertNumberIntoDOM(apptDetailsExcludingOrders.patient_contact,$("#patient_phone"),$("#patient_phone_country_code"),
	    			$("#patient_phone_national"));
			form.ailment.value = apptDetailsExcludingOrders.complaint;
		}
	}
}

//function loadSchedulerPatientDetailsOnly() {
//
//	if (!empty(gAppointmentId) && empty(appointmentDetailsList)) {
//		var form = document.mainform;
//		var mrno = apptDetailsExcludingOrders.mr_no;
//		var appointmentId = apptDetailsExcludingOrders.appointment_id;
//
//		// Existing patient
//		if (!empty(mrno)) {
//			document.getElementById('regTyperegd').checked = true;
//			clearRegDetails();
//			form.mrno.value = mrno;
//			document.mrnoform.mrno.value = mrno;
//			form.appointmentId.value = appointmentId;
//			getRegDetails();
//
//		} else {
//			// New patient
//			document.getElementById('regTypenew').checked = true;
//			clearRegDetails();
//			form.appointmentId.value = appointmentId;
//			form.patient_name.value = apptDetailsExcludingOrders.patient_name;
//			form.patient_phone.value = apptDetailsExcludingOrders.patient_contact;
//			form.ailment.value = apptDetailsExcludingOrders.complaint;
//		}
//	}
//}

function setAppointmentDateTimeAsConsultationDateTime(primaryResource) {
	var appDate = primaryResource.text_appointment_date;
	document.mainform.consDate.value = appDate;
	var serverDate = getServerDate();
	var appointmentTimeInMillis = primaryResource.appointment_date_time;
	var arrivedTimeInMillis = serverDate.getTime();
	var hrPart = serverDate.getHours();
	var minPart = serverDate.getMinutes();
	hrPart = (hrPart.length == 1) ? '0'+hrPart : hrPart;
	minPart = (minPart.length == 1) ? '0'+minPart : minPart;

	if(appointmentTimeInMillis < arrivedTimeInMillis)
		document.mainform.consTime.value = hrPart+":"+minPart;
	else
		document.mainform.consTime.value = primaryResource.text_appointment_time;
}
/* For tests and Services getting the orderdetails from SchedulerScreen to the order grid of RegistrationScreen.*/
/* Also for channelling appointment getting order details for unordered MVP doctor components */
function loadSchedulerOrders() {
	if ( !loadPrvPrescripitons ){
		return;
	}
	var currentDate = document.getElementById('current_date').value;
	var currentTime = document.getElementById('current_time').value;
	var order = null;
	if (!empty(document.mainform.appointmentId.value) && !empty(appointmentDetailsList)) {
		for (var i = 0; i < appointmentDetailsList.length; i++) {
			var category = appointmentDetailsList[i].category;
			var isPrimary = appointmentDetailsList[i].primary_resource;
			if(isPrimary && (category == 'DOC') && channellingOrders != null && !channel_orders_added) {
				addOrderDialog.getChargeRequest = new Array();
				var index = 0;
				for (var j=0; j<channellingOrders.length; j++) {
				    var itemId = channellingOrders[j].activity_id;
					var quantity = channellingOrders[j].activity_qty;

					var itemType = channellingOrders[j].activity_type;
					if (itemType == null)		// can happen if package has no components, only operation
						continue;
					var itemName = channellingOrders[j].activity_description;
					var cType = channellingOrders[j].consultation_type_id;
					var cTypeDisplay = channellingOrders[j].chargehead_name;
					var multiVisitPackage = true;
					var packObId = channellingOrders[j].pack_ob_id;

					var fromDate = appointmentDetailsList[i].text_appointment_date;
					var fromTime = appointmentDetailsList[i].text_appointment_time;
					var	itemName = channellingOrders[j].doctor_name;

					var conductingDoctor = channellingOrders[j].activity_id;
					var conductingDoctorName = channellingOrders[j].doctor_name;
					var additionalDetails = null;
					var toDate = appointmentDetailsList[i].text_appointment_date;
					var toTime = appointmentDetailsList[i].text_end_appointment_only_time;
					var units = 1;
					/*addOrderDialog.setOrder(itemType,itemId, itemName,
								additionalDetails, cType, cTypeDisplay, fromDate, toDate, fromTime, toTime,
								units, quantity,null,null,null,
								multiVisitPackage,packObId,channellingOrders[i].package_id,null,null,
								conductingDoctor, conductingDoctorName);*/
					var order = {itemType: itemType,
							itemId:     itemId,
							itemName:   itemName,
							presDate:   currentDate + ' ' + currentTime,
							fromDate:   fromDate,
							toDate:     toDate,
							fromTime:   fromTime,
							toTime:     toTime,
							from:       fromDate + ' ' + fromTime,
							to:         toDate + ' ' + toTime,
							units:      '',
							quantity:   quantity,
							remarks:    channellingOrders[j].activity_remarks,
							chargeType: cType,           chargeTypeDisplay: cTypeDisplay,
							presDoctorName:   	appointmentDetailsList[i].presc_doctor, presDoctorId: appointmentDetailsList[i].presc_doc_id,
							addTo:				'orderTable0',
							condDoctorName:   	conductingDoctorName,
							condDoctorId: conductingDoctor,
        					multiVisitPackage : multiVisitPackage,
							packObId : packObId,
							packageId : channellingOrders[j].package_id,
							mandateTestAdditionalInfo : channellingOrders[j].mandate_additional_info,
							additionalTestInfo : channellingOrders[i].additional_info_reqts
						}
					package_id = channellingOrders[j].package_id;
					addOrderDialog.getCharge(order, true);
					channel_orders_added = true;
				}
			}
			// Test and Service which are primary resources need to be ordered.
			// Equipment is secondary resource for these scheduling categories (no need to be ordered).
			if (isPrimary && (category == 'DIA' || category == 'SNP')) {
				var schRemarks = (category == 'DIA') ? "Scheduler Test" : ((category == 'SNP') ? "Scheduler Service" : "");
				order = {
					itemType: appointmentDetailsList[i].item_type,
					itemId: appointmentDetailsList[i].res_sch_name,
					itemName: appointmentDetailsList[i].central_resource_name,
					presDate: currentDate + ' ' + currentTime,
					fromDate: currentDate,
					toDate: currentDate,
					fromTime: currentTime,
					toTime: currentTime,
					units: '',
					quantity: 1,
					preAuthNo : '',
					preAuthModeNo : '0',
					from: currentDate + ' ' + currentTime,
					to: currentDate + ' ' + currentTime,
					remarks: schRemarks,
					presDoctorName: appointmentDetailsList[i].presc_doctor,
				    presDoctorId: appointmentDetailsList[i].presc_doc_id,
					addTo: 'orderTable0',
					mandateTestAdditionalInfo : appointmentDetailsList[i].mandate_additional_info,
					additionalTestInfo : appointmentDetailsList[i].additional_info_reqts
				}
				addOrderDialog.getCharge(order, true);
			}
		}
	}
}

/***************************** Scheduler script ends ***********************************/

/* Function to populate 'ORG0001' i.e GENERAL bed types,
 * called on load and when the bed_type selected value is empty.
 */

function populateBedTypes() {
	var bedTypeObj = document.mainform.bed_type;
	loadSelectBox(bedTypeObj, bedTypesList, 'bedtype', 'bedtype', getString("js.registration.patient.commonselectbox.defaultText"));
}

/* Function to populate all wards,
 * called on load and when the ward_id selected value is empty.
 */

function populateWards() {
	var wardObj = document.mainform.ward_id;
	loadSelectBox(wardObj, wardsList, 'ward_name', 'ward_no', getString("js.registration.patient.commonselectbox.defaultText"));
}

function changeAllocateSection(value) {
	document.mainform.bed_id.disabled = value;
	document.mainform.duty_doctor_id.disabled = value;
	document.mainform.daycare_status.checked = false;
	populateBedCharge();
	document.mainform.daycare_status.disabled = value;
	dutyDoctorMand = !value;
	if (dutyDoctorReq == 'I') getICUStatus();
	else if (dutyDoctorReq == 'A') dutyDoctorMand = true;
	else if (dutyDoctorReq == 'N') dutyDoctorMand = false;
}

/* Function to load bed type related wards.
 * If bed type exisits in a single ward, the ward is defaulted and
 * bed names for the bed type and ward are loaded.
 */

function onBedTypeChange() {
	var bedTypeObj = document.mainform.bed_type;
	var wardObj = document.mainform.ward_id;
	var selectedbedtype = bedTypeObj.value;
	var selectedward = wardObj.value;

	if (selectedbedtype == '' || selectedward == '') {
		document.getElementById('bedAdvance').innerHTML = "";
		//document.getElementById('initialAmount').innerHTML = "";
		document.getElementById('ipcreditlimit').value = "";
		document.getElementById('availabelBeds').innerHTML = "";
		if (document.getElementById('estimateAmount') != null)
			document.getElementById('estimateAmount').value = document.getElementById('opIpcharge').value;
		if (document.getElementById('estimtAmount') != null)
			document.getElementById('estimtAmount').innerHTML = document.getElementById('opIpcharge').value;
	}

	if (selectedbedtype == '') {
		populateBedTypes();
		populateWards();
		setSelectedIndex(bedTypeObj, '');
		setSelectedIndex(wardObj, '');
	}

	if (selectedbedtype != '') getWardNames();

	// Get the defaulted ward if selected.
	selectedward = wardObj.value;
	selectedbedtype = bedTypeObj.value;

	if (document.mainform.bed_id) {
		if (selectedbedtype != '' && selectedward != '') getBedNames();
		else {
			var bedObj = document.mainform.bed_id;
			bedObj.length = 1;
			bedObj.options[bedObj.length - 1].text = getString("js.registration.patient.commonselectbox.defaultText");
			bedObj.options[bedObj.length - 1].value = "";
		}
		setSelectedIndex(document.mainform.bed_id, '');
	}

	if (selectedbedtype != '' && selectedward != '') {
		populateBedCharge();
	}
	if(document.getElementById('duty_doc_star')!= null){
		if((selectedbedtype == 'ICU' && dutyDoctorReq == 'I') || dutyDoctorReq == 'A')
			document.getElementById('duty_doc_star').style.visibility = 'visible';
		else
			document.getElementById('duty_doc_star').style.visibility = 'hidden';
	}

} //onBedTypeChange()

/* Function to load ward related bed types.
 * If ward has single bed type, the bed type is defaulted and
 * bed names for the bed type and ward are loaded.
 */

function onWardChange(reset) {
	var bedTypeObj = document.mainform.bed_type;
	var wardObj = document.mainform.ward_id;

	var selectedbedtype = bedTypeObj.value;
	var selectedward = wardObj.value;
  var flag = 0;
    var gender = document.mainform.patient_gender.value;
    var result = wardsList.filter(ward => ward.ward_no === selectedward);
    if(result !== undefined && result.length > 0){
    if(result[0].allowed_gender !== 'ALL' && result[0].allowed_gender !== gender && !(gender === 'C' || gender === 'O' || gender === 'N')) {
      var replaceWord;
      if(result[0].allowed_gender === 'F'){
         replaceWord = 'Female';
      }
      if(result[0].allowed_gender === 'M'){
       replaceWord = 'Male';
      }
      alert(getString('js.registration.patient.not.valid.ward.for.gender', replaceWord));
      selectedward = '';
      flag = 1;
    }
    }
	if (selectedbedtype == '' || selectedward == '') {
		document.getElementById('bedAdvance').innerHTML = "";
		//document.getElementById('initialAmount').innerHTML = "";
		document.getElementById('ipcreditlimit').value = "";
		document.getElementById('availabelBeds').innerHTML = "";
		if (document.getElementById('estimateAmount') != null)
			document.getElementById('estimateAmount').value = document.getElementById('opIpcharge').value;
		if (document.getElementById('estimtAmount') != null)
			document.getElementById('estimtAmount').innerHTML = document.getElementById('opIpcharge').value;
	}

	if (selectedward == '') {
		populateBedTypes();
		populateWards();
		setSelectedIndex(bedTypeObj, '');
		setSelectedIndex(wardObj, '');
	}

	if (selectedward != '') {
		document.mainform.ipwardname.value = selectedward;
		getWardBedTypes();
	}

	// Get the defaulted bed type if selected.
	selectedbedtype = bedTypeObj.value;
	selectedward = wardObj.value;

	if (document.mainform.bed_id && flag === 0 && reset !== 1) {
		if (selectedbedtype != '' && selectedward != '') getBedNames();
		else {
			var bedObj = document.mainform.bed_id;
			bedObj.length = 1;
			bedObj.options[bedObj.length - 1].text = getString("js.registration.patient.commonselectbox.defaultText");
			bedObj.options[bedObj.length - 1].value = "";
		}
		setSelectedIndex(document.mainform.bed_id, '');
	}

	if (selectedbedtype != '' && selectedward != '') {
		populateBedCharge();
	}
	if(flag == 1){
	  if(document.mainform.bed_id !== undefined)
	    setSelectedIndex(document.mainform.bed_id, '');
	  return false;
	}
	else {
    return true;
	}

} //onWardChange()

function onGenderChange(){
  if(screenid === "ip_registration"){
   document.mainform.ward_id.value = '';
   document.mainform.bed_type.value='';
  }
}

/* Function to get Ward related bed types. */

function getWardBedTypes() {
	var bedTypeObj = document.mainform.bed_type;
	var wardObj = document.mainform.ward_id;
	var selectedbedtype = bedTypeObj.value;
	var selectedward = wardObj.value;

	if (document.mainform.ipfreebeds) document.mainform.ipfreebeds.value = 0;
	document.getElementById('availabelBeds').innerHTML = "0";

	// Empty bed types
	bedTypeObj.length = 1;
	bedTypeObj.options[bedTypeObj.length - 1].text = getString("js.registration.patient.commonselectbox.defaultText");
	bedTypeObj.options[bedTypeObj.length - 1].value = "";

	if (selectedward != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getBedTypesForWard&selectedward=' + selectedward;
		var freebeds = 0;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var bedtypes =" + ajaxobj.responseText);
					if (bedtypes != null && bedtypes != '') {
						var len = bedtypes.length;
						for (var i = 0; i < len; i++) {
							var record = bedtypes[i];
							bedTypeObj.length = bedTypeObj.length + 1;
							bedTypeObj.options[bedTypeObj.length - 1].text = record.bed_type;
							bedTypeObj.options[bedTypeObj.length - 1].value = record.bed_type;
							freebeds = freebeds + record.freebeds;
						}
					}
					if (freebeds == 0) {
						document.mainform.ipfreebeds.value = 0;
						document.getElementById('availabelBeds').innerHTML = "0";
						showMessage("js.registration.patient.no.free.beds");
						if (isAllocBed) return false;
					} else {
						document.mainform.ipfreebeds.value = freebeds;
						document.getElementById('availabelBeds').innerHTML = freebeds;
					}
				}
			}
		}
	} else {
		document.mainform.ipfreebeds.value = 0;
		document.getElementById('availabelBeds').innerHTML = "0";
		bedTypeObj.selectedIndex = 0;
		wardObj.selectedIndex = 0;
		bedTypeObj.length = 1;
	}

	if (bedTypeObj.length == 2)
		setSelectedIndex(bedTypeObj, bedTypeObj.options[1].value);
	else setSelectedIndex(bedTypeObj, selectedbedtype);

	if (dutyDoctorReq == 'I') getICUStatus();
	else if (dutyDoctorReq == 'A') dutyDoctorMand = true;
	else if (dutyDoctorReq == 'N') dutyDoctorMand = false;
}

/* Function to get bed type related wards. */

function getWardNames() {
	var bedTypeObj = document.mainform.bed_type;
	var wardObj = document.mainform.ward_id;
	var selectedward = wardObj.value;
	var selectedbedtype = bedTypeObj.value;

	if (document.mainform.ipfreebeds)
		document.mainform.ipfreebeds.value = 0;
	document.getElementById('availabelBeds').innerHTML = "0";

	// Empty wards
	wardObj.length = 1;
	wardObj.options[wardObj.length - 1].text = getString("js.registration.patient.commonselectbox.defaultText");
	wardObj.options[wardObj.length - 1].value = "";

	if (selectedbedtype != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getWardnamesForBedType&selectedbedtype=' + encodeURIComponent(selectedbedtype);
		var freebeds = 0;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var wards =" + ajaxobj.responseText);
					if (wards != null && wards != '') {
						var len = wards.length;
						for (var i = 0; i < len; i++) {
							var record = wards[i];
							wardObj.length = wardObj.length + 1;
							wardObj.options[wardObj.length - 1].text = record.ward_name;
							wardObj.options[wardObj.length - 1].value = record.ward_no;
							freebeds = freebeds + record.freebeds;
						}
					}
					if (freebeds == 0) {
						document.mainform.ipfreebeds.value = 0;
						document.getElementById('availabelBeds').innerHTML = "0";
						showMessage("js.registration.patient.no.free.beds");
						if (isAllocBed) return false;
					} else {
						document.mainform.ipfreebeds.value = freebeds;
						document.getElementById('availabelBeds').innerHTML = freebeds;
					}
				}
			}
		}
	} else {
		document.mainform.ipfreebeds.value = 0;
		document.getElementById('availabelBeds').innerHTML = "0";
		bedTypeObj.selectedIndex = 0;
		wardObj.selectedIndex = 0;
		wardObj.length = 1;
	}

	if (wardObj.options.length == 2) setSelectedIndex(wardObj, wardObj.options[1].value);
	else setSelectedIndex(wardObj, selectedward);

	if (dutyDoctorReq == 'I') getICUStatus();
	else if (dutyDoctorReq == 'A') dutyDoctorMand = true;
	else if (dutyDoctorReq == 'N') dutyDoctorMand = false;
}


/*
 * Displays bed charge for the selected bed type and organization
 * (selected organization for corporate patient type, else general organization)
 */

function populateBedCharge() {
	var chargeType = 'D';
	if (document.getElementById("daycare_status") && document.getElementById("daycare_status").checked) chargeType = 'H';

	var selectedbedtype = document.mainform.bed_type.value;

	var orgObj = document.mainform.organization;
	var organization = 'ORG0001';

	if (orgObj.value == '') organization = "ORG0001";
	else organization = orgObj.value;
	gPatientDepositAmt = gPatientDepositAmt == null ? 0.00 : gPatientDepositAmt;
	// ajax call for getting bed charges
	var url = cpath + '/pages/registration/regUtils.do?_method=getBedChargesJson&organization=' + organization + '&bedtype=' + selectedbedtype + '&screenid=' + screenid;

	if (cachedBedCharges.hasOwnProperty(organization + selectedbedtype)) {
		return parseBedChargesResponse(cachedBedCharges[organization + selectedbedtype], chargeType);
	}
	cachedBedCharges[organization + selectedbedtype] = null;

	$.ajax({
		"url": url,
		"type" : "POST",
		"dataType": "json",
		"success" : function(responseJson, status, xhr) {
			cachedBedCharges[organization + selectedbedtype] = responseJson;
			parseBedChargesResponse(responseJson, chargeType);
		}
	});
}

function parseBedChargesResponse(responseJson, chargeType) {
	if (!responseJson) {
		return;
	}
	$.each(responseJson, function(idx, item) {
		var bedAmount = item.bed_charge - item.bed_charge_discount
						+ item.nursing_charge - item.nursing_charge_discount
						+ item.maintainance_charge - item.maintainance_charge_discount
						+ item.duty_charge - item.duty_charge_discount;
		var min_charge = item.daycare_slab_1_charge - item.daycare_slab_1_charge_discount;
		var slab1_charge = item.daycare_slab_2_charge - item.daycare_slab_2_charge_discount;
		var slab2_charge = item.daycare_slab_3_charge - item.daycare_slab_3_charge_discount;
		if (chargeType == 'H') bedAmount = item.hourly_charge - item.hourly_charge_discount;
		if (luxuryTaxApplicableOn == 'B') bedAmount += (item.luxary_tax * (item.bed_charge - item.bed_charge_discount)) / 100;
		else bedAmount += (item.luxary_tax * (bedAmount)) / 100;

		document.mainform.ipbedavance.value = bedAmount;

		if (chargeType == 'H') {
			var bedChargeinnerHTML = bedAmount != 0 ? ('-' + formatAmountPaise(getPaise(bedAmount)) + ' Per Hour') : '';
			document.getElementById('bedAdvance').innerHTML = formatAmountPaise(getPaise(min_charge))
															+ '/' + formatAmountPaise(getPaise(slab1_charge)) + '/'
															+ formatAmountPaise(getPaise(slab2_charge)) + bedChargeinnerHTML;
		} else {
			document.getElementById('bedAdvance').innerHTML = formatAmountPaise(getPaise(bedAmount)) + ' Per Day';
		}

		//document.getElementById('initialAmount').innerHTML = formatAmountPaise(getPaise(item.initial_payment - item.initial_payment_discount));

		var initialPaymentAmt = formatAmountPaise(getPaise(item.initial_payment - item.initial_payment_discount));
		document.getElementById('ipcreditlimit').value = formatAmountValue(parseFloat(initialPaymentAmt)+parseFloat(gPatientDepositAmt));

		document.getElementById("credit_limit_help").title = "Available Credit Limit = Credit Limit + Available Deposits - Patient Dues.\n"+"Credit Limit : "+initialPaymentAmt+"\n"+"Available Deposits : "+formatAmountValue(gPatientDepositAmt)+"\n"+"Patient Dues : 0.00";

		var display = displayEstimatedTotalAmountTable(organization);
		if (display) {
			document.getElementById("bedAdvance").style.display = 'block';
			//document.getElementById("initialAmount").style.display = 'block';
		}else {
			document.getElementById("bedAdvance").style.display = 'none';
			//document.getElementById("initialAmount").style.display = 'none';
		}
	});
	estimateTotalAmount();
}

/* Function to get bed names when bed type and ward is selected */

function getBedNames() {
	var bedTypeObj = document.mainform.bed_type;
	var wardObj = document.mainform.ward_id;
	var selectedward = wardObj.value;
	var selectedbedtype = bedTypeObj.value;

	var url = cpath + '/pages/registration/regUtils.do?_method=getBednamesForWard&selectedward=' + selectedward + '&selectedbedtype=' + encodeURIComponent(selectedbedtype);
	var bedObj = document.mainform.bed_id;

	bedObj.length = 1;
	bedObj.options[bedObj.length - 1].text = getString("js.registration.patient.commonselectbox.defaultText");
	bedObj.options[bedObj.length - 1].value = "";
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var beds =" + ajaxobj.responseText);
				if (beds != null && beds != '') {
					var len = beds.length;
					for (var i = 0; i < len; i++) {
						var record = beds[i];
						bedObj.length = bedObj.length + 1;
						bedObj.options[bedObj.length - 1].text = record.bed_name;
						bedObj.options[bedObj.length - 1].value = record.bed_id;
					}
				}
			}
		}
	}
}

var dutyDoctorMand = false;

function getICUStatus() {
	var bedTypeObj = document.mainform.bed_type;
	var selectedbedtype = bedTypeObj.value;
	if (selectedbedtype != '') {
		var reqObject = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getICUStatus&bed_type=' + selectedbedtype;

		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				if (eval(reqObject.responseText) == "Y") {
					dutyDoctorMand = true;
				} else {
					dutyDoctorMand = false;
				}
			}
		}
	}
	return false;
}

function isNewReg() {
	var radios = document.mrnoform.regType;
	if (radios == null) return false;
	for (var i = 0; i < radios.length; i++) {
		if (radios[i].checked == true) {
			var value = radios[i].value;
			return value == 'new';
		}
	}
	return false;
}

// Function called when Insurance Company is changed in UI

function onLoadTpaList(spnsrIndex) {
    loadTpaList(spnsrIndex);
    tpaChange(spnsrIndex);
    insuCatChange(spnsrIndex);
	  RatePlanList();
	  ratePlanChange();
	//setSelectedDateForCorpInsurance();
}

function loadInsuranceCompList(spnsrIndex) {

	var categoryId = '';
	if (document.mainform.patient_category_id)
		categoryId = document.mainform.patient_category_id.value;

	var tpaIdObj = null;
	var tpaIdWrapperObj = null;
	var insuCompIdObj = null;

	if (spnsrIndex == 'P') {
		tpaIdObj = getPrimarySponsorObj();
		tpaIdWrapperObj = document.getElementById("primary_sponsor_wrapper");
		insuCompIdObj = getPrimaryInsuObj();

	}else if (spnsrIndex == 'S') {
		tpaIdObj = getSecondarySponsorObj();
		tpaIdWrapperObj = document.getElementById("secondary_sponsor_wrapper");
		insuCompIdObj = getSecondaryInsuObj();
	}

	// Proceed with processing only if insuCompIdObj is not null
	if (insuCompIdObj == null)
		return;

	var insCompList = insuCompanyDetails; // the default set: all Ins Comps
	var defaultInsComp = "";
	var visitType = screenid == 'ip_registration'? 'i': 'o';

	if (categoryId != '') {
		// category is enabled, the list of Insurance Comps. is restricted
		for (var i = 0; i < categoryJSON.length; i++) {
			var item = categoryJSON[i];
			if (categoryId == item.category_id) {
				if (visitType == 'i') {
					if (!empty(item.ip_allowed_insurance_co_ids) && item.ip_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.ip_allowed_insurance_co_ids.split(',');
						var ip_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++)
						ip_allowedInsComps.push(findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]));
						// override the insCompList with allowed Ins Comps.
						insCompList =  !empty(ip_allowedInsComps) ? ip_allowedInsComps : insCompList;
					}
					if (spnsrIndex == 'P')
						defaultInsComp = item.primary_ip_insurance_co_id;
					else if (spnsrIndex == 'S')
						defaultInsComp = item.secondary_ip_insurance_co_id;
					break;
				 } else {
				 	if (!empty(item.op_allowed_insurance_co_ids) && item.op_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.op_allowed_insurance_co_ids.split(',');
						var op_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++)
						op_allowedInsComps.push(findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]));
						// override the insCompList with allowed Ins Comps.
						insCompList =  !empty(op_allowedInsComps) ? op_allowedInsComps : insCompList;
					}
					if (spnsrIndex == 'P')
						defaultInsComp = item.primary_op_insurance_co_id;
					else if (spnsrIndex == 'S')
						defaultInsComp = item.secondary_op_insurance_co_id;
					break;
				}
			}
		}
	}
	var item1;
	if(insuCompIdObj != null && !empty(insuCompIdObj.value)){
		item1 = findInList(insuCompanyDetails, "insurance_co_id", insuCompIdObj.value);
	}
	// Empty Ins Comps in ins company dropdown
	var index = 0;
	if (insuCompIdObj != null) {
		insuCompIdObj.length = 1;
		insuCompIdObj.options[index].text = "-- Select --";
		insuCompIdObj.options[index].value = "";
	}
	//insCompList=sortByKey(insCompList,"insurance_co_name");
	//insuCompanyDetails=sortByKey(insuCompanyDetails,"insurance_co_name");
	insCompList=sortByKey(insCompList,"insurance_co_id");
	insuCompanyDetails=sortByKey(insuCompanyDetails,"insurance_co_id");

	//Removed for Registration performance Improvement in 11.3, However we can probably do away with this check itself. The reasons:
	//1) If the companies associated with a patient category is *, it means all so no check is required.
	//2) If the companies associated are a selected few, we have seen that the insurance companies that are so associated with a patient category cannot be deactivated. Therefore, again the items in insCompList will be part of insuCompanyDetails.
	//This means that the loop used to check for this is not required
	// Add all the allowed InsComps for patient category and insurance company.
	//var startIndex = 0;
	// insuCompIdObjOptions = array containing insurance companies from insCompList that exist in insuCompanyDetails
	// insCompList = insurance companies allowed for the patient category
	// insuCompanyDetails = insurance companies that are active
	/*var insuCompIdObjOptions = [];
	for (var i = 0; i < insCompList.length; i++) {
		var exists = false;
		var item = insCompList[i];
		//for (var k = 0; k < insuCompanyDetails.length; k++) {
		for (var k = startIndex; k < insuCompanyDetails.length; k++) {
			var insItem = insuCompanyDetails[k];
			if (!empty(item) && !empty(insItem) && (item.insurance_co_id == insItem.insurance_co_id)) {
				exists = true;
				startIndex = k + 1; // Since it is sorted, the next match will only be after the current match
				insuCompIdObjOptions.push(item);
				index++;
				break;
			}
		}*/
		// Code moved out of the loop to sort by name before adding to the dropdown
		/*if (exists) {
			index++;
			if (insuCompIdObj != null) {
				insuCompIdObj.length = index + 1;
				insuCompIdObj.options[index].text = item.insurance_co_name;
				insuCompIdObj.options[index].value = item.insurance_co_id;
			}
		}*/
	//}

	//Add to the dropdown
	////Changed for Registration performance Improvement in 11.3
	if (insuCompIdObj != null){
		insuCompIdObj.length = insCompList.length + 1;
		for (i=0; i < insCompList.length; i++){
			var insCo = insCompList[i];
			insuCompIdObj.options[i+1].text = insCo.insurance_co_name;
			insuCompIdObj.options[i+1].value = insCo.insurance_co_id;
		}
	}

	// Default the insurance company if that is the only one
	if (insCompList.length == 1) {
		defaultInsComp = insCompList[0].insurance_co_id;
	}


	if (allowBillNowInsurance == 'true' || (document.mainform.bill_type != null && document.mainform.bill_type.value == 'C')) {
		// if there is a default ins. company for patient category, set it
		if (insuCompIdObj != null) {
			if (!empty(defaultInsComp)) {
				tpaIdWrapperObj.checked = true;//add wrapper here
			setSelectedIndex(insuCompIdObj, defaultInsComp);

			} else if (document.mainform.patient_category_id && insCompList.length == 1) {
				setSelectedIndex(insuCompIdObj, insCompList[0].insurance_co_id);
			}
			insuCompIdObj.removeAttribute("disabled");

			if(null != item1 && !empty(item1)){
				setSelectedIndex(insuCompIdObj, item1.insurance_co_id);
			}
		}

	} else {
		if (insuCompIdObj != null) {
			setSelectedIndex(insuCompIdObj, "");
			insuCompIdObj.disabled = true;
		}
	}
}

/*
 * There are 4 different ways to load the list of TPAs based on:
 *  - Whether Category is enabled or not
 *  - Whether insurance module is enabled or not.
 * If category is enabled, the list of TPAs for new cases is limited to what is allowed by
 * the category. If insurance module is enabled, we can connect to an existing case, or create
 * a new case for any of the (allowed) TPAs.
 */

/* Function called in 4 places, when Insurance company is changed in UI
	(or) existing patient details are loaded (loadInsurancePolicyDetails())
	(or) Member ship autocomplete is changed (loadPolicyDetails)
	(or) patient category is changed (onChangeCategory())
*/

function loadTpaList(spnsrIndex) {


	var loadTpaOnInsChange = (isModAdvanceIns || isModInsurance);

	var tpaObj = null;
	var tpaWrapperObj = null;
	var insuCompObj = null;
	var planTypeObj = null;
	var planObj = null;
	var tpaNameObj=null;
	var memberIdObj=null;

	if (spnsrIndex == 'P') {
		tpaObj = getPrimarySponsorObj();
		tpaNameObj=getPrimarySponsorNameObj();
		insuCompObj = getPrimaryInsuObj();
		planTypeObj = getPrimaryPlanTypeObj();
		planObj = getPrimaryPlanObj();
		memberIdObj = getPrimaryMemberIdObj();
		tpaWrapperObj = document.getElementById("primary_sponsor_wrapper");

	}else if (spnsrIndex == 'S') {
		tpaObj = getSecondarySponsorObj();
		tpaNameObj=getSecondarySponsorNameObj();
		insuCompObj = getSecondaryInsuObj();
		planTypeObj = getSecondaryPlanTypeObj();
		planObj = getSecondaryPlanObj();
		memberIdObj = getSecondaryMemberIdObj();
		tpaWrapperObj = document.getElementById("secondary_sponsor_wrapper");
	}

	var categoryId = '';
	if (document.mainform.patient_category_id)
		categoryId = document.mainform.patient_category_id.value;

	var insCompanyId = '';
	if (insuCompObj != null)
		insCompanyId = insuCompObj.value;

	var planType = '';
	if (planTypeObj != null) {
		planType = planTypeObj.value;

		// Empty plan types in plan type dropdown
		var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
		planTypeObj.options.length = 1;
		planTypeObj.options[0] = optn;
	}

	var plan = '';
	if (planObj != null) {
		plan = planObj.value;

		// Empty plans in plan dropdown
		var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
		planObj.options.length = 1;
		planObj.options[0] = optn;
	}

	var mainSpnsrIndex = spnsrIndex; // getMainSponsorIndex();
	if (memberIdObj != null)
		memberIdObj.value = "";

	var mainInsuObj = null;
	var mainInsCompanyId = '';
	if (mainSpnsrIndex == 'P') mainInsuObj = getPrimaryInsuObj();
	else if (mainSpnsrIndex == 'S') mainInsuObj = getSecondaryInsuObj();
	if (mainInsuObj != null)
		mainInsCompanyId = mainInsuObj.value;

	// gIsInsurance - advance insurance and company not empty, variable for first of category check & display patient amounts.
	if (isModAdvanceIns && mainInsCompanyId != '') gIsInsurance = true;
	else gIsInsurance = false;

	if (!gIsInsurance) {
		if (planTypeObj != null) {
			planTypeObj.selectedIndex = 0;
			insuCatChange(spnsrIndex);
		}
		if (document.mainform.op_type != null
				&& (document.mainform.op_type.value != "F" && document.mainform.op_type.value != "D"))
			if (tpaObj != null) {
				tpaObj.disabled = false;
				tpaWrapperObj.checked = true;
			}
		//if (insuCompObj != null)
			//setSelectedIndex(insuCompObj, "");
	}

		if (empty(mainSpnsrIndex) || mainSpnsrIndex == spnsrIndex) {

			var j = 2;
			// Load plan types related to insurance company
			for (var i = 0; i < insuCatNames.length; i++) {
				var ele = insuCatNames[i];
				if (ele.insurance_co_id == mainInsCompanyId && ele.status == "A") {
					var hasPlanMappedToSponsor = false;
					if (typeof networkTypeSponsorIdListMap !== 'undefined') {
						hasPlanMappedToSponsor = true;
					}
					var sponsorIdList = typeof networkTypeSponsorIdListMap !== 'undefined'
						? networkTypeSponsorIdListMap[ele.category_id] : [];
					for (var k = 0; k < sponsorIdList.length; k++) {
						if (!tpaObj || !tpaObj.value) {
							break;
						}
						if (sponsorIdList[k] === null || sponsorIdList[k] === '' || sponsorIdList[k] === tpaObj.value) {
							hasPlanMappedToSponsor = true;
							break;
						}
					}
					if (!hasPlanMappedToSponsor) {
						continue;
					}
					var optn = new Option(ele.category_name, ele.category_id);
					planTypeObj.options.length = j;
					planTypeObj.options[j-1] = optn;
					j++;
					planType = ele.category_id;
				}
			}
		}

	var ratePlanObj = document.mainform.organization;
	var selectedRatePlan = ratePlanObj.value;
	var insCompDefaultRatePlan = '';

	var selectedIns = findInList(insuCompanyDetails, "insurance_co_id", mainInsCompanyId);
	if (!empty(selectedIns) && !empty(selectedIns.default_rate_plan)) {
		insCompDefaultRatePlan = selectedIns.default_rate_plan;
	}
	if (!empty(insCompDefaultRatePlan))
		setSelectedIndex(ratePlanObj, insCompDefaultRatePlan);
	else
		setSelectedIndex(ratePlanObj, selectedRatePlan);

	var insCompTpaList = filterList(companyTpaList, 'insurance_co_id', insCompanyId);
	if (empty(insCompTpaList)) {
		insCompTpaList = tpanames;
	}

	// Loading TPAs follows....

	// For revisit if tpa exists, need to set that back after TPAs are loaded.
	var previousTpa = (tpaObj != null) ? tpaObj.value : "";

	var newCaseSuffix = '';
	var tpaList = tpanames; // the default set: all TPAs
	var defaultTpa = "";
	var visitType = screenid == 'ip_registration'? 'i': 'o';
	if (categoryId != '') {
		// category is enabled, the list of TPAs is restricted
		for (var i = 0; i < categoryJSON.length; i++) {
			var item = categoryJSON[i];
			if (categoryId == item.category_id) {
				document.mainform.reg_charge_applicable.value = item.registration_charge_applicable;
				if(visitType == 'i') {
					if ((item.ip_allowed_sponsors == null ||item.ip_allowed_sponsors=='')) {
						tpaList = [];
						loadSelectBox(insuCompObj, [], 'insurance_co_name',
							'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
					}else if ((item.ip_allowed_sponsors != '*')) {
						var tpaIdList = item.ip_allowed_sponsors.split(',');
						var ip_allowedTpas = [];
						for (var i = 0; i < tpaIdList.length; i++)
						ip_allowedTpas.push(findInList(tpanames, "tpa_id", tpaIdList[i]));
						// override the tpaList with allowed TPAs.
						tpaList =  ip_allowedTpas ;
					} else {
						tpaList = tpaList;
					}
					if (spnsrIndex == 'P')
						defaultTpa = item.primary_ip_sponsor_id;
					else if (spnsrIndex == 'S')
						defaultTpa = item.secondary_ip_sponsor_id
					break;
				 } else {
				 	if ((item.op_allowed_sponsors == null ||item.op_allowed_sponsors=='')) {
						tpaList = [];
						loadSelectBox(insuCompObj, [], 'insurance_co_name',
							'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
					}else if ((item.op_allowed_sponsors != '*')) {
						var tpaIdList = item.op_allowed_sponsors.split(',');
						var op_allowedTpas = [];
						for (var i = 0; i < tpaIdList.length; i++)
						op_allowedTpas.push(findInList(tpanames, "tpa_id", tpaIdList[i]));
						// override the tpaList with allowed TPAs.
						tpaList = !empty(op_allowedTpas) ? op_allowedTpas : tpaList;
					} else {
						tpaList = tpaList;
					}
					if (spnsrIndex == 'P')
						defaultTpa = item.primary_op_sponsor_id;
					else if (spnsrIndex == 'S')
						defaultTpa = item.secondary_op_sponsor_id;
					break;
				 }
			}
		}
	} else {
		document.mainform.reg_charge_applicable.value = "Y";
	}
	if (tpaObj != null) {
		// Empty TPAs in tpa dropdown
		//tpaObj.length = 1;
		//var index = 0;
		//tpaObj.value = "";

		var  tpaObjName = tpaObj.name;
		var sponsorType = '';
		if (tpaObjName.startsWith('primary_sponsor')) sponsorType = 'I';
		else if (tpaObjName.startsWith('primary_corporate')) sponsorType = 'C';
		else if (tpaObjName.startsWith('primary_national')) sponsorType = 'N';
		else if (tpaObjName.startsWith('secondary_sponsor')) sponsorType = 'I';
		else if (tpaObjName.startsWith('secondary_corporate')) sponsorType = 'C';
		else if (tpaObjName.startsWith('secondary_national')) sponsorType = 'N';
		else {}

		// Add all the allowed TPAs for patient category and insurance company as new cases.
		/*var newtpa =[];
		for (var i = 0; i < tpaList.length; i++) {
			var item = tpaList[i];

			item = findInList(tpanames, "tpa_id", item.tpa_id);
			if (sponsorType == 'I' && item.sponsor_type == 'I') {
				for (var k = 0; k < insCompTpaList.length; k++) {
					var insItem = insCompTpaList[k];
					if (!empty(item) && !empty(insItem) && (item.tpa_id == insItem.tpa_id)) {
						newtpa.push(item);
					}
				}
			}else {
				if (sponsorType == item.sponsor_type) {
					//tpaNameObj.value = item.tpa_name;
					//tpaObj.value = item.tpa_id;
				}
			}
		}
		if (!empty(newtpa)){
			tpaList=newtpa;
			if(!empty(insCompanyId) ){
				if(tpaObj.value != ''){
					var exist = findInList(newtpa, "tpa_id", tpaObj.value);
					if(empty(exist)){
						tpaNameObj.value = '';
						tpaObj.value = '';
					}
				}
			}
			if(newtpa.length ==1 && tpaObj.value ==''){
				tpaNameObj.value = newtpa[0].tpa_name;
				tpaObj.value = newtpa[0].tpa_id;
			}
		}*/

		if (allowBillNowInsurance == 'true' || (document.mainform.bill_type != null && document.mainform.bill_type.value == 'C')) {
			// if the patient is for revisit and TPA exists, set it
			if (!empty(previousTpa)) {
				//setSelectedIndex(tpaObj, previousTpa);
				var item2 = findInList(tpanames, "tpa_id", previousTpa);
				tpaNameObj.value = item2.tpa_name;
				tpaObj.value = previousTpa;

				// if there is a default tpa for patient category, set it (doesn't work well if there is a case for the same TPA)
			} else if (!empty(defaultTpa)) {
				//setSelectedIndex(tpaObj, defaultTpa);
				//add wrapper here
				var item2 = findInList(tpanames, "tpa_id", defaultTpa);
				tpaNameObj.value = item2.tpa_name;
				tpaObj.value = defaultTpa;
				tpaWrapperObj.checked = true;

				// if there is a default tpa for insurance company, set it
			} else if (insCompTpaList.length == 1) {
				var item2 = findInList(tpanames, "tpa_id", insCompTpaList[0].tpa_id);
				tpaNameObj.value = item2.tpa_name;
				tpaObj.value = insCompTpaList[0].tpa_id;
				//setSelectedIndex(tpaObj, insCompTpaList[0].tpa_id);
			}
			if (document.mainform.op_type != null
					&& (document.mainform.op_type.value != "F" && document.mainform.op_type.value != "D"))
				tpaObj.disabled = false;
			// Ins30 : Corporate insurance validations removed
			//if(corpInsuranceCheck == 'Y')
					//setSelectedIndexForCorpInsurance1(tpaObj);

		} else {
			setSelectedIndex(tpaObj, "");
			tpaObj.disabled = true;
			tpaWrapperObj.checked = false;
		}
	}

	if (insuCompObj != null)
		sortDropDown(insuCompObj);
	if (tpaObj != null){
		if(spnsrIndex == 'P')
			priAutoLoadTpa(tpaList);
		if(spnsrIndex == 'S')
			secAutoLoadTpa(tpaList);
	}
	if (planTypeObj != null)
		sortDropDown(planTypeObj);
	if (planObj)
		sortDropDown(planObj);

	// Ins30 : Corporate insurance validations removed
	// if(corpInsuranceCheck != 'Y'){
			if (document.mainform.patient_category_id &&
					 planTypeObj != null && planTypeObj.options.length == 2) {
				setSelectedIndex(planTypeObj, planType);
			}
		// }else
		// {
			// if (document.mainform.patient_category_id &&
			//		 planTypeObj != null) {
			//	setSelectedIndexForCorpInsurance(planTypeObj);
			// }
		// }

		if(tpaObj != null && !empty(tpaObj.value)){
				enableRegistrationOtherInsuranceDetailsTab(spnsrIndex);
        loadRegistrationInsuCompanyDetails(spnsrIndex);
        loadRegistrationOtherInsDetails(spnsrIndex);
				insuCatChange(spnsrIndex);
				RatePlanList();
				ratePlanChange();
		}
}

function setSelectedIndexForCorpInsurance1(tpaObj) {
				tpaObj.selectedIndex = 1;
				return;
}

function setSelectedIndexForCorpInsurance(planTypeObj) {
				planTypeObj.selectedIndex = 1;
				return;
}

function setSelectedDateForCorpInsurance() {
	var corpInsuPlanId = '';
	if(null != document.getElementById('primary_plan_id'))
		corpInsuPlanId = document.getElementById('primary_plan_id').value;

	if(document.getElementById('primary_policy_validity_start') != null)
		document.getElementById('primary_policy_validity_start').textContent='';

	if(document.getElementById('primary_policy_validity_end') != null)
		document.getElementById('primary_policy_validity_end').textContent='';

	if(corpInsuPlanId != ''){
	for (var i = 0; i < policynames.length; i++) {
		if (policynames[i].plan_id == corpInsuPlanId) {
		var corpInsuStartDate = policynames[i].insurance_validity_start_date;
		var corpInsuEndDate =	policynames[i].insurance_validity_end_date;
		var corpInsuValStartDate = formatDate(new Date(policynames[i].insurance_validity_start_date));
		var corpInsuValEndDate = formatDate(new Date(policynames[i].insurance_validity_end_date));
			if(corpInsuStartDate){
			if(document.getElementById('primary_policy_validity_start') != null)
				document.getElementById('primary_policy_validity_start').textContent=corpInsuValStartDate;

			if(document.getElementById('primary_policy_validity_end') != null)
				document.getElementById('primary_policy_validity_end').textContent=corpInsuValEndDate;

				if(document.getElementById('primary_policy_validity_start1') != null)
					document.getElementById('primary_policy_validity_start1').value = corpInsuValStartDate;

				if(document.getElementById('primary_policy_validity_end1') != null)
					document.getElementById('primary_policy_validity_end1').value = corpInsuValEndDate;

				return true;
			} else {
				if(document.getElementById('primary_policy_validity_start') != null)
					document.getElementById('primary_policy_validity_start').textContent='NA';

				if(document.getElementById('primary_policy_validity_end') != null)
					document.getElementById('primary_policy_validity_end').textContent='NA';

				if(document.getElementById('primary_policy_validity_start1') != null)
					document.getElementById('primary_policy_validity_start1').value = '';

				if(document.getElementById('primary_policy_validity_end1') != null)
					document.getElementById('primary_policy_validity_end1').value = '';

				return false;
				}
			 break;
		}
	 }
	}
	return true;
}

function onCorporateChange(spnsrIndex) {

	var tpaObj = null;
	var tpa = null;
	var approvalLimitObj = null;
	var uploadRowObj = null;

	var employeeIdObj = null;
	var empNameObj = null;
	var empRelationObj = null;

	if (spnsrIndex == 'P') {
		tpaObj = getPrimarySponsorObj();
		approvalLimitObj = getPrimaryApprovalLimitObj();
		uploadRowObj = getPrimaryUploadRowObj();

		employeeIdObj = getPrimaryMemberIdObj();
		empNameObj = getPrimaryPatientHolderObj();
		empRelationObj = getPrimaryPatientRelationObj();

	}else if (spnsrIndex == 'S') {
		tpaObj = getSecondarySponsorObj();
		approvalLimitObj = getSecondaryApprovalLimitObj();
		uploadRowObj = getSecondaryUploadRowObj();

		employeeIdObj = getSecondaryMemberIdObj();
		empNameObj = getSecondaryPatientHolderObj();
		empRelationObj = getSecondaryPatientRelationObj();

	}
	if (tpaObj != null && employeeIdObj != null) {
		var tpaId = tpaObj.value;
		approvalLimitObj.value = "";
		employeeIdObj.value = "";
		empNameObj.value = "";
		empRelationObj.value = "";

		if (tpaId != '') {
			tpa = findInList(tpanames, "tpa_id", tpaId);
		}
		if (tpa != null) {
			if (uploadRowObj != null) {
				if (tpa.scanned_doc_required == 'N')
					uploadRowObj.style.display = 'none';
				else
					uploadRowObj.style.display = 'table-row';
			}
		}
		ratePlanChange();
	}
}

function onNationalSponsorChange(spnsrIndex) {

	var tpaObj = null;
	var tpa = null;
	var approvalLimitObj = null;
	var uploadRowObj = null;

	var nationalIdObj = null;
	var citizenNameObj = null;
	var patRelationObj = null;

	if (spnsrIndex == 'P') {
		tpaObj = getPrimarySponsorObj();
		approvalLimitObj = getPrimaryApprovalLimitObj();
		uploadRowObj = getPrimaryUploadRowObj();

		nationalIdObj = getPrimaryMemberIdObj();
		citizenNameObj = getPrimaryPatientHolderObj();
		patRelationObj = getPrimaryPatientRelationObj();

	}else if (spnsrIndex == 'S') {
		tpaObj = getSecondarySponsorObj();
		approvalLimitObj = getSecondaryApprovalLimitObj();
		uploadRowObj = getSecondaryUploadRowObj();

		nationalIdObj = getSecondaryMemberIdObj();
		citizenNameObj = getSecondaryPatientHolderObj();
		patRelationObj = getSecondaryPatientRelationObj();
	}

	if (tpaObj != null && nationalIdObj != null && approvalLimitObj != null) {
		var tpaId = tpaObj.value;
		approvalLimitObj.value = "";
		nationalIdObj.value = "";
		citizenNameObj.value = "";
		patRelationObj.value = "";

		if (tpaId != '') {
			tpa = findInList(tpanames, "tpa_id", tpaId);
		}
		if (tpa != null) {
			if (uploadRowObj != null) {
				if (tpa.scanned_doc_required == 'N')
					uploadRowObj.style.display = 'none';
				else
					uploadRowObj.style.display = 'table-row';
			}
		}
		ratePlanChange();
	}
}


// Function called in 3 places, when TPA is changed (tpaChange())
// (or) Rate plans are loaded (RatePlanList())
// (or) to load existing patient details (loadInsurancePolicyDetails())

var gSelectedTPA = null;
var gSelectedPlan = null;
var gSelectedRatePlan = null;
var gSelectedPatientCategory = null;
function ratePlanChange() {

	var patientCategoryObj	= document.mainform.patient_category_id;
	var patientCategory		= "";
	if (patientCategoryObj != null) patientCategory = patientCategoryObj.value;

	if (gSelectedPatientCategory == patientCategory) {
		gPatientCategoryChanged = false;

	} else {
		gSelectedPatientCategory = patientCategory;
		gPatientCategoryChanged = true;
		return;
	}

	var plan = "", tpaId = "";
	var isRatePlanChanged = false;
	var isTPAorPlanChanged = false;

	var ratePlanObj = document.mainform.organization;
	var ratePlan = ratePlanObj.value;

	var planObj = null;
	var tpaObj = null;

	var spnsrIndex = getMainSponsorIndex();

	if (spnsrIndex == 'P') {
		planObj = getPrimaryPlanObj();
		tpaObj = getPrimarySponsorObj();

	}else if (spnsrIndex == 'S') {
		planObj = getSecondaryPlanObj();
		tpaObj = getSecondarySponsorObj();
	}

	if (planObj != null) plan = planObj.value;
	if (tpaObj != null) tpaId = tpaObj.value;

	if (gSelectedRatePlan == ratePlan && gSelectedTPA == tpaId && gSelectedPlan == plan) return;

	if (gSelectedRatePlan == ratePlan){}
	else {
		gSelectedRatePlan = ratePlan;
		isRatePlanChanged = true;
	}

	if (gSelectedPlan == plan) {}
	else {
		gSelectedPlan = plan;
		isTPAorPlanChanged = true;
	}

	if (gSelectedTPA == tpaId) {}
	else {
		gSelectedTPA = tpaId;
		isTPAorPlanChanged = true;
	}

	if (isRatePlanChanged || isTPAorPlanChanged)
		resetOrderDialogRatePlanInsurance(gSelectedRatePlan, gSelectedTPA, gSelectedPlan, isRatePlanChanged);
}

function resetOrderDialogRatePlanInsurance(n_orgId, n_tpaId, n_plan, n_isRatePlanChanged) {


	if (n_isRatePlanChanged && document.mrnoform.group.value == "ipreg" && screenid == "ip_registration") {
		populateBedCharge();
	}

	if (screenid != 'ip_registration' && screenid != 'out_pat_reg') {

		if (!empty(addOrderDialog.getChargeRequest)) {
			if (YAHOO.lang.isArray(addOrderDialog.getChargeRequest)) {
				for (var i=0; i<getChargeRequest.length; i++) {
					YAHOO.util.Connect.abort(addOrderDialog.getChargeRequest[i] , addOrderDialog.onGetCharge , true) ;
				}
			}else {
				YAHOO.util.Connect.abort(addOrderDialog.getChargeRequest , addOrderDialog.onGetCharge , true) ;
			}
		}

		// clear the order table, since new rates are now applicable
		if(channellingOrders == null) {
		    clearOrderTable(0);
		}

		// tell order dialog the new Plan, so that it can use new rates for co-pay calculation.
		addOrderDialog.setInsurance((n_tpaId != ''), n_plan);

		// tell orderd dialog the new org ID, so that it can use new rates
		if (addOrderDialog) addOrderDialog.setOrgId(n_orgId);

		// tell order dialog if patient has insurance for restricting doctor consultation per visit.
		if( eClaimModule == 'Y' && !empty(n_plan) && n_plan != 0)
			addOrderDialog.restictionType = 'Doctor';
		else
			addOrderDialog.restictionType = null;

		// Update the list of items in the order dialog: this depends on the rate plan
		var url = orderItemsUrl + '&orgId=' + n_orgId + '&visitType=o' + "&center_id=" + centerId + "&tpa_id=" + n_tpaId;
        var reqObject = newXMLHttpRequest();
        reqObject.open("POST", url.toString(), false);
        reqObject.send(null);
        if (reqObject.readyState == 4) {
            if ((reqObject.status == 200) && (reqObject.responseText != null)) {
                onNewItemList(reqObject);
            }
        }

		disableRegBillButton();
		calculateTotalEstimateAmount();
	}
}

// Display the estimated total amount field set and disable register & pay based on action rights and rate plan
function disableRegBillButton() {
	if (document.mainform.regBill == null)
		return;
	var organizationId = document.mainform.organization.value;
	var display = displayEstimatedTotalAmountTable(organizationId);
	var regBillDisabled = document.mainform.regBill.disabled;
	if (display) {
		document.getElementById("estimatedTotalAmountFieldSet").style.display = 'block';
		if ((document.mainform.regBill != null)) {
			var newType = document.mainform.bill_type.value;
			if (document.mainform.bill_type != null
					&& document.mainform.bill_type.value == 'P' && !empty(billingCounterId))
				document.mainform.regBill.disabled = false;
			else
				document.mainform.regBill.disabled = regBillDisabled;
		}
	}else {
		document.getElementById("estimatedTotalAmountFieldSet").style.display = 'none';
		document.mainform.regBill.disabled = true;
	}

	if (document.mainform.regBill != null) {
		var disable = document.mainform.regBill.disabled;
		document.getElementById("paymentTab").style.display = disable ? 'none' : 'block';
	}
}

function displayEstimatedTotalAmountTable(rateplan) {
	return (!empty(showChargesAllRatePlan) && showChargesAllRatePlan == 'A');
}

function calculateTotalEstimateAmount() {
	var doctorObj = document.mainform.doctor;
	var chargeTypeObj = document.mainform.doctorCharge;
	var doctorCharge = (chargeTypeObj) ? chargeTypeObj.value : '';

	getConsultationTypes(document.mainform.organization.value);

	if (chargeTypeObj) {
		setSelectedIndex(chargeTypeObj, "");
		setSelectedIndex(chargeTypeObj, doctorCharge);
	}

	if (doctorObj) {
		var  doctorId = doctorObj.value;
		if (!empty(doctorId)) getDoctorCharge();
	}

	loadPreviousUnOrderedPrescriptions();

	if (trim(document.mrnoform.mrno.value) != "" && gSelectedRatePlan == null && gSelectedTPA == null && gSelectedPlan  == null) return;
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
	estimateTotalAmount();
}

// For the previous prescriptions - patient and sponsor amts have to be calculated so post the orders after
// ins. comp, tpa and plan are loaded.
function loadPreviousUnOrderedPrescriptions() {
	if ( !loadPrvPrescripitons ){
		return;
	}
	var planObj	= getPrimaryPlanObj();
	if(planObj && null != planObj.value && !empty(planObj.value))
		setDiscountPlan(planObj.value);

	if (screenid != 'ip_registration' && screenid != 'out_pat_reg') {

		if (!empty(addOrderDialog.getChargeRequest)) {
			if (YAHOO.lang.isArray(addOrderDialog.getChargeRequest)) {
				for (var i=0; i<getChargeRequest.length; i++) {
					YAHOO.util.Connect.abort(addOrderDialog.getChargeRequest[i] , addOrderDialog.onGetCharge , true) ;
				}
			}else {
				YAHOO.util.Connect.abort(addOrderDialog.getChargeRequest , addOrderDialog.onGetCharge , true) ;
			}
		}
        if(channellingOrders == null) {
		    clearOrderTable(0);
        }
	}
	if (document.mainform.op_type != null && (screenid != "out_pat_reg")
			&& (document.mainform.op_type.value == 'F' || document.mainform.op_type.value == 'D')) {
		if (gTestPrescriptions != null || gServicePrescriptions != null || gConsultationPrescriptions != null
			|| gDietPrescriptions != null || gStandingInstructions != null || gOperationPrescriptions != null) {
			orderPrescriptions();
		}
	}
	//estimateTotalAmount();
}

var cachedConsultTypes = [];

function getConsultationTypes(orgId) {
	orgId = empty(orgId) ? 'ORG0001' : orgId;
	if (cachedConsultTypes[orgId] == undefined) {
		var ajaxobj = newXMLHttpRequest();
		var url = '../../pages/registration/regUtils.do?_method=getConsultationTypesForRateplan&orgId=' + orgId;
		ajaxobj.open("POST", url, false);
		ajaxobj.send(null);

		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var consultationTypes = " + ajaxobj.responseText);
					cachedConsultTypes[orgId] = consultationTypes;
				}
			}
		}
	}

	if(allow_all_cons_types_in_reg == 'N') {
		loadPreferenceConsultationTypes(document.orderDialogForm.doctorCharge);
		loadPreferenceConsultationTypes(document.mainform.doctorCharge);
	} else {
		loadSelectBox(document.orderDialogForm.doctorCharge, cachedConsultTypes[orgId], 'consultation_type', 'consultation_type_id', getString("js.registration.patient.commonselectbox.defaultText"), '');
		loadSelectBox(document.mainform.doctorCharge, cachedConsultTypes[orgId], 'consultation_type', 'consultation_type_id', getString("js.registration.patient.commonselectbox.defaultText.none"), '');
		sortDropDown(document.orderDialogForm.doctorCharge);
		sortDropDown(document.mainform.doctorCharge);
	}
}

function onNewItemList(response) {
	eval(response.responseText);
	// re-initialize the item list within the order dialog.
	addOrderDialog.setNewItemList(rateplanwiseitems);
}

function onDeptChange() {
	loadPrvPrescripitons = false;
	DeptChange();
	loadPreviousUnOrderedPrescriptions();
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
	estimateTotalAmount();
	loadPrvPrescripitons = true;
	loadPreviousUnOrderedPrescriptions();
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
}

var isDoctorChange = false;

function DeptChange() {
	var deptId = document.mainform.dept_name.value;
	document.mainform.dept_allowed_gender.value = '';
	setDeptAllowedGender(deptId);

	document.mainform.doctor_name.removeAttribute("title");
	document.mainform.doctor_name.value = '';
	document.mainform.doctor.value = '';
	document.mainform.consFees.value = 0;
	document.mainform.opdocchrg.value = 0;
	if (document.getElementById("docConsultationFees") != null) {
		document.mainform.doctorCharge.selectedIndex = 0;
		document.getElementById("docConsultationFees").textContent = '';
	}
	estimateTotalAmount();

	//Initialize doctor auto complete and reset op type.
	// Doctor autocomplete is initialized when the department is changed.
	if (!isDoctorChange)
		initDoctorDept(deptId);
	if (screenid != "ip_registration")
		setDocRevistCharge(document.mainform.doctor.value);

	var unitSelect = document.mainform.unit_id;
	if (unitSelect != null) {
		loadDepartmentUnit(unitSelect, deptId);
	}

	setPatientComplaint();

	if (screenid != "ip_registration") calculateEstimateAmountOnDeptChange();
}

function calculateEstimateAmountOnDeptChange() {
	var estimateAmount = document.getElementById('estimateAmount').value;
	if (estimateAmount == '') {
		estimateAmount = 0;
	}
	if (document.mainform.consFees.value != '') {
		document.getElementById('estimtAmount').innerHTML =
					formatAmountPaise(getPaise(estimateAmount) - getPaise(document.mainform.consFees.value));

		document.getElementById('estimateAmount').value =
					formatAmountPaise(getPaise(estimateAmount) - getPaise(document.mainform.consFees.value));

		document.mainform.consFees.value = '';
		document.mainform.opdocchrg.value = '';
	}
}

function validateSchConsultation() {
	var form = document.mainform;
	var appointmentId = form.appointmentId.value;
	if (!empty(appointmentId)) {
		var primaryResource = getSchedulerPrimaryResource();
		if (!empty(primaryResource)) {
			var docChrgObj = document.mainform.doctorCharge;
			var docChrg = (docChrgObj != null) ? docChrgObj.value : "";
			setSelectedIndex(docChrgObj, docChrg);

			if (primaryResource.category == 'DOC') {
				if (!empty(primaryResource.consultation_type_id)
					&& primaryResource.consultation_type_id != "0"
					&& primaryResource.consultation_type_id != docChrg) {

					var organizationObj	   = document.mainform.organization;
					var consTypes		   = cachedConsultTypes[organizationObj.value];
					var consultation	   = findInList(consTypes, "consultation_type_id", primaryResource.consultation_type_id);
					if (empty(consultation)) {
						consTypes		   = cachedConsultTypes["ORG0001"];
						consultation 	   = findInList(consTypes, "consultation_type_id", primaryResource.consultation_type_id);
					}
					var consultation_type  = (!empty(consultation)) ? consultation.consultation_type : "";

					var msg = " "+getString("js.registration.patient.selected.consultation.type.is.not.same.as.scheduler.consultation.type.string")+":";
					msg += !empty(consultation_type) ? "\n ( "+ consultation_type + ") " : "";
					msg += "\n " + getString("js.registration.patient.want.to.continue.string");

					var ok = confirm(msg);
					if (!ok) {
						document.getElementById("doctorCharge").focus();
						return false;
					}
				}
			}
		}
	}
	return true;
}

function validateCreditLimitAmount() {
	var crLimitObj = document.getElementById('ipcreditlimit');
	if(crLimitObj == null || crLimitObj == undefined) {
		return true;
	}
	var availableCreditLimit = document.getElementById('ipcreditlimit').value;
	var sanctionedCreditLimit = formatAmountValue(parseFloat(availableCreditLimit) - parseFloat(gPatientDepositAmt));
	if( (screenid == "ip_registration") && (sanctionedCreditLimit < 0) ) {
		alert(getString("js.registration.patient.creditlimit.cannot.less")+formatAmountValue(gPatientDepositAmt))
		document.getElementById('ipcreditlimit').focus();
		return false;
	}
	return true;
}

function validateCreditLimitLength() {
	var availableCreditLimitObj = document.getElementById('ipcreditlimit');
	if(visitType != 'I' || availableCreditLimitObj == null) {
		return true;
	}

	var availableCreditLimit = availableCreditLimitObj.value;
	if(availableCreditLimit == null || availableCreditLimit == "") {
		alert(getString("js.registration.patient.creditlimit.cannot.empty"));
		document.getElementById('ipcreditlimit').focus();
		return false;
	}

	var noOfDigits = getNumberOfDigits(availableCreditLimit);
	if(noOfDigits > 13) {
		alert(getString("js.registration.patient.creditlimit.cannot.moredigits"));
		document.getElementById('ipcreditlimit').focus();
		return false;
	}
	return true;
}

function getNumberOfDigits(availableCreditLimit) {
	var decimalsBeforeDot = (availableCreditLimit.split(".")[0]);

	if(decimalsBeforeDot.indexOf("-") != -1) {
		return decimalsBeforeDot.length - 1;
	} else {
		return decimalsBeforeDot.length;
	}
}

function validateandregister() {
	var newReg = isNewReg();

	if (!newReg && !empty(gMrNo) && trim(gMrNo) != '' && trim(document.mrnoform.mrno.value) != ''
		&& trim(gMrNo) != trim(document.mrnoform.mrno.value)) {
		(getString("js.registration.patient.valid.mr.no.check")+" : "+trim(gMrNo));
		document.mrnoform.mrno.focus();
		return false;
	}

	document.mainform.mrno.value = trim(document.mrnoform.mrno.value);
	var mrno = document.mainform.mrno.value;
	if (!patientDetailsValidation()) return false;
	if (!patientAdditionalFieldsValidation()) return false;

	if (newReg) {
		document.mainform.regType.value = "new";
	} else {
		document.mainform.regType.value = "regd";
		var mrno = trim(document.mrnoform.mrno.value);
		if (mrno == "") {
			showMessage("js.registration.patient.mr.no.required");
			return false;
		}

		var elements = document.getElementsByName("category_expiry_date");
		for (var i = 0; i < elements.length; i++) {
			var obj = elements[i];
			if (obj.getAttribute("name") == "category_expiry_date") {
				var previousExpDate = document.getElementById('cardExpiryDate').value;
				var selectedExpDate = document.mainform.category_expiry_date.value;
				myDate = new Date();
				var currDate = formatDueDate(myDate);
				if (previousExpDate == "") {
					if (getDateDiff(currDate, selectedExpDate) < 0) {
						showMessage("js.registration.patient.expiry.date.current.date.check");
						return false;
					}
				} else {
					if (selectedExpDate == previousExpDate) {
						if (getDateDiff(currDate, previousExpDate) < 0) {
							showMessage("js.registration.patient.registration.validity.period.check");
							document.mainform.category_expiry_date.focus();
							return false;
						}
					}
					if (getDateDiff(currDate, selectedExpDate) < 0) {
						showMessage("js.registration.patient.expiry.date.current.date.check");
						document.mainform.category_expiry_date.focus();
						return false;
					}
				}
			}
		}
	}

	if (document.mrnoform.mlccheck.checked) {
		if (document.mainform.mlc_template.selectedIndex <= 0) {
			showMessage("js.registration.patient.mlc.template.required");
			if (mlcDialog != null) mlcDialog.show();
			document.mainform.mlc_template.focus();
			return false;
		}
	}

	var priInsCompObj = getPrimaryInsuObj();
	var priTpaObj = getPrimarySponsorObj();

	var secInsCompObj = getSecondaryInsuObj();
	var secTpaObj = getSecondarySponsorObj();

	if (priInsCompObj != null && priInsCompObj.selectedIndex != 0
					&& priTpaObj != null && empty(priTpaObj.value)) {
		// Ins30 : Corporate Insurance validations removed
		//if(corpInsuranceCheck != 'Y'){
				showMessage("js.registration.patient.tpa.sponsor.required");
			// }else{
				// showMessage("js.registration.patient.sponsor.required");
			// }
		priTpaObj.focus();
		return false;
	}
	if (secInsCompObj != null && secInsCompObj.selectedIndex != 0
					&& secTpaObj != null && empty(secTpaObj.value)) {
		showMessage("js.registration.patient.tpa.sponsor.required");
		secTpaObj.focus();
		return false;
	}

	if (document.getElementById('organization').value == '') {
		showMessage("js.registration.patient.rate.plan.required");
		document.getElementById('organization').focus();
		return false;
	}

	if (document.mainform.bed_id != undefined) {
		if (allocBed == 'Y' && dutyDoctorMand && !document.mainform.duty_doctor_id.disabled && document.mainform.duty_doctor_id.value == '') {
			showMessage("js.registration.patient.duty.doctor.required");
			document.mainform.duty_doctor_id.focus();
			return false;
		}
		if (allocBed == 'Y' && !document.mainform.bed_id.disabled && document.mainform.bed_id.value == 0) {
			showMessage("js.registration.patient.bed.name");
			document.mainform.bed_id.focus();
			return false;
		}
	}

	return true;
}

function validateOnDepPref() {
	var prefArea = document.mainform.areaValidate.value;
	var prefAddress = document.mainform.addressValidate.value;
	var prefEmailId = document.mainform.validateEmailId.value;
	var prefNextOfkin = document.mainform.nextofkinValidate.value;
	var area = document.mainform.patient_area.value;
	var prefReferredBy = document.mainform.referredbyValidate.value;
	var address = document.mainform.patient_address.value;
	var emailId = document.mainform.email_id.value;
	var cRealation = document.mainform.relation.value;
	var cPerson = document.mainform.patient_care_oftext.value;
	var cpAddress = document.mainform.patient_careof_address.value;
	var complaint = "";
	var prefPatientPhone = document.mainform.patientPhoneValidate.value;
	var patientPhone = document.mainform.patient_phone.value;

	if (document.mainform.ailment != null) complaint = document.mainform.ailment.value;
	var creferaldoctorName = document.mainform.referaldoctorName.value;
	if (prefArea == "A" || prefArea == "I" && visitType == "I" || prefArea == "O" && visitType == "O") {
		if (area == "") {
			showMessage("js.registration.patient.area.required");
			CollapsiblePanel1.open();
			document.mainform.patient_area.focus();
			return false;
		}
	}
	if (prefAddress == "A" || prefAddress == "I" && visitType == "I" || prefAddress == "O" && visitType == "O") {
		if (address == "") {
			showMessage("js.registration.patient.address.required");
			CollapsiblePanel1.open();
			document.mainform.patient_address.focus();
			return false;
		}
	}
	if (prefEmailId == "A" || prefEmailId == "I" && visitType == "I" || prefEmailId == "O" && visitType == "O") {
		if (emailId == "") {
			showMessage("js.registration.patient.email.id.required");
			CollapsiblePanel1.open();
			document.mainform.email_id.focus();
			return false;
		}
	}
	if (prefPatientPhone == "A" || prefPatientPhone == "I" && visitType == "I" || prefPatientPhone == "O" && visitType == "O") {
		if (patientPhone == "") {
			showMessage("js.registration.patient.phone.no.required");
			$("#patient_phone_national").focus();
			return false;
		}
	}
	if (prefNextOfkin == "A" || prefNextOfkin == "I" && visitType == "I" || prefNextOfkin == "O" && visitType == "O") {
		if (cRealation == "") {
			showMessage("js.registration.patient.next.of.kin.relation.name.required");
			document.mainform.relation.focus();
			return false;
		}
		if (cPerson == "") {
			showMessage("js.registration.patient.next.of.kin.contact.no.required");
			$("#patient_care_oftext_national").focus();
			return false;
		}
		if (cpAddress == "") {
			showMessage("js.registration.patient.next.of.kin.address.required");
			CollapsiblePanel1.open();
			cfDialog.show();
			document.mainform.patient_careof_address.focus();
			return false;
		}
	}
	if (complaintField == "A" || complaintField == "I" && visitType == "I" || complaintField == "O" && visitType == "O") {
		if (document.mainform.ailment != null && !document.mainform.ailment.disabled) {
			if (complaint == "") {
				showMessage("js.registration.patient.complaint.required");
				document.mainform.ailment.focus();
				return false;
			}
		}
	}
	if (prefReferredBy == "A" || prefReferredBy == "I" && visitType == "I" || prefReferredBy == "O"
			&& visitType == "O" && screenid != "out_pat_reg" || prefReferredBy == "P" && visitType == "O" && screenid == "out_pat_reg") {
		if (creferaldoctorName == "") {
			showMessage("js.registration.patient.referral.required");
			document.mainform.referaldoctorName.focus();
			return false;
		}
	}
	if (cfDialog != null) cfDialog.hide();
	return true;
}

function visitFieldsValidation() {
	if (screenid == "ip_registration") {
		//validation for  ip
		var selIndex = document.mainform.dept_name.selectedIndex;
		if (selIndex == 0) {
			showMessage("js.registration.patient.consulting.department.required");
			document.mainform.dept_name.focus();
			return false;
		}

		if (trim(document.mainform.doctor_name.value) == '') {
			showMessage("js.registration.patient.admitting.doctor.required");
			document.mainform.doctor_name.focus();
			return false;
		}

		if (document.mainform.bed_type.selectedIndex == 0) {
			showMessage("js.registration.patient.bed.type.required");
			document.mainform.bed_type.focus();
			return false;
		}

		if (document.mainform.ward_id.selectedIndex == 0) {
			showMessage("js.registration.patient.ward.name.required");
			document.mainform.ward_id.focus();
			return false;
		}
		return true;

	}

	if (screenid == "out_pat_reg") {
		return true;
	}
}

function hasPlanVisitCopayLimit(planId) {
	var plan = findInList(policynames, "plan_id", planId);
	var hasVisitCopayLimit = false;
	if (empty(plan))
		return hasVisitCopayLimit;

	hasVisitCopayLimit = ((!empty(document.getElementById("primary_max_copay").value) && getPaise(document.getElementById("primary_max_copay").value) != 0)||
		(!empty(document.getElementById("secondary_max_copay").value) && getPaise(document.getElementById("secondary_max_copay").value) != 0))


	/*var visitType = screenid == 'ip_registration'? 'i': 'o';
	if (visitType == 'o')
		hasVisitCopayLimit = (!empty(plan.op_visit_copay_limit) && getPaise(plan.op_visit_copay_limit) != 0);

	else if (visitType == 'i')
		hasVisitCopayLimit = (!empty(plan.ip_visit_copay_limit) && getPaise(plan.ip_visit_copay_limit) != 0);*/

	//if()

	return hasVisitCopayLimit;
}

function validateMultiSponsorForPlanWithCopay() {
	if (document.getElementById("secondary_sponsor").value != "") {
		var planObj = getSecondaryPlanObj();
		if (planObj == null)
			planObj = getPrimaryPlanObj();

		if (planObj != null) {
			var planId = planObj.value;
			var hasVisitCopayLimit = hasPlanVisitCopayLimit(planId);
			if (hasVisitCopayLimit) {
				var msg = getString("js.registration.patient.plan.has.visit.copay.string");
				msg += " \n";
				msg += getString("js.registration.patient.secondary.sponsor");
				msg += " "+	getString("js.registration.patient.is.not.allowed.string");

				alert(msg);
				document.getElementById("secondary_sponsor").focus();
				return false;
			}
		}
	}
	return true;
}

function validatePrimarySponsor() {
	if (document.getElementById("secondary_sponsor").value != "") {
		var tpaObj = getSecondarySponsorObj();
		if (tpaObj != null && tpaObj.value != "") {
			if (document.getElementById("primary_sponsor").value == "") {
				var msg = getString("js.registration.patient.primary.sponsor") +
						  " "+getString("js.registration.patient.is.required.string");
				alert(msg);
				document.getElementById("primary_sponsor").focus();
				return false;
			}
			tpaObj = getPrimarySponsorObj();
			if (tpaObj == null || tpaObj.value == "") {
				showMessage("js.registration.patient.tpa.sponsor.required");
				tpaObj.focus();
				return false;
			}
		}
	}
	return true;
}

function validateInsuranceFields(spnsrIndex) {

	if (!validateSponsor(spnsrIndex)) return false;

	if (spnsrIndex == 'P') {
		var priInsCompObj = getPrimaryInsuObj();
		var priTpaObj = getPrimarySponsorObj();
		var priPlanObj = getPrimaryPlanObj();
		var priPlanTypeObj = getPrimaryPlanTypeObj();
		var priMemberIdObj = getPrimaryInsuranceMemberIdObj();
	}

	if (spnsrIndex == 'S') {
		var secInsCompObj = getSecondaryInsuObj();
		var secTpaObj = getSecondarySponsorObj();
		var secPlanObj = getSecondaryPlanObj();
		var secPlanTypeObj = getSecondaryPlanTypeObj();
		var secMemberIdObj = getSecondaryInsuranceMemberIdObj();
	}
	//var spnsrIndex = getMainSponsorIndex();

//	if (isModAdvanceIns) {

		if(spnsrIndex == 'P' && (priTpaObj != null && !empty(priTpaObj))
			&& (priInsCompObj != null && priInsCompObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.InsuranceCompany.required");
			priInsCompObj.focus();
			return false;
		}

		if(spnsrIndex == 'P'
			&& (priInsCompObj != null && priInsCompObj.selectedIndex != 0)
			&& (priPlanTypeObj != null && priPlanTypeObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.network.plantype.name.required");
			priPlanTypeObj.focus();
			return false;
		}

		if (spnsrIndex == 'P'
			&& (priPlanObj != null && priPlanObj.selectedIndex == 0
			&& priPlanTypeObj.selectedIndex != 0)) {
			showMessage("js.registration.patient.plan.name.required");
			priPlanObj.focus();
			return false;
		}

		if(spnsrIndex == 'S' && (secTpaObj != null && !empty(secTpaObj))
			&& (secInsCompObj != null && secInsCompObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.InsuranceCompany.required");
			secInsCompObj.focus();
			return false;
		}

		if (spnsrIndex == 'S'
			&& (secInsCompObj != null && secInsCompObj.selectedIndex != 0)
			&& (secPlanTypeObj != null && secPlanTypeObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.network.plantype.name.required");
			secPlanTypeObj.focus();
			return false;
		}

		if (spnsrIndex == 'S'
			&& (secPlanObj != null && secPlanObj.selectedIndex == 0
			&& secPlanTypeObj.selectedIndex != 0)) {
			showMessage("js.registration.patient.plan.name.required");
			secPlanObj.focus();
			return false;
		}

		if ((priPlanObj != null && priPlanObj.value != "")
			|| (secPlanObj != null && secPlanObj.value != "")) {

			var memberIdRequiredMsg = memberIdLabel + " " +getString("js.common.is.required");
			var memberIdValidFromRequiredMsg = memberIdValidFromLabel + " " +getString("js.common.is.required");
			var memberIdValidToRequiredMsg = memberIdValidToLabel + " " +getString("js.common.is.required");

			if (screenid == "ip_registration" || screenid == "out_pat_reg") {
				if (spnsrIndex != null) {
					if(!isMemberidValidated){
						if(!checkForMemberID(spnsrIndex)){
				 			return false;
						}
					}
				}
			}

			if(!validateMemberIdRequired(spnsrIndex))
				return false;

			if(!validatePlanPeriodRequired(spnsrIndex))
				return false;

			if(!validatePolicyDetailsRequired(spnsrIndex))
				return false;

			/*if (spnsrIndex == 'P' && priMemberIdObj != null && priMemberIdObj.value == "") {
				alert(memberIdRequiredMsg);
				priMemberIdObj.focus();
				return false;
			}
			if (spnsrIndex == 'S' && secMemberIdObj != null && secMemberIdObj.value == "") {
				alert(memberIdRequiredMsg);
				secMemberIdObj.focus();
				return false;
			}*/

			if(!checkForVisitCopayLimit(spnsrIndex))
				return false;

			/*if (spnsrIndex == 'P') {
				var policyStartDateObj = getPrimaryPolicyValidityStartObj();
				var policyEndDateObj = getPrimaryPolicyValidityEndObj();

			  if (policyStartDateObj != null) {
				if(corpInsuranceCheck != 'Y'){
				if (!validateRequired(policyStartDateObj, memberIdValidFromRequiredMsg)) return false;
				if (!validateRequired(policyEndDateObj, memberIdValidToRequiredMsg)) return false;
				}else{
					return true;
				}

				var fromDt = getDateFromField(policyStartDateObj);
				var toDt = getDateFromField(policyEndDateObj);
				var dateCompareMsg = memberIdValidToLabel+" "+getString("js.common.message.cannot.be.less.than")+" "+memberIdValidFromLabel;

				if ((toDt != null) && (fromDt != null)) {
					if (fromDt > toDt) {
						alert(dateCompareMsg);
						policyEndDateObj.focus();
						return false;
					}
				}

				var memberIdValidFromFutureValidate = memberIdValidFromLabel + " " +getString("js.common.message.date.invalid.future");
				var memberIdValidToPastValidate = memberIdValidToLabel + " " +getString("js.common.message.date.invalid.past");
				var curDate = new Date();
				if (gServerNow != null) {
					curDate.setTime(gServerNow);
				}
				curDate.setHours(0);
				curDate.setMinutes(0);
				curDate.setSeconds(0);
				curDate.setMilliseconds(0);

				if (fromDt > curDate) {
					alert(memberIdValidFromFutureValidate);
					policyStartDateObj.focus();
					return false;
				}

				if (toDt < curDate) {
					alert(memberIdValidToPastValidate);
					policyEndDateObj.focus();
					return false;
				}
			  }
			}*/

			/*if (spnsrIndex == 'S') {
				var policyStartDateObj = getSecondaryPolicyValidityStartObj();
				var policyEndDateObj = getSecondaryPolicyValidityEndObj();
			   if (policyStartDateObj != null) {
				if (!validateRequired(policyStartDateObj, memberIdValidFromRequiredMsg)) return false;
				if (!validateRequired(policyEndDateObj, memberIdValidToRequiredMsg)) return false;

				var fromDt = getDateFromField(policyStartDateObj);
				var toDt = getDateFromField(policyEndDateObj);
				var dateCompareMsg = memberIdValidToLabel+" "+getString("js.common.message.cannot.be.less.than")+" "+memberIdValidFromLabel;

				if ((toDt != null) && (fromDt != null)) {
					if (fromDt > toDt) {
						alert(dateCompareMsg);
						policyEndDateObj.focus();
						return false;
					}
				}

				var memberIdValidFromFutureValidate = memberIdValidFromLabel + " " +getString("js.common.message.date.invalid.future");
				var memberIdValidToPastValidate = memberIdValidToLabel + " " +getString("js.common.message.date.invalid.past");
				var curDate = new Date();
				if (gServerNow != null) {
					curDate.setTime(gServerNow);
				}
				curDate.setHours(0);
				curDate.setMinutes(0);
				curDate.setSeconds(0);
				curDate.setMilliseconds(0);

				if (fromDt > curDate) {
					alert(memberIdValidFromFutureValidate);
					policyStartDateObj.focus();
					return false;
				}

				if (toDt < curDate) {
					alert(memberIdValidToPastValidate);
					policyEndDateObj.focus();
					return false;
				}
			  }
			}*/
		}
//	}


	if (spnsrIndex == 'P') {

		if (!validateIdDetails('P')) {
			return false;
		}

		if (document.getElementById("primary_sponsor").value != "") {
			var tpaObj = getPrimarySponsorObj();
			if (tpaObj != null && tpaObj.value != "") {
				if (!validateInsuranceApprovalAmount('P'))
					return false;
			}
		}

		/*if (!isInsCompanyActive(priInsCompObj)) {
			showMessage("js.registration.patient.insurance.company.is.inactive");
			priInsCompObj.focus();
			return false;
		}*/

		if (!isTpaActive(priTpaObj)) {
			showMessage("js.registration.patient.tpa.is.inactive");
			priTpaObj.focus();
			return false;
		}

		if (!validateDRGCodeWithPlan(spnsrIndex)) {
			return false;
		}

		if (!validateInsuranceCardRequired(spnsrIndex)) {
			return false;
		}

		if (!validateScannedDocRequired(spnsrIndex)) {
			return false;
		}
	}

	if (spnsrIndex == 'S') {

		if (!validateIdDetails('S')) {
			return false;
		}

		if (document.getElementById("secondary_sponsor").value != "") {
			var tpaObj = getSecondarySponsorObj();
			if (tpaObj != null && tpaObj.value != "") {
				if (!validateInsuranceApprovalAmount('S'))
					return false;
			}
		}

		/*if (!isInsCompanyActive(secInsCompObj)) {
			showMessage("js.registration.patient.insurance.company.is.inactive");
			secInsCompObj.focus();
			return false;
		}*/

		if (!isTpaActive(secTpaObj)) {
			showMessage("js.registration.patient.tpa.is.inactive");
			secTpaObj.focus();
			return false;
		}

		if (!validateDRGCodeWithPlan(spnsrIndex)) {
			return false;
		}

		if (!validateInsuranceCardRequired(spnsrIndex)) {
			return false;
		}

		if (!validateScannedDocRequired(spnsrIndex)) {
			return false;
		}
	}

//	if (isModAdvanceIns) {
		if (spnsrIndex == 'P') {
			if (!isPlanTypeActive(priPlanTypeObj)) {
				showMessage("js.registration.patient.network.plan.type.is.inactive");
				priPlanTypeObj.focus();
				return false;
			}

			if (!isPlanActive(priPlanObj)) {
				showMessage("js.registration.patient.plan.is.inactive");
				priPlanObj.focus();
				return false;
			}
		}

		if (spnsrIndex == 'S') {
			if (!isPlanTypeActive(secPlanTypeObj)) {
				showMessage("js.registration.patient.network.plan.type.is.inactive");
				secPlanTypeObj.focus();
				return false;
			}

			if (!isPlanActive(secPlanObj)) {
				showMessage("js.registration.patient.plan.is.inactive");
				secPlanObj.focus();
				return false;
			}
		}
//	}

	return true;
}

function validateGovtIdentity() {

	var priPlanObj = getPrimaryPlanObj();
	var secPlanObj = getSecondaryPlanObj();

	if (isModAdvanceIns) {
		if ((priPlanObj != null && priPlanObj.value != "")
			|| (secPlanObj != null && secPlanObj.value != "")) {

			if (!empty(eClaimModule) && eClaimModule == 'Y') {
				var govtIdentifierIdObj = document.mainform.identifier_id;
				var isIdEmpty = false;
				if ((govtIdentifierIdObj != null && govtIdentifierIdObj.value == ""))
					isIdEmpty = true;

				if (isIdEmpty) {
					alert(govtIDType+" "+getString("js.registration.patient.is.required.string"));

					if (!CollapsiblePanel1.isOpen()) {
						CollapsiblePanel1.open();
						setTimeout("document.mainform.identifier_id.focus()", 800);
					}else
						setTimeout("document.mainform.identifier_id.focus()", 100);

					cfDialog.hide();
					return false;
				}

				return true;
			}
			return true;
		}
		return true;
	}

	return true;
}


function validateOutpatientMandatoryfields() {

	if (screenid == "out_pat_reg") {
		if (modMrdIcdEnabled == 'Y' && !validateDiagnosisDetails(false, ospDiagnosisSectionMandatory, false))
			return false;
		if (document.mainform.referaldoctorName.value != "" && document.mainform.clinician_id.value == "") {
			showMessage("js.registration.patient.clinician.id.required");
			document.mainform.clinician_id.focus();
			return false;
		}
		return true;
	}

	return true;
}

function isRatePlanActive(ratePlan) {
	if (empty(ratePlan)) return true;

	var org = findInList(orgNamesJSON, "org_id", ratePlan);
	if (org != null && org.status == 'A') {
		return true;
	}
	return false;
}

function isInsCompanyActive(insCompObj) {
	if (insCompObj == null) return true;
	var insComp = insCompObj.value;
	if (empty(insComp)) return true;

	var inscomp = findInList(insuCompanyDetails, "insurance_co_id", insComp);
	if (inscomp != null && inscomp.status == 'A') {
		return true;
	}
	return false;
}

function isTpaActive(tpaIdObj) {
	if (tpaIdObj == null) return true;
	var tpaId = tpaIdObj.value;
	if (empty(tpaId)) return true;

	//var tpa = findInList(tpanames, "tpa_id", tpaId);
	var tpa;
  if(tpaIdObj.id === "primary_sponsor_id"){
     tpa = primarySponsorDetails;
  } else {
     tpa = secondarySponsorDetails;
  }

	if (tpa != null && tpa.status == 'A') {
		return true;
	}
	return false;
}

function isPlanTypeActive(planTypeObj) {
	if (planTypeObj == null) return true;
	var planType = planTypeObj .value;
	if (empty(planType)) return true;


	//var plantype = findInList(insuCatNames, "category_id", planType);
	var planTypeList;
  if(planTypeObj.id === "primary_plan_type"){
       var preInsuranceCompany = getPrimaryInsuObj();
    	 planTypeList = findInList(primarySponsorDetails.insurance_companies, "insurance_co_id", preInsuranceCompany.value);
  } else {
      var secInsuranceCompany = getSecondaryInsuObj();
    	planTypeList = findInList(secondarySponsorDetails.insurance_companies,"insurance_co_id",secInsuranceCompany.value);
  }
  var plantype = findInList(planTypeList.network_plan_types, "category_id", planType);
	if (plantype != null && plantype.status == 'A') {
		return true;
	}
	return false;
}

function isPlanActive(planIdObj) {
	if (planIdObj == null) return true;
	var planId = planIdObj .value;
	if (empty(planId)) return true;

	var plan = findInList(policynames, "plan_id", planId);
	if (plan != null && plan.status == 'A') {
		return true;
	}
	return false;
}

function validateSponsor(spnsrIndex) {
	var tpaObj = null;
	var spnsrText = "";
	if (spnsrIndex == 'P') spnsrText = getString("js.registration.patient.primary.sponsor");
	if (spnsrIndex == 'S') spnsrText = getString("js.registration.patient.secondary.sponsor");
	else {}

	var msg =  spnsrText+ " " +getString("js.common.is.required");

	if (spnsrIndex == 'P')
		tpaObj = getPrimarySponsorObj();
	else if (spnsrIndex == 'S')
		tpaObj = getSecondarySponsorObj();

	if (tpaObj != null && tpaObj.value == "") {
		alert(msg);
		tpaObj.focus();
		return false;
	}
	return true;
}

function validateIdDetails(spnsrIndex) {
	var idValue = 0;
	var idObj = null;
	var msg = "";
	var tpaObj = getPrimarySponsorObj();
	if (spnsrIndex == 'P') {
		var primarySponsorObj = document.getElementById("primary_sponsor");
		tpaObj = getPrimarySponsorObj();
		if (primarySponsorObj != null) {
			if (primarySponsorObj.value == 'C') {
				idObj = document.getElementById("primary_employee_id");
				msg = getString("js.registration.patient.employee.id.required");
			}else if (primarySponsorObj.value == 'N') {
				idObj = document.getElementById("primary_national_member_id");
				msg = getString("js.registration.patient.member.id.required");
			}
		}
	}else if (spnsrIndex == 'S') {
		var secondarySponsorObj = document.getElementById("secondary_sponsor");
		tpaObj = getSecondarySponsorObj();
		if (secondarySponsorObj != null) {
			if (secondarySponsorObj.value == 'C') {
				idObj = document.getElementById("secondary_employee_id");
				msg = getString("js.registration.patient.employee.id.required");
			}else if (secondarySponsorObj.value == 'N') {
				idObj = document.getElementById("secondary_national_member_id");
				msg = getString("js.registration.patient.member.id.required");
			}
		}
	}

	if (tpaObj != null && tpaObj.value != "") {
		if (idObj != null && trim(idObj.value) == '') {
			alert(msg);
			idObj.focus();
			return false;
		}
	}
	return true;
}

function validateInsuranceApprovalAmount(spnsrIndex) {
	var insApprovalAmt = 0;
	var approvalLimitObj = null;
	if (spnsrIndex == 'P') {
		approvalLimitObj = getPrimaryApprovalLimitObj();
	}else if (spnsrIndex == 'S') {
		approvalLimitObj = getSecondaryApprovalLimitObj();
	}

	if (approvalLimitObj != null) {
		if (approvalLimitObj.disabled || approvalLimitObj.readOnly) return true;
		if (trimAll(approvalLimitObj.value) == '') {

			var spnsrText = "";
			if (spnsrIndex == 'P') spnsrText = getString("js.registration.patient.primary.sponsor");
			if (spnsrIndex == 'S') spnsrText = getString("js.registration.patient.secondary.sponsor");
			else {}

			var msg = " "+getString("js.registration.patient.warning");
			var tpaObj = getSecondarySponsorObj();
			if (tpaObj != null && tpaObj.value != "") {
				msg += " "+spnsrText;
			}
			msg += " "+getString("js.registration.patient.approval.amount.is.given.string")+" \n";
			msg += " "+getString("js.registration.patient.warning.validations.against.approval.amount.will.be.disabled.string")+" \n";
			msg += " "+getString("js.registration.patient.for.blanket.approvals.specify.amount.as.zero.string")+" \n";
			msg += " "+getString("js.registration.patient.do.you.want.tocontinue.to.save.string")+" \n";

			var ok = confirm(msg);
			if (!ok) {
				approvalLimitObj.focus();
				return false;
			} else return true;
		}
		formatAmountObj(approvalLimitObj, false);
		insApprovalAmt = getPaise(approvalLimitObj.value);
	}

	var approvalMsg = getString("js.registration.patient.approval.amount.must.be.a.valid.amount");
	// Ins30 : Corporate Insurance validations removed
/*	if(corpInsuranceCheck != 'Y' && approvalLimitObj != null){
			if (!validateAmount(approvalLimitObj, approvalMsg))
				return false;
			}
*/
	if(approvalLimitObj != null){
		if (!validateAmount(approvalLimitObj, approvalMsg))
			return false;
		}

	/* Bug # 16439 - Blanket approval, if approval amount is zero then the amount is treated as unlimited. */
	if (insApprovalAmt >= 0) return true;
	else return false;
}

function validateBilltypeIfSecondarySponsor() {

	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj != null && secondarySponsorObj.value != "") {
		var tpaObj = getPrimarySponsorObj();
		if (tpaObj != null && tpaObj.value != "") {
			var billTypeObj = document.mainform.bill_type;

			if (billTypeObj && (billTypeObj.value == "C" || (allowBillNowInsurance == 'true' && billTypeObj.value == 'P'))) {
				return true;
			}

			if (billTypeObj && billTypeObj.value != 'C') {
				showMessage("js.registration.patient.secondary.sponsor.type.is.bill.later.required");
				billTypeObj.focus();
				return false;
			}
		}
		return true;
	}
	return true;
}

function validateScannedDocRequired() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var memberIdObj = null;
	var validatePrimary = true;
	var validateSecondary = true;

	if (primarySponsorObj != null && !primarySponsorObj.disabled && primarySponsorObj.value != "") {
		var tpaObj = getPrimarySponsorObj();
		memberIdObj = getPrimaryMemberIdObj();
		// Check if an existing sponsor details are being updated or new values are being inserted.
		if(primarySponsorObj.value == 'C') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(gPatientCorporateIds, "sponsor_id", tpaObj.value, "employee_id", memberIdObj.value);
				if(rec!= null) {
					validatePrimary = false;
				}
			}
		} else if(primarySponsorObj.value == 'N') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(gPatientNationalIds, "sponsor_id", tpaObj.value, "national_id", memberIdObj.value);
				if(rec!= null) {
					validatePrimary = false;
				}
			}
		} else if(primarySponsorObj.value == 'I') {
			validatePrimary = false;
		}


		if (tpaObj != null && tpaObj.value != "" && validatePrimary) {
			var tpaId = tpaObj.value;
			var tpa = findInList(tpanames, "tpa_id", tpaId);
			var insuranceCardMandatory = !empty(tpa) ? tpa.scanned_doc_required : "N";
			var docContentObj = getPrimaryDocContentObj();
			var fileHtmlId = 'primary_sponsor_cardfileLocation'+primarySponsorObj.value;

			if (insuranceCardMandatory == 'R' && docContentObj.value == ''
					&& document.getElementById(fileHtmlId).value == '') {
				showMessage("js.registration.patient.sponsor.required.scanned.doc");
				docContentObj.focus();
				return false;
			}
		}
	}

	if (secondarySponsorObj != null && !secondarySponsorObj.disabled && secondarySponsorObj.value != "") {

		var tpaObj = getSecondarySponsorObj();
		memberIdObj = getSecondaryMemberIdObj();
		if(secondarySponsorObj.value == 'C') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(gPatientCorporateIds, "sponsor_id", tpaObj.value, "employee_id", memberIdObj.value);
				if(rec!= null) {
					validateSecondary = false;
				}
			}
		} else if(secondarySponsorObj.value == 'N') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(gPatientNationalIds, "sponsor_id", tpaObj.value, "national_id", memberIdObj.value);
				if(rec!= null) {
					validateSecondary = false;
				}
			}
		} else if(secondarySponsorObj.value == 'I') {
			validateSecondary = false;
		}

		if (tpaObj != null && tpaObj.value != "" && validateSecondary) {
			memberIdObj = getSecondaryMemberIdObj();

			var tpaId = tpaObj.value;
			var tpa = findInList(tpanames, "tpa_id", tpaId);
			var insuranceCardMandatory = !empty(tpa) ? tpa.scanned_doc_required : "N";
			var docContentObj = getSecondaryDocContentObj();
			var fileHtmlId = 'secondary_sponsor_cardfileLocation'+secondarySponsorObj.value;

			if (insuranceCardMandatory == 'R' && docContentObj.value == ''
					&& document.getElementById(fileHtmlId).value == '') {
				showMessage("js.registration.patient.sponsor.required.scanned.doc");
				docContentObj.focus();
				return false;
			}
		}
	}
	return true;
}


function validateInsuranceCardRequired(spnsrIndex) {

	//var spnsrIndex = getMainSponsorIndex();
	var memberIdObj = null;
	var planIdObj = null;
	var tpaObj = null;
	var docContentObj = null;
	var docNameObj = null;
	var policyEndDateObj = null;
	var fileHtmlId = '';
	var previousPlan;
	var previousMemberId;
	var previousEndDate;
	var previousTpa;

	if (spnsrIndex == 'P') {
		memberIdObj = getPrimaryInsuranceMemberIdObj();
		planIdObj 	= getPrimaryPlanObj();
		tpaObj		= getPrimarySponsorObj();
		docContentObj = getPrimaryDocContentObj();
		docNameObj	= getPrimaryDocNameObj();
		policyEndDateObj = getPrimaryPolicyValidityEndObj();
		fileHtmlId = 'primary_sponsor_cardfileLocation'+document.getElementById("primary_sponsor").value;
		pastedPhotoId = 'primary_sponsor_pastedPhoto';
		previousPlan=gPreviousPlan;
		previousMemberId=gPreviousMemberId;
		previousEndDate=gPreviousEndDate;
		previousTpa=gPreviousPrimaryTpa;

	}else if (spnsrIndex == 'S') {
		memberIdObj = getSecondaryInsuranceMemberIdObj();
		planIdObj 	= getSecondaryPlanObj();
		tpaObj		= getSecondarySponsorObj();
		docContentObj = getSecondaryDocContentObj();
		docNameObj	= getSecondaryDocNameObj();
		policyEndDateObj = getSecondaryPolicyValidityEndObj();
		fileHtmlId = 'secondary_sponsor_cardfileLocation'+document.getElementById("secondary_sponsor").value;
		pastedPhotoId = 'secondary_sponsor_pastedPhoto';
		previousPlan=gPreviousSecPlan;
		previousMemberId=gPreviousSecMemberId;
		previousEndDate=gPreviousSecEndDate;
		previousTpa=gPreviousSecondaryTpa;
	}

	if (planIdObj == null || memberIdObj == null) return true;

	if (empty(planIdObj.value)) {
		if(!empty(docContentObj.value)) {
			docContentObj.value = "";
			alert(getString("js.registration.patient.please.select.a.plan"));
			return false;
		}
	}

	var tpaId = (tpaObj != null) ? tpaObj.value : "";
	var tpa = findInList(tpanames, "tpa_id", tpaId);
	var insuranceCardMandatory = !empty(tpa) ? tpa.scanned_doc_required : "N";

	var op_type;
	if (null != document.mainform.op_type){
		op_type=document.mainform.op_type.value;
	}

	if (insuranceCardMandatory == 'R' && isModAdvanceIns && (!empty(planIdObj.value) || !empty(memberIdObj.value))) {
		if(null != tpa && tpa !=""){
			if (null != previousTpa && tpaId != previousTpa && (op_type == 'R' || op_type == 'M')
						&& insuranceCardMandatory == 'R' && docContentObj.value == '' &&
						document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
				showMessage("js.registration.patient.insurance.card.required");
				docContentObj.focus();
				return false;
			}
			for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==tpa.sponsor_type_id){
					if(sponsorTypeList[i].member_id_show == 'Y' && sponsorTypeList[i].validity_period_show == 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])) {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'F' || op_type == 'D'))
							return true;
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'R' || op_type == 'M')){
							if(memberIdObj.value == previousMemberId && (parseDateStr(policyEndDateObj.value).getTime() == new Date(previousEndDate).getTime()))
								return true;
							else if(((!empty(memberIdObj.value) && memberIdObj.value != previousMemberId) || (!empty(policyEndDateObj.value) &&
									(parseDateStr(policyEndDateObj.value).getTime() != new Date(previousEndDate).getTime()))) && insuranceCardMandatory == 'R'
									&& docContentObj.value == '' && document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
								showMessage("js.registration.patient.insurance.card.required");
								docContentObj.focus();
								return false;
							}
						}
						else if(null != previousPlan && planIdObj.value !=previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
							&& document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}

					else if(sponsorTypeList[i].member_id_show == 'Y' && sponsorTypeList[i].validity_period_show != 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == ''  && empty(pastedImages[pastedPhotoId])) {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'F' || op_type == 'D'))
							return true;
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'R' || op_type == 'M')){
							if(memberIdObj.value == previousMemberId)
								return true;
							else if(!empty(memberIdObj.value) && memberIdObj.value != previousMemberId && insuranceCardMandatory == 'R'
									&& docContentObj.value == '' && document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
								showMessage("js.registration.patient.insurance.card.required");
								docContentObj.focus();
								return false;
							}
						}
						else if(null != previousPlan && planIdObj.value !=previousPlan && insuranceCardMandatory == 'R'
							&& docContentObj.value == '' && document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}

					else if(sponsorTypeList[i].member_id_show != 'Y' && sponsorTypeList[i].validity_period_show == 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])) {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'F' || op_type == 'D'))
							return true;
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'R' || op_type == 'M')){
							if(parseDateStr(policyEndDateObj.value).getTime() == new Date(previousEndDate).getTime())
								return true;
							else if(!empty(policyEndDateObj.value) && (parseDateStr(policyEndDateObj.value).getTime() != new Date(previousEndDate).getTime())
									&& insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
								showMessage("js.registration.patient.insurance.card.required");
								docContentObj.focus();
								return false;
							}
						}
						else if(null != previousPlan && planIdObj.value !=previousPlan && insuranceCardMandatory == 'R'
							&& docContentObj.value == '' && document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}

					else if(sponsorTypeList[i].member_id_show != 'Y' && sponsorTypeList[i].validity_period_show != 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])) {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan && (op_type == 'F' || op_type == 'D'))
							return true;
						else if (null != previousPlan && planIdObj.value != previousPlan && (op_type == 'R' || op_type == 'M')
									&& insuranceCardMandatory == 'R' && docContentObj.value == '' &&
									document.getElementById(fileHtmlId).value == '' && empty(pastedImages[pastedPhotoId])){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}
					break;
				}
			}
		}
	}
	return true;
}

function validateDRGCodeWithPlan(spnsrIndex) {

//	var spnsrIndex = getMainSponsorIndex();
	var useDRGObj = null;
	var drgCheckObj = null;
	var planObj = null;
	var billTypeObj = document.mainform.bill_type;

	if (spnsrIndex == 'P') {
		useDRGObj 	= getPrimaryUseDRGObj();
		drgCheckObj = getPrimaryDRGCheckObj();
		planObj 	= getPrimaryPlanObj();

	}else if (spnsrIndex == 'S') {
		useDRGObj 	= getSecondaryUseDRGObj();
		drgCheckObj = getSecondaryDRGCheckObj();
		planObj 	= getSecondaryPlanObj();
	}


	if (drgCheckObj && drgCheckObj.checked) {
		if (billTypeObj && billTypeObj.value != 'C') {
			showMessage("js.registration.patient.drg.bill.type.is.bill.later.required");
			billTypeObj.focus();
			return false;
		}

		if (planObj && planObj.value == '') {
			showMessage("js.registration.patient.drg.code.required.is.only.for.plan.patient");
			drgCheckObj.focus();
			return false;
		}
	}
	return true;
}


function validateFollowUpDoctor() {
	var doctorObj = document.mainform.doctor;
	var doctorChargeObj = document.mainform.doctorCharge;
	var opTypeObj = document.mainform.op_type;
	if (opTypeObj != null && opTypeObj.value == 'D') {
		if (doctorObj.value == '') {
			showMessage("js.registration.patient.follow.up.doctor.required");
			doctorObj.focus();
			return false;
		}
		if (doctorChargeObj.value != '') {
			showMessage("js.registration.patient.consultation.is.not.required.for.follow.up.without.consultation");
			doctorChargeObj.focus();
			return false;
		}
	}
	return true;
}

// Validate consultation validity only for follow up with cons.
function validateVisitValidity() {
	if (document.mainform.doctor != null) {
		var visitValid = setVisitType(document.mainform.doctor.value, gPreviousDocVisits);
		if (document.mainform.op_type) {
		 	if (!visitValid && (document.mainform.op_type.value == 'F')) {
				alert(getString("js.registration.patient.selected.doctor.visit.validity.has.expired.string")+
					"\n"+getString("js.registration.patient.select.visit.type.as.main.follow.up.no.cons.revisit.visit.string"));
				document.mainform.op_type.focus();
				return false;
		 	}
		}
	}
	return true;
}

function validatePatientCategory() {
	var patientCategoryObj	= document.mainform.patient_category_id;
	if (patientCategoryObj != null && patientCategoryObj.value == "") {
		alert(patientCategory+" "+getString("js.registration.patient.is.required.string"));
		patientCategoryObj.focus();
		return false;
	}
	return true;
}

function registerAndBill() {
	document.getElementById('registerBtn').focus();
	document.mainform.regAndBill.value = "Y";
	forFollowUps();
	setOtherInsObjects();
	if(!checkTransactionLimitValue()){
		disableOrEnableRegisterBtns(false);
		return false;
	}
	validateRegister();
}

function registerAndEditBill() {
	document.mainform.regAndBill.value = "E";
	document.getElementById('registerBtn').focus();
	forFollowUps();
	setOtherInsObjects();
	validateRegister();
}

function registervalidate() {
	document.mainform.regAndBill.value = "N";
	document.getElementById('registerBtn').focus();
	forFollowUps();
	setOtherInsObjects();
	validateRegister();
}

function disableOrEnableRegisterBtns(disable) {
	if (disable) {
		if (document.mainform.regBill != null) document.mainform.regBill.disabled = true;
		if (document.mainform.editBill != null) document.mainform.editBill.disabled = true;
		if (document.mainform.registerBtn != null) document.mainform.registerBtn.disabled = true;
	} else {
		if (document.mainform.regBill != null) document.mainform.regBill.disabled = false;
		if (document.mainform.editBill != null) document.mainform.editBill.disabled = false;
		if (document.mainform.registerBtn != null) document.mainform.registerBtn.disabled = false;
	}
}

// AJAX search to check these patient details already exists

function validateRegister() {

	//alert(document.mainform.primary_sponsor_id.value);
	if (max_centers>1 && centerId == 0) {
		showMessage("js.registration.patient.registration.allowed.only.for.center.users");
		return false;
	}
	if (!validateandregister()) return false;
	if (!validateOnDepPref()) return false;
	if (!validateCustomFields()) return false;
	if (!validateCreditLimitLength()) return false;
	if (!validateCreditLimitAmount()) return false;

	if(channellingOrders == null) {
	    if (!visitFieldsValidation()) return false;
	} else {
		//validate only department.
		var selIndex = document.mainform.dept_name.selectedIndex;
		if (selIndex == 0) {
			showMessage("js.registration.patient.consulting.department.required");
			document.mainform.dept_name.focus();
			return false;
		}
	}
	if (document.getElementById('regTyperegd').checked == true) {
		if (!validateFollowUpDoctor()) return false;
		if (!validateVisitValidity()) return false;
	}
	if (!validatePatientCategory()) return false;
	if (!validateOnChangePatientCategory()) return false;
	if (!uploadForms()) return false;

	var priPlanObj = getPrimaryPlanObj();
	var priAuthIdObj = getPrimaryAuthIdObj();
	var priAuthModeIdObj = getPrimaryAuthModeIdObj();

	var secPlanObj = getSecondaryPlanObj();
	var secAuthIdObj = getSecondaryAuthIdObj();
	var secAuthModeIdObj = getSecondaryAuthModeIdObj();

	if (isModAdvanceIns && priPlanObj != null && priPlanObj.value != ''
			&& !empty(priorAuthRequired) && trim(priAuthIdObj.value) == ""
			&& (priorAuthRequired=="A" || (priorAuthRequired=="I" && screenid == "ip_registration"))) {
		showMessage("js.registration.patient.prior.auth.no.required");
		priAuthIdObj.focus();
		return false;
	}

	if (priAuthModeIdObj != null && !empty(priAuthModeIdObj.value)) {
		if(!validatePriorAuthMode(null, null, priAuthIdObj.name, priAuthModeIdObj.name))
		return false;
	}
	if (isModAdvanceIns && secPlanObj != null && secPlanObj.value != ''
			&& !empty(priorAuthRequired) && trim(secAuthIdObj.value) == ""
			&& (priorAuthRequired=="A" || (priorAuthRequired=="I" && screenid == "ip_registration"))) {
		showMessage("js.registration.patient.prior.auth.no.required");
		secAuthIdObj.focus();
		return false;
	}

	if (secAuthModeIdObj != null && !empty(secAuthModeIdObj.value)) {
		if(!validatePriorAuthMode(null, null, secAuthIdObj.name, secAuthModeIdObj.name))
		return false;
	}


	//if (!validateMultiSponsorForPlanWithCopay()) return false;
	if (!validatePrimarySponsor()) return false;

	if (!validateInsuranceFields('P')) return false;

	if (!validateInsuranceFields('S')) return false;

	if (!validatePlan()) return false;

	if (!validateGovtIdentity()) return false;

	if (!validateMemberId()) return false;

	if (!validateOutpatientMandatoryfields()) return false;

	if (!validateBilltypeIfSecondarySponsor()) return false;
	if (!validateTestsAdditionalDetails())
		return false;

	if(channellingOrders != null) {
		addOrderDialog.multiVisitPackage = true;
		addOrderDialog.orderItemId = package_id;
	}
	if ((!empty(addOrderDialog)) && !addOrderDialog.validateMultiVisitPackageItems('')) return false;

	// Ins30 : Corporate insurance validations removed

	// if(corpInsuranceCheck == "Y"){
/*	    if (!setSelectedDateForCorpInsurance()) {
	    	alert(getString("js.registration.patient.primary.policy.date.na.validation"));
			document.getElementById("primary_plan_id").focus();
	    	return false;
	    } */

	    // if(!checkCurrentDateWithEndDateForCorpInsurance()) return false;
	// }

	if (document.mrnoform.group.value == "opreg") {
		document.mainform.group.value = "opreg";
	} else {
		document.mainform.group.value = "ipreg";
	}
	if (document.mainform.referaldoctorName.value == "")
		document.mainform.referred_by.value = "";

	if (!ratePlanApplicableForAppointment()) return false;

	if (!validateConductingDoctor()) return false;

	if (!validatePrescribingDoctor()) return false;

	if (!validatePatientIdentification()) return false;

	if (!validateGovtIdentifierMandatory()) return false;
	if ((document.mainform.appointmentId) && (document.mainform.appointmentId.value != '') && (document.mainform.appointmentId.value != '0')
			&& (document.mainform.category) && (document.mainform.category.value == 'OPE' && !empty(scheduleName))) {
		if(advancedOTModule == "Y") {
		} else
			alert(getString("js.registration.patient.patient.has.surgery.scheduled.string")+" "+"\n"+getString("js.registration.patient.surgery.will.be.scheduled.string"));
	}

	if (document.getElementById('regTyperegd').checked == true) {
		return register();
	}
	var middleName2='';
	var firstName = trim(document.getElementById('patient_name').value);
	var middleName = trim(document.getElementById('middle_name').value);
	if(regPref.name_parts == 4)
		middleName2 = trim(document.getElementById('middle_name2').value);
	var lastName = trim(document.getElementById('last_name').value);

	if (middleName == '' || middleName == "..MiddleName..") middleName = '';
	if (middleName2 == '' || middleName2 == "..MiddleName2..") middleName2 = '';
	if (lastName == '' || lastName == "..LastName..") lastName = '';
	middleName = trim(middleName+" "+middleName2);
	var gender = document.getElementById('patient_gender').options[document.getElementById('patient_gender').options.selectedIndex].value;
	var age = document.getElementById('age').value;
	var dob = document.mainform.dateOfBirth.value;
	var phno = document.getElementById('patient_phone').value;
	if(null!=document.getElementById('government_identifier'))
		var nationalId = document.getElementById('government_identifier').value;
	else
		var nationalId = "";
	var url = "../../pages/registration/regUtils.do?_method=checkPatientDetailsExists&firstName=" + firstName
			+ "&middleName=" + middleName + "&lastName=" + lastName + "&gender=" + gender + "&age=" + age + "&dob=" + dob + "&phno=" + encodeURIComponent(phno) + "&govt_identifier=" + nationalId;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			getPatientInfo(reqObject.responseText);
		}
	}
	return null;
}

function checkCurrentDateWithEndDateForCorpInsurance() {
    var EnteredDate = '';
	if(document.getElementById("primary_policy_validity_end1") != null)
           EnteredDate = document.getElementById("primary_policy_validity_end1").value; //for javascript

    var date = EnteredDate.substring(0, 2);
    var month = EnteredDate.substring(3, 5);
    var year = EnteredDate.substring(6, 10);
    var myDate = new Date(year, month - 1, date);

    var curDate = new Date();
   	if (gServerNow != null) {
		curDate.setTime(gServerNow);
	}
	curDate.setHours(0);
	curDate.setMinutes(0);
	curDate.setSeconds(0);
	curDate.setMilliseconds(0);

    if(EnteredDate != ''){
    if (myDate < curDate) {
        alert(getString("js.registration.patient.policy.validity.has.expired.corpInsurance"));
        if(document.getElementById("primary_plan_id") != null)
	        document.getElementById("primary_plan_id").focus();
			document.getElementById("primary_plan_id").selectedIndex = '0';

		if(document.getElementById('primary_policy_validity_start') != null)
				document.getElementById('primary_policy_validity_start').textContent = '';

		if(document.getElementById('primary_policy_validity_end') != null)
				document.getElementById('primary_policy_validity_end').textContent = '';

		return false;
     } else {
         return true;
		}
	}
 return true;
}


var existingPatientDetails = null;
var gotPatDetails = false;

function getPatientInfo(responseData) {
	existingPatientDetails = null;
	eval("existingPatientDetails =" + responseData);
	if (!empty(existingPatientDetails)) {
		var eobirth = empty(existingPatientDetails.eob) ? '' : existingPatientDetails.eob;
		var dobirth = empty(existingPatientDetails.dob) ? '' : existingPatientDetails.dob;

		var age = empty(existingPatientDetails.age) ? '' : existingPatientDetails.age;
		var agein = empty(existingPatientDetails.agein) ? '' : existingPatientDetails.agein;

		var ageAndDOB = age +" "+ agein;
		if (document.mainform.dateOfBirth.value != '' && document.mainform.dateOfBirth.value != null) {
			ageAndDOB = ageAndDOB + " / " + (empty(dobirth) ? eobirth : dobirth) ;
		}

		var area = empty(existingPatientDetails.patient_area) ? '' : existingPatientDetails.patient_area;
		var middleName = empty(existingPatientDetails.middle_name) ? '' : existingPatientDetails.middle_name;
		var lastName = empty(existingPatientDetails.last_name) ? '' : existingPatientDetails.last_name;
		var phoneNo = empty(existingPatientDetails.patient_phone) ? '' : existingPatientDetails.patient_phone;
		var nationalId = empty(existingPatientDetails.government_identifier) ? '' :existingPatientDetails.government_identifier;

		if (confirm(getString("js.registration.patient.patient.with.following.details.already.exists.string")+" " +
					" \n " + " \n "+getString("js.registration.patient.mr.no.string")+" " + existingPatientDetails.mr_no +
					" \n " +getString("js.registration.patient.national.id.string")+" " + nationalId +
					" \n " +getString("js.registration.patient.first.name.string")+" " + existingPatientDetails.patient_name +
					" \n " +getString("js.registration.patient.middle.name.string")+" " + middleName +
					" \n " +getString("js.registration.patient.last.name.string")+" "+ lastName +
					" \n " +getString("js.registration.patient.gender.string")+" "+ existingPatientDetails.patient_gender  +
					" \n " +getString("js.registration.patient.age.date.of.birth.string")+" "+ ageAndDOB +
					" \n " +getString("js.registration.patient.phone.string")+" " +phoneNo +
					" \n " +getString("js.registration.patient.address.string")+" "+ existingPatientDetails.patient_address +
					" \n " +getString("js.registration.patient.city.string")+" "+ existingPatientDetails.city_name +
					" \n " +getString("js.registration.patient.area.string")+" " + area + " \n " + " \n " +
					getString("js.registration.patient.are.you.want.to.register.a.new.patient.string"))) {

			register();
			return true;
		} else {
			document.getElementById('regTypenew').checked = false;
			document.getElementById('regTyperegd').checked = true;
			clearRegDetails();
			document.mrnoform.mrno.value = existingPatientDetails.mr_no;

			var form = document.mainform;
			var primaryResource = getSchedulerPrimaryResource();
			if (primaryResource != null) {
				form.appointmentId.value = primaryResource.appointment_id;
				form.category.value = primaryResource.category;
				insertNumberIntoDOM(primaryResource.patient_contact,$("#patient_phone"),$("#patient_phone_country_code"),
		    			$("#patient_phone_national"));
				form.ailment.value = primaryResource.complaint;
			}
			getRegDetails();
			cardOverwrite();

		}

	} else {
		register();
	}
}

function updateNationalityId(natioanlity) {
	var nationalityCode = cpath + "/master/countries/lookup.json?nationality=" + natioanlity;
	$.ajax({
		url : nationalityCode,
		dataType : 'json',
		success : function(response) {
			if(response && response.nationality) {
				$('#nationality_id').val(response.nationality.country_id);
			}
		}
	});
}

function cardOverwrite() {

	if(null != dt && "" != dt) {
		document.getElementById("patient_name").value = dt["fName"];
		if(regPref.name_parts == 4 && dt["mName1"]) {
			document.getElementById("middle_name").value = dt["mName1"];
			document.getElementById("middle_name2").value = dt["mName2"];
		} else {
			document.getElementById("middle_name").value = dt["mName"];
		}
		document.getElementById("last_name").value = dt["lName"];
		if(null	!=	document.getElementById("government_identifier"))
			document.getElementById("government_identifier").value = dt["idNumber"];
		document.getElementById("dobDay").value = dt["dateOfBirth"].substring(0,2);
		document.getElementById("dobMonth").value = dt["dateOfBirth"].substring(3,5);
		document.getElementById("dobYear").value = dt["dateOfBirth"].substring(6,10);
		calculateAgeAndHijri();
		if(null	!=	document.getElementById("identifier_id")) {
				document.getElementById("identifier_id").disabled = true;
		}
		if(dt["maritalStatus"]=="Married" && dt["sex"]=="F" ) {
			document.getElementById("salutation").value = "SALU0003";
			document.getElementById("patient_gender").value = "F";
		}
		else if(dt["sex"]=="F") {
			document.getElementById("salutation").value = "SALU0004";
			document.getElementById("patient_gender").value = "F";
		}
		else if(dt["sex"]=="M") {
			document.getElementById("salutation").value = "SALU0002";
			document.getElementById("patient_gender").value = "M";
		}

		document.getElementById("resource_captured_from").value="card";
		document.getElementById("cardImage").value=dt["photo"];
		document.getElementById("patPhoto").value=null;
		if(null != dt["address"] && dt["address"] != "")
			document.getElementById("patient_address").value = dt["address"];
// 		if(null != dt["area"] && dt["area"] != "")
// 			document.getElementById("patient_area").value = dt["area"];
		if(null != dt["contactNo"] && dt["contactNo"] != "") {
			document.getElementById("patient_phone2").value=dt["contactNo"];
		}

		if (null != dt["nationality"] && dt["nationality"] != "" && null != document.getElementById('nationality_id')) {
			updateNationalityId(dt["nationality"]);
		}

	}
}

function ratePlanApplicableForAppointment() {

	var appointmentId = "", scheduleId = "", scheduleName = "", category = "";
	var consultationTypeId = "";
	var primaryResource = getSchedulerPrimaryResource();

	if (!empty(primaryResource)) {
		for (var i = 0; i < appointmentDetailsList.length; i++) {
			var category = appointmentDetailsList[i].category;
			var isPrimary = appointmentDetailsList[i].primary_resource;
			if (isPrimary) {
				appointmentId	= appointmentDetailsList[i].appointment_id;
				category		= appointmentDetailsList[i].category;
				scheduleId		= appointmentDetailsList[i].res_sch_name;
				scheduleName	= appointmentDetailsList[i].central_resource_name;
				consultationTypeId	= appointmentDetailsList[i].consultation_type_id;
				break;
			}
		}
	}

	if (empty(appointmentId) || appointmentId == '0') return true;

	if (category == 'DOC' && (empty(consultationTypeId) || consultationTypeId == 0 || consultationTypeId == '0')) return true;
	else if (empty(scheduleId)) return true;

	var organizationObj = document.mainform.organization;

	var orgId = organizationObj.options[organizationObj.options.selectedIndex].value;
	var orgName = organizationObj.options[organizationObj.options.selectedIndex].text;

	orgId = empty(orgId) ? 'ORG0001' : orgId;
	orgName = empty(orgName) ? 'GENERAL' : orgName;

	var reqObject = newXMLHttpRequest();
	var url = cpath + "/pages/registration/regUtils.do?_method=isRatePlanApplicable&category=" +
		category + "&orgId=" + orgId;

	if (category == 'DOC')
		url += "&schedule_id=" + consultationTypeId;
	else
		url += "&schedule_id=" + scheduleId;

	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			if (!empty(reqObject.responseText)) {
				if (reqObject.responseText != 't') {

					var schMsg = " "+getString("js.registration.patient.patient.scheduled");
					var msg = getString("js.registration.patient.is.not.applicable.with.rate.plan");

					if (category == 'SNP') {
						alert(schMsg +" "+ getString("js.registration.patient.patient.service") +": "+scheduleName +" \n "+ msg +" : " + orgName);
						return false;
					} else if (category == 'DIA') {
						alert(schMsg +" "+ getString("js.registration.patient.patient.test") +": "+scheduleName +" \n "+ msg +" : " + orgName);
						return false;
					} else if (category == 'OPE') {
						alert(schMsg +" "+ getString("js.registration.patient.patient.surgery") +": "+scheduleName +" \n "+ msg +" : " + orgName);
						return false;
					} else if (category == 'DOC') {
						var consultationType = "";

						var consTypes		   = cachedConsultTypes[orgId];
						var consultation	   = findInList(consTypes, "consultation_type_id", consultationTypeId);
						if (empty(consultation)) {
							consTypes		   = cachedConsultTypes["ORG0001"];
							consultation 	   = findInList(consTypes, "consultation_type_id", consultationTypeId);
						}
						consultationType  = (!empty(consultation)) ? consultation.consultation_type : "";

						if (consultationType == "") return true;

						alert(schMsg +" "+ getString("js.registration.patient.patient.consultation") +": "+consultationType +" \n "+ msg +" : " + orgName);
						return false;
					}
				}
			}
		}
	}
	return true;
}


function validateEmptyOrders() {
	return true;
}


function register() {

	if (!validateEmptyOrders()) return false;

	document.mainform.patient_area.value = trimAll(document.mainform.patient_area.value);
	//if (!validateCategoryExpiryDate()) return false;

	if (screenid == "ip_registration") {
		document.mainform.action = 'IpRegistration.do';
	} else {
		document.mainform.action = 'outPatientRegistration.do';
	}

	document.mainform._method.value = "doRegister";
	document.getElementById('patient_name').disabled = false;

	enableDisableDateOfBirthFields('dob', false);
	document.mainform.patient_gender.disabled = false;

	enableCustomLists();
	// Pass the forceDisabled value as false so that while registration, the disabled insurance fields are enabled.
	disableOrEnableInsuranceFields(false, false);
	if (document.mainform.op_type) document.mainform.op_type.disabled = false;
	document.getElementById('referaldoctorName').disabled = false;
	if (screenid != "out_pat_reg") document.getElementById('ailment').disabled = false;

	disableOrEnableRegisterBtns(true);

	// For tracking Bill Type
	if (document.getElementById('bill_type') != null) {
		var billTypeValue = document.getElementById('bill_type').value === 'P' ? 'Bill Now' : 'Bill Later';
		eventTracking("Registration", "Bill Type", billTypeValue);
	}

	formData = new FormData(document.mainform);
	for(var key in pastedImages) {
			formData.append(key, pastedImages[key]['blob']);
			formData.append(key+"_contentType" ,pastedImages[key]['contentType']);
	}
	document.mainform.setAttribute("action", document.mainform.getAttribute('action') + '?_method=doRegister');
	document.mainform.setAttribute("method" ,"POST");
	document.mainform.submit();
} //end of register

function getRegDetailsOnMrnoNoChange() {
	// Reset the selected doctor on mrno change.
	gSelectedDoctorName = null;
	gSelectedDoctorId = null;
	var mrno = document.mrnoform.mrno.value;
	var newReg = isNewReg();
	if (!newReg && !empty(gMrNo) && trim(gMrNo) != '' && trim(mrno) != '' && trim(gMrNo) != trim(mrno)) {
		clearRegDetails();
	}
	document.mrnoform.mrno.value = mrno;
	getRegDetails();
}

function getRegDetails() {
	clearTodaysAppointmentDetails();
	var mrno = document.mrnoform.mrno.value;
	var mainVisitId = document.mainform.main_visit_id.value;
	var isFollowUpNoCons = (document.mainform.op_type != null && document.mainform.op_type.value == "D");

	if(conflictCheckFlag==true) {
		var ajaxobj = newXMLHttpRequest();
		var url = '../../pages/registration/regUtils.do?_method=photo&mrno=' + mrno;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200)) {
					eval("var patientPhoto =" + ajaxobj.responseText);
					if(patientPhoto != "" && null != patientPhoto)
						document.getElementById("img_sys").src = "data:image/jpeg;base64," + patientPhoto;
					else
						document.getElementById("img_sys").src = cpath + "/images/patienPlaceholder.png";
					if(patientPhoto!=dt['photo']) {
						smartConflict = true;
					}
				}
			}
		}
	}

	var ajaxobj1 = newXMLHttpRequest();
	var url1 = '../../pages/registration/regUtils.do?_method=getPatientDetailsJSON&mrno=' + mrno;
	url1 = url1 + '&reg_screen_id='+screenid;
	(isDoctorChange || isFollowUpNoCons) ? url1 = url1+'&patient_id='+mainVisitId : '';
	getResponseHandlerText(ajaxobj1, patientDetailsResponseHandler, url1.toString());
}

function empty(obj) {
	if (obj == null || obj == undefined || obj == '' || obj == 'undefined') return true;
}

function clearPreviousPatientDetails() {
	patient = null;
	gLastVisitId = null;
	gLastVisitIdInThisCenter = null;
	gVisitId = null;
	gConsultationStatus = null;
	gPreviousVisitDoctor = null;
	gPreviousVisitDept = null;
	gMrNo = null;

	gPreviousPrimarySponsorIndex = "";
	gPreviousSecondarySponsorIndex = "";

	gPreviousPrimaryInsCompany = null;
	gPreviousPrimaryTpa = null;

	gPreviousPlan = null;
	gPreviousPlanType = null;
	gPreviousMemberId = null;
	gPreviousPolicyNumber = null;
	gPreviousHolder = null;
	gPreviousRelation = null;
	gPreviousStartDate = null;
	gPreviousEndDate = null;
	gPreviousPatientPolicyId = null;
	gPreviousPriorauthid = null;
	gPreviousPriorauthmodeid = null;

	gPreviousSecondaryInsCompany = null;
	gPreviousSecondaryTpa = null;

	gPreviousSecPlan = null;
	gPreviousSecPlanType = null;
	gPreviousSecMemberId = null;
	gPreviousSecPolicyNumber = null;
	gPreviousSecHolder = null;
	gPreviousSecRelation = null;
	gPreviousSecStartDate = null;
	gPreviousSecEndDate = null;
	gPreviousSecPatientPolicyId = null;
	gPreviousSecPriorauthid = null;
	gPreviousSecPriorauthmodeid = null;

	gPreviousCorporateRelation = null;
	gPreviousCorporateSponsorId = null;
	gPreviousCorporateEmployeeId = null;
	gPreviousCorporateEmployeeName = null;
	gPreviousNationalSponsorId = null;
	gPreviousNationalId = null;
	gPreviousNationalCitizenName = null;
	gPreviousNationalRelation = null;

	gPreviousSecCorporateRelation = null;
	gPreviousSecCorporateSponsorId = null;
	gPreviousSecCorporateEmployeeId = null;
	gPreviousSecCorporateEmployeeName = null;
	gPreviousSecNationalSponsorId = null;
	gPreviousSecNationalId = null;
	gPreviousSecNationalCitizenName = null;
	gPreviousSecNationalRelation = null;

	gPreviousPatientCategoryId = null;
	gPreviousPatientCategoryExpDate = null;
	gPreviousRatePlan = null;

	gPatientComplaint = null;

	gPatientLastIpVisit = null;
	gFollowUpDocVisits = null;
	gLastGenRegChargeAcceptedDate = null;
	gPatientRegDate = null;
	gPatientRegDateRaw = null;
	gPatientPolciyNos = null;
	gPatientCorporateIds = null;
	gPatientNationalIds = null;
	gPreviousDocVisits = null;

	gTestPrescriptions = null;
	gServicePrescriptions = null;
	gConsultationPrescriptions = null;
	gDietPrescriptions = null;
	gStandingInstructions = null;
	gOperationPrescriptions = null;

	gPatientBillsApprovalTotal = null;
	gPrevVisitDues = null;
	gPatientFamilyBillsTotal = null;
	gPatMultiVisitComponentDetails = null;
	gPatMultiVisitConsumedDetails = null;
	gMvPackageIds = null;

}


var gLastVisitId = null;
var gLastVisitIdInThisCenter = null;
var gVisitId = null;
var gConsultationStatus = null;
var gPreviousVisitDoctor = null;
var gPreviousVisitDept = null;
var gMrNo = null;

var gPreviousPrimarySponsorIndex = "";
var gPreviousSecondarySponsorIndex = "";

var gPreviousPrimaryInsCompany = null;
var gPreviousPrimaryTpa = null;

var gPreviousPlan = null;
var gPreviousPlanType = null;
var gPreviousMemberId = null;
var gPreviousPolicyNumber = null;
var gPreviousHolder = null;
var gPreviousRelation = null;
var gPreviousStartDate = null;
var gPreviousEndDate = null;
var gPreviousPatientPolicyId = null;
var gPreviousPriorauthid = null;
var gPreviousPriorauthmodeid = null;

var gPreviousSecondaryInsCompany = null;
var gPreviousSecondaryTpa = null;

var gPreviousSecPlan = null;
var gPreviousSecPlanType = null;
var gPreviousSecMemberId = null;
var gPreviousSecPolicyNumber = null;
var gPreviousSecHolder = null;
var gPreviousSecRelation = null;
var gPreviousSecStartDate = null;
var gPreviousSecEndDate = null;
var gPreviousSecPatientPolicyId = null;
var gPreviousSecPriorauthid = null;
var gPreviousSecPriorauthmodeid = null;

var gPreviousCorporateRelation = null;
var gPreviousCorporateSponsorId = null;
var gPreviousCorporateEmployeeId = null;
var gPreviousCorporateEmployeeName = null;
var gPreviousNationalSponsorId = null;
var gPreviousNationalId = null;
var gPreviousNationalCitizenName = null;
var gPreviousNationalRelation = null;

var gPreviousSecCorporateRelation = null;
var gPreviousSecCorporateSponsorId = null;
var gPreviousSecCorporateEmployeeId = null;
var gPreviousSecCorporateEmployeeName = null;
var gPreviousSecNationalSponsorId = null;
var gPreviousSecNationalId = null;
var gPreviousSecNationalCitizenName = null;
var gPreviousSecNationalRelation = null;

var gPreviousPatientCategoryId = null;
var gPreviousPatientCategoryExpDate = null;
var gPreviousRatePlan = null;

var gPatientComplaint = null;

var gPatientLastIpVisit = null;
var gFollowUpDocVisits = null;
var gLastGenRegChargeAcceptedDate = null;
var gPatientRegDate = null;
var gPatientRegDateRaw = null;
var gPatientPolciyNos = null;
var gAllPatientPolciyNos = null;
var gPatientCorporateIds = null;
var gPatientNationalIds = null;
var gPreviousDocVisits = null;

var gTestPrescriptions = null;
var gServicePrescriptions = null;
var gConsultationPrescriptions = null;
var gDietPrescriptions = null;
var gStandingInstructions = null;
var gOperationPrescriptions = null;

var gPatientBillsApprovalTotal = null;
var gPrevVisitDues = null;
var gPatientFamilyBillsTotal = null;
var gPatientDepositAmt = null;

var patient = null;
var filter = '';
var gPatMultiVisitComponentDetails = null;
var gPatMultiVisitConsumedDetails = null;
var gMvPackageIds = null;
function loadPatientResponseDetails(patientInfo, patient) {
	gLastVisitId = patientInfo.lastVisitId;
	gLastVisitIdInThisCenter = patientInfo.lastVisitIdInThisCenter;
	gConsultationStatus = patientInfo.consultation_status;
	gPreviousVisitDoctor = patient.doctor;
	gPreviousVisitDept = patient.dept_id;
	gMrNo = patient.mr_no;
	gPatientPolciyNos = patientInfo.policyNos;
	gAllPatientPolciyNos = patientInfo.allPolicyNos;
	gVisitId = patient.patient_id;

	if (gPatientPolciyNos.length == 2 ) {

		var primaryPlanDetais = gPatientPolciyNos[0];
		var secondaryPlanDetails = gPatientPolciyNos[1];

		gPreviousPrimaryInsCompany = primaryPlanDetais.insurance_co;
		gPreviousPrimaryTpa = primaryPlanDetais.sponsor_id;
		gPreviousPlan = primaryPlanDetais.plan_id;
		gPreviousPlanType = primaryPlanDetais.plan_type_id;
		gPreviousMemberId = !empty(primaryPlanDetais.member_id) ? primaryPlanDetais.member_id : null;
		gPreviousPolicyNumber = !empty(primaryPlanDetais.policy_number) ? primaryPlanDetais.policy_number : null;
		gPreviousHolder = !empty(primaryPlanDetais.policy_holder_name) ? primaryPlanDetais.policy_holder_name : null;
		gPreviousRelation = !empty(primaryPlanDetais.patient_relationship) ? primaryPlanDetais.patient_relationship : null;
		gPreviousStartDate = primaryPlanDetais.policy_validity_start;
		gPreviousEndDate = primaryPlanDetais.policy_validity_end;
		gPreviousPatientPolicyId = primaryPlanDetais.patient_policy_id;
		gPreviousPriorauthid = primaryPlanDetais.prior_auth_id;
		gPreviousPriorauthmodeid = primaryPlanDetais.prior_auth_mode_id;

		gPreviousSecondaryInsCompany = secondaryPlanDetails.insurance_co;
		gPreviousSecondaryTpa = secondaryPlanDetails.sponsor_id;
		gPreviousSecPlan = secondaryPlanDetails.plan_id;
		gPreviousSecPlanType = secondaryPlanDetails.plan_type_id;
		gPreviousSecMemberId = !empty(secondaryPlanDetails.member_id) ? secondaryPlanDetails.member_id : null;
		gPreviousSecPolicyNumber = !empty(secondaryPlanDetails.policy_number) ? secondaryPlanDetails.policy_number : null;
		gPreviousSecHolder = !empty(secondaryPlanDetails.policy_holder_name) ? secondaryPlanDetails.policy_holder_name : null;
		gPreviousSecRelation = !empty(secondaryPlanDetails.patient_relationship) ? secondaryPlanDetails.patient_relationship : null;
		gPreviousSecStartDate = secondaryPlanDetails.policy_validity_start;
		gPreviousSecEndDate = secondaryPlanDetails.policy_validity_end;
		gPreviousSecPatientPolicyId = secondaryPlanDetails.patient_policy_id;
		gPreviousSecPriorauthid = secondaryPlanDetails.prior_auth_id;
		gPreviousSecPriorauthmodeid = secondaryPlanDetails.prior_auth_mode_id;
		gPreviousPrimarySponsorIndex = 'I';
		gPreviousSecondarySponsorIndex = 'I';

	} else if (gPatientPolciyNos.length == 1) {

		if (!empty(patient.corporate_sponsor_id) || !empty(patient.national_sponsor_id) ) {

			var secondaryPlanDetails = gPatientPolciyNos[0];

			gPreviousSecondaryInsCompany = secondaryPlanDetails.insurance_co;
			gPreviousSecondaryTpa = secondaryPlanDetails.sponsor_id;
			gPreviousSecPlan = secondaryPlanDetails.plan_id;
			gPreviousSecPlanType = secondaryPlanDetails.plan_type_id;
			gPreviousSecMemberId = !empty(secondaryPlanDetails.member_id) ? secondaryPlanDetails.member_id : null;
			gPreviousSecPolicyNumber = !empty(secondaryPlanDetails.policy_number) ? secondaryPlanDetails.policy_number : null;
			gPreviousSecHolder = !empty(secondaryPlanDetails.policy_holder_name) ? secondaryPlanDetails.policy_holder_name : null;
			gPreviousSecRelation = !empty(secondaryPlanDetails.patient_relationship) ? secondaryPlanDetails.patient_relationship : null;
			gPreviousSecStartDate = secondaryPlanDetails.policy_validity_start;
			gPreviousSecEndDate = secondaryPlanDetails.policy_validity_end;
			gPreviousSecPatientPolicyId = secondaryPlanDetails.patient_policy_id;
			gPreviousSecPriorauthid = secondaryPlanDetails.prior_auth_id;
			gPreviousSecPriorauthmodeid = secondaryPlanDetails.prior_auth_mode_id;

			gPreviousSecondarySponsorIndex = 'I';
			gPreviousPrimarySponsorIndex = (!empty(patient.corporate_sponsor_id)) ? 'C' : 'N';
			gPreviousPrimaryTpa = patient.primary_sponsor_id;

		} else if ((!empty(patient.sec_corporate_sponsor_id) || !empty(patient.sec_national_sponsor_id))
						|| (empty(patient.sec_corporate_sponsor_id) && empty(patient.sec_national_sponsor_id))) {

			var primaryPlanDetais = gPatientPolciyNos[0];

			gPreviousPrimaryInsCompany = primaryPlanDetais.insurance_co;
			gPreviousPrimaryTpa = primaryPlanDetais.sponsor_id;
			gPreviousPlan = primaryPlanDetais.plan_id;
			gPreviousPlanType = primaryPlanDetais.plan_type_id;
			gPreviousMemberId = !empty(primaryPlanDetais.member_id) ? primaryPlanDetais.member_id : null;
			gPreviousPolicyNumber = !empty(primaryPlanDetais.policy_number) ? primaryPlanDetais.policy_number : null;
			gPreviousHolder = !empty(primaryPlanDetais.policy_holder_name) ? primaryPlanDetais.policy_holder_name : null;
			gPreviousRelation = !empty(primaryPlanDetais.patient_relationship) ? primaryPlanDetais.patient_relationship : null;
			gPreviousStartDate = primaryPlanDetais.policy_validity_start;
			gPreviousEndDate = primaryPlanDetais.policy_validity_end;
			gPreviousPatientPolicyId = primaryPlanDetais.patient_policy_id;
			gPreviousPriorauthid = primaryPlanDetais.prior_auth_id;
			gPreviousPriorauthmodeid = primaryPlanDetais.prior_auth_mode_id;

			gPreviousPrimarySponsorIndex = 'I';
			gPreviousSecondarySponsorIndex = (!empty(patient.sec_corporate_sponsor_id)) ? 'C' : (!empty(patient.sec_national_sponsor_id) ? 'N' : '');
			gPreviousSecondaryTpa = patient.secondary_sponsor_id;

		}


	} else {
		gPreviousPrimarySponsorIndex = !empty(patient.sponsor_type) ? patient.sponsor_type : '';
		gPreviousSecondarySponsorIndex = !empty(patient.sec_sponsor_type) ? patient.sec_sponsor_type : '';
		gPreviousPrimaryTpa = patient.primary_sponsor_id;
		gPreviousSecondaryTpa = patient.secondary_sponsor_id;
	}

	gPreviousCorporateRelation =  !empty(patient.patient_corporate_relation) ? patient.patient_corporate_relation : null;
	gPreviousCorporateSponsorId = !empty(patient.corporate_sponsor_id) ? patient.corporate_sponsor_id : null;
	gPreviousCorporateEmployeeId = !empty(patient.employee_id) ? patient.employee_id : null;
	gPreviousCorporateEmployeeName =  !empty(patient.employee_name) ? patient.employee_name : null;
	gPreviousNationalSponsorId = !empty(patient.national_sponsor_id) ? patient.national_sponsor_id : null;
	gPreviousNationalId = !empty(patient.national_id) ? patient.national_id : null;
	gPreviousNationalCitizenName = !empty(patient.citizen_name) ? patient.citizen_name : null;
	gPreviousNationalRelation = !empty(patient.patient_national_relation) ? patient.patient_national_relation : null;

	gPreviousSecCorporateRelation =  !empty(patient.sec_patient_corporate_relation) ? patient.sec_patient_corporate_relation : null;
	gPreviousSecCorporateSponsorId = !empty(patient.sec_corporate_sponsor_id) ? patient.sec_corporate_sponsor_id : null;
	gPreviousSecCorporateEmployeeId = !empty(patient.sec_employee_id) ? patient.sec_employee_id : null;
	gPreviousSecCorporateEmployeeName =  !empty(patient.sec_employee_name) ? patient.sec_employee_name : null;
	gPreviousSecNationalSponsorId = !empty(patient.sec_national_sponsor_id) ? patient.sec_national_sponsor_id : null;
	gPreviousSecNationalId = !empty(patient.sec_national_id) ? patient.sec_national_id : null;
	gPreviousSecNationalCitizenName = !empty(patient.sec_citizen_name) ? patient.sec_citizen_name : null;
	gPreviousSecNationalRelation = !empty(patient.sec_patient_national_relation) ? patient.sec_patient_national_relation : null;

	gFollowUpDocVisits = patientInfo.followUpDocVisits;
	gPatientLastIpVisit = patientInfo.patientLastIpVisit;

	gPatientCorporateIds = patientInfo.corporateIds;
	gPatientNationalIds = patientInfo.nationalIds;
	gPreviousDocVisits = patientInfo.previousDocVisits;
	gLastGenRegChargeAcceptedDate = new Date(patientInfo.recentGenRegChargePostedDate);
	gPatientRegDate = new Date(patient.reg_date);
	gPatientRegDateRaw = patient.reg_date;
	gTestPrescriptions = patientInfo.previousTestPrescriptions;
	gServicePrescriptions = patientInfo.previousServicePrescriptions;
	gConsultationPrescriptions = patientInfo.previousConsultationPrescriptions;

	gDietPrescriptions = '';
	gStandingInstructions = '';
	gOperationPrescriptions = '';

	gPatientBillsApprovalTotal = patientInfo.billsApprovalTotal;
	gPrevVisitDues = !empty(patientInfo.previousVisitDues) ? patientInfo.previousVisitDues : null;
	gPatientDepositAmt = !empty(patientInfo.patDepositAmount) ? patientInfo.patDepositAmount : null;
	gPatientFamilyBillsTotal = patientInfo.patientFamilyBillsTotal;
	gPatMultiVisitComponentDetails = patientInfo.patientMultiVisitPacakgeComponentDetails;
	gPatMultiVisitConsumedDetails = patientInfo.patientMultiVisitPacakgeConsumedDetails;
	gMvPackageIds = patientInfo.mvPackageIds;
	// If General registration patient then patient category is patient.patient_category_id else patient.patient_category
	var patientCategoryId;
	if(patient_reg_basis=='P')
		{
			patientCategoryId = patient.patient_category_id;
		}
	else
		patientCategoryId = !empty(patient.patient_category) ? patient.patient_category : patient.patient_category_id;


	if(!empty(patient))
		insured = !empty(patient.primary_sponsor_id);
	if (empty(patient.visit_status) || patient.visit_status == 'N')
		patientCategoryId = !empty(patient.patient_category_id) ? patient.patient_category_id : 1;

	gPreviousPatientCategoryId = patientCategoryId;
	gPreviousPatientCategoryExpDate = !empty(patient.category_expiry_date) ? patient.category_expiry_date : null;
	gPreviousRatePlan = patient.org_id;

	gPatientComplaint = !empty(patient.complaint) ? patient.complaint : "";

	/*
	 * Got the patient, now, set the patient's info in the details section
	 */
	var form = document.mainform;
	setSelectedIndex(form.salutation, patient.salutation_id);
	form.patient_name.value = patient.patient_name;
	// for four parts name while we editing registration through mr_no auto-complete we can
	//populate the values in to text boxes by splitting the middle name & show in different text boxes
	var middle_name = patient.middle_name;
	if(regPref.name_parts == 4){
		if(middle_name != null && middle_name !='' && middle_name != undefined){
			if(middle_name.match(" ")){
				form.middle_name.value = middle_name.substr(0,middle_name.indexOf(" "));
				form.middle_name2.value = middle_name.substr(middle_name.indexOf(" ")+1,middle_name.length-1);
			}
			else
				form.middle_name.value = middle_name;
		}
	}
	else {
		form.middle_name.value = patient.middle_name;
	}
	form.last_name.value = patient.last_name;
	if(regPref.name_local_lang_required == 'Y')
		form.name_local_language.value = patient.name_local_language;

	form.resource_captured_from.value =  "register";
	if (patient.resource_captured_from) {
		form.resource_captured_from.value = patient.resource_captured_from;
	} else if (smartCardDetails && Object.keys(smartCardDetails).indexOf("error") === -1) {
		form.resource_captured_from.value =  "card";
	}
	if ((roleId == 1 || roleId == 2) || (editFirstName == "A") && (roleId != 1 || roleId != 2)) {
		document.getElementById('patient_name').disabled = false;
		enableDisableDateOfBirthFields('dob', false);

		form.patient_gender.disabled = false;
	} else {
		document.getElementById('patient_name').disabled = true;
		enableDisableDateOfBirthFields('dob', true);
		/*form.age.disabled = true;
		form.ageIn.disabled = true;*/
		form.patient_gender.disabled = true;
	}

	if ((roleId == 1 || roleId == 2) || (catChangeRights == "A") && (roleId != 1 || roleId != 2)) {
		if (form.patient_category_id) form.patient_category_id.disabled = false;
		if (form.category_expiry_date) form.category_expiry_date.readOnly = false;
	} else {
		if (form.patient_category_id) form.patient_category_id.disabled = true;
		if (form.category_expiry_date) form.category_expiry_date.readOnly = true;
	}

	if (patient.vip_status != null) {
		document.mainform.vip_check.checked = patient.vip_status == 'Y';
		enableVipStatus();
	}
	if (patient.dateofbirth != null) {
		setDateOfBirthFields('dob', new Date(patient.dateofbirth));
		if(hijriPref == 'Y') {
		    gregorianToHijri();
		}
	} else {
		setDateOfBirthFields('dob', null);
	}
	if (gLastVisitId != null) {
		form.previousVisit.value = "Y";
	} else {
		form.previousVisit.value = "N";
	}
	form.age.value = patient.age;
	form.patient_phone2.value = patient.addnl_phone;
	form.patient_area.value = patient.patient_area;
	if (form.mobilePatAccess) {
	  form.mobilePatAccess.value = 'N';
	  if (isPatientPhoneMandate != 'Y') {
			$(".patient_phone_star").hide();
		}
		if (isPatientEmailMandate != 'Y') {
			$(".patient_email_star").hide();
		}
	}
	checkTypeOfReg();

	if (allowMultipleActiveVisits == 'Y') {
		// Set the previous visit id to be closed if visit is OP/IP/OUTSIDE visit and Active.
		if ((patient.visit_status != null) && (patient.visit_status == 'A') && gLastVisitIdInThisCenter != null
			&& !empty(patient.visit_type) && (patient.visit_type == 'o' || patient.visit_type == 'i')) {
			document.mrnoform.close_last_active_visit.disabled = false;
			document.mrnoform.close_last_active_visit.checked = true;
		} else {
			document.mrnoform.close_last_active_visit.disabled = true;
			document.mrnoform.close_last_active_visit.checked = false;
		}
		setLastVisitToClose();
	}

	// Set the patient category and category expiry date.
	var patientCategoryObj = document.mainform.patient_category_id;
	var patientCategoryExpDtObj = document.mainform.category_expiry_date;

	if (!empty(gPreviousPatientCategoryId) && patientCategoryObj) {
		var isMain = ((document.mainform.op_type && document.mainform.op_type.value == "M") || empty(document.mainform.op_type));
		var isRevisit = ((document.mainform.op_type && document.mainform.op_type.value == "R") || empty(document.mainform.op_type));
		var isFollowUp = !isMain && !isRevisit;
		var visitCenterId = patient.center_id;
		var activeVisit = patientInfo.active_visit_in_another_center;

		var cat = findInList(categoryJSON, "category_id", gPreviousPatientCategoryId);
		var catAllowed = cat == null ? false : (cat.center_id == 0 || cat.center_id == centerId);
		var disableButtons = false;
		var categoryName = empty(patient.category_name) ? patient.patient_category_name : patient.category_name;
		if (!catAllowed) {
			if (isMain || isRevisit) {
				if (allowMultipleActiveVisits == 'Y' && activeVisit != null) {
					disableButtons = true;
					var msg = getString("js.registration.patient.catnotallowed.multiplevisits");
					alert(msg.replace("#", categoryName));
				} else {
					alert(getString("js.registration.patient.catnotallowed.mainvisit")+ " "+categoryName);
					gPreviousPatientCategoryId = 1;
				}
			} else {
				disableButtons = true;
				var msg = getString("js.registration.patient.catnotallowed.followup");
				alert(msg.replace("#", categoryName));
			}
		}
		if (disableButtons) {
			disableOrEnableRegisterBtns(true);
		}
		if (empty(patient.visit_status) || patient.visit_status == 'N') {
			setSelectedIndex(patientCategoryObj, "");
			onChangeCategory(); // reset patient category if pre-registered patient
		}
		if (gPreviousPatientCategoryId == 1 && findInList(categoryJSON, "category_id", gPreviousPatientCategoryId) == null) {
			var msg = getString("js.registration.patient.catnotallowed.inactive");
			alert(msg.replace("#", gen_category.category_name));
		}
		setSelectedIndex(patientCategoryObj, gPreviousPatientCategoryId);
		onChangeCategory(); // this will also load the tpa list
	}

	if (!empty(gPreviousPatientCategoryExpDate) && patientCategoryExpDtObj) {
		patientCategoryExpDtObj.value = formatDate(new Date(gPreviousPatientCategoryExpDate), "ddmmyyyy", "-");
		document.getElementById('cardExpiryDate').value = formatDate(new Date(gPreviousPatientCategoryExpDate), "ddmmyyyy", "-");
	}

	setSelectedIndex(form.ageIn, patient.agein);
	setSelectedIndex(form.patient_gender, patient.patient_gender);

	if (!patientConfidentialityCategoriesJSON.find(category => category.confidentiality_grp_id == patient.patient_group)) {
		form.patient_group.options[form.patient_group.options.length] = new Option(patient.patient_group_name, patient.patient_group);
		form.patient_group.disabled = true;
	}
	setSelectedIndex(form.patient_group, patient.patient_group);

	//set country and national number of patient_phone
	insertNumberIntoDOM(patient.patient_phone,$("#patient_phone"),$("#patient_phone_country_code"),
			$("#patient_phone_national"));
	if (patient.email_id != null) form.email_id.value = patient.email_id;

	form.patient_city.value = patient.patient_city;
	form.patient_state.value = patient.patient_state;
	form.country.value = patient.country;

	form.pat_city_name.value = patient.city_name;
	
	form.blood_group_id.value = patientInfo.patient.blood_group_id;
	form.race_id.value = patientInfo.patient.race_id;
	form.religion_id.value = patientInfo.patient.religion_id;
	form.marital_status_id.value = patientInfo.patient.marital_status_id;
	
	form.preferredLanguage.value = patientInfo.contact_pref_lang_code;
	if(form.modeOfCommSms){
		form.modeOfCommSms.checked = patientInfo.send_sms =='Y'; 
	}
	if(form.modeOfCommEmail){
		form.modeOfCommEmail.checked = patientInfo.send_email =='Y'; 
	}
	document.getElementById("statelbl").textContent = patient.state_name;
	document.getElementById("countrylbl").textContent = patient.country_name;
	if(document.getElementById("districtlbl")) {
		document.getElementById("districtlbl").textContent = patient.district_name;
	}
  /*if ((form.bloodgroup != null) && !empty(patient.bloodgroup) && form.bloodgroup)
		setSelectedIndex(form.bloodgroup, patient.bloodgroup);
    if ((form.religion != null) && !empty(patient.religion) && form.religion)
		setSelectedIndex(form.religion, patient.religion);
    if ((form.occupation != null) && !empty(patient.occupation) && form.occupation)
		setSelectedIndex(form.occupation, patient.occupation);*/

	for (var i=1; i<10; i++) {
		var customListValue = eval("patient.custom_list"+i+"_value");
		var customListLabel = eval("custom_list"+i+"_name");
		var customListObj = eval("form.custom_list"+i+"_value");
		if (!empty(customListValue) && !empty(customListLabel) && customListObj)
			customListObj.value = customListValue;
	}

	for (var i=1; i<20; i++) {
		var customFieldValue = eval("patient.custom_field"+i);
		var customFieldLabel = eval("custom_field"+i+"_label");
		var customFieldObj = eval("form.custom_field"+i);
		if (!empty(customFieldValue) && !empty(customFieldLabel) && customFieldObj) {
			if(i>13 && i<17) {
				customFieldValue = formatDate(new Date(customFieldValue), "ddmmyyyy", "-");
			}
			customFieldObj.value = customFieldValue;
		}
	}

	/*for (var i=1; i<3; i++) {
		var customListValue = eval("patient.visit_custom_list"+i);
		var customListLabel = eval("visit_custom_list"+i+"_name");
		var customListObj = eval("form.visit_custom_list"+i);
		if (!empty(customListValue) && !empty(customListLabel) && customListObj)
			customListObj.value = customListValue;
	}

	for (var i=1; i<10; i++) {
		var customFieldValue = eval("patient.visit_custom_field"+i);
		var customFieldLabel = eval("visit_custom_field"+i+"_name");
		var customFieldObj = eval("form.visit_custom_field"+i);
		if (!empty(customFieldValue) && !empty(customFieldLabel) && customFieldObj) {
			if(i>3 && i<7) {
				customFieldValue = formatDate(new Date(customFieldValue), "ddmmyyyy", "-");
			}
			customFieldObj.value = customFieldValue;
		}
	} */

    if (!empty(patient.identifier_id) && !empty(govtID) && form.identifier_id) {
		setSelectedIndex(form.identifier_id, patient.identifier_id);
		form.identifier_id.onchange();
	}
    if (!empty(patient.government_identifier) && !empty(govtIDType) && form.government_identifier)
		form.government_identifier.value = patient.government_identifier;

	if (!empty(patient.passport_no) && !empty(passportNoLabel) && form.passport_no)
		form.passport_no.value = patient.passport_no;
	if (!empty(patient.passport_issue_country) && !empty(passportIssueCountryLabel) && form.passport_issue_country)
		setSelectedIndex(form.passport_issue_country, patient.passport_issue_country);
	if (!empty(patient.passport_validity) && !empty(passportValidityLabel) && form.passport_validity)
		form.passport_validity.value = formatDate(new Date(patient.passport_validity), "ddmmyyyy", "-");
	if (!empty(patient.visa_validity) && !empty(visaValidityLabel) && form.visa_validity)
		form.visa_validity.value = formatDate(new Date(patient.visa_validity), "ddmmyyyy", "-");

	if (!empty(patient.family_id) && !empty(familyIDLabel) && form.family_id)
		form.family_id.value = patient.family_id;

	if (!empty(patient.nationality_id) && !empty(nationalityLabel) && form.nationality_id)
		form.nationality_id.value = patient.nationality_id;

  if(doctorId == null || doctorId == "") {
    if (patient.dept_id != null && screenid != "out_pat_reg") {
	    setSelectedIndex(form.dept_name, patient.dept_id);
	    DeptChange();
	    if (document.mainform.unit_id != null && patient.unit_id != null)
	 	    setSelectedIndex(form.unit_id, patient.unit_id);
    }
  }
	if((category == 'DOC') && channellingOrders != null) {
		setSelectedIndex(form.dept_name, dept_id);
	}
	if (patient.patient_address != null)
		form.patient_address.value = UnFormatTextAreaValues(patient.patient_address);
	else form.patient_address.value = "";

	if (form.oldmrno != null) {
		if (!empty(patient.oldmrno)) {
			form.oldmrno.value = patient.oldmrno;
			form.oldmrno.readOnly = true;
		} else {
			form.oldmrno.value = patient.oldmrno;
			form.oldmrno.readOnly = false;
		}
	}
	mrno = document.mrnoform.mrno.value;
	if (form.casefileNo != null) {
		if (!empty(patient.casefile_no)) {
			form.casefileNo.value = patient.casefile_no;
			form.casefileNo.readOnly = true;
			form.oldRegAutoGenerate.checked = false;
			form.oldRegAutoGenerate.disabled = true;
			if (document.getElementById("autoGenCaseFileDiv") != null)
					document.getElementById("autoGenCaseFileDiv").style.display = 'none';
			if (document.getElementById("caseFileIssuedDiv") != null)
					document.getElementById("caseFileIssuedDiv").style.display = 'table-cell';
			if (!empty(patient.file_status)) {
				if (!empty(patient.issued_to) && patient.file_status != 'A') {
					document.getElementById("caseFileIssuedBy").innerHTML = patient.issued_to;
				} else {
					document.getElementById("caseFileIssuedBy").innerHTML = "MRD Dept";
				}
				if (patient.indented == 'Y') form.raiseCaseFileIndent.disabled = true;
				else form.raiseCaseFileIndent.disabled = false;
			}

		} else {
			form.casefileNo.value = patient.casefile_no;
			form.casefileNo.readOnly = false;
			form.oldRegAutoGenerate.checked = false;
			form.oldRegAutoGenerate.disabled = false;
			form.raiseCaseFileIndent.disabled = false;
			form.raiseCaseFileIndent.checked = false;
			if (document.getElementById("autoGenCaseFileDiv") != null)
					document.getElementById("autoGenCaseFileDiv").style.display = 'table-cell';
			if (document.getElementById("caseFileIssuedDiv") != null)
					document.getElementById("caseFileIssuedDiv").style.display = 'none';
		}
		showHideCaseFile();
	}
	document.getElementById("remarks").value = patient.remarks;
	document.getElementById('prevVisitTag').style.display = 'inline';

	if (patient.doctor_name != null && patient.doctor_name != '') {
		document.getElementById('prvsDoctor').textContent = patient.doctor_name;
		document.getElementById('prvsDate').innerHTML = '<font style="font-weight: normal"> on </font>' + formatDate(new Date(gPatientRegDate), "ddmmyyyy", "-");
	}else {
		document.getElementById('prvsDoctor').textContent = 'None';
		document.getElementById('prvsDate').textContent = '';
	}

	//set country and national number of kin phone

	var patientCareOftext =  !empty(patient.patcontactperson) ? patient.patcontactperson :
		(!empty(patient.patient_care_oftext) ? patient.patient_care_oftext : '');

	insertNumberIntoDOM(patientCareOftext ,$("#patient_care_oftext"),$("#patient_care_oftext_country_code"),
			$("#patient_care_oftext_national"));

	form.relation.value = !empty(patient.patrelation) ? patient.patrelation : (!empty(patient.relation) ? patient.relation : '');
	form.next_of_kin_relation.value = !empty(patient.next_of_kin_relation) ? patient.next_of_kin_relation : '';
	form.patient_careof_address.value = !empty(patient.pataddress) ? patient.pataddress :
												(!empty(patient.patient_careof_address) ? patient.patient_careof_address : '');

	document.mainform.salutation.focus();

	var mrno = document.mrnoform.mrno.value;
	var ajaxobj2 = newXMLHttpRequest();
	var url = '../../pages/registration/regUtils.do?_method=photoSize&mrno=' + mrno;
	getResponseHandlerText(ajaxobj2, patientPhotoResponseHandler, url.toString());
	document.getElementById('regTyperegd').checked = true;

	// If mod_adv_ins is enabled, initialize the membership id auto complete (if there are more than one membership ids for the patient).
	if (isModAdvanceIns) {
		if (gPreviousPrimarySponsorIndex == 'I')
			policyNoAutoComplete('P', gPatientPolciyNos);
		if (gPreviousSecondarySponsorIndex == 'I')
			policyNoAutoComplete('S', gPatientPolciyNos);
	}

	//corporateNoAutoComplete('P', gPatientCorporateIds);	//11.3 optimisation
	//corporateNoAutoComplete('S', gPatientCorporateIds);	//11.3 optimisation

	//nationalNoAutoComplete('P', gPatientNationalIds);		//11.3 optimisation
	//nationalNoAutoComplete('S', gPatientNationalIds);		//11.3 optimisation

	var hasDoctor = false;

	// Set globally, the selected doctor.
	gSelectedDoctorName = (form.doctor_name != null) ? form.doctor_name.value : null;
	gSelectedDoctorId = (form.doctor != null) ? form.doctor.value : null;
	return hasDoctor;
}

var initSCConflictDialog;

function initSmartCardConflictDialog() {
	document.getElementById('SmartCardConflictDialog').style.display = 'block';
	initSCConflictDialog = new YAHOO.widget.Dialog('SmartCardConflictDialog', {
	   	context:["SmartCardConflictDialog","tr","br"],
	       visible: false,
	       modal: true,
	       constraintoviewport: true,
	   });

	   var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                            { fn:cancelOnConflict,
	                                              scope:initSCConflictDialog,
	                                              correctScope:true } );
	   initSCConflictDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	   initSCConflictDialog.cancelEvent.subscribe(cancelOnConflict);
	   initSCConflictDialog.render();
	}
var patientInfo;

function updateOnConflict() {
	initSCConflictDialog.hide();
	if(mainform.appointmentId.value=="")
		patientDetailsResponseHandlerWrapper(patientInfo);
	cardOverwrite();
	smartConflict = false;
}

function cancelOnConflict(){
	initSCConflictDialog.hide();
	if(mainform.appointmentId.value=="")
		clearRegDetails();
	smartConflict = false;
}

var isFollowUpNoCons;
function patientDetailsResponseHandler(responseText) {

	isFollowUpNoCons = (document.mainform.op_type != null && document.mainform.op_type.value == "D");

	clearPreviousPatientDetails();

	eval("patientInfo =" + responseText);

	if (patientInfo.patient.death_date) {
		var deathDate = new Date(patientInfo.patient.death_date);
		var dateStr = deathDate.getDate() + " " + gMonthLongNames[deathDate.getMonth()].substr(0,3) + " " + deathDate.getFullYear();
		if (patientInfo.patient.death_time) {
			deathDate.setTime(patientInfo.patient.death_time);
			dateStr += " " + (deathDate.getHours() < 10 ? "0" : "") + deathDate.getHours() + ":" + (deathDate.getMinutes() < 10 ? "0" : "") + deathDate.getMinutes();
		}
		alert(deadPatientMessage.replace("{0}", dateStr));
		document.getElementById("mrno").value = "";
		return;
	}
	if(conflictCheckFlag==true) {
		if(null != dt && dt != "")
		  if(patientInfo.patient.patient_name!=trim(dt["fName"]) || patientInfo.patient.middle_name!=trim(dt["mName"]) || patientInfo.patient.last_name!=trim(dt["lName"]) ||
				  patientInfo.patient.government_identifier!=dt["idNumber"] || formatDate(new Date(patientInfo.patient.dateofbirth), "ddmmyyyy", "/")!=dt["dateOfBirth"]) {
				smartConflict = true;
		  }
		  conflictCheckFlag = false;
	}

	if(smartConflict==true) {
		if(dt['photo'] != null && dt['photo'] != "")
			document.getElementById("img_sc").src= "data:image/jpeg;base64," + dt['photo'];
		else
			document.getElementById("img_sc").src= cpath + "/images/patienPlaceholder.png"; //img_sys handled in PhotoResponseHandler()
		document.getElementById("nationalid_sc").textContent= dt["idNumber"];
		document.getElementById("patientName_sc").textContent= dt['fullNameEnglish'];
		document.getElementById("dob_sc").textContent= dt['dateOfBirth'];

		document.getElementById("nationalid_sys").textContent= patientInfo.patient.government_identifier;
		var patientNameSys;
		if(null!=patientInfo.patient.patient_name)
			patientNameSys = patientInfo.patient.patient_name;
		if(null!=patientInfo.patient.middle_name)
			patientNameSys = patientNameSys + " " + patientInfo.patient.middle_name;
		if(null!=patientInfo.patient.last_name)
			patientNameSys = patientNameSys + " " + patientInfo.patient.last_name;

		document.getElementById("patientName_sys").textContent= patientNameSys;
		document.getElementById("dob_sys").textContent= formatDate(new Date(patientInfo.patient.dateofbirth), "ddmmyyyy", "/");
		initSCConflictDialog.show();
	}
	else {
		if(smartCardDetails != null && smartCardDetails !="") {
			if(null != dt['photo'] && dt['photo'] != ""){
				document.getElementById("cardImage").value=dt["photo"];
			}
		}
		patientDetailsResponseHandlerWrapper(patientInfo);
	}
}

function patientDetailsResponseHandlerWrapper(patientInfo) {

	if (isFollowUpNoCons) {
		patient = patientInfo.patient;
		loadPatientResponseDetails(patientInfo, patient);
		if (!empty(gPreviousPrimarySponsorIndex) && allowBillNowInsurance == 'false') {
			if (document.mainform.bill_type != null && document.mainform.bill_type.value != 'C') {
				setSelectedIndex(document.mainform.bill_type, "C");
				onChangeBillType();
			}
		}else
			onChangeBillType();

	}else {
		setPatientDetailsResponse(patientInfo);
	}

	// to show all the Doctor consultation appointments

		if (referal_for_life == 'N')
			clearReferralDoctor();

}



function clearReferralDoctor() {
	document.getElementById('referaldoctorName').value = "";
	document.mainform.referred_by.value = "";
}

function setPatientAppointmentDetailsResponse (patientInfo) {
	patientTodaysAppointmentDetails = patientInfo.patientTodaysAppointmnts;
	showTodaysAppointments(patientTodaysAppointmentDetails);
}

// this method is to show all Doctor consultation appointments in registration screen.

function showTodaysAppointments (patientTodaysAppointmentDetails) {
	var table = document.getElementById('patientDetailsTable');
	var row;
	var cell;

	if (patientTodaysAppointmentDetails.length > 0) {
		document.getElementById('patDetFieldset').setAttribute('style','height:120px;');
		var disabled = false;
		row =  document.createElement("TR");
		cell = document.createElement("TD");
		cell.setAttribute('class' , 'formlable');
		cell.innerHTML = "Today's Doctor Appointments: <select name='appointment_item' id='appointment_item' class='dropdown' style='width:350px;' onchange='loadAppointmentOrder(this)'>";
		row.appendChild(cell);
		table.appendChild(row);
		var option0 = new Option("-- Select --", "");
		var apptItemObj = document.getElementById('appointment_item');
		var j = 1;
		apptItemObj.options[0] = option0;
		for (var i=0;i<patientTodaysAppointmentDetails.length;i++) {
			if (patientTodaysAppointmentDetails[i].primary_resource) {
				var apptId = patientTodaysAppointmentDetails[i].appointment_id;
				var resourceName = patientTodaysAppointmentDetails[i].central_resource_name;
				var apptTime = patientTodaysAppointmentDetails[i].text_appointment_time;
				var doctorDept = patientTodaysAppointmentDetails[i].doctor_department;
				var dispalyStr = resourceName+"("+doctorDept+") "+"at "+apptTime;
				var option = new Option(dispalyStr,apptId);
				apptItemObj.options[j++] = option;

				if (!disabled)
					disabled = apptId == gAppointmentId;
			}
		}

		if (!empty(gAppointmentId)) {
			setSelectedIndex(document.getElementById('appointment_item'),gAppointmentId);
			document.getElementById('appointment_item').disabled = disabled;
		}
	}
}

// loading the scheduled consultation or checked consultation in registration screen to connect the appointment to the patient visit.

function loadAppointmentOrder(obj) {
	if (!empty(obj.value)) {
		var list = filterList(patientTodaysAppointmentDetails,"appointment_id",obj.value);
		category = list[0].category;
		document.mainform.appointmentId.value = list[0].appointment_id;
		document.mainform.category.value = list[0].category;
		appointmentDetailsList = list;
		scheduleName = list[0].res_sch_name;

		if (category == 'DOC') {
			var ajaxobj = newXMLHttpRequest();
			var url = cpath + '/pages/registration/quickregistration/getChannelingOrders.do?_method=getChannelingOrders&appointmentId=' + list[0].appointment_id;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var data =" + ajaxobj.responseText);
						var status = data.status;
						if(status == true) {
							var form = document.mainform;
							if (!empty(form.doctor_name.value)) {
								form.doctor_name.value = "";
								form.doctor.value = "";
								form.doctorCharge.value = "";
								setSelectedIndex(form.dept_name, "");
								DeptChange();
								getDoctorCharge();
							}
							//channeling appointment
							channellingOrders = data.channellingOrdersList;
							clearOrderTable('0');
							channel_orders_added = false;
							dept_id = data.dept_id;
							pat_package_id = data.pat_package_id;
							dept_name = document.getElementById("dept_name");
							setSelectedIndex(dept_name, dept_id);
							//appointmentDetailsList = data.appointmentDetailsList;
							loadSchedulerOrders();
						} else {
							clearOrderTable('0');
							dept_name = document.getElementById("dept_name");
							setSelectedIndex(dept_name, "");
							channel_orders_added = false;
							channellingOrders = null;
							//orderTableInit(true);
							loadConsultation();
						}
					}
				}
			}
		}
	} else {
		clearOrderTable('0');
		dept_name = document.getElementById("dept_name");
		setSelectedIndex(dept_name, "");
		channel_orders_added = false;
		channellingOrders = null;
		//orderTableInit(true);
		clearConsultation();
	}
}

function clearConsultation() {
	var form = document.mainform;
	if (!empty(form.doctor_name.value)) {
		form.doctor_name.value = "";
		form.doctor.value = "";
		form.doctorCharge.value = "";
		setSelectedIndex(form.dept_name, "");
		DeptChange();
		getDoctorCharge();
	}
	document.mainform.appointmentId.value = "";
	document.mainform.category.value = "";
}

function loadConsultation() {
	var form = document.mainform;
	var primaryResource = getSchedulerPrimaryResource();
	if (primaryResource != null) {
		// For Doctor scheduling, load the doctor details
		if (primaryResource.category == 'DOC') {
			setSelectedIndex(form.dept_name, primaryResource.dept_id);
			DeptChange();

			form.doctor_name.value = primaryResource.resourcename;
			form.doctor.value = primaryResource.res_sch_name;

			if (document.mainform.doctorCharge.selectedIndex == 0) {
				// Sets the consultation, op type and gets the doctor charge
				setDocRevistCharge(primaryResource.res_sch_name);
				changeVisitType();
				//first setting the scheduler doctor and then scheduler consultatin type if action is coming form scheduler
				if (!empty(primaryResource.res_sch_name) && !empty(primaryResource.resourcename)) {
					document.mainform.doctor.value = primaryResource.res_sch_name;
					document.mainform.doctor_name.value = primaryResource.resourcename;
				}

				resetDoctorAndReferralOnRevisit();

				if (!empty(primaryResource.consultation_type_id) && primaryResource.consultation_type_id != 0) {
					setSelectedIndex(form.doctorCharge,primaryResource.consultation_type_id);
				}

				setAppointmentDateTimeAsConsultationDateTime(primaryResource);
				getDoctorCharge();

			}
		}
	}

}

function setPatientDetailsResponse(patientInfo) {
	loadPrvPrescripitons = false;
	if (empty(patientInfo) || empty(patientInfo.patient)) {
		showMessage("js.registration.patient.valid.mr.no.check");
		document.mrnoform.mrno.value = '';
		document.mrnoform.mrno.focus();
		return false;
	}

	patient = patientInfo.patient;

	if (!empty(patient.original_mr_no)) {
		showMessage("js.registration.patient.valid.mr.no.check");
		document.mrnoform.mrno.value = '';
		document.mrnoform.mrno.focus();
		return false;
	}

	if ((patient.discharge_type != null) && (patient.discharge_type == 'Expiry')) {
		showMessage("js.registration.patient.expired.mrno.check");
		document.mrnoform.mrno.value = '';
		document.mrnoform.mrno.focus();
		return false;
	}

	if(!empty(patient))
		insured = !empty(patient.primary_sponsor_id);

	if ((patient.visit_status != null) && (patient.visit_status == 'A') && !isDoctorChange && screenid == "ip_registration") {
		if (empty(patientInfo.activeIPVisitId)) {
			var ok = confirm(getString("js.registration.patient.patient.is.already.registered.with.id.string")+" " + patient.patient_id +
						". \n"+getString("js.registration.patient.want.to.create.new.visit.for.the.patient.string")+" ? ");
			if (!ok) {
				clearRegDetails();
				return false;
			}
		}else {
			alert(getString("js.registration.patient.patient.already.registered.with.id")+": " + patientInfo.activeIPVisitId);
			clearRegDetails();
			return false;
		}
	}

	var hasDoctor = loadPatientResponseDetails(patientInfo, patient);

	// Insurance details are also loaded
	// Previous Insurance company, TPA, Plan type, Plan and RatePlan are loaded
	// Also enable the policy validity end date field if empty

	// If Revisit, the doctor & referral is not loaded, since the user selects the doctor (and or) referral as well consultation.
	resetDoctorAndReferralOnRevisit();

	changeVisitType();

	if (!empty(gPreviousPrimarySponsorIndex) && allowBillNowInsurance == 'false') {
		if (document.mainform.bill_type != null && document.mainform.bill_type.value != 'C') {
			setSelectedIndex(document.mainform.bill_type, "C");
			onChangeBillType();
		}
	}

	// After the patient's last visit details are loaded, load schedulerappointment details of the patient if patient has an appointment_id.
	var appointmentId = document.mainform.appointmentId.value;
	if(!empty(appointmentId))
		loadSchedulerPatientInfo();

	// Load the complaint if follow up/revisit visit.
	setPatientComplaint();

	//setting admission request realted info
	var admReqId = document.mainform.adm_request_id.value;
	if(!empty(admReqId))
		loadPatientAdmissionRequestInfo();

	// If Patient Category Rate Plan is not changed then load previous prescriptions.
	if ((empty(gSelectedRatePlan) && empty(gSelectedTPA) && empty(gSelectedPlan))
		|| ((gPatientCategoryRatePlan == gPreviousRatePlan) && (gSelectedRatePlan == gPreviousRatePlan))) {
		loadPreviousUnOrderedPrescriptions();
		if (category != null && (category != 'SNP' || !empty(scheduleName)))
				loadSchedulerOrders();
		//estimateTotalAmount();
	}

	if (!isDoctorChange && gPrevVisitDues != null) {
		var j = 0;
		var msg = null;
		for (var i = 0; i < gPrevVisitDues.length; i++) {
			if (getPaise(gPrevVisitDues[i].DUE_AMOUNT) != 0) {
				if (j == 0 )
					msg = getString("js.registration.patient.patient.has.following.bills.string")+"\n";

				msg += "" + formatAmountValue(gPrevVisitDues[i].DUE_AMOUNT) + " "+getString("js.registration.patient.from.bill.no.text")+" " + gPrevVisitDues[i].BILL_NO + "\n";
				j++;
			}
		}
		if (!empty(msg)) {
			alert(msg);
		}
	}

	if (!isDoctorChange && !empty(regPref.family_id) && gPatientFamilyBillsTotal != null && !empty(gPatientFamilyBillsTotal.total_amount)
					&& getPaise(gPatientFamilyBillsTotal.total_amount) != 0 ) {
		var msg = getString("js.registration.patient.total.family.expenditure.string")+"\n"+
				  getString("js.registration.patient.in.this.finacial.year.is.string")+" " +formatAmountValue(gPatientFamilyBillsTotal.total_amount)+"\n \n "+
				  "("+ regPref.family_id +" : "+patient.family_id+")";
		alert(msg);
	}

	isDoctorChange = false;
	enableDisablePlanDetailsButton(gPreviousPrimarySponsorIndex,gPreviousPrimarySponsorIndex);
	//to load prv prescriptions
	loadPrvPrescripitons = true;
	loadPreviousUnOrderedPrescriptions();
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
	if(!empty(patient.mr_no))
		getPrepopulate_visit_info(patient.mr_no);
}

//set visit fields from prepopulate_visit_info table for syncexternalpatient module, if not enabled,response would be null
function getPrepopulate_visit_info(mrNo) {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + `/patients/opregistration/getprepopulatevisitinfo.json?mr_no=${encodeURIComponent(mrNo)}`;
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				var prePopulateVisitFields = JSON.parse(ajaxobj.responseText);// response is JSON
				if( prePopulateVisitFields != null && prePopulateVisitFields != undefined  ) {
					var jsonVisitInfo = JSON.parse(prePopulateVisitFields.prepopulate_visit_info);
                  	if( jsonVisitInfo != null && jsonVisitInfo != undefined ) {
                  		$("select[name=visit_custom_list1] option[value='"+jsonVisitInfo.visit_custom_list1+"']").attr("selected",true);
						$("select[name=visit_custom_list1_value] option[value='"+jsonVisitInfo.visit_custom_list1+"']").attr("selected",true);
						$("select[name=visit_custom_list2_value] option[value='"+jsonVisitInfo.visit_custom_list2+"']").attr("selected",true);
						$("select[name=visit_custom_list2] option[value='"+jsonVisitInfo.visit_custom_list2+"']").attr("selected",true);
                  		$("input[name=visit_custom_field1]").val(jsonVisitInfo.visit_custom_field1);
						$("input[name=visit_custom_field2]").val(jsonVisitInfo.visit_custom_field2);
						$("input[name=visit_custom_field3]").val(jsonVisitInfo.visit_custom_field3);
                  	}
				}
			}
		}
	}
}
//enabling and disabling palndetails button depending upon primary sponsor or secondary sponsor
function enableDisablePlanDetailsButton(PrimarySponsorIndex,SecondarySponsorIndex) {
	if (!empty(gPreviousPrimarySponsorIndex) && gPreviousPrimarySponsorIndex == "I") {
		if (document.getElementById('pd_primary_planButton') != null) {
			if (!empty(gPreviousPlan)) {
				document.getElementById('pd_primary_planButton').disabled = false;
			} else {
				document.getElementById('pd_primary_planButton').disabled = true;
			}
		}
	}if (!empty(gPreviousSecondarySponsorIndex) && gPreviousSecondarySponsorIndex == "I") {
		if (!empty(gPreviousSecPlan)) {
			if (document.getElementById('pd_secondary_planButton') != null)
				document.getElementById('pd_secondary_planButton').disabled = false;
		}
	}
}

// The complaint is loaded if the visit type is follow up/revisit visit.
function setPatientComplaint() {
	if (document.mainform.ailment) {
		if (document.mainform.op_type != null
			&& (document.mainform.op_type.value == "F" || document.mainform.op_type.value == "D" || document.mainform.op_type.value == "R")) {
			if (!empty(gPatientComplaint)) {
				document.getElementById('ailment').value = gPatientComplaint;
			}
		}else if (!isNewReg() && !empty(gPatientComplaint) && !empty(document.getElementById('ailment').value)
				&& gPatientComplaint == document.getElementById('ailment').value)
			document.getElementById('ailment').value = "";
	}
	if ( loadPrvPrescripitons ){//this is last function on doctor autocomplete item select even.Need to figure out rt place for it
		loadPreviousUnOrderedPrescriptions();
		if (category != null && (category != 'SNP' || !empty(scheduleName)))
			loadSchedulerOrders();
	}
}

function checkUseDRG(spnsrIndex) {
	var drgCheckObj = null;
	var useDRGobj = null;

	if (spnsrIndex == 'P') {
		drgCheckObj = getPrimaryDRGCheckObj();
		useDRGobj = getPrimaryUseDRGObj();
	}else if (spnsrIndex == 'S') {
		drgCheckObj = getSecondaryDRGCheckObj();
		useDRGobj = getSecondaryUseDRGObj();
	}
	if (drgCheckObj != null) {
		var useDRG = drgCheckObj.checked ? 'Y' : 'N';
		useDRGobj.value = useDRG;
	}
}

// If Revisit, the doctor is not loaded, since the user selects the doctor as well consultation.
function resetDoctorAndReferralOnRevisit() {
	var appointmentId = document.mainform.appointmentId.value;
	if (document.mainform.op_type && document.mainform.op_type.value == 'R') {
		if(empty(appointmentId) && (doctorId == null || doctorId == "")) {
			document.mainform.doctor.value = "";
			document.mainform.doctor_name.value = "";
			document.mainform.doctor_name.removeAttribute("title");
			setDoctorChargeBasedOnPractition('', true);
		}
		if (referal_for_life == 'N') {
			document.mainform.referaldoctorName.value = "";
			document.mainform.referred_by.value = "";
		}
	}
}

// Load the scheduler patient details if the patient is already registered.

function loadSchedulerPatientInfo() {
	var form = document.mainform;
	var appointmentId = form.appointmentId.value;
	loadPrvPrescripitons = false;
	if (!empty(appointmentId)) {
		var primaryResource = getSchedulerPrimaryResource();

		if (primaryResource != null) {
			insertNumberIntoDOM(primaryResource.patient_contact,$("#patient_phone"),$("#patient_phone_country_code"),
	    			$("#patient_phone_national"));
			form.ailment.value = primaryResource.complaint;
			/*if (primaryResource.category == 'DOC' && channellingOrders != null) {
				setSelectedIndex(form.dept_name, primaryResource.dept_id);
			}*/
			// For Doctor scheduling, load the doctor details
			if (primaryResource.category == 'DOC' && channellingOrders == null) {
				setSelectedIndex(form.dept_name, primaryResource.dept_id);
				DeptChange();

				form.doctor_name.value = primaryResource.resourcename;
				form.doctor.value = primaryResource.res_sch_name;

				if (document.mainform.doctorCharge.selectedIndex == 0) {
					// Sets the consultation, op type and gets the doctor charge
					setDocRevistCharge(primaryResource.res_sch_name);
					changeVisitType();
					//first setting the scheduler doctor and then scheduler consultatin type if action is coming form scheduler
					if (!empty(primaryResource.res_sch_name) && !empty(primaryResource.resourcename)) {
						document.mainform.doctor.value = primaryResource.res_sch_name;
						document.mainform.doctor_name.value = primaryResource.resourcename;
					}

					resetDoctorAndReferralOnRevisit();

					if (!empty(primaryResource.consultation_type_id) && primaryResource.consultation_type_id != 0)
						setSelectedIndex(form.doctorCharge,primaryResource.consultation_type_id);

					setAppointmentDateTimeAsConsultationDateTime(primaryResource);
					loadPreviousUnOrderedPrescriptions();
					if (category != null && (category != 'SNP' || !empty(scheduleName)))
						loadSchedulerOrders();
				}
			} else {
				form.doctor_name.value = '';
				form.doctor.value = '';
				gSelectedDoctorName = document.mainform.doctor_name.value;
				gSelectedDoctorId = document.mainform.doctor.value;
				setDocRevistCharge(document.mainform.doctor.value);
				if (document.getElementById('docConsultationFees') != null) {
					document.getElementById('docConsultationFees').textContent = '';
					getDoctorCharge();
				}
				if (gSelectedRatePlan == gPatientCategoryRatePlan) {
					if (category != null && (category != 'SNP' || !empty(scheduleName))){
						loadPrvPrescripitons = true;
						loadSchedulerOrders();
					}
					//estimateTotalAmount();
				}
			}
		}
		// If Patient Category not enabled or no default(rateplan or tpa) then load the scheduler orders else the orders are loaded
		// in ratePlanChange() i.e while rateplan/tpa/plan is changed
		if (gSelectedRatePlan == null && gSelectedTPA == null && gSelectedPlan  == null) {
			if (category != null && (category != 'SNP' || !empty(scheduleName))){
				loadPrvPrescripitons = true;
				loadSchedulerOrders();
			}
			//estimateTotalAmount();
		}
	}
}

var policyNoAutoComp = null;
function policyNoAutoComplete(spnsrIndex, gPatientPolciyNos) {

	var policyIdObj = null;
	if (spnsrIndex == 'S')
		policyIdObj = document.getElementById('secondary_member_id');
	else
		policyIdObj = document.getElementById('primary_member_id');

	if (policyNoAutoComp != null)
		policyNoAutoComp.destroy();

	var policyNoArray = [];
	if (!empty(gPatientPolciyNos)) {
		policyNoArray.length = gPatientPolciyNos.length;
		for (i = 0; i < gPatientPolciyNos.length; i++) {
			var item = gPatientPolciyNos[i]
			if(null != item.member_id)
				policyNoArray[i] = item.member_id + "[" + item.insurance_co_name + "]";
		}
	}
	policyIdObj.disabled = false;
	YAHOO.example.ACJSAddArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(policyNoArray);

		if (spnsrIndex == 'S')
			policyNoAutoComp = new YAHOO.widget.AutoComplete('secondary_member_id', 'secondaryMemberIdContainer', datasource);
		else
			policyNoAutoComp = new YAHOO.widget.AutoComplete('primary_member_id', 'primaryMemberIdContainer', datasource);

		policyNoAutoComp.formatResult = Insta.autoHighlight;
		policyNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		policyNoAutoComp.typeAhead = false;
		policyNoAutoComp.useShadow = false;
		policyNoAutoComp.allowBrowserAutocomplete = false;
		policyNoAutoComp.queryMatchContains = true;
		policyNoAutoComp.minQueryLength = 0;
		policyNoAutoComp.maxResultsDisplayed = 20;
		policyNoAutoComp.forceSelection = false;
		policyNoAutoComp.itemSelectEvent.subscribe(loadPolicyDetails);
		// Tab key press from keyboard,
		policyNoAutoComp.autoHighlight = false;
	}
}

var loadPolicyDetails = function (sType, aArgs) {
		var oData = aArgs[2];
		var acPolicyNo = (oData + "").split('[')[0];
		var acCompanyName = (((oData + "").split("[")[1]) + "").split("]")[0];

		var inputElmt = policyNoAutoComp.getInputEl();
		var spnsrIndex = (inputElmt.name.substr(0,1)).toUpperCase();

		var insuCompObj = null;
		var planTypeObj = null;
		var planObj = null;
		var policyValidityStartObj = null;
		var policyValidityEndObj = null;
		var memberIdObj = null;
		var policyNumberObj = null;
		var policyHolderObj = null;
		var policyRelationObj = null;

		if (spnsrIndex == 'P') {
			insuCompObj = getPrimaryInsuObj();
			planTypeObj = getPrimaryPlanTypeObj();
			planObj = getPrimaryPlanObj();
			policyValidityStartObj = getPrimaryPolicyValidityStartObj();
			policyValidityEndObj = getPrimaryPolicyValidityEndObj();

			memberIdObj = getPrimaryInsuranceMemberIdObj();
			policyNumberObj = getPrimaryInsurancePolicyNumberObj();
			policyHolderObj = getPrimaryPatientHolderObj();
			policyRelationObj = getPrimaryPatientRelationObj();

		}else if (spnsrIndex == 'S') {
			insuCompObj = getSecondaryInsuObj();
			planTypeObj = getSecondaryPlanTypeObj();
			planObj = getSecondaryPlanObj();
			policyValidityStartObj = getSecondaryPolicyValidityStartObj();
			policyValidityEndObj = getSecondaryPolicyValidityEndObj();

			memberIdObj = getSecondaryInsuranceMemberIdObj();
			policyNumberObj = getSecondaryInsurancePolicyNumberObj();
			policyHolderObj = getSecondaryPatientHolderObj();
			policyRelationObj = getSecondaryPatientRelationObj();

		}

		for (var i = 0; i < gPatientPolciyNos.length; i++) {
			var item = gPatientPolciyNos[i];
			if (acPolicyNo == item.member_id && getCompanyIdForCompanyName(acCompanyName) == item.insurance_co_id) {
				policyValidityStartObj.disabled = false;
				policyValidityEndObj.disabled = false;
				policyHolderObj.disabled = false;
				policyNumberObj.disabled = false;
				setSelectedIndex(insuCompObj, item.insurance_co_id);
				loadTpaList(spnsrIndex);
				tpaChange(spnsrIndex);
				setSelectedIndex(planTypeObj, item.plan_type_id);
				insuCatChange(spnsrIndex);
				setSelectedIndex(planObj, item.plan_id);
				policyChange(spnsrIndex);
				RatePlanList();
				ratePlanChange();
				memberIdObj.value = acPolicyNo;
				policyNumberObj.value = item.policy_number;
				policyHolderObj.value = item.policy_holder_name;
				policyRelationObj.value = item.patient_relationship;
				// #45950
				// Ins30 : Corporate insurance validations removed
				//if(corpInsuranceCheck == "Y"){
					//setSelectedDateForCorpInsurance();
				//} else 	{
					if(null != item.policy_validity_start && "" != item.policy_validity_start)
						policyValidityStartObj.value = formatDate(new Date(item.policy_validity_start), "ddmmyyyy", "-");
					if(null != item.policy_validity_end && "" != item.policy_validity_end)
						policyValidityEndObj.value = formatDate(new Date(item.policy_validity_end), "ddmmyyyy", "-");
				//}
				break;
			}
		}
	}

function getCompanyIdForCompanyName(companyName) {
	if (empty(companyName)) {
		showMessage("js.registration.patient.member.id.valid.check.against.insurance.company");
		return null;
	}

	var companyId = null;
	if (!empty(gPatientPolciyNos)) {
		for (var i = 0; i < gPatientPolciyNos.length; i++) {
			if (gPatientPolciyNos[i].insurance_co_name == companyName) {
				companyId = gPatientPolciyNos[i].insurance_co_id;
				break;
			}
		}
	}
	return companyId;
}

var primaryCorporateNoAutoComp = null;
var secondaryCorporateNoAutoComp = null;
function corporateNoAutoComplete(spnsrIndex, gPatientCorporateIds) {

	var corporateNoArray = [];
	if (!empty(gPatientCorporateIds)) {
		corporateNoArray.length = gPatientCorporateIds.length;
		for (i = 0; i < gPatientCorporateIds.length; i++) {
			var item = gPatientCorporateIds[i]
			corporateNoArray[i] = item.employee_id + "[" + item.tpa_name + "]";
		}
	}

	YAHOO.example.ACJSAddArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(corporateNoArray);

		if (spnsrIndex == 'S') {

			if (secondaryCorporateNoAutoComp != null)
				secondaryCorporateNoAutoComp.destroy();

			secondaryCorporateNoAutoComp = new YAHOO.widget.AutoComplete('secondary_employee_id', 'secondaryCorporateIdContainer', datasource);
			secondaryCorporateNoAutoComp.formatResult = Insta.autoHighlight;
			secondaryCorporateNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			secondaryCorporateNoAutoComp.typeAhead = false;
			secondaryCorporateNoAutoComp.useShadow = false;
			secondaryCorporateNoAutoComp.allowBrowserAutocomplete = false;
			secondaryCorporateNoAutoComp.queryMatchContains = true;
			secondaryCorporateNoAutoComp.minQueryLength = 0;
			secondaryCorporateNoAutoComp.maxResultsDisplayed = 20;
			secondaryCorporateNoAutoComp.forceSelection = false;
			secondaryCorporateNoAutoComp.itemSelectEvent.subscribe(loadPatientCorporateDetails);
		}else {

			if (primaryCorporateNoAutoComp != null)
				primaryCorporateNoAutoComp.destroy();

			primaryCorporateNoAutoComp = new YAHOO.widget.AutoComplete('primary_employee_id', 'primaryCorporateIdContainer', datasource);
			primaryCorporateNoAutoComp.formatResult = Insta.autoHighlight;
			primaryCorporateNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			primaryCorporateNoAutoComp.typeAhead = false;
			primaryCorporateNoAutoComp.useShadow = false;
			primaryCorporateNoAutoComp.allowBrowserAutocomplete = false;
			primaryCorporateNoAutoComp.queryMatchContains = true;
			primaryCorporateNoAutoComp.minQueryLength = 0;
			primaryCorporateNoAutoComp.maxResultsDisplayed = 20;
			primaryCorporateNoAutoComp.forceSelection = false;
			primaryCorporateNoAutoComp.itemSelectEvent.subscribe(loadPatientCorporateDetails);
		}
	}
}

var loadPatientCorporateDetails = function (sType, aArgs) {
		var oData = aArgs[2];
		var acCorporateNo = (oData + "").split('[')[0];
		var acSponsorName = (((oData + "").split("[")[1]) + "").split("]")[0];

		var inputElmt = this.getInputEl()
		var spnsrIndex = (inputElmt.name.substr(0,1)).toUpperCase();

		var tpaObj = null;
		var employeeIdObj = null;
		var empNameObj = null;
		var empRelationObj = null;

		if (spnsrIndex == 'P') {
			tpaObj = getPrimarySponsorObj();
			employeeIdObj = getPrimaryMemberIdObj();
			empNameObj = getPrimaryPatientHolderObj();
			empRelationObj = getPrimaryPatientRelationObj();

		}else if (spnsrIndex == 'S') {
			tpaObj = getSecondarySponsorObj();
			employeeIdObj = getSecondaryMemberIdObj();
			empNameObj = getSecondaryPatientHolderObj();
			empRelationObj = getSecondaryPatientRelationObj();
		}

		if (!empty(gPatientCorporateIds)) {
			for (var i = 0; i < gPatientCorporateIds.length; i++) {
				var item = gPatientCorporateIds[i];
				if (acCorporateNo == item.employee_id && getSponsorIdForCorporateSponsorName(acSponsorName) == item.sponsor_id) {

					setSelectedIndex(tpaObj, item.sponsor_id);
					loadTpaList(spnsrIndex);
					onCorporateChange(spnsrIndex);

					RatePlanList();
					ratePlanChange();
					employeeIdObj.value = acCorporateNo;
					empNameObj.value = item.employee_name;
					empRelationObj.value = item.patient_relationship;
					break;
				}
			}
		}
	}

function getSponsorIdForCorporateSponsorName(sponsorName) {
	if (empty(sponsorName)) {
		showMessage("js.registration.patient.employee.id.valid.check.against.sponsor");
		return null;
	}

	var sponsorId = null;
	if (!empty(gPatientCorporateIds)) {
		for (var i = 0; i < gPatientCorporateIds.length; i++) {
			if (gPatientCorporateIds[i].tpa_name == sponsorName) {
				sponsorId = gPatientCorporateIds[i].tpa_id;
				break;
			}
		}
	}
	return sponsorId;
}

var primaryNationalNoAutoComp = null;
var secondaryNationalNoAutoComp = null;
function nationalNoAutoComplete(spnsrIndex, gPatientNationalIds) {

	var nationalNoArray = [];
	if (!empty(gPatientNationalIds)) {
		nationalNoArray.length = gPatientNationalIds.length;
		for (i = 0; i < gPatientNationalIds.length; i++) {
			var item = gPatientNationalIds[i]
			nationalNoArray[i] = item.national_id + "[" + item.tpa_name + "]";
		}
	}

	YAHOO.example.ACJSAddArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(nationalNoArray);

		if (spnsrIndex == 'S') {

			if (secondaryNationalNoAutoComp != null)
				secondaryNationalNoAutoComp.destroy();

			secondaryNationalNoAutoComp = new YAHOO.widget.AutoComplete('secondary_national_member_id', 'secondaryNationalIdContainer', datasource);
			secondaryNationalNoAutoComp.formatResult = Insta.autoHighlight;
			secondaryNationalNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			secondaryNationalNoAutoComp.typeAhead = false;
			secondaryNationalNoAutoComp.useShadow = false;
			secondaryNationalNoAutoComp.allowBrowserAutocomplete = false;
			secondaryNationalNoAutoComp.queryMatchContains = true;
			secondaryNationalNoAutoComp.minQueryLength = 0;
			secondaryNationalNoAutoComp.maxResultsDisplayed = 20;
			secondaryNationalNoAutoComp.forceSelection = false;
			secondaryNationalNoAutoComp.itemSelectEvent.subscribe(loadPatientNationalDetails);
		}else {

			if (primaryNationalNoAutoComp != null)
				primaryNationalNoAutoComp.destroy();

			primaryNationalNoAutoComp = new YAHOO.widget.AutoComplete('primary_national_member_id', 'primaryNationalIdContainer', datasource);
			primaryNationalNoAutoComp.formatResult = Insta.autoHighlight;
			primaryNationalNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			primaryNationalNoAutoComp.typeAhead = false;
			primaryNationalNoAutoComp.useShadow = false;
			primaryNationalNoAutoComp.allowBrowserAutocomplete = false;
			primaryNationalNoAutoComp.queryMatchContains = true;
			primaryNationalNoAutoComp.minQueryLength = 0;
			primaryNationalNoAutoComp.maxResultsDisplayed = 20;
			primaryNationalNoAutoComp.forceSelection = false;
			primaryNationalNoAutoComp.itemSelectEvent.subscribe(loadPatientNationalDetails);
		}
	}
}

var loadPatientNationalDetails = function (sType, aArgs) {
		var oData = aArgs[2];
		var acNationalNo = (oData + "").split('[')[0];
		var acSponsorName = (((oData + "").split("[")[1]) + "").split("]")[0];

		var inputElmt = this.getInputEl()
		var spnsrIndex = (inputElmt.name.substr(0,1)).toUpperCase();

		var tpaObj = null;
		var nationalIdObj = null;
		var citizenNameObj = null;
		var patRelationObj = null;

		if (spnsrIndex == 'P') {
			tpaObj = getPrimarySponsorObj();
			nationalIdObj = getPrimaryMemberIdObj();
			citizenNameObj = getPrimaryPatientHolderObj();
			patRelationObj = getPrimaryPatientRelationObj();

		}else if (spnsrIndex == 'S') {
			tpaObj = getSecondarySponsorObj();
			nationalIdObj = getSecondaryMemberIdObj();
			citizenNameObj = getSecondaryPatientHolderObj();
			patRelationObj = getSecondaryPatientRelationObj();
		}

		if (!empty(gPatientNationalIds)) {
			for (var i = 0; i < gPatientNationalIds.length; i++) {
				var item = gPatientNationalIds[i];
				if (acNationalNo == item.national_id && getSponsorIdForNationalSponsorName(acSponsorName) == item.sponsor_id) {

					setSelectedIndex(tpaObj, item.sponsor_id);
					loadTpaList(spnsrIndex);
					//onNationalSponsorChange(spnsrIndex);

					RatePlanList();
					ratePlanChange();
					nationalIdObj.value = acNationalNo;
					citizenNameObj.value = item.citizen_name;
					patRelationObj.value = item.patient_relationship;
					break;
				}
			}
		}
	}

function getSponsorIdForNationalSponsorName(sponsorName) {
	if (empty(sponsorName)) {
		showMessage("js.registration.patient.national.id.valid.check.against.sponsor");
		return null;
	}

	var sponsorId = null;
	if (!empty(gPatientNationalIds)) {
		for (var i = 0; i < gPatientNationalIds.length; i++) {
			if (gPatientNationalIds[i].tpa_name == sponsorName) {
				sponsorId = gPatientNationalIds[i].tpa_id;
				break;
			}
		}
	}
	return sponsorId;
}


function patientPhotoResponseHandler(responseText) {
	eval("var patientPhoto =" + responseText);
	if (patientPhoto == 0) {
		document.getElementById('viewPhoto').style.display = 'none';
	} else {
		document.getElementById('viewPhoto').style.display = 'block';
	}
}

function setDateOfBirthFields(prefix, dateObj) {
	if (dateObj != null) {
		enableDobAndHijriDob();
		document.getElementById(prefix + "Year").value = dateObj.getFullYear();
		document.getElementById(prefix + "Day").value = dateObj.getDate();
		document.getElementById(prefix + "Month").value = dateObj.getMonth() + 1;
		dissableAge();
	} else {
		document.getElementById(prefix + "Day").value = getString("js.registration.patient.show.dd.text");
		document.getElementById(prefix + "Month").value = getString("js.registration.patient.show.mm.text");
		document.getElementById(prefix + "Year").value = getString("js.registration.patient.show.yy.text");
		dissableDobAndHijriDob();
		enableAge();
	}
}
 function gregorianToHijri() {

	        var day =  document.getElementById("dobDay").value;
		    var month = document.getElementById("dobMonth").value;
		    var year = document.getElementById("dobYear").value;

		if (day != '' && day != 'DD' && month != '' && month != 'MM' && year != '' && year != 'YY') {
			var ajaxobj = newXMLHttpRequest();

		    if (!isInteger(month)) {
		        //showMessage("js.registration.patient.invalid.month.not.an.integer.string");
			    //setTimeout("document.mainform.dobMonth.focus()",0);
		        return null;
		    }
		    if (!isInteger(day)) {
		        //showMessage("js.registration.patient.invalid.month.not.an.integer.string");
			    //setTimeout("document.mainform.dobDay.focus()",0);
		        return null;
		    }
		    if(!isInteger(year)) {
		    	return null;
		    }
			if(year.length < 4) {
		        var stryear = convertTwoDigitYear(parseInt(year,10));
		        if (stryear < 1900) {
		            alert(getString("Invalid year:")+" " + stryear +
		                ". "+getString("js.registration.patient.must.be.two.digit.or.four.digit.year.string"));
			        setTimeout("document.mainform.dobYear.focus()",0);
		            return null;
		        }
		        // silently set the 4-digit year back to the textbox, and get the new value
		        document.mainform.dobYear.value = stryear;
		        year = stryear;
			}
			//console.log("Entered 2 "+year+"::"+month+"::"+day);
			var expDate = new Date(1937,3 ,14);
			var futureDate=new Date(2077,11,16);
			var entDate = new Date(year,month,day);
			//console.log(expDate);
			if((expDate.getTime()>entDate.getTime())||(futureDate.getTime()<entDate.getTime())) {
				document.getElementById("dobHDay").value = "";
				document.getElementById("dobHMonth").value = "";
				document.getElementById("dobHYear").value = "";
			} else {
				var url = cpath + '/pages/registration/regUtils.do?_method=getGregorianToHijri&dobDay=' + day
				+ '&dobMonth=' + month + '&dobYear=' + year;
				ajaxobj.open("POST", url.toString(), false);
				ajaxobj.send(null);
				if (ajaxobj) {
					if (ajaxobj.readyState == 4) {
						if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
							eval("var Date =" + ajaxobj.responseText);
							//alert(ajaxobj.responseText);
							document.getElementById("dobHDay").value = Date.day;
							document.getElementById("dobHMonth").value = Date.month;
							document.getElementById("dobHYear").value = Date.year;
						}
					}
				}
			}
		}
	}

function enableDisableDateOfBirthFields(prefix, disable) {
	document.getElementById(prefix + "Year").disabled = disable;
	document.getElementById(prefix + "Day").disabled = disable;
	document.getElementById(prefix + "Month").disabled = disable;
}

// Set the last visit to be closed.

function setLastVisitToClose() {
	var closeVisitObj = document.mrnoform.close_last_active_visit;
	var lastVisitObj = document.mainform.last_active_visit;
	if (closeVisitObj.checked) {
		lastVisitObj.value = gLastVisitIdInThisCenter;
	} else {
		lastVisitObj.value = '';
	}
}

/**
  * disable (possible values are true/false)
  		= parameter which indicates whether insurance fields are to be disabled (true) or enabled (false).
  * forceEnable (possible values are true/false)
  		= parameter which ignores disable (above parameter) and forcefully enables or disables the insurance fields.
 */

function disableOrEnableInsuranceFields(disable, forceEnable) {

/** If mod_adv_ins is not enabled, and bill type selected is Prepaid then the insurance fields are disabled.
	   Also if mod_adv_ins is enabled and Visit type selected is Follow up the the insurance fields are disabled. */

	var allowInsurance = false;

	if (document.mainform.bill_type
			&& (document.mainform.bill_type.value == "C" || (allowBillNowInsurance == 'true' && document.mainform.bill_type.value == 'P'))) {
		allowInsurance = true;
	}

	var isMain = ((document.mainform.op_type && document.mainform.op_type.value == "M") || empty(document.mainform.op_type));
	var isRevisit = ((document.mainform.op_type && document.mainform.op_type.value == "R") || empty(document.mainform.op_type));

	var isFollowUpVisit = (!isMain && !isRevisit);

	if (typeof (forceEnable) == 'undefined') {

		if (allowInsurance) {
			if (isFollowUpVisit) disable = true;
			else disable = disable;
		}else
			disable = true;

	} else disable = forceEnable;

	if (document.mainform.ailment)
		document.mainform.ailment.disabled = (typeof (forceEnable) == 'undefined') ? (!isMain && !isRevisit) : forceEnable;

	var disableCategory = forceEnable;
	if (typeof (disableCategory) == 'undefined') {
		if (document.getElementById('regTypenew').checked) disableCategory = false;
		else if ((roleId != 1 && roleId != 2 && catChangeRights != 'A') || (!isMain && !isRevisit)) disableCategory = true;
	}
	if (document.mainform.patient_category_id)
		document.mainform.patient_category_id.disabled = disableCategory;

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var primarySponsorWrapperObj = document.getElementById("primary_sponsor_wrapper");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");

	var primarySpnsrType = primarySponsorObj.value;
	var secondarySpnsrType = secondarySponsorObj.value;

	primarySponsorObj.disabled = disable;
	primarySponsorWrapperObj.disabled = disable;

	if (primarySpnsrType == '' || corpInsuranceCheck =='Y') {
		secondarySponsorWrapperObj.disabled = true;
		if (secondarySponsorObj != null) secondarySponsorObj.value = '';
	}else
		secondarySponsorWrapperObj.disabled = disable;

	// var approvalAmtEnableDisable = !isMain && !isRevisit;

	// For bug # 29283 Follow-Up visits, per visit co-pay is not applicable.
	// Hence, enabling approval amount field for follow up also
	var approvalAmtEnableDisable = false;

	if (primarySponsorObj != null) {
		if (primarySpnsrType == 'I') {
			enableDisablePrimaryInsuranceDetails(disable, approvalAmtEnableDisable);

		}else if (primarySpnsrType == 'C') {
			enableDisablePrimaryCorporateDetails(disable, approvalAmtEnableDisable);

		}else if (primarySpnsrType == 'N') {
			enableDisablePrimaryNationalDetails(disable, approvalAmtEnableDisable);
		}else {}
	}
	if (secondarySponsorObj != null) {
		if (secondarySpnsrType == 'I') {
			enableDisableSecondaryInsuranceDetails(disable, approvalAmtEnableDisable);

		}else if (secondarySpnsrType == 'C') {
			enableDisableSecondaryCorporateDetails(disable, approvalAmtEnableDisable);

		}else if (secondarySpnsrType == 'N') {
			enableDisableSecondaryNationalDetails(disable, approvalAmtEnableDisable);
		}else {}
	}

	if(isMain || isRevisit)
		enableDisableValidityPeriods();

}

function enableDisableValidityPeriods(){
	var primarySponsorObj = document.getElementById("primary_sponsor");
	var primaryTpaObj = document.getElementById("primary_sponsor_id");
	var priPolicyStartDtObj= getPrimaryPolicyValidityStartObj();
	var priPolicyEndDtObj = getPrimaryPolicyValidityEndObj();

	enableDisableValidityPeriod(primarySponsorObj,primaryTpaObj,priPolicyStartDtObj,priPolicyEndDtObj);

	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var secondaryTpaObj = document.getElementById("secondary_sponsor_id");
	var secPolicyStartDtObj = getSecondaryPolicyValidityStartObj();
	var secPolicyEndDtObj = getSecondaryPolicyValidityEndObj();

	enableDisableValidityPeriod(secondarySponsorObj,secondaryTpaObj,secPolicyStartDtObj,secPolicyEndDtObj);
}

function enableDisableValidityPeriod(sponsorObj, tpaObj, policyStartDtObj, policyEndDtObj){
	if (sponsorObj != null && tpaObj != null && tpaObj.value != '') {
		//var tpaId = tpaObj.value;
    //var item = findInList(tpanames, "tpa_id", tpaId);
     var item;
     if(sponsorObj.id === "primary_sponsor_id"){
        item = primarySponsorDetails;
     } else {
        item = secondarySponsorDetails;
     }
		for(var i=0;i<sponsorTypeList.length;i++){
			if(sponsorTypeList[i].sponsor_type_id == item.sponsor_type_id){
				if(sponsorTypeList[i].validity_period_editable != 'Y'){
					if(policyStartDtObj!=null)
						policyStartDtObj.readOnly = true;
					if(policyEndDtObj!=null)
						policyEndDtObj.readOnly = true;
				}
			}
		}
	}
}

// Populate the main visit or the previous insurance & policy details
// if op_type is F/D i.e FollowUp/FollowUp with consultation.
// Populate the category related TPA and Rate plan if op_type is M/R i.e Main or Revisit.

function loadInsuranceDetails(spnsrIndex) {

	var mainSpnsrIndex = spnsrIndex;

	var loadPreviousInsDetails	= false;
	var allowInsurance			= false;

	// If bill type is bill now and allow bill now insurance is true or bill type is bill later or for out side patient then load
	// the previous patient insurance details.
	if (document.mainform.bill_type
			&& (document.mainform.bill_type.value == "C" || (allowBillNowInsurance == 'true' && document.mainform.bill_type.value == 'P'))) {
		allowInsurance = true;
	}

	if (allowInsurance || screenid == "out_pat_reg") {
		loadPreviousInsDetails = true;
	}

	var insCompObj	= null;
	var tpaObj		= null;
	var tpaObjName	= null;
	var planObj		= null;
	var planTypeObj	= null;
	var memberIdObj	= null;
	var policyStartDtObj= null;
	var policyEndDtObj	= null;
	var policyNumberObj = null;
	var policyHolderObj	= null;
	var patientRelationshipObj	= null;
	var insuranceApprovalObj	= null;

	var priorAuthIdObj = null;
	var priorAuthModeIdObj = null;

	var previousInsCompany = null;
	var previousTpa = null;
	var previousPlan = null;
	var previousPlanType = null;
	var previousMemberId = null;
	var previousPatientPolicyId = null;
	var previousStartDate = null;
	var previousEndDate = null;
	var previousPolicyNumber = null;
	var previousHolder = null;
	var previousRelation = null;
	var previousPriorAuthid = null;
	var previousPriorAuthmodeid = null;

	if (spnsrIndex == 'P') {
		insCompObj	= getPrimaryInsuObj();
		tpaObj		= getPrimarySponsorObj();
		tpaObjName  = getPrimarySponsorNameObj();
		planObj		= getPrimaryPlanObj();
		planTypeObj	= getPrimaryPlanTypeObj();
		memberIdObj	= getPrimaryInsuranceMemberIdObj();
		policyStartDtObj= getPrimaryPolicyValidityStartObj();
		policyEndDtObj	= getPrimaryPolicyValidityEndObj();
		policyNumberObj = getPrimaryInsurancePolicyNumberObj();
		policyHolderObj	= getPrimaryPatientHolderObj();
		patientRelationshipObj	= getPrimaryPatientRelationObj();
		insuranceApprovalObj	= getPrimaryApprovalLimitObj();

		priorAuthIdObj = getPrimaryAuthIdObj();
		priorAuthModeIdObj = getPrimaryAuthModeIdObj();

		previousInsCompany = gPreviousPrimaryInsCompany;
		previousTpa = gPreviousPrimaryTpa;
		previousPlan = gPreviousPlan;
		previousPatientPolicyId = gPreviousPatientPolicyId;

		var member = findInList(gPatientPolciyNos, "patient_policy_id", previousPatientPolicyId);
		if (member != null && member.plan_id == previousPlan && member.insurance_co_id == previousInsCompany) {
			previousPlanType = gPreviousPlanType;
			previousMemberId = gPreviousMemberId;
			previousStartDate = gPreviousStartDate;
			previousEndDate = gPreviousEndDate;

			previousPolicyNumber = gPreviousPolicyNumber;
			previousHolder = gPreviousHolder;
			previousRelation = gPreviousRelation;
			previousPriorAuthid = gPreviousPriorauthid;
			previousPriorAuthmodeid = gPreviousPriorauthmodeid;

		}else {
			previousPlan = null;
			previousPlanType = null;
			previousMemberId = null;
			previousStartDate = null;
			previousEndDate = null;
			previousPolicyNumber = null;
			previousHolder = null;
			previousRelation = null;
			previousPriorAuthid = null;
			previousPriorAuthmodeid = null;
		}

	}else if (spnsrIndex == 'S') {
		insCompObj	= getSecondaryInsuObj();
		tpaObj		= getSecondarySponsorObj();
		tpaObjName  = getSecondarySponsorNameObj();
		planObj		= getSecondaryPlanObj();
		planTypeObj	= getSecondaryPlanTypeObj();
		memberIdObj	= getSecondaryInsuranceMemberIdObj();
		policyStartDtObj= getSecondaryPolicyValidityStartObj();
		policyEndDtObj	= getSecondaryPolicyValidityEndObj();
		policyNumberObj = getSecondaryInsurancePolicyNumberObj();
		policyHolderObj	= getSecondaryPatientHolderObj();
		patientRelationshipObj	= getSecondaryPatientRelationObj();
		insuranceApprovalObj	= getSecondaryApprovalLimitObj();

		priorAuthIdObj = getSecondaryAuthIdObj();
		priorAuthModeIdObj = getSecondaryAuthModeIdObj();

		previousInsCompany = gPreviousSecondaryInsCompany;
		previousTpa = gPreviousSecondaryTpa;
		previousPlan = gPreviousSecPlan;
		previousPatientPolicyId = gPreviousSecPatientPolicyId;

		var member = findInList2(gPatientPolciyNos, "patient_policy_id", previousPatientPolicyId,'priority',2);
		if (mainSpnsrIndex == 'S' && member != null && member.plan_id == previousPlan && member.insurance_co_id == previousInsCompany) {
			previousPlanType = gPreviousSecPlanType;
			previousMemberId = gPreviousSecMemberId;
			previousPolicyNumber = gPreviousSecPolicyNumber;
			previousStartDate = gPreviousSecStartDate;
			previousEndDate = gPreviousSecEndDate;
			previousHolder = gPreviousSecHolder;
			previousRelation = gPreviousSecRelation;
			previousPriorAuthid = gPreviousSecPriorauthid;
			previousPriorAuthmodeid = gPreviousSecPriorauthmodeid;
		}else {
			previousPlan = null;
			previousPlanType = null;
			previousMemberId = null;
			previousPolicyNumber = null;
			previousStartDate = null;
			previousEndDate = null;
			previousHolder = null;
			previousRelation = null;
			previousPriorAuthid = null;
			previousPriorAuthmodeid = null;
		}
	}

	var organizationObj	  = document.mainform.organization;
	var patientCategoryObj	= document.mainform.patient_category_id;

	var patientCategoryId = '';
	var insCompId = '';
	var tpaId = '';
	var planId = '';
	var planTypeId = '';
	var ratePlanId = '';

	var memberId = '';
	var policyNumber = '';
	var policyStart = '';
	var policyEnd = '';
	var holder = '';
	var relation = '';

	var validPatientCategoryId = '';
	var validPlanType = '';
	var validInsComp = '';
	var validTpa = '';
	var validMemberId = '';
	var validRatePlan = '';

	var patcategory = findInList(categoryJSON, "category_id", gPreviousPatientCategoryId);

	if (patcategory != null && patcategory.status == 'A') {
		patientCategoryId = gPreviousPatientCategoryId;
		validPatientCategoryId = patcategory.category_id;
	}

	if (validPatientCategoryId == gPreviousPatientCategoryId) {
		patientCategoryId = gPreviousPatientCategoryId;
	}

	if (empty(previousPlan)) {
		ratePlanId = gPreviousRatePlan;
		tpaId = previousTpa;
		insCompId = previousInsCompany;
	}

	var plan = findInList(policynames, "plan_id", previousPlan);

	if (plan != null && plan.status == 'A') {
		planId = previousPlan;
		validPlanType = plan.category_id;
		validRatePlan = plan.default_rate_plan;
	}

	if (validPlanType == previousPlanType) {
		var plantype = findInList(insuCatNames, "category_id", previousPlanType);

		if (plantype != null && plantype.status == 'A') {
			planTypeId = previousPlanType;
			validInsComp = plantype.insurance_co_id;
		}
	}

	if (validRatePlan == gPreviousRatePlan) {
		var ratePlan = findInList(orgNamesJSON, "org_id", gPreviousRatePlan);

		if (ratePlan != null && ratePlan.status == 'A') {
			ratePlanId = gPreviousRatePlan;
		}
	}
	var tpanameval='';
	if (validInsComp == previousInsCompany && validPatientCategoryId == gPreviousPatientCategoryId) {
		var inscomp = findInList(insuCompanyDetails, "insurance_co_id", previousInsCompany);

		if (inscomp != null && inscomp.status == 'A') {
			insCompId = previousInsCompany;

			var tpaList = filterList(companyTpaList, 'insurance_co_id', previousInsCompany);

			if (empty(tpaList)) {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				if (tpa != null && tpa.status == 'A') {
					tpaId = previousTpa;
					validTpa = tpa.tpa_id;
					tpanameval= tpa.tpa_name;
				}
			} else {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				if (tpa != null && tpa.status == 'A') {
					tpaId = previousTpa;
					validTpa = tpa.tpa_id;
					tpanameval= tpa.tpa_name;
				}
			}

			// Rate plan related to insurance company
			if (empty(ratePlanId) && !empty(inscomp.default_rate_plan)) {
				validRatePlan = inscomp.default_rate_plan;
			}

			if (validRatePlan == gPreviousRatePlan) {
				var ratePlan = findInList(orgNamesJSON, "org_id", gPreviousRatePlan);

				if (ratePlan != null && ratePlan.status == 'A') {
					ratePlanId = gPreviousRatePlan;
				}
			}
		}
	}

	// If mod_adv_ins (or) mod_insurance is enabled, set the insurance company & tpa.
	if (loadPreviousInsDetails) {

		if (isModAdvanceIns) {

			if (patientCategoryObj != null) {
				setSelectedIndex(patientCategoryObj, patientCategoryId);
			}

			if (insCompObj != null) {
				loadInsuranceCompList(spnsrIndex);
				setSelectedIndex(insCompObj, insCompId);
				//loadTpaList(spnsrIndex);
			}

			if (tpaObj != null) {
				tpaObj.value= tpaId;
				tpaObjName.value=tpanameval;
				tpaChange(spnsrIndex);
				enableRegistrationOtherInsuranceDetailsTab(spnsrIndex);
				forEditInsAndfollowUps(spnsrIndex);
			}
		} else if (isModInsurance) {

			if (patientCategoryObj != null) {
				setSelectedIndex(patientCategoryObj, gPreviousPatientCategoryId);
			}

			if (insCompObj != null) {
				loadInsuranceCompList(spnsrIndex);
				setSelectedIndex(insCompObj, previousInsCompany);
				//loadTpaList(spnsrIndex);
			}

			if (tpaObj != null) {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				tpaObj.value= previousTpa;
				tpaObjName.value=tpa.tpa_name;
				tpaChange(spnsrIndex);
				enableRegistrationOtherInsuranceDetailsTab(spnsrIndex);
				forEditInsAndfollowUps(spnsrIndex);
			}
		} else {
			if (patientCategoryObj != null)
				setSelectedIndex(patientCategoryObj, gPreviousPatientCategoryId);
			setSelectedIndex(insCompObj, previousInsCompany);
			//loadTpaList(spnsrIndex);		//11.3 optimisation
			var tpa = findInList(tpanames, "tpa_id", previousTpa);
			tpaObj.value= previousTpa;
			tpaObjName.value=tpa.tpa_name;
			tpaChange(spnsrIndex);
			enableRegistrationOtherInsuranceDetailsTab(spnsrIndex);
			forEditInsAndfollowUps(spnsrIndex);
		}
	}else {
		if (patientCategoryObj != null)
			setSelectedIndex(patientCategoryObj, "");
		setSelectedIndex(insCompObj, "");
		setSelectedIndex(tpaObj, "");
	}

	// Set the plan type, plan and the validity dates if the membership id validity has not expired.
	//if (isModAdvanceIns) {

		if (loadPreviousInsDetails) {
			loadTpaList(spnsrIndex);
			setSelectedIndex(planTypeObj, planTypeId);
			insuCatChange(spnsrIndex);
			setSelectedIndex(planObj, planId);
			enableDisablePlanDetails(spnsrIndex, planObj);
			setDateObjectsForValidityPeriod();
			//policyChange(); // skipped this since the mandatory fields (red star mark and the overall treatment amount not required to be loaded)
			/*if(corpInsuranceCheck != "Y"){

				if (policyStart !=null && policyStart != '') policyStartDtObj.value = formatDate(new Date(policyStart), "ddmmyyyy", "-");
				else policyStartDtObj.value = '';

				if (policyEnd !=null && policyEnd != '') policyEndDtObj.value = formatDate(new Date(policyEnd), "ddmmyyyy", "-");
				else policyEndDtObj.value = '';

				}else{

				if (policyStart !=null && policyStart != '') policyStartDtObj.textContent = formatDate(new Date(policyStart), "ddmmyyyy", "-");
				else policyStartDtObj.value = '';

				if (policyEnd !=null && policyEnd != '') policyEndDtObj.textContent = formatDate(new Date(policyEnd), "ddmmyyyy", "-");
				else policyEndDtObj.value = '';

			}*/

		}else {
			setSelectedIndex(planTypeObj, "");
			insuCatChange(spnsrIndex);
			setSelectedIndex(planObj, "");
			policyStartDtObj.value = '';
			policyEndDtObj.value = '';
		}
	//}

	if (!empty(gPatientPolciyNos) && (gPatientPolciyNos.length > 0)) {

		var member = findInList(gPatientPolciyNos, "patient_policy_id", previousPatientPolicyId);
		if (member != null) validMemberId = member.member_id;

		memberId = previousMemberId;
		policyNumber = previousPolicyNumber;
		policyStart = previousStartDate;
		holder = previousHolder;
		relation = previousRelation;

		var item = findInList(tpanames, "tpa_id", tpaId);

		if(null != item && item !=""){
		for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
					if(sponsorTypeList[i].validity_period_mandatory == 'Y' && sponsorTypeList[i].member_id_mandatory == 'Y')
						if (isModAdvanceIns && member != null && validMemberId != null && validMemberId == previousMemberId) {
							var d = new Date(gServerNow);
							d.setHours(0);
							d.setSeconds(0);
							d.setMinutes(0);
							d.setMilliseconds(0);
							var diff = new Date(previousEndDate) - d;
							if (diff >= 0) {
								policyEnd = previousEndDate;
							} else {
								memberId = '';
								policyNumber = '';
								policyStart = '';
								holder = '';
								relation = '';
								alert(getString("js.registration.patient.policy.validity.has.expired.on.string")+" " + formatDate(new Date(previousEndDate), 'ddmmyyyy', '-') +
												"\n" +getString("js.registration.patient.select.another.string")+" "+ "\n"+getString("js.registration.patient.or.string") + "\n"+getString("js.registration.patient.enter.validity.end.date.string"));
								policyEndDtObj.focus();
							}
						}
				}
			}
		}
	}

	if (priorAuthIdObj  != null
			&& previousPriorAuthid != null && previousPriorAuthid != "" && previousPriorAuthid != "0")
		priorAuthIdObj.value = previousPriorAuthid;

	if (priorAuthModeIdObj != null
      		&& previousPriorAuthmodeid != null && previousPriorAuthmodeid != "")
		setSelectedIndex(priorAuthModeIdObj, previousPriorAuthmodeid);

	// If mod_adv_ins (or) mod_insurance is enabled, set the member id, policy holder and relationship details.
	if (loadPreviousInsDetails) {
		//if (isModAdvanceIns) {
			memberIdObj.value = memberId;
			policyNumberObj.value = policyNumber;
			policyHolderObj.value = holder;
			patientRelationshipObj.value = relation;

		//}
	}else /*if (isModAdvanceIns )*/ {
		memberIdObj.value = "";
		policyNumberObj.value = "";
		policyHolderObj.value = "";
		patientRelationshipObj.value = "";
	}

	// Set the approval limit.
	/*if (loadPreviousInsDetails && (isModAdvanceIns || isModInsurance)) {
		insuranceApprovalObj.value = !empty(gPatientBillsApprovalTotal) ? formatAmountValue(gPatientBillsApprovalTotal) : "";
	}else {
		insuranceApprovalObj.value = "";
	}*/

	// bug no #45791,45162(Corp Insurance)
	if(insuranceApprovalObj != null)
		insuranceApprovalObj.value = "";

	loadRatePlanDetails(validRatePlan);
}

function loadRatePlanDetails(validRatePlan) {

	var loadPreviousInsDetails	= false;
	var allowInsurance			= false;

	// If bill type is bill now and allow bill now insurance is true or bill type is bill later or for out side patient then load
	// the previous patient insurance details.
	if (document.mainform.bill_type
			&& (document.mainform.bill_type.value == "C" || (allowBillNowInsurance == 'true' && document.mainform.bill_type.value == 'P'))) {
		allowInsurance = true;
	}

	if (allowInsurance || screenid == "out_pat_reg") {
		loadPreviousInsDetails = true;
	}

	var organizationObj	  = document.mainform.organization;
	var patientCategoryObj	= document.mainform.patient_category_id;

	// Set the rate plan -- If mod_adv_ins, set the rate plan according to the above criteria filtered.
	// In other cases i.e if mod_insurance (or) no insurance, set the previous rate plan.
	RatePlanList();

	if (loadPreviousInsDetails) {
		if (isModAdvanceIns || isModInsurance) {

			// Rate plan defaulting.
			var patientCategoryObj = document.mainform.patient_category_id;
			if (patientCategoryObj == null) {
				if (!empty(gPreviousPlan) && !empty(validRatePlan))
					setSelectedIndex(organizationObj, validRatePlan);
				else setSelectedIndex(organizationObj, gPreviousRatePlan);
			}else {
				if (!empty(gPreviousPlan) && !empty(validRatePlan))
					setSelectedIndex(organizationObj, validRatePlan);
				//else if (!empty(gPreviousPatientCategoryId)) {}
				//else setSelectedIndex(organizationObj, gPreviousRatePlan);
				else setSelectedIndex(organizationObj, gPreviousRatePlan);
			}
		}else {
			loadPreviousRatePlan();
		}
		ratePlanChange();

	}else {
		loadPreviousRatePlan();
		ratePlanChange();
	}
}

function loadPreviousRatePlan() {
	var organizationObj	  = document.mainform.organization;
	var patientCategoryObj = document.mainform.patient_category_id;
	if (patientCategoryObj == null) {
		if (!empty(gPreviousRatePlan))
			setSelectedIndex(organizationObj, gPreviousRatePlan);
		else
			setSelectedIndex(organizationObj, "ORG0001");
	} else {
		if (!empty(gPreviousRatePlan))
			setSelectedIndex(organizationObj, gPreviousRatePlan);
	}

	var patientCategoryExpDtObj = document.mainform.category_expiry_date;

	if (!empty(gPreviousPatientCategoryId) && patientCategoryObj) {
		setSelectedIndex(patientCategoryObj, gPreviousPatientCategoryId);
		onChangeCategory(); // this will also load the tpa list
	}

	if (!empty(gPreviousPatientCategoryExpDate) && patientCategoryExpDtObj) {
		patientCategoryExpDtObj.value = formatDate(new Date(gPreviousPatientCategoryExpDate), "ddmmyyyy", "-");
		document.getElementById('cardExpiryDate').value = formatDate(new Date(gPreviousPatientCategoryExpDate), "ddmmyyyy", "-");
	}
}

function loadCorporateDetails(spnsrIndex) {

	var corporateSpnsrObj= null;
	var employeeIdObj	= null;
	var employeeNameObj	= null;
	var patientRelationshipObj	= null;
	var insuranceApprovalObj	= null;

	if (spnsrIndex == 'P') {
		loadTpaList('P');
		onCorporateChange('P');
		corporateSpnsrObj = document.getElementById("primary_corporate");
		employeeIdObj = document.getElementById("primary_employee_id");
		employeeNameObj = document.getElementById("primary_employee_name");
		patientRelationshipObj = document.getElementById("primary_employee_relation");
		insuranceApprovalObj = document.getElementById("primary_corporate_approval");

		setSelectedIndex(corporateSpnsrObj, gPreviousPrimaryTpa);
		onCorporateChange('P');
		employeeIdObj.value		= gPreviousCorporateEmployeeId;
		employeeNameObj.value	= gPreviousCorporateEmployeeName;
		patientRelationshipObj.value	= gPreviousCorporateRelation;
		insuranceApprovalObj.value	= null;

	}else if (spnsrIndex == 'S') {
		loadTpaList('S');
		onCorporateChange('S');
		corporateSpnsrObj = document.getElementById("secondary_corporate");
		employeeIdObj = document.getElementById("secondary_employee_id");
		employeeNameObj = document.getElementById("secondary_employee_name");
		patientRelationshipObj = document.getElementById("secondary_employee_relation");
		insuranceApprovalObj = document.getElementById("secondary_corporate_approval");

		setSelectedIndex(corporateSpnsrObj, gPreviousSecondaryTpa);
		onCorporateChange('S');
		employeeIdObj.value		= gPreviousSecCorporateEmployeeId;
		employeeNameObj.value	= gPreviousSecCorporateEmployeeName;
		patientRelationshipObj.value	= gPreviousSecCorporateRelation;
		insuranceApprovalObj.value	= null;
	}
}

function loadNationalDetails(spnsrIndex) {

	var nationalSpnsrObj= null;
	var nationalIdObj	= null;
	var nationalMemberNameObj	= null;
	var patientRelationshipObj	= null;
	var insuranceApprovalObj	= null;

	if (spnsrIndex == 'P') {
		loadTpaList('P');
		//onNationalSponsorChange('P');
		nationalSpnsrObj = document.getElementById("primary_national_sponsor");
		nationalIdObj = document.getElementById("primary_national_member_id");
		nationalMemberNameObj = document.getElementById("primary_national_member_name");
		patientRelationshipObj = document.getElementById("primary_national_relation");
		insuranceApprovalObj = document.getElementById("primary_national_approval");

		setSelectedIndex(nationalSpnsrObj, gPreviousPrimaryTpa);
		//onNationalSponsorChange('P');
		nationalIdObj.value			= gPreviousNationalId;
		nationalMemberNameObj.value	= gPreviousNationalCitizenName;
		patientRelationshipObj.value= gPreviousNationalRelation;
		insuranceApprovalObj.value	= null;

	}else if (spnsrIndex == 'S') {
		loadTpaList('S');
		//onNationalSponsorChange('S');
		nationalSpnsrObj = document.getElementById("secondary_national_sponsor");
		nationalIdObj = document.getElementById("secondary_national_member_id");
		nationalMemberNameObj = document.getElementById("secondary_national_member_name");
		patientRelationshipObj = document.getElementById("secondary_national_relation");
		insuranceApprovalObj = document.getElementById("secondary_national_approval");

		setSelectedIndex(nationalSpnsrObj, gPreviousSecondaryTpa);
		//onNationalSponsorChange('S');
		nationalIdObj.value			= gPreviousSecNationalId;
		nationalMemberNameObj.value	= gPreviousSecNationalCitizenName;
		patientRelationshipObj.value= gPreviousSecNationalRelation;
		insuranceApprovalObj.value	= null;
	}
}

function loadInsurancePolicyDetails() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var primarySponsorWrapperObj = document.getElementById("primary_sponsor_wrapper");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");

	var loadPreviousInsDetails	= false;
	var allowInsurance			= false;

	// If bill type is bill now and allow bill now insurance is true or bill type is bill later or for out side patient then load
	// the previous patient insurance details.
	if (document.mainform.bill_type
			&& (document.mainform.bill_type.value == "C" || (allowBillNowInsurance == 'true' && document.mainform.bill_type.value == 'P'))) {
		allowInsurance = true;
	}

	if (allowInsurance || screenid == "out_pat_reg") {
		loadPreviousInsDetails = true;
	}

	if (!loadPreviousInsDetails) {
		loadRatePlanDetails(gPreviousRatePlan);
		return;
	}

	primarySponsorObj.value = gPreviousPrimarySponsorIndex;
	resetPrimarySponsorChange();
	secondarySponsorObj.value = gPreviousSecondarySponsorIndex;
	resetSecondarySponsorChange();
	if (gPreviousPrimarySponsorIndex == 'I')
		{
			primarySponsorWrapperObj.checked = true;
			loadInsuranceDetails('P');// Also loads rate plan
		}

	else if (gPreviousPrimarySponsorIndex == 'C') {
		loadCorporateDetails('P');
		loadRatePlanDetails(gPreviousRatePlan);

	}else if (gPreviousPrimarySponsorIndex == 'N') {
		loadNationalDetails('P');
		loadRatePlanDetails(gPreviousRatePlan);

	}else
		loadRatePlanDetails(gPreviousRatePlan);

	if (gPreviousSecondarySponsorIndex == 'I')
		{
			secondarySponsorWrapperObj.checked = true;
			loadInsuranceDetails('S');
		}
	else if (gPreviousSecondarySponsorIndex == 'C')
		loadCorporateDetails('S');
	else if (gPreviousSecondarySponsorIndex == 'N')
		loadNationalDetails('S');

}

// Practitioner type G--  General, S -- Specialist
// Sets the consultation type and gets the doctor charge

function setDoctorChargeBasedOnPractition(doctorId, isFirstVisit, isIpFollowUp) {
	var form = document.mainform;
	var docChrgObj = document.mainform.doctorCharge;
	var docChrg = (docChrgObj != null) ? docChrgObj.value : "";
	var doctor = findInList(doctorsList, "doctor_id", doctorId);
	if (!empty(doctor)) {
		var practition_type = doctor.practition_type;
		if (!empty(practition_type)) {
			if (practition_type == 'G') {
				if (isFirstVisit) {
					if (!empty(default_gp_first_consultation))
						setSelectedIndex(document.mainform.doctorCharge, default_gp_first_consultation);
					else setSelectedIndex(document.mainform.doctorCharge, '-1');
				} else {
					if (typeof(isIpFollowUp) == 'undefined') {
						if (!empty(default_gp_revisit_consultation))
							setSelectedIndex(document.mainform.doctorCharge, default_gp_revisit_consultation);
						else setSelectedIndex(document.mainform.doctorCharge, '-2');
					} else {
						if (isIpFollowUp) setSelectedIndex(document.mainform.doctorCharge, '-4');
						else setSelectedIndex(document.mainform.doctorCharge, '-2');
					}
				}
			} else if (practition_type == 'S') {
				if (isFirstVisit) {
					if (!empty(default_sp_first_consultation))
						setSelectedIndex(document.mainform.doctorCharge, default_sp_first_consultation);
					else setSelectedIndex(document.mainform.doctorCharge, '-1');
				} else {
					if (typeof(isIpFollowUp) == 'undefined') {
						if (!empty(default_sp_revisit_consultation))
							setSelectedIndex(document.mainform.doctorCharge, default_sp_revisit_consultation);
						else setSelectedIndex(document.mainform.doctorCharge, '-2');
					} else {
						if (isIpFollowUp)
							setSelectedIndex(document.mainform.doctorCharge, '-4');
						else setSelectedIndex(document.mainform.doctorCharge, '-2');
					}
				}
			} else {
				if (isFirstVisit)
					setSelectedIndex(document.mainform.doctorCharge, '-1');
				else {
					if (typeof(isIpFollowUp) == 'undefined')
						setSelectedIndex(document.mainform.doctorCharge, '-2');
					else {
						if (isIpFollowUp)
							setSelectedIndex(document.mainform.doctorCharge, '-4');
						else setSelectedIndex(document.mainform.doctorCharge, '-2');
					}
				}
			}
		} else {
			if (isFirstVisit)
				setSelectedIndex(document.mainform.doctorCharge, '-1');
			else {
				if (typeof (isIpFollowUp == 'undefined'))
					setSelectedIndex(document.mainform.doctorCharge, '-2');
				else {
					if (isIpFollowUp)
						setSelectedIndex(document.mainform.doctorCharge, '-4');
					else setSelectedIndex(document.mainform.doctorCharge, '-2');
				}
			}
		}
	} else {
		if(document.mainform.doctorCharge)
			setSelectedIndex(document.mainform.doctorCharge, '');
	}

	// If scheduler patient, the consultation type is as scheduled.
	//if (document.mainform.appointmentId != null
			//&& document.mainform.appointmentId.value != "")
		//setSelectedIndex(docChrgObj, docChrg);
	getDoctorCharge();
}

function getMainVisitOnChangeToFollowUpWithoutCons() {
	var doctorId = (document.mainform.doctor != null) ? document.mainform.doctor.value : null;

	if (!empty(doctorId)) {

		var tempPreviousDocVisits = gPreviousDocVisits;
		getPatientDoctorVisits(doctorId);

		if (!empty(gPreviousDocVisits)) {
			var cons = gPreviousDocVisits[0];
			var mainVisitId = cons.main_visit_id;
			if (document.mainform.main_visit_id.value == mainVisitId) {
				gPreviousDocVisits = tempPreviousDocVisits;
				return;
			}
		}

		var mrno = document.mrnoform.mrno.value;
		var doctorId = document.mainform.doctor.value;
		var doctorName = document.mainform.doctor_name.value;
		var opType = document.mainform.op_type.value;

		clearRegDetails();

		gSelectedDoctorName = doctorName;
		gSelectedDoctorId = doctorId;
		document.mrnoform.mrno.value = mrno;
		setSelectedIndex(document.mainform.op_type, opType);

		getPatientDoctorVisits(doctorId);

		var mainVisitId = null;
		if (!empty(gPreviousDocVisits)) {
			for (var i = 0; i < gPreviousDocVisits.length; i++) {
				var cons = gPreviousDocVisits[i];
				mainVisitId = cons.main_visit_id;
				break;
			}
		}

		document.mainform.main_visit_id.value = mainVisitId;

		getRegDetails();
	}
}

// Function called when Visit type is changed in UI

function onChangeVisitType() {
	loadPrvPrescripitons = false;
	var spnsrIndex = getMainSponsorIndex();
	if (document.mainform.op_type != null) {
		if (document.mainform.op_type.value == "D")
			getMainVisitOnChangeToFollowUpWithoutCons();

		if(document.mainform.op_type.value == "F" || document.mainform.op_type.value == "D")
			loadInsurancePolicyDetails();
	}
	changeVisitType();
	loadPreviousUnOrderedPrescriptions();
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
	estimateTotalAmount();

	var priPlanObj= getPrimaryPlanObj();
	if(priPlanObj!=null)
		enableDisablePlanDetails('P',priPlanObj);
	var secPlanObj = getSecondaryPlanObj();
	if(secPlanObj !=null)
		enableDisablePlanDetails('S',secPlanObj);

	loadPrvPrescripitons = true;
	loadPreviousUnOrderedPrescriptions();
	if (category != null && (category != 'SNP' || !empty(scheduleName)))
		loadSchedulerOrders();
}

// Load previous visit details based on selected op type and set the consultation to get the doctor charge.

function changeVisitType() {
	setPatientComplaint();
}


/** Doctor Consultations & Visit Type:
 * 1. For main visit the selected doctor is the base doctor.
 * 2. For revisit, the selected doctor's validity is checked w.r.t previous visits base doctor
      and visit type i.e, op type is determined as Main / Follow up / Revisit.
 * 3. If for revisit, different doctor is selected, then the visit type is Main.
 */

/* Function called when patient details are loaded (patientDetailsResponseHandler())
	(or) to load scheduler patient details
		New patient --> loadSchedulerPatientDetails()
		Existing patient --> loadSchedulerPatientInfo()
*/

//loading consultation types depending upon visit type if in generic_preferences allow_all_cons_types_in_reg is 'N';
//otherwise all consultation types.

function loadPreferenceConsultationTypes(docConsObj) {
	var orgObj = document.mainform.organization;
	var opTypeObj = document.mainform.op_type;

	var orgId = empty(orgObj.value) ? 'ORG0001' : orgObj.value;
	var consTypesList = cachedConsultTypes[orgId];
	var opType = (opTypeObj != null) ? opTypeObj.value : "";

	var defaultText = getString("js.registration.patient.commonselectbox.defaultText");

	if(opType == 'M' || opType == 'R') {
		// visit type Main, load consultation types (-1/0 : OP Consultation/Others)
		var consFilterList = filterListWithValues(consTypesList, "visit_consultation_type", new Array(-1,0));
		loadSelectBox(docConsObj, consFilterList, "consultation_type", "consultation_type_id", defaultText);

	} else if(opType == 'F') {
		// visit type Follow up(with consultation), load consultation types  (-2/-4/0 : Revisit Consultation/IP Follow Up Consultation/Others)
		var consFilterList = filterListWithValues(consTypesList, "visit_consultation_type", new Array(-2,-4,0));
		loadSelectBox(docConsObj, consFilterList, "consultation_type", "consultation_type_id", defaultText);

	} else if(opType == 'D') {
		// visit type Follow up(with out consultation), load empty consultation types.
		loadSelectBox(docConsObj, null, "consultation_type", "consultation_type_id", defaultText);

	} else {
		loadSelectBox(docConsObj, consTypesList, "consultation_type", "consultation_type_id", defaultText);
	}

	if (docConsObj)
		sortDropDown(docConsObj);
}

function setDocRevistCharge(doctorId) {
	getPatientDoctorVisits(doctorId);

	var opType = (document.mainform.op_type != null) ? document.mainform.op_type.value : "";
	if (opType == 'D') return;

	// OP follow up for IP visit
	if (isRevisitAfterDischarge(doctorId, gPatientLastIpVisit, gFollowUpDocVisits)) {
		if(document.getElementById("docConsultationFees") != null)
			document.getElementById("docConsultationFees").textContent = '';
		if(document.mainform.op_type) {
			setSelectedIndex(document.mainform.op_type, "M");
			document.mainform.op_type.disabled = true;
		}
		setDoctorChargeBasedOnPractition(doctorId, false, true); // isFirstVisit = false, isIpFollowUp = true, default IP follow up consultation
		disableOrEnableInsuranceFields(false);

		if (document.getElementById('ailment').value != "")
			document.getElementById('ailment').disabled = true;
	}

	// Doctor no previous visits -- Main Visit
	if (empty(gPreviousDocVisits)) {
		if(document.getElementById("docConsultationFees") != null)
			document.getElementById("docConsultationFees").textContent = '';
		if(document.mainform.op_type) {
			setSelectedIndex(document.mainform.op_type, "M");
			document.mainform.op_type.disabled = true;
		}
		disableOrEnableInsuranceFields(false);
		document.mainform.main_visit_id.value = "";
		setDoctorChargeBasedOnPractition(doctorId, true); // isFirstVisit = true, default OP Main visit consultation
		document.getElementById('referaldoctorName').disabled = false;
		document.getElementById('ailment').disabled = false;

	} else {
		var doctor = findInList(doctorsList, 'doctor_id', doctorId);
		// Doctor has no validity days or count -- Main Visit
		if (doctor == null || empty(doctor.op_consultation_validity) || empty(doctor.allowed_revisit_count)) {
			if(document.getElementById("docConsultationFees") != null)
				document.getElementById("docConsultationFees").textContent = '';
			if(document.mainform.op_type) {
				setSelectedIndex(document.mainform.op_type, "M");
				document.mainform.op_type.disabled = true;
			}
			disableOrEnableInsuranceFields(false);
			document.mainform.main_visit_id.value = "";
			setDoctorChargeBasedOnPractition(doctorId, true); // isFirstVisit = true, default OP Main visit consultation
			document.getElementById('referaldoctorName').disabled = false;
			document.getElementById('ailment').disabled = false;

			// Doctor visit not within validity -- Revisit
		} else if (!setVisitType(doctorId, gPreviousDocVisits)) {
			if(document.getElementById("docConsultationFees") != null)
				document.getElementById("docConsultationFees").textContent = '';
			if(document.mainform.op_type) {
				setSelectedIndex(document.mainform.op_type, "R");
				document.mainform.op_type.disabled = false;
			}
			loadInsurancePolicyDetails();
			disableOrEnableInsuranceFields(false);
			setDoctorChargeBasedOnPractition(doctorId, true); // isFirstVisit = true, default OP Main visit consultation
			document.getElementById('ailment').disabled = true;

			// Doctor visit within validity -- Follow Up Visit with consultation
		} else {
			if(document.getElementById("docConsultationFees") != null)
				document.getElementById("docConsultationFees").textContent = '';
			if(document.mainform.op_type) {
				setSelectedIndex(document.mainform.op_type, "F");
				document.mainform.op_type.disabled = false;
			}
			loadInsurancePolicyDetails();
			disableOrEnableInsuranceFields(true);
			setDoctorChargeBasedOnPractition(doctorId, false); // isFirstVisit = false, default OP Follow up consultation
			if (document.getElementById('ailment').value != "")
				document.getElementById('ailment').disabled = true;
		}
	}

	if ( opType != document.mainform.op_type.value && null != document.mrnoform.mrno && document.mrnoform.mrno.value != ""){
		var priPlanObj= getPrimaryPlanObj();
		if(priPlanObj!=null)
			enableDisablePlanDetails('P',priPlanObj);
		var secPlanObj = getSecondaryPlanObj();
		if(secPlanObj!=null)
			enableDisablePlanDetails('S',secPlanObj);
	}
	// Note: Follow Up Visit without consultation -- user needs to select manually
}

/* Get the patient previous visits for the selected doctor */

function getPatientDoctorVisits(doctor) {
	var mrno = document.mrnoform.mrno.value;
	var opType = (document.mainform.op_type != null) ? document.mainform.op_type.value : "";

	if (mrno != null && mrno != '') {
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=getPatientDoctorVisits&mrNo=' + mrno
						+ '&doctor=' + doctor + '&opType=' + opType;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var visits =" + ajaxobj.responseText);
					if (!empty(visits)) {
						gPreviousDocVisits = visits;
					}else
						gPreviousDocVisits = null;
				}
			}
		}
	}
}


// Set the main visit id

function setVisitType(doctorId, gPreviousDocVisits) {

	if (doctorId == null) return false;

	var doctor = findInList(doctorsList, 'doctor_id', doctorId);
	if (doctor == null) return false;
	var dept = doctor.dept_id;

	if (empty(gPreviousDocVisits)) return false;

	var validityDays = doctor.op_consultation_validity;
	var maxVisits = doctor.allowed_revisit_count;

	var revisitCount = 0;
	var visitWithinValidity = false;
	var mainVisitId = null;
	var k = 0;
	for (var i = 0; i < gPreviousDocVisits.length; i++) {
		var cons = gPreviousDocVisits[i];

		// Based on visit type dependence (Doctor/Speciality) the op-type is determined.
		if ((visitTypeDependence == 'D' && doctorId == cons.doctor_name) || (visitTypeDependence == 'S' && dept == cons.dept_name)) {
			var visitDate = new Date(cons.visited_date);
			var currentDate = getServerDate();
			var days = daysDiff(visitDate, currentDate);
			if(consultationValidityUnits != 'T') {
				visitDate = getDatePart(cons.visited_date);
				currentDate = getDatePart(currentDate);
				days = daysDiff(visitDate, currentDate);
				if (days < 1 && days >= 0)
					days = 1;
				else
					days = days + 1;
			}
			revisitCount++;
			if (days <= validityDays) {
				visitWithinValidity = true;
				// Choose the latest doctor visit for setting the main visit id.
				if (k == 0) {
					mainVisitId = cons.main_visit_id;
				}
				k++;
			}
			if (!visitWithinValidity)
				break;
		}
	}
	document.mainform.main_visit_id.value = mainVisitId;
	return visitWithinValidity && (revisitCount <= maxVisits);
}

// Function called on op_type change in UI (changeVisitType())
// (or) to calcuate estimate amount when Insu. company/TPA/Rate plan is changed in UI (calculateTotalEstimateAmount())
// (or) to load existing patient details (loadSchedulerPatientInfo())

function getDoctorCharge() {
	var orgid = document.mainform.organization.value;
	var bedType = (screenid != "ip_registration" && !empty(billingBedTypeForOp)) ? billingBedTypeForOp : 'GENERAL';
	var ajaxobj = newXMLHttpRequest();
	var doctor = document.mainform.doctor.value;
	var chargeType = document.mainform.doctorCharge ? document.mainform.doctorCharge.value : '';
	var planId = ""; var planObj = null;
	var tpaId = ""; var tpaObj = null;

	var discPlanId = document.mainform.insurance_discount_plan.value;

	var spnsrIndex = getMainSponsorIndex();
	if (spnsrIndex != null) {
		if (spnsrIndex == 'P') {
			planObj = getPrimaryPlanObj();
			tpaObj	= getPrimarySponsorObj();
		}else if (spnsrIndex == 'S') {
			planObj = getSecondaryPlanObj();
			tpaObj	= getSecondarySponsorObj();
		}

		if (planObj != null) planId = planObj.value;
		if (tpaObj != null) tpaId = tpaObj.value;
	}

	var planIds = getPlanIds();

	if (screenid == "ip_registration") {
		var visitType = "i";
	} else {
		var visitType = "o";
	}
	var url = '';
	if (doctor != '' && chargeType != '') {
		if (null != planIds && planIds.length >  0) {
			url = cpath + '/master/orderItems.do?method=getItemCharges&type=Doctor' + '&id=' + doctor
				+ '&orgId=' + orgid + '&bedType=' + bedType + '&chargeType=' + chargeType
				+ '&planIds=' + planIds + '&visitType=' + visitType + '&insurance_discount_plan=' + discPlanId;
		} else if (tpaId != "") {
			url = cpath + '/master/orderItems.do?method=getItemCharges&type=Doctor' + '&id=' + doctor
				+ '&orgId=' + orgid + '&bedType=' + bedType + '&chargeType=' + chargeType
				+ '&insurance=true&visitType=' + visitType + '&insurance_discount_plan=' + discPlanId;
		} else {
			url = cpath + '/master/orderItems.do?method=getItemCharges&type=Doctor' + '&id=' + doctor
				+ '&orgId=' + orgid + '&bedType=' + bedType + '&chargeType=' + chargeType
				+ '&visitType=' + visitType + '&insurance_discount_plan=' + discPlanId;
		}

		ajaxobj.open("POST", url, false);
		ajaxobj.send(null);

		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var doctorChargeJSBean = " + ajaxobj.responseText);
					var charge = 0;
					if (doctorChargeJSBean != null) {
						charge = doctorChargeJSBean[0].amount;
					}
					var insurancePayable = "true";
					if (addOrderDialog.orderChargeHeadJSON != null && !empty(doctorChargeJSBean)) {
						insurancePayable = findInList(addOrderDialog.orderChargeHeadJSON, "CHARGEHEAD_ID",
													doctorChargeJSBean[0].chargeHead).INSURANCE_PAYABLE == 'Y' ? "true" : "false";
					}

					document.getElementById("doc_chargehead").value = doctorChargeJSBean[0].chargeHead;
					document.getElementById("doc_insCategoryId").value = doctorChargeJSBean[0].insuranceCategoryId;
					document.getElementById("doc_amount").value = doctorChargeJSBean[0].amount;
					document.getElementById("doc_discount").value = doctorChargeJSBean[0].discount;

					document.mainform.consFees.value = charge;
					document.mainform.opdocchrg.value = charge;
					if(document.getElementById("docConsultationFees") != null)
						document.getElementById("docConsultationFees").textContent = formatAmountValue(charge);
					if (document.getElementById('patietConsultAmount')) {

					var insClaimAmt = doctorChargeJSBean[0].insuranceClaimAmount;
					insClaimAmt = null != planIds && planIds.length > 0 ?(planIds.length==2 ? doctorChargeJSBean[0].claimAmounts[0] + doctorChargeJSBean[0].claimAmounts[1] :
						doctorChargeJSBean[0].claimAmounts[0]) : insClaimAmt ;

					//document.getElementById('patietConsultAmount').value = charge - insClaimAmt;
					var ordCat = document.getElementsByName("orderCategory");
					var ordFirstOfCategory = document.getElementsByName("firstOfCategory");

					var insPayable = findInList(addOrderDialog.orderChargeHeadJSON, "CHARGEHEAD_ID",
													doctorChargeJSBean[0].chargeHead).INSURANCE_PAYABLE;

					if (ordCat != null && ordCat != undefined && ordCat.length > 0) {
						for (var i = 0; i < ordCat.length; i++) {
							if (ordCat[i].value == doctorChargeJSBean[0].insuranceCategoryId && ordFirstOfCategory[i].value == "true") {
								document.getElementById('patietConsultAmount').value = 0;
								document.getElementById('regDocFirstOfCategory').value = "false";

								var claimAmt = 0;
								var remainingAmt = charge;
								if(planIds != null && planIds.length > 0) {
								for(var k=0; k<planIds.length; k++) {
									claimAmt = calculateClaimAmount(remainingAmt,doctorChargeJSBean[0].discount,doctorChargeJSBean[0].insuranceCategoryId,
									doctorChargeJSBean[0].firstOfCategory, doctorChargeJSBean[0].visitType,doctorChargeJSBean[0].billNo,planIds[k], insPayable);
									remainingAmt = remainingAmt - claimAmt;
									//document.getElementById('patietConsultAmount').value = remainingAmt;
								}
							}

							}
						}
					}
					if (insurancePayable != "true")
						document.getElementById('patietConsultAmount').value = charge;
					}
				}
			}
		}
		//estimateTotalAmount();
	} else {
		document.mainform.consFees.value = 0;
		document.mainform.opdocchrg.value = 0;
		if(document.getElementById("docConsultationFees"))
			document.getElementById("docConsultationFees").textContent = 0;
		//estimateTotalAmount();
	}

	var display = displayEstimatedTotalAmountTable(orgid);
	if (display && document.getElementById("docConsultationFees")) {
		document.getElementById("docConsultationFees").style.display = 'block';
	}else {
		if(document.getElementById("docConsultationFees"))
			document.getElementById("docConsultationFees").style.display = 'none';
	}

	getBillChargeClaims("new_visit", document.mainform);
	estimateTotalAmount();
}

// Function called when TPA is changed in UI

function onTpaChange(spnsrIndex) {
	tpaChange(spnsrIndex);
	ratePlanChange();
}

// Function called in 3 places, when Insurance company changed (loadTpaList()) (or) TPA is changed in UI
// (or) to load existing patient details (loadInsurancePolicyDetails())

function tpaChange(spnsrIndex) {


	var tpaIdObj = null;
	var tpaNameObj =null;
	var insuCompIdObj = null;
	var uploadRowObj = null;
  var sponsorDetails = null;

	if (spnsrIndex == 'P') {
		tpaIdObj = getPrimarySponsorObj();
		insuCompIdObj = getPrimaryInsuObj();
		uploadRowObj = getPrimaryUploadRowObj();
		tpaNameObj=getPrimarySponsorNameObj();
    sponsorDetails = primarySponsorDetails;
	}else if (spnsrIndex == 'S') {
		tpaIdObj = getSecondarySponsorObj();
		insuCompIdObj = getSecondaryInsuObj();
		uploadRowObj = getSecondaryUploadRowObj();
		tpaNameObj=getSecondarySponsorNameObj();
		sponsorDetails = secondarySponsorDetails;
	}

	if (tpaIdObj != null && tpaIdObj.value != '') {
		gIsInsurance = true;
		var selectedTpaId = tpaIdObj.value;
		var tpaValidityDate = new Date(sponsorDetails.validity_end_date);
    if(!empty(sponsorDetails.validity_end_date)){
    		     if (daysDiff(getServerDate(), tpaValidityDate) < 0) {
    		        showMessage("js.registration.patient.tpa.validity.check");
    		        tpaIdObj.value='';
    		        tpaNameObj.value='';
    		     }
    		     if (uploadRowObj != null) {
                 if (tpanames[i].scanned_doc_required == 'N')
                     uploadRowObj.style.display = 'none';
                 else
                     uploadRowObj.style.display = 'table-row';
              }
    }

/*
		for (var i = 0; i < tpanames.length; i++) {
			var tpaValidityDate = new Date(tpanames[i].validity_end_date);
			if (selectedTpaId == tpanames[i].tpa_id) {
				if (!empty(tpanames[i].validity_end_date)) {
					if (daysDiff(getServerDate(), tpaValidityDate) < 0) {
						showMessage("js.registration.patient.tpa.validity.check");
						tpaIdObj.value='';
						tpaNameObj.value='';
					}
				}
				if (uploadRowObj != null) {
					if (tpanames[i].scanned_doc_required == 'N')
						uploadRowObj.style.display = 'none';
					else
						uploadRowObj.style.display = 'table-row';
				}
				break;
			}
		}*/
	} else {
		gIsInsurance = false;
	}
	if (insuCompIdObj != null)
		sortDropDown(insuCompIdObj);
}

// Function called when Plan is changed in UI

function onPolicyChange(spnsrIndex) {
	policyChange(spnsrIndex);
	RatePlanList();
	ratePlanChange();
	//setSelectedDateForCorpInsurance();

	// Ins30 : Corporate insurance validations removed
	// if(corpInsuranceCheck == "Y")
		// checkCurrentDateWithEndDateForCorpInsurance();
}

// Function called when Plan type is changed (insuCatChange())
// (or) plan is changed (onPolicyChange())
// (or) Member ship autocomplete is changed (loadPolicyDetails)



function getRatePlanAjax() {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	if(screenid == "out_pat_reg"){
		url = cpath + "/pages/registration/outPatientRegistration.do?_method=getRatePlan";
	}else{
		url = cpath + "/pages/registration/IpRegistration.do?_method=getRatePlan";
	}

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function getSponsorDetails(tpaId, categoryId) {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	if(screenid == "out_pat_reg"){
		url = cpath + "/pages/registration/outPatientRegistration.do?_method=getSponsorDetails&tpa_id="+tpaId+"&category_id="+categoryId;
	}else{
		url = cpath + "/pages/registration/IpRegistration.do?_method=getSponsorDetails&tpa_id="+tpaId+"&category_id="+categoryId;
	}

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}


function policyChange(spnsrIndex) {

	var approvalAmtObj = null;
	var planObj = null;
	var policyValidityStartObj = null;
	var policyValidityEndObj = null;
	var memberIdObj = null;
	var policyNumberObj = null;
	var policyHolderObj = null;
	var insuranceDocContentObj = null;

	var approvalAmtStarObj = null;
	var policyValidityStartStarObj = null;
	var policyValidityEndStarObj = null;

	var tpaObj = null;
	let item;

	if (spnsrIndex == 'P') {
		approvalAmtObj = getPrimaryApprovalLimitObj();
		planObj = getPrimaryPlanObj();
		policyValidityStartObj = getPrimaryPolicyValidityStartObj();
		policyValidityEndObj = getPrimaryPolicyValidityEndObj();

		memberIdObj = getPrimaryInsuranceMemberIdObj();
		policyNumberObj = getPrimaryInsurancePolicyNumberObj();
		policyHolderObj = getPrimaryPatientHolderObj();
		insuranceDocContentObj = getPrimaryDocContentObj();

		approvalAmtStarObj = getPrimaryApprovalLimitStarObj();
		policyValidityStartStarObj = getPrimaryPolicyValidityStartStarObj();
		policyValidityEndStarObj = getPrimaryPolicyValidityEndStarObj();

		tpaObj = getPrimarySponsorObj();

		enableDisablePlanDetails("P",planObj);

		if (planObj != null) {
			if (empty(planObj.value)) {
				document.getElementById('pd_primary_planButton').disabled = true;
			} else {
				document.getElementById('pd_primary_planButton').disabled = false;
			}
			document.getElementById('primary_plan_div').title = "";
		}
		item = empty(primarySponsorDetails) ? "" : primarySponsorDetails;

	}else if (spnsrIndex == 'S') {
		approvalAmtObj = getSecondaryApprovalLimitObj();
		planObj = getSecondaryPlanObj();
		policyValidityStartObj = getSecondaryPolicyValidityStartObj();
		policyValidityEndObj = getSecondaryPolicyValidityEndObj();

		memberIdObj = getSecondaryInsuranceMemberIdObj();
		policyNumberObj = getSecondaryInsurancePolicyNumberObj();
		policyHolderObj = getSecondaryPatientHolderObj();
		insuranceDocContentObj = getSecondaryDocContentObj();

		approvalAmtStarObj = getSecondaryApprovalLimitStarObj();
		policyValidityStartStarObj = getSecondaryPolicyValidityStartStarObj();
		policyValidityEndStarObj = getSecondaryPolicyValidityEndStarObj();

		tpaObj = getSecondarySponsorObj();

		enableDisablePlanDetails("S",planObj);

		if (planObj != null){
			if (empty(planObj.value)){
				document.getElementById('pd_secondary_planButton').disabled = true;
			} else {
				document.getElementById('pd_secondary_planButton').disabled = false;
			}
			document.getElementById('secondary_plan_div').title = "";
		}
		item = empty(secondarySponsorDetails) ? "" : secondarySponsorDetails;

	}

	if (planObj != null) {
		var plan =  planObj.value;

		if (!empty(plan)) {
			if(item.validity_period_editable === 'Y'){
				policyValidityStartObj.removeAttribute("disabled");
				policyValidityEndObj.removeAttribute("disabled");

				for (var i = 0; i < policynames.length; i++) {
        	if (policynames[i].plan_id == plan) {
        		 if(policynames[i].insurance_validity_end_date != null && policynames[i].insurance_validity_end_date != "")
                			  policyValidityEndObj.value = formatDate(new Date(policynames[i].insurance_validity_end_date), 'ddmmyyyy','-');
             if(policynames[i].insurance_validity_start_date != null && policynames[i].insurance_validity_start_date != "")
                		  policyValidityStartObj.value =formatDate(new Date(policynames[i].insurance_validity_start_date), 'ddmmyyyy','-');
           }
        }
			}
			if(approvalAmtObj != null)
				approvalAmtObj.value = "";

			/*for (var i = 0; i < policynames.length; i++) {
				if (policynames[i].plan_id == plan) {
					if (!empty(policynames[i].overall_treatment_limit) && approvalAmtObj != null) {
						approvalAmtObj.value = formatAmountPaise(getPaise(policynames[i].overall_treatment_limit));
					}
					if(approvalAmtStarObj != null)
						approvalAmtStarObj.style.visibility = 'visible';
					if(policyValidityStartStarObj != null)
						policyValidityStartStarObj.style.visibility = 'visible';
					if(policyValidityEndStarObj)
						policyValidityEndStarObj.style.visibility = 'visible';
					break;
				}
			}*/
		} else {
			memberIdObj.value = "";
			policyNumberObj.value = "";
			policyHolderObj.value = "";
			policyValidityEndObj.value = "";
			policyValidityStartObj.value = "";
			policyValidityEndObj.setAttribute("disabled", true);
			policyValidityStartObj.setAttribute("disabled", true);
			insuranceDocContentObj.value = "";
		}
	}
}

function calculateTestAmount(index) {
	var testQty = document.getElementById('testQuantity' + index).value;
	if (testQty == '' || testQty == 0) {
		showMessage("js.registration.patient.is.qty.greater.than.zero.check");
		document.getElementById('testQuantity' + index).focus();
		return false;
	}
	var testRate = document.getElementById('testRate' + index).value;
	document.getElementById('testAmount' + index).value = (parseFloat(testQty) * parseFloat(testRate)).toFixed(2);
}

var cachedCharges = [];

function getCharge(chargeId, orgId, bedType, chargeType) {
	var amount = 0;

	var discPlanId = document.mainform.insurance_discount_plan.value;

	var ajaxobj = newXMLHttpRequest();
	if (empty(orgId)) orgId = 'ORG0001';
	bedType = (screenid != "ip_registration" && !empty(billingBedTypeForOp)) ? billingBedTypeForOp : 'GENERAL';
	var url = cpath + '/master/orderItems.do?method=getItemCharges&type=Direct Charge' + '&id=' + chargeId
								+ '&orgId=' + orgId + '&bedType=' + bedType + '&visitType=' + visitType + '&insurance_discount_plan=' + discPlanId;
	var key = chargeId + "_" + orgId + "_" + bedType;
	if (chargeType != null) {
		url = url + "&chargeType=" + chargeType;
		key += "_" + chargeType;
	}
	if (cachedCharges[key] == undefined) {
		ajaxobj.open("POST", url, false);
		ajaxobj.send(null);

		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var chargeJSBean = " + ajaxobj.responseText);
					amount = chargeJSBean[0].amount;
					cachedCharges[key] = amount;
				}
			}
		}
	} else {
		amount = cachedCharges[key];
	}
	return amount;
}
var totalRegistrationCharges = 0;
function estimateTotalAmount() {


	var totalEstimatedAmount = 0;
	var genregcharge = 0;
	var ipregcharge = 0;
	var opregcharge = 0;
	var mlcCharge = 0;
	var orgId;
	var bedType;
	var id;

	if (screenid == "ip_registration")
		id = "IPREG";
	else
		id = "OPREG";

	if (document.mainform.organization.value == "..Rate Plan..")
		orgId = "ORG0001";
	else
		orgId = document.mainform.organization.value;

	if (document.mainform.bed_type != null && document.mainform.bed_type.value != "")
		bedType = document.mainform.bed_type.value;
	else bedType = (screenid != "ip_registration" && !empty(billingBedTypeForOp)) ? billingBedTypeForOp : 'GENERAL';

	// Get OP or IP registration charge.
	if (document.mainform.reg_charge_applicable.value == "Y") {
		if (screenid == "ip_registration") {
			ipregcharge = getCharge(id, orgId, bedType);
		} else {
			opregcharge = getCharge(id, orgId, bedType);
		}
	}

	// Get MLC charge
	if (document.mrnoform.mlccheck != null && document.mrnoform.mlccheck.checked) {
		mlcCharge = getCharge('MLREG', orgId, bedType);
	}

	if (document.mainform.reg_charge_applicable.value == "Y" && regPref.no_reg_charge_sources.split(",").indexOf(document.mainform.resource_captured_from.value) === -1) {
		if (document.getElementById('regTypenew').checked == true) {
			// New registration: add the general registration charge.
			genregcharge = getCharge('GREG', orgId, bedType);
		} else {
			// Existing patient: based on validity, and if previous visit exists.
			if (gLastGenRegChargeAcceptedDate != null) {
				// previous visit exists, check the validity and apply genreg renewal charge
				if (regPref.regValidityPeriod > 0) {
					if (daysDiff(gLastGenRegChargeAcceptedDate, new Date()) > regPref.regValidityPeriod)
						genregcharge = getCharge('GREG', orgId, bedType, 'renewal');
				}
			} else if (!gPatientRegDateRaw) {
				genregcharge = getCharge('GREG', orgId, bedType);
			} else if (gPatientRegDate > goLiveDate) {
				genregcharge = getCharge('GREG', orgId, bedType);
			}
		}
	}

	if (document.mrnoform.group.value == "opreg") {
		var reg_charges = getPaise(opregcharge) + getPaise(genregcharge);
		totalRegistrationCharges = reg_charges;
		var consFee;
		if (document.getElementById('consFees').value == '') {
			consFee = 0;
		} else {
			consFee = document.getElementById('consFees').value;
		}
		totalEstimatedAmount += getPaise(consFee) + reg_charges;
	} else {
		totalEstimatedAmount += getPaise(ipregcharge) + getPaise(genregcharge);
	}

	if (document.mrnoform.mlccheck != null && document.mrnoform.mlccheck.checked) {
		totalEstimatedAmount += getPaise(mlcCharge);
	}

	if (screenid != "ip_registration" && screenid != "out_pat_reg") {
		document.getElementById('estimtAmount').innerHTML = formatAmountPaise(totalEstimatedAmount);
		document.getElementById('estimateAmount').value = formatAmountPaise(totalEstimatedAmount);
	}
	document.mainform.opIpcharge.value = formatAmountPaise(totalEstimatedAmount);
	totalRegistrationCharges = formatAmountPaise(totalRegistrationCharges);

	if(!empty(registrationChargeApplicability) && registrationChargeApplicability == 'A')
		setRegistrationChargesFlag(document.mainform.patient_category_id);

}

/*
 * Brings certain larger details via ajax to prevent page load slowdown.
 */
function getDetailsAjax() {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	if(screenid == "out_pat_reg"){
		url = cpath + "/pages/registration/outPatientRegistration.do?_method=getdetailsAJAX";
	}else{
		url = cpath + "/pages/registration/IpRegistration.do?_method=getdetailsAJAX";
	}

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function updateNewTotalAmount() {
	var totalNewPatientAmt = getTotalNewOrdersPatientAmountPaise(0);
	if (document.getElementById('totalNewPatientAmt') != null) {
		var totalNewPatientAmtEl = document.getElementById('totalNewPatientAmt');
		var doctorChargeEmpty = document.getElementById('doctorCharge').value;
		if (totalNewPatientAmtEl) {
			if(empty(doctorChargeEmpty))
			{
				totalNewPatientAmtEl.textContent =
					formatAmountPaise(totalNewPatientAmt);
				document.getElementById('patientAmt').value =
					formatAmountPaise(totalNewPatientAmt);
			} else {
				totalNewPatientAmtEl.textContent =
					formatAmountPaise(totalNewPatientAmt + getPaise(document.getElementById('patietConsultAmount').value) );
				document.getElementById('patientAmt').value =
					formatAmountPaise(totalNewPatientAmt + getPaise(document.getElementById('patietConsultAmount').value) );
			}
		}
	}
}

/* Function called in 5 places, when Patient category is changed (onChangeCategory())
	(or) Plan type is changed (insuCatChange())
	(or) Plan is changed (onPolicyChange())
	(or) to load existing patient details (loadInsurancePolicyDetails())
	(or) Member ship autocomplete is changed (loadPolicyDetails)
*/
function RatePlanList() {

	var spnsrIndex = getMainSponsorIndex();

	var planObj= null;
	var insuCompObj = null;

	if (spnsrIndex == 'P') {
		insuCompObj = getPrimaryInsuObj();
		planObj		= getPrimaryPlanObj();
	}else if (spnsrIndex == 'S') {
		insuCompObj = getSecondaryInsuObj();
		planObj		= getSecondaryPlanObj();
	}

	var categoryId = '';
	var planId = '';
	var insCompanyId = '';
	var catDefaultRatePlan = "";
	var planDefaultRatePlan = "";
	var insCompDefaultRatePlan = "";

	var orgIdList = null;

	var ratePlan = document.getElementById("organization");

	if (insuCompObj) insCompanyId = insuCompObj.value;
	if (planObj) planId = planObj.value;

	if (document.mainform.patient_category_id)
		categoryId = document.mainform.patient_category_id.value;

	if (categoryId != '') {
		var category = findInList(categoryJSON, "category_id", categoryId);
		if (!empty(category)) {
			if(screenid == 'ip_registration') {
				catDefaultRatePlan = category.ip_rate_plan_id;
				if (category.ip_allowed_rate_plans != '*')
					orgIdList = category.ip_allowed_rate_plans.split(',');

				if(category.ip_allowed_sponsors == null) {
					if (document.getElementById("primary_insurance_co") != null)
						loadSelectBox(document.getElementById('primary_insurance_co'), [],
							'insurance_co_name', 'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
					if (document.getElementById("secondary_insurance_co") != null)
						loadSelectBox(document.getElementById('secondary_insurance_co'), [],
							'insurance_co_name', 'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
				}
			} else {
				catDefaultRatePlan = category.op_rate_plan_id;
				if (category.op_allowed_rate_plans != '*' )
					orgIdList = category.op_allowed_rate_plans.split(',');

				if(category.op_allowed_sponsors == null) {
					if (document.getElementById("primary_insurance_co") != null)
						loadSelectBox(document.getElementById("primary_insurance_co"), [],
							'insurance_co_name', 'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
					if (document.getElementById("secondary_insurance_co") != null)
						loadSelectBox(document.getElementById("secondary_insurance_co"), [],
							'insurance_co_name', 'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
				}
			}
		}
	}

	// Rate plan related to insurance company
	if (insCompanyId != '') {
		var selectedIns = findInList(insuCompanyDetails, "insurance_co_id", insCompanyId);
		if (!empty(selectedIns) && !empty(selectedIns.default_rate_plan)) {
			insCompDefaultRatePlan = selectedIns.default_rate_plan;
			insCompDefaultRatePlan = isRatePlanActive(insCompDefaultRatePlan) ? insCompDefaultRatePlan : "";
		}
	}

	// Rate plan related to plan
	if (planId != '') {
		var plan = findInList(policynames, "plan_id", planId);
		planDefaultRatePlan = plan.default_rate_plan;
		planDefaultRatePlan = isRatePlanActive(planDefaultRatePlan) ? planDefaultRatePlan : "";
	}

	// If plan default rate plan is empty and insurance company default rate plan exists
	// then company default rate plan is choosen.
	if (empty(planDefaultRatePlan) && !empty(insCompDefaultRatePlan))
		planDefaultRatePlan = insCompDefaultRatePlan;

	// Empty Rate plans
	var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
	var len = 1;
	ratePlan.options.length = len;
	ratePlan.options[len - 1] = optn;

	var len = 1;

	if (document.mainform.patient_category_id && !empty(planDefaultRatePlan)) {
		if (!empty(orgIdList)) {
			for (var k = 0; k < orgIdList.length; k++) {
				// Not empty plan default rate plan and also category rate plan list containd the plan rate plan,
				// populate the rate plan.
				if (planDefaultRatePlan == orgIdList[k]) {
					var org = findInList(orgNamesJSON, "org_id", orgIdList[k]);
					if (!empty(org)) {
						var optn = new Option(org.org_name, org.org_id);
						len++;
						ratePlan.options.length = len;
						ratePlan.options[len - 1] = optn;
						break;
					}
				}
			}
		} else {
			for (var k = 0; k < orgNamesJSON.length; k++) {
				// Not empty plan default rate plan and also category rate plan list containd the plan rate plan,
				// populate the rate plan.
				if (planDefaultRatePlan == orgNamesJSON[k].org_id) {
					var optn = new Option(orgNamesJSON[k].org_name, orgNamesJSON[k].org_id);
					len++;
					ratePlan.options.length = len;
					ratePlan.options[len - 1] = optn;
					break;
				}
			}
		}

		if (ratePlan.options.length == 1) {
			showMessage("js.registration.patient.valid.rate.plans.against.category.plan.insurance.company");
		}

	} else {
		if (!empty(orgIdList)) {
			for (var k = 0; k < orgIdList.length; k++) {
				var org = null;
				if(orgIdList[k].org_id)
					org = findInList(orgNamesJSON, "org_id", orgIdList[k].org_id);
				else
					org = findInList(orgNamesJSON, "org_id", orgIdList[k]);
				if (!empty(org)) {
					var optn = new Option(org.org_name, org.org_id);
					len++;
					ratePlan.options.length = len;
					ratePlan.options[len - 1] = optn;
				}
			}
		} else {
			for (var i = 0; i < orgNamesJSON.length; i++) {
				ratePlan.options.length = len + 1;
				var optn = new Option(orgNamesJSON[i].org_name, orgNamesJSON[i].org_id);
				ratePlan.options[len] = optn;
				len++;
			}
		}
	}

	if (!empty(catDefaultRatePlan))
		setSelectedIndex(ratePlan, catDefaultRatePlan);

	if (!empty(planDefaultRatePlan))
		setSelectedIndex(ratePlan, planDefaultRatePlan);

	if (!empty(gPatientCategoryRatePlan)) {
		var patientCategoryObj = document.mainform.patient_category_id;
		if (patientCategoryObj == null)
			setSelectedIndex(ratePlan, gPatientCategoryRatePlan);
	}else {
		if (ratePlan.options.length == 2)
			ratePlan.selectedIndex = 1;
	}
	sortDropDown(ratePlan);
	setDiscountPlan(planId);
}

function setDiscountPlan(planId) {
	var discountPlanId = "";
	var discountPlanName = "";

	if ((!empty(discountPlansJSON)) && (!empty(discountPlansJSON[planId]))) {
		discountPlanId = discountPlansJSON[planId]["discount_plan_id"];
		discountPlanName = discountPlansJSON[planId]["discount_plan_name"];
	}

	var hidDiscountPlan = document.getElementById("insurance_discount_plan");
	if (!empty(hidDiscountPlan)) {
		hidDiscountPlan.value = discountPlanId;
	}

	var lblDiscountPlan = document.getElementById("insurance_discount_plan_lbl");
	if (!empty(lblDiscountPlan)) {
		if(discountPlanName != ""){
			document.getElementById("discPlanLabel").style.display = "block";
			lblDiscountPlan.innerHTML = discountPlanName;
			document.getElementById("insurance_discount_plan_lbl").style.display = "block";
		}else{
			document.getElementById("discPlanLabel").style.display = "none";
			document.getElementById("insurance_discount_plan_lbl").style.display = "none";
		}
	}
}

function CategoryList() {
	var categoryObj = document.getElementById("patient_category_id");
	var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
	categoryObj.options[0] = optn;

	var len = 1;
	for (var i = 0; i < categoryJSON.length; i++) {
		optn = new Option(categoryJSON[i].category_name, categoryJSON[i].category_id);
		categoryObj.options[len] = optn;
		len++;
	}
	if (!empty(defaultPatientCategory)) {
		setSelectedIndex(categoryObj, defaultPatientCategory);
		checkPassportAsteriskRequired();
	} else {
		// if there is only one category found, then default it.
		if (len == 2)
			categoryObj.options.selectedIndex = 1;
		else
			setSelectedIndex(categoryObj, "");

	}
}

// If the selected Primary sponsor is Insurance && has plan then Primary is Main Insurance
// If the selected Secondary sponsor is Insurance && has plan then Secondary is Main Insurance
// Otherwise Primary Sponsor is Main
function getMainSponsorIndex() {

	if (document.getElementById("primary_member_id") != null
			&& document.getElementById("primary_member_id").value !='')
		return 'P';

	if (document.getElementById("secondary_member_id") != null
			&& document.getElementById("secondary_member_id").value !='')
		return 'S';

	if (document.getElementById("primary_plan_id") != null
			&& document.getElementById("primary_plan_id").value !='')
		return 'P';

	if (document.getElementById("secondary_plan_id") != null
			&& document.getElementById("secondary_plan_id").value !='')
		return 'S';

	if (document.getElementById("primary_plan_type") != null
			&& document.getElementById("primary_plan_type").value !='')
		return 'P';

	if (document.getElementById("secondary_plan_type") != null
			&& document.getElementById("secondary_plan_type").value !='')
		return 'S';

	if (document.getElementById("primary_insurance_co") != null
			&& document.getElementById("primary_insurance_co").value !='')
		return 'P';

	if (document.getElementById("secondary_insurance_co") != null
			&& document.getElementById("secondary_insurance_co").value !='')
		return 'S';

	if (document.getElementById("primary_sponsor_id") != null
			&& document.getElementById("primary_sponsor_id").value !='')
		return 'P';

	if (document.getElementById("secondary_sponsor_id") != null
			&& document.getElementById("secondary_sponsor_id").value !='')
		return 'S';

	if (document.getElementById("primary_sponsor") != null
			&& document.getElementById("primary_sponsor").value == 'I')
		return 'P';

	if (document.getElementById("secondary_sponsor") != null
			&& document.getElementById("secondary_sponsor").value == 'I')
		return 'S';

	if (document.getElementById("primary_sponsor") != null
			&& document.getElementById("primary_sponsor").value == 'C')
		return 'P';

	if (document.getElementById("primary_sponsor") != null
			&& document.getElementById("primary_sponsor").value == 'N')
		return 'P';
}

// Patient category -- primary, secondary default sponsor types and bill type default
function setPrimarySecondarySponsor() {

	var visitType = screenid == 'ip_registration'? 'i': 'o';
	var patientCategoryObj	= document.mainform.patient_category_id;
	var patientCategory		= "";

	if (patientCategoryObj != null) {
		patientCategory	= patientCategoryObj.value;
		if (gSelectedPatientCategory == patientCategory) return;
	}

	var category = findInList(categoryJSON, "category_id", patientCategory);

	var defaultPrimaryTpa = '';
	var defaultSecondaryTpa = '';

	if (visitType == 'i') {
		defaultPrimaryTpa = !empty(category) ? category.primary_ip_sponsor_id : '';
		defaultSecondaryTpa = !empty(category) ? category.secondary_ip_sponsor_id : '';
	}else if (visitType == 'o') {
		defaultPrimaryTpa = !empty(category) ? category.primary_op_sponsor_id : '';
		defaultSecondaryTpa = !empty(category) ? category.secondary_op_sponsor_id : '';
	}

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var primarySponsorWrapperObj = document.getElementById("primary_sponsor_wrapper");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");

	if (primarySponsorObj != null) {
		if (!empty(defaultPrimaryTpa)) {
			var tpa = findInList(tpanames, "tpa_id", defaultPrimaryTpa);
			var spnsrType = !empty(tpa) ? tpa.sponsor_type : "";

			if (document.mainform.bill_type) {
				if (!empty(spnsrType) && visitType == 'o' && allowBillNowInsurance == 'false')
					setSelectedIndex(document.mainform.bill_type, "C");
				else {
					if (visitType == 'o') setSelectedIndex(document.mainform.bill_type, defaultOpBillType);
					else setSelectedIndex(document.mainform.bill_type, defaultIpBillType);
				}
			}

			if (!empty(spnsrType) && document.mainform.bill_type &&
				(document.mainform.bill_type.value == "C"
				|| (allowBillNowInsurance == 'true' && document.mainform.bill_type.value == 'P'))) {
				primarySponsorObj.value = spnsrType;
			}else {
				primarySponsorObj.value = '';
				primarySponsorWrapperObj.checked = false;
			}

		}else {
			primarySponsorObj.value = '';
			primarySponsorWrapperObj.checked = false;
			if (document.mainform.bill_type) {
				if (visitType == 'o') setSelectedIndex(document.mainform.bill_type, defaultOpBillType);
				else setSelectedIndex(document.mainform.bill_type, defaultIpBillType);
			}
		}
	}

	if (secondarySponsorObj != null) {
		if (primarySponsorObj != null && primarySponsorObj.value != '') {
			if (!empty(defaultSecondaryTpa)) {
				var tpa = findInList(tpanames, "tpa_id", defaultSecondaryTpa);
				var spnsrType = !empty(tpa) ? tpa.sponsor_type : "";

				if (!empty(spnsrType) && document.mainform.bill_type &&
						(document.mainform.bill_type.value == "C"
						|| (allowBillNowInsurance == 'true' && document.mainform.bill_type.value == 'P'))) {
					secondarySponsorObj.value = spnsrType;
					secondarySponsorWrapperObj.disabled = false;

				}else {
					secondarySponsorObj.value = '';
					secondarySponsorWrapperObj.checked = false;
				}

			}else {
				secondarySponsorObj.value = '';
				secondarySponsorWrapperObj.checked = false;
			}
		}else {
			secondarySponsorObj.value = '';
			secondarySponsorWrapperObj.disabled = true;
		}
	}
}

//on Load category change function called
var gPatientCategoryChanged = false;

// Function called when Patient Category is changed in UI
//var gPatientCategoryChanged = false;
function onChangeCategory() {

	var patientCategoryObj	= document.mainform.patient_category_id;
	var patientCategory		= "";

	if (patientCategoryObj != null) {
		patientCategory	= patientCategoryObj.value;
		if (gSelectedPatientCategory == patientCategory) return;
	}

	setPrimarySecondarySponsor();
	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");

	if (primarySponsorObj != null) resetPrimarySponsorChange();
	if (secondarySponsorObj != null) resetSecondarySponsorChange();

	if (screenid != "out_pat_reg")
		onBillTypeChange();

	var visitType = screenid == 'ip_registration'? 'i': 'o';
  if (visitType === 'i') {
           var categoryId = document.getElementById('patient_category_id').value;
           var category = findInList(categoryJSON,'category_id',categoryId);
           if(category!==null && category.primary_ip_sponsor_id !== null){
                    document.getElementById("primary_sponsor_wrapper").checked = true
                    document.getElementById("primary_sponsor").checked ='I'
                    onChangePrimarySponsor();
                    var sponsorDetailsObject = getSponsorDetails(category.primary_ip_sponsor_id,categoryId);
                    primarySponsorDetails = sponsorDetailsObject.insurance_sponsor_details;
                    document.getElementById('primary_sponsor_name').value = primarySponsorDetails.tpa_name;
                    document.getElementById('primary_sponsor_id').value = primarySponsorDetails.tpa_id;
                    loadRegistrationInsuCompanyDetails('P');
                    var insuranceComp=findInList(primarySponsorDetails.insurance_companies,'insurance_co_id',category.primary_ip_insurance_co_id);
                    if(insuranceComp !== null){
                        var primaryInsuranceObj = getPrimaryInsuObj();
                        setSelectedIndex(primaryInsuranceObj,insuranceComp.insurance_co_id);
                    }
                    onInsuranceCompanyChange('P');
            }
            var isDisabled = document.getElementById("secondary_sponsor").disabled;
            if(!isDisabled && category!==null && category.secondary_ip_sponsor_id !== null){
                       document.getElementById("secondary_sponsor_wrapper").checked = true
                       document.getElementById("secondary_sponsor").checked ='I'
                       onChangeSecondarySponsor();
                          var sponsorDetailsObject = getSponsorDetails(category.secondary_ip_sponsor_id,categoryId);
                          secondarySponsorDetails = sponsorDetailsObject.insurance_sponsor_details;
                          document.getElementById('secondary_sponsor_name').value = secondarySponsorDetails.tpa_name;
                          document.getElementById('secondary_sponsor_id').value = secondarySponsorDetails.tpa_id;
                          loadRegistrationInsuCompanyDetails('S');
                          var insuranceComp=findInList(secondarySponsorDetails.insurance_companies,'insurance_co_id',category.secondary_ip_insurance_co_id);
                          if(insuranceComp !== null){
                             var secondaryInsuranceObj = getSecondaryInsuObj();
                             setSelectedIndex(secondaryInsuranceObj,insuranceComp.insurance_co_id);
                          }
                          onInsuranceCompanyChange('S');
             }
      }
	//setAllDefaults();

	if (document.mainform.op_type != null
			&& (document.mainform.op_type.value == "F" || document.mainform.op_type.value == "D"))
		loadInsurancePolicyDetails();
	changeVisitType();
	estimateTotalAmount();

	showHideCaseFile();
	//ratePlanChange();
	isCategoryChanged();
	setSchedulerPriorAuthDetails();
	setRegistrationChargesFlag(patientCategoryObj);
	checkPassportAsteriskRequired();
/*	if(corpInsuranceCheck == 'Y')
		setSelectedDateForCorpInsurance(); */
}

function setRegistrationChargesFlag(patientCategoryObj) {

}

function checkUncheckCheckbox(obj) {
	var totalEstimatedAmt = parseFloat(document.getElementById('estimtAmount').textContent);
	if(obj.checked) {
		document.getElementById('apply_registration_charges').value = "Y";
		document.getElementById('estimtAmount').textContent = formatAmountPaise(getPaise(totalEstimatedAmt)+getPaise(totalRegistrationCharges));
		document.getElementById('estimateAmount').value = formatAmountPaise(getPaise(totalEstimatedAmt)+getPaise(totalRegistrationCharges));
	} else {
		document.getElementById('apply_registration_charges').value = "N";
		document.getElementById('estimtAmount').textContent = formatAmountPaise(getPaise(totalEstimatedAmt)-getPaise(totalRegistrationCharges));
		document.getElementById('estimateAmount').value = formatAmountPaise(getPaise(totalEstimatedAmt)-getPaise(totalRegistrationCharges));
	}
}

function isCategoryChanged() {
	var spnsrIndex = getMainSponsorIndex();

	var tpaObj = null;
	var planObj= null;

	if (spnsrIndex == 'P') {
		tpaObj = getPrimarySponsorObj();
		planObj= getPrimaryPlanObj();
	}else if (spnsrIndex == 'S') {
		tpaObj = getSecondarySponsorObj();
		planObj= getSecondaryPlanObj();
	}
	var tpaId = "";
	if (tpaObj != null) tpaId = tpaObj.value;

	var plan 	= "";
	if (planObj != null) plan = planObj.value;

	var orgId = document.mainform.organization.value;

	if (gPatientCategoryChanged)
		resetOrderDialogRatePlanInsurance(orgId, tpaId, plan, true);
}

function populateMLCTemplates() {
	var isMlc = document.mrnoform.mlccheck.checked;
	if (isMlc) {
		document.mainform.patientMlcStatus.value = 'Y';
		document.mrnoform.mlcBtn.disabled = false;
		showMlcDialog();
	} else {
		document.mainform.patientMlcStatus.value = 'N';
		document.mrnoform.mlcBtn.disabled = true;
	}
	//estimateTotalAmount();
}

function setValueInAnotherForm(formName, elName, obj, defaultValue) {
	if (obj.type == 'checkbox') {
		var mForm = document.forms[formName];
		if (obj.checked) mForm[elName].value = obj.value;
		else mForm[elName].value = defaultValue;
	}
}

function onChangeMLCDoc() {
	document.mainform.mlc_template_id.value = document.mainform.mlc_template.options[document.mainform.mlc_template.options.selectedIndex].value;
	document.mainform.mlc_template_name.value = document.mainform.mlc_template.options[document.mainform.mlc_template.options.selectedIndex].text;
}

//for validation... As sometimes the number of characters may exceed the max-length specified

function limitText(limitField, limitNum) {
	if (limitField.value.length > limitNum) {
		alert(getString("js.registration.patient.sorry.a.maximum.description.of.only.string")+" " + limitNum + " "+getString("js.registration.patient.characters.can.be.entered.string"));
		limitField.value = limitField.value.substring(0, limitNum);
	}
}

String.prototype.startsWith = function (str){
   	return this.slice(0, str.length) == str;
};

var mlcDialog = null;

function initMlcDialog() {
	var dialog = document.getElementById('mlcFieldsDialog');
	dialog.style.display = 'block';
	mlcDialog = new YAHOO.widget.Dialog("mlcFieldsDialog", {
		width: "760px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	YAHOO.util.Event.addListener("mlcFieldsOkBtn", "click", validateMlcFields, mlcDialog, true);
	subscribeKeyListeners(mlcDialog, 'mlc');
	mlcDialog.render();
}

function showMlcDialog() {
	var obj = document.getElementById("openmlc");
	if (mlcDialog != null) {
		mlcDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
		mlcDialog.show();
	}
	return false;
}

function validateMlcFields() {
	if (document.mainform.mlc_template.selectedIndex <= 0) {
		showMessage("js.registration.patient.mlc.template.required");
		document.mainform.mlc_template.focus();
		return false;
	}

	if (mlcDialog != null) mlcDialog.hide();
	return true;
}

var cfDialog = null;
var vcfDialog = null;

function customDialog() {
	var dialog = document.getElementById('customFieldsDialog');
	dialog.style.display = 'block';
	cfDialog = new YAHOO.widget.Dialog("customFieldsDialog", {
		width: "760px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	YAHOO.util.Event.addListener("customFieldsOkBtn", "click", validateSecondaryCustomFields, cfDialog, true);
	subscribeKeyListeners(cfDialog, 'custom');
	cfDialog.render();
}

function visitCustomDialog() {
	var dialog = document.getElementById('visitCustomFieldsDialog');
	dialog.style.display = 'block';
	vcfDialog = new YAHOO.widget.Dialog("visitCustomFieldsDialog", {
		width: "760px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	YAHOO.util.Event.addListener("visitCustomFieldsOkBtn", "click", validateSecondaryVisitCustomFields, vcfDialog, true);
	subscribeKeyListeners(vcfDialog, 'visit_custom');
	vcfDialog.render();
}

function subscribeKeyListeners(dialog, type) {
	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: closeDialog,
		scope: dialog,
		correctScope: true
	});

	// Alt+Shift+K
	if (type == 'mlc') {
		var okButtonListener = new YAHOO.util.KeyListener(document, {
			alt: true,
			shift: true,
			keys: 75
		}, {
			fn: validateMlcFields,
			scope: dialog,
			correctScope: true
		});
		dialog.cfg.setProperty("keylisteners", [escKeyListener, okButtonListener]);
	} else if (type == 'custom') {
		var okButtonListener = new YAHOO.util.KeyListener(document, {
			alt: true,
			shift: true,
			keys: 75
		}, {
			fn: validateSecondaryCustomFields,
			scope: dialog,
			correctScope: true
		});
		dialog.cfg.setProperty("keylisteners", [escKeyListener, okButtonListener]);
	} else if (type == 'visit_custom') {
		var okButtonListener = new YAHOO.util.KeyListener(document, {
			alt: true,
			shift: true,
			keys: 75
		}, {
			fn: validateSecondaryVisitCustomFields,
			scope: dialog,
			correctScope: true
		});
		dialog.cfg.setProperty("keylisteners", [escKeyListener, okButtonListener]);
	}
}

function showCustomDialog(obj) {
	var row = getThisRow(obj);
	if (cfDialog != null) {
		cfDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
		cfDialog.show();
	}
	return false;
}

function showVisitCustomDialog(obj) {
	var row = getThisRow(obj);
	if (vcfDialog != null) {
		vcfDialog.cfg.setProperty("context", [obj, "tl", "tr"], false);
		vcfDialog.show();
	}
	return false;
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function closeDialog() {
	this.cancel();
}

/*function validateVisitCustomFields() {
	if (screenid == "ip_registration") {
		var visitType = "I";
	} else {
		var visitType = "O";
	}
	if (visitFieldsList.length > 0) {
		for (var i = 0; i < visitFieldsList.length; i++) {
			var custfieldObj = customVisitFieldValidation(visitType, visitFieldsList[i]);
			if (custfieldObj != null) {
				if (VisitCollapsiblePanel1.isOpen()) {
					setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
				} else {
					VisitCollapsiblePanel1.open();
					setTimeout("document.mainform." + custfieldObj + ".focus()", 800);
				}
				return false;
			}
		}
	}
	return true;
}*/

function validatePrimaryCustomFields() {
	if (screenid == "ip_registration") {
		var visitType = "I";
	} else {
		var visitType = "O";
	}
	if (mainPagefieldsList.length > 0) {
		for (var i = 0; i < mainPagefieldsList.length; i++) {
			var custfieldObj = customFieldValidation(visitType, mainPagefieldsList[i]);
			if (custfieldObj != null) {
				if (CollapsiblePanel1.isOpen()) {
					setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
				} else {
					CollapsiblePanel1.open();
					setTimeout("document.mainform." + custfieldObj + ".focus()", 800);
				}
				cfDialog.hide();
				return false;
			}
		}
	}
	if (cfDialog != null) cfDialog.hide();
	return true;
}

function validatePrimaryVisitCustomFields() {
	if (screenid == "ip_registration") {
		var visitType = "I";
	} else {
		var visitType = "O";
	}
	if (mainPageVisitfieldsList.length > 0) {
		for (var i = 0; i < mainPageVisitfieldsList.length; i++) {
			var custfieldObj = customVisitFieldValidation(visitType, mainPageVisitfieldsList[i]);
			if (custfieldObj != null) {
				if (VisitCollapsiblePanel1.isOpen()) {
					setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
				} else {
					VisitCollapsiblePanel1.open();
					setTimeout("document.mainform." + custfieldObj + ".focus()", 800);
				}
				vcfDialog.hide();
				return false;
			}
		}
	}
	if (vcfDialog != null) vcfDialog.hide();
	return true;
}


function validateSecondaryCustomFields() {
	if (screenid == "ip_registration") {
		var visitType = "I";
	} else {
		var visitType = "O";
	}
	if (dialogFieldsList.length > 0) {
		for (var i = 0; i < dialogFieldsList.length; i++) {
			var custfieldObj = customFieldValidation(visitType, dialogFieldsList[i]);
			if (custfieldObj != null) {
				if (!CollapsiblePanel1.isOpen()) CollapsiblePanel1.open();
				cfDialog.cfg.setProperty("context", [document.getElementById('btnCustomFields'), "tr", "tl"], false);
				cfDialog.show();
				setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
				return false;
			}
		}
	}
	if (cfDialog != null) cfDialog.hide();
	return true;
}

function validateSecondaryVisitCustomFields() {
	if (screenid == "ip_registration") {
		var visitType = "I";
	} else {
		var visitType = "O";
	}
	if (dialogVisitFieldsList.length > 0) {
		for (var i = 0; i < dialogVisitFieldsList.length; i++) {
			var custfieldObj = customVisitFieldValidation(visitType, dialogVisitFieldsList[i]);
			if (custfieldObj != null) {
				if (!VisitCollapsiblePanel1.isOpen()) VisitCollapsiblePanel1.open();
				vcfDialog.cfg.setProperty("context", [document.getElementById('btnVisitCustomFields'), "tl", "tr"], false);
				vcfDialog.show();
				setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
				return false;
			}
		}
	}
	if (vcfDialog != null) vcfDialog.hide();
	return true;
}

function validateCustomFields() {
	if (!validatePrimaryCustomFields()) return false;
	if (!validateSecondaryCustomFields()) return false;
	if (!validatePrimaryVisitCustomFields()) return false;
	if (!validateSecondaryVisitCustomFields()) return false;
	return true;
}

function openPatientBill() {
	if (patientBilPopUp == "Y") {
		window.open("../../billing/BillAction.do?_method=getCreditBillingCollectScreen&billNo=" + billNo);
	}
}

// function called by order dialog when user clicks Add in the order dialog.

function addOrders(order) {
	order.planIds = getPlanIds();
	var type = order.itemType;
	if (type == 'Laboratory' || type == 'Radiology') {
		index = addInvestigations(order);
	} else if (type == 'Service') {
		index = addServices(order);
	} else if (type == 'Other Charge') {
		index = addOtherServices(order, "OCOTC");
	} else if (type == 'Implant') {
		index = addOtherServices(order, "IMPOTC");
	} else if (type == 'Consumable') {
		index = addOtherServices(order, "CONOTC");
	} else if (type == 'Doctor') {
		index = addDoctor(order);
	} else if (type == 'Equipment') {
		index = addEquipment(order);
	} else if (type == 'Package') {
		index = addPackages(order);
	} else if (type == 'Operation') {
		index = infoOfOrder(order);
	}

	getBillChargeClaims("new_visit", document.mainform);

	estimateTotalAmount();
	return true;
}


function getBillChargeClaims(visitID, form){

	var formToken = form._insta_transaction_token.value;
	form._insta_transaction_token.value="";

	YAHOO.util.Connect.setForm(form);

	var url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getBillChargeClaimsForOrderItems&visitID='+visitID+'&regScreen=Y&gPreviousPlan='+gPreviousPlan+'&gPreviousSecPlan='+gPreviousSecPlan;
	var ajaxRequestForBillChargeClaims = YAHOO.util.Connect.asyncRequest('POST', url,
			{
				success: OnGetBillChargeClaims,
				failure: OnGetBillChargeClaimsFailure,
				argument: [form, formToken]
			}
		)
}

function OnGetBillChargeClaims(response){

	if (response.responseText != undefined) {
		var planMap = eval('(' + response.responseText + ')');
		var planIds = getPlanIds();
		for(var j=0; j<planIds.length; j++){
			var planId = planIds[j];
			var billChgClaimMap = planMap[planId];
			setSponsorAmounts(billChgClaimMap, j+1);
		}
	}
	var multiPlanExists = planIds.length > 1;
	setPatientAmounts(multiPlanExists);
	var args = response.argument;
	if (null != args && args.length >1) {
		args[0]._insta_transaction_token.value = args[1];
	}

}

function OnGetBillChargeClaimsFailure(){

}


function setSponsorAmounts(billChgClaimMap, priority){

	var table = document.getElementById('orderTable'+0);
	var numRows = table.rows.length;

	for (var id=1; id < numRows-1 ;id++) {
		var row = table.rows[id];
		var chargeId = null;
		chargeId = "_"+id;
		if(billChgClaimMap != undefined && billChgClaimMap[chargeId] != undefined){
			var insClaimAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].insurance_claim_amt));
			if(priority == 1){
				getElementByName(row, 'priClaimAmt').value = insClaimAmt;
			}else{
				getElementByName(row, 'secClaimAmt').value = insClaimAmt;
			}
		}
	}

	if(billChgClaimMap != undefined && billChgClaimMap["_0"] != undefined){
		var insClaimAmt = formatAmountPaise(getPaise(billChgClaimMap["_0"].insurance_claim_amt));
		if(priority == 1){
			document.getElementById("doc_priClaimAmt").value = insClaimAmt;
		}else{
			document.getElementById("doc_secClaimAmt").value = insClaimAmt;
		}
	}

}


function setPatientAmounts(multiPlanExists){
	var table = document.getElementById('orderTable'+0);
	var numRows = table.rows.length;

	for (var id=1; id < numRows-1 ;id++) {

		var row = table.rows[id];
		var insClaimAmt = getElementByName(row, 'priClaimAmt').value;
		var priClaimAmtPaise = getPaise(insClaimAmt);
		var insClaimAmtPaise = priClaimAmtPaise;

		if(multiPlanExists){
			var secClaimAmtPaise = getPaise(getElementByName(row, 'secClaimAmt').value);
			insClaimAmtPaise = insClaimAmtPaise + secClaimAmtPaise;
		}
		var amtPaise = getPaise(getElementByName(row, 'orderAmount').value);
		var patAmt = amtPaise - insClaimAmtPaise;

		setNodeText(row.cells[PATIENT_AMT_COL], formatAmountPaise(patAmt));
		getElementByName(row, 'orderPatientAmt').value = formatAmountPaise(patAmt);

	}

	if (document.getElementById('patietConsultAmount')) {

		var docAmtPaise = getPaise(document.getElementById("doc_amount").value);
		var docPriClaimAmtPaise = getPaise(document.getElementById("doc_priClaimAmt").value);
		var docSecClaimAmtPaise = 0;
		var insClaimAmtPaise = docPriClaimAmtPaise;
		if(multiPlanExists){
			docSecClaimAmtPaise = getPaise(document.getElementById("doc_secClaimAmt").value);
			insClaimAmtPaise = insClaimAmtPaise + docSecClaimAmtPaise;
		}
		var patAmt = docAmtPaise - insClaimAmtPaise;

		document.getElementById('patietConsultAmount').value = formatAmountPaise(patAmt);
	}
	estimateTotalAmount();
}

function getPlanIds(){
	var primaryPlanId = document.getElementById("primary_plan_id");
	var secPlanId = document.getElementById("secondary_plan_id");
	var noofPlans = 0;
	var priPlanExists = false;
	var secPlanExists = false;
	if(primaryPlanId != null && primaryPlanId.value != '') {
		priPlanExists = true;
		noofPlans++;
	}
	if(secPlanId != null && secPlanId.value != '') {
		secPlanExists = true;
		noofPlans++;
	}
	var planIds = [];
	planIds.length = noofPlans;

	if(priPlanExists && secPlanExists) {
		planIds[0] = primaryPlanId.value;
		planIds[1] = secPlanId.value;
	}else if(priPlanExists) {
		planIds[0] = primaryPlanId.value;
	}else if(secPlanExists) {
		planIds[0] = secPlanId.value;
	}
	return planIds;
}


function infoOfOrder(order) {
	showMessage("js.registration.patient.operations.cannot.be.ordered.use.order.screen.string");
	return false;
}

function cancelOrder(imgObj) {
	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	if (getElementByName(row, "firstOfCategory").value == "true" && gIsInsurance)
		alert(getString("js.registration.patient.deleting.the.first.item.of.insurance.category.string")+"\n" +
			 getString("js.registration.patient.insurance.patient.co.pay.fixed.amount.may.need.to.be.adjusted.string")+"\n" +
			getString("js.registration.patient.for.additional.charges.of.the.same.category.string"));
	clearDocVisitForPack(row); // clear the doctor visits created if it is a package and contains doctor visits or conducting doctors for services in a package.
	deleteTestAdditionalDocs(row);
	row.parentNode.removeChild(row);
	getBillChargeClaims("new_visit", document.mainform);
	estimateTotalAmount();
}

function clearDocVisitForPack(row) {
	var packIdEl = getElementByName(row, 'package.packageId');
	if (!empty(packIdEl)) {
		var packId = packIdEl.value;
		var prescId = getElementByName(row, 'package.prescId').value;
		var packIdDocEl = document.getElementsByName('package.packIdFordoc');
		var prescIdDocEl = document.getElementsByName('package.packPrescIdFordoc');
		var rowIndexEl = document.getElementsByName('package.mainRowIndex');

		var rowArray = new Array();
		var docVisitTable = document.getElementById('innerDocVisitForPack');
		for (var i=0; i<packIdDocEl.length; i++) {
			if (packIdDocEl[i].value == packId && prescIdDocEl[i].value == prescId && parseInt(rowIndexEl[i].value) == row.rowIndex) {
				// we should not delete the row here itself, if we do that, i value will vary.
				rowArray.push(findAncestor(packIdDocEl, 'TR'));
			}
		}
		for (var i=0; i<rowArray.length; i++) {
			docVisitTable.deleteRow(rowArray[i]);
		}

		var packIdCondDocEl = document.getElementsByName('package.packIdForCondDoc');
		var prescIdCondDocEl = document.getElementsByName('package.packPrescIdForCondDoc');
		var rowIndexCondEl = document.getElementsByName('package.mainRowIndex');

		var condRowArray = new Array();
		var condDocTable = document.getElementById('innerCondDocForPack');
		for (var i=0; i<packIdCondDocEl.length; i++) {
			if (packIdCondDocEl[i].value == packId && prescIdCondDocEl[i].value == prescId) {
				// we should not delete the row here itself, if we do that, i value will vary.
				condRowArray.push(findAncestor(packIdCondDocEl, 'TR'));
			}
		}
		for (var i=0; i<condRowArray.length; i++) {
			condDocTable.deleteRow(condRowArray[i]);
		}
	}
}

// Function called when Plan type is changed in UI

function onInsuCatChange(spnsrIndex) {
	insuCatChange(spnsrIndex);
	RatePlanList();
	ratePlanChange();
}

// Function called when Insurance company is changed in UI (loadTpaList())
// (or) existing patient details are loaded (loadInsurancePolicyDetails())
// (or) Member ship autocomplete is changed (loadPolicyDetails)

function insuCatChange(spnsrIndex) {

	var insApprovalAmtObj = null;
	var planObj = null;
	var insCompObj = null;
	var planTypeObj = null;
	var tpaobj=null;

	if (spnsrIndex == 'P') {
		insApprovalAmtObj = getPrimaryApprovalLimitObj();
		planObj = getPrimaryPlanObj();
		insCompObj = getPrimaryInsuObj();
		planTypeObj = getPrimaryPlanTypeObj();
		tpaobj= getPrimarySponsorObj();

	}else if (spnsrIndex == 'S') {
		insApprovalAmtObj = getSecondaryApprovalLimitObj();
		planObj = getSecondaryPlanObj();
		insCompObj = getSecondaryInsuObj();
		planTypeObj = getSecondaryPlanTypeObj();
		tpaobj=getSecondarySponsorObj();

	}

	if (insApprovalAmtObj) insApprovalAmtObj.value = "";

	// if insurance co and plan type are selected bring policyNames via ajax
	if (insCompObj != null && insCompObj.value != ""
		&& planTypeObj != null && planTypeObj.value != "") {
		policynames = mergePolicyNames(policynames, getPolicyNames(spnsrIndex));
	}
	if (planObj != null && policynames != null) {

		var selectedInsId = insCompObj.value;
		var selectedCatId = planTypeObj.value;
		var policySelect = planObj;

		// Empty plans
		var len = 1;
		var policyDefault = "";
		var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
		policySelect.options.length = len;
		policySelect.options[len - 1] = optn;
		for (var k = 0; k < policynames.length; k++) {
			var ele = policynames[k];
			if (screenid == "out_pat_reg") {
				if (ele.insurance_co_id == selectedInsId && ele.category_id == selectedCatId && ele.status == "A" && ele.op_applicable == "Y"
					&& (empty(ele.sponsor_id) ||ele.sponsor_id == tpaobj.value)) {
					var optn = new Option(ele.plan_name, ele.plan_id);
					len++;
					policySelect.options.length = len;
					policySelect.options[len - 1] = optn;
					policyDefault = ele.plan_id;
				}
			} else {
				if (ele.insurance_co_id == selectedInsId && ele.category_id == selectedCatId && ele.status == "A" && ele.ip_applicable == "Y"
					&& (empty(ele.sponsor_id) ||ele.sponsor_id == tpaobj.value)) {
					var optn = new Option(ele.plan_name, ele.plan_id);
					len++;
					policySelect.options.length = len;
					policySelect.options[len - 1] = optn;
					policyDefault = ele.plan_id;
				}
			}
		}

		if (policySelect.options.length == 2) {
			setSelectedIndex(policySelect, policyDefault);
		}

		sortDropDown(planObj);
		policyChange(spnsrIndex);
	}
}

function showPatientAmountsColumns(show) {
	// show patient amounts in order table
	showOrderTablePatientAmounts(0, show);

	// show patient amount totals
	var patientAmountRow = document.getElementById("patientAmounts");
	if (patientAmountRow) {
		if (show) patientAmountRow.style.display = '';
		else patientAmountRow.style.display = 'none';
	}
}

function uploadForms() {
	var docSelected = false;
	var tpaIdObj = getPrimarySponsorObj();
	if (document.getElementById("docTable")) {
		var table = document.getElementById("docTable");
		// First row -- Insurance card upload
		// From second row the docs required in docs_upload, hence check if rows > 1
		var totalNoOfRows = table.rows.length;
		if (totalNoOfRows > 0) {
			for (var i = 1; i <= totalNoOfRows; i++) {
				if (document.getElementById('mandatory' + i).value == "A"
								&& document.getElementById('doc_content_bytea' + i).value == "") {
					alert(getString("js.registration.patient.upload.document.required")+" " + document.getElementById('doc_name' + i).value);
					document.getElementById('doc_content_bytea' + i).focus();
					return false;
				}
				if (document.getElementById('mandatory' + i).value == "I"
								&& document.getElementById('doc_content_bytea' + i).value == "" && screenid == "ip_registration") {
					alert(getString("js.registration.patient.upload.document.required")+" " + document.getElementById('doc_name' + i).value);
					document.getElementById('doc_content_bytea' + i).focus();
					return false;
				}
				if (document.getElementById('mandatory' + i).value == "S"
								&& document.getElementById('doc_content_bytea' + i).value == "" && screenid == "out_pat_reg") {
					alert(getString("js.registration.patient.upload.document.required")+" " + document.getElementById('doc_name' + i).value);
					document.getElementById('doc_content_bytea' + i).focus();
					return false;
				}
				if (document.getElementById('mandatory' + i).value == "P"
					&& document.getElementById('doc_content_bytea' + i).value == ""
					&& tpaIdObj && tpaIdObj.value != ""
					&& (document.mainform.op_type == null || document.mainform.op_type.value == "M")) {

					alert(getString("js.registration.patient.upload.document.required")+" " + document.getElementById('doc_name' + i).value);
					document.getElementById('doc_content_bytea' + i).focus();
					return false;
				}
			}
		}
	}
	return true;
}

var babyMemeberId;
function validateBabyAge(mrNo,visitId,sponsorObj,memberId) {
	babyMemeberId = memberId;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/pages/registration/regUtils.do?_method=getBabyDOBAndMemberIdValidityDetails&mr_no=" + mrNo +
						"&visit_id=" + visitId+"&sponsor_type="+sponsorObj.value;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return babyDetailsHandaler(ajaxobj.responseText);
		}
	}

	return true;
}


function babyDetailsHandaler(responseText) {
	var memberId = babyMemeberId;
	eval("babyInfo =" + responseText);
	if (babyInfo != null) {
		var babyDetails = babyInfo.babyDetails;
		var babyVisitDetails = babyInfo.babyVisitDetails;
		var helthAuthPrefs =  babyInfo.helathAuthPrefs;
		var parentMemberId = babyInfo.member_id;
		if(babyDetails != null && helthAuthPrefs != null) {
			var salutation = babyDetails.salutation;
			salutation = salutation.toUpperCase();
			if(salutation == 'BABY' && memberId == parentMemberId) {
				var dobInMillis = babyDetails.dateofbirth;
				var child_mother_ins_member_validity_days = helthAuthPrefs.child_mother_ins_member_validity_days;
				var dob = new Date(dobInMillis);
				var serverDate = new Date();
				var diffInDays = (serverDate - dob)/ 60 / 60 / 24 / 1000;
				if(!empty(child_mother_ins_member_validity_days) && diffInDays < child_mother_ins_member_validity_days) {
					return false;
				}
			}
		}
	}
	return true;
}

function checkForMemberID(spnsrIndex) {
	var memberIdObj = null;
	var insuCompObj = null;
	var sponsorObj = null;
	var memberIdLabel=null;

	if (spnsrIndex == 'P') {
		memberIdObj = getPrimaryInsuranceMemberIdObj();
		insuCompObj = getPrimaryInsuObj();
		sponsorObj = getPrimarySponsorTypeObj();
		memberIdLabel=document.getElementById("primary_member_id_label").innerHTML;
		var tpaId = document.getElementById("primary_sponsor_id").value;
		/*var tpa = findInList(tpanames, "tpa_id", tpaId);

		if(tpa != null)
			var memberIdPattern = tpa.member_id_pattern;
			*/
		if(!empty(primarySponsorDetails))
       var memberIdPattern = primarySponsorDetails.member_id_pattern;
	} else if (spnsrIndex == 'S') {
		memberIdObj = getSecondaryInsuranceMemberIdObj();
		insuCompObj = getSecondaryInsuObj();
		sponsorObj = getSecondarySponsorTypeObj();
		memberIdLabel=document.getElementById("secondary_member_id_label").innerHTML;
		var tpaId = document.getElementById("secondary_sponsor_id").value;
	/*	var tpa = findInList(tpanames, "tpa_id", tpaId);
		if(tpa != null)
			var memberIdPattern =  tpa.member_id_pattern;*/
    if(!empty(secondarySponsorDetails))
    	  var memberIdPattern =  secondarySponsorDetails.member_id_pattern;
	}
	memberIdLabel=memberIdLabel.replace(':','');
	if (memberIdObj != null) {
		if(!compareMemberIdAndPattern(memberIdObj, memberIdPattern, memberIdLabel))
			return false;

		var memberId = trimAll(memberIdObj.value);
		var mrNo = document.mrnoform.mrno.value;

		if (!empty(memberId)) {
			var companyId = insuCompObj.value;

			var ajaxobj = newXMLHttpRequest();
			var url = cpath + `/patients/tpaMemberCheck.json?member_id=${encodeURIComponent(memberId)}&tpa_id=${tpaId}&exclude_mr_no=${mrNo}`;
			ajaxobj.open("GET", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						var exists =  JSON.parse(ajaxobj.responseText);
						if (exists && exists.parent_child_mr_nos && exists.parent_child_mr_nos.length > 0) {
							let validation_type_text = "";
							let tpa_select_index = 0;
							let mrNos = exists.parent_child_mr_nos;
							let memIdValidType;
              if(spnsrIndex === "P"){
              			memIdValidType == primarySponsorDetails.tpa_member_id_validation_type;
              } else {
              			memIdValidType == secondarySponsorDetails.tpa_member_id_validation_type;
              }
							/*tpanames.map((tpaname, tpanameIndex) => {
							if(tpaname.tpa_id === tpaId)
									tpa_select_index = tpanameIndex;
							});
							if(tpanames && tpanames[tpa_select_index]){
								let currentTpa = tpanames[tpa_select_index];
								let memIdValidType = currentTpa.tpa_member_id_validation_type;*/
							 if(memIdValidType) {

								if(memIdValidType === "A"){
									return true;
								}else if(memIdValidType === "B"){
									alert(
											getString('js.common.message.block') + ': ' +
											getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
										);
									memberIdObj.value = "";
									memberIdObj.focus();
									return false;
								}else if(memIdValidType === "W"){
									alert(
											getString('js.common.message.warn') + ': ' +
											getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
										);
									return true;
								}else if(memIdValidType === "C" && mrNo != null && mrNo != ""){
									const not_parent_child = mrNos.filter(item => item.is_parent_child === false);
									if (not_parent_child && not_parent_child.length) {
										alert(
												getString('js.common.message.block') + ': ' +
												getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
											);
										memberIdObj.value = "";
										memberIdObj.focus();
										return false;
									}
									const is_parent_mr_no = mrNos.filter(item => item.is_parent_mr_no === true);
									if(is_parent_mr_no && is_parent_mr_no.length ){
										if(document.mainform.ageIn.value === 'D' && document.mainform.age.value <= currentTpa.child_dup_memb_id_validity_days){
											return true;
										}else{
											alert(
													getString('js.common.message.validaity.count.over') + ': ' +
													getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
												);
											memberIdObj.value = "";
											memberIdObj.focus();
											return false;
										}
									}else{
										return true;
									}
								}else{
									alert(
											getString('js.common.message.block') + ': ' +
											getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
										);
									memberIdObj.value = "";
									memberIdObj.focus();
									return false;
								}
							}
 						}else
 							return true;
 					}
 				}
 			}
 		}
 	}
 	return true;
 }

function hotKeys() {
/** which will adds the keylistener for the document(for key Alt + Shift +  I).
	  toggles the Additional Information Collapsible Panel. */

	var addInfoKeyListener = new YAHOO.util.KeyListener(document, {
		alt: true,
		shift: true,
		keys: 73
	}, {
		fn: toggleCollapsiblePanel,
		scope: CollapsiblePanel1,
		correctScope: true
	});
	addInfoKeyListener.enable();

	/** Keylistener for the Additional Information Collapsible Panel.(key Alt + Shift + N) */
	var addVisitInfoKeyListener = new YAHOO.util.KeyListener(document, {
		alt: true,
		shift: true,
		keys: 78
	}, {
		fn: toggleVisitCollapsiblePanel,
		scope: VisitCollapsiblePanel1,
		correctScope: true
	});
	addVisitInfoKeyListener.enable();

	/** enables the mrno search. (Alt + Shift + M) */

	var mrnoSearchKeyListener = new YAHOO.util.KeyListener(document, {
		alt: true,
		shift: true,
		keys: 77
	}, {
		fn: showMrnoSearch,
		scope: "regTyperegd",
		correctScope: true
	});
	mrnoSearchKeyListener.enable();
}

	var photoViewDialog = null;

	function initPatientphotoViewDialog() {
		var dialog = document.getElementById('showphotoViewDialog');
		dialog.style.display = 'block';
		photoViewDialog = new YAHOO.widget.Dialog("showphotoViewDialog", {
			width: "350px",
			height: "250px",
			visible: false,
			modal: true,
			constraintoviewport: true
		});
		photoViewDialog.render();

	}

	var primarySponsorDialog = null;

	function initprimarySponsorDialog() {
		var dialog = document.getElementById('showPimarySponsorViewDialog');
		dialog.style.display = 'block';
		primarySponsorDialog = new YAHOO.widget.Dialog("showPimarySponsorViewDialog", {
			width: "350px",
			height: "250px",
			visible: false,
			modal: true,
			constraintoviewport: true
		});
		primarySponsorDialog.render();

	}

	var secondarySponsorDialog = null;

	function initsecondarySponsorDialog() {
		var dialog = document.getElementById('showSecondarySponsorViewDialog');
		dialog.style.display = 'block';
		secondarySponsorDialog = new YAHOO.widget.Dialog("showSecondarySponsorViewDialog", {
			width: "350px",
			height: "250px",
			visible: false,
			modal: true,
			constraintoviewport: true
		});
		secondarySponsorDialog.render();

	}

function checkForCorporateMemberID(spnsrIndex) {
    var memberIdObj = null;
    var insuCompObj = null;
    var planObj= null;
    var sponsorObj = null;
    if (spnsrIndex == 'P') {
        memberIdObj =  document.getElementById("primary_employee_id")
        insuCompObj = getPrimarySponsorObj();
        sponsorObj = getPrimarySponsorTypeObj();

    } else if (spnsrIndex == 'S') {
        memberIdObj =  document.getElementById("secondary_employee_id")
        insuCompObj =  getSecondarySponsorObj();
        sponsorObj = getPrimarySponsorTypeObj();
    }

    if (memberIdObj != null) {
		var memberId = trimAll(memberIdObj.value);
		var mrNo = document.mrnoform.mrno.value;

		if (!empty(memberId)) {
			var companyId = insuCompObj.value;

			if(!empty(mrNo) && !empty(gVisitId) && !empty(sponsorObj)
				&& !validateBabyAge(mrNo,gVisitId,sponsorObj,memberId)) {
				return true;
			}

			var ajaxobj = newXMLHttpRequest();
			var url = cpath + "/pages/registration/regUtils.do?_method=checkForDuplicateCorporateMemberId"
						+ "&member_id=" + encodeURIComponent(memberId) +"&sponsor_id="+companyId+ "&mr_no=" + mrNo;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var exists =" + ajaxobj.responseText);
						if (exists == "true") {
							showMessage("js.registration.patient.employee.id.already.exists.string");
							memberIdObj.value = "";
							memberIdObj.focus();
							return false;
						}else
							return true;
					}
				}
			}
		}
	}
    return true;
}

function checkForNationalMemberID(spnsrIndex) {
    var memberIdObj = null;
    var insuCompObj = null;
    var planObj= null;
    var sponsorObj = null;
    if (spnsrIndex == 'P') {
        memberIdObj = document.getElementById('primary_national_member_id');
        insuCompObj = getPrimarySponsorObj();
        sponsorObj = getPrimarySponsorTypeObj();
    } else if (spnsrIndex == 'S') {
        memberIdObj = document.getElementById('secondary_national_member_id')
        insuCompObj =  getSecondarySponsorObj();
        sponsorObj = getPrimarySponsorTypeObj();
    }

    if (memberIdObj != null) {
		var memberId = trimAll(memberIdObj.value);
		var mrNo = document.mrnoform.mrno.value;

		if (!empty(memberId)) {
			var companyId = insuCompObj.value;

			if(!empty(mrNo) && !empty(gVisitId) && !empty(sponsorObj)
				&& !validateBabyAge(mrNo,gVisitId,sponsorObj,memberId)) {
				return true;
			}

			var ajaxobj = newXMLHttpRequest();
			var url = cpath + "/pages/registration/regUtils.do?_method=checkForDuplicateNationalMemberId"+
						"&member_id=" + encodeURIComponent(memberId) + "&sponsor_id=" + companyId + "&mr_no=" + mrNo
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var exists =" + ajaxobj.responseText);
						if (exists == "true") {
							showMessage("js.registration.patient.member.id.already.exists.string");
							memberIdObj.value = "";
							memberIdObj.focus();
							return false;
						}else
							return true;
					}
				}
			}
		}
	}
    return true;
}

//--------- editorders related javascript code ---

var presAutoComp = null;
var rowUnderEdit = null;
function doctorAutoComplete(field, dropdown, list, thisForm) {

	var localDs = new YAHOO.util.LocalDataSource(list,{ queryMatchContains : true });
	localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = { resultsList : "doctors",
		fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete(field, dropdown, localDs);

	autoComp.prehightlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;

	var itemSelectHandler = function(sType, aArgs) {
		thisForm.ePresDocId.value =  aArgs[2][1];
	};

	autoComp.itemSelectEvent.subscribe(itemSelectHandler);
	return autoComp;
}
function initEditDialog() {
	editDialog = new YAHOO.widget.Dialog("editDialog", { width:"600px",
			context: ["orderTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("editDialog").style.display = 'block';
	editDialog.render();
	subscribeEscKeyEvent(editDialog, cancelEdit);
	editDialog.cancelEvent.subscribe(onEditDialogCancel);
}

function showEditDialog(imgObj) {
	editDialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	var newEl = getElementByName(row, 'new');
	var type = getElementByName(row, "existingtype").value;
	var newType =  getElementByName(row, "type").value;
	var finStatus = getElementByName(row, "finStatus").value;
	var newFinStatus = getElementByName(row, "newFinStatus").value;
	var isFinalizable = (type == 'Equipment') && (finStatus == 'N');		// orig fin status in db
	var isFinalized = (type == 'Equipment') && (newFinStatus == 'F');
	var conductingDoctorMandatory = getElementByName(row, "conducting_doc_mandatory");
	var itemID = getElementByName(row, "item_id").value;

	if(newType == 'test'|| newType == 'service') {
		document.getElementById('eConductingDoc').style.display = 'table-row';
		function onSelectEConductingDoc(sType, aArgs) {
			var doctor = aArgs[2];
			document.editForm.eConducting_doctorId.value = doctor[1];
		}
		document.editForm.eConducting_doctor.value = '';
		document.editForm.eConducting_doctorId.value = '';
		var payee_doctor_id = getElementByName(row, newType+'.'+"payee_doctor_id").value;
		var filter = getElementByName(row, "addedItemType");
		if (newType == 'test')
			addOrderDialog.initOrderDoctorAutoComplete('eConducting_doctor', addOrderDialog.doctorList, onSelectEConductingDoc, 'dept_id', filter.value);
		if (newType == 'service')
			addOrderDialog.initOrderDoctorAutoComplete('eConducting_doctor', addOrderDialog.doctorList, onSelectEConductingDoc);
		if (payee_doctor_id != '') {
			var doctor = findInList(doctorsList, 'doctor_id', payee_doctor_id);
			document.editForm.eConducting_doctor.value = doctor['doctor_name'];
			document.editForm.eConducting_doctorId.value = payee_doctor_id;
			if (conductingDoctorMandatory.value == 'O') {
				document.editForm.eConducting_doctor.disabled = false;
			} else {
				document.editForm.eConducting_doctor.disabled = true;
			}
		} else {
			if (conductingDoctorMandatory.value == 'O')
				document.editForm.eConducting_doctor.disabled = false;
			else
				document.editForm.eConducting_doctor.disabled = true;
		}
	} else {
		document.getElementById('eConductingDoc').style.display = 'none';
	}
	// editing an existing order

	var remarksElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"remarks");
	if (remarksElmt != null) document.editForm.eRemarks.value = remarksElmt.value;
	var prescDrIdElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"presDocId");
	if (prescDrIdElmt != null) document.editForm.ePresDocId.value = prescDrIdElmt.value;

	document.editForm.ePrescribedBy.value = getElementByName(row, "presDocName").value;
	// the following is to prevent clearing of the autocomp on blur
	presAutoComp._bItemSelected = true;
	var qtyElmt = getElementByName(row,( newEl.value == 'Y' ? newType+".quantity" : "quantity"));

	if (isFinalizable || isFinalized) {
		var fromDateElmt = getElementByName(row, "fromDate");
		if (fromDateElmt != null) document.editForm.eFromDate.value = fromDateElmt.value;

		var fromTimeElmt = getElementByName(row, "fromTime");
		if (fromTimeElmt != null) document.editForm.eFromTime.value = fromTimeElmt.value;

		var toDateElmt = getElementByName(row, "toDate");
		if (toDateElmt != null) document.editForm.eToDate.value = toDateElmt.value;

		var toTimeElmt = getElementByName(row, "toTime");
		if (toTimeElmt != null) document.editForm.eToTime.value = toTimeElmt.value;
	}
	document.editForm.eFinalized.checked = isFinalized;
	document.editForm.eFinalized.disabled = !isFinalizable;
	document.editForm.eFromDate.disabled = !isFinalizable;
	document.editForm.eFromTime.disabled = !isFinalizable;
	document.editForm.eToDate.disabled = !isFinalizable;
	document.editForm.eToTime.disabled = !isFinalizable;

	var urgentElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"urgent");

	if(type == 'Laboratory' || type == 'Radiology' || newType == 'test')
		document.editForm.eurgent.disabled = false;
	else
		document.editForm.eurgent.disabled = true;

	if (urgentElmt != null && urgentElmt.value == 'S')
		document.editForm.eurgent.checked = true;
	else
		document.editForm.eurgent.checked = false;

	if(multiPlanExists) {
		document.getElementById("ePriPreAuthLbl").style.display = 'block';
		document.getElementById("ePreAuthLbl").style.display = 'none';
		document.getElementById("ePriPreAuthModeLbl").style.display = 'block';
		document.getElementById("ePreAuthModeLbl").style.display = 'none';
	}else{
		document.getElementById("ePriPreAuthLbl").style.display = 'none';
		document.getElementById("ePreAuthLbl").style.display = 'block';
		document.getElementById("ePriPreAuthModeLbl").style.display = 'none';
		document.getElementById("ePreAuthModeLbl").style.display = 'block';
	}

	var priorAuthElmt = getElementByName(row, "prior_auth_id");
	if (priorAuthElmt != null) document.editForm.ePriorAuthId.value = priorAuthElmt.value;

	var secPriorAuthElmt = getElementByName(row, "sec_prior_auth_id");
	if(secPriorAuthElmt != null) document.editForm.eSecPriorAuthId.value = secPriorAuthElmt.value;

	var priorAuthModeElmt = getElementByName(row, "prior_auth_mode_id");
	if (priorAuthModeElmt != null) document.editForm.ePriorAuthMode.value = priorAuthModeElmt.value;

	var secPriorAuthModeElmt = getElementByName(row, "sec_prior_auth_mode_id");
	if(secPriorAuthModeElmt != null) document.editForm.eSecPriorAuthMode.value = secPriorAuthModeElmt.value;

	var toothNum = getElementByName(row, "s_tooth_number").value;
	if (empty(type)) {
		document.getElementById('edToothNumBtnDiv').style.display = empty(toothNum) ? 'none' : 'block';
		document.getElementById('edToothNumDsblBtnDiv').style.display = empty(toothNum) ? 'block' : 'none';
	} else {
		// do not allow to edit the tooth number for the services which are already saved.
		document.getElementById('edToothNumBtnDiv').style.display = 'none';
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'block';
	}
	var nos = toothNum.split(',');
	var tooth_numbers_text = '';
	var index = 0;
	for (var k=0; k<nos.length; k++) {
		if (index > 0) tooth_numbers_text += ',';
		if (index%10 ==0)
			tooth_numbers_text += '\n';

		tooth_numbers_text += nos[k];
		index++;
	}
	document.getElementById('edToothNumberDiv').textContent = tooth_numbers_text;
	document.getElementById('ed_tooth_number').value = toothNum;
	var toothNumReqEl = getElementByName(row, 's_tooth_num_required');
	if (toothNumReqEl != null) document.editForm.ed_tooth_num_required.value = toothNumReqEl.value;

	if(newEl.value == 'Y') {
		document.getElementById("ePriAuthRowId").style.visibility = "visible";
		if(multiPlanExists)
			document.getElementById("eSecPriAuthRowId").style.visibility = "visible";
	}
	else {
		document.getElementById("ePriAuthRowId").style.visibility = "hidden";
		document.getElementById("eSecPriAuthRowId").style.visibility = "hidden";
	}

/*	var billStatusEl = getElementByName(row, 'bill_status');
	if (billStatusEl && billStatusEl.value != 'A') {
		// disable changing of presc doctor if bill is not open. Presc doctor change
		// affects payment rule selection, so this should not be allowed.
		document.editForm.ePrescribedBy.disabled = true;
	} else {
		document.editForm.ePrescribedBy.disabled = false;
	}*/

	showCondDoctorsOfPackInEditDialog(row);

	rowUnderEdit = row;
	YAHOO.util.Dom.addClass(row, 'editing');
	editDialog.show();

	document.editForm.eRemarks.focus();
}

function showCondDoctorsOfPackInEditDialog(row) {
	var table = document.getElementById('condDoctorsTableED'); // conducting doctors table in edit dialog.
	for (var i=1; i<table.rows.length; ) {
		table.deleteRow(i);
	}
	var curPrescIdEl = getElementByName(row, 'package.prescId');
	if (empty(curPrescIdEl)) {
		document.getElementById('condDoctorsTableED').style.display = 'none'; // hide when not found.
		return ;
	}

	var packageItems = document.getElementsByName('package.packageItemName');
	var packageName = document.getElementsByName('package.packageName');
	var packageTypes = document.getElementsByName('package.packageItemType');
	var packageActivityIds = document.getElementsByName('package.activity_id');
	var packageActivityIndexes = document.getElementsByName('package.packageActivityIndex');
	var packCondDoctors = document.getElementsByName('package.condDoctor');
	var packageIds = document.getElementsByName('package.packIdForCondDoc');
	var prescIdEls = document.getElementsByName('package.packPrescIdForCondDoc');


	document.getElementById('condDoctorsTableED').style.display = 'table'; // display only when prescriptions found.
	for (var i=0; i<prescIdEls.length; i++) {
		if (curPrescIdEl.value == prescIdEls[i].value) {
			var docRow = table.insertRow(-1);

			var cell = docRow.insertCell(-1);
			cell.appendChild(makeLabel(null, packageTypes[i].value));

			var cell = docRow.insertCell(-1);
			cell.appendChild(makeLabel(null, packageItems[i].value));
			cell.appendChild(makeHidden("ed_packageName", "ed_packageName"+i, packageName[i].value));
			cell.appendChild(makeHidden("ed_packageId", "ed_packageId"+i, packageIds[i].value));
			cell.appendChild(makeHidden("ed_packageItemName", "ed_packageItemName"+i, packageItems[i].value));
			cell.appendChild(makeHidden("ed_packageActId", "ed_packageActId"+i, packageActivityIds[i].value));
			// this is required to maintain the association to support the duplicate items.
			cell.appendChild(makeHidden("ed_packageActivityIndex", "ed_packageActivityIndex"+i, packageActivityIndexes[i].value+''));

			var condDoctor = "ed_packConductingDoctor"+i;
			var condCoctorContainer = condDoctor + "AcDropdown";

			var doctorName = '';
			if (!empty(packCondDoctors[i].value)) {
				doctorName = findInList(addOrderDialog.doctorList.doctors, "doctor_id", packCondDoctors[i].value).doctor_name;
			}

			var cell = docRow.insertCell(-1);
			cell.setAttribute("class", "yui-skin-sam");
			cell.innerHTML = '<div><input id="'+ condDoctor +'" name="ed_packConductingDoctor" type="text" value="'+doctorName+'"/><div id="'+ condCoctorContainer +'" ></div></div>';
			cell.appendChild(makeHidden("ed_packCondDoctorId", "ed_packCondDoctorId"+i, packCondDoctors[i].value));

			var itemType = packageTypes[i].value;
			var autoComp = null;;
			if (itemType == 'Laboratory' || itemType == 'Radiology') {
				autoComp = addOrderDialog.initOrderDoctorAutoComplete(condDoctor, addOrderDialog.doctorList, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("ed_packConductingDoctor", "");
						document.getElementById("ed_packCondDoctorId"+index).value = aArgs[2][1];
						}, "dept_id", (itemType == 'Laboratory' ? 'DEP_LAB': 'DEP_RAD'));
			} else {
				autoComp = addOrderDialog.initOrderDoctorAutoComplete(condDoctor, addOrderDialog.doctorList, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("ed_packConductingDoctor", "");
						document.getElementById("ed_packCondDoctorId"+index).value = aArgs[2][1];
						});
			}
			if (autoComp._elTextbox.value != '') {
				autoComp._bItemSelected = true;
				autoComp._sInitInputValue = autoComp._elTextbox.value;
			}

		}
	}
}



function cancelEdit() {
	editDialog.cancel();
	rowUnderEdit = undefined;
}

function onEditDialogCancel() {
	document.getElementById("ePriAuthRowId").style.visibility = "hidden";
	document.getElementById("eSecPriAuthRowId").style.visibility = "hidden";
	YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
}

function saveEdit() {
	var row = rowUnderEdit;
	var newEl = getElementByName(row, 'new');

	var editedEl = getElementByName(row, 'edited');
	if (editedEl)
		editedEl.value = 'Y';

	if (document.editForm.ePrescribedBy.value == '')
		document.editForm.ePresDocId.value = '';
	if (document.editForm.ed_tooth_num_required.value == 'Y' &&
		document.editForm.ed_tooth_number.value == '') {
		alert('Service required the tooth number.');
		return false;
	}

	setNodeText(row.cells[PRES_DOCTOR_COL], document.editForm.ePrescribedBy.value);
	setNodeText(row.cells[REMARKS_COL], document.editForm.eRemarks.value, 16);
	setNodeText(row.cells[PRE_AUTH_COL], document.editForm.ePriorAuthId.value, 16);
	if(multiPlanExists)
		setNodeText(row.cells[SEC_PRE_AUTH_COL], document.editForm.eSecPriorAuthId.value, 16);

	if(!document.editForm.eFromDate.disabled && !validateEditFields())
		return false;

	var tmType = getElementByName(row, "type").value;

	var prescribedDoctor = "";
	if(tmType == 'doctor') {
		prescribedDoctor = tmType+'.presc_doctor_id';
	} else if(tmType == 'service') {
		prescribedDoctor = tmType+'.doctor_id';
	} else if(tmType == 'test') {
		prescribedDoctor = tmType+'.pres_doctor';
	} else if(tmType == 'package') {
		prescribedDoctor = tmType+'.doctorId';
	} else if(tmType == 'other') {
		prescribedDoctor = tmType+'.doctor_id';
	} else if(tmType == 'Equipment') {
		prescribedDoctor = 'equipment.doctor_id';
	} else if(tmType == 'diet') {
		prescribedDoctor = tmType+'.ordered_by';
	}

	if (tmType == 'test' || tmType == 'service') {
		var condDocMandatory = getElementByName(row, 'conducting_doc_mandatory').value;
		var eConductingDoctor = document.editForm.eConducting_doctor.value;
		if (condDocMandatory == 'O') {
			if (eConductingDoctor == '') {
				showMessage('js.common.order.conducting.doctor.required');
				return false;
			} else {
				getElementByName(row, tmType+"."+"payee_doctor_id").value = document.editForm.eConducting_doctorId.value;
			}
		}
	}

	var remarksElmt = getElementByName(row, (newEl.value == 'Y' ? tmType+".":'')+"remarks");
	if (remarksElmt != null) remarksElmt.value = document.editForm.eRemarks.value;
	var prescDrIdElmt = getElementByName(row, prescribedDoctor);
	if (prescDrIdElmt != null) prescDrIdElmt.value = document.editForm.ePresDocId.value;
	getElementByName(row, "presDocName").value = document.editForm.ePrescribedBy.value;
	getElementByName(row, "newFinStatus").value = document.editForm.eFinalized.checked ? 'F' : 'N' ;
	getElementByName(row, "fromDate").value = document.editForm.eFromDate.value;
	getElementByName(row, "fromTime").value = document.editForm.eFromTime.value;
	getElementByName(row, "toDate").value = document.editForm.eToDate.value;
	getElementByName(row, "toTime").value = document.editForm.eToTime.value;
	if (document.editForm.ed_tooth_number) {
		var tooth_number = document.editForm.ed_tooth_number.value;
		if (getElementByName(row, "service.tooth_unv_number")) {
			getElementByName(row, "service.tooth_unv_number").value = tooth_number;
		} else if (getElementByName(row, "service.tooth_fdi_number")) {
			getElementByName(row, "service.tooth_fdi_number").value = tooth_number;
		}
		getElementByName(row, "s_tooth_number").value = tooth_number;
	}


	var urgentElmt = getElementByName(row, (newEl.value == 'Y' ? tmType+".":'')+"urgent");
	if (urgentElmt != null) urgentElmt.value = document.editForm.eurgent.checked ? 'S' : 'R' ;

	if(document.editForm.ePriorAuthId && insured){
		var priorAuthIdElmt = getElementByName(row, tmType+".prior_auth_id");
		if (priorAuthIdElmt != null) priorAuthIdElmt.value = document.editForm.ePriorAuthId.value;
		if (getElementByName(row, "prior_auth_id") != null)
			getElementByName(row, "prior_auth_id").value = document.editForm.ePriorAuthId.value;
		var priorAuthModeIdElmt = getElementByName(row, tmType+".prior_auth_mode_id");
		if (priorAuthModeIdElmt != null) priorAuthModeIdElmt.value = document.editForm.ePriorAuthMode.value;
		if (getElementByName(row, "prior_auth_mode_id") != null)
			getElementByName(row, "prior_auth_mode_id").value = document.editForm.ePriorAuthMode.value;
	}

	if(document.editForm.eSecPriorAuthId && insured && multiPlanExists){
		var secPriorAuthIdElmt = getElementByName(row, tmType+".sec_prior_auth_id");
		if (secPriorAuthIdElmt != null) secPriorAuthIdElmt.value = document.editForm.eSecPriorAuthId.value;
		if (getElementByName(row, "sec_prior_auth_id") != null)
			getElementByName(row, "sec_prior_auth_id").value = document.editForm.eSecPriorAuthId.value;
		var secPriorAuthModeIdElmt = getElementByName(row, tmType+".sec_prior_auth_mode_id");
		if (secPriorAuthModeIdElmt != null) secPriorAuthModeIdElmt.value = document.editForm.eSecPriorAuthMode.value;
		if (getElementByName(row, "sec_prior_auth_mode_id") != null)
			getElementByName(row, "sec_prior_auth_mode_id").value = document.editForm.eSecPriorAuthMode.value;
	}

	if (tmType == 'package') {

		var condDoctorInputEls = document.getElementsByName('ed_packConductingDoctor');
		for (var i=0; i<condDoctorInputEls.length; i++) {
			if (empty(condDoctorInputEls[i].value)) {
				showMessage('js.common.order.conducting.doctor.required');
				condDoctorInputEls[i].focus();
				return false;
			}

			var packId = document.getElementsByName('ed_packageId')[0].value;
			var prescId = getElementByName(row, "package.prescId").value;

			var packIdCondDocEl = document.getElementsByName('package.packIdForCondDoc');
			var prescIdCondDocEl = document.getElementsByName('package.packPrescIdForCondDoc');
			var rowIndexCondEl = document.getElementsByName('package.mainRowIndex');
			var packActivityEl = document.getElementsByName('package.packageActivityIndex');
			for (var j=0; j<packIdCondDocEl.length; j++) {
				if (packIdCondDocEl[j].value == packId
						&& prescIdCondDocEl[j].value == prescId
						&& parseInt(packActivityEl[j].value) == parseInt(document.getElementsByName('ed_packageActivityIndex')[i].value)) {
					document.getElementsByName('package.condDoctor')[j].value = document.getElementsByName('ed_packCondDoctorId')[i].value;
				}
			}
		}
	}

	editDialog.cancel();
	YAHOO.util.Dom.addClass(rowUnderEdit, 'edited');
}

function validateEditFields(){
	var valid = true;

	valid = valid && validateRequired(document.editForm.eFromDate,"Start Date required");
	valid = valid && validateRequired(document.editForm.eFromTime,"Start Time required");
	valid = valid && validateRequired(document.editForm.eToDate,"End Date required");
	valid = valid && validateRequired(document.editForm.eToTime,"End Time required");
	valid = valid && validateFromToDateTime(document.editForm.eFromDate, document.editForm.eFromTime,
			document.editForm.eToDate, document.editForm.eToTime, true, true)
	return valid;
}

function validatePlan() {

	var priInsCompObj = getPrimaryInsuObj();
	var priTpaObj = getPrimarySponsorObj();
	var priPlanObj = getPrimaryPlanObj();
	var priPlanTypeObj = getPrimaryPlanTypeObj();
	var priMemberIdObj = getPrimaryInsuranceMemberIdObj();

	var secInsCompObj = getSecondaryInsuObj();
	var secTpaObj = getSecondarySponsorObj();
	var secPlanObj = getSecondaryPlanObj();
	var secPlanTypeObj = getSecondaryPlanTypeObj();
	var secMemberIdObj = getSecondaryInsuranceMemberIdObj();


	if (isModAdvanceIns) {
		if (!empty(priInsCompObj) && priInsCompObj.value !='' && !empty(secInsCompObj) && secInsCompObj.value !=''
							&& !empty(priPlanObj) && priPlanObj.value !='' && !empty(secPlanObj) && secPlanObj.value !='') {

			if (priInsCompObj.value == secInsCompObj.value && priPlanObj.value == secPlanObj.value) {
				var msg = getString("js.registration.patient.plans");
				msg += " "+getString("js.registration.patient.same.isnotallowed");;
				alert(msg);
				return false;
			}
			return true;
		}
		return true;
	}
	return true;
}

function validateMemberId() {

	if (isModAdvanceIns) {

		var priPlanObj = getPrimaryPlanObj();
		var secPlanObj = getSecondaryPlanObj();
		var priInsCompObj = getPrimaryInsuObj();
		var secInsCompObj = getSecondaryInsuObj();
		var priMemberIdObj = getPrimaryInsuranceMemberIdObj();
		var secMemberIdObj = getSecondaryInsuranceMemberIdObj();

		if ((priPlanObj != null && priPlanObj.value != "")
				&& (secPlanObj != null && secPlanObj.value != "")) {

			if (priInsCompObj.value == secInsCompObj.value
						&& (priMemberIdObj != null && priMemberIdObj.value.trim() != '')
						&& (secMemberIdObj != null && secMemberIdObj.value.trim() != '')) {

				if (priMemberIdObj.value.trim() == secMemberIdObj.value.trim()) {
					var memberIdRequiredMsg = memberIdLabel + " " +getString("js.registration.patient.same.isnotallowed");
					alert(memberIdRequiredMsg);
					return false;
				}
				return true;
			}
			return true;
		}
		return true;
	}
	return true;
}


function hijriToGregorian() {
	//alert("hi");
	var day =  document.getElementById("dobHDay").value;
	var month = document.getElementById("dobHMonth").value;
	var year = document.getElementById("dobHYear").value;

    if (year != 'YYYY' && !isInteger(year)) {
        showMessage("js.registration.patient.invalid.year.not.an.integer.string");
        return null;
    }
    if (month != 'MM' && !isInteger(month)) {
        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
	    //setTimeout("document.mainform.dobHMonth.focus()",0);
        return null;
    }
    if (day != 'DD' && !isInteger(day)) {
        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
	    //setTimeout("document.mainform.dobHDay.focus()",0);
        return null;
    }

    if (parseInt(day) > 30) {
        showMessage("js.common.invalid.date.enter.1.30.for.day.string");
        return null;
    }

    if (parseInt(month) > 12) {
        showMessage("js.common.invalid.date.enter.1.12.for.month.string");
        return null;
    }

	if (year.length < 4) {
		alert("Please enter the hijri year as YYYY");
		return null;
	}

	if (day != '' && day != 'DD' && month != '' && month != 'MM' && year != '' && year != 'YYYY') {
		var ajaxobj = newXMLHttpRequest();
		//alert("about to make ajax")

	var url = cpath + '/pages/registration/regUtils.do?_method=getHijriToGregorian&dobDay=' + day
	+ '&dobMonth=' + month + '&dobYear=' + year;
		//alert("1");
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var Date =" + ajaxobj.responseText);
				//alert(ajaxobj.responseText);
				if (empty(Date.error)) {
					document.getElementById("dobDay").value = Date.day;
					document.getElementById("dobMonth").value = Date.month;
					document.getElementById("dobYear").value = Date.year;
					getAge(true, null);
				} else {
					document.getElementById("dobDay").value = '';
					document.getElementById("dobMonth").value = '';
					document.getElementById("dobYear").value = '';
				}
			}
		}
	}
}
}

function calculateAgeAndHijri() {
	if(getAge(true, null)) {
		if(hijriPref == 'Y') {
			gregorianToHijri();
		}
		return true;
	} else
		return false;
}
var conflictCheckFlag = false;
var smartCardPatientNotFound = false;
var dt;
function readFromCard(){
	var ajaxReqObject = newXMLHttpRequest();
	var url = '//127.0.0.1:9876/devices/cardReader/read';
	var reqObject = newXMLHttpRequest();
	reqObject.open("GET", url.toString(), false);
	try {
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				smartCardDetails=reqObject.responseText;
				dt=JSON.parse(smartCardDetails);
				if(null != dt["dateOfBirth"]) {		//dt["dateOfBirth"] is of format MM/dd/yyyy
					var day = dt["dateOfBirth"].substring(3,5);
					var month = dt["dateOfBirth"].substring(0,2);
					var year = dt["dateOfBirth"].substring(6,10);
					dt["dateOfBirth"] = day + "/" + month + "/" + year;
				}
				if(null != dt["idNumber"])
					dt["idNumber"] = applyRegEx(dt["idNumber"],smartCardIDPattern);
				if(dt['cardStatus'] == 'The Card Read Successfully' && smartCardDetails != null && smartCardDetails !=""){
					if(mainform.appointmentId.value!=""){
						var dobSys;
						var nameSys;
						var nameSc;
						if(null!=document.getElementById("dobDay")) {
							var dobDay = document.getElementById("dobDay").value;
							var dobMonth = document.getElementById("dobMonth").value;
							var dobYear = document.getElementById("dobYear").value;
							if(dobDay.length == 1)
								dobSys = "0"+dobDay+"/";
							else
								dobSys = dobDay+"/";
							if(dobMonth.length == 1)
								dobSys = dobSys + "0" + dobMonth + "/";
							else
								dobSys = dobSys + dobMonth + "/";
							dobSys = dobSys + dobYear;
						}
						if(null!=document.getElementById("patient_name"))
							nameSys = document.getElementById("patient_name").value;
						if(null!=document.getElementById("middle_name") && document.getElementById("middle_name").value!='..MiddleName..')
							nameSys = nameSys + " " + document.getElementById("middle_name").value;
						if(null!=document.getElementById("last_name") && document.getElementById("last_name").value!='..LastName..')
							nameSys = nameSys + " " + document.getElementById("last_name").value;

						if(null!=dt["fName"])
							nameSc = dt["fName"];
						if(null!=dt["mName"])
							nameSc = nameSc + " " + dt["mName"];
						if(null!=dt["lName"])
							nameSc = nameSc + " " + dt["lName"];

						if(nameSys!=nameSc || document.getElementById("government_identifier").value!=dt["idNumber"] || dobSys!=dt["dateOfBirth"] ) {

							if(dt['photo'] != null && dt['photo'] != "")
								document.getElementById("img_sc").src= "data:image/jpeg;base64," + dt['photo'];
							else
								document.getElementById("img_sc").src= cpath + "/images/patienPlaceholder.png";
							document.getElementById("nationalid_sc").textContent= dt["idNumber"];
							document.getElementById("patientName_sc").textContent= nameSc;
							document.getElementById("dob_sc").textContent= dt['dateOfBirth'];

							document.getElementById("img_sys").src= cpath + "/images/patienPlaceholder.png";
							document.getElementById("nationalid_sys").textContent= document.getElementById("government_identifier").value;
							document.getElementById("patientName_sys").textContent= nameSys;
							document.getElementById("dob_sys").textContent= dobSys;
							initSCConflictDialog.show();
						}
						else
							alert("Card details already filled");
					}
					else
						handleAjaxResponseForCardDetails(smartCardDetails);
				}else if(dt['error'] != ""){
					return showPatientDetailsFmomSCDialogErr(dt['error']);
				}
			}
			else{
				return showPatientDetailsFmomSCDialogErr(reqObject.statusText);
			}
		}else{
			return showPatientDetailsFmomSCDialogErr(reqObject.statusText);
		}
	} catch (ex) {
		alert("InstaNexus App does not seem to be running, Please start the app and try again.");
	}

	if(document.getElementById('regTyperegd').checked == true && mainform.appointmentId.value=="") {

			var sqlDOB = dt["dateOfBirth"].substring(6,10) +"-"+ dt["dateOfBirth"].substring(3,5) +"-"+ dt["dateOfBirth"].substring(0,2);
			myAutocomp.generateRequest = function(sQuery) {
				return YAHOO.widget.AutoComplete.prototype.generateRequest.call(this, sQuery)+"&smartCard=yes"+"&patientName="+dt['fName']+"&middleName="+dt['mName']+"&lastName="+dt['lName']+"&patientGender="+dt['sex']+"&nid="+dt["idNumber"]+"&sqlDOB="+sqlDOB;
			}
			document.getElementById("mrno").value = dt["idNumber"];
			myAutocomp.sendQuery(dt["fName"]);
			document.getElementById("mrno").focus();
			myAutocomp.generateRequest = function(sQuery) {
				return YAHOO.widget.AutoComplete.prototype.generateRequest.call(this, sQuery);
			}
			conflictCheckFlag = true;
			//myAutocomp.itemSelectEvent.subscribe(conflictCheck);
			smartCardPatientNotFound = true;
			myAutocomp.dataReturnEvent.subscribe(noSmartCardPatient);
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

function noSmartCardPatient(oSelf , sQuery , aResults){
	if(decodeURI(sQuery[1]) != JSON.parse(smartCardDetails)["fName"]) {
		conflictCheckFlag = false;
	}
	if(smartCardPatientNotFound ==true) {
		smartCardPatientNotFound = false;
		if(sQuery[2].length == 0) {
			conflictCheckFlag = false;
			alert("Patient not found, please register as a new patient.");
		}
	}
}

function handleAjaxResponseForCardDetails(smartCardDetails){
	if ( document.getElementById('regTyperegd').checked == false) {
		showPatientDetailsFmomSCDialog();
	}
}


var initPatDetailsFromSCDialog;

function initPatDetailsFromSmartCardDialog() {
	document.getElementById('patientDetailsFromSmartCardDialog').style.display = 'block';
	initPatDetailsFromSCDialog = new YAHOO.widget.Dialog('patientDetailsFromSmartCardDialog', {
    	context:["patientDetailsFromSmartCardDialog","tr","br"],
    	fixedcenter: true,
        visible: false,
        modal: true,
        constraintoviewport: true,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePatDetailsFromSCDialogCancel,
                                               scope:initPatDetailsFromSCDialog,
                                               correctScope:true } );
    initPatDetailsFromSCDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
    initPatDetailsFromSCDialog.cancelEvent.subscribe(handlePatDetailsFromSCDialogCancel);
    initPatDetailsFromSCDialog.render();
}

function handlePatDetailsFromSCDialogCancel(){
	initPatDetailsFromSCDialog.hide();
	if (!empty(document.getElementById('identifier_id')))
		document.getElementById('identifier_id').removeAttribute('disabled');
	clearRegDetails();
}

function showPatientDetailsFmomSCDialog() {
	var button = null;
	button = document.getElementById('readCard');

	if (button != null) {
		initPatDetailsFromSCDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		showPatientDetailsFromSC();
	}
}


function showPatientDetailsFromSC(){
	if(null != dt['photo'] && dt['photo'] != "")
		document.getElementById("patient-card-image").src= "data:image/jpeg;base64," + dt['photo'];
	else
		document.getElementById("patient-card-image").src= cpath + "/images/patienPlaceholder.png";
	document.getElementById("nationalid_sc1").textContent= dt["idNumber"];
	document.getElementById("title_sc1").textContent= getTitle(dt['sex'],dt['maritalStatus']);
	document.getElementById("patientName_sc1").textContent= dt["fullNameEnglish"];
	document.getElementById("gender_sc1").textContent= dt["sex"];
	document.getElementById("nationality_sc1").textContent= dt["nationality"];
	document.getElementById("dob_sc1").textContent= dt["dateOfBirth"];

	initPatDetailsFromSCDialog.show();
}

function getTitle(sex,maritalStatus){
	 if(sex =='M'){
		 return 'Mr';
	 }
	 else if(sex =='F' && (maritalStatus =='Single' || maritalStatus == 'Not married' || maritalStatus == '')){
		 return 'Miss';
	 }
	 else if(sex=='F' && (maritalStatus =='Married' || maritalStatus == 'Divorced'|| maritalStatus == 'Widowed')){
		 return 'Mrs';
	 }
}

function saveDetailsInRegScreen(){
	cardOverwrite();
	initPatDetailsFromSCDialog.hide();
}

var initPatDetailsFromSCDialogErr;

function initPatDetailsFromSmartCardDialogErr() {
	document.getElementById('patientDetailsFromSmartCardDialogForErr').style.display = 'block';
	initPatDetailsFromSCDialogErr = new YAHOO.widget.Dialog('patientDetailsFromSmartCardDialogForErr', {
    	context:["patientDetailsFromSmartCardDialogForErr","tr","br"],
    	fixedcenter: true,
        visible: false,
        modal: true,
        constraintoviewport: true,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePatDetailsFromSCDialogCancelErr,
                                               scope:initPatDetailsFromSCDialogErr,
                                               correctScope:true } );
    initPatDetailsFromSCDialogErr.cfg.queueProperty("keylisteners", [escKeyListener]);
    initPatDetailsFromSCDialogErr.cancelEvent.subscribe(handlePatDetailsFromSCDialogCancelErr);
    initPatDetailsFromSCDialogErr.render();
}

function handlePatDetailsFromSCDialogCancelErr(){
	initPatDetailsFromSCDialogErr.hide();
}

function showPatientDetailsFmomSCDialogErr(errors) {
		document.getElementById("pd_sc_error").textContent=errors;
		initPatDetailsFromSCDialogErr.show();
}

function closeErrDialogue(){
	initPatDetailsFromSCDialogErr.hide();
}

function onChangeOfGovtIdType(){
	if (!empty(document.getElementById('identifier_id')))
		document.getElementById('identifier_id').removeAttribute('disabled');
}

function compareMemberIdAndPattern(memberIdObj, memberIdPattern, memberIdLabel){
	var ok;
	var memberId;

	if(memberIdObj !=null){
		memberId = memberIdObj.value == null || memberIdObj.value == "" ? null : memberIdObj.value;
	}
	if(memberIdPattern != null && memberIdPattern!= "" && memberId != null){
		if(memberId.length == memberIdPattern.length){
			for(i=0;i<memberId.length;i++){
				var patternChar = memberIdPattern.charAt(i);
				var membChar = memberId.charAt(i);
				if((patternChar == 'x') && (isNaN(membChar) == true)) {
					continue;
				} else if((patternChar == '9') && (isNaN(membChar) == false)) {
					continue;
				} else if(patternChar == membChar) {
					continue;
				}
				ok = confirm(memberIdLabel+" pattern is not matching with pattern:"+memberIdPattern);
				isMemberidValidated = true;
				if(!ok){
					memberIdObj.value='';
					memberIdObj.focus();
					return false;
				} else
					return true;
			}
		} else {
			ok = confirm(memberIdLabel+" pattern is not matching with pattern:"+memberIdPattern);
			isMemberidValidated = true;
			if(!ok){
				memberIdObj.value='';
				memberIdObj.focus();
				return false;
			} else
				return true;
		}
	}
	return true;
}

function loadUhidPatient() {
	if(uhidPatient == "yes") {
		document.getElementById('regTypenew').checked = true;
		document.getElementById("patient_name").disabled = false;
		document.getElementById("patient_name").value = uhidPatientFirstName;
		document.getElementById("middle_name").value = uhidPatientMiddleName;
		document.getElementById("last_name").value = uhidPatientLastName;
		insertNumberIntoDOM(uhidPatientPhone,$("#patient_phone"),$("#patient_phone_country_code"),
				$("#patient_phone_national"));
		document.getElementById("patient_gender").value = uhidPatientGender;
		document.getElementById("age").value = uhidPatientAge;
		if(document.getElementById("oldmrno") != null)
			document.getElementById("oldmrno").value = uhidPatientUHID;
		document.getElementById("uhidPatientUHID").value = uhidPatientUHID;

	}
}

function checkTransactionLimitValue(){
	var totPatAmt=0.00;
	    if(gIsInsurance)
	    	totPatAmt = parseFloat(document.getElementById('totalNewPatientAmt').textContent);
	    else
	    	totPatAmt = parseFloat(document.getElementById('estimtAmount').textContent)

	if(jPaymentModes != undefined){
		var paymentModeDetail = findInList(jPaymentModes, "payment_mode", "Cash");
		if(paymentModeDetail != null){
			if(paymentModeDetail.transaction_limit != null && paymentModeDetail.transaction_limit != 0){
				if(totPatAmt > paymentModeDetail.transaction_limit && paymentModeDetail.allow_payments_more_than_transaction_limit =='W'){
					if ( !confirm("Pay amount is more than Cash mode transaction limit "+paymentModeDetail.transaction_limit+" . "+ "Do you want proceed?")) {
						return false;
					}

				}else if(totPatAmt > paymentModeDetail.transaction_limit && paymentModeDetail.allow_payments_more_than_transaction_limit =='B'){
					alert("You have exceeded the transaction limit "+paymentModeDetail.transaction_limit+" of Cash, can continue through edit bill screen with other payment mode");
					return false;
				}
			}
		}
	}

	return true;
}
