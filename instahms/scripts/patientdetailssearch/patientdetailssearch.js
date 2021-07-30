var searchForm = null;

function init(form) {
	searchForm = form;
	setTimeout("autoCountry()", 1000);
	setTimeout("autoState()", 1000);
	autoCity();
	autoArea();
	onChangeRegField();
	showDateAndNumericInput(searchForm._customRegFieldName);
}

function initPatientToolbar(toolbarOptions) {
	// Assumes that the title and description are set appropriately in the resource properties
	// file. Does not make any checks for validity or empty values.
	// toolbarOptions : is an array of arrays
	// toolbarOptions = [
	//	[<title0>, <description0>],
	//	[<title1>, <description1>]
	// ]
	Insta.initMRNoAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
	var toolbar = {};
	toolbar.registration = {
			title : toolbarOptions["Register"]["name"],
			imageSrc : 'icons/Edit.png',
			href : '/patients/opregistration/index.htm',
			target : '_blank'
	}
	if (emr == 'A')
		toolbar.EMR = { title: toolbarOptions["EMR"]["name"], imageSrc: "icons/Edit.png", href: 'emr/EMRMainDisplay.do?_method=list', target: '_blank'};
	if (documentsList == 'A')
		toolbar.listDocuments = { title: toolbarOptions["doclist"]["name"], imageSrc: "icons/Edit.png",
			href: 'pages/GenericDocuments/GenericDocumentsAction.do?_method=searchPatientGeneralDocuments',
			target: '_blank',
			description: toolbarOptions["doclist"]["description"]};
	if (documentsAdd == 'A')
		toolbar.addDocuments = { title: toolbarOptions["adddocs"]["name"], imageSrc: "icons/Edit.png",
			href: 'pages/GenericDocuments/GenericDocumentsAction.do?_methDescod=addPatientDocument',
			target: '_blank',
			description: toolbarOptions["adddocs"]["description"]};
//	if (docket == 'A')
//		toolbar.Docket = { title: "Docket",   imageSrc: "icons/Edit.png", href: 'emr/PatientDocket.do?_method=getPatientDocket', target: '_blank'};
	if (regGeneral == 'A')
		toolbar.Edit = { title: toolbarOptions["edit"]["name"], imageSrc: "icons/Edit.png",
				 href: 'Registration/GeneralRegistration.do?_method=show&regType=regd',
				 description: toolbarOptions["edit"]["description"], target: '_blank'};
	if (slida == 'A')
	    toolbar.XRay = {title:toolbarOptions["slida"]["name"], imageSrc:"icons/Edit.png",
			description:toolbarOptions["slida"]["description"], onclick : 'postSlidaMessage'};
	createToolbar(toolbar);
}

function postSlidaMessage(anchor, params, id, toolbar) {
    if (slida == 'A' && params && params.mr_no) {
        var slidaUrl = cpath + "/SlidaAction.do?_method=register&mr_no=" + params.mr_no;
        var xhr = newXMLHttpRequest();
        xhr.open("GET",slidaUrl.toString(), false);
        xhr.send(null);
        if (xhr.readyState == 4 && xhr.status == 200) {
	    if (xhr.responseText!=null) {
	        eval("var resp=" + xhr.responseText+";");
	        alert (resp.message);
	    }
    }
    }
    return false;
}


var countryAutoComplete = null;
function autoCountry() {
	if (empty(document.getElementById('_country'))) return;

	YAHOO.example.ACJSArray = new function() {
		var countryArray = {result: countryList};
		datasource = new YAHOO.util.LocalDataSource(countryArray);
		datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'country_name'},
						{key : 'country_id'}
					 ]
		};

		if (countryAutoComplete != null) {
			countryAutoComplete.destroy();
		}
		countryAutoComplete = new YAHOO.widget.AutoComplete('_country','countrycontainer', datasource);
		countryAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		countryAutoComplete.useShadow = true;
		countryAutoComplete.minQueryLength = 0;
		countryAutoComplete.autoHighlight = false;
		countryAutoComplete.allowBrowserAutocomplete = false;
		countryAutoComplete.forceSelection = true;
		countryAutoComplete.resultTypeList = false;
		countryAutoComplete.maxResultsDisplayed = 20;
		if (countryAutoComplete._elTextbox.value != '') {
			countryAutoComplete._bItemSelected = true;
			countryAutoComplete._sInitInputValue = countryAutoComplete._elTextbox.value;
		}

		countryAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.country.value = elItem[2].country_id;
		});

		countryAutoComplete.selectionEnforceEvent.subscribe(function(){
			searchForm.country.value = '';
		});
	}
}

var stateAutoComplete = null;
function autoState() {
	if (empty(document.getElementById('_patientstate'))) return;

	YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : stateList});
		datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [	{key : 'state_name'},
						{key : 'state_id'}
					]
		};
		if (stateAutoComplete != null) {
			stateAutoComplete.destroy();
		}
		stateAutoComplete = new YAHOO.widget.AutoComplete('_patientstate','statecontainer', datasource);
		stateAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		stateAutoComplete.useShadow = true;
		stateAutoComplete.minQueryLength = 0;
		stateAutoComplete.autoHighlight = false;
		stateAutoComplete.allowBrowserAutocomplete = false;
		stateAutoComplete.resultTypeList = false;
		stateAutoComplete.forceSelection = true;
		stateAutoComplete.maxResultsDisplayed = 20;
		if (stateAutoComplete._elTextbox.value != '') {
			stateAutoComplete._bItemSelected = true;
			stateAutoComplete._sInitInputValue = stateAutoComplete._elTextbox.value;
		}

		stateAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.patient_state.value = elItem[2].state_id;
			autoCity();
		});

		stateAutoComplete.selectionEnforceEvent.subscribe(function(){
			searchForm.patient_state.value = '';
		});
	}
}

var cityAutoComplete = null;
function autoCity() {
	if (empty(document.getElementById('_patientcity'))) return;

	var selectedState = searchForm.patient_state.value;
	var citiesForSelectedState = new Array();
	var i=0;
	for(var j = 0;j<cityList.length;j++){
		if (selectedState == cityList[j].state_id){
			citiesForSelectedState.push(cityList[j]);
		}
	}

	YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : citiesForSelectedState});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'city_name'},
						{key : 'city_id'}
					]
		};
		if (cityAutoComplete != null) {
			cityAutoComplete.destroy();
		}
		cityAutoComplete = new YAHOO.widget.AutoComplete('_patientcity','citycontainer', datasource);
		cityAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		cityAutoComplete.useShadow = true;
		cityAutoComplete.minQueryLength = 0;
		cityAutoComplete.autoHighlight = false;
		cityAutoComplete.allowBrowserAutocomplete = false;
		cityAutoComplete.resultTypeList = false;
		cityAutoComplete.forceSelection = true;
		cityAutoComplete.maxResultsDisplayed = 20;
		if (cityAutoComplete._elTextbox.value != '') {
			cityAutoComplete._bItemSelected = true;
			cityAutoComplete._sInitInputValue = cityAutoComplete._elTextbox.value;
		}

		cityAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.patient_city.value = elItem[2].city_id;
			autoArea();
		});

		cityAutoComplete.selectionEnforceEvent.subscribe(function(){
			searchForm.patient_city.value = '';
		});
	}
}

var areaAutoComplete = null;
function autoArea() {
	if (empty(document.getElementById('patient_area'))) return;

	var selectedCity = searchForm.patient_city.value;
	// this made it as ajax autocomplete since for some hospital total no of areas were huge.
	// bringing all of them onload was hitting the performance issue.
	YAHOO.example.ACJSArray = new function() {
		var areaDataSource = new YAHOO.util.XHRDataSource(cpath + '/master/areas/lookup.json');
		areaDataSource.scriptQueryParam="filterText";
		areaDataSource.scriptQueryAppend = "page_size=10&sort_order=area_name&contains=true&city_id=" + selectedCity;
		areaDataSource.connMethodPost=false;
		areaDataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		areaDataSource.responseSchema = {
		resultsList : "dtoList",
		fields: [{key: "area_name"},
			{key: "area_id"},
			{key: "city_name"},
			{key: "city_id"},
			{key: "district_name"},
			{key: "district_id"},
			{key: "state_name"},
			{key: "state_id"},
			{key: "country_name"},
			{key: "country_id"}]
		};

		if (areaAutoComplete != null) {
			areaAutoComplete.destroy();
			areaAutoComplete = null;
		}
		areaAutoComplete = new YAHOO.widget.AutoComplete('patient_area','areacontainer', areaDataSource);
		areaAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		areaAutoComplete.useShadow = false;
		areaAutoComplete.minQueryLength = 1;
		areaAutoComplete.typeAhead = false;
		areaAutoComplete.autoHighlight = false;
		areaAutoComplete.allowBrowserAutocomplete = false;
		areaAutoComplete.resultTypeList = false;
		areaAutoComplete.forceSelection = true;
		areaAutoComplete.maxResultsDisplayed = 10;

		if (areaAutoComplete._elTextbox.value != '') {
			areaAutoComplete._bItemSelected = true;
			areaAutoComplete._sInitInputValue = areaAutoComplete._elTextbox.value;
		}
		areaAutoComplete.itemSelectEvent.subscribe(function(oSelf, elItem) {
			var record = elItem[2];
			YAHOO.util.Dom.get('patient_area').value = record.area_name;
			YAHOO.util.Dom.get('area_id').value = record.area_id;
		});
		areaAutoComplete.selectionEnforceEvent.subscribe(function() {
			YAHOO.util.Dom.get(areaid).value = '';
		});
		return areaAutoComplete;
	}
}


function onChangeRegField() {
	var regFieldNameObj = searchForm._regFieldName;
	var regFieldValueObj = searchForm._regFieldValue;
	regFieldValueObj.options.selectedIndex = 0;
	regFieldValueObj.options.length = 1;

	if(regFieldNameObj.value == 'patient_gender') {
		regFieldValueObj.length = 4;
		var option = new Option(getString("js.common.gender.male"),"M");
		regFieldValueObj[1] = option;
		option = new Option(getString("js.common.gender.female"),"F");
		regFieldValueObj[2] = option;
		option = new Option(getString("js.common.gender.couple"),"C");
		regFieldValueObj[3] = option;
		option = new Option(getString("js.common.gender.others"),"O");
		regFieldValueObj[4] = option;
	}
	if(regFieldNameObj.value == 'primary_sponsor_id' || regFieldNameObj.value == 'secondary_sponsor_id') {
	    regFieldValueObj.length = tpasponsorList.length+1;
	    for(var i=0;i<tpasponsorList.length;i++) {
	    var option = new Option(tpasponsorList[i].tpa_name,tpasponsorList[i].tpa_id);
	    regFieldValueObj[i+1] = option;
	    }
	}
	if(regFieldNameObj.value == 'org_id') {
	    regFieldValueObj.length = orgNameJSONList.length+1;
	    for(var i=0;i<orgNameJSONList.length;i++) {
	    var option = new Option(orgNameJSONList[i].org_name,orgNameJSONList[i].org_id);
	    regFieldValueObj[i+1] = option;
	    }
	}
	if(regFieldNameObj.value == 'patient_category_id') {
		regFieldValueObj.length = categoryList.length+1;
		for(var i=0;i<categoryList.length;i++) {
			var option = new Option(categoryList[i].category_name,categoryList[i].category_id);
			regFieldValueObj[i+1] = option;
		}
	}
	if(regFieldNameObj.value == 'custom_list1_value') {
	    regFieldValueObj.length = customList1.length+1;
	    for(var i=0;i<customList1.length;i++) {
	    	var option = new Option(customList1[i].custom_value,customList1[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}
	if(regFieldNameObj.value == 'custom_list2_value') {
	    regFieldValueObj.length = customList2.length+1;
	    for(var i=0;i<customList2.length;i++) {
	    	var option = new Option(customList2[i].custom_value,customList2[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}
	if(regFieldNameObj.value == 'custom_list3_value') {
	    regFieldValueObj.length = customList3.length+1;
	    for(var i=0;i<customList3.length;i++) {
	    	var option = new Option(customList3[i].custom_value,customList3[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}

	if(regFieldNameObj.value == 'custom_list4_value') {
	    regFieldValueObj.length = customList4.length+1;
	    for(var i=0;i<customList4.length;i++) {
	    	var option = new Option(customList4[i].custom_value,customList4[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}

	if(regFieldNameObj.value == 'custom_list5_value') {
	    regFieldValueObj.length = customList5.length+1;
	    for(var i=0;i<customList5.length;i++) {
	    	var option = new Option(customList5[i].custom_value,customList5[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}

	if(regFieldNameObj.value == 'custom_list6_value') {
	    regFieldValueObj.length = customList6.length+1;
	    for(var i=0;i<customList6.length;i++) {
	    	var option = new Option(customList6[i].custom_value,customList6[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}

	if(regFieldNameObj.value == 'custom_list7_value') {
	    regFieldValueObj.length = customList7.length+1;
	    for(var i=0;i<customList7.length;i++) {
	    	var option = new Option(customList7[i].custom_value,customList7[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}

	if(regFieldNameObj.value == 'custom_list8_value') {
	    regFieldValueObj.length = customList8.length+1;
	    for(var i=0;i<customList8.length;i++) {
	    	var option = new Option(customList8[i].custom_value,customList8[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}

	if(regFieldNameObj.value == 'custom_list9_value') {
	    regFieldValueObj.length = customList9.length+1;
	    for(var i=0;i<customList9.length;i++) {
	    	var option = new Option(customList9[i].custom_value,customList9[i].custom_value);
	    	regFieldValueObj[i+1] = option;
	    }
	}
   setSelectedIndex(regFieldValueObj, searchForm._hiddenRegFieldValue.value);
}

function selectListItem(jsonArray, autocomplete) {
	var textEl = autocomplete._elTextbox;
	var textBoxValue = textEl.value;
	if (empty(textBoxValue)) return null;

	var elListItem = autocomplete._elList.childNodes[0];
	sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key ||
               		autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
	for (var i=0; i<jsonArray.length; i++) {
    	var value = jsonArray[i][sMatchKey];
    	if (textBoxValue == value) {
    		oResult = jsonArray[i];
    		elListItem._sResultMatch = value;
		    elListItem._oResultData = oResult;
		    return elListItem;
    	}
    }
    return null;
}

function showDateAndNumericInput(obj) {
	if (obj!= null && !empty(obj.value)) {
		if (obj.value == 'custom_field14' || obj.value == 'custom_field15' || obj.value == 'custom_field16') {
			document.getElementById('_customRegFieldValue') !== null ? document.getElementById('_customRegFieldValue').disabled = true : '';
			document.getElementById('_customRegNumericFieldValue') !== null ? document.getElementById('_customRegNumericFieldValue').disabled = true : '';
			document.getElementById('_customRegDateFieldValue') !== null ? document.getElementById('_customRegDateFieldValue').disabled = false : '';
			document.getElementById('customField') !== null ? document.getElementById('customField').style.display = 'none' : '';
			document.getElementById('customNumericField') !== null ? document.getElementById('customNumericField').style.display = 'none' : '';
			document.getElementById('customDateField') !== null ? document.getElementById('customDateField').style.display = 'block' : '';
		} else if (obj.value == 'custom_field17' || obj.value == 'custom_field18' || obj.value == 'custom_field19') {
			document.getElementById('_customRegFieldValue') !== null ? document.getElementById('_customRegFieldValue').disabled = true : '';
			document.getElementById('_customRegDateFieldValue') !== null ? document.getElementById('_customRegDateFieldValue').disabled = true : '';
			document.getElementById('_customRegNumericFieldValue') !== null ? document.getElementById('_customRegNumericFieldValue').disabled = false: '';
			document.getElementById('customField') !== null ? document.getElementById('customField').style.display = 'none' : '';
			document.getElementById('customDateField') !== null ? document.getElementById('customDateField').style.display = 'none' : '';
			document.getElementById('customNumericField') !== null ? document.getElementById('customNumericField').style.display = 'block' : '';
		} else {
			document.getElementById('_customRegNumericFieldValue') !== null ? document.getElementById('_customRegNumericFieldValue').disabled = true : '';
			document.getElementById('_customRegDateFieldValue') !== null ? document.getElementById('_customRegDateFieldValue').disabled = true : '';
			document.getElementById('_customRegFieldValue') !== null ? document.getElementById('_customRegFieldValue').disabled = false : '';
			document.getElementById('customNumericField') !== null ? document.getElementById('customNumericField').style.display = 'none' : '';
			document.getElementById('customDateField') !== null ? document.getElementById('customDateField').style.display = 'none' : '';
			document.getElementById('customField') !== null ? document.getElementById('customField').style.display = 'block' : '';
		}
	} else {
		document.getElementById('customField') !== null ? document.getElementById('customField').style.display = 'block' : '';
		document.getElementById('customField') !== null ? document.getElementById('_customRegFieldValue').disabled = false : '';
		document.getElementById('_customRegNumericFieldValue') !== null ? document.getElementById('_customRegNumericFieldValue').disabled = true : '';
		document.getElementById('_customRegDateFieldValue') !== null ? document.getElementById('_customRegDateFieldValue').disabled = true : '';
	}
}

function validateRegNumericFields() {
	var regCustomNumericField = searchForm._customRegFieldName;
	var fieldLabel = regCustomNumericField.options[regCustomNumericField.selectedIndex].text
	if(regCustomNumericField != null && !empty(regCustomNumericField.value)) {
		if(regCustomNumericField.value == "custom_field17" || regCustomNumericField.value == "custom_field18" ||
			regCustomNumericField.value == "custom_field19") {
			if(!isValidNumber(document.getElementById('_customRegNumericFieldValue'),"Y",fieldLabel))
				return false;
		}
	}
	return true;
}

var patientSearchSetHrefs = function(params, id, enableList, toolbarKey, event, validateOnRClick) {
	if (empty(gToolbars[toolbarKey])) return false;

	var i=0;
	var toolbar = gToolbars[toolbarKey];
	for (var key in toolbar) {
		var data = toolbar[key];
		var anchor = document.getElementById('toolbarAction' + toolbarKey + key);
		if (empty(anchor)) {
			debug("No anchor for " + 'toolbarAction'+ toolbarKey + key + ":");
			i++;
			continue;
		}
		var href = data.href;
		if (!empty(href) && href != '/patients/opregistration/index.htm') {
			for (var paramname in params) {
				var paramvalue = params[paramname];
				if (paramname.charAt(0) == '%') {
					// replace a component of the href
					href = href.replace(paramname, paramvalue);
				} else {
					// append as param=value
					href += "&" + paramname + "=" + encodeURIComponent(paramvalue);
				}
			}
			anchor.href = cpath + "/" + href;
		} else if (!empty(href) && href == '/patients/opregistration/index.htm') {
			href = cpath + href;
			var mrNo = Object.keys(params).indexOf("mr_no") != -1 ? params['mr_no'] : undefined;
			if (!mrNo) {
				mrNo = Object.keys(params).indexOf("mrno") != -1 ? params['mrno'] : undefined;
			}
			href += "#/filter/default/patient/" + mrNo + "/registration";
			href +="?retain_route_params=true&";
			anchor.href = href;
		}

		var enable = true;
		if (enableList) {
			enableToolbarItem(key, enableList[i], toolbarKey);
			enable = enableList[i];
		} else {
			enableToolbarItem(key, enable, toolbarKey);
		}

		if (!empty(data.onclick) && enable) {
			setParams(anchor, params, id, toolbar, validateOnRClick);
		}
		i++;
	}
	return true;
}

