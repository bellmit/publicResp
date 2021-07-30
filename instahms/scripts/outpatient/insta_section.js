/*
 * Functions used by PhysicianForm.jsp
 */

var dialogs = {};
YAHOO.util.Event.onContentReady("content", initInstaSectionDialogs);
function initInstaSectionDialogs() {
	initNotesDialog();
	initPhrasesDialog();
	isPatientHistoryMandatory();
	var sections = document.getElementsByName('insta_sections');
	var sections_l = sections.length;
	for (var i = 0; i < sections_l; i++) {
		var fields = document.getElementsByName('field_ids_' + sections[i].value);
		var fields_l = fields.length;
		var d_sections = document.getElementsByName('section_detail_ids_' + sections[i].value);
		var d_sections_l = d_sections.length;
		for (var j = 0; j < fields_l; j++) {
			if (document.getElementsByName('field_type_' + fields[j].value)[0].value === 'checkbox') {
				for (var k = 0; k < d_sections_l; k++) {
					var dialog = new YAHOO.widget.Dialog("editDialog_" + d_sections[k].value + '_' + fields[j].value, {
						width:"450px",
						context :["field" + d_sections[k].value + '_' + fields[j].value, "bl", "tl"],
						visible:false,
						modal:true,
						constraintoviewport:true
					});

					var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
							{ fn:sectionFieldDialogCancel, scope:dialog, correctScope:true } );
					dialog.cfg.queueProperty("keylisteners", escKeyListener);
					dialog.render();
					dialogs[d_sections[k].value + '_' + fields[j].value] = dialog;
				}
			}
		}
	}
}

function sectionFieldDialogCancel() {
	if (phraseChildDialog == null) {
		this.cancel();
	}
}

var phraseDialog = null;
var phraseChildDialog = null;
function initPhrasesDialog() {
	if (!document.getElementById('phraseDialog')) return;
	document.getElementById('phraseDialog').style.display = 'block';
	phraseDialog = new YAHOO.widget.Dialog("phraseDialog", {
				width:"300px",
				context :["notesDialog", "bl", "tl"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:cancelPhraseDialog, scope:phraseDialog, correctScope:true } );
	phraseDialog.cfg.queueProperty("keylisteners", escKeyListener);
	phraseDialog.render();
}

var last_added_phrases = [];
function cancelPhraseDialog() {
	phraseChildDialog = null;
	phraseDialog.cancel();
}

function appendPhrase(label, formName, sectionId, fieldId, optionId) {
	var phrase = label.textContent;
	if (!empty(optionId)) {
		var text = document.forms[formName][sectionId +"_option_remarks_" + fieldId + "_"+optionId];
		if (!text.disabled) {
			text.value += (text.value != '' ? ", " : '') + phrase;
			last_added_phrases[sectionId +"_option_remarks_" + fieldId + "_"+optionId] = phrase;
		}
	} else {
		var text = document.forms[formName][sectionId +"_option_remarks_" + fieldId];
		text.value += (text.value != '' ? ", " : '') + phrase;
		last_added_phrases[sectionId +"_option_remarks_" + fieldId] = phrase;
	}
	return false;
}

function showPysicianFormEditDialog(sectionId, fieldId) {
	var dialog = dialogs[sectionId + "_" +fieldId];
	var dialogDiv = document.getElementById("editDialog_" + sectionId + "_" + fieldId);
	dialogDiv.style.display = 'block';
	dialog.show();
}

function onEditSubmit(fieldId, formName, sectionId, mrNo) {
	updateFieldText(fieldId, formName, sectionId);
	var linkedTo = "";
	for (var j=0; j<insta_sections_json.length; j++) {
		var masterSectionId = insta_sections_json[j].section_id;
		if (masterSectionId == sectionId) {
			linkedTo = insta_sections_json[j].linked_to;
		}
	}
	if (!empty(collapsiblePanels) && collapsiblePanels[mrNo] && linkedTo == 'patient') {
		collapsiblePanels[mrNo].open(); // on ok button of dialog we are reopening a same collapsiblePanel again
	}
	var dialog = dialogs[sectionId + "_" +fieldId];
	dialog.cancel();
}

function adjustTextArea(sectionId, mrNo) {
	var linkedTo = "";
	for (var j=0; j<insta_sections_json.length; j++) {
		var masterSectionId = insta_sections_json[j].section_id;
		if (masterSectionId == sectionId) {
			linkedTo = insta_sections_json[j].linked_to;
		}
	}

	if (!empty(collapsiblePanels) && !empty(collapsiblePanels[mrNo]) && linkedTo == 'patient') {
		collapsiblePanels[mrNo].open(); // on ok button of dialog we are reopening a same collapsiblePanel again
	}
}

function updateFieldText(fieldId, formName, sectionId) {
	var div = document.getElementById("editDialog_" + sectionId +"_"+ fieldId);
	var text = document.getElementById("fieldText_" + sectionId +"_"+ fieldId);
	var boxes = getElementsByName(div, sectionId+"_field_" + fieldId);
	if (boxes.length == 0) {
		text.innerHTML = '';
		// do nothing.
	} else {
		var isNormal = boxes[0].checked;
		var allowNormal = document.getElementById("allowNormal"+sectionId+"_"+fieldId).value;
		var formDetailsDiv = document.getElementById('sectionDetailsDiv_'+sectionId);
		if (allowNormal == 'Y' && isNormal) {
			text.innerHTML = "Normal - " +document.forms[formName][sectionId+"_option_remarks_"+fieldId + "_" + boxes[0].value].value
		} else {
			var fieldText = "";
			var start = 0;
			if (allowNormal == 'Y') start = 1; // if normal is there checkbox values should start from 1.
			for (var i=start; i< boxes.length; i++) {
				if (boxes[i].checked) {
					if (fieldText != "")
						fieldText += '<br/>';

					if (boxes[i].value == -1) {
						fieldText += document.forms[formName][sectionId+"_option_remarks_"+fieldId + "_" + boxes[i].value].value;
					} else {
						fieldText += jOptionsList[sectionId][boxes[i].value].option_value;
						var optionRemarks = document.forms[formName][sectionId+"_option_remarks_"+fieldId + "_" + boxes[i].value].value;
						if (optionRemarks != '') {
							fieldText += '-' + optionRemarks;
						}
					}
				}
			}
			if (fieldText == "")
				fieldText = "--";
			text.innerHTML = fieldText;
		}
	}
}

function reCreateDivText(sectionId, formName) {

	var fieldNames = document.getElementsByName("field_title_"+sectionId);
	var divText = "";
	for (var j=0; j<fieldNames.length; j++) {

		var optionsText = '';
		var fieldId = fieldNames[j].id.split("_")[3];
		var field_type = document.getElementsByName('field_type_'+ sectionId +"_"+fieldId)[0].value;
		if (field_type == 'text' || field_type == 'wide text') {
			optionsText += document.forms[formName][sectionId+"_option_remarks_"+fieldId].value;
		} else if (field_type == 'dropdown') {
			var dropdown = document.getElementsByName(sectionId+'_field_'+fieldId)[0];
			var value = dropdown.value;
			if (value == 0) {
				// it is normal no need to display.
			} else if (value == -1) {
				// others text
				optionsText += document.getElementsByName(sectionId+'_option_remarks_'+fieldId)[0].value;
			} else {
				optionsText += dropdown.options[dropdown.options.selectedIndex].text;
			}
		} else if (field_type == 'checkbox') {
			var div = document.getElementById("editDialog_" + sectionId +"_"+ fieldId);
			var boxes = getElementsByName(div, sectionId+"_field_" + fieldId);
			if (boxes.length == 0) continue;

			var isNormal = boxes[0].checked;
			var allowNormal = document.getElementById("allowNormal"+sectionId+"_"+fieldId).value;
			var formDetailsDiv = document.getElementById('sectionDetailsDiv_'+sectionId);

			if (allowNormal == 'Y' && isNormal) {
				optionsText += "";
			} else {
				var fieldText = "";
				var start = 0;
				if (allowNormal == 'Y') start = 1; // if normal is there checkbox values should start from 1.
				for (var i=start; i< boxes.length; i++) {
					if (boxes[i].checked) {
						if (optionsText != "")
							optionsText += ', ';

						if (boxes[i].value == -1) {
							optionsText += document.forms[formName][sectionId+"_option_remarks_"+fieldId + "_" + boxes[i].value].value;
						} else {
							optionsText += jOptionsList[sectionId][boxes[i].value].option_value;
							var optionRemarks = document.forms[formName][sectionId+"_option_remarks_"+fieldId + "_" + boxes[i].value].value;
							if (optionRemarks != '')
								optionsText += '-' + optionRemarks;
						}
					}
				}
			}
		} else if (field_type == 'date') {
			optionsText += document.forms[formName][sectionId+"_date_"+fieldId].value;
		} else if (field_type == 'datetime') {
			optionsText += document.forms[formName][sectionId+"_date_"+fieldId].value;
			optionsText += " " + document.forms[formName][sectionId+"_time_"+fieldId].value;
		}
		if (trim(optionsText) != '') {
			if (j!=0) {
				divText += ",";
			}
			divText += "<b>" + fieldNames[j].value + ": </b>";
			divText += optionsText;
		}
	}
	document.getElementById('sectionDetailsDiv_'+sectionId).innerHTML = divText;

}

function toggleMarkerTables(sectionId, fieldId) {
	if (document.getElementById('chkbox_toggle_mrkr_tables_'+sectionId+"_"+fieldId).checked) {
		document.getElementById('mrkr_with_lbl_table_'+sectionId+"_"+fieldId).style.display = 'block';
		document.getElementById('mrkr_without_lbl_table_'+sectionId+"_"+fieldId).style.display = 'none'
	} else {
		document.getElementById('mrkr_without_lbl_table_'+sectionId+"_"+fieldId).style.display = 'block'
		document.getElementById('mrkr_with_lbl_table_'+sectionId+"_"+fieldId).style.display = 'none';
	}
	changeColor(sectionId, fieldId, document.getElementById("marker_selected_"+sectionId+"_"+fieldId).value);
}

function markerSelected(sectionId, fieldId, markerId) {
	var delMarker = document.getElementById("hidden_delete_marker_"+sectionId+"_"+fieldId).value;
	if (delMarker == 'true') {
		showMessage('js.outpatient.consultation.mgmt.deletingmarkerenabled');
		return false;
	}
	document.getElementById("marker_selected_"+sectionId+"_"+fieldId).value = markerId;
	changeColor(sectionId, fieldId, markerId);
}

function changeColor(sectionId, fieldId, markerId) {
	if (document.getElementById('mrkr_with_lbl_table_'+sectionId+"_"+fieldId).style.display == 'block') {
		var mrkr_img_with_label = document.getElementsByName("mrkr_img_with_lbl_"+sectionId+"_"+fieldId);
		for (var i=0; i<mrkr_img_with_label.length; i++) {
			if (mrkr_img_with_label[i].id == "mrkr_img_with_lbl_"+sectionId+"_"+fieldId+"_"+markerId) {
				addClassName(mrkr_img_with_label[i].parentNode, "mrkr_selected");
				addClassName(YAHOO.util.Dom.getNextSiblingBy(mrkr_img_with_label[i].parentNode), 'mrkr_selected');
			} else {
				removeClassName(mrkr_img_with_label[i].parentNode, "mrkr_selected");
				removeClassName(YAHOO.util.Dom.getNextSiblingBy(mrkr_img_with_label[i].parentNode), 'mrkr_selected');
			}
		}
	} else {
		var mrkr_img_without_label = document.getElementsByName("mrkr_img_without_lbl_"+sectionId+"_"+fieldId);
		for (var i=0; i<mrkr_img_without_label.length; i++) {
			if (mrkr_img_without_label[i].id == "mrkr_img_without_lbl_"+sectionId+"_"+fieldId+"_"+markerId) {
				addClassName(mrkr_img_without_label[i].parentNode, "mrkr_selected");
			} else {
				removeClassName(mrkr_img_without_label[i].parentNode, "mrkr_selected");
			}
		}
	}
}

function deleteMarker(obj, sectionId, fieldId) {
	var delMarker = document.getElementById("hidden_delete_marker_"+sectionId+"_"+fieldId).value;
	if (delMarker == 'true') {
		// obj: is the image marker element placed on the main image.
		// this is the div element, which consists of image marker which is placed on the main image, and it's details
		var div = obj.parentNode;
		div.parentNode.removeChild(div);
	}
}

function toggleMarkerDelete(obj, sectionId, fieldId) {
	var delMarker = document.getElementById("hidden_delete_marker_"+sectionId+"_"+fieldId);
	delMarker.value = delMarker.value != 'true';
 	if (delMarker.value == 'true') {
		// deleting the marker is enabled. so remove the marker selected in marker list for marking image.
		document.getElementById("marker_selected_"+sectionId+"_"+fieldId).value = '';
		changeColor(sectionId, fieldId, '');
		addClassName(obj, 'mrkr_selected');
	} else {
		removeClassName(obj, 'mrkr_selected');
	}
}

function updateXY(event, sectionId, fieldId, formName) {
	var delMarker = document.getElementById("hidden_delete_marker_"+sectionId+"_"+fieldId).value;
	if (delMarker == 'true') return ;

	var pos_x = 0;
	var pos_y = 0;
	var obj = document.getElementById("image_"+sectionId+"_"+fieldId);
	if (event.offsetX) {
		pos_x = event.offsetX;
		pos_y = event.offsetY;
	} else {
		var offsetLeft = 0;
		var offsetTop = 0;
		if (obj.offsetParent) {
			do {
				offsetLeft += obj.offsetLeft;
				offsetTop += obj.offsetTop;
			} while(obj = obj.offsetParent);
		}
		pos_x = event.pageX - offsetLeft;
		pos_y = event.pageY - offsetTop;

	}

	var div = document.getElementById("image_" + sectionId + "_" + fieldId);
	var markerDivs = getElementsByName(div, "markerTemplateDiv_"+ sectionId + "_" + fieldId);
	if (event.target.tagName == 'IMG') {
		notesObj = getElementByName(event.target.parentNode, sectionId + "_notes_" + fieldId);
		//notesObj = document.forms[formName][sectionId + "_notes_" + fieldId][markerDivs.length-1];
		document.getElementById('d_notes').value = notesObj.value;
		notesDialog.cfg.setProperty('context', [event.target.parentNode, 'tl', 'tl', ["beforeShow", "windowResize"], [pos_x, pos_y]], false);
		notesDialog.show();
		return ;
		// open the notes dialog, allow the user to enter the notes.
	}
	var markerSelected = document.getElementById("marker_selected_"+sectionId+"_"+fieldId).value;

	if (markerSelected == '') {
		showMessage("js.outpatient.consultation.mgmt.selecthemarker");
		return false;
	}
	var templateDiv = markerDivs[markerDivs.length-1];
	var clonedDiv = templateDiv.cloneNode(true);
	clonedDiv.style.display = '';
	// cross marker onclick function for duplicate sections
	if (sectionId.indexOf("_new_") >= 0) {
		temp_cross_mkr = clonedDiv.getElementsByClassName("deleteMarkerClass")[0];
		temp_cross_mkr.onclick =
			function(elmt, ds_detail_id, fld) {
			return function() {
				deleteMarker(elmt, ds_detail_id, fld);
				};
		}(temp_cross_mkr, sectionId, fieldId);
	}

	div.insertBefore(clonedDiv, templateDiv);

	// when using formname to retrieve the image element, it is not returning in the elements in order. hence
	// used document.getElementsByName() to retrieve the image element.
	var markerImage = document.getElementsByName("cross_" + sectionId + "_" + fieldId)[markerDivs.length-1];
	markerImage.src = cpath + "/master/ImageMarkers/ViewImage.do?_method=view&image_id="+markerSelected;

	// centering the marker image.
	var left = (pos_x-(markerImage.width/2));
	var top = (pos_y-(markerImage.height/2));
	markerImage.style.left = left + 'px';
	markerImage.style.top = top + 'px';
	markerImage.style.display = 'block';

	document.forms[formName][sectionId + "_marker_id_" + fieldId][markerDivs.length-1].value = markerSelected;
	document.forms[formName][sectionId + "_coordinate_x_" + fieldId][markerDivs.length-1].value = left;
	document.forms[formName][sectionId + "_coordinate_y_" + fieldId][markerDivs.length-1].value = top;

}

var notesDialog = null;
var notesObj = null;
function initNotesDialog() {
	if (!document.getElementById('notesDialog')) return;
	document.getElementById('notesDialog').style.display = 'block';
	notesDialog = new YAHOO.widget.Dialog("notesDialog", {
				width:"300px",
				context :["notesDialog", "bl", "tl"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('notes_ok', 'click', addOrUpdateNotes, notesDialog, true);
	YAHOO.util.Event.addListener('notes_close', 'click', cancelNotes, notesDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:cancelNotes, scope:notesDialog, correctScope:true } );
	notesDialog.cfg.queueProperty("keylisteners", escKeyListener);
	notesDialog.render();
}

function cancelNotes() {
	document.getElementById('d_notes').value = '';
	notesObj = null;
	notesDialog.cancel();
}
function addOrUpdateNotes() {
	var notes = document.getElementById('d_notes').value;
	notesObj.value = notes.trim();

	// set the title for the image, to make hover work.
	var div = notesObj.parentNode;
	var childNodes = div.childNodes;
	for (var i=0; i<childNodes.length; i++) {
		if (childNodes[i].tagName == 'IMG') {
			childNodes[i].title = notes.trim();
			break;
		}
	}
	document.getElementById('d_notes').value = '';
	notesObj = null;
	notesDialog.cancel();
}


function hasEidtRights(section_id) {
	var flag = false;
	for (var i=0; i< editable_sections.length ; i++) {
		if (section_id == editable_sections[i]) {
			flag = true;
		}
	}
	return (all_section_edit_rights || (editable_sections !=null && flag));
}
function validateSysGenForms() {
	var forms_exists = document.getElementsByName('sys_gen_section_id');
	var diagnosisMandatory = false;
	var diagnosisExists = false;
	for (var i=0; i<sys_generated_forms.length; i++) {
		for (var j=0; j<forms_exists.length; j++) {
			if(forms_exists[j].value == -13) {
				if (!validateObstetricDetails()) {
					return false;
				}
			}
			if (forms_exists[j].value == -6) {
				diagnosisExists = true;
			}
			if (sys_generated_forms[i].section_mandatory) {
				if (forms_exists[j].value == sys_generated_forms[i].section_id) {
					// component exists for this consultation.
					if (sys_generated_forms[i].section_name == 'Complaint (Sys)') {
						if (!complaintEntered()) {
							showMessage("js.outpatient.consultation.mgmt.enteratleastonecomplaint");
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Allergies (Sys)') {
						if (hasEidtRights("-2") && !allergyEntered()) {
							showMessage("js.outpatient.consultation.mgmt.enteratleastoneallergy");
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Obstetric History (Sys)') {
						if (hasEidtRights("-13") && !checkPregnancies()) {
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Antenatal (Sys)') {
						if (hasEidtRights("-14") && !checkAntenatal()) {
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Pre-Anaesthetic Checkup (Sys)') {
						if (hasEidtRights("-16") && !pacEntered()) {
							showMessage("js.outpatient.consultation.mgmt.enteratleastonepac");
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Health Maintenance (Sys)') {
						if (hasEidtRights("-15") && !checkHealthMaintenance()) {
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Vitals (Sys)') {
						if (!validateVitals()) {
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Consultation Notes (Sys)') {
						if (hasEidtRights("-5") && !consultationNotesEntered()) {
							showMessage("js.outpatient.consultation.mgmt.enteratleastonevalueinconsultationnotes");
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Prescriptions (Sys)') {
						if (document.getElementById('itemsTable').rows.length == 2) {
							showMessage("js.outpatient.consultation.mgmt.prescribeatleastoneitem");
							return false;
						}
					} else if (sys_generated_forms[i].section_name == 'Diagnosis Details (Sys)' ) {
						diagnosisMandatory = true;
					}
					// diagnosis details checked only when the mod_mrd_icd is enabled.
				}
			}
		}
	}
	if (diagnosisExists && !validateDiagnosisDetails(false, diagnosisMandatory, true)) return false;



	return true;
}

function addNewSection(section_id) {
	if (!validateToRepeatSection(section_id)) {
		return false;
	}
	var section_count = parseInt(document.getElementById("new_sections_count_" + section_id).value) + 1;
	document.getElementById("new_sections_count_" + section_id).value = section_count;
	var dyn_section_detail_id = section_id + "_new_" + section_count;
	var x = document.getElementById("section_" + section_id + "_new");
	var cln = x.cloneNode(true);
	cln.id = "section_" + dyn_section_detail_id;
	cln.style.display = "block";
	var allElm = cln.getElementsByTagName("div");
	var allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		}
	}
	allElm = cln.getElementsByTagName("span");
	allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		}
	}
	allElm = cln.getElementsByTagName("a");
	allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		}
	}
	allElm = cln.getElementsByTagName("table");
	allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		}
	}
	allElm = cln.getElementsByTagName("label");
	allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		}
	}
	allElm = cln.getElementsByTagName("img");
	allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		}
		if (allElm[i].name != "") {
			allElm[i].name = allElm[i].name.replace(/_new/i, '_new_' + section_count);
		}
	}
	allElm = cln.elements;
	allElm_l = allElm.length;
	for ( var i=0; i< allElm_l; i++) {
		if (allElm[i].id != "") {
			allElm[i].id = allElm[i].id.replace(/_new/i, '_new_' + section_count);
		} else if (allElm[i].name === "section_detail_ids_" + section_id) {
			// adding the new section dynamic section_detail_id to list
			allElm[i].value = dyn_section_detail_id;
		}
		if (allElm[i].name != "") {
			allElm[i].name = allElm[i].name.replace(/_new/i, '_new_' + section_count);
		}
	}

	document.getElementById("newSectionArea_" + section_id).appendChild(cln);
	document.getElementById("section_title_" + dyn_section_detail_id).innerHTML =
		document.getElementById("section_title_" + dyn_section_detail_id).innerHTML + " " + section_count + "(new)";

	var linkedTo = "";
	for (var j=0; j<insta_sections_json.length; j++) {
		var masterSectionId = insta_sections_json[j].section_id;
		if (masterSectionId == section_id) {
			linkedTo = insta_sections_json[j].linked_to;
		}
	}
	// resize the collapsible panel, when we minimize the patient section.
	if (mrNo && !empty(collapsiblePanels) && collapsiblePanels[mrNo] && linkedTo == 'patient') {
		collapsiblePanels[mrNo].open(); // on ok button of dialog we are reopening a same collapsiblePanel again
	}

	var fields = document.getElementsByName('field_ids_' + section_id);
	var fields_l = fields.length;
	for (var j = 0; j < fields_l; j++) {
		var temp_field_type = document.getElementsByName('field_type_' + fields[j].value)[0].value;
		if (temp_field_type === 'text' || temp_field_type === 'wide text') {
			var temp_phrase_elmt = document.getElementById("pharse_" + dyn_section_detail_id + "_" + fields[j].value);
			if (temp_phrase_elmt != null) {
				temp_phrase_elmt.onclick =
					function(elmt, ds_detail_id, fld, op_id, ctg_id) {
					return function() {
						showPhraseEditDialog(elmt, ds_detail_id, fld, op_id, ctg_id);
					};
				}(temp_phrase_elmt, dyn_section_detail_id, fields[j].value, null, document.getElementById("field_phrase_category_id_" + fields[j].value).value);
			}
		}
		if (temp_field_type === 'datetime' || temp_field_type === 'date') {
			// initialize date widget
			// check if field need to default to current date time
			var datevalue = null;
			var timevalue = null;
			var default_to_current_datetime = document.getElementById(section_id + '_' + fields[j].value + '_default_to_current_datetime');
			if (default_to_current_datetime && default_to_current_datetime.value == 'Y') {
				datevalue = new Date();
				timevalue = formatTime(new Date(), false);
			}
			document.getElementById("datewidget_" + dyn_section_detail_id + "_date_" + fields[j].value).innerHTML =
				getDateWidget(dyn_section_detail_id + "_date_" + fields[j].value, dyn_section_detail_id + "_date_" + fields[j].value, datevalue, '', '', true, true, '', cpath);
			makePopupCalendar(dyn_section_detail_id + "_date_" + fields[j].value);
			if (temp_field_type === 'datetime') {
				document.getElementsByName(dyn_section_detail_id + '_time_' + fields[j].value)[0].value = timevalue;
			}
		} else if (temp_field_type === 'dropdown') {
			document.getElementsByName(dyn_section_detail_id + "_field_" + fields[j].value)[0].onchange =
				function(fld, ds_detail_id) {
					return function() {
						onChangeOption(fld, ds_detail_id);
					};
				}(fields[j].value, dyn_section_detail_id);
		} else if ( temp_field_type === 'checkbox') {
			// initializing the checkbox widgets
			var dialog = new YAHOO.widget.Dialog("editDialog_" + dyn_section_detail_id + "_" + fields[j].value, {
				width:"450px",
				context :["field" + dyn_section_detail_id + "_" + fields[j].value, "bl", "tl"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});

			var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
					{ fn:sectionFieldDialogCancel, scope:dialog, correctScope:true } );
			dialog.cfg.queueProperty("keylisteners", escKeyListener);
			dialog.render();
			dialogs[dyn_section_detail_id + "_" + fields[j].value] = dialog;

			// Editing the checkbox onclick functions
			var checkbox_onclicks = document.getElementsByName(dyn_section_detail_id + "_field_" + fields[j].value);
			var checkbox_onclicks_l = checkbox_onclicks.length;
			for ( var chkbox = 0; chkbox < checkbox_onclicks_l; chkbox++) {
				if (checkbox_onclicks[chkbox].value === "0") {
					checkbox_onclicks[chkbox].onclick =
						function(fld, ds_detail_id) {
							return function() {
								onClickNormal(fld, ds_detail_id);
							};
						}(fields[j].value, dyn_section_detail_id);
				}
				else {
					checkbox_onclicks[chkbox].onclick =
						checkbox_onclicks[chkbox].onclick =
							function(elm, fld, ds_detail_id) {
								return function() {
									onClickChkBox(elm, fld, ds_detail_id);
								};
							}(checkbox_onclicks[chkbox], fields[j].value, dyn_section_detail_id);
				}
			}
			// changing Edit icon onclick function
			document.getElementById("checkboxEditBtn_" + dyn_section_detail_id + "_" + fields[j].value).onclick =
				function(ds_detail_id, fld) {
				return function() {
					showPysicianFormEditDialog(ds_detail_id , fld);
				};
			}(dyn_section_detail_id, fields[j].value);
			// changing dialog ok onclick function
			document.getElementById("btn_editDialog_" + dyn_section_detail_id + "_" + fields[j].value).onclick =
				function(fld, ds_detail_id) {
				return function() {
					updateChecboxFieldText(fld, ds_detail_id);
				};
			}(fields[j].value, dyn_section_detail_id);
			// changing phrase onclick functions
			var temp_checkboxs = document.getElementsByName(dyn_section_detail_id + "_field_" + fields[j].value);
			var temp_checkboxs_l = temp_checkboxs.length;
			for (var i = 0; i < temp_checkboxs_l; i++) {
				if (temp_checkboxs[i].value != "0" && temp_checkboxs[i].value != "-1") {
					var temp_phrase_elmt = document.getElementById(
							"phrase_" + dyn_section_detail_id + "_" + fields[j].value + "_" + temp_checkboxs[i].value);
					if (temp_phrase_elmt != null) {
						temp_phrase_elmt.onclick =
							function(elmt, ds_detail_id, fld, op_id, ctg_id) {
							return function() {
								showPhraseEditDialog(elmt, ds_detail_id, fld, op_id, ctg_id);
							};
						}(temp_phrase_elmt, dyn_section_detail_id, fields[j].value, temp_checkboxs[i].value,
								document.getElementById("option_phrase_category_id_" + temp_checkboxs[i].value).value);
					}
				}
			}
		} else if (temp_field_type === 'image') {
			document.getElementById("markerTemplateDiv_" + dyn_section_detail_id + "_" + fields[j].value).setAttribute("name",
					"markerTemplateDiv_" + dyn_section_detail_id + "_" + fields[j].value);
			// mark event onclick function
			document.getElementById("image_" + dyn_section_detail_id + "_" + fields[j].value).onclick =
				function(ds_detail_id, fld) {
				return function(e) {
					updateXY(e, ds_detail_id, fld, sectionFormName);
					};
			}(dyn_section_detail_id, fields[j].value);

			// Show marker Onclick function
			document.getElementById("chkbox_toggle_mrkr_tables_" + dyn_section_detail_id + "_" + fields[j].value).onclick =
				function(ds_detail_id, fld) {
				return function() {
					toggleMarkerTables(ds_detail_id, fld);
					};
			}(dyn_section_detail_id, fields[j].value);

			// Delete marker onclick function
			var temp_mkr_del_elmt = document.getElementById("toggle_marker_delete_" + dyn_section_detail_id + "_" + fields[j].value);
			temp_mkr_del_elmt.onclick =
				function(elmt, ds_detail_id, fld) {
				return function() {
					toggleMarkerDelete(elmt, ds_detail_id, fld);
					};
			}(temp_mkr_del_elmt, dyn_section_detail_id, fields[j].value);

			// img markers with label
			var temp_img_markers = document.getElementsByName("mrkr_img_with_lbl_" + dyn_section_detail_id + "_" + fields[j].value);
			temp_img_markers_l = temp_img_markers.length;
			for (var i = 0; i < temp_img_markers_l; i++) {
				temp_marker_id = temp_img_markers[i].id.split("_").pop(-1);
				temp_img_markers[i].onclick =
					function(ds_detail_id, fld, mrk) {
					return function() {
						markerSelected(ds_detail_id, fld, mrk);
						};
				}(dyn_section_detail_id, fields[j].value, temp_marker_id);
				document.getElementById("mrkr_img_lbl_" +
						dyn_section_detail_id + "_" + fields[j].value + "_" + temp_marker_id).onclick =
							function(ds_detail_id, fld, mrk) {
								return function() {
									markerSelected(ds_detail_id, fld, mrk);
									};
							}(dyn_section_detail_id, fields[j].value, temp_marker_id);
			}

			//img markers without label
			var temp_img_markers = document.getElementsByName("mrkr_img_without_lbl_" + dyn_section_detail_id + "_" + fields[j].value);
			temp_img_markers_l = temp_img_markers.length;
			for (var i = 0; i < temp_img_markers_l; i++) {
				temp_marker_id = temp_img_markers[i].id.split("_").pop(-1);
				temp_img_markers[i].onclick =
					function(ds_detail_id, fld, mrk) {
					return function() {
						markerSelected(ds_detail_id, fld, mrk);
						};
				}(dyn_section_detail_id, fields[j].value, temp_marker_id);
			}
		}
	}
	// changing Remove onclick function
	document.getElementById("remove_" +dyn_section_detail_id).onclick =
		function(sec_id, count) {
			return function() {
				removeSection(sec_id, count);
			};
		}(section_id, section_count);
	// changing minimize onclick funtion
	var min_btn = document.getElementById("insta_section_btn_" +dyn_section_detail_id);
	min_btn.onclick =
		function(sec_id, ds_detail_id, btn_elmt) {
			return function() {
				toggleInstaSection(sec_id, ds_detail_id, btn_elmt);
			};
		}(section_id, dyn_section_detail_id, min_btn);

	// all normal onclick function
	if (document.getElementById("allow_all_normal_div" +dyn_section_detail_id)) {
		document.getElementById("allow_all_normal_div" +dyn_section_detail_id).onclick =
			function(sec_id, ds_detail_id) {
			return function() {
				allNormal(sec_id, ds_detail_id);
			};
			}(section_id, dyn_section_detail_id);
	}
}

function removeSection(section_id, count) {
	document.getElementById("newSectionArea_" + section_id).removeChild(
			document.getElementById("section_" + section_id + "_new_" + count));

	var linkedTo = "";
	for (var j=0; j<insta_sections_json.length; j++) {
		var masterSectionId = insta_sections_json[j].section_id;
		if (masterSectionId == section_id) {
			linkedTo = insta_sections_json[j].linked_to;
		}
	}
	// resize the collapsible panel, when we minimize the patient section.
	if (mrNo && !empty(collapsiblePanels) && collapsiblePanels[mrNo] && linkedTo == 'patient') {
		collapsiblePanels[mrNo].open(); // on ok button of dialog we are reopening a same collapsiblePanel again
	}
}

function updateChecboxFieldText(field_id, section_detail_id) {
	var x = document.getElementsByName(section_detail_id + "_field_" + field_id);
	var y = "";
	for (var i=0; i<x.length; i++) {
		if (x[i].checked == true) {
			if (x[i].value != "-1") {
				y = y + document.getElementById(section_detail_id + "_option_value_" + field_id + "_" + x[i].value).innerHTML + "-" +
						document.getElementById(section_detail_id + "_option_remarks_" + field_id + "_" + x[i].value).value +
						"<br />"
			} else {
				y = y + document.getElementById(section_detail_id + "_option_remarks_" + field_id + "_" + x[i].value).value + "<br />"
			}
		}
	}
	document.getElementById("fieldText_" + section_detail_id + "_" + field_id).innerHTML = y;
	var dialog = dialogs[section_detail_id + "_" + field_id];
	dialog.cancel();
}

function onClickNormal(fieldId, sectionId) {
	var div = document.getElementById("editDialog_" + sectionId + "_" + fieldId);
	var boxes = getElementsByName(div, sectionId+"_field_" + fieldId);
	document.getElementsByName(sectionId + "_option_remarks_" + fieldId + "_0")[0].disabled = !boxes[0].checked;
	var isNormal = boxes[0].checked;
	for (var i=1; i< boxes.length; i++) {
		boxes[i].disabled = isNormal;
		document.getElementsByName(sectionId + "_option_remarks_" + fieldId + "_" + boxes[i].value)[0].disabled = isNormal;
		if (boxes[i].disabled) {
			boxes[i].checked = false;
			document.getElementsByName(sectionId + "_option_remarks_" + fieldId + "_"+ boxes[i].value).value = '';
		}
	}
}

function onClickChkBox(checkbox, fieldId, sectionId) {
	document.getElementsByName(sectionId + "_option_remarks_" + fieldId + "_" +checkbox.value)[0].disabled = !checkbox.checked;
}

function onChangeOption(fieldId, sectionDetailId) {
	var sel = document.getElementsByName(sectionDetailId + "_field_" + fieldId);
	var text = document.getElementsByName(sectionDetailId +"_option_remarks_" + fieldId);

	if (text.length > 0) {
		// do enabling and disabling only when others text box is present.
		if (sel[0].value === "-1")
			text[0].readOnly = false;
		else
			text[0].readOnly = true;
	}
}

function toggleInstaSection(sectionId, sectionDetailId, sectionBtn) {
	if (sectionBtn.value == "-") {
		document.getElementById("section_detail_view_" + sectionDetailId).style.display = "none";
		reCreateDivTextNew(sectionId, sectionDetailId);
		document.getElementById(sectionDetailId + "_minText").style.display = "block";
		sectionBtn.value = "+";
	}
	else {
		document.getElementById("section_detail_view_" + sectionDetailId).style.display = "block";
		sectionBtn.value = "-";
		document.getElementById(sectionDetailId + "_minText").innerHTML = "";
	}
	var linkedTo = "";
	for (var j=0; j<insta_sections_json.length; j++) {
		var masterSectionId = insta_sections_json[j].section_id;
		if (masterSectionId == sectionId) {
			linkedTo = insta_sections_json[j].linked_to;
		}
	}
	// resize the collapsible panel, when we minimize the patient section.
	if (mrNo && !empty(collapsiblePanels) && collapsiblePanels[mrNo] && linkedTo == 'patient') {
		collapsiblePanels[mrNo].open(); // on ok button of dialog we are reopening a same collapsiblePanel again
	}
}


function reCreateDivTextNew(sectionId, sectionDetailId) {

	var fieldIds = document.getElementsByName("field_ids_"+sectionId);
	var fieldIds_l = fieldIds.length
	var divText = "";
	for (var j = 0; j < fieldIds_l; j++) {
		var optionsText = '';
		var field_type = document.getElementsByName('field_type_' + fieldIds[j].value)[0].value;
		if (field_type == 'text' || field_type == 'wide text') {
			optionsText += document.getElementsByName(sectionDetailId + "_option_remarks_" + fieldIds[j].value)[0].value;
		} else if (field_type == 'date') {
			optionsText += document.getElementsByName(sectionDetailId + "_date_" + fieldIds[j].value)[0].value;
		} else if (field_type == 'datetime') {
			optionsText += document.getElementsByName(sectionDetailId + "_date_" + fieldIds[j].value)[0].value;
			optionsText += " " + document.getElementsByName(sectionDetailId + "_time_" + fieldIds[j].value)[0].value;
		} else if (field_type ==  'dropdown') {
			var dropdown = document.getElementsByName(sectionDetailId + '_field_' + fieldIds[j].value)[0];
			var value = dropdown.value;
			if (value == 0) {
				// it is normal no need to display.
			} else if (value == -1) {
				// others text
				optionsText += document.getElementsByName(sectionDetailId + '_option_remarks_' + fieldIds[j].value)[0].value;
			} else {
				optionsText += dropdown.options[dropdown.options.selectedIndex].text;
			}
		} else if (field_type == 'checkbox') {
			var checkboxs = document.getElementsByName(sectionDetailId + "_field_" + fieldIds[j].value);
			var checkboxs_l = checkboxs.length;
			for (var i = 0; i < checkboxs_l; i++) {
				// no need to display normal.
				if ( checkboxs[i].checked && checkboxs[i].value != "0" && checkboxs[i].value != "-1") {
					optionsText += document.getElementById(sectionDetailId +"_option_value_" + fieldIds[j].value + "_" + checkboxs[i].value).innerHTML;
					optionsText += "-" + document.getElementById(sectionDetailId +"_option_remarks_" + fieldIds[j].value + "_" + checkboxs[i].value).value;
					optionsText += (i < checkboxs_l) ? "," : "";
				} else if (checkboxs[i].checked && checkboxs[i].value == "-1") {
					optionsText += document.getElementById(sectionDetailId +"_option_remarks_" + fieldIds[j].value + "_" + checkboxs[i].value).value;
				}
			}
		}
		if (trim(optionsText) != '') {
			divText += "<b>" + document.getElementById("field_title_" + sectionDetailId + "_" + fieldIds[j].value).innerHTML +
						"</b>: " + optionsText + ((j< fieldIds_l) ? "; " : "");
		}
	}
	document.getElementById(sectionDetailId + "_minText").innerHTML = divText;
}

function showPhraseEditDialog(obj, sectionDetailId, fieldId, optionId, categoryId) {

	var phrases = [];
	if (!empty(optionId)) {
		var chkbox = document.getElementsByName(sectionDetailId + "_field_" + fieldId);
		var option_checked = false;

		for (var i=0; i<chkbox.length; i++) {
			if (chkbox[i].value == optionId && chkbox[i].checked) {
				option_checked = true;
			}
		}
		if (!option_checked) {
			showMessage('js.outpatient.consultation.mgmt.tickcorrespondingoption');
			return false;
		}
	}
	var table = document.getElementById('phrases_table');
	for (var i=0; i<table.rows.length; ) {
		table.deleteRow(i);
	}
	var clear_field_label = document.getElementById('section_field_clear_anchor');
	clear_field_label.onclick = function () {
		if (!empty(optionId)) {
			var text = document.getElementsByName(sectionDetailId + "_option_remarks_" + fieldId + "_" + optionId)[0];
			if (!text.disabled) {
				text.value = '';
			}
		} else {
			var text = document.getElementsByName(sectionDetailId +"_option_remarks_" + fieldId)[0];
			text.value = '';
		}
		return false;
	}
	var undo_last_label = document.getElementById('section_field_undo_last');
	undo_last_label.onclick = function () {
		if (!empty(optionId)) {
			var text = document.getElementsByName(sectionDetailId + "_option_remarks_" + fieldId + "_" + optionId)[0];
			if (!text.disabled) {
				undoLastPhrase(text, sectionDetailId +"_option_remarks_" + fieldId + "_"+optionId);
			}
		} else {
			var text = document.getElementsByName(sectionDetailId +"_option_remarks_" + fieldId)[0];
			undoLastPhrase(text, sectionDetailId +"_option_remarks_" + fieldId);
		}
		return false;
	}
	var undoLastPhrase = function(obj, id) {
		var val = obj.value;
		var lastPhrase = last_added_phrases[id];
		if (empty(lastPhrase)) return ;

		if (val.trim() == lastPhrase.trim()) {
			obj.value = '';
		} else if (val.match('^'+lastPhrase)) {
			var regexp = new RegExp(lastPhrase + ',\\s*');
			obj.value = val.replace(regexp, '');
		} else if (val.match(lastPhrase+'$')) {
			var regexp = new RegExp(',\\s*'+lastPhrase);
			obj.value = val.replace(regexp, '');
		} else {
			var regexp = new RegExp(',\\s*'+lastPhrase);
			obj.value = val.replace(regexp, '');
		}
		last_added_phrases[id] = ''; // removing last phrase from the array.
	}

	phrases = phrase_suggestions_by_dept_json[categoryId];
	if(phrases != undefined && phrases != '' && phrases != null){
		for (var i=0; i<phrases.length; i++) {
			var row = table.insertRow(-1);

			var cell = row.insertCell(-1);
			var style = 'border-top: 1px #CCCCCC solid;'
			if (i == phrases.length-1)
				style += 'border-bottom: 1px #CCCCCC solid;';
			style += 'padding: 3px 0px 0px 5px;';

			cell.setAttribute("style", style);
			// for add phrase anchor, onclick returning false just to avoid the javascript error.
			cell.innerHTML = '<a href="javascript:Add Phrase" onclick="return false;" ondblclick="return appendPhraseNew(this,\''+sectionDetailId+'\','+fieldId+','+optionId+');">' +phrases[i].phrase_suggestions_desc + '</a>';
		}
	}
	phraseChildDialog = phraseDialog;
	phraseDialog.cfg.setProperty('context', [obj, 'tr', 'bl'], false);
	phraseDialog.show();
}

function appendPhraseNew(label, sectionDetailId, fieldId, optionId) {
	var phrase = label.textContent;
	if (!empty(optionId)) {
		var text = document.getElementsByName(sectionDetailId + "_option_remarks_" + fieldId + "_" + optionId)[0];
		if (!text.disabled) {
			text.value += (text.value != '' ? ", " : '') + phrase;
			last_added_phrases[sectionDetailId + "_option_remarks_" + fieldId + "_" + optionId] = phrase;
		}
	} else {
		var text = document.getElementsByName(sectionDetailId +"_option_remarks_" + fieldId)[0];
		text.value += (text.value != '' ? ", " : '') + phrase;
		last_added_phrases[sectionDetailId +"_option_remarks_" + fieldId] = phrase;
	}
	return false;
}

function validateToRepeatSection(section_id) {
	var sub_sections = document.getElementsByName("section_detail_ids_" + section_id);
	var sub_sections_l = sub_sections.length;
	var section_fields = document.getElementsByName("field_ids_" + section_id);
	var section_fields_l = section_fields.length;
	for (var i = 0; i < sub_sections_l && sub_sections[i].value != ''; i++) {
		var is_fieldValue_exists = false;
		var temp_section_name = document.getElementById("section_title_" + sub_sections[i].value).textContent;
		for (var j = 0; j < section_fields_l; j++) {
			fld_type = document.getElementsByName("field_type_" + section_fields[j].value)[0].value;
			if (fld_type == 'text' || fld_type == 'wide text') {
				if (trim(document.getElementsByName(sub_sections[i].value + "_option_remarks_" + section_fields[j].value)[0].value) != '') {
					is_fieldValue_exists = true;
				}
			} else if (fld_type == 'date' ){
				if (trim(document.getElementsByName(sub_sections[i].value + "_date_" + section_fields[j].value)[0].value) != '') {
					is_fieldValue_exists = true;
				}
			} else if (fld_type == 'datetime' ){
				if (trim(document.getElementsByName(sub_sections[i].value + "_date_" + section_fields[j].value)[0].value) != '' &&
						trim(document.getElementsByName(sub_sections[i].value + "_time_" + section_fields[j].value)[0].value) != '') {
					is_fieldValue_exists = true;
				}
			} else if (fld_type == 'checkbox' ){
				var boxes = document.getElementsByName(sub_sections[i].value + "_field_" + section_fields[j].value);
				var boxes_l = boxes.length;
				for (var k = 0; k < boxes_l; k++) {
					if(boxes[k].checked) {
						is_fieldValue_exists = true;
					}
				}
			} else if (fld_type == 'dropdown' ){
				if (trim(document.getElementsByName(sub_sections[i].value + "_field_" + section_fields[j].value)[0].value) != '') {
					is_fieldValue_exists = true;
				}
			}
		}
		if (!is_fieldValue_exists) {
			alert("All fields in '" + temp_section_name + "' are empty. Please enter any one field");
			return false;
		}
	}
	return true;
}

function validateMandatoryFields() {
	var all_fields = document.getElementsByName("is_fields_mandatory");
	var all_fields_l = all_fields.length;
	var all_fields_map = {};
	var fields_type_map = {};
	var pattern_ids_map = {};
	for (var i = 0; i < all_fields_l; i++) {
		temp_field_id = all_fields[i].id.split("_").pop(-1);
		all_fields_map[temp_field_id] = all_fields[i].value == "true";
		fields_type_map[temp_field_id] = document.getElementsByName("field_type_" + temp_field_id)[0].value;
		pattern_ids_map[temp_field_id] = document.getElementsByName("pattern_id_" + temp_field_id)[0].value;
	}
	var all_sections = document.getElementsByName("insta_sections");
	var all_sections_l = all_sections.length;
	for (var i = 0; i < all_sections_l; i++) {
		var is_section_mandatory = document.getElementById("section_mandatory_" + all_sections[i].value).value == "true";
		var section_fields = document.getElementsByName("field_ids_" + all_sections[i].value);
		var section_fields_l = section_fields.length;
		var sub_sections = document.getElementsByName("section_detail_ids_" + all_sections[i].value);
		var sub_sections_l = sub_sections.length;
		for (var j = 0; j < sub_sections_l && hasEidtRights(all_sections[i].value); j++) {
			var temp_section_name = trim(sub_sections[j].value) != '' ? document.getElementById("section_title_" + sub_sections[j].value).textContent : '';
			var is_section_empty = true;
			for (var k = 0; k < section_fields_l && trim(sub_sections[j].value) != ''; k++) {
				var temp_field_name = document.getElementById("field_title_" + sub_sections[j].value + "_" + section_fields[k].value).textContent;
				if (fields_type_map[section_fields[k].value] == 'text' || fields_type_map[section_fields[k].value] == 'wide text') {
					var temp_element = document.getElementsByName(sub_sections[j].value + "_option_remarks_" + section_fields[k].value)[0];
					is_section_empty = (trim(temp_element.value) == '') ? is_section_empty : false;
					if (trim(temp_element.value) == '' && all_fields_map[section_fields[k].value]) {
						alert('Field "'+ temp_field_name +'" in Section "'+ temp_section_name +'" is mandatory.');
						return false;
					} else if (pattern_ids_map[section_fields[k].value] != '' && trim(temp_element.value) != '') {
					    var reg = regExp[pattern_ids_map[section_fields[k].value]];
					    if (reg != undefined) {
					        var regex = reg.substring(1,reg.length-1);
					        var regexp = new RegExp(regex);
					        if (!regexp.test(temp_element.value)){
					              alert('Not Matched Regular Expression for the Field:'+' ' + temp_field_name);
					              return false;
				            }
				        }
					}
				} else if (fields_type_map[section_fields[k].value] == 'date') {
					var temp_element = document.getElementsByName(sub_sections[j].value + "_date_" + section_fields[k].value)[0];
					is_section_empty = (temp_element.value == '') ? is_section_empty : false;
					if (temp_element.value == '' && all_fields_map[section_fields[k].value]) {
						alert('Field "'+ temp_field_name +'" in Section "'+ temp_section_name +'" is mandatory.');
						return false;
					} else {
						if(!doValidateDateField(temp_element))
							return false;
					}
				} else if (fields_type_map[section_fields[k].value] == 'datetime') {
					var temp_element_date = document.getElementsByName(sub_sections[j].value + "_date_" + section_fields[k].value)[0];
					var temp_element_time = document.getElementsByName(sub_sections[j].value + "_time_" + section_fields[k].value)[0];
					is_section_empty = (temp_element_date.value == '' || temp_element_time.value == '') ? is_section_empty : false;
					if ((temp_element_date.value == '' || temp_element_time.value.value == '') &&
							all_fields_map[section_fields[k].value]) {
						alert('Field "'+ temp_field_name +'" in Section "'+ temp_section_name +'" is mandatory.');
						return false;
					} else {
						var valid = true;
						valid = valid && doValidateDateField(temp_element_date);
						valid = valid && validateTime(temp_element_time);
						if (!valid) return false;
					}
				} else if (fields_type_map[section_fields[k].value] == 'checkbox') {
					var chk_elmts =  document.getElementsByName(sub_sections[j].value + "_field_" + section_fields[k].value);
					var chk_elmts_l = chk_elmts.length;
					var value_selected = false;
					for (var l = 0; l < chk_elmts_l; l++) {
						if (chk_elmts[l].checked) {
							value_selected = true;
							break;
						}
					}
					is_section_empty = (!value_selected) ? is_section_empty : false;
					if (!value_selected && all_fields_map[section_fields[k].value]) {
						alert('Field "'+ temp_field_name +'" in Section "'+ temp_section_name +'" is mandatory.');
						return false;
					} else {
					    for (var l = 0; l < chk_elmts_l; l++) {
							if (chk_elmts[l].checked && chk_elmts[l].value != '-1' && chk_elmts[l].checked && chk_elmts[l].value != '0') {
							   var optionValue = document.getElementsByName(sub_sections[j].value + "_option_remarks_" +
									   section_fields[k].value + "_" + chk_elmts[l].value)[0].value;
							   var op_pattern_id = document.getElementsByName("pattern_id_"+ section_fields[k].value +
									   "_" + chk_elmts[l].value)[0].value;
							   var option_name = document.getElementById(sub_sections[j].value + "_option_value_" +
									   section_fields[k].value + "_" + chk_elmts[l].value).innerHTML;
							   if(op_pattern_id != null && op_pattern_id != ''){
							      var reg = regExp[op_pattern_id];
							      if(reg != undefined){
							          var regex = reg.substring(1,reg.length-1);
							          var regexp = new RegExp(regex);
							          if(optionValue != '' && !regexp.test(optionValue)){
							              alert('Not Matched Regular Expression for the Field:'+' '+temp_field_name+' with option '+option_name);
							              return false;
							          }
							      }
							   }
							}
						}
					 }
				} else if (fields_type_map[section_fields[k].value] == 'dropdown') {
					var temp_element = document.getElementsByName(sub_sections[j].value + "_field_" + section_fields[k].value)[0];
					is_section_empty = (temp_element.value == '') ? is_section_empty : false;
					if (temp_element.value == '' && all_fields_map[section_fields[k].value]) {
						alert('Field "'+ temp_field_name +'" in Section "'+ temp_section_name +'" is mandatory.');
						return false;
					}
				}
			}
			if (is_section_empty && is_section_mandatory && trim(sub_sections[j].value) != '') {
				alert("Section '"+ temp_section_name +"' required atleast one value to be entered.");
				return false;
			}
		}
	}
	return true;
}

function allNormal(sectionId, sectionDetailId) {

	var sectionFields = document.getElementsByName("field_ids_" + sectionId);
	var sectionFields_l = sectionFields.length;
	for (var i = 0; i < sectionFields_l; i++) {
		var temp_field_type = document.getElementsByName("field_type_" + sectionFields[i].value)[0].value;
		if (temp_field_type == 'checkbox') {
			var allowNormal = document.getElementById("allowNormal" + sectionDetailId + "_" + sectionFields[i].value).value;
			if (allowNormal == 'Y') {
				var boxes = document.getElementsByName(sectionDetailId + "_field_" + sectionFields[i].value);
				if (boxes[0].checked)
					continue;		// already at normal
				var anyChecked = false;
				for (var j=0; j<boxes.length; j++) {
					if (boxes[j].checked) {
						anyChecked = true;
						break;
					}
				}
				if (!anyChecked) {
					// set Normal as checked
					boxes[0].checked = true;
					onClickNormal(sectionFields[i].value, sectionDetailId);
					// update the fieldText to reflect the same
					updateChecboxFieldText(sectionFields[i].value, sectionDetailId);
				}
			}
		}
	}
}

var finalizeAllInstaSections = function () {
	var finalizedElements = document.getElementsByClassName("finalize");
	var finlalizeAll = document.getElementById("finalizeAll");
	var finalizeAllChecked = finalizeAll.checked;
	if (finalizeAllChecked) {
		var confirmation = confirm("Once the form is finalized, It cannot be undone\nunless user has undo section finalization action right.\nDo you want to proceed?");
		if (!confirmation) {
			finalizeAll.checked = false;
			return false;
		}

	}
	for (var i = 0; i < finalizedElements.length; i++) {
		if (!finalizedElements[i].disabled) {
			finalizedElements[i].checked = finalizeAllChecked;
			var id = finalizedElements[i].id;
			if (finalizeAllChecked) {
				document.getElementsByName(id)[0].value = "Y";
			}
			else {
				document.getElementsByName(id)[0].value = "N";
			}
		}
	}
	return true;
};

function isPatientHistoryMandatory() {
	var temp = document.getElementsByName("is_patient_history_mandatory");
	var temp_l = temp.length;
	if(temp_l > 0) {
		for(var i = 0; i < temp_l; i++) {
			if(temp[i].value == "true") {
				var patient_history_label = document.getElementById("patient_history_label");
				patient_history_label.innerHTML = patient_history_label.innerHTML + '<span class="star"> *</span>';
				break;
			}
		}
	}
}

function changeFinalized(element,sectionId) {
	if (element.checked) {
		document.getElementsByName(sectionId + "_finalized")[0].value = "Y";
	}
	else {
		document.getElementsByName(sectionId + "_finalized")[0].value = "N";
	}
}

YAHOO.util.Event.onContentReady("content", disableFinalizeAll);
function disableFinalizeAll() {
	var finalizeCheckboxes = document.getElementsByClassName("finalize");
	var status=0;
	for (var i=0;i<finalizeCheckboxes.length;i++) {
		if (!finalizeCheckboxes[i].disabled) {
			status=1;
		}
	}
	if (status==0) {
		var finlalizeAll = document.getElementById("finalizeAll");
		if (finlalizeAll) {
		    finalizeAll.disabled=true;
		}
	}
}

