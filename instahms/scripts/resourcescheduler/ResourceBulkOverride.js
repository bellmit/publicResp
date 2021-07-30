function initDialog() {
	for(var i=0;i<7;i++) {
		initResourceTimingsDialog(i);
	}
}

var gIndex;
var resourceTimingDialog = new Array();
var gRowObj;
var lastVisibleIndex = 0;
var centerId = 0;
addedit = 'addbulk';

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

/* *
* validation for checking if few resources (Doctor) selected are not available 
* for online consultation and enable/disable visit mode accordingly
* */
function validateResources(selectedList, index){
	var docList = doctorJSON.filter(a => selectedList.includes(a.doctor_id));
	var notAvailableResourceList = docList.filter((doc) => doc.available_for_online_consults === "N");
	var mode = docList.length ? mode = docList[0].available_for_online_consults != "Y" : true;
	if(notAvailableResourceList.length && notAvailableResourceList.length !== docList.length )
		mode = true;
	document.getElementById('dialog_visit_mode'+index).disabled = mode;
	if (disableBulkResourceFlag) {
		$("#res_sch_name").prop('disabled',true).trigger("chosen:updated").chosen({ width: "410px", height: "18px" });
		document.getElementById('select_all').disabled = true;
	}
}

function closeResourceTimingsDialog() {
	clearDialog(gIndex);
	resourceTimingDialog[gIndex].hide();
}
function openResourceTimingsDialog(index) {
	var res_sch_type = document.ResourceAvailabilityForm.res_sch_type.value;
	var res_sch_name = document.ResourceAvailabilityForm.res_sch_name.value;
	var resourceName = document.getElementById('res_sch_name');
	var selectedResources = [...resourceName.options]
		.filter(option => option.selected)
		.map(option => option.value);

	if (empty(res_sch_type)) {
		showMessage("js.scheduler.resourceavailability.resourcetype.required");
		document.ResourceAvailabilityForm.res_sch_type.focus();
		return;
	}

	if (empty(res_sch_name)) {
		showMessage("js.scheduler.resourceavailability.resourcename.required");
		document.ResourceAvailabilityForm.res_sch_name.focus();
		return;
	}

	if (!empty(res_sch_type) && !empty(resourceName))
		getResourceTimings(res_sch_type,resourceName,index);


	button = document.getElementById("btnAddItem"+index);
	resourceTimingDialog[index].cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('resourcedialogheader'+index).innerHTML = getString('js.scheduler.resourceavailability.details');
	document.getElementById('dialog_from_time'+index).disabled = false;
	document.getElementById('dialog_to_time'+index).disabled = false;
	if (res_sch_type == 'DOC' ){
		validateResources(selectedResources, index);

	}
	resourceTimingDialog[index].show();
	document.getElementById('dialog_from_time'+index).focus();
	gIndex = index;
	edited = false;
	addEdit='addbulk';
}


function getResourceTimings(resourceType,schedulerName,index) {
	var screenId = "resourceAvailability";

	if (!empty(resourceType) && !empty(schedulerName)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = cpath+'/master/resourceschedulers/getResourceTimingsByDuration.htm?resource_type='+resourceType+'&scheduler_name=*&screenId='+screenId;
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


function cancelDialog(index) {
	clearDialog(index);
	resourceTimingDialog[index].cancel();
}

function centerChanged(obj){
	loadResources();
}

function loadResources(obj) {
	//Below code is , on select of resource type submit the page.
	var category = document.forms[0].res_sch_type.value;
	var selectCenterId = document.forms[0].select_center_id.value;
	var categoryName = document.forms[0].res_sch_name.value;
	if (empty(category) && empty(selectCenterId))
		window.location.href = "../../master/resourceoverrides/addbulk.htm?method=addbulk&res_sch_type="+''+"&res_sch_name="+''+"&center_id=";
	else
		window.location.href = "../../master/resourceoverrides/addbulk.htm?method=addbulk&res_sch_type="+category+"&res_sch_name=&center_id="+selectCenterId;

	//Below code is, based on selection of resource type getting auto complete resource name
	var resourceType = category;
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
			populateMultiSelect(list);
		}
	}
}



function populateMultiSelect(list){
	$html = '<select name="res_sch_name" id="res_sch_name" multiple="multiple" size="1" class="chosenElement">';
	for (var i = 0; i < list.length; i++) {
		$html += '<option value='+list[i].resource_id+'>'+list[i].resource_name+'</option>';
	}
	$('.chosen-mutli-select').html($html);
	$('.chosenElement').chosen({ width: "410px", height: "18px" });
	$(".chosen-search-input").val("Select Resources");
}

function selectAllOptions(obj) {
	if(obj.checked){
		$('.chosen-mutli-select option').prop('selected', true).trigger('chosen:updated');
	} else {
		$('.chosen-mutli-select option').prop('selected', false).trigger('chosen:updated');
	}
	
}


var resId='';
function init(screen)	{
	resId=resourceName;
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
				populateMultiSelect(list);
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

var edited = false;
function openEditResTimingsDialogBox(obj,iIndex) {
	var rowObj = findAncestor(obj,"TR");
	var table = document.getElementById('resultTable'+iIndex);
	gRowObj = table.rows[rowObj.rowIndex];
	var index = getRowItemIndex(rowObj);

	var res_sch_type = document.ResourceAvailabilityForm.res_sch_type.value;
	var resourceName = document.ResourceAvailabilityForm.res_sch_name.value;
	var resourceNameOptions = document.getElementById('res_sch_name');
	var selectedResources = [...resourceNameOptions.options]
		.filter(option => option.selected)
		.map(option => option.value);
	getResourceTimings(res_sch_type,resourceName,iIndex);

	rowObj.className = 'selectedRow';
	document.getElementById('resourcedialogheader'+index).innerHTML = getString('js.scheduler.resourceavailability.editdetails');
	document.getElementById('dialogId'+iIndex).value = iIndex;
	updateGridToDialog(obj,rowObj,iIndex);
	document.getElementById('dialog_from_time'+index).focus();
	gIndex = iIndex;
	edited = false;
	addEdit='edit';
	if (resourceType == 'DOC' ){
		validateResources(selectedResources, index);
	}
}



function addReplica() {
	lastVisibleIndex++;
	document.getElementById('DayResult'+lastVisibleIndex).style.display = '';
	
}

function removeReplica() {
	deleteAll(lastVisibleIndex);
	//document.getElementById('DayResult'+lastVisibleIndex).style.display = 'none';
	lastVisibleIndex--;
	
}



function deleteAll(index) {
	var table = document.getElementById("resultTable"+index);
	var tableLength  = table.rows.length;
	for (var i=1;i<tableLength-1;i++){
		table.deleteRow(i);
	}
	insertRowValues(table,index,"N","00:00","23:59","","","","");
}

function validatebulk(){
	var resourceType = document.getElementById('res_sch_type');
	var resourceName = document.getElementById('res_sch_name');
	var b_from_date = document.getElementById('from_date');
	var b_to_date = document.getElementById('to_date');
	
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
	var date = Date.parse(in_to_date_arr[2]+"-"+in_to_date_arr[1]+"-"+in_to_date_arr[0]);
 	var date1 = Date.parse(in_from_date_arr[2]+"-"+in_from_date_arr[1]+"-"+in_from_date_arr[0]);
 	var timeDiff = date - date1;
  var daysDiff = Math.floor(timeDiff / (1000 * 60 * 60 * 24));
  var now = new Date();
  var todaysIndex = now.getDay();


	if (long_to_date < long_from_date) {
		showMessage("js.scheduler.resourceavailability.fromdate.greaterthan.todate");
		b_to_date.focus();
		return false;
	}
	
	var daySelected = false;
	var maxDayIndexOfWeek = -1;
	for (var i=0;i<7;i++){
		for (var j=0;j<7;j++){
			var elements = document.getElementsByClassName(i+'day'+j);
			var requiredElement = elements[0];
			if (requiredElement.checked){
				daySelected = true;
				maxDayIndexOfWeek = requiredElement.value;
			}
		}
	}
	
	if (!daySelected){
		showMessage("js.scheduler.resourceavailability.no.day.selected");
		return false;
	}

	if(daySelected) {
	  if(daysDiff !== 0 && todaysIndex + daysDiff <  Number(maxDayIndexOfWeek)) {
	    alert("Selected day does not fall under the date range" + " " + b_to_date.value + " " + b_from_date.value);
    	return false;
	  }
	  if(daysDiff == 0) {
	    var indexOfSelectedDateRange = new Date(date).getDay();
	    if(indexOfSelectedDateRange !== Number(maxDayIndexOfWeek)) {
	      alert("Selected day does not fall under the date range" + " " + b_to_date.value + " " + b_from_date.value);
        return false;
	    }
	  }
	}

	var overrideExisting = document.getElementById("override_existing").checked;
	
	if(!overrideExisting && checkOverrideExists()){
		showMessage("js.scheduler.resourceavailability.override.exists");
		return false;
	}
	document.getElementById('res_sch_name').disabled = false;
	return true;
	
}

function checkOverrideExists() {
	var start = document.forms[0].from_date.value;
	var end = document.forms[0].to_date.value;
	if(resourceType == 'DOC')
		$('#res_sch_name').prop('disabled',false);
	var resSchName = $('#res_sch_name').chosen().val();
	if (!empty(resourceType) && !empty(schedulerName)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = cpath+'/master/resourceoverrides/overridesexist.json?';
		url = url + "from_date=" + start;
		url = url + "&to_date=" + end;
		url = url + "&res_sch_name=" + resSchName;
		var reqObject = new XMLHttpRequest();
		reqObject.open("GET", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				eval("var existsOverride = " + reqObject.responseText);
				return existsOverride.exists;
			}
		}
	}
}

// function to disable or enable day of week(dow) from override cards 
function dowChecked(obj){
	var day = obj.value;
	var isChecked = obj.checked;
	var index = obj.name.substring(3, 4);
	for (var i=0;i<7;i++){
		if (i==index){
			continue;
		} else {
			var elements = document.getElementsByClassName(i+'day'+day);
			var requiredElement = elements[0];
			requiredElement.disabled = isChecked ? true : false;
		}
	}
	
}

	
