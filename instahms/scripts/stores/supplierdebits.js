var oAutoComp;
var itemNamesArray = '';
var dialogOpened=0;
var rowEdit = 'N';
var gDefaultOnloadStore=0;

function initSupplierAutoComplete() {
	var supplierNames = [];
	var j=0;
	if(centerId == 0) {
		var dataSource = new YAHOO.widget.DS_JSArray(jAllSuppliers1);
	} else {    			
        var dataSource = new YAHOO.widget.DS_JSArray(jCenterSuppliers);
	}
	YAHOO.example.ACJSAddArray = new function() {
		dataSource.responseSchema = {
	    		resultsList : "result",
	    		fields : [  {key : "SUPPLIER_NAME_WITH_CITY"}, {key : "SUPPLIER_CODE"}, {key : "SUPPLIER_NAME"} ]
	    	};
		oAutoComp = new YAHOO.widget.AutoComplete('supplierName', 'suppliername_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 10;
		oAutoComp.allowBrowserAutocomplete = true;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
		oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		//oAutoComp.textboxChangeEvent.subscribe(onSelectSupplier);
		oAutoComp.itemSelectEvent.subscribe(onSelectSupplier);
	}
}

function onSelectSupplier(type,args) {
	if(fromedit != 'Y')
		clearGrid();
	var suppId = '';
	var supplierAddress = '';
	var selSupplierName = '';
	if ( args && args[2][2]){
		selSupplierName = args[2][2];
		document.supplierdebitform.supplierName.value = selSupplierName;
	} else {
		selSupplierName = document.supplierdebitform.supplierName.value;
	}
	if ( selSupplierName == '')
		document.getElementById('suppAddId').textContent = '';
	else {
		for ( var i=0; i<jAllSuppliers1.length; i++){
			if (selSupplierName == jAllSuppliers1[i].SUPPLIER_NAME){
				suppId = jAllSuppliers1[i].SUPPLIER_CODE;
				supplierAddress = jAllSuppliers1[i].SUPPLIER_ADDRESS;
				break;
			}
		}
	}
	var supplier = filterList(jAllSuppliers1, "SUPPLIER_CODE", suppId);
	document.supplierdebitform.supplier_code.value = suppId;
	//document.getElementById('suppAddId').textContent = supplier[0].SUPPLIER_ADDRESS;
	setNodeText(document.getElementById('suppAddId').parentNode, supplier[0].SUPPLIER_ADDRESS, 60, supplier[0].SUPPLIER_ADDRESS);

	medNamesAutoComplete();
}

function selectStore(){
	if (fromedit == 'Y') {
		document.getElementById('itemlvl').disabled = true;
		if (debitReturnType == 'O') document.getElementById('othersreason').readOnly = false;
		else document.getElementById('othersreason').readOnly = true;
		setSelectedIndex(document.supplierdebitform.returnType,debitReturnType);
		document.getElementById('saveStk').value = "Update Debit Note";
		var idx = document.getElementById("status").selectedIndex;
		if (document.getElementById("status").options[idx].value != 'O'){
			document.getElementById('itemleveldisc').readOnly = true;
			if (fromedit == '') document.getElementById('plusItem').disabled = true;
		} else{
			medNamesAutoComplete();
			if (fromedit == '') document.getElementById('plusItem').disabled = false;
		}
		document.supplierdebitform.store.disabled = true;
		if (debitReturnType == 'C' ) {
			document.supplierdebitform.returnType.disabled = true;
			document.supplierdebitform.discType.disabled = true;
		}

		initSupplierAutoComplete();
		var elNewItem = matches(selsupp, oAutoComp);
		oAutoComp._bItemSelected = true;
		onSelectSupplier();
//		oAutoComp._selectItem(elNewItem);

		document.getElementById('suppliername_dropdown').style.zIndex = -1000;

	} else {
		document.getElementById('saveStk').value = "Raise Debit Note";
		document.getElementById('itemleveldisc').readOnly = false;
		if (debitReturnType == 'O') document.getElementById('othersreason').readOnly = false;
		else document.getElementById('othersreason').readOnly = true;
	    //initMedicineAutoComplete();
	    medNamesAutoComplete();
	    if (fromedit == '') document.getElementById('plusItem').disabled = false;
	    document.supplierdebitform.store.disabled = false;
	    initSupplierAutoComplete();
	}
}
function init () {
	selectStore();
	initItemGroupDialog();
	addGroupMedDetails();
	checkstoreallocation();
	initPurchaseDetailsDialog(document.supplierdebitform.return_billedQty);
	document.getElementById("dialogId").value = '';
	if (fromedit == 'Y') document.supplierdebitform.status.focus();
	else document.supplierdebitform.supplierName.focus();
//	if ( prefVAT == 'Y' ){
//		document.getElementById("vat").checked = true;
//	}
	gDefaultOnloadStore = document.getElementById('store').value;
	if (isReturnAgainstGrn && fromedit != 'Y') {
		document.getElementById('return_against_grn').checked = true;
		document.supplierdebitform.grn_no.disabled = false;
		document.supplierdebitform.grn_no.focus();
		document.supplierdebitform.supplierName.disabled = true;
		document.supplierdebitform.returnAgainst.disabled = true;
		document.supplierdebitform.grnReturnH.value = "grnReturn";
	} else if (fromedit != 'Y')
		document.supplierdebitform.grn_no.disabled = true;
	
	resetTotals();
}
var oMedAutoComp;
function initMedicineAutoComplete(medArray) {
   	if (oMedAutoComp != undefined) {
		oMedAutoComp.destroy();
	}
	var dataSource = new YAHOO.widget.DS_JSArray(medArray);
			 dataSource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "cust_item_code_with_name"}, {key : "medicine_name"}, {key : "medicine_id"} ]
			 };
	oMedAutoComp = new YAHOO.widget.AutoComplete('medicine', 'medicine_dropdown', dataSource);

	oMedAutoComp.maxResultsDisplayed = 50;
	oMedAutoComp.allowBrowserAutocomplete = false;
	oMedAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oMedAutoComp.typeAhead = false;
	oMedAutoComp.useShadow = false;
	oMedAutoComp.minQueryLength = 0;
	oMedAutoComp.forceSelection = true;
	oMedAutoComp.animVert = false;
	oMedAutoComp._onTextboxFocus(null, oMedAutoComp);

	oMedAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oMedAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oMedAutoComp.itemSelectEvent.subscribe(onSelectMedicine);
}


function onSelectMedicine(type,args) {
	var medicineName = '';
	if ( args == undefined ){//from barcode
		medicineName = document.supplierdebitform.medicine.value;
	} else {
		medicineName = args[2][1];
	}
    document.supplierdebitform.medicine.value = medicineName;
	
	var storeId=document.supplierdebitform.store.value;
	var supp = document.supplierdebitform.supplierName.value;
	var grnNo = document.supplierdebitform.grn_no.value;
	if (medicineName == "") {
		resetMedicineDetails();
	} else  {
		resetOnlyRates();
		var ajaxReqObject = newXMLHttpRequest();
		var url="StoresSupplierReturnslist.do?_method=getStockJSON&medicineName="+encodeURIComponent(medicineName)+"&storeId="+storeId+"&supp="+encodeURIComponent(supp)+"&saleType=supReturn";
		if (grnNo != "" && grnNo != null)
			url = url+"&grnNo="+encodeURIComponent(grnNo);
		getResponseHandlerText(ajaxReqObject, handleMedicineStockResponse, url);
	}
}
/*
 * Response handler for the ajax call to retrieve medicine details like mfr and batches
 */
var gExpDate;		// store the current exp in a global
var gStock;	        // store the latest retrieved stock in a global
var gMrp;            // store the latest mrp  in a global
var gCurrentStock;    // store the latest currentstock  in a global
var gCurrentBonusStock;
var gGrnStock;
var flag=false;

function handleMedicineStockResponse(responseText) {
	 if (responseText==null) return;
	if (responseText=="") return;

	flag = false;
    eval("gStock = " + responseText);
   if (gStock.length > 0) {


		var batchSel = document.supplierdebitform.batch;
		var selBatchIndex = document.supplierdebitform.batch.value;
		document.supplierdebitform.medicineId.value = gStock[0].medicineId;
		document.supplierdebitform.tax_type.value = gStock[0].taxType;
		var index = 0;
		if (gStock.length > 1) {
			batchSel.length = gStock.length + 1;
			batchSel.options[index].text = "..Select Batch..";
			batchSel.options[index].value = "";
			index++;
		} else {
		    batchSel.length = gStock.length + 1;
		    batchSel.options[index].text = "..Select Batch..";
			batchSel.options[index].value = "";
			index++;
			flag=true;


		}

		for (var i=0; i<gStock.length; i++){
			var item = gStock[i];
			batchSel.options[index].text = item.batchNo ;
			batchSel.options[index].value = item.itemBatchId;
			if ((null != selBatchIndex) && (selBatchIndex != '') && (selBatchIndex == item.batchNo)){
				batchSel.selectedIndex = index;
			}
			index++;
		}

		var dialog = document.getElementById("dialogId").value;
		if (dialog != '') {
			document.supplierdebitform.batch.value = document.getElementById("item_batch_id"+dialog).value ;
			displayMedicineDetails(null,null);
		//	document.getElementById('item_unit').textContent = qtyUnitSelection() == 'I' ? item.issueUnits : item.packageUOM;
		}
		if (flag) {
			batchSel.selectedIndex = 1;
			getBatchDetails(batchSel.value, true);
			displayMedicineDetails(null,null);
		}
		// success: move to the next selectable item
		if (gStock.length > 1) {
			document.supplierdebitform.batch.focus();
		} else {
			document.supplierdebitform.return_billedQty.focus();
		}
		document.supplierdebitform.barCodeId.value = item.itemBarcode;

	} else {
		alert("Error retrieving stock details for item");
		resetMedicineDetails();
	}
	if (dialogOpened > 0){
		setDialogValues();
		dialogOpened = 0;
	}
}

function getBatchDetails(batchNo, isDetailsExist) {
	var medicineName = document.supplierdebitform.medicine.value;
	var storeId=document.supplierdebitform.store.value;
	var supp = document.supplierdebitform.supplierName.value;
	var grnNo = document.supplierdebitform.grn_no.value;
	
	if(batchNo && batchNo!= 'undefined' && batchNo != null && batchNo != 'null') {
		var taxDetails = getExistGrnTaxDetails(medicineName, storeId, supp, grnNo, batchNo);
	}
	if(!isDetailsExist || isDetailsExist == 'false')
		displayMedicineDetails(null, batchNo);
}

function displayMedicineDetails(paramtaxType,batchNo){
    var label1 = document.getElementById('expdate');
	var origrate = document.getElementById('rate');
	var label3 = document.getElementById('currentstock');
	var bonusQtylbl = document.getElementById('currentbonusstock');
	var discount = document.getElementById('discper');
	var schemediscount = document.getElementById('schemediscper');
	var revrate = document.getElementById('rev_rate');
	var revdisc = document.getElementById('rev_discper');
	var revschemedisc = document.getElementById('rev_schemediscper');
	var sBatchNo=document.supplierdebitform.batch.options[document.supplierdebitform.batch.selectedIndex > 0 ? document.supplierdebitform.batch.selectedIndex : 0].text;
	var selectedBatchId = document.supplierdebitform.batch.value;
	//var labelCED = null;
	//var labelCEDAmt = null;
	var medicineId = document.supplierdebitform.medicineId.value;
	taxType = (paramtaxType == null ? document.supplierdebitform.tax_type.value : paramtaxType);
	var lbltaxrate = document.getElementById("lbltax_rate");
	/*if (prefCed == 'Y'){
		labelCED = document.getElementById("cedamt");
	}*/
	billedQty = parseFloat(document.supplierdebitform.return_billedQty.value);
	bonusQty = parseFloat(document.supplierdebitform.return_bonusQty.value);
    //document.supplierdebitform.returnQty.focus();
    for (var i=0; i<gStock.length; i++){
   		var item = gStock[i];
   		/*if ( taxNotApplicable ){
   			item.taxPercent = 0;
   		}*/
    	if( selectedBatchId == item.itemBatchId ){
   			if (label1) {
       			if(item.expDt != null) {
					label1.innerHTML = formatExpiry(item.expDt);
					gExpDate = new Date(item.expDt);
				} else {
					label1.innerHTML = "";
					gExpDate = null;
				}
    		}
       		var selQty = qtyUnitSelection ();
       		var dialogQty = billedQty+bonusQty;
       		if ( dialogQty == 0 ) {
       			dialogQty = 1;
       		}
       		
       		gCurrentBonusStock = (selQty == 'I') ? item.bonusQty : parseFloat(item.bonusQty/item.packageUnit).toFixed(decDigits);
			gCurrentStock = (selQty == 'I') ? item.qty-item.bonusQty : parseFloat((item.qty-item.bonusQty)/item.packageUnit).toFixed(decDigits);
			gGrnStock = (selQty == 'I') ? item.grn_qty :parseFloat(item.grn_qty/item.packageUnit).toFixed(decDigits) ;
			if ( paramtaxType == null ){
				taxType = item.taxType;
				document.supplierdebitform.tax_type.value = item.taxType;
			}
				
	        if (paramtaxType != null || batchNo != null || (origrate && (origrate.value == '' || origrate.value == 0))){
	        	
	        	if (selQty == 'I') {
					
					/*if (null != labelCED){
	        			labelCED.innerHTML = parseFloat((item.cedTaxAmt/item.packageUnit)).toFixed(decDigits);
	        		}*/
					
					if (taxType == 'CB' || taxType == 'C') {
						var numerator = (item.package_cp-(item.cedTaxAmt/(gCurrentStock/item.packageUnit))) / item.packageUnit  ;
						var denominator = 1 + (item.taxPercent /100);
						origrate.value = (gCurrentStock <= 0 ? 0 : parseFloat((numerator / denominator)).toFixed(decDigits));
						revrate.value = (gCurrentStock <= 0 ? 0 : parseFloat((numerator / denominator)).toFixed(decDigits));

					}else{
						taxamount = ((item.mrp) - (item.packageSp))/ item.packageUnit;
						origrate.value = ( gGrnStock <= 0 ? 0 : parseFloat((((item.package_cp-(item.cedTaxAmt/(gGrnStock/item.packageUnit)))/item.packageUnit) - taxamount)).toFixed(decDigits));
						revrate.value = ( gGrnStock <= 0 ? 0 : parseFloat((((item.package_cp-((item.cedTaxAmt/(gGrnStock/item.packageUnit))))/item.packageUnit) - taxamount)).toFixed(decDigits));
					}
				} else{
					/*if (null != labelCED){
	        			labelCED.innerHTML = item.cedTaxAmt*(dialogQty);
	        		}*/
					if (taxType == 'CB' || taxType == 'C') {
						/** Need to get original rate by removing the added vat and ced(if any) from the package_cp*/
						var numerator = (item.package_cp) ;
						var denominator = 1 + (item.taxPercent /100);
						origrate.value = ( gGrnStock <= 0 ? 0 : parseFloat((numerator / denominator)  - (item.cedTaxAmt/ gGrnStock)).toFixed(decDigits));
						revrate.value = ( gGrnStock <= 0 ? 0 : parseFloat((numerator / denominator) - (item.cedTaxAmt/ gGrnStock)).toFixed(decDigits));
					}else{
						taxamount = parseFloat((item.mrp) - (item.packageSp)).toFixed(decDigits);
						origrate.value = ( gGrnStock <= 0 ? 0 : parseFloat(((item.package_cp) - taxamount - (item.cedTaxAmt/ gGrnStock))).toFixed(decDigits));
						revrate.value = ( gGrnStock <= 0 ? 0 :parseFloat(((item.package_cp) - taxamount - (item.cedTaxAmt/ gGrnStock))).toFixed(decDigits));
					}
				}
			}
	        
	        /*if ( prefCed == 'Y' ){
	        	recalcDialogCedAmt(item.cedTaxAmt,item.packageUnit,dialogQty);
	        }*/
			gMrp = (selQty == 'I') ? parseFloat(item.mrp/item.packageUnit).toFixed(decDigits) : item.mrp;
			setFieldValue('mrp', gMrp);

			if (bonusQtylbl) {
				if (selQty == 'I') {
					bonusQtylbl.innerHTML = item.bonusQty;
				} else {
					bonusQtylbl.innerHTML = parseFloat(item.bonusQty/item.packageUnit).toFixed(decDigits);
				}
			}
			
			if (label3) {
				if (selQty == 'I') label3.innerHTML = item.qty-item.bonusQty;
				else label3.innerHTML = parseFloat((item.qty-item.bonusQty)/item.packageUnit).toFixed(decDigits);
			}
			setElementText('UOMDesc', item.packageUOM);
			//if ( prefVAT == 'Y' ){
				lbltaxrate.innerHTML = item.taxPercent;
			//}
			document.getElementById('item_unit').textContent = selQty == 'I' ? item.issueUnits : item.packageUnit;
			if(item.identification == 'S'){
				if(item.bonusQty == 1) {
					document.supplierdebitform.return_bonusQty.value = '1';
			  		document.supplierdebitform.return_bonusQty.readOnly = true;
			  		document.supplierdebitform.return_billedQty.value = '0';
			  		document.supplierdebitform.return_billedQty.readOnly = true;
				} else {
					document.supplierdebitform.return_billedQty.value = '1';
			  		document.supplierdebitform.return_billedQty.readOnly = true;
			  		document.supplierdebitform.return_bonusQty.value = '0';
			  		document.supplierdebitform.return_bonusQty.readOnly = true;
				}
				getTaxDetails(prepareTaxObj());
			}
		   	else {
		   		document.supplierdebitform.return_billedQty.readOnly = false;
		   	}
			document.supplierdebitform.item_code.value = item.itemcode;
			break;
		}
	}
}
function formatExpiry(dateMSecs) {
	var dateObj = new Date(dateMSecs);
	var dateStr = formatDate(dateObj, 'monyyyy', '-');
	return dateStr;
}
function resetMedicineDetails(){
	var label1 = document.getElementById('expdate');
	var label2 = document.getElementById('mrp');
	var label3 = document.getElementById('currentstock');
	if (label1)
	label1.innerHTML = "";
	if (label2)
	label2.innerHTML = "";
	if (label3)
	label3.innerHTML = "";
	document.supplierdebitform.batch.options[0].selected=true;
	document.supplierdebitform.batch.length=1;
	document.supplierdebitform.medicine.value = '';
	document.supplierdebitform.return_billedQty.value = 0;
	document.supplierdebitform.return_bonusQty.value = 0;
	document.supplierdebitform.rate.value="";
	document.supplierdebitform.discper.value="";
	document.supplierdebitform.rev_discper.value="";
	document.supplierdebitform.schemediscper.value="";
	document.supplierdebitform.rev_schemediscper.value="";
	document.supplierdebitform.rev_rate.value="";
	document.supplierdebitform.medicineId.value = '';
	document.supplierdebitform.barCodeId.value = '';
	document.getElementById('item_unit').textContent = '';
	if (prefBarCode == 'Y') document.supplierdebitform.barCodeId.focus();
	else document.supplierdebitform.medicine.focus();
}

function resetOnlyRates(){
	var label1 = document.getElementById('expdate');
	var label2 = document.getElementById('mrp');
	var label3 = document.getElementById('currentstock');
	if (label1)
	label1.innerHTML = "";
	if (label2)
	label2.innerHTML = "";
	if (label3)
	label3.innerHTML = "";
	document.supplierdebitform.batch.options[0].selected=true;
	document.supplierdebitform.batch.length=1;
	document.supplierdebitform.return_billedQty.value = 0;
	document.supplierdebitform.return_bonusQty.value = 0;
	document.supplierdebitform.rate.value="";
	document.supplierdebitform.discper.value="";
	document.supplierdebitform.rev_discper.value="";
	document.supplierdebitform.schemediscper.value="";
	document.supplierdebitform.rev_schemediscper.value="";
	document.supplierdebitform.rev_rate.value="";
	if (prefBarCode == 'Y') document.supplierdebitform.barCodeId.focus();
	else document.supplierdebitform.medicine.focus();
}
function changeStore(){
	clearGrid();
 	medNamesAutoComplete();

 }

 /*
 * Deleteing values from the grid
 */
function clearGrid() {
	var itemDetailsTableObj = document.getElementById("medList");
	if (itemDetailsTableObj.rows.length>2) {
		var len = document.getElementById("medList").rows.length;
		for ( var p=len-1;p>1;p--){
			document.getElementById("medList").deleteRow(p);
		}
	}

	document.getElementById("totDisc").value = formatAmountValue(0,false,decDigits);
	document.getElementById("totSchemeDisc").value = formatAmountValue(0,false,decDigits);
	document.getElementById("totVAT").value = formatAmountValue(0,false,decDigits);
	document.getElementById("totRevVAT").value = formatAmountValue(0,false,decDigits);
	document.getElementById("totAmount").value = formatAmountValue(0,false,decDigits);
	document.getElementById("totDisclabel").innerHTML = formatAmountValue(0,false,decDigits);
	document.getElementById("totSchemeDisclabel").innerHTML = formatAmountValue(0,false,decDigits);
	document.getElementById("totAmountlabel").innerHTML = formatAmountValue(0,false,decDigits);

	document.supplierdebitform.medicine.value="";
	document.supplierdebitform.barCodeId.value = '';
	document.supplierdebitform.batch.options[0].selected=true;
	document.supplierdebitform.batch.length=1;
	document.getElementById('expdate').innerHTML = "";
	document.supplierdebitform.return_billedQty.value="";
	document.supplierdebitform.return_bonusQty.value="";
	document.getElementById("dialogId").value = 1;

    var dialogId  = document.getElementById("dialogId").value;
    document.getElementById('medName'+dialogId).innerHTML = '';
    document.getElementById('itemcodelabel'+dialogId).innerHTML = '';

    document.getElementById('hmedId'+dialogId).value = '';
	document.getElementById('hmedName'+dialogId).value = '';
	document.getElementById('hbatchnolabel'+dialogId).innerHTML = '';
	document.getElementById('item_batch_id'+dialogId).value = '';
	document.getElementById('hbatchno'+dialogId).value = '';
	document.getElementById('hmrplabel'+dialogId).innerHTML = '';
	document.getElementById('hmrp'+dialogId).value = '';
	document.getElementById('hpkgszlabel'+dialogId).innerHTML = '';
	document.getElementById('hpkgsz'+dialogId).value = '';
	document.getElementById('hexpdtlabel'+dialogId).innerHTML='';
	document.getElementById('hexpdt'+dialogId).value='';
	document.getElementById('hactqty'+dialogId).value = '';
	document.getElementById('hretqtylabel'+dialogId).innerHTML = '';
	document.getElementById('itemUnits'+dialogId).innerHTML = '';
	document.getElementById('hretqty'+dialogId).value = '';
	document.getElementById('hadjmrp'+dialogId).value = '';
	document.getElementById('htaxtype'+dialogId).value = '';
	document.getElementById('hrate'+dialogId).value = '';
	document.getElementById('hratelabel'+dialogId).innerHTML = '';
	document.getElementById('hrevrate'+dialogId).value = '';
	document.getElementById('hrevratelabel'+dialogId).innerHTML = '';
	document.getElementById('hdiscper'+dialogId).value = '';
	document.getElementById('hdiscamt'+dialogId).value = '';
	document.getElementById('hdiscper'+dialogId).value = '';
	document.getElementById('hrevdisc'+dialogId).value = '';
	document.getElementById('hrevdisclabel'+dialogId).innerHTML = '';
	document.getElementById('hdiscamtlabel'+dialogId).innerHTML = '';
	document.getElementById('hschemediscper'+dialogId).value = '';
	document.getElementById('hschemediscamt'+dialogId).value = '';
	document.getElementById('hschemediscper'+dialogId).value = '';
	document.getElementById('hrevschemedisc'+dialogId).value = '';
	document.getElementById('hrevschemedisclabel'+dialogId).innerHTML = '';
	document.getElementById('hschemediscamtlabel'+dialogId).innerHTML = '';
	document.getElementById('htaxrate'+dialogId).value = '';
	document.getElementById('hrevtaxrate'+dialogId).value = '';
	document.getElementById('hitembarcode'+dialogId).value = '';
	//if (prefVAT == 'Y') {
		document.getElementById('htaxratelabel'+dialogId).innerHTML = '';
		document.getElementById('htaxamtlabel'+dialogId).innerHTML = '';
	//}
	document.getElementById('hcedamt'+dialogId).value = '0';
	document.getElementById('hvat'+dialogId).value = '';
	document.getElementById('hrevvat'+dialogId).value = '';
	document.getElementById('hitemidentification'+dialogId).value = '';
	document.getElementById('hamtlabel'+dialogId).innerHTML = '';
	document.getElementById('hamt'+dialogId).value = '';
	document.getElementById('hRecdAmtlabel'+dialogId).innerHTML = '';
	document.getElementById('hRecdAmt'+dialogId).value = '';
	document.getElementById("itemCheck"+dialogId).value = '';
	document.getElementById("editIcon"+dialogId).value = '';
	document.getElementById("itemCheck"+dialogId).innerHTML = '';
	document.getElementById("editIcon"+dialogId).innerHTML = '';
	document.getElementById("totRevVAT").value="";
	document.getElementById("totVAT").value="";
	document.getElementById("totRecAmountlabel").innerHTML = '';
	document.getElementById("totRecAmount").value=formatAmountValue(0,false,decDigits);
	document.getElementById("recDebitAmt").value=formatAmountValue(0,false,decDigits);
	document.getElementById("hTranType"+dialogId).value = '';
	document.getElementById("medList").rows[dialogId].style.display = 'none';
	/*var addImg = document.getElementById('add'+dialogId);
		addImg.setAttribute("src",popurl+'/icons/Add.png');
		var addButton = document.getElementById('addButton'+dialogId);
		addButton.setAttribute("accesskey","+");
		addButton.setAttribute("title","");*/
	resetTotals()
}

function onAddMedicine() {
	if(screenValidation()==false)
  	  return;
    if(stockValidation()==false)
      return;
     var dialogId = document.getElementById("dialogId").value;
     if (dialogId == ''){
		if (getTotRows() > 2) {
			dialogId = getTotRows()-1;
			document.getElementById("dialogId").value = dialogId;
		} else {
			dialogId = 1;
			document.getElementById("dialogId").value = dialogId;
		}
	} else {
		// checking wheather this is insert or update...
		if (rowEdit == 'N') {
			document.getElementById("dialogId").value = getTotRows()-1;
			dialogId = getTotRows()-1;
		}
	}
    if (document.getElementById("debitNo").value == ""){
     if(document.getElementById("medList").rows.length>2){
	     for(var i=1;i<document.getElementById("medList").rows.length-1;i++){
	     var currentbatchNo= gStock[document.supplierdebitform.batch.selectedIndex-1].batchNo;
	     var currentMedicineId=gStock[document.supplierdebitform.batch.selectedIndex-1].medicineId;
	      if (i != dialogId){
		     if((currentbatchNo==document.getElementById("hbatchno"+i).value)&&(currentMedicineId==document.getElementById("hmedId"+i).value)){
			     alert("Duplicate Entry");
			     return false;
			     break;

			  }
		  }

	  }
    }
   }
	if (document.supplierdebitform.batch.disabled == true){
		document.supplierdebitform.batch.disabled = false;
	}
	if (document.supplierdebitform.return_billedQty.disabled == true){
		document.supplierdebitform.return_billedQty.disabled = false;
	}
	if (document.getElementById("debitNo").value == ""){
		var batchIndex = document.supplierdebitform.batch.selectedIndex;
		if (document.supplierdebitform.batch.length > 1) {
			// there is also a ..Select.. option in this case
			batchIndex -= 1;
		}

		var batchDetails = findInList(gStock,"itemBatchId",document.supplierdebitform.batch.value)
		var medicineId = batchDetails.medicineId;
		document.getElementById("medicineId").value = batchDetails.medicineId;
		var batchNo = batchDetails.batchNo;
		var itemBatchId = batchDetails.itemBatchId;


		var expDt = gExpDate;
		var batch = document.supplierdebitform.batch.options[document.supplierdebitform.batch.selectedIndex].text;
		var medicineName = document.supplierdebitform.medicine.value;
		var itemcode = document.supplierdebitform.item_code.value;
		var selQty = qtyUnitSelection ();
		var mrp = (selQty == 'I') ? parseFloat(batchDetails.mrp/batchDetails.packageUnit).toFixed(decDigits): (batchDetails.mrp).toFixed(decDigits);
		var pkgsize = batchDetails.packageUnit;
		var manf = batchDetails.manfMnemonic;
		var adj_mrp_from_batch = (batchDetails.mrp/(1 + batchDetails.taxPercent/100)).toFixed(decDigits);
		var adjmrp = (selQty == 'I') ? parseFloat(adj_mrp_from_batch/batchDetails.packageUnit): adj_mrp_from_batch;
		var taxrate = batchDetails.taxPercent;
		var taxtype = document.supplierdebitform.tax_type.value;
		var cedAmt = 0;
		var itemUnits = (selQty == 'I') ? batchDetails.issueUnits : batchDetails.packageUOM;
		if ((null == cedAmt) || (cedAmt == "")){
			cedAmt = 0;
		}
		var itemIdentification = batchDetails.identification;
		var recdAmt = 0.00;
		/*if (prefVAT == 'Y') {
			if ( document.getElementById("cst").checked ) {
				if (!taxRateFormGRN) {
					taxrate = document.supplierdebitform.cstrate.value;
				} 
				taxtype = 'C';
			}
		}*/
		gCurrentStock = document.getElementById("currentstock").innerHTML;
		gCurrentBonusStock = document.getElementById("currentbonusstock").innerHTML;
		var expiryDate = new Date(batchDetails.expDt);
	} else {
		taxrate = document.getElementById("tax_rate").value
	}
	var discount = 0;
	var revDisc = 0;
	var schemediscount = 0;
	var schemerevDisc = 0;
	var costprice = document.supplierdebitform.rate.value;
	if ((document.supplierdebitform.discper.value != null) && (document.supplierdebitform.discper.value != "")){
		discount = document.supplierdebitform.discper.value;
	} else
		discount = 0;
	if ((document.supplierdebitform.rev_discper.value != null) && (document.supplierdebitform.rev_discper.value != "")){
		revDisc = document.supplierdebitform.rev_discper.value;
	} else if ((document.supplierdebitform.discper.value != null) && (document.supplierdebitform.discper.value != "")){
		revDisc = document.supplierdebitform.discper.value;
	} else
		revDisc = 0;

	if ((document.supplierdebitform.schemediscper.value != null) && (document.supplierdebitform.schemediscper.value != "")){
		schemediscount = document.supplierdebitform.schemediscper.value;
	} else
		schemediscount = 0;

	if ((document.supplierdebitform.rev_schemediscper.value != null) && (document.supplierdebitform.rev_schemediscper.value != "")){
		schemerevDisc = document.supplierdebitform.rev_schemediscper.value;
	} else if ((document.supplierdebitform.schemediscper.value != null) && (document.supplierdebitform.schemediscper.value != "")){
		schemerevDisc = document.supplierdebitform.schemediscper.value;
	} else
		schemerevDisc = 0;
	var revRate = document.supplierdebitform.rev_rate.value;

	var taxamount = 0;
	if(isNotNullText('lbltax_amt')) {
		taxamount = getFieldText('lbltax_amt');
	}
	var returnQty = document.supplierdebitform.return_billedQty.value;
	var returnBonusQty = document.supplierdebitform.return_bonusQty.value

	/* clear off fields on the dialog*/
	document.supplierdebitform.medicine.value="";
	//document.supplierdebitform.barCodeId.value = '';
	document.supplierdebitform.batch.options[0].selected=true;
	document.supplierdebitform.batch.length=1;
	document.getElementById('expdate').innerHTML = "";
	document.getElementById('rate').value = "";
	document.getElementById('mrp').value = "";
	document.getElementById('medicineId').value = "";
	document.getElementById('discper').value = "";
	document.getElementById('schemediscper').value = "";
	document.getElementById('rev_rate').value = "";
	document.getElementById('rev_discper').value = "";
	document.getElementById('rev_schemedisc').value = "";
	document.getElementById('rev_schemediscper').value = "";
	document.getElementById('rev_disc').value = "";
	document.getElementById('currentstock').innerHTML = "";
	document.getElementById('currentbonusstock').innerHTML = "";
	document.supplierdebitform.return_billedQty.value="";
	document.supplierdebitform.return_bonusQty.value="";
	document.supplierdebitform.recdAmt.value="";
	document.supplierdebitform.tax_type.options[0].selected=true;
	
	var taxSubGroupList = new Array();
	for(var i=0; i < groupListJSON.length; i++) {
		var p = groupListJSON[i].item_group_id;
		var taxGroup = {};
		if(isNotNullValue("taxrate_", p)){
			taxGroup['rate'] = getFieldValue("taxrate_", p);
		}
		if(isNotNullValue("taxamount_", p)){
			taxGroup['amount'] = getFieldValue("taxamount_", p);
		}
		if(isNotNullValue("taxsubgroupid_", p)){
			taxGroup['taxsubgroupid'] = getFieldValue("taxsubgroupid_", p);
			taxGroup['groupid'] = p;
			taxSubGroupList.push(taxGroup);
		}
	}
	if (document.getElementById("debitNo").value == ""){
		addToInnerHTML(medicineName,medicineId,manf,batchNo,
					itemBatchId,mrp,pkgsize,expDt,
					gCurrentStock,returnQty,returnBonusQty,adjmrp,taxtype,
					taxrate,taxamount,costprice,expiryDate,
					'','',itemIdentification, discount,
					revRate, revDisc, cedAmt,"NoGroup",
					itemUnits,gCurrentBonusStock,schemediscount,schemerevDisc,batchDetails.grn_cp,itemcode,taxSubGroupList);
		//getItemGroupDialog(eval(dialogId)+1);
	} else{
		updateInnerHTML(costprice,discount, revRate, revDisc,schemediscount,schemerevDisc,taxSubGroupList, taxrate);
	}
	clearTaxSubgroupsFields();
	
	var totRowsCount = getTotRows()-2;
	if (fromedit == 'Y') totRowsCount = getTotRows()-1;
	if (totRowsCount == dialogId) openAddDialog();
	else getItemGroupDialog(eval(dialogId)+1);
}
function getTotRows () {
	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length;
	return numRows;
}
function addToInnerHTML(medicineName,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,expDt,gCurrentStock,returnQty,
	returnBonusQty,adjmrp,taxtype,taxrate,taxamount,costprice,expiryDate,indentno,rejQty,itemIdentification,
	discount, revRate, revDisc, cedAmt, tranType, itemUnits,gCurrentBonusStock,schemediscount,schemerevDisc,grnCp,itemcode,taxSubGroupList){
	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length;
	var id = getTotRows() - 1;
	rowObj = itemListTable.rows[id];
	if(rowEdit == 'N') rowObj.style.display = '';

	var dialogId  = document.getElementById("dialogId").value;
	var revtaxamt = 0;
	var cell;
	var flag = '';
	var cedamt = 0.00;
	var revced = 0.00;
	if (medicineName != null){
		if (taxtype == 'CB' || taxtype == 'C') {
			//tax amount is the sum of ced tax and vat amount
			/*var cedamt = parseFloat(costprice  * cedper /100).toFixed(2);
			var revced = parseFloat(revRate  * cedper /100).toFixed(2);
			taxamount = (costprice-(((costprice + cedamt) * 100) / ( 100 + parseFloat(taxrate) )));
			revtaxamt = (revRate-(((revRate + revced) * 100) / ( 100 + parseFloat(taxrate) )));*/
			taxamount = 0.00;
			revtaxamt = 0.00;
			cedamt = 0.00;
			revced = 0.00;
			flag ='costp';
		} else {
			//taxamount = (adjmrp * taxrate)/100;
			//revtaxamt = (adjmrp * taxrate)/100;
			taxamount = 0.00;
			revtaxamt = 0.00;
			var cedamt = 0.00;
			var revced = 0.00;
			flag ='';
		}
		if ((flag == 'costp')){
			document.getElementById('medName'+dialogId).innerHTML = '<img class="flag" src= "'+popurl+'/images/yellow_flag.gif"/> <b><font color="#444444">'+medicineName+'</font></b>';
		} else{
			document.getElementById('medName'+dialogId).innerHTML = '<b><font color="#444444">'+medicineName+'</font></b>';
		}
		document.getElementById('itemcodelabel'+dialogId).innerHTML = itemcode;
		document.getElementById('hmedId'+dialogId).value = medicineId;
		document.getElementById('hmedName'+dialogId).value = medicineName;
		document.getElementById('hbatchnolabel'+dialogId).innerHTML = batchNo;
		document.getElementById('hbatchno'+dialogId).value = batchNo;
		document.getElementById('item_batch_id'+dialogId).value = itemBatchId;
		document.getElementById('hmrplabel'+dialogId).innerHTML = mrp;
		document.getElementById('hmrp'+dialogId).value = mrp;
		document.getElementById('hpkgszlabel'+dialogId).innerHTML = pkgsize;
		document.getElementById('hpkgsz'+dialogId).value = pkgsize;
		if(expDt == null) {
			document.getElementById('hexpdtlabel'+dialogId).innerHTML="";
			document.getElementById('hexpdt'+dialogId).value="";
		} else {
			document.getElementById('hexpdtlabel'+dialogId).innerHTML = formatDate(expDt, 'monyyyy', '-');;
			document.getElementById('hexpdt'+dialogId).value = formatDate(expDt);
		}
		document.getElementById('hactbonusqty'+dialogId).value = gCurrentBonusStock;
		document.getElementById('hactqty'+dialogId).value = gCurrentStock;
		document.getElementById('hretqtylabel'+dialogId).innerHTML = returnQty;
		document.getElementById('hretbonusqtylabel'+dialogId).innerHTML = returnBonusQty;
		document.getElementById('hretqty'+dialogId).value = returnQty;
		document.getElementById('hretbonusqty'+dialogId).value = returnBonusQty;
		document.getElementById('itemUnits'+dialogId).value = itemUnits;
		document.getElementById('hadjmrp'+dialogId).value = adjmrp;
		document.getElementById('htaxtype'+dialogId).value = taxtype;
		document.getElementById('hrate'+dialogId).value = parseFloat(costprice).toFixed(decDigits);
		document.getElementById('hgrnrate'+dialogId).value = parseFloat(qtyUnitSelection () == 'P' ? grnCp : grnCp/pkgsize).toFixed(decDigits);
		document.getElementById('hratelabel'+dialogId).innerHTML = parseFloat(costprice).toFixed(decDigits);
		document.getElementById('hdiscper'+dialogId).value = parseFloat(discount).toFixed(decDigits);
		document.getElementById('hdiscamt'+dialogId).value = 0;
		document.getElementById('hschemediscper'+dialogId).value = parseFloat(schemediscount).toFixed(decDigits);
		document.getElementById('hschemediscamt'+dialogId).value = 0;
		document.getElementById('htaxrate'+dialogId).value = taxrate;
		document.getElementById('hrevtaxrate'+dialogId).value = taxrate;
		document.getElementById('hcedamt'+dialogId).value = 0;
		//if (prefVAT == 'Y') {
		document.getElementById('htaxratelabel'+dialogId).innerHTML = taxrate;
		document.getElementById('htaxamtlabel'+dialogId).innerHTML = parseFloat(taxamount).toFixed(decDigits);
		//}
		document.getElementById('hvat'+dialogId).value = taxamount;
		document.getElementById('hitemidentification'+dialogId).value = itemIdentification

		document.getElementById('hamtlabel'+dialogId).innerHTML = 0;//parseFloat(adjmrp * (0-returnQty) + (0-taxamount) - (0-discount));
		document.getElementById('hamt'+dialogId).value = 0;//parseFloat(adjmrp * (0-returnQty) + (0-taxamount) - (0-discount));
		document.getElementById('hrevrate'+dialogId).value = parseFloat(revRate).toFixed(decDigits);
		document.getElementById('hrevratelabel'+dialogId).innerHTML = parseFloat(revRate).toFixed(decDigits);
		document.getElementById('hitembarcode'+dialogId).value = document.supplierdebitform.barCodeId.value;

		document.getElementById('itemUnits'+dialogId).innerHTML = itemUnits;
		document.getElementById('pkg_uom'+dialogId).value = itemUnits;

		if (revDisc != "" && revDisc != 0){
			document.getElementById('hrevdiscper'+dialogId).value = parseFloat(revDisc).toFixed(decDigits);

		} else{
			document.getElementById('hrevdiscper'+dialogId).value = 0;
		}

		if (schemerevDisc != "" && schemerevDisc != 0){
			document.getElementById('hrevschemediscper'+dialogId).value = parseFloat(schemerevDisc).toFixed(decDigits);

		} else{
			document.getElementById('hrevschemediscper'+dialogId).value = 0;
		}
		if (indentno != ""){
			var hrejQty = makeHidden('hrejQty','hrejQty'+dialogId,"");
			var hindentno = makeHidden('hindentno','hindentno'+dialogId,"");
			var tdObj = document.getElementById("hamt"+dialogId);
			tdObj.appendChild(hrejQty);
			tdObj.appendChild(hindentno);
			document.getElementById('hrejQty'+dialogId).value = rejQty;
			document.getElementById('hindentno'+dialogId).value = indentno;

		}
		var taxAmt = 0;
		document.getElementById("hTranType"+dialogId).value = tranType;
		for(var i=0; i < taxSubGroupList.length; i++) {
			var groupId = taxSubGroupList[i].groupid;
			if(document.getElementById(dialogId+'taxrate'+groupId))
				document.getElementById(dialogId+'taxrate'+groupId).value = taxSubGroupList[i].rate;
			if(document.getElementById(dialogId+'taxamount'+groupId))
				document.getElementById(dialogId+'taxamount'+groupId).value = taxSubGroupList[i].amount;
			if(document.getElementById(dialogId+'taxsubgroupid'+groupId))
				document.getElementById(dialogId+'taxsubgroupid'+groupId).value = taxSubGroupList[i].taxsubgroupid;
			if(taxSubGroupList[i] && isNotNullObj(taxSubGroupList[i].amount)) {
				if(!isNaN(parseFloat(taxSubGroupList[i].amount))) {
					taxAmt = taxAmt+parseFloat(taxSubGroupList[i].amount);
				}
			}
		}
		document.getElementById("hrevvat"+dialogId).value = parseFloat(taxAmt).toFixed(decDigits);

		var delButton = document.getElementById("itemCheck"+dialogId);
		if(delButton.firstChild == null){
			var imgbutton = makeImageButton('imgDelete','imgDelete'+dialogId,'deleteIcon',popurl+'/icons/delete.gif');
			imgbutton.setAttribute('onclick','deleteItem(this,'+dialogId+')');
			delButton.appendChild(imgbutton);
		}
		var editImg = document.getElementById('editIcon'+dialogId);
		if(editImg.firstChild == null){
			var imgbutton = makeImageButton('editButton','editButton'+dialogId,'editIcon',popurl+'/icons/Edit.png');
			imgbutton.setAttribute('onclick','getItemGroupDialog('+dialogId+')');
			editImg.appendChild(imgbutton);
		}
		//editImg.setAttribute("src",popurl+'/icons/Edit.png');
		//var editButton = document.getElementById('addButton'+dialogId);
		//editButton.setAttribute("accesskey","");
		//editButton.setAttribute("title","");
		calc (dialogId);
	}
	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
	if (nextrow == null){
		id = parseInt(dialogId)+1;
		var row = itemListTable.insertRow(id);
		numRows = itemListTable.rows.length;
		var tabRow = "tableRow" + id;
		row.id = tabRow;
		row.style.display = 'none';



		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		if (flag == '') cell.innerHTML = '<label id="medName'+id+'"></label>';
		else  cell.innerHTML = '<label id="medName'+id+'"></label>';
		
		cell = row.insertCell(-1);
		cell.innerHTML = '<label id="itemcodelabel'+id+'"></label>';

		cell = row.insertCell(-1);
		cell.setAttribute("style","padding-left: 0.5em;");
		cell.innerHTML = '<input type="hidden" name="hpkgsz" id="hpkgsz'+id+'" value="">'+
						'<label id="hpkgszlabel'+id+'"></label>';

		cell = row.insertCell(-1);
		cell.setAttribute("style","padding-left: 0.5em;");
		var cellContent = '<label id="hbatchnolabel'+id+'"></label>' +
			'<input type="hidden" name="hbatchno" id="hbatchno'+id+'" value="">'+
			'<input type="hidden" name="item_batch_id" id="item_batch_id'+id+'" value="">'+
			'<input type="hidden" name="hmedId" id="hmedId'+id+'" value=""> <input type="hidden" name="hmedName" id="hmedName'+id+'" value="">'+
			'<input type="hidden" name="hitemidentification" id="hitemidentification'+id+'" value="">'+
			'<input type="hidden" name="hadjmrp" id="hadjmrp'+id+'" value="">'+
			'<input type="hidden" name="hitembarcode" id="hitembarcode'+id+'" value="">'+
			'<input type="hidden" name="htaxtype" id="htaxtype'+id+'" value="">';
		
		var additionalCellContent = '';
		for(var j=0; j < groupListJSON.length; j++) {
			var p =groupListJSON[j].item_group_id;
			additionalCellContent = additionalCellContent + '<input type="hidden" name="taxname'+p+'" id="'+id+'taxname'+p+'" value="0" />'+
			'<input type="hidden" name="taxrate'+p+'" id="'+id+'taxrate'+p+'" value="0" />'+
			'<input type="hidden" name="taxamount'+p+'" id="'+id+'taxamount'+p+'" value="0" />'+
			'<input type="hidden" name="taxsubgroupid'+p+'" id="'+id+'taxsubgroupid'+p+'" value="0" />';
		}
		
		cell.innerHTML = cellContent + additionalCellContent;

		cell = row.insertCell(-1);
		cell.setAttribute("style","padding-left: 0.5em;");
		cell.innerHTML = '<label id="hexpdtlabel'+id+'"></label>'+
						'<input type="hidden" name="hexpdt" id="hexpdt'+id+'" value="">';
		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '<label id="hmrplabel'+id+'"></label>' +
		    '<input type="hidden" name="hmrp" id="hmrp'+id+'" value="">';


		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '<label id="hretqtylabel'+id+'"></label>'+
		'<input type="hidden" name="hactqty" id="hactqty'+id+'" value="">'+
						'<input type="hidden" name="hretqty" id="hretqty'+id+'" value="">';

		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '<label id="hretbonusqtylabel'+id+'"></label>'+
		'<input type="hidden" name="hactbonusqty" id="hactbonusqty'+id+'" value="">'+
				'<input type="hidden" name="hretbonusqty" id="hretbonusqty'+id+'" value="">';

		cell = row.insertCell(-1);
		cell.innerHTML = '<label id="itemUnits'+id+'"></label>'+
		'<input type="hidden" name="pkg_uom" id="pkg_uom'+id+'" value="">';


		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '<label id="hratelabel'+id+'"></label>'+
					     '<input type="hidden" name="hrate" id="hrate'+id+'" value="">' +
					     '<input type="hidden" name="hgrnrate" id="hgrnrate'+id+'" value="">';


		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = ' <label id="hdiscamtlabel'+id+'"></label>'+
						'<input type="hidden" name="hdiscper" id="hdiscper'+id+'" value="">'+
						'<input type="hidden" name="hdiscamt" id="hdiscamt'+id+'" value="">';

		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = ' <label id="hschemediscamtlabel'+id+'"></label>'+
						'<input type="hidden" name="hschemediscper" id="hschemediscper'+id+'" value="">'+
						'<input type="hidden" name="hschemediscamt" id="hschemediscamt'+id+'" value="">';


		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '<label id="hrevratelabel'+id+'"></label>'+
					     '<input type="hidden" name="hrevrate" id="hrevrate'+id+'" value="">';

		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '</label> <label id="hrevdisclabel'+id+'"></label>'+
				'<input type="hidden" name="hrevdiscper" id="hrevdiscper'+id+'" value="">'+
				'<input type="hidden" name="htaxrate" id="htaxrate'+id+'" value="">'+
				'<input type="hidden" name="hrevtaxrate" id="hrevtaxrate'+id+'" value="">'+
				'<input type="hidden" name="hcedamt" id="hcedamt'+id+'" value="">'+
				'<input type="hidden" class="number" readonly  name="vatperqty" id="vatperqty'+id+'" value=""  maxlength="8" >'+
				'<input type="hidden" class="number" readonly  name="revvatperqty" id="revvatperqty'+id+'" value=""  maxlength="8" >'+
				'<input type="hidden" class="number" readonly  name="hrevvat" id="hrevvat'+id+'" value=""  maxlength="8" >'+
				'<input type="hidden" name="hvat" id="hvat'+id+'" value="">'+
				'<input type="hidden" name="hrevdisc" id="hrevdisc'+id+'" value="">';

	    cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '</label> <label id="hrevschemedisclabel'+id+'"></label>'+
				'<input type="hidden" name="hrevschemediscper" id="hrevschemediscper'+id+'" value="">'+
				'<input type="hidden" name="hrevschemedisc" id="hrevschemedisc'+id+'" value="">';
		//if (prefVAT == 'Y') {
			cell = row.insertCell(-1);
			cell.setAttribute("style","text-align:right");
			cell.innerHTML = '<label id="htaxratelabel'+id+'"></label>';

			cell = row.insertCell(-1);
			cell.setAttribute("style","text-align:right");
			cell.innerHTML = '<label id="htaxamtlabel'+id+'"></label>' ;
		//}
		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		if (indentno!= ''){
			cell.innerHTML = '<label id="hamtlabel'+id+'"></label>'+
						'<input type="hidden" name="hamt" id="hamt'+id+'" value="0">'
			+ '<input type="hidden" name="hindentno" id="hindentno'+id+'" value="'+indentno+'">'+
			'<input type="hidden" name="hrejQty" id="hrejQty'+id+'" value="">';
		} else{
		cell.innerHTML = '<label id="hamtlabel'+id+'"></label>'+
						'<input type="hidden" name="hamt" id="hamt'+id+'" value="0.00">';

		}
		cell = row.insertCell(-1);
		cell.setAttribute("style","text-align:right");
		cell.innerHTML = '<label id="hRecdAmtlabel'+id+'"></label>'+
						'<input type="hidden" name="hRecdAmt" id="hRecdAmt'+id+'" value="0.00">' +
			'<input type="hidden" name="hTranType" id="hTranType'+id+'" value="">';
		cell = row.insertCell(-1);
		cell.setAttribute("align","right");
		cell.innerHTML	= '<label id="itemCheck'+id+'"></label>'
						+ '<input type="hidden" name="hdeleted" id="hdeleted'+id+'" value="false">';
		cell = row.insertCell (-1);
		cell.setAttribute("align","right");
		cell.innerHTML = '<label id="editIcon'+id+'"></label>';

		var tableEls = Dom.getElementsByClassName("dialog_displayColumns", "table");
		for (var i=0; i<tableEls.length; i++) {
			var colsArray = YAHOO.util.Cookie.get(actionId +"_headerColumns_"+i);
			if (null != colsArray) {
				colsArray = YAHOO.lang.JSON.parse(colsArray);
			}

			if (colsArray == null || colsArray == '') {
				colsArray = new Array();
			}

			alterColumns(colsArray, i);
		}

	}
	resetMedicineDetails();
}


function updateInnerHTML(costprice,discount, revRate, revDisc, schemedisc,revschemedisc,taxSubGroupList,taxrate){

	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length;
	var dialogId  = document.getElementById("dialogId").value;
	var id = numRows - 1;
	var revtaxamt = 0;
	var cell;
	var flag = '';
	var cedamt = 0.00;
	var revced = 0.00;
	document.getElementById('hrate'+dialogId).value = parseFloat(costprice).toFixed(decDigits);
	document.getElementById('hratelabel'+dialogId).innerHTML = parseFloat(costprice).toFixed(decDigits);
	document.getElementById('hdiscper'+dialogId).value = parseFloat(discount).toFixed(decDigits);
	document.getElementById('hdiscamt'+dialogId).value = 0;
	document.getElementById('hschemediscper'+dialogId).value = parseFloat(schemedisc).toFixed(decDigits);
	document.getElementById('hschemediscamt'+dialogId).value = 0;
	document.getElementById('hrevrate'+dialogId).value = parseFloat(revRate).toFixed(decDigits);
	document.getElementById('hrevratelabel'+dialogId).innerHTML = parseFloat(revRate).toFixed(decDigits);
	if (revDisc != "" && revDisc != 0){
		document.getElementById('hrevdiscper'+dialogId).value = parseFloat(revDisc).toFixed(decDigits);

	} else{
		document.getElementById('hrevdiscper'+dialogId).value = 0;
	}

	if (revschemedisc != "" && revschemedisc != 0){
		document.getElementById('hrevschemediscper'+dialogId).value = parseFloat(revschemedisc).toFixed(decDigits);

	} else{
		document.getElementById('hrevschemediscper'+dialogId).value = 0;
	}
	var taxAmt = 0;
	for(var i=0; i < taxSubGroupList.length; i++) {
		var groupId = taxSubGroupList[i].groupid;
		if(document.getElementById(dialogId+'taxrate'+groupId))
			document.getElementById(dialogId+'taxrate'+groupId).value = taxSubGroupList[i].rate;
		if(document.getElementById(dialogId+'taxamount'+groupId))
			document.getElementById(dialogId+'taxamount'+groupId).value = taxSubGroupList[i].amount;
		if(document.getElementById(dialogId+'taxsubgroupid'+groupId))
			document.getElementById(dialogId+'taxsubgroupid'+groupId).value = taxSubGroupList[i].taxsubgroupid;
		if(taxSubGroupList[i] && isNotNullObj(taxSubGroupList[i].amount)) {
			if(!isNaN(parseFloat(taxSubGroupList[i].amount))) {
				taxAmt = taxAmt+parseFloat(taxSubGroupList[i].amount);
			}
		}
	}
	document.getElementById("hrevvat"+dialogId).value =  parseFloat(taxAmt).toFixed(decDigits);
	if(isNotNullValue('hrevtaxrate', dialogId))
		document.getElementById("hrevtaxrate"+dialogId).value = taxrate;
	
	calc (dialogId);
	itemDialog.hide();
}


function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		getTaxDetails(prepareTaxObj());
		onAddMedicine()
		return false;
	}
}

function onChangeBonusQty(obj) {
	var bonusStock = getFieldText('currentbonusstock');
	if(obj.value <= bonusStock) {
		getTaxDetails(prepareTaxObj());
	} else {
		obj.value = 0;
	}
		
}

function onChangeQty(obj) {
	var billedStock = parseFloat(getFieldText('currentstock'));
	if(parseFloat(obj.value) <= billedStock) {
		getTaxDetails(prepareTaxObj());
	} else {
		alert("Return Qty should be less than or equal to Stock Qty");
		obj.value = 0;
	}
}

function onChangeRevRate(obj, decDigits) {
	makeingDec(obj.value, obj, decDigits);
	getTaxDetails(prepareTaxObj());
}

function validateSupplier(){
	 if(document.supplierdebitform.supplierName.value == ''){
		   alert("Select Supplier");
		   document.supplierdebitform.supplierName.focus();
		   return false
	  }

	  return true;
}

function screenValidation(){
   if(document.supplierdebitform.supplierName.value == ''){
	   alert("Select Supplier");
	   document.supplierdebitform.supplierName.focus();
	   return false
   }

   if(document.supplierdebitform.returnType.options.selectedIndex==2){
	   if(document.getElementById("remarks").value==""){
		   alert("Enter Remarks");
		   document.supplierdebitform.remarks.focus();
		   return false
       }saveStk
   }
   if(document.supplierdebitform.medicine.value==""){
	   alert("Enter Item Name");
	   document.supplierdebitform.medicine.focus();
	   return false;
   }
   if(fromedit != 'Y' && document.getElementById("batch").value == ''){
	   alert("Select Batch/Serial No");
	   document.supplierdebitform.batch.focus();
	   return false
   }
   if (document.supplierdebitform.return_billedQty.value == '' || !isAmount(document.supplierdebitform.return_billedQty.value)) {
	   	alert("Please enter only Numerics");
	   	document.supplierdebitform.return_billedQty.value='';
	   	document.supplierdebitform.return_billedQty.focus();
	   	return false;
   }
    if (document.supplierdebitform.return_bonusQty.value == '' || !isAmount(document.supplierdebitform.return_bonusQty.value)) {
	   	alert("Please enter only Numerics");
	   	document.supplierdebitform.return_bonusQty.value='';
	   	document.supplierdebitform.return_bonusQty.focus();
	   	return false;
   }
   if((document.supplierdebitform.return_billedQty.value=="" || document.supplierdebitform.return_billedQty.value=="0") &&
   	(document.supplierdebitform.return_bonusQty.value=="" || document.supplierdebitform.return_bonusQty.value=="0")){
	   alert("Enter Non-zero Return Quantity ");
	   document.supplierdebitform.return_billedQty.focus();
	   return false;
   }
	if (!isValidNumber(document.supplierdebitform.return_billedQty, qtyDecimal)) return false;
	if (!isValidNumber(document.supplierdebitform.return_bonusQty, qtyDecimal)) return false;



    return true
}
function stockValidation(){
	var currentStock = document.getElementById("currentstock").innerHTML;
  	var currentStockStatus=parseFloat(currentStock)-(parseFloat(document.getElementById("return_billedQty").value));
  	if(currentStockStatus<0){
	 	alert("Requested quantity can't be Returnable since it is more than available stock")
	 	document.getElementById("return_billedQty").focus();
		document.getElementById("return_billedQty").value=0;
		return false;
   	}

   	var currentbonusStock = document.getElementById("currentbonusstock").innerHTML;
  	var currentbonusStockStatus=parseFloat(currentbonusStock)-(parseFloat(document.getElementById("return_bonusQty").value));
 	if(currentbonusStockStatus<0){
	 	alert("Requested bonus quantity can't be Returnable since it is more than available bonus stock")
	 	document.getElementById("return_bonusQty").focus();
		document.getElementById("return_bonusQty").value=0;
		return false;
   	}
   return true;
}


function deleteItem(imgObj, rowId) {
	var itemListTable = document.getElementById("medList");
	var row = itemListTable.rows[rowId];
	var deletedInput = document.getElementById('hdeleted'+rowId);
	var editImgObj = document.getElementById('editButton'+rowId);
	if (deletedInput.value == 'false') {
		deletedInput.value = 'true';
		document.getElementById(editImgObj.id).src = popurl+"/icons/Edit1.png";
		document.getElementById(imgObj.id).src = popurl+"/icons/undo_delete.gif";
		editImgObj.setAttribute('onclick','');
		editImgObj.setAttribute('class','');
		row.className = "deleted";
	} else {
		deletedInput.value = 'false';
		row.className = "";
		document.getElementById(editImgObj.id).src = popurl+"/icons/Edit.png";
		document.getElementById(imgObj.id).src = popurl+"/icons/delete.gif";
		editImgObj.setAttribute('onclick',"getItemGroupDialog('"+rowId+"')");
		editImgObj.setAttribute('class','button');
	}
	resetTotals();
}
/*function onchangeQty (rowId) {
	var actQty = parseFloat(document.getElementById("hactqty"+rowId).value);
	var retQty = parseFloat(document.getElementById("hretqty"+rowId).value);
	if (retQty > actQty) {
		alert("Return Qty should be less than or equal to Stock Qty");
		document.getElementById("hretqty"+rowId).value = 0;
		document.getElementById("hretqty"+rowId).focus();
		calc (rowId);
	} else {
		calc (rowId);
	}
	resetTotals();
}*/
function calc (rowId) {
	if (document.getElementById('hrate'+rowId).value == '') document.getElementById('hrate'+rowId).value = 0;
	if (document.getElementById('hgrnrate'+rowId).value == '') document.getElementById('hgrnrate'+rowId).value = 0;
	if (document.getElementById('hdiscper'+rowId).value == '') document.getElementById('hdiscper'+rowId).value = 0;
	if (document.getElementById('hrevrate'+rowId).value == '') document.getElementById('hrevrate'+rowId).value = 0;
	if (document.getElementById('hrevdiscper'+rowId).value == '') document.getElementById('hrevdiscper'+rowId).value = 0;
	if (document.getElementById('hretqty'+rowId).value == '') document.getElementById('hretqty'+rowId).value = 0;
	if (document.getElementById('htaxrate'+rowId).value == '') document.getElementById('htaxrate'+rowId).value = 0;
	if (document.getElementById('hrevtaxrate'+rowId).value == '') document.getElementById('hrevtaxrate'+rowId).value = 0;
	if (document.getElementById('hcedamt'+rowId).value == '') document.getElementById('hcedamt'+rowId).value = 0;
	var rate = parseFloat(document.getElementById('hratelabel'+rowId).innerHTML);
	var grnrate = parseFloat(document.getElementById('hgrnrate'+rowId).value);
	var revrate = parseFloat(document.getElementById('hrevratelabel'+rowId).innerHTML);
	var disc = parseFloat(document.getElementById('hdiscper'+rowId).value);
	var schemedisc = parseFloat(document.getElementById('hschemediscper'+rowId).value);
	var revdiscper = parseFloat(document.getElementById('hrevdiscper'+rowId).value);
	var revschemediscper = parseFloat(document.getElementById('hrevschemediscper'+rowId).value);
	var vatperqty = parseFloat(document.getElementById('hvat'+rowId).value);
	var revvatperqty = parseFloat(document.getElementById('hvat'+rowId).value);
	var retQty = parseFloat(document.getElementById('hretqty'+rowId).value);
	var retBonusQty = parseFloat(document.getElementById('hretbonusqty'+rowId).value);
	var taxType = document.getElementById('htaxtype'+rowId).value;
	var taxPercent = parseFloat(document.getElementById('htaxrate'+rowId).value);
	var revTaxPercent = parseFloat(document.getElementById('hrevtaxrate'+rowId).value);
	var cedamt = parseFloat(document.getElementById('hcedamt'+rowId).value) * retQty;
	var adjMRP = parseFloat(document.getElementById('hadjmrp'+rowId).value);
	var discamt  = (rate * retQty * disc)/100;
	var schemediscamt  = (rate * retQty * schemedisc)/100;
	var revdiscamt = (revrate * retQty * revdiscper)/100;
	var revschemediscamt = (revrate * retQty * revschemediscper)/100;
	document.getElementById('hdiscamt'+rowId).value =  parseFloat(discamt).toFixed(decDigits);
	document.getElementById('hdiscamtlabel'+rowId).innerHTML =  parseFloat(discamt).toFixed(decDigits);
	document.getElementById('hrevdisc'+rowId).value = parseFloat(revdiscamt).toFixed(decDigits);
	document.getElementById('hrevdisclabel'+rowId).innerHTML =  parseFloat(revdiscamt).toFixed(decDigits);
	var discAmt = parseFloat(document.getElementById('hdiscamt'+rowId).value);

	document.getElementById('hschemediscamt'+rowId).value =  parseFloat(schemediscamt).toFixed(decDigits);
	document.getElementById('hschemediscamtlabel'+rowId).innerHTML =  parseFloat(schemediscamt).toFixed(decDigits);
	document.getElementById('hrevschemedisc'+rowId).value = parseFloat(revschemediscamt).toFixed(decDigits);
	document.getElementById('hrevschemedisclabel'+rowId).innerHTML =  parseFloat(revschemediscamt).toFixed(decDigits);
	var schemediscAmt = parseFloat(document.getElementById('hschemediscamt'+rowId).value);
	var vatamt = 0;
	var bonusvatamt = 0;
	var revvatamt = 0;
	var revbonusvatamt = 0;
	var revcedamt = 0;
	var bonusvatperqty = 0;
	
	if ( ! taxType.match(/^M/)){
		if (  taxType.match('CB')){
			discperqty = rate * (retQty) * disc/100;
			revdiscperqty = revrate * (retQty) * revdiscper/100;
			vatamt = ((rate*retQty ) -discperqty -schemediscamt  + cedamt) * taxPercent / 100;
			bonusvatamt = ((grnrate*retBonusQty)) * taxPercent / 100;
			vatperqty = retQty > 0 ? vatamt / (retQty) : 0 ;
			revvatamt = ((revrate*retQty) -revdiscperqty+ cedamt) * taxPercent / 100;
			revbonusvatamt = ((grnrate*retBonusQty) ) * taxPercent / 100;
			revvatperqty = retQty > 0 ? revvatamt / (retQty) : 0;
			bonusvatperqty = retBonusQty > 0 ? bonusvatamt / (retBonusQty) : 0;
		} else if(retQty != 0){
			discperqty = Math.round(
					rate * (retQty) * disc/100 );
			revdiscperqty = Math.round(
					revrate * (retQty) * revdiscper/100 );
			vatamt = ((rate * retQty) -discperqty+ cedamt) * taxPercent / 100;
			vatperqty = retQty > 0 ? vatamt / retQty : 0;
			bonusvatperqty = vatperqty;
			revvatamt = ((revrate * retQty)-revdiscperqty + cedamt) * taxPercent / 100;
			revvatperqty = retQty > 0 ? revvatamt / retQty : 0;
		}
	}else{
		vatperqty = adjMRP * taxPercent / 100;
		revvatperqty = adjMRP * taxPercent / 100;
		vatamt = vatperqty *(taxType.match('MB') ? retBonusQty+retQty : retQty);
		revvatamt = revvatperqty * (taxType.match('MB') ? retBonusQty+retQty : retQty);
		bonusvatperqty = vatperqty;
		/** CED only comes into effect if tax type is 'C'*/
	}
	var calcrate = "";
	var calcrevrate = "";

	calcrate = parseFloat(document.getElementById('hrate'+rowId).value);
	calcrevrate = parseFloat(document.getElementById('hrevrate'+rowId).value);
	
	var vatamt = (taxType.match('CB') || taxType.match('MB') ? bonusvatperqty * retBonusQty : 0) + vatperqty * retQty ;
	var revvatamt = (taxType.match('CB') || taxType.match('MB') ? bonusvatperqty * retBonusQty : 0) + revvatperqty * retQty ;
	
	document.getElementById('hvat'+rowId).value =  parseFloat(vatamt);
	revvatamt = parseFloat(document.getElementById('hrevvat'+rowId).value).toFixed(decDigits);
	//if (prefVAT == 'Y') {
		document.getElementById('htaxamtlabel'+rowId).innerHTML = parseFloat(revvatamt).toFixed(decDigits);
		document.getElementById('htaxratelabel'+rowId).innerHTML = revTaxPercent;
	//}
	document.getElementById('hrevvat'+rowId).value =  parseFloat(revvatamt);
	document.getElementById('hcedamt'+rowId).value =  retQty == 0 ? 0 : (parseFloat(cedamt)/retQty).toFixed(decDigits);
	var totamt = calcrate * retQty;
	var totRevAmt = calcrevrate * retQty ;
	if ( vatamt < 0 ){
		vatamt = 0;
	}
	
	if ( revvatamt < 0 ){
		revvatamt = 0;
	}
	document.getElementById('hamt'+rowId).value =  parseFloat(totamt + vatamt + cedamt - (discamt+schemediscamt));
	document.getElementById('hamtlabel'+rowId).innerHTML =  parseFloat(totamt + vatamt + cedamt - (discamt+schemediscamt)).toFixed(decDigits);
	document.getElementById('hRecdAmt'+rowId).value =  parseFloat(totRevAmt + parseFloat(revvatamt) + cedamt - (revdiscamt+revschemediscamt)).toFixed(decDigits);
	document.getElementById('hRecdAmtlabel'+rowId).innerHTML =  parseFloat(totRevAmt + parseFloat(revvatamt) + cedamt - (revdiscamt+revschemediscamt)).toFixed(decDigits);
	document.getElementById('hrate'+rowId).value = calcrate;
	document.getElementById('hrevrate'+rowId).value = calcrevrate;
	document.getElementById('vatperqty'+rowId).value = vatperqty;
	document.getElementById('revvatperqty'+rowId).value = revvatperqty;
	resetTotals();
}
function itemLevelDiscount () {
	if (document.getElementById('itemleveldisc').value == '') document.getElementById('itemleveldisc').value = 0;
	var item = parseFloat(document.getElementById('itemleveldisc').value);
	var totalNoOfRows;
	totalNoOfRows = document.getElementById("medList").rows.length-1;
	for (var i=1;i<totalNoOfRows;i++) {
		if (!(document.getElementById("hdeleted"+i).value == 'true')) {
			document.getElementById('hdiscper'+i).value = item;
			document.getElementById("hrevdiscper"+i).value = item;
			calc (i);
			/**
			 * Recalculate Tax split based on new discount.
			 */
//			var pkgSize = document.getElementById("hpkgsz"+i).value;
//			var retQty = document.getElementById("hretqty"+i).value;
//			var retBonusQty = document.getElementById("hretbonusqty"+i).value;
//			var retRate = document.getElementById("hrevrate"+i).value;
//			var retSchemeDiscountAmt = document.getElementById("hrevschemedisc"+i).value;
//			var taxType = document.getElementById("htaxtype"+i).value;
//			var discountPer = item;
//			var discountAmt = (retRate*discountPer)/100;
//			var storePkgSize = document.getElementsByName('qty_unit');
//			var store_package_uom = 'P';
//			for(var i = 0; i < storePkgSize.length; i++){
//			    if(storePkgSize[i].checked){
//			    	store_package_uom = storePkgSize[i].value;
//			        break;
//			    }
//			}
//			if(taxType && taxType != 'undefined' && taxType != null && taxType != 'null' && (taxType =='CB' || taxType =='C')) {
//				for(var j=0; j < groupListJSON.length; j++) {
//					var itemGroupId = groupListJSON[j].item_group_id;
//					if(document.getElementById(i+"taxrate"+itemGroupId) != undefined && document.getElementById(i+"taxrate"+itemGroupId) != null && document.getElementById(i+"taxrate"+itemGroupId).value != undefined) {
//						var taxRate = document.getElementById(i+"taxrate"+itemGroupId).value;
//						var tempQty = 0;
//						if(taxRate > 0) {
//							if(store_package_uom == 'P') {
//								tempQty = ((retQty+retBonusQty)/pkgSize);
//							} else {
//								tempQty = (retQty+retBonusQty);
//							}
//							var taxSplitAmt = ((retRate-discountAmt)*tempQty*taxRate)/100;
//							document.getElementById(i+"taxamount"+itemGroupId).value = taxSplitAmt;
//						}
//					}
//				}
//			}
		}
	}
	document.getElementById('itemleveldisc').value = 0;
	//resetTotals();
}

function resetTotals(){
	var totalNoOfRows;
	totalNoOfRows = document.getElementById("medList").rows.length-1;
	var tempTotDisc = 0;
	var tempTotRevDisc = 0;
	var tempTotSchDisc = 0;
	var tempTotRevSchDisc = 0;
	var tempTotVAT = 0;
	var tempTotRevVAT = 0;
	var tempTotCED = 0;
	var tempTotRevCED = 0;
	var tempTotAmount = 0;
	var tempTotRecAmt = 0;
	
	for(var j=0; j < groupListJSON.length; j++) {
	 	setFieldHtml("taxamtlabel_", "0", groupListJSON[j].item_group_id);
	}
	setFieldHtml("lblTotalTaxes", "0");
	
	var totalTaxAmt = 0;
	for (var i=1;i<=totalNoOfRows;i++) {
		if (document.getElementById('hdeleted'+i).value == 'false'){
			if (document.getElementById("hdiscamt"+i).value == ''){
				document.getElementById("hdiscamt"+i).value = 0;
			}
			if (document.getElementById("hrevdisc"+i).value == ''){
				document.getElementById("hrevdisc"+i).value = 0;
			}
			var discToBeAdded = parseFloat(formatAmountObj(document.getElementById("hdiscamt"+i),decDigits));
			var revDiscToBeAdded = parseFloat(formatAmountObj(document.getElementById("hrevdisc"+i), decDigits));
			var schdiscToBeAdded = parseFloat(formatAmountObj(document.getElementById("hschemediscamt"+i),decDigits));
			var revSchDiscToBeAdded = parseFloat(formatAmountObj(document.getElementById("hrevschemedisc"+i), decDigits));
			var vatToBeAdded = document.getElementById("hvat"+i).value?parseFloat(document.getElementById("hvat"+i).value):0;
			var revvatToBeAdded = document.getElementById("hrevvat"+i).value?parseFloat(document.getElementById("hrevvat"+i).value):0;
			var cedToBeAdded = parseFloat(formatAmountObj(document.getElementById("hced"+i), decDigits));
			var amtToBeAdded = parseFloat(formatAmountObj(document.getElementById("hamt"+i), decDigits));
			var recdAmtToBeAdded = parseFloat(formatAmountObj(document.getElementById("hRecdAmt"+i), decDigits));
			tempTotDisc = parseFloat(tempTotDisc+discToBeAdded);
			tempTotRevDisc = parseFloat(tempTotRevDisc+revDiscToBeAdded);
			tempTotSchDisc = parseFloat(tempTotSchDisc+schdiscToBeAdded);
			tempTotRevSchDisc = parseFloat(tempTotRevSchDisc+revSchDiscToBeAdded);
			tempTotVAT = parseFloat(tempTotVAT+vatToBeAdded);
			tempTotRevVAT = parseFloat(tempTotRevVAT+revvatToBeAdded);
			tempTotCED = parseFloat(tempTotCED+cedToBeAdded);
			tempTotRevCED = parseFloat(tempTotRevCED+cedToBeAdded);
			tempTotAmount = parseFloat(tempTotAmount) + parseFloat(amtToBeAdded);
			tempTotRecAmt = parseFloat(tempTotRecAmt) + parseFloat(recdAmtToBeAdded) ;
			for(var j=0; j < groupListJSON.length; j++) {
				var itemGroupId = groupListJSON[j].item_group_id;
				if(document.getElementById(i+"taxamount"+itemGroupId) && document.getElementById(i+"taxamount"+itemGroupId).value) {
					var taxAmt = parseFloat(document.getElementById(i+"taxamount"+itemGroupId).value);
					var existTotalTaxAmt = parseFloat(getFieldHtml("taxamtlabel_", itemGroupId));
					totalTaxAmt += parseFloat(existTotalTaxAmt + taxAmt);
					setFieldHtml("taxamtlabel_", parseFloat(existTotalTaxAmt + taxAmt).toFixed(decDigits), itemGroupId);
				}
			}
		}
	}
	
	document.getElementById("totDisc").value = formatAmountValue(tempTotDisc,false, decDigits);
	document.getElementById("totRevDisc").value = formatAmountValue(tempTotRevDisc,false, decDigits);
	document.getElementById("totSchemeDisc").value = formatAmountValue(tempTotSchDisc,false, decDigits);
	document.getElementById("totRevSchemeDisc").value = formatAmountValue(tempTotRevSchDisc,false, decDigits);
	document.getElementById("totDisc").value = formatAmountValue(tempTotDisc,false, decDigits);
	document.getElementById("totRevDisc").value = formatAmountValue(tempTotRevDisc,false, decDigits);
	document.getElementById("totVAT").value = formatAmountValue(tempTotVAT,false, decDigits);
	document.getElementById("totRevVAT").value = formatAmountValue(tempTotRevVAT,false, decDigits);
	document.getElementById("totCED").value = 0;
	document.getElementById("totRevCED").value = 0;
	document.getElementById("totAmount").value = formatAmountValue(tempTotAmount,false, decDigits);
	document.getElementById("totRecAmount").value = formatAmountValue(tempTotRecAmt,false, decDigits);
	document.getElementById("totDisclabel").innerHTML = formatAmountValue(tempTotDisc,false, decDigits);
	document.getElementById("totRevDisclabel").innerHTML = formatAmountValue(tempTotRevDisc,false, decDigits);
	document.getElementById("totSchemeDisclabel").innerHTML = formatAmountValue(tempTotSchDisc,false, decDigits);
	document.getElementById("totRevSchemeDisclabel").innerHTML = formatAmountValue(tempTotRevSchDisc,false, decDigits);
	document.getElementById("totAmountlabel").innerHTML = formatAmountValue(tempTotAmount,false, decDigits);
	document.getElementById("totRecAmountlabel").innerHTML = formatAmountValue(tempTotRecAmt,false, decDigits);
	document.getElementById("lblTotalTaxes").innerHTML = formatAmountValue(tempTotRevVAT,false, decDigits);
	calculateNetPayble();
}
function savestock(){
	if (!(fromedit == 'Y')) {
		if (document.supplierdebitform.returnAgainst.value == 'grnReturn' && document.supplierdebitform.grn_no.value == ''){
			alert("Please select GRN No.");
			document.supplierdebitform.grn_no.focus();
			return false;
		}
	}
	if(document.supplierdebitform.supplierName.value == ''){
	   alert("Select Supplier");
	   document.supplierdebitform.supplierName.focus();
	   return false
   	}

	if (!(fromedit == 'Y')) {
		var length = document.getElementById('medList').rows.length-2;
	 	if (length < 1) {
	 		alert("No rows in the grid !");
	 		return false;
	 	}
	    var allChecked = false;
	 	var itemListTable = document.getElementById("medList");
		var numRows = itemListTable.rows.length-1;

		for (var k=1;k<numRows;k++) {
	    	if (document.getElementById("hdeleted"+k).value == 'false') {
		    	if (document.getElementById("hretqty"+k).value == 0 && document.getElementById("hretbonusqty"+k).value == 0) {
		    		alert("Return qty should not be zero");
		    		document.getElementById("hretqty"+k).focus();
		    		return false;
			    }
	    		allChecked = true;
	    	}
		}
	    if (!allChecked) {
	    	alert("all row(s) in the grid are checked \n so no record(s) to save");
			return false;
	    }
    }
    document.supplierdebitform.saveStk.disabled = true;
    document.supplierdebitform.returnType.disabled = false;
	document.supplierdebitform.discType.disabled = false;
    var url = null;
    if (fromedit == 'Y') url = "StoresSupplierReturnslist.do?_method=updateSupplierReturnDebit";
    else url = "StoresSupplierReturnslist.do?_method=makeSupplierDebit";
	document.supplierdebitform.action = url;
	document.supplierdebitform.store.disabled = false;
	document.supplierdebitform.submit();
}

function changeGRN (grn) {
 window.open(popurl+'/DirectReport.do?report=grnreport&grNo='+grn);
 document.getElementById('grn').options.selectedIndex = 0;
}
function changeDebit (debit) {
 window.open(popurl+'/DirectReport.do?report=StoresDebitNoteReport&debitNo='+debit);
 document.getElementById('debitNote').options.selectedIndex = 0;
}
function makeingDec(objValue,obj, digits){
	if (objValue == '') objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj.name).value = parseFloat(objValue).toFixed(digits);
	} else document.getElementById(obj.name).value = parseFloat(0).toFixed(digits);
    //getTaxDetails(prepareTaxObj());
}

function validateMaxPercent(objValue, obj){
	 if (objValue == '') objValue = 0;
	  if (isAmount(objValue)) {
	  	if (parseFloat(objValue) > 100){
	  		alert('Maximum value cannot exceed 100');
	  		document.getElementById(obj.id).value = 0.00;
	  		obj.focus();
	  		return false;
	  	} else return true;
	  }
	  return true;
}

function validateOtherDiscount(){
	 var objValue = document.supplierdebitform.discount.value;
	 var obj = document.supplierdebitform.discount;
	 if (document.supplierdebitform.discType.value == 'P'){
		 validateMaxPercent(objValue, obj);
	  }

}

function calculateNetPayble(){
	var itemRecTotalObj = document.supplierdebitform.totRecAmount;
	var itemTotalObj = document.supplierdebitform.totAmount;
	var roundedAmtObj = document.supplierdebitform.roundAmt;
	var otherChargesObj = document.supplierdebitform.otherCharges;
	var invDiscountsObj = document.supplierdebitform.totDisc;
	var revDiscountsObj = document.supplierdebitform.totRevDisc;
	var totalRevVATObj = document.supplierdebitform.totRevVAT
	var totalVATObj = document.supplierdebitform.totVAT
	var calcDisc = "";
	var calcRevDisc = "";
	if (document.supplierdebitform.discType.value == 'P') {
		calcRevDisc = parseFloat((parseFloat(document.supplierdebitform.totRecAmount.value) * parseFloat(document.supplierdebitform.discount.value)) / 100).toFixed(decDigits);
		calcDisc = parseFloat((parseFloat(document.supplierdebitform.totAmount.value) * parseFloat(document.supplierdebitform.discount.value)) / 100).toFixed(decDigits);
		document.supplierdebitform.otherdisc.value = calcDisc;
		document.supplierdebitform.otherrevdisc.value = calcRevDisc;
	} else {
		calcRevDisc = document.supplierdebitform.discount.value ? document.supplierdebitform.discount.value : "0";
		calcDisc = document.supplierdebitform.discount.value ? document.supplierdebitform.discount.value : "0";
		document.supplierdebitform.otherdisc.value = calcDisc;
		document.supplierdebitform.otherrevdisc.value = calcRevDisc;
	}
	if ( itemTotalObj.value == "" ) { itemTotalObj.value = 0; }
	if ( itemRecTotalObj.value == "" ) { itemTotalRecObj.value = 0; }
	if ( roundedAmtObj.value == "" ) { roundedAmtObj.value = 0; }
	if ( otherChargesObj.value == "") { otherChargesObj.value = 0;}
	if ( invDiscountsObj.value == "") { invDiscountsObj.value = 0;}
	var itemRecTotal = formatAmountObj(itemRecTotalObj);
	var itemTotal = formatAmountObj(itemTotalObj);
	var otherCharges = formatAmountObj(otherChargesObj);
	var roundedAmt = formatAmountObj(roundedAmtObj);
	var invoiceDiscount = formatAmountObj(invDiscountsObj);
	var revDiscount = formatAmountObj(revDiscountsObj) ;
	var totalRevVAT =  formatAmountObj(totalRevVATObj);
	var totalVAT =  formatAmountObj(totalVATObj);
	totalRevVAT = eval(0 - totalRevVAT);
	document.supplierdebitform.netAmtPayble.value = parseFloat(eval(parseFloat(itemTotal) + parseFloat(roundedAmt) - parseFloat(calcDisc) +parseFloat(otherCharges))  ).toFixed(decDigits) ;

	document.supplierdebitform.recDebitAmt.value =  parseFloat(eval(parseFloat(itemRecTotal)+ parseFloat(roundedAmt) - parseFloat(calcRevDisc)+parseFloat(otherCharges))).toFixed(decDigits);
}

function onchangeDiscType(){
	if (document.supplierdebitform.discount.value == '') document.supplierdebitform.discount.value = 0;
    if (document.supplierdebitform.discType.value == 'P') {
    	var calcRevDiscount = parseFloat(document.supplierdebitform.totRecAmount.value) * parseFloat(document.supplierdebitform.discount.value) / 100;
    	var calcDiscount = parseFloat(document.supplierdebitform.totAmount.value) * parseFloat(document.supplierdebitform.discount.value) / 100;
    	document.supplierdebitform.otherrevdisc.value = parseFloat(calcRevDiscount).toFixed(decDigits);
    	document.supplierdebitform.otherdisc.value = parseFloat(calcDiscount).toFixed(decDigits);
		calculateNetPayble();
	} else {
       document.supplierdebitform.otherdisc.value = document.supplierdebitform.discount.value;
       document.supplierdebitform.otherrevdisc.value = document.supplierdebitform.discount.value;
       calculateNetPayble();
	}
}
function chgType(val) {
	if (val == 'O') document.getElementById('othersreason').readOnly = false;
	else document.getElementById('othersreason').readOnly = true;
}
function enableTaxDefaults() {
	if (document.getElementById("vat").checked ) {
		document.getElementById("cstrate").readOnly = true;
		if(taxLabel == 'V') {
			document.getElementById("taxId").innerHTML = "VAT(%)";
			document.getElementById("taxIdAmt").innerHTML = "VAT Amt";
		} else{
			document.getElementById("taxId").innerHTML = "GST(%)";
			document.getElementById("taxIdAmt").innerHTML = "GST Amt";
		}
		clearGrid();

	}else{
		document.getElementById("cstrate").readOnly = false;
		if(taxLabel == 'V') {
			document.getElementById("taxId").innerHTML = "CST(%)";
			document.getElementById("taxIdAmt").innerHTML = "CST Amt";
		}else{
			document.getElementById("taxId").innerHTML = "iGST(%)";
			document.getElementById("taxIdAmt").innerHTML = "iGST Amt";
		}
		
		if ( document.getElementById("cstrate").value == '' )
			document.getElementById("cstrate").value = 0;
		clearGrid();
	}
}

function updateTaxRates() {
	if ( document.supplierdebitform.cstrate.value == '' )
		document.supplierdebitform.cstrate.value = 0;

	var cstRt = document.supplierdebitform.cstrate.value;
	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length-1;
	for( var i=1; i<numRows; i++) {
		var CP = parseFloat( document.getElementById("hratelabel"+i).innerHTML ) + parseFloat( document.getElementById("vatperqty"+i).value );
		var revCP = parseFloat( document.getElementById("hrevratelabel"+i).innerHTML ) + parseFloat( document.getElementById("revvatperqty"+i).value );
		var tAmt = CP - ( (CP * 100) / ( 100 + parseFloat(cstRt) ) );
		var tRevAmt = revCP - ( (revCP * 100 ) / (100 + parseFloat(cstRt) ) );
		document.getElementById("vatperqty"+i).value = tAmt;
		document.getElementById("hrate"+i).i = (parseFloat(CP) - parseFloat(tAmt)).toFixed(decDigits);
		document.getElementById("hrevrate"+i).i = (parseFloat(revCP) - parseFloat(tRevAmt)).toFixed(decDigits);
		document.getElementById("htaxrate"+i).value = cstRt;
		calc(i);
	}

}
function addGroupMedDetails(){
	if (groupMedDetails != null && groupMedDetails.length > 0) document.getElementById('issue_units').checked = true;
	if(groupMedDetails == null || groupMedDetails.length == 0)
		return;

	document.forms[0].store.value = groupDeptId;
	//initMedicineAutoComplete();
	//initSupplierAutoComplete();

	for(var i=0;i<groupMedDetails.length;i++){

		var medicineId = groupMedDetails[i].medicine_id;
		var medicine = groupMedDetails[i].medicine_name;
		var batchNo = groupMedDetails[i].batch_no;
		var itemBatchId = groupMedDetails[i].item_batch_id;
		if(groupMedDetails[i].exp_dt == null)
		var dateObj = groupMedDetails[i].exp_dt;
		else
		var dateObj = new Date(groupMedDetails[i].exp_dt);
		var mrp = groupMedDetails[i].mrp;
		var stockQty = groupMedDetails[i].qty;
		var issueQty = groupMedDetails[i].qty;
		var pkgsize =  groupMedDetails[i].issue_base_unit;
		var manf = groupMedDetails[i].manf_mnemonic;
		var adj_mrp_from_batch = Math.round(groupMedDetails[i].mrp/(1 + groupMedDetails[i].tax_rate/100));
		var adjmrp = adj_mrp_from_batch;
		var	taxtype = groupMedDetails[i].tax_type;
		var	taxrate = groupMedDetails[i].tax_rate;
		var cedamt = groupMedDetails[i].item_ced_amt;
		var selQty = qtyUnitSelection ();
		var itemUnits = selQty == 'I' ? groupMedDetails[i].issue_units : groupMedDetails[i].package_uom;
		if ((null == cedamt) || (cedamt == "")){
			cedamt = 0;
		}
		var	costprice = groupMedDetails[i].package_cp;
		var expiryDate = formatDate(new Date(groupMedDetails[i].exp_dt),'yyyymmdd','-');
		var actualQty = stockQty;
		var rejQty = groupMedDetails[i].qty_in_transit;
		var itemIdentification = groupMedDetails[i].identification;
		var issueUnits = groupMedDetails[i].issue_units;
		document.getElementById("dialogId").value = i+1;
		document.supplierdebitform.barCodeId.value = groupMedDetails[i].item_barcode_id;
		if ((rejQty != 'undefined') && (rejQty!= null) && (rejQty!= 0)){
			var actualRejQty = rejQty;        // removed / by pkg_size because we set issue_unit selection...
			var indentno = groupMedDetails[i].indent_no;
			addToInnerHTML(medicine,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,dateObj,actualQty,
				actualRejQty,0,adjmrp,taxtype,taxrate,0,costprice,expiryDate,indentno,rejQty,itemIdentification,0,
				costprice,0,cedamt,"Group", itemUnits,0,0,groupMedDetails[i].grn_cp);

		} else{
			addToInnerHTML(medicine,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,dateObj,
			actualQty,actualQty,0,adjmrp,taxtype,taxrate,0,costprice,expiryDate,'','',
			itemIdentification,0,costprice,0,cedamt,"Group", itemUnits,0,0,groupMedDetails[i].grn_cp);
		}

	}
	var elNewItem = matches(selsupp, oAutoComp);
//	oAutoComp._selectItem(elNewItem);
	oAutoComp._bItemSelected = true;
	onSelectSupplier();
}

function initItemGroupDialog(){
	itemDialog = new YAHOO.widget.Dialog("itemDialog",
			{
			width:"800px",
			context :["addButton", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
			});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:itemDialog,
	                                              	correctScope:true} );
	itemDialog.cfg.queueProperty("keylisteners", escKeyListener);
itemDialog.render();
}

function handleCancel(){
	resetMedicineDetails ();
	itemDialog.cancel();
}

function handleEnter (field, e) {
		e ? e : window.event;
		e.preventDefault ? e.preventDefault() : e.returnValue = false;
		document.getElementById('rate').focus;
		return false;
}


function handleFocus (field, e) {
		document.getElementById('rate').focus;
}


function getItemGroupDialog(id){
	if(document.getElementById("supplierName").value == '') {
		alert("Please Select the Supplier Name");
		return false;
	}
	button = document.getElementById("editButton"+id);
	itemDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	if ((document.getElementById("hmedId"+id) != null) && (document.getElementById("hmedId"+id).value != "")){
			document.supplierdebitform.medicine.value 	= document.getElementById("hmedName"+id).value ;
			document.supplierdebitform.medicineId.value = document.getElementById("hmedId"+id).value;
			document.supplierdebitform.barCodeId.value = document.getElementById('hitembarcode'+id).value;
			var elNewItem = matches(document.getElementById("hmedName"+id).value, oMedAutoComp);
			dialogOpened = id;
			if (document.getElementById("debitNo").value != null && document.getElementById("debitNo").value != ""){

			} else{
				oMedAutoComp._bItemSelected = true;
				onSelectMedicine();
//				oMedAutoComp._selectItem(elNewItem);
			}
	} else{
		//trying to add a new row to already added group items
		document.supplierdebitform.medicine.value = "";
		document.supplierdebitform.barCodeId.value = "";
		document.supplierdebitform.batch.value 	= "" ;
		document.getElementById('expdate').innerHTML = "" ;
		document.getElementById('rate').value 	= "" ;
		document.getElementById('rev_rate').value 	= "" ;
		document.getElementById('discper').value 	= "" ;
		document.getElementById('rev_discper').value 	= "" ;
		document.getElementById('schemediscper').value 	= "" ;
		document.getElementById('rev_schemediscper').value 	= "" ;
		document.getElementById('currentstock').innerHTML 	= "" ;
		document.getElementById('currentbonusstock').innerHTML = "";
		document.getElementById('return_billedQty').value 	= "";
		document.getElementById('return_bonusQty').value 	= "";
		document.getElementById('retQty').value = "";
		document.getElementById('tax_type').value = "";
		if (document.getElementById("batch").hasAttribute("disabled")){
			document.getElementById("batch").removeAttribute("disabled");
		}
	}
	if (document.getElementById("debitNo").value != null && document.getElementById("debitNo").value != ""){
		//editing an already created debit note. Make only rates and discounts editable
		document.getElementById("medicine").setAttribute("readOnly","readOnly");
		document.getElementById("medicine").disabled='true';
		document.getElementById("medicine_dropdown").style.zIndex = -2001;
		document.getElementById("medicine_dropdown").setAttribute("onKeyPress","return handleEnter (this, event);");
		document.getElementById("medicine_dropdown").setAttribute("onHover","return handleFocus();");
		document.getElementById("medicine").setAttribute("onKeyPress","return handleEnter (this, event);");
		document.getElementById("medicine").setAttribute("onFocus","return handleFocus();");
		document.getElementById("medicine").setAttribute("onHover","return handleFocus();");
		document.getElementById("barCodeId").setAttribute("readonly","readOnly");
		setSelectedIndex(document.supplierdebitform.batch,document.getElementById("hbatchno"+id).value);
		document.getElementById("batch").setAttribute("disabled", "disabled");
		document.supplierdebitform.batch.options[0].text	= document.getElementById("hbatchno"+id).value ;
		document.getElementById('expdate').innerHTML 	= document.getElementById("hexpdtlabel"+id).innerHTML ;
		//current stock,package size is not usefull for already raised debit note,hence hiding them
		document.getElementById("stockrow").style.display = 'none';
		document.getElementById("pkgrow").style.display = 'none';
		document.getElementById('return_billedQty').value 	= document.getElementById("hretqty"+id).value;
		document.getElementById('return_bonusQty').value 	= document.getElementById("hretbonusqty"+id).value;
		document.getElementById('retQty').value 	= document.getElementById("hretqty"+id).value;
		document.getElementById("return_billedQty").setAttribute("disabled", "disabled");
		document.getElementById("return_bonusQty").setAttribute("disabled", "disabled");
//		if (prefCed == 'Y'){
//			document.getElementById('cedamt').innerHTML 	= document.getElementById('hcedamt'+id).value ;
//		}
		document.supplierdebitform.rate.value = document.getElementById('hrate'+id).value;
		document.supplierdebitform.rev_rate.value = document.getElementById('hrevratelabel'+id).innerHTML;
		document.supplierdebitform.discper.value = document.getElementById("hdiscper"+id).value ;
		document.supplierdebitform.rev_discper.value = document.getElementById("hrevdiscper"+id).value ;
		document.supplierdebitform.schemediscper.value = document.getElementById("hschemediscper"+id).value ;
		document.supplierdebitform.rev_schemediscper.value = document.getElementById("hrevschemediscper"+id).value ;
		document.supplierdebitform.tax_type.value = document.getElementById("htaxtype"+id).value ;
		document.supplierdebitform.tax_type.disabled = true;
		document.getElementById("lbltax_rate").innerHTML = isNotNullValue('hrevtaxrate', id) ? document.getElementById("hrevtaxrate"+id).value : 0 ;
		document.getElementById("debit_pkg_size").value = document.getElementById("hpkgsz"+id).value ;
		document.getElementById("mrp").value = document.getElementById("hmrp"+id).value ;
		
		
	}
	document.getElementById("lbltax_amt").innerHTML = parseFloat(getFieldText('htaxamtlabel', id)).toFixed(decDigits) ;
	for(var i=0; i < groupListJSON.length; i++) {
		var itemGroupId = groupListJSON[i].item_group_id;
		
		setFieldHtml("taxrate", parseFloat(document.getElementById(id+"taxrate"+itemGroupId).value).toFixed(decDigits), itemGroupId);
		setFieldValue("taxrate_", parseFloat(document.getElementById(id+"taxrate"+itemGroupId).value).toFixed(decDigits), itemGroupId);
	    
		setFieldHtml("taxamount", parseFloat(document.getElementById(id+"taxamount"+itemGroupId).value).toFixed(decDigits), itemGroupId);
		setFieldValue("taxamount_", parseFloat(document.getElementById(id+"taxamount"+itemGroupId).value).toFixed(decDigits), itemGroupId);
		
		setFieldValue("taxsubgroupid_", document.getElementById(id+"taxsubgroupid"+itemGroupId).value, itemGroupId);
	}
	rowEdit = 'Y';
	if ((document.getElementById("hmedId"+id) != null) && (document.getElementById("hmedId"+id).value != "")){
		document.supplierdebitform.tax_type.value = document.getElementById("htaxtype"+id).value ;
	}
	itemDialog.show();
	if (document.getElementById("debitNo").value == null || document.getElementById("debitNo").value == ""){
		if (prefBarCode == 'Y') document.supplierdebitform.barCodeId.focus();
		else document.supplierdebitform.medicine.focus();
	} else{
		document.supplierdebitform.rate.focus();
	}


}

function openAddDialog () {
	clearTaxFields();
	if ( !validateSupplier() ) return false;
	rowEdit = 'N';
	document.supplierdebitform.medicine.value = "";
	document.supplierdebitform.barCodeId.value = "";
	document.supplierdebitform.batch.value 	= "" ;
	document.getElementById('expdate').innerHTML = "" ;
	document.getElementById('rate').value 	= "" ;
	document.getElementById('mrp').value 	= "" ;
	document.getElementById('rev_rate').value 	= "" ;
	document.getElementById('discper').value 	= "" ;
	document.getElementById('rev_discper').value 	= "" ;
	document.getElementById('currentstock').innerHTML 	= "" ;
	document.getElementById('currentbonusstock').innerHTML = "";
	document.getElementById('return_billedQty').value 	= "";
	document.getElementById('return_bonusQty').value 	= "";
	document.getElementById('retQty').value = "";
	document.getElementById("debit_pkg_size").value = "0";
	if (document.getElementById("batch").hasAttribute("disabled")){
		document.getElementById("batch").removeAttribute("disabled");
	}
	if (fromedit == '') {
		button = document.getElementById("plusItem");
		itemDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		itemDialog.show();
	}
	if (prefBarCode == 'Y') document.supplierdebitform.barCodeId.focus();
	else document.supplierdebitform.medicine.focus();
}

function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 		alert("There is no assigned store, hence you dont have any access to this screen");
 		document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}
function qtyUnitSelection () {
	var valadd = getRadioSelection(document.forms[0].qty_unit);
	var valedit = document.forms[0].qty_unit.value;//alert("valadd=="+valadd);alert("valedit=="+valedit);alert("comingFromPO=="+comingFromPO);alert("comingFromGRN=="+comingFromGRN);
	var qtySel = (document.forms[0].debitNo.value == '' ?  valadd : valedit);
	return qtySel;
}
function medNamesAutoComplete () {
	if (retAgtSupp == 'Y' && fromedit != 'Y') clearGrid();
	if (retAgtSupp == 'Y') {
		ajaxReqForItem ('StoresSupplierReturnslist.do?_method=getSuppMedDetails&supp=',handleMedResponse);
	} else {
		getMedicinesForStore(medNamesAutoCompleteAllItems);
	}
}

function medNamesAutoCompleteAllItems() {
	initMedicineAutoComplete(jMedicineNames);
	itemNamesArray = jMedicineNames;
}

function getItemBarCodeDetails (val) {
	if (val == '') {
		resetMedicineDetails();
		document.supplierdebitform.medicine.value = '';
		return;
	}//alert(val + "---"+itemNamesArray.length);
	var flag = false;
	for (var m=0;m<itemNamesArray.length;m++) {
	     var item = itemNamesArray[m];//alert(val + "== "+item.ITEM_BARCODE_ID);
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, oMedAutoComp);//alert(oAutoComp);
			oMedAutoComp._selectItem(elNewItem);
	     	onSelectMedicine ();
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	resetMedicineDetails();
	 	document.supplierdebitform.medicine.value = '';
	 }
}
function ajaxReqForItem (path,method) {
	var supp = document.supplierdebitform.supplierName.value;
	var store = document.supplierdebitform.store.value;
	var ajaxReqObject = newXMLHttpRequest();
	var url = path + encodeURIComponent(supp) + "&store="+store;
	getResponseHandlerText(ajaxReqObject, method, url);
}

function handleMedResponse(responseText) {
	if (responseText == null || responseText == "") {
		var ar = [];
		ar[0] = "";
		initMedicineAutoComplete (ar);
	} else {
	    eval("medicineDeatils = " + responseText);
	    itemNamesArray = medicineDeatils;
	    initMedicineAutoComplete (itemNamesArray);
	}
}

function matches(mName, autocomplete) {
	var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
   	elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
	return elListItem;
}

function setRevDiscPer(obj){
	if ((null != obj.value) && (obj.value != "")){
		revDiscField = obj.value;
	}
	if ((document.supplierdebitform.rev_discper.value == "") || (parseFloat(document.supplierdebitform.rev_discper.value) == 0)){
		document.supplierdebitform.rev_discper.value = obj.value;
		getTaxDetails(prepareTaxObj());
	}
	return true;
}

function setSchemeRevDiscPer(obj){
	if ((null != obj.value) && (obj.value != "")){
		revSchemeDiscField = obj.value;
	}
	if ((document.supplierdebitform.rev_schemediscper.value == "") || (parseFloat(document.supplierdebitform.rev_schemediscper.value) == 0)){
		document.supplierdebitform.rev_schemediscper.value = obj.value;
		getTaxDetails(prepareTaxObj());
	}
	return true;
}

function onChangeRevDiscount(obj) {
	setRevDiscPer(obj);
	getTaxDetails(prepareTaxObj());
}

function onChangeRevSchemeDiscount(obj) {
	setSchemeRevDiscPer(obj);
	getTaxDetails(prepareTaxObj());
}

function setDialogValues(){
	var id = dialogOpened;
	if (document.getElementById("hTranType"+id).value == 'Group'){
		setSelectedIndex(document.supplierdebitform.batch,document.getElementById("hbatchno"+id).value);
		document.getElementById("batch").setAttribute("disabled", "disabled");

	}
	setSelectedIndex(document.supplierdebitform.batch,document.getElementById("hbatchno"+id).value);
	document.getElementById('expdate').innerHTML 	= document.getElementById("hexpdtlabel"+id).innerHTML ;
	document.getElementById('rate').value 	= document.getElementById("hrate"+id).value ;
	document.getElementById('rev_rate').value 	= document.getElementById("hrevrate"+id).value ;
	document.getElementById('discper').value 	= document.getElementById("hdiscper"+id).value ;
	document.getElementById('schemediscper').value 	= document.getElementById("hschemediscper"+id).value ;
	document.getElementById('rev_discper').value  	= document.getElementById("hrevdiscper"+id).value ;
	document.getElementById('rev_schemediscper').value  	= document.getElementById("hrevschemediscper"+id).value ;
	document.getElementById('currentstock').innerHTML 	= parseFloat(document.getElementById("hactqty"+id).value).toFixed(decDigits) ;
	document.getElementById('currentbonusstock').innerHTML = parseFloat(document.getElementById("hactbonusqty"+id).value).toFixed(decDigits) ;
	document.getElementById('return_billedQty').value 	= document.getElementById("hretqty"+id).value;
	document.getElementById('return_bonusQty').value 	= document.getElementById("hretbonusqty"+id).value;
	document.getElementById('retQty').value 	= document.getElementById("hretqty"+id).value;
	document.supplierdebitform.barCodeId.value = document.getElementById('hitembarcode'+id).value;
	document.supplierdebitform.tax_type.value 	= document.getElementById("htaxtype"+id).value;
	if(document.getElementById("hitemidentification"+id).value != 'B') {
		document.supplierdebitform.return_billedQty.readOnly = true;
		document.supplierdebitform.return_bonusQty.readOnly = true;
	}
//	if (prefCed == 'Y'){
//		document.getElementById('cedamt').innerHTML 	= document.getElementById('hcedamt'+id).value ;
//	}
	if ((document.getElementById("hindentno"+id) != null) && (document.getElementById("hindentno"+id).value != "")
	&&  (document.getElementById("hindentno"+id).value != "undefined")){
		document.supplierdebitform.medicine.readOnly = true;
		document.supplierdebitform.batch.disabled = true;
		document.supplierdebitform.return_billedQty.readOnly = true;
		document.supplierdebitform.return_bonusQty.readOnly = true;
	}
	dialogOpened = 0;

}

function getMedicinesForStore(onCompletionFunction) {
	// get the medicine time stamp for this store: required for fetching the items.
	var storeId=document.supplierdebitform.store.value;
	var url = cpath + "/stores/utils.do?_method=getStoreStockTimestamp&storeId=" + storeId;
	YAHOO.util.Connect.asyncRequest('GET', url, {
			success: onGetStockTimestamp,
			argument: onCompletionFunction,
		}
	);
}

function onGetStockTimestamp(response) {
	if (response.status != 200)
		return;

	var ts = parseInt(response.responseText);
	var storeId=document.supplierdebitform.store.value;
	if (storeId == '')
		return;
	
	var url = cpath + "/pages/stores/getMedicinesInStock.do?ts=" + ts + "&hosp=" + sesHospitalId +
		"&includeUnapproved=Y=" +
		"&storeId=" + storeId;
	if (fromedit != 'Y') {
		var grnNo = document.supplierdebitform.grn_no.value;
		if (grnNo != null && grnNo != "")
			url = url + "&grnNo="+grnNo;

	}
	// Note that since this is a GET, the results could potentially come from the browser cache.
	// This is desirable.
	YAHOO.util.Connect.asyncRequest('GET', url, { success: onGetStoreStock, argument: response.argument });
}

function onGetStoreStock(response) {
	if (response.status != 200)
		return;

	eval(response.responseText);		// response is like var jMedicineNames = [...];
	// overwrite the global object.
	window.jMedicineNames = jMedicineNames;

	// the function to be called after the fetch.
	var completionFunction = response.argument;
	if (completionFunction)
		completionFunction();
}

function onClickPurchaseDetails() {
	var medId = document.supplierdebitform.medicineId.value;
	var storeId = document.supplierdebitform.store.value;
	if (!medId) {
		showMessage("js.stores.supplier.debit.selectitemforshowingpurchasedetails");
		return false;
	} else {
		showPurchaseDetails(medId,storeId);
		return false;
	}
}

function recalcDialogCedAmt(cedTaxAmt,packageUnit,qty){
	
	document.getElementById('cedamt').innerHTML = (gGrnStock <= 0 ? 0 : parseFloat((cedTaxAmt)/gGrnStock).toFixed(decDigits));
	
}

function initAddEditDialog() {
	itemDialog = new YAHOO.widget.Dialog("itemDialog", {
        width: "800px",
        context: ["plusItem", "tr", "br"],
        visible: false,
        modal: true,
        constraintoviewport: true
    });
    var escKeyListener = new YAHOO.util.KeyListener("itemDialog",
			{keys: 27 }, handleitemDialogCancel);
    itemDialog.cfg.queueProperty("keylisteners", escKeyListener);
    itemDialog.render();
}

function changeReturnType() {

	var returnAgainst = getRadioSelection(document.supplierdebitform.returnAgainst);
	if(returnAgainst == 'grnReturn') {
		document.supplierdebitform.grn_no.value = "";
		document.supplierdebitform.grn_no.disabled = false;
		document.supplierdebitform.store.value = "";
		document.supplierdebitform.store.disabled = true;
		document.supplierdebitform.supplierName.disabled = true;
		//if ( prefVAT == 'Y' )
		//document.getElementById('cstRateId').style.display = 'none';

		clearGrnAutofilledField();
		clearGrid();
		clearOtherFields();
	} else if (returnAgainst == 'withoutGrnReturn') {
		document.supplierdebitform.grn_no.value = "";
		document.supplierdebitform.grn_no.disabled = true;
		document.supplierdebitform.supplierName.disabled = false;
		
		//if ( prefVAT == 'Y' ) {
		document.getElementById('tax_td').style.display = 'table-cell';
		//document.getElementById('cstRateId').style.display = 'table-cell';
		document.getElementById('taxNAlbl').style.display = 'none';
		//}
			
		clearGrnAutofilledField();
		clearGrid();
		clearOtherFields();
//		if ( prefVAT == 'Y' )
//			enableTaxDefaults();
	}

}

function getGrnDetails() {

	var grnNo = document.supplierdebitform.grn_no.value;
	var storeId=document.supplierdebitform.store.value;
	
	clearGrnAutofilledField();
	clearGrid();
	clearOtherFields();
	if( !grnNo ) {
		showMessage("js.stores.supplier.debit.grnnodoesntexists");
		setTimeout("document.supplierdebitform.grn_no.focus()",100);
		return;
	}
	
	var ajaxReqObject = newXMLHttpRequest();
	var url="StoresSupplierReturnslist.do?_method=getGrnDetailsJSON&grnNo="+encodeURIComponent(grnNo)+"&storeId="+storeId+"&saleType=supReturn";
	ajaxReqObject.open("GET",url.toString(), false);
	ajaxReqObject.send(null);

	if (ajaxReqObject.readyState == 4 && ajaxReqObject.status == 200) {
			handleGrnDetailsResponse(ajaxReqObject.responseText);
	}
}

var taxNotApplicable = false;
var cFormApplicable = false;
var taxRateFormGRN = false;
function handleGrnDetailsResponse(response) {
	
	if (response == null) {
		showMessage("js.stores.supplier.debit.grnnodoesntexists");
		setTimeout("document.supplierdebitform.grn_no.focus()",100);
		return;
	}
	
	var grnDetails = eval("(" + response + ")");
	if (grnDetails.length == 0) {
		showMessage("js.stores.supplier.debit.grnnodoesntexists");
		setTimeout("document.supplierdebitform.grn_no.focus()",100);
		return;
	}
	
	taxNotApplicable = false;
	document.supplierdebitform.supplierName.value = grnDetails[0].supplier_name;
	document.supplierdebitform.supplier_name.value = grnDetails[0].supplier_name;
	document.supplierdebitform.store.value		  = grnDetails[0].store_id;
	document.supplierdebitform.store.disabled = true;
	var supplierCode = '';
	for ( var i=0; i<jAllSuppliers1.length; i++){
		if (grnDetails[0].supplier_name == jAllSuppliers1[i].SUPPLIER_NAME){
			supplierCode = jAllSuppliers1[i].SUPPLIER_CODE;
			break;
		}
	}
	document.supplierdebitform.supplier_code.value = supplierCode;
	//document.getElementById('suppAddId').textContent = grnDetails[0].supplier_address;
	setNodeText(document.getElementById('suppAddId').parentNode, grnDetails[0].supplier_address, 60, grnDetails[0].supplier_address);
	cFormApplicable = grnDetails[0].c_form ? true : false ;
	//if ( prefVAT == 'Y' ){
		if (grnDetails[0].tax_name == 'NA'){
			//tax is not applicable for unrigered supplier in a supplier tax rules yes schema
			document.getElementById('tax_td').style.display = 'none';
			//document.getElementById('cstRateId').style.display = 'none';
			document.getElementById('taxNAlbl').style.display = 'block';
			taxNotApplicable = true;
		}else if (grnDetails[0].tax_name == 'CST' || grnDetails[0].tax_name == 'iGST') {
			document.getElementById('tax_td').style.display = 'table-cell';
			document.getElementById('taxNAlbl').style.display = 'none';
			//document.getElementById("cst").checked = true;
			taxRateFormGRN = true;
			//enableTaxDefaults();
		}else {
			document.getElementById('tax_td').style.display = 'table-cell';
			document.getElementById('taxNAlbl').style.display = 'none';
			//document.getElementById("vat").checked = true;
			//enableTaxDefaults();
		}
	//}
	getMedicinesForStore(medNamesAutoCompleteAllItems);
	
}

function clearGrnAutofilledField() {

	document.supplierdebitform.supplierName.value = "";
	document.supplierdebitform.store.value		  = "";
	document.supplierdebitform.store.disabled = false;
	document.getElementById('suppAddId').textContent = "";
//	if ( prefVAT == 'Y' ){
//		document.getElementById("vat").checked = true;
//		document.supplierdebitform.cstrate.value = "";
//		document.supplierdebitform.cstrate.readOnly = true;
//	}
	
}
function clearOtherFields() {

	document.getElementById("itemleveldisc").value = 0;
	document.getElementById("discount").value = formatAmountValue(0,false,decDigits);
	document.getElementById("otherdisc").value = "";
	document.getElementById("otherCharges").value = "";
	document.getElementById("roundAmt").value = "";
	document.getElementById("netAmtPayble").value = "";
	document.getElementById("roundAmt").value = "";
	document.getElementById("recDebitAmt").value = "";
	document.getElementById("otherrevdisc").value = "";
	document.getElementById("remarks").value="";
	document.getElementById("otherDescription").value = "";
	document.getElementById("gatepass").checked = false;
	document.supplierdebitform.discType.value = defaultDiscType != '' ? defaultDiscType :'P';
	document.supplierdebitform.status.value = defaultStatus != '' ? defaultStatus :'O';
	document.getElementById('store').value = gDefaultOnloadStore;

}


function getTaxDetails(reqObj) {
	clearTaxFields();
	if(reqObj && ((reqObj.billed_qty_display && reqObj.billed_qty_display != null && reqObj.billed_qty_display != '' && reqObj.billed_qty_display != '0' && reqObj.billed_qty_display != 0) || 
			(reqObj.bonus_qty_display && reqObj.bonus_qty_display != null && reqObj.bonus_qty_display != '' && reqObj.bonus_qty_display != '0' && reqObj.bonus_qty_display != 0))) {
		var url = cpath + "/stocks/debitnotetaxdetails.json";
		var response = ajaxFormObj(reqObj, url, false);
		var taxRate = 0;
		var taxAmt = 0;
		if (response != undefined && response.tax_details != undefined && response.tax_details != null) {
			var taxMap = response.tax_details;
			var vatRate = 0;
			var vatAmt = 0;
			var adj_mrp = 0;
			for(var i=0; i<taxMap.length; i++) {
			    for(var j=0; j < subgroupNamesList.length; j++) {
			    	if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id] && taxMap[i][subgroupNamesList[j].item_subgroup_id] != null) {
			    		var val = taxMap[i][subgroupNamesList[j].item_subgroup_id];
			    		var itemGroupId = subgroupNamesList[j].item_group_id;
						setFieldHtml("taxrate", parseFloat(val.rate).toFixed(decDigits), itemGroupId);
						setFieldValue("taxrate_", parseFloat(val.rate).toFixed(decDigits), itemGroupId);
					    
						setFieldHtml("taxamount", parseFloat(val.amount).toFixed(decDigits), itemGroupId);
						setFieldValue("taxamount_", parseFloat(val.amount).toFixed(decDigits), itemGroupId);
						
						vatAmt += parseFloat(parseFloat(val.amount).toFixed(decDigits));
						setFieldValue("taxsubgroupid_", subgroupNamesList[j].item_subgroup_id, itemGroupId);
					    
					    vatRate = vatRate + parseFloat(val.rate);
			    	}
				}
			}
			document.getElementById("tax_rate").value = vatRate;
			document.getElementById("tax").value = vatAmt;
			setFieldHtml("lbltax_rate", parseFloat(vatRate).toFixed(decDigits));
			setFieldHtml("lbltax_amt", parseFloat(vatAmt).toFixed(decDigits));
		}
	}
}

function getExistGrnTaxDetails(medicineName, storeId, supp, grnNo, itemBatchId) {
	
	var url="StoresSupplierReturnslist.do?_method=getItemTaxDetails&medicineName="+encodeURIComponent(medicineName)+"&storeId="+storeId+"&supp="+encodeURIComponent(supp)+"&saleType=supReturn";
	if (grnNo != "" && grnNo != null)
		url = url+"&grnNo="+encodeURIComponent(grnNo);
	url = url+"&item_batch_id="+itemBatchId;
	
	var taxDetails = {
			taxRate : 0,
			taxAmt: 0
	};
	
	var ajaxReqObject = newXMLHttpRequest();
    ajaxReqObject.open("GET",url.toString(), false);
    ajaxReqObject.send();
    if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null && ajaxReqObject.responseText != '' && ajaxReqObject.responseText != undefined)) {
			taxMap = JSON.parse(ajaxReqObject.responseText);
			var taxRate = 0;
			var taxAmt = 0;
			for(var i=0; i<taxMap.length; i++) {
			    for(var j=0; j < subgroupNamesList.length; j++) {
			    	if(taxMap[i] && taxMap[i] != null && taxMap[i].item_subgroup_id == subgroupNamesList[j].item_subgroup_id) {
			    		var val = taxMap[i];
			    		var itemGroupId = subgroupNamesList[j].item_group_id;
						
						taxAmt += parseFloat(parseFloat(val.tax_amt).toFixed(decDigits));
						setFieldValue("taxsubgroupid_", subgroupNamesList[j].item_subgroup_id, itemGroupId);
					    
						taxRate = taxRate + parseFloat(val.tax_rate);
			    	}
				}
			}
			taxDetails.taxRate = taxRate;
			taxDetails.taxAmt = taxAmt;
		}
	}
    return taxDetails;
}

function clearTaxFields() {
	setFieldHtml("lbltax_rate", parseFloat(0).toFixed(decDigits));
	setFieldHtml("lbltax_amt", parseFloat(0).toFixed(decDigits));
	document.getElementById("tax_rate").value = 0;
	document.getElementById("tax").value = 0;
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

function clearTaxSubgroupsFields() {
	setFieldHtml("lbltax_rate", parseFloat(0).toFixed(decDigits));
	setFieldHtml("lbltax_amt", parseFloat(0).toFixed(decDigits));
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
		if(isNotNullValue("taxsubgroupid_", p)){
			setFieldValue("taxsubgroupid_", '', p);
		}
	}
}

function prepareTaxObj() {
	var reqObj = {
		mrp_display : 0,
		billed_qty_display:0,
		bonus_qty_display:0,
    	cost_price_display:0,
    	discount:0,
    	scheme_discount:0,
    	grn_pkg_size:0,
    	item_subgroup_id:'',
    	tax_type:'',
    	store_package_uom:'',
    	store_id_hid:'',
    	supplier_code_hid:'',
    	medicine_id:''
    };
	
	var storePkgSize = document.getElementsByName('qty_unit');
	var store_package_uom;
	for(var i = 0; i < storePkgSize.length; i++){
	    if(storePkgSize[i].checked){
	    	store_package_uom = storePkgSize[i].value;
	        break;
	    }
	}
	if(store_package_uom == undefined || store_package_uom =='undefined' || store_package_uom == null || store_package_uom == 'null') {
		store_package_uom = document.getElementsByName('qty_unit')[0].value;
	}
	
	reqObj.cost_price_display = getFieldValue('rev_rate');
	reqObj.billed_qty_display = getFieldValue('return_billedQty');
	reqObj.bonus_qty_display = getFieldValue('return_bonusQty');
	reqObj.discount = (getFieldValue('rev_discper')*getFieldValue('return_billedQty')*getFieldValue('rev_rate'))/100;
	reqObj.scheme_discount = (getFieldValue('rev_schemediscper')*getFieldValue('rev_rate'))/100;
	reqObj.mrp_display = getFieldValue('mrp');
	reqObj.grn_pkg_size = document.getElementById('item_unit').textContent;
	reqObj.store_package_uom = store_package_uom;
	reqObj.store_id_hid = document.getElementById('store').value;
	reqObj.supplier_code_hid = document.supplierdebitform.supplier_code.value;
	reqObj.tax_type = document.supplierdebitform.tax_type.value;
	reqObj.medicine_id = getFieldValue('medicineId');
	var itemSubGroupArray = new Array();
	for(var i=0; i < groupListJSON.length; i++) {
		var p = groupListJSON[i].item_group_id;
		if(isNotNullValue("taxsubgroupid_", p)){
			var subGroupId = getFieldValue("taxsubgroupid_", p);
			if(subGroupId && subGroupId != 0 && subGroupId != '0')
				itemSubGroupArray.push(subGroupId);
		}
	}
	reqObj.item_subgroup_id = itemSubGroupArray;
	if(reqObj.grn_pkg_size == undefined || reqObj.grn_pkg_size =='undefined' || reqObj.grn_pkg_size == null || reqObj.grn_pkg_size == 'null' || reqObj.grn_pkg_size == 0 || reqObj.grn_pkg_size == '0') {
		reqObj.grn_pkg_size = document.getElementById('debit_pkg_size').value;
	}
	return reqObj;
}

function onChangeTaxType(val) {
	displayMedicineDetails(val,null);
	getTaxDetails(prepareTaxObj());
}