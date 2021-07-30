function validateComplaint() {
	if (complaintForm.complaint) {
		var complaint = complaintForm.complaint.value;
		if (complaint != '' && complaint.length < 2) {
			showMessage("js.outpatient.consultation.mgmt.complaint.minimum2chars");
			complaintForm.complaint.focus();
			return false;
		}
	}
	var s_complaint_els = document.getElementsByName("s_complaint");
	for (var i=0; i<s_complaint_els.length; i++) {
		if (s_complaint_els[i].value != '' && s_complaint_els[i].value.length < 2) {
			showMessage("js.outpatient.consultation.mgmt.complaint.minimum2chars");
			s_complaint_els[i].focus();
			return false;
		}
	}

	return true;
}

function complaintEntered() {
	if (complaintForm.complaint) {
		var complaint = complaintForm.complaint.value;
		if (complaint != '') {
			return true;
		}
	}
	var s_complaint_els = document.getElementsByName("s_complaint");
	for (var i=0; i<s_complaint_els.length; i++) {
		if (s_complaint_els[i].value != '') {
			return true;
		}
	}

	return false;
}

function addSecondaryComplaint() {
	var formName = document.getElementById("formName").value;
	var field_ph_cat_id = document.getElementById("field_ph_cat_id").value;
	var cpath = document.getElementById("cpath").value;

	var table = document.getElementById('complaintsTable');
	var id = table.rows.length -1;

	var row = table.insertRow(-1);
	cell = row.insertCell(-1);
	cell.setAttribute("class", "formlabel");
	cell.innerHTML = 'Other Complaint: ';

	cell = row.insertCell(-1);
	cell.setAttribute('colspan', '3');
	cell.innerHTML = '<input type="hidden" name="s_complaint_row_id" value="_"/>' +
					 '	<input type="text" id="s_complaint_'+id+'" name="s_complaint" style="width: 520px"/>' ;
	if(field_ph_cat_id != '') {
		cell = row.insertCell(-1);
		cell.innerHTML = '<a href="javascript:void(0);"'+
			'onclick="return showPhraseComplaintDialog(this, \''+formName+'\', \'s_complaint_'+id+'\','+field_ph_cat_id+');" '+
			'title="Select Values">'+
					'<img class="button" src="'+cpath+'/icons/openbook.png" />'+
			'</a>';
	}
	cell.setAttribute("style", 'text-align:left;width: 70px');
}


function showPhraseComplaintDialog(obj, formName, textBoxId, categoryId) {

	var phrases = [];
	var table = document.getElementById('phrases_table');
		for (var i=0; i<table.rows.length; ) {
			table.deleteRow(i);
		}
	var clear_field_label = document.getElementById('section_field_clear_anchor');

	clear_field_label.onclick = function (){
		textObj = document.getElementById(textBoxId);
		textObj.value = '';
	}

	var undo_last_label = document.getElementById('section_field_undo_last');

	undo_last_label.onclick = function () {
	textObj = document.getElementById(textBoxId);
	var text = textObj.value;
	var newtext = '';
	var textArray = text.split(",");
		for(var i=0;i<textArray.length-1;i++){
			newtext += textArray[i] + (i == textArray.length-2 ? '' : ",");
		}
	textObj.value = newtext;
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
		cell.innerHTML = '<a title="double click to select" href="javascript:Add Phrase" '+
		'onclick="return false;" ondblclick="return appendPhraseComplaint(this, \''+formName+'\',\''+textBoxId+'\');">' +
		phrases[i].phrase_suggestions_desc + '</a>';
	}
	}
	phraseChildDialog = phraseDialog;
	phraseDialog.cfg.setProperty('context', [obj, 'tr', 'bl'], false);
	phraseDialog.show();

}

function appendPhraseComplaint(label, formName, textBoxId) {

	var phrase = label.textContent;
	textObj = document.getElementById(textBoxId);
	textObj.value += (textObj.value == '' ? '' : ", ") + phrase;

}


