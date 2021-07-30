var addMrdDialog;
var addMultiCasefileDialog;
var issueForm = document.issueCasefileForm;
var editform = document.editForm;

function init(){
issueForm = document.issueCasefileForm;
addMrdDialog = new Insta.AddMRDCasefileDialog("btnAddCasefiles", onAddCasefiles, contextPath, "issue");

var i=0;
MRNO = i++; PATNAME = i++; CASEFILENO = i++; REQUESTEDBY = i++; ISSUEDTO = i++; INDENTDATE = i++;
PURPOSE = i++;

initCasefileEditDialog();
initMultipleCasefileAddDialog();
initCaseFileUserNameAutoComplete();
initEditCaseFileDeptAutoComplete();
addMrdDialog.initAddDeptAutoComplete();
}


function onAddCasefiles(mrd){
	var mrnos = document.getElementsByName("mrNo");
	for (var i=0;i<mrnos.length;i++){
		if (mrnos[i].value == mrd.mrno){
			alert("Record already entered");
			return false;
		}
	}

	if (mrd.mrno != ''){
			addToTable(mrd.mrno, mrd.patientName, mrd.casefileNo, mrd.indentedDeptName, mrd.regDate,
					mrd.regTime, mrd.addDeptName, mrd.addDeptId, mrd.addDeptType, mrd.mlcStatus, mrd.indentOn, mrd.indentDate, mrd.indnentTime);
	}else{
		alert("Get details to add to the grid");
	}
}


function addToTable(mrno, patname, casefileNo, requestedby, regdate, regtime, issuedtoName,
		issuedtoId, type, mlc_status,indentOn, indentDate, indentTime){

	if (type == '') type='D';

	var id = getCasefileNum();
	var table = document.getElementById("casefileIssueTable");
	var no = document.getElementById("casefileIssueTable").rows.length-3;
	var temp = no + 1;
	var mrdRow = table.rows[temp];
	var row = mrdRow.cloneNode(true);
	row.style.display = '';


	table.tBodies[0].insertBefore(row, mrdRow);
	setNodeText(row.cells[MRNO], mrno);
	setNodeText(row.cells[PATNAME], patname.substring(0,15));

	if(mlc_status == 'Y')
		row.cells[CASEFILENO].setAttribute("class", "mlcIndicator");

	setNodeText(row.cells[CASEFILENO], casefileNo);
	setNodeText(row.cells[REQUESTEDBY], requestedby);
	setNodeText(row.cells[ISSUEDTO], issuedtoName);
	setNodeText(row.cells[INDENTDATE],indentOn);

	setHiddenValue(id, "mrNo", mrno);
	setHiddenValue(id, "patname", patname);
	setHiddenValue(id, "casefile", casefileNo);
	setHiddenValue(id, "requestedBy", requestedby);
	setHiddenValue(id, "issueType", type);
	setHiddenValue(id, "issuedToId", issuedtoId);
	setHiddenValue(id, "issuedToName", issuedtoName);
	setHiddenValue(id, "regDate", regdate);
	setHiddenValue(id, "regTime", regtime);
	setHiddenValue(id, "indentedOnDate",indentDate);
	setHiddenValue(id, "indentedOnTime",indentTime);
	addMrdDialog.align();
	return id;
}

function getCasefileNum(){
	return document.getElementById("casefileIssueTable").rows.length-3;
}

function onSaveValidate(){
	var issuedTo = 	document.getElementsByName("issuedToName");
	var casefileno = document.issueCasefileForm.casefile;
	var mrno = document.issueCasefileForm.mrNo;

	for (var i=0;i<issuedTo.length-1; i++){
		if (issuedTo[i].value == ""){
			alert("Patient " +mrno[i].value+ " is not belongs to any department please select department name from edit case file dialog to issue case file ");
			return false;
		}
	}


	if (getCasefileNum() == 0){
		alert("Do you want to issue casefile ? then add case file ");
		if(indentbasedissue != 'Y') {
			document.getElementById("btnAddCasefiles").focus();
			document.getElementById("btnAddCasefiles").onfocus=addMrdDialog.start();
		}
		return false;
	}

	var requestedBy = document.getElementsByName("requestedBy");
	var issuedto = document.getElementsByName("issuedto");

	for (var i=0;i<requestedBy.length;i++){
		if (requestedBy[i].value != ""){
			if (requestedBy[i].value == issuedTo[i].value){
				return true;
			}else{
				alert("indent requested user and issued to user are not same for case file "+casefileno[i].value);
				return true;
			}
		}else{
			if(issuedTo[i].value !=null){
				return true;
			}else{
			}
		}

	}


	for (var i=0;i<requestedBy.length-1;i++){
		if (requestedBy[i].value != "" && issuedto[i].value !=""){
			if (requestedBy[i].value != issuedto[i].value){
				var issue = confirm("indent requested user '"+issuedto[i].value+"' and issued to user '"+requestedBy[i].value+"' are not same for case file "+casefileno[i].value);
			}
		}
	}
	if (issue)
		return true;
	else
		return false;
	return true;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(issueCasefileForm, name, index);
	if (el) {
		el.value = value;
	}
}
function backToSearch(){
	    window.location.href="./MRDCaseFileSearch.do?_method=searchCasefiles&_visit_type=o&_visit_status=A&date_range=week";
}

function cancelCasefile(obj){
	var row = findAncestor(obj, "TR");
	var id = row.rowIndex-0;
	row.parentNode.removeChild(row);
	addMrdDialog.align();
}

function initCasefileEditDialog(){
	var issueDialogDiv = document.getElementById("editCasefileDialog");
	issueDialogDiv.style.display = 'block';
 	issueDialog = new YAHOO.widget.Dialog('editCasefileDialog',{
					width:"600px",
					context: ["casefileIssueTable","tr","br"],
					visible:false,
					modal:true,
					constraintoviewport:true
	});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:issueDialog.cancel, scope:issueDialog, correctScope:true } );
		issueDialog.cfg.setProperty("keylisteners",escKeyListener);

	var enterKeyListener = new YAHOO.util.KeyListener(document, {keys:13},
			{fn:validateEditDialog, scope:issueDialog, correctScope:true } );
	issueDialog.cfg.setProperty("keylisteners", enterKeyListener);

	issueDialog.render();
}


function editCasefileDialog(obj){
	editform = document.editForm;
	var row = findAncestor(obj, "TR");
	var id = row.rowIndex - 1;
	YAHOO.util.Dom.addClass(row, 'editing');
	var issueToId = getIndexedFormElement(issueForm, "issuedToId", id);
	var issueToName = getIndexedFormElement(issueForm, "issuedToName", id);
	var issueType = getIndexedFormElement(issueForm, "issueType", id);
	var issueDate =  getIndexedFormElement(issueForm, "issuedOnDate", id);
	var indentDate = getIndexedFormElement(issueForm, "indentedOnDate", id);
	var indentTime = getIndexedFormElement(issueForm, "indentedOnTime", id);
	var purpose = getIndexedFormElement(issueForm, "purpose", id);
	document.getElementById("edit_issued_to").value=issueToName.value;
	document.getElementById("edit_issued_to_id").value=issueToId.value;
	document.getElementById("edit_issued_to_type").value=issueType.value;
	document.getElementById("issue_date").value=issueDate.value;
	if(indentDate != null)
		document.getElementById("indent_date").value=indentDate.value;
	else
		document.getElementById("indent_date").value='';
	if(indentTime != null)
		document.getElementById("indent_time").value=indentTime.value;
	else
		document.getElementById("indent_time").value='';
	document.getElementById("issuePurpose").value=purpose.value;
	document.forms['editForm'].editRowId.value=id;
	issueDialog.show();
}


function validateEditDialog(){
	var issueTo  = document.getElementById("issued_to").value;
	var issuedDate = document.getElementById("issue_date");
	var issuedTime = document.getElementById("issue_time");

	var issueDtTime = getDateTime(issuedDate.value, issuedTime.value);

	var regDate = document.forms['issueCasefileForm'].regDate;
	var regTime = document.forms['issueCasefileForm'].regTime;

	if (issueTo == ""){
		alert("Select Issued to Department");
		return false;
	}


	for (var i=0;i<regDate.length; i++){
		if (regDate[i].value !=""){
			var regDateTime = getDateTime(regDate[i].value, regTime[i].value);
			var dateDiff = daysDiff(regDateTime, issueDtTime);
			if (dateDiff < 0){
				alert("issued date cannot be less than registration date");
				return false;
			}
		}
	}
	if(issuedTime.value == ""){
		alert("Enter time");
		return false;
	}else{
		if (!validateTime(issuedTime)){
			return false;
		}
	}

	if (issuedDate.value == ""){
		alert("Date field cannot be null");
		return false;
	}
	if (validateDateFormat(issuedDate.value)){
		return true;
	}
	/*
	 *     var dt = document.getElementById("issue_date");
	 *         if (!doValidateDateField(dt, "future")){
	 *                 return false;
	 *                     }
	 *                     */
	return true;
}


function onEditCasefile(){
	if (!validateEditDialog()){
		return false;
	}
	var id = document.forms['editForm'].editRowId.value;
	var table = document.getElementById("casefileIssueTable");
	var row = table.rows[parseInt(id) + 1];

	var edit_issued_to_name = document.getElementById("edit_issued_to").value;
	var edit_issued_to_id = document.getElementById("edit_issued_to_id").value;
	var edit_issued_to_type = document.getElementById("edit_issued_to_type").value;
	var issue_date = document.getElementById("issue_date").value;
	var issue_time = document.getElementById("issue_time").value;
	var issuePurpose = document.getElementById("issuePurpose").value;
	var indent_date = document.getElementById("indent_date").value;
	var indent_time = document.getElementById("indent_time").value;
	var indent_date_time = (indent_date+" "+indent_time);;
	var issuedToId = getIndexedItemValue("issuedToId", id);
	if (issuedToId.value != edit_issued_to_id){
		setNodeText(row.cells[ISSUEDTO], edit_issued_to_name);
		setIndexedValue("issuedToId", id, edit_issued_to_id);
		setIndexedValue("issuedToName", id, edit_issued_to_name);
		setIndexedValue("issueType", id, edit_issued_to_type);
	}else if (issuedToId.value == ""){
		setNodeText(row.cells[ISSUEDTO], edit_issued_to_name);
		setIndexedValue("issuedToId", id, edit_issued_to_id);
		setIndexedValue("issuedToName", id, edit_issued_to_name);
		setIndexedValue("issueType", id, edit_issued_to_type);
	}

	setIndexedValue("issuedOnDate", id, issue_date);
	setIndexedValue("issuedOnTime", id, issue_time);
	setNodeText(row.cells[INDENTDATE], indent_date_time);
	setIndexedValue("purpose", id, issuePurpose);
	setNodeText(row.cells[PURPOSE], issuePurpose);
	issueDialog.hide();

}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(issueForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedItemValue(name, index){
	var obj = getIndexedFormElement(issueForm, name, index);
	return obj;
}

function initEditCaseFileDeptAutoComplete() {
	var dataSource = new YAHOO.util.XHRDataSource(contextPath +"/medicalrecorddepartment/MRDCaseFileIssue.do");
	dataSource.scriptQueryAppend="_method=getDepartmentList";
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
resultsList :"result",
			 fields : [  {key : "issued_to_name"},
						 {key : "issue_to_id"},
						 {key : "type"}
			 ]
	};
	oAutoComp = new YAHOO.widget.AutoComplete('edit_issued_to', 'editIssuedToDropdown', dataSource);
	oAutoComp.minQueryLength = 2;
	oAutoComp.forceSelection = false;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.resultTypeList = false;

	oAutoComp.itemSelectEvent.subscribe(getDeptId);
	oAutoComp.selectionEnforceEvent.subscribe(clearDeptId);

	function getDeptId(oSelf, elItem, oData){
		document.editForm.edit_issued_to_id.value = elItem[2].issue_to_id;
		document.editForm.edit_issued_to_type.value = elItem[2].type;
	}

	function clearDeptId(oSelf , sClearedValue){
		document.editForm.edit_issued_to_id.value = '';
	}
}




function initCaseFileUserNameAutoComplete() {
	var dataSource = new YAHOO.util.XHRDataSource(contextPath +"/medicalrecorddepartment/MRDCaseFileIssue.do");
		dataSource.scriptQueryAppend="_method=getDepartmentList";
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		dataSource.responseSchema = {
			resultsList :"result",
			fields : [  {key : "issued_to_name"},
						{key : "issue_to_id"},
						{key : "type"}
					]
		};
		oAutoComp = new YAHOO.widget.AutoComplete('req_issued_to', 'reqIssuedToDropdown', dataSource);
		oAutoComp.minQueryLength = 2;
		//oAutoComp.forceSelection = true;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.resultTypeList = false;

		oAutoComp.itemSelectEvent.subscribe(getDeptId);
		oAutoComp.selectionEnforceEvent.subscribe(clearDeptId);

		function getDeptId(oSelf, elItem, oData){
			document.addMultipleCasefileform.issue_to_id.value = elItem[2].issue_to_id;
			document.addMultipleCasefileform.issue_to_type.value = elItem[2].type;
		}

		function clearDeptId(oSelf , sClearedValue){
			document.addMultipleCasefileform.issue_to_id.value = '';
		}
}

function initMultipleCasefileAddDialog(){
	var multiCasefileDialogDiv = document.getElementById("addMultipleCasefileDialog");
	multiCasefileDialogDiv.style.display = 'block';
 	multiCasefileDialog = new YAHOO.widget.Dialog('addMultipleCasefileDialog',{
					width:"400px",
					context: ["casefileIssueTable","tr","br"],
					visible:false,
					modal:true,
					constraintoviewport:true
	});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:multiCasefileDialog.cancel, scope:multiCasefileDialog, correctScope:true } );
		multiCasefileDialog.cfg.setProperty("keylisteners",escKeyListener);

	var enterKeyListener = new YAHOO.util.KeyListener(document, {keys:13},
			{fn:onAddMulitpleCasefile, scope:multiCasefileDialog, correctScope:true } );
		multiCasefileDialog.cfg.setProperty("keylisteners", enterKeyListener);

	multiCasefileDialog.render();
}

function addMultiCasefileDialog(obj){
	multiCasefileDialog.show();
	multiCasefileDialog.align("tr","br");
}

function onAddMulitpleCasefile(){
	var deptName = document.addMultipleCasefileform.req_issued_to.value;
	var deptId = document.addMultipleCasefileform.issue_to_id.value;
	var year = document.addMultipleCasefileform.year.value;
	var type = document.addMultipleCasefileform.issue_to_type.value;
	var request_date =  document.addMultipleCasefileform.request_date.value;
	var issueMrnos = document.getElementsByName("mrNo");
	var url = contextPath+ '/medicalrecorddepartment/MRDCaseFileIssue.do?_method=getIndentedCasefileList&requesting_dept='+encodeURIComponent(deptName)+'&indented_date='+year+'&indented_date@type=text&_screen=issue&request_date='+request_date+'&request_date@type=timestamp';

	var reqObj = newXMLHttpRequest();
	reqObj.open("POST", url.toString(), false);
	reqObj.send(null);
	var details = null;

	if (reqObj.readyState == 4){
		if ( (reqObj.status == 200) && (reqObj.responseText != null)){
			details = eval(reqObj.responseText);
		}
	}
	if (details.length == 0){
		alert("There is no indented case files for given search");
	}else{
		var flag;
		for (var i=0;i<details.length;i++){
			for (var j=0;j<issueMrnos.length-1;j++){
				if (details[i].mr_no == issueMrnos[j].value){
					flag= true;
					break;
				}else{
					flag = false;
				}
			}
			if (!flag){
				addToTable(details[i].mr_no, details[i].salutation + '' +details[i].patient_name +' '+
						details[i].last_name, details[i].casefile_no, details[i].requesting_dept,
						formatDate(new Date(details[i].regdate),'ddmmyyyy','-'),
						formatTime(new Date(details[i].regtime), false), details[i].requesting_dept,
						details[i].req_dept_id, type,details[i].indent_date,details[i].ind_date,details[i].ind_time);
			}
		}
	}

	multiCasefileDialog.cancel();

	return null;
}

function closeMultiDialog(){
	    multiCasefileDialog.cancel();
}

