/*
*   Attention: make sure your changes "Indented Properly" when modifying.
*/


/**
* No of AutoComplete controls created.
*/
Insta.AutoComplete = [];

/**
* array of YUI Datasources.
*/
Insta.AutoComplete.DataSource = [];
var _generic_combo=null;

function _Insta_checkForAutoComplete() {

	var autocomplete_widget = YAHOO.util.Dom.getElementsByClassName("autocomplete_widget", "div");
	for (var i=0; i<autocomplete_widget.length; i++) {
		// debug("inside for loop");
		var inputEl =  YAHOO.util.Dom.getFirstChild(autocomplete_widget[i]);
		var containerEl = YAHOO.util.Dom.getLastChild(autocomplete_widget[i]);
		var inputElId = inputEl.id;
		var containerElId = containerEl.id;
		eval("var _generic_Details="+inputEl.getAttribute("jsvar"));

		_Init_DataSource(inputEl.getAttribute("common_datasource"), inputElId,
		 _generic_Details);

		_Init_Insta_AutoComplete(inputElId, containerElId, null, true);
	}
}

function _Insta_checkForAutoComplete_combo() {
	var autocomplete_widget = YAHOO.util.Dom.getElementsByClassName("autocomplete_combo_widget", "div");
	var jsvar = [];
	for (var i=0; i<autocomplete_widget.length; i++) {
		var inputEl =  YAHOO.util.Dom.getFirstChild(autocomplete_widget[i]);
		var containerEl = YAHOO.util.Dom.getLastChild(autocomplete_widget[i]);
		var inputElId = inputEl.id;
		var containerElId = containerEl.id;
		var couterfrows=0;
		eval("_generic_combo = "+inputEl.getAttribute("jsvar"));
		var jsArray= _generic_combo;
		for(var k=0;k<jsArray.length;k++) {
			if(jsArray[couterfrows]==undefined)	break;

			jsvar[k]=jsArray[couterfrows];
			couterfrows=couterfrows+2;
		}

		_Init_DataSource(inputEl.getAttribute("common_datasource"), inputElId, jsvar);
		_Init_Insta_AutoComplete_combo(inputElId);
	}
   }

function _Init_Insta_AutoComplete(inputElId) {
	var auto = Insta.AutoComplete["autocomplete_"+inputElId]
			 = new YAHOO.widget.AutoComplete(inputElId, "container_"+inputElId,
						Insta.AutoComplete.DataSource["datasource_"+inputElId]);

		// alert("autocomp"+Insta.AutoComplete.DataSource["datasource_"+inputElId]);
		auto.prehighlightClassName = "yui-ac-prehighlight";
	    auto.useShadow = true;
	    auto.typeAhead = true;
	    auto.maxResultsDisplayed = 15;
		auto.minQueryLength = 0;

}

function _Init_Insta_AutoComplete_combo(inputElId) {
	var auto = Insta.AutoComplete["autocomplete_"+inputElId]
				= new YAHOO.widget.AutoComplete(inputElId, "container_"+inputElId,
	         		Insta.AutoComplete.DataSource["datasource_"+inputElId]);

		auto.prehighlightClassName = "yui-ac-prehighlight";
		auto.useShadow = true;
		auto.typeAhead = true;
		auto.forceSelection = true;
		auto.maxResultsDisplayed = 15;
		auto.minQueryLength = 0;
		auto.itemSelectEvent.subscribe(function (oSelf , elItem , oData) {
		      _on_Selected_Item(elItem[2],inputElId)
		});

}

function _Init_DataSource(common, inputElId,  jsVar) {

	// debug("inside for datasource");
	var datasource = "datasource_"+inputElId;
	if (common) {
		if (_getDataSource(datasource) == null) {
			Insta.AutoComplete.DataSource[datasource] = new YAHOO.widget.DS_JSArray(jsVar);
		}
	} else {
		Insta.AutoComplete.DataSource[datasource] = new YAHOO.widget.DS_JSArray(jsVar);
	}
}

function _getDataSource(datasource) {
	try {
		if (Insta.AutoComplete.DataSource[datasource]) {
			return Insta.AutoComplete.DataSource[datasource];
		}
	} catch (e){
	}
	return null;
}

function _on_Selected_Item(oData,instance){
	var _id="";
	for(var i=0;i<_generic_combo.length;i++){
		if(oData==_generic_combo[i]){
			i=i+1;
			_id=_generic_combo[i];
			document.getElementById("autocomplete_hiidden_"+instance).value=_id;
			break;
		}
	}
}

function _AutoComplete_JS_Call(inputElId, inputElName, targetEl, jsVar, commonDatasource) {

	var autoDiv = document.createElement("div");
	var inputEl = document.createElement("input");
	var containerDiv = document.createElement("div");
	var hiddenInputEl = document.createElement("input");

	autoDiv.setAttribute("id", "autocomplete_"+inputElId);
	autoDiv.setAttribute("class", "autocomplete_"+widget);

	containerDiv.setAttribute("id", "container_"+inputElId);

	inputEl.setAttribute("type", "text");
	inputEl.setAttribute("id", inputElId);

	hiddenInputEl.setAttribute("type", "hidden");
	hiddenInputEl.setAttribute("id", "hidden_"+inputElId);

	if (inputElName != null) {
		inputEl.setAttribute("name", inputElName);
		hiddenInputEl.setAttribute("name", inputElName);
	}
	autoDiv.appendChild(inputEl);
	autoDiv.appendChild(containerDiv);
	targetEl.appendChild(autoDiv);
	targetEl.appendChild(hiddenInputEl);

	if (commonDatasource == null || commonDatasource == "undefined" || commonDatasource == "") {
		commonDatasource = true;
	}
	_Init_DataSource(commonDatasource, inputElId, jsVar);
	_Init_Insta_AutoComplte(inputElId);

}

function matchWordBeginnings(query, value) {

	// split the query as well as the value and compare each component
	var valueWords = value.split(/[\s,.-]+/);
	var queryWords = query.split(/ +/);

	for (var q=0; q<queryWords.length; q++) {
		// ALL of the query words must be found in ANY of the value words
		var anyValueMatch = false;
		var queryWord = queryWords[q];
		for (var v=0; v<valueWords.length; v++) {
			if (0 == valueWords[v].indexOf(queryWord)) {
				// any one value matching the query is enough
				anyValueMatch = true;
				break;
			}
		}
		if (!anyValueMatch) {
			// this query word was not found, so no match
			return false;
		}
	}
	// all query words were found: return true
	return true;
}

/*
 * Simple function for highlighting the found string in a query. Useful for
 * simple match highlights where a begins or a contains match is used.
 */
Insta.autoHighlight = function(oResultData, sQuery) {

	var query = sQuery.toLowerCase();
	var name = "";
	if (this.resultTypeList)
		// oResultData is of type array
		name = oResultData[0];
 	else {
 		if (YAHOO.lang.isString(oResultData)) {
			name = oResultData;
		} else {
			// oResultData is of type Object
			var oDS = this.dataSource;
			var key = oDS.responseSchema.fields[0].key;
			name = oResultData[key];
		}
	}

	if (empty(name)) return "Empty Row found(please correct the data)";
	var nameMatchIndex = name.toLowerCase().indexOf(query);
	var displayName;

	if (nameMatchIndex > -1) {
		displayName = name.substring(0, nameMatchIndex) + "<b>" +
			name.substr(nameMatchIndex, query.length) + "</b>" +
			name.substring(nameMatchIndex + query.length);
	} else {
		displayName = displayName;
	}
	return displayName;
}

function empty(obj) {
	if (obj == null || obj == undefined || obj == '' || obj == 'undefined')
		return true;
	else return false;
}

/*
 * Another function for highlighting the found string in a query, but this time it does
 * a word beginnings match, suitable for use with queryMatchContainsWordBegining.
 */
Insta.autoHighlightWordBeginnings = function(oResultData, sQuery) {

	var query = sQuery.toLowerCase();
	var value = null;
	if (this.resultTypeList) {
		// oResultData is of type array
		value = oResultData[0];

	} else if(YAHOO.lang.isString(oResultData)) {
		// oResultData is of type Object
		value = oResultData;

	} else if (this.dataSource.responseSchema.fields) {
		var oDS = this.dataSource;
		var numMatchFields = (oDS.responseSchema.numMatchFields || 1);
		for (var f=0; f<numMatchFields; f++) {
			var key = oDS.responseSchema.fields[f].key;
			var val = oResultData[key];
			if (val == null || val == '')
				continue;
			if (value == null)
				value = val;
			else
				value = value + ' [' + val + ']';
		}
	}

	if (empty(value))
		return 'Empty Row Found (Please correct the data)';
	var origValue = value;

	var queryWords = query.split(/ +/);
	for (var q=0; q<queryWords.length; q++) {
		var queryWord = queryWords[q];
		// look for a delimiter followed by a query word. Delimiter can be beginning of string (^)
		// or space, or [ (for codes). Group the queryword since we will replace it
		var regex = new RegExp("(^|[\\s[])" + "(" + Insta.escape(queryWord) + ")", 'ig');
		// Now, $1 will be the first match (delimiter) and $2 will be the query matched word
		value = value.replace(regex, "$1<b>$2</b>");

	}
	if (origValue == value) {
		// nothing changed, maybe we need to fallback to full query beginsWith match
		var regex = new RegExp(query, 'ig');
		value = value.replace(regex, "<b>$&</b>");
	}
	return value;
}

Insta.queryMatchWordStartsWith = function(sQuery, oFullResponse, oParsedResponse, oCallback) {
	//filter the list based on filtervalue specified in responseSchema
	var dateSource = this.dataSource;
    var mainFilterValue  = this.responseSchema.mainFilterValue;
    var subFilterValue  = this.responseSchema.subFilterValue;
	if(mainFilterValue != undefined){
		var mainFilterColumn = this.responseSchema.mainFilterCol;
		var subFilterColumn = this.responseSchema.subFilterCol;
		if(mainFilterValue == ''){
			//do nothing
		}else if(mainFilterValue != '' && subFilterValue == ''){
			oParsedResponse.results = filterList(oParsedResponse.results, mainFilterColumn, mainFilterValue);
		}else{
			oParsedResponse.results = filterList(oParsedResponse.results, subFilterColumn, subFilterValue);
		}
	}

    // If AC has passed a query string value back to itself, grab it
    if(oCallback && oCallback.argument && oCallback.argument.query) {
        sQuery = oCallback.argument.query;
    }

    // Only if a query string is available to match against
    if (sQuery && sQuery !== "") {
        // First make a copy of the oParseResponse
        oParsedResponse = YAHOO.widget.AutoComplete._cloneObject(oParsedResponse);

        var oAC = oCallback.scope,
            oDS = this,
            allResults = oParsedResponse.results, // the array of results
            startMatchResults = [], // container for filtered results,
			wordMatchResults = [],
            nMax = oAC.maxResultsDisplayed, // max to find
            bMatchCase = (oDS.queryMatchCase || oAC.queryMatchCase), // backward compat
            bMatchContains = (oDS.queryMatchContains || oAC.queryMatchContains); // backward compat

        var startsWithMatchedCount = 0;

        // Loop through each result object...
        for (var i=0, len=allResults.length; i<len; i++) {
        	if(mainFilterValue != undefined && mainFilterValue == 'N')
        		continue;
            var oResult = allResults[i];

            // Grab the data to match against from the result object...
            var sResult = null;

            // Result object is a simple string already
            if(YAHOO.lang.isString(oResult)) {
                sResult = oResult;
            }
            // Result object is an array of strings
            else if(YAHOO.lang.isArray(oResult)) {
                sResult = oResult[0];

            }
            // Result object is an object literal of strings
            else if (this.responseSchema.fields) {
				var numMatchFields = (this.responseSchema.numMatchFields || 1);
				for (var f=0; f<numMatchFields; f++) {
					// concatenate all the field values we are interested in matching within
					var key = this.responseSchema.fields[f].key;
					var value = oResult[key];
					if (value == null || value == '')
						continue;
					if (sResult == null)
						sResult = value;
					else
						sResult = sResult + ' ' + value;
				}
            }

            // Backwards compatibility
            else if (this.key) {
                sResult = oResult[this.key];
            }

            if (YAHOO.lang.isString(sResult)) {
				var query = decodeURIComponent(sQuery);

				/*
				 * If the query matches the beginning of the result as is, we have found a match
				 * This is the most used case, so try this first, as also unshift so that it shows
				 * up in the beginning.
				 */
				var sMatchIndex = (bMatchCase) ?
					sResult.indexOf(query) :
					sResult.toLowerCase().indexOf(query.toLowerCase());

				if (sMatchIndex == 0) {
                	startMatchResults.push(oResult);
                	startsWithMatchedCount++;

				} else {
					/*
					* Else, try any query component matches beginning of any word in result kind of match.
					* Split the result string as well as the query on space only (see bug 17357).
					*/
					var wordsInResult = sResult.split(/ /);
					var queryComps = query.split(/ /);
					var matchCount = 0;
					for (var j=0; j<queryComps.length; j++) {
						for (var k=0; k<wordsInResult.length; k++) {
							var sKeyIndex = (bMatchCase) ?
								wordsInResult[k].indexOf(queryComps[j]) :
								wordsInResult[k].toLowerCase().indexOf(queryComps[j].toLowerCase());
							if (sKeyIndex == 0) {
								matchCount++;
								break;
							}
						}
					}
					if (queryComps.length != 0 && matchCount == queryComps.length) {
						wordMatchResults.push(oResult);
					}
				}
            }

            // Filter no more if maxResultsDisplayed is reached
           if (startsWithMatchedCount === nMax) {
                break;
           }
        }
        oParsedResponse.results = startMatchResults.concat(wordMatchResults);
    }

    return oParsedResponse;
};

/**
* escapes the regular expression special characters.
*/
Insta.escape = function(text) {
    return text.replace(/[-[\]{}()*+?.,\\^$|#\s]/g, "\\$&");
}


/*
 * Searches the given string for any of the regular expressions in the given reArray,
 * and if found, returns a string that surrounds the occurrence with <b> and </b>
 * Only the first match is replaced. If no match, the original string is returned.
 */
function highlight(s, reArray) {
	if (s == null)
		return null;
	for (var i=0; i<reArray.length; i++) {
		if (s.search(reArray[i]) != -1) {
			return s.replace(reArray[i], "<b>$&</b>");
		}
	}
	return s;
}

var itemSelectCallBackStatus = {}; // which holds the select event fired status
/*
 * Initialize a Patient AutoComplete search. Example:
 *   initPatientAcSearch(contextPath, "mrnoAutocomplete", "mrnoAutoCompleteDropdown", "active", onChangeMrno,
 *   	clearMrno)
 */
Insta.initMRNoAcSearch = function(cpath, searchComp, searchDropdown, status,
		selectCallback, invalidCallback, autoSnapContainer, action, showDuplicateMrNos, isRegScreen,scheduler) {

	if (scheduler === null || scheduler === undefined){
		scheduler = false;
	}
	if (empty(isRegScreen)) isRegScreen = '';
	if (empty(showDuplicateMrNos)) showDuplicateMrNos = false;
	var ds = new YAHOO.util.XHRDataSource(cpath + "/pages/Common/PatientSearch.do");
	ds.scriptQueryAppend = "method=findPatientsJson&searchType=mrNo&status=" 
		+ status+"&showDuplicateMrNos="
		+showDuplicateMrNos
		+"&scheduler="
		+ scheduler
		+"&isRegScreen="
		+isRegScreen;

	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "mr_no"},
					{key : "patient_name"},
					{key : "salutation_name"},		//
					{key : "patient_full_name"},	//
					{key : "age_in"},				//
					{key : "status"},				//
					{key : "original_mr_no"},		//
					{key : "middle_name"},
					{key : "last_name"},
					{key : "dateofbirth"},
					{key : "expected_dob"},
					{key : "dob"},
					{key : "eob"},
					{key : "age"},
					{key : "agein"},
					{key : "patient_gender"},
					{key : "patient_phone"},
					{key : "patient_address"},
					{key : "patient_city"},
					{key : "city_name"},
					{key : "patient_area"},
					{key : "government_identifier"},
					{key : "abbreviation"},
					{key : "confidentiality_grp_id"}
				 ]
	};
	itemSelectCallBackStatus[searchComp] = false;
	var autoComp = new YAHOO.widget.AutoComplete(searchComp, searchDropdown, ds);

	autoComp.minQueryLength = 4;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 50;
	if (enableForceSelectionForMRNoSearch == 'Y' || (!empty(action) && action == 'Save'))
		autoComp.forceSelection = true;
	else
		autoComp.forceSelection = false;
	autoComp.resultTypeList = false;
	autoComp.autoSnapContainer = !autoSnapContainer;
	autoComp._bItemSelected = true;
	autoComp.queryDelay = 0.6;
	autoComp.typeAheadDelay = 0.7; // which will be used when typeAhead property set to true. it should be greater than the queryDelay

	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var patient = oResultData;
		var queryComps = sQuery.split(" ", 2);

		var reStarts = [];
		var reEnds = [];


		for (var i=0; i<queryComps.length; i++) {
			var escapedComp = Insta.escape(queryComps[i]);
			reStarts[i] = new RegExp('^' + escapedComp, 'i');
			reEnds[i] =   new RegExp(escapedComp + '$', 'i');
		}
		var details = "";
		if (trim(patient.mr_no).startsWith("contact")) {
			details = "";
		} else {
			details = highlight(patient.mr_no, reEnds);
		}

		if(patient.salutation_name!=null && patient.salutation_name!="")
			details += " " + patient.salutation_name;

		if(patient.patient_full_name!=null && patient.patient_full_name!="") {
			var fName = patient.patient_full_name.split(/\s/);
			for (var i=0; i< fName.length; i++) {
				details += " " + highlight(fName[i], reStarts);
			}
		}
		else {
			details += " " + patient.patient_name;
			if(patient.middle_name!=null)
				details += " " + patient.middle_name;
			if(patient.last_name!=null)
				details += " " + patient.last_name;
		}
		details += " " + highlight(patient.patient_phone, reStarts);
		if (!empty(patient.government_identifier))
			details += " [" + highlight(patient.government_identifier, reStarts) + "] ";

		if(patient.age_in!=null 
				&& patient.age_in!="" 
				&& patient.age_in!=undefined 
				&& patient.age!=null 
				&& patient.age!="" 
				&& patient.age!=undefined) {
			details += " <span class='additional-info'>(" +
				patient.age + patient.age_in + "/" + patient.patient_gender +
				")</span>" ;
		}
		if (patient.abbreviation != null && patient.abbreviation != "" && patient.confidentiality_grp_id != 0) {
			details +=" <span class='additional-info' " +
					" style='border: 1px solid #ccc; border-radius:0px; padding: 0px 2px 0px 2px;'>" +
					patient.abbreviation +
			"</span>" ;
		}
		return details;
	}

	/**
	 * Refer BUG : 26954
	 *------------------------------------
	 * Note: in selectCallBack function do not use the fields which r retrieved as part of this datasource.
	 * Ex:
	 *    function selectItem(sType, oArgs) {
	 *			var record = oArgs[2];
	 * 			document.getElementById('patient_id').value = record.patient_id; // this field is retrieved in datasource.
	 *    }
	 * which will not work.
	 *
	 * Reason : when calling call back function on enter key, we will not be having the same arguments.
	 *
	 */

	autoComp.itemSelectEvent.subscribe(function() {
		itemSelectCallBackStatus[searchComp] = true;
		if (!empty(selectCallback)) {
			var callback = selectCallback;
			callback();
		}
	});


	if (!empty(invalidCallback))
		autoComp.selectionEnforceEvent.subscribe(invalidCallback);

	YAHOO.util.Event.addListener(searchComp, "keypress",
		function(searchComp, status, autoComp, selectCallback, showDuplicateMrNos, isRegScreen) {
			return function(e) {
				getMrNo(e, searchComp, status, autoComp, selectCallback, showDuplicateMrNos, isRegScreen);
			};
		}(searchComp, status, autoComp, selectCallback, showDuplicateMrNos, isRegScreen), searchComp, true);

	return autoComp;
}

function getMrNo(e, searchComp, status, autoComp, selectCallback, showDuplicateMrNos, isRegScreen) {


	var query = document.getElementById(searchComp).value;
	var charCode = YAHOO.util.Event.getCharCode(e);
	if (charCode == 13) { // enter key
		if (gUse_smart_card == 'Y') {
			// here we want the request to be synchronous, so used custom code, instead of using the yui.
			// yui supports only the asynchronous requests.
			var xmlhttp = newXMLHttpRequest();
			var url = cpath + "/pages/Common/PatientSearch.do?method=getMrNo&query="+query+"&status="+status +
						"&showDuplicateMrNos="+showDuplicateMrNos+"&isRegScreen="+isRegScreen;
			xmlhttp.open("GET", url, false);
			xmlhttp.send();
			eval('var response='+xmlhttp.responseText);
			if (response.msg != '' && response.msg != 'not found') {
				// if there is any sql exception, do not submit page
				alert("Server Error. Refresh the page and try again, if still exists contact customer support. \n"+response.msg);
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
			if (response.mrNo != null) {
				document.getElementById(searchComp).value = response.mrNo;
			}
		}

		// visit id is not selected from a autocomplete container.
		if (enableForceSelectionForMRNoSearch == 'Y' && !itemSelectCallBackStatus[searchComp]) {
			if (document.getElementById(searchComp).value == '') {
				alert("Please enter the search value");
				YAHOO.util.Event.stopEvent(e);
				return false;
			} else {
				document.getElementById(searchComp).value = '';
				alert("Please select 'MR No.' from the dropdown.");
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
		}
		if (!empty(selectCallback) && !itemSelectCallBackStatus[searchComp]) {
			var callback = selectCallback;
			callback(); // calling the callback function without arguments.
		}
		itemSelectCallBackStatus[searchComp] = false;
		return true;
	}
	itemSelectCallBackStatus[searchComp] = false;
}

Insta.initVisitAcSearch = function(cpath, searchComp, searchDropdown, status,visitType ,
		selectCallback, invalidCallback, autoSnapContainer, searchType, forceSelection) {

	var ds = new YAHOO.util.XHRDataSource(cpath + '/pages/Common/PatientSearch.do');
	ds.scriptQueryAppend = "method=findPatientsJson&searchType="+searchType+"&status="+status+"&visitType="+visitType;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "patient_id"},
					{key : "mr_no"},
					{key : "reg_date"},
					{key : "salutation_name"},
					{key : "patient_full_name"},
					{key : "patient_phone"},
					{key : "age_in"},
					{key : "patient_gender"},
					{key : "doctor_name"},
					{key : "dept_name"},
					{key : "age"},
					{key : "visit_reg_date"},
					{key : "op_type"},
					{key : "error"},
					{key : "government_identifier"}
				 ]
	};

	itemSelectCallBackStatus[searchComp] = false;

	var autoComp = new YAHOO.widget.AutoComplete(searchComp, searchDropdown, ds);
	autoComp.minQueryLength = 4;
	autoComp.animVert = false;
	autoComp.maxResultsDisplayed = 50;
	if (!empty(forceSelection))
		autoComp.forceSelection = forceSelection;
	else
		autoComp.forceSelection = false;
	
	autoComp.resultTypeList = false;
	autoComp.queryDelay = 0.6;
	autoComp.typeAheadDelay = 0.7; // which will be used when typeAhead property set to true. it should be greater than the queryDelay
	autoComp.autoSnapContainer = !autoSnapContainer;

	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var patient = oResultData;
		var queryComps = sQuery.split(" ", 2);
		if (!empty(patient.error)) return "<font style='color: red'>" +patient.error + "</span>";

		var reStarts = [];
		var reEnds = [];

		for (var i=0; i<queryComps.length; i++) {
			var escapedComp = Insta.escape(queryComps[i]);
			reStarts[i] = new RegExp('^' + escapedComp, 'i');
			reEnds[i] = new RegExp(escapedComp + '$', 'i');
		}

		var details = highlight(patient.mr_no, reEnds);
		details += " " + highlight(patient.patient_id, reEnds);
		details += " " + patient.salutation_name;
		var fName = patient.patient_full_name.split(' ');
		for (var i=0; i<fName.length; i++) {
			details += " " + highlight(fName[i], reStarts);
		}
		details += " " + highlight(patient.patient_phone, reStarts);
		if (!empty(patient.government_identifier))
			details += " [" + highlight(patient.government_identifier, reStarts) + "] ";

		var regDate = patient.visit_reg_date ? formatDate(new Date(patient.visit_reg_date), 'ddmmyyyy', '-') : '';
		return details + " <span class='additional-info'>(" +
			patient.age + patient.age_in + "/" + patient.patient_gender +
			(patient.doctor_name ?  " " + patient.doctor_name : "" ) +
			(regDate ? " " + regDate : "") +
			")</span>" ;

		//return details;
	}

	/**
	 * Refer BUG : 26954
	 *------------------------------------
	 * Note: in selectCallBack function do not use the fields which r retrieved as part of this datasource.
	 * Ex:
	 *    function selectItem(sType, oArgs) {
	 *			var record = oArgs[2];
	 * 			document.getElementById('patient_id').value = record.patient_id; // this field is retrieved in datasource.
	 *    }
	 * which will not work.
	 *
	 * Reason : when calling call back function on enter key, we will not be having the same arguments.
	 *
	 */
	if (!empty(selectCallback)) {
		autoComp.itemSelectEvent.subscribe(function() {
			itemSelectCallBackStatus[searchComp] = true;
			var callback = selectCallback;
			callback();
		});
	}

	if (!empty(invalidCallback))
		autoComp.selectionEnforceEvent.subscribe(invalidCallback);

	// which is used only to reset the flag
	YAHOO.util.Event.addListener(searchDropdown, "click", function(v, oSelf) {
		var elTarget = YAHOO.util.Event.getTarget(v);
		var elTag = elTarget.nodeName.toLowerCase();
		while (elTarget && (elTag != "table")) {
			switch(elTag) {
				case "body":
					return;
				case "li":
					itemSelectCallBackStatus[searchComp] = false;
					return;
				default:
					break;
			}
			elTarget = elTarget.parentNode;
			if(elTarget) {
				elTag = elTarget.nodeName.toLowerCase();
			}
		}
	 }, autoComp);

	YAHOO.util.Event.addListener(searchComp, "keypress",
		function(searchComp, status, visitType, autoComp, selectCallback) {
			return function(e) {
				getVisitId(e, searchComp, status, visitType, autoComp, selectCallback);
			};
		}(searchComp, status, visitType, autoComp, selectCallback), searchComp, true);

	return autoComp;
}

function getVisitId(e, searchComp, status, visitType, autoComp, selectCallback) {
	var query = document.getElementById(searchComp).value;
	var charCode = YAHOO.util.Event.getCharCode(e);
	if (charCode == 9) { // tab key
		// here also we have to set the select event fired to false
		// reason is that whenever the container is open and user tabs out autocomplete fires the itemSelectEvent
		// so after itemSelectEvent we have to reset this variable.

		itemSelectCallBackStatus[searchComp] = false;
	}
	if (charCode == 13) { // enter key
		if (gUse_smart_card == 'Y') {
			// here we want the request to be synchronous, so used custom code, instead of using the yui.
			// yui supports only the asynchronous requests.
			var xmlhttp = newXMLHttpRequest();
			var url = cpath + "/pages/Common/PatientSearch.do?method=getVisitId&query="+query+"&status="+status+"&visitType="+visitType;
			xmlhttp.open("GET", url, false);
			xmlhttp.send();
			eval('var response='+xmlhttp.responseText);
			if (response.msg != '' && response.msg != 'not found') {
				// if there is any sql exception, do not submit page
				alert("Server Error. Refresh the page and try again, if still exists contact customer support. \n"+response.msg);
				YAHOO.util.Event.stopEvent(e);
				return false;
			}
			if (response.visitId != null) {
				document.getElementById(searchComp).value = response.visitId;
			}
		}

		// select call back should be called after transalating to original visit id.
		if (!empty(selectCallback) && !itemSelectCallBackStatus[searchComp]) {
			var callback = selectCallback;
			callback(); // calling the callback function without arguments.
		}
		itemSelectCallBackStatus[searchComp] = false;
		return true;
	}
}

Insta.mastersAutocomplete = function(fieldName, divContainer) {
	dataSource = new YAHOO.util.LocalDataSource({result : Insta.masterData});
	dataSource.responseSchema = {
		resultsList : 'result',
		fields : [
					{key : parameters[0]},
					{key : parameters[1]}
				 ]
	};

	var autoComplete = new YAHOO.widget.AutoComplete(fieldName, divContainer, dataSource);
	autoComplete.minQueryLength = 0;
	autoComplete.maxResultsDisplayed = 50;
	autoComplete.forceSelection = true;
	autoComplete.resultTypeList = false;
	autoComplete.typeAhead = false;
	autoComplete.useShadow = false;
	autoComplete.animVert = false;

	autoComplete.itemSelectEvent.subscribe(setIds);

	return autoComplete;
}
Insta.incomingPatientAutocomplete = function(cpath, fieldId, divContainer) {

	var ds = new YAHOO.util.XHRDataSource(cpath + '/pages/Common/PatientSearch.do');
	ds.scriptQueryAppend = "method=findIncomingPatientsJson";
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "incoming_visit_id"},
					{key : "mr_no"},
					{key : "patient_name"}
				 ],
		numMatchFields: 2
	};

	var autoComplete = new YAHOO.widget.AutoComplete(fieldId, divContainer, ds);
	autoComplete.minQueryLength = 0;
	autoComplete.maxResultsDisplayed = 50;
	autoComplete.forceSelection = true;
	autoComplete.resultTypeList = false;
	autoComplete.typeAhead = false;
	autoComplete.useShadow = false;
	autoComplete.animVert = false;

	autoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
		var patient = oResultData;
		var queryComps = sQuery.split(" ", 2);

		var reStarts = [];
		var reEnds = [];

		for (var i=0; i<queryComps.length; i++) {
			var escapedComp = Insta.escape(queryComps[i]);
			reStarts[i] = new RegExp('^' + escapedComp, 'i');
			reEnds[i] = new RegExp(escapedComp + '$', 'i');
		}

		//var details = highlight(patient.incoming_visit_id, reEnds);
		var details = (!empty(patient.mr_no) ? highlight(patient.mr_no, reEnds) : highlight(patient.incoming_visit_id, reEnds)) + " " + highlight(patient.patient_name, reEnds);

		return details;
	}

	return autoComplete;
}


function setIds(oself, elItem) {
	var record = elItem[2];
	document.getElementById(hidden_id).value = record[parameters[1]];
}
