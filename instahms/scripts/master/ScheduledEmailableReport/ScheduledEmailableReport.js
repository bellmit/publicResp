var acFavReports = null;
var acBuiltinReports = null;
var acCustomReports = null;

var mainForm;
var dialogForm;

var theDialog = null;

var reportTypesDisplay = {
	"B" : "Builtin",
	"F" : "Favourite",
	"C" : "Custom"
};

function init() {

	mainForm = document.mainForm;
	dialogForm = document.dialogForm;

	initFavReportsAutoComplete();
	initBuiltinReportsAutoComplete();
	initCustomReportsAutoComplete();

	initDialog();
}

function initDialog() {
	document.getElementById("dialog").style.display = 'block';

	theDialog = new YAHOO.widget.Dialog("dialog",
							{ width : "30em",
							  visible : false,
							  constraintoviewport : true,
							  buttons : [ { text:"Save", handler:handleSubmit, isDefault: true},
								      { text:"Cancel", handler:handleCancel } ]
							});

	theDialog.validate = function() {

		var data = this.getData();
		if (trim(data.report_id) == '') {
			alert("Report Name is required.");
			return false;
		}
		if (data.frequency == '') {
			alert("Frequency is required.");
			return false;
		}
		if (data.subevent == '' && data.category!='y') {
			alert("Period is required");
			return false;
		}
		if (data.email_id == '') {
			alert("Email ID is required.");
			return false;
		}
		return true;
	};

	// Render the Dialog
	theDialog.render();
	addListenerForAllButtons(theDialog);
}

function initFavReportsAutoComplete() {
	if (acFavReports != null) {
		acFavReports.destroy();
	}
	acFavReports = initAutoComplete('favReportName', 'favReportName_dropdown', favReports);
}

function initBuiltinReportsAutoComplete() {
	if (acBuiltinReports != null) {
		acBuiltinReports.destroy();
	}
	acBuiltinReports = initAutoComplete('builtinReportName', 'builtinReportName_dropdown', builtinReports);
}

function initCustomReportsAutoComplete() {
	if (acCustomReports != null) {
		acCustomReports.destroy();
	}
	acCustomReports = initAutoComplete('customReportName', 'customReportName_dropdown', customReports);
}

function initAutoComplete(inputName, dropdownName, dataSet) {

	var datasource = new YAHOO.util.LocalDataSource({result : dataSet});
	datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	datasource.responseSchema = { resultsList : 'result',
			  fields : [{key : 'report_name'}, {key : 'report_id'} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete(inputName, dropdownName, datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.useShadow = true;
	autoComp.minQueryLength = 0;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.resultTypeList = false;
	autoComp.forceSelection = true;
	autoComp.maxResultsDisplayed = 50;

	// set the report_id based on the selection
	autoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			dialogForm.report_id.value = elItem[2].report_id;
	});

	autoComp.selectionEnforceEvent.subscribe(function(){
			dialogForm.report_id.value = '';
	});

	return autoComp;
}

function loadDialogFromRow(rowId) {

	var docId = document.getElementById("hReportId"+rowId).value;
	dialogForm.invokedFor.value = rowId;

	if (empty(docId)) {
		// New addition
		dialogForm.report_type.disabled = false;
		dialogForm.favReportName.disabled = false;
		dialogForm.builtinReportName.disabled = false;
		dialogForm.customReportName.disabled = false;

		setSelectedIndex(dialogForm.report_type, 'F');
		dialogForm.favReportName.disabled = false;
		dialogForm.favReportName.value = '';
		dialogForm.builtinReportName.value = '';
		dialogForm.customReportName.value = '';
		dialogForm.report_id.value = '';
		setSelectedIndex(dialogForm.frequency, 'Daily');
		populatePeriod();
		dialogForm.params.value = '';
		setSelectedIndex(dialogForm.outputMode, 'pdf');
		dialogForm.email_id.value = '';

		populateReports();

	} else {
		// edit
		var reportType = document.getElementById("hReportType" + rowId).value;
		var reportId = document.getElementById("hReportId" + rowId).value;
		var reportName = document.getElementById("hReportName" + rowId).value;
		var triggerEnum = document.getElementById("hTriggerEnum" + rowId).value;
		var subevent = document.getElementById("hSubEvent" + rowId).value;
		var params = document.getElementById("hParams" + rowId).value;
		var mode = document.getElementById("hOutputMode" + rowId).value;
		var emailId = document.getElementById("hEmailId" + rowId).value;

		dialogForm.report_type.disabled = true;

		setReportEditName(reportName);
		dialogForm.report_type.value = reportType;
		dialogForm.report_id.value = reportId;
		dialogForm.frequency.value = triggerEnum;
		populatePeriod();
		dialogForm.subevent.value = subevent;
		dialogForm.params.value = params;
		dialogForm.outputMode.value = mode;
		dialogForm.email_id.value = emailId;
	}
}

var handleSubmit = function() {
	if (!this.validate())
		return false;

	var data = this.getData();
	var rowId = data.invokedFor;
	var category = data.category;

	// display labels
	setElementText("_l_ReportName" + rowId, getReportName(data));
	setElementText("_l_ReportType" + rowId, reportTypesDisplay[data.report_type]);
	setElementText("_l_TriggerEnum" + rowId, data.frequency);
	setElementText("_l_SubEvent" + rowId, data.subevent==''?'Report Default':data.subevent);
	setElementText("_l_params" + rowId, data.params);
	setElementText("_l_OutputMode" + rowId,  data.outputMode);
	setElementText("_l_emailId" + rowId, data.email_id);

	// form values
	document.getElementById("hReportType"+ rowId).value = data.report_type;
	document.getElementById("hReportId"+ rowId).value = data.report_id;
	document.getElementById("hReportName"+ rowId).value = getReportName(data)
	document.getElementById("hTriggerEnum"+ rowId).value = data.frequency;
	document.getElementById("hSubEvent"+ rowId).value = data.subevent;
	document.getElementById("hParams"+ rowId).value = data.params;
	document.getElementById("hOutputMode"+ rowId).value = data.outputMode;
	document.getElementById("hEmailId"+ rowId).value = data.email_id;

	while(document.getElementById('flgDiv'+rowId).hasChildNodes()) {
		document.getElementById('flgDiv'+rowId).removeChild(document.getElementById('flgDiv'+rowId).firstChild);
	}

	var imgEle = document.createElement('img');
	imgEle.src = cpath+"/images/yellow_flag.gif";
	document.getElementById('flgDiv'+rowId).appendChild(imgEle);
	document.getElementById('flgDiv'+rowId).style.display = 'block';
	var nextEl = document.getElementById('hDocId'+ (new Number(rowId)+1));
	if (empty(nextEl)) {
		addRow((new Number(rowId)+1));
	}
	addListenerForAllButtons();
	theDialog.hide();
};

var handleCancel = function() {
	theDialog.hide();
};


var addRow = function(index) {

	var tab = document.getElementById("dataTable");
	var row = tab.insertRow(-1);

	var cell = row.insertCell(-1);
	cell.innerHTML = '<input type="checkbox" name="deleteReport" id="deleteReport'+ index +'" onclick="return checkForDelete(this.checked, \'delete'+ index +'\');">';

	var cell = row.insertCell(-1);
	cell.innerHTML = '<div class="w15" id="flgDiv'+index+'" style=""><img src="'+cpath+'/images/yellow_flag.gif"/></div><label id="_l_ReportName'+ index +'"></label>';

	cell = row.insertCell(-1);
	cell.innerHTML = '<label id="_l_ReportType'+ index +'"></label>';

	cell = row.insertCell(-1);
	cell.innerHTML = '<label id="_l_TriggerEnum'+ index +'"></label>';

	cell = row.insertCell(-1);
	cell.innerHTML = '<label id="_l_SubEvent'+ index +'"></label>';

	cell = row.insertCell(-1);
	cell.innerHTML = '<label id="_l_params'+ index +'"></label>';

	cell = row.insertCell(-1);
	cell.innerHTML = '<label id="_l_OutputMode'+ index +'"></label>';

	cell = row.insertCell(-1);
	cell.innerHTML = '<label id="_l_emailId'+ index +'"></label>';

	cell = row.insertCell(-1);
	var button = document.createElement("input");
	button.setAttribute('type', 'button');
	button.setAttribute('id', 'addOredit');
	button.setAttribute('name', 'addOredit');
	button.setAttribute('rowId', index);
	button.setAttribute('value', '#');
	cell.appendChild(button);

	cell.appendChild(makeHidden("doc_id", "hDocId"+index, ""));
	cell.appendChild(makeHidden("report_type", "hReportType"+index, ""));
	cell.appendChild(makeHidden("report_id", "hReportId"+index, ""));
	cell.appendChild(makeHidden("report_name", "hReportName"+index, ""));
	cell.appendChild(makeHidden("trigger_enum", "hTriggerEnum"+index, ""));
	cell.appendChild(makeHidden("subevent", "hSubEvent"+index, ""));
	cell.appendChild(makeHidden("params", "hParams"+index, ""));
	cell.appendChild(makeHidden("output_mode", "hOutputMode"+index, ""));
	cell.appendChild(makeHidden("email_id", "hEmailId"+index, ""));
	cell.appendChild(makeHidden("delete", "delete"+index, "N"));
}

function addListenerForAllButtons() {
	// get all buttons with the name addOrEdit
	var addOreditEls = document.getElementsByName("addOredit");
	// add listener for opening the dialog to all these buttons
	YAHOO.util.Event.addListener(addOreditEls, "click", function() { showDialog(theDialog); },
		theDialog, true);
}

function getReportName() {
	var category = document.dialogForm.report_type.value;
	if (category == 'F') {
		return document.dialogForm.favReportName.value;
		nameField = 'reportName';
	} else if (category == 'B') {
		return document.dialogForm.builtinReportName.value;
	} else if (category == 'C') {
		return document.dialogForm.customReportName.value;
	}
}

function updateRowOnEnter(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);

	if (charCode == 13) {
		 var buttonGroup = YAHOO.util.Dom.getElementsByClassName("default");
		 buttonGroup[0].click();
	}
	return true;

}

function showDialog(theDialog) {
	if (theDialog.visible) {
		theDialog.hide();
		return;
	}

	var corner;
	var pos = 'left';
	if ((pos!=null) && (pos=='left')) {
		corner = "tr";
	} else {
		corner = "tl";
	}
	var contextEl = YAHOO.util.Event.getTarget(YAHOO.util.Event.getEvent());
	var rowId = contextEl.getAttribute("rowId");

	loadDialogFromRow(rowId);

	var contextConfig = [contextEl, corner, "br"];
	theDialog.cfg.setProperty("context", contextConfig);
	theDialog.cfg.queueProperty("context", contextConfig);

	theDialog.show();
}

function empty(obj) {
	if (obj == null || obj == undefined || obj == '') return true;
	else return false;
}


function checkForDelete(isChecked, objId) {
	if (isChecked) document.getElementById(objId).value = 'Y';
	else document.getElementById(objId).value = 'N';
}

function saveValues() {
	mainForm.submit();
	return true;
}

function populatePeriod() {
	var frequency = dialogForm.frequency.value;
	for (var ev in events) {
		if (ev == frequency) {
			var selEl = document.dialogForm.subevent;
			selEl.length = 0;
			var len = 0;
			if(dialogForm.report_type.value=='F') {
			 	selEl.length = len + 1;
			 	selEl.options[len].value = 'Report Default';
			 	selEl.options[len].text =  'Report Default';
			 	len++;
			}
			var periodList = events[ev];
			for (var period in periodList) {
				selEl.length = len + 1;
				selEl.options[len].value = periodList[period];
				selEl.options[len].text = periodList[period];
				len++;
			}
		}
	}
}

function populateReports() {

	var reportType = dialogForm.report_type.value;

	displayElementId('favReportNameDiv', (reportType == 'F'));
	displayElementId('customReportNameDiv', (reportType == 'C'));
	displayElementId('builtinReportNameDiv', (reportType == 'B'));
	displayElementId('reportNameEditDiv', false);

	if (reportType == 'F') {
		// srxml - params not applicable, but output mode is
		document.dialogForm.outputMode.disabled = false;
		document.dialogForm.params.disabled = true;
	} else {
		// built-in reports, custom: output mode NA, but params i
		document.dialogForm.outputMode.disabled = true;
		document.dialogForm.params.disabled = false;
	}
}

function setReportEditName(name) {
	displayElementId('favReportNameDiv', false);
	displayElementId('customReportNameDiv', false);
	displayElementId('builtinReportNameDiv', false);

	dialogForm.favReportName.value = name;
	dialogForm.builtinReportName.value = name;
	dialogForm.customReportName.value = name;

	displayElementId('reportNameEditDiv', true);
	document.getElementById('reportNameEditDiv').innerHTML = '<b>' + name + '</b>';
}

