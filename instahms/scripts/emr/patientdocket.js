function selectAll() {
	var printCheckBoxs = document.getElementsByName("printDocument");
	for (var i=0; i<printCheckBoxs.length; i++) {
		if (!printCheckBoxs[i].checked && !printCheckBoxs[i].disabled) {
			printCheckBoxs[i].checked = true;
		}
	}
}

function unSelectAll() {
	var printCheckBoxs = document.getElementsByName("printDocument");
	for (var i=0; i<printCheckBoxs.length; i++) {
		if (printCheckBoxs[i].checked) {
			printCheckBoxs[i].checked = false;
		}
	}
}

function checkDocuments() {
	var printCheckBoxs = document.getElementsByName("printDocument");
	var enabledBoxes = false;
	var printDocket = false;
	var params = '';
	for (var i=0; i<printCheckBoxs.length; i++) {
		if (printCheckBoxs[i].checked) {
			params += "&printDocument="+printCheckBoxs[i].value;
		}
		if (!printCheckBoxs[i].disabled) {
			enabledBoxes = true;
		}
	}
	if (params != '') {
		// user selected documents for print.
		var url = contextPath + "/emr/PatientDocket.do?_method=printDocket"+params;
		window.open(url);
	} else {
		if (enabledBoxes) {
			alert("Please select atleast one document for docket print");
			return false;
		} else {
			alert("Sorry no documents available for docket print");
			return false;
		}
	}
}

function checkOrUncheckAll(checkBoxName, object) {
	var checkBox = document.getElementsByName(checkBoxName);
	for(var i=0; i<checkBox.length; i++) {
		if (checkBox[i].disabled == false)
		checkBox[i].checked = object.checked;
	}
}