function initSearch() {
	document.getElementById('last_bill_finalized_date1').value = formatDate(
			getServerDate(), 'ddmmyyyy', '-');
}

function reloadNetworkTypes() {
	var catObj = document.getElementById('category_id');
	var insCompObj = document.getElementById('insurance_co_id');
	var tpaObj = document.getElementById('tpa_id');
	var insCompId = insCompObj.value;
	var tpaId = tpaObj.value;
	if (catObj != null) {
		var insuranceCategoryList = getInsuranceCategories(insCompId, tpaId);
		loadSelectBox(catObj, insuranceCategoryList, 'category_name',
			'category_id', '(All)');
		sortDropDown(catObj);
	}

}

function resetPlans() {
	var planObj = document.getElementById('plan_id');
	if (planObj != null) {
		var optn = new Option("(All)", "");
		planObj.options.length = 1;
		planObj.options[0] = optn;
	}
}

function onInsuranceCompanyChange() {
	var insCompObj = document.getElementById('insurance_co_id');
	var tpaObj = document.getElementById('tpa_id');
	var catObj		= document.getElementById('category_id');
	var planObj		= document.getElementById('plan_id');

	var insCompId = insCompObj.value;
	var tpaId = tpaObj.value;

	if (insCompId != '') {
		// get companyTpaList
		var companyTpaList = getCompanyTpaListAJAX(insCompId);
		var insCompTpaDetails = filterList(companyTpaList, 'insurance_co_id',
				insCompId);

		if (empty(insCompTpaDetails))
			loadSelectBox(tpaObj, tpaList, 'tpa_name', 'tpa_id', '(All)');
		else
			loadSelectBox(tpaObj, insCompTpaDetails, 'tpa_name', 'tpa_id',
					'(All)');

		sortDropDown(tpaObj);

		setSelectedIndex(tpaObj, tpaId);
		setSelectedIndex(insCompObj, insCompId);

		if (planObj != null) {
			var optn = new Option("(All)", "");
			planObj.options.length = 1;
			planObj.options[0] = optn;
		}

		if (catObj != null) {
			var insuranceCategoryList = getInsuranceCategories(insCompId);
			loadSelectBox(catObj, insuranceCategoryList, 'category_name',
					'category_id', '(All)');
			sortDropDown(catObj);
		}
		resetPlans();
		reloadNetworkTypes();

	} else {
		if (tpaId == '')
			loadTpaArray();

		if (insCompId == '') {
			loadCategoryArray();

			resetPlans();
		}
	}

}

function getInsuranceCategories(insCompId, sponsorId) {
	if (insCompId != "") {
		var ajaxobj = newXMLHttpRequest();
		var url = null;
		url = cpath
				+ "/master/insuranceplans/categoryList.json?insuranceCompanyId="
				+ insCompId;
		if (sponsorId) {
			url += "&sponsorId=" + sponsorId;
		}

		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				var responseObj = JSON.parse(ajaxobj.responseText);
				return responseObj.category_details;
			}
		}
	} else {
		return [];
	}
}

function getPlanList(categoryList, sponsorId) {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	url = cpath
			+ "/master/insuranceplans/planListByCategoriesAndSponsor.json?categoryList="
			+ categoryList;
	if (sponsorId) {
		url += "&sponsorId=" + sponsorId;
	}
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			var responseObj = JSON.parse(ajaxobj.responseText);
			return responseObj.planList;
		}
	}
	return [];
}

function getPlanTypesBasedOnSponsor(sponsorId) {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	url = cpath
			+ "/master/insuranceplans/planTypeListBySponsor.json?sponsorId="
			+ sponsorId;
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			var responseObj = JSON.parse(ajaxobj.responseText);
			return responseObj.planTypeList;
		}
	}
	return [];
}

function onInsuranceCategoryChange() {
	var planObj = document.getElementById('plan_id');
	var tpaObj = document.getElementById('tpa_id');

	var catList = getSelectedCategories();
	var planId = planObj.value;
	var tpaId = tpaObj.value;
	var planList = [];

	if (catList.length > 0) {
		planList = getPlanList(catList, tpaId);
		loadSelectBox(planObj, planList, 'plan_name', 'plan_id', '(All)');
	} else {
		var optn = new Option("(All)", "");
		planObj.options.length = 1;
		planObj.options[0] = optn;
	}
	setSelectedIndex(planObj, planId);
}

function onTPAChange() {
	var insCompObj = document.getElementById('insurance_co_id');
	var tpaObj = document.getElementById('tpa_id');
	var catObj = document.getElementById('category_id');

	var planObj = document.getElementById('plan_id');

	var insCompId = insCompObj.value;
	var tpaId = tpaObj.value;

	if (tpaId != '') {

		setSelectedIndex(insCompObj, insCompId);
		setSelectedIndex(tpaObj, tpaId);

	} else {
		if (insCompId == '') {
			loadTpaArray();
		}
	}

	if (tpaId != '' && insCompId == '') {
		if (catObj != null) {
			var categoryList = getPlanTypesBasedOnSponsor(tpaId);
			loadSelectBox(catObj, categoryList, 'category_name', 'category_id',
					'(All)');
			sortDropDown(catObj);
		}
	} else {
		reloadNetworkTypes();
	}
	resetPlans();
}

function isPatientType() {
	var success = false;
	var patientType = document.getElementsByName('visit_type');
	for (var i = 0; i < patientType.length; i++) {
		var el = patientType[i];
		if (el.checked) {
			success = true;
		}
	}
	return success;
}

function isPatientTypeSelected() {
	var patientType = document.getElementsByName('visit_type');
	if (patientType != null) {
		if (patientType.length == undefined) {
			if (!patientType.checked) {
				alert('Select patient type');
				return false;
			}
		} else {
			if (!isPatientType()) {
				return false;
			}
		}
	}
	return true;
}

function validateSubmit() {
	var insCompObj = document.submissionform.insurance_co_id;
	var tpaObj = document.submissionform.tpa_id;
	var patientType = document.getElementsByName('visit_type');
	var billStatus = document.getElementsByName('bill_status');
	var firstOpenToDate = document.getElementById('first_bill_open_date1');
	var regDate = document.getElementById('reg_date1');
	var firstOpenFromDate = document.getElementById('first_bill_open_date0');
	var regFromDate = document.getElementById('reg_date0');
	var lastFinalizedToDate = document
			.getElementById('last_bill_finalized_date1');
	var lastFinalizedFromDate = document
			.getElementById('last_bill_finalized_date0');
	var dischargeFromDate = document.getElementById('discharge_date0');
	var dischargeToDate = document.getElementById('discharge_date1');

	if (tpaObj != null && tpaObj.value == '') {
		alert("Select TPA name");
		tpaObj.focus();
		return false;
	}
	if (!isPatientTypeSelected()) {
		alert('Select patient type');
		return false;
	}

	var openBills = false;
	for (var i = 0; i < billStatus.length; i++) {
		if (billStatus[i].checked
				&& (billStatus[i].value == 'A' || billStatus[i].value == '')) {
			openBills = true;
			break;
		}
	}

	if (trim(document.getElementById('last_bill_finalized_date0').value) == ''
			&& trim(document.getElementById('last_bill_finalized_date1').value) == ''
			&& trim(document.getElementById('first_bill_open_date0').value) == ''
			&& trim(document.getElementById('first_bill_open_date1').value) == ''
			&& trim(document.getElementById('reg_date0').value) == ''
			&& trim(document.getElementById('reg_date1').value) == ''
			&& trim(document.getElementById('discharge_date0').value) == ''
			&& trim(document.getElementById('discharge_date1').value) == '') {
		alert("Enter any date range");
		return false;
	}

	/**
	 * If the bill status filter option selected is All/Open then validate bill
	 * open date. If the filter option is only finalized then search for claims
	 * which have only finalized bills and validate finalized date
	 */

	if (!openBills) {
		if (lastFinalizedToDate.value == ''
				|| lastFinalizedFromDate.value == '') {
			alert('Enter the last bill finalized date');
			lastFinalizedToDate.focus();
			return false;
		}
	} else {
		if (firstOpenToDate.value == '' && regDate.value == ''
				&& dischargeToDate.value == '') {
			if (firstOpenFromDate.value != '' && regFromDate.value == ''
					&& dischargeFromDate.value == '') {
				alert('Enter first bill open to date');
				firstOpenToDate.focus();
			} else if (regFromDate.value != '') {
				alert('Enter the registration to date');
				regDate.focus();
			} else if (dischargeFromDate != '') {
				alert('Enter the discharge to date');
				dischargeToDate.focus();
			}
			return false;
		}
	}
	// check that no time is being sent without a date
	var regFromTime = document.getElementById('reg_time0').value
	var regToTime = document.getElementById('reg_time1').value
	var lastFinalizedFromTime = document
			.getElementById('last_bill_finalized_time0').value
	var lastFinalizedToTime = document
			.getElementById('last_bill_finalized_time1').value
	var firstOpenFromTime = document.getElementById('first_bill_open_time0').value
	var firstOpenToTime = document.getElementById('first_bill_open_time1').value
	var dischargeFromTime = document.getElementById('discharge_time0').value
	var dischargeToTime = document.getElementById('discharge_time1').value
	var showError = false;
	if ((regFromTime && !regFromDate.value) || (regToTime && !regDate.value)
			|| (lastFinalizedFromTime && !lastFinalizedFromDate.value)
			|| (lastFinalizedToTime && !lastFinalizedToDate.value)
			|| (firstOpenFromTime && !firstOpenFromDate.value)
			|| (firstOpenToTime && !firstOpenToDate.value)
			|| (dischargeFromTime && !dischargeFromDate.value)
			|| (dischargeToTime && !dischargeToDate.value)) {
		showError = true;
	}
	if (showError) {
		alert('Enter a correct date range');
		return false;
	}

	if (!validateDates())
		return false;

	if (!validateTimes())
		return false;

	document.submissionform.submit();
}

function validateDates() {
	if (!doValidateDateField(document
			.getElementById('last_bill_finalized_date0'))) {
		return false;
	}
	if (!doValidateDateField(document
			.getElementById('last_bill_finalized_date1'))) {
		return false;
	}
	if (!doValidateDateField(document.getElementById('first_bill_open_date0'))) {
		return false;
	}
	if (!doValidateDateField(document.getElementById('first_bill_open_date1'))) {
		return false;
	}
	if (!doValidateDateField(document.getElementById('reg_date0'))) {
		return false;
	}
	if (!doValidateDateField(document.getElementById('reg_date1'))) {
		return false;
	}
	if (!doValidateDateField(document.getElementById('discharge_date0'))) {
		return false;
	}
	if (!doValidateDateField(document.getElementById('discharge_date1'))) {
		return false;
	}
	return true;
}

function validateTimes() {
	if (!validateTime(document.getElementById('reg_time0'))) {
		return false;
	}
	if (!validateTime(document.getElementById('reg_time1'))) {
		return false;
	}
	if (!validateTime(document.getElementById('last_bill_finalized_time0'))) {
		return false;
	}
	if (!validateTime(document.getElementById('last_bill_finalized_time1'))) {
		return false;
	}
	if (!validateTime(document.getElementById('first_bill_open_time0'))) {
		return false;
	}
	if (!validateTime(document.getElementById('first_bill_open_time1'))) {
		return false;
	}
	if (!validateTime(document.getElementById('discharge_time0'))) {
		return false;
	}
	if (!validateTime(document.getElementById('discharge_time1'))) {
		return false;
	}
	return true;
}

function defaultRegTimeFrom() {
	if (document.getElementById('reg_date0').value != '') {
		document.getElementById('reg_time0').value = '00:00';
	} else {
		document.getElementById('reg_time0').value = '';
	}
}
function defaultRegTimeTo() {
	if (document.getElementById('reg_date1').value != '') {
		document.getElementById('reg_time1').value = '23:59';
	} else {
		document.getElementById('reg_time1').value = '';
	}
}
function defaultLastFinalizedTimeFrom() {
	if (document.getElementById('last_bill_finalized_date0').value != '') {
		document.getElementById('last_bill_finalized_time0').value = '00:00';
	} else {
		document.getElementById('last_bill_finalized_time0').value = '';
	}
}
function defaultLastFinalizedTimeTo() {
	if (document.getElementById('last_bill_finalized_date1').value != '') {
		document.getElementById('last_bill_finalized_time1').value = '23:59';
	} else {
		document.getElementById('last_bill_finalized_time1').value = '';
	}
}
function defaultFirstBillOpenTimeFrom() {
	if (document.getElementById('first_bill_open_date0').value != '') {
		document.getElementById('first_bill_open_time0').value = '00:00';
	} else {
		document.getElementById('first_bill_open_time0').value = '';
	}
}
function defaultFirstBillOpenTimeTo() {
	if (document.getElementById('first_bill_open_date1').value != '') {
		document.getElementById('first_bill_open_time1').value = '23:59';
	} else {
		document.getElementById('first_bill_open_time1').value = '';
	}
}
function defaultDischargeTimeFrom() {
	if (document.getElementById('discharge_date0').value != '') {
		document.getElementById('discharge_time0').value = '00:00';
	} else {
		document.getElementById('discharge_time0').value = '';
	}
}
function defaultDischargeTimeTo() {
	if (document.getElementById('discharge_date1').value != '') {
		document.getElementById('discharge_time1').value = '23:59';
	} else {
		document.getElementById('discharge_time1').value = '';
	}
}
