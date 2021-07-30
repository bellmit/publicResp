var addMRDDialog;
var casefileAdded = 0;

function init(){
	addMrdDialog = new Insta.AddMRDCasefileDialog("btnAddCasefiles", onAddCasefiles, contextPath, "closeIndent");
	var i = 0;
	MRNO=i++; PATNAME= i++; CASEFILENO=i++; DEPTNAME=i++;
	 addMrdDialog.initAddDeptAutoComplete();
}

function validateIndent(){
	var mrno = document.closemrdCasefileForm.mrNo;
	if (getCasefileNum() == 0){
		alert("Do you want to close the indent ? then add casefiles");
		document.getElementById("btnAddCasefiles").focus();
		document.getElementById("btnAddCasefiles").onfocus = addMrdDialog.start();
		return false;
	}
	return true;
}
function onAddCasefiles(mrd){
	var mrnos = document.getElementsByName("mrNo");
	for (var i=0;i<mrnos.length;i++){
		if(mrnos[i].value == mrd.mrno){
			alert("Record already entered ");
			return false;
		}
	}

	if (mrd.mrno != ''){
			addToTable(mrd.mrno, mrd.patientName, mrd.casefileNo, mrd.indentedDeptName, mrd.mlcStatus);
	}else{
		alert("Get details to add to the grid");
	}
}

function addToTable(mrno, patname, casefileNo, deptname, mlc_status){
	var id = getCasefileNum();
	var table = document.getElementById("closeIndentTable");
	var no = document.getElementById("closeIndentTable").rows.length-3;
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
	setNodeText(row.cells[DEPTNAME], deptname);

	setHiddenValue(id, "mrNo", mrno);
	setHiddenValue(id, "patName",  patname);
	setHiddenValue(id, "casefile", casefileNo);
	setHiddenValue(id, "deptname", deptname);
	casefileAdded++;
	addMrdDialog.align();
	return id;
}

function getCasefileNum(){
	return document.getElementById("closeIndentTable").rows.length-3;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(closemrdCasefileForm, name, index);
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
