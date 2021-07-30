var toolbar = {
	editNurseWard : {
			title : "Edit User Ward Assignment",
			href  : "/editwardassignment/show.htm?",
			imageSrc : "icons/Order.png",
			description : "Edit User Ward Assignment"
			}
	};
function init() {
	autoCompleteForUserNames();
	autoCompleteForUserRoles();
	createToolbar(toolbar);
}
function nurseInit() {
	clearFields();
}
var itemsAdded = 0;
var colIndex  = 0;
var SNO_COL = colIndex++; ROLE_COL = colIndex++; EMPUSER_COL =colIndex++; WARD_COL = colIndex++; USER_COL = colIndex++; DATE_COL = colIndex++;  TRASH_COL = colIndex++; 

function autoCompleteForUserNames(){
	var datasource = new YAHOO.util.LocalDataSource({result: userNameList});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "emp_username"} ]
	};
	var rAutoComp = new YAHOO.widget.AutoComplete('emp_username','usernameContainer', datasource);
	rAutoComp.minQueryLength = 0;
 	rAutoComp.maxResultsDisplayed = 18;
 	rAutoComp.forceSelection = false ;
 	rAutoComp.animVert = false;
 	rAutoComp.resultTypeList = false;
 	rAutoComp.typeAhead = false;
 	rAutoComp.allowBroserAutocomplete = false;
 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	rAutoComp.autoHighlight = true;
	rAutoComp.useShadow = false;
 	if (rAutoComp._elTextbox.value != '') {
			rAutoComp._bItemSelected = true;
			rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
	}
}
function autoCompleteForUserRoles(){
	var datasource = new YAHOO.util.LocalDataSource({result: roleNameList});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
	resultsList : "result",
	fields : [  {key : "role_name"}]
	};
	var rAutoComp = new YAHOO.widget.AutoComplete('role_name','roleContainer', datasource);
	rAutoComp.minQueryLength = 0;
 	rAutoComp.maxResultsDisplayed = 18;
 	rAutoComp.forceSelection = false ;
 	rAutoComp.animVert = false;
 	rAutoComp.resultTypeList = false;
 	rAutoComp.typeAhead = false;
 	rAutoComp.allowBroserAutocomplete = false;
 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	rAutoComp.autoHighlight = true;
	rAutoComp.useShadow = false;
 	if (rAutoComp._elTextbox.value != '') {
			rAutoComp._bItemSelected = true;
			rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
	}
}

function addWardToGrid(obj){
	var empName = document.getElementById('empusername').value;
	var empRoleId = document.getElementById('roleId').value;
	var empRoleName = document.getElementById('roleName').value;
	var selectedWardId = document.getElementById('a_ward_id').value;
	var wardName = "";
	if(!checkDuplicates(obj)){
		return false;
	}
	for(var i=0;i<wards.length;i++){
		if(selectedWardId == wards[i]['ward_no']){
			wardName = wards[i]['ward_name'];
			break;
		}
	}
	if(!empty(selectedWardId)){
		var id = getNumCharges('itemsTable');
	   	var table = document.getElementById("itemsTable");
		var templateRow = table.rows[getTemplateRow('itemsTable')];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	   	row.id = "itemRow" + id;
			
		var cell = null;
		setNodeText(row.cells[SNO_COL], '');
		setNodeText(row.cells[ROLE_COL], empRoleName);
		setNodeText(row.cells[EMPUSER_COL], empName);
		setNodeText(row.cells[WARD_COL], wardName);
		setNodeText(row.cells[USER_COL], loggedInUserId);
		setNodeText(row.cells[DATE_COL], curDate);
		
		row.cells[0].appendChild(makeHidden('h_role_id', 'h_role_id', empRoleId));
		row.cells[0].appendChild(makeHidden('h_ward_id', 'h_ward_id', selectedWardId));
		row.cells[0].appendChild(makeHidden('h_emp_username', 'h_emp_username', empName));
		row.cells[0].appendChild(makeHidden('h_ward_name', 'h_ward_name', wardName));
		row.cells[0].appendChild(makeHidden('h_mode_time', 'h_mode_time', curDate));
		row.cells[0].appendChild(makeHidden('h_isadded', 'h_isadded' , "true"));
		row.cells[0].appendChild(makeHidden('h_delItem', 'h_delItem' , "false"));
		//itemsAdded++;
		setRowStyle(id);
		clearFields();
	}
	return id;
}
function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}
function getThisRow(node) {
	return findAncestor(node, "TR");
}
function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
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
	var el = getIndexedFormElement(document.nurseWardForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}
function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.nurseWardForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.nurseWardForm, name, index);
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
function clearFields(){
	document.getElementById("ward_id").value = '';
	document.getElementById("a_ward_id").value = '';
	
}
function clearAutoFields(){
	document.getElementById("user").value = '';
	document.getElementById("role_name").value = '';
	
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
function checkDuplicates(obj){
	var selectedWardId = document.getElementById('a_ward_id').value;
	var gridWardId = document.getElementsByName('h_ward_id');
	for (var i=0; i<gridWardId.length; i++) {	
		if(!empty(gridWardId[i].value) && (gridWardId[i].value == selectedWardId)){
			alert("Cannot Assign to duplicate ward");
			clearFields();
			return false;
		}
	}
	return true;
}
function saveUserWard() {
	var selWardId = document.getElementById("a_ward_id").value;
	if(!empty(selWardId)){
		alert("Click on plus button to assign ward")
		return false;
	}
	document.nurseWardForm.submit();
	//clearFields();
	return true;
}

