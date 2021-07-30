
var searchForm = null;


function init() {
	searchForm = document.searchForm;
	enablePatientStatus();
	enablePatientType();
	initMrNoAutoComplete(cpath);
	createToolbar(toolbarArray);
}

function clearSearch(){
	searchForm.fdate.value = "";
	searchForm.tdate.value = "";
	searchForm.firstName.value = "";
	searchForm.lastName.value = "";
	searchForm.phone.value = "";
	clearMultiSelect(searchForm.doctor);
	clearMultiSelect(searchForm.department);
	searchForm.mrno.value="";
	searchForm.statusActive.checked=true;
	searchForm.typeAll.checked=true;
	enablePatientStatus();
	enablePatientType();
}

function clearMultiSelect(el) {
	for (var i=0; i<el.options.length; i++) {
		if (el.options[i].selected) {
			el.options[i].selected = false;
		}
	}
}

function enablePatientStatus(){
	var disabled = searchForm.statusAll.checked;
	searchForm.statusActive.disabled = disabled;
	searchForm.statusInactive.disabled = disabled;
}

function enablePatientType(){
	var disabled = searchForm.typeAll.checked;
	searchForm.typeIP.disabled = disabled;
	searchForm.typeOP.disabled = disabled;
}


function validateSearchForm(){

	if(!doValidateDateField(searchForm.fdate)){
		return false;
	}
	if(!doValidateDateField(searchForm.tdate)){
		return false;
	}
	return true;
}

/*complete the MRNO
 */
function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = searchForm.mrno;

//	complete
		var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		searchForm.mrno.value = ""
			setTimeout("searchForm.mrno.focus()",0);
		return false;
	}
	return true;
}

