
var presAutoComp = null;
var rowUnderEdit = null;
var editDialog = null;
var gIsNewItemDeleted = false;

function initDialysisOrder() {

  if (dischargedAsDead === true) {
    removeMainForm("mainform");
    return;
  } else {
    dischargedAsDead = false;
  }

	var doctors = {
		"doctors": doctorsList
	};
	presAutoComp = doctorAutoComplete('ePrescribedBy', 'ePrescribedByContainer', doctors, document.editForm);
	initEditDialog();

	resetHiddenFields();
	initAddOrderDialog();
	initCancelOptionsDialog();
	initOpCancelOptionsDialog();
	onChangeFilter();
	//setPaymentAmount();
	setRequiredAmtsToOrderGrid();

	enableORdisableRatePlan(mainVisitId);
	document.getElementById("addPaymentMode").disabled = true;
	if(userCenterId == 0 && isMultiCenter) {
		document.getElementById("saveButton").disabled = true;
	}

	if(null != visitID && '' != visitID){
		document.getElementById("visitDate").disabled = true;
		document.getElementById("visitTime").disabled = true;
	} else{
		document.getElementById("visitDate").disabled = false;
		document.getElementById("visitTime").disabled = false;
	}
}

function removeMainForm(formName) {
  let mainForm = document.getElementById(formName);
  mainForm.remove();
  let message = getString("js.billing.dialysisorders.patient.not.alive");
  message = message.replace("js.billing.dialysisorders.patient.not.alive", deathDate + " " + deathTime);
  alert(message);
}

function enableORdisableRatePlan(mainVisitId){
	if(mainVisitId != '')
		document.getElementById("organization_details").disabled = true;
}

function onChangeRatePlan(){
	clearOrderTable(0);
	addOrderDialog.setOrgId(document.getElementById("organization_details").value);
}

var visitOrgId = null;
var addOrderDialog;

function initAddOrderDialog() {

	var doctors = {
		"doctors": doctorsList
	};

	visitOrgId  = document.getElementById('organization_details').value;

	addOrderDialog = new Insta.AddOrderDialog('btnAddItem',
				rateplanwiseitems, null, addOrders, doctors, null, 'o', 'GENERAL', visitOrgId, "", "",
				regDate, regTime, 'A',
				fixedOtCharges, null, forceSubGroupSelection, regPref, anaeTypes, '',null);
	var curDate = cur_Date+' '+cur_Time;
	billOpenDate= (billOpenDate	== null || billOpenDate=='') ? curDate : billOpenDate;
	addOrderDialog.billOpenDate = billOpenDate;
	orderTableInit(true);
	//addToPresTable(order);
	//showOrderTablePatientAmounts(0,true);
	//addOrderDialog.allowFinalization = true;
	//addOrderDialog.restictionType = 'Doctor';
	//addOrderDialog.multiVisitPackage = true;
}

var newlyAddedItems = [];
var newlyAddedApprovalDetailsIds = [];
var newlyAddedApprovalLmtTypes = [];
var newlyAddedApprovalLmtValues = [];
var noOfNewlyAddedItems = 0;

function addOrders(order) {
//TODO if needed
//addToPresTable(order);

order.planIds = null; //getPlanIds();
	var type = order.itemType;

	if (type == 'Laboratory' || type == 'Radiology') {
		index = addInvestigations(order);
	} else if (type == 'Service') {
		index = addServices(order);
	} else if (type == 'Other Charge') {
		index = addOtherServices(order, "OCOTC");
	} else if (type == 'Implant') {
		index = addOtherServices(order, "IMPOTC");
	} else if (type == 'Consumable') {
		index = addOtherServices(order, "CONOTC");
	} else if (type == 'Doctor') {
		index = addDoctor(order);
	} else if (type == 'Equipment') {
		index = addEquipment(order);
	} else if (type == 'Package') {
		index = addPackages(order);
	} else if (type == 'Operation') {
		index = infoOfOrder(order);
	}

	setNewlyAddedItems(order.itemId);

	//getBillChargeClaims("new_visit", document.mainform);

	//estimateTotalAmount();

	var mrNo = document.getElementById('mr_no').value;
	var visitId = document.getElementById('visitId').value;
	var visitOrgId = document.getElementById('organization_details').value;

	var url = cpath + '/pages/order/ajaxCallOnAddDialysisOrderItem.do?_method=getDialysisOrderSponsorAmounts&mr_no='+mrNo+
				'&serviceGrp='+order.itemServiceGroupId+'&itemId='+encodeURIComponent(order.itemId)+
				'&visitOrgId='+visitOrgId+'&quantity='+ order.quantity+'&type='+order.itemType+'&visit_id='+visitId+
				'&chargeType='+order.chargeType+
				'&newlyAddedItems='+newlyAddedItems+'&newlyAddedApprovalDetailsIds='+newlyAddedApprovalDetailsIds+
				'&newlyAddedApprovalLmtTypes='+newlyAddedApprovalLmtTypes+
				'&newlyAddedApprovalLmtValues='+newlyAddedApprovalLmtValues;

	var ajaxRequestForDialysisOrderItem = YAHOO.util.Connect.asyncRequest('POST', url,
			{
				success: OnGetDialysisOrderItemCharges,
				failure: OnGetDialysisOrderItemChargesFailure,
				argument: []
			}
		)

	updateNewItemTotals();
	return true;
}

function setNewlyAddedItems(itemId){
	newlyAddedItems[noOfNewlyAddedItems] = itemId;
	newlyAddedApprovalDetailsIds[noOfNewlyAddedItems] = 0;
	newlyAddedApprovalLmtTypes[noOfNewlyAddedItems]='X';
	newlyAddedApprovalLmtValues[noOfNewlyAddedItems]=0;


	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "newly_added_item_approval_detail_ids");
    inp0.setAttribute("id", "newly_added_item_approval_detail_ids"+noOfNewlyAddedItems);
    inp0.setAttribute("value", 0);

    var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "hidden");
    inp1.setAttribute("name", "newly_added_item_approval_limit_values");
    inp1.setAttribute("id", "newly_added_item_approval_limit_values"+noOfNewlyAddedItems);
    inp1.setAttribute("value", 0);

    var dialysisOrderForm = document.mainform;
	dialysisOrderForm.appendChild(inp0);
	dialysisOrderForm.appendChild(inp1);

	noOfNewlyAddedItems++;
}

function handleNewlyAddedItems(rowObj , newEl) {
	if(newEl.value == 'Y') {
		gIsNewItemDeleted=true;
		noOfNewlyAddedItems--;
	}
}

function OnGetDialysisOrderItemCharges(response) {
	if (response.responseText != undefined) {
		var chargeMap = eval('(' + response.responseText + ')');
		setOrderRespectiveAmounts(chargeMap);
	}
}

function setOrderRespectiveAmounts(chargeMap) {
	var table = document.getElementById('orderTable'+0);
	var numRows = table.rows.length;

	//for (var id=1; id < numRows-1 ;id++) {
		var row = table.rows[numRows-2];
		//var chargeId = null;
		//chargeId = "_"+id;
		if(chargeMap != undefined){
			var insClaimAmt = formatAmountPaise(getPaise(chargeMap.insurance_claim_amount));
			var orderAmt = formatAmountPaise(getPaise(chargeMap.amount));
			//if(priority == 1){
				getElementByName(row, 'priClaimAmt').value = insClaimAmt;
			//}else{
				//getElementByName(row, 'secClaimAmt').value = insClaimAmt;
			//}
			setNodeText(row.cells[PATIENT_AMT_COL], formatAmountPaise(getPaise(orderAmt-insClaimAmt)));
			getElementByName(row, 'orderPatientAmt').value = formatAmountPaise(getPaise(orderAmt-insClaimAmt));

			setNodeText(row.cells[AMOUNT_COL], orderAmt);
			getElementByName(row, 'orderAmount').value = orderAmt;

			newlyAddedApprovalDetailsIds[noOfNewlyAddedItems-1] = chargeMap.approval_detail_id;
			newlyAddedApprovalLmtTypes[noOfNewlyAddedItems-1] = chargeMap.limit_type;
			if(chargeMap.limit_type == 'Q')
				newlyAddedApprovalLmtValues[noOfNewlyAddedItems-1] = chargeMap.act_quantity;
			else
				newlyAddedApprovalLmtValues[noOfNewlyAddedItems-1] = chargeMap.insurance_claim_amount;

			var idx = noOfNewlyAddedItems-1;
			document.getElementById("newly_added_item_approval_detail_ids"+idx).value = newlyAddedApprovalDetailsIds[noOfNewlyAddedItems-1];
			document.getElementById("newly_added_item_approval_limit_values"+idx).value = newlyAddedApprovalLmtValues[noOfNewlyAddedItems-1];

		}
	//}
	updateNewItemTotals();
}

function OnGetDialysisOrderItemChargesFailure() {
}
function initEditDialog() {
	editDialog = new YAHOO.widget.Dialog("editDialog", { width:"600px",
			context: ["orderTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("editDialog").style.display = 'block';
	editDialog.render();
	subscribeEscKeyEvent(editDialog, cancelEdit);
	editDialog.cancelEvent.subscribe(onEditDialogCancel);
}

function cancelEdit() {
	editDialog.cancel();
	rowUnderEdit = undefined;
}

function showEditDialog(imgObj) {
	editDialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	var newEl = getElementByName(row, 'new');
	var type = getElementByName(row, "existingtype").value;
	var newType =  getElementByName(row, "type").value;
	var finStatus = getElementByName(row, "finStatus").value;
	var newFinStatus = getElementByName(row, "newFinStatus").value;
	var isFinalizable = (type == 'Equipment') && (finStatus == 'N');		// orig fin status in db
	var isFinalized = (type == 'Equipment') && (newFinStatus == 'F');
	var conductingDoctorMandatory = getElementByName(row, "conducting_doc_mandatory");
	var itemID = getElementByName(row, "item_id").value;

	if(newType == 'test'|| newType == 'service') {
		document.getElementById('eConductingDoc').style.display = 'table-row';
		function onSelectEConductingDoc(sType, aArgs) {
			var doctor = aArgs[2];
			document.editForm.eConducting_doctorId.value = doctor[1];
		}
		document.editForm.eConducting_doctor.value = '';
		document.editForm.eConducting_doctorId.value = '';
		var payee_doctor_id = getElementByName(row, newType+'.'+"payee_doctor_id").value;
		var filter = getElementByName(row, "addedItemType");
		if (newType == 'test')
			addOrderDialog.initOrderDoctorAutoComplete('eConducting_doctor', addOrderDialog.doctorList, onSelectEConductingDoc, 'dept_id', filter.value);
		if (newType == 'service')
			addOrderDialog.initOrderDoctorAutoComplete('eConducting_doctor', addOrderDialog.doctorList, onSelectEConductingDoc);
		if (payee_doctor_id != '') {
			var doctor = findInList(doctorsList, 'doctor_id', payee_doctor_id);
			document.editForm.eConducting_doctor.value = doctor['doctor_name'];
			document.editForm.eConducting_doctorId.value = payee_doctor_id;
			if (conductingDoctorMandatory.value == 'O') {
				document.editForm.eConducting_doctor.disabled = false;
			} else {
				document.editForm.eConducting_doctor.disabled = true;
			}
		} else {
			if (conductingDoctorMandatory.value == 'O')
				document.editForm.eConducting_doctor.disabled = false;
			else
				document.editForm.eConducting_doctor.disabled = true;
		}
	} else {
		document.getElementById('eConductingDoc').style.display = 'none';
	}
	// editing an existing order

	var remarksElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"remarks");
	if (remarksElmt != null) document.editForm.eRemarks.value = remarksElmt.value;
	var prescDrIdElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"presDocId");
	if (prescDrIdElmt != null) document.editForm.ePresDocId.value = prescDrIdElmt.value;

	document.editForm.ePrescribedBy.value = getElementByName(row, "presDocName").value;
	// the following is to prevent clearing of the autocomp on blur
	presAutoComp._bItemSelected = true;
	var qtyElmt = getElementByName(row,( newEl.value == 'Y' ? newType+".quantity" : "quantity"));

	if (isFinalizable || isFinalized) {
		var fromDateElmt = getElementByName(row, "fromDate");
		if (fromDateElmt != null) document.editForm.eFromDate.value = fromDateElmt.value;

		var fromTimeElmt = getElementByName(row, "fromTime");
		if (fromTimeElmt != null) document.editForm.eFromTime.value = fromTimeElmt.value;

		var toDateElmt = getElementByName(row, "toDate");
		if (toDateElmt != null) document.editForm.eToDate.value = toDateElmt.value;

		var toTimeElmt = getElementByName(row, "toTime");
		if (toTimeElmt != null) document.editForm.eToTime.value = toTimeElmt.value;
	}
	document.editForm.eFinalized.checked = isFinalized;
	document.editForm.eFinalized.disabled = !isFinalizable;
	document.editForm.eFromDate.disabled = !isFinalizable;
	document.editForm.eFromTime.disabled = !isFinalizable;
	document.editForm.eToDate.disabled = !isFinalizable;
	document.editForm.eToTime.disabled = !isFinalizable;

	var urgentElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"urgent");

	if(type == 'Laboratory' || type == 'Radiology' || newType == 'test')
		document.editForm.eurgent.disabled = false;
	else
		document.editForm.eurgent.disabled = true;

	if (urgentElmt != null && urgentElmt.value == 'S')
		document.editForm.eurgent.checked = true;
	else
		document.editForm.eurgent.checked = false;

	if(multiPlanExists) {
		document.getElementById("ePriPreAuthLbl").style.display = 'block';
		document.getElementById("ePreAuthLbl").style.display = 'none';
		document.getElementById("ePriPreAuthModeLbl").style.display = 'block';
		document.getElementById("ePreAuthModeLbl").style.display = 'none';
	}else{
		document.getElementById("ePriPreAuthLbl").style.display = 'none';
		document.getElementById("ePreAuthLbl").style.display = 'block';
		document.getElementById("ePriPreAuthModeLbl").style.display = 'none';
		document.getElementById("ePreAuthModeLbl").style.display = 'block';
	}

	var priorAuthElmt = getElementByName(row, "prior_auth_id");
	if (priorAuthElmt != null) document.editForm.ePriorAuthId.value = priorAuthElmt.value;

	var secPriorAuthElmt = getElementByName(row, "sec_prior_auth_id");
	if(secPriorAuthElmt != null) document.editForm.eSecPriorAuthId.value = secPriorAuthElmt.value;

	var priorAuthModeElmt = getElementByName(row, "prior_auth_mode_id");
	if (priorAuthModeElmt != null) document.editForm.ePriorAuthMode.value = priorAuthModeElmt.value;

	var secPriorAuthModeElmt = getElementByName(row, "sec_prior_auth_mode_id");
	if(secPriorAuthModeElmt != null) document.editForm.eSecPriorAuthMode.value = secPriorAuthModeElmt.value;

	var toothNum = getElementByName(row, "s_tooth_number").value;
	if (empty(type)) {
		document.getElementById('edToothNumBtnDiv').style.display = empty(toothNum) ? 'none' : 'block';
		document.getElementById('edToothNumDsblBtnDiv').style.display = empty(toothNum) ? 'block' : 'none';
	} else {
		// do not allow to edit the tooth number for the services which are already saved.
		document.getElementById('edToothNumBtnDiv').style.display = 'none';
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'block';
	}
	var nos = toothNum.split(',');
	var tooth_numbers_text = '';
	var index = 0;
	for (var k=0; k<nos.length; k++) {
		if (index > 0) tooth_numbers_text += ',';
		if (index%10 ==0)
			tooth_numbers_text += '\n';

		tooth_numbers_text += nos[k];
		index++;
	}
	document.getElementById('edToothNumberDiv').textContent = tooth_numbers_text;
	document.getElementById('ed_tooth_number').value = toothNum;
	var toothNumReqEl = getElementByName(row, 's_tooth_num_required');
	if (toothNumReqEl != null) document.editForm.ed_tooth_num_required.value = toothNumReqEl.value;

	if(newEl.value == 'Y') {
		document.getElementById("ePriAuthRowId").style.visibility = "visible";
		if(multiPlanExists)
			document.getElementById("eSecPriAuthRowId").style.visibility = "visible";
	}
	else {
		document.getElementById("ePriAuthRowId").style.visibility = "hidden";
		document.getElementById("eSecPriAuthRowId").style.visibility = "hidden";
	}

/*	var billStatusEl = getElementByName(row, 'bill_status');
	if (billStatusEl && billStatusEl.value != 'A') {
		// disable changing of presc doctor if bill is not open. Presc doctor change
		// affects payment rule selection, so this should not be allowed.
		document.editForm.ePrescribedBy.disabled = true;
	} else {
		document.editForm.ePrescribedBy.disabled = false;
	}*/

	showCondDoctorsOfPackInEditDialog(row);

	rowUnderEdit = row;
	YAHOO.util.Dom.addClass(row, 'editing');
	editDialog.show();

	document.editForm.eRemarks.focus();
}

function onChangeFilter(filterObj) {
	var filterStatus = document.mainform.status.value;
	var filterTpaId = document.mainform.tpa_id.value;
	var filterPeriod = document.mainform.period.value;

	filterApprovals();
}

function filterApprovals() {
	var num = document.mainform.numapprovals.value;
   	var table = document.getElementById("patientApprovals");
	var filterStatus = document.mainform.filterstatus.value;
	var filterTpaId = document.mainform.tpa_id.value;
	var filterPeriod = document.mainform.filterperiod.value;

	for (var i=1; i<=num; i++) {
		var row = table.rows[i];
		var aprovalStatus = getElementByName(row, 'status').value;
		var aprovalTpaId = getElementByName(row, 'sponsor_id').value;
		var aprovalPeriod = getElementByName(row, 'period').value;

		var show = true;
		if ((filterStatus != "") && (filterStatus != aprovalStatus))
			show = false;
		if ((filterTpaId != "") && (filterTpaId != aprovalTpaId))
			show = false;
		if ((filterPeriod != "") && (filterPeriod != aprovalPeriod))
			show = false;

		if (show) {
			row.style.display = "";
		} else {
			row.style.display = "none";
		}
	}

}

function updateNewItemTotals() {
	var newItemAmount = getTotalNewOrdersAmount(0);
	var newItemPatAmt = getTotalNewOrdersPatientAmount(0);

	document.getElementById('lblNewAmt').textContent = newItemAmount;
	document.getElementById('lblNewPatAmt').textContent = formatAmountPaise(getPaise(newItemPatAmt));
	document.getElementById('lblNewSponAmt').textContent = formatAmountPaise(getPaise(newItemAmount - formatAmountPaise(getPaise(newItemPatAmt))));

	document.getElementById('lblNetPatDue').textContent = formatAmountPaise(getPaise(newItemPatAmt +
				parseFloat(document.getElementById('lblTotPatAmt').textContent)));

	document.getElementById('lblGrosPatDue').textContent = formatAmountPaise(getPaise(newItemPatAmt +
				parseFloat(document.getElementById('lblPrevBillsPatDue').textContent)));

	//var totPatAmt = document.getElementById('lblTotPatAmt').textContent;
	// set payment pannel total amount
	//setPaymentAmount();
}

function updateSavedItemsTotalsOnDelete(rowObj , undeleteFrom) {

	var totAmt = parseFloat(document.getElementById('lblTotAmt').textContent);
	var totSponAmt = parseFloat(document.getElementById('lblTotSponAmt').textContent);
	var totPatAmt = parseFloat(document.getElementById('lblTotPatAmt').textContent);

	var totNewItemPatAmt = parseFloat(document.getElementById('lblNewPatAmt').textContent);

	var totGrossPatDue = parseFloat(document.getElementById('lblGrosPatDue').textContent);
	var totPrevBillsPatDue = parseFloat(document.getElementById('lblPrevBillsPatDue').textContent);

	var itemAmt=parseFloat(rowObj.cells[AMOUNT_COL].innerHTML);
	var itemPatAmt=parseFloat(rowObj.cells[PATIENT_AMT_COL].innerHTML);
	var itemSponAmt=parseFloat( formatAmountPaise(getPaise(itemAmt-itemPatAmt)) );

	var cancelledStatus = getElementByName(rowObj, 'cancelled').value;
	var isNew = getElementByName(rowObj, 'new').value;
	if(isNew != 'Y') {
	   	if(cancelledStatus == 'IC') {
	   		document.getElementById('lblTotAmt').textContent = formatAmountPaise(getPaise(totAmt-itemAmt));
			document.getElementById('lblTotSponAmt').textContent = formatAmountPaise(getPaise(totSponAmt-itemSponAmt));
			document.getElementById('lblTotPatAmt').textContent = formatAmountPaise(getPaise(totPatAmt-itemPatAmt));

			document.getElementById('lblNetPatDue').textContent = formatAmountPaise(getPaise(totNewItemPatAmt+(totPatAmt-itemPatAmt)));

			document.getElementById('lblGrosPatDue').textContent = formatAmountPaise(getPaise(totGrossPatDue-itemPatAmt))
			document.getElementById('lblPrevBillsPatDue').textContent = formatAmountPaise(getPaise(totPrevBillsPatDue-itemPatAmt))
	   	} else if (cancelledStatus == '' && undeleteFrom == 'IC') {
	   		document.getElementById('lblTotAmt').textContent = formatAmountPaise(getPaise(totAmt+itemAmt));
			document.getElementById('lblTotSponAmt').textContent = formatAmountPaise(getPaise(totSponAmt+itemSponAmt));
			document.getElementById('lblTotPatAmt').textContent = formatAmountPaise(getPaise(totPatAmt+itemPatAmt));

			document.getElementById('lblNetPatDue').textContent = formatAmountPaise(getPaise(totNewItemPatAmt+(totPatAmt+itemPatAmt)));

			document.getElementById('lblGrosPatDue').textContent = formatAmountPaise(getPaise(totGrossPatDue+itemPatAmt))
			document.getElementById('lblPrevBillsPatDue').textContent = formatAmountPaise(getPaise(totPrevBillsPatDue+itemPatAmt))
	   	}
   	}
}

function setPaymentAmount() {
	var newItemPatAmt = getTotalNewOrdersPatientAmount(0);

	document.getElementById("totPayingAmt0").value =
		formatAmountPaise(getPaise(
			parseFloat(newItemPatAmt) +
			parseFloat(document.getElementById('lblPrevBillsPatDue').textContent)
		));
}

function getTotalAmount() {
	var patAmtFrmNewOrd = getTotalNewOrdersPatientAmount(0);
	var patAmtFrmPrevBills = document.getElementById("h_prevBillsTotPatAmt").value;

	return parseFloat(patAmtFrmNewOrd) + parseFloat(patAmtFrmPrevBills);
}

function getTotalAmountDue() {
	var patAmtFrmNewOrd = getTotalNewOrdersPatientAmount(0);

	return formatAmountPaise(getPaise(
			parseFloat(patAmtFrmNewOrd) +
			parseFloat(document.getElementById('lblPrevBillsPatDue').textContent)
		));
}

function validateSave(){
	var ratePlanId = document.getElementById("organization_details").value;
	var amtObj = getIndexedFormElement(documentForm, "totPayingAmt", 0);
	var visitDateObj = document.getElementById("visitDate");
	var visitTimeObj = document.getElementById("visitTime");

	var mrNo = document.getElementById("mr_no").value;

	if(mrNo == ''){
		alert("Please Enter Mr No..");
		return false;
	}

	if(null != visitDateObj && empty(visitDateObj.value)){
		alert("Please select Valid Visit Date");
		visitDateObj.focus();
		return false;
	}
	if( null != visitTimeObj && empty(visitTimeObj.value)){
		alert("Please select Valid Visit Time");
		visitTimeObj.focus();
		return false;
	}
	if(!validateForFutureDateAndTime(visitDateObj,visitTimeObj))
		return false;

	if(ratePlanId == ''){
		alert("Please select rate plan..");
		return false;
	}
	if ((null != amtObj) && (amtObj.value != "")) {
		if (!validateAmount(amtObj, getString("js.laboratory.radiology.billpaymentcommon.pay.validamount")))
		return false;
	}
	if(!validateRefundPaymentAmount(amtObj))
		return false;
	
	if(!validatePaymentTagFields())
		return false;
	
	document.mainform.submit();
}

function validateForFutureDateAndTime(visitDateObj,visitTimeObj){
	if(null != visitID && '' == visitID && null != visitDateObj && !empty(visitDateObj.value) &&
				null != visitTimeObj && !empty(visitTimeObj.value)){
		var visitDt = visitDateObj.value;
		var visitTm = visitTimeObj.value;
		var visitTmArr =visitTm.split(':');

		var curddate= new Date();
		var curddate1 = curddate.getTime();

		var visitDate =new Date(getDateFromField(visitDateObj));
		visitDate.setHours(visitTmArr[0],visitTmArr[1],0);
		var visitDate1=visitDate.getTime();

		if (visitDate1 > curddate1) {
			alert("Visit Date/Time cannot be greater than Current Date/Time");
			visitDateObj.focus();
			return false;
		}
	}return true;
}

function validateRefundPaymentAmount(amtObj) {
	if(prevBillsReceiptsTotal == null || prevBillsReceiptsTotal == '')
		prevBillsReceiptsTotal=0;

	if(document.getElementById("paymentType").value == 'refund') {
		if(parseFloat(prevBillsReceiptsTotal) <= 0) {
			alert("No payments are there. Unable to process Refund");
			return false;
		}

		if ((null != amtObj) && (amtObj.value != "") ) {
			if(amtObj.value > parseFloat(prevBillsReceiptsTotal)) {
				alert("Refund amount can not be greater than "+prevBillsReceiptsTotal);
				return false;
			}
		}
	}
	return true;
}

function setRequiredAmtsToOrderGrid() {
	var table = document.getElementById('orderTable'+0);
	var numRows = table.rows.length-2;

	for (var id=0; id < numRows ;id++) {
		var row = table.rows[id+1];

		var chargeId = allOrdersJSON[id].charge_id;
		var amount = allOrdersJSON[id].amount;
		var sponsorAmt = ordrClaimAmtsMapJSON[chargeId]
    var patAmt = amount-sponsorAmt;

		setNodeText(row.cells[AMOUNT_COL], formatAmountPaise(getPaise(amount)));
		setNodeText(row.cells[PATIENT_AMT_COL], formatAmountPaise(getPaise(patAmt)));
	}
}

function resetHiddenFields() {
	var cancelledEl = document.getElementsByName("cancelled");
	if(cancelledEl != null && cancelledEl != 'undefined') {
		for(var i=0; i<cancelledEl.length; i++) {
			cancelledEl[i].value = '';
		}
	}
}