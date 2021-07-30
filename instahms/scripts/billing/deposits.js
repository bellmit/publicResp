/*
 * Common script file for DepositsList.jsp as well as CollectOrRefundDeposits.jsp
 * Defining toolbar for Depositslist.jsp
 *
 */

if(loadToolBar){
	var toolbar = {}
		toolbar.Collect ={
				title  : toolbarOptions["collect"]["name"],
				imageSrc : "icons/Collect.png",
				href   : "pages/BillDischarge/Deposits.do?_method=collectOrRefundDeposits&depositType=R",
				onclick: null,
				description : toolbarOptions["collect"]["description"]
		};
		toolbar.Refund ={
				  title :toolbarOptions["refund"]["name"],
				  imageSrc : "icons/Refund.png",
				  href : "pages/BillDischarge/Deposits.do?_method=collectOrRefundDeposits&depositType=F",
				  onclick:null,
				  description :toolbarOptions["refund"]["description"]
		};
		toolbar.Print ={
				title : toolbarOptions["print"]["name"],
				imageSrc : "icons/Print.png",
				href : "pages/BillDischarge/DepositPrint.do?_method=printDepositeStmt",
				onclick : null,
				target : "_blank",
				description : toolbarOptions["print"]["description"],
	};
}
/*
 * The following are used in CollectOrRefundDeposits.jsp
 */

function init() {
	var payerPhonenational = $('#payer_phone_national');
	var payerPhoneCountryCode = $('#payer_phone_country_code');
	var payerPhoneHelp = $('#payer_phone_help');
	var phoneError = $('#phone_error');

	document.getElementById('ps_mrNo').focus();
	$(".dropdown option[value='-3'], .dropdown option[value='-5']").remove(); //hide for loyalty and oneApollo payment option in deposits screen
	payerPhonenational.on('blur', () => {
		validatePayerPhone();
	});
	payerPhoneCountryCode.select2();
	getExamplePhoneNumber(payerPhoneCountryCode.val(), payerPhoneHelp, phoneError);

	payerPhoneCountryCode.on('change', function(e){
		getExamplePhoneNumber(e.target.value, payerPhoneHelp, phoneError);
	});
	if (preselectPatPackageId) {
		document.getElementById('multi_visit_package').click();
		$("#patientPackageId option[value='" + preselectPatPackageId + "'" + "]").attr("selected", "selected");
		$("#patientPackageId").trigger("change");
	}
}

function doSave() {
	if (max_centers_inc_default>1 && centerId == 0) {
		showMessage("js.billing.deposits.allowed.centers");
		return false;
	}
	if (counterCenter != centerId) {
		showMessage("js.billing.deposits.allowed.counter.centers");
		return false;
	}
	var mrno = document.mainform.mr_no.value;
	if (mrno == null || mrno == '') {
		showMessage("js.billing.deposits.entermrno");
		document.getElementById("ps_mrNo").focus();
		return false;
	}

	if(!empty(patPackDetailsJson)) {
		if(document.getElementById('multi_visit_package')
			&& document.getElementById('multi_visit_package').checked && empty(document.getElementById('patientPackageId').value)) {
			showMessage('js.billing.deposits.select.multivisitpackage');
			document.getElementById('patientPackageId').focus();
			return false;
		}
	}

	var valid = true;
	valid = valid && ajaxCallForCollectOrRefundDeposits(mrno);
	valid = valid && validatePayDates();
	valid = valid && validateCounter();
	valid = valid && validatePaymentRefund();
	valid = valid && validatePaymentTagFields();
	valid = valid && validateAllNumerics();
	valid = valid && validateMultiVisitRefunds();
	valid = valid && validateMultiVisitRefundsWithoutDeposit();
	valid = valid && validatePayerPhone();
    // valid = valid && validateMultiVisitDeposit();
    valid = valid && validateDepositAmount();
    valid = valid && doPaytmTransactions();
    valid = valid && checkTransactionLimitValue();
    valid = valid && validateTax();
    if(income_tax_cash_limit_applicability == 'Y') {
       valid = valid && checkDepositCashLimitValidation();
    }
    
	if (!valid) return false;

	enableFormValues();
	document.mainform.submit();
}

function validatePayerPhone(){
	var payerPhone = $('#payer_phone');
	var payerPhoneNational = $('#payer_phone_national');
	var payerCountryCode=$("#payer_phone_country_code");
	var phoneError =$("#phone_error");
	var phoneValid = $("#phone_valid");
	
	clearErrorsAndValidatePhoneNumber(payerPhone,phoneValid,payerPhoneNational, payerCountryCode, phoneError, true, null );
	if(phoneValid.val() != 'N'){
		phoneError.text('');
	}
	return !(phoneValid.val() == 'N');
}

function getTotalAmount() {
	return 0;
}

function getTotalAmountDue() {
	return 0;
}

function validateDepositAmount() {
	var availableDepositsPaise = getPaise(document.mainform.avlblDeposits.value);
	var availableIPDepositPaise = 0;
	if(null != document.mainform.avlblIPDeposits){
		availableIPDepositPaise = getPaise(document.mainform.avlblIPDeposits.value)
	}
	var totalAmt =  Math.abs(availableDepositsPaise);
	var totIPAmt =  Math.abs(availableIPDepositPaise);
	var totGenAmt = totalAmt - totIPAmt;
	var refundAmt = getPayingAmountPaiseGeneral('refund');
	var pkgAmt = getPackageDepositsTotal();
	refundAmt = Math.abs(refundAmt);

	var applicableToIP = document.getElementById("applicable_to_ip");

	if(applicableToIP.checked){
		if(refundAmt > totIPAmt){
			showMessage("js.billing.deposits.refundsnotmore.availableipdeposits");
			return false;
		}
	}else {
		if(refundAmt > totGenAmt){
			showMessage("js.billing.deposits.refundsnotmore.availablegendeposits");
			return false;
		}
	}
	// showMessage("general amounts :" + totalAmt + ":" + refundAmt + ":" + pkgAmt);

	if (refundAmt > totalAmt - pkgAmt) {
		showMessage("js.billing.deposits.refundsnotmore.availabledeposits");
		return false;
	}
	return true;
}

function getPackageDepositsTotal() {
    var packDeposit = 0;
    if (!empty(packageDeposits)) {
	for (var i = 0; i < packageDeposits.length; i++) {
	    packDeposit += (getPaise(packageDeposits[i].total_deposits) - getPaise(packageDeposits[i].total_set_offs));
	}
    }
    return Math.abs(packDeposit);
}

function validateMultiVisitRefunds() {
    var numPayments = getNumOfPayments();
    if (numPayments <= 0) return true;
    if (empty(packageDeposits)) return true;
    for (var i =0; i < packageDeposits.length; i++) {
	var patientPackageId = packageDeposits[i].pat_package_id;
	var payAmount = getPayingAmountPaiseByPackage(patientPackageId);
	if (payAmount < 0 && // net refund
	    Math.abs(payAmount) > getPaise((packageDeposits[i].total_deposits - packageDeposits[i].total_set_offs))) {
	    var msg=getString("js.billing.deposits.refundamtnotmore.depositspkg");
	    msg+=" ";
	    msg+=getPackageName(patientPackageId);
	    alert(msg);
	    return false;
	}
    }
    return true;
}

function validateMultiVisitRefundsWithoutDeposit() {
	var numPayments = getNumOfPayments();
    if (numPayments <= 0) return true;
    if (empty(packageDeposits)){
    	for (var i = 0; i < numPayments; i++) {
            var packageObj = getIndexedFormElement(documentForm, "patientPackageId", i);
            var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
            var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
            if(packageObj  && packageObj.value !='' && paymentObj.value ==='refund' && amtObj && amtObj.value !='') {
            	var msg=getString("js.billing.deposits.refundamtnotmore.depositspkg");
         	    msg+=" ";
         	    msg+=getPackageName(packageObj.value);
         	    alert(msg);
         	    return false;
            } 
        } 	
    } else {
    	// HMS-30654: if the user has multiple packages and one of them have a deposit.
    	// But refunding from some other package.
    	for (var i = 0; i < numPayments; i++) {
            var packageObj = getIndexedFormElement(documentForm, "patientPackageId", i);
            var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
            var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
            if(packageObj  && packageObj.value !='' && amtObj && amtObj.value !='') {
            	var selectedPackageStatus = packageObj.selectedOptions[0].getAttribute("data-status");
				var selectedPackageIsDiscontinued = packageObj.selectedOptions[0].getAttribute("data-discontinued");
                // If selected package is discontinued or closed, then collection is not allowed
				if (paymentObj.value === 'receipt_settlement' && (selectedPackageIsDiscontinued == 'true' || selectedPackageStatus == 'C')) {
            		alert(getString("js.billing.deposits.package.settlementnotapplicable")); 
            		packageObj.focus();
					packageObj.selectedIndex = 0;
					return false; 
            	}
				// validation against refund, if receipt_settlement, then continue
            	if (paymentObj.value === 'receipt_settlement' ) {
            	    continue;
            	}
				var patientPackageId = packageObj.value;					
				var filteredPackageDeposits = packageDeposits.filter(val => (val.pat_package_id === Number(patientPackageId)))
				if(empty(filteredPackageDeposits)) {
					var msg=getString("js.billing.deposits.refundamtnotmore.depositspkg");
					msg+=" ";
					msg+=getPackageName(patientPackageId);
					alert(msg);
					return false;
				}	
            }
        } 
    }
    return true;
}

function getPackageName(patientPackageId) {
    var numPayments = getNumOfPayments();
    if (numPayments <= 0) return true;
    for (var i = 0; i < numPayments; i++) {
        var packageObj = getIndexedFormElement(documentForm, "patientPackageId", i);
	if (packageObj && !empty(packageObj.value) && packageObj.value == patientPackageId) {
	    return getSelText(packageObj);
	}
    }
}

function validateMultiVisitRefundsOld() {
	var totalRefundAmt = getMultiVisitPackageBalanceAmt(patPackDetailsJson);
	totalRefundAmt = Math.abs(totalRefundAmt);
	var refundAmt = getPayingAmountPaise('refund');
	refundAmt = Math.abs(refundAmt);

	if(!validateMVRefund(patPackDetailsJson)) {
		showMessage("js.billing.deposits.refundnotpossible");
		return false;
	}

	if (totalRefundAmt != 0 && refundAmt > totalRefundAmt) {
		showMessage("js.billing.deposits.refundsnotmore.multipkgdeposits");
		return false;
	}
	return true;
}

function getPayingAmountPaiseByPackage(patientPackageId, paymentType) {
    var numPayments = getNumOfPayments();
    if (numPayments <= 0) return true;
    var amt = 0;
    for (var i = 0; i < numPayments; i++) {
        var packageObj = getIndexedFormElement(documentForm, "patientPackageId", i);
        var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
        var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
	if (empty(patientPackageId) || (packageObj && !empty(packageObj.value) && patientPackageId == packageObj.value)) { // there is a match
	    if (empty(paymentType) || (paymentObj && !empty(paymentObj.value) && paymentType == paymentObj.value)) {
		if (amtObj && !empty(amtObj.value)) {
		    amt += getPaise(amtObj.value) * (paymentObj && !empty(paymentObj.value) && (paymentObj.value == 'refund') ? -1 : 1);
		}
	    }
        }
    }
    return amt;
}

function getPayingAmountPaiseGeneral(paymentType) {
    var numPayments = getNumOfPayments();
    if (numPayments <= 0) return true;
    var amt = 0;
    for (var i = 0; i < numPayments; i++) {
        var packageObj = getIndexedFormElement(documentForm, "patientPackageId", i);
        var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
        var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
        if ((!packageObj || empty(packageObj.value))) { // there is a match
            if (empty(paymentType) || (paymentObj && !empty(paymentObj.value) && paymentType == paymentObj.value)) {
                if (amtObj && !empty(amtObj.value)) {
                    amt += getPaise(amtObj.value) * (paymentObj && !empty(paymentObj.value) && (paymentObj.value == 'refund') ? -1 : 1);
                }
            }
        }
    }
    return amt;

}

function validateMultiVisitDeposit() {
	if(!validateMVDeposit(patPackDetailsJson)) {
		showMessage("js.billing.deposits.depositnotpossible");
		return false;
	}
	return true;
}

function IsAnyPaymnetHappened() {
	var numPayments = getNumOfPayments();
	if (numPayments <= 0) 
		return false;
	
	for (var i = 0; i < numPayments; i++) {
		var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
		if (amtObj && !empty(amtObj.value) && (amtObj.value != 0)) {
			return true;
		}
	}
	return false;

}
function ajaxCallForCollectOrRefundDeposits(mrNo) {
	var check= IsAnyPaymnetHappened();
	if(check){
		var ajaxobj = newXMLHttpRequest();
		var url = cpath + "/pages/BillDischarge/Deposits.do?_method=collectOrRefundDepositsAjax&mr_no=" + mrNo;

		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				return callbackFunc(ajaxobj.responseText);
			}
		}
	}
	return true;
}

function callbackFunc(responseText) {
	eval("returnedData =" + responseText);
	if (returnedData != null) {
		var packTotalDeposit = 0;
		var packTotalDepositSetOffs = 0;
		var avlblIPDeposits = 0;

		var deposit_details = returnedData.depositDetails;
		var avlDeposite= deposit_details.hosp_total_balance;

		var ip_deposit_details = returnedData.ipDepositDetails;
		if(ip_deposit_details != null){
			avlblIPDeposits = ip_deposit_details.total_ip_deposits - ip_deposit_details.total_ip_set_offs;
		}

		packageDeposits =returnedData.packageDeposits;

		if (!empty(packageDeposits)) {
			for (var i = 0; i < packageDeposits.length; i++) {
				packTotalDeposit +=packageDeposits[i].total_deposits;
				packTotalDepositSetOffs += packageDeposits[i].total_set_offs;
			}
		}

		var pageAvailableDeposit = getPaise(document.getElementById('availableDepositId').innerHTML);
		var currentAvailableDeposit =getPaise(avlDeposite-(packTotalDeposit-packTotalDepositSetOffs));
		if(pageAvailableDeposit != currentAvailableDeposit){
			alert("Displayed Deposit Availability in this screen and Actual Deposit available with selected MrNo are not matching. " +
			"There are latest updates on deposit availability status. Please refresh your screen and verify the data before you continue your action.");
			return false;
		}

		if(ip_deposit_details != null){
			var pageAvailableIPDeposit = getPaise(document.getElementById('availableIPDepositId').innerHTML);
			var currentAvailableIPDeposit =getPaise(avlblIPDeposits);
			if(pageAvailableIPDeposit != currentAvailableIPDeposit){
				alert("Displayed Deposit Availability in this screen and Actual Deposit available with selected MrNo are not matching. " +
				"There are latest updates on deposit availability status. Please refresh your screen and verify the data before you continue your action.");
				return false;
			}
		}

		if (!empty(packageDeposits)) {
			var pageAvailablePackDeposit = getPaise(document.getElementById('availablePackageDepositId').innerHTML);
			var currentAvailablePackDeposit =getPaise(packTotalDeposit-packTotalDepositSetOffs);
			if(pageAvailablePackDeposit != currentAvailablePackDeposit){
				alert("Displayed Deposit Availability in this screen and Actual Deposit available with selected MrNo are not matching. " +
				"There are latest updates on deposit availability status. Please refresh your screen and verify the data before you continue your action.");
				return false
			}
		}

	}
	return true;
}

function validateTax(){
	var numPayments = getNumOfPayments();
    if (numPayments <= 0) return true;
	for (var i = 0; i < numPayments; i++) {
        var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
        if(paymentObj.value ==='refund') {
        	continue;
        } else{
        	if(taxMandatory == 'Y') {
        		if(document.getElementById("tax_subgrp_primary") != null
        				&& document.getElementById("tax_subgrp_primary").value == "") {
        			alert("Tax Selection is Mandatory");
        			return false;
        		}
        			
        		if(document.getElementById("tax_subgrp_secondary") != null) {
        			if(document.getElementById("tax_subgrp_secondary").value == ""){
        				alert("Tax Selection is Mandatory");
        				return false;
        			}
        		}
        		return true;
        	}
        	return true;
        }
    }
	return true;
}
/* Validation for Deposit Cash Limit */
function checkDepositCashLimitValidation(){
	var numPayments = getNumOfPayments();
	if (numPayments <= 0) return true;
	var amount = 0;
	var refundAmount = 0;
	var mrno = document.mainform.mr_no.value;
	for (i=0; i<numPayments; i++){
		var totPayingAmt = "totPayingAmt"+i;
		var paymentModeId = "paymentModeId"+i;
		// if payment mod is cash then add/subtract and then with final amount
		// check the limit
		var paymentModelValue = $("#"+paymentModeId+" option:selected").val();
		var paymentType = getIndexedFormElement(documentForm, "paymentType", i).value;
		if (paymentModelValue == -1 && paymentType != "refund") {
			var cashAmount = $("#"+totPayingAmt+"").val();
			amount += getAmount(cashAmount);
		}
		if (paymentModelValue == -1 && paymentType == "refund") {
			var refundCashAmount = $("#"+totPayingAmt+"").val();
			refundAmount += getAmount(refundCashAmount);
		}
	}
	if (amount != 0 || refundAmount !=0){
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
					if (availableCashDeposit < 0) {
						var cashDepAvbl=(amount+availableCashDeposit) <= 0
						? 0 : (amount+availableCashDeposit) ;
						alert("Available Deposit from this MRNO:" +mrno+ " reaches the allowed cash deposit availability threshold of  Rs." +transactionLimit+ ".\n" +
								"Remaining Cash limit Available is Rs." +cashDepAvbl+ ".");
						return false;
					} else if (dayDepositCashLimit < 0) {
						var cashDepAvbl=(amount+dayDepositCashLimit) <= 0
						? 0 : (amount+dayDepositCashLimit) ;
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

