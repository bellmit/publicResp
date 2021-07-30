var toolbar = {}
	toolbar.Sale= {
		title: toolbarOptions["sales"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/MedicinePrescList.do?_method=getSalesScreen',
		target:'_blank',
		onclick: null,
		description: toolbarOptions["sales"]["description"]
	};
	toolbar.PresPrint= {
		title: toolbarOptions["prescriptionprint"]["name"],
		imageSrc: "icons/Print.png",
		href: 'print/printPresConsultation.json?',
		target:'_blank',
		onclick: null,
		description: toolbarOptions["prescriptionprint"]["description"]
	};
	toolbar.DischargePrint= {
		title: toolbarOptions["dischargeprint"]["name"],
		imageSrc: "icons/Print.png",
		href: '/pages/dischargeMedicationPrint.do?_method=dischargeMedicationPrint',
		target:'_blank',
		onclick: null,
		description: toolbarOptions["dischargeprint"]["description"]
	};	
var theForm = document.listSearchForm;
psAc = null;

function init() {
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
	theForm = document.listSearchForm;
	createToolbar(toolbar);
	initDocAutoComplete ();
}

function initDocAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(docList);
		oAutoComp = new YAHOO.widget.AutoComplete('doctor', 'doc_dropdown', dataSource);
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
 			showMessage("js.sales.issues.userreturns.noassignedstore.donthaveaccess.thisscreen");
 			document.getElementById("storecheck").style.display = 'none';
 		}
 	}
}



