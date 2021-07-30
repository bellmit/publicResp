var autoComp = null;
var _referaldoctorNameId = null;
var _referalDoctorId = null;

function refDocAutoComplete(cpath, referaldoctorNameId, referalDoctorId, container, url) {
	if (!empty(autoComp)) {
		autoComp.destroy();
		autoComp = null;
	}
	_referalDoctorId=referalDoctorId;
	_referaldoctorNameId=referaldoctorNameId;
	var refDocSearchQuery = document.getElementById(_referaldoctorNameId).value;
	var dataSource = new YAHOO.util.XHRDataSource(cpath + url);
	dataSource.scriptQueryAppend = "_method=searchReferralDoctors";
	dataSource.responseType=YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : "result",
		fields: [
			{key: "ref_name"},
			{key: "ref_id"},
			{key: "ref_mobile"},
			{key: "ref_type"},
			{key: "clinician_id"}
		],
		numMatchFields: 2
	};

	autoComp = new YAHOO.widget.AutoComplete(_referaldoctorNameId, container, dataSource);
	
	autoComp.formatResult = Insta.autoHighlight;
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = false;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.queryMatchContains = true;
	autoComp.minQueryLength = 1;
	autoComp.maxResultsDisplayed = 20;
	autoComp.forceSelection = true;
	autoComp._bItemSelected = true;

	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		return highlightedValue;
	}

	autoComp.textboxBlurEvent.subscribe(function() {
		var referralName = YAHOO.util.Dom.get(_referaldoctorNameId).value;
		if(referralName == '') {
			YAHOO.util.Dom.get(_referalDoctorId).value = '';
		}
	});

	if (autoComp.forceSelection) {
		autoComp.itemSelectEvent.subscribe(selectRefDocItem);
	} else {
		autoComp.itemSelectEvent.subscribe(selectRefDocItem);
	}
	return autoComp;

}

function selectRefDocItem(sType, oArgs) {
	var record = oArgs[2];
	var ref_name = record[0];
	var ref_id = record[1];
	document.getElementById(_referaldoctorNameId).value = ref_name;
	document.getElementById(_referalDoctorId).value = ref_id;
}

function clearRefDocItem(oSelf) {
	document.getElementById(_referaldoctorNameId).value='';
	document.getElementById(_referalDoctorId).value='';
}