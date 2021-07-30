var spform = null;
function init() {
	spform = document.sponsorBillForm;
	createToolbar(toolbar);
	showFilterActive(document.sponsorBillForm);
}

function changeSponsorType(type) {
	if(type == 'H') {
		if(document.getElementById('hospital').value != '') {
			document.getElementById('tpa').options.selectedIndex=0;
			spform.sponsor_type.value = 'H';
		}else {
			spform.sponsor_type.value = '';
		}
	}
	if(type == 'S') {
		if(document.getElementById('tpa').value != '') {
			document.getElementById('hospital').options.selectedIndex=0;
			spform.sponsor_type.value = 'S';
		}else {
			spform.sponsor_type.value = '';
		}
	}
}

var toolbar = {
	Add: {
		title: "Add bills",
		imageSrc: "icons/Edit.png",
		href: 'pages/BillDischarge/addSponsorBill.do?_method=addBills',
		onclick: null,
		description: "Add more sponsor bills"
	},
	Edit: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: 'pages/BillDischarge/editSponsorBill.do?_method=show',
		onclick: null,
		description: "Edit Sponsor bill details"
	},
	View: {
		title: "Receive/Allocate",
		imageSrc: "icons/Collect.png",
		href: 'pages/BillDischarge/allocateSponsorBill.do?_method=view',
		onclick: null,
		description: "View Sponsor bill details"
	}
};
