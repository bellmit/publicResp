YAHOO.util.Event.onContentReady('content', initPatientPreviousPrescDialog);
var pPrescDialog = null;
function initPatientPreviousPrescDialog() {
	var dialogDiv = document.getElementById("previousPrescDiv");
	if ( dialogDiv )
		dialogDiv.style.display = 'block';
	pPrescDialog = new YAHOO.widget.Dialog("previousPrescDiv",
			{	width:"800px",
				context : ["previousPrescDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('presc_Ok', 'click', addToTableFromPrevPrescriptions, pPrescDialog, true);
	YAHOO.util.Event.addListener('previousResults_btn', 'click', pPrescDialog.cancel, pPrescDialog, true);
	subscribeEscKeyListener(pPrescDialog);
	pPrescDialog.render();
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

function getPreviousPrescriptions(obj, mrNo, consId, doctorId) {
	validateAddOrEditERxData();

	pPrescDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	pPrescDialog.show();
	if (jsonConsultationIds[0] == undefined)
		consId = 0000;
	else
		consId = jsonConsultationIds[0]['consultation_id'];
		makeAjaxCallForPrev(mrNo, consId, doctorId);
}

var trackConsId = null;

function makeAjaxCallForPrev(mrNo, consId, doctorId) {

	trackConsId = consId;
	if (consId == 0) {
		var table = document.getElementById("previousResultsTable");
		document.getElementById('prevProgressbar').style.visibility = 'hidden';
		var noResultsRow = table.rows[table.rows.length-1];
		noResultsRow.style.display = 'table-row';
		return false;
	}
	document.getElementById('prevProgressbar').style.visibility = 'none';
	//var mrNo = document.getElementById('mrno').value;
	var patientId = document.getElementById('patient_id').value;
	var url = cpath + '/outpatient/OpPrescribeAction.do?_method=getPatientPreviousPrescriptions';
	url += "&mr_no="+mrNo;
	url += "&consultaion_id="+consId;
	url += "&doctor_id="+doctorId;
	url += "&patient_id="+patientId;
	url += "&p_health_authority=" + health_authority;

	YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: populatePreviousPrescriptionsDialog,
			failure: failedToGetPreviousPrescriptions,
			argument: [mrNo, consId, doctorId]
		});
}

function failedToGetPreviousPrescriptions() {
}

var chargesMap = null;

function populatePreviousPrescriptionsDialog(response) {

	var mrNo = decodeURIComponent(response.argument[0]);
	var consultationId = decodeURIComponent(response.argument[1]);
	var doctorId = response.argument[2];

	if (response.responseText != undefined) {

		var previousResults = eval('(' + response.responseText + ')');
		var table = document.getElementById("previousResultsTable");
		var label = null;
		for (var i=1; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		var list = previousResults.list;
		chargesMap = previousResults.chargeMap;
		var doctorDetailsMap = previousResults.docdetailsMap;

			generatePaginationSectionForPrev(mrNo, consultationId, doctorId);

		var noResultsRow = table.rows[table.rows.length-1];
		noResultsRow.style.display = list.length == 0 ? 'table-row' : 'none';

		document.getElementById('consultationDate').textContent = doctorDetailsMap.consultation_date;
		document.getElementById('doctorName').textContent = doctorDetailsMap.doctor_name;
		var primaryDiagnosisEle = document.getElementById('primaryDiagnosis');
		var diagnosis = doctorDetailsMap.diagnosis;

		if (diagnosis != null && diagnosis != '') {
			if (diagnosis.length < 110) {
				primaryDiagnosisEle.textContent = empty(diagnosis) ? '' : diagnosis;
			} else {
				primaryDiagnosisEle.textContent = diagnosis.substring(0, 110);
				primaryDiagnosisEle.setAttribute('title', diagnosis);
			}
		}
		var erx_ref_el = document.getElementById('erx_reference_no');
		var erx_reference_no = '';
		if (erx_ref_el)
			erx_reference_no = erx_ref_el.value;

		for (var i=0; i<list.length; i++) {
			var record = list[i];
			document.getElementById('noofrecords').textContent = jsonConsultationIds.length + ' consultation' +(jsonConsultationIds.length == 1 ? '' : 's') + ' found';

		//	if (record.op_medicine_pres_id != null) {
				var templateRow = table.rows[table.rows.length-2];
				var row = templateRow.cloneNode(true);
				var id = table.rows.length-3;
				row.style.display = '';

				var item_prescriptions_by_generics = record.item_id == null || record.item_id == undefined ? 'true' : 'false';

				table.tBodies[0].insertBefore(row, templateRow);
				var inputEle = document.createElement('input');
				inputEle.setAttribute("type", "checkbox");
				inputEle.setAttribute("name", "select_presc");
				inputEle.setAttribute("value", record.op_medicine_pres_id);
				if (record.item_type == 'Medicine' && !record.non_hosp_medicine && (erx_reference_no != '' || (prescriptions_by_generics != item_prescriptions_by_generics)))
					inputEle.setAttribute("disabled", "true");

				row.cells[0].appendChild(inputEle);

				var itemName = ((record.item_type == 'Medicine' && (record.item_id == null || record.item_id == undefined)) ? record.generic_name : record.item_name);
				if (use_store_items == 'Y' && record.item_type == 'Medicine' && !record.non_hosp_medicine)
					itemName = itemName + '('+record.qtyavl +')';
				setNodeText(row.cells[1], record.item_type + (record.item_type == 'Medicine' && record.non_hosp_medicine ? '[Non Hosp]' : ''));
				setNodeText(row.cells[2], itemName);
				setNodeText(row.cells[3], record.item_form_name);
				var strength = record.item_strength;
				if (!empty(record.unit_name))
					strength += ' ' + record.unit_name;
				setNodeText(row.cells[4], strength);

				setNodeText(row.cells[5], record.admin_strength);
				var details = empty(record.medicine_dosage) ? '' : record.medicine_dosage;
				details += (!empty(details) && !empty(record.duration)) ? '/' : '';
				details += empty(record.duration) ? '' : (' ' + record.duration_units);

				setNodeText(row.cells[6], details, 20, details);
				setNodeText(row.cells[7], record.route_name, 20, record.route_name);

				var quantity = 0;
				var remarks = '';
				if (record.item_type == 'Medicine' || record.item_type == 'NonHospital') {
					quantity = record.medicine_quantity;
					remarks = record.item_type == 'Medicine' && !record.non_hosp_medicine ? record.remarks : record.item_remarks;
				} else {
					quantity = record.service_qty;
					remarks = record.item_remarks;
				}

				setNodeText(row.cells[8], remarks, 20, remarks);
				setNodeText(row.cells[9], record.special_instr, 20, record.special_instr);
				if (record.item_type == 'Medicine' || record.item_type == 'NonHospital' || record.item_type == 'Service')
					setNodeText(row.cells[10], quantity);

				setHiddenValue(id, "prev_item_name", record.item_id == null || record.item_id == undefined ? record.generic_name : record.item_name);
				setHiddenValue(id, "prev_item_id", record.item_id == null || record.item_id == undefined ? record.generic_code : record.item_id);
				setHiddenValue(id, "prev_strength", record.strength);

				setHiddenValue(id, "prev_granular_units", record.granular_units);
				setHiddenValue(id, "prev_admin_strength", record.admin_strength);
				setHiddenValue(id, "prev_frequency", record.frequency);
				setHiddenValue(id, "prev_item_type", record.item_type);
				setHiddenValue(id, "prev_drug_code", record.drug_code);
				setHiddenValue(id, "prev_duration", record.duration);
				setHiddenValue(id, "prev_duration_units", record.duration_units);
				setHiddenValue(id, "prev_medicine_quantity", quantity);
				setHiddenValue(id, "prev_item_remarks", remarks);
				setHiddenValue(id, "prev_special_instr", record.special_instr);
				setHiddenValue(id, "prev_item_master", record.master);
				setHiddenValue(id, "prev_ispackage", record.ispackage);
				setHiddenValue(id, "prev_generic_code", record.generic_code);
				setHiddenValue(id, "prev_generic_name", record.generic_name);
				setHiddenValue(id, "prev_route_id", record.route_id);
				setHiddenValue(id, "prev_route_name", record.route_name);
				setHiddenValue(id, "prev_consumption_uom", record.consumption_uom);
				setHiddenValue(id, "prev_item_form_id", record.item_form_id);
				setHiddenValue(id, "prev_item_form_name", record.item_form_name);
				setHiddenValue(id, "prev_item_strength", record.item_strength);
				setHiddenValue(id, "prev_item_strength_units", record.item_strength_units);
				setHiddenValue(id, "prev_item_strength_unit_name", empty(record.unit_name) ? '' : record.unit_name);
				setHiddenValue(id, "prev_charge", record.charge);
				setHiddenValue(id, "prev_discount", record.discount);
				//setHiddenValue(id, "prev_display_order", record.);
				setHiddenValue(id, "prev_tooth_num_required", record.tooth_num_required);
				setHiddenValue(id, "prev_priorAuth", record.prior_auth_required);
				setHiddenValue(id, "prev_non_hosp_medicine", record.non_hosp_medicine);
				setHiddenValue(id, "prev_insurance_category_id", record.insurance_category_id);
				setHiddenValue(id, "prev_insurance_category_name", record.insurance_category_name);
		//	}
		}
	}
	document.getElementById('prevProgressbar').style.visibility = 'hidden';
}

function generatePaginationSectionForPrev(mrNo, consultationId, doctorId) {
	var div = document.getElementById('prevPaginationDiv');
	div.innerHTML = '';
	var consultIdsLen = jsonConsultationIds.length;
	if (consultIdsLen <= 1) {

	} else {
		var considIndex=0;
		var prevConsultationId = null;
		for (var i=0; i<jsonConsultationIds.length; i++) {
			if (consultationId == jsonConsultationIds[i]['consultation_id']) {
				considIndex = i;
			}
		}
		if (considIndex > 0) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCallForPrev("'+encodeURIComponent(mrNo)+'", "'+jsonConsultationIds[considIndex-1]['consultation_id']+'", "'+doctorId+'"'+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}
		if (considIndex > 0 && considIndex < consultIdsLen) {
			var txtEl = document.createTextNode(' | ');
			div.appendChild(txtEl);
		}
		if (considIndex < consultIdsLen-1) {
			var txtEl = document.createTextNode('Next>>');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCallForPrev("'+encodeURIComponent(mrNo)+'", "'+jsonConsultationIds[considIndex+1]['consultation_id']+'", "'+doctorId+'"'+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}


function addToTableFromPrevPrescriptions() {

	var selected_presc = document.getElementsByName('select_presc');
	for (var i=0; i<selected_presc.length; i++) {
		if (selected_presc[i].checked) {
			if (ajaxInProgress) {
				setTimeout("addToTable()", 100);
				return false
			}

			var id = getNumCharges('itemsTable');
		   	var table = document.getElementById("itemsTable");
			var templateRow = table.rows[getTemplateRow('itemsTable')];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
		   	row.id = "itemRow" + id;

		   	var cell = null;

		   	var packageSize = '';
			var price = 0;
			var prescId = selected_presc[i].value;
			var itemType = document.getElementsByName('prev_item_type')[i].value;
			var prevCharge = document.getElementsByName('prev_charge')[i].value;
			var prevDiscount = document.getElementsByName('prev_discount')[i].value;
			
			var chargeHeadId = '';
			var chargeGroupId = '';
			var medDiscount = 0;
			var discountType = '';
			var itemCode = '';
			var batchNo = '';
			var itemBatchId = '';
			var itemDiscount = 0;
			
			if (itemType == 'Medicine') {
				if (useGenerics || use_store_items == 'N') {
				} else {
					var rateDetails = chargesMap[prescId];
					if (rateDetails != null) {
						price = empty(rateDetails.selling_price) ? '' : rateDetails.selling_price;
						medDiscount = empty(rateDetails.meddisc) ? 0 : rateDetails.meddisc;
						
						discountType = document.getElementById('pharmacy_discount_type').value;
						var discountPer = document.getElementById('pharmacy_discount_percentage').value;
						if (!empty(medDiscount)) {
							medDiscount += discountPer;  
						} else {
							if (!empty(discountPer))
								medDiscount = discountPer;
						}
						itemCode = empty(rateDetails.item_code) ? '' : rateDetails.item_code;
						packageSize = empty(rateDetails.issue_base_unit) ? '' : rateDetails.issue_base_unit;
						batchNo = empty(rateDetails.batch_no) ? '' : rateDetails.batch_no;
						itemBatchId = empty(rateDetails.item_batch_id) ? '' : rateDetails.item_batch_id;
					}
				}
			} else {
				if (itemType == 'Service') {
					chargeHeadId = 'SERSNP';
					chargeGroupId = 'SNP';
				} else if (itemType == 'Doctor') {
					chargeHeadId = 'DOC';
					chargeGroupId = 'OPDOC';
				} else if (itemType == 'Operation') {
					chargeHeadId = 'OPE';
					chargeGroupId = 'TCOPE';
				} else if (itemType == 'Inv.') {
					chargeHeadId = '';
					chargeGroupId = '';
				}
				
				var charge = empty(prevCharge) ? 0 : prevCharge;
				var discount = empty(prevDiscount) ? 0 : prevDiscount;
				price = charge - discount;
				itemDiscount = discount;
			}
			var non_hosp_medicine = document.getElementsByName('prev_non_hosp_medicine')[i].value;
			if (itemType == 'Medicine' && non_hosp_medicine == 'false' && mod_eclaim_erx == 'Y') {
				// by default mark the item for sending to erx.
				document.getElementsByName('chk_send_item_for_erx')[id].checked = true;
				document.getElementsByName('chk_send_item_for_erx')[id].disabled = false;
				setHiddenValue(id, "send_item_for_erx", 'Y');
			} else {
				document.getElementsByName('chk_send_item_for_erx')[id].checked = false;
				document.getElementsByName('chk_send_item_for_erx')[id].disabled = true;
			}

			price = getPaise(price);
			var pkg_size = getAmount(packageSize);
			var itemType = document.getElementsByName('prev_item_type')[i].value;
			var itemName = document.getElementsByName('prev_item_name')[i].value;
			var drugCode = document.getElementsByName('prev_drug_code')[i].value;
		   	var itemId = document.getElementsByName('prev_item_id')[i].value;
		   	var adminStrength = document.getElementsByName('prev_admin_strength')[i].value;
		   	var granular_units = document.getElementsByName('prev_granular_units')[i].value;
		   	var frequency = document.getElementsByName('prev_frequency')[i].value;
		   	var strength = document.getElementsByName('prev_strength')[i].value;
		   	var duration = document.getElementsByName('prev_duration')[i].value;
		   	var duration_units = document.getElementsByName('prev_duration_units')[i].value;
		   	var qty = document.getElementsByName('prev_medicine_quantity')[i].value;
		   	var remarks = document.getElementsByName('prev_item_remarks')[i].value;
		   	var spl_instruction = document.getElementsByName('prev_special_instr')[i].value;
		   	var master = document.getElementsByName('prev_item_master')[i].value;
		   	var genericCode = document.getElementsByName('prev_generic_code')[i].value;
		   	var genericName = document.getElementsByName('prev_generic_name')[i].value;
		   	var ispackage = document.getElementsByName('prev_ispackage')[i].value;
		   	var consumption_uom = document.getElementsByName('prev_consumption_uom')[i].value;
		   	var routeId = document.getElementsByName('prev_route_id')[i].value;
		   	var routeName = document.getElementsByName('prev_route_name')[i].value;
		   	var priorAuth = document.getElementsByName('prev_priorAuth')[i].value;
		   	var item_form_id = document.getElementsByName('prev_item_form_id')[i].value;
		   	var item_strength = document.getElementsByName('prev_item_strength')[i].value;
		   	var item_strength_units = document.getElementsByName('prev_item_strength_units')[i].value;
		   	var item_strength_unit_name = document.getElementsByName('prev_item_strength_unit_name')[i].value;
		   	var item_form_name = document.getElementsByName('prev_item_form_name')[i].value;
		   	var tooth_num_required = document.getElementsByName('prev_tooth_num_required')[i].value;
		   	var ins_cat_id = document.getElementsByName('prev_insurance_category_id')[i].value;
		   	var ins_cat_name = document.getElementsByName('prev_insurance_category_name')[i].value;
		   	var item_pkg_price = 0;
			var item_unit_price = 0;
			if(itemType == 'Service')
		   		qty = 1;

		   	setNodeText(row.cells[ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine == 'true' ? '[Non Hosp]' : ''));
		   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
		   	setNodeText(row.cells[INS_CAT_NAME], ins_cat_name, 20);
		   	if(itemType == 'Service')
		   		setNodeText(row.cells[QTY], qty);

			if (itemType == 'Medicine' || itemType == 'NonHospital') {

				setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
		   		var details = "";
		   		if (frequency != '' || duration != '')
		   			details = frequency + " / " + duration + ' ' + duration_units;
		   		setNodeText(row.cells[DETAILS], details, 20);
				setNodeText(row.cells[QTY], qty);
				if (item_form_id != '') {
					setNodeText(row.cells[FORM], item_form_name, 15);
				}
				setNodeText(row.cells[STRENGTH], item_strength + ' ' + item_strength_unit_name, 15);

				setHiddenValue(id, "generic_code", genericCode);
				setHiddenValue(id, "drug_code", drugCode);
				setHiddenValue(id, "generic_name", genericName);
				setHiddenValue(id, "granular_units", granular_units);
				setHiddenValue(id, "admin_strength", adminStrength);
				setHiddenValue(id, "frequency", frequency);
				setHiddenValue(id, "strength", strength);
				setHiddenValue(id, "duration", duration);
				setHiddenValue(id, "duration_units", duration_units);
				setHiddenValue(id, "medicine_quantity", qty);
				setHiddenValue(id, "item_form_id", item_form_id);
				setHiddenValue(id, "item_strength", item_strength);
				setHiddenValue(id, "item_strength_units", item_strength_units);
				if (pkg_size != '' && price != '' && qty != '') {
					item_unit_price = (price/pkg_size) * qty;
					item_pkg_price = Math.ceil(qty/pkg_size) * price;
				}
				
				if (itemType == 'Medicine') {
					// these parameters are used to calculate the patient copay.
					
					setHiddenValue(id, 'temp_charge_id', "_"+id);
					//setHiddenValue(id, "chargeId", "_"+id);
					setHiddenValue(id, 'medDiscRS', medDiscount);
					setHiddenValue(id, 'medDiscType', discountType);
					setHiddenValue(id, 'origRate', price == '' ? 0 : formatAmountPaise(price));
					setHiddenValue(id, 'itemCode', itemCode);
					setHiddenValue(id, 'batchNo', batchNo);
					setHiddenValue(id, 'itemBatchId', itemBatchId);
					setHiddenValue(id, "qty", qty);
					var amt = formatAmountPaise(item_unit_price - getPaise(medDiscount))
					setHiddenValue(id, "amt", amt);
					setHiddenValue(id, "insuranceCategoryId", ins_cat_id);
				}
			} else {
				// these parameters are used to calculate the patient copay.
			   	
			   	setHiddenValue(id, "chargeId", "_"+id);
				setHiddenValue(id, "chargeHeadId", chargeHeadId);
				setHiddenValue(id, "chargeGroupId", chargeGroupId);
				setHiddenValue(id, "disc", itemDiscount);
				setHiddenValue(id, "insuranceCategoryId", ins_cat_id);
				
				var amt = price;
				if (itemType == 'Service')
					amt = amt * qty;
				setHiddenValue(id, "amt", formatAmountPaise(amt));
				
				item_pkg_price = price;
				item_unit_price = amt;
			}

			setNodeText(row.cells[PKG_PRICE], item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
			setNodeText(row.cells[UNIT_PRICE], item_unit_price == 0? '' : formatAmountPaise(item_unit_price));
			setNodeText(row.cells[ROUTE], routeName);
			setNodeText(row.cells[REMARKS], remarks, 30);
			setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 30);
			if(requireERxAuth)
				setNodeText(row.cells[DRUG_CODE], drugCode);

			setHiddenValue(id, "consumption_uom", consumption_uom);
			setHiddenValue(id, "item_prescribed_id", "_");
			setHiddenValue(id, "itemType", itemType);
			setHiddenValue(id, "item_name", itemName);
			setHiddenValue(id, "item_id", itemId);
			setHiddenValue(id, "item_remarks", remarks);
			setHiddenValue(id, "special_instr", spl_instruction);
			setHiddenValue(id, "item_master", master);
			setHiddenValue(id, "ispackage", ispackage);
			setHiddenValue(id, "drug_code", drugCode);
			setHiddenValue(id, "pkg_size", pkg_size == '' ? '' : pkg_size);
			setHiddenValue(id, "pkg_price", price == '' ? '' : formatAmountPaise(price));
			setHiddenValue(id, "item_pkg_price", item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
			setHiddenValue(id, "item_unit_price", item_unit_price == 0 ? '' : formatAmountPaise(item_unit_price));
			setHiddenValue(id, "route_id", routeId);
			setHiddenValue(id, "route_name", routeName);
			setHiddenValue(id, "priorAuth", priorAuth);
			setHiddenValue(id, "issued", "N");
			setHiddenValue(id, "addToFavourite", false);
			setHiddenValue(id, "tooth_num_required", tooth_num_required);
			setHiddenValue(id, "service_qty", qty);
			setHiddenValue(id, "non_hosp_medicine", non_hosp_medicine);
			if (itemType == 'Operation') {
				setHiddenValue(id, "op_id", itemId);
			}
			if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
			} else {
				estimateTotal();
			}
			itemsAdded++;
			setRowStyle(id);
			
			var visitId = document.getElementById('patient_id').value;
			if (itemType == 'Medicine') {
				if (non_hosp_medicine != 'true' && !useGenerics) {
					setHiddenValue(id, "medicineId", itemId);
					getSaleItemClaim(visitId, document.prescribeForm); 
				}
			} else {
				getItemClaims(visitId, document.prescribeForm);
			}
			// setting proper value in CROSS_CODE_STATUS column
			if (mod_ceed_enabled) {
	   			var div = document.createElement("div");
	   			var textnode = document.createTextNode(" Not Initiated");
	   			div.setAttribute("class", "ceedcircle black");
	   			// removing all existing children
	   			while (row.cells[CROSS_CODE_STATUS].firstChild) {
	   				row.cells[CROSS_CODE_STATUS].removeChild(row.cells[CROSS_CODE_STATUS].firstChild);
	   			}
	   			row.cells[CROSS_CODE_STATUS].appendChild(div);
	   			row.cells[CROSS_CODE_STATUS].appendChild(textnode);
		   	}
		}
	}
	
	
	document.getElementById("previousPrescDiv").scrollIntoView(true);
	pPrescDialog.cancel();

}