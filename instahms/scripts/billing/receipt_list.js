/*
 * Functions used by BillList.jsp
 */
var theForm = document.ReceiptSearchForm;

var toolbar = {}
toolbar.Edit= {
		title: toolbarOptions["editbill"]["name"],
		imageSrc: "icons/Edit.png",
		href: "billing/BillAction.do?_method=getCreditBillingCollectScreen",
		onclick: null,
		description: toolbarOptions["editbill"]["description"]
	  };

toolbar.EditReceipt={
			title: toolbarOptions["editreceipt"]["name"],
		    imageSrc:"icons/Edit.png",
		    href:"billing/editReceipt.do?_method=getReceipt&screen=billReceipts",
		    onclick: null,
		    description: toolbarOptions["editreceipt"]["description"]
			};

toolbar.Print= {
	    title:toolbarOptions["print"]["name"],
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

function checkTheTemplate(ancor, params) {
	var tempType = params.type;
	var selectedTemp = document.printerSelectForm.printTemplate.value;
	var id = document.printerSelectForm.printTemplate;
	var seletedTempText = id.options[id.selectedIndex].text;
	var depVal = seletedTempText.indexOf('(Deposit)');
	var recVal = seletedTempText.indexOf('(Receipt/Refund)');

	if (selectedTemp != 'BUILTIN_HTML' && selectedTemp != 'BUILTIN_TEXT') {
		if (tempType == 'DR' || tempType == 'DF') {
			if (depositTempList != null) {
				for (var i=0; i<depositTempList.length; i++) {
					if (selectedTemp == depositTempList[i].template_name && depVal >= 0 ){
						return true;
					}
				}
			}
			showMessage("js.billing.receiptlist.selecttemplate.typedeposit");
			return false;
		} else {
			if (receiptsTempList != null) {
				for (var i=0; i<receiptsTempList.length; i++) {
					if (selectedTemp == receiptsTempList[i].template_name && depVal == -1) {
						return true;
					}
				}
			}
			showMessage("js.billing.receiptlist.selecttemplate.typereceipt.refund");
			return false;
		}
	}
}

function init() {
	theForm = document.ReceiptSearchForm;
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

/*
 * Complete the MRNO
 */
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
		showMessage("js.billing.receiptlist.invalidmrnoformat");
		theForm.mr_no.value = ""
		theForm.mr_no.focus();
		return false;
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
		showMessage("js.billing.receiptlist.invalidbillnoformat");
		theForm.bill_no.value = ""
		theForm.bill_no.focus();
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

function changePrinter() {
	if (document.printerSelectForm != null) {
	var template = document.printerSelectForm.printTemplate.value;
	var printer = document.printerSelectForm.printer.value;
	toolbar.Print.href = "billing/ReceiptPrint.do?_method=receiptPrint"+
		"&printTemplate="+template + "&printerType="+printer;
	}
}

