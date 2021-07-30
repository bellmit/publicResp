var avlFlds = null;
var selFlds = null;

function swapOptions(obj, i, j) {
	var o = obj.options;
	var i_selected = o[i].selected;
	var j_selected = o[j].selected;
	var temp = new Option(o[i].text, o[i].value, o[i].title, o[i].defaultSelected, o[i].selected);
	temp.setAttribute("title", o[i].title);
	var temp2 = new Option(o[j].text, o[j].value, o[j].title, o[j].defaultSelected, o[j].selected);
	temp2.setAttribute("title", o[j].title);

	o[i] = temp2;
	o[j] = temp;
	o[i].selected = j_selected;
	o[j].selected = i_selected;
}

function moveOptionUp(obj) {
	if (!hasOptions(obj)) {
		return;
	}
	for (i = 0; i < obj.options.length; i++) {
		if (obj.options[i].selected) {
			if (i != 0 && !obj.options[i - 1].selected) {
				swapOptions(obj, i, i - 1);
				obj.options[i - 1].selected = true;
			}
		}
	}
}

function moveOptionDown(obj) {
	if (!hasOptions(obj)) {
		return;
	}
	for (i = obj.options.length - 1; i >= 0; i--) {
		if (obj.options[i].selected) {
			if (i != (obj.options.length - 1) && !obj.options[i + 1].selected) {
				swapOptions(obj, i, i + 1);
				obj.options[i + 1].selected = true;
			}
		}
	}
}

function sortSelect(obj) {
	var o = new Array();
	if (!hasOptions(obj)) {
		return;
	}
	for (var i = 0; i < obj.options.length; i++) {
		o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
		(o[i]).title = obj.options[i].title;
		(o[i]).value = obj.options[i].value;

	}
	if (o.length == 0) {
		return;
	}
	o = o.sort(function(val1, val2) {
		if ((val1.text + "") < (val2.text + "")) {
			return - 1;
		}
		if ((val1.text + "") > (val2.text + "")) {
			return 1;
		}
		return 0;
	});

	for (var i = 0; i < o.length; i++) {
		obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
		obj.options[i].title = o[i].title;
		obj.options[i].value = o[i].value;
	}
}

function hasOptions(obj) {
	if (obj != null && obj.options != null) {
		return true;
	}
	return false;
}

function moveSelectedOptions(from, to, sort) {
	if (!hasOptions(from)) {
		return;
	}
	for (var i = 0; i < from.options.length; i++) {
		var o = from.options[i];
		if (o.selected) {
			if (!hasOptions(to)) {
				var index = 0;
			} else {
				var index = to.options.length;
			}
			to.options[index] = new Option(o.text, o.value, o.title, false, false);
			to.options[index].setAttribute("title", o.title);
		}
	}
	// Delete the selected options from  the available list.
	for (var i = (from.options.length - 1); i >= 0; i--) {
		var o = from.options[i];
		if (o.selected) {
			from.options[i] = null;
		}
	}
	//********If needed, the fields in the list can be sorted after addition or deletion.******
	if (from.id == avlbl_options) {
		sortSelect(from);
	} else if(to.id == avlbl_options) {
		sortSelect(to);
	}
	//*** below code is used in Consultation Components screen only
	if(document.getElementById('group_patient_sections') != null && document.getElementById('group_patient_sections').checked) {
		groupPatientSections(to);
	}
	from.selectedIndex = -1;
	to.selectedIndex = -1;
}

function createListElements(from, to) {
	avlFlds = document.getElementById(from);
	selFlds = document.getElementById(to);
}

function addListFields() {
	createListElements(avlbl_options, selected_options);
	moveSelectedOptions(avlFlds, selFlds, 'from');
}

function removeListFields() {
	createListElements(avlbl_options, selected_options);
	moveSelectedOptions(selFlds, avlFlds);
}

function groupPatientSections(to) {
	var groupPatientSections = document.getElementById('group_patient_sections');
	if(groupPatientSections.checked) {
		document.getElementById('group_patient_sections').value = 'Y';
		var pArray = new Array();
		var vArray = new Array();

		if(!hasOptions(to)) {
			return;
		}
		var plen = 0;
		var vlen = 0;
		for (var i = 0; i < to.options.length; i++) {
			var patient_form = false;
			for (var j=0; j< selSection.length; j++) {
				if ((to.options[i].value == selSection[j].section_id) && (selSection[j].linked_to == 'patient')) {
					patient_form = true;
					break;
				}
			}
			if (patient_form) {
				pArray[plen] = new Option(to.options[i].text, to.options[i].value, to.options[i].defaultSelected, to.options[i].selected);
				(pArray[plen]).title = to.options[i].title;
				(pArray[plen]).value = to.options[i].value;
				plen++;
			} else {
				vArray[vlen] = new Option(to.options[i].text, to.options[i].value, to.options[i].defaultSelected, to.options[i].selected);
				(vArray[vlen]).title = to.options[i].title;
				(vArray[vlen]).value = to.options[i].value;
				vlen++;
			}
		}

		var sArray = new Array();
		var slen = 0;
		for (var i = 0; i < pArray.length; i++) {
			sArray[slen] = new Option(pArray[i].text, pArray[i].defaultSelected, pArray[i].selected);
			(sArray[slen]).title = pArray[i].title;
			(sArray[slen]).value =pArray[i].value;
			slen++;
		}
		for (var i = 0; i < vArray.length; i++) {
			sArray[slen] = new Option(vArray[i].text, vArray[i].defaultSelected, vArray[i].selected);
			(sArray[slen]).title = vArray[i].title;
			(sArray[slen]).value =vArray[i].value;
			slen++;
		}
		for(var i=0; i< sArray.length; i++) {
			to.options[i] = new Option(sArray[i].text, sArray[i].defaultSelected, sArray[i].selected);
			to.options[i].title = sArray[i].title;
			to.options[i].value = sArray[i].value;
		}
	} else {
		document.getElementById('group_patient_sections').value = 'N';
	}
}

