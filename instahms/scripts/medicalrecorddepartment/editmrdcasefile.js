
function doClose() {
	var screen = '${param.mrdscreen}';
	if(screen == 'issue') {
		window.location.href = '${cpath}/medicalrecorddepartment/MRDCaseFileIssue.do?_method=list&mrdscreen='+screen+'&case_status=&visit_status=A&sortReverse=false&sortOrder=mr_no';
	}else {
		window.location.href = '${cpath}/medicalrecorddepartment/MRDCaseFileReturn.do?_method=list&mrdscreen='+screen+'&case_status=&visit_status=A&sortReverse=false&sortOrder=mr_no';
	}
}

function changeApprovalReject(value,id) {
	document.getElementById("approve_reject"+id).value = value;
}

	function validateFields() {
		if (!doValidateDateField(document.forms[0].issued_date))
			return false;

		if (document.forms[0].issued_date.value == "")  {
			alert("Issue Date is required");
			document.forms[0].issued_date.focus();
			return false;
		}
		if(!doValidateDateField(document.forms[0].issued_date, "past")){
			return false;
		}
		if (trim(document.forms[0].issued_to.value) == "")  {
			alert("Issued To is required");
			document.forms[0].issued_to.focus();
			return false;
		}
		if (document.forms[0].purpose.value == "")  {
			alert("Purpose is required");
			document.forms[0].purpose.focus();
			return false;
		}
		if(document.forms[0].mrd_available != null &&
				document.forms[0].mrd_available.checked &&
				document.forms[0].remarks.value == "") {
			alert("Please enter remarks");
			document.forms[0].remarks.focus();
			return false;
		}
		return true;
	}

function onSaveValidate() {
	if(document.forms[0].caseStatus != null && document.forms[0].caseStatus.checked) {
		document.forms[0].case_status.value = "I";
	}else document.forms[0].case_status.value = "";
	document.forms[0].submit();
}


function initCaseFileUserNameAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(mrdUserNameList);
		oAutoComp = new YAHOO.widget.AutoComplete('issued_to', 'issuedToDropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 5;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = false;
	}
}

function setMRDReturn() {
	if(document.forms[0].mrd_available.checked) {
		document.forms[0].mrdReturn.value = "Y";
	} else {
		document.forms[0].mrdReturn.value = "N";
	}
}

function backToSearch(){
	 window.location.href="./MRDCaseFileSearch.do?_method=searchCasefiles&_visit_type=o&_visit_status=A";
}

