
var toolbar = {}
	toolbar.Report= {
		title: toolbarOptions["issueprint"]["name"],
		imageSrc: "icons/Report.png",
		href: 'stores/StockPatientIssuePrint.do?_method=printPatientIssues',
		target: '_blank',
		onclick: null,
		description: toolbarOptions["issueprint"]["description"]
};

function initForPatient() {
	var theForm = document.PatIssSearchForm;
	initMrNoAutoComplete(cpath);
	theForm.mr_no.focus();
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}