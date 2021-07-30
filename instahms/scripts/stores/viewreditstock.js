var b_no = new Array();

var theForm = document.stockSearchForm;

function init() {
	theForm = document.stockSearchForm;
	setMultipleSelectedIndexs(theForm.med_category_id,catArray);
	createToolbar(toolbar);
	document.getElementById("_method").value = "list";
}

function clearSearch() {
	  	document.stockSearchForm.dept_id.value = '0';
	  	document.stockSearchForm.category_name.value = '';
	  	document.stockSearchForm.medicine_name.value = '';
	  	document.stockSearchForm.batch_no.value = '';
	  	document.stockSearchForm.stocktype.value = 'all';
	  	document.stockSearchForm.asset.value = 'all';
	  	document.stockSearchForm.stqty.value = 'Y';
	  	document.stockSearchForm.grn_no.value = '';
 		document.stockSearchForm.supplier_name.value = '';
  		document.stockSearchForm.invoice_no.value = '';
  		document.stockSearchForm.manf_name.value = '';
  		document.stockSearchForm.category.value = '';
  		document.stockSearchForm.exp_dt.value = '';
    }

function checkstoreallocation() {
 	if(gRoleId != 1 && gRoleId != 2) {
 		if(deptId == "") {
 			alert("There is no assigned store, hence you dont have any access to this screen");
 			document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}

function initSupplierAutoComplete() {
	var supplierNames = [];
	var j = 0;
	for (var i=0; i<jAllSuppliers.length; i++ ) {
		 if (jAllSuppliers[i].STATUS == 'A' ) supplierNames[j++] = jAllSuppliers[i].SUPPLIER_NAME;
	}
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(supplierNames);
		oAutoComp = new YAHOO.widget.AutoComplete('supplier_name', 'suppliername_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 20;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.forceSelection = true;
		oAutoComp.minQueryLength = 0;

	}


}