function init() {
	initAddCenterDialog();
	initEditCenterDialog();
	hideresultApplicability();
	filterTableByLabel();
	getNumRows();
	//isExpressionResult();

	var filterLength = document.getElementById('resultlabel_id').options.length;
	if (filterLength == 2) {
		document.getElementById('resultlabel_id').options.selectedIndex = 1;
		document.getElementById('hideandShow').style.display = 'block';
		document.getElementById('resultlabel_id').onchange();
	}

	var resultLabelIdAftSave = document.getElementById('resultLabelIdSaved').value;
	if (resultLabelIdAftSave != null && resultLabelIdAftSave != "") {
		document.getElementById('resultlabel_id').value = resultLabelIdAftSave;
		hideresultApplicability();
		filterTableByLabel();
		document.getElementById('resultlabel_id').onchange();
	}
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

function hideresultApplicability() {
	var filterLabelId =  document.diag_center_association_form.resultlabel_id.value;
	if (filterLabelId == '') {
	document.getElementById('hideandShow').style.display ='none';
	} else {
	document.getElementById('hideandShow').style.display ='block';
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
var selected_for;
function saveForm() {
	var app_for_centers = document.getElementsByName('applicable_for_centers');
	if (max_centers > 1) {
		var checked = false;
		for (var i=0; i<app_for_centers.length; i++) {
			if (app_for_centers[i].checked) {
				checked = true;
				selected_for = app_for_centers[i].value;
				break;
			}
		}
		if (!checked) {
			alert("Please select the diag result applicability for centers");
			return false;
		}

		var filterLabelId =  document.diag_center_association_form.resultlabel_id.value;
		if (filterLabelId == '') {
			alert("Please Select Result Label");
			document.diag_center_association_form.resultlabel_id.focus();
			return false;
		}

		var counter = 0;
		var labelgrid = document.getElementsByName('resultlabel_id_grid');
		var table = document.getElementById("centers_table");
		var length = table.rows.length - 1;

		for (var i = 1; i <= length; i++) {
			var row = table.rows[i];
			var rowstyle = row.style.display;
			if (rowstyle != "") {
				counter = counter+1;
			}
		}
		if (document.getElementById("applicablefew").checked == true && length == counter ) {
				alert("Add atleast one Center for selected Result Label");
				return false;
		}


			var actRowCount=0;
			var delRowCount=0;

			var table = document.getElementById('centers_table');
			var delObj = document.getElementsByName('cntr_delete');
			 for(var i=1; i<table.rows.length ; i++){
				 var row = table.rows[i];
				 if(row.style.display == 'none'){
					// alert('hidden');
				 } else {
					 actRowCount = actRowCount + 1;
					// alert('active')
				 }

				 var isDelete = delObj[i-1].value;
				// alert(isDelete);
				 if(isDelete == 'true' || isDelete == true){
					 delRowCount = delRowCount + 1;
					// alert('delete');
				 } else {
					// alert('not to delete');
				 }
			 }

			 if(delRowCount == actRowCount && document.getElementById("applicablefew").checked == true){
				 alert('Add atleast one Center for selected Result Label');
				 return false;
			 }


	document.diag_center_association_form.submit();
	return true;
	}
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

	var filterLabelId =  document.diag_center_association_form.resultlabel_id.value;
	if (filterLabelId == '') {
		alert("Please Select Result Label");
		document.diag_center_association_form.resultlabel_id.focus();
		return false;
	}

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
	CENTER_STATUS = centerIndex++,  CNTR_TRASH_COL = centerIndex++ ;
function addToCenterTable() {

	var center_chkboxes = document.getElementsByName('d_center_chkbox');
	var state_name = document.getElementsByName('d_state_name');
	var city_name = document.getElementsByName('d_city_name');
	var center_name = document.getElementsByName('d_center_name');

	for (var i=0; i<center_chkboxes.length-1; i++) {
		var center_id = center_chkboxes[i].value;
		var center_add = center_name[i].value;
		var centersArray = center_chkboxes[i];
		if (!center_chkboxes[i].checked) continue;
		if (isDuplicate('C', center_id)) continue;
		/*if (!isExpressionResult(center_id,center_add)) continue;*/

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

		setHiddenValue(id, "result_center_id", "_");
		//setHiddenValue(id, "resultlabel_id", reultLabelId[i]);
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
	var labelgrid = document.getElementsByName('resultlabel_id_grid');
	var centerIds = document.getElementsByName('center_id');
	var resultLabelid = document.getElementById('resultlabel_id').value;
	var tableRow = document.getElementById('centers_table');
	var length = tableRow.rows.length-1;

	for (var j = 0; j <labelgrid.length -1; j++) {
		var labelId = labelgrid[j].value;
		var centId = centerIds[j].value;

		if (labelId != "") {
				if (resultLabelid == labelId && id == centId) {
				var els = document.getElementsByName(type == 'S' ? 'resultlabel_id' : 'center_id');
				var delete_el = document.getElementsByName(type == 'S' ? 'resultlabel_delete' : 'cntr_delete');
				for (var i=0; i<els.length; i++) {
					if (els[i].value == id && delete_el[i].value == 'false' )
					return true;
				}
			  }
			} else {
				if (labelId == "" && id == centId ) {
				var els = document.getElementsByName(type == 'S' ? 'resultlabel_id' : 'center_id');
				var delete_el = document.getElementsByName(type == 'S' ? 'resultlabel_delete' : 'cntr_delete');
				for (var i=0; i<els.length; i++) {
					if (els[i].value == id && delete_el[i].value == 'false' )
					return true;
				}
			}
		}
	}
	return false;
}

function foundDuplicate(type, id, index) {
	var labelgrid = document.getElementsByName('resultlabel_id_grid');
	var centerIds = document.getElementsByName('center_id');
	var resultLabelid = document.getElementById('resultlabel_id').value;

	for (var j=0; j<labelgrid.length-1 ; j++) {
		var labelId = labelgrid[j].value;
		var centId = centerIds[j].value;

		if (resultLabelid == labelId) {
			var els = document.getElementsByName(type == 'S' ? 'resultlabel_id' : 'center_id');
			var delete_el = document.getElementsByName(type == 'S' ? 'resultlabel_delete' : 'cntr_delete');
			for (var i=0; i<els.length; i++) {
					if (index != i && els[i].value == id && delete_el[i].value == 'false')
					return true;
			}
		}
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
	document.getElementsByName('d_checkAllCenters').checked = false;
	document.getElementsByName('d_center_chkbox').checked = false;
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

	var filterLabelId =  document.diag_center_association_form.resultlabel_id.value;
	if (filterLabelId == '') {
		alert("Please Select Result Label");
		document.diag_center_association_form.resultlabel_id.focus();
		return false;
	}
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

	for (var i=id; i<document.getElementById('centers_table').rows.length-2; i--) {

		if (i-1 != document.getElementById('centers_table').rows.length-2) {
			if (i>0) {
				var disp = getChargeRow(i-1, 'centers_table').style.display;
				if (disp == "") {
						showEditCenterDialog(document.getElementsByName('_editCenterAnchor')[parseInt(i)-1]);
							break;
				}
			} else {
				break;
			}
		}
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

	for (var i=id; i<document.getElementById('centers_table').rows.length-2; i++) {

		if (i+1 != document.getElementById('centers_table').rows.length-2) {
			var disp = getChargeRow(i+1, 'centers_table').style.display;
			if (disp == "") {
				showEditCenterDialog(document.getElementsByName('_editCenterAnchor')[parseInt(i)+1]);
				break;
			}
		}

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
	var packCenterId = getIndexedValue("result_center_id", i);

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

	deletOp = true;

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);


	if (foundDuplicate('C', getIndexedValue("resultlabel_id", id), id)) {
		alert("Center alreday exists.");
		return false;
	 }
	var oldDeleted =  getIndexedValue("cntr_delete", id);
	var isNew = getIndexedValue("result_center_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		centerIndex--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true') {
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
	return getElementPaise(getIndexedFormElement(document.diag_center_association_form, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.diag_center_association_form, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.diag_center_association_form, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.diag_center_association_form, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


function getNumRows() {
	var table = document.getElementById("centers_table");
	var length = table.rows.length - 1;
	var filterLabelId =  document.diag_center_association_form.resultlabel_id.value;

	for (var i = 1; i <= length; i++) {
		var row = table.rows[i];
		var rowstyle = row.style.display;
		var rowResultLabelId  = getElementByName(row, 'resultlabel_id_grid').value;
		var centerId = getElementByName(row, 'center_id').value;

		if (rowstyle == 'none') {
		var hiddenRow = document.getElementsByName("cntr_delete");
		for (var k=0; k<hiddenRow.length-1 ;k++) {
			if (hiddenRow[k].value == "true") {
				document.getElementsByName('cntr_edited')[k].value = 'false';
				document.getElementsByName('cntr_delete')[k].value = 'false';
				document.getElementsByName('imgDelete')[k].src = cpath+'/icons/delete.gif';
			}
		  }
		}

		if (rowstyle != 'none') {
			if (filterLabelId == rowResultLabelId && centerId !=0) {
				document.getElementById("applicablefew").checked = true;
				toggleCenterAddIcon(true);
			} else {
				document.getElementById("applicableall").checked = true;
				toggleCenterAddIcon(false);
			}
		}
		var show = true;
		if (filterLabelId == "")
		 	show = false;

		if ( filterLabelId != '' && rowResultLabelId != filterLabelId)
			show = false;
		if (show && centerId != 0) {//
			row.style.display = "";
		} else {
			row.style.display = "none";
		}
	}
}

function filterTableByLabel() {
   	var table = document.getElementById("centers_table");
   	var length = table.rows.length - 1;
   	var filterLabelId =  document.diag_center_association_form.resultlabel_id.value;

	for (var i = 1; i <= length; i++) {
		var row = table.rows[i];
		var rowResultLabelId  = getElementByName(row, 'resultlabel_id_grid').value;
		var centerId = getElementByName(row, 'center_id').value;

		if (filterLabelId == rowResultLabelId && centerId !=0) {
			document.getElementById("applicablefew").checked = true;
			toggleCenterAddIcon(true);
		} else {
			document.getElementById("applicableall").checked = true;
			toggleCenterAddIcon(false);
		}

		var show = true;
		if (filterLabelId == "")
		 	show = false;

		if ( filterLabelId != '' && rowResultLabelId != filterLabelId)
			show = false;
		if (show && centerId != 0) {
			row.style.display = "";
		} else {
			row.style.display = "none";
		}
	}

	}

function changeGridStatus() {
	var hiddenRow = document.getElementsByName("cntr_delete");
	for (var k=0; k<hiddenRow.length-1 ;k++) {
			if (hiddenRow[k].value == "true") {
				document.getElementsByName('cntr_edited')[k].value = 'false';
				document.getElementsByName('cntr_delete')[k].value = 'false';
				document.getElementsByName('imgDelete')[k].src = cpath+'/icons/delete.gif';
			}
	  }
}

function changeDropDown() {
	var resultBeforeSave = document.getElementsByName('result_center_id');
	var table = document.getElementById("centers_table");
   	var rowCount = table.rows.length;
   	var k = 0;
	for (var j = 2; j <= rowCount; j++) {
			if (resultBeforeSave[k].value == "_") {
				table.deleteRow(k+1);
				table = document.getElementById("centers_table");
				rowCount = table.rows.length+1;
				k=-1;
				resultBeforeSave = document.getElementsByName('result_center_id');
			}
		rowCount--;
		j--;
		k++;
	}
}


/*function isExpressionResult(center_id , center_add) {
	
	var resultlabelSelect = document.getElementById("resultlabel_id");
	var resultlabelText = resultlabelSelect.options[resultlabelSelect.selectedIndex].text;
	var resultToCompare = '';
	var methodToCompare = '';
	if(resultlabelText.indexOf('(') !== -1)
	{
		resultToCompare = resultlabelText.substring(0, resultlabelText.indexOf(' ('));
		methodToCompare = resultlabelText.match(/\(([^)]+)\)/)[1];
	} else {
		resultToCompare = resultlabelText;
	}
	 var expression = '';
	if (testExpressions != '') {
		var expressionLabels = [];
		var resultWithExpression = [];
		var methodWithExpression = [];
		for (var exp = 0; exp<testExpressions.length ;exp++) {
			expression = testExpressions[exp].expr_4_calc_result;
			resultWithExpression.push(testExpressions[exp].resultlabel);
			methodWithExpression.push(testExpressions[exp].mathod_name);
			if ( expression != '') {
				var pattern = /\[\"(.*?)\"]/g;
				var match;
				while ((match = pattern.exec(expression)) != null)
				{
					expressionLabels.push(match[1]);
				}
			}
		}
		
		var methodArray = [];
		var resultlabelArray = [];
		if (matches != '' ) {
			for (var m = 0;m<expressionLabels.length;m++) {
				var resultAssociation = expressionLabels[m];
				resultAssociation = resultAssociation.split('.');
				resultlabelArray.push(resultAssociation[0]);
				methodArray.push(resultAssociation[1]);
			}
		}
		
		if (resultlabelArray != '') { 
			for (var res= 0;res<resultlabelArray.length;res++) {
				var resultexists ='';
				var resultexistsIndex = resultlabelArray.indexOf(resultToCompare);
				if (methodToCompare != '') {
					var methodexists = methodArray.indexOf(methodToCompare);
					resultexists = resultlabelArray[methodexists];
				} 
					
				if (resultexists != -1 && ((resultexists != -1 && methodToCompare != '') || methodToCompare != ''))
				if ((resultexists == resultToCompare) || (resultexistsIndex != -1 && methodexists != undefined) ) {
					alert(resultlabelText+" " +"ResultLabel is associated with the Expression.");
					return false;
				} else {
					var resultExp = resultWithExpression.indexOf(resultToCompare);
					if (methodToCompare != '') 
						var methodExp = methodWithExpression.indexOf(methodToCompare);
					if ( resultExp != -1 && ((resultExp != -1 && methodExp != -1) || methodExp == undefined)) {//&& ((resultExp != -1 && methodToCompare != '') || methodToCompare != '')
						break;
					} else {
						return true;
					}
				}
			}			
		}
	}
	
	var resultSplitArray = [];
	var methodologyArray = [];
	
	for (var i=0; i<resultsJSON.length ; i++) {
		//var expression = resultsJSON[i].expr_4_calc_result;
		var testResultLabel = resultsJSON[i].resultlabel_id;
		var resulLabelName = resultsJSON[i].resultlabel;
		
		var matches = [];
		if (expression != '') {//&& resultlabel == testResultLabel
			var pattern = /\[\"(.*?)\"]/g;
			var match;
			while ((match = pattern.exec(expression)) != null)
			{
			  matches.push(match[1]);
			}
		}
		
		if (matches != '' ) {
			for (var m = 0;m<matches.length;m++) {
				var resultName = matches[m];
				resultName = resultName.split('.');
				resultSplitArray.push(resultName[0]);
   				methodologyArray.push(resultName[1]);
			}
		}
		
		var resultList = [];
		if (matches != '') {
			for (var l=0;l<matches.length;l++) {
				var testId = document.getElementById('test_id').value;
				var req = null;
				var mathodname; 
				var url = cpath+'/master/diagresultcenterapplicability.do?_method=getCentersRequest&resultLabel=' + resultSplitArray[l]
					+ "&methodname=" + methodologyArray[l] + "&test_id=" + testId ;
				var result = null;
				if (window.XMLHttpRequest) {
					req = new XMLHttpRequest();
				} else if (window.ActiveXObject) {
					req = new ActiveXObject("MSXML2.XMLHTTP");
				}
				req.open('POST', url.toString(), false);
				req.send(null);

				if (req.readyState == 4) {
					if (req.status == 200 && req.responseText != null) {
						result = eval(req.responseText);
						for (var k=0;k<result.length;k++) {
							resultList.push(result[k].center_id);
						}
					}
				}
			}
		}
		
		var count = 0;
		if (matches != '') {
			if (resultList != '') {
				for (var l=0;l<resultList.length;l++) {
					for (var c=0;c<center_id.length;c++) {
						if (resultList[l] == center_id[c] || resultList[l] == 0) {//comparing resultList with adding center Id
							count = count+1;
						}
					}
				}
			}

			var size = matches.length;//total number of elements in the list
			if (count !=0 && count%size == 0) {
				return true;
			} else {
				alert(center_add+" " + "is not Associated with one of the Result Labels");
				return false;
			}
		}
	}
	return true;
}*/
