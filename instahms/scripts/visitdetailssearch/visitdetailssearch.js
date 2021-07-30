var extraDetails = [];
var psAc = null;
function initVisitToolbar(toolbarOptions) {
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	document.getElementById('_mr_no').checked = true;
	var toolbar = {};
	toolbar.VisitEdit = { title: toolbarOptions["editvisit"]["name"], imageSrc: "icons/Edit.png",
		href: 'pages/registration/editvisitdetails.do?_method=getPatientVisitDetails&ps_status=active',
		description: toolbarOptions["editvisit"]["description"],
		target: '_blank',
		show: (visitDetails == 'A')};
	toolbar.PatientEdit = { title: toolbarOptions["editpatient"]["name"], imageSrc: "icons/Edit.png",
		href: 'Registration/GeneralRegistration.do?_method=show&regType=regd',
		description: toolbarOptions["editpatient"]["description"],
		target: '_blank',
		show: (regGeneral == 'A')};
	toolbar.Readmit = { title: toolbarOptions["readmit"]["name"], imageSrc: 'icons/Edit.png',
		href: 'pages/registration/readmit.do?_method=getReadmitScreen',
		target: '_blank',
		show: (readmit == 'A')};
	toolbar.Discharge = { title: toolbarOptions["discharge.summary"]["name"], imageSrc: "icons/Edit.png",
		href: 'dischargesummary/discharge.do?_method=addOrEdit',
		target: '_blank',
		onclick: 'changeDischargeURL',
		show: (dischargeSummary == 'A')};
	toolbar.TreatmentSheet = {title: toolbarOptions["treatment.sheet"]["name"], imageSrc: "icons/Print.png",
		href: 'dischargesummary/treatmentSheetPrint.do?_method=print',
		target: 'blank',
		show: (dischargeSummary == 'A')};
	toolbar.Order = { title: toolbarOptions["order"]["name"],   imageSrc: "icons/Order.png",
		href: 'patients/orders',
		target: '_blank',
		show: (opOrder == 'A'|| ipOrder=='A')};
	toolbar.UpdateMLC = { title: toolbarOptions["update.mlc"]["name"], imageSrc: "icons/Order.png",
		href: 'MLCDocuments/MLCDocumentsAction.do?_method=show',
		target: '_blank'};
	toolbar.Coding = { title: toolbarOptions["codification"]["name"], imageSrc: "icons/Edit.png",
		href: 'pages/medicalrecorddepartment/MRDUpdate.do?_method=getMRDUpdateScreen',
		target: '_blank'};

	createToolbar(toolbar);

	if (mod_adt == 'Y')
		initBedNamesAutoComplete();
	else
		initBedTypesAutoComplete();
	initWardNamesAutoComplete();

	if ( document.getElementById('resultTable') != null)
		initTooltip('resultTable', extraDetails);
}

function changeDischargeURL( anchor, params, id, toolbar ) {
	anchor.href = 'inpatients/dischargesummary/index.htm#/filter/default/patient/' + encodeURIComponent(params.mr_no) + '/dischargesummary?retain_route_params=true';
}

function initBedTypesAutoComplete() {
	datasource = new YAHOO.util.LocalDataSource({result: bedTypes});
	datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : 'result',
		fields : [ 	{key : 'bed_type_name'} ]
	};

	var bedTypeAutoComplete = new YAHOO.widget.AutoComplete('exclude_in_qb_reg_bed_type','bedTypeContainer', datasource);
	bedTypeAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
	bedTypeAutoComplete.useShadow = true;
	bedTypeAutoComplete.minQueryLength = 0;
	bedTypeAutoComplete.autoHighlight = false;
	bedTypeAutoComplete.allowBrowserAutocomplete = false;
	bedTypeAutoComplete.forceSelection = true;
	bedTypeAutoComplete.resultTypeList = false;
	bedTypeAutoComplete.maxResultsDisplayed = 20;
	if (mod_adt != 'Y' && bedTypeAutoComplete._elTextbox.value != '') {
		bedTypeAutoComplete._bItemSelected = true;
		bedTypeAutoComplete._sInitInputValue = bedTypeAutoComplete._elTextbox.value;
	}
}

function initBedNamesAutoComplete() {
	datasource = new YAHOO.util.LocalDataSource({result: bedNames});
	datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : 'result',
		fields : [ 	{key : 'bed_name'},
					{key : 'bed_id'},
					{key : 'ward_name'}
				 ]
	};

	var bedNameAutoComplete = new YAHOO.widget.AutoComplete('exclude_in_qb_bed_name','bedNameContainer', datasource);
	bedNameAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
	bedNameAutoComplete.useShadow = true;
	bedNameAutoComplete.minQueryLength = 0;
	bedNameAutoComplete.autoHighlight = false;
	bedNameAutoComplete.allowBrowserAutocomplete = false;
	bedNameAutoComplete.forceSelection = true;
	bedNameAutoComplete.resultTypeList = false;
	bedNameAutoComplete.maxResultsDisplayed = 20;
	bedNameAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
		var bed = oResultData;
		return bed.bed_name + " (" +bed.ward_name + ")";
	}
	if (mod_adt == 'Y' && bedNameAutoComplete._elTextbox.value != '') {
		bedNameAutoComplete._bItemSelected = true;
		bedNameAutoComplete._sInitInputValue = bedNameAutoComplete._elTextbox.value;
	}

	bedNameAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.exclude_in_qb_alloc_bed_no.value = elItem[2].bed_id;
	});

	bedNameAutoComplete.selectionEnforceEvent.subscribe(function(){
			searchForm.exclude_in_qb_alloc_bed_no.value = '';
	});
}

function initWardNamesAutoComplete() {
	datasource = new YAHOO.util.LocalDataSource({result: wardNames});
	datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : 'result',
		fields : [ 	{key : 'ward_name'},
					{key : 'ward_no'}
				 ]
	};

	var wardAutoComplete = new YAHOO.widget.AutoComplete('exclude_in_qb_ward_name','wardContainer', datasource);
	wardAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
	wardAutoComplete.useShadow = true;
	wardAutoComplete.minQueryLength = 0;
	wardAutoComplete.autoHighlight = false;
	wardAutoComplete.allowBrowserAutocomplete = false;
	wardAutoComplete.forceSelection = true;
	wardAutoComplete.resultTypeList = false;
	wardAutoComplete.maxResultsDisplayed = 20;
	if (wardAutoComplete._elTextbox.value != '') {
		wardAutoComplete._bItemSelected = true;
		wardAutoComplete._sInitInputValue = wardAutoComplete._elTextbox.value;
	}

	wardAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
		if (mod_adt == 'Y')
			searchForm.exclude_in_qb_alloc_ward_no.value = elItem[2].ward_no;
		else
			searchForm.exclude_in_qb_reg_ward_no.value = elItem[2].ward_no;
	});

	wardAutoComplete.selectionEnforceEvent.subscribe(function(){
		if (mod_adt == 'Y')
			searchForm.exclude_in_qb_alloc_ward_no.value = '';
		else
			searchForm.exclude_in_qb_reg_ward_no.value = '';
	});
}

function validateSearchForm() {

	if (document.getElementById('exclude_in_qb_ward_name').value == '') {
		if (mod_adt == 'Y')
			searchForm.exclude_in_qb_alloc_ward_no.value = '';
		else
			searchForm.exclude_in_qb_reg_ward_no.value = '';
	}
	if (mod_adt == 'Y' && document.getElementById('exclude_in_qb_bed_name').value == '') {
		searchForm.exclude_in_qb_alloc_bed_no.value = '';
	}

	var regFTime = searchForm._reg_time[0].value;
	var regFDate = searchForm._reg_date[0].value;
	var regToTime = searchForm._reg_time[1].value;
	var regToDate = searchForm._reg_date[1].value;

	if (regFDate != '') {
		if (!doValidateDateField(searchForm._reg_date[0])) {
			searchForm._reg_date[0].focus();
			return false;
		}
	}
	if (regToDate != '') {
		if (!doValidateDateField(searchForm._reg_date[1])) {
			searchForm._reg_date[1].focus();
			return false;
		}
	}
	if (regFTime != '') {
		if (!validateTime(searchForm._reg_time[0])) {
			searchForm._reg_time[0].focus();
			return false;
		}
	}
	if (regToTime != '') {
		if (!validateTime(searchForm._reg_time[1])) {
			searchForm._reg_time[1].focus();
			return false;
		}
	}


	if (regFDate == '' && regFTime != '') {
		showMessage("js.search.patient.visit.admission.from.date.required");
		searchForm._reg_date[0].focus();
		return false;
	}

	if (regToDate == '' && regToTime != '') {
		showMessage("js.search.patient.visit.admission.to.date.required");
		searchForm._reg_date[1].focus();
		return false;
	}
	if (regFDate != '' && regFTime == '' && regToTime != '') {
		var d = new Date();
		regFTime = d.getHours() + ":" + d.getMinutes();
	}
	if (regToDate != '' && regToTime == '' && regFTime != '') {
		var d = new Date();
		regToTime = d.getHours() + ":" +d.getMinutes();
	}

	var regFDateTime = '';
	if (regFDate != '')
		regFDateTime = regFDate;
	if (regFTime != '')
		regFDateTime += ' ' + regFTime + ":00";

	var regTDateTime = '';
	if (regToDate != '')
		regTDateTime = regToDate;
	if (regToTime != '')
		regTDateTime += ' ' + regToTime + ":00";

	searchForm.visit_reg_date[0].value = regFDateTime;
	searchForm.visit_reg_date[1].value = regTDateTime;
	document.getElementById('visit_reg_date@op').value = 'ge,le';
	document.getElementById('visit_reg_date@type').value = (regFTime == '' && regToTime == '') ? 'date' : 'timestamp';

	if(!validateRegNumericFields())
		return false;

	return true;
}

function changeDatevalues() {
	if (searchForm.visit_reg_date[0].value != "" && searchForm.visit_reg_date[0].value != null)
	{

		var regFDate = searchForm.visit_reg_date[0].value;
		var regTDate = searchForm.visit_reg_date[1].value;
		var regFDateArr = regFDate.split(" ");
		var regTDateArr = regTDate.split(" ");

		if (regFDateArr[1] == undefined) {
			document.getElementById('reg_date0').value = regFDateArr[0];
			document.getElementById('reg_date1').value = regTDateArr[0];
		} else {
			var regFTime = regFDateArr[1].substring(0, regFDateArr[1].lastIndexOf(":"));
			var regTTime = regTDateArr[1].substring(0, regTDateArr[1].lastIndexOf(":"));
			document.getElementById('reg_date0').value = regFDateArr[0]
			document.getElementById('reg_date1').value = regTDateArr[0];
			document.getElementById('_reg_time0').value = regFTime;
			document.getElementById('_reg_time1').value = regTTime;
		}
	}
 }

/** Changing The status field depends upon the mr_no field if Active only checkbox is checked then status field will
automattically checked with active checkbox and uncheck of Active only checkbox All cheeckbox will checked.
And depending upon the Active Only Checkbox we are showing the mr_no Autocomplete. **/

 function changeStatus() {
 	var status = '';

	if (document.getElementById('_mr_no').checked) {
		status = 'active';
	} else {
		status = 'all';
	}
	if (status == 'active') {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	} else {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	}
 }

 function setExtraDetails(icuBeds, prevBeds, retBeds, curBed, byStanderBeds, rowId, DischargeDate) {
	icuBeds = icuBeds.replace(/,\s|,|\s,/g, '|');
	var regExp = new RegExp('(' + icuBeds + ')', 'g');
	curBed = curBed.replace(regExp, '<span style="color: red" >$1</span>');
	prevBeds = prevBeds.replace(regExp, '<span style="color: red">$1</span>');
	retBeds = retBeds.replace(regExp, '<span style="color: red">$1</span>');
	byStanderBeds = byStanderBeds.replace(regExp, '<span style="color: red">$1</span>');

	//extraDetails[rowId] = {'Current Bed': curBed, 'Retain Bed(s)': retBeds, 'Previous Bed(s)' : prevBeds, 'Bystander Beds': byStanderBeds, 'Previous Visit Discharge Date': prevDischargeDate };
	extraDetails[rowId] = {};
	extraDetails[rowId][getString("js.search.patient.visit.tooltip.current.bed")] = curBed;
	extraDetails[rowId][getString("js.search.patient.visit.tooltip.retain.bed")] = retBeds;
	extraDetails[rowId][getString("js.search.patient.visit.tooltip.prev.bed")] = prevBeds;
	extraDetails[rowId][getString("js.search.patient.visit.tooltip.bystander.bed")] = byStanderBeds;
	extraDetails[rowId][getString("js.search.patient.visit.tooltip.discharge.date")] = DischargeDate;

}
