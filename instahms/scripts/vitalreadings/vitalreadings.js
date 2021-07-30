Insta.HeaderDetail = {};
Insta.HeaderDetail.Utilities = {
	setHiddenValue: function(index, name, value) {
		var el = getIndexedFormElement(vitalHeaderDetailsForm, name, index);
		if (el) {
			if (value == null || value == undefined)
				value = "";
			el.value = value;
		}
	},

	getNumCharges: function(tableId) {
		// header, add row, hidden template row: totally 2 extra
		return document.getElementById(tableId).rows.length-2;
	},

	getFirstItemRow: function() {
		// index of the first charge item: 0 is header, 1 is first charge item.
		return 1;
	},

	getTemplateRow: function(tableId) {
		// gets the hidden template row index: this follows header row + num charges.
		return this.getNumCharges(tableId) + 1;
	},

	getChargeRow: function(i, tableId) {
		i = parseInt(i);
		var table = document.getElementById(tableId);
		return table.rows[i + this.getFirstItemRow()];
	},

	getRowChargeIndex: function(row) {
		return row.rowIndex - this.getFirstItemRow();
	},

	getThisRow: function(node) {
		return findAncestor(node, "TR");
	},

	getIndexedPaise: function(name, index) {
		return getElementPaise(getIndexedFormElement(vitalHeaderDetailsForm, name, index));
	},

	setIndexedValue: function(name, index, value) {
		var obj = getIndexedFormElement(vitalHeaderDetailsForm, name, index);
		if (obj)
			obj.value = value;
		return obj;
	},

	getIndexedValue: function(name, index) {
		var obj = getIndexedFormElement(vitalHeaderDetailsForm, name, index);
		if (obj)
			return obj.value;
		else
		return null;
	},

	validateDateTime: function(vitalDate, vitalTime) {

		var valid = true;
		if (document.getElementById(vitalDate)!= null) {
			valid = valid && validateRequired(document.getElementById(vitalDate), getString("js.outpatient.vitalform.enter.selectdate"));
			valid = valid && validateRequired(document.getElementById(vitalTime), getString("js.outpatient.vitalform.entertime"));
			valid = valid && doValidateDateField(document.getElementById(vitalDate), 'past');
			valid = valid && validateTime(document.getElementById(vitalTime));
		}
		return valid;
	}

}
var utility = Insta.HeaderDetail.Utilities;
YAHOO.util.Event.onContentReady("content", initVitalDialogs);
var vitalHeaderDetailsForm = null;
var VITAL_TRASH_COL = 0;
var vitalColIndex = 0;
var READING_DATE=vitalColIndex++, READING_TIME=vitalColIndex++;
function initVitalDialogs() {
	var vitalAddDialogDiv = document.getElementById("addVitalDialog");
	if (vitalAddDialogDiv) {
		initAddVitalReadingDialog();
		initEditVitalReadingDialog();
		// retrieve the form name using element
		vitalHeaderDetailsForm = document.getElementsByName("h_vital_reading_id")[0].form;
		VITAL_TRASH_COL= vitalColIndex + document.getElementsByName("add_param_value").length;
	}
}

var addVitalDialog = null;
var editVitalDialog = null;
var vitalsAdded = 0;
function initAddVitalReadingDialog() {
	var dialogDiv = document.getElementById("addVitalDialog");
	dialogDiv.style.display = 'block';
	addVitalDialog = new YAHOO.widget.Dialog("addVitalDialog",
			{	width:"470px",
				context : ["addVitalDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('vital_add_Ok', 'click', addToVitalTable, addVitalDialog, true);
	YAHOO.util.Event.addListener('vital_add_Close', 'click', handleAddVitalCancel, addVitalDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddVitalCancel,
	                                                scope:addVitalDialog,
	                                                correctScope:true } );
	addVitalDialog.cfg.queueProperty("keylisteners", escKeyListener);
	addVitalDialog.render();
}

function initEditVitalReadingDialog() {
	var dialogDiv = document.getElementById("editVitalDialog");

	dialogDiv.style.display = 'block';
	editVitalDialog = new YAHOO.widget.Dialog("editVitalDialog",
			{	width:"470px",
				context : ["editVitalDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('vital_edit_Ok', 'click', editVitalTableRow, editVitalDialog, true);
	YAHOO.util.Event.addListener('vital_edit_Cancel', 'click', handleEditVitalCancel, editVitalDialog, true);
	YAHOO.util.Event.addListener('vital_edit_Previous', 'click', openPreviousVital, editVitalDialog, true);
	YAHOO.util.Event.addListener('vital_edit_Next', 'click', openNextVital, editVitalDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditVitalCancel,
	                                                scope:editVitalDialog,
	                                                correctScope:true } );
	editVitalDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editVitalDialog.render();
}

function showAddVitalDialog(obj) {
	var row = getThisRow(obj);
	
	var flag = false;
	if (!gVitalReadingsExists && !empty(latest_vital_reading_json)) {
		fillDialog();
		flag = true;
	}
	if (!flag) {
		fillVitalParams();
		flag = true;
	}
	addVitalDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addVitalDialog.show();
	return false;
}

function fillDialog() {
	var txtEls = document.getElementsByName('add_param_value');
	var hIdEls = document.getElementsByName('add_param_id');
	var hParamLabelEls = document.getElementsByName('add_param_label');
	var hMandatoryInTxn = document.getElementsByName('add_mandatory_in_tx');
	for (var i=0; i<latest_vital_reading_json.length; i++) {
		var reading = latest_vital_reading_json[i];
		for (var j=0; j<hIdEls.length; j++) {
			if (parseInt(hIdEls[j].value) == parseInt(reading.param_id)) {
				txtEls[j].value = reading.param_value;
				txtEls[j].onchange();
				
				hParamLabelEls[j].value = reading.param_label;
				hMandatoryInTxn[j].value = reading.mandatory_in_tx;			
			}			
		}
	}
}

function fillVitalParams() {
	var hIdEls = document.getElementsByName('add_param_id');
	var txtEls = document.getElementsByName('add_param_value');
	var readingIdEls = document.getElementsByName('h_vital_reading_id');
	if (readingIdEls.length == 1) {
		for (var j=0; j<height_weight_params.length; j++) {
			for (var i=0; i<hIdEls.length; i++) {
				if (parseInt(hIdEls[i].value) == parseInt(height_weight_params[j].paramid)) {
					txtEls[i].value = height_weight_params[j].paramvalue;
					txtEls[i].onchange();
				}
			}
		}
	} else {
		var index = readingIdEls.length-2;
		var h_param_el = document.getElementsByName('h_param_value'+index);
		var h_param_id = document.getElementsByName('h_param_id'+index);
		for (var j=0; j<h_param_id.length; j++) {
			for (var i=0; i<hIdEls.length; i++) {
				var idVal = parseInt(hIdEls[i].value);
				if ((idVal == 5 || idVal == 6) && idVal == parseInt(h_param_id[j].value)) {
					txtEls[i].value = h_param_el[j].value;
					txtEls[i].onchange();
				}
			}
		}
	}
}

function addToVitalTable() {
	var paramValuesEls = document.getElementsByName("add_param_value");
	var mandatoryFieldEls = document.getElementsByName("add_mandatory_in_tx");
	var paramLabelEls = document.getElementsByName("add_param_label");
	var paramRemarksEls = document.getElementsByName("add_param_remarks");
	var valueAdded = false;
	for (i=0; i<paramValuesEls.length; i++) {
		if (paramValuesEls[i].value != "") {
			valueAdded = true;
			break;
		}
	}
	if (!valueAdded) {
		showMessage("js.outpatient.vitalform.enteratleastonevalue");
		paramValuesEls[0].focus();
		return false;
	}
	if (!utility.validateDateTime('add_reading_date', 'add_reading_time')) {
		return false;
	}

	var id = utility.getNumCharges('vitalsTable');
   	var table = document.getElementById("vitalsTable");
	var templateRow = table.rows[utility.getTemplateRow('vitalsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	var paramIdsEls = document.getElementsByName("add_param_id");
   	var paramRemarksEls = document.getElementsByName("add_param_remarks");
	var readingDate = document.getElementById('add_reading_date').value;
	var readingTime = document.getElementById('add_reading_time').value;

	setNodeText(row.cells[READING_DATE], readingDate);
	setNodeText(row.cells[READING_TIME], readingTime);
	for (var i=0; i<paramValuesEls.length; i++) {
		var startFrom = vitalColIndex;
		var index = i+(startFrom++);
		if(paramType == 'I/O' && paramRemarksEls[i].value !=''){
			var paramValue = paramValuesEls[i].value+",("+paramRemarksEls[i].value+")";
			if(paramRemarksEls[i].value.length >= 20){
				paramValue =paramValuesEls[i].value+",("+paramRemarksEls[i].value.substring(0,20) + "...)";
			}
			setNodeText(row.cells[index], paramValue);
		} else {
			setNodeText(row.cells[index], paramValuesEls[i].value);
		}
		if (paramType == 'V') {
			var fontColor = hexc(paramValuesEls[i].style.getPropertyValue("background-color")) == normalResult ? 'grey' : paramValuesEls[i].style.getPropertyValue("background-color");
			row.cells[index].setAttribute('style', 'color:'+fontColor+';font-weight: '+(empty(fontColor) || fontColor == 'grey' ? '' : 'bold'));
		}
		row.appendChild(makeHidden("h_param_id"+id, null, paramIdsEls[i].value));
		row.appendChild(makeHidden("h_param_value"+id, null, paramValuesEls[i].value));
		row.appendChild(makeHidden("h_mandatory_in_tx"+id, null, mandatoryFieldEls[i].value));
		row.appendChild(makeHidden("h_param_label"+id, null, paramLabelEls[i].value));
		row.appendChild(makeHidden("colorCode"+id, paramIdsEls[i].value+""+id+"colorCode", paramValuesEls[i].style.getPropertyValue("background-color")));
		row.appendChild(makeHidden("h_param_remarks"+id, null, paramRemarksEls[i].value));
	}

	utility.setHiddenValue(id, "h_vital_reading_id", "_");
	utility.setHiddenValue(id, "h_reading_date", document.getElementById('add_reading_date').value);
	utility.setHiddenValue(id, "h_reading_time", document.getElementById('add_reading_time').value);
	
	//row.className = "added";
	vitalsAdded++;
	clearVitalFields();

	this.align("tr", "tl");
	document.getElementsByName('add_param_value')[0].focus();
	return id;
}

function hexc(colorval) {
    var parts = colorval.match(/^rgb\((\d+),\s*(\d+),\s*(\d+)\)$/);
    var color="";
    if (parts == null)
    	return;

	    delete(parts[0]);
	    for (var i = 1; i <= 3; ++i) {
	        parts[i] = parseInt(parts[i]).toString(16).toUpperCase();
	        if (parts[i].length == 1) parts[i] = '0' + parts[i];
	    }
	    color = '#' + parts.join('');
	    return color;
}

function handleAddVitalCancel() {
	this.cancel();
}

function showEditVitalDialog(obj) {
	var row = utility.getThisRow(obj);
	var id = utility.getRowChargeIndex(row);
	editVitalDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editVitalDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	vitalHeaderDetailsForm.vitalEditRowId.value = id;
	var paramValuesEls = document.getElementsByName('h_param_value'+id);
	document.getElementById('edit_reading_date').value = utility.getIndexedValue("h_reading_date", id);
	document.getElementById('edit_reading_time').value = utility.getIndexedValue("h_reading_time", id);
	document.getElementById('hidden_edit_reading_date').value = utility.getIndexedValue("h_reading_date", id);
	document.getElementById('ed_vital_entered_by_label').textContent = utility.getIndexedValue("h_userName", id);
	var paramValuesIds = document.getElementsByName('h_param_id'+id);
	var paramRemarksEls = document.getElementsByName('h_param_remarks'+id);
	for (var i=0; i<paramValuesEls.length; i++) {
		document.getElementsByName('edit_param_value')[i].value = paramValuesEls[i].value;
		document.getElementsByName('edit_param_remarks')[i].value = paramRemarksEls[i].value;
		
		var backgroundColor = '';
		if (paramType == 'V') {
			backgroundColor = "background-color:"+document.getElementById(paramValuesIds[i].value+""+id+'colorCode').value;
		}
		document.getElementsByName('edit_param_value')[i].setAttribute("style","width: 100px;" +backgroundColor);
	}

	return false;
}

function setValueModified(obj) {
	var row = getThisRow(obj);
	getElementByName(row, 'edit_param_value_modified').value = 'Y';
	getElementByName(row, 'edit_param_remarks').value = '';
}

function handleEditVitalCancel() {
	var id = vitalHeaderDetailsForm.vitalEditRowId.value;
	var row = utility.getChargeRow(id, "vitalsTable");
	YAHOO.util.Dom.removeClass(row, 'editing');
	vitalFieldEdited = false;
	this.cancel();
}

function editVitalTableRow(id) {
	var paramValuesEls = document.getElementsByName("edit_param_value");
	var paramRemarksEls = document.getElementsByName("edit_param_remarks");
	if (!utility.validateDateTime('edit_reading_date', 'edit_reading_time')) {
		return false;
	}
	var valueAdded = false;
	for (i=0; i<paramValuesEls.length; i++) {
		if (paramValuesEls[i].value != "") {
			valueAdded = true;
			break;
		}
	}
	
	if (!valueAdded) {
		showMessage("js.outpatient.vitalform.enteratleastonevalue");
		paramValuesEls[0].focus();
		return false;
	}
	var id = vitalHeaderDetailsForm.vitalEditRowId.value;
	var row = utility.getChargeRow(id, 'vitalsTable');
	
	// if the value is edited and remarks not entered alert the user to enter remarks for the param.
	var remarksEls = document.getElementsByName('edit_param_remarks');
	var valueEditedEls = document.getElementsByName('edit_param_value_modified');
	var isNew = utility.getIndexedValue("h_vital_reading_id", id) == '_';

	if (!isNew) {
		var labelAr = new Array();
		var firstEl = null;
		// instead of alerting one after another, list out all parameters edited and not entered remarks and alert all at once.
		for (var i=0; i<valueEditedEls.length; i++) {
			if (valueEditedEls[i].value == 'Y' && empty(remarksEls[i].value)) {
				labelAr.push(document.getElementsByName('edit_param_label')[i].value);
				
				if (firstEl == null)
					firstEl = remarksEls[i];
			}
		}
		if (labelAr.length > 0) {
			alert(getString("js.outpatient.vitalform.enter.remarks.for.following.params")+"\n * "+labelAr.join("\n * "));
			firstEl.focus(); // focus on the first edited param remarks element.
			return false;
		}
	}

	var paramIdsEls = document.getElementsByName("edit_param_id");
	var readingDate = document.getElementById('edit_reading_date').value;
	var readingTime = document.getElementById('edit_reading_time').value;

	setNodeText(row.cells[READING_DATE], readingDate);
	setNodeText(row.cells[READING_TIME], readingTime);
	for (var i=0; i<paramValuesEls.length; i++) {
		var startFrom = vitalColIndex;
		var index = i+(startFrom++);
		if(paramType == 'I/O' && paramRemarksEls[i].value !=''){
			var paramValue = paramValuesEls[i].value+",("+paramRemarksEls[i].value+")";
			if(paramRemarksEls[i].value.length >= 20){
				paramValue =paramValuesEls[i].value+",("+paramRemarksEls[i].value.substring(0,20) + "...)";
			}
			setNodeText(row.cells[index], paramValue);
		} else {
			setNodeText(row.cells[index], paramValuesEls[i].value);
		}
		if (paramType == 'V') {
			var fontColor = (hexc(paramValuesEls[i].style.getPropertyValue("background-color")) == normalResult ? 'grey' : paramValuesEls[i].style.getPropertyValue("background-color"));
			if (row.cells[index].getElementsByTagName('label')[0] != undefined) {
				row.cells[index].getElementsByTagName('label')[0].setAttribute('style', 'color:'+fontColor+';font-weight: '+(fontColor == 'grey' ? '' : 'bold'));
			} else {
				row.cells[index].setAttribute('style', 'color:'+fontColor+';font-weight: '+(fontColor == 'grey' ? '' : 'bold'));
			}
		}
		utility.setHiddenValue(i, "h_param_id"+id, paramIdsEls[i].value);
		utility.setHiddenValue(i, "h_param_value"+id, paramValuesEls[i].value);
		utility.setHiddenValue(i, "h_param_remarks"+id, remarksEls[i].value);
		utility.setHiddenValue(i, "colorCode"+id, paramValuesEls[i].style.getPropertyValue("background-color"));
	}

	utility.setHiddenValue(id, "h_reading_date", document.getElementById('edit_reading_date').value);
	utility.setHiddenValue(id, "h_reading_time", document.getElementById('edit_reading_time').value);


	YAHOO.util.Dom.removeClass(row, 'editing');

	utility.setIndexedValue("vital_edited", id, 'true');
	setVitalRowStyle(id);
	editVitalDialog.cancel();

	vitalFieldEdited = false; // after updating the row details make this as false.
	return id;
}

var vitalFieldEdited = false;
function setVitalEdited() {
	vitalFieldEdited = true;
}

function validateSystemVitals(obj, sysVital, label) {
	if (sysVital != 'Y')
		return;
	if (obj.value === '.') {
		alert(label+" "+getString("js.outpatient.vitalform.systemvital.mustbenumber"));
		obj.value='';
		setTimeout("document.getElementById('"+obj.id+"').focus()", 10);
		return false;
	}
	if(!validateDecimal(obj, label+" "+getString("js.outpatient.vitalform.systemvital.mustbenumber"), 2)) {
		obj.value='';
		setTimeout("document.getElementById('"+obj.id+"').focus()", 10);
		return false;
	}
	return true;
}

/*
* checks whether the vital date in edit dialog is edited or not.
* if edited sets the global flag vitalFieldEdited to true.
* this function is written since the function provided in extravalidation of datewidget is not being called when we select
* the date using the datewidget. (and also some versions of the mozilla(ex:9.0) are not supporting the two methods
* on any event(ex: onchange="method1();method2()" second method is not being called at all))
*/
function setVitalEditedIfDateChanged() {
	var hiddenEditedDate = document.getElementById('hidden_edit_reading_date').value;
	var editedDate = document.getElementById('edit_reading_date').value;
	if (hiddenEditedDate != editedDate)
		setVitalEdited();
}
function openPreviousVital(id, previous, next) {
	var id = vitalHeaderDetailsForm.vitalEditRowId.value;
	id = parseInt(id);
	var row = utility.getChargeRow(id, 'vitalsTable');
	setVitalEditedIfDateChanged();
	if (vitalFieldEdited) {
		if(!editVitalTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditVitalDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNextVital() {
	var id = vitalHeaderDetailsForm.vitalEditRowId.value;
	id = parseInt(id);
	var row = utility.getChargeRow(id, 'vitalsTable');
	setVitalEditedIfDateChanged();
	if (vitalFieldEdited) {
		if (!editVitalTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id+1 != document.getElementById('vitalsTable').rows.length-2) {
		showEditVitalDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
	}
}

function cancelVitalItem(obj) {
	var row = utility.getThisRow(obj);
	var id = utility.getRowChargeIndex(row);

	var oldDeleted =  utility.getIndexedValue("delVitalItem", id);
	var isNew = utility.getIndexedValue("h_vital_reading_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		vitalsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		utility.setIndexedValue("delVitalItem", id, newDeleted);
		utility.setIndexedValue("vital_edited", id, "true");
		setVitalRowStyle(id);
	}
	return false;
}

function setVitalRowStyle(i) {
	var row = utility.getChargeRow(i, 'vitalsTable');
	var readingId = utility.getIndexedValue("h_vital_reading_id", i);
	var flagImgs = row.cells[READING_DATE].getElementsByTagName("img");
	var trashImgs = row.cells[VITAL_TRASH_COL].getElementsByTagName("img");

	var added = (readingId.substring(0,1) == "_");
	var cancelled = utility.getIndexedValue("delVitalItem", i) == 'true';
	var edited = utility.getIndexedValue("vital_edited", i) == 'true';

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
		if (delete_vitals || added) {
			trashSrc = cpath + '/icons/delete.gif';
		} else {
			trashSrc = cpath + '/icons/delete_disabled.gif';
		}
	}

	row.className = cls;

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function clearVitalFields() {
	var els = document.getElementsByName("add_param_value");
	var paramIdEls = document.getElementsByName("add_param_id");
	var remarksEls = document.getElementsByName("add_param_remarks");
	for (var i=0; i<els.length; i++) {
		// do not clear the height and weight parameters.
		if (!(parseInt(paramIdEls[i].value) == 5 || parseInt(paramIdEls[i].value) == 6))
			els[i].value = '';
		
		// clear the remarks 
		remarksEls[i].value = '';
		
		if (paramType == 'V') {
			els[i].removeAttribute('style');
			els[i].setAttribute('style', 'width: 100px; background-color:'+ normalResult);
		}
	}
	document.getElementById('add_reading_date').value = formatDate(new Date(), 'ddmmyyyy', '-') ;
	document.getElementById('add_reading_time').value = formatTime(new Date());
}

function searchvitalreadings(visitid) {
  document.mainform.visitId.value = visitid;
  document.mainform.submit();
}

function setTime(time){
	if (time.value.length == 2) {
		time.value = time.value+":00";
	}
	if (time.value.length == 1) {
		time.value = "0"+time.value+":00";
	}
}

function setSiviarity(obj,mn_normal,mx_normal,mn_critical,mx_critical,mn_improbable,mx_improbable, map) {

	var id = obj.id;
	var textObj = document.getElementById(id);
	var parsedValue = parseFloat(obj.value);
	if (paramType != 'V') {
		return;
	}

	if (empty(obj.value)) {
		textObj.setAttribute("style","width: 100px; background-color:"+normalResult);
		return true;
	}

	if ((mx_improbable!= '') && parsedValue > parseFloat(mx_improbable)) {
		textObj.setAttribute("style","width: 100px; background-color:"+improbableResult);
	} else if ((mx_critical!= '') && parsedValue > parseFloat(mx_critical)) {

		textObj.setAttribute("style","width: 100px; background-color:"+criticalResult);
	} else if ((mx_normal!= '') && parsedValue > parseFloat(mx_normal)) {

		textObj.setAttribute("style","width: 100px; background-color:"+abnormalResult);
	} else if ((mn_improbable!= '') && parsedValue < parseFloat(mn_improbable)) {

		textObj.setAttribute("style","width: 100px; background-color:"+improbableResult);
	} else if ((mn_critical!= '') && parsedValue < parseFloat(mn_critical)) {

    	textObj.setAttribute("style","width: 100px; background-color:"+criticalResult);
	} else if ((mn_normal!= '') && parsedValue < parseFloat(mn_normal)) {

    	textObj.setAttribute("style","width: 100px; background-color:"+abnormalResult);

	} else if ((mn_normal!= '') || (mx_normal!= '')) {

    	textObj.setAttribute("style","width: 100px; background-color:"+normalResult);

	} else {
		textObj.setAttribute("style","width: 100px; background-color:"+normalResult);
    }
	return true;
}


function validateVitals() {
	var table = document.getElementById('vitalsTable');
	var markedForDelete = document.getElementsByName('delVitalItem');

	var hasResults = false;
	for (var i=0; i<markedForDelete.length-1; i++) {
		if (markedForDelete[i].value == 'false') {
			hasResults = true;
			break;
		}
	}
	if (!hasResults) {
		showMessage("js.outpatient.vitalform.entervitaldetails");
		return false;
	}

	var mandatory_fields = new Array();
	for (var i=1; i<table.rows.length-1; i++) {
		var value_found_in_mandatory = true;
		var mandatory_in_tx_els = document.getElementsByName('h_mandatory_in_tx'+(i-1));
		var param_value_els = document.getElementsByName('h_param_value'+(i-1));
		var param_label_els = document.getElementsByName('h_param_label'+(i-1));

		for (var j=0; j<param_value_els.length; j++) {
			if (mandatory_in_tx_els[j].value == 'Y') {
				if (i==1) // all the rows contain the same mandatory fields. hence first row mandatory fields are enough
					mandatory_fields.push(param_label_els[j].value);
				if (param_value_els[j].value != '')
					value_found_in_mandatory &= true;
				else
					value_found_in_mandatory &= false;
			}
		}
		//need to check all the rows
		if (mandatory_fields.length > 0 && !value_found_in_mandatory && markedForDelete[i-1].value == 'false')
			break;

	}
	if (mandatory_fields.length > 0 && !value_found_in_mandatory) {
		alert(getString('js.outpatient.vitalform.allreadingscontainvalue')+'* '+mandatory_fields.join("\n* "));
		return false;
	}
	return true;
}

