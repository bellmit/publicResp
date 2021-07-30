var toolbar = {
	EditRole: {
		title: "Edit Role",
		imageSrc: "icons/Edit.png",
		href: '/pages/usermanager/RoleAction.do?method=getRoleScreen'
	},

	EditUser: {
		title: "Edit User",
		imageSrc: "icons/Edit.png",
		href: '/pages/usermanager/UserAction.do?method=getUserScreen'
	},

	EditStats: {
		title: "Edit Role Page Stats",
		imageSrc: "icons/Edit.png",
		href: '/pages/usermanager/PageStatsAction.do?method=getPageStatsScreen'
	},
	
	EditCounter: {
		title: "Edit Billing Counter Mapping",
		imageSrc: "icons/Edit.png",
		href: '/master/usercentercounters/show.htm?'
	}

}

function autoCompleteForRole(){
	dataSource = new YAHOO.widget.DS_JSArray(roleNameList);
	oAutoComp = new YAHOO.widget.AutoComplete('roleName', 'roleName_dropdown', dataSource);
	oAutoComp.maxResultsDisplayed = 18;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = false;
	oAutoComp.animVert = false;

	autoCompleteForUserNames();

}

function autoCompleteForUserNames(){
	dataSource = new YAHOO.widget.DS_JSArray(userNameList);
	oAutoComp = new YAHOO.widget.AutoComplete('userName', 'userName_dropdown', dataSource);
	oAutoComp.maxResultsDisplayed = 18;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = false;
	oAutoComp.animVert = false;

	createToolbar(toolbar);
}

function clearSearch(){
	document.forms[0].roleName.value="";
	document.forms[0].userName.value="";
}