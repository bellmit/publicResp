var d_antenatalDocAutoComp = null;
var ed_antenatalDocAutoComp = null;

function initAntenatalDetails() {
  initAntenatalDetailsDialog();
  initEditAntenatalDetailsDialog();
  d_antenatalDocAutoComp = initAntenatalDoctorAC('d_antenatal_doctor', 'd_antenatal_doctor_container', false, '');
}

function antenatalDetailsEntered() {
	return document.getElementById('antenatalDetailsTable').rows.length > 2;
}


function checkAntenatal() {
	if(!antenatalDetailsEntered()) {
		showMessage("js.outpatient.consultation.mgmt.enteratleastoneantenatalvalue");
		return false;
	} else {
	var els = document.getElementsByName('antenatal_visit_date');
	var antenatal_delete = document.getElementsByName('antenatal_deleted');
	var antenatalCount = 0;

	for (var i=0; i<els.length; i++) {
		var antenatal_visit_date = els[i].value;
		if (antenatal_visit_date !='' && antenatal_delete[i].value == 'false') {
			antenatalCount++;
		}
	}
	
	if (antenatalCount == 0) {
		showMessage("js.outpatient.consultation.mgmt.enteratleastoneantenatalvalue");
		return false;
	} 
 }
	return true;
}

function addtoAntenatalSaverecords(action) {
	
	var prefix = null;
	if (action == 'add'){
		prefix = 'd';
	} else if (action == 'edit'){
		prefix = 'ed';
	}
	
	var gestationage = document.getElementById(prefix+'_antenatal_gestation_age').value;
	var heightoffundus = document.getElementById(prefix+'_antenatal_height_fundus').value;
   	var presentation = document.getElementById(prefix+'_antenatal_presentation').value;
   	var relationpptobrim = document.getElementById(prefix+'_antenatal_relation_pp_brim').value;
   	var foetalheart = document.getElementById(prefix+'_antenatal_foetal_heart').value;
   	var urine = document.getElementById(prefix+'_antenatal_urine').value;
   	var sbp = document.getElementById(prefix+'_antenatal_systolic_bp').value;
   	var dbp = document.getElementById(prefix+'_antenatal_diastolic_bp').value;
   	var weight = document.getElementById(prefix+'_antenatal_weight').value;
   	var prescriptionsummary = document.getElementById(prefix+'_antenatal_prescription_summary').value;
   	var consultingdoctor = document.getElementById(prefix+'_antenatal_doctor').value;
   	//var consultingdoctorid = document.getElementById(prefix+'_antenatal_doctor_id').value;
   	var nextvisitdate = document.getElementById(prefix+'_next_visit_date').value;
	
	if ((gestationage =='' && heightoffundus =='' &&
			presentation =='' && relationpptobrim =='' &&
			foetalheart =='' && urine =='' &&
			sbp =='' && dbp =='' && weight =='' && prescriptionsummary =='' && nextvisitdate =='') ) {
   		  alert("Atleast add one field to grid");
   		   return false;
   	    }
	
	return true;
}

function initAntenatalDoctorAC(inputElId, containerId, edit, doctorId) {
	var doctorList = [];
	for (var i=0; i<antenatal_doctors_json.length; i++) {
		if (antenatal_doctors_json[i].status == 'A' || doctorId == antenatal_doctors_json[i].doctor_id == '') {
			doctorList.push(antenatal_doctors_json[i]);
		}
	}
	if (edit && !empty(ed_antenatalDocAutoComp)) {
		ed_antenatalDocAutoComp.destroy();
		ed_antenatalDocAutoComp = null;
	} else if (!empty(d_antenatalDocAutoComp)) {
		d_antenatalDocAutoComp.destroy();
		d_antenatalDocAutoComp = null;
	}

	var ds = new YAHOO.util.LocalDataSource({result: doctorList});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [	{key : "doctor_name"},
					{key : "doctor_id"}
			 	]
	};

	var autoComp = new YAHOO.widget.AutoComplete(inputElId, containerId, ds);
	autoComp.minQueryLength = 1;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 100;
	autoComp.resultTypeList = false; // making the result available as object literal.
	autoComp.forceSelection = true;
	autoComp.autoSnapContainer = false;
	autoComp.queryMatchContains = true;
	autoComp.formatResult = Insta.autoHighlight;
	if (document.getElementById(inputElId).value != '') {
		autoComp._bItemSelected = true;
		autoComp._sInitInputValue = document.getElementById(inputElId).value;
	}

	autoComp.itemSelectEvent.subscribe(setAntenatalDoctor);
	autoComp.selectionEnforceEvent.subscribe(clearAntenatalDoctor);

	return autoComp;
}

function setAntenatalDoctor(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_antenatal_doctor_id").value = sArgs[2].doctor_id;
}

function clearAntenatalDoctor(oSelf, sClearedValue) {
	var elId = sClearedValue[0]._elTextbox.id;
	var prefix = elId.split("_")[0];
	document.getElementById(prefix+"_antenatal_doctor_id").value = '';
}

function validateAntenatalDateField(nextvisitdate,dateFieldTitle, valid, validEmpty) {
	var form = document.forms[antenatal_details_form];
	var nextvisitDate = document.getElementById(nextvisitdate);
	if (nextvisitDate != "" ){
		var msg = validateDate(document.getElementById(nextvisitdate).value,"future");
		if (msg == null){
		}else{
			alert(msg);
			return false;
		}
	return true;
}
}

var addAntenatalDialog = null;
function initAntenatalDetailsDialog() {
	var dialogAntenatalDiv=document.getElementById("addAntenatalDialog");
	if(dialogAntenatalDiv == undefined) return;
	dialogAntenatalDiv.style.display = 'block';
	addAntenatalDialog = new YAHOO.widget.Dialog("addAntenatalDialog",
										{	width:"750px",
											context : ["addAntenatalDialog", "tr", "br"],
											visible:false,
											modal:true,
											constraintoviewport:true
										});

	YAHOO.util.Event.addListener('antenatalDetails_add_btn', 'click', addAntenatalDetailsTable, addAntenatalDialog, true);
	YAHOO.util.Event.addListener('antenatalDetails_cancel_btn', 'click', handleAddAntenatalCancel, addAntenatalDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddAntenatalCancel,
	                                                scope:addAntenatalDialog,
	                                                correctScope:true } );
	addAntenatalDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addAntenatalDialog.render();
}

function handleAddAntenatalCancel() {
		parentAntenatalDialog = null;
		this.cancel();
}

var parentAntenatalDialog = null;
function showAddAntenatalDialog(obj) {
	var row = getAntenatalDetailsThisRow(obj);
	clearAntenatalDetailsFields();
	addAntenatalDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	if (visit_type == 'o') {
		var consultingDoctorId = document.getElementById('an_consulting_doctor_id').value;
		consultingDoctorId = empty(consultingDoctorId) ? antenatal_user_doctor_id : consultingDoctorId;
		var record = findInList(antenatal_doctors_json, "doctor_id", consultingDoctorId);

		var consultingDoctorName = empty(consultingDoctorId) ? '' : record.doctor_name;
		document.getElementById('d_antenatal_doctor').disabled = true;
		document.getElementById('d_antenatal_doctor').value = consultingDoctorName;
		document.getElementById('d_antenatal_doctor_id').value = consultingDoctorId;
		d_antenatalDocAutoComp = initAntenatalDoctorAC('d_antenatal_doctor', 'd_antenatal_doctor_container', false, consultingDoctorId);
		if (document.getElementById('d_antenatal_doctor').value != '') {
			d_antenatalDocAutoComp._bItemSelected = true;
			d_antenatalDocAutoComp._sInitInputValue = document.getElementById('d_antenatal_doctor').value;
		}

	} else if (!empty(antenatal_user_doctor_id)) {
		// populate the user doctor in ip visit summary/outside patient registration screens.
		var record = findInList(antenatal_doctors_json, "doctor_id", antenatal_user_doctor_id);
		var userDoctorName = record.doctor_name
		document.getElementById('d_antenatal_doctor').value = userDoctorName;
		document.getElementById('d_antenatal_doctor_id').value = antenatal_user_doctor_id;
		d_antenatalDocAutoComp = initAntenatalDoctorAC('d_antenatal_doctor', 'd_antenatal_doctor_container', false, antenatal_user_doctor_id);
		if (document.getElementById('d_antenatal_doctor').value != '') {
			d_antenatalDocAutoComp._bItemSelected = true;
			d_antenatalDocAutoComp._sInitInputValue = document.getElementById('d_antenatal_doctor').value;
		}
	}
	addAntenatalDialog.show();
	document.getElementById('d_antenatal_visit_date').value=formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_antenatal_visit_date').focus();
	parentAntenatalDialog = addAntenatalDialog;
	return false;
}

var antenatalDetailsColIndex  = 0;
var antenatalDetailsAdded = 0;
var ANTENATAL_VISIT_DATETIME = antenatalDetailsColIndex++, ANTENATAL_GESTATION_AGE = antenatalDetailsColIndex++, ANTENATAL_HEIGHT_FUNDUS = antenatalDetailsColIndex++, ANTENATAL_PRESENTATION =  antenatalDetailsColIndex++,
	ANTENATAL_RELATION_PP_BRIM = antenatalDetailsColIndex++, ANTENATAL_FOETAL_HEART = antenatalDetailsColIndex++, ANTENATAL_URINE = antenatalDetailsColIndex++, ANTENATAL_BP = antenatalDetailsColIndex++,
	ANTENATAL_WEIGHT = antenatalDetailsColIndex++,ANTENATAL_PRESCRIPTION_SUMMARY = antenatalDetailsColIndex++,ANTENATAL_CONSULTING_DOCTOR= antenatalDetailsColIndex++,ANTENATAL_NEXT_VISIT_DATE = antenatalDetailsColIndex++,
	ANTENATAL_TRASH_COL = antenatalDetailsColIndex++, ANTENATAL_EDIT_COL = antenatalDetailsColIndex++;

function addAntenatalDetailsTable() {
	var visitdate = document.getElementById('d_antenatal_visit_date').value;
	if (visitdate == '') {
   		alert('Enter the visit date');
   		document.getElementById('d_antenatal_visit_date').focus();
   		return false;
   	}
	if(!(checkAntenatalvalidation('add'))) return false;
	if(!(addtoAntenatalSaverecords('add'))) return false;
   	var id = getAntenatalDetailsNumCharges('antenatalDetailsTable');
   	var table = document.getElementById("antenatalDetailsTable");
	var templateRow = table.rows[getAntenatalDetailsTemplateRow('antenatalDetailsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

		  	
   	var cell = null;
   	var bp=null;
    var antenatal_id = document.getElementById('d_antenatal_id').value;
   	var gestationage = document.getElementById('d_antenatal_gestation_age').value;
	var heightoffundus = document.getElementById('d_antenatal_height_fundus').value;
   	var presentation = document.getElementById('d_antenatal_presentation').value;
   	var relationpptobrim = document.getElementById('d_antenatal_relation_pp_brim').value;
   	var foetalheart = document.getElementById('d_antenatal_foetal_heart').value;
   	var urine = document.getElementById('d_antenatal_urine').value;
   	var sbp = document.getElementById('d_antenatal_systolic_bp').value;
   	var dbp = document.getElementById('d_antenatal_diastolic_bp').value;
   	if (sbp!='' && dbp=='') {
   		bp=document.getElementById('d_antenatal_systolic_bp').value;
   	} else if (dbp!='' && sbp=='') {
   		bp=document.getElementById('d_antenatal_diastolic_bp').value;
   	} else if (dbp!='' && sbp!=''){
   		bp=sbp+"/"+dbp;
   	}
   	
   	//var bp = sbp+"/"+dbp;
   	var weight = document.getElementById('d_antenatal_weight').value;
   	var prescriptionsummary = document.getElementById('d_antenatal_prescription_summary').value;
   	var consultingdoctor = document.getElementById('d_antenatal_doctor').value;
   	var consultingdoctorid = document.getElementById('d_antenatal_doctor_id').value;
   	var nextvisitdate = document.getElementById('d_next_visit_date').value;
   	
   	if (gestationage != null && gestationage != '') {
	   	if(gestationage.indexOf('Weeks') == -1) {
	   		gestationage = gestationage+" Weeks";
	   	}
   	}
	setNodeText(row.cells[ANTENATAL_VISIT_DATETIME], visitdate);
	setNodeText(row.cells[ANTENATAL_GESTATION_AGE], gestationage);
	setNodeText(row.cells[ANTENATAL_HEIGHT_FUNDUS], heightoffundus);
	setNodeText(row.cells[ANTENATAL_PRESENTATION], presentation,20);
	setNodeText(row.cells[ANTENATAL_RELATION_PP_BRIM], relationpptobrim,20);
	setNodeText(row.cells[ANTENATAL_FOETAL_HEART], foetalheart,20);
	setNodeText(row.cells[ANTENATAL_URINE], urine,20);
	setNodeText(row.cells[ANTENATAL_BP], bp);
	setNodeText(row.cells[ANTENATAL_WEIGHT], weight);
	setNodeText(row.cells[ANTENATAL_PRESCRIPTION_SUMMARY], prescriptionsummary,20);
	setNodeText(row.cells[ANTENATAL_CONSULTING_DOCTOR], consultingdoctor);
	setNodeText(row.cells[ANTENATAL_NEXT_VISIT_DATE], nextvisitdate);

	setAntenatalDetailsHiddenValue(id, "antenatal_id", '_');
	setAntenatalDetailsHiddenValue(id, "antenatal_visit_date", visitdate);
   	setAntenatalDetailsHiddenValue(id, "antenatal_gestation_age", gestationage);
   	setAntenatalDetailsHiddenValue(id, "antenatal_height_fundus", heightoffundus);
   	setAntenatalDetailsHiddenValue(id, "antenatal_presentation", presentation);
	setAntenatalDetailsHiddenValue(id, "antenatal_rel_pp_brim", relationpptobrim);
	setAntenatalDetailsHiddenValue(id, "antenatal_foetal_heart", foetalheart);
	setAntenatalDetailsHiddenValue(id, "antenatal_urine", urine);
	setAntenatalDetailsHiddenValue(id, "antenatal_systolic_bp", sbp);
	setAntenatalDetailsHiddenValue(id, "antenatal_diastolic_bp", dbp);
   	setAntenatalDetailsHiddenValue(id, "antenatal_weight", weight);
   	setAntenatalDetailsHiddenValue(id, "antenatal_prescription_summary", prescriptionsummary);
	setAntenatalDetailsHiddenValue(id, "antenatal_doctor_id", consultingdoctorid);
	setAntenatalDetailsHiddenValue(id, "antenatal_doctor_name", consultingdoctor);
   	setAntenatalDetailsHiddenValue(id, "antenatal_next_visit_date", nextvisitdate);


  	antenatalDetailsAdded++;
	clearAntenatalDetailsFields();
	setAntenatalDetailsRowStyle(id);
	addAntenatalDialog.align("tr", "tl");
	document.getElementById('d_antenatal_visit_date').focus();
	return id;
}

function setAntenatalDetailsHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.forms[antenatal_details_form], name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


function clearAntenatalDetailsFields() {
	document.getElementById('d_antenatal_visit_date').value = '';
	if (visit_type != 'o') {
		document.getElementById('d_antenatal_doctor').value = '';
		document.getElementById('d_antenatal_doctor_id').value = '';
		d_antenatalDocAutoComp = initAntenatalDoctorAC('d_antenatal_doctor', 'd_antenatal_doctor_container', false, antenatal_user_doctor_id);
		if (!empty(antenatal_user_doctor_id)) {
			var record = findInList(antenatal_doctors_json, "doctor_id", antenatal_user_doctor_id);
			var userDoctorName = record.doctor_name
			document.getElementById('d_antenatal_doctor').value = userDoctorName;
			document.getElementById('d_antenatal_doctor_id').value = antenatal_user_doctor_id;
			if (document.getElementById('d_antenatal_doctor').value != '') {
				d_antenatalDocAutoComp._bItemSelected = true;
				d_antenatalDocAutoComp._sInitInputValue = document.getElementById('d_antenatal_doctor').value;
			}
		}
	}
	
	document.getElementById('d_antenatal_visit_date').value = formatDate(new Date(), 'ddmmyyyy', '-');
	document.getElementById('d_antenatal_gestation_age').value = '';
	document.getElementById('d_antenatal_height_fundus').value = '';
	document.getElementById('d_antenatal_presentation').value = '';
	document.getElementById('d_antenatal_relation_pp_brim').value = '';
	document.getElementById('d_antenatal_foetal_heart').value = '';
	document.getElementById('d_antenatal_urine').value = '';
	document.getElementById('d_antenatal_systolic_bp').value = '';
	document.getElementById('d_antenatal_diastolic_bp').value = '';
	document.getElementById('d_antenatal_weight').value = '';
	document.getElementById('d_antenatal_prescription_summary').value = '';
	document.getElementById('d_next_visit_date').value = '';
}

function setAntenatalDetailsRowStyle(i) {
	var row = getAntenatalDetailsChargeRow(i, 'antenatalDetailsTable');
	var antenatalId = getAntenatalDetailsIndexedValue("antenatal_id", i);

	var trashImgs = row.cells[ANTENATAL_TRASH_COL].getElementsByTagName("img");
	var flagImgs = row.cells[ANTENATAL_VISIT_DATETIME].getElementsByTagName("img");
	var added = (antenatalId.substring(0,1) == "_");
	var cancelled = getAntenatalDetailsIndexedValue("antenatal_deleted", i) == 'true';
	var edited = getAntenatalDetailsIndexedValue("antenatal_edited", i) == 'true';

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
		cls = 'added';
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

function cancelAntenatalDetails(obj) {

	var row = getAntenatalDetailsThisRow(obj);
	var id = getAntenatalDetailsRowChargeIndex(row);
	var oldDeleted =  getAntenatalDetailsIndexedValue("antenatal_deleted", id);

	var isNew = getAntenatalDetailsIndexedValue("antenatal_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		antenatalDetailsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setAntenatalDetailsIndexedValue("antenatal_deleted", id, newDeleted);
		setAntenatalDetailsIndexedValue("antenatal_edited", id, "true");
		setAntenatalDetailsRowStyle(id);
	}
	return false;
}

var weightfield = null;
var heightfield = null;
var ageField = null;
var sbpField = null;
var dbpField = null;

function checkAntenatalvalidation(action) {
	var prefix = null;
	if (action == 'add'){
		prefix = 'd';
	} else if (action == 'edit'){
		prefix = 'ed';
	}
	
	weightfield = document.getElementById(prefix+"_antenatal_weight").value;
	heightfield = document.getElementById(prefix+"_antenatal_height_fundus").value;
	ageField = document.getElementById(prefix+"_antenatal_gestation_age").value;
	sbpField = document.getElementById(prefix+"_antenatal_systolic_bp").value;
	dbpField = document.getElementById(prefix+"_antenatal_diastolic_bp").value;
	
	if (ageField != '') {
		if(ageField > 52) {
			alert("Gestation Age should be between 0 to 52");
			return false;
		}
	} if (sbpField != '') {
		if (!(isInteger(sbpField))) {
		alert("Systolic Bp Field should be numeric field");
		return false;
		} else if((sbpField < 10 || sbpField > 300)) {
			alert("Systolic Bp should be between 10 and 300");
			return false;
		}
	} if (dbpField != '') {
		if (!(isInteger(dbpField))) {
			alert("Diastolic Bp Field should be numeric field");
			return false;
			} else if((dbpField < 10 || dbpField > 300)) {
				alert("Diastolic Bp should be between 10 and 300");
				return false;
			}
	} if(weightfield != '') {
		if(!isDecimal(weightfield,2)) {
			alert("Invalid Weight Value.Enter proper weight value");
			return false;
		}
		if(!checkMaximumHeightAndWeight(weightfield,heightfield)) return false;
	} if(heightfield != '') {
		if(!isDecimal(heightfield,2)) {
			alert("Invalid Height Value.Enter proper height value");
			return false;
		}
		if(!checkMaximumHeightAndWeight(weightfield,heightfield)) return false;
	}
		return true;
}

	
function checkMaximumHeightAndWeight(weightfield,heightfield) {
	var weightVal = parseFloat(weightfield);
	var heightVal = parseFloat(heightfield);
	if (weightVal > 200) {
		alert("Weight Should be between 0 to 200");
			return false;
	} if (heightVal > 100) {
		alert("Height of Fundus Should be between 0 to  100");
			return false;
	} 
	 	return true;
}

var editAntenatalDialog=null;
function initEditAntenatalDetailsDialog() {
	var dialogAntenatalDiv = document.getElementById("editAntenatalDialog");
	dialogAntenatalDiv.style.display = 'block';
	editAntenatalDialog = new YAHOO.widget.Dialog("editAntenatalDialog",{
			width:"750px",
			text: "Edit Antenatal Details",
			context :["antenatalDetailsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditAntenatalCancel,
	                                                scope:editAntenatalDialog,
	                                                correctScope:true } );
	editAntenatalDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editAntenatalDialog.cancelEvent.subscribe(handleEditAntenatalCancel);
	YAHOO.util.Event.addListener('edit_AntenatalDetails_Ok', 'click', editAntenatalTableRow, editAntenatalDialog, true);
	YAHOO.util.Event.addListener('edit_AntenatalDetails_Cancel', 'click', handleEditAntenatalCancel, editAntenatalDialog, true);
	YAHOO.util.Event.addListener('edit_AntenatalDetails_Previous', 'click', openAntenatalDetailsPrevious, editAntenatalDialog, true);
	YAHOO.util.Event.addListener('edit_AntenatalDetails_Next', 'click', openAntenatalDetailsNext, editAntenatalDialog, true);
	editAntenatalDialog.render();
}

function editAntenatalTableRow() {
	var visitdate = document.getElementById('ed_antenatal_visit_date').value;
		if (visitdate == '') {
	   		alert('Enter the visit date');
	   		document.getElementById('ed_antenatal_visit_date').focus();
	   		return false;
	   	}
	if(!(checkAntenatalvalidation('edit'))) return false;
	if(!(addtoAntenatalSaverecords('edit'))) return false;
	var id = document.forms[antenatal_details_form].editAntenatalRowId.value;
	var row = getAntenatalDetailsChargeRow(id, 'antenatalDetailsTable');

		
	var antenatal_id = document.getElementById('ed_antenatal_id').value;
   	var gestationage = document.getElementById('ed_antenatal_gestation_age').value;
	var heightoffundus = document.getElementById('ed_antenatal_height_fundus').value;
   	var presentation = document.getElementById('ed_antenatal_presentation').value;
   	var relationpptobrim = document.getElementById('ed_antenatal_relation_pp_brim').value;
   	var foetalheart = document.getElementById('ed_antenatal_foetal_heart').value;
   	var urine = document.getElementById('ed_antenatal_urine').value;
   	var sbp = document.getElementById('ed_antenatal_systolic_bp').value;
 	var dbp = document.getElementById('ed_antenatal_diastolic_bp').value;
 	var bp=null;
 	if (sbp!='' && dbp=='') {
   		bp=sbp;
   	} else if (dbp!='' && sbp=='') {
   		bp=dbp;
   	} else if (dbp!='' && sbp!=''){
   		bp=sbp+"/"+dbp;
   	}
   	var weight = document.getElementById('ed_antenatal_weight').value;
   	var prescriptionsummary = document.getElementById('ed_antenatal_prescription_summary').value;
   	var consultingdoctor = document.getElementById('ed_antenatal_doctor').value;
   	var consultingdoctorid = document.getElementById('ed_antenatal_doctor_id').value;
   	var nextvisitdate = document.getElementById('ed_next_visit_date').value;

   	if (gestationage != null && gestationage != '') {
	   	if(gestationage.indexOf('Weeks') == -1) {
	   		gestationage = gestationage+" Weeks";
	   	}
   	}
	setNodeText(row.cells[ANTENATAL_VISIT_DATETIME], visitdate);
	setNodeText(row.cells[ANTENATAL_GESTATION_AGE], gestationage);
	setNodeText(row.cells[ANTENATAL_HEIGHT_FUNDUS], heightoffundus);
	setNodeText(row.cells[ANTENATAL_PRESENTATION], presentation,20);
	setNodeText(row.cells[ANTENATAL_RELATION_PP_BRIM], relationpptobrim,20);
	setNodeText(row.cells[ANTENATAL_FOETAL_HEART], foetalheart,20);
	setNodeText(row.cells[ANTENATAL_URINE], urine,20);
	setNodeText(row.cells[ANTENATAL_BP], bp);
	setNodeText(row.cells[ANTENATAL_WEIGHT], weight);
	setNodeText(row.cells[ANTENATAL_PRESCRIPTION_SUMMARY], prescriptionsummary,20);
	setNodeText(row.cells[ANTENATAL_CONSULTING_DOCTOR], consultingdoctor);
	setNodeText(row.cells[ANTENATAL_NEXT_VISIT_DATE], nextvisitdate);

	setAntenatalDetailsHiddenValue(id, "antenatal_id", antenatal_id);
	setAntenatalDetailsHiddenValue(id, "antenatal_visit_date", visitdate);
   	setAntenatalDetailsHiddenValue(id, "antenatal_gestation_age", gestationage);
   	setAntenatalDetailsHiddenValue(id, "antenatal_height_fundus", heightoffundus);
   	setAntenatalDetailsHiddenValue(id, "antenatal_presentation", presentation,20);
	setAntenatalDetailsHiddenValue(id, "antenatal_rel_pp_brim", relationpptobrim,20);
	setAntenatalDetailsHiddenValue(id, "antenatal_foetal_heart", foetalheart,20);
	setAntenatalDetailsHiddenValue(id, "antenatal_urine", urine,20);
	setAntenatalDetailsHiddenValue(id, "antenatal_systolic_bp", sbp);
	setAntenatalDetailsHiddenValue(id, "antenatal_diastolic_bp", dbp);
   	setAntenatalDetailsHiddenValue(id, "antenatal_weight", weight);
   	setAntenatalDetailsHiddenValue(id, "antenatal_prescription_summary", prescriptionsummary,20);
	setAntenatalDetailsHiddenValue(id, "antenatal_doctor_id", consultingdoctorid);
	setAntenatalDetailsHiddenValue(id, "antenatal_doctor_name", consultingdoctor);
   	setAntenatalDetailsHiddenValue(id, "antenatal_next_visit_date", nextvisitdate);

	YAHOO.util.Dom.removeClass(row, 'editing');

	setAntenatalDetailsIndexedValue("antenatal_edited", id, 'true');
	setAntenatalDetailsRowStyle(id);

	editAntenatalDialog.cancel();
	return true;
}


var fieldAntenatalEdited = false;
function setAntenatalDetailsEdited() {
	fieldAntenatalEdited = true;
}

function handleEditAntenatalCancel() {
		var id = document.forms[antenatal_details_form].editAntenatalRowId.value;
		var row = getAntenatalDetailsChargeRow(id, "antenatalDetailsTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldAntenatalEdited = false;
		this.hide();
}

function showEditAntenatalDialog(obj) {
	parentAntenatalDialog = editAntenatalDialog;
	var row = getAntenatalDetailsThisRow(obj);
	var id = getAntenatalDetailsRowChargeIndex(row);
	editAntenatalDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editAntenatalDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.forms[antenatal_details_form].editAntenatalRowId.value = id;

	document.getElementById('ed_antenatal_id').value = getAntenatalDetailsIndexedValue("antenatal_id", id);
	document.getElementById('d_antenatal_visit_date').value = getAntenatalDetailsIndexedValue("antenatal_visit_date", id);
	var cons_doctor_id = getAntenatalDetailsIndexedValue('antenatal_doctor_id', id);
	document.getElementById('ed_antenatal_doctor').value = getAntenatalDetailsIndexedValue('antenatal_doctor_name', id);
	document.getElementById('ed_antenatal_doctor_id').value = cons_doctor_id;
	ed_antenatalDocAutoComp = initAntenatalDoctorAC('ed_antenatal_doctor', 'ed_antenatal_doctor_container', true, cons_doctor_id);
	if (document.getElementById('ed_antenatal_doctor').value != '') {
		ed_antenatalDocAutoComp._bItemSelected = true;
		ed_antenatalDocAutoComp._sInitInputValue = document.getElementById('ed_antenatal_doctor').value;
	}
	if (visit_type == 'o') {
		document.getElementById('ed_antenatal_doctor').disabled = true;
	}
	var antenatal_visit_datetime = getAntenatalDetailsIndexedValue("antenatal_visit_date", id);
	document.getElementById('ed_antenatal_visit_date').value = empty(antenatal_visit_datetime) ? '' : antenatal_visit_datetime.split(' ')[0];
	document.getElementById('ed_antenatal_id').value = getAntenatalDetailsIndexedValue("antenatal_id", id);
	document.getElementById('ed_antenatal_gestation_age').value = getAntenatalDetailsIndexedValue("antenatal_gestation_age", id);
	document.getElementById('ed_antenatal_height_fundus').value = getAntenatalDetailsIndexedValue("antenatal_height_fundus", id);
	document.getElementById('ed_antenatal_presentation').value = getAntenatalDetailsIndexedValue("antenatal_presentation", id);
	document.getElementById('ed_antenatal_relation_pp_brim').value = getAntenatalDetailsIndexedValue("antenatal_rel_pp_brim", id);
	document.getElementById('ed_antenatal_foetal_heart').value = getAntenatalDetailsIndexedValue("antenatal_foetal_heart", id);
	document.getElementById('ed_antenatal_urine').value = getAntenatalDetailsIndexedValue("antenatal_urine", id);
	document.getElementById('ed_antenatal_systolic_bp').value = getAntenatalDetailsIndexedValue("antenatal_systolic_bp", id);
	document.getElementById('ed_antenatal_diastolic_bp').value = getAntenatalDetailsIndexedValue("antenatal_diastolic_bp", id);
	document.getElementById('ed_antenatal_weight').value = getAntenatalDetailsIndexedValue("antenatal_weight", id);
	document.getElementById('ed_antenatal_prescription_summary').value = getAntenatalDetailsIndexedValue("antenatal_prescription_summary", id);
	document.getElementById('ed_next_visit_date').value = getAntenatalDetailsIndexedValue("antenatal_next_visit_date", id);

	document.getElementById('ed_antenatal_visit_date').focus();
	return false;
}

function openAntenatalDetailsPrevious() {
	var id = document.forms[antenatal_details_form].editAntenatalRowId.value;
	id = parseInt(id);
	var row = getAntenatalDetailsChargeRow(id, 'antenatalDetailsTable');

	if (fieldAntenatalEdited) {
		if (!editAntenatalTableRow()) return false;
		fieldAntenatalEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditAntenatalDialog(document.getElementsByName('antenatalEditAnchor')[parseInt(id)-1]);
	}
}

function openAntenatalDetailsNext() {
	var id = document.forms[antenatal_details_form].editAntenatalRowId.value;
	id = parseInt(id);
	var row = getAntenatalDetailsChargeRow(id, 'antenatalDetailsTable');

	if (fieldAntenatalEdited) {
		if (!editAntenatalTableRow()) return false;
		fieldAntenatalEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('antenatalDetailsTable').rows.length-2) {
		showEditAntenatalDialog(document.getElementsByName('antenatalEditAnchor')[parseInt(id)+1]);
	}
}

function getAntenatalDetailsNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstAntenatalDetailsRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getAntenatalDetailsNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getAntenatalDetailsMainRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getAntenatalDetailsTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getAntenatalDetailsNumCharges(tableId) + 1;
}

function getAntenatalDetailsChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstAntenatalDetailsRow()];
}

function getAntenatalDetailsRowChargeIndex(row) {
	return row.rowIndex - getFirstAntenatalDetailsRow();
}

function getAntenatalDetailsThisRow(node) {
	return findAncestor(node, "TR");
}

function getAntenatalDetailsIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.forms[antenatal_details_form], name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function setAntenatalDetailsIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.forms[antenatal_details_form], name, index);
	if (obj)
		obj.value = value;
	return obj;
}