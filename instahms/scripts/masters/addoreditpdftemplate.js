
function init() {
	initImgFieldsDialog();
}

// Image Fields Dialog
function initImgFieldsDialog() {
	var dialogDiv = document.getElementById("imgFieldsDialog");
	dialogDiv.style.display = 'block';
	editImgDialog = new YAHOO.widget.Dialog("imgFieldsDialog",{
			width:"400px",
			text: "Image Fields Dialog",
			context :["imgFieldsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onCancel,
	                                                scope:editImgDialog,
	                                                correctScope:true } );
	editImgDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editImgDialog.render();
}

function showNextField() {
	var id = document.imgFieldsForm.imgRowId.value;
	var row = getImageFieldRow(id);
	var nRow = YAHOO.util.Dom.getNextSibling(row);
    if (nRow != null) {
		if (isFieldEdited(id)) {
			row.className = "edited";
		}else {
	        YAHOO.util.Dom.removeClass(row, 'editing');
		}
		editFieldDetails(id);
		var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[4]);
		showImgFieldsDialog(anchor);
    }
}

function showPreviousField() {
	var id = document.imgFieldsForm.imgRowId.value;
	var row = getImageFieldRow(id);
	var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
    var nPrevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
    if (nPrevRow != null) {

		if (isFieldEdited(id)) {
			row.className = "edited";
		}else {
	        YAHOO.util.Dom.removeClass(row, 'editing');
		}
		editFieldDetails(id);
        var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[4]);
        showImgFieldsDialog(anchor);
    }
}

function getImageFieldRow(i) {
	i = parseInt(i);
	var table = document.getElementById("imgFieldsTable");
	return table.rows[i + 1];
}

function getImageFieldTabRows() {
	var table = document.getElementById("imgFieldsTable");
	return  (table.rows.length - 1);
}

function deleteImgFields(obj) {
	var row = getThisRow(obj);
	if (row == null) return false;
	var id = row.rowIndex - 1;

	document.imgFieldsForm.imgRowId.value = id;

	var deleted	= getIndexedValue("field_delete",id);
	var fieldId = getIndexedValue("field_id", id);

	var deleteImg = row.cells[3].getElementsByTagName("img")[0];

	if (fieldId.substring(0,1) == '_') {
		row.parentNode.removeChild(row);

	} else {

		var trashSrc;

		if (deleted == 'N') {
			setHiddenValue(id, "field_delete", "Y");
			trashSrc = cpath + '/icons/undo_delete.gif';
			YAHOO.util.Dom.addClass(row, 'editing');

		}else {
			setHiddenValue(id, "field_delete", "N");
			trashSrc = cpath + '/icons/delete.gif';
			YAHOO.util.Dom.addClass(row, 'editing');
		}

		deleteImg.src = trashSrc;
	}

	return false;
}

function showImgFieldsDialog(obj, addnew) {

	if (typeof(addnew) != 'undefined') {
		var addBtn = document.getElementById("btnAddField");
		document.imgFieldsForm.imgRowId.value = "";

		editImgDialog.cfg.setProperty("context", [addBtn, "tr", "bl"], false);

		document.imgFieldsForm.field_name.value = "";
		document.imgFieldsForm.display_name.value = "";
		setSelectedIndex(document.imgFieldsForm.field_input, "");

		document.getElementById("prevFieldBtn").style.display = 'none';
		document.getElementById("nextFieldBtn").style.display = 'none';

		editImgDialog.show();
		document.imgFieldsForm.field_name.focus();

	}else {
		var row = getThisRow(obj);
		if (row == null) return false;
		var id = row.rowIndex - 1;
		if (checkAllNewFields()) {
			if (id == 0) return false;
		}

		YAHOO.util.Dom.addClass(row, 'editing');
		document.imgFieldsForm.imgRowId.value = id;

		editImgDialog.cfg.setProperty("context", [row.cells[4], "tr", "bl"], false);

		setFieldDetails();

		document.getElementById("prevFieldBtn").style.display = 'inline';
		document.getElementById("nextFieldBtn").style.display = 'inline';

		editImgDialog.show();
		document.imgFieldsForm.field_name.focus();
	}
	return false;
}

function validateFieldDetails() {
	var fldNameObj  = document.imgFieldsForm.field_name;
	var dispNameObj = document.imgFieldsForm.display_name;
	var fldInputObj = document.imgFieldsForm.field_input;

	var valid = true;
	valid = valid && validateRequired(fldNameObj, "Field name is required");
	valid = valid && validateRequired(dispNameObj, "Display name is required");
	valid = valid && validateRequired(fldInputObj, "Field input is required");

	return valid;
}

function checkAllNewFields() {
	var tabLen = getImageFieldTabRows();
	for (var i=0; i<tabLen; i++) {
		var fldId = getIndexedValue("field_id",i);
		if (!empty(fldId) && fldId != '_') {
			return false;
		}
	}
	return true;
}

function setFieldDetails() {
	var id = document.imgFieldsForm.imgRowId.value;

	var fldNameObj  = document.imgFieldsForm.field_name;
	var dispNameObj = document.imgFieldsForm.display_name;
	var fldInputObj = document.imgFieldsForm.field_input;

	var origFldName		= getIndexedValue("field_name",id);
	var origDispName	= getIndexedValue("display_name",id);
	var origFldInput	= getIndexedValue("field_input",id);

	fldNameObj.value = origFldName;
	dispNameObj.value = origDispName;
	setSelectedIndex(fldInputObj, origFldInput);
}

function onCancel() {
	var id = document.imgFieldsForm.imgRowId.value;
	if (!empty(id)) {
		var row = getImageFieldRow(id);
		YAHOO.util.Dom.removeClass(row, 'editing');
		editImgDialog.hide();
		var editImg = row.cells[4].childNodes[1];
		editImg.focus();
	}else {
		editImgDialog.hide();
		document.getElementById("btnAddField").focus();
	}
}

function onSubmit() {

	var id = document.imgFieldsForm.imgRowId.value;

	var fldName  = trim(document.imgFieldsForm.field_name.value);
	var dispName = trim(document.imgFieldsForm.display_name.value);
	var fldInput = document.imgFieldsForm.field_input.value;
	var fldInputTxt = document.imgFieldsForm.field_input.options[document.imgFieldsForm.field_input.selectedIndex].text;

	// Add new field
	if (id == '') {

		var table = document.getElementById("imgFieldsTable");
		var id = getImageFieldTabRows();
		var lastRow = table.rows[id];

		if (!checkDuplicateFields(id))
			return false;

		if (!validateFieldDetails())
			return false;

		var newRow = null;
		if (id > 0) {
			newRow = lastRow.cloneNode(true);
			table.tBodies[0].insertBefore(newRow, lastRow);
			newRow = getImageFieldRow(id);
			newRow.style.display = '';
		}else {
			newRow = lastRow;
		}

		setNodeText(newRow.cells[0], fldName);
		setNodeText(newRow.cells[1], dispName);
		setNodeText(newRow.cells[2], fldInputTxt);

		setHiddenValue(id, "field_id", "_");
		setHiddenValue(id, "field_name", fldName);
		setHiddenValue(id, "display_name", dispName);
		setHiddenValue(id, "field_delete", "N");
		setHiddenValue(id, "field_input", fldInput);

		var deleteImg = newRow.cells[3].getElementsByTagName("img")[0];
		deleteImg.src = cpath + '/icons/delete.gif';

		newRow.className = "added";

		editImgDialog.cfg.setProperty("context", [document.getElementById("btnAddField"), "tr", "bl"], false);
	}else {
		// Edit field
		editFieldDetails(id);

		if (isFieldEdited(id)) {
			row.className = "edited";
		}
		editImgDialog.hide();
	}

	document.imgFieldsForm.field_name.value = "";
	document.imgFieldsForm.display_name.value = "";
	setSelectedIndex(document.imgFieldsForm.field_input, "");

	return id;
}

function editFieldDetails(id) {

	var fldName  = trim(document.imgFieldsForm.field_name.value);
	var dispName = trim(document.imgFieldsForm.display_name.value);
	var fldInput = document.imgFieldsForm.field_input.value;
	var fldInputTxt = document.imgFieldsForm.field_input.options[document.imgFieldsForm.field_input.selectedIndex].text;

	var row = getImageFieldRow(id);

	if (!checkDuplicateFields(id))
		return false;

	if (!validateFieldDetails())
		return false;

	setNodeText(row.cells[0], fldName);
	setNodeText(row.cells[1], dispName);
	setNodeText(row.cells[2], fldInputTxt);

	setHiddenValue(id, "field_name", fldName);
	setHiddenValue(id, "display_name", dispName);
	setHiddenValue(id, "field_input", fldInput);
}

function checkDuplicateFields(id) {
	var tabLen = getImageFieldTabRows();
	var newFldName = trim(document.imgFieldsForm.field_name.value);
	for (var i=0; i<tabLen; i++) {
		var fldName = getIndexedValue("field_name",i);
		if (!empty(fldName) && fldName == newFldName && i != id) {
			alert("Field: "+newFldName + " already exists.");
			document.imgFieldsForm.field_name.focus();
			return false;
		}
	}
	return true;
}

function isFieldEdited(id) {
	var fldName  = trim(document.imgFieldsForm.field_name.value);
	var dispName = trim(document.imgFieldsForm.display_name.value);
	var fldInput = document.imgFieldsForm.field_input.value;

	var origFldName		= getIndexedValue("field_name",id);
	var origDispName	= getIndexedValue("display_name",id);
	var origFldInput	= getIndexedValue("field_input",id);

	if (fldName == origFldName && dispName == origDispName && fldInput == origFldInput)
		return false;
	return true;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(pdfForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(pdfForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}