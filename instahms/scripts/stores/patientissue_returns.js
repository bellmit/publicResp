function init(){
	checkstoreallocation();
	initDialog();
	initItemsAutoComplete();
	fillIndents();
	if (empty(patIndentDetailsJSON)) {
		populatePackages();
	}
	setMedBatches();
	setFocus();
	initLoginDialog();
	if ( returnNo != 0 )
		getReport();
}

function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2 ) {
 		if(deptId == "") {
 			showMessage("js.sales.issues.issuereturn.noassignedstore.donthaveanyaccess.thisscreen");
 			document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}

function setFocus(){
	if ( document.patientIssueReturnForm.btnAddItem )//auto fill indents will not allow additional returns
		document.patientIssueReturnForm.btnAddItem.focus();
}

function initDialog(){

		dialog = new YAHOO.widget.Dialog("dialog",
				{
					width:"600px",
					context : ["itemListtable", "tr", "br"],
					visible:false,
					modal:true,
					constraintoviewport:true
				} );

		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog,
	                                              	correctScope:true} );
		dialog.cfg.queueProperty("keylisteners", escKeyListener);
		dialog.render();
	}
	function handleCancel() {
		dialog.cancel();
	}

	function setMedBatches(){
		if ( itemNamesArray == undefined ) return false;
		for(var i = 0;i<itemNamesArray.length;i++){
		 	var medId = itemNamesArray[i].medicine_id;
			gMedicineBatches[medId] = medBatches[medId];
		}
	}

    function getUniqueMedicineNames(arrayWithDups){
    	var medNamesNdIdsArray = new Array();

		if ( arrayWithDups == undefined )
			return medNamesNdIdsArray;
    	for(var d = 0 ;d<arrayWithDups.length;d++){
    		if( medNamesNdIdsArray == 0 || !contains(medNamesNdIdsArray,arrayWithDups[d].medicine_id) ){
    			medNamesNdIdsArray.push(arrayWithDups[d]);
    		}
    	}

    	return medNamesNdIdsArray;
    }

    function contains(array,obj) {
	    for(var i =0;i<array.length;i++) {
	        if(isEqual(array[i].medicine_id,obj))return true;
	    }
	    return false;
	}

	//comparator
	function isEqual(obj1,obj2) {
	    if(obj1 == obj2) return true;
	    return false;
	}
var oItemAutoComp = null;
	function initItemsAutoComplete(){
		if (oItemAutoComp != undefined) {
			oItemAutoComp.destroy();
		}

		if ((typeof(itemsListJSON) != 'undefined') && (itemsListJSON != null)){

			itemNamesArray = itemsListJSON[document.patientIssueReturnForm.dept_to.value];
			var storeWiseItemsArray = [];
			storeWiseItemsArray = itemNamesArray;
			var medNamesUniqueArray = getUniqueMedicineNames(storeWiseItemsArray);
			var dataSource = new YAHOO.widget.DS_JSArray(medNamesUniqueArray);
			dataSource.responseSchema = {
				resultsList : "result",
				fields : [ {key : "cust_item_code_with_name"},
				           {key : "medicine_name"},
						   {key : "medicine_id"},
						   {key : "issue_uom"},
						   {key : "package_uom"},
						   {key : "issue_base_unit"} ]
		 	};

			oItemAutoComp = new YAHOO.widget.AutoComplete('items','item_dropdown', dataSource);
			oItemAutoComp.prehightlightClassName = "yui-ac-prehighlight";
			oItemAutoComp.typeAhead = false;
			oItemAutoComp.useShadow = false;
			oItemAutoComp.allowBrowserAutocomplete = false;
			oItemAutoComp.minQueryLength = 0;
			oItemAutoComp.forceSelection = true;
			oItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
			oItemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
			oItemAutoComp._bItemSelected = true;
			oItemAutoComp.itemSelectEvent.subscribe(onSelectItem);

			oItemAutoComp.textboxFocusEvent.subscribe(function(){
				var sInputValue = YAHOO.util.Dom.get('items').value;
				if(sInputValue.length === 0) {
					var oSelf = this;
					setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
				} });
			oItemAutoComp.itemSelectEvent.subscribe(getIssueReturnsItemDetails);
		}
	}
	/*
	 * Called on selection of an item in the item auto comp
	 */
	function onSelectItem(type, args) {
		document.getElementById("items").value = args[2][1];
	}

	var itemDetails = {};
	var itemDetailsMap = {};

	function getIssueReturnsItemDetails(sType, aArgs){
		var item = aArgs[2];
		var medicineId = item[2];
		setDetails(item,medicineId);

	}

	function setDetails(item,medicineId){
		var batchNoArray = [];
		var qty = [];
		var expDt = [];
		batchNoArray.length = itemNamesArray.length;
		qty.length = itemNamesArray.length;
		expDt.length = itemNamesArray.length;
		var itemBarCode = '';
		var issueUnit = '',packageUOM = '',pckSize  = '',issueUOM = '';
		var batchIndex  = 0;
		for(var i = 0;i<itemNamesArray.length;i++){
			if( medicineId == itemNamesArray[i].medicine_id ){
				itemDetails = {};
				batchNoArray.length = 1+batchIndex;
				batchNoArray[batchIndex] = itemNamesArray[i].batch_no;
				qty[batchIndex] = ( itemNamesArray[i].returnqty );
				expDt[batchIndex] = itemNamesArray[i].exp_dt;
				itemBarCode = itemNamesArray[i].item_barcode_id;
				issueUnit = itemNamesArray[i].issue_units;
				packageUOM = itemNamesArray[i].package_uom;
				pckSize = itemNamesArray[i].issue_base_unit;
				issueUOM = itemNamesArray[i].issue_uom;


				itemDetails["medicine_id"] = medicineId;
				itemDetails["medicine_name"] = itemNamesArray[i].medicine_name;
				itemDetails["insurance_category_id"] = itemNamesArray[i].insurance_category_id;
				itemDetails["rate"] = itemNamesArray[i].item_unit == 'P' ? parseFloat(itemNamesArray[i].unit_rate*itemNamesArray[i].issue_base_unit).toFixed(decDigits) : parseFloat(itemNamesArray[i].unit_rate);
				itemDetails["discount"] = itemNamesArray[i].discount;
				itemDetails["rtn_pkg_size"] = itemNamesArray[i].issue_units;
				itemDetails["exp_dt"] = itemNamesArray[i].exp_dt;
				itemDetails["returnqty"] = (  itemNamesArray[i].returnqty );
				itemDetails["qty"] = ( itemNamesArray[i].returnqty );
				itemDetails["unit_mrp"] = itemNamesArray[i].unit_mrp;
				itemDetails["amount"] = itemNamesArray[i].amount;
				itemDetails["patient_amount"] = itemNamesArray[i].patient_amount == undefined ? 0 : itemNamesArray[i].patient_amount;
				itemDetails["insurance_claim_amt"] = itemNamesArray[i].insurance_claim_amt == undefined ? 0 : itemNamesArray[i].insurance_claim_amt;
				itemDetails["item_barcode_id"] = itemNamesArray[i].item_barcode_id;
				itemDetails["patient_percent"] = itemNamesArray[i].patient_percent;
				itemDetails["patient_amount_cap"] = itemNamesArray[i].patient_amount_cap;
				itemDetails["issue_units"] = itemNamesArray[i].issue_units;
				itemDetails["package_uom"] = itemNamesArray[i].package_uom;
				itemDetails["issue_base_unit"] = itemNamesArray[i].issue_base_unit;
				itemDetails["control_type_name"] = itemNamesArray[i].control_type_name;
				itemDetails["item_batch_id"] = itemNamesArray[i].item_batch_id;
				itemDetails["issued_qty_in_units"] = itemNamesArray[i].returnqty;
				itemDetails["item_unit"] = itemNamesArray[i].item_unit;
				
				var claimAmts = new Array();
				for( var j = 0;j<visitissuedClaimJSON.length;j++ ){
					if( visitissuedClaimJSON[j].item_batch_id == itemNamesArray[i].item_batch_id ) {
						claimAmts.push( visitissuedClaimJSON[j].insurance_claim_amt );
					}
				}

				itemDetails["claim_amts"] = claimAmts;

				itemDetailsMap[itemNamesArray[i].batch_no] = itemDetails;
				batchIndex++;

			}
		}
		if( prefbarcode == 'Y' )
			document.patientIssueReturnForm.eItemBarcode.value = itemBarCode;
 		document.patientIssueReturnForm.eBatch.length = batchNoArray.length > 1 ? 1+batchNoArray.length : 1;

		var batchOptIndex = 0;
 		if ( batchNoArray.length > 1 ) {
 			batchOptIndex = 1;
		    document.patientIssueReturnForm.eBatch.options[0].text = '-- Select --';
		    document.patientIssueReturnForm.eBatch.options[0].value = '';
		    document.patientIssueReturnForm.eBatch.disabled = false;
	     	document.patientIssueReturnForm.eBatch.selectedIndex = 0;
	    }else{
	    	document.patientIssueReturnForm.eBatch.disabled = true;
	     	document.patientIssueReturnForm.eBatch.selectedIndex = 0;
	    }

	    for(var i = 0;i<batchNoArray.length;i++){
	    	document.patientIssueReturnForm.eBatch.options[batchOptIndex].text =  batchNoArray[i]+"/"+qty[i]+( expDt[i] == null ? '' : "/"+formatExpiry(new Date(expDt[i])));
		    document.patientIssueReturnForm.eBatch.options[batchOptIndex].value = batchNoArray[i];
		    batchOptIndex++;
	    }

	    setUOMOptions(document.patientIssueReturnForm.issue_unit,{"issue_units": issueUOM,"package_uom":packageUOM});
	    document.patientIssueReturnForm.ePackageSize.value = pckSize;

	}


	function onChangeStore(obj){
		initItemsAutoComplete();
		fillIndents();
		if (empty(patIndentDetailsJSON)) {
			populatePackages();
		}
		//delete rows
	}
	var editRowIndex = 0;
	function validateEditDialog(){

		var item = itemDetailsMap[getElementByName(document.getElementById("indentItemListTab").rows[editRowIndex],"batch_no").value];

		var returnQty = document.patientIssueReturnForm.issue_unit.value == 'I' ? document.getElementById("eReturnQty").value : document.getElementById("eReturnQty").value*item["issue_base_unit"];
		var issuedQty =  item["issued_qty_in_units"];

		if( returnQty > issuedQty ){
			var msg=getString("js.sales.issues.issuereturn.returnquantity.notbegreater.issuedquantity");
			msg+=" ";
			msg+=issuedQty;
			msg+=" ";
			msg+= getSelText(document.patientIssueReturnForm.issue_unit);
			document.getElementById("eReturnQty").focus();
			return false;
		}

		return true;
	}

	function validateAddDialog(){


		var valid = true;

		valid = valid && validateItem();
		valid = valid && validateBatchNo();
		valid = valid && validateReturnQty();
		return valid;
	}

	function validateItem(){

		if ( document.patientIssueReturnForm.items.value == '' ) {
			showMessage("js.sales.issues.issuereturn.selectanitem");
			document.patientIssueReturnForm.items.focus();
			return false;
		}
		return true;
	}

	function validateBatchNo(){
		if ( document.patientIssueReturnForm.eBatch.value == '' ) {
			showMessage("js.sales.issues.issuereturn.selectbatch");
			document.patientIssueReturnForm.eBatch.focus();
			return false;
		}
		return true;
	}

	function validateReturnQty(){
		var itemDetails = itemDetailsMap[document.patientIssueReturnForm.eBatch.value];
		if ( document.getElementById("eReturnQty").value == '' ) {
			showMessage("js.sales.issues.issuereturn.returnquantity.notbezero");
			document.getElementById("eReturnQty").focus();
			return false;
		}

		if (allowDecimalsForQty == 'Y') {
			if (!validateDecimal(document.getElementById("eReturnQty"), getString("js.sales.issues.enter.validquantity"), 2))
				return false;

		} else {
			if (!validateInteger(document.getElementById("eReturnQty"), getString("js.sales.issues.enter.validquantity"), 2))
				return false;
		}

		var returnQty = document.patientIssueReturnForm.issue_unit.value == 'I' ? document.getElementById("eReturnQty").value : document.getElementById("eReturnQty").value*itemDetails["issue_base_unit"];
		var issuedQty = itemDetails["issued_qty_in_units"];

		if( returnQty > issuedQty ){
			var msg=getString("js.sales.issues.issuereturn.returnquantity.notbegreater.issuedquantity");
			msg+=" ";
			msg+=issuedQty;
			msg+=" ";
			msg+=getSelText(document.patientIssueReturnForm.issue_unit);
			alert(msg);
			document.getElementById("eReturnQty").focus();
			return false;
		}
		return true;

	}

	function addItems(validateDialog){
		var valid = true;
		if ( validateDialog )
			valid = validateAddOrEdit();

		valid &= checkDups();
		if ( valid ){
			addItemsToTable(null,null,null);
		}
		clearDialog();
		showNextDialog();

	}

	function showNextDialog() {
		getItemDialog(document.patientIssueReturnForm.btnAddItem);
	}

	function clearDialog(){
		document.patientIssueReturnForm.items.value = '';
		if ( prefbarcode == 'Y' )
			document.patientIssueReturnForm.eItemBarcode.value = '';
		document.patientIssueReturnForm.eBatch.length = 0;
		document.patientIssueReturnForm.eReturnQty.value = '';
		document.patientIssueReturnForm.ePackageSize.value = '';
		loadSelectBox(document.patientIssueReturnForm.issue_unit, [], null, null);
	}

	function validateAddOrEdit(){

		if ( editRowIndex == 0 ) {//add dialog
			if ( !validateAddDialog() )return false;
		} else {//edit dialog
			if ( !validateEditDialog() )return false;
		}

		return true;
	}
	function addItemsToTable(qty,batch,indentDetails){
		var table = document.getElementById("indentItemListTab");
		var numRows = table.rows.length;
		var templateRow = table.rows[numRows-1];
		var row;
		if ( editRowIndex == 0 ) {//insert new row
			row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
			itemDetails = itemDetailsMap[document.patientIssueReturnForm.eBatch.value];
			row.className = "added";

		} else {//edit row
			row = table.rows[editRowIndex];
			row.className = "edited";
			if ( batch != null ){
				itemDetails = itemDetailsMap[getElementByName(row,"batch_no").value];//auto fill indents
			} else {
				itemDetails = itemDetailsMap[document.patientIssueReturnForm.eBatch.value];
			}

		}
		var qtyVal = parseFloat(getFieldValue('eReturnQty'));
		var issueUnit = getFieldValue('issue_unit');
		var pkgSize = parseFloat(getFieldValue('ePackageSize'));
		var taxAmt = 0;
		var taxRate = 0;
		var originalTaxAmt = 0;
		
		if(batch != null){
			qtyVal = qty;
			pkgSize =  indentDetails.issue_base_unit;
			issueUnit = indentDetails.qty_unit;
		}

		if(issueUnit == 'P') {
			qtyVal = qtyVal * pkgSize;
		}



		var medicineId = (batch == null ? itemDetails["medicine_id"] : batch["medicine_id"]);
		var mrp = ( batch == null ? itemDetails["rate"] : parseFloat(batch.unit_rate * batch.issue_base_unit).toFixed(decDigits) );
		var batchNo = ( batch == null ? document.patientIssueReturnForm.eBatch.value : batch.batch_no);
		var qtyToUse = ( batch == null ? itemDetails["qty"] : qty )
		var issuedQty = ( document.patientIssueReturnForm.issue_unit.value == 'I' ? itemDetails["issued_qty_in_units"] : qtyToUse );
		qty = ( batch == null ? document.getElementById("eReturnQty").value : qty );
		var issueUnits = ( batch == null ? itemDetails["issue_base_unit"] : batch.issue_base_unit);
		var controlTypeName = ( batch == null ? itemDetails["control_type_name"] : batch.control_type_name);
		var unitRate = ( batch == null ? ( itemDetails["item_unit"] == 'P'
			? parseFloat(itemDetails["rate"] / itemDetails["issue_base_unit"]).toFixed(decDigits) : parseFloat(itemDetails["rate"]).toFixed(decDigits)) : parseFloat(batch.unit_rate).toFixed(decDigits) );
		var patAmount = ( batch == null ? itemDetails["patient_amount"] : batch.patient_amount);
		var insClaimAmt = ( batch == null ? itemDetails["insurance_claim_amt"] : batch.insurance_claim_amt );
		var expDt = ( batch == null ? itemDetails["exp_dt"] : batch.exp_dt );
		var discount = ( batch == null ? itemDetails["discount"] : batch.discount);
		var itemUnits = ( indentDetails == null ? document.patientIssueReturnForm.issue_unit.value : indentDetails.qty_unit );
		var medName = ( batch == null ? itemDetails["medicine_name"] : batch.medicine_name );
		var medId = ( batch == null ? itemDetails["medicine_id"] : batch.medicine_id );
		var barCode = ( batch == null ? itemDetails["item_barcode_id"] : batch.item_barcode_id );
		var pkgSize = ( batch == null ? itemDetails["issue_base_unit"] : batch.issue_base_unit );
		var itemBatchId = ( batch == null ? itemDetails["item_batch_id"] : batch.item_batch_id );
		var claimAmts  = ( batch == null ? itemDetails["claim_amts"] : batch.claim_amts );
		var insCatId = ( batch == null ? itemDetails["insurance_category_id"] : batch.insurance_category_id );


		for(var i=0; i < itemTaxDetails.length; i++) {
			for(var j=0; j < subgroupNamesList.length; j++) {
				for(var k=0; k < itemNamesArray.length; k++){
					if(itemTaxDetails && itemTaxDetails[i] && itemTaxDetails[i].medicine_id == medicineId 
							 && (itemTaxDetails[i].package_id == document.getElementById("package_id").value || (!document.getElementById("package_id").value && !itemTaxDetails[i].package_id))
							&& itemNamesArray[k].item_batch_id == itemTaxDetails[i].item_batch_id && itemNamesArray[k].batch_no == batchNo
							&& itemTaxDetails[i].tax_sub_group_id && itemTaxDetails[i].tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
						var itemGroupId = subgroupNamesList[j].item_group_id;
						getElementByName(row,"taxrate"+itemGroupId).value = itemTaxDetails[i].tax_rate;
						taxRate += itemTaxDetails[i].tax_rate;
						taxAmt += parseFloat(parseFloat(itemTaxDetails[i].unit_tax)*(qtyVal));
						originalTaxAmt += parseFloat(parseFloat(itemTaxDetails[i].unit_original_tax)*(qtyVal));
						getElementByName(row,"taxamount"+itemGroupId).value = parseFloat(parseFloat(itemTaxDetails[i].unit_tax)*(qtyVal)).toFixed(decDigits);
						getElementByName(row,"originaltaxamount"+itemGroupId).value = parseFloat(parseFloat(itemTaxDetails[i].unit_original_tax)*(qtyVal)).toFixed(decDigits);
						getElementByName(row,"taxsubgroupid"+itemGroupId).value = subgroupNamesList[j].item_subgroup_id;
					}
				}
			}
		}

		var labelNodes = row.cells[0].getElementsByTagName("label");
		if (labelNodes && labelNodes[0])
			node = labelNodes[0];
		// setting the values to grid columns
		setNodeText(node,medName);
		if (controlTypeName != 'Normal')
			row.cells[1].innerHTML = "<font color='blue'>"+controlTypeName+"</font>";
		else
			setNodeText(row.cells[1],controlTypeName);
		setNodeText(row.cells[2],batchNo);
		setNodeText(row.cells[3],expDt == null ? '' : formatExpiry(new Date(expDt)));//if item category has no expiry date
		setNodeText(row.cells[4],issuedQty);
		setNodeText(row.cells[5],qty);
		setNodeText(row.cells[6],( indentDetails == null ? getSelText(document.patientIssueReturnForm.issue_unit) : ((indentDetails.uom == '' ? indentDetails.issue_units: indentDetails.uom)) ));
		if ((null != showCharges) && (showCharges == 'A')) {
			setNodeText(row.cells[7],mrp);
			setNodeText(row.cells[8],unitRate);
		}
		getElementByName(row,"insurance_category_id").value = insCatId;
		getElementByName(row,"medicine_id").value = medId;
		getElementByName(row,"medicine_name").value = medName;
		getElementByName(row,"batch_no").value = batchNo;
		getElementByName(row,"item_batch_id").value = itemBatchId;
		getElementByName(row,"qty").value = ( itemUnits == 'I' ? qty : parseFloat(qty*pkgSize).toFixed(decDigits) );
		getElementByName(row,"discount").value = discount;
		getElementByName(row,"item_unit").value = itemUnits;
		getElementByName(row,"rtn_pkg_size").value = issueUnits != null ? issueUnits : itemDetails["issue_base_unit"];
		getElementByName(row,"rate").value = mrp;
		getElementByName(row,"item_barcode_id").value = barCode;
		getElementByName(row,"unit_rate").value = unitRate;
		if ( indentDetails != null ) {//auto fill indents needs this
			getElementByName(row,"patient_indent_no").value = indentDetails.patient_indent_no;
			getElementByName(row,"indent_item_id").value = indentDetails.indent_item_id;
		}
		getElementByName(row,"patient_percent").value = ( batch == null ?  itemDetails["patient_percent"] : batch.patient_percent );
		getElementByName(row,"patient_amount_cap").value = ( batch == null ?  itemDetails["patient_amount_cap"] : batch.patient_amount_cap );
		getElementByName(row,"insurance_claim_amt").value = insClaimAmt;
		getElementByName(row,"patient_amount").value = patAmount;
		getElementByName(row,"patient_amount_ref").value = patAmount;
		getElementByName(row,"patient_percent_ref").value = ( batch == null ?  itemDetails["patient_percent"] : batch.patient_percent );
		getElementByName(row,"patient_amount_cap_ref").value = ( batch == null ?  itemDetails["patient_amount_cap"] : batch.patient_amount_cap );

		var qtyInUnits = getElementByName(row,"item_unit").value == 'I' ? qty : qty*(batch == null ? itemDetails["issue_base_unit"] : batch.issue_base_unit) ;
		var rowDiscountPaise = getPaise(parseFloat(discount)* qtyInUnits);
		var rowAmtPaise = getPaise(parseFloat(unitRate) * qtyInUnits) - (rowDiscountPaise);
		var finAmt = formatAmountPaise(rowAmtPaise,2);
		if ((null != showCharges) && (showCharges == 'A'))
			setNodeText(row.cells[9],formatAmountValue(finAmt));//amount
		setNodeText(row.cells[10], taxAmt);//Tax
		setNodeText(row.cells[12], taxAmt);
		getElementByName(row,"tax_amount").value = taxAmt;
		getElementByName(row,"original_tax_amount").value = originalTaxAmt;
		getElementByName(row,"tax_rate").value = taxRate;
		calcInsuranceAmts(row,finAmt,qtyInUnits, formatAmountPaise(rowDiscountPaise));
		if ((null != showCharges) && (showCharges == 'A')) recalcTotAmt(row);

	}

function reCalculateInsAmt(){
	if (document.getElementById('activePackage').value) {
		return;
	}
	var itemtable = document.getElementById("indentItemListTab");
	var len = itemtable.rows.length;
	var tabLen = len-1;

	for (e = 1; e<tabLen; e++){
		var row = itemtable.rows[e];
		var unitRate = getElementByName(row,"unit_rate").value;
		var discount = getElementByName(row,"discount").value;
		var qty = ( getElementByName(row,"item_unit").value == 'I' ? getElementByName(row,"qty").value : qty*getElementByName(row,"rtn_pkg_size").value );

		var rowDiscountPaise = getPaise(parseFloat(discount)* qty);
		var rowAmtPaise = getPaise(parseFloat(unitRate) * qty) - (rowDiscountPaise);
		var finAmt = formatAmountPaise(rowAmtPaise,2);

		calcInsuranceAmts(row,finAmt,qty, formatAmountPaise(rowDiscountPaise));
		recalcTotAmt(row)
	}
}

function recalcTotAmt(row){
	var itemtable = document.getElementById("indentItemListTab");
	var len = itemtable.rows.length;
	var tabLen = len-1;
	var totAmt = 0;
	var totPatAmt = 0;
	var totClaimAmt = 0;
	var totTax = 0;
	for (i=1; i<tabLen; i++){
		var row = itemtable.rows[i];
		totAmt = totAmt + parseFloat(row.cells[9].innerHTML);
		totTax = totTax + parseFloat(getElementByName(row,"tax_amount").value);
		totPatAmt = totPatAmt + parseFloat(getElementByName(row,"patient_amount").value);
		totClaimAmt = totClaimAmt + parseFloat(getElementByName(row,"pri_insurance_claim_amt").value);
		if ( !empty(getElementByName(row,"sec_insurance_claim_amt").value) )
			totClaimAmt = totClaimAmt  + parseFloat(getElementByName(row,"sec_insurance_claim_amt").value)
	}
	document.getElementById("totAmt").textContent = formatAmountValue(totAmt + totTax);
	document.getElementById("totTax").textContent = formatAmountValue(totTax);
	document.getElementById("totPatAmt").textContent = formatAmountValue(totPatAmt + totTax);
	document.getElementById("totPatTax").textContent = formatAmountValue(totTax);
	document.getElementById("totClaimAmt").textContent = formatAmountValue(totClaimAmt);
}

var claimAmts = new Array();
function calcInsuranceAmts(row,amt,update_qty,discount){
	claimAmts = new Array();
	var planId =  null != document.getElementById("plan_id")? document.getElementById("plan_id").value : 0 ;
	var isTpaBill = getBillIsTpa(document.getElementById("bill_no").value);
	var visitPlans = patientPlanDetails;

	if (visitPlans.length > 0 && isTpaBill){
		//if patient has insurance plan

		var patientAmt = getElementByName(row,"patient_amount_ref").value;
		var patientPer = getElementByName(row,"patient_percent_ref").value;
		var patientCap = getElementByName(row,"patient_amount_cap_ref").value;
		var insCatId = getElementByName(row,"insurance_category_id").value;

		var amtAfterClaim = amt;//this is before claim calculation
		var amtAfterClaimPaise = getPaise(amt);
		for( var j = 0;j<visitPlans.length;j++ ){
			claimAmt = getClaimAmt(visitPlans[j].plan_id,amtAfterClaim,document.patientIssueReturnForm.visit_type.value,insCatId, "true",discount);
			amtAfterClaim = amtAfterClaim - claimAmt;
			amtAfterClaimPaise = getPaise(amtAfterClaim);
			//claimAmts.push(claimAmt); //changing as per insurance3.0. refer below comment.
			claimAmts.push(0);
		}

/** As per insurance 3.0 changes we are always making sponsor amount to zero and
  * amount becomes patient amount for returns.
*/
		amtAfterClaimPaise = getPaise(amt);
		if ((null != showCharges) && (showCharges == 'A')) {
			getElementByName(row,"patient_amount").value = formatAmountPaise(amtAfterClaimPaise);
			setNodeText(row.cells[11],parseFloat(formatAmountPaise(amtAfterClaimPaise)).toFixed(decDigits));
			for( var i =0;i<claimAmts.length;i++ ) {
				getElementByName(row,( i == 0 ? "pri_" : "sec_" )+"insurance_claim_amt").value = claimAmts[i];
				setNodeText(row.cells[12+i],parseFloat(claimAmts[i]).toFixed(decDigits));
			}
		}

	} else{
		//he may not have insurance but may have a tpa
		var billNo = document.getElementById("bill_no").value;
		var medId = getElementByName(row,"medicine_id").value;
		var url;
		var ajaxReqObject = newXMLHttpRequest();
		//AJAX call to get whether this med. is claimable. If TPA is set, and category of item is claimable, the amt is claimable
		url = "StockPatientReturn.do?_method=isSponsorBill&medId="+medId+"&billNo="+billNo;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				getFinalAmts (ajaxReqObject.responseText, row,amt);
			}
		}
	}
}

function getFinalAmts(responseText, row,amt){
	var isInsurance = "";
	var claimAmt = 0;
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;

	if (responseText == 'true')
		claimAmt = amt;
	else
		claimAmt = 0;//non insurence bill

	var claimAmtPaise = parseFloat(getPaise(claimAmt));
	claimAmt = formatAmountPaise(Math.round(claimAmtPaise));


	if ((null != showCharges) && (showCharges == 'A')) {
		getElementByName(row,"pri_insurance_claim_amt").value = parseFloat(claimAmt).toFixed(decDigits);
		setNodeText(row.cells[13],parseFloat(claimAmt).toFixed(decDigits));
	}
	getElementByName(row,"patient_amount").value = parseFloat(amt - claimAmt).toFixed(decDigits);
	if ((null != showCharges) && (showCharges == 'A'))
		setNodeText(row.cells[11],parseFloat(amt - claimAmt).toFixed(decDigits));
}

function getClaimAmt(planId,amt,visitType,categoryId, firstOfCategory,discount){
	//multi-payer
	return calculateClaimAmount(planId,amt,visitType,categoryId, firstOfCategory,discount);
}
	function deleteReturn(trashObj){
		var row = getThisRow(trashObj);
		row.parentNode.removeChild(row);
		recalcTotAmt(null);
	}


	function getItemDialog(imgObj) {

		editRowIndex = 0;
		if ( !visitSelected ){
			showMessage("js.sales.issues.issuereturn.selectapatient.beforeaddingitems");
			document.patientSearch.patient_id.focus();
			return false;
		}
		clearDialog();
		if ( imgObj.name != 'btnAddItem' ){//edit mode
			var row = getThisRow(imgObj);
			editRowIndex = row.rowIndex;
			setDetails(item,getElementByName(row,"medicine_id").value);
			var item = itemDetailsMap[getElementByName(row,"batch_no").value];
			var itemUnit = getElementByName(row,"item_unit").value;
			var rtnPkgSize = getElementByName(row,'rtn_pkg_size').value;

			var labelNodes = row.cells[0].getElementsByTagName("label");
			document.patientIssueReturnForm.items.value = labelNodes[0].innerHTML;
			if (oItemAutoComp._elTextbox.value != '') {
				oItemAutoComp._bItemSelected = true;
				oItemAutoComp._sInitInputValue = oItemAutoComp._elTextbox.value;
			}
			if( prefbarcode == 'Y' )
				document.patientIssueReturnForm.eItemBarcode.value = getElementByName(row,"item_barcode_id").value;
			document.patientIssueReturnForm.eBatch.value = getElementByName(row,"batch_no").value;
			document.patientIssueReturnForm.eReturnQty.value = ( itemUnit == 'P' ? parseFloat(getElementByName(row,"qty").value/rtnPkgSize).toFixed(2) : getElementByName(row,"qty").value );
			document.patientIssueReturnForm.maxReturnQty.value = item["returnqty"];
			document.patientIssueReturnForm.issue_unit.value = itemUnit;

		}

		dialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
		dialog.show();
		document.patientIssueReturnForm.items.focus();
	}
	function formatExpiry(dateMSecs) {
		if (dateMSecs == null) return '(---)';
		var dateObj = new Date(dateMSecs);
		var dateStr = formatDate(dateObj, 'monyyyy', '-');
		return dateStr;
	}

	function fillIndents(){
		var table = document.getElementById('indentInfo');
		var detailstable = document.getElementById('indentItemListTab');

		var numItems = table.rows.length;
		for (var i=1; i<numItems-1; i++) {		// delete old indent
			table.deleteRow(1);
		}

		var numDetails = detailstable.rows.length;
		for (var j=1; j<numDetails-1; j++) {
			detailstable.deleteRow(1);
		}
		showIndentFields('none');
		var numItems = table.rows.length;
		if ( getAutoFillIndents() && !empty(indentsListJSON[document.patientIssueReturnForm.dept_to.value]) ) {
			var indents = indentsListJSON[document.patientIssueReturnForm.dept_to.value];
			for ( var i = 0 ;i<indents.length;i++){

				var templateRow = table.rows[numItems-1];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);

				// doctor name in first column
				setNodeText(row.cells[0], indents[i].patient_indent_no);

				// url to view presc in second col
				if (indents[i].indent_type == 'R' ) {
					var url = cpath + "/stores/PatientIndentViewReturn.do?_method=view&stop_doctor_orders=true";
					url += "&patient_indent_no="+ indents[i].patient_indent_no;
					url +="&patient_id="+ indents[i].visit_id;
				} else {
					var url = cpath + "/stores/PatientIndentView.do?_method=view&stop_doctor_orders=true";
					url += "&patient_indent_no="+ indents[i].patient_indent_no;
				}
				var anch = row.cells[1].getElementsByTagName("A")[0];
				anch.href = url;

				var headerRow = table.rows[0];
				headerRow.cells[2].style.display = 'none';
				row.cells[2].style.display = "none"//returns should dispense all indent items

				getElementByName(row, "patientIndentNoRef").value = indents[i].patient_indent_no;
			}
			fillIndentDetails();
			showIndentFields('block');
		}

	}

	function showIndentFields(value){
		document.getElementById('indentDetailsDiv').style.display= value;
	}

	function fillIndentDetails(){

		var indentDetails = patIndentDetailsJSON[document.patientIssueReturnForm.dept_to.value];

		for(var i = 0;i<indentDetails.length;i++){
			var indentDetail = indentDetails[i];
			itemDetails = [];
			itemDetails["medicine_id"] = indentDetail["medicine_id"];
			itemDetails["medicine_name"] = indentDetail["indent_medicine_name"];
			itemDetails["rate"] = indentDetail["mrp"];
			itemDetails["discount"] = indentDetail["discount"];
			itemDetails["item_unit"] = indentDetail["unit"];
			itemDetails["rtn_pkg_size"] = indentDetail["rtn_pkg_size"];
			itemDetails["exp_dt"] = indentDetail["exp_dt"];
			itemDetails["returnqty"] = indentDetail["issue_returnqty"];
			itemDetails["unit_mrp"] = indentDetail["unit_mrp"];
			itemDetails["amount"] = indentDetail["amount"];
			itemDetails["patient_amount"] = indentDetail["patient_amount"] == undefined ? 0 : indentDetail["patient_amount"];
			itemDetails["insurance_claim_amt"] = indentDetail["insurance_claim_amt"] == undefined ? 0 : indentDetail["insurance_claim_amt"];
			itemDetails["item_barcode_id"] = indentDetail["item_barcode_id"];
			itemDetails["unit"] = indentDetail["unit"];
			itemDetails["patient_percent"] = indentDetail["patient_percent"];
			itemDetails["patient_amount_cap"] = indentDetail["patient_amount_cap"];

			setIndentMediceneBatches();
			autoFillIndentBatch(indentDetail["medicine_id"],itemDetails,indentDetail);


		}

	}
	function setIndentMediceneBatches(){
		var issuedItemOfSelectedStore = itemsListJSON[document.patientIssueReturnForm.dept_to.value];
		if ( issuedItemOfSelectedStore == undefined ) return false;
		for(var i = 0;i<issuedItemOfSelectedStore.length;i++){
		 	var medId = issuedItemOfSelectedStore[i].medicine_id;
			gMedicineBatches[medId] = issuedItemsMedicineWiseMapJSON[medId];
		}
	}
	function getAutoFillIndents() {
		var storeId =document.getElementById("dept_to").value;
	for (var i=0;i<jStores.length;i++) {
			if (jStores[i].dept_id == storeId) {
				if (jStores[i].auto_fill_indents) return true;
				else return false;
			}
		}
	}

	/*
	 * Find if there is another entry of the same medicineId/Batch in the grid,
	 * given the batch object and an index that is ourselves (so that we don't give a duplicate
	 * error for ourselves
	 */
	function getDuplicateIndex(batch, selfIndex) {
		var numItems = getNumItems();
		var duplicateCount = 0;

		for (var i=1; i<=numItems; i++) {
			if (i == selfIndex)
				continue;

			var row = getRowObject(i);
			var itemMedicineId = getElementByName(row,'medicine_id').value;
			var itemBatchNo = getElementByName(row,'batch_no').value;

			if ((itemMedicineId == batch.medicine_id) && (itemBatchNo == batch.batch_no))
				return i;
		}
		return -1;
	}

	function getNumItems() {
		var itemDetailsTableObj = document.getElementById("indentItemListTab");
		return itemDetailsTableObj.rows.length - 2;	// one for header, one for template row
	}

	function getRowObject (id) {
		var itemListTable = document.getElementById("indentItemListTab");
		return itemListTable.rows[id];
	}

	function autoFillIndentBatch(medicineId,itemDetails,indentDetails){
		var remQty = indentDetails["qty"];
		var expiredQty = 0; var nearingExpiryQty = 0;
		var negativeQty = 0; var diffPkgQty = 0;
		var medicineBatches = gMedicineBatches[medicineId];
		var units = itemDetails["unit"];
		var medicineName = medicineBatches[0].medicine_name;
		var qtyUnit = indentDetails["qty_unit"];

		for (b=0; b<medicineBatches.length; b++) {

			var batch = medicineBatches[b];
			var avlblQty = 0;

			// if batch is already in grid, skip
		    var dupExists = getDuplicateIndex(batch, -1);
			if (dupExists != -1)
				continue;

			//avlblQty = batch.qty;//use only issue units
			avlblQty = getAvailableQuantity(batch);
			if (qtyUnit == 'P') {
				avlblQty = Math.floor(avlblQty/batch.issue_base_unit);
			}
			// check for expiry date for normal sales (ie, not estimate, and not negative stock)
			if ((avlblQty > 0)) {
				var daysToExpire = batch.exp_dt != null ? getDaysToExpire(batch.exp_dt) : expiryWarnDays+1;
				if (daysToExpire <= 0) {
					expiredQty += avlblQty;
					if (gAllowExpiredSale != 'Y') {
						continue;
					}
				} else if (daysToExpire <= expiryWarnDays) {
					nearingExpiryQty += avlblQty;
				}
			}

			var qtyForBatch = 0;
			if ((stockNegative != 'D') && (avlblQty <= 0 || (b == medicineBatches.length - 1))) {
				// if stock is allowed to be negative, use up all of remQty for the last batch
				// or for the first batch where it is already 0 or negative.
				qtyForBatch = remQty;
				negativeQty = qtyForBatch - avlblQty;
			} else {
				qtyForBatch = Math.min(remQty, avlblQty);
				qtyForBatch = qtyForBatch < 0 ? 0 : qtyForBatch;   // stock can be negative even otherwise.
				if (allowDecimalsForQty == 'Y') {
					// round it off to 2 decimal places to avoid float problems
					qtyForBatch = Math.round(qtyForBatch*100)/100;
				}
			}
			// add to grid
			if (qtyForBatch > 0)
				if (!empty(returnIndentItemsJSON) && Object.getOwnPropertyNames(returnIndentItemsJSON).length != 0) {
					var batchId = batch.item_batch_id;
					if (!empty(returnIndentItemsJSON[batchId])) {
						var dispenseStatus = returnIndentItemsJSON[batchId][0].dispense_status;
						if(dispenseStatus != 'C') {
							addItemsToTable(qtyForBatch,batch,indentDetails);
						} else {
							qtyForBatch = 0;
						}
					}
				} else
				addItemsToTable(qtyForBatch,batch,indentDetails);

			// if no more to be added finish up
			remQty = remQty - qtyForBatch;
			if (remQty == 0)
				break;
		}


		var msg = "";
		if (remQty > 0) {
			msg += getString("js.sales.issues.issuereturn.warning.insufficientquantityinstock");
			msg+= medicineName;
			msg += "': " ;
			msg+= remQty.toFixed(2);
			alert(msg);
			if ((expiredQty > 0) && !gAllowExpiredSale) {
				msg += " (" ;
				msg+= expiredQty;
				msg +=getString("js.sales.issues.issuereturn.itemsavailable.pastexpirydate");
				alert(msg);
			}
			if (diffPkgQty > 0) {
				msg += " (" ;
				msg+= diffPkgQty;
				msg +=getString("js.sales.issues.issuereturn.itemsavailable.differentpackagesize");
				alert(msg);
			}
			msg += "\n";
		}

		if (negativeQty > 0 && stockNegative != 'A') {
			msg += getString("js.sales.issues.issuereturn.warning.insufficientquantityinstock");
			msg += medicineName;
			msg+= "': ";
			msg+=negativeQty.toFixed(2);
			msg += getString("js.sales.issues.issuereturn.proceedingwithsalecause.stocktobecomenegative");
			msg+="\n";
			alert(msg);
		}

		if (nearingExpiryQty > 0) {
			msg += getString("js.sales.issues.issuereturn.warning.someitemsfor");
			msg += medicineName;
			msg +=getString("js.sales.issues.issuereturn.aresoontoexpire");
			msg+="\n";
			alert(msg);
		}

		if ((expiredQty > 0) && gAllowExpiredSale) {
			msg += getString("js.sales.issues.issuereturn.warning.someitemsfor");
			msg += medicineName;
			msg +=getString("js.sales.issues.issuereturn.aresoontoexpire");
			alert(msg);
		}
	}

	function getAvailableQuantity(batch) {
		var avlblQty =0;
		var batchId = batch.item_batch_id;
		if (!empty(returnIndentItemsJSON) && Object.getOwnPropertyNames(returnIndentItemsJSON).length != 0) {
			if (!empty(returnIndentItemsJSON[batchId])) {
				return returnIndentItemsJSON[batchId][0].qty_required - returnIndentItemsJSON[batchId][0].qty_received;
			}
		} else {
			if (!empty(batch)) {
				return batch.qty;
			}
		}
		return avlblQty;
	}


	function getDaysToExpire(expDt) {
		// expDt in ms is first of the month _after_ which it is going to expire. Convert it to
		// the 1st of next month, the actual date when it is considered expired
		// Example: expDt = Sep 09, we store it as 1-Sep-09, but it is considered expired only
		// on or after 1 Oct 09.
		var dateOfExpiry = new Date(expDt);

		var diff = daysDiff(getDatePart(getServerDate()), dateOfExpiry);
		return diff;
	}

	function validate(){
		if(document.getElementById("indentItemListTab").rows.length <= 2){
			showMessage("js.sales.issues.issuereturn.additemstoreturn");
			return false;
		}
		var billStatus = getBillStatus();
		if(billStatus == 'F'){
			showMessage("js.sales.issues.issuereturn.patientbillisfinalized.notreturnitem");
	 		return false;
		} else if(billStatus == '') {
			showMessage("js.sales.issues.storesuserissues.patientnothave.openunpaidbills.youcannotreturnbillableitem");
	 		return false;
		}
		if(billStatus == 'S'){
			showMessage("js.sales.issues.issuereturn.patientbillissettled.notreturnitem");
	   		return false;
		}
		if(billStatus == 'C'){
			showMessage("js.sales.issues.issuereturn.patientbillisclosed.notreturnitem");
	   		return false;
		}
		if (isSharedLogIn == 'Y') {
			loginDialog.show();
		} else {
			document.patientIssueReturnForm.submit();
			return true;
		}
	}

	function submitHandler() {
		document.getElementById('save').disabled = true;
		document.patientIssueReturnForm.authUser.value = document.getElementById('login_user').value;
		document.patientIssueReturnForm.submit();
	}

	function getBillStatus(){
		var billStatus = '';
		for ( var i =0;i<billsJSON.length;i++ ){
			if ( billsJSON[i].bill_no == document.patientIssueReturnForm.bill_no.value ){
				billStatus = billsJSON[i].status;
				break;
			}
		}

		return billStatus;
	}

function checkDups(){
	var itemListTable = document.getElementById("indentItemListTab");
	var numItems = itemListTable.rows.length  - 1;

	for (var k=1; k < numItems; k++) {
		if (k == editRowIndex) {
			// currently being edited row, skip this.
			continue;
		}
		var rowObj = itemListTable.rows[k];

		if (getElementByName(rowObj,'medicine_name').value == document.patientIssueReturnForm.items.value
				&& getElementByName(rowObj,'batch_no').value == document.patientIssueReturnForm.eBatch.value) {
			showMessage("js.sales.issues.duplicateentry");
			return false;
		}
	}
	return true;

}

function getReport(){
		//window.open(cpath+'/DirectReport.do?report=StoreStockPatientReturns&returnNo='+returnNo);
	window.open(cpath+'/stores/StockPatientIssueReturnPrint.do?_method=printPatientIssuesReturn&returnNo='+returnNo);

}
function getBillIsTpa(billNo){
	var visitBills = billsJSON;
	var isTpa = false;
	for(var i = 0;i < visitBills.length ; i++){
		isTpa = visitBills[i].bill_no == billNo && visitBills[i].is_tpa;
		if ( isTpa ) break;
	}
	return isTpa;
}


function setIssueUnits(batchNo){
	var itemDetailOFBatch = itemDetailsMap[batchNo];
	document.getElementById("eIssueUnits").innerHTML = itemDetailOFBatch.issue_units_lbl;
}

function getItemBarCodeDetails(barCode){
	if ( empty(barCode) || itemNamesArray == undefined )
		return false;
	for( var i = 0,j = itemNamesArray.length;i<j;i+= 1 ){
		var itemDetails = itemNamesArray[i];
		if ( itemDetails.item_barcode_id == barCode ) {
			var itmName = itemDetails.medicine_name;
			var itemAutoCompleteDetails = new Array();
			itemAutoCompleteDetails.push(itemDetails.medicine_name);
			itemAutoCompleteDetails.push(itemDetails.medicine_id);
	     	var elNewItem = matches(itmName, oItemAutoComp,itemAutoCompleteDetails);
	     	document.patientIssueReturnForm.items.value = itmName;
	     	oItemAutoComp._bItemSelected = true;
	     	setDetails(itmName,itemDetails.medicine_id);
//			oItemAutoComp._selectItem(elNewItem);
	     	return true;
		}
	}
}


function matches(mName, autocomplete,itemDetails) {
	var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
   	elListItem._sResultMatch = mName;
    elListItem._oResultData = itemDetails;
	return elListItem;
}

function calculateClaimAmount(planId,amt,visitType,categoryId, firstOfCategory,discount){
		var claimAmount = 0;
		var ajaxReqObject = newXMLHttpRequest();
		url = cpath + "/patientissues/getclaimamount.json?plan_id="+planId+"&amount="+amt+"&visit_type="+visitType+"&category_id="+categoryId+"&foc="+firstOfCategory+"&discount="+discount;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					var response;
					eval('response = '+ajaxReqObject.responseText);
					claimAmount = response.claimAmt;
			}
		}
		return claimAmount;
}

function populatePackages() {
	if (packages) activePackages = packages;
	document.getElementById("activePackage").length=0;
	addOption(document.getElementById("activePackage"), "--- Select ---", "");
	for(var i=0;i<activePackages.length;i++) {
		addOption(document.getElementById("activePackage"), activePackages[i].package_name, activePackages[i].pat_package_id);
	}
}

function addOption(selectbox,text,value ) {
	var optn = document.createElement("OPTION");
	optn.text = text;
	optn.value = value;
	selectbox.options.add(optn);
}

function onChangePackage(patPackageId) {
	table = document.getElementById('packageInfo');
	row = table.rows[0];
	isStaticPackage = false;
	clearIssuedItems();
	document.getElementById("bill_no").length=0;

	if (!patPackageId) {
		getElementByName(row, "pkg_charge_id_ref").value= '';
		document.getElementById("package_id").value = '';
		for(billNo of billsJSON) {
			addOption(document.getElementById("bill_no"), billNo.bill_no, billNo.bill_no);
		}
		initItemsAutoComplete();
		return;
	}

	for(var i=0;i<packages.length;i++) {
		if (patPackageId == packages[i].pat_package_id) {
			addOption(document.getElementById("bill_no"), packages[i].bill_no, packages[i].bill_no);
			getElementByName(row, "pkg_charge_id_ref").value = packages[i].charge_ref;
			document.getElementById("package_id").value = packages[i].package_id;
			isStaticPackage = true;
			break;
		}
	}
	getPackageDetails(patPackageId);

}


function clearIssuedItems() {
	var table = document.getElementById('indentInfo');
	var detailstable = document.getElementById('indentItemListTab');

	var numItems = table.rows.length;
	for (var i=1; i<numItems-1; i++) {		// delete old indent
		table.deleteRow(1);
	}

	var numDetails = detailstable.rows.length;
	for (var j=1; j<numDetails-1; j++) {
		detailstable.deleteRow(1);
	}
	showIndentFields('none');

}

function getPackageDetails(patPackageId) {
	var packageId = document.getElementById('package_id').value;
	var visitId = document.getElementsByName('returned_by')[0].value
    var reqObj = new XMLHttpRequest();

	var url = cpath+'/stores/StockPatientReturn.do?_method=getPkgIssuedItems&visit_id='
	+ visitId +'&package_id='+packageId+'&patPkgId='+patPackageId;

	ajaxGETRequest(reqObj, pkgItemDetailsHandler, url.toString(), false);

}

function pkgItemDetailsHandler(responseText) {
	var pkgDetails = JSON.parse(responseText);
	pkgItemsAutoComplete(pkgDetails);
}

function pkgItemsAutoComplete(pkgDetails){
	if (oItemAutoComp != undefined) {
		oItemAutoComp.destroy();
	}

	itemNamesArray = pkgDetails.pkgItemsList[document.patientIssueReturnForm.dept_to.value];
	var storeWiseItemsArray = [];
	storeWiseItemsArray = itemNamesArray;
	var medNamesUniqueArray = getUniqueMedicineNames(storeWiseItemsArray);
	var dataSource = new YAHOO.widget.DS_JSArray(medNamesUniqueArray);
	dataSource.responseSchema = {
		resultsList : "result",
		fields : [ {key : "cust_item_code_with_name"},
					{key : "medicine_name"},
					{key : "medicine_id"},
					{key : "issue_uom"},
					{key : "package_uom"},
					{key : "issue_base_unit"} ]
		};

	oItemAutoComp = new YAHOO.widget.AutoComplete('items','item_dropdown', dataSource);
	oItemAutoComp.prehightlightClassName = "yui-ac-prehighlight";
	oItemAutoComp.typeAhead = false;
	oItemAutoComp.useShadow = false;
	oItemAutoComp.allowBrowserAutocomplete = false;
	oItemAutoComp.minQueryLength = 0;
	oItemAutoComp.forceSelection = true;
	oItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oItemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oItemAutoComp._bItemSelected = true;
	oItemAutoComp.itemSelectEvent.subscribe(onSelectItem);

	oItemAutoComp.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get('items').value;
		if(sInputValue.length === 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		} });
	oItemAutoComp.itemSelectEvent.subscribe(getIssueReturnsItemDetails);
}
