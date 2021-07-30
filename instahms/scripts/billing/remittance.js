function init() {
	initArrays();
	var insCompObj	= document.getElementById('insurance_co_id');
	var tpaObj		= document.getElementById('tpa_id');
	if (insCompObj.selectedIndex != 0)
		onChangeInsuranceCompany();

	var detailLevelObj			= document.getElementById('detail_level');
	var itemIdentificationObj	= document.getElementById('item_identification');

	if (tpaObj.selectedIndex != 0)
		onChangeTPA();

	if (paramMethod == 'add' && detailLevelObj) {
		if (!empty(detailLevel)) setSelectedIndex(detailLevelObj, detailLevel);
		enableDisableDetailLevelFields();
		if (detailLevelObj.value == 'B') {}
		else {
			if (!empty(itemIdentity)) setSelectedIndex(itemIdentificationObj, itemIdentity);
			enableDisableIdentificationFields();
		}
		enableDisablePaymentRefFields();
	}else {
		if (detailLevelObj != null && itemIdentificationObj != null) {
			document.getElementById('bill_no_heading').value  = "";
			document.getElementById('item_id_heading').value = "";
			document.getElementById('service_name_heading').value  = "";
			document.getElementById('charge_insurance_claim_amount_heading').value  = "";
			document.getElementById('service_posted_date_heading').value  = "";
			document.getElementById('payment_reference').value  = "";
			document.getElementById('payment_reference_heading').value  = "";
			document.getElementById('amount_heading').value  = "";
			document.getElementById('denial_remarks_heading').value  = "";
			document.getElementById('payer_id_heading').value  = "";
		}
	}
	setRecovery();
}

function enableDisableDetailLevelFields() {
	var detailLevelObj			= document.getElementById('detail_level');
	var itemIdentificationObj	= document.getElementById('item_identification');

	if (detailLevelObj.value == 'B') {
		setSelectedIndex(itemIdentificationObj, 'BillNo');
		itemIdentificationObj.disabled = true;
	}else {
		setSelectedIndex(itemIdentificationObj, 'ActivityId');
		itemIdentificationObj.disabled = false;
	}
	enableDisableIdentificationFields();

	var serviceNameHeadObj		= document.getElementById('service_name_heading');
	var insuranceClaimAmountHeadObj		= document.getElementById('charge_insurance_claim_amount_heading');
	var servicePostedDateHeadObj= document.getElementById('service_posted_date_heading');

	if (detailLevelObj.value == 'B' && itemIdentificationObj.value == 'BillNo') {
		serviceNameHeadObj.disabled = true;
		insuranceClaimAmountHeadObj.disabled = true;
		servicePostedDateHeadObj.disabled = true;
	}else {
		serviceNameHeadObj.disabled = serviceNameHeadObj.disabled;
		insuranceClaimAmountHeadObj.disabled = insuranceClaimAmountHeadObj.disabled;
		servicePostedDateHeadObj.disabled = servicePostedDateHeadObj.disabled;
	}
	defaultLabelHeadings();
}

function enableDisableIdentificationFields() {
	var itemIdentificationObj	= document.getElementById('item_identification');
	var itemIdHeadObj			= document.getElementById('item_id_heading');
	var billNoHeadObj			= document.getElementById('bill_no_heading');
	var serviceNameHeadObj		= document.getElementById('service_name_heading');
	var insuranceClaimAmountHeadObj		= document.getElementById('charge_insurance_claim_amount_heading');
	var servicePostedDateHeadObj= document.getElementById('service_posted_date_heading');

	if (itemIdentificationObj.value == 'ActivityId') {
		itemIdHeadObj.disabled = false;

		billNoHeadObj.disabled = true;
		serviceNameHeadObj.disabled = true;
		insuranceClaimAmountHeadObj.disabled = true;
		servicePostedDateHeadObj.disabled = true;
	}else {
		itemIdHeadObj.disabled = true;

		billNoHeadObj.disabled = false;
		serviceNameHeadObj.disabled = false;
		insuranceClaimAmountHeadObj.disabled = false;
		servicePostedDateHeadObj.disabled = false;
	}
	defaultLabelHeadings();
}

function enableDisablePaymentRefFields() {
	var paymentRefTypeObj		= document.getElementById('payment_ref_type');
	var paymentReferenceObj		= document.getElementById('payment_reference');
	var paymentReferenceHeadObj = document.getElementById('payment_reference_heading');

	if (paymentRefTypeObj.value  == 'Single') {
		paymentReferenceObj.disabled = false;
		paymentReferenceHeadObj.disabled = true;
	}else {
		paymentReferenceObj.disabled = true;
		paymentReferenceHeadObj.disabled = false;
	}
	defaultLabelHeadings();
}

function validateDelete() {
	if (!validateFileDelete())
		return false;

	document.RemittanceForm._method.value = "deleteRemittance";
	document.RemittanceForm.submit();
	return true;
}

function validateFileDelete() {
	var ok = confirm("The claim amount received for the charges/bills will be deleted.\n"+
					"Are you sure you want to delete this file?");
	if (!ok)
		return false;
	else
		return true;
}

function validateSubmit() {
	var insCompObj = document.getElementById('insurance_co_id');
	var tpaObj = document.getElementById('tpa_id');

	var dateObj = document.getElementById('received_date');
	if (dateObj != null) {
		if (dateObj.value == '') {
			alert("Enter date");
			dateObj.focus();
			return false;
		}
		if (!doValidateDateField(dateObj)) {
			dateObj.focus();
			return false;
		}
	}

	var valid = true;

	valid = valid && validateRequired(tpaObj, "Select TPA name");
	if (!valid) return false;

	var Format = document.getElementById("claimFormat").value;

	if (Format == 'XL'){
		var detailLevelObj			= document.getElementById('detail_level');
		var workSheetIndexObj 		= document.getElementById('worksheet_index');
		var itemIdentificationObj	= document.getElementById('item_identification');
		var itemIdHeadObj			= document.getElementById('item_id_heading');
		var billNoHeadObj			= document.getElementById('bill_no_heading');
		var serviceNameHeadObj		= document.getElementById('service_name_heading');
		var insuranceClaimAmountHeadObj	= document.getElementById('charge_insurance_claim_amount_heading');
		var servicePostedDateHeadObj= document.getElementById('service_posted_date_heading');
		var paymentRefTypeObj		= document.getElementById('payment_ref_type');
		var paymentReferenceObj		= document.getElementById('payment_reference');
		var paymentReferenceHeadObj = document.getElementById('payment_reference_heading');
		var amountHeadObj			= document.getElementById('amount_heading');
		var denialRemarksHeadObj	= document.getElementById('denial_remarks_heading');
		var payerIdHeadObj			= document.getElementById('payer_id_heading');

		workSheetIndexObj.value		= trim(workSheetIndexObj.value);
		itemIdHeadObj.value			= trim(itemIdHeadObj.value);
		billNoHeadObj.value			= trim(billNoHeadObj.value);
		serviceNameHeadObj.value	= trim(serviceNameHeadObj.value);
		paymentReferenceObj.value	= trim(paymentReferenceObj.value);
		paymentReferenceHeadObj.value = trim(paymentReferenceHeadObj.value);
		amountHeadObj.value			= trim(amountHeadObj.value);
		denialRemarksHeadObj.value	= trim(denialRemarksHeadObj.value);
		payerIdHeadObj.value		= trim(payerIdHeadObj.value);
		insuranceClaimAmountHeadObj.value	= trim(insuranceClaimAmountHeadObj.value);
		servicePostedDateHeadObj.value	= trim(servicePostedDateHeadObj.value);


		valid = valid && validateRequired(workSheetIndexObj, "Worksheet Index is required");
		if (!valid) return false;

		workSheetIndexObj.value = trim(workSheetIndexObj.value);

		if (!isInteger(workSheetIndexObj.value) || (workSheetIndexObj.value==0) ) {
			alert("Invalid Worksheet Index. Worksheet Index should be greater than Zero");
			workSheetIndexObj.focus();
			valid = false;
			return false;
		}

		valid = valid && validateRequired(itemIdentificationObj, "Item Identification is required");
		if (!valid) return false;

		if (detailLevelObj.value == 'B') {
			valid = valid && validateRequired(billNoHeadObj, "Bill No. Heading is required");
			if (!valid) return false;
		}else {
			if (itemIdentificationObj.value == 'ActivityId') {
				valid = valid && validateRequired(itemIdHeadObj, "Item ID Heading is required");
				if (!valid) return false;

			}else {

				valid = valid && validateRequired(billNoHeadObj, "Bill No. Heading is required");
				if (!valid) return false;

				valid = valid && validateRequired(serviceNameHeadObj, "Service Name Heading is required");
				if (!valid) return false;

				valid = valid && validateRequired(insuranceClaimAmountHeadObj, "Service Insurance Claim Amt. Heading is required");
				if (!valid) return false;

				valid = valid && validateRequired(servicePostedDateHeadObj, "Service Posted Date Heading is required");
				if (!valid) return false;
			}
		}

		if (paymentRefTypeObj.value  == 'Single') {
			valid = valid && validateRequired(paymentReferenceObj, "Payment Reference is required");
			if (!valid) return false;
		}else {
			valid = valid && validateRequired(paymentReferenceHeadObj, "Payment Reference Heading is required");
			if (!valid) return false;
		}

		valid = valid && validateRequired(amountHeadObj, "Remittance Amount Heading is required");
		if (!valid) return false;

		valid = valid && validateRequired(denialRemarksHeadObj, "Denial Remarks Heading is required");
		if (!valid) return false;

		//valid = valid && validateRequired(payerIdHeadObj, "Payer ID Heading is required");
		//if (!valid) return false;
	}
	if (document.getElementById('remittance_metadata')!= null
	&& (document.getElementById('remittance_metadata').value == null
	|| document.getElementById('remittance_metadata').value == "")) {
		alert("Please upload the Remittance Advice File...");
		return false;
	}
	document.RemittanceForm.submit();
	return true;
}

function defaultLabelHeadings() {

	var itemIdHeadObj			= document.getElementById('item_id_heading');
	var billNoHeadObj			= document.getElementById('bill_no_heading');
	var serviceNameHeadObj		= document.getElementById('service_name_heading');
	var insuranceClaimAmountHeadObj	= document.getElementById('charge_insurance_claim_amount_heading');
	var servicePostedDateHeadObj= document.getElementById('service_posted_date_heading');
	var paymentReferenceObj		= document.getElementById('payment_reference');
	var paymentReferenceHeadObj = document.getElementById('payment_reference_heading');
	var amountHeadObj			= document.getElementById('amount_heading');
	var denialRemarksHeadObj	= document.getElementById('denial_remarks_heading');
	var payerIdHeadObj			= document.getElementById('payer_id_heading');

	if (!billNoHeadObj.disabled) {
		if (trim(billNoHeadObj.value) == "") billNoHeadObj.value  = "Bill No.";
		else billNoHeadObj.value  = trim(billNoHeadObj.value);
	}else
		billNoHeadObj.value  = "";

	if (!itemIdHeadObj.disabled) {
		if (trim(itemIdHeadObj.value) == "") itemIdHeadObj.value  = "Activity Charge Id";
		else itemIdHeadObj.value  = trim(itemIdHeadObj.value);
	}else
		itemIdHeadObj.value  = "";

	if (!serviceNameHeadObj.disabled) {
		if (trim(serviceNameHeadObj.value) == "") serviceNameHeadObj.value  = "Item Description";
		else serviceNameHeadObj.value  = trim(serviceNameHeadObj.value);
	}else
		serviceNameHeadObj.value  = "";

	if (!insuranceClaimAmountHeadObj.disabled) {
		if (trim(insuranceClaimAmountHeadObj.value) == "") insuranceClaimAmountHeadObj.value  = "Item Claim Net Amount";
		else insuranceClaimAmountHeadObj.value  = trim(insuranceClaimAmountHeadObj.value);
	}else
		insuranceClaimAmountHeadObj.value  = "";

	if (!servicePostedDateHeadObj.disabled) {
		if (trim(servicePostedDateHeadObj.value) == "") servicePostedDateHeadObj.value  = "Posted Date";
		else servicePostedDateHeadObj.value  = trim(servicePostedDateHeadObj.value);
	}else
		servicePostedDateHeadObj.value  = "";

	if (!paymentReferenceObj.disabled) {
		if (trim(paymentReferenceObj.value) == "") paymentReferenceObj.value  = "";
		else paymentReferenceObj.value  = trim(paymentReferenceObj.value);
	}else
		paymentReferenceObj.value  = "";

	if (!paymentReferenceHeadObj.disabled) {
		if (trim(paymentReferenceHeadObj.value) == "") paymentReferenceHeadObj.value  = "Payment Reference";
		else paymentReferenceHeadObj.value  = trim(paymentReferenceHeadObj.value);
	}else
		paymentReferenceHeadObj.value  = "";

	if (!amountHeadObj.disabled) {
		if (trim(amountHeadObj.value) == "") amountHeadObj.value  = "Paid Amount";
		else amountHeadObj.value  = trim(amountHeadObj.value);
	}else
		paymentReferenceObj.value  = "";

	if (!denialRemarksHeadObj.disabled) {
		if (trim(denialRemarksHeadObj.value) == "") denialRemarksHeadObj.value  = "Denial Remarks";
		else denialRemarksHeadObj.value  = trim(denialRemarksHeadObj.value);
	}else
		denialRemarksHeadObj.value  = "";

	if (!payerIdHeadObj.disabled) {
		if (trim(payerIdHeadObj.value) == "") payerIdHeadObj.value  = "Payer Id";
		else payerIdHeadObj.value  = trim(payerIdHeadObj.value);
	}else
		payerIdHeadObj.value  = "";
}

function addMandatoryIndicators() {
	var form = document.RemittanceForm;
	if (form) {
		for (var i=0; i<form.elements.length; i++) {
			var elmt = form.elements[i];
			var typeTxt = elmt.type;
			var name = elmt.name;
			typeTxt = typeTxt.toLowerCase();
			if (name != 'insurance_co_id' && name != 'received_date' && (typeTxt == 'text' || typeTxt == 'select-one')) {
				if (!elmt.disabled && !elmt.readOnly)
					appendStarSpan(elmt.parentNode);
				else
					removePreviousSpans(elmt.parentNode);
			}
		}
	}
	return true;
}

function setRecovery() {
	var isRecoveryObj		= document.getElementById('is_recovery');
	var recoveryCheckObj	= document.getElementById('recovery_check');
	if (recoveryCheckObj != null && recoveryCheckObj.checked) {
		if (isRecoveryObj != null) isRecoveryObj.value = "Y";
	}else {
		if (isRecoveryObj != null) isRecoveryObj.value = "N";
	}
}

function showClaimFormat() {
	var selectedTPAId = document.getElementById("tpa_id").value;
	var selectedCenId = document.getElementById("center_or_account_group").value;
	if(selectedCenId.substring(0, 1) == 'C') {
		selectedCenId = selectedCenId.slice(1);
	}
	var Format = '';
	for(var i=0; i<tpaList.length; i++){
		if(selectedTPAId == tpaList[i].tpa_id){
			Format = tpaList[i].claim_format;
			break;
		}
	}
	for(var i=0; i<tpaCenterList.length; i++){
		if(selectedTPAId == tpaCenterList[i].tpa_id && selectedCenId == tpaCenterList[i].center_id) {
			Format = tpaCenterList[i].claim_format;
			break;
		}
	}
	document.getElementById("claimFormat").value = Format;
	if(Format == 'XML'){
		//document.getElementById("recoveryId").style.display = 'block';
		//document.getElementById("recoveryId").style = 'white-space:nowrap';
		//document.getElementById("XLRow").style.display = 'none';
	}else if(Format == 'XL'){
		document.getElementById("XLRow").style.display = 'block';
		document.getElementById("XLRow").style = 'white-space:nowrap';
		document.getElementById("recoveryId").style.display = 'none';
	}
}
