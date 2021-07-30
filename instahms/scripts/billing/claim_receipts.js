var theForm = document.ReceiptSearchForm;

var toolbar = {
Edit: {
		title: "Edit Receipt",
		imageSrc: "icons/Edit.png",
		href: "billing/claimReceipts.do?_method=show",
		onclick: null,
		description: "View and/or Edit receipt remarks"
	  }
};

function init() {
	theForm = document.ReceiptSearchForm;
	createToolbar(toolbar);
	initArrays();
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	if (insCompObj.selectedIndex != 0)
		onChangeInsuranceCompany();

	if (tpaObj.selectedIndex != 0)
		onChangeTPA();
}