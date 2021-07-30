
var searchForm ;
function init(){
 searchForm=document.patientSearchForm;
initMrNoAutoComplete(cpath);
 autoArea();
 autoCountry();
 autoState();
 autoCity();
 onChangeRegField();
}


function clearSearch(){
	searchForm.fdate.value = "";
	searchForm.tdate.value = "";
	searchForm.firstName.value = "";
	searchForm.lastName.value = "";
	searchForm.phone.value = "";
	searchForm.department.options.selectedIndex = -1;
	searchForm.doctor.options.selectedIndex = -1;
	searchForm.mrno.value="";
	searchForm.country.value="";
	searchForm.patientstate.value="";
	searchForm.patientcity.value="";
	searchForm.patientarea.value="";

	if (searchForm.disfdate != undefined) {
		searchForm.disfdate.value = '';
	}
	if (searchForm.distdate != undefined) {
		searchForm.distdate.value = '';
	}
}

function validateSearchForm(){

	for(var j = 0;j<countryList.length;j++){
		if (searchForm.country.value == countryList[j].COUNTRY_NAME){
			searchForm.countryid.value = countryList[j].COUNTRY_ID;
		}
	}

	for(var j = 0;j<stateList.length;j++){
		if (searchForm.patientstate.value == stateList[j].STATE_NAME){
			searchForm.stateid.value = stateList[j].STATE_ID;
		}
	}

	for(var j = 0;j<cityList.length;j++){
		if (searchForm.patientcity.value == cityList[j].CITY_NAME){
			searchForm.cityid.value = cityList[j].CITY_ID;
		}
	}
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
	if(searchForm.disfdate && searchForm.distdate) {
		if(!doValidateDateField(searchForm.disfdate)){
			return false;
		}
		if(!doValidateDateField(searchForm.distdate)){
			return false;
		}
	}
	return true;
}

function checkMrno(){
}

function funreset(){
      //  document.getElementById("mrno").value="";
}


function resetAll() {
	document.patientSearchForm.firstName.value = "";
	document.patientSearchForm.lastName.value = "";
	document.patientSearchForm.phone.value = "";
}


/*complete the MRNO
 */
function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox = searchForm.mrno;

//	complete
		var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		searchForm.mrno.value = ""
			setTimeout("searchForm.mrno.focus()",0);
		return false;
	}
	return true;
}
function checkForDisRole(anchor,status,roleId){

	var href1 = anchor.getAttribute("href");

	if (status== "I" && roleId == '') {
		anchor.setAttribute("href", "#");
		alert("No Permission to Edit Inactive Patient's Discharge Summary");
		return false;
	}else{
		anchor.setAttribute("href", href1);
	}
}

function funGetSortUrl(sortOrder,sortReverse){
	var form  = document.patientSearchForm;
	form.sortOrder.value  = sortOrder;
	form.sortReverse.value =sortReverse;
	document.patientSearchForm.submit();
}


function autoArea(){
	var selectedCity;
	for(var j = 0;j<cityList.length;j++){
		if (searchForm.patient_city.value == cityList[j].CITY_NAME){
			selectedCity = cityList[j].CITY_ID;
		}
	}

YAHOO.example.areaArray = [];
		var i=0;
		for(var j = 0;j<areaListmain.length;j++){
			if (selectedCity == areaListmain[j].PATIENT_CITY){
				YAHOO.example.areaArray.length = i+1;
				YAHOO.example.areaArray[i] = areaListmain[j].PATIENT_AREA;
				i++;
			}

		}

	YAHOO.example.ACJSArray = new function() {
		// Instantiate first JS Array DataSource
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.areaArray);
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('patientarea','areacontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('patient_area').value;
			if(sInputValue.length === 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
			}
		});
	}
}

function autoCountry(){

YAHOO.example.countryArray = [];
		var i=0;
		for(var j = 0;j<countryList.length;j++){

			YAHOO.example.countryArray.length = i+1;
			YAHOO.example.countryArray[i] = countryList[j].COUNTRY_NAME;
			i++;

		}

	YAHOO.example.ACJSArray = new function() {
		// Instantiate first JS Array DataSource
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.countryArray);
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('country','countrycontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('country').value;
			if(sInputValue.length === 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
			}
		});
	}
}




function autoState(){

YAHOO.example.stateArray = [];
		var i=0;
		for(var j = 0;j<stateList.length;j++){
			YAHOO.example.stateArray.length = i+1;
			YAHOO.example.stateArray[i] = stateList[j].STATE_NAME;
			i++;

		}

	YAHOO.example.ACJSArray = new function() {
		// Instantiate first JS Array DataSource
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.stateArray);
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('patientstate','statecontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('patientstate').value;
			if(sInputValue.length === 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
			}
		});
	}
}


function autoCity(){

var selectedState;

	for(var j = 0;j<stateList.length;j++){
		if ( searchForm.patientstate.value == stateList[j].STATE_NAME ){
				selectedState = stateList[j].STATE_ID;
		}
	}

YAHOO.example.cityArray = [];
		var i=0;
		for(var j = 0;j<cityList.length;j++){
			if (selectedState == cityList[j].STATE_ID){
				YAHOO.example.cityArray.length = i+1;
				YAHOO.example.cityArray[i] = cityList[j].CITY_NAME;
				i++;
			}
		}

	YAHOO.example.ACJSArray = new function() {
		// Instantiate first JS Array DataSource
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.cityArray);
			// Instantiate first AutoComplete
		this.oAutoComp = new YAHOO.widget.AutoComplete('patientcity','citycontainer', datasource);
		this.oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		this.oAutoComp.typeAhead = true;
		this.oAutoComp.useShadow = true;
		this.oAutoComp.minQueryLength = 0;
		this.oAutoComp.autoHighlight = false;
		this.oAutoComp.allowBrowserAutocomplete = false;
		this.oAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('patientcity').value;
			if(sInputValue.length === 0) {
						var oSelf = this;
						setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
			}
		});
	}
}

function onChangeRegField() {
	var regFieldNameObj = document.patientSearchForm.regFieldName;
	var regFieldValueObj = document.patientSearchForm.regFieldValue;
	if(regFieldNameObj != null && regFieldValueObj != null) {
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
}
