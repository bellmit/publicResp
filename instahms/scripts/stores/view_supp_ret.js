var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["viewprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["viewprint"]["description"]
	};

	toolbar.Replace= {
		title: toolbarOptions["replace"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=getSupplierReplacementScreen',
		onclick: null,
		description:toolbarOptions["replace"]["description"]
	};

	toolbar.Close= {
		title: toolbarOptions["close"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=getConfirmationScreen',
		onclick: null,
		description: toolbarOptions["close"]["description"]
	};

	toolbar.Reopen={
		title: toolbarOptions["reopen"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=getConfirmationScreen',
		onclick: null,
		description: toolbarOptions["reopen"]["description"]
};

var theForm = document.PhSuppRetSearchForm;

function init() {
	theForm = document.PhSuppRetSearchForm;
	//setSelectedIndex(theForm.supplier_id,suppId);
	theForm.return_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
	initSupplierNames();
}

var itAutoComplete = null;

function initSupplierNames() {
	if (itAutoComplete != undefined) {
		itAutoComplete.destroy();
	}

  YAHOO.example.itemArray = [];
	var i=0;
	for(var j=0; j<suppliers.length; j++)
		{
		   YAHOO.example.itemArray.length = i+1;
			YAHOO.example.itemArray[i] = suppliers[j];
			i++;
		}

   YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'cust_supplier_code_with_name'},
			           	{key : 'supplier_name'},
						{key : 'supplier_code'}
					]
		};

		itAutoComplete = new YAHOO.widget.AutoComplete('supplier_name','supplier_name_dropdown', datasource);
		itAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		itAutoComplete.useShadow = true;
		itAutoComplete.minQueryLength = 0;
		itAutoComplete.allowBrowserAutocomplete = false;
		itAutoComplete.resultTypeList = false;
		itAutoComplete.forceSelection = true;
		itAutoComplete.maxResultsDisplayed = 20;
		itAutoComplete.filterResults = Insta.queryMatchWordStartsWith;
		itAutoComplete.formatResult = Insta.autoHighlightWordBeginnings;

		itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			theForm.supplier_name.value = elItem[2].supplier_name;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
    	   theForm.supplier_name.value = '';
		});

}
}
