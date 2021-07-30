var itemNamesArray = '';
var oOrderKitAutoComp;
var gMedicineBatches;
var gItemType = 'itemname';
function changeIdentifiers(item_identifier){
	if (item_identifier == '') {
		document.getElementById("qty_avbl").innerHTML = '';
		document.getElementById("mrp").innerHTML = '';
		document.getElementById("unit_rate").innerHTML = '';
	}else {
		document.getElementById("mrp").innerHTML = mrp[item_identifier];
		document.getElementById("unit_rate").innerHTML = unitMrp[item_identifier];

		if(identification_type[item_identifier] == 'S'){
	  		document.stocktransferform.qty.value = '1';
	  		document.stocktransferform.qty.readOnly = true;
		}
	   	if(identification_type[item_identifier] != 'S'){
	   		document.stocktransferform.qty.readOnly = false;
	   	}
	   	document.stocktransferform.stocktype.value = stock_type[item_identifier];
	   	document.stocktransferform.itemtype.value = identification_type[item_identifier];
	   	document.stocktransferform.e_item_batch_id.value = itemBatchId[item_identifier];
	   	setAvblQty(item_identifier);
   	}
}

function onChangeUOM(uom){
	setAvblQty(document.getElementById("batch").value);
}

function setAvblQty(item_identifier) {
	if (item_identifier != '')
		document.getElementById("qty_avbl").textContent = document.getElementById('item_unit').value == 'I' ? qty_avbl[item_identifier] : (qty_avbl[item_identifier]/pkgSize[item_identifier]).toFixed(2);
	else
		document.getElementById("qty_avbl").textContent = '';
}

function setPackageSize(uom){
	var item = YAHOO.util.Dom.get('items').value;//selected medicine name
	var medDetails = findInList(itemNamesArray, 'medicine_name', item);
	var item = YAHOO.util.Dom.get('items').value;
	setElementText('pkg_size',( uom.value == 'I' ? '' : medDetails.issue_base_unit ));
	document.stocktransferform.issue_base_unit.value = medDetails.issue_base_unit;
}

var added_item = [];
var added_identifier = [];
function addItems() {
	var itemtable = document.getElementById("itemListtable");
	var len = itemtable.rows.length;//alert(len);
	var id = len;   // leave 1 for heading
	if(gItemType == 'orderkit') {
		var orderkitName = document.getElementById("orderkits").value;
		if(orderkitName == undefined || orderkitName == '' || orderkitName == null) {
			showMessage("js.stores.mgmt.indents.ordrkitrequired");
			document.getElementById("orderkits").focus();
			return false;
		}
		addMedicinesFromOrderKit();
	} else {
		var qty = document.stocktransferform.qty.value;
		if (!isValidNumber(document.stocktransferform.qty, qtyDecimal)) return false;

		var items = document.stocktransferform.items.value;
		var batch = document.stocktransferform.batch.value;
		var pkgSize = document.getElementById("pkg_size").innerHTML;
		var dialogId  = document.getElementById("dialogId").value;
		var itemUnit = document.stocktransferform.item_unit.value;
		var qty1 = itemUnit == 'I' ? qty : qty*pkgSize;
		var description = document.stocktransferform.e_item_transfer_description.value;
		if(!(checkItemDetails(qty_avbl[batch],qty1,len,items,batch)))
			return false;

		if (dialogId == (len-1)) {
			var editButton = document.getElementById("add"+parseFloat(len-1));
			var eBut =  document.getElementById("addBut"+parseFloat(len-1));
			editButton.setAttribute("src",popurl+'/icons/Edit.png');
			eBut.setAttribute("title", getString("js.stores.mgmt.edititem"));
			eBut.setAttribute("accesskey", "");
			var isEdit =  document.getElementById("isEdit"+parseFloat(len-1));
			isEdit.value = 'Y';
		}


		addToInnerHTML(items, batch,document.stocktransferform.store.value,
							document.stocktransferform.to_store.value,qty,document.stocktransferform.stocktype.value,
							"",document.stocktransferform.itemtype.value, pkgSize, itemUnit,null,
							document.stocktransferform.e_item_batch_id.value,description);
		document.stocktransferform.qty.readOnly = false;
	}

}
function addToInnerHTML(ItemName,Identifier,fromStoreId,toStoreId,qty,stocktype,transaction,itemtype,pkgSize, itemUnit, uom,itemBatchId,description)
{
	var itemtable = document.getElementById("itemListtable");
	var tabLen = itemtable.rows.length;
	var dialogId  = document.getElementById("dialogId").value;
	dialogId = ( empty(dialogId) ? 1 : dialogId);
	var flag = '';

	added_item[tabLen] = ItemName;
	added_identifier[tabLen] = Identifier;

	if (stocktype == true || stocktype == 'true' || stocktype == 't')
	{
		stocktype = true;
		flag = 'cstk';
	} else{
		 flag = '';
	}

	 var checkbox = makeImageButton('itemCheck','itemCheck'+dialogId,'imgDelete',cpath+'/icons/Delete.png');
	checkbox.setAttribute('onclick','cancelRow(this.id,tableRow'+dialogId+','+dialogId+')');

	if(document.getElementById("itemRow"+dialogId).firstChild == null) {
		document.getElementById("itemRow"+dialogId).appendChild(checkbox);
	}
	document.getElementById("itemLabel"+dialogId).textContent = '';
	if (flag != '') document.getElementById("itemLabel"+dialogId).innerHTML = '<img class="flag" src= "'+cpath+'/images/grey_flag.gif"/>'+ItemName;
	else document.getElementById("itemLabel"+dialogId).textContent =ItemName;
	document.getElementById("identifierLabel"+dialogId).textContent =Identifier ;
	document.getElementById("expdtLabel"+dialogId).textContent =formatExpiry(exp_dt[Identifier]) ;
	if ( empty(Identifier) )
		document.getElementById("packagetypeLabel"+dialogId).textContent = '' ;
	else
		document.getElementById("packagetypeLabel"+dialogId).textContent = package_type[Identifier] ;
	document.getElementById("transferqtyLabel"+dialogId).textContent = qty;
	document.getElementById("pkgSizeLabel"+dialogId).textContent = pkgSize;
	document.getElementById("descriptionLabel"+dialogId).textContent = description ;

	document.getElementById("pkgSize"+dialogId).value = pkgSize;
	document.getElementById("itemidentification"+dialogId).value = itemtype;
	document.getElementById("item_batch_id"+dialogId).value = itemBatchId;
	document.getElementById("description"+dialogId).value = description;
	if (transaction == 'group')
		document.getElementById("itemUOM"+dialogId).textContent = uom;
	else if ( empty(Identifier) )//possible in case kit issue from CSSD
		document.getElementById("itemUOM"+dialogId).textContent = '';
	else
		document.getElementById("itemUOM"+dialogId).textContent = itemUnit == 'I' ? issueUnits[Identifier] : pkgUOM[Identifier];
	document.getElementById("itemUnit"+dialogId).value = itemUnit;
	document.getElementById("item_id"+dialogId).value = item_ids[Identifier] ;
	document.getElementById("item_name"+dialogId).value = ItemName;
	document.getElementById("from_store"+dialogId).value = fromStoreId;
	document.getElementById("tranfer_store"+dialogId).value = toStoreId;
	document.getElementById("itemidentifier"+dialogId).value =Identifier ;
	document.getElementById("expdt"+dialogId).value = formatExpiry(exp_dt[Identifier])
	document.getElementById("trannsfer_qty"+dialogId).value = itemUnit == 'I' ? qty : qty*pkgSize;
	document.getElementById("stk_type"+dialogId).value =stocktype;
	document.getElementById("hdeleted"+dialogId).value = "false" ;
	document.getElementById("hmrp"+dialogId).value = mrp[Identifier] ;

	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
		if(nextrow == null){
			AddRowsToGrid(tabLen);
		}
		if( transaction!= "group" && document.getElementById('isEdit'+(eval(dialogId)+1) ).value != 'Y')
			openDialogBox(eval(dialogId)+1);

		if(document.getElementById('isEdit'+(eval(dialogId)+1) ).value == 'Y' || (document.getElementById('isEdit'+(eval(dialogId)+1) ).value == 'Y' && eval(dialogId) == tabLen-1) ) {
			handleCancel();
		}
}

function makeButton1(name, id, value){
	var el = document.createElement("button");

	if (name!=null && name!="")
		el.name= name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
}

function AddRowsToGrid(tabLen){
	var itemtable = document.getElementById("itemListtable");
	var tdObj="",trObj="";
	var row = "tableRow" + tabLen;
	var deleteLabel			= makeLabel('itemRow','itemRow'+tabLen,'');
	var itemLabel			= makeLabel('itemLabel','itemLabel'+tabLen,'');
	var identifierLabel		= makeLabel('identifierLabel','identifierLabel'+tabLen,'');
	var transferqtyLabel		= makeLabel('transferqtyLabel','transferqtyLabel'+tabLen,'');
	var itemUOM		= makeLabel('itemUOM','itemUOM'+tabLen,'');
	var packagetypeLabel	= makeLabel('packagetypeLabel','packagetypeLabel'+tabLen,'');
	var expdtLabel			= makeLabel('expdtLabel','expdtLabel'+tabLen,'');
	var pkgSizeLabel			= makeLabel('pkgSizeLabel','pkgSizeLabel'+tabLen,'');
	var descLabel			= makeLabel('descriptionLabel','descriptionLabel'+tabLen,'');

	var item_idHidden 		= makeHidden('item_id','item_id'+tabLen,'');
	var item_nameHidden 	= makeHidden('item_name','item_name'+tabLen,'');
	var from_storeHidden 	= makeHidden('from_store','from_store'+tabLen,'');
	var tranfer_storeHidden = makeHidden('tranfer_store','tranfer_store'+tabLen,'');
	var identification  = makeHidden('itemidentification','itemidentification'+tabLen,'');
	var itemBatchId  = makeHidden('item_batch_id','item_batch_id'+tabLen,'');
	var itemUnit = makeHidden('itemUnit','itemUnit'+tabLen,'');
	var pkSize = makeHidden('pkgSize','pkgSize'+tabLen,'');
	var description = makeHidden('description','description'+tabLen,'');

	var itemidentifierHidden= makeHidden('itemidentifier','itemidentifier'+tabLen,'');
	var expdtHidden 		= makeHidden('expdt','expdt'+tabLen,'');
	var trannsfer_qtyHidden	= makeHidden('trannsfer_qty','trannsfer_qty'+tabLen,'');
	var stk_typeHidden 		= makeHidden('stk_type','stk_type'+tabLen,'');
	var hdeletedHidden 		= makeHidden('hdeleted','hdeleted'+tabLen,'');
	var hmrpHidden 		= makeHidden('hmrp','hmrp'+tabLen,'');

	var buton = makeButton1("addBut", "addBut"+tabLen);
	buton.setAttribute("class", "imgButton");
	buton.setAttribute("onclick","openDialogBox('"+tabLen+"'); return false;");
	buton.setAttribute("title", getString("js.stores.mgmt.addnewitem"));
	buton.setAttribute("accesskey", "+");
	var isEditHidden= makeHidden('isEdit','isEdit'+tabLen,'N');
	var itemrowbtn = makeImageButton('add','add'+tabLen,'imgAdd',cpath+'/icons/Add.png');
	buton.appendChild(itemrowbtn);

/**	var itemrowbtn = makeButton('add','add'+tabLen,'+');
	itemrowbtn.setAttribute("onclick","openDialogBox('"+tabLen+"')");
	itemrowbtn.setAttribute("class","plus");
	itemrowbtn.setAttribute("accesskey", "+");
	itemrowbtn.setAttribute("title", 'Add New Item (Alt_Shift_+)'); */
	trObj = itemtable.insertRow(tabLen);
	trObj.id = row;


	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(itemLabel);
	tdObj.appendChild(item_idHidden);
	tdObj.appendChild(item_nameHidden);
	tdObj.appendChild(from_storeHidden);
	tdObj.appendChild(tranfer_storeHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(identifierLabel);
	tdObj.appendChild(itemidentifierHidden);
	tdObj.appendChild(expdtHidden);
	tdObj.appendChild(trannsfer_qtyHidden);
	tdObj.appendChild(stk_typeHidden);
	tdObj.appendChild(identification);
	tdObj.appendChild(itemBatchId);
	tdObj.appendChild(hmrpHidden);
	tdObj.appendChild(itemUnit);
	tdObj.appendChild(pkSize);
	tdObj.appendChild(description);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(expdtLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(packagetypeLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(transferqtyLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(itemUOM);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(pkgSizeLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(descLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(deleteLabel);
	tdObj.appendChild(hdeletedHidden);


	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(buton);
	tdObj.appendChild(isEditHidden);
}
function cancelRow(checkbox,rowid,len){
	var rowId = rowid.id;

	var deletedInput = document.getElementById('hdeleted'+len);
	if(deletedInput.value == 'false'){
		YAHOO.util.Dom.get(rowId).disabled = true;
		deletedInput.value = 'true';
		document.getElementById("add"+len).src = cpath+"/icons/Edit1.png";
		document.getElementById("addBut"+len).disabled = true;
		document.getElementById(checkbox).src = cpath+"/icons/Deleted.png";
	} else {
		deletedInput.value = 'false';
		YAHOO.util.Dom.get(rowId).disabled = false;
		document.getElementById("add"+len).src = cpath+"/icons/Edit.png";
		document.getElementById("addBut"+len).disabled = false;
		document.getElementById(checkbox).src = cpath+"/icons/Delete.png";
	}
}
function qtyValidation (id) {
  var transferqty = parseFloat(document.getElementById('trannsfer_qty'+id).value);
  var actualqty = parseFloat(document.getElementById('qty_avl'+id).value);
  if (transferqty > actualqty) {
  	showMessage("js.stores.mgmt.transferedqty.lessthancurrentstock");
  	document.getElementById('trannsfer_qty'+id).value = 0;
  	document.getElementById('trannsfer_qty'+id).focus();
  }
}
function checkItemDetails( avbl_qty, transfer_qty, len, newItemName, newItemBatch ) {

		var len = document.getElementById('itemListtable').rows.length-1;
		for ( var i = 1; i < len; i++ ) {
			var existingItemIdentifier = document.getElementById('itemidentifier' + i ).value;
			var existingItem = document.getElementById('itemLabel' + i ).textContent;
			var currentId = document.getElementById("dialogId").value;

			if ( i == currentId )
				continue;

			if ( existingItem == newItemName && existingItemIdentifier == newItemBatch ) {
				showMessage("js.stores.mgmt.duplicateentry");
					return false;
			}
		}

		if(document.stocktransferform.items.value == ''){
			showMessage("js.stores.mgmt.selectanitem");
			document.stocktransferform.items.focus();
			return false;
		}
		if(document.stocktransferform.batch.value == ''){
			showMessage("js.stores.mgmt.pickabatch.or.serial.no");
			document.stocktransferform.batch.focus();
			return false;
		}
		if(document.stocktransferform.qty.value == ''){
			showMessage("js.stores.mgmt.transferquantity.required");
			document.stocktransferform.qty.focus();
			return false;
		}
		if(document.stocktransferform.qty.value == 0){
			showMessage("js.stores.mgmt.transferquantity.notbezero");
			document.stocktransferform.qty.focus();
			return false;
		}
		if(avbl_qty < transfer_qty){
			showMessage("js.stores.mgmt.requestedquantity.notavailable");
			document.stocktransferform.qty.value = '';
			document.stocktransferform.qty.focus();
			return false;
		}

		var disallowExpiredItems = document.stocktransferform.disallow_expired.checked;

		if ( disallowExpiredItems ) {
			if (  !validatedExpiredItems(document.stocktransferform.batch.value) )
				return false;
		}
		return true;
}
function validate(){
	var allChecked = false;
	if(document.getElementById("to_store").value == ""){
		showMessage("js.stores.mgmt.tostoreismandatory");
		document.getElementById("to_store").focus();
		return false;
	}
	if(trimAll(document.getElementById("reason").value) == ""){
		showMessage("js.stores.mgmt.transferreasonismandatory");
		document.getElementById("reason").value = '';
		document.getElementById("reason").focus();
		return false;
	}
	if (document.getElementById("transferDate").value == '') {
		showMessage("js.stores.mgmt.selecttransferdate");
		document.getElementById("transferDate").focus();
        return false;
	}
	if(document.getElementById("transferDate").value!=""){
	   var valid = true;
       valid=doValidateDateField(document.stocktransferform.transferDate,'past');
       if (!valid) {
       	   document.getElementById("transferDate").focus();
       	   return false;
       }
	}
	var numRows = document.getElementById("itemListtable").rows.length-1;
	if(numRows <= 1){
		showMessage("js.stores.mgmt.pleaseadditemstotransfer");
		return false;
    }
    if (numRows > 1) {
	    for (var k=1;k<=(numRows-1);k++) {
	    	if ((document.getElementById("hdeleted"+k).value == 'false')) {
	    		allChecked = true;
	    	}
		}
	    if (!allChecked) {
	    	showMessage("js.stores.mgmt.allrowsingrid");
			return false;
	    }
	}

	var item_id = document.getElementsByName("item_id");
	var hdeleted = document.getElementsByName("hdeleted");

	for (var i =0;i<item_id.length;i++){
		if ( item_id[i].value == 'undefined' && hdeleted[i].value != 'true' ) {
			showMessage("js.stores.mgmt.pleaseselectbatch");
			openDialogBox(i+1);
			return false;
		}
	}

	if ( ! (validateToStore(document.getElementById("store").value,document.getElementById("to_store").value) ) )
	return false;

    document.stocktransferform.action= (sterileTransfer ? "SterileStockTransfer" : "stocktransfer")+".do?_method=create";
	document.stocktransferform.submit();
	return true;
}
function onChangeStore(storechanged,msgDisplay){
	var img_button = document.createElement( 'img' );
	img_button.src = cpath + '/images/delete.jpg';
	document.stocktransferform.store.value = storechanged;


	var item_table = document.getElementById("itemListtable");
	var innertablelength = item_table.rows.length-1;
	if(innertablelength > 0){
		for(var i =innertablelength;i>0;i--){
				item_table.deleteRow(i);
		}

		AddRowsToGrid(1);
	}
   document.getElementById("dialogId").value = 1;
   document.getElementById("items").value = '';
   document.getElementById("result_msg").style.display = msgDisplay;
   document.getElementById("reason").value='';
   document.getElementById("to_store").value='';
   document.getElementById("grn_no").value='';
   document.stocktransferform.store.disabled= false;

	if (oItemAutoComp != undefined) {
		// make sure autocomp is not functional while we fetch the items
		oItemAutoComp.destroy();
		oItemAutoComp = undefined;
	}

	var storeIdObj = document.stocktransferform.store;
	getMedicinesForStore(storeIdObj, setItems);
}

function validateToStore(fromStore,toStore){
	if(fromStore == toStore){
		showMessage("js.stores.mgmt.fromandtostores.bedifferent");
		document.getElementById("to_store").value="";
		return false;
	}
	return true;
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

var oItemAutoComp;
function setItems(){
	if (oItemAutoComp != undefined) {
		oItemAutoComp.destroy();
		oItemAutoComp = undefined;
	}

	itemNamesArray = jMedicineNames;
	var dataSource = new YAHOO.widget.DS_JSArray(itemNamesArray);
	dataSource.responseSchema = {
		resultsList: "result",
		fields: [  {key : "cust_item_code_with_name"},{key : "medicine_name"} ]
	};
	oItemAutoComp = new YAHOO.widget.AutoComplete('items','item_dropdown', dataSource);
	oItemAutoComp.prehightlightClassName = "yui-ac-prehighlight";
	oItemAutoComp.typeAhead = false;
	oItemAutoComp.useShadow = false;
	oItemAutoComp.allowBrowserAutocomplete = false;
	oItemAutoComp.minQueryLength = 1;
	oItemAutoComp.forceSelection = true;
	oItemAutoComp.maxResultsDisplayed = 50;
	oItemAutoComp.itemSelectEvent.subscribe(onSelectItem);
	oItemAutoComp.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get('items').value;
		if(sInputValue.length === 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		} });
	oItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oItemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oItemAutoComp.containerCollapseEvent.subscribe(getItemDetails);

}

/*
 * Called on selection of an item in the item auto comp
 */
function onSelectItem(type, args) {
	if (args && args[2][1])
		document.getElementById("items").value = args[2][1];
}

function getItemBarCodeDetails (val) {
	if (val == '') {
		resetMedicineDetails();
		document.forms[0].items.value = '';
		return;
	}
	var flag = false;
	for (var m=0;m<itemNamesArray.length;m++) {
	     var item = itemNamesArray[m];
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, oItemAutoComp);//alert(oAutoComp);
	     	document.forms[0].items.value = itmName;
	     	oItemAutoComp._bItemSelected = true;
//			oItemAutoComp._selectItem(elNewItem);
	     	getItemDetails ();
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	resetMedicineDetails();
	 	document.forms[0].items.value = '';
	 }
}
function resetMedicineDetails() {
	document.getElementById("batch").disabled = false;
	document.getElementById("batch").selectedIndex = 0;
   	//document.getElementById("exp_dt").innerHTML = '';
	//document.getElementById("pkg_type").innerHTML = '';
	document.getElementById("qty_avbl").innerHTML = '';
	document.getElementById("pkg_size").innerHTML = '';
	document.getElementById("mrp").innerHTML = '';
	document.getElementById("unit_rate").innerHTML = '';
	document.forms[0].barCodeId.value = "";
	document.forms[0].items.value = '';
	document.getElementById("item_unit").options.length = 0;
}
function makeingDec(objValue,obj){
	if (objValue == '') objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj.name).value = parseFloat(objValue).toFixed(2);
	}else document.getElementById(obj.name).value = 0.00;
}

function AddGroupItemDetails(){
	if(groupItemDetails == null || groupItemDetails.length == 0)
		return;

	document.stocktransferform.store.value = groupStoreId;
	document.getElementById("to_store").value="";

	for(var i=0;i<groupItemDetails.length;i++){

		var item_id = groupItemDetails[i].medicine_id;
		var item_name = groupItemDetails[i].medicine_name;
		var batchIndex =groupItemDetails[i].batch_no;
		var qty = groupItemDetails[i].qty;
		var issuetype = groupItemDetails[i].issue_type;
		var item_identifier = groupItemDetails[i].batch_no;
		var stktype = groupItemDetails[i].consignment_stock;
		var pkg_size = groupItemDetails[i].stock_pkg_size;
		var store_id = groupItemDetails[i].dept_id;
		var itemtype = groupItemDetails[i].identification;

		package_type[groupItemDetails[i].batch_no] = groupItemDetails[i].package_type;

		exp_dt[groupItemDetails[i].batch_no] = groupItemDetails[i].exp_dt;
		pkgSize[groupItemDetails[i].batch_no] = groupItemDetails[i].stock_pkg_size;
    	item_ids[groupItemDetails[i].batch_no] = groupItemDetails[i].medicine_id;
    	identification_type[groupItemDetails[i].batch_no] = groupItemDetails[i].identification;
        stock_type[groupItemDetails[i].batch_no] = groupItemDetails[i].consignment_stock;
        qty_avbl[groupItemDetails[i].batch_no] = groupItemDetails[i].qty;
        mrp[groupItemDetails[i].batch_no] = parseFloat(groupItemDetails[i].mrp/groupItemDetails[i].stock_pkg_size).toFixed(2);
		document.getElementById("dialogId").value = i+1;

		var editButton = document.getElementById("add"+(i+1));
		var eBut =  document.getElementById("addBut"+(i+1));
		editButton.setAttribute("src",popurl+'/icons/Edit.png');

	eBut.setAttribute("title", getString("js.stores.mgmt.edititem"));
	eBut.setAttribute("accesskey", "");


		addToInnerHTML(item_name,item_identifier,store_id,"",qty,stktype,"group",itemtype, pkg_size, 'I', groupItemDetails[i].issue_units,'');
	}
}
function setFocus () {
	if (prefBarCode == 'Y') document.forms[0].barCodeId.focus();
	else document.getElementById("items").focus();
}
function openDialogBox(id){
	var itemtable = document.getElementById("itemListtable");
	var len = itemtable.rows.length-1;
	gItemType = 'itemname';
	if(document.stocktransferform != undefined && document.stocktransferform != null) {
		document.getElementById("orderKitDetails").style.display = "none";
		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
		document.getElementById("orderKitMissedItems").style.display = "none";
		document.getElementById("itemInfo").innerHTML = "";
		document.getElementById("orderkits").value = "";
		document.getElementById("itemname").checked = true;
		document.getElementById("order").checked = false;
		document.getElementById("itemFieldSet").style.display = "block";
		document.getElementById("itemDetails").style.display = "block";
		document.getElementById("order").disabled = false;
		document.getElementById("OK").disabled = false;
		document.getElementById("itemInfo").style.height = "10px";
	}
	if(document.getElementById("isEdit"+id).value == 'Y') {
		document.getElementById("order").disabled = true;
	} else {
		document.getElementById("order").disabled = false;
	}
	if (id == len) {
		document.getElementById('item_unit').options.length = 0;
	}
	var button = document.getElementById("tableRow"+id);
	document.forms[0].barCodeId.value = "";
//	document.stocktransferform.items.value = document.getElementById("itemLabel"+id).textContent;

	if (groupItemDetails != null) {
		document.stocktransferform.batch.options[0].text = document.getElementById("itemidentifier"+id).value;
		document.stocktransferform.batch.options[0].value = document.getElementById("itemidentifier"+id).value;
		document.stocktransferform.batch.selectedIndex = 0;
	}
	var elNewItem = matches(document.getElementById("itemLabel"+id).textContent, oItemAutoComp);//alert(oAutoComp4);
//	oItemAutoComp._selectItem(elNewItem);
	document.forms[0].items.value = document.getElementById("itemLabel"+id).textContent;
	oItemAutoComp._bItemSelected = true;
	getItemDetails("stocktransfer", id);

	if ( document.getElementById("identifierLabel"+id).textContent == '' && document.stocktransferform.batch.disabled){
		document.stocktransferform.batch.selectedIndex = 1;
		changeIdentifiers(document.stocktransferform.batch.value);
		document.getElementById("e_item_transfer_description").value = document.getElementById("description"+id).value;
	} else {
		document.stocktransferform.batch.value = document.getElementById("identifierLabel"+id).textContent;
		document.stocktransferform.qty.value = document.getElementById("transferqtyLabel"+id).textContent;
		//document.getElementById("exp_dt").textContent = document.getElementById("expdtLabel"+id).textContent;
		//document.getElementById("pkg_type").textContent = document.getElementById("packagetypeLabel"+id).textContent;

		document.getElementById("pkg_size").textContent = document.getElementById("pkgSizeLabel"+id).textContent;
		document.getElementById("mrp").textContent = document.getElementById("hmrp"+id).textContent;
		document.getElementById("item_unit").value = document.getElementById("itemUnit"+id).value;
		document.getElementById("e_item_transfer_description").value = document.getElementById("description"+id).value;
	}

	if (document.getElementById("itemidentification"+id).value == 'S') document.stocktransferform.qty.readOnly = true;
	else document.stocktransferform.qty.readOnly = false;

	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	dialog.show();
	setFocus ();
}

function initDialog(){
	dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"800px",
			context : ["itemListtable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
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
	document.stocktransferform.save.focus();
}
function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		addItems();
		return false;
	}
}

function checkstoreallocation() {
	if(gRoleId != 1 && gRoleId != 2) {
		if(deptId == "") {
			showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
			document.getElementById("storecheck").style.display = 'none';
		}
	}
}
function init () {
	var storeIdObj = document.stocktransferform.store;
	getMedicinesForStore(storeIdObj, setItems);
	initDialog();
	initOrderKitAutoComplete();
	AddGroupItemDetails();
	checkstoreallocation();
    if (allowBackDate == 'N') document.forms[0].transferDate.readOnly = true;
 	else document.forms[0].transferDate.readOnly = false;

 	var isSterileStockTransfer = document.stocktransferform.sterile_stock_transfer.value;
 	if ( isSterileStockTransfer == 'true' ) {
 		onChangeStore(document.stocktransferform.store.value,'block');
 		//default store need not be sterile store in which case items will not get loeaded.
 		//So, call onchange store method to load medicines again.
 	}

 	if ( kitIssue )
 		autoFillKitItems();
	// we do this here instead of onClick on the button so that init of dialog is ensured before calling it
	YAHOO.util.Event.addListener("addBut1", "click", function() {openDialogBox(1)});
}


function setUOMOptions(obj, btch) {
	var uomOptList = null;
	if ( btch.packageSize > 1) {
		uomOptList = [
					{uom_name: btch.issueUnits, uom_value: 'I'},
					{uom_name: btch.packageUOM, uom_value: 'P'}
				];
	}else if ( btch.packageSize == 1 ) {
		uomOptList = [
					{uom_name: btch.issueUnits, uom_value: 'I'}
				];
	}
	loadSelectBox(obj, uomOptList, 'uom_name', 'uom_value');
}

function getLastDayForMonth(month, year) {
	var dt = new Date(parseInt(year),parseInt(month),0);
	return [(dt.getDate() < 10 ? ('0'+ dt.getDate()) : dt.getDate()),(dt.getMonth() < 9 ? ('0'+ (dt.getMonth()+1)) : (dt.getMonth()+1)),dt.getFullYear()].join('-');
}

/**
	Called on dialog save if Disallow Expired check box is checked
	and checks the expiry date of item.
**/
function validatedExpiredItems(item_identifier){
	var dateStr = formatDate(new Date(exp_dt[item_identifier]), 'mmyyyy', '-');
	var dateAry = dateStr.split("-");

	if ( dateStr != null && dateStr != '' ) {
		var formattedStrDate = getLastDayForMonth(dateAry[0],convertTwoDigitYear(parseInt(dateAry[1],10)));
		var msg = validateDateStr(formattedStrDate, 'future');
		if ( msg != null && msg != '' ){
			showMessage("js.stores.mgmt.itemsavailable.pastexpirydate");
			return false;
		}
	}

	return true;
}

function onChangeDisallowExpired(obj) {
	if ( obj.checked == true ) {
		clearGrid();
	}
}

function clearGrid(){
	var item_table = document.getElementById("itemListtable");
	var innertablelength = item_table.rows.length-1;
	if(innertablelength > 0){
		for(var i =innertablelength;i>0;i--){
				item_table.deleteRow(i);
		}
		AddRowsToGrid(1);
		document.getElementById("dialogId").value = 1;
	}

}

/**
 * This method used to set dialog box according to item type.
 *
 * @returns {Boolean}
 */
function onChangeItemType() {
	var radios = document.getElementsByName('item_type');
	var itemInfo = document.getElementById("itemInfo");
	for (var i = 0, length = radios.length; i < length; i++) {
	    if (radios[i].checked) {
	    	if(radios[i].value == 'itemname') {
	    		document.getElementById("OK").disabled = false;
	    		document.getElementById("itemFieldSet").style.display = "block";
	    		document.getElementById("itemDetails").style.display = "block";
	    		if(prefBarCode == 'Y') {
	    			document.getElementById("barCodeId").focus();
	    		} else {
	    			document.getElementById("items").focus();
	    		}
	    		document.getElementById("orderKitDetails").style.display = "none";
	    		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
	    		document.getElementById("orderKitMissedItems").style.display = "none";
	    		document.getElementById("orderkits").value = "";
	    		itemInfo.style.height = '10px';
	    		itemInfo.innerHTML = "";
	    		gItemType = 'itemname';
	    	} else {
	    		document.getElementById("OK").disabled = false;
	    		document.getElementById("itemDetails").style.display = "none";
	    		document.getElementById("orderKitDetails").style.display = "block";
	    		document.getElementById("orderkits").focus();
	    		document.getElementById("itemFieldSet").style.display = "none";
	    		itemInfo.style.height = '150px';
	    		itemInfo.innerHTML = "";
	    		document.getElementById("itemFieldSet").style.display = "none";
	    		document.getElementById("orderkits").value = "";
	    		resetMedicineDetails();
    			gItemType = 'orderkit';
	    	}
	        break;
	    }
	}
}

/**
 * This method is used to set order kit details based on order kit selection.
 *
 * @param type
 * @param args
 */
function setOrderDetails(type, args) {
	var orderKitName = args[2][0];
	var orderKitId = args[2][1];
	var orderKitItems;
	var ajaxReqObject = newXMLHttpRequest();
	var storeid = document.getElementById("store").value;
	var url = 'stocktransfer.do?_method=getOrderKitItemsJSON&order_kit_id='+orderKitId+"&storeId="+document.getElementById("store").value;
	ajaxReqObject.open("POST",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			orderKitItems = JSON.parse(ajaxReqObject.responseText);
			gOrderKitItems = orderKitItems.order_kit_items;
			gMedicineBatches = orderKitItems.medBatches;
			setOrderKitItems(orderKitItems.order_kit_items_status, orderKitName, orderKitItems.total_items_status, orderKitItems.nonissuableitems);
		}
	}
}

/**
 * This method is used to show the low stock medicines in the order kit.
 *
 * @param obj
 * @param orderKitName
 * @param totalItemsStatus
 */
function setOrderKitItems(obj, orderKitName, totalItemsStatus, nonIssuableItems) {
	var tableRowIndex = 0;
	var isAllItemsAvaiable = true;
	document.getElementById("orderKitMissedItemsHeader").style.display = "block";
	document.getElementById("orderKitMissedItems").style.display = "block";
	var table = document.getElementById("orderKitMissedItems");
	var numItems = table.rows.length;
	var totalItemsStatusArray = totalItemsStatus.split("@");
	if(parseFloat(totalItemsStatusArray[0]) == parseFloat(totalItemsStatusArray[1])) {
		document.getElementById("OK").disabled = true;
	} else {
		document.getElementById("OK").disabled = false;
	}
	var totalUnavaiableItems = totalItemsStatusArray[0]+" / "+totalItemsStatusArray[1];
	for ( var i=0; i<numItems; i++ ) {
		table.deleteRow(0);
	}
	var itemInfo = document.getElementById("itemInfo");
	if(obj != undefined && Object.keys(obj).length !== 0) {
	 	for (var key in obj) {
		  if (obj.hasOwnProperty(key)) {
		    var val = obj[key];
		    var stockArray = val.split("@");
		    if(parseFloat(stockArray[0]) < parseFloat(stockArray[1])) {
		    	isAllItemsAvaiable &= false;
		    	if(parseFloat(stockArray[0]) > 0) {
		    		document.getElementById("OK").disabled = false;
		    	}
		    	var row = table.insertRow(tableRowIndex).outerHTML =
			    	'<td style="white-space: pre-wrap;word-break: break-all;width:430px;padding-left:10px;color:#666666;font-size:11px;border-top: 0px;border-bottom: 0px;">'+key+'</td><td style="text-align:center;border-top: 0px;border-bottom: 0px;width:300px;">'+
			    	'<b><font color="red">'+parseFloat(stockArray[0])+'</font></b> / '+parseFloat(stockArray[1])+'</td>';
			    tableRowIndex++;
		    }
		  }
		}
	 	if(!isAllItemsAvaiable) {
	 		itemInfo.style.height = '10px';
	 		itemInfo.innerHTML = "<table width='100%' align='left' style='border: 1px #CCCCCC solid;padding: 5px;text-align: left;background-color:white;'>" +
			"<tr><td style='padding-left:7px;font-size:12px;'><b>Items Not Available ("+totalUnavaiableItems+")</b></td></tr></table>";
	 	} else {
	 		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
			document.getElementById("orderKitMissedItems").style.display = "none";
			itemInfo.style.height = '150px';
			itemInfo.innerHTML = "<span style='color: green;padding-left:6px;font-size:12px;'>All items are available of \""+orderKitName+"\"</span>";
	 	}


	} else {
		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
		document.getElementById("orderKitMissedItems").style.display = "none";
		itemInfo.style.height = '150px';
		itemInfo.innerHTML = "<span style='color: green;padding-left:6px;font-size:12px;'>All items are available of \""+orderKitName+"\".</span>";
	}


}

function initOrderKitAutoComplete(){
	if (oOrderKitAutoComp != undefined) {
		oOrderKitAutoComp.destroy();
	}

	if ((typeof(orderKitJSON) != 'undefined') && (orderKitJSON != null)){

		var dataSource1 = new YAHOO.util.LocalDataSource({result: orderKitJSON});
		dataSource1.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		dataSource1.responseSchema = {
			resultsList : "result",
			fields : [ {key : "order_kit_name"}, {key : "order_kit_id"}]
	 	};
		oOrderKitAutoComp = new YAHOO.widget.AutoComplete('orderkits','orderkit_dropdown', dataSource1);
		oOrderKitAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oOrderKitAutoComp.typeAhead = false;
		oOrderKitAutoComp.useShadow = false;
		oOrderKitAutoComp.allowBrowserAutocomplete = false;
		oOrderKitAutoComp.minQueryLength = 0;
		oOrderKitAutoComp.forceSelection = true;
		oOrderKitAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		oOrderKitAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

		oOrderKitAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('orderkits').value;
		});
		oOrderKitAutoComp.itemSelectEvent.subscribe(setOrderDetails);
	}
}

function addMedicinesFromOrderKit(){
	var dialogId = document.getElementById("dialogId").value;
	if(dialogId == undefined || dialogId == '' || dialogId == null) {
		dialogId = 0;
	} else {
		dialogId = dialogId -1;
	}
	if(gOrderKitItems != undefined && gOrderKitItems != null) {
		for (var p=0; p<gOrderKitItems.length; p++) {
			var msg = "";
			var medicineId = gOrderKitItems[p].medicine_id;
			var medBatches = gMedicineBatches[medicineId];
			var medicineName = gOrderKitItems[p].medicine_name;
			if (medBatches == null || medBatches.length == 0) {
				msg += msg.endsWith("\n") ? "" : "\n";
				msg += getString("js.sales.issues.storesuserissues.warning.nostockavailable");
				msg += medicineName;
				msg += " : ";
				msg += gOrderKitItems[p].qty_needed;
				alert(msg);
				continue;
			}
			dialogId = parseInt(dialogId)+p;

			addOrderKitItems(medicineId, gOrderKitItems[p].qty_needed, dialogId+1, gOrderKitItems[p].issue_units);
		}
	}
	var itemtable = document.getElementById("itemListtable");
	var len = itemtable.rows.length;
	openDialogBox(eval(len-1));
}

/*
 * Find if there is another entry of the same medicineId/Batch in the grid,
 * given the batch object and an index that is ourselves (so that we don't give a duplicate
 * error for ourselves
 */
function getDuplicateIndex(batch, selfIndex) {
	var numItems = document.getElementById("itemListtable").rows.length-1;
	var duplicateCount = 0;

	for (var i=1; i<=numItems; i++) {
		if (i == selfIndex)
			continue;
		var itemName = document.getElementById('item_name'+i).value;
		var itemIdentifier = document.getElementById('identifierLabel'+i).value;

		if ((itemName == batch.medicine_id) && (itemIdentifier == batch.batch_no))
			return i;
	}
	return -1;
}

/**
 * This method is used to add medicines from order kit and validate the medicine qty.
 *
 * @param medicineId
 * @param userQty
 * @param dialogId
 * @param issueUnits
 * @returns
 */
function addOrderKitItems(medicineId, userQty, dialogId,  issueUnits) {

	var allBatches = gMedicineBatches[medicineId];
	var medicineName = allBatches[0].medicine_name;

	var remQty = userQty;
	var expiredQty = 0; var nearingExpiryQty = 0;
	var negativeQty = 0; var diffPkgQty = 0;

	for (var b=0; b<allBatches.length; b++) {

		var batch = allBatches[b];
		var avlblQty = 0;
		exp_dt[batch.batch_no] = batch.exp_dt;

		// if batch is already in grid, skip
	    var dupExists = getDuplicateIndex(batch, -1);
		if (dupExists != -1)
			continue;

		// use only whole packages
		avlblQty = batch.qty;

		var disallowExpiredItems = document.stocktransferform.disallow_expired.checked;

		if ( disallowExpiredItems ) {
			if (  !validatedExpiredItems(batch.batch_no) )
				continue;
		}
		var qtyForBatch = 0;
		qtyForBatch = Math.min(remQty, avlblQty);
		qtyForBatch = qtyForBatch < 0 ? 0 : qtyForBatch;   // stock can be negative even otherwise.

		document.getElementById("dialogId").value = dialogId;
		// add to grid
		if (qtyForBatch > 0) {

			if(document.getElementById("itemListtable").rows.length>1){
				for(var i=1;i<=document.getElementById("itemListtable").rows.length-1;i++){
					var currentbatchNo = batch.batch_no;
					var currentMedicineName = medicineName;
					if ( (currentbatchNo==document.getElementById("itemidentifier"+i).value)
							&& (encodeURIComponent(currentMedicineName)==encodeURIComponent(document.getElementById("item_name"+i).value))
							&& ( dialogId != i) ){
						var msg = "";
						msg= getString("js.sales.issues.storesuserissues.duplicateentry")+" : ";
						msg+= medicineName;
						alert(msg);
						return false;
						break;
					}
				}
			}

			addOrderkitItemsToInnerHTML(medicineName, batch, document.stocktransferform.store.value,
					document.stocktransferform.to_store.value,qtyForBatch,batch.consignment_stock,
					"",batch.identification, batch.issue_base_unit, 'I',null,
					batch.item_batch_id,'');
		}

		// if no more to be added finish up
		remQty = remQty - qtyForBatch;
		if (remQty == 0)
			break;
	}

	var msg = "";
	if (remQty > 0) {
		msg= getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg+= medicineName;
		msg+= " : " ;
		msg+= remQty.toFixed(2);
		alert(msg);
		msg += "\n";
	}
	return msg;
}

function addOrderkitItemsToInnerHTML(ItemName,batch,fromStoreId,toStoreId,qty,stocktype,transaction,itemtype,pkgSize, itemUnit, uom,itemBatchId,description)
{
	var itemtable = document.getElementById("itemListtable");
	var tabLen = itemtable.rows.length;
	var dialogId  = (itemtable.rows.length)-1;

	if (dialogId == (tabLen-1)) {
		var editButton = document.getElementById("add"+parseFloat(tabLen-1));
		var eBut =  document.getElementById("addBut"+parseFloat(tabLen-1));
		editButton.setAttribute("src",popurl+'/icons/Edit.png');
		eBut.setAttribute("title", getString("js.stores.mgmt.edititem"));
		eBut.setAttribute("accesskey", "");
		var isEdit =  document.getElementById("isEdit"+parseFloat(tabLen-1));
		isEdit.value = 'Y';
	}
	dialogId = ( empty(dialogId) ? 1 : dialogId);
	var flag = '';
	if (stocktype == true || stocktype == 'true' || stocktype == 't')
	{
		stocktype = true;
		flag = 'cstk';
	} else{
		 flag = '';
	}

	var checkbox = makeImageButton('itemCheck','itemCheck'+dialogId,'imgDelete',cpath+'/icons/Delete.png');
	checkbox.setAttribute('onclick','cancelRow(this.id,tableRow'+dialogId+','+dialogId+')');

	if(document.getElementById("itemRow"+dialogId).firstChild == null) {
		document.getElementById("itemRow"+dialogId).appendChild(checkbox);
	}
	document.getElementById("itemLabel"+dialogId).textContent = '';
	if (flag != '') document.getElementById("itemLabel"+dialogId).innerHTML = '<img class="flag" src= "'+cpath+'/images/grey_flag.gif"/>'+ItemName;
	else document.getElementById("itemLabel"+dialogId).textContent =ItemName;
	document.getElementById("identifierLabel"+dialogId).textContent =batch.batch_no ;
	document.getElementById("expdtLabel"+dialogId).textContent =formatExpiry(exp_dt[batch.batch_no]) ;
	if ( empty(batch.batch_no) )
		document.getElementById("packagetypeLabel"+dialogId).textContent = '' ;
	else
		document.getElementById("packagetypeLabel"+dialogId).textContent = batch.package_type ;
	document.getElementById("transferqtyLabel"+dialogId).textContent = qty;
	document.getElementById("pkgSizeLabel"+dialogId).textContent = pkgSize;
	document.getElementById("descriptionLabel"+dialogId).textContent = description ;

	document.getElementById("pkgSize"+dialogId).value = pkgSize;
	document.getElementById("itemidentification"+dialogId).value = itemtype;
	document.getElementById("item_batch_id"+dialogId).value = itemBatchId;
	document.getElementById("description"+dialogId).value = description;
	if (transaction == 'group')
		document.getElementById("itemUOM"+dialogId).textContent = uom;
	else if ( empty(batch.batch_no) )//possible in case kit issue from CSSD
		document.getElementById("itemUOM"+dialogId).textContent = '';
	else
		document.getElementById("itemUOM"+dialogId).textContent = itemUnit == 'I' ? batch.issue_units : pkgUOM[batch.batch_no];
	document.getElementById("itemUnit"+dialogId).value = itemUnit;
	document.getElementById("item_id"+dialogId).value = batch.medicine_id;
	document.getElementById("item_name"+dialogId).value = ItemName;
	document.getElementById("from_store"+dialogId).value = fromStoreId;
	document.getElementById("tranfer_store"+dialogId).value = toStoreId;
	document.getElementById("itemidentifier"+dialogId).value =batch.batch_no ;
	document.getElementById("expdt"+dialogId).value = formatExpiry(exp_dt[batch.batch_no])
	document.getElementById("trannsfer_qty"+dialogId).value = itemUnit == 'I' ? qty : qty*pkgSize;
	document.getElementById("stk_type"+dialogId).value =stocktype;
	document.getElementById("hdeleted"+dialogId).value = "false" ;
	document.getElementById("hmrp"+dialogId).value = parseFloat(parseFloat(batch.mrp)/parseFloat(batch.issue_base_unit)).toFixed(2) ;

	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
	if(nextrow == null){
		AddRowsToGrid(tabLen);
	}
	if( transaction!= "group" && document.getElementById('isEdit'+(eval(dialogId)+1) ).value != 'Y')
		openDialogBox(eval(dialogId)+1);

	if(document.getElementById('isEdit'+(eval(dialogId)+1) ).value == 'Y' || (document.getElementById('isEdit'+(eval(dialogId)+1) ).value == 'Y' && eval(dialogId) == tabLen-1) ) {
		handleCancel();
	}
}

function validateExpDate(expDt){
	var expDtarray = expDt.split('-');
 	var expDate = new Date (expDtarray[2], expDtarray[1] - 1,expDtarray[0]);
	var toDay = new Date();
	if (expDate > toDay)
		return true;
	else
		return false;
}

function getGrnDetails() {
	var grnNo = document.stocktransferform.grn_no.value;
	if (grnNo == '' || grnNo == null){
		msg= "enter GRN No"
		clearGrid();
		document.stocktransferform.store.disabled = false; 
		alert(msg);
	}else {
		var url = cpath + '/stocks/getgrnitemsanddetails.json?grn_no=' + grnNo;
		let ajaxReqObject = newXMLHttpRequest();
		ajaxReqObject.open("GET",url.toString(), false);
		ajaxReqObject.send(null);
		if (empty(ajaxReqObject.response)){
			document.stocktransferform.grn_no.value = '';
			alert("invalid GRN No : " + grnNo);
			document.stocktransferform.store.disabled = false;
			return false;
		}
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) ) {
				clearGrid()
				var response = JSON.parse(ajaxReqObject.responseText);
				addItemsToGrid(response);
			}
		}

	}
}

function addItemsToGrid(response) {
	var grnDetails = response.grn_details;
	var grnItems = JSON.parse(response.grn_items);
	var availableGrnItems = response.stock_availabelity;
	var msg = "";
	var expItems = "";

	document.stocktransferform.store.value = grnDetails.store_id;
	document.stocktransferform.store.disabled = true;
	
	var itemtable = document.getElementById("itemListtable");
	
	for (var i = 0, length = grnItems.length; i < length; i++) {
		
		if (availableGrnItems[grnItems[i].item_batch_id] == 'N') {
			if (msg != ""){
				msg = msg + " , ";
			}
			msg = msg + grnItems[i].medicine_name;
			continue;
		}
		var expDt = grnItems[i].exp_dt;
		var disallowExpiredItems = document.stocktransferform.disallow_expired.checked;
		if (disallowExpiredItems) {
			if (!validateExpDate(expDt)){
				if (expItems != ""){
					expItems = expItems + " , ";
				}
				expItems = expItems + grnItems[i].medicine_name;
				continue;
			}

		}
		var qty = grnItems[i].billed_qty;
		
		var items = grnItems[i].medicine_name;
		var batch = grnItems[i].batch_no;
		var pkgSize = grnItems[i].grn_pkg_size;
		var dialogId  = document.getElementById("dialogId").value;
		var len = itemtable.rows.length;
		dialogId = ( empty(dialogId) ? 1 : dialogId);
		if (grnItems[i].issue_base_unit > 1){
			var itemUnit = "P";
		}else { 
			var itemUnit = "I";
		}
		var description = ""
	
		if (dialogId == (len-1)) {
			var editButton = document.getElementById("add"+parseFloat(len-1));
			var eBut =  document.getElementById("addBut"+parseFloat(len-1));
			editButton.setAttribute("src",popurl+'/icons/Edit.png');
			eBut.setAttribute("title", getString("js.stores.mgmt.edititem"));
			eBut.setAttribute("accesskey", "");
			var isEdit =  document.getElementById("isEdit"+parseFloat(len-1));
			isEdit.value = 'Y';
		}
	
	
		addGrnToInnerHTML(items, batch,document.stocktransferform.store.value,
							document.stocktransferform.to_store.value,qty,document.stocktransferform.stocktype.value,
							"",document.stocktransferform.itemtype.value, pkgSize, itemUnit,grnItems[i].grn_package_uom,
							grnItems[i].item_batch_id,description,grnItems[i].exp_dt,grnItems[i].medicine_id,grnItems[i].mrp);
		document.stocktransferform.qty.readOnly = false;

	}
	if (msg.length > 0){
		alert("following items are out of stock " + msg);
	}
	if (expItems.length > 0){
		alert("following items have passed the Expiry date " + expItems);
	}
}

function addGrnToInnerHTML(ItemName,Identifier,fromStoreId,toStoreId,qty,stocktype,transaction,itemtype
	,pkgSize,itemUnit,uom,itemBatchId,description,expDt,medicineId,mrp)
{
	var itemtable = document.getElementById("itemListtable");
	var tabLen = itemtable.rows.length;
	var dialogId  = document.getElementById("dialogId").value;
	dialogId = ( empty(dialogId) ? 1 : dialogId);
	var flag = '';

	added_item[tabLen] = ItemName;
	added_identifier[tabLen] = Identifier;

	if (stocktype == true || stocktype == 'true' || stocktype == 't')
	{
		stocktype = true;
		flag = 'cstk';
	} else{
		 flag = '';
	}

	 var checkbox = makeImageButton('itemCheck','itemCheck'+dialogId,'imgDelete',cpath+'/icons/Delete.png');
	checkbox.setAttribute('onclick','cancelRow(this.id,tableRow'+dialogId+','+dialogId+')');

	if(document.getElementById("itemRow"+dialogId).firstChild == null) {
		document.getElementById("itemRow"+dialogId).appendChild(checkbox);
	}
	document.getElementById("itemLabel"+dialogId).textContent = '';
	if (flag != '') document.getElementById("itemLabel"+dialogId).innerHTML = '<img class="flag" src= "'+cpath+'/images/grey_flag.gif"/>'+ItemName;
	else document.getElementById("itemLabel"+dialogId).textContent =ItemName;
	document.getElementById("identifierLabel"+dialogId).textContent =Identifier ;
	document.getElementById("expdtLabel"+dialogId).textContent = expDt ;
	if ( empty(Identifier) )
		document.getElementById("packagetypeLabel"+dialogId).textContent = '' ;
	else
		document.getElementById("packagetypeLabel"+dialogId).textContent = package_type[Identifier] ;
	document.getElementById("transferqtyLabel"+dialogId).textContent = qty;
	document.getElementById("pkgSizeLabel"+dialogId).textContent = pkgSize;
	document.getElementById("descriptionLabel"+dialogId).textContent = description ;

	document.getElementById("pkgSize"+dialogId).value = pkgSize;
	document.getElementById("itemidentification"+dialogId).value = itemtype;
	document.getElementById("item_batch_id"+dialogId).value = itemBatchId;
	document.getElementById("description"+dialogId).value = description;

	document.getElementById("itemUOM"+dialogId).textContent = uom
	document.getElementById("itemUnit"+dialogId).value = itemUnit;
	document.getElementById("item_id"+dialogId).value = medicineId;
	document.getElementById("item_name"+dialogId).value = ItemName;
	document.getElementById("from_store"+dialogId).value = fromStoreId;
	document.getElementById("tranfer_store"+dialogId).value = toStoreId;
	document.getElementById("itemidentifier"+dialogId).value =Identifier ;
	document.getElementById("expdt"+dialogId).value = expDt;
	document.getElementById("trannsfer_qty"+dialogId).value = qty;
	document.getElementById("stk_type"+dialogId).value =stocktype;
	document.getElementById("hdeleted"+dialogId).value = "false" ;
	document.getElementById("hmrp"+dialogId).value = mrp;

	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
	if(nextrow == null){
		AddRowsToGrid(tabLen);
	}
	openDialogBox(eval(dialogId)+1);
	handleCancel();
}
