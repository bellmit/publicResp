
var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["adjprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'DirectReport.do?report=PharmacyStockAdjustmentReport',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["adjprint"]["description"]
};

var theForm = document.StkAdjSearchForm;

function init() {
	theForm = document.StkAdjSearchForm;
	theForm.adj_no.focus();

	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}

function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 		showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
 		document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}

var itAutoComplete = null;

function initItemNames() {
	if (itAutoComplete != undefined) {
		itAutoComplete.destroy();
	}

	var selected_store_id = document.StkAdjSearchForm.store_id.value;
	YAHOO.example.itemArray = [];
	var i=0;
	for(var j=0; j<item_list.length; j++)
		{
		  if(selected_store_id == item_list[j].dept_id) {
		   YAHOO.example.itemArray.length = i+1;
			YAHOO.example.itemArray[i] = item_list[j];
			i++;  }
		}


   YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.itemArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'medicine_name'},
						{key : 'medicine_id'}
					]
		};

		itAutoComplete = new YAHOO.widget.AutoComplete('item_name','item_name_dropdown', datasource);
		itAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		itAutoComplete.useShadow = true;
		itAutoComplete.minQueryLength = 0;
		itAutoComplete.allowBrowserAutocomplete = false;
		itAutoComplete.resultTypeList = false;
		itAutoComplete.forceSelection = true;
		itAutoComplete.maxResultsDisplayed = 20;


		itAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			StkAdjSearchForm.item_name.value = elItem[2].medicine_name;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			StkAdjSearchForm.item_name.value = '';
		});

}
}
