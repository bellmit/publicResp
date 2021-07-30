/**
 * 
 */

var centerIndex = 0;
var CNTR_STATE_NAME = centerIndex++, CNTR_CITY_NAME = centerIndex++, CENTER_NAME = centerIndex++, CNTR_TRASH_COL = centerIndex++ ;

function saveForm(theForm) {
	if (max_centers == 1) {
		alert("Center group option is available only for multi center hospital.");
			return false;
	}
	if (max_centers > 1) {
		var checked = false;
		var selected_for;
		var selectedCenterGroup = document.center_group_association_form.center_group_id.value;
		var centerGroupName = trim(document.center_group_association_form.center_group_name.value);
		var status = document.center_group_association_form.status.value;
		var sel_for_delete = document.getElementsByName("cntr_delete");
		var centersFound = false;
		for (var c=0; c<sel_for_delete.length-1; c++) {
			if (sel_for_delete[c].value == 'false') {
				centersFound = true;
				break;
			}
		}
		var count = 0;
		for(var i=0;i<userCenterGroups.length;i++){
			item = userCenterGroups[i];
			if(!empty(selectedCenterGroup)) {
				if(selectedCenterGroup == item.report_center_id) {
					if (status == 'I') {
						alert("User already associated with this center group, so we can not mark it as inactive");
						return false;
					} 
				}
			}
		}
		if (centerGroupName == "") {
			alert("Center group name should not be empty");
				return false;
		}
		if (!centersFound && status == 'A') {
			alert("Please enter atleast one center");
			return false;
		}
		if (centersFound && status == 'I') {
			alert("Centers already associated with this center group, so we can not mark it as inactive.");
			return false;
		}
	}


	document.forms[theForm].submit();
	return true;
}

function init() {
	initAddCenterDialog();
	setUniqCenterId("center_group_id");
}


function addToCenterTable() {
	var center_chkboxes = document.getElementsByName('d_center_chkbox');
	var state_name = document.getElementsByName('d_state_name');
	var city_name = document.getElementsByName('d_city_name');
	var center_name = document.getElementsByName('d_center_name');
	var center_group_id = document.getElementsByName('center_group_id');
	for (var i=0; i<center_chkboxes.length-1; i++) { 
		var center_id = center_chkboxes[i].value;

		if (!center_chkboxes[i].checked) continue;
		if (isDuplicate('C', center_id)) continue;

		var id = getNumCentersAdded('centers_table');
	   	var table = document.getElementById("centers_table");
		var templateRow = table.rows[getTemplateRow('centers_table')];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	   	row.id = "centerRow" + id;

		setNodeText(row.cells[CNTR_STATE_NAME], state_name[i].value);
		setNodeText(row.cells[CNTR_CITY_NAME], city_name[i].value);
		setNodeText(row.cells[CENTER_NAME], center_name[i].value);

		setHiddenValue(id, "center_group_assoc_id", "_");
		setHiddenValue(id, "center_name", center_name[i].value);
		setHiddenValue(id, "state_name", state_name[i].value);
		setHiddenValue(id, "city_name", city_name[i].value);
		setHiddenValue(id, "center_id", center_id);

	}
	clearCenterFields();
	addCenterDialog.align("tr", "tl");
	return id;
}

function cancelCenterDetails(obj) {

	var row = getThisRow(obj);
	var id = getRowCenterIndex(row);
	if (foundDuplicate('C', getIndexedValue("center_id", id), id)) {
		alert("Center alreday exists.");
		return false;
	}
	var oldDeleted =  getIndexedValue("cntr_delete", id);
	var isNew = getIndexedValue("center_group_assoc_id", id) == '_';
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


function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.center_group_association_form, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.center_group_association_form, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.center_group_association_form, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function isDuplicate(type, id) {
	var els = document.getElementsByName(type == 'S' ? 'center_group_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'center_grp_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}

function foundDuplicate(type, id, index) {
	var els = document.getElementsByName(type == 'S' ? 'center_group_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'center_grp_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (index != i && els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}
