var dlgForm;
var adjForm;
var gRowUnderEdit = -1;
var gItemNames;
var gItemLots = {};
var gRowItems = [];
var gColIndexes = [];
var lblIndex = [];

function init(){
	dlgForm = document.dialogForm;
	adjForm = document.StockAdjustmentForm;

	focus();
	getCategories();
	initDialog();
	checkstoreallocation();

    var lblInd = 0;
    gColIndexes.category = lblInd++;
    gColIndexes.medicine_name = lblInd++;
    gColIndexes.batch_no = lblInd++;
    gColIndexes.exp_dt = lblInd++;
    gColIndexes.adjStatus = lblInd++;
    gColIndexes.issue_units = lblInd++;
    gColIndexes.adjRemarks = lblInd++;
    TRASH_COL = lblInd++; EDIT_COL = lblInd++;

}


function getCategories() {

	var storeid = document.getElementById("store_id").value;
	dlgForm.item.value = dlgForm.item.value;
	dlgForm.identifier.value = '';
	var ajaxReqObject = newXMLHttpRequest();
	var url="StoresStockAdjust.do?_method=getCategories&storeid="+storeid;
	ajaxReqObject.open("POST",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			handleCategoryResponse (ajaxReqObject.responseText);
		}
	}

	getItemslist();
}

function getItemslist(){
		var store = document.getElementById("store_id").value;
   		var ajaxReqObject = newXMLHttpRequest();

		clearTable();

		var url="StoresStockAdjust.do?_method=getItems&storeid="+store;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("gItemNames = " + ajaxReqObject.responseText);
				initItemAutoComplete()
			}
		}
	}

function handleCategoryResponse(responseText){
 	var storeid = document.getElementById("store_id").value;
 	var selCategory = dlgForm.category.value
	var obj = document.getElementById("category");
	obj.length=1;
	 if (responseText==null) return;
	 if (responseText=="") return;
     eval("categoryList = " + responseText);
	var k = 0;
}

var oAutoComp;

function refreshItemMaster() {
	// check the timestamp before getting the new master.
	var url = cpath + "/stores/utils.do?_method=getItemMasterTimestamp"
	YAHOO.util.Connect.asyncRequest('GET', url, {success: onGetItemMasterTimestamp});
}

function onGetItemMasterTimestamp(response) {
	if (response.responseText != undefined) {
		var newTimestamp = parseInt(response.responseText);
		if (gItemMasterTimestamp != newTimestamp) {
			var url = cpath + "/pages/stores/getItemMaster.do?addVarName=N&ts=" + newTimestamp;
			gItemMasterTimestamp = newTimestamp;
			YAHOO.util.Connect.asyncRequest('GET', url, { success: initItemAutoComplete });
		}
	}
}
function initItemAutoComplete() {
    if (oAutoComp != undefined) {
        oAutoComp.destroy();
    }

	dataSource = new YAHOO.widget.DS_JSArray(gItemNames);
	dataSource.responseSchema = {
		resultsList: "result", fields: [{key: "cust_item_code_with_name"},{key: "medicine_id"}, {key: "medicine_name"}]
	};

	oAutoComp = new YAHOO.widget.AutoComplete('item', 'itemcontainer', dataSource);
	oAutoComp.maxResultsDisplayed = 50;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.animVert = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	oAutoComp.itemSelectEvent.subscribe(getItemIdentifiers);
}

function getItemBarCodeDetails (val) {
	if (val == '') {
		clearItemDetails();
		return;
	}
	var flag = false;
	var aa = gItemNames;
	for (var m=0;m<gItemNames.length;m++) {
	     var item = gItemNames[m];//alert(val + "== "+item.ITEM_BARCODE_ID);
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, oAutoComp);//alert(oAutoComp);
//			oAutoComp._selectItem(elNewItem);
			oAutoComp._bItemSelected = true;
			clearItemDetails();
			dlgForm.item.value = itmName;
			getItemDetailsForItem(item.medicine_id);
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	clearItemDetails();
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

var identAuto = null;

function getItemIdentifiers(sType, aArgs){
	 dlgForm.item.value = aArgs[2][2];
	 var medicineId = aArgs[2][1];
	 clearItemDetails();
	 if (!isNaN(medicineId))
		 getItemDetailsForItem(medicineId);

}

   	var itemDetailsListForUpdate ="";
   	function handleIdentifierResponse(responseText){
   		var selItem = document.forms[0].item.value;
	   	var store = document.forms[0].store_id.value;
		var itemId;


		if (responseText==null) return;
		if (responseText=="") return;
	    eval("itemList = " + responseText);

   		for(var j = 0;j<itemList.length;j++){
			if(selItem == itemList[j].MEDICINE_NAME){
				itemId = itemList[j].MEDICINE_ID;
			}
		}
		document.forms[0].itemId.value = itemId;
		document.forms[0].itemid.value = itemId;

		YAHOO.example.identifierArray=[];
		var i=0;
		for ( j=0; j<itemList.length;j++){
			var item = itemList[j];
			if (itemId == item["MEDICINE_ID"] && store == item["DEPT_ID"]){
				YAHOO.example.identifierArray.length = i+1;
				YAHOO.example.identifierArray[i] = item["BATCH_NO"];
				i++;
			}
		}

		document.getElementById("category").value = itemList[0].CATEGORY_ID;
		document.getElementById("categoryName").value = itemList[0].CATEGORY;
		dlgForm.item_barcode_id.value = itemList[0].ITEM_BARCODE_ID;
		if(itemList[0].IDENTIFICATION == 'S') {
			document.getElementById('stockqty').value = 1;
			document.getElementById('stockqty').readOnly = true;
		} else {
			document.getElementById('stockqty').value = '';
			document.getElementById('stockqty').readOnly = false;
		}

	/**	if (oAutoComp != undefined) {
			oAutoComp.destroy();
		}*/

		itemDetailsListForUpdate = itemList;


		var identDatasource = new YAHOO.util.LocalDataSource(YAHOO.example.identifierArray);
		identDatasource.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
		identDatasource.responseSchema = { resultsList : "result",
			fields: [ {key: "batch_no"}, {key: "package_cp"}, {key: "qty"},
					  {key: "item_lot_id"}, {key: "item_batch_id"}],
			numMatchFields: 2
		};

		identAuto = new YAHOO.widget.AutoComplete('identifier', 'identifiercontainer',	identDatasource);
		identAuto.typeAhead = false;
		identAuto.useShadow = true;
		identAuto.allowBrowserAutocomplete = false;
		identAuto.maxResultsDisplayed = 50;
		identAuto.autoHighlight = true;
		identAuto.resultTypeList = false;
		identAuto.minQueryLength = 1;
		identAuto.forceSelection = true;

		identAuto.filterResults = Insta.queryMatchWordStartsWith;
		identAuto.itemSelectEvent.subscribe(setDetails);

		if(YAHOO.example.identifierArray.length == 1) {
			document.forms[0].identifier.value = YAHOO.example.identifierArray[0];
			if (identAuto != null && identAuto._elTextbox.value != '') {
				identAuto._bItemSelected = true;
				identAuto._sInitInputValue = identAuto._elTextbox.value;
			}
			setDetails();
		}
   	}



var qtyAvbl;
var qtyMaint;
var qtyLost;
var qtyUnknown;
var qtyRetired;
var issueUnits;
var gItemDetails = {};
var gDialogItem = null;

this.setDetails = function(sType, aArgs) {
	var item = aArgs[2];
	var itemDetailsList = itemList;
	gItemDetails[item.item_lot_id] = findInList(itemList, 'item_lot_id', item.item_lot_id);
	getDetails(itemDetailsList);
}

function getItemDetailsForItem(medicineId) {
	var storeId = adjForm.store_id.value;

	// item details not in our cache: get it using ajax
	var url = cpath + "/stores/StoresStockAdjust.do?_method=getItemDetails&medicineId=" + medicineId + "&store=" + storeId;

	YAHOO.util.Connect.asyncRequest('GET', url, {
       	success: function(response) {
			eval('var item =' + response.responseText);
			gItemDetails[medicineId] = item;
			populateItemDetails(item);
		},
		failure: onAjaxFailure,
	});
}

function populateItemDetails(item) {
	if (item == null)
		return;

	gDialogItem = newItem();		// with all the defaults
	// set the default values from the item to gDialogItem and transfer that to the dialog
	shallowCopy(item, gDialogItem);
	itemToDialog(gDialogItem);
}

function getDetails(itemDetailsList){
	   	var selId = document.getElementById("identifier").value;
		var store = document.getElementById("store_id").value;
		enableDiv();
		for ( i=0; i<itemDetailsList.length;i++){
			var item = itemDetailsList[i];
				if (selId == item["BATCH_NO"] && store == item["DEPT_ID"]){
                var stocktype = item["CONSIGNMENT_STOCK"];
				var medtabel = document.getElementById("qtyDetails");
				var numRows = medtabel.rows.length;
				var oldrowIndex = numRows - 1;
				var rowCount = numRows-1;
				issueUnits = item["ISSUE_UNITS"];
				document.getElementById('issue_units').value = issueUnits;
				for (j=rowCount; j>0; j--){
					medtabel.deleteRow(j);
				}
				document.StockAdjustmentForm.e_item_batch_id.value = item["ITEM_BATCH_ID"];
				document.getElementById("expiry_dt").value = formatExpiry(new Date(item["EXP_DT"]));
				var numRows = medtabel.rows.length;
				var oldrowIndex = numRows - 1;
			   	var row = document.createElement("TR");
			   	if (stocktype == 't') row.className = "cstk";
			   	else row.className = "";
			   	row.id="detRow"+numRows;
			    var oldRow=document.getElementById("detRow"+oldrowIndex);

				var cell1 = document.createElement("TD");
				cell1.setAttribute("class", "label");
				cell1.setAttribute("title", item["QTY"]);
				cell1.setAttribute("style", "max-width: 15em");
				cell1.setAttribute("id","QTY");
				var text = document.createTextNode(item["QTY"] + " " + item["ISSUE_UNITS"]);
				qtyAvbl = item["QTY"];
				cell1.appendChild(text);

				var cell2 = document.createElement("TD");
				cell2.setAttribute("class", "label");
				cell2.setAttribute("title", item["QTY_IN_USE"]);
				cell2.setAttribute("style", "max-width: 15em");
				cell2.setAttribute("id","QTY_IN_USE");
				var text = document.createTextNode(item["QTY_IN_USE"] + " " + item["ISSUE_UNITS"]);
				cell2.appendChild(text);

				var cell3 = document.createElement("TD");
				cell3.setAttribute("class", "label");
				cell3.setAttribute("title", item["QTY_MAINT"]);
				cell3.setAttribute("style", "max-width: 15em");
				cell3.setAttribute("id","QTY_MAINT");
				var text = document.createTextNode(item["QTY_MAINT"] + " " + item["ISSUE_UNITS"]);
				qtyMaint = item["QTY_MAINT"];
				cell3.appendChild(text);
				var cell4 = document.createElement("TD");
				cell4.setAttribute("class", "label");
				cell4.setAttribute("title", item["QTY_RETIRED"]);
				cell4.setAttribute("id","QTY_RETIRED");
				cell4.setAttribute("style", "max-width: 15em");
				var text = document.createTextNode(item["QTY_RETIRED"] + " " + item["ISSUE_UNITS"]);
				cell4.appendChild(text);

				var cell5 = document.createElement("TD");
				cell5.setAttribute("class", "label");
				cell5.setAttribute("title", item["QTY_LOST"]);
				cell5.setAttribute("style", "max-width: 15em");
				cell5.setAttribute("id","QTY_LOST");
				var text = document.createTextNode(item["QTY_LOST"] + " " + item["ISSUE_UNITS"]);
				qtyLost = item["QTY_LOST"];
				cell5.appendChild(text);

				var cell6 = document.createElement("TD");
				cell6.setAttribute("class", "label");
				cell6.setAttribute("title", item["QTY_UNKNOWN"]);
				cell6.setAttribute("style", "max-width: 15em");
				cell6.setAttribute("id","QTY_UNKNOWN");
				var text = document.createTextNode(item["QTY_UNKNOWN"] + " " + item["ISSUE_UNITS"]);
				qtyUnknown = item["QTY_UNKNOWN"];
				/*var hdstype = document.createElement("INPUT");
				hdstype.setAttribute("type", "hidden");
				hdstype.setAttribute("name", "stype");
				hdstype.setAttribute("id", "stype");
				hdstype.setAttribute("value", item["CONSIGNMENT_STOCK"]);
		        cell6.appendChild(hdstype);*/
				cell6.appendChild(text);
				row.appendChild(cell1);
				row.appendChild(cell2);
				row.appendChild(cell3);
				row.appendChild(cell4);
				row.appendChild(cell5);
				row.appendChild(cell6);

				document.getElementById("tbody2").insertBefore(row, oldRow.nextSibling);
				document.forms[0].stkyype.value = item["CONSIGNMENT_STOCK"];

			}
		}


		var iden;
		if (document.getElementById("category").value != '') {
			for ( i=0; i<categoryList.length;i++){
				var item = categoryList[i];
				if (document.getElementById("store_id").value == item["DEPT_ID"]){
					if (document.getElementById("category").value == item["CATEGORY_ID"]){
						iden = item["IDENTIFICATION"];
					}
				}
			}



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

     function enableDiv(){
     	document.getElementById("stock").style.display = 'block';
     	document.getElementById("stkIssueUnits").innerHTML = issueUnits;
	document.getElementById("issue_units").value = issueUnits;

     }

	function cancelRow(imgObj,btn,len){
		var deletedInput = document.getElementById('hdeleted'+len);
		if(deletedInput.value == 'false'){
			YAHOO.util.Dom.get(btn).disabled = true;
			deletedInput.value = 'true';
			document.getElementById(imgObj).src = cpath+"/icons/Deleted.png";
			document.getElementById("add"+len).src = cpath+"/icons/Edit1.png";
			document.getElementById("addBut"+len).disabled = true;
		} else {
			deletedInput.value = 'false';
			YAHOO.util.Dom.get(btn).disabled = false;
			document.getElementById(imgObj).src = cpath+"/icons/Delete.png";
			document.getElementById("add"+len).src = cpath+"/icons/Edit.png";
			document.getElementById("addBut"+len).disabled = false;
		}
	}

	function validate() {
		var store = adjForm.store_id.value;
		var category = dlgForm.category.value;
		var item = dlgForm.item.value;
		var itemid = dlgForm.identifier.value;
		var stockType = dlgForm.adjStatus.value;
		if (store == ''){
			 dlgForm.store_id.focus();
			showMessage("js.stores.mgmt.storeisrequired");
			return false;
		}

		if (item == ''){
			showMessage("js.stores.mgmt.selectanyitem");
			dlgForm.item.focus();
			return false;
		}
		if (itemid == ''){
			showMessage("js.stores.mgmt.batch.or.serial.no.isrequired");
			dlgForm.identifier.focus();
			return false;
		}
		if ( stockType == 'empty') {
			showMessage("js.stores.mgmt.stocktyperequired");
			dlgForm.adjStatus.focus();
			return false;
		}

		return true;
	}

	function checkId(id){
		document.getElementById("stockqty").readOnly = false;
		var identifierid = document.getElementById('identifier').value;

		if (identifierid == ''){
			document.getElementById("stock").style.display = 'none';
	    	return false;
		}

		enableDiv();
		chkSRB (id);
		return true;
	}

	function chkSRB (id) {
		var iden = '';

		for ( i=0; i<categoryList.length;i++){
			var item = categoryList[i];
			if (document.getElementById("store_id").value == item["DEPT_ID"]){
				if (document.getElementById('category').value == item["CATEGORY_ID"]){
					iden = item["IDENTIFICATION"];
				}
			}
		}
		if (iden != '') {
			if (iden == 'S') {

				/* If this is coming from rejected transfer indents screen, set the qty with the passed in value and do not allow qty to be edited */
				if ((document.getElementById('qty_rejected'+id)!= 'undefined') && (document.getElementById('qty_rejected'+id)!= null) && (document.getElementById('qty_rejected'+id).value != '')){
					document.getElementById("stockqty").value = document.getElementById("qty_rejected"+id).value;
					document.getElementById("stockqty").readOnly = true;
					document.getElementById("item").readOnly = true;
					document.getElementById("category").disabled = true;
					document.getElementById("identifier").readOnly = true;


				}
				document.getElementById("stockqty").readOnly = true;

			} else{

				/* If this is coming from rejected transfer indents screen, set the qty with the passed in value and do not allow qty to be edited */
				if ((document.getElementById('qty_rejected'+id)!= 'undefined') && (document.getElementById('qty_rejected'+id)!= null) && (document.getElementById('qty_rejected'+id).value != '')){
					document.getElementById("stockqty").value = document.getElementById("qty_rejected"+id).value;
					document.getElementById("stockqty").readOnly = true;
					document.getElementById("item").readOnly = true;
					document.getElementById("category").disabled = true;
					document.getElementById("identifier").readOnly = true;

				} else{
					document.getElementById("stockqty").value = '';
				}


			}
		}

	}

	function validateStock(){
		if(!validateDuplicate())
			return false;

		if(!validate()){
			return false;
		}

		if (dlgForm.adjQty_fld.value == '' || dlgForm.adjQty_fld.value == 0){
			showMessage("js.stores.mgmt.quantityisrequired");
			dlgForm.adjQty_fld.focus();
			return false;
		}
		if( isNaN(dlgForm.adjQty_fld.value) ){
			showMessage("js.stores.mgmt.quantity.validnumber");
			dlgForm.adjQty_fld.focus();
			return false;
		}
		if(qtyDecimal == 'N'){
			if(!isNaN(dlgForm.adjQty_fld.value)
				&&  Math.round(dlgForm.adjQty_fld.value) != dlgForm.adjQty_fld.value) {
				if(!isValidNumber(dlgForm.adjQty_fld, qtyDecimal))
					return false;
			}
		}else {
			if(!isValidNumber(dlgForm.adjQty_fld, qtyDecimal))
					return false;
		}
		if ( parseInt(dlgForm.qty.value) < parseInt(dlgForm.adjQty_fld.value) && dlgForm.adjType_fld.value == 'R' ) {
			var msg=getString("js.stores.mgmt.decreasingquantity.notlessthanstockquantity");
			msg+=" ";
			msg+=dlgForm.qty.value;
			alert(msg);
			dlgForm.adjQty_fld.value = 0;
			dlgForm.adjQty_fld.focus();
			return false;

		}

		var iden;
		if (document.getElementById("category").value != '') {
			for ( i=0; i<categoryList.length;i++){
				var item = categoryList[i];
				if (document.getElementById("store_id").value == item["DEPT_ID"]){
					if (document.getElementById("category").value == item["CATEGORY_ID"]){
						iden = item["IDENTIFICATION"];
					}
				}
			}
		}

		return true;

	}


	function validateOnSave(){
	    var medtabel = document.getElementById("medtabel");
		var numRows = medtabel.rows.length;
		var totalRows = numRows;
		var fromRejIndent = false;

		for (var j=1;j < numRows - 1; j++){
			rowObj = getItemRow(i);

			if ( getElementByName(rowObj,'adjStatus').value == null || getElementByName(rowObj,'adjStatus').value == null ){
				var msg=getString("js.stores.mgmt.needtoupdatedestinationofstockonrow");
				msg+=j;
				alert(msg);
				return false;
			}
		}

		if (trimAll(adjForm.reason.value) == '' ){
			showMessage("js.stores.mgmt.plsenterthereason");
			adjForm.reason.value = '';
			adjForm.reason.focus();
			return false;
		}
		if ( parseInt(getNumRows()) == 0 ) {
			showMessage("js.stores.mgmt.pleaseenteritemstosave");
			return false;
		}

		adjForm.action="StoresStockAdjust.do?method=save";
		adjForm.submit();
		return true;
	}

function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		validateStock();
		return false;
	} else {
		return enterNumAndDot(e);
	}
}

function validateDuplicate(){
	var item = gDialogItem;
	if(document.getElementById("medtabel").rows.length>1){
		for (var k = 0; k < gRowItems.length; k++) {
			if (gRowUnderEdit == k) continue;		// self
			var gridItem = gRowItems[k];
			if (gridItem.item_lot_id == item.item_lot_id) {
				showMessage("js.stores.mgmt.duplicateentry");
				return false;
	        }
	    }
	}
	return true;
}

function initDialog(){
	dialog = new YAHOO.widget.Dialog("dialog",
	{
		width:"800px",
		context : ["medtabel", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true
	} );
	dialog.render();
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog,
	                                              	correctScope:true} );
	dialog.cfg.setProperty("keylisteners", [escKeyListener]);

}


function handleCancel() {
	dialog.cancel();
	clearDialog();
}


function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 			showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
		 	document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}
function makeingDec(objValue,obj){
		if (objValue == '') objValue = 0;
	    if (isAmount(objValue)) {
			document.getElementById(obj.name).value = qtyDecimal == 'Y' ? parseFloat(objValue).toFixed(decDigits) : parseFloat(objValue);
		} else document.getElementById(obj.name).value = qtyDecimal == 'Y' ? 0.00 : 0;
}


/*
 * Create a new item, contains a superset of all required fields
 */
function newItem() {
	var item = {
		medicine_name: '', medicine_id: '', batch_no: '', item_barcode_id: '',
		billed_qty: 0, billed_qty_display: 0, bonus_qty: 0, bonus_qty_display: 0,
		cost_price: 0, item_lot_id: 0,lot_source: '',grn_no: '',purchase_type: '',stock_time: '',
		qty_maint: 0,qty_retired: 0,qty_lost: 0,qty_kit: 0,qty_unknown: 0,
		qty_in_transit: 0 ,qty_in_use: 0,package_cp: 0,identification: '',issue_units: '',

		grnmed: 'N', pomed: 'N', pomedrate: 0, pomedrate_display: 0,
		gnrqty: 0, grnbqty: 0, qty_req: 0, qty_received: 0,
		newbatch: 'Y', batches: [],
		adjType: '',adjQty: 0,adjStatus: '',adjRemarks: ''
	};

	return item;
}

function itemToDialog(item) {
	// call common object to form, with dlgForm as the target
	objectToForm(item, dlgForm);

	// initialize auto completes
	if (item.medicine_id != '') {
		oAutoComp._bItemSelected = true;
		initBatchNoAutoComplete(gItemDetails[item.medicine_id].batches);
		if (item.batch_no != '')
			identDatasource._bItemSelected = true;
	}
}

function initBatchNoAutoComplete(batches){

		if (identAuto != undefined) {
	        identAuto.destroy();
	    }

		identDatasource = new YAHOO.widget.DS_JSArray(batches);
		identDatasource.responseSchema = {
			resultsList: "result",
			fields: [  {key: "batch_no"},
					   {key: "qty"},
				       {key: "item_batch_id"},
				       {key: "item_lot_id"},
				       {key: "grn_no"},
				       {key: "lot_source"},
				       {key: "purchase_type"},
				       {key: "package_cp"}
				    ],
			numMatchFields: 2
		};

		identAuto = new YAHOO.widget.AutoComplete('identifier', 'identifiercontainer', identDatasource);
		identAuto.maxResultsDisplayed = 50;
		identAuto.allowBrowserAutocomplete = false;
		identAuto.prehighlightClassName = "yui-ac-prehighlight";
		identAuto.typeAhead = false;
		identAuto.useShadow = false;
		identAuto.animVert = false;
		identAuto.minQueryLength = 0;
		identAuto.forceSelection = true;
		identAuto.filterResults = Insta.queryMatchWordStartsWith;


		identAuto.formatResult = function(oResultData, sQuery, sResultMatch) {
			var batch = oResultData;
			var queryComps = sQuery.split(" ", 2);

			var reStarts = [];
			var reEnds = [];


			for (var i=0; i<queryComps.length; i++) {
				var escapedComp = Insta.escape(queryComps[i]);
				reStarts[i] = new RegExp('^' + escapedComp, 'i');
				reEnds[i] =   new RegExp(escapedComp + '$', 'i');
			}

			var details = highlight(batch[0], reEnds);
			details += " - " + batch[1];
			details += " - " + batch[7];
			return details + " - " +batch[4] + " - " + batch[5] + " - " + batch[6] ;

		}
		identAuto.itemSelectEvent.subscribe(onSelectBatchNo);
}

/*
 *	This will be called on selecting batch number.
 *	We get all lots of the selected batch and load lot autocomplete here
 */

function onSelectBatchNo(sType, aArgs){

	var storeId = adjForm.store_id.value;
	var selectedItemBatchLotId = aArgs == undefined ? gDialogItem.item_lot_id : aArgs[2][3];
	var lotDetails = gItemDetails[gDialogItem.medicine_id].batches;
	for( var i = 0;i < lotDetails.length;i++ ){
		var lot = lotDetails[i];
		if( selectedItemBatchLotId == lot.item_lot_id ) {
			gItemLots[lot.item_lot_id] = lot;
		}
	}
	populateLotDetails(gItemLots[selectedItemBatchLotId]);
}

function populateLotDetails(lot) {
	if (lot == null)
		return;
	dlgForm.expiry_dt.value = formatExpiry(lot.exp_dt);
	shallowCopy(lot, gDialogItem);
	objectToForm(gDialogItem, dlgForm);
	formToLabelIds(dlgForm, 'lbl_', 20);
	enableDiv();
}

function openAddDialog() {

	refreshItemMaster();

	gRowUnderEdit = -1;
	button = document.getElementById("addItem");
	document.getElementById("dialog").style.display = "block";
	dialog.cfg.setProperty("context", [button, "tr", "br"], false);

	gDialogItem = newItem();
	itemToDialog(gDialogItem);
	clearDialog();

	dialog.show();
	if (prefBarCode == 'Y') {
		setTimeout("dlgForm.item_barcode_id.focus()", 100);
	} else {
		setTimeout("dlgForm.item.focus()", 100);
	}
}

onAjaxFailure = function (response) {
	showMessage("js.stores.mgmt.ajaxcallfailed");
}

function onDialogSave() {
	if (!validateStock())
		return false;

	// copy dialog to dialogItem
	formToObject(dlgForm, gDialogItem);
	setAddtionalAdjParams();

	dialogSave();
	return true;
}

function dialogSave() {

	if (gRowUnderEdit == -1) {

		addDialogItemToGrid();
        dialog.cancel();
        openAddDialog();

	} else {

		saveDialogItemToGrid(gRowUnderEdit);
        dialog.cancel();
	}

}

function addDialogItemToGrid() {
	var rowIndex = addRow();
	saveDialogItemToGrid(rowIndex);
}

function saveDialogItemToGrid(rowIndex) {

	gRowItems[rowIndex] = gDialogItem;
	gDialogItem = null;
	rowItemToRow(rowIndex);
	var row = getItemRow(rowIndex);
	row.className = '';
	var descriptionLbl = getSelText(dlgForm.adjType_fld) + " By " + dlgForm.adjQty_fld.value;
	setNodeText(row.cells[4],descriptionLbl);
	setNodeText(row.cells[3],dlgForm.expiry_dt.value);
	getElementByName(row,'description').value = descriptionLbl;

}

function setAddtionalAdjParams() {
	gDialogItem.adjQty = dlgForm.adjQty_fld.value;
	gDialogItem.adjRemarks = dlgForm.adjRemarks_fld.value;
	gDialogItem.adjType = dlgForm.adjType_fld.value;
}



function addRow() {
    var totalNoOfRows = getNumRows();
    var table = document.getElementById("medtabel");
    var templateRow = table.rows[getTemplateRow()];
    var row = templateRow.cloneNode(true);
    row.style.display = '';
    table.tBodies[0].insertBefore(row, templateRow);
	return totalNoOfRows;
}

function getNumRows() {
    // header, hidden template row: totally 3 extra
    return document.getElementById("medtabel").rows.length - 2;
}

function getTemplateRow() {
    // gets the hidden template row index: this follows header row + num charges.
    return getNumRows() + 1;
}

function rowItemToRow(rowIndex) {
	// copy to hidden values in row: call common object to form, with indexed grnForm as the target
	var row = getItemRow(rowIndex);
	objectToHidden(gRowItems[rowIndex], row);
	// copy to labels in table: call common object to label
	objectToRowLabels(gRowItems[rowIndex], row, gColIndexes);
	//description from select box
}

function getItemRow(i) {
    i = parseInt(i);
    var table = document.getElementById("medtabel");
    return table.rows[i + getFirstItemRow()];
}

function getFirstItemRow() {
    return 1;
}

function onEditRow(img) {
    var row = findAncestor(img, "TR");
	openEditDialog(row);
}

function openEditDialog(row) {
    row.className = 'editing';
	gRowUnderEdit = getRowItemIndex(row);

	enableDiv();
    rowItemToDialog(gRowUnderEdit,row);

	var button = row.cells[EDIT_COL];
	document.getElementById("dialog").style.display = "block";
	dialog.cfg.setProperty("context", [button, "tr", "br"], false);

	dialog.show();
    setTimeout("dlgForm.item.focus()", 100);
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

function getRowItemIndex(row) {
    return row.rowIndex - getFirstItemRow();
}

function clearDialog(){
	dlgForm.item.value = '';
	dlgForm.identifier.value = '';
	dlgForm.adjQty_fld.value = '';
	dlgForm.adjType_fld.value = 'A';
	dlgForm.adjRemarks_fld.value = '';
}

function rowItemToDialog(rowIndex,row) {
	// we need a shallow copy so that a cancel will not affect the original
	gDialogItem = {};
	shallowCopy(gRowItems[rowIndex], gDialogItem);
	itemToDialog(gDialogItem, dlgForm);

	dlgForm.item.value = gDialogItem.medicine_name;
	dlgForm.identifier.value = gDialogItem.batch_no;
	dlgForm.adjQty_fld.value = gDialogItem.adjQty;
	dlgForm.adjType_fld.value = gDialogItem.adjType;
	dlgForm.adjRemarks_fld.value = gDialogItem.adjRemarks;

	if (oAutoComp != null && oAutoComp._elTextbox.value != '') {
		oAutoComp._bItemSelected = true;
		oAutoComp._sInitInputValue = oAutoComp._elTextbox.value;
	}
	if (identAuto != null && identAuto._elTextbox.value != '') {
		identAuto._bItemSelected = true;
		identAuto._sInitInputValue = identAuto._elTextbox.value;
	}

	onSelectBatchNo();

}

function clearTable(){
	var len = getNumRows();
	for (var p = len; p >= 1; p--) {
		document.getElementById("medtabel").deleteRow(p);
	}
	gRowItems = [];
}

function clearItemDetails(){
	dlgForm.identifier.value = '';
	dlgForm.adjQty_fld.value = '';
	dlgForm.adjType_fld.value = 'A';
	dlgForm.adjRemarks_fld.value = '';
	dlgForm.expiry_dt.value = '';
}


function StockAdjustAutoComp() {

	initStockAdjust('reason','adjustment_reason');
}

function initStockAdjust(adjustment_reason, AdjustReasonDropDown) {
if (oAutoComp != undefined) {
        oAutoComp.destroy();
    }

	var nameArray = [];
	nameArray.length = StockadjustreasonList.length;
	for ( i=0 ; i< StockadjustreasonList.length; i++){
		var item = StockadjustreasonList[i]["adjustment_reason"];
		nameArray[i] = item;
	}
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(nameArray);
			namelistAuto = new YAHOO.widget.AutoComplete(adjustment_reason, AdjustReasonDropDown, dataSource);
			namelistAuto.maxResultsDisplayed = 10;
			namelistAuto.queryMatchContains = true;
			namelistAuto.allowBrowserAutocomplete = false;
			namelistAuto.prehighlightClassName = "yui-ac-prehighlight";
			namelistAuto.typeAhead = false;
			namelistAuto.useShadow = false;
			namelistAuto.minQueryLength = 0;
			namelistAuto.forceSelection = true;
			namelistAuto.textboxBlurEvent.subscribe(adjustment_reason)
         }
}