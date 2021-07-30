var psAc = null;

var toolbar = {
	PrintDocuments : {
		title: "Print Package Documents ",
		imageSrc: "icons/Print.png",
		href: 'advpackages/PatientPackages/HandedoverPackages.do?_method=downloadDocuments',
		onclick: null,
		target: "_blank",
		description: "Print Patient Documents"
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
