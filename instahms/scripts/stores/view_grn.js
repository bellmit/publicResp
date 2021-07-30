
var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["grnprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'stores/stockentry.do?_method=generateGRNprint',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["grnprint"]["description"]
	};

	toolbar.StockEntry= {
		title: toolbarOptions["editgrn"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/stockentry.do?_method=getScreen',
		onclick: null,
		description: toolbarOptions["editgrn"]["description"]
	};

	toolbar.ViewGRN= {
		title: toolbarOptions["vieweditgrn"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/stockentry.do?_method=getScreen',
		onclick: null,
		description: toolbarOptions["vieweditgrn"]["description"].replace("#", ',')
};

var theForm = document.GrnSearchForm;

function init() {
	theForm = document.GrnSearchForm;
	//setSelectedIndex(theForm.supplier_id,suppId);
	initSupplierNames();
//	setMultipleSelectedIndexs(theForm.dept_id,storeArray);
	theForm.grn_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}
function onKeyPressGRNno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeGRNno();
	} else {
		return true;
	}
}

function onChangeGRNno() {
	var grnNoBox = theForm.grn_no;
	// complete
	var valid = addPrefix(grnNoBox, 'GR', 4);
	if (!valid) {
		showMessage("js.stores.procurement.invalidgrno");
		theForm.grn_no.value = ""
		theForm.grn_no.focus();
		return false;
	}
	return true;
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
						{key : 'supplier_code'},
						{key : 'cust_supplier_code'}
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


		itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			GrnSearchForm.supplier_name.value = elItem[2].supplier_name;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			GrnSearchForm.supplier_name.value = '';
		});

}
}
