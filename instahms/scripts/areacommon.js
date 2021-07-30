/**
 *
 */

function initAddressFieldsAutocompletes(){
	var addressFields = {
		areaNameField: 'patient_area',
		cityNameField: 'patient_city',
		districtNameField: 'patient_district',
		stateNameField: 'patient_state',
		countryNameField: 'patient_country',
		areaIdField: 'area_id',
		cityIdField: 'city_id',
		districtIdField: 'district_id',
		stateIdField: 'state_id',
		countryIdField: 'country_id'
	};
	initAreaAutoComplete("patient_area","area_dropdown", "area_id", addressFields);
	initCityAutoComplete("patient_city","city_dropdown", "city_id", addressFields);
	if(enableDistrict == 'Y') {
		initDistrictAutoComplete("patient_district","district_dropdown","district_id", addressFields);
	}
	initStateAutoComplete("patient_state","state_dropdown","state_id", addressFields);
}

function initRegistrationReferralDoctorFilterAutocompletes(){
	var addressFields = {
		areaNameField: 'referral_filter_area',
		cityNameField: 'referral_filter_city',
		districtNameField: 'referral_filter_district',
		stateNameField: 'referral_filter_state',
		countryNameField: 'referral_filter_country',
		areaIdField: 'referral_filter_area_id',
		cityIdField: 'referral_filter_city_id',
		districtIdField: 'referral_filter_district_id',
		stateIdField: 'referral_filter_state_id',
		countryIdField: 'referral_filter_country_id',
		referralDoctorIdField: 'referred_by',
		referralDoctorField: 'referaldoctorName'
	};
	initAreaAutoComplete("referral_filter_area","referral_filter_area_dropdown", "referral_filter_area_id", addressFields);
	initCityAutoComplete("referral_filter_city","referral_filter_city_dropdown", "referral_filter_city_id", addressFields);
	if(enableDistrict == 'Y') {
		initDistrictAutoComplete("referral_filter_district","referral_filter_district_dropdown","referral_filter_district_id", addressFields);
	}
	initStateAutoComplete("referral_filter_state","referral_filter_state_dropdown","referral_filter_state_id", addressFields);
}

function initAreaAutoComplete(patientareaname,areadropdown,areaid, addressFields){
	var areaUrl = cpath+"/master/areas/lookup.json?page_size=10&sort_order=area_name&contains=true";
	var dataSource = new YAHOO.util.DataSource(areaUrl);
	dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	dataSource.responseSchema = {
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

	oAutoComp = new YAHOO.widget.AutoComplete(patientareaname,areadropdown,dataSource);


	oAutoComp.generateRequest = function(sQuery) {
		return getQueryParameters(sQuery, "area_id", addressFields);
	};

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 5;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList= false;

	oAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var areaDetails = oResultData;

		var details = '';
		if(enableDistrict == 'Y') {
			details = areaDetails.area_name + "-"+ areaDetails.city_name+"-"+
				(areaDetails.district_name == null || areaDetails.district_name == undefined ? '' : areaDetails.district_name+"-")+
				areaDetails.state_name+"-"+areaDetails.country_name;
		} else {
			details = areaDetails.area_name + "-"+ areaDetails.city_name+"-"+
						areaDetails.state_name+"-"+areaDetails.country_name;
		}
		return details;
	}

	oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var record = elItem[2];
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		YAHOO.util.Dom.get(addressFields.areaIdField).value = record.area_id;
		YAHOO.util.Dom.get(addressFields.areaNameField).value = record.area_name;
		YAHOO.util.Dom.get(addressFields.cityIdField).value = record.city_id;
		YAHOO.util.Dom.get(addressFields.cityNameField).value = record.city_name;
		if(enableDistrict == 'Y') {
			YAHOO.util.Dom.get(addressFields.districtIdField).value = record.district_id;
			YAHOO.util.Dom.get(addressFields.districtNameField).value = record.district_name;
		}
		YAHOO.util.Dom.get(addressFields.stateIdField).value = record.state_id;
		YAHOO.util.Dom.get(addressFields.stateNameField).value = record.state_name;
		YAHOO.util.Dom.get(addressFields.countryIdField).value = record.country_id;
		document.getElementById(addressFields.countryNameField).textContent = record.country_name;
	});

	oAutoComp.selectionEnforceEvent.subscribe(function() {
		YAHOO.util.Dom.get(areaid).value = '';
	});

	oAutoComp.textboxBlurEvent.subscribe(function(oAutoComp) {
		if(YAHOO.util.Dom.get(addressFields.areaIdField).value) {
			return;
		}
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
	});
}


// city auto complete
function initCityAutoComplete(patientcityname,citydropdown,cityid, addressFields){
	var cityUrl = cpath+"/master/cities/lookup.json?page_size=10&sort_order=city_name&contains=true";
	var dataSource = new YAHOO.util.DataSource(cityUrl);
	dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : "dtoList",
		fields: [{key: "city_name"},
				 {key: "city_id"},
				 {key: "district_name"},
				 {key: "district_id"},
				 {key: "state_name"},
				 {key: "state_id"},
				 {key: "country_name"},
				 {key: "country_id"}]
		};

	oAutoComp = new YAHOO.widget.AutoComplete(patientcityname,citydropdown,dataSource);


	oAutoComp.generateRequest = function(sQuery) {
		return getQueryParameters(sQuery, "city_id", addressFields);
	};

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 5;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList= false;

	oAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var areaDetails = oResultData;

		var details = '';
		if(enableDistrict == 'Y') {
			details = areaDetails.city_name+"-"+
				(areaDetails.district_name == null || areaDetails.district_name == undefined ? '' : areaDetails.district_name+"-")+
				areaDetails.state_name+"-"+areaDetails.country_name;
		} else {
			details = areaDetails.city_name+"-"+
						areaDetails.state_name+"-"+areaDetails.country_name;
		}
		return details;
	}

	oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var record = elItem[2];
		YAHOO.util.Dom.get(addressFields.areaIdField).value = '';
		YAHOO.util.Dom.get(addressFields.areaNameField).value = '';
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		YAHOO.util.Dom.get(addressFields.cityIdField).value = record.city_id;
		YAHOO.util.Dom.get(addressFields.cityNameField).value = record.city_name;
		if(enableDistrict == 'Y') {
			YAHOO.util.Dom.get(addressFields.districtIdField).value = record.district_id;
			YAHOO.util.Dom.get(addressFields.districtNameField).value = record.district_name;
		}
		YAHOO.util.Dom.get(addressFields.stateIdField).value = record.state_id;
		YAHOO.util.Dom.get(addressFields.stateNameField).value = record.state_name;
		YAHOO.util.Dom.get(addressFields.countryIdField).value = record.country_id;
		document.getElementById(addressFields.countryNameField).textContent = record.country_name;
	});

	oAutoComp.selectionEnforceEvent.subscribe(function() {
		YAHOO.util.Dom.get(cityid).value = '';
	});

	oAutoComp.textboxBlurEvent.subscribe(function(oAutoComp) {
		if(YAHOO.util.Dom.get(addressFields.cityIdField).value) {
			return;
		}
		YAHOO.util.Dom.get(addressFields.areaIdField).value = '';
		YAHOO.util.Dom.get(addressFields.areaNameField).value = '';
		YAHOO.util.Dom.get(addressFields.cityIdField).value = '';
		YAHOO.util.Dom.get(addressFields.cityNameField).value = '';
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		/*YAHOO.util.Dom.get(addressFields.districtIdField).value = '';
		YAHOO.util.Dom.get(addressFields.districtNameField).value = '';
		YAHOO.util.Dom.get(addressFields.stateIdField).value = '';
		YAHOO.util.Dom.get(addressFields.stateNameField).value = '';
		YAHOO.util.Dom.get(addressFields.countryIdField).value = '';
		document.getElementById(addressFields.countryNameField).textContent = '';*/
	});
}

// district autocomplete

function initDistrictAutoComplete(patientdistrictname,districtdropdown,districtid, addressFields){
	var districtUrl = cpath+"/master/districts/lookup.json?page_size=10&sort_order=district_name&contains=true";
	var dataSource = new YAHOO.util.DataSource(districtUrl);
	dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : "dtoList",
		fields: [{key: "district_name"},
				 {key: "district_id"},
				 {key: "state_name"},
				 {key: "state_id"},
				 {key: "country_name"},
				 {key: "country_id"}]
		};

	oAutoComp = new YAHOO.widget.AutoComplete(patientdistrictname,districtdropdown,dataSource);


	oAutoComp.generateRequest = function(sQuery) {
		return getQueryParameters(sQuery, "district_id", addressFields);
	};

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 5;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList= false;

	oAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var areaDetails = oResultData;

		var details = areaDetails.district_name+"-"+areaDetails.state_name+"-"+areaDetails.country_name;
		return details;
	}

	oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var record = elItem[2];
		YAHOO.util.Dom.get(addressFields.areaIdField).value = '';
		YAHOO.util.Dom.get(addressFields.areaNameField).value = '';
		YAHOO.util.Dom.get(addressFields.cityIdField).value = '';
		YAHOO.util.Dom.get(addressFields.cityNameField).value = '';
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		YAHOO.util.Dom.get(addressFields.districtIdField).value = record.district_id;
		YAHOO.util.Dom.get(addressFields.districtNameField).value = record.district_name;
		YAHOO.util.Dom.get(addressFields.stateIdField).value = record.state_id;
		YAHOO.util.Dom.get(addressFields.stateNameField).value = record.state_name;
		YAHOO.util.Dom.get(addressFields.countryIdField).value = record.country_id;
		document.getElementById(addressFields.countryNameField).textContent = record.country_name;
	});

	oAutoComp.selectionEnforceEvent.subscribe(function() {
		YAHOO.util.Dom.get(districtid).value = '';
	});

	oAutoComp.textboxBlurEvent.subscribe(function(oAutoComp) {
		if(YAHOO.util.Dom.get(addressFields.districtIdField).value) {
			return;
		}
		YAHOO.util.Dom.get(addressFields.areaIdField).value = '';
		YAHOO.util.Dom.get(addressFields.areaNameField).value = '';
		YAHOO.util.Dom.get(addressFields.cityIdField).value = '';
		YAHOO.util.Dom.get(addressFields.cityNameField).value = '';
		YAHOO.util.Dom.get(addressFields.districtIdField).value = '';
		YAHOO.util.Dom.get(addressFields.districtNameField).value = '';
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		/*YAHOO.util.Dom.get(addressFields.stateIdField).value = '';
		YAHOO.util.Dom.get(addressFields.stateNameField).value = '';
		YAHOO.util.Dom.get(addressFields.countryIdField).value = '';
		document.getElementById(addressFields.countryNameField).textContent = '';*/
	});
}

// state autocomplete
function initStateAutoComplete(patientstatename,statedropdown,stateid, addressFields){
	var stateUrl = cpath+"/master/states/lookup.json?page_size=10&sort_order=state_name&contains=true";
	var dataSource = new YAHOO.util.DataSource(stateUrl);
	dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : "dtoList",
		fields: [{key: "state_name"},
				 {key: "state_id"},
				 {key: "country_name"},
				 {key: "country_id"}]
		};

	oAutoComp = new YAHOO.widget.AutoComplete(patientstatename,statedropdown,dataSource);


	oAutoComp.generateRequest = function(sQuery) {
		return getQueryParameters(sQuery, "state_id", addressFields);
	};

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 5;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList= false;

	oAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var areaDetails = oResultData;

		var details = areaDetails.state_name+"-"+areaDetails.country_name;
		return details;
	}

	oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var record = elItem[2];
		YAHOO.util.Dom.get(addressFields.areaIdField).value = '';
		YAHOO.util.Dom.get(addressFields.areaNameField).value = '';
		YAHOO.util.Dom.get(addressFields.cityIdField).value = '';
		YAHOO.util.Dom.get(addressFields.cityNameField).value = '';
		if(enableDistrict == 'Y') {
			YAHOO.util.Dom.get(addressFields.districtIdField).value = '';
			YAHOO.util.Dom.get(addressFields.districtNameField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		YAHOO.util.Dom.get(addressFields.stateIdField).value = record.state_id;
		YAHOO.util.Dom.get(addressFields.stateNameField).value = record.state_name;
		YAHOO.util.Dom.get(addressFields.countryIdField).value = record.country_id;
		document.getElementById(addressFields.countryNameField).textContent = record.country_name;
	});

	oAutoComp.selectionEnforceEvent.subscribe(function() {
		YAHOO.util.Dom.get(stateid).value = '';
	});

	oAutoComp.textboxBlurEvent.subscribe(function(oAutoComp) {
		if(YAHOO.util.Dom.get(addressFields.stateIdField).value) {
			return;
		}
		YAHOO.util.Dom.get(addressFields.areaIdField).value = '';
		YAHOO.util.Dom.get(addressFields.areaNameField).value = '';
		YAHOO.util.Dom.get(addressFields.cityIdField).value = '';
		YAHOO.util.Dom.get(addressFields.cityNameField).value = '';
		if(enableDistrict == 'Y') {
			YAHOO.util.Dom.get(addressFields.districtIdField).value = '';
			YAHOO.util.Dom.get(addressFields.districtNameField).value = '';
		}
		YAHOO.util.Dom.get(addressFields.stateIdField).value = '';
		YAHOO.util.Dom.get(addressFields.stateNameField).value = '';
		YAHOO.util.Dom.get(addressFields.countryIdField).value = '';
		document.getElementById(addressFields.countryNameField).textContent = '';
		if (addressFields.hasOwnProperty("referralDoctorIdField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
		if (addressFields.hasOwnProperty("referralDoctorField")) {
			YAHOO.util.Dom.get(addressFields.referralDoctorField).value = '';
		}
	});
}

function getQueryParameters(sQuery, filterLevel, addressFields) {
	var filterLevelArr = ["country_id","state_id","","city_id","area_id"];
	var filterLevelValuesArr = [addressFields.countryIdField, addressFields.stateIdField,
	                            addressFields.districtIdField,addressFields.cityIdField,
	                            addressFields.areaIdField]
	if(enableDistrict == 'Y') {
		filterLevelArr = ["country_id","state_id","district_id","city_id","area_id"];
	}
	var i=0;
	var queryParameters='';
	while((filterLevelArr[i] != filterLevel) && i < filterLevelArr.length) {
		if(filterLevelArr[i] == "") {
			i++;
			continue;
		}
		var fieldVal = YAHOO.util.Dom.get(filterLevelValuesArr[i]).value;
		if(fieldVal != null && fieldVal != '' && fieldVal != undefined) {
			queryParameters += '&'+filterLevelArr[i]+"="+fieldVal;  //Ex: queryParameters += '&country_id='+countryId;
		}
		i++;
	}
	return queryParameters+"&filterText="+sQuery;
}
