	var toolbar = {

		Edit: {
			title: "Edit", imageSrc: "icons/Add.png", href: null,
			onclick: null,
			href: '/otservices/operations.do?_method=getOperationsConductionScreen',
			description: "Edit Operation"
		},

		OTReport: {
			title: "OT Report",	imageSrc: "icons/Add.png", href: 'otservices/operations.do?_method=getOTReportScreen',
			onclick: null,
			description: "View or Edit OT Report"
		}
	};


	function init() {
		Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
		createToolbar(toolbar);
		getOperations();
		initOpAutocomplete();

	}

	function SignOff(){
		var form = document.mainForm;
		form._method.value = "signOffSelectedReports";
		form.submit();
	}

	var operationNames = [];
	function getOperations() {
		for ( i=0; i<operations.length; i++ ) {
			operationNames[i] = operations[i]["OPERATION_NAME"];
		}
	}


	function initOpAutocomplete() {
		var form = document.mainForm;
		var opList = {"operations": operations };
		var dataSource = new YAHOO.widget.DS_JSArray(operationNames);
		var autoComp = new YAHOO.widget.AutoComplete('operation', 'opConatainer', dataSource);

		autoComp.prehightlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = true;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = true;
		autoComp.forceSelection = true;
		autoComp.animVert = false;
		autoComp.useIFrame = true;
		autoComp.formatResult = Insta.autoHighlight;
	}
