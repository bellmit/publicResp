	function Reset(){

		var item_table = document.getElementById("medtabel");
		var innertablelength = item_table.rows.length-1;
		if(innertablelength > 0){
			for(var i =innertablelength;i>0;i--){
					item_table.deleteRow(i);
			}
			src = "/icons/Add.png";
			AddRowsToGrid(1,src);
		}


		enableDiv();
	}

	function getCategoriesNew() {

		var storeid = document.getElementById("store_id").value;
		var selItem = document.forms[0].item.value;
		document.StockStatusAdjustForm.identifier.value = '';
		if(stockAdjustmentDetails == null || stockAdjustmentDetails.length == 0){
			Reset();
		}
		for(var j = 0;j<itList.length;j++){
			if(selItem == itList[j].MEDICINE_NAME){
				itemId = itList[j].MEDICINE_ID;

			}
		}
		var ajaxReqObject = newXMLHttpRequest();
		var url="StoresStockAdjust.do?_method=getCategories&storeid="+storeid+"&itemId="+itemId;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				handleCategoryResponse (ajaxReqObject.responseText);
			}
		}
	}





	function getCategories() {

		var storeid = document.getElementById("store_id").value;
		document.StockStatusAdjustForm.identifier.value = '';
		if(stockAdjustmentDetails == null || stockAdjustmentDetails.length == 0){
			Reset();
		}
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

	function handleCategoryResponse(responseText){
	 	var storeid = document.getElementById("store_id").value;
	 	var selCategory = document.forms[0].category.value
		var obj = document.getElementById("category");
		obj.length=1;
	    if (responseText==null) return;
		if (responseText=="") return;
	    eval("categoryList = " + responseText);
		var k = 0;


	}

	var oAutoComp;
   	function getItemslist(){
		var store = document.getElementById("store_id").value;
   		var ajaxReqObject = newXMLHttpRequest();
   		document.forms[0].item.value = '';
   		document.forms[0].itemid.value = '';
		document.forms[0].identifier.value = '';
		enableDiv();

		var url="StoresStockAdjust.do?_method=getItems&storeid="+store;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				handleItemsResponse (ajaxReqObject.responseText);
			}
		}
//		getResponseHandlerText(ajaxReqObject, handleItemsResponse, url);
	}

	function handleItemsResponse(responseText){
		var category = document.forms[0].category.value;
		var store = document.forms[0].store_id.value;
		if (responseText==null) return;
		if (responseText=="") return;
	    eval("itList = " + responseText);

		YAHOO.example.itemArray = [];
			var i=0;

			for(var j = 0;j<itList.length;j++){
				if(store == itList[j].DEPT_ID){
					YAHOO.example.itemArray.length = i+1;
					YAHOO.example.itemArray[i] = {
						MEDICINE_NAME : itList[j].MEDICINE_NAME,
						MEDIICNE_ID : itList[j].MEDICINE_ID,
						ITEM_BARCODE_ID : itList[j].ITEM_BARCODE_ID
					}
					i++;
				}
			}

		if (oAutoComp != undefined) {
			oAutoComp.destroy();
		}

		YAHOO.example.ACJSArray = new function() {
			datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.itemArray);
				datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "MEDICINE_NAME"} ]
		 	};
			oAutoComp = new YAHOO.widget.AutoComplete('item','itemcontainer', datasource);

			oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			oAutoComp.typeAhead = false;
			oAutoComp.useShadow = false;
			oAutoComp.minQueryLength = 0;
			oAutoComp.allowBrowserAutocomplete = false;
			oAutoComp.maxResultsDisplayed = 50;
			oAutoComp.autoHighlight = true;
			oAutoComp.forceSelection = false;
			oAutoComp.itemSelectEvent.subscribe(getItemIdentifiers);
		}
	}

	function getItemBarCodeDetails (val) {
	if (val == '') {
		resetMedicineDetails();
		document.forms[0].item.value = '';
		return;
	}//alert(val + "---"+YAHOO.example.itemArray.length);
	var flag = false;
	for (var m=0;m<YAHOO.example.itemArray.length;m++) {
	     var item = YAHOO.example.itemArray[m];//alert(val + "== "+item.ITEM_BARCODE_ID);
	     if (val == item.ITEM_BARCODE_ID ) {
	     	var itmName = item.MEDICINE_NAME;
	     	var elNewItem = matches(itmName, oAutoComp);//alert(oAutoComp);
			oAutoComp._selectItem(elNewItem);
	     	getItemIdentifiers ();
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	resetMedicineDetails();
	 	document.forms[0].item.value = '';
	 }
}
function resetMedicineDetails () {
	document.getElementById("status").style.display = 'none';
	document.forms[0].item.value = '';
	document.forms[0].barCodeId.value = '';
	document.forms[0].itemid.value = '';
	document.forms[0].identifier.value = '';
	document.forms[0].expiry_dt.value = '';
	document.forms[0].statusqty.value = '';
	document.forms[0].stsremarks.value = '';
	document.forms[0].statussource.value = 'empty';
	document.forms[0].statusdest.value = 'empty';
	document.forms[0].statusqty.value = '';

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
   	function getItemIdentifiers(){
  		if (identAuto||identAuto != undefined ) {
			identAuto.destroy();
			identAuto = null;
		}
	   	var selItem = document.forms[0].item.value;
	   	var store = document.forms[0].store_id.value;
		var itemId='';

		document.forms[0].identifier.value = '';
		enableDiv();

		for(var j = 0;j<itList.length;j++){
			if(selItem == itList[j].MEDICINE_NAME){
				itemId = itList[j].MEDICINE_ID;

			}
		}


		if (itemId != ''){
			var ajaxReqObject = newXMLHttpRequest();
			var url="StoresStockAdjust.do?_method=getIdentifiers&storeid="+store+"&itemid="+itemId;
			ajaxReqObject.open("POST",url.toString(), false);
			ajaxReqObject.send(null);
			if (ajaxReqObject.readyState == 4) {
				if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					handleIdentifierResponse (ajaxReqObject.responseText);
				}
			}

		}
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
		document.forms[0].barCodeId.value = itemList[0].ITEM_BARCODE_ID;

	/**	if (oAutoComp != undefined) {
			oAutoComp.destroy();
		}*/

		itemDetailsListForUpdate = itemList;

		YAHOO.example.ACJSArray = new function(){

			identDatasource = new YAHOO.widget.DS_JSArray(YAHOO.example.identifierArray);
			identAuto = new YAHOO.widget.AutoComplete('identifier','identifiercontainer',identDatasource);
			identAuto.prehighlightClassName = "yui-ac-prehighlight";
			identAuto.typeAhead = true;
			identAuto.useShadow = false;
			identAuto.minQueryLength = 0;
			identAuto.allowBrowseAutocomplete = false;
			identAuto.maxResultsDisplayed = 5;
			identAuto.autoHighlight = true;
			identAuto.forceSelection = false;
			identAuto.itemSelectEvent.subscribe(setDetails);
		}
		if(YAHOO.example.identifierArray.length == 1) {
			document.forms[0].identifier.value = YAHOO.example.identifierArray[0];
			setDetails();
		}
   	}



var qtyAvbl;
var qtyMaint;
var qtyLost;
var qtyUnknown;
var qtyRetired;
var issueUnits;

function setDetails(){
	var itemDetailsList = itemList;
	getDetails(itemDetailsList);
	checkId(1);
}

function getDetails(itemDetailsList){
	   	var selId = document.getElementById("identifier").value;
		var store = document.getElementById("store_id").value;
		enableDiv();
		for ( i=0; i<itemDetailsList.length;i++){
			var item = itemDetailsList[i];
			if (selId == item["BATCH_NO"] && store == item["DEPT_ID"]){
               	var stocktype = item["CONSIGNMENT_STOCK"];

				issueUnits = item["ISSUE_UNITS"];

				document.getElementById("expiry_dt").value = item["EXP_DT"];
				document.StockStatusAdjustForm.e_item_batch_id.value = item["ITEM_BATCH_ID"];
				document.getElementById("issue_units").value = issueUnits;
		   		var row = document.createElement("TR");
		   		if (stocktype == 't') row.className = "cstk";
		   		else row.className = "";

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

   	/* This function is used when adding items to the grid. A few changes made for when this function is used for adding items from Rejected Indents dashboard.
   	For such items, we should not be showing the add new row and the delete icon. Also, in such cases, the dialog should close after we modify the item at hand.
   	Leaving the dialog open will mean that the user can add more items, which is not allowed.
   	Such items have indent_no value set on them, since they are coming from an indent. So, based on whether indent_no is set or not, we can differentiate the behavior*/

   	function addItem(){
		var dialogId  = document.getElementById("dialogId").value;
	    var medtabel = document.getElementById("medtabel");
		var tabLen = medtabel.rows.length;

		var flag = '';
		var imgbutton = makeImageButton('imgdelete','imgdelete'+dialogId,'deleteIcon',cpath+'/icons/Delete.png');
		imgbutton.setAttribute('onclick','cancelRow(this.id,itemRow'+dialogId+','+dialogId+')');

		var stockType = document.forms[0].stkyype.value;
	   	if (stockType == 't')
	   		 flag = "cstk";
		else
			 flag = "";

	//	var category = document.forms[0].category;

		var catText = document.forms[0].categoryName.value;
		document.getElementById("categoryLabel"+dialogId).textContent	= catText;
		setNodeText(document.getElementById("categoryLabel"+dialogId), catText, 20);
		document.getElementById("itemLabel"+dialogId).textContent = '';
		var itemLabel = document.forms[0].item.value;
		itemLabel = itemLabel.length > 25 ? itemLabel.substring(0, 25) + "..." : itemLabel;
		if (flag != '') document.getElementById("itemLabel"+dialogId).innerHTML = '<img class="flag" src= "'+cpath+'/images/grey_flag.gif"/>'+itemLabel;
		else setNodeText(document.getElementById("itemLabel"+dialogId), document.forms[0].item.value, 20);
		document.getElementById("identifierLabel"+dialogId).textContent = document.forms[0].identifier.value;
		document.getElementById("expiryLabel"+dialogId).textContent = document.forms[0].expiry_dt.value;
		document.getElementById("issueUnitsLabel"+dialogId).textContent = document.forms[0].issue_units.value;
	    var remarks;
	    var description;
	    var inctype='';
		var stockstatus='';
		var statusSrc = '';
		var statusDst = '';

      	qty = document.forms[0].statusqty.value;
      	remarks = document.forms[0].stsremarks.value;
      	statusSrc = document.forms[0].statussource.value;
      	statusDst = document.forms[0].statusdest.value;

      	description = qty + " moved from "+document.forms[0].statussource.options[document.forms[0].statussource.selectedIndex].text +" to "+
		document.forms[0].statusdest.options[document.forms[0].statusdest.selectedIndex].text;


		var rem = remarks.length > 15 ? remarks.substring(0, 15) + "..." : remarks;
		setNodeText(document.getElementById("descriptionLabel"+dialogId), description, 20);
		setNodeText(document.getElementById("remarksLabel"+dialogId), remarks, 15);
		document.getElementById("hdeleted"+dialogId).value = "false" ;
		document.getElementById("category_id"+dialogId).value = document.forms[0].category.value;
		document.getElementById("item_id"+dialogId).value =document.getElementById('itemid').value;
		document.getElementById("item_identifier"+dialogId).value = document.forms[0].identifier.value ;
		document.getElementById("item_batch_id"+dialogId).value = document.StockStatusAdjustForm.e_item_batch_id.value;
		document.getElementById("expiry_date"+dialogId).value = document.forms[0].expiry_dt.value;
		document.getElementById("expiry_date"+dialogId).readOnly = true;
		document.getElementById("description"+dialogId).value =description;
		document.getElementById("stype"+dialogId).value =stockType;
		document.getElementById("remarks"+dialogId).value = remarks ;
		document.getElementById("incType"+dialogId).value = inctype ;
		document.getElementById("stockStatus"+dialogId).value = stockstatus ;
		document.getElementById("qty"+dialogId).value = qty ;
		document.getElementById("statusSrc"+dialogId).value = statusSrc ;
		document.getElementById("statusDst"+dialogId).value = statusDst ;
		if((document.getElementById("itemRow"+dialogId).firstChild == null) && (document.getElementById("indent_no"+dialogId).value == '')){
			/** only append deletebutton in case of new items added..do not need it when we are coming from rejected indents*/
			document.getElementById("itemRow"+dialogId).appendChild(imgbutton);
			document.getElementById("add"+dialogId).src = cpath+"/icons/Edit.png";
			var editBut = document.getElementById("addBut"+dialogId);
			editBut.setAttribute("title", "Edit Item");
			editBut.setAttribute("accesskey", "");
		}
		var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
		if((nextrow == null) && (document.getElementById("indent_no"+dialogId).value == '')) {
			src = "/icons/Add.png";
			AddRowsToGrid(tabLen,src);
		}

		if (document.getElementById("indent_no"+dialogId).value == ''){
			openDialogBox(eval(dialogId)+1);
		} else{
			dialog.hide();
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

	function AddRowsToGrid(tabLen,strsrc){
		var itemtable = document.getElementById("medtabel");
		var tdObj="",trObj="";
		var row = "tableRow" + tabLen;

		var deleteLabel			= makeLabel('itemRow','itemRow'+tabLen,'');
		var categoryLabel		= makeLabel('categoryLabel','categoryLabel'+tabLen,'');
		var itemLabel			= makeLabel('itemLabel','itemLabel'+tabLen,'');
		var identifierLabel 	= makeLabel('identifierLabel','identifierLabel'+tabLen,'');
		var expiryLabel         = makeLabel('expiryLabel','expiryLabel'+tabLen,'');
		var descriptionLabel	= makeLabel('descriptionLabel','descriptionLabel'+tabLen,'');
		var remarksLabel		= makeLabel('remarksLabel','remarksLabel'+tabLen,'');
		var issueUnitsLabel		= makeLabel('issueUnitsLabel','issueUnitsLabel'+tabLen,'');

		var hdeletedHidden 		= makeHidden('hdeleted','hdeleted'+tabLen,'');
		var category_idHidden 	= makeHidden('category_id','category_id'+tabLen,'');
		var item_idHidden		= makeHidden('item_id','item_id'+tabLen,'');
		var item_identifierHidden= makeHidden('item_identifier','item_identifier'+tabLen,'');
		var itemBatchIdHidden = makeHidden('item_batch_id','item_batch_id'+tabLen,'');
		var expiry_dateHidden   = makeHidden('expiry_date', 'expiry_date'+tabLen,'');
		var descriptionHidden	= makeHidden('description','description'+tabLen,'');
		var stypeHidden 		= makeHidden('stype','stype'+tabLen,'');
		var remarksHidden		= makeHidden('remarks','remarks'+tabLen,'');
		var incTypeHidden 		= makeHidden('incType','incType'+tabLen,'');
		var stockStatusHidden 	= makeHidden('stockStatus','stockStatus'+tabLen,'');
		var qtyHidden 			= makeHidden('qty','qty'+tabLen,'');
		var statusSrcHidden 	= makeHidden('statusSrc','statusSrc'+tabLen,'');
		var statusDstHidden 	= makeHidden('statusDst','statusDst'+tabLen,'');
		var qtyRejectedHidden 	= makeHidden('qty_rejected','qty_rejected'+tabLen,'');
		var indentnoHidden 	= makeHidden('indent_no','indent_no'+tabLen,'');
		var src = cpath+strsrc;

		var buton = makeButton1("addBut", "addBut"+tabLen);
		buton.setAttribute("class", "imgButton");
		buton.setAttribute("onclick","openDialogBox('"+tabLen+"'); return false;");
		buton.setAttribute("title", "Add New Item (Alt_Shift_+)");
		buton.setAttribute("accesskey", "+");
		var itemrowbtn = makeImageButton('add','add'+tabLen,'imgAdd',src);
		buton.appendChild(itemrowbtn);

		trObj = itemtable.insertRow(tabLen);
		trObj.id = row;

		tdObj = trObj.insertCell(-1);
		/*tdObj.appendChild(deleteLabel);
		tdObj.appendChild(hdeletedHidden);
		*/
		tdObj.appendChild(categoryLabel);
		tdObj.appendChild(category_idHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemLabel);
		tdObj.appendChild(item_idHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(identifierLabel);
		tdObj.appendChild(item_identifierHidden);
		tdObj.appendChild(itemBatchIdHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(expiryLabel);
		tdObj.appendChild(expiry_dateHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(descriptionLabel);
		tdObj.appendChild(descriptionHidden);
		tdObj.appendChild(stypeHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(remarksLabel);
		tdObj.appendChild(remarksHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(issueUnitsLabel);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(incTypeHidden);
		tdObj.appendChild(stockStatusHidden);
		tdObj.appendChild(qtyHidden);
		tdObj.appendChild(statusSrcHidden);
		tdObj.appendChild(statusDstHidden);
		tdObj.appendChild(qtyRejectedHidden);
		tdObj.appendChild(indentnoHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(deleteLabel);
		tdObj.appendChild(hdeletedHidden);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(buton);


	}

   function enableDiv(){
      	document.getElementById("status").style.display = 'block';
      	document.getElementById("stIssueUnits").innerHTML = issueUnits;

    }

     function setStatus(){
	     var srcStatus = document.forms[0].statussource.value;
	     var destStatus = document.forms[0].statusdest.value;
		//     	empty,A,M,L,U
		//	if ()
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


	function validate(){
		var store = document.forms[0].store_id.value;
		var category = document.forms[0].category.value;
		var item = document.forms[0].item.value;
		var itemid = document.forms[0].identifier.value;
		if (store == ''){
			alert("Store is required");
			return false;
		}

		if (category == ''){
			alert("Category is required");
			return false;
		}

		if (item == ''){
			alert("Item is required");
			return false;
		}
		if (itemid == ''){
			alert("Batch/Serial No. is required");
			return false;
		}
		return true;
	}

	function checkId(id){
		document.getElementById("statusqty").readOnly = false;
		var identifierid = document.getElementById('identifier').value;
		if (identifierid == ''){
			document.getElementById("status").style.display = 'none';
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
				document.getElementById("statusqty").value = '1';
				document.getElementById("statusqty").readOnly = true;
			}
		}
	}


	function validateStock(){
		if (validate() ){
			if (document.forms[0].stockstatus.value == 'empty'){
				alert("Stock status is required");
				document.forms[0].stockstatus.focus();
				return false;
			}

			if (document.forms[0].stockqty.value == ''){
				alert("Quantity is required");
				document.forms[0].stockqty.focus();
				return false;
			}
			if (document.forms[0].stockqty.value == 0){
				alert("Quantity should not be zero");
				document.forms[0].stockqty.focus();
				return false;
			}
			var srcQty;
			if (document.forms[0].stockstatus.value == 'A'){
				srcQty = qtyAvbl;
			}else if (document.forms[0].stockstatus.value == 'M'){
				srcQty = qtyMaint;
			}else if (document.forms[0].stockstatus.value == 'L'){
				srcQty = qtyLost;
			}else if (document.forms[0].stockstatus.value == 'U'){
				srcQty = qtyUnknown;
			}else if (document.forms[0].stockstatus.value == 'R'){
				srcQty = qtyRetired;
			}


	/**		if (document.forms[0].incorDec.value == 'R' ){
					if (srcQty < parseInt(document.forms[0].stockqty.value )){
						alert("Quantity can not be decreased");
						return false;
					}
			} */

			addItem();
			return true;
		}
	}

	function validateStatus(){
		if (validate() ){

			if (document.forms[0].statussource.value == 'empty'){
				alert("Source status is required");
				document.forms[0].statussource.focus();
				return false;
			}

			if (document.forms[0].statusdest.value == 'empty'){
				alert("Destination status is required");
				document.forms[0].statusdest.focus();
				return false;
			}
			if (!isValidNumber(document.forms[0].statusqty, qtyDecimal)) return false;

			if (document.forms[0].statusqty.value == ''){
				alert("Quantity is required");
				document.forms[0].statusqty.focus();
				return false;
			}
			if (document.forms[0].statusqty.value == 0){
				alert("Quantity should not be zero");
				document.forms[0].statusqty.focus();
				return false;
			}

			if (document.forms[0].statussource.value == document.forms[0].statusdest.value) {
				alert("Source and Destination Status should not be same");
				return false;
			}

			var srcQty;
			if (document.forms[0].statussource.value == 'A'){
				srcQty = qtyAvbl;
			}else if (document.forms[0].statussource.value == 'M'){
				srcQty = qtyMaint;
			}else if (document.forms[0].statussource.value == 'L'){
				srcQty = qtyLost;
			}else if (document.forms[0].statussource.value == 'U'){
				srcQty = qtyUnknown;
			}else if(document.forms[0].statussource.value == 'R'){
				srcQty = qtyRetired;
			}

			if (parseFloat(document.forms[0].statusqty.value) > parseFloat(srcQty)){
				alert("Quantity can not be greater than available quantity in source");
				return false;
			}

			addItem();
			return true;
		}
	}

	function validateOnSave(){
	    var medtabel = document.getElementById("medtabel");
		var numRows = medtabel.rows.length;
		var totalRows = numRows;

		if (trimAll(document.forms[0].reason.value) == '' ){
			alert("pls enter the Reason");
			document.forms[0].reason.value = '';
			document.forms[0].reason.focus();
			return false;
		}
		for(var i=1;i<numRows-1;i++) {
			if(document.getElementById("hdeleted"+i).value == 'true') {
				totalRows = totalRows-1;
			}
		}
		if (parseInt(totalRows) > 2){
			document.forms[0].action="StockStatusAdjust.do?method=save";
			document.forms[0].submit();
			return true;
		}else{
			alert("Please enter items to save.");
			return false;
		}
	}

function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		if (validateStatus()){
			return true;
		} else {
			return false;
		}
	}

}

/* This function is used for prepopulating the Stock Adjustment Grid in case of Rejected Transfer Indents*/
function prePopulateStockDetails(){
	if(stockAdjustmentDetails == null || stockAdjustmentDetails.length == 0)
		return;

	if ((document.getElementById("dialogId").value == null) || (document.getElementById("dialogId").value == '')){
		document.getElementById("dialogId").value = 1;
	}
	for(var i=0;i<stockAdjustmentDetails.length;i++){
		var numRows = medtabel.rows.length;
		var nextrow =  document.getElementById("tableRow"+dialogId);
		if ((nextrow == null) && (numRows <= stockAdjustmentDetails.length)){
			src = "/icons/Edit.png";
			AddRowsToGrid(numRows,src);
		}
		/* Call function to add the passed in stock details into the Adjustment Grid*/
		addToInnerHTML(numRows, stockAdjustmentDetails[i].CATEGORY, stockAdjustmentDetails[i].CATEGORY_ID, stockAdjustmentDetails[i].MEDICINE_ID,
		stockAdjustmentDetails[i].MEDICINE_NAME, stockAdjustmentDetails[i].BATCH_NO,stockAdjustmentDetails[i].QTY_REJECTED,stockAdjustmentDetails[i].INDENT_NO, stockAdjustmentDetails[i].EXP_DT,
		stockAdjustmentDetails[i].ISSUE_UNITS);
		var nextdialogId = parseInt(document.getElementById("dialogId").value)+1;
		document.getElementById("dialogId").value = nextdialogId;

	}


}


/* This function is called ONLY from the prePopulateStockDetails function. It is used only in case of Rejected Transfer Indents.
It populates the grid with values passed from the Rejected Transfer Indents dashboard.
It also does not append the delete icon and add icon since we are not supposed to add/delete items from this set*/

function addToInnerHTML(numRows, category, categoryId, medicineId, medicineName, identifier,qty_rejected,indent_no,exp_dt,issue_units){
	var dialogId  = document.getElementById("dialogId").value;
	var imgbutton = makeImageButton('itemRow','itemRow'+numRows,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this.id,itemRow'+numRows+','+numRows+')');
	var tdObj="",trObj="";
	var itemtable = document.getElementById("medtabel");
	var tabLen = itemtable.rows.length;
	var row = "tableRow" + numRows;
	document.getElementById('categoryLabel'+dialogId).innerHTML = category;
	document.getElementById('itemLabel'+dialogId).innerHTML = medicineName;
	document.getElementById('identifierLabel'+dialogId).innerHTML = identifier;
	document.getElementById('expiryLabel'+dialogId).innerHTML = exp_dt;
	document.getElementById('descriptionLabel'+dialogId).value = '';
	document.getElementById('remarksLabel'+dialogId).value = '';
	document.getElementById('category_id'+dialogId).value = categoryId;
	document.getElementById('item_id'+dialogId).value = medicineId;
	document.getElementById('qty_rejected'+dialogId).value = qty_rejected;
	document.getElementById('issue_units'+dialogId).value = issue_units;
	document.getElementById('indent_no'+dialogId).value = indent_no;
	if((document.getElementById("itemRow"+dialogId).firstChild == null) && (indent_no == '')){
			document.getElementById("itemRow"+dialogId).appendChild(imgbutton);
	}




}




/*This function does 3 kinds of validation on items being added to the grid from the modal dialog:
Checks for duplicates, validates stock in case of Stock Change and validates status in case of Status Change*/

function handelSubmit(){

	if(document.getElementById("medtabel").rows.length>1){
		 for(var i=1;i<=document.getElementById("medtabel").rows.length-1;i++){
			var currentbatchNo= document.forms[0].identifier.value;
			var currentMedicineId=document.forms[0].itemid.value;
     		if ( (currentbatchNo==document.getElementById("item_identifier"+i).value)
     			&& (currentMedicineId==document.getElementById("item_id"+i).value)
     			 && ( (parseInt(document.getElementById("dialogId").value) ) != i) ){
     			alert("Duplicate Entry");
    			 return false;
     			break;
		 	}
		 }
	}
	if (document.forms[0].category.disabled == true){
		document.forms[0].category.disabled = false;
	}

	validateStatus();

}
function openDialogBox(id){
	var button = document.getElementById("tableRow"+id);
	var indentno = document.getElementById("indent_no"+id).value;
	var categoryid = document.getElementById("category_id"+id).value;
	document.forms[0].barCodeId.value = '';
	if (indentno == ""){
		document.forms[0].category.value 	= document.getElementById("category_id"+id).value ;
		//getItems();
		document.forms[0].item.value 		= document.getElementById("itemLabel"+id).title != '' ? document.getElementById("itemLabel"+id).title : document.getElementById("itemLabel"+id).textContent;
		getItemIdentifiers();
		document.forms[0].identifier.value 	= document.getElementById("identifierLabel"+id).textContent;
		document.forms[0].expiry_dt.value   = document.getElementById("expiryLabel"+id).textContent;
		document.forms[0].expiry_dt.readOnly = true;
		document.forms[0].issue_units.value   = document.getElementById("issueUnitsLabel"+id).textContent;
		getDetails(itemDetailsListForUpdate);
		itemDetailsListForUpdate = "";
		checkId(id);
		document.forms[0].statussource.value 	= document.getElementById("statusSrc"+id).value  ;
		document.forms[0].statusdest.value 	= document.getElementById("statusDst"+id).value ;
		document.forms[0].statusqty.value 	= document.getElementById("qty"+id).value ;
		document.forms[0].stsremarks.value 	= document.getElementById("remarks"+id).value ;

	} else{

		dialog.cfg.setProperty("context",[button, "tr", "br"], false);
		//document.getElementById("dialogId").value = id;
		document.getElementById("identifier").value = document.getElementById("identifierLabel"+id).textContent;
		document.getElementById("expiry_dt").value  = document.getElementById("expiryLabel"+id).textContent;
		document.getElementById("item").value = document.getElementById("itemLabel"+id).title != '' ? document.getElementById("itemLabel"+id).title : document.getElementById("itemLabel"+id).textContent;
		document.getElementById("expiry_dt").readOnly = true;
		document.getElementById("itemid").value = document.getElementById('item_id'+id).value;
		document.getElementById("category").value = categoryid;
		checkId(id);
	}
	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	dialog.show();
	setFocus ();
}
function setFocus () {
	if (prefBarCode == 'Y') document.forms[0].barCodeId.focus();
	else setTimeout("document.forms[0].item.focus()", 100);
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
	resetMedicineDetails();
	//document.supplierdebitform.saveStk.focus();
}

function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 			alert("There is no assigned store, hence you dont have any access to this screen");
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
