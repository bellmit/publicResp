function init(){
	setSelDateRange();
}



function onChangeFilterBy(){
	var sel = document.inputform.filterValue;
	var filterBy = document.inputform.filterBy.value;

	if (filterBy !=""){
		loadSelectBox(sel, gGroupList[filterBy].list, gGroupList[filterBy].column,
				gGroupList[filterBy].column, "Select", "*");
		if (gGroupList[filterBy].addNull){
			insertIntoSelectBox(sel, 1, "(None)", "");
		}
	}else {
		loadSelectBox(sel, null, null, null, "Select", "*");
	}
	sel.selectedIndex = 0;
}

function onSubmit(option){
	var sel = document.inputform.groupBy;
	 document.inputform.format.value = option;
	if (sel.value == "" ) {
		alert("Select group by option");
		return false;
	}

	if ((document.inputform.filterBy.value !="")  &&  (document.inputform.filterValue.value =="*")){
		alert("Select Filter value");
		return false;
	}

	if (option == 'pdf') {
		if ( document.inputform.printerType.value == 'text' )
			document.inputform.method.value = 'getText';
		else
			document.inputform.method.value = 'paidPaymentsReport';

		document.inputform.target = "_blank";
	}
	else
		document.inputform.target = "";
	return validateFromToDate(fromDate, toDate);


}
