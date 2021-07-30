var toolbar = {
	Edit: {
		title: "Edit",
		imageSrc: "icons/Edit.png",
		href: 'pages/BillDischarge/allocateSponsorBill.do?_method=view',
		onclick: null,
		description: "Edit Receipt details"
		},
	Print: {
	    title: "Print",
	    imageSrc:"icons/Print.png",
	    href:'',			// will be set in changePrinter() function
	    onclick: '',
		target:"_blank",
	    description: "Print Receipt"
	   }
};
function init()
{
	createToolbar(toolbar);
	showFilterActive(document.sponsorReceipts);
	changePrinter();
}
function changePrinter() {
	if (document.printerSelectForm != null) {
	var template = document.printerSelectForm.printTemplate.value;
	var printer = document.printerSelectForm.printer.value;
	toolbar.Print.href = "pages/BillDischarge/allocateSponsorBill.do?_method=sponsorReceiptPrint"+
		"&printTemplate="+template + "&printerType="+printer;
	}
}