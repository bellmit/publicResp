/**
 * 
 */


function init() {
	initResourceTimingsDialog();
}

var gRowObj;

var reportsTimingsDialog ;
function initResourceTimingsDialog() {
	var reportsTimingDialogDiv = document.getElementById("reportsTimingsDialog");
	reportsTimingDialogDiv.style.display = 'block';
	reportsTimingsDialog = new YAHOO.widget.Dialog("reportsTimingsDialog",{
			width:"450px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeReportsTimingsDialog,
	                                                scope:openReportsTimingsDialog,
	                                                correctScope:true } );
	reportsTimingsDialog.cfg.queueProperty("keylisteners", escKeyListener);
	reportsTimingsDialog.render();
}

function closeReportsTimingsDialog() {
	clearDialog();
	reportsTimingsDialog.hide();
}

var edited = false;
function openReportsTimingsDialog() {
	
	button = document.getElementById("btnAddItem");
	reportsTimingsDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('reportsdialogheader').innerHTML = 'Reports Override Details';
	document.getElementById('report_time').disabled = false;
	reportsTimingsDialog.show();
	document.getElementById('delivery_time').focus();
	clearDialog();
	edited = false;
}

function cancelDialog() {
	clearDialog();
	reportsTimingsDialog.cancel();
}

function clearDialog(){
	document.getElementById('report_time').value = '';
	document.getElementById('dialogId').value = '';
}

var i=0;
DELIVERY_TIME_COL=i++; DELETE_COL=i++; EDIT_COL=i++;


var edited = false;
function openEditReportsTimingsDialogBox(obj) {
	var table = document.getElementById('resultTable');
	var rowObj = findAncestor(obj,"TR");
	var index = getRowItemIndex(rowObj);
	gRowObj = rowObj;
	rowObj.className = 'selectedRow';
	document.getElementById('reportsdialogheader').innerHTML = 'Reports Override Edit Details';
	document.getElementById('dialogId').value = index;
	updateGridToDialog(obj,rowObj,index);
	document.getElementById('report_time').focus();
	edited = false;
}

function updateGridToDialog(obj,rowObj) {
	document.getElementById('report_time').value 	=  getElementByName(rowObj,"delivery_time").value;
	var index = getRowItemIndex(rowObj);
	
	if (getElementByName(rowObj,"default_value").value == 'true') {
		
		document.getElementById('report_time').disabled = true;
	} else {
		document.getElementById('report_time').disabled = false;
	}
	
	reportsTimingsDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	//clearDialog();
	reportsTimingsDialog.show();
}

function getNumItems() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("resultTable").rows.length-2;
}

function getFirstItemRow() {
	// index of the first Information item: 0 is header, 1 is first Information item.
	return 1;
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num Information.

	return getNumItems() + 1;
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getItemRow(i) {
	i = parseInt(i);
	var table = document.getElementById("resultTable");
	return table.rows[i + getFirstItemRow()];
}

function getReportsRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getPregnancyDetailsChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstPregnancyDetailsRow()];
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.ReportsOverrideForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getReportsDetailsIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.ReportsOverrideForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setReportsDetailsIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.ReportsOverrideForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function validateForm(id) {
	var table = document.getElementById('resultTable');
	var reportTime = document.getElementById('report_time').value;
	var dialogId = document.ReportsOverrideForm.dialogId.value;
	var flag = true;
	var deliv_fromTimeArr = reportTime.split(":");
	var deliveryStartDate = new Date();
		deliveryStartDate.setHours(parseInt(deliv_fromTimeArr[0],10));
		deliveryStartDate.setMinutes(parseInt(deliv_fromTimeArr[1],10));
		deliveryStartDate.setSeconds(0);
		deliveryStartDate.setMilliseconds(0);
	
	var delivStartTimeInLong = deliveryStartDate.getTime();

	if(reportTime == '') {
		alert("Enter Report Delivery Time");
		document.getElementById('report_time').focus();
		return false;
	}


	var rowObj;
	var fromTime;
	var toTime;
	var rStatus;
	var fromDateAndTime;
	var toDateAndTime;
	var fromTimeArr;
	var toTimeArr;
	var dFromDateAndTimeInlong;
	var dToDateAndTimeInlong;
	var reportTimes = document.getElementsByName("delivery_time");
	
	for (var i=0;i<reportTimes.length;i++) {
		var id=getNumItems();
		 if ((i!=id) && (reportTimes[i].value == document.getElementById("report_time").value)) {
		   	alert("Duplicate Entry");
		   		document.getElementById("report_time").focus();
		   		return false;
		 }
	  }
	return true;
}



function addRecord() {
	var id = getNumItems();
	var dialogId = document.ReportsOverrideForm.dialogId.value;
	var defaultRow = document.getElementById('defaultRow');
	var timeField = document.getElementById('report_time');
	if (!empty(defaultRow)) {
		defaultRow.parentNode.removeChild(defaultRow);
	}
	var check = true;
	if (!empty(dialogId)) {
		var rowObj = getItemRow(dialogId);
		if(!validateTime(timeField))
			return false;
		updateRecordToGrid(rowObj,check);
		cancelDialog();
	} else {
		if (!validateForm(id))
			return false;
		if(!validateTime(timeField))
			return false;
		addRecordToGrid();
		openReportsTimingsDialog();
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

function addRecordToGrid() {
	var table = document.getElementById("resultTable");
	var defaultVal = document.getElementById('default_value').value;
	
	var id = getNumItems();
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	var deliveryTime = document.getElementById('report_time').value;
	
	setNodeText(row.cells[DELIVERY_TIME_COL], !empty(deliveryTime) ? deliveryTime : '');
	setHiddenValue(id, "rep_delivery_override_time_id", "_");
	setHiddenValue(id, "delivery_time", deliveryTime);
	setHiddenValue(id, "r_delete","false");

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", getString("js.scheduler.resourceavailability.editdetails"));
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditReportsTimingsDialogBox(this)");
	editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
	deleteImg.setAttribute("title", getString("js.scheduler.resourceavailability.deletedetails"));
	deleteImg.setAttribute("onclick","deleteItem(this)");
	deleteImg.setAttribute("class", "button");

	for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
		row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
	}
	row.cells[DELETE_COL].appendChild(deleteImg);

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}
	row.cells[EDIT_COL].appendChild(editImg);
	clearDialog();
	document.getElementById('report_time').focus();
}

function updateRecordToGrid(row) {
	var table = document.getElementById("resultTable");
	var deliveryTime = document.getElementById('report_time').value;
	var status = 'Available';
	var defaultRow = document.getElementById('defaultRow');
	var appendRemarks = null;
	var id = getNumItems();
	var index = getRowItemIndex(row);
	
	setNodeText(row.cells[DELIVERY_TIME_COL], !empty(deliveryTime) ? deliveryTime : '');

	if (defaultRow)
		defaultRow.setAttribute("id","");
		getElementByName(row,"delivery_time").value = deliveryTime;

		var editImg = document.createElement("img");
			editImg.setAttribute("src", cpath + "/icons/Edit.png");
			editImg.setAttribute("title", 'Reports Availability Timings');
			editImg.setAttribute("name", "editIcon");
			editImg.setAttribute("onclick","openEditReportsTimingsDialogBox(this)");
			editImg.setAttribute("class", "button");

			var deleteImg = document.createElement("img");
			deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
			deleteImg.setAttribute("title", 'Delete Reports Availability Timings');
			deleteImg.setAttribute("onclick","deleteItem(this)");
			deleteImg.setAttribute("class", "button");

			var deleteImg1 = document.createElement("img");
			deleteImg1.setAttribute("src", cpath + "/icons/Delete1.png");
			deleteImg1.setAttribute("title", "");
			deleteImg1.setAttribute("onclick","");
			deleteImg1.setAttribute("class", "button");

			for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
				row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
			}

			row.cells[DELETE_COL].appendChild(deleteImg);

			for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
				row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
			}
			row.cells[EDIT_COL].appendChild(editImg);

			document.getElementById('dialogId').value = getRowItemIndex(row);
}

function getFirstReportsDetailsRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getReportsDetailsThisRow(node) {
	return findAncestor(node, "TR");
}

function getReportDetailsRowChargeIndex(row) {
	return row.rowIndex - getFirstReportsDetailsRow();
}

function deleteItem(imgObj) {
	var row = getReportsDetailsThisRow(imgObj);
	var id = getReportDetailsRowChargeIndex(row);
	var table = document.getElementById("resultTable");
	var len = table.rows.length-3;
	var rowObj = getThisRow(imgObj);
	var table = document.getElementById("resultTable");
	var deltedId = rowObj.rowIndex;
	var row = getReportsRow(deltedId-1, 'resultTable');
	var oldDeleted =  getReportsDetailsIndexedValue("r_delete", id);
	var isNew = getReportsDetailsIndexedValue('rep_delivery_override_time_id',id) == '_';
	if (isNew) {
		row.parentNode.removeChild(row);
		i--;
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
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
	
	var flagSrc;
	if (newDeleted == 'true') {
		setReportsDetailsIndexedValue('r_delete', id, 'true');
		flagSrc = cpath + '/icons/undo_delete.gif';
	} 
	if (oldDeleted == 'true') {
		setReportsDetailsIndexedValue('r_delete', id, 'false');
		flagSrc = cpath + '/icons/delete.gif';
	}
	row.className = cls;
	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;
	}
	}
}

function setReportsDetailsRowStyle(i) {
	var row = getItemRow(i, 'resultTable');
	var overrideDetailsId = getReportsDetailsIndexedValue("rep_delivery_override_time_id", i);
}

function validateTime(timeField) {
	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '') {
		alert("Enter Time");
		timeField.focus();
		return false;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			alert("Incorrect time format : hour is not a number");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			alert("Incorrect time format : minute is not a number");
			timeField.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			alert("Incorrect hour : please enter 0-23 for hour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			alert("Incorrect minute : please enter 0-59 for minute");
			timeField.focus();
			return false;
		}
		if(strMinutes.length !=2){
			alert("incorrect minute please enter 2 digit minutes");
			return false;
		}

		if(strHours.length != 2) {
			alert("incorrect hour please enter 2 digit hours");
			return false;
		}
	} else {
		alert("Incorrect time format : please enter HH:MM");
		timeField.focus();
		return false;
	}
	return true;
}

function dateValidate(inputField) {
	var pickeddate = getDateFromField(inputField);
	todayDate = new Date();
	todayDate.setDate(todayDate.getDate()-1);
	if(pickeddate > todayDate) {
		return true;
	} else {
		alert("Report Delivery Start Date can not be past");
			return false;
	}
}


function validate() {
	var method = document.ReportsOverrideForm._method.value;
	var table = document.getElementById('resultTable');
	var b_from_date = document.getElementById('b_report_start_date');
	var b_to_date = document.getElementById('b_report_end_date');
	var overrideTime = document.getElementById('delivery_time');
	
	if (empty(overrideTime.value)) {
		alert("Enter atleast one report delivery time");
			return false;
	}

	if (empty(b_from_date.value)) {
			alert("Report Start Date should not be empty");
			b_from_date.focus();
		return false
	}

	if (empty(b_to_date.value)) {
			alert("Report End Date should not be empty");
			b_to_date.focus();
		return false
	}

	var in_to_date_arr = b_to_date.value.split("-");
	var in_from_date_arr = b_from_date.value.split("-");
	var in_to_date = new Date(in_to_date_arr[2],in_to_date_arr[1],in_to_date_arr[0]);
	var in_from_date = new Date(in_from_date_arr[2],in_from_date_arr[1],in_from_date_arr[0]);
	var long_to_date = in_to_date.getTime();
	var long_from_date = in_from_date.getTime();

	if (long_to_date < long_from_date) {
			alert("Report End Date should not be less than Report Start Date");
			b_to_date.focus();
		return false;
	}

	var dateObj = document.getElementById('b_report_start_date');
	if (!dateValidate(dateObj)) {
		dateObj.focus();
		return false;
	}

	var numRows = getNumItems();
	var allChecked = false;
	var b_from_date = document.getElementById('report_time');
	var in_from_date_arr = b_from_date.value.split("-");
	var in_from_date = new Date(in_from_date_arr[2],in_from_date_arr[1],in_from_date_arr[0]);
	var long_from_date = in_from_date.getTime();

	return true;
}

function getCompleteTime(action,obj){
var time = getElementByName(gRowObj,action).value;
if (!empty(obj.value)) {
	if ((obj.value == time))
		edited = false;
	else
		edited = true;
}
}

function deleteSelectedRows(resAvailIds) {
	var reqObj = newXMLHttpRequest();
	var url = "../master/reportscenteroverrides.do?"+
				"_method=deleteSelectedRows&rep_deliv_override_id="+resAvailIds;
	reqObj.open("POST", url.toString(), false);
	reqObj.send(null);
	if (reqObj.readyState == 4) {
		if ((reqObj.status == 200) && (reqObj.responseText != null)) {
			var obj = reqObj.responseText;
			if (obj != null && obj == 'Deleted') {
				return true;
			}
		}
	}
	return false;
}

function deleteAllSelectedRows() {
	var checkBoxes = document.ReportsSearchForm._cancelResourceAvailability;
	var anyChecked = false;
	var disabledCount = 0;
	var totalCancellation = 1;
	if (checkBoxes.length) {
		totalDelete = checkBoxes.length;
		for (var i=0; i<checkBoxes.length; i++) {
			if (!checkBoxes[i].disabled && checkBoxes[i].checked) {
				anyChecked = true;
				break;
			}
		}

		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].disabled)
				disabledCount++;
		}

	} else {
		var checkBox = document.ReportsSearchForm._cancelResourceAvailability;
		if (!checkBox.disabled && checkBox.checked)
			anyChecked = true;
		if (checkBox.disabled)
			disabledCount++;
	}
	var resAvailIds = new Array();
	if (checkBoxes.length) {
		var l = 0;
		for(var k=0;k<checkBoxes.length;k++){
			if (checkBoxes[k].checked && !checkBoxes[k].disabled) {
				resAvailIds[l] = checkBoxes[k].value;
				l++;
			}
		}
	} else {
		resAvailIds = checkBox.value;
	}

	if (!anyChecked) {
		showMessage("js.scheduler.resourceavailability.checkrowsdeleted");
		return false;
	}
	if(!deleteSelectedRows(resAvailIds)) {
		showMessage("js.scheduler.resourceavailability.failtodeleterows");
		return false;
	}
	document.ReportsSearchForm._method.value = "list";
	document.ReportsSearchForm.submit();
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

}

function getTemplateRowWithoutIndex() {
	// gets the hidden template row index: this follows header row + num Information.
	return getNumItemsWithoutIndex() + 1;
}

function getNumItemsWithoutIndex() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("resultTable").rows.length-2;
}
