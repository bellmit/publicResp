/*
 * This script file contains a bunch of functions and utilities very commonly
 * used across the entire application.
 */

// our namespace.
Insta = function() {};

/*
 * Deprecated: use prototype String.trim() instead
 */
function trim(inputString) {

   if (typeof inputString != "string") { return inputString; }
   var retValue = inputString;
   var ch = retValue.substring(0, 1);
   while (ch == " ") {
      retValue = retValue.substring(1, retValue.length);
      ch = retValue.substring(0, 1);
   }
   ch = retValue.substring(retValue.length-1, retValue.length);
   while (ch == " ") {
      retValue = retValue.substring(0, retValue.length-1);
      ch = retValue.substring(retValue.length-1, retValue.length);
   }
   while (retValue.indexOf("  ") != -1) { // Note that there are two spaces in the string - look for multiple spaces within the string
      retValue = retValue.substring(0, retValue.indexOf("  ")) + retValue.substring(retValue.indexOf("  ")+1, retValue.length); // Again, there are two spaces in each of the strings
   }
   return retValue;
}

/*
 *function to trim all the spaces
 */
function removeSpaces(string)
	{
		return string.split(' ').join('');
	}

/*
 * Add a trim function to the usual string functions. Henceforth,
 * you can use trim like myString = myString.trim();
 * Note also that this function does NOT mess with double-spaces in the middle
 * of the string, (which can be quite a pain) as in the trim(String) function above.
 * (Thanks to http://blog.stevenlevithan.com/archives/faster-trim-javascript)
 */
String.prototype.trim = function() {
	return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}

String.prototype.endsWith = function(str) {
	return (this.match(str+"$")==str)
}

function getMonth(mon){
	var mm;
	switch(mon){
		case 'JAN': mm=1; break;
		case 'FEB': mm=2; break;
		case 'MAR': mm=3; break;
		case 'APR': mm=4; break;
		case 'MAY': mm=5; break;
		case 'JUN': mm=6; break;
		case 'JUL': mm=7; break;
		case 'AUG': mm=8; break;
		case 'SEP': mm=9; break;
		case 'OCT': mm=10; break;
		case 'NOV': mm=11; break;
		case 'DEC': mm=12; break;

	}
	return mm;
}

/************************************
 *    VALIDATION related utilities  *
 ************************************/

/*
 * isInteger: accepts only positive integers, no +/- sign allowed
 */
function isInteger(text) {
    // in reality, integers can contain +/-, but the more common usage
    // is for only positive numbers, or 0. A function such as isWholeNumber
    // which is technically correct is rather unusable, so we approximate
    // Integer to mean whole numbers (>0) and SignedInteger to mean the real Integer.
    var str = text.toString();
    var re = /^\d*$/;
	return re.test(text);
}

/*
 * isSignedInteger: accepts +/- as the first character, and only digits after that
 */
function isSignedInteger(text) {
    var str = text.toString();
    var re = /^[+-]?\d*$/;
    return re.test(text);
}

/*
 * isDecimal: accepts one decimal point, but only positive numbers
 */
function isDecimal(text, numDecimals) {
    var str = text.toString();
	var re;
	if (numDecimals == null) {
    	re = /^\d*\.?\d*$/;
	} else {
    	re = new RegExp('^\\d*\\.?\\d{0,' + numDecimals + '}$');
	}
    return re.test(text);
}

/*
 * isSignedDecimal: accepts +/- and one decimal point
 */
function isSignedDecimal(text) {
    var str = text.toString();
    var re = /^[+-]?\d*\.?\d*$/;
    return re.test(text);
}

/*
 * Amount is defined as numeric(15,2), or (16,3) so this allows not more than 8/7
 * digits before the decimal sign, and not more than 2/3 digits after. Allows negative.
 */
function isAmount(text) {
    var str = text.toString();
	var re = (decDigits == 3) ? /^\d{0,13}(\.\d{1,3})?$/ : /^\d{0,13}(\.\d{1,2})?$/;
    return re.test(text);
}

function isSignedAmount(text) {
    var str = text.toString();
	var re = (decDigits == 3) ? /^[+-]?\d{0,13}(\.\d{1,3})?$/ : /^[+-]?\d{0,13}(\.\d{1,2})?$/;
    return re.test(text);
}

/*
 * isTime: accepts HH:MM (no seconds allowed)
 */
function isTime(text) {
    var str = text.toString();
    var re = /^\d\d:\d\d$/;
	if (re.test(text)) {
		// now validate the hours and minutes
	 	var values = text.split(":");
		if (values[0] > 23) return false;
		if (values[1] > 59) return false;
		return true;
	} else {
		return false;
	}
}

/*
 * Convenience function to validate that a field is not empty,
 * and also alert and focus to that field if it is empty.
 * Returns false if the field is empty.
 */
function validateRequired(field, msg) {
	if(field.disabled)
		return true;//validation not required for disabled fields
	if (field == null) {
		alert(msg + getString("js.common.field.is.null.string"));
	}
	var userVal = field.value;
	if (userVal==null || userVal.trim() == "") {
		if (msg) {
			alert(msg);
		} else if (field.getAttribute('title')) {
			alert(field.getAttribute('title') + " "+getString("js.common.is.required"));
		} else if (field.getAttribute('displayName')) {
			alert(field.getAttribute('displayName') + " " +getString("js.common.is.required"));
		} else {
			showMessage("js.common.value.required");
		}
		field.focus();
		return false;
	}
	return true;
}

/*
 * Validate that both the fields are empty or both the fields are non-empty
 */
function validatePair(obj1, obj2, msg) {
	if ((obj1.value=="") && (obj2.value !="")) {
		alert(msg);
		obj1.focus();
		return false;
	}
	if ((obj2.value=="") && (obj1.value !="")) {
		alert(msg);
		obj2.focus();
		return false;
	}
	return true;
}

/*
 * Validate that the given field is a decimal number. Null is assumed
 * to be valid, so you have to include validateRequired in addition to
 * this function if the value is required in addition.
 */
function validateDecimal(field, msg, numDecimals) {
	if (field == null) {
		alert(msg + "(field is null)");
	}
	var userVal = field.value;
	if ( userVal==null || userVal=="") {
		return true;
	}
	if (!isDecimal(userVal, numDecimals)) {
		if (msg != null) alert(msg);
		field.focus();
		return false;
	}
	return true;
}

/*
 * Validate that the given field is a number. Null is assumed
 * to be valid, so you have to include validateRequired in addition to
 * this function if the value is required in addition.
 */
function validateInteger(field, msg) {
	if (field == null) {
		alert(msg + " (field is null)");
	}
	var userVal = field.value;
	if ( userVal==null || userVal=="") {
		return true;
	}
	if (!isInteger(userVal)) {
		if (msg != null) {
			alert(msg);
		} else if (field.getAttribute("displayName")) {
			alert(field.getAttribute("displayName") + " must be an integer");
		} else {
			alert("Invalid integer");
		}
		field.focus();
		return false;
	}
	return true;
}

function validateIntMinMax(field, min, max, msg) {
	if (!validateInteger(field, msg))
		return false;

	var value = parseInt(field.value,10);
	if ((value < min) || (value > max)) {
		if (msg != null) alert(msg + " (min/max check failed: " + value + ")");
		field.focus();
		return false;
	}
	return true;
}

/*
 * Validate that the given field is a number, and also that it is not 0. Null is assumed
 * to be valid, so you have to include validateRequired in addition to
 * this function if the value is required in addition.
 */
function validateNonZeroInteger(field, msg) {
	if (field == null) {
		alert(msg + " (field is null)");
	}
	var userVal = field.value;
	if ( userVal==null || userVal=="") {
		return true;
	}
	if (!isInteger(userVal) || (userVal==0) ) {
		if (msg != null) alert(msg);
		field.focus();
		return false;
	}
	return true;
}

function validatePositiveInteger(field, msg) {
	if (field == null) {
		alert(msg + " (field is null)");
	}
	var userVal = field.value;
	if ( userVal==null || userVal=="" ) {
		if (msg != null) alert(msg);
		field.value=0;
		return false;
	}
	if (!isInteger(userVal)) {
		if (msg != null) alert(msg);
		if(userVal<0)
			field.value=field.value*-1;
		else
			field.value=0;
		field.focus();
		return false;
	}
	return true;
}

/*
 * Validate that the given field is a numeric(10,2). Null is assumed
 * to be valid, so you have to include validateRequired in addition to
 * this function if the value is mandatory.
 */
function validateAmount(field, msg) {
	if (field == null) {
		alert(msg + " (field is null)");
	}
	var userVal = field.value;
	if ( userVal==null || userVal=="") {
		return true;
	}
	if (!isAmount(userVal)) {
		if (msg != null) {
			alert(msg);
		} else if (field.getAttribute("displayName")) {
			alert(field.getAttribute("displayName") + " must be a number");
		} else {
			alert("Invalid number");
		}
		field.focus();
		return false;
	}
	return true;
}

function validateSignedAmount(field, msg) {
	if (field == null) {
		alert(msg + " (field is null)");
	}
	var userVal = field.value;
	if ( userVal==null || userVal=="") {
		return true;
	}
	if (!isSignedAmount(userVal)) {
		if (msg != null) alert(msg);
		else alert("Invalid Amount");
		field.focus();
		return false;
	}
	return true;
}

function validateLength(field, maxLen, label, msg) {
	if (field == null) {
		alert(msg + " (field is null)");
	}
	if (field.value.length > maxLen) {
		if (msg) alert(msg);
		else alert(label + "Length of text should not exceed " + maxLen + ".");
		field.focus();
		return false;
	}
	return true;
}

/****************************************************
 * LIST manipulation functions                      *
 *                                                  *
 * The following functions operate on a list        *
 * of objects, which is of the form:                *
 * [ {key1: 'value1', key2: 'value2' ...} ...]      *
 *                                                  *
 ****************************************************/

/*
 * Filter out a list based on varName = varValue in the objects in the list.
 * Returns a new Array.
 */
function filterList(list, varName, varValue) {
	if (list == null) return null;
	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		if (list[i][varName] == varValue) {
			filteredList.push(list[i]);
		}
	}
	return filteredList;
}

function filterZeroItemsInList(list, varName) {
	if (list == null) return null;
	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		if (list[i][varName] > 0) {
			filteredList.push(list[i]);
		}
	}
	return filteredList;
}

function filterList2(list, varName1, varValue1, varName2, varValue2) {
	if (list == null) return null;
	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		if (list[i][varName1] == varValue1 && list[i][varName2] == varValue2) {
			filteredList.push(list[i]);
		}
	}
	return filteredList;
}

function filterListWithValues(list, varName, varValues) {
	if (list == null) return null;
	if (varValues == null) return list;

	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		if (varValues.indexOf(list[i][varName]) != -1) {
			filteredList.push(list[i]);
		}
	}
	return filteredList;
}

/**
 * Filter list with exluding specic value
 *
 * @param {*} list single dimension array
 * @param {*} varName String
 * @param {*} varValue String
 *
 * @returns an array
 */
function filterListWithExcludingValues(list, varName, varValue) {
	if (list == null) return null;
	if (varValue == null) return list;

	var filteredList = new Array();
	for (var i=0; i<list.length; i++) {
		if (list[i][varName] != varValue) {
			filteredList.push(list[i]);
		}
	}
	return filteredList;
}
/*
 * Find an object based on varName = varValue in a list of objects.
 * Returns the found object, if any, or null.
 */
function findInList(list, varName, varValue) {
	if (list == null) return null;
	for (var i=0, len = list.length ; i<len; i++) {
		if (list[i][varName] == varValue) {
			return list[i];
		}
	}
	return null;
}

function findInList2(list, varName1, varValue1, varName2, varValue2) {
	if (list == null) return null;
	for (var i=0; i<list.length; i++) {
		var listItem = list[i];
		if ( (listItem[varName1] == varValue1) && (listItem[varName2] == varValue2) ) {
			return listItem;
		}
	}
	return null;
}

function findInList3(list, varName1, varValue1, varName2, varValue2, varName3, varValue3) {
	if (list == null) return null;
	for (var i=0; i<list.length; i++) {
		var listItem = list[i];
		if ( (listItem[varName1] == varValue1) && (listItem[varName2] == varValue2)
			&& (listItem[varName3] == varValue3)) {
			return listItem;
		}
	}
	return null;
}

/*****************************************
 * FORM FIELD related utilities          *
 *****************************************/

/*
 * Set the selected index in a dropdown based on the value supplied,
 * this will calculated the index by going through available values in the dropdown.
 */
function setSelectedIndex(opt, set_value) {
	var index=0;
	if (opt.options) {
		for(var i=0; i<opt.options.length; i++) {
			var opt_value = opt.options[i].value;
			if (opt_value == set_value) {
				opt.selectedIndex = i;
				return;
			}
		}
	}
}

/*
 * same as above supporting multiple selection...
 *
 */

function setMultipleSelectedIndexs(opt, value) {
	var index=0;
	for(var i=0; i<opt.options.length; i++) {
		var opt_value = opt.options[i].value;
		for (var j in value) {
			if (opt_value == value[j]) {
				opt.options[index].selected = true;
			}
		}
		index++;
	}
}

/*
 * Set the selected index in a dropdown based on the text (display value) supplied,
 * this will calculated the index by going through available values in the dropdown.
 */
function setSelectedIndexText(opt, set_text) {
	var index=0;
	for(var i=0; i<opt.options.length; i++) {
		var opt_text = opt.options[i].text;
		if (opt_text == set_text) {
			opt.selectedIndex = i;
			return;
		}
	}
}

/*
 * Get the text of a select box, given the id of the select box
 * The text is what is displayed to the user, the value is what
 * you can get directly from the select box's .value property.
 */
function getSelTextFromId(selName) {
	var sel = document.getElementById(selName);
	return getSelText(sel);
}

function getSelText(sel) {
	var index = sel.selectedIndex;
	if (index >=0) {
		return sel.options[index].text;
	}
}

function getSelValue(sel) {
	var index = sel.selectedIndex;
	if (index >=0) {
		return sel.options[index].value;
	}
}

/*
 * Get the value of a radio group. Example:
 * <input type="radio" name="myRadio" value="b">Blue
 * <input type="radio" name="myRadio" value="g" checked>Green
 *
 * getRadioSelection(document.forms[0].myRadio) will return 'g'
 */
function getRadioSelection(formEl) {
	var len = formEl.length;
	if (len == undefined) {
		if (formEl.checked)
			return formEl.value;
	}
	for (var i=0; i<len; i++) {
		if (formEl[i].checked) {
			return formEl[i].value;
		}
	}
	return null;
}

/*
 * Convenience function that takes a list of items to be added to a
 * select box. An optional "title" can be added which is the first element
 * in the select box, surrounded by .. and ..
 */
function loadSelectBox(selectBox, itemList, dispNameVar, valueVar, title, titleValue) {
	if (itemList == null) {
		selectBox.length = 0;
		selectBox.disabled = true;
		return;
	}

	// clear the select box
	selectBox.length = 0;
	selectBox.disabled = false;
	selectBox.selectedIndex = -1;

	var index = 0;

	// set the title of the select box, and the length of options
	if (title != null) {
		selectBox.length = itemList.length + 1;
		if (titleValue == null)
			titleValue = "";
		selectBox.options[index] = new Option(title, titleValue);
		index++;
	} else {
		selectBox.length = itemList.length;
	}

	// add items from the itemList to the select box
	for (var i=0; i<itemList.length; i++) {
		var item = itemList[i];
		if (dispNameVar == null) {
			selectBox.options[index] = new Option(item, item);
		} else {
			selectBox.options[index] = new Option(item[dispNameVar], item[valueVar]);
		}
		index++;
	}
}

function clearForm(form) {
	if (form) {
		for (var i=0; i<form.elements.length; i++) {
			if (form.elements[i].nodeName == 'FIELDSET')
				continue;
			var type = form.elements[i].type;
			if (type)
				type = type.toLowerCase();
			switch (type) {
				case "text":
				case "password":
				case "file":
				case "textarea":
					form.elements[i].value = "";
					break;
				case "radio":
				case "checkbox":
					if (form.elements[i].getAttribute("isallcheckbox") == 1) {
						form.elements[i].checked = true;
						form.elements[i].onclick();
					} else {
						form.elements[i].checked = false;
					}
					break;
				case "select-one":
					form.elements[i].selectedIndex = 0;
					break;
				case "select-multiple":
					form.elements[i].selectedIndex = -1;
					break;
				case "button":
				case "submit":
				case "reset":
				case "hidden":
					break;
				default:
					alert("Unknown input type: " + type + " for " + form.elements[i]);
					break;
			}
		}
	}
}

function clearFormHidden(form) {
	if (form) {
		for (var i=0; i<form.elements.length; i++) {
			if (form.elements[i].nodeName == 'FIELDSET')
				continue;
			var type = form.elements[i].type;
			if (type)
				type = type.toLowerCase();
			if (type == 'hidden') {
				form.elements[i].value = "";
			}
		}
	}
}

function clearFormAll(form) {
	clearForm(form);
	clearFormHidden(form);
}

/*
 * Copy one row from a grid row to a dialog form
 * The names of the form elements must be the same in both.
 */
function hiddenToForm(row, form) {
	for (var i=0; i<form.elements.length; i++) {
		var el = form.elements[i];
		var type = el.type;
		var name = el.name;
		if (!type || !name) continue;

		var hiddenObj = getElementByName(row, el.name);
		if (!hiddenObj)
			continue;
		type = type.toLowerCase();

		switch (type) {
			case "hidden":
			case "text":
			case "password":
			case "textarea":
				el.value = hiddenObj.value;
				break;
			case "select-one":
				setSelectedIndex(el, hiddenObj.value);
				break;
			case "radio":
			case "checkbox":
				// todo
				break;
			default:
				break;
		}
	}
}

/*
 * Reverse of the above
 */
function formToHidden(form, row) {
	for (var i=0; i<form.elements.length; i++) {
		var el = form.elements[i];
		var type = el.type;
		var name = el.name;
		if (!type || !name) continue;

		var hiddenObj = getElementByName(row, el.name);
		if (!hiddenObj)
			continue;
		type = type.toLowerCase();

		switch (type) {
			case "hidden":
			case "text":
			case "password":
			case "textarea":
			case "select-one":
				hiddenObj.value = el.value;
				break;
			case "radio":
			case "checkbox":
				// todo
				break;
			default:
				break;
		}
	}
}

/*
 * For all the given column indexes (in the form of a map which maps the name to index)
 * copy the hidden values in the row and show them as a label in the appropriate column.
 */
function rowHiddenToLabels(row, colIndexes) {

	for (var fieldName in colIndexes) {

		var index = colIndexes[fieldName];
		if (index < 0 || index >= row.cells.length)
			continue;

		var hiddenObj = getElementByName(row, fieldName);
		if (!hiddenObj)
			continue;

		setNodeText(row.cells[index], hiddenObj.value);
	}
}

function rowHiddenToLabelIds(row, prefix, colIndexes) {

	for (var fieldName in colIndexes) {

		var index = colIndexes[fieldName];
		if (index < 0 || index >= row.cells.length)
			continue;

		var hiddenObj = getElementByName(row, fieldName);
		if (!hiddenObj)
			continue;

		setNodeText(row.cells[index], hiddenObj.value);
	}
}

/*
 * Copy an "object" to a form based on the same names of the object and fields
 * The index is optional, if passed in uses the indexed form element (eg, hidden
 * fields in a row), otherwise it is assumed to be a non-array form (eg dialog)
 */
function objectToForm(obj, form) {
	// iterate through all the object attributes
	for (var fname in obj) {
		var el = form[fname];
		if (el == null) continue;	// corresponding field not present in the form

		var type = el.type;
		var name = el.name;

		if (!type || !name) continue;

		switch (type) {
			case "select-one":
				setSelectedIndex(el, obj[fname]);
				break;
			case "radio":
			case "checkbox":
			case "button":
				break;
			default:
				el.value = obj[fname];
				break;
		}
	}
}

/*
 * Reverse of above
 */
function formToObject(form, obj) {
	// iterate through all the object attributes
	for (var fname in obj) {
		var el = form[fname];
		if (el == null) continue;	// corresponding field not present in the form

		var type = el.type;
		var name = el.name;

		if (!type || !name) continue;

		switch (type) {
			default:
				obj[fname] = el.value;
				break;
		}
	}
}

/*
 * Sets the form hidden variables as labels based on IDs
 * Eg, form has hidden qty_available and there is a label lbl_qty_available
 * then qty_available is copied to the label.
 */
function formToLabelIds(form, prefix, maxlen) {
	for (var i=0; i<form.elements.length; i++) {
		var el = form.elements[i];
		var type = el.type;
		var name = el.name;
		if (!type || !name) continue;

		var lbl = document.getElementById(prefix + name);
		if (lbl) {
			setNodeText(lbl, el.value, maxlen, el.value);
		}
	}
}


/*
 * Copies the object attributes to hidden input form elements based on
 * matching names of the attribute and the hidden input.
 */
function objectToHidden(obj, row) {
	// iterate through all the object attributes
	for (var fname in obj) {
		var hiddenObj = getElementByName(row, fname);
		if (!hiddenObj)
			continue;
		hiddenObj.value = obj[fname];
	}
}

function objectToRowLabelIds(obj, row, prefix) {
	// iterate through all the object attributes
	for (var fname in obj) {
		var hiddenObj = getElementByAttribute(row, prefix + fname, "id");
		if (!hiddenObj)
			continue;
		setNodeText(hiddenObj, obj[fname]);
	}
}

function objectToRowLabels(obj, row, colIndexes) {
	for (var fieldName in colIndexes) {
		var index = colIndexes[fieldName];
		if (index < 0 || index >= row.cells.length)
			continue;

		setNodeText(row.cells[index], obj[fieldName]);
	}
}

function shallowCopy(from, to, names) {
	if (names != undefined) {
		for (var i in names) {
			var name = names[i];
			to[name] = from[name];
		}
	} else {
		for (var fname in from) {
			to[fname] = from[fname];
		}
	}
}

/*
 * Add to an existing select box, without clearing the existing
 * contents
 */
function addToSelectBox(selectBox, itemList, dispNameVar, valueVar) {

	var index = selectBox.options.length;
	selectBox.options.length += itemList.length;

	// add items from the itemList to the select box
	for (var i=0; i<itemList.length; i++) {
		var item = itemList[i];
		selectBox.options[index].text = item[dispNameVar];
		selectBox.options[index].value = item[valueVar];
		index++;
	}
}

function insertIntoSelectBox(selectBox, before, text, value) {
	var opt = document.createElement('option');
	opt.text = text; opt.value = value;
	try {
		selectBox.add(opt,selectBox.options[before]);
	} catch (ex) {
		selectBox.add(opt, before);		// IE
	}
}

function clearMultiSelectSelections(selectBox) {
	for (var i=0; i<selectBox.options.length; i++) {
		selectBox.options[i].selected = false;
	}
}

/*
 * Sets the node's text to the given text. If a label node is found, sets it inside
 * that, otherwise sets the entire cell's content to the text. If title is also to be
 * added, adds it to the label. If length is non-zero, then truncates the text to that
 * many characters, and forcibly adds a title (if supplied, that title, else the text itself).
 */
function setNodeText(node, text, len, title) {

	if (YAHOO.lang.isString(node))
		node = document.getElementById(node);

	if (node == null)
		return;

	if (text == null) text = "";

	var displayText = text;
	var truncated = false;

	if (len != null && len > 0) {
		if (text.length > len) {
			displayText = text.substring(0, len-2) + '...';
			truncated = true;
		}
	}

	var labelNodes = node.getElementsByTagName("label");
	if (labelNodes && labelNodes[0]) {
		node = labelNodes[0];
	}
	var divNodes = node.getElementsByTagName("div");
	if (divNodes &&  divNodes[0])
		node = divNodes[0];

	node.textContent = displayText;

	if (title) {
		node.setAttribute("title", title);
	} else if (truncated) {
		node.setAttribute("title", text);
	} else {
		node.setAttribute("title", '');
	}
}

/*
 * this common function truncates a given text to provided length.
 */

function truncateText(text,len) {
	if (text == null) text = "";

	var displayText = text;

	if (len != null && len > 0) {
		if (text.length > len) {
			displayText = text.substring(0, len-2) + '...';
		}
	}

	return displayText;
}

function setElementText(elId, text, len, title) {
	var node = document.getElementById(elId);
	setNodeText(node, text, len, title);
}

/* complete the MRNO: assumes you have included MrnoPrefix.jsp */
function onKeyPressMrno(e, mrnoBox) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno(mrnoBox);
	} else {
		return true;
	}
}

function onChangeMrno(mrnoBox) {
	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		mrnoBox.value = "";
		setTimeout("mrnoBox.focus()",0);
		return false;
	}
	return true;
}

/********************************
 * MISCELLANEOUS utilities      *
 ********************************/

/*
 * Display a string inside a variety of consoles, if available.
 * Useful if an alert message will interfere with the screen flow.
 */
function debug(str, severity) {

	// The following requires firebug and firebug console to be open/enabled. This is the most
	// preferred method since you can persist messages across screens if you enable "persist"
	// in the firebug console.
	if (typeof(console) !== 'undefined' && console != null)
		console.log("Insta DEBUG: " + str);

	// The following requires new YAHOO.widget.LogReader(); to have been executed, and also
	// requires that your body be of class "yui-skin-sam"
	if (YAHOO)
		YAHOO.log("Insta: " + str, severity);

	// The following requires that you have your own div in your page for writing debug msgs.
    var div = document.getElementById("debug");
	if (div != null)
    	div.innerHTML = div.innerHTML + str + "<br>";
}

/*
 * author : krishna
 * this method is used to check for the oldmrno entered is
 * unique or not.
 */

function checkForUniqueNo(oldmrno){
	var ajaxReq = newXMLHttpRequest();
	var url = "editregistration.do?method=checkForUniqueOldMrno";
	ajaxReq.onreadystatechange = function () {
		if(ajaxReq.readyState == 4 && ajaxReq.status == 200){
			if(ajaxReq.responseText!=null){
				if (ajaxReq.responseText == "not exists") {
					//Entered OldMrno is unique
				} else {
					alert("OldMrNo. " +oldmrno.value+ " already exists");
					oldmrno.value = "";
					oldmrno.focus();
					return false;
				}
			}
		}
	}
	var parameters = "oldmrno="+oldmrno.value;
	ajaxReq.open("POST",url.toString(), true);
	// modified :krishna: BUG#:2891: fixed
	ajaxReq.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	ajaxReq.setRequestHeader("Content-length", parameters.length);
	ajaxReq.send(parameters);

}

/*
 * Function to complete the prefix of any ID, used for Mrno completion etc.
 * Since multiple formats can be valid, eg, if the format has changed after
 * initial deployment, does not attempt to validate the given string.
 */
function addPrefix(textBox, prefix, numDigits) {
	var userVal = textBox.value.trim();

	if (userVal == "") {
		textBox.value = userVal;
		return true;
	}

	if (isInteger(userVal) && (userVal.length <= numDigits)) {
		var numZeros = numDigits - userVal.length;
		var zeros = "";
		for (var i=0; i<numZeros; i++) {
			zeros += "0";
		}
		var newVal = prefix + zeros + userVal;
		textBox.value = newVal;
		return true;

	} else {
		// Not a number, or longer than the num digits, don't try and complete it
		return true;
	}
}


function isEventEnterOrTab(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 || charCode==9 ) {
		return true;
	}
	return false;
}

function makeTextInput(name, id, cls, size, value) {
	var el = document.createElement('input');
	el.type = 'text';
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id!="")
		el.id = id;
	if (cls!=null && cls!="")
		el.setAttribute("class",cls);
	if (size!=null && size!="")
		el.size = size;
	if (value != null && value != "")
		el.value = value;
	return el;
}

function makeHidden(name, id, value) {
	var el = document.createElement('input');
	el.type = 'hidden';
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	else
		el.value = "";
	return el;
}

function makeButton(name, id, value){
	var el = document.createElement("input");
	el.type = 'button';
	if (name!=null && name!="")
		el.name= name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
}

function makeCheckbox(name, id, cls, checked) {
	var el = document.createElement('input');
	el.type = 'checkbox';
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id!="")
		el.id = id;
	if (cls!=null && cls!="")
		el.setAttribute("class",cls);
	if (checked!=null)
		el.checked = checked;
	return el;
}

function makeSelect(name, id, cls) {
	var el = document.createElement('select');
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id!="")
		el.id = id;
	if (cls!=null && cls!="")
		el.setAttribute("class",cls);
	return el;
}

function makeTextArea(name, id, cls, rows, cols) {
	var el = document.createElement('textarea');
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id != "")
		el.id = id;
	if (cls!=null && cls!="")
		el.className = cls;
	if (rows!=null && rows!="")
		el.rows = rows;
	if (cols!=null && cols!="")
		el.cols = cols;
	return el;
}

function makeImageButton(name, id, cls, src){
	var el = document.createElement('img');
	if (name !=null && name!="")
		el.name = name;
	if (id != null && id != "")
		el.id=id;
	if (cls != null && cls != "")
		el.className = cls;
	if (src!=null && src!="")
		el.src = src;
	return el;
}

function makeImageHref(id, src, onclick, title, cls){
	var elA = document.createElement('a');
	elA.setAttribute("onclick", onclick);
	elA.setAttribute("title", title);

	var elImg = document.createElement('img');
	if (id != null && id != "")
		elImg.id=id;
	if (cls != null && cls != "")
		elImg.className = cls;
	if (src!=null && src!="")
		elImg.src = src;
	elA.appendChild(elImg);
	return elA;
}

function makeLabel(id, value){
	var el = document.createElement('label');
	if( id != null && id != "")
		el.id = id;
	if (value != null && value != "")
		el.textContent = value;
	return el;
}

function makeTextCell(row, text, cls) {
	var td = row.insertCell(-1);
	var text = document.createTextNode(text);
	td.appendChild(text);
	if (cls != null)
		td.setAttribute("class", cls);
	return td;
}

function findAncestor(node, type) {
	while (node != null) {
		if (node.nodeName == type)
			break;
		node = node.parentNode;
	}
	return node;
}

/*
 * Gets the indexed form element, when there are more than one element with the same
 * name in a form (where elements are dynamically added). The form.element can give an
 * array or a single element depending on whether there are many, so this convenience function.
 * Note that el.length is not convenient to check if it is an array, since selects return
 * length (of options) even though they are not arrays.
 */
function getIndexedFormElement(form, elName, index) {
	var el = form[elName];

	if (el == null)
		return null;

	if (el.nodeName) // it is an object, not an array
		return el;

	if (el.length)
		return el[index];

	return el;
}

/*
 * Gets the number of elements of the given name in a form. The element can be single
 * or an array. Eg, a single occurrance of <input type="text" name="a">, we can address
 * the element as form.a (form.al.length or form.a[0] will given an error). Whereas, if there
 * is more than one occurence, we need to deal with an array.
 */
function getFormElementLength(form, elName) {
	var el = form[elName];

	if (el == null)
		return null;

	if (el.nodeName) // it is an object, not an array
		return 1;

	if (el.length)
		return el.length;

	return 0;
}

/*
 * Gets the number of checked elements, similar to above functions, works both on a single
 * as well as multiple items.
 */
function getNumChecked(form, elName) {
	var el = form[elName];
	var numChecked = 0;

	if (el == null)
		return null;

	if (el.nodeName) { // it is an object, not an array
		if (el.checked) numChecked++;
	} else {
		for (var i=0; i<el.length; i++) {
			if (el[i].checked) numChecked++;
		}
	}
	return numChecked;
}


/*
 * Finds a specific form element by name, by searching under a given node.
 */
function getElementsByName(startNode, elName) {
    var elements = startNode.all ? startNode.all : startNode.getElementsByTagName('*');
    var returnElements = new Array();

    for(var i=0; i<elements.length; i++) {
        var el = elements[i];
		if (el.getAttribute("name") == elName) {
            returnElements.push(el);
        }
    }
    return returnElements;
}

/*
 * Finds the first form element below the startNode with the given element name
 * Returns null if not found.
 */
function getElementByName(startNode, elName) {
	var obj = YAHOO.util.Dom.getElementBy(function(el) {
				return (el.getAttribute('name') == elName)
			}, null, startNode);
	// YUI returns an empty array if no elements match.
	if (YAHOO.lang.isArray(obj))
		return null;
	return obj;
}

function getElementByAttribute(startNode, elName, attribName) {
	var obj = YAHOO.util.Dom.getElementBy(function(el) {
				return (el.getAttribute(attribName) == elName)
			}, null, startNode);
	// YUI returns an empty array if no elements match.
	if (YAHOO.lang.isArray(obj))
		return null;
	return obj;
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getThisTable(node) {
	return findAncestor(node, "TABLE");
}

function getThisCell(node) {
	return findAncestor(node, "TD");
}

/*
 * Enable a set of checkboxes, based on the value of one "all" checkbox.
 * This is typically used in a search filter, for the onclick of the "all" checkbox.
 * (See also <insta:checkgroup>)
 */
function enableCheckGroupAll(obj) {
	var form = obj.form;
	var checkBoxes = form[obj.name];
	var disable = checkBoxes[0].checked;

	for (var i=1; i<checkBoxes.length; i++) {
		checkBoxes[i].disabled = disable;
	}
}

/*
 * Use roundNumber instead of toFixed, since toFixed will convert to string,
 * which we don't want in many cases where we use the number to calculate
 */
function roundNumber(num,decimals) {
	return Math.round(num*Math.pow(10,decimals))/Math.pow(10,decimals);
}

/*
 * The most common solutions for rounding to a decimal place is to either use Number.prototype.toFixed(), or multiply the float by some power of 10 in order to leverage Math.round(). Both of these work, except sometimes a decimal of 5 is rounded down instead of up.
	Number((1.005).toFixed(2)); // 1 instead of 1.01
	Math.round(1.005*100)/100; // 1 instead of 1.01

	The rounding problem can be avoided by using numbers represented in exponential notation:
	Number(Math.round(1.005+'e2')+'e-2'); // 1.01
	So use below round function to get correct answer after roundoff.

 */
function round(value, decimals) {
	return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}

function round2(num) {
	return Math.round(num*100)/100;
}

function round3(num) {
	return Math.round(num*1000)/1000;
}

function roundAmt(num) {
	if (decDigits == 2)
		return round2(num);
	else if (decDigits == 3)
		return round3(num);
	return roundNumber(num, decDigits);
}

/*
* checks for atleast one checkbox to be checked.
* returns true if atleast one checkbox is checked.
* if none of them checked, returns false stopping event(if event is not null and undefined).
*/
function checkBoxesChecked(elName, event, msg) {
	var elements = document.getElementsByName(elName);
	var disabledCount = 0;
	for (var element in elements) {
		if (elements[element].disabled) {
			elements[element].checked = false;
			disabledCount++;
		}
		if (elements[element].checked)
				return true;
	}
	if (elements.length == disabledCount) {
		alert("None of the items are applicable for delete");
		return false
	}
	if (msg == null || msg == undefined || msg == '')
		alert("Please select atleast one item for delete.");
	else
		alert(msg);

	if (event == null || event == undefined) {
		return false;
	} else {
		YAHOO.util.Event.stopEvent(event);
		return false;
	}
}

function checkOrUncheckAll(elName, obj) {
	var checkBox = document.getElementsByName(elName);
	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled) {
			checkBox[i].checked = obj.checked;
			if (document.getElementById('toolbarRow'+i)) {
				if (checkBox[i].checked) {
					document.getElementById('toolbarRow'+i).classList.add("rowbgToolBar");
				} else {
					document.getElementById('toolbarRow'+i).classList.remove("rowbgToolBar");
				}
			}
		}
	}
}

function gotoLocation(url) {
	window.location.href = url;
}

function ajaxForPrintUrls(){
	var url = cpath+'/pages/common/getPrintUrls.do';
	YAHOO.util.Connect.asyncRequest('POST', url,
					{ 	success: popUpPrints,
					}
	);
}

function popUpPrints(response) {
	if (response.responseText != undefined) {
		var jsonExpression = "(" + response.responseText + ")";
		var list = eval(jsonExpression);
		if (list != null) {
			for (var i=0; i<list.length; i++) {
				window.open(list[i]);
			}
		}
	}
}

/* Function for validating discounts in masters*/
function validateDiscount(amtelmt,discelmt,i) {
	var amtObj = document.getElementById(amtelmt+i);
	var discObj = document.getElementById(discelmt+i);

	if (amtObj.value == "") { amtObj.value = 0; }
	if (discObj.value == "") { discObj.value = 0; }

	if(!validateAmount(amtObj)) {
		removeClassName(amtObj,'validation-passed');
		addClassName(amtObj,'validation-failed');
		amtObj.value = 0;
		return false;
	}
	if(!validateAmount(discObj)) {
		removeClassName(discObj,'validation-passed');
		addClassName(discObj,'validation-failed');
		discObj.value = 0;
		return false;
	}

	if(getPaise(formatAmountValue(discObj.value)) > getPaise(formatAmountValue(amtObj.value))) {
		alert("Discount is more than amount");
		discObj.value = 0;
		discObj.focus();
		removeClassName(amtObj,'validation-passed');
		addClassName(amtObj,'validation-failed');
		removeClassName(discObj,'validation-passed');
		addClassName(discObj,'validation-failed');
		return false;
	} else {
		removeClassName(amtObj,'validation-failed');
		addClassName(amtObj,'validation-passed');
		removeClassName(discObj,'validation-failed');
		addClassName(discObj,'validation-passed');
		return true;
	}
}

/*
* changes the row color
* @id hidden parameter to change the status between 'Y' and 'N'
* @index parameter index.
*/
function markForDelete(id, index) {
	var markedForDelete = document.getElementById(id + index).value =
		document.getElementById(id + index).value == 'Y' ? 'N' : 'Y';
	if (markedForDelete == 'Y')
		addClassName('tr'+index, 'delete');
	else
		removeClassName('tr'+index, 'delete');
}

/*
* Function to attach a transaction token (hidden input field) to a form. The token is added
* only if the form method is post and the variable token is set in the page.
*/

function loadTransactionTokens(tokenKey, tokenValue) {
	var pageForms = document.getElementsByTagName("form");
	if (null != pageForms) {
		for (var i = 0; i < pageForms.length; i++) {
			var theForm = pageForms[i];
			if (null != tokenValue && tokenValue != "") {
				if (formRequiresToken(theForm)) {
					var hiddenToken = makeHidden(tokenKey, tokenKey, tokenValue);
					theForm.appendChild(hiddenToken);
				}
				if (isMultipartForm(theForm)) {
					var action = theForm.action;
					if (!empty(action)) {
						var modifiedAction = appendQueryParam(action, tokenKey, tokenValue);
						theForm.action = modifiedAction;
					}
				}
			}
		}
	}
}


function isMultipartForm(theForm) {
	if (null == theForm) return false;
	if (null == theForm.enctype) return false;
	var encoding = theForm.getAttribute("enctype");
	if (empty(encoding)) return false;
	if (encoding.toLowerCase() == "multipart/form-data") return true;
	return false;
}

function appendQueryParam(url, key, value) {
	if (empty(url)) return url;
	if (empty(key) || empty(value)) return url;

	var urlModified = url;
	var paramString = key + "=" + value;

	// If it ends with ? just append the key=value
	var len = url.length;
	var lastChar = url.substr(len-1, 1);
	if (lastChar == '?') {
		urlModified = urlModified + paramString;
		return urlModified;
	}

	var pos = url.indexOf('?');
	if (pos == -1) {
		urlModified = urlModified + "?" + paramString;
	} else if (lastChar != '&') {
		urlModified = urlModified + '&' + paramString;
	} else {
		urlModified = urlModified + paramString;
	}

	return urlModified;

}

function formRequiresToken(thePageForm) {
	if (null == thePageForm) return false;
	if (null == thePageForm.method) return false;
	var formMethod = thePageForm.getAttribute("method");
	if (empty(formMethod)) return false;
	if (formMethod.toUpperCase() != "POST") return false;
	return true;
}


/**
*	Script for YUI Tooltip Initialization.
*
*/
Insta.Tooltip = {
	ttipObj: null,
	contextIds: []
};

function initTooltip(tableId, extraDetails) {

	var table = document.getElementById(tableId);
	var len = table.rows.length;

	for (i=1; i<len; i++) {
		var row = table.rows[i];

		if (extraDetails[row.id]) {
			var columnsData = extraDetails[row.id];

			var dataAvailable = false;
			for ( var key in columnsData ) {
				if ( columnsData[key] != null && columnsData[key] != '' )
					dataAvailable = true;
			}

			if ( dataAvailable )
				Insta.Tooltip.contextIds.push(row.id);
		}
	}
	createTooltip();
	var switcher = document.getElementById('toolTipSwitch');
	if (switcher != null)
		YAHOO.util.Event.addListener(switcher, 'click', toggleTooltip);
	var ttDisabled = YAHOO.util.Cookie.exists("disableTooltip");
	enableTooltip(!ttDisabled);
}

function createTooltip() {
	 Insta.Tooltip.ttipObj = new YAHOO.widget.Tooltip("tooltip",
				{ 	context: Insta.Tooltip.contextIds,
					hidedelay: 0,
					showdelay: 1000,
					autodismissdelay: 10000
				} );
	Insta.Tooltip.ttipObj.contextTriggerEvent.subscribe(setTooltipContent);
}

function setTooltipContent(type, args) {
	var rowId = args[0].id;
	var dataRow = document.getElementById(rowId);
	var columnsData = extraDetails[rowId];

	var text = '<table class="tooltipTable">';
	for (var key in columnsData) {
		text += '<tr><td class="label">' + key + ':</td>' + '<td class="info">' + columnsData[key] + '</td></tr>'
	}
	text += '</table>';
	this.cfg.setProperty("text", text);
}

function toggleTooltip() {
	var ttDisabled = YAHOO.util.Cookie.exists("disableTooltip");
	enableTooltip(ttDisabled);
}

function enableTooltip(enable) {
	Insta.Tooltip.ttipObj.cfg.setProperty('disabled', !enable);
	var switcher = document.getElementById('toolTipSwitch');
	if (switcher != null)
	switcher.className = enable ? 'fltL tooltipactive' : 'fltL tooltipinactive';
	if (enable) {
		YAHOO.util.Cookie.remove("disableTooltip");
	} else {
		var expDate = new Date();
		expDate.setDate(expDate.getDate() + 100);
		YAHOO.util.Cookie.set("disableTooltip", "true", {expires: expDate});
	}
}

/**
*	End of YUI Tooltip Script.
*/

/** Patient Details Tag related functions
*/

var isPlanDetlDialgInitzld = false;
var isPhotoDialgInitzld = false;
var isSecPlanDetlDialgInitzld = false;

var patientPlanDetailsDialog;
function initPatientPlanDetailsDialog() {
	if(!isPlanDetlDialgInitzld) {
	    patientPlanDetailsDialog = new YAHOO.widget.Dialog('patientPlanDetailsDialog', {
	    	context:["pd_planButton","tr","br", ["beforeShow", "windowResize"]],
	        width:"525px",
	        visible: false,
	        modal: true,
	        constraintoviewport: true,
			close :false,
	    });

	    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                             { fn:handlePatientPlanDetailsDialogCancel,
	                                               scope:patientPlanDetailsDialog,
	                                               correctScope:true } );
		patientPlanDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
		isPlanDetlDialgInitzld = true;
	    patientPlanDetailsDialog.render();
   }
}

function handlePatientPlanDetailsDialogCancel(){
	 document.getElementById('patientPlanDetailsDialog').style.display='none';
	 document.getElementById('patientPlanDetailsDialog').style.visibility='hidden';
	 patientPlanDetailsDialog.cancel();
}


function showPatientPlanDetailsDialog(){
	document.getElementById('patientPlanDetailsDialog').style.display='block';
	document.getElementById('patientPlanDetailsDialog').style.visibility='visible';
	patientPlanDetailsDialog.show();
}

var patientSecPlanDetailsDialog;
function initPatientSecPlanDetailsDialog() {
	if(!isSecPlanDetlDialgInitzld) {
	    patientSecPlanDetailsDialog = new YAHOO.widget.Dialog('patientSecPlanDetailsDialog', {
	    	context:["pd_planButton","tr","br", ["beforeShow", "windowResize"]],
	        width:"525px",
	        visible: false,
	        modal: true,
	        constraintoviewport: true,
			close :false,
	    });

	    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                             { fn:handlePatientSecPlanDetailsDialogCancel,
	                                               scope:patientSecPlanDetailsDialog,
	                                               correctScope:true } );
		patientSecPlanDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
		isSecPlanDetlDialgInitzld = true;
	    patientSecPlanDetailsDialog.render();
   }
}

function handlePatientSecPlanDetailsDialogCancel(){
	 document.getElementById('patientSecPlanDetailsDialog').style.display='none';
	 document.getElementById('patientSecPlanDetailsDialog').style.visibility='hidden';
	 patientSecPlanDetailsDialog.cancel();
}


function showPatientSecPlanDetailsDialog(){
	document.getElementById('patientSecPlanDetailsDialog').style.display='block';
	document.getElementById('patientSecPlanDetailsDialog').style.visibility='visible';
	patientSecPlanDetailsDialog.show();
}


var patientPhotoDialog;
function initPatientPhotoDialog() {
	if(!isPhotoDialgInitzld) {
    patientPhotoDialog = new YAHOO.widget.Dialog('patientPhotoDialog', {
        width:"525px",
        visible: false,
        modal: true,
        constraintoviewport: true,
		close:false,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePatientPhotoDialogCancel,
                                               scope:patientPhotoDialog,
                                               correctScope:true } );
	patientPhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	isPhotoDialgInitzld = true;
    patientPhotoDialog.render();
  }
}

function handlePatientPhotoDialogCancel(){
	 document.getElementById('patientPhotoDialog').style.display='none';
	 document.getElementById('patientPhotoDialog').style.visibility='hidden';
	 patientPhotoDialog.cancel();
}

function showPatientPhotoDialog(){
	var imgElement = document.getElementById('pd_patientImage');
	if(imgElement!= null) {
		resize(imgElement);
		patientPhotoDialog.cfg.setProperty("width",imgElement.width+130);
	} else {
		patientPhotoDialog.cfg.setProperty("width",500);
	}
	var button = document.getElementById('pd_viewPhotoIcon');
	patientPhotoDialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById('patientPhotoDialog').style.display='block';
	document.getElementById('patientPhotoDialog').style.visibility='visible';
	patientPhotoDialog.show();
}

function resizeCustom(imgEle, maxWidth, maxHeight){
    if(imgEle == null)
    	return null;
	var ratio = 0;
    var width = imgEle.width;
    var height = imgEle.height;

    // Check if the current width is larger than the max
    if(width > maxWidth){
        ratio = maxWidth / width;
        imgEle.setAttribute("width",maxWidth); // Set new width
        imgEle.setAttribute("height",height * ratio);// Scale height based on ratio
        height = height * ratio;    // Reset height & width to match scaled image
        width = width * ratio;
    }

    // Check if current height is larger than max
    if(height > maxHeight){
        ratio = maxHeight / height;
        imgEle.setAttribute("height",maxHeight);
        imgEle.setAttribute("width",width * ratio);
        width = width * ratio;
        height = height * ratio;
    }
}

function resize(imgEle) {
	var maxWidth = 500; // Max width for the image
    var maxHeight = 500;    // Max height for the image
    resizeCustom(imgEle, maxWidth, maxHeight);
}

// Structure to hold the localized javascript strings used on the page

Insta.i18n = {
	bundle : {}
};

// Function that will put the key, value pair supplied into
// the resource bundle
function addResourceBundle(prefix, bundle) {
	if (!empty(bundle)) {
		for (var resource in bundle) {
			Insta.i18n.bundle[resource] = bundle[resource];
		}
	}
}

// Function that fetches the value from the resource bundle
// Usage getString(key, a0, a1, ....)

// key 			: is the resource key for the the string in application.properties
// a0, a1, ... 	: are the arguments which will be substituted for {0}, {1}, ...

// respectively

function getString(key) {
	// Fetch the value from the bundle, if the key exists
	var s = Insta.i18n.bundle[key];
	// If the key isn't there, return what struts returns in such cases.
	if( !s ) return (  "???" + key + "???"  );
	// Replace any {n} place holders with the arguments passed to the function
	for (var i = 0; i < arguments.length; i++) {
		s = s.replace("{" + i + "}", arguments[i]);
	}
	return s;
}

// Convenience function to display a alert box to the user.
function showMessage(messageKey) {
	if (!empty(messageKey)) {
		alert(getString(messageKey));
	}
}

function getToolbarBundle(pagePrefix) {
	var prefix = pagePrefix;
	var toolbarBundle = {};
	if (empty(pagePrefix) || pagePrefix.length == 0 ) {
		// Copy the entire bundle over.
		for (var key in Insta.i18n.bundle) {
			toolbarBundle[key] = Insta.i18n.bundle[key];
		}
	} else {
		if (pagePrefix.charAt(pagePrefix.length -1) != '.') {
			// append a . at the end if not already specified in the pattern
			prefix = prefix + ".";
		}
		for (var key in Insta.i18n.bundle) {
			if (key.substring(0, prefix.length) == prefix) {
				var toolbarKey = key.substring(prefix.length);
				var values = Insta.i18n.bundle[key].split(',');
				if (!empty(values)) {
					var obj = new Object();
					obj.name = values[0];
					obj.description = (values.length > 1) ? values[1] : '';
					toolbarBundle[toolbarKey] = obj;
				}
			}
		}
	}
	return toolbarBundle;
}

function getCornerSpec(ltrCorner) {
	if (gPageDirection == 'ltr') {
		return ltrCorner;
	} else {
		if (ltrCorner == 'tl') return 'tr';
		if (ltrCorner == 'tr') return 'tl';
		if (ltrCorner == 'br') return 'bl';
		if (ltrCorner == 'bl') return 'br';
	}
}

function empty(obj) {
	if (obj == null || obj == '' || obj == 'undefined') return true;
	else return false;
}

function displayElement(el, display) {
	if (display == null)
		display = true;
	// usage of 'block' is not good for trs and tds. Setting it to '' makes it display
	// in the default manner that is appropriate. For divs, this is block, and for tds it is table-cell.
	if (display)
		el.style.display = '';
	else
		el.style.display = 'none';
}

function displayElementId(elId, display) {
	var el = document.getElementById(elId);
	if (el == null)
		alert("Invalid element ID: " + elId);
	else
		displayElement(el, display);
}

function sortDropDown(obj, defaultText) {
	var objArr = new Array();
	if (!empty(obj)) {
		objArr = new Array();
		var objValue = obj.value;
		var i = 0;
    	for (var n=0; n<obj.options.length; n++) {
    		if (!empty(obj.options[n].value)) {
      			objArr[i] = new Array(obj.options[n].text, {text: obj.options[n].text, value: obj.options[n].value});
      			i++;
      		}
    	}
    	objArr.sort();

		var len = 1;
		var optn = new Option("-- Select --", "");
		if (!empty(defaultText))
			optn = new Option("(All)", "");

		obj.options.length = len;
		obj.options[len - 1] = optn;

		if (objArr.length > 0) {
	    	for (var n=0; n<objArr.length; n++) {
	    		var optn = new Option(objArr[n][1].text, objArr[n][1].value);
				len++;
				obj.options.length = len;
				obj.options[len - 1] = optn;
	    	}
		}
    	setSelectedIndex(obj, objValue);
    }
}


function removePreviousSpans(parent) {
	var children= parent.childNodes;
	var spanChildren = new Array();
	if(children!= null){
		var j=0;
		for(var i=0; i<children.length; i++) {
			if(children[i].tagName == "SPAN"){
				spanChildren[j++] = children[i];
			}
		}
	}

	if(spanChildren != null) {
		for(var k=0; k<spanChildren.length; k++){
			parent.removeChild(spanChildren[k]);
		}
	}
}

function appendStarSpan(parent){
	var star = document.createElement("span");
	star.setAttribute("class", "star");
	star.setAttribute("style", "padding:1px 0px 1px 0px;text-align:center;float:right");
	star.innerHTML = " * ";
	removePreviousSpans(parent);
	parent.appendChild(star);
}


function isValidNumber(inputEl, allowDecimal, fieldLabel) {
	fieldLabel = fieldLabel ? fieldLabel : "Quantity";
	if (allowDecimal == 'Y')
		return validateDecimal(inputEl, fieldLabel+" must be a decimal number, allowed only "+decDigits+" decimals", decDigits);
	else
		return validateInteger(inputEl, fieldLabel+" must be a whole number");
}

function isValidNumberPBM(inputEl, allowDecimal, fieldLabel,noOfDecDigits) {
  fieldLabel = fieldLabel ? fieldLabel : "Quantity";
  if (allowDecimal == 'Y')
    return validateDecimal(inputEl, fieldLabel+" must be a decimal number, allowed only "+noOfDecDigits+" decimals", noOfDecDigits);
  else
    return validateInteger(inputEl, fieldLabel+" must be a whole number");
}

function formElValueModified(form) {
	if (form) {
		for (var i=0; i<form.elements.length; i++) {
			if (form.elements[i].nodeName == 'FIELDSET')
				continue;
			var type = form.elements[i].type;
			if (type)
				type = type.toLowerCase();
			switch (type) {
				case "text":
				case "password":
				case "textarea":
					if (form.elements[i].defaultValue != form.elements[i].value)
						return true
					break;
				case "radio":
				case "checkbox":
					if (form.elements[i].checked != form.elements[i].defaultChecked)
						return true;
					break;
				case "select-one":
				case "select-multiple":
					var	el = form.elements[i], c = false, def = 0, o, ol, opt;
					for (o = 0, ol = el.options.length; o < ol; o++) {
						opt = el.options[o];
						c = c || (opt.selected != opt.defaultSelected);
						if (opt.defaultSelected) def = o;
					}
					if (c && !el.multiple) c = (def != el.selectedIndex);
					if (c) return true;
					break;
				case "button":
				case "submit":
				case "reset":
					break;
				case "hidden":
					if (form.elements[i].defaultValue != form.elements[i].value)
						return true
					break;
				default:
					break;
			}
		}
	}
	return false;
}

function selectAllItems(elName, obj) {
	var checkBox = document.getElementsByName(elName);
	for (var i=0; i<checkBox.length; i++) {
		var applicable = document.getElementById("applicable"+i);
		if (!checkBox[i].disabled) {
			checkBox[i].checked = obj.checked;
			if (document.getElementById('toolbarRow'+i)) {
				if (checkBox[i].checked) {
					applicable.value = 'false';
					document.getElementById('toolbarRow'+i).className = "rowbgToolbar";
				} else {
					applicable.value = 'true';
					document.getElementById('toolbarRow'+i).className = "";
				}
			}
		}
	}
}

function validatePhoneNo(number, msg) {
	// number should start with digit or ( or + or - character followed by digit followed by ) or space or -
	// that followed by digit.
	var regExp = new RegExp(/^[\d  \(  \+ -]+[\d]*[ \)  \s  -]*[\d-]*$/);
	if (!regExp.test(number)) {
		if (msg != undefined && msg != '') {
			alert(msg);
		} else {
			alert("Please enter valid Phone No. Ex.080-25844444, (0567)-88888888 or as +917777777777 etc.,");
		}
		return false;
	}
	return true;
}

function asyncPostForm(form, url, useToken, successCallback, failureCallback) {

	// TODO : validate all the parameters and provide good defaults
	// when all parameters not supplied

	var formToken = "";

	// If token should not be used, then clear the token in the form before submission
	if (!useToken) {
		formToken = form._insta_transaction_token.value;
		form._insta_transaction_token.value="";
	}

	YAHOO.util.Connect.setForm(form);

	var callback = {success : successCallback, failure: failureCallback};
	var ajaxRequest = YAHOO.util.Connect.asyncRequest('POST', url, callback);

	// restore the token, if we cleared the token earlier
	if (!useToken) {
		form._insta_transaction_token.value=formToken;
	}

}

function initDataTable(id, deletable, insertable, data) {
	var dataTable = new Insta.DataTable(id, deletable, insertable);
	dataTable.init(data);
	return dataTable;
}

/*
function initDataGrid(id, config, dlgEl, dataList) {
	var dataGrid = new Insta.DataGrid(id, config); //, mode, callbacks,dlgEl);
	dataGrid.registerEditor(dlgEl);
	dataGrid.render();
	dataGrid.init(dataList);
	return dataGrid;
}
*/

function initDialog(dlg, context) {
	var dialog = new YAHOO.widget.Dialog(dlg,
						{
							width:"600px",
							/* context : [context, "tr", "br"], */
							visible:false,
							modal:true,
							constraintoviewport:true
						});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:dialogCancelHandler,
		                                              	scope:dialog,
		                                              	correctScope:true} );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
	dialog.render();
	return dialog;
}

function dialogCancelHandler() {
	this.cancel();
}

// This function checks if the user as shown by the page header is the same as the logged in user in the session
// If not, it invokes the log out url. The user at the time of page load is available in the span element with ID
// pageuser and last logged in user is available in the localstorage variable "lastUser"
function logOffStaleUser () {
	var failureRedirectPath = cpath + "/home.do";
  $.ajax({
  	url: cpath + "/session.json",
  	success: function(data){
  		if (data && data.hasOwnProperty("userId") && data.userId !== userid) {
  			document.location.href = failureRedirectPath;
  		}
  	},
  	error: function(jqXHR){
		if (jqXHR.status == 402 || jqXHR.status == 401 || jqXHR.status == 403) {
			document.location.href = failureRedirectPath;
		}
  	}
  });
}

function setUserSchema(userSchema) {
	if (typeof(Storage) === "undefined") {
		return;
	}
	localStorage.setItem("user_schema"+"@"+cpath, userSchema);
}
