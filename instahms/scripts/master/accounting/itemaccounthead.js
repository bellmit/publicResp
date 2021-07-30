function loadChargeHeads(groupObj) {
	var group = groupObj.value;
	var chargeHeadSelect = document.getElementById("chargehead_id");
	//resetSelectedValues("loadChargeHeads");
	chargeHeadSelect.length = 1;
	var optIndex = 1;

	// add pseudo heads for BED and ICU
	if (group == "BED") {
		chargeHeadSelect.length +=1;
		chargeHeadSelect[optIndex].value = "MULTIBED";
		chargeHeadSelect[optIndex].text = "(All Bed Charges)";
		optIndex++;

	} else if (group == "ICU") {
		chargeHeadSelect.length +=1;
		chargeHeadSelect[optIndex].value = "MULTIICU";
		chargeHeadSelect[optIndex].text = "(All ICU Charges)";
		optIndex++;

	}

	for (var i=0; i<jChargeHeads.length; i++) {
		var charge = jChargeHeads[i];
		if (charge.chargegroup_id == groupObj.value) {
			chargeHeadSelect.length +=1;
			chargeHeadSelect.options[optIndex].value = charge.chargehead_id;
			chargeHeadSelect.options[optIndex].text = charge.chargehead_name;
			optIndex++;
		}
	}
}

var gItems = {};
function getItems(chargehead, chargegroup, callback, cacheGroup) {
	/*
	 * If we have cached the items, return that
	 */
	 var type = null;
	 if (cacheGroup) type = chargegroup;
	 else type = chargehead;

	 if (gItems[type] != null) return gItems[type];

	var url = "ItemAccountHeads.do?method=getItems&chargehead_id="+chargehead+"&charge_group="+chargegroup;
	var xhr = newXMLHttpRequest();
	getResponseHandlerText(xhr, function(responseData) {saveItems(responseData, type, callback);}, url);
	return null;
}

function saveItems(responseData, type, callback) {
	/*
	 * Save the items to the appropriate variable.
	 * If it fails, we indicate that the ajax call was at least made, so that the next time
	 * it will not be made. Otherwise, this can go into an infinite loop on any failure.
	 */
	var obj = eval(responseData);

	if (obj != null) {
		gItems[type] = obj;
	} else {
		gItems[type] = [];
	}

	/*
	 * Call back the original function that was interrupted for lack of data
	 */
	callback();
}

var jEquipments = null;
var jDoctors= null;
var jOtDoctors= null;
var jAnaestetists = null;
var jBedWards = null;
var jICUWards = null;
var jLTests = null;
var jRTests = null;
var jOperations = null;
var jServices = null;
var jOtherCharges = null;
var jConsumables = null;
var jMedicines = null;
var jTheatres = null;
var jPackages = null;
var jPackOBDetails = null;
var jImplants = null;
var keys = null;
var keysIdentifier = null;

function loadDescriptions() {

	var chargeGroup = document.forms[0].charge_group.value;
	var chargeHead = document.forms[0].chargehead_id.value;
	var descBox  = document.forms[0].item_description;
	if ("REG" == chargeGroup) {
		//loadSelectBox(descBox, null, "", "");

	} else if ("DOC" == chargeGroup) {
		if ( (jDoctors = getItems(chargeHead, chargeGroup, loadDescriptions, true)) == null ) return;
			//loadSelectBox(descBox, jDoctors , "doctor_name", "doctor_id", "Doctor");
			loadItemAutoComplete(descBox, jDoctors, "doctor_name", "doctor_id", "Doctor", "doctors");
			keysIdentifier = "doctors";
	} else if ("LTDIA" == chargeHead) {
		if ( (jLTests = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jLTests , "test_name", "test_id", "Test");
			loadItemAutoComplete(descBox, jLTests, "test_name", "test_id", "Test", "laboratory");
			keysIdentifier = "laboratory";
	} else if ("RTDIA" == chargeHead) {
		if ( (jRTests = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jRTests , "test_name", "test_id", "Test");
			loadItemAutoComplete(descBox, jRTests, "test_name", "test_id", "Test", "radiology");
			keysIdentifier = "radiology";
	} else if ("BED" == chargeGroup) {
		if ( (jDoctors = getItems(chargeHead, chargeGroup, loadDescriptions, true)) == null ) return;
			//loadSelectBox(descBox, bedsInWard , "bed_name", "bed_name", "Bed");
			loadItemAutoComplete(descBox, jRTests, "bed_name", "bed_name", "bed", "bed");
			keysIdentifier = "bed";
	} else if ("ICU" == chargeGroup) {
		if ( (jDoctors = getItems(chargeHead, chargeGroup, loadDescriptions, true)) == null ) return;
			//loadSelectBox(descBox, bedsInWard , "bed_name", "bed_name", "Bed");
			loadItemAutoComplete(descBox, jRTests, "bed_name", "bed_name", "icu", "icu");
			keysIdentifier = "icu";
	} else if ("TCOPE" == chargeHead) {
		if ( (jTheatres = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jTheatres, "theatre_name", "theatre_id", "Theatre");
			loadItemAutoComplete(descBox, jTheatres, "theatre_name", "theatre_id", "Theatre", "theatres");
			keysIdentifier = "theatres";
	} else if ( ("SUOPE" == chargeHead) || ("ASUOPE" == chargeHead) || ("COSOPE" == chargeHead)) {
		if ( (jOtDoctors = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jOtDoctors , "doctor_name", "doctor_id", "Doctor");
			loadItemAutoComplete(descBox, jOtDoctors, "doctor_name", "doctor_id", "Doctor", "doctors", "otdoctors");
			keysIdentifier = "otdoctors";
	} else if ( ("ANAOPE" == chargeHead) || ("AANOPE" == chargeHead) ) {
		if ( (jAnaestetists = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jAnaestetists , "doctor_name", "doctor_id", "Doctor");
			loadItemAutoComplete(descBox, jAnaestetists, "doctor_name", "doctor_id", "Doctor", "anaestetists");
			keysIdentifier = "anaestetists";
	} else if ("SACOPE" == chargeHead) {
		if ( (jOperations = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jOperations, "operation_name", "op_id", "Operation");
			loadItemAutoComplete(descBox, jOperations, "operation_name", "op_id", "Operation", "operations");
			keysIdentifier = "operations";
	} else if ("EQOPE" == chargeHead) {
		if ( (jEquipments = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jEquipments, "equipment_name", "eq_id", "Equipment");
			loadItemAutoComplete(descBox, jEquipments, "equipment_name", "eq_id", "Equipment", "equipments");
			keysIdentifier = "equipments";
	} else if ("CONOPE" == chargeHead) {
		if ( (jConsumables = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jConsumables, "charge_name", "charge_name", "Charge");
			loadItemAutoComplete(descBox, jConsumables, "charge_name", "charge_name", "Charge", "consumables");
			keysIdentifier = "consumables";
	} else if ("SERSNP" == chargeHead) {
		if ( (jServices = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jServices, "service_name", "service_id", "Service");
			loadItemAutoComplete(descBox, jServices, "service_name", "service_id", "Service", "services");
			keysIdentifier = "services";
	} else if ("MEMED" == chargeHead) {
		if ( (jMedicines = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jMedicines, "charge_name", "charge_name", "Medicine");
			loadItemAutoComplete(descBox, jMedicines, "charge_name", "charge_name", "Medicine", "medicines");
			keysIdentifier = "medicines";
	} else if ("CONMED" == chargeHead) {
		if ( (jConsumables = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jConsumables, "charge_name", "charge_name", "Charge");
			loadItemAutoComplete(descBox, jConsumables, "charge_name", "charge_name", "Charge", "consumables");
			keysIdentifier = "consumables";
	} else if ("EQUOTC" == chargeHead) {
		if ( (jEquipments = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jEquipments, "equipment_name", "eq_id", "Equipment");
			loadItemAutoComplete(descBox, jEquipments, "equipment_name", "eq_id", "Equipment", "equipments");
			keysIdentifier = "equipments";
	} else if ("PKGPKG" == chargeHead) {
		if ( (jPackages = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jPackages, "package_name", "package_id", "Packages");
			loadItemAutoComplete(descBox, jPackages, "package_name", "package_id", "Packages", "packages");
			keysIdentifier = "packages";
	} else if ("OCOTC" == chargeHead) {
		if ( (jOtherCharges = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jOtherCharges, "charge_name", "charge_name", "Charge");
			loadItemAutoComplete(descBox, jOtherCharges, "charge_name", "charge_name", "Charge", "othercharges");
			keysIdentifier = "othercharges";
	} else if ("CONOTC" == chargeHead) {
		if ( (jConsumables = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jConsumables, "charge_name", "charge_name", "Charge");
			loadItemAutoComplete(descBox, jConsumables, "charge_name", "charge_name", "Charge", "consumables");
			keysIdentifier = "consumables";
	} else if ("MISOTC" == chargeHead) {
			keysIdentifier = null;

	}else if ("IMPOTC" == chargeHead) {
		if ( (jImplants = getItems(chargeHead, chargeGroup, loadDescriptions, false)) == null ) return;
			//loadSelectBox(descBox, jImplants, "charge_name", "charge_name", "Charge");
			loadItemAutoComplete(descBox, jImplants, "charge_name", "charge_name", "Charge", "implants");
			keysIdentifier = "implants";
	} else if ("TAX" == chargeGroup) {
		//loadSelectBox(descBox, "", null, "", "");

	} else if ( "BIDIS" == chargeHead ) {

	} else {
		alert ("Unknown charge head: " + chargeHead);
	}
}

var itemDataSources = [];
var autoCompleteArys = [];
var itemAutoCompleteEl  = null;
function loadItemAutoComplete(inputEl, jsArray, arrayElName, arrayElId, title, identifier) {

	var dataSource = getDataSource(identifier, jsArray, arrayElName, arrayElId);
	if (itemAutoCompleteEl != null) {
		itemAutoCompleteEl.destroy();
		itemAutoCompleteEl = null;
	}
	itemAutoCompleteEl = new YAHOO.widget.AutoComplete(inputEl.id, inputEl.id+"_container", dataSource);

	itemAutoCompleteEl.maxResultsDisplayed = jsArray.length;
	itemAutoCompleteEl.allowBrowserAutocomplete = false;
	itemAutoCompleteEl.prehighlightClassName = "yui-ac-prehighlight";
	itemAutoCompleteEl.typeAhead = true;
	itemAutoCompleteEl.useShadow = false;
	itemAutoCompleteEl.autoHighlight = true;
	itemAutoCompleteEl.minQueryLength = 0;
	itemAutoCompleteEl.forceSelection = true;
	itemAutoCompleteEl.animVert = false;
	YAHOO.util.Dom.addClass(YAHOO.util.Dom.get("autocomplete"), "autocomplete");
}

function getDataSource(identifier, jsArray, arrayElName, arrayElId) {

	if (itemDataSources[identifier] == undefined) {
		var array = getAutoCompleteArray(identifier, jsArray, arrayElName);
		dataSource = new YAHOO.widget.DS_JSArray(array);
		itemDataSources[identifier] = dataSource;
		keys = {identifier:[arrayElName, arrayElId]};
	}
	return itemDataSources[identifier];
}

function getAutoCompleteArray(arrayIdentifier, jsArray, arrayElName) {

	if (autoCompleteArys[arrayIdentifier] == undefined) {
		if (jsArray == null || arrayElName == null || arrayElName == '') {
			return null;
		}
		var array = new Array();
		for (var i in jsArray) {
			var el = jsArray[i];
			array.push(el[arrayElName]);
		}
		autoCompleteArys[arrayIdentifier] = array;
	}
	return autoCompleteArys[arrayIdentifier];

}

function getItemId() {
	var itemDesc  = document.forms[0].item_description.value;
	if (keysIdentifier == null) return;

	var chargeHead = document.forms[0].chargehead_id.value;
	var chargeGroup = document.forms[0].charge_group.value;

	var array = null;
	if (gItems[chargeHead] != null) array = gItems[chargeHead];
	if (gItems[chargeGroup] != null) array = gItems[chargeGroup];

	var columnName = null;
	var columnId = null;
	for (var i in keys) {
		var keyNames = keys[i];
		columnName = keyNames[0];
		columnId = keyNames[1];
	}

	for (var i in array) {
		var item = array[i];
		if (item[columnName] == itemDesc)
			alert(item[columnId]);
	}
}

/*
function getItemId(itemDesc) {
	var itemId = null;
	if (jEquipments != null) {
		try {
			itemId = jEquipments[itemDesc];
		} catch (e) {
		}
	}
	if (jDoctors != null) {
		try {
			itemId = jDoctors[itemDesc];
		} catch (e) {
		}
	}
	if (jOtDoctors != null) {
		try {
			itemId = jOtDoctors[itemDesc];
		} catch (e) {
		}
	}
	if (jAnaestetists != null) {
		try {
			itemId = jAnaestetists[itemDesc];
		} catch (e) {
		}
	}
	if (jBedWards != null) {
		try {
			itemId = jBedWards[itemDesc];
		} catch (e) {
		}
	}
	if (jICUWards != null) {
		try {
			itemId = jICUWards[itemDesc];
		} catch (e) {
		}
	}
	if (jLTests != null) {
		try {
			itemId = jLTests[itemDesc];
		} catch (e) {
		}
	}
	if (jRTests != null) {
		try {
			itemId = jRTests[itemDesc];
		} catch (e) {
		}
	}
	if (jOperations != null) {
		try {
			itemId = jOperations[itemDesc];
		} catch (e) {
		}
	}
	if (jServices != null) {
		try {
			itemId = jServices[itemDesc];
		} catch (e) {
		}
	}
	if (jOtherCharges != null) {
		try {
			itemId = jOtherCharges[itemDesc];
		} catch (e) {
		}
	}
	if (jConsumables != null) {
		try {
			itemId = jConsumables[itemDesc];
		} catch (e) {
		}
	}
	if (jMedicines != null) {
		try {
			itemId = jMedicines[itemDesc];
		} catch (e) {
		}
	}
	if (jTheatres != null) {
		try {
			itemId = jTheatres[itemDesc];
		} catch (e) {
		}
	}
	if (jPackages != null) {
		try {
			itemId = jPackages[itemDesc];
		} catch (e) {
		}
	}
	if (jImplants != null) {
		try {
			itemId = jImplants[itemDesc];
		} catch (e) {
		}
	}
	return itemId;
}
*/
