var toolbar = {}
	toolbar.SaleReport= {
		title: toolbarOptions["billcollection"]["name"],
		imageSrc: "icons/Edit.png",
		href: 'pages/stores/pendingSalesBill.do?_method=getDetailSaleList',
		onclick: null,
		description: toolbarOptions["billcollection"]["description"]

};
var theForm = document.pendingSalesSearchForm;

function init() {
	theForm = document.pendingSalesSearchForm;
	initMrNoAutoComplete(cpath);
	theForm.mr_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}

function filterPaymentModes1() {
	var numPayments = getNumOfPayments();
	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;
		
		$("#"+paymentModeId+" option[value='-8']").remove();
		$("#"+paymentModeId+" option[value='-6']").remove();
		$("#"+paymentModeId+" option[value='-7']").remove();
		$("#"+paymentModeId+" option[value='-9']").remove();
	}
}