var searchForm = null;

function init(visitsExist, form) {
	searchForm = form;
	// enablePatientStatus();
	// enablePatientType();
	enablePatientVisit();
	autoArea();
	autoCountry();
	autoState();
	autoCity();
	//onChangeRegField();
	initDeptAutoComp();
	initDocAutoComp();
	initRefDocAutoComp();
	mrNoAutoComplete(cpath);
	if (visitsExist) {
		document.getElementById('divMore').style.display = 'none';
	} else {
		document.getElementById('divMore').style.display = 'block';
	}
	document.getElementById('mr_no').focus();

	createToolbar(toolbar);
}

function mrNoAutoComplete(cpath) {
	Insta.initMRNoAcSearch(cpath, 'mr_no', 'mrnoContainer', 'all', null, null);
}

function enablePatientStatus() {
	if (searchForm.statusAll) {
		var disabled = searchForm.statusAll.checked;
		searchForm.statusActive.disabled = disabled;
		searchForm.statusInactive.disabled = disabled;
	}
}

function enablePatientType() {
	if (searchForm.typeAll) {
		var disabled = searchForm.typeAll.checked;
		searchForm.typeIP.disabled = disabled;
		searchForm.typeOP.disabled = disabled;
	}
	searchForm.statusNoVisit.disabled = disabled;
}

function enablePatientVisit() {
	if (searchForm.visitAll) {
		var disabled = searchForm.visitAll.checked;
		searchForm.visitNew.disabled = disabled;
		searchForm.visitRevisit.disabled = disabled;
	}
}

function validateSearchForm(){

	var fromDate = searchForm.fdate.value;
	var toDate = searchForm.tdate.value;
	var disFromDate = '';
	var disToDate = '';

	if ( searchForm.disfdate != undefined )
		disFromDate = searchForm.disfdate.value
	if ( searchForm.distdate != undefined )
	    disToDate = searchForm.distdate.value;

	if ((fromDate != '' && toDate == '') || (toDate != '' && fromDate=='')) {
		alert("Invalid search criteria: please enter from and to dates");
		if (fromDate == '') searchForm.fdate.focus();
		if (toDate == '') searchForm.tdate.focus();
		return false;
	}
	if ((disFromDate != '' && disToDate == '') || (disToDate != '' && disFromDate == '')) {
		alert("Invalid search criteria: please enter from and to dates");
		if (disFromDate == '') searchForm.disfdate.focus();
		if (disToDate == '') searchForm.distdate.focus();
		return false;
	}

	if(!doValidateDateField(searchForm.fdate)){
		return false;
	}
	if(!doValidateDateField(searchForm.tdate)){
		return false;
	}
	if (searchForm.disfdate && searchForm.distdate) {
		if(!doValidateDateField(searchForm.disfdate)){
			return false;
		}
		if(!doValidateDateField(searchForm.distdate)){
			return false;
		}
	}
	return true;
}

function autoArea(){
	if (empty(document.getElementById('patientarea'))) return;

	var selectedCity;
	for(var j = 0;j<cityList.length;j++){
		if (searchForm.cityname.value == cityList[j].CITY_NAME){
			selectedCity = cityList[j].CITY_ID;
		}
	}

	YAHOO.example.areaArray = [];
	var i=0;
	for(var j = 0;j<areaListmain.length;j++){
		if (selectedCity == areaListmain[j].PATIENT_CITY){
			YAHOO.example.areaArray.length = i+1;
			YAHOO.example.areaArray[i] = areaListmain[j];
			i++;
		}

	}

	YAHOO.example.ACJSArray = new function() {
		var areaArray = {result : YAHOO.example.areaArray};
		datasource = new YAHOO.util.LocalDataSource(areaArray);
		datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : "result",
			fields: [{key : "PATIENT_AREA"}]
		}
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('patientarea','areacontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.resultTypeList = false;
		this.oAutoComp.forceSelection = true;
		this.oAutoComp.maxResultsDisplayed = 20;

		selectListItem(YAHOO.example.areaArray, this.oAutoComp);

	}
}

function autoCountry(){
	if (empty(document.getElementById('country_name'))) return;

	YAHOO.example.ACJSArray = new function() {
		var countryArray = {result: countryList};
		datasource = new YAHOO.util.LocalDataSource(countryArray);
		datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'COUNTRY_NAME'},
						{key : 'COUNTRY_ID'}
					 ]
		};
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('country_name','countrycontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.forceSelection = true;
		this.oAutoComp.resultTypeList = false;
		this.oAutoComp.maxResultsDisplayed = 20;
		searchForm.country_name.value = ''; // clears the previously selected country id on browser refresh

		selectListItem(countryList, this.oAutoComp);
		this.oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.country.value = elItem[2].COUNTRY_ID;
		});

		this.oAutoComp.selectionEnforceEvent.subscribe(function(){
			searchForm.country.value = '';
		});
	}
}

function autoState(){
	if (empty(document.getElementById('statename'))) return;

	YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : stateList});
		datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [	{key : 'STATE_NAME'},
						{key : 'STATE_ID'}
					]
		};
		// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('statename','statecontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.resultTypeList = false;
		this.oAutoComp.forceSelection = true;
		this.oAutoComp.maxResultsDisplayed = 20;
		searchForm.patient_state.value = ''; // clears the previously selected state id on browser refresh

		selectListItem(stateList, this.oAutoComp);
		this.oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.patient_state.value = elItem[2].STATE_ID;
		});

		this.oAutoComp.selectionEnforceEvent.subscribe(function(){
			searchForm.patient_state.value = '';
		});
	}
}


function autoCity(){
	if (empty(document.getElementById('cityname'))) return;

	var selectedState = searchForm.patient_state.value;
	YAHOO.example.cityArray = [];
	var i=0;
	for(var j = 0;j<cityList.length;j++){
		if (selectedState == cityList[j].STATE_ID){
			YAHOO.example.cityArray.length = i+1;
			YAHOO.example.cityArray[i] = cityList[j];
			i++;
		}
	}

	YAHOO.example.ACJSArray = new function() {
		datasource = new YAHOO.util.LocalDataSource({result : YAHOO.example.cityArray});
		datasource.reponseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList : 'result',
			fields : [ 	{key : 'CITY_NAME'},
						{key : 'CITY_ID'}
					]
		};
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('cityname','citycontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.resultTypeList = false;
		this.oAutoComp.forceSelection = true;
		this.oAutoComp.maxResultsDisplayed = 20;
		searchForm.patient_city.value = ''; // clears the previously selected city id on browser refresh

		selectListItem(YAHOO.example.cityArray, this.oAutoComp);
		this.oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
			searchForm.patient_city.value = elItem[2].CITY_ID;
		});

		this.oAutoComp.selectionEnforceEvent.subscribe(function(){
			searchForm.patient_city.value = '';
		});
	}
}

function initDeptAutoComp() {
	if (empty(document.getElementById('departmentName'))) return;
	var localDs = new YAHOO.util.LocalDataSource({result : depts});
	localDs.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = {
		resultsList : 'result',
		fields : [	{key : 'DEPT_NAME'},
					{key : 'DEPT_ID'}
				 ]
	};
	var deptAutoComp = new YAHOO.widget.AutoComplete('departmentName', 'deptContainer', localDs);
	deptAutoComp.typeAhead = false;
	deptAutoComp.forceSelection = true;
	deptAutoComp.resultTypeList = false;
	deptAutoComp.minQueryLength = 0;
	deptAutoComp.maxResultsDisplayed = 20;
	searchForm.department.value = ''; // clears the previously selected department id on browser refresh
	selectListItem(depts, deptAutoComp);
	deptAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
		searchForm.department.value = elItem[2].DEPT_ID;
	});

	deptAutoComp.selectionEnforceEvent.subscribe(function(){
		searchForm.department.value = '';
	});
}

function onChangeRegField() {
	var regFieldNameObj = document.patientSearchForm.regFieldName;
	var regFieldValueObj = document.patientSearchForm.regFieldValue;
	regFieldValueObj.options.selectedIndex = 0;
	regFieldValueObj.options.length = 1;

	if(regFieldNameObj.value == 'patient_gender') {
		regFieldValueObj.length = 4;
		var option = new Option("Male","M");
		regFieldValueObj[1] = option;
		option = new Option("Female","F");
		regFieldValueObj[2] = option;
		option = new Option("Others","O");
		regFieldValueObj[3] = option;
	}
	if(regFieldNameObj.value == 'occupation') {
		regFieldValueObj.length = occupationList.length+1;
		for(var i=0;i<occupationList.length;i++) {
			var option = new Option(occupationList[i].occupation,occupationList[i].occupation_id);
			regFieldValueObj[i+1] = option;
		}
	}
	if(regFieldNameObj.value == 'bloodgroup') {
		regFieldValueObj.length = bloodgroupList.length+1;
		for(var i=0;i<bloodgroupList.length;i++) {
			var option = new Option(bloodgroupList[i].blood_group_name,bloodgroupList[i].blood_group_name);
			regFieldValueObj[i+1] = option;
		}
	}
	if(regFieldNameObj.value == 'religion') {
		regFieldValueObj.length = religionList.length+1;
		for(var i=0;i<religionList.length;i++) {
			var option = new Option(religionList[i].religion_name,religionList[i].religion_id);
			regFieldValueObj[i+1] = option;
		}
	}
	setSelectedIndex(regFieldValueObj,document.patientSearchForm.hiddenRegFieldValue.value);
}


function initDocAutoComp() {
	if (empty(document.getElementById('doctorName'))) return;
	var localDs = new YAHOO.util.LocalDataSource({result : doctors});
	localDs.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = {
		resultsList : 'result',
		fields : [	{key : 'DOCTOR_NAME'},
					{key : 'DOCTOR_ID'}
				 ]
	};
	var docAutoComp = new YAHOO.widget.AutoComplete('doctorName', 'doctorContainer', localDs);
	docAutoComp.typeAhead = false;
	docAutoComp.forceSelection = true;
	docAutoComp.resultTypeList = false;
	docAutoComp.minQueryLength = 0;
	docAutoComp.maxResultsDisplayed = 20;
	searchForm.doctor.value = ''; // clears the previously selected doctor id on browser refresh

	selectListItem(doctors, docAutoComp);
	docAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
		searchForm.doctor.value = elItem[2].DOCTOR_ID;
	});

	docAutoComp.selectionEnforceEvent.subscribe(function(){
		searchForm.doctor.value = '';
	});
}

function initRefDocAutoComp() {
	if (empty(document.getElementById('refDoctorName'))) return;

	var localDs = new YAHOO.util.LocalDataSource({result : referralDoctors});
	localDs.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = {
		resultsList : 'result',
		fields : [	{key : 'REF_NAME'},
					{key : 'REF_ID'}
				 ]
	};
	var refdocAutoComp = new YAHOO.widget.AutoComplete('refDoctorName', 'refDoctorContainer', localDs);
	refdocAutoComp.typeAhead = false;
	refdocAutoComp.forceSelection = true;
	refdocAutoComp.resultTypeList = false;
	refdocAutoComp.minQueryLength = 0;
	searchForm.refdoctor.value = ''; // clears the previously selected ref doctor id on browser refresh

	selectListItem(referralDoctors, refdocAutoComp);
	refdocAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem, oData) {
		searchForm.refdoctor.value = elItem[2].REF_ID;
	});

	refdocAutoComp.selectionEnforceEvent.subscribe(function(){
		searchForm.refdoctor.value = '';
	});

}

function selectListItem(jsonArray, autocomplete) {
	var textEl = autocomplete._elTextbox;
	var textBoxValue = textEl.value;
	var txtdefault = textEl.getAttribute('txtdefault') ? textEl.getAttribute('txtdefault') : '';

	if (txtdefault == textBoxValue) return false;
	if (empty(textBoxValue)) return false;

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
		    autocomplete._selectItem(elListItem);
		    return true;
    	}
    }
    return false;
}

