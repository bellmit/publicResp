
function onInit() {
	setSelDateRange();
	onChangeReportType();
	onChangeFilterBy();
	onChangeExclude();
}

function onSubmit(option) {
	document.inputform.format.value = option;
	var sel = document.inputform.groupBy;
	if ((sel.disabled == false) && (sel.value == "")) {
		alert("Please select a Group By option");
		return false;
	}
	if ( (document.inputform.filterBy.value != "") && (document.inputform.filterValue.value == "*") ) {
		alert("Please select a Filter value");
		return false;
	}
	if ( (document.inputform.exclude.value != "") && (document.inputform.excludeValue.value == "*") ) {
		alert("Please select an Exclude value");
		return false;
	}
	document.inputform.groupByName.value = sel.options[sel.selectedIndex].text;
	if (option == 'pdf')
		document.inputform.target = "_blank";
	else
		document.inputform.target = "";
	return validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
}

function onChangeFilterBy() {
	var sel = document.inputform.filterValue;
	var filterBy = document.inputform.filterBy.value;
	if (filterBy != "") {
		if(filterBy == 'bill_type'){
			loadSelectBox(sel, gGroupList[filterBy].list, gGroupList[filterBy].column,
					gGroupList[filterBy].column2, "Select", "*");
		}else {
			loadSelectBox(sel, gGroupList[filterBy].list, gGroupList[filterBy].column,
					gGroupList[filterBy].column, "Select", "*");
			if (gGroupList[filterBy].addNull) {
				insertIntoSelectBox(sel, 1, "(None)", "");
			}
		}
	} else {
		loadSelectBox(sel, null, null, null, "Select", "*");
	}
	sel.selectedIndex = 0;
}

function onChangeExclude() {
	var sel = document.inputform.excludeValue;
	var exclude = document.inputform.exclude.value;
	if (exclude != "") {
		if(exclude == 'bill_type'){
			loadSelectBox(sel, gGroupList[exclude].list, gGroupList[exclude].column,
					gGroupList[exclude].column2, "Select", "*");
		}else {
			loadSelectBox(sel, gGroupList[exclude].list, gGroupList[exclude].column,
					gGroupList[exclude].column, "Select", "*");
			if (gGroupList[exclude].addNull) {
				insertIntoSelectBox(sel, 1, "(None)", "");
			}
		}
	} else {
		loadSelectBox(sel, null, null, null, "Select", "");
	}
	sel.selectedIndex = 0;
}

function onChangeReportType() {
	var type = getRadioSelection(document.inputform.reportType);
	// enable/disable trend type dropdown
	if (type == 'trend') {
		document.inputform.trendPeriod.disabled = false;
	} else {
		document.inputform.trendPeriod.disabled = true;
	}
	// enable/disable View button
	if ( (type == 'billWise') || (type == 'csv') || (type == 'itemWise') ) {
		document.getElementById('viewButton').disabled = true;
	} else {
		document.getElementById('viewButton').disabled = false;
	}
	// enable/disable Group By dropdown
	if (type == 'csv') {
		document.inputform.groupBy.disabled = true;
	} else {
		document.inputform.groupBy.disabled = false;
	}
}

