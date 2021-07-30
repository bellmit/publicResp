var addMRDDialog;
var casefileAdded = 0;
var	MRNO=0; PATNAME=1; CASEFILENO=2; DEPTNAME=3;
function init(){
	if ( (roleId == 1) || (roleId == 2)){
		allowBackdate = 'A';
	}
	addMrdDialog = new Insta.AddMRDCasefileDialog("btnAddCasefiles", onAddCasefiles, contextPath,
			"indent");
	addMrdDialog.initAddDeptAutoComplete();

}

function validateIndent(){
	var mrno = document.raiseIndentForm.mrNo;
	var remarks = document.getElementById("remarks");
	var indentDate = document.getElementById("indent_date");
	var indentTime = document.getElementById("indent_time");
	var indDtTime = getDateTime(indentDate.value, indentTime.value);


	var regDate = document.forms['raiseIndentForm'].regDate;
	var regTime = document.forms['raiseIndentForm'].regTime;

	for (var i=0;i<regDate.length; i++){
		if (regDate[i].value !=""){
			var regDateTime = getDateTime(regDate[i].value, regTime[i].value);
			var dateDiff = daysDiff(regDateTime, indDtTime);
			if (dateDiff < 0){
				alert("Indent date cannot be less than registration date");
				return false;
			}
		}
	}
	if (getNumRows() == 0){
		alert("Do you want to raise indent ? then add casefiles");
		document.getElementById("btnAddCasefiles").focus();
		document.getElementById("btnAddCasefiles").onfocus = addMrdDialog.start();

		return false;
	}

	if (indentDate.value == ""){
		alert("Date field cannot be null");
		return false;
	}
	if (validateDateFormat(indentDate.value)){
		return true;
	}
	if(indentTime.value == ""){
		alert("Enter time");
		return false;
	}else{
		if (!validateTime(indentTime)){
			return false;
		}
	}

	return true;
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
			addToTable(mrd.mrno, mrd.patientName, mrd.casefileNo, mrd.addDeptName, mrd.addDeptId,
					mrd.regDate, mrd.regTime, mrd.mlcStatus);
	}else{
		alert("Get details to add to the grid");
	}
}

function addToTable(mrno, patname, casefileNo, deptName, deptId, regdate, regtime, mlc_status){
	var id = getCasefileNum();
	var table = document.getElementById("mrdIndentTable");
	var no = document.getElementById("mrdIndentTable").rows.length-3;
	var temp = no + 1;
	var mrdRow = table.rows[temp];
	var row = mrdRow.cloneNode(true);
	row.id = "mrdRow"+id;
	row.style.display = '';
	table.tBodies[0].insertBefore(row, mrdRow);

	setNodeText(row.cells[MRNO], mrno);
	setNodeText(row.cells[PATNAME], patname);

	if(mlc_status == 'Y')
		row.cells[CASEFILENO].setAttribute("class","mlcIndicator");

	setNodeText(row.cells[CASEFILENO], casefileNo);
	setNodeText(row.cells[DEPTNAME], deptName);

	setHiddenValue(id, "mrNo", mrno);
	setHiddenValue(id, "patname",  patname);
	setHiddenValue(id, "casefile", casefileNo);
	setHiddenValue(id, "deptName", deptName);
	setHiddenValue(id, "deptId", deptId);
	setHiddenValue(id, "regDate", regdate);
	setHiddenValue(id, "regTime", regtime);
	casefileAdded++;
	addMrdDialog.align();
	return id;
}

function getCasefileNum(){
	return document.getElementById("mrdIndentTable").rows.length-3;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(raiseIndentForm, name, index);
	if (el) {
		el.value = value;
	}
}


function cancelCasefile(obj){
	var row = findAncestor(obj, "TR");
	var id = row.rowIndex-0;
	row.parentNode.removeChild(row);
	addMrdDialog.align();
}

function backToSearch(){
	    window.location.href="./MRDCaseFileSearch.do?_method=searchCasefiles&_visit_type=o&_visit_status=A&date_range=week";
}

function initDialog(){

dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"700px",
			context : ["mrdIndentTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		} );

		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog,
	                                              	correctScope:true} );
		dialog.cfg.queueProperty("keylisteners", escKeyListener);
dialog.render();
}

function handleCancel() {
	dialog.cancel();
}

function editCaseFile(obj){
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	initDialog();
	initDeptNames();
	document.getElementById("mno").textContent = getIndexedValue("mrNo",id);
	document.getElementById("patient_name").textContent = getIndexedValue("patname",id);
	document.getElementById("casefileno").textContent = getIndexedValue("casefile",id);
	document.getElementById("depname").value = getIndexedValue("deptName",id);
	document.getElementById("depid").value = getIndexedValue("deptId",id);
	document.getElementById("selected_row_id").value = id;
	dialog.show();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(raiseIndentForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function getCaseFileRow(i) {
	i = parseInt(i);
	var table = document.getElementById("mrdIndentTable");
	return table.rows[i + getFirstItemRow()];
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(raiseIndentForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function handelSubmit(){
	var id = document.getElementById("selected_row_id").value;
	var table = document.getElementById("mrdIndentTable");
	var row = getCaseFileRow(id);

	row.id = "mrdRow"+id;

	setIndexedValue("deptName", id, document.getElementById("depname").value);
	setIndexedValue("deptId", id, document.getElementById("depid").value);
	setNodeText(row.cells[DEPTNAME], document.getElementById("depname").value);
	dialog.hide();
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCaseFiles() + 1;
}

function getNumCaseFiles() {
	// header, add row, hidden template row: totally 3 extra
	return document.getElementById("mrdIndentTable").rows.length-3;
}

var itAutoComplete = null;
function initDeptNames() {
	if (itAutoComplete != undefined) {
		itAutoComplete.destroy();
	}

  YAHOO.example.deptArray = [];
	var i=0;
	for(var j=0; j<dept_list.length; j++)
		{
		   YAHOO.example.deptArray.length = i+1;
			YAHOO.example.deptArray[i] = dept_list[j];
			i++;
		}

   YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.deptArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'dept_name'},
						{key : 'dept_id'}
					]
		};

		itAutoComplete = new YAHOO.widget.AutoComplete('depname','dept_dropdown', datasource);
		itAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		itAutoComplete.useShadow = true;
		itAutoComplete.minQueryLength = 0;
		itAutoComplete.allowBrowserAutocomplete = false;
		itAutoComplete.resultTypeList = false;
		itAutoComplete.forceSelection = true;
		itAutoComplete.maxResultsDisplayed = 20;


		itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			raiseIndentForm.depname.value = elItem[2].dept_name;
			raiseIndentForm.depid.value = elItem[2].dept_id;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			raiseIndentForm.depname.value = '';
		});

}
}

function getNumRows() {
	// header, add row, hidden template row: totally 3 extra
	return document.getElementById("mrdIndentTable").rows.length-3;
}
