function ValidateSupplierMaster(){
	 document.supplierForm.supplier_name.value=trim(document.supplierForm.supplier_name.value);
	 if(trimAll(document.supplierForm.supplier_name.value)==""){
	     alert("Supplier Name Should Not Be Empty");
	     document.supplierForm.supplier_name.value="";
		 document.supplierForm.supplier_name.focus();
		 return false;
	 } else if(document.supplierForm.cust_supplier_code.value.length > 20){
		 alert("Supplier Code cannot be more than 20 characters");
		 document.supplierForm.cust_supplier_code.value="";
		 document.supplierForm.cust_supplier_code.focus();
		 return false;
	 } else if(document.supplierForm.credit_period.value=="") {
	  	alert("Credit Period should not be empty");
	  	document.supplierForm.credit_period.focus();
	  	return false;
	 } else if(document.supplierForm.is_registered.value=="") {
	  	alert("Registered Supplier should not be empty");
	  	document.supplierForm.is_registered.focus();
	  	return false;
	} else if(document.supplierForm.is_registered.value == 'Y' && document.supplierForm.supplier_tin_no.value == "") {
    	alert("TIN number is required.");
    	document.supplierForm.supplier_tin_no.focus();
    	return false;  	
    } else if(document.supplierForm.is_registered.value == 'Y' && document.supplierForm.supplier_state.value == "") {
    	alert("State is required");
    	document.supplierForm.supplier_state.focus();
    	return false;  	
    } else {
     	document.supplierForm.action="suppdetails.do?_method=saveSupplierDetails";
    	document.supplierForm.submit();
        return true;
    }
}
function chk(e){
	var key=0;
  	if(window.event || !e.which) {
		key = e.keyCode;
	} else {
		key = e.which;
	}
    if(document.supplierForm.supplier_address.value.length<500 || key==8) {
		key=key;
		return true;
	} else {
		key=0;
		return false;
	}
}

function chklen(){
	if(document.supplierForm.supplier_address.value.length > 500){
		alert("Address should be 500 characters only");
	  	var s = document.supplierForm.supplier_address.value;
	  	s = s.substring(0,500);
	  	document.supplierForm.supplier_address.value = s;
	}
}

function checkmail() {
    var x = document.forms["supplierForm"]["supplier_mailid"].value;
    var atpos = x.indexOf("@");
    var dotpos = x.lastIndexOf(".");
    if (atpos<1 || dotpos<atpos+2 || dotpos+2>=x.length) {
        alert("Not a valid e-mail address");
        document.supplierForm.supplier_mailid.value="";
        document.supplierForm.supplier_mailid.focus();
        return false;
    }
}
function setMandotoryspan() {
	if(document.supplierForm.is_registered.value == 'Y') {
		document.getElementById('tin_mandatory').style.display = 'inline-block';
		document.getElementById('state_mandatory').style.display = 'inline-block';
	}
	else {
		document.getElementById('tin_mandatory').style.display = 'none';
		document.getElementById('state_mandatory').style.display = 'none';
	}	
		
	
}
function init() {
	initAutoCityStateCountry("supplier_city","city_state_country_dropdown", "city_id");
	initSupplierAutoComplete("supplier_state","state_dropdown", "state_id");
	setState();
	setIsRegisteredField();
	setTcsApplicableField();
 }

function setIsRegisteredField() {
	if((isRegistered == null || isRegistered.trim() == '') && (isEdit == 'true' || isEdit == true)) {
		//Set --select--
		document.supplierForm.is_registered.selectedIndex = 0;//2;
	} else if(isRegistered.trim() == 'Y' || (isEdit == 'false' || isEdit == false)) {
		//Set Yes
		document.supplierForm.is_registered.selectedIndex = 1;//0;
		setMandotoryspan();
	} else if(isRegistered.trim() == 'N'){
		// Set No
		document.supplierForm.is_registered.selectedIndex = 2;//1;
	}
	
}

function setTcsApplicableField() {
	if(isTcsApplicable == null || isTcsApplicable.trim() == '' || isTcsApplicable.trim() == 'N') {
		document.supplierForm.tcs_applicable.selectedIndex = 1;
	} else {
		document.supplierForm.tcs_applicable.selectedIndex = 0;
	}
}

function setState() {
	var isStateExist = false;
	for (var i = 0; i < cityStateCountryJSON.length; i++) {
		if (cityStateCountryJSON[i].state_name == document.supplierForm.supplier_state.value ) {
			isStateExist = true;
		}
	}
	if(!isStateExist)
		document.supplierForm.supplier_state.value = '';
}

function initAutoCityStateCountry(patientcityname,citydropdown,suppliercityid) {
		var cityAuthJson = {result:cityStateCountryJSON};
		dataSource  = new YAHOO.util.LocalDataSource(cityAuthJson, { queryMatchContains : true } );
		dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
		dataSource.responseSchema = {
			resultsList : "result",
			fields: [{key: "city_state_country_name"},
					 {key: "city_name"},
					 {key: "city_id"},
					 {key: "state_name"},
					 {key: "state_id"},
					 {key: "country_name"}]
			};

		oAutoComp = new YAHOO.widget.AutoComplete(patientcityname, citydropdown, dataSource);

		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.maxResultsDisplayed = 20;
		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = false;
		oAutoComp.resultTypeList= false;
		oAutoComp._bItemSelected = true;

		oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
			var record = elItem[2];
			YAHOO.util.Dom.get(patientcityname).value = record.city_name;
			YAHOO.util.Dom.get(suppliercityid).value = record.city_id;
			//if(!isNotNullValue('supplier_state')) {
				document.getElementById("supplier_state").value = record.state_name;
				document.getElementById("state_id").value = record.state_id;
			//}
			//if(!isNotNullValue('supplier_country')) {
				document.getElementById("supplier_country").value = record.country_name;
			//}
		});
		oAutoComp.selectionEnforceEvent.subscribe(function() {
			YAHOO.util.Dom.get(suppliercityid).value = '';
		});
		oAutoComp.textboxBlurEvent.subscribe(function(oAutoComp){
			var index=true;
			for (var i=0;i<cityStateCountryJSON.length;i++) {
				var record=cityStateCountryJSON[i];
					if (record["city_name"] == YAHOO.util.Dom.get('supplier_city').value && YAHOO.util.Dom.get('city_id').value == record["city_id"]) {
						index=false;
						break;
					}
			}
			if(index) {
				YAHOO.util.Dom.get('city_id').value = '';
				YAHOO.util.Dom.get('state_id').value = '';
				//YAHOO.util.Dom.get('supplier_state').value = '';
				//document.getElementById("supplier_country").value = '';
			}
		});
	}

function initSupplierAutoComplete(stateName, stateDropdown, stateId) {
    var dataSource = new YAHOO.widget.DS_JSArray(stateJSON);
	dataSource.responseSchema = {
			resultsList : "result",
			fields: [{key: "STATE_NAME"},
					 {key: "STATE_ID"}]
			};

    stateAutoComp = new YAHOO.widget.AutoComplete('supplier_state', 'state_dropdown', dataSource);
    stateAutoComp.maxResultsDisplayed = 5;
    stateAutoComp.allowBrowserAutocomplete = false;
    stateAutoComp.prehighlightClassName = "yui-ac-prehighlight";
    stateAutoComp.typeAhead = false;
    stateAutoComp.animVert = false;
    stateAutoComp.minQueryLength = 0;
    stateAutoComp.forceSelection = true;
    stateAutoComp._bItemSelected = true;

    stateAutoComp.itemSelectEvent.subscribe(function (oSelf, elItem){
    	var record = elItem[2];
    	YAHOO.util.Dom.get(stateName).value = record[0];
		YAHOO.util.Dom.get(stateId).value = record[1];
    	var country = findInList(stateCountryJSON,"STATE_ID",document.getElementById("state_id").value);
		document.getElementById("supplier_country").value = country.COUNTRY_NAME;
		document.getElementById("state_id").value = country.STATE_ID;
    });
}
function changeCountry() {
	document.getElementById("supplier_country").value = '';
	document.getElementById("state_id").value = '';
}
