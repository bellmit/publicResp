var toolbar = {}
	toolbar.View= {
		title: toolbarOptions["view"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresReceiveIndents.do?_method=show',
		onclick: null,
		description: toolbarOptions["view"]["description"]
	};

	toolbar.Edit= {
		title: toolbarOptions["edit"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresReceiveIndents.do?_method=show',
		onclick: null,
		description: toolbarOptions["edit"]["description"]
};

var theForm = document.indentApprovalSearchForm;

function init() {
	theForm = document.indentReceiveSearchForm;
	theForm.requester_name.focus();
	var chkStatus=checkAllowAccess();
	if(chkStatus == false) {
	checkstoreallocation();
	}
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
	initUserNameAutoComplete();
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
function checkstoreallocation() {
	 	if(gRoleId != 1 && gRoleId != 2) {
	 		if(deptId == "") {
	 		showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
	 		}
	 	}
    }
function checkAllowAccess() {
	 	if(gAccessAllow == 'N') {
	 		showMessage("js.stores.mgmt.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
			return true ;
	 	}
	 	return false;
    }

