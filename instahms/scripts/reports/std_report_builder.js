/***************************************************************************
 * Functions to add/change sample images as per the groupings.
 * o->grouping present ||  x->grpng. absent ||s->summary || t->trend || d-> vertical trend
 ****************************************************************************/

function changeTrendImg(cpath) {
	var srcL, srcS;
	if (document.getElementById('trendGroupVert').selectedIndex == 0) {
		srcL = "xxx_summary_large.png";
		srcS = "xxx_summary_small.png";
	} else {
		if (document.getElementById('trendGroupVertSub').selectedIndex == 0) {
			srcL = "tox_trend_large.png";
			srcS = "tox_trend_small.png";
		} else if (document.getElementById('trendGroupVertSub').value == '_data') {
			srcL = "tos_trend_large.png";
			srcS = "tos_trend_small.png";
		} else {
			srcL = "too_trend_large.png";
			srcS = "too_trend_small.png";
		}

	}
	document.getElementById("largeImg_trend").src = cpath + "/images/std_rep/" + srcL;
	document.getElementById("smallImg_trend").src = cpath + "/images/std_rep/" + srcS;
}



function changeVerticalTrendImg(cpath) {
	//TODO: create images and update
	var srcL, srcS;

	if (document.getElementById('vtrendGroupVertSub').value == '_data' && document.getElementById('vtrendGroupHoriz').value != '_data') {
	    if (document.getElementById('vtrendGroupHoriz').selectedIndex == 0) {
	        srcL = "xds_vtrend_large.png";
	        srcS = "xds_vtrend_small.png";
	    } else {
	        srcL = "ods_vtrend_large.png";
	        srcS = "ods_vtrend_small.png";
	    }
	} else if (document.getElementById('vtrendGroupVertSub').value != '_data' && document.getElementById('vtrendGroupHoriz').value == '_data') {
	    if (document.getElementById('vtrendGroupVertSub').selectedIndex == 0) {
	        srcL = "sdx_vtrend_large.png";
	        srcS = "sdx_vtrend_small.png";
	    } else {
	        srcL = "sdo_vtrend_large.png";
	        srcS = "sdo_vtrend_small.png";
	    }
	} else if (document.getElementById('vtrendGroupVertSub').value != '_data' && document.getElementById('vtrendGroupHoriz').value != '_data') {
	    if (document.getElementById('vtrendGroupVertSub').selectedIndex == 0 && document.getElementById('vtrendGroupHoriz').selectedIndex == 0) {
	        srcL = "xdx_vtrend_large.png";
	        srcS = "xdx_vtrend_small.png";
	    } else if (document.getElementById('vtrendGroupVertSub').selectedIndex != 0 && document.getElementById('vtrendGroupHoriz').selectedIndex == 0) {
	        srcL = "xdo_vtrend_large.png";
	        srcS = "xdo_vtrend_small.png";
	    } else if (document.getElementById('vtrendGroupVertSub').selectedIndex == 0 && document.getElementById('vtrendGroupHoriz').selectedIndex != 0) {
	        srcL = "odx_vtrend_large.png";
	        srcS = "odx_vtrend_small.png";
	    } else if (document.getElementById('vtrendGroupVertSub').selectedIndex != 0 && document.getElementById('vtrendGroupHoriz').selectedIndex != 0) {
	        srcL = "odo_vtrend_large.png";
	        srcS = "odo_vtrend_small.png";
	    } else {
	        srcL = "xxx_summary_large.png";
	        srcS = "xxx_summary_small.png";
	    }
	} else if (document.getElementById('vtrendGroupVertSub').value == '_data' && document.getElementById('vtrendGroupHoriz').value == '_data') {
	    srcL = "xxx_summary_large.png";
	    srcS = "xxx_summary_small.png";
	} else {
	    srcL = "xxx_summary_large.png";
	    srcS = "xxx_summary_small.png";
	}
	document.getElementById("largeImg_vtrend").src = cpath + "/images/std_rep/" + srcL;
	document.getElementById("smallImg_vtrend").src = cpath + "/images/std_rep/" + srcS;
}



function changeSummImg(cpath) {
	var srcL, srcS;
	if (document.getElementById("sumGroupVert").selectedIndex == 0) {
		srcL = "xxx_summary_large.png";
		srcS = "xxx_summary_small.png";
	} else {
		if (document.getElementById("sumGroupHoriz").value == '_data' && document.getElementById("sumGroupVertSub").value != '_data') {
			if (document.getElementById("sumGroupVertSub").selectedIndex == 0) {
				srcL = "sox_summary_large.png";
				srcS = "sox_summary_small.png";
			} else {
				srcL = "soo_summary_large.png";
				srcS = "soo_summary_small.png";
			}
		} else if (document.getElementById("sumGroupVertSub").value == "_data" && document.getElementById("sumGroupHoriz").value != "_data") {
			if (document.getElementById("sumGroupHoriz").selectedIndex == 0) {
				srcL = "xos_summary_large.png";
				srcS = "xos_summary_small.png";
			} else {
				srcL = "oos_summary_large.png";
				srcS = "oos_summary_large.png";
			}
		} else if ((document.getElementById("sumGroupVertSub").selectedIndex != 0 && document.getElementById("sumGroupVertSub").value != '_data') && (document.getElementById("sumGroupHoriz").selectedIndex != 0 && document.getElementById("sumGroupHoriz").value != '_data')) {
			srcL = "ooo_summary_large.png";
			srcS = "ooo_summary_small.png";
		} else if (document.getElementById("sumGroupHoriz").selectedIndex == 0 && (document.getElementById("sumGroupVertSub").selectedIndex != 0 && document.getElementById("sumGroupVertSub").value != '_data')) {
			srcL = "xoo_summary_large.png";
			srcS = "xoo_summary_small.png";
		} else if (document.getElementById("sumGroupVertSub").selectedIndex == 0 && (document.getElementById("sumGroupHoriz").selectedIndex != 0 && document.getElementById("sumGroupHoriz").value != '_data')) {
			srcL = "oox_summary_large.png";
			srcS = "oox_summary_small.png";
		} else if (document.getElementById("sumGroupVertSub").selectedIndex == 0 && document.getElementById("sumGroupHoriz").selectedIndex == 0) {
			srcL = "xox_summary_large.png";
			srcS = "xox_summary_small.png";
		} else {
			srcL = "xxx_summary_large.png";
			srcS = "xxx_summary_small.png";
		}
	}
	document.getElementById("largeImg_summ").src = cpath + "/images/std_rep/" + srcL;
	document.getElementById("smallImg_summ").src = cpath + "/images/std_rep/" + srcS;
}

function changeListImg(cpath) {
	var srcL, srcS;
	if (!document.getElementById("listGroups1") && !document.getElementById("listGroups2") && !document.getElementById("listGroups3")) {
		srcL = "xxx_list_large.png";
		srcS = "xxx_list_small.png";
	} else if (!document.getElementById("listGroups2") && !document.getElementById("listGroups3")) {
			srcL = "oxx_list_large.png";
			srcS = "oxx_list_small.png";
	} else if (!document.getElementById("listGroups3")) {
		if (document.getElementById("listGroups1").selectedIndex != 0 && document.getElementById("listGroups2").selectedIndex != 0) {
			srcL = "oox_list_large.png";
			srcS = "oox_list_small.png";
		} else if ((document.getElementById("listGroups1").selectedIndex != 0 && document.getElementById("listGroups2").selectedIndex == 0) || (document.getElementById("listGroups1").selectedIndex == 0 && document.getElementById("listGroups2").selectedIndex != 0)) {

			srcL = "oxx_list_large.png";
			srcS = "oxx_list_small.png";
		} else if (document.getElementById("listGroups1").selectedIndex == 0 && document.getElementById("listGroups2").selectedIndex == 0) {
			srcL = "oxx_list_large.png";
			srcS = "oxx_list_small.png";
		}
	} else {
		if (document.getElementById("listGroups1").selectedIndex != 0 && document.getElementById("listGroups2").selectedIndex != 0 && document.getElementById("listGroups3").selectedIndex != 0) {
			srcL = "ooo_list_large.png";
			srcS = "ooo_list_small.png";
		} else if ((document.getElementById("listGroups1").selectedIndex != 0 && document.getElementById("listGroups2").selectedIndex != 0 && document.getElementById("listGroups3").selectedIndex == 0) || (document.getElementById("listGroups1").selectedIndex == 0 && document.getElementById("listGroups2").selectedIndex != 0 && document.getElementById("listGroups3").selectedIndex != 0) || (document.getElementById("listGroups1").selectedIndex != 0 && document.getElementById("listGroups2").selectedIndex == 0 && document.getElementById("listGroups3").selectedIndex != 0)) {
			srcL = "oox_list_large.png";
			srcS = "oox_list_small.png";
		} else if ((document.getElementById("listGroups1").selectedIndex == 0 && document.getElementById("listGroups2").selectedIndex == 0 && document.getElementById("listGroups3").selectedIndex != 0) || (document.getElementById("listGroups1").selectedIndex != 0 && document.getElementById("listGroups2").selectedIndex == 0 && document.getElementById("listGroups3").selectedIndex == 0) || (document.getElementById("listGroups1").selectedIndex == 0 && document.getElementById("listGroups2").selectedIndex != 0 && document.getElementById("listGroups3").selectedIndex == 0)) {
			srcL = "oxx_list_large.png";
			srcS = "oxx_list_small.png";
		} else {
			srcL = "xxx_list_large.png";
			srcS = "xxx_list_small.png";
		}
	}
	if(document.getElementById('listGroupsDiv')) {
		document.getElementById("largeImg_list").src = cpath + "/images/std_rep/" + srcL;
		document.getElementById("smallImg_list").src = cpath + "/images/std_rep/" + srcS;
	}
}

// for date field customizations

function checkIfNoneSelctd(){
	if (document.getElementById("dateFieldSelection").type == "select-one") {
		var dateSel = document.getElementById("dateFieldSelection");
		var selIndex = dateSel.selectedIndex;
		if (dateSel.options[selIndex].value.toLowerCase() == 'none') {
			document.getElementById("_sel").disabled= true;
			document.inputform.fromDate.disabled= true;
			document.inputform.toDate.disabled= true;
			if (document.inputform.reportType[2] != null || document.inputform.reportType[2] != 'undefined'){
				if (document.inputform.reportType[2].checked) {
					document.inputform.reportType[0].checked = true;
					onChangeReportType();
				}
				document.inputform.reportType[2].disabled = true;
			}
			if (document.inputform.reportType[3] != null || document.inputform.reportType[3] != 'undefined'){
				if (document.inputform.reportType[3].checked) {
					document.inputform.reportType[0].checked = true;
					onChangeReportType();
				}
				document.inputform.reportType[3].disabled = true;
			}
		} else {
			document.getElementById("_sel").disabled= false;
			document.inputform.fromDate.disabled= false;
			document.inputform.toDate.disabled= false;
		}
	}

	// disable trend if the date field is a filter-only field
	// todo: we can hide it in the jsp itself instead of disabling .
	var dateField = document.getElementById("dateFieldSelection");
	if (dateField && dateField.value != '') {
		var disableTrend = false;
		for (var fname in filterOnlyNames) {
			if (dateField.value == fname) {
				document.inputform.reportType[0].checked = true;
				onChangeReportType();
				disableTrend = true;
				break;
			}
		}

		document.inputform.reportType[2].disabled = disableTrend;
		document.inputform.reportType[3].disabled = disableTrend;
	}

}

function setDateRangeforSel(){
	var dateSelected = document.getElementById("_sel").value;
	var fromDate = document.getElementById("fromDate");
	var toDate = document.getElementById("toDate");
	if(dateSelected == 'pd'){
		setDateRangeYesterday(fromDate, toDate);
	}else if(dateSelected == 'td'){
		setDateRangeToday(fromDate, toDate);
	}else if(dateSelected == 'tm'){
		setDateRangeMonth(fromDate, toDate);
	}else if(dateSelected == 'pm'){
		setDateRangePreviousMonth(fromDate, toDate);
	}else if(dateSelected == 'pf'){
		setDateRangePreviousFinancialYear(fromDate, toDate);
	}else if(dateSelected == 'tf'){
		setDateRangeFinancialYear(fromDate, toDate);
	}else if(dateSelected == 'cstm'){
		toggleCustom();
	}
}

function toggleCustom() {
	setDateToken();
}

function selectCustom() {
	var dateSelBox = document.getElementById("_sel");
	dateSelBox.options[6].selected = true;
	document.getElementById("selDateRange").value = "cstm";
}

function setDateToken() {
	var dateArray = new Array();
	dateArray[0] = document.getElementById("fromDate").value;
	dateArray[1] = document.getElementById("toDate").value;
	dateSelected = document.getElementById("_sel").value;
	if (dateSelected == 'td') {
		document.getElementById("selDateRange").value = "td";
	} else if (dateSelected == 'pd') {
		document.getElementById("selDateRange").value = "pd";
	} else if (dateSelected == 'pm') {
		document.getElementById("selDateRange").value = "pm";
	} else if (dateSelected == 'tm') {
		document.getElementById("selDateRange").value = "tm";
	} else if (dateSelected == 'pf') {
		document.getElementById("selDateRange").value = "pf";
	} else if (dateSelected == 'tf') {
		document.getElementById("selDateRange").value = "tf";
	} else if (dateSelected == 'cstm') {
		document.getElementById("selDateRange").value = "cstm";
	}
}

function resetPage() {
	srjsFile = document.getElementById("srjsFile").value;
	if (srjsFile != null && srjsFile != "Null" && srjsFile != "null") {
		var custArr = customReportIds;
		var cst_id = null;
		for (var i=0;i<custArr.length;i++) {
			var ele = custArr[i];
			if (ele.report_name + ".srjs" == srjsFile) {
				cst_id = ele.report_id;
				break;
			}
		}
		if (cst_id != null) window.location.href = window.location.pathname + "?method=runReport&id=" + cst_id;
	} else {
		window.location.href = window.location.pathname + "?method=getScreen";
	}
}


function getTodaysDateForVal(){
	var d = new Date();
	var curr_date = d.getDate()<10?  '0'+d.getDate():d.getDate();
	var curr_month = (d.getMonth()+1)<10? '0'+(d.getMonth()+1):(d.getMonth()+1);
	var curr_year = d.getFullYear();
	return curr_date + "-" + curr_month + "-" + curr_year;
}

//Variables used for storing fields parsed from url
var newfieldNameArray = new Array();
var newfieldValueArray = new Array();
var newfieldOpArray = new Array();
var newfieldTypeArray = new Array();

var newvalueArray = new Array();
var newopArray = new Array();
var newtypArray = new Array();
var newnameArray = new Array();

//Temporary variables used for storing field values before sorting
var fieldNameArray = new Array();
var fieldValueArray = new Array();
var fieldOpArray = new Array();
var fieldTypeArray = new Array();

var valueArray = new Array();
var opArray = new Array();
var typArray = new Array();
var nameArray = new Array();

var ur = document.location.href;
var argName = new Array();
var argVal = new Array();

//regular expression patterns to match tokens of type "field.x", "fop.x", "ftype.x" and "fval.x"; where x is a numeral.
var fieldMtch = /^filter\.[0-9][0-9]*$/gi;
var fvalMtch = /^filterVal\.[0-9][0-9]*$/gi;
var fopMtch = /^filterOp\.[0-9][0-9]*$/gi;
var ftypeMtch = /^filterType\.[0-9][0-9]*$/gi;
var fdateMtch = /\$\{td\}/gi;

var usfieldNameArray = new Array();
var usnameArray = new Array();

/***************************************************************************
 * Used to extend the javascript "sort" function for multi-dimensional arrays.
 * sortBy(dimension)--> sorts the array on that particular dimension.
 ****************************************************************************/

function sortBy(i) {
	return function(a, b) {
		a = a[i];
		b = b[i];
		return a == b ? 0 : (a < b ? -1 : 1)
	}
}

/***************************************************************************
 *To sort the fieldnames(i.e field.x) according to their indices (viz. x);
 * So that we can display them later on the dashboard, in the same order.
 ****************************************************************************/

function sortField(fieldNameArray, nameArray) {
	var tempArray = new Array(fieldNameArray.length);
	for (var i = 0; i < fieldNameArray.length; i++) {
		tempArray[i] = new Array(3);
	}
	for (var i = 0; i < fieldNameArray.length; i++) {
		var fNameAry = fieldNameArray[i].split('.');
		var indx = parseInt(fNameAry[1]);
		tempArray[i][0] = indx;
		tempArray[i][1] = fieldNameArray[i];
		tempArray[i][2] = nameArray[i];
	}
	tempArray = tempArray.sort(sortBy(0));
	return tempArray;
}

/***************************************************************************
 * to Decode the encoded url string into plain string.
 ****************************************************************************/

function uRLDecode(encodedString) {
	var output = decodeURIComponent(encodedString);
	output = output.replace('+',' ','g');
	return output;
}

/******************************************************************************************
 * function to handle favourite report PREVIEW; Here, the url is parsed to segregate fields.
 ********************************************************************************************/

function editReportHandler() {
	var staticfieldCount = no_of_static_fields;
	var fi = 0;
	if (window.location != null && window.location.search.length > 1) {
		//parse the URL
		var urlParameters = window.location.search.substring(1);
		var parameterPair = urlParameters.split('&');
		//get the key-value pairs.
		for (var i = 0; i < parameterPair.length; i++) {
			var pos = parameterPair[i].indexOf('=');
			argName[i] = parameterPair[i].substring(0, pos);
			argVal[i] = uRLDecode(parameterPair[i].substring(pos + 1));
		}
	}
	/**************************************************************************************
     *	We iterate through the parameter values to intitialize the values for:
     * 	the report builder's  groups, visible fields, filters, date and font.
     **************************************************************************************/
	for (var i = 0; i < argName.length; i++) {
		if (argName[i] == 'reportType') {
			var typeSel = document.inputform.reportType;
			for (var j = 0; j < typeSel.length; j++) {
				if (typeSel[j].value == argVal[i]) typeSel[j].checked = true;
			}
			if (argVal[i].toString() == 'list') {
				document.getElementById('listFieldsDiv').style.display = 'block';
				if(document.getElementById('listGroupsDiv'))
					document.getElementById('listGroupsDiv').style.display = 'block';
				document.getElementById('sumFieldsDiv').style.display = 'none';
				document.getElementById('sumGroupsDiv').style.display = 'none';
				document.getElementById('trendGroupsDiv').style.display = 'none';
				document.getElementById('verticalTrendGroupsDiv').style.display = 'none';
				if (document.getElementById("chart") != null) {
					document.getElementById("chart").disabled = true;
				}
				enableSortFields();
				//initialize sort fields
				for (var s = 0; s < argName.length; s++) {
					if (argName[s] == 'customOrder1') {
						var nameSel = document.inputform.customOrder1;
						for (var h = 0; h < nameSel.length; h++) {
							if (nameSel[h].value == argVal[s]) {
								nameSel[h].selected = true;
							}
						}
					} else if (argName[s] == 'customOrder2') {
						var nameSel = document.inputform.customOrder2;
						for (var h = 0; h < nameSel.length; h++) {
							if (nameSel[h].value == argVal[s]) {
								nameSel[h].selected = true;
							}
						}
					}
				}
				for (var d = 0; d < argName.length; d++) {
					if (argName[d] == 'sort1') {
						if(argVal[d] == 'DESC')
							document.inputform.sort1.checked = true;
					} else if(argName[d] == 'sort2') {
						if(argVal[d] == 'DESC')
							document.inputform.sort2.checked = true;
					}
				}
				//parse and store the field and group selected values for report Type= "list"
				var fCount = 0;
				var gCount = 0;
				var fldArray = new Array();
				var grpArray = new Array();
				for (var k = 0; k < argName.length; k++) {
					if (argName[k] == 'listFields') {
						fldArray[fCount++] = argVal[k];
					} else if (argName[k] == 'listGroups') {
						grpArray[gCount++] = argVal[k];
					}
				}
				var listFld = document.inputform.listFields;
				var avbFld = document.inputform.avlbListFlds;
				for (var k = 0; k < listFld.length; k++) {
					listFld[k].selected = true;
				}
				moveSelectedOptions(listFld, avbFld, 'from');
				for (var h = 0; h < fldArray.length; h++) {
					for (var k = 0; k < avbFld.length; k++) {
						if (fldArray[h].toString() == avbFld[k].value) {
							avbFld[k].selected = true;
							moveSelectedOptions(avbFld, listFld, 'from');
						}
					}
				}
				for (var k = 0; k < grpArray.length; k++) {
					if (grpArray[k] != null && grpArray[k] != '') {
						var grpFld = document.getElementById("listGroups" + (k + 1));
						for (var h = 0; h < grpFld.length; h++) {
							if (grpArray[k] == grpFld[h].value) {
								grpFld[h].selected = true;
							}
						}
					}
				}
			} else if (argVal[i].toString() == 'sum') {
				document.getElementById('listFieldsDiv').style.display = 'none';
				if(document.getElementById('listGroupsDiv'))
					document.getElementById('listGroupsDiv').style.display = 'none';
				document.getElementById('sumFieldsDiv').style.display = 'block';
				document.getElementById('sumGroupsDiv').style.display = 'block';
				document.getElementById('trendGroupsDiv').style.display = 'none';
				document.getElementById('verticalTrendGroupsDiv').style.display = 'none';
				if (document.getElementById("chart") != null) {
					document.getElementById("chart").disabled = true;
				}
				disableSortFields();
				//parse and store the field and group selected values for report Type= "summary"
				var fCount = 0;
				var gCount = 0;
				var fldArray = new Array();
				var grpVert, grpHoriz, grpSub;
				for (var k = 0; k < argName.length; k++) {
					if (argName[k] == 'sumFields') {
						fldArray[fCount++] = argVal[k];
					} else if (argName[k] == 'sumGroupVert') {
						var grpVertSel = document.inputform.sumGroupVert;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < grpVertSel.length; h++) {
							if (grpVertSel[h].value == argVal[k]) {
								grpVertSel[h].selected = true;
							}
						}
					} else if (argName[k] == 'sumGroupHoriz') {
						var grpHorizSel = document.inputform.sumGroupHoriz;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < grpHorizSel.length; h++) {
							if (grpHorizSel[h].value == argVal[k]) {
								grpHorizSel[h].selected = true;
							}
						}
					} else if (argName[k] == 'sumGroupVertSub') {
						var sumGroupVertSel = document.inputform.sumGroupVertSub;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < sumGroupVertSel.length; h++) {
							if (sumGroupVertSel[h].value == argVal[k]) {
								sumGroupVertSel[h].selected = true;
							}
						}
					}
				}
				var listFld = document.inputform.sumFields;
				var avbFld = document.inputform.avlbSummFlds;

				for (var h = 0; h < fldArray.length; h++) {
					for (var k = 0; k < avbFld.length; k++) {
						if (fldArray[h].toString() == avbFld[k].value) {
							avbFld[k].selected = true;
							moveSelectedOptions(avbFld, listFld, 'from');
						}
					}
				}


				for (var h = 0; h < fldArray.length; h++) {
					for (var k = 0; k < avbFld.length; k++) {
						if (fldArray[h].toString() == avbFld[k].value) {
							avbFld[k].selected = true;
							moveSelectedOptions(avbFld, listFld, 'from');
						}
					}
				}

			} else if (argVal[i].toString() == 'trend') {
				document.getElementById('listFieldsDiv').style.display = 'none';
				if(document.getElementById('listGroupsDiv'))
					document.getElementById('listGroupsDiv').style.display = 'none';
				document.getElementById('sumFieldsDiv').style.display = 'block';
				document.getElementById('sumGroupsDiv').style.display = 'none';
				document.getElementById('trendGroupsDiv').style.display = 'block';
				document.getElementById('verticalTrendGroupsDiv').style.display = 'none';
				if (document.getElementById("chart") != null) {
					document.getElementById("chart").disabled = false;
				}
				disableSortFields();
				//parse and store the field and group selected values for report Type= "trend"
				var fCount = 0;
				var gCount = 0;
				var fldArray = new Array();
				var grpVert, grpHoriz, grpSub;
				for (var k = 0; k < argName.length; k++) {
					if (argName[k] == 'sumFields') {
						fldArray[fCount++] = argVal[k];
					} else if (argName[k] == 'trendGroupVert') {
						var grpVertSel = document.inputform.trendGroupVert;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < grpVertSel.length; h++) {
							if (grpVertSel[h].value == argVal[k]) {
								grpVertSel[h].selected = true;
							}
						}
					} else if (argName[k] == 'trendType') {
						var grpHorizSel = document.inputform.trendType;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < grpHorizSel.length; h++) {
							if (grpHorizSel[h].value == argVal[k]) {
								grpHorizSel[h].selected = true;
							}
						}
					} else if (argName[k] == 'trendGroupVertSub') {
						var sumGroupVertSel = document.inputform.trendGroupVertSub;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < sumGroupVertSel.length; h++) {
							if (sumGroupVertSel[h].value == argVal[k]) {
								sumGroupVertSel[h].selected = true;
							}
						}
					}
				}
				var listFld = document.inputform.sumFields;
				var avbFld = document.inputform.avlbSummFlds;

				for (var h = 0; h < fldArray.length; h++) {
					for (var k = 0; k < avbFld.length; k++) {
						if (fldArray[h].toString() == avbFld[k].value) {
							avbFld[k].selected = true;
							moveSelectedOptions(avbFld, listFld, 'from');
						}
					}
				}
		} else if (argVal[i].toString() == 'vtrend') {
				document.getElementById('listFieldsDiv').style.display = 'none';
				if(document.getElementById('listGroupsDiv'))
					document.getElementById('listGroupsDiv').style.display = 'none';
				document.getElementById('sumFieldsDiv').style.display = 'block';
				document.getElementById('sumGroupsDiv').style.display = 'none';
				document.getElementById('trendGroupsDiv').style.display = 'none';
				document.getElementById('verticalTrendGroupsDiv').style.display = 'block';
				if (document.getElementById("chart") != null) {
					document.getElementById("chart").disabled = false;
				}
				disableSortFields();
				//parse and store the field and group selected values for report Type= "trend"
				var fCount = 0;
				var gCount = 0;
				var fldArray = new Array();
				var grpVert, grpHoriz, grpSub;
				for (var k = 0; k < argName.length; k++) {
					if (argName[k] == 'sumFields') {
						fldArray[fCount++] = argVal[k];
					} else if (argName[k] == 'vtrendType') {
						var grpVertSel = document.inputform.vtrendType;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < grpVertSel.length; h++) {
							if (grpVertSel[h].value == argVal[k]) {
								grpVertSel[h].selected = true;
							}
						}
					} else if (argName[k] == 'vtrendGroupHoriz') {
						var grpHorizSel = document.inputform.vtrendGroupHoriz;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < grpHorizSel.length; h++) {
							if (grpHorizSel[h].value == argVal[k]) {
								grpHorizSel[h].selected = true;
							}
						}
					} else if (argName[k] == 'vtrendGroupVertSub') {
						var sumGroupVertSel = document.inputform.vtrendGroupVertSub;
						if (argVal[k] != null || argVal[k] != '') for (var h = 0; h < sumGroupVertSel.length; h++) {
							if (sumGroupVertSel[h].value == argVal[k]) {
								sumGroupVertSel[h].selected = true;
							}
						}
					}
				}
				var listFld = document.inputform.sumFields;
				var avbFld = document.inputform.avlbSummFlds;

				for (var h = 0; h < fldArray.length; h++) {
					for (var k = 0; k < avbFld.length; k++) {
						if (fldArray[h].toString() == avbFld[k].value) {
							avbFld[k].selected = true;
							moveSelectedOptions(avbFld, listFld, 'from');
						}
					}
				}

			}
		} else if (argName[i] == 'selDateRange') {
			var dateSel = document.inputform._sel;
			if(dateSel != null){
				for (var h = 0; h < dateSel.length; h++) {
					if (dateSel.options[h].value == argVal[i]) {
						dateSel.options[h].selected = true;
						if (dateSel[h].id != 'cstm') {
							setDateRangeforSel();
							setDateToken();
						}
					}
				}
			}
			if (argVal[i] == 'cstm') {
				for (var indx = 0; indx < argName.length; indx++) {
					if (argName[indx] == 'fromDate') document.inputform.fromDate.value = argVal[indx]
					else if (argName[indx] == 'toDate') document.inputform.toDate.value = argVal[indx];
				}
			}
		} else if (argName[i] == 'baseFontSize') {
			var fontSel = document.inputform.baseFontSize;
			for (var h = 0; h < fontSel.length; h++) {
				if (fontSel[h].value == argVal[i]) {
					fontSel[h].selected = true;
				}
			}
		} else if (argName[i] == 'userNameNeeded') {
			if(argVal[i] == 'Y' || argVal[i]=='y')
				document.inputform.pdfcstm_option[0].checked = true;
			else
				document.inputform.pdfcstm_option[0].checked = false;
		} else if(argName[i] == 'dt_needed'){
				if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[1].checked = true;
			else
				document.inputform.pdfcstm_option[1].checked = false;
		}else if(argName[i] == 'hsp_needed'){
				if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[2].checked = true;
			else
				document.inputform.pdfcstm_option[2].checked = false;
		}else if(argName[i] == 'hsp_needed_h'){
				if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[3].checked = true;
			else
				document.inputform.pdfcstm_option[3].checked = false;
		}else if(argName[i] == 'pgn_needed'){
			if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[4].checked = true;
			else
				document.inputform.pdfcstm_option[4].checked = false;
		}else if(argName[i] == 'grpn_needed'){
			if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[5].checked = true;
			else
				document.inputform.pdfcstm_option[5].checked = false;

		}else if(argName[i] == 'skip_repeated_values'){
			if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[6].checked = true;
			else
				document.inputform.pdfcstm_option[6].checked = false;

		}else if(argName[i] == 'filterDesc_needed'){
			if(argVal[i] == 'true' || argVal[i]==true)
				document.inputform.pdfcstm_option[7].checked = true;
			else
				document.inputform.pdfcstm_option[7].checked = false;

		}else if (argName[i] == 'dateFieldSelection') {
			//for multiple date fields
			if (document.getElementById('dateFieldSelection').type == 'select-one') {
				var dtdFld = document.getElementById('dateFieldSelection');
				for (var dtdFldCnt = 0; dtdFldCnt < dtdFld.length; dtdFldCnt++) {
					if (dtdFld[dtdFldCnt].value == argVal[i]) dtdFld[dtdFldCnt].selected = true;
				}
				//for a single date field.
			} else if (document.getElementById('dateFieldSelection').type == 'hidden') {
				document.getElementById('dateFieldSelection').value = argVal[i];
			}

		}else if(argName[i] == 'print_title'){
				document.getElementById('print_title').value = argVal[i];
		}
	}
	/*****************************************************************************************************
     --------------------------Filter Processing for favourite reports------------------------------------
     *To Match and segregate the url key-value pairs into filter- field, value, operator and type tokens.
     *****************************************************************************************************/
	fi = 0;
	for (var i = 0; i < argName.length; i++) {
		if (argName[i].match(fieldMtch)) {
			usfieldNameArray[fi] = argName[i];
			usnameArray[fi] = argVal[i];
			fi++;
		}
	}
	fi = 0;
	for (var i = 0; i < argName.length; i++) {
		if (argName[i].match(fvalMtch)) {
			fieldValueArray[fi] = argName[i];
			valueArray[fi] = argVal[i];
			fi++;
		}
	}
	fi = 0;
	for (var i = 0; i < argName.length; i++) {
		if (argName[i].match(fopMtch)) {
			fieldOpArray[fi] = argName[i];
			opArray[fi] = argVal[i];
			fi++;
		}
	}
	fi = 0;
	for (var i = 0; i < argName.length; i++) {
		if (argName[i].match(ftypeMtch)) {
			fieldTypeArray[fi] = argName[i];
			typArray[fi] = argVal[i];
			fi++;
		}
	}

	//Sort the token pairs by their field indices;
	//So that they may be displayed in ascending order(i.e, their order of addition).
	var tempArray = sortField(usfieldNameArray, usnameArray);
	var lngth = usfieldNameArray.length;
	for (var c = 0; c < lngth; c++) {
		fieldNameArray[c] = tempArray[c][1];
		nameArray[c] = tempArray[c][2];
	}

	fi = 0;
	for (var i = 0; i < fieldNameArray.length; i++) {
		if (fieldNameArray[i] != null) {
			var fb = fieldNameArray[i].toString();
			var idxArray = fb.split('.');
			var idx = idxArray[1];
			for (var k = 0; k < fieldValueArray.length; k++) {
				var vdxArray = fieldValueArray[k].split('.');
				var vdx = vdxArray[1];
				if (vdx == idx) {
					newfieldNameArray[fi] = fieldNameArray[i];
					newfieldValueArray[fi] = fieldValueArray[k];
					newnameArray[fi] = nameArray[i];
					newvalueArray[fi] = valueArray[k];

				}
			}
			for (var k = 0; k < fieldOpArray.length; k++) {
				var odxArray = fieldOpArray[k].split('.');
				var odx = odxArray[1];
				if (odx == idx) {
					newfieldOpArray[fi] = fieldOpArray[k];
					newopArray[fi] = opArray[k];

				}
			}
			for (var k = 0; k < fieldTypeArray.length; k++) {
				var tdxArray = fieldTypeArray[k].split('.');
				var tdx = tdxArray[1];
				if (tdx == idx) {
					newfieldTypeArray[fi] = fieldTypeArray[k];
					newtypArray[fi] = typArray[k];

				}
			}
			fi++;
		}
	}

	//Add default values when "operator" is absent.
	for (var k = 0; k < fieldNameArray.length; k++) {
		if (newopArray[k] == null) {
			newopArray[k] = "eq";
		}
	}

	for (var k = 0; k < fieldNameArray.length; k++) {
		var indxArray = (fieldNameArray[k].toString()).split('.');
		var index = indxArray[1];
		//do processing, after segregration...
		processEdit(index, k);
	}
	changeSummImg(document.getElementById('ctpath').value);
	changeTrendImg(document.getElementById('ctpath').value);
	changeListImg(document.getElementById('ctpath').value);
	changeVerticalTrendImg(document.getElementById('ctpath').value);
	checkIfNoneSelctd();
}

/*****************************************************************************************
 *function to initialize form elements with corresponding, favourite-report values.
 ******************************************************************************************/

function processEdit(fieldIndex, ArrayIndex) {
	if (fieldIndex == 1) {
		var static_sel = document.getElementById("filter." + fieldIndex);
		var len = static_sel.options.length;
		for (var i = 0; i < len; i++) {
			if (static_sel.options[i].value == newnameArray[ArrayIndex]) {
				static_sel.selectedIndex = i;
			}
		}
		onChangeFilterBy(fieldIndex);

		var oprSel = document.getElementById("filterOp." + fieldIndex);
		var oplen = oprSel.options.length;
		for (var j = 0; j < oplen; j++) {
			if (oprSel.options[j].value == newopArray[ArrayIndex]) oprSel.selectedIndex = j;
		}
		if (newopArray[ArrayIndex] == 'nin' || newopArray[ArrayIndex] == 'in') {
			fillTBx(fieldIndex);
			document.getElementById("filterVal." + fieldIndex).value = newvalueArray[ArrayIndex];
		} else if (newopArray[ArrayIndex] == 'between') {
			fillTBx(fieldIndex);
			for (var i = 0; i < fieldValueArray.length; i++) {
				var idxArray = fieldValueArray[i].split(".");
				var idx = idxArray[1];
				if (fieldIndex == idx) {
					if (valueArray[i] != null && document.getElementById('fromVal.' + fieldIndex) && document.getElementById('fromVal.' + fieldIndex).value == '') document.getElementById('fromVal.' + fieldIndex).value = valueArray[i];
					else if (valueArray[i] != null && document.getElementById('toVal.' + fieldIndex)) document.getElementById('toVal.' + fieldIndex).value = valueArray[i];
				}
			}
		} else {
			fillTBx(fieldIndex);
			var valSel = document.getElementById("filterVal." + fieldIndex);
			if (valSel.type == 'select-one') {
				var valLen = valSel.options.length;
				var selval = uRLDecode(newvalueArray[ArrayIndex]);
				for (var j = 0; j < valLen; j++) {
					if (valSel.options[j].value == selval) valSel.selectedIndex = j;
				}
			} else if (valSel.type == 'text') {
				var fval = uRLDecode(newvalueArray[ArrayIndex]);
				valSel.value =  fval.match(fdateMtch)!= null? getTodaysDateForVal(): fval;
			}
		}

	} else if (fieldIndex > no_of_static_fields) {
		num = fieldIndex;
		addfilterElements();
		var static_sel = document.getElementById("filter." + fieldIndex);
		var len = static_sel.options.length;
		for (var i = 0; i < len; i++) {
			if (static_sel.options[i].value == newnameArray[ArrayIndex]) {
				static_sel.selectedIndex = i;
			}
		}
		onChangeFilterBy(fieldIndex);
		var oprSel = document.getElementById("filterOp." + fieldIndex);
		var oplen = oprSel.options.length;
		for (var j = 0; j < oplen; j++) {
			if (oprSel.options[j].value == newopArray[ArrayIndex]) oprSel.selectedIndex = j;
		}
		if (newopArray[ArrayIndex] == 'nin' || newopArray[ArrayIndex] == 'in') {
			fillTBx(fieldIndex);
			var x = uRLDecode(newvalueArray[ArrayIndex]);
			document.getElementById("filterVal." + fieldIndex).value = x;
		} else if (newopArray[ArrayIndex] == 'between') {
			fillTBx(fieldIndex);
			for (var i = 0; i < fieldValueArray.length; i++) {
				var idxArray = fieldValueArray[i].split(".");
				var idx = idxArray[1];
				if (fieldIndex == idx) {
					if (valueArray[i] != null && document.getElementById('fromVal.' + fieldIndex) && document.getElementById('fromVal.' + fieldIndex).value == '') document.getElementById('fromVal.' + fieldIndex).value = valueArray[i];
					else if (valueArray[i] != null && document.getElementById('toVal.' + fieldIndex)) document.getElementById('toVal.' + fieldIndex).value = valueArray[i];
				}
			}
		} else {
			fillTBx(fieldIndex);
			var valSel = document.getElementById("filterVal." + fieldIndex);
			if (valSel.type == 'select-one') {
				var valLen = valSel.options.length;
				var selval = uRLDecode(newvalueArray[ArrayIndex]);
				for (var j = 0; j < valLen; j++) {
					if (valSel.options[j].value == selval) valSel.selectedIndex = j;
				}
			} else if (valSel.type == 'text') {
				var fval = uRLDecode(newvalueArray[ArrayIndex]);
				valSel.value = fval.match(fdateMtch)!= null? getTodaysDateForVal(): fval;
			}
		}
	}
}

/*****************************************************************************************
 *-------------------------Filter addition and Processing----------------------------------
 ******************************************************************************************/
var no_of_static_fields = 1;
var num = no_of_static_fields + 1;

function addfilterElements() {
	var parentDiv;
	// Append the new "filter by" select box.
	parentDiv = document.getElementById('addMore');
	var tr1 = parentDiv.insertRow( - 1);
	tr1.name = "tr." + num;
	tr1.id = "tr." + num;

	var td1 = tr1.insertCell( - 1);
	td1.name = "td1." + num;
	td1.id = "td1." + num;

	var sel1 = document.createElement('select');
	sel1.name = 'filter.' + num;
	sel1.id = 'filter.' + num;
	sel1.setAttribute("onchange", "onChangeFilterBy(" + num + ");");
	sel1.setAttribute("style", "width:11em");
	sel1.setAttribute("class", "dropDown filterfields");
	td1.appendChild(sel1);
	var opt1 = document.createElement('option');
	sel1.appendChild(opt1);
	var optText = new Array();
	var k = 0;

	var fFldNames = document.getElementById("filterFieldNamez").value;
	var filtFNstring = fFldNames.substr(1, fFldNames.length - 2);
	var filtFNvals = filtFNstring.split(",");
	var optVal = new Array();
	k = 0;
	for (var l = 0; l < filtFNvals.length; l++) {
		optVal[k] = trim(filtFNvals[l]);
		k = k + 1;
	}
	for (var x = 0; x < optVal.length; x++) {
		optText[x] = filterNmz[optVal[x]];
	}
	sel1.length = optText.length + 1;
	sel1.options[0].text = "(No Filter)";
	sel1.options[0].value = "";
	for (index = 0; index < optText.length; index++) {
		sel1.options[index + 1].text = optText[index];
		sel1.options[index + 1].value = optVal[index];
	}
	sortSelect(sel1);
	sel1.selectedIndex = 0;

	//create and append the operator-select.
	var td3 = tr1.insertCell( - 1);
	td3.name = "td3." + num;
	td3.id = "td3." + num;

	td3.setAttribute("style", "padding-left:4px;");
	var opSel = document.createElement('Select');
	opSel.name = "filterOp." + num;
	opSel.id = "filterOp." + num;
	opSel.setAttribute("onchange", "fillTBx(" + num + ");");
	opSel.setAttribute("class", "dropDown");
	var opSelOpt = document.createElement('option');
	opSel.appendChild(opSelOpt);
	td3.appendChild(opSel);
	opSel.setAttribute("style", "width:60px;");
	opSel.length = 16;
	opSel.options[0].text = "=";
	opSel.options[0].value = "eq";
	opSel.options[1].text = '\u2260';
	opSel.options[1].value = "ne";
	opSel.options[2].text = "<";
	opSel.options[2].value = "lt";
	opSel.options[3].text = ">";
	opSel.options[3].value = "gt";
	opSel.options[4].text = '\u2264';
	opSel.options[4].value = "le";
	opSel.options[5].text = '\u2265';
	opSel.options[5].value = "ge";
	opSel.options[6].text = "Any of";
	opSel.options[6].value = "in";
	opSel.options[7].text = "None of";
	opSel.options[7].value = "nin";
	opSel.options[8].text = "Contains";
	opSel.options[8].value = "ico";
	opSel.options[9].text = "Contains (exact case)";
	opSel.options[9].value = "co";
	opSel.options[10].text = "Starts with";
	opSel.options[10].value = "isw";
	opSel.options[11].text = "Starts with (exact case)";
	opSel.options[11].value = "sw";
	opSel.options[12].text = "Ends with";
	opSel.options[12].value = "iew";
	opSel.options[13].text = "Ends with (exact case)";
	opSel.options[13].value = "ew";
	opSel.options[14].text = "Between";
	opSel.options[14].value = "Between";
	opSel.options[15].text = "Is empty";
	opSel.options[15].value = "null";
	opSel.selectedIndex = 0;

	var td4 = tr1.insertCell( - 1);
	td4.name = "td4." + num;
	td4.id = "td4." + num;
	td4.setAttribute("style", "padding-left:4;");
	var txtBx = document.createElement("input");
	txtBx.type = "text";
	txtBx.name = "txt." + num;
	txtBx.setAttribute("style", "width:11em;");
	txtBx.setAttribute("onkeypress", "return validateTextField(event,'filterVal." + num + "');");
	txtBx.setAttribute("onchange", "return validateTextField(event,'filterVal." + num + "');");
	txtBx.setAttribute("onBlur", "return trimText(this);");

	txtBx.id = "txt." + num;
	txtBx.disabled = "true";
	td4.appendChild(txtBx);

	var spc2 = document.createElement("span");
	spc2.id = "spc2." + num;
	spc2.innerHTML = '&nbsp;&nbsp;';
	td4.appendChild(spc2);

	//create and append filter-value select box
	var valSel = document.createElement('select');
	valSel.name = "filterVal." + num;
	valSel.id = "filterVal." + num;
	valSel.setAttribute("class", "dropDown");
	valSel.setAttribute("style", "width:11em");
	var valSelOpt = document.createElement('option');
	valSel.appendChild(valSelOpt);
	td4.appendChild(valSel);
	valSel.options[0].text = "--(All)--";
	valSel.options[0].value = "";
	valSel.selectedIndex = 0;
	valSel.disabled = false;

	var td0 = tr1.insertCell( - 1);
	td0.name = "td0." + num;
	td0.id = "td0." + num;
	td0.width = "";
	td0.setAttribute("align", "right");
	td0.setAttribute("style", "padding-left: 5px; padding-right: 5px; height: 18px; width: 17px;");
	var cpth = document.getElementById('ctpath').value;
	var di = document.createElement('img');
	di.setAttribute("src", cpth + "/icons/delete.gif");
	di.setAttribute("onclick", "deleteFilterElements(" + num + ")");
	td0.appendChild(di);
	num++;
}

/*****************************************************************************************
 * To handle the deletion of filter fields
 ******************************************************************************************/

function deleteFilterElements(id) {
	var delEle = document.getElementById("tr." + id);
	delEle.parentNode.removeChild(delEle);
}

function getFilterValues(fieldName) {
	const ajaxobj = newXMLHttpRequest();
	const reportProvider = $("#reptDescProvider").val();
	const isCustom = $("#isCustom").val();
	const reportFile = isCustom == 'true' ? $("#srjsFile").val() : $("#reptDescFile").val();
	const url = cpath + "/reports/getFilterValues.json?fieldName=" + fieldName + 
		"&reportProvider=" + (reportProvider && reportProvider != 'null' ?reportProvider:'') + 
		"&isCustom=" + (isCustom?isCustom:'') + 
		"&reportName=" + (reportFile?reportFile:'');

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			console.log(ajaxobj.responseText)
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function onChangeFilterBy(id) {
	var selArray = new Array();
	var indx = 0;
	var filterBy = document.getElementById("filter." + id).value;

	var otd = document.getElementById("td3." + id);
	otd.setAttribute("style", "padding-left:4;");
	otd.innerHTML = '';

	var ptd = document.getElementById("td4." + id);
	ptd.setAttribute("style", "padding-left:4;");
	ptd.innerHTML = '';
	if (filterTyps[filterBy] != 'numeric' && !notHrzGrpble[filterBy] && filterTyps[filterBy] != 'date' && filterTyps[filterBy] != 'timestamp') {
		//create and initialize the operator select.
		var opSel = document.createElement('select');
		opSel.name = "filterOp." + id;
		opSel.id = "filterOp." + id;
		opSel.setAttribute("style", "width:60px;");
		opSel.setAttribute("class", "dropDown");
		opSel.setAttribute("onchange", "fillTBx(" + id + ");");
		otd.appendChild(opSel);
		var opOpt = document.createElement("option");
		opSel.appendChild(opOpt);
		opSel.disabled = false;
		opSel.length = 16;
		opSel.options[0].text = "=";
		opSel.options[0].value = "eq";
		opSel.options[1].text = '\u2260';
		opSel.options[1].value = "ne";
		opSel.options[2].text = "<";
		opSel.options[2].value = "lt";
		opSel.options[3].text = ">";
		opSel.options[3].value = "gt";
		opSel.options[4].text = '\u2264';
		opSel.options[4].value = "le";
		opSel.options[5].text = '\u2265';
		opSel.options[5].value = "ge";
		opSel.options[6].text = "contains any of the words";
		opSel.options[6].value = "in";
		opSel.options[7].text = "contains none of the words";
		opSel.options[7].value = "nin";
		opSel.options[8].text = "contains word (exact case)";
		opSel.options[8].value = "co";
		opSel.options[9].text = "contains word";
		opSel.options[9].value = "ico";
		opSel.options[10].text = "starts with (exact case)";
		opSel.options[10].value = "sw";
		opSel.options[11].text = "starts with";
		opSel.options[11].value = "isw";
		opSel.options[12].text = "ends with (exact case)";
		opSel.options[12].value = "ew";
		opSel.options[13].text = "ends with";
		opSel.options[13].value = "iew";
		opSel.options[14].text = "between";
		opSel.options[14].value = "between";
		opSel.options[15].text = "is empty";
		opSel.options[15].value = "null";
		opSel.selectedIndex = 0;

		//the new tbox added
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "txt." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("onkeypress", "return validateTextField(event,'filterVal." + id + "');");
		txtBx.setAttribute("onchange", "return validateTextField(event,'filterVal." + id + "');");
		txtBx.setAttribute("onBlur", "return trimText(this);");
		txtBx.id = "txt." + id;
		txtBx.disabled = "true";
		ptd.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		ptd.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "filterVal." + id;
		sel.id = "filterVal." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		ptd.appendChild(sel);
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		//fill the "field value" select box based on filter selected.
		if ((filterBy != "")) {
			values = filterValues[filterBy];
			if(!values) {
				values = getFilterValues(filterBy);
				filterValues[filterBy] = values;
			}
			loadSelectBox(sel, values, null, null, "(All)", "");
		} else {
			loadSelectBox(sel, null, null, null, "(All)", "");
		}
		sel.selectedIndex = 0;
	} else if (notHrzGrpble[filterBy] == true && filterTyps[filterBy] != 'date' && filterTyps[filterBy] != 'numeric' && filterTyps[filterBy] != 'timestamp') {
		var opSel = document.createElement('select');
		opSel.name = "filterOp." + id;
		opSel.id = "filterOp." + id;
		opSel.setAttribute("style", "width:60px;");
		opSel.setAttribute("class", "dropDown");
		opSel.setAttribute("onchange", "fillTBx(" + id + ");");
		otd.appendChild(opSel);
		var opOpt = document.createElement("option");
		opSel.appendChild(opOpt);
		opSel.disabled = false;
		opSel.length = 16;
		opSel.options[0].text = "=";
		opSel.options[0].value = "eq";
		opSel.options[1].text = '\u2260';
		opSel.options[1].value = "ne";
		opSel.options[2].text = "<";
		opSel.options[2].value = "lt";
		opSel.options[3].text = ">";
		opSel.options[3].value = "gt";
		opSel.options[4].text = '\u2264';
		opSel.options[4].value = "le";
		opSel.options[5].text = '\u2265';
		opSel.options[5].value = "ge";
		opSel.options[6].text = "contains any of the words";
		opSel.options[6].value = "in";
		opSel.options[7].text = "contains none of the words";
		opSel.options[7].value = "nin";
		opSel.options[8].text = "contains word (exact case)";
		opSel.options[8].value = "co";
		opSel.options[9].text = "contains word";
		opSel.options[9].value = "ico";
		opSel.options[10].text = "starts with (exact case)";
		opSel.options[10].value = "sw";
		opSel.options[11].text = "starts with";
		opSel.options[11].value = "isw";
		opSel.options[12].text = "ends with (exact case)";
		opSel.options[12].value = "ew";
		opSel.options[13].text = "ends with";
		opSel.options[13].value = "iew";
		opSel.options[14].text = "between";
		opSel.options[14].value = "between";
		opSel.options[15].text = "is empty";
		opSel.options[15].value = "null";
		opSel.selectedIndex = 0;

		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.id = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("onkeypress", "return enterAlphaNumericalsExt(event)");
		txtBx.setAttribute("onchange", "return enterAlphaNumericalsExt(event)");
		txtBx.setAttribute("onBlur", "return trimText(this);");
		ptd.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		ptd.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		ptd.appendChild(sel);
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		sel.selectedIndex = 0;

		sel.disabled = true;
	} else {
		//create and initialize the operator select.
		var opSel = document.createElement('select');
		opSel.name = "filterOp." + id;
		opSel.id = "filterOp." + id;
		opSel.setAttribute("style", "width:60px;");
		opSel.setAttribute("class", "dropDown");
		opSel.setAttribute("onchange", "fillTBx(" + id + ");");
		otd.appendChild(opSel);
		var opOpt = document.createElement("option");
		opSel.appendChild(opOpt);
		opSel.disabled = false;
		opSel.length = 8;
		opSel.options[0].text = "=";
		opSel.options[0].value = "eq";
		opSel.options[1].text = '\u2260';
		opSel.options[1].value = "ne";
		opSel.options[2].text = "<";
		opSel.options[2].value = "lt";
		opSel.options[3].text = ">";
		opSel.options[3].value = "gt";
		opSel.options[4].text = '\u2264';
		opSel.options[4].value = "le";
		opSel.options[5].text = '\u2265';
		opSel.options[5].value = "ge";
		opSel.options[6].text = "between";
		opSel.options[6].value = "between";
		opSel.options[7].text = "is empty";
		opSel.options[7].value = "null";
		opSel.selectedIndex = 0;

		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.id = "filterVal." + id;
		txtBx.setAttribute("maxlength", 10);
		txtBx.setAttribute("style", "width:11em;");
		if (filterTyps[filterBy] == 'timestamp'){
			txtBx.setAttribute("title","Please enter date in DD-MM-[YY]YY [hh:mm:ss] format.")
			txtBx.setAttribute("onkeypress", "return enterDateNumericals(event)");
			txtBx.setAttribute("onchange", "return enterDateNumericals(event)");
			txtBx.setAttribute("onBlur", "validateFilterTimeStampFormat(this);");
			txtBx.removeAttribute("maxlength");
		} else if (filterTyps[filterBy] == 'date'){
			txtBx.setAttribute("title","Please enter date in DD-MM-[YY]YY format.")
			txtBx.setAttribute("maxlength","10");
			txtBx.setAttribute("onkeypress", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txtBx.setAttribute("onchange", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txtBx.setAttribute("onBlur", "validateFilterDateFormat(this);");
		} else {
			txtBx.setAttribute("onkeypress", "return enterNumAndDotAndMinus(event)");
			txtBx.setAttribute("onchange", "return enterNumAndDotAndMinus(event)");
			txtBx.setAttribute("onBlur", "return trimNum(this);");
		}
		ptd.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		ptd.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		ptd.appendChild(sel);
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		sel.selectedIndex = 0;

		sel.disabled = true;
	}
}

/*****************************************************************************************
 * To handle different operators for numeric filter fields
 ******************************************************************************************/

function fillTbxNum(id) {
	var filterBy = document.getElementById("filter." + id);
	trim(filterBy.value);
	if (filterBy.selectedIndex == 0) return;
	var op = document.getElementById("filterOp." + id).value;
	var td = document.getElementById("td4." + id);
	if ((op == 'eq' || op == 'ne')) {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.id = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");
		if(filterTyps[filterBy.value] == 'date'){
			txtBx.setAttribute("title","Please enter date in DD-MM-(YY)YY format.")
			txtBx.setAttribute("maxlength","10");
			txtBx.setAttribute("onkeypress", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txtBx.setAttribute("onchange", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txtBx.setAttribute("onBlur", "validateFilterDateFormat(this);");
		}else {
			txtBx.setAttribute("onkeypress", "return enterNumAndDotAndMinus(event)");
			txtBx.setAttribute("onchange", "return enterNumAndDotAndMinus(event)");
			txtBx.setAttribute("onBlur", "return trimNum(this);");
		}
		txtBx.setAttribute("maxlength", 10);
		td.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		td.appendChild(sel);
		sel.disabled = true;
	} else if (op == 'null') {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "txt." + id;
		txtBx.id = "txt." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("disabled", "true");
		td.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = 'filterVal.' + id;
		sel.id = 'filterVal.' + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		td.appendChild(sel);
		sel.appendChild(filtOpt);
		sel.length = 2;
		sel.options[0].text = "yes";
		sel.options[0].value = "y";
		sel.options[1].text = "no";
		sel.options[1].value = "n";
		sel.id = 'filterVal.' + id;
		sel.name = 'filterVal.' + id;
	} else if (op == 'between') {
		td.innerHTML = '';
		var txt1 = document.createElement('input');
		txt1.type = "text";
		txt1.name = "filterVal." + id;
		txt1.id = "fromVal." + id;
		txt1.setAttribute("style", "width:65px;");
		if(filterTyps[filterBy.value] == 'date'){
			txt1.setAttribute("maxlength","10");
			txt1.setAttribute("title","Please enter date in DD-MM-(YY)YY format.")
			txt1.setAttribute("onkeypress", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txt1.setAttribute("onchange", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txt1.setAttribute("onBlur", "validateDateForBetwFrom(this,"+id+");");
		}else {
			txt1.setAttribute("onkeypress", "return enterNumAndDotAndMinus(event)");
			txt1.setAttribute("onchange", "return enterNumAndDotAndMinus(event)");
			txt1.setAttribute("onBlur", "return trimNum(this);");
		}
		txt1.setAttribute("maxlength", 10);

		td.appendChild(txt1);

		var txt2 = document.createElement('input');
		txt2.type = "text";
		txt2.name = "filterVal." + id;
		txt2.id = "toVal." + id;
		if(filterTyps[filterBy.value] == 'date'){
			txt2.setAttribute("maxlength","10");
			txt2.setAttribute("title","Please enter date in DD-MM-(YY)YY format.")
			txt2.setAttribute("onkeypress", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txt2.setAttribute("onchange", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txt2.setAttribute("onBlur", "validateDateForBetw(this,"+id+");");
		}else {
			txt2.setAttribute("onkeypress", "return enterNumAndDotAndMinus(event)");
			txt2.setAttribute("onchange", "return enterNumAndDotAndMinus(event)");
			txt2.setAttribute("onBlur", "return trimNum(this);");
		}
		txt2.setAttribute("style", "width:65px;");
		txt2.setAttribute("maxlength", 10);

		td.appendChild(txt2);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		td.appendChild(sel);
		sel.setAttribute("disabled", "true");
	} else if ((op == 'lt' || op == 'gt' || op == 'le' || op == 'ge' || op == 'co' || op == 'ico' || op == 'sw' || op == 'isw' || op == 'ew' || op == 'iew')) {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");

		if(filterTyps[filterBy.value] == 'date'){
			txtBx.setAttribute("title","Please enter date in DD-MM-(YY)YY format.")
			txtBx.setAttribute("maxlength","10");
			txtBx.setAttribute("onkeypress", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txtBx.setAttribute("onchange", "return enterNumOnlyANDhipWithArrowsBkspace(event)");
			txtBx.setAttribute("onBlur", "validateFilterDateFormat(this);");
		}else {
			txtBx.setAttribute("onkeypress", "return enterNumAndDotAndMinus(event)");
			txtBx.setAttribute("onchange", "return enterNumAndDotAndMinus(event)");
			txtBx.setAttribute("onBlur", "return trimNum(this);");
		}
		txtBx.setAttribute("maxlength", 10);
		td.appendChild(txtBx);
		txtBx.id = "filterVal." + id;

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		td.appendChild(sel);

		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		sel.selectedIndex = 0;
		sel.setAttribute("disabled", "true");
	}

}

/*****************************************************************************************
 * To handle different operators for character based filter fields
 ******************************************************************************************/
var titleText = "Type the values as a comma-separated list without space, " + "or select them from the drop-down list towards the right";

function fillTbxChar(id) {
	var filterBy = document.getElementById("filter." + id);
	trim(filterBy.value);
	if (filterBy.selectedIndex == 0) return;
	var op = document.getElementById("filterOp." + id).value;
	var td = document.getElementById("td4." + id);

	if ((op == 'nin' || op == 'in')) {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");

		txtBx.setAttribute("onkeypress", "return validateTextField(event,'filterVal." + id + "');");
		txtBx.setAttribute("onchange", "return validateTextField(event,'filterVal." + id + "');");
		txtBx.setAttribute("onBlur", "return trimText(this);");
		txtBx.setAttribute("title", titleText);
		td.appendChild(txtBx);
		txtBx.id = "filterVal." + id;

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		sel.setAttribute("onChange", "addIntoTextBox(this," + id + ");");
		td.appendChild(sel);

		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		if ((filterBy.value != "")) {
			values = filterValues[trim(filterBy.value)];
			loadSelectBox(sel, values, null, null, "(All)", "");
		} else {
			loadSelectBox(sel, null, null, null, "(All)", "");
		}
		sel.selectedIndex = 0;
	} else if ((op == 'eq' || op == 'ne')) {
		td.innerHTML = '';
		if (notHrzGrpble[filterBy.value] == true) {
			var txtBx = document.createElement("input");
			txtBx.type = "text";
			txtBx.name = "filterVal." + id;
			txtBx.id = "filterVal." + id;
			txtBx.setAttribute("style", "width:11em;");
			txtBx.setAttribute("onkeypress", "return enterAlphaNumericalsExt(event)");
			txtBx.setAttribute("onchange", "return enterAlphaNumericalsExt(event)");
			td.appendChild(txtBx);

			var spc2 = document.createElement("span");
			spc2.id = "spc2." + id;
			spc2.innerHTML = '&nbsp;&nbsp;';
			td.appendChild(spc2);

			var sel = document.createElement('select');
			sel.name = "sel." + id;
			sel.id = "sel." + id;
			sel.setAttribute("style", "width:11em");
			sel.setAttribute("class", "dropDown");
			var filtOpt = document.createElement('option');
			sel.appendChild(filtOpt);
			sel.options[0].text = "--(All)--";
			sel.options[0].value = "";
			td.appendChild(sel);
			sel.disabled = true;
		} else {
			var txtBx = document.createElement("input");
			txtBx.type = "text";
			txtBx.name = "txt." + id;
			txtBx.id = "txt." + id;
			txtBx.setAttribute("style", "width:11em;");
			txtBx.setAttribute("onkeypress", "return enterAlphaNumericalsExt(event)");
			txtBx.setAttribute("onchange", "return enterAlphaNumericalsExt(event)");
			txtBx.setAttribute("disabled", "true");
			td.appendChild(txtBx);

			var spc2 = document.createElement("span");
			spc2.id = "spc2." + id;
			spc2.innerHTML = '&nbsp;&nbsp;';
			td.appendChild(spc2);

			var sel = document.createElement('select');
			sel.name = "filterVal." + id;
			sel.id = "filterVal." + id;
			sel.setAttribute("style", "width:11em");
			sel.setAttribute("class", "dropDown");
			var filtOpt = document.createElement('option');
			sel.appendChild(filtOpt);
			sel.options[0].text = "--(All)--";
			sel.options[0].value = "";
			if ((filterBy.value != "")) {
				values = filterValues[trim(filterBy.value)];
				loadSelectBox(sel, values, null, null, "(All)", "");
			} else {
				loadSelectBox(sel, null, null, null, "(All)", "");
			}
			td.appendChild(sel);
		}
	} else if (op == 'null') {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "txt." + id;
		txtBx.id = "txt." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("disabled", "true");
		td.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = 'filterVal.' + id;
		sel.id = 'filterVal.' + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		td.appendChild(sel);
		sel.appendChild(filtOpt);
		sel.length = 2;
		sel.options[0].text = "yes";
		sel.options[0].value = "y";
		sel.options[1].text = "no";
		sel.options[1].value = "n";
		sel.id = 'filterVal.' + id;
		sel.name = 'filterVal.' + id;
	} else if (op == 'between') {
		td.innerHTML = '';
		var txt1 = document.createElement('input');
		txt1.type = "text";
		txt1.name = "filterVal." + id;
		txt1.id = "fromVal." + id;
		txt1.setAttribute("style", "width:65px;");
		txt1.setAttribute("onkeypress", "return enterAlphaNumericalsExt(event)");
		txt1.setAttribute("onchange", "return enterAlphaNumericalsExt(event)");
		td.appendChild(txt1);

		var txt2 = document.createElement('input');
		txt2.type = "text";
		txt2.name = "filterVal." + id;
		txt2.id = "toVal." + id;
		txt2.setAttribute("onkeypress", "return enterAlphaNumericalsExt(event)");
		txt2.setAttribute("style", "width:65px;");
		txt2.setAttribute("onchange", "return enterAlphaNumericalsExt(event)");
		td.appendChild(txt2);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		td.appendChild(sel);

		sel.setAttribute("disabled", "true");
	} else if ((op == 'lt' || op == 'gt' || op == 'le' || op == 'ge' || op == 'co' || op == 'ico' || op == 'sw' || op == 'isw' || op == 'ew' || op == 'iew')) {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("onkeypress", "return enterAlphaNumericalsExt(event)");
		txtBx.setAttribute("onchange", "return enterAlphaNumericalsExt(event)");
		td.appendChild(txtBx);
		txtBx.id = "filterVal." + id;

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		td.appendChild(sel);

		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		sel.selectedIndex = 0;
		sel.setAttribute("disabled", "true");
	}
}

function enterDateNumericals(e)
{
	var key=0;
	if(window.event || !e.which){
		key = e.keyCode;
	}
	else{
		key = e.which;
	}
	if((key>=48)&&(key<=57)||key==8||key==9||key==45 || key==0 ||key==37||key==38||key==39 || key==32 || key==40 || key==58){
		key=key;
		return true;
	}
	else{
		key=0;
		return false;
	}
}


function fillTbxTStamp(id){
	var filterBy = document.getElementById("filter." + id);
	trim(filterBy.value);
	if (filterBy.selectedIndex == 0) return;
	var op = document.getElementById("filterOp." + id).value;
	var td = document.getElementById("td4." + id);
	titleText="Please enter date in DD-MM-[YY]YY [hh:mm:ss] format.";
	if ((op == 'nin' || op == 'in')) {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");

		txtBx.setAttribute("onkeypress", "return validateTimeStampField(event,'filterVal." + id + "');");
		txtBx.setAttribute("onchange", "return validateTimeStampField(event,'filterVal." + id + "');");
		txtBx.setAttribute("onBlur", "validateFilterTimeStampFormat(this);");
		txtBx.setAttribute("title", titleText);
		td.appendChild(txtBx);
		txtBx.id = "filterVal." + id;

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		sel.setAttribute("onChange", "addIntoTextBox(this," + id + ");");
		td.appendChild(sel);

		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		if ((filterBy.value != "")) {
			values = filterValues[trim(filterBy.value)];
			loadSelectBox(sel, values, null, null, "(All)", "");
		} else {
			loadSelectBox(sel, null, null, null, "(All)", "");
		}
		sel.selectedIndex = 0;
	} else if ((op == 'eq' || op == 'ne')) {
		td.innerHTML = '';
		if (notHrzGrpble[filterBy.value] == true) {
			var txtBx = document.createElement("input");
			txtBx.type = "text";
			txtBx.name = "filterVal." + id;
			txtBx.id = "filterVal." + id;
			txtBx.setAttribute("style", "width:11em;");
			txtBx.setAttribute("onkeypress", "return enterDateNumericals(event)");
			txtBx.setAttribute("onchange", "return enterDateNumericals(event)");
			txtBx.setAttribute("onBlur", "validateFilterTimeStampFormat(this);");
			txtBx.setAttribute("title", titleText);
			td.appendChild(txtBx);

			var spc2 = document.createElement("span");
			spc2.id = "spc2." + id;
			spc2.innerHTML = '&nbsp;&nbsp;';
			td.appendChild(spc2);

			var sel = document.createElement('select');
			sel.name = "sel." + id;
			sel.id = "sel." + id;
			sel.setAttribute("style", "width:11em");
			sel.setAttribute("class", "dropDown");
			var filtOpt = document.createElement('option');
			sel.appendChild(filtOpt);
			sel.options[0].text = "--(All)--";
			sel.options[0].value = "";
			td.appendChild(sel);
			sel.disabled = true;
		} else {
			var txtBx = document.createElement("input");
			txtBx.type = "text";
			txtBx.name = "txt." + id;
			txtBx.id = "txt." + id;
			txtBx.setAttribute("style", "width:11em;");
			txtBx.setAttribute("onkeypress", "return enterDateNumericals(event)");
			txtBx.setAttribute("onchange", "return enterDateNumericals(event)");
			txtBx.setAttribute("onBlur", "validateFilterTimeStampFormat(this);");
			txtBx.setAttribute("title", titleText);
			txtBx.setAttribute("disabled", "true");
			td.appendChild(txtBx);

			var spc2 = document.createElement("span");
			spc2.id = "spc2." + id;
			spc2.innerHTML = '&nbsp;&nbsp;';
			td.appendChild(spc2);

			var sel = document.createElement('select');
			sel.name = "filterVal." + id;
			sel.id = "filterVal." + id;
			sel.setAttribute("style", "width:11em");
			sel.setAttribute("class", "dropDown");
			var filtOpt = document.createElement('option');
			sel.appendChild(filtOpt);
			sel.options[0].text = "--(All)--";
			sel.options[0].value = "";
			if ((filterBy.value != "")) {
				values = filterValues[trim(filterBy.value)];
				loadSelectBox(sel, values, null, null, "(All)", "");
			} else {
				loadSelectBox(sel, null, null, null, "(All)", "");
			}
			td.appendChild(sel);
		}
	} else if (op == 'null') {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "txt." + id;
		txtBx.id = "txt." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("disabled", "true");
		td.appendChild(txtBx);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = 'filterVal.' + id;
		sel.id = 'filterVal.' + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		td.appendChild(sel);
		sel.appendChild(filtOpt);
		sel.length = 2;
		sel.options[0].text = "yes";
		sel.options[0].value = "y";
		sel.options[1].text = "no";
		sel.options[1].value = "n";
		sel.id = 'filterVal.' + id;
		sel.name = 'filterVal.' + id;
	} else if (op == 'between') {
		td.innerHTML = '';
		var txt1 = document.createElement('input');
		txt1.type = "text";
		txt1.name = "filterVal." + id;
		txt1.id = "fromVal." + id;
		txt1.setAttribute("style", "width:65px;");
		txt1.setAttribute("onkeypress", "return enterDateNumericals(event)");
		txt1.setAttribute("onchange", "return enterDateNumericals(event)");
		txt1.setAttribute("title", titleText);
		txt1.setAttribute("onBlur", "validateFilterTimeStampFormatForBetw(this,"+id+");");
		td.appendChild(txt1);

		var txt2 = document.createElement('input');
		txt2.type = "text";
		txt2.name = "filterVal." + id;
		txt2.id = "toVal." + id;
		txt2.setAttribute("onkeypress", "return enterDateNumericals(event)");
		txt2.setAttribute("style", "width:65px;");
		txt2.setAttribute("title", titleText);
		txt2.setAttribute("onchange", "return enterDateNumericals(event)");
		txt2.setAttribute("onBlur", "validateFilterTimeStampFormatForBetw(this,"+id+");");
		td.appendChild(txt2);

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		td.appendChild(sel);

		sel.setAttribute("disabled", "true");
	} else if ((op == 'lt' || op == 'gt' || op == 'le' || op == 'ge' || op == 'co' || op == 'ico' || op == 'sw' || op == 'isw' || op == 'ew' || op == 'iew')) {
		td.innerHTML = '';
		var txtBx = document.createElement("input");
		txtBx.type = "text";
		txtBx.name = "filterVal." + id;
		txtBx.setAttribute("style", "width:11em;");
		txtBx.setAttribute("onkeypress", "return enterDateNumericals(event)");
		txtBx.setAttribute("onchange", "return enterDateNumericals(event)");
		txtBx.setAttribute("onBlur", "validateFilterTimeStampFormat(this);");
		txtBx.setAttribute("title", titleText);
		td.appendChild(txtBx);
		txtBx.id = "filterVal." + id;

		var spc2 = document.createElement("span");
		spc2.id = "spc2." + id;
		spc2.innerHTML = '&nbsp;&nbsp;';
		td.appendChild(spc2);

		var sel = document.createElement('select');
		sel.name = "sel." + id;
		sel.id = "sel." + id;
		sel.setAttribute("style", "width:11em");
		sel.setAttribute("class", "dropDown");
		td.appendChild(sel);

		var filtOpt = document.createElement('option');
		sel.appendChild(filtOpt);
		sel.options[0].text = "--(All)--";
		sel.options[0].value = "";
		sel.selectedIndex = 0;
		sel.setAttribute("disabled", "true");
	}

}

/*****************************************************************************************
 * To delegate to various filter operator handlers, based on filter field types.
 ******************************************************************************************/
function fillTBx(id) {
	var filterBy = document.getElementById("filter." + id);
	trim(filterBy.value);
	if (filterBy.selectedIndex == 0) return;
	if (filterTyps[filterBy.value] == 'numeric' || filterTyps[filterBy.value]=='date') {
		fillTbxNum(id);
	} else {
		if(filterTyps[filterBy.value] == 'timestamp')
			fillTbxTStamp(id);
		else
			fillTbxChar(id);
	}
}

// function to handle addition of text into the filterVal text-box.
var selArray = new Array();
var indx = 0;

function addIntoTextBox(sel, id) {
	var txtBx
	if (document.getElementById("fvalue" + id)) txtBx = document.getElementById("fvalue" + id);
	else txtBx = document.getElementById("filterVal." + id);
	var tbv = (txtBx.value).split(",");
	var selIndx = sel.selectedIndex;
	if (selIndx == 0) return;
	var selected = sel.options[selIndx].text;
	for (indx = 0; indx < tbv.length; indx++) {
		if (tbv[indx] == selected) {
			return;
		}
	}
	var lastn = tbv[tbv.length - 1];
	var laslen = lastn.length;
	if (lastn.charAt(laslen - 1) == '') tbv = tbv + selected;
	else if (tbv.length > 0 && tbv[0] != '') tbv = tbv + ',' + selected;
	else if (tbv.length > 1 && tbv[0] == '') tbv = tbv + ',' + selected;
	else tbv = tbv + selected;
	txtBx.value = tbv;
}

//To convert non-numeric data to numeric data.
function trimNum(field) {
	var tbValue = field.value;		// multiple inputs not allowed for numbers.

	tbValue =  (tbValue.trim()) ;

	// Upon copy and paste(Ctrl-V),
	// removes all other characters excluding numerals, periods and hyphens.
	tbValue = tbValue.replace(/[^\d\.\-]/g, "");

	//matches continuous periods  and hyphens, and removes them.
	tbValue = tbValue.replace(/(\.)\1/g, "");
	tbValue = tbValue.replace(/(\-)\1/g, "");

	//matches duplicate periods and removes them
	tbValue = tbValue.replace(/(\-*\d*\.\d*\-*)?\./g, function($0, $1){
		return $1 ? $1 + '' : $0;
		});

	//matches duplicate hyphens and removes them
	tbValue = tbValue.replace(/(\-\d*\.*)?\-/g, function($0, $1){
		return $1 ? $1 + '' : $0;
		});

	//matches a hyphen preceded by number and removes it
	tbValue = tbValue.replace(/(\d+\.*)?\-/g, function($0, $1){
		return $1 ? $1 + '' : $0;
	});

	// if there are no digits at all, empty it out.
	if (!/\d/.test(tbValue))
		tbValue = '';
	field.value = tbValue;
}

//removes extra space between towards left and right of text.
function trimText(field) {
	var txtBx = field;
	var tbValues = (txtBx.value).split(",");
	for (var i = 0; i < tbValues.length; i++) {
		tbValues[i] = tbValues[i].trim();
	}
	txtBx.value = tbValues;
}

/****************************************************************************************************
 * Validate function that allows commas and spaces, only before or after an alphanumeric character.
 ****************************************************************************************************/

function validateTextField(e, field) {
	var key = 0;
	if (window.event) {
		key = e.keyCode;
	} else {
		key = e.which;
	}
	if ((key >= 65) && (key <= 90) || (key >= 97) && (key <= 122) || (key >= 48) && (key <= 57) || key == 8 || key == 9 || key == 32 || key == 38 || key == 40 || key == 41 || key == 44 || key == 46 || key == 0) {
		if (key == 44 || key == 9 || key == 32) {
			var src = document.getElementById(field.toString());
			var val = src.value;
			var len = val.length;
			var iCaretPos = 0;
			if (src.selectionStart || src.selectionStart == '0') {
				iCaretPos = src.selectionStart;
			}
			var prev = val.charCodeAt((iCaretPos - 1));
			if (((prev >= 65) && (prev <= 90)) || ((prev >= 97) && (prev <= 122)) || ((prev >= 48) && (prev <= 57)) || (prev == 46) || (prev == 41)) {
				if (iCaretPos > 0 && iCaretPos != len) {
					var next = val.charCodeAt((iCaretPos));
					if (((next >= 65) && (next <= 90)) || ((next >= 97) && (next <= 122)) || ((next >= 48) && (next <= 57)) || (next == 46) || (next == 0) || (next == 40)) {
						return true;
					} else {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		return true;
	} else {
		return false;
	}
}

/****************************************************************************************************
 * Validate function that allows commas and spaces, only before or after a numeral.
 ****************************************************************************************************/

function validateNumericField(e, field) {
	var key = 0;
	if (window.event) {
		key = e.keyCode;
	} else {
		key = e.which;
	}
	if ((key >= 48) && (key <= 57) || key == 8 || key == 9 || key == 32 || key == 44 || key == 45 || key == 46 || key == 0) {
		if (key == 44 || key == 9 || key == 32) {
			var src = document.getElementById(field.toString());
			var val = src.value;
			var len = val.length;
			var iCaretPos = 0;
			if (src.selectionStart || src.selectionStart == '0') {
				iCaretPos = src.selectionStart;
			}
			var prev = val.charCodeAt((iCaretPos - 1));
			if (((prev >= 48) && (prev <= 57)) || (prev == 46)) {
				if (iCaretPos > 0 && iCaretPos != len) {
					var next = val.charCodeAt((iCaretPos));
					if (((next >= 48) && (next <= 57)) || (next == 46) || (next == 0)) {
						return true;
					} else {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		return true;
	} else {
		return false;
	}
}

/****************************************************************************************************
 * Validate function that allows commas, hyphen, colon and spaces, only before or after an numeric character.
 ****************************************************************************************************/
function validateTimeStampField(e, field) {
	var key = 0;
	if (window.event) {
		key = e.keyCode;
	} else {
		key = e.which;
	}
	if ((key >= 48) && (key <= 57)|| key==49 || key==45 || key == 8 || key == 9 || key == 32 || key == 44 || key == 45 || key == 46 || key == 0) {
		if (key == 44 || key == 9 || key == 32 ||  key==49 || key==45 ) {
			var src = document.getElementById(field.toString());
			var val = src.value;
			var len = val.length;
			var iCaretPos = 0;
			if (src.selectionStart || src.selectionStart == '0') {
				iCaretPos = src.selectionStart;
			}
			var prev = val.charCodeAt((iCaretPos - 1));
			if (((prev >= 48) && (prev <= 57)) || (prev == 46)) {
				if (iCaretPos > 0 && iCaretPos != len) {
					var next = val.charCodeAt((iCaretPos));
					if (((next >= 48) && (next <= 57)) || (next == 46) || (next == 0)) {
						return true;
					} else {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		}
		return true;
	} else {
		return false;
	}
}




/****************************************************************************************************
 * function to eliminate the addition of duplicate filters.
 ****************************************************************************************************/

function checkDuplicateFilter(em, id) {
	if (id == num - 1) last = num - 2;
	else last = num - 1;
	first = no_of_static_fields - 1;
	for (var i = last; i > id; i--) {
		if (document.getElementById("filter." + i) && document.getElementById("filter." + i).selectedIndex != 0) {
			if ((document.getElementById("filter." + i).value) == em.value) {
				alert("duplicate filter.");
				em.selectedIndex = 0;
				if (document.getElementById("filterVal." + id)) document.getElementById("filterVal." + id).disabled = true;
				return;
			}
		}
	}
	for (i = first; i < id; i++) {
		if (document.getElementById("filter." + i) && document.getElementById("filter." + i).selectedIndex != 0) {
			if ((document.getElementById("filter." + i).value) == em.value) {
				alert("duplicate filter.");
				em.selectedIndex = 0;
				if (document.getElementById("filterVal." + id)) document.getElementById("filterVal." + id).disabled = true;
				return;
			}
		}
	}

}

/*****************************************************************************************
 *-------------------------List Fields addition and Processing-----------------------------
 ******************************************************************************************/
var avlFlds;
var selFlds;

function swapOptions(obj, i, j) {
	var o = obj.options;
	var i_selected = o[i].selected;
	var j_selected = o[j].selected;
	var temp = new Option(o[i].text, o[i].value, o[i].title, o[i].defaultSelected, o[i].selected);
	temp.setAttribute("title", o[i].title);
	var temp2 = new Option(o[j].text, o[j].value, o[j].title, o[j].defaultSelected, o[j].selected);
	temp2.setAttribute("title", o[j].title);

	o[i] = temp2;
	o[j] = temp;
	o[i].selected = j_selected;
	o[j].selected = i_selected;
}

function moveOptionUp(obj) {
	if (!hasOptions(obj)) {
		return;
	}
	for (i = 0; i < obj.options.length; i++) {
		if (obj.options[i].selected) {
			if (i != 0 && !obj.options[i - 1].selected) {
				swapOptions(obj, i, i - 1);
				obj.options[i - 1].selected = true;
			}
		}
	}
}

function moveOptionDown(obj) {
	if (!hasOptions(obj)) {
		return;
	}
	for (i = obj.options.length - 1; i >= 0; i--) {
		if (obj.options[i].selected) {
			if (i != (obj.options.length - 1) && !obj.options[i + 1].selected) {
				swapOptions(obj, i, i + 1);
				obj.options[i + 1].selected = true;
			}
		}
	}
}

function sortSelect(obj) {
	var o = new Array();
	if (!hasOptions(obj)) {
		return;
	}
	for (var i = 0; i < obj.options.length; i++) {
		o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
		(o[i]).title = obj.options[i].title;
		(o[i]).value = obj.options[i].value;

	}
	if (o.length == 0) {
		return;
	}
	o = o.sort(function(val1, val2) {
		if(val2.text+"" == "(Summary Fields)")
			return -1;

		if ((val1.text + "") < (val2.text + "")) {
			return - 1;
		}
		if ((val1.text + "") > (val2.text + "")) {
			return 1;
		}
		return 0;
	});

	for (var i = 0; i < o.length; i++) {
		obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
		obj.options[i].title = o[i].title;
		obj.options[i].value = o[i].value;
	}
}


function sortSelectForOrder(obj) {
	var o = new Array();
	if (!hasOptions(obj)) {
		return;
	}
	for (var i = 0; i < obj.options.length; i++) {
		o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
		(o[i]).title = obj.options[i].title;
		(o[i]).value = obj.options[i].value;

	}
	if (o.length == 0) {
		return;
	}
	o = o.sort(function(val1, val2) {

		if(val1.text+"" == "-- Select --")
			return -1;

		if ((val1.text + "") < (val2.text + "")) {
			return - 1;
		}
		if ((val1.text + "") > (val2.text + "")) {
			return 1;
		}
		return 0;
	});

	for (var i = 0; i < o.length; i++) {
		obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
		obj.options[i].title = o[i].title;
		obj.options[i].value = o[i].value;
	}
}

function createListElements(from, to) {
	avlFlds = document.getElementById(from);
	selFlds = document.getElementById(to);
}

function hasOptions(obj) {
	if (obj != null && obj.options != null) {
		return true;
	}
	return false;
}

/*
 * Move a single named field (no need to mark as selected)
 * from one list to another
 */
function moveSelectedOption(fromList, toList, fieldName) {
	if (!hasOptions(fromList)) {
		return;
	}
	for (var i = 0; i < fromList.options.length; i++) {
		var o = fromList.options[i];
		if (o.value == fieldName) {
			if (!hasOptions(toList)) {
				var index = 0;
			} else {
				var index = toList.options.length;
			}
			toList.options[index] = new Option(o.text, o.value, o.title, false, false);
			toList.options[index].setAttribute("title", o.title);

			// Delete the selected options from  the available list.
			fromList.options[i] = null;
			break;
		}
	}

	// Only the 'toList' may need sorting
	if (toList.id=='avlbListFlds' || toList.id=='avlbSummFlds') {
		sortSelect(toList);
	}

	fromList.selectedIndex = -1;
	toList.selectedIndex = -1;
}

/*
 * Move all fields in the from list marked as selected to the to list
 */
function moveSelectedOptions(from, to, sort) {

	if (!hasOptions(from)) {
		return;
	}
	for (var i = 0; i < from.options.length; i++) {
		var o = from.options[i];
		if (o.selected) {
			if (!hasOptions(to)) {
				var index = 0;
			} else {
				var index = to.options.length;
			}
			to.options[index] = new Option(o.text, o.value, o.title, false, false);
			to.options[index].setAttribute("title", o.title);
		}
	}
	// Delete the selected options from  the available list.
	for (var i = (from.options.length - 1); i >= 0; i--) {
		var o = from.options[i];
		if (o.selected) {
			from.options[i] = null;
		}
	}
	//********If needed, the fields in the list can be sorted after addition or deletion.******
	if(from.id=='avlbListFlds' || from.id=='avlbSummFlds'){
		sortSelect(from);
	}else if(to.id=='avlbListFlds' || to.id=='avlbSummFlds'){
		sortSelect(to);
	}
	from.selectedIndex = -1;
	to.selectedIndex = -1;
}

function addListFields() {
	var type = getRadioSelection(document.inputform.reportType);
	if (type == 'list') {
		createListElements('avlbListFlds', 'listFields');
		moveSelectedOptions(avlFlds, selFlds, 'from');

	} else {
		createListElements('avlbSummFlds', 'sumFields');
		moveSelectedOptions(avlFlds, selFlds, 'from');
	}
}

function removeListFields() {
	var type = getRadioSelection(document.inputform.reportType);
	if (type == 'list') {
		createListElements('avlbListFlds', 'listFields');
		moveSelectedOptions(selFlds, avlFlds);

	} else {
		createListElements('avlbSummFlds', 'sumFields');
		moveSelectedOptions(selFlds, avlFlds);
	}
}


function disableSortFields(){
	document.getElementById("customOrder1").selectedIndex = 0;
	document.getElementById("customOrder1").disabled = true;
	document.getElementById("sort1").disabled = true;
	document.getElementById("customOrder2").selectedIndex = 0;
	document.getElementById("customOrder2").disabled = true;
	document.getElementById("sort2").disabled = true;
}

function enableSortFields(){
	document.getElementById("customOrder1").removeAttribute("disabled");
	document.getElementById("sort1").removeAttribute("disabled");
	document.getElementById("customOrder2").removeAttribute("disabled");
	document.getElementById("sort2").removeAttribute("disabled");
}
/*****************************************************************************************
 *-------------------------Report Builder Processing---------------------------------------
 ******************************************************************************************/

function onChangeReportType() {
	var type = getRadioSelection(document.inputform.reportType);
	if (type == 'list') {
		document.getElementById('listFieldsDiv').style.display = 'block';
		if(document.getElementById('listGroupsDiv'))
			document.getElementById('listGroupsDiv').style.display = 'block';
		document.getElementById('sumFieldsDiv').style.display = 'none';
		document.getElementById('sumGroupsDiv').style.display = 'none';
		document.getElementById('trendGroupsDiv').style.display = 'none';
		document.getElementById('verticalTrendGroupsDiv').style.display = 'none';
		if (document.getElementById("chart") != null) {
			document.getElementById("chart").disabled = true;
		}
		enableSortFields();
	} else if (type == 'sum') {
		document.getElementById('listFieldsDiv').style.display = 'none';
		if(document.getElementById('listGroupsDiv'))
			document.getElementById('listGroupsDiv').style.display = 'none';
		document.getElementById('sumFieldsDiv').style.display = 'block';
		document.getElementById('sumGroupsDiv').style.display = 'block';
		document.getElementById('trendGroupsDiv').style.display = 'none';
		document.getElementById('verticalTrendGroupsDiv').style.display = 'none';
		if (document.getElementById("chart") != null) {
			document.getElementById("chart").disabled = true;
		}
		disableSortFields();
	} else if (type == 'trend') {
		document.getElementById('listFieldsDiv').style.display = 'none';
		if(document.getElementById('listGroupsDiv'))
			document.getElementById('listGroupsDiv').style.display = 'none';
		document.getElementById('sumFieldsDiv').style.display = 'block';
		document.getElementById('sumGroupsDiv').style.display = 'none';
		document.getElementById('trendGroupsDiv').style.display = 'block';
		document.getElementById('verticalTrendGroupsDiv').style.display = 'none';
		if (document.getElementById("chart") != null) {
			document.getElementById("chart").disabled = false;
		}
		disableSortFields();
	} else if (type == 'vtrend') {
		document.getElementById('listFieldsDiv').style.display = 'none';
		if(document.getElementById('listGroupsDiv'))
			document.getElementById('listGroupsDiv').style.display = 'none';
		document.getElementById('sumFieldsDiv').style.display = 'block';
		document.getElementById('sumGroupsDiv').style.display = 'none';
		document.getElementById('trendGroupsDiv').style.display = 'none';
		document.getElementById('verticalTrendGroupsDiv').style.display = 'block';
		if (document.getElementById("chart") != null) {
			document.getElementById("chart").disabled = true;
		}
		disableSortFields();
	}
}

function selectedFieldsCharWidth(obj, name, obj1) {
	var len = obj.length;
	var width = 0;
	var temp;
	var tempName;
	for (var indx = 0; indx < obj.length; indx++) {
		temp = obj[indx].value;
		if (obj1[temp] && obj1[temp] != null) {
			width += parseInt(parseInt(obj1[temp]) / 5);
			width += 1; //  space after every column
		}
	}
	width = width - 1; // no space after the last column
	return width;
}

function checkedCheckBoxsMaxCharWidth(obj, name, obj1) {
	var len = obj.length;
	var width = 0;
	var dataWidth = 0;
	var temp;
	var tempName;
	for (var indx = 0; indx < obj.length; indx++) {
		temp = obj[indx].value;
		if (temp != '_data' && temp != '_count') {
			if (obj1[temp] && obj1[temp] != null) {
				width = parseInt(parseInt(obj1[temp]) / 5);
				width += 1; //  space after every column
				dataWidth = (width > dataWidth) ? width: dataWidth;
			}
		}
	}
	if (dataWidth > 0) return dataWidth - 1;
	else return dataWidth;
}

function checkedCheckBoxsMaxWidth(obj, name, obj1) {
	var len = obj.length;
	var width = 0;
	var dataWidth = 0;
	var temp;
	var tempName;
	for (var indx = 0; indx < obj.length; indx++) {
		temp = obj[indx].value;
		if (temp != '_data' && temp != '_count') {
			if (obj1[temp] && obj1[temp] != null) {
				width = parseInt(obj1[temp]);
				dataWidth = (width > dataWidth) ? width: dataWidth;
			}
		}
	}
	return dataWidth;
}

function summaryValueCount(name) {
	var valArr = new Array();
	valArr = filterValues[name];
	if (!valArr)  return 1;
	else{
		var arrLen = valArr.length;
		if (allowNull[name] == 'true') ++arrLen; ++arrLen;
		return arrLen;
	}
}

function validateReport(val) {
	var type = getRadioSelection(document.inputform.reportType);
	var listChkBox = checkBoxsLength(document.inputform.listFields);
	var sumChkBox = checkBoxsLength(document.inputform.sumFields);
	var listCheckedChkBoxs = checkedCheckBoxsLength(document.inputform.listFields);
	var sumCheckedChkBoxs = checkedCheckBoxsLength(document.inputform.sumFields);
	if (type == 'list') {
		if (listCheckedChkBoxs == 0) {
			alert("At least one field must be selected");
			return false;
		}
		if (val == 'pdf') {
			var totalWidth = checkedCheckBoxsWidth(document.inputform.listFields, 'listFields', fieldWidth);
			var fontSize = parseInt(document.inputform.baseFontSize.value);
			var assumedPDFWidth = parseInt(totalWidth * (fontSize / 10));
			var actualPDFWidth = parseInt(741);
			// assumption Landscape orientation 841-100 (50+50);
			if (assumedPDFWidth > actualPDFWidth) {
				var result = confirm("Too many fields selected, some fields may be truncated in the PDF.\nRemove some fields or reduce font size to prevent truncation.");
				if (result) return true;
				else return false;
			}
		} else if (val == 'text') {
			var totalWidth = selectedFieldsCharWidth(document.inputform.listFields, 'listFields', fieldWidth);
			var res;
			if (totalWidth >= 120) res = confirm("Total character width: " + totalWidth + ". Too many fields selected.\nSome fields may be truncated in the Print. Remove some fields to prevent this.");
			else if (totalWidth >= 96 && totalWidth < 120) res = confirm("Net. Character Width: " + totalWidth + "\n Please use Condensed mode for printing.");
			else if (totalWidth >= 80 && totalWidth < 96) res = confirm("Net. Character Width: " + totalWidth + "\n Please use 12 CPI mode for printing.");
			else res = true;

			if (res) return true;
			else return false;
		}

	} else if (type == 'sum') {
		if (sumCheckedChkBoxs == 0) {
			alert("At least one field must be selected");
			return false;
		} else if (sumCheckedChkBoxs == 1) {
			if (document.inputform.sumGroupHoriz.value == '_data') {
				alert("Please select another field for Horizontal Axis - (Summary Fields) will be shown by default since only one exists");
				document.inputform.sumGroupHoriz.selectedIndex = 0;
				return false;
			}
			if (document.inputform.sumGroupVertSub.value == '_data') {
				alert("Please select another field (optional) for Vertical Axis Sub - (Summary Fields) will be shown by default since only one exists");
				document.inputform.sumGroupVertSub.selectedIndex = 0;
				return false;
			}
		} else {
			if ((document.inputform.sumGroupHoriz.value != '_data' && document.inputform.sumGroupVertSub.value != '_data') || (document.inputform.sumGroupHoriz.value == '_data' && document.inputform.sumGroupVertSub.value == '_data')) {
				alert("Please select (Summary Fields) for either Horizontal Axis or Vertical Axis Sub ");
				document.inputform.sumGroupHoriz.selectedIndex = 0;
				document.inputform.sumGroupVertSub.selectedIndex = 0;
				return false;
			}
		}
		if (document.inputform.sumGroupVert.selectedIndex == 0) {
			alert("Please select Vertical Axis");
			return false;
		}
		if (val == 'pdf') {
			var fontSize = parseInt(document.inputform.baseFontSize.value);
			var actualPDFWidth = parseInt(741);
			if (document.inputform.sumGroupHoriz.value == '_data') {
				if (document.inputform.sumGroupVertSub.selectedIndex != 0) {
					var totalFldWidth = checkedCheckBoxsWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					for (var i = 0; i < document.inputform.sumFields.options.length; i++) {
						if (document.inputform.sumFields.options[i].value == '_count') totalFldWidth += 40;
					}
					totalFldWidth += parseInt(fieldWidth[document.inputform.sumGroupVert.value]);
					totalFldWidth += parseInt(fieldWidth[document.inputform.sumGroupVertSub.value]);
					var assumedPDFWidth = parseInt(totalFldWidth * (fontSize / 10));
					if (assumedPDFWidth > actualPDFWidth) {
						var result = confirm("Too many fields selected !!!\n Some fields may be Truncated in the PDF Print...");
						if (result) return true;
						else return false;
					}

				} else {
					var totalFldWidth = checkedCheckBoxsWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					for (var i = 0; i < document.inputform.sumFields.options.length; i++) {
						if (document.inputform.sumFields.options[i].value == '_count') totalFldWidth += 40;
					}

					totalFldWidth += parseInt(fieldWidth[document.inputform.sumGroupVert.value]);
					var assumedPDFWidth = parseInt(totalFldWidth * (fontSize / 10));
					if (assumedPDFWidth > actualPDFWidth) {
						var result = confirm("Too many fields selected !!!\n Some fields may be Truncated in the PDF Print...");
						if (result) return true;
						else return false;
					}
				}
			} else if (document.inputform.sumGroupVertSub.value == '_data') {
				if (document.inputform.sumGroupHoriz.selectedIndex != 0) {
					var maxsumFldWidth = checkedCheckBoxsMaxWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					var maxHrzFldWidth = parseInt(fieldWidth[document.inputform.sumGroupHoriz.value]);
					var maxWidth = (maxsumFldWidth > maxHrzFldWidth) ? maxsumFldWidth: maxHrzFldWidth;
					var totalFldWidth = parseInt(fieldWidth[document.inputform.sumGroupVert.value]);
					var sumValCnt = summaryValueCount(document.inputform.sumGroupHoriz.value);
					totalFldWidth += maxsumFldWidth;
					totalFldWidth += (maxWidth * sumValCnt);
					var assumedPDFWidth = parseInt(totalFldWidth * (fontSize / 10));
					if (assumedPDFWidth > actualPDFWidth) {
						var result = confirm("Too many fields selected !!!\n Some fields may be Truncated in the PDF Print...");
						if (result) return true;
						else return false;
					}
				} else {
					var maxsumFldWidth = checkedCheckBoxsMaxWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					var totalFldWidth = parseInt(fieldWidth[document.inputform.sumGroupVert.value]);
					totalFldWidth += (maxsumFldWidth * 2);
					var assumedPDFWidth = parseInt(totalFldWidth * (fontSize / 10));
					if (assumedPDFWidth > actualPDFWidth) {
						var result = confirm("Too many fields selected !!!\n Some fields may be Truncated in the PDF Print...");
						if (result) return true;
						else return false;
					}
				}
			} else if (document.inputform.sumGroupHoriz.value != '_data' && document.inputform.sumGroupVertSub.value != '_data') {
				if (document.inputform.sumGroupHoriz.selectedIndex != 0 && document.inputform.sumGroupVertSub.selectedIndex != 0) {
					var HrzFldWidth = parseInt(fieldWidth[document.inputform.sumGroupHoriz.value]);
					var sumValCntt = summaryValueCount(document.inputform.sumGroupHoriz.value);
					var totalFldWidth = parseInt(fieldWidth[document.inputform.sumGroupVert.value]);
					totalFldWidth += parseInt(fieldWidth[document.inputform.sumGroupVertSub.value]);
					totalFldWidth += (HrzFldWidth * sumValCntt);
					var assumedPDFWidth = parseInt(totalFldWidth * (fontSize / 10));
					if (assumedPDFWidth > actualPDFWidth) {
						var result = confirm("Too many fields selected !!!\n Some fields may be Truncated in the PDF Print...");
						if (result) return true;
						else return false;
					}
				} else if (document.inputform.sumGroupHoriz.selectedIndex != 0 && document.inputform.sumGroupVertSub.selectedIndex == 0) {
					var maxsumFldWidth = checkedCheckBoxsMaxWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					var maxHrzFldWidth = parseInt(fieldWidth[document.inputform.sumGroupHoriz.value]);

					for (var i = 0; i < document.inputform.sumFields.options.length; i++) {
						if (document.inputform.sumFields.options[i].value == '_count') maxsumFldWidth = 40;
					}
					var maxWidth = (maxsumFldWidth > maxHrzFldWidth) ? maxsumFldWidth: maxHrzFldWidth;

					var sumValCnt = summaryValueCount(document.inputform.sumGroupHoriz.value);
					var totalFldWidth = parseInt(fieldWidth[document.inputform.sumGroupVert.value]);
					totalFldWidth += (maxWidth * sumValCnt);
					var assumedPDFWidth = parseInt(totalFldWidth * (fontSize / 10));
					if (assumedPDFWidth > actualPDFWidth) {
						var result = confirm("Field widths exceed page limits!!!\n Some fields may be Truncated in the PDF Print...");
						if (result) return true;
						else return false;
					}

				}
			}
		} else if (val == 'text') {
			var fontSize = parseInt(document.inputform.baseFontSize.value);
			var actualPDFWidth = parseInt(741);
			if (document.inputform.sumGroupHoriz.value == '_data') {
				if (document.inputform.sumGroupVertSub.selectedIndex != 0) {
					var totalFldWidth = selectedFieldsCharWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					for (var i = 0; i < document.inputform.sumFields.options.length; i++) {
						if (document.inputform.sumFields.options[i].value == '_count') {
							totalFldWidth += (40 / 5) + 1;
						}
					}
					totalFldWidth += (parseInt(fieldWidth[document.inputform.sumGroupVert.value]) / 5) + 1;
					totalFldWidth += (parseInt(fieldWidth[document.inputform.sumGroupVertSub.value]) / 5) + 1;
					totalFldWidth--;
					if (totalFldWidth >= 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Too many fields selected !!!\n Some fields may be Truncated in the Print...");
					else if (totalFldWidth >= 96 && totalFldWidth < 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use Condensed mode for printing.");
					else if (totalFldWidth >= 80 && totalFldWidth < 96) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use 12 CPI mode for printing.");
					else res = true;

					if (res) return true;
					else return false;

				} else {
					var totalFldWidth = selectedFieldsCharWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					for (var i = 0; i < document.inputform.sumFields.options.length; i++) {
						if (document.inputform.sumFields.options[i].value == '_count') totalFldWidth += (40 / 5) + 1;
					}

					totalFldWidth += (parseInt(fieldWidth[document.inputform.sumGroupVert.value]) / 5) + 1;
					totalFldWidth--;

					if (totalFldWidth >= 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Too many fields selected !!!\n Some fields may be Truncated in the Print...");
					else if (totalFldWidth >= 96 && totalFldWidth < 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use Condensed mode for printing.");
					else if (totalFldWidth >= 80 && totalFldWidth < 96) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use 12 CPI mode for printing.");
					else res = true;

					if (res) return true;
					else return false;
				}
			} else if (document.inputform.sumGroupVertSub.value == '_data') {
				if (document.inputform.sumGroupHoriz.selectedIndex != 0) {
					var maxsumFldWidth = checkedCheckBoxsMaxCharWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					var maxHrzFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupHoriz.value]) / 5) + 1;

					var maxWidth = (maxsumFldWidth > maxHrzFldWidth) ? maxsumFldWidth: maxHrzFldWidth;
					var totalFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupVert.value]) / 5) + 1;
					var sumValCnt = summaryValueCount(document.inputform.sumGroupHoriz.value);
					totalFldWidth += maxsumFldWidth;
					totalFldWidth += (maxWidth * sumValCnt);
					totalFldWidth--;
					if (totalFldWidth >= 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Too many fields selected !!!\n Some fields may be Truncated in the Print...");
					else if (totalFldWidth >= 96 && totalFldWidth < 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use Condensed mode for printing.");
					else if (totalFldWidth >= 80 && totalFldWidth < 96) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use 12 CPI mode for printing.");
					else res = true;

					if (res) return true;
					else return false;
				} else {
					var maxsumFldWidth = checkedCheckBoxsMaxCharWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					var totalFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupVert.value]) / 5) + 1;
					totalFldWidth += (maxsumFldWidth * 2);
					totalFldWidth--;
					if (totalFldWidth >= 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Too many fields selected !!!\n Some fields may be Truncated in the Print...");
					else if (totalFldWidth >= 96 && totalFldWidth < 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use Condensed mode for printing.");
					else if (totalFldWidth >= 80 && totalFldWidth < 96) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use 12 CPI mode for printing.");
					else res = true;

					if (res) return true;
					else return false;
				}
			} else if (document.inputform.sumGroupHoriz.value != '_data' && document.inputform.sumGroupVertSub.value != '_data') {
				if (document.inputform.sumGroupHoriz.selectedIndex != 0 && document.inputform.sumGroupVertSub.selectedIndex != 0) {
					var HrzFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupHoriz.value]) / 5) + 1;
					var sumValCntt = summaryValueCount(document.inputform.sumGroupHoriz.value);
					var totalFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupVert.value]) / 5) + 1;
					totalFldWidth += (parseInt(fieldWidth[document.inputform.sumGroupVertSub.value]) / 5) + 1;
					totalFldWidth += (HrzFldWidth * sumValCntt);
					totalFldWidth--;
					if (totalFldWidth >= 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Too many fields selected !!!\n Some fields may be Truncated in the Print...");
					else if (totalFldWidth >= 96 && totalFldWidth < 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use Condensed mode for printing.");
					else if (totalFldWidth >= 80 && totalFldWidth < 96) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use 12 CPI mode for printing.");
					else res = true;

					if (res) return true;
					else return false;
				} else if (document.inputform.sumGroupHoriz.selectedIndex != 0 && document.inputform.sumGroupVertSub.selectedIndex == 0) {
					var maxsumFldWidth = checkedCheckBoxsMaxCharWidth(document.inputform.sumFields, 'sumFields', fieldWidth);
					var maxHrzFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupHoriz.value]) / 5) + 1;

					for (var i = 0; i < document.inputform.sumFields.options.length; i++) {
						if (document.inputform.sumFields.options[i].value == '_count') maxsumFldWidth = (40 / 5) + 1;
					}
					var maxWidth = (maxsumFldWidth > maxHrzFldWidth) ? maxsumFldWidth: maxHrzFldWidth;

					var sumValCnt = summaryValueCount(document.inputform.sumGroupHoriz.value);
					var totalFldWidth = (parseInt(fieldWidth[document.inputform.sumGroupVert.value]) / 5) + 1;
					totalFldWidth += (maxWidth * sumValCnt);
					totalFldWidth--;
					if (totalFldWidth >= 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Too many fields selected !!!\n Some fields may be Truncated in the Print...");
					else if (totalFldWidth >= 96 && totalFldWidth < 120) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use Condensed mode for printing.");
					else if (totalFldWidth >= 80 && totalFldWidth < 96) res = confirm("Net. Character Width: " + totalFldWidth + "\n Please use 12 CPI mode for printing.");
					else res = true;

					if (res) return true;
					else return false;

				}
			}
		}
	} else if (type == 'trend') {
		if(document.getElementById('dateFieldSelection').value == 'None'){
			alert("Date range cannot be None for trend reports");
			return false;
		}
		if (sumCheckedChkBoxs == 0) {
			alert("At least one field must be selected");
			return false;
		} else if (sumCheckedChkBoxs == 1) {
			if (document.inputform.trendGroupVertSub.value == '_data') {
				alert("Please select another field (optional) for Vertical Axis Sub - (Summary Fields) will be shown by default since only one exists");
				document.inputform.trendGroupVertSub.selectedIndex = 0;
				return false;
			}
		} else {
			if (document.inputform.trendGroupVertSub.value != '_data') {
				alert("Please select (Summary Fields) for Vertical Axis Sub ");
				return false;
			}
		}
		if (document.inputform.trendGroupVert.selectedIndex == 0) {
			alert("Please select Vertical Axis");
			return false;
		}
		if(val == 'chart'){
			if(document.getElementById('sumFields').length > 20){
				alert('Please select less than 20 Summary  Fields');
				return false;
			}
		}
	} else if (type == 'vtrend') {
		if(document.getElementById('dateFieldSelection').value == 'None'){
			alert("Date range cannot be None for trend reports");
			return false;
		}
		if (sumCheckedChkBoxs == 0) {
			alert("At least one field must be selected");
			return false;
		} else if (sumCheckedChkBoxs == 1) {
			if (document.inputform.vtrendGroupVertSub.value == '_data') {
				alert("Please select another field (optional) for Vertical Axis Sub - (Summary Fields) will be shown by default since only one exists");
				document.inputform.vtrendGroupVertSub.selectedIndex = 0;
				return false;
			}
		} else {
			if ((document.inputform.vtrendGroupHoriz.value != '_data' && document.inputform.vtrendGroupVertSub.value != '_data') || (document.inputform.vtrendGroupHoriz.value == '_data' && document.inputform.vtrendGroupVertSub.value == '_data')) {
				alert("Please select (Summary Fields) for either Horizontal Axis or Vertical Axis Sub ");
				document.inputform.vtrendGroupHoriz.selectedIndex = 0;
				document.inputform.vtrendGroupVertSub.selectedIndex = 0;
				return false;
			}
		}
	}
	return true;
}

function onSubmit(val) {
	setPdfCustomVals();
	if(document.inputform._sel)
		setDateToken();
	document.inputform.outputMode.value = val;
	for (var indx = 0; indx < document.inputform.listFields.length; indx++) {
		document.inputform.listFields[indx].selected = true;
	}
	for (var indx = 0; indx < document.inputform.sumFields.length; indx++) {
		document.inputform.sumFields[indx].selected = true;
	}
	if (!validateReport(val)) return false;

	if (val == 'text') document.inputform.method.value = 'getText';
	else if(val == 'chart') document.inputform.method.value = 'getChart';
	else document.inputform.method.value = 'getReport';

	var filterFields = YAHOO.util.Dom.getElementsByClassName("filterfields");
	for (var i=0; i<filterFields.length; i++) {
		var filterVal = document.getElementById('filterVal.'+(i+1));
		var fieldName = filterFields[i].value;
		if (filterTyps[fieldName] == 'timestamp') {
			if (!validateFilterTimeStampFormat(filterVal)) {
				filterVal.focus();
				return false;
			}
		} else if (filterTyps[fieldName] == 'date') {
			if ( document.getElementById('filterOp.'+(i+1)) && document.getElementById('filterOp.'+(i+1)).value == 'between'){
				//in this filter option filterVal is name not an id of element
				var filterValObjName = 'filterVal.'+(i+1);
				filterVal = getElementByName(getThisRow(document.getElementById('filterOp.'+(i+1)).value), filterValObjName);
			}
			if (!validateFilterDateFormat(filterVal)) {
				filterVal.focus();
				return false;
			}
		}
	}

	return true;
}

function checkBoxsLength(obj) {
	return obj.length;
}

function checkedCheckBoxsLength(obj) {
	var len = obj.length;
	return len;
}

function checkedCheckBoxsWidth(obj, name, obj1) {
	var len = obj.length;
	var width = 0;
	var temp;
	var tempName;
	for (var indx = 0; indx < obj.length; indx++) {
		temp = obj[indx].value;
		if (temp != '_data' && temp != '_count') {
			if (obj1[temp] && obj1[temp] != null) {
				width += parseInt(obj1[temp]);
			}
		}
	}
	return width;
}

/*******************************************************************************************
 *	To select the list of fields to be displayed by default in the report.
 *******************************************************************************************/

function initSelectedFields() {
	var lstFlds = document.inputform.listFields;
	var avlbFlds = document.inputform.avlbListFlds;
	for (var indx = 0; indx < defaultShowFields.length; indx++) {
		moveSelectedOption(avlbFlds, lstFlds, defaultShowFields[indx]);
	}
	sortAllReportSelectFields();
}

// Sorts all grouping and sort select fields by their name.

function sortAllReportSelectFields(){
	// sort List Group fields
	if(document.getElementById('listGroups1')!= null)
		sortSelect(document.getElementById('listGroups1'));
	if(document.getElementById('listGroups2')!= null)
		sortSelect(document.getElementById('listGroups2'));
	if(document.getElementById('listGroups3')!= null)
		sortSelect(document.getElementById('listGroups3'));

	//sort Summary Group fields
	if(document.getElementById('sumGroupHoriz')!= null)
		sortSelect(document.getElementById('sumGroupHoriz'));
	if(document.getElementById('sumGroupVert')!= null)
		sortSelect(document.getElementById('sumGroupVert'));
	if(document.getElementById('sumGroupVertSub')!= null)
		sortSelect(document.getElementById('sumGroupVertSub'));

	//sort Trend Group fields
	if(document.getElementById('trendType')!= null)
		sortSelect(document.getElementById('trendType'));
	if(document.getElementById('trendGroupVert')!= null)
		sortSelect(document.getElementById('trendGroupVert'));
	if(document.getElementById('trendGroupVertSub')!= null)
		sortSelect(document.getElementById('trendGroupVertSub'));

	//sort Vertical Trend fields
	if(document.getElementById('vtrendGroupHoriz')!= null)
		sortSelect(document.getElementById('vtrendGroupHoriz'));
	if(document.getElementById('vtrendType')!= null)
		sortSelect(document.getElementById('vtrendType'));
	if(document.getElementById('vtrendGroupVertSub')!= null)
		sortSelect(document.getElementById('vtrendGroupVertSub'));

	//sort Custom Sort Select fields
	if(document.getElementById('customOrder1')!= null)
		sortSelectForOrder(document.getElementById('customOrder1'));
	if(document.getElementById('customOrder2')!= null)
		sortSelectForOrder(document.getElementById('customOrder2'));
}


/*******************************************************************************************
 * function to handle favourite report browsing.
 *******************************************************************************************/

function onReportChange(value, form) {
	if (value == 'nosearch') {
		return false;
	}
	form.target = "_parent";
	form.action = cpath + "/FavouriteReportAction.do";
	form.method.value = 'getMyFavourite';
	form.submit();
	return true;
}

function displaySaveInputs() {
	var obj = document.getElementById('_save_inputs');
	if (obj.style.display == 'none' || !obj.style.display) obj.style.display = 'block';
	else obj.style.display = 'none';
}

/*******************************************************************************************
 * function to handle the saving of reports as favourites.
 *******************************************************************************************/
function saveReport(form) {
	setPdfCustomVals();
	if(document.inputform._sel)
		setDateToken();
	var mysearches = form._myreport;
	var report_name = form._report_name.value;
	if (empty(report_name)) {
		alert("Please enter a Report Name.");
		document.getElementById('_report_name').focus();
		return false;
	}
	if(!checkIfDupFavRepName()){
		return false;
	}
	if(document.inputform._sel!= null) {
		document.inputform.selDateRange.value = document.getElementById("_sel").value;
	}
	for (var indx = 0; indx < document.inputform.listFields.length; indx++) {
		document.inputform.listFields[indx].selected = true;
	}
	for (var indx = 0; indx < document.inputform.sumFields.length; indx++) {
		document.inputform.sumFields[indx].selected = true;
	}
	if (!validateReport('pdf')) return false;
	form.target = "_parent";
	form.method.value = 'saveFavourite';
	form.submit();
}

function validateFavRepTextField(e, field) {
	var key = 0;
	if (window.event) {
		key = e.keyCode;
	} else {
		key = e.which;
	}
	if ((key >= 65) && (key <= 90) || (key >= 97) && (key <= 122) || (key >= 48) && (key <= 57) || key == 8 || key == 9 || key == 32 || key == 38 || key == 40 || key == 41 || key == 44 || key == 46 || key == 95 || key == 0) return true;
	else return false;
}

/*******************************************************************************************
 * function to handle the display of favourite report Active image indicator.
 *******************************************************************************************/

function showFiltActive(favreport) {
	var dobj = document.getElementById('_filters_active');
	if (favreport == null || favreport == '') {
		dobj.style.display = 'none';
	} else {
		dobj.style.display = 'block';
	}
	editReportHandler();
	return true;
}


/*******************************************************************************************
 * functions to handle pdf footer customizations.
 *******************************************************************************************/
var myDialog;
function initDialog() {
    myDialog = new YAHOO.widget.Dialog('myDialog', {
        width:"300px",
        visible: false,
        modal: true,
        constraintoviewport: true,
        buttons: [{
            text: "OK",
            handler: handleOk,
            isDefault:true
           }]
    });
    myDialog.setHeader("<font style='font:Arial,FreeSans,sans-serif;color: #666666;font-weight: bold;' title='Customization Options'>Customization Options</font>");
    myDialog.render();
}

function handleCancel() {
    this.cancel();
}

function handleOk() {
	document.getElementById('myDialog').style.display='none';
    myDialog.cancel();
    document.getElementById('button1').focus();
}

function showDialog() {
	document.getElementById('myDialog').style.display='block';
	var button1 = document.getElementById("button1");
    myDialog.cfg.setProperty("context", [button1, "bl", "bl"], false);
    myDialog.show();
}


function setPdfCustomVals(){

	if(document.inputform.pdfcstm_option[0].checked){
		document.getElementById("userNameNeeded").value= "Y";
	}else{
		document.getElementById("userNameNeeded").value= "N";
	}


	if(document.inputform.pdfcstm_option[1].checked){
		document.getElementById("dt_needed").value= true;
	}else{
		document.getElementById("dt_needed").value= false;
	}


	if(document.inputform.pdfcstm_option[2].checked){
		document.getElementById("hsp_needed").value= true;
	}else{
		document.getElementById("hsp_needed").value= false;
	}
	if(document.inputform.pdfcstm_option[3].checked){
		document.getElementById("hsp_needed_h").value= true;
	}else{
		document.getElementById("hsp_needed_h").value= false;
	}

	if(document.inputform.pdfcstm_option[4].checked){
		document.getElementById("pgn_needed").value= true;
	}else{
		document.getElementById("pgn_needed").value= false;
	}

	if(document.inputform.pdfcstm_option[5].checked){
		document.getElementById("grpn_needed").value= true;
	}else{
		document.getElementById("grpn_needed").value= false;
	}

	if(document.inputform.pdfcstm_option[6].checked){
		document.getElementById("skip_repeated_values").value= true;
	}else{
		document.getElementById("skip_repeated_values").value= false;
	}

	if(document.inputform.pdfcstm_option[7].checked){
		document.getElementById("filterDesc_needed").value= true;
	}else{
		document.getElementById("filterDesc_needed").value= false;
	}
}


function checkIfDupFavRepName(){
var favrepArr = favRepTitles;
var rep_name = inputform._report_name.value;
	for (var i=0;i<favrepArr.length; i++) {
		var ele = favrepArr[i];
		if((ele.report_title).trim().toUpperCase() == (rep_name).trim().toUpperCase() ) {
			if(ele.user_name == document.getElementById("current_user").value){
				var flag = confirm("This Report already exists... \nOverwrite Report?");
				if(flag) return true;
				else return false;
			}else {
				alert("This Report Name already exists... \n Please try another...");
				return false;
			}
		}
	}
	return true;
}

function validateFilterDateFormat(datefld) {
	if ( datefld == null )return;
	var dateStr = datefld.value;
	if(dateStr=='') return;
	var myarray = dateStr.split("-");

	if (myarray.length != 3) {
		alert( "Incorrect date format: please use DD-MM-YYYY");
		datefld.focus();
		return false;
	}

	var dt = myarray[0];
	var mth = myarray[1];
	var yr = myarray[2];

	if (!isInteger(parseInt(dt))  || isNaN(parseInt(dt))){
		alert("Incorrect date format: day is not a number");
		datefld.focus();
		return false;
	}
	if (!isInteger(parseInt(mth)) || isNaN(parseInt(mth))) {
		alert( "Incorrect date format: month is not a number");
		datefld.focus();
		return false;
	}
	if (!isInteger(parseInt(yr)) || isNaN(parseInt(yr))) {
		alert( "Incorrect date format: year is not a number");
		datefld.focus();
		return false;
	}

    if (parseInt(parseInt(dt)) > 31) {
        alert( "Invalid date: please enter 1-31 for day");
        datefld.focus();
        return false;
    }

    if (parseInt(parseInt(mth)) > 12) {
        alert( "Invalid date: please enter 1-12 for month");
        datefld.focus();
        return false;
    }

    if ( (yr.length != 2) && (yr.length != 4) ) {
		alert("Invalid date: year must be a 2-digit or 4-digit year");
		datefld.focus();
		return false;
	}

	return true;
}


function validateDateForBetwFrom(field, index){
	var from= document.getElementById("fromVal."+index);
	validateFilterDateFormat(from);
}

function validateDateForBetw(field, index){
	var from= document.getElementById("fromVal."+index);
	var to= document.getElementById("toVal."+index);

	var fromstr = from.value;
	var tostr = to.value;

	if(fromstr=='' || fromstr==null){
	 	alert("Please enter the From-Date");
		from.focus();
		return false;
	}else if(tostr==''|| tostr==null){
		alert("Please enter the To-Date");
	  	to.focus();
	  	return false;
	}else {
		if(validateFilterDateFormat(from)){
			if(validateFilterDateFormat(to)){
			 	return true;
			}else{
				return false;
			}
		}
	}
}

function validateFilterTimeStampFormat(field){
        if(field.value == '')
                return true;
	var tbValues = (field.value).split(",");
	var frmtdValues = new Array();
	var dateAndTimeRegex =new RegExp(
		    "^\\s*" +                    // ignore whitespace
		    "(" +                        // start of date
		        "([012]?[0-9]|3[01])" +  // day: 0 through 9, or 00 through 29, or 30, or 31
		        "\\-"   +                // delimiter between year and month; typically will be "-"
		        "([0]?[0-9]|1[012])" +   // month: 0 through 9, or 00 through 09, or 10 through 12
		        "\\-"     +              // delimiter between month and day; typically will be "-"
	            "[0-9]{1,4}"  +          // year: 2010, 2011, ...,
		    ")?"      +                  // end of optional date
		    "\\s?" +                     // optional whitespace
		    "("+                         // start of time
		        "([01]?[0-9]|2[0-3])" +  // hour: 0 through 9, or 00 through 19, or 20 through 23
		        "\\:"   +                // delimiter between hours and minutes; typically will be ":"
		        "([0-5][0-9])"  +        // minute: 00 through 59
		        "("       +              // start of seconds (optional)
		            "\\:"  +             // delimiter between minutes and seconds; typically will be ":"
		            "([0-5][0-9])" +     // seconds: 00 through 59
		        ")?"    +                // end of optional seconds
		    ")?"   +                     // end of optional time
		    "\\s*$",'i');
	var newTbValues = new Array();
	var j=0;
	for (var i = 0; i < tbValues.length; i++) {
		tbValues[i] =  (tbValues[i].trim()) ;
		if((matchz = dateAndTimeRegex.exec(tbValues[i])) != null  ) {
			newTbValues[j++] = matchz[0];
		}
	}
	if(newTbValues.length<1 && tbValues.length>0 || (newTbValues.length<tbValues.length)){
		alert("Incorrect date format: please use DD-MM-YY[YY] [hh:mm:ss]");
	 	field.focus();
	 	return false;
	}else {
			for(var ind=0;ind<newTbValues.length;ind++){
				var timestamp=newTbValues[ind];
					var split = timestamp.split(/[^\d]+/);
				    var day = parseInt((split[0]));
				    var month = parseInt((split[1]));
				    var year = (split[2]);
				    var hour = parseInt((split[3]));
				    var minute = parseInt((split[4]));
				    var second = parseInt((split[5]));
			if (!isInteger(parseInt(day))  || isNaN(parseInt(day))){
				alert("Incorrect date format: day is not a number");
				field.focus();
				return false;
			}
			if (!isInteger(parseInt(month)) || isNaN(parseInt(month))) {
				alert( "Incorrect date format: month is not a number");
				field.focus();
				return false;
			}
			if (!isInteger(parseInt(year)) || isNaN(parseInt(year))) {
				alert( "Incorrect date format: year is not a number");
				field.focus();
				return false;
			}
		    if (parseInt(parseInt(day)) > 31) {
	        	alert( "Invalid date: please enter 1-31 for day");
	        	field.focus();
	        	return false;
	   		 }
			if (parseInt(parseInt(month)) > 12) {
	        	alert( "Invalid date: please enter 1-12 for month");
	        	field.focus();
	        	return false;
	   	     }
	   	    if ( ((year.toString()).length != 2) && ((year.toString()).length != 4) ) {
				alert("Invalid date: year must be a 2-digit or 4-digit year");
				field.focus();
				return false;
			}
			if((month == 2) && !((parseInt(year) % 4 == 0) || (parseInt(year) % 100 == 0) || (parseInt(year) % 400 == 0)) && day>28){
				alert("Invalid date: Cannot have more than 28 days for february.");
				field.focus();
				return false;
			}
   			if ((day > 30) && ((month == "04") || (month == "06") || (month == "09") || (month == "11"))) {
      			alert("Invalid date: Cannot have more than 30 days.");
				field.focus();
				return false;
  			}
			if(!isNaN(hour) && hour>23){
				alert("Invalid hour: Please enter 00-23 for hour");
				field.focus();
				return false;
			}
			if(!isNaN(minute) && hour>59){
				alert("Invalid minute: Please enter 00-59 for minute");
				field.focus();
				return false;
			}
			if(!isNaN(second) && second>59){
				alert("Invalid second: Please enter 00-59 for second");
				field.focus();
				return false;
			}
			 var fday = day==0?"01":day<10?"0"+day:day;
			 var fmonth = month==0?"01":month<10?"0"+month:month;
			 var fyear = parseInt(year)==0?"2000":(year.toString()).length==4?year:parseInt(year)<100?parseInt(year)<10?"200"+parseInt(year):"20"+parseInt(year):year;
			 var fhour = isNaN(hour)? "00":hour<10?"0"+hour:hour;
			 var fminute = isNaN(minute)? "00":minute<10?"0"+minute:minute;
			 var fsecond = isNaN(second)? "00":second<10?"0"+second:second;
			 frmtdValues[ind] = fday+"-"+fmonth+"-"+fyear+" "+fhour+":"+fminute+":"+fsecond;
		}

		field.value= frmtdValues;
	}
	return true;
}

function validateFilterTimeStampFormatForBetw(field, index){
	var from= document.getElementById("fromVal."+index);
	var to= document.getElementById("toVal."+index);

	var fromstr = from.value;
	var tostr = to.value;
	if(fromstr=='' || fromstr==null){
	 	alert("Please enter the From-Date");
		from.focus();
		return false;
	}else if(tostr==''|| tostr==null){
			alert("Please enter the To-Date");
	  		to.focus();
	  		return false;
	}else {
		if(validateFilterTimeStampFormat(from)){
			if(validateFilterTimeStampFormat(to)){
			 	return true;
			}else{
				return false;
			}
		}
	}
}


//function extended to include ':', '/' and '\'
function enterAlphaNumericalsExt(e) {
	var c = getEventChar(e);
	return  true;
}
