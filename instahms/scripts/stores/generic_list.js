var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/StoresMastergendetails.do?_method=getGenericDetailsScreen',
		onclick: null,
		description: "View/Edit Generic Details"
	},

};

var theForm = document.genListSearchForm;

function init() {
	theForm = document.genListSearchForm;
	theForm.generic_name.focus();
	createToolbar(toolbar);
}