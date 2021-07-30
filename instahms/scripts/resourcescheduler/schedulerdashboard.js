function doClose() {
	window.location.href = cpath+"/master/resourceschedulers/list.htm?method=list&status=A";
}

function doOpenTab() {
	window.location.href = cpath+"/master/resourceschedulers/show.htm?method=show&status=A";
}
var i=0;
	FROM_TIME_COL=i++; TO_TIME_COL = i++; STATUS_COL = i++;
	if(resource_type =='DOC' && max_centers_inc_default > 1)
	CENTER_COL=i++; 
	if(resource_type == 'DOC' ){
		VISIT_MODE_COL=i++;
	}
	REMARKS_COL = i++; DELETE_COL=i++; EDIT_COL=i++;

var gRowObj;

function submitForm(){
	var category = document.forms[0].res_sch_type.value;
	var duration = "None";

	if (empty(category)) {
		window.location.href = "../../master/resourceschedulers/add.htm?method=add&res_sch_type="+category+"&resource_type=primary";
	}
	else {
		window.location.href = "../../master/resourceschedulers/add.htm?method=add&res_sch_type="+category+"&category_default_duration="+duration+"&resource_type=primary";
	}
}

function initDialog() {
	if ((resourceName == '*' && resource_type == 'DOC' && centerId != 0))
		document.getElementById('savebtn').disabled = true;
	else
		document.getElementById('savebtn').disabled = false;
	for(var i=0;i<7;i++) {
		initResourceTimingsDialog(i);
	}
}

var gIndex;
var resourceTimingDialog = new Array();

function initResourceTimingsDialog(index) {
	var resourceTimingDialogDiv = document.getElementById("resorceTimingsDialog"+index);
	resourceTimingDialogDiv.style.display = 'block';
	resourceTimingDialog[index] = new YAHOO.widget.Dialog("resorceTimingsDialog"+index,{
			width:"450px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeResourceTimingsDialog,
	                                                scope:resourceTimingDialog[index],
	                                                correctScope:true } );
	resourceTimingDialog[index].cfg.queueProperty("keylisteners", escKeyListener);
	resourceTimingDialog[index].render();
}

function closeResourceTimingsDialog() {
	clearDialog(gIndex);
	resourceTimingDialog[gIndex].hide();
}

function getResourceTimings(duration,index) {
	var screenId = "defaultResourceAvailability";

	if (!empty(duration)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = cpath+'/master/resourceschedulers/getResourceTimingsByDuration.htm?duration='+duration+'&screenId='+ screenId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return handleAjaxTimingResponse(reqObject.responseText,index);
			}
		}
	}
}

function handleAjaxTimingResponse(responseText,index) {
	eval("var responseOfSelectedSchedule = " + responseText);

	var timeList = null;
	document.getElementById('dialog_from_time'+index).length = 1;
	document.getElementById('dialog_to_time'+index).length = 1;

	if (responseOfSelectedSchedule != null) {
		timeList = responseOfSelectedSchedule;
	}
	var rform =  document.resourceAvailableForm;

	var curdate = new Date();

	if (timeList != null && timeList.length > 0) {
		for (var i = 0; i < timeList.length; i++) {
			curdate.setTime(timeList[i].from_time);
			var hours = curdate.getHours();
			if (hours < 10) {
				hours = '0' + hours;
			}
			var minutes = curdate.getMinutes();
			if (minutes < 10) {
				minutes = '0' + minutes;
			}
			var time = hours + ':' + minutes;
			var option_from = new Option(time, time);
			var option_to = new Option(time, time);

			document.getElementById('dialog_from_time'+index).options[document.getElementById('dialog_from_time'+index).length] = option_from;
			document.getElementById('dialog_to_time'+index).options[document.getElementById('dialog_to_time'+index).length] = option_to;
		}
	}
}

var addEdit='';
function openResourceTimingsDialog(index) {
	var duration = document.resourceAvailableForm.duration.value;
	var resourceId = document.resourceAvailableForm.schedulerName.value;
	if (empty(duration)) {
		showMessage("js.scheduler.schedulerdashboard.duration");
		document.resourceAvailableForm.duration.focus();
		return;
	}
if (!empty(duration))
		getResourceTimings(duration,index);

	if(resource_type == 'DOC' && max_centers_inc_default > 1){
		if(empty(resourceId)){
			showMessage("js.scheduler.schedulerdashboard.resourcenameempty");
			document.resourceAvailableForm.duration.focus();
			return;
		}
	}

	button = document.getElementById("btnAddItem"+index);
	resourceTimingDialog[index].cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('resourcedialogheader'+index).innerHTML = getString('js.scheduler.resourceavailability.details');
	document.getElementById('dialog_from_time'+index).disabled = false;
	document.getElementById('dialog_to_time'+index).disabled = false;
	var resourceType = document.getElementById('res_sch_type').value;
	resourceTimingDialog[index].show();
	document.getElementById('dialog_from_time'+index).focus();
	gIndex = index;
	edited = false;
	addEdit='add';
	
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
	 getCenterName(index,addEdit);
	 document.getElementById('dialog_center_label'+index).innerHTML =document.getElementById("login_center_name").value;
	}
	if(resourceType == 'DOC')
		enableVisitMode(resourceId,doctorJSON,index);
}

function cancelDialog(index) {
	clearDialog(index);
	resourceTimingDialog[index].cancel();
}

function clearDialog(index){
	document.getElementById('dialog_from_time'+index).value = '';
	document.getElementById('dialog_to_time'+index).value = '';
	document.getElementById('dialog_remarks'+index).value = '';
	document.getElementById('dialogId'+index).value = '';
	if(!empty(document.getElementById('dialog_visit_mode'+index)))
		document.getElementById('dialog_visit_mode'+index).value = 'I';
	if(!empty(document.getElementById('dialog_center_name_hid'+index)))
		document.getElementById('dialog_center_name_hid'+index).value = '';
	var rStatus = null;

	if (!empty(gRowObj))
		rStatus = getElementByName(gRowObj,"availability_status").value;
	if (!empty(rStatus) && rStatus == 'N')
		gRowObj.setAttribute("class","notAvailableRow");
}
var edited = false;
function openEditResTimingsDialogBox(obj,iIndex) {
	var rowObj = findAncestor(obj,"TR");
	var table = document.getElementById('resultTable'+iIndex);
	gRowObj = table.rows[rowObj.rowIndex];
	var index = getRowItemIndex(rowObj);

	var duration = document.resourceAvailableForm.duration.value;
	var resourceId = document.resourceAvailableForm.schedulerName.value;
	if (empty(duration)) {
		showMessage("js.scheduler.schedulerdashboard.duration");
		document.resourceAvailableForm.duration.focus();
		return;
	}

	if (!empty(duration))
		getResourceTimings(duration,iIndex);

	rowObj.className = 'selectedRow';
	document.getElementById('resourcedialogheader'+index).innerHTML = getString('js.scheduler.resourceavailability.editdetails');
	document.getElementById('dialogId'+iIndex).value = iIndex;
	updateGridToDialog(obj,rowObj,iIndex);
	document.getElementById('dialog_from_time'+index).focus();
	gIndex = iIndex;
	edited = false;
	addEdit='edit';
	var resourceType = document.getElementById('res_sch_type').value;
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		getCenterName(iIndex,addEdit);
	}
	if(resourceType == 'DOC')
		enableVisitMode(resourceId,doctorJSON,iIndex);
}

function updateGridToDialog(obj,rowObj,index) {
	var loginCenterId = document.getElementById("login_center_id").value;
	var availableStatus = getElementByName(rowObj,"availability_status").value;
	var loginCenterName= document.getElementById("login_center_name").value;
	var resourceType = document.getElementById('res_sch_type').value;
	document.getElementById('dialog_from_time'+index).value 	=  getElementByName(rowObj,"from_time").value;
	document.getElementById('dialog_to_time'+index).value =  getElementByName(rowObj,"to_time").value;
	//showing center name to edit dialogBox for doctor
	if(resourceType == 'DOC' && max_centers_inc_default > 1 && document.getElementById('dialog_center_label'+index) != null){
		document.getElementById('dialog_center_label'+index).innerHTML = getElementByName(rowObj,"center_name").value;
		document.getElementById("dialog_center_name_hid"+index).value = getElementByName(rowObj,"center_id").value;
	}
	if(resourceType == 'DOC' && document.getElementById('dialog_visit_mode'+index) != null){
		document.getElementById('dialog_visit_mode'+index).value = getElementByName(rowObj,"visit_mode").value;
	}
	document.getElementById('dialog_remarks'+index).value =  getElementByName(rowObj,"remarks").value;
	resourceTimingDialog[index].cfg.setProperty("context", [obj, "tr", "br"], false);
	// Centerwise shown edit time duration Enable and Disable for doctor 
	if(resourceType == 'DOC' && max_centers_inc_default > 1 && document.getElementById('dialog_from_time'+index) != null 
			&& getElementByName(rowObj,"center_name") != null){
		var centerName = getElementByName(rowObj,"center_name").value;
		if (availableStatus == 'A' && loginCenterId == 0) {
			document.getElementById('dialog_from_time'+index).disabled = false;
			document.getElementById('dialog_to_time'+index).disabled = false;
		}
		else if(availableStatus == 'A' && loginCenterId != 0 && centerName == loginCenterName){
			document.getElementById('dialog_from_time'+index).disabled = false;
			document.getElementById('dialog_to_time'+index).disabled = false;

		} else {
			document.getElementById('dialog_from_time'+index).disabled = true;
			document.getElementById('dialog_to_time'+index).disabled = true;
		}
	//for other resource it will work same as non center		
	}else{
		if (availableStatus == 'N') {
			document.getElementById('dialog_from_time'+index).disabled = true;
			document.getElementById('dialog_to_time'+index).disabled = true;
		} else {
			document.getElementById('dialog_from_time'+index).disabled = false;
			document.getElementById('dialog_to_time'+index).disabled = false;
		}

	}	
	resourceTimingDialog[index].show();
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

function validateForm(index,id) {
	var table = document.getElementById('resultTable'+index);
	var dialogFromTime = document.getElementById('dialog_from_time'+index);
	var dialogToTime = document.getElementById('dialog_to_time'+index);
	var dialogCenterName = document.getElementById('dialog_center_name_hid'+index);
	var flag = true;
	var d_fromTimeArr = dialogFromTime.value.split(":");
	var d_toTimeArr = dialogToTime.value.split(":");
	var dialogStartDate = new Date();
		dialogStartDate.setHours(parseInt(d_fromTimeArr[0],10));
		dialogStartDate.setMinutes(parseInt(d_fromTimeArr[1],10));
		dialogStartDate.setSeconds(0);
		dialogStartDate.setMilliseconds(0);
	var dialogEndDate = new Date();
		dialogEndDate.setHours(parseInt(d_toTimeArr[0],10));
		dialogEndDate.setMinutes(parseInt(d_toTimeArr[1],10));
		dialogEndDate.setSeconds(0);
		dialogEndDate.setMilliseconds(0);
	var dStartTimeInLong = dialogStartDate.getTime();
	var dEndTimeInLong = dialogEndDate.getTime();

	if(empty(dialogFromTime.value)){
		showMessage("js.scheduler.schedulerdashboard.fromtime");
		dialogFromTime.focus();
		return false;
	}

	if(empty(dialogToTime.value)){
		showMessage("js.scheduler.schedulerdashboard.totime");
		dialogToTime.focus();
		return false;
	}
	
	var login_center_id=document.getElementById("login_center_id").value;
	var resourceType = document.getElementById('res_sch_type').value;
	if((resourceType == 'DOC' && max_centers_inc_default >1) && (addEdit == 'add' && login_center_id == 0)){
		if(empty(dialogCenterName.value)){
			showMessage("js.scheduler.schedulerdashboard.centerName");
			dialogCenterName.focus();
			return false;
		}
	}

	if (dEndTimeInLong <= dStartTimeInLong) {
		showMessage("js.scheduler.schedulerdashboard.totime.morethan.fromtime");
		dialogToTime.focus();
		return false;
	}

	if(trimAll(dialogFromTime.value) != '' && trimAll(dialogToTime.value) != '') {
		if(!validateTime(dialogFromTime)){
			dialogFromTime.focus();
			return false;
		}
		if(!validateTime(dialogToTime)){
			dialogToTime.focus();
			return false;
		}
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

	for (var i=0;i<id;i++) {
	 	rowObj = table.rows[i+1];
	 	fromTime = getElementByName(rowObj,"from_time").value;
	 	toTime = getElementByName(rowObj,"to_time").value;
	 	rStatus = getElementByName(rowObj,"availability_status").value;
	 	fromDateAndTime = new Date();
	 	toDateAndTime = new Date();
	 	fromTimeArr = fromTime.split(":");
	 	toTimeArr = toTime.split(":");
		fromDateAndTime.setHours(parseInt(fromTimeArr[0],10));
		fromDateAndTime.setMinutes(parseInt(fromTimeArr[1],10));
		fromDateAndTime.setSeconds(0);
		fromDateAndTime.setMilliseconds(0);
		toDateAndTime.setHours(parseInt(toTimeArr[0],10));
		toDateAndTime.setMinutes(parseInt(toTimeArr[1],10));
		toDateAndTime.setSeconds(0);
		toDateAndTime.setMilliseconds(0);
		dFromDateAndTimeInlong = fromDateAndTime.getTime();
		dToDateAndTimeInlong = toDateAndTime.getTime();

		if (dFromDateAndTimeInlong == dStartTimeInLong && dEndTimeInLong == dToDateAndTimeInlong && rStatus != 'N' && gRowObj != rowObj) {
			showMessage("js.scheduler.schedulerdashboard.duplicate.entry");
			return false;
		}

		if (rStatus != 'N' && gRowObj != rowObj) {
		/*	if ((dFromDateAndTimeInlong > dStartTimeInLong && dToDateAndTimeInlong < dStartTimeInLong)
				|| (dEndTimeInLong > dFromDateAndTimeInlong  && dEndTimeInLong < dToDateAndTimeInlong)
				|| (dFromDateAndTimeInlong > dStartTimeInLong && dFromDateAndTimeInlong < dEndTimeInLong && dEndTimeInLong >= dToDateAndTimeInlong)
				|| (dStartTimeInLong > dFromDateAndTimeInlong && dStartTimeInLong < dToDateAndTimeInlong && dEndTimeInLong > dToDateAndTimeInlong)) {*/

			if((dFromDateAndTimeInlong < dStartTimeInLong && dToDateAndTimeInlong > dStartTimeInLong)
					|| (dFromDateAndTimeInlong >= dStartTimeInLong && dFromDateAndTimeInlong < dEndTimeInLong)
					|| (dStartTimeInLong > dFromDateAndTimeInlong && dStartTimeInLong < dToDateAndTimeInlong)) {

				flag = false;
				break;
			}
		}
	}

	if (!flag) {
		showMessage("js.scheduler.schedulerdashboard.timeslot.overlapping");
		document.getElementById('dialog_from_time'+index).focus();
		return false;
	}
	return flag;
}
var disableBulkResourceFlag;
function addRecord(index) {
	var table = document.getElementById("resultTable"+index);
	var dialogId = document.getElementById('dialogId'+index).value;
	var defaultRow = document.getElementById('defaultRow'+index);
	var resourceType = document.getElementById("res_sch_type").value ;
	/*if (defaultRow)
		table.deleteRow(1);*/
	var id = getNumItems(index);

	if(resourceType == 'DOC')
		disableBulkResourceFlag = true;
	if (!empty(dialogId)) {
		var rowObj = getItemRow(index,gRowObj);
		var rStatus = getElementByName(rowObj,"availability_status").value;
		if (rStatus != 'N') {
			if (!validateForm(index,id))
				return false;
		}
		updateRecordToGrid(rowObj,index);
		cancelDialog(index);
	} else {
		if(!validateForm(index,id))
			return false;
		if(resourceType == 'DOC' && max_centers_inc_default > 1 && centerId !=0 ){
			if(!getResourceCenter(index))
				return false;
		}
		addRecordToGrid(index);
		openResourceTimingsDialog(index);
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

function addRecordToGrid(index) {
	var table = document.getElementById("resultTable"+index);
	var id = getNumItems(index);
	var fieldset = document.getElementById('fieldset'+index);
		fieldset.setAttribute("style","height:");
	var templateRow = table.rows[getTemplateRow(index)];
	var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	var fromTime = document.getElementById('dialog_from_time'+index).value;
	var toTime = document.getElementById('dialog_to_time'+index).value;
	var status = 'Available';
	var res_sch_name = document.getElementById('schedulerName').value;
	var centerName;
	var centerId;
	var visitMode;
	var loginCenterName= document.getElementById("login_center_name").value;
	var loginCenterId= document.getElementById("login_center_id").value;
	var resourceType = document.getElementById('res_sch_type').value;
	//Adding center name to grid only for doctor centerwise
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		if(loginCenterId==0 && res_sch_name != '*'){
			if(document.getElementById('dialog_center_name'+index) != null){
				centerName = document.getElementById('dialog_center_name'+index).options
				[document.getElementById('dialog_center_name'+index).selectedIndex].text;
				centerId = document.getElementById('dialog_center_name'+index).value;
			} else if(document.getElementById('select_center_id') != null){
				centerName = document.getElementById('select_center_id').options
				[document.getElementById('select_center_id').selectedIndex].text;
				centerId = document.getElementById('select_center_id').value;
			}
		}
		else{
			centerName = loginCenterName;
			centerId   = loginCenterId;
		}
	}
	var remarks = document.getElementById('dialog_remarks'+index).value;
	var appendRemarks = null;

	if(resourceType == 'DOC') {
		visitMode = document.getElementById('dialog_visit_mode'+index).value;
	}
	if (remarks.length >= 30) {
		appendRemarks = remarks.substring(0,30) + "...";
	}

	setNodeText(row.cells[FROM_TIME_COL], !empty(fromTime) ? fromTime : '');
	setNodeText(row.cells[TO_TIME_COL], !empty(toTime) ? toTime : '');
	setNodeText(row.cells[STATUS_COL], !empty(status) ? status : '');
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		setNodeText(row.cells[CENTER_COL], !empty(centerName) ? centerName : '');
	}
	if(resourceType == 'DOC'){
		setNodeText(row.cells[VISIT_MODE_COL], getVisitMode(visitMode));
	}
	setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
	setHiddenValue(table,id, "day_of_week", index);
	setHiddenValue(table,id, "from_time", fromTime);
	setHiddenValue(table,id, "to_time", toTime);
	setHiddenValue(table,id, "availability_status", "A");
	setHiddenValue(table,id, "center_id" , centerId);
	setHiddenValue(table,id, "center_name" , centerName);
	setHiddenValue(table,id, "visit_mode", visitMode);
	setHiddenValue(table,id, "remarks", remarks);
	setHiddenValue(table,id, "default_value","false");

	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString('js.scheduler.resourceavailability.editdetails'));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("id", "editIcon"+index);
		editImg.setAttribute("onclick","openEditResTimingsDialogBox(this," +index+')');
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title",getString("js.scheduler.resourceavailability.deletetimingsrow"));
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

	calculateUnavailableTimings(table,index,'Y');
	clearDialog(index);
	document.getElementById('dialog_from_time'+index).focus();
}

function updateRecordToGrid(row,index) {
	var table = document.getElementById("resultTable"+index);
	var fromTime = document.getElementById('dialog_from_time'+index).value;
	var toTime = document.getElementById('dialog_to_time'+index).value;
	var status = 'Available';
	var fieldset = document.getElementById('fieldset'+index);
		fieldset.setAttribute("style","height:");
	var rStatus = getElementByName(row,"availability_status").value;
	var remarks = document.getElementById('dialog_remarks'+index).value;
	var appendRemarks = null;
	if (remarks.length >= 30) {
		appendRemarks = remarks.substring(0,30) + "...";
	}
	if(resource_type == 'DOC') {
		visitMode = document.getElementById('dialog_visit_mode'+index).value;
	}
	setNodeText(row.cells[FROM_TIME_COL], !empty(fromTime) ? fromTime : '');
	setNodeText(row.cells[TO_TIME_COL], !empty(toTime) ? toTime : '');
	if (rStatus == 'A')
		setNodeText(row.cells[STATUS_COL], !empty(status) ? status : '');
	else
		setNodeText(row.cells[STATUS_COL], 'NotAvailable');
	if(resource_type == 'DOC' ){
		setNodeText(row.cells[VISIT_MODE_COL], getVisitMode(visitMode));
	}
	setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
	getElementByName(row,"day_of_week").value = index;
	getElementByName(row,"from_time").value = fromTime;
	getElementByName(row,"to_time").value = toTime;
	if (rStatus == 'A')
		getElementByName(row,"availability_status").value = "A";
	else
		getElementByName(row,"availability_status").value = "N";
	if(resource_type == 'DOC' && getElementByName(row,"visit_mode") != null)
		getElementByName(row,"visit_mode").value=visitMode;
	getElementByName(row,"remarks").value = remarks;
	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.scheduler.resourceavailability.edittimings"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditResTimingsDialogBox(this," +index+')');
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.scheduler.resourceavailability.deletetimingsrow"));
		deleteImg.setAttribute("onclick","deleteItem(this, " +index+')');
		deleteImg.setAttribute("class", "button");

	var deleteImg1 = document.createElement("img");
		deleteImg1.setAttribute("src", cpath + "/icons/Delete1.png");
		deleteImg1.setAttribute("title","");
		deleteImg1.setAttribute("onclick","");
		deleteImg1.setAttribute("class", "button");

	for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
		row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
	}
	if (getElementByName(row,"availability_status").value =="A" ){
		if(resource_type == 'DOC' && max_centers_inc_default > 1){
			if(document.getElementById('dialog_center_name_hid'+index) != null){
				var rCenterId = document.getElementById('dialog_center_name_hid'+index).value;
				if(centerId == 0 || rCenterId == centerId)
					row.cells[DELETE_COL].appendChild(deleteImg);
				else
					row.cells[DELETE_COL].appendChild(deleteImg1);
			}
		}else{
			row.cells[DELETE_COL].appendChild(deleteImg);
		}	
	}else{
		row.cells[DELETE_COL].appendChild(deleteImg1);
	}

	if(row.cells[EDIT_COL]){
		for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
			row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
		}
		row.cells[EDIT_COL].appendChild(editImg);
	}
	if (rStatus == 'A' && edited)
		calculateUnavailableTimings(table,index,'Y');
	else
		row.setAttribute("class","notAvailableRow");
	document.getElementById('dialogId'+index).value = index;
}

// click on delete image deleting the selected row and again timings is being calculated and appended to the grid.
// if there is only one available timings then deleting that row default timing will be appended to the grid.

function deleteItem(imgObj,index) {
		var table = document.getElementById("resultTable"+index);
		var rowObj = getThisRow(imgObj);
		var fieldset = document.getElementById('fieldset'+index);
		var table = document.getElementById("resultTable"+index);
		var deltedId = rowObj.rowIndex;
		var flag = false;

		if (!empty(deltedId)) {
			table.deleteRow(deltedId);
			calculateUnavailableTimings(table,index,'Y');
			fieldset.setAttribute("style","height:");
		}

		var ids = table.rows.length-2;

		if (ids < 1) {
			flag = true;
		}

		if (flag) {
			insertRowValues(table,index,"N","00:00","23:59","","","","");
		}
}

function selectResource(index,resourceid){
	var resourcetypeid = "type" + index;
	var resourcevalueid = "resource" + index;
	var resourceType = document.getElementById(resourcetypeid).value;
	var resourceValue = document.getElementById(resourcevalueid);
	var list = null;
	if(resourceType == 'SUDOC'){
		loadSelectBox(resourceValue, surgeonsJSON, "doctor_name", "doctor_id","-- Select --");
	}else if(resourceType == 'ANEDOC'){
		loadSelectBox(resourceValue, anesthestistsJSON, "doctor_name", "doctor_id","-- Select-- ");
	}else if(resourceType == 'THID'){
		loadSelectBox(resourceValue, theatresJSON, "THEATRE_NAME", "THEATRE_ID","-- Select --");
	}else if(resourceType == 'EQID'){
		loadSelectBox(resourceValue, equipmentsJSON, "equipment_name", "eq_id","-- Select --");
	}else if(resourceType == 'OPDOC' || resourceType == 'DOC'){
		loadSelectBox(resourceValue, scheduleDoctorJSON, "doctor_name", "doctor","-- Select --");
	}else if(resourceType == 'LABTECH'){
		loadSelectBox(resourceValue, labTechniciansJSON, "doctor_name", "doctor_id","-- Select --");
	} else if(resourceType == 'SRID'){
		loadSelectBox(resourceValue, serviceResourcesListJson, "serv_resource_name", "serv_res_id","-- Select --");
	} else if(resourceType == 'ASUDOC'){
		loadSelectBox(resourceValue, surgeonsJSON, "doctor_name", "doctor_id","-- Select --");
	} else if(resourceType == 'PAEDDOC'){
		loadSelectBox(resourceValue, surgeonsJSON, "doctor_name", "doctor_id","-- Select --");
	} else {
		if(!empty(genericResourceListJson))
			list = filterList(genericResourceListJson,"scheduleable","T");
		if(!empty(list))
			list = filterList(list,"scheduler_resource_type",resourceType);
		if(!empty(list))
			loadSelectBox(resourceValue, list, "generic_resource_name", "generic_resource_id","-- Select --");
	}
	if (resource_type == 'DOC' && resourceType =="DOC") {
		document.getElementById("priresource"+index).value = true;
	} else if (resource_type == 'SER' && resourceType =="SRID") {
		document.getElementById("priresource"+index).value = true;
	} else if(resource_type == 'TST' && resourceType =="EQID") {
		document.getElementById("priresource"+index).value = true;
	} else if(resource_type == 'OPE' && resourceType =="THID") {
		document.getElementById("priresource"+index).value = true;
	}
	setSelectedIndex(resourceValue, resourceid);
	document.getElementById('resource_name'+index).value = document.getElementById(resourcetypeid).value;
	if(empty(document.getElementById(resourcetypeid).value) && !empty(document.getElementById(resourcevalueid))) {
		document.getElementById(resourcevalueid).value = "";
		document.getElementById("resource_value"+index).value = "";
	}
}



function showResourceName(catgory,loadOrEdit,resName){
	var dept = document.forms[0].dept != null? document.forms[0].dept.value :'' ;
	
	var method = document.forms[0].method.value;
	var category = "";
		category = document.forms[0].res_sch_type.value;
	if(loadOrEdit == 'edit') {
		document.getElementById("_resource_name").value = "";
		document.getElementById("schedulerName").value = "";
	}
	var schedulerName = document.forms[0].schedulerName;
	var list = null;
	if (!empty(category)) {
		if(category == 'DOC' || category == 'SER' || category == 'SUR' || category == 'TST'){
			document.getElementById("dept1").value = dept;
			schedulerResourcetype = "";
			list = filterList(allResourcesList,"resource_type",category);
			list  = filterList(list,"dept_id",dept);
		} else if(category == 'EQID' || category == 'SRID' || category == 'THID'){
			schedulerResourcetype = "";
			list  = filterList(allResourcesList,"resource_type",category);
		} else {
			var schedulerResourcetype = 'GEN';
			list = filterList(allResourcesList,"resource_type",schedulerResourcetype);
			if(!empty(list)) {
				list  = filterList(list,"scheduler_resource_type",category);
			}
		}

		if(!empty(list)) {
			resourceNameAutoComplete(list,resName);
		} else {
			if (!empty(oAutoComp)) {
				oAutoComp.destroy();
				oAutoComp = null;
				document.getElementById('_resource_name').value = "";
				document.getElementById('schedulerName').value = "";
			}
		}
		if(category == 'DOC' && max_centers_inc_default > 1){
			var schId = document.getElementById('schedulerName').value;
			makeCenterAjaxCall(schId);
		}
	}
	disableIfGeneric();
}

var oAutoComp = null;
function resourceNameAutoComplete(list,resourceName) {
	if (!empty(oAutoComp)) {
		oAutoComp.destroy();
		oAutoComp = null;
	}
	var resList = {
		result: list
	};
	var ds = new YAHOO.util.LocalDataSource(resList);
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = {
		resultsList: "result",
		fields: [{
			key: "resource_name"
		}, {
			key: "resource_id"
		}]
	};
	oAutoComp = new YAHOO.widget.AutoComplete('_resource_name', 'resourceNameContainer', ds);

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	document.getElementById('resourceNameContainer').style.width = "900px";
	oAutoComp.maxResultsDisplayed = 20;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList = false;
	oAutoComp.queryMatchContains = true;
	oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	if(!empty(resourceName)) {
		var resourceList = filterList(list,"resource_id",resourceName);
		if(!empty(resourceList)) {
			document.getElementById("_resource_name").value = resourceList[0]["resource_name"];
			document.getElementById("schedulerName").value = resourceList[0]["resource_id"];
			document.getElementById("_resource_name").title = resourceList[0]["resource_name"];
		}
	}
	oAutoComp.itemSelectEvent.subscribe(selectItem);
	oAutoComp.textboxBlurEvent.subscribe(function () {
		var sNames = document.getElementById("_resource_name").value;
		if (sNames != '') {
			document.getElementById("_resource_name").title = sNames;
		} else {
			document.getElementById("_resource_name").removeAttribute("title");
		}
	});

	if (oAutoComp._elTextbox.value != '') {
		oAutoComp._bItemSelected = true;
		oAutoComp._sInitInputValue = oAutoComp._elTextbox.value;
	}
	return oAutoComp;
}

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	var sNames = record.resource_name;
	document.getElementById('schedulerName').value = record.resource_id;
	document.getElementById('_resource_name').value = sNames;
	document.getElementById("_resource_name").title = sNames;
	//On select of doctor resource disabling auto complete and department field only for doctor in center schema
	var resourceType = document.getElementById('res_sch_type').value;
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		document.getElementById('_resource_name').readOnly = false;
		document.forms[0].dept.disabled = false;
		var sId = record.resource_id;
		makeCenterAjaxCall(sId);
	}
	
}


function showAllResources(){
	var table = document.getElementById("resourceTable");
	var count = 0;
	deleteRows();
	if(resourceBeanJSON == '') {
		addResourceRow();
	} else {
		for(var i=0;i<resourceBeanJSON.length;i++){
			var resource = resourceBeanJSON[i];
			addResourceRow();

			var typeId = "type"+count;
			var resourceId = "resource"+count;
			var item_id = "item_id" + count;
			var priresource = "priresource" + count;

			var resourceTypeObj = document.getElementById(typeId);
			var resourceValueObj = document.getElementById(resourceId);
			var item_id = document.getElementById(item_id);
			var priresource = document.getElementById(priresource);

			setSelectedIndex(resourceTypeObj ,resource["resource_type"]);
			selectResource(count,resource["resource_id"]);
			item_id.value = resource["resource_id"];
			priresource.value = resource["primary_resource"];
			document.getElementById('resource_name'+count).value = resource["resource_type"];
			document.getElementById('resource_value'+count).value = resource["resource_id"];
			if (!empty(resourceTypeObj.value) && !empty(resourceValueObj.value)) {
				resourceTypeObj.disabled = false;
				resourceValueObj.disabled = false;
			}
			enableButton(count);
			if (empty(resourceTypeObj.value) || empty(resourceValueObj.value)) {
				table.deleteRow(count+1);
			} else {
				count++;
			}
			if (count == 0 && i == resourceBeanJSON.length-1) {
				addResourceRow();
			}
		}
	}
}

function deleteRows(){
	var table = document.getElementById("resourceTable");
	if(table != null) {
		var rowCount = table.rows.length;
		for(var i=1; i<rowCount;i++){
			table.deleteRow(-1);
		}
	}
}

function changeElsColor(index) {
	var markRowForDelete =
	document.getElementById('resourceDelete'+index).value = document.getElementById('resourceDelete'+index).value == 'N' ? 'Y' :'N';

	if (markRowForDelete == 'Y') {
		addClassName('type' + index, "delete");
		addClassName('resource' + index, "delete");
  	} else {
  		checkDuplicates(index);
  		removeClassName('type' + index, "delete");
  		removeClassName('resource' + index, "delete");
  	}
}

function addResourceRow(){
	var rtab = document.getElementById("resourceTable");
	if(rtab != null) {
		var rlen = rtab.rows.length;
		var id = rlen - 1;
		var newRow = "",typeTd="",resourceTd = "",rcheckbox="";

		var typeId = "type"+id;
		var resourceId = "resource"+id;
		var imgDeleteCheckId = "resourceCheckBox"+id;
		var resourceDeleteId = "resourceDelete"+id;
		var itemId = "item_id"+id;
		var priresource = "priresource" + id;
		var imageSrc = cpath + "/icons/Delete.png";

		newRow = rtab.insertRow(rlen);
		var rowId = "row"+id;
		newRow.id = rowId;

		typeTd = newRow.insertCell(0);
		typeTd.setAttribute('class', 'first');
		typeTd.innerHTML = '<select name="rType" class="dropdown" style="width: 200;" id="'+typeId+'" onchange="selectResource('+id+')"></select>';
		var hiddenNameObj = makeHidden('resource_name','resource_name'+id);
		typeTd.appendChild(hiddenNameObj);
		resourceTd = newRow.insertCell(1);
		var hiddenValueObj = makeHidden('resource_value','resource_value'+id);
		resourceTd.innerHTML = '<select name="rValue" class="dropdown" style="width: 200;" id="'+resourceId+'" onchange="checkDuplicates('+id+');enableButton('+id+');"> '+
								'</select> <input type="hidden" name="item_id" id="'+itemId+'" value=""/> <input type="hidden" name="priresource" id="'+priresource+'" value=""/>';
		resourceTd.appendChild(hiddenValueObj);

		loadSelectBox(document.getElementById(typeId), resourceTypeFilteredJSON, "resource_description", "resource_type","Select");
		rdeleteImg = newRow.insertCell(2);
		rdeleteImg.setAttribute('class', 'last');
		rdeleteImg.innerHTML =
			'<a href="javascript:void(0)" onclick="changeElsColor('+id+');"> '+
			'<img src="'+imageSrc+'" name="'+ imgDeleteCheckId +'" class="imgDelete" id="'+ imgDeleteCheckId +'" /></a>' +
			'<input type="hidden" name="rDelete" id="'+resourceDeleteId+'" value="N"/>';
		enableButton(id);
	}
}

function innerResourcesTable(){
	if(!validate()){
		return false;
	}
	if(document.getElementById("resourceTable") != null) {
	var tablen = document.getElementById("resourceTable").rows.length;
		if(tablen>1){
			for(var j=0;j<(tablen-1);j++){
				var resourceType = "type" + j;
				var resourceValue = "resource" + j;
				var resourceCheck = "resourceCheckBox" + j;
				var resourceDelete = "resourceDelete" + j;
				var itemId = "item_id" + j;
				 if(document.getElementById(resourceType).value != "" && document.getElementById(resourceValue).value != "" ){

						var type = document.getElementById(resourceType).value;
						var resource = document.getElementById(resourceValue).value;
						var check = document.getElementById(resourceCheck).value;
						var item = document.getElementById(itemId).value;
						var del = document.getElementById(resourceDelete).value;

						var innerResourceTab = document.getElementById("InnerResourceTable");
						var trObj = "", tdObj = "";
						trObj = innerResourceTab.insertRow(-1);

						tdObj = trObj.insertCell(0);
						tdObj.innerHTML = '<input type="hidden" name="resourceCheck" id="'+resourceCheck+'" value="'+ check +'">';

						tdObj = trObj.insertCell(1);
						tdObj.innerHTML = '<input type="hidden" name="resourceDelete" id="'+resourceDelete+'" value="'+ del +'">';

						tdObj = trObj.insertCell(2);
						tdObj.innerHTML = '<input type="hidden" name="resourceType" id="'+resourceType+'" value="'+ type +'">';

						tdObj = trObj.insertCell(3);
						tdObj.innerHTML = '<input type="hidden" name="resourceValue" id="'+resourceValue+'" value="'+ resource +'">';

						tdObj = trObj.insertCell(4);
						tdObj.innerHTML = '<input type="hidden" name="itemId" id="'+itemId+'" value="'+ item +'">';
				}else{}
			}
		}
	}
}

function checkDuplicates(index){
	var rtab = document.getElementById("resourceTable");
	var rlen = rtab.rows.length;
	var resourceType = document.getElementById("type" + index).value;
	var resourceId = document.getElementById("resource" + index).value;

	for(var k=0;k<(rlen-1);k++){
		if((k != index) && (resourceType == document.getElementById("type" + k).value)) {
			if((document.getElementById("priresource" + k).value == "true") || (resourceId == document.getElementById("resource" + k).value)) {
				if ((document.getElementById("resourceDelete"+k).value == 'N') && (document.getElementById("resourceDelete"+index).value == 'N')) {
					showMessage("js.scheduler.schedulerdashboard.duplicate.entry");
					document.getElementById("resource" + index).selectedIndex = 0;
						return false;
				}
			}
		} 
	}
	document.getElementById('resource_value'+index).value = resourceId;
}

function validateSecondaryResource() {
	var rtab = document.getElementById("resourceTable");
	var flag = true;
	var j = 0;
	if(rtab) {
		var rlen = rtab.rows.length;
		for(var k=0;k<(rlen-1);k++){
			var resourceType = document.getElementById("type" + k).value;
			var resourceId = document.getElementById("resource" + k).value;
			if (document.getElementById("resourceDelete"+k).value == 'Y') {
				j++;
			}
			if (empty(resourceType) || empty(resourceId) && rlen > 2) {
				if (document.getElementById("resourceDelete"+k).value == 'Y') {
					document.getElementById("resource_name"+k).disabled = true;
					document.getElementById("resource_value"+k).disabled = true;
					document.getElementById("resourceDelete"+k).disabled = true;
				} else if (rlen > 2) {
					showMessage("js.scheduler.schedulerdashboard.secondaryresource.required");
					document.getElementById('resource'+k).focus();
					flag = false;
					break;
				}
			}
		}
	}
	return flag;
}

function disableIfGeneric(){
	if(document.forms[0].method.value != 'create'){
		var res_sch_id = document.forms[0].res_sch_id.value;
		var res_sch_name = document.getElementById('res_sch_name').value;
		if(res_sch_name == '*' || (!empty(res_sch_name && resource_type == 'DOC'))){
			if(document.forms[0].dept) document.forms[0].dept.disabled = true;
			if(document.forms[0]._resource_name) document.forms[0]._resource_name.readOnly = true;
			if(document.forms[0].description) document.forms[0].description.readOnly = true;
			document.forms[0].schedulerName.value = res_sch_name;
		}
	}
}

function enableButton(index){
	var resourceType = document.getElementById("type" + index).value;
	var resourceId = document.getElementById("resource" + index).value;
	if(resourceType != "" && resourceId != ""){
		document.forms[0].addresource.disabled = false;
	}else{
		document.forms[0].addresource.disabled = true;
	}
}


function validate(){
	var method = document.forms[0].method.value;
	var category = document.forms[0].res_sch_type;
	if (!empty(category) && (category.value == 'DOC' ||category.value == 'SUR'||category.value == 'SER'||category.value == 'TST'))
		var dept = document.forms[0].dept.value;
	if (!empty(category.value))
		var schedulerName = document.forms[0]._resource_name.value;
	var duration = document.forms[0].duration.value;
	var res_sch_name = document.getElementById('res_sch_name').value;
	var status = document.forms[0].status.value;
/*  var morningStartTime = document.forms[0].morning_start_time;
	var morningEndTime = document.forms[0].morning_end_time;
	var eveningStartTime = document.forms[0].evening_start_time;
	var eveningEndTime = document.forms[0].evening_end_time;*/
	var description = document.forms[0].description.value;

	if(method == 'create'){
		if(empty(category.value)){
			showMessage("js.scheduler.schedulerdashboard.schedulertype");
			category.focus();
			return false;
		}
		if(!empty(category.value) &&  (category.value == 'DOC' ||category.value == 'SUR'||category.value == 'SER'||category.value == 'TST') && empty(dept)){
			showMessage("js.scheduler.schedulerdashboard.department");
			document.forms[0].dept.focus();
			return false;
		}
		if(empty(schedulerName)){
			showMessage("js.scheduler.schedulerdashboard.schedulername");
			document.getElementById('_resource_name').focus();
			return false;
		}

	// as part of bug#33706 -- this part of code is commented out.
	/*	if (!isOverridesExist(category.value,document.forms[0].schedulerName.value)) {
			return false;
		}*/
	} else{
		var res_sch_id = document.forms[0].res_sch_id.value;
		if(empty(dept)){
			if((category.value == 'DOC' ||category.value == 'SUR'||category.value == 'SER'||category.value == 'TST') && res_sch_name != '*'){
				showMessage("js.scheduler.schedulerdashboard.department");
				return false;
			}
			else{}
		}
		if(empty(schedulerName)){
			if(!empty(res_sch_name) && res_sch_name == '*'){
			}else{
				showMessage("js.scheduler.schedulerdashboard.schedulername");
				document.getElementById('_resource_name').focus();
				return false;
			}
		}

	// as part of bug#33706 -- this part of code is commented out.
	/*	if(!empty(schedulerName) && !empty(category.value) && !empty(status) && status == 'A') {
			if (!isOverridesExist(category.value,document.forms[0].schedulerName.value)) {
				return false;
			}
		}*/
	}

	if(!validateSecondaryResource()) {
		return false;
	}

	if(duration == ''){
		showMessage("js.scheduler.schedulerdashboard.duration");
		document.forms[0].duration.focus();
		return false;
	}
	if(document.forms[0].height_in_px != null) {
		var height_in_px = document.forms[0].height_in_px.value;
		if(height_in_px == ''){
			showMessage("js.scheduler.schedulerdashboard.height");
			document.forms[0].height_in_px.focus();
			return false;
		}

		if(height_in_px == 0){
			showMessage("js.scheduler.schedulerdashboard.height.nottozero");
			document.forms[0].height_in_px.focus();
			return false;
		}
	}

/*	if(morningStartTime.value == '' && morningEndTime.value == ''
		&& eveningStartTime.value == '' && eveningEndTime.value == '') {
		showMessage("Enter timings");
		morningStartTime.focus();
		return false;
	}
	if(morningStartTime.value == '' && morningEndTime.value != ''){
		showMessage("Enter morning start time");
		morningStartTime.focus();
		return false;
	}
	if(morningEndTime.value == '' && morningStartTime.value != ''){
		showMessage("Enter morning end time");
		morningEndTime.focus();
		return false;
	}
	if(eveningStartTime.value == '' && eveningEndTime.value != ''){
		showMessage("Enter evening start time");
		eveningStartTime.focus();
		return false;
	}
	if(eveningEndTime.value == '' &&  eveningStartTime.value != ''){
		showMessage("Enter evening end time");
		eveningEndTime.focus();
		return false;
	}

	if(trimAll(morningStartTime.value) != '' && trimAll(morningEndTime.value) != '') {
		if(!validateTime(morningStartTime)){
			morningStartTime.value = "";
			return false;
		}
		if(!validateTime(morningEndTime)){
			morningEndTime.value = "";
			return false;
		}
		if(!validateStartEndTime(morningStartTime, morningEndTime)){
			return false;
		}
	}
	if(trimAll(eveningStartTime.value) != '' && trimAll(eveningEndTime.value) != '') {
		if(!validateTime(eveningStartTime)){
			eveningStartTime.value = "";
			return false;
		}
		if(!validateTime(eveningEndTime)){
			eveningEndTime.value = "";
			return false;
		}
		if(!validateStartEndTime(eveningStartTime, eveningEndTime)){
			return false;
		}
	}
	if(trimAll(morningEndTime.value) != '' && trimAll(eveningStartTime.value) != '') {
		if(!validateStartEndTime(morningEndTime, eveningStartTime)){
			return false;
		}
	}*/
	return true;

}

// as part of bug#33706 -- this part of code is commented out.
/*
function isOverridesExist(resourceType,resourceName) {
	var ajaxReqObject = newXMLHttpRequest();
	var url = "../resourcescheduler/categorymaster.do?_method=isOverridesExist";
	url = url + "&resource_type=" + resourceType;
	url = url + "&scheduler_name=" + resourceName;
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (!empty(reqObject.responseText) && reqObject.responseText != "doesNotExist")) {
			var message ="Default timings have been overriden for resource "+reqObject.responseText+"."+
				"\nPlease go to Resource availability and  delete the overrides,\n"+
				"before creating/updating new default timings for "+reqObject.responseText+".";
			showMessage(message);
			return false;
		}
	}
	return true;
}*/

function validateStartEndTime(startTime, endTime){
	var sttime  = startTime.value.split(":");
	var endtime  = endTime.value.split(":");
	if(eval(sttime[0]) < eval(endtime[0])){
	}else{
		if(eval(sttime[1]) < eval(endtime[1])){
		}else{
			var msg=(endTime.name.split("_")[0]+" "+endTime.name.split("_")[1]);
				msg+=getString("js.scheduler.schedulerdashboard.time.lessthan");
				msg+=startTime.name.split("_")[0];
				msg+=" ";
				msg+=startTime.name.split("_")[1];
				msg+=getString("js.scheduler.schedulerdashboard.time");
				alert(msg);
			endTime.focus();
			return false;
		}
	}
	return true;
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

function getCompleteTime(action,obj,index){
	if (!empty(gRowObj)) {
		var rowObj = getItemRow(index,gRowObj);
		var time = getElementByName(rowObj,action).value;
		if (!empty(obj.value)) {
			if ((obj.value == time))
				edited = false;
			else
				edited = true;
		}
	}
}

function checkLength(obj,len,field){
	if( obj.value.length  > len ){
		var msg=getString("js.scheduler.schedulerdashboard.max");
		msg+=len;
		msg+=getString("js.scheduler.schedulerdashboard.charsallowed");
		msg+=field;
		alert(msg);
		obj.value = (obj.value).substring(0,parseInt(len)-1);
		obj.focus();
		return false;
	}
	return true;
}
var gSchCenterList;
//ajax call to get resource center
function makeCenterAjaxCall(sId){
	var schedulerId = sId;
	var url = cpath+'/master/resourceschedulers/getSchedulerCenter.htm?doctor_id='+schedulerId;
	var ajaxObj = newXMLHttpRequest();
	ajaxObj.open("POST", url.toString(), false);
	ajaxObj.send(null);
	if (ajaxObj.readyState == 4 && ajaxObj.status == 200) {
		eval("var response =" +ajaxObj.responseText);
		gSchCenterList = response.centerlist;
	}
}

function getCenterName(index,addEdit){
	var res_sch_name = document.getElementById('schedulerName').value;
	var loginCenterId= document.getElementById("login_center_id").value;
	var cen = document.getElementById("dialog_center_name"+index);
	var resourceType = document.getElementById('res_sch_type').value;
	
	if(addEdit == 'add' && loginCenterId == 0){
		loadSelectBox(cen, gSchCenterList , "center_name", "center_id", "-- Select --");
		document.getElementById("dialog_center_name"+index).style.display = 'block';
		document.getElementById("dialog_center_label"+index).style.display = 'none';
		if(res_sch_name == '*'){
			document.getElementById("dialog_center_name"+index).style.display = 'none';
			document.getElementById("dialog_center_label"+index).style.display = 'block';
			document.getElementById("dialog_center_name_hid"+index).value = loginCenterId;
		}
			
	}
	else{
		document.getElementById("dialog_center_name"+index).style.display = 'none';
		document.getElementById("dialog_center_label"+index).style.display = 'block';
		if(addEdit=='add' && loginCenterId!=0){
			document.getElementById("dialog_center_name_hid"+index).value = loginCenterId;
		}

	}
		
}

function fillHidenCeterName(evt, index){
	document.getElementById("dialog_center_name_hid"+index).value = evt.value;
}

function fillVisitMode(evt,index){
	document.getElementById("dialog_visit_mode"+index).value = evt.value;
}

function getResourceCenter(index){
	var selectedCenterId= document.getElementById("dialog_center_name_hid"+index).value;
	var loginCenterId= document.getElementById("login_center_id").value;
	var resourceId = document.getElementById("schedulerName").value ;
	var resourceCategory = document.getElementById("res_sch_type").value ;
	var doctorCenter =[];
	if(resourceId == ""){
		showMessage("js.scheduler.schedulerdashboard.resourcenameempty");
		return false;
	}
	if(resourceId == '*' && loginCenterId != 0 ){
		showMessage("js.scheduler.schedulerdashboard.resourcecentername");
		return false;
	}else{
		if(resourceId == '*' && loginCenterId == 0 ){
			return true;
		}
	}
	
	if(resourceId != '*'){
		for(var i=0;i<doctorJSON.length;i++){
			if(resourceId == doctorJSON[i].doctor_id){
				doctorCenter.push(doctorJSON[i].center_id)
			}
		}
		for(var i=0; i<=doctorCenter.length; i++){
			if(doctorCenter[i] == 0){
				return true;
			}
			if(selectedCenterId ==doctorCenter[i]){
				return true;
			}
		}
		showMessage("js.scheduler.schedulerdashboard.resourcecentername");
		return false;
	}
}
		
