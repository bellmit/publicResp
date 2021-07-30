function init() {
	initDoctorAutoComplete();
}
var itemsAdded = 0;
var colIndex  = 0;
var SNO_COL = colIndex++; DOCTOR_COL = colIndex++; DEPT_COL =colIndex++; DATE_COL = colIndex++; USER_COL = colIndex++; TRASH_COL = colIndex++; 

function addDoctorToGrid(obj){
	var snumber = 0;
	var selectedDocId = document.getElementById('doctor_id').value;
	var deptId ="";
	var deptName = "";
	var doctorName ="";
	if(!checkDuplicates(obj)){
		return false;
	}
	for(var i=0;i<doctors.length;i++){
		console.log(JSON.stringify(doctors[i]));
		if(selectedDocId == doctors[i]['doctor_id']){
			deptId = doctors[i]['dept_id'];
			deptName = doctors[i]['dept_name'];
			doctorName = doctors[i]['doctor_name'];
			break;
		}
	}
	if(!empty(selectedDocId)){
		var id = getNumCharges('itemsTable');
	   	var table = document.getElementById("itemsTable");
		var templateRow = table.rows[getTemplateRow('itemsTable')];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	   	row.id = "itemRow" + id;
			
		var cell = null;
		setNodeText(row.cells[SNO_COL], '');
		setNodeText(row.cells[DOCTOR_COL], doctorName);
		setNodeText(row.cells[DEPT_COL], deptName);
		setNodeText(row.cells[DATE_COL], curDate);
		setNodeText(row.cells[USER_COL], loggedInUserId);
		
		row.cells[0].appendChild(makeHidden('h_dept_id', 'h_dept_id', deptId));
		row.cells[0].appendChild(makeHidden('h_dept_name', 'h_dept_name', deptName));
		row.cells[0].appendChild(makeHidden('h_doctor_id', 'h_doctor_id', selectedDocId));
		row.cells[0].appendChild(makeHidden('h_doctor_name', 'h_doctor_name', doctorName));	
		row.cells[0].appendChild(makeHidden('h_mode_time', 'h_mode_time', curDate));
		row.cells[0].appendChild(makeHidden('h_isadded', 'h_isadded' , "true"));
		row.cells[0].appendChild(makeHidden('h_delItem', 'h_delItem' , "false"));
		//itemsAdded++;
		setRowStyle(id);
		clearFields();
	}
	return id;
}
function initDoctorAutoComplete() {
	var ds = new YAHOO.util.LocalDataSource({result : doctors}, {queryMatchContains: true});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				 ],
	};

	docAutoComp = new YAHOO.widget.AutoComplete("doctor", "doctorContainer", ds);
	docAutoComp.minQueryLength = 1;
	docAutoComp.animVert = false;
	docAutoComp.maxResultsDisplayed = 50;
	docAutoComp.resultTypeList = false;
	docAutoComp.forceSelection = true;
	docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	docAutoComp.formatResult = Insta.autoHighlight;
	docAutoComp.itemSelectEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('doctor_id').value = record.doctor_id;
	});
	docAutoComp.selectionEnforceEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('doctor_id').value = '';
	});

}
function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}
function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}
function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}
function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.careTeamForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}
function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.careTeamForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.careTeamForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}
function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');

	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = getIndexedValue("h_isadded", i) == 'true';
	var cancelled = getIndexedValue("h_delItem", i) == 'true';

	var cls;
	if (added) {
		cls = 'added';
	} else if (cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
	
	row.className = cls;
	
}
function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function saveIPCare() {
	var careDoc = document.getElementById("doctor").value;
	if(!empty(careDoc)){
		alert("Click on plus button to add the doctor")
		return false;
	}
	document.careTeamForm.submit();
	return true;
}
function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}
function getThisRow(node) {
	return findAncestor(node, "TR");
}

function cancelItem(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var isNew = getIndexedValue("h_isadded", id) == 'true';
	var oldDeleted =  getIndexedValue("h_delItem", id);

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("h_delItem", id, newDeleted);
		//setIndexedValue("edited", id, "true");
		setRowStyle(id);
	}
	return false;
}
function clearFields(){
	document.getElementById("doctor").value = '';
	document.getElementById("doctor_id").value = '';
	
}
function checkDuplicates(obj){
	var selectedDocId = document.getElementById('doctor_id').value;
	var doctorsId = document.getElementsByName('h_doctor_id');
	for (var i=0; i<doctorsId.length; i++) {	
		if(!empty(doctorsId[i].value) && (doctorsId[i].value == selectedDocId)){
			alert("Duplicate care doctors are not allowed");
			clearFields();
			//document.getElementById("doctor").focus();
			return false;
			
		}
	}
	return true;
}