var toolbar = {}

	toolbar.Edit= {
		title: toolbarOptions["viewedit"]["name"],
		imageSrc: "icons/Edit.png",
		href: '/pages/stores/stockcheckpoint.do?_method=getChkpointDetailsScreen',
		onclick: null,
		description: toolbarOptions["viewedit"]["description"]
	};

	toolbar.Report= {
		title: toolbarOptions["delete"]["name"],
		imageSrc: "icons/Delete.png",
		href: '/pages/stores/stockcheckpoint.do?_method=deleteChkpointDetails',
		onclick: null,
		description: toolbarOptions["delete"]["description"]
};
var theForm = document.chkptSearchForm;

function init() {
	/*
	 * NOTE: call createToolbar to initialize the toolbar.
	 */
	createToolbar(toolbar);
}