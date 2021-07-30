var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["debitprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=generatePrintForItemReturnNote',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["debitprint"]["description"]
	};

	toolbar.Replace= {
		title: toolbarOptions["viewedit"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'stores/StoresSupplierReturnslist.do?_method=editSupplierReturnDebit',
		onclick: null,
		description: toolbarOptions["viewedit"]["description"]
};
var theForm = document.PhSuppRetDebitSearchForm;

function init() {
	theForm = document.PhSuppRetDebitSearchForm;
	setMultipleSelectedIndexs(theForm.supplier_id,suppArray);
	theForm.debit_note_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}
function onKeyPressDebitno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeDebitno();
	} else {
		return true;
	}
}

function onChangeDebitno() {
	var debitNoBox = theForm.debit_note_no;

	// complete
	var valid = addPrefix(debitNoBox, 'DB', 4);

	if (!valid) {
		showMessage("js.stores.procurement.invaliddebitnoformat");
		theForm.debit_note_no.value = ""
		theForm.debit_note_no.focus();
		return false;
	}
}