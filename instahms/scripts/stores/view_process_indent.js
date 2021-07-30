var toolbar = {}
	toolbar.View= {
		title: toolbarOptions["view"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresIndentProcess.do?_method=show',
		onclick: null,
		description: toolbarOptions["view"]["description"]
	};

	toolbar.Edit= {
		title: toolbarOptions["edit"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresIndentProcess.do?_method=show',
		onclick: null,
		description: toolbarOptions["edit"]["description"]
	};
	toolbar.Print= {
		title: toolbarOptions["print"]["name"],
		imageSrc: "icons/Print.png",
		href: 'stores/StoresIndentProcess.do?_method=generateReport&target=_blank',
		onclick: null,
		target:"_blank",
		description: toolbarOptions["print"]["description"]
};

var theForm = document.indentProcessSearchForm;

function init() {
	theForm = document.indentProcessSearchForm;
	theForm.requester_name.focus();
	checkstoreallocation();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
	initUserNameAutoComplete();
	initItemAutoComplete();
}
function initUserNameAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(userNameList);
		oAutoComp = new YAHOO.widget.AutoComplete('requester_name', 'userName_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 20;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
	}
}

function initItemAutoComplete() {
	var localDs = new YAHOO.util.DataSource(cpath + "/pages/stores/getItemMaster.do", { queryMatchContains : true });
	localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = { resultsList : "result",
		fields: [ {key : "cust_item_code_with_name"},{key : "medicine_name"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete('item_name', 'item_dropdown', localDs);

	autoComp.maxResultsDisplayed = 50;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = false;
	autoComp.minQueryLength = 2;
	autoComp.forceSelection = true;
	autoComp.queryDelay = 0.6;
	autoComp.typeAheadDelay = 0.7;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;
	autoComp.itemSelectEvent.subscribe(onSelectItem);
}

var onSelectItem = function(type, args) {
	theForm.item_name.value = args[2][1];
}

function checkstoreallocation() {
	 	if(gRoleId != 1 && gRoleId != 2) {
	 		if(deptId == "") {
	 		showMessage("js.stores.mgmt.noassignedstore.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
	 		}
	 	}
    }

function generateReport(){
 if (getReport){
 	window.open(cpath+'/stores/StoresIndentProcess.do?_method=generateReport&indent_no='+indentno);
 }
}
