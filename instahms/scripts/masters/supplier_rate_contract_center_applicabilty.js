var centerIndex = 0;
var CNTR_STATE_NAME = centerIndex++, CNTR_CITY_NAME = centerIndex++, CENTER_NAME = centerIndex++,
	CENTER_STATUS = centerIndex++, CNTR_TRASH_COL = centerIndex++ ;

function saveForm(theForm) {
	var app_for_centers = document.getElementsByName('applicable_for_centers');
	var selectedCenters = document.getElementsByName('center_id');
	var selectedCentersStatus = document.getElementsByName('center_status');
	
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
			alert("Please select supplier rate contract applicability for centers");
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
		if(status == 'A') {
			if(selected_for == 'few') {
				for(var i=0; i<selectedCenters.length; i++) {
					var centerId = selectedCenters[i].value;
					var centerStatus = selectedCentersStatus[i].value;
					if(sel_for_delete[i].value == 'false'  && centerStatus == 'A') {
						if(allotedCenters != null) {
							for(var j=0; j<allotedCenters.length; j++) {
								if(allotedCenters[j] == centerId) {
									alert(document.getElementsByName('center_name')[i].value+" has active supplier rate contract");
									return false;
								}
							}
						}						
					} 
					
				}
			} else {
				var centerId = '0';
				if(sel_for_delete[i].value == 'false') {
					for(var j=0; j<allotedCenters.length; j++) {
						if(allotedCenters[j] == centerId) {
							alert("All centers have active supplier rate contract");
							return false;
						}
					}
				} 
					
			}
		}	
		
	}

	document.forms[theForm].submit();
	return true;
}

function init() {
	initAddCenterDialog();
	initEditCenterDialog();
	setUniqCenterId("supplier_rate_contract_center_id");
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


function addToCenterTable() {
	var center_chkboxes = document.getElementsByName('d_center_chkbox');
	var state_name = document.getElementsByName('d_state_name');
	var city_name = document.getElementsByName('d_city_name');
	var center_name = document.getElementsByName('d_center_name');
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
		setNodeText(row.cells[CENTER_STATUS], 'Active');

		setHiddenValue(id, "supplier_rate_contract_center_id", "_");
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


function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.center_association_form, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.center_association_form, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.center_association_form, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function isDuplicate(type, id) {
	var els = document.getElementsByName(type == 'S' ? 'supplier_rate_contract_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'dis_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}

function foundDuplicate(type, id, index) {
	var els = document.getElementsByName(type == 'S' ? 'supplier_rate_contract_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'dis_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (index != i && els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}

