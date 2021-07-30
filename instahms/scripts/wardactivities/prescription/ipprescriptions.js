function captureSubmitEvent() {
	if(screenId == 'doctororder') {
		var form = document.forms[prescriptionForm];
		form.validateFormSubmit = form.submit;

		form.submit = function validatedSubmit() {
			if (!blockSubmit()) {
				var e = xGetElementById(document.forms[prescriptionForm]);
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
			form.validateFormSubmit();
			return true;
		};
	}

}
function blockSubmit() {
	if (document.getElementById('patient_discharged').value == 'true') {
		alert("Patient is inactive or discharged, add/edit/delete of doctor order is not allowed.");
		return false;
	}
	return true;
}
function init() {
	captureSubmitEvent();
	initAddItemDialog();
	editDialogGeneric();
	initDoctorAutoComplete();
	hideDiscontinued();
	initEditItemDialog();
	initDoctorEditAutoComplete();
	if(screenId == 'doctororder')
		initLoginDialog();
	if(screenId == 'addnewadmissionrequest')
		document.getElementById('hide_discontinued').disabled = true;
}

function printDoctorOrder() {
	var printerId = document.getElementById('printerId').value;
	var patientId = document.getElementById('patient_id').value;
	window.open(cpath+"/wardactivities/DoctorOrderPrint.do?_method=print&printerId="+printerId+"&patientId="+patientId);
}


function initDoctorAutoComplete() {
	if (!document.getElementById('d_doctor')) return;

	var ds = new YAHOO.util.LocalDataSource({result : doctors}, {queryMatchContains: true});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				 ],
	};

	docAutoComp = new YAHOO.widget.AutoComplete("d_doctor", "d_doctorContainer", ds);
	docAutoComp.minQueryLength = 1;
	docAutoComp.animVert = false;
	docAutoComp.maxResultsDisplayed = 50;
	docAutoComp.resultTypeList = false;
	docAutoComp.forceSelection = true;
	docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	docAutoComp.formatResult = Insta.autoHighlight;
	docAutoComp.itemSelectEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('d_doctor_id').value = record.doctor_id;
	});
	docAutoComp.selectionEnforceEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('d_doctor_id').value = '';
	});

}

function initDoctorEditAutoComplete() {
	if (!document.getElementById('ed_doctor')) return;

	var ds = new YAHOO.util.LocalDataSource({result : doctors}, {queryMatchContains: true});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "doctor_name"},
					{key : "doctor_id"}
				 ],
	};

	editDocAutoComp = new YAHOO.widget.AutoComplete("ed_doctor", "ed_doctorContainer", ds);
	editDocAutoComp.minQueryLength = 1;
	editDocAutoComp.animVert = false;
	editDocAutoComp.maxResultsDisplayed = 50;
	editDocAutoComp.resultTypeList = false;
	editDocAutoComp.forceSelection = true;
	editDocAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	editDocAutoComp.formatResult = Insta.autoHighlight;
	editDocAutoComp.itemSelectEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('ed_doctor_id').value = record.doctor_id;
	});
	editDocAutoComp.selectionEnforceEvent.subscribe(function(sType, oArgs) {
		var record = oArgs[2];
		document.getElementById('ed_doctor_id').value = '';
	});

}

//used for enter key listner
	this.handleEnterKey = function() {
		// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new autocomplete.)
	document.getElementById("d_item_name").blur();
	addToTable();
	}

var addItemDialog = null;
function initAddItemDialog() {
	var dialogDiv = document.getElementById("addItemDialog");
	if (!dialogDiv) return;

	dialogDiv.style.display = 'block';
	addItemDialog = new YAHOO.widget.Dialog("addItemDialog",
			{	width:"700px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_Ok', 'click', addToTable, addItemDialog, true);
	YAHOO.util.Event.addListener('d_Close', 'click', handleItemCancel, addItemDialog, true);
	YAHOO.util.Event.addListener('d_previous', 'click', handleAddItemPrevious, addItemDialog, true);
	YAHOO.util.Event.addListener('d_next', 'click', handleAddItemNext, addItemDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleItemCancel,
	                                                scope:addItemDialog,
	                                                correctScope:true } );
	addItemDialog.cfg.setProperty("keylisteners", [escKeyListener]);

	var enterKeyListener = new YAHOO.util.KeyListener("addItemDialog", { keys:13 },
				{ fn:this.handleEnterKey, scope:this, correctScope:true } );
	this.addItemDialog.cfg.setProperty("keylisteners", enterKeyListener);

	addItemDialog.render();
}

function handleAddItemPrevious() {
	document.getElementById('itemDiv').style.display = 'block';
	document.getElementById('doctorVisitDiv').style.display = 'none';

	enableButtons(_g_doctor_visits_exists ? 'next' : 'add', false);
}

function handleAddItemNext() {
	var itemType = getItemType();
	var itemName = document.getElementById('d_item_name').value;
	var itemId = document.getElementById('d_item_id').value;
	if (empty(itemType)) {
		alert('Please select the Item Type');
		return false;
	}
	if (itemName == '') {
   		alert('Please prescribe the item');
   		document.getElementById('d_item_name').focus();
   		return false;
   	}
	if (!validate('d')) return false;

	document.getElementById('itemDiv').style.display = 'none';
	document.getElementById('doctorVisitDiv').style.display = 'block';
	var noOfItems = _g_pack_contents == null ? 0 : _g_pack_contents.length;
	var docVisitTable = document.getElementById("docVisits");
	if (docVisitTable.rows.length == 0) {
		var doctorIndex = 0;
		for (var i=0; i<noOfItems; i++) {
			if (_g_pack_contents[i].item_type == 'Doctor' || _g_pack_contents[i].item_type == 'Department') {
				var docRow = docVisitTable.insertRow(-1);

				var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", _g_pack_contents[i].consultation_type_id);
				var disabled = false;
				if(_g_pack_contents[i].doctor_id) {
					disabled = true;
				}
				var deptDoctorsList = undefined;
				var deptName = undefined;
				if(_g_pack_contents[i].dept_id) {
					deptDoctorsList = filterDoctorsByDepartment(_g_pack_contents[i].dept_id);
					deptName = _g_pack_contents[i].activity_description;
				}
				makeTextCell(docRow, "Doctor:", "formlabel");
				var doctorVisit = "doctorVisit" + doctorIndex;
				var doctorVisitContainer = doctorVisit + "AcDropdown";
				var cell = docRow.insertCell(-1);
				cell.setAttribute("class", "yui-skin-sam");
				cell.innerHTML = '<div><input id="'+ doctorVisit +'" name="'+
					doctorVisit + (disabled ? '" value ="' + _g_pack_contents[i].activity_description + '" disabled' : '"' ) +
					' type="text" style="width: 300px"/><div id="'+ doctorVisitContainer +'" style="width: 300px"></div></div>';
				cell.innerHTML += '<div style="clear:both"/>';
				if(deptName) {
					cell.appendChild(makeLabel(null, deptName));
					cell.appendChild(document.createElement('br'));
				}
				cell.appendChild(makeLabel(null, '(' + consultationType.consultation_type + ')'));

				cell.appendChild(makeHidden("vDoctorId", "vDoctorId"+doctorIndex, _g_pack_contents[i].doctor_id ));

				cell = docRow.insertCell(-1);
				cell = docRow.insertCell(-1);

				initDoctorVisitAutoComplete(doctorVisit, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("doctorVisit", "");
						document.getElementById("vDoctorId"+index).value = aArgs[2][1];
						}, deptDoctorsList);
				doctorIndex++;
			}

		}
	}

	enableButtons('add', true);
}

function filterDoctorsByDepartment(department_id) {
	if (doctors == null) return null;
	var filteredList = new Array();
	for (var i=0; i<doctors.length; i++) {
		if (doctors[i]["dept_id"] == department_id) {
			filteredList.push(doctors[i]);
		}
	}
	return filteredList;
}

function handleItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		this.cancel();
	}
}

var parentDialog = null;
var childDialog = null;
function showAddDialog(obj) {
	addItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	addItemDialog.show();
	if(screenId == 'addnewadmissionrequest') {
		document.getElementById('d_doctor_id').value = document.getElementById('requesting_doc').value;
		var reqDocText = !empty(document.getElementById('requesting_doc').value) ? document.getElementById('requesting_doc').options[document.getElementById('requesting_doc').selectedIndex].text : ''
		document.getElementById('d_doctor').value = reqDocText;
	} else {
		if (!empty(userRecord.doctor_id)) {
			var record = findInList(doctors, "doctor_id", userRecord.doctor_id);
			document.getElementById('d_doctor_id').value = userRecord.doctor_id;
			document.getElementById('d_doctor').value = record.doctor_name;
			docAutoComp._bItemSelected = true;
			docAutoComp._sInitInputValue = docAutoComp._elTextbox.value;
		}
	}
	parentDialog = addItemDialog;
	clearFields();
	document.getElementById('itemDiv').style.display = 'block';
	document.getElementById('doctorVisitDiv').style.display = 'none';
	enableButtons('add', false);

}

function toggleEndActivity(prefix) {
	var value = document.getElementById(prefix+'_frequency').value;
	var freq_type = 'F';
	var freqChkObj = document.forms[prescriptionForm][prefix+'_freq_type'];
	for (var i=0; i<freqChkObj.length; i++) {
		if (freqChkObj[i].checked)
			freq_type = freqChkObj[i].value;
	}
	if (freq_type == 'F' && value == -1) {
		var endEls = document.getElementsByName(prefix+'_end_of_activity');
		for (var i=0; i<endEls.length; i++) {
			if (endEls[i].value == 'E') {
				endEls[i].disabled = true;
				endEls[i].checked = false;
				document.getElementById(prefix+'_end_date').value = '';
				document.getElementById(prefix+'_end_time').value = '';
				document.getElementById(prefix+'_end_date').disabled = true;
				document.getElementById(prefix+'_end_time').disabled = true;
			} else if (endEls[i].value == 'O') {
				endEls[i].disabled = false;
				endEls[i].checked = true;
				document.getElementById(prefix+'_no_of_occurrences').value = 1;
			} else if (endEls[i].value == 'D') {
				endEls[i].disabled = true;
				endEls[i].checked = false;
			}
		}
	} else {
		var endEls = document.getElementsByName(prefix+'_end_of_activity');
		for (var i=0; i<endEls.length; i++) {
			endEls[i].disabled = false;
		}
		document.getElementById(prefix+'_end_date').disabled = false;
		document.getElementById(prefix+'_end_time').disabled = false;
		document.getElementById(prefix+'_no_of_occurrences').disabled = false;
	}
}

function validate(prefix) {
	var itemType = prefix == 'd' ? getItemType() : document.getElementById(prefix + '_itemType').value;
	var freq_type = 'F';
	var freqChkObj = document.forms[prescriptionForm][prefix+'_freq_type'];
	for (var i=0; i<freqChkObj.length; i++) {
		if (freqChkObj[i].checked)
			freq_type = freqChkObj[i].value;
	}
   	var interval = document.getElementById(prefix+'_repeat_interval').value;
	var frequency = document.getElementById(prefix+'_frequency').options[document.getElementById(prefix+'_frequency').selectedIndex].text;
   	var frequencyId = document.getElementById(prefix+'_frequency').value;
	var doctor = document.getElementById(prefix+'_doctor').value;
	if (doctor == '') {
		alert('Please enter the doctor');
		document.getElementById(prefix+'_doctor').focus();
		return false;
	}
	if (itemType == 'M') {
		var dosage = document.getElementById(prefix+ '_med_dosage').value;
		if (dosage == '') {
			alert("Please enter the dosage");
			document.getElementById(prefix+'_med_dosage').focus();
			return false;
		}
	}

	if (document.getElementById(prefix+'_start_date').value == '') {
	   		alert("Please enter the start date");
	   		document.getElementById(prefix+'_start_date').focus();
	   		return false;
   	}
   	if (document.getElementById(prefix+'_start_time').value == '') {
   		alert("Please enter the start time");
   		document.getElementById(prefix+'_start_time').focus();
   		return false;
   	}
   	if (document.getElementById(prefix+'_prescription_date').value == '') {
   		alert("Please enter the Prescription date");
   		document.getElementById(prefix+'_prescription_date').focus();
   		return false;
   	}
   	if (document.getElementById(prefix+'_prescription_time').value == '') {
   		alert("Please enter the Prescription time");
   		document.getElementById(prefix+'_prescription_time').focus();
   		return false;
   	}

	if (itemType != 'C') {
		if (freq_type == 'F' && frequencyId == '') {
	   		alert("Please enter the frequency");
	   		document.getElementById(prefix+'_frequency').focus();
	   		return false;
	   	}
	   	if (freq_type == 'R') {
	   		if (interval == '') {
	   			alert("Please enter the Interval greater than zero");
	   			document.getElementById(prefix+'_repeat_interval').focus();
	   			return false;
	   		}

	   		if (!isInteger(interval)) {
	   			alert("Interval should be a Number");
	   			document.getElementById(prefix+'_repeat_interval').focus();
	   			return false;
	   		}
	   		if (interval == 0) {
	   			alert("Interval should be greater then zero.");
	   			document.getElementById(prefix+'_repeat_interval').focus();
	   			return false;
	   		}
	   	}

	   	var valid = true;
	   	valid = valid && doValidateDateField(document.getElementById(prefix+'_start_date'));
		valid = valid && validateTime(document.getElementById(prefix+'_start_time'));
		if (!valid) return false;

		var valid = true;
		valid = valid && doValidateDateField(document.getElementById(prefix+'_prescription_date'));
		valid = valid && validateTime(document.getElementById(prefix+'_prescription_time'));
		if (!valid) return false;

		var valid = true;
		valid = valid && doValidateDateField(document.getElementById(prefix+'_end_date'));
		valid = valid && validateTime(document.getElementById(prefix+'_end_time'));
		if (!valid) return false;

		var endText = '';
		var endVal = '';
		var endChkVal = '';
		var endChkObj = document.forms[prescriptionForm][prefix+'_end_of_activity'];
		for (var i=0; i<endChkObj.length; i++) {
			if (endChkObj[i].checked) {
				var val = endChkObj[i].value;
				endChkVal = val;
				if (val == 'E') {
					if (document.getElementById(prefix+'_end_date').value == '') {
						alert("Please enter the end date.");
						document.getElementById(prefix+'_end_date').focus();
						return false;
					}
					if (document.getElementById(prefix+'_end_time').value == '') {
						alert("Please enter the end time.");
						document.getElementById(prefix+'_end_time').focus();
						return false;
					}
					endVal = document.getElementById(prefix+'_end_date').value;
					if (!empty(endVal)) endVal += " " + document.getElementById(prefix+'_end_time').value;

					endText = endVal;
				} else if (val == 'O') {
					if (document.getElementById(prefix+'_no_of_occurrences').value == '' || document.getElementById(prefix+'_no_of_occurrences').value == 0) {
						alert("Please enter the Occurrences greater than zero.");
						document.getElementById(prefix+'_no_of_occurrences').focus();
						return false;
					}
					endVal = document.getElementById(prefix+'_no_of_occurrences').value;
					endText = endVal + ' time(s)';
				} else if (val == 'D') {
					endVal = 'D';
					endText = 'Till Discontinued';
				}
			}
		}

		if (endChkVal == '') {
			alert('Please select one of the end options');
			return false;
		}
	}
	return true;
}

function clearEndActivityFields(prefix) {
	var endChkObj = document.forms[prescriptionForm][prefix+'_end_of_activity'];
	for (var i=0; i<endChkObj.length; i++) {
		if (endChkObj[i].checked) {
			var val = endChkObj[i].value;
			endChkVal = val;
			if (val == 'E') {
				document.getElementById(prefix+'_no_of_occurrences').value = '';
			} else if (val == 'O') {
				document.getElementById(prefix+'_end_date').value = '';
				document.getElementById(prefix+'_end_time').value = '';
			} else if (val == 'D') {
				document.getElementById(prefix+'_no_of_occurrences').value = '';
				document.getElementById(prefix+'_end_date').value = '';
				document.getElementById(prefix+'_end_time').value = '';
			}
		}
	}
}

var itemsAdded = 0;
var colIndex=0;
var TYPE = colIndex++, NAME = colIndex++, DOSAGE = colIndex++, ADMIN_STRENGTH = colIndex++, FREQUENCY = colIndex++, ROUTE = colIndex++,
	START = colIndex++, END = colIndex++, REMARKS = colIndex++, TRASH_COL = colIndex++, EDIT_COL = colIndex++;
function addToTable() {
	var dosage = document.getElementById('d_med_dosage').value;
	var itemType = getItemType();
	var pack_type = document.getElementById('d_pack_type').value;
	var itemName = document.getElementById('d_item_name').value;
	var itemId = document.getElementById('d_item_id').value;
	var freq_type = 'F';

	var freqChkObj = document.forms[prescriptionForm].d_freq_type;
	for (var i=0; i<freqChkObj.length; i++) {
		if (freqChkObj[i].checked)
			freq_type = freqChkObj[i].value;
	}
	var adminStrength = document.getElementById('d_admin_strength').value;
	var interval = document.getElementById('d_repeat_interval').value;
	var frequency = document.getElementById('d_frequency').options[document.getElementById('d_frequency').selectedIndex].text;
   	var frequencyId = document.getElementById('d_frequency').value;
   	frequencyId = freq_type == 'F' ? frequencyId : '';
   	interval = freq_type == 'F' ? '' : interval;
   	if (frequencyId == '') frequency = '';

	if (empty(itemType)) {
		alert('Please select the Item Type');
		return false;
	}
	if (itemName == '') {
   		alert('Please prescribe the item');
   		document.getElementById('d_item_name').focus();
   		return false;
   	}
   	if (!validate('d')) return false;

	var doctorVisits = document.getElementById('docVisits');
	for (var k=0; k<doctorVisits.rows.length; k++) {
		var docName = document.getElementById('doctorVisit'+k).value;
		if (docName == '') {
			alert("Please enter the Doctor Name.");
			document.getElementById('doctorVisit'+k).focus();
			return false;
		}
	}
	var med_strength = document.getElementById('d_med_strength').value;
	var med_strength_units = document.getElementById('d_med_strength_units').value;
	var strength_unit_name = document.getElementById('d_med_strength_units').options[document.getElementById('d_med_strength_units').selectedIndex];
	if(strength_unit_name != undefined)
	strength_unit_name = med_strength_units == '' ? '' : strength_unit_name.text;
	var endText = '';
	var endVal = '';
	var endChkVal = '';
	var endChkObj = document.forms[prescriptionForm].d_end_of_activity;
	for (var i=0; i<endChkObj.length; i++) {
		if (endChkObj[i].checked) {
			var val = endChkObj[i].value;
			endChkVal = val;
			if (val == 'E') {
				endVal = document.getElementById('d_end_date').value;
				if (!empty(endVal)) endVal += " " + document.getElementById('d_end_time').value;

				endText = endVal;
			} else if (val == 'O') {
				endVal = document.getElementById('d_no_of_occurrences').value;
				endText = endVal + ' time(s)';
			} else if (val == 'D') {
				endVal = 'Y';
				endText = 'Till Discontinued';
			}
		}
	}

	var noOfItems = 1;
	if (pack_type == 'O') {
		noOfItems = _g_pack_contents.length;
		// if the prescribed items is a template package display all the components of a package.
	}
	for (var k=0; k<noOfItems; k++) {
		if (_g_pack_contents != null) {
			itemId = _g_pack_contents[k].activity_id;
			itemName = _g_pack_contents[k].item_name;
			itemType = _g_pack_contents[k].item_type;
			if( itemType == 'Laboratory' || itemType == 'Radiology') {
				itemType = 'I';
			} else if (itemType == 'Doctor' || itemType == 'Department') {
				itemType = 'C';
			} else if (itemType == 'Service') {
				itemType = 'S';
			} else {
				itemType = 'POC';
			}
			if (itemType == 'I' || itemType == 'S') {
				// add to the grid, only if package component is a test or service.
			} else {
				continue;
			}
		}
		var id = getNumCharges('prescDetails');
	   	var table = document.getElementById("prescDetails");
		var templateRow = table.rows[getTemplateRow('prescDetails')];
		var row = templateRow.cloneNode(true);
		table.tBodies[0].insertBefore(row, templateRow);
		row.style.display = 'table-row';
	   	row.id = "itemRow" + id;

	   	var cell = null;
	   	var doctorId = document.getElementById('d_doctor_id').value;
	   	var doctorName = document.getElementById('d_doctor').value;
	   	var remarks = document.getElementById('d_remarks').value;
	   	var genericCode = document.getElementById('d_generic_code').value;
	   	var genericName = document.getElementById('d_generic_name').value;
	   	var ispackage = document.getElementById('d_ispackage').value;
	   	var routeId = document.getElementById('d_med_route').options[document.getElementById('d_med_route').selectedIndex].value;
	   	var routeName = document.getElementById('d_med_route').options[document.getElementById('d_med_route').selectedIndex].text;
	   	routeName = routeId == '' ? '' : routeName;
	   	var med_form_id = document.getElementById('d_med_form_id').value;
	   	var med_form_name = document.getElementById('d_med_form_id').options[document.getElementById('d_med_form_id').selectedIndex];
	   	med_form_name = med_form_id == '' ? '' : med_form_name.text;
		var consumption_uom = document.getElementById('d_consumption_uom').value;
		var repeat_interval_units = document.getElementById('d_repeat_interval_units').value;
		var repeat_interval_units_text = document.getElementById('d_repeat_interval_units').options[
			document.getElementById('d_repeat_interval_units').selectedIndex].text;
		var type = '';
		if (itemType == 'M') type = 'Medicine';
		if (itemType == 'I') type = 'Inv.';
		if (itemType == 'S') type = 'Service';
		if (itemType == 'C') type = 'Consultation';
		if (itemType == 'OPE') type = 'Surgery/Procedure';
		if (itemType == 'O') type = 'Others';

		var name = itemName;
		if (itemType == 'M') {
			if (!empty(med_form_name))
				name += '/' + med_form_name;
			if (!empty(med_strength)){
			   if(strength_unit_name != undefined)
				name += '/' + med_strength + ' ' + strength_unit_name.text;
				else
				name += '/' + med_strength;
              }
		}
	   	setNodeText(row.cells[TYPE], type);
	   	setNodeText(row.cells[NAME], name, 25, name);
	   	if (itemType == 'M') {
			setNodeText(row.cells[DOSAGE], dosage + consumption_uom);

			setHiddenValue(id, "h_generic_code", genericCode);
			setHiddenValue(id, "h_generic_name", genericName);
			setHiddenValue(id, "h_qty_in_stock", document.getElementById('d_qty_in_stock').value);
			setHiddenValue(id, "h_med_form_id", med_form_id);
			setHiddenValue(id, "h_med_strength", med_strength);
			setHiddenValue(id, "h_med_strength_units", med_strength_units);

		}

		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
		setNodeText(row.cells[FREQUENCY], freq_type == 'F' ? frequency : (interval + ' ' +repeat_interval_units_text));
		setNodeText(row.cells[ROUTE], routeName);

		var startDateTime = document.getElementById('d_start_date').value;
		if (!empty(startDateTime)) startDateTime += " " + document.getElementById('d_start_time').value;

		var prescDateTime = document.getElementById('d_prescription_date').value;
		if (!empty(prescDateTime)) prescDateTime += " " + document.getElementById('d_prescription_time').value;


		setNodeText(row.cells[START],  startDateTime);
		setNodeText(row.cells[END], endText);
		setNodeText(row.cells[REMARKS], remarks, 30, remarks);

		setHiddenValue(id, "h_prescription_id", "-1");
		setHiddenValue(id, "h_doctor_id", doctorId);
		setHiddenValue(id, "h_doctor_name", doctorName);
		setHiddenValue(id, "h_repeat_interval", interval);
		setHiddenValue(id, "h_med_dosage", dosage);
		setHiddenValue(id, "h_admin_strength", adminStrength);
		setHiddenValue(id, "h_presc_type", itemType);
		setHiddenValue(id, "h_item_name", itemName);
		setHiddenValue(id, "h_item_id", itemId);
		setHiddenValue(id, "h_remarks", remarks);
		setHiddenValue(id, "h_ispackage", ispackage);
		setHiddenValue(id, "h_recurrence_daily_id", frequencyId);
		setHiddenValue(id, "h_freq_type", freq_type);
		setHiddenValue(id, "h_med_route", routeId);
		setHiddenValue(id, "h_route_name", routeName);
		setHiddenValue(id, "h_start_datetime", startDateTime);
		setHiddenValue(id, "h_prescription_date", prescDateTime);
		setHiddenValue(id, "h_consumption_uom", consumption_uom);
		setHiddenValue(id, "h_entered_by", document.getElementById('d_entered_by').value);
		setHiddenValue(id, "h_repeat_interval_units", freq_type == 'F' ? '' : repeat_interval_units);

		var priorMed = 'N';
		if (document.getElementById('d_prior_medication').checked)
			priorMed = 'Y';
		setHiddenValue(id, 'h_prior_med', priorMed);

		var discontinued = 'N';
		if (document.getElementById('d_discontinued').checked)
			discontinued = 'Y';
		setHiddenValue(id, "h_discontinued", discontinued);

		if (endChkVal == 'E') {
			setHiddenValue(id, "h_end_datetime", endVal);

			setHiddenValue(id, "h_no_of_occurrences", '');
			setHiddenValue(id, "h_end_on_discontinue", 'N');
		} else if (endChkVal == 'O') {
			setHiddenValue(id, "h_no_of_occurrences", endVal);

			setHiddenValue(id, "h_end_datetime", '');
			setHiddenValue(id, "h_end_on_discontinue", 'N');
		} else if (endChkVal == 'D') {
			setHiddenValue(id, "h_end_on_discontinue", endVal);

			setHiddenValue(id, "h_no_of_occurrences", '');
			setHiddenValue(id, "h_end_datetime", '');
		}
		setRowStyle(id);
		itemsAdded++;
	}
	for (var i=0; i<doctorVisits.rows.length; i++) {

		itemId = document.getElementById('vDoctorId'+i).value;
		itemName = document.getElementById('doctorVisit'+i).value;
		itemType = 'C';

		var id = getNumCharges('prescDetails');
	   	var table = document.getElementById("prescDetails");
		var templateRow = table.rows[getTemplateRow('prescDetails')];
		var row = templateRow.cloneNode(true);
		table.tBodies[0].insertBefore(row, templateRow);
		row.style.display = 'table-row';
	   	row.id = "itemRow" + id;

	   	var cell = null;
	   	var doctorId = document.getElementById('d_doctor_id').value;
	   	var doctorName = document.getElementById('d_doctor').value;
	   	var remarks = document.getElementById('d_remarks').value;
	   	var ispackage = document.getElementById('d_ispackage').value;
	   	var repeat_interval_units = document.getElementById('d_repeat_interval_units').value;
		var repeat_interval_units_text = document.getElementById('d_repeat_interval_units').options[
			document.getElementById('d_repeat_interval_units').selectedIndex].text;
		var type = '';
		if (itemType == 'C') type = 'Consultation';

		var name = itemName;
		setNodeText(row.cells[TYPE], 'Consultation');
	   	setNodeText(row.cells[NAME], name, 25, name);

		setNodeText(row.cells[FREQUENCY], 'Once');

		var startDateTime = document.getElementById('d_start_date').value;
		if (!empty(startDateTime)) startDateTime += " " + document.getElementById('d_start_time').value;

		var prescDateTime = document.getElementById('d_prescription_date').value;
		if (!empty(prescDateTime)) prescDateTime += " " + document.getElementById('d_prescription_time').value;


		setNodeText(row.cells[START],  startDateTime);
		setNodeText(row.cells[END], '1 time(s)');
		setNodeText(row.cells[REMARKS], remarks, 30, remarks);

		setHiddenValue(id, "h_prescription_id", "-1");
		setHiddenValue(id, "h_doctor_id", doctorId);
		setHiddenValue(id, "h_doctor_name", doctorName);
		setHiddenValue(id, "h_repeat_interval", '');
		setHiddenValue(id, "h_presc_type", itemType);
		setHiddenValue(id, "h_item_name", itemName);
		setHiddenValue(id, "h_item_id", itemId);
		setHiddenValue(id, "h_remarks", remarks);
		setHiddenValue(id, "h_ispackage", ispackage);
		setHiddenValue(id, "h_recurrence_daily_id", '-1');
		setHiddenValue(id, "h_freq_type", 'F');
		setHiddenValue(id, "h_start_datetime", startDateTime);
		setHiddenValue(id, "h_prescription_date", prescDateTime);
		setHiddenValue(id, "h_entered_by", document.getElementById('d_entered_by').value);
		setHiddenValue(id, "h_repeat_interval_units", '');

		var priorMed = 'N';
		if (document.getElementById('d_prior_medication').checked)
			priorMed = 'Y';
		setHiddenValue(id, 'h_prior_med', priorMed);

		var discontinued = 'N';
		if (document.getElementById('d_discontinued').checked)
			discontinued = 'Y';
		setHiddenValue(id, "h_discontinued", discontinued);

		setHiddenValue(id, "h_no_of_occurrences", '1');
		setHiddenValue(id, "h_end_datetime", '');
		setHiddenValue(id, "h_end_on_discontinue", 'N');

		setRowStyle(id);
		itemsAdded++;
	}
	clearFields();

	document.getElementById('itemDiv').style.display = 'block';
	document.getElementById('doctorVisitDiv').style.display = 'none';
	this.align("tr", "tl");
	document.getElementById('d_item_name').focus();
	return id;
}

function initDoctorVisitAutoComplete(field, selectHandler, deptDoctorsList = undefined) {
	var ds = new YAHOO.util.LocalDataSource({result : deptDoctorsList? deptDoctorsList : doctors},{ queryMatchContains : true });
	ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = { resultsList : "result",
		  fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete(field, field+'AcDropdown', ds);

	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;

	autoComp.itemSelectEvent.subscribe(selectHandler);
	return autoComp;
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'prescDetails');
	var prescribedId = getIndexedValue("h_prescription_id", i);
	var qty_in_stock = getIndexedValue("qty_in_stock", i);

 	var flagImgs = row.cells[TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "-1");
	var cancelled = getIndexedValue("h_delete", i) == 'true';
	var edited = getIndexedValue("h_edited", i) == 'true';
	var itemType = getIndexedValue("h_presc_type", i);

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
		if (itemType == 'M' && qty_in_stock == 0)
			cls = 'zero_qty'
		else cls = 'added';
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

function cancelPrescription(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	var oldDeleted =  getIndexedValue("h_delete", id);

	var isNew = getIndexedValue("h_prescription_id", id) == '-1';
	var discontinued = getIndexedValue('h_discontinued', id);

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		if (discontinued == 'Y') {
			alert("Item is already discontinued, you can not delete this.");
			return false;
		}
		if (!confirm('All incomplete activities associated with the prescription will be deleted')) return false;
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("h_delete", id, newDeleted);
		setIndexedValue("h_edited", id, "true");
		setRowStyle(id);
	}
	return false;
}

function getItemType() {
	var itemTypeObj = document.getElementsByName('d_itemType');
	for (var i=0; i<itemTypeObj.length; i++) {
		if (itemTypeObj[i].checked)
			return itemTypeObj[i].value;
	}
	return null;
}

function toggleFrequencyNDuration(disable, prefix) {
	var freqType = document.forms[prescriptionForm][prefix+'_freq_type'];
	for (var i=0; i<freqType.length; i++) {
		freqType[i].disabled = disable;
	}
	document.getElementById(prefix+'_frequency').disabled = disable;
	document.getElementById(prefix+'_repeat_interval').disabled = disable;
	var endOfActivity = document.forms[prescriptionForm][prefix+"_end_of_activity"];
	for (var i=0; i<endOfActivity.length; i++) {
		endOfActivity[i].disabled = disable;
	}
	document.getElementById(prefix+'_end_date').disabled = disable;
	document.getElementById(prefix+'_end_time').disabled = disable;
	document.getElementById(prefix+'_no_of_occurrences').disabled = disable;
	if (prefix == 'd') {
		// in add dialog on item change(ex: medicine to doctor) reset the end of activity to no of occurrences and it's value to 1.
		for (var i=0; i<endOfActivity.length; i++) {
			endOfActivity[i].checked = endOfActivity[i].value == 'O';
		}
		document.getElementById(prefix+'_no_of_occurrences').value = 1;
		clearEndActivityFields(prefix);
	}
}

function clearFields() {
	document.getElementById('d_item_name').value = '';
	document.getElementById('d_admin_strength').value = '';
   	document.getElementById('d_med_dosage').value = '';
   	document.getElementById('d_med_route').length = 1;
   	document.getElementById('d_remarks').value = '';
   	document.getElementById('d_ispackage').value = '';
   	document.getElementById('d_generic_name_anchor').innerHTML = '';
   	document.getElementById('d_generic_name_anchor').style.display = 'none';
   	document.getElementById('d_generic_name_anchor').href = '';
   	document.getElementById('d_med_form_id').value = '';
	document.getElementById('d_med_strength').value = '';
	document.getElementById('d_med_strength_units').value = '';
	document.getElementById('d_discontinued').checked = false;
	document.getElementById('d_consumption_uom').value = '';
	document.getElementById('d_medicineUOM').textContent = '';
	document.getElementById('d_repeat_interval_units').value = 'M';
	document.getElementById('d_hide_ope').style.display = 'block';
	
	var itemType = getItemType();
	if (itemType == 'OPE') {
		document.getElementById('d_hide_ope').style.display = 'none';
	}
	(document.forms[prescriptionForm].d_freq_type)[0].checked = itemType != 'C';
   	(document.forms[prescriptionForm].d_freq_type)[1].checked = false;
   	document.getElementById('d_frequency').value = '-1';
   	document.forms[prescriptionForm].d_end_of_activity.value = 'O';
   	document.getElementById('d_repeat_interval').value = '';
	if (itemType == 'C') {
		toggleFrequencyNDuration(true, 'd');
		document.getElementById('d_itemDisplayLabel').textContent = 'Doctor: ';
	} else if(itemType == 'OPE') {
		document.getElementById('d_itemDisplayLabel').textContent = 'Surgery/Procedure: ';
	} else {
		toggleFrequencyNDuration(false, 'd');
		document.getElementById('d_itemDisplayLabel').textContent = 'Item: ';
	}
	toggleEndActivity('d');
	document.getElementById('d_prior_medication').checked = false;
	if (itemType == 'M') {
		document.getElementById('d_prior_medication').disabled = false;
	} else {
		document.getElementById('d_prior_medication').disabled = true;
	}
	var doctorVisitsTab = document.getElementById('docVisits');
	var len = doctorVisitsTab.rows.length;
	for (var i=len; i>0; i--) {
		doctorVisitsTab.deleteRow(i-1);
	}
	enableButtons('add', false);
}

function onItemChange() {
	clearFields();
	initItemAutoComplete();
	toggleItemFormRow(true);
	var itemType = getItemType();

	document.getElementById('d_remarks').disabled = false;
	if (itemType == "M") {
		document.getElementById('d_med_dosage').disabled = false;
		document.getElementById('d_admin_strength').disabled = false;
		document.getElementById('d_med_route').disabled = false;

	} else if (itemType == "OPE") {
		document.getElementById('d_hide_ope').style.display = 'none';
		
	} else {
		document.getElementById('d_med_dosage').disabled = true;
		document.getElementById('d_admin_strength').disabled = true;
		document.getElementById('d_med_route').disabled = true;

		document.getElementById('d_generic_name_anchor').innerHTML = '';
 		document.getElementById('d_generic_name_anchor').style.display = 'none';
   		document.getElementById('d_generic_name_anchor').href = '';
	}
}



function toggleInterval(prefix) {
	var frequency = document.getElementById(prefix+ "freq_type").value;
	if (frequency == 0) {
		document.getElementById(prefix + "repeat_interval").disabled = false;
	} else {
		document.getElementById(prefix + "repeat_interval").disabled = true;
	}
}

function toggleItemFormRow(addDialog) {
	var prefix = addDialog ? 'd_' : 'ed_';
	var itemType = addDialog ? getItemType() : document.getElementById(prefix + 'itemType').value;
	document.getElementById(prefix + 'med_form_id').disabled = itemType != 'Medicine';
	document.getElementById(prefix + 'med_strength').disabled = itemType != 'Medicine';
	document.getElementById(prefix + 'med_strength_units').disabled = itemType != 'Medicine';
}

var itemAutoComp = null;
function initItemAutoComplete() {
	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	var itemType = getItemType();
	if (itemType == 'O') return null; // for doctor instrctions no need to create the autocomplete.

	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/common/commonAjaxRequest.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId + "&center_id=" + centerId + "&tpa_id=" + tpaId + '&dept_id=' + departmentId + '&gender_applicability=' + patientGender;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "generic_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : 'consumption_uom'},
					{key : 'item_form_id'},
					{key : 'item_strength'},
					{key : 'med_strength_units'},
					{key : 'pack_type'}
				 ],
		numMatchFields: 2
	};

	itemAutoComp = new YAHOO.widget.AutoComplete("d_item_name", "d_itemContainer", ds);
	itemAutoComp.minQueryLength = 1;
	itemAutoComp.animVert = false;
	itemAutoComp.maxResultsDisplayed = 50;
	itemAutoComp.resultTypeList = false;
	var forceSelection = true;
	itemAutoComp.forceSelection = forceSelection;

	itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'M') {
			// show qty only for pharmacy items.
			if (use_store_items == 'Y')
				highlightedValue += "(" + record.qty + ") ";
			highlightedValue += (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
		}
		return highlightedValue;
	}

	itemAutoComp.dataRequestEvent.subscribe(clearMasterType);
	if (forceSelection) {
		itemAutoComp.itemSelectEvent.subscribe(selectItem);
		itemAutoComp.selectionEnforceEvent.subscribe(clearMasterType);
	} else {
		itemAutoComp.itemSelectEvent.subscribe(selectItem);
	}

	return itemAutoComp;
}

var _g_pack_contents = null;
var _g_doctor_visits_exists = false;
function selectItem(sType, oArgs) {
	var record = oArgs[2];
	if (record.item_type == 'M' && !empty(record.generic_name)) {
		document.getElementById('d_generic_name_anchor').style.display = 'block';
		document.getElementById('d_generic_name_anchor').href = 'javascript:showGenericInfo("", "d_", "dialog", "'+record.generic_code+'")';
		document.getElementById('d_generic_name_anchor').innerHTML = record.generic_name;
		document.getElementById('d_generic_code').value = record.generic_code;
		document.getElementById('d_generic_name').value = record.generic_name;
	}
	document.getElementById('d_qty_in_stock').value = record.qty;
	document.getElementById('d_item_id').value = record.item_id;
	document.getElementById('d_ispackage').value = record.ispkg;

	if (record.item_type == 'M')
		getRouteOfAdministrations();

	document.getElementById('d_med_form_id').value = record.item_form_id == 0 ? '' : record.item_form_id;
	document.getElementById('d_med_strength').value = record.item_strength;
	document.getElementById('d_med_strength_units').value = record.med_strength_units;
	document.getElementById('d_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_medicineUOM').textContent = empty(record.consumption_uom) ? '' : '(' + record.consumption_uom + ')';
	document.getElementById('d_pack_type').value = record.pack_type;
	var doctorVisitsExists = false;
	if (record.pack_type == 'O') {
		var	url = cpath + '/patients/orders/getpackagecontents.json?packageId='+record.item_id+'&multi_visit_package='+false;
		var ajaxReq = newXMLHttpRequest();
		ajaxReq.open("GET", url.toString(), false);
		ajaxReq.send();
		if (ajaxReq.readyState == 4) {
			if ( (ajaxReq.status == 200) && (ajaxReq.responseText != null ) ) {
				var packageComponentDetails = eval('('+ajaxReq.responseText+')');
				_g_pack_contents = packageComponentDetails['packComponentDetails'];
			}
		}
		var noOfItems = _g_pack_contents.length;
		_g_doctor_visits_exists = false;
		var packageContentsModified = [];
		for (var k=0; k<noOfItems; k++) {
			if (_g_pack_contents[k].item_type === 'Doctor' || _g_pack_contents[k].item_type === 'Department') {
				var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", _g_pack_contents[k].consultation_type_id);
				if("i" === consultationType.patient_type) {
					packageContentsModified.push(_g_pack_contents[k]);
					_g_doctor_visits_exists = true;
				}
			} else {
				packageContentsModified.push(_g_pack_contents[k]);
			}
		}
		_g_pack_contents = packageContentsModified;
	} else {
		_g_pack_contents = null;
		_g_doctor_visits_exists = false;
	}
	enableButtons(_g_doctor_visits_exists ? 'next' : 'add', false);
}

function enableButtons(addOrNext, previousEnabled) {
	var addEnabled = (addOrNext == 'add');
	var nextEnabled = !addEnabled;
	document.getElementById('d_Ok').disabled = !addEnabled;
	document.getElementById('d_next').disabled = !nextEnabled;
	document.getElementById('d_previous').disabled = !previousEnabled;

	addItemDialog.align("tr", "tl");
}

function getRouteOfAdministrations() {
	var itemId = document.getElementById('d_item_id').value;
	var itemName = document.getElementById('d_item_name').value;
	var url = cpath+'/common/commonAjaxRequest.do?_method=getRoutesOfAdministrations';
		url += '&item_id='+itemId;
		url += '&item_name='+encodeURIComponent(itemName);
	var	ajaxRequestForRoutes = YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: onGetRoutes,
					failure: onGetRoutesFailure
				}
		)
}

function onGetRoutes(response) {
	if (response.responseText != undefined) {
		var routes = eval('(' + response.responseText + ')');
		if (routes == null) {
			document.getElementById('d_med_route').length = 1;
			return ;
		}
		var routeIds = routes.route_id.split(",");
		var routeNames = routes.route_name.split(",");
		var medicine_route_el = document.getElementById('d_med_route');
		medicine_route_el.length = 1; // clear the previously populated list
		var len = 1;
		for (var i=0; i<routeIds.length; i++) {
			if (routeIds[i].trim() != '') {
				medicine_route_el.length = len+1;
				medicine_route_el.options[len].value = routeIds[i].trim();
				medicine_route_el.options[len].text = routeNames[i];
				len++;
			}
		}
	}
}

function onGetRoutesFailure() {
}

function clearMasterType(oSelf) {
	document.getElementById('d_item_id').value = '';
	document.getElementById('d_generic_name_anchor').style.display = 'none';
	document.getElementById('d_generic_name_anchor').href = '';
	document.getElementById('d_generic_name_anchor').innerHTML = '';
	document.getElementById('d_generic_code').value = '';
	document.getElementById('d_generic_name').value = '';
	document.getElementById('d_ispackage').value = '';
	document.getElementById('d_qty_in_stock').value = '';
	document.getElementById('d_med_form_id').value = '';
	document.getElementById('d_med_strength').value = '';
	document.getElementById('d_med_strength_units').value == '';
	document.getElementById('d_consumption_uom').value = '';
	document.getElementById('d_medicineUOM').textContent = '';
}

function validateForm() {
	if (isSharedLogIn == 'Y') {
		loginDialog.show();
		document.getElementById("login_user").focus();
	}
	else {
		document.forms[prescriptionForm].submit();
	}
	return true;
}

function submitHandler() {
	document.getElementById('authUser').value = document.getElementById('login_user').value;
	document.forms[prescriptionForm].submit();
	return false;
}

function hideDiscontinued() {
	// when we directly come from a menu item, only patient header part exists. so no items exists, just ignore.
	if (!document.getElementById('hide_discontinued')) return ;

	var table = document.getElementById('prescDetails');
	var checked = document.getElementById('hide_discontinued').checked;
	for (var i=1; i<prescDetails.rows.length-1; i++) {
		var val = getElementByName(prescDetails.rows[i], 'h_discontinued').value;
		prescDetails.rows[i].style.display = (checked && val == 'Y') ? 'none' : 'table-row';

	}
}

var editItemDialog = null;
function initEditItemDialog() {
	var dialogDiv = document.getElementById("editItemDialog");
	if (!dialogDiv) return;

	dialogDiv.style.display = 'block';
	editItemDialog = new YAHOO.widget.Dialog("editItemDialog",
			{	width:"650px",
				context : ["editItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditItemCancel,
	                                                scope:editItemDialog,
	                                                correctScope:true } );
	editItemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editItemDialog.cancelEvent.subscribe(handleEditItemCancel);
	YAHOO.util.Event.addListener('ed_Ok', 'click', editTableRow, editItemDialog, true);
	YAHOO.util.Event.addListener('ed_Close', 'click', handleEditItemCancel, editItemDialog, true);
	YAHOO.util.Event.addListener('ed_Previous', 'click', openPrevious, editItemDialog, true);
	YAHOO.util.Event.addListener('ed_Next', 'click', openNext, editItemDialog, true);
	editItemDialog.render();
}

function handleEditItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		var id = document.forms[prescriptionForm].editRowId.value;
		var row = getChargeRow(id, "prescDetails");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldEdited = false;
		this.hide();
	}
}

function showEditPrescDialog(obj) {
	parentDialog = editItemDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editItemDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.forms[prescriptionForm].editRowId.value = id;

	document.getElementById('ed_doctor').disabled = false;
	document.getElementById('ed_prescription_date').disabled = false;
	document.getElementById('ed_prescription_time').disabled = false;

	document.getElementById('ed_prescription_date').value = getIndexedValue('h_prescription_date', id).split(' ')[0];
	document.getElementById('ed_prescription_time').value = getIndexedValue('h_prescription_date', id).split(' ')[1];
	document.getElementById('ed_doctor').value = getIndexedValue('h_doctor_name', id);
	document.getElementById('ed_doctor_id').value = getIndexedValue('h_doctor_id', id);
	document.getElementById('ed_ip_entered_by_label').textContent = getIndexedValue('h_entered_by', id);
	document.getElementById('ed_entered_by').value = getIndexedValue('h_entered_by', id);
	document.getElementById('ed_hide_ope').style.display = 'block';

	if (editDocAutoComp._elTextbox.value != '') {
		editDocAutoComp._bItemSelected = true;
		editDocAutoComp._sInitInputValue = editDocAutoComp._elTextbox.value;
	}

	var itemType = getIndexedValue("h_presc_type", id);
	var type = '';
	if (itemType == 'M') type = 'Medicine';
	if (itemType == 'I') type = 'Investigation';
	if (itemType == 'S') type = 'Service';
	if (itemType == 'C') type = 'Consultation';
	if (itemType == 'O') type = 'Others';
	if (itemType == 'OPE') type = 'Surgery/Procedure';
	
	document.getElementById('ed_itemDisplayLabel').textContent = itemType == 'C' ? 'Doctor: ' : 'Item: ' ;
	document.getElementById('ed_itemTypeLabel').textContent = type;
	document.getElementById('ed_item_name').textContent = getIndexedValue("h_item_name", id);
	document.getElementById('ed_item_id').value = getIndexedValue("h_item_id", id);
	document.getElementById('ed_itemType').value = itemType;
	document.getElementById('ed_prior_medication').checked = getIndexedValue('h_prior_med', id) == 'Y';

	toggleItemFormRow(false);
	if (itemType == 'M') {
		document.getElementById('ed_prior_medication').disabled = false;
		document.getElementById('ed_med_dosage').disabled = false;
		document.getElementById('ed_admin_strength').disabled = false;
		document.getElementById('ed_med_route').disabled = false;

		document.getElementById('ed_generic_name_anchor').innerHTML = getIndexedValue("h_generic_name", id);
		document.getElementById('ed_generic_name_anchor').href =
			'javascript:showGenericInfo("", "", "editdialog", "' + getIndexedValue("h_generic_code", id) + '")';
		document.getElementById('ed_generic_name_anchor').style.display = 'block';

	} else if (itemType == 'OPE') {
		document.getElementById('ed_hide_ope').style.display = 'none';
		document.getElementById('ed_itemDisplayLabel').textContent = 'Surgery/Procedure:';
	} else {
		document.getElementById('ed_prior_medication').disabled = true;
		document.getElementById('ed_med_dosage').disabled = true;
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_med_route').disabled = false;

		document.getElementById('ed_generic_name_anchor').innerHTML = '';
		document.getElementById('ed_generic_name_anchor').style.display = 'none';
		document.getElementById('ed_generic_name_anchor').href='';
	}
	document.getElementById('ed_medicineUOM').textContent = getIndexedValue("h_consumption_uom", id);
	document.getElementById('ed_consumption_uom').textContent = getIndexedValue("h_consumption_uom", id);
	document.getElementById('ed_med_strength').value = getIndexedValue('h_med_strength', id);
	document.getElementById('ed_med_strength_units').value = getIndexedValue('h_med_strength_units', id);
	document.getElementById('ed_med_form_id').value = getIndexedValue('h_med_form_id', id);
	document.getElementById('ed_med_route').value = getIndexedValue('h_med_route', id);
	document.getElementById('ed_med_dosage').value = getIndexedValue('h_med_dosage', id);
	document.getElementById('ed_admin_strength').value = getIndexedValue("h_admin_strength", id);

	document.getElementById('ed_start_date').disabled = false;
	document.getElementById('ed_start_time').disabled = false;
	document.getElementById('ed_start_date').value = getIndexedValue('h_start_datetime', id).split(' ')[0];
	document.getElementById('ed_start_time').value = getIndexedValue('h_start_datetime', id).split(' ')[1];
	if (itemType == 'C') {
		toggleFrequencyNDuration(true, 'ed');
		document.getElementById('ed_frequency').value = '-1'
		document.getElementById('ed_repeat_interval').value = '';
		document.getElementById('ed_repeat_interval_units').value = '';
		document.getElementById('ed_end_date').value = '';
		document.getElementById('ed_end_time').value = '';
		document.getElementById('ed_no_of_occurrences').value = getIndexedValue('h_no_of_occurrences', id);

	} else {
		var freqType = getIndexedValue('h_freq_type', id);
		document.forms[prescriptionForm].ed_freq_type[0].disabled = false;
		document.forms[prescriptionForm].ed_freq_type[1].disabled = false;
		document.getElementById('ed_frequency').disabled = false;
		document.getElementById('ed_repeat_interval').disabled = false;
		if (freqType == 'F') {
			document.forms[prescriptionForm].ed_freq_type[0].checked = true;
		} else {
			document.forms[prescriptionForm].ed_freq_type[1].checked = true;
		}
		document.getElementById('ed_frequency').value = getIndexedValue('h_recurrence_daily_id', id);
		document.getElementById('ed_repeat_interval').value = getIndexedValue('h_repeat_interval', id);
		var interval_units = trim(getIndexedValue('h_repeat_interval_units', id));
		if (!empty(interval_units))
			document.getElementById('ed_repeat_interval_units').value = interval_units;
		toggleEndActivity('ed');

		var endOfDate = getIndexedValue('h_end_datetime', id);
		var noOfOccurrences = getIndexedValue('h_no_of_occurrences', id);
		var tillDiscontinued = getIndexedValue('h_end_on_discontinue', id);
		if (!empty(endOfDate)) {
			document.forms[prescriptionForm].ed_end_of_activity[0].checked = true;
			document.getElementById('ed_end_date').value = getIndexedValue('h_end_datetime', id).split(' ')[0];
			document.getElementById('ed_end_time').value = getIndexedValue('h_end_datetime', id).split(' ')[1];
		} else if (!empty(noOfOccurrences)) {
			document.forms[prescriptionForm].ed_end_of_activity[1].checked = true;
			document.getElementById('ed_no_of_occurrences').value = getIndexedValue('h_no_of_occurrences', id);
		} else if (!empty(tillDiscontinued)) {
			document.forms[prescriptionForm].ed_end_of_activity[2].checked = true;
		}
		clearEndActivityFields('ed');
	}
	document.getElementById('ed_discontinued').disabled = getIndexedValue('h_discontinued', id) == 'Y';
	document.getElementById('ed_discontinued').checked = getIndexedValue('h_discontinued', id) == 'Y';
	document.getElementById('ed_ispackage').value = getIndexedValue("ispackage", id);
	document.getElementById('ed_remarks').value = getIndexedValue('h_remarks', id);

	var discontinued = getIndexedValue('h_discontinued', id);
	if (discontinued == 'Y') {
		document.getElementById('ed_doctor').disabled = true;
		document.getElementById('ed_prescription_date').disabled = true;
		document.getElementById('ed_prescription_time').disabled = true;

		document.getElementById('ed_med_strength').disabled = true;
		document.getElementById('ed_med_strength_units').disabled = true;
		document.getElementById('ed_med_form_id').disabled = true;
		document.getElementById('ed_med_dosage').disabled = true;
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_prior_medication').disabled = true;
		document.getElementById('ed_frequency').disabled = true;
		document.getElementById('ed_repeat_interval').disabled = true;
		var freqType = document.forms[prescriptionForm].ed_freq_type;
		for (var i=0; i<freqType.length; i++) {
			freqType[i].disabled = true;
		}
		document.getElementById('ed_start_date').disabled = true;
		document.getElementById('ed_start_time').disabled = true;
		var endOfActivity = document.forms[prescriptionForm].ed_end_of_activity;
		for (var i=0; i<endOfActivity.length; i++) {
			endOfActivity[i].disabled = true;
		}
		document.getElementById('ed_end_date').disabled = true;
		document.getElementById('ed_end_time').disabled = true;
		document.getElementById('ed_no_of_occurrences').disabled = true;

		document.getElementById('ed_discontinued').disabled = true;
		document.getElementById('ed_remarks').disabled = true;
	}

	if(screenId == 'addnewadmissionrequest') {
		document.getElementById('ed_discontinued').disabled = true;
	}
	document.getElementById('ed_remarks').focus();
	return false;
}

function isFreqNDurationModified() {
	var id = document.forms[prescriptionForm].editRowId.value;
	var prescriptionId = getIndexedValue('h_prescription_id', id);
	if (prescriptionId > 0) {
		var freqType = 'F';
		var freqTypeObj = document.forms[prescriptionForm].ed_freq_type;
		for (i=0; i<freqTypeObj.length; i++) {
			if (freqTypeObj[i].checked) {
				freqType = freqTypeObj[i].value;
			}
		}
		var startDateTime = document.getElementById('ed_start_date').value == '' ? '' :
			document.getElementById('ed_start_date').value + ' ' + document.getElementById('ed_start_time').value;
		var endDateTime = document.getElementById('ed_end_date').value == '' ? '' :
			document.getElementById('ed_end_date').value + ' ' + document.getElementById('ed_end_time').value

		if (getIndexedValue('h_org_freq_type', id) != freqType) {
			return true;
		} else if (getIndexedValue('h_org_recurrence_daily_id', id) != document.getElementById('ed_frequency').value) {
			return true;
		} else if (getIndexedValue('h_org_repeat_interval', id) != document.getElementById('ed_repeat_interval').value) {
			return true;
		} else if (freqType == 'R' && (getIndexedValue('h_org_repeat_interval_units', id) != document.getElementById('ed_repeat_interval_units').value)) {
			return true;
		} else if (getIndexedValue('h_start_datetime', id) != startDateTime) {
			return true;
		} else if (getIndexedValue('h_end_datetime', id) !=	endDateTime) {
			return true;
		} else if (getIndexedValue('h_no_of_occurrences', id) != document.getElementById('ed_no_of_occurrences').value) {
			return true;
		} else {
			if (document.forms[prescriptionForm].ed_end_of_activity[2].checked && getIndexedValue('h_end_on_discontinued', id) != 'D' ) {
				return true;
			} else if (!document.forms[prescriptionForm].ed_end_of_activity[2].checked && getIndexedValue('h_end_on_discontinued', id) == 'D') {
				return true;
			}
		}
	}
	return false;
}

function editTableRow() {
	var id = document.forms[prescriptionForm].editRowId.value;
	var row = getChargeRow(id, 'prescDetails');
	if (!fieldEdited) {
		editItemDialog.cancel();
		return false;
	}

	if (!document.getElementById('ed_discontinued').disabled && document.getElementById('ed_discontinued').checked) {
		if (!confirm('Activity marked as discontinued, hence all incomplete activities associated with this prescription will be cancelled.')) {
			document.getElementById('ed_discontinued').checked = false;
			return false;
		}
	}
	var doctorId = document.getElementById('ed_doctor_id').value;
   	var doctorName = document.getElementById('ed_doctor').value;
	if (doctorName == '') {
		alert('Please enter the doctor');
		document.getElementById('ed_doctor').focus();
		return false;
	}

	if (!validate('ed')) return false;
	var itemType = document.getElementById('ed_itemType').value;
	if (itemType != 'C' && isFreqNDurationModified()) {
		if (!confirm('Frequency & Duration is modified, hence all incomplete activities associated with this prescription will be cancelled and new ones added.')) {
			editItemDialog.cancel();
			return false;
		}
	}

	var dosage = document.getElementById('ed_med_dosage').value;
	var adminStrength = document.getElementById('ed_admin_strength').value;
   	var remarks = document.getElementById('ed_remarks').value;
   	var consumption_uom = document.getElementById('ed_consumption_uom').value;
   	var interval = document.getElementById('d_repeat_interval').value;
   	var med_form_id = document.getElementById('ed_med_form_id').value;
   	var med_strength = document.getElementById('ed_med_strength').value;
   	var med_form_name = document.getElementById('ed_med_form_id').options[document.getElementById('ed_med_form_id').selectedIndex].text;
	var med_strength_units = document.getElementById('ed_med_strength_units').value;

	var freq_type = 'F';
	var freqChkObj = document.forms[prescriptionForm].ed_freq_type;
	for (var i=0; i<freqChkObj.length; i++) {
		if (freqChkObj[i].checked)
			freq_type = freqChkObj[i].value;
	}
   	var interval = document.getElementById('ed_repeat_interval').value;
   	var repeat_interval_units_text = document.getElementById('ed_repeat_interval_units').options[document.getElementById('ed_repeat_interval_units').selectedIndex].text;
   	var repeat_interval_units = document.getElementById('ed_repeat_interval_units').value;
	var frequency = document.getElementById('ed_frequency').options[document.getElementById('ed_frequency').selectedIndex].text;
   	var frequencyId = document.getElementById('ed_frequency').value;
   	frequencyId = freq_type == 'F' ? frequencyId : '';
   	interval = freq_type == 'F' ? '' : interval;
	if (frequencyId == '') frequency = '';

	var endText = '';
	var endVal = '';
	var endChkVal = '';
	var endChkObj = document.forms[prescriptionForm].ed_end_of_activity;
	for (var i=0; i<endChkObj.length; i++) {
		if (endChkObj[i].checked) {
			var val = endChkObj[i].value;
			endChkVal = val;
			if (val == 'E') {
				endVal = document.getElementById('ed_end_date').value;
				if (!empty(endVal)) endVal += " " + document.getElementById('ed_end_time').value;

				endText = endVal;
			} else if (val == 'O') {
				endVal = document.getElementById('ed_no_of_occurrences').value;
				endText = endVal + ' time(s)';
			} else if (val == 'D') {
				endVal = 'Y';
				endText = 'Till Discontinued';
			}
		}
	}

	var routeId = document.getElementById('ed_med_route').options[document.getElementById('ed_med_route').selectedIndex].value;
   	var routeName = document.getElementById('ed_med_route').options[document.getElementById('ed_med_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;

	if (itemType == 'M') {
		setNodeText(row.cells[DOSAGE], dosage + consumption_uom);
		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);

		setHiddenValue(id, "h_med_form_id", med_form_id);
		setHiddenValue(id, "h_med_strength", med_strength);
		setHiddenValue(id, "h_med_strength_units", med_strength_units);
	}

	setNodeText(row.cells[FREQUENCY], freq_type == 'F' ? frequency : (interval + ' ' + repeat_interval_units_text));
	setNodeText(row.cells[ROUTE], routeName);

	var startDateTime = document.getElementById('ed_start_date').value;
	if (!empty(startDateTime)) startDateTime += " " + document.getElementById('ed_start_time').value;

	var prescDateTime = document.getElementById('ed_prescription_date').value;
	if (!empty(prescDateTime)) prescDateTime += " " + document.getElementById('ed_prescription_time').value;


	setNodeText(row.cells[START],  startDateTime);
	setNodeText(row.cells[END], endText);
	setNodeText(row.cells[REMARKS], remarks, 30, remarks);

	setHiddenValue(id, "h_doctor_id", doctorId);
	setHiddenValue(id, "h_doctor_name", doctorName);
	setHiddenValue(id, "h_repeat_interval", interval);
	setHiddenValue(id, "h_med_dosage", dosage);
	setHiddenValue(id, "h_admin_strength", adminStrength);
	setHiddenValue(id, "h_remarks", remarks);
	setHiddenValue(id, "h_recurrence_daily_id", frequencyId);
	setHiddenValue(id, "h_freq_type", freq_type);
	setHiddenValue(id, "h_med_route", routeId);
	setHiddenValue(id, "h_route_name", routeName);
	setHiddenValue(id, "h_start_datetime", startDateTime);
	setHiddenValue(id, "h_prescription_date", prescDateTime);
	setHiddenValue(id, "h_consumption_uom", consumption_uom);
	setHiddenValue(id, "h_repeat_interval_units", freq_type == 'F' ? '' : repeat_interval_units);
	var priorMed = 'N';
	if (document.getElementById('ed_prior_medication').checked)
		priorMed = 'Y';
	setHiddenValue(id, 'h_prior_med', priorMed);

	var discontinued = 'N';
	if (document.getElementById('ed_discontinued').checked)
		discontinued = 'Y';
	setHiddenValue(id, "h_discontinued", discontinued);

	if (endChkVal == 'E') {
		setHiddenValue(id, "h_end_datetime", endVal);

		setHiddenValue(id, "h_no_of_occurrences", '');
		setHiddenValue(id, "h_end_on_discontinue", 'N');
	} else if (endChkVal == 'O') {
		setHiddenValue(id, "h_no_of_occurrences", endVal);

		setHiddenValue(id, "h_end_datetime", '');
		setHiddenValue(id, "h_end_on_discontinue", 'N');
	} else if (endChkVal == 'D') {
		setHiddenValue(id, "h_end_on_discontinue", endVal);

		setHiddenValue(id, "h_no_of_occurrences", '');
		setHiddenValue(id, "h_end_datetime", '');
	}

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("h_edited", id, 'true');
	setRowStyle(id);

	editItemDialog.cancel();
	return true;
}

var fieldEdited = false;
function setFieldEdited() {
	fieldEdited = true;
}

function openPrevious(id, previous, next) {
	var id = document.forms[prescriptionForm].editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'prescDetails');
	if (fieldEdited) {
		var flag = editTableRow();
		fieldEdited = false;
		if (!flag) return flag;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		var row = getThisRow(document.getElementsByName('_prescEditAnchor')[parseInt(id)-1]);
		while(row && row.style.display == 'none') {
			// ignore if the row is hidden.
			id = id-1;
			row = getThisRow(document.getElementsByName('_prescEditAnchor')[parseInt(id)]);
		}
		if (row)
			showEditPrescDialog(getElementByName(row, '_prescEditAnchor'));
	}
}

function openNext() {
	var id = document.forms[prescriptionForm].editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'prescDetails');
	if (fieldEdited) {
		var flag = editTableRow();
		fieldEdited = false;
		if (!flag) return flag;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id+1 != document.getElementById('prescDetails').rows.length-2) {
		var row = getThisRow(document.getElementsByName('_prescEditAnchor')[parseInt(id)+1]);
		while(row && row.style.display == 'none') {
			// ignore if the row is hidden.
			id = id+1;
			row = getThisRow(document.getElementsByName('_prescEditAnchor')[parseInt(id)]);
		}
		if (row)
			showEditPrescDialog(getElementByName(row, "_prescEditAnchor"));
	}
}




function editDialogGeneric() {
	var genericDialogDiv = document.getElementById('genericNameDisplayDialog');
	if (!genericDialogDiv) return;

	genericDialogDiv.style.visibility = 'display';
	genericDialog = new YAHOO.widget.Dialog("genericNameDisplayDialog",
			{
				width:"500px",
				context : ["loadGenInfo", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	YAHOO.util.Event.addListener("genericNameCloseBtn", "click", closeGenericDialog, genericDialog, true);
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:closeGenericDialog, scope:genericDialog, correctScope:true } );
	genericDialog.cancelEvent.subscribe(closeGenericDialog);
	genericDialog.cfg.setProperty("keylisteners", kl);
	genericDialog.render();
}

function closeGenericDialog() {
	childDialog = null;
	this.hide();
}

function showGenericInfo(index, prefix, suffix, generic_code) {
	childDialog = genericDialog;
	var anchor = document.getElementById(prefix + "genericNameAnchor" + index + "_" + suffix);
	genericDialog.cfg.setProperty("context", [anchor, "tr", "tl"], false);
	genericDialog.show();
	if (generic_code != "") {
		var ajaxReqObject = new XMLHttpRequest();
		var url = cpath+"/common/commonAjaxRequest.do?_method=getGenericJSON&generic_code="+encodeURIComponent(generic_code);
		getResponseHandlerText(ajaxReqObject, handleGenericResponse, url);
	} else {
		document.getElementById('classification_name').innerHTML = '';
		document.getElementById('sub_classification_name').innerHTML = '';
		document.getElementById('standard_adult_dose').innerHTML = '';
		document.getElementById('criticality').innerHTML = '';
		document.getElementById('generic_name').innerHTML = '';
	}
}

/*
 * Response handler for the ajax call to retrieve generic details like classification and sub-classification
 */
function handleGenericResponse(responseText) {
	if (responseText==null) return;
	if (responseText=="") return;
	var genericDetails;
    eval("var genericDetails = " + responseText);			// response is an array of item batches
    if (genericDetails != null) {
		var genericId = genericDetails.generic_code;
		document.getElementById('classification_name').innerHTML = genericDetails.classificationName;
		if (genericDetails.sub_ClassificationName != null) {
			document.getElementById('sub_classification_name').innerHTML = genericDetails.sub_ClassificationName;
		}
		document.getElementById('standard_adult_dose').innerHTML = genericDetails.standard_adult_dose;
		document.getElementById('criticality').innerHTML = genericDetails.criticality;
		document.getElementById('gen_generic_name').innerHTML = genericDetails.gmaster_name;

	}
}

function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.forms[prescriptionForm], name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[prescriptionForm], name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[prescriptionForm], name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[prescriptionForm], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

