gFrom_pending_prescriptions = (typeof gFrom_pending_prescriptions == 'undefined') ? '' : gFrom_pending_prescriptions;
var chargesAdded = 0;
var index = 0;
var visitingDoctor = '';
var operationDoctor = '';
var anaesteciaDoctor = '';
var newOrderCount = 0;
var addOrderDialog;
var rowUnderEdit = null;
var oPRowUnderEdit = null;
var presAutoComp = null;
var opPresAutoComp = null;
var patientInfo = null;
var newOrdersMap = null;
var gBillNo;


function init() {

	if( mrno != ''){
		initEditDialog();
		initOpEditDialog();
		orderTableInit(false);
		initCancelReconductiontestDialog();

		var doctors = { "doctors": doctorsList };
		var anaList = filterList(doctorsList, 'dept_id', 'DEP0002');
		var anaesthetists = { "doctors": anaList };

		if (patientType != '') {
			addOrderDialog = new Insta.AddOrderDialog('btnAddItem',
				rateplanwiseitems, rateplanwiseoperationitems,
				addOrder,
				doctors, anaesthetists, patientType, bedType, orgId,
				prescribingDocName, prescribingDoctor, regdate, regtime,
				allowBackDateBillActivities,fixedOtCharges, null, forceSubGroupSelection,regPref,anaeTypes,document.getElementById('patientid').value);

			addOrderDialog.setValidateTestAdditionalInfo(true);

			addOrderDialog.setInsurance((tpaId != ''), planId);
			var billOk = selectBill(document.mainform.billNo.value); // multivisit package
			setBillDetails();
			addOrderDialog.allowFinalization = (billType == 'C');

			if (document.mainform.billNo.value != '')
				getItems(document.mainform.billNo.value);

			if (!billOk) {
				showPrescribedItems();
			} else {
				orderPrescriptions(document.mainform.patientid.value);
			}
			// TODO: this should not be called like this: use req attribute and fix order dialog also
			patientInfo = getVisitDoctorConsultationDetails(document.getElementById("mrno").value);
		}

		presAutoComp = doctorAutoComplete('ePrescribedBy', 'ePrescribedByContainer', doctors, document.editForm);
		opPresAutoComp =
			doctorAutoComplete('eOpPrescribedBy', 'eOpPrescribedByContainer', doctors, document.oPeditForm);

		if (patientType == '') {
			document.patientSearch.patient_id.focus();
		} else {
			document.mainform.billNo.focus();
		}
		initNewAmts();
		initPkgValueCapDialog();
	}
	gBillNo= document.mainform.billNo.value;


}

function showPrescribedItems() {
	var itemsNotAdded = new Array();
	for (var i = 0; i < gTestPrescriptions.length; i++) {
		var pres = gTestPrescriptions[i];
		itemsNotAdded.push({type:"Investigation", name: pres.name});
	}
	for (var i = 0; i < gServicePrescriptions.length; i++) {
		var pres = gServicePrescriptions[i];
		itemsNotAdded.push({type:"Service", name: pres.name});
	}
	for (var i = 0; i < gDietPrescriptions.length; i++) {
		var pres = gDietPrescriptions[i];
		itemsNotAdded.push({type:"Meal", name: pres.name});
	}
	for (var i = 0; i < gConsultationPrescriptions.length; i++) {
		var pres = gConsultationPrescriptions[i];
		itemsNotAdded.push({type:"Doctor", name: pres.name});
	}
	if (itemsNotAdded.length > 0) {
		var msg = getString("js.order.common.prescribeditems.notadd");
		msg+="\n";
		msg+=getString("js.order.common.createnewbill");
		alert(msg);
		for (var i=0; i < itemsNotAdded.length; i++) {
			msg += "\n";
			msg += itemsNotAdded[i].type + ": " + itemsNotAdded[i].name;
		}
		alert(msg);
	}
}
function getTotalPrescriptions() {
	var totalPrescriptions = 0;
	if (filter == 'Radiology' || filter == 'Laboratory' || filter == '')
		totalPrescriptions += gTestPrescriptions.length;
	if (filter == 'Service' || filter == '')
		totalPrescriptions += gServicePrescriptions.length;
	if (filter == '') {
		totalPrescriptions += gDietPrescriptions.length;
		totalPrescriptions += gConsultationPrescriptions.length;
	}
	return totalPrescriptions;
}

function selectBill(selectedBill) {
	if (empty(selectedBill) || selectedBill == 'new' || selectedBill == 'newInsurance') {
		return true;
	}
	var totalPrescriptions = getTotalPrescriptions();
	if (totalPrescriptions <= 0) {
		return true;
	}
	var mvPackageId = getMultiVisitPackageBillDetails(selectedBill);
	var isMultiVisitPackageBill = !empty(mvPackageId);
	if (!isMultiVisitPackageBill) {
		return true;
	}
	setSelectedIndex(document.mainform.billNo, 'new');
	// Check if the new bill was actually selected
	if (document.mainform.billNo.options) {
		var index = document.mainform.billNo.selectedIndex;
		if (index >= 0) {
			var selValue = document.mainform.billNo.options[index].value;
			return (selValue == 'new');
		}
	}
	return false;
}

function orderPrescriptions(patientId) {

	itemsNotAvailable = new Array();

	// var totalPrescriptions = 0;
	var totalPrescriptions = getTotalPrescriptions();
	if (totalPrescriptions > 0 && empty(gFrom_pending_prescriptions)) {
		var ok = confirm("There are prescriptions available for the patient.\n" +
				"Do you want to automatically order these items?");
		if (!ok)
			return;
	}

	if (filter == 'Radiology' || filter == 'Laboratory' || filter == '') {
		// rowsLen: this is used to keep track of package row index.
		var rowsLen = document.getElementById('orderTable0').rows.length-1;
		for (var i=0; i<gTestPrescriptions.length; i++) {
			// find the item from the list
			var pres = gTestPrescriptions[i];

			var item = findInList2(rateplanwiseitems.result, 'type', 'Laboratory', 'id', pres.test_id);
			if (item == null)
				item = findInList2(rateplanwiseitems.result, 'type', 'Radiology', 'id', pres.test_id);
			if (item == null) {
				item = findInList2(rateplanwiseitems.result, 'type', 'Package', 'id', pres.test_id);
				if (item != null && item.department != 'DIAG')
					item = null;
			}
			if (item == null) {
				item = findInList2(rateplanwiseitems.result, 'type', 'Order Sets', 'name', pres.name);
			}


			if (item == null) {
				itemsNotAvailable.push({type: 'Investigation', name: pres.name});
			} else if (item.type == 'Order Sets') {
				var index = 0;
				var chargeType;
				var packageComponentDetails = addOrderDialog.getPackageComponents(item.id, null, false, item.type);
				packageDetails = packageComponentDetails['packComponentDetails'];
				for (var j=0; j<packageDetails.length; j++) {
					var packComp = packageDetails[j];
					doPrescriptionOrder(patientId, packComp.item_type, packComp.activity_id,
							packComp.activity_description, pres, "N", "", "", packComp.mandate_additional_info);
				}
			} else  if (item.type == 'Package') {
				var packageComponentDetails = addOrderDialog.getPackageComponents(item.id);
				packageDetails = packageComponentDetails['packComponentDetails'];
				addOrderDialog.packageDetails = packageDetails;
				for (var j=0; j<packageDetails.length; j++) {
					var packComp = packageDetails[j];
					if (packComp.conducting_doc_mandatory) {
						var docVisitInnerTable = document.getElementById("innerCondDocForPack");
						var docInnerRow = docVisitInnerTable.insertRow(-1);
						var innerCell = docInnerRow.insertCell(-1);
						innerCell.appendChild(makeHidden("package.condDoctor", "", "" ));
						innerCell.appendChild(makeHidden("package.activity_id", "", packComp.activity_id ));
						innerCell.appendChild(makeHidden("package.packIdForCondDoc", "", item.id));
						innerCell.appendChild(makeHidden("package.packPrescIdForCondDoc", "", "_"+rowsLen));
						innerCell.appendChild(makeHidden("package.packageActivityIndex", "", j+''));
						innerCell.appendChild(makeHidden("package.mainRowIndex", "", rowsLen));
						innerCell.appendChild(makeHidden("package.packageItemName", "", packComp.item_name));
						innerCell.appendChild(makeHidden("package.packageName", "", packComp.package_name));
						innerCell.appendChild(makeHidden("package.packageItemType", "", packComp.item_type));
					}
				}

				// normal package or single item, we assume that we cannot have additional details such
				// as operation and doctor visits for diag packages.

				doPrescriptionOrder(patientId, item.type, item.id, pres.name, pres, item.prior_auth_required, item.department, 'N','');
			} else {
				// normal package or single item, we assume that we cannot have additional details such
				// as operation and doctor visits for diag packages.
				doPrescriptionOrder(patientId, item.type, item.id, pres.name, pres, item.prior_auth_required, item.department, item.mandate_additional_info, item.additional_info_reqts);
			}

			if (item != null) rowsLen = rowsLen+1;
		}
	}

	if (filter == 'Service' || filter == '') {
		for (var i=0; i < gServicePrescriptions.length; i++) {
			// find the item from the list
			var pres = gServicePrescriptions[i];
			var item = findInList2(rateplanwiseitems.result, 'type', 'Service', 'id', pres.service_id);
			if (item == null) {
				itemsNotAvailable.push({type: 'Service', name: pres.name});
			} else {
				doPrescriptionOrder(patientId, 'Service', item.id, pres.name, pres, item.prior_auth_required, item.department, 'N','');
			}
		}
	}

	if (filter == '') {
		if (gDietPrescriptions.length > 0) {
			for (var i=0; i < gDietPrescriptions.length; i++) {
				var pres = gDietPrescriptions[i];
				var item = findInList2(rateplanwiseitems.result, 'type', 'Meal', 'name', pres.name);
				if (item == null) {
					itemsNotAvailable.push({type: 'Meal', name: pres.name});
				} else {
					doPrescriptionOrder(patientId, 'Meal', item.id, pres.name, pres, item.prior_auth_required, item.department, 'N','');
				}
			}
		}
		if (gConsultationPrescriptions.length > 0) {
			var doctorConsultationsStr = "";
			for (var i=0; i < gConsultationPrescriptions.length; i++) {
				var pres = gConsultationPrescriptions[i];
				var item = findInList2(rateplanwiseitems.result, 'type', 'Doctor', 'id', pres.cross_cons_doctor_id);

				if ( eClaimModule == 'Y' && patientType == 'o' ) {
					if( item != null ){
						doctorConsultationsStr += pres.name+"  ["+item.department+"] \n";
					}
				} else {
					if (item == null) {
						itemsNotAvailable.push({type: 'Doctor', name: pres.name});
					} else {
						doPrescriptionOrder(patientId, 'Doctor', item.id, pres.name, pres, item.prior_auth_required, item.department, 'N', '');
					}
				}
			}

			if (doctorConsultationsStr != "" && patientType == 'o') {

			alert(getString("js.order.common.crossreferrals.pending")+"\n"+getString("js.order.common.createseparatevisit.foreachdoctor")+ doctorConsultationsStr);
			}

		}


		if (gOperationPrescriptions.length > 0) {
			var msg = getString("js.order.common.followingitems.notadded");
			for (var i=0; i<gOperationPrescriptions.length; i++) {
				msg += "\n";
				msg += i+1 +") " + gOperationPrescriptions[i].operation_name;
			}
			alert(msg);
		}
	}

	if (itemsNotAvailable.length > 0) {
		var msg = getString("js.order.common.followingitems.notavailable");
		for (var i=0; i < itemsNotAvailable.length; i++) {
			msg += "\n";
			msg += itemsNotAvailable[i].type + ": " + itemsNotAvailable[i].name;
		}
		alert(msg);
	}
}


function doPrescriptionOrder(patientId, type, id, name, pres, priorAuthRequired, department, mandate_test_additional_info, additional_info_reqts) {

	var toothNum = "None";
	var toothNumrReq = 'N';
	var itemQty = 1;
	if (type == 'Service') {
		toothNumrReq = pres.tooth_num_required;
		if (tooth_numbering_system == 'U') {
			toothNum = pres.tooth_unv_number;
		} else {
			toothNum = pres.tooth_fdi_number;
		}
		itemQty = pres.service_qty;
	}
	var order = {itemType: type,
		itemId: 			id,
		itemName: 			name,
		remarks:    		pres.remarks,
		presDoctorName:   	pres.doctor_name,
		presDoctorId: 		pres.doctor_id,
		prescriptionRef: 	pres.pres_id,
		preAuthNo:			(empty(pres.pri_pre_auth_no) ? '' : pres.pri_pre_auth_no),
		preAuthModeNo:		(empty(pres.pri_pre_auth_mode_id) ? '' : pres.pri_pre_auth_mode_id),
		secPreAuthNo:		(empty(pres.sec_pre_auth_no) ? '' : pres.sec_pre_auth_no),
		secPreAuthModeNo:	(empty(pres.sec_pre_auth_mode_id) ? '' : pres.sec_pre_auth_mode_id),

		toDate: 	'',
		toTime: 	'',
		to: 		'',
		fromDate: 	'',
		fromTime: 	'',
		from: 		'',
		mealTiming: '',

		presDate:   formatDateTime(getServerDate()),
		presDatePart : formatDate(getServerDate()),
		addTo:		'orderTable0',
		units:      "",
		quantity:   itemQty,
		operationId : '',
		operationRef:	'',
		additionalDetails: [{operation_id: '', presId: pres.pres_id}],
		priorAuthReq : priorAuthRequired,
		toothNumberReq : toothNumrReq,
		toothNumber: toothNum,
		mandateTestAdditionalInfo : mandate_test_additional_info,
		additionalTestInfo   : additional_info_reqts
	};

	if (type == 'Meal') {
		order.fromDate = formatDate(new Date(pres.meal_date));
		order.fromTime = formatTime(new Date(pres.meal_time));
		if (order.fromTime != null)
			order.from = order.fromDate + ' ' + order.fromTime;
		else
			order.from = order.fromDate;
		order.mealTiming = pres.meal_timing;
	}

	if(null != patientId && !empty(patientId)){
		order.patientId = patientId;
	}

	if (type == 'Doctor') {
		if (order.fromTime != null)
			order.from = formatDateTime(getServerDate());
		order.chargeType = pres.head;
		order.chargeTypeDisplay = pres.chargehead_name;
	}
	addOrderDialog.orderItemType = type;
	addOrderDialog.orderItemId = id;
	addOrderDialog.orderItemName = name;
	addOrderDialog.orderItemDeptName = department;
	addOrderDialog.patientType = patientType;
	document.orderDialogForm.doctorCharge.value = -1;
	if(addOrderDialog.checkDuplicates())
		addOrderDialog.getCharge(order, false);
}

function doctorAutoComplete(field, dropdown, list, thisForm) {

	var localDs = new YAHOO.util.LocalDataSource(list,{ queryMatchContains : true });
	localDs.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	localDs.responseSchema = { resultsList : "doctors",
		fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
	};

	var autoComp = new YAHOO.widget.AutoComplete(field, dropdown, localDs);

	autoComp.prehightlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp.useIFrame = true;
	autoComp.formatResult = Insta.autoHighlight;

	var itemSelectHandler = function(sType, aArgs) {
		thisForm.ePresDocId.value =  aArgs[2][1];
	};

	autoComp.itemSelectEvent.subscribe(itemSelectHandler);
	return autoComp;
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


	// editing an existing order

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

	var remarksElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"remarks");
	if (remarksElmt != null) document.editForm.eRemarks.value = remarksElmt.value;
	var prescDrIdElmt = getElementByName(row, (newEl.value == 'Y' ? newType+".":'')+"presDocId")
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

	var priorAuthElmt = getElementByName(row, "prior_auth_id");
	if (priorAuthElmt != null) document.editForm.ePriorAuthId.value = priorAuthElmt.value;

	var priorAuthModeElmt = getElementByName(row, "prior_auth_mode_id");
	if (priorAuthModeElmt != null) document.editForm.ePriorAuthMode.value = priorAuthModeElmt.value;

	var secPriorAuthElmt = getElementByName(row, "sec_prior_auth_id");
	if (secPriorAuthElmt != null) document.editForm.eSecPriorAuthId.value = secPriorAuthElmt.value;

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

	var billStatusEl = getElementByName(row, 'bill_status');
	if (billStatusEl && billStatusEl.value != 'A') {
		// disable changing of presc doctor if bill is not open. Presc doctor change
		// affects payment rule selection, so this should not be allowed.
		document.editForm.ePrescribedBy.disabled = true;
	} else {
		document.editForm.ePrescribedBy.disabled = false;
	}
	showCondDoctorsOfPackInEditDialog(row);

	rowUnderEdit = row;
	YAHOO.util.Dom.addClass(row, 'editing');
	editDialog.show();

	document.editForm.eRemarks.focus();
}

function showCondDoctorsOfPackInEditDialog(row) {
	var table = document.getElementById('condDoctorsTableED'); // conducting doctors table in edit dialog.
	for (var i=1; i<table.rows.length; ) {
		table.deleteRow(i);
	}
	var curPrescIdEl = getElementByName(row, 'package.prescId');
	if (empty(curPrescIdEl)) {
		document.getElementById('condDoctorsTableED').style.display = 'none'; // hide when not found.
		return ;
	}

	var packageItems = document.getElementsByName('package.packageItemName');
	var packageName = document.getElementsByName('package.packageName');
	var packageTypes = document.getElementsByName('package.packageItemType');
	var packageActivityIds = document.getElementsByName('package.activity_id');
	var packageActivityIndexes = document.getElementsByName('package.packageActivityIndex');
	var packCondDoctors = document.getElementsByName('package.condDoctor');
	var packageIds = document.getElementsByName('package.packIdForCondDoc');
	var prescIdEls = document.getElementsByName('package.packPrescIdForCondDoc');


	document.getElementById('condDoctorsTableED').style.display = 'table';
	for (var i=0; i<prescIdEls.length; i++) {
		if (curPrescIdEl.value == prescIdEls[i].value) {
			var docRow = table.insertRow(-1);

			var cell = docRow.insertCell(-1);
			cell.appendChild(makeLabel(null, packageTypes[i].value));

			var cell = docRow.insertCell(-1);
			cell.appendChild(makeLabel(null, packageItems[i].value));
			cell.appendChild(makeHidden("ed_packageName", "ed_packageName"+i, packageName[i].value));
			cell.appendChild(makeHidden("ed_packageId", "ed_packageId"+i, packageIds[i].value));
			cell.appendChild(makeHidden("ed_packageItemName", "ed_packageItemName"+i, packageItems[i].value));
			cell.appendChild(makeHidden("ed_packageActId", "ed_packageActId"+i, packageActivityIds[i].value));
			// this is required to maintain the association to support the duplicate items.
			cell.appendChild(makeHidden("ed_packageActivityIndex", "ed_packageActivityIndex"+i, packageActivityIndexes[i].value+''));

			var condDoctor = "ed_packConductingDoctor"+i;
			var condCoctorContainer = condDoctor + "AcDropdown";

			var doctorName = '';
			if (!empty(packCondDoctors[i].value)) {
				doctorName = findInList(addOrderDialog.doctorList.doctors, "doctor_id", packCondDoctors[i].value).doctor_name;
			}

			var cell = docRow.insertCell(-1);
			cell.setAttribute("class", "yui-skin-sam");
			cell.innerHTML = '<div><input id="'+ condDoctor +'" name="ed_packConductingDoctor" type="text" value="'+doctorName+'"/><div id="'+ condCoctorContainer +'" ></div></div>';
			cell.appendChild(makeHidden("ed_packCondDoctorId", "ed_packCondDoctorId"+i, packCondDoctors[i].value));

			var itemType = packageTypes[i].value;
			var autoComp = null;;
			if (itemType == 'Laboratory' || itemType == 'Radiology') {
				autoComp = addOrderDialog.initOrderDoctorAutoComplete(condDoctor, addOrderDialog.doctorList, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("ed_packConductingDoctor", "");
						document.getElementById("ed_packCondDoctorId"+index).value = aArgs[2][1];
						}, "dept_id", (itemType == 'Laboratory' ? 'DEP_LAB': 'DEP_RAD'));
			} else {
				autoComp = addOrderDialog.initOrderDoctorAutoComplete(condDoctor, addOrderDialog.doctorList, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("ed_packConductingDoctor", "");
						document.getElementById("ed_packCondDoctorId"+index).value = aArgs[2][1];
						});
			}
			if (autoComp._elTextbox.value != '') {
				autoComp._bItemSelected = true;
				autoComp._sInitInputValue = autoComp._elTextbox.value;
			}

		}
	}
}

function cancelEdit() {
	editDialog.cancel();
	rowUnderEdit = undefined;
}

function onEditDialogCancel() {
	document.getElementById("ePriAuthRowId").style.visibility = "hidden";
	document.getElementById("eSecPriAuthRowId").style.visibility = "hidden";
	YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
}

function saveEdit() {
	var row = rowUnderEdit;
	var newEl = getElementByName(row, 'new');

	var editedEl = getElementByName(row, 'edited');
	if (editedEl)
		editedEl.value = 'Y';

	if (document.editForm.ePrescribedBy.value == '')
		document.editForm.ePresDocId.value = '';
	if (document.editForm.ed_tooth_num_required.value == 'Y' &&
		document.editForm.ed_tooth_number.value == '') {
		showMessage('js.order.common.required.toothnumber');
		return false;
	}

	setNodeText(row.cells[PRES_DOCTOR_COL], document.editForm.ePrescribedBy.value);
	setNodeText(row.cells[REMARKS_COL], document.editForm.eRemarks.value, 16);
	setNodeText(row.cells[PRE_AUTH_COL], document.editForm.ePriorAuthId.value, 16);
	if(multiPlanExists)
		setNodeText(row.cells[SEC_PRE_AUTH_COL], document.editForm.eSecPriorAuthId.value, 16);

	if(!document.editForm.eFromDate.disabled && !validateEditFields())
		return false;

	var tmType = getElementByName(row, "type").value;

	if (tmType == 'test' || tmType == 'service') {
		var condDocMandatory = getElementByName(row, 'conducting_doc_mandatory').value;
		var eConductingDoctor = document.editForm.eConducting_doctor.value;
		if (condDocMandatory == 'O') {
			if (eConductingDoctor == '') {
				showMessage('js.common.order.conducting.doctor.required');
				return false;
			} else {
				getElementByName(row, tmType+"."+"payee_doctor_id").value = document.editForm.eConducting_doctorId.value;
			}
		}
	}

	var remarksElmt = getElementByName(row, (newEl.value == 'Y' ? tmType+".":'')+"remarks");
	if (remarksElmt != null) remarksElmt.value = document.editForm.eRemarks.value;
	var prescDrIdElmt = getElementByName(row, (newEl.value == 'Y' ? tmType+".":'')+"presDocId")
	if (prescDrIdElmt != null) prescDrIdElmt.value = document.editForm.ePresDocId.value;
	getElementByName(row, "presDocName").value = document.editForm.ePrescribedBy.value;
	getElementByName(row, "newFinStatus").value = document.editForm.eFinalized.checked ? 'F' : 'N' ;
	getElementByName(row, "fromDate").value = document.editForm.eFromDate.value;
	getElementByName(row, "fromTime").value = document.editForm.eFromTime.value;
	getElementByName(row, "toDate").value = document.editForm.eToDate.value;
	getElementByName(row, "toTime").value = document.editForm.eToTime.value;
	if (document.editForm.ed_tooth_number) {
		var tooth_number = document.editForm.ed_tooth_number.value;
		if (getElementByName(row, "service.tooth_unv_number")) {
			getElementByName(row, "service.tooth_unv_number").value = tooth_number;
		} else if (getElementByName(row, "service.tooth_fdi_number")) {
			getElementByName(row, "service.tooth_fdi_number").value = tooth_number;
		}
		getElementByName(row, "s_tooth_number").value = tooth_number;
	}


	var urgentElmt = getElementByName(row, (newEl.value == 'Y' ? tmType+".":'')+"urgent");
	if (urgentElmt != null) urgentElmt.value = document.editForm.eurgent.checked ? 'S' : 'R' ;

	if(document.editForm.ePriorAuthId && insured){
		var priorAuthIdElmt = getElementByName(row, tmType+".prior_auth_id");
		if (priorAuthIdElmt != null) priorAuthIdElmt.value = document.editForm.ePriorAuthId.value;
		if (getElementByName(row, "prior_auth_id") != null)
			getElementByName(row, "prior_auth_id").value = document.editForm.ePriorAuthId.value;
		var priorAuthModeIdElmt = getElementByName(row, tmType+".prior_auth_mode_id");
		if (priorAuthModeIdElmt != null) priorAuthModeIdElmt.value = document.editForm.ePriorAuthMode.value;
		if (getElementByName(row, "prior_auth_mode_id") != null)
			getElementByName(row, "prior_auth_mode_id").value = document.editForm.ePriorAuthMode.value;
	}

	if(document.editForm.eSecPriorAuthId && insured){
		var secPriorAuthIdElmt = getElementByName(row, tmType+".sec_prior_auth_id");
		if (secPriorAuthIdElmt != null) secPriorAuthIdElmt.value = document.editForm.eSecPriorAuthId.value;
		if (getElementByName(row, "sec_prior_auth_id") != null)
			getElementByName(row, "sec_prior_auth_id").value = document.editForm.eSecPriorAuthId.value;
		var secPriorAuthModeIdElmt = getElementByName(row, tmType+".sec_prior_auth_mode_id");
		if (secPriorAuthModeIdElmt != null) secPriorAuthModeIdElmt.value = document.editForm.eSecPriorAuthMode.value;
		if (getElementByName(row, "sec_prior_auth_mode_id") != null)
			getElementByName(row, "sec_prior_auth_mode_id").value = document.editForm.eSecPriorAuthMode.value;
	}
	if (tmType == 'package') {

		var condDoctorInputEls = document.getElementsByName('ed_packConductingDoctor');
		for (var i=0; i<condDoctorInputEls.length; i++) {
			if (empty(condDoctorInputEls[i].value)) {
				showMessage('js.common.order.conducting.doctor.required');
				condDoctorInputEls[i].focus();
				return false;
			}

			var packId = document.getElementsByName('ed_packageId')[0].value;
			var prescId = getElementByName(row, "package.prescId").value;

			var packIdCondDocEl = document.getElementsByName('package.packIdForCondDoc');
			var prescIdCondDocEl = document.getElementsByName('package.packPrescIdForCondDoc');
			var rowIndexCondEl = document.getElementsByName('package.mainRowIndex');
			var packActivityEl = document.getElementsByName('package.packageActivityIndex');
			for (var j=0; j<packIdCondDocEl.length; j++) {
				if (packIdCondDocEl[j].value == packId
						&& prescIdCondDocEl[j].value == prescId
						&& parseInt(packActivityEl[j].value) == parseInt(document.getElementsByName('ed_packageActivityIndex')[i].value)) {
					document.getElementsByName('package.condDoctor')[j].value = document.getElementsByName('ed_packCondDoctorId')[i].value;
				}
			}
		}
	}


	/*
	 * todo: if we are finalizing, the charges may change. We need to calculate the differential
	 * charge from the original charge and set it as the "orderAmount" so that credit calculations
	 * will consider the incremental charge. We cannot assume that the previous from/to will match
	 * the old amount, since it could have edited/changed in the bill.
	 */
	editDialog.cancel();
	YAHOO.util.Dom.addClass(rowUnderEdit, 'edited');
}

function validateEditFields(){
	var valid = true;

	valid = valid && validateRequired(document.editForm.eFromDate,getString("js.order.common.startdate.required"));
	valid = valid && validateRequired(document.editForm.eFromTime,getString("js.order.common.starttime.required"));
	valid = valid && validateRequired(document.editForm.eToDate,getString("js.order.common.enddate.required"));
	valid = valid && validateRequired(document.editForm.eToTime,getString("js.order.common.endtime.required"));
	valid = valid && validateFromToDateTime(document.editForm.eFromDate, document.editForm.eFromTime,
			document.editForm.eToDate, document.editForm.eToTime, true, true)
	return valid;
}

function initOpEditDialog() {
	oPeditDialog = new YAHOO.widget.Dialog("oPeditDialog", { width:"700px",
			context: ["orderTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("oPeditDialog").style.display = 'block';
	oPeditDialog.render();
	subscribeEscKeyEvent(oPeditDialog, cancelOpEdit);
	oPeditDialog.cancelEvent.subscribe(clearAnaesthesiaDetails);
}

function clearAnaesthesiaDetails() {
	clearOperEditAnaesthesiaDetailsTable();
}

function showOpEditDialog(imgObj) {
	oPeditDialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);

	var tab = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'table');
	var opRow = YAHOO.util.Dom.getAncestorByTagName(tab, 'tr');

	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	var newEl = getElementByName(row, 'new');

	oPRowUnderEdit = opRow;
	rowUnderEdit = row;

	if (newEl.value == 'Y') {
		// newly added items cannot be edited. Disable.
		return false;
	} else {
		// editing an existing order
		document.oPeditForm.eRemarks.value = getElementByName(row, "remarks").value;

		document.oPeditForm.ePresDocId.value = getElementByName(row, "presDocId").value;
		document.oPeditForm.eOpPrescribedBy.value = getElementByName(row, "presDocName").value;
		opPresAutoComp._bItemSelected = true;

		document.oPeditForm.eOpFromDate.value = getElementByName(row, "fromDate").value;
		document.oPeditForm.eOpFromTime.value = getElementByName(row, "fromTime").value;
		document.oPeditForm.eOpToDate.value = getElementByName(row, "toDate").value;
		document.oPeditForm.eOpToTime.value = getElementByName(row, "toTime").value;

		var billStatusEl = getElementByName(row, 'bill_status');
		var finStatus = getElementByName(row, "finStatus").value;
		var isFinalized = (finStatus == 'F');		// orig fin status in db
		var billFinalized = (billStatusEl && billStatusEl.value != 'A');
		var isFixedOtCharges = (fixedOtCharges == 'Y');

		document.oPeditForm.eOpFromDate.value = getElementByName(row, "fromDate").value;
		document.oPeditForm.eOpFromTime.value = getElementByName(row, "fromTime").value;
		document.oPeditForm.eOpToDate.value = getElementByName(row, "toDate").value;
		document.oPeditForm.eOpToTime.value = getElementByName(row, "toTime").value;

		// if already finalized, disable finalization.
		document.oPeditForm.eCompleted.checked = isFinalized;
		document.oPeditForm.eCompleted.disabled = isFinalized || billFinalized;
		document.oPeditForm.eOpFromDate.disabled = isFinalized || billFinalized || isFixedOtCharges;
		document.oPeditForm.eOpFromTime.disabled = isFinalized || billFinalized || isFixedOtCharges;
		document.oPeditForm.eOpToDate.disabled = isFinalized || billFinalized || isFixedOtCharges;
		document.oPeditForm.eOpToTime.disabled = isFinalized || billFinalized || isFixedOtCharges;
		document.oPeditForm.eOpPrescribedBy.disabled = billFinalized;
		var anaethesiaTypes = getElementsByName(row, "anaesthesia_type_"+oPRowUnderEdit.rowIndex);
	/*	if (fixedOtCharges == 'Y') {
			document.oPeditForm.eOpFromDate.disabled = true;
			document.oPeditForm.eOpFromTime.disabled = true;
			document.oPeditForm.eOpToDate.disabled   = true;
			document.oPeditForm.eOpToTime.disabled   = true;
		} else {
			if (isFinalized || billFinalized) {
				document.oPeditForm.eOpFromDate.disabled = true;
				document.oPeditForm.eOpFromTime.disabled = true;
				document.oPeditForm.eOpToDate.disabled   = true;
				document.oPeditForm.eOpToTime.disabled   = true;
			} else {
				document.oPeditForm.eOpFromDate.disabled = false;
				document.oPeditForm.eOpFromTime.disabled = false;
				document.oPeditForm.eOpToDate.disabled   = false;
				document.oPeditForm.eOpToTime.disabled   = false;
			}
		}*/
		if(anaethesiaTypes != null && anaethesiaTypes.length > 0) {
			showOperationAnaesthesiaDetails(row,anaethesiaTypes,oPRowUnderEdit);
		} else {
			document.getElementById('opEditAnaesDetFieldset').style.display = 'none';
		}
	}
	opRow.className = "editing";
	oPeditDialog.show();

//	document.oPeditForm.eRemarks.focus();
}

function showOperationAnaesthesiaDetails(rowObj,anaethesiaTypes,opRow) {
	var anaethesiaTypesFromDateTime = getElementsByName(rowObj, "anaes_start_datetime_"+opRow.rowIndex);
	var anaethesiaTypesToDateTime = getElementsByName(rowObj, "anaes_end_datetime_"+opRow.rowIndex);
	var anaesthesiaDetTab = document.getElementById('operAnaesthesiaDetTable');
	var billStatusEl = getElementByName(rowObj, 'bill_status');
	var finStatus = getElementByName(rowObj, "finStatus").value;
	var isFinalized = (finStatus == 'F');
	var billFinalized = (billStatusEl && billStatusEl.value != 'A');

	for(var id=0;id<anaethesiaTypes.length;id++) {
		var anRowObj = document.createElement("TR");
		anaesthesiaDetTab.appendChild(anRowObj);
		var cell;
		cell = document.createElement("TD");
		cell.setAttribute('class', 'formlabel');
		cell.innerHTML = getString("js.common.order.operation.anaesthesia.type.label")+":";
		anRowObj.appendChild(cell);

		var cell = document.createElement("TD");
		anRowObj.appendChild(cell);
		cell.innerHTML = '<select name="op_edit_anesthesia_type" id="op_edit_anesthesia_type'+id+'" class="dropdown">'+
						 '</select>';
		loadSelectBox(document.getElementById("op_edit_anesthesia_type"+id), anaeTypes, 'anesthesia_type_name',
				'anesthesia_type_id','-- Select --', '');
		setSelectedIndex(document.getElementById("op_edit_anesthesia_type"+id),anaethesiaTypes[id].value);
		document.getElementById("op_edit_anesthesia_type"+id).disabled = true;

		var cell = document.createElement("TD");
		cell.setAttribute('class', 'formlabel');
		cell.innerHTML=getString("js.common.order.operation.anaesthesia.type.start.time.label")+":";
		anRowObj.appendChild(cell);

		var cell = document.createElement("TD");
		anRowObj.appendChild(cell);
		cell.innerHTML = getDateWidget('op_edit_anes_start_date', 'op_edit_anes_start_date_'+id,parseDateStr(anaethesiaTypesFromDateTime[id].value), '', '', true, true, '', cpath);
		makePopupCalendar('op_edit_anes_start_date_'+id);

		var textInput1 = makeTextInput("op_edit_anes_start_time", "op_edit_anes_start_time"+id, "timefield", 5, null);
		cell.appendChild(textInput1);
		textInput1.setAttribute('style','display:inline') ;
		document.getElementById('op_edit_anes_start_time'+id).value = (anaethesiaTypesFromDateTime[id].value).split(" ")[1];

		var cell = document.createElement("TD");
		cell.setAttribute('class', 'formlabel');
		cell.innerHTML=getString("js.common.order.operation.anaesthesia.type.end.time.label")+":";
		anRowObj.appendChild(cell);

		var cell = document.createElement("TD");
		anRowObj.appendChild(cell);
		cell.innerHTML = getDateWidget('op_edit_anes_end_date', 'op_edit_anes_end_date_'+id,parseDateStr(anaethesiaTypesToDateTime[id].value), '', '', true, true, '', cpath);
		makePopupCalendar('op_edit_anes_end_date_'+id);

		var textInput2 = makeTextInput("op_edit_anes_end_time", "op_edit_anes_end_time"+id, "timefield", 5, null);
		cell.appendChild(textInput2);
		textInput2.setAttribute('style','display:inline') ;
		document.getElementById('op_edit_anes_end_time'+id).value = (anaethesiaTypesToDateTime[id].value).split(" ")[1]
		anaesthesiaDetTab.appendChild(anRowObj);

		document.getElementById('op_edit_anes_start_date_'+id).disabled = isFinalized || billFinalized;
		document.getElementById('op_edit_anes_end_date_'+id).disabled = isFinalized || billFinalized;
		document.getElementById('op_edit_anes_start_time'+id).disabled = isFinalized || billFinalized;
		document.getElementById('op_edit_anes_end_time'+id).disabled = isFinalized || billFinalized;
	}
}

function cancelOpEdit() {
	oPRowUnderEdit.className = "";
	oPeditDialog.cancel();
	rowUnderEdit = undefined;
	oPRowUnderEdit = undefined;
	clearOperEditAnaesthesiaDetailsTable();
}

function validateOpAnaesthesiaDetails(opEditAnaesTypes,opEditAnaesTypeFromDate,opEditAnaesTypeToDate,
	opEditAnaesTypeFromTime,opEditAnaesTypeToTime) {
	var regDateTime = getDateTime(regdate,regtime);
	for (var i=0;i<opEditAnaesTypes.length;i++) {
		var anStartDateTime = getDateTime(opEditAnaesTypeFromDate[i].value,opEditAnaesTypeFromTime[i].value);
		var anEndDateTime = getDateTime(opEditAnaesTypeToDate[i].value,opEditAnaesTypeToTime[i].value);
		if(empty(opEditAnaesTypeFromDate[i].value)) {
			showMessage("js.common.order.operation.anaesthesia.start.date.is.required");
			opEditAnaesTypeFromDate[i].focus();
			return false;
		}
		if(empty(opEditAnaesTypeToDate[i].value)) {
			showMessage("js.common.order.operation.anaesthesia.end.date.is.required");
			opEditAnaesTypeToDate[i].focus();
			return false;
		}
		if(empty(opEditAnaesTypeFromTime[i].value)) {
			showMessage("js.common.order.operation.anaesthesia.start.time.is.required");
			opEditAnaesTypeFromTime[i].focus();
			return false;
		}
		if(empty(opEditAnaesTypeToTime[i].value)) {
			showMessage("js.common.order.operation.anaesthesia.end.time.is.required");
			opEditAnaesTypeToTime[i].focus();
			return false;
		}
		if (anStartDateTime < regDateTime) {
			showMessage("js.common.order.operation.anaesthesia.from.time.less.than.admit.time.string");
			opEditAnaesTypeFromDate[i].focus();
			return false;
		}

		if (anEndDateTime < regDateTime) {
			showMessage("js.common.order.operation.anaesthesia.to.time.less.than.admit.time.string");
			opEditAnaesTypeToDate[i].focus();
			return false;
		}

		if (anEndDateTime < anStartDateTime) {
			showMessage("js.common.order.operation.anaesthesia.to.date.can.not.bes.less.than.start.date.string");
			opEditAnaesTypeFromDate[i].focus();
			return false;
		}
	}
	return true;
}

function saveOpEdit() {
	var row = rowUnderEdit;
	var opRow = oPRowUnderEdit;
	var newEl = getElementByName(row, 'new');

	if (newEl.value == 'Y') // edit of a newly added row is not allowed, so we should not be here.
		return false;

	var editedEl = getElementByName(row, 'edited');
	if (editedEl)
		editedEl.value = 'Y';

	setNodeText(row.cells[3], document.oPeditForm.eOpFromDate.value + ' ' + document.oPeditForm.eOpFromTime.value);
	setNodeText(row.cells[5], document.oPeditForm.eOpToDate.value + ' ' + document.oPeditForm.eOpToTime.value);

	var opEditAnaesTypes = document.getElementsByName('op_edit_anesthesia_type');
	var opEditAnaesTypeFromDate = document.getElementsByName('op_edit_anes_start_date');
	var opEditAnaesTypeToDate = document.getElementsByName('op_edit_anes_end_date');
	var opEditAnaesTypeFromTime = document.getElementsByName('op_edit_anes_start_time');
	var opEditAnaesTypeToTime = document.getElementsByName('op_edit_anes_end_time');

	if (opEditAnaesTypes != null && opEditAnaesTypes.length > 0) {
		if(!validateOpAnaesthesiaDetails(opEditAnaesTypes,opEditAnaesTypeFromDate,
			opEditAnaesTypeToDate,opEditAnaesTypeFromTime,opEditAnaesTypeToTime))
		return false;
	}

	getElementByName(row, "remarks").value = document.oPeditForm.eRemarks.value;
	getElementByName(row, "presDocId").value = document.oPeditForm.ePresDocId.value;
	getElementByName(row, "presDocName").value = document.oPeditForm.eOpPrescribedBy.value;
	getElementByName(row, "newFinStatus").value = document.oPeditForm.eCompleted.checked ? 'F' :
	getElementByName(row, "finStatus").value ;
	getElementByName(row, "fromDate").value = document.oPeditForm.eOpFromDate.value;
	getElementByName(row, "fromTime").value = document.oPeditForm.eOpFromTime.value;
	getElementByName(row, "toDate").value = document.oPeditForm.eOpToDate.value;
	getElementByName(row, "toTime").value = document.oPeditForm.eOpToTime.value;


	if(opEditAnaesTypes != null && opEditAnaesTypes.length > 0) {
		for(var i=0;i<opEditAnaesTypes.length;i++) {
			getElementsByName(row,'anaes_start_datetime_'+opRow.rowIndex)[i].value = opEditAnaesTypeFromDate[i].value+" "+opEditAnaesTypeFromTime[i].value;
			getElementsByName(row,'anaes_end_datetime_'+opRow.rowIndex)[i].value = opEditAnaesTypeToDate[i].value+" "+opEditAnaesTypeToTime[i].value;
		}
	}
	clearOperEditAnaesthesiaDetailsTable();
	oPeditDialog.cancel();
	opRow.className = "edited";
}

function clearOperEditAnaesthesiaDetailsTable() {
	var table= document.getElementById('operAnaesthesiaDetTable');
	if(table && table.rows.length > 0) {
		for (var i=table.rows.length-1;i>=0;i--) {
			table.deleteRow(i);
		}
	}
}

function initCancelOptionsDialog() {
	cancelOptionsDialog = new YAHOO.widget.Dialog("cancelOptionsDialog", { width:"300px",
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("cancelOptionsDialog").style.display = 'block';
	cancelOptionsDialog.render();
	subscribeEscKeyEvent(cancelOptionsDialog, cancelDialogCancel);
	cancelOptionsDialog.cancelEvent.subscribe(onCancelDialogCancel);

	YAHOO.util.Event.addListener(document.cancelOptionsForm.cancelOrderOk, "click", onCancelDialogOk);
	YAHOO.util.Event.addListener(document.cancelOptionsForm.cancelOrderCancel, "click", cancelDialogCancel);
}

function showCancelOptionsDialog(imgObj) {
	cancelOptionsDialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	rowUnderEdit = row;
	YAHOO.util.Dom.addClass(row, 'editing');

	var billStatusEl = getElementByName(row, 'bill_status');
	if (billStatusEl && billStatusEl.value != 'A') {
		// disable cancel with refund because the bill cannot be changed
		document.cancelOptionsForm.cancelOrderType[0].disabled = true;
	} else {
		document.cancelOptionsForm.cancelOrderType[0].disabled = false;
	}

	cancelOptionsDialog.show();
}

function showCancelReconductTestDialog(imgObj) {
	cancelReconductionTestDialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	rowUnderEdit = row;
	YAHOO.util.Dom.addClass(row, 'editing');
	cancelReconductionTestDialog.show();
}

function onCancelDialogOk() {
	var row = rowUnderEdit;			// was saved when the dialog was started
	var cancelEl = getElementByName(row, 'cancelled');
	var typeEl = getElementByName(row, 'type');
	var option = getRadioSelection(document.cancelOptionsForm.cancelOrderType);
	if (option == null || option == '') {
		showMessage("js.order.common.select.cancellationoption");
		return false;
	}
	cancelEl.value = option;

	// set "deleted" flag and also the edited class to the row
	YAHOO.util.Dom.addClass(row, 'edited');

	var flagImg = row.cells[ITEM_COL].getElementsByTagName("img")[0];
	if(null != flagImg)
		flagImg.src = cpath+"/images/red_flag.gif";
	var trashImg = row.cells[TRASH_COL].getElementsByTagName("img")[0];
	trashImg.src = cpath+"/icons/undo_delete.gif";

	if( typeEl.value.toLowerCase() == 'Doctor'.toLowerCase() )
		onDoctorCancle(row);
	cancelOptionsDialog.cancel();

	if(screenid == 'dialysis_order') {
		updateSavedItemsTotalsOnDelete(row , '');
	}
}

function cancelDialogCancel() {
	cancelOptionsDialog.cancel();
}

function onCancelDialogCancel() {
	YAHOO.util.Dom.removeClass(rowUnderEdit, 'editing');
}

function initOpCancelOptionsDialog() {
	oPcancelOptionsDialog = new YAHOO.widget.Dialog("oPcancelOptionsDialog", { width:"300px",
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("oPcancelOptionsDialog").style.display = 'block';
	oPcancelOptionsDialog.render();
	subscribeEscKeyEvent(oPcancelOptionsDialog, onOpCancelDialogCancel);

	YAHOO.util.Event.addListener(document.oPcancelOptionsForm.oPcancelOrderOk, "click", onOpCancelDialogOk);
	YAHOO.util.Event.addListener(document.oPcancelOptionsForm.oPcancelOrderCancel, "click", onOpCancelDialogCancel);
}

function showOpCancelOptionsDialog(imgObj) {
	oPcancelOptionsDialog.cfg.setProperty("context", [imgObj, "tr", "br"], false);
	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');

	var tab = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'table');
	var opRow = YAHOO.util.Dom.getAncestorByTagName(tab, 'tr');

	oPRowUnderEdit = opRow;
	rowUnderEdit = row;
	YAHOO.util.Dom.addClass(opRow, 'editing');

	var billStatusEl = getElementByName(row, 'bill_status');
	if (billStatusEl && billStatusEl.value != 'A') {
		// disable cancel with refund because the bill cannot be changed
		document.oPcancelOptionsForm.oPcancelOrderType[0].disabled = true;
	} else {
		document.oPcancelOptionsForm.oPcancelOrderType[0].disabled = false;
	}

	oPcancelOptionsDialog.show();
}



function onOpCancelDialogOk() {
	var row = rowUnderEdit;			// was saved when the dialog was started
	var opRow = oPRowUnderEdit;
	var cancelEl = getElementByName(row, 'cancelled');
	var option = getRadioSelection(document.oPcancelOptionsForm.oPcancelOrderType);
	if (option == null || option == '') {
		showMessage("js.order.common.select.cancellationoption");
		return false;
	}
	cancelEl.value = option;

	// set the edited class to the row
	YAHOO.util.Dom.addClass(opRow, 'edited');

	var trashImg = row.cells[8].getElementsByTagName("img")[0];
	trashImg.src = cpath+"/icons/undo_delete.gif";

	oPcancelOptionsDialog.cancel();
	YAHOO.util.Dom.removeClass(opRow, 'editing');
}

function onOpCancelDialogCancel() {
	oPcancelOptionsDialog.cancel();
	YAHOO.util.Dom.removeClass(oPRowUnderEdit, 'editing');
}

function cancelOrder(imgObj) {

	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	var newEl = getElementByName(row, 'new');
	var typeEl = getElementByName(row, 'type');
	if (newEl.value == 'Y') {
		clearDocVisitForPack(row); // clear the doctor visits created if it is a package and contains doctor visits.
		deleteTestAdditionalDocs(row);

		// this is a new item, we just need to delete the entire row from the table
		if(getElementByName(row, "firstOfCategory").value == "true" && gIsInsurance )
			alert(getString("js.order.common.deletingthefirstitem")+"\n"+getString("js.order.common.theinsurancepatientcopay.needtobeadjusted")+"\n"+getString("js.order.common.additionalcharges.thesamecategory"));
		row.parentNode.removeChild(row);
		newOrderCount--;
		updateTotalNewAmount();


		if( typeEl.value.toLowerCase() == 'Doctor'.toLowerCase() )
			onDoctorCancle(row);
	} else {
		// existing row. Operations can be uncancel or to ask for cancel options.
		var cancelEl = getElementByName(row, 'cancelled');
		var cancleBill = getElementByName(row, 'cancleBill');

		if (cancelEl.value == '') {
			if(getElementByName(row, "firstOfCategory").value == "true" && gIsInsurance)
				alert(getString("js.order.common.deletingthefirstitem")+"\n"+getString("js.order.common.theinsurancepatientcopay.needtobeadjusted")+"\n"+getString("js.order.common.additionalcharges.thesamecategory"));
			rowUnderEdit = row;
			if ( cancleBill.value == 'false')
				showCancelReconductTestDialog(imgObj);
			else
				showCancelOptionsDialog(imgObj);
		} else {
			var undeleteFrom = cancelEl.value;
			cancelEl.value = '';
			var editedEl = getElementByName(row, 'edited');
			if (editedEl.value =='Y') {
				row.className = 'edited';
			} else {
				YAHOO.util.Dom.removeClass(row, 'edited');
				if(screenid == 'dialysis_order') {
					updateSavedItemsTotalsOnDelete(row , undeleteFrom);
				}
			}
			var flagImg = row.cells[ITEM_COL].getElementsByTagName("img")[0];
			if(null != flagImg)
				flagImg.src = cpath+"/images/empty_flag.gif";
			var trashImg = row.cells[TRASH_COL].getElementsByTagName("img")[0];
			trashImg.src = cpath+"/icons/delete.gif";
		}
	}

	if(screenid == 'dialysis_order') {
		updateNewItemTotals();
		//updateSavedItemsTotalsOnDelete(row);
		handleNewlyAddedItems(row,newEl);
	}
}

function cancelOperation(imgObj) {

	var tab = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'table');
	var opRow = YAHOO.util.Dom.getAncestorByTagName(tab, 'tr');

	var row = YAHOO.util.Dom.getAncestorByTagName(imgObj, 'tr');
	var newEl = getElementByName(row, 'new');

	var nextRow = tab.rows[1];

	if (newEl.value == 'Y') {
		opRow.parentNode.removeChild(opRow);
		newOrderCount--;
		updateTotalNewAmount();
	} else {
		var cancelEl = getElementByName(row, 'cancelled');
		if (cancelEl.value == '') {
			oPRowUnderEdit = opRow;
			showOpCancelOptionsDialog(imgObj);
		} else {
			cancelEl.value = '';
			var editedEl = getElementByName(row, 'edited');
			if (editedEl.value =='Y') {
				opRow.className = 'edited';
			} else {
				YAHOO.util.Dom.removeClass(opRow, 'edited');
			}

			var trashImg = row.cells[8].getElementsByTagName("img")[0];
			trashImg.src = cpath+"/icons/delete.gif";
		}
	}

	//hiding heading in case no other surgery exists
	hideOPerationHeading();
}

function hideOPerationHeading(){
		document.getElementById("surgeriesHeading").style.display =
						document.getElementById("operationtable").rows.length <= 1 ? 'none' : 'block';
}

billAmount = 0;
totalCreditAmount = 0;
gIsInsurance = false;
billType = "";

function onChangeBill() {
	var origInsurance = gIsInsurance;
	getItems(document.mainform.billNo.value);

	setBillDetails();
	if(mod_adv_packages == 'Y') {
		if(clearOrderGrid()) {
			gBillNo = document.mainform.billNo.value;
		} else {
			setSelectedIndex(document.mainform.billNo,gBillNo);
		}
	}

	clearOrderTable(0);
	// TODO: clear operations that are new and and orders under existing operations
	orderPrescriptions(document.mainform.patientid.value);
	updateTotalNewAmount();

	return true;
}

function clearOrderGrid() {
	var billNo = document.mainform.billNo.value;
	var packageIds = document.getElementsByName('package_id');
	var newElems = document.getElementsByName('new');
	var multiVisitPackArr = document.getElementsByName('multi_visit_package');
	var cancelled = document.getElementsByName('cancelled');
	var mvPackBillNo = document.getElementsByName("multi_visit_package_bill_no");
	var patPackIds = document.getElementsByName("mv_pat_package_id");
	var orderTable = document.getElementById('orderTable'+0);
	var orderTableLength = orderTable.rows.length;
	var index = 0;
	var isMultiVisitPackageBill = false;
	var mvItemsArr = new Array();
	var otherItemsArr = new Array();
	var otherMvPackArr = new Array();
	var ok = true;

	if (billNo != 'new' && billNo != 'newInsurance' && orderTableLength > 1) {
		var mvPackageId = getMultiVisitPackageBillDetails(billNo);
		isMultiVisitPackageBill = !empty(mvPackageId);
		var billItemsCount = getBillItemsCount(billNo);

		if (billItemsCount != '0' && isMultiVisitPackageBill) {
			for (var i=0;i<packageIds.length-3;i++) {
				if(newElems[i].value == 'Y') {
					if(multiVisitPackArr[i].value == 'false') {
						otherItemsArr.push(i);
					} else if (multiVisitPackArr[i].value == 'true'){
						mvItemsArr.push(i);
					}
				}
			}

			var patPackId = 0;
			var packId =0;
			for(var i=0;i<packageIds.length-3;i++) {
				if(billNo == mvPackBillNo[i].value) {
					patPackId = patPackIds[i].value;
					packId = packageIds[i].value;
					break;
				}
			}

			if(!empty(packId) && !empty(patPackId)) {
				var packStatus = this.getPackageStatus(patPackId,packId);
				if((packStatus == 'C' || packStatus == 'X')) {
					ok = confirm(billNo+" contains multi visit package, which has been completed or cancelled."+"\n other items will be cleared from order grid."+"\n do you want to continue? ");
					if(ok) {
						if(otherItemsArr != null && otherItemsArr.length > 0) {
							for (var i=otherItemsArr.length-1;i>=0;i--) {
								clearDocVisitForPack(orderTable.rows[otherItemsArr[i]+1]); // delete the doctor visits created if it is a package.
								deleteTestAdditionalDocs(orderTable.rows[otherItemsArr[i]]);
				  				orderTable.deleteRow(otherItemsArr[i]+1);
				  			}
						}
						if(mvItemsArr != null && mvItemsArr.length > 0) {
							for (var i=mvItemsArr.length-1;i>=0;i--) {
								deleteTestAdditionalDocs(orderTable.rows[mvItemsArr[i]]);
				  				orderTable.deleteRow(mvItemsArr[i]+1);
				  			}
						}
					}
					return ok;
				}
			}

			if (otherItemsArr != null && otherItemsArr.length > 0) {
				  var ok = confirm("others items will be cleared from the order grid.\n do you want to continue? ");
			  if (ok) {
			  	for (var i=otherItemsArr.length-1;i>=0;i--) {
			  		clearDocVisitForPack(orderTable.rows[otherItemsArr[i]+1]); // delete the doctor visits created if it is a package.
			  		deleteTestAdditionalDocs(orderTable.rows[otherItemsArr[i]]);
			  		orderTable.deleteRow(otherItemsArr[i]+1);
			  	}
			  }
			} else {
				var flag = true;
				for (var i=0;i<packageIds.length-3;i++) {
					if(newElems[i].value == 'Y') {
						if(!empty(mvPackageId) && mvPackageId != packageIds[i].value) {
							otherMvPackArr.push(i);
							flag = false;
						}
					}
				}
				if(!flag) {
					var ok = confirm("other multi visit package items will be cleared from the order grid.\n do you want to continue? ");
					if(ok) {
						for (var i=otherMvPackArr.length-1;i>=0;i--) {
							deleteTestAdditionalDocs(orderTable.rows[otherMvPackArr[i]]);
			  				orderTable.deleteRow(otherMvPackArr[i]+1);
			  			}
					}
				}
			}

		} else if (billItemsCount != '0' && !isMultiVisitPackageBill) {
			for (var i=0;i<packageIds.length-3;i++) {
				if (newElems[i].value == 'Y') {
					if (multiVisitPackArr[i].value == 'false') {
						otherItemsArr.push(i);
					} else if (multiVisitPackArr[i].value == 'true'){
						mvItemsArr.push(i);
					}
				}
			}

			if (mvItemsArr != null && mvItemsArr.length > 0) {
				  var ok = confirm("multivisit pacakge items will be cleared form the order grid.\n do you want to continue? ");
			  if (ok) {
			  	for (var i=mvItemsArr.length-1;i>=0;i--) {
			  		deleteTestAdditionalDocs(orderTable.rows[mvItemsArr[i]]);
			  		orderTable.deleteRow(mvItemsArr[i]+1);
			  	}
			  }
			}
		}
	}
	return ok;
}

function clearDocVisitForPack(row) {
	var packIdEl = getElementByName(row, 'package.packageId');
	if (!empty(packIdEl)) {
		var packId = packIdEl.value;
		var prescId = getElementByName(row, 'package.prescId').value;

		var packIdDocEl = document.getElementsByName('package.packIdFordoc');
		var prescIdDocEl = document.getElementsByName('package.packPrescIdFordoc');
		var rowIndexEl = document.getElementsByName('package.mainRowIndex');

		var rowArray = new Array();
		var docVisitTable = document.getElementById('innerDocVisitForPack');
		for (var i=0; i<packIdDocEl.length; i++) {
			if (packIdDocEl[i].value == packId && prescIdDocEl[i].value == prescId && parseInt(rowIndexEl[i].value) == row.rowIndex) {
				// we should not delete the row here itself, if we do that, 'i' value will vary.
				rowArray.push(findAncestor(packIdDocEl, 'TR'));
			}
		}
		for (var i=0; i<rowArray.length; i++) {
			docVisitTable.deleteRow(rowArray[i]);
		}

		var packIdCondDocEl = document.getElementsByName('package.packIdForCondDoc');
		var prescIdCondDocEl = document.getElementsByName('package.packPrescIdForCondDoc');
		var rowIndexCondEl = document.getElementsByName('package.mainRowIndex');

		var condRowArray = new Array();
		var condDocTable = document.getElementById('innerCondDocForPack');
		for (var i=0; i<packIdCondDocEl.length; i++) {
			if (packIdCondDocEl[i].value == packId && prescIdCondDocEl[i].value == prescId) {
				// we should not delete the row here itself, if we do that, 'i' value will vary.
				condRowArray.push(findAncestor(packIdCondDocEl, 'TR'));
			}
		}
		for (var i=0; i<condRowArray.length; i++) {
			condDocTable.deleteRow(condRowArray[i]);
		}
	}
}

function getPackageStatus(patPackId,packId) {
	var ajaxReqObject = newXMLHttpRequest();
	var url = cpath+"/master/orderItems.do?method=getMvPackageStatus"
	url = url + "&pat_pack_id=" + patPackId;
	url = url + "&package_id=" + packId;
	ajaxReqObject.open("POST", url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
			return ajaxReqObject.responseText;
		}
	}
	return null;
}

function nullifyAllBillAmountFields() {
	document.getElementById("billAmt").textContent = "";
	document.getElementById("creditsAmt").textContent = "";
	if (document.getElementById("billPatientAmt") != null)
		document.getElementById("billPatientAmt").textContent = "";
	billAmount	= "";
	totalCreditAmount = "";
}

var checkCredits = true;

function setBillDetails() {

	if (document.mainform.billNo.value == "") {
		// nothing selected
		nullifyAllBillAmountFields();
		billType = 'P';		// new bills are prepaid always
		addOrderDialog.billOpenDate = null;

	} else if (document.mainform.billNo.value == "new" || document.mainform.billNo.value == "newInsurance") {
		nullifyAllBillAmountFields();
		billType = 'P';		// new bills are prepaid always

		if (document.mainform.billNo.value == "newInsurance") {
			showPatientAmounts(true);		// defined in ordertable.js
			gIsInsurance = true;
		} else if (document.mainform.billNo.value == "new") {
			showPatientAmounts(false);
			gIsInsurance = false;
		}
		addOrderDialog.billOpenDate = null;
	} else {
		// existing open bill is chosen: find the bill object and set bill amounts
		var bill;
		for (var i=0;i<billDetails.length;i++) {
			if (billDetails[i].bill_no == document.mainform.billNo.value) {
				bill = billDetails[i];
				break;
			}
		}

		document.getElementById("billAmt").textContent = formatAmountValue(bill.total_amount + bill.total_tax);
		gIsInsurance = bill.is_tpa && bill.primary_sponsor_id != '' && bill.primary_sponsor_id != null;
		billAmount = bill.total_amount + bill.total_tax;
		totalCreditAmount = getPaise(bill.approval_amount) +
			getPaise(bill.deposit_set_off) + getPaise(bill.total_receipts);

		billType = bill.bill_type;

		if(bill.approval_amount == 0)
			checkCredits = false;
		else if(bill.approval_amount == null || bill.approval_amount > 0)
			checkCredits = true;

		document.getElementById("creditsAmt").textContent = formatAmountPaise(totalCreditAmount);

		if ( gIsInsurance && document.getElementById("billPatientAmt") != null ) {
			document.getElementById("billPatientAmt").textContent =
				formatAmountValue(bill.total_amount + bill.total_tax - bill.total_claim - bill.total_claim_tax + bill.insurance_deduction);
		}

		showPatientAmounts(gIsInsurance);
		addOrderDialog.allowFinalization = (billType == 'C');
		addOrderDialog.billOpenDate = bill.open_date;
	}
	addOrderDialog.billNo = document.mainform.billNo.value;
	return true;
}

function showPatientAmounts(show) {
	// show patient amounts in order table
	showOrderTablePatientAmounts(0, show);

	// iterate through all operations and show patient amounts in them
	var table = document.getElementById("operationtable");
	var templateRowNum = table.rows.length-1;

	for (var i=0; i<templateRowNum; i++) {		// no header row, start from 0.
		var row = table.rows[i];
		var opId = getElementByName(row, 'operationId').value;
		showOrderTablePatientAmounts(opId, show);
	}

	// show patient amount totals
	var patientAmountRow = document.getElementById("patientAmounts");
	if (patientAmountRow) {
		if (show)
			patientAmountRow.style.display = '';
		else
			patientAmountRow.style.display = 'none';
	}
}

function validateCredits() {

	// no validation for bill now bills
	if (billType == 'P')
		return true;

	var totalBill = getPaise(billAmount);
	totalBill += getNewOrdersAmount();

	if (checkCredits && totalBill > totalCreditAmount) {
		var totalBillDisp = formatAmountPaise(totalBill);
		var totalCreditDisp = formatAmountPaise(totalCreditAmount);
		var ok = confirm("Total bill amount (" + totalBillDisp +
				") is greater than existing Credits (" + totalCreditDisp + ") \n" +
				"Do you want to proceed?");
		if (!ok) {
			return false;
		}
	}
	return true;
}

/*
 * Validation to ensure that bill now bills don't have any new orders that
 * are not finalized.
 */
function validateFinalization() {

	// no validation for bill later bills
	if (billType == 'C')
		return true;

	var unfCount = 0;		// un-finalized count
	unfCount += getNumUnfinalizedEqpmt(document.getElementById("orderTable0"));

	// iterate through all operations and get their finalization status as well as equipment within
	var table = document.getElementById("operationtable");
	var templateRowNum = table.rows.length-1;
	for (var i=0; i<templateRowNum; i++) {		// no header row, start from 0.
		var opRow = table.rows[i];

		// check for the operation itself
		var isNew = getElementByName(opRow, 'new').value;
		if (isNew == 'Y') {
			var finalized = getElementByName(opRow, 'operation.finalization_status').value;
			if (finalized != 'F')
				unfCount++;
		}

		// check for equipment within the operation
		var orderTable = YAHOO.util.Dom.getElementsByClassName('detailList', 'table', opRow)[0];
		unfCount += getNumUnfinalizedEqpmt(orderTable);
	}

	if (unfCount > 0) {
		var msg=getString("js.order.common.thereare");
		msg+= unfCount;
		msg+=getString("js.order.common.unfinalizedot.equipmentorders");
		msg+=getString("js.order.common.theseordersnotadd");
		msg+="\n";
		msg+=getString("js.order.common.selectabilllaterbill.removeunfinalizedorders");
		alert(msg);
		return false;
	}

	return true;
}

/*
function validateIpCreditLimit() {
	//Credit limit rule is applicable for IP visits only
	var newOrEditItemsExist = isNewOrEditedItemsExist();
	if(visitType != 'i' || !newOrEditItemsExist) {
		return true;
	}
	
	var totalNewPatAmt = 0;
	var newPatientAmtEl = document.getElementById('totalNewPatientAmt');
	var newAmountEl = document.getElementById('totalNewAmount');

	if(gIsInsurance) {
		if(newPatientAmtEl != null) {
			totalNewPatAmt = newPatientAmtEl.textContent;
			if(totalNewPatAmt != null && totalNewPatAmt != '' && totalNewPatAmt.trim() != '') {
				totalNewPatAmt = parseFloat(totalNewPatAmt);
			}
		}
	} else {
		if(newAmountEl != null) {
			totalNewPatAmt = newAmountEl.textContent;
			if(totalNewPatAmt != null && totalNewPatAmt != '' && totalNewPatAmt.trim() != '') {
				totalNewPatAmt = parseFloat(totalNewPatAmt);
			}
		}
	}
	
	// visitPatientDuePaise = visit patient due and new items patient due
	var visitPatientDuePaise = getPaise(visitTotalPatientDue) + getPaise(totalNewPatAmt);  
	var ipCreditLimitAmountPaise = getPaise(ipCreditLimitAmount);
	if(ip_credit_limit_rule == 'B') {
		if((visitPatientDuePaise > ipCreditLimitAmountPaise) && ipCreditLimitAmountPaise > 0) {
			var msg=getString("js.order.common.ipcreditlimitis");
			msg+=' '+ formatAmountPaise(ipCreditLimitAmountPaise);
			msg+="\n";
			msg+=getString("js.order.common.and.below.currentoutstanding");
			msg+=' '+ formatAmountPaise(visitPatientDuePaise);
			alert(msg);
			return false;
		}
	} else if (ip_credit_limit_rule == 'W') {
		if((visitPatientDuePaise > ipCreditLimitAmountPaise) && ipCreditLimitAmountPaise > 0) {
			var msg=getString("js.order.common.ipcreditlimitis");
			msg+=' '+ formatAmountPaise(ipCreditLimitAmountPaise) ;
			msg+="\n";
			msg+=getString("js.order.common.and.below.currentoutstanding");
			msg+=' '+ formatAmountPaise(visitPatientDuePaise);
			msg+="\n";
			msg+=getString("js.order.common.doyouwanttoproceed");
			var ok = confirm(msg);
			if(!ok)
				return false;
		}
	}
	return true;
} */

function isNewOrEditedItemsExist() {
	var isNewList = document.getElementsByName("new"); // Y or N
	var isEditedList = document.getElementsByName("edited"); // Y or N
	var isCancelledList = document.getElementsByName("cancelled"); // IC(to be refunded) or I(no refund)
	
	for (var i=0; i<isNewList.length; i++) {
		if( (isNewList[i] != null && (isNewList[i].value) == 'Y') ||
				(isEditedList[i] != null && (isEditedList[i].value) == 'Y') ||
				(isCancelledList[i] != null && ((isCancelledList[i].value) == 'I' || (isCancelledList[i].value) == 'IC'))
				) {
			return true;
		}
	}
	return false;
}


function getNumUnfinalizedEqpmt(table) {
	var count = 0;
	var numRows = table.rows.length;
	var templateRowIndex = numRows-1;
	for (var i = 1; i < templateRowIndex; i++) {
		var row = table.rows[i];
		var isNew = getElementByName(row, 'new').value;
		if (isNew == 'Y') {
			var type = getElementByName(row, 'type').value;
			if (type == 'Equipment') {
				var finalized = getElementByName(row, 'equipment.finalization_status').value;
				if (finalized != 'F')
					count++;
			}
		}
	}
	return count;
}

function getNewOrdersAmount() {
	// total amount of the main orders table
	var totalNewAmount = getTotalNewOrdersAmountPaise(0);

	// iterate through all operations and get their totals
	var table = document.getElementById("operationtable");
	if (table != null) {
		var templateRowNum = table.rows.length-1;

		for (var i=0; i<templateRowNum; i++) {		// no header row, start from 0.
			// amount for the operation itself
			var row = table.rows[i];
			var amountEl = getElementByName(row, 'operAmount');
			if (amountEl != null) totalNewAmount += getPaise(amountEl.value);

			// amount for sub-orders of the operation
			var opId = getElementByName(row, 'operationId').value;
			totalNewAmount += getTotalNewOrdersAmountPaise(opId);
		}
	}
	return totalNewAmount;
}

function getNewOrdersPatientAmount() {
	// total amount of the main orders table
	var totalNewAmount = getTotalNewOrdersPatientAmountPaise(0);

	// iterate through all operations and get their totals
	var table = document.getElementById("operationtable");
	if (table != null) {
		var templateRowNum = table.rows.length-1;

		for (var i=0; i<templateRowNum; i++) {		// no header row, start from 0.
			// amount for the operation itself
			var row = table.rows[i];
			var amountEl = getElementByName(row, 'operPatientAmount');
			if (amountEl != null) totalNewAmount += getPaise(amountEl.value);

			// amount for sub-orders of the operation
			var opId = getElementByName(row, 'operationId').value;
			totalNewAmount += getTotalNewOrdersPatientAmountPaise(opId);
		}
	}
	return totalNewAmount;
}

function onSubmit(button) {
	if (document.mainform.patientid.value == '') {
		showMessage("js.order.common.selectapatient");
		return false;
	}

	if ( newOrderCount > 0 && document.mainform.billNo.value == '' ) {
		if( newBillAllowed )
			showMessage("js.order.common.selectabillno");
		else
			showMessage("js.order.common.notauthorized");

		return false;
	}

	if (!validateConductingDoctor())
		return false;

	if (!validatePrescribingDoctor())
		return false;

	if (!validateTestsAdditionalDetails())
		return false;

	if (!validateCredits())
		return false;

	if (!validateFinalization())
		return false;
	
	/* if (!validateIpCreditLimit())
		return false; */

	if(!validatePriorAuthIds())
		return false;
	if (button.name == 'save')
		document.getElementById("print").value = 'N';
	else
		document.getElementById("print").value = 'Y';

	document.getElementById("printerId").value =document.getElementById("printId").value;
	document.mainform.submit();
}

function getMultiVisitPackageBillDetails(billNo) {
	var ajaxReqObject = newXMLHttpRequest();
	var url = cpath+"/master/orderItems.do?method=getMultiVisitPackageBillDetails"
	url = url + "&bill_no=" + billNo;
	ajaxReqObject.open("POST", url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
			return ajaxReqObject.responseText;
		}
	}
	return null;
}

function getBillItemsCount(billNo) {
	var ajaxReqObject = newXMLHttpRequest();
	var url = cpath+"/master/orderItems.do?method=getBillItemsCount"
	url = url + "&bill_no=" + billNo;
	ajaxReqObject.open("POST", url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
			return ajaxReqObject.responseText;
		}
	}
	return null;
}


function enablePrint() {
	if (newOrderCount > 0)
		document.getElementById("saveNPrint").disabled = false;
	else
		document.getElementById("saveNPrint").disabled = true;
}


var editDialog;

function subscribeEscKeyEvent(dialog, cHandler) {
	var cancelHandler = (cHandler == null) ? dialog.cancel : cHandler;
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:cancelHandler, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}


function addOrder(order) {
	var type = order.itemType;
	var activity_id = order.itemId;
	var activity_rate = order.rate;
	if (type == 'Operation') {
		index = addOperations(order);
		clearOperAnaesthesiaDetailsTable();
		document.getElementById("surgeriesHeading").style.display = 'block';
	} else if (type == 'Laboratory' || type == 'Radiology') {
		index = addInvestigations(order);
	} else if (type == 'Service') {
		index = addServices(order,'','',prescribingDoctor);
	} else if (type == 'Other Charge') {
		index = addOtherServices(order, "OCOTC");
	} else if (type == 'Implant') {
		index = addOtherServices(order, "IMPOTC");
	} else if (type == 'Consumable') {
		index = addOtherServices(order, "CONOTC");
	} else if (type == 'Medicine') {
		index = addOtherServices(order, "MEMED");
	} else if (type == 'Doctor') {
		index = addDoctor(order);
	} else if (type == 'Equipment') {
		index = addEquipment(order);
	} else if (type == 'Meal') {
		index = addDiets(order, '', '');
	} else if (type == 'Package') {
		index = addPackages(order);
	}

	newOrderCount++;
	enablePrint();

	var visitId = document.getElementById('patientid').value;
	getBillChargeClaims(visitId, document.mainform);

	updateTotalNewAmount();
	return true;
}

function getBillChargeClaims(visitID, form){

	var formToken = form._insta_transaction_token.value;
	form._insta_transaction_token.value="";

	YAHOO.util.Connect.setForm(form);

	var url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getBillChargeClaimsForOrderItems&visitID='+visitID;
	var ajaxRequestForBillChargeClaims = YAHOO.util.Connect.asyncRequest('POST', url,
			{
				success: OnGetBillChargeClaims,
				failure: OnGetBillChargeClaimsFailure,
				argument: [form, formToken]
			}
		)
}


function OnGetBillChargeClaims(response){

	if (response.responseText != undefined) {
		var planMap = eval('(' + response.responseText + ')');
		var adjTaxAmtsMap = planMap[-2];
		for(var j=0; j<planList.length; j++){
			var planId = planList[j].plan_id;
			var billChgClaimMap = planMap[planId];
			setSponsorAmounts(billChgClaimMap, j+1);
		}
	}

	setPatientAmounts(multiPlanExists,adjTaxAmtsMap,planMap);
	var args = response.argument;
	if (null != args && args.length >1) {
		args[0]._insta_transaction_token.value = args[1];
	}

}

function setSponsorAmounts(billChgClaimMap, priority){

	var table = document.getElementById('orderTable'+0);
	var numRows = table.rows.length;

	for (var id=1; id < numRows-1 ;id++) {
		var row = table.rows[id];
		var chargeId = null;
		chargeId = "_"+id;
		if(billChgClaimMap[chargeId] != undefined){
			var insClaimAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].insurance_claim_amt) + getPaise(billChgClaimMap[chargeId].tax_amt));
			
			if(priority == 1){
				getElementByName(row, 'priClaimAmt').value = insClaimAmt;
			}else{
				getElementByName(row, 'secClaimAmt').value = insClaimAmt;
			}
		}
	}
}


function setPatientAmounts(multiPlanExists, adjTaxAmtsMap, planMap){
	var table = document.getElementById('orderTable'+0);
	var numRows = table.rows.length;

	for (var id=1; id < numRows-1 ;id++) {
		var row = table.rows[id];
		if(getElementByName(row, 'new').value == 'Y'){
			var insClaimAmt = getElementByName(row, 'priClaimAmt').value;
			var priClaimAmtPaise = getPaise(insClaimAmt);
			var insClaimAmtPaise = priClaimAmtPaise;

			if(multiPlanExists){
				var secClaimAmtPaise = getPaise(getElementByName(row, 'secClaimAmt').value);
				insClaimAmtPaise = insClaimAmtPaise + secClaimAmtPaise;
			}
			var amtPaise = getPaise(getElementByName(row, 'orderAmount').value);
			var patAmt = amtPaise - insClaimAmtPaise;
			var chargeId = null;
			chargeId = "_"+id;
			if(adjTaxAmtsMap[chargeId] != undefined){
				if(adjTaxAmtsMap[chargeId] == 'Y'){
					var taxPaise = getPaise(getElementByName(row, 'orderTax').value);
					amtPaise = amtPaise - taxPaise ;
					var totAmt;
					var totalClaimAmtPaise = 0;
					var totalTaxAmt = 0;
					for(var j=0; j<planList.length; j++){
						var planId = planList[j].plan_id;
						var billChgClaimMap = planMap[planId];
						if(billChgClaimMap[chargeId] != undefined){
							var insClaimAmt = getPaise(billChgClaimMap[chargeId].insurance_claim_amt);
							totalClaimAmtPaise += insClaimAmt;
							totalTaxAmt += getPaise(billChgClaimMap[chargeId].tax_amt)
						}	
					}
					patAmt = amtPaise - totalClaimAmtPaise;
					totAmt = amtPaise + totalTaxAmt;
					if(null != document.mainform.billNo.value && document.mainform.billNo.value != 'new'){
						setNodeText(row.cells[AMOUNT_COL], formatAmountPaise(totAmt));
					}
				}
			}	
			setNodeText(row.cells[PATIENT_AMT_COL], formatAmountPaise(patAmt));
			getElementByName(row, 'orderPatientAmt').value = formatAmountPaise(patAmt);
		}
	}

	updateTotalNewAmount();
}


function OnGetBillChargeClaimsFailure(){

}

function clearOperAnaesthesiaDetailsTable() {
	var table= document.getElementById('anaestiatistTypeTable');
	if(table && table.rows.length > 0) {
		for (var i=table.rows.length-1;i>1;i--) {
			table.deleteRow(i);
		}
	}
	document.getElementById('anesthesia_type0').value = '';
	document.getElementById('anes_start_date0').value = '';
	document.getElementById('anes_end_date0').value = '';
	document.getElementById('anes_start_time0').value = '';
	document.getElementById('anes_end_time0').value = '';
	document.getElementById('addOpAnaesDetailsFieldSet').style.display  = 'none';
}

function updateTotalNewAmount() {
	var totalNewAmount = getNewOrdersAmount();
	var totalNewAmountEl = document.getElementById('totalNewAmount');
	if (totalNewAmountEl) {
		totalNewAmountEl.textContent = formatAmountPaise(totalNewAmount);
	}
	var totalNewPatientAmt = getNewOrdersPatientAmount();
	var totalNewPatientAmtEl = document.getElementById('totalNewPatientAmt');
	if (totalNewPatientAmtEl) {
		totalNewPatientAmtEl.textContent = formatAmountPaise(totalNewPatientAmt);
		document.getElementById("newPatientAmt").value = formatAmountPaise(totalNewPatientAmt);
	}
}

function initNewAmts(){
	var newAmtEl = document.getElementById('totalNewAmount');
	if(newAmtEl){
		newAmtEl.textContent = formatAmountPaise(0);
	}
	var newPatientAmtEl = document.getElementById('totalNewPatientAmt');
	if (newPatientAmtEl) {
		newPatientAmtEl.textContent = formatAmountPaise(0);
		document.getElementById("newPatientAmt").value = 0;
	}
}

function validatePriorAuthIds(){
	var table = document.getElementById("orderTable0");
	var rows = table.rows.length-1;
	var valid = true;

	for( var i = 0;i <table.rows.length;i++){
		var row = table.rows[i];
		var editImg = row.cells[EDIT_COL].getElementsByTagName("img")[0];
		if (getElementByName(row, 'new') != null) {
			var newEl = getElementByName(row, 'new');
			var priorAuthReq = getElementByName(row, 'prior_auth_id_req');
			var priorAuthId = getElementByName(row, 'prior_auth_id');
			var priorAuthMode = getElementByName(row, 'prior_auth_mode_id');

			if( newEl.value == 'Y' && gIsInsurance && priorAuthReq.value != 'N' ){
				if(priorAuthId.value == ''){
					if(priorAuthReq.value == 'A'){
						showMessage("js.order.common.enterpreauthnumber");
						showEditDialog(editImg);
						valid = false;
						break;
					}else{
						return confirm("Some items may need a pre auth number\n Check plan details for more details");
					}
				}
				if( priorAuthMode.value == '' ){
					var msg=getString("js.order.common.priorauthmode.required");
					msg+="\n";
					msg+=getString("js.order.common.select.priorauthmode");
					alert(msg);
					showEditDialog(editImg);
					valid = false;
					break;
				}
			}
		}
	}

	return valid;
}

function initCancelReconductiontestDialog() {
	cancelReconductionTestDialog = new YAHOO.widget.Dialog("cancleReconductTestDialog", { width:"300px",
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("cancleReconductTestDialog").style.display = 'block';
	cancelReconductionTestDialog.render();
	subscribeEscKeyEvent(cancelReconductionTestDialog, cancelDialogCancel);
	cancelReconductionTestDialog.cancelEvent.subscribe(onCancelDialogCancel);

	YAHOO.util.Event.addListener(document.cancelReconductionTestForm.cancleReconductionTestOK, "click", onCancelReconductionTestOk);
	YAHOO.util.Event.addListener(document.cancelReconductionTestForm.cancleReconductionTestCancle, "click", cancelReconductionTestCancel);
}

function onCancelReconductionTestOk(){
	var row = rowUnderEdit;
	var cancelEl = getElementByName(row, 'cancelled');
	cancelEl.value = 'I';

	// set "deleted" flag and also the edited class to the row
	YAHOO.util.Dom.addClass(row, 'edited');

	var flagImg = row.cells[ITEM_COL].getElementsByTagName("img")[0];
	flagImg.src = cpath+"/images/red_flag.gif";
	var trashImg = row.cells[TRASH_COL].getElementsByTagName("img")[0];
	trashImg.src = cpath+"/icons/undo_delete.gif";

	cancelReconductionTestDialog.cancel();
}

function cancelReconductionTestCancel() {
	cancelReconductionTestDialog.cancel();
}

function validateTestsAdditionalDetails() {
	var table = document.getElementById('ad_test_info_table');
	var mainRows = YAHOO.util.Dom.getElementsByClassName("mainRow", 'tr', table);
	var testNames = new Array();
	for (var i=0; i<mainRows.length; i++) {
		var row = mainRows[i];
		var notes = getElementByName(row, 'ad_clinical_notes').value;
		if (!empty(notes)) continue;

		var docFound = false;
		while (!YAHOO.util.Dom.hasClass(row, 'dummyRow')) {
			var deleted = getElementByName(row, "ad_test_doc_delete").value;
			var docId = getElementByName(row, "ad_test_doc_id").value;
			var fileName = !empty(docId) ? '' : YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', row)[0].value;

			if (deleted == 'false' &&
					(!empty(docId) || !empty(fileName)) ) {
				docFound = true;
				break;
			}

			row = YAHOO.util.Dom.getNextSibling(row);
		}
		if (!docFound) {
			testNames.push(getElementByName(row, 'ad_test_name').value);
		}
	}

	if (testNames.length > 0) {
		alert("Please enter clinical notes or upload a document for the following tests. \n\n * " + testNames.join("\n * "));
		return false;
	}

	return true;
}

function validateConductingDoctor() {
	var condDocMandatoryForTests = document.getElementsByName('test.conducting_doc_mandatory');
	var condDocMandatoryForServ = document.getElementsByName('service.conducting_doc_mandatory');
	var typeEle = document.mainform.addedItemType;
	var msg = '';

	for (var i=0; i<condDocMandatoryForTests.length; i++) {
		if (condDocMandatoryForTests[i].value == 'O' &&
					document.getElementsByName('test.payee_doctor_id')[i].value == '') {
			msg += '\n';
			msg += document.getElementsByName('test.item_name')[i].value;
		}
	}

	for (var i=0; i<condDocMandatoryForServ.length; i++) {
		if (condDocMandatoryForServ[i].value == 'O' &&
					document.getElementsByName('service.payee_doctor_id')[i].value == '') {
			msg += '\n';
			msg += document.getElementsByName('service.item_name')[i].value;
		}
	}
	// these are autopopulaed prescriptions.
	var condDoctorsInPack = document.getElementsByName('package.condDoctor');
	for (var i=0; i<condDoctorsInPack.length; i++) {
		if (condDoctorsInPack[i].value ==  '') {
			msg += '\n';
			msg += document.getElementsByName('package.packageName')[i].value + " " + document.getElementsByName('package.packageItemName')[i].value;
		}
	}

	if (msg != '' ) {
		alert(getString('js.order.common.conductingdoctor.required')+msg);
		return false;
	} else
		return true;
}

function validatePrescribingDoctor() {

	if (gOnePrescDocForOP == 'Y' && patientType == 'o') {
		var consultingDoctor = null;
		if (!empty(document.getElementById('doctor')) && !empty(document.getElementById('doctor').value)) {
			var doctor = findInList(doctorsList, 'doctor_id', document.getElementById('doctor').value);
			consultingDoctor = doctor['doctor_name'];
		}
		if (empty(consultingDoctor))
			consultingDoctor = consultingDocName;
		var prescribedDoctors = document.getElementsByName('presDocName');

		for (var i=0; i<prescribedDoctors.length; i++) {
			if (!empty(prescribedDoctors[i].value)) {
				if (empty(consultingDoctor))
					consultingDoctor = 	prescribedDoctors[i].value;
				if (consultingDoctor != prescribedDoctors[i].value) {
					alert(getString('js.order.common.one.prescribingdoctor.required'));
					return false;
				}
			}
		}
		return true;
	}
	return true;
}

function getConsultationTypes(billNo) {
	orgId = empty(orgId) ? 'ORG0001' : orgId;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/pages/registration/regUtils.do?_method=getConsultationTypesForBill&bill_no=' + billNo + '&visit_id=' + document.getElementById('patientid').value;
	ajaxobj.open("POST", url, false);
	ajaxobj.send(null);

	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var consultationTypes = " + ajaxobj.responseText);
				addOrderDialog.setNewConsultationTypesList(consultationTypes);
			}
		}
	}
}

function getDoctorCharges(){
		orgId = empty(orgId) ? 'ORG0001' : orgId;
		var ajaxobj = newXMLHttpRequest();

		var url = cpath+"/master/orderItems.do?method=getDoctorAndOtDoctorCharges"
		url = url + "&visit_type=" + visitType;
		url = url + "&billNo=" + document.mainform.billNo.value + '&visit_id=' + document.getElementById('patientid').value;
		url = url + "&includeOtDocCharges=Y";

		ajaxobj.open("POST", url, false);
		ajaxobj.send(null);

		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var docCharges = " + ajaxobj.responseText);
					addOrderDialog.setNewConsultationTypesList(docCharges.doctor_charges);
					addOrderDialog.setAnaesthesiaTypesList(docCharges.anaeTypesJSON);
					addOrderDialog.fillAnesthesiaTypes();
				}
			}
		}

}

