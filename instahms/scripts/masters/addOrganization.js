function populateBaseOrgNames(){
		document.forms[0].orgName.focus();
		AutoComplete();

	}
function AutoComplete(){

	YAHOO.example.orgNamesArray = [];
	YAHOO.example.orgNamesArray.length =orgNames.length;

	for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
				YAHOO.example.orgNamesArray[i] = item["ORG_NAME"];
	}

	YAHOO.example.ACJSArray = new function(){
				  // Instantiate first JS Array DataSource
				  datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.orgNamesArray);
				  // Instantiate first AutoComplete
				  var autoComp = new YAHOO.widget.AutoComplete('baseOrgName','orgContainer', datasource);
				  autoComp.prehighlightClassName = "yui-ac-prehighlight";
				  autoComp.typeAhead = true;
				  // Enable a drop-shadow under the container element
				  autoComp.useShadow = true;
				  // Disable the browser's built-in autocomplete caching mechanism
				  autoComp.allowBrowserAutocomplete = false;
				  // Require user to type at least 0 characters before triggering a query
				  autoComp.minQueryLength = 0;
				  //commas and/or spaces may delimited queries
				  //autoComp.delimChar = [];
				  // Display up to 20 results in the container
				  autoComp.maxResultsDisplayed = 20;
				  // Do not automatically highlight the first result item in the container
				  autoComp.autoHighlight = false;
				  // disable force selection,user can type his/her own complaint(which is not there in master)
				  autoComp.forceSelection = true;
				  autoComp.textboxFocusEvent.subscribe(function(){
					   var sInputValue = YAHOO.util.Dom.get('baseOrgName').value;
					   if(sInputValue.length === 0) {
						         var oSelf = this;
						         setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					  }

			     });

			     //autoComp.dataReturnEvent.subscribe(populateOrgId);
			}

}


function validate(){

	var form = document.OMasterForm

	form.orgName.value = trim(form.orgName.value);
	if(form.orgName.value == ''){
		alert("organization name is required");
		form.orgName.focus();
		return false;
	}

	if(form.address.value.length > 100){
		alert("Address length cannot be greater than 100 characters");
		form.address.focus();
		return false;
	}

	if (document.OMasterForm.hasDateValidity.checked){
		if (document.OMasterForm.fromDate.value == ''){
			alert("From Date is required");
			return false;
		}
		if (document.OMasterForm.toDate.value == ''){
			alert("To Date is required");
			return false;
		}
	}

	if(form._method.value == 'saveNewOrganization'){
		/*if(form.baseOrgId.selectedIndex == 0){
			alert('base orgname is required for applying charges for new organization');
			form.baseOrgId.focus();
			return false;
		}*/
	}else if(form._method.value == 'updateOrgDetails'){
			if(form.editOrgId.value == 'ORG0001'){
				if(form.status.value == 'I'){
					alert("'GENERAL' Rate Plan can not be deactivated");
					return false;
				}
				if(form.orgName.value != 'GENERAL'){
					alert("'GENERAL' Rate Plan name can not be changed");
					return false;
				}
			}
			/*if(form.varianceBy.value != '' || form.varianceValue.value != '') {
				if(form.baseOrgId.selectedIndex == 0) {
					alert('base orgname is required for applying charges for new organization');
					form.baseOrgId.focus();
					return false;
				}
			}*/
	}
}


function validateDiscount() {
	var discPerc = isNaN(document.OMasterForm.discperc.value)?0: document.OMasterForm.discperc.value;
	if(discPerc >100){
		alert("Percentage cannot be more than 100...");
		document.OMasterForm.discperc.value =0;
		document.OMasterForm.discperc.focus();
	}
	return;
}

function enableDateValidity(){
	if (document.OMasterForm.hasDateValidity.checked){
		document.getElementById('dateValidDiv').style.visibility = 'visible';
	}else{
		document.getElementById('dateValidDiv').style.visibility = 'hidden';
	}
}
function validateDates(){
	document.getElementsByName('orgName')[0].value = trim(document.getElementsByName('orgName')[0].value);
	if (document.OMasterForm.hasDateValidity.checked){
		return validateFromToDate(document.OMasterForm.fromDate, document.OMasterForm.toDate);
	}else {
	return true;
	}
}

function doClose() {
	window.location.href = cpath + '/pages/masters/hosp/admin/Organ.do?_method=getOrganizationDetails&status=A&sortOrder=org_name&sortReverse=false';
}

/*function setRateVariationText() {
	var basePlan = getSelText(document.forms[0].baseOrgId);
	var type = document.forms[0].variaceType.value;
	var perc = document.forms[0].varianceBy.value;
	var amount = document.forms[0].varianceValue.value;
	var text = basePlan + ' ';
	if (type == 'Incr') {
		text = text + '+' + ' ';
	} else {
		text = text + '-' + ' ';
	}
	if (perc != '') {
		text = text + perc + '%';
	} else {
		text = text + amount;
	}

	document.forms[0].rateVariation.value = text;
}*/

