var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/pages/masters/insta/stores/ManufacturerDetails.do?_method=getManfDetailsScreen',
		onclick: null,
		description: "View/Edit Manufacturer Details"
	},

};

var theForm = document.manfListSearchForm;

function init() {
	theForm = document.manfListSearchForm;
	theForm.manf_name.focus();
	createToolbar(toolbar);
}