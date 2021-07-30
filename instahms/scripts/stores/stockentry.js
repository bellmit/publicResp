var batchAutoComp;
var medAutoComp;
var suppAutoComp;
var gCurrentPoNo = '';
var poMedList = [];
var invoicesOfSupp = null;
var poItemNames = [];
var gItemNames = [];
var globalStoreId;

var gItemDetails = {};		// cache of item details and their batches

var dlgForm;
var grnForm;
var gColIndexes = [];
var gRowUnderEdit = -1;
var gRowItems = [];
var gDialogItem = null;
var gItemTaxGroups = {};
var gItemTaxSubGroups = {};
var supplierCache = {};
var initialTCSValue;

function init() {
	if (editGRN) {
		ajaxForPrintUrls();
	}
	
	if(!editGRN && poNo == '') {
		document.getElementById("tcsRow").style.display = 'none';
	}

	dlgForm = document.detailForm;
	grnForm = document.directstockform;

    //check if the user has store access
    checkstoreallocation();

	globalStoreId = grnForm.store_id.value;

    // column indices
    var cl = 0;
    gColIndexes.medicine_name = cl++;gColIndexes.item_code = cl++; gColIndexes.batch_no = cl++;  gColIndexes.exp_dt_display = cl++;
    gColIndexes.grn_pkg_size = cl++;
    if(!doAllowStatus) {
    	gColIndexes.mrp_display = cl++; gColIndexes.cost_price_display = cl++;

    }
    gColIndexes.billed_qty_display = cl++; gColIndexes.bonus_qty_display = cl++;
	gColIndexes.uom_display = cl++;
    if (!doAllowStatus) {
    	gColIndexes.tax_rate = cl++;
    	gColIndexes.tax_type = cl++;
    }
    if(!doAllowStatus) {
    	gColIndexes.discount_per = cl++;
    	gColIndexes.discount = cl++;
        gColIndexes.scheme_discount = cl++;
        gColIndexes.tax = cl++;
        gColIndexes.med_total = cl++;
    }

	TRASH_COL = cl++; EDIT_COL = cl++;

    initSupplierAutoComplete();
    initDialogs();
    /*if (prefVAT == 'Y') {
		initForm8hVariables();// in purchasedetails.js
	}*/

    if (poNo != '') {
		/*
		 * We have a PO. This could be a new GRN or it could be an edit GRN
		 */
		/* if (grnForm.store_id.options != undefined) {
			// loadPOs requires store to be set first
			var po = findInList(polist, "po_no", poNo);
			setSelectedIndex(grnForm.store_id, po.store_id);
		} */
    	if(poStoreId) {
    		grnForm.store_id.value =poStoreId;
    	}
		loadPOs();
        if (grnForm.po_no.type == 'select-one')
        	setSelectedIndex(grnForm.po_no, poNo);
		// for edit GRN, this is not a select box, it is a label.
        loadPoDetails(poNo);		// this will also load the item autocomp medAutoComp if strict PO
        grnForm.invoice_no.focus();
    } else {
		loadPOs();
	}

    if(strictPO || (noItemAddPO && poNo != '')) {
		toggleAddButton(true);
	} else {
		toggleAddButton(false);
	}
    
    if (editGRN) {
    	//disable all invoice related fields
        disableInvoiceFields(true);
        if(tcsApplicable == 'Y') {
        	document.getElementById("tcsRow").style.display = '';
        	if(poNo != '' && applyStrictPoControls) {
        		grnForm.tcs_type.disabled = true;
        		grnForm.tcs_value.disabled = true;
        	}        	
        } else {
        	document.getElementById("tcsRow").style.display = 'none';
        }
       	if(invSupId !='') {
       		grnForm.supplier_id.value = invSupId;
	    }
		if(invSupName !='') {
	        grnForm.supplierName.value = invSupName;
	    }
		loadGrnItemsToGrid();
        // set PO's for supplier, if they are present
        if (grnForm.po_no.type == 'select-one')
        	setSelectedIndex(grnForm.po_no, grnpo_no);
        //set PO discount allowed/disallowed
        if (strictPO && grnpo_no != '')
        	restrictPOFlow(true);
        else
        	restrictPOFlow(false);
        grnForm.po_no.disabled = true;
        //grnForm.tax_name.disabled = true;//tax type is not editable

        if (grnForm.po_no.value != '')
			showPOLink(grnForm.po_no.value);
    } else {
        gCurrentPoNo = '';
        if(poNo != '' && applyStrictPoControls) {
    		grnForm.tcs_type.disabled = true;
    		grnForm.tcs_value.disabled = true;
    	}
    }

    initItemAutoComplete();

	if (!allowSave) {
		disableAllFields();
	}
	var currentStore = storesListJSON.filter( store => store.dept_id == globalStoreId);
	setFieldValue('grn_print_template', currentStore.length > 0 ? currentStore[0].grn_print_template : 'BUILTIN_HTML')
	setFieldValue('grnPrintTemplateHid', currentStore.length > 0 ? currentStore[0].grn_print_template : 'BUILTIN_HTML')
	
	getSubgroups();
	taxGroupInit("add_tax_groups");
}

function loadPOs() {
	// load POs for the selected store only. If no store is selected, don't load any.
    if (grnForm.po_no.type == 'select-one') {
		var storeId = grnForm.store_id.value;
		var ajaxReqObject = newXMLHttpRequest();
		var url = cpath+'/stores/stockentry.do?_method=getPOsForStore&store='+storeId;
		var ajaxResponse = '';
		ajaxReqObject.open("GET",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("ajaxResponse = " + ajaxReqObject.responseText)
				polist = ajaxResponse.polist;
			}
		}

		var storePos = filterList(polist, "store_id", storeId);
    	loadSelectBox(grnForm.po_no, storePos, "po_no", "po_no", "-- Select --", "");
	}
}

//checks whether store is allocated for user.
function checkstoreallocation() {
    if (gRoleId != 1 && gRoleId != 2) {
        if (deptId == "") {
            showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
            document.getElementById("checkDefaultStore").style.display = 'none';
        }
    }
}


function initDialogs() {
	initPurchaseDetailsDialog(dlgForm.billed_qty_display);	// in purchasedetails.js
    initAddEditDialog();
}

function initAddEditDialog() {
    detaildialog = new YAHOO.widget.Dialog("addEditDialog", {
        width: "800px",
        context: ["plusItem", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    var escKeyListener = new YAHOO.util.KeyListener("addEditDialog",
			{keys: 27 }, handleDetailDialogCancel);
    detaildialog.cfg.queueProperty("keylisteners", escKeyListener);
    detaildialog.render();
}

function getSupplierDetails(supplierCode) {
	if(supplierCache[supplierCode]){
		if(supplierCache[supplierCode].tcs_applicable == 'Y') {
	    	document.getElementById("tcsRow").style.display = '';
		} else {
	    	document.getElementById("tcsRow").style.display = 'none';
		}
		return supplierCache[supplierCode];
	}

	var xhttp = new XMLHttpRequest();
	var url = cpath + '/pages/stores/supplierDetails.do?method=getSupplierById&supplierCode=' + supplierCode;
	xhttp.open("GET", url, false);
	xhttp.send(null);
	if (xhttp.readyState == 4) {
		if ( (xhttp.status == 200) && (xhttp.responseText != null ) ) {
			var supplier= eval('('+xhttp.responseText+')');
			supplierCache[supplier.supplier_code] = supplier;
			if (supplier.tcs_applicable == 'Y') {
				document.getElementById("tcsRow").style.display = '';
			} else {
				document.getElementById("tcsRow").style.display = 'none';
			}
			return supplier;
		}
	}
	return null;
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
		fields : [  {key : "supplier_name_with_city"}, {key : "supplier_code"}, {key : "supplier_name"} ]
	};

    suppAutoComp = new YAHOO.widget.AutoComplete('supplierName', 'suppliername_dropdown', dataSource);
    suppAutoComp.maxResultsDisplayed = maxResults;
    suppAutoComp.allowBrowserAutocomplete = false;
    suppAutoComp.prehighlightClassName = "yui-ac-prehighlight";
    suppAutoComp.typeAhead = false;
    suppAutoComp.useShadow = false;
    suppAutoComp.animVert = false;
    suppAutoComp.minQueryLength = 0;
    suppAutoComp.forceSelection = true;
    suppAutoComp.filterResults = Insta.queryMatchWordStartsWith;
    suppAutoComp.unmatchedItemSelectEvent.subscribe(clearSupplierAddress);

    //setTimeout(function () {suppAutoComp.textboxChangeEvent.subscribe(onSelectSupplier)}, 100);
    setTimeout(function () {suppAutoComp.itemSelectEvent.subscribe(onSelectSupplier)}, 100);
    grnForm.supplierName.focus();
}

function disableInvoiceFields(val) {
    grnForm.supplierName.disabled = val;
    grnForm.po_no.disabled = val;
    grnForm.status.focus();
}

function disableAllFields() {
	grnForm.invoice_date.disabled = true;
    grnForm.invoice_no.readOnly = true;
	grnForm.due_date.disabled = true;
	grnForm.status.disabled = true;
	grnForm.po_reference.disabled = true;
	grnForm.remarks.disabled = true;
	grnForm.invoiceAttachment.disabled = true;
	grnForm.inv_discount_type.disabled = true;
	grnForm.inv_discount_val.disabled = true;
	grnForm.other_charges.disabled = true;
	grnForm.transportation_charges.disabled = true;
	grnForm.round_off.disabled = true;
	grnForm.tcs_type.disabled = true;
	grnForm.tcs_value.disabled = true
}


function clearSupplierAddress() {
    document.getElementById('suppAddId').textContent = '';
}

function loadSupplierDetails() {

    var suppId = grnForm.supplier_id.value;
	var supplier = getSupplierDetails(suppId);
	
	var invDate = parseDateStr(grnForm.invoice_date.value);
	var fromDate='';
	var toDate='';
	startMonth = fin_yr_start_month-1;
	endMonth = fin_yr_end_month-1;
	if(startMonth>0){
		if (invDate.getMonth() > endMonth) {
			fromDate = new Date(invDate.getFullYear(), startMonth, 1);
			toDate   = new Date(invDate.getFullYear() + 1, endMonth+1, 0);
		} else {
			fromDate = new Date(invDate.getFullYear() - 1, startMonth, 1);
			toDate   = new Date(invDate.getFullYear(), endMonth+1, 0);
		}
	} else{
			fromDate = new Date(invDate.getFullYear(), startMonth, 1);
			toDate = new Date(invDate.getFullYear(), endMonth, 31);
	}
	
    var url = cpath + '/stores/stockentry.do?_method=getInvoiceNosOfSupplier&supplier_id=' + suppId+'&from_date='+formatDate(fromDate, 'yyyymmdd')+'&to_date='+formatDate(toDate, 'yyyymmdd');
    YAHOO.util.Connect.asyncRequest('GET', url, {
        success: handleInvoicesOfSupplier,
		failure: onAjaxFailure
    });

	if(supplier != null) {
		var supplierAddress = supplier.supplier_address;
    	setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 60, supplierAddress);
    }

}

function onSelectSupplier(type, args) {
    var selSupplierName ='';
    var selSupplierId = '';
    if ( args == undefined ){//from editgrn / Reorder to po/on select PO
    	selSupplierName = grnForm.supplierName.value;
    	selSupplierId = grnForm.supplier_id.value;
    } else {
    	selSupplierName = args[2][2];
    	selSupplierId = args[2][1];
    }
	var supplier = getSupplierDetails(selSupplierId)
	if(supplier != null){
		if(grnForm.supplier_id.value != ""){
			if(grnForm.supplier_id.value != supplier.supplier_code){
				deleteAllRows();
				grnForm.invoice_no.value = '';
			}
		}
		grnForm.supplier_id.value = supplier.supplier_code;
		grnForm.supplierName.value = supplier.supplier_name;
	}


	if ( allowSave ) {
		 setDefualtDueDate();//no calculation of due date for saves grns
	}
	deleteAllRows();
	loadSupplierDetails();

	// if we have any cached medicine details, it is no longer valid, since the
	// best cost price can change depending on supplier
	gItemDetails = {};
}

function handleInvoicesOfSupplier(response) {
    invoicesOfSupp = eval(response.responseText);
    if (trimAll(grnForm.invoice_no.value) != '') {
    	checkDuplicateInvoice(grnForm.invoice_no);
    }
}

//function to check whether the invoice_no entered is duplicate for this supplier
function checkDuplicateInvoice(invObj) {
	invObj.value = trimAll(invObj.value);
	var invDate = parseDateStr(grnForm.invoice_date.value);
	if (editGRN && invObj.value == trimAll(origInvoiceNo) && formatDate(invDate,'ddmmyyyy') == origInvoiceDate) {
	  return;
	}

    for (var i = 0; i < invoicesOfSupp.length; i++) {
        if (invObj.value == trimAll(invoicesOfSupp[i].invoice_no)) {
        	if(doAllowStatus || doSchemaAllowStatus) {
        		 showMessage("js.stores.procurement.duplicatedono");
        	} else {
        		 showMessage("js.stores.procurement.duplicateinvoiceno");
        	}

            invObj.value = '';
            invObj.focus();
        }
    }
}

//set invoice date (today's date + supplier credit period)
function setDefualtDueDate() {
    var today = formatDate(getServerDate(), "ddmmyyyy", "-");
    if ( empty(grnForm.invoice_date.value) ) {
  		  grnForm.invoice_date.value = today;
  	}
    var supplierId = grnForm.supplier_id.value;
    var newSupplier = true;
	var item = getSupplierDetails(supplierId)
	var creditPeriod = item.credit_period == null || item.credit_period == '' ? 0 : item.credit_period;
	if ( parseInt(creditPeriod) == 0 ){
		newSupplier = false;
		// continue;
	}else{
		var tod = getServerDate();
		tod.setDate(tod.getDate() + parseInt(creditPeriod));
		var dueDate = tod;
		grnForm.due_date.value = formatDate(dueDate, "ddmmyyyy", "-");
		newSupplier = false;
	}
            
    if (newSupplier) {
        grnForm.due_date.value = today;
    }
}

/*
 * Set the PO header information. The PO Items is handled by onGetPOItems
 * Not to be called for Edit GRN, because we don't need the PO any more for
 * editing a GRN, all values must be copied over to the GRN anyway.
 */
function populatePODetails(poNo) {

	var po = findInList(polist, "po_no", poNo);
	var supplier = getSupplierDetails(po.supplier_id);
	if (!supplier) {
		showMessage("js.stores.procurement.supplierisinactive");
		return false;
	}
	
	if(supplier.tcs_applicable == 'Y') {
    	document.getElementById("tcsRow").style.display = '';
    	setSelectedIndex(grnForm.tcs_type, po.tcs_type);

    	initialTCSValue = po.tcs_amount; 
      grnForm.po_inv_amt.value = po.po_total;
    	if (po.tcs_type == 'P') {
    		grnForm.inv_tcs_per.value = po.tcs_per;
    		grnForm.tcs_value.value = po.tcs_per;
    	} else {
    		grnForm.inv_tcs_amount.value = po.tcs_amount;
    		grnForm.tcs_value.value = po.tcs_amount;
    	}
    	if(applyStrictPoControls) {
    		grnForm.tcs_type.disabled = true;
    		grnForm.tcs_value.disabled = true;
    	}
    	
	} else {
    	document.getElementById("tcsRow").style.display = 'none';
	}
	
	grnForm.supplierName.value = supplier.supplier_name;
	grnForm.supplier_id.value = supplier.supplier_code;

	if (suppAutoComp._elTextbox.value != '') {
		suppAutoComp._bItemSelected = true;
		suppAutoComp._sInitInputValue = suppAutoComp._elTextbox.value;
	}

	setSelectedIndex(grnForm.inv_discount_type, po.discount_type);

	if (po.discount_type == 'P') {
		grnForm.inv_discount_per.value = po.discount_per;
		grnForm.inv_discount_val.value = po.discount_per;
	} else {
		grnForm.inv_discount.value = po.discount;
		grnForm.inv_discount_val.value = po.discount;
	}

	grnForm.round_off.value = po.round_off;
	setSelectedIndex(grnForm.grn_qty_unit, po.po_qty_unit);

	grnForm.remarks.value = po.remarks;
	grnForm.po_reference.value = po.reference;
	/*if ( prefVAT == 'Y' ) {
		setSelectedIndex(grnForm.tax_name, po.vat_type);
		grnForm.main_cst_rate.value = po.vat_rate;
		onChangeTaxName();
		onChangeCST();
	}*/

	restrictPOFlow(strictPO);
	grnForm.transportation_charges.value = po.transportation_charges;

	if ( grnForm.purpose_of_purchase ){
		grnForm.purpose_of_purchase.value = po.purpose_of_purchase;
	}
	/*if ( prefVAT == 'Y' )
		grnForm.c_form.checked = po.c_form;*/

	restrictPOFlow(strictPO);
	return true;
}

function loadGrnItemsToGrid() {
	for (var i = 0; i < grnItemsList.length; i++) {
		var item = grnItemsList[i];
		var taxAmt = 0;
		// save a copy in our global Item and batches cache
		var masterItem = {};
		shallowCopy(item, masterItem, ['medicine_id', 'medicine_name', 'item_barcode_id', 'grn_package_uom',
			'issue_units', 'issue_base_unit',  'identification', 'expiry_date_val', 'package_uom',
			'tax_rate', 'tax_type', 'billable', 'bin', 'cost_price', 'mrp', 'batch_no_applicable' ,'code_type','item_code']);

		masterItem.batches = grnItemBatches[item.medicine_id];
		masterItem.item_max_cost_price =item.item_max_cost_price;
		var itemBatches = masterItem.batches;
		item.asset_approved = itemBatches[0].asset_approved;
		gItemDetails[item.medicine_id] = masterItem;

		item.grnqty = item.billed_qty; item.grnbqty = item.bonus_qty;
		item.po_billed_qty = item.billed_qty; item.po_bonus_qty = item.bonus_qty;

		// we need all attributes in the object to ensure proper copy: this is the superset.
		item.newbatch = 'N'; item.grnmed = 'Y'; item.discount_per = 0; item.scheme_discount_per = 0;
		item.cst_rate = ( grnForm.tax_name.value == 'CST' || grnForm.tax_name.value == 'iGST' ? item.outgoing_tax_rate : 0 );

		for(var j=0; j < groupListJSON.length; j++) {
			var p = groupListJSON[j].item_group_id;
			var taxName = 'taxname'+p;
			var taxRate = 'taxrate'+p;
			var taxAmount = 'taxamount'+p;
			var taxSubgroup = 'taxsubgroupid'+p;
			var oldtaxSubgroup = 'oldtaxsubgroupid'+p;
			item[taxName] = '';

			for(var k=0; k < grnTaxJSON.length; k++) {
				if(grnTaxJSON[k] && grnTaxJSON[k].item_batch_id == item.item_batch_id &&
						grnTaxJSON[k].medicine_id == item.medicine_id && grnTaxJSON[k].item_group_id == p) {
					taxAmt += grnTaxJSON[k].tax_amt;
					item[taxRate] = grnTaxJSON[k].tax_rate;
					item[taxAmount] = grnTaxJSON[k].tax_amt;
					item[taxSubgroup] = grnTaxJSON[k].item_subgroup_id;
					item[oldtaxSubgroup] = grnTaxJSON[k].item_subgroup_id;
					break;
				} else {
					item[taxRate] = '0';
					item[taxAmount] = '0';
					item[taxSubgroup] = '';
					item[oldtaxSubgroup] = '';
				}
			}

		}
		if(!isNotNullObj(item.tax)) {
			item.tax = taxAmt;
			item.tax_paise = getPaise(taxAmt);
		} else {
			item.tax_paise = getPaise(item.tax);
		}
		calcItemValues(item, 'stored');
		// Now add a row based on the object
		var rowIndex = addRow();
		// set the row's item to the one we just created
		gRowItems[rowIndex] = item;
		rowItemToRow(rowIndex);

		// disable delete icon, cannot delete these
		var row = getItemRow(rowIndex);
		if (row.cells[TRASH_COL] != undefined) {
			// trash column exists only for edit GRN, not for view.
			var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");
			if (trashImgs && trashImgs[0])
				trashImgs[0].src = cpath + '/icons/delete_disabled.gif';
			(document.getElementsByName("candelete"))[rowIndex].value = "N";
		}
	}
	resetTotals();
}

onAjaxFailure = function (response) {
	showMessage("js.stores.procurement.ajaxcallfailed");
}

function onChangeInvDiscType() {
    resetTotals();
}

function onChangeInvDiscVal() {
	formatAmountObj(grnForm.inv_discount_val);
    resetTotals();
}

function onChangeTcsType() {
    resetTotals();
}

function onChangeTcsVal() {
	if(grnForm.tcs_type.value =='P') {
		formatTCSAmountObj(grnForm.tcs_value);
	} else {
		formatAmountObj(grnForm.tcs_value);
	}
    resetTotals();
}

function onChangeOtherCharges() {
    resetTotals();
}

function onChangeRoundOff() {
    resetTotals();
}

function onChangeCessRate() {
    resetTotals();
}

function qtyUnitSelection() {
	return grnForm.grn_qty_unit.value;
}

function isGrnConsignment() {
    return (grnForm.consignment_stock.value == 'true');
}

function taxSelection() {
    if (prefVAT == 'Y') {
        return grnForm.tax_name.value;
    } else {
		return '';
	}
}

function invdiscSelection() {
    return grnForm.inv_discount_type.value;
}

function itemDiscountType() {
    return grnForm.item_discount_type.value;
}

function getDefaultDiscount() {
    return grnForm.itemdiscount.value;
}

function onChangeTaxName() {
	recalcAllRows();
	setCstRateState();
	displayItemLvlCst();
}

function setCstRateState() {
	var taxName = grnForm.tax_name.value;
	grnForm.main_cst_rate.readOnly = (taxName == taxLabel);
}

function addOption(selectbox, text, value) {
    var optn = document.createElement("OPTION");
    optn.text = text;
    optn.value = value;
    selectbox.options.add(optn);
}

function onChangeCST(value) {
	validateCformTaxrate(grnForm.c_form);
	recalcAllRows();
}

function onChangeQtyUOM() {
	recalcAllRows();
}

function onChangeStore() {
	var poNum = grnForm.po_no;
	if (poNum != '') {
		// we will remove the PO selection, so need confirm delete
		if (!confirmGridDelete()) {
			setSelectedIndex(grnForm.store, globalStoreId);
			return false;
		}
	}

	globalStoreId = grnForm.store_id.value;
    var storeId = grnForm.store_id.value;
    var storeAccGroup;
    for (var i = 0; i < storesListJSON.length; i++) {
        if (storeId == storesListJSON[i].dept_id) {
            storeAccGroup = storesListJSON[i].account_group;
        }
    }

    if (invoiceAccGroup != '' && storeAccGroup != invoiceAccGroup) {
        showMessage("js.stores.procurement.storeaccount.invoiceaccount.same");
        return false;
    }

	// load the POs for the new store selected
	loadPOs();
	setSelectedIndex(grnForm.po_no, '');
	loadPoDetails('');

	// if we have any cached medicine details, it is no longer valid, since the bin can
	// change depending on store
	gItemDetails = {};

	var currentStore = storesListJSON.filter( store => store.dept_id == globalStoreId);
	setFieldValue('grn_print_template', currentStore.length > 0 ? currentStore[0].grn_print_template : 'BUILTIN_HTML')
	setFieldValue('grnPrintTemplateHid', currentStore.length > 0 ? currentStore[0].grn_print_template : 'BUILTIN_HTML')

    return true;
}

function onChangeInvDate() {
    var supplierId = grnForm.supplier_id.value;
    if(supplierId !='') {
    	var item = getSupplierDetails(supplierId);
        var creditPeriod = item.CREDIT_PERIOD == null || item.CREDIT_PERIOD == '' ? 0 : item.CREDIT_PERIOD;
        var tod = parseDateStr(grnForm.invoice_date.value);
        tod.setDate(tod.getDate() + parseInt(creditPeriod));
        var dueDate = tod;
        grnForm.due_date.value = formatDate(dueDate, "ddmmyyyy", "-");
        loadSupplierDetails();
    }
}

function onClickDeleteInvoice() {
	if (grnForm.deleteUploadedInvoice.checked) {
		grnForm.invoiceAttachment.disabled = true;
	} else {
		grnForm.invoiceAttachment.disabled = false;
	}
}

function onChangePO() {
	if (!confirmGridDelete(true)) {
		setSelectedIndex(grnForm.po_no, gCurrentPoNo);
		return false;
	}

	var po = grnForm.po_no.value;
	loadPoDetails(po);
}

function loadPoDetails(poNo) {
	gCurrentPoNo = poNo;
	if(strictPO || (noItemAddPO && poNo != '')) {
		toggleAddButton(true);
	} else {
		toggleAddButton(false);
	}
	if (poNo == '') {
		grnForm.supplierName.disabled = false;
		showPOLink(poNo);
		return;
	}
	grnForm.supplierName.disabled = true;
	if (!editGRN) {
		var ok = populatePODetails(poNo);
		if (!ok)
			return false;
	}

	// get the PO line items and fill them in the grid (async)
    var url = "stockentry.do?_method=getPOItems&po_no=" + poNo + "&store=" + grnForm.store_id.value + "&supplierId=" +grnForm.supplier_id.value;
    YAHOO.util.Connect.asyncRequest('GET', url, {
        success: onGetPOItems,
		failure: onAjaxFailure
    });

    loadSupplierDetails();
	onSelectSupplier();
    showPOLink(poNo);
}

function onGetPOItems(response) {
    if (response.responseText == undefined)
		return false;

	var poDetails = eval('(' + response.responseText + ')');
	poMedList = poDetails.poItems;		// this will double up as our item master for strict PO
	var poItemBatches = poDetails.poItemBatches;
	var poTaxDetails = poDetails.poTaxDetails;

	for (var i = 0; i < poMedList.length; i++) {
		var poItem = poMedList[i];
		poItem.pomed = 'Y';
		poItem.pomedrate = poItem.cost_price;
		poItem.po_discount_per = poItem.discount_per;
		poItem.po_billed_qty = poItem.qty_req;
		poItem.po_bonus_qty = poItem.bonus_qty_req;

		// save in our global Item and batches list also, including some item level defaults from
		// the po such as mrp, pomedrate, tax_rate and discount_per. This is so that if the same
		// item is selected for addition to grid as a second batch in the same GRN, these defaults
		// can apply.
		var masterItem = {};
		shallowCopy(poItem, masterItem, ['medicine_id', 'medicine_name', 'item_barcode_id', 'package_uom',
			'issue_units', 'issue_base_unit',  'identification', 'expiry_date_val','tax_type', 'tax_rate',
			'billable', 'bin', 'cost_price', 'mrp', 'pomed', 'pomedrate',
			'discount_per', 'po_discount_per', 'po_billed_qty', 'po_bonus_qty',
			'batch_no_applicable','supplier_rate_validation','max_cost_price' ]);

		gItemDetails[masterItem.medicine_id] = masterItem;
		gItemDetails[masterItem.medicine_id].batches = poItemBatches[masterItem.medicine_id];

		if (editGRN) {
			// no need to add to grid, we only needed gItemDetails for validations and medAutoComp
			continue;
		}

		var item = {};
		shallowCopy(poItem, item);
		for(var j=0; j < groupListJSON.length; j++) {
			var p = groupListJSON[j].item_group_id;
			var taxName = 'taxname'+p;
			var taxRate = 'taxrate'+p;
			var taxAmount = 'taxamount'+p;
			var taxSubgroup = 'taxsubgroupid'+p;
			item[taxName] = '';

			for(var k=0; k < poTaxDetails.length; k++) {
				if(poTaxDetails[k] && poTaxDetails[k].medicine_id == item.medicine_id && poTaxDetails[k].item_group_id == p) {
					item[taxRate] = poTaxDetails[k].tax_rate;
					item[taxAmount] = poTaxDetails[k].tax_amt;
					item[taxSubgroup] = poTaxDetails[k].item_subgroup_id;
					break;
				} else {
					item[taxRate] = '0';
					item[taxAmount] = '0';
					item[taxSubgroup] = '';
				}
			}

		}
		item.tax = item.vat;
		item.tax_paise = getPaise(item.vat);
		item.tax_type = item.vat_type;
		item.tax_rate = item.vat_rate;

		// copy some GRN values from the PO values. Some attributes like mrp are common
		// for both, no need to copy.
		item.grn_package_uom = item.package_uom;
		item.grnqty = item.grnbqty = 0;

		// we need all attributes in the object to ensure proper copy: this is the superset.
		item.batch_no = ''; item.exp_dt= ''; item.newbatch = 'Y';
		item.grnmed = 'N';

		var billedQty = round2(Math.max(item.qty_req - item.qty_received, 0));
		var bonusQty = round2(Math.max(item.bonus_qty_req - item.bonus_qty_received, 0));

		var bonusTax = ((item.vat/(item.qty_req+item.bonus_qty_req))*bonusQty);

		if(isNotNullObj(item.tax_type) && (item.tax_type == 'CB' || item.tax_type == 'MB'))
			item.bonus_tax = bonusTax;
		else
			item.bonus_tax = 0;
		// don't add to grid if quantity remaining to be added is 0
		if (billedQty == 0 && bonusQty == 0)
			continue;

		if (billedQty != item.qty_req) {
			// not the original quantity, so discount has to be recalculated based on %
			item.discount = 0;
			item.scheme_discount = 0;
		}

		item.po_mrp = item.mrp;
		if (item.identification != 'S') {
			// batch item (normal)
			item.grn_pkg_size = item.po_pkg_size;
			item.billed_qty = billedQty;
			item.po_billed_qty = billedQty;
			item.bonus_qty = bonusQty;
			item.po_bonus_qty = bonusQty;
			item.asset_approved = 'Y';
			if (item.batch_no_applicable == 'N')
				item.batch_no = "---";

			setPartialPOTaxDetails(item, poTaxDetails);
			calcItemAndAddRow(item);

		} else {
			// serial item. Loop over quantitities and add one row each.
			item.grn_pkg_size = 1;
			item.asset_approved = 'N';
			item.discount = 0;	// don't use discount, use only %
			item.scheme_discount = 0;

			item.billed_qty = billedQty;
			item.po_billed_qty = billedQty;
			item.bonus_qty = bonusQty;
			item.po_bonus_qty = bonusQty;
			setPartialPOTaxDetails(item, poTaxDetails);
			for (var s=0; s<(billedQty+bonusQty); s++) {
				var serialItem = {};
				shallowCopy(item, serialItem);
				serialItem.billed_qty = (s<billedQty) ? 1 : 0;
				serialItem.po_billed_qty = (s<billedQty) ? 1 : 0;
				serialItem.bonus_qty  = (s<billedQty) ? 0 : 1;
				serialItem.po_bonus_qty  = (s<billedQty) ? 0 : 1;
                serialItem.batch_no = identifierSeq++;
                setSerialTaxDetails(serialItem, item);
				calcItemAndAddRow(serialItem);
			}
		}

	}


	if (!editGRN)
		resetTotals();

	if (strictPO) {
		gItemNames = poMedList;
		initItemAutoComplete();
	}
}

function setPartialPOTaxDetails(item, poTaxDetails) {
	var grnBilledQty = parseFloat(item.billed_qty)/parseFloat(item.issue_base_unit);
	var grnBonusQty = parseFloat(item.bonus_qty)/parseFloat(item.issue_base_unit);
	var grnAllQty = grnBilledQty + grnBonusQty;

	var poBilledQty = parseFloat(item.qty_req)/parseFloat(item.issue_base_unit);
	var poBonusQty = parseFloat(item.bonus_qty_req)/parseFloat(item.issue_base_unit);
	var poAllQty = poBilledQty + poBonusQty;
	var taxAmt = parseFloat(item.tax);

	if(item.tax_type == 'MB' || item.tax_type == 'CB') {
		item.tax = formatAmountPaise(getPaise((taxAmt/poAllQty) * grnAllQty));
		item.tax_paise = getPaise((taxAmt/poAllQty)*grnAllQty);

	} else if(item.tax_type == 'M' || item.tax_type == 'C') {
		item.tax = formatAmountPaise(getPaise((taxAmt/poBilledQty) * grnBilledQty));
		item.tax_paise = getPaise((taxAmt/poBilledQty)*grnBilledQty);
	}

	for(var j=0; j < groupListJSON.length; j++) {
		var p = groupListJSON[j].item_group_id;
		var taxName = 'taxname'+p;
		var taxRate = 'taxrate'+p;
		var taxAmount = 'taxamount'+p;
		var taxSubgroup = 'taxsubgroupid'+p;
		item[taxName] = '';

		for(var k=0; k < poTaxDetails.length; k++) {
			if(poTaxDetails[k] && poTaxDetails[k].medicine_id == item.medicine_id && poTaxDetails[k].item_group_id == p) {
				item[taxRate] = poTaxDetails[k].tax_rate;
				item[taxSubgroup] = poTaxDetails[k].item_subgroup_id;

				if(item.tax_type == 'MB' || item.tax_type == 'CB') {
					item[taxAmount] = formatAmountPaise(getPaise((parseFloat(poTaxDetails[k].tax_amt)/poAllQty)*grnAllQty));

				} else if(item.tax_type == 'M' || item.tax_type == 'C') {
					item[taxAmount] = formatAmountPaise(getPaise((parseFloat(poTaxDetails[k].tax_amt)/poBilledQty)*grnBilledQty));
				}

				break;
			} else {
				item[taxRate] = '0';
				item[taxAmount] = '0';
				item[taxSubgroup] = '';
			}
		}

	}
}

function restrictPOFlow(restrict) {
	grnForm.inv_discount_type.disabled = restrict;
    grnForm.inv_discount_val.readOnly = restrict;
	/*if (prefVAT == 'Y') {
		grnForm.tax_name.disabled = restrict;
		grnForm.main_cst_rate.disabled = restrict;
	}*/
}

function showPOLink(val) {
	if(!doAllowStatus) {
		if (val != '') {
	    	document.getElementById('polink').href =
				popurl + '/pages/stores/poscreen.do?_method=getPOScreen&poNo=' + val+'&&grn_count='+document.getElementById("grn_count").value;
			document.getElementById('polink').innerHTML = ' | ' + val;
		} else {
	    	document.getElementById('polink').href = "";
			document.getElementById('polink').innerHTML = '';
		}
	}
}

function confirmGridDelete(retainPoNum) {
    var len = getNumItems();
	if (retainPoNum == null)
		retainPoNum = false;

	if (len == 0)
		return true;

	//if (confirm("If you change selection, all existing items will be deleted")) {
		deleteAllRows();
		if (!retainPoNum) {
			gCurrentPoNo = '';
			setSelectedIndex(grnForm.po_no, gCurrentPoNo);
		}
		return true;
	//}

	//return false;
}

function deleteAllRows() {
	var len = getNumItems();
	for (var p = len; p >= 1; p--) {
		document.getElementById("medtabel").deleteRow(p);
	}
	gRowItems = [];
	resetTotals();
}

function recalcAllRows() {
	for (var k=0; k<gRowItems.length; k++) {
		var item = gRowItems[k];
		calcItemValues(item, 'stored');
		rowItemToRow(k);
	}
}

function mandatoryfieldsval() {
    if (trimAll(grnForm.supplierName.value) == '') {
        grnForm.supplierName.value = '';
        showMessage("js.stores.procurement.suppliername.required");
        grnForm.supplierName.focus();
        return false;
    }
    if (trimAll(grnForm.invoice_no.value) == '') {
    	if(doAllowStatus || doSchemaAllowStatus) {
    		showMessage("js.stores.procurement.enterdono");
    	} else {
    		showMessage("js.stores.procurement.enterinvoiceno");
    	}
        grnForm.invoice_no.value = '';
        grnForm.invoice_no.focus();
        return false;
    }
    if (grnForm.invoice_date.value == '') {
    	if(doAllowStatus || doSchemaAllowStatus) {
    		showMessage("js.stores.procurement.selectdodate");
    	} else {
    		showMessage("js.stores.procurement.selectinvoicedate");
    	}
        grnForm.invoice_date.focus();
        return false;
    }
    if (grnForm.due_date.value == '') {
        showMessage("js.stores.procurement.selectduedate");
        grnForm.due_date.focus();
        return false;
    }
    if (grnForm.invoice_date.value != "") {
        var valid = true;
        valid = doValidateDateField(grnForm.invoice_date, 'past');
        if (!valid) {
            grnForm.invoice_date.focus();
            return false;
        }
    }
    if (grnForm.due_date.value != "") {
        var valid = true;
        valid = doValidateDateField(grnForm.due_date, '');
        if (!valid) {
            grnForm.due_date.focus();
            return false;
        }
    }

    if (strictPO) {
        if (grnForm.po_no.value == '') {
            showMessage("js.stores.procurement.selectpo");
            grnForm.po_no.focus();
            return false;
        }
    }

    if (grnForm.debit_amt.value == '')
		grnForm.debit_amt.value = 0;
    return true;
}


function getNumItems() {
    // header, hidden template row: totally 3 extra
    return document.getElementById("medtabel").rows.length - 2;
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

function onEditRow(img) {
    var row = findAncestor(img, "TR");
	openEditDialog(row);
}

function onDeleteRow(obj) {
    var row = getThisRow(obj);
	var rowIndex = getRowItemIndex(row);
	if (gRowItems[rowIndex].grnmed == 'Y')
		return false;

	// delete from the grid
    row.parentNode.removeChild(row);

	// delete the row in gRowItems
	for (var i=rowIndex+1; i< gRowItems.length; i++) {
		// move one backward.
		gRowItems[i-1] = gRowItems[i];
	}
	gRowItems.length = gRowItems.length - 1;
    resetTotals();
    return false;
}

function getVatTypePref() {
	return gDefaultVatType;
}

function resetTotals() {

    var totDiscountPaise = 0;
    var totschemeDiscountPaise = 0;
    var totAmountPaise = 0;
    var totVatPaise = 0;
    var cRate = (grnForm.cess_tax_rate) ? grnForm.cess_tax_rate.value : 0;

    for(var j=0; j < groupListJSON.length; j++) {
	 	setFieldHtml("taxamtlabel_", "0", groupListJSON[j].item_group_id);
	}

    for (var i = 0; i < gRowItems.length; i++) {
        var item = gRowItems[i];
		totDiscountPaise += item.discount_paise;
		totschemeDiscountPaise += item.scheme_discount_paise;
		totAmountPaise += item.med_total_paise;
		totVatPaise += item.tax_paise;

		for(var j=0; j < groupListJSON.length; j++) {
			var itemGroupId = groupListJSON[j].item_group_id;
			var taxAmt = 'taxamount'+itemGroupId;
			if(item[taxAmt]){
				var taxAmt = parseFloat(item[taxAmt]);
				var existTotalTaxAmt = parseFloat(getFieldHtml("taxamtlabel_", itemGroupId));
				setFieldHtml("taxamtlabel_", parseFloat(existTotalTaxAmt + taxAmt).toFixed(decDigits), itemGroupId);
			}
		}
    }

	setElementText('lblTotalDisc', formatAmountPaise(totDiscountPaise));
	setElementText('lblTotalschemeDisc', formatAmountPaise(totschemeDiscountPaise));
	setElementText('lblTotalVat', formatAmountPaise(totVatPaise));
	setElementText('lblTotalItemAmt', formatAmountPaise(totAmountPaise));

    /*var cessTaxAmtPaise = parseFloat(cRate) * (totVatPaise) / 100;
    if (prefCESS == 'Y') {
		setElementPaise(grnForm.cess_tax_amt, cessTaxAmtPaise);
		setElementText('lblCessAmt', grnForm.cess_tax_amt.value);
	}*/

    var otherChargesPaise = getElementPaise(grnForm.other_charges);
    var transportationChargePaise = getElementPaise(grnForm.transportation_charges);
    var invoiceAmtPaise = transportationChargePaise + otherChargesPaise + totAmountPaise /*+ cessTaxAmtPaise*/;
    var invoiceDiscType = invdiscSelection();
	var discAmtPaise = 0;
	var grnTcsAmountPaise = 0;

    if (invoiceDiscType == 'P') {
		grnForm.inv_discount_per.value = grnForm.inv_discount_val.value;
		var discPer = getElementAmount(grnForm.inv_discount_val);
		discAmtPaise = invoiceAmtPaise * discPer / 100;

    } else {
		grnForm.inv_discount_per.value = '';
		discAmtPaise = getElementPaise(grnForm.inv_discount_val);
    }

	setElementPaise(grnForm.inv_discount, discAmtPaise);
	setElementText('lblInvDisc', grnForm.inv_discount.value);
	
	var tcsType = grnForm.tcs_type.value;
	if (tcsType == 'P') {
		var tcsPer = parseFloat(grnForm.tcs_value.value);
		grnForm.inv_tcs_per.value = tcsPer;
		grnTcsAmountPaise = (tcsPer * invoiceAmtPaise) / 100;
	} else {
		if(grnForm.po_no.value != '') {
			var tcsValue = getPaise(grnForm.tcs_value.value);
			if(initialTCSValue != grnForm.tcs_value.value){ // if the TCS value is changed from the UI then it overrides else calculation needs to be done
        grnTcsAmountPaise = tcsValue; 
      }else{
        //to handle multiple GRN with single PO,the TCS percentage will be calculated on PO TCS Amt and calculate the TCS amt for each GRN
        var poinvAmt = getPaise(grnForm.po_inv_amt.value) - tcsValue - getPaise(grnForm.round_off.value);
        var tcsPer = (tcsValue * 100 / poinvAmt);
        grnTcsAmountPaise = (tcsPer * invoiceAmtPaise) / 100;
      }
		} else {
			grnTcsAmountPaise = getElementPaise(grnForm.tcs_value);
		}
	}
	setElementPaise(grnForm.inv_tcs_amount, grnTcsAmountPaise);
	setElementText('lblGrnTcsAmount', grnForm.inv_tcs_amount.value);

	var roundOffPaise = getElementPaise(grnForm.round_off);

    var gdTotal = invoiceAmtPaise - discAmtPaise + grnTcsAmountPaise + roundOffPaise;

	setElementText('lblGrandTotal', formatAmountPaise(gdTotal));
}

/********************    Dialog related functions follow ********************/

function initItemAutoComplete() {
    if (medAutoComp != undefined) {
        medAutoComp.destroy();
    }

	var dataSource = new YAHOO.util.DataSource(cpath + "/pages/stores/getItemMaster.do");
	dataSource.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList: "result",
		fields: [{key: "cust_item_code_with_name"},{key: "medicine_id"}, {key: "medicine_name"}]
	};

	medAutoComp = new YAHOO.widget.AutoComplete(dlgForm.medicine_name, 'item_dropdown', dataSource);
	medAutoComp.maxResultsDisplayed = 50;
	medAutoComp.allowBrowserAutocomplete = false;
	medAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	medAutoComp.typeAhead = false;
	medAutoComp.useShadow = false;
	medAutoComp.animVert = false;
	medAutoComp.minQueryLength = 2;
	medAutoComp.forceSelection = true;
	medAutoComp.queryDelay = 0.6;
	medAutoComp.typeAheadDelay = 0.7;
	medAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	medAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	medAutoComp.itemSelectEvent.subscribe(onSelectItem);
}

function initBatchNoAutoComplete(batchArray) {
    if (batchAutoComp != undefined) {
        batchAutoComp.destroy();
    }
    YAHOO.example.ACJSAddArray = new function () {
        var dataSource = new YAHOO.widget.DS_JSArray(batchArray);
		dataSource.responseSchema = {
			resultsList : "result",
			fields : [ {key : "batch_no"} ]
		};

        batchAutoComp = new YAHOO.widget.AutoComplete(dlgForm.batch_no, 'identifier_dropdown', dataSource);
        batchAutoComp.maxResultsDisplayed = 10;
        batchAutoComp.allowBrowserAutocomplete = false;
        batchAutoComp.prehighlightClassName = "yui-ac-prehighlight";
        batchAutoComp.typeAhead = false;
        batchAutoComp.useShadow = false;
        batchAutoComp.animVert = false;
        batchAutoComp.minQueryLength = 0;
        batchAutoComp.textboxChangeEvent.subscribe(onChangeBatchNo);
    }
	if (document.activeElement == dlgForm.batch_no) {
		// we already have focus: intimate the autocomp that this is the case
		batchAutoComp._onTextboxFocus(null, batchAutoComp);
	}
}

function openAddDialog() {
	clearTaxSelection();
	clearTaxFields();
	applyStrictPOControls(false);
	dlgForm.adj_mrp.value = 0;
	dlgForm.store_package_uom.value = 'P';
	dlgForm.tax_type.selectedIndex = 0;
	dlgForm.po_billed_qty.value = 0;
	dlgForm.po_bonus_qty.value = 0;
	dlgForm.pomed.value = 'N';
	if (!mandatoryfieldsval())
		return false;

	//displayItemLvlCst();
	gRowUnderEdit = -1;
	button = document.getElementById("plusItem");
	document.getElementById("addEditDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById("prevDialog").disabled = true;
	document.getElementById("nextDialog").disabled = true;

	dlgForm.supplier_code_hid.value = grnForm.supplier_id.value;
	dlgForm.store_id_hid.value = grnForm.store_id.value;
	gDialogItem = newItem();
//	if(grnForm.main_cst_rate)
//		setCST();
	itemToDialog(gDialogItem);
	detaildialog.show();
	if (prefBarCode == 'Y') {
		setTimeout("dlgForm.item_barcode_id.focus()", 100);
	} else {
		dlgForm.billed_qty_display.focus();
		setTimeout("dlgForm.medicine_name.focus()", 100);
	}
}

function openEditDialog(row) {
	clearTaxFields();
	dlgForm.store_package_uom.value = 'P';
	row.className = 'editing';
	gRowUnderEdit = getRowItemIndex(row);

    rowItemToDialog(gRowUnderEdit);
    document.getElementById('lblvat_rate').textContent = dlgForm.tax_rate.value;
    document.getElementById('lblvat_amt').textContent = dlgForm.tax.value;

    for(var i=0; i < groupListJSON.length; i++) {
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
    //displayItemLvlCst();

	var button = row.cells[EDIT_COL];
	dlgForm.supplier_code_hid.value = grnForm.supplier_id.value;
	dlgForm.store_id_hid.value = grnForm.store_id.value;
	document.getElementById("addEditDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);

	document.getElementById("prevDialog").disabled = false;
	document.getElementById("nextDialog").disabled = false;
	//getTaxDetails('dlgForm');
	if (strictPO && applyStrictPoControls && grnForm.po_no.value != ''){
		applyStrictPOControls(true);
	} 
	detaildialog.show();
    setTimeout("dlgForm.medicine_name.focus()", 100);
}
var form8Hcal = false;
function onDialogSave() {
	if (gDialogItem.po_mrp != "" && gDialogItem.po_mrp != undefined && gDialogItem.po_mrp != gDialogItem.mrp) {
		alert(getString("js.stores.procurement.bonusqty.validation1")
			+" "+gDialogItem.po_mrp+" "
			+getString("js.stores.procurement.bonusqty.validation2")
			+" "+gDialogItem.mrp
			+getString("js.stores.procurement.bonusqty.validation3"));
	}
	if (!dialogValidate())
		return false;

	form8Hcal = true;
	// copy dialog to dialogItem
	dialogToItem(gDialogItem);
	if (!dialogItemValidate())
		return false;

	dialogSave();
	form8Hcal = false;
	return true;
}

function onNextPrev(val) {
	if (!dialogValidate())
		return false;

	// copy dialog to dialogItem
	dialogToItem(gDialogItem);

	if (!dialogItemValidate())
		return false;

	dialogSave();

	var index = (val.name == 'prevDialog') ? gRowUnderEdit - 1 : gRowUnderEdit + 1;
	if (index >= getNumItems() || index == -1)
		return;

	openEditDialogIndex(index);
	return true;
}

function openEditDialogIndex(rowIndex) {
	openEditDialog(getItemRow(rowIndex));
}

function dialogSave() {

	if (gRowUnderEdit == -1) {
		// new item addition
		addDialogItemToGrid();
        detaildialog.cancel();
        openAddDialog();

	} else {
		// existing item edited and saved
		saveDialogItemToGrid(gRowUnderEdit);
        detaildialog.cancel();
	}

}

function onChangeMrp() {
	dlgForm.discount.value = '0';		// force recalc of discount & ced amt
	dlgForm.scheme_discount.value = '0';
	dlgForm.item_ced.value = '0';
	formatAmountObj(dlgForm.mrp_display);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeCostPrice() {
	if(!validatePoCostPrice()) {
		return;
	}
	dlgForm.discount.value = '0';
	dlgForm.scheme_discount.value = '0';
	dlgForm.item_ced.value = '0';
	formatAmountObj(dlgForm.cost_price_display);
	getSupplierRateValue();
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function validateQty(item_batch_id){
	// check that the total amount is not changed unless some quantity is also changed.
	var grnItem = findInList(grnItemsList, 'item_batch_id', item_batch_id);
	if (grnItem != null) {
		if (dlgForm.cost_price_display.value != grnItem.cost_price_display
			|| dlgForm.discount.value != grnItem.discount
			|| dlgForm.scheme_discount.value != grnItem.scheme_discount
			|| dlgForm.item_ced.value != grnItem.item_ced
			) {
			if (dlgForm.billed_qty_display.value*dlgForm.grn_pkg_size.value == grnItem.billed_qty
					&& dlgForm.bonus_qty_display.value*dlgForm.grn_pkg_size.value == grnItem.bonus_qty) {
				showMessage("js.stores.procurement.changingofrate.discountnotallowed");
				dlgForm.cost_price_display.value = grnItem.cost_price_display;
				dlgForm.discount.value = grnItem.discount;
				dlgForm.discount_per.value = grnItem.discount_per;
				dlgForm.scheme_discount.value = grnItem.scheme_discount;
				dlgForm.scheme_discount_per.value = grnItem.scheme_discount_per;
				dlgForm.item_ced.value = grnItem.item_ced;
				dlgForm.item_ced_per.value = grnItem.item_ced_per;
				return false;
			}
		}
	}

	return true;
}

function validatePoQty() {
	var valid = true;
	if (grnForm.po_no.value != '' && dlgForm.pomed.value != 'N') {
		const unitGrnQty = qtyUnitSelection() == 'P' ? Number(dlgForm.billed_qty_display.value) * Number(dlgForm.grn_pkg_size.value)
			 : Number(dlgForm.billed_qty_display.value);
		if (unitGrnQty > Number(dlgForm.po_billed_qty.value)
			|| dlgForm.bonus_qty_display.value*dlgForm.grn_pkg_size.value > dlgForm.po_bonus_qty.value) {
			dlgForm.billed_qty_display.value = qtyDisplay(parseFloat(dlgForm.po_billed_qty.value), dlgForm.grn_pkg_size.value);
			dlgForm.bonus_qty_display.value = qtyDisplay(parseFloat(dlgForm.po_bonus_qty.value), dlgForm.grn_pkg_size.value);
			valid = false;
		}
	}
	return valid;
}

function onChangeBilledQty() {
	var valid = validatePoQty();
	if (!valid) {
		showMessage("js.stores.procurement.billedqty.notexceedpoqty");
		return false;
	}
	dlgForm.item_ced.value = '0';
	dlgForm.discount.value = '0';
	dlgForm.scheme_discount.value = '0';
	formatAmountObj(dlgForm.billed_qty_display, true);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeBonusQty() {
	var valid = true;
	if(strictPO && applyStrictPoControls && grnForm.po_no.value != '') {
		valid = validatePoQty();
	}
	if (!valid) {
		showMessage("js.stores.procurement.bonusqty.notexceedpoqty");
		return false;
	}
	formatAmountObj(dlgForm.bonus_qty_display, true);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeTaxType() {
	//dlgForm.item_ced.value = '0';
	dlgForm.discount.value = '0';
	dlgForm.scheme_discount.value = '0';
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeCedPer() {
	dlgForm.item_ced.value = '0';
	formatAmountObj(dlgForm.item_ced_per);
	calcDialogValues();
}

function onChangeCedAmt() {
	dlgForm.item_ced_per.value = '0';
	formatAmountObj(dlgForm.item_ced);
	calcDialogValues();
}

function onChangeDiscountPer() {
	dlgForm.discount.value = '0';
	dlgForm.item_ced.value = '0';
	formatAmountObj(dlgForm.discount_per);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeDiscountAmt() {
	dlgForm.discount_per.value = '0';
	formatAmountObj(dlgForm.discount);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeSchemeDiscountPer() {
	dlgForm.scheme_discount.value = '0';
	dlgForm.item_ced.value = '0';
	formatAmountObj(dlgForm.scheme_discount_per);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeSchemeDiscountAmt() {
	dlgForm.scheme_discount_per.value = '0';
	formatAmountObj(dlgForm.scheme_discount);
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onChangeTaxPer() {
	calcDialogValues();
	getTaxDetailsOnChange('dlgForm');
}

function onClickPurchaseDetails() {
	var medId = dlgForm.medicine_id.value;
	var storeId = document.getElementsByName("store_id")[0].value;
	if (!medId) {
		showMessage("js.stores.procurement.selectitemforshowingpurchasedetails");
		return false;
	} else {
		showPurchaseDetails(medId,storeId);
		return false;
	}
}

function onKeyPressAddQty(e) {
    e = (e) ? e : event;
    var valid = validatePoQty();
	if (!valid) {
		showMessage("js.stores.procurement.billedqty.notexceedpoqty");
		return false;
	}
    var charCode = (e.charCode) ? e.charCode : ((e.which) ? e.which : e.keyCode);
    if (charCode == 13 || charCode == 3) {
    	getTaxDetailsOnChange('dlgForm');
		onDialogSave();
        return false;
    }
}

/*
 * Called when the barcode ID text field is changed.
 */
function getItemBarCodeDetails(val) {
    if (val == '') {
        dlgForm.medicine_name.value = '';
        dlgForm.medicine_id.value = '';
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
        dlgForm.medicine_name.value = '';
        dlgForm.medicine_id.value = '';
    } else {
        dlgForm.medicine_name.value = item.medicine_name;
		medAutoComp._bItemSelected = true;
		dlgForm.medicine_id.value = item.medicine_id;
		getItemDetailsForItem(item.medicine_id);
	}
}

/*
 * Called on selection of an item in the item auto comp
 */
function onSelectItem(type, args) {
	if (dlgForm.medicine_name.readOnly)
		return;
	var mId = args[2][1];
	clearTaxSelection();
	dlgForm.medicine_id.value = mId;
	dlgForm.medicine_name.value = args[2][2];
	getItemDetailsForItem(mId);		// This will also populate the dialog
	getTaxDetails('dlgForm');
}

/*
 * Fetch the details for the given item and populate into the dialog.
 * This may fetch from the local cache if the item is already available.
 */
function getItemDetailsForItem(medicineId) {
	var itemDetails = gItemDetails[medicineId];

	if (itemDetails != null) {
		populateItemDetails(itemDetails);
		return;
	}

	var storeId = grnForm.store_id.value;
	var supplierId = grnForm.supplier_id.value;

	// item details not in our cache: get it using ajax
	var url = "stockentry.do?_method=getItemDetails" + "&medicineId=" + medicineId + "&store=" + storeId
		+ "&supplierId=" + supplierId;

	YAHOO.util.Connect.asyncRequest('GET', url, {
       	success: function(response) {
			eval('var item =' + response.responseText);
			//tax is not applicable for non-registerd supplier at supplier tax rule schema
			if ( !taxApplicable ){
				item.tax_rate = 0;
			}
			item.item_max_cost_price = item.max_cost_price;
			gItemDetails[medicineId] = item;
			populateItemDetails(item);
		},
		failure: onAjaxFailure,
	});
}

/*
 * Use the item details object to fill in all the defaults when the item is selected.
 * Batch properties are not set here, this is for item only.
 * Bin is technically not a batch property, instead is a item + store property, but is available
 * only in store_stock_details. That is also handled here.
 */
function populateItemDetails(item) {
	if (item == null)
		return;

	dlgForm.item_max_cost_price.value = item.item_max_cost_price;
	if(item.supplier_rate_validation != null && item.supplier_rate_validation == "true"){
		dlgForm.max_cost_price.value = parseFloat(item.cost_price).toFixed(decDigits);
		dlgForm.supplier_rate_validation.value = item.supplier_rate_validation;
		item.max_cost_price = item.cost_price;
	}

	gDialogItem = newItem();		// with all the defaults
	// set the default values from the item to gDialogItem and transfer that to the dialog
	shallowCopy(item, gDialogItem); 	// medicine_id, medicine_name, item_barcode_id, issue_units, exist,
	// identification, expiry_date_val, billable, bin, cost_price
    gDialogItem.grn_pkg_size = item.issue_base_unit;
    gDialogItem.grn_package_uom = item.package_uom;
    gDialogItem.issue_base_unit = item.issue_base_unit;
    gDialogItem.master_tax_rate = item.tax_rate;
    gDialogItem.discount_per = item.discount;


	if (item.identification == 'S') {
		gDialogItem.asset_approved = 'N';
		if (gRowUnderEdit == -1) {
			// Add mode of dialog only
			gDialogItem.batch_no = identifierSeq;
		}
	} else {
		gDialogItem.asset_approved = 'Y';
	}

	calcItemValues(gDialogItem, 'stored');
	itemToDialog(gDialogItem);
}

function displayPackageSize(pkgSizeId, issueUom, packageUom) {
    if (packageUom != '') {
        var uomDetails = findInList2(packageUOMs, 'package_uom', packageUom, 'issue_uom', issueUom);
        if (uomDetails != null) {
            setElementText(pkgSizeId, uomDetails.package_size);
			dlgForm.grn_pkg_size.value = uomDetails.package_size;
            return;
        }
    }
    setElementText(pkgSizeId, '');
}

function displayUserUOM() {
    var qtySel = qtyUnitSelection();
    if (qtySel == 'I') {
        // show the issue units of the item
		gDialogItem.uom_display = gDialogItem.issue_units;
    } else {
        // show the package units of the item
        gDialogItem.uom_display = gDialogItem.package_uom;
    }
	setElementText('UOMDesc', gDialogItem.uom_display);
}

function onChangeBatchNo() {
    var batchNo = dlgForm.batch_no.value;
    var itemId = dlgForm.medicine_id.value;
    batchExist = false;

    if (batchNo == "")
		return;

	var batch = findInList(gItemDetails[itemId].batches, 'batch_no', batchNo);

	if (batch) {
		dialogToItem(gDialogItem);		// retain some values user has already typed, if any.

		// overwrite batch specific attributes
		gDialogItem.newbatch = 'N';
		gDialogItem.exp_dt = batch.exp_dt;
		gDialogItem.consignment_stock = batch.consignment_stock;

		// prefer the PO MRP and tax rate to existing batch (Bug 31217).
		// Allowing the batchMRP (Bug HMS-27678)
		gDialogItem.tax_rate = batch.tax_rate;
		gDialogItem.mrp = batch.mrp;

		// calculate the display values like exp_dt etc.
		calcItemValues(gDialogItem, 'stored');

		// push it back to the dialog
		//itemToDialog(gDialogItem) 	cannot use this, it will reinitialize the batch autocomp and mess it
		objectToForm(gDialogItem, dlgForm);
		onChangeMrp();
		enableNewBatchFields(false);

	} else {
		gDialogItem.newbatch = 'Y';
		enableNewBatchFields(true);
	}
	
	if (strictPO && applyStrictPoControls && grnForm.po_no.value != ''){
		applyStrictPOControls(true);
	} 
}

function enableNewBatchFields(enable) {
    var disable = !enable;
    dlgForm.exp_dt_mon.disabled = disable;
    dlgForm.exp_dt_year.disabled = disable;
    dlgForm.mrp_display.disabled = disable;
}

function addRow() {
    var totalNoOfRows = getNumItems();
    var table = document.getElementById("medtabel");
    var templateRow = table.rows[getTemplateRow()];
    var row = templateRow.cloneNode(true);
    row.style.display = '';
    table.tBodies[0].insertBefore(row, templateRow);
	return totalNoOfRows;
}

function handleDetailDialogCancel() {
    detaildialog.cancel();
    removeRowClasses();
    grnForm.saveStk.focus();
}

function removeRowClasses() {
    var totalNoOfRows = getNumItems();
    for (var i = 0; i < totalNoOfRows; i++) {
        var row = getItemRow(i);
        row.className = '';
    }
}

function matches(mName, autocomplete) {
    var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ? (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
    elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
    return elListItem;
}

/*
 * Transforms: deal with qtyUnit based stored-display conversion (and other, like exp_dt)
 * and calculated values.
 *
 * The base storage is a javascript array of objects (gRowItems). All the transforms are from/to
 * one object in this array. Each item in the array represents a row in the UI. The object contains
 * a superset of all required variations of a field, ie, stored values, display values and paise
 * values.
 *
 * Convention:
 *  Object names match those in the DB. Display values are <field>_display and paise
 *  values are <field>_paise.
 *
 * These are the various transforms we need to deal with:
 *  obj -> dialog display values (object to form)
 *  obj -> row hidden values (object to form)
 *  obj -> row labels (object to labels)
 *  dialog -> obj (form to obj)
 *  DB -> obj (nothing to transform, only calc)
 *
 */

function rowItemToDialog(rowIndex) {
	// we need a shallow copy so that a cancel will not affect the original
	gDialogItem = {};
	shallowCopy(gRowItems[rowIndex], gDialogItem);
	itemToDialog(gDialogItem, dlgForm);
}

function rowItemToRow(rowIndex) {
	// copy to hidden values in row: call common object to form, with indexed grnForm as the target
	var row = getItemRow(rowIndex);
	objectToHidden(gRowItems[rowIndex], row);
	// copy to labels in table: call common object to label
	objectToRowLabels(gRowItems[rowIndex], row, gColIndexes);
	if (!doAllowStatus && gColIndexes['tax_type'] ) {
		//set tax type title
		var taxType = row.cells[gColIndexes.tax_type].textContent;
		row.cells[gColIndexes['tax_type']].title = ( taxType == 'MB'
										? "MRP Based(with bonus)" : (
								 taxType == 'M'
								 		? 'MRP Based(without bonus)' : (
						 		 taxType == 'C' ?  'CP Based(without bonus)' : 'CP Based(with bonus)')));
	}
}

function dialogToItem(item) {
	formToObject(dlgForm, item);
	for(var i=0; i < groupListJSON.length; i++) {
		var p = groupListJSON[i].item_group_id;
		var taxName = 'taxname'+p;
		var taxRate = 'taxrate'+p;
		var taxAmount = 'taxamount'+p;
		var taxSubgroup = 'taxsubgroupid'+p;
		if(document.getElementById('taxamount_'+p) && document.getElementById('taxamount_'+p).value) {
			item[taxAmount] = document.getElementById('taxamount_'+p).value;
		}
		if(document.getElementById('taxrate_'+p) && document.getElementById('taxrate_'+p).value) {
			item[taxRate] = document.getElementById('taxrate_'+p).value;
		}
		if(document.getElementById('taxname_'+p) && document.getElementById('taxname_'+p).value) {
			item[taxName] = document.getElementById('taxname_'+p).value;
		}
		if(document.getElementById('taxsubgroupid_'+p) && document.getElementById('taxsubgroupid_'+p).value) {
			item[taxSubgroup] = document.getElementById('taxsubgroupid_'+p).value;
		}
	}
	// special case: expiry date display is to be calculated
	if (dlgForm.exp_dt_mon.value != "" && dlgForm.exp_dt_year.value != '') {
		item.exp_dt_display = dlgForm.exp_dt_mon.value + "-" + dlgForm.exp_dt_year.value;
	} else {
		item.exp_dt_display = "";
	}
	calcItemValues(item, 'display');
}

/*
 * itemToDialog: this is more than just a transform. It also initializes the dialog
 * fields like the Package UOM dropdown and enables/disables fields based on the
 * item that is being shown.
 */
function itemToDialog(item) {
	// call common object to form, with dlgForm as the target
	objectToForm(item, dlgForm);
	// initialize auto completes
	if (item.medicine_id != '') {
		medAutoComp._bItemSelected = true;
		initBatchNoAutoComplete(gItemDetails[item.medicine_id].batches);
		if (item.batch_no != '')
			batchAutoComp._bItemSelected = true;
	}
	dlgForm.grn_pkg_size.value = gDialogItem.issue_base_unit;
    setElementText('lblGrnPkgSize', gDialogItem.issue_base_unit);
    if(document.getElementById("lbltax_rate")) {//vat applicability can be No
    	document.getElementById("lbltax_rate").innerHTML = gDialogItem.tax_rate;
    }
    if(gDialogItem.adj_mrp) {
    	dlgForm.adj_mrp_display.value = gDialogItem.adj_mrp;
    }

    dlgForm.store_package_uom.value = 'P';

	displayUserUOM();

	enableNewBatchFields(item.newbatch == 'Y');

	dlgForm.billed_qty_display.readOnly = (item.identification == 'S') && (gRowUnderEdit != -1);
	dlgForm.bonus_qty_display.readOnly = (item.identification == 'S') && (gRowUnderEdit != -1);

	dlgForm.medicine_name.readOnly = (item.grnmed == 'Y');
	dlgForm.medicine_name.disabled = (doSchemaAllowStatus);
	if (prefBarCode == 'Y') dlgForm.item_barcode_id.readOnly = (item.grnmed == 'Y');
	dlgForm.exp_dt_mon.readOnly = (item.grnmed == 'Y');
	dlgForm.exp_dt_year.readOnly = (item.grnmed == 'Y');
	dlgForm.cost_price_display.readOnly = (item.grnmed == 'Y');
	dlgForm.discount_per.readOnly = (item.grnmed == 'Y');
	dlgForm.discount.readOnly = (item.grnmed == 'Y');
	dlgForm.scheme_discount_per.readOnly = (item.grnmed == 'Y');
	dlgForm.scheme_discount.readOnly = (item.grnmed == 'Y');
	dlgForm.item_ced_per.readOnly = (item.grnmed == 'Y');
	dlgForm.item_ced.readOnly = (item.grnmed == 'Y');
	dlgForm.batch_no.readOnly = (item.identification == 'S') || (item.grnmed == 'Y');
	dlgForm.tax_type.disabled = (strictPO) || (item.identification == 'S') || (item.grnmed == 'Y');
	if (item.batch_no_applicable == 'N') {
		dlgForm.batch_no.value = '---';
		dlgForm.batch_no.readOnly = true;
		var batches = gItemDetails[item.medicine_id].batches;
		if (batches!= null && batches.length > 0) {
			batchAutoComp._onTextboxFocus(null, batchAutoComp);
			batchAutoComp._bItemSelected = true;
			onChangeBatchNo();
		}
	}
}

/*
 * Create a new item, contains a superset of all required fields
 */
function newItem() {
	var item = {
		medicine_name: '', medicine_id: '', batch_no: '', item_barcode_id: '',
		grn_pkg_size: 1, issue_units: '', grn_package_uom: '', uom_display: '',
		bin: '', asset_approved: 'Y', exist: 0,

		exp_dt: null, exp_dt_display: '', exp_dt_mon: '', exp_dt_year: '',
		mrp: 0, mrp_display: 0, mrp_paise: 0,
		adj_mrp: 0, adj_mrp_display: 0, adj_mrp_paise: 0,
		billed_qty: 0, billed_qty_display: 0, bonus_qty: 0, bonus_qty_display: 0,
		cost_price: 0, cost_price_display: 0, cost_price_paise: 0,
		tax_rate: gDefaultTaxRate, tax: 0, tax_paise: 0, bonus_tax: 0, bonus_tax_paise: 0,
		identification: 'B', billable: true, expiry_date_val: true,
		item_ced_per: 0, item_ced: 0, item_ced_paise: 0,
		discount_per: 0, discount: 0, discount_paise: 0,
		scheme_discount_per: 0, scheme_discount: 0, scheme_discount_paise: 0,
		med_total: 0, med_total_paise: 0,cst_rate: 0, master_tax_rate: 0,

		grnmed: 'N', pomed: 'N', pomedrate: 0, pomedrate_display: 0,
		gnrqty: 0, grnbqty: 0, qty_req: 0, qty_received: 0,
		newbatch: 'Y', batches: [], code_type:  '', item_code: ''
	};
	item.tax_rate = 0;

    if (itemDiscountType() == 'P') item.discount_per = getDefaultDiscount();
    else item.discount = getDefaultDiscount();

	if (grnForm.tax_name.value == taxLabel) {
		item.tax_type = getVatTypePref();
	} else {
		// CST: use the first char of the the vat type only: M or C. MB and CB are not availalble for CST
		item.tax_type = getVatTypePref().substring(0,1);
	}

	for(var i=0; i < groupListJSON.length; i++) {
		var p = groupListJSON[i].item_group_id;
		var taxName = 'taxname'+p;
		var taxRate = 'taxrate'+p;
		var taxAmount = 'taxamount'+p;
		var taxSubgroup = 'taxsubgroupid'+p;
		item[taxName] = '';
		item[taxRate] = '';
		item[taxAmount] = '';
		item[taxSubgroup] = '';
	}

	return item;
}

/*
 * Add a validated dialog item to the grid.
 */
function addDialogItemToGrid() {

	if (gDialogItem.identification != 'S') {
		var rowIndex = addRow();
		saveDialogItemToGrid(rowIndex);

	} else {
		// Serial item: loop through quantities and add one row for each. Use only
		// discount_per, cannot apply entire discount to all items.
		var billedQty = gDialogItem.billed_qty;
		var bonusQty = gDialogItem.bonus_qty;
		gDialogItem.discount = 0;
		gDialogItem.scheme_discount = 0;
		for (var i=0; i<(billedQty + bonusQty); i++) {
			var item = {};
			shallowCopy(gDialogItem, item);
			item.billed_qty = (i<billedQty) ? 1 : 0;
			item.bonus_qty  = (i<billedQty) ? 0 : 1;
			item.batch_no = identifierSeq++;
			setSerialTaxDetails(item, gDialogItem);
			calcItemAndAddRow(item);
		}
		gDialogItem = null;
		resetTotals();
	}
}

function setSerialTaxDetails(item, gDialogItem) {
	var billedQty = gDialogItem.billed_qty;
	var bonusQty = gDialogItem.bonus_qty;
	var totalQty = billedQty + bonusQty;

	var taxType = item.tax_type;
	var taxAmt = item.tax;
	var taxPaise = item.tax_paise;
	if(taxType == 'MB' || taxType == 'CB') {
		item.tax = formatAmountPaise(getPaise(taxAmt/totalQty));
		item.tax_paise = getPaise((taxPaise/100)/totalQty);

		for(var i=0; i < groupListJSON.length; i++) {
			var p = groupListJSON[i].item_group_id;
			var taxNameCol = 'taxname'+p;
			var taxRateCol = 'taxrate'+p;
			var taxAmtCol = 'taxamount'+p;
			var taxSubgroupIdCol = 'taxsubgroupid'+p;

			if(isNotNullObj(item[taxAmtCol])){
				var taxSplitAmt = item[taxAmtCol];
				item[taxAmtCol] = formatAmountPaise(getPaise(taxSplitAmt/totalQty));
			}
		}

	} else if(taxType == 'M' || taxType == 'C') {
		if(item.bonus_qty > 0) {
			item.tax = 0;
			item.tax_paise = 0;
			item.tax_rate = 0;

			for(var i=0; i < groupListJSON.length; i++) {
				var p = groupListJSON[i].item_group_id;
				var taxNameCol = 'taxname'+p;
				var taxRateCol = 'taxrate'+p;
				var taxAmtCol = 'taxamount'+p;
				var taxSubgroupIdCol = 'taxsubgroupid'+p;

				if(isNotNullObj(item[taxRateCol])){
					item[taxRateCol] = 0;
				}
				if(isNotNullObj(item[taxAmtCol])){
					item[taxAmtCol] = 0;
				}
				if(isNotNullObj(item[taxSubgroupIdCol])){
					item[taxSubgroupIdCol] = '';
				}
			}
		} else {
			item.tax = formatAmountPaise(getPaise(taxAmt/billedQty));
			item.tax_paise = getPaise((taxPaise/100)/billedQty);

			for(var i=0; i < groupListJSON.length; i++) {
				var p = groupListJSON[i].item_group_id;
				var taxNameCol = 'taxname'+p;
				var taxRateCol = 'taxrate'+p;
				var taxAmtCol = 'taxamount'+p;
				var taxSubgroupIdCol = 'taxsubgroupid'+p;

				if(isNotNullObj(item[taxAmtCol])){
					var taxSplitAmt = item[taxAmtCol];
					item[taxAmtCol] = formatAmountPaise(getPaise(taxSplitAmt/billedQty));
				}
			}
		}
	}
}

/*
 * Recalculate item values and add a row based on this. Used from automated population
 * into grid such as from PO and serial item addition.
 */
function calcItemAndAddRow(item) {
	calcItemValues(item, 'stored');
	var rowIndex = addRow();
	// set the row's item to the one we just created
	gRowItems[rowIndex] = item;
	// copy the item to the row
	rowItemToRow(rowIndex);
}

function saveDialogItemToGrid(rowIndex) {
	// item is already validated and saved
	gRowItems[rowIndex] = gDialogItem;
	gDialogItem = null;				// ensure we don't refer to this again.
	rowItemToRow(rowIndex);
	getItemRow(rowIndex).className = '';
	resetTotals();
}

function getLastDayForMonth(month, year) {
	var dt = new Date(parseInt(year),parseInt(month),0);
	return [(dt.getDate() < 10 ? ('0'+ dt.getDate()) : dt.getDate()),(dt.getMonth() < 9 ? ('0'+ (dt.getMonth()+1)) : (dt.getMonth()+1)),dt.getFullYear()].join('-');
}

/*
 * Calculate all the missing values in an item. This depends on whether we are
 * having all stored values, or whether we are having all display values.
 * Note: display values (stored/display) are text, but paise values are always numbers.
 */
function calcItemValues(item, valueType) {
	var pkgSize = getAmount(item.grn_pkg_size);
	var itemDetails = gItemDetails[item.medicine_id];
	if (valueType == 'stored') {
		// item contains stored values. Display values to be calculated, eg, when loading and editing grn
		item.cost_price_paise = getPaise(item.cost_price);
		item.mrp_paise = getPaise(item.mrp);
		item.pomedrate_paise = getPaise(item.pomedrate);

		item.cost_price_display = formatAmountPaise(rateDisplay(item.cost_price_paise, pkgSize));
		item.mrp_display = formatAmountPaise(rateDisplay(item.mrp_paise, pkgSize));
		item.pomedrate_display = formatAmountPaise(rateDisplay(item.pomedrate_paise, pkgSize));

		item.billed_qty_display = qtyDisplay(item.billed_qty, pkgSize);
		item.bonus_qty_display = qtyDisplay(item.bonus_qty, pkgSize);

		item.grnqty_display = qtyDisplay(item.grnqty, pkgSize);
		item.grnbqty_display = qtyDisplay(item.grnbqty, pkgSize);
		item.qty_req_display = qtyDisplay(item.qty_req, pkgSize);
		item.qty_received_display = qtyDisplay(item.qty_received, pkgSize);

		if (item.exp_dt) {
			var parts = item.exp_dt.split('-');		// stored is like 01-03-2015, display is like 03-15
			item.exp_dt_year = parts[2].substring(2,4);
			item.exp_dt_mon = parts[1];
			item.exp_dt_display = item.exp_dt_mon + "-" + item.exp_dt_year;
		} else {
			item.exp_dt_year = ''; item.exp_dt_mon = '';
			item.exp_dt = ''; item.exp_dt_display = '';
		}

	} else {
		// vice-versa: display values exist, eg, updating from dialog to row
		item.cost_price_paise = ratePackage(getPaise(item.cost_price_display), pkgSize);
		item.mrp_paise = ratePackage(getPaise(item.mrp_display), pkgSize);

		item.cost_price = getPaiseReverse(item.cost_price_paise);
		item.mrp = getPaiseReverse(item.mrp_paise);

		item.billed_qty = qtyUnit(parseFloat(item.billed_qty_display), pkgSize);
		item.bonus_qty = qtyUnit(parseFloat(item.bonus_qty_display), pkgSize);
		// grn and PO qty are static, they cannot be changed in display.

		if (item.exp_dt_display != "") {
			var parts = item.exp_dt_display.split('-');		// display => 03-15, stored => 31-03-15
			item.exp_dt_mon = parts[0];
			item.exp_dt_year = parts[1];
			item.exp_dt = getLastDayForMonth(item.exp_dt_mon, convertTwoDigitYear(item.exp_dt_year));
		} else {
			item.exp_dt = "";
		}
	}

	/*
	 * Calculate the Discount and CED based on amount or percent present in display
	 * If there is an amount, take it as is and reverse calc the percentage. If
	 * the amount is 0 and percentage is non-zero, calculate the amount. This means
	 * that the amount takes priority over percentage (because it does not have round-off
	 * inaccuracy as is there for the percentage). To trigger a recalc of the amount
	 * from the percentage, the amount has to be 0'd out explicitly.
	 */
	item.discount_per = getAmount(item.discount_per);
	item.discount = getAmount(item.discount);
	item.scheme_discount_per = getAmount(item.scheme_discount_per);
	item.scheme_discount = getAmount(item.scheme_discount);

	 if (strictPO && applyStrictPoControls && grnForm.po_no.value != '') {
         item.discount_per = getAmount(item.discount_per);
         item.discount_paise = getPaise(item.discount);
         item.scheme_discount_per = getAmount(item.scheme_discount_per);
         item.scheme_discount_paise = getPaise(item.scheme_discount);
    } else {
	if (item.discount != 0) {
		// use discount as is, but reverse calculate discount_per.
		item.discount_paise = getPaise(item.discount);
		if (item.cost_price_paise > 0 && item.billed_qty > 0) {
			item.discount_per = round2(100 * item.discount_paise /(item.cost_price_paise *
						item.billed_qty/pkgSize));
			item.discount_per_changed = true;
		} // else can be infinity, so don't calculate.

	} else if (item.discount_per != 0) {
		item.discount_paise = Math.round(
				item.cost_price_paise * item.billed_qty/pkgSize * item.discount_per/100 );
		item.discount = formatAmountPaise(item.discount_paise);
		item.discount_changed = true;

	} else {
		// both are 0
		item.discount_paise = 0;
	}

	if (item.scheme_discount != 0) {
		// use discount as is, but reverse calculate discount_per.
		item.scheme_discount_paise = getPaise(item.scheme_discount);
		if (item.cost_price_paise > 0 && item.billed_qty > 0) {
			item.scheme_discount_per = round2(100 * item.scheme_discount_paise /(item.cost_price_paise *
						item.billed_qty/pkgSize));
			item.scheme_discount_per_changed = true;
		} // else can be infinity, so don't calculate.

	} else if (item.scheme_discount_per != 0) {
		item.scheme_discount_paise = Math.round(
				item.cost_price_paise * item.billed_qty/pkgSize * item.scheme_discount_per/100 );
		item.scheme_discount = formatAmountPaise(item.scheme_discount_paise);
		item.scheme_discount_changed = true;

	} else {
		// both are 0
		item.scheme_discount_paise = 0;
	}
    }

//	item.item_ced_per = getAmount(item.item_ced_per);
//	item.item_ced = getAmount(item.item_ced);

//	if (item.item_ced != 0) {
//		item.item_ced_paise = getPaise(item.item_ced);
//		var totalPaise = item.cost_price_paise * item.billed_qty/pkgSize - (item.discount_paise+item.scheme_discount_paise);
//		if (totalPaise > 0) {
//			item.item_ced_per = round2(item.item_ced_paise * 100 / totalPaise);
//			item.item_ced_per_changed = true;
//		}
//
//	} else if (item.item_ced_per != 0) {
//		item.item_ced_paise = Math.round(
//			(item.cost_price_paise * item.billed_qty/pkgSize - (item.discount_paise+item.scheme_discount_paise)) * item.item_ced_per/100);
//		item.item_ced = formatAmountPaise(item.item_ced_paise);
//		item.item_ced_changed = true;
//
//	} else {
//		// both are 0
//		item.item_ced_paise = 0;
//	}

	/*if (prefVAT == 'Y') {
		if (taxSelection() == 'CST' || taxSelection() == 'iGST') {
			/*
			 * In case of CST, the tax_type and tax_rate are the outgoing tax type/rate only.
			 * The incoming tax is based only on the global CST rate, CP-based. CST needs to be
			 * calculated post discount.
			 */
			/*var cstRate = getAmount(item.cst_rate);
			if ( itemLevelCSTApplicable ){
				if (grnForm.c_form.checked && item.tax_rate >2) {
					item.master_tax_rate = 2;
				} else {
					item.master_tax_rate = item.tax_rate;
				}
				cstRate = item.master_tax_rate;
	        } else if (editGRN) {
	        	cstRate = item.tax_rate;
	        }

			item.tax_paise = Math.round((item.cost_price_paise*item.billed_qty/pkgSize -
							(item.discount_paise +item.scheme_discount_paise) + item.item_ced_paise) * cstRate / 100);
			item.bonus_tax_paise = 0;
			item.tax_type = '';
			item.tax_rate = cstRate;

		} else {
			// VAT

			if(form8HApplicable  && form8Hcal){

				if(gDialogItem  != null && gDialogItem.master_tax_rate != undefined && gDialogItem.master_tax_rate != '' ){
					item.tax_rate = gDialogItem.master_tax_rate;
				}

				item.tax_paise = 0;
				// bonus tax portion
				if (item.tax_type == 'MB') {
					item.bonus_tax_paise = item.adj_mrp_paise * item.bonus_qty/pkgSize
							* item.tax_rate / 100;

				} else if (item.tax_type == 'CB') {
					item.bonus_tax_paise = Math.round((item.cost_price_paise*item.bonus_qty/pkgSize
								) * item.tax_rate / 100);
				} else {
					item.bonus_tax_paise = 0;
				}

//				item.bonus_tax_paise = item.adj_mrp_paise * item.bonus_qty/pkgSize * item.tax_rate / 100;
				if ( item.bonus_tax_paise <= 0 ){
					item.tax_type = '';
				}
				// bonus tax portion
				if (item.tax_type == 'MB') {
					item.bonus_tax_paise = item.adj_mrp_paise * item.bonus_qty/pkgSize
							* item.tax_rate / 100;

				} else if (item.tax_type == 'CB') {
					item.bonus_tax_paise = Math.round((item.cost_price_paise*item.bonus_qty/pkgSize
								) * item.tax_rate / 100);
				} else {
					item.bonus_tax_paise = 0;
				}

//				item.bonus_tax_paise = item.adj_mrp_paise * item.bonus_qty/pkgSize * item.tax_rate / 100;
				if(item.bonus_qty == 0)
					item.tax_rate = 0;

			}else{

				if (item.tax_type == 'M' || item.tax_type == 'MB') {
						item.tax_paise = item.adj_mrp_paise * item.billed_qty / pkgSize * item.tax_rate/100;

				} else if (item.tax_type == 'C' || item.tax_type == 'CB') {
						item.tax_paise = (item.cost_price_paise*item.billed_qty/pkgSize -
								(item.discount_paise+item.scheme_discount_paise)+item.item_ced_paise) * item.tax_rate / 100;
				}


				// bonus tax portion
				if (item.tax_type == 'MB') {
					item.bonus_tax_paise = item.adj_mrp_paise * item.bonus_qty/pkgSize
							* item.tax_rate / 100;

				} else if (item.tax_type == 'CB') {
					item.bonus_tax_paise = Math.round((item.cost_price_paise*item.bonus_qty/pkgSize
								) * item.tax_rate / 100);
				} else {
					item.bonus_tax_paise = 0;
				}
			}


			item.tax_paise += item.bonus_tax_paise;
			item.tax_paise = Math.round(item.tax_paise);

		}

	} else {
		item.tax_paise = 0;
		item.bonus_tax_paise = 0;
	}*/
	/*if((item.tax_paise == undefined || item.tax_paise == 'undefined' || item.tax_paise == null) && (item.tax != undefined))
		item.tax_paise = getPaise(item.tax);
	else {
		item.tax_paise = 0;
	}*/


	item.med_total_paise = item.cost_price_paise * item.billed_qty / pkgSize
		- (item.discount_paise+item.scheme_discount_paise) + item.tax_paise;

	item.tax = formatAmountPaise(item.tax_paise);
	if(isNotNullObj(item.tax_type) && (item.tax_type == 'CB' || item.tax_type == 'MB')) {
		item.bonus_tax_paise = (item.tax_paise/(item.billed_qty+item.bonus_qty))*item.bonus_qty;
		item.bonus_tax = formatAmountPaise(item.bonus_tax_paise);
	} else {
		item.bonus_tax_paise = 0;
		item.bonus_tax = 0;
	}

	item.med_total = formatAmountPaise(item.med_total_paise);

	item.uom_display = (qtyUnitSelection() == 'I') ? item.issue_units : item.grn_package_uom;
}

function convertTwoDigitYear(year) {
	// convert 2 digit years intelligently
	if (year == '') return year;
	var now = new Date();
	var century = now.getFullYear();
	var s = century.toString();
	var yearPrefix = s.substring(0, 2);
	return (yearPrefix + year);
}

/*
 * Called when the dialog elements are changed. We need to re-calculate
 * the discount amount/ced etc. Note that either percentage or amt can
 * be changed
 */
function calcDialogValues() {
	var item = gDialogItem;
	dialogToItem(item);

	if (item.discount_changed)
		dlgForm.discount.value = item.discount;
	if (item.discount_per_changed)
		dlgForm.discount_per.value = item.discount_per;

	if (item.scheme_discount_changed)
		dlgForm.scheme_discount.value = item.scheme_discount;
	if (item.scheme_discount_per_changed)
		dlgForm.scheme_discount_per.value = item.scheme_discount_per;

//	if (item.item_ced_changed)
//		dlgForm.item_ced.value = item.item_ced;
//	if (item.item_ced_per_changed)
//		dlgForm.item_ced_per.value = item.item_ced_per;

	if (item.mrp_changed)
		dlgForm.mrp_display.value = item.mrp_display;

	// the following will always change during calculations.
	dlgForm.tax.value = item.tax;
	dlgForm.med_total.value = item.med_total;
	//dlgForm.adj_mrp_display.value =  item.adj_mrp_display;
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
		return roundNumber(qty / pkgSize,2);
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
 * If empty, show 0 for some fields (not all, some are mandatory, don't touch them, eg,
 * if cost_price is empty, force user to enter 0 even if they want to)
 */
function dialogSetEmptyValues() {

	var amounts = [dlgForm.tax_rate, dlgForm.discount_per, dlgForm.discount, dlgForm.scheme_discount_per, dlgForm.scheme_discount];
	var quantities = [dlgForm.billed_qty_display, dlgForm.bonus_qty_display];
	for (i=0; i<amounts.length; i++) {
		if (amounts[i].value == "")
			amounts[i].value = formatAmountValue(0);
	}
	for (i=0; i<quantities.length; i++) {
		if (quantities[i].value == "")
			quantities[i] = "0";
	}
}

/*
 * Validate the sanity of individual fields in the dialog. Cross-field validation
 * is done part of itemValidate
 */
function dialogValidate() {

	dialogSetEmptyValues();

	var typeOfAction = prefExpItemProc;
	var noOfDays = parseInt(procExpireDays);

	if (!validateRequired(dlgForm.medicine_name, 'Enter Item name')) return false;
	if (!validateRequired(dlgForm.batch_no, 'Enter Batch No.')) return false;

    if (gDialogItem.expiry_date_val) {
    	if (!validateRequired(dlgForm.exp_dt_mon, 'Month of Expiry is required')) return false;
		if (!validateRequired(dlgForm.exp_dt_year, 'Year of Expiry is required')) return false;
    }

    if (!chkExpireDate(dlgForm.exp_dt_year,dlgForm.exp_dt_mon,typeOfAction,noOfDays)) {
    	return false;
    }
    if(!doAllowStatus) {
    	if (!validateRequired(dlgForm.cost_price_display, 'Enter Cost Price')) return false;
    	formatAmountObj(dlgForm.cost_price_display);
    }

    if(!doAllowStatus) {
    	if (gDialogItem.billable) {
    		//if (!validateRequired(dlgForm.grn_package_uom, 'MRP is required')) return false;
    		formatAmountObj(dlgForm.mrp_display);
        } else {
    		if (dlgForm.mrp_display.value == '')
    			dlgFrom.mrp_display.value = formatAmountValue(0);
        }
    }


	if (!isValidNumber(dlgForm.billed_qty_display, qtyDecimal)) return false;
	if (!isValidNumber(dlgForm.bonus_qty_display, qtyDecimal, 'Bonus Quantity')) return false;
	if(!doAllowStatus) {
		if (!isValidNumber(dlgForm.tax_rate, 'Y', 'Tax %')) return false;
		if (!isValidNumber(dlgForm.discount_per, 'Y', 'Discount Per')) return false;
		if (!isValidNumber(dlgForm.discount, 'Y', 'Discount')) return false;
		if (!isValidNumber(dlgForm.scheme_discount_per, 'Y', 'Scheme Discount Per')) return false;
		if (!isValidNumber(dlgForm.scheme_discount, 'Y', 'Scheme Discount')) return false;
	}

	if ( gRowUnderEdit != -1 && (document.getElementsByName("candelete"))[gRowUnderEdit].value == 'N') {
		if (!validateQty(dlgForm.item_batch_id.value) )
			return false;
	}


	formatAmountObj(dlgForm.billed_qty_display, true);
	formatAmountObj(dlgForm.bonus_qty_display, true);
	if(!doAllowStatus) {
		formatAmountObj(dlgForm.tax_rate, true);
		formatAmountObj(dlgForm.discount_per);
		formatAmountObj(dlgForm.discount);
		formatAmountObj(dlgForm.scheme_discount_per);
		formatAmountObj(dlgForm.scheme_discount);
	}

	return true;
}

/*
 * If empty, show 0 for some fields (not all, some are mandatory, don't touch them, eg,
 * if cost_price is empty, force user to enter 0 even if they want to)
 */
function dialogSetEmptyValues() {

	var amounts = [dlgForm.tax_rate, dlgForm.discount_per, dlgForm.discount, dlgForm.scheme_discount_per, dlgForm.scheme_discount];
	var quantities = [dlgForm.billed_qty_display, dlgForm.bonus_qty_display];
	for (i=0; i<amounts.length; i++) {
		if (amounts[i].value == "")
			amounts[i].value = formatAmountValue(0);
	}
	for (i=0; i<quantities.length; i++) {
		if (quantities[i].value == "")
			quantities[i] = "0";
	}
}

function dialogItemValidate() {

	var item = gDialogItem;
	if (item.billed_qty + item.bonus_qty == 0) {
        showMessage("js.stores.procurement.bothqty.bonusshouldnotbezero");
        dlgForm.billed_qty_display.focus();
        return false;
    }

	if(!doAllowStatus){
		if (item.billable && item.mrp == 0) {
			var msg ="";
			msg+= dlgForm.medicine_name.value;
			msg+=" ";
			msg+=getString("js.stores.procurement.isbillableitem");
			msg+= "\n";
			msg+=getString("js.stores.procurement.mrpshouldnotbezero");
			alert(msg);
			dlgForm.mrp_display.focus();
			return false;
		}
	}


    if (item.grnmed == 'Y') {
        if (item.stock_package_size != item.grn_package_size) {
            showMessage("js.stores.procurement.packagesize.changedinitialgrnentry");
            return false;
        }
    }

	if (editGRN) {
		if (item.billed_qty < item.grnqty) {
			var msg=getString("js.stores.procurement.quantity");
			msg+=" ";
			msg+= item.billed_qty_display;
			msg+=" ";
			msg+=getString("js.stores.procurement.cannotbeless.grnquantity");
			msg+=" ";
			msg+=item.grnqty_display;
			alert(msg);
			dlgForm.billed_qty_display.focus();
			return false;
		}

		if (item.bonus_qty < item.grnbqty) {
			var msg=getString("js.stores.procurement.bonusquantity");
			msg+=" ";
			msg+= item.bonus_qty_display;
			msg+=" ";
			msg+=getString("js.stores.procurement.cannotbeless.grnquantity");
			msg+=" ";
			msg+=item.grnbqty_display;
			alert(msg);
			dlgForm.bonus_qty_display.focus();
			return false;
		}
	}
    if(dlgForm.supplier_rate_validation.value == "true"){
    	if(!supplierRateValidation(item.cost_price)) return false;
    }else{
    	if (validateCostPrice == "Y" && !doAllowStatus) {
            if (!validateMaxCostPrice(item.medicine_id, item.cost_price, item.item_max_cost_price)) return false;
        }
    }


    if(!doAllowStatus) {
    	if (item.pomed == 'Y') {
    		if (item.cost_price > item.pomedrate) {
    			var msg=getString("js.stores.procurement.rate");
    			msg+=" ";
    			msg+=item.cost_price_display;
    			msg+=" ";
    			msg+=getString("js.stores.procurement.notbegreaterthanporate");
    			msg+=" ";
    			msg+= item.pomedrate_display;
    			alert(msg);
    			dlgForm.cost_price_display.focus();
    			return false;
    		}
    		if (item.discount_per < item.po_discount_per) {
    			var msg=getString("js.stores.procurement.discountpercentage");
    			msg+=" ";
    			msg+= item.discount_per;
    			msg+=" ";
    			msg+=getString("js.stores.procurement.notbelesserthanpovalue");
    			msg+=" ";
    			msg+=item.po_discount_per;
    			alert(msg);
    			dlgForm.cost_price_display.focus();
    			return false;
    		}

        }

	    if (item.mrp < item.cost_price && item.billable) {
	        var msg=getString("js.stores.procurement.mrp");
	        msg+=" ";
	        msg+=item.mrp_display;
	        msg+=" ";
	        msg+=getString("js.stores.procurement.begreaterthanrate");
	        msg+= " ";
	        msg+= item.cost_price_display;
	        alert(msg);
	        dlgForm.cost_price_display.focus();
	        return false;
	    }

	    if (item.med_total < 0) {
	        showMessage("js.stores.procurement.discountshouldbeless");
	        dlgForm.discount.focus();
	        return false;
	    }
    }
	for (var k = 0; k < gRowItems.length; k++) {
		if (gRowUnderEdit == k) continue;		// self
		var gridItem = gRowItems[k];
		if (gridItem.medicine_id == item.medicine_id && gridItem.batch_no == item.batch_no) {
			showMessage("js.stores.procurement.duplicateentry");
			return false;
        }
    }

    if (item.newbatch == 'N' && item.grnmed == 'N') {
		if (item.consignment_stock != isGrnConsignment()) {
			var msg=getString("js.stores.procurement.stocktype.differswithbatchinstock");
			msg+="\n";
			msg+=getString("js.stores.procurement.selectadifferentbatchnumber");
			alert(msg);
			return false;
		}
	}

    if(!doAllowStatus) {
    	if (parseFloat(dlgForm.cost_price_display.value) == 0)
    		alert(getString("js.stores.procurement.rateiszeroforitem") +" "+ dlgForm.medicine_name.value);
    }


    return true;
}

function validateMaxCostPrice(medicineId, cp, itemMaxCostPrice) {
	itemMaxCostPrice = parseFloat(itemMaxCostPrice);
	if (isNaN(itemMaxCostPrice)) {
		// no need to validate
		return true;
	}
	if (parseFloat(cp) > itemMaxCostPrice) {
		if (editMaxCP == 'A' || gRoleId == 1 || gRoleId == 2) {
			// allow but warn.
			if (!confirm("Cost price (pkg) " + cp + " is more than the max cost price "
						+ itemMaxCostPrice + ". Are you sure?")) {
				return false;
			}
		} else {
			alert(getString("js.stores.procurement.costprice.pkg")+" "+ cp +" "+getString("js.stores.procurement.notexceedthemaxcostprice")+" "+ itemMaxCostPrice);
			return false;
		}
	}
    return true;
}

function chkMon() {
    if (dlgForm.exp_dt_mon.value != '') {
        if (dlgForm.exp_dt_mon.value == 0 || dlgForm.exp_dt_mon.value > 12) {
            showMessage("js.stores.procurement.monthshouldbe1to12only");
            dlgForm.exp_dt_mon.value = '';
            dlgForm.exp_dt_mon.focus();
            return false;
        }
        if (dlgForm.exp_dt_mon.value.length == 1) {
            dlgForm.exp_dt_mon.value = '0' + dlgForm.exp_dt_mon.value;
        }
    }
}

function chkYear() {
    if (dlgForm.exp_dt_year.value != '') {
        if (dlgForm.exp_dt_year.value.length == 1) {
            dlgForm.exp_dt_year.value = '0' + dlgForm.exp_dt_year.value;
        }
    }
}

function makeingDec(objValue, obj) {
    if (objValue == '' || isNaN(objValue)) objValue = 0;
    obj.value = parseFloat(objValue).toFixed(decDigits);
}

function validatePOItems() {
	// check that each item in the grid is present in the PO list
    var numRows = getNumItems();
    for (var i = 0; i < numRows; i++) {
        rowObj = getItemRow(i);
		var medicineId = getElementByName(rowObj, "medicine_id").value;
		var item = findInList(poMedList, "medicine_id", medicineId);

        if (item == null) {
            var msg=getString("js.stores.procurement.item");
            msg+= getElementByName(rowObj, 'medicine_name').value;
            msg+=getString("js.stores.procurement.notexistinpo");
            msg+="\n";
            msg+=getString("js.stores.procurement.stockentrydisallowed");
            alert(msg);
            return false;
        }
    }

	// cross check the total quantity of each PO item: should not exceed the PO qty.
    for (k = 0; k < poMedList.length; k++) {
        var poItem = poMedList[k];
		var billedQty = 0;
		var grnQty = 0;
		// sum through all items since we may have multiple batches of same item.
        for (var i = 0; i < gRowItems.length; i++) {
			var item = gRowItems[i];
            rowObj = getItemRow(i);
            if (poItem.medicine_id == item.medicine_id) {
                billedQty = billedQty + item.billed_qty;
                if (item.pomed == 'Y' && item.grnmed == 'Y') {
					grnQty = grnQty + item.grnqty;
                }
            }
        }

		// since qty is inclusive of grnQty that is already received, add it back
		// Eg, when editing a GRN where PO qty is 10, grnQty is 5, and received is also 5,
		// we need to be able to receive up to 10, which is 10 - 5 + 5. Alert shows original
		// PO quantity, and all GRNs quantity.
        var maxqty = poItem.qty_req - poItem.qty_received + grnQty;

       if (billedQty > maxqty) {
          /*var msg=getString("js.stores.procurement.item");
            msg+=" ";
            msg+=poItem.medicine_name;
            msg+=" ";
            msg+=getString("js.stores.procurement.totalbilledquantity");
            msg+=round2(billedQty) ;
            msg+=(poItem.qty_received - grnQty);
            msg+=getString("js.stores.procurement.exceedspoquantity");
            msg+=round2(poItem.qty_req);
            msg+= ")";
            alert(msg);*/
            alert(getString("js.stores.procurement.item")+" "+ poItem.medicine_name +" "+getString("js.stores.procurement.totalbilledquantity")
					+ round2(billedQty + (poItem.qty_received - grnQty)) +
					getString("js.stores.procurement.exceedspoquantity")+ round2(poItem.qty_req) + ")");
            return false;
        }

		// allow excess bonus quantity.
    }
    return true;
}

function saveAndPrint() {
	grnForm._printAfterSave.value = 'Y';
	savestock();
	grnForm._printAfterSave.value = 'N';
}

function saveAndFinalize() {
	grnForm.status.value = 'F';
	savestock();
}

function savestock() {
	if (!mandatoryfieldsval()) {
		return false;
	}

	if (strictPO && !validatePOItems()) {
		return false;
	}

	var length = getNumItems();
	if (length < 1) {
		showMessage("js.stores.procurement.noitemsaddedry");
		return false;
	}

	var allChecked = false;

	for (var k = 0; k < gRowItems.length; k++) {
		var item = gRowItems[k];
		var medName = item.medicine_name;
		var rowObj = getItemRow(k);

		if (item.batch_no == '') {
			var msg=getString("js.stores.procurement.enterthebatchfor");
			msg+= medName;
			msg+= "\'";
			alert(msg);
			openEditDialogIndex(k);
			setTimeout("dlgForm.batch_no.focus();", 100);
			return false;
		}
		if (item.expiry_date_val) {
			if (item.exp_dt == '') {
				var msg=getString("js.stores.procurement.entertheexpirymonth.yearfor");
				msg+= medName;
				msg+= "\'";
				alert(msg);
				openEditDialogIndex(k);
				dlgForm.exp_dt_mon.focus();
				return false;
			}
		}

		if (item.grn_pkg_size == 0) {
			if (item.identification == 'S') {
				item.grn_pkg_size = 1;
			} else {
				showMessage("js.stores.procurement.packagesizecannotbezero");
				openEditDialogIndex(k);
				return false;
			}
		}

		// sanity: ensure package size in the hidden input is also proper
		var hPkgSize = getElementByName(rowObj, 'grn_pkg_size');
		if (hPkgSize.value == '' || hPkgSize.value == 0) {
			showMessage("js.stores.procurement.packagesizecannotbezero");
			openEditDialogIndex(k);
			return false;
		}

		if ((item.billed_qty + item.bonus_qty) == 0) {
			showMessage("js.stores.procurement.bothqtyandbonusshouldnotbezero");
			openEditDialogIndex(k);
			return false;
		}

		if (item.cost_price_paise > item.mrp_paise && item.billable && !doAllowStatus) {
            var msg=getString("js.stores.procurement.mrp");
            msg+=" ";
            msg+= item.mrp_display;
            msg+=" ";
            msg+=getString("js.stores.procurement.begreaterthanrate");
            msg+=" ";
            msg+= item.cost_price_display;
            alert(msg);
            openEditDialogIndex(k);
            dlgForm.cost_price_display.focus();
            return false;
        }
	}

	if (grnForm.status.value == 'O') {
		var ok = confirm("GRN is to be saved in open status. Are you sure?");
		if (!ok)
			return false;
	}

	var grandTotalText = document.getElementById("lblGrandTotal").textContent;
	if (grandTotalText == "") {
		document.getElementById("lblGrandTotal").textContent = "0";
	}

	var grandTotalPaise = getPaise(grandTotalText);
	if (grandTotalPaise < 0 && !doAllowStatus) {
		showMessage("js.stores.procurement.invoicetotalisnegative");
		return false;
	}

	var debitAmtText = grnForm.debit_amt.value;
	var debitTotalPaise = getPaise(debitAmtText);

	if(!doAllowStatus) {
		if (!confirm("Invoice Total Amount: " + grandTotalText + "\n\t   Debit Amount: " + debitAmtText +
					"\n Net Payble Amount:  " + formatAmountPaise(grandTotalPaise - debitTotalPaise) )) {
			return false;
		}
	}
	// enable so that the values are sent to backend
	disableInvoiceFields(false);
	restrictPOFlow(false);
	grnForm.countSeq.value = identifierSeq;
	grnForm.supplierName.disabled = false;

	grnForm.saveStk.disabled = true;
	grnForm.submit();
}

function reopen() {
	grnForm.saveAction.value = "reopen";
	grnForm.saveStk.disabled = true;
	grnForm.action.value = "stockentry.do?_method=insertInvoiceStock";
	grnForm.submit();
}

function closeGRN() {
	grnForm.saveAction.value = "close";
	grnForm.closeStk.disabled = true;
	grnForm.submit();
}

function grnPrint() {
	var printerType = grnForm.printType.value;
	var grnPrintTemplate = getFieldValue('grn_print_template');
	window.open(cpath + "/stores/stockentry.do?_method=generateGRNprint&grNo=" + grnNo +
			"&printerType=" + printerType + "&grnPrintTemplate=" + grnPrintTemplate);
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
	//YAHOO.util.Connect.asyncRequest('GET', url, {success: onGetItemMasterTimestamp});
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
		initItemAutoComplete();
	}
}

function displayItemLvlCst(){
	var cstRow = getThisRow(dlgForm.cst_rate);
	cstRow.style.display = ( (grnForm.tax_name.value == 'CST' || grnForm.tax_name.value == 'iGST') && applySupplierTaxRules != 'true' ? 'table-row' : 'none' );
}

function setCST(){
		gDialogItem.cst_rate = grnForm.main_cst_rate.value;
}

var supplierRateObj = {};
function getSupplierRateValue() {

	 var medicineId = dlgForm.medicine_id.value;
	 if(supplierRateObj[medicineId] != null){
		 if(supplierRateObj[medicineId].supplier_id == directstockform.supplier_id.value && supplierRateObj[medicineId].storeId == directstockform.store_id.value){
			 dlgForm.supplier_rate_validation.value = supplierRateObj[medicineId].supplier_rate_validation;
			 dlgForm.max_cost_price.value = supplierRateObj[medicineId].supplier_rate;
		 }else{
			 getSupplierRateAjaxCall();
		 }

	 }else{
		 getSupplierRateAjaxCall();
	 }
}

function applyStrictPOControls(value) {
	dlgForm.mrp_display.disabled = value;
	dlgForm.cost_price_display.disabled = value;
	dlgForm.tax_type.disabled = value;
	
	//dlgForm.billed_qty_display.disabled = value;
	//dlgForm.bonus_qty_display.disabled = value;
	
	dlgForm.discount_per.disabled = value;
	dlgForm.discount.disabled = value;
	
	dlgForm.scheme_discount_per.disabled = value;
	dlgForm.scheme_discount.disabled = value;
	
	var length = document.getElementsByName("subgroups").length;
	for (var k = 0; k <length; k++) {
		document.getElementsByName("subgroups")[k].disabled =value;
	}
	
}

function getSupplierRateAjaxCall(){
	 var medicineId = dlgForm.medicine_id.value;
	 var ajaxReqObject = newXMLHttpRequest();
	 var getSupplierRateDetails = '';
	 var url = cpath+'/pages/master/SupplierContractMaster/SupplierContractItemRates.do?_method=getSupplierItemRateDetails&medicineId='+medicineId+'&supplierId='+directstockform.supplier_id.value+'&storeId='+directstockform.store_id.value;

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
		dlgForm.max_cost_price.value = getSupplierRateDetails.supplier_rate;
}
function supplierRateValidation(cp){

	if(dlgForm.max_cost_price.value != "" && parseFloat(cp) > parseFloat(dlgForm.max_cost_price.value)){
		alert("The Rate defined as per Supplier Rate Contracts is " + parseFloat(dlgForm.max_cost_price.value).toFixed(decDigits));
		return false;
	}
	return true;
}
function initForm8hVariables(){
	taxobj = grnForm.tax_name;;
	form8HObj = grnForm.form_8h;
	cformObj = grnForm.c_form;
	if (form8HObj.checked){
		form8Hcal = true;
		form8HApplicable = true;
	}
}

function getTaxDetails(dlgForm) {
	clearTaxFields();
	var url = cpath + "/stocks/grntaxdetails.json";
	ajaxForm(dlgForm, url, false, setTaxDetails);
}

function getTaxDetailsOnChange(dlgForm) {
	clearTaxFields();
	var url = cpath + "/stocks/changetaxdetails.json";
	ajaxForm(dlgForm, url, false, setTaxDetails);
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
			dlgForm.tax_rate.value = parseFloat(vatRate).toFixed(decDigits);
			dlgForm.tax.value = parseFloat(vatAmt).toFixed(decDigits);
			document.getElementById("lblvat_rate").innerHTML = parseFloat(vatRate).toFixed(decDigits);
			document.getElementById("lblvat_amt").innerHTML = parseFloat(vatAmt).toFixed(decDigits);

			if(response.adj_price != undefined && response.adj_price != null && response.adj_price != 'null' && response.adj_price != '')
				adj_mrp = parseFloat(response.adj_price).toFixed(decDigits);
			else
				adj_mrp = 0;
			dlgForm.adj_mrp_display.value = parseFloat(adj_mrp).toFixed(decDigits);
			dlgForm.adj_mrp.value = parseFloat(adj_mrp).toFixed(decDigits);

			gDialogItem.adj_mrp = parseFloat(adj_mrp).toFixed(decDigits);
			gDialogItem.adj_mrp_display = parseFloat(adj_mrp).toFixed(decDigits);
			gDialogItem.adj_mrp_paise = getPaise(adj_mrp);

			gDialogItem.tax_rate = parseFloat(vatRate).toFixed(decDigits);
			gDialogItem.tax = parseFloat(vatAmt).toFixed(decDigits);
			gDialogItem.tax_paise = getPaise(vatAmt);
			gDialogItem.tax_type = dlgForm.tax_type.value;
		}
		calcDialogValues();
	}
}

function onChangeGrnTemplate(printTemplate){
	setFieldValue('grnPrintTemplateHid', printTemplate);
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
}

function toggleAddButton(value) {
	if(document.getElementById("plusItem") !=null) {
		if(value) {
			document.getElementById("plusItem").style.display='none';
		} else {
			document.getElementById("plusItem").style.display='block';
		}
	}
}

function validatePoCostPrice() {
	var valid = true;
	if (grnForm.po_no.value != '' && dlgForm.pomed.value != 'N') {
			if (Number(dlgForm.cost_price_display.value)>Number(dlgForm.cost_price.value)) {
				var msg=getString("js.stores.procurement.rate");
    			msg+=" ";
    			msg+=dlgForm.cost_price_display.value;
    			msg+=" ";
    			msg+=getString("js.stores.procurement.notbegreaterthanporate");
    			msg+=" ";
    			msg+= dlgForm.cost_price.value;
    			alert(msg);
    			dlgForm.cost_price_display.focus();
				dlgForm.cost_price_display.value = dlgForm.cost_price.value;
				valid = false;
			}
		}
		return valid;
	
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
