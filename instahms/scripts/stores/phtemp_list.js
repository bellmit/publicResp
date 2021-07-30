var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/pages/masters/insta/stores/tempdetails.do?_method=getTemplateDetailsScreen',
		onclick: null,
		description: "View/Edit Template Details"
	},

};

var theForm = document.tempListSearchForm;

function init() {
	theForm = document.tempListSearchForm;
	theForm.template_name.focus();
	createToolbar(toolbar);
}