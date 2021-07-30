function init(){
	 setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
}

var payeeType = null;
function getPayeeType(){
	var docPayee = document.getElementById("docPayee");
	var refPayee = document.getElementById("refPayee");
	var presPayee = document.getElementById("presPayee");
	if (docPayee.checked && refPayee.checked  && presPayee.checked){
		payeeType = "all";
	}else if (docPayee.checked || refPayee.checked || presPayee.checked ){
		payeeType = "";
		if (docPayee.checked){
			if (payeeType !="")payeeType = payeeType+",";
				payeeType = payeeType + docPayee.value;
		}
		if (refPayee.checked){
			if (payeeType !="") payeeType = payeeType + ",";
			payeeType = payeeType + refPayee.value;
		}

		if (presPayee.checked){
			if (payeeType !="")payeeType = payeeType + ",";
			payeeType = payeeType + presPayee.value;
		}

	}else {
		alert("Select Payee Type");
		return false;
	}
		document.getElementById("payeeTypeValues").value = payeeType;
		return true;
}

function onSubmit(option) {
	document.inputform.format.value = option;
	if (option == 'pdf') {

		if ( document.forms[0].printerType.value == 'text' )
			document.inputform.method.value = 'getText';
		else
			document.inputform.method.value = 'getPaymentToInsuranceReport';

		document.inputform.target = "_blank";
	}else {
		document.inputform.target = "";
	}
	return validateFromToDate(fromDate, toDate);
}