var oItemAutoComp;

var poForm;
var dlgForm;
var gColIndexes = [];
var gRowUnderEdit = -1;
var gRowItems = [];
var poAmendmentStatus = false;
var gDialogItem = null;
var addItemsFromStkRed = false;
var form8Hcal = false;
var gDoRoundOff = true;
var gItemTaxGroups = {};
var gItemTaxSubGroups = {};

function init() {
    if (poNo != '')	{
		ajaxForPrintUrls();
	}
    
    if (poNo != '' && tcsApplicable == 'Y')	{
    	document.getElementById("tcsRow").style.display = '';
    }
  
	poForm = document.poForm;
	dlgForm = document.dlgForm;
	 if(poForm.status.value != 'O'){
		 //poForm.supplier_name.disabled =true;
		 poForm.store_id.disabled =true;
	 }
	 if(poForm.supplier_name){
		 disableSupplier(poForm.status.value);
	 }
	
	// column indexes
	var cl=0;

	gColIndexes.medicine_name = cl++;
	gColIndexes.item_code = cl++;
	gColIndexes.po_pkg_size = cl++;
	gColIndexes.package_type = cl++;
	gColIndexes.mrp_display = cl++;
	gColIndexes.adj_mrp_display = cl++;
	gColIndexes.cost_price_display = cl++;
	gColIndexes.qty_req_display = cl++;
	gColIndexes.bonus_qty_req_display = cl++;
	gColIndexes.uom_display = cl++;
	gColIndexes.vat_rate = cl++;
	gColIndexes.vat_type = cl++;
	gColIndexes.discount_per = cl++;
	gColIndexes.discount = cl++;
	gColIndexes.vat = cl++;
	gColIndexes.med_total = cl++;
	TRASH_COL = cl++; EDIT_COL = cl++;

	initSupplierAutoComplete();
	initItemAutoComplete();
	initDialog();
	initPurchaseDetailsDialog(dlgForm.qty_req_display);
	/*if (prefVAT == 'Y') {
		initForm8hVariables();// in purchasedetails.js
	}*/

    if (poNo != '')	{
		oSupplierAutoComp._bItemSelected = true;
		setSupplierAttributes();
		/*var vatTaxApplicable = (poStoredVattype != 'CST' && poStoredVattype != 'iGST' );
		if ( prefVAT == 'Y' && amendment == 'true' && taxVariation() && vatTaxApplicable){
			if(confirm(getString("js.stores.procurement.amend.update.master.tax.confirmation"))){
				updateMasterTax();
			}
		}*/
		//if update with master option is picked 
		allRowsHiddenToLabels();
		if(poForm.supplier_name){
			 disableSupplier(poForm.status.value);
		}
		/*if( poForm.main_vat_type )
			poForm.main_vat_type.disabled = true;*///tax type is not editable

	} else if (initSupplierId != '') {
		// called from stock reorder: supplier should be pre-selected.
		poForm.supplier_id.value = initSupplierId;
		oSupplierAutoComp._bItemSelected = true;
		setSelectedIndex(poForm.store_id, initStoreId);
		setSupplierAttributes();
	}

	// add new items, eg, from stock reorder
	if (jAdditionalItems != undefined) {
		addAdditionalItems();
	}

	poForm.supplier_name.focus();

	if (allowBackDate == 'N') {
		poForm.po_date.readOnly = true;
	} else {
		poForm.po_date.readOnly = false;
	}
	checkstoreallocation();
   
	// fields disabled for Partial PO
	if(document.getElementById("grn_count").value > 0){
		fieldsdisabledforPartialPO();
	}
	/*if (applySupplierTaxRules != 't'){
		for(var i = 0; i < document.getElementsByName("cstRateH").length; i++) {
			document.getElementsByName("cstRateH")[i].style.display = "table-cell";
		}
	}*/
	resetTotals();
	getSubgroups();
	taxGroupInit("add_tax_groups");
}

function fieldsdisabledforPartialPO() {
	document.getElementsByName("store_id")[0].disabled = true;
	document.getElementsByName("dept_id")[0].disabled = true;
	document.getElementsByName("po_qty_unit")[0].disabled = true;
	document.getElementsByName("po_qty_unit")[0].disabled = true;
	document.getElementsByName("supplier_name")[0].disabled = true;
	document.getElementsByName("po_alloted_to")[0].disabled = true;
	document.getElementsByName("po_date")[0].disabled = true;
	document.getElementsByName("enq_no")[0].disabled = true;
	document.getElementsByName("enq_date")[0].disabled = true;
	document.getElementsByName("qut_no")[0].disabled = true;
	document.getElementsByName("qut_date")[0].disabled = true;
	document.getElementsByName("reference")[0].disabled = true;
	document.getElementsByName("credit_period")[0].disabled = true;
	document.getElementsByName("delivery_date")[0].disabled = true;
	document.getElementsByName("main_vat_type")[0].disabled = true;
	document.getElementsByName("main_cst_rate")[0].disabled = true;
	document.getElementsByName("discount_type")[0].disabled = true;
	document.getElementsByName("discount_val")[0].disabled = true;
	document.getElementsByName("round_off")[0].disabled = true;
	document.getElementsByName("supplierTermTemplates")[0].disabled = true;
	document.getElementsByName("supplier_terms")[0].disabled = true;
	document.getElementsByName("hospital_terms")[0].disabled = true;
	document.getElementsByName("deliveryInstructionTemplates")[0].disabled = true;
	document.getElementsByName("delivery_instructions")[0].disabled = true;
	
	if(document.getElementsByName("status_fld")[0].value == 'FC'){
		document.getElementsByName("remarks")[0].disabled = true;
	}
 
    var rowNum =document.getElementById("medtabel").rows.length;
	var table = document.getElementById("medtabel");
	for (var i = 1; i <= rowNum-2; i++) {
		var rowObj =table.rows[i];
		var editImg = getElementByName(rowObj,"editicon");
		var x=document.getElementsByClassName("imgDelete");
		x[i-1].setAttribute('src',popurl+"/icons/delete_disabled.gif");
		x[i-1].setAttribute('onclick','');
		
		editImg.setAttribute('src',popurl+'/icons/Edit1.png');
		editImg.setAttribute('onclick','');
	}
}

function checkstoreallocation() {
	if (poForm.store_id.options.length == 0) {
		showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
		poForm.btnSavePo.disabled = true;
		poForm.btnSaveAndPrintPo.disabled = true;
	} else if ((poNo != '') && (poForm.store_id.value != poStoreId)) {
		showMessage("js.stores.procurement.nothaveaccess.postore.savedisabled");
		poForm.btnSavePo.disabled = true;
		poForm.btnSaveAndPrintPo.disabled = true;
	}
}

function initSupplierAutoComplete() {
	var supplierNames = [];
    var j = 0;
	var maxResults = 200;
	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/pages/stores/supplierDetails.do");
	var queryParams = "method=getSuppliersByQuery&centerId=" + centerId + '&limit=' + maxResults ;
    dataSource.scriptQueryAppend = queryParams;


	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "supplier_name_with_city"}, {key : "supplier_code"}, {key : "tcs_applicable"} ]
	};



	oSupplierAutoComp = new YAHOO.widget.AutoComplete(poForm.supplier_name, 'supplier_dropdown', dataSource);
	oSupplierAutoComp.maxResultsDisplayed = maxResults;
	oSupplierAutoComp.allowBrowserAutocomplete = false;
	oSupplierAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oSupplierAutoComp.typeAhead = false;
	oSupplierAutoComp.useShadow = false;
	oSupplierAutoComp.minQueryLength = 0;
	oSupplierAutoComp.forceSelection = true;
	oSupplierAutoComp.filterResults = Insta.queryMatchWordStartsWith;

	oSupplierAutoComp.itemSelectEvent.subscribe(onSelectSupplier);
}

function onSelectSupplier(type, args) {
	var suppId = args[2][1];
	var tcsApplicable = args[2][2];
	if(poForm.supplier_id.value != "" && poNo == ''){
		if(poForm.supplier_id.value != suppId){
			deleteAllRows();
		}
	}
	poForm.supplier_id.value = suppId;
	if(tcsApplicable == 'Y') {
		document.getElementById("tcsRow").style.display = '';
	} else {
		document.getElementById("tcsRow").style.display = 'none';
	}
	setSupplierAttributes();
}

function getSupplierDetails(supplierCode) {
	var xhttp = new XMLHttpRequest();
	var url = cpath + '/pages/stores/supplierDetails.do?method=getSupplierById&supplierCode=' + supplierCode;
	xhttp.open("GET", url, false);
	xhttp.send(null);
	if (xhttp.readyState == 4) {
		if ( (xhttp.status == 200) && (xhttp.responseText != null ) ) {
			return eval('('+xhttp.responseText+')');
		}
	}
	return null;
}

function setSupplierAttributes() {
	var supplierList= [];
    var j = 0;
	var suppId = poForm.supplier_id.value;
	var supplier = getSupplierDetails(suppId);
		    		
	var supplierAddress = supplier.supplier_address;
	if(supplier.supplier_phone1 != null && supplier.supplier_phone1 != '')
		supplierAddress = supplierAddress + " Ph: " + supplier.supplier_phone1;
	else if(supplier.supplier_phone2 != null && supplier.supplier_phone2 != '')
		supplierAddress = supplierAddress + " Ph: " +supplier.supplier_phone2;
	if(supplier.supplier_fax != null && supplier.supplier_fax != '')
		supplierAddress = supplierAddress + " Fax: " + supplier.supplier_fax;

	setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 16, supplierAddress);

	if (poNo == '') {
		poForm.credit_period.value = supplier.credit_period;
	}
	poForm.supplier_name.value = supplier.supplier_name;
	
}

function allRowsHiddenToLabels() {
	var numItems = getNumItems();
	for (var k=0; k < numItems; k++) {
		var row = getItemRow(k);

		calcRowValues(row, 'stored');
		rowHiddenToLabels(row, gColIndexes);
//		if( taxSelection() == 'CST' || taxSelection() == 'iGST' ) {
//        	row.cells[gColIndexes.vat_rate].textContent = getElementByName(row, "cst_rate").value;
//        }

//		if ( taxSelection() == 'VAT' && gColIndexes.vat_type ) {
//			var taxType = row.cells[gColIndexes.vat_type].textContent;
//			row.cells[gColIndexes['vat_type']].title = ( taxSelection() == 'VAT' ? ( taxType == 'MB'
//												? "MRP Based(with bonus)" : (
//										 taxType == 'M'
//										 		? 'MRP Based(without bonus)' : (
//								 		 taxType == 'CB' ?  'CP Based(with bonus)' : 'CP Based(without bonus)'))) : '' );
//		} else if ( (taxSelection() == 'CST' || taxSelection() == 'iGST') && gColIndexes.vat_type ) {
//			row.cells[gColIndexes.vat_type].textContent = '';
//			row.cells[gColIndexes['vat_type']].title = '';
//            row.cells[gColIndexes['vat_rate']].textContent = getRowAmount(row,'cst_rate');
//		}
	}
}

function onChangeDefVat() {
	var vat = formatAmountObj(poForm.vat_rate);
	if (parseFloat(vat) > 100) {
		showMessage("js.stores.procurement.vattaxvalue.lessthan100");
		poForm.vat_rate.value = 0;
		poForm.vat_rate.focus();
		return false;
	}
}

function onChangeQtySelection() {
	// we just need to recalculate row values and update the labels, just like
	// we would do when loading up an existing PO for edit
	allRowsHiddenToLabels();
}

function onChangeRoundOff() {
	formatAmountObj(poForm.round_off);
	resetTotals();
}

function onChangeDiscountType() {
	poForm.discount_val.value = 0;
	resetTotals();
}

function onChangeDiscountVal() {
	formatAmountObj(poForm.discount_val);
	resetTotals();
}

function onChangeTcsType() {
	poForm.tcs_value.value = 0;
	resetTotals();
}

function onChangeTcsVal() {
	if(poForm.tcs_type.value =='P') {
		formatTCSAmountObj(poForm.tcs_value);
	} else {
		formatAmountObj(poForm.tcs_value);
	}
	resetTotals();
}

function onChangeTransportationChargeVal() {
	formatAmountObj(poForm.transportation_charges);
	resetTotals();
}
function initItemAutoComplete() {
	if (oItemAutoComp)
		oItemAutoComp.destroy();

	//var dataSource = new YAHOO.widget.DS_JSArray(jItemNames);
	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/pages/stores/getItemMaster.do");
	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "cust_item_code_with_name"},{key : "medicine_id"}, {key : "medicine_name"} ]
	};
	oItemAutoComp = new YAHOO.widget.AutoComplete(dlgForm.medicine_name, 'item_dropdown', dataSource);
	oItemAutoComp.maxResultsDisplayed = 50;
	oItemAutoComp.allowBrowserAutocomplete = false;
	oItemAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oItemAutoComp.typeAhead = false;
	oItemAutoComp.useShadow = false;
	oItemAutoComp.minQueryLength = 2;
	oItemAutoComp.forceSelection = true;
	oItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oItemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oItemAutoComp.itemSelectEvent.subscribe(onSelectItem);
}

function onChangeBarcode(val) {
	if (val == '') {
		resetDetails();
		dlgForm.medicine_name.value = '';
		return;
	}
	var ajaxReqObject = newXMLHttpRequest();
	var url = cpath+'/pages/stores/getItemMaster.do?isBarCodeSearch=true&query='+val;
	var ajaxResponse = '';
	var item = null;
	ajaxReqObject.open("GET",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			eval("ajaxResponse = " + ajaxReqObject.responseText);
			var result = ajaxResponse.result;
			if(result.length>0) {
				item = result[0];
			}
		}
	}

	if (item == null) {
		showMessage("js.stores.procurement.itemforthegivenbarcode.notexist");
		resetDetails();
		dlgForm.medicine_name.value = '';
		dlgForm.medicine_id.value = '';
		return;
	}

	dlgForm.medicine_name.value = item.medicine_name;
	dlgForm.medicine_id.value = item.medicine_id;
	oItemAutoComp._bItemSelected = true;
	selectItem();
}

var onSelectItem = function(type, args) {
	dlgForm.medicine_id.value = args[2][1];
	dlgForm.medicine_name.value = args[2][2];
	clearTaxSelection();
	selectItem();
}

function selectItem() {
 	var medId = dlgForm.medicine_id.value;
	var storeId = poForm.store_id.value;
	var supp = poForm.supplier_id.value;

	var medUrl= cpath+"/pages/stores/poscreen.do?_method=getMedDetails" +
		"&medicineId="+medId + "&storeId="+storeId + "&suppId="+supp+"&supplier_Validation=true";
	YAHOO.util.Connect.asyncRequest('GET', medUrl, { success: handleNewItemDetailResponse });

}

function handleNewItemDetailResponse(response) {
	var responseText = response.responseText;
	if ( (responseText==null) || (responseText=="")) return;

    eval("itemdet = " + responseText);
	var item = itemdet;
	item.tax_rate = 0;
	//tax is not applicable for non-registerd supplier at supplier tax rule schema
	if ( !taxApplicable ){
		item.tax_rate = 0;
	}

	dlgForm.medicine_id.value = item.medicine_id;
	dlgForm.billable.value = item.billable;
	dlgForm.po_pkg_size.value = item.issue_base_unit;
	dlgForm.package_uom.value = item.package_uom;
	dlgForm.issue_units.value = item.issue_units;
	dlgForm.package_type.value = item.package_type;
	dlgForm.item_code.value = item.item_code;
	dlgForm.min_rate.value = item.min_rate;
	dlgForm.discounted_min_rate.value = item.discounted_min_rate;
	dlgForm.min_rate_suppliers.value = item.min_rate_suppliers;
	dlgForm.margin.value = item.margin;
	dlgForm.margin_type.value = item.margin_type;
	dlgForm.max_cost_price.value = item.max_cost_price;
	
	if (prefBarCode == 'Y')
		dlgForm.item_barcode_id.value = item.item_barcode_id;

//	if ( itemLevelCSTApplicable ){
//		if (poForm.c_form.checked && item.tax_rate >2) {
//			item.master_tax_rate = 2;
//		} else {
//			item.master_tax_rate = item.tax_rate;
//		}
//		dlgForm.vat_rate.value = item.master_tax_rate;
//		item.vat_rate = item.tax_rate;
//		dlgForm.tax_rate.value = item.tax_rate;
//		
//    } else 
    dlgForm.vat_rate.value = item.tax_rate;
    dlgForm.vat_type.value = item.tax_type;
    document.getElementById("lblvat_rate").innerHTML = dlgForm.vat_rate.value;
	document.getElementById("lblvat_amt").innerHTML = dlgForm.vat.value;
	

	if (qtyUnitSelection() == 'I') {
		// convert rates to issue units
		dlgForm.mrp_display.value = (item.mrp/item.issue_base_unit).toFixed(decDigits);
		dlgForm.cost_price_display.value = (item.cost_price/item.issue_base_unit).toFixed(decDigits);
	} else {
		dlgForm.mrp_display.value = item.mrp;
		dlgForm.cost_price_display.value = item.cost_price;
	}
	
	if(item.supplier_rate_validation != null && item.supplier_rate_validation == "true"){
		dlgForm.supplier_rate_val.value = parseFloat(item.cost_price).toFixed(decDigits);
		dlgForm.supplier_rate_validation.value = item.supplier_rate_validation;
	}else{
		dlgForm.supplier_rate_val.value = parseFloat(0).toFixed(decDigits);
		dlgForm.supplier_rate_validation.value = 'false';
	}
	
	dlgForm.discount.value = "0";
	if(typeof(item.discount) == 'undefined'){
		dlgForm.discount_per.value = "0";
	}else{
		dlgForm.discount_per.value = item.discount;
	}
	dlgForm.store_id_hid.value = poForm.store_id.value;
	dlgForm.supplier_code_hid.value = poForm.supplier_id.value;
	dlgSetStockInfo(item);
	getTaxDetails('dlgForm');
}

function onChangeTaxBasis(obj) {
	dlgForm.vat_type.value =  obj.value;
	getTaxDetailsOnChange('dlgForm');
}

function onPurchaseDetails() {
	var medId = dlgForm.medicine_id.value;
	var storeId =document.getElementsByName("store_id")[0].value;
	if (medId == '')
		return;
	showPurchaseDetails(medId,storeId);
}

// clear the dialog values
function resetDetails(){
	dlgForm.medicine_name.value = '';
    dlgForm.medicine_id.value = '';
	dlgForm.qty_req_display.value = 0;
	dlgForm.bonus_qty_req_display.value = 0;

	dlgForm.mrp_display.value = 0;
    dlgForm.cost_price_display.value = 0;
    dlgForm.discount.value = 0;
    dlgForm.discount_per.value = 0;
    dlgForm.item_ced_per.value = 0;
    dlgForm.item_ced.value = 0;
	dlgForm.po_pkg_size.value = '';
	dlgForm.min_rate.value = '';
	dlgForm.discounted_min_rate.value = '';
	dlgForm.min_rate_suppliers.value = '';
	dlgForm.min_rate_suppliers.margin = '';
	dlgForm.min_rate_suppliers.margin_type = '';
	setSelectedIndex(dlgForm.vat_type, 0);

	if ( dlgForm.status ) {
		setSelectedIndex(dlgForm.status, 0);
		dlgForm.item_remarks.value = '';
	}

    document.getElementById('lblManf').innerHTML = "";
	document.getElementById('lblStoreStock').innerHTML = "";
	document.getElementById('lblTotalStock').innerHTML = "";
	document.getElementById('lblPkgSize').innerHTML = "";
	document.getElementById('lblPkgUom').innerHTML = "";
	document.getElementById('lblUnitUom').innerHTML = "";

    if (prefBarCode == 'Y') {
    	dlgForm.item_barcode_id.value = '';
    	dlgForm.item_barcode_id.readOnly = false;
    }
}

function onDialogSave () {
	if (dlgValidate ()) {
		dlgSave();
	}
}

function dlgSave() {
	form8Hcal = true;
	calcDlgValues();
	var rownumber = gRowUnderEdit;
	if (rownumber == -1) {
		// new item added
		addDialogToGrid();
	    detaildialog.cancel();
		openAddDialog();		// add another
	} else {
		// existing item updated
  		updateDialogToGrid();
  		gRowItems[rownumber] = formatPaise(gDialogItem);
  		gDialogItem = null;
	    detaildialog.cancel();	// save and stay
    }
	form8Hcal = false;
}

function dlgValidate() {
	if (trimAll(dlgForm.medicine_name.value) == '') {
		dlgForm.medicine_name.value = '';
		showMessage("js.stores.procurement.itemnameisrequired");
        dlgForm.medicine_name.focus();
        return false;
    }

	if (!validateRequired(dlgForm.qty_req_display, getString("js.stores.procurement.quantityisrequired"))) return false;

    if (!isValidNumber(dlgForm.qty_req_display, qtyDecimal, 'Quantity')) return false;
    if (!isValidNumber(dlgForm.bonus_qty_req_display, qtyDecimal, 'Bonus Quantity')) return false;

    if (dlgForm.qty_req_display.value == 0 && dlgForm.bonus_qty_req_display.value == 0) {
    	showMessage("js.stores.procurement.qtynotbezero");
        dlgForm.qty_req_display.focus();
        return false;
    }

	if (dlgForm.cost_price_display.value == 0 || dlgForm.cost_price_display.value == '' ) {
		showMessage("js.stores.procurement.rateisrequired");
        dlgForm.cost_price_display.focus();
        return false;
    }

    var rate = getPaise(dlgForm.cost_price_display.value);
	var mrp =  getPaise(dlgForm.mrp_display.value);
	var billable =  dlgForm.billable.value;

	if ((mrp < rate) && (billable == 'true')) {
		var msg=getString("js.stores.procurement.mrpgreaterthanrate");
		msg+=" ";
		msg +=getString("js.stores.procurement.mrpis");
		msg+=" ";
		msg+= getPaiseReverse(mrp);
		msg +=getString("js.stores.procurement.rateis");
		msg+=" ";
		msg += getPaiseReverse(rate);
		msg+=" ";
		alert(msg);
		dlgForm.mrp_display.focus();
		return false;
	}
    if (dlgForm.mrp_display.value == 0 || dlgForm.mrp_display.value == '') {
		if (!confirm("MRP is zero. Do you want to proceed ?.")) {
        	dlgForm.mrp_display.focus();
        	return false;
		}
	}
    var costPrice = dlgForm.cost_price_display.value;
    var medicineId = dlgForm.medicine_id.value;
    var maximumCostPrice = dlgForm.max_cost_price.value;
	if(dlgForm.supplier_rate_validation.value == "true"){
		if (!validateMaxCostPrice(medicineId, costPrice, maximumCostPrice))
			return false;
	}else{
		if (validateCostPrice == "Y") {
			if (!validateMaxCostPrice(medicineId, costPrice, maximumCostPrice))
				return false;
	    }
	}
	

	var discountPer = getAmount(dlgForm.discount_per.value);
	if (discountPer > 100) {
		showMessage("js.stores.procurement.discountshouldbelessthan100");
		dlgForm.discount_per.focus();
		return false;
	}

    var itemListTable = document.getElementById("medtabel");
	var numItems = getNumItems();

	for (var k=0; k < numItems; k++) {
		if (gRowUnderEdit == k)
			continue;
		rowObj = getItemRow(k);
		if (getElementByName(rowObj,'_deleted').value == 'true')
			continue;

		if (getElementByName(rowObj,'medicine_id').value == dlgForm.medicine_id.value) {
			showMessage("js.stores.procurement.duplicateentry");
			dlgForm.medicine_name.value= '';
			resetDetails();
			return false;
		}
    }

    if( prefRejRemarks == 'Y' && dlgForm.item_remarks ){
    	if ( dlgForm.status.value == 'R' && dlgForm.item_remarks.value.trim() == ""){
			var msg=getString('js.stores.procurement.enterremarks.item.rejected');
			alert(msg);
			dlgForm.item_remarks.focus();
			return false;
		}
	}
	
	var currentDiscountPer = dlgForm.discount_per.value?parseFloat(dlgForm.discount_per.value):0;
	var currentDiscountedRate = dlgForm.cost_price_display.value?parseFloat(dlgForm.cost_price_display.value) * ((100 - currentDiscountPer) / 100):0;

	if(dlgForm.discounted_min_rate.value && parseFloat(dlgForm.discounted_min_rate.value) < currentDiscountedRate && parseFloat(dlgForm.min_rate.value) != parseFloat(dlgForm.discounted_min_rate_value) ){
		var message = getString("js.stores.procurement.supplier.contract.with.less.discounted.rate", dlgForm.medicine_name.value, parseFloat(dlgForm.discounted_min_rate.value).toFixed(decDigits), dlgForm.min_rate_suppliers.value);
		alert(message);
	}else if(dlgForm.min_rate.value && parseFloat(dlgForm.min_rate.value) < currentDiscountedRate){
		var message = getString("js.stores.procurement.supplier.contract.with.less.rate", dlgForm.medicine_name.value, parseFloat(dlgForm.min_rate.value).toFixed(decDigits), dlgForm.min_rate_suppliers.value);
		alert(message);
	}

	var margin = parseFloat(dlgForm.margin.value);
	var marginType = dlgForm.margin_type.value;
	var displayMrp = parseFloat(dlgForm.mrp_display.value);
	var qty = parseFloat(dlgForm.qty_req_display.value);
	var bonusQty = parseFloat(dlgForm.bonus_qty_req_display.value);
	var unitTax = bonusQty || qty ? parseFloat(dlgForm.vat.value)/(bonusQty + qty):0;

 	if(margin || margin === 0){
		if(marginType == 'A' && (displayMrp - margin) < currentDiscountedRate + unitTax){
			alert(getString('js.stores.procurement.margin.not.honored', dlgForm.medicine_name.value, margin, margin));
			return false;
		}else if(marginType == 'P' && ((100 - margin) / 100) * displayMrp < currentDiscountedRate + unitTax){
			alert(getString('js.stores.procurement.margin.not.honored', dlgForm.medicine_name.value, margin + '%', margin + '%'));
			return false;
		}
	}
 	//Validating the total item amount with mrp
 	var discount = getAmount(dlgForm.discount.value);
 	var vat = getAmount(dlgForm.vat.value);
 	var item_ced = getAmount(dlgForm.item_ced.value);	
	if (applyCpValidationForPo == 'B' || applyCpValidationForPo == 'W') {
      var itemAmt = getPaise(costPrice * qty - discount + vat + item_ced);
      var avgcp =  (itemAmt / (qty + bonusQty));
      if ((mrp < avgcp) && (billable == 'true')) {
        var msg=getString("js.stores.procurement.itemamtlessthanmrp");
            msg+=" ";
            msg +=getString("js.stores.procurement.mrpis");
            msg+=" ";
            msg+= getPaiseReverse(mrp);
            msg +=getString("js.stores.procurement.avgcpis");
            msg+=" ";
            msg += getPaiseReverse(avgcp);
            msg+=" ";
            alert(msg);
            dlgForm.cost_price_display.focus();
            if (applyCpValidationForPo == 'B') {
              return false;
            } else if (applyCpValidationForPo == 'W') {
              return true;
            }
      }
	}
    return true;
}

function validateMaxCostPrice(medicineId, cp, maximumCostPrice) {
	var maxCostPrice = null;
   
    if(dlgForm.supplier_rate_validation.value == "true"){
		   maxCostPrice = parseFloat(dlgForm.supplier_rate_val.value).toFixed(decDigits);
	 } else{
		 maxCostPrice = parseFloat(maximumCostPrice);
		 if (qtyUnitSelection () == 'I')
 		   maxCostPrice = parseFloat(maxCostPrice/dlgForm.po_pkg_size.value).toFixed(decDigits); 
	 }

    if (maxCostPrice != null && parseFloat(cp) > maxCostPrice) {
    	if(dlgForm.supplier_rate_validation.value == "true"){
    		alert("The Rate defined as per Supplier Rate Contracts is " + maxCostPrice);
    		return false;
    	}else{
    		if (editMaxCP == 'A' || gRoleId == 1 || gRoleId == 2) {
    			if (!confirm("Cost price  "+cp +" exceed to your max cost price: " + maxCostPrice + "\n" +
    						"Do you want to continue?")) {
    				return false;
    			}
    		} else {
    			var msg=getString("js.stores.procurement.costprice");
    			msg+=" ";
    			msg+=cp;
    			msg+=" ";
    			msg +=getString("js.stores.procurement.notexceedt.yourmaxcostprice");
    			msg+=" ";
    			msg+= maxCostPrice;
    			alert(msg);
    			dlgForm.cost_price_display.focus();
    			return false;
    		}
    	}
		
	}

    return true;
}

function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		getTaxDetailsOnChange('dlgForm');
		onDialogSave();
		return false;
	}
}

function addAdditionalItems() {
	var existItems = [];
	var existIndexes = [];
	addItemsFromStkRed = true;
	var formRowData = [];
	var formData;
	var poHeaderData = {};
	for (var i=0; i<jAdditionalItems.length; i++) {
		var item = jAdditionalItems[i];
		var existIndex = itemIndexInGrid(item.medicine_id);
		if (existIndex != null) {
			existItems.push(item);
			existIndexes.push(existIndex);
			continue;
		}
		var index = addRow();
		var row = getItemRow(index);
		/*
		 * Copy the item based on same name to hidden values in row.
		 */
		objectToHidden(item, row);
		item.store_package_uom = 'I';
		item.qty = getAdditionalQuantityRounded(item);
		// some names don't match, we have to do these explicitly.
		var discAmt = 0;
		var disPer = 0;
		if(item.discount != 0 && typeof(item.discount) != 'undefined' && isNotNullObj(item.qty) && isNotNullObj(item.discount) && isNotNullObj(item.cost_price)) {
			disPer = item.discount;
			discAmt = calcDiscountAmt(item.cost_price, (item.qty/item.issue_base_unit), item.discount);
			item.discount = discAmt;
		}
		formRowData.push(objectToFormData(item));
		
		getElementByName(row, "vat_rate").value = 0;
		getElementByName(row, "vat_type").value = item.tax_type;
		getElementByName(row, "qty_req").value = getAdditionalQuantityRounded(item);
		getElementByName(row, "bonus_qty_req").value = 0;
		getElementByName(row, "discount").value = discAmt;
		getElementByName(row, "discount_per").value = disPer;
		getElementByName(row, "item_ced").value = 0;
		getElementByName(row, "item_ced_per").value = 0;
		getElementByName(row, "po_pkg_size").value = item.issue_base_unit;
		getElementByName(row, "_deleted").value = "false";
		calcRowValues(row, 'stored');
		rowHiddenToLabels(row, gColIndexes);
		YAHOO.util.Dom.addClass(row, 'added');
	}
	if (existItems.length > 0) {
		var ok = confirm("Some items already existing in the PO. The reorder quantities will " +
				" be added to the exsting PO quantities. Proceed?");
		if (!ok) return;

		for (var i=0; i< existItems.length; i++) {
			var item = existItems[i];
			var index = existIndexes[i];
			var row = getItemRow(index);

			// add the quantity only, nothing more to do.
			var qty = getRowAmount(row, 'qty_req');
			qty += getAdditionalQuantityRounded(item);
			item.qty = qty;
			item.store_package_uom = 'I';
			formRowData.push(objectToFormData(item));
			setRowQty(row, 'qty_req', qty);
			calcRowValues(row, 'stored');
			rowHiddenToLabels(row, gColIndexes);
			YAHOO.util.Dom.addClass(row, 'edited');
		}
	}
	
	var poHeader = {
			store_id : poForm.store_id.value,
			supplier_id : poForm.supplier_id.value,
			po_qty_unit : poForm.po_qty_unit.value
		};
	formData = objectToFormData(poHeader);
	formData = formData + '&' + formRowData.join('&');
	var resp = formDataPost(formData, cpath + "/purchaseorder/itemtaxdetails.json", true);
	var itemListTable = document.getElementById("medtabel");
	var numRows = getNumItems();
	if(resp && resp != null && resp.mapList && resp.mapList != null) {
		for(var i=0; i < resp.mapList.length; i++) {
			var taxObj = resp.mapList[i];
			if(taxObj.adj_price && taxObj.adj_price != null && taxObj.adj_price != 'null' && taxObj.adj_price != 'undefined') {
				//Set tax details in hidden fields
				var taxMap = taxObj.tax_details;
				if(taxMap && taxMap != null && taxMap != 'undefined' && taxMap != 'null') {
					for (var l=0; l<numRows; l++) {
						var rowObj = getItemRow(l);
						if(getElementByName(rowObj,'medicine_id').value && getElementByName(rowObj,'medicine_id').value != '' 
							&& getElementByName(rowObj,'medicine_id').value == taxObj.medicine_id) {
							
							getElementByName(rowObj,'adj_mrp').value = taxObj.adj_price;
							getElementByName(rowObj,'adj_mrp_display').value = taxObj.adj_price;
							
							// Set Tax Split details.
							var vatRate = 0;
							var vatAmt = 0;
							for(var k=0; k<taxMap.length; k++) {
							    for(var j=0; j < subgroupNamesList.length; j++) {
							    	if(taxMap[k] && taxMap[k][subgroupNamesList[j].item_subgroup_id] && taxMap[k][subgroupNamesList[j].item_subgroup_id] != null) {
							    		var val = taxMap[k][subgroupNamesList[j].item_subgroup_id];
							    		var itemGroupId = subgroupNamesList[j].item_group_id;
							    		
							    		getElementByName(rowObj,'taxrate'+itemGroupId).value = parseFloat(val.rate).toFixed(decDigits);
							    		getElementByName(rowObj,'taxamount'+itemGroupId).value = parseFloat(val.amount).toFixed(decDigits);
							    		getElementByName(rowObj,'taxsubgroupid'+itemGroupId).value = subgroupNamesList[j].item_subgroup_id;
										
										vatAmt += parseFloat(parseFloat(val.amount).toFixed(decDigits));
									    vatRate = vatRate + parseFloat(val.rate);
									    break;
							    	}
								}
							}
							//End
							
							getElementByName(rowObj,'tax_rate').value = vatRate;
							getElementByName(rowObj,'vat_rate').value = vatRate;
							getElementByName(rowObj,'vat').value = vatAmt;
							getElementByName(rowObj,'adj_mrp_display').value = taxObj.adj_price;
							
							calcRowValues(rowObj, 'stored');
							rowHiddenToLabels(rowObj, gColIndexes);
						}
						
					}
				}
			}
			
		}
	}
	
	resetTotals();
}

/*
 * Gets the additional quantity: this is rounded UP to the nearest highest
 * package quantity in case the qtySelection is package.
 */
function getAdditionalQuantityRounded(item) {
	var qty = jAdditionalQtys[item.medicine_id];
	if (qtyUnitSelection() != 'I') {
		var pkgQty = Math.ceil(qty/item.issue_base_unit);
		qty = pkgQty * item.issue_base_unit;
	}
	return qty;
}

function itemIndexInGrid(medId) {
	var len = getNumItems();
	for (var k=0; k<len; k++) {
		var rowObj = getItemRow(k);
		if (medId == getElementByName(rowObj,"medicine_id").value)
			return k;
	}
	return null;
}

/*
 * Add a new row, copy the dialog values to the grid in the new row.
 */
function addDialogToGrid() {
	var id = addRow();
	var row = getItemRow(id);
	gRowItems[id] = formatPaise(gDialogItem);
	gDialogItem = null;
	YAHOO.util.Dom.addClass(row, 'added');
	dialogToGrid(getItemRow(id));
	resetTotals();
}

function addRow() {
	var id = getNumItems();
	var table = document.getElementById("medtabel");
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.id="itmRow"+id;
	return id;
}

/*
 * Copy the current dialog form elements to the given row. The row hidden values
 * is the "master", ie, contains a superset of all values required in the row display,
 * dialog and form submit. This is different from StockEntry model, where there is
 * a javascript array that is the master. Eventually, it is good to convert this
 * also to the javascript array model for consistency.
 */
function dialogToGrid(row) {
	/*
	 * First, copy form values to hidden fields in the row. Even for display-only fields
	 * (ie, those that are not saved like Package Type, Bin etc. we need hidden variables
	 * in the row as well as the dialog. This ensures that an edit carries it over neatly to the
	 * dialog and back. Otherwise, we need to keep track that some are labels only and treat
	 * them differently between an edit (no change to label) vs. Add (set the label)
	 */
	formToHidden(dlgForm, row);
	if ( nextPOstatus == 'A' || nextPOstatus == 'AA' ) {
		getElementByName(row,"status_ar").value = dlgForm.status.value;
		var flagImg = row.cells[gColIndexes.medicine_name].getElementsByTagName("img")[0];
		flagImg.src = cpath+"/images/"+(dlgForm.status.value == "A" ? "green" : (dlgForm.status.value == 'R' ? "red" : "empty" ))+"_flag.gif";
	}

	/*
	 * Now, do some extra calculations: this sets additional hidden variables in the row
	 * which may not be there in the dialog, eg, totals and display values.
	 * This is useful when the row may be populated without a dialog, ie, pre-loaded
	 * (eg, from stock-reorder or edit po). Thus, the row-hidden fields is the superset of fields.
	 */
	calcRowValues(row, 'display');

	/*
	 * Make row labels from the hidden values in the row.
	 */
	rowHiddenToLabels(row, gColIndexes);

//	if( taxSelection() == 'VAT' ) {
//		if ( gColIndexes.vat_type ) {
//			var taxType = row.cells[gColIndexes.vat_type].textContent;
//			row.cells[gColIndexes['vat_type']].title = ( taxType == 'MB'
//																? "MRP Based(with bonus)" : (
//														 taxType == 'M'
//														 		? 'MRP Based(without bonus)' : (
//												 		 taxType == 'C' ?  'CP Based(without bonus)' : 'CP Based(with bonus)')));
//		}
//	} else if( (taxSelection() == 'CST' || taxSelection() == 'iGST') && gColIndexes.vat_type ) {
//		if ( itemLevelCSTApplicable ){
//			row.cells[gColIndexes.vat_type].textContent = '';
//			row.cells[gColIndexes['vat_type']].title = '';
//		} else {
//			row.cells[gColIndexes.vat_type].textContent = '';
//			row.cells[gColIndexes['vat_type']].title = '';
//			row.cells[gColIndexes['vat_rate']].textContent = getRowAmount(row,'cst_rate');
//		}
//	}
	
	getElementByName(row,"supplier_rate_validation").value = dlgForm.supplier_rate_validation.value;
	getElementByName(row,"supplier_rate_val").value = dlgForm.supplier_rate_val.value;
	getElementByName(row,"adj_mrp").value = dlgForm.adj_mrp.value;
	getElementByName(row,"adj_mrp_display").value = dlgForm.adj_mrp.value;
	getElementByName(row,"vat_rate").value = dlgForm.tax_rate.value;
	row.cells[gColIndexes.adj_mrp_display].textContent = dlgForm.adj_mrp.value;
	row.cells[gColIndexes.vat_rate].textContent = dlgForm.tax_rate.value;
	getElementByName(row,"vat_type").value = dlgForm.vat_type.value;
	row.cells[gColIndexes.vat_type].textContent = dlgForm.vat_type.value;
}

function getNumItems() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("medtabel").rows.length-2;
}
function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumItems() + 1;
}
function getItemRow(i) {
	i = parseInt(i);
	var table = document.getElementById("medtabel");
	return table.rows[i + getFirstItemRow()];
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function resetTotals(){
	var totalNoOfRows;
	totalNoOfRows = getNumItems();

	var itemDiscounts = 0;
	var itemTotal = 0;
	var itemTaxes = 0;
	var poDiscount = 0;
	var poTcsAmount = 0;
	var doRoundOff = poForm.round_off_val ? poForm.round_off_val.checked : false;
	
	for(var j=0; j < groupListJSON.length; j++) {
	 	setFieldHtml("taxamtlabel_", "0", groupListJSON[j].item_group_id);
	}

	for (var i=0;i<totalNoOfRows;i++) {
		rowObj = getItemRow(i)
		if (getElementByName(rowObj,'_deleted').value == 'true')
			continue;

		itemDiscounts += getRowPaise(rowObj, "discount");
		itemTotal += getRowPaise(rowObj, "med_total");
		itemTaxes += getRowPaise(rowObj, "vat");
		
		for(var j=0; j < groupListJSON.length; j++) {
			var itemGroupId = groupListJSON[j].item_group_id;
			if(getElementByName(rowObj,"taxamount"+itemGroupId).value){
				var taxAmt = parseFloat(getElementByName(rowObj,"taxamount"+itemGroupId).value);
				var existTotalTaxAmt = parseFloat(getFieldHtml("taxamtlabel_", itemGroupId));
				setFieldHtml("taxamtlabel_", parseFloat(existTotalTaxAmt + taxAmt).toFixed(decDigits), itemGroupId);
			}
		}
		 
	}

	var transportationCharges = getElementPaise(poForm.transportation_charges);
	
	itemTotal = itemTotal + transportationCharges;
    setLabel('lblItemDiscounts', formatAmountPaise(itemDiscounts, false));
	setLabel('lblTotalTaxes', formatAmountPaise(itemTaxes, false));
    setLabel('lblItemTotal', formatAmountPaise(itemTotal, false));

	var discType = poForm.discount_type.value;
	if (discType == 'P') {
		var discountPer = getAmount(poForm.discount_val.value);
		poForm.po_discount_per.value = discountPer;
		poDiscount = (discountPer * itemTotal) / 100;
	} else {
		poDiscount = getElementPaise(poForm.discount_val);
	}
	var discountText = formatAmountPaise(poDiscount);
	poForm.po_discount.value = discountText;
	setLabel('lblPODiscount', discountText);
	
	var tcsType = poForm.tcs_type.value;
	if (tcsType == 'P') {
		var tcsPer = parseFloat(poForm.tcs_value.value);
		if(isNaN(tcsPer)) {
			poForm.po_tcs_per.value = 0;
			poTcsAmount = 0;
		} else {
			poForm.po_tcs_per.value = tcsPer;
			poTcsAmount = (tcsPer * itemTotal) / 100;
		}
	} else {
		poTcsAmount = getElementPaise(poForm.tcs_value);
	}
	var tcsText = formatAmountPaise(poTcsAmount);
	poForm.po_tcs_amount.value = tcsText;
	setLabel('lblPOTcsAmount', tcsText);

	var roundOffPaise = autoOrManulaRoundoff == 'A' ? 0 : getElementPaise(poForm.round_off);;
	
	if(doRoundOff) {
		poForm.round_off_flag.value='Y';
		roundOffPaise = getRoundOffPaise(itemTotal - poDiscount + poTcsAmount);
	} else {
		poForm.round_off_flag.value='N';
	}
	poForm.round_off.value=formatAmountPaise(roundOffPaise);
	var grandTotalPaise = itemTotal - poDiscount + poTcsAmount + roundOffPaise;

    var grandTotalText = formatAmountPaise(grandTotalPaise);
    poForm.po_total.value = grandTotalText;
	setLabel('lblGrandTotal', grandTotalText);
}

function setLabel(labelId, value) {
	var label = document.getElementById(labelId);
	label.textContent = '';
	label.innerHTML = value;
}

function onSupplierTermTemplatesChange() {
	var templateCode = poForm.supplierTermTemplates.value;

	var url= cpath+"/pages/stores/poscreen.do?_method=getTemplateText" +
		"&templateCode="+templateCode;

	YAHOO.util.Connect.asyncRequest('GET', url, { success:
		function(r) { poForm.supplier_terms.value = r.responseText }
	});
}

function onDeliveryInstructionTemplatesChange() {
	var templateCode = poForm.deliveryInstructionTemplates.value;

	var url= cpath+"/pages/stores/poscreen.do?_method=getTemplateText" +
		"&templateCode="+templateCode;

	YAHOO.util.Connect.asyncRequest('GET', url, { success:
		function(r) { poForm.delivery_instructions.value = r.responseText }
	});
}

function saveAndPrintPO() {
	poForm._printAfterSave.value = 'Y';
	if ( !savePO() ) return false;;
	submitSave();
	poForm._printAfterSave.value = 'N';
}

function saveAndSetStatusPO(status){
	if ( ( status == 'A' || status == 'AA' ) && userpoApprovalLimit != 0 && getElementPaise(poForm.po_total) > getPaise(userpoApprovalLimit) ){
		showMessage("js.stores.procurement.can.not.approve");
		return false;
	}

	var itemListTable = document.getElementById("medtabel");
	var numRows = getNumItems();
	var itemApprovedrRejected = false;

	if ( !savePO() ) return false;
	
	for (var l=0; l<numRows; l++) {
		var rowObj = getItemRow(l);
		if (getElementByName(rowObj,'_deleted').value == 'true')
			continue;


		itemApprovedrRejected = itemApprovedrRejected && ( getElementByName(rowObj, 'status_ar').value == 'A' || getElementByName(rowObj, 'status_ar').value == 'R' );
	}
	//approval validation
	if ( status == 'A' || status == 'AA' ) {
		if ( !alertUsrApproval(itemApprovedrRejected) )
			return false;
	}
	
	setPOStatus(status);
	submitSave();
}

function savePO() {
	var length = getTableRowsWithNoDeletedRows();
	if (length < 1) {
		showMessage("js.stores.procurement.norowsingrid");
		return false;
	}
	// HMS-17268
	if (!doValidateDateField(poForm.po_date, 'past')) {
		return false;
	}
	// HMS-17268 Ends here
	if (document.getElementById("delivery_date").value != "") {
		var podate = getDatePart(parseDateStr(poForm.po_date.value));
		var deldate = getDatePart(parseDateStr(document.getElementById("delivery_date").value));
		if (daysDiff(podate, deldate) < 0) {
			showMessage("js.stores.procurement.deliverydates.greater.equaltopodate");
			document.getElementById("delivery_date").focus();
			return false;
		}
	}
	setPOStatus(existingPOstatus);
	
	if (/(AO|AV|AA)/gi.test(nextPOstatus) ) {
		if(existingPOstatus != 'AV')
		setPOStatus("AO");
	}
	
	var allChecked = false;
	var itemListTable = document.getElementById("medtabel");
	var numRows = getNumItems();

	// create an item count map for checking duplicates
	var itemCount = {};
	for (var k=0; k<numRows; k++) {
		var rowObj = getItemRow(k);
		var medId = getElementByName(rowObj, 'medicine_id').value;
		if (itemCount[medId] == undefined)
			itemCount[medId] = 1;
		else
			itemCount[medId]++;
	}

	var activeRows = 0;
	for (var l=0; l<numRows; l++) {
		var rowObj = getItemRow(l);
		if (getElementByName(rowObj,'_deleted').value == 'true')
			continue;

		activeRows++;
		var medId = getElementByName(rowObj, 'medicine_id').value;
		var itemName = getElementByName(rowObj, 'medicine_name').value;


		if (itemCount[medId] > 1) {
			var msg=itemName;
			msg+=getString("js.stores.procurement.hasduplicateentries");
			alert(msg);
			return false;
		}

		var rate = getRowPaise(rowObj,'cost_price_display');
		if (rate == 0) {
			var msg=itemName;
			msg +=getString("js.stores.procurement.rateisrequired");
			alert(msg);
			openEditDialogBox(rowObj);
			dlgForm.cost_price_display.focus();
			return false;
		}

		// Dialog validation is not enough because rows may be added by Stock Reorder

		var billable =  getElementByName(rowObj, 'billable').value;
		var mrp =  getRowPaise(rowObj,'mrp');
		if ((billable == 'true') && (mrp < rate)) {
			var msg=getString("js.stores.procurement.mrpgreaterthanrate");
			msg+=" ";
			msg +=getString("js.stores.procurement.mrpis");
			msg+=" ";
			msg+= getPaiseReverse(mrp) ;
			msg+=getString("js.stores.procurement.rateis");
			msg+=" ";
			msg+= getPaiseReverse(rate);
			msg+=" ";
			alert(msg);
			openEditDialogBox(rowObj);
			dlgForm.cost_price_display.focus();
			return false;
		}
		if (itemLevelCSTApplicable && cFormChkCount != 0) {
			setRowPaise(rowObj, 'cost_price', getElementByName(rowObj, 'cost_price').value);
		}
	}



    var poTotalPaise = getElementPaise(poForm.po_total);
	if (poTotalPaise < 0) {
		showMessage("js.stores.procurement.pototalvalueisnegative");
		return false;
	}

	if (!validateLength(poForm.supplier_terms, 3900, getString("js.stores.procurement.supplierterms")))
		return false;
	if (!validateLength(poForm.hospital_terms, 3900, getString("js.stores.procurement.hospitalterms")))
		return false;
	if (!validateLength(poForm.delivery_instructions, 3900, getString("js.stores.procurement.deliveryinstructions")))
		return false;

	if (poForm.round_off.value == '') {
		poForm.round_off.value = '0.00';
	}

	if ( poForm.status_fld && poForm.status_fld.value == 'FC' )
		setPOStatus();
	
	if ( !checkAmendedReason() ) {//after approval status amendemetnt reason is mandatory
		return false;
	}
	
	return true;
	
}

function setPOStatus(status){
	poForm.status.value = ( poForm.status_fld && poForm.status_fld.value == 'FC') ? 'FC' : status;
}
function printPO() {
	var printUrl= cpath+"/pages/stores/poscreen.do?_method=generatePOprint&poNo="+poForm.po_no.value +
		"&printType="+poForm.printType.value + "&temp_name="+poForm.template_name.value;
	window.open(printUrl);
}

function openAddDialog() {
	clearTaxFields();
	clearTaxSelection();
	dlgForm.adj_mrp.value = 0;
	dlgForm.store_package_uom.value = 'P';
	setSelectedIndex(dlgForm.vat_type, 0);
	// supplier and store are required to get the item details.
	if (poForm.supplier_name.value == '' || poForm.supplier_id.value == '') {
		poForm.supplier_name.focus();
		showMessage("js.stores.procurement.selectsupplier");
		return false;
	}

	if (poForm.store_id.value == '') {
		showMessage("js.stores.procurement.selectstore");
		poForm.store_id.focus();
		return false;
	}

	refreshItemMaster();
	resetDetails();

	gRowUnderEdit = -1;
	document.getElementById("prevDialog").disabled = true;
	document.getElementById("nextDialog").disabled = true;
	if (prefBarCode == 'Y') dlgForm.item_barcode_id.value = '';
	if (prefBarCode == 'Y') dlgForm.item_barcode_id.readOnly = false;
	if (prefBarCode == 'Y') setTimeout("dlgForm.item_barcode_id.focus()", 100);
	else setTimeout("dlgForm.medicine_name.focus()", 100);
	button = document.getElementById("plusItem");
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);
	if (!onlyView) detaildialog.show();
}

function initDialog() {
	detaildialog = new YAHOO.widget.Dialog("detaildialog",
	{
		width:"770px",
		context : ["plusItem", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true
	} );

	var escKeyListener = new YAHOO.util.KeyListener("detaildialog", { keys:27 }, onDialogCancel);
	detaildialog.cfg.queueProperty("keylisteners", escKeyListener);
	//dialog.setHeader("Add Item");
	detaildialog.render();
}

function onDialogCancel() {
	detaildialog.cancel();
	if (gRowUnderEdit != -1) {
		var row = getItemRow(gRowUnderEdit);
		YAHOO.util.Dom.removeClass(row, 'editing');
	}
	if (!onlyView)
		poForm.btnSavePo.focus();
}

function deleteAllRows() {
	var len = getNumItems();
	for (var p=len; p>=1; p--){
		document.getElementById("medtabel").deleteRow(p);
	}

	//poForm.round_off.value = '0';
	poForm.discount_val.value = '0';

	resetTotals();
}

function setRowQty(row, name, qty) {
	setElementAmount(getElementByName(row, name), qty, qtyDecimal != 'Y');
}

/*
 * Common item amounts calculation based on other params supplied.
 * vat and total are always calculated.
 * Discount/ced amounts are calculated if 0, otherwise, the perecentage is reverse calculated
 * All amounts are in paise.
 */
function calcItemValues(item) {
	//item.vat = 0;
	//item.adj_mrp = (taxSelection() == 'CST' || taxSelection() == 'iGST') ? item.mrp : Math.round(item.mrp/(1 + item.vat_rate/100));
	if(item.vat == undefined) {
		item.vat = 0;
	}
	item.adj_mrp =  parseFloat(item.mrp/(1 + item.vat_rate/100)).toFixed(decDigits);
	if (item.discount_per != 0 && item.discount == 0 && item.qty_req > 0) {
		// calc discount
		item.discount = item.cost_price * item.qty_req/item.po_pkg_size * item.discount_per / 100;
		item.discount_changed = true;
	}

	if (item.discount != 0 && item.discount_per == 0 && item.qty_req > 0) {
		// reverse calc discount per
		item.discount_per = item.discount * 100 / (item.cost_price * item.qty_req/item.po_pkg_size );
		item.discount_per_changed = true;
	}

	/*if (item.item_ced_per != 0 && item.item_ced == 0) {
		// calc ced
		item.item_ced = (item.cost_price * item.qty_req/item.po_pkg_size - item.discount)
			* item.item_ced_per / 100;
		item.item_ced_changed = true;
	}

	if (item.item_ced != 0 && item.item_ced_per == 0) {
		// reverse calc ced per
		item.item_ced_per = item.item_ced * 100 / (item.cost_price * item.qty_req/item.po_pkg_size
				- item.discount);
		item.item_ced_per_changed = true;
	}*/
//	if (taxSelection() == 'CST' || taxSelection() == 'iGST') {
			/*
			 * In case of CST, the tax_type and tax_rate are the outgoing tax type/rate only.
			 * The incoming tax is based only on the global CST rate, CP-based. CST needs to be
			 * calculated post discount.
			 */
			/*var cstRate = (taxSelection() == 'CST' || taxSelection() == 'iGST') ? getAmount(item.cst_rate) : getAmount(item.vat_rate);
			if ( itemLevelCSTApplicable ){
				if (poForm.c_form.checked && item.vat_rate >2) {
					item.master_tax_rate = parseFloat(2).toFixed(2);
				} else {
					item.master_tax_rate = item.vat_rate;
				}
				cstRate = item.master_tax_rate;
	        } 
	        item.vat = Math.round((item.cost_price*item.qty_req/item.po_pkg_size - item.discount + item.item_ced) * cstRate / 100);
			item.bonus_tax_paise = 0;

		} else {*/
			// VAT
			//form 8h purchase order will not have tax applied on billed qty
			//var qtyReq = (form8Hcal && form8HApplicable) ? 0 : item.qty_req;
			/*if (item.vat_type == 'M') {
				item.vat = Math.round(item.adj_mrp * qtyReq / item.po_pkg_size
						* item.vat_rate/100 );
			} else if (item.vat_type == 'MB') {
				item.vat = Math.round(item.adj_mrp * (qtyReq + item.bonus_qty_req) / item.po_pkg_size
						* item.vat_rate / 100 );
			} else if (item.vat_type == 'C') {
				item.vat = Math.round((item.cost_price*qtyReq/item.po_pkg_size -
							item.discount+item.item_ced) * item.vat_rate / 100);
			}else if (item.vat_type == 'CB') {
				item.vat = Math.round((item.cost_price*(qtyReq+item.bonus_qty_req)/item.po_pkg_size
							- item.discount) * item.vat_rate / 100);
			}*/
	//}
	if(item.vat == 0){
			for(var i=0; i < groupListJSON.length; i++) {
				if(isNotNullValue("taxamount_", groupListJSON[i].item_group_id)){
				item.vat = parseFloat(item.vat) + parseFloat(getFieldValue("taxamount_", groupListJSON[i].item_group_id));
			}
		}
		item.vat = getPaise(item.vat);
	}

	item.med_total = item.cost_price * item.qty_req / item.po_pkg_size
		- item.discount + item.vat + item.item_ced;
}

/*
 * Update some calculated row values: especially used when loading up
 * the rows not from dialog, as in loading it from a list of stock reorder items.
 */
function calcRowValues(row, valueType) {
	var item = {};

	item.po_pkg_size = getRowAmount(row, 'po_pkg_size');

	if (valueType == 'stored') {
		//  row contains stored values. Display values to be calculated, eg, when loading during edit PO
		item.cost_price = getRowPaise(row, 'cost_price');
		item.mrp = getRowPaise(row, 'mrp');
		item.qty_req = getRowAmount(row, 'qty_req');
		item.bonus_qty_req = getRowAmount(row, 'bonus_qty_req');
		item.cst_rate = getRowAmount(row,'cst_rate');
		item.vat_rate = getRowAmount(row,'vat_rate');
		item.vat_type = getElementByName(row, 'vat_type').value;
		item.master_tax_rate = getRowAmount(row,'vat_rate');

		calcItemDisplayValues(item);

		setRowPaise(row, 'cost_price_display', item.cost_price_display);
		setRowPaise(row, 'mrp_display', item.mrp_display);
		setRowQty(row, 'qty_req_display', item.qty_req_display);
		setRowQty(row, 'bonus_qty_req_display', item.bonus_qty_req_display);
		if ( itemLevelCSTApplicable && addItemsFromStkRed ){
			item.medicine_name = getElementByName(row, "medicine_name").value;
			item.medicine_id = getElementByName(row, "medicine_id").value;
			item.package_uom = getElementByName(row, "package_uom").value;
			item.uom_display = getElementByName(row, "package_uom").value;
			item.po_pkg_size = getElementByName(row, "po_pkg_size").value;
			item.cost_price_display = formatAmountPaise(item.cost_price);
			item.mrp_display = formatAmountPaise(item.mrp);
			item.tax_rate = item.vat_rate;
		}
		item.vat = getRowPaise(row, 'vat');

	} else {
		// vice-versa: display values exist, eg, updating from dialog to row
		item.cost_price_display = getRowPaise(row, 'cost_price_display');
		item.mrp_display = getRowPaise(row, 'mrp_display');
		item.qty_req_display = getRowAmount(row, 'qty_req_display');
		item.bonus_qty_req_display = getRowAmount(row, 'bonus_qty_req_display');
		item.cst_rate = getRowAmount(row,'cst_rate');
		item.vat_rate = getRowAmount(row,'vat_rate');
		item.vat_type = getElementByName(row, 'vat_type').value;

		calcItemStoredValues(item);

		setRowPaise(row, 'cost_price', item.cost_price);
		setRowPaise(row, 'mrp', item.mrp);
		setRowQty(row, 'qty_req', item.qty_req);
		setRowQty(row, 'bonus_qty_req', item.bonus_qty_req);

	}

	item.discount = getRowPaise(row, 'discount');
	item.discount_per = getRowAmount(row, 'discount_per');
	item.item_ced = getRowPaise(row, 'item_ced');
	item.item_ced_per = getRowAmount(row, 'item_ced_per');
	setRowAmount(row, 'vat_rate', item.vat_rate);
	setRowAmount(row, 'cst_rate', item.cst_rate);
 	getElementByName(row, 'vat_type').value = item.vat_type;

	calcItemValues(item);
	
	if ( itemLevelCSTApplicable && addItemsFromStkRed ){
		var gReorderItem = {};
		//calcItemValues(item);
		shallowCopy(item, gReorderItem);
		gReorderItem.mrp = formatAmountPaise(gReorderItem.mrp);
		gRowItems[getRowItemIndex(row)] = formatPaise(gReorderItem);
		gReorderItem = null;
 	}

	// set the calculated values back into the row
	setRowPaise(row, 'vat', item.vat);
	setRowPaise(row, 'adj_mrp', item.adj_mrp);
	setRowPaise(row, 'adj_mrp_display', rateDisplay(item.adj_mrp, item.po_pkg_size));
	setRowPaise(row, 'med_total', item.med_total);

	getElementByName(row, 'uom_display').value = qtyUnitSelection() == 'I' ?
		getElementByName(row, 'issue_units').value :
		getElementByName(row, 'package_uom').value;
}

function calcItemDisplayValues(item) {
	item.cost_price_display = rateDisplay(item.cost_price, item.po_pkg_size);
	item.mrp_display = rateDisplay(item.mrp, item.po_pkg_size);
	item.qty_req_display = qtyDisplay(item.qty_req, item.po_pkg_size);
	item.bonus_qty_req_display = qtyDisplay(item.bonus_qty_req, item.po_pkg_size);
}

function calcItemStoredValues(item) {
	item.cost_price = ratePackage(item.cost_price_display, item.po_pkg_size);
	item.mrp = ratePackage(item.mrp_display, item.po_pkg_size);
	item.qty_req = qtyUnit(item.qty_req_display, item.po_pkg_size);
	item.bonus_qty_req = qtyUnit(item.bonus_qty_req_display, item.po_pkg_size);
}

/*
 * Return the actual quantity in issue units based on Package / Issue unit selection
 * Quantities are always stored in issue units.
 */
function qtyUnit(qty, pkgSize) {
	if (qtyUnitSelection() == 'P') {
		return qty * pkgSize;
	} else {
		return qty;
	}
}

/*
 * Return the package rate (mrp/cost price) based on Package / Issue unit selection
 * MRP and CP are always stored in package units.
 */
function ratePackage(rate, pkgSize) {
	if (qtyUnitSelection() == 'I') {
		return rate * pkgSize;
	} else {
		return rate;
	}
}

/*
 * Reverse
 */
function qtyDisplay(qty, pkgSize) {
	if (qtyUnitSelection() == 'P') {
		return qty / pkgSize;
	} else {
		return qty;
	}
}

function rateDisplay(rate, pkgSize) {
	if (qtyUnitSelection() == 'I') {
		return rate / pkgSize;
	} else {
		return rate;
	}
}

/*
 * Called when the dialog elements are changed. We need to re-calculate
 * the discount amount/ced etc. Note that either percentage or amt can
 * be changed
 */
function calcDlgValues () {
	var item = {};

	item.cost_price_display = getElementPaise(dlgForm.cost_price_display);
	item.mrp_display = getElementPaise(dlgForm.mrp_display);
	item.qty_req_display = getElementAmount(dlgForm.qty_req_display);
	item.bonus_qty_req_display = getElementAmount(dlgForm.bonus_qty_req_display);
	item.po_pkg_size = getElementAmount(dlgForm.po_pkg_size);

	item.discount = getElementPaise(dlgForm.discount);
	item.discount_per = getElementAmount(dlgForm.discount_per);
	item.item_ced = getElementPaise(dlgForm.item_ced);
	item.item_ced_per = getElementAmount(dlgForm.item_ced_per);
	item.vat_rate = getElementAmount(dlgForm.vat_rate);
//	if(taxSelection() == 'CST' || taxSelection() == 'iGST')
//		item.vat_type = '';
//	else
	item.vat_type = dlgForm.vat_type.value;
	item.cst_rate = dlgForm.cst_rate.value;
	item.master_tax_rate = getElementAmount(dlgForm.vat_rate);
	item.medicine_name = dlgForm.medicine_name.value;
	item.medicine_id = dlgForm.medicine_id.value;
	item.package_uom = dlgForm.package_uom.value;
	item.uom_display = dlgForm.package_uom.value;
	item.tax_rate = getElementAmount(dlgForm.tax_rate);
	
	calcItemStoredValues(item);
	calcItemValues(item);

	if (item.discount_changed)
		setElementPaise(dlgForm.discount, item.discount);
	if (item.discount_per_changed)
		setElementAmount(dlgForm.discount_per, item.discount_per);

	if (item.item_ced_changed)
		setElementPaise(dlgForm.item_ced, item.item_ced);
	if (item.item_ced_per_changed)
		setElementAmount(dlgForm.item_ced_per, item.item_ced_per);

	// vat and med_total are not used in the dialog.
	
	gDialogItem = {};
	item.cost_price_display = formatAmountPaise(item.cost_price_display);
	item.cost_price = formatAmountPaise(item.cost_price);
	item.mrp = formatAmountPaise(item.mrp);
	item.mrp_display = formatAmountPaise(item.mrp_display);
	
	shallowCopy(item, gDialogItem);
}

function onChangeQty() {
	formatAmountObj(dlgForm.qty_req_display, qtyDecimal);
	dlgForm.discount.value = '0';	// force recalc of discount based on percent
	dlgForm.item_ced.value = '0';	// force recalc of ced based on percent
	calcDlgValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeTaxPer() {
	calcDlgValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeBonusQty() {
	formatAmountObj(dlgForm.bonus_req_qty);
	calcDlgValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeCostPrice() {
	formatAmountObj(dlgForm.cost_price_display);
	dlgForm.discount.value = '0';	// force recalc of discount based on percent
	dlgForm.item_ced.value = '0';	// force recalc of ced based on percent
	getSupplierRateValue();
	calcDlgValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeMrp() {
	formatAmountObj(dlgForm.mrp_display);
	calcDlgValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeTaxType() {
	calcDlgValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeTaxRate() {
	calcDlgValues();
}

function onChangeDiscountPer () {
	if((isNotNullFormObj(dlgForm.qty_req_display) && dlgForm.qty_req_display.value == 0 && dlgForm.qty_req_display.value == '0') && 
			(isNotNullFormObj(dlgForm.bonus_qty_req_display) && dlgForm.bonus_qty_req_display.value == 0 && dlgForm.bonus_qty_req_display.value == '0')) {
		dlgForm.discount_per.value = '0';
		return false;
		
	} else {
		formatAmountObj(dlgForm.discount_per);
		dlgForm.discount.value = '0';
		calcDlgValues();
		getTaxDetailsOnChange('dlgForm');
	}
}

function onChangeDiscountAmt () {
	if((isNotNullFormObj(dlgForm.qty_req_display) && dlgForm.qty_req_display.value == 0 && dlgForm.qty_req_display.value == '0') && 
			(isNotNullFormObj(dlgForm.bonus_qty_req_display) && dlgForm.bonus_qty_req_display.value == 0 && dlgForm.bonus_qty_req_display.value == '0')) {
		dlgForm.discount.value = '0';
		return false;
	} else {
		formatAmountObj(dlgForm.discount);
		dlgForm.discount_per.value = '0';
		calcDlgValues();
		getTaxDetailsOnChange('dlgForm');
	}
}

function onChangeCedPer() {
	formatAmountObj(dlgForm.item_ced_per);
	if((parseFloat(dlgForm.item_ced_per.value,10))>100){
		showMessage("js.stores.procurement.cedvalueshouldbelessthan100");
		dlgForm.item_ced_per.value = 0;
		dlgForm.item_ced_per.focus();
		return false;
	}
	dlgForm.item_ced.value = '0';
	calcDlgValues();
}

function onChangeCedAmt() {
	formatAmountObj(dlgForm.item_ced);
	dlgForm.item_ced_per.value = '0';
	calcDlgValues();
}

function openEditDialogBox(obj) {
	clearTaxFields();
	clearTaxSelection();
	resetDetails();
	dlgForm.store_package_uom.value = 'P';
	var row = findAncestor(obj, "TR");
	YAHOO.util.Dom.addClass(row, 'editing');
	dlgForm.adj_mrp.value = 0;
	updateGridToDialog(row);
	dlgForm.medicine_name.focus();
	
	dlgForm.store_id_hid.value = poForm.store_id.value;
	dlgForm.supplier_code_hid.value = poForm.supplier_id.value;
	for(var i=0; i < groupListJSON.length; i++) {
		 if(groupListJSON[i] != undefined && groupListJSON[i].item_group_id) {
			var p = groupListJSON[i].item_group_id;
			var subgrpId = document.getElementById('ad_taxsubgroupid'+p);
			var selectedSubGrpId = document.getElementById('taxsubgroupid_'+p);
			for(var k, j = 0; k = subgrpId.options[j]; j++) {
			    if(k.value == selectedSubGrpId.value) {
			    	subgrpId.selectedIndex = j;
			        break;
			    }
			}
		}
	}
	//getTaxDetails('dlgForm');
	
}

function updateGridToDialog (rowObj) {

	hiddenToForm(rowObj, dlgForm);
	if ( dlgForm.status )
		setSelectedIndex(dlgForm.status, getElementByName(rowObj,"status_ar").value);

	gRowUnderEdit = getRowItemIndex(rowObj);
	
	oItemAutoComp._bItemSelected = true;
	document.getElementById("prevDialog").disabled = false;
	document.getElementById("nextDialog").disabled = false;

	button = rowObj.cells[EDIT_COL];
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);
	
	editItemAfterAdded();
	document.getElementById("lblvat_rate").innerHTML = document.getElementById("vat_rate").value;
	document.getElementById("lblvat_amt").innerHTML = document.getElementById("vat").value;
	dlgForm.vat_type.value = getElementByName(rowObj,"vat_type").value;
		
	detaildialog.show();
}

function editItemAfterAdded () {
	var medId = dlgForm.medicine_id.value;
	var storeId = poForm.store_id.value;
	var supp = poForm.supplier_id.value;

	var medUrl= cpath+"/pages/stores/poscreen.do?_method=getMedDetails" +
		"&medicineId="+medId + "&storeId="+storeId + "&suppId="+supp+"&supplier_Validation=false";
	YAHOO.util.Connect.asyncRequest('GET', medUrl, { success: handleExistingItemsEdit });
}

/*
 * On save after edit: update the row under edit with values from the dialog.
 */
function updateDialogToGrid () {
	var row = getItemRow(gRowUnderEdit);

	dialogToGrid(row);
	YAHOO.util.Dom.addClass(row, 'edited');
	YAHOO.util.Dom.removeClass(row, 'editing');
	resetTotals();
}

function onDialogPrevNext(doNext) {
	if (!dlgValidate())
		return false;

	dlgSave();
	var gridLen = getNumItems();
	detaildialog.cancel();

	var index;
	if (!doNext) {
		index = gRowUnderEdit-1;
		if (index == -1) return;

	} else {
		index = gRowUnderEdit+1;
		if (index == gridLen || index == '')
			return;
	}
	var rowObj = getItemRow(index);
	clearTaxFields();
	openEditDialogBox(rowObj);
}

// todo: get rid of this and get the details from the row itself. Including Manf, pkgsize etc.
// shown at the bottom of the dialog (?? for edit po, this may not be there? Maybe get details only
// in that case?).

function handleExistingItemsEdit(response) {
	var responseText = response.responseText;
	if ((responseText == null) || (responseText == "")) return;

    eval("itemdet = " + responseText);

	if (itemdet == null)
		return;

	dlgSetStockInfo(itemdet);
	calcDlgValues();
}

function dlgSetStockInfo(item) {
    var label1 = document.getElementById('lblManf');
	var label2 = document.getElementById('lblStoreStock');
	var label4 = document.getElementById('lblTotalStock');
	var label3 = document.getElementById('lblPkgSize');

	label1.innerHTML = item.manf_name;
	label3.innerHTML = item.issue_base_unit;

	if (qtyUnitSelection() == 'I') {
		label2.innerHTML = parseFloat(item.qty).toFixed(decDigits);
		label4.innerHTML = parseFloat(item.totalqty).toFixed(decDigits);
	} else {
		label2.innerHTML = parseFloat(item.qty/dlgForm.po_pkg_size.value).toFixed(decDigits);
		label4.innerHTML = parseFloat(item.totalqty/dlgForm.po_pkg_size.value).toFixed(decDigits);
	}

	document.getElementById('lblPkgUom').innerHTML = item.package_uom;
	document.getElementById('lblUnitUom').innerHTML = item.issue_units;
	var taxType = item.tax_type;
}

function qtyUnitSelection () {
	return poForm.po_qty_unit.value;
}

function deleteItem(imgObj) {
	var rowObj = getThisRow(imgObj);
	var newItem = getElementByName(rowObj, "item_order").value;
	if (newItem == '') {
		// just remove the row
		rowObj.parentNode.removeChild(rowObj);
	} else {
		// mark as deleted so that on save the item is removed
		var deletedInput = getElementByName(rowObj,"_deleted").value;
		var trashImgObj = imgObj;
		editImgObj = getElementByName(rowObj,"editicon");
		if (deletedInput == 'false') {
			getElementByName(rowObj,"_deleted").value = 'true';
			trashImgObj.setAttribute('src',popurl+"/icons/undo_delete.gif");
			editImgObj.setAttribute('src',popurl+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
			YAHOO.util.Dom.addClass(rowObj, 'edited');
		} else {
			getElementByName(rowObj,"_deleted").value = 'false';
			trashImgObj.setAttribute('src',popurl+"/icons/delete.gif");
			editImgObj.setAttribute('src',popurl+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.stores.procurement.edititems"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
			YAHOO.util.Dom.removeClass(rowObj, 'edited');
		}
	}
	resetTotals();
}

function openMasterScreen() {
	window.open(cpath+'/master/StoresItemMaster.do?_method=add');
}

function setMasterModified() {
	setTimeout(refreshItemMaster, 10);
}

function refreshItemMaster() {
	// check the timestamp before getting the new master.
	var url = cpath + "/stores/utils.do?_method=getItemMasterTimestamp"
	//YAHOO.util.Connect.asyncRequest('GET', url, { success: onGetItemMasterTimestamp });
}

function onGetItemMasterTimestamp(response) {
	if (response.responseText != undefined) {
		var newTimestamp = parseInt(response.responseText);
		if (gItemMasterTimestamp != newTimestamp) {
			var url = cpath + "/pages/stores/getItemMaster.do?addVarName=N&ts=" + newTimestamp;
			gItemMasterTimestamp = newTimestamp;
			YAHOO.util.Connect.asyncRequest('GET', url, { success: onGetMasterItems });
		}
	}
}

function onGetMasterItems(response) {
	if (response.responseText != undefined) {
        jItemNames = eval('(' + response.responseText + ')');
		initItemAutoComplete();
	}
}

function onChangeTaxName() {
	allRowsHiddenToLabels();
	setCstRateState();
	displayItemLvlCst();
	resetTotals();
}


function setCstRateState() {
	var taxName = poForm.main_vat_type.value;
	poForm.main_cst_rate.readOnly = (taxName == 'VAT');
}

function displayItemLvlCst(){
	var cstRow = getThisRow(dlgForm.cst_rate);
	cstRow.style.display = ( (poForm.main_vat_type.value == 'CST' || taxSelection() == 'iGST') && applySupplierTaxRules != 't' ? 'table-row' : 'none' );
}

function onChangeCST(value) {
	validateCformTaxrate(poForm.c_form);
	allRowsHiddenToLabels();
	resetTotals();
}
function setCST(){
		dlgForm.cst_rate.value = poForm.main_cst_rate.value;
}
function makeingDec(objValue, obj) {
    if (objValue == '' || isNaN(objValue)) objValue = 0;
    obj.value = parseFloat(objValue).toFixed(decDigits);
}

function taxSelection() {
    return poForm.main_vat_type.value;
}

function alertUsrApproval(itemApprovedrRejected){

	if ( itemApprovedrRejected )
		return true;

	if (!confirm(getString("js.stores.procurement.no.approve.reject")))
        	return false;

    return true;
}

function checkAmendedReason(){
		if((existingPOstatus == 'A' || existingPOstatus == 'AA' || existingPOstatus == 'AO' || existingPOstatus == 'AV') 
				&& (nextPOstatus == 'AO' || nextPOstatus == 'AV' || nextPOstatus == 'AA')) {
			if (!validateRequired(poForm.amended_reason, getString("js.stores.procurement.amendment.reason"))) {
				poForm.amended_reason.focus();
				return false;
			}
		}
		
		/*if((existingPOstatus == 'A' || existingPOstatus == 'AA') 
				&& (nextPOstatus == 'AO' || nextPOstatus == 'AV' || nextPOstatus == 'AA')) {
			if(poAmendmentStatus == false ) {
				alert("Please add additional amendment reason");
				poForm.amended_reason.focus();
				return false;
			}
		}*/
	
	return true;
}

/*function setPOAmendementStatus () {
	if((existingPOstatus == 'A' || existingPOstatus == 'AA') && (nextPOstatus == 'AA' || nextPOstatus == 'AO' || nextPOstatus == 'AV')) {
		poAmendmentStatus = true;
	}
}*/

function submitSave(){
 	poForm.btnSavePo.disabled = true;
	poForm.btnSaveAndPrintPo.disabled = true;
	poForm.store_id.disabled =false;
	if ( poForm.main_vat_type ){
		poForm.main_vat_type.disabled = false;
	}
	poForm.submit();
}

function onlySavePO(){
	if ( !savePO() ) return false;;
	submitSave();
	
}

function getTableRowsWithNoDeletedRows(){
	var itemListTable = document.getElementById("medtabel");
	var numRows = getNumItems();
	var tableLength = 0;

	for (var l=0; l<numRows; l++) {
		var rowObj = getItemRow(l);
		if (getElementByName(rowObj,'_deleted').value == 'true')
			continue;
		tableLength++;
	}
	
	return tableLength;
	

}
function submitPartialPOSave(){
	if(document.getElementsByName("status_fld")[0].value == 'FC'){
		if(document.getElementsByName("remarks")[0].value.trim() == ''){
			alert(getString("js.stores.procurement.enterremarks"));
			document.getElementsByName("remarks")[0].focus();
			return false;
		}
	}
	poForm.submit();
}
var supplierRateObj = {};
function getSupplierRateValue() {
	
	 var medicineId = dlgForm.medicine_id.value;
	
	 if(supplierRateObj[medicineId] != null){
		 if(supplierRateObj[medicineId].supplier_id == poForm.supplier_id.value && supplierRateObj[medicineId].storeId == poForm.store_id.value){
			 dlgForm.supplier_rate_validation.value = supplierRateObj[medicineId].supplier_rate_validation;
			 dlgForm.supplier_rate_val.value = supplierRateObj[medicineId].supplier_rate;
		 }else{
			 getSupplierRateAjaxCall();
		 }
	 }else{
		 getSupplierRateAjaxCall();
	 }	
}
function getSupplierRateAjaxCall(){ 
	 var medicineId = dlgForm.medicine_id.value;
	 var ajaxReqObject = newXMLHttpRequest();
	 var getSupplierRateDetails = '';
	 var url = cpath+'/pages/master/SupplierContractMaster/SupplierContractItemRates.do?_method=getSupplierItemRateDetails&medicineId='+medicineId+'&supplierId='+poForm.supplier_id.value+'&storeId='+poForm.store_id.value;

		ajaxReqObject.open("GET",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("supp_Rate_Datails = " + ajaxReqObject.responseText)
				getSupplierRateDetails = supp_Rate_Datails;
			}
		}
		
		supplierRateObj[medicineId] = getSupplierRateDetails;
		dlgForm.supplier_rate_validation.value = getSupplierRateDetails.supplier_rate_validation;
		dlgForm.supplier_rate_val.value = getSupplierRateDetails.supplier_rate;
}
function onChangeStore(){
	if (applySupplierTaxRules == 't'){
		var suppId = poForm.supplier_id.value;
		var supplier = findInList(jAllSuppliers, 'SUPPLIER_CODE', suppId);
		//registeredSupplierSettings(supplier);
	}
	deleteAllRows();
}

function rowItemToRow(rowIndex) {
	// copy to hidden values in row: call common object to form, with indexed grnForm as the target
	var row = getItemRow(rowIndex);

	objectToHidden(gRowItems[rowIndex], row);
	// copy to labels in table: call common object to label
	objectToRowLabels(gRowItems[rowIndex], row, gColIndexes);

//	if ( (taxSelection() == 'CST' || taxSelection() == 'iGST') && gColIndexes.vat_type ) {
//		row.cells[gColIndexes.vat_type].textContent = '';
//		row.cells[gColIndexes['vat_type']].title = '';
//	}
	
}
function formatPaise(gDialogItem) {
	
	gDialogItem.adj_mrp = formatAmountPaise(gDialogItem.adj_mrp);
	gDialogItem.adj_mrp_display = gDialogItem.adj_mrp;
	gDialogItem.cost_mrp = formatAmountPaise(gDialogItem.cost_mrp);
	gDialogItem.med_total = formatAmountPaise(gDialogItem.med_total);
	gDialogItem.vat = formatAmountPaise(gDialogItem.vat);
	return gDialogItem;
}

function onClickDeleteQuotation() {
	if (poForm.deleteUploadedQuotation.checked) {
		poForm.quotationAttachment.disabled = true;
	} else {
		poForm.quotationAttachment.disabled = false;
	}
}

function initForm8hVariables(){
	taxobj = poForm.main_vat_type;
	form8HObj = poForm.form_8h;
	cformObj = poForm.c_form;
	if (document.poForm.form_8h.checked){
		form8Hcal = true;
		form8HApplicable = true;
	}
}

function taxVariation(){
	var taxUpdation = false;
	var totalNoOfRows;
	totalNoOfRows = getNumItems();
	var table = document.getElementById("medtabel");
	for (var i=0;i<totalNoOfRows;i++) {
		row = getItemRow(i);		
		var savedVatrate = getRowAmount(row,'vat_rate');
		var masterVatRate = getRowAmount(row,'master_vat_rate');
		if ( savedVatrate != masterVatRate  ||
				getElementByName(row, "vat_type").value != getElementByName(row, 'master_vat_type').value){
			taxUpdation = true;
			break;
		}
	}
	return taxUpdation;
}

function updateMasterTax(){
	var numItems = getNumItems();
	for (var k=0; k < numItems; k++) {
		var row = getItemRow(k);
		setElementAmount(getElementByName(row, "vat_rate"), getRowAmount(row,'master_vat_rate'));
		getElementByName(row, "vat_type").value = getElementByName(row, 'master_vat_type').value;
	}
}

function disableSupplier(postatus){
	//supplier edit is allowed for Open/AmendOpen PO
	poForm.supplier_name.disabled = !(postatus == 'O' || (amendment == 'true' && (postatus == 'A'  || postatus == 'AO' )));
}

function getTaxDetails(dlgForm) {
	clearTaxFields();
	if(document.dlgForm.discount_per && document.dlgForm.discount_per != null && document.dlgForm.discount_per.value 
			&& document.dlgForm.qty_req_display && document.dlgForm.qty_req_display != null && document.dlgForm.qty_req_display.value
			&& document.dlgForm.cost_price_display && document.dlgForm.cost_price_display != null && document.dlgForm.cost_price_display.value 
			&& document.dlgForm.discount && document.dlgForm.discount != null && document.dlgForm.discount.value && document.dlgForm.discount_per.value > 0
			&& (document.dlgForm.discount.value == '0' || document.dlgForm.discount.value == '' || document.dlgForm.discount.value == 0)) {
		var qty = document.dlgForm.qty_req_display.value;
		var rate = document.dlgForm.cost_price_display.value;
		var discountPer = document.dlgForm.discount_per.value;
		document.dlgForm.discount.value = calcDiscountAmt(rate, qty, discountPer);
	}
	var url = cpath + "/purchaseorder/potaxdetails.json";
	ajaxForm(dlgForm, url, false, setTaxDetails);
}

function getTaxDetailsOnChange(dlgForm) {
	clearTaxFields();
	if(document.dlgForm.discount_per && document.dlgForm.discount_per != null && document.dlgForm.discount_per.value 
			&& document.dlgForm.qty_req_display && document.dlgForm.qty_req_display != null && document.dlgForm.qty_req_display.value
			&& document.dlgForm.cost_price_display && document.dlgForm.cost_price_display != null && document.dlgForm.cost_price_display.value 
			&& document.dlgForm.discount && document.dlgForm.discount != null && document.dlgForm.discount.value && document.dlgForm.discount_per.value > 0
			&& (document.dlgForm.discount.value == '0' || document.dlgForm.discount.value == '' || document.dlgForm.discount.value == 0)) {
		var qty = document.dlgForm.qty_req_display.value;
		var rate = document.dlgForm.cost_price_display.value;
		var discountPer = document.dlgForm.discount_per.value;
		document.dlgForm.discount.value = calcDiscountAmt(rate, qty, discountPer);
	}
	var url = cpath + "/purchaseorder/changepotaxdetails.json";
	ajaxForm(dlgForm, url, false, setTaxDetails);
}

function calcDiscountAmt(rate, qty, discountPer) {
	var discAmt = 0;
	discAmt = rate * qty * discountPer / 100;
	if(isNotNullObj(discAmt)) 
		discAmt = formatAmountValue(discAmt);
	return discAmt;
}

function clearTaxFields() {
	setFieldHtml("lblvat_rate", parseFloat(0).toFixed(decDigits));
	setFieldHtml("lblvat_amt", parseFloat(0).toFixed(decDigits));
	for(var i=0; i < groupListJSON.length; i++) {
		var p = groupListJSON[i].item_group_id;
		if(isNotNullHtml("taxname", p)){
			setFieldHtml("taxname", groupListJSON[i].item_group_name, p);
			setFieldValue("taxname_", groupListJSON[i].item_group_name, p);
			
		}
		if(isNotNullHtml("taxrate", p)){
			setFieldHtml("taxrate", 0, p);
			setFieldValue("taxrate_", 0, p);
		}
		if(isNotNullHtml("taxamount", p)){
			setFieldHtml("taxamount", parseFloat(0).toFixed(decDigits), p);
			setFieldValue("taxamount_", parseFloat(0).toFixed(decDigits), p);
		}
	}
}

function setTaxDetails(response) {
	if (response != undefined && response.tax_details != undefined && response.tax_details != null) {
		var taxMap = response.tax_details;
		var vatRate = 0;
		var vatAmt = 0;
		var adj_mrp = 0;
		
		for(var k=0; k < groupListJSON.length; k++) {
			var itemGrpId = groupListJSON[k].item_group_id;
			setFieldHtml("taxrate", '', itemGrpId);
			setFieldValue("taxrate_", '', itemGrpId);
			setFieldHtml("taxamount", '', itemGrpId);
			setFieldValue("taxamount_", '', itemGrpId);
			setFieldValue("taxsubgroupid_", '', itemGrpId);
		}
		
		for(var i=0; i<taxMap.length; i++) {
		    for(var j=0; j < subgroupNamesList.length; j++) {
		    	if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id] && taxMap[i][subgroupNamesList[j].item_subgroup_id] != null) {
		    		var val = taxMap[i][subgroupNamesList[j].item_subgroup_id];
		    		var itemGroupId = subgroupNamesList[j].item_group_id;
					setFieldHtml("taxrate", parseFloat(val.rate).toFixed(decDigits), itemGroupId);
					setFieldValue("taxrate_", parseFloat(val.rate).toFixed(decDigits), itemGroupId);
					setFieldHtml("taxamount", parseFloat(val.amount).toFixed(decDigits), itemGroupId);
					setFieldValue("taxamount_", parseFloat(val.amount).toFixed(decDigits), itemGroupId);
					setFieldValue("taxsubgroupid_", subgroupNamesList[j].item_subgroup_id, itemGroupId);
		    	}
		    }
		}
		
		// Added for edit tax subgroups
		for(var key in gItemTaxGroups) {
			if (gItemTaxGroups.hasOwnProperty(key)) {
				// Set subgroup dropdown.
				for(var subkey in gItemTaxSubGroups) {
					if (gItemTaxSubGroups.hasOwnProperty(subkey)) {
						for(var i=0; i<gItemTaxSubGroups[subkey].length ; i++) {
							if(gItemTaxSubGroups[subkey][i].item_group_id == gItemTaxGroups[key].item_group_id) {
								for(var k =0 ; k<taxMap.length ;k++) {
									for(var subGroupKey in taxMap[k]) {
										if(taxMap[k].hasOwnProperty(subGroupKey)) {
											if(taxMap[k][subGroupKey].tax_sub_group_id == gItemTaxSubGroups[subkey][i].item_subgroup_id) {
												document.getElementById('ad_taxsubgroupid'+gItemTaxGroups[key].item_group_id).value = taxMap[k][subGroupKey].tax_sub_group_id;
												var val = taxMap[k][subGroupKey];
												vatRate = vatRate + parseFloat(val.rate);
												vatAmt += parseFloat(parseFloat(val.amount).toFixed(decDigits));
											    break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		if(dlgForm) {
			document.getElementById("vat_rate").value = parseFloat(vatRate).toFixed(decDigits);
			document.getElementById("tax_rate").value = parseFloat(vatRate).toFixed(decDigits);
			document.getElementById("lblvat_rate").innerHTML = parseFloat(vatRate).toFixed(decDigits);
			document.getElementById("lblvat_amt").innerHTML = parseFloat(vatAmt).toFixed(decDigits);
			document.getElementById("vat").value = parseFloat(vatAmt).toFixed(decDigits);
			if(response.adj_price != undefined && response.adj_price != null && response.adj_price != 'null' && response.adj_price != '')
				adj_mrp = parseFloat(response.adj_price).toFixed(decDigits);
			else 
				adj_mrp = 0;
			
			document.getElementById("adj_mrp").value = parseFloat(adj_mrp).toFixed(decDigits);
			
		}
		calcDlgValues();
	}
}
function validpastDate(){
	if (!doValidateDateField(poForm.po_date, 'past')) {
		if(podate != ""){
			poForm.po_date.value = podate;
		} else {
			var today = getServerDate();
			poForm.po_date.value = formatDate(today, 'ddmmyyyy', '-');
		}
		return false;
	}
}

function getSubgroups(){
	var url = cpath + "/purchaseorder/taxgroups.json";
	var response = ajaxGetFormObj(null, url, false);
	if(response && response != null) {
		gItemTaxGroups = response.item_groups?response.item_groups:'';
		gItemTaxSubGroups = response.item_subgroups?response.item_subgroups:'';
	}
}
function taxGroupInit(id) {
	var labelNameOptions = '';
	var count = 1;
	for(var key in gItemTaxGroups) {
		if (gItemTaxGroups.hasOwnProperty(key)) {
			// Set Groupname label.
			labelNameOptions += '<td class="formlabel">';
			labelNameOptions += gItemTaxGroups[key].item_group_name;
			labelNameOptions += ':</td>';
			labelNameOptions += '<td class="forminfo"';
			labelNameOptions += ' colspan="2">';
			labelNameOptions += '<select name="subgroups" id="ad_taxsubgroupid'+gItemTaxGroups[key].item_group_id+'" onchange="return onChangeTaxPer();">';

			labelNameOptions += '<option value="">--select--</option>';
			//Set subgroup dropdown.
			for(var subkey in gItemTaxSubGroups) {
				if (gItemTaxSubGroups.hasOwnProperty(subkey)) {

					for(var i=0; i<gItemTaxSubGroups[subkey].length ; i++) {
						if(gItemTaxSubGroups[subkey][i].item_group_id == gItemTaxGroups[key].item_group_id) {
							labelNameOptions += '<option value='+gItemTaxSubGroups[subkey][i].item_subgroup_id
							labelNameOptions += '>'+gItemTaxSubGroups[subkey][i].item_subgroup_name+'</option>';
						}
					}
				}
			}
			labelNameOptions += '</select>';
			if(count >= 3) {
				labelNameOptions += '</td>';
				labelNameOptions += '</tr>';
				labelNameOptions += '<tr>';
				count = 0;
			} else {
				labelNameOptions += '</td>';
			}
			count++;
		}
	}
	document.getElementById(id).outerHTML = labelNameOptions;
	if(allowTaxEditRights != 'A' && !(gRoleId == 1 || gRoleId == 2)){
		var length = document.getElementsByName("subgroups").length;
		for (var k = 0; k <length; k++) {
			document.getElementsByName("subgroups")[k].disabled =true;
		}
	}	
}

function clearTaxSelection() {
	var length = document.getElementsByName("subgroups").length;
	for (var j = 0; j <length; j++) {
		document.getElementsByName("subgroups")[j].selectedIndex =0;
	}
	
	for(var i=0; i < groupListJSON.length; i++) {
		var p = groupListJSON[i].item_group_id;
		setFieldValue("taxname_", groupListJSON[i].item_group_name, p);
		setFieldValue("taxrate_", 0, p);
		setFieldValue("taxamount_", parseFloat(0).toFixed(decDigits), p);
		setFieldValue("taxsubgroupid_", '', p);
	}
}
function formatTCSAmountObj(obj) {
	if (null != obj) {
		if ( "" != obj.value ) {
			var objValue = parseFloat(obj.value);
			if(isNaN(objValue)) {
				obj.value = '0';
				return '0';
			}
			obj.value = objValue;
			return objValue;
			
		} else {
			obj.value = '0';
			return '0';
		}
	}
}

