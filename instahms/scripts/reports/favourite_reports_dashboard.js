function init(){
	createToolbar(toolbar);
	if(document.getElementById('report_group').type=='select-one')
		sortSelect(document.getElementById('report_group'));
	sortSelect(document.getElementById('parent_report_name'));
	sortSelect(document.getElementById('user_name'));
	document.inputform.action = getBase()+".do";
	gBase = getBase()+".do";
}

function getBase() {
    var base = document.getElementsByTagName('base')[0];
    if (base && base.href && (base.href.length > 0)) {
        base = base.href;
    } else {
        base = document.URL;
    }
    var action = base.substring( base.indexOf("/Reports/"),
        base.indexOf(".do"));

    action = action.substring(action.indexOf("/Reports")+9);
    return action;
};

var gBase ='';

var toolbar = {
	Edit: {
		title: "View/Edit Report Details",
		imageSrc: "icons/Report.png",
		href: '/FavouriteReportAction.do?method=getMyFavourite',
		target: '_blank',
		onclick: null,
		description: "View and/or Edit the contents of this report."
	},

	Rights: {
		title: "Edit Report Rights",
		imageSrc: "images/genericuser-icon.png",
		href: '/pages/Reports/FavouriteReportsDashboard.do?_method=editReportRights&%params',
		target: '_blank',
		onclick: null,
		description: "View and/or Edit the rights of this report."
	},

	RunReport: {
		title: "Run Report",
		imageSrc: "icons/View.png",
		href: 'pages/Reports/FavouriteReportsDashboard.do?_method=getDateRangeForFavReports&_myreport=%params',
		target: '_blank',
		onclick: null,
		description: "View the report output."
	}
};


function onDelete(){
	var canBeDeletedArray = new Array();
	var cannotBeDeletedArray = new Array();
	var canIndex = 0;
	var cannotIndex= 0;

	var disCheckElmts = document.inputform._chkbx;
	if(!disCheckElmts)
		return;
	if(disCheckElmts.type== 'checkbox'){
		var temp = new Array();
		temp.push(disCheckElmts);
		disCheckElmts=temp;
	}
	for(var i=0;i<disCheckElmts.length; i++){
		if(disCheckElmts[i].checked){
			var report_name = disCheckElmts[i].value;
			var userName = document.getElementById(report_name+"_User").value;
			if(document.getElementById("_loggedUser").value == userName || isAdmin== 'true'){
				canBeDeletedArray[canIndex++] = report_name;
			}else{
				cannotBeDeletedArray [cannotIndex++] = report_name;
				disCheckElmts[i].checked = false;
				checkifselAllChkbxChkd();
			}
		}
	}
	var canBeDeltdReports="";
	for(var i=0; i<canBeDeletedArray.length; i++){
		if(i==0) canBeDeltdReports += canBeDeletedArray[i];
		else canBeDeltdReports += "@"+canBeDeletedArray[i];
	}
	var msg = "The following report(s) cannot be deleted:\n [Only the report owner can delete them...]\n";
	if(cannotBeDeletedArray==null || cannotBeDeletedArray.length<1){
		msg="Please select a report to delete.";
	}
	for(var i=0; i<cannotBeDeletedArray.length ; i++){
		msg += ""+cannotBeDeletedArray[i]+"\n";
	}
	if(cannotBeDeletedArray.length<=0 && canBeDeltdReports!=""){
		document.inputform._method.value= "deleteFavReport";
		document.inputform._reportsToBeDeleted.value= canBeDeltdReports;
		document.inputform.action = getBase()+".do";
		document.inputform.submit();
	}else if(confirm(msg) && canBeDeltdReports!=""){
		document.inputform._method.value= "deleteFavReport";
		document.inputform._reportsToBeDeleted.value= canBeDeltdReports;
		document.inputform.action = getBase()+".do";
		document.inputform.submit();
	}else{
		// do nothing
	}
}


function onToggleFrequentlyViewed(){
	var disCheckElmts = document.inputform._chkbx;
	if(!disCheckElmts)
		return;
	var canBeUpdtdReports ="";
	if(disCheckElmts.type== 'checkbox'){
		var temp = new Array();
		temp.push(disCheckElmts);
		disCheckElmts=temp;
	}
	for(var i=0;i<disCheckElmts.length; i++){
		if(disCheckElmts[i].checked){
			if(canBeUpdtdReports =="") canBeUpdtdReports += disCheckElmts[i].value;
			else canBeUpdtdReports += "@"+disCheckElmts[i].value;
		}
	}
	if(canBeUpdtdReports!=""){
		document.inputform._method.value= "markFavReportAsFreq";
		document.inputform._reportsToBeMarkdFreq.value= canBeUpdtdReports;
		document.inputform.action = getBase()+".do";
		document.inputform.submit();
	}else{
		// do nothing
	}
}


function getName(name){
	return name.toString();
}

// sethrefs from toolbar.js has been overridden
var setHrefsOld = function(params, id, enableList, toolbarKey) {
	if (empty(gToolbars[toolbarKey])) return ;

	var i=0;
	var toolbar = gToolbars[toolbarKey];
	for (var key in toolbar) {
		var data = toolbar[key];
		var anchor = document.getElementById('toolbarAction' + toolbarKey + key);
		var href = data.href;

		if (!empty(anchor)) {
			// append the params
			if (!empty(href)) {
				for (var paramname in params) {
					var paramvalue = params[paramname];
					if(paramname == 'aR'){
						if(href == 'edit')
							href = document.getElementById(paramvalue+(parseInt(id)+1).toString()).value;
						else if(href == 'run')
							href = document.getElementById(paramvalue+'T'+(parseInt(id)+1).toString()).value;
					}else if(paramname == 'report_title'){
						if(href== 'delete'){
							href = "javascript:deleteTRow('"+paramvalue+"');" ;
						}else if(href =='frequent'){
							href = "javascript:markAsFreq('"+paramvalue+"');";
						}
					}
				}
				anchor.href =  href;
			}

			var enable = true;
			if (enableList) {
				enableToolbarItem(key, enableList[i], toolbarKey);
				enable = enableList[i];
			} else {
				enableToolbarItem(key, enable, toolbarKey);
			}

			if (!empty(data.onclick) && enable) {
				setParams(anchor, params, id, toolbar);
			}

		} else {
			debug("No anchor for " + 'toolbarAction'+ toolbarKey + key + ":");
		}

		i++;
	}
}

function checkifselAllChkbxChkd(){
		if(document.getElementById("selAllChkbx").checked)
			document.getElementById("selAllChkbx").checked= false;
}

function selectAllForDiscounts(){
 	var disCheckElmts = document.inputform._chkbx;
 	if(disCheckElmts && disCheckElmts.type!= 'checkbox'){
		if (document.getElementById("selAllChkbx").checked)	{
			for(var i=0;i<disCheckElmts.length;i++){
				disCheckElmts[i].checked=true;
				document.getElementById("toolbarRow"+i).setAttribute("class","rowbgToolBar");
			}
		} else {
			for(var i=0;i<disCheckElmts.length;i++){
				disCheckElmts[i].checked=false;
				document.getElementById("toolbarRow"+i).removeAttribute("class");
			}
		}
	}else if(disCheckElmts && disCheckElmts.type== 'checkbox'){
		if (document.getElementById("selAllChkbx").checked)	{
			document.inputform._chkbx.checked = true;
			document.getElementById("toolbarRow"+0).setAttribute("class","rowbgToolBar");
		}else{
			document.inputform._chkbx.checked = false;
			document.getElementById("toolbarRow"+0).removeAttribute("class");
		}

	}
}


function sortSelect(obj) {
	var o = new Array();
	if (!hasOptions(obj)) {
		return;
	}
	for (var i = 0; i < obj.options.length; i++) {
		o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
		(o[i]).title = obj.options[i].title;
		(o[i]).value = obj.options[i].value;

	}
	if (o.length == 0) {
		return;
	}
	o = o.sort(function(val1, val2) {
		if(val2.text+"" == "(Summary Fields)")
			return -1;

		if ((val1.text + "") < (val2.text + "")) {
			return -1;
		}
		if ((val1.text + "") > (val2.text + "")) {
			return 1;
		}
		return 0;
	});

	obj.options.length = 0;
	for (var i = 0; i < o.length; i++) {
		obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
		obj.options[i].title = o[i].title;
		obj.options[i].value = o[i].value;
	}
}

function hasOptions(obj) {
	if (obj != null && obj.options != null) {
		return true;
	}
	return false;
}

