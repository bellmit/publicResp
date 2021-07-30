
function init() {
	addDialog();
	linenItemAutoComplete();
}

function addDialog() {
	var dialog = document.getElementById('addNewItem');
	dialog.style.display = 'block';
	addNewItemDialog = new YAHOO.widget.Dialog("addNewItem", {
				context : ["itemlistTable", "tr", "br"],
				visible: false,
				modal: true,
				constraintoviewport: true,

				});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeDialog,
	                                              	scope:addNewItemDialog,
	                                              	correctScope:true} );
	addNewItemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	YAHOO.util.Event.addListener("okbtn", "click", addNewItemDetails, addNewItemDialog, true);
	YAHOO.util.Event.addListener("cancelBtn", "click", closeDialog, addNewItemDialog, true);
	addNewItemDialog.render();
}

function showAddNewItemDialog(obj) {
	if (document.getElementById('linen_name'+obj)!=null && document.getElementById('linen_name'+obj).value!="") {
		editLinenItemDetails(obj);
		return false;
	}
	document.getElementById('dialogStatus').value="";
	document.getElementById('dialogId').value = obj;
	resetAddNewItemDialog();
	var row = getThisRow(obj);
	addNewItemDialog.cfg.setProperty("context", [obj, "tl", "tr"], false);
	addNewItemDialog.show();
	document.getElementById('linen_item').focus();
	return false;
}

function editLinenItemDetails(dialogId) {
	document.getElementById('dialogStatus').value = 'edit';
	document.getElementById('dialogId').value = dialogId;
	document.getElementById('linen_item').value = document.getElementById('linen_name'+dialogId).value;
	getItemDetails();
	setSelectedIndex(document.forms[0].batchNo, 
		document.getElementById('batch_no'+dialogId).value+"-"+document.getElementById('qty'+dialogId).value);
	setSelectedIndex(document.forms[0].eCleaningType, document.getElementById('cleaningType'+dialogId).value);
	setSelectedIndex(document.forms[0].eReuse, document.getElementById('reuse'+dialogId).value);
	document.getElementById('remarks').value = document.getElementById('remarks'+dialogId).value;
	addNewItemDialog.show();
	return false;
}

function closeDialog() {
	this.cancel();
	removeRowHighlight();
}

function loadLinenUser() {
	var linenuser = document.forms[0].temp_linen_user;
	var categoryId = document.forms[0].temp_linen_category.value;
	linenuser.selectedIndex = 0;
	linenuser.length = 1;
	var index = 1;
	for (var i=0;i<linenUsersNames.length;i++) {
		var item = linenUsersNames[i];
		if (categoryId==item.CATEGORY_ID) {
			linenuser.length = parseFloat(index)+parseFloat(1);
			linenuser.options[index].text = item.CATEGORY_USER_NAME;
			linenuser.options[index].value = item.CATEGORY_USER_ID;
			index++;
		}
 	}
}

var userArray = [];
var userId = [];
function userAutoComplete(fieldName,containerName) {

	userArray.length = linenUsersNames.length;
	userId.length = linenUsersNames.length;

	for (i=0 ; i< linenUsersNames.length; i++) {
		var item = linenUsersNames[i]
		userArray[i] = item["CATEGORY_USER_NAME"];
		userId[i] = item["CATEGORY_USER_ID"];
	}

	var datasource = new YAHOO.widget.DS_JSArray(userArray, { queryMatchContains : true } );
	var autoComp = new YAHOO.widget.AutoComplete(fieldName,containerName,datasource);

	autoComp.formatResult = Insta.autoHighlight;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.forceSelection = false;
	autoComp.animVert = false;
	if (fieldName=="addNewUser") {
		autoComp.itemSelectEvent.subscribe(setUserId);
  	} else {
  		autoComp.itemSelectEvent.subscribe(setLinenUserId);
  	}
}

var setUserId = function(sType, aArgs) {
	 var oData = aArgs[2];
	 for (i=0 ; i< linenUsersNames.length; i++) {
	 	var item = linenUsersNames[i];
	 	if (oData==item["CATEGORY_USER_NAME"]) {
	 		document.getElementById('addNewUserId').value = item["CATEGORY_USER_ID"];
	 	}
	 }
}

var setLinenUserId = function(sType, aArgs) {
	 var oData = aArgs[2];
	 for (i=0 ; i< linenUsersNames.length; i++) {
	 	var item = linenUsersNames[i];
	 	if (oData==item["CATEGORY_USER_NAME"]) {
	 		document.getElementById('linen_user_id').value = item["CATEGORY_USER_ID"];
	 	}
	 }
}

var linenItemNameArray = [];
function linenItemAutoComplete() {

	linenItemNameArray.length = linenItems.length;

	for (i=0 ; i< linenItems.length; i++) {
		var item = linenItems[i]
		linenItemNameArray[i] = item["MEDICINE_NAME"];
	}

	var datasource = new YAHOO.widget.DS_JSArray(linenItemNameArray, { queryMatchContains : true } );
	var autoComp = new YAHOO.widget.AutoComplete('linen_item','linenItemContainer',datasource);

	autoComp.formatResult = Insta.autoHighlight;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.forceSelection = false;
	autoComp.animVert = false;
	autoComp.itemSelectEvent.subscribe(getItemDetails);
}

function getItemDetails() {
	var item = YAHOO.util.Dom.get('linen_item').value;
	var url;
	var ajaxReqObject = newXMLHttpRequest();
	url = "LinenChange.do?_method=getItemDetails&item_name="+encodeURIComponent(item);
	getResponseHandlerText(ajaxReqObject, fillItemDetails, url);
}

var itemdetails = null;
function fillItemDetails(responseText) {
	 eval("itemdetails = " + responseText);
	 var index = 1;
   	 document.getElementById("batchNo").length = 1+itemdetails.length;
	 for (var i=0; i<itemdetails.length; i++) {
	    document.getElementById("batchNo").options[index].text = itemdetails[i].batch_no+" - "+itemdetails[i].qty;
	    document.getElementById("batchNo").options[index].value = itemdetails[i].batch_no+"-"+itemdetails[i].qty;
	    index++;
	 }
}

function addNewItemDetails() {
	var dialogId  = document.getElementById("dialogId").value;
	var itemtable = document.getElementById("itemlistTable");
	var len = itemtable.rows.length;

	var itemName = document.getElementById('linen_item').value;
		if (itemName=="") {
			alert("Select the Linen Item");
			document.getElementById('linen_item').focus();
			return false;
		}
	var batchNo = document.getElementById('batchNo').value;
		if (document.getElementById('batchNo').selectedIndex==0) {
			alert("Select the Batch / Sl No");
			document.getElementById('batchNo').focus();
			return false;
		}
	var batch = batchNo.split("-")[0];
	var qty = batchNo.split("-")[1];
	var remarks = document.getElementById('remarks').value;
	var cleaningType = document.getElementById('eCleaningType').value;
	var reuse = document.getElementById('eReuse').value;
	
	addValues(dialogId,itemName,batch,qty,remarks,cleaningType,reuse);	

	return true;
}

function addValues(dialogId,itemName,batchNo,qty,remarks,cleaningType,reuse) {

	document.getElementById('linenName_label'+dialogId).innerHTML = itemName;
	document.getElementById('linen_name'+dialogId).value = itemName;
	document.getElementById('batchNo_label'+dialogId).innerHTML = batchNo;
	document.getElementById('batch_no'+dialogId).value = batchNo;
	document.getElementById('qty_label'+dialogId).innerHTML = qty;
	document.getElementById('qty'+dialogId).value = qty;
	document.getElementById('remarks_label'+dialogId).innerHTML = remarks;
	document.getElementById('remarks'+dialogId).value = remarks;
	document.getElementById('cleaningType'+dialogId).value = cleaningType;
	document.getElementById('reuse'+dialogId).value = reuse;
	
	var editButton = document.getElementById("add"+dialogId);
	var eBut =  document.getElementById("addBut"+dialogId);
	editButton.setAttribute("src",popurl+'/icons/Edit.png');
	eBut.setAttribute("title", "Edit Item");
	eBut.setAttribute("accesskey", "");
	
	var imgbutton = makeImageButton('itemCheck','itemCheck'+dialogId,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this.id,tableRow'+dialogId+','+dialogId+')');
	document.getElementById("hdeleted"+dialogId).value = "false" ;
	if(document.getElementById("itemRow"+dialogId).firstChild == null) {
		document.getElementById("itemRow"+dialogId).appendChild(imgbutton);
	}

	
	if (document.getElementById('dialogStatus').value=="") {
		addNewRowToGrid(eval(dialogId)+1);
	} else {
		addNewItemDialog.cancel();
		resetAddNewItemDialog();
	}
}

function addNewRowToGrid(tabLen) {
	var dialogId  = document.getElementById("dialogId").value;
	var itemtable = document.getElementById("itemlistTable");
	var tdObj="",trObj="";
	var row = "tableRow" + tabLen;
	
	var linenLabel 	= makeLabel('linenName_label'+tabLen,'','');
	var linenName 	= makeHidden('linen_name','linen_name'+tabLen,'');
	var batchLabel 	= makeLabel('batchNo_label'+tabLen,'','');
	var batchNo 	= makeHidden('batch_no','batch_no'+tabLen,'');
	var qtyLabel 	= makeLabel('qty_label'+tabLen,'','');
	var qty 		= makeHidden('qty','qty'+tabLen,'');
	var remarksLabel= makeLabel('remarks_label'+tabLen,'','');
	var remarks 	= makeHidden('remarks','remarks'+tabLen,'');
	var cleaningType 	= makeHidden('cleaningType','cleaningType'+tabLen,'');
	var reuse 	= makeHidden('reuse','reuse'+tabLen,'');
	var hdeleteLabel 	= makeLabel('itemRow'+tabLen,'','');
	var hdelete 	= makeHidden('hdeleted','hdeleted'+tabLen,'');
	
	var buton = makeButton1("addBut", "addBut"+tabLen);
	buton.setAttribute("class", "imgButton");
	buton.setAttribute("onclick","showAddNewItemDialog('"+tabLen+"'); return false;");
	buton.setAttribute("title", "Add New Item (Alt_Shift_+)");
	buton.setAttribute("accesskey", "+");
	var itemrowbtn = makeImageButton('add','add'+tabLen,'imgAdd',cpath+'/icons/Add.png');
	buton.appendChild(itemrowbtn);
	
	trObj = itemtable.insertRow(tabLen);
	trObj.id = row;
	
	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(linenLabel);
	tdObj.appendChild(linenName);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(batchLabel);
	tdObj.appendChild(batchNo);
	
	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(qtyLabel);
	tdObj.appendChild(qty);
	
	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(remarksLabel);
	tdObj.appendChild(remarks);
	tdObj.appendChild(cleaningType);
	tdObj.appendChild(reuse);
	
	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(hdeleteLabel);
	tdObj.appendChild(hdelete);
	
	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(buton);
	
	showAddNewItemDialog(eval(dialogId)+1);
	resetAddNewItemDialog();
}

function cancelRow(imgObj,btn,len){
	var deletedInput = document.getElementById('hdeleted'+len);
	if(deletedInput.value == 'false'){
		YAHOO.util.Dom.get(btn).disabled = true;
		deletedInput.value = 'true';
		document.getElementById("add"+len).disabled = true;
		document.getElementById(imgObj).src = cpath+"/icons/Deleted.png";
	} else {
		deletedInput.value = 'false';
		YAHOO.util.Dom.get(btn).disabled = false;
		document.getElementById("add"+len).disabled = false;
		document.getElementById(imgObj).src = cpath+"/icons/Delete.png";
	}
}

function resetAddNewItemDialog() {
	document.getElementById('linen_item').value = "";
	document.getElementById('batchNo').selectedIndex=0;
	document.getElementById('remarks').value="";
	document.getElementById('eCleaningType').selectedIndex=0;
	document.getElementById('eReuse').selectedIndex=0;
}

function makeButton1(name, id, value) {
	var el = document.createElement("button");

	if (name!=null && name!="")
		el.name= name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
}

function editDialog() {
	var editDialog = document.getElementById('editLinenItemDialog');
	editDialog.style.display = 'block';
	editLinenItemDialog = new YAHOO.widget.Dialog("editLinenItemDialog", {
				context : ["editLinenItemTable", "tr", "br"],
				visible: false,
				modal: true,
				constraintoviewport: true,
				});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeDialog,
	                                              	scope:editLinenItemDialog,
	                                              	correctScope:true} );
	editLinenItemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editLinenItemDialog.render();
}

function showEditDialog(obj) {
	var linenItemListTable = document.getElementById("editLinenItemTable");
	var row = linenItemListTable.rows[obj];
	row.className = 'selectedRow';
	document.getElementById('dialogId').value = obj;
	loadLinenItemValues(obj);
	var row = getThisRow(obj);
	editLinenItemDialog.cfg.setProperty("context", [obj, "tl", "tr"], false);
	editLinenItemDialog.show();
	return false;
}

function loadLinenItemValues(len) {
	document.getElementById('linenNameLabel').innerHTML = document.getElementById('linen_item_name'+len).value;
	document.getElementById('batchNoLabel').innerHTML = document.getElementById('batch_no'+len).value;
	document.getElementById('qtyLabel').innerHTML = document.getElementById('qty'+len).value;
	document.getElementById('eRemarks').value = document.getElementById('issue_remarks'+len).value;
	setSelectedIndex(document.forms[0].eCleaningType, document.getElementById('cleaning_type'+len).value);
	setSelectedIndex(document.forms[0].eReuse, document.getElementById('reuse_flag'+len).value);
	setSelectedIndex(document.forms[0].eStatus, document.getElementById('status'+len).value);
}

function addItems(val) {
	updateValues();
	var dialogId = eval(document.forms[0].dialogId.value);
	var gridLen = parseInt(document.getElementById("editLinenItemTable").rows.length)-2;
	var index = dialogId+1;

	if (val.name == 'prevDialog') {
		index = dialogId-1;
		if (dialogId == 1 || dialogId == '');
		else {
			showEditDialog(index);
		}
	} else {
		showEditDialog(index);
	}
} 

function updateValues() {
	var id = document.getElementById('dialogId').value;
	document.getElementById('label_issue_remarks'+id).innerHTML = document.getElementById('eRemarks').value;
	document.getElementById('issue_remarks'+id).value = document.getElementById('eRemarks').value;
	
	document.getElementById('label_cleaning_type'+id).innerHTML = 
		document.getElementById('eCleaningType').options[document.getElementById('eCleaningType').options.selectedIndex].text;
	document.getElementById('cleaning_type'+id).value = document.getElementById('eCleaningType').value;
	
	if (document.getElementById('eReuse').value != " ") {
		document.getElementById('label_reuse_flag'+id).innerHTML = 
			document.getElementById('eReuse').options[document.getElementById('eReuse').options.selectedIndex].text;
		document.getElementById('reuse_flag'+id).value = document.getElementById('eReuse').value;
	}
	
	if (document.getElementById('eStatus').value != " ") {
		document.getElementById('label_status'+id).innerHTML = 
			document.getElementById('eStatus').options[document.getElementById('eStatus').options.selectedIndex].text;
		document.getElementById('status'+id).value = document.getElementById('eStatus').value;
	}
	
	editLinenItemDialog.cancel();
	resetEditDialog();
}

function resetEditDialog() {
	var id = document.getElementById('dialogId').value;
	document.forms[0].eCleaningType.selectedIndex=0;
	document.forms[0].eReuse.selectedIndex=0;
	document.forms[0].eStatus.selectedIndex=0;
	var itemListTable = document.getElementById("editLinenItemTable");
	var row = itemListTable.rows[id];
	row.className = '';
}

function removeRowHighlight() {
	var editLinenItemTable = document.getElementById("editLinenItemTable");
	var totalNoOfRows = editLinenItemTable.rows.length-1;
	for (var i=1;i<=totalNoOfRows;i++) {
		var row = editLinenItemTable.rows[i];
		row.className = '';
	}
}

function validate() {
	if (document.getElementById('temp_linen_category').selectedIndex==0) {
		alert("Select the Linen Category");
		document.getElementById('temp_linen_category').focus();
		return false;
	}
	if (document.forms[0].temp_linen_user.selectedIndex==0) {
		alert("Select the Linen User");
		document.forms[0].temp_linen_user.focus();
		return false;
	}
	document.forms[0].submit();
}

