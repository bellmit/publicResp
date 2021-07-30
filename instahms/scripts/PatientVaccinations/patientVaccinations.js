
function init() {

	initEditDosageDialog();
	initAdminsteredList();
	initAdverseReactionDialog();
	initAutoNameSearch();
}

function initEditDosageDialog() {
	var dialogDiv = document.getElementById("editDosageDialog");
	dialogDiv.style.display = 'block';
	editDosageDialog = new YAHOO.widget.Dialog("editDosageDialog", {
		width: "850px",
		text: "Edit Dosage",
		context: ["dosageTable", "tl", "tl"],
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys: 27 },
		{
			fn: handleEditCancel,
			scope: editDosageDialog,
			correctScope: true
		});
	editDosageDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editDosageDialog.cancelEvent.subscribe(handleEditCancel);
	YAHOO.util.Event.addListener('editOk', 'click', editDosageTableRow, editDosageDialog, true);
	YAHOO.util.Event.addListener('editCancel', 'click', handleEditCancel, editDosageDialog, true);
	YAHOO.util.Event.addListener('editPrevious', 'click', openDosagePrevious, editDosageDialog, true);
	YAHOO.util.Event.addListener('editNext', 'click', openDosageNext, editDosageDialog, true);
	editDosageDialog.render();
}
var doctorAutoComp = null;
var batchAutoComp = null;


function initAdminsteredList() {
	if (!empty(doctorAutoComp)) {
		doctorAutoComp.destroy();
		doctorAutoComp = null;
	}
	var dataSource = new YAHOO.util.LocalDataSource({ result: doctors });
	dataSource.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList: 'result',
		fields: [
			{ key: 'doctor_name' },
			{ key: 'doctor_id' }
		]
	};
	var doctorAutoComp = new YAHOO.widget.AutoComplete('administered_name', 'administered_dropdown', dataSource);
	doctorAutoComp.minQueryLength = 0;
	doctorAutoComp.animVert = false;
	doctorAutoComp.maxResultsDisplayed = 50;
	doctorAutoComp.allowBrowserAutocomplete = false;
	doctorAutoComp.resultTypeList = false;
	doctorAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	doctorAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	doctorAutoComp.itemSelectEvent.subscribe(setDoctorId);
	doctorAutoComp.unmatchedItemSelectEvent.subscribe(removeDoctorId);
	if (doctorAutoComp._elTextbox.value != '') {
		doctorAutoComp._bItemSelected = true;
		doctorAutoComp._sInitInputValue = doctorAutoComp._elTextbox.value;
	}

}

function initAdverseReactionDialog() {
    var dialogDiv = document.getElementById("adverseReactionDialog");
	dialogDiv.style.display = 'block';
	adverseReactionDialog = new YAHOO.widget.Dialog("adverseReactionDialog",{
			width:"600px",
			text: "Adverse Reaction Monitoring",
			context :["dosageTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelAddAdverseReaction,
	                                                scope:adverseReactionDialog,
	                                                correctScope:true } );
	adverseReactionDialog.cfg.queueProperty("keylisteners", escKeyListener);
	adverseReactionDialog.cancelEvent.subscribe(cancelAddAdverseReaction);
	YAHOO.util.Event.addListener('adverseReactionOk', 'click', addAdverseReaction, adverseReactionDialog, true);
	YAHOO.util.Event.addListener('adverseReactionCancel', 'click', cancelAddAdverseReaction, adverseReactionDialog, true);
	populateInitialSymptomData();
	adverseReactionDialog.render();
}

function timeNow(returnUtcString) {
    var correctionToApply = 0;
    if (!window.serverTS) {
        if (window.serverTS != null) {
            correctionToApply = window.serverTS.getTime() - window.localTS.getTime();
        }
    }
    var currentTS = new Date();
    if (returnUtcString) {
        return (new Date(Date.UTC(currentTS.getUTCFullYear(), currentTS.getUTCMonth(), currentTS.getUTCDate(), currentTS.getUTCHours(), currentTS.getUTCMinutes(), currentTS.getUTCSeconds(), 0) + correctionToApply).format("yyyy-mm-dd HH:MM:ss", true));
    } else {
        return new Date(currentTS.getTime() + correctionToApply);
    }
}

function populateInitialSymptomData() {
    var vaccineSymptomSeverityMapping = JSON.parse(vaccineSymptomSeverityMappingJSON);
    if (vaccineSymptomSeverityMapping) {
        var adverseReactionIds = Object.keys(vaccineSymptomSeverityMapping);
        if(vaccineSymptomSeverityMapping) {
            if(adverseReactionIds) {
                adverseReactionIds.forEach((adverseId, index) => {
                        vaccineSymptomSeverityMapping[adverseId].forEach((ssm) => {
                        var symptomListId = ssm.adverse_reaction_symptoms_list_id;

                        var elementId = "symptom_name_"+symptomListId;
                        document.getElementsByName(elementId)[index].value = ssm.adverse_reaction_symptoms_list_id;

                        var typeofReactionElementId = "severity_of_reaction_"+symptomListId;
                        document.getElementsByName(typeofReactionElementId)[index].value = ssm.severity_of_reaction_id;

                        var occurrencesElementId = "occurrences_"+symptomListId;
                        document.getElementsByName(occurrencesElementId)[index].value = ssm.number_of_occurrences;

                        document.getElementsByName("adverse_symptom_severity_id_"+symptomListId)[index].value = ssm.adverse_symptom_severity_id;

                        document.getElementById(elementId).onchange = () => handleSymptomNameChange(elementId, typeofReactionElementId, occurrencesElementId);
                    })
                })
            }
        }
    }
}

var dateTimeFormat = 'DD-MM-YYYY HH:mm'
function validateDateAndTimeMoment(startDateTime, endDateTime) {
    var momentStartDate = moment(startDateTime, dateTimeFormat, true);
    var momentEndDate = moment(endDateTime, dateTimeFormat, true);
    return  momentEndDate.isSameOrBefore(momentStartDate);
}

function handleSymptomNameChange(symptomNameId, typeofReactionElementId, occurrencesElementId) {
    var isSymptomSelected = document.getElementById(symptomNameId).checked;
    if (!isSymptomSelected) {
        document.getElementById(typeofReactionElementId).value = "";
        document.getElementById(typeofReactionElementId).disabled = true;

        document.getElementById(occurrencesElementId).value = "";
        document.getElementById(occurrencesElementId).disabled = true;
    } else {
        document.getElementById(typeofReactionElementId).value = "";
        document.getElementById(typeofReactionElementId).disabled = false;
        document.getElementById(occurrencesElementId).value = "";
        document.getElementById(occurrencesElementId).disabled = false;
    }
}

function setDoctorId(oSelf, elItem) {
	document.getElementById('d_administered_by_id').value = elItem[2].doctor_id;
	document.getElementById('d_administered_by_name').value = elItem[2].doctor_name;
}

function removeDoctorId() {
	document.getElementById('d_administered_by_id').value = '';
	document.getElementById('d_administered_by_name').value = '';
}

var parentDosageDialog = null;
var childDialog = null;
var dosageFieldEdited = false;

function onchangeFields() {
	dosageFieldEdited = true;
}

function handleEditCancel() {
	if (childDialog == null) {
		var id = document.vaccinationForm.dosage_editRowId.value;
		var row = getChargeRow(id, "dosageTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		parentDosageDialog = null;
		dosageFieldEdited = false;
		clearItemDetails();
		this.hide();
	}
}

function cancelAddAdverseReaction() {
    var id = document.vaccinationForm.dosage_adverseReactionId.value;
    var row = getChargeRow(id, "dosageTable");
    YAHOO.util.Dom.removeClass(row, 'addAdverseReaction');

    // To reset any value if previous stored element so that it will not be carry forwarded
    var symptomsList = JSON.parse(symptomsListJSON);

    if(symptomsList) {
        symptomsList.forEach((symptom, index) => {
            var symptomId = symptom.id;
            var symptomElementId = "symptom_name_"+symptomId;
            var isSymptomChecked = document.getElementById(symptomElementId).checked;
            if(isSymptomChecked) {
                document.getElementById(symptomElementId).checked = false;

                var occurrencesElementId = "occurrences_"+symptomId;
                document.getElementById(occurrencesElementId).value = "";

                var typeofReactionElementId = "severity_of_reaction_"+symptomId;
                document.getElementById(typeofReactionElementId).value = "";

                document.getElementById("adverse_symptom_severity_id_"+symptomId).value = "";
            }
        })
    }
    this.hide();
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.vaccinationForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.vaccinationForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function showEditDosageDialog(obj) {
	parentDialog = editDosageDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editDosageDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editDosageDialog.show();
	enableAndDisableStatus();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.vaccinationForm.dosage_editRowId.value = id;

	var patientVaccinationId = getIndexedValue('pat_vacc_id',id);
	document.getElementById('dosageModal').textContent = patientVaccinationId ? 'Edit Dose Details' : 'Add Dose Details';

	document.getElementById('administered_name').value = getIndexedValue('vacc_doctor_name', id);
	document.getElementById('d_vaccine_label').textContent = getIndexedValue("vaccine_name", id);
	document.getElementById('d_vaccine_dose_label').textContent = getIndexedValue("dose_num", id);
	if (!getIndexedValue("dose_num", id)) {
		document.getElementById('vaccine_dose_label').style.visibility = 'hidden';
		document.getElementById('d_vaccine_dose_label').style.visibility = 'hidden';
	} else {
	    document.getElementById('vaccine_dose_label').style.visibility = 'visible';
        document.getElementById('d_vaccine_dose_label').style.visibility = 'visible';
	}
	document.getElementById('d_dosage_due_date_label').textContent = getIndexedValue("due_date", id);
	document.getElementById('d_vaccination_status').value = getIndexedValue("vaccination_status", id);
	document.getElementById('d_vaccination_reason').value = getIndexedValue("reason_for_not", id);
	document.getElementById('d_med_name').value = getIndexedValue('med_name', id);
	document.getElementById('d_manufacturer').value = getIndexedValue('manufacturer', id);

	document.getElementById('d_batch_text').value = getIndexedValue('batch', id);
	document.getElementById('d_expiry_date').value = getIndexedValue('expiry_date', id);
	var currentDateTime = moment(timeNow()).format(dateTimeFormat);
	var vaccinationDate = getIndexedValue('vaccination_date', id);
	if (vaccinationDate) {
	  document.getElementById('d_adminstered_date').value = vaccinationDate;
	} else {
	  document.getElementById('d_adminstered_date').value = currentDateTime.split(' ')[0];
	}
	var vaccinationTime = getIndexedValue('vaccination_time', id);
	if (vaccinationTime) {
	  document.getElementById('d_administered_time').value = vaccinationTime;
	} else {
	  document.getElementById('d_administered_time').value = currentDateTime.split(' ')[1];
	}
	document.getElementById('d_administered_by_name').value = getIndexedValue('vacc_doctor_name', id);
	document.getElementById('d_administered_by_id').value = getIndexedValue('vacc_doctor_id', id);
	document.getElementById('d_remarks').value = getIndexedValue('remarks', id);

	enableAndDisableStatus(getIndexedValue("vaccination_status", id));
	fetchVaccineCategoryList(getIndexedValue("vaccine_id", id), getIndexedValue("vaccine_category_id", id));
	//To set Item related details
	var medicineId = getIndexedValue('medicine_id', id);
	if (medicineId) {
		document.getElementById('d_medicine_id').value = medicineId;
		document.getElementById('d_manufacturer').disabled = true;
		document.getElementById('outsidehospital').checked = false;
		enableFields();
		document.getElementById('d_cons_uom_id').value = getIndexedValue('cons_uom_id', id);
		document.getElementById('d_dosage_qty').value = getIndexedValue('medicine_quantity', id);
		document.getElementById('d_site_id').value = getIndexedValue('site_id', id);
		setMedicineRoutes(getIndexedValue('route_of_admin', id), getIndexedValue('medicine_id', id));
		setBatchAutoCompleteList(mrNo, medicineId)
	}
	else if (getIndexedValue("d_med_name", id)) {
		document.getElementById('outsidehospital').checked = true;
		enableFields();
		document.getElementById('d_manufacturer').disabled = false;
		document.getElementById('d_batch_text').value = getIndexedValue('batch', id);
		document.getElementById('d_expiry_date').value = getIndexedValue('expiry_date', id);
		document.getElementById('d_manufacturer').value = getIndexedValue('manufacturer', id);
	}
	else {
		document.getElementById('outsidehospital').checked = false;
		enableFields();
		document.getElementById('d_manufacturer').disabled = false;

	}

	// To Hide Dosage when modal is closed
	document.getElementById('vaccine_dose_label').style.visibility = 'hidden';
    document.getElementById('d_vaccine_dose_label').style.visibility = 'hidden';
	return false;
}

function showAdverseReactionDialogue(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	adverseReactionDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	adverseReactionDialog.show();
	YAHOO.util.Dom.addClass(row, 'addAdverseReaction');

    var symptomsList = JSON.parse(symptomsListJSON);
    if (symptomsList) {
        symptomsList.forEach((symptom, index) => {
            var symptomId = symptom.id;
            var symptomElementId = "symptom_name_"+symptomId;
            var selectedSymptomId = parseInt(getIndexedValue(symptomElementId, id));
            var isSymptomChecked = symptomId === selectedSymptomId;
            if(isSymptomChecked) {
                document.getElementById(symptomElementId).checked = true;
                var occurrencesElementId = "occurrences_"+symptomId;
                document.getElementById(occurrencesElementId).value = getIndexedValue(occurrencesElementId, id);

                var typeofReactionElementId = "severity_of_reaction_"+symptomId;
                document.getElementById(typeofReactionElementId).value = getIndexedValue(typeofReactionElementId, id);
            }
        })
    }

	document.vaccinationForm.dosage_adverseReactionId.value = id;

	document.getElementById('adverse_reaction_monitoring_for').value = getIndexedValue('adverse_reaction_monitoring_for_id', id);
	document.getElementById('adverse_reaction_onset').value = getIndexedValue('adverse_reaction_onset_id', id);
	document.getElementById('adverse_reaction_corelation').value = getIndexedValue('adverse_reaction_corelation_id', id);
	document.getElementById('adverse_reaction_actions').value = getIndexedValue('adverse_reaction_actions_id', id);
    document.getElementById('adverse_start_date_id').value = getIndexedValue('adverse_reaction_start_date', id);
    document.getElementById('adverse_start_time_id').value = getIndexedValue('adverse_reaction_start_time', id);
    document.getElementById('adverse_end_date_id').value = getIndexedValue('adverse_reaction_end_date', id);
    document.getElementById('adverse_end_time_id').value = getIndexedValue('adverse_reaction_end_time', id);
    document.getElementById('adverse_remarks_id').value = getIndexedValue('adverse_remarks', id);
    var adverseStartDate = getIndexedValue('adverse_reaction_start_date', id);
    if(!adverseStartDate) {
        var momentCurrentDate = moment(timeNow()).format(dateTimeFormat).split(" ");
        var startDate = momentCurrentDate[0];
        var startTime = momentCurrentDate[1];
        document.getElementById('adverse_start_date_id').value = startDate;
        document.getElementById('adverse_start_time_id').value = startTime;
    }
	// add diagloue
	return false;
}

function disableAutoCompleteForEditDosage() {
    var isVaccineAdministeredOutside = document.getElementById('outsidehospital').checked;
    if(isVaccineAdministeredOutside) {
        document.getElementById('batch_dropdown').style.visibility = 'hidden';
        document.getElementById('medicinename_dropdown').style.visibility = 'hidden';
    } else {
        document.getElementById('batch_dropdown').style.visibility = 'visible';
        document.getElementById('medicinename_dropdown').style.visibility = 'visible';
    }
}

var D_VACCINE_NAME = 0, D_DOSE = 1, D_RECOMMENDED_AGE = 2, D_DUE_DATE = 3, D_VACCINE_STATUS = 4, D_REASON = 5, D_ADMNS_DATE = 6,
	D_ADMNS_BY = 7, D_MED_MANF_BATCH_EXP = 8, D_REMARKS = 9

function editDosageTableRow() {
	var id = document.vaccinationForm.dosage_editRowId.value;
	var row = getChargeRow(id, 'dosageTable');


	var statusText = document.getElementById('d_vaccination_status').options[document.getElementById('d_vaccination_status').selectedIndex].text;
	var statusValue = document.getElementById('d_vaccination_status').options[document.getElementById('d_vaccination_status').selectedIndex].value;
	var vaccinationReason = trim(document.getElementById('d_vaccination_reason').value);
	var medicineName = document.getElementById('d_med_name').value;
	var medicineId = document.getElementById('d_medicine_id').value;
	var manufacturer = document.getElementById('d_manufacturer').value;
	var batch = document.getElementById('d_batch_text').value;
	var expiryDate = document.getElementById('d_expiry_date').value;
	var administeredDate = document.getElementById('d_adminstered_date').value;
	var administeredTime = document.getElementById('d_administered_time').value;
	var administeredName = document.getElementById('d_administered_by_name').value;
	var remarks = document.getElementById('d_remarks').value;
	var administeredId = document.getElementById('d_administered_by_id').value;
	var administeredText = document.getElementById('administered_name').value;
	var routeOfAdmin = document.getElementById('d_medicine_route').value;
	var dosageUnit = document.getElementById('d_cons_uom_id').value;
	var dosageQty = document.getElementById('d_dosage_qty').value;
	var siteOfAdministration = document.getElementById('d_site_id').value;
	var vaccineCategory = document.getElementById('d_vaccine_category').value;
	var isOutsideHospital = document.getElementById('outsidehospital').checked;

	if (administeredId == '') {
		administeredId = administeredText;
		administeredName = administeredText;
	}

	var currentDateTime = moment(timeNow()).format(dateTimeFormat);
    var administeredDateTime = administeredDate + " " + administeredTime;
    var administeredDateTimeMoment = moment(administeredDateTime, dateTimeFormat, true);

    if(!moment(expiryDate, 'DD-MM-YYYY', true).isValid()) {
        alert('Please Enter Valid Expiry Date in DD-MM-YYYY');
        return false;
    }

    if (!administeredDateTimeMoment.isValid()) {
        alert('Please Enter Valid Administered Date in '+dateTimeFormat);
        return false;
    }

    if (!validateDateAndTimeMoment(currentDateTime, administeredDateTime)) {
        alert('Administered Date is Future Date !!');
        return false;
    }

    var expiryDateTime = expiryDate + " 23:59";
    if (!validateDateAndTimeMoment(expiryDateTime, currentDateTime)) {
        alert("Expiry Date Can't be past");
        return false;
    }

	if (statusValue == 'A') {

		if (vaccineCategory == '') {
			alert('Vaccine Category cannot be empty');
			document.getElementById('d_vaccine_category').focus();
			return false;
		}

		if (administeredDate == '') {
			alert('Please enter Administered Date..');
			document.getElementById('d_adminstered_date').focus();
			return false;
		}
		if (administeredTime == '') {
			alert('Please enter Administered Time..');
			document.getElementById('d_administered_time').focus();
			return false;
		}
		if (!validateTime(document.getElementById('d_administered_time'))) {
			alert('Please enter valid Administered Time');
			document.getElementById('d_administered_time').focus();
			return false;
		}

		if (administeredName == '') {
			alert('Please enter Administered By..');
			document.getElementById('d_administered_by_name').focus();
			return false;
		}
		if (!isOutsideHospital) {
            if (!validateItemRelatedFields()) {
                return false;
            }
		} else {
		    medicineId = '';
		    dosageUnit = '';
            dosageQty = '';
		    routeOfAdmin = '';
		    siteOfAdministration = '';
		}
		vaccinationReason = '';

	} else if (statusValue == 'N') {
		if (vaccinationReason == '') {
			alert("Please enter reason for not administering..");
			return false;
		}
	} else if (statusValue == '') {
		statusText = '';
		vaccinationReason = '';
		administeredDate = '';
		administeredTime = '';
		administeredName = '';
		medicineName = '';
		manufacturer = '';
		batch = '';
		expiryDate = '';
		remarks = '';
		clearItemDetails();
		document.getElementById('d_manufacturer').disabled = false;
        medicineId = '';
        dosageUnit = '';
        dosageQty = '';
        routeOfAdmin = '';
        siteOfAdministration = '';
	}

	setNodeText(row.cells[D_VACCINE_STATUS], statusText, 15);
	setNodeText(row.cells[D_REASON], vaccinationReason, 10);
	setNodeText(row.cells[D_ADMNS_DATE], administeredDate);
	setNodeText(row.cells[D_ADMNS_BY], administeredName, 15);
	setNodeText(row.cells[D_MED_MANF_BATCH_EXP], medicineName + "/" + manufacturer + "/" + batch + "/" + expiryDate, 20);
	setNodeText(row.cells[D_REMARKS], remarks, 20);

	setHiddenValue(id, "vaccine_category_id", vaccineCategory);
	setHiddenValue(id, "vaccination_status", statusValue);
	setHiddenValue(id, "reason_for_not", vaccinationReason);
	setHiddenValue(id, "med_name", medicineName);
	setHiddenValue(id, "medicine_id", medicineId);
	setHiddenValue(id, "vaccination_date", administeredDate);
	setHiddenValue(id, "vaccination_time", administeredTime)
	setHiddenValue(id, "vacc_doctor_id", administeredId);
	setHiddenValue(id, "vacc_doctor_name", administeredName);
	setHiddenValue(id, "expiry_date", expiryDate);
	setHiddenValue(id, "batch", batch);
	setHiddenValue(id, "manufacturer", manufacturer);
	setHiddenValue(id, "remarks", remarks);
	setHiddenValue(id, "route_of_admin", routeOfAdmin);
	setHiddenValue(id, "cons_uom_id", dosageUnit);
	setHiddenValue(id, "medicine_quantity", dosageQty);
	setHiddenValue(id, "site_id", siteOfAdministration);
	setHiddenValue(id, "isEdited", true);
	setHiddenValue(id, "isOutsideHospital", isOutsideHospital ? 'Y' : 'N');
	YAHOO.util.Dom.removeClass(row, 'editing');

	editDosageDialog.cancel();

	// To Hide the Dosage label when modal is closed
    document.getElementById('vaccine_dose_label').style.visibility = 'hidden';
    document.getElementById('d_vaccine_dose_label').style.visibility = 'hidden';
	return true;

}


function addAdverseReaction() {
    var id = document.vaccinationForm.dosage_adverseReactionId.value;
    var row = getChargeRow(id, 'dosageTable');
    var adverseReactionMonitoringFor = document.getElementById('adverse_reaction_monitoring_for').value;
    var adverReactionOnset = document.getElementById('adverse_reaction_onset').value;
    var adverseReactionCoRelation = document.getElementById('adverse_reaction_corelation').value;
    var adverseReactionActions = document.getElementById('adverse_reaction_actions').value;
    var adverseReactionStartDate = document.getElementById('adverse_start_date_id').value;
    var adverseReactionStartTime = document.getElementById('adverse_start_time_id').value;
    var adverseReactionEndDate = document.getElementById('adverse_end_date_id').value;
    var adverseReactionEndTime = document.getElementById('adverse_end_time_id').value;
    var adverseReactionRemarks = document.getElementById('adverse_remarks_id').value;

    var startDateTime = adverseReactionStartDate + " " + adverseReactionStartTime;
    var endDateTime;
    if (adverseReactionEndDate) {
        endDateTime = adverseReactionEndDate + " " + adverseReactionEndTime;
    }

    if (!moment(startDateTime, dateTimeFormat, true).isValid()) {
        alert('Adverse Reaction Start Date & Time is Not Valid \n Enter in '+ dateTimeFormat +' format');
        return;
    }

    if (endDateTime && !moment(endDateTime, dateTimeFormat, true).isValid()) {
        alert('Adverse Reaction End Date & Time is Not Valid \n Enter in '+ dateTimeFormat +' format');
        return;
    }

    if (!validateDateAndTimeMoment(endDateTime, startDateTime)) {
        alert('Adverse Reaction Start Date is greater than End Date');
        return;
    }

    var vaccinatedDate = getIndexedValue('vaccination_date', id);
    var vaccinatedTime = getIndexedValue('vaccination_time', id);
    var vaccinatedDateTime = moment(vaccinatedDate+" "+vaccinatedTime, dateTimeFormat);

    if (validateDateAndTimeMoment(vaccinatedDateTime, startDateTime)) {
        alert('Adverse Reaction Start Date cannot be less than Administered Date');
        return;
    }

    if(endDateTime) {
        var adverseReactionEndDateTime = adverseReactionEndDate + " "+adverseReactionEndTime;
        var currentDateTime = moment(timeNow()).format(dateTimeFormat);
        if (!validateDateAndTimeMoment(currentDateTime, adverseReactionEndDateTime)) {
            alert('Adverse Reaction End Date cannot be more than Current Date');
            return;
        }
    }


    var vaccineSymptomSeverityMapping = JSON.parse(vaccineSymptomSeverityMappingJSON);
    var adverseReactionMonitoringForId = getIndexedValue('adverse_reaction_monitoring_for_id', id);

    var symptomsList = JSON.parse(symptomsListJSON);

    if(symptomsList) {
        symptomsList.forEach((symptom, index) => {
            var symptomId = symptom.id;
            var symptomElementId = "symptom_name_"+symptomId;
            var isSymptomChecked = document.getElementById(symptomElementId).checked;
            var adverseReactionSymptomId = getIndexedValue("adverse_symptom_severity_id_"+symptomId, id);
            if(isSymptomChecked) {
                setHiddenValue(id, symptomElementId, symptomId);

                var occurrencesElementId = "occurrences_"+symptomId;
                var numberOfOccurrences = document.getElementById(occurrencesElementId).value;
                setHiddenValue(id, occurrencesElementId, numberOfOccurrences);

                var typeofReactionElementId = "severity_of_reaction_"+symptomId;
                var typeOfReactionId = document.getElementById(typeofReactionElementId).value;
                setHiddenValue(id, typeofReactionElementId, typeOfReactionId);
            } else if (!isSymptomChecked && parseInt(adverseReactionSymptomId) > 0) {
                setHiddenValue(id, "to_be_deleted_"+symptomId, true);
            }
        })
    }

    setHiddenValue(id, "adverse_reaction_monitoring_for_id", adverseReactionMonitoringFor);
    setHiddenValue(id, "adverse_reaction_onset_id", adverReactionOnset);
    setHiddenValue(id, "adverse_reaction_corelation_id", adverseReactionCoRelation);
    setHiddenValue(id, "adverse_reaction_actions_id", adverseReactionActions);
    setHiddenValue(id, "adverse_remarks", adverseReactionRemarks);
    setHiddenValue(id, "adverse_reaction_start_date", adverseReactionStartDate);
    setHiddenValue(id, "adverse_reaction_start_time", adverseReactionStartTime);
    setHiddenValue(id, "adverse_reaction_end_date", adverseReactionEndDate);
    setHiddenValue(id, "adverse_reaction_end_time", adverseReactionEndTime);

    YAHOO.util.Dom.removeClass(row, 'addAdverseReaction');
    adverseReactionDialog.cancel();
    return true;
}

function openDosagePrevious() {
	var id = document.vaccinationForm.dosage_editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'dosageTable');
	if (!editDosageTableRow())
		return false;

	if (id != 0) {
		showEditDosageDialog(document.getElementsByName('dosage_editAnchor')[parseInt(id) - 1]);
	}

}

function openDosageNext() {

	var id = document.vaccinationForm.dosage_editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'dosageTable');
	if (!editDosageTableRow())
		return false;

	if (id + 1 != document.getElementById('dosageTable').rows.length - 1) {
		showEditDosageDialog(document.getElementsByName('dosage_editAnchor')[parseInt(id) + 1]);
	}
}

function validate() {

	if (mrNo == '') {
		alert('Please enter mrno');
		return false;
	}

	if (document.getElementById('dosageTable').rows.length == 1) {
		alert("There is no records to save in the grid" + "\n" + "pls add one or more records.");
		return false;
	}

	return true;
}

function validateItemRelatedFields() {

	if (document.getElementById('d_dosage_qty').value == '') {
		alert("Dosage quantity ccannot be empty")
	}

	if (!validateInteger(document.getElementById('d_dosage_qty'), "Dosage quantity must be a valid number")) {
		return false;
	}

	if (document.getElementById('d_cons_uom_id').value == '') {
		alert("Dosage unit cannot be empty");
		return false;
	}

	if (document.getElementById('d_site_id').value == '') {
		alert("Site of adminisration cannot be empty");
		return false;
	}

	if (document.getElementById('d_medicine_route').value == '') {
		alert("Route of adminisration cannot be empty");
		return false;
	}

	if (document.getElementById('d_expiry_date').value == '') {
		alert("Batch Expiry Date cannot be empty");
		return false;
	}

	if (document.getElementById('d_batch_text').value == '') {
		alert("Batch cannot be empty");
		return false;
	}
	return true;
}

function enableAndDisableStatus(value) {
	var status = null;
	if (value == null)
		status = document.getElementById('d_vaccination_status').value;
	else
		status = value;

	if (status == 'N') {
		document.getElementById('vaccination_reason_tdid1').style.display = '';
		document.getElementById('vaccination_reason_tdid2').style.display = '';
	} else {
		document.getElementById('vaccination_reason_tdid1').style.display = 'none';
		document.getElementById('vaccination_reason_tdid2').style.display = 'none';
	}

}

function fetchVaccineCategoryList(vaccineId, selectedVaccineCategory) {
	var url = cpath + "/vaccinationsinfo/vaccineCategoryList.json?vaccine_id=" + vaccineId;
	var reqObject = newXMLHttpRequest();
	reqObject.onreadystatechange = function () {
	    if(this.readyState === 4 && this.status === 200) {
			setVaccineCategoryList(reqObject.responseText, selectedVaccineCategory);
	    }
	}
	reqObject.open("GET", url.toString(), true);
	reqObject.send(null);
}

function setVaccineCategoryList(response, selectedVaccineCategory) {
	var categoryList = JSON.parse(response).vaccineCategoryList;
	var vaccine_category_el = document.getElementById('d_vaccine_category');
	vaccine_category_el.length = 1; // clear the previously populated list
	var len = 1;
	for (var i = 0; i < categoryList.length; i++) {

		vaccine_category_el.length = len + 1;
		vaccine_category_el.options[len].value = categoryList[i].vaccine_category_id;
		vaccine_category_el.options[len].text = categoryList[i].vaccine_category_name;
		len++;
	}
	if (categoryList.length === 1) {
	    vaccine_category_el.selectedIndex = 1;
	} else {
	    vaccine_category_el.selectedIndex = selectedVaccineCategory ? selectedVaccineCategory : 0;
	}
}

function setMedicineRoutes(routeId, medicineId) {
	var itemRoutes = JSON.parse(getItemRoutes(medicineId));
	var medicine_route_el = document.getElementById('d_medicine_route');
	if (itemRoutes.route_id.length > 0) {
		var routeIds = itemRoutes.route_id.split(",");
		var routeNames = itemRoutes.route_name.split(",");
		medicine_route_el.length = 1; // clear the previously populated list
		var len = 1;
		for (var i = 0; i < routeIds.length; i++) {
			if (routeIds[i].trim() != '') {
				medicine_route_el.length = len + 1;
				medicine_route_el.options[len].value = routeIds[i].trim();
				medicine_route_el.options[len].text = routeNames[i];
				len++;
			}
		}
		setSelectedIndex(document.getElementById('d_medicine_route'), routeId);
	}
	else {
		var len = 1;
		for (var i = 0; i < routeOfAdminList.length; i++) {
			medicine_route_el.length = len + 1;
			medicine_route_el.options[len].value = routeOfAdminList[i].route_id;
			medicine_route_el.options[len].text = routeOfAdminList[i].route_name;
			len++;
		}
		setSelectedIndex(document.getElementById('d_medicine_route'), routeId);
	}
}

function clearItemDetails() {
	document.getElementById('d_manufacturer').value = '';
	document.getElementById('d_manufacturer').disabled = false;
	document.getElementById('d_medicine_id').value = '';
	document.getElementById('d_cons_uom_id').value = '';
	document.getElementById('d_medicine_route').value = '';
	document.getElementById('d_dosage_qty').value = '';
	document.getElementById('d_site_id').value = '';
	document.getElementById('d_batch_text').value = '';
	document.getElementById('d_expiry_date').value = '';
}

function enableFields() {
	var isOutsideHospitalMedicine = document.getElementById('outsidehospital').checked;
	var visibility = isOutsideHospitalMedicine ? 'none' : '';
	document.getElementById('vaccine_route_label').style.display = visibility;
	document.getElementById('d_medicine_route').style.display = visibility;
	document.getElementById('vaccine_site_label').style.display = visibility;
	document.getElementById('d_site_id').style.display = visibility;
	document.getElementById('vaccine_dosageqty_label').style.display = visibility;
	document.getElementById('d_dosage_qty').style.display = visibility;
	document.getElementById('d_cons_uom_id').style.display = visibility;
	if(isOutsideHospitalMedicine) {
	    document.getElementById('d_manufacturer').disabled = false;
	    clearItemDetails();
	}
	
	disableAutoCompleteForEditDosage()
}

function onChangeAdministeredType() {
	
	document.getElementById('d_med_name').value = '';
	document.getElementById('d_manufacturer').value='';
	document.getElementById('d_batch_text').value = '';
	document.getElementById('d_expiry_date').value = '';
	document.getElementById('d_adminstered_date').value = '';
	document.getElementById('d_administered_time').value = '';
	document.getElementById('d_administered_by_name').value = '';
	document.getElementById('administered_name').value = '';
	document.getElementById('d_administered_by_id').value = '';
	document.getElementById('d_remarks').value = '';
	
	enableFields();
}

function initAutoNameSearch() {
	if (!empty(AutoComp)) {
		AutoComp.destroy();
		AutoComp = null;
	}
	if(document.getElementById('outsidehospital').checked){ 
		return;
		}
	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/vaccinationsinfo/searchItems.json");
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	dataSource.responseSchema = {
		resultsList: "items",
		fields: [{ key: "medicine_name" },
		{ key: "medicine_id" },
		{ key: "manf_name" },
		{ key: "cons_uom_id" }
		]
	};

	itemNamesArray = dataSource;
	var AutoComp = new YAHOO.widget.AutoComplete('d_med_name', 'medicinename_dropdown', dataSource);
	AutoComp.allowBrowserAutocomplete = false;
	AutoComp.prehighlightClassName = "yui-ac-prehighlight";
	AutoComp.typeAhead = false;
	AutoComp.useShadow = false;
	AutoComp.animVert = false;
	AutoComp.minQueryLength = 1;
	AutoComp.forceSelection = false;
	AutoComp.filterResults = Insta.queryMatchWordStartsWith;

	AutoComp.formatResult = Insta.autoHighlight;
	AutoComp.dataRequestEvent.subscribe(clearItemDetails);
	AutoComp.itemSelectEvent.subscribe(onSelectMedicine);
}




function onSelectMedicine(type, oArgs) {
	var record = oArgs[2];
	var medicineName = record[0];
	var medicineId = record[1];
	var manufacturer = record[2];
	var consumptionUOM = record[3];
	document.getElementById('d_med_name').value = medicineName;
	document.getElementById('d_manufacturer').value = manufacturer;
	document.getElementById('d_manufacturer').disabled = true;
	if (consumptionUOM) {
		document.getElementById('d_cons_uom_id').value = consumptionUOM;
	}
	document.getElementById('d_medicine_id').value = medicineId;
	var itemRoutes = JSON.parse(getItemRoutes(medicineId));
	var medicine_route_el = document.getElementById('d_medicine_route');
	if (itemRoutes.route_id.length > 0) {
		var routeIds = itemRoutes.route_id.split(",");
		var routeNames = itemRoutes.route_name.split(",");
		medicine_route_el.length = 1; // clear the previously populated list
		var len = 1;
		for (var i = 0; i < routeIds.length; i++) {
			if (routeIds[i].trim() != '') {
				medicine_route_el.length = len + 1;
				medicine_route_el.options[len].value = routeIds[i].trim();
				medicine_route_el.options[len].text = routeNames[i];
				len++;
			}
		}
		medicine_route_el.selectedIndex = 1;
	}
	else {
		var len = 1;
		for (var i = 0; i < routeOfAdminList.length; i++) {
			medicine_route_el.length = len + 1;
			medicine_route_el.options[len].value = routeOfAdminList[i].route_id;
			medicine_route_el.options[len].text = routeOfAdminList[i].route_name;
			len++;
		}
		medicine_route_el.selectedIndex = (medicine_route_el.length == 2 ? 1 : 0); // if only one route found, then default it.
	}

	setBatchAutoCompleteList(mrNo, medicineId);
}

function onSelectBatch(type, oArgs) {
	document.getElementById('d_batch_text').value = oArgs[2][0];
	document.getElementById('d_expiry_date').value = oArgs[2][1];
}

function getItemRoutes(medItemId) {
	if (!empty(medItemId)) {
		var url = cpath + "/vaccinationsinfo/itemRoutes.json?medicine_id="
			+ medItemId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("GET", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return reqObject.responseText;
			}
		}
	}
	return null;
}

function getItemBatchDetails(mrNo, medItemId) {
	if (!empty(medItemId)) {
		var url = cpath + "/vaccinationsinfo/itemBatchDetails.json";
		var urlParams = 'mr_no=' + mrNo + '&medicine_id=' + medItemId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("GET", url.toString() + '?' + urlParams.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return reqObject.responseText;
			}
		}
	}
	return null;
}

function setBatchAutoCompleteList(mrNo, medicineId) {
    	var batchDetails = getItemBatchDetails(mrNo, medicineId);
    	var dataSource = new YAHOO.widget.DS_JSArray(batchDetails);
    	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    	dataSource.responseSchema = {
    		resultsList: "batchDetails",
    		fields: [{ key: "batch_no" },
    		{ key: "exp_dt" }
    		]
    	};
    	var batchAutoComp = new YAHOO.widget.AutoComplete('d_batch_text', 'batch_dropdown', dataSource);
    	batchAutoComp.prehighlightClassName = "yui-ac-prehighlight";
    	batchAutoComp.typeAhead = false;
    	batchAutoComp.allowBrowserAutocomplete = false;
    	batchAutoComp.minQueryLength = 0;
    	batchAutoComp.maxResultsDisplayed = 20;
    	batchAutoComp.autoHighlight = false;
    	batchAutoComp.forceSelection = false;
    	batchAutoComp.animVert = false;
    	batchAutoComp.itemSelectEvent.subscribe(onSelectBatch);
}
