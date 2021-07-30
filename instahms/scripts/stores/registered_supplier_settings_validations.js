var taxobj = null;
var cformObj = null;
var taxRateObj = null;
var taxApplicable = true;
var itemLevelCSTApplicable = false;
var cFormApplicable = false;
var cFormChkCount = 0;
var form8HObj = null;
function registeredSupplierSettings(supplierObj){
	if ( isRegisteredSupplier(supplierObj) ){
		//registered supplier
		taxApplicable = true;
		if ( taxRateObj ){
			taxRateObj.disabled = false;
		}
		
		var selectedtoreId = document.getElementsByName("store_id")[0].value;
		if ( isIntraStateSupplier(supplierObj,selectedtoreId)){
			//Intra state purchase
			var taxArray = new Array();
			taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
			if( prefVAT == 'Y' ){
				loadSelectBox(taxobj, taxArray, null, null);
			}
			if ( taxRateObj ){
				taxRateObj.readOnly = true;
			}
			if ( cformObj ){
				cformObj.checked = false;
				cformObj.disabled = true;
			}
			if ( form8HObj ){
				form8HObj.checked = false;
				form8HObj.disabled = false;
			}

			itemLevelCSTApplicable = false;
		}else{
			//Inter state purchase
			var taxArray = new Array();
			taxArray.push(taxLabel == 'V'?'CST' : 'iGST');
			if ( prefVAT == 'Y'  ){
				loadSelectBox(taxobj, taxArray, null, null);
			}	
			if ( taxRateObj ){
				taxRateObj.readOnly = false;
			}
			if ( cformObj ){
				cformObj.checked = false;
				cformObj.disabled = false;
			}
			if ( form8HObj ){
				form8HObj.checked = false;
				form8HObj.disabled = true;
			}

			itemLevelCSTApplicable = true;
		}
		
	} else {
		//unregistered supplier
		var taxArray = new Array();
		taxApplicable = false;
		itemLevelCSTApplicable = false;
		
		if ( taxRateObj ){
			taxRateObj.value = 0;
			taxRateObj.disabled = true;
		}
		if ( cformObj ){
			cformObj.checked = false;
			cformObj.disabled = true;
		}	
		if ( form8HObj ){
			form8HObj.checked = false;
			form8HObj.disabled = true;
		}
		if(prefVAT == 'Y' ){
			loadSelectBox(taxobj, [{"key":"NA","value":"Not Applicable"}], "value", "key");
		}
	}
}

function isRegisteredSupplier(selectedSupplier){
	//var selectedSupplier = findInList(suppliersListJSON,'SUPPLIER_CODE', suppId);
	return ( selectedSupplier != null ? selectedSupplier.IS_REGISTERED == 'Y' : false );
}

function isIntraStateSupplier(supplierObj,selectedtoreId){
	//var selectedSupplier = findInList(suppliersListJSON,'SUPPLIER_CODE', suppId);
	
	var center_id = 0;
	var selectedStore = findInList(storesListJSON,'dept_id',selectedtoreId);
	if (selectedStore != null && selectedStore != undefined){ 
		center_id = selectedStore.center_id;
	}
	
	var storesCenterDetails = findInList(centersJSON,'center_id',center_id);
	return ( storesCenterDetails != null && storesCenterDetails.state_id != null ? 
			supplierObj.STATE_ID == storesCenterDetails.state_id : false );
}

function validateCformTaxrate(cformObj){
	if ( !cformObj.checked ){
		cFormApplicable = false;
		recalTaxAmt(cformObj);
		return true;
	}else{
		if(form8HObj != null){
		  form8HObj.checked = false;
		}
		cformObj.checked = true;
		var taxArray = new Array();
		taxArray.push(taxLabel == 'V' ? 'CST':'iGST');
		loadSelectBox(taxobj, taxArray, null, null);
	}
	cFormApplicable = true;
	//make sure main tax rate is not > 2
	recalTaxAmt(cformObj);

	return true;
}

var form8HApplicable = false;
function validate8HformTaxrate(form8HObj){
	var taxArray = new Array();
	if ( !form8HObj.checked ){

		if(applySupplierTaxRules == 'false'){
			taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
			taxArray.push(taxLabel == 'V' ? 'CST' : 'iGST');
		}else{
			if(cformObj != null && cformObj.disabled){
				taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
			}else{
				taxArray.push(taxLabel == 'V' ? 'CST' : 'iGST');
			}

		}
		
		if( prefVAT == 'Y' ){
			loadSelectBox(taxobj, taxArray, null, null);
		}
		form8HApplicable = false;

	}else{

		taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
		form8HApplicable = true;
		if(cformObj != null)
			cformObj.checked = false;
		if( prefVAT == 'Y' ){
			loadSelectBox(taxobj, taxArray, null, null);
		}

	}
	form8Hcal = true;
	recalTaxAmt(form8HObj);
	form8Hcal = false;
	return true;
}
function recalTaxAmt(cformObj) {
	
	 var formName = cformObj.form.name;

	 var numRows = getNumItems();
	 //if (itemLevelCSTApplicable) {
		 for (var k=0; k<gRowItems.length; k++) {
			 cFormChkCount = cFormChkCount + 1;
			var item = gRowItems[k];

			if(cFormApplicable && item.tax_rate > 2) {
				item.master_tax_rate = 2;
				item.vat_rate = item.master_tax_rate;
			} else
				item.vat_rate = item.tax_rate;
			calcItemValues(item, 'stored');
			if(formName == 'poForm') {
				item.vat = formatAmountPaise(item.vat);
				item.med_total = formatAmountPaise(item.med_total);
				item.discount = formatAmountPaise(item.discount);
				item.item_ced = formatAmountPaise(item.item_ced);
			}
			rowItemToRow(k);
			resetTotals();
			if(formName == 'poForm') {
				item.discount = getElementPaise(item.discount);
				item.item_ced = getElementPaise(item.item_ced);
			}
		}
	 //}
}

function resetForm8hValues(form8HObj){
	var taxArray = new Array();
	if ( !form8HObj.checked ){

		if(applySupplierTaxRules == 'false' || applySupplierTaxRules == 'f'){
			taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
			taxArray.push(taxLabel == 'V' ? 'CST' : 'iGST');
		}else{
			if(cformObj != null && cformObj.disabled){
				taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
			}else{
				taxArray.push(taxLabel == 'V' ? 'CST' : 'iGST');
			}

		}
		
		if( prefVAT == 'Y' ){
			loadSelectBox(taxobj, taxArray, null, null);
		}
		form8HApplicable = false;

	}else{

		taxArray.push(taxLabel == 'V' ? 'VAT' : 'GST');
		form8HApplicable = true;
		if(cformObj != null)
			cformObj.checked = false;
		if( prefVAT == 'Y' ){
			loadSelectBox(taxobj, taxArray, null, null);
		}

	}
	form8Hcal = true;
	allRowsHiddenToLabels();
	resetTotals();
	form8Hcal = false;
	return true;
}

function resetTaxTypeValues(supObj){
	if(prefVAT == 'Y' && supObj.value == '' &&  (form8HObj && !form8HObj.checked) ){
		if ( applySupplierTaxRules == 'false' || applySupplierTaxRules == 'f' ){
			loadSelectBox(taxobj, [{"key":(taxLabel == 'V' ? 'VAT' : 'GST'),"value":(taxLabel == 'V' ? 'VAT' : 'GST')},{"key":(taxLabel == 'V' ? 'CST' : 'iGST'),"value":(taxLabel == 'V' ? 'CST' : 'iGST')}], "value", "key");
		}else{
			loadSelectBox(taxobj, [{"key":(taxLabel == 'V' ? 'VAT' : 'GST'),"value":(taxLabel == 'V' ? 'VAT' : 'GST')},{"key":(taxLabel == 'V' ? 'CST' : 'iGST'),"value":(taxLabel == 'V' ? 'CST' : 'iGST')},{"key":"NA","value":"Not Applicable"}], "value", "key");
		}
		
	}
}
