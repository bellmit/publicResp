/*
 * Referred by pages/master/PatientReport/*.jsp
 */


function initSearch() {
	var toolbar = {
		Edit :	{ title: "Edit", imageSrc: "icons/Edit.png",  href: 'master/PatientReport.do?_method=show'}
	}
	createToolbar(toolbar);
}

function addHeadingRow() {
	var table = document.getElementById("fieldsTable");
	var numFields = table.rows.length;

	if (numFields != 1)
		removeBottomBorder(numFields-2);

	var newRow = table.insertRow(-1);

	var cell1 = newRow.insertCell(-1);
	var caption = makeTextInput("caption", "", "first");
	cell1.appendChild(caption);

	var cell2 = newRow.insertCell(-1);
	var displayOrder = makeTextInput("displayorder", "", "validate-number ");
	cell2.appendChild(displayOrder);

	var cell3 = newRow.insertCell(-1);
	var noOfLines = makeTextInput("no_of_lines", "", "validate-number");
	cell3.appendChild(noOfLines);

	var cell4 = newRow.insertCell(-1);
	var txtEl = makeTextInput("default_text", "", "");
	txtEl.setAttribute('style', 'width: 400px;');
	cell4.appendChild(txtEl);

	var cell5 = newRow.insertCell(-1);
	cell5.setAttribute('class', 'last');

	var imgAnchor = document.createElement('a');
	imgAnchor.setAttribute('href', 'javascript:void(0)');
	imgAnchor.setAttribute('onclick', 'changeElsColor("deleted", '+numFields+')');

	var img = document.createElement('img');
	img.setAttribute('src', cpath + '/icons/Delete.png');
	img.setAttribute('class', 'imgDelete');
	imgAnchor.appendChild(img);
	cell5.appendChild(imgAnchor);

	var delHidden = makeHidden("deleted", "deleted"+numFields, "N");
	cell5.appendChild(delHidden);

	var idHidden = makeHidden("field_id", null, "_");
	cell5.appendChild(idHidden);

	caption.focus();
}

function removeBottomBorder(index) {
	addClassName(document.getElementsByName("caption")[index], 'previousEl');
	addClassName(document.getElementsByName("displayorder")[index], 'previousEl');
	addClassName(document.getElementsByName("no_of_lines")[index], 'previousEl');
	addClassName(document.getElementsByName("default_text")[index], 'previousEl');
}

function changeElsColor(id, index) {
	var markedForDelete = document.getElementById(id + index).value =
		document.getElementById(id + index).value == 'N' ? 'Y' : 'N';
	if (markedForDelete == 'Y') {
		addClassName(document.getElementsByName("caption")[index-1], 'delete');
		addClassName(document.getElementsByName("displayorder")[index-1], 'delete');
		addClassName(document.getElementsByName("no_of_lines")[index-1], 'delete');
		addClassName(document.getElementsByName("default_text")[index-1], 'delete');
	} else {
		removeClassName(document.getElementsByName("caption")[index-1], 'delete');
		removeClassName(document.getElementsByName("displayorder")[index-1], 'delete');
		removeClassName(document.getElementsByName("no_of_lines")[index-1], 'delete');
		removeClassName(document.getElementsByName("default_text")[index-1], 'delete');
	}

}

function checkForDeleteRows() {
	var deleted = document.getElementsByName('deleted');
	for (var i=0; i<deleted.length; i++) {
		if (deleted[i].value == 'N') {
			return true;
		}
	}
	alert("All rows cannot be deleted, atleast one row should be present.");
	return false;
}

function submitValues(format){
	if(format == 'F'){
		if(!checkForDeleteRows())
			return false;
	}
	document.forms[0].submit();
}
