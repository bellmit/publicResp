var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/pages/masters/insta/stores/suppdetails.do?_method=getSupplierDetailsScreen',
		onclick: null,
		description: "View/Edit Supplier Details"
	},

};

var theForm = document.suppListSearchForm;

function init() {
	initSupplierNames();
	toolbar.Center = {
		 title: "Center Applicability",
		 imageSrc : "icons/Edit.png",
		 href: '/master/SupplierCenterApplicability.do?_method=getScreen',
		 onclick: null,
		 description : 'Center Applicability of this Supplier',
		 show: suppliersCenterapplicable
	};
	theForm = document.suppListSearchForm;
	theForm.supplier_name.focus();
	createToolbar(toolbar);
}

function initSupplierNames() {
	var itAutoComplete = null;
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
			suppListSearchForm.supplier_name.value = elItem[2].supplier_name;
		});
       itAutoComplete.selectionEnforceEvent.subscribe(function(){
			suppListSearchForm.supplier_name.value = '';
		});
       
}
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
	}
	form.submit();
}