var toolbar = {}
	toolbar.Edit= {
		title: toolbarOptions["edit"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresPOApproval.do?_method=show',
		onclick: null,
		description:toolbarOptions["edit"]["description"]
	};

	toolbar.View= {
		title: toolbarOptions["view"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresPOApproval.do?_method=show',
		onclick: null,
		description: toolbarOptions["view"]["description"]
	};

var theForm = document.poApprovalSearchForm;

function init() {
	theForm = document.poApprovalSearchForm;
	createToolbar(toolbar);
}

function checkstoreallocation() {
	if(gRoleId != 1 && gRoleId != 2 && accessstores != 'A') {
	 	if(deptId == "") {
	 		showMessage("js.stores.procurement.noassignedstore.notaccessthisscreen");
	 		document.getElementById("storecheck").style.display = 'none';
	 		storeAccess = false;

	 	}

	}
}