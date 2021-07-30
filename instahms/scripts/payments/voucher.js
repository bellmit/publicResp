var form = document.paymentVoucherForm;

function init(){
	form = document.paymentVoucherForm;
}

function taxAmount(){
	form = document.paymentVoucherForm;
	var amtPaise = getElementIdPaise("totalAmount");
	var staxPer = form.serviceTax.value;
	var tdsPer = form.tds.value;
	if (staxPer == ""){
		staxPre=0.0;
		form.serviceTax.value=0.0;
	}
	if( tdsPer == ""){
		tdsPer =0.0;
		form.tds.value =0.0;
	}

	var roundOff = form.roundoff.checked;
	var roundOffPaise = 0;
	var staxAmount = staxPer * amtPaise / 100;  // (parseFloat(amt) * parseFloat(stax))/100;
	var tdsAmount = tdsPer * amtPaise / 100;  //(parseFloat(amt) * parseFloat(tds))/100;

	var netAmount = amtPaise + staxAmount - tdsAmount;
	if (roundOff) {
		var grandTotal = amtPaise + staxAmount - tdsAmount;
		roundOffPaise = getRoundOffPaise(grandTotal);
		form.roundOffAmt.value = formatAmountPaise(roundOffPaise);
	} else {
		form.roundOffAmt.value ="";
	}

	form.netPayment.value=formatAmountPaise(amtPaise+staxAmount-tdsAmount+roundOffPaise);
}

function onChangeRoundOff(){
	taxAmount();
}

function formValidation(){
	var counter = form.counter.value;
	if (counter ==""){
		alert("You are not authorized to issue Payments");
		return false;
	}

	if (!validatePayment()) return false;

    if (document.getElementById("payDate").value==""){
	   alert("Enter Date");
	   document.getElementById("payDate").focus();
	   return false;
   }
	document.forms[0].printType.value = document.forms[0].voucherPrint.value;
	form.submit();
}

function validateNum(obj) {
	var objValue = obj.value;
	var fractionExists = false;

	var numericVal = objValue;
	for ( var i = 0; i < objValue.length; i++ ) {
		if ( objValue.charAt(i) == "." ) {
			var splittedValue = objValue.split(".");
			numericVal = splittedValue[0];
			var fractionalVal = splittedValue[1];
			fractionExists = true;
			break;
		}
	}

	if (fractionExists) {
		if ( fractionalVal.length > 2 ) {
			obj.value =  parseFloat(obj.value).toFixed(2);
		}
	}
	if ( (numericVal.length >2) && (numericVal>100) ) {
		alert("Numeric field should not exceed 3 digits and less than or equal to 100");
		obj.value="0.00"
			obj.focus();
		return false;
	}
	return true;
}

