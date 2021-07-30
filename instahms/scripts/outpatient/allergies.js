var MEDICINE_ALLERGY_TYPE_ID = "1";

function initAllergies() {
	initAllergyDialog();
	initEditAllergyDialog();
}

function allergyEntered() {
	return document.getElementById('allergiesTable').rows.length > 2;
}

var addAllergyDialog = null;
function initAllergyDialog() {
	var dialogAllergyDiv = document.getElementById("addAllergyDialog");
	if (dialogAllergyDiv == undefined) return;

	dialogAllergyDiv.style.display = 'block';
	addAllergyDialog = new YAHOO.widget.Dialog("addAllergyDialog",
		{
			width: "600px",
			context: ["addAllergyDialog", "tr", "br"],
			visible: false,
			modal: true,
			constraintoviewport: true
		});
	YAHOO.util.Event.addListener('Add_bt', 'click', addToAllergyTable, addAllergyDialog, true);
	YAHOO.util.Event.addListener('Close_bt', 'click', handleAddAllergyCancel, addAllergyDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys: 27 },
		{
			fn: handleAddAllergyCancel,
			scope: addAllergyDialog,
			correctScope: true
		});
	addAllergyDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addAllergyDialog.render();
}

function handleAddAllergyCancel() {
	if (childAllergyDialog == null) {
		parentAllergyDialog = null;
		this.cancel();
	}
}

var parentAllergyDialog = null;
var childAllergyDialog = null;
function showAddAllergyDialog(obj) {
	var row = getAllergyThisRow(obj);
	clearAllergyFields();
	addAllergyDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addAllergyDialog.show();
	document.getElementById('d_allergy_type').value = "";
	document.getElementById('d_allergy_type').focus();
	parentAllergyDialog = addAllergyDialog;
	return false;
}


var allergyColIndex = 0;
var ALLERGY_TYPE = allergyColIndex++, ALLERGY = allergyColIndex++, ONSET_DATE = allergyColIndex++, ALLERGY_REACTION = allergyColIndex++, SEVERITY = allergyColIndex++, ALLERGY_STATUS = allergyColIndex++,
	ALLERGY_TRASH_COL = allergyColIndex++, ALLERGY_EDIT_COL = allergyColIndex++;
var allergiesAdded = 0;

function addToAllergyTable() {

	var cell = null;
	var allergyId = document.getElementById('d_allergy_id').value;
	var allergenCodeId = document.getElementById('d_allergen_code_id').value;
	var genericCode = document.getElementById('d_generic_code').value;
	var allergyType = document.getElementById('d_allergy_type').value;
	var status = document.getElementById('d_status').options[document.getElementById('d_status').selectedIndex].value;
	var allergy = document.getElementById('d_allergy').value;
	var onSetDate = document.getElementById('d_onset_date').value;
	var reaction = document.getElementById('d_reaction').value;
	var severity = document.getElementById('d_severity').options[document.getElementById('d_severity').selectedIndex].value;

	if (allergyType != "") {
		if (allergy.trim() == "") {
			alert("Allergy cannot be empty");
			document.getElementById('d_allergy').value = '';

			return false;
		}

		if (allergyType == MEDICINE_ALLERGY_TYPE_ID && (allergy == "" || genericCode == "")) {
			alert("Please select a valid medicine allergy");
			return false;
		}

		if (allergyType != MEDICINE_ALLERGY_TYPE_ID && allowFreeTextEntries != "Y" && allergenCodeId == "") {
			alert("Please select a valid allergy");
			return false;
		}
	}

	var id = getAllergyNumCharges('allergiesTable');
	var table = document.getElementById("allergiesTable");
	var templateRow = table.rows[getAllergyTemplateRow('allergiesTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.id = "itemRow" + id;
	var allergyTypeNameString = setAllergyTypeName(allergyType);
	setNodeText(row.cells[ALLERGY_TYPE], allergyTypeNameString);
	setNodeText(row.cells[ALLERGY], allergy, 500);
	setNodeText(row.cells[ONSET_DATE], onSetDate, 50);
	setNodeText(row.cells[ALLERGY_REACTION], reaction, 2000);
	setNodeText(row.cells[SEVERITY], severity == 'Mild' ? 'Mild' : severity == 'Moderate' ? 'Moderate' : severity == 'Severe' ? 'Severe' : '');
	setNodeText(row.cells[ALLERGY_STATUS], status == 'A' ? 'Active' : 'Inactive');

	setAllergyHiddenValue(id, "allergy_id", '_');
	setAllergyHiddenValue(id, "allergy_type", allergyType);
	setAllergyHiddenValue(id, "allergen_code_id", allergenCodeId);
	setAllergyHiddenValue(id, "generic_code", genericCode);
	setAllergyHiddenValue(id, "allergy", allergy);
	setAllergyHiddenValue(id, "reaction", reaction);
	setAllergyHiddenValue(id, "onset_date", onSetDate);
	setAllergyHiddenValue(id, "severity", severity);
	setAllergyHiddenValue(id, "status", status);

	allergiesAdded++;
	clearAllergyFields();
	setAllergyRowStyle(id);
	addAllergyDialog.align("tr", "tl");
	document.getElementById('d_allergy_type').focus();
	return id;
}

function setAllergyHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[allergy_detatils_form], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function handleAllergyTypeChangeOnAdd() {
	var value = document.getElementById('d_allergy_type').value;
	if (value != "") {
		document.getElementById("d_status").disabled = false;
		document.getElementById("d_onset_date").disabled = false;
		document.getElementById("d_severity").disabled = false;
		document.getElementById("d_allergy").disabled = false;
		document.getElementById("d_reaction").disabled = false;
		document.getElementById('d_allergen_code_id').value = '';
		document.getElementById('d_generic_code').value = '';
		document.getElementById('d_allergy').value = '';
		document.getElementById('d_status').value = 'A';
		document.getElementById("d_onset_date").value = '';
		document.getElementById("d_severity").value = '';
		document.getElementById("d_reaction").value = '';
		initAutoAllergySearch(value);
	} else {
		//Reset to default state
		document.getElementById("d_onset_date").value = '';
		document.getElementById("d_severity").value = '';
		document.getElementById("d_allergy").value = '';
		document.getElementById("d_reaction").value = '';
		document.getElementById("d_onset_date").disabled = true;
		document.getElementById("d_severity").disabled = true;
		document.getElementById("d_allergy").disabled = true;
		document.getElementById("d_reaction").disabled = true;
	}
}

function clearAllergyFields() {
	document.getElementById('d_allergy_type').value = '';
	document.getElementById('d_allergy_id').value = '';
	document.getElementById('d_allergy').value = '';
	document.getElementById('d_allergen_code_id').value = '';
	document.getElementById('d_generic_code').value = '';
	document.getElementById('d_allergy').value = '';
	document.getElementById('d_onset_date').value = '';
	document.getElementById('d_reaction').value = '';
	document.getElementById('d_status').value = 'A';
	document.getElementById('d_severity').value = '';
	handleAllergyTypeChangeOnAdd();
}

function setAllergyRowStyle(i) {
	var row = getAllergyChargeRow(i, 'allergiesTable');
	var allergyId = getAllergyIndexedValue("allergy_id", i);

	var trashImgs = row.cells[ALLERGY_TRASH_COL].getElementsByTagName("img");

	var added = (allergyId.substring(0, 1) == "_");
	var cancelled = getAllergyIndexedValue("delAllergy", i) == 'true';
	var edited = getAllergyIndexedValue("Allergy_edited", i) == 'true';
	var allergyType = getAllergyIndexedValue("allergy_type", i);

	/*
	 * Pre-saved state is shown using background colours. The pre-saved states can be:
	 *  - Normal: no background
	 *  - Added: Greenish background
	 *  - Modified: Yellowish background
	 *    (includes cancelled, which is a change in the status attribute)
	 *
	 * Attributes are shown using flags. The only attribute indicated is the cancelled
	 * attribute, using a red flag.
	 *
	 * Possible actions using the trash icon are:
	 *  - Cancel/Delete an item: Normal trash icon.
	 *    (newly added items are deleted, saved items are cancelled)
	 *  - Un-cancel an item: Trash icon with a cross
	 *  - The item cannot be cancelled: Grey trash icon.
	 */

	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
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

	row.className = cls;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelAllergy(obj) {

	var row = getAllergyThisRow(obj);
	var id = getAllergyRowChargeIndex(row);
	var oldDeleted = getAllergyIndexedValue("delAllergy", id);

	var isNew = getAllergyIndexedValue("allergy_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		allergiesAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true') {
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setAllergyIndexedValue("delAllergy", id, newDeleted);
		setAllergyIndexedValue("Allergy_edited", id, "true");
		setAllergyRowStyle(id);
	}
	return false;
}

function initEditAllergyDialog() {
	var dialogAllergyDiv = document.getElementById("editAllergyDialog");
	dialogAllergyDiv.style.display = 'block';
	editAllergyDialog = new YAHOO.widget.Dialog("editAllergyDialog", {
		width: "600px",
		text: "Edit Allergy",
		context: ["allergiesTable", "tl", "tl"],
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys: 27 },
		{
			fn: handleEditAllergyCancel,
			scope: editAllergyDialog,
			correctScope: true
		});
	editAllergyDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editAllergyDialog.cancelEvent.subscribe(handleEditAllergyCancel);
	YAHOO.util.Event.addListener('edit_allergy_Ok', 'click', editAllegyTableRow, editAllergyDialog, true);
	YAHOO.util.Event.addListener('edit_allergy_Cancel', 'click', handleEditAllergyCancel, editAllergyDialog, true);
	YAHOO.util.Event.addListener('edit_allergy_Previous', 'click', openAllergyPrevious, editAllergyDialog, true);
	YAHOO.util.Event.addListener('edit_allergy_Next', 'click', openAllergyNext, editAllergyDialog, true);
	editAllergyDialog.render();
}

function handleEditAllergyCancel() {
	if (childAllergyDialog == null) {
		parentAllergyDialog = null;
		var id = document.forms[allergy_detatils_form].editAllergyRowId.value;
		var row = getAllergyChargeRow(id, "allergiesTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldAllergyEdited = false;
		this.hide();
	}
}

function showEditAllergyDialog(obj) {
	parentAllergyDialog = editAllergyDialog;
	var row = getAllergyThisRow(obj);
	var id = getAllergyRowChargeIndex(row);
	editAllergyDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editAllergyDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.forms[allergy_detatils_form].editAllergyRowId.value = id;
	document.getElementById('ed_allergy_type').value = getAllergyIndexedValue("allergy_type", id);
	handleAllergyTypeChangeOnEdit();
	document.getElementById('ed_allergy_id').value = getAllergyIndexedValue("allergy_id", id);
	document.getElementById('ed_status').value = getAllergyIndexedValue("status", id);
	document.getElementById('ed_allergy').value = getAllergyIndexedValue("allergy", id);
	document.getElementById('ed_onset_date').value = getAllergyIndexedValue("onset_date", id);
	document.getElementById('ed_reaction').value = getAllergyIndexedValue("reaction", id);
	document.getElementById('ed_severity').value = getAllergyIndexedValue("severity", id);
	document.getElementById('ed_allergen_code_id').value = getAllergyIndexedValue("allergen_code_id", id);
	document.getElementById('ed_generic_code').value = getAllergyIndexedValue("generic_code", id);
	document.getElementById('ed_allergy_type').focus();
	return false;
}

function validateAllergies() {
	var allergyTypes = document.getElementsByName('allergy_type');
	var allergyStatus = document.getElementsByName('status');
	var allergyDeleted = document.getElementsByName('delAllergy');
	var allergies = document.getElementsByName('allergy');
	var allergyIds = document.getElementsByName('allergy_id');
	let countAllergies = [];
	var noAllergyCount = 0;
	var allergyCount = 0;

	for (var i = 0; i < allergyTypes.length; i++) {
		if (allergyTypes[i].value == '' && allergyStatus[i].value == 'A' && allergyDeleted[i].value == 'false')
			noAllergyCount++;
		if (allergyTypes[i].value != '' && allergyDeleted[i].value == 'false') {
			if(allergyStatus[i].value == 'A')
			{
			allergyCount++;
			}
			var allergyString = allergies[i].value;
			var sanatizedString = allergyString.trim().replace(/\.+$/, "").replace(/^\.+/, "").trim();
			var duplicateFound =countAllergies.some(code => code.allergy === sanatizedString && code.type === allergyTypes[i].value);
			if (duplicateFound && allergyIds[i] != '') {
				showMessage('js.outpatient.consultation.mgmt.duplicate.allergies.exist');
				return false;
			}

			countAllergies.push({allergy : sanatizedString , type : allergyTypes[i].value});
		}
	}

	if (noAllergyCount > 1) {
		showMessage('js.outpatient.consultation.mgmt.duplicate.noallergies.exist');
		return false;
	}
	if (noAllergyCount > 0 && allergyCount > 0) {
		showMessage('js.outpatient.consultation.mgmt.noallergiesexist');
		return false;
	}
	return true;
}

function editAllegyTableRow() {
	var id = document.forms[allergy_detatils_form].editAllergyRowId.value;
	var row = getAllergyChargeRow(id, 'allergiesTable');

	var allergyType = document.getElementById('ed_allergy_type').value;
	var allergenCodeId = document.getElementById('ed_allergen_code_id').value;
	var genericCode = document.getElementById('ed_generic_code').value;
	var status = document.getElementById('ed_status').value;
	var allergyId = document.getElementById('ed_allergy_id').value;
	var allergy = document.getElementById('ed_allergy').value;
	var onSetDate = document.getElementById('ed_onset_date').value;
	var reaction = document.getElementById('ed_reaction').value;
	var severity = document.getElementById('ed_severity').value;

	if (allergyType != "") {
		if (allergy.trim() == "") {
			alert("Allergy cannot be empty");
			document.getElementById('d_allergy').value = '';
			return false;
		}

		if (allergyType == MEDICINE_ALLERGY_TYPE_ID && (allergy == "" || genericCode == "")) {
			alert("Please select a valid medicine allergy");
			return false;
		}

		if (allergyType != MEDICINE_ALLERGY_TYPE_ID && allowFreeTextEntries != "Y" && allergenCodeId == "") {
			alert("Please select a valid allergy");
			return false;
		}
	}

var allergyTypeNameString = setAllergyTypeName(allergyType);
setNodeText(row.cells[ALLERGY_TYPE], allergyTypeNameString);
setNodeText(row.cells[ALLERGY], allergy, 500);
setNodeText(row.cells[ONSET_DATE], onSetDate, 50);
setNodeText(row.cells[ALLERGY_REACTION], reaction, 2000);
setNodeText(row.cells[SEVERITY], severity == 'Mild' ? 'Mild' : severity == 'Moderate' ? 'Moderate' : severity == 'Severe' ? 'Severe' : '');
setNodeText(row.cells[ALLERGY_STATUS], status == 'A' ? 'Active' : 'Inactive');

setAllergyHiddenValue(id, "allergy_id", allergyId);
setAllergyHiddenValue(id, "allergy_type", allergyType);
setAllergyHiddenValue(id, "allergen_code_id", allergenCodeId);
setAllergyHiddenValue(id, "generic_code", genericCode);
setAllergyHiddenValue(id, "allergy_type", allergyType);
setAllergyHiddenValue(id, "allergy", allergy);
setAllergyHiddenValue(id, "reaction", reaction);
setAllergyHiddenValue(id, "onset_date", onSetDate);
setAllergyHiddenValue(id, "severity", severity);
setAllergyHiddenValue(id, "status", status);

YAHOO.util.Dom.removeClass(row, 'editing');

setAllergyIndexedValue("Allergy_edited", id, 'true');
setAllergyRowStyle(id);

editAllergyDialog.cancel();

return true;
}

var fieldAllergyEdited = false;
function setAllergiesEdited() {
	fieldAllergyEdited = true;
}

function openAllergyPrevious() {
	var id = document.forms[allergy_detatils_form].editAllergyRowId.value;
	id = parseInt(id);
	var row = getAllergyChargeRow(id, 'allergiesTable');

	if (fieldAllergyEdited) {
		if (!editAllegyTableRow()) return false;
		fieldAllergyEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditAllergyDialog(document.getElementsByName('_editAllergyAnchor')[parseInt(id) - 1]);
	}
	handleAllergyTypeChangeOnEdit();
}

function openAllergyNext() {
	var id = document.forms[allergy_detatils_form].editAllergyRowId.value;
	id = parseInt(id);
	var row = getAllergyChargeRow(id, 'allergiesTable');

	if (fieldAllergyEdited) {
		if (!editAllegyTableRow()) return false;
		fieldAllergyEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id + 1 != document.getElementById('allergiesTable').rows.length - 2) {
		showEditAllergyDialog(document.getElementsByName('_editAllergyAnchor')[parseInt(id) + 1]);
	}
	handleAllergyTypeChangeOnEdit();
}

function getAllergyNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length - 2;
}

function getFirstAllergyRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getAllergyTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getAllergyNumCharges(tableId) + 1;
}

function getAllergyChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstAllergyRow()];
}

function getAllergyRowChargeIndex(row) {
	return row.rowIndex - getFirstAllergyRow();
}

function getAllergyThisRow(node) {
	return findAncestor(node, "TR");
}

function getAllergyIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[allergy_detatils_form], name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function setAllergyIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[allergy_detatils_form], name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function handleAllergyTypeChangeOnEdit() {
	var value = document.getElementById('ed_allergy_type').value;
	if (value != "") {
		document.getElementById("ed_status").disabled = false;
		document.getElementById("ed_onset_date").disabled = false;
		document.getElementById("ed_severity").disabled = false;
		document.getElementById("ed_allergy").disabled = false;
		document.getElementById("ed_reaction").disabled = false;
		document.getElementById('ed_allergen_code_id').value = '';
		document.getElementById('ed_generic_code').value = '';
		document.getElementById('ed_allergy').value = '';
		document.getElementById('ed_status').value = 'A';
		document.getElementById("ed_onset_date").value = '';
		document.getElementById("ed_severity").value = '';
		document.getElementById("ed_reaction").value = '';
		initEditAutoAllergySearch(value);

	} else {
		document.getElementById("ed_onset_date").value = '';
		document.getElementById("ed_severity").value = '';
		document.getElementById("ed_allergy").value = '';
		document.getElementById("ed_reaction").value = '';
		document.getElementById("ed_onset_date").disabled = true;
		document.getElementById("ed_severity").disabled = true;
		document.getElementById("ed_allergy").disabled = true;
		document.getElementById("ed_reaction").disabled = true;
	}
}

function setAllergyTypeName(selectedAllergyTypeId) {
	var allergyTypesArray = JSON.parse(allergyTypes);

	for (var i = 0; i < allergyTypesArray.length; i++) {
		if (allergyTypesArray[i].allergy_type_id === parseInt(selectedAllergyTypeId)) {
			return allergyTypesArray[i].allergy_type_name;
		}
	}

	return "No Known Allergies";
}

var AutoComp = null;

function initAutoAllergySearch(value) {
	if (!empty(AutoComp)) {
		AutoComp.destroy();
		AutoComp = null;
	}
	if (value === "") {
		return;
	}
	var dataSource;
	if (value === MEDICINE_ALLERGY_TYPE_ID) {
		dataSource = new YAHOO.util.XHRDataSource(cpath + '/master/genericnames/lookup.json');

		dataSource.scriptQueryParam = "filterText";
		dataSource.scriptQueryAppend = "page_size=10";
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		dataSource.responseSchema = {
			resultsList: "dtoList",
			fields: [{ key: "generic_name" },
			{ key: "generic_code" }
			]
		};
	}
	else {
		dataSource = new YAHOO.util.XHRDataSource(cpath + '/genericform/allergieslookup.json');

		dataSource.scriptQueryParam = "filterText";
		dataSource.scriptQueryAppend = "page_size=10&contains=true&allergy_type_id=" + value;
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		dataSource.responseSchema = {
			resultsList: "dtoList",
			fields: [{ key: "allergen_description" },
			{ key: "allergen_code_id" }
			]
		};
	}
	AutoComp = new YAHOO.widget.AutoComplete('d_allergy', 'allergy_dropdown', dataSource);
	AutoComp.allowBrowserAutocomplete = false;
	AutoComp.prehighlightClassName = "yui-ac-prehighlight";
	AutoComp.typeAhead = false;
	AutoComp.useShadow = false;
	AutoComp.animVert = false;
	AutoComp.minQueryLength = 1;
	AutoComp.filterResults = Insta.queryMatchWordStartsWith;

	AutoComp.formatResult = Insta.autoHighlight;
	var forceSelection = true;
	if (value !== MEDICINE_ALLERGY_TYPE_ID && allowFreeTextEntries === "Y") {
		forceSelection = false;
	}
	AutoComp.forceSelection = forceSelection;
	AutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	var itemSelectHandler = function(sType, aArgs) {
		var record = aArgs[2];
		document.getElementById("d_allergy").value = record[0];
		if (value === MEDICINE_ALLERGY_TYPE_ID) {
			document.getElementById('d_generic_code').value = record[1];
		}
		else {
			document.getElementById('d_allergen_code_id').value = record[1];
		}
	};
	AutoComp.itemSelectEvent.subscribe(itemSelectHandler);
}

var editAutoComp = null;

function initEditAutoAllergySearch(value) {
	if (!empty(editAutoComp)) {
		editAutoComp.destroy();
		editAutoComp = null;
	}
	if (value === "") {
		return;
	}
	var dataSource;
	if (value === MEDICINE_ALLERGY_TYPE_ID) {
		dataSource = new YAHOO.util.XHRDataSource(cpath + '/master/genericnames/lookup.json');

		dataSource.scriptQueryParam = "filterText";
		dataSource.scriptQueryAppend = "page_size=10";
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		dataSource.responseSchema = {
			resultsList: "dtoList",
			fields: [{ key: "generic_name" },
			{ key: "generic_code" }
			]
		};
	}
	else {
		dataSource = new YAHOO.util.XHRDataSource(cpath + '/genericform/allergieslookup.json');

		dataSource.scriptQueryParam = "filterText";
		dataSource.scriptQueryAppend = "page_size=10&contains=true&allergy_type_id=" + value;
		dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		dataSource.responseSchema = {
			resultsList: "dtoList",
			fields: [{ key: "allergen_description" },
			{ key: "allergen_code_id" }
			]
		};
	}
	editAutoComp = new YAHOO.widget.AutoComplete('ed_allergy', 'ed_allergy_dropdown', dataSource);
	editAutoComp.allowBrowserAutocomplete = false;
	editAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	editAutoComp.typeAhead = false;
	editAutoComp.useShadow = false;
	editAutoComp.animVert = false;
	editAutoComp.minQueryLength = 1;
	editAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	editAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	var forceSelection = true;
	if (value !== MEDICINE_ALLERGY_TYPE_ID && allowFreeTextEntries === "Y") {
		forceSelection = false;
	}
	editAutoComp.forceSelection = forceSelection;

	var itemSelectHandler = function(sType, aArgs) {
		var record = aArgs[2];
		document.getElementById("ed_allergy").value = record[0];
		if (value === MEDICINE_ALLERGY_TYPE_ID) {
			document.getElementById('ed_generic_code').value = record[1];
		}
		else {
			document.getElementById('ed_allergen_code_id').value = record[1];
		}
	};
	editAutoComp.itemSelectEvent.subscribe(itemSelectHandler);
}