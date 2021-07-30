var toolbar = {}
	toolbar.DuplicateSaleReport= {
		title: toolbarOptions["duplicatesalebills"]["name"],
		imageSrc: "icons/Report.png",
		href: '/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&duplicate=true',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["duplicatesalebills"]["description"],
		show: (salePrintRights == 'A')
	};

	toolbar.SaleReport= {
		title: toolbarOptions["salebills"]["name"],
		imageSrc: "icons/Report.png",
		href: '/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&duplicate=false',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["salebills"]["description"],
		show: (salePrintRights == 'A')
	};

	toolbar.Report= {
		title: toolbarOptions["hospitalbill"]["name"],
		imageSrc: "icons/Report.png",
		href: '/pages/Enquiry/billprint.do?_method=pharmaBreakupBill&duplicate=true',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["hospitalbill"]["description"],
		show: (billPrintRights == 'A')
	};

	toolbar.EditClaim= {
		title: toolbarOptions["editsalesreturn"]["name"],
		imageSrc: "icons/Edit.png",
		href: '/pages/stores/editSales.do?_method=getSaleDetails&duplicate=true',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["editsalesreturn"]["description"],
		show: (editRights == 'A')
	};

	toolbar.PrescriptionLabel= {
		title: toolbarOptions["prescriptionlabelprint"]["name"],
		imageSrc: "icons/Report.png",
		href: '/pages/stores/MedicineSalesPrint.do?method=printPrescLabel&duplicate=false',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["prescriptionlabelprint"]["description"],
		show: (salePrintRights == 'A')
	};


var theForm = document.dupSalesSearchForm;

function init() {
	theForm = document.dupSalesSearchForm;
	initMrNoAutoComplete(cpath);
	theForm.mr_no.focus();
	printerType = theForm._printerType.value;
	if(theForm._labelPrinterType)
		labelPrinterType = theForm._labelPrinterType.value;
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}

function setPrinterId(obj) {
	printerType = obj.value;
}
function setLabelPrinterId(obj) {
	labelPrinterType = obj.value;
}
function setTemplatename(obj) {
	templatename = obj.value;
}


