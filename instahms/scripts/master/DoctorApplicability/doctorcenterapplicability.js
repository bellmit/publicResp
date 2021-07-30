function init() {
	initAddCenterDialog();
	initEditCenterDialog();
	if (max_centers > 1) {
		var app_for_centers = document.getElementsByName('applicable_for_centers');
		var checked = false;
		for (var i=0; i<app_for_centers.length; i++) {
			if (app_for_centers[i].checked) {
				selected_for = app_for_centers[i].value;
				break;
			}
		}
		toggleCenterAddIcon(selected_for == 'few');
	}
}

function allowAddingCenter(val) {
	toggleCenterAddIcon(val == 'few');
}

function toggleCenterAddIcon(enableAdd) {
		document.getElementById('btnAddCenter').disabled = !enableAdd;
		document.getElementById('centerAddIconEnabled').style.display = enableAdd ? 'block' : 'none';
		document.getElementById('centerAddIconDisabled').style.display = !enableAdd ? 'block' : 'none';
}

function saveForm() {
	var app_for_centers = document.getElementsByName('applicable_for_centers');
	if (max_centers > 1) {
		var checked = false;
		var selected_for;
		for (var i=0; i<app_for_centers.length; i++) {
			if (app_for_centers[i].checked) {
				checked = true;
				selected_for = app_for_centers[i].value;
				break;
			}
		}
		if (!checked) {
			alert("Please select the doctor applicability for centers");
			return false;
		}
		var sel_for_delete = document.getElementsByName("cntr_delete");
		var centersFound = false;
		for (var c=0; c<sel_for_delete.length-1; c++) {
			if (sel_for_delete[c].value == 'false') {
				centersFound = true;
				break;
			}
		}
		if (selected_for == 'few' && !centersFound) {
			alert("Please enter atleast one center");
			return false;
		}
	}

	document.doctor_center_association_form.submit();
	return true;
}

function populateCities() {
	var stateId = document.getElementById('d_state').value;
	var city = document.getElementById('d_city');
	city.options.length = 1;
	for (var i=0; i<citiesJSON.length; i++) {
		var record = citiesJSON[i]
		if (empty(stateId) || stateId == record.state_id) {
			var len = city.options.length;
			city.options.length = len+1;
			city.options[len].text = record.city_name;
			city.options[len].value = record.city_id;
		}
	}
}

var addCenterDialog = null;
function initAddCenterDialog() {
	var dialogDiv = document.getElementById("addCenterDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addCenterDialog = new YAHOO.widget.Dialog("addCenterDialog",
			{	width:"450px",
				context : ["addCenterDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_center_ok', 'click', addToCenterTable, addCenterDialog, true);
	YAHOO.util.Event.addListener('d_center_cancel', 'click', cancelCenterDialog, addCenterDialog, true);
	YAHOO.util.Event.addListener('d_search_centers', 'click', getCenters, addCenterDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelCenterDialog,
	                                                scope:addCenterDialog,
	                                                correctScope:true } );
	addCenterDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addCenterDialog.render();
}

function showAddCenterDialog(obj) {
	var row = getThisRow(obj);

	clearCenterFields();
	addCenterDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addCenterDialog.show();

	return false;
}

function showAllCenters(obj) {
	document.getElementById('d_state').value = '';
	document.getElementById('d_city').value = '';
	if (obj.checked) {
		searchCenters(1);
	} else {
		var ctable = document.getElementById('avlbl_centers_table');
		for (var i=1; i<ctable.rows.length-1; ) {
			ctable.deleteRow(i);
		}
		var div = document.getElementById('paginationDiv');
		div.innerHTML = '';
	}

}

function getCenters() {
	searchCenters(1);
}

function searchCenters(currentPage) {
	var ctable = document.getElementById('avlbl_centers_table');
	for (var i=1; i<ctable.rows.length-1; ) {
		ctable.deleteRow(i);
	}
	var curPage = 1;
	if (!empty(currentPage))
		curPage = parseInt(currentPage);
	var stateId = document.getElementById('d_state').value;
	var cityId = document.getElementById('d_city').value;

	var centers = null;
	if (!empty(cityId))
		centers = filterList(centersJSON, "city_id", cityId);
	else if (!empty(stateId))
		centers = filterList(centersJSON, "state_id", stateId);
	else if (empty(cityId) && empty(stateId))
		centers = centersJSON;
	centers = filterList(centers, "status", "A");
	var numPages = 0;
	if (!empty(centers)) {
		numPages = centers.length/10;
	}
	generatePaginationSection(curPage, numPages);
	var offSet = curPage == 1 ? 0 : ((curPage-1)*10);
	if (!empty(centers)) {
		for (var i=offSet; i<offSet+10; i++) {
			if (i==centers.length) break;
			if (centers[i].status != 'A') continue;

			var id = getNumCharges('avlbl_centers_table');
		   	var table = document.getElementById("avlbl_centers_table");
		   	var templateRow = table.rows[getTemplateRow('avlbl_centers_table')];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
		   	row.id = "avbl_center_row" + id;

			setHiddenValue(id, "d_center_name", centers[i].center_name);
			setHiddenValue(id, "d_state_name", centers[i].state_name);
			setHiddenValue(id, "d_city_name", centers[i].city_name);

			document.getElementsByName('d_center_chkbox')[id].value = centers[i].center_id;
			setNodeText(row.cells[1], centers[i].state_name, 20);
			setNodeText(row.cells[2], centers[i].city_name, 20);
			setNodeText(row.cells[3], centers[i].center_name, 20);
		}
	}
}

function generatePaginationSection(curPage, numPages) {
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';

	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'searchCenters('+(curPage-1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}
		if (curPage > 1 && curPage < numPages) {
			var txtEl = document.createTextNode(' | ');
			div.appendChild(txtEl);
		}
		if (curPage < numPages) {
			var txtEl = document.createTextNode('Next>>');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'searchCenters('+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}

function cancelCenterDialog() {
	addCenterDialog.cancel();
}

var centerIndex = 0;
var CNTR_STATE_NAME = centerIndex++, CNTR_CITY_NAME = centerIndex++, CENTER_NAME = centerIndex++
	CENTER_STATUS = centerIndex++, CNTR_TRASH_COL = centerIndex++ ;
function addToCenterTable() {
	var center_chkboxes = document.getElementsByName('d_center_chkbox');
	var state_name = document.getElementsByName('d_state_name');
	var city_name = document.getElementsByName('d_city_name');
	var center_name = document.getElementsByName('d_center_name');
	for (var i=0; i<center_chkboxes.length-1; i++) {
		var center_id = center_chkboxes[i].value;

		if (!center_chkboxes[i].checked) continue;
		if (isDuplicate('C', center_id)) continue;

		var id = getNumCharges('centers_table');
	   	var table = document.getElementById("centers_table");
		var templateRow = table.rows[getTemplateRow('centers_table')];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	   	row.id = "centerRow" + id;

		setNodeText(row.cells[CNTR_STATE_NAME], state_name[i].value);
		setNodeText(row.cells[CNTR_CITY_NAME], city_name[i].value);
		setNodeText(row.cells[CENTER_NAME], center_name[i].value);
		setNodeText(row.cells[CENTER_STATUS], 'Active');

		setHiddenValue(id, "doc_center_id", "_");
		setHiddenValue(id, "center_name", center_name[i].value);
		setHiddenValue(id, "state_name", state_name[i].value);
		setHiddenValue(id, "city_name", city_name[i].value);
		setHiddenValue(id, "center_id", center_id);
		setHiddenValue(id, "center_status", "A");

	}
	clearCenterFields();
	addCenterDialog.align("tr", "tl");
	return id;
}

function isDuplicate(type, id) {
	var els = document.getElementsByName(type == 'S' ? 'doctor_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'doctor_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}

function foundDuplicate(type, id, index) {
	var els = document.getElementsByName(type == 'S' ? 'doctor_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'doctor_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (index != i && els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}
function clearCenterFields() {
	var table = document.getElementById('avlbl_centers_table');
	for (var i=1; i<table.rows.length-1; ) {
		table.deleteRow(i);
	}
	document.getElementById('d_state').value = '';
	document.getElementById('d_city').value = '';
	populateCities();
	document.getElementById('show_all_centers_chkbox').checked = false;
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';
}

var editCenterDialog = null;
function initEditCenterDialog() {
	var dialogDiv = document.getElementById("editCenterDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	editCenterDialog = new YAHOO.widget.Dialog("editCenterDialog",
			{	width:"400px",
				context : ["editCenterDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('edit_center_ok', 'click', editCenterRow, editCenterDialog, true);
	YAHOO.util.Event.addListener('edit_center_cancel', 'click', cancelEditCenterDialog, editCenterDialog, true);
	YAHOO.util.Event.addListener('edit_center_previous', 'click', openPreviousCenter, editCenterDialog, true);
	YAHOO.util.Event.addListener('edit_center_next', 'click', openNextCenter, editCenterDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelEditCenterDialog,
	                                                scope:editCenterDialog,
	                                                correctScope:true } );
	editCenterDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	editCenterDialog.render();
}

function showEditCenterDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editCenterDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editCenterDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('center_edit_row_id').value = id;
	document.getElementById('ed_center_label').textContent = getIndexedValue('center_name', id);
	document.getElementById('ed_state_label').textContent = getIndexedValue('state_name', id);
	document.getElementById('ed_city_label').textContent = getIndexedValue('city_name', id);
	document.getElementById('ed_center_status').value = getIndexedValue('center_status', id);

	return false;

}

function editCenterRow() {
	var id = document.getElementById('center_edit_row_id').value;
	var row = getChargeRow(id, 'centers_table');

	var status = document.getElementById('ed_center_status').value;
	setNodeText(row.cells[CENTER_STATUS], status == 'A' ? 'Active' : 'Inactive');

	setHiddenValue(id, "center_status", status);

	setIndexedValue("cntr_edited", id, 'true');
	setCenterRowStyle(id);

	editCenterDialog.cancel();
	return true;
}

var centerFieldEdited = false;
function setCenterFieldEdited() {
	centerFieldEdited = true;
}

function openPreviousCenter() {
	var id = document.getElementById('center_edit_row_id').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'centers_table');

	if (centerFieldEdited) {
		if (!editCenterRow()) return false;
		centerFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditCenterDialog(document.getElementsByName('_editCenterAnchor')[parseInt(id)-1]);
	}
}

function openNextCenter() {
	var id = document.getElementById('center_edit_row_id').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'centers_table');

	if (centerFieldEdited) {
		if (!editCenterRow()) return false;
		centerFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('centers_table').rows.length-2) {
		showEditCenterDialog(document.getElementsByName('_editCenterAnchor')[parseInt(id)+1]);
	}
}

function cancelEditCenterDialog() {
	centerFieldEdited = false;
	var id = document.getElementById("center_edit_row_id").value;
	var row = getChargeRow(id, "centers_table");
	YAHOO.util.Dom.removeClass(row, 'editing');
	editCenterDialog.cancel();
}

function setCenterRowStyle(i) {
	var row = getChargeRow(i, 'centers_table');
	var packCenterId = getIndexedValue("doc_center_id", i);

 	var flagImgs = row.cells[CNTR_STATE_NAME].getElementsByTagName("img");
	var trashImgs = row.cells[CNTR_TRASH_COL].getElementsByTagName("img");

	var added = (packCenterId.substring(0,1) == "_");
	var cancelled = getIndexedValue("cntr_delete", i) == 'true';
	var edited = getIndexedValue("cntr_edited", i) == 'true';


	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var flagSrc;
	var showFlag;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
		showFlag = true;
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
		showFlag = false;
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0]) {
		flagImgs[0].src = flagSrc;
		flagImgs[0].style.display = showFlag ? 'block' : 'none';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelCenter(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	if (foundDuplicate('C', getIndexedValue("center_id", id), id)) {
		alert("Center alreday exists.");
		return false;
	}
	var oldDeleted =  getIndexedValue("cntr_delete", id);
	var isNew = getIndexedValue("doc_center_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		centerIndex--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("cntr_delete", id, newDeleted);
		setIndexedValue("cntr_edited", id, "true");
		setCenterRowStyle(id)
	}

	return false;
}




function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.doctor_center_association_form, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.doctor_center_association_form, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.doctor_center_association_form, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.doctor_center_association_form, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


