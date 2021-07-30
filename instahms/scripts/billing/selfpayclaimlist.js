var csform = null;

function init() {
if(batchType == 'PR'){
	toolbar.EClaim.title = "Generate Person Register";
	toolbar.ClaimSent.title = "Sent Person Register";
	toolbar.Delete.title = "Delete";
	toolbar.DownloadEclaim.title = "Download Person Register";
	toolbar.Download.title = "View Errors";
	toolbar.Upload.title = "Upload Person Register";
	
	toolbar.EClaim.description = "Generate Person Register E-Claim";
	toolbar.ClaimSent.description = "Sent Person Register Submission";
	toolbar.Delete.description = "Delete Person Register submission";
	toolbar.DownloadEclaim.description = "Download Person Register EClaim XML";
	toolbar.Download.description = "Download Error Logs";
	toolbar.Upload.description = "Upload Person Register E-Claim";
	createToolbar(toolbar);
	}
	else{
	createToolbar(toolbar);
	}
}

var toolbar = {
	EClaim: {
		title: "Generate E-Claim",
		imageSrc: "icons/Run.png",
		href: '/billing/selfpay/generate.htm?',
		onclick: null,
		description: "Generate E-Claim",
		show: (eClaimModule == 'Y' && haadClaimRights == 'A')
	},
	ClaimSent: {
		title: "Sent Claim",
		imageSrc: "icons/Send.png",
		href: '/billing/selfpay/markAsSent.htm?',
		onclick: null,
		description: "Sent Claim Submission"
	},
	Delete: {
		title: "Delete",
		imageSrc: "icons/Delete.png",
		href: '/billing/selfpay/delete.htm?',
		onclick: null,
		description: "Delete submission"
	},
	DownloadEclaim: {
		title: "Download E-Claim",
		imageSrc: "images/arrow_down.png",
		href: '/billing/selfpay/downloadXML.htm?',
		onclick: null,
		target: "_blank",
		description: "Download EClaim XML"
	},
	Download: {
		title: "View Errors",
		imageSrc: "images/arrow_down.png",
		href: '/billing/selfpay/downloadError.htm?',
		target: "_blank",
		onclick: null,
		description: "Download Error Logs"
	},
	Upload: {
		title: "Upload E-Claim",
		imageSrc: "icons/Run.png",
		href: '/billing/selfpay/uploadClaim.htm?',
		target: "_blank",
		onclick: null,
		description: "Upload E-Claim"
	},
};

function doSearch() {
	return true;
}
