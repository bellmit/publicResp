var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/StoresItemMaster.do?_method=show',
		onclick: null,
		description: "View/Edit Item Details"
	},
	EditRates: {
		title : "Edit Selling Price",
		imageSrc : "icons/Edit.png",
		href : "/pages/master/StoresMaster/StoreItemRates.do?_method=show",
		description : "View and/or Edit the rates of Item"
	}

};

var theForm = document.itemListSearchForm;

function init() {
	theForm = document.itemListSearchForm;
	createToolbar(toolbar);
	autoItem();
	automanf();
	theForm.medicine_name.focus();
	var codeType =document.codeTypeForm.code_type.value;
	document.exportForm.code_type.value = codeType;
	document.importForm.code_type.value = codeType;
}
var oAutoItem;
function autoItem(){
	var itemNames = [];
    var j = 0;
		
		var dataSource = new YAHOO.widget.DS_JSArray(itemDetailsList);
		dataSource.responseSchema = {
			resultsList: "result", 
			fields: [{key: "CUST_ITEM_CODE_WITH_NAME"},{key: "MEDICINE_NAME"}, {key: "MEDICINE_ID"}]
		};

		oAutoItem = new YAHOO.widget.AutoComplete("item","itemcontainer",dataSource);
		oAutoItem.minQueryLength = 0;
		oAutoItem.typeAhead = false;
		oAutoItem.prehighlightClassname = "yui-ac-prehighlight";
		oAutoItem.autoHighlight = true;
		oAutoItem.useShadow = false;
		oAutoItem.forceSelection = false;
		oAutoItem.maxResultsDisplayed = 20;
		oAutoItem.allowBroserAutocomplete = false;

		oAutoItem.filterResults = Insta.queryMatchWordStartsWith;
		oAutoItem.formatResult = Insta.autoHighlightWordBeginnings;
		oAutoItem.itemSelectEvent.subscribe(onSelectItem);
}

function onSelectItem(type,args) {
	var item = findInList(itemDetailsList, 'MEDICINE_NAME', args[2][1]);
	if(item != null){
		itemListSearchForm.medicine_name.value = item.MEDICINE_NAME;
	}
}

var oAutoManf;
function automanf(){
	YAHOO.example.ACJSArray = new function(){
		datasource = new YAHOO.widget.DS_JSArray(manfList);

		oAutoManf = new YAHOO.widget.AutoComplete("manf_name","manfcontainer",datasource);
		oAutoManf.minQueryLength = 0;
		oAutoManf.typeAhead = false;
		oAutoManf.prehighlightClassname = "yui-ac-prehighlight";
		oAutoManf.autoHighlight = true;
		oAutoManf.useShadow = false;
		oAutoManf.forceSelection = false;
		oAutoManf.maxResultsDisplayed = 10;
		oAutoManf.allowBroserAutocomplete = false;
		oAutoManf.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get("manf_name").value;
			if (sInputValue.length === 0){
				var oSelf = this;
				setTimeout(oSelf.sendQuery(sInputValue),0);
			}
		});
	}
}


function changeCodeType(obj) {
	document.exportForm.code_type.value = obj.value;
	document.importForm.code_type.value = obj.value;
}

/*
	All below methods are copied from inventoryitem.js file
*/

function doClose() {
	document.storesitemmasterform.method.value = 'list';
	document.storesitemmasterform.submit();
	return true;
}
function focus(){
  document.storesitemmasterform.medicine_name.focus();
}

  function Add() {
	if (validate() == true) {
		if(action == 'add') {
			var itemListTable = document.getElementById("medtabel");
		    var numRows = itemListTable.rows.length-1;
		    var id = numRows;
			var row = itemListTable.insertRow(id);
	
		    var minlevel = document.storesitemmasterform.min_level.value;
		    var maxlevel = document.storesitemmasterform.max_level.value;
		    var dangerlevel = document.storesitemmasterform.danger_level.value;
		    var reorderlevel = document.storesitemmasterform.reorder_level.value;
		    var deptid = document.storesitemmasterform.store_id.value;
		    var bin = document.storesitemmasterform.store_bin.value.trim();
		    var deptname = document.storesitemmasterform.store_id.options[document.storesitemmasterform.store_id.selectedIndex].text;
		    var itemId='';
		    if (document.getElementById("item_id") != null){
		    	itemId = document.getElementById("item_id").value ;
		    }
	
			var cell;
	
		    cell = row.insertCell(-1);
		 	cell.innerHTML = '<label id="depLabel'+id+'">'+deptname+'</label>' +
			        '<input type="hidden" name="hdepartment" id="hdepartment'+id+'" value="'+deptid+'">'+
			        '<input type="hidden" name="hdepartmentname" id="hdepartmentname'+id+'" value="'+deptname+'">'+
			        '<input type="hidden" name="deptoldrnew" id="deptoldrnew'+id+'" value="new">'+
					'<input type="hidden" name="hmedicineId" id="hmedicineId'+id+'" value="'+itemId+'">';
	
		 	cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
			cell.innerHTML = '<input type="text" name="hiddenBin" id="hiddenBin'+id+'" value="'+bin+'"  maxlength="50" readonly>';
		 	
		 	cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
			cell.innerHTML = '<input type="text" class="num"   name="hiddenDangerLevel" id="hiddenDangerLevel'+id+'" value="'+dangerlevel+'"  maxlength="8" readonly>';
	
		    cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
			cell.innerHTML = '<input type="text" class="num"   name="hiddenMinLevel" id="hiddenMinLevel'+id+'" value="'+minlevel+'"  maxlength="8" readonly>';
	
	  		cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
			cell.innerHTML = '<input type="text" class="num"   name="hiddenReorderLevel" id="hiddenReorderLevel'+id+'" value="'+reorderlevel+'"  maxlength="8" readonly>';
	
		    cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
			cell.innerHTML = '<input type="text" class="num"   name="hiddenMaxLevel" id="hiddenMaxLevel'+id+'" value="'+maxlevel+'"  maxlength="8" readonly>';
			
	
			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "delItem" id="delItem'+id+'" ' + 'onclick="deleteItem(this, '+id+')" src="'+cpath+'/icons/Delete.png">' +
			              '<input type="hidden" name="hdeleted" id="hdeleted'+id+'"  value="false">';
	
			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "editBut" id="editBut'+id+'" ' + 'onclick="editItemGroupDialog('+id+')" src="'+cpath+'/icons/Edit.png">';
	
		    document.storesitemmasterform.min_level.value = '';
		    document.storesitemmasterform.max_level.value = '';
		    document.storesitemmasterform.danger_level.value = '';
		    document.storesitemmasterform.reorder_level.value = '';
		    document.storesitemmasterform.store_bin.value = '';
		    document.storesitemmasterform.store_id.options.selectedIndex = 0;
		}
	    if(action == 'edit') {
	    	document.getElementById('hiddenMinLevel'+editedrowId).value = document.storesitemmasterform.min_level.value;
	    	document.getElementById('hiddenMaxLevel'+editedrowId).value =  document.storesitemmasterform.max_level.value;
	    	document.getElementById('hiddenDangerLevel'+editedrowId).value =  document.storesitemmasterform.danger_level.value;
	    	document.getElementById('hiddenReorderLevel'+editedrowId).value =  document.storesitemmasterform.reorder_level.value;
	    	document.getElementById('hdepartment'+editedrowId).value = document.storesitemmasterform.store_id.value;
	    	document.getElementById('hiddenBin'+editedrowId).value = document.storesitemmasterform.store_bin.value.trim();
	    	document.getElementById('hdepartmentname'+editedrowId).value = document.storesitemmasterform.store_id.options[document.storesitemmasterform.store_id.selectedIndex].text;
			document.getElementById('depLabel'+editedrowId).textContent = document.storesitemmasterform.store_id.options[document.storesitemmasterform.store_id.selectedIndex].text;
	
	    	document.storesitemmasterform.min_level.value = '';
		    document.storesitemmasterform.max_level.value = '';
		    document.storesitemmasterform.danger_level.value = '';
		    document.storesitemmasterform.reorder_level.value = '';
		    document.storesitemmasterform.store_bin.value = '';
		    document.storesitemmasterform.store_id.options.selectedIndex = 0;
	    }
    }else {
    	return false;
    }

}

function validate(){
	if(document.storesitemmasterform.store_id.options.selectedIndex==0){
	   alert("Select the Store");
	   document.storesitemmasterform.store_id.focus();
	   return false;
	}
	if(empty(document.storesitemmasterform.store_bin.value.trim()) && 
			empty(document.storesitemmasterform.danger_level.value) && 
			empty(document.storesitemmasterform.min_level.value) && 
			empty(document.storesitemmasterform.reorder_level.value) && 
			empty(document.storesitemmasterform.max_level.value) ){
		alert("Either reorder levels or rack/bin is required");
		document.storesitemmasterform.store_bin.focus();
		return false;
	} else if(!empty(document.storesitemmasterform.danger_level.value) || !empty(document.storesitemmasterform.min_level.value) ||
				!empty(document.storesitemmasterform.reorder_level.value) || !empty(document.storesitemmasterform.max_level.value)){
		if(empty(document.storesitemmasterform.danger_level.value)) {
		   alert("Danger level is required");
		   document.storesitemmasterform.danger_level.focus();
		   return false;
		}
		if(empty(document.storesitemmasterform.min_level.value)){
		   alert("Min level is required");
		   document.storesitemmasterform.min_level.focus();
		   return false;
		 }
		 if(empty(document.storesitemmasterform.reorder_level.value)){
		   alert("Reorder level is required");
		   document.storesitemmasterform.reorder_level.focus();
		   return false;
		 }
		 if(empty(document.storesitemmasterform.max_level.value)){
		   alert("Max level is required");
		   document.storesitemmasterform.max_level.focus();
		   return false;
		 }
		 if(parseInt(document.storesitemmasterform.reorder_level.value,10)>parseInt(document.storesitemmasterform.max_level.value,10)){
		   alert("Reorder level should be less than maxLevel");
		   document.storesitemmasterform.reorder_level.focus();
		   return false;
		 }
		 if(parseFloat(document.storesitemmasterform.min_level.value,10) > parseFloat(document.storesitemmasterform.reorder_level.value,10)){
			 alert("Min level should be less than Reorder level");
			 document.storesitemmasterform.min_level.select();
			 document.storesitemmasterform.min_level.focus();
		     return false;
		 }
		 if(parseFloat(document.storesitemmasterform.danger_level.value,10) > parseFloat(document.storesitemmasterform.reorder_level.value,10)){
			 alert("Danger level should be less than Reorder level");
			 document.storesitemmasterform.danger_level.focus();
		     return false;
		 }
	     if(parseInt(document.storesitemmasterform.danger_level.value,10)>parseInt(document.storesitemmasterform.min_level.value,10)){
		   alert("Danger level should be less than Min level");
		   document.storesitemmasterform.danger_level.focus();
		   return false;
		 }
	}
	
	var itemListTable = document.getElementById("medtabel");
    var numRows = itemListTable.rows.length-1;
    id = numRows;
   	if (numRows > 1) {
		for (var i=1;i<=numRows-1;i++) {
				if(action == 'edit') {
				if (document.storesitemmasterform.store_id.value == (document.getElementById('hdepartment'+i).value) && editedrowId != i) {
					alert(document.getElementById('hdepartmentname'+i).value+" already exists, update if required");
					return false;
					}
				}else
				if(document.storesitemmasterform.store_id.value == (document.getElementById('hdepartment'+i).value)) {
					alert(document.getElementById('hdepartmentname'+i).value+" already exists, update if required");
					return false;
				}
		}
	}
	return true;
}

function deleteItem(checkBox, rowId) {
	var itemListTable = document.getElementById("medtabel");
	var row = itemListTable.rows[rowId];
	var img = document.createElement("img");
	img.setAttribute("name", "editBut");
	img.setAttribute("id", "editBut"+rowId);
	img.setAttribute("style", "cursor:pointer;");
	img.setAttribute("src", cpath + "/icons/Edit1.png");
	img.setAttribute("class", "button");

	var deletedInput = document.getElementById('hdeleted'+rowId);
	if (deletedInput.value == 'false') {
		document.getElementById('delItem'+rowId).src = cpath+"/icons/Deleted.png";
		document.getElementById('editBut'+rowId).src = cpath+"/icons/Edit1.png";
		for (var i=row.cells[7].childNodes.length-1; i>=0; i--) {
			row.cells[7].removeChild(row.cells[7].childNodes[i]);
		}
		row.cells[7].appendChild(img);
		deletedInput.value = 'true';
		row.className = "deleted";
	} else {
		deletedInput.value = 'false';
		document.getElementById('delItem'+rowId).src = cpath+"/icons/Delete.png";
		img.setAttribute("onclick", "editItemGroupDialog('"+rowId+"')");
		for (var i=row.cells[7].childNodes.length-1; i>=0; i--) {
			row.cells[7].removeChild(row.cells[7].childNodes[i]);
		}
		row.cells[7].appendChild(img);
		document.getElementById('editBut'+rowId).src = cpath+"/icons/Edit.png";
		row.className = "";
	}
}

function setPackSize(){
	for ( i=0; i<identList.length; i++){
		var item = identList[i];

		if (document.storesitemmasterform.med_category_id.value == item["CATEGORY_ID"]){
			 if (item["IDENTIFICATION"] == 'S'){
			 	document.storesitemmasterform.issue_units.value = 'Numbers';
				var selectBox = document.getElementById('package_uom');
				if (selectBox != null) {
					selectBox.length = 2;
					selectBox.options[0].text = '-- Select --';
					selectBox.options[0].value = '';
					selectBox.options[1].text = 'Numbers';
					selectBox.options[1].value = 'Numbers';
					selectBox.value = 'Numbers';
					document.getElementById('package_uom_hidden').value = 'Numbers';
				}
				document.getElementById('issue_units_hidden').value = 'Numbers';
				document.getElementById('issue_base_unit_label').textContent = 1;
				document.getElementById('issue_base_unit').value = 1;
				document.storesitemmasterform.issue_units.disabled = true;
				document.storesitemmasterform.package_uom.disabled = true;
			}
	}
	}
}

function putAsterick(){
        ajaxCategoryDetails = getCategoryDetailsAjax();
    	isDrug = ajaxCategoryDetails.isDrug;
    	var status = isDrug === 'Y' ? "visible" : "hidden";
    	document.getElementById("itemFormDrug").style.visibility = status;
    	document.getElementById("routAdminDrug").style.visibility = status;
    	document.getElementById("genericNameDrug").style.visibility = status;
    	document.getElementById("consUomDrug").style.visibility = status;
}

var oManfAuto = null;
function addShowautomanf(){

	var manfCode = document.storesitemmasterform.manf_name.value;
	for (i=0; i<manfList.length; i++){
		var item = manfList[i];
		if (manfCode == item["manf_name"]){
			document.storesitemmasterform.manf_name.value = item["manf_name"];
		}
	}

	var manfArray = [];
	manfArray.length = 0;
	var j=0;
	for ( i=0; i<manfList.length; i++){

		if (manfList[i].status == 'A' ) {
			var item = manfList[i];
			manfArray[j++] = item;
			manfArray.length = manfArray.length +1;
		}
	}
	if (oManfAuto != null) {
		oManfAuto.destroy();
		oManfAuto = null;
	}
	datasource = new YAHOO.util.LocalDataSource({result : manfArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'manf_name'}	]
		};
	oManfAuto = new YAHOO.widget.AutoComplete('manf_name','manfcontainer',datasource);
	oManfAuto.prehighlightClassname = "yui-ac-prehighlight";
	oManfAuto.minQueryLength = 0;
	oManfAuto.maxResultsDisplayed = 5;
	oManfAuto.typeAhead = false;
	oManfAuto.useShadow = false;
	oManfAuto.allowBrowserAutocomplete = false;
	oManfAuto.autoHighlight = false;
	oManfAuto.forceSelection = false;
	oManfAuto.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get("manf_name").value;
		if (sInputValue.length === 0){
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		}
	});
	oManfAuto.textboxBlurEvent.subscribe(setManfCode);
	oManfAuto.unmatchedItemSelectEvent.subscribe(checkForInactiveManf);

}

function setManfCode(){

	var manfName = document.storesitemmasterform.manf_name.value;
	document.storesitemmasterform.manf_code.value = '';
	for (i=0; i<manfList.length; i++){
		var item = manfList[i];
		if (manfName == item["manf_name"]){
			document.storesitemmasterform.manf_code.value = item["manf_code"];
		}
	}
}

function checkForInactiveManf() {
	var	manfName = document.storesitemmasterform.manf_name.value;
	for (var i=0; i<manfList.length; i++ ) {
	     if ( (manfName == manfList[i].manf_name) && ( manfList[i].status != 'A' ) ) {
	     	alert("Manufacturer: '"+manfName+"' exists in master \n if u want to show in list\n pls update Manufacturer Master");
	     	document.storesitemmasterform.manf_name.value = '';
	     	document.storesitemmasterform.manf_code.value = '';
	     	break;
	     }
	}
}


function validateUpdateMRP (issue_base_unit) {

	if ( ! validateAllFields())
		return false;
	/*if (opeartion == 'show') {
	     if (document.storesitemmasterform.issue_base_unit.value != document.storesitemmasterform.originalPkgSize.value) {
		  	document.storesitemmasterform.changedPkgSize.value = 'yes';
		  	if(confirm("do you want update the MRP, Cost Price and Selling price with the same ratio")){
		    	document.storesitemmasterform.updateMrpCp.value = 'yes';
		    }
		    else document.storesitemmasterform.updateMrpCp.value = 'no';
		 }
	 }*/
	return true;
}
function makeingDec(objValue,obj){
	if (objValue == '') objValue = 0;
    if (isInteger(objValue)) {
		document.getElementById(obj.name).value = parseInt(objValue);
	} else document.getElementById(obj.name).value = 0;
}
function initItemGroupDialog(){
	itemDialog = new YAHOO.widget.Dialog("itemDialog",
			{
			width:"700px",
			context :["btnAddItems", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
			});

			var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:itemDialog,
	                                                correctScope:true } );
			itemDialog.cfg.queueProperty("keylisteners", escKeyListener);

	itemDialog.render();
}

function handleCancel(){
	document.storesitemmasterform.min_level.value = '';
    document.storesitemmasterform.max_level.value = '';
    document.storesitemmasterform.danger_level.value = '';
    document.storesitemmasterform.reorder_level.value = '';
    document.storesitemmasterform.store_bin.value = '';
    document.storesitemmasterform.store_id.options.selectedIndex = 0;
	itemDialog.cancel();
}

var action = '';
var editedrowId = '';
function getItemGroupDialog(id){
	button = document.getElementById("btnAddItems"+id);
	itemDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	action = 'add';
	itemDialog.show();
}

function editItemGroupDialog(id){
	button = document.getElementById("editBut"+id);
	itemDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	action = 'edit';
	editedrowId = id;
	itemDialog.show();
	document.storesitemmasterform.min_level.value = document.getElementById('hiddenMinLevel'+id).value;
	document.storesitemmasterform.max_level.value = document.getElementById('hiddenMaxLevel'+id).value;
	document.storesitemmasterform.danger_level.value = document.getElementById('hiddenDangerLevel'+id).value;
	document.storesitemmasterform.reorder_level.value = document.getElementById('hiddenReorderLevel'+id).value;
	document.storesitemmasterform.store_id.value = document.getElementById('hdepartment'+id).value;
	document.storesitemmasterform.store_bin.value = document.getElementById('hiddenBin'+id).value;

}

var issueUOMAutoComp = null;
var packUOMAutoComp = null;
function addShowinit () {
	autoAddItem();
	initItemGroupDialog();
	if (document.storesitemmasterform.service_group_id.value == '') {
		setSelectedIndexText(document.storesitemmasterform.service_group_id, 'Pharmacy item');
	}
	loadServiceSubGroup();
	setSelectedIndex(document.storesitemmasterform.service_sub_group_id,
			document.storesitemmasterform.subGroupId.value);
	focus();

	if (masterModified == 'Y') {
		// if we were opened from the PO/GRN etc. screens (the opener), tell them that master is modified
		// so that they can refresh their autocomplete.
		if (window.opener && window.opener.setMasterModified) {
			window.opener.setMasterModified(true);
		}
	}
	initItemHaCodeTypeDialog();
}

function loadCodeTypes(obj) {
	if(!empty(obj)) {
		var list = filterList(healthAuthSpecificCodeTypesJson,"health_authority",obj.value);
		if(list != null && list.length > 0) {
			loadSelectBox(document.getElementById('code_type'),list,'drug_code_type','drug_code_type','-- Select --',"");
		}
	}
}

var itemHaCodeTypeDialog;
var haCodeTypeAction = '';
var haCodeTypeEditedrowId = '';
function initItemHaCodeTypeDialog() {
	itemHaCodeTypeDialog = new YAHOO.widget.Dialog("itemHaCodeTypeDialog",
			{
			width:"400px",
			context :["btnAddItemsHaCodeTypes", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
			});

			var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleItemHaCodeTypesCancel,
	                                                scope:itemHaCodeTypeDialog,
	                                                correctScope:true } );
			itemHaCodeTypeDialog.cfg.queueProperty("keylisteners", escKeyListener);

	itemHaCodeTypeDialog.render();
}

function getItemHaCodeTypesDialog(id){
	button = document.getElementById("btnAddItemsHaCodeTypes"+id);
	itemHaCodeTypeDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	haCodeTypeAction = 'add';
	itemHaCodeTypeDialog.show();
}

function editItemHaCodeDialog(id) {
	button = document.getElementById("haEditBut"+id);
	itemHaCodeTypeDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	haCodeTypeAction = 'edit';
	haCodeTypeEditedrowId = id;
	itemHaCodeTypeDialog.show();
	document.storesitemmasterform.health_authority.value = document.getElementById('healthAuth'+id).textContent;
	loadCodeTypes(document.storesitemmasterform.health_authority);
	document.storesitemmasterform.code_type.value = document.getElementById('h_ha_code_type'+id).textContent;
}

function validateHaCodeTypesDialog() {
	var healthAuth = document.storesitemmasterform.health_authority;
	var itemCodeType = document.storesitemmasterform.code_type;

	if(empty(healthAuth.value)) {
		alert("health authority is required");
		healthAuth.focus();
		return false;
	}

	if(empty(itemCodeType.value)) {
		alert("item code type is required");
		itemCodeType.focus();
		return false;
	}

	return true;
}

function checkDuplicateHaCodeTypes() {
	var itemListTable = document.getElementById('itemHaCodeTypeTable');
	var numRows = itemListTable.rows.length-1;
	var healthAuth = document.getElementsByName('h_health_authority');
	var itemCodeTypes = document.getElementsByName('h_code_type');
	var itemDeleted = document.getElementsByName('h_ha_deleted');
	var numRows = itemListTable.rows.length-2;

	var dHealthAuth = document.storesitemmasterform.health_authority.value;
	var dItemCodeType = document.storesitemmasterform.code_type.value;

	for(var i=0;i<numRows;i++) {
		if(haCodeTypeAction == 'edit' && haCodeTypeEditedrowId == i+1) continue;
		if(itemDeleted[i].value == 'false') {
			if(healthAuth[i].value == dHealthAuth) {
				alert("dupliacte code types are not allowed to add for same health authority");
				return false;
			}
		}
	}
	return true;
}

function allowForUndelete(index) {
	var itemListTable = document.getElementById('itemHaCodeTypeTable');
	var numRows = itemListTable.rows.length-1;
	var healthAuth = document.getElementsByName('h_health_authority');
	var itemCodeTypes = document.getElementsByName('h_code_type');
	var itemDeleted = document.getElementsByName('h_ha_deleted');
	var numRows = itemListTable.rows.length-2;

	var dHealthAuth = document.getElementById('h_health_authority'+index).value;
	var dItemCodeType = document.getElementById('h_code_type'+index).value;

	for(var i=0;i<numRows;i++) {
		if(i+1 == index) {
			continue;
		}

		if(healthAuth[i].value == dHealthAuth) {
			alert("dupliacte code types are not allowed to add for same health authority");
			return false;
		}
	}

	return true;
}
function AddRecord() {
	if (validateHaCodeTypesDialog() && checkDuplicateHaCodeTypes()) {
		if(haCodeTypeAction == 'add') {
			var itemListTable = document.getElementById("itemHaCodeTypeTable");
		    var numRows = itemListTable.rows.length-1;
		    var id = numRows;
			var row = itemListTable.insertRow(id);

		    var healthAuth = document.storesitemmasterform.health_authority.value;
		    var codeType = document.storesitemmasterform.code_type.value;

		    var itemId='';
		    if (document.storesitemmasterform.medicine_id != null){
		    	itemId = document.storesitemmasterform.medicine_id.value ;
		    }

			var cell;
		    cell = row.insertCell(-1);
		    cell.setAttribute("class","forminfo");
		 	cell.innerHTML = '<label id="healthAuth'+id+'">'+healthAuth+'</label>' +
			        '<input type="hidden" name="h_health_authority" id="h_health_authority'+id+'" value="'+healthAuth+'">'+
			        '<input type="hidden" name="h_ha_code_type_id" id="h_ha_code_type_id'+id+'" value="">'+
			        '<input type="hidden" name="hacodeoldrnew" id="hacodeoldrnew'+id+'" value="new">'+
					'<input type="hidden" name="hmedicineId" id="hmedicineId'+id+'" value="'+itemId+'">';

			cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
		    cell.setAttribute("style","width:300px;");
			cell.innerHTML = '<label id="h_ha_code_type'+id+'">'+codeType+'</label>'+
					'<input type="hidden" name="h_code_type" id="h_code_type'+id+'" value="'+codeType+'">';

			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "haDelItem" id="haDelItem'+id+'" ' + 'onclick="deleteHaCodeTypeItem(this, '+id+')" src="'+cpath+'/icons/Delete.png">' +
			              '<input type="hidden" name="h_ha_deleted" id="h_ha_deleted'+id+'"  value="false">';

			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "haEditBut" id="haEditBut'+id+'" ' + 'onclick="editItemHaCodeDialog('+id+')" src="'+cpath+'/icons/Edit.png">';

		    document.storesitemmasterform.health_authority.options.selectedIndex = 0;
    		document.storesitemmasterform.code_type.options.selectedIndex = 0;
	   	}
	    if(haCodeTypeAction == 'edit') {
	    	if(checkDuplicateHaCodeTypes()) {
		    	document.getElementById('healthAuth'+haCodeTypeEditedrowId).textContent = document.storesitemmasterform.health_authority.value;
		    	document.getElementById('h_ha_code_type'+haCodeTypeEditedrowId).textContent =  document.storesitemmasterform.code_type.value;
		    	document.getElementById('h_health_authority'+haCodeTypeEditedrowId).value = document.storesitemmasterform.health_authority.value;
		    	document.getElementById('h_code_type'+haCodeTypeEditedrowId).value = document.storesitemmasterform.code_type.value;

		    	document.storesitemmasterform.health_authority.options.selectedIndex = 0;
	    		document.storesitemmasterform.code_type.options.selectedIndex = 0;
	    	}
	    }
	  }else {
	  	return false;
	  }

}

function handleItemHaCodeTypesCancel(){
	document.storesitemmasterform.health_authority.options.selectedIndex = 0;
    document.storesitemmasterform.code_type.options.selectedIndex = 0;
	itemHaCodeTypeDialog.cancel();
}

function deleteHaCodeTypeItem(checkBox, rowId) {
	var itemListTable = document.getElementById("itemHaCodeTypeTable");
	var row = itemListTable.rows[rowId];
	var img = document.createElement("img");
	img.setAttribute("name", "haEditBut");
	img.setAttribute("id", "haEditBut"+rowId);
	img.setAttribute("style", "cursor:pointer;");
	img.setAttribute("src", cpath + "/icons/Edit1.png");
	img.setAttribute("class", "button");

	var deletedInput = document.getElementById('h_ha_deleted'+rowId);
	if (deletedInput.value == 'false') {
		var ok = confirm("item codes will be deleted from store item codes"+"\n"+"do you want to continue?");
		if(ok) {
			document.getElementById('haDelItem'+rowId).src = cpath+"/icons/Deleted.png";
			document.getElementById('haEditBut'+rowId).src = cpath+"/icons/Edit1.png";
			for (var i=row.cells[3].childNodes.length-1; i>=0; i--) {
				row.cells[3].removeChild(row.cells[3].childNodes[i]);
			}
			row.cells[3].appendChild(img);
			deletedInput.value = 'true';
			row.className = "deleted";
		} else {
			return;
		}
	} else {
		if(allowForUndelete(rowId)) {
			deletedInput.value = 'false';
			document.getElementById('haDelItem'+rowId).src = cpath+"/icons/Delete.png";
			img.setAttribute("onclick", "editItemHaCodeDialog('"+rowId+"')");
			for (var i=row.cells[3].childNodes.length-1; i>=0; i--) {
				row.cells[3].removeChild(row.cells[3].childNodes[i]);
			}
			row.cells[3].appendChild(img);
			document.getElementById('haEditBut'+rowId).src = cpath+"/icons/Edit.png";
			row.className = "";
		}
	}
}


var oAutoComp3;
/**
*  this method contains genericNames AutoComplete
*
*/
function initGenericAutoComplete(jGenericNames) {
    YAHOO.example.ACJSAddArray = new function() {
	var dataSource = new YAHOO.widget.DS_JSArray(jGenericNames);
	oAutoComp3 = new YAHOO.widget.AutoComplete('generic_name', 'generic_dropdown', dataSource);
	oAutoComp3.maxResultsDisplayed = 10;
	oAutoComp3.allowBrowserAutocomplete = false;
	oAutoComp3.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp3.typeAhead = false;
	oAutoComp3.useShadow = false;
	oAutoComp3.forceSelection = true;
	oAutoComp3.minQueryLength = 0;
  }

}
/*function initPackageUOMAutoComplete(fieldName, divContainer, parameters, dataArray) {
   dataSource = new YAHOO.util.LocalDataSource({result : dataArray});
   var fieldsArray = [];
   for ( var i=0; i<parameters.length; i++ )
   	fieldsArray[i] = { key: parameters[i] };
	dataSource.responseSchema = {
		resultsList : 'result',
		fields : fieldsArray
	};

	var autoComplete = new YAHOO.widget.AutoComplete(fieldName, divContainer, dataSource);
	autoComplete.minQueryLength = 0;
	autoComplete.maxResultsDisplayed = 50;
	autoComplete.forceSelection = true;
	autoComplete.resultTypeList = false;
	autoComplete.typeAhead = false;
	autoComplete.useShadow = false;
	autoComplete.animVert = false;
	return autoComplete;
  }*/

function validateAllFields() {
	var medicineName = document.getElementById("medicine_name");
	if (medicineName.value == null || medicineName.value == '') {
		alert("Enter the item name");
		medicineName.focus();
		return false;
	}

	var medicine_short_name = document.getElementById("medicine_short_name");
	if (medicine_short_name.value == null  || medicine_short_name.value == '') {
		alert("Enter the short name");
		medicine_short_name.focus();
		return false;
	}

	var manf_name = document.getElementById("manf_name");
	if (manf_name.value == null || manf_name.value == ''){
		alert("Enter the manufacturer name");
		manf_name.focus();
		return false;
	}

	var med_category_id = document.getElementById("med_category_id");
	if (med_category_id.selectedIndex == 0) {
		alert("Enter the category");
		med_category_id.focus();
		return false;
	}

	var isInsuranceCatIdSelected = false;
	var insuranceCatId = document.getElementById('insurance_category_id');
	for (var i=0; i<insuranceCatId.options.length; i++) {
	  if (insuranceCatId.options[i].selected) {
		isInsuranceCatIdSelected = true;
	  }
	}
	if (!isInsuranceCatIdSelected) {
		alert("Please select at least one insurance category");
		return false;
	}

	var issUOM = document.getElementById('issue_units');
	if (issUOM.value == null  || issUOM.value == '') {
		alert("Enter the Issue UOM");
		issUOM.focus();
		return false;
	}
	var packUOM = document.getElementById('package_uom');
	if (packUOM.value == null  || packUOM.value == '') {
		alert("Enter the Package UOM");
		packUOM.focus();
		return false;
	}

	var issue_base_unit = document.getElementById("issue_base_unit");
	if(issue_base_unit.value == 0){
		alert("Package size can not be zero");
		issue_base_unit.value='';
		issue_base_unit.focus();
		return false;
	}
	var item_strength = document.getElementById('item_strength').value;
   	var strength_units = document.getElementById('item_strength_units').value;
   	strength_units = item_strength == '' ? '' : strength_units;


	var subGroupId = document.storesitemmasterform.service_sub_group_id.value;
	if (subGroupId == '') {
		alert("Enter the Service Sub Group");
		document.storesitemmasterform.service_sub_group_id.focus();
		return false;
	}

	if (opeartion == 'add' && prefBarCode == 'Y') {
		if (trimAll(document.storesitemmasterform.item_barcode_id.value) == '' && document.storesitemmasterform.itembarcodechk.checked == false) {
			alert("Barcode is Mandatory pls select one of the below \n 1) Check Generate barcode \n 2) Enter Custom Barcode");
			document.storesitemmasterform.item_barcode_id.value = '';
        	document.storesitemmasterform.itembarcodechk.focus();
			return false;
		}
	} else if (opeartion == 'show' && prefBarCode == 'Y') {
		if (trimAll(document.storesitemmasterform.item_barcode_id.value) == '') {
			alert("Barcode is Mandatory pls Enter Custom Barcode");
			document.storesitemmasterform.item_barcode_id.focus();
			return false;
		}
	}
	
	if (!isDrugCategory()) {
		return false;
	}
	return true;
}

function loadServiceSubGroup() {
	var groupId = document.storesitemmasterform.service_group_id.value;
	var filteredList = filterList(subGroups, 'SERVICE_GROUP_ID', groupId);
	loadSelectBox(document.storesitemmasterform.service_sub_group_id,
			filteredList, 'SERVICE_SUB_GROUP_NAME', 'SERVICE_SUB_GROUP_ID', '-- Select --', '');
}

function trimMedName(obj){
	var medName = obj.value;
	medName =  medName.replace(/^\s+/,'').replace(/\s+$/,'');
	obj.value = medName;
	storesitemmasterform.medicine_short_name.value = medName;

}

function hidecustitemcode() {
	if (document.storesitemmasterform.cust_item_code_chk.checked) {
		document.storesitemmasterform.cust_item_code.value = '';
		document.storesitemmasterform.cust_item_code.readOnly = true;
	} else {
		document.storesitemmasterform.cust_item_code.value = '';
		document.storesitemmasterform.cust_item_code.readOnly = false;
	}
}

function hidecode() {
	if (document.storesitemmasterform.itembarcodechk.checked) {
		document.storesitemmasterform.item_barcode_id.value = '';
		document.storesitemmasterform.item_barcode_id.readOnly = true;
	} else {
		document.storesitemmasterform.item_barcode_id.value = '';
		document.storesitemmasterform.item_barcode_id.readOnly = false;
	}
}
function getBarcodePrint () {
        if (savedBarCode != '') {
			var med = document.storesitemmasterform.medicine_id.value;
			if (prefBarCode == 'Y' && med != '') window.open(popurl+"/pages/registration/GenerateRegistrationBarCode.do?method=execute&itemId="+med+"&barcodeType=ItemMaster");
		} else {
		  alert("No Barcode Print for this Item - we suspect one of the following reason \n * Barcode doesn't given  \n * Barcode not yet saved");
		}
}
var count = 0;

function changePackageUom(obj) {
	var item = obj.value;
	var packUom = filterList(isuuePackageList, "issue_uom", item);
	if (packUom != null && packUom.length != 0) {
		var selectBox = document.getElementById('package_uom');
		var selectValue = selectBox.value;
		var index = 0;
		selectBox.length = index+1;
		selectBox.options[index].text = "-- Select --";
		selectBox.options[index].value = "";
		if (empty(item)) {
			for (var i=0;i<isuuePackageList.length;i++) {
				index++;
				selectBox.length = index+1;
				selectBox.options[index].text = isuuePackageList[i].package_uom;
				selectBox.options[index].value = isuuePackageList[i].package_uom;
			}
		}else {
			for (var i=0;i<packUom.length;i++) {
				index++;
				selectBox.length = index+1;
				selectBox.options[index].text = packUom[i].package_uom;
				selectBox.options[index].value = packUom[i].package_uom;
			}
		}
		if (count == 0) {
			setSelectedIndex(selectBox, selectValue);
			count ++;
		} else {
			setSelectedIndex(selectBox, "");
		}
		if (document.getElementById('issue_units').value == "") {
			setSelectedIndex(selectBox, "");
		}
	}
}

function changePackageSize(obj) {
	var item = obj.value;
	var issueList = filterList(isuuePackageList, "issue_uom", document.getElementById('issue_units').value);
	var packList = null;
	if (issueList != null && issueList.length !=0) {
		 packList = filterList(issueList, "package_uom", item);
	}
	if (packList != null && packList.length == 1) {
		document.getElementById('issue_base_unit_label').textContent = packList[0].package_size;
		document.getElementById('issue_base_unit').value = packList[0].package_size;
	}
}

function trimNum(e, field) {
	var tbValues = (field.value).split(",");
	for (var i = 0; i < tbValues.length; i++) {
		tbValues[i] =  (tbValues[i].trim()) ;

		// Upon copy and paste(Ctrl-V),
		// removes all other characters excluding numerals, periods and hyphens.
		tbValues[i] = tbValues[i].replace(/[^\d\.\-]/g, "");

		//matches continuous periods  and hyphens, and removes them.
		 tbValues[i] = tbValues[i].replace(/(\.)\1/g, "");
		 tbValues[i] = tbValues[i].replace(/(\-)\1/g, "");

		//matches duplicate periods and removes them
		 tbValues[i] = tbValues[i].replace(/(\-*\d*\.\d*\-*)?\./g, function($0, $1){
			return $1 ? $1 + '' : $0;
			});

		//matches duplicate hyphens and removes them
		tbValues[i] = tbValues[i].replace(/(\-\d*\.*)?\-/g, function($0, $1){
			return $1 ? $1 + '' : $0;
			});

		//matches a hyphen preceded by number and removes it
		tbValues[i] = tbValues[i].replace(/(\d+\.*)?\-/g, function($0, $1){
			return $1 ? $1 + '' : $0;
		});
	}
	field.value = tbValues;
	if(field.value== '' || isNaN(field.value))
		field.value = 0;
}
function onChangeTaxRate(taxRate){
	if ( taxRate.value == '' ) taxRate.value = 0;
}

function doValidate(formType) {
	var extall=".xls";

	var file = document.importForm.uploadFile.value;
	var ext = file.split('.').pop().toLowerCase();
	if(parseInt(extall.indexOf(ext)) < 0)
	{
		alert("Please upload : xls file !");
		return false;
	}
	doUpload(formType);
}

function doUpload(formType) {
	var form = null;
	if(formType == 'importForm') {
		form = document.importForm;
		if (empty(form.uploadFile.value)) {
			alert("Please browse and select a file to upload");
			return false;
		}
	} else if(formType == 'uploaditemhealthcodetypeform'){
		form = document.uploaditemhealthcodetypeform;
		if (empty(form.uploadFile.value)) {
			alert("Please browse and select a file to upload");
			return false;
		}
	} else {
		form = document.uploaditemcodeform;
		if (empty(form.uploadFile.value)) {
			alert("Please browse and select a file to upload");
			return false;
		}
	}
	form.submit();
}

var oAutoAddItem;
function autoAddItem(){
	var itemNames = [];
    var j = 0;
		var dataSource = new YAHOO.widget.DS_JSArray(itemDetailsList);
		dataSource.responseSchema = {
			resultsList: "result", 
			fields: [{key: "CUST_ITEM_CODE_WITH_NAME"},{key: "MEDICINE_NAME"}, {key: "MEDICINE_ID"}]
		};

		oAutoAddItem = new YAHOO.widget.AutoComplete("medicine_name","itemcontainer",dataSource);
		oAutoAddItem.minQueryLength = 0;
		oAutoAddItem.typeAhead = false;
		oAutoAddItem.prehighlightClassname = "yui-ac-prehighlight";
		oAutoAddItem.autoHighlight = true;
		oAutoAddItem.useShadow = false;
		oAutoAddItem.forceSelection = false;
		oAutoAddItem.maxResultsDisplayed = 20;
		oAutoAddItem.allowBroserAutocomplete = false;

		oAutoAddItem.filterResults = Insta.queryMatchWordStartsWith;
		oAutoAddItem.formatResult = Insta.autoHighlightWordBeginnings;
		oAutoAddItem.itemSelectEvent.subscribe(onSelectAddItem);
}

function onSelectAddItem(type,args) {
	var item = findInList(itemDetailsList, 'MEDICINE_NAME', args[2][1]);
	if(item != null){
		storesitemmasterform.medicine_name.value = item.MEDICINE_NAME;
		storesitemmasterform.medicine_short_name.value = item.MEDICINE_NAME;
	}
}

function getCategoryDetailsAjax() {
	var catId = document.getElementById("med_category_id").value;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath
			+ "/master/StoresMaster.do?_method=getCategorydetailsAJAX&category_id="+catId;

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function getItemFormDetailsAjax() {
	var itemFormId = document.getElementById("item_form_id").value;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath
			+ "/master/itemforms/show.json?&item_form_id="+itemFormId;

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function isDrugCategory() {
	ajaxCategoryDetails = getCategoryDetailsAjax();
	isDrug = ajaxCategoryDetails.isDrug;
	if (isDrug == 'Y') {
		var item_form_id = document.getElementById("item_form_id");
		if (item_form_id.selectedIndex == 0) {
			alert("Select Item Form");
			item_form_id.focus();
			return false;
		}

		var route_of_admin = document.getElementById("route_of_admin");
		if (!route_of_admin.value) {
			alert("Select Route");
			route_of_admin.focus();
			return false;
		}

		var cons_uom_id = document.getElementById("cons_uom_id");
        	    if (cons_uom_id.selectedIndex == 0) {
        		alert("Enter the Consumption UOM");
        		cons_uom_id.focus();
        		return false;
        }

		var generic_name = document.getElementById("generic_name");
		if (!generic_name.value) {
			alert("Enter Generic Name");
			generic_name.focus();
			return false;
		}
	}
	return true;
}