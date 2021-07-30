var toolbar = {}
	toolbar.SalePayment= {
		title: toolbarOptions["salepayment"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'pages/stores/RetailpendingSalesBill.do?_method=getRetailPendingSaleList',
		onclick: null,
		description: toolbarOptions["salepayment"]["description"]

};
var theForm = document.RetCrdtpendingSalesSearchForm;

function init() {
	theForm = document.RetCrdtpendingSalesSearchForm;
	theForm.bill_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}

/*
 * Complete the Bill No.
 */
function onKeyPressBillNo(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeBillNo();
	} else {
		return true;
	}
}

function onChangeBillNo() {
	var billNoBox = theForm.bill_no;
	// complete
	var valid = addPrefix(billNoBox, gBillNoPrefix, gBillNoDigits);

	if (!valid) {
		showMessage("js.sales.issues.invalidbillformat");
		theForm.bill_no.value = ""
		theForm.bill_no.focus();
		return false;
	}
}