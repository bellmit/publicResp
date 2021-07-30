/**
 * 
 */

var j=0;
var DELIVERY_TIME_COL=j++; DELETE_COL=j++; EDIT_COL=j++; 

var gRowObj;

function initDialog() {
	
	for(var i=0;i<7;i++) {
		initReportsTimingsDialog(i);
	}
}

var gIndex;
var reportsTimingsDialog = new Array();

function initReportsTimingsDialog(index) {
	var reportsTimingsDialogDiv = document.getElementById("reportsTimingsDialog"+index);
	reportsTimingsDialogDiv.style.display = 'block';
	reportsTimingsDialog[index] = new YAHOO.widget.Dialog("reportsTimingsDialog"+index,{
			width:"450px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeReportsTimingsDialog,
	                                                scope:reportsTimingsDialog[index],
	                                                correctScope:true } );
	reportsTimingsDialog[index].cfg.queueProperty("keylisteners", escKeyListener);
	reportsTimingsDialog[index].render();
}

function closeReportsTimingsDialog() {
	clearDialog(gIndex);
	reportsTimingsDialog[gIndex].hide();
}

function openReportsTimingsDialog(index) {

	button = document.getElementById("btnAddItem"+index);
	reportsTimingsDialog[index].cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('reportsdialogheader'+index).innerHTML = 'Reports Delivery Details';
	document.getElementById('report_time'+index).disabled = false;
	reportsTimingsDialog[index].show();
	document.getElementById('report_time'+index).focus();
	gIndex = index;
	clearDialog(index);
	edited = false;
}

function cancelDialog(index) {
	clearDialog(index);
	reportsTimingsDialog[index].cancel();
}

function clearDialog(index){
	document.getElementById('report_time'+index).value = '';
	document.getElementById('dialogId'+index).value = '';
}

var edited = false;
function openEditReportsTimingsDialogBox(obj,iIndex) {
	var rowObj = findAncestor(obj,"TR");
	var table = document.getElementById('resultTable'+iIndex);
	gRowObj = table.rows[rowObj.rowIndex];
	var index = getRowItemIndex(rowObj);

	rowObj.className = 'selectedRow';
	document.getElementById('reportsdialogheader'+index).innerHTML ='Edit Reports Delivery Details';
	document.getElementById('dialogId'+iIndex).value = iIndex;
	updateGridToDialog(obj,rowObj,iIndex);
	document.getElementById('report_time'+index).focus();
	gIndex = iIndex;
	edited = false;
}

function updateGridToDialog(obj,rowObj,index) {
	document.getElementById('report_time'+index).value 	=  getElementByName(rowObj,"delivery_time").value;
	reportsTimingsDialog[index].cfg.setProperty("context", [obj, "tr", "br"], false);
	var id = getNumItems(index);
	reportsTimingsDialog[index].show();
}

function getNumItems(index) {
	// header, hidden template row: totally 3 extra
	return document.getElementById("resultTable"+index).rows.length-2;
}

function getFirstItemRow() {
	// index of the first Information item: 0 is header, 1 is first Information item.
	return 1;
}
//this method is for multiple tables with different ids.
function getTemplateRow(index) {
	// gets the hidden template row index: this follows header row + num Information.
	return getNumItems(index) + 1;
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getItemRow(index,rowObj) {
	i = parseInt(getRowItemIndex(rowObj));
	var table = document.getElementById("resultTable"+index);
	return table.rows[i + getFirstItemRow(index)];
}

function setHiddenValue(table,index, name, value) {
	var el = getIndexedTableElement(table, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function setReportsDetailsIndexedValue(table, name, index, value) {
	var obj = getIndexedTableElement(table, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function saveForm() {
	document.reportsAvailableForm.submit();
	return true;
}

function validateForm(index,id) {
	var table = document.getElementById('resultTable'+index);
	var deliveryTime = document.getElementById('report_time'+index).value;
	var flag = true;
	var d_fromTimeArr = deliveryTime.split(":");
	var deliveryReportTime = new Date();
	deliveryReportTime.setHours(parseInt(d_fromTimeArr[0],10));
	deliveryReportTime.setMinutes(parseInt(d_fromTimeArr[1],10));
	deliveryReportTime.setSeconds(0);
	deliveryReportTime.setMilliseconds(0);
	var dStartTimeInLong = deliveryReportTime.getTime();

	if(deliveryTime==''){
		alert("Report Delivery Time is required");
		 document.getElementById('report_time'+index).focus();
		return false;
	}
	var rowObj;
	var fromTime;
	var fromDateAndTime;
	var fromTimeArr;
	var dFromDateAndTimeInlong;

	for (var i=0;i<id;i++) {
	 	rowObj = table.rows[i+1];
	 	fromTime = getElementByName(rowObj,"delivery_time").value;
	 	fromDateAndTime = new Date();
	 	fromTimeArr = fromTime.split(":");
		fromDateAndTime.setHours(parseInt(fromTimeArr[0],10));
		fromDateAndTime.setMinutes(parseInt(fromTimeArr[1],10));
		fromDateAndTime.setSeconds(0);
		fromDateAndTime.setMilliseconds(0);
		dFromDateAndTimeInlong = fromDateAndTime.getTime();
		
		
		if (deliveryTime == fromTime  && gRowObj != rowObj) {
			alert("Duplicate Entry");
			document.getElementById('report_time'+index).focus();
			return false;
		}
	}
	return flag;
}
function addRecord(index) {
	var table = document.getElementById("resultTable"+index);
	var dialogId = document.getElementById('dialogId'+index).value;
	var defaultRow = document.getElementById('defaultRow'+index);
	var timeField = document.getElementById('report_time'+index);
	var defaultField = document.getElementById('default_value'+index);
	if (!empty(defaultRow)) {
		defaultRow.parentNode.removeChild(defaultRow);
	}
	
	var id = getNumItems(index);
	if (!empty(dialogId)) {
		var rowObj = getItemRow(index,gRowObj);
		if(!validateForm(index,id))
			return false;
		if(!validateTime(timeField))
			return false;
		updateRecordToGrid(rowObj,index);
		cancelDialog(index);
	} else {
		if(!validateForm(index,id))
			return false;
		if(!validateTime(timeField))
			return false;
		addRecordToGrid(index);
		openReportsTimingsDialog(index);
	}
}

function getIndexedTableElement(tableName, elName, index) {
	var elements = tableName.rows[index+1].getElementsByTagName('input');
	var len = elements.length;
	
	for(var i=0;i<elements.length;i++) {
		if(elements[i].name == elName) {
			return elements[i];
		}
	}
	return null;
}

function getReportsDetailsIndexedValue(table, name, index) {
	var obj = getIndexedTableElement(table, name, index);
	if (obj) 
		return obj.value;
	else
	return null;
}



function getReportsRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function addRecordToGrid(index) {
	var table = document.getElementById("resultTable"+index);
	var id = getNumItems(index);
	var fieldset = document.getElementById('fieldset'+index);
		fieldset.setAttribute("style","height:");
	var defaultVal = document.getElementById('default_value').value;
	var fromTime = document.getElementById('report_time'+index).value;
	
	var templateRow = table.rows[getTemplateRow(index)];
	var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	delvalue = getElementByName(row,"delivery_time").value;
	
	setNodeText(row.cells[DELIVERY_TIME_COL], !empty(fromTime) ? fromTime : '');
	setHiddenValue(table,id, "rep_deliv_default_id", "_");
	setHiddenValue(table,id, "day_of_week", index);
	setHiddenValue(table,id, "delivery_time", fromTime);
	setHiddenValue(table,id, "default_value","false");
	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", 'Edit delivery report details');
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("id", "editIcon"+index);
		editImg.setAttribute("onclick","openEditReportsTimingsDialogBox(this," +index+')');
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title",'Delete delivery report details');
		deleteImg.setAttribute("onclick","deleteItem(this,"+index+')');
		deleteImg.setAttribute("class", "button");

	for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
		row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
	}
		row.cells[DELETE_COL].appendChild(deleteImg);

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}
		row.cells[EDIT_COL].appendChild(editImg);

	clearDialog(index);
	document.getElementById('report_time'+index).focus();
}

function updateRecordToGrid(row,index) {
	var table = document.getElementById("resultTable"+index);
	var fromTime = document.getElementById('report_time'+index).value;
	var fieldset = document.getElementById('fieldset'+index);
		fieldset.setAttribute("style","height:");
		
	setNodeText(row.cells[DELIVERY_TIME_COL], !empty(fromTime) ? fromTime : '');
	getElementByName(row,"day_of_week").value = index;
	getElementByName(row,"delivery_time").value = fromTime;
	
	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", 'Edit Delivery Reports');
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditReportsTimingsDialogBox(this," +index+')');
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", "Cancel Delivery Reports Details");
		deleteImg.setAttribute("onclick","deleteItem(this, " +index+')');
		deleteImg.setAttribute("class", "button");

	for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
		row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
	}
		row.cells[DELETE_COL].appendChild(deleteImg);

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}
	row.cells[EDIT_COL].appendChild(editImg);
	document.getElementById('dialogId'+index).value = index;
	clearDialog(index);
}

function getReportDetailsRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

// click on delete image deleting the selected row and again timings is being calculated and appended to the grid.
// if there is only one available timings then deleting that row default timing will be appended to the grid.
function deleteItem(imgObj,index) {
	var table = document.getElementById("resultTable"+index);
	var rowObj = getThisRow(imgObj);
	var deltedId = rowObj.rowIndex;
	var len = table.rows.length-3;
	var row = getReportsRow(deltedId-1, 'resultTable'+index);
	var id = getReportDetailsRowChargeIndex(row);
	var fieldset = document.getElementById('fieldset'+index);
	
	var isNew = getReportsDetailsIndexedValue(table,'rep_deliv_default_id', id) == '_';
	var flag = false;
	if (isNew) {
		table.deleteRow(deltedId);
		j--;
		if (len == 0) {
			insertRowValues(table,"","");
		}
	} else {
	var flagImgs = row.cells[DELETE_COL].getElementsByTagName("img");
	var cls = '';
	if (id == "_") {
		table.deleteRow(deltedId);
		insertRowValues(table,"","");
	}  else {
		var newDeleted;
		var oldDeleted =  getReportsDetailsIndexedValue(table, "r_delete", id);
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
	var trashSrc;
	var flagSrc;
	if (newDeleted == 'true') {
		setReportsDetailsIndexedValue(table,'r_delete', id, 'true');
		flagSrc = cpath + '/icons/undo_delete.gif';
	} if (oldDeleted == 'true') {
		setReportsDetailsIndexedValue(table,'r_delete', id, 'false');
		flagSrc = cpath + '/icons/delete.gif';
	}
	row.className = cls;
	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;
	}
	}
}

function validateTime(timeField) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '') {
		showMessage("js.scheduler.schedulerdashboard.enter.time");
		timeField.focus();
		return false;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			showMessage("js.scheduler.schedulerdashboard.incorrecttimeformat.hour");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			showMessage("js.scheduler.schedulerdashboard.incorrecttimeformat.minute");
			timeField.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			showMessage("js.scheduler.schedulerdashboard.incorrecthour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			showMessage("js.scheduler.schedulerdashboard.incorrectminute");
			timeField.focus();
			return false;
		}
		if(strMinutes.length !=2){
			showMessage("js.scheduler.schedulerdashboard.incorrectminute.digits");
			return false;
		}

		if(strHours.length != 2) {
			showMessage("js.scheduler.schedulerdashboard.incorrecthourdigits");
			return false;
		}
	} else {
		showMessage("js.scheduler.doctornonavailability.incorrecttimeformat");
		timeField.focus();
		return false;
	}
	return true;
}


function insertRowValues(table,index,deliveryTime) {
	var templateRow = null;
	if (!empty(index+''))
		templateRow = table.rows[getTemplateRow(index)];
	else
		templateRow = table.rows[getTemplateRowWithoutIndex()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);

		setNodeText(row.cells[DELIVERY_TIME_COL], !empty(deliveryTime) ? deliveryTime : '');
		getElementByName(row,"delivery_time").value = deliveryTime;
		if (!empty(index+''))
			getElementByName(row,"day_of_week").value = index;
		getElementByName(row,"default_value").value = 'true';
		for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
			row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
		}
		
		var deleteImg = document.createElement("img");
			deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
			deleteImg.setAttribute("title", 'Delete Report Timings');
		if (!empty(index+''))
			deleteImg.setAttribute("onclick","deleteItem(this,"+index+')');
		else
			deleteImg.setAttribute("onclick","deleteItem(this)");
		deleteImg.setAttribute("class", "button");

		for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
			row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
		}

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}

	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", 'Edit Reports Timings');
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("id", "editIcon"+index);
		if (!empty(index+''))
			editImg.setAttribute("onclick","openEditReportsTimingsDialogBox(this," +index+')');
		else
			editImg.setAttribute("onclick","openEditReportsTimingsDialogBox(this)");
		editImg.setAttribute("class", "button");

	//row.cells[EDIT_COL].appendChild(editImg);
}

function getTemplateRowWithoutIndex() {
	// gets the hidden template row index: this follows header row + num Information.
	return getNumItemsWithoutIndex() + 1;
}

function getNumItemsWithoutIndex() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("resultTable"+index).rows.length-2;
}



