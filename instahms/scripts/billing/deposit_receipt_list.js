

var theForm = document.DepositReceiptSearchForm;
var toolbar = {}

toolbar.EditReceipt={
			title: toolbarOptions["editreceipt"]["name"],
		    imageSrc:"icons/Edit.png",
		    href:"billing/editReceipt.do?_method=getReceipt&screen=depositReceipts",
		    onclick: null,
		    description: toolbarOptions["editreceipt"]["description"]
			};

toolbar.Print={
	    title: toolbarOptions["print"]["name"],
	    imageSrc:"icons/Print.png",
	    href:'',			// will be set in changePrinter() function
	    //onclick: 'checkTheTemplate',
		target:"_blank",
	    description: toolbarOptions["print"]["description"],
	    show : (receiptPrintRights == 'A' || roleId == '1' || roleId == '2')
	   };

toolbar.AuditLog= {
		title: toolbarOptions["auditlog"]["name"],
		imageSrc: "icons/Edit.png",
		href: '/Receipt/auditlog/AuditLogSearch.do?_method=getAuditLogDetails',
		target : '_blank',
		description: toolbarOptions["auditlog"]["description"],

};


function init() {
	theForm = document.DepositReceiptSearchForm;
	initMrNoAutoComplete(cpath);
	changePrinter();
	createToolbar(toolbar);
}

function clearSearch() {
	theForm.fromDate.value = "";
	theForm.toDate.value = "";

	theForm.main_type[0].checked = true;
	enableCheckGroupAll(theForm.main_type[0]);

	theForm.status[0].checked = true;
	enableCheckGroupAll(theForm.status[0]);

	theForm.bill_type[0].checked = true;
	enableCheckGroupAll(theForm.bill_type[0]);

	theForm.visit_type[0].checked = true;
	enableCheckGroupAll(theForm.visit_type[0]);

	theForm.mr_no.value = "";
	theForm.bill_no.value = "";
}


function changePrinter() {
	if (document.printerSelectForm != null) {
	var template = document.printerSelectForm.printTemplate.value;
	var printer = document.printerSelectForm.printer.value;
	toolbar.Print.href = "billing/ReceiptPrint.do?_method=depositReceiptPrint"+
		"&printTemplate="+template + "&printerType="+printer;
	}
}

function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = theForm.mr_no;

	// complete
	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		showMessage("js.billing.depositreceiptlist.invalidmrnoformat");
		theForm.mr_no.value = ""
		theForm.mr_no.focus();
		return false;
	}
}

function doSearch() {
	if (!doValidateDateField(theForm.fromDate))
		return false;
	if (!doValidateDateField(theForm.toDate))
		return false;
	return true;
}