/**
 * @author irshad
 */

var oItemAutoComp;
var oSupplierAutoComp;

var woForm;
var dlgForm;
var gColIndexes = [];
var gRowUnderEdit = -1;
var gRowItems = [];
var oAutoItem;

	function init() {
		
		if (woNo != '')	{
			ajaxForPrintUrls();
		}
		woForm = document.woForm;
		dlgForm = document.dlgForm;
		
		// column indexes
		var cl=0;

		gColIndexes.wo_item_name = cl++;
		gColIndexes.qty_display = cl++;
		gColIndexes.rate_display = cl++;
		if (prefVAT == 'Y') {
			gColIndexes.vat_rate = cl++;
			gColIndexes.item_tax = cl++;
		}
		gColIndexes.discount = cl++;
		gColIndexes.description = cl++;
		gColIndexes.amount = cl++;
		EDIT_COL = cl++;
		initSupplierAutoComplete();
		initDialog();
		autoItem();
		
		 if (woNo != '')	{
			oSupplierAutoComp._bItemSelected = true;
			setSupplierAttributes();
			allRowsHiddenToLabels();
		
		}
		 woForm.supplier_name.focus();
	}

	function initDialog() {
		detaildialog = new YAHOO.widget.Dialog("detaildialog",
		{
			width:"600px",
			context : ["plusItem", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		} );

		var escKeyListener = new YAHOO.util.KeyListener("detaildialog", { keys:27 }, onDialogCancel);
		detaildialog.cfg.queueProperty("keylisteners", escKeyListener);
		detaildialog.render();
	}
	
	function onDialogCancel() {
		detaildialog.cancel();
		if (gRowUnderEdit != -1) {
			var row = getItemRow(gRowUnderEdit);
			YAHOO.util.Dom.removeClass(row, 'editing');
		}
		/*if (!onlyView)
			woForm.btnSavePo.focus();*/
	}
	
	function initSupplierAutoComplete() {
		var supplierNames = [];
	    var j = 0;
		if(centerId == 0) {
			var dataSource = new YAHOO.widget.DS_JSArray(jAllSuppliers);
		} else {    			
	        var dataSource = new YAHOO.widget.DS_JSArray(jCenterSuppliers);
		}
		dataSource.responseSchema = {
			resultsList : "result",
			fields : [  {key : "SUPPLIER_NAME_WITH_CITY"}, {key : "SUPPLIER_CODE"} ]
		};
	
		oSupplierAutoComp = new YAHOO.widget.AutoComplete(woForm.supplier_name, 'supplier_dropdown', dataSource);
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

	function autoItem(){
		YAHOO.example.ACJSArray = new function(){
			datasource = new YAHOO.widget.DS_JSArray(itemList);
			datasource.responseSchema = {
					resultsList : "result",
					fields : [  {key : "WO_ITEM_NAME"} ]
				};
			oAutoItem = new YAHOO.widget.AutoComplete("wo_item_name","item_dropdown",datasource);
			oAutoItem.minQueryLength = 0;
			oAutoItem.typeAhead = false;
			oAutoItem.prehighlightClassname = "yui-ac-prehighlight";
			oAutoItem.autoHighlight = true;
			oAutoItem.useShadow = false;
			oAutoItem.forceSelection = false;
			oAutoItem.maxResultsDisplayed = 10;
			oAutoItem.allowBroserAutocomplete = false;
			oAutoItem.textboxChangeEvent.subscribe(function(){
				var sInputValue = YAHOO.util.Dom.get("wo_item_name").value;
				if (sInputValue.length === 0){
					var oSelf = this;
					setTimeout(oSelf.sendQuery(sInputValue),0);
				}
			});
		}
	}
	
	function onSelectSupplier(type, args) {
		var suppId = args[2][1];
		woForm.supplier_id.value = suppId;
		setSupplierAttributes();
	}

	function setSupplierAttributes() {
		var supplierList= [];
	    var j = 0;
		var suppId = woForm.supplier_id.value;
		if (centerId == 0) {
			for (var i = 0; i < jAllSuppliers.length; i++) {
	            if (jAllSuppliers[i].STATUS == 'A')
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
	
	            		if (woNo == '') {
	            			woForm.credit_period.value = jAllSuppliers[i].CREDIT_PERIOD;
	            		}
	            		woForm.supplier_name.value = jAllSuppliers[i].SUPPLIER_NAME;
	            	}
			}
	       
		}
		
		 else {
			for (var i = 0; i < jCenterSuppliers.length; i++) {
	            if (jCenterSuppliers[i].STATUS == 'A' ) {
	            	supplierList[j++] = jCenterSuppliers[i].SUPPLIER_NAME;
	            	if (suppId == jCenterSuppliers[i].SUPPLIER_CODE) {
	            		var supplierAddress = jCenterSuppliers[i].SUPPLIER_ADDRESS;
	            		if(jCenterSuppliers[i].SUPPLIER_PHONE1 != null && jCenterSuppliers[i].SUPPLIER_PHONE1 != '')
	            			supplierAddress = supplierAddress + " Ph: " + jCenterSuppliers[i].SUPPLIER_PHONE1;
	            		else if(jCenterSuppliers[i].SUPPLIER_PHONE2 != null && jCenterSuppliers[i].SUPPLIER_PHONE2 != '')
	            			supplierAddress = supplierAddress + " Ph: " +jCenterSuppliers[i].SUPPLIER_PHONE2;
	            	   if(jCenterSuppliers[i].SUPPLIER_FAX != null && jCenterSuppliers[i].SUPPLIER_FAX != '')
	            	   		supplierAddress = supplierAddress + " Fax: " + jCenterSuppliers[i].SUPPLIER_FAX;
	
	            	    setNodeText(document.getElementById('suppAddId').parentNode, supplierAddress, 30, supplierAddress);
	
	            		woForm.supplier_name.value = jCenterSuppliers[i].SUPPLIER_NAME;
	            		
	            	}
	            }
	        }
		}
	}

	// clear the dialog values
	function resetDetails(){
		dlgForm.wo_item_name.value = '';
	    dlgForm.qty_display.value = 0;
		
		dlgForm.rate_display.value = 0;
		if (prefVAT == 'Y') {
			dlgForm.vat_rate.value = 0;
			dlgForm.item_tax.value = 0;
		}
		if ( dlgForm.item_status ) {
			setSelectedIndex(dlgForm.item_status, 0);
		}
	    dlgForm.discount_per.value = 0;
	    dlgForm.discount.value = 0;
	    dlgForm.description.value = '';
	    
	}
	
	function openAddDialog() {
		// supplier and store are required to get the item details.
		if (woForm.supplier_name.value == '' || woForm.supplier_id.value == '') {
			woForm.supplier_name.focus();
			showMessage("js.resourcemanagement.workorder.selectsupplier");
			return false;
		}
	
		resetDetails();
		gRowUnderEdit = -1;
		document.getElementById("prevDialog").disabled = true;
		document.getElementById("nextDialog").disabled = true;
		setTimeout("dlgForm.wo_item_name.focus()", 100);
		button = document.getElementById("plusItem");
		detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);
		//if (!onlyView)
		detaildialog.show();
	}
	
	function dlgValidate() {
		if (trimAll(dlgForm.wo_item_name.value) == '') {
			dlgForm.wo_item_name.value = '';
			showMessage("js.resourcemanagement.workorder.itemnameisrequired");
	        dlgForm.wo_item_name.focus();
	        return false;
	    }

		if (!validateRequired(dlgForm.qty_display, getString("js.resourcemanagement.workorder.quantityisrequired"))) return false;

	    if (!isValidNumber(dlgForm.qty_display, qtyDecimal, 'Quantity')) return false;

	    if (dlgForm.qty_display.value == 0) {
	    	showMessage("js.resourcemanagement.workorder.qtynotbezero");
	        dlgForm.qty_display.focus();
	        return false;
	    }

		if (dlgForm.rate_display.value == 0 || dlgForm.rate_display.value == '' ) {
			showMessage("js.resourcemanagement.workorder.rateisrequired");
	        dlgForm.rate_display.focus();
	        return false;
	    }

	    var rate = getPaise(dlgForm.rate_display.value);
		
		var discountPer = getAmount(dlgForm.discount_per.value);
		if (discountPer > 100) {
			showMessage("js.resourcemanagement.workorder.discountshouldbelessthan100");
			dlgForm.discount_per.focus();
			return false;
		}

	    var itemListTable = document.getElementById("itemtabel");
		var numItems = getNumItems();

		for (var k=0; k < numItems; k++) {
			if (gRowUnderEdit == k)
				continue;
			rowObj = getItemRow(k);
			if (getElementByName(rowObj,'_deleted').value == 'true')
				continue;

			if (getElementByName(rowObj,'wo_item_name').value == dlgForm.wo_item_name.value) {
				showMessage("js.resourcemanagement.workorder.duplicateentry");
				dlgForm.wo_item_name.value= '';
				resetDetails();
				return false;
			}
	    }

	    return true;
	}
	
	/*
	 * Called when the dialog elements are changed. We need to re-calculate
	 * the discount amount etc. Note that either percentage or amt can
	 * be changed
	 */
	function calcDlgValues () {
		var item = {};

		item.rate_display = getElementPaise(dlgForm.rate_display);
		item.qty_display = getElementAmount(dlgForm.qty_display);
		if (prefVAT == 'Y') {
			item.vat_rate = getElementAmount(dlgForm.vat_rate);
		}
		item.discount_per = getElementAmount(dlgForm.discount_per);
		calcItemValues(item);
		setElementAmount(dlgForm.discount, item.discount);
		if (prefVAT == 'Y') {
			setElementAmount(dlgForm.item_tax, item.tax_total);
		}

	}
	
	/*
	 * Add a new row, copy the dialog values to the grid in the new row.
	 */
	function addDialogToGrid() {
		var id = addRow();
		var row = getItemRow(id);
		YAHOO.util.Dom.addClass(row, 'added');
		dialogToGrid(getItemRow(id));
		resetTotals();
	}
	
	function addRow() {
		var id = getNumItems();
		var table = document.getElementById("itemtabel");
		var templateRow = table.rows[getTemplateRow()];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
		row.id="itmRow"+id;
		return id;
	}
	
	function dlgSave() {
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
		    detaildialog.cancel();	// save and stay
	    }
	}
	
	function updateDialogToGrid () {
		var row = getItemRow(gRowUnderEdit);

		dialogToGrid(row);
		YAHOO.util.Dom.addClass(row, 'edited');
		YAHOO.util.Dom.removeClass(row, 'editing');
		resetTotals();
	}
	
	function onDialogSave () {
		if (dlgValidate ()) {
			dlgSave();
		}
	}
	
	function deleteItem(imgObj) {
		var rowObj = getThisRow(imgObj);
		var newItem = getElementByName(rowObj, "wo_item_id").value;
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
	
	/*
	 * Update some calculated row values: especially used when loading up
	 * the rows not from dialog, as in loading it from a list of stock reorder items.
	 */
	function calcRowValues(row, valueType) {
		var item = {};
		
		if (valueType == 'stored') {
			//  row contains stored values. Display values to be calculated, eg, when loading during edit PO
			item.rate_display = getRowPaise(row, 'rate');
			item.qty_display = getRowAmount(row, 'qty');
			if (prefVAT == 'Y') {
				item.vat_rate = getRowAmount(row, 'vat_rate');
			}
			//item.item_tax = getRowAmount(row, 'item_tax');
			calcItemStoredValues(item);

			setRowPaise(row, 'rate_display', item.rate);
			setRowPaise(row, 'rate', item.rate);
			setRowQty(row, 'qty_display', item.qty);
			item.discount_amount = getRowAmount(row, 'discount');
			item.discount_per = item.discount_amount / (item.rate_display * item.qty_display) * 10000;
			
			calcItemValues(item);
			
			setRowQty(row, 'discount_per', item.discount_per);
			
			if (prefVAT == 'Y') {
				setRowPaise(row, 'item_tax', item.tax_total*100);
			}
		
		} else {
			// vice-versa: display values exist, eg, updating from dialog to row
			item.rate_display = getRowPaise(row, 'rate_display');
			item.qty_display = getRowAmount(row, 'qty_display');
			if (prefVAT == 'Y') {
				item.vat_rate = getRowAmount(row, 'vat_rate');
				item.item_tax = getRowAmount(row, 'item_tax');
			}
			calcItemStoredValues(item);

			setRowPaise(row, 'rate_display', item.rate);
			setRowPaise(row, 'rate', item.rate);
			setRowQty(row, 'qty', item.qty);
			item.discount_per = getRowAmount(row, 'discount_per');
			calcItemValues(item);
			//setRowQty(row, 'discount', item.discount);
			
			
		}
		setRowPaise(row, 'total', (item.rate*item.qty));
		// set the calculated values back into the row
		if (prefVAT == 'Y') {
			setRowPaise(row, 'vat_display', item.vat_rate);
		}
		setRowPaise(row, 'amount', item.amount);
		

	}
	
	function calcItemStoredValues(item) {
		item.rate = item.rate_display;
		item.qty = item.qty_display;
	}
	
	function calcItemValues(item) {
		item.rate = item.rate_display;
		item.qty = item.qty_display;
		item.discount = (((item.rate/100) * item.qty)* item.discount_per)/100;
		if (prefVAT == 'Y') {
			
			var totalTax = (item.rate/100*item.qty -item.discount) * item.vat_rate/ 100;
			item.tax_total = totalTax;
		}
		
		if (prefVAT == 'Y') {
			item.amount = (((item.rate/100) * item.qty)- (item.discount) + (item.tax_total) )*100 ;
		} else {
			item.amount = (((item.rate/100) * item.qty)- (item.discount)  )*100 ;
		}
		
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
		if ( document.getElementById("status_fld").value == 'A' || document.getElementById("status_fld").value == 'O') {
			getElementByName(row,"status_ar").value = dlgForm.item_status.value;
			var flagImg = row.cells[gColIndexes.wo_item_name].getElementsByTagName("img")[0];
			flagImg.src = cpath+"/images/"+(dlgForm.item_status.value == "A" ? "green" : (dlgForm.item_status.value == 'R' ? "red" : "empty" ))+"_flag.gif";
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
		
		
	}
	
	function rowHiddenToLabels(row, colIndexes) {

		for (var fieldName in colIndexes) {

			var index = colIndexes[fieldName];
			if (index < 0 || index >= row.cells.length)
				continue;

			var hiddenObj = getElementByName(row, fieldName);
			if (!hiddenObj)
				continue;
			if(fieldName == "wo_item_name") {
				setNodeText(row.cells[index], hiddenObj.value, 20, hiddenObj.value);
			} else if(fieldName == "description"){
				setNodeText(row.cells[index], hiddenObj.value, 40, hiddenObj.value);
			} else {
				setNodeText(row.cells[index], hiddenObj.value);
			}
			
		}
	}
	
	
	function resetTotals(){
		var totalNoOfRows;
		totalNoOfRows = getNumItems();

		var itemTotalWithoutTax = 0;
		var itemDiscounts = 0;
		var itemTotal = 0;
		var vatTaxes = 0;
		var woDiscount = 0;
		var serviceTax = document.getElementById("servicetax").value!=""? parseFloat(document.getElementById("servicetax").value) : 0;
		var cessTax = document.getElementById("cess").value!=""? parseFloat(document.getElementById("cess").value) : 0;
		var serviceTaxAmount = 0.00;
		var cessTaxAmount = 0.00;
		var totalTax = 0.00;
		
		for (var i=0;i<totalNoOfRows;i++) {
			rowObj = getItemRow(i)
			if (getElementByName(rowObj,'_deleted').value == 'true')
				continue;
			itemTotalWithoutTax += (((getRowPaise(rowObj, "qty")/100)*(getRowPaise(rowObj, "rate")/100))-(getRowPaise(rowObj, "discount")/100));
			itemDiscounts += getRowPaise(rowObj, "discount");
			itemTotal += getRowPaise(rowObj, "amount");
			if (prefVAT == 'Y') {
				vatTaxes += getRowPaise(rowObj, "item_tax");
			}
		}
		serviceTaxAmount = (itemTotalWithoutTax*serviceTax);
		if (prefVAT == 'Y') {
			totalTax = serviceTaxAmount + vatTaxes;
		} else {
			totalTax = serviceTaxAmount;
		}
		cessTaxAmount = ((serviceTaxAmount+vatTaxes)*cessTax)/100;
		
		totalTax = totalTax + cessTaxAmount;
		setLabel ('lblItemTotal', formatAmountPaise(itemTotal), false);
		
		if (prefVAT == 'Y') {
			setLabel('lblTotalVat', formatAmountPaise(vatTaxes, false));
		}
		setLabel('lblServiceTaxes', formatAmountPaise(serviceTaxAmount, false));
		setLabel('lblTotalTaxes', formatAmountPaise(totalTax, false));
		woForm.total_tax.value = formatAmountPaise(totalTax, false);
		setLabel('lblCessTaxes', formatAmountPaise(cessTaxAmount, false));
		setLabel('lblDiscount', formatAmountPaise(itemDiscounts, false));
		woForm.total_discount.value = formatAmountPaise(itemDiscounts, false);
		setLabel('lblWOTotal', formatAmountPaise((itemTotal+serviceTaxAmount+cessTaxAmount), false));
		woForm.total_amount.value = formatAmountPaise(itemTotal+serviceTaxAmount+cessTaxAmount);
	  
	}
	
	function saveAndPrintWO() {
		woForm._printAfterSave.value = 'Y';
		if ( !saveWO() ) return false;;
		submitSave();
		woForm._printAfterSave.value = 'N';
	}
	
	function openEditDialogBox(obj) {
		resetDetails();
		var row = findAncestor(obj, "TR");
		YAHOO.util.Dom.addClass(row, 'editing');
		updateGridToDialog(row);
		dlgForm.wo_item_name.focus();
	}
	
	function getRowItemIndex(row) {
		return row.rowIndex - getFirstItemRow();
	}
	
	function updateGridToDialog (rowObj) {

		hiddenToForm(rowObj, dlgForm);
		if ( dlgForm.item_status )
			setSelectedIndex(dlgForm.item_status, getElementByName(rowObj,"status_ar").value);
	
		gRowUnderEdit = getRowItemIndex(rowObj);

		//oItemAutoComp._bItemSelected = true;
		document.getElementById("prevDialog").disabled = false;
		document.getElementById("nextDialog").disabled = false;

		button = rowObj.cells[EDIT_COL];
		detaildialog.cfg.setProperty("context", [button, "tr", "br"], false);
		//editItemAfterAdded();
		/*if(prefVAT == 'Y')
			document.getElementById("lblvat_rate").innerHTML = dlgForm.vat_rate.value;*/
		detaildialog.show();
	}
	
	function onChangeCostPrice() {
		formatAmountObj(dlgForm.rate_display);
		dlgForm.discount_per.value = '0';	// force recalc of discount based on percent
		calcDlgValues();
	}
	
	function getFirstItemRow() {
		// index of the first charge item: 0 is header, 1 is first charge item.
		return 1;
	}
	
	function setRowQty(row, name, qty) {
		setElementAmount(getElementByName(row, name), qty, qtyDecimal != 'Y');
	}
	
	function onChangeQty() {
		formatAmountObj(dlgForm.qty_display, qtyDecimal);
		dlgForm.discount_per.value = '0';	// force recalc of discount based on percent
		dlgForm.discount.value = '0';
		calcDlgValues();
	}
	

	function onChangeDiscountAmt () {
		formatAmountObj(dlgForm.discount_per, true);
		calcDlgValues();
		
		dlgForm.discount.value = formatAmountPaise((dlgForm.rate_display.value * dlgForm.qty_display.value ) * dlgForm.discount_per.value , null );
	}
	
	function onChangeDiscountPer(obj) {
		dlgForm.discount_per.value = obj.value / (dlgForm.rate_display.value * dlgForm.qty_display.value ) * 100;
		formatAmountObj(dlgForm.discount_per, true);
		calcDlgValues();
	}
	
	function onChangeTaxAmt() {
		if (prefVAT == 'Y') {
			formatAmountObj(dlgForm.vat_rate, qtyDecimal);
		//var totalTax = (((dlgForm.rate_display/100) * dlgForm.qty_display)* dlgForm.vat_display)/100;
		//item.tax_total = totalTax*100;
			dlgForm.item_tax.value = '0';	// force recalc of discount based on percent
		}
		calcDlgValues();
	}
	
	function getNumItems() {
		// header, hidden template row: totally 3 extra
		return document.getElementById("itemtabel").rows.length-2;
	}
	
	function getTemplateRow() {
		// gets the hidden template row index: this follows header row + num charges.
		return getNumItems() + 1;
	}
	
	function getItemRow(i) {
		i = parseInt(i);
		var table = document.getElementById("itemtabel");
		return table.rows[i + getFirstItemRow()];
	}
	
	function setLabel(labelId, value) {
		var label = document.getElementById(labelId);
		label.textContent = '';
		label.innerHTML = value;
	}
	
	function onKeyPressAddQty(e) {
		e = (e) ? e : event;
		var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
		if ( charCode==13 || charCode==3 ) {
			onDialogSave();
			return false;
		}
		
		if ( !enterNumOnlyANDdot(e) ){
			return false;
		}
		
		return true;
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
		openEditDialogBox(rowObj);
	}
	
	function saveWO() {
		var length = getTableRowsWithNoDeletedRows();
		if (length < 1) {
			showMessage("js.resourcemanagement.workorder.norowsingrid");
			return false;
		}

		if (document.getElementById("expected_received_date").value!="") {
	       var deldate = getDatePart(parseDateStr(document.getElementById("expected_received_date").value));
	       var wodate = getDatePart(new Date());
	       if (daysDiff(wodate,deldate) < 0) {
	       		showMessage("js.resourcemanagement.workorder.equaltopodate");
	       		document.getElementById("expected_received_date").focus();
	       		return false;
	       }
		}
		setWOStatus(document.getElementById("status_fld").value);
		
		var allChecked = false;
		var itemListTable = document.getElementById("itemtabel");
		var numRows = getNumItems();

		// create an item count map for checking duplicates
		var itemCount = {};
		for (var k=0; k<numRows; k++) {
			var rowObj = getItemRow(k);
			var itemName = getElementByName(rowObj, 'wo_item_name').value;
			if (itemCount[itemName] == undefined)
				itemCount[itemName] = 1;
			else
				itemCount[itemName]++;
		}

		var activeRows = 0;
		for (var l=0; l<numRows; l++) {
			var rowObj = getItemRow(l);
			if (getElementByName(rowObj,'_deleted').value == 'true')
				continue;

			activeRows++;
			var itemName = getElementByName(rowObj, 'wo_item_name').value;


			if (itemCount[itemName] > 1) {
				var msg=itemName;
				msg+=getString("js.resourcemanagement.workorder.hasduplicateentries");
				alert(msg);
				return false;
			}

			var rate = getRowPaise(rowObj,'rate');
			if (rate == 0) {
				var msg=itemName;
				msg +=getString("js.resourcemanagement.workorder.rateisrequired");
				alert(msg);
				openEditDialogBox(rowObj);
				dlgForm.rate_display.focus();
				return false;
			}

		}
		return true;
	}
	
	function submitSave(){
		woForm.btnSaveWo.disabled = true;
		woForm.submit();
	}
	
	function onlySaveWO(){
		var status = saveWO(); 
		if ( !status ) {
			return false;
		}
		submitSave();
		
	}
	
	function getTableRowsWithNoDeletedRows(){
		var itemListTable = document.getElementById("itemtabel");
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
	
	
	function onChangeServiceTax() {
		resetTotals();
	}
	
	function onChangeCESS() {
		resetTotals();
	}
	
	function allRowsHiddenToLabels() {
		var numItems = getNumItems();
		for (var k=0; k < numItems; k++) {
			var row = getItemRow(k);

			calcRowValues(row, 'stored');
			rowHiddenToLabels(row, gColIndexes);
			
		}
	}
	
	function setWOStatus(status){
		woForm.status.value = ( woForm.status_fld && woForm.status_fld.value == 'FC') ? 'FC' : status;
	}
	
	function alertUsrApproval(itemApprovedrRejected){

		if ( itemApprovedrRejected )
			return true;

		if (!confirm(getString("js.resourcemanagement.workorder.no.approve.reject")))
	        	return false;

	    return true;
	}
	
	function saveAndSetStatusWO(status){
		var itemListTable = document.getElementById("itemtabel");
		var numRows = getNumItems();
		var itemApprovedrRejected = true;

		for (var l=0; l<numRows; l++) {
			var rowObj = getItemRow(l);
			if (getElementByName(rowObj,'_deleted').value == 'true')
				continue;

			itemApprovedrRejected = itemApprovedrRejected && ( getElementByName(rowObj, 'status_ar').value == 'A' || getElementByName(rowObj, 'status_ar').value == 'R' );
		}
		//approval validation
		if ( status == 'A' ) {
			if ( !alertUsrApproval(itemApprovedrRejected) )
				return false;
		}
		
		if ( !saveWO() ) return false;;
		setWOStatus(status);
		submitSave();
	}
	
	function printWO() {
		var printUrl= cpath+"/resourcemanagement/workorder.do?_method=generateWOprint&wo_no="+woForm.wo_no.value;
		window.open(printUrl);
	}