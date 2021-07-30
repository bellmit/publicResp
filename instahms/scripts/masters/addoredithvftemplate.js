function addNewRow() {
	var table = document.getElementById('hvfFieldsTable');
	var len = table.rows.length;
	if (document.getElementsByName('field_name')[len-1]
			&& document.getElementsByName('field_name')[len-1].value == '') return;

	var row = table.insertRow(len);
	//row.setAttribute("class", "newRow");
	var cell = null;

	if (len != 1)	removeBottomBorder(len-2);

	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" name="field_name" id="field_name" style="width: 200px" class="first"/>' +
		'<input type="hidden" name="field_id" id="field_id" value=""/>';
	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" name="display_order" id="display_order" class="validate-number " style="width: 100px;" maxlength="4" title="Display Order is mandatory and it should be Number." />';
	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" name="num_lines" id="num_lines" class="validate-number " style="width: 100px;" maxlength="5" title="No. Of Lines is mandatory and it should be Number." />';
	cell = row.insertCell(-1);
	cell.innerHTML = '<input type="text" name="default_value" id="default_value" style="width: 300px;" maxlength="500">';
	cell = row.insertCell(-1);
	cell.innerHTML = '<select name="field_input" id="field_input" class="dropdown"> ' +
						'<option value="D">Default</option> '+
						'<option value="E">External Device</option> ' +
						'<option value="P">Paste Image</option> ' +
						'<option value="B">Browse Image</option> ';
	cell = row.insertCell(-1);
	cell.setAttribute('class', 'border');
	cell.innerHTML = '<input type="checkbox" name="print_column_chk" id="print_column_chk'+len+'" onclick="toggleValue(this, \'print_column'+len+'\')" checked/>' +
					 '<input type="hidden" name="print_column" id="print_column'+len+'" value="Y"/>';
	cell = row.insertCell(-1);
	cell.setAttribute('class', 'last');
	// value attribute of delete checkbox is used to ignore the row insertion into database, if the checkbox is checked.
	cell.innerHTML = '<input type="checkbox" name="field_status_chk" id="field_status_chk'+len+'" onclick="toggleValue(this, \'field_status'+len+'\')" checked/>' +
					 '<input type="hidden" name="field_status" id="field_status'+len+'" value="A"/>' ;
	document.getElementsByName('field_name')[len-1].focus();
}

function removeBottomBorder(index) {
	addClassName(document.getElementsByName('field_name')[index], 'previousEl');
   	addClassName(document.getElementsByName('display_order')[index], 'previousEl');
   	addClassName(document.getElementsByName('num_lines')[index], 'previousEl');
   	addClassName(document.getElementsByName('default_value')[index], 'previousEl');
}

function toggleValue(checkbox, elId) {
	if (checkbox.checked)
		document.getElementById(elId).value = checkbox.name == 'print_column_chk' ? 'Y' : 'A';
	else
		document.getElementById(elId).value = checkbox.name == 'print_column_chk' ? 'N' : 'I';
}


function dashboard() {
	if (document.getElementById('documentType').value == 'mlc')
		window.location.href = contextPath + "/master/MLCTemplate.do?method=list";
	else if (document.getElementById('documentType').value == 'service')
		window.location.href = contextPath + "/master/ServiceTemplate.do?method=list";
	else if (document.getElementById('documentType').value == 'reg')
		window.location.href = contextPath + "/master/RegistrationTemplate.do?method=list";
	else
		window.location.href = contextPath + "/master/GenericDocumentTemplate.do?method=list";
}

function validateForm(){
	var errs = new Array();
	var all_valid = true;
	var field_names = document.getElementsByName("field_name");
	var hDeleteRow = document.getElementsByName("field_status");
	var display_order = document.getElementsByName("display_order");
	var num_lines = document.getElementsByName("num_lines");
	var field_checks = document.getElementsByName("field_status_chk");
	for (var i=0; i<field_names.length; i++) {
		if (field_checks[i].checked)
			hDeleteRow[i].value = 'A';
		else
			hDeleteRow[i].value = 'I';

		if (hDeleteRow[i].value == 'A' && trim(field_names[i].value) == "") {
			errs[errs.length] = field_names[i].getAttribute('title') + ": should not be empty";
			field_names[i].focus();
			all_valid = false;
		}

		if (display_order[i].value == "" || display_order[i].value == 0) {
			errs[errs.length] = display_order[i].getAttribute('title') + ": should not be empty or zero";
			display_order[i].focus();
			all_valid = false;
		}
		if (num_lines[i].value == "" || display_order[i].value == 0) {
			errs[errs.length] = num_lines[i].getAttribute('title') + ": should not be empty or zero";
			num_lines[i].focus();
			all_valid = false;
		}
	}

	if (!all_valid) {
		if (errs.length > 0){
			alert("We have found the following error(s):\n\n  * "+errs.join("\n  * ")+"\n\nPlease check the fields and try again");
		} else {
			alert('Some required values are not correct. Please check the items in red.');
		}
		//YAHOO.util.Event.stopEvent(event);
		return all_valid;
	}
	if (all_valid) {
		var len = document.getElementsByName("field_id").length;
		var hDeleteRow = document.getElementsByName("field_status");
		for (var i=0; i<len; ) {
			var _el = document.getElementsByName("field_id")[i];
			if (hDeleteRow[i].value == 'I' && _el.value == '') {
				document.getElementById('hvfFieldsTable').deleteRow(i+1);
				len = len - 1;
			} else {
				i++;
			}
		}
	}
	return true;
}

function validateFields() {
	if (!validateForm())
		return false;

	document.hvfTemplateForm.submit();
	return true;
}

function changeElsColor(id, index) {
	var markedForDelete = document.getElementById(id + index).value =
		document.getElementById(id + index).value == 'A' ? 'I' : 'A';
	if (markedForDelete == 'I') {
		addClassName(document.forms[0].field_name[index-1], 'delete');
		addClassName(document.forms[0].display_order[index-1], 'delete');
		addClassName(document.forms[0].num_lines[index-1], 'delete');
		addClassName(document.forms[0].default_value[index-1], 'delete');
	} else {
		removeClassName(document.forms[0].field_name[index-1], 'delete');
		removeClassName(document.forms[0].display_order[index-1], 'delete');
		removeClassName(document.forms[0].num_lines[index-1], 'delete');
		removeClassName(document.forms[0].default_value[index-1], 'delete');
	}

}