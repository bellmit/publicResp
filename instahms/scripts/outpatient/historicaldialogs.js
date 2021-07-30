function initHistoricalDialogs() {
	initPreviousVisitSummaryDialog();
	initDiagnosisHistoryDialog();
	initVitalsHistoryDialog();
	initConsultationNotesHistoryDialog();
	initConsultationImagesHistoryDialog();
	initPrescriptionsHistoryDialog();
}

function initPreviousVisitSummaryDialog() {
	var dialogDiv = document.getElementById("previousVisitSummaryDiv");
	dialogDiv.style.display = 'block';
	previousVisitSummaryDialog = new YAHOO.widget.Dialog("previousVisitSummaryDiv",
			{	width:"960px",
				context : ["viewHistory", "tr", "br", ["beforeShow", "windowResize"]],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('pvdCloseBtn', 'click', previousVisitSummaryDialog.cancel, previousVisitSummaryDialog, true);
	subscribeEscKeyListener(previousVisitSummaryDialog);
	previousVisitSummaryDialog.render();
}

function initDiagnosisHistoryDialog() {
	var dialogDiv = document.getElementById("diagnosisHistoryDiv");
	dialogDiv.style.display = 'block';
	diagnosisHistoryDialog = new YAHOO.widget.Dialog("diagnosisHistoryDiv",
			{	width:"600px",
				context : ["viewHistory", "tr", "br", ["beforeShow", "windowResize"]],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('dhdCloseBtn', 'click', diagnosisHistoryDialog.cancel, diagnosisHistoryDialog, true);
	subscribeEscKeyListener(diagnosisHistoryDialog);
	diagnosisHistoryDialog.render();
}

function initVitalsHistoryDialog() {
	var dialogDiv = document.getElementById("vitalsHistoryDiv");
	dialogDiv.style.display = 'block';
	vitalsHistoryDialog = new YAHOO.widget.Dialog("vitalsHistoryDiv",
			{	width:"600px",
				context : ["viewHistory", "tr", "br", ["beforeShow", "windowResize"]],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('vhdCloseBtn', 'click', vitalsHistoryDialog.cancel, vitalsHistoryDialog, true);
	subscribeEscKeyListener(vitalsHistoryDialog);
	vitalsHistoryDialog.render();
}

function initConsultationNotesHistoryDialog() {
	var dialogDiv = document.getElementById("consultationNotesHistoryDiv");
	dialogDiv.style.display = 'block';
	consultationNotesHistoryDialog = new YAHOO.widget.Dialog("consultationNotesHistoryDiv",
			{	width:"600px",
				context : ["viewHistory", "tr", "br", ["beforeShow", "windowResize"]],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('cnhdCloseBtn', 'click', consultationNotesHistoryDialog.cancel, consultationNotesHistoryDialog, true);
	subscribeEscKeyListener(consultationNotesHistoryDialog);
	consultationNotesHistoryDialog.render();
}

function initConsultationImagesHistoryDialog() {
	var dialogDiv = document.getElementById("consultationImagesHistoryDiv");
	dialogDiv.style.display = 'block';
	consultationImagesHistoryDialog = new YAHOO.widget.Dialog("consultationImagesHistoryDiv",
			{	width:"600px",
				context : ["viewHistory", "tr", "br", ["beforeShow", "windowResize"]],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('cihdCloseBtn', 'click', consultationImagesHistoryDialog.cancel, consultationImagesHistoryDialog, true);
	subscribeEscKeyListener(consultationImagesHistoryDialog);
	consultationImagesHistoryDialog.render();
}

function initPrescriptionsHistoryDialog() {
	var dialogDiv = document.getElementById("prescriptionsHistoryDiv");
	dialogDiv.style.display = 'block';
	prescriptionsHistoryDialog = new YAHOO.widget.Dialog("prescriptionsHistoryDiv",
			{	width:"600px",
				context : ["viewHistory", "tr", "br", ["beforeShow", "windowResize"]],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('phdCloseBtn', 'click', prescriptionsHistoryDialog.cancel, prescriptionsHistoryDialog, true);
	subscribeEscKeyListener(prescriptionsHistoryDialog);
	prescriptionsHistoryDialog.render();
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

function onChangeViewHistory() {
	var viewHistory = document.getElementById('viewHistory').value;
	var consultationId = document.getElementById('consultation_id').value;
	var visitType = document.getElementById('visitType').value;
	if (viewHistory == 'previous_visit_summary') {
		previousVisitSummaryDialog.show();
	} else if (viewHistory == 'historical_diagnosis') {
		var url = cpath + "/outpatient/OpPrescribeAction.do?_method=getDiagnosisHistory&consultation_id="+consultationId + "&visit_type="+visitType;
		YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: populateDiagnosisHistoryDialog,
				failure: failedToLoadDiagnosisHistory,
			});
	} else if (viewHistory == 'historical_vitals') {
		var url = cpath + "/outpatient/OpPrescribeAction.do?_method=getVitalsHistory&consultation_id="+consultationId + "&visit_type="+visitType;
		YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: populateVitalsHistoryDialog,
				failure: failedToLoadVitalsHistory,
			});

	} else if (viewHistory == 'historical_notes') {
		var url = cpath + "/outpatient/OpPrescribeAction.do?_method=getConsultationNotesHistory&consultation_id="+consultationId + "&visit_type="+visitType;
		YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: populateConsultationNotesHistoryDialog,
				failure: failedToLoadConsultationNotesHistory,
			});
	} else if (viewHistory == 'historical_images') {
		var url = cpath + "/outpatient/OpPrescribeAction.do?_method=getImagesHistory&consultation_id="+consultationId + "&visit_type="+visitType;
		YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: populateConsultationImagesHistoryDialog,
				failure: failedToLoadConsultationImagesHistory,
			});
	}else if (viewHistory == 'historical_prescriptions') {
		var table = document.getElementById("prescriptionsHistoryTable");
		
		if (table.rows.length > 3) {
			prescriptionsHistoryDialog.show();
		} else {
			var url = cpath + "/outpatient/OpPrescribeAction.do?_method=getPrescriptionsHistory&consultation_id="+consultationId + "&visit_type="+visitType;
			YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: populatePrescriptionsHistoryDialog,
					failure: failedToLoadPrescriptionsHistory,
				});
		}
	}
	// resetting the view history.
	document.getElementById('viewHistory').value = '';
}

var reportsFetched = false;
function loadReports() {
	var reportsDropDown = document.getElementById('viewReports');
	var patientId = document.getElementById('patient_id').value;
	var ajaxReqObject = new XMLHttpRequest();
	var url = cpath+"/outpatient/OpPrescribeAction.do?_method=getReports&patient_id="+patientId;
	ajaxReqObject.onreadystatechange = function() {
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("var allReports="+ajaxReqObject.responseText);
				var dropdownIndex = 1;
				for (var key in allReports) {
					for (var i=0; i< allReports[key].length; i++) {
						var report = allReports[key][0];
						reportsDropDown.options[dropdownIndex++] = new Option(report.title, report.displayUrl);
						reportsFetched = true;
					} 
				}
			}
		}
	}
	ajaxReqObject.open("GET", url.toString(), true);
	ajaxReqObject.send(null);
}

function onChangeViewReports() {
	var viewReport = document.getElementById('viewReports').value;
	if (viewReport != '') {
		window.open(cpath + viewReport);
	}
	alert(viewReport);
}
function populateDiagnosisHistoryDialog(response) {
	if (response.responseText != undefined) {
		var diagnosisHistory = eval('(' + response.responseText + ')');
		var table = document.getElementById("diagnosisHistoryTable");
		var patientId = null;
		for (var i=0; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}

		for (var i=0; i<diagnosisHistory.length; i++) {
			if (patientId != diagnosisHistory[i].visit_id) {
				var patientDetailsTemplateRow = table.rows[table.rows.length-2];
				var patientDetailsRow = patientDetailsTemplateRow.cloneNode(true);
				patientDetailsRow.style.display = '';
				table.tBodies[0].insertBefore(patientDetailsRow, patientDetailsTemplateRow);

				var diagnosis_patient_id = diagnosisHistory[i].visit_id;
				var diagnosis_visited_date = diagnosisHistory[i].reg_date ?
					formatDate(new Date(diagnosisHistory[i].reg_date), 'ddmmyyyy', '-') : '';
				setNodeText(patientDetailsRow.cells[0], diagnosis_patient_id);
				setNodeText(patientDetailsRow.cells[1], diagnosis_visited_date);
			}

			var patientDetailsTemplateRow = table.rows[table.rows.length-2];
			var templateRow = table.rows[table.rows.length-1];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, patientDetailsTemplateRow);

			setNodeText(row.cells[0],
				diagnosisHistory[i].diag_type == 'P' ? 'Principal Diagnosis' : (diagnosisHistory[i].diag_type == 'V' ? 'Reason For Visit' : 'Secondary Diagnosis'));
			setNodeText(row.cells[1], diagnosisHistory[i].description);
   			setNodeText(row.cells[3], diagnosisHistory[i].icd_code);
   			patientId = diagnosisHistory[i].visit_id;
		}
		diagnosisHistoryDialog.show();
	}
}

function failedToLoadDiagnosisHistory() {

}

function failedToLoadVitalsHistory() {

}
function failedToLoadPrescriptionsHistory() {

}

function failedToLoadConsultationNotesHistory() {

}

function failedToLoadConsultationImagesHistory() {

}

function populateVitalsHistoryDialog(response) {
	if (response.responseText != undefined) {
		var vitalsHistory = eval('(' + response.responseText + ')');
		var table = document.getElementById("vitalsHistoryTable");
		var vital_reading_id = null;
		for (var i=0; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		var id = null;
		var templateRow = null;
		var row = null;
		var j = null;
		var k = 0;
		for (var i=0; i<vitalsHistory.length; i++) {
			var displayInNewRow = false;
			if (vital_reading_id != vitalsHistory[i].vital_reading_id) {
				var patientDetailsTemplateRow = table.rows[table.rows.length-2];
				var patientDetailsRow = patientDetailsTemplateRow.cloneNode(true);
				patientDetailsRow.style.display = '';
				table.tBodies[0].insertBefore(patientDetailsRow, patientDetailsTemplateRow);

				var vital_patientId = vitalsHistory[i].patient_id;
				var visited_date = vitalsHistory[i].reg_date ?
					formatDate(new Date(vitalsHistory[i].reg_date), 'ddmmyyyy', '-') : '';
				setNodeText(patientDetailsRow.cells[0], vital_patientId);
				setNodeText(patientDetailsRow.cells[1], visited_date);
				displayInNewRow = true;

			}

			if (k%2 == 0 || displayInNewRow) {
				var patientDetailsTemplateRow = table.rows[table.rows.length-2];
				templateRow = table.rows[table.rows.length-1];
				row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, patientDetailsTemplateRow);
				j=0;
			}
			setNodeText(row.cells[j++], vitalsHistory[i].param_label + ": ");
			setNodeText(row.cells[j++], vitalsHistory[i].param_value + " " +vitalsHistory[i].param_uom);

			vital_reading_id = vitalsHistory[i].vital_reading_id;

			if (k!=0 && displayInNewRow) {

			} else {
				k++;
			}
		}
		vitalsHistoryDialog.show();
	}
}


function populatePrescriptionsHistoryDialog(response) {
	var prescriptionsHistory = eval('(' + response.responseText + ')');
	var consultationId = null;
	var table = document.getElementById("prescriptionsHistoryTable");
	
	for (var i=0; i<prescriptionsHistory.length; i++) {
		if (consultationId != prescriptionsHistory[i].consultation_id) {
			var patientDetailsTemplateRow = table.rows[table.rows.length-2];
			var patientDetailsRow = patientDetailsTemplateRow.cloneNode(true);
			patientDetailsRow.style.display = '';
			table.tBodies[0].insertBefore(patientDetailsRow, patientDetailsTemplateRow);

			var prescription_patientId = prescriptionsHistory[i].patient_id;
			var visited_date = prescriptionsHistory[i].visited_date ?
				formatDate(new Date(prescriptionsHistory[i].visited_date), 'ddmmyyyy', '-') : '';
			setNodeText(patientDetailsRow.cells[0], prescription_patientId);
			setNodeText(patientDetailsRow.cells[1], visited_date);
		}

		var patientDetailsTemplateRow = table.rows[table.rows.length-2];
		var templateRow = table.rows[table.rows.length-1];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, patientDetailsTemplateRow);

		var itemType = prescriptionsHistory[i].item_type;
		setNodeText(row.cells[0], itemType, 15);
		setNodeText(row.cells[1], prescriptionsHistory[i].item_name, 20);

		if (!empty(prescriptionsHistory[i].frequency) || !empty(prescriptionsHistory[i].duration)) {
			setNodeText(row.cells[2], itemType == 'Medicine' ?
				(prescriptionsHistory[i].frequency + " / " + prescriptionsHistory[i].duration + ' ' + prescriptionsHistory[i].duration_units) : '', 15);
		}
		setNodeText(row.cells[3], itemType == 'Medicine' ? prescriptionsHistory[i].medicine_quantity : '', 5);
		setNodeText(row.cells[4], prescriptionsHistory[i].item_remarks, 30);

		consultationId = prescriptionsHistory[i].consultation_id;
	}
	prescriptionsHistoryDialog.show();

}

function populateConsultationNotesHistoryDialog(response) {
	var consultationNotesHistory = eval('(' + response.responseText + ')');
	var table = document.getElementById("consultNotesTable");
	var consultationId = null;
	for (var i=0; i<table.rows.length-2; ) {
		table.deleteRow(i);
	}
	for (var i=0; i<consultationNotesHistory.length; i++) {
		if (consultationId != consultationNotesHistory[i].consultation_id) {
			var patientDetailsTemplateRow = table.rows[table.rows.length-2];
			var patientDetailsRow = patientDetailsTemplateRow.cloneNode(true);
			patientDetailsRow.style.display = '';
			table.tBodies[0].insertBefore(patientDetailsRow, patientDetailsTemplateRow);

			var consult_notes_patientId = consultationNotesHistory[i].patient_id;
			var visited_date = consultationNotesHistory[i].visited_date ?
				formatDate(new Date(consultationNotesHistory[i].visited_date), 'ddmmyyyy', '-') : '';
			setNodeText(patientDetailsRow.cells[0], consult_notes_patientId);
			setNodeText(patientDetailsRow.cells[1], visited_date);
		}

		var patientDetailsTemplateRow = table.rows[table.rows.length-2];
		var templateRow = table.rows[table.rows.length-1];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, patientDetailsTemplateRow);

		setNodeText(row.cells[0], consultationNotesHistory[i].field_name + ": ");
		setNodeText(row.cells[1], consultationNotesHistory[i].field_value);

		consultationId = consultationNotesHistory[i].consultation_id;
	}
	consultationNotesHistoryDialog.show();
}

function populateConsultationImagesHistoryDialog(response) {
	var consultationImagesHistory = eval('(' + response.responseText + ')');
	var table = document.getElementById("consultationImagesHistoryTable");
	var consultationId = null;
	for (var i=1; i<table.rows.length-2; ) {
		table.deleteRow(i);
	}
	for (var i=0; i<consultationImagesHistory.length; i++) {
		if (consultationId != consultationImagesHistory[i].consultation_id) {
			var patientDetailsTemplateRow = table.rows[table.rows.length-2];
			var patientDetailsRow = patientDetailsTemplateRow.cloneNode(true);
			patientDetailsRow.style.display = '';
			table.tBodies[0].insertBefore(patientDetailsRow, patientDetailsTemplateRow);

			var consult_images_patientId = consultationImagesHistory[i].patient_id;
			var visited_date = consultationImagesHistory[i].visited_date ?
				formatDate(new Date(consultationImagesHistory[i].visited_date), 'ddmmyyyy', '-') : '';
			setNodeText(patientDetailsRow.cells[0], consult_images_patientId);
			setNodeText(patientDetailsRow.cells[1], visited_date);
		}

		var patientDetailsTemplateRow = table.rows[table.rows.length-2];
		var templateRow = table.rows[table.rows.length-1];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, patientDetailsTemplateRow);

		setNodeText(row.cells[2], consultationImagesHistory[i].datetime);
		document.getElementsByName("History_dialog_viewImage")[i].setAttribute("onclick",
			'displayNote('+ consultationImagesHistory[i].image_id +', "History_dialog_");');

		consultationId = consultationImagesHistory[i].consultation_id;
	}
	consultationImagesHistoryDialog.show();
}

function getNumItems(exceptionalRows, tableId) {
	return document.getElementById(tableId).rows.length-exceptionalRows;
}

function getHiddenRow(exceptionalRows, tableId) {
	return getNumItems(exceptionalRows, tableId) + 1;
}
