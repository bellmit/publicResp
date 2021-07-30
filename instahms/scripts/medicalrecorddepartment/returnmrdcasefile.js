var addMrdDialog;

function init(){
addMrdDialog = new Insta.AddMRDCasefileDialog("btnAddCasefiles", onAddCasefiles, contextPath, "return");
var i = 0;
SELECTCASEFILE=i++; MRNO=i++; PATNAME= i++; CASEFILENO=i++; DEPTNAME=i++;
initReturnDeptAutoComplete();
initMultipleCasefileAddDialog();
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
		addToTable(mrd.mrno, mrd.patientName, mrd.casefileNo, mrd.addDeptName, mrd.addDeptId, mrd.addDeptType,
				mrd.regDate, mrd.regTime, mrd.mlcStatus);
	}else{
		alert("Getdetails to add to the grid");
	}
}

function addToTable(mrno, patname, casefileNo, casefilewith, deptId, deptType, regDate, regTime, mlc_status) {
	var id = getCasefileNum();
	var table = document.getElementById("casefileReturnTable");
	var no = document.getElementById("casefileReturnTable").rows.length-3;
	var temp = no + 1;
	var mrdRow = table.rows[temp];
	var row = mrdRow.cloneNode(true);
	row.id = "mrdRow"+id;
	row.style.display = '';
	table.tBodies[0].insertBefore(row, mrdRow);
	var cell = null;
	var inp = null;

	setNode(row.cells[SELECTCASEFILE],"select_case_file",id);
	setNodeText(row.cells[MRNO], mrno);
	setNodeText(row.cells[PATNAME], patname);

	if(mlc_status == 'Y')
		row.cells[CASEFILENO].setAttribute("class","mlcIndicator");

	setNodeText(row.cells[CASEFILENO], casefileNo);
	setNodeText(row.cells[DEPTNAME], casefilewith);

	setHiddenValue(id, "selected", "true");
	setHiddenValue(id, "mrNo", mrno);
	setHiddenValue(id, "patName", patname);
	setHiddenValue(id, "casefile", casefileNo);
	setHiddenValue(id, "deptName", casefilewith);
	setHiddenValue(id, "deptId", deptId);
	setHiddenValue(id, "deptType", deptType);
	setHiddenValue(id, "regDate", regDate);
	setHiddenValue(id, "regTime", regTime);
	addMrdDialog.align();
	return id;
}

function setNode(node, select_case_file, id) {
	var cellText=document.createElement("input");
	cellText.type="checkbox";
	cellText.name=select_case_file;
	cellText.id=select_case_file;
	cellText.checked = true;
	cellText.setAttribute("onClick", "return setSelectedValue(this,"+ id +")");
 	node.appendChild(cellText);

}

function setSelectedValue(check,id) {
	var selected;
	if(check.checked == true)
		selected = "true";
	else
		selected="false";

	setHiddenValue(id, "selected", selected);

	var casefileReturnTable = document.getElementById("casefileReturnTable");
	var row = casefileReturnTable.rows[id+1];
		if(check.checked == false)
		row.className = 'unselectedRow';
		else
		removeClassName(row,'unselectedRow');
	return true;
}

function getCasefileNum(){
	return document.getElementById("casefileReturnTable").rows.length-3;
}

function onReturnValidate(){
	var returnDate = document.getElementById("return_date");
	var returnTime = document.getElementById("return_time");
	var returnDtTime = getDateTime(returnDate.value, returnTime.value);

	var regDate = document.forms['returnCasefileForm'].regDate;
	var regTime = document.forms['returnCasefileForm'].regTime;

	var mrno = document.returnCasefileForm.mrNo;

	if (getCasefileNum() == 0){
		alert("Do you want to return case file ? then add casefile");
		document.getElementById("btnAddCasefiles").focus();
		document.getElementById("btnAddCasefiles").onfocus = addMrdDialog.start();
		return false;
	}

	for (var i=0;i<regDate.length; i++){
		if (regDate[i].value !=""){
			var regDateTime = getDateTime(regDate[i].value, regTime[i].value);
			var dateDiff = daysDiff(regDateTime, returnDtTime);
			if (dateDiff < 0){
				alert("Return date cannot be less than registration date");
				return false;
			}
		}
	}


	if(returnTime.value == ""){
		alert("Enter time");
		return false;
	}else{
		if (!validateTime(returnTime)){
			return false;
		}
	}

	if (returnDate.value == ""){
		alert("Date field cannot be null");
		return false;
	}
	if (validateDateFormat(returnDate.value)){
		return true;
	}

	return true;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(returnCasefileForm, name, index);
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


function initReturnDeptAutoComplete() {
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
	oAutoComp = new YAHOO.widget.AutoComplete('return_dept_name', 'reqIssuedToDropdown', dataSource);
	oAutoComp.minQueryLength = 2;
	oAutoComp.forceSelection = true;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.resultTypeList = false;

	oAutoComp.itemSelectEvent.subscribe(getDeptId);
	oAutoComp.selectionEnforceEvent.subscribe(clearDeptId);

	function getDeptId(oSelf, elItem, oData){
		document.addMultipleCasefileform.return_dept_id.value = elItem[2].issue_to_id;
		document.addMultipleCasefileform.return_dept_type.value = elItem[2].type;
	}

	function clearDeptId(oSelf , sClearedValue){
		document.addMultipleCasefileform.return_dept_id.value = '';
	}
}



function initMultipleCasefileAddDialog(){
	var multiCasefileDialogDiv = document.getElementById("addMultipleCasefileDialog");
	multiCasefileDialogDiv.style.display = 'block';
	multiCasefileDialog = new YAHOO.widget.Dialog('addMultipleCasefileDialog',{
						width:"400px",
						context: ["casefileReturnTable","tr","br"],
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

	document.getElementById("patient_type").value = 'o';
	multiCasefileDialog.render();
}

function addMultiCasefileDialog(obj){
	multiCasefileDialog.show();
	multiCasefileDialog.align("tr","br");
}


function onAddMulitpleCasefile(){
	var deptName = document.addMultipleCasefileform.return_dept_name.value;
	var deptId = document.addMultipleCasefileform.return_dept_id.value;
	var year = document.addMultipleCasefileform.year.value;
	var type = document.addMultipleCasefileform.return_dept_type.value;
	var issued_on = document.addMultipleCasefileform.issued_on.value;
	var issueMrnos = document.getElementsByName("mrNo");
	var patientType = document.getElementById("patient_type").value;
	var url = contextPath+ '/medicalrecorddepartment/MRDCaseFileIssue.do?_method=getIndentedCasefileList&casefile_with='+encodeURIComponent(deptName)+'&issue_date='+year+'&issue_date@type=text&_screen=return&issued_on='+issued_on+'&issued_on@type=timestamp&patient_type='+patientType;

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
		alert("There is no issued case files for given search ");
	}else{
		var flag;
		for (var i=0;i<details.length;i++){
			for (var j=0; j<issueMrnos.length-1;j++){
				if (details[i].mr_no == issueMrnos[j].value){
					flag = true;
					break;
				}else{
					flag = false;
				}
			}
			if (!flag){
				addToTable(details[i].mr_no, details[i].salutation + '' +details[i].patient_name +' '+
						details[i].last_name, details[i].casefile_no,
						details[i].casefile_with, details[i].issued_to_dept, 'D',
						formatDate(new Date(details[i].regdate),'ddmmyyyy','-'),
						formatTime(new Date(details[i].regtime), false));
			}

		}
	}

	multiCasefileDialog.cancel();

	return null;
}

function closeDailog(){
	multiCasefileDialog.cancel();
}
