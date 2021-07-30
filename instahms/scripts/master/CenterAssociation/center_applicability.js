/**
 * This files has functions which can be used for Center applicability UI accross application.
 * Few dependent function required to write in own script file.
 */

function allowAddingCenter(val) {
	toggleCenterAddIcon(val == 'few');
}

var addCenterDialog = null;
var uniqCenterId = null;

function setUniqCenterId(tableWiseUniqueId) {
	uniqCenterId = tableWiseUniqueId;
}
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

function toggleCenterAddIcon(enableAdd) {
		document.getElementById('btnAddCenter').disabled = !enableAdd;
		document.getElementById('centerAddIconEnabled').style.display = enableAdd ? 'block' : 'none';
		document.getElementById('centerAddIconDisabled').style.display = !enableAdd ? 'block' : 'none';

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

			var id = getNumCentersAdded('avlbl_centers_table');
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
function editCenterRow() {
	var id = document.getElementById('center_edit_row_id').value;
	var row = getCenterRow(id, 'centers_table');

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
	var row = getCenterRow(id, 'centers_table');

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
	var row = getCenterRow(id, 'centers_table');

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
	sponsorFieldEdited = false;
	var id = document.getElementById("center_edit_row_id").value;
	var row = getCenterRow(id, "centers_table");
	YAHOO.util.Dom.removeClass(row, 'editing');
	editCenterDialog.cancel();
}

function setCenterRowStyle(i) {
	var row = getCenterRow(i, 'centers_table');
	var entityCenterId = getIndexedValue(uniqCenterId, i);

 	var flagImgs = row.cells[CNTR_STATE_NAME].getElementsByTagName("img");
	var trashImgs = row.cells[CNTR_TRASH_COL].getElementsByTagName("img");

	var added = (entityCenterId.substring(0,1) == "_");
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

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;


	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelCenter(obj) {

	var row = getThisRow(obj);
	var id = getRowCenterIndex(row);

	if (foundDuplicate('C', getIndexedValue("center_id", id), id)) {
		alert("Center alreday exists.");
		return false;
	}
	var oldDeleted =  getIndexedValue("cntr_delete", id);
	var isNew = getIndexedValue(uniqCenterId, id) == '_';

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


function getNumCentersAdded(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstItemRow() {
	return 1;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCentersAdded(tableId) + 1;
}


function getRowCenterIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getCenterRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function showEditCenterDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowCenterIndex(row);
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

