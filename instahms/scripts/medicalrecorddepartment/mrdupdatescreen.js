function init() {
    initAddOrEditDiagnosisCode();
    initTrtDialog();
    initEncDialog();
    initDrgDialog();
    initLoincDialog();
    initAddOrEditConsulCode();
    initInfoDialogs();
    initDiagnosisHistoryDialog();
    initPrimaryInsurancePhotoDialog();
}

function openConsultationPrint(obj, url) {
	var templateName = document.getElementById('consTemplateList').value;
	if (empty(templateName)) {
		alert("Please select the template before printing.");
		return false;
	}
	var printerId = document.getElementById('consPrinterId').value;
	obj.href = url + "&templateName="+templateName+"&printerId="+printerId;

}

var diagnosisDialog;
var consulDialog;
var trtDialog;
var encDialog;
var drgDialog;
var loincDialog;

function initAddOrEditDiagnosisCode() {
    var diagnosisDiv = document.getElementById("addoreditDiagnosisCodeDialog");
    diagnosisDiv.style.display = 'block';
    diagnosisDialog = new YAHOO.widget.Dialog('addoreditDiagnosisCodeDialog', {
        width: "1000px",
        context: ["diagnosiscodes", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: diagDialogCancel,
        scope: diagnosisDialog,
        correctScope: true
    });
    diagnosisDialog.cfg.setProperty("keylisteners", escKeyListener);
    diagnosisDialog.render();

}
function diagDialogCancel(){
	hidetoolTip('diagInfo');
	diagnosisDialog.cancel();
}

function initAddOrEditConsulCode() {
    var consulDiv = document.getElementById("addoreditConsulCodeDialog");
    consulDiv.style.display = 'block';
    consulDialog = new YAHOO.widget.Dialog('addoreditConsulCodeDialog', {
        width: "800px",
        context: ["consulcodes", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: cancelConsulDialog,
        scope: consulDialog,
        correctScope: true
    });
    consulDialog.cfg.setProperty("keylisteners", escKeyListener);
    consulDialog.cancelEvent.subscribe(onCloseConsDialog);
    consulDialog.render();
}
var trtColIndex = 0;
var BILLNO = trtColIndex++;
var TREATMENT_DATE = trtColIndex++;
var ORDER_NUMBER = trtColIndex++;
var CONDUCTION_STATUS = trtColIndex++;
if (mod_ceed_enabled) {
	var CEED_CHECK_STATUS = trtColIndex++;
}
var TYPE = trtColIndex++;
var DEPARTMENT = trtColIndex++;
var ITEM = trtColIndex++;
var CODE_TYPE = trtColIndex++;
var CODE = trtColIndex++;
var LAST_CELL = trtColIndex++;
function initTrtDialog() {
    var trtDiv = document.getElementById('trtDialog');
    trtDiv.style.display = 'block';
    trtDialog = new YAHOO.widget.Dialog('trtDialog', {
        width: "900px",
        context: ["treatment", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: cancelTrtDialog,
        scope: trtDialog,
        correctScope: true
    });
    trtDialog.cfg.setProperty("keylisteners", escKeyListener);
    trtDialog.cancelEvent.subscribe(onCloseTrtDialog);
    trtDialog.render();
}

function initLoincDialog() {
    var loincDiv = document.getElementById('loincDialog');
    loincDiv.style.display = 'block';
    loincDialog = new YAHOO.widget.Dialog('loincDialog', {
        width: "650px",
        context: ["loinc", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: cancelTrtDialog,
        scope: trtDialog,
        correctScope: true
    });
    loincDialog.cfg.setProperty("keylisteners", escKeyListener);
    loincDialog.cancelEvent.subscribe(onCloseLoincDialog);
    loincDialog.render();
}

function initEncDialog() {
    var encDiv = document.getElementById('encDialog');
    encDiv.style.display = 'block';
    encDialog = new YAHOO.widget.Dialog('encDialog', {
        width: "900px",
        context: ["encounter", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: encDialog.cancel,
        scope: encDialog,
        correctScope: true
    });
    encDialog.cfg.setProperty("keylisteners", escKeyListener);
    encDialog.render();
}

function initDrgDialog() {
	var drgDiv = document.getElementById('drgDialog');
    drgDiv.style.display = 'block';
    drgDialog = new YAHOO.widget.Dialog('drgDialog', {
        width: "650px",
        context: ["drg", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, {
        keys: 27
    }, {
        fn: drgDialog.cancel,
        scope: drgDialog,
        correctScope: true
    });
    drgDialog.cfg.setProperty("keylisteners", escKeyListener);
    drgDialog.render();
}

function cancelDrgDialog() {
	drgDialog.cancel();
}

function openEditDRGDialog(obj) {
    drgDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
    drgDialog.show();
    initDrgCodeAutoComplete();
    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    rowUnderEdit = row;
    var drgCode = getElementByName(row, 'drg_code').value;

    var elNewItem = matches(drgCode, drgAutoComp);
    drgAutoComp._selectItem(elNewItem);
    chargeId = getElementByName(row, 'drg_charge_id').value;
    resetObservationPanel('Drg');
	initializeObservationDialogFields(chargeId, 'Drg');
    setNodeText(document.getElementById('drgCodeDesc'), getElementByName(row, 'drg_description').value, 35);
    document.DRGCodeForm._drgCodeDesc.value = getElementByName(row, 'drg_description').value;
    if(isBaby == true && !(drgChargeId == chargeId) && chargeId == "")
    	defaultDrgObservation('drgObs');
}

function defaultDrgObservation(obsPreFix) {
	if (eclaimXMLSchema == 'HAAD') {
		document.getElementById(obsPreFix+'ValueTable').style.display = "block";
		if (document.getElementById('drgObsCodeType.1').value == '')
			setSelectedIndexText(document.getElementById('drgObsCodeType.1'), 'LOINC');
		if (document.getElementById('drgObsCode.1').value == '')
			document.getElementById('drgObsCode.1').value = '8339-4';
		if (document.getElementById('drgObsValue.1').value == '')
			document.getElementById('drgObsValue.1').value = weightValue;
		if (document.getElementById('drgObsValueType.1').value == '')
			document.getElementById('drgObsValueType.1').value = weightUom;
	}
}

var drgAutoComp = null;

function initDrgCodeAutoComplete() {
	drgAutoComp = initCodesAutocomplete('drgCode', 'drgDropDown', 'IR-DRG');
    drgAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('drgCodeDesc'), aArgs[2].code_desc, 35);
        document.DRGCodeForm._drgCodeDesc.value = aArgs[2].code_desc;
    })
}

function editDrgDialog() {
    var drgCode = document.DRGCodeForm.drgCode.value;
    var drgCodeDesc = document.DRGCodeForm._drgCodeDesc.value;
	var codeType = 'Code Type: IR-DRG';
	var drgObsValue = document.DRGCodeForm.drgObsValue.value;

	if((!drgObsValue == "" || !drgObsValue == null) && (drgCode == "" || drgCode == null)) {
		alert("Please Provide DRG Code.");
		document.DRGCodeForm.drgCode.focus();
		return false;
	}else if (drgCode == null || drgCode == 0 || drgCode == "") {
        drgCodeDesc = '';
        codeType = 'Code Type:';
    }

    getElementByName(rowUnderEdit, 'drg_code').value = drgCode;
    getElementByName(rowUnderEdit, 'drg_description').value = drgCodeDesc;

    setNodeText(rowUnderEdit.cells[2], (drgCode) + '-' + drgCodeDesc);
    setNodeText(rowUnderEdit.cells[3], codeType);
    addObservationsToGrid('Drg');
    drgDialog.cancel();
}

var encAuto1 = null;
var encAuto2 = null;
var encAuto3 = null;

function initEncCodeAutos() {
    encAuto1 = initCodesAutocomplete('encCode', 'encDropDown', 'Encounter Type');
    encAuto1.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('encTypeCodeDesc'), aArgs[2].code_desc, 35);
        document.EncounterForm._encTypeCodeDesc.value = aArgs[2].code_desc;
    })
    encAuto2 = initCodesAutocomplete('encStartCode', 'encStartDropDown', 'Encounter Start');
    encAuto2.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('encStartCodeDesc'), aArgs[2].code_desc, 35);
        document.EncounterForm._encStartCodeDesc.value = aArgs[2].code_desc;
    })
    encAuto3 = initCodesAutocomplete('encEndCode', 'encEndDropDown', 'Encounter End');
    encAuto3.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('encEndCodeDesc'), aArgs[2].code_desc, 35);
        document.EncounterForm._encEndCodeDesc.value = aArgs[2].code_desc;
    })
}

function initEncDateAndTime(row){
  let encounter_start_date = getElementByName(row, 'encounter_start_date').value;
  let encounter_start_time = getElementByName(row, 'encounter_start_time').value;
  let encounter_end_date = getElementByName(row, 'encounter_end_date').value;
  let encounter_end_time = getElementByName(row, 'encounter_end_time').value;

  encounter_start_date = formatDate(new Date(encounter_start_date), 'ddmmyyyy', '-')

  setElementValueAndTitle('encStartDate',encounter_start_date,encounter_start_date);
  setElementValueAndTitle('encStartTime',encounter_start_time.substring(0, 5),encounter_start_time);

  // For IP visit without any discharge date and time.
  if(encounter_end_date !== "" && encounter_end_time !== ""){
    if(validateDateFormat(encounter_end_date) != null){ // Date format other than `DD-MM-YYYY`, then only do the formatting
      encounter_end_date = formatDate(new Date(encounter_end_date), 'ddmmyyyy', '-');
    }
    setElementValueAndTitle('encEndDate',encounter_end_date,encounter_end_date);
    setElementValueAndTitle('encEndTime',encounter_end_time.substring(0, 5),encounter_end_time);
  }
}

function setElementValueAndTitle(elementId,value, title){
  let element = document.getElementById(elementId);
  element.setAttribute("value", value);
  element.setAttribute("title", title);
}

function validateEncDuration(obj){
  let row = YAHOO.util.Dom.getAncestorByTagName(obj, 'td');
  if (getElementByName(row, 'encEndDate') === null
      && getElementByName(row, 'encEndTime') === null) {
    return true;
  }
  let encounter_end_date = getElementByName(row, 'encEndDate').value;
  let encounter_end_time = getElementByName(row, 'encEndTime').value;

  let encounterEndDateTime = mergeDateAndTime(encounter_end_date,encounter_end_time)
  
  //Encounter end date must be greater than the max activity date(encEndDateTimeInitial) irrespective of the seconds part hence 59s
  const encounterEndDiff = (encounterEndDateTime - new Date(maxOrderDateTime)) / 1000; //In seconds
  if (encounterEndDiff < -59) {
    alert("Encounter End time must be greater than or equal to the last prescribed/conducted activity's datetime");
    return false;
  }
  return true;
}

function initCodesAutocomplete(field, dropdown, type, dialogType) {
    var dataSource = new YAHOO.util.XHRDataSource(cpath + "/pages/medicalrecorddepartment/MRDUpdate.do");
    var queryParams = "_method=getCodesListOfCodeType&codeType=" + encodeURIComponent(type);
    queryParams += (type != 'IR-DRG') ? "" : '&patientType='+patientType;
    queryParams += '&dialog_type='+ dialogType;
    dataSource.scriptQueryAppend = queryParams;
    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    dataSource.responseSchema = {
        resultsList: "result",
        fields: [{
            key: "code"
        }, {
            key: "icd"
        }, {
            key: "code_desc"
        }, {
        		key: "code_type"
        }, {
        		key: "is_year_of_onset_mandatory"
        }]
    };
    var oAutoComp = new YAHOO.widget.AutoComplete(field, dropdown, dataSource);
    oAutoComp.minQueryLength = 1;
    oAutoComp.forceSelection = true;
    oAutoComp.allowBrowserAutocomplete = false;
    oAutoComp.resultTypeList = false;
    oAutoComp.maxResultsDisplayed = 50;
    var reArray = [];
    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
        var escapedComp = Insta.escape(sQuery);
        reArray[0] = new RegExp('^' + escapedComp, 'i');
        reArray[1] = new RegExp("\\s" + escapedComp, 'i');

        var title = oResultData.code + ' / ' + oResultData.code_desc;
        var det = highlight(oResultData.code + ' / ' + oResultData.code_desc, reArray);

        var span = document.createElement('span');
    	span.setAttribute("title", title);
    	span.innerHTML = det;
    	var div = document.createElement('div');
    	div.appendChild(span);
    	return div.innerHTML;
    };
    oAutoComp.textboxChangeEvent.subscribe(function () {
        trtFieldEdited = true;
        loincFieldEdited = true;
    });
    oAutoComp.setHeader(' Code / Description ');
    return oAutoComp;
}

var trtCodeAutoComp = null;
var trtFieldEdited = false;

function initTrtCodesAutocomp(feild, dropDown, value) {
   // trtFieldEdited = true;
    if (trtCodeAutoComp != null) trtCodeAutoComp.destroy();
    trtCodeAutoComp = initCodesAutocomplete(feild, dropDown, value);
    trtCodeAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('trtCodeDesc'), aArgs[2].code_desc, 35, aArgs[2].code_desc)
    })

}


var loincCodeAutoComp = null;
var loincFieldEdited = false;

function initLoincCodesAutocomp(feild, dropDown, value) {
    loincFieldEdited = true;
    if (loincCodeAutoComp != null) loincCodeAutoComp.destroy();
    loincCodeAutoComp = initCodesAutocomplete(feild, dropDown, value);
    loincCodeAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('loincCodeDesc'), aArgs[2].code_desc, 35)
    })

}

var transFromAutoComp = null;
var transToAutoComp = null;
function initTransferHospSourceAutoCom() {
	    if (transFromAutoComp != null) transFromAutoComp.destroy();
	    transFromAutoComp = initTransFromAutoComplete();
	    transFromAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
	        setNodeText(document.getElementById('encStartSource'), aArgs[2].transfer_hospital_name, 35)
	        document.EncounterForm._encStartSource.value = aArgs[2].transfer_hospital_id;
	    })
}

function initTransferHospDestAutoCom() {
    if (transToAutoComp != null) transToAutoComp.destroy();
    transToAutoComp = initTransToAutoComplete();
    transToAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        setNodeText(document.getElementById('encEndDestination'), aArgs[2].transfer_hospital_name, 35)
        document.EncounterForm._encEndDestination.value = aArgs[2].transfer_hospital_id;
    })

}

function initTransFromAutoComplete() {
	var maxResults = 15;
	var datasource = new YAHOO.util.XHRDataSource(cpath + "/pages/medicalrecorddepartment/MRDUpdate.do");
	datasource.scriptQueryAppend="_method=getTransferHospitalSearch&limit=" + maxResults;
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
			resultsList : "results",
			fields : [  {key : "transfer_hospital_name"}, {key : "transfer_hospital_id"} ]
		};
		var oAutoComp = new YAHOO.widget.AutoComplete('encStartSource','encStartSource_dropdown', datasource);
		oAutoComp.minQueryLength = 1;
	    oAutoComp.forceSelection = false;
	    oAutoComp.allowBrowserAutocomplete = false;
	    oAutoComp.resultTypeList = false;
	    oAutoComp.maxResultsDisplayed = maxResults;
	    var reArray = [];
	    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
	        var escapedComp = Insta.escape(sQuery);
	        reArray[0] = new RegExp('^' + escapedComp, 'i');
	        reArray[1] = new RegExp("\\s" + escapedComp, 'i');
	        var det = highlight(oResultData.transfer_hospital_name, reArray);
	        return det;
	    };
	    return oAutoComp;
}

function initTransToAutoComplete() {
	var maxResults = 15;
	var datasource = new YAHOO.util.XHRDataSource(cpath + "/pages/medicalrecorddepartment/MRDUpdate.do");
	datasource.scriptQueryAppend="_method=getTransferHospitalSearch&limit=" + maxResults;
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
			resultsList : "results",
			fields : [  {key : "transfer_hospital_name"}, {key : "transfer_hospital_id"} ]
		};
		var oAutoComp = new YAHOO.widget.AutoComplete('encEndDestination','encEndDestination_dropdown', datasource);
		oAutoComp.minQueryLength = 1;
	    oAutoComp.forceSelection = false;
	    oAutoComp.allowBrowserAutocomplete = false;
	    oAutoComp.resultTypeList = false;
	    oAutoComp.maxResultsDisplayed = maxResults;
	    var reArray = [];
	    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
	        var escapedComp = Insta.escape(sQuery);
	        reArray[0] = new RegExp('^' + escapedComp, 'i');
	        reArray[1] = new RegExp("\\s" + escapedComp, 'i');
	        var det = highlight(oResultData.transfer_hospital_name, reArray);
	        return det;
	    };
	    return oAutoComp;
}

/**
* Function to handle diagnosis code additions on the diag dialog.
*/
function openDiagAddOrEditDialog(anchor) {
	deleteDTable();
	populateDiagDialog();
	showAddOrEditDiagnosisCodeDialog(anchor);
}

/**
* Diagnosis dialog Refresh function which deletes previous codes on the dialog's table.
*/
function deleteDTable() {
    var table = document.getElementById("mrd_codes");
    var len = table.rows.length;
    for (var i = 0; i < len - 2; i++)
    table.deleteRow(0);
}

/**
* Diagnosis dialog Populate function which populates the diagnosis dialog with saved diagnosis values.
*/
function populateDiagDialog() {
    var table = document.getElementById("diagnosiscodes");
    var numRows = table.rows.length;
    for (var i = 0; i < (numRows - 2); i++) {
        var row = table.rows[i];
        var cType = getElementByName(row, 'diag_code_type').value;
        var code = getElementByName(row, 'icd_code').value;
        var cDesc = getElementByName(row, 'description').value;
        var diagPOA = getElementByName(row, 'present_on_admission').value;
        var yearOfOnset = getElementByName(row, 'year_of_onset').value;
        var isPrime = getElementByName(row, 'diag_type').value;
        var cId = getElementByName(row, 'id').value;
        var masterDesc = getElementByName(row, 'master_desc').value;
        var deleted = getElementByName(row,'deleted').value;
        addOneMoreCode(code, cDesc, isPrime, cType, cId, masterDesc, deleted, diagPOA, yearOfOnset);
    }
}

// appends diagnosis code rows in the diagnosis dialog.

var diagIcdCodeAutoComp = {};
function addOneMoreCode(code, desc, primary, codeType, cId, masterDesc, deleted, diagPOA, yearOfOnset) {
	var table = document.getElementById("mrd_codes");
    var numRows = table.rows.length;
    var templateRow = table.rows[numRows - 2];
    var row = templateRow.cloneNode(true);
    row.style.display = '';
    YAHOO.util.Dom.insertBefore(row, templateRow);
    var el = getElementByName(row, 'code_id_desc');
    var isYearOfOnsetMandatoryEl = getElementByName(row, 'isYearOfOnsetMandatory');
    var dropDown = YAHOO.util.Dom.getNextSibling(el);
    var descEl = getElementByName(row, 'desc');
    var diagTypeSel = getElementByName(row, 'diagType');
    var diagCodeTypeEl = getElementByName(row, 'codeType');
    var codeIdeEl = getElementByName(row, 'codeId');
    var masterDescEl = getElementByName(row, 'masterCodeIdDesc');
    var deletedEl = getElementByName(row, 'deleted');
    var diagPOAEl = getElementByName(row, 'diagPOA');
    var yearOfOnsetEl = getElementByName(row, 'yearOfOnset');

	row.setAttribute("newlyAdded", true);        //to keep track of diagnoses added on the dialogue
    //populate the dialog from diagnosiscodes table
    if (code != null) {
        diagTypeSel.selectedIndex = primary == 'P' ? 1 : primary == 'A' ? 3 : primary == 'S' ? 2 :  primary == 'V' ? 3 : 0;
        diagCodeTypeEl.value = codeType;
        masterDescEl.value = masterDesc;
        deletedEl.value = deleted;

        row.setAttribute("newlyAdded", false);
        //set description
        setNodeText(row.cells[4], desc, 5);
        //set selected present on arrival dropdown
        if(diagPOAEl) {
        		diagPOAEl.value = diagPOA;
        }
        // set year of onset input field
        if (yearOfOnsetEl) {
        		yearOfOnsetEl.value = yearOfOnset;
        }
        if(deletedEl.value == 'true'){
        	deletedEl.value = false;
        	deleteDiagCode(row);
        }
    } else {
    	// when clicking on plus button, if no diagnosis exists default it to Primary else default it to Secondary.
    	var aDiagType = document.getElementsByName("diagType");
    	diagTypeSel.selectedIndex = aDiagType.length == 2 ? '1' : '2';
    }

	/*
	* 	Here, codeId refers to the mrd_diagnosis table "ID"
	* 	A codeId of -999 implies, that the diagnosis has been newly added
	* 	and is yet to be inserted into the mrd_diagnosis table
	*/
    codeIdeEl.value = (cId == null || cId == '') ? -999 : cId;
    if (!empty(codeType) || !empty(diagCodeType))
    	diagCodeTypeEl.value = (codeType == null || codeType == '') ? diagCodeType : codeType;

    var autoComp = initCodesAutocomplete(el, dropDown, diagCodeTypeEl.value);
    autoComp.itemSelectEvent.subscribe(diagnosisCodeTypeSelectItem);
    if (code != null) {
    	el.value = code;
    	autoComp._bItemSelected = true;
		autoComp._sInitInputValue = autoComp._elTextbox.value;

    	descEl.value = desc;
    }
    diagIcdCodeAutoComp[row.rowIndex] = autoComp;
}

function clearRowData(diagCodeTypeEl) {
	var row = findAncestor(diagCodeTypeEl, "TR");
	getElementByName(row, 'code_id_desc').value = '';
	getElementByName(row, 'desc').value = '';
	getElementByName(row, 'masterCodeIdDesc').value = '';
	setNodeText(row.cells[4], '');

	var el = getElementByName(row, 'code_id_desc');
	var dropDown = YAHOO.util.Dom.getNextSibling(el);
	var autoComp = diagIcdCodeAutoComp[row.rowIndex];
	if (!empty(autoComp)) {
		autoComp.destroy();
		autoComp = null;
	}
	autoComp = initCodesAutocomplete(el, dropDown, diagCodeTypeEl.value);
	autoComp.itemSelectEvent.subscribe(diagnosisCodeTypeSelectItem);
	diagIcdCodeAutoComp[row.rowIndex] = autoComp;
}

function diagnosisCodeTypeSelectItem(sType, aArgs) {
	var tRow = YAHOO.util.Dom.getAncestorByTagName(aArgs[0]._elTextbox, 'tr');
    var descEl = getElementByName(tRow, 'desc');
    var isYearOfOnsetMandatory = getElementByName(tRow, 'isYearOfOnsetMandatory');

    getElementByName(tRow, 'masterCodeIdDesc').value = aArgs[2].code_desc;
    descEl.value = aArgs[2].code_desc;
    isYearOfOnsetMandatory.value = aArgs[2].is_year_of_onset_mandatory;
    setNodeText(tRow.cells[4], aArgs[2].code_desc, 5);
}

function isCodeTypeSameForAll() {
	var codeTypes = document.getElementsByName('diag_code_type');
	var deleted = document.getElementsByName('deleted');
	var type = '';
	for (var i=0; i<codeTypes.length-1; i++) {
		if (deleted[i].value != 'true' && type != '' && codeTypes[i].value != type) {
			return false;
		}
		if (deleted[i].value != 'true')
			type = codeTypes[i].value;
	}
	return true;
}


/**
 * Marks a row for deletion in Diagnosis dialog display
 */
function deleteDiagCode(row) {

	// Check if the row was newly added through the dialogue box
	if (row.getAttribute("newlyAdded") == "true") {
		row.parentNode.removeChild(row);
		return;
	}
	// Toggle between imgDelete and deleteText if not newly added
	imgDelete = row.getElementsByClassName('imgDelete')[0];
	deleteText = row.getElementsByClassName('deleteText')[0];

	var deletedEl = getElementByName(row, 'deleted');
	if (deletedEl.value == 'true') {
		imgDelete.style.display = '';
		deleteText.style.display = 'none';
		row.style.opacity = 1;
		// enable inputs while row is not in deleted state
		input = row.getElementsByTagName('input')[0];
		codeTypeDropdown = row.getElementsByClassName('dropdown')[0]
		dropdown = row.getElementsByClassName('dropdown')[1];
		input.disabled = false;
		codeTypeDropdown.disabled = false;
		dropdown.disabled = false;
		deletedEl.value = false;
		return;
	} else {
		// if not new diagnosis, hide imgDelete and show deleteText
		imgDelete.style.display = 'none';
		deleteText.style.display = '';
		row.style.opacity = 0.5;
		// disable inputs while row is in deleted state
		input = row.getElementsByTagName('input')[0];
		codeTypeDropdown = row.getElementsByClassName('dropdown')[0]
		dropdown = row.getElementsByClassName('dropdown')[1];
		input.disabled = true;
		codeTypeDropdown = true;
		dropdown.disabled = true;
		deletedEl.value = true;
		return;
	}
}


/**
* Diagnosis dialog Display function.
*/
function showAddOrEditDiagnosisCodeDialog(obj) {
	diagnosisDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
    diagnosisDialog.show();
}

function showDiagnosisToolTip(id, alignDir, evt, obj) {
	var label = obj;
	var curRow = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
	var info = getElementByName(curRow, 'masterCodeIdDesc');
	var header = getElementByName(curRow, 'code_id_desc');
	showtoolTip('diagInfo', alignDir , evt,info.value, header.value);
}

function showtoolTip(id, dir, event, info, header) {
  hidetoolTip(id);
  var event = event || window.event;
  var xpos = mouseX(event);
  var ypos = mouseY(event);
  if (document.getElementById(id).style.display != 'inline') {
    document.getElementById(id).style.display = 'inline';
    switch (dir.toLowerCase()) {
      case 'center':
        document.getElementById(id).style.left = xpos - 125;
        break;
      case 'left':
        document.getElementById(id).style.left = ypos - 250;
        break;
      default:
        document.getElementById(id).style.left = xpos;
    }

    document.getElementById(id).style.top = ypos + 10;
  }
  document.getElementById(id+'Label').textContent = info;
  document.getElementById(id+'HeaderLabel').textContent = header;
}

function mouseX(evt) {
	if (evt.pageX)
		return evt.pageX;
	else if (evt.clientX)
   		return evt.clientX + (document.documentElement.scrollLeft ?
   				document.documentElement.scrollLeft :
   				document.body.scrollLeft);
	else
		return null;
}

function mouseY(evt) {
	if (evt.pageY)
		return evt.pageY;
	else if (evt.clientY)
   		return evt.clientY + (document.documentElement.scrollTop ?
   			document.documentElement.scrollTop :
   			document.body.scrollTop);
	else
		return null;
}


function hidetoolTip(id) {
  if(id == null || id == '') id='diagInfo';
  document.getElementById(id).style.display = 'none';
}

function validateEmptyRows() {
	var dTable = document.getElementById("mrd_codes");
    var numRows = dTable.rows.length;
	for (var i = 0; i < (numRows - 2); i++) {
		var row = dTable.rows[i];
		var diagTypeSel = getElementByName(row, 'diagType');
        var codeEl = getElementByName(row, 'code_id_desc');
		if (diagTypeSel.value == '' && trim(codeEl.value) != '') {
			alert("Please select diag type...");
			diagTypeSel.focus();
			return false;
		}
		if (diagTypeSel.value != '' && trim(codeEl.value) == '') {
			alert("Please enter code/desc...");
			codeEl.focus();
			return false;
		}
	}
	//validation for duplicate diagnosis codes
	for (var i = 0; i < (numRows - 2); i++) {
	   var codeEl = document.getElementsByName('code_id_desc');
	   var diagTypeEl = document.getElementsByName('diagType');

	   for (var j = i+1; j <  (numRows - 2); j++){
		   var codeDesc = document.getElementsByName('code_id_desc');
		   if ( trim(codeEl[i].value) == '' && trim(codeDesc[j].value) == '' )
		   		continue;
		   // codes are considered duplicate if they are of the same group type.
		   // here we consider P(Primary Diag) and S(secondary diag) as one group.
		   // and reason for visit as another group. Same codes may not exist in same group
		   // but can exist across groups.
		   if(codeEl[i].value == codeEl[j].value
				   && !((diagTypeEl[i].value == 'V' && (diagTypeEl[j].value == 'S' || diagTypeEl[j].value == 'P'))
				   || (diagTypeEl[i].value == 'P' && diagTypeEl[j].value == 'V')
				   || (diagTypeEl[i].value == 'S' && diagTypeEl[j].value == 'V'))
				   ){
		      alert('Duplicate Diagnosis Code'+' '+codeDesc[j].value);
		      codeDesc[j].focus();
		      return false;
		   }
	   }


	}
	// validate year of onset
	var yearOfOnset =  document.getElementsByName('yearOfOnset');
    var isYearOfOnsetMandatory = document.getElementsByName('isYearOfOnsetMandatory');
    var codeDesc = document.getElementsByName('code_id_desc');
    var diagTypeSel = document.getElementsByName('diagType');
	for (var i = 0; i < yearOfOnset.length - 1; i++) {

		if (yearOfOnset[i].value && (yearOfOnset[i].value.length != 4  || !numberCheck(yearOfOnset[i])) ){
			alert("Please check the year of onset format (YYYY).");
			return false;
		}

		if(isYearOfOnsetMandatory[i].value == 'true' && yearOfOnset[i].value == "" && diagTypeSel[i].value == 'P') {
			alert("Year of onset is required for primary diagnosis. Please enter year of onset.");
			return false;
		}
	}


	return true;
}


/**
* function to add diagnosis codes from dialog to the main screen table.
*/
function addDiagCodesToGrid() {
	 if (!validateEmptyRows()) {
    	return false;
    }
    var table = document.getElementById("diagnosiscodes");
    deleteRows(); //clear existing table contents

    var codeTemplateRow = document.getElementById('codeRow');

    var dTable = document.getElementById("mrd_codes");
    var numRows = dTable.rows.length;
    var pCount = 0;
    var sCount = 0;
    var topCodeRow = null;
    var topRvfRow = null;

    //insert rows from dialogue into main table
    for (var i = 0; i < (numRows - 2); i++) {

        var row = dTable.rows[i];
        var diagTypeSel = getElementByName(row, 'diagType');
        var codeEl = getElementByName(row, 'code_id_desc');
        var descEl = getElementByName(row, 'desc');
        var presentOnAdmission = getElementByName(row, 'diagPOA');
        var yearOfOnset = getElementByName(row, 'yearOfOnset');
        var isYearOfOnsetMandatory = getElementByName(row, 'isYearOfOnsetMandatory');
        var codeTypeEl = getElementByName(row, 'codeType');
        var idEl = getElementByName(row, 'codeId');
        var isPrime = diagTypeSel.value == 'P';
        var isAdmit = diagTypeSel.value == 'A';
        var isRvf = diagTypeSel.value == 'V';
        var isSec = diagTypeSel.value == 'S';
        var masterDescEl = getElementByName(row, 'masterCodeIdDesc');
        var deletedEl = getElementByName(row, 'deleted');
        var diag_type_from_db_el = getElementByName(row, 'diag_type_from_db');
        var icd_code_from_db_el = getElementByName(row, 'icd_code_from_db');
        var diagcodeortypeedited_el = getElementByName(row, 'diagcodeortypeedited');
        // check if diag_code or type edited
        diagcodeortypeedited_el.value = "false";
        if(diagTypeSel.value != diag_type_from_db_el.value || codeEl.value != icd_code_from_db_el.value) {
        	diagcodeortypeedited_el.value = "true";
        }

        var codeRow = codeTemplateRow.cloneNode(true);
        codeRow.style.display = '';
        //Shows the row as deleted
    	if ( deletedEl.value == 'true' ){
    		codeRow.style.display = 'none';
    	}

    	if(isRvf){
    		if(topRvfRow == null) topRvfRow = codeRow;
    	}

        if (isPrime && topCodeRow != null) YAHOO.util.Dom.insertBefore(codeRow, topCodeRow);
        else if (pCount == 1 && isAdmit) {
            YAHOO.util.Dom.insertAfter(codeRow, topCodeRow);
        } else {
        	if(isSec && topRvfRow != null){
        		YAHOO.util.Dom.insertBefore(codeRow, topRvfRow);
        	}else{
        		YAHOO.util.Dom.insertBefore(codeRow, codeTemplateRow);
        	}
        }

        if (topCodeRow == null) topCodeRow = codeRow;

        var labels = YAHOO.util.Dom.getElementsByClassName("formlabel", null, codeRow);
        var info = YAHOO.util.Dom.getElementsByClassName("forminfo", null, codeRow);

        var s = diagTypeSel.value == 'P' ? 'Primary Diagnosis(Type):' : diagTypeSel.value == 'A' ? 'Admitting Diagnosis(Type)' : 'Secondary Diagnosis(Type):';
        if(diagTypeSel.value == 'V'){
        	s = 'Reason For Visit(Type):';
        }
        labels[0].innerHTML = s + labels[0].innerHTML;

        info[0].textContent = codeEl.value + '(' + codeTypeEl.value + ')';
        getElementByName(codeRow, 'diag_code_type').value = codeTypeEl.value;
        getElementByName(codeRow, 'icd_code').value = codeEl.value;
        getElementByName(codeRow, 'description').value = info[1].textContent = descEl.value;
        // only valid for IP patients
        if (presentOnAdmission && presentOnAdmission.value != '') {
        		info[2].textContent = presentOnAdmission.options[presentOnAdmission.selectedIndex].text;
            getElementByName(codeRow, 'present_on_admission').value = presentOnAdmission.value;
        }
        getElementByName(codeRow, 'year_of_onset').value = yearOfOnset.value;
        getElementByName(codeRow, 'master_desc').value = masterDescEl.value;
        getElementByName(codeRow, 'diag_type').value = diagTypeSel.value == '' ? 'S' : diagTypeSel.value;
        getElementByName(codeRow, 'id').value = idEl.value;
        getElementByName(codeRow, 'deleted').value = deletedEl.value;
        getElementByName(codeRow, 'diag_type_from_db').value = diag_type_from_db_el.value;
        getElementByName(codeRow, 'icd_code_from_db').value = icd_code_from_db_el.value;
        getElementByName(codeRow, 'diagcodeortypeedited').value = diagcodeortypeedited_el.value;
        isPrime ? pCount++ : sCount++;
    }
    hidetoolTip('diagInfo');
    diagnosisDialog.cancel();
}

/**
* Diagnosis Refresh function which clears previous diagnosis values from the table.
*/
function deleteRows() {
    var table = document.getElementById("diagnosiscodes");
    var len = table.rows.length;
    for (var i = 0; i < len - 2; i++)
    table.deleteRow(0);
}

/**
*  Diagnosis function, which on change of Diagnosis Type, sets utmost one of the types to Primary.
*  Toggles Year of Onset input field.
*/
function setPrimaryCode(currentSelEl) {
	var table = document.getElementById("mrd_codes");
	var len = table.rows.length;

	for (var i = 0; i < len - 2; i++) {
		var row = table.rows[i];
		var diagtypeEl = getElementByName(row, 'diagType');

		if (currentSelEl.value == 'A' || currentSelEl.value == 'P') {
			if ((currentSelEl != diagtypeEl)
					&& (currentSelEl.value == diagtypeEl.value)) {
				diagtypeEl.selectedIndex = 2;
			}
		}
	}
}

/**
 * Diagnosis history dialog initializer function.
 */
var diagnosisHistoryDialog;
function initDiagnosisHistoryDialog() {
	var dialogDiv = document.getElementById("diagnosisHistoryDiv");
	dialogDiv.style.display = 'block';
	diagnosisHistoryDialog = new YAHOO.widget.Dialog("diagnosisHistoryDiv",
			{	width:"600px",
				context : ["diagnosisHistoryDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('dhdCloseBtn', 'click', diagnosisHistoryDialog.cancel, diagnosisHistoryDialog, true);
	subscribeEscKeyListener(diagnosisHistoryDialog);
	diagnosisHistoryDialog.render();
}

function fetchAndDisplayDiagnosisHistoryDetails() {
	var url = cpath + "/pages/medicalrecorddepartment/MRDUpdate.do?_method=getDiagnosisHistory&visitId="+document.getElementById('patId').value;
		YAHOO.util.Connect.asyncRequest('GET', url,
			{ 	success: populateDiagnosisHistoryDialog,
				failure: failedToLoadDiagnosisHistory,
			});
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
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

			setNodeText(row.cells[2],
				diagnosisHistory[i].diag_type == 'P' ? 'Principal Diagnosis' : 'Secondary Diagnosis');
			setNodeText(row.cells[3], diagnosisHistory[i].description);
   			setNodeText(row.cells[1], diagnosisHistory[i].icd_code);
   			patientId = diagnosisHistory[i].visit_id;
		}
		diagnosisHistoryDialog.show();
	}
}


function failedToLoadDiagnosisHistory() {

}

var sort_by = function(field, reverse, primer){
   var key = function (x) {return primer ? primer(x[field]) : x[field]};
   return function (a,b) {
       var A = key(a), B = key(b);
       return (A < B ? -1 : (A > B ? 1 : 0)) * [1,-1][+!!reverse];
   }
}

function removeDuplicates(consultTypes) {
	consultTypes.sort(sort_by('consultation_type', false, function(x){return x.toUpperCase()}))
	for (var i=consultTypes.length-1; i>0; i--) {
		var record1 = consultTypes[i];
		var record2 = consultTypes[i-1];
		if (record1.consultation_type == record2.consultation_type) {
			consultTypes.splice(i-1, 1);
		}
	}
	return consultTypes;
}

/**
*  function to handle the display of consultation dialog...
*/
var consulAutoCmplt = null;
function openConsulEditDialog(obj, index) {
	clearForm(document.AddOrEditConsulForm);

    //copy the item index to dialog's index.
    document.getElementById('dlg_consulIndex').value = index;
    // table row index from which values  are being copied to dialog.
    var row = document.getElementById('consulRow' + index);

    //create consultation code (E&M or Service or etc.) autocmplt and intialialize with values of those in the table
    if (consulAutoCmplt != null) {
        consulAutoCmplt.destroy();
        consulAutoCmplt = null;
    }
    consulAutoCmplt = initCodesAutocomplete(document.getElementById('dlg_item_code'), document.getElementById('dlg_itemCode'), trim(supportedConsCodeTypes), 'consultation');
    consulAutoCmplt.forceSelection = true; // as user cannot enter codes other than the supported code type for consultations.
    var tabItemCode = getElementByName(row, 'item_code');

    //consultation auto complete item-code select handler
    consulAutoCmplt.itemSelectEvent.subscribe(function (sType, aArgs) {
        var isValidEnM = false;
        var consultTypes = filterList2(consulItemCodes, "item_code", (YAHOO.lang.isString(aArgs[2]) ? aArgs[2] : aArgs[2].code).trim(), "org_id", orgID);
        consultTypes = removeDuplicates(consultTypes);
		if (consultTypes == null || consultTypes.length == 0) {
        	isVaidEnM = false;
        	alert("Code not applicable. Please change.");
        	document.getElementById('dlg_item_code').focus();
        	document.getElementById("dlg_codeType").value = '';
        	if (document.getElementById('consulType_drop'))
	        	document.getElementById('consulType_drop').length = 0;
	        if (document.getElementById('dlg_consultType_label'))
	        	document.getElementById('dlg_consultType_label').textContent = '';
	        document.getElementById("dlg_consultationTypeId").value = '';
        	return false;
        } else {
        	isValidEnM = true;
        	if (consultTypes.length == 1) {
        		document.getElementById('dlg_consultType_label_div').style.display = 'block';
        		document.getElementById('dlg_consultType_dropdown').style.display = 'none';
        		document.getElementById('dlg_consultType_label').textContent = consultTypes[0].consultation_type;
        		document.getElementById("dlg_consultationTypeId").value = consultTypes[0].consultation_type_id;
        		document.getElementById("dlg_consultationType").value = consultTypes[0].consultation_type;
        	} else {
        		document.getElementById('dlg_consultType_label_div').style.display = 'none';
        		document.getElementById('dlg_consultType_dropdown').style.display = 'block';
        		loadSelectBox(document.getElementById('consulType_drop'), consultTypes, 'consultation_type', 'consultation_type_id', '-- Select --', '');
        		document.getElementById('consulType_drop').value = getElementByName(row, 'base_consultation_type_id').value;
        		document.getElementById("dlg_consultationTypeId").value = document.getElementById('consulType_drop').value;
        	}
        	document.getElementById("dlg_masterCodeDesc").value = aArgs[2].code_desc;
            document.getElementById("dlg_codeDesc").value = aArgs[2].code_desc;
            document.getElementById('dlg_masterCode').value = aArgs[2].code;
            document.getElementById("dlg_codeType").value = aArgs[2].code_type;
        }

    });

    var tableConslTypeId = getElementByName(row, 'consultation_type_id').value;
    document.getElementById("dlg_consultationTypeId").value = (tableConslTypeId == null || tableConslTypeId == '') ? '-999' : tableConslTypeId;
    document.getElementById("consul_codes").rows[0].style.display = 'block';

	document.getElementById('dlg_consultType_label_div').style.display = 'block';
	document.getElementById('dlg_consultType_dropdown').style.display = 'none';
    //if a code already exists in the table, intialize the dialog item code with the same.
    if (!empty(tabItemCode.value)) {
        var matchItem = matches(tabItemCode.value, consulAutoCmplt);
        consulAutoCmplt._selectItem(matchItem); // select the item code value in auto complete
        // set the corresponding consultation type for the item code
        document.getElementById('dlg_consultType_label').textContent = getElementByName(row, 'consultation_type').value;
   		document.getElementById("dlg_consultationTypeId").value = getElementByName(row, 'consultation_type_id').value;
        document.getElementById("dlg_consultationType").value = getElementByName(row, 'consultation_type').value;
        // also, the descriptor and code types
        document.getElementById('dlg_codeDesc').value = getElementByName(row, 'consul_code_desc').value;
        document.getElementById('dlg_masterCodeDesc').value = getElementByName(row, 'consul_code_desc').value;
        document.getElementById('dlg_codeType').value = getElementByName(row, 'consul_code_type').value;
        document.getElementById('dlg_masterCode').value = tabItemCode.value;

		 if (getElementByName(row, 'consultation_bill_status').value == 'A'
		 			&& getElementByName(row, 'consultation_status').value == 'C' ) {
		 		document.getElementById('dlg_codeDesc').disabled = false;
		 		document.getElementById('dlg_item_code').disabled = false;
		 }else {
		 		document.getElementById('dlg_codeDesc').disabled = true;
		 		document.getElementById('dlg_item_code').disabled = true;
		 }
    }

    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    rowUnderEdit = row;
    YAHOO.util.Dom.addClass(row, 'editing');
	editTrt = true;

    var vitalChargeId = getElementByName(row, 'vitalCharge_id').value;
    resetObservationPanel('Vital');
    initializeObservationDialogFields(vitalChargeId, 'Vital');

    consulDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
    consulDialog.show();
}

function changeTypeId(obj) {
	if (obj.options.length >= 0) {
		document.getElementById("dlg_consultationTypeId").value = obj.value;
		document.getElementById("dlg_consultationType").value = obj.options[obj.options.selectedIndex].text;
	} else {
		document.getElementById("dlg_consultationTypeId").value = '';
		document.getElementById("dlg_consultationType").value = '';
	}
}

function findConsultationNameFromTypeId(selEl, cid) {
    for (var i = 0; i < selEl.length; i++) {
        if (selEl.options[i].value == cid) return selEl.options[i].text + "";
    }
    return selEl.options[selEl.selectedIndex].text + "";
}

function addConsulCodesToGrid() {
    if (document.getElementById("dlg_item_code").value == null || document.getElementById("dlg_item_code").value == '') {
        alert("Please enter the code for consultation type...");
        return false;
    } else {
    	if (document.getElementById('dlg_consultationTypeId').value == '') {
    		if (document.getElementById('consulType_drop').length > 0) {
    			alert("Please select the consultation type.");
    			return false;
    		} else {
    			alert("Code not applicable. Please change.");
    			return false;
    		}
    	}
    }

    var tabIndex = document.getElementById("dlg_consulIndex").value;
    var row = document.getElementById('consulRow' + tabIndex);

    getElementByName(row, "item_code").value = document.getElementById("dlg_item_code").value;
    getElementByName(row, "consul_code_type").value = document.getElementById("dlg_codeType").value;
    getElementByName(row, "consultation_type").value = document.getElementById("dlg_consultationType").value;
    getElementByName(row, "consultation_type_id").value = document.getElementById("dlg_consultationTypeId").value;
    getElementByName(row, "head").value = document.getElementById("dlg_consultationTypeId").value;
    getElementByName(row, "consul_code_desc").value = document.getElementById("dlg_codeDesc").value;

	var codetType = document.getElementById("dlg_codeType").value;
    document.getElementById('consulCode' + tabIndex).textContent = document.getElementById("dlg_item_code").value + "(" + codetType + ")";
    document.getElementById('consulType' + tabIndex).textContent = document.getElementById("dlg_consultationType").value;
    var dlg_desc = document.getElementById("dlg_codeDesc").value;
    if (dlg_desc.length <= 39) {
        document.getElementById('consulDesc' + tabIndex).textContent = dlg_desc;
    } else {
        document.getElementById('consulDesc' + tabIndex).title = dlg_desc;
        document.getElementById('consulDesc' + tabIndex).textContent = dlg_desc.trim().substr(0, 28) + "...";
    }
    addObservationsToGrid('Vital');
    consulDialog.cancel();
}


var rowUnderEdit = null;
var editTrt = true;

function openEditTrtDialog(obj, type) {
    clearForm(document.EditTrtForm);
    

    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    rowUnderEdit = row;
    YAHOO.util.Dom.addClass(row, 'editing');
    var itemCode;
    var codeType;
    var codeDesc;
    var masterDesc;
    var chargeId;

    var isTpa = getElementByName(row, 'is_tpa').value;
    if ( isTpa == 'true' ) {
    	var primaryClaimID = getElementByName(row, "primary_claim_id");
    	var secondaryClaimID = getElementByName(row, "secondary_claim_id");
    	if (!empty(primaryClaimID.value)) {
    		document.getElementById("prAuthRowP").style.display = 'table-row';
    		document.EditTrtForm.trtPreAuthIdP.value = getElementByName(row, 'primary_auth_id').value;
    		document.EditTrtForm.trtPreAuthModeIdP.value = getElementByName(row, 'primary_auth_mode_id').value;
    	}

		if (!empty(secondaryClaimID.value)) {
    		document.getElementById("prAuthRowS").style.display = 'table-row';
    		document.EditTrtForm.trtPreAuthIdS.value = getElementByName(row, 'secondary_auth_id').value;
    		document.EditTrtForm.trtPreAuthModeIdS.value = getElementByName(row, 'secondary_auth_mode_id').value;
    	}
    }
    if (type == 'drug') {
        document.EditTrtForm.drgCodeType.style.display = 'block';
        document.EditTrtForm.trtCodeType.style.display = 'none';
        document.EditTrtForm.drgCodeType.selectedIndex = 0;
        itemCode = getElementByName(row, 'item_code').value;
        codeType = getElementByName(row, 'drg_code_type').value;
        codeDesc = getElementByName(row, 'trt_mster_desc').value;
        document.EditTrtForm.drgCodeType.value = codeType;
        editTrt = false;
    } else {
        document.EditTrtForm.trtCodeType.style.display = 'block';
        document.EditTrtForm.drgCodeType.style.display = 'none';
        document.EditTrtForm.trtCodeType.selectedIndex = 0;

        itemCode = getElementByName(row, 'act_rate_plan_item_code').value;
        codeType = getElementByName(row, 'trt_code_type').value;
        codeDesc = getElementByName(row, 'trt_mster_desc').value;
        document.EditTrtForm.trtCodeType.value = codeType;
        editTrt = true;
    }
    chargeId = getElementByName(row, 'charge_id').value;
    resetObservationPanel('Treatment');
    initializeObservationDialogFields(chargeId, 'Treatment');

    initTrtCodesAutocomp('trtCode', 'trtDropDown', codeType);
    var elNewItem = matches(itemCode, trtCodeAutoComp);
    trtCodeAutoComp._selectItem(elNewItem);
   	setNodeText(document.getElementById('trtCodeDesc'), codeDesc, 35, codeDesc);
   	trtFieldEdited = false;

   	if(mod_ceed_enabled && (roleId == 1 || roleId == 2 || has_right_to_view_ceed_comments)) {
		document.getElementById('ceedcommentsfieldset').style.display = 'block';
		if(chargeId in ceedresponsemap) {
			var responseedits = ceedresponsemap[chargeId];
			var addedcount = 0;
			var ulist = document.getElementById('ceed_response_comments');
			while(ulist.hasChildNodes()) {
				ulist.removeChild(ulist.lastChild);
			}
			for(var i=0;i<responseedits.length;i++) {
				var comment = responseedits[i]["claim_edit_response_comments"];
				if(comment != null && comment != "") {
					var litem = document.createElement("li");
					litem.innerHTML=comment;
					ulist.appendChild(litem);
					addedcount++;
				}
			}
			if(addedcount == 0) {
				document.getElementById('ceedcommentsfieldset').style.display = 'none';
			}
		}
		else {
			document.getElementById('ceedcommentsfieldset').style.display = 'none';
		}
	}

	// first display all the treatment codes(display : block or none will reduces or increases the height of the
	// modal dialog) and then align the dialog to the corresponding anchor.
    trtDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
    trtDialog.show();
}

function resetObservationPanel(obsType) {

	var obsPreFix = '';
	if (obsType == 'Vital') {
		obsPreFix = 'vitalObs';
	} else if(obsType == 'Drg'){
		obsPreFix = 'drgObs';
    }else {
    	obsPreFix = 'obs';
    }

   	var obsTable = document.getElementById(obsPreFix+'ValueTable');
   	for (var i = obsTable.rows.length; i > 2; i--) {
        obsTable.deleteRow(-1);
    }
    document.getElementById(obsPreFix+'ValueTable').style.display = "none";
    document.getElementById(obsPreFix+'ChgId').value = "";
    
    document.getElementById(obsPreFix+'Index').value = "1";
    
    // reset the first observation row
    var row = document.getElementById(obsPreFix + '.1');
    
    var uploadFileEl = getElementsByName(row, 'uploadFile')[0];
    var downloadFileEl = getElementsByName(row, 'btnDownloadFile')[0];
    if(uploadFileEl != undefined){
    	uploadFileEl.style = 'display:none;';
    	uploadFileEl.disabled = true;
    	uploadFileEl.value = "";
    	//hide the download button
    	downloadFileEl.style = 'display:none;';
    }
	var obsValue = document.getElementById(obsPreFix+'Value.1');
	var obsValueType = document.getElementById(obsPreFix+'ValueType.1');
	var obsCodeDesc = document.getElementById(obsPreFix+'CodeDesc.1');
	
	obsValue.disabled = false;
	obsValueType.disabled =  false;
	obsCodeDesc.textContent = '';
	obsCodeDesc.title = '';
	obsValueType.value = '';
}

var diagAutoCompleteArray = new Array();
var diagAcArrayIndex = 0;
function initializeObservationDialogFields(chargeId, obsType){
	var obsPreFix = '';
	var hidObsPrefix = '';
	if (obsType == 'Vital') {
		obsPreFix = 'vitalObs';
		hidObsPrefix = 'vitalObser';

	}else if(obsType == 'Drg'){
		obsPreFix = 'drgObs';
		hidObsPrefix = 'drgObser';

    }else {
    	obsPreFix = 'obs';
    	hidObsPrefix = 'obser';
    }

	var obsParentDiv = document.getElementById(obsPreFix+'ervations.'+chargeId);
	var noOfObs = document.getElementById(hidObsPrefix+'Index.'+chargeId).value;
    if (noOfObs > 0) document.getElementById(obsPreFix+'ValueTable').style.display = "block";
    var dlg_chargeId = document.getElementById(obsPreFix+'ChgId');
    dlg_chargeId.value = chargeId;
	diagAutoCompleteArray = new Array();
	diagAcArrayIndex = 0;

    for (var i = 1; i <= noOfObs; i++) {
        var observationType = document.getElementById(hidObsPrefix+'Type.' + chargeId + i).value;
        var observationCode = document.getElementById(hidObsPrefix+'Code.' + chargeId + i).value;
        var observationValue = document.getElementById(hidObsPrefix+'Value.' + chargeId + i).value;
        var observationValueType = document.getElementById(hidObsPrefix+'ValueType.' + chargeId + i).value;
        var observationValueEditable = document.getElementById(hidObsPrefix+'ValueEditable.' + chargeId + i).value;
        var observationCodeDesc = document.getElementById(hidObsPrefix+'CodeDesc.' + chargeId + i).value;
        var observationSponsorId='';
        var documentId = '';
        if(obsPreFix == 'obs' && hidObsPrefix == 'obser') {
        		observationSponsorId = document.getElementById(hidObsPrefix+'SponsorId.' + chargeId + i).value;
        		documentId = document.getElementById(hidObsPrefix+'DocumentId.' + chargeId + i).value;
        }
        	

        addObservationElements(obsPreFix);

        var dlg_obsIndex = document.getElementById(obsPreFix+'Index').value - 1;

        var dlg_obsCode = document.getElementById(obsPreFix+'Code.' + dlg_obsIndex);
        var dlg_obsCodeType = document.getElementById(obsPreFix+'CodeType.' + dlg_obsIndex);
        var dlg_obsValue = document.getElementById(obsPreFix+'Value.' + dlg_obsIndex);
        var dlg_obsValueType = document.getElementById(obsPreFix+'ValueType.' + dlg_obsIndex);
        var dlg_obsValueEditable = document.getElementById(obsPreFix+'ValueEditable.' + dlg_obsIndex);
        var dlg_codeDesc = document.getElementById(obsPreFix+'MasterCodeDesc.' + dlg_obsIndex);
        var dlg_obsSponsorId='';
        var dlg_documentId = '';
        if(obsPreFix == 'obs' && hidObsPrefix == 'obser') {
        		dlg_obsSponsorId = document.getElementById(obsPreFix+'SponsorId.' + dlg_obsIndex);
        		dlg_documentId = document.getElementById(obsPreFix+'DocumentId.' + dlg_obsIndex);
        }
        	
		dlg_obsCode.value = observationCode;
		dlg_obsCodeType.value = observationType;
		dlg_obsValue.value = observationValue;
		dlg_obsValueType.value = observationValueType;
		dlg_obsValueEditable.value = observationValueEditable;
		dlg_codeDesc.value = observationCodeDesc;
		if(obsPreFix == 'obs' && hidObsPrefix == 'obser') {
			dlg_obsSponsorId.value = observationSponsorId;
			dlg_documentId.value = documentId;	
		}
			

		if(observationValueEditable == 'N'){
			dlg_obsValueType.setAttribute("readonly", "true");
			dlg_obsValue.setAttribute("readonly", "true");
		}else {
			dlg_obsValueType.removeAttribute("readonly");
			dlg_obsValue.removeAttribute("readonly");
		}
		document.getElementById(obsPreFix+'Auto.'+dlg_obsIndex).setAttribute('style','padding-bottom: 20px;');
		var obsAuto = initCodesAutocomplete(obsPreFix+'Code.'+dlg_obsIndex, obsPreFix+'DropDown.'+dlg_obsIndex, observationType);
		obsAuto.itemSelectEvent.subscribe(function (sType, aArgs) {
				    	setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+dlg_obsIndex), aArgs[2].code_desc, 5);
				    	document.getElementById(obsPreFix+'MasterCodeDesc.'+dlg_obsIndex).value = aArgs[2].code_desc;
		});
		var elNewItem = matches(observationCode, obsAuto);
		obsAuto._selectItem(elNewItem);
		diagAutoCompleteArray[diagAcArrayIndex] =  new Array(2);
		diagAutoCompleteArray[diagAcArrayIndex][0] = new Array ({prefix: obsPreFix, index: dlg_obsIndex});
		diagAutoCompleteArray[diagAcArrayIndex][1] = obsAuto;
		diagAcArrayIndex++;

		dlg_codeDesc.value = observationCodeDesc;
		setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+dlg_obsIndex), observationCodeDesc, 5);
		
		showFileUpload(obsPreFix, i);
	}
}

function saveTrtObservations() {
	var url = cpath + "/MedicalRecords/Codification/trtObservations.htm"
	var ajaxReqObject = newXMLHttpRequest();
  	ajaxReqObject.open("POST",url.toString(), false); //synchronous call
  	var $form = $("#EditTrtForm");
  	var formArray = $form.serializeArray();
  	var obsObj = convertFormToMrdObjectArr(formArray);
  	var formData = new FormData(document.EditTrtForm);
  	formData.append("obsObj", JSON.stringify(obsObj));
  	ajaxReqObject.send(formData);
  	if (ajaxReqObject.readyState == 4) {
  		if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null)) {
  			var responseJson = JSON.parse(ajaxReqObject.responseText);
  			// set document ids for documents that have been uploaded
  			obsValueTable = document.getElementById('obsValueTable');
  			for (var j = 1; j < obsValueTable.rows.length; j++) {
  				i = (obsValueTable.rows[j].id).split(".")[1];
  		        var dlg_obsDocumentId = document.getElementById('obsDocumentId.' + i);
  		        if (responseJson.trtObservations && i-1 < responseJson.trtObservations.length) {
  		        		dlg_obsDocumentId.value = responseJson.trtObservations[i-1].documentId;
  		        }
  			}
  			return true;
  		}
  	}
}

// converts EditTrtForm to mrd observation object arrays.
function convertFormToMrdObjectArr(formArray) {
  	var obsObj = [];
	for (var i=0; i< formArray.length; i++) {
  		if (formArray[i].name.startsWith('obsCode.')) {
  		    index = formArray[i].name.split(".")[1] - 1;
  			if (obsObj[index])
  				obsObj[index].code = formArray[i].value;
  			else
  				obsObj.push({'code': formArray[i].value})
  		} else if (formArray[i].name.startsWith('obsValue.')) {
  			index = formArray[i].name.split(".")[1] - 1;
  			if (obsObj[index])
  				obsObj[index].value = formArray[i].value;
  			else
  				obsObj.push({'value': formArray[i].value})
  		} else if (formArray[i].name.startsWith('obsValueType.')) {
  			index = formArray[i].name.split(".")[1] - 1;
  			if (obsObj[index])
  				obsObj[index].valueType = formArray[i].value;
  			else
  				obsObj.push({'valueType': formArray[i].value})
  		} else if (formArray[i].name.startsWith('obsCodeType.')) {
  			index = formArray[i].name.split(".")[1] - 1;
  			if (obsObj[index])
  				obsObj[index].observationType = formArray[i].value;
  			else
  				obsObj.push({'observationType': formArray[i].value})
  		} else if (formArray[i].name.startsWith('obsDocumentId.')) {
  			index = formArray[i].name.split(".")[1] - 1;
  			if (obsObj[index])
  				obsObj[index].documentId = formArray[i].value;
  			else
  				obsObj.push({'documentId': formArray[i].value})
  		} else if (formArray[i].name.startsWith('obsChgId')) {
  			chargeId = formArray[i].value;
  		}
  	}
  	for (var i=0; i<obsObj.length;) {
  		if (obsObj[i]) {
  			obsObj[i].chargeId = chargeId;
  			// if observation type has been deselected to -- Select -- option.
  			// Dont send it via the api
  			if (obsObj[i].observationType == '' || obsObj[i].code == '') {
  	  			obsObj.splice(i,1);
  	  			continue;
  	  		}
  		}
  		i++;
  	}
  	return obsObj;
}

function addObservationsToGrid(obsType) {

	var obsPreFix = '';
	var hidObsPrefix = '';
	if (obsType == 'Vital') {
		obsPreFix = 'vitalObs';
		hidObsPrefix = 'vitalObser';

    }else if(obsType == 'Treatment'){
    	obsPreFix = 'obs';
    	hidObsPrefix = 'obser';
    } else {
    	obsPreFix = 'drgObs';
    	hidObsPrefix = 'drgObser';
    }

    var dlg_chargeId = document.getElementById(obsPreFix+'ChgId').value;

    var obsParentDiv = document.getElementById(obsPreFix+'ervations.' + dlg_chargeId);
    while (obsParentDiv.firstChild) obsParentDiv.removeChild(obsParentDiv.firstChild);

    var obsIndx = document.createElement('input');
    obsIndx.type = "hidden";
    obsIndx.id = hidObsPrefix+'Index.' + dlg_chargeId;

    obsIndx.value = document.getElementById(obsPreFix+'Index').value - 1;
    obsValueTable = document.getElementById(obsPreFix+'ValueTable');

    obsParentDiv.appendChild(obsIndx);

    var validObsCount = 0;
    for (var ii = 1; ii < obsValueTable.rows.length; ii++) {
        i = (obsValueTable.rows[ii].id).split(".")[1];
        var dlg_obsCode = document.getElementById(obsPreFix+'Code.' + i);
        var dlg_obsCodeType = document.getElementById(obsPreFix+'CodeType.' + i);
        var dlg_obsFile = document.getElementById(obsPreFix+'File.' + i);
        var dlg_obsValue = document.getElementById(obsPreFix+'Value.' + i);
        var dlg_obsValueType = document.getElementById(obsPreFix+'ValueType.' + i);
        var dlg_obsValueEditable = document.getElementById(obsPreFix+'ValueEditable.' + i);
        var dlg_codeDesc = document.getElementById(obsPreFix+'MasterCodeDesc.' + i);
        var dlg_obsSponsorId='';
        var dlg_obsDocumentId='';
        if(obsPreFix == 'obs' && hidObsPrefix == 'obser') {
        		dlg_obsSponsorId = document.getElementById(obsPreFix+'SponsorId.' + i);
        		dlg_obsDocumentId = document.getElementById(obsPreFix+'DocumentId.' + i);
        }

        if (dlg_obsCode.value != '' && dlg_obsCode.value != null && dlg_obsCodeType.value != '' && dlg_obsCodeType.value != null) {
            validObsCount++;
            var obserCode = document.createElement('input');
            obserCode.type = "hidden";
            obserCode.name = hidObsPrefix+'Code.' + dlg_chargeId;
            obserCode.id = hidObsPrefix+'Code.' + dlg_chargeId + validObsCount;
            obserCode.value = dlg_obsCode.value;
            obsParentDiv.appendChild(obserCode);

            var obserType = document.createElement('input');
            obserType.type = "hidden";
            obserType.name = hidObsPrefix+'Type.' + dlg_chargeId;
            obserType.id = hidObsPrefix+'Type.' + dlg_chargeId + validObsCount;
            obserType.value = dlg_obsCodeType.value;
            obsParentDiv.appendChild(obserType);            

            var obserValue = document.createElement('input');
            obserValue.type = "hidden";
            obserValue.name = hidObsPrefix+'Value.' + dlg_chargeId;
            obserValue.id = hidObsPrefix+'Value.' + dlg_chargeId + validObsCount;
            obserValue.value = dlg_obsValue.value;
            obsParentDiv.appendChild(obserValue);

            var obserValueType = document.createElement('input');
            obserValueType.type = "hidden";
            obserValueType.name = hidObsPrefix+'ValueType.' + dlg_chargeId;
            obserValueType.id = hidObsPrefix+'ValueType.' + dlg_chargeId + validObsCount;
            obserValueType.value = dlg_obsValueType.value;
            obsParentDiv.appendChild(obserValueType);

            var obserValueEditable = document.createElement('input');
            obserValueEditable.type = "hidden";
            obserValueEditable.name = hidObsPrefix+'ValueEditable.' + dlg_chargeId;
            obserValueEditable.id = hidObsPrefix+'ValueEditable.' + dlg_chargeId + validObsCount;
            obserValueEditable.value = dlg_obsValueEditable.value;
            obsParentDiv.appendChild(obserValueEditable);

            var obserCodeDesc = document.createElement('input');
            obserCodeDesc.type = "hidden";
            obserCodeDesc.name = hidObsPrefix+'CodeDesc.' + dlg_chargeId;
            obserCodeDesc.id = hidObsPrefix+'CodeDesc.' + dlg_chargeId + validObsCount;
            obserCodeDesc.value = dlg_codeDesc.value;
            obsParentDiv.appendChild(obserCodeDesc);

			if(obsPreFix == 'obs' && hidObsPrefix == 'obser'){
	            var obserSponsorId = document.createElement('input');
	            obserSponsorId.type = "hidden";
	            obserSponsorId.name = hidObsPrefix+'SponsorId.' + dlg_chargeId;
	            obserSponsorId.id = hidObsPrefix+'SponsorId.' + dlg_chargeId + validObsCount;
	            obserSponsorId.value = dlg_obsSponsorId.value;
	            obsParentDiv.appendChild(obserSponsorId);
	            
	            var obserDocumentId = document.createElement('input');
	            obserDocumentId.type = "hidden";
	            obserDocumentId.name = hidObsPrefix+'DocumentId.' + dlg_chargeId;
	            obserDocumentId.id = hidObsPrefix+'DocumentId.' + dlg_chargeId + validObsCount;
	            obserDocumentId.value = dlg_obsDocumentId.value;
	            obsParentDiv.appendChild(obserDocumentId);
            }

			if(!document.getElementById(hidObsPrefix+'Img.'+ dlg_chargeId)) {
				var obserFlag = document.createElement('img');
				obserFlag.id = hidObsPrefix+'Img.'+ dlg_chargeId;
			    obserFlag.src = cpath+"/images/yellow_flag.gif";
            	obsParentDiv.appendChild(obserFlag);
			}

        }
    }
    obsIndx.value = validObsCount;
}


function openNextTrtDialog() {
    var nRow = YAHOO.util.Dom.getNextSibling(rowUnderEdit);
    if (trtFieldEdited) {
        editTrtGrid();
    }
    if (nRow != null) {
        YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
        var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[LAST_CELL]);
        var trtDrug = getElementByName(nRow, 'trtDrug').value;
        if (trtDrug == 'true') {
            openEditTrtDialog(anchor, 'drug');
        } else {
            openEditTrtDialog(anchor);
        }
    }
}


function openPreviousTrtDialog() {
    var prevRow = YAHOO.util.Dom.getPreviousSibling(rowUnderEdit);
    var nPrevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
    if (trtFieldEdited) {
        editTrtGrid();
    }
    if (nPrevRow != null) {
        YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
        var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[LAST_CELL]);
        openEditTrtDialog(anchor);
    }

}

function editTrtGrid() {
	saveTrtObservations(); // saves the observations to backend
    var codeType = editTrt ? document.EditTrtForm.trtCodeType.value : document.EditTrtForm.drgCodeType.value;
    var code = document.EditTrtForm.trtCode.value;
    // title contains the actual value. value contains the truncated value.
    var codeDesc = document.getElementById('trtCodeDesc').title;
    if (codeDesc == '') document.getElementById('trtCodeDesc').textContent;

    if (editTrt) {
        getElementByName(rowUnderEdit, 'act_rate_plan_item_code').value = code;
        getElementByName(rowUnderEdit, 'trt_code_type').value = codeType;
        getElementByName(rowUnderEdit, 'trt_mster_desc').value = codeDesc;
    } else {
        getElementByName(rowUnderEdit, 'item_code').value = code;
        getElementByName(rowUnderEdit, 'drg_code_type').value = codeType;
        getElementByName(rowUnderEdit, 'trt_mster_desc').value = codeDesc;
    }
    setNodeText(rowUnderEdit.cells[CODE_TYPE], codeType);
    setNodeText(rowUnderEdit.cells[CODE], code);
    addObservationsToGrid('Treatment');

	var isTpa = getElementByName(rowUnderEdit, 'is_tpa').value;
    if ( isTpa == 'true'  ) {
		//prior auth info for insu visit
		var primaryClaimID = getElementByName(rowUnderEdit, "primary_claim_id");
    	var secondaryClaimID = getElementByName(rowUnderEdit, "secondary_claim_id");

    	if (!empty(primaryClaimID.value)) {
		    getElementByName(rowUnderEdit, 'primary_auth_id').value = document.EditTrtForm.trtPreAuthIdP.value;
		    getElementByName(rowUnderEdit, 'primary_auth_mode_id').value = document.EditTrtForm.trtPreAuthModeIdP.value;
	    }

	    if (!empty(secondaryClaimID.value)) {
		    getElementByName(rowUnderEdit, 'secondary_auth_id').value = document.EditTrtForm.trtPreAuthIdS.value;
		    getElementByName(rowUnderEdit, 'secondary_auth_mode_id').value = document.EditTrtForm.trtPreAuthModeIdS.value;
		}
    }

    trtDialog.cancel();
    YAHOO.util.Dom.addClass(rowUnderEdit, 'editing');
    trtFieldEdited = false;
}


function openEditLoincDialog(obj) {
    loincDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
    loincDialog.show();

    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    rowUnderEdit = row;
    YAHOO.util.Dom.addClass(row, 'editing');
    var itemCode;
    var codeType;
    var codeDesc;
    var masterDesc;

    itemCode = getElementByName(row, 'result_code').value;
    codeType = getElementByName(row, 'loinc_code_type').value;
    codeDesc = getElementByName(row, 'loinc_description').value;

    initLoincCodesAutocomp('loincCode', 'loincDropDown', codeType);
    var elNewItem = matches(itemCode, loincCodeAutoComp);
    loincCodeAutoComp._selectItem(elNewItem);
    setNodeText(document.getElementById('loincCodeDesc'), codeDesc, 35);
    document.EditLOINCForm.loincCodeType.value = codeType;
}

function openNextLoincDialog() {
    var nRow = YAHOO.util.Dom.getNextSibling(rowUnderEdit);
    if (loincFieldEdited) {
        editLoincGrid();
    }
    if (nRow != null) {
        YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
        var anchor = YAHOO.util.Dom.getFirstChild(nRow.cells[5]);
        openEditLoincDialog(anchor);
    }
}

function openPreviousLoincDialog() {
    var prevRow = YAHOO.util.Dom.getPreviousSibling(rowUnderEdit);
    var nPrevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
    if (loincFieldEdited) {
        editLoincGrid();
    }
    if (nPrevRow != null) {
        YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
        var anchor = YAHOO.util.Dom.getFirstChild(prevRow.cells[5]);
        openEditLoincDialog(anchor);
    }
}

function editLoincGrid() {
    var codeType = document.EditLOINCForm.loincCodeType.value;
    var code = document.EditLOINCForm.loincCode.value;
    var codeDesc = document.getElementById('loincCodeDesc').textContent;

    getElementByName(rowUnderEdit, 'result_code').value = code;
    getElementByName(rowUnderEdit, 'loinc_code_type').value = codeType;
    getElementByName(rowUnderEdit, 'loinc_mster_desc').value = codeDesc;

    setNodeText(rowUnderEdit.cells[3], codeType);
    setNodeText(rowUnderEdit.cells[4], code);
    YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
    loincDialog.cancel();
}

function openEditEncDialog(obj) {
    encDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
    encDialog.show();
    initEncCodeAutos();
    initTransferHospSourceAutoCom();
    initTransferHospDestAutoCom();
    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    rowUnderEdit = row;
    initEncDateAndTime(row);
    var encTypeCode = getElementByName(row, 'encounter_type').value;
    var encStartCode = getElementByName(row, 'encounter_start_type').value;
    var encEndCode = getElementByName(row, 'encounter_end_type').value;

    var encStartSource = getElementByName(row, 'encounter_start_source').value;
    var encEndDestination = getElementByName(row, 'encounter_end_destination').value;
    var encStartSourceId = getElementByName(row, 'encounter_start_source_id').value;
    var encEndDestinationId = getElementByName(row, 'encounter_end_destination_id').value;

    var elNewItem = matches(encTypeCode, encAuto1);
    encAuto1._selectItem(elNewItem);
    elNewItem = matches(encStartCode, encAuto2);
    encAuto2._selectItem(elNewItem);
    elNewItem = matches(encEndCode, encAuto3);
    encAuto3._selectItem(elNewItem);

    setNodeText(document.getElementById('encTypeCodeDesc'), getElementByName(row, 'encounter_type_desc').value, 35);
    setNodeText(document.getElementById('encStartCodeDesc'), getElementByName(row, 'encounter_start_type_desc').value, 35);
    setNodeText(document.getElementById('encEndCodeDesc'), getElementByName(row, 'encounter_end_type_desc').value, 35);
    document.EncounterForm._encTypeCodeDesc.value = getElementByName(row, 'encounter_type_desc').value;
    document.EncounterForm._encStartCodeDesc.value = getElementByName(row, 'encounter_start_type_desc').value;
    document.EncounterForm._encEndCodeDesc.value = getElementByName(row, 'encounter_end_type_desc').value;

    document.EncounterForm.encStartSource.value = encStartSource ;
    document.EncounterForm.encEndDestination.value = encEndDestination ;
    document.EncounterForm._encStartSource.value = encStartSourceId ;
    document.EncounterForm._encEndDestination.value = encEndDestinationId ;
}

function editEncGrid() {
    if(!validateEncDuration()){
      document.EncounterForm.encEndTime.focus();
      return;
    }
    var encTypeCode = document.EncounterForm.encCode.value;
    var encStartCode = document.EncounterForm.encStartCode.value;
    var encEndCode = document.EncounterForm.encEndCode.value;

    var encStartSourceId = document.EncounterForm._encStartSource.value;
    var encStartSource = document.EncounterForm.encStartSource.value;
    var encEndDest = document.EncounterForm.encEndDestination.value;
    var encEndDestId = document.EncounterForm._encEndDestination.value;

    var encTypeCodeDesc = document.EncounterForm._encTypeCodeDesc.value;
    var encStartCodeDesc = document.EncounterForm._encStartCodeDesc.value;
    var encEndCodeDesc = document.EncounterForm._encEndCodeDesc.value;

    let encEndDate = null;
    let encEndTime = null;
    if (document.EncounterForm.encEndDate !== undefined
        && document.EncounterForm.encEndTime !== undefined) {
      encEndDate = document.EncounterForm.encEndDate.value;
      encEndTime = document.EncounterForm.encEndTime.value;
    }

    if (encTypeCode == null || encTypeCode == 0 || encTypeCode == "") {
        encTypeCodeDesc = '';
    }

    if (encStartCode == null || encStartCode == 0 || encStartCode == "") {
        encStartCodeDesc = '';
    }

    if (encEndCode == null || encEndCode == 0 || encEndCode == "") {
        encEndCodeDesc = '';
    }

    if( encStartSource == null || encStartSource == "") {
       encStartSourceId = '';
    }

    if( encEndDest == null || encEndDest == "") {
       encEndDestId = '';
    }

    getElementByName(rowUnderEdit, 'encounter_type').value = encTypeCode;
    getElementByName(rowUnderEdit, 'encounter_start_type').value = encStartCode;
    getElementByName(rowUnderEdit, 'encounter_end_type').value = encEndCode;

    getElementByName(rowUnderEdit, 'encounter_start_source_id').value = encStartSourceId;
    getElementByName(rowUnderEdit, 'encounter_start_source').value = encStartSource;
    getElementByName(rowUnderEdit, 'encounter_end_destination').value = encEndDest;
    getElementByName(rowUnderEdit, 'encounter_end_destination_id').value = encEndDestId;

    getElementByName(rowUnderEdit, 'encounter_type_desc').value = encTypeCodeDesc;
    getElementByName(rowUnderEdit, 'encounter_start_type_desc').value = encStartCodeDesc;
    getElementByName(rowUnderEdit, 'encounter_end_type_desc').value = encEndCodeDesc;

    getElementByName(rowUnderEdit, 'encounter_end_date').value = encEndDate;
    getElementByName(rowUnderEdit, 'encounter_end_time').value = encEndTime;

    setNodeText(rowUnderEdit.parentElement.children[0].cells[2], (encTypeCode) + '-' + encTypeCodeDesc);
    setNodeText(rowUnderEdit.parentElement.children[1].cells[2], (encStartCode) + '-' + encStartCodeDesc);
    setNodeText(rowUnderEdit.parentElement.children[2].cells[2], (encEndCode) + '-' + encEndCodeDesc);
  if (encEndDate !== null && encEndTime !== null) {
    setNodeText(rowUnderEdit.parentElement.children[2].cells[4], (encEndDate) + ' ' + encEndTime);
  }
    encDialog.cancel();
}

function validateDRGCode() {
	var drgCode = document.MRDUpdateForm.drg_code;
	var drgChargeId = document.MRDUpdateForm.drg_charge_id;
	var useDRG =  document.MRDUpdateForm.use_drg;
	if (!empty(useDRG) && useDRG.value == 'Y'
			&& drgCode && trim(drgCode.value) == '') {
		alert("DRG Code is required...\nPlease select the DRG Code... ");
		return false;
	}
	return true;
}

function validateDRGBills() {
	var drgCode = document.MRDUpdateForm.drg_code;
	if (drgCode && trim(drgCode.value) != '' &&
			!empty(visitTpaBillsCnt) && visitTpaBillsCnt != 0) {
		alert("  DRG Code cannot be added. \n "
			+" This visit has "+visitTpaBillsCnt+ " bill(s) which are connected to TPA. \n "
			+" Please disconnect them from TPA. \n "
			+" Connect TPA to the primary bill later bill and select DRG.");
		return false;
	}
	return true;
}

function validate(ceedcheck) {

    if(validateDateFormat(document.MRDUpdateForm.encounter_end_date.value) != null){ // Date format other than `DD-MM-YYYY`, then only do the formatting
      document.MRDUpdateForm.encounter_end_date.value = formatDate(new Date(document.MRDUpdateForm.encounter_end_date.value), 'ddmmyyyy', '-');
    }
    let encEndDateTimeCurrent = mergeDateAndTime(document.MRDUpdateForm.encounter_end_date.value, document.MRDUpdateForm.encounter_end_time.value)
    const encounterEndDiff = (encEndDateTimeCurrent - new Date(maxOrderDateTime)) / 1000; //In seconds
    if (encounterEndDiff > 0){
      document.MRDUpdateForm.is_enc_end_overridden.value = 'Y';
    }

	var codification_status = document.MRDUpdateForm.codification_status.value;
	if (codification_status == '') {
		alert("Please select the codification status");
		document.MRDUpdateForm.codification_status.focus();
		return false;
	}

	if (!document.getElementById('finalizeAll').disabled
			&& document.getElementById('finalizeAll').checked) {
		if (!validateDRGCode()) {
			return false;
		}
	}

	if (!validateDRGBills()) {
		return false;
	}

	if (eclaimXMLSchema == 'DHA') {
		var presentingComplaints = getPresentingComplaintObservationCount();
		if (presentingComplaints > 1) {
			alert("More than one Presenting-complaint observation code (or) values are not allowed for consultation.");
			return false;
		}
	}

	if (!document.getElementById('finalizeAll').disabled
			&& document.getElementById('finalizeAll').checked) {
		if (!validateBabyCustomFields()) {
			return false;
		}
	}

	if(!empty(ceedcheck) && ceedcheck) {
		if(!ceedCheckIfPrimaryDiagnosisPresent()) {
			return false;
		}
		if(!ceedCheckIfAtleastOneTreatmentCodePresent()) {
			return false;
		}
		var ceedchecktype = document.getElementById("ceed_check_type").value;
		document.MRDUpdateForm.ceedchecktype.value = ceedchecktype;
		document.MRDUpdateForm.ceedcheck.value = 'Y';
	}

	if (codification_status == 'P') {
		// if the codification in progress. no need to validate.
		document.MRDUpdateForm.submit();
		if (!empty(ceedcheck) && ceedcheck) {
			eventTracking('Code Check Submission','Code Check Submission','Code Check Submission');
		}
		return true;
	}
	var encTable = document.getElementById('encounter');
    if (encTable) {
        var row = encTable.rows[0];
        var encStartEmpty = (getElementByName(row, 'encounter_start_type').value == '');
        var encEndEmpty = (getElementByName(row, 'encounter_end_type').value == '');
        var encType = getElementByName(row, 'encounter_type').value;
        var encTypeEmpty = (encType == '');


        if (encntrStartAndEndReqd != 'NR') {
            if (encntrStartAndEndReqd == 'RQ' && (encStartEmpty || encEndEmpty)) {
                alert("Encounter Start and End codes are required");
                return false;
            }
            if (encntrStartAndEndReqd == 'IP' && (encStartEmpty || encEndEmpty) && patientType == 'i') {
                alert("Encounter Start and End codes are required");
                return false;
            }
            if (encntrStartAndEndReqd == 'OP' && (encStartEmpty || encEndEmpty) && patientType != 'o') {
                alert("Encounter Start and End codes are required");
                return false;
            }
        }

        if (encntrTypeReqd != 'NR') {
            if (encntrTypeReqd == 'RQ' && (encTypeEmpty)) {
                alert("Encounter Type code is required");
                return false;
            }
            if (encntrTypeReqd == 'IP' && (encTypeEmpty) && patientType == 'i') {
                alert("Encounter Type code is required");
                return false;
            }
            if (encntrTypeReqd == 'OP' && (encTypeEmpty) && patientType != 'i') {
                alert("Encounter Type code is required");
                return false;
            }
        }

        var encStart = getElementByName(row, 'encounter_start_type').value;
        var encEnd = getElementByName(row, 'encounter_end_type').value;

        var encStartSource = document.EncounterForm.encStartSource.value;
    	  var encEndDest = document.EncounterForm.encEndDestination.value;

        if ((encStart == '3' || encStart == '8') && empty(encStartSource)) {
        		alert("Encounter Transfer From is required");
        		openEditEncDialog(document.getElementById("encounterHref"));
        		document.EncounterForm.encStartSource.focus();
        		return false;
        }

        if ((encEnd == '4' || encEnd =='7') && empty(encEndDest)) {
				alert("Encounter Transfer To is required");
				openEditEncDialog(document.getElementById("encounterHref"));
				document.EncounterForm.encEndDestination.focus();
        		return false;
        }
    }

    var diagnosisTable = document.getElementById('diagnosiscodes');
    if (diagnosisTable) {

        var len = diagnosisTable.rows.length;
        var fieldsEmpty = false;
        var validateCodification = (codification_status != 'P');
		var primaryPresent = false;
       	var codeBlank = false;
        if (len >= 2) {
          for (var i = 0; i < (len - 2); i++) {
	           var row = diagnosisTable.rows[i];
	           var deletedEl = getElementByName(row, 'deleted');
	           var diagPOA = getElementByName(row, 'present_on_admission');
	           var diagIcd = getElementByName(row, 'icd_code');
	           if (deletedEl.value != 'true') {
	               if(getElementByName(row, 'diag_type').value == 'P'
	               		&& diagIcd.value != ''
	               		&& diagIcd.value != null ) {
	               	primaryPresent = true;
	               }
	               if(getElementByName(row, 'diag_type').value == 'P' &&
	               		(diagIcd.value == '' || diagIcd.value == null))
	               codeBlank = true;
	           }
	           //validate present_on_admission
	           if (encTable && (encType == '3' || encType == '4')
						&& (!diagPOA || diagPOA.value == '') && patientType =='i') {
					alert("Please enter the Present On Admission (POA) value for Diagnosis " + diagIcd.value);
					return false;
				}
           }
          	if(!primaryPresent){
          		alert("Primary Diagnosis required...\nPlease select one of the diagnoses as Primary...");
          		return false;
			}else if(codeBlank) {
				alert("Primary Diagnosis required...\nPlease select a valid Code for Primary Diagnosis... ");
				return false;
			}
            for (var i = 1; i < (len - 3); i = i + 2) {
                var row = diagnosisTable.rows[i];
                fieldsEmpty = fieldsEmpty || (getElementByName(row, 'icd_code').value == '');
            }

            if (fieldsEmpty) if (!confirm("Some of the Diagnosis codes not entered, Proceed anyway ?.")) return false;
            if (!isCodeTypeSameForAll()) {
            	alert("Multiple Diagnosis Code Types have been selected which may not be valid. Please check and try again.")
            	return false;
            }
        }
    }



	var trtTable = document.getElementById('treatment');
    var trtFeildsEmty = false;

    if (trtTable != null) {
        var len = trtTable.rows.length;
        for (var i = 0; i < len - 1; i++) {
            var row = trtTable.rows[i + 1];
            var trtDrug = getElementByName(row, 'trtDrug').value;
            var codeType = (trtDrug == 'false') ? getElementByName(row, 'trt_code_type').value : getElementByName(row, 'drg_code_type').value;
            var trtDrug = getElementByName(row, 'trtDrug').value;
            var code = (trtDrug == 'false') ? getElementByName(row, 'act_rate_plan_item_code').value : getElementByName(row, 'item_code').value;

            if (code != '' && codeType != '') {
                if (!validateCode(row)) return false;
            }
            trtFeildsEmty = trtFeildsEmty || ((validateCodification && (code == '' || codeType == '')) || (!validateCodification && ((code != '' && codeType == '') || (code == '' && codeType != ''))));
        }

        if (trtFeildsEmty) {
            if (!confirm("Some of the Treatment Codes/Types not entered, Proceed anyway ?.")) return false;
        }
    }
	if(!validateLoincCode()) {
		return false;
	}

	if(tooth_numbering_system == 'U' && !validateToothCode()) {
		return false;
	}

	if (codification_status == 'C') {
		if (!validateDRGCode()) {
			return false;
		}
	}

	if (patientType != 'i' && eclaimXMLSchema == 'DHA') {
		if (codification_status == 'C') {
			if (!presentingComplaintObservationValuesEmpty()) {
				alert("Presenting-complaint observation code (or) value not entered for consultation.");
				return false;
			}
		}
	}

	if (anyVitalObservationValuesEmpty()) {
		if (!confirm("Warning: Some of the Consultation Observation Value(s) are not entered, Proceed anyway ?."))
			return false;
	}

	if (anyObservationValuesEmpty()) {
		if (!confirm("Warning: Some of the Treatment Observation Value(s) are not entered, Proceed anyway ?."))
			return false;
	}
	if(mod_ceed_enabled && !ceedstatus) {
		var codification_status_message = null;
		if(codification_status == "C") {
			codification_status_message = "Completed";
		}
		else if(codification_status == "R") {
			codification_status_message = "Completed-Needs Verification";
		}
		else if(codification_status == "V") {
			codification_status_message = "Verified and Closed";
		}
		var warning = "Warning: Ceed Check is not done yet. Do you want to mark codification status as "
						+ codification_status_message + " ?";
		if(!confirm(warning)) {
			return false;
		}
	}
	if( modcoderReview == 'Y' && (codification_status == 'R' || codification_status == 'V') ) {
		//Get open+inprogress reviews for current visit id
		var url = cpath + "/pages/medicalrecorddepartment/MRDUpdate.do?_method=getOpenReviewCount&visitId="+document.getElementById('patId').value;
	  	var ajaxReqObject = newXMLHttpRequest();
	  	ajaxReqObject.open("POST",url.toString(), false); //synchronous call
	  	ajaxReqObject.send(null);
	  	if (ajaxReqObject.readyState == 4) {
	  	    if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
	  	    	reviewsCount = eval(ajaxReqObject.responseText);
		    	document.getElementById('reviewCountId').innerHTML = reviewsCount;
		    	if( reviewsCount > 0 ){
					alert(getString("js.coder.claim.reviews.validation.reviews.found.cannot.close",reviewsCount));
					return false;
				 }
	  	    }
	  	}
	}
	document.MRDUpdateForm.submit();
    if (!empty(ceedcheck) && ceedcheck) {
		eventTracking('Code Check Submission','Code Check Submission','Code Check Submission');
	}
    return true;
}

function ceedCheckIfPrimaryDiagnosisPresent() {
	var diagnosisTable = document.getElementById('diagnosiscodes');
    if (diagnosisTable) {

        var len = diagnosisTable.rows.length;
		var primaryPresent = false;

        if (len >= 2) {
          for (var i = 0; i < (len - 1); i++) {
	           var row = diagnosisTable.rows[i];
               if(row.style.display != 'none' && getElementByName(row, 'diag_type').value == 'P') {
            	   primaryPresent = true;
               }

           }
          	if(!primaryPresent){
          		alert("Primary Diagnosis is required for Ceed Check.");
          		return false;
			}
          	else {
          		return true;
          	}
        }
    }
    alert("Diagnosis section is not present. Ceed Check cannot be performed.");
    return false;
}

function ceedCheckIfAtleastOneTreatmentCodePresent() {
	var trtTable = document.getElementById('treatment');

    if (trtTable != null) {
        var len = trtTable.rows.length;
        if(len == 0) {
        	alert("At least one Treatment code is required for Ceed Check.");
        	return false;
        }
        else {
        	return true;
        }
    }
    alert("Treatment Codes section is not present. Ceed Check cannot be performed.");
    return false;
}

function get_previoussibling(n){
//n = <author>
	var x=n.previousSibling;
	while (x.nodeType!=1){
		x=x.previousSibling;
	}
	return x;
}

function get_nextsibling(n){
//n = <author>
	var x=n.nextSibling;
	while (x.nodeType!=1){
		x=x.nextSibling;
	}
	return x;
}

function findSubListInList(list, varName, varValue) {
	var subList = new Array();
	var j = 0;
	if (list == null) return null;
	for (var i=0; i<list.length; i++) {
		if (list[i][varName] == varValue) {
			subList[j++] = list[i];
		}
	}
	return empty(subList)? null : subList;
}

function validateToothCode(){
	var trtTable = document.getElementById('treatment');
    var trtFeildsEmty = false;
    if (trtTable != null) {
    	var len = trtTable.rows.length;
        for (var ii = 0; ii < len - 1; ii++) {
            var row = trtTable.rows[ii + 1];
            var trtDrug = getElementByName(row, 'trtDrug').value;
            var codeType = (trtDrug == 'false') ? getElementByName(row, 'trt_code_type').value : getElementByName(row, 'drg_code_type').value;
            var trtDrug = getElementByName(row, 'trtDrug').value;
            var code = (trtDrug == 'false') ? getElementByName(row, 'act_rate_plan_item_code').value : getElementByName(row, 'item_code').value;
			var isDental = findInList2(mrdSupportedCodeTypes, "code_type", codeType, "haad_code","6" ) != null;
			var isToothNumRequired = getElementByName(row, 'tooth_num_reqd').value;
			if (code != '' && codeType != '') {
			 	if(isDental && isToothNumRequired == 'Y'){
		 			var chargeObj = getElementByName(row, 'charge_id');
		 			var chargeId = getElementByName(row, 'charge_id').value;
			 		if(document.getElementById('observations.'+chargeId)) {
			 			var act_desc = get_previoussibling(get_previoussibling(chargeObj));
						//find the number of observations
						var observations = document.getElementById('observations.'+chargeId).childNodes;
						var observationsLength = document.getElementById('observations.'+chargeId).childNodes.length;
			 			if(act_desc!= null && act_desc!='') {
							// if there are no results of type Universal Dental..alert user.
							if(observationsLength<=3) {
								alert('Save could not be completed...\nTooth Number absent for '+get_previoussibling(chargeObj).value);
								setSelectedIndex(document.getElementById('codification_status'),document.getElementById('last_codification_status').value =='C'?'P':document.getElementById('last_codification_status').value);
								return false;
							}
						}
						var obsResultCount = 0;
						for(var j=0; j<observationsLength; j++ ){
							// find out if the observation is of code type Universal dental
							var typeRe = new RegExp("^obserType\\."+chargeId,"gi");
							var codeRe = new RegExp("Universal Dental","gi");
							if(observations[j].type == 'hidden') {
								var obsId = (observations[j].id) + "";
								if(obsId.match(typeRe) && (observations[j].value).match(codeRe)) {
									obsResultCount++;
								}
							}
						}
						if(obsResultCount == 0){
							alert('Save could not be completed...\nTooth Number required for '+get_previoussibling(chargeObj).value);
							setSelectedIndex(document.getElementById('codification_status'),document.getElementById('last_codification_status').value =='C'?'P':document.getElementById('last_codification_status').value);
							return false;
						}

			 		}
			 	}
			}
        }
    }
	return true;
}

function validateLoincCode() {
	var chargeIds = document.getElementsByName("charge_id");
	for( var i=0; i<chargeIds.length; i++) {
		var chargeId = chargeIds[i].value;
		// if observations exist for the charge
		if(document.getElementById('observations.'+chargeId)) {
			var act_desc = get_previoussibling(get_previoussibling(chargeIds[i]));
			//find the number of observations
			var observations = document.getElementById('observations.'+chargeId).childNodes;
			var observationsLength = document.getElementById('observations.'+chargeId).childNodes.length;
			//check if its an ordered non-LOINC test updated recently to LOINC status, after this charge was posted
			if(act_desc!= null && act_desc!='') {
				var isTestWithResult = findInList(testResultsData, "test_id", act_desc.value) != null;
				// if there are no results of LOINC type and the LOINC type results are needed alert the user of it.
				if(isTestWithResult && observationsLength<=3) {
					alert('Save could not be completed...\nLOINC type observations absent for '+get_previoussibling(chargeIds[i]).value);
					setSelectedIndex(document.getElementById('codification_status'),document.getElementById('last_codification_status').value =='C'?'P':document.getElementById('last_codification_status').value);
					return false;
				}
			}
			var obsResultCount = 0;
			for(var j=0; j<observationsLength; j++ ){
				// find out if the observation is of code type LOINC
				var typeRe = new RegExp("^obserType\\."+chargeId,"gi");
				var codeRe = new RegExp("LOINC","gi");
				if(observations[j].type == 'hidden') {
					var obsId = (observations[j].id) + "";
					if(obsId.match(typeRe) && (observations[j].value).match(codeRe)) {
						obsResultCount++;
						var codeValue = get_nextsibling(observations[j]).value;
						//check if all values are present for a code of type LOINC, else display error
						if(codeValue == null || codeValue == '') {
							var item = get_previoussibling(chargeIds[i]);// the item for which LOINC value is not present
							alert('Save could not be completed...\nLOINC type observations with no result value present');
							setSelectedIndex(document.getElementById('codification_status'),document.getElementById('last_codification_status').value =='C'?'P':document.getElementById('last_codification_status').value);
							return false;
						}
					}
				}
			}
			var resultsNeeded = findSubListInList(testResultsData, "test_id", act_desc.value);

			var resultCountNeeded =  resultsNeeded== null || empty(resultsNeeded) ? 0: resultsNeeded.length;
			if(obsResultCount < resultCountNeeded) {
				var item = get_previoussibling(chargeIds[i]);
				alert('Save could not be completed...\n Additional LOINC type result parameters needed for '+get_previoussibling(chargeIds[i]).value);
				setSelectedIndex(document.getElementById('codification_status'),document.getElementById('last_codification_status').value);
				return false;
			}
		}

	}
	return true;
}

function anyVitalObservationValuesEmpty() {
	var isEmpty = false;
	var vitalCharges = document.getElementsByName("vitalCharge_id");
	for (var j=0; j<vitalCharges.length; j++) {
		var vitalChargesObs = document.getElementsByName("vitalObserCode." +vitalCharges[j].value);
		isEmpty = false;
		for (var i=0; i<vitalChargesObs.length; i++) {
	      var dlg_obsCode = document.getElementById("vitalObserCode." +vitalCharges[j].value + (i+1));
	      var dlg_obsCodeType = document.getElementById("vitalObserType." +vitalCharges[j].value + (i+1));
	      var dlg_obsValue = document.getElementById("vitalObserValue." +vitalCharges[j].value + (i+1));
	      var dlg_obsValueType = document.getElementById("vitalObserValueType." +vitalCharges[j].value + (i+1));
	      var dlg_obsValueEditable = document.getElementById("vitalObserValueEditable." +vitalCharges[j].value + (i+1));

	      if (dlg_obsCodeType != null && dlg_obsCodeType.value != ''
	      			&& dlg_obsCode != null && (dlg_obsCode.value).trim() != ''
	      			&& dlg_obsValue != null && (dlg_obsValue.value).trim() == '' ) {
				isEmpty = true;
				break;
	      }
	   }
	   if (isEmpty) {
		 	return isEmpty;
		}
	}
	return isEmpty;
}

function anyObservationValuesEmpty() {
	var isEmpty = false;
	var charges = document.getElementsByName("charge_id");
	for (var j=0; j<charges.length; j++) {
		var chargesObs = document.getElementsByName("obserCode." +charges[j].value);
		isEmpty = false;
		for (var i=0; i<chargesObs.length; i++) {
	      var dlg_obsCode = document.getElementById("obserCode." +charges[j].value + (i+1));
	      var dlg_obsCodeType = document.getElementById("obserType." +charges[j].value + (i+1));
	      var dlg_obsValue = document.getElementById("obserValue." +charges[j].value + (i+1));
	      var dlg_obsValueType = document.getElementById("obserValueType." +charges[j].value + (i+1));
	      var dlg_obsValueEditable = document.getElementById("obserValueEditable." +charges[j].value + (i+1));
	      var dlg_obsSponsorId = document.getElementById("obserSponsorId." +charges[j].value + (i+1));

	      if (dlg_obsCodeType != null && dlg_obsCodeType.value != ''
	      			&& dlg_obsCode != null && (dlg_obsCode.value).trim() != ''
	      			&& dlg_obsValue != null && (dlg_obsValue.value).trim() == '' ) {
				isEmpty = true;
				break;
	      }
	   }
	   if (isEmpty) {
		 	return isEmpty;
		}
	}
	return isEmpty;
}

function presentingComplaintObservationValuesEmpty() {
	var vitalCharges = document.getElementsByName("vitalCharge_id");
	if (vitalCharges.length == 0) {
		return true; // No consultations.
	}
	for (var j=0; j<vitalCharges.length; j++) {
		var vitalChargesObs = document.getElementsByName("vitalObserCode." +vitalCharges[j].value);
		if (vitalChargesObs.length == 0) {
			return false; // No observations
		}
	}

	for (var j=0; j<vitalCharges.length; j++) {
		var vitalChargesObs = document.getElementsByName("vitalObserCode." +vitalCharges[j].value);
		var hasPresentingComplaint = false;
		for (var i=0; i<vitalChargesObs.length; i++) {
	      var dlg_obsCode = document.getElementById("vitalObserCode." +vitalCharges[j].value + (i+1));
	      var dlg_obsCodeType = document.getElementById("vitalObserType." +vitalCharges[j].value + (i+1));
	      var dlg_obsValue = document.getElementById("vitalObserValue." +vitalCharges[j].value + (i+1));
	      var dlg_obsValueType = document.getElementById("vitalObserValueType." +vitalCharges[j].value + (i+1));
	      var dlg_obsValueEditable = document.getElementById("vitalObserValueEditable." +vitalCharges[j].value + (i+1));

	      if (dlg_obsCodeType != null && dlg_obsCodeType.value != ''
	      			&& dlg_obsCode != null && (dlg_obsCode.value).trim() == 'Presenting-Complaint') {
	      	hasPresentingComplaint = true;
	   		break;
			}
		}
		if (!hasPresentingComplaint) {
		 	return false;
		}
	}
	return true;
}

function getPresentingComplaintObservationCount() {
	var presentingComplaints = 0;
	var vitalCharges = document.getElementsByName("vitalCharge_id");
	for (var j=0; j<vitalCharges.length; j++) {
		var vitalChargesObs = document.getElementsByName("vitalObserCode." +vitalCharges[j].value);
		presentingComplaints = 0;
		for (var i=0; i<vitalChargesObs.length; i++) {
	      var dlg_obsCode = document.getElementById("vitalObserCode." +vitalCharges[j].value + (i+1));
	      var dlg_obsCodeType = document.getElementById("vitalObserType." +vitalCharges[j].value + (i+1));
	      var dlg_obsValue = document.getElementById("vitalObserValue." +vitalCharges[j].value + (i+1));
	      var dlg_obsValueType = document.getElementById("vitalObserValueType." +vitalCharges[j].value + (i+1));
	      var dlg_obsValueEditable = document.getElementById("vitalObserValueEditable." +vitalCharges[j].value + (i+1));
	      if (dlg_obsCodeType != null && dlg_obsCodeType.value != ''
	      			&& dlg_obsCode != null && (dlg_obsCode.value).trim() == 'Presenting-Complaint') {
	    			presentingComplaints++;
	      }
		}
		if (presentingComplaints > 1) {
			return presentingComplaints;
		}
	}
	return presentingComplaints;
}

function validateCode(row, category) {
    var trtDrug = getElementByName(row, 'trtDrug').value;
    var codeType = '';
    var anchor;

    var code = '';
    var codeCategory = category == null ? ((trtDrug == 'false') ? 'Treatment' : 'Drug') : category;

    if (codeCategory == 'Observations') {
        codeType = getElementByName(row, 'loinc_code_type').value;
        code = getElementByName(row, 'result_code').value;
        anchor = YAHOO.util.Dom.getFirstChild(row.cells[4]);
    } else {
        codeType = (trtDrug == 'false') ? getElementByName(row, 'trt_code_type').value : getElementByName(row, 'drg_code_type').value;
        code = (trtDrug == 'false') ? getElementByName(row, 'act_rate_plan_item_code').value : getElementByName(row, 'item_code').value;
        anchor = YAHOO.util.Dom.getFirstChild(row.cells[LAST_CELL]);
    }

    if( codeType == 'Drug HAAD' ){
    	return true;//HMS-13478:Drug HAAD/Drug DHA code replacement in item master(validation is not considered for Drug HAAD type)
    }

    var url = cpath + "/pages/medicalrecorddepartment/MRDUpdate.do?_method=validateCode&codeCategory=" + encodeURIComponent(codeCategory) + "&codeType=" + encodeURIComponent(codeType)
    	+ "&code=" + encodeURIComponent(code) + "&visit_type=" + patientType ;
    var reqObject = newXMLHttpRequest();
    reqObject.open("POST", url.toString(), false);
    reqObject.send(null);
    if (reqObject.readyState == 4) {
        if ((reqObject.status == 200) && (reqObject.responseText != null)) {
            var result = reqObject.responseText;
            if (result == 'invalid') {
                if (codeCategory == 'Observations') {
                    alert("Invalid LOINC code, please correct it");
                    openEditLoincDialog(anchor);
                } else {
                    alert("Invalid treatment code, please correct it");
                    openEditTrtDialog(anchor);
                }
                return false;
            }
        }
    }


    return true;
}

var diagnosisDescriptionDialog = null;

function initDescDialog() {
    diagnosisDescriptionDialog = new YAHOO.widget.Overlay("diagnosisDescriptionDialog", {
        context: ['mrd_codes', 'tr', 'bl'],
        visible: false,
        model: true,
        width: "300px"
    });
    diagnosisDescriptionDialog.render('addoreditDiagnosisCodeDialog');
    YAHOO.util.Event.addListener('cancelDiagnosisDescImg', "click", diagnosisDescriptionDialog.hide, diagnosisDescriptionDialog, true);
}

function showDiagnosisDescDialog(obj) {
    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    diagnosisDescriptionDialog.cfg.setProperty("context", [obj, "tr", "bl"], false);
    diagnosisDescriptionLabel.textContent = getElementByName(row, 'masterCodeIdDesc').value;
    diagnosisDescriptionDialog.render('addoreditDiagnosisCodeDialog');
    diagnosisDescriptionDialog.show();
}

var trtDescriptionDialog = null;

function initTrtDescDialog() {
    trtDescriptionDialog = new YAHOO.widget.Overlay("trtDescriptionDialog", {
        context: ['trtCodes', 'tr', 'bl'],
        visible: false,
        model: true,
        width: "300px"
    });
    trtDescriptionDialog.render('trtDialog');
    YAHOO.util.Event.addListener('cancelTrtDescImg', "click", trtDescriptionDialog.hide, trtDescriptionDialog, true);
}

function showTrtDescDialog(obj) {
    var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
    trtDescriptionDialog.cfg.setProperty("context", [obj, "tr", "bl"], false);
    trtDescriptionLabel.textContent = getElementByName(row, 'trtMasterDesc').value;
    trtDescriptionDialog.render('trtDialog');
    trtDescriptionDialog.show();
}

function reopenCodification() {
    document.MRDUpdateForm._method.value = 'reopenCodifiaction';
    document.MRDUpdateForm.submit();
}


function cancelDiagDialog() {
	hidetoolTip('diagInfo');
    diagnosisDialog.cancel();
}

function cancelConsulDialog() {
	YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
    consulDialog.cancel();
}

function onCloseConsDialog() {
    YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
}

function cancelTrtDialog() {
    YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
    trtDialog.cancel();
}

function onCloseTrtDialog() {
    YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
}

function cancelEncDialog() {
    encDialog.cancel();
}

function cancelLoincDialog() {
    loincDialog.cancel();
}

function onCloseLoincDialog() {}

function matches(mName, autocomplete) {
    var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ? (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
    elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
    return elListItem;
}

function displayReferredTo(selEl) {

    if (selEl.value == "Referred To") {
        document.getElementById("refToHospLabel").style.display = 'block';
        document.getElementById("refToHospDiv").style.display = 'block';
    } else {
        document.getElementById("refToHospLabel").style.display = 'none';
        document.getElementById("refToHospDiv").style.display = 'none';

    }
    if (selEl.value == "Expiry") {
        document.getElementById("deathDateLabel").style.display = 'block';
        document.getElementById("deathDateDiv").style.display = 'block';
        document.getElementById("deathReasonLabel").style.display = 'block';
        document.getElementById("deathReasonDiv").style.display = 'block';
    } else {
        document.getElementById("deathDateLabel").style.display = 'none';
        document.getElementById("deathDateDiv").style.display = 'none';
        document.getElementById("deathReasonLabel").style.display = 'none';
        document.getElementById("deathReasonDiv").style.display = 'none';
    }
}

function addObservationElements(obsPreFix) {
	var obsTable = document.getElementById(obsPreFix+'ValueTable');
	var obserIndex = document.getElementById(obsPreFix+'Index');
	var obserIndexValue = obserIndex.value;
    if (obserIndexValue == null || obserIndexValue == '') obserIndexValue = 1;

    if (obserIndexValue == 1) {
        // make the hidden row visible
        obsTable.style.display = 'block';
    } else {
        // add observation elements
        appendObservationElementsToTable(obserIndexValue, obsPreFix);
    }
    obserIndex.value = ++obserIndexValue;
}

/*handles the displaying of the file upload and download buttons*/
function showFileUpload (obsPrefix, curIndex) {
	if (obsPrefix == 'obs') {
		var codeType;
		var obsDocumentId;
		var row;
		var uploadFileEl;
		var downloadFileEl;
		var obsValue;
		var obsValueType;
		if (document.getElementById(obsPrefix+'CodeType.'+curIndex)
			&& document.getElementById(obsPrefix+'DocumentId.'+curIndex)) {
			codeType = document.getElementById(obsPrefix+'CodeType.'+curIndex);
			obsDocumentId = document.getElementById(obsPrefix+'DocumentId.'+curIndex);
			row = document.getElementById(obsPrefix + '.' + curIndex);
			uploadFileEl = getElementsByName(row, 'uploadFile')[0];
			downloadFileEl = getElementsByName(row, 'btnDownloadFile')[0];
			obsValue = document.getElementById(obsPrefix+'Value.'+curIndex);
			obsValueType = document.getElementById(obsPrefix+'ValueType.'+curIndex);
			
			if (codeType && codeType.value == 'File') {
				// disable value field and default value type field based on health authority
				obsValue.disabled = true;
				obsValue.value = '';
				obsValueType.disabled =  true;
				if (eclaimXMLSchema == 'DHA') {
					obsValueType.value = 'File';
				} else {
					obsValueType.value = 'Base64PDF';
				}
				// show the choose file option
				uploadFileEl.style = 'display:block;';
				uploadFileEl.disabled = false;
				downloadFileEl.disabled = false;
				// if file has already been uploaded, show download option and hide upload option
				if (obsDocumentId.value !='' && obsDocumentId.value && obsDocumentId.value != 0) {
					uploadFileEl.disabled = true;
					uploadFileEl.style = 'display:none;';
					downloadFileEl.style = 'display:block;';
					downloadFileEl.disable = true;
				}
			} else {
				// clear fields for other observation types
				uploadFileEl.style = 'display:none;';
				downloadFileEl.style = 'display:none;';
				uploadFileEl.disabled = true;
				obsValue.disabled = false;
				obsValueType.disabled =  false;
			}
		}
	}
	
}

function appendObservationElementsToTable(nextRowIndex, obsPreFix) {
    var obsTable = document.getElementById(obsPreFix+'ValueTable');
    var tr1 = obsTable.insertRow(-1);
    tr1.name = obsPreFix+'.' + nextRowIndex;
    tr1.id = obsPreFix+'.' + nextRowIndex;

    var td1 = tr1.insertCell(-1);

    var sel1 = document.createElement('select');
    sel1.name = obsPreFix+'CodeType.' + nextRowIndex;
    sel1.id = obsPreFix+'CodeType.' + nextRowIndex;
    sel1.setAttribute("onchange", "initObsCodesAutocomp('" + obsPreFix + "'," + nextRowIndex + "); showFileUpload('" + obsPreFix + "'," + nextRowIndex + ");");
    sel1.setAttribute("class", "dropDown");
    td1.appendChild(sel1);
    var opt1 = document.createElement('option');
    sel1.appendChild(opt1);
    loadSelectBox(sel1, observationCodeTypesList, 'CODE_TYPE', 'CODE_TYPE', "--Select--", "");

    var td2 = tr1.insertCell(-1);

    var div1 = document.createElement('div');
    div1.id = obsPreFix+'Auto.' + nextRowIndex;
    div1.setAttribute("style", "width:70px;");
    td2.appendChild(div1);

    var txtBx1 = document.createElement("input");
    txtBx1.type = "text";
    txtBx1.name = obsPreFix+'Code.' + nextRowIndex;
    txtBx1.id = obsPreFix+'Code.' + nextRowIndex;
    txtBx1.setAttribute("style", "width:70px;");
    div1.appendChild(txtBx1);

    var childDiv1 = document.createElement('div');
    childDiv1.id = obsPreFix+'DropDown.' + nextRowIndex;
    childDiv1.setAttribute("class", "scrolForContainer");
    div1.appendChild(childDiv1);

    var td3 = tr1.insertCell(-1);
    td3.setAttribute("style", "text-align:right;");
    var label1 = document.createElement('label');
    label1.id = obsPreFix+'CodeDesc.' + nextRowIndex;
    td3.appendChild(label1);

    var hiddenchild = document.createElement("input");
    hiddenchild.type = "hidden";
    hiddenchild.id = obsPreFix+'MasterCodeDesc.' + nextRowIndex;
    hiddenchild.name = obsPreFix+'MasterCodeDesc.' + nextRowIndex;
    td3.appendChild(hiddenchild);

    var helpImg = document.createElement('img');
    helpImg.id = obsPreFix+'HelpImg.' + nextRowIndex;
    helpImg.name = obsPreFix+'HelpImg.' + nextRowIndex;
    helpImg.setAttribute("src", cpath + "/images/help.png");
    helpImg.setAttribute("onclick", "showCodeDescription('" + obsPreFix + "'," + nextRowIndex + ")");
    td3.appendChild(helpImg);

    var td4 = tr1.insertCell(-1);
    var txtBx2 = document.createElement("input");
    txtBx2.id = obsPreFix+'Value.' + nextRowIndex;
    txtBx2.name = obsPreFix+'Value.' + nextRowIndex;
    txtBx2.type = "text";
    td4.appendChild(txtBx2);

    var td5 = tr1.insertCell(-1);
    var txtBx3 = document.createElement("input");
    txtBx3.id = obsPreFix+'ValueType.' + nextRowIndex;
    txtBx3.type = "text";
    txtBx3.name = obsPreFix+'ValueType.' + nextRowIndex;
    td5.appendChild(txtBx3);

    var hidden1 = document.createElement("input");
    hidden1.type = "hidden";
    hidden1.id = obsPreFix+'ValueEditable.' + nextRowIndex;
    hidden1.name = obsPreFix+'ValueEditable.' + nextRowIndex;
    td5.appendChild(hidden1);

	if(obsPreFix == 'obs') {
	    var hidden2 = document.createElement("input");
	    hidden2.type = "hidden";
	    hidden2.id = obsPreFix+'SponsorId.' + nextRowIndex;
	    hidden2.name = obsPreFix+'SponsorId.' + nextRowIndex;
	    td5.appendChild(hidden2);
	    
	    var hidden3 = document.createElement("input");
	    hidden3.type = "hidden";
	    hidden3.id = obsPreFix+'DocumentId.' + nextRowIndex;
	    hidden3.name = obsPreFix+'DocumentId.' + nextRowIndex;
	    td5.appendChild(hidden3);
    }

    var td6 = tr1.insertCell(-1);
    td6.name = obsPreFix+'Del.' + nextRowIndex;
    td6.id = obsPreFix+'Del.' + nextRowIndex;
    td6.width = "17px";
    td6.setAttribute("align", "right");
    td6.setAttribute("style", "padding-left: 5px; padding-right: 5px; height: 18px; width: 17px;");
    
    var td7 = tr1.insertCell(-1);
    var fileInput = document.createElement('input');
    fileInput.id = "uploadFile";
    fileInput.style = "display:none;";
    fileInput.disabled = true;
    fileInput.name = "uploadFile";
    fileInput.type = "file";
    fileInput.accept="image/png, image/jpeg, application/pdf";
    //file download button
    var fileDownloadButton = document.createElement('button');
    fileDownloadButton.id = "btnDownloadFile";
    fileDownloadButton.type = "button";
    fileDownloadButton.name = "btnDownloadFile";
    fileDownloadButton.style = "display:none;";
    fileDownloadButton.title = "Download File";
    fileDownloadButton.setAttribute( "onclick", "downloadFile("+nextRowIndex+");" );
    fileDownloadButton.innerText = "Download";
    td7.appendChild(fileInput);
    td7.appendChild(fileDownloadButton);

}

var obsAutocomp = null;

function downloadFile(rowIndex) {
	var observation = document.getElementById('obs.' + rowIndex);
	var documentId = getElementByName(observation, 'obsDocumentId.' + rowIndex).value;
	var mrNo = document.getElementById('mrNo').value;
	if (documentId) {
		var url = cpath + "/MedicalRecords/Codification/downloadDocument.htm";
		var params = "doc_id=" + documentId;
		params += "&mr_no=" + mrNo;
		window.open(url + '?' + params);
	}
}

function initObsCodesAutocomp(obsPreFix, curIndex) {
	for(var i=0; i< diagAutoCompleteArray.length; i++){
		if(diagAutoCompleteArray[i][0].prefix == obsPreFix && diagAutoCompleteArray[i][0].index == curIndex
					&& diagAutoCompleteArray[i][1]!= null) {
			var autoCmp = diagAutoCompleteArray[i][1];
			autoCmp.destroy();
			diagAutoCompleteArray[i][1] = null;
		}
	}

	if(obsAutocomp!= null){
		obsAutocomp.destroy();
	}
	document.getElementById(obsPreFix+'Code.'+curIndex).value ="";
	document.getElementById(obsPreFix+'MasterCodeDesc.'+curIndex).value ="";
	
	if(document.getElementById(obsPreFix+'DocumentId.'+curIndex) != null){
		document.getElementById(obsPreFix+'DocumentId.'+curIndex).value ="";
	}
	setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+curIndex), "", 5);
	var codeType = document.getElementById(obsPreFix+'CodeType.'+curIndex).value;
	document.getElementById(obsPreFix+'Auto.'+curIndex).setAttribute('style','padding-bottom: 20px;');
	obsAutocomp = initCodesAutocomplete(obsPreFix+'Code.'+curIndex, obsPreFix+'DropDown.'+curIndex, codeType);
	obsAutocomp.itemSelectEvent.subscribe(function (sType, aArgs) {
		setNodeText(document.getElementById(obsPreFix+'CodeDesc.'+curIndex), aArgs[2].code_desc, 5);
		document.getElementById(obsPreFix+'MasterCodeDesc.'+curIndex).value = aArgs[2].code_desc;
	});
}


var infoDialogs;

function initInfoDialogs() {
    infoDialogs = new YAHOO.widget.Dialog("infoDialogs", {
        context: ['', 'tr', 'bl', ["beforeShow", "windowResize"]],
        visible: false,
        modal: true,
        iframe: true,
        width: "300px"
    });
    YAHOO.util.Event.addListener('cancelInfoImg', "click", infoDialogs.hide, infoDialogs, true);
}

function showCodeDescription(obsPreFix, index) {
    var imgEl = document.getElementById(obsPreFix+'HelpImg.' + index);
    var codeDesc = document.getElementById(obsPreFix+'MasterCodeDesc.' + index).value;
    var info = codeDesc == null || codeDesc == 'undefined' ? '' : codeDesc;
    showInfoDialogs(imgEl, info);
}

function showInfoDialogs(contextEl, text) {
    infoDialogs.cfg.setProperty("context", [contextEl, "tl", "tl", ["beforeShow", "windowResize"]], false);
    infoDialogs.cfg.setProperty("zIndex", "999");
    infoDialogs.render();
    document.getElementById('infoDialogs').visibility = 'visible';
    document.getElementById('infoDialogs').style.display = 'block';

    document.getElementById('infoDialogsText').textContent = text;
    document.getElementById('infoDialogs').style.zIndex = "999";
    infoDialogs.show();
}

function checkFinalizeBills(obj) {
	var chkBox = document.getElementById('finalizeAll');
	if(obj.checked == true){
		chkBox.checked = true;
	}
}

function validateBabyCustomFields(){
	var valid = true;
	if (isCustomFieldsExist == false && isChildPatient == true){
		alert("Patient data is not completely recorded. Please edit patient's Custom Fields to record information before finalizing.");
		valid = false;
	}
	return valid;
}
var primaryInsurancePhotoDialog;

function initPrimaryInsurancePhotoDialog() {
	primaryInsurancePhotoDialog = new YAHOO.widget.Dialog('primaryInsurancePhotoDialog', {
		context:["_p_plan_card","tr","br", ["beforeShow", "windowResize"]],
		width: "550px",
		height: "250px",
		visible: false,
		modal: true,
		constraintoviewport: true,
		close: false,
	});

	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: handlePrimaryInsurancePhotoDialogCancel,
		scope: primaryInsurancePhotoDialog,
		correctScope: true
	});
	primaryInsurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	primaryInsurancePhotoDialog.render();
}

function showPrimaryInsurancePhotoDialog() {
	document.getElementById('primaryInsurancePhotoDialog').style.display = 'block';
	document.getElementById('primaryInsurancePhotoDialog').style.visibility = 'visible';
	primaryInsurancePhotoDialog.show();
}

function handlePrimaryInsurancePhotoDialogCancel() {
	document.getElementById('primaryInsurancePhotoDialog').style.display = 'none';
	document.getElementById('primaryInsurancePhotoDialog').style.visibility = 'hidden';
	primaryInsurancePhotoDialog.cancel();
}



