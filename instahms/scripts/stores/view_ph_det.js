var toolbar = {}
	toolbar.Edit= {
			title: toolbarOptions["edititembatchdetails"]["name"],
			imageSrc: "icons/Edit.png",
			href: 'stores/StoreItemBatchDetails.do?_method=show',
			onclick: null,
			description:toolbarOptions["edititembatchdetails"]["description"],
			show : (!empty(addEditRights) && addEditRights == 'A')
		};

	toolbar.Report= {
		title: toolbarOptions["debitprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["debitprint"]["description"]
	};

	toolbar.GrnReport= {
		title: toolbarOptions["grnprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'stores/stockentry.do?_method=generateGRNprint',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["grnprint"]["description"]

};
var theForm = document.PhPurchaseDetailsSearchForm;

function init() {
	theForm = document.PhPurchaseDetailsSearchForm;
	theForm.store.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
	initItemAutoComplete();
	initItemAutoCompleteforGen();
}

/**
 *  this method contains itemNames AutoComplete
 *
 */
function initItemAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(jmedNames);
		dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "CUST_ITEM_CODE_WITH_NAME"},{key : "MEDICINE_ID"},{key : "MEDICINE_NAME"} ]
		};
		oAutoComp = new YAHOO.widget.AutoComplete('medicine_name', 'med_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 20;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
		oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		oAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
		oAutoComp.itemSelectEvent.subscribe(onSelectItem);
	}

}
var onSelectItem = function(type, args) {
	theForm.medicine_name.value = args[2][2];
}

function initItemAutoCompleteforGen() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(jgenNames);
		oAutoComp1 = new YAHOO.widget.AutoComplete('generic_name', 'gen_dropdown', dataSource);
		oAutoComp1.maxResultsDisplayed = 20;
		oAutoComp1.allowBrowserAutocomplete = false;
		oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp1.typeAhead = false;
		oAutoComp1.useShadow = false;
		oAutoComp1.minQueryLength = 0;
		oAutoComp1.forceSelection = true;
	}

}
function funValidate() {
	if (trimAll(theForm.medicine_name.value) == '' && trimAll(theForm.generic_name.value) == '') {
		showMessage("js.stores.procurement.enteritemname.genericname");
		theForm.medicine_name.value = '';
		theForm.medicine_name.focus();
		return false;
	}
	return true;
}

