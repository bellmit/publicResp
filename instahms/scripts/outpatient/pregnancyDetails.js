function initPregnancyDetails() {
  initPregnancyDetailsDialog();
  initEditPregnancyDetailsDialog();
}

function pregnancyDetailsEntered() {
	return document.getElementById('pregnancyDetailsTable').rows.length > 2;
}

var addPregnancyDialog = null;
function initPregnancyDetailsDialog() {
	var dialogPregnancyDiv=document.getElementById("addPregnancyDialog");
	if(dialogPregnancyDiv == undefined) return;
	dialogPregnancyDiv.style.display = 'block';
	addPregnancyDialog = new YAHOO.widget.Dialog("addPregnancyDialog",
										{	width:"600px",
											context : ["addPregnancyDialog", "tr", "br"],
											visible:false,
											modal:true,
											constraintoviewport:true
										});

	YAHOO.util.Event.addListener('pregnancyDetails_add_btn', 'click', addPregnancyDetailsTable, addPregnancyDialog, true);
	YAHOO.util.Event.addListener('pregnancyDetails_cancel_btn', 'click', handleAddPregnancyCancel, addPregnancyDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddPregnancyCancel,
	                                                scope:addPregnancyDialog,
	                                                correctScope:true } );
	addPregnancyDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addPregnancyDialog.render();
}

function handleAddPregnancyCancel() {
		parentPregnancyDialog = null;
		this.cancel();
}

function checkPregnancies() {
	var els = document.getElementsByName('pregnancy_date');
	var pregnancy_delete = document.getElementsByName('pregnancy_deleted');
	var pregnancyCount = 0;
	var fieldG = document.getElementById("field_g").value;
	var fieldP = document.getElementById("field_p").value;
	var fieldL = document.getElementById("field_l").value;
	var fieldA = document.getElementById("field_a").value;
	for (var i=0; i<els.length; i++) {
		var pregnancy_date = els[i].value;
		if (pregnancy_date !='' && pregnancy_delete[i].value == 'false' || (fieldG !='' || fieldP !='' || fieldL !='' || fieldA !='')) {
			pregnancyCount++;
		}
	}
	if (pregnancyCount == 0) {
		showMessage("js.outpatient.consultation.mgmt.enteratleastonepregnancyhistoryvalue");
		return false;
	} 
	return true;
}


var parentPregnancyDialog = null;
function showAddPregnancyDialog(obj) {
	var row = getPregnancyDetailsThisRow(obj);
	clearPregnancyDetailsFields();
	addPregnancyDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addPregnancyDialog.show();
	document.getElementById('d_pregnancy_date').focus();
	parentPregnancyDialog = addPregnancyDialog;
	return false;
}

var pregnancyDetailsColIndex  = 0;
var pregnancyDetailsAdded = 0;
var PREGNANCY_DATETIME = pregnancyDetailsColIndex++, PREGNANCY_WEEK = pregnancyDetailsColIndex++, PREGNANCY_PLACE =  pregnancyDetailsColIndex++, PREGNANCY_METHOD = pregnancyDetailsColIndex++, 
	PREGNANCY_WEIGHT = pregnancyDetailsColIndex++, PREGNANCY_SEX = pregnancyDetailsColIndex++, PREGNANCY_COMPLICATIONS = pregnancyDetailsColIndex++, PREGNANCY_FEEDING = pregnancyDetailsColIndex++,
	PREGNANCY_OUTCOME = pregnancyDetailsColIndex++;PREGNANCY_TRASH_COL = pregnancyDetailsColIndex++, PREGNANCY_EDIT_COL = pregnancyDetailsColIndex++;
function addPregnancyDetailsTable() {
	if(!checkWeightandWeekvalidation('add')) return false;
	if(!addtoPregnancySaverecords('add')) return false;
	var week = document.getElementById('d_pregnancy_week').value;
	
   	var dateField = document.getElementById('d_pregnancy_date').value;
	if (dateField == '') {
   		alert('Enter Pregnancy Date');
   		document.getElementById('d_pregnancy_date').focus();
   		return false;
   	}
   
   	var id = getPregnancyDetailsNumCharges('pregnancyDetailsTable');
   	var table = document.getElementById("pregnancyDetailsTable");
	var templateRow = table.rows[getPregnancyDetailsTemplateRow('pregnancyDetailsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
    var pregnancy_history_id = document.getElementById('d_pregnancy_history_id').value;
    var place = document.getElementById('d_pregnancy_place').value;
   	var method = document.getElementById('d_pregnancy_method').value;
   	var weight = document.getElementById('d_pregnancy_weight').value;
   	var sex = document.getElementById('d_pregnancy_sex').options[document.getElementById('d_pregnancy_sex').selectedIndex].value;
   	var complications = document.getElementById('d_pregnancy_complication').value;
   	var feeding = document.getElementById('d_pregnancy_feeding').value;
   	var outcome = document.getElementById('d_pregnancy_outcome').value;
   	
   	setNodeText(row.cells[PREGNANCY_DATETIME], dateField);
	setNodeText(row.cells[PREGNANCY_WEEK], week);
	setNodeText(row.cells[PREGNANCY_PLACE], place,20);
	setNodeText(row.cells[PREGNANCY_METHOD], method,20);
	setNodeText(row.cells[PREGNANCY_WEIGHT], weight);
	setNodeText(row.cells[PREGNANCY_SEX], sex == 'M' ? 'Male' : sex == 'F' ? 'Female' :  sex == 'O' ? 'Unknown' : '');
	setNodeText(row.cells[PREGNANCY_COMPLICATIONS], complications,20);
	setNodeText(row.cells[PREGNANCY_FEEDING], feeding,20);
	setNodeText(row.cells[PREGNANCY_OUTCOME], outcome,20);

	setPregnancyDetailsHiddenValue(id, "pregnancy_history_id", '_');
	setPregnancyDetailsHiddenValue(id, "pregnancy_date", dateField);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_weeks", week);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_place", place);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_method", method);
	setPregnancyDetailsHiddenValue(id, "pregnancy_weight", weight);
	setPregnancyDetailsHiddenValue(id, "pregnancy_sex", sex);
	setPregnancyDetailsHiddenValue(id, "pregnancy_complications", complications);
	setPregnancyDetailsHiddenValue(id, "pregnancy_feeding", feeding);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_outcome", outcome);

  	pregnancyDetailsAdded++;
	clearPregnancyDetailsFields();
	setPregnancyDetailsRowStyle(id);
	addPregnancyDialog.align("tr", "tl");
	document.getElementById('d_pregnancy_date').value=formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_pregnancy_date').focus();
	return id;
}

function setPregnancyDetailsHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[pregnancy_details_form], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


function addtoPregnancySaverecords(action) {
	
	var prefix = null;
	if (action == 'add'){
		prefix = 'd';
	} else if (action == 'edit'){
		prefix = 'ed';
	}
	
	var week = document.getElementById(prefix+'_pregnancy_week').value;
	var place = document.getElementById(prefix+'_pregnancy_place').value;
	var method = document.getElementById(prefix+'_pregnancy_method').value;
	var weight = document.getElementById(prefix+'_pregnancy_weight').value;
	var sex = document.getElementById(prefix+'_pregnancy_sex').options[document.getElementById(prefix+'_pregnancy_sex').selectedIndex].value;
	var complications = document.getElementById(prefix+'_pregnancy_complication').value;
	var feeding = document.getElementById(prefix+'_pregnancy_feeding').value;
	var outcome = document.getElementById(prefix+'_pregnancy_outcome').value;
	
	if ((week =='' && place =='' &&
   		    method =='' && weight =='' &&
   		   sex =='' && complications =='' &&
   		   feeding =='' && outcome =='') ) {
   		  alert("Atleast add one field to grid");
   		   return false;
   	    }
	
	return true;
}


function clearPregnancyDetailsFields() {
	document.getElementById('d_pregnancy_date').value = formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_pregnancy_week').value = '';
	document.getElementById('d_pregnancy_place').value = '';
	document.getElementById('d_pregnancy_method').value = '';
	document.getElementById('d_pregnancy_weight').value = '';
	document.getElementById('d_pregnancy_sex').value = '';
	document.getElementById('d_pregnancy_complication').value = '';
	document.getElementById('d_pregnancy_feeding').value = '';
	document.getElementById('d_pregnancy_outcome').value = '';
	
}

function setPregnancyDetailsRowStyle(i) {
	var row = getPregnancyDetailsChargeRow(i, 'pregnancyDetailsTable');
	var pregnancyhistoryId = getPregnancyDetailsIndexedValue("pregnancy_history_id", i);

	var trashImgs = row.cells[PREGNANCY_TRASH_COL].getElementsByTagName("img");
	var flagImgs = row.cells[PREGNANCY_DATETIME].getElementsByTagName("img");
	var added = (pregnancyhistoryId.substring(0,1) == "_");
	var cancelled = getPregnancyDetailsIndexedValue("pregnancy_deleted", i) == 'true';
	var edited = getPregnancyDetailsIndexedValue("pregnancy_edited", i) == 'true';

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
	
	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}


	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;
	
	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelPregnancyDetails(obj) {

	var row = getPregnancyDetailsThisRow(obj);
	var id = getPregnancyDetailsRowChargeIndex(row);
	var oldDeleted =  getPregnancyDetailsIndexedValue("pregnancy_deleted", id);

	var isNew = getPregnancyDetailsIndexedValue("pregnancy_history_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		pregnancyDetailsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setPregnancyDetailsIndexedValue("pregnancy_deleted", id, newDeleted);
		setPregnancyDetailsIndexedValue("pregnancy_edited", id, "true");
		setPregnancyDetailsRowStyle(id);
	}
	return false;
}

var editPregnancyDialog=null;
function initEditPregnancyDetailsDialog() {
	var dialogPregnancyDiv = document.getElementById("editPregnancyDialog");
	dialogPregnancyDiv.style.display = 'block';
	editPregnancyDialog = new YAHOO.widget.Dialog("editPregnancyDialog",{
			width:"600px",
			text: "Edit Pregnancy Details",
			context :["pregnancyDetailsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditPregnancyCancel,
	                                                scope:editPregnancyDialog,
	                                                correctScope:true } );
	editPregnancyDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editPregnancyDialog.cancelEvent.subscribe(handleEditPregnancyCancel);
	YAHOO.util.Event.addListener('edit_PregnancyDetails_Ok', 'click', editPregnancyTableRow, editPregnancyDialog, true);
	YAHOO.util.Event.addListener('edit_PregnancyDetails_Cancel', 'click', handleEditPregnancyCancel, editPregnancyDialog, true);
	YAHOO.util.Event.addListener('edit_PregnancyDetails_Previous', 'click', openPregnancyDetailsPrevious, editPregnancyDialog, true);
	YAHOO.util.Event.addListener('edit_PregnancyDetails_Next', 'click', openPregnancyDetailsNext, editPregnancyDialog, true);
	editPregnancyDialog.render();
}

function handleEditPregnancyCancel() {
		var id = document.forms[pregnancy_details_form].editPregnancyRowId.value;
		var row = getPregnancyDetailsChargeRow(id, "pregnancyDetailsTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldPregnancyEdited = false;
		this.hide();
}

function editPregnancyTableRow() {
	if(!checkWeightandWeekvalidation('edit')) return false;
	if(!addtoPregnancySaverecords('edit')) return false;
	var id = document.forms[pregnancy_details_form].editPregnancyRowId.value;
	var row = getPregnancyDetailsChargeRow(id, 'pregnancyDetailsTable');
	var week = document.getElementById('ed_pregnancy_week').value;
	
   	var dateField = document.getElementById('ed_pregnancy_date').value;
	if (dateField == '') {
   		alert('Enter Pregnancy Date');
   		document.getElementById('ed_pregnancy_date').focus();
   		return false;
   	}
   	var pregnancy_history_id = document.getElementById('ed_pregnancy_history_id').value;
    var place = document.getElementById('ed_pregnancy_place').value;
   	var method = document.getElementById('ed_pregnancy_method').value;
   	var date = document.getElementById('ed_pregnancy_date').value ;
   	var weight = document.getElementById('ed_pregnancy_weight').value;
   	var sex = document.getElementById('ed_pregnancy_sex').options[document.getElementById('ed_pregnancy_sex').selectedIndex].value;
   	var complications = document.getElementById('ed_pregnancy_complication').value;
   	var feeding = document.getElementById('ed_pregnancy_feeding').value;
   	var outcome = document.getElementById('ed_pregnancy_outcome').value;

	setNodeText(row.cells[PREGNANCY_DATETIME], dateField);
	setNodeText(row.cells[PREGNANCY_WEEK], week);
	setNodeText(row.cells[PREGNANCY_PLACE], place,30);
	setNodeText(row.cells[PREGNANCY_METHOD], method,30);
	setNodeText(row.cells[PREGNANCY_WEIGHT], weight);
	setNodeText(row.cells[PREGNANCY_SEX], sex == 'M' ? 'Male' : sex == 'F' ? 'Female' :  sex == 'O' ? 'Unknown' : '');
	setNodeText(row.cells[PREGNANCY_COMPLICATIONS], complications,30);
	setNodeText(row.cells[PREGNANCY_FEEDING], feeding,30);
	setNodeText(row.cells[PREGNANCY_OUTCOME], outcome),30;

	setPregnancyDetailsHiddenValue(id, "pregnancy_history_id", pregnancy_history_id);
	setPregnancyDetailsHiddenValue(id, "pregnancy_date", dateField);
	setPregnancyDetailsHiddenValue(id, "pregnancy_weeks", week);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_place", place);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_method", method);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_weight", weight);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_sex", sex);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_complications", complications);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_feeding", feeding);
   	setPregnancyDetailsHiddenValue(id, "pregnancy_outcome", outcome);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setPregnancyDetailsIndexedValue("pregnancy_edited", id, 'true');
	setPregnancyDetailsRowStyle(id);

	editPregnancyDialog.cancel();
	return true;
}



var fieldPregnancyEdited = false;
function setPregnancyDetailsEdited() {
	fieldPregnancyEdited = true;
}


function showEditPregnancyDialog(obj) {
	parentPregnancyDialog = editPregnancyDialog;
	var row = getPregnancyDetailsThisRow(obj);
	var id = getPregnancyDetailsRowChargeIndex(row);
	editPregnancyDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editPregnancyDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.forms[pregnancy_details_form].editPregnancyRowId.value = id;

	var pregnancy_datetime = getPregnancyDetailsIndexedValue("pregnancy_date", id);
	document.getElementById('ed_pregnancy_date').value = empty(pregnancy_datetime) ? '' : pregnancy_datetime.split(' ')[0];
	document.getElementById('ed_pregnancy_history_id').value = getPregnancyDetailsIndexedValue("pregnancy_history_id", id);
	document.getElementById('ed_pregnancy_week').value = getPregnancyDetailsIndexedValue("pregnancy_weeks", id);
	document.getElementById('ed_pregnancy_place').value = getPregnancyDetailsIndexedValue("pregnancy_place", id);
	document.getElementById('ed_pregnancy_method').value = getPregnancyDetailsIndexedValue("pregnancy_method", id);
	document.getElementById('ed_pregnancy_sex').value = getPregnancyDetailsIndexedValue("pregnancy_sex", id);
	document.getElementById('ed_pregnancy_weight').value = getPregnancyDetailsIndexedValue("pregnancy_weight", id);
	document.getElementById('ed_pregnancy_complication').value = getPregnancyDetailsIndexedValue("pregnancy_complications", id);
	document.getElementById('ed_pregnancy_feeding').value = getPregnancyDetailsIndexedValue("pregnancy_feeding", id);
	document.getElementById('ed_pregnancy_outcome').value = getPregnancyDetailsIndexedValue("pregnancy_outcome", id);
	
	document.getElementById('ed_pregnancy_date').focus();
	return false;
}

var weightfield = null;
var weekField = null;
function checkWeightandWeekvalidation(action) {
	var prefix = null;
	if (action == 'add'){
		prefix = 'd';
	} else if (action == 'edit'){
		prefix = 'ed';
	}
	
	weekField = document.getElementById(prefix+"_pregnancy_week").value;
	if (weekField != '') {
		if (!(isInteger(weekField))) {
		alert("Week Field should be numeric field");
			return false;
		} else if((weekField < 1 || weekField > 50)) {
			alert("Week Field should be between 1 to 50");
			return false;
		}
	}
		
	weightfield = document.getElementById(prefix+"_pregnancy_weight").value;
	if(weightfield != '') {
		if(!isDecimal(weightfield,2)) {
			alert("Invalid Weight Value	");
			return false;
		}
		if(!checkMaximumWeight(weightfield)) return false;
	}
    return true;
}

function checkMaximumWeight(weightfield) {
	var val = parseFloat(weightfield);
	if (val > 10) {
		alert("Weight Should be between 0 and 10");
		return false;
	}
	return true;
}


function validateObstetricDetails() {
	var fieldG = document.getElementById("field_g").value;
	var fieldP = document.getElementById("field_p").value;
	var fieldL = document.getElementById("field_l").value;
	var fieldA = document.getElementById("field_a").value;
	
	if (fieldG != '') {
		if (!(isInteger(fieldG))) {
			alert("Obstetric Field G should be numeric field");
				return false;
		}
	}
	if (fieldP != '') {
		if (!(isInteger(fieldP))) {
			alert("Obstetric Field P should be numeric field");
				return false;
		}
	}
	if (fieldL != '') {
		if (!(isInteger(fieldL))) {
			alert("Obstetric Field L should be numeric field");
				return false;
		}
	}
	if (fieldA != '') {
		if (!(isInteger(fieldA))) {
			alert("Obstetric Field A should be numeric field");
				return false;
		}
	}
	return true;
}

function openPregnancyDetailsPrevious() {
	var id = document.forms[pregnancy_details_form].editPregnancyRowId.value;
	id = parseInt(id);
	var row = getPregnancyDetailsChargeRow(id, 'pregnancyDetailsTable');

	if (fieldPregnancyEdited) {
		if (!editPregnancyTableRow()) return false;
		fieldPregnancyEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditPregnancyDialog(document.getElementsByName('pregnancyEditAnchor')[parseInt(id)-1]);
	}
}

function openPregnancyDetailsNext() {
	var id = document.forms[pregnancy_details_form].editPregnancyRowId.value;
	id = parseInt(id);
	var row = getPregnancyDetailsChargeRow(id, 'pregnancyDetailsTable');

	if (fieldPregnancyEdited) {
		if (!editPregnancyTableRow()) return false;
		fieldPregnancyEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('pregnancyDetailsTable').rows.length-2) {
		showEditPregnancyDialog(document.getElementsByName('pregnancyEditAnchor')[parseInt(id)+1]);
	}
}

function getPregnancyDetailsNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstPregnancyDetailsRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getPregnancyDetailsNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getPregnancyDetailsMainRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getPregnancyDetailsTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getPregnancyDetailsNumCharges(tableId) + 1;
}

function getPregnancyDetailsChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstPregnancyDetailsRow()];
}

function getPregnancyDetailsRowChargeIndex(row) {
	return row.rowIndex - getFirstPregnancyDetailsRow();
}

function getPregnancyDetailsThisRow(node) {
	return findAncestor(node, "TR");
}

function getPregnancyDetailsIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[pregnancy_details_form], name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setPregnancyDetailsIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[pregnancy_details_form], name, index);
	if (obj)
		obj.value = value;
	return obj;
}