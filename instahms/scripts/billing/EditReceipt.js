function init() {
	enableBankDetails(this);
	getCurrencyDetails();
	enableCommissionDetails(this);
	// if the select mode is not loyalty card or oneApollo  then dont show loyalty card in
	// payment mode dropdown so the mode cant be edited to loyalty card
	if ($("#payment_mode_id").val() != -3)
		$(".dropdown option[value='-3']").remove();
	if ($("#payment_mode_id").val() != -5) //one_apollo_loyalty_card
		$(".dropdown option[value='-5']").remove();

	if ($("#payment_mode_id").val() == -5 || $("#payment_mode_id").val() == -3){
		$("#reference_no").attr("readOnly",true);
		$("#amount").attr("readOnly",true);
		$('option:not(:selected)').prop('disabled', true)
	}
}

function enableBankDetails(modeObj) {

	var modeObj = document.getElementById("payment_mode_id");
	var bankObj = document.getElementById("bank_name");
	var cardObj = document.getElementById("card_type_id");
	var refObj = document.getElementById("reference_no");

	var bankBatchObj = document.getElementById("bank_batch_no");
	var cardAuthObj = document.getElementById("card_auth_code");
	var cardHolderObj = document.getElementById("card_holder_name");

	var cardNumberObj = document.getElementById("card_number");
	var cardExpDtObj = document.getElementById("card_expdate");
	var mode = modeObj.value;

	var currencyObj = document.getElementById("currency_id");

	if (currencyObj != null && currencyObj.value != "") {
		getCurrencyDetails();
	}

	var paymentModeDetail = findInList(jPaymentModes, "mode_id", mode);

	if (!empty(paymentModeDetail)) {
		cardObj.disabled = (paymentModeDetail.card_type_required != 'Y');
		bankObj.disabled = (paymentModeDetail.bank_required != 'Y');
		refObj.disabled = (paymentModeDetail.ref_required != 'Y');

		bankBatchObj.disabled = (paymentModeDetail.bank_batch_required != 'Y');
		cardAuthObj.disabled = (paymentModeDetail.card_auth_required != 'Y');
		cardHolderObj.disabled = (paymentModeDetail.card_holder_required != 'Y');

		cardNumberObj.disabled = (paymentModeDetail.card_number_required != 'Y');
		cardExpDtObj.disabled = (paymentModeDetail.card_expdate_required != 'Y');
	}
	if(cardObj.disabled) setSelectedIndex(cardObj, "");
	if (bankObj.disabled) bankObj.value = "";
	if (refObj.disabled)  refObj.value = "";

	if (bankBatchObj.disabled) bankBatchObj.value = "";
	if (cardAuthObj.disabled)  cardAuthObj.value = "";
	if (cardHolderObj.disabled)  cardHolderObj.value = "";

	if (cardNumberObj.disabled)  cardNumberObj.value = "";
	if (cardExpDtObj.disabled)  cardExpDtObj.value = "";
}

function refreshCommissionDetails(Obj) {
    if (!empty(Obj)) {
		 document.getElementById('commissionPer_div').style.display = "none";
		 document.getElementById('commissionAmt_div').style.display = "none";
		 document.getElementById('commissionPer').innerHTML = "";
		 document.getElementById('commissionAmt').innerHTML = "";
	}

}

function getNumOfPayments() {
	var paymentTable = document.getElementById("receiptTable");
	if (paymentTable) {
		var numPayments = (paymentTable.rows.length - paymentRowsUncloned) /paymentRows;
		return numPayments;
	}
	return 0;
}

function enableCommissionDetails(obj) {
	var cardmodeObj = document.getElementById("payment_mode_id").value;
	var numPayments = getNumOfPayments();
	var creditType = document.getElementById("card_type_id").value;
	jgetAllCreditTypes = eval(jgetAllCreditTypes);
	var totalAmt = receiptForm.amount.value;
	if(jgetAllCreditTypes != undefined && creditType != '') {
		var creditTypeObj = findInList(jgetAllCreditTypes, "card_type_id", creditType);
		if (!empty(creditTypeObj)) {
			if (!empty(creditTypeObj.commission_percentage)) {
				var commissionPer = (creditTypeObj.commission_percentage).toFixed(2);
				var commissionAmt = (totalAmt*commissionPer/100).toFixed(decDigits);
					for(var i=0;i<numPayments;i++) {
						var commissionPerObj = getIndexedFormElement(receiptForm, "commissionPercent", i);
						var commissionAmtObj = getIndexedFormElement(receiptForm, "commissionAmount", i);
						commissionPerObj.value = commissionPer;
						commissionAmtObj.value = commissionAmt;
					}
				} else {
					document.getElementById('commissionPer_div').style.display = "none";
					document.getElementById('commissionAmt_div').style.display = "none";
					document.getElementById('commissionPer').innerHTML = '';
					document.getElementById('commissionAmt').innerHTML =  '';
				}
			}
		}

		if (commissionPer > 0 &&  creditType != '') {
			document.getElementById('commissionPer_div').style.display = "block";
			document.getElementById('commissionPer').innerHTML =  commissionPer;
			document.getElementById('commissionAmt_div').style.display = "block";
			document.getElementById('commissionAmt').innerHTML =  commissionAmt;
		} else {
			document.getElementById('commissionPer_div').style.display = "none";
			document.getElementById('commissionAmt_div').style.display = "none";
			document.getElementById('commissionPer').innerHTML = '';
			document.getElementById('commissionAmt').innerHTML =  '';

			document.getElementById("commissionPercent").value = '';
			document.getElementById("commissionAmount").value = '';
		}
}

function getCurrencyDetails() {

	var currObj = document.getElementById("currency_id");
	var currVal = (!empty(currObj)) ? currObj.value : '';
	var currAmtObj = document.getElementById("currency_amt");
	var exchRateObj = document.getElementById("exchange_rate");
	var exchDtObj = document.getElementById("exchange_date");
	var exchTimeObj = document.getElementById("exchange_time");
	var payObj = null;
	if(roleId == 1 || roleId == 2 || editReceiptAmounts == 'A') {
		payObj = document.getElementById("amount");
	}

	if (roleId == 1 || roleId == 2 || editReceiptAmounts == 'A') {
		if ((null != currObj) && !currObj.disabled && currVal != '') {
			var currency = findInList(jForeignCurrencyList, "currency_id", currVal);
			if (!empty(currency)) {
				exchRateObj.value = currency.conversion_rate;
				exchDtObj.value = formatDate(new Date(currency.mod_time),'ddmmyyyy','-');
				exchTimeObj.value = formatTime(new Date(currency.mod_time));
			}else {
				currAmtObj.value = '';
				exchRateObj.value = '';
				exchDtObj.value = '';
				exchTimeObj.value = '';
			}

			if ((null != currAmtObj) && !currAmtObj.disabled && (currAmtObj.value == ""))
				if ((null != payObj)) payObj.value = '';

		}else {
			if (currAmtObj != null) currAmtObj.value = '';
			if (exchRateObj != null) exchRateObj.value = '';
			if (exchDtObj != null) exchDtObj.value = '';
			if (exchTimeObj != null) exchTimeObj.value = '';
		}

		if (currAmtObj != null) currAmtObj.disabled = (currVal == '');
		if (exchRateObj != null) exchRateObj.disabled = (currVal == '');
		if (exchDtObj != null) exchDtObj.disabled = (currVal == '');
		if (exchTimeObj != null) exchTimeObj.disabled = (currVal == '');

		convertCurrency(currAmtObj);
	}
}

function convertCurrency() {

	var currAmtObj = document.getElementById("currency_amt");

	var currAmtVal = (!empty(currAmtObj)) ? currAmtObj.value  : '';
	var currObj = document.getElementById("currency_id");
	var currVal = (!empty(currObj)) ? currObj.value : '';

	var exchRateObj = document.getElementById("exchange_rate");

	if(roleId == 1 || roleId == 2 || editReceiptAmounts == 'A')	{
		var payObj = document.getElementById("amount");
		if (trim(currAmtVal) != '' && currObj != '') {
			var currency = findInList(jForeignCurrencyList, "currency_id", currVal);
			if (!empty(currency)) {
				payObj.readOnly = true;
				payObj.value = formatAmountValue(currAmtVal * currency.conversion_rate);
			}else {
				payObj.readOnly = false;
				//payObj.value = '';
			}
		}
	}
}

function doSave() {
	if (empty(document.receiptForm.receiptNo.value)) {
		showMessage("js.billing.editreceipt.noreceiptdetails.save");
		document.receiptNoForm.receiptNo.focus();
		return false;
	}
	if (max_centers_inc_default>1 && centerId == 0) {
		showMessage("js.billing.editreceipt.allowed.centers");
		return false;
	}
	if(income_tax_cash_limit_applicability == 'Y') {
		var valid = true;
		if (payType != 'DF' && payType != 'DR'){
			var mrno=(visitType != 'r' &&  visitType != 't' ? mr_no : null)
			valid = valid && checkCashLimitValidation(mrno,visitId);
		}
		else if (payType == 'DF' || payType == 'DR'){
			valid = valid && checkDepositCashLimitValidation(mrno);
		}
		if (!valid) return valid;
	}
	if (!validatePaymentFields()) return false;
	document.receiptForm.submit();
}

function doValidateDateTime(dateinput, timeinput, checkType) {
	if (validateDateTime(getDateTime(dateinput.value, timeinput.value), checkType)) {
		alert(validateDateTime(getDateTime(dateinput.value, timeinput.value), checkType));
		dateinput.focus();
		return false;
	}
	return true;
}

function validatePaymentFields() {
	var modeObj = document.getElementById("payment_mode_id");
	var bankObj = document.getElementById("bank_name");
	var cardObj = document.getElementById("card_type_id");
	var refObj = document.getElementById("reference_no");

	var bankBatchObj = document.getElementById("bank_batch_no");
	var cardAuthObj = document.getElementById("card_auth_code");
	var cardHolderObj = document.getElementById("card_holder_name");

	var cardNumberObj = document.getElementById("card_number");
	var cardExpDtObj = document.getElementById("card_expdate");

	var exchDtObj = document.getElementById("exchange_date");
	var exchTimeObj = document.getElementById("exchange_time");
	var exchRateObj = document.getElementById("exchange_rate");

	var currIdObj = document.getElementById("currency_id");
	var amtObj = document.getElementById("amount");
	var currAmtObj = document.getElementById("currency_amt");
	var tdsObj = document.getElementById("tds_amt");

	var dispDtObj = document.getElementById("display_date");
	var dispTimeObj = document.getElementById("display_time");

	var paymentObj = document.getElementById("paymentType");

	var type = paymentObj.value;

	if (!paymentObj.disabled && paymentObj.value == "") {
		showMessage("js.billing.editreceipt.paymenttype.required");
		paymentObj.focus();
		return false;
	}

	if ( (null != amtObj) && (amtObj.value != "") ) {
		if (!validateAmount(amtObj, getString("js.billing.editreceipt.payvalidamount")))
		return false;
	}
	if ( (null != tdsObj) && (tdsObj.value != "") ) {
		if(type == 'pri_sponsor_receipt_advance' || type == 'pri_sponsor_receipt_settlement'
		  || type == 'sec_sponsor_receipt_advance' || type == 'sec_sponsor_receipt_settlement') {
			if (!validateAmount(tdsObj, getString("js.billing.editreceipt.tdsvalidamount")))
			return false;
		}
	}

	if (( null != currIdObj) && !currIdObj.disabled && (currIdObj.value != "") ) {
		if ( (null != currAmtObj) && !currAmtObj.disabled && (currAmtObj.value != "") ) {
			if (!validateAmount(currAmtObj, getString("js.billing.editreceipt.currencyvalidamount")))
			return false;
		}
	}else {
		if ( (null != currAmtObj) && !currAmtObj.disabled && (currAmtObj.value != "") ) {
			currAmtObj.value = "";
		}
	}

	if (( null != exchRateObj) && !exchRateObj.disabled && (exchRateObj.value != "") ) {
		if (!validateAmount(exchRateObj, getString("js.billing.editreceipt.exchangerate.validamount")))
		return false;
	}

	if (!modeObj.disabled && modeObj.value == "") {
		showMessage("js.billing.editreceipt.modeisrequired");
		modeObj.focus();
		return false;
	}

	if ( (null != cardObj) && !cardObj.disabled && trim(cardObj.value) == "") {
		showMessage("js.billing.editreceipt.cardtypeisrequired");
		cardObj.focus();
		return false;
	}

	if ( (null != bankObj) && !bankObj.disabled && trim(bankObj.value) == "") {
		showMessage("js.billing.editreceipt.banknameisrequired");
		bankObj.focus();
		return false;
	}
	if ((null != bankObj) && bankObj.value.length > 50) {
		showMessage("js.billing.editreceipt.entershortnamefor.banknamefield");
		bankObj.focus();
		return false;
	}

	if ((null != refObj) && !refObj.disabled && trim(refObj.value) == "") {
		showMessage("js.billing.editreceipt.bankrefnumber.required");
		refObj.focus();
		return false;
	}

	if ((null != refObj) && refObj.value.length >50) {
		 showMessage("js.billing.editreceipt.entershortreferencenumber.refnumfield");
		 refObj.focus();
	     return false;
	}

	if ((null != bankBatchObj) && !bankBatchObj.disabled && trim(bankBatchObj.value) == "") {
		showMessage("js.billing.editreceipt.bankbatchnumber.required");
		bankBatchObj.focus();
		return false;
	}

	if ((null != bankBatchObj) && bankBatchObj.value.length >100) {
		 showMessage("js.billing.editreceipt.batchnumberexceedssize100");
		 bankBatchObj.focus();
		 return false;
	}

	if ((null != cardAuthObj) && !cardAuthObj.disabled && trim(cardAuthObj.value) == "") {
		showMessage("js.billing.editreceipt.cardauthorizationcode.required");
		cardAuthObj.focus();
		return false;
	}

	if ((null != cardAuthObj) && cardAuthObj.value.length >100) {
		 showMessage("js.billing.editreceipt.cardauthorizationcode.exceedssize100");
		 cardAuthObj.focus();
		 return false;
	}

	if ((null != cardHolderObj) && !cardHolderObj.disabled && trim(cardHolderObj.value) == "") {
		showMessage("js.billing.editreceipt.cardholdername.required");
		cardHolderObj.focus();
		return false;
	}

	if ((null != cardHolderObj) && cardHolderObj.value.length >100) {
		 showMessage("js.billing.editreceipt.cardholdername.exceedssize300");
		 cardHolderObj.focus();
		 return false;
	}

	if ((null != cardNumberObj) && !cardNumberObj.disabled && trim(cardNumberObj.value) == "") {
		showMessage("js.billing.editreceipt.cardnumberisrequired");
		cardNumberObj.focus();
		return false;
	}

	if ((null != cardNumberObj) && !cardNumberObj.disabled && trim(cardNumberObj.value) != "" && no_of_credit_debit_card_digits != 0 && cardNumberObj.value.length != no_of_credit_debit_card_digits) {
		 alert("Card number should be "+no_of_credit_debit_card_digits+" digits");
		 cardNumberObj.value="";
		 cardNumberObj.focus();
        return false;
	}

	if ((null != cardNumberObj) && cardNumberObj.value.length >100) {
		 showMessage("js.billing.editreceipt.cardnumberexceedssize150");
		 cardNumberObj.focus();
		 return false;
	}

	if ((null != dispDtObj) && !dispDtObj.disabled) {
		var valid = true;
		valid = valid && validateRequired(dispDtObj, getString("js.billing.editreceipt.dateisrequired"));
		valid = valid && validateRequired(dispTimeObj, getString("js.billing.editreceipt.timeisrequired"));

		valid = valid && doValidateDateField(dispDtObj, 'past');
		valid = valid && validateTime(dispTimeObj);
		valid = valid && doValidateDateTime(dispDtObj, dispTimeObj, "past") ;

		var billOpenDtObj = document.receiptForm.billOpenedDate;
      var billOpenTimeObj = document.receiptForm.billOpenedTime;

      var billCloseDtObj = document.receiptForm.billCloseDate;
      var billCloseTimeObj = document.receiptForm.billCloseTime;

		if (!empty(billOpenDtObj.value) && !empty(billOpenTimeObj.value)) {

			var billdt = getDateTimeFromField(billOpenDtObj, billOpenTimeObj);
			billdt.setSeconds(0);
			billdt.setMilliseconds(0);

			var recptDt = getDateTimeFromField(dispDtObj, dispTimeObj);
			recptDt.setSeconds(0);
			recptDt.setMilliseconds(0);

			var diff = recptDt - billdt;
         if (diff < 0) {
             var msg=getString("js.billing.editreceipt.receiptdatenotless.thanbillopendate");
             msg+= billOpenDtObj.value +" "+billOpenTimeObj.value;
             alert(msg);
             dispDtObj.focus();
             valid = false;
         }

			if (!empty(billCloseDtObj.value) && !empty(billCloseTimeObj.value)) {
	            var billClosedt = getDateTimeFromField(billCloseDtObj, billCloseTimeObj);
				billClosedt.setSeconds(0);
				billClosedt.setMilliseconds(0);

				var diff = recptDt - billClosedt;
            if (diff > 0) {
                var msg=getString("js.billing.editreceipt.receiptdatenotmore.thanbillcloseddate");
                msg+=billCloseDtObj.value +" "+billCloseTimeObj.value;
                alert(msg);
                dispDtObj.focus();
                valid = false;
            }
			}
		}

		if (!valid) return valid;
	}

	if((roleId == 1 || roleId == 2 || editReceiptAmounts == 'A') && getPaise(foreignCurrencyAmt) > 0) {
		if ((null != exchDtObj) && !exchDtObj.disabled) {
			var valid = true;
			valid = valid && validateRequired(exchDtObj, getString("js.billing.editreceipt.exchangedate.required"));
			valid = valid && validateRequired(exchTimeObj, getString("js.billing.editreceipt.exchangetime.required"));

			valid = valid && doValidateDateField(exchDtObj);
			valid = valid && validateTime(exchTimeObj);
			if (!valid) return valid;
		}
	}

	if ((null != cardExpDtObj) && !cardExpDtObj.disabled) {
		if(trim(cardExpDtObj.value) == "") {
			showMessage("js.billing.editreceipt.cardexpirydate.required");
			cardExpDtObj.focus();
			return false;
		}

		var errorStr = validateCardDateStr(cardExpDtObj.value, "future");
		if (errorStr != null) {
			alert(errorStr);
			cardExpDtObj.focus();
			return false;
		}
	}
	return true;
}

function hidePaymentModeForEditReceipt() {
	var paymentModeId = "payment_mode_id";
	
	$("#"+paymentModeId+" option[value='-8']").remove();
	$("#"+paymentModeId+" option[value='-6']").remove();
	$("#"+paymentModeId+" option[value='-7']").remove();
	$("#"+paymentModeId+" option[value='-9']").remove();
}

/* Validation for Cash Limit */
function checkCashLimitValidation(mrno,visitId){
	var amount = 0;
	var refundAmount = 0;
	var gen_deposit_setoff = 0;
	var ip_deposit_setoff = 0;
	var package_deposit_setoff = 0;
	var paymentModeId = document.getElementById("payment_mode_id").value;
	var amtObj = document.getElementById("amount").value;
	if(amtObj != null){
		var amt=amtObj;
	}
	else{
		var amt=totalBillAmt;
	}
	// if payment mod is cash then add/subtract and then with final amount
	// check the limit
	if (paymentModeId == -1 && payType != "F") {
		amount += getAmount(amt);
	}
	if (paymentModeId == -1 && payType == "F") {
		refundAmount += getAmount(-amt);
	}
	if (amount != 0 || refundAmount !=0){
		var paymentModeDetail = findInList(jPaymentModes, "mode_id", paymentModeId);
		var cashTransactionLimitAmt=paymentModeDetail.transaction_limit;
		if (amount > cashTransactionLimitAmt || refundAmount > cashTransactionLimitAmt){
			if (mrno !=  null) {
				alert("Total cash in aggregate from this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");
			}
			else if (mrno ==  null) {
				alert("Total cash in aggregate from this Visit No:"  +visitId+ " in a day reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");
			}
			return false;
		}
		var url = cpath + "/cashlimit/getcashlimit.json";
		var urlParams ='mr_no=' +mrno + '&visit_id=' + visitId + '&cash_payment=' +amount +
		'&refund_payment=' +refundAmount+ '&gen_deposit_setoff=' +gen_deposit_setoff+
		'&ip_deposit_setoff=' +ip_deposit_setoff+ '&package_deposit_setoff=' +package_deposit_setoff;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString() + '?' + urlParams.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				var responseObj =  JSON.parse(ajaxobj.responseText);
				var dayCashLimit=responseObj.cash_limit_details.dayCash;
				var dayRefundCashLimit=responseObj.cash_limit_details.dayRefund;
				var visitCashLimit=responseObj.cash_limit_details.visitCash;
				var visitRefundCashLimit=responseObj.cash_limit_details.visitRefund;
				var transactionLimit=responseObj.cash_limit_details.transactionLimit;
				if (dayCashLimit < 0 && mrno != null) {
					var dayCashLimitAvble = (amount+dayCashLimit) <= 0 ? 0 : (amount+dayCashLimit);
					alert("Total cash in aggregate from this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
						"Remaining Cash limit Available today is Rs." +dayCashLimitAvble + ".");
					return false;
				} else if (dayRefundCashLimit < 0 && mrno != null) {
					var dayRefundLimitAvble = (refundAmount+dayRefundCashLimit) <= 0 ? 0 : (refundAmount+dayRefundCashLimit);
					alert("Total cash in aggregate to this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
						"Remaining Cash limit Available today is Rs." +dayRefundLimitAvble+ ".");
					return false;
				} else if (visitCashLimit < 0) {
					var visitCashLimitAvble = (amount+visitCashLimit) <= 0 ? 0 : (amount+visitCashLimit);
					alert("Total cash transactions relating to Visit No:" +visitId+  " reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
						"Remaining  Cash Limit Available for this visit is Rs." +visitCashLimitAvble+ ".");
					return false;
				} else if (visitRefundCashLimit < 0) {
					var visitRefundLimitAvble = (refundAmount+visitRefundCashLimit) <= 0 ? 0 : (refundAmount+visitRefundCashLimit);
					alert("Total cash transactions relating to Visit No:" +visitId+  " reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
						"Remaining  Cash Limit Available for this visit is Rs." +visitRefundLimitAvble+ ".");
					return false;
				}
			}
		}
	}
	return true;
}

/* Validation for Deposit Cash Limit */
function checkDepositCashLimitValidation(mrno){
	var amount = 0;
	var refundAmount = 0;
	var paymentModeId = document.getElementById("payment_mode_id").value;
		// if payment mod is cash then add/subtract and then with final amount
		// check the limit
	if (paymentModeId == -1 && payType != "DF") {
		amount += getAmount(totalBillAmt);
	}
	if (paymentModeId == -1 && payType == "DF") {
		refundAmount += getAmount(-totalBillAmt);
	}
	if (amount!= 0 || refundAmount !=0){
		var paymentModeDetail = findInList(jPaymentModes, "mode_id", paymentModeId);
		var cashDepositTransactionLimitAmt=paymentModeDetail.transaction_limit;
		if (amount > cashDepositTransactionLimitAmt || refundAmount > cashDepositTransactionLimitAmt){
			alert("Total cash in aggregate from this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +cashDepositTransactionLimitAmt+ ".");
			return false;
		}
		var url = cpath + "/cashlimit/getdepositcashlimit.json";
		var urlParams ='mr_no=' +mrno + '&deposit_cash_payment=' +amount + '&deposit_refund_payment=' +refundAmount;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("GET", url.toString() + '?' + urlParams.toString(), false);
		ajaxobj.send(null);
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					var responseObj =  JSON.parse(ajaxobj.responseText);
					var dayDepositCashLimit=responseObj.deposit_cash_limit_details.dayDepositCash;
					var dayDepositRefundCashLimit=responseObj.deposit_cash_limit_details.dayDepositRefund;
					var availableCashDeposit=responseObj.deposit_cash_limit_details.availableDepositCash;
					var transactionLimit=responseObj.deposit_cash_limit_details.transactionLimit;
					if (availableCashDeposit < 0 || dayDepositCashLimit < 0) {
						var cashDepAvbl=(amount+availableCashDeposit+dayDepositCashLimit) <= 0
						? 0 : (amount+availableCashDeposit+dayDepositCashLimit) ;
						alert("Total cash in aggregate from this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
								"Remaining Cash limit Available today is Rs." +cashDepAvbl+ ".");
						return false;
					} else if (dayDepositRefundCashLimit < 0){
						var cashDepRefundAvbl=(refundAmount+dayDepositRefundCashLimit) <= 0
						? 0 : (refundAmount+dayDepositRefundCashLimit);
						alert("Total cash in aggregate to this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
								"Remaining Cash limit Available today is Rs." +cashDepRefundAvbl+ ".");
						return false;
					}
				}
			}
	}
	return true;
}
