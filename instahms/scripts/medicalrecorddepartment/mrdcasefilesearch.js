var toolbar = {
Edit: {
title: "Edit",
	   imageSrc: "icons/Edit.png",
	   href: 'medicalrecorddepartment/MRDCaseFileIssue.do?_method=editCasefile',
	   onclick: null,
	   description: "Edit and/or Issue MRD case file details"
	  },
History: {
title: "History",
	   imageSrc: "icons/Edit.png",
	   href: 'medicalrecorddepartment/MRDCaseFileIssue.do?_method=view',
	   onclick: null,
	   description: "View MRD Case File Issue Log"
		 }
};

var cpath="${cpath}";

function init() {
	initMrNoAutoComplete(cpath);
	document.getElementById("btnActions").disabled = true;
	if('${param.mrdscreen}' == 'issue') {
		initMrdUserNameAutoComplete();
	}

	createToolbar(toolbar);
	showFilterActive(document.MRDSearchResults);
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



function onFileCheck(fileCheck, hiddenMrnoElmt, id) {
	if(fileCheck.checked) {
		document.getElementById(hiddenMrnoElmt).value = fileCheck.value;
	}else {
		document.getElementById(hiddenMrnoElmt).value = "";
	}
}


function onChangeAction(){
	var action = document.getElementById("action").value;
	var fileChk = document.forms['MRDSearchResults']._hiddenMrno;
	for (var i=0;i<fileChk.length;i++){
		if (fileChk[i].value != null){
			fileChk[i].value = "";
		}
	}
	resetRadioSelect();
	if (action == "indent"){
		enableDisableCasefile(action,"single") ;
		document.getElementById("btnActions").disabled = false;
		document.getElementById("btnActions").textContent = "Indent";
		document.getElementById("btnActions").innerHTML= "<b><u>I</u></b>ndent";
		document.getElementById("btnActions").accessKey = "I";
		document.getElementById("singlecase").checked= true;
	}else if (action == "issue"){
		enableDisableCasefile(action, "single");
		document.getElementById("btnActions").disabled = false;
		document.getElementById("btnActions").textContent = "Issue";
		document.getElementById("btnActions").innerHTML= "<b><u>I</u></b>ssue";
		document.getElementById("btnActions").accessKey = "I";
		document.getElementById("singlecase").checked= true;
	}else if (action == "return"){
		enableDisableCasefile(action, "single");
		document.getElementById("btnActions").disabled = false;
		document.getElementById("btnActions").textContent = "Return";
		document.getElementById("btnActions").innerHTML= "<b><u>R</u></b>eturn";
		document.getElementById("btnActions").accessKey = "R";
		document.getElementById("singlecase").checked= true;
	}else if (action == "close"){
		enableDisableCasefile(action, "single");
		document.getElementById("btnActions").disabled = false;
		document.getElementById("btnActions").textContent = "Close Indent";
		document.getElementById("btnActions").innerHTML= "<b><u>C</u></b>lose Indent";
		document.getElementById("btnActions").accessKey = "C";
		document.getElementById("singlecase").checked= true;
	}else{
		enableDisableCasefile(action, "single");
		document.getElementById("btnActions").disabled = true;
	}
}

function resetRadioSelect(){
	var rdSelect = document.MRDSearchResults._selectItem;
	for (var i=0;i<rdSelect.length; i++){
		rdSelect[i].checked = false;
	}
}

function resetValues(){
	var  chkObj = document.MRDSearchResults._fileCheck;
	for (var i=0;i<chkObj.length;i++){
		if (chkObj[i].checked){
			chkObj[i].checked = false;
		}
	}
}

function enableDisableCasefile(action, val){
	var len = document.MRDSearchResults._fileCheck.length;
	if (len == undefined) len = 1;
	for(var i=0;i<len;i++){
		var fileStatus =  document.getElementById("fileStatus"+i).value;
		var indented = document.getElementById("indented"+i).value;
		var caseStatus = document.getElementById("caseStatus"+i).value;
		var chkObj = document.getElementById("fileCheck"+i);
		if (val == 'all'){
			if (action == "indent"){
				if ((fileStatus == 'A' || fileStatus == 'U') && indented == 'N' && caseStatus != 'I'){
					chkObj.disabled = true;
					chkObj.checked = true;
				}else {
					chkObj.checked = false;
					chkObj.disabled	= true;
				}
			}else if (action == "issue"){
				if(allowIndentBasedIssue == 'N')  {
				if (fileStatus == 'A' && caseStatus != 'I' ){
					chkObj.disabled = true;
					chkObj.checked = true;
				}else{
					chkObj.disabled  = true;
					chkObj.checked = false;
				}
			} else {
				if(fileStatus == 'A' && caseStatus != 'I' && indented == 'Y') {
					chkObj.disabled = true;
					chkObj.checked = true;
				}else{
					chkObj.disabled  = true;
					chkObj.checked = false;
				}
			}
			}else if (action == "return"){
				if (fileStatus == 'U' && caseStatus != 'I'){
					chkObj.disabled = true;
					chkObj.checked = true;
				}else{
					chkObj.disabled  = true;
					chkObj.checked = false;
				}
			}else if (action == "close"){
				if (indented == 'Y' && caseStatus != 'I'){
					chkObj.disabled = true;
					chkObj.checked = true;
				}else {
					chkObj.disabled  = true;
					chkObj.checked = false;
				}
			}else{
				chkObj.disabled = false;
			}
		}else{
			if (action == "indent"){
				if ((fileStatus == 'A' || fileStatus == 'U') && indented == 'N' && caseStatus != 'I'){
					chkObj.disabled = false;
					chkObj.checked = false;
				}else {
					chkObj.disabled	= true;
				}
			}else if (action == "issue"){
				if(allowIndentBasedIssue == 'N') {
				if (fileStatus == 'A' && caseStatus != 'I' ){
					chkObj.disabled = false;
					chkObj.checked = false;
				}else{
					chkObj.disabled  = true;
				}
			 }
			 else {
			 		if(fileStatus == 'A' && caseStatus != 'I' && indented == 'Y') {
			 			chkObj.disabled = false;
						chkObj.checked = false;
			 		}else {
			 			chkObj.disabled  = true;
			 		}

			 	}
			}else if (action == "return"){
				if (fileStatus == 'U' && caseStatus != 'I'){
					chkObj.disabled = false;
					chkObj.checked = false;
				}else{
					chkObj.disabled  = true;
				}
			}else if (action == "close"){
				if (indented == 'Y' && caseStatus != 'I'){
					chkObj.disabled = false;
					chkObj.checked = false;
				}else {
					chkObj.disabled  = true;
				}
			}else{
				chkObj.disabled = false;
			}
		}
	}
}

function onCheckRadio(val) {
	var action = document.getElementById("action").value;
	var fileElmts = document.MRDSearchResults._fileCheck;

	if (action == ""){
		alert("select action ");
		return false;
	}
	if (val == 'all'){
		if (action == "indent"){
			enableDisableCasefile(action, val);
			//selectOptions(fileElmts,val);
		}else if (action == "issue"){
			enableDisableCasefile(action, val);
		}else if (action == "return"){
			enableDisableCasefile(action, val);
		}else if (action == "close"){
			enableDisableCasefile(action, val);
		}
	}else{
		if (action == "indent"){
			enableDisableCasefile(action, val);
		}else if (action == "issue"){
			enableDisableCasefile(action, val);
		}else if (action == "return"){
			enableDisableCasefile(action, val);
		}else if (action == "close"){
			enableDisableCasefile(action, val);
		}
	}
}

function validateAction(){
	var action = document.getElementById("action").value;
	var fileElmts = document.getElementsByName("_fileCheck");
	var disableFlag = false;
	var rdSelect = document.MRDSearchResults._selectItem;
	var rdFlag = false;
	var flag = false;
	var radValue = "";

	for (var i=0;i<rdSelect.length; i++){
		if (rdSelect[i].checked){
			radValue = rdSelect[i].value;
			rdFlag = true;
			break;
		}

	}
	if(!rdFlag){
		alert("select case file selection options");
		return false;
	}

	if (radValue == "all"){
		disableFlag = true;
	}else{
		for(var i=0;i<fileElmts.length;i++) {
			if(!fileElmts[i].disabled) {
				disableFlag = true;
				break;
			}else{
				disableFlag = false;
			}
		}
	}

	var itemLen = 0;
	if(disableFlag == true) {
		for(var i=0;i<fileElmts.length;i++) {
			itemLen = fileElmts.length;
			if(fileElmts[i].checked) {
				flag = true;
				break;
			}
		}
		if (!flag && itemLen == 1) {
			alert("Case file is either Issued or Indented ");
			return false;
		}else if (!flag) {
			alert("Please select any file to "+getSelText(document.getElementById("action")));
			return false;
		}
	}else{
		alert("Casefile is either Issued or Indented ");
		return false;
	}

	if(searchValidation()){
		return true;
	}else{
		return false;
	}


	return true;
}

function printPatientsList() {
	if(searchValidation()){
	var elmLen = document.forms[0].elements.length;
	var elm = eval("document.forms[0].elements");
	var params="" ;
	for (i=0;i<elmLen;i++){
			if (elm[i].type =='text' || elm[i].type == 'select-one'
					|| elm[i].type == 'hidden' ){
				params += '&'+elm[i].name + "=" + elm[i].value;
			}
			if (elm[i].type == 'checkbox' && elm[i].checked){
				params +='&'+elm[i].name+ "=" + elm[i].value;
			}
	}

	window.open('./MRDCaseFileSearch.do?_method=print'+ params);
	}
}


function onSubmitAction(){
	if (validateAction()){

		document.forms['MRDSearchResults'].action="./MRDCaseFileSearch.do";

		var mrno = 	makeHidden("_mr_no", "mr_no",document.forms['MRDCaseSearchForm']._mr_no.value);
		var caseno = makeHidden("casefile_no", "casefile_no",
				document.forms['MRDCaseSearchForm'].casefile_no.value);
		var depId = makeHidden("_dept_id","dept_id",document.forms['MRDCaseSearchForm']._dept_id.value);
		var issueToDept = makeHidden("_issued_to_dept", "issued_to_dept",
				document.forms['MRDCaseSearchForm']._issued_to_dept.value);
		var issueToUser = makeHidden("_issued_to_user", "issued_to_user",
				document.forms['MRDCaseSearchForm']._issued_to_user.value);
		var reqBy = makeHidden("requested_by", "requested_by",
				document.forms['MRDCaseSearchForm'].requested_by.value);

		var reqDept = makeHidden("_req_dept_id", "req_dept_id",
				document.forms['MRDCaseSearchForm']._req_dept_id.value);

		var fs = document.forms['MRDCaseSearchForm'].file_status;
		var fileStatus = {};
		for (var i=0;i<fs.length;i++){
			if (fs[i].checked){
				fileStatus[i] = makeHidden("file_status", "file_status"+i, fs[i].value);
				document.forms['MRDSearchResults'].appendChild(fileStatus[i]);
			}
		}
		var fileStatusOp = makeHidden("file_status@op", "file_status@op", "in");

		var vt = document.forms['MRDCaseSearchForm']._visit_type;
		var visitType = {};
		for (var i=0;i<vt.length;i++){
			if (vt[i].checked){
				visitType[i] = makeHidden("_visit_type","visit_type"+i, vt[i].value);
				document.forms['MRDSearchResults'].appendChild(visitType[i]);
			}
		}

		var visitTypeOp = makeHidden("_visit_type@op","","in");
		
		var vs = document.forms['MRDCaseSearchForm']._visit_status;
		var visitStatus = {};
		for (var i=0; i<vs.length; i++){
			if (vs[i].checked){
				visitStatus[i] = makeHidden("_visit_status","visit_status"+i, vs[i].value);
				document.forms['MRDSearchResults'].appendChild(visitStatus[i]);
			}
		}

		var visitStatusOp = makeHidden("_visit_status@op","","in");

		var cs = document.forms['MRDCaseSearchForm'].case_status;
		var caseStatus = {};
		for (var i=0;i<cs.length;i++){
			if (cs[i].checked){
				caseStatus[i] = makeHidden("case_status", "case_status"+i,cs[i].value);
				document.forms['MRDSearchResults'].appendChild(caseStatus[i]);
			}
		}

		var caseStatusOp = makeHidden("case_status@op", "", "in");

		var indented = document.forms['MRDCaseSearchForm'].indented;
		var indented_status = {};
		for(var i=0;i<indented.length;i++){
			if(indented[i].checked){
				indented_status[i] = makeHidden("indented","indented"+i, indented[i].value);
				document.forms['MRDSearchResults'].appendChild(indented_status[i]);
			}
		}

		var indentedOp = makeHidden("indented@op","","in");

		var fromRequestedDate = makeHidden("requested_date0", "requested_date0",document.getElementById("requested_date0").value);
		var toRequestedDate = makeHidden("requested_date1", "requested_date1",document.getElementById("requested_date1").value);


		var fromRequestedTime = makeHidden("requested_time0","requested_time0",document.getElementById("requested_time0").value);
		var toRequestedTime  = makeHidden("requested_time1","requested_time1",document.getElementById("requested_time1").value);

		var fromIssuedDate = makeHidden("issued_on0", "issued_on0",document.getElementById("issued_on0").value);
		var toIssuedDate = makeHidden("issued_on1", "issued_on1", document.getElementById("issued_on1").value);

		var fromIssuedTime = makeHidden("issued_on_time0", "issued_on_time0", document.getElementById("issued_on_time0").value);
		var toIssuedTime = makeHidden("issued_on_time1", "issued_on_time1", document.getElementById("issued_on_time1").value);

		document.forms['MRDSearchResults'].appendChild(caseno);
		document.forms['MRDSearchResults'].appendChild(mrno);
		document.forms['MRDSearchResults'].appendChild(depId);
		document.forms['MRDSearchResults'].appendChild(reqBy);
		document.forms['MRDSearchResults'].appendChild(fileStatusOp);
		document.forms['MRDSearchResults'].appendChild(visitTypeOp);
		document.forms['MRDSearchResults'].appendChild(visitStatusOp);
		document.forms['MRDSearchResults'].appendChild(caseStatusOp);
		document.forms['MRDSearchResults'].appendChild(indentedOp);
		document.forms['MRDSearchResults'].appendChild(reqDept);
		document.forms['MRDSearchResults'].appendChild(issueToDept);
		document.forms['MRDSearchResults'].appendChild(issueToUser);
		document.forms['MRDSearchResults'].appendChild(fromRequestedDate);
		document.forms['MRDSearchResults'].appendChild(toRequestedDate);
		document.forms['MRDSearchResults'].appendChild(fromRequestedTime);
		document.forms['MRDSearchResults'].appendChild(toRequestedTime);
		document.forms['MRDSearchResults'].appendChild(fromIssuedDate);
		document.forms['MRDSearchResults'].appendChild(toIssuedDate);
		document.forms['MRDSearchResults'].appendChild(fromIssuedTime);
		document.forms['MRDSearchResults'].appendChild(toIssuedTime);


		document.forms['MRDSearchResults'].submit();
		return true;
	}else{
		return false;
	}

}


function searchValidation(){

	var requestFromDate  = document.getElementById("requested_date0");
	var requestFromTime = document.getElementById("requested_time0");
	var requestToDate = document.getElementById("requested_date1");
	var requestToTime = document.getElementById("requested_time1");

	if (requestFromDate.value != "" && requestFromTime.value == ""){
		alert("Enter from Time");
		document.getElementById("requested_time0").focus();
		return false;
	}

	if (!validateTime(requestFromTime)){
		document.getElementById("requested_time0").focus();
		return false;
	}

	if (requestFromDate.value !=""){
		if (validateDateFormat(requestFromDate.value)){
			return true;
		}
	}
	if (requestToDate.value != "" && requestToTime.value == ""){
		alert("Enter to Time");
		document.getElementById("requested_time1").focus();
		return false;
	}

	if (!validateTime(requestToTime)){
		document.getElementById("requested_time1").focus();
		return false;
	}
	if (requestToDate.value !=""){
		if (validateDateFormat(requestToDate.value)){
			return true;
		}
	}
	if(requestFromDate.value != "" || requestToDate.value != ""){
		var msg = validateDateStr(document.getElementById("requested_date0").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("requested_date1").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}

		if (getDateDiff(document.getElementById("requested_date0").value,document.getElementById("requested_date1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}

	}

	var issueFromDate  = document.getElementById("issued_on0");
	var issueFromTime = document.getElementById("issued_on_time0");
	var issueToDate = document.getElementById("issued_on1");
	var issueToTime = document.getElementById("issued_on_time1");

	if (issueFromDate.value != "" && issueFromTime.value == ""){
		alert("Enter from Time");
		document.getElementById("issued_on_time0").focus();
		return false;
	}

	if (!validateTime(issueFromTime)){
		document.getElementById("issued_on_time0").focus();
		return false;
	}

	if (issueFromDate.value != ""){
		if (validateDateFormat(issueFromDate.value)){
			return true;
		}
	}

	if (issueToDate.value != "" && issueToTime.value == ""){
		alert("Enter to Time");
		document.getElementById("issued_on_time1").focus();
		return false;
	}

	if (!validateTime(issueToTime)){
		document.getElementById("issued_on_time1").focus();
		return false;
	}

	if (issueToDate.value !=""){
		if (validateDateFormat(issueToDate.value)){
			return true;
		}
	}

	if(issueFromDate.value != "" || issueToDate.value != ""){
		var msg = validateDateStr(document.getElementById("issued_on0").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("issued_on1").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("issued_on0").value,document.getElementById("issued_on1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}
	}
	return true;
}
