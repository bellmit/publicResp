var patternAutoComp;
function initPatternIdAutoComplete() {
	var dataSource = new YAHOO.util.LocalDataSource({
		result : patternList
	});
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : "result",
		fields : [ {
			key : "pattern_id"
		}, {
			key : "pattern_id"
		} ]
	};
	var patternAutoComp = new YAHOO.widget.AutoComplete('pattern_id',
			'patternIdContatiner', dataSource);
	patternAutoComp.minQueryLength = 0;
	patternAutoComp.maxResultsDisplayed = 20;
	patternAutoComp.forceSelection = true;
	patternAutoComp.animVert = false;
	patternAutoComp.resultTypeList = false;
	patternAutoComp.typeAhead = false;
	patternAutoComp.allowBroserAutocomplete = false;
	patternAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	patternAutoComp.autoHighlight = true;
	patternAutoComp.useShadow = false;
	patternAutoComp.itemSelectEvent.subscribe(setHospIdPatternDetails);
}

function setHospIdPatternDetails(type, args) {
	var patternId = args[2].pattern_id;
	var ajaxReqObject = newXMLHttpRequest();
	var url = cpath
			+ "/sequences/hospitalidpatterns/hospidpattern.json?_method=getHospitalIdPatternDetail&pattern_id="
			+ patternId;
	ajaxReqObject.open("GET", url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ((ajaxReqObject.status == 200)
				&& (ajaxReqObject.responseText != null)) {
			eval("hospIdPatternDetail = " + ajaxReqObject.responseText)
			fillHospIdPatternDetails(hospIdPatternDetail.pattern_details);
		}
	}
}

function fillHospIdPatternDetails(patternDetails) {
	document.getElementById("std_prefix").value = patternDetails.std_prefix;
	document.getElementById("sequence_name").value = patternDetails.sequence_name;
	document.getElementById("num_pattern").textContent = patternDetails.num_pattern;
	document.getElementById("sequence_reset_freq").disabled = false;
	document.getElementById("sequence_reset_freq").value = patternDetails.sequence_reset_freq;
	document.getElementById("sequence_reset_freq").disabled = true;
	document.getElementById("date_prefix_pattern").textContent = patternDetails.date_prefix_pattern;
	document.getElementById("date_prefix").textContent = patternDetails.date_prefix;
}

function setDatePrefix() {
	var datePrefix = '';
	var format = document.getElementById("date_prefix_pattern").value;
	if (format != '') {
		datePrefix = getDatePrefixWithoutSeperator(format);
	}
	document.getElementById("lbl_date_prefix").innerHTML = datePrefix;
	document.getElementById("date_prefix").value = datePrefix;
}

function getDatePrefixWithoutSeperator(format) {
	var dateObj = new Date();
	var year = dateObj.getFullYear();
	var monthIndex = dateObj.getMonth();
	var month = monthIndex + 1;
	var day = dateObj.getDate();
	if (("" + month).length == 1) {
		month = "0" + month;
	}
	if (("" + day).length == 1) {
		day = "0" + day;
	}
	if (format == "DDMMYY") {
		return day.toString() + month.toString() + year.toString().substr(2, 4);
	} else if (format == "MMDDYY") {
		return month.toString() + day.toString() + year.toString().substr(2, 4);
	} else if (format == "DDYYMM") {
		return day.toString() + year.toString().substr(2, 4) + month;
	} else if (format == "MMYYDD") {
		return month.toString() + year.toString().substr(2, 4) + day.toString();
	} else if (format == "YYMMDD") {
		return year.toString().substr(2, 4) + month.toString() + day.toString();
	} else if (format == "YYDDMM") {
		return year.toString().substr(2, 4) + day.toString() + month.toString();
	} else if (format == "MMYY") {
		return month.toString() + year.toString().substr(2, 4);
	} else if (format == "MMYYYY") {
		return month.toString() + year.toString();
	} else if (format == "YYMM") {
		return year.toString().substr(2, 4) + month.toString();
	} else if (format == "YYYYMM") {
		return year.toString() + month.toString();
	} else if (format == "YY") {
		return year.toString().substr(2, 4);
	} else if (format == "YYYY") {
		return year;
	}
}

function validateHospitalIdPatternForm() {
	var patternId = document.hospitalidpatternsform.pattern_id.value;
	var sequenceName = document.hospitalidpatternsform.sequence_name.value;
	var numPattern = document.hospitalidpatternsform.num_pattern.value;
	var seqResetFreq = document.hospitalidpatternsform.sequence_reset_freq.value;
	var transactionType = document.hospitalidpatternsform.transaction_type.value;
	
	if(patternId == '') {
		alert('Please enter Pattern Id.');
		document.hospitalidpatternsform.pattern_id.focus();
		return false;
	}
	if(sequenceName == ''){
		alert('Please enter Sequence Name.');
		document.hospitalidpatternsform.sequence_name.focus();
		return false;
	}
	if (seqResetFreq != '') {
		if (document.hospitalidpatternsform.date_prefix_pattern.value == '') {
			alert('Please select Date Prefix Pattern.');
			document.hospitalidpatternsform.date_prefix_pattern.focus();
			return false;
		}
	}
	
	if(numPattern == ''){
		alert('Please enter Number Pattern.');
		document.hospitalidpatternsform.num_pattern.focus();
		return false
	}
	if(transactionType == '') {
		alert('Please select Transaction Type.');
		document.hospitalidpatternsform.transaction_type.focus();
		return false;
	}
	
	return true;
}

function setMandotoryspan() {
	if(document.hospitalidpatternsform.sequence_reset_freq.value != '')
		document.getElementById('date_prefix_pattern_mandatory').style.display = 'inline-block';
	else	
		document.getElementById('date_prefix_pattern_mandatory').style.display = 'none';
}

function allowOnlyZeroAndNine(e) {
	   var c = getEventChar(e);
	   return (c == 48) || (c == 57) || (c == 0); // (c == 0) is for firefox and opera. Back button was not working in these browser.
}