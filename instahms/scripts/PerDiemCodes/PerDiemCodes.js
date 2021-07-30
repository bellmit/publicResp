
var toolBar = {
	Edit : {
		title : 'View/Edit',
		imageSrc : 'icons/Edit.png',
		href  : 'master/PerDiemCodes.do?_method=show',
		onclick : null,
		description : 'View and/or Edit Per Diem Code Details'
	}
};

function setPerdiemDescription() {
	var perdiemCode = document.getElementById('per_diem_code').value;
	var perdiem = findInList(perdiemCodesListJSON, "per_diem_code", perdiemCode);
	if (!empty(perdiem)) {
		document.getElementById('per_diem_description').value = perdiem.code_desc;
		document.getElementById('per_diem_description').title = perdiem.code_desc;
	}else {
		document.getElementById('per_diem_description').value = "";
		document.getElementById('per_diem_description').title = "";
	}
}

function validate() {
	if (trim(document.getElementById('per_diem_code').value)=="") {
		alert("Per diem code is required");
		document.getElementById('per_diem_code').focus();
		return false;
	}
	if (trim(document.getElementById('per_diem_description').value)=="") {
		alert("Per diem code description is required");
		document.getElementById('per_diem_description').focus();
		return false;
	}

	var subgrpsObj = document.getElementById('service_groups_incl');
	var subgrpsSelected = false;
	for (var i=0; i<subgrpsObj.options.length; i++) {
		if (subgrpsObj.options[i].selected) {
			subgrpsSelected = true;
			break;
		}
	}
	if (!subgrpsSelected) {
		alert("Select atleast one Included Service Groups.");
		subgrpsObj.focus();
		return false;
	}

	document.perDiemCodesForm.submit();
	return true;
}

function fillAllValuesForAdd() {
	if (document.perDiemCodesForm._method.value == 'create') {
		var checkObj = document.perDiemCodesForm.checkbox;
		var tableName = "perDiemCharges";
		checkObj.checked = true;
		fillAllValues(tableName, checkObj);
	}
}

function getChargesForNewRatePlan() {
	document.forms[0]._method.value = "show";
	document.forms[0].submit();
}