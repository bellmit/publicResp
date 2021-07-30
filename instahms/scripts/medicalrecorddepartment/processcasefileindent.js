
function init() {
initMrdUserNameAutoComplete();
}

function initMrdUserNameAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(mrdUserNameList);
		oAutoComp = new YAHOO.widget.AutoComplete('mrd_issued_to', 'issuedToContainer', dataSource);
		oAutoComp.maxResultsDisplayed = 5;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
	}
}

function checkFiles() {
	var fileElmts = document.forms[0].fileCheck;
	for(var i=0;i<fileElmts.length;i++) {
		if(fileElmts[i].checked) return true;
	}	
	return false;
}
function onFileCheck(fileCheck, hiddenMrnoElmt) {
	if(fileCheck.checked) {
		document.getElementById(hiddenMrnoElmt).value = fileCheck.value;
	}else {
		document.getElementById(hiddenMrnoElmt).value = "";
	}
}


function validate(screen) {
	for(var i=0;i<document.forms[0].selectFiles.length;i++) {
		if(document.forms[0].selectFiles[i].checked &&
			(document.forms[0].selectFiles[i].value == 'singleFile' || 
				 document.forms[0].selectFiles[i].value == 'pageFiles') ) {
			if(!checkFiles()) {
				alert("Please select any case file");
				return false;
			}
		}
	}
	if (screen == "issue"){
		if(trim(document.forms[0].mrd_issued_to.value) == '') {
			alert("Please enter issued to");
			document.forms[0].mrd_issued_to.focus();
			return false;
		}
		document.forms[0].button.value = 'issue';
	}else{
		if (document.getElementById("remarks").value == ''){
			alert("Enter Remarks ");
			document.getElementById("remarks").focus();
			return false;
		}
		document.forms[0].button.value = 'closeIndent';
	}

	document.forms[0].method.value= 'processCasefileIndent';
	document.forms[0].submit();
}

function onCheckRadio(val) {
	var fileElmts = document.forms[0].fileCheck;
	if(fileElmts.length != undefined) {
		if(val == 'singleFile') {
			for(var i=0;i<fileElmts.length;i++) {
				fileElmts[i].disabled = false;
				fileElmts[i].checked = false;
				document.forms[0].hiddenMrno[i].value = "";
			}
		}else if(val == 'pageFiles') {
			for(var i=0;i<fileElmts.length;i++) {
				fileElmts[i].checked = true;
				fileElmts[i].disabled = false;
				document.forms[0].hiddenMrno[i].value = fileElmts[i].value;
			}
		}else {
			for(var i=0;i<fileElmts.length;i++) {
				fileElmts[i].disabled = true;
				fileElmts[i].checked = true;
				document.forms[0].hiddenMrno[i].value = fileElmts[i].value;
			}
		}
	}else {
		if(val == 'singleFile') {
			fileElmts.disabled = false;
			fileElmts.checked = false;
			document.forms[0].hiddenMrno.value = "";
		}else if(val == 'pageFiles') {
			fileElmts.checked = true;
			fileElmts.disabled = false;
			document.forms[0].hiddenMrno.value = fileElmts.value;
		}else {
			fileElmts.disabled = true;
			fileElmts.checked = true;
			document.forms[0].hiddenMrno.value = fileElmts.value;
		}
	}
}


function printPatientsList(){
	var dept = document.getElementById("dep_unit_name").value;
	var name = document.getElementById("requested_by").value;
	var fdate = document.forms[0].request_date0.value;
	var ftime = document.forms[0].request_time0.value;
	var tdate = document.forms[0].request_date1.value;
	var ttime = document.forms[0].request_time1.value;
	
	var params = '&dep_unit_name=' +dept
	params += '&requested_by=' +name;
	params += '&request_date0=' +fdate;
	params += '&request_date1=' + tdate;
	params += '&request_time0=' + ftime;
	params += '&request_time1=' + ttime;
	
	var url = 'ProcessMRDCasefileIndent.do?method=printCasefileIndent'+params;
	window.open(url);
}


function validateIndent(){
//	var fdate = document.getElementById("request_date0");
	var tdate = document.getElementById("request_date1");
	var ftime = document.getElementById("request_time0");
	var ttime = document.getElementById("request_time1");
	var msg = null;

	if (fdate.value != "" && ftime.value == ""){
		alert("Enter from Time");
		document.getElementById("request_time0").focus();
		return false;
	}

	if (!validateTime(ftime)){
		document.getElementById("request_time0").focus();
		return false;
	}

	if (tdate.value != "" && ttime.value == ""){
		alert("Enter to Time");
		document.getElementById("request_time1").focus();
		return false;
	}

	if (!validateTime(ttime)){
		document.getElementById("request_time1").focus();
		return false;
	}

		alert("fdate.value==="+fdate.value+"-== todate value"+tdate.value);
	if(fdate.value != "" || tdate.value != "" ){
		msg = validateDateStr(document.getElementById("request_date0").value, "past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		msg = validateDateStr(document.getElementById("request_date1").value, "future");
		alert(msg);
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}

		if (getDateDiff(document.getElementById("request_date0").value, 
					document.getElementById("request_date1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}
	}
	return true;
}
