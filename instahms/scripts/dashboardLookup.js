
function showMore(divMoreId) {

	var saveSearch = document.getElementById('_save_search');
	var divMore = document.getElementById(divMoreId);

	if (divMore.style.display == 'none') {
		// show it
		show(divMore, true);
		document.getElementById('aMore').innerHTML = getString("js.common.search.less.options");
		if (saveSearch) {
			show(saveSearch, true);
		}
	} else {
		// hide it
		show(divMore, false);
		document.getElementById('aMore').innerHTML = getString("js.common.search.more.options");
		if (saveSearch) {
			show(saveSearch, false);
			showSaveInputs(false);
		}
	}

}

function showSaveInputs(doShow) {
	show(document.getElementById('_save_inputs'), doShow);
}

function toggleShow(obj) {
	obj.style.display = obj.style.display == 'block' ? 'none' : 'block'
}

function show(obj, doShow) {
	if ((doShow == null) || doShow)
		obj.style.display = 'block';
	else
		obj.style.display = 'none';
}

function saveSearch(form) {
	var mysearches = form._mysearch;
	var search_name = form._search_name.value;
	if (empty(search_name))  {
		showMessage("js.common.message.search.name.prompt");
		document.getElementById('_search_name').focus();
		return false;
	}
	for (var i=0; i<mysearches.options.length; i++) {
		var savedsearch = mysearches.options[i].text;
		if (trim(savedsearch.toUpperCase()) == trim(search_name.toUpperCase())) {
			showMessage("js.common.message.search.name.duplicate");
			return false;
		}
	}
	form.action = cpath + "/master/SavedSearches/saveSearch.htm";
	form.submit();
}

function onSearchChange(value, form) {
	if (value == 'nosearch') {
		return false;
	}
	form.action = cpath + "/master/SavedSearches/getMySearch.htm";
	form.submit();
	return true;
}

function isFilterActive(form) {
	var Els = form.elements;
	for (var i in Els) {
		var el = Els[i];
		var type = el.type ? (el.type.toString()).toLowerCase() : '';
		if (type == 'text' || type == 'textarea') {
			if (el.value != '')
				return true;
		}
		if ((type == 'checkbox') && (el.getAttribute("isallcheckbox") == null) && el.checked) {
			debug("checkbox " + el.isallcheckbox + " " + el.checked);
			return true;
		}
		if ((type == 'radio') && el.checked)
			return true;
		if (type == 'select-one' && el.options.selectedIndex > 0)
			return true;
		if (type =='select-multiple' && el.options.selectedIndex >= 0)
			return true;
	}
	debug("Filter is not active");
	return false;
}

function showFilterActive(form) {
	if (isFilterActive(form)) {
		show(document.getElementById('_filters_active'), true);
	} else {
		show(document.getElementById('_filters_active'), false);
	}
}

function initMrNoAutoComplete(cpath) {
	Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
}

function initLocalAutoComplete(field, dataMap, chooseFields) {
	var container = field + "_container";
	var sAutoComp = null;
	if (null != dataMap) {
		var dataList = dataMap[field];
		var datasource = new YAHOO.util.LocalDataSource({result: dataList});
		datasource.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields : chooseFields
		};
		sAutoComp = new YAHOO.widget.AutoComplete(field, container, datasource);
		sAutoComp.minQueryLength = 0;
	 	sAutoComp.maxResultsDisplayed = 20;
	 	sAutoComp.forceSelection = false ;
	 	sAutoComp.animVert = false;
	 	sAutoComp.resultTypeList = false;
	 	sAutoComp.typeAhead = false;
	 	sAutoComp.allowBroserAutocomplete = false;
	 	sAutoComp.prehighlightClassname = "yui-ac-prehighlight";
		sAutoComp.autoHighlight = true;
		sAutoComp.useShadow = false;
	 	if (sAutoComp._elTextbox.value != '') {
				sAutoComp._bItemSelected = true;
				sAutoComp._sInitInputValue = sAutoComp._elTextbox.value;
		}
	}
 	return sAutoComp;
}

