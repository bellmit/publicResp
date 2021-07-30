function markArrivedForSelectedAppointments() {		
	var checkBoxes = document.bulkArrivalForm.markAppointmentArrived;	
	var anyChecked = false;
	var appIds = new Array();
	if (checkBoxes.length) {
		var l = 0;
		for(var k=0;k<checkBoxes.length;k++){
			if (checkBoxes[k].checked && !checkBoxes[k].disabled) {
				appIds[l] = checkBoxes[k].value;
				l++;
				anyChecked = true;
			}
		}
	} else {
		appIds = checkBoxes.value;
		if (checkBoxes.checked) anyChecked = true;
	}
	
	if (!anyChecked) {
		showMessage("js.patient.resourcescheduler.bulkarrival.checkoneormore");
		return false;	
	}				
	document.bulkArrivalForm.appIds.value = appIds;
	document.bulkArrivalForm.submit();			
}

function checkOrUncheckAll(elName, obj) {
	var checkBox = document.getElementsByName(elName);
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled) {
			checkBox[i].checked = obj.checked;						
		}
	}
}

function init() {				
	psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	document.getElementById('_mr_no').checked = true;	
	doctorsAutocomp();
}

function clearSearchParameters(form) {
	clearForm(form);
	document.getElementById("doctor").value = "";
}

function validateSearchForm() {
	var fromTime = document.getElementById('appoint_time0');
	var toTime = document.getElementById('appoint_time1');
	if (fromTime.value != null) {
		if (!validateTime(fromTime)) {
			fromTime.focus();
			return false;
		}
	}

	if (toTime.value != null) {
		if (!validateTime(toTime)) {
			toTime.focus();
			return false;
		}
	}
	return true;
}

function changePatientStatus() {	
	var status = '';

	if (document.getElementById('_mr_no').checked) {
		status = 'active';
	} else {
		status = 'all';
	}
	if (status == 'active') {
		psAc1.destroy();
		psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	} else {
		psAc1.destroy();
		psAc1 = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	}
}

function doctorsAutocomp() {
	YAHOO.example.doctorNamesArray = [];
	YAHOO.example.doctorNamesArray.length = docJson.length;
	for (var i = 0; i < docJson.length; i++) {
		var item = docJson[i];
		YAHOO.example.doctorNamesArray[i] = item["DOCTOR_NAME"];
	}

	YAHOO.example.ACJSArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.doctorNamesArray);
		var autoComp = new YAHOO.widget.AutoComplete('doctor_name', 'docDropdown', datasource);
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = true;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = false;
		autoComp.forceSelection = true;
		autoComp.filterResults = Insta.queryMatchWordStartsWith;
		autoComp.formatResult = Insta.autoHighlightWordBeginnings;

		autoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
			var data = document.appointmentSearchForm.doctor_name.value;
			for (var i = 0; i < docJson.length; i++) {
				if (data == docJson[i].DOCTOR_NAME) {
					document.getElementById("doctor").value = docJson[i].DOCTOR_ID;
				}
			}
		});
		
		autoComp.textboxBlurEvent.subscribe(function () {
			if (document.getElementById('doctor_name').value == '') {
				document.getElementById("doctor").value = '';			
			}
		});
		
	}
}