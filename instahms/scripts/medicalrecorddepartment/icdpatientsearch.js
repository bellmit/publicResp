
var searchForm ;
function init(){
 searchForm=document.icdSearchForm;
 initMrNoAutoComplete(cpath);
 createToolbar(toolbar);
 showFilterActive(document.icdSearchForm);
 initArr();
}

var tpaArray = null;
var selectOptn = new Array("(All)", "");
function initArr() {

	tpaArray = new Array();
	tpaArray[0] = selectOptn;

	var j = 1;
   	for (var n=0; n<tpaList.length; n++) {
   		tpaArray[j] = new Array(tpaList[n].tpa_name, tpaList[n].tpa_id);
    	j++;
   	}
}

function clearSearch(){
	document.getElementById("reg_date1") = "";
	document.getElementById("reg_date2") = "";
	document.getElementById("discharge_date1") = "";
	document.getElementById("discharge_date2") = "";
	searchForm.mr_no.value="";
	searchForm.diagnosis_icd.value="";
	searchForm.treatment_icd.value="";
}


function validateSearchForm(){

	if(!doValidateDateField(document.getElementById("reg_date1"))){
		return false;
	}
	if(!doValidateDateField(document.getElementById("reg_date2"))){
		return false;
	}
	if(!doValidateDateField(document.getElementById("discharge_date1"))){
		return false;
	}
	if(!doValidateDateField(document.getElementById("discharge_date2"))){
		return false;
	}
	return true;
}

function onKeyPressMrno(e){
	if(isEventEnterOrTab(e)){
		return onChangeMrno();
	}else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = searchForm.mr_no;
	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		searchForm.mr_no.value = ""
			setTimeout("searchForm.mrno.focus()",0);
		return false;
	}
	return true;
}

function printSearch() {
	if(!validateSearchForm()) return false;
	var url = window.location.href;
	url = url.replace('searchICDPatients', 'print');
	url = url.split('ICDPatientSearch.do?');
	window.open("ICDPatientSearch.do?" + url[1]);
}

function doSearch(){
	searchForm.method.value = "searchICDPatients";
	searchForm.submit();
}

function checkAssignee() {
	if (!selectRecords())
		return false;
	var assignTo = document.icdSelectForm._assign_to.value;
	if (assignTo == null || assignTo == '') {
		alert("assign to is required.");
		document.icdSelectForm._assign_to.focus();
		return false;
	}
	document.icdSelectForm._method.value = "saveCodification";
	document.icdSelectForm.submit();
	return true;
}

function checkCompleted() {
	var selectedPatientCheckObj = document.icdSelectForm._selectedPatient;
	var cStatuses = document.icdSelectForm._patient_codification_status;
	var codificationInProgress = false;
	var verificationRequired = false;
	var anyChecked = false;
	if (selectedPatientCheckObj.length) {
		for (var i=0; i<selectedPatientCheckObj.length; i++) {
			if (selectedPatientCheckObj[i].checked) {
				if (cStatuses[i].value == 'C') {
					anyChecked = true;
				} else if (cStatuses[i].value == 'P') {
					codificationInProgress = true;
					break;
				} else if (cStatuses[i].value == 'R') {
					verificationRequired = true;
					break;
				}

			}
		}
	} else {
		 if (document.icdSelectForm._selectedPatient.checked) {
			if (cStatuses.value == 'C') {
				anyChecked = true;
			} else if (cStatuses.value == 'P') {
				codificationInProgress = true;
			} else if (cStatuses.value == 'R') {
				verificationRequired = true;
			}
		}
	}
	if (codificationInProgress) {
		alert('Codification in progress for selected case.');
		return false;
	}
	if (verificationRequired) {
		alert('Codification to be verified.');
		return false;
	}
	if (!anyChecked) {
		alert("Please select one or more patients which are in completed status.");
		return false;
	}

	document.icdSelectForm._method.value = "codificationComplete";
	document.icdSelectForm.submit();
	return true;
}

function checkReopenForCodification() {
	var selectedPatientCheckObj = document.icdSelectForm._selectedPatient;
	var statuses = document.icdSelectForm._patient_codification_status;
	var anyChecked = false;
	if (selectedPatientCheckObj.length) {
		for (var i=0; i<selectedPatientCheckObj.length; i++) {
			if (selectedPatientCheckObj[i].checked && statuses[i].value == 'V') {
				anyChecked = true;
			} else {
				selectedPatientCheckObj[i].checked = false;
			}
		}
	} else {
		 if (document.icdSelectForm._selectedPatient.checked && statuses.value == 'V') {
		 	anyChecked = true;
		 } else {
		 	document.icdSelectForm._selectedPatient.checked = false;
		 }
	}

	if (!anyChecked) {
		alert("Please select one or more patients which are in verified and closed status only.");
		return false;
	}

	document.icdSelectForm._method.value = "reopenForCodification";
	document.icdSelectForm.submit();
	return true;
}


function selectRecords() {
	var selectedPatientCheckObj = document.icdSelectForm._selectedPatient;
	var anyChecked = false;
	if (selectedPatientCheckObj.length) {
		for (var i=0; i<selectedPatientCheckObj.length; i++) {
			if (selectedPatientCheckObj[i].checked){
				anyChecked = true;
			}
		}
	} else {
		 anyChecked = document.icdSelectForm._selectedPatient.checked;
	}

	if (!anyChecked) {
		alert("Please select one or more patients");
		return false;
	}
	return true;
}

function getSelectedCategories() {
	var catObj		= document.getElementById('primary_category_id');
	var catList = new Array();
	var len = catObj.options.length;
	for (var i=0;i<len;i++) {
		catList.push(catObj.options[i].value)
	}
	return catList;
}

function getAjaxList(insCompId) {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	var url = cpath
			+ '/pages/medicalrecorddepartment/ICDPatientSearch.do?_method=getdetailsAJAX'
			+ '&primary_insurance_co_id=' + insCompId;

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function onChangePrimaryInsuranceCompany() {

	var insCompObj	= document.getElementById('primary_insurance_co_id');
	var tpaObj		= document.getElementById('primary_sponsor_id');
	var catObj		= document.getElementById('primary_category_id');

	var insCompId	= insCompObj.value;
	var tpaId		= tpaObj.value;
	var catId		= catObj.value;

	if (insCompId != '') {
		var insCompTpaDetails = filterList(companyTpaList, 'insurance_co_id', insCompId);

		if (empty(insCompTpaDetails))
			loadSelectBox(tpaObj, tpaList, 'tpa_name', 'tpa_id' , '(All)');
		else
			loadSelectBox(tpaObj, insCompTpaDetails, 'tpa_name', 'tpa_id' , '(All)');

		sortDropDown(insCompObj, "(All)");
		sortDropDown(tpaObj, "(All)");

		setSelectedIndex(tpaObj, tpaId);
		setSelectedIndex(insCompObj, insCompId);
		if (catObj != null) {
			 ajaxDetails = getAjaxList(insCompId);
			 inscategoryName = ajaxDetails.inscatName;
			 if (!empty(inscategoryName)){
				 var insuranceCategoryList = filterList(inscategoryName, 'insurance_co_id', insCompId);
				 loadSelectBox(catObj, insuranceCategoryList, 'category_name', 'category_id', '(All)');
			 }
			 sortDropDown(catObj,"(All)");
			 setSelectedIndex(catObj,catId);
		}
	}else {
		if (tpaId == '')
			loadPrimaryTpaArray();
		
		if (insCompId == '') {
			loadCategoryArray();
		}
	}

}


function onChangePrimaryTPA() {
	var insCompObj	= document.getElementById('primary_insurance_co_id');
	var tpaObj		= document.getElementById('primary_sponsor_id');

	var insCompId	= insCompObj.value;
	var tpaId		= tpaObj.value;

	if (tpaId != '') {

		setSelectedIndex(insCompObj, insCompId);
		setSelectedIndex(tpaObj, tpaId);

	}else {
		if (insCompId == '') {
			loadPrimaryTpaArray();
		}
	}
}

function loadPrimaryTpaArray() {
	var tpaObj		= document.getElementById('primary_sponsor_id');
	var tpaId		= tpaObj.value;
	var len = 1;
	for (var n=0; n<tpaArray.length; n++) {
		tpaObj.options.length = len;
		tpaObj.options[len - 1] = new Option(tpaArray[n][0], tpaArray[n][1]);
		len++;
   	}
	sortDropDown(tpaObj, "(All)");
   	setSelectedIndex(tpaObj, tpaId);
}

function setSelectedCategories(catList) {
	var catObj		= document.getElementById('primary_category_id');
	for (var i=0;i<catObj.options.length;i++) {
		for (var j=0;j<catList.length;j++) {
			if (catList[j] == catObj.options[i].value) {
				catObj.options[i].selected = true;
			}
		}
	}
}

function loadCategoryArray() {
	var catObj		= document.getElementById('primary_category_id');
	if (catObj.value != null & catObj.value != "") {
		var catList		= getSelectedCategories();
		for (var n=0; n<categoryArray.length; n++) {
			catObj.options.push(new Option(categoryArray[n][0], categoryArray[n][1]));
	   	}
	   	sortDropDown(catObj);
	   	setSelectedCategories(catList);
   	}
}

function onChangeSecondaryInsuranceCompany() {

}


function onChangeSecondaryTPA() {

}
