var theForm = document.rejectIndentSearchForm;

var toolbar = {}
toolbar.Print= {
	title: toolbarOptions["print"]["name"],
	imageSrc: "icons/Print.png",
	href: 'stores/storesIndent.do?_method=generateReport&target=_blank',
	onclick: null,
	target:"_blank",
	description: toolbarOptions["print"]["description"]
};

function init() {
	theForm = document.rejectIndentSearchForm;
	createToolbar(toolbar);
	checkstoreallocation();
	//theForm.requester_name.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	initItemAutoComplete();
}

function initItemAutoComplete() {
	var items = {"item_names": jItemNames};
	var localDs = new YAHOO.util.LocalDataSource(items, { queryMatchContains : true });
	localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = { resultsList : "item_names",
		fields: [ {key : "cust_item_code_with_name"},{key : "medicine_name"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('medicine_name', 'item_dropdown', localDs);
	autoComp.maxResultsDisplayed = 50;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = false;
	autoComp.minQueryLength = 0;
	autoComp.forceSelection = true;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;
	autoComp.itemSelectEvent.subscribe(onSelectItem);

}

var onSelectItem = function(type, args) {
	document.rejectIndentSearchForm.medicine_name.value = args[2][1];
}

function checkAllowAccess() {
 	if(gAccessAllow == 'N') {
 		showMessage("js.stores.mgmt.notaccessthisscreen");
 		document.getElementById("storecheck").style.display = 'none';

 	}
}

function setSelected(obj) {
	var id = obj.id.substring('_select'.length);
	var selected = document.getElementById('_selected' + id);
	if (obj.checked){
		 selected.value = 'Y'
	} else{
		selected.value='N';
	}
}


function funActions(action){
	var deptId = document.getElementById("indent_store").value;
	var actionToDo = action;
	var selectedItems = false;

	var len = document.rejectIndentSearchForm._select.length;
	for(var i=0;i<len;i++){
		if(document.rejectIndentSearchForm._select[i].checked)
			selectedItems = true;
	}

	if(len == undefined){
		if(document.rejectIndentSearchForm._select.checked)
				selectedItems = true;
	}

	if(!selectedItems){
		showMessage("js.stores.mgmt.selectitems");
		return false;
	}


/*	if(actionToDo == "stockAdjust"){
		document.rejectIndentSearchForm.action = cpath+"/stores/StoresStockAdjust.do?dept_id="+deptId;
		document.rejectIndentSearchForm._method.value = "getStockAdjForRejIndents";
	}*/
	if(actionToDo == "takeIntoStock"){
		document.rejectIndentSearchForm.action = cpath+"/stores/StoresRejectIndents.do?dept_id="+deptId;
		document.rejectIndentSearchForm._method.value = "updateRejectedStockInStore";
	}
/*	if(actionToDo == "supplierReturns"){
		document.rejectIndentSearchForm.action = cpath+"/stores/StoresSupplierReturnslist.do?dept_id="+deptId;
		document.rejectIndentSearchForm._method.value = "listForReject";
	}if(actionToDo == "supplierDebitNotes"){
		document.rejectIndentSearchForm.action = cpath+"/stores/StoresSupplierReturnslist.do?dept_id="+deptId;
		document.rejectIndentSearchForm._method.value = "getSupplierDebitForReject";
	}*/
	document.rejectIndentSearchForm._transaction.value = "group";
	document.rejectIndentSearchForm.submit();
}

function checkstoreallocation() {
	if(gRoleId != 1 && gRoleId != 2) {
		if((!empty(isUserHavingSuperStore) && isUserHavingSuperStore == 'N') || (!empty(storeAccess) && storeAccess == 'N')) {
			showMessage("js.stores.mgmt.noassignedstore.notaccessscreen");
			document.getElementById("storecheck").style.display = 'none';
		}
	}
}

function clearRejectedForm(obj) {
	clearForm(obj);
	var indentStore = document.getElementById("indent_store");
	if(indentStore.options != undefined && indentStore.options != null) {
		for(var i=0; i<indentStore.options.length; i++) {
			if(deptId == indentStore.options[i].value) {
				document.getElementById("indent_store").value = deptId;
			}
		}
	}
	
}