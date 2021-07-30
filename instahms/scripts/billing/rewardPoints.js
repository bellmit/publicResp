var toolbar = {}
	toolbar.Print ={
			title : toolbarOptions["printledger"]["name"],
			imageSrc : "icons/Print.png",
			href : "billing/RewardPointsLedger.do?_method=getReport",
			onclick : null,
			target : "_blank",
			description : toolbarOptions["printledger"]["description"],
};
	toolbar.view ={
		title : toolbarOptions["view"]["name"],
		imageSrc : "icons/Edit.png",
		href : "billing/RewardPoints.do?_method=addRewardPoints",
		onclick : null,
		target : "_blank",
		description : toolbarOptions["view"]["description"],
};
