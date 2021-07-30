
function validateResourceForm() {
	
	var resourceType = document.getElementById("res_sch_type");
	var resourceName = document.getElementById("_resource_name");
	var fromDate = document.getElementById("from_date");
	var toDate = document.getElementById("to_date");
	if (empty(resourceType.value)) {
		showMessage("js.scheduler.resourceavailability.resourcetype.required");
		resourceType.focus();
		return false;
	}
	
	if (parseDateStr(fromDate.value) > parseDateStr(toDate.value)) {
		showMessage("js.common.message.date.to.before.from");
		fromDate.focus();
		return false;
	}
		return true;
	
}

function loadResources(obj) {
	//Below code is , on select of resource type submit the page.
	var category = document.forms[0].res_sch_type.value;
	var categoryName = document.forms[0].res_sch_name.value;
	if(method == 'add'){
		if (empty(category))
			window.location.href = "../../master/resourceoverrides/add.htm?method=add&res_sch_type="+''+"&res_sch_name="+'';
		else
			window.location.href = "../../master/resourceoverrides/add.htm?method=add&res_sch_type="+category+"&res_sch_name="+categoryName;
	}
	
	//Below code is, based on selection of resource type getting auto complete resource name
	var resourceType = obj.value;
	var list = null;
	var schedulerResourcetype = null;
	document.getElementById('_resource_name').value = "";
	document.getElementById('res_sch_name').value = "";
	resourceName = document.getElementById('res_sch_name').value;

	if(!empty(resourceType)) {
		if(resourceType == 'DOC' || resourceType == 'THID' || resourceType == 'SRID' || resourceType == 'EQID' || resourceType == 'SUR'
			|| resourceType == 'SER' || resourceType == 'TST') {
			schedulerResourcetype = "";
		} else {
			var schedulerResourcetype = 'GEN';
		}

		if(!empty(schedulerResourcetype)) {
			list = filterList(allResourcesList,"resource_type",schedulerResourcetype);
			if(!empty(list))
				list = filterList(list,"scheduler_resource_type",resourceType);
		}
		else
			list = filterList(allResourcesList,"resource_type",resourceType);


		if(!empty(list)) {
			resourceNameAutoComplete(list);
		}
	} else {
		if (!empty(oAutoComp)) {
			oAutoComp.destroy();
			oAutoComp = null;
		}
	}
}
var resId='';
function init(screen)	{
	resId=resourceName;
	if(screen == 'edit')
		initResourceTimingsDialog();
	if(method == 'list'){
		createToolbar(toolbar);
		if(resourceType == 'DOC' && resourceName !='' && max_centers_inc_default > 1){
			getEnableDisableCheckBox(resId);
		}
	}
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		if(method == 'add'){
			makeCenterAjaxCall(resId);
		}else{
			makeCenterAjaxCall(resId);
		}
	}
	if(method != 'show') {
		var list = null;
		var schedulerResourcetype = null;
		if(!empty(resourceType)) {
			if(resourceType == 'DOC' || resourceType == 'THID' || resourceType == 'SRID' || resourceType == 'EQID' || resourceType == 'SUR'
				|| resourceType == 'SER' || resourceType == 'TST') {
				schedulerResourcetype = "";
			} else {
				var schedulerResourcetype = 'GEN';
			}

			if(!empty(schedulerResourcetype)) {
				list = filterList(allResourcesList,"resource_type",schedulerResourcetype);
				if(!empty(list))
					list = filterList(list,"scheduler_resource_type",resourceType);
			}
			else
				list = filterList(allResourcesList,"resource_type",resourceType);

			if(!empty(list)) {
				resourceNameAutoComplete(list);
			}
		} else {
			if (!empty(oAutoComp)) {
				oAutoComp.destroy();
				oAutoComp = null;
			}
		}
	}
}
function resourceNameAutoComplete(list) {
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
			document.getElementById("res_sch_name").value = resourceList[0]["resource_id"];
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
	//Disabled doctor auto complete selection
	if(method == 'add'){
		var resName = document.getElementById('_resource_name').value;
		if(resourceType == 'DOC' && max_centers_inc_default > 1){
			if(resName!='')
				document.getElementById('_resource_name').disabled = true;
			else
				document.getElementById('_resource_name').disabled = false;
		}
	}
	return oAutoComp;
	
	
}

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	var sNames = record.resource_name;
	document.getElementById('res_sch_name').value = record.resource_id;
	document.getElementById('_resource_name').value = sNames;
	document.getElementById("_resource_name").title = sNames;
	//On select of doctor resource name making field as disable in AddOverride screen.
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		if(method == 'add'){
			document.getElementById('_resource_name').disabled = true;
			var sId = record.resource_id;
			makeCenterAjaxCall(sId);
		}else{
			var sId = record.resource_id;
			makeCenterAjaxCall(sId);
		}
	}
}

var i=0;
	FROM_TIME_COL=i++; TO_TIME_COL = i++; STATUS_COL = i++;
	if(resourceType =='DOC' && max_centers_inc_default > 1)
		CENTER_COL=i++; 
	if(resourceType == 'DOC')
		VISIT_MODE_COL=i++;
	REMARKS_COL = i++; DELETE_COL= i++; EDIT_COL= i++;
var gRowObj;

var resourceTimingDialog;
function initResourceTimingsDialog() {
	var resourceTimingDialogDiv = document.getElementById("resorceTimingsDialog");
	resourceTimingDialogDiv.style.display = 'block';
	resourceTimingDialog = new YAHOO.widget.Dialog("resorceTimingsDialog",{
			width:"450px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeResourceTimingsDialog,
	                                                scope:resourceTimingDialog,
	                                                correctScope:true } );
	resourceTimingDialog.cfg.queueProperty("keylisteners", escKeyListener);
	resourceTimingDialog.render();
}

function closeResourceTimingsDialog() {
	clearDialog();
	resourceTimingDialog.hide();
}

function getResourceTimings(resourceType,schedulerName) {
	var screenId = "resourceAvailability";

	if (!empty(resourceType) && !empty(schedulerName)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = cpath+'/master/resourceschedulers/getResourceTimingsByDuration.htm?resource_type='+resourceType+'&scheduler_name='+ schedulerName+'&screenId='+screenId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return handleAjaxTimingResponse(reqObject.responseText);
			}
		}
	}
}

function handleAjaxTimingResponse(responseText) {
	eval("var responseOfSelectedSchedule = " + responseText);

	var timeList = null;
	document.getElementById('dialog_from_time').length = 1;
	document.getElementById('dialog_to_time').length = 1;

	if (responseOfSelectedSchedule != null) {
		timeList = responseOfSelectedSchedule;
	}
	var rform =  document.ResourceAvailabilityForm;

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

			document.getElementById('dialog_from_time').options[document.getElementById('dialog_from_time').length] = option_from;
			document.getElementById('dialog_to_time').options[document.getElementById('dialog_to_time').length] = option_to;
		}
	}
}


function openResourceTimingsDialog() {
	var res_sch_type = document.ResourceAvailabilityForm.res_sch_type.value;
	var resourceName = document.ResourceAvailabilityForm.res_sch_name.value;

	if (empty(res_sch_type)) {
		showMessage("js.scheduler.resourceavailability.resourcetype.required");
		document.ResourceAvailabilityForm.res_sch_type.focus();
		return;
	}

	if (empty(resourceName)) {
		showMessage("js.scheduler.resourceavailability.resourcename.required");
		document.ResourceAvailabilityForm.res_sch_name.focus();
		return;
	}

	if (!empty(res_sch_type) && !empty(resourceName))
		getResourceTimings(res_sch_type,resourceName);

	button = document.getElementById("btnAddItem");
	resourceTimingDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('resourcedialogheader').innerHTML = getString('js.scheduler.resourceavailability.details');
	document.getElementById('dialog_from_time').disabled = false;
	document.getElementById('dialog_to_time').disabled = false;
	resourceTimingDialog.show();
	document.getElementById('dialog_from_time').focus();
	edited = false;
	addEdit='add';
	if(resourceType =='DOC' && max_centers_inc_default > 1){
			getCenterName(addEdit);
			document.getElementById('dialog_center_label').innerHTML =document.getElementById("login_center_name").value;
	}
	if(resourceType == 'DOC')
		enableVisitMode(resourceName,doctorJSON);
}

function cancelDialog() {
	clearDialog();
	resourceTimingDialog.cancel();
}

function clearDialog(){
	document.getElementById('dialog_from_time').value = '';
	document.getElementById('dialog_to_time').value = '';
	document.getElementById('dialog_remarks').value = '';
	document.getElementById('dialogId').value = '';
	if(!empty(document.getElementById('dialog_center_name_hid')))
		document.getElementById('dialog_center_name_hid').value = '';
	if(!empty(document.getElementById('dialog_visit_mode')))
		document.getElementById('dialog_visit_mode').value ='I';
	var rStatus = null;

	if (!empty(gRowObj))
		 rStatus = getElementByName(gRowObj,"availability_status").value;
	if (!empty(rStatus) && rStatus == 'N')
		gRowObj.setAttribute("class","notAvailableRow");
}

var edited = false;
function openEditResTimingsDialogBox(obj) {
	var res_sch_type = document.ResourceAvailabilityForm.res_sch_type.value;
	var resourceName = document.ResourceAvailabilityForm.res_sch_name.value;
	var rowObj = findAncestor(obj,"TR");
	gRowObj = rowObj;
	var index = getRowItemIndex(rowObj);

	if (empty(res_sch_type)) {
		showMessage("js.scheduler.resourceavailability.resourcetype.required");
		document.ResourceAvailabilityForm.res_sch_type.focus();
		return;
	}

	if (empty(resourceName)) {
		showMessage("js.scheduler.resourceavailability.resourcename.required");
		document.ResourceAvailabilityForm.res_sch_name.focus();
		return;
	}

	if (!empty(res_sch_type) && !empty(resourceName))
		getResourceTimings(res_sch_type,resourceName);

	rowObj.className = 'selectedRow';
	document.getElementById('resourcedialogheader').innerHTML = getString('js.scheduler.resourceavailability.editdetails');
	document.getElementById('dialogId').value = index;
	updateGridToDialog(obj,rowObj);
	document.getElementById('dialog_from_time').focus();
	edited = false;
	addEdit='edit';
	if(resourceType =='DOC' && max_centers_inc_default > 1){
		getCenterName(addEdit);
	}
	if(resourceType == 'DOC')
		enableVisitMode(resourceName,doctorJSON);
}

function updateGridToDialog(obj,rowObj) {
	document.getElementById('dialog_from_time').value 	=  getElementByName(rowObj,"from_time").value;
	document.getElementById('dialog_to_time').value =  getElementByName(rowObj,"to_time").value;
	if(resourceType =='DOC' && max_centers_inc_default > 1){
		document.getElementById('dialog_center_label').innerHTML = getElementByName(rowObj,"center_name").value;
		document.getElementById('dialog_center_name_hid').value =  getElementByName(rowObj,"center_id").value;
	}
	document.getElementById('dialog_remarks').value =  getElementByName(rowObj,"remarks").value;
	var loginCenterId = document.getElementById("login_center_id").value;
	var availableStatus = getElementByName(rowObj,"availability_status").value;
	//var centerName = getElementByName(rowObj,"center_name").value;
	var loginCenterName= document.getElementById("login_center_name").value;
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		var centerName = getElementByName(rowObj,"center_name").value;
		if (availableStatus == 'A' && loginCenterId == 0) {
			document.getElementById('dialog_from_time').disabled = false;
			document.getElementById('dialog_to_time').disabled = false;
		}
		else if(availableStatus == 'A' && loginCenterId != 0 && centerName == loginCenterName){
			document.getElementById('dialog_from_time').disabled = false;
			document.getElementById('dialog_to_time').disabled = false;
			
		} else {
			document.getElementById('dialog_from_time').disabled = true;
			document.getElementById('dialog_to_time').disabled = true;
		}
	//for other resource it will work same as non center		
	}else{
		if (availableStatus == 'N') {
			document.getElementById('dialog_from_time').disabled = true;
			document.getElementById('dialog_to_time').disabled = true;
		} else {
			document.getElementById('dialog_from_time').disabled = false;
			document.getElementById('dialog_to_time').disabled = false;
		}
		
	}
	if(resourceType == 'DOC' && document.getElementById('dialog_visit_mode') != null){
		document.getElementById('dialog_visit_mode').value = getElementByName(rowObj,"visit_mode").value;
	}
	resourceTimingDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	resourceTimingDialog.show();
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

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.ResourceAvailabilityForm, name, index);
		if (el) {
			if (value == null || value == undefined)
				value = "";
			el.value = value;
		}
	}

	function validateForm(id) {
		var table = document.getElementById('resultTable');
		var dialogFromTime = document.getElementById('dialog_from_time');
		var dialogToTime = document.getElementById('dialog_to_time');
		var dialogCenterName = document.getElementById('dialog_center_name_hid');
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
			showMessage("js.scheduler.resourceavailability.fromtime");
			dialogFromTime.focus();
			return false;
		}

		if(empty(dialogToTime.value)){
			showMessage("js.scheduler.resourceavailability.totime");
			dialogToTime.focus();
			return false;
		}
		var login_center_id=document.getElementById("login_center_id").value;
		if((resourceType == 'DOC' && max_centers_inc_default >1) && (addEdit == 'add' && login_center_id == 0)){
			if(empty(dialogCenterName.value)){
				showMessage("js.scheduler.schedulerdashboard.centerName");
				dialogCenterName.focus();
				return false;
			}
		}

		if (dEndTimeInLong <= dStartTimeInLong) {
			showMessage("js.scheduler.resourceavailability.totime.notequal.fromtime");
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

			if (dFromDateAndTimeInlong == dStartTimeInLong && dEndTimeInLong == dToDateAndTimeInlong && gRowObj != rowObj && rStatus != 'N') {
				showMessage("js.scheduler.resourceavailability.duplicate.entry");
				return false;
			}

			if (rStatus != 'N' && gRowObj != rowObj && !empty(dFromDateAndTimeInlong) && !empty(dToDateAndTimeInlong)) {
			/*	if ((dFromDateAndTimeInlong > dStartTimeInLong && dToDateAndTimeInlong < dStartTimeInLong)
					|| (dEndTimeInLong > dFromDateAndTimeInlong  && dEndTimeInLong < dToDateAndTimeInlong)
					|| (dFromDateAndTimeInlong > dStartTimeInLong && dFromDateAndTimeInlong < dEndTimeInLong && dEndTimeInLong > dToDateAndTimeInlong)
					|| (dStartTimeInLong > dFromDateAndTimeInlong && dStartTimeInLong < dToDateAndTimeInlong && dEndTimeInLong > dToDateAndTimeInlong)) {*/

				if((dToDateAndTimeInlong < dStartTimeInLong && dToDateAndTimeInlong > dStartTimeInLong)
					|| (dFromDateAndTimeInlong >= dStartTimeInLong && dFromDateAndTimeInlong < dEndTimeInLong)
					|| (dStartTimeInLong > dFromDateAndTimeInlong && dStartTimeInLong < dToDateAndTimeInlong)) {

					flag = false;
					break;
				}
			}
		}

		if (!flag) {
			showMessage("js.scheduler.resourceavailability.timeslot.overlapping");
			document.getElementById('dialog_from_time').focus();
			return false;
		}
		return flag;
	}

	function addRecord() {
		var id = getNumItems();
		var dialogId = document.ResourceAvailabilityForm.dialogId.value;

		if (!empty(dialogId)) {
			var rowObj = getItemRow(dialogId);
			var rStatus = getElementByName(rowObj,"availability_status").value;
			if (rStatus != 'N') {
				if (!validateForm(id))
					return false;
			}
			updateRecordToGrid(rowObj);
			cancelDialog();
		} else {
			if (!validateForm(id))
				return false;
			if(resourceType == 'DOC' && max_centers_inc_default > 1){
				if(!getResourceCenter())
					return false;
			}
			addRecordToGrid();
			openResourceTimingsDialog();
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
	var addEdit='';
	
function addRecordToGrid() {
	var table = document.getElementById("resultTable");
	var defaultRow = document.getElementById('defaultRow');
	if (defaultRow)
		table.deleteRow(1);
	var id = getNumItems();
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	var fromTime = document.getElementById('dialog_from_time').value;
	var toTime = document.getElementById('dialog_to_time').value;
	var status = 'Available';
	var centerName;
	var centerId;
	var visitMode;
	var loginCenterName= document.getElementById("login_center_name").value;
	var loginCenterId= document.getElementById("login_center_id").value;
	//Adding center name to grid only for doctor centerwise
	if(resourceType == 'DOC' && max_centers_inc_default > 1){
		if(loginCenterId==0){
			centerName = document.getElementById('dialog_center_name').options
			[document.getElementById('dialog_center_name').selectedIndex].text;
			centerId = document.getElementById('dialog_center_name').value;
		}
		else{
			centerName = loginCenterName;
			centerId   = loginCenterId;
		}
	}
	if(resourceType == 'DOC') {
		visitMode = document.getElementById('dialog_visit_mode').value;
	}
	var remarks = document.getElementById('dialog_remarks').value;
	var appendRemarks = null;
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
	setHiddenValue(id, "from_time", fromTime);
	setHiddenValue(id, "to_time", toTime);
	setHiddenValue(id, "availability_status", "A");
	setHiddenValue(id, "center_id" , centerId);
	setHiddenValue(id, "center_name" , centerName);
	setHiddenValue(id, "visit_mode", visitMode);
	setHiddenValue(id, "remarks", remarks);
	setHiddenValue(id, "default_value","false");
	setHiddenValue(id, "r_delete","false");

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", getString("js.scheduler.resourceavailability.editdetails"));
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditResTimingsDialogBox(this)");
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
	calculateUnavailableTimings(table,"","N");
	clearDialog();
	document.getElementById('dialog_from_time').focus();
}

function updateRecordToGrid(row) {
	var table = document.getElementById("resultTable");
	var fromTime = document.getElementById('dialog_from_time').value;
	var toTime = document.getElementById('dialog_to_time').value;
	var status = 'Available';
	var remarks = document.getElementById('dialog_remarks').value;
	var defaultRow = document.getElementById('defaultRow');
	var rStatus = getElementByName(row,"availability_status").value;
	if(resourceType == 'DOC') {
		var visitMode = document.getElementById('dialog_visit_mode').value;
	}
	var appendRemarks = null;
	if (remarks.length >= 30) {
		appendRemarks = remarks.substring(0,30) + "...";
	}

	setNodeText(row.cells[FROM_TIME_COL], !empty(fromTime) ? fromTime : '');
	setNodeText(row.cells[TO_TIME_COL], !empty(toTime) ? toTime : '');
	if (rStatus == 'A')
		setNodeText(row.cells[STATUS_COL], !empty(status) ? status : '');
	else
		setNodeText(row.cells[STATUS_COL], 'NotAvailable');
	if(resourceType == 'DOC' ){
		setNodeText(row.cells[VISIT_MODE_COL], getVisitMode(visitMode));
	}
	setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);

	if (defaultRow)
		defaultRow.setAttribute("id","");
	getElementByName(row,"default_value").value = "false";
	getElementByName(row,"from_time").value = fromTime;
	getElementByName(row,"to_time").value = toTime;

	if (rStatus == 'A')
		getElementByName(row,"availability_status").value = "A";
	else
		getElementByName(row,"availability_status").value = "N";

	if(resourceType == 'DOC' && getElementByName(row,"visit_mode") != null)
		getElementByName(row,"visit_mode").value=visitMode;
	
	getElementByName(row,"remarks").value = remarks;
	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.scheduler.resourceavailability.edittimings"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditResTimingsDialogBox(this)");
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.scheduler.resourceavailability.deletetimingsrow"));
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
	if (rStatus =="A" ){
		if(resourceType == 'DOC' && max_centers_inc_default > 1){
			var rCenterId = document.getElementById('dialog_center_name_hid').value;
			if(loggedInCenterId == 0 || rCenterId == loggedInCenterId)
				row.cells[DELETE_COL].appendChild(deleteImg);
			else
				row.cells[DELETE_COL].appendChild(deleteImg1);
		}else{
			row.cells[DELETE_COL].appendChild(deleteImg);
		}	
	}else{
		row.cells[DELETE_COL].appendChild(deleteImg1);
	}
		
	/*	row.cells[DELETE_COL].appendChild(deleteImg);
	else
		row.cells[DELETE_COL].appendChild(deleteImg1);*/

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}
	row.cells[EDIT_COL].appendChild(editImg);
	if (rStatus == 'A' && edited) {
		calculateUnavailableTimings(table,"","N");
	} else {
		row.setAttribute("class","notAvailableRow");
	}

	document.getElementById('dialogId').value = getRowItemIndex(row);
	//clearDialog();
}

function deleteItem(imgObj) {
	var table = document.getElementById("resultTable");
	var rowObj = getThisRow(imgObj);
	var table = document.getElementById("resultTable");
	var deltedId = rowObj.rowIndex;
	var flag = false;

	if (!empty(deltedId)) {
		table.deleteRow(deltedId);
		calculateUnavailableTimings(table,"","N");
	}
	var ids = table.rows.length-2;

	if (ids < 1) {
		flag = true;
	}

	if (flag) {
		insertRowValues(table,"","N","00:00","23:59","","","","");
	}
}


function validate() {
	var method = document.ResourceAvailabilityForm.method.value;

	if(!empty(method) && method == 'create') {
		var resourceType = document.getElementById('res_sch_type');
		var resourceName = document.getElementById('_resource_name');
		var b_from_date = document.getElementById('from_date');
		var b_to_date = document.getElementById('to_date');
		var numRows = getNumItems();
		var allChecked = false;

		if (empty(resourceType.value)) {
			showMessage("js.scheduler.resourceavailability.resourcetype.required");
			resourceType.focus();
			return false
		}

		if (empty(resourceName.value)) {
			showMessage("js.scheduler.resourceavailability.resourcename.required");
			resourceName.focus();
			return false
		}

		if (empty(b_from_date.value)) {
			showMessage("js.scheduler.resourceavailability.fromtime");
			b_from_date.focus();
			return false
		}

		if (empty(b_to_date.value)) {
			showMessage("js.scheduler.resourceavailability.totime");
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
			showMessage("js.scheduler.resourceavailability.fromdate.greaterthan.todate");
			b_to_date.focus();
			return false;
		}

		if (numRows <= 0) {
			var msg=getString("js.clinicaldata.dialysisadequacy.norecordsingrid");
			msg+="\n";
			msg+=getString("js.clinicaldata.dialysisadequacy.addmorerecords");
			alert(msg);
			document.getElementById('btnAddItem').focus();
			    return false;
		}

		for (var k=0;k<numRows;k++) {
	    	var arowObj = getItemRow(k);
	    	if (getElementByName(arowObj,'r_delete').value == 'false') {
	    		allChecked = true;
	    	}
	    }

	    if (!allChecked) {
	    	showMessage("js.scheduler.resourceavailability.allrecordsdeleted");
			    return false;
	    }
	}
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


function deleteAllSelectedRows() {
	//unCheckResourceOverrides();
	var checkBoxes = document.CategorySearchForm._cancelResourceAvailability;
	var resourceType = document.getElementById('res_sch_type').value ;
	var resourceName = document.getElementById('res_sch_name').value ;
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
		var checkBox = document.CategorySearchForm._cancelResourceAvailability;
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
	document.CategorySearchForm.res_sch_type.value = resourceType;
	if(!deleteSelectedRows(resAvailIds, resourceType)) {
		showMessage("js.scheduler.resourceavailability.failtodeleterows");
		return false;
	}
	document.CategorySearchForm.method.value = "list";
	document.CategorySearchForm.res_sch_name.value = resourceName;
	document.CategorySearchForm.submit();
}

function deleteSelectedRows(resAvailIds, resourceType) {
	var reqObj = newXMLHttpRequest();
	var url = cpath+"/master/resourceoverrides/deleteSelectedRows.htm?res_avail_id="+resAvailIds+"&res_sch_type="+resourceType;
	reqObj.open("GET", url.toString(), false);
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

function checkLength(obj,len,field){
	if( obj.value.length  > len ){
		var msg=getString("js.scheduler.resourceavailability.max");
		msg+=len;
		msg+=getString("js.scheduler.resourceavailability.charsallowed");
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


//Method to get the center drop down in dialog box
function getCenterName(addEdit){
	var res_sch_name = document.getElementById('res_sch_name').value;
	var loginCenterId= document.getElementById("login_center_id").value;
	var cen = document.getElementById("dialog_center_name");
	var resourceType = document.getElementById('res_sch_type').value;

	if(addEdit == 'add' && loginCenterId == 0){
		loadSelectBox(cen, gSchCenterList , "center_name", "center_id", "-- Select --");
		document.getElementById("dialog_center_name").style.display = 'block';
		document.getElementById("dialog_center_label").style.display = 'none';
		if(res_sch_name == '*'){
			document.getElementById("dialog_center_name").style.display = 'none';
			document.getElementById("dialog_center_label").style.display = 'block';
			document.getElementById("dialog_center_name_hid").value = loginCenterId;
			}

		}
		 else{
		document.getElementById("dialog_center_name").style.display = 'none';
		document.getElementById("dialog_center_label").style.display = 'block';
		if(addEdit=='add' && loginCenterId!=0){
			document.getElementById("dialog_center_name_hid").value = loginCenterId;
		}

	}

}
//Method to fill center dialog hidden value
function fillHidenCeterName(evt){
	document.getElementById("dialog_center_name_hid").value = evt.value;
}

//Method to fill visit mode
function fillVisitMode(evt){
	document.getElementById("dialog_visit_mode").value = evt.value;
}
//Method to Filter and validate resource center and selected dialog center should be match.
function getResourceCenter(){
	var selectedCenterId= document.getElementById("dialog_center_name_hid").value;
	var loginCenterId= document.getElementById("login_center_id").value;
	var resourceId = document.getElementById("res_sch_name").value ;
	//var resourceCategory = document.getElementById("res_sch_type").value ;
	var doctorCenter =[];
	if(resourceId == ""){
		showMessage("js.scheduler.schedulerdashboard.resourcenameempty");
		return false;
	}
		for(var i=0;i<doctorJSON.length;i++){
			if(resourceId == doctorJSON[i]['doctor_id']){
				doctorCenter.push(doctorJSON[i]['center_id'])
			}
		}
		for(var i=0; i<=doctorCenter.length; i++){
			if(doctorCenter[i] == 0){
				return true;
			}
			if(selectedCenterId == doctorCenter[i]){
				return true;
			}
		}
		return true;
					
	}
//Method to disable the resource availablilty list check box if doctor belongs to multiple center and 
//not matching with login center except super center.
function getEnableDisableCheckBox(resId){
	var resourceId = document.getElementById("res_sch_name").value ;
	resourceId= resId;
	var loginCenterId= document.getElementById("login_center_id").value;
	var doctorCenter =[];
	var checkBoxes = document.getElementsByName("_cancelResourceAvailability");
	
	for(var i=0;i<doctorJSON.length;i++){
		if(resourceId == doctorJSON[i]['doctor_id']){
			doctorCenter.push(doctorJSON[i]['center_id'])
		}
	}
	
	var key ="Delete";
	var doctCenterLen = doctorCenter.length;
	for(var i=0; i< doctCenterLen; i++){
		
		
		if( checkBoxes instanceof NodeList ) {
			if(doctCenterLen == 1  && doctorCenter[i] != 0){
				for(var j=0; j<checkBoxes.length;j++){
					if((loginCenterId == 0) || (loginCenterId == doctorCenter[i])){
						checkBoxes[j].disabled=false;
					}else{
						checkBoxes[j].disabled=true;
					}
				}
			}
			else if(doctCenterLen == 1  && doctorCenter[i] == 0){
				for(var j=0; j<checkBoxes.length;j++){
					if(loginCenterId == 0 ){
						checkBoxes[j].disabled=false;
					}else 
						checkBoxes[j].disabled=true;
				}
				
			}
			else if(doctCenterLen > 1 ){
				for(var j=0; j<checkBoxes.length;j++){
					if(loginCenterId == 0 ){
						checkBoxes[j].disabled=false;
					}else 
						checkBoxes[j].disabled=true;
				}
			
			}
			
		}else {
			if(checkBoxes ==  null)
			return true;
		}
	
	}
}
//Disable delete tool bar when check box is disable.		
var setHrefs = function(params, id, enableList,toolbarKey){
	if (empty(toolbarKey)) toolbarKey = defaultKey;
	var checkbox = document.getElementById('_cancelResourceAvailability').disabled;
	var i=0;
	var toolbar = gToolbars[toolbarKey];

	for (var key in toolbar){
		var data = toolbar[key];
		var anchor = document.getElementById('toolbarAction' + toolbarKey + key);
		var href = data.href;
		
		if (!empty(anchor)){
			for (var paramname in params){
				var paramvalue = params[paramname];
				href +="&"+paramname+"="+paramvalue;
			}
		
			anchor.href = cpath +"/"+href;
			if (enableList)
				enableToolbarItem(key, enableList[i]);
			else
				enableToolbarItem(key, true);
			if(key == 'Edit')
				enableToolbarItem(key, true);
			else if(key == 'Delete'){
				if(checkbox == true)
					enableToolbarItem(key, false);
				else
					enableToolbarItem(key, true);	
			}else enableToolbarItem(key, true);
		}else {
			debug("No anchor  for "+ 'toolbarAction'+key + ":");
		}
		i++;
	}
	
	

}

	

		
