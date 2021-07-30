function init() {
	complaintForm = serviceConductionForm;
	initConductingDoctor();
	onStoreChange();
	if (serviceConductionForm.updateConsumables && !empty(conductedFlag) && conductedFlag != 'C')
		serviceConductionForm.updateConsumables.checked = true;
}

function initConductingDoctor() {
	var dataSource = new YAHOO.util.LocalDataSource({result : doctors});
	dataSource.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : 'result',
		fields : [
			{key : 'doctor_name'},
			{key : 'doctor_id'}
		]
	};
	var doctorAutoComp = new YAHOO.widget.AutoComplete('conducting_doctor', 'doctorContainer', dataSource);
	doctorAutoComp.minQueryLength = 0;
	doctorAutoComp.animVert = false;
	doctorAutoComp.maxResultsDisplayed = 50;
	doctorAutoComp.allowBrowserAutocomplete = false;
	doctorAutoComp.resultTypeList = false;
	doctorAutoComp.forceSelection = true;
	doctorAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	doctorAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	doctorAutoComp.itemSelectEvent.subscribe(setDoctorId);
	doctorAutoComp.unmatchedItemSelectEvent.subscribe(removeDoctorId);
	if (doctorAutoComp._elTextbox.value != '') {
			doctorAutoComp._bItemSelected = true;
			doctorAutoComp._sInitInputValue = doctorAutoComp._elTextbox.value;
		}
}

function setDoctorId(oSelf, elItem) {
	document.getElementById('conducting_doctor_id').value = elItem[2].doctor_id;
}

function removeDoctorId() {
	document.getElementById('conducting_doctor_id').value = '';
}

function onStoreChange() {
	var storeId = document.getElementById("store_id") != null ? document.getElementById("store_id").value : "";

	if (storeId == "") return;
	if (empty(consumablesJSON)) return;
	if (empty(medicineStoreStockDetailsJSON)) return;

	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId = document.getElementById("consumable_id"+i).value;
		var row = getThisRow(document.getElementById("consumable_id"+i));
		var stkQty = 0;
		var stkUnits = consumablesJSON[i].issue_units;
		var stkPkgType = consumablesJSON[i].package_type;

		// If the consumable is existent in store then negative stock deduction happens otherwise
		// we need to block save since the reagent deduction does not happen.
		var existentItem = "N";

		for (var m=0; m<medicineStoreStockDetailsJSON.length; m++ ) {
			var medicineId = medicineStoreStockDetailsJSON[m].medicineId;
			var deptId = medicineStoreStockDetailsJSON[m].deptId;
			if (consumableId == medicineId && storeId == deptId) {
				stkQty += medicineStoreStockDetailsJSON[m].qty;
				existentItem = "Y";
			}
		}
		setNodeText(row.cells[2], formatAmountValue(stkQty, true));
		setNodeText(row.cells[3], stkUnits);
		setNodeText(row.cells[4], stkPkgType);
		document.getElementById("existentItemInStore"+i).value = existentItem;
	}
}

function checkConsumableQty() {
	if (empty(consumablesJSON)) return null;

	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId	= document.getElementById("consumable_id"+i).value;
		var consumableQty	= document.getElementById("qty"+i).value;
		var row				= getThisRow(document.getElementById("consumable_id"+i));

		var stkQty = row.cells[2].getElementsByTagName("label")[0].textContent;
		if(eval(consumableQty) !=0)
			if (eval(stkQty) < eval(consumableQty)) return consumableId;
	}
	return null;
}

function numberOfNoStockConsumables() {
	if (empty(consumablesJSON)) return 0;

	var noOfConsumables = 0;
	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId	= document.getElementById("consumable_id"+i).value;
		var existent	 	= document.getElementById("existentItemInStore"+i).value;
		var consumableQty	= document.getElementById("qty"+i).value;
		var row				= getThisRow(document.getElementById("consumable_id"+i));

		var stkQty = row.cells[2].getElementsByTagName("label")[0].textContent;
		if (!empty(existent) && existent == 'Y' && eval(stkQty) < eval(consumableQty))
			noOfConsumables++;
	}
	return noOfConsumables;
}

function noStockConsumables() {

	var consumableNames = "";
	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId	= document.getElementById("consumable_id"+i).value;
		var existent	 	= document.getElementById("existentItemInStore"+i).value;
		var consumableQty	= document.getElementById("qty"+i).value;
		var row				= getThisRow(document.getElementById("consumable_id"+i));

		var stkQty = row.cells[2].getElementsByTagName("label")[0].textContent;
		if (!empty(existent) && existent == 'Y' && eval(stkQty) < eval(consumableQty))
			consumableNames += getConsumableName(consumableId) +",\n";
	}
	return consumableNames;
}

function checkConsumableExistenceInStore() {
	if (empty(consumablesJSON)) return null;

	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId = document.getElementById("consumable_id"+i).value;
		var existent	 = document.getElementById("existentItemInStore"+i).value;
		var row			 = getThisRow(document.getElementById("consumable_id"+i));

		if (empty(existent) || existent == 'N') return consumableId;
	}
	return null;
}

function numberOfNonExistentConsumablesInStore() {
	if (empty(consumablesJSON)) return 0;

	var nonExistents = 0;
	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId = document.getElementById("consumable_id"+i).value;
		var existent	 = document.getElementById("existentItemInStore"+i).value;
		var row			 = getThisRow(document.getElementById("consumable_id"+i));

		if (empty(existent) || existent == 'N')
			nonExistents++;
	}
	return nonExistents;
}

function nonExistentConsumables() {

	var consumableNames = "";
	for (var i=0; i<consumablesJSON.length; i++ ) {
		var consumableId	= document.getElementById("consumable_id"+i).value;
		var existent	 = document.getElementById("existentItemInStore"+i).value;
		var row				= getThisRow(document.getElementById("consumable_id"+i));

		if (empty(existent) || existent == 'N')
			consumableNames += getConsumableName(consumableId) + ",\n";
	}
	return consumableNames;
}

function validateAvailablityOfItemInStore() {
	var noOfNonExistent = numberOfNonExistentConsumablesInStore();

	if (noOfNonExistent == 0) return true;

	if (noOfNonExistent > 1) {
		var msg = "";
		if(consumableStockNegative == 'N')
			msg = "Warning: These consumables does not exist in the store. \n So unable to conduct the service. \n"+nonExistentConsumables();
		else
		{
			msg=getString("js.services.serviceconduction.warning.consumablesnotexist");
			msg+="\n";
			msg+=getString("js.services.serviceconduction.wanttocompletetheservice");
			msg+="\n";
			msg+=nonExistentConsumables();
		//	alert(msg);
			}
		var ok = confirm(msg);
		if (!ok) return false;
	}else {
		var nonExistentConsumable = checkConsumableExistenceInStore();
		var msg = "";
		if(consumableStockNegative == 'N')
		{
			msg = getString("js.services.serviceconduction.consumable");
			msg+=" ";
			msg+=getConsumableName(nonExistentConsumable);
			msg+="\n";
			msg+=getString("js.services.serviceconduction.isnotexistinginthestore");
			msg+="\n";
			msg+=getString("js.services.serviceconduction.unabletoconducttheservice");
			//alert(msg);
			}
		else
		{
			msg = getString("js.services.serviceconduction.consumable");
			msg+=" ";
			msg+=getConsumableName(nonExistentConsumable);
			msg+="\n";
			msg+=getString("js.services.serviceconduction.isnotexistinginthestore");
			msg+="\n";
			msg+=getString("js.services.serviceconduction.wanttocompletetheservice.withoutusingthis");
		//	alert(msg);
			}
		if (nonExistentConsumable != null) {
			var ok = confirm(msg);
			if (!ok) return false;
		}
	}
	return true;
}

function validateConsumableAvailableQty() {
	var noOfConsumables = numberOfNoStockConsumables();

	if (noOfConsumables == 0) return true;

	if (noOfConsumables > 1) {
		var msg ="";
		if(consumableStockNegative == 'N')
			msg=getString("js.services.serviceconduction.warning.thestorehasnoavailablequantity")+"\n"+getString("js.services.serviceconduction.unabletoconducttheservice")+"\n" +noStockConsumables();
		else
			msg=getString("js.services.serviceconduction.warning.thestorehasnoavailablequantity")+"\n"+getString("js.services.serviceconduction.wanttocontinue")+"\n" +noStockConsumables();
		var ok = confirm(msg);
		if (!ok) return false;
	}else {
		var msg = "";
		var zeroQtyConsumable = checkConsumableQty();
		if(consumableStockNegative == 'N')
			msg="Warning: The store has no available quantity for \n Consumable : " + getConsumableName(zeroQtyConsumable)+ " \n So unable to conduct the service.";
		else
			msg="Warning: The store has no available quantity for \n Consumable : " + getConsumableName(zeroQtyConsumable)+ " \n Do you want to continue?";

		if (zeroQtyConsumable != null) {
			var ok = confirm(msg);
			if (!ok) return false;
		}
	}
	return true;
}

function checkConsumableQtyWithMasterQty() {
	if(empty(serviceconsumablesjson))
		return true;

	if(empty(prescribedQty))
		prescribedQty = 1;

	var table = document.getElementById('reagentstable');
	var rows = table.rows.length;
	var tabLength = table.rows.length -2;
	var medicineNames = "";
	var index = 0;

	for(var i=0;i<tabLength;i++) {
		var consumableId = document.getElementById('consumable_id'+i).value;
		var qty = document.getElementById('qty'+i).value;
		if(consumableId == serviceconsumablesjson[i].consumable_id) {
			if(serviceconsumablesjson[i].qty*prescribedQty < qty) {
				medicineNames += empty(medicineNames) ? serviceconsumablesjson[i].medicine_name : ','+serviceconsumablesjson[i].medicine_name;
				index++;
			}
		}
	}

	if(!empty(medicineNames)) {
		var msg = 'Item';
		if(index > 1)
			msg = 'items';

		alert(getString("js.services.serviceconduction.consumptionqtyentered")+" "+msg+" "+medicineNames+" "+getString("js.services.serviceconduction.ismorethanallowed"));
		return false;
	}

	return true;
}

function getConsumableName(consumableId) {
	if (empty(consumablesJSON)) return null;

	for (var i=0; i<consumablesJSON.length; i++ ) {
		var medicineId = consumablesJSON[i].consumable_id;
		var medicineName = consumablesJSON[i].medicine_name;
		if (consumableId == medicineId) {
			return medicineName;
		}
	}
	return null;
}

function validateQuantity() {
	if (empty(consumablesJSON)) return true;

	var table = document.getElementById("reagentstable");
	var rowLen = table.rows.length - 2;

	for (var j=0; j<rowLen; j++) {
		var consumableId = document.getElementById("consumable_id"+j).value;
		var consumableQty = document.getElementById("qty"+j).value;
		if (empty(consumableQty)) document.getElementById("qty"+j).value = "0";

		if (!validateAmount(document.getElementById("qty"+j), getString("js.services.serviceconduction.quantity.validamount")))
			return false;
	}
	return true;
}

function isConsumableQtyEdited() {
	if (empty(consumablesJSON)) return false;

	var table = document.getElementById("reagentstable");
	var rowLen = table.rows.length - 2;

	for (var j=0; j<rowLen; j++) {
		var consumableId = document.getElementById("consumable_id"+j).value;
		var consumableQty = document.getElementById("qty"+j).value;

		for (var i=0; i<consumablesJSON.length; i++ ) {
			var origConsumableId = consumablesJSON[i].consumable_id;
			var origConsumableQty = consumablesJSON[i].qty;
			if (consumableId == origConsumableId) {
				if (eval(consumableQty) != eval(origConsumableQty)) return true;
			}
		}
	}
	return false;
}

function resetConsumablesQty() {
	if (empty(consumablesJSON)) return;

	var table = document.getElementById("reagentstable");
	var rowLen = table.rows.length - 2;

	for (var j=0; j<rowLen; j++) {
		var consumableId = document.getElementById("consumable_id"+j).value;

		for (var i=0; i<consumablesJSON.length; i++ ) {
			var origConsumableId = consumablesJSON[i].consumable_id;
			var origConsumableQty = consumablesJSON[i].qty*prescribedQty;
			if (consumableId == origConsumableId) {
				document.getElementById("qty"+j).value = origConsumableQty.toFixed(decDigits);
			}
		}
	}
}

function validateEditedQuantity() {

	if (empty(consumablesJSON)) return true;

	if (!validateQuantity()) {
		return false;
	}
	var isQtyEdited = isConsumableQtyEdited();
	var updateChecked = serviceConductionForm.updateConsumables.checked;
	if (isQtyEdited && !updateChecked) {
		var ok = confirm(" One or more consumables quantity is edited. \n " +
						 "Do you want to update quantity?");
		if (ok) {
			serviceConductionForm.updateConsumables.checked = true;
		}else {
			showMessage("js.services.serviceconduction.resettingconsumablesquantity");
			resetConsumablesQty();
			return false;
		}
	}
	return true;
}

function checkUserStore() {

	if (empty(consumablesJSON)) return true;

	var storeObj = document.getElementById('store_id');
	if (storeObj == null || storeObj.value == '' || (!empty(storeObj.options) && storeObj.options.length == 0)) {
		showMessage("js.services.serviceconduction.nostoreavailable.deductconsumables");
		return false;
	}
	return true;
}

