var toolbar = {}
	toolbar.SalePayment= {
		title: toolbarOptions["billpayment"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'pages/stores/PhItemsCreditBill.do?_method=getBillItemList',
		onclick: null,
		description: toolbarOptions["billpayment"]["description"]
	};
var theForm = document.pendingCrdtBillSearchForm;

function init() {
	theForm = document.pendingCrdtBillSearchForm;
	initMrNoAutoComplete(popurl);
	theForm.bill_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);


	if (receiptNo != '') {
		var url = popurl+'/pages/stores/PhItemsCreditBillPrint.do?_method=getCreditBillPrint'+
			'&receiptNo='+receiptNo+
			'&billNo='+billNo+
			'&customerId='+customerId+
			'&payType='+paymentType+
			'&doctor='+doctor+
			'&printerType='+printType;
		 window.open(url);
	}
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
