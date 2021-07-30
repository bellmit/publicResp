
var searchForm ;
function init(){
 searchForm=document.patientSearchForm;
 if(dashboard != 'register'){
 enablePatientStatus();
 }
 enablePatientVisit();
 enablePatientType();
 autoArea();
 autoCountry();
 autoState();
 autoCity();
}


function clearSearch(){
	searchForm.fdate.value = "";
	searchForm.tdate.value = "";
	searchForm.firstName.value = "";
	searchForm.lastName.value = "";
	searchForm.phone.value = "";
	searchForm.department.value = "";
	searchForm.doctor.value = "";
	searchForm.mrno.value="";
}

function enablePatientType(){
	var disabled = searchForm.typeAll.checked;
	searchForm.typeIP.disabled = disabled;
	searchForm.typeOP.disabled = disabled;
}

function enablePatientStatus(){
	var disabled = searchForm.statusAll.checked;
	searchForm.statusActive.disabled = disabled;
	searchForm.statusInactive.disabled = disabled;

}
function enablePatientVisit(){
	var disabled = searchForm.visitAll.checked;
	searchForm.visitNew.disabled = disabled;
	searchForm.visitRevisit.disabled = disabled;
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

	if(!doValidateDateField(searchForm.fdate)){
		return false;
	}
	if(!doValidateDateField(searchForm.tdate)){
		return false;
	}
	if(!doValidateDateField(searchForm.disfdate)){
		return false;
	}
	if(!doValidateDateField(searchForm.distdate)){
		return false;
	}
	document.patientSearchForm.startPage.value ="";
	document.patientSearchForm.endPage.value ="";
	document.patientSearchForm.pageNum.value ="";
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
function getNextPage(startPage,endPage){
	var form  = document.patientSearchForm;
	form.startPage.value = parseInt(endPage) + 1 ;
	form.endPage.value = parseInt(endPage) + 10;
	form.pageNum.value = parseInt(endPage) + 1 ;
	document.patientSearchForm.submit();
}


function getPrevPage(startPage,endPage){
	var form  = document.patientSearchForm;
	form.startPage.value = parseInt(startPage) - 10;
	form.endPage.value = parseInt(form.startPage.value) + 9;
	form.pageNum.value = parseInt(startPage) - 10;
	document.patientSearchForm.submit();
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
		if (searchForm.patientcity.value == cityList[j].CITY_NAME){
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
			var sInputValue = YAHOO.util.Dom.get('patientarea').value;
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
