function renameOrDeleteForms() {
	var forms = document.getElementsByName('renameForm');
	var renameFlag = false;
	for (var i in forms) {
		var form = forms[i];
		if (form.checked) {
			renameFlag = true;
			break;
		}
	}
	var forms = document.getElementsByName('deleteForm');
	var deleteFlag = false;
	for (var i in forms) {
		var form = forms[i];
		if (form.checked) {
			deleteFlag = true;
			break;
		}
	}
	if (renameFlag || deleteFlag){
		document.forms[0].submit();
		return true;
	} else {
		alert("Please rename or delete atleast one consultation form.");
		return false;
	}
}

function hideShowInputBox(chkBox, index) {
	var inputEl = document.getElementsByName('form_name')[index-1];
	var labelEl = document.getElementsByName('cons_form_title')[index-1];
	var renameEl = document.getElementsByName('rename')[index-1];
	var deleteChk = document.getElementsByName('deleteForm')[index-1];
	if (chkBox.checked) {
		renameEl.value = 'Y';
		inputEl.style.display = 'block';
		labelEl.style.display = 'none';
	} else {
		renameEl.value = 'N';
		inputEl.style.display = 'none';
		labelEl.style.display = 'block';
	}
}
function changeRowColor(chkBox, index) {
	var row = document.getElementById('addedFormsTable').rows[parseInt(index)];
	var renameEl = document.getElementsByName('rename')[index-1];
	var renameChk =  document.getElementsByName('renameForm')[index-1];
	var labelEl = document.getElementsByName('cons_form_title')[index-1];
	var inputEl = document.getElementsByName('form_name')[index-1];

	if (chkBox.checked) {
		renameEl.value = 'N';
		renameChk.checked = false;
		addClassName(row, 'deleted');
		labelEl.style.display = 'block';
		inputEl.style.display = 'none';
	} else {
		removeClassName(row, 'deleted');
	}
}
