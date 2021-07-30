/*
 * Scripts used for/by OrderTable.jsp
 */

orderTableOnlyNew = false;

function orderTableInit(onlyNew) {
	orderTableOnlyNew = onlyNew;

	var i=1; // 0 is for package image
	if (!onlyNew)
		BILL_NO_COL = i++;
	DATE_COL = i++;
	if (!onlyNew)
		ORDERNO_COL = i++;
	PRES_DOCTOR_COL = i++;
	TYPE_COL = i++;
	ITEM_COL = i++;
	DETAILS_COL = i++;
	REMARKS_COL = i++;
	AMOUNT_COL = i++;
	PATIENT_AMT_COL = i++;
	PRE_AUTH_COL = i++;
	SEC_PRE_AUTH_COL = i++;
	if(multiPlanExists) {
		// moving this outside the if, since the column itself is static but only display is dynamic
		// SEC_PRE_AUTH_COL = i++;
		document.getElementById("secPriorAuthHeader").style.display = 'table-cell';
	}else{
		document.getElementById("secPriorAuthHeader").style.display = 'none';
	}
	TRASH_COL = i++;
	EDIT_COL = i++;

	if (!onlyNew) {
		initCancelOptionsDialog();
		initOpCancelOptionsDialog();
	}
}

/*
 * order.rateDetails contains details of the charge, usually the rate,
 * qty, discount, amount. For operation, this is a list of charges, for others it is one charge.
 * For equipment and OT, additional info about min_rate, incr_rate etc. are also available.
 */

function addToPresTable(order) {
	var table = document.getElementById(order.addTo);
	var numRows = table.rows.length;
	var templateRow = table.rows[numRows-1];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.className = "added";

	var details = "";
	if (order.itemType == 'Other Charge' || order.itemType == 'Service') {
		details = order.quantity + ' No(s)';
	} else if (order.itemType == 'Meal') {
		if (order.mealTiming == 'Spl') {
			details = order.from + ' ' + order.fromTime;
		} else {
			details = order.from + ' ' + order.mealTiming;
		}
	}else if(order.itemType == 'Bed'){
		details = order.quantity + ' Days ';
	} else {
		if (order.from) {
			details = order.from;
		}
		if (order.to) {
			details += ' - ' + order.to;
		}
	}
	setNodeText(row.cells[DATE_COL], order.presDate);
	setNodeText(row.cells[PRES_DOCTOR_COL], order.presDoctorName);
	if (order.chargeTypeDisplay != null && order.chargeTypeDisplay != '')
		setNodeText(row.cells[TYPE_COL], order.itemType + ' (' + order.chargeTypeDisplay + ')');
	else
		setNodeText(row.cells[TYPE_COL], order.itemType);
	setNodeText(row.cells[ITEM_COL], order.itemName, 32);
	setNodeText(row.cells[DETAILS_COL], details, 16);
	setNodeText(row.cells[REMARKS_COL], order.remarks, 10);

	// we calculate and warn about total charges regardless of whether we show
	// charges or not. Therefore, we need to store the amounts anyway.
	var amountPaise = 0;
	var patientAmtPaise = 0;
	for (var i=0; i<order.rateDetails.length; i++) {
		charge = order.rateDetails[i];
		//table.rows[numRows-1].cells[ITEM_COL].appendChild(makeHidden('orderCategory', '', charge.insuranceCategoryId));
		//table.rows[numRows-1].cells[ITEM_COL].appendChild(makeHidden('chargeHead', '', charge.chargeHead));
		//table.rows[numRows-1].cells[ITEM_COL].appendChild(makeHidden('orderDiscount', '', charge.discount));
		//table.rows[numRows-1].cells[ITEM_COL].appendChild(makeHidden('priClaimAmt', '', 0));
		//table.rows[numRows-1].cells[ITEM_COL].appendChild(makeHidden('secClaimAmt', '', 0));


		getElementByName(row, 'orderCategory').value = charge.insuranceCategoryId;
		getElementByName(row, 'orderDiscount').value = charge.discount;
		getElementByName(row, 'chargeHead').value = charge.chargeHead;
		getElementByName(row, 'orderTax').value = charge.taxAmt;
		getElementByName(row, 'priClaimAmt').value = 0;
		getElementByName(row, 'secClaimAmt').value = 0;

		table.rows[numRows-1].cells[ITEM_COL].appendChild(makeHidden('pseudoPatientAmt', '', formatAmountPaise(getPaise(charge.amount) - getPaise(charge.insuranceClaimAmount))));
		getElementByName(table.rows[numRows-1],"firstOfCategory").value  = charge.firstOfCategory.toString();
		amountPaise += getPaise(charge.amount);
		patientAmtPaise += (getPaise(charge.amount) - getPaise(charge.insuranceClaimAmount));
		var patientAmount = patientAmtPaise;
		var firstOfCategory = charge.firstOfCategory;
		for(var j=0; j<numRows-1; j++) {
			var ordTablRow = table.rows[j];
			if (getElementByName(ordTablRow, 'orderCategory') != null) {
				var tablOrderCategory =	getElementByName(ordTablRow, 'orderCategory').value;
				var tablOrderPatientAmt =	getElementByName(ordTablRow, 'orderPatientAmt').value;
				var insurancePayable = "true";
				// Only for doctor charges, whose insurance category will be dependent on the consultation type and be 0 on item fetch, recalculate the charges.
				if(addOrderDialog.orderInsuranceCategoryId == 0 && tablOrderCategory ==  charge.insuranceCategoryId && tablOrderPatientAmt!= 0) {
					getElementByName(table.rows[numRows-1], 'firstOfCategory').value = "false";
					firstOfCategory = false;
				}
			}
		}

		var claimAmt = 0;
		var remainingAmt = charge.amount;
		var patAmt = formatAmountPaise(patientAmtPaise);
		var insPayable = findInList(addOrderDialog.orderChargeHeadJSON, "CHARGEHEAD_ID", charge.chargeHead).INSURANCE_PAYABLE;
		if(!orderTableOnlyNew) {
			for(var j=0; j<planList.length; j++){
				claimAmt = calculateClaimAmount(remainingAmt,charge.discount,charge.insuranceCategoryId,firstOfCategory,
					charge.visitType,charge.billNo,planList[j].plan_id, insPayable);
				remainingAmt = remainingAmt - claimAmt;
				patAmt = remainingAmt;
			}
		} else {
			var planIds = [];
			planIds = order.planIds;
			if(planIds != null && planIds.length > 0) {
				for(var i=0; i<planIds.length; i++) {
					claimAmt = calculateClaimAmount(remainingAmt,charge.discount,charge.insuranceCategoryId,firstOfCategory,
					charge.visitType,charge.billNo,planIds[i], insPayable);
					remainingAmt = remainingAmt - claimAmt;
					patAmt = remainingAmt;
				}
			}
		}
		patientAmtPaise = getPaise(patAmt);

		var insurancePayable = "true";
		if(addOrderDialog.orderChargeHeadJSON != null ){
			insurancePayable = findInList(addOrderDialog.orderChargeHeadJSON, "CHARGEHEAD_ID", charge.chargeHead).INSURANCE_PAYABLE == 'Y'? "true":"false";
			patientAmtPaise = insurancePayable != "true"? amountPaise : patientAmtPaise;
		}
	}
	var amountEl = getElementByName(row, 'orderAmount');
	amountEl.value = formatAmountPaise(amountPaise);
	var patientAmtEl = getElementByName(row, 'orderPatientAmt');
	patientAmtEl.value = formatAmountPaise(patientAmtPaise);
	if(document.getElementById('patietConsultAmount')){
		getDoctorCharge();
	}

	// Display order table amount & patient amount columns based on action rights and rate plan.
	var display = displayRatePlanAmounts(order);
	if (display) {
		setNodeText(row.cells[AMOUNT_COL], formatAmountPaise(amountPaise));
		setNodeText(row.cells[PATIENT_AMT_COL], formatAmountPaise(patientAmtPaise));
	}
	setNodeText(row.cells[PRE_AUTH_COL], order.preAuthNo);
	if(multiPlanExists)
		setNodeText(row.cells[SEC_PRE_AUTH_COL], order.secPreAuthNo);


	// disable editing for newly added items by clearing the cell contents
	if (!editOrders)
		row.cells[EDIT_COL].innerHTML = '';


	var newEl = getElementByName(row, 'new');
	newEl.value = 'Y';

	// blank indicates its not the existingtype
	var existingtype = getElementByName(row, 'existingtype');
	existingtype.value="";

	return numRows - 1;
}

function displayRatePlanAmounts(order) {
	return (!empty(showChargesAllRatePlan) && showChargesAllRatePlan == 'A');
}

function getTotalNewOrdersAmountPaise(instanceId) {
	var totalAmountPaise = 0;
	var table = document.getElementById('orderTable' + instanceId);
	var numRows = table.rows.length;
	var templateRowIndex = numRows-1;
	for (var index = 1; index < templateRowIndex; index++) {
		var row = table.rows[index];
		var newEl = getElementByName(row, 'new');
		if (newEl.value == 'Y') {
			totalAmountPaise += getPaise(getElementByName(row, 'orderAmount').value);
		}
	}
	return totalAmountPaise;
}

function getTotalNewOrdersAmount(instanceId) {
	return getTotalNewOrdersAmountPaise(instanceId)/100;
}

function getTotalNewOrdersPatientAmountPaise(instanceId) {
	var totalAmountPaise = 0;
	var table = document.getElementById('orderTable' + instanceId);
	var numRows = table.rows.length;
	var templateRowIndex = numRows-1;
	for (var index = 1; index < templateRowIndex; index++) {
		var row = table.rows[index];
		var newEl = getElementByName(row, 'new');
		if (newEl.value == 'Y') {
			totalAmountPaise += getPaise(getElementByName(row, 'orderPatientAmt').value);
		}
	}
	return totalAmountPaise;
}

function getTotalNewOrdersPatientAmount(instanceId) {
	return getTotalNewOrdersPatientAmountPaise(instanceId)/100;
}

function clearOrderTable(instanceId) {
	var table = document.getElementById('orderTable' + instanceId);
	var numRows = table.rows.length;
	var lastRowIndex = numRows-2;
	for (var index = lastRowIndex; index >0; index--) {
		var row = table.rows[index];
		var newEl = getElementByName(row, 'new');
		if (newEl.value == 'Y') {
			row.parentNode.removeChild(row);
		}
	}
	var docVisitTable = document.getElementById('innerDocVisitForPack');
	for (var i=0; i<docVisitTable.rows.length; ) {
		docVisitTable.deleteRow(i);
	}

	var docVisitTable = document.getElementById('innerCondDocForPack');
	for (var i=0; i<docVisitTable.rows.length; ) {
		docVisitTable.deleteRow(i);
	}


	// delete only the newly ordered test documents.
	var testDocTable = document.getElementById('ad_test_info_table');
	var testDocArray = new Array();
	if (!empty(testDocTable)) {
		for (var i=0; i < testDocTable.rows.length; i++) {
			var row = testDocTable.rows[i];
			if (YAHOO.util.Dom.hasClass(row, 'added')) {
				testDocArray.push(row);
			}
		}
	}

	for (var i=0; i<testDocArray.length; i++) {
		var row = testDocArray[i];
		row.parentNode.removeChild(row);
	}
}

function showOrderTablePatientAmounts(instanceId, show) {
	var table = document.getElementById('orderTable' + instanceId);
	var numRows = table.rows.length;
	for (var i = 0; i < numRows; i++) {
		if (show){
			table.rows[i].cells[PATIENT_AMT_COL].style.display = '';
			table.rows[i].cells[PRE_AUTH_COL].style.display = '';
			if(multiPlanExists)
				table.rows[i].cells[SEC_PRE_AUTH_COL].style.display = '';
		}else{
			table.rows[i].cells[PATIENT_AMT_COL].style.display = 'none';
			table.rows[i].cells[PRE_AUTH_COL].style.display = 'none';
			if(multiPlanExists)
				table.rows[i].cells[SEC_PRE_AUTH_COL].style.display = 'none';
		}
	}
}

function addInvestigations(order) {
	var index = addToPresTable(order);
	var orderTable = order.addTo;
	var cell = document.getElementById(orderTable).rows[index].cells[0];
	var row = document.getElementById(orderTable).rows[index];
	var filter = order.itemType == 'Laboratory' ? 'DEP_LAB' : 'DEP_RAD';
	var testPrescId = '_' + index;

	// the field names must match the db field names
	getElementByName(row,"type").value = 'test';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('test.package_id', '', order.packageId));
	cell.appendChild(makeHidden('test.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('test.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('test.test_id', '', order.itemId));
	cell.appendChild(makeHidden('test.pres_date', '', order.presDate));
	cell.appendChild(makeHidden('test.pres_doctor', '', order.presDoctorId));
	cell.appendChild(makeHidden('test.conducted', '', 'N'));
	cell.appendChild(makeHidden('test.priority', '', 'R'));
	cell.appendChild(makeHidden('addedItemType', '', filter));
	cell.appendChild(makeHidden('test.item_name', '', order.itemName));
	cell.appendChild(makeHidden('test.conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	getElementByName(row, "remarks").value = order.remarks;
	getElementByName(row, "serviceGroupId").value = order.itemServiceGroupId;
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	cell.appendChild(makeHidden('test.remarks', '', order.remarks));
	cell.appendChild(makeHidden('test.operation_ref', '', order.operationRef));
	cell.appendChild(makeHidden('test.doc_presc_id', '', order.prescriptionRef));
	cell.appendChild(makeHidden('conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	cell.appendChild(makeHidden('conduction_required', '', order.rateDetails[0].conduction_required));
	cell.appendChild(makeHidden('test.prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('test.prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('test.sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('test.sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('test.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('test.payee_doctor_id','',order.condDoctorId));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));
	cell.appendChild(makeHidden('test.urgent','',order.urgent));
	cell.appendChild(makeHidden('test.prescId', '', testPrescId));

	if (addOrderDialog.validateTestAdditionalInfo == true)
		addRowToTestAdtnlDocs(order, testPrescId);
}


function addServices(order) {

	var index = addToPresTable(order);
	var orderTable = order.addTo;
	var cell = document.getElementById(orderTable).rows[index].cells[0];
	var row = document.getElementById(orderTable).rows[index];

	// the field names must match the db field names
	getElementByName(row,"type").value = 'service';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('service.package_id', '', order.packageId));
	cell.appendChild(makeHidden('service.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('service.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('service.service_id', '', order.itemId));
	cell.appendChild(makeHidden('service.presc_date', '', order.presDate));
	cell.appendChild(makeHidden('service.doctor_id', '', order.presDoctorId));
	cell.appendChild(makeHidden('service.conducted', '', 'N'));
	cell.appendChild(makeHidden('service.item_name', '', order.itemName));
	cell.appendChild(makeHidden('addedItemType', '', 'service'));
	cell.appendChild(makeHidden('service.conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	getElementByName(row, "remarks").value = order.remarks;
	getElementByName(row, "serviceGroupId").value = order.itemServiceGroupId;
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	cell.appendChild(makeHidden('service.remarks', '', order.remarks));
	cell.appendChild(makeHidden('service.operation_ref', '', order.operationRef));
	cell.appendChild(makeHidden('service.doc_presc_id', '', order.prescriptionRef));
	cell.appendChild(makeHidden('conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	cell.appendChild(makeHidden('conduction_required', '', order.rateDetails[0].conduction_required));
	cell.appendChild(makeHidden('service.prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('service.prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('service.sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('service.sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('service.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('service.payee_doctor_id','',order.condDoctorId));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id','', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));
	cell.appendChild(makeHidden('service.quantity', '', order.quantity));

	if(tooth_numbering_system == 'U') {
		cell.appendChild(makeHidden('service.tooth_unv_number', '', order.toothNumber));
	} else {
		cell.appendChild(makeHidden('service.tooth_fdi_number', '', order.toothNumber));
	}
	cell.appendChild(makeHidden('s_tooth_number', '', order.toothNumber));
	cell.appendChild(makeHidden('s_tooth_num_required', '', order.toothNumberReq));
}

function addEquipment(order) {

	var index = addToPresTable(order);
	var orderTable = order.addTo;
	var cell = document.getElementById(orderTable).rows[index].cells[0];
	var row = document.getElementById(orderTable).rows[index];

	// the field names must match the db field names
	getElementByName(row,"type").value = 'Equipment';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('equipment.package_id', '', order.packageId));
	cell.appendChild(makeHidden('equipment.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('equipment.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('equipment.eq_id', '', order.itemId));
	cell.appendChild(makeHidden('equipment.date', '', order.presDate));
	cell.appendChild(makeHidden('equipment.used_from', '', order.from));
	cell.appendChild(makeHidden('equipment.used_till', '', order.to));
	cell.appendChild(makeHidden('equipment.units', '', order.units));
	cell.appendChild(makeHidden('equipment.doctor_id', '', order.presDoctorId));
	cell.appendChild(makeHidden('equipment.conducted', '', 'U'));
	cell.appendChild(makeHidden('equipment.finalization_status', '', order.finalized ? 'F' : 'N'));
	getElementByName(row, "remarks").value = order.remarks;
	getElementByName(row, "serviceGroupId").value = order.itemServiceGroupId;
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	cell.appendChild(makeHidden('equipment.remarks', '', order.remarks));
	cell.appendChild(makeHidden('equipment.operation_ref', '', order.operationRef));
	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	cell.appendChild(makeHidden('conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	cell.appendChild(makeHidden('conduction_required', '', order.rateDetails[0].conduction_required));
	cell.appendChild(makeHidden('equipment.prior_auth_id', '', null));
	cell.appendChild(makeHidden('equipment.prior_auth_mode_id', '', 1));
	cell.appendChild(makeHidden('equipment.sec_prior_auth_id', '', null));
	cell.appendChild(makeHidden('equipment.sec_prior_auth_mode_id', '', 1));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('equipment.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));

}

function addOtherServices(order, serviceGroup) {

	var index = addToPresTable(order);
	var orderTable = order.addTo;
	var cell = document.getElementById(orderTable).rows[index].cells[0];
	var row = document.getElementById(orderTable).rows[index];

	// the field names must match the db field names
	getElementByName(row,"type").value = 'other';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('other.package_id', '', order.packageId));
	cell.appendChild(makeHidden('other.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('other.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('other.service_name', '', order.itemId));
	cell.appendChild(makeHidden('other.pres_time', '', order.presDate));
	cell.appendChild(makeHidden('other.doctor_id', '', order.presDoctorId));
	cell.appendChild(makeHidden('other.service_group', '', serviceGroup));
	cell.appendChild(makeHidden('other.quantity', '', order.quantity));
	getElementByName(row, "remarks").value = order.remarks;
	getElementByName(row, "serviceGroupId").value = order.itemServiceGroupId;
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	cell.appendChild(makeHidden('other.remarks', '', order.remarks));
	cell.appendChild(makeHidden('other.operation_ref', '', order.operationRef));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('other.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	cell.appendChild(makeHidden('conduction_required', '', order.rateDetails[0].conduction_required));
	cell.appendChild(makeHidden('other.prior_auth_id', '', null));
	cell.appendChild(makeHidden('other.prior_auth_mode_id', '', 1));
	cell.appendChild(makeHidden('other.sec_prior_auth_id', '', null));
	cell.appendChild(makeHidden('other.sec_prior_auth_mode_id', '', 1));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));

}

function addDoctor(order) {

	var index = addToPresTable(order);
	var orderTable = order.addTo;
	var cell = document.getElementById(orderTable).rows[index].cells[0];
	var row = document.getElementById(orderTable).rows[index];

	// the field names must match the db field names
	getElementByName(row,"type").value = 'doctor';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('doctor.package_id', '', order.packageId));
	cell.appendChild(makeHidden('doctor.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('doctor.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('doctor.doctor_name', '', order.itemId));
	cell.appendChild(makeHidden('doctor.presc_date', '', order.presDate));
	cell.appendChild(makeHidden('doctor.presc_doctor_id', '', order.presDoctorId));
	cell.appendChild(makeHidden('doctor.visited_date', '', order.from == ' '?order.presDate:order.from));
	getElementByName(row, "remarks").value = order.remarks;
	getElementByName(row, "serviceGroupId").value = order.itemServiceGroupId;
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	cell.appendChild(makeHidden('doctor.remarks', '', order.remarks));
	cell.appendChild(makeHidden('doctor.head', '', order.chargeType) );
	cell.appendChild(makeHidden('doctor.operation_ref', '', order.operationRef));
	cell.appendChild(makeHidden('doctor.prior_auth_id', '', null));
	cell.appendChild(makeHidden('doctor.prior_auth_mode_id', '', 1));
	cell.appendChild(makeHidden('doctor.sec_prior_auth_id', '', null));
	cell.appendChild(makeHidden('doctor.sec_prior_auth_mode_id', '', 1));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('doctor.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));

	var otDocRole = '';
	var conductionStatus = 'A';
	if (order.operationRef != '') {
		otDocRole = order.chargeType;
		conductionStatus = 'U';
	}
	cell.appendChild(makeHidden('doctor.ot_doc_role', '', otDocRole));
	cell.appendChild(makeHidden('doctor.status', '', conductionStatus));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));

	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	if (order.additionalDetails != null) {
		cell.appendChild(makeHidden('doctor.prescription_id', '', order.additionalDetails[0].presId));
		cell.appendChild(makeHidden('doctor.doc_presc_id', '', order.additionalDetails[0].presId));
	}
	if (order.rateDetails.length > 0) {
		cell.appendChild(makeHidden('conducting_doc_mandatory', '',
					order.rateDetails[0].conducting_doc_mandatory));
		cell.appendChild(makeHidden('conduction_required', '',
					order.rateDetails[0].conduction_required));
	} else {
		// can be empty if no charges for the anaesthetist while ordering operation
		cell.appendChild(makeHidden('conducting_doc_mandatory', '', false));
		cell.appendChild(makeHidden('conduction_required', '', false));
	}


	//to validate main consultation
	var consultationType = findInList(doctorConsultationTypes, "consultation_type_id", parseInt(order.subType));
	if(consultationType != null &&
		( consultationType.visit_consultation_type  != -2 || consultationType.visit_consultation_type != -4 ) )
		mainConsultations.push(consultationType);

}

function addDiets(order) {

	var index = addToPresTable(order);
	var orderTable = order.addTo;
	var cell = document.getElementById(orderTable).rows[index].cells[0];
	var row = document.getElementById(orderTable).rows[index];

	// the field names must match the db field names
	getElementByName(row, "type").value = 'diet';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('diet.package_id', '', order.packageId));
	cell.appendChild(makeHidden('diet.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('diet.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('diet.diet_id', '', order.itemId));
	cell.appendChild(makeHidden('diet.ordered_time', '', order.presDate));
	cell.appendChild(makeHidden('diet.meal_date', '', order.fromDate));
	cell.appendChild(makeHidden('diet.meal_timing', '', order.mealTiming));
	cell.appendChild(makeHidden('diet.meal_time', '',order.fromTime));
	cell.appendChild(makeHidden('diet.ordered_by', '', order.presDoctorId));
	cell.appendChild(makeHidden('diet.special_instructions', '', order.remarks));
	cell.appendChild(makeHidden('diet.status', '', 'N'));
	if (order.additionalDetails != null)
		cell.appendChild(makeHidden('diet.diet_pres_id', '', order.additionalDetails[0].presId));

	cell.appendChild(makeHidden('diet.doc_presc_id', '', order.prescriptionRef));
	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	cell.appendChild(makeHidden('conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	cell.appendChild(makeHidden('conduction_required', '', order.rateDetails[0].conduction_required));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('diet.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('diet.prior_auth_id', '', null));
	cell.appendChild(makeHidden('diet.prior_auth_mode_id', '', 1));
	cell.appendChild(makeHidden('diet.sec_prior_auth_id', '', null));
	cell.appendChild(makeHidden('diet.sec_prior_auth_mode_id', '', 1));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '',null));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', null));
	cell.appendChild(makeHidden('sec_prior_auth_id', '',null));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', null));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;

}

String.prototype.startsWith = function (str){
   	return this.slice(0, str.length) == str;
};

function addOperations(order) {

	var table = document.getElementById("operationtable");
	var numRows = table.rows.length;
	var templateRow = table.rows[numRows-1];
	var row = templateRow.cloneNode(true);

	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.className = "added";
	row.id = 'op_new_' + row.rowIndex;

	var addButtonForOp = document.getElementsByName('btnAddItem')[row.rowIndex+1];
	var instanceId = '-' + row.rowIndex;  // -ve number for dummy op prescId.
	addButtonForOp.setAttribute("onclick", "addOrderDialog.start(this, true, \'" + instanceId + "\',\'" +order.itemId+ "'\); return false;");

	var opOrderTable = YAHOO.util.Dom.getElementsByClassName("detailList", null, row)[0];
	opOrderTable.id = 'orderTable' + instanceId;

	var idEl = getElementByName(row, 'operationId');
	idEl.value = instanceId;

	var legend = YAHOO.util.Dom.getElementsByClassName("fieldSetLabel", null, row)[0];

	setNodeText(legend, order.itemName.length > 32 ? order.itemName.substring(0,32)+'...' : order.itemName);
	legend.setAttribute("title", order.itemName );
	// todo: show prescribed date and doc as well. Maybe also stuff like Remarks and cond. status

	// disable editing for newly added items by clearing the cell contents
	var opTab = YAHOO.util.Dom.getElementsByClassName("infotable", null, row)[0];
	//opTab.rows[0].cells[9].innerHTML = '';

	var newEl = getElementByName(row, 'new');
	newEl.value = 'Y';

	// blank indicates its not the existingtype
	var existingtype = getElementByName(row, 'existingtype');
	existingtype.value="";

	var labels = YAHOO.util.Dom.getElementsByClassName("forminfo", null, row);
	setNodeText(labels[0], empty(order.theatreId) ? "" : order.theatreName);
	setNodeText(labels[1], order.from);
	setNodeText(labels[2], order.to);
	setNodeText(labels[3], order.surgeonName);
	setNodeText(labels[4], 'New');
	setNodeText(labels[5], order.remarks,50);
	// hidden variables required.
	var cell = row.cells[0];
	cell.appendChild(makeHidden('type', '', "operation"));
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('operation.package_id', '', order.packageId));
	cell.appendChild(makeHidden('operation.pack_ob_id', '', order.packObId));
	cell.appendChild(makeHidden('operation.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('operation.consultant_doctor', '', order.presDoctorId));
	cell.appendChild(makeHidden('operation.operation_name', '', order.itemId));
	cell.appendChild(makeHidden('operation.theatre_name', '', order.theatreId));
	cell.appendChild(makeHidden('operation.department', '', ''));		// todo
	cell.appendChild(makeHidden('operation.finalization_status', '', order.finalized ? 'F' : 'N'));
	cell.appendChild(makeHidden('operation.start_datetime', '', order.from == ' '?null:order.from));
	cell.appendChild(makeHidden('operation.remarks', '', order.remarks));
	cell.appendChild(makeHidden('operation.end_datetime', '', order.to == ' '?null:order.to));
	cell.appendChild(makeHidden('operation.hrly', '', order.units == 'H' ? 'checked' : ''));
	cell.appendChild(makeHidden('operation.surgeon', '', order.surgeonId));
	cell.appendChild(makeHidden('operation.anaesthetist', '', order.anaesthetistId));
	cell.appendChild(makeHidden('operation.prescribed_date', '', order.presDate));
	cell.appendChild(makeHidden('operation.prescribed_id', '', instanceId));
//	cell.appendChild(makeHidden('operation.anesthesia_type', '', order.anaeTypeId));
	cell.appendChild(makeHidden('operation.prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('operation.prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('operation.sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('operation.sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('operation.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));

	var anaeTypeIds = order.anaeTypeIds;
	var anaTypeFromDates = order.anaTypeFromDates;
	var anaTypeToDates = order.anaTypeToDates;
	var anaTypeFromTimes = order.anaTypeFromTimes;
	var anaTypeToTimes = order.anaTypeToTimes;
	for (var i=0;i<anaeTypeIds.length;i++) {
		if(!empty(anaeTypeIds[i].value)) {
			cell.appendChild(makeHidden('operation.anesthesia_type'+instanceId, '', anaeTypeIds[i].value));
			cell.appendChild(makeHidden('operation.anesthesia_type_from_date'+instanceId, '', anaTypeFromDates[i].value));
			cell.appendChild(makeHidden('operation.anesthesia_type_to_date'+instanceId, '', anaTypeToDates[i].value));
			cell.appendChild(makeHidden('operation.anesthesia_type_from_time'+instanceId, '', anaTypeFromTimes[i].value));
			cell.appendChild(makeHidden('operation.anesthesia_type_to_time'+instanceId, '', anaTypeToTimes[i].value));
		}
	}
	var operTotalAmountPaise = 0;
	var operTotalPatientAmtPaise = 0;
	var anaCharges = new Array();
	for (var i=0; i<order.rateDetails.length; i++) {
		if (order.rateDetails[i].chargeHead == 'ANAOPE') {
			anaCharges.push(order.rateDetails[i]);
		} else {
			// exclude Ana charge from total operations amount
			operTotalAmountPaise += getPaise(order.rateDetails[i].amount);
			operTotalPatientAmtPaise += getPaise(order.rateDetails[i].amount) -
				getPaise(order.rateDetails[i].insuranceClaimAmount);
		}
	}
	var amountEl = getElementByName(row, 'operAmount');
	amountEl.value = formatAmountPaise(operTotalAmountPaise);

	var patientAmtEl = getElementByName(row, 'operPatientAmt');
	patientAmtEl.value = formatAmountPaise(operTotalPatientAmtPaise);
	setNodeText(labels[6], formatAmountPaise(operTotalAmountPaise));

	if (order.anaesthetistId != '') {
		// add an additional order for the anaesthetist, which is separate
		var anaOrder = { addTo: 'orderTable' + instanceId,
			operationRef: instanceId,
			itemId: order.anaesthetistId,
			itemType: 'Anaesthetist',
			itemName: order.anaesthetistName,
			remarks: order.remarks,
			presDoctorId: order.presDoctorId,
			presDoctorName: order.presDoctorName,
			presDate: order.presDate,
			from: order.from,
			chargeType: 'ANAOPE',
			rateDetails: anaCharges
		}
		addDoctor(anaOrder);
	}

}


function addPackages(order) {
	var orderTable = order.addTo;
	var index = addToPresTable(order);
	var cell = document.getElementById("orderTable0").rows[index].cells[0];
	var packPrescId = '_' + index;
	var row = document.getElementById(orderTable).rows[index];

	getElementByName(row, "type").value = 'package';
	getElementByName(row,"item_id").value = order.itemId;
	cell.appendChild(makeHidden('package.package_id', '', order.packageId));
	cell.appendChild(makeHidden('package.multi_visit_package', '', order.multiVisitPackage+''));
	cell.appendChild(makeHidden('package.packageId', '', order.itemId));
	cell.appendChild(makeHidden('package.orderedTime', '', order.presDate));
	cell.appendChild(makeHidden('package.prescId', '', packPrescId));
	/** details of operation in package */
	cell.appendChild(makeHidden('package.opId', '', order.additionalDetails[0].operation_id));
	cell.appendChild(makeHidden('package.startDateTime', '', order.from == ' '?order.presDate:order.from));
	cell.appendChild(makeHidden('package.toDateTime', '', order.to));
	cell.appendChild(makeHidden('package.theatreName', '', order.theatreId));
	cell.appendChild(makeHidden('package.units', '', order.units));
	cell.appendChild(makeHidden('package.doctorId', '', order.presDoctorId));
	getElementByName(row, "remarks").value = order.remarks;
	getElementByName(row, "serviceGroupId").value = order.itemServiceGroupId;
	getElementByName(row, "package_id").value = order.packageId;
	getElementByName(row, "multi_visit_package").value = order.multiVisitPackage;
	cell.appendChild(makeHidden('package.remarks', '', order.remarks));
	cell.appendChild(makeHidden('package.surgeon', '', order.surgeonId));
	cell.appendChild(makeHidden('package.anaesth', '', order.anaesthetistId));
	cell.appendChild(makeHidden('package.status', '', 'S'));
	cell.appendChild(makeHidden('package.needReport', '', 'true'));

	cell.appendChild(makeHidden('package.doc_presc_id', '', order.prescriptionRef));

	getElementByName(row, "presDocName").value = empty(order.presDoctorName) ? '' : order.presDoctorName;
	cell.appendChild(makeHidden('conducting_doc_mandatory', '', order.rateDetails[0].conducting_doc_mandatory));
	cell.appendChild(makeHidden('conduction_required', '', order.rateDetails[0].conduction_required));
	cell.appendChild(makeHidden('package.prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('package.prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('package.sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('package.sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	var firstOfCat = document.getElementById(orderTable) != null ? getElementByName(document.getElementById(orderTable).rows[index], 'firstOfCategory') : null;
	cell.appendChild(makeHidden('package.firstOfCategory', '', (firstOfCat != null) ? firstOfCat.value : ''));
	cell.appendChild(makeHidden('prior_auth_id_req', '', order.priorAuthReq));
	cell.appendChild(makeHidden('prior_auth_id', '', order.preAuthNo));
	cell.appendChild(makeHidden('prior_auth_mode_id', '', order.preAuthModeNo));
	cell.appendChild(makeHidden('sec_prior_auth_id', '', order.secPreAuthNo));
	cell.appendChild(makeHidden('sec_prior_auth_mode_id', '', order.secPreAuthModeNo));
	cell.appendChild(makeHidden('pres_date', '', order.presDatePart));
	cell.appendChild(makeHidden('cancelled', '', ''));
	/** End Operation Details */

	/** details of Doctor visits in package */
	var docVisitTable = document.getElementById("docVisits");
	if (docVisitTable.rows.length > 0) {
		for ( var index = 0; index < docVisitTable.rows.length/2; index++ ) {
			var docVisitInnerTable = document.getElementById("innerDocVisitForPack");
			var docInnerRow = docVisitInnerTable.insertRow(-1);
			var innerCell = docInnerRow.insertCell(-1);
			innerCell.appendChild(makeHidden("package.doctorHead", "", document.getElementById("doctorHeadId"+index).value));
			innerCell.appendChild(makeHidden("package.vDoctor", "", document.getElementById("vDoctorId"+index).value ));
			innerCell.appendChild(makeHidden("package.packIdFordoc", "", order.itemId));
			innerCell.appendChild(makeHidden("package.packPrescIdFordoc", "", packPrescId));
			innerCell.appendChild(makeHidden("package.docVisitDateTime", "", document.getElementById("presdate"+index).value + ' ' + document.getElementById("prestime"+index).value));
			innerCell.appendChild(makeHidden("package.mainRowIndex", "", row.rowIndex));

		}
		//handleDocVisitDialogCancel();
	}

	var condDocTable = document.getElementById("conductingDoctors");
	if (condDocTable.rows.length > 0) {
		for ( var index = 0; index < condDocTable.rows.length-1; index++ ) {
			var docVisitInnerTable = document.getElementById("innerCondDocForPack");
			var docInnerRow = docVisitInnerTable.insertRow(-1);
			var innerCell = docInnerRow.insertCell(-1);
			innerCell.appendChild(makeHidden("package.condDoctor", "", document.getElementById("packCondDoctorId"+index).value ));
			innerCell.appendChild(makeHidden("package.activity_id", "", document.getElementById("packageActId"+index).value ));
			innerCell.appendChild(makeHidden("package.packIdForCondDoc", "", order.itemId));
			innerCell.appendChild(makeHidden("package.packPrescIdForCondDoc", "", packPrescId));
			innerCell.appendChild(makeHidden("package.packageActivityIndex", "", document.getElementById("packageActivityIndex"+index).value));
			innerCell.appendChild(makeHidden("package.mainRowIndex", "", row.rowIndex));
			innerCell.appendChild(makeHidden("package.packageItemName", "", document.getElementById("packageItemName"+index).value));
			innerCell.appendChild(makeHidden("package.packageName", "", document.getElementById("packageName"+index).value));
			innerCell.appendChild(makeHidden("package.packageItemType", "", document.getElementById("packageItemType"+index).value))
		}
		//handleDocVisitDialogCancel();
	}
	/** End Doctor visits */

	if (addOrderDialog.validateTestAdditionalInfo == true)
		addRowToTestAdtnlDocs(order, packPrescId);
}

function validatePriorAuthMode(priorAuthEl, priorAuthModeEl, priorAuthId, priorAuthModeId) {
	var priorAuthValue ="";
	var priorAuthModeVal = "";

	var thePriorAuthModeEl = priorAuthModeEl;

	if(priorAuthEl == null && priorAuthId == null){
		return false;
	}

	if(priorAuthModeEl == null && priorAuthModeId == null){
		return false;
	}

	if(priorAuthEl == null && priorAuthId != null){
		priorAuthValue = document.getElementById(priorAuthId).value;
	} else {
		priorAuthValue = priorAuthEl.value;
	}

	if(priorAuthModeEl == null && priorAuthModeId != null){
		priorAuthModeVal = document.getElementById(priorAuthModeId).value;
		thePriorAuthModeEl = document.getElementById(priorAuthModeId);
	} else {
		priorAuthModeVal = priorAuthModeEl.value;
	}

	var isPriorAuthEmpty = priorAuthValue == null || priorAuthValue == "" ;
	var isPriorAuthModeEmpty = priorAuthModeVal == null || priorAuthModeVal == "";

	if(!isPriorAuthEmpty && isPriorAuthModeEmpty) {
		alert(getString("js.common.order.prior.auth.mode.required")+"\n"+getString("js.common.order.select.prior.auth.mode.string"));
		thePriorAuthModeEl.focus();
		return false;
	}

	if(isPriorAuthEmpty && !isPriorAuthModeEmpty) {
		setSelectedIndex(thePriorAuthModeEl, "");
	}

	return true;
}

function onDoctorCancle(row){
	var subtype  = getElementByName(row, 'doctor.head');
	if( subtype == '' )
		subtype  = getElementByName(row, 'sub_type');
	if (subtype == null) return;
	var consultationType = findInList(doctorConsultationTypes, "consultation_type_id", parseInt(subtype.value));
	if(consultationType != null &&
		( consultationType.visit_consultation_type  != -2 || consultationType.visit_consultation_type != -4 ) )
		mainConsultations.splice(consultationType);
}