var gMedicineDetails = [];
var oAutoComp;
var itemNamesArray = '';
function initSupplierAutoComplete() {
	var supplierNames = [];
	var j =0;
	if(centerId == 0) {
		var dataSource = new YAHOO.widget.DS_JSArray(jAllSuppliers);
	} else {    			
        var dataSource = new YAHOO.widget.DS_JSArray(jCenterSuppliers);
	}
	dataSource.responseSchema = {
    		resultsList : "result",
    		fields : [  {key : "SUPPLIER_NAME_WITH_CITY"}, {key : "SUPPLIER_CODE"}, {key : "SUPPLIER_NAME"} ]
    	};
	oAutoComp = new YAHOO.widget.AutoComplete('supplierName', 'suppliername_dropdown', dataSource);
	oAutoComp.maxResultsDisplayed = 10;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp.unmatchedItemSelectEvent.subscribe(clearSupplierAddress);
	//oAutoComp.textboxChangeEvent.subscribe(onSelectSupplier);
	oAutoComp.itemSelectEvent.subscribe(onSelectSupplier);

}

function clearSupplierAddress() {
    document.getElementById('suppAddId').textContent = '';
}

function onSelectSupplier(type,args) {

	var suppId = '';
	var supplierAddress = '';
	var selSupplierName = args[2][2];
	document.supplierreturnsform.supplierName.value = selSupplierName;
	if ( selSupplierName == '')
		document.getElementById('suppAddId').textContent = '';
	else {
		for ( var i=0; i<jAllSuppliers.length; i++){
			if (selSupplierName == jAllSuppliers[i].SUPPLIER_NAME){
				suppId = jAllSuppliers[i].SUPPLIER_CODE;
				supplierAddress = jAllSuppliers[i].SUPPLIER_ADDRESS;
				break;
			}
		}
	}

	var supplier = filterList(jAllSuppliers, "SUPPLIER_CODE", suppId);
	//document.getElementById('suppAddId').textContent = supplier[0].SUPPLIER_ADDRESS;
	setNodeText(document.getElementById('suppAddId').parentNode, supplier[0].SUPPLIER_ADDRESS, 60, supplier[0].SUPPLIER_ADDRESS);

	medNamesAutoComplete();
}




function selectStore(){
    //initMedicineAutoComplete(jMedicineNames[document.supplierreturnsform.store.value]);
    initSupplierAutoComplete();
    medNamesAutoComplete();
    initItemGroupDialog();
}
function init () {
	selectStore();
	addGroupMedDetails();
	checkstoreallocation();
	document.supplierreturnsform.supplierName.focus();
}
var oMedAutoComp;
function initMedicineAutoComplete(medArray) {
   	if (oMedAutoComp != undefined) {
		oMedAutoComp.destroy();
	}
	var dataSource = new YAHOO.widget.DS_JSArray(medArray);
			 dataSource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "cust_item_code_with_name"},{key : "medicine_name"} ]
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
	oMedAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oMedAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	oMedAutoComp.itemSelectEvent.subscribe(onSelectMedicine);
}

/*
 * Response handler for the ajax call to retrieve medicine details like mfr and batches
 */
var gExpDate;		// store the current exp in a global
var gStock;	        // store the latest retrieved stock in a global
var gMrp;            // store the latest mrp  in a global
var gCurrentStock;    // store the latest currentstock  in a global
var flag=false;

function onSelectMedicine(type,args) {

	var medicineName = '';
	if ( args == undefined ){
		medicineName = document.supplierreturnsform.medicine.value;
	} else {
		medicineName = args[2][1];
	}
	document.supplierreturnsform.medicine.value = medicineName;
	var storeId=document.supplierreturnsform.store.value;
	var supp = document.supplierreturnsform.supplierName.value;
	if (medicineName == "") {
		resetMedicineDetails();
	} else {
		var ajaxReqObject = newXMLHttpRequest();
		var url="StoresSupplierReturnslist.do?_method=getStockJSON&medicineName="+encodeURIComponent(medicineName)+"&storeId="+storeId+"&supp="+encodeURIComponent(supp)+"&saleType=supReturn";
		getResponseHandlerText(ajaxReqObject, handleMedicineStockResponse, url);
	}
}



function handleMedicineStockResponse(responseText) {
	 if (responseText==null) return;
	if (responseText=="") return;
	flag=false;
    eval("gStock = " + responseText);
    var medicineId = 0;

	if (gStock.length > 0) {


		var medicineName = document.supplierreturnsform.medicine.value;
		var batchSel = document.supplierreturnsform.batch;
		batchSel.disabled = true;
		var selBatchIndex = document.supplierreturnsform.batch.value;
		var index = 0;

		if (groupMedDetails != null) {
			for(var i=0;i<groupMedDetails.length;i++){
				var existingMed = groupMedDetails[i].medicine_name;
				var existingBatch = groupMedDetails[i].batch_no;
				var batchNo = document.supplierreturnsform.batch.value;
				if ((medicineName == existingMed) && (existingBatch == batchNo)){
					//this is an update
					var tempStock = [];
					for (var j=0; j<gStock.length; j++){
						var item = gStock[j];
						if ( batchNo == item.batchNo ) {
							tempStock[0] =  gStock[j];
							break;
						}
					}
					gStock = tempStock;
					break;
				}
			}
		}

		if (gStock.length > 1) {
			batchSel.length = gStock.length + 1;
			batchSel.options[index].text = "..Select Batch..";
			batchSel.options[index].value = "";
			index++;
			clearLabels();
		} else {

		    batchSel.length = gStock.length + 1;

		    batchSel.options[index].text = "..Select Batch..";
			batchSel.options[index].value = "";
			index++;
			flag=true;


		}

		for (var i=0; i<gStock.length; i++){
			var item = gStock[i];
			medicineId = item.medicineId;
			batchSel.options[index].text = item.batchNo ;
			batchSel.options[index].value = item.itemBatchId;
			if ((null != selBatchIndex) && (selBatchIndex != '')){
				batchSel.selectedIndex = index;
			}
			index++;
		}
		var dialog = document.getElementById("dialogId").value;
		if (dialog != '') {
			document.supplierreturnsform.batch.value = document.getElementById("itemBatchId"+dialog).value ;
			displayMedicineDetails();
		}
		if (flag) {
			batchSel.selectedIndex = 1;
			displayMedicineDetails();
		}
		// success: move to the next selectable item
		if (gStock.length > 1) {
			document.supplierreturnsform.batch.disabled = false;
			document.supplierreturnsform.batch.focus();
		} else {
			document.supplierreturnsform.batch.disabled = false;
			document.supplierreturnsform.returnQty.focus();
		}
		document.supplierreturnsform.barCodeId.value = item.itemBarcode;

	} else {
		alert("Error retrieving stock details for item");
		resetMedicineDetails();
	}

	gMedicineDetails[medicineId] = gStock;
}
function displayMedicineDetails(){

        var label1 = document.getElementById('expdate');
		var label2 = document.getElementById('mrp');
		var label3 = document.getElementById('currentstock');
		var label4 = document.getElementById('issueUnitsLabel');
		var sBatchNo=document.supplierreturnsform.batch.options[document.supplierreturnsform.batch.selectedIndex].text;
        document.supplierreturnsform.returnQty.focus();
        for (var i=0; i<gStock.length; i++){
       		var item = gStock[i];
        	if(sBatchNo==item.batchNo){

        		if (label1){
					label1.innerHTML = formatExpiry(item.expDt);
					gExpDate = formatExpiry(item.expDt);
				}
				var selQty = qtyUnitSelection ();
				if (label2){
					if (selQty == 'I') label2.innerHTML = parseFloat(item.mrp/item.packageUnit).toFixed(decDigits);
					else label2.innerHTML = parseFloat(item.mrp).toFixed(decDigits);
				}
				gMrp = (selQty == 'I') ? parseFloat(item.mrp/item.packageUnit).toFixed(decDigits) : parseFloat(item.mrp).toFixed(decDigits);
				if (label3) {
					if (selQty == 'I') label3.innerHTML = item.qty;
					else label3.innerHTML = parseFloat(item.qty/item.packageUnit).toFixed(2);
				}
				if (label4){
					label4.innerHTML = item.issue_units;
				}
				gCurrentStock = (selQty == 'I') ? item.qty : parseFloat(item.qty/item.packageUnit).toFixed(2);
				document.getElementById('item_unit').textContent = selQty == 'I' ? item.issueUnits : item.packageUOM;
				if(item.identification == 'S'){
	  				document.supplierreturnsform.returnQty.value = '1';
	  				document.supplierreturnsform.returnQty.readOnly = true;
				}else {
	   				document.supplierreturnsform.returnQty.readOnly = false;
	   			}
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
	clearLabels();
	document.supplierreturnsform.batch.options[0].selected=true;
	document.supplierreturnsform.batch.length=1;
	document.supplierreturnsform.medicine.value = '';
	document.supplierreturnsform.barCodeId.value = '';
	document.supplierreturnsform.returnQty.value = 0;
	document.getElementById('item_unit').textContent = '';
	if (prefBarCode == 'Y') document.supplierreturnsform.barCodeId.focus();
	else document.supplierreturnsform.medicine.focus();
}
function changeStore(){
	clearGrid();
 	medNamesAutoComplete ();
}

 function clearLabels () {
 	var label1 = document.getElementById('expdate');
	var label2 = document.getElementById('mrp');
	var label3 = document.getElementById('currentstock');
	var label4 = document.getElementById('issueUnitsLabel');
	if (label1)
	label1.innerHTML = "";
	if (label2)
	label2.innerHTML = "";
	if (label3)
	label3.innerHTML = "";
	if (label4)
	label4.innerHTML = "";
 }

 /*
 * Deleteing values from the grid
 */
function clearGrid() {

	var itemDetailsTableObj = document.getElementById("medList");
	if (itemDetailsTableObj.rows.length>1) {
		var itemDetailsTableLength=itemDetailsTableObj.rows.length-1;
		for(d=itemDetailsTableLength;d>=1;d--){
			itemDetailsTableObj.deleteRow(d);
		}
		extraRow ("");
		resetMedicineDetails();
	}
	document.supplierreturnsform.medicine.value="";
	document.supplierreturnsform.batch.options[0].selected=true;
	document.supplierreturnsform.batch.length=1;
	document.supplierreturnsform.barCodeId.value = '';
	//document.getElementById('expdate').innerHTML = "";
	//document.getElementById('mrp').innerHTML = "";
	//document.getElementById('currentstock').innerHTML = "";
	//document.supplierreturnsform.adjtype.options[0].selected=true;
	//document.supplierreturnsform.addAdjQty.value="";

}

function onAddMedicine() {
	if(screenValidation()==false)
  	  return;
    if(stockValidation()==false)
      return;

     var dialognum = document.supplierreturnsform.dialogId.value;

     if(document.getElementById("medList").rows.length-1>1){
	     for(var i=1;i<=document.getElementById("medList").rows.length-2;i++){
	     var currentbatchNo= gStock[document.supplierreturnsform.batch.selectedIndex-1].batchNo;
	     var currentMedicineId=gStock[document.supplierreturnsform.batch.selectedIndex-1].medicineId;

	     if (i != dialognum){
		     if((currentbatchNo==document.getElementById("itemBatchId"+i).value)&&(currentMedicineId==document.getElementById("hmedId"+i).value)){
			     alert("Duplicate Entry");
			     return false;
			     break;

			  }
		}
	  }
    }

	var batchIndex = document.supplierreturnsform.batch.selectedIndex;
	if (document.supplierreturnsform.batch.length > 1) {
		// there is also a ..Select.. option in this case
		batchIndex -= 1;
	}
	var medicineId = gStock[batchIndex].medicineId;
	var batchNo = gStock[batchIndex].batchNo;
	var itemBatchId = gStock[batchIndex].itemBatchId;
	var expDt = gExpDate;
	var batch = document.supplierreturnsform.batch.options[document.supplierreturnsform.batch.selectedIndex].text;
	var medicineName = document.supplierreturnsform.medicine.value;
	var selQty = qtyUnitSelection ();
	var mrp = (selQty == 'I') ? parseFloat(gStock[batchIndex].mrp/gStock[batchIndex].packageUnit).toFixed(decDigits): parseFloat(gStock[batchIndex].mrp).toFixed(decDigits);
	var pkgsize = gStock[batchIndex].packageUnit;
	var manf = gStock[batchIndex].manfName;
	var qty = document.supplierreturnsform.returnQty.value;
	var itemIdentification = gStock[batchIndex].identification;
	var issueUnits = selQty == 'I' ? gStock[batchIndex].issueUnits : gStock[batchIndex].packageUOM;
	var indentno = "";
	addToInnerHTML(medicineName,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,expDt,gCurrentStock,qty,indentno,itemIdentification, issueUnits);
	var dialogId  = document.getElementById("dialogId").value;
	getItemGroupDialog(eval(dialogId)+1);
}

function addToInnerHTML(medicineName,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,expDt,gCurrentStock,qty,indentno,itemIdentification,issueUnits){

	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length;
	var dialogId  = document.getElementById("dialogId").value;
	var id = numRows - 1;
	var cell;
	if (medicineName != null){
		document.getElementById('medlabel'+dialogId).innerHTML = medicineName;
		document.getElementById('hmedId'+dialogId).value = medicineId;
		document.getElementById('hmedName'+dialogId).value = medicineName;
		document.getElementById('batchlabel'+dialogId).innerHTML = batchNo;
		document.getElementById('itemBatchId'+dialogId).value = itemBatchId;
		document.getElementById('manflabel'+dialogId).innerHTML = manf;
		document.getElementById('mrplabel'+dialogId).innerHTML = mrp;
		document.getElementById('hpkgsz'+dialogId).value = pkgsize;
		document.getElementById('expdtlabel'+dialogId).innerHTML = expDt;
		document.getElementById('hexpdt'+dialogId).value = expDt;
		document.getElementById('currentstklabel'+dialogId).innerHTML = gCurrentStock;
		document.getElementById('hactqty'+dialogId).value = gCurrentStock;
		document.getElementById('qtylabel'+dialogId).innerHTML = qty;
		document.getElementById('hindentno'+dialogId).innerHTML = indentno;
		document.getElementById('issueUnitsLabel'+dialogId).innerHTML = issueUnits;
		document.getElementById('hretqty'+dialogId).value = qty;
		document.getElementById('hitemidentification'+dialogId).value = itemIdentification
		document.getElementById('hitembarcode'+dialogId).value = document.supplierreturnsform.barCodeId.value;

		document.getElementById('hindentno'+dialogId).value = indentno;

		var delButton = document.getElementById("itemCheck"+dialogId);
		if(delButton.firstChild == null){
			var imgbutton = makeImageButton('imgDelete','imgDelete'+dialogId,'deleteIcon',popurl+'/icons/delete.gif');
			imgbutton.setAttribute('onclick','deleteItem(this,'+dialogId+')');
			delButton.appendChild(imgbutton);
		}

		var editButton = document.getElementById('add'+dialogId);
		if (indentno == ""){
			editButton.setAttribute("src",popurl+'/icons/Edit.png');
		} else{
			//in case we are coming here from Rejected Transfer Indent, we do not want them to update qty and other details
			editButton.setAttribute("src",popurl+'/icons/Edit1.png');
		}
		var editBut =  document.getElementById('addBut'+dialogId);
		editBut.setAttribute("title", "Edit Item");
		editBut.setAttribute("accesskey", '');

	}
	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
	if (nextrow == null){
		extraRow(indentno);
	}

	resetMedicineDetails();


}
function extraRow (indentno) {
	    var itemListTable = document.getElementById("medList");
	    var numRows = itemListTable.rows.length;
	    var row = itemListTable.insertRow(numRows);
		numRows = itemListTable.rows.length;
		id = numRows - 1;
		var tabRow = "tableRow" + id;
		row.id = tabRow;
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="medlabel'+id+'"></label>' +
			'<input type="hidden" name="hmedId" id="hmedId'+id+'" value=""> <input type="hidden" name="hmedName" id="hmedName'+id+'" value="">'+
			'<input type="hidden" name="hitemidentification" id="hitemidentification'+id+'" value="">' +
			'<input type="hidden" name="hitembarcode" id="hitembarcode'+id+'" value="">';

		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="manflabel'+id+'"></label>';
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="batchlabel'+id+'"></label>' +
			'<input type="hidden" name="itemBatchId" id="itemBatchId'+id+'" value="">';
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="mrplabel'+id+'"></label>' +
			'<input type="hidden" name="hpkgsz" id="hpkgsz'+id+'" value="">';
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="expdtlabel'+id+'"></label>'+
			'<input type="hidden" name="hexpdt" id="hexpdt'+id+'" value="">';
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="currentstklabel'+id+'"></label>' +
			'<input type="hidden" name="hactqty" id="hactqty'+id+'" value="">';
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="qtylabel'+id+'"></label>' +
			'<input type="hidden" name="hindentno" id="hindentno'+id+'" value="">'+
			'<input type="hidden" name="hretqty" id="hretqty'+id+'" value="">';
		//var imgbutton = makeImageButton('add','add'+dialogId,'editIcon',popurl+'/icons/Edit.png');
		//imgbutton.setAttribute('onclick',"getItemGroupDialog('+id+')");
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML = '<label id ="issueUnitsLabel'+id+'"></label>' ;
		cell = row.insertCell(-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell.innerHTML='<label id="itemCheck'+id+'"></label>'+
				    '<input type="hidden" name="hdeleted" id="hdeleted'+id+'"  value="false">';
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		cell = row.insertCell (-1);
		cell.setAttribute("style","width:10em;padding-left: 0.5em;");
		if (indentno==""){
			cell.innerHTML = '<button class="imgButton" name="addBut" id=addBut'+id+' onclick="getItemGroupDialog('+id+'); return false;" accesskey="+" title="Add New Item (Alt_Shift_+)">'+
			'<img class="button" name="add"  id="add'+id+'"  style="cursor:pointer" src="'+popurl+'/icons/Add.png">'+
			'</button>' ;
		} else{
			cell.innerHTML = '<button class="imgButton" name="addBut" id=addBut'+id+' onclick="getItemGroupDialog('+id+'); return false;" accesskey="+" title="Add New Item (Alt_Shift_+)">'+
			'<img class="button" name="add"  id="add'+id+'"  style="cursor:pointer" src="'+popurl+'/icons/Add1.png">'+
			'</button>' ;
		}

}
function screenValidation(){


   if(document.supplierreturnsform.supplierName.value == ''){
	   alert("Select Supplier");
	   document.supplierreturnsform.supplierName.focus();
	   return false
   }
   if(document.supplierreturnsform.returnType.options.selectedIndex == 3){
	   if(document.getElementById("remarks").value==""){
		   alert("Enter Remarks");
		   document.supplierreturnsform.remarks.focus();
		   return false
       }
   }
   if(document.supplierreturnsform.medicine.value==""){
	   alert("Enter Item");
	   document.supplierreturnsform.medicine.focus();
	   return false;
   }
   if(document.getElementById("batch").selectedIndex==0){
	   alert("Select Batch/Serial No");
	   document.supplierreturnsform.batch.focus();
	   return false
   }
   if (!isAmount(document.supplierreturnsform.returnQty.value)) {
	   	alert("Please enter only Numerics");
	   	document.supplierreturnsform.returnQty.value='';
	   	document.supplierreturnsform.returnQty.focus();
	   	return false;
   }
   if(document.supplierreturnsform.returnQty.value=="" || document.supplierreturnsform.returnQty.value=="0"){
	   alert("Enter Non-zero Return Quantity ");
	   document.supplierreturnsform.returnQty.focus();
	   return false;
   }
	if (!isValidNumber(document.getElementById("returnQty"), qtyDecimal)) return false;

    return true
}
function stockValidation(){
  	var currentStockStatus=gCurrentStock-document.getElementById("returnQty").value;
 	if(currentStockStatus<0){
	 	alert("Requested quantity can't be Returnable")
	 	document.getElementById("returnQty").focus();
		document.getElementById("returnQty").value="";
		return false;
   	}
   return true;
}


function deleteItem(imgObj, rowId) {
	var itemListTable = document.getElementById("medList");
	var row = itemListTable.rows[rowId];
	var deletedInput = document.getElementById('hdeleted'+rowId);
	if (deletedInput.value == 'false') {
		deletedInput.value = 'true';
		YAHOO.util.Dom.get('add1').disabled = true;
		document.getElementById(imgObj.id).src = popurl+"/icons/undo_delete.gif";
		row.className = "deleted";
		updateEdit(document.getElementById("addBut"+rowId),'disabled');
	} else {
		deletedInput.value = 'false';
		row.className = "";
		//document.getElementById("btnAddItems"+rowId).readOnly = false;
		//YAHOO.util.Dom.get('btnAddItems'+rowId).disabled = false;
		document.getElementById(imgObj.id).src = popurl+"/icons/delete.gif";
		updateEdit(document.getElementById("addBut"+rowId),'enabled');
	}
}
function onchangeQty (rowId) {
	var actQty = parseFloat(document.getElementById("currentstock").value);
	var retQty = parseFloat(document.getElementById("returnQty").value);
	if (retQty > actQty) {
		alert("Return Qty should be less than or equal to Stock Qty");
		document.getElementById("hretqty"+rowId).value = 0;
		document.getElementById("hretqty"+rowId).focus();
	}
}

function savestock(){
   if(document.supplierreturnsform.supplierName.value == ''){
	   alert("Select Supplier");
	   document.supplierreturnsform.supplierName.focus();
	   return false
   }

	if(document.supplierreturnsform.returnType.options.selectedIndex==3){
	   if(document.getElementById("remarks").value==""){
		   alert("Enter Remarks");
		   document.supplierreturnsform.remarks.focus();
		   return false
       }
   }

	var length = document.getElementById('medList').rows.length-2;
 	if (length < 1) {
 		alert("Please add items to return");
 		return false;
 	}
    var allChecked = false;
 	var itemListTable = document.getElementById("medList");
	var numRows = itemListTable.rows.length-2;

	for (var k=1;k<=numRows;k++) {
    	if (document.getElementById("hdeleted"+k).value == 'false') {
    		if (document.getElementById("hretqty"+k).value == 0) {
	    		alert("Return qty should not be zero");
	    		document.getElementById("hretqty"+k).focus();
	    		return false;
		    }
    		allChecked = true;
    	}
	}
    if (!allChecked) {
    	alert("All row(s) in the grid are deleted, \n so no record(s) to save");
		return false;
    }
    document.supplierreturnsform.saveStk.disabled = true;
	document.supplierreturnsform.action = "StoresSupplierReturnslist.do?_method=makeSupplierReturns";
	document.supplierreturnsform.store.disabled = false;
	document.supplierreturnsform.submit();
}
function cancelstock () {
	document.supplierreturnsform.action = popurl+"/stores/StoresSupplierReturns.do?method=getSupplierReturns&filterClosed=true&sortOrder=retNo&sortReverse=true&typeall=on&statusall=O&statusall=P";
	document.supplierreturnsform.submit();
}
function makeingDec(objValue,obj){
	if (objValue == '') objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj.name).value = parseInt(objValue);
	} else document.getElementById(obj.name).value = 0;
}

function makeingDecValidate(objValue,obj){
	if (objValue == '') objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj).value = parseFloat(objValue);
	} else document.getElementById(obj).value = 0;
}
function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		if (enterNumAndDot(e)){
			onAddMedicine()
			return false;
		}
	}
}



function addGroupMedDetails(){
	if (groupMedDetails != null && groupMedDetails.length > 0)document.getElementById('issue_units').checked = true;
	if (groupMedDetails == null || groupMedDetails.length == 0)
		return;
	document.forms[0].store.value = groupDeptId;
	//medNamesAutoComplete ();
	//initSupplierAutoComplete();
	if (groupMedDetails.length > 0){
		for(var i=0;i<groupMedDetails.length;i++){
			var medicineId = groupMedDetails[i].medicine_id;
			var medicine = groupMedDetails[i].medicine_name;
			var batchNo = groupMedDetails[i].batch_no;
			var itemBatchId = groupMedDetails[i].item_batch_id;
			var dateObj = groupMedDetails[i].exp_dt;
			var expDt = formatExpiry(dateObj);
			var mrp = groupMedDetails[i].mrp;
			var stockQty = groupMedDetails[i].qty;
			var issueQty = groupMedDetails[i].qty;
			var pkgsize =  groupMedDetails[i].issue_base_unit;
			var manf = groupMedDetails[i].manf_name;
			var selQty = qtyUnitSelection ();
			var issueUnits = selQty == 'I' ? groupMedDetails[i].issue_units : groupMedDetails[i].package_uom;
			var actualQty = stockQty;
			actualQty = Math.round(actualQty*100)/100;
			var rejQty = groupMedDetails[i].qty_in_transit;
			var itemIdentification = groupMedDetails[i].identification;
			document.getElementById("dialogId").value = i+1;
			if ((rejQty != 'undefined') && (rejQty!= null) && (groupMedDetails[i].indent_no != "")){
				rejQty = Math.round(rejQty*100)/100;
				var indentno = groupMedDetails[i].indent_no;
				var actualRejQty = rejQty/pkgsize;
				actualRejQty = Math.round(actualRejQty*100)/100;
				addToInnerHTML(medicine,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,expDt,actualQty,actualRejQty,indentno,itemIdentification,issueUnits);
			} else{
				addToInnerHTML(medicine,medicineId,manf,batchNo,itemBatchId,mrp,pkgsize,expDt,actualQty,actualQty,'',itemIdentification,issueUnits);
			}
		}
	}
	var elNewItem = matches(selsupp, oAutoComp);
	oAutoComp._selectItem(elNewItem);
}

function initItemGroupDialog(){
	itemDialog = new YAHOO.widget.Dialog("itemDialog",
			{
			width:"970px",
			context :["addBut", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:itemDialog,
	                                              	correctScope:true} );
	itemDialog.cfg.queueProperty("keylisteners", escKeyListener);

itemDialog.render();
}

function handleCancel(){
	resetMedicineDetails();
	itemDialog.cancel();
}

function getItemGroupDialog(id){
	if ( document.getElementById("hdeleted"+id).value == 'true' ){
		itemDialog.cancel();
		return false;
	}

	if(document.getElementById("supplierName").value == '') {
		alert("Please Select the Supplier Name");
		return false;
	}
	button = document.getElementById("addBut"+id);
	itemDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;

	var itemDetails = gMedicineDetails[document.getElementById("hmedId"+id).value];
		if ((document.getElementById("hmedId"+id) != null) && (document.getElementById("hmedId"+id).value != "")){
			if (('undefined' != document.getElementById("hindentno"+id).value) && (document.getElementById("hindentno"+id).value != "")){
				return false;
			}

			loadSelectBox(document.supplierreturnsform.batch,itemDetails,'itemBatchId',null,null);
			document.supplierreturnsform.medicine.value 	= document.getElementById("hmedName"+id).value ;
			var elNewItem = matches(document.getElementById("hmedName"+id).value, oMedAutoComp);
			
			oMedAutoComp._bItemSelected = true;
			onSelectMedicine();
			
//			oMedAutoComp._selectItem(elNewItem);
			document.supplierreturnsform.batch.value 	= document.getElementById("itemBatchId"+id).value ;
			document.supplierreturnsform.returnQty.value 	= document.getElementById("hretqty"+id).value ;
			document.supplierreturnsform.barCodeId.value = document.getElementById('hitembarcode'+id).value;
			if(document.getElementById("hitemidentification"+id).value == 'B') document.supplierreturnsform.returnQty.readOnly = false;
			else document.supplierreturnsform.returnQty.readOnly = true;

		} else{
			resetMedicineDetails();
		}
		itemDialog.show();
		if (prefBarCode == 'Y') document.supplierreturnsform.barCodeId.focus();
		else document.supplierreturnsform.medicine.focus();


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
	return getRadioSelection(document.forms[0].qty_unit);
}

function medNamesAutoComplete () {
	if (retAgtSupp == 'Y') clearGrid();
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
		document.supplierreturnsform.medicine.value = '';
		return;
	}//alert(val + "---"+itemNamesArray.length);
	var flag = false;
	for (var m=0;m<itemNamesArray.length;m++) {
	     var item = itemNamesArray[m];//alert(val + "== "+item.ITEM_BARCODE_ID);
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, oMedAutoComp);//alert(oAutoComp);
	     	
	     	document.supplierreturnsform.medicine.value = itmName;
	     	oMedAutoComp._bItemSelected = true;
//			oMedAutoComp._selectItem(elNewItem);
	     	onSelectMedicine ();
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	resetMedicineDetails();
	 	document.supplierreturnsform.medicine.value = '';
	 }
}
function ajaxReqForItem (path,method) {
	var supp = document.supplierreturnsform.supplierName.value;
	var store = document.supplierreturnsform.store.value;
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

function updateEdit(editObj,status){
	var childEditLen = editObj.childNodes.length;
	for (var i = 0; i < childEditLen; i++){
		var child = editObj.childNodes[i];
		if (child.nodeName == 'IMG'){
			if (status == 'disabled'){
				child.src = cpath+"/icons/Edit1.png";
				editObj.disabled=status;
				break;
			} else{
				child.src = cpath+"/icons/Edit.png";
				editObj.disabled="";
				break;
			}
		}
	}


}

function getMedicinesForStore(onCompletionFunction) {
	// get the medicine time stamp for this store: required for fetching the items.
	var storeId = document.supplierreturnsform.store.value;
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
	var storeId = document.supplierreturnsform.store.value;
	if (storeId == '')
		return;

	var url = cpath + "/pages/stores/getMedicinesInStock.do?ts=" + ts + "&hosp=" + sesHospitalId +
		"&includeUnapproved=Y=" +
		"&storeId=" + storeId;

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

