function init() {
	initCodesAutocomplete();
}
function validateTreatmentCount(){
	var selectedPblmStatus = document.getElementById('problemStatus').
			options[document.getElementById('problemStatus').selectedIndex].value;

	var treatmentCount = document.getElementById('treatmentCount').value;

	if (!empty(treatmentCount)) {
		treatmentCount = parseInt(treatmentCount);

		if (selectedPblmStatus == 'SM') {

			if (treatmentCount  < 1 || treatmentCount > 20) {
				alert('The treatment count should be between 1 to 20.');
				document.getElementById('treatmentCount').value = 1;
				document.getElementById('treatmentCount').focus();
				return false;
			}
		} else if (selectedPblmStatus == 'EI') {

			if (treatmentCount  < 1 || treatmentCount > 2) {
				alert('The treatment count should be between 1 to 2.');
				document.getElementById('treatmentCount').value = 1;
				document.getElementById('treatmentCount').focus();
				return false;
			}

		} else if (selectedPblmStatus == 'EW') {

			if (treatmentCount  < 1 || treatmentCount > 20) {
				alert('The treatment count should be between 1 to 20.');
				document.getElementById('treatmentCount').value = 1;
				document.getElementById('treatmentCount').focus();
				return false;
			}

		} else if (selectedPblmStatus == 'NN') {

			if (treatmentCount != 1) {
				alert('The treatment count should be 1 only.');
				document.getElementById('treatmentCount').value = 1;
				document.getElementById('treatmentCount').focus();
				return false;
			}

		} else if (selectedPblmStatus == 'NW') {

			if (treatmentCount  < 1 || treatmentCount > 20) {
				alert('The treatment count should be between 1 to 20.');
				document.getElementById('treatmentCount').value = 1;
				document.getElementById('treatmentCount').focus();
				return false;
			}

		}
		calcTreatmentCount();
	}

}

function calcTreatmentCount() {
	var treatmentCount = document.getElementById('treatmentCount').value;
//	var calcOptionsCount = document.getElementById('calcOptionsCount');
	var selectedPblmStatus = document.getElementById('problemStatus').
				options[document.getElementById('problemStatus').selectedIndex].value;
	var tempVal = 0;

	if (selectedPblmStatus == 'SM' || selectedPblmStatus == 'EI') {
		tempVal = 1*parseInt(treatmentCount);

	} else if (selectedPblmStatus == 'EW') {
		tempVal = 2*parseInt(treatmentCount);

	} else if (selectedPblmStatus == 'NN') {
		tempVal = 3*parseInt(treatmentCount);

	} else if (selectedPblmStatus == 'NW') {
		tempVal = 4*parseInt(treatmentCount);

	}

	document.getElementById('calcOptionsCount').innerHTML = tempVal > 4 ? 4 : tempVal;
	document.getElementById('calcTreatmentOptionsCount').value = tempVal > 4 ? 4 : tempVal;
}

function calcComplexityCount(obj) {
	//var complexityCount = document.getElementById('complexityCount').textContent;
	var complexityCount = 0;
	var complexityList = YAHOO.util.Dom.getElementsByClassName('complexity');
	var totalComplexityCount = 0;

	for (var i=0; i<complexityList.length; i++) {

		if (complexityList[i].checked) {
			complexityCount = complexityCount + parseInt(complexityList[i].value);
		}
	}

	document.getElementById('complexityCount').innerHTML = complexityCount > 4 ? 4 : complexityCount;
	document.getElementById('complexityCountHiddenVal').value = complexityCount > 4 ? 4 : complexityCount;
}

function clearMdmAndEmCode() {
	document.getElementById('mdmValLabel').innerHTML = '';
	document.getElementById('mdmHiddenVal').value = '';
	document.getElementById('eandmcode').innerHTML = '';
	document.getElementById('eandmCodeHiddenVal').value = '';
	document.getElementById('consultation_type_id').value = '';
	document.getElementById('finalize_n_update').checked = false;
	document.getElementById('finalize_n_update').disabled = false;
	document.getElementById('item_code').value = '';
	document.getElementById('remarks').value = ''
	document.getElementById('item_code').disabled = true;
	document.getElementById('remarks').disabled = true;
	document.getElementById('h_finalize_n_update').value = 'false';
}

function calcMdmAndEmcode() {
	if (document.getElementById('visitTypes').value == '') {
		alert("Please select the visit type");
		document.getElementById('visitTypes').focus();
		return false;
	}
	if (document.getElementById('hpiCount').value == '0' || document.getElementById('hpiCount').value == '') {
		alert("HPI Count is not available.");
		return false;
	}
	if (document.getElementById('peCount').value == '0' || document.getElementById('peCount').value == '') {
		alert("Physical Examination Count is not available.");
		return false;
	}
	if (document.getElementById('problemStatus').value == '') {
		alert("Please select the Problem Status");
		document.getElementById('problemStatus').focus();
		return false;
	}
	if (document.getElementById('treatmentCount').value == '') {
		alert('Treatment Options Count should not be empty.');
		document.eandmscreen.treatmentCount.focus();
		return false;
	}
	if (document.getElementById('risk').value == '') {
		alert("Please select the 'Risk of complications,Morbidity and/ mortality'");
		document.getElementById('risk').focus();
		return false;
	}
	var optionsCount = document.getElementById('calcOptionsCount').textContent;
	var complexityCount = document.getElementById('complexityCount').textContent;
	var riskValue = document.getElementById('risk').options[document.getElementById('risk').selectedIndex].value;
	var mdmIntVal = null;
	if (empty(optionsCount)) {

		alert('Treatment options count is not available');
		return false;
	} else if ( empty(complexityCount) || complexityCount == 0) {

		alert('Complexity count is not available');
		return false;
	} else {
		var totalVal = parseInt(optionsCount) + parseInt(complexityCount) + parseInt(riskValue);
		var maxVal = Math.max(optionsCount, complexityCount, riskValue);
		var minVal = Math.min(optionsCount, complexityCount, riskValue);
		var secondMaxVal = parseInt(totalVal) - (parseInt(maxVal) + parseInt(minVal));
		mdmIntVal = Math.min(maxVal, secondMaxVal);
		document.getElementById('mdmValLabel').innerHTML = getmdmValue(mdmIntVal);
		document.getElementById('mdmHiddenVal').value = mdmIntVal;
	}
	getEMcode();
	getConsultationType(document.getElementById('eandmCodeHiddenVal').value);

	return true;
}

function setConsultationType(obj) {
	document.getElementById('consultation_type_id').value = document.getElementById('consult_type_drop').value;
}

function getConsultationType(eAndMCode) {
	var consultTypes = filterList2(consulItemCodes, "item_code", eAndMCode,
		"org_id", document.getElementById('org_id').value);
    consultTypes = removeDuplicates(consultTypes);
    if (consultTypes.length == 0) {
    	document.getElementById('consultTypeLabelDiv').style.display = 'none';
   		document.getElementById('consultTypeDropDiv').style.display = 'none';
   		document.getElementById('consultation_type_id').value = '';
    	alert("Consultation type not found for the E&M Code. Code not applicable, Please change.");
    	return false;
    } else {
    	if (consultTypes.length == 1) {
    		document.getElementById('consultTypeLabelDiv').style.display = 'block';
        	document.getElementById('consultTypeDropDiv').style.display = 'none';
			document.getElementById('consultation_type_id').value = consultTypes[0].consultation_type_id;
			document.getElementById('consult_type_label').textContent = consultTypes[0].consultation_type;
    	} else {
    		document.getElementById('consultTypeLabelDiv').style.display = 'none';
       		document.getElementById('consultTypeDropDiv').style.display = 'block';
    		loadSelectBox(document.getElementById('consult_type_drop'), consultTypes, 'consultation_type', 'consultation_type_id', '-- Select --', '');
    		document.getElementById('consult_type_drop').value = document.getElementById('base_consultation_type_id').value;
    	}
    }
}

var mdmMap = {1:'Straight Forward', 2:'Low', 3:'Moderate', 4:'High'};

function getmdmValue(mdmNo) {

	for(key in mdmMap) {
		if (key == mdmNo) {
			return mdmMap[key];
		}
	}
}

function saveEandMValues() {
	var calculatedEnMCode = document.getElementById('eandmCodeHiddenVal').value;
	var editableEnMCode = document.getElementById('item_code').value;
	var finalizeNUpdate = document.getElementById('finalize_n_update');
	var consultationStatus = document.getElementById('consultationStatus').value;
	var billStatus = document.getElementById('billStatus').value;

	if (finalizeNUpdate.checked) {
		if (consultationStatus != 'C') {
			alert('Please close consultation before updating codification E&M code');
			return false;
		}
		if (billStatus != 'A') {
			alert('Bill is not in Open Status. Cannot update E&M code');
			return false;
		}
		if (editableEnMCode == '' && calculatedEnMCode == '') {
			alert('Please enter/calculate the E&M Code and then Save');
			return false;
		}
	} else if (calculatedEnMCode == '') {
		alert('Please calculate the E&M Code and then Save ');
		return false;
	}

	if (document.getElementById('consultation_type_id').value == '') {
		if (document.getElementById('consult_type_drop').length > 0) {
			alert("Please select the consultation type");
		} else {
			alert("Code not applicable. Please change.");
		}
		return false;
	}

	document.getElementById('_method').value = 'saveEandMvalues';
	var form = document.eandmscreen;
	form.setAttribute('method',  'POST');
	document.eandmscreen.submit();
	return true;
}

function validate(print) {
	var mrNo = document.getElementById('mrNo').value;
	document.eandmscreen.print.value=print;
	setTimeout("saveEandMValues()", 1000);
}

function getEMcode() {
	var req = null;
	var emcode = null;
	var mdmValue = document.getElementById('mdmValLabel').innerHTML;

	var visitType = document.getElementById('visitTypes').value;
	var hpiCount = document.getElementById('hpiCount').value;
	var rosCount = document.getElementById('rosCount').value;
	var pfshCount = document.getElementById('pfshCount').value;
	var peCount = document.getElementById('peCount').value;

	var url = cpath+"/eandmcalculator.do?_method=getEMcode&hpiCount="+hpiCount+
					"&rosCount="+rosCount+"&pfshCount="+pfshCount+"&peCount="+peCount+
					"&mdm="+mdmValue+"&visitType="+visitType;

	if(window.XMLHttpRequest) {
		req = new XMLHttpRequest();
	}
	else if(window.ActiveXObject) {
		req = new ActiveXObject("MSXML2.XMLHTTP");
	}
	req.open("GET", url, false);
	req.setRequestHeader("Content-Type", "text/plain");
	req.send(null);
	document.getElementById('eandmcode').innerHTML = req.responseText;
	document.getElementById('eandmCodeHiddenVal').value = req.responseText;
}

var sort_by = function(field, reverse, primer){
   var key = function (x) {return primer ? primer(x[field]) : x[field]};
   return function (a,b) {
       var A = key(a), B = key(b);
       return (A < B ? -1 : (A > B ? 1 : 0)) * [1,-1][+!!reverse];
   }
}

function removeDuplicates(consultTypes) {
	consultTypes.sort(sort_by('consultation_type', false, function(x){return x.toUpperCase()}))
	for (var i=consultTypes.length-1; i>0; i--) {
		var record1 = consultTypes[i];
		var record2 = consultTypes[i-1];
		if (record1.consultation_type == record2.consultation_type) {
			consultTypes.splice(i-1, 1);
		}
	}
	return consultTypes;
}

function initCodesAutocomplete() {
    var dataSource = new YAHOO.util.XHRDataSource(cpath + "/pages/medicalrecorddepartment/MRDUpdate.do");
    var queryParams = "_method=getCodesListOfCodeType&codeType=" + encodeURIComponent(consultationSupportedCodeType);
    queryParams += (consultationSupportedCodeType != 'IR-DRG') ? "" : '&patientType='+document.getElementById('patientType').value;
    queryParams += '&dialog_type=consultation';
    dataSource.scriptQueryAppend = queryParams;
    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    dataSource.responseSchema = {
        resultsList: "result",
        fields: [
        	{key: "code"},
        	{key: "icd"},
        	{key: "code_desc"}
        ]
    };
    var oAutoComp = new YAHOO.widget.AutoComplete('item_code', 'itemCodeContainer', dataSource);
    oAutoComp.minQueryLength = 1;
    oAutoComp.forceSelection = true;
    oAutoComp.allowBrowserAutocomplete = false;
    oAutoComp.resultTypeList = false;
    oAutoComp.maxResultsDisplayed = 50;
    oAutoComp.forceSelection = true;
    var reArray = [];
    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
        var escapedComp = Insta.escape(sQuery);
        reArray[0] = new RegExp('^' + escapedComp, 'i');
        reArray[1] = new RegExp("\\s" + escapedComp, 'i');
        var det = highlight(oResultData.code + ' / ' + oResultData.code_desc, reArray);
        return det;
    };
    oAutoComp.setHeader(' Code / Description ');
    oAutoComp.itemSelectEvent.subscribe(function (sType, aArgs) {
        getConsultationType(aArgs[2].code)
    });

    if (oAutoComp._elTextbox.value != '') {
		oAutoComp._bItemSelected = true;
		oAutoComp._sInitInputValue = oAutoComp._elTextbox.value;
	}
    return oAutoComp;
}

function matches(mName, autocomplete) {
    var elListItem = autocomplete._elList.childNodes[0];
    elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
    return elListItem;
}

function enableFields(obj) {
	if (obj.checked) {
		document.getElementById('item_code').disabled = false;
		document.getElementById('remarks').disabled = false;
		document.getElementById('item_code').value = '';
		document.getElementById('remarks').value = '';
		document.getElementById('h_finalize_n_update').value = 'true';
		var code = document.getElementById('item_code').value;
		if (code != '')
			getConsultationType(code)
	} else {
		document.getElementById('item_code').disabled = true;
		document.getElementById('remarks').disabled = true;
		document.getElementById('h_finalize_n_update').value = 'false';
		var code = document.getElementById('eandmCodeHiddenVal').value;
		if (code != '')
			getConsultationType(code);
	}
}