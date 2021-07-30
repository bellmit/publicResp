function addTemplate(e, url) {
	if (e.button == 2 || e.button == 3) {
		YAHOO.util.Event.stopEvent(e);
		alert('sorry, right click is not allowed');
		return false
	}
	var format = document.getElementById('format_for_add').value;
	if (format == '') {
		alert('please select the format');
		document.getElementById('format_for_add').focus();
		return false;
	} else {
		href = url + "&format="+format;
		document.getElementById("add").href = href;
	}
}

function clearSearch(){
	var theForm = document.forms[0];

	theForm.template_name.value = '';

	theForm.filter_format[0].checked = true;
	enableCheckGroupAll(theForm.filter_format[0]);

	theForm.status[0].checked = true;
	enableCheckGroupAll(theForm.status[0]);

	if (theForm.doc_type_name) {
		theForm.doc_type_name[0].checked = true;
		enableCheckGroupAll(theForm.doc_type_name[0]);
	}
}

