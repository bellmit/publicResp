
var searchForm;

function initSearch() {
	searchForm = document.searchForm;
	autoCountry();
	autoState();
	autoCity();
	autoArea();
    initMrNoAutoComplete(cpath);
	createToolbar(toolbar);
}

function clearSearch(){
	clearForm(searchForm);

	searchForm.status[0].checked = true;
	enableCheckGroupAll(searchForm.status[0]);
}

function autoCountry() {
	var localDs = new YAHOO.util.LocalDataSource(countryList);
	localDs.responseSchema = {fields : ["COUNTRY_NAME"]};
	var auto = new YAHOO.widget.AutoComplete('country_name', 'country_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

function autoState() {
	var localDs = new YAHOO.util.LocalDataSource(stateList);
	localDs.responseSchema = {fields : ["STATE_NAME"]};
	var auto = new YAHOO.widget.AutoComplete('state_name', 'state_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

function autoCity() {
	var localDs = new YAHOO.util.LocalDataSource(cityList);
	localDs.responseSchema = {fields : ["CITY_NAME"]};
	var auto = new YAHOO.widget.AutoComplete('city_name', 'city_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}

function autoArea() {
	var localDs = new YAHOO.util.LocalDataSource(areaList);
	localDs.responseSchema = {fields : ["PATIENT_AREA"]};
	var auto = new YAHOO.widget.AutoComplete('patient_area', 'area_dropdown', localDs);
	auto.allowBrowserAutocomplete = false;
	auto.typeAhead = true;
	auto.animVert = false;
	auto.minQueryLength = 0;
}