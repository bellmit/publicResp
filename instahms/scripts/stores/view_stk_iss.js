
var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["issueprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'pages/stores/viewstockissues.do?_method=getPrint&report=StoreStockUserIssue',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["issueprint"]["description"]
};

function init() {
	var theForm = document.StkIssSearchForm;
	theForm.user_issue_no.focus();
	fillUsers('issued_to');
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}