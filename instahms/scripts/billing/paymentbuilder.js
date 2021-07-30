function onInit() {
	document.getElementById('pd').checked = true;
	setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
	document.inputform.groupBy.selectedIndex = 0;
	onChangeReportType();
	onChangeFilterBy();

}

function onSubmit(option) {
	var groupby = document.inputform.groupBy.value;
	var filterBy = document.inputform.filterBy.value;
	var filterValue = document.inputform.filterValue.value;
	var type = getRadioSelection(document.inputform.reportType);
	var filterText = document.inputform.filterValue.options[document.inputform.filterValue.selectedIndex].text;
	var fltvalue = document.inputform.filterValue.options[document.inputform.filterValue.selectedIndex].value;
	if (fltvalue == "*"){
		document.getElementById("docName").value = '';
	}else {
		document.getElementById("docName").value = filterText;
	}

	if (groupby == ''){
		alert("Select group by value");
		return false;
	}

	if (groupby != ''){
		if (filterBy != '' && filterValue ==''){
			alert("Select filter by value");
			return false;
		}
		if(filterBy == '' && filterValue == ''){
			alert("Select filter by value");
			return false;
		}
	}
	if (type == 'summary'){
		if (!(document.getElementById("insurance").checked) && !(document.getElementById("nonInsurance").checked)){
			alert("Select insurance type");
			return false;
		}
	}
	document.inputform.format.value = option;
	if (option == 'pdf') {
		document.inputform.target = "_blank";
	}else if (option == 'csv'){
		document.getElementById("format").value = "csv";
	}
	return validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
}

function onChangeReportType() {
	var type = getRadioSelection(document.inputform.reportType);
	var groupBy = document.inputform.groupBy.value;
	if ( (type == 'summary') || (type == 'csv') || (type == 'itemWise') ) {
		document.getElementById('viewButton').disabled = true;
		document.getElementById('exportButton').disabled = false;
		if (groupBy == 'All' || groupBy == 'doctor_name'){
			document.getElementById("insurance").disabled = false;
			document.getElementById("nonInsurance").disabled = false;
		}else{
			document.getElementById("insurance").disabled = true;
			document.getElementById("nonInsurance").disabled = true;
		}
	} else {
		document.getElementById('viewButton').disabled = false;
		document.getElementById('exportButton').disabled = true;
		document.getElementById("insurance").disabled = true;
		document.getElementById("nonInsurance").disabled = true;
	}
}

function onChangeFilterBy(){
	var sel = document.inputform.filterValue;
	var filterBy = document.inputform.filterBy.value;

	if (filterBy == 'doctor_id'){
		loadSelectBox(sel, jDocList,"doctor_name","doctor_id", "(All)", "*");
	}else if (filterBy == 'reference_docto_id'){
		loadSelectBox(sel, jRefDocList,"doctor_name","doctor_id", "(All)", "*");
	}else if (filterBy == 'prescribing_dr_id'){
		loadSelectBox(sel, jPresDocList,"doctor_name","prescribing_dr_id", "(All)", "*");
	}else{
		loadSelectBox(sel,jDocList,"doctor_name","doctor_id", "Select","");
	}
	sel.selectedIndex = 0;
}

function setInsuranceFilter(){
	if (document.getElementById("insurance").checked){
		//document.getElementById("").value = document.getElementById("insurance").value;
	}else if (document.getElementById("nonInsurance").checked){
		//document.getElementById("").value = document.getElementById("nonInsurance").value;
	}else if ((document.getElementById("insurance").checked) && (document.getElementById("nonInsurance").checked)){
	}
}

