/* itemNames AutoComplete */
var oAutoComp = null;
var items = [];
var ajaxLoaded = false;
var oOrderKitAutoComp;
var gItemType = 'itemname';
var gOrderKitItems;

function initItemAutoComplete(indentType) {
	if (oAutoComp != undefined) {
		oAutoComp.destroy();
		oAutoComp = undefined;
	}

	var itemNames = [];
	var j = 0;
	var filterType = getRadioSelection(document.forms[0].filterType);

	var dataSource = null;

	if (filterType == 'A') {
		// master items
		dataSource = new YAHOO.widget.DS_JSArray(jItemNames);

	} else if (filterType == 'I') {
		// storewise items
		var storeIdObj = document.forms[0].indent_store;
		dataSource = new YAHOO.widget.DS_JSArray(jMedicineNames);
	} else {
		var storeIdObj = document.forms[0].store_to;
		dataSource = new YAHOO.widget.DS_JSArray(jMedicineNames);
	}

	dataSource.responseSchema = {
		resultsList: "result",
		fields: [ {key : "cust_item_code_with_name"},{key : "medicine_name"} ]
	};

	oAutoComp = new YAHOO.widget.AutoComplete('itemname', 'item_dropdown', dataSource);
	oAutoComp.maxResultsDisplayed = 200;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	oAutoComp.itemSelectEvent.subscribe(onSelectItem);
	//oAutoComp.selectionEnforceEvent.subscribe(clearDialogDetails);

}

function clearItemAttributes () {
	document.forms[0].pkg_type.value = '';
	document.forms[0].avail_qty.value = '';
	document.forms[0].qty_avbl_for_reqstore.value = '';
	document.forms[0].issue_type_id.value = '';
	document.forms[0].identification.value = '';
	document.forms[0].itemid.value = '';
	document.forms[0].pkg_size.value = '';
}

function clearDialogDetails () {
	clearItemAttributes ();
	document.forms[0].item_qty.value = '';
	document.forms[0].itemname.value = '';
	document.forms[0].issue_units.value = '';
}



function onSelectItem(type,args) {
	clearItemAttributes ();
	var itemname = args[2][1];
	document.forms[0].itemname.value = itemname;
	var val = indentType;

	if (trimAll(itemname) != '') {

		for (var i=0; i<jItemNames.length; i++) {
		      if (val == 'S') {
		      	if (itemname == jItemNames[i].MEDICINE_NAME && jItemNames[i].STATUS == 'I') {
		      		var msg=getString("js.stores.mgmt.indents.item");
		      		msg+=itemname;
		      		msg+=getString("js.stores.mgmt.indents.existsinmaster");
		      		msg+="\n";
		      		msg+=getString(" js.stores.mgmt.indents.wanttoshowinlist");
		      		msg+="\n";
		      		msg+=getString("js.stores.mgmt.indents.updateitemmaster");
					alert(msg);
		      		document.forms[0].itemname.value = '';
		      		document.forms[0].itemname.focus();
		      		showMessage("js.stores.mgmt.indents.firstreturn");
		      		return false;
		      	}
		      } else {
					if (itemname == jItemNames[i].MEDICINE_NAME && (jItemNames[i].ISSUE_TYPE == 'R' || jItemNames[i].STATUS == 'I')) {
		      		var msg=getString("js.stores.mgmt.indents.item");
		      		msg+=itemname;
		      		msg+=getString("js.stores.mgmt.indents.notissuetouser");
		      		msg+="\n";
		      		msg+=getString("js.stores.mgmt.indents.wesuspectoneofthebelowresons");
		      		msg+="\n";
		      		msg+=getString("js.stores.mgmt.indents.itemisinactiveinmaster");
		      		msg+=" \n";
		      		msg+=getString("js.stores.mgmt.indents.itemissuetypeisretailonly");
		      		alert(msg);
		      		document.forms[0].itemname.value = '';
		      		document.forms[0].itemname.focus();
		      		shoeMessage("js.stores.mgmt.indents.secondreturn");
		      		return false;
		      	}

		      }
		}
	}

	var store_id = document.forms[0].indent_store.value;
	var req_store_id = 0;
	if (undefined != document.forms[0].store_to) {
		req_store_id = document.forms[0].store_to.value;
	}

	if(itemname != '') {

		var medicineDetailsAjaxObj = newXMLHttpRequest();
		var url = 'storesIndent.do?_method=getItemDetails&itemname='+encodeURIComponent(itemname)+'&store_id='+store_id+'&req_store_id='+req_store_id;

		medicineDetailsAjaxObj.open("GET", url, false);
		medicineDetailsAjaxObj.send(null);
		if (medicineDetailsAjaxObj) {
			if (medicineDetailsAjaxObj.readyState == 4) {
				if ( (medicineDetailsAjaxObj.status == 200) && (medicineDetailsAjaxObj.responseText!=null) ) {
				eval("var item = "+medicineDetailsAjaxObj.responseText);
					if (!empty(item)) {
						handleAjaxResponseForItemDetails(item);
					}
				}
			}
		}
	}
}

function handleAjaxResponseForItemDetails(item){
	if(item != null && item != ''){
		var issueType = '';
		if(item.issue_type == 'P') {
			issueType = 'Permanent';
		}else if(item.issue_type == 'C') {
			issueType = 'Consumable';
		}else if(item.issue_type == 'L') {
			issueType = 'Reusable';
		} else issueType = 'Retail Only';
		document.forms[0].pkg_type.value = item.package_type;
		document.forms[0].issue_type_id.value = decodeURIComponent(item.issue_type);
		document.forms[0].avail_qty.value = decodeURIComponent(item.qty_avbl);
		document.forms[0].qty_avbl_for_reqstore.value = decodeURIComponent(item.qty_avbl_for_reqstore);
		document.forms[0].itemid.value = decodeURIComponent(item.medicine_id);
		document.forms[0].identification.value = decodeURIComponent(item.identification);
		document.forms[0].pkg_size.value = item.issue_base_unit;
		document.forms[0].issue_units.value = item.issue_units;
	}
}

function onChangeIndentStore() {

	if (document.getElementById("store_to") && ! (validateToStore(document.getElementById("indent_store").value,document.getElementById("store_to").value) ) )
		return false;
	deleteRows();
	var filterType = getRadioSelection(document.forms[0].filterType);
	if (filterType == 'I') {
		var storeIdObj = document.forms[0].indent_store;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
	}
	if (filterType == 'N') {
		var storeIdObj = document.forms[0].store_to;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
	}
}

function onChangeRequestStore() {
	if ( ! (validateToStore(document.getElementById("indent_store").value,document.getElementById("store_to").value) ) )
		return false;
	deleteRows();
	var filterType = getRadioSelection(document.forms[0].filterType);
	if (filterType == 'N') {
		var storeIdObj = document.forms[0].store_to;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
	}
	if (filterType == 'I') {
		var storeIdObj = document.forms[0].indent_store;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
	}
}

function deleteRows() {
	var tab = document.getElementById("indentItemListTab");
	var rowlen = (tab.rows.length) -2;

	for(var i=0;i<rowlen;i++) {
		tab.deleteRow(-1);
	}
	document.getElementById("dialogId").value = 1;
	var dialogId = document.getElementById("dialogId").value;
	document.getElementById("itemchecklbl"+dialogId).innerHTML = '';
	document.getElementById("itemnamelbl"+dialogId).textContent = '';
	document.getElementById("itemqtylbl"+dialogId).textContent = '';
	if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
		document.getElementById("availqtylbl"+dialogId).textContent = '';
		if (actionId == 'stores_transfer_indent')
			document.getElementById("availreqstoreqtylbl"+dialogId).textContent = '';
	}
	document.getElementById("itemidlbl"+dialogId).value = '';
	document.getElementById("identificationlbl"+dialogId).value = '';
	document.getElementById("issunitslbl"+dialogId).textContent =  '';
	document.getElementById("pkgsizelbl"+dialogId).textContent =  '';
	if (null != document.getElementById("indentnolbl"+dialogId)){
		document.getElementById("indentnolbl"+dialogId).value = '';
	}
	document.getElementById("pkgtypelbl"+dialogId).textContent =  '';
	var addButton = document.getElementById("addBut"+dialogId);
	addButton.setAttribute("class", "imgButton");
	addButton.setAttribute("type", "button");
	addButton.setAttribute("onclick","addIndent('"+dialogId+"'); return false;");
	addButton.setAttribute("title", getString("js.stores.mgmt.addnewitem"));
	addButton.setAttribute("accesskey", "+");
	document.getElementById("itemrow1").src = path+'/icons/Add.png';
}

function init(indentType){
	initDialog();
	filterItems(getRadioSelection(document.forms[0].filterType));
	var indent = document.forms[0].indentNo.value;
	if (indent != '')  {
		if ( user != indentCreater && gRoleId != '1' && gRoleId != '2') {
			document.forms[0].indent_store.disabled = true;
			if (undefined != document.forms[0].store_to){
				document.forms[0].store_to.disabled = true;
			}
		}
	}
	var selectBoxes = document.getElementsByTagName("select");
	if(gDisableForEdit == true || gDisableForEdit== "true") {
		for(var i=0; i<selectBoxes.length; i++) {
	  		var selectBox = selectBoxes[i];
	  		if(selectBox.name!= "status")
	  			selectBox.disabled= true;
  		}

	}
	var testRequestStore = document.getElementById("msgDiv");
	var storeTo = document.forms[0].store_to;
	if (undefined != storeTo) {
		if(empty(storeTo.value)) {
			testRequestStore.style.display = "";
			document.forms[0].save.disabled = true;
		}
	}
	initOrderKitAutoComplete();
	initItemAutoComplete(indentType);
}

function initDialog() {
	dialog = new YAHOO.widget.Dialog("dialog",
			{
				width:"660px",
				context : ["indentItemListTab", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	var escKeyListener = new YAHOO.util.KeyListener("dialog", { keys:27 },
	                                              { fn:closeDialog} );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
	document.getElementById("dialog").style.display = 'block';
	dialog.render();
}

function closeDialog() {
	dialog.hide();
	return false;
}

function addIndent(id){
	var button = document.getElementById("addBut"+id);
	if(indentType == 'S') {
		if(button.title == 'Edit Item') {
			gItemType = 'itemname';
			document.getElementById("orderKitDetails").style.display = "none";
			document.getElementById("orderKitMissedItemsHeader").style.display = "none";
			document.getElementById("orderKitMissedItems").style.display = "none";
			document.getElementById("itemInfo").innerHTML = "";
			document.getElementById("orderkits").value = "";
			document.getElementById("itemName").checked = true;
			document.getElementById("order").checked = false;
			document.getElementById("itemFieldSet").style.display = "block";
			document.getElementById("itemDetails").style.display = "block";
			document.getElementById("order").disabled = true;
			document.getElementById("Add").disabled = false;
		} else {
			gItemType = 'itemname';
			document.getElementById("orderKitDetails").style.display = "none";
			document.getElementById("orderKitMissedItemsHeader").style.display = "none";
			document.getElementById("orderKitMissedItems").style.display = "none";
			document.getElementById("itemInfo").innerHTML = "";
			document.getElementById("orderkits").value = "";
			document.getElementById("itemName").checked = true;
			document.getElementById("order").checked = false;
			document.getElementById("itemFieldSet").style.display = "block";
			document.getElementById("itemDetails").style.display = "block";
			document.getElementById("order").disabled = false;
			document.getElementById("Add").disabled = false;
		}
	} else {
		gItemType = 'itemname';
		document.getElementById("orderKitDetails").style.display = "none";
		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
		document.getElementById("orderKitMissedItems").style.display = "none";
		document.getElementById("itemInfo").innerHTML = "";
		document.getElementById("orderkits").value = "";
		document.getElementById("itemName").checked = true;
		document.getElementById("order").checked = false;
		document.getElementById("itemFieldSet").style.display = "block";
		document.getElementById("itemDetails").style.display = "block";
		document.getElementById("order").disabled = true;
		document.getElementById("Add").disabled = false;
		document.getElementById("orderKitId").style.display = "none";
	}


	if(!validateIndentType()) {
		return false;
	}
	if (document.getElementById("indentnolbl"+id) != null && document.getElementById("indentnolbl"+id).value != '') {
		document.forms[0].itemname.disabled = true;
	}else{
		document.forms[0].itemname.disabled = false;
	}


	document.forms[0].itemname.value = document.getElementById("itemnamelbl"+id).textContent;
	if (oAutoComp._elTextbox.value != '') {
		oAutoComp._bItemSelected = true;
		oAutoComp._sInitInputValue = oAutoComp._elTextbox.value;
	}
	document.forms[0].item_qty.value = document.getElementById("itemqtylbl"+id).textContent;
	document.forms[0].pkg_type.value = document.getElementById("pkgtypelbl"+id).textContent;
	if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
		document.forms[0].avail_qty.value = document.getElementById("availqtylbl"+id).textContent;
		if (actionId == 'stores_transfer_indent')
			document.forms[0].qty_avbl_for_reqstore.value = document.getElementById("availreqstoreqtylbl"+id).textContent;
	}
	document.forms[0].itemid.value = document.getElementById("itemidlbl"+id).value;
	document.forms[0].identification.value = document.getElementById("identificationlbl"+id).value;

	document.forms[0].pkg_size.value = document.getElementById("pkgsizelbl"+id).textContent;
	document.forms[0].issue_units.value = document.getElementById("issunitslbl"+id).textContent;
	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById("dialogId").value = id;
	dialog.show();
	document.forms[0].itemname.disabled ? document.forms[0].item_qty.focus() : document.forms[0].itemname.focus();
}

function makeLabel(name, id, value) {
	var el = document.createElement('label');
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
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

function handleSubmit() {
	if (indValue == 'Add') {
		document.getElementById('Add').disabled = false;
		if(gItemType == 'orderkit') {
			var orderkitName = document.getElementById("orderkits").value;
			if(orderkitName == undefined || orderkitName == '' || orderkitName == null) {
				showMessage("js.stores.mgmt.indents.ordrkitrequired");
				document.getElementById("orderkits").focus();
				return false;
			}
			if(gOrderKitItems != undefined && gOrderKitItems != null && gOrderKitItems != '') {
				if(gOrderKitItems.length > 0) {
					for(var i = 0; i < gOrderKitItems.length; i++ ) {
						addOrderKitToGrid(gOrderKitItems[i]);
					}
				} else {
					showMessage("js.stores.mgmt.indents.noitemorderkit");
					document.getElementById("orderkits").focus();
					return false;
				}
			} else {
				showMessage("js.stores.mgmt.indents.noitemorderkit");
				document.getElementById("orderkits").focus();
				return false;
			}
		} else {
			handleSubmit1();
		}
	} else  {
		if(gItemType == 'orderkit') {
			var orderkitName = document.getElementById("orderkits").value;
			if(orderkitName == undefined || orderkitName == '' || orderkitName == null) {
				showMessage("js.stores.mgmt.indents.ordrkitrequired");
				document.getElementById("orderkits").focus();
				return false;
			}
			if(gOrderKitItems != undefined && gOrderKitItems != null && gOrderKitItems != '') {
				if(gOrderKitItems.length > 0) {
					for(var i = 0; i < gOrderKitItems.length; i++ ) {
						addOrderKitToGrid(gOrderKitItems[i]);
					}
				} else {
					showMessage("js.stores.mgmt.indents.noitemorderkit");
					document.getElementById("orderkits").focus();
					return false;
				}
			} else {
				showMessage("js.stores.mgmt.indents.noitemorderkit");
				document.getElementById("orderkits").focus();
				return false;
			}
		} else {
			handleSubmit1();
		}
	}
}


function handleSubmit1(){

	var dialogId = document.getElementById("dialogId").value;

	var itemname = trim(document.forms[0].itemname.value);
	var itemqty = trim(document.forms[0].item_qty.value);
//	var issuetype = document.forms[0].issue_type.value;
	var pkgtype = document.forms[0].pkg_type.value;
	var availqty = document.forms[0].avail_qty.value;
	var availqtyforReqStore = document.forms[0].qty_avbl_for_reqstore.value;
	var itemid = document.forms[0].itemid.value;
	var identification = document.forms[0].identification.value;
	var issueUnits = document.forms[0].issue_units.value;
	var pkgSize = document.forms[0].pkg_size.value;

	if ( document.getElementById("store_to") && ! (validateToStore(document.getElementById("indent_store").value,document.getElementById("store_to").value) ) )
		return false;

	if(itemname == ''){
		showMessage("js.stores.mgmt.indents.enteritemname");
		document.forms[0].itemname.focus();
		return false;
	}

	if(duplicate(itemname,dialogId)) {
		alert(getString("js.stores.mgmt.indents.duplicateitem")+itemname);
		return false;
	}
	if(itemqty == ''){
		showMessage("js.stores.mgmt.indents.enterquantity");
		document.forms[0].item_qty.focus();
		return false;
	}
	if(itemqty == '0') {
		showMessage("js.stores.mgmt.indents.enterqty");
		document.forms[0].item_qty.focus();
		return false;
	}

	if(allowDecimalsForQty == 'N'){
		if(!isNaN(document.forms[0].item_qty.value)
			&&  Math.round(document.forms[0].item_qty.value) != document.forms[0].item_qty.value) {
			if(!isValidNumber(document.forms[0].item_qty, allowDecimalsForQty))
				return false;
		}
	}else {
		if(!isValidNumber(document.forms[0].item_qty, allowDecimalsForQty))
				return false;
	}

	if(identification == '') {}
	else{
		if(identification != 'S'){
			if(!isFloat(document.forms[0].item_qty.value,'Qty')) {
				document.forms[0].item_qty.focus();
				return false;
			}
		}else {
			if(!isNumber(document.forms[0].item_qty.value,getString('js.stores.mgmt.indents.itemqty'),getString('js.stores.mgmt.indents.qtyisinvalid.serialitem'))) {
				document.forms[0].item_qty.focus();
				return false;
			}
		}
	}
	var imgbutton = makeImageButton('indentCheck','indentCheck'+dialogId,'imgDelete',path+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this,itemrow'+dialogId+','+dialogId+')');

	if(document.getElementById("itemchecklbl"+dialogId).firstChild == null) {
		document.getElementById("itemchecklbl"+dialogId).appendChild(imgbutton);
	}
	var editImgObj = document.getElementById("itemrow"+dialogId);
	document.getElementById(editImgObj.id).src = path+'/icons/Edit.png';

	var editBut = document.getElementById("addBut"+dialogId);
	editBut.setAttribute("accesskey", "");
	editBut.setAttribute("type", "button");
	editBut.setAttribute("title", getString("js.stores.mgmt.edititem"));

	document.getElementById("itemnamelbl"+dialogId).textContent = itemname;
	document.getElementById("itemqtylbl"+dialogId).textContent = itemqty;
//	document.getElementById("issuetypelbl"+dialogId).textContent =  issuetype;
	document.getElementById("pkgtypelbl"+dialogId).textContent =  pkgtype;
	if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
		document.getElementById("availqtylbl"+dialogId).textContent = availqty;
		if (actionId == 'stores_transfer_indent')
			document.getElementById("availreqstoreqtylbl"+dialogId).textContent = availqtyforReqStore;
	}
	document.getElementById("issunitslbl"+dialogId).textContent =  issueUnits;
	document.getElementById("pkgsizelbl"+dialogId).textContent = pkgSize;



	document.getElementById("identificationlbl"+dialogId).value = identification;
	//document.getElementById("indentdelete"+dialogId).value = 'false';
	if(itemid == '') {
		document.getElementById("itemidlbl"+dialogId).value = '0';
	}else {
		document.getElementById("itemidlbl"+dialogId).value = itemid;
	}
	var tab = document.getElementById("indentItemListTab");
	var tabLen = tab.rows.length;
	var row = "row" + tabLen;
	var nextrow =  document.getElementById("row"+(eval(dialogId)+1));
	if(nextrow == null){
		var itemchecklbl = makeLabel('itemchecklbl','itemchecklbl'+tabLen,'');
		var itemnamelbl = makeLabel('itemnamelbl','itemnamelbl'+tabLen,'');
		var itemqtylbl = makeLabel('itemqtylbl','itemqtylbl'+tabLen,'');
	//	var issuetypelbl = makeLabel('issuetypelbl','issuetypelbl'+tabLen,'');
		var pkgtypelbl = makeLabel('pkgtypelbl','pkgtypelbl'+tabLen,'');
		var availqtylbl;
		var availqtyForreqstoreLbl;
		if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
			availqtylbl = makeLabel('availqtylbl','availqtylbl'+tabLen,'');
			if (actionId == 'stores_transfer_indent')
				availqtyForreqstoreLbl = makeLabel('availreqstoreqtylbl','availreqstoreqtylbl'+tabLen,'');
		}
		var itemidlbl = makeHidden('itemidlbl','itemidlbl'+tabLen,'');
		var identificationlbl = makeHidden('identificationlbl','identificationlbl'+tabLen,'');
		var issueunitsbl = makeLabel('issunitslbl','issunitslbl'+tabLen,'');
		var pkgsizelbl = makeLabel('pkgsizelbl','pkgsizelbl'+tabLen,'');
		var indentDelete = makeHidden('indentdelete','indentdelete'+tabLen,'false');

		var buton = makeButton1("addBut", "addBut"+tabLen);
		buton.setAttribute("class", "imgButton");
		buton.setAttribute("type", "button");
		buton.setAttribute("onclick","addIndent('"+tabLen+"'); return false;");
		buton.setAttribute("title", getString("js.stores.mgmt.addnewitem"));
		buton.setAttribute("accesskey", "+");

		var itemrowbtn = makeImageButton('itemrow','itemrow'+tabLen,'button',path+'/icons/Add.png');
		buton.appendChild(itemrowbtn);

		trObj = tab.insertRow(tabLen);
		trObj.id = row;

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemnamelbl);
		tdObj.appendChild(itemidlbl);
		tdObj.appendChild(identificationlbl);
		tdObj.appendChild(indentDelete);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemqtylbl);

		if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
			tdObj = trObj.insertCell(-1);
			tdObj.appendChild(availqtylbl);
			if (actionId == 'stores_transfer_indent') {
				tdObj = trObj.insertCell(-1);
				tdObj.appendChild(availqtyForreqstoreLbl);
			}
		}

//		tdObj = trObj.insertCell(-1);
//		tdObj.appendChild(issuetypelbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pkgtypelbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pkgsizelbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(issueunitsbl);



		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemchecklbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(buton);

	}
	if(document.getElementById('indentCheck'+(eval(dialogId)+1))
		&& document.getElementById('indentCheck'+(eval(dialogId)+1)).className =='imgDelete'){
		dialog.hide();
		return;
	}
	addIndent(eval(dialogId)+1);
	//dialog.hide();
}

function handleCancel() {
	closeDialog();
	document.forms[0].save.focus();
}

function duplicate(itemname,id) {
	var tab = document.getElementById("indentItemListTab");
	for(var j=1;j<tab.rows.length;j++) {
		var name = trim(document.getElementById("itemnamelbl"+j).textContent);
		if((id != j) && (name == itemname)) {
			return true;
		}
	}
	return false;
}

function cancelRow(imgobj,btn,row){
	if (document.getElementById('indentdelete'+row).value == 'false') {
		//YAHOO.util.Dom.get(row).className = "deleted";
		document.getElementById(imgobj.id).src = path+"/icons/Deleted.png";
		document.getElementById(btn.id).src = path+"/icons/Edit1.png";
		document.getElementById('addBut'+row).disabled = true;
		document.getElementById('indentdelete'+row).value = 'true';
	} else {
		//YAHOO.util.Dom.get(row).className = "newRow";
		document.getElementById(imgobj.id).src = path+"/icons/Delete.png";
		document.getElementById(btn.id).src = path+"/icons/Edit.png";
		document.getElementById('addBut'+row).disabled = false;
		document.getElementById('indentdelete'+row).value = 'false';
	}
}


function showHideDeptWard(value) {
	if (indentType == 'S')
		return;

	if(value == 'D') document.forms[0].dept_ward[0].checked = true;
	if(value == 'W') document.forms[0].dept_ward[1].checked = true;

	if(value == 'D'){
		//document.forms[0].ward.selectedIndex = 0;
		document.forms[0].dept.disabled = false;
		document.forms[0].ward.disabled = true;
	}else{
		//document.forms[0].dept.selectedIndex = 0;
		document.forms[0].dept.disabled = true;
		document.forms[0].ward.disabled = false;
	}
	if(gDisableForEdit == true || gDisableForEdit=="true") {
		document.forms[0].dept.disabled = true;
		document.forms[0].ward.disabled = true;
	}
}

function validateIndentType() {
	var valadd = indentType;
	// User Issue
	if(valadd != null) {
		if(valadd == 'U') {
			if(document.forms[0].dept_ward[0].checked) {
				if(document.forms[0].dept.value == '') {
					showMessage("js.stores.mgmt.indents.selectdepartment");
					document.forms[0].dept.focus();
					return false;
				}
			}
			else if(document.forms[0].dept_ward[1].checked) {
				if(document.forms[0].ward.value == '') {
					showMessage("js.stores.mgmt.indents.selectward");
					document.forms[0].ward.focus();
					return false;
				}
			}
		}
	}
	return true;
}

function removeDisableOnSelectBoxes(){
	var selectBoxes = document.getElementsByTagName("select");
  	for(var i=0; i<selectBoxes.length; i++) {
  		var selectBox = selectBoxes[i];
  		selectBox.disabled= false;
  	}
}

function validateFields() {

	if(!validateIndentType()) {
		return false;
	}
	if (indentType == 'S') {
		if (document.forms[0].store_to.value == document.forms[0].indent_store.value) {
			showMessage("js.stores.mgmt.indents.bothstores.notbesame");
			return false;
		}
	}
	if (!doValidateDateField(document.forms[0].expected_date))
		return false;

	if (document.forms[0].expected_date.value == "")  {
		showMessage("js.stores.mgmt.indents.expecteddate.required");
		document.forms[0].expected_date.focus();
		return false;
	}
	if (!(validateTime(document.forms[0].expected_time))) return false;

	if (document.forms[0].expected_date.value != '') {
		msg = validateDateStr(document.forms[0].expected_date.value,'future');
		if ( msg != null) {
			alert(msg);
			document.forms[0].expected_date.focus();
	        return false;
        }
	}
	if(!checkEmptyRows()) {
		showMessage("js.stores.mgmt.indents.additems.save");
		return false;
	}
	addToInnerHTML();

	var indent = document.forms[0].indentNo.value;
	if (indent != '')  {
		document.forms[0].indent_store.disabled = false;
		if (undefined != document.forms[0].store_to){
			document.forms[0].store_to.disabled = false;
		}
	}
	removeDisableOnSelectBoxes();
	document.forms[0].save.disabled = true;
	document.forms[0].submit();
}

function checkEmptyRows() {
	var tab = document.getElementById("indentItemListTab");
	var tabLen = tab.rows.length;
	for(var i=1;i<tabLen;i++) {
	    if (document.getElementById('indentdelete'+i).value == 'true') continue;
		var item = document.getElementById("itemnamelbl"+i).textContent;
		if(item != "") return true;
	}
	return false;
}

function addToInnerHTML() {
	var tab = document.getElementById("indentItemListTab");
	var tabLen = tab.rows.length;
	for(var i=1;i<tabLen;i++) {
		var del = document.getElementById("indentdelete"+i);

		//if(del.value == 'false') {
			var itemid = document.getElementById("itemidlbl"+i).value;
			var itemname = document.getElementById("itemnamelbl"+i).textContent;
			var itemqty = document.getElementById("itemqtylbl"+i).textContent;
			var indentno = "";
			if(document.getElementById("indentnolbl"+i) != null) {
				indentno = document.getElementById("indentnolbl"+i).value;
			}
			var innerIndentTabObj = document.getElementById("hiddenIndentItemListTab");
			var trObj = "", tdObj = "";
			trObj = innerIndentTabObj.insertRow(-1);

			tdObj = trObj.insertCell(-1);
			var el = makeHidden('indentdel', 'indentdel'+i, del.value);
			tdObj.appendChild(el);

			tdObj = trObj.insertCell(-1);
			el = makeHidden('medicine_id', 'medicine_id', itemid);
			tdObj.appendChild(el);

			tdObj = trObj.insertCell(-1);
			el = makeHidden('medicine_name', 'medicine_name', itemname);
			tdObj.appendChild(el);

			tdObj = trObj.insertCell(-1);
			el = makeHidden('qty', 'qty', itemqty);
			tdObj.appendChild(el);

			tdObj = trObj.insertCell(-1);
			el = makeHidden('indent_no', 'indent_no', indentno);
			tdObj.appendChild(el);
		//}
	}
}

function isEventEnterEscAuto(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 ) {
		document.forms[0].item_qty.focus();
		return true;
	}else if ( charCode==27 ) {
		dialog.cancel();
		document.forms[0].save.focus();
		return true;
	}else return true;
}

function isEventEnterEsc(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 ) {
		handleSubmit();
		return false;
	}else if ( charCode==9 ){
		if(e.shiftKey==true){
			var inpt = document.getElementsByTagName("INPUT");
			for(var i=0;i<inpt.length;i++) {
				if(inpt[i].name == "itemname") {
					inpt[i].focus();
					break;
					return true;
				}
			}
		}else{
			var btn = document.getElementsByTagName("BUTTON");
			for(var i=0;i<btn.length;i++) {
				if(btn[i].textContent == "Add") {
					btn[i].focus();
					break;
					return true;
				}
			}
		}
	}else if ( charCode==27 ) {
		dialog.cancel();
		document.forms[0].save.focus();
		return true;
	}else {
		return enterNumOnly(e);
	}
	return false;
}

function checkstoreallocation() {
	var storeAccess = true;
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 		storeAccess = false;
 		}
 	}
 	var editIndent = document.forms[0].indentNo.value != '';
 	if ( storeAccess && editIndent)
		checkEditRights();
}

function checkEditRights() {
	if ( user != indentCreater && gRoleId != '1' && gRoleId != '2') {
		showMessage("js.stores.mgmt.indents.unauthorized.editthisindent");
		document.getElementById("storecheck").style.display = 'none';
	}
}

function filterItems(indType) {
	if (indType == 'I') {
		var storeIdObj = document.forms[0].indent_store;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
	} else if (indType == 'N') {
		var storeIdObj = document.forms[0].store_to;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
	} else {
		initItemAutoComplete(indentType);
	}
}


function validateToStore(fromStore,toStore){
	if(fromStore == toStore){
		showMessage("js.stores.mgmt.indents.indentandrequestingstores.different");
		return false;
	}
	return true;
}

function loadItems(){
	var filterType = getRadioSelection(document.forms[0].filterType);
	if (filterType == 'I') {
		var storeIdObj = document.forms[0].indent_store;
		getMedicinesForStore(storeIdObj, initItemAutoComplete);
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
	    		document.getElementById("Add").disabled = false;
	    		document.getElementById("itemFieldSet").style.display = "block";
	    		document.getElementById("itemDetails").style.display = "block";
	    		document.getElementById("orderKitDetails").style.display = "none";
	    		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
	    		document.getElementById("orderKitMissedItems").style.display = "none";
	    		document.getElementById("orderkits").value = "";
	    		document.getElementById("itemname").focus();
	    		itemInfo.innerHTML = "";
	    		gItemType = 'itemname';
	    	} else {
	    		document.getElementById("Add").disabled = false;
	    		document.getElementById("itemDetails").style.display = "none";
	    		document.getElementById("orderKitDetails").style.display = "block";
	    		document.getElementById("itemFieldSet").style.display = "none";
	    		itemInfo.innerHTML = "";
	    		document.getElementById("orderkits").value = "";
	    		document.getElementById("orderkits").focus();
	    		clearDialogDetails();
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
	var store_id = document.forms[0].indent_store.value;
	var req_store_id = 0;
	if (undefined != document.forms[0].store_to) {
		req_store_id = document.forms[0].store_to.value;
	}
	var storeType = document.forms[0].filterType.value;

	var url = 'storesIndent.do?_method=getOrderKitItemsJSON&order_kit_id='+orderKitId+"&indentStoreId="+store_id+"&reqStoreId="+req_store_id;
	ajaxReqObject.open("POST",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			orderKitItems = JSON.parse(ajaxReqObject.responseText);
			gOrderKitItems = orderKitItems.order_kit_items;

		}
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

function addOrderKitToGrid(orderKitItem){

	var dialogId = document.getElementById("dialogId").value;

	var itemname = orderKitItem.medicine_name;
	var itemqty = orderKitItem.qty_needed;
	var pkgtype = orderKitItem.package_type;
	var availqty = orderKitItem.indent_store_qty;
	var availqtyforReqStore = orderKitItem.req_store_qty;
	var itemid = orderKitItem.medicine_id;
	var identification = orderKitItem.identification;
	var issueUnits = orderKitItem.issue_units;
	var pkgSize = orderKitItem.issue_base_unit;

	if ( document.getElementById("store_to") && ! (validateToStore(document.getElementById("indent_store").value,document.getElementById("store_to").value) ) )
		return false;

	if(duplicate(itemname,dialogId)) {
		alert(getString("js.stores.mgmt.indents.duplicateitem")+itemname);
		return false;
	}

	var imgbutton = makeImageButton('indentCheck','indentCheck'+dialogId,'imgDelete',path+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this,itemrow'+dialogId+','+dialogId+')');

	if(document.getElementById("itemchecklbl"+dialogId).firstChild == null) {
		document.getElementById("itemchecklbl"+dialogId).appendChild(imgbutton);
	}
	var editImgObj = document.getElementById("itemrow"+dialogId);
	document.getElementById(editImgObj.id).src = path+'/icons/Edit.png';

	var editBut = document.getElementById("addBut"+dialogId);
	editBut.setAttribute("accesskey", "");
	editBut.setAttribute("type", "button");
	editBut.setAttribute("title", getString("js.stores.mgmt.edititem"));

	document.getElementById("itemnamelbl"+dialogId).textContent = itemname;
	document.getElementById("itemqtylbl"+dialogId).textContent = itemqty;
	document.getElementById("pkgtypelbl"+dialogId).textContent =  pkgtype;
	if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
		document.getElementById("availqtylbl"+dialogId).textContent = availqty;
		if (actionId == 'stores_transfer_indent')
			document.getElementById("availreqstoreqtylbl"+dialogId).textContent = availqtyforReqStore;
	}
	document.getElementById("issunitslbl"+dialogId).textContent =  issueUnits;
	document.getElementById("pkgsizelbl"+dialogId).textContent = pkgSize;

	document.getElementById("identificationlbl"+dialogId).value = identification;
	if(itemid == '') {
		document.getElementById("itemidlbl"+dialogId).value = '0';
	}else {
		document.getElementById("itemidlbl"+dialogId).value = itemid;
	}
	var tab = document.getElementById("indentItemListTab");
	var tabLen = tab.rows.length;
	var row = "row" + tabLen;
	var nextrow =  document.getElementById("row"+(eval(dialogId)+1));
	if(nextrow == null){
		var itemchecklbl = makeLabel('itemchecklbl','itemchecklbl'+tabLen,'');
		var itemnamelbl = makeLabel('itemnamelbl','itemnamelbl'+tabLen,'');
		var itemqtylbl = makeLabel('itemqtylbl','itemqtylbl'+tabLen,'');
		var pkgtypelbl = makeLabel('pkgtypelbl','pkgtypelbl'+tabLen,'');
		var availqtylbl;
		var availqtyForreqstoreLbl;
		if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
			availqtylbl = makeLabel('availqtylbl','availqtylbl'+tabLen,'');
			if (actionId == 'stores_transfer_indent')
				availqtyForreqstoreLbl = makeLabel('availreqstoreqtylbl','availreqstoreqtylbl'+tabLen,'');
		}
		var itemidlbl = makeHidden('itemidlbl','itemidlbl'+tabLen,'');
		var identificationlbl = makeHidden('identificationlbl','identificationlbl'+tabLen,'');
		var issueunitsbl = makeLabel('issunitslbl','issunitslbl'+tabLen,'');
		var pkgsizelbl = makeLabel('pkgsizelbl','pkgsizelbl'+tabLen,'');
		var indentDelete = makeHidden('indentdelete','indentdelete'+tabLen,'false');

		var buton = makeButton1("addBut", "addBut"+tabLen);
		buton.setAttribute("class", "imgButton");
		buton.setAttribute("type", "button");
		buton.setAttribute("onclick","addIndent('"+tabLen+"'); return false;");
		buton.setAttribute("title", getString("js.stores.mgmt.addnewitem"));
		buton.setAttribute("accesskey", "+");

		var itemrowbtn = makeImageButton('itemrow','itemrow'+tabLen,'button',path+'/icons/Add.png');
		buton.appendChild(itemrowbtn);

		trObj = tab.insertRow(tabLen);
		trObj.id = row;

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemnamelbl);
		tdObj.appendChild(itemidlbl);
		tdObj.appendChild(identificationlbl);
		tdObj.appendChild(indentDelete);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemqtylbl);

		if ( showAvblQty == 'A' || gRoleId == '1' || gRoleId == '2') {
			tdObj = trObj.insertCell(-1);
			tdObj.appendChild(availqtylbl);
			if (actionId == 'stores_transfer_indent') {
				tdObj = trObj.insertCell(-1);
				tdObj.appendChild(availqtyForreqstoreLbl);
			}
		}

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pkgtypelbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pkgsizelbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(issueunitsbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(itemchecklbl);

		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(buton);

	}
	if(document.getElementById('indentCheck'+(eval(dialogId)+1))
		&& document.getElementById('indentCheck'+(eval(dialogId)+1)).className =='imgDelete'){
		dialog.hide();
		return;
	}
	addIndent(eval(dialogId)+1);
}