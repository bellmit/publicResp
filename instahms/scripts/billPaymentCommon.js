
/* Get the bill amount total and due amount total */
var total_AmtPaise =  0;
var total_AmtDuePaise = 0;
var total_SpnsrAmtDuePaise = 0;
var total_DepositAmtAvailablePaise = 0;
var total_LessDeposits = 0;
var subscription;
var pineLabResponsePoller;
var pendingPineLabPaymentDialogId;
var total_RewardPointsAvailable = 0;
var total_RewardPointsAmountAvailable = 0;
var total_LessRewardPoints = 0;
var total_LessRewardPointsAmount = 0;
var loyaltyDialog=null;
var salucroDialog=null;
function resetTotalsForPayments() {
	total_AmtPaise =  getTotalAmount();
	total_AmtDuePaise = getTotalAmountDue();
	total_AmtDuePaise = total_AmtDuePaise - total_LessDeposits - total_LessRewardPointsAmount;
}
function resetTotalsForCreditNotePayments() {
	total_AmtPaise =  getTotalAmount();
	total_AmtDuePaise = getTotalNetAmountDue();
	total_AmtDuePaise = total_AmtDuePaise - total_LessDeposits - total_LessRewardPointsAmount;
}

function resetTotalsForSpnsrPayments() {
	total_SpnsrAmtDuePaise = getTotalSpnsrAmountDue();
}

function resetTotalsForDepositPayments() {
	total_DepositAmtAvailablePaise = getTotalDepositAmountAvailable();
}

function resetTotalsForPointsRedeemed() {
	total_RewardPointsAvailable = getAmountDecimal(getTotalRewardPointsAvailable(), 2);
	total_RewardPointsAmountAvailable = getTotalRewardPointsAmountAvailable();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function setFormIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(documentForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getFormIndexedValue(name, index) {
	var obj = getIndexedFormElement(documentForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function showRewardPoints(modeObj) {
        var paymentModeValue = modeObj.value;
        var index=modeObj.id.substr(-1);
        var paymentModeName = $(modeObj).children(':selected').text();
        var paymentType = getIndexedFormElement(documentForm, "paymentType", index).value;
        if ( (paymentModeValue == -2 || paymentModeValue == -3
                        || paymentModeValue == -5 || paymentModeValue == -9) && paymentType == 'refund') {
                alert('Amount cannot be Refund by ' + paymentModeName);
                $(modeObj).val(-1); //reset to cash
                return false;
        }
        if(paymentModeValue === '-9') {
                $(modeObj).closest("tr").find(".redeemPointsTD").show();
                $("#totPayingAmt"+index).closest("td").find(".redeemPointsTD").show();
                $("#totPayingAmt"+index).val("");
                $("#totPayingAmt"+index).prop("readonly",true);
        }else{
                $(modeObj).closest("tr").find(".redeemPointsTD").hide();
                $("#totPayingAmt"+index).closest("td").find(".redeemPointsTD").hide();
                $("#totPayingAmt"+index).prop("readonly",false);
        }
}


function hidePaymentModeType(modeObj) {
        var selPaymentModeValue = modeObj.value;
        var selPaymentModeId = modeObj.id;
        if(selPaymentModeValue === '-8' || selPaymentModeValue === '-6' || selPaymentModeValue === '-7' || selPaymentModeValue === '-9') {
                var numPayments = getNumOfPayments();
                for (i=0; i<numPayments; i++){
                        var paymentModeId = "paymentModeId"+i;
                        var paymentModelValue = $("#"+paymentModeId+" option:selected").val();

                        if(selPaymentModeId == paymentModeId)
                                continue;

                        if(selPaymentModeValue ==  paymentModelValue) {
                                alert("The payment mode is already selected \nPlease select some other payment mode");
                                $("#"+selPaymentModeId).val(-1);
                                $("#"+selPaymentModeId).trigger("change");
                        }

                }
        }
}

function getNumOfPayments() {
	var paymentTable = document.getElementById("paymentsTable");
	if (paymentTable) {
		var numPayments = (paymentTable.rows.length - paymentRowsUncloned) /paymentRows;
		return numPayments;
	}
	return 0;
}

function setTotalPayAmount() {
	var numPayments = getNumOfPayments();
	if (numPayments == 1) {
		var obj = getIndexedFormElement(documentForm, "totPayingAmt", 0);
		var pmtObj = getIndexedFormElement(documentForm, "paymentType", 0);
		var pmtMode = getIndexedFormElement(documentForm, "paymentModeId", 0);
		if (obj != null && pmtMode != null && pmtMode.value != -9) {
			if (total_AmtDuePaise >= 0) {
				setPaymentType(pmtObj, "receipt_settlement");
				obj.value = formatAmountPaise(total_AmtDuePaise);

			} else if (total_AmtDuePaise < 0) {
				setPaymentType(pmtObj, "refund");
				obj.value = formatAmountPaise(Math.abs(total_AmtDuePaise));

			} else {
				setPaymentType(pmtObj, "");
				obj.value = "";
			}
		}
		setColor(pmtObj);
	}
}

function setPaymentType(pmtObj, pmtVal) {
	var index=0;
	if (pmtObj != null) {
		for(var i=0; i<pmtObj.options.length; i++) {
			var opt_value = pmtObj.options[i].value;
			if (opt_value == pmtVal) {
				pmtObj.selectedIndex = i;
				return;
			}
		}
	}
}

function validatePaymentRefund() {

	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var totPayAmt = getIndexedFormElement(documentForm, "totPayingAmt", i);

			if (getPaise(totPayAmt.value) != 0) {
				var payType = getIndexedFormElement(documentForm, "paymentType", i);
				if (payType.value == "refund" && allowRefundRights != "A") {
					showMessage("js.laboratory.radiology.billpaymentcommon.notauthorized.refund");
					payType.focus();
					return false;
				}
				var paymentMode = getIndexedFormElement(documentForm, "paymentModeId", i);
				var depositPaymentModes = ['-6','-7','-8'];
				if (payType.value == "refund" && (paymentMode.value == -6 || paymentMode.value == -8) && generalDepositSetOff > 0 && (getPaise(totPayAmt.value) > getPaise(generalDepositSetOff)) ) {
			        alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ generalDepositSetOff);
				    totPayAmt.focus();
				    return false;
				}
				if (payType.value == "refund" && paymentMode.value == -7 && generalDepositSetOff > 0 && (getPaise(totPayAmt.value) > getPaise(ipDepositSetOff)) ) {
			        alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ ipDepositSetOff);
				    totPayAmt.focus();
				    return false;
				}
				//hospital bill refund validation starts here
				if(typeof totalBilledAmount != 'undefined'){
				 if (payType.value == "refund" && getPaise(totPayAmt.value) != 0 && depositPaymentModes.indexOf(paymentMode.value) == -1) {
				    if(totalBilledAmount > 0 ){
				       if(getPaise(totPayAmt.value) > getPaise(existingReceipts)){
				           alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ existingReceipts);
					       totPayAmt.focus();
					    return false;
					    }
					  }
					  else {
					  if(getPaise(totPayAmt.value) > (Math.abs(total_AmtDuePaise))){
				            alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ Math.abs(getPaiseReverse(total_AmtDuePaise)));
				            totPayAmt.focus();
					    return false;
					    }
					  }
				}
			}
			  //dental & pharmacy bill refund validation starts here
			  if(typeof screenId != 'undefined'){
			    if(screenId == 'dental_consultations'){
			     if (payType.value == "refund" && getPaise(totPayAmt.value) != 0 && depositPaymentModes.indexOf(paymentMode.value) == -1) {
			              var patPaid = getPaise(document.getElementById("totalRec").value);
			              var totalDue = getPaise(document.getElementById("totalDue").value);
				          if(getPaise(document.getElementById("totalAmt").value) > 0 ){
				               if(getPaise(totPayAmt.value) > patPaid ){
				                     alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ getPaiseReverse(patPaid));
					                 totPayAmt.focus();
					        return false;
					        }
					     }
					     else {
					     if(getPaise(totPayAmt.value) > Math.abs(totalDue)){
				            alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ Math.abs(getPaiseReverse(totalDue)));
				            totPayAmt.focus();
					        return false;
					        }
					    }
				}
			  }
				else if(screenId == 'pharma_retail_sale_pending_bills') {
				  if (payType.value == "refund" && getPaise(totPayAmt.value) != 0 && depositPaymentModes.indexOf(paymentMode.value) == -1) {
				          var patPaid = getPaise(document.getElementById("netPay").value);
			              var totalDue = getPaise(document.getElementById("amountdue").value);
			              var billedAmount = getPaise(document.getElementById('l_total').textContent);
			              if(billedAmount > 0 ){
				               if(getPaise(totPayAmt.value) > patPaid ){
				                     alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ getPaiseReverse(patPaid));
					                 totPayAmt.focus();
					                 return false;
					                 }
					     }
					    else {
					    if(getPaise(totPayAmt.value) > Math.abs(totalDue) ){
					              alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ Math.abs(getPaiseReverse(totalDue)));
					              totPayAmt.focus();
					              return false;
					              }
					    }
				 }
				}
				else if(screenId == 'pharma_item_credit_bill') {
				  	  if (payType.value == "refund" && getPaise(totPayAmt.value) != 0 && depositPaymentModes.indexOf(paymentMode.value) == -1) {
				          var patPaid = getPaise(document.getElementById("lblExistingReceipts").textContent);
			              var totalDue = getPaise(document.getElementById("lblTotAmtDue").textContent);
			              var billedAmount = getPaise(document.getElementById('lblTotAmt').textContent);
			              if(billedAmount > 0 ){
				               if(getPaise(totPayAmt.value) > patPaid ){
				                     alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ getPaiseReverse(patPaid));
					                 totPayAmt.focus();
					                 return false;
					        }
					     }
					    else {
					    if(getPaise(totPayAmt.value) > Math.abs(totalDue)){
				                 alert(getString("js.laboratory.radiology.billpaymentcommon.refundamt.notexceed")+' '+ Math.abs(getPaiseReverse(totalDue)));
				                 totPayAmt.focus();
				                 return false;
				             }
				        }
        		    }
        		 }
        	 }
           }
		}
	}
	return true;
}

function onAddPayMode(obj) {
	total_AmtPaise =  getTotalAmount();
	total_AmtDuePaise = getTotalAmountDue();
	var table = document.getElementById("paymentsTable");
	var thisRow = getThisRow(obj);		// location of the add button
	var numPayments = getNumOfPayments();
	var id = numPayments;
	if (document.getElementById("payDD") != null)
		document.getElementById("payDD").style.height="auto";

	var payObj = getIndexedFormElement(documentForm, "totPayingAmt", numPayments -1 );

	if ( (null != payObj) && (payObj.value == "") ) {
		showMessage("js.laboratory.radiology.billpaymentcommon.payamount");
		payObj.focus();

	} else {
		for (var i=(paymentRowsUncloned-1); i<(paymentRows + paymentRowsUncloned-1) ; i++) {
			var newRow = table.rows[i].cloneNode(true);
			table.tBodies[0].insertBefore(newRow, thisRow);
		}
		clearPaymentFields(id);

		var paymentObj = getIndexedFormElement(documentForm, "paymentType", id);

		var totalAmt =  Math.abs(getTotalAmountDue());

		var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund') + getPayingAmountPaise('sponsor');
		payingAmt = Math.abs(payingAmt);

		var prevPaymentObj = getIndexedFormElement(documentForm, "paymentType", (id-1));
		var prevPaymentModeObj = getIndexedFormElement(documentForm, "paymentModeId", (id-1));
		var prevPaymentType = prevPaymentObj != null ? prevPaymentObj.value : "";
		var isSponsorPay = (prevPaymentType.indexOf("sponsor") != -1);


		if (!isSponsorPay) {
			if ((totalAmt - payingAmt) < 0)
				setSelectedIndex(paymentObj, "refund");
			else if ((totalAmt - payingAmt) >= 0)
				setSelectedIndex(paymentObj, "receipt_advance");
		}else {
			setSelectedIndex(paymentObj, "receipt_advance");
		}

		var modeObj = getIndexedFormElement(documentForm, "paymentModeId", id);
		var currObj = getIndexedFormElement(documentForm, "currencyId", id);
		var totPayingAmtObj = getIndexedFormElement(documentForm, "totPayingAmt", id);
		var cardTypeIdObj = getIndexedFormElement(documentForm, "cardTypeId", id);
		var totpObj = getIndexedFormElement(documentForm, "totp", id);
		var mobNumberObj = getIndexedFormElement(documentForm, "mobNumber", id);
		var edcMachineObj = getIndexedFormElement(documentForm, "edcMachine", id);
		var processPaymentObj = getIndexedFormElement(documentForm, "processPayment", id);
		var plutusTxnIdObj = getIndexedFormElement(documentForm, "plutusTxnId", id);
		var rewardPointsRedeemedObj = getIndexedFormElement(documentForm, "rewardPointsRedeemed", id);
		var isRedeemPoints = (modeObj.value == "-9");//if paymentMode is redeem points

		cardTypeIdObj.id = "cardTypeId"+id;
		modeObj.id = "paymentModeId"+id;
		totPayingAmtObj.id = "totPayingAmt"+id;
		totpObj.id = "totp"+id;
		mobNumberObj.id = "mobNumber"+id;
		edcMachineObj.id = "edcMachine"+id;
		processPaymentObj.id = "processPayment"+id;
		plutusTxnIdObj.id = "plutusTxnId"+id;
		rewardPointsRedeemedObj.id = "rewardPointsRedeemed"+id;
		document.getElementById("paymentModeId"+id).disabled = false;
		document.getElementById("processPayment"+id).hidden = true;
		document.getElementById("plutusTxnId"+id).hidden = true;
		document.getElementById("plutusTxnId"+id).value = "-1"; //-1 denotes that we have no active transaction
		document.getElementById("edcMachine"+id).disabled = true;
		//while adding new payment, if previous payment is Redeem points(-9)
		if (modeObj) {
			document.getElementById("rewardPointsRedeemed"+id).value = "0";
			//showRewardPoints(modeObj);
			$(modeObj).closest("tr").find(".redeemPointsTD").hide();
			$("#totPayingAmt"+id).val(0);
			$("#totPayingAmt"+id).closest("td").find(".redeemPointsTD").hide();
			$("#totPayingAmt"+id).prop("readonly",false);
		}
		var type = paymentObj.value;
		if (currObj != null)
			getCurrencyDetails(currObj);
		else
			setPayingAmountPaise(type, id);

		if (isSponsorPay)
			setFormIndexedValue("totPayingAmt", id, '');

		enableBankDetails(modeObj);
		enableDisableDeletePayMode();

	}
	setTaxDetailsDeposits();
	return false;
}

function setColor(paymentObj) {
	if (paymentObj != null && paymentObj.value =="refund")
		paymentObj.style.color = 'red';
	else
		paymentObj.style.color = '';
}

function populatePaymentType(billType, defaultPaymentType) {
 	var payment_type_el = document.getElementById('paymentType');
 	var rec_selected = '';
 	var ref_selected = '';
 	var adv_selected = '';
	payment_type_el.length = 0; // clear the previously populated list
	if (billType == "C") {
		if (defaultPaymentType == 'A')
			adv_selected = 'selected';
		payment_type_el.innerHTML += '<option value="receipt_advance"'+ adv_selected +'">Advance</option>';
		if (defaultPaymentType == 'R')
			 rec_selected = 'selected';
		payment_type_el.innerHTML += '<option value="receipt_settlement"'+ rec_selected +'">Settlement</option>';
	}
	if (billType == "P") {
		if (defaultPaymentType == 'R')
			 rec_selected = 'selected';
		payment_type_el.innerHTML += '<option value="receipt_settlement"'+ rec_selected +'">Settlement</option>';
	}
	if (defaultPaymentType == 'F')
		ref_selected = 'selected';
	payment_type_el.innerHTML += '<option value="refund" "'+ ref_selected +'">Refund</option>'

}

function enableBankDetails(modeObj) {
	var thisRow  = getThisRow(modeObj);
	var mode = modeObj.value;
	var numPayments = getNumOfPayments();
	var paymentIndex = numPayments;
	//R.C:write a seperate method for this like refreshCommisionDetails()

	if (numPayments > 0) {
		paymentIndex = ((thisRow.rowIndex + 1) - paymentRowsUncloned) / paymentRows;
	}

	var bankObj = getIndexedFormElement(documentForm, "bankName", paymentIndex);
	var cardObj = getIndexedFormElement(documentForm, "cardTypeId", paymentIndex);
	var refObj = getIndexedFormElement(documentForm, "refNumber", paymentIndex);
	var totPayingAmtObj = getIndexedFormElement(documentForm, "totPayingAmt", paymentIndex);

	var bankBatchObj = getIndexedFormElement(documentForm, "bankBatchNo", paymentIndex);
	var cardAuthObj = getIndexedFormElement(documentForm, "cardAuthCode", paymentIndex);
	var cardHolderObj = getIndexedFormElement(documentForm, "cardHolderName", paymentIndex);

	var cardNumberObj = getIndexedFormElement(documentForm, "cardNumber", paymentIndex);
	var cardExpDtObj = getIndexedFormElement(documentForm, "cardExpDate", paymentIndex);

	var mobNumberObj = getIndexedFormElement(documentForm, "mobNumber", paymentIndex);
	var totpObj = getIndexedFormElement(documentForm, "totp", paymentIndex);

	var commissionPerObj = getIndexedFormElement(documentForm, "commissionPer", paymentIndex);
	var commissionAmtObj = getIndexedFormElement(documentForm, "commissionAmt", paymentIndex);

	setSelectedIndex(cardObj, "");

	var currencyObj = getIndexedFormElement(documentForm, "currencyId", paymentIndex);

	if (currencyObj != null && currencyObj.value != "") {
		setSelectedIndex(currencyObj, "");
		getCurrencyDetails(currencyObj);
	}

	var paymentModeDetail = findInList(jPaymentModes, "mode_id", mode);

	cardObj.disabled = (paymentModeDetail.card_type_required != 'Y');
	bankObj.disabled = (paymentModeDetail.bank_required != 'Y');
	if (refObj.readOnly){
		refObj.readOnly = false;
		refObj.value = "";
	}
	refObj.disabled = (paymentModeDetail.ref_required != 'Y');

	bankBatchObj.disabled = (paymentModeDetail.bank_batch_required != 'Y');
	cardAuthObj.disabled = (paymentModeDetail.card_auth_required != 'Y');
	cardHolderObj.disabled = (paymentModeDetail.card_holder_required != 'Y');

	cardNumberObj.disabled = (paymentModeDetail.card_number_required != 'Y');
	cardExpDtObj.disabled = (paymentModeDetail.card_expdate_required != 'Y');
	if (mobNumberObj.readOnly){
		mobNumberObj.readOnly = false;
		mobNumberObj.value = "";
	}
	mobNumberObj.disabled = (paymentModeDetail.mobile_number_required != 'Y');
	totpObj.disabled = (paymentModeDetail.totp_required != 'Y');
	var creditNoteScreenElement =document.getElementById("creditNoteTotalAmt");
	if (creditNoteScreenElement == null){
			totPayingAmtObj.readOnly=false;
		}
	if (bankObj.disabled) bankObj.value = "";
	if (refObj.disabled)  refObj.value = "";

	if (bankBatchObj.disabled) bankBatchObj.value = "";
	if (cardAuthObj.disabled)  cardAuthObj.value = "";
	if (cardHolderObj.disabled)  cardHolderObj.value = "";

	if (cardNumberObj.disabled)  cardNumberObj.value = "";
	if (cardExpDtObj.disabled)  cardExpDtObj.value = "";

	if (mobNumberObj.disabled)  mobNumberObj.value = "";
	if (totpObj.disabled)  totpObj.value = "";

	addMandatoryIndicators();
}


function enableCommissionDetails(obj) {
	var Dom = YAHOO.util.Dom;
	var thisRow  = getThisRow(obj);
	var mode = obj.value;
	var numPayments = getNumOfPayments();

	var paymentIndex = numPayments;
	var index = obj.id.substring(obj.name.length);
	var optionObj = getIndexedFormElement(documentForm, "paymentModeId", index);
	var paymentTypeObj = getIndexedFormElement(documentForm, "totPayingAmt", index);

	jgetAllCreditTypes = eval(jgetAllCreditTypes);

		//R.C:Code seems similar for all screen.No need of seperate blocks for each screen write common code as much as possible.
	var creditType = getIndexedFormElement(documentForm, "cardTypeId", index );
	if(jgetAllCreditTypes != undefined && creditType.value != '' ) {
		var creditTypeObj = findInList(jgetAllCreditTypes, "card_type_id", creditType.value);
		var payObj = getIndexedFormElement(documentForm, "totPayingAmt", index );
		var totalCommonAmt = payObj.value;
		var commissionPercentage =0;
		var commissionAmount = 0;
		if (creditTypeObj.commission_percentage != null) {
			var commissionPercentage = (creditTypeObj.commission_percentage).toFixed(2);
			var commissionAmount = (totalCommonAmt*commissionPercentage/100).toFixed(decDigits);
		}
		if (!empty(creditTypeObj) && totalCommonAmt!='' && commissionPercentage > 0 && creditType.value != '') {
			if (creditTypeObj.commission_percentage != '' && commissionPercentage > 0 ) {
					var Percentage = Dom.getElementsByClassName("Per", "label")[index];
					Percentage.textContent =  'Commission (%):';
					var commissionPerObj = Dom.getElementsByClassName("commissionPer", "label")[index];
					commissionPerObj.innerHTML =  commissionPercentage;
					setCommissionHiddenValue(index, "commissionPer" ,commissionPercentage);

					var Amount = Dom.getElementsByClassName("Amt", "label")[index];
					Amount.textContent =  'Commission Amount:';
					var commissionAmtObj = Dom.getElementsByClassName("commissionAmt", "label")[index];
					commissionAmtObj.innerHTML =  commissionAmount;
					setCommissionHiddenValue(index, "commissionAmt" ,commissionAmount);


				}
		} else if (optionObj.value != 1 || creditTypeObj.value == '' || commissionPercentage == '' || commissionPercentage == 0) {
			var commissionPerObj = Dom.getElementsByClassName("commissionPer", "label")[index];
			commissionPerObj.textContent =  "";
			var Percentage = Dom.getElementsByClassName("Per", "label")[index];
			Percentage.innerHTML =  "";
			setCommissionHiddenValue(index, "commissionPer" ,0);

			var commissionAmtObj = Dom.getElementsByClassName("commissionAmt", "label")[index];
			commissionAmtObj.textContent =  "";
			var Amount = Dom.getElementsByClassName("Amt", "label")[index];
			Amount.innerHTML =  "";
			setCommissionHiddenValue(index, "commissionAmt" ,0);
		}

	} else {
		var commissionPerObj = Dom.getElementsByClassName("commissionPer", "label")[index];
		commissionPerObj.textContent =  "";
		var Percentage = Dom.getElementsByClassName("Per", "label")[index];
		Percentage.textContent =  "";
		setCommissionHiddenValue(index, "commissionPer" ,0);
		var commissionAmtObj = Dom.getElementsByClassName("commissionAmt", "label")[index];
		commissionAmtObj.textContent =  "";
		var Amount = Dom.getElementsByClassName("Amt", "label")[index];
		Amount.textContent =  "";
		setCommissionHiddenValue(index, "commissionAmt" ,0);
	}
	setTaxDetailsDeposits();
}

function disablePackage(obj) {
	var thisRow  = getThisRow(obj);
	var numPayments = getNumOfPayments();
	var paymentIndex = numPayments;

	if (numPayments > 0) {
	    paymentIndex = Math.floor(((thisRow.rowIndex + 1) - paymentRowsUncloned) / paymentRows);
	}

	var packageObj = getIndexedFormElement(documentForm, "patientPackageId", paymentIndex);
	var applToIPObj = getIndexedFormElement(documentForm, "applicable_to_ip", paymentIndex);

	if(packageObj) {
	    if (obj.checked) {
			packageObj.disabled = false;
			applToIPObj.disabled = true;
	    } else {
			packageObj.value = "";
			packageObj.disabled = true;
			applToIPObj.disabled = false;
			// HMS-20319 : Allowing to Refund deposit Against a Package where package Deposits is zero
			var packageIdObj = getIndexedFormElement(documentForm, "mvPackageId", paymentIndex);
			packageIdObj.value = "";
	    }
	}
}

function onCheckApplicableToIP(obj){
	var thisRow = getThisRow(obj);

	var numPayments = getNumOfPayments();
	var paymentIndex = numPayments;

	if (numPayments > 0) {
	    paymentIndex = Math.floor(((thisRow.rowIndex + 1) - paymentRowsUncloned) / paymentRows);
	}

	var packageObj = getIndexedFormElement(documentForm, "patientPackageId", paymentIndex);
	var applicableToIpObj = getIndexedFormElement(documentForm, "applicableToIp", paymentIndex);
	var multiVisitPkgObj = getIndexedFormElement(documentForm, "multi_visit_package", paymentIndex);
	applicableToIpObj.value = (obj.checked)?"I":"";
    if( multiVisitPkgObj ) {
    	multiVisitPkgObj.checked = (obj.checked)?false:multiVisitPkgObj.checked;
    	multiVisitPkgObj.disabled = (obj.checked)?true:false;
    }
    if( packageObj ){
    	packageObj.value = (obj.checked)?"":packageObj.value;
    }

}

function assignPackageIdValue(obj) {
	var thisRow  = getThisRow(obj);
	var numPayments = getNumOfPayments();
	var paymentIndex = numPayments;

	if (numPayments > 0) {
	    paymentIndex = Math.floor(((thisRow.rowIndex + 1) - paymentRowsUncloned) / paymentRows);
	}

	var packageIdObj = getIndexedFormElement(documentForm, "mvPackageId", paymentIndex);
	if(packageIdObj != null)
		packageIdObj.value = $(obj).find(":selected").data("package-id");
}

function onDeletePayMode(obj) {
	var table = document.getElementById("paymentsTable");
	var numRows = table.rows.length;

	var numRows = numRows - paymentRowsUncloned;

	for (var i=1; i<=paymentRows; i++) {
		table.deleteRow(numRows-i);
	}
	enableDisableDeletePayMode();
	setTaxDetails();
}

function enableDisableDeletePayMode() {
	var numPayments = getNumOfPayments();
	var enable = numPayments > 1;
	document.getElementById("deletePayMode").disabled = !enable;
}

function setPayingAmountPaise(type, id) {

	var paymentObj = getIndexedFormElement(documentForm, "paymentType", id);
	setColor(paymentObj);

	var totPayingPaise = getPayingAmountPaise('patient');
	var totPayingRefundPaise = getPayingAmountPaise('refund');
	var totSponsorPayingPaise = getPayingAmountPaise('sponsor');
	var totTdsPaise = getPayingTDSAmountPaise();

	var sponsorDue = total_SpnsrAmtDuePaise - (totSponsorPayingPaise + totPayingRefundPaise + totTdsPaise);
	var patientDue = total_AmtDuePaise - (totPayingPaise + totPayingRefundPaise);

	sponsorDue = Math.abs(sponsorDue);
	patientDue = Math.abs(patientDue);

	if (type == 'pri_sponsor_receipt_advance' || type == 'pri_sponsor_receipt_settlement'
	    || type == 'sec_sponsor_receipt_advance' || type == 'sec_sponsor_receipt_settlement') {
		setFormIndexedValue("totPayingAmt", id, formatAmountPaise(sponsorDue));

	} else if (type == 'receipt_advance' || type == 'receipt_settlement') {
		setFormIndexedValue("totPayingAmt", id, formatAmountPaise(patientDue));

	}else if (type == 'refund') {
		setFormIndexedValue("totPayingAmt", id, formatAmountPaise(patientDue));
	}
}

/*
 * Returns an amount that is being paid/refunded. If refunded, the amount
 * is returned negative, thus, calculations can use the returned value as is,
 * regardless of refund/receipt.
 */

function getPayingAmountPaise(paymentType) {
	var payingAmtPaise = 0;
	var isReturns = (empty(isReturns)) ? false : isReturns;

	// paymentType is patient, sponsor, refund (patient)
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
			var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);

			var type = paymentObj.value;
			if ( (null != amtObj) && (amtObj.value != "") ) {
				if (paymentType == 'patient' && (type == 'receipt_advance' || type == 'receipt_settlement')) {
					payingAmtPaise = payingAmtPaise + getPaise(amtObj.value);

				}else if (paymentType == 'sponsor'
					&& (type == 'pri_sponsor_receipt_advance' || type == 'pri_sponsor_receipt_settlement'
						|| type == 'sec_sponsor_receipt_advance' || type == 'sec_sponsor_receipt_settlement')) {
					payingAmtPaise = payingAmtPaise + getPaise(amtObj.value);

				}else if (paymentType == 'refund' && (type == 'refund')) {
					payingAmtPaise = payingAmtPaise + getPaise(amtObj.value);
					payingAmtPaise = Math.abs(payingAmtPaise);
				}
			}
		}
	}

	if (paymentType == 'refund' && !isReturns)
		payingAmtPaise = (0-payingAmtPaise);
	return payingAmtPaise;
}

function getPayingTDSAmountPaise() {
	var tdsAmtPaise = 0;

	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
			var tdsObj = getIndexedFormElement(documentForm, "tdsAmt", i);

			var type = paymentObj.value;
			if ( (null != tdsObj) && (tdsObj.value != "") ) {
				if (type == 'pri_sponsor_receipt_advance' || type == 'pri_sponsor_receipt_settlement'
				    || type == 'sec_sponsor_receipt_advance' || type == 'sec_sponsor_receipt_settlement') {
					tdsAmtPaise = tdsAmtPaise + getPaise(tdsObj.value);
				}
			}
		}
	}
	return tdsAmtPaise;
}

function getCurrencyDetails(currObj) {
	var thisRow  = getThisRow(currObj);
	var currVal = currObj.value;

	var numPayments = getNumOfPayments();
	var paymentIndex = numPayments;

	if (numPayments > 0) {
		paymentIndex = ((thisRow.rowIndex) - paymentRowsUncloned) / paymentRows;
	}

	var paymentObj = getIndexedFormElement(documentForm, "paymentType", paymentIndex);
	var payObj = getIndexedFormElement(documentForm, "totPayingAmt", paymentIndex);
	var currAmtObj = getIndexedFormElement(documentForm, "currencyAmt", paymentIndex);
	var exchRateObj = getIndexedFormElement(documentForm, "exchangeRate", paymentIndex);
	var exchDtTimeObj = getIndexedFormElement(documentForm, "exchangeDateTime", paymentIndex);
	var exchangeRateDtlbl = thisRow.getElementsByTagName("label")[0];

	var type = paymentObj.value;
	setPayingAmountPaise(type, paymentIndex);

	if (currVal != '') {
		var currency = findInList(jForeignCurrencyList, "currency_id", currVal);
		if (!empty(currency)) {
			payObj.readOnly = true;
			exchRateObj.value = currency.conversion_rate;
			exchDtTimeObj.value = formatDate(new Date(currency.mod_time),'ddmmyyyy','-') + ' ' + formatTime(new Date(currency.mod_time));
			exchangeRateDtlbl.textContent =
				currency.conversion_rate +" (    "+ formatDate(new Date(currency.mod_time),'ddmmyyyy','-') + ' '
												  + formatTime(new Date(currency.mod_time)) +"   )";
		}else {
			payObj.readOnly = false;
			currAmtObj.value = '';
			exchRateObj.value = '';
			exchDtTimeObj.value = '';
			exchangeRateDtlbl.textContent = '';
		}

		if ((null != currAmtObj) && !currAmtObj.disabled && (currAmtObj.value == ""))
			if ((null != payObj)) payObj.value = '';

	}else {
		payObj.readOnly = false;
		payObj.value = '';
		currAmtObj.value = '';
		exchRateObj.value = '';
		exchDtTimeObj.value = '';
		exchangeRateDtlbl.textContent = '';
		setPayingAmountPaise(type, paymentIndex);
	}

	convertCurrency(currAmtObj);
}

function convertCurrency(currAmtObj) {

	var thisRow  = getThisRow(currAmtObj);
	var currAmtVal = currAmtObj.value;
	if (isNaN(currAmtVal)) {
		showMessage("js.laboratory.radiology.billpaymentcommon.enternumeric");
		currAmtObj.value = 0;
		currAmtObj.focus();
	}

	if (!validateAmount(currAmtObj, getString("js.laboratory.radiology.billpaymentcommon.currency.validamount"))) {
		currAmtObj.value = 0;
		currAmtObj.focus();
	}

	var numPayments = getNumOfPayments();
	var paymentIndex = numPayments;

	if (numPayments > 0) {
		paymentIndex = ((thisRow.rowIndex) - paymentRowsUncloned) / paymentRows;
	}

	var currObj = getIndexedFormElement(documentForm, "currencyId", paymentIndex);
	var currVal = currObj.value;

	var exchRateObj = getIndexedFormElement(documentForm, "exchangeRate", paymentIndex);
	var payObj = getIndexedFormElement(documentForm, "totPayingAmt", paymentIndex);

	if (trim(currAmtVal) != '' && currVal != '') {
		var currency = findInList(jForeignCurrencyList, "currency_id", currVal);
		if (!empty(currency)) {
			payObj.readOnly = true;
			payObj.value = formatAmountValue(currAmtVal * currency.conversion_rate);
		}else {
			payObj.readOnly = false;
			payObj.value = '';
			var paymentObj = getIndexedFormElement(documentForm, "paymentType", paymentIndex);
			var type = paymentObj.value;
			setPayingAmountPaise(type, paymentIndex);
		}
	}
}

/*
 * Run through all the date widgets and ensure they have properly formatted date.
 */
function validatePayDates() {
	var valid = true;

	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var pay = getIndexedFormElement(documentForm, "totPayingAmt", i);
			if ( (null != pay) && (pay.value != "") ) {
				if (allowReceiptBackDate != 'N'){
					var dateObj = getIndexedFormElement(documentForm, "payDate", i);
					var timeObj = getIndexedFormElement(documentForm, "payTime", i);
					if ( !dateObj ) continue;
					valid = valid && validateRequired(dateObj, getString("js.laboratory.radiology.billpaymentcommon.paymentdate.required"));
					valid = valid && validateRequired(timeObj, getString("js.laboratory.radiology.billpaymentcommon.paymenttime.required"));

					valid = valid && doValidateDateField(dateObj);
					valid = valid && validateTime(timeObj);
				}

				if (!valid) return false;
			}
		}
	}
	return true;
}

/*
 * Validate that a counter is selected if any payment is being made
 */
function validateCounter() {
	if (typeof(documentForm) != 'undefined' && documentForm.counterId.value == "") {
		showMessage("js.laboratory.radiology.billpaymentcommon.notauthorized.collect");
		return false;
	}
	return true;
}

function validateAllNumerics() {
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
			var tdsObj = getIndexedFormElement(documentForm, "tdsAmt", i);
			var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
			var currIdObj = getIndexedFormElement(documentForm, "currencyId", i);
			var currAmtObj = getIndexedFormElement(documentForm, "currencyAmt", i);

			var type = paymentObj.value;

			if (( null != currIdObj) && !currIdObj.disabled && (currIdObj.value != "") ) {
				if ( (null != currAmtObj) && !currAmtObj.disabled && (currAmtObj.value != "") ) {
					if (!validateAmount(currAmtObj, getString("js.laboratory.radiology.billpaymentcommon.currency.validamount")))
					return false;
				}
			}else {
				if ( (null != currAmtObj) && !currAmtObj.disabled && (currAmtObj.value != "") ) {
					currAmtObj.value = "";
				}
			}

			if ( (null != amtObj) && (amtObj.value != "") ) {
				if (!validateAmount(amtObj, getString("js.laboratory.radiology.billpaymentcommon.pay.validamount")))
				return false;
			}

			if ( (null != tdsObj) && (tdsObj.value != "") ) {
				if (!validateAmount(tdsObj, getString("js.laboratory.radiology.billpaymentcommon.tds.validamount")))
				return false;
			}
		}
	}
	return true;
}

function addMandatoryIndicators() {
	var numPayments = getNumOfPayments();
	for (var i=0; i<numPayments; i++) {

		var cardtype = getIndexedFormElement(documentForm, "cardTypeId", i);
		if (!cardtype.disabled) {
			appendStarSpan(cardtype.parentNode);
		} else {
			removePreviousSpans(cardtype.parentNode);
		}

		var bank = getIndexedFormElement(documentForm, "bankName", i);
		if (!bank.disabled ) {
			appendStarSpan(bank.parentNode);
		} else {
			removePreviousSpans(bank.parentNode);
		}

		var ref = getIndexedFormElement(documentForm, "refNumber", i);
		if (!ref.disabled ) {
			appendStarSpan(ref.parentNode);
		} else {
			removePreviousSpans(ref.parentNode);
		}

		var btch = getIndexedFormElement(documentForm, "bankBatchNo", i);
		if (!btch.disabled ) {
			appendStarSpan(btch.parentNode);
		} else {
			removePreviousSpans(btch.parentNode);
		}

		var crdAuth = getIndexedFormElement(documentForm, "cardAuthCode", i);
		if (!crdAuth.disabled) {
			appendStarSpan(crdAuth.parentNode);
		} else {
			removePreviousSpans(crdAuth.parentNode);
		}

		var crdHolder = getIndexedFormElement(documentForm, "cardHolderName", i);
		if (!crdHolder.disabled ) {
			appendStarSpan(crdHolder.parentNode);
		} else {
			removePreviousSpans(crdHolder.parentNode);
		}


		var crdNumber = getIndexedFormElement(documentForm, "cardNumber", i);
		if (!crdNumber.disabled ) {
			appendStarSpan(crdNumber.parentNode);
		} else {
			removePreviousSpans(crdNumber.parentNode);
		}

		var crdExpDt = getIndexedFormElement(documentForm, "cardExpDate", i);
		if (!crdExpDt.disabled) {
			appendStarSpan(crdExpDt.parentNode);
		} else {
			removePreviousSpans(crdExpDt.parentNode);
		}
		var toptp = getIndexedFormElement(documentForm, "totp", i);
		if (!toptp.disabled) {
			appendStarSpan(toptp.parentNode);
		} else {
			removePreviousSpans(toptp.parentNode);
		}
		var mobNumber = getIndexedFormElement(documentForm, "mobNumber", i);
		if (!mobNumber.disabled) {
			appendStarSpan(mobNumber.parentNode);
		} else {
			removePreviousSpans(mobNumber.parentNode);
		}
	}

	return true;
}

/*
 * Validate that if a payment is being made via non-cash instruments,
 * the required values are provided as per payment mode master preference.
 */
function validatePaymentTagFields() {
	var numPayments = getNumOfPayments();
	var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund') + getPayingAmountPaise('sponsor');

	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {

			var totPayAmt = getIndexedFormElement(documentForm, "totPayingAmt", i);

			if (payingAmt != 0) {

				if (getPaise(totPayAmt.value) != 0) {

					var payType = getIndexedFormElement(documentForm, "paymentType", i);
					if (!payType.disabled && payType.value == "") {
						showMessage("js.laboratory.radiology.billpaymentcommon.paymenttype.required");
						payType.focus();
						return false;
					}

					var payMode = getIndexedFormElement(documentForm, "paymentModeId", i);
					if (!payMode.disabled && payMode.value == "") {
						showMessage("js.laboratory.radiology.billpaymentcommon.mode.required");
						payMode.focus();
						return false;
					}
				}

				var mobNumber = getIndexedFormElement(documentForm, "mobNumber", i);
				if (!mobNumber.disabled && trim(mobNumber.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.mobnumber.required");
					mobNumber.focus();
					return false;
				}

				var totp = getIndexedFormElement(documentForm, "totp", i);
				if (!totp.disabled && trim(totp.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.totp.required");
					totp.focus();
					return false;
				}

				var cardtype = getIndexedFormElement(documentForm, "cardTypeId", i);
				if (!cardtype.disabled && trim(cardtype.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.cardtype.required");
					cardtype.focus();
					return false;
				}

				var bank = getIndexedFormElement(documentForm, "bankName", i);
				if (!bank.disabled && trim(bank.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.bankname.required");
					bank.focus();
					return false;
				}
				if (bank.value.length > 50) {
					showMessage("js.laboratory.radiology.billpaymentcommon.entershortname.banknamefield");
					bank.focus();
					return false;
				}

				var ref = getIndexedFormElement(documentForm, "refNumber", i);
				if (!ref.disabled && trim(ref.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.bankrefnumber.required");
					ref.focus();
					return false;
				}

				if (ref.value.length >50) {
					 showMessage("js.laboratory.radiology.billpaymentcommon.entershortreferencenumber.refnumfield");
					 ref.focus();
  	                 return false;
				}

				var btch = getIndexedFormElement(documentForm, "bankBatchNo", i);
				if (!btch.disabled && trim(btch.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.bankbatchnumber.required");
					btch.focus();
					return false;
				}

				if (btch.value.length >100) {
					 showMessage("js.laboratory.radiology.billpaymentcommon.batchnumberexceedssize100");
					 btch.focus();
  	                 return false;
				}

				var crdAuth = getIndexedFormElement(documentForm, "cardAuthCode", i);
				if (!crdAuth.disabled && trim(crdAuth.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.cardauthorizationcode.required");
					crdAuth.focus();
					return false;
				}

				if (crdAuth.value.length >100) {
					 showMessage("js.laboratory.radiology.billpaymentcommon.cardauthorizationcodeexceedssize100");
					 crdAuth.focus();
  	                 return false;
				}

				var crdHolder = getIndexedFormElement(documentForm, "cardHolderName", i);
				if (!crdHolder.disabled && trim(crdHolder.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.cardholdername.required");
					crdHolder.focus();
					return false;
				}

				if (crdHolder.value.length >100) {
					 showMessage("js.laboratory.radiology.billpaymentcommon.cardholdernameexceedssize300");
					 crdHolder.focus();
  	                 return false;
				}

				var crdNumber = getIndexedFormElement(documentForm, "cardNumber", i);
				if (!crdNumber.disabled && trim(crdNumber.value) == "") {
					showMessage("js.laboratory.radiology.billpaymentcommon.cardnumber.required");
					crdNumber.focus();
					return false;
				}

				if ((null != crdNumber) && !crdNumber.disabled && trim(crdNumber.value) != "" && no_of_credit_debit_card_digits != 0 && crdNumber.value.length != no_of_credit_debit_card_digits) {
					 alert("Card number should be "+no_of_credit_debit_card_digits+" digits");
					 crdNumber.value="";
					 crdNumber.focus();
	                 return false;
				}

				if (crdNumber.value.length >100) {
					 showMessage("js.laboratory.radiology.billpaymentcommon.cardnumberexceedssize150");
					 crdNumber.focus();
  	                 return false;
				}

				var multiVisitSelectBox = getIndexedFormElement(documentForm, "multi_visit_package", i);
				var packageIdObj = getIndexedFormElement(documentForm, "patientPackageId", i);
				if(multiVisitSelectBox && packageIdObj) {
					if(multiVisitSelectBox.checked) {
						if(empty(packageIdObj.value)) {
							showMessage("js.laboratory.radiology.billpaymentcommon.pleaseselectapackage");
							packageIdObj.focus();
							return false;
						}
					}
				}

				var crdExpDt = getIndexedFormElement(documentForm, "cardExpDate", i);
				if (!crdExpDt.disabled) {
					if (trim(crdExpDt.value) == "") {
						showMessage("js.laboratory.radiology.billpaymentcommon.cardexpirydate.required");
						crdExpDt.focus();
						return false;
					}

					var errorStr = validateCardDateStr(crdExpDt.value, "future");
					if (errorStr != null) {
						alert(errorStr);
						crdExpDt.focus();
						return false;
					}
				}

			}
		}
	}
	return true;
}
/*
 * Do paytm transaction
 */
function doPaytmTransactions(){
	if(!validateModeForRefund())
		return false;
	var numPayments = getNumOfPayments();
	var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund') + getPayingAmountPaise('sponsor');

	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {

			var totPayAmt = getIndexedFormElement(documentForm, "totPayingAmt", i);

			if (payingAmt != 0) {
				var paymentMode = getIndexedFormElement(documentForm, "paymentModeId", i);

				if(paymentMode.value==-2){ // Paytm mode
					var pmtObj = getIndexedFormElement(documentForm, "paymentType", i);
					if(pmtObj.value == 'refund'){
						alert('Amount cannot be Refund by Paytm');
						return false;
					}

					var mobNumber = getIndexedFormElement(documentForm, "mobNumber", i); //null check was done in validatePaymentTagFields

					var totp = getIndexedFormElement(documentForm, "totp", i);  //null check was done in validatePaymentTagFields

					var mobNumberValue=mobNumber.value;
					var otpValue=totp.value;
					var totalAmtValue=totPayAmt.value;
					if(totalAmtValue == 0){
						continue;
					}
					var ajaxobjpaytm = newXMLHttpRequest();
					var isTranSuccess=false;
					var transactionId = null;
					var url = cpath + "/billing/BillAction.do?_method=requestMoneyByPaytm&mobileNumber="+
						encodeURIComponent(mobNumberValue)+"&otp="+encodeURIComponent(otpValue)+"&totalAmt="+encodeURIComponent(totalAmtValue)+
						"&billNumber="+encodeURIComponent(billNumber);
					ajaxobjpaytm.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
					    	try{
						    	var json = JSON.parse(this.responseText);
						    	if(json['result'] == false){
						    		if(json['timed_out'] == true){
						    			checkTransactionStatus(json['transaction_id']);
						    		}
						    		else{
						    			alert(json['message']);
						    			eventTracking('Paytm Integration','Save button is clicked with paytm payment mode and transaction fails','Reason: '+json['message'] );
						    		}
						    	}
						    	else{
						    		transactionId = json['reference_no'];
						    		isTranSuccess=true;
						    		eventTracking('Paytm Integration','Save button is clicked with payTM as payment mode and payment is success','' );
						    	}
					    	}
					    	catch(e){
					    	}
						}
					};

					function checkTransactionStatus(transactionId){
						var ajaxObj = newXMLHttpRequest();
						var url = cpath + "/billing/BillAction.do?_method=checkTransactionStatusPaytm&transaction_id="+encodeURIComponent(transactionId);
						ajaxObj.onreadystatechange = function() {
							if (this.readyState == 4 && this.status == 200) {
						    	try{
							    	var json = JSON.parse(this.responseText);
							    	if(json['result'] == false){
							    		alert('Transaction Failed. Please re-try.');
							    	}
							    	else{
							    		transactionId = json['reference_no'];
							    		isTranSuccess=true;
							    	}
						    	}
						    	catch(e){
						    	}
							}
						};
						ajaxObj.open("POST", url.toString(), false);
						ajaxObj.send(null);
					}
					ajaxobjpaytm.open("POST", url.toString(), false);
					ajaxobjpaytm.send(null);
					if(isTranSuccess != true){
						return false;
					}
					//set transactionId  to form-element
					var refNumber = getIndexedFormElement(documentForm, "refNumber", i);
					refNumber.value = transactionId;
					refNumber.disabled = false;
				}
				//if loyality mode or points redemption from apollo_one
				if(paymentMode.value==-3 || paymentMode.value==-5){
					var pmtObj = getIndexedFormElement(documentForm, "paymentType", i);
					if(pmtObj.value == 'refund'){
						alert('Amount cannot be Refund by Loyalty Card');
						return false;
					}

					var mobNumber = getIndexedFormElement(documentForm, "mobNumber", i); //null check was done in validatePaymentTagFields

					var totp = getIndexedFormElement(documentForm, "totp", i);  //null check was done in validatePaymentTagFields
					var refereceNumber = getIndexedFormElement(documentForm, "refNumber", i);
					var paymentModeId = getIndexedFormElement(documentForm, "paymentModeId", i);
					var paymentMode = paymentModeId.value;
					var mobNumberValue=mobNumber.value;
					var otpValue=totp.value;
					var refereceNumberValue=refereceNumber.value;
					var totalAmtValue=totPayAmt.value;
					if(totalAmtValue == 0){
						continue;
					}
					var ajaxobjloyaltycard = newXMLHttpRequest();
					var isTranSuccess=false;
					var transactionId = null;
					var url = cpath + "/billing/BillAction.do?_method=requestMoneyByLoyaltyCard&mobileNumber="+
						encodeURIComponent(mobNumberValue)+"&otp="+encodeURIComponent(otpValue)+"&totalAmt="+encodeURIComponent(totalAmtValue)+
						"&billNumber="+encodeURIComponent(billNumber)+"&paymentModeId="+paymentMode+"&referenceNumber="+refereceNumberValue;
					ajaxobjloyaltycard.onreadystatechange = function() {
						if (this.readyState == 4 && this.status == 200) {
					    	try{
						    	var json = JSON.parse(this.responseText);
						    	if(json['result'] == false){
						    			alert(json['message']);
						    	}
						    	else{
						    		isTranSuccess=true;
						    	}
					    	}
					    	catch(e){
					    	}
						}
					};

					ajaxobjloyaltycard.open("POST", url.toString(), false);
					ajaxobjloyaltycard.send(null);
					if(isTranSuccess != true){
						return false;
					}
				}
			}
		}
	}
	return true;
}

function validateDepositSetOff(depositSetOffObj, origDepositSetOffObj) {

	clearBillPaymentDetails();

	var isReturns = (empty(isReturns)) ? false : isReturns;

	var valid = true;

	if (typeof(depositSetOffObj) != 'undefined' && depositSetOffObj != null) {

		if (!validateAmount(depositSetOffObj, getString("js.laboratory.radiology.billpaymentcommon.depositsetoff.avalidamount"))) {
			depositSetOffObj.value = 0;
			valid = false;
		}

		var depositSetOffPaise = getPaise(depositSetOffObj.value);

		if (depositSetOffPaise >= 0) {
			if (!isReturns) {
				// Billing need to reset back to original deposit set off used.
				if (typeof(origDepositSetOffObj) != 'undefined' && origDepositSetOffObj != null) {

					if (depositSetOffPaise > total_DepositAmtAvailablePaise) {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.depositsetoff.lessthanavailabledeposits");
						msg +=formatAmountPaise(total_DepositAmtAvailablePaise);
						msg +=getString( "js.laboratory.radiology.billpaymentcommon.resettingtoexisitingdeposits");
						alert(msg);
						depositSetOffObj.value = origDepositSetOffObj.value;
						depositSetOffObj.focus();
						valid = false;
					}else {
						// Bill deposits existing, so on deposit amount change resetTotals() is called.
						valid = true;
					}

				}else {
					// Pharmacy sales reset to zero.
					if (depositSetOffPaise > total_DepositAmtAvailablePaise) {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.depositsetoffamount.notexceedmaxamount");
						msg +=formatAmountPaise(total_DepositAmtAvailablePaise);
						msg +=getString("js.laboratory.radiology.billpaymentcommon.resettingtozero");
						alert(msg);
						depositSetOffObj.value = 0;
						depositSetOffObj.focus();
						valid = false;
					}else {
						valid = true;
					}

					// Existing sale deposits -- calculate less deposits.
					total_LessDeposits = getPaise(depositSetOffObj.value);
					resetPayments();
				}
			}else if (isReturns) {
				// Pharmacy returns reset to zero
				if (depositSetOffPaise > total_DepositAmtAvailablePaise) {
					showMessage("js.laboratory.radiology.billpaymentcommon.depositsetoffamount.notexceedbilldepositamount");
					depositSetOffObj.value = 0;
					depositSetOffObj.focus();
					valid = false;

				}else if (total_DepositAmtAvailablePaise > 0) {
					valid = true;
				}

				// Existing sale deposits -- calculate less deposits.
				total_LessDeposits = getPaise(depositSetOffObj.value);
				resetPayments();
			}
		}
	}

	return valid;
}

function calculatePointsRedeemedAmt(pointsRedeemedObj, pointsRedeemedAmountObj) {
	if (pointsRedeemedObj != null) {
		var newPointsRedeemedAmt = pointsRedeemedObj.value * points_redemption_rate;
		pointsRedeemedAmountObj.value = formatAmountValue(newPointsRedeemedAmt);
	}
}

function validateRewardPoints(pointsRedeemedObj, pointsRedeemedAmountObj, origPointsRedeemedObj) {
	clearBillPaymentDetails();
	return validateRewardPointsRedeemed(pointsRedeemedObj, pointsRedeemedAmountObj, origPointsRedeemedObj);
}

function validateRewardPointsRedeemed(pointsRedeemedObj, pointsRedeemedAmountObj, origPointsRedeemedObj) {

	var isReturns = (empty(isReturns)) ? false : isReturns;

	if (isReturns) return true;

	var valid = true;

	if (typeof(pointsRedeemedObj) != 'undefined' && pointsRedeemedObj != null) {

		if (!validateInteger(pointsRedeemedObj, getString("js.laboratory.radiology.billpaymentcommon.pointsredeemed.avalidnumber"))) {
			pointsRedeemedObj.value = 0;
			calculatePointsRedeemedAmt(pointsRedeemedObj, pointsRedeemedAmountObj);
			valid = false;
		}

		calculatePointsRedeemedAmt(pointsRedeemedObj, pointsRedeemedAmountObj);

		var pointsRedeemed = pointsRedeemedObj.value;
		var pointsRedeemedAmtPaise = getPaise(pointsRedeemedAmountObj.value);

		if (pointsRedeemed >= 0) {
			if (!isReturns) {
				// Billing need to reset back to original points redeemed.??
				if (typeof(origPointsRedeemedObj) != 'undefined' && origPointsRedeemedObj != null) {

					if (pointsRedeemed > total_RewardPointsAvailable) {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.pointsredeemed.lessthanmaxpoints");
						msg +=formatAmountValue(total_RewardPointsAvailable);
						msg +=getString("js.laboratory.radiology.billpaymentcommon.resettingtozero");
						alert(msg);
						pointsRedeemedObj.value = 0;
						pointsRedeemedObj.focus();
						valid = false;

					}else if (pointsRedeemedAmtPaise > total_RewardPointsAmountAvailable) {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.pointsredeemedamount.notexceedeligibleamount");
						 msg+=formatAmountPaise(total_RewardPointsAmountAvailable);
						 msg +=getString("js.laboratory.radiology.billpaymentcommon.resettingtozero");
						alert(msg);
						pointsRedeemedObj.value = 0;
						pointsRedeemedObj.focus();
						valid = false;

					}else {
						// Bill Reward points existing, so on reward points change resetTotals() is called.
						valid = true;
					}

					calculatePointsRedeemedAmt(pointsRedeemedObj, pointsRedeemedAmountObj);

				}else {
					// Pharmacy sales reset to zero.
					if (pointsRedeemed > total_RewardPointsAvailable) {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.pointsredeemed.notexceedmaxpoints");
						msg +=formatAmountValue(total_RewardPointsAvailable);
						msg +=getString("js.laboratory.radiology.billpaymentcommon.resettingtozero");
						alert(msg);
						pointsRedeemedObj.value = 0;
						pointsRedeemedObj.focus();
						valid = false;

					}else if (pointsRedeemedAmtPaise > total_RewardPointsAmountAvailable) {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.pointsredeemedamount.notexceedeligibleamount");
						msg +=formatAmountPaise(total_RewardPointsAmountAvailable);
						msg +=getString("js.laboratory.radiology.billpaymentcommon.resettingtozero");
						alert(msg);
						pointsRedeemedObj.value = 0;
						pointsRedeemedObj.focus();
						valid = false;

					}else {
						valid = true;
					}

					calculatePointsRedeemedAmt(pointsRedeemedObj, pointsRedeemedAmountObj);

					// Existing reward points and reward points amount
					total_LessRewardPoints = pointsRedeemedObj.value;
					total_LessRewardPointsAmount = getPaise(pointsRedeemedAmountObj.value);
					resetPayments();
				}
			}
		}
	}

	return valid;
}

function validateModeForRefund() {
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i = 0; i < numPayments; i++) {

				var paymentMode = getIndexedFormElement(documentForm,
						"paymentModeId", i);
				var paymentModeName = $(paymentMode).children(':selected').text();
				if (paymentMode.value == -2 || paymentMode.value == -3 || paymentMode.value == -5 || paymentMode.value == -9) { // Paytm mode or Loyalty Card or apollo one
					var pmtObj = getIndexedFormElement(documentForm,
							"paymentType", i);
					if (pmtObj.value == 'refund') {
						alert('Amount cannot be Refund by ' + paymentModeName);
						return false;
					}
				}
		}
	}
	return true;
}

function validatePaymentAmount() {

	var totalAmt =  Math.abs(getTotalAmount());

	// TODO: Need to use same deposit field in all payment screens
	var depositSetOff = 0;
	if (documentForm.depositsetoff)
		depositSetOff = getPaise(documentForm.depositsetoff.value);
	else if(documentForm.depositSetOff)
		depositSetOff = getPaise(documentForm.depositSetOff.value);

	var rewardPointsAmount = 0;
	if (documentForm.rewardPointsRedeemedAmount)
		rewardPointsAmount = getPaise(documentForm.rewardPointsRedeemedAmount.value);

	var payingAmt = getPayingAmountPaise('patient') + getPayingAmountPaise('refund')
					+ getPayingAmountPaise('sponsor') + depositSetOff + rewardPointsAmount;

	var dueAmount = getTotalAmount() - payingAmt;

	payingAmt = Math.abs(payingAmt);

	if (totalAmt >= 0 && (documentForm.billType != null || documentForm.close != null)) {
		var billtype = documentForm.billType.value;

		if (billtype == "BN" || billtype == "BN-I" || billtype == "P") {

			if (payingAmt > totalAmt) {
				var msg=getString("js.laboratory.radiology.billpaymentcommon.totalpaymentamount.morethanbillamount")
				msg+="\n";
				msg+=getString("js.laboratory.radiology.billpaymentcommon.pleaserefundtheexcessamount");
				alert(msg);
				return false;
			}

			if (payingAmt != totalAmt) {
				var msg=getString("js.laboratory.radiology.billpaymentcommon.totalbillamount.patientpaidamountsdonotmatch");
				msg+="\n";
				msg+=getString("js.laboratory.radiology.billpaymentcommon.pleasepaythebillamount");
				alert(msg);
				return false;
			}

			if (totalAmt != 0 && (payingAmt == totalAmt)
				&& (Math.abs(getPayingAmountPaise('refund')) == totalAmt)
				&& (getPayingAmountPaise('patient') == 0)
				&& (getPayingAmountPaise('sponsor') == 0)
				&& (!isReturns) ) {
				var msg=getString("js.laboratory.radiology.billpaymentcommon.totalbillamount.patientpaidamountsdoesnotmatch");
				msg+="\n";
				msg+=getString("js.laboratory.radiology.billpaymentcommon.pleasepaythebillamount");
				alert(msg);
				return false;
			}

		}else if (documentForm.close != null && documentForm.close.checked) {
			if (payingAmt != totalAmt) {
				if(!validateSettlement()) {
					var msg=getString("js.laboratory.radiology.billpaymentcommon.totalbillamount.patientpaidamountsdonotmatch");
					msg+="\n";
					msg+=getString("js.laboratory.radiology.billpaymentcommon.settleorrefundtheamounttoclosetheaccount");
					alert(msg);
					if (documentForm.close != null)
						documentForm.close.checked = false;

					return false;
				}else if (dueAmount > 0) {
					if (writeOffAmountRights == 'A') {
						var ok = confirm("Warning: Total bill amount and patient paid amounts do not match.\n" +
									"The balance amount will be considered as Write-Off. Do you want to proceed?");
						if (!ok) {
							if (documentForm.close != null)
								documentForm.close.checked = false;
							return false;

						}else {
							var billRemarksObj = getIndexedFormElement(documentForm, "billRemarks", 0);
							var oldRemarksObj = getIndexedFormElement(documentForm, "oldRemarks", 0);

							if ( (null != billRemarksObj) && (
								('' == trimAll(billRemarksObj.value))||
								(trimAll(oldRemarksObj.value) == trimAll(billRemarksObj.value)))) {
								showMessage("js.laboratory.radiology.billpaymentcommon.entervalidreasonforwriteoff");
								billRemarksObj.focus();
								return false;
							}
						}
					}else {
						var msg=getString("js.laboratory.radiology.billpaymentcommon.totalbillamount.patientpaidamountsdonotmatch");
						msg+="\n";
						msg+=getString("js.laboratory.radiology.billpaymentcommon.notauthorized.writeoffthebalanceamount");
						alert(msg);
						if (documentForm.close != null)
							documentForm.close.checked = false;
						return false;
					}
				}else if (dueAmount < 0) {
					var msg=getString("js.laboratory.radiology.billpaymentcommon.totalbillamount.patientpaidamountsdoesnotmatch");
					msg+="\n";
					msg+=getString("js.laboratory.radiology.billpaymentcommon.cannotclosethebill");
					alert(msg);
					return false;
				}
			}
		}
	}

	if (documentForm.billType != null || documentForm.close != null) {
		var billtype = documentForm.billType.value;

		if (billtype == "BN" || billtype == "BN-I" || billtype == "P") {
			if (payingAmt == totalAmt) {
				if (documentForm.close != null)
					documentForm.close.checked = true;
			}
		}
	}

	return true;
}

function validateSettlement() {
	var numPayments = getNumOfPayments();

	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var payType = getIndexedFormElement(documentForm, "paymentType", i);
			if (payType.value == "receipt_settlement" || payType.value == "refund") {
				return true;
			}
		}
	}
	return false;
}

function clearBillPaymentDetails() {
	var numPayments = getNumOfPayments();

	if (numPayments > 1) {
		for (var i=0; i<numPayments-1; i++) {
			//onDeletePayMode(document.getElementById('deletePayMode'));
		}
	}

	for (var i=0; i<numPayments; i++) {
		clearPaymentFields(i);
	}
}

function clearPaymentFields(i) {
	var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
	var modeObj = getIndexedFormElement(documentForm, "paymentModeId", i);
	if (modeObj.value == '-9') {
		return;
	}
	var totPayAmt = getIndexedFormElement(documentForm, "totPayingAmt", i);
	var mobNumberObj = getIndexedFormElement(documentForm, "mobNumber", i);
	var totpObj = getIndexedFormElement(documentForm, "totp", i);
	totPayAmt.removeAttribute('readOnly');
	if (paymentObj != null) paymentObj.selectedIndex = 0;
	if (modeObj != null) setSelectedIndex(modeObj, "-1");
	if (modeObj != null) enableBankDetails(modeObj);
	if (totPayAmt != null) totPayAmt.value = "";
	if (mobNumberObj != null) mobNumberObj.value = "";
	if (totpObj != null) totpObj.value = "";

	var userRemObj = getIndexedFormElement(documentForm, "allUserRemarks", i);
	var payRemObj = getIndexedFormElement(documentForm, "paymentRemarks", i);

	if (userRemObj != null) userRemObj.value = "";
	if (payRemObj != null) payRemObj.value = "";

	var bankObj = getIndexedFormElement(documentForm, "bankName", i);
	var cardObj = getIndexedFormElement(documentForm, "cardTypeId", i);
	var refObj = getIndexedFormElement(documentForm, "refNumber", i);

	var bankBatchObj = getIndexedFormElement(documentForm, "bankBatchNo", i);
	var cardAuthObj = getIndexedFormElement(documentForm, "cardAuthCode", i);
	var cardHolderObj = getIndexedFormElement(documentForm, "cardHolderName", i);

	var cardNumberObj = getIndexedFormElement(documentForm, "cardNumber", i);
	var cardExpDtObj = getIndexedFormElement(documentForm, "cardExpDate", i);

	var commisionPerObj =  Dom.getElementsByClassName("commissionPer", "label")[i];
	var commisionAmtObj =  Dom.getElementsByClassName("commissionAmt", "label")[i];

	var percentage = Dom.getElementsByClassName("Per", "label")[i];
	var amount = Dom.getElementsByClassName("Amt", "label")[i];

	if (commisionPerObj != null) commisionPerObj.textContent = "";
	if (commisionAmtObj != null) commisionAmtObj.textContent = "";
	percentage.textContent =  "";
	amount.textContent =  "";

	if (cardObj != null) setSelectedIndex(cardObj, "");

	var currencyObj = getIndexedFormElement(documentForm, "currencyId", i);

	if (currencyObj != null && currencyObj.value != "") {
		setSelectedIndex(currencyObj, "");
		getCurrencyDetails(currencyObj);
	}

	var packageObj = getIndexedFormElement(documentForm, "patientPackageId", i);
	var multiVisitCheckBox = getIndexedFormElement(documentForm, "multi_visit_package", i);

	if(multiVisitCheckBox && multiVisitCheckBox.checked) {
		multiVisitCheckBox.checked = false;
	}

	if(packageObj && !empty(packageObj.value)) {
		setSelectedIndex(packageObj, "");
	}
}

function validateMVRefund(patPackDetailsJson) {
	if(patPackDetailsJson != null && patPackDetailsJson.length > 0) {
		var numPayments = getNumOfPayments();

		if (numPayments > 0) {
			for (var i=0; i<numPayments; i++) {
				var packageIdObj = getIndexedFormElement(documentForm, "patientPackageId", i);
				var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
				if(paymentObj.value == 'refund') {
					if(packageIdObj && !empty(packageIdObj.value)) {
						var list = findInList(patPackDetailsJson,"pat_package_id",packageIdObj.value);
						if(list != null && list.length > 0) {
							for(var j=0;j<list.length;j++) {
								if(list[j].status == 'P') {
									return false;
								}
							}
						}
					}
				}
			}
		}
	}
	return true;
}

function validateMVDeposit(patPackDetailsJson) {
	if(patPackDetailsJson != null && patPackDetailsJson.length > 0) {
		var numPayments = getNumOfPayments();

		if (numPayments > 0) {
			for (var i=0; i<numPayments; i++) {
				var packageIdObj = getIndexedFormElement(documentForm, "patientPackageId", i);
				var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
				if(paymentObj.value == 'receipt_settlement') {
					if(packageIdObj && !empty(packageIdObj.value)) {
						var list = findInList(patPackDetailsJson,"pat_package_id",packageIdObj.value);
						if(list != null && list.length > 0) {
							for(var j=0;j<list.length;j++) {
								if(list[j].status == 'C' || list[j].status == 'X') {
									return false;
								}
							}
						}
					}
				}
			}
		}
	}
	return true;
}

function getMultiVisitPackageBalanceAmt(patPackDetailsJson) {
	var balnaceAmt = 0;
	if(patPackDetailsJson != null && patPackDetailsJson.length > 0) {
		var numPayments = getNumOfPayments();
		if(numPayments > 0) {
			for (var i=0; i<numPayments; i++) {
				var packageIdObj = getIndexedFormElement(documentForm, "pat_package_id", i);
				var paymentObj = getIndexedFormElement(documentForm, "paymentType", i);
				if(paymentObj.value == 'refund') {
					if(packageIdObj && !empty(packageIdObj.value)) {
						var list = findInList(patPackDetailsJson,"pat_package_id",packageIdObj.value);
						if(list != null && list.length > 0) {
								for(var j=0;j<list.length;j++) {
									if(list[j].status == 'C' || list[j].status == 'X') {
										balnaceAmt = getPaise(balnaceAmt)+getPaise(list[j].balance);
									}
								}
							}
					}
				}
			}
		}
	}
	return balnaceAmt;
}

function setCommissionHiddenValue(index, name, value) {
	var el = getIndexedFormElement(documentForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function checkTransactionLimitValue(){

	var totalamtObj = {};
	var paymentmode =[];
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var paymentObj = getIndexedFormElement(documentForm, "paymentModeId", i);
			var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", i);
			var payType = getIndexedFormElement(documentForm, "paymentType", i);
			if(totalamtObj[paymentObj.options[paymentObj.selectedIndex].text] == null){
				totalamtObj[paymentObj.options[paymentObj.selectedIndex].text]= amtObj.value;
				paymentmode[i]=paymentObj.options[paymentObj.selectedIndex].text;
			}else{
				totalamtObj[paymentObj.options[paymentObj.selectedIndex].text] = parseFloat(amtObj.value)+parseFloat(totalamtObj[paymentObj.options[paymentObj.selectedIndex].text]);
			}
		}
	}

	if(jPaymentModes != undefined){
		for (i = 0; i < paymentmode.length; i++) {
			var totalPayModeAmt = totalamtObj[paymentmode[i]];
			var visitId = '';
			if(document.mainform != undefined && document.mainform.visitId != undefined){
				visitId = document.mainform.visitId.value;
			}else if (document.retailCreditForm != undefined && document.retailCreditForm.customerId != undefined){
				visitId = document.retailCreditForm.customerId.value;
			}
			var paymentModeDetail = findInList(jPaymentModes, "payment_mode", paymentmode[i]);
			if(paymentModeDetail != null ){
				if(paymentModeDetail.transaction_limit != null && paymentModeDetail.transaction_limit != 0){
					if( visitId != null && visitId != undefined && visitId != ""){
						var url = cpath+"/billing/BillAction.do?_method=getpaymentModeAmount&visit_id="+visitId+"&mode_id="+paymentModeDetail.mode_id;
						if(window.XMLHttpRequest) {
							req = new XMLHttpRequest();
						}
						else if(window.ActiveXObject) {
							req = new ActiveXObject("MSXML2.XMLHTTP");
						}
						req.open("GET", url.toString(), false);
						req.setRequestHeader("Content-Type", "text/plain");
						req.send(null);
							if (req.readyState == 4 && req.status == 200) {
								if(req.responseText !="" && req.responseText != null){
									if (payType.value == "refund"){
										totalPayModeAmt = parseFloat(req.responseText) - parseFloat(totalPayModeAmt);
									}
									else{
										totalPayModeAmt = parseFloat(totalPayModeAmt) + parseFloat(req.responseText);
									}
								}
							}
					}
					if(totalPayModeAmt > paymentModeDetail.transaction_limit && paymentModeDetail.allow_payments_more_than_transaction_limit =='W'){
						if ( !confirm("Pay amount is more than " +paymentmode[i] +" mode transaction limit "+paymentModeDetail.transaction_limit+". "+ "Do you want proceed?")) {
								return false;
						}
					}else if(totalPayModeAmt > paymentModeDetail.transaction_limit && paymentModeDetail.allow_payments_more_than_transaction_limit =='B'){
						alert("You have exceeded the transaction limit "+paymentModeDetail.transaction_limit+" of "+paymentmode[i] +", please try collecting through other payment modes");
						return false;
					}
				}
			}
		}
	}

	return true;
}


function getLoyaltyPoints(mobNumber, paymentIndex){
	if(mobNumber=="" || mobNumber.length<10){
		alert("Please enter valid number");
		return;
	}
	if( mobNumber.substring(0, 3)=='+91')
		mobNumber=mobNumber.substring(3);
	var ajaxobj = newXMLHttpRequest();
	var paymentModeId = 0;
	var paymentModeElement =  document.getElementById('paymentModeId'+paymentIndex);
	if (typeof(paymentModeElement) != 'undefined' && paymentModeElement != null){
		paymentModeId = paymentModeElement.value;
	}
	// if payment mode is other than -3 or -5.
	if( paymentModeId!=-3 && paymentModeId!=-5 ){
		alert("Please select other payment mode.");
		return;
	}
	showLoader();
	var url = cpath + "//billing/BillAction.do?_method=fetchPointsInLoyaltyCard&mobileNumber="+mobNumber+"&paymentModeId="+paymentModeId;
	ajaxobj.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			hideLoader();
    	try{
	    	var json = JSON.parse(this.responseText);
	    	if(json['result'] == true){
	    		document.getElementById("loyaltyPointAmount").innerHTML = json['wallet_balance'];
	    		document.getElementById("loyaltyPointAmount").setAttribute("data-amount",json['wallet_balance']);
	    		document.getElementById("sendOTPBtn").disabled = false;
	    		document.getElementById("getPointsBtn").disabled = true;
	    	}
	    	else{
	    		alert(json['message']);
	    	}
    	}
    	catch(e){
    	}
	}
	}
	ajaxobj.open("GET", url.toString(), true);
	ajaxobj.send(null);
}

function getLoyaltyOTP(mobNumber,points,paymentIndex){
	if(mobNumber=="" || mobNumber.length<10){
		alert("Please enter valid Moblie Number.");
		return;
	}
	if(points==""){
		alert("Please enter valid number of Points.");
		return;
	}
	if( mobNumber.substring(0, 3)=='+91')
		mobNumber=mobNumber.substring(3);
	var paymentModeId = 0;
	var paymentModeElement =  document.getElementById('paymentModeId'+paymentIndex);
	if (typeof(paymentModeElement) != 'undefined' && paymentModeElement != null){
		paymentModeId = paymentModeElement.value;
	}
	// if payment mode is other than -3 or -5.
	if( paymentModeId!=-3 && paymentModeId!=-5 ){
		alert("Please select other payment mode.");
		return;
	}
	showLoader();
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "//billing/BillAction.do?_method=requestOTPForLoyaltyCard&mobileNumber="+mobNumber+"&points="+points+"&paymentModeId="+paymentModeId;
	ajaxobj.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			hideLoader();
    	try{
	    	var json = JSON.parse(this.responseText);
	    	if(json['result'] == true){
	    		var mobNumberObj = getIndexedFormElement(documentForm, "mobNumber", paymentIndex);
	    		mobNumberObj.value = mobNumber;
	    		mobNumberObj.readOnly = true;
	    		if ('points_value' in json) {
	    		    var totPayingAmtObj = getIndexedFormElement(documentForm, "totPayingAmt", paymentIndex);
	    		    totPayingAmtObj.value = json['points_value'].replace(/,/g,"");
			}
	    		if ('request_number' in json) {
	    		    var refNumberObj = getIndexedFormElement(documentForm, "refNumber", paymentIndex);
	    			refNumberObj.value = json['request_number'];
	    			refNumberObj.readOnly = true;
	    		}
	    		loyaltyDialog.hide();
	    	}
	    	else{
	    		alert(json['message']);
	    	}
    	}
    	catch(e){
    	}
	}
	}
	ajaxobj.open("GET", url.toString(), true);
	ajaxobj.send(null);
}

function loadLoyaltyDialog() {
    var dialog = document.getElementById("loyaltyCardDialog");
    dialog.style.display = 'block';
    loyaltyDialog = new YAHOO.widget.Dialog("loyaltyCardDialog", {
        width : "450px",
        visible : false,
        modal : true,
        constraintoviewport : true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
            { fn:handleCancel,
              scope:loyaltyDialog,
              correctScope:true } );
    loyaltyDialog.cfg.queueProperty("keylisteners",[escKeyListener]);
    loyaltyDialog.cancelEvent.subscribe(handleCancel);
    loyaltyDialog.render();
}

function handleCancel() {
	var index=document.getElementById("rowIndex").value;
	var paymentModeId="paymentModeId"+index;
	document.getElementById(paymentModeId).value =-1;
	enableBankDetails(document.getElementById(paymentModeId));
	enableCommissionDetails(document.getElementById(paymentModeId));
	loyaltyDialog.hide();
}

function showLoyaltyPopup(modeObj){
	var selectedPaymentType = $(modeObj).closest('tr').find('#paymentType').val();
	var selectedPaymentTypeName = $(modeObj).closest('tr').find('#paymentType').children(':selected').text();
	var mode = modeObj.value;
	var index=modeObj.id.substr(-1);
	if( selectedPaymentType != 'receipt_advance' && selectedPaymentType != 'receipt_settlement' && (mode==-3 || mode==-5)){
		var paymentModeName = $(modeObj).children(':selected').text()
		modeObj.value = -1;
		alert(paymentModeName+" is not allowed for "+selectedPaymentTypeName);
		return false;
	}

	if(mode==-3 || mode==-5)
		{
			var row = getThisRow(modeObj);
		    if (loyaltyDialog != null) {
		    	loyaltyDialog.cfg.setProperty("context", [ modeObj, "tl", "tl" ], false);
		    	$(".loyaltyPointsLagendLabel").html($(modeObj).children(':selected').text());
		    	loyaltyDialog.show();
		    	document.getElementById("loyaltyPointMob").value =patientPhone;
		    	document.getElementById("redeemPoints").value ="";
		    	document.getElementById("rowIndex").value =index;
		    	var totPayingAmtid="totPayingAmt"+index;
		    	document.getElementById(totPayingAmtid).readOnly =true;
		    	document.getElementById("loyaltyPointAmount").innerHTML ="";
		    	document.getElementById("sendOTPBtn").disabled = true;
	    		document.getElementById("getPointsBtn").disabled = false;
		    }
		    return false;
		}
}

function showProcessPaymentBtn(modeObj) {
	var mode = modeObj.value;
	var id = modeObj.id.substring(13);	// modeObj.id will give paymentModeId0
	document.getElementById("processPayment"+id).hidden = (mode != -4 && mode != -10);
	document.getElementById("edcMachine"+id).disabled = (mode != -4);
	document.getElementById("processPayment"+id).disabled = (mode != -4 && mode != -10);

	if(mode == -4 && localStorage.getItem('edcMachine')) {
		document.getElementById("edcMachine"+id).value = localStorage.getItem('edcMachine');
	} else {
		document.getElementById("edcMachine"+id).value = "";
	}
}

function setLocalStorage(edcMachineObj) {
	localStorage.setItem('edcMachine', edcMachineObj.value );
}

function hidePaymentModeForDeposit() {
	var numPayments = getNumOfPayments();
	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;

		$("#"+paymentModeId+" option[value='-8']").remove();
		$("#"+paymentModeId+" option[value='-6']").remove();
		$("#"+paymentModeId+" option[value='-7']").remove();
		$("#"+paymentModeId+" option[value='-9']").remove();
	}
}

function loadProcessPaymentDialog() {
    var dialog = document.getElementById("processPaymentDialog");
    dialog.style.display = 'block';
    processPaymentDialog = new YAHOO.widget.Dialog("processPaymentDialog", {
        width : "450px",
        visible : false,
        modal : true,
        constraintoviewport : true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
            { fn:closeProcessPaymentDialog,
              scope:processPaymentDialog,
              correctScope:true } );
    processPaymentDialog.cfg.queueProperty("keylisteners",[escKeyListener]);
    processPaymentDialog.cancelEvent.subscribe(closeProcessPaymentDialog);
    processPaymentDialog.render();
}

function closeProcessPaymentDialog() {
	var id = document.getElementById("paymentRowNum").value;
	var plutusTxnId = document.getElementById("plutusTxnId"+id).value;
	if(plutusTxnId != "-1") {
		var imei = document.getElementById("edcMachine"+id).value;
		var amount = document.getElementById("totPayingAmt"+id).value;
		var mode = document.getElementById("paymentModeId"+id).value;

		var url = cpath + '/billing/canceltransaction.json?pay=' + amount + '&mode=' + mode + '&imei=' + imei + '&plutusTxnId=' + plutusTxnId;
		$.ajax({
			"url": url,
			"method": "POST",
			"dataType": "json",
			"success": function(cancelResult) {
				if(cancelResult["ResponseMessage"] == "APPROVED") {
					document.getElementById("plutusTxnId"+id).value = "-1"; //-1 denotes that we have no active transaction
					if (subscription) {
						subscription.unsubscribe();
						subscription = null;
					}
					if (pineLabResponsePoller) {
						clearTimeOut(pineLabResponsePoller);
						pineLabResponsePoller = null;
					}
					processPaymentDialog.hide();
				} else {
					document.getElementById("pineLabsResponse").innerHTML = cancelResult["ResponseMessage"];
				}
			}
		});
	} else {
		processPaymentDialog.hide();
	}
}

function showProcessPaymentDialog(processPaymentBtn) {

	var id = processPaymentBtn.id.substring(14); 	// processPaymentBtn.id will give processPayment0
	var imeiObj = document.getElementById("edcMachine"+id);
	var imei = imeiObj.value;
	var amount = document.getElementById("totPayingAmt"+id).value;
	var mode = document.getElementById("paymentModeId"+id).value;
	var sequence = 1 + +id ;
	var paymentType = getIndexedFormElement(documentForm, "paymentType", id).value;

	if(!amount) {
		alert("Please fill the amount");
		return;
	}
	if(mode == -4) {
	if(!imei) {
		alert("Please select the EDC machine");
		return;
	}
	if(getIndexedFormElement(documentForm, "paymentType", id).value == "refund") {
		alert("Payment type cannot be refund for EDC machine");
		return;
	}

	processPaymentDialog.cfg.setProperty("context", [ imeiObj, "tr", "br" ], false);
	document.getElementById("pineLabsResponse").innerHTML = "Processing payment on EDC machine and waiting for response...";
	processPaymentDialog.show();

	var url = cpath + '/billing/dotransaction.json?pay=' + amount + '&mode=' + mode + '&imei=' + imei + '&billNo=' + billNumber + '&sequence=' + sequence;
	$.ajax({
		"url": url,
		"method": "POST",
		"dataType": "json",
		"success": function(transactionResult) {
			document.getElementById("paymentRowNum").value = id;
			if(transactionResult["ResponseCode"] == "0") {
				document.getElementById("plutusTxnId"+id).value = transactionResult["PlutusTxId"];
				document.getElementById("pineLabsResponse").innerHTML = "Processing payment on EDC machine with Plutus Transaction ID: " + transactionResult["PlutusTxId"];
				getPineLabsTxnResponse(transactionResult["PlutusTxId"],id);
				checkPineLabsTxnStatus(mode,imei,transactionResult["PlutusTxId"]);
				return;
			} else if(transactionResult["ResponseCode"] !== "1") {
				return;
			}
			document.getElementById("plutusTxnId"+id).value = "-1"; //-1 denotes that we have no active transaction
			if (transactionResult["ResponseMessage"] !== "PLEASE APPROVE OPEN TXN FIRST") {
				return;
			}
			document.getElementById("pineLabsResponse").innerHTML = "Cancelling pending transaction first";
			var cancelUrl = cpath + "/billing/cancelpendingtransaction.json?mode=" + mode + "&imei=" + imei;
			$.ajax({
				"url": cancelUrl,
				"method": "POST",
				"dataType": "json",
				"success": function(cancelResult) {
					if(cancelResult["ResponseMessage"] == "APPROVED" || cancelResult["ResponseMessage"] == "TRANSACTION NOT FOUND") {
						showProcessPaymentDialog(processPaymentBtn);
					} else if(cancelResult["ResponseMessage"] == "CANNOT CANCEL AS TRANSACTION IS IN PROGRESS") {
						pineLabResponsePoller = setTimeout(function (){showProcessPaymentDialog(processPaymentBtn)}, 30000);
					}
				},
				"error": function(jqXHR, textStatus, err) {
					if (jqXHR.status !== 423) {
						return;
					}
					var result = JSON.parse(jqXHR.responseText);
					var ok = confirm(result.error.displayMessage);
					if (!ok) {
						return;
					}
					$.ajax({
						"url": cancelUrl + '&forceClose=true',
						"method": "POST",
						"dataType": "json",
						"success": function(forceCancelResult) {
							if(forceCancelResult["ResponseMessage"] == "APPROVED"  || forceCancelResult["ResponseMessage"] == "TRANSACTION NOT FOUND") {
								showProcessPaymentDialog(processPaymentBtn);
							} else if(forceCancelResult["ResponseMessage"] == "CANNOT CANCEL AS TRANSACTION IS IN PROGRESS") {
								pineLabResponsePoller = setTimeout(function (){showProcessPaymentDialog(processPaymentBtn)}, 30000);
							}
						}
					});
				}
			});
		},
		"error": function(){
			document.getElementById("pineLabsResponse").innerHTML = "Connection Error Occurred";
		}
	});
} else if (mode == -10) {
	if(!validateAllNumerics() || !validatePaymentAmount()){
		return;
	}
	doSalucroPayment(amount,mode,sequence,paymentType);
  }
}

function doSalucroPayment(amount,mode,sequence,paymentType) {
	var counterId = document.getElementById("counterId").value;
	if(billNumber == "medicine_sales"){
	 billNumber=document.getElementsByName("custName")[0].value;
	}
	var payload = {
		"pay": amount,
		"mode": mode,
		"billNo": billNumber,
		"sequence": sequence,
		"paymentType": paymentType,
		"counter_id" : counterId
	};	
	$.ajax({
		"url": cpath + "/billing/dotransaction.json",
		"data": payload,
		"method": "POST",
		"dataType": "json",
		"success": function(transactionResult) {
			var data=transactionResult.result;
			var dataObj = JSON.parse(data);
			var responsecode=dataObj.code;
			if(responsecode == "200") {
				var payload=transactionResult.payload;
				var obj = JSON.parse(payload);
				var header = getString("js.billing.salucro.popup.header");
				salucroPopup(obj.launchUrl,header) ;
				document.getElementById("salucroIframe").src=obj.launchUrl;
				salucroDialog.show();
				return;
			} else if(code == "400") {
				var error=transactionResult.error;
				var errorObj = JSON.parse(error);
				console.log(errorObj);
				return;
			}

		},
		"error": function(error){
			let errorMessage;
			if (typeof error.responseJSON.validationErrors !="undefined"){
			Object.keys(error.responseJSON.validationErrors).forEach(key => {
			errorMessage = error.responseJSON.validationErrors.error[key][0];
			})
		   }
		    else{
				errorMessage=error.responseJSON.error.displayMessage;
			}
			alert(errorMessage);
		}
	});
}

function salucroPopup(url, header) {
	salucroIframeDialog();
	document.getElementById("headerSalucro").innerHTML =header;
	document.getElementById("salucroIframe").innerHTML =url;
    if (salucroDialog != null) { 
		salucroDialog.show();
	}
}

function salucroIframeDialog() {
	var dialog = document.getElementById("salucroIframeDialog");
    dialog.style.display = 'block';
    salucroDialog = new YAHOO.widget.Dialog("salucroIframeDialog", {
		width : "850px",
		height : "650px",
        visible : false,
        modal : true,
        constraintoviewport : true
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
            { fn:handleSalucroCancel,
              scope:salucroDialog,
              correctScope:true } );
			  salucroDialog.render();
	salucroDialog.cfg.queueProperty("keylisteners",[escKeyListener]);
	salucroDialog.cancelEvent.subscribe(handleSalucroCancel);
	salucroDialog.render();
}

function handleSalucroCancel() {
	salucroDialog.hide();
}

function checkPineLabsTxnStatus(mode,imei,plutusTxnId) {
	var url = cpath + '/billing/checktransactionstatus.json?imei=' + imei + '&plutusTxnId=' + plutusTxnId + '&mode=' + mode;
	$.ajax({
		"url": url,
		"method": "POST",
		"dataType": "json",
		"success": function(transactionResult) {
			if(transactionResult["ResponseCode"] == "1001") {
				pineLabResponsePoller = setTimeout(function (){checkPineLabsTxnStatus(mode,imei,plutusTxnId)}, 30000);
			} else {
				pineLabResponsePoller = null;
				processPaymentDialog.hide();
				var id = document.getElementById("paymentRowNum").value;
				if(transactionResult["ResponseCode"] == "0") {
					getIndexedFormElement(documentForm, "refNumber", id).value = transactionResult["TID"];
					getIndexedFormElement(documentForm, "cardAuthCode", id).value = transactionResult["ApprovalCode"];
					getIndexedFormElement(documentForm, "cardNumber", id).value = transactionResult["CardNumber"];
					if(getIndexedFormElement(documentForm, "paymentRemarks", id) != null)
						getIndexedFormElement(documentForm, "paymentRemarks", id).value = "RRN:" + transactionResult["RRN"] +"/MID:" + transactionResult["MID"];

					getIndexedFormElement(documentForm, "refNumber", id).readOnly = true;
					getIndexedFormElement(documentForm, "cardAuthCode", id).readOnly = true;
					getIndexedFormElement(documentForm, "cardNumber", id).readOnly = true;
					document.getElementById("totPayingAmt"+id).readOnly = true;
					document.getElementById("paymentModeId"+id).disabled = true;
					document.getElementById("edcMachine"+id).disabled = true;
					getIndexedFormElement(documentForm, "paymentType", id).disabled = true;
				}
			}
		}
	});
}


function getPineLabsTxnResponse(plutusTxnId,id) {
	 var socket = SockJS(cpath + "/ws/instahms");
	 var stompClient = Stomp.over(socket);
	 stompClient.connect({}, function (){
		 subscription = stompClient.subscribe("/user/"+sessionId+"/topic/txnresult",handlePineLabsTxnResponse(plutusTxnId,id));
	 }, function(){
		 pineLabResponsePoller = setTimeout(function(){ getPineLabsTxnResponse(plutusTxnId,id); }, 1000);
	 });
}

function handlePineLabsTxnResponse(plutusTxnId,id) {
	return function(data){
		var txnResult = JSON.parse(data.body);
		if(plutusTxnId == txnResult["PlutusTransactionReferenceID"]) {
			if(txnResult["ResponseCode"] == "0") {
				getIndexedFormElement(documentForm, "refNumber", id).value = txnResult["TID"];
				getIndexedFormElement(documentForm, "cardAuthCode", id).value = txnResult["ApprovalCode"];
				getIndexedFormElement(documentForm, "cardNumber", id).value = txnResult["CardNumber"];
				if(getIndexedFormElement(documentForm, "paymentRemarks", id) != null)
					getIndexedFormElement(documentForm, "paymentRemarks", id).value = "RRN:" + txnResult["RRN"] +"/MID:" + txnResult["MID"];

				getIndexedFormElement(documentForm, "refNumber", id).readOnly = true;
				getIndexedFormElement(documentForm, "cardAuthCode", id).readOnly = true;
				getIndexedFormElement(documentForm, "cardNumber", id).readOnly = true;
				document.getElementById("totPayingAmt"+id).readOnly = true;
				document.getElementById("paymentModeId"+id).disabled = true;
				document.getElementById("edcMachine"+id).disabled = true;
				getIndexedFormElement(documentForm, "paymentType", id).disabled = true;
			} else {
				alert(txnResult["ResponseMessage"]);
			}
			processPaymentDialog.hide();
			if (subscription) {
				subscription.unsubscribe();
				subscription = null;
			}
		}
	}
}

function enableFormValues() {
	var numPayments = getNumOfPayments();
	for(var i=0; i<numPayments; i++) {
		document.getElementById("paymentModeId"+i).disabled = false;
		document.getElementById("edcMachine"+i).disabled = false;
		getIndexedFormElement(documentForm, "paymentType", i).disabled = false;
	}
}

function MobvalueChanged(){
	if($('#getPointsBtn').is(':disabled')){
		document.getElementById("sendOTPBtn").disabled = true;
		document.getElementById("getPointsBtn").disabled = false;
		document.getElementById("loyaltyPointAmount").innerHTML ="";
	}
}


function getPayDates() {
	var payDates = [];
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i = 0; i < numPayments && (allowReceiptBackDate != 'N'); i++) {
			var dateObj = getIndexedFormElement(documentForm, "payDate", i);
			var timeObj = getIndexedFormElement(documentForm, "payTime", i);
			payDates.push(getDateTime(dateObj.value, timeObj.value));
		}
	}
	return payDates;
}

function filterPaymentModes() {
	var numPayments = getNumOfPayments();
	//Storing payment mode names in hidden fields, for future reference;
	$('body').append('<input type="hidden" name="paymentModeOptionM6" id="paymentModeOptionM6"  value="'+$("#paymentModeId0 option[value='-6']").html()+'" />');
	$('body').append('<input type="hidden" name="paymentModeOptionM7" id="paymentModeOptionM7" value="'+$("#paymentModeId0 option[value='-7']").html()+'" />');
	$('body').append('<input type="hidden" name="paymentModeOptionM8" id="paymentModeOptionM8" value="'+$("#paymentModeId0 option[value='-8']").html()+'" />');
	$('body').append('<input type="hidden" name="paymentModeOptionM9" id="paymentModeOptionM9" value="'+$("#paymentModeId0 option[value='-9']").html()+'" />');

	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;
		if(!isMvvPackage) {
			$("#"+paymentModeId+" option[value='-8']").remove();
		}
		if(visitType === 'i') {
			if(((availableDeposits - ipDeposits) <= 0 && generalDepositSetOff == 0) || (availableDeposits > 0 && isMvvPackage)) {
				$("#"+paymentModeId+" option[value='-6']").remove();
			}
		} else {
			if(((availableDeposits) <= 0  && generalDepositSetOff == 0) || (availableDeposits > 0 && isMvvPackage)) {
				$("#"+paymentModeId+" option[value='-6']").remove();
			}
		}

		if((ipDeposits == 0 && ipDepositSetOff == 0) || (ipDeposits > 0 && isMvvPackage) || 
		   (typeof showIpDesposit != 'undefined' && !showIpDesposit)) {
			$("#"+paymentModeId+" option[value='-7']").remove();
		}

		if(!hasRewardPointsEligibility) {
			$("#"+paymentModeId+" option[value='-9']").remove();
		}
	}
}

function addDepositPaymentModesinDropdown() {
	var numPayments = getNumOfPayments();
	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;
		if( $("#"+paymentModeId+" option[value='-6']").length == 0) {
			$("#"+paymentModeId).append($('<option>').val('-6').html($('#paymentModeOptionM6').val()));
		}
		if( $("#"+paymentModeId+" option[value='-7']").length == 0) {
			$("#"+paymentModeId).append($('<option>').val('-7').html($('#paymentModeOptionM7').val()));
		}
		if( $("#"+paymentModeId+" option[value='-8']").length == 0) {
			$("#"+paymentModeId).append($('<option>').val('-8').html($('#paymentModeOptionM8').val()));
		}
		if( $("#"+paymentModeId+" option[value='-9']").length == 0) {
			$("#"+paymentModeId).append($('<option>').val('-9').html($('#paymentModeOptionM9').val()));
		}
	}

}

/** This function is to set payment type to set 'settlement',
 *  if selected type is advance for deposit payment types
 *  also validate if max available deposit  is 0 and deposit payment mode selected
 */
function depositSettlementRestriction(modeObj) {
	var selPaymentModeValue = modeObj.value;
	var selPaymentModeId = modeObj.id;
	if(selPaymentModeValue === '-6' || selPaymentModeValue === '-7' || selPaymentModeValue === '-8'
		|| selPaymentModeValue === '-9') {
		var selPaymentType = $(modeObj).closest("tr").find("#paymentType").val();
		if (selPaymentType == 'receipt_advance') {
			if (selPaymentModeValue === '-9') {
				alert(getString("js.billing.billlist.redeemnotpossible.advancetype"));
			} else {
				alert(getString("js.billing.billlist.depositnotpossible.advancetype"));
			}
			$(modeObj).closest("tr").find("#paymentType").val('receipt_settlement');
        }
		//validate if max available deposit  is 0 and deposit payment mode is selected
		if (selPaymentType == 'receipt_settlement' && availableDeposits == 0) {
			alert("Available Deposit is Zero,Cannot do the Deposit Set Off");
			$("#"+modeObj.id).val(-1); //reset to cash
		}
	}

}

//Amount validation method for Deposit set off payment
function validateAmountOnPaymentMode() {
	var numPayments = getNumOfPayments();
	var ipDepositsNumber = ipDeposits;
	// in sales screen ipDeposits is an Object
	if (ipDeposits instanceof Object) {
		ipDepositsNumber = ipDeposits.total_ip_deposits - ipDeposits.total_ip_set_offs;
	}

	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;
		var totPayingAmt = "totPayingAmt"+i;
		var paymentModelValue = $("#"+paymentModeId+" option:selected").val();
		var paymentTypeObj = getIndexedFormElement(documentForm, "paymentType", i);
		var paymentType = getIndexedFormElement(documentForm, "paymentType", i).value;
		var amount = $("#"+totPayingAmt+"").val();
		var paymentMode = getIndexedFormElement(documentForm,
				"paymentModeId", i);
		var paymentModeName = $(paymentMode).children(':selected').text();
		if ((paymentModelValue == -2 || paymentModelValue == -3
				|| paymentModelValue == -5 || paymentModelValue == -9)
				&& paymentType == 'refund') { // Paytm mode or Loyalty Card or apollo one
			alert('Amount cannot be Refund by ' + paymentModeName);
			$("#"+paymentModeId).val(-1); //reset to cash
			showRewardPoints(document.getElementById(paymentModeId));
			return false;
		}

		if ((paymentModelValue == -6 || paymentModelValue == -7
				|| paymentModelValue == -8 || paymentModelValue == -9 )
				&& paymentType == 'receipt_advance') { // Deposit payment modes, can't take accept advances
			$(paymentTypeObj).val('receipt_settlement');
			if (paymentModelValue == -9) {
				alert(getString("js.billing.billlist.redeemnotpossible.advancetype"));
			} else {
				alert(getString("js.billing.billlist.depositnotpossible.advancetype"));
			}
			return false;
		}

		if(amount>0) {
		    if(paymentModelValue === '-8' && paymentType !== 'refund') {
			    if(getPaise(amount) > getPaise(availableDeposits)) {
				    alert("The amount can not exceed package deposit amount"+" ("+availableDeposits+")")
				    $("#"+totPayingAmt+"").val(availableDeposits);
			    }
		    }
		    if(paymentModelValue === '-8' && paymentType == 'refund') {
			    if(getPaise(amount) > getPaise(generalDepositSetOff)) {
				    alert("The amount can not exceed package deposit setoff amount"+" ("+generalDepositSetOff+")")
				    $("#"+totPayingAmt+"").val(generalDepositSetOff);
			    }
		    }
		    if(paymentModelValue === '-6' && paymentType !== 'refund') {
		    	// In case of OP bills available deposits does not include IP deposits.
		    	var availableGeneralDeposit = availableDeposits;
		    	if(visitType === 'i') {
		    		availableGeneralDeposit = availableDeposits - ipDepositsNumber;
		    	}
			    if(getPaise(amount) > getPaise(availableGeneralDeposit)) {
				    alert("The amount can not exceed general deposit amount"+" ("+(availableGeneralDeposit)+")")
				    $("#"+totPayingAmt+"").val(availableGeneralDeposit);
			    }
		    }
		    if(paymentModelValue === '-6' && paymentType == 'refund') {
			    if(getPaise(amount) > getPaise(generalDepositSetOff)) {
				    alert("The amount can not exceed general deposit setoff amount"+" ("+generalDepositSetOff+")")
				    $("#"+totPayingAmt+"").val(generalDepositSetOff);
			    }
		    }
		    if(paymentModelValue === '-7' && paymentType !== 'refund') {
			    if(getPaise(amount) > getPaise(ipDepositsNumber)) {
				    alert("The amount can not exceed ip deposit amount"+" ("+ipDepositsNumber+")")
				    $("#"+totPayingAmt+"").val(ipDepositsNumber);
			    }
		    }
		    if(paymentModelValue === '-7' && paymentType == 'refund') {
			    if(getPaise(amount) > getPaise(ipDepositSetOff)) {
				    alert("The amount can not exceed ip deposit setoff amount"+" ("+ipDepositSetOff+")")
				    $("#"+totPayingAmt+"").val(ipDepositSetOff);
			    }
		    }

		    // deposit set off should not be more than patient due if not refund.
		    var depositPaymentModes = ['-6', '-7', '-8'];
		    if(depositPaymentModes.includes(paymentModelValue)) {
		    	var patDue = total_AmtDuePaise;
		    	var numPayments = getNumOfPayments();
		    	if (numPayments > 0) {
		    		for (var i=0; i<numPayments; i++) {
		    			var totPayAmt = getIndexedFormElement(documentForm, "totPayingAmt", i);
		    			var payType = getIndexedFormElement(documentForm, "paymentType", i);

		    			if (payType.value == "refund") {
		    				patDue += getPaise(totPayAmt.value);
		    			} else {
		    				patDue -= getPaise(totPayAmt.value);
		    			}
		    		}
		    	}
		    	// Do not consider if payment mode is refund
		    	if(paymentType !== 'refund' && patDue < 0) {
		    		alert("The deposit setoff amount cannot be more than the patient due amount.");
		    		$("#"+totPayingAmt+"").val(0);
		    	}
		    }
	    }
	}
}

//Check deposits if exists, have a confirmation box to prompt the user to use deposit amount on save
function checkDepositExistsAndNotUsed() {
	var numPayments = getNumOfPayments();
	var paymentModes = [];
	var paymentType = '';
	// For multivisit package or OP bill don't consider IP deposit.
	var generalDeposits = availableDeposits;
	var ipDepositsNumber = ipDeposits;
	// in sales screen ipDeposits is an Object
	if (ipDeposits instanceof Object) {
		ipDepositsNumber = ipDeposits.total_ip_deposits - ipDeposits.total_ip_set_offs;
	}
	// Consider IP deposit for IP bills only.
	if (visitType=='i') {
		generalDeposits = availableDeposits-ipDepositsNumber;
	}

	for (i=0; i<numPayments; i++){
		paymentType = getIndexedFormElement(documentForm, "paymentType", i).value;
		if($("#totPayingAmt"+i).val() != 0 && (paymentType=='receipt_advance' || paymentType=='receipt_settlement')) {
			paymentModes.push($("#paymentModeId"+i+" option:selected").val());
		}

	}

	//if patient visit type is IP and ipDepositsNumber have un utilized amount
	if ((ipDepositsNumber || availableDeposits) && paymentModes.length>0) {
		if (visitType=='i' && !paymentModes.includes("-7") && ipDepositsNumber>0) {
			if( generalDeposits>0 ) {
				confirmOk = confirm("User has a IP Deposit = "+ipDepositsNumber+" and General Deposit = "+generalDeposits+", Do you want to continue?");
			} else {
				confirmOk = confirm("User has a IP Deposit = "+ipDepositsNumber+", Do you want to continue?");
			}
			if(!confirmOk){
				return false;
			}
		}
		if ((ipDepositsNumber == 0 || visitType=='o') && !paymentModes.includes("-6") && generalDeposits>0 && !isMvvPackage ) {
			var confirmOk = confirm("User has a General Deposit = "+generalDeposits+", Do you want to continue?");
			if(!confirmOk){
				return false;
			}
		}
		if (!paymentModes.includes("-8") && generalDeposits>0 && isMvvPackage ) {
			var confirmOk = confirm("User has a Package Deposit = "+generalDeposits+", Do you want to continue?");
			if(!confirmOk){
				return false;
			}
		}
	}
	return true;

}

function disableTaxFeilds(){
	var numPayments = getNumOfPayments();
	if (numPayments > 0) {
		for (var i=0; i<numPayments; i++) {
			var payType = getIndexedFormElement(documentForm, "paymentType", i);
			if (payType.value == "refund") {

				if(document.getElementById("tax_subgrp_primary") != null) {
					document.getElementById("tax_subgrp_primary").selectedIndex = 0;;
					document.getElementById("tax_subgrp_primary").disabled = true;
				}

				if(document.getElementById("tax_subgrp_secondary") != null) {
					document.getElementById("tax_subgrp_secondary").selectedIndex = 0;
					document.getElementById("tax_subgrp_secondary").disabled = true;
				}

				if(document.getElementById("deposit_amount") != null) {
					document.getElementById("deposit_amount").textContent = "";
				}
				if(document.getElementById("total_tax_amount") != null) {
					document.getElementById("total_tax_amount").textContent = "";
				}
			} else {
				if(document.getElementById("tax_subgrp_primary") != null) {
					document.getElementById("tax_subgrp_primary").disabled = false;
				}

				if(document.getElementById("tax_subgrp_secondary") != null) {
					document.getElementById("tax_subgrp_secondary").disabled = false;
				}
			}
		}
	}
	setTaxDetailsDeposits();
}


function setTaxDetailsDeposits(){
	var numPayments = getNumOfPayments();
	var pTax = 0;
	var sTax = 0;
	var amount= 0;

	if(document.getElementById("tax_subgrp_primary") != null) {
		for(var i =0; i < taxSubGroupsList.length; i++){
			var taxdetails = taxSubGroupsList[i];
			if(document.getElementById("tax_subgrp_primary").value == taxdetails["item_subgroup_id"]){
				pTax = pTax + taxdetails["tax_rate"];
			}

			if(document.getElementById("tax_subgrp_secondary") != null) {
				if(document.getElementById("tax_subgrp_secondary").value == taxdetails["item_subgroup_id"]){
					sTax = sTax + taxdetails["tax_rate"];
				}
			}
		}
	}

	if (numPayments > 0) {
		for (var j=0; j<numPayments; j++) {
			var payType = getIndexedFormElement(documentForm, "paymentType", j).value;
			amount = amount + Number(getIndexedFormElement(documentForm, "totPayingAmt", j).value);

			if(payType == "refund"){
				if(document.getElementById("tax_subgrp_primary") != null) {
					document.getElementById("tax_subgrp_primary").selectedIndex = 0;;
					document.getElementById("tax_subgrp_primary").disabled = true;
				}

				if(document.getElementById("tax_subgrp_secondary") != null) {
					document.getElementById("tax_subgrp_secondary").selectedIndex = 0;
					document.getElementById("tax_subgrp_secondary").disabled = true;
				}

				if(document.getElementById("deposit_amount") != null) {
					document.getElementById("deposit_amount").textContent = "";
				}
				if(document.getElementById("total_tax_amount") != null) {
					document.getElementById("total_tax_amount").textContent = "";
				}
				pTax = 0;
				sTax = 0;
				amount= 0;
				break;
			}
		}
	}

	var taxCalculatedValue = formatAmountPaise(getPaise(amount- ((amount)/(1+((pTax+sTax)/100)))));

	if(document.getElementById("total_tax_amount") != null) {
		document.getElementById("total_tax_amount").textContent = taxCalculatedValue;
	}

	if(document.getElementById("deposit_amount") != null) {
		document.getElementById("deposit_amount").textContent = formatAmountPaise(getPaise(amount -taxCalculatedValue));
	}

}

/* Validation for Cash Limit */
function checkCashLimitValidation(mrno,visitId){
	var numPayments = getNumOfPayments();
	if (numPayments <= 0) return true;
	var amount = 0;
	var refundAmount = 0;
	var gen_deposit_setoff = 0;
	var ip_deposit_setoff = 0;
	var package_deposit_setoff = 0;
	var totalSetOff = 0;
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
		if ((paymentModelValue == -6) && paymentType != "refund") {
			var gensetOffCashAmount = $("#"+totPayingAmt+"").val();
			gen_deposit_setoff += getAmount(gensetOffCashAmount);
		}
		if ((paymentModelValue == -7) && paymentType != "refund") {
			var ipsetOffCashAmount = $("#"+totPayingAmt+"").val();
			ip_deposit_setoff += getAmount(ipsetOffCashAmount);
		}
		if ((paymentModelValue == -8) && paymentType != "refund") {
			var packagesetOffCashAmount = $("#"+totPayingAmt+"").val();
			package_deposit_setoff += getAmount(packagesetOffCashAmount);
		}
		if (paymentModelValue == -1 && paymentType == "refund") {
			var refundCashAmount = $("#"+totPayingAmt+"").val();
			refundAmount += getAmount(refundCashAmount);
		}
	}
	totalSetOff =(gen_deposit_setoff+ip_deposit_setoff+package_deposit_setoff);
	if (amount != 0 || refundAmount !=0 || totalSetOff !=0 ){
		if (amount > cashTransactionLimitAmt || refundAmount > cashTransactionLimitAmt){
			if(visitType == 'r' || visitType == 't' ){
				alert("Total cash in aggregate from this patient reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");

			} else {
				alert("Total cash in aggregate from this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +cashTransactionLimitAmt+ ".");
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
		if (ajaxobj && ajaxobj.readyState == 4
			&& (ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			var responseObj =  JSON.parse(ajaxobj.responseText);
			var dayCashLimit=responseObj.cash_limit_details.dayCash;
			var dayRefundCashLimit=responseObj.cash_limit_details.dayRefund;
			var visitCashLimit=responseObj.cash_limit_details.visitCash;
			var visitRefundCashLimit=responseObj.cash_limit_details.visitRefund;

			/* Set Off Amounts which are deposited through Cash Mode */
			var genCashDepSetOff=responseObj.cash_limit_details.genCashDepSetOff;
			var ipCashDepSetOff=responseObj.cash_limit_details.ipCashDepSetOff;
			var pkgCashDepSetOff=responseObj.cash_limit_details.pkgCashDepSetOff;
			var unAllocatedAmount=responseObj.cash_limit_details.un_allocatedAmount;

			/*
			* Set Off Amounts which are deposited through other than
			* Cash Mode
			*/
			var genDepSetOffAmt=responseObj.cash_limit_details.genDepSetOffAmt;
			var ipDepSetOffAmt=responseObj.cash_limit_details.ipDepSetOffAmt;
			var pkgDepSetOffAmt=responseObj.cash_limit_details.pkgDepSetOffAmt;

			var avblDepLimit=responseObj.cash_limit_details.avblCashLimit;
			var transactionLimit=responseObj.cash_limit_details.transactionLimit;
			if (dayCashLimit < 0) {
				var dayCashLimitAvble = (amount+dayCashLimit) <= 0 ? 0 : (amount+dayCashLimit);
				alert("Total cash in aggregate from this MRNO:" +mrno+ " in a day reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
					"Remaining Cash limit Available today is Rs." +dayCashLimitAvble + ".");
				return false;
			} else if (dayRefundCashLimit < 0 ) {
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
			} else if (totalSetOff !=0 &&
					(avblDepLimit <= 0 && unAllocatedAmount == 0 ) ||
					(genCashDepSetOff <= 0 || ipCashDepSetOff <= 0 || pkgCashDepSetOff <= 0)
					&& (genDepSetOffAmt > 0 || ipDepSetOffAmt > 0 || pkgDepSetOffAmt > 0)) {
				var visitSetOffLimitAvble = avblDepLimit  <= 0 ? 0 : (avblDepLimit);
				alert("Total cash transactions relating to Visit No:" +visitId+  " reaches the allowed Cash Transaction Limit of Rs." +transactionLimit+ ".\n" +
					"Remaining  Cash Limit Available for this visit is Rs." +visitSetOffLimitAvble+ ".\n" +
					"Available Deposit Amount in other Mode is Rs." +unAllocatedAmount+ ".");
				return false;
			}
		}
	}
	return true;
}

window.addEventListener("message", function (event) { 
	if (["https://pay.patientportal.me","https://integration.salucro.net","https://ppv4.salucro-training.net"].indexOf(event.origin) === -1) {
		return;
	}
		var command = JSON.parse(event.data);
		if (command.action !== 'transaction_notification')  {
			return;
		}
			var transaction = encodeURIComponent(JSON.stringify(command.transaction));
			var transactionDetails = command.transaction.transactions[0];
			var amount = transactionDetails.accounts[0].amount;
			amount = amount.replace (/,/g, "");
			var bill_no = transactionDetails.accounts[0].account_identifier;
			var transactionId = transactionDetails.transaction_id;
			var status = transactionDetails.status;
			var url = cpath + '/billing/paymentTransaction.json?mode=-10' + '&amount=' + amount + '&transactionId=' + transactionId + '&status=' + status + '&billNo=' + bill_no + '&transaction=' +transaction;
			if(!transactionDetails.original_payment_id)
			{
				var user_name = command.transaction.username?command.transaction.username:"";
		        var payer_name = transactionDetails.payment_method.payer_name?transactionDetails.payment_method.payer_name:"";
			    var card_number = transactionDetails.payment_method.last4?transactionDetails.payment_method.last4:"";
			    var card_type = transactionDetails.payment_method.brand?transactionDetails.payment_method.brand:"";
				var expiry = transactionDetails.payment_method.expiry?transactionDetails.payment_method.expiry:"" ;
				if(expiry){
				expiry = expiry.substring(0, 2) + "/" + expiry.substring(2, expiry.length);
				}
				var cardAuthCode = transactionDetails.auth_code?transactionDetails.auth_code:"" ;
				var type = transactionDetails.payment_method.type;
			    url = url + '&inititaedBy=' + user_name + '&cardNumber=' + card_number + '&paymentType=' + type + '&cardAuthCode=' + cardAuthCode;
			}	
			$.ajax({
					 "url": url,
					 "method": "POST",
					 "dataType": "json",
					 "success": function(transactionResult) {
					     var id = getNumOfPayments()-1;
 						 getIndexedFormElement(documentForm, "totPayingAmt", id).value = transactionResult.bean.map.amount;
						if ( transactionResult.bean.map.card_number && transactionResult.bean.map.card_number != "" ) {
							getIndexedFormElement(documentForm, "cardNumber", id).value = "**** **** **** " +transactionResult.bean.map.card_number;
						}
						if ( expiry) {
							getIndexedFormElement(documentForm, "cardExpDate", id).value = expiry;
						}
						if ( card_type ) {
							getIndexedFormElement(documentForm, "cardTypeId", id).value = card_type;
						}
						if ( payer_name ) {
							getIndexedFormElement(documentForm, "cardHolderName", id).value = payer_name;
						}
						if ( transactionResult.transaction_id ) {
							getIndexedFormElement(documentForm, "paymentTransactionId", id).value = transactionResult.transaction_id;
						}
						if ( transactionResult.bean.map.approval_code ) {
							getIndexedFormElement(documentForm, "cardAuthCode", id).value = transactionResult.bean.map.approval_code;
						}
						if ( transactionId ) {
							getIndexedFormElement(documentForm, "refNumber", id).value = transactionId;
						}
						getIndexedFormElement(documentForm, "refNumber", id).readOnly = true;
						getIndexedFormElement(documentForm, "cardAuthCode", id).readOnly = true;
						getIndexedFormElement(documentForm, "cardNumber", id).readOnly = true;
						getIndexedFormElement(documentForm, "cardExpDate", id).readOnly = true;
						getIndexedFormElement(documentForm, "cardTypeId", id).readOnly = true;
						getIndexedFormElement(documentForm, "cardHolderName", id).readOnly = true;
						getIndexedFormElement(documentForm, "processPayment", id).disabled = true;
						getIndexedFormElement(documentForm, "paymentModeId", id).disabled = true;
						getIndexedFormElement(documentForm, "totPayingAmt", id).readOnly = true;
						return;
					},
					"error": function(error){
						let errorMessage;
					if (typeof error.responseJSON.validationErrors !="undefined"){
						Object.keys(error.responseJSON.validationErrors).forEach(key => {
						errorMessage = error.responseJSON.validationErrors.error[key][0];
						})
					}
					else{
						errorMessage=error.responseJSON.error.displayMessage;
					}
					alert(errorMessage);
					}
				});
			var salucroIframe = document.getElementById('salucroIframe');
			var msg = JSON.stringify({action:"notification_ack"});
			salucroIframe.contentWindow.postMessage(msg, '*');
}, false);
