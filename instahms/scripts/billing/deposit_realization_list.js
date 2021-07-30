
function init() {
	initMrNoAutoComplete(cpath);
	initUserNameAutoComplete();
}

function initUserNameAutoComplete() {
	YAHOO.example.ACJSAddArray = new function() {
		var dataSource = new YAHOO.widget.DS_JSArray(userNameList);
		oAutoComp = new YAHOO.widget.AutoComplete('username', 'userName_dropdown', dataSource);
		oAutoComp.maxResultsDisplayed = 20;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = true;
	}
}

function doSearch() {
	document.depositRealizationForm.method.value = "getDepositRealizationScreen";
	document.depositRealizationForm.submit();
}

function realize() {
	if(!checkRealized()){
		showMessage("js.billing.depositrealization.checkdeposit.realize");
		return false;
	}
	document.depositRealizationForm._method.value = "saveRealized";
	document.depositRealizationForm.submit();
}

function selectOrUnselectAll() {
	var check = document.forms[0].realizeAll.checked;
	var realizeDepositElmts = document.getElementsByName("_realizeDeposit");
	for(var i=0;i<realizeDepositElmts.length;i++) {
		realizeDepositElmts[i].checked = check;
	}
}

function checkRealized() {
	var realizeDepositElmts = document.getElementsByName("_realizeDeposit");
	for(var i=0;i<realizeDepositElmts.length;i++) {
		if(realizeDepositElmts[i].checked) return true;
	}
	return false;
}
