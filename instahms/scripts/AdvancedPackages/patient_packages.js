var psAc = null;

var toolbar = {
	PatientPackageDetails : {
		title: "Patient Package Details ",
		imageSrc: "icons/Edit.png",
		href: 'advpackages/PatientPackages.do?_method=getPatientPackageDetails',
		onclick: null,
		description: "View Patient Package Details"
	},
	PrintDocuments : {
		title: "Print Package Documents ",
		imageSrc: "icons/Print.png",
		href: 'advpackages/PatientPackages.do?_method=downloadDocuments',
		onclick: null,
		target: '_blank',
		description: "Print Patient Documents"
	},
	Handover : {
		title: "Handover To Patient",
		imageSrc: "icons/HandOver.png",
		href: 'advpackages/PatientPackages.do?_method=getPatientPackageHandoverScreen',
		onclick: null,
		description: "Handover Patient Package"
	}
};
function init(){
	psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
	createToolbar(toolbar);
}

function changeStatus() {
 	var status = '';

 	if (document.getElementById('_mr_no').checked) {
		status = 'active';
	} else {
		status = 'all';
	}
	if (status == 'active') {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	} else {
		psAc.destroy();
		psAc = Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', status, null, null);
	}
 }

 function checkOrUncheckAll(obj) {
	var checkBox = document.getElementsByName('_prescription_id');
	var packageStatus = document.getElementsByName("_package_status");
	var anyChecked = false;

	for (var i=0; i<checkBox.length; i++) {
		if (!checkBox[i].disabled) {

			if ( !checkPackageStatus(packageStatus[i].value) ) break;
			checkBox[i].checked = obj.checked;
			anyChecked = true;
			if (document.getElementById('toolbarRow'+i)) {
				if (checkBox[i].checked) {
					document.getElementById('toolbarRow'+i).className = "rowbgToolbar";
				} else {
					document.getElementById('toolbarRow'+i).className = "";
				}
			}
		}
	}

	if (!anyChecked) {
		alert ("Select one or more packages for completion");
		return false;
	}

	return true;
	}


function checkPackageStatus(packageStatus){
	if ( packageStatus != 'C' ) {
		alert("Package have components that are not completed. Cannot complete");
		return false;
	}
	return true;
}

function complete(){
	document.patpackageform._method.value = 'completePackage';
	document.patpackageform.submit();
}