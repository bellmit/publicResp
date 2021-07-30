/**
 *	@author irshad
 */
var oSupplierAutoComp;
var supplierRateContractForm;
var gColIndexes = [];
var gRowUnderEdit = -1;
var gRowItems = [];
var gDialogItem = null;
var dlgForm = null;
var medAutoComp;

function init() {
	supplierRateContractForm = document.supplierRateContractForm;
	gItemNames = jItemNames;
	initSupplierAutoComplete();
	if (supplier_rate_contract_id != '')	{
		oSupplierAutoComp._bItemSelected = true;
		setSupplierAttributes();
	}
	 var cl = 0;
	 gColIndexes.medicine_name = cl++;
	 gColIndexes.issue_base_unit = cl++;
	 gColIndexes.mrp = cl++;
	 gColIndexes.supplier_rate = cl++;
	 gColIndexes.discount = cl++;
	 gColIndexes.margin = cl++;
	 gColIndexes.margin_type_label = cl++;
	 EDIT_COL = cl++;
	 TRASH_COL = cl++;

	 dlgForm = document.detailForm;
	 initAddEditDialog();


	 initItemAutoComplete();
}

function initSupplierAutoComplete() {
	var supplierNames = [];
    var j = 0;

	var dataSource = new YAHOO.widget.DS_JSArray(jAllSuppliers);

	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "SUPPLIER_NAME_WITH_CITY"}, {key : "SUPPLIER_CODE"} ]
	};

	oSupplierAutoComp = new YAHOO.widget.AutoComplete(supplierRateContractForm.supplier_name, 'supplier_dropdown', dataSource);
	oSupplierAutoComp.maxResultsDisplayed = 20;
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
	if(suppId=="-1"){
		suppId="";
	}
	supplierRateContractForm.supplier_id.value = suppId;
	setSupplierAttributes();
}

function setSupplierAttributes() {
	var supplierList= [];
    var j = 0;
	var suppId = supplierRateContractForm.supplier_id.value;
	for (var i = 0; i < jAllSuppliers.length; i++) {
    	supplierList[j++] = jAllSuppliers[i].SUPPLIER_NAME;
    	if (suppId == jAllSuppliers[i].SUPPLIER_CODE) {

    		var supplierAddress = jAllSuppliers[i].SUPPLIER_ADDRESS;
    		if(jAllSuppliers[i].SUPPLIER_PHONE1 != null && jAllSuppliers[i].SUPPLIER_PHONE1 != '')
    			supplierAddress = supplierAddress + " Ph: " + jAllSuppliers[i].SUPPLIER_PHONE1;
    		else if(jAllSuppliers[i].SUPPLIER_PHONE2 != null && jAllSuppliers[i].SUPPLIER_PHONE2 != '')
    			supplierAddress = supplierAddress + " Ph: " +jAllSuppliers[i].SUPPLIER_PHONE2;
    	   if(jAllSuppliers[i].SUPPLIER_FAX != null && jAllSuppliers[i].SUPPLIER_FAX != '')
    	   		supplierAddress = supplierAddress + " Fax: " + jAllSuppliers[i].SUPPLIER_FAX;

    	    setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 30, supplierAddress);

    	    supplierRateContractForm.supplier_name.value = jAllSuppliers[i].SUPPLIER_NAME;
    	}
	}
}

function saveSupplierRateContract() {
	var supplierRateContract = document.getElementById("supplier_contract_name").value;
	if (supplierRateContract == "" || supplierRateContract == null) {
		showMessage("js.master.supplierratecontract.suppliercontractnamerequired");
   		document.getElementById("supplier_contract_name").focus();
   		return false;
	}
	var supplierName = document.getElementById("supplier_name").value;
	if (supplierName == "" || supplierName == null) {
		showMessage("js.master.supplierratecontract.supplierrequired");
   		document.getElementById("supplier_name").focus();
   		return false;
	}
	var startDate = document.getElementById("validity_start_date").value;
	if (startDate == "" || startDate == null) {
		showMessage("js.master.supplierratecontract.startdaterequired");
   		document.getElementById("validity_start_date").focus();
   		return false;
	}
	var endDate = document.getElementById("validity_end_date").value;
	if (endDate == "" || endDate == null) {
		showMessage("js.master.supplierratecontract.enddaterequired");
   		document.getElementById("validity_end_date").focus();
   		return false;
	}
   var valStartDate = getDatePart(parseDateStr(startDate));
   var valEndDate = getDatePart(parseDateStr(endDate));
   if (daysDiff(valStartDate,valEndDate) < 0) {
   		showMessage("js.master.supplierratecontract.enddatemorethanstartdate");
   		document.getElementById("validity_end_date").focus();
   		return false;
   }
   if((opMode == 'edit' && supplierRateContract != document.getElementById("supplier_contract_name_hid").value) || (opMode == 'add')) {
	   for(var i=0; i < supplierContractNameList.length; i++) {
		   if(supplierContractNameList[i].supplier_rate_contract_name == supplierRateContract) {
			   showMessage("js.master.supplierratecontract.duplicatesuppliercontractname");
			   document.getElementById("supplier_contract_name").focus();
			   return false;
		   }
	   }
   }

   if(mode == 'additem' && gRowItems.length == 0 ){
	   alert("Please add atleast one item");
	   openAddDialog();
	   return false;
   }

   if ( document.getElementById("uploadFile") ){
	    var sFileName = document.getElementById("uploadFile").value;
	    var _validFileExtensions = ".csv";
	    if (sFileName.length > 0) {
	        var blnValid = false;
	        var sCurExtension = _validFileExtensions;
	        var sEnteredExtension = sFileName.substr(sFileName.length - sCurExtension.length, sCurExtension.length).toLowerCase();
	        if (sEnteredExtension == sCurExtension.toLowerCase()) {
	            blnValid = true;
	        }
	        var filename = sFileName;
	        var lastIndex = filename.lastIndexOf("\\");
	        if (lastIndex >= 0) {
	            filename = filename.substring(lastIndex + 1);
	        }
	        if (!blnValid) {
	            alert("Sorry, " + filename + " is invalid, allowed extensions are: " + _validFileExtensions);
	            return false;
	        }
	    }
   }
   supplierRateContractForm.submit();

}

function openAddDialog() {

	gRowUnderEdit = -1;
	button = document.getElementById("plusItem");
	document.getElementById("addEditDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);


	gDialogItem = newItem();
	itemToDialog(gDialogItem);

	document.getElementById("prevDialog").disabled = true;
	document.getElementById("nextDialog").disabled = true;

	detaildialog.show();
	dlgForm.medicine_name.focus();
}

/*
 * Create a new item, contains a superset of all required fields
 */
function newItem() {
	var item = {
		medicine_name: '', medicine_id: '', mrp: '', supplier_rate: '',
		discount: '', margin: '', margin_type: '',issue_base_unit:''
	};

	return item;
}

/*
 * itemToDialog: this is more than just a transform. It also initializes the dialog
 * fields like the Package UOM dropdown and enables/disables fields based on the
 * item that is being shown.
 */
function itemToDialog(item) {
	// call common object to form, with dlgForm as the target
	objectToForm(item, dlgForm);
	setNodeText(document.getElementById('itemPkgSize').parentNode, item.issue_base_unit, 10, "");
}

function onDialogSave() {

	// copy dialog to dialogItem
	dialogToItem(gDialogItem);
	if (!dialogValidate())
		return false;
	dialogSave();
	return true;
}

function dialogValidate(){

	var item = gDialogItem;
	if ( item.medicine_name == ''){
			alert("Select an Item");
			dlgForm.medicine_name.focus();
			return false;
	}

	if((dlgForm.margin.value || dlgForm.margin.value === 0)){
		if(!dlgForm.margin_type.value){
			showMessage("js.stores.suppliercontracts.itemrate.select.margin.type");
			dlgForm.margin_type.focus();
			return false;
		}
	} else if(dlgForm.supplier_rate.value == '' || parseFloat(dlgForm.supplier_rate.value) <= parseFloat(0).toFixed(2)){
		 showMessage("js.stores.suppliercontracts.itemrate.enternumber");
		 dlgForm.supplier_rate.focus();
		 return false;
	}

	if(dlgForm.margin_type.value == 'P' && parseFloat(dlgForm.margin.value) > 100){
		showMessage("js.stores.suppliercontracts.margin.greater.than.hundred");
		dlgForm.margin.focus();
		return false;
	}

	 var discountValue = dlgForm.discount.value;
	 if(discountValue > 100){
		 showMessage("js.stores.suppliercontracts.itemrate.discountnot.greaterthan100percent");
		 dlgForm.discount.focus();
		 return false;
	 }

	if ( getExistingItemStatus() == 'exist' ){
		showMessage("js.stores.procurement.duplicateentry");
		return false;
	}

	for (var k = 0; k < gRowItems.length; k++) {
		if (gRowUnderEdit == k) continue;		// self
		var gridItem = gRowItems[k];
		if (gridItem.medicine_id == item.medicine_id) {
			showMessage("js.stores.procurement.duplicateentry");
			return false;
        }
    }

	return true;
}
function dialogToItem(item) {
	formToObject(dlgForm, item);
	item.margin_type_label = item.margin_type == 'P'? 'Percent' : item.margin_type == 'A' ? 'Amount' : '';
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


/*
 * Add a validated dialog item to the grid.
 */
function addDialogItemToGrid() {
	var rowIndex = addRow();
	saveDialogItemToGrid(rowIndex);
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

function handleDetailDialogCancel() {
    detaildialog.cancel();
    removeRowClasses();
    supplierRateContractForm.saveStk.focus();
}

function removeRowClasses() {
    var totalNoOfRows = getNumItems();
    for (var i = 0; i < totalNoOfRows; i++) {
        var row = getItemRow(i);
        row.className = '';
    }
}

function getNumItems() {
    // header, hidden template row: totally 3 extra
    return document.getElementById("medtabel").rows.length - 2;
}
function getItemRow(i) {
    i = parseInt(i);
    var table = document.getElementById("medtabel");
    return table.rows[i + getFirstItemRow()];
}


function initItemAutoComplete() {
	var dataSource = new YAHOO.widget.DS_JSArray(gItemNames);

	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "medicine_name"}, {key : "medicine_id"}, {key : "issue_base_unit"} ]
	};

	medAutoComp = new YAHOO.widget.AutoComplete(dlgForm.medicine_name, 'item_dropdown', dataSource);
	medAutoComp.maxResultsDisplayed = 20;
	medAutoComp.allowBrowserAutocomplete = false;
	medAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	medAutoComp.typeAhead = false;
	medAutoComp.useShadow = false;
	medAutoComp.minQueryLength = 0;
	medAutoComp.forceSelection = true;
	medAutoComp.filterResults = Insta.queryMatchWordStartsWith;

	medAutoComp.itemSelectEvent.subscribe(onSelectItem);
}



/*
 * Called on selection of an item in the item auto comp
 */
function onSelectItem(type, args) {
	var mId = args[2][1];
	dlgForm.medicine_id.value = mId;
	dlgForm.medicine_name.value = args[2][0];
	dlgForm.issue_base_unit.value = args[2][2];
    setNodeText(document.getElementById('itemPkgSize').parentNode, args[2][2], 10, "");

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
    supplierRateContractForm.saveStk.focus();
}

function removeRowClasses() {
    var totalNoOfRows = getNumItems();
    for (var i = 0; i < totalNoOfRows; i++) {
        var row = getItemRow(i);
        row.className = '';
    }
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


function onDeleteRow(obj) {
    var row = getThisRow(obj);
	var rowIndex = getRowItemIndex(row);

	// delete from the grid
    row.parentNode.removeChild(row);

	// delete the row in gRowItems
	for (var i=rowIndex+1; i< gRowItems.length; i++) {
		// move one backward.
		gRowItems[i-1] = gRowItems[i];
	}
	gRowItems.length = gRowItems.length - 1;
    return false;
}

function saveDialogItemToGrid(rowIndex) {
	// item is already validated and saved
	gRowItems[rowIndex] = gDialogItem;
	gDialogItem = null;				// ensure we don't refer to this again.
	rowItemToRow(rowIndex);
	getItemRow(rowIndex).className = '';
}
function rowItemToRow(rowIndex) {
	// copy to hidden values in row: call common object to form, with indexed grnForm as the target
	var row = getItemRow(rowIndex);
	objectToHidden(gRowItems[rowIndex], row);
	// copy to labels in table: call common object to label
	objectToRowLabels(gRowItems[rowIndex], row, gColIndexes);
}

function onEditRow(img) {
    var row = findAncestor(img, "TR");
	openEditDialog(row);
}

function openEditDialog(row) {
    row.className = 'editing';
	gRowUnderEdit = getRowItemIndex(row);

    rowItemToDialog(gRowUnderEdit);

	var button = row.cells[EDIT_COL];
	document.getElementById("addEditDialog").style.display = "block";
	detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);
	medAutoComp._bItemSelected = true;

	document.getElementById("prevDialog").disabled = false;
	document.getElementById("nextDialog").disabled = false;

	detaildialog.show();
    setTimeout("dlgForm.medicine_name.focus()", 100);
}

function rowItemToDialog(rowIndex) {
	// we need a shallow copy so that a cancel will not affect the original
	gDialogItem = {};
	shallowCopy(gRowItems[rowIndex], gDialogItem);
	itemToDialog(gDialogItem, dlgForm);
}

function onNextPrev(val) {

	// copy dialog to dialogItem
	dialogToItem(gDialogItem);

	if (!dialogValidate())
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
function onKeyPressAddQty(e) {
    e = (e) ? e : event;
    var charCode = (e.charCode) ? e.charCode : ((e.which) ? e.which : e.keyCode);
    if (charCode == 13 || charCode == 3) {
		onDialogSave();
        return false;
    }
}
function getExistingItemStatus(){
	var medicineId = dlgForm.medicine_id.value;
	 var ajaxReqObject = newXMLHttpRequest();
	 var getSupplierRateDetails = '';
	 var url = cpath+'/pages/master/SupplierRateContractMaster.do?_method=getExistingItemStatus&medicineId='+medicineId+'&contractId='+supplier_rate_contract_id;

		ajaxReqObject.open("GET",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("itemStatus = " + ajaxReqObject.responseText)
			}
		}

		return itemStatus;
}

function setDiscountValue(){
	 var discountValue = dlgForm.discount.value;
	 if(isNaN(discountValue) || discountValue == '' ){
		 //showMessage("js.stores.suppliercontracts.itemrate.empty.enternumber");
		 document.getElementById("discount").value='';
	 }else{
		dlgForm.discount.value =  round(parseFloat(dlgForm.discount.value), DecimalDigits);
	 }
}

function setMrpValue(){
	var mrpValue = dlgForm.mrp.value.trim();
	if(isNaN(mrpValue) || mrpValue == '' ){
	 //showMessage("js.stores.suppliercontracts.itemrate.empty.enternumber");
		dlgForm.mrp.value='';
	}else{
		dlgForm.mrp.value =  round(parseFloat(dlgForm.mrp.value), DecimalDigits);
	}
}

function setRateValue(){
	var rateValue = dlgForm.supplier_rate.value.trim();
	if(isNaN(rateValue) || rateValue == '' ){
	 //showMessage("js.stores.suppliercontracts.itemrate.empty.enternumber");
		dlgForm.supplier_rate.value='';
	}else{
		dlgForm.supplier_rate.value =  round(parseFloat(dlgForm.supplier_rate.value), DecimalDigits);
	}
}
