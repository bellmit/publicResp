var useGenerics = (use_store_items == 'Y' && prescriptions_by_generics == 'true');
var showRateDetails = false;

function markForErx(obj) {
	var row = findAncestor(obj, 'TR');
	var send_for_erx = getElementByName(row, 'send_item_for_erx');
	send_for_erx.value = obj.checked ? 'Y' : 'N';
}

function initOneTimePrescriptions() {
	initItemDialog();
	initEditItemDialog();
	initFrequencyAutoComplete();
	initInstructionAutoComplete();
	initDoctorFavouritesDialog();
	// Display amounts based on action rights and rate plan.
	showRateDetails = displayAmounts();
	initToothNumberDialog();

}

function modifyUOMLabel(obj, prefix) {
	document.getElementById(prefix+ '_consumption_uom_label').textContent = obj.value;
}

var favouritesDialog = null;
function initDoctorFavouritesDialog() {
	var dialogDiv = document.getElementById("doctorFavouritesDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	favouritesDialog = new YAHOO.widget.Dialog("doctorFavouritesDialog",
			{	width:"1093px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('fav_Ok', 'click', addToTableFromFavourites, favouritesDialog, true);
	YAHOO.util.Event.addListener('fav_Close', 'click', cancelFavourites, favouritesDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelFavourites,
	                                                scope:favouritesDialog,
	                                                correctScope:true } );
	favouritesDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	favouritesDialog.render();
}

var doc_fav_current_page = 0;

function showFavouritesDialog(obj, operation) {
	var row = getThisRow(obj);

	var page = doc_fav_current_page;
	if (operation == "prev")
		page -= 1;
	else if (operation == "next")
		page += 1;
	else
		page = 0;
	validateAddOrEditERxData();
 	var selected_fav = document.getElementsByName('select_favourite');
// 	if (selected_fav.length == 1) {
	var ajaxReqObject = new XMLHttpRequest();
	var url = cpath+"/outpatient/OpPrescribeAction.do?_method=getPrescriptionFavourites&consultation_id="+encodeURIComponent(consultationId)+"&page_no="+page;
	ajaxReqObject.onreadystatechange = function() {
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				loadFavourites(ajaxReqObject.responseText);
			}
		}
	}
	ajaxReqObject.open("GET",url.toString(), false);
	ajaxReqObject.send(null);
//	}

	favouritesDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	favouritesDialog.show();
	// uncheck all the favourites.
	for (var i=0; i<selected_fav.length; i++) {
		selected_fav[i].checked = false;
	}
}

var fav_charges = null;
var doctor_presc_favourites = [];
var doc_fav_col_index = 1
var FAV_ITEM_TYPE = doc_fav_col_index++, FAV_DISPLAY_ORDER = doc_fav_col_index++,
	FAV_ITEM_NAME = doc_fav_col_index++, FAV_ITEM_FORM = doc_fav_col_index++,
	FAV_ITEM_STRENGTH = doc_fav_col_index++, FAV_ITEM_ADMIN_STRENGTH = doc_fav_col_index++,
	FAV_ITEM_DETAILS = doc_fav_col_index++, FAV_MEDICINE_ROUTE = doc_fav_col_index++,
	FAV_ITEM_INSTRUCTIONS = doc_fav_col_index++, FAV_ITEM_SPL_INSTRUCTIONS = doc_fav_col_index++;
	FAV_ITEM_QTY = doc_fav_col_index++;
function loadFavourites(responseText) {
	if (responseText == null) return;
	if (responseText == "") return;
    eval("var favourites = " + responseText);
    if (favourites != null) {
		var fav_table = document.getElementById("doctorPrescriptionFavouritesTable");
		var fav_table_rows = fav_table.rows;
		var fav_table_rows_length = fav_table_rows.length;
		for (var i=1; i< fav_table_rows_length -1; i++) {
			fav_table.deleteRow(1);
		}
		doc_fav_current_page = favourites.page_no;
		if (favourites.prev) {
			document.getElementById("fav_prev_link").style.display = 'block';
		} else {
			document.getElementById("fav_prev_link").style.display = 'none'
		}
		if (favourites.next) {
			document.getElementById("fav_next_link").style.display = 'block';
		} else {
			document.getElementById("fav_next_link").style.display = 'none'
		}
		if (favourites.next && favourites.prev) {
			document.getElementById("fav_pipe").style.display = 'block';
		} else {
			document.getElementById("fav_pipe").style.display = 'none'
		}

    	fav_charges = favourites.fav_charges;
    	var doctor_presc_favourites = favourites.doctor_favourites;
    	for (var i=0; i<doctor_presc_favourites.length; i++) {
    		var favourite = doctor_presc_favourites[i];
	    	var id = getNumCharges('doctorPrescriptionFavouritesTable');
		   	var table = document.getElementById("doctorPrescriptionFavouritesTable");
			var templateRow = table.rows[getTemplateRow('doctorPrescriptionFavouritesTable')];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
		   	row.id = "doctorPrescriptionFavouritesTableRow" + id;

		   	var itemType = favourite.item_type;
		   	var non_hosp_medicine = favourite.non_hosp_medicine;
		   	var itemName = "";
		   	var itemId = "";
		   	if (itemType == "Medicine" && empty(favourite.item_id) && !empty(favourite.generic_name)) {
		   		itemName = favourite.generic_name;
		   		itemId = favourite.generic_code;
		   	} else {
		   		itemName = favourite.item_name;
		   		itemId = favourite.item_id;
		   	}

		   	var details = null;
		   	if ((itemType == 'Medicine' || itemType == 'NonHospital')
		   			&& (!empty(favourite.medicine_dosage) || !empty(favourite.duration))) {
		   		details = favourite.medicine_dosage;
		   		if (!empty(favourite.duration)) {
		   			details += " / " + favourite.duration + " " + (empty(favourite.duration_units) ? "" : favourite.duration_units);
		   		}
		   	}
		   	var itemStrength = "";
		   	if (!empty(favourite.item_strength)) {
		   		itemStrength = favourite.item_strength + " " + favourite.unit_name;
		   	}
		   	setNodeText(row.cells[FAV_ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine ? "[Non Hosp]" : ""), 20);
		   	setNodeText(row.cells[FAV_DISPLAY_ORDER], favourite.display_order);
		   	setNodeText(row.cells[FAV_ITEM_NAME], itemName, 20);
		   	setNodeText(row.cells[FAV_ITEM_FORM], favourite.item_form_name, 15);
		   	setNodeText(row.cells[FAV_ITEM_STRENGTH], itemStrength, 15);
		   	setNodeText(row.cells[FAV_ITEM_ADMIN_STRENGTH], favourite.admin_strength, 15);
		   	setNodeText(row.cells[FAV_ITEM_DETAILS], details, 20);
		   	setNodeText(row.cells[FAV_MEDICINE_ROUTE], favourite.route_name);
		   	setNodeText(row.cells[FAV_ITEM_INSTRUCTIONS], favourite.item_remarks, 20);
		   	setNodeText(row.cells[FAV_ITEM_SPL_INSTRUCTIONS], favourite.special_instr, 20);
		   	setNodeText(row.cells[FAV_ITEM_QTY], empty(favourite.medicine_quantity) ? 1 : favourite.medicine_quantity);

		   	document.getElementsByName('select_favourite')[id].value = favourite.favourite_id;
		   	if (itemType == 'Medicine' || itemType == 'NonHospital') {

				setHiddenValue(id, "fav_granular_units", empty(favourite.granular_units) ? "" : favourite.granular_units);
				setHiddenValue(id, "fav_generic_code", empty(favourite.generic_code) ? "" : favourite.generic_code);
				setHiddenValue(id, "fav_drug_code", empty(favourite.drug_code) ? "" : favourite.drug_code);
				setHiddenValue(id, "fav_generic_name", empty(favourite.generic_name) ? "" : favourite.generic_name);
				setHiddenValue(id, "fav_admin_strength",  empty(favourite.admin_strength) ? "" : favourite.admin_strength);
				setHiddenValue(id, "fav_frequency", empty(favourite.frequency) ? "" : favourite.frequency);
				setHiddenValue(id, "fav_strength",  empty(favourite.strength) ? "" : favourite.strength);
				setHiddenValue(id, "fav_duration", empty(favourite.duration) ? "" : favourite.duration);
				setHiddenValue(id, "fav_duration_units", empty(favourite.duration_units) ? "" : favourite.duration_units);
				setHiddenValue(id, "fav_medicine_quantity", empty(favourite.medicine_quantity) ? 1 : favourite.medicine_quantity);
				setHiddenValue(id, "fav_item_form_id", favourite.item_form_id == 0 ? '' : favourite.item_form_id);
				setHiddenValue(id, "fav_item_form_name", empty(favourite.item_form_name) ? "" : favourite.item_form_name);
				setHiddenValue(id, "fav_item_strength", empty(favourite.item_strength) ? "" : favourite.item_strength);
				setHiddenValue(id, "fav_item_strength_units", empty(favourite.item_strength_units) ? "" : favourite.item_strength_units);
				setHiddenValue(id, "fav_item_strength_unit_name", empty(favourite.unit_name) ? "" : favourite.unit_name);

				if (itemType == 'Medicine') {
					// these parameters are used to calculate the patient copay.

					setHiddenValue(id, "fav_insurance_category_id", empty(favourite.insurance_category_id) ? "" : favourite.insurance_category_id);
					setHiddenValue(id, "fav_insurance_category_name", empty(favourite.insurance_category_name) ? "" : favourite.insurance_category_name);
				}
			} else {
				// these parameters are used to calculate the patient copay.

			   	setHiddenValue(id, "fav_insurance_category_id", empty(favourite.insurance_category_id) ? "" : favourite.insurance_category_id);
				setHiddenValue(id, "fav_insurance_category_name", empty(favourite.insurance_category_name) ? "" : favourite.insurance_category_name);
			}
		   	setHiddenValue(id, "fav_favourite_id", favourite.favourite_id);
		   	setHiddenValue(id, "fav_display_order", favourite.display_order);
		   	setHiddenValue(id, "fav_consumption_uom", empty(favourite.consumption_uom) ? "" : favourite.consumption_uom);
		   	setHiddenValue(id, "fav_itemType", favourite.item_type);
			setHiddenValue(id, "fav_item_name", itemName);
			setHiddenValue(id, "fav_item_id", itemId);
			setHiddenValue(id, "fav_item_remarks", empty(favourite.item_remarks) ? "" : favourite.item_remarks);
			setHiddenValue(id, "fav_special_instr", empty(favourite.special_instr) ? "" : favourite.special_instr);
			setHiddenValue(id, "fav_item_master", favourite.master);
			setHiddenValue(id, "fav_drug_code", empty(favourite.drug_code) ? "" : favourite.drug_code);
			setHiddenValue(id, "fav_ispackage", empty(favourite.ispackage) ? "" : favourite.ispackage);
			setHiddenValue(id, "fav_route_id", empty(favourite.route_id) ? "" : favourite.route_id);
			setHiddenValue(id, "fav_route_name", empty(favourite.route_name) ? "" : favourite.route_name);
			setHiddenValue(id, "fav_priorAuth", (!empty(document.prescribeForm.tpa_id.value) ? favourite.prior_auth_required : ''));
			setHiddenValue(id, "fav_tooth_num_required", empty(favourite.tooth_num_required) ? "" : favourite.tooth_num_required);
			setHiddenValue(id, "fav_non_hosp_medicine", favourite.non_hosp_medicine);

    	}

    }
}

function addToTableFromFavourites() {

	var selected_fav = document.getElementsByName('select_favourite');
	for (var i=0; i<selected_fav.length; i++) {
		if (selected_fav[i].checked) {
			var itemType = document.getElementsByName('fav_itemType')[i].value;

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
			var favouriteId = selected_fav[i].value;

			var chargeHeadId = '';
			var chargeGroupId = '';
			var medDiscount = 0;
			var discountType = '';
			var itemCode = '';
			var batchNo = '';
			var itemBatchId = '';
			var itemDiscount = 0;

			var non_hosp_medicine = document.getElementsByName('fav_non_hosp_medicine')[i].value;
			if (itemType == 'Medicine') {
				if (useGenerics || use_store_items == 'N') {
				} else {
					var rateDetails = fav_charges[itemType + "_" + non_hosp_medicine + "_" + favouriteId];
					if (rateDetails != null) {
						price = empty(rateDetails.charge) ? '' : rateDetails.charge;
						medDiscount = empty(rateDetails.discount) ? 0 : rateDetails.discount;

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
				var rateDetails = fav_charges[itemType + "_" + non_hosp_medicine + "_" + favouriteId];
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
				if (rateDetails != null) {
					var charge = empty(rateDetails.charge) ? 0 : rateDetails.charge;
					var discount = empty(rateDetails.discount) ? 0 : rateDetails.discount;
					price = charge - discount;
					itemDiscount = discount;
				}
			}
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
			var itemName = document.getElementsByName('fav_item_name')[i].value;
		   	var itemId = document.getElementsByName('fav_item_id')[i].value;
		   	var adminStrength = document.getElementsByName('fav_admin_strength')[i].value;
		   	var granularUnit = document.getElementsByName('fav_granular_units')[i].value;
		   	var frequency = document.getElementsByName('fav_frequency')[i].value;
		   	var strength = document.getElementsByName('fav_strength')[i].value;
		   	var duration = document.getElementsByName('fav_duration')[i].value;
		   	var duration_units = document.getElementsByName('fav_duration_units')[i].value;
		   	var qty = document.getElementsByName('fav_medicine_quantity')[i].value;
		   	if(itemType == 'Service')
		   		qty = 1;
		   	var remarks = document.getElementsByName('fav_item_remarks')[i].value;
		   	var spl_instruction = document.getElementsByName('fav_special_instr')[i].value;
		   	var master = document.getElementsByName('fav_item_master')[i].value;
		   	var genericCode = document.getElementsByName('fav_generic_code')[i].value;
		   	var drugCode = document.getElementsByName('fav_drug_code')[i].value;
		   	var genericName = document.getElementsByName('fav_generic_name')[i].value;
		   	var ispackage = document.getElementsByName('fav_ispackage')[i].value;
		   	var consumption_uom = document.getElementsByName('fav_consumption_uom')[i].value;
		   	var routeId = document.getElementsByName('fav_route_id')[i].value;
		   	var routeName = document.getElementsByName('fav_route_name')[i].value;
		   	var priorAuth = document.getElementsByName('fav_priorAuth')[i].value;
		   	var item_form_id = document.getElementsByName('fav_item_form_id')[i].value;
		   	var item_strength = document.getElementsByName('fav_item_strength')[i].value;
		   	var item_strength_units = document.getElementsByName('fav_item_strength_units')[i].value;
		   	var item_strength_unit_name = document.getElementsByName('fav_item_strength_unit_name')[i].value;
		   	var item_form_name = document.getElementsByName('fav_item_form_name')[i].value;
		   	var tooth_num_required = document.getElementsByName('fav_tooth_num_required')[i].value;
		   	var fav_ins_cat_id = document.getElementsByName('fav_insurance_category_id')[i].value;
		   	var fav_ins_cat_name = document.getElementsByName('fav_insurance_category_name')[i].value;

			var item_pkg_price = 0;
			var item_unit_price = 0;

		   	setNodeText(row.cells[ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine == 'true' ? '[Non Hosp]' : ''));
		   	setNodeText(row.cells[ITEM_NAME], itemName, 20);
		   	setNodeText(row.cells[INS_CAT_NAME], fav_ins_cat_name, 20);
		   	if(itemType == 'Service')
		   		setNodeText(row.cells[QTY], qty);

		   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		   		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
		   		var details = "";
		   		if (frequency != '' || duration != '')
		   			details = frequency + " / " + duration + " " + duration_units;
		   		setNodeText(row.cells[DETAILS], details, 20);
				setNodeText(row.cells[QTY], qty);
				if (item_form_id != '') {
					setNodeText(row.cells[FORM], item_form_name, 15);
				}
				setNodeText(row.cells[STRENGTH], item_strength + ' ' + item_strength_unit_name, 15);

				setHiddenValue(id, "granular_units", granularUnit);
				setHiddenValue(id, "generic_code", genericCode);
				setHiddenValue(id, "drug_code", drugCode);
				setHiddenValue(id, "generic_name", genericName);
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
					var discAmount = item_unit_price * getAmount(medDiscount)/100;
                    var amt = formatAmountPaise(item_unit_price - discAmount);
					setHiddenValue(id, "amt", amt);
					setHiddenValue(id, "insuranceCategoryId", fav_ins_cat_id);
				}
			} else {
				// these parameters are used to calculate the patient copay.

			   	setHiddenValue(id, "chargeId", "_"+id);
				setHiddenValue(id, "chargeHeadId", chargeHeadId);
				setHiddenValue(id, "chargeGroupId", chargeGroupId);
				setHiddenValue(id, "disc", itemDiscount);
				setHiddenValue(id, "insuranceCategoryId", fav_ins_cat_id);
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
			setNodeText(row.cells[REMARKS], remarks, 20);
			setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 20);
			if (requireERxAuth)
				setNodeText(row.cells[DRUG_CODE], drugCode);

			setHiddenValue(id, "consumption_uom", consumption_uom);
			setHiddenValue(id, "item_prescribed_id", "_");
			setHiddenValue(id, "itemType", itemType);
			setHiddenValue(id, "item_name", itemName);
			setHiddenValue(id, "item_id", itemId);
			setHiddenValue(id, "item_remarks", remarks);
			setHiddenValue(id, "special_instr", spl_instruction);
			setHiddenValue(id, "item_master", master);
			setHiddenValue(id, "drug_code", drugCode);
			setHiddenValue(id, "ispackage", ispackage);
			setHiddenValue(id, "pkg_size", pkg_size == '' ? '' : pkg_size);
			setHiddenValue(id, "pkg_price", price == '' ? '' : formatAmountPaise(price));
			setHiddenValue(id, "item_pkg_price", item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
			setHiddenValue(id, "item_unit_price", item_unit_price == 0 ? '' : formatAmountPaise(item_unit_price));
			setHiddenValue(id, "route_id", routeId);
			setHiddenValue(id, "route_name", routeName);
			setHiddenValue(id, "priorAuth", priorAuth);
			setHiddenValue(id, "issued", "P");
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
				if (non_hosp_medicine != 'true' && use_store_items == 'Y' && prescriptions_by_generics != 'true') {
					setHiddenValue(id, "medicineId", itemId);
					getSaleItemClaim(visitId, document.prescribeForm);
				}
			} else {
				getItemClaims(visitId, document.prescribeForm);
			}
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

	document.getElementById("doctorFavouritesDialog").scrollIntoView(true);
	favouritesDialog.cancel();
}

function cancelFavourites() {
	document.getElementById("doctorFavouritesDialog").scrollIntoView(true);
	favouritesDialog.cancel();
}

var toothNumDialog = null;
function initToothNumberDialog() {
	var dialogDiv = document.getElementById("toothNumDialog");
	dialogDiv.style.display = 'block';
	toothNumDialog = new YAHOO.widget.Dialog("toothNumDialog",
			{	width:"600px",
				context : ["toothNumDialog", "tr", "tl"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('toothNumDialog_ok', 'click', updateToothNumbers, toothNumDialog, true);
	YAHOO.util.Event.addListener('toothNumDialog_close', 'click', cancelToothNumDialog, toothNumDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelToothNumDialog,
	                                                scope:toothNumDialog,
	                                                correctScope:true } );
	toothNumDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	toothNumDialog.render();
}

function showToothNumberDialog(action, obj) {
	var els = document.getElementsByName('d_chk_tooth_number');
	document.getElementById('dialog_type').value = action;
	var tnumbers = document.getElementById((action == 'add' ? 'd' : 'ed') + '_tooth_number').value.split(",");
	for (var i=0; i<els.length; i++) {
		var checked = false;
		for (var j=0; j<tnumbers.length; j++) {
			if (els[i].value == tnumbers[j]) {
				checked = true;
				break;
			}
		}
		els[i].checked = checked;
	}
	toothNumDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	toothNumDialog.show();
	childDialog = true;
}

function updateToothNumbers() {
	var els = document.getElementsByName('d_chk_tooth_number');
	var tooth_numbers = '';
	var tooth_numbers_text = '';
	var checked_toothNos = 0;
	for (var i=0; i<els.length; i++) {
		if (!els[i].checked) continue;

		if (tooth_numbers != '') {
			tooth_numbers += ',';
			tooth_numbers_text += ',';
		}
		if (checked_toothNos%10 == 0)
			tooth_numbers_text += '\n';

		checked_toothNos++;
		tooth_numbers += els[i].value;
		tooth_numbers_text += els[i].value;
	}
	var action = document.getElementById('dialog_type').value;
	document.getElementById(action == 'add' ? 'd_tooth_number' : 'ed_tooth_number').value = tooth_numbers;
	document.getElementById(action == 'add' ? 'dToothNumberDiv' : 'edToothNumberDiv').textContent = tooth_numbers_text;
	if (action != 'add')
		fieldEdited = true;
	childDialog = null;
	this.cancel();
}

function cancelToothNumDialog() {
	childDialog = null;
	toothNumDialog.cancel();
}


function initItemDialog() {
	var dialogDiv = document.getElementById("addItemDialog");
	dialogDiv.style.display = 'block';
	addItemDialog = new YAHOO.widget.Dialog("addItemDialog",
			{	width:"650px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('Add', 'click', addToTable, addItemDialog, true);
	YAHOO.util.Event.addListener('Close', 'click', handleAddItemCancel, addItemDialog, true);
	var enterKeyListener = new YAHOO.util.KeyListener("addItemDialogFields", { keys:13 },
				{ fn:onEnterKeyItemDialog, scope:addItemDialog, correctScope:true } );
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddItemCancel,
	                                                scope:addItemDialog,
	                                                correctScope:true } );
	addItemDialog.cfg.setProperty("keylisteners", [escKeyListener, enterKeyListener]);
	addItemDialog.render();
}

function onEnterKeyItemDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new autocomplete.)
	document.getElementById("d_itemName").blur();
	addToTable();
}

function handleAddItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		this.cancel();
	}
}


function getItemType() {
	var itemTypeObj = document.getElementsByName('d_itemType');
	for (var i=0; i<itemTypeObj.length; i++) {
		if (itemTypeObj[i].checked)
			return itemTypeObj[i].value;
	}
	return null;
}


var parentDialog = null;
var childDialog = null;
function showAddItemDialog(obj) {

	var itemType = getItemType();
	var non_hosp_medicine = false;
	if (itemType == 'Medicine') {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'table-cell';
		document.getElementById('d_non_hosp_medicine_div1').style.display = 'table-cell';
		non_hosp_medicine = document.getElementById('d_non_hosp_medicine').checked;
	} else {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'none';
		document.getElementById('d_non_hosp_medicine_div1').style.display = 'none';
	}

	var row = getThisRow(obj);

	addItemDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addItemDialog.show();

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient
		&& ((itemType == 'Medicine' && !non_hosp_medicine && !useGenerics)
				|| itemType == 'Inv.'
				|| itemType == 'Service'
				|| itemType == 'Operation'
				|| itemType == 'Doctor')) {
		document.getElementById('d_priorAuthLabelTd').style.display = 'block';
		document.getElementById('d_priorAuth_label').style.display = 'table-cell';
		document.getElementById('d_category_payable_show').style.display = 'table-row';
	//	document.getElementById('d_category_payable_show').style.visibility='visible';
	} else {
		document.getElementById('d_priorAuthLabelTd').style.display = 'none';
		document.getElementById('d_priorAuth_label').style.display = 'none';
		document.getElementById('d_category_payable_show').style.display = 'none';
	}

	if (document.getElementById('d_markPriorAuthReqTd') != null) {
		if (isInsurancePatient
				&& (itemType == 'Inv.' || itemType == 'Service' || itemType == 'Operation' || itemType == 'Doctor')) {
			document.getElementById('d_markPriorAuthReqTd').style.display = 'block';
			document.getElementById('d_markPriorAuthCheckBox').style.display = 'table-cell';
		}else {
			document.getElementById('d_markPriorAuthReqTd').style.display = 'none';
			document.getElementById('d_markPriorAuthCheckBox').style.display = 'none';
		}
	}

	checkOrUncheckShowFavourite();
	clearFields();
	toggleAddToFavouriteField();
	document.getElementById('d_itemName').focus();
	parentDialog = addItemDialog;
	document.getElementById('pkg_details_button').style.display = 'none';
	return false;
}

function toggleAddToFavouriteField() {
	var itemType = getItemType();
	document.getElementById('d_addToFavourite').checked = false;
	if (itemType == 'Instructions') {
		document.getElementById('d_addToFavourite').disabled = true;
		return;
	}
}

function toggleDurationUnits(enable, prefix) {
	enable = empty(enable) ? false : enable;
	var els = document.getElementsByName(prefix+"_duration_units");
	for (var i=0; i<els.length; i++) {
		els[i].disabled = !enable;
		els[i].checked = false;
	}
}

function setGranularUnit(event, prefix) {
	var itemFormId = document.getElementById(prefix + '_item_form_id').value;
	var granularUnitForItem = filterList(itemFormList, "item_form_id", itemFormId);
	var granular_unit = '';
	if (granularUnitForItem.length > 0) {
		for (var k=0; k <granularUnitForItem.length; k++) {
			granular_unit = granularUnitForItem[k].granular_units;
			break;
		}
	}
	if (!empty(granular_unit)) {
		document.getElementById(prefix + '_granular_units').value = granular_unit;
		document.getElementById(prefix + '_qty').value = '';
		document.getElementById(prefix + '_remarks').value = '';
		if (granular_unit == 'Y') {
			calcQty(prefix);
			setAutoGeneratedInstruction(prefix);
		} else
			document.getElementById(prefix + '_qty').value = 1;
	}
}

function onItemChange(){
	clearFields();
	toggleAddToFavouriteField();
	var itemType = getItemType();
	if (itemType == 'Medicine') {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'table-cell';
		document.getElementById('d_non_hosp_medicine_div1').style.display = 'table-cell';
		document.getElementById('d_drug_code_show').style.display = 'table-row';
		//document.getElementById('d_drug_code_show').style.visibility='visible';
	} else {
		document.getElementById('d_non_hosp_medicine_div').style.display = 'none';
		document.getElementById('d_non_hosp_medicine_div1').style.display = 'none';
		document.getElementById('d_drug_code_show').style.display = 'none';
	}
	if (itemType == "All") {

	} else if (itemType == "Medicine" || itemType == 'NonHospital') {
		//document.getElementById('dGenericNameRow').style.display = 'table-row';
		document.getElementById('d_strength').disabled = false;
		document.getElementById('d_admin_strength').disabled = false;
		document.getElementById('d_frequency').disabled = false;
		document.getElementById('d_duration').disabled = false;
		document.getElementById('d_qty').disabled = false;
		document.getElementById('d_remarks').disabled = false;
		document.getElementById('d_medicine_route').disabled = false;
		document.getElementById('d_refills').disabled = false;

	} else {
		if (itemType == 'Service') {
			document.getElementById('d_qty').disabled = false;
			document.getElementById('d_remarks').disabled = false;
		} else if (itemType == "Instructions") {
			document.getElementById('d_qty').disabled = true;
			document.getElementById('d_remarks').disabled = true;
		} else {
			document.getElementById('d_remarks').disabled = false;
			document.getElementById('d_qty').disabled = true;
		}

		document.getElementById('d_strength').disabled = true;
		document.getElementById('d_admin_strength').disabled = true;
		document.getElementById('d_frequency').disabled = true;
		document.getElementById('d_duration').disabled = true;
		document.getElementById('d_medicine_route').disabled = true;
		document.getElementById('d_refills').disabled = true;

		//document.getElementById('dGenericNameRow').style.display = 'none';
		document.getElementById('genericNameAnchor_dialog').innerHTML = '';
 		document.getElementById('genericNameAnchor_dialog').style.display = 'none';
   		document.getElementById('genericNameAnchor_dialog').href = '';
	}
	var isInsurancePatient =document.getElementById('tpa_id').value != '';
	var non_hosp_medicine = document.getElementById('d_non_hosp_medicine').checked;
	if (isInsurancePatient
			&& ((itemType == 'Medicine' && !non_hosp_medicine && !useGenerics)
				|| itemType == 'Inv.'
				|| itemType == 'Service'
				|| itemType == 'Operation'
				|| itemType == 'Doctor')) {
		document.getElementById('d_priorAuthLabelTd').style.display = 'block';
		document.getElementById('d_priorAuth_label').style.display = 'table-cell';
		document.getElementById('d_category_payable_show').style.display = 'table-row';
		//document.getElementById('d_category_payable_show').style.visibility='visible';
	} else {
     	document.getElementById('d_drug_code_show').style.display='none';
		document.getElementById('d_priorAuthLabelTd').style.display = 'none';
		document.getElementById('d_priorAuth_label').style.display = 'none';
		document.getElementById('d_category_payable_show').style.display = 'none';
	}

	if (document.getElementById('d_markPriorAuthReqTd') != null) {
		if (isInsurancePatient
				&& (itemType == 'Inv.' || itemType == 'Service' || itemType == 'Operation' || itemType == 'Doctor')) {
			document.getElementById('d_markPriorAuthReqTd').style.display = 'block';
			document.getElementById('d_markPriorAuthCheckBox').style.display = 'table-cell';
		}else {
			document.getElementById('d_markPriorAuthReqTd').style.display = 'none';
			document.getElementById('d_markPriorAuthCheckBox').style.display = 'none';
		}
	}
	document.getElementById('pkg_details_button').style.display = 'none';
}

// this method gets called when user clicks on Non Hospital Medicine checkbox.
function clearFieldsWhenChanged() {
	var showFavourite = document.getElementById('d_doctor_favourite').checked;
	document.getElementById('d_itemName').value = '';
	clearItemDetails();
	toggleItemFormRow(true);
	if (showFavourite)
		initDoctorFavouriteItems();
	else
		initItemAutoComplete();
}

function initItemAutoComplete() {
	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	if (!empty(itemDocFavAutoComp)) {
		itemDocFavAutoComp.destroy();
		itemDocFavAutoComp = null;
	}

	var nonHospMedicine = document.getElementById('d_non_hosp_medicine').checked
	var itemType = getItemType();
	if (itemType == 'Instructions' || itemType == 'NonHospital' ||
		(itemType == 'Medicine' && nonHospMedicine)) return null; // for doctor instrctions no need to create the autocomplete.

	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeAction.do');
	ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId + "&center_id=" + centerId + "&p_health_authority=" + health_authority + "&tpa_id=" + tpaId + "&dept_id=" + departmentId;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "generic_code"},
					{key : "drug_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "route_of_admin"},
					{key : "consumption_uom"},
					{key : 'prior_auth_required'},
					{key : 'item_form_id'},
					{key : 'item_strength'},
					{key : 'tooth_num_required'},
					{key : 'item_strength_units'},
					{key : 'granular_units'},
					{key : 'insurance_category_id'},
					{key : 'insurance_category_name'},
					{key : 'package_type'}
				 ],
		numMatchFields: 2
	};

	itemAutoComp = new YAHOO.widget.AutoComplete("d_itemName", "itemContainer", ds);
	itemAutoComp.minQueryLength = 1;
	itemAutoComp.animVert = false;
	itemAutoComp.maxResultsDisplayed = 50;
	itemAutoComp.resultTypeList = false;
	var forceSelection = true;
	if (itemType == 'Medicine' && use_store_items != 'Y')
		forceSelection = false;
	itemAutoComp.forceSelection = forceSelection;

	itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			// show qty only for pharmacy items.
			if (use_store_items == 'Y' && prescriptions_by_generics == 'false')
				highlightedValue += "(" + record.qty + ") ";
			// show generic name along with the medicine name when prescriptions done by brand names.
			if (!useGenerics)
				highlightedValue += (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
		}
		return highlightedValue;
	}

	itemAutoComp.dataRequestEvent.subscribe(clearItemDetails);
	if (forceSelection) {
		itemAutoComp.itemSelectEvent.subscribe(selectItem);
		itemAutoComp.selectionEnforceEvent.subscribe(clearItemDetails);
	} else {
		itemAutoComp.itemSelectEvent.subscribe(selectItem);
	}


	return itemAutoComp;
}

function toggleItemFormRow(addDialog) {
	var prefix = addDialog ? 'd_' : 'ed_';
	var itemType = addDialog ? getItemType() : document.getElementById(prefix + 'itemType').value;
	var non_hosp_medicine = addDialog ? document.getElementById('d_non_hosp_medicine').checked+'' : document.getElementById('ed_non_hosp_medicine').value;

	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById(prefix + 'itemFormRow').style.display = 'table-row';
		// allow user to select the medicine form if it is a prescription by generics.
		if ((itemType == 'Medicine' && non_hosp_medicine == 'true') || itemType == 'NonHospital' || useGenerics) {
			document.getElementById(prefix + 'item_form_id').disabled = false;
			document.getElementById(prefix + 'item_strength').disabled = false;
			document.getElementById(prefix + 'item_strength_units').disabled = false;
		} else {
			document.getElementById(prefix + 'item_form_id').disabled = true;
			document.getElementById(prefix + 'item_strength').disabled = true;
			document.getElementById(prefix + 'item_strength_units').disabled = true;
		}
	} else {
		document.getElementById(prefix + 'itemFormRow').style.display = 'none';
	}
}

function selectItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_item_id').value = record.item_id;
	document.getElementById('d_package_type').value = record.package_type;
	document.getElementById('d_insurance_category_id').value = record.insurance_category_id;
	document.getElementById('d_insurance_category_name').value = record.insurance_category_name;
	if (record.item_type == 'Medicine') {
		document.getElementById('d_qty_in_stock').value = record.qty;
		if (!empty(record.generic_name)) {
			document.getElementById('genericNameAnchor_dialog').style.display = 'block';
			document.getElementById('genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "", "dialog", "'+record.generic_code+'")';
			document.getElementById('genericNameAnchor_dialog').innerHTML = record.generic_name;
			document.getElementById('d_generic_code').value = record.generic_code;
			document.getElementById('d_generic_name').value = record.generic_name;
		}
	}
	var prior_auth = record.prior_auth_required;
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}
	document.getElementById('d_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('d_priorAuth').value = prior_auth;

	var markPriorAuthReqObj = document.getElementById('d_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		if (prior_auth == 'A' && !empty(TPArequiresPreAuth) && TPArequiresPreAuth == 'Y') {
			markPriorAuthReqObj.checked = true;
		}else {
			markPriorAuthReqObj.checked = false;
		}
	}

	document.getElementById('d_drug_code').value = empty(record.drug_code) ? '' : record.drug_code;
	document.getElementById('d_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_consumption_uom_label').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_ispackage').value = record.ispkg;
	if (record.ispkg) {
		document.getElementById('pkg_details_button').style.display = 'table-row';
		var pkg_id = record.item_id;
		document.getElementById('pkgid').value = pkg_id;
	} else {
		document.getElementById('pkg_details_button').style.display = 'none';
	}
	document.getElementById('d_item_master').value = record.master;
	document.getElementById('d_item_form_id').value = record.item_form_id == 0 || record.item_form_id == null ? '' : record.item_form_id;
	document.getElementById('d_item_strength').value = record.item_strength;
	document.getElementById('d_item_strength_units').value = record.item_strength_units == 0 || record.item_strength_units == null ? '' : record.item_strength_units;
	document.getElementById('d_item_strength_units').selectedIndex=document.getElementById('d_item_strength_units').selectedIndex == -1 ? 0:
		document.getElementById('d_item_strength_units').selectedIndex;
	document.getElementById('d_granular_units').value = record.granular_units;
	if (record.granular_units != 'Y') {
		document.getElementById('d_qty').value = 1;
	}

	document.getElementById('d_tooth_num_required').value = record.tooth_num_required;
	if (record.tooth_num_required == 'Y') {
		document.getElementById('d_tooth_number').disabled = false;
		document.getElementById('dToothNumBtnDiv').style.display = 'block';
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'none';
	} else {
		document.getElementById('d_tooth_number').disabled = true;
		document.getElementById('dToothNumBtnDiv').style.display = 'none';
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';
	}
	getItemRateDetails();
}


var ajaxRequest = null;
var ajaxInProgress = false;
function getItemRateDetails() {
	var itemMaster = document.getElementById('d_item_master').value;
	var drugCode = document.getElementById('d_drug_code').value;
	var itemType = getItemType();
	var ispackage = document.getElementById('d_ispackage').value;

	if (itemType == 'Service') {
		document.getElementById('d_chargeHeadId').value = 'SERSNP';
		document.getElementById('d_chargeGroupId').value = 'SNP';
	} else if (itemType == 'Doctor') {
		document.getElementById('d_chargeHeadId').value = 'OPDOC';
		document.getElementById('d_chargeGroupId').value = 'DOC';
	} else if (itemType == 'Operation') {
		document.getElementById('d_chargeHeadId').value = 'OPE';
		document.getElementById('d_chargeGroupId').value = 'TCOPE';
	}

	if (itemType == 'Instructions'
		|| itemType == 'NonHospital'
		|| (itemType == 'Medicine' && useGenerics)) {
		document.getElementById('d_package_size').value = '';
		document.getElementById('d_price').value = '';
		document.getElementById('d_pkg_size_label').textContent = '';
		document.getElementById('d_price_label').textContent = '';

	} else {

		var orgId = document.getElementById('org_id').value;
		var itemId = document.getElementById('d_item_id').value;
		var itemName = document.getElementById('d_itemName').value;
		var bedType = document.getElementById('bed_type').value;
		var url = cpath+'/outpatient/OpPrescribeAction.do?_method=getItemRateDetails';
		url += '&item_type='+itemType;
		url += '&org_id='+orgId;
		url += '&item_id='+itemId;
		url += '&item_name='+encodeURIComponent(itemName);
		url += '&is_package='+ispackage;
		url += '&bed_type='+bedType;
		url += '&planId='+planId;
		url += '&planId1='+planId1;
		ajaxRequest = YAHOO.util.Connect.asyncRequest('GET', url,
				{ 	success: onGetCharge,
					failure: onGetChargeFailure,
					argument: [itemType, ispackage, itemMaster  , drugCode]}
		)
		ajaxInProgress = true;
	}
}

function onGetCharge(response) {
	if (response.responseText != undefined) {
		var itemType = response.argument[0];
		var rateDetails = eval('(' + response.responseText + ')');
		if (rateDetails == null) {
			document.getElementById('d_price').value = '';
			document.getElementById('d_package_size').value= '';
			document.getElementById('d_pkg_size_label').textContent = '';
			document.getElementById('d_price_label').textContent = '';
			document.getElementById('d_medicine_route').length = 1;
			ajaxInProgress = false;
			return;
		}
		var packageSize = '';
		var price = 0;
		var discount = 0;
		var discountType = 'E';
		var cat_pay = '';
		var drugCode = response.argument[3];
		if (itemType == 'Medicine') {
			document.getElementById('d_batchNo').value = rateDetails.batch_no;
			document.getElementById('d_itemBatchId').value = rateDetails.item_batch_id;

			document.getElementById('d_drug_code_label').textContent = drugCode;
			packageSize = empty(rateDetails.issue_base_unit) ? '' : rateDetails.issue_base_unit;
			price = empty(rateDetails.selling_price) ? '' : rateDetails.selling_price;
			discount = empty(rateDetails.meddisc) ? '' : rateDetails.meddisc;
			cat_pay = empty(rateDetails.category_payable) ? '' : rateDetails.category_payable=='Y'? 'Yes':'No';
			if (showRateDetails) {
				document.getElementById('d_price').value = price;
				document.getElementById('d_price_label').textContent = price;

			} else {
				document.getElementById('d_price').value = '';
				document.getElementById('d_price_label').textContent = '';
			}
			discountType = document.getElementById('pharmacy_discount_type').value;
			var discountPer = document.getElementById('pharmacy_discount_percentage').value;
			if (!empty(discount)) {
				discount = formatAmountPaise(getPaise(discount)+getPaise(discountPer));
			} else {
				if (!empty(discountPer))
					discount = discountPer;
			}
			document.getElementById('d_med_discount').value = discount;
			document.getElementById('d_med_disc_type').value = discountType;
			document.getElementById('d_itemCode').value = empty(rateDetails.item_code) ? '' : rateDetails.item_code;
			document.getElementById('d_category_payable').value = rateDetails.category_payable;
			document.getElementById('d_category_payable_label').textContent = cat_pay;
			if(cat_pay == 'No'){
				document.getElementById('d_category_payable_label').style.color = 'red';
			}else{
				document.getElementById('d_category_payable_label').style.color = '';
			}
			document.getElementById('d_insurance_category_name_label').textContent = document.getElementById('d_insurance_category_name').value;
			document.getElementById('d_package_size').value = packageSize;
			document.getElementById('d_pkg_size_label').textContent = packageSize;
			var routeIds = rateDetails.route_id.split(",");
			var routeNames = rateDetails.route_name.split(",");
			var medicine_route_el = document.getElementById('d_medicine_route');
			medicine_route_el.length = 1; // clear the previously populated list
			var len = 1;
			for (var i=0; i<routeIds.length; i++) {
				if (routeIds[i].trim() != '') {
					medicine_route_el.length = len+1;
					medicine_route_el.options[len].value = routeIds[i].trim();
					medicine_route_el.options[len].text = routeNames[i];
					len++;
				}
			}
			medicine_route_el.selectedIndex = medicine_route_el.length == 2 ? 1 : 0;
		} else {
			if (itemType == 'Inv.') {
				if (rateDetails.type == 'P') {
					document.getElementById('d_chargeHeadId').value = 'PKGPKG';
					document.getElementById('d_chargeGroupId').value = 'PKG';
				} else {
					var category = empty(rateDetails.category) ? '' : rateDetails.category;
					document.getElementById('d_test_category').value = category;

					document.getElementById('d_chargeHeadId').value = category == 'DEP_LAB' ? 'LTDIA' : 'RTDIA';
					document.getElementById('d_chargeGroupId').value = 'DIA';
				}
			}
			var charge = empty(rateDetails.charge) ? 0 : rateDetails.charge;
			var discount = empty(rateDetails.discount) ? 0 : rateDetails.discount;

			price = getPaise(charge) - getPaise(discount);
			price = formatAmountPaise(price);
			if (showRateDetails) {
				document.getElementById('d_price').value = price;
				document.getElementById('d_price_label').textContent = price;

				document.getElementById('d_disc').value = discount;
			} else {
				document.getElementById('d_price').value = '';
				document.getElementById('d_price_label').textContent = '';
			}
			document.getElementById('d_package_size').value= '';
			document.getElementById('d_pkg_size_label').textContent = '';
			cat_pay = empty(rateDetails.category_payable) ? '' : rateDetails.category_payable=='Y'? 'Yes':'No';
			document.getElementById('d_category_payable').value = rateDetails.category_payable;
			document.getElementById('d_category_payable_label').textContent = cat_pay;
			if(cat_pay == 'No'){
				document.getElementById('d_category_payable_label').style.color = 'red';
			}else{
				document.getElementById('d_category_payable_label').style.color = '';
			}
			document.getElementById('d_insurance_category_name_label').textContent = document.getElementById('d_insurance_category_name').value;
		}
		ajaxInProgress = false;
	}
}

function displayAmounts() {
	return (!empty(showChargesAllRatePlan) && showChargesAllRatePlan == 'A');
}

function onGetChargeFailure() {
	ajaxInProgress = false;
}

function clearItemDetails(oSelf) {
	var itemType = getItemType();
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_item_id').value = '';

	var allRoutes = !(itemType == 'Medicine' && !document.getElementById('d_non_hosp_medicine').checked && !useGenerics);
	document.getElementById('d_medicine_route').length = 1;
	if (allRoutes) {
		var len = 2;
		for (var i=0; i<routesListJson.length; i++) {
			document.getElementById('d_medicine_route').length	= len;
			document.getElementById('d_medicine_route').options[len-1].value = routesListJson[i].route_id;
			document.getElementById('d_medicine_route').options[len-1].text = routesListJson[i].route_name;
			len++;
		}
	}
	document.getElementById('d_medicine_route').selectedIndex = (document.getElementById('d_medicine_route').length == 2 ? 1 : 0); // if only one route found, then default it.

	document.getElementById('d_admin_strength').value = '';
	document.getElementById('d_frequency').value = '';
	document.getElementById('d_strength').value = '';
	document.getElementById('d_refills').value = '';
	document.getElementById('d_duration').value = '';
	var enable = itemType == 'Medicine' || itemType == 'NonHospital';
	toggleDurationUnits(enable, 'd');
	if (enable) {
		// disable if it is prescription by brand names for medicines.
		if (itemType == 'Medicine' && !useGenerics && !document.getElementById('d_non_hosp_medicine').checked) {
			document.getElementById('d_consumption_uom').disabled = true;
		} else {
			document.getElementById('d_consumption_uom').disabled = false;
		}
		document.getElementsByName('d_duration_units')[0].checked = true;
		document.getElementById('d_remarks').value = '';
	} else {
		document.getElementById('d_consumption_uom').disabled = true;
		document.getElementById('d_remarks').value = '';
	}
	if (itemType == 'Service')
		document.getElementById('d_qty').value = 1;
	else
		document.getElementById('d_qty').value = '';
	document.getElementById('d_special_instruction').value = '';
	document.getElementById('d_consumption_uom').value = '';
	document.getElementById('d_consumption_uom_label').textContent = '';
	document.getElementById('genericNameAnchor_dialog').style.display = 'none';
	document.getElementById('genericNameAnchor_dialog').href = '';
	document.getElementById('genericNameAnchor_dialog').innerHTML = '';
	document.getElementById('d_generic_code').value = '';
	document.getElementById('d_drug_code').value = '';
	document.getElementById('d_drug_code_label').textContent = '';
	document.getElementById('d_generic_name').value = '';
	document.getElementById('d_ispackage').value = '';
	document.getElementById('d_item_master').value = '';
	document.getElementById('d_package_size').value = '';
	document.getElementById('d_price').value = '';
	document.getElementById('d_pkg_size_label').textContent = '';
	document.getElementById('d_price_label').textContent = '';
	document.getElementById('d_qty_in_stock').value = '';
	document.getElementById('d_priorAuth_label').textContent = '';
	document.getElementById('d_priorAuth').value = '';
	document.getElementById('d_item_form_id').value = '';
	document.getElementById('d_granular_units').value = '';
	document.getElementById('d_item_strength').value = '';
	document.getElementById('d_item_strength_units').value = '';
	document.getElementById('d_tooth_num_required').value = 'N';
	document.getElementById('dToothNumberDiv').textContent = '';
	document.getElementById('d_tooth_number').value = '';
	document.getElementById('dToothNumBtnDiv').style.display = 'none';
	document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';
	document.getElementById('d_category_payable').value = '';
	document.getElementById('d_category_payable_label').textContent = '';
	document.getElementById('d_insurance_category_name_label').textContent = '';
}

var colIndex  = 0;
var PRES_DATE = colIndex++, ITEM_TYPE = colIndex++, ITEM_NAME = colIndex++;
if (mod_ceed_enabled)
	var CROSS_CODE_STATUS = colIndex++;

var	TOOTH_NUMBER = colIndex++, FORM =  colIndex++, STRENGTH = colIndex++,
	ADMIN_STRENGTH = colIndex++, DETAILS = colIndex++, ROUTE = colIndex++, REMARKS = colIndex++, SPECIAL_INSTRUCTION = colIndex++;
var QTY = colIndex++, PKG_PRICE = colIndex++, UNIT_PRICE = colIndex++, PAT_CO_PAY = colIndex++, INS_CAT_NAME = colIndex++;

if (requireERxAuth)
 ERXSTATUS_COL = colIndex++, DENIAL_COL = colIndex++, DENIAL_TYPE = colIndex++, DRUG_CODE = colIndex++, DENIAL_DESC = colIndex++, DENIAL_EXAMP = colIndex++;
TRASH_COL = colIndex++, EDIT_COL = colIndex++;

var itemsAdded = 0;
function addToTable() {

	var itemType = getItemType();
	var non_hosp_medicine = false;
	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (mod_eclaim_preauth == 'Y' && itemType == 'Inv.' && TPAEAuthMode == 'O') {
		var itemFromPackage = document.getElementById('d_ispackage').value;
		if (isInsurancePatient && itemFromPackage == 'true') {
			alert(getString("js.outpatient.consultation.mgmt.patienttparequires") +
				getString("js.outpatient.consultation.mgmt.packageitemsnotallowed"));
			return false;
		}
	}
	if (itemType == 'Medicine')
		non_hosp_medicine = document.getElementById('d_non_hosp_medicine').checked;

	if (itemType == 'Medicine' && !non_hosp_medicine && !validateAddOrEditERxData()) {
		return false;
	}

	if (ajaxInProgress) {
		setTimeout("addToTable()", 100);
		return false
	}
	var itemName = document.getElementById('d_itemName').value;
	if (itemName == '') {
   		showMessage('js.outpatient.consultation.mgmt.prescribetheitem');
   		document.getElementById('d_itemName').focus();
   		return false;
   	}
   	var strength = document.getElementById('d_strength').value;
   	var granular_unit = document.getElementById('d_granular_units').value ;
   	if (!empty(granular_unit) && granular_unit == 'Y') {
	   	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		showMessage("js.outpatient.consultation.mgmt.dosageshouldbegreater.zeroandnumber.two.in.brackets");
	   		document.getElementById('d_strength').focus();
	   		return false;
	   	}
	 }
   	var duration = document.getElementById('d_duration').value;
   	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		showMessage("js.outpatient.consultation.mgmt.durationshouldbegreater.zeroandnumber");
		document.getElementById('d_duration').focus();
		return false;
	}
	if (itemType == 'Medicine' && !non_hosp_medicine) {
		if (mod_eclaim_erx == 'Y') {
			if (!validateEprxFields("d", false)) return false;
		} else if (mod_eclaim_pbm == 'Y') {
			if (!validatPBMFields("d", false)) return false;
		}
	}

	var qty = document.getElementById('d_qty').value;

	if (itemType == 'Service' || (itemType == 'Medicine' && !non_hosp_medicine) ) {
		if (qty == '') {
			showMessage("js.outpatient.consultation.mgmt.pleaseentertheqty");
			document.getElementById('d_qty').focus();
			return false;
		}
	}
	if (qty != '' && (!regExp.test(qty) || qty == 0)) {
		showMessage("js.outpatient.consultation.mgmt.qtyshouldbegreater.number");
		document.getElementById('d_qty').focus();
		return false;
	}
	var item_strength = document.getElementById('d_item_strength').value;
   	var item_strength_units = document.getElementById('d_item_strength_units').value;
   	item_strength_units = item_strength == '' ? '' : item_strength_units;
	if(!(document.getElementById('d_item_strength_units').disabled))
   	var strength_unit_name = document.getElementById('d_item_strength_units').options[document.getElementById('d_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;

   	var tooth_number = document.getElementById('d_tooth_number').value;
   	var tooth_num_required = document.getElementById('d_tooth_num_required').value;
   	if (tooth_num_required == 'Y' && tooth_number == '') {
   		showMessage('js.outpatient.consultation.mgmt.toothnumberrequiredfortheservice');
   		return false;
   	}
	var id = getNumCharges('itemsTable');
   	var table = document.getElementById("itemsTable");
	var templateRow = table.rows[getTemplateRow('itemsTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	var cell = null;
   	var itemId = document.getElementById('d_item_id').value;
   	var packageType = document.getElementById('d_package_type').value;
   	var adminStrength = document.getElementById('d_admin_strength').value;
   	var frequency = document.getElementById('d_frequency').value;
   	var remarks = document.getElementById('d_remarks').value;
   	var spl_instruction = document.getElementById('d_special_instruction').value;
   	var master = document.getElementById('d_item_master').value;
   	var genericCode = document.getElementById('d_generic_code').value;
   	var genericName = document.getElementById('d_generic_name').value;
   	var drugCode = document.getElementById('d_drug_code').value;
   	var ispackage = document.getElementById('d_ispackage').value;
   	var pkg_size = getAmount(document.getElementById('d_package_size').value);
   	var consumption_uom = document.getElementById('d_consumption_uom').value;
   	var price = getPaise(document.getElementById('d_price').value);
   	var routeId = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].value;
   	var routeName = document.getElementById('d_medicine_route').options[document.getElementById('d_medicine_route').selectedIndex].text;
   	routeName = routeId == '' ? '' : routeName;
   	var priorAuth = document.getElementById('d_priorAuth').value;
   	var markPriorAuthReqObj = document.getElementById('d_markPriorAuthReq');
   	var item_form_id = document.getElementById('d_item_form_id').value;
   	var refills = document.getElementById('d_refills').value;
   	var item_form_name = document.getElementById('d_item_form_id').options[document.getElementById('d_item_form_id').selectedIndex].text;
	var item_pkg_price = 0;
	var item_unit_price = 0;

	var insCatName = document.getElementById('d_insurance_category_name').value;
   	setNodeText(row.cells[ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine ? '[Non Hosp]' : ''));
   	setNodeText(row.cells[ITEM_NAME], itemName + (tooth_number == '' ? '' : ('[' + tooth_number + ']')), 20);
   	setNodeText(row.cells[INS_CAT_NAME], insCatName, 20);

   	if(itemType == 'Inv.' && packageType == 'O') {
   		// Check if order set contains only Lab and Rad items.
   		var packageDetails;
		var	url = cpath + '/patients/orders/getpackagecontents.json?packageId='+itemId+'&mr_no='+'&multi_visit_package='+false;
		var reqObject = newXMLHttpRequest();
		reqObject.open("GET", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
				var packageComponentDetails = eval('('+reqObject.responseText+')');
				packageDetails = packageDetails['packComponentDetails'];
			}
		}
		for(var index in packageComponentDetails) {
			if(packageComponentDetails[index].item_type !== 'Labaratory' && packageComponentDetails[index].item_type !== 'Radiology') {
				showMessage("js.common.order.cannot.add.order.set.comprising.other.items");
				return false;
			}
		}
   	}

   	if (itemType == 'Medicine' || itemType == 'NonHospital') {
   		var duration_radio_els = document.getElementsByName('d_duration_units');
		var duration_units;
		for (var k=0; k<duration_radio_els.length; k++) {
			if (duration_radio_els[k].checked) {
				duration_units = duration_radio_els[k].value;
				break;
			}
		}
		if (itemType == 'Medicine' && !non_hosp_medicine && mod_eclaim_erx == 'Y') {
			// by default mark the item for sending to erx.
			document.getElementsByName('chk_send_item_for_erx')[id].checked = true;
			document.getElementsByName('chk_send_item_for_erx')[id].disabled = false;
			setHiddenValue(id, "send_item_for_erx", 'Y');
		}

		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
   		var details = "";
   		if (frequency != '' || duration != '')
   			details = frequency + " / " + duration + " " + duration_units;
   		setNodeText(row.cells[DETAILS], details, 20);
		setNodeText(row.cells[QTY], qty);
		if (item_form_id != '') {
			setNodeText(row.cells[FORM], item_form_name, 15);
		}
		setNodeText(row.cells[STRENGTH], item_strength + ' ' + strength_unit_name, 15);
		setHiddenValue(id, "generic_code", genericCode);
		setHiddenValue(id, "drug_code", drugCode);
		setHiddenValue(id, "admin_strength", adminStrength);
		setHiddenValue(id, "generic_name", genericName);
		setHiddenValue(id, "frequency", frequency);
		setHiddenValue(id, "strength", strength);
		setHiddenValue(id, "duration", duration);
		setHiddenValue(id, "duration_units", duration_units);
		setHiddenValue(id, "medicine_quantity", qty);
		setHiddenValue(id, "qty_in_stock", document.getElementById('d_qty_in_stock').value);
		setHiddenValue(id, "item_form_id", item_form_id);
		setHiddenValue(id, "granular_units", granular_unit);
		setHiddenValue(id, "item_strength", item_strength);
		setHiddenValue(id, "item_strength_units", item_strength_units);
		setHiddenValue(id, "refills", refills);
		if (pkg_size != '' && price != '' && qty != '') {
			item_unit_price = (price/pkg_size) * qty;
			item_pkg_price = Math.ceil(qty/pkg_size) * price;
		}

		if (itemType == 'Medicine') {
			// these parameters are used to calculate the patient copay.

			var d_med_discount = document.getElementById('d_med_discount').value;
			var d_med_disc_type = document.getElementById('d_med_disc_type').value;
			var sellingPrice = document.getElementById('d_price').value;
			var itemCode = document.getElementById('d_itemCode').value;
			var batchNo = document.getElementById('d_batchNo').value;
			var itemBatchId = document.getElementById('d_itemBatchId').value;

			setHiddenValue(id, 'temp_charge_id', "_"+id);
			setHiddenValue(id, 'medDiscRS', d_med_discount);
			setHiddenValue(id, 'medDiscType', d_med_disc_type);
			setHiddenValue(id, 'origRate', sellingPrice);
			setHiddenValue(id, 'itemCode', itemCode);
			setHiddenValue(id, 'batchNo', batchNo);
			setHiddenValue(id, 'itemBatchId', itemBatchId);
			setHiddenValue(id, "qty", qty);
			var discAmount = ((sellingPrice/pkg_size) * qty * d_med_discount/100);
            setHiddenValue(id, "amt", (sellingPrice/pkg_size) * qty - discAmount);
			setHiddenValue(id, "insuranceCategoryId", document.getElementById('d_insurance_category_id').value);
			setHiddenValue(id, "category_payable", document.getElementById('d_category_payable').value);
			setHiddenValue(id, "insuranceCategoryName", insCatName);
		}
	} else {
		// these parameters are used to calculate the patient copay.
		var disc = document.getElementById('d_disc').value;
	   	var chargeHeadId = document.getElementById('d_chargeHeadId').value;
	   	var groupId = document.getElementById('d_chargeGroupId').value;

	   	setHiddenValue(id, "chargeId", "_"+id);
		setHiddenValue(id, "chargeHeadId", chargeHeadId);
		setHiddenValue(id, "chargeGroupId", groupId);
		setHiddenValue(id, "disc", disc);
		setHiddenValue(id, "insuranceCategoryId", document.getElementById('d_insurance_category_id').value);
		setHiddenValue(id, "category_payable", document.getElementById('d_category_payable').value);
		setHiddenValue(id, "insuranceCategoryName", insCatName);
		var amt = price;
		if (itemType == 'Service')
			amt = price * qty;
		setHiddenValue(id, "amt", formatAmountPaise(amt));

		document.getElementsByName('chk_send_item_for_erx')[id].checked = false; // erx will not be applicable for other items
		document.getElementsByName('chk_send_item_for_erx')[id].disabled = true;
		item_pkg_price = price;
		item_unit_price = price;
	}

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

	if (itemType == 'Service') {
		setNodeText(row.cells[QTY], qty);
		item_unit_price = item_unit_price * qty;
	}

	setNodeText(row.cells[PKG_PRICE], item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setNodeText(row.cells[UNIT_PRICE], item_unit_price == 0? '' : formatAmountPaise(item_unit_price));
	setNodeText(row.cells[ROUTE], routeName);
	setNodeText(row.cells[REMARKS], remarks, 20);
	setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 20);
	setNodeText(row.cells[TOOTH_NUMBER], tooth_number);
	if (requireERxAuth)
		setNodeText(row.cells[DRUG_CODE], drugCode);

	setHiddenValue(id, "consumption_uom", consumption_uom);
	setHiddenValue(id, "item_prescribed_id", "_");
	setHiddenValue(id, "itemType", itemType);
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "drug_code", drugCode);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "special_instr" , spl_instruction);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "ispackage", ispackage);
	setHiddenValue(id, "pkg_size", pkg_size == '' ? '' : pkg_size);
	setHiddenValue(id, "pkg_price", price == '' ? '' : formatAmountPaise(price));
	setHiddenValue(id, "item_pkg_price", item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setHiddenValue(id, "item_unit_price", item_unit_price == 0 ? '' : formatAmountPaise(item_unit_price));
	setHiddenValue(id, "route_id", routeId);
	setHiddenValue(id, "route_name", routeName);
	setHiddenValue(id, "priorAuth", priorAuth);
	setHiddenValue(id, "issued", "P");
	setHiddenValue(id, "addToFavourite", document.getElementById('d_addToFavourite').checked);
	setHiddenValue(id, "tooth_num_required", tooth_num_required);
	setHiddenValue(id, "service_qty", qty);
	setHiddenValue(id, "non_hosp_medicine", non_hosp_medicine);
	if (itemType == 'Operation') {
   		setHiddenValue(id, "op_id", itemId);
   	}
	if (tooth_num_required == 'Y') {
		if (tooth_numbering_system == 'U')
			setHiddenValue(id, 'tooth_unv_number', tooth_number);
		else
			setHiddenValue(id, 'tooth_fdi_number', tooth_number);
	}

	if (markPriorAuthReqObj != null) {
		if (markPriorAuthReqObj.checked) {
			setHiddenValue(id, "requirePriorAuth", "Y");
		}else {
			setHiddenValue(id, "requirePriorAuth", "N");
		}
	}

	if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
	} else {
		estimateTotal();
	}
	itemsAdded++;
	clearFields();
	toggleAddToFavouriteField();
	setRowStyle(id);
	addItemDialog.align("tr", "tl");
	document.getElementById('d_itemName').focus();
	document.getElementById('pkg_details_button').style.display = 'none';

	var visitId = document.getElementById('patient_id').value;
	if (itemType == 'Medicine') {
		if (!non_hosp_medicine && use_store_items == 'Y' && prescriptions_by_generics != 'true') {
			setHiddenValue(id, "medicineId", itemId);
			getSaleItemClaim(visitId, document.prescribeForm);
		}
	} else {
		getItemClaims(visitId, document.prescribeForm);
	}

	return id;
}

function getSaleItemClaim(visitId, form) {
	// Changed the method to use the new ajax form post method.
	// Once tested, can be used for hospital bill as well.
	var url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getMedicineSalesChargeClaims&visitId='+visitId;
	var formToken = form._insta_transaction_token.value;
	form._insta_transaction_token.value="";

	YAHOO.util.Connect.setForm(form);

	var ajaxRequestForBillChargeClaims = YAHOO.util.Connect.asyncRequest('POST', url,
		{
			success: OnGetChargeClaims,
			failure: OnGetChargeClaimsFailure,
			argument: [form, formToken, true]
		}
	);
}

function getItemClaims(visitID, form){

	var formToken = form._insta_transaction_token.value;
	form._insta_transaction_token.value="";

	YAHOO.util.Connect.setForm(form);

	var url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getBillChargeClaims&visitID='+visitID;
	var ajaxRequestForBillChargeClaims = YAHOO.util.Connect.asyncRequest('POST', url,
			{
				success: OnGetChargeClaims,
				failure: OnGetChargeClaimsFailure,
				argument: [form, formToken, false]
			}
		)
}

function OnGetChargeClaims(response){

	if (response.responseText != undefined) {
		var planMap = eval('(' + response.responseText + ')');
		for(var j=0; j<planList.length; j++){
			var planId = planList[j].plan_id;
			var chgClaimMap = planMap[planId];
			setSponsorAmounts(chgClaimMap, j+1);
		}
	}

	var args = response.argument;
	var medicine = false;
	if (null != args && args.length >1) {
		args[0]._insta_transaction_token.value = args[1];
		medicine = args[2];
	}
	setPatientAmounts(multiPlanExists, medicine);
}

function setSponsorAmounts(chgClaimMap, priority){

	var table = document.getElementById('itemsTable');
	var numRows = table.rows.length;

	for (var id=1; id < numRows-1 ;id++) {
		var row = table.rows[id];
		var chargeId = null;
		chargeId = "_"+(id-1);
		if(chgClaimMap[chargeId] != undefined){
			var insClaimAmt = formatAmountPaise(getPaise(chgClaimMap[chargeId].insurance_claim_amt));
			if(priority == 1){
				getElementByName(row, 'priClaimAmt').value = insClaimAmt;
			}else{
				getElementByName(row, 'secClaimAmt').value = insClaimAmt;
			}
		}
	}
}


function setPatientAmounts(multiPlanExists, medicine){
	var table = document.getElementById('itemsTable');
	var numRows = table.rows.length;

	for (var id=1; id < numRows-1 ;id++) {
		var row = table.rows[id];
		if (getElementByName(row, medicine ? 'temp_charge_id' : 'chargeId').value == ('_'+(id-1))){
			var insClaimAmt = getElementByName(row, 'priClaimAmt').value;
			var priClaimAmtPaise = getPaise(insClaimAmt);
			var insClaimAmtPaise = priClaimAmtPaise;

			if(multiPlanExists){
				var secClaimAmtPaise = getPaise(getElementByName(row, 'secClaimAmt').value);
				insClaimAmtPaise = insClaimAmtPaise + secClaimAmtPaise;
			}
			var amtPaise = getPaise(getElementByName(row, 'amt').value);
			var patAmt = amtPaise - insClaimAmtPaise;

			getElementByName(row, 'orderPatientAmt').value = formatAmountPaise(patAmt);
			setNodeText(row.cells[PAT_CO_PAY], formatAmountPaise(patAmt));
		}
	}
	estimatePatCoPayTotal();
}

function OnGetChargeClaimsFailure(){

}



function estimateTotal() {
	var unitEls = document.getElementsByName('item_unit_price');
	var pkgEls = document.getElementsByName('item_pkg_price');
	var qtyEls = document.getElementsByName('service_qty');
	var itemType = document.getElementsByName('itemType');
	var pkgTotalAmount = 0;
	var unitTotalAmount = 0;
	for (var i=0; i<pkgEls.length; i++) {
		var itemPkgAmount = getPaise(pkgEls[i].value);

		pkgTotalAmount += itemPkgAmount;
		var itemUnitAmount = getPaise(unitEls[i].value);

		unitTotalAmount += itemUnitAmount;
	}
	document.getElementById('estimatedUnitTotal').textContent = formatAmountPaise(unitTotalAmount);
	document.getElementById('estimatedPkgTotal').textContent = formatAmountPaise(pkgTotalAmount);
}

function estimatePatCoPayTotal() {
	var copayAmts = document.getElementsByName('orderPatientAmt');
	var total = 0;
	for (var i=0; i<copayAmts.length-1; i++) {
		if (!empty(copayAmts[i].value))
			total = total + getPaise(copayAmts[i].value);
	}
	document.getElementById('estimatedCopayTotal').textContent = formatAmountPaise(total);
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.prescribeForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function clearFields() {
	var showFavourite = document.getElementById('d_doctor_favourite').checked;
	document.getElementById('d_itemName').value = '';
	document.getElementById('d_toothLabelTd').style.display = 'none';
	document.getElementById('d_toothValueTd').style.display = 'none';

	var itemType = getItemType();
	if (itemType == 'Medicine') {
		document.getElementById('d_non_hosp_medicine').checked = false;
	} else if (itemType == 'Service') {
		document.getElementById('d_toothLabelTd').style.display = 'table-cell';
		document.getElementById('d_toothValueTd').style.display = 'table-cell';
	}
	if (showFavourite)
		initDoctorFavouriteItems();
	else
		initItemAutoComplete();
   	clearItemDetails();
   	toggleItemFormRow(true);
}

function setRowStyle(i) {
	var row = getChargeRow(i, 'itemsTable');
	var prescribedId = getIndexedValue("item_prescribed_id", i);
	var qty_in_stock = getIndexedValue("qty_in_stock", i);

 	var flagImgs = row.cells[ITEM_TYPE].getElementsByTagName("img");
	var trashImgs = row.cells[TRASH_COL].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("delItem", i) == 'true';
	var edited = getIndexedValue("edited", i) == 'true';
	var priorAuth = getIndexedValue("priorAuth", i);
	var itemType = getIndexedValue("itemType", i);

	/*
	 * Pre-saved state is shown using background colours. The pre-saved states can be:
	 *  - Normal: no background
	 *  - Added: Greenish background
	 *  - Modified: Yellowish background
	 *    (includes cancelled, which is a change in the status attribute)
	 *
	 * Attributes are shown using flags. The only attribute indicated is the cancelled
	 * attribute, using a red flag.
	 *
	 * Possible actions using the trash icon are:
	 *  - Cancel/Delete an item: Normal trash icon.
	 *    (newly added items are deleted, saved items are cancelled)
	 *  - Un-cancel an item: Trash icon with a cross
	 *  - The item cannot be cancelled: Grey trash icon.
	 */

	var cls;
	if (added) {
		if (itemType == 'Medicine' && qty_in_stock == 0) cls = 'zero_qty'
		else cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}
	/**
	* cancelled flag takes priority when a prescriptions is of type prior auth required and it is cancelld.
	*/
	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else if (priorAuth == 'A') {
		flagSrc = cpath + '/images/blue_flag.gif';
	} else if (priorAuth == 'S') {
		flagSrc = cpath + "/images/green_flag.gif";
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function reCalcIndexes() {
	var els = document.getElementsByName('item_prescribed_id');

	for (var i=0; i<els.length-1; i++) {
		if (els[i].value != '_') continue

		var row = getThisRow(els[i]);
		var id = getRowChargeIndex(row);

		var itemType = getIndexedValue("itemType", id);
		if (itemType == 'Medicine')
			setHiddenValue(id, "temp_charge_id", "_"+id);
		else
			setHiddenValue(id, "chargeId", "_"+id);
	}

}

function cancelItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var itemType = getIndexedValue("itemType", id);
	var nonHospMedicine = getIndexedValue("non_hosp_medicine", id);
	var send_item_for_erx = getIndexedValue("send_item_for_erx", id);
   	if (itemType == 'Medicine' && nonHospMedicine == 'false' && send_item_for_erx == 'Y' && !validateAddOrEditERxData()) {
		return false;
	}

	var oldDeleted =  getIndexedValue("delItem", id);

	var isNew = getIndexedValue("item_prescribed_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted

		row.parentNode.removeChild(row);
		itemsAdded--;

		reCalcIndexes();


	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("delItem", id, newDeleted);
		setIndexedValue("edited", id, "true");
		setRowStyle(id);
	}
	var itemType = getIndexedValue("itemType", id);
	if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
	} else {
		estimateTotal();
	}

	return false;
}

function initEditItemDialog() {
	var dialogDiv = document.getElementById("editItemDialog");
	dialogDiv.style.display = 'block';
	editItemDialog = new YAHOO.widget.Dialog("editItemDialog",{
			width:"650px",
			text: "Edit Item",
			context :["itemsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditItemCancel,
	                                                scope:editItemDialog,
	                                                correctScope:true } );
	editItemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	editItemDialog.cancelEvent.subscribe(handleEditItemCancel);
	YAHOO.util.Event.addListener('editOk', 'click', editTableRow, editItemDialog, true);
	YAHOO.util.Event.addListener('editCancel', 'click', handleEditItemCancel, editItemDialog, true);
	YAHOO.util.Event.addListener('editPrevious', 'click', openPrevious, editItemDialog, true);
	YAHOO.util.Event.addListener('editNext', 'click', openNext, editItemDialog, true);
	editItemDialog.render();
}

function handleEditItemCancel() {
	if (childDialog == null) {
		parentDialog = null;
		var id = document.prescribeForm.editRowId.value;
		var row = getChargeRow(id, "itemsTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		fieldEdited = false;
		this.hide();
	}
}

function showEditItemDialog(obj) {

	parentDialog = editItemDialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editItemDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);

	document.prescribeForm.editRowId.value = id;

	var itemType = getIndexedValue("itemType", id);
	var nonHospMedicine = getIndexedValue("non_hosp_medicine", id);
	var send_item_for_erx = getIndexedValue("send_item_for_erx", id);

   	if (itemType == 'Medicine' && nonHospMedicine == 'false' && send_item_for_erx == 'Y' && !validateAddOrEditERxData()) {
		return false;
	}

	editItemDialog.show();
	YAHOO.util.Dom.addClass(row, 'editing');

	document.getElementById('ed_itemTypeLabel').textContent = itemType + (itemType == 'Medicine' && nonHospMedicine == 'true' ? '[Non Hosp]' : '');
	document.getElementById('ed_itemNameLabel').textContent = getIndexedValue("item_name", id);
	document.getElementById('ed_itemName').value = getIndexedValue("item_name", id);
	document.getElementById('ed_item_id').value = getIndexedValue("item_id", id);
	document.getElementById('ed_drug_code_label').textContent = getIndexedValue("drug_code", id);
	document.getElementById('ed_non_hosp_medicine').value = nonHospMedicine;
	document.getElementById('ed_itemType').value = itemType;
	var master = getIndexedValue("item_master", id);
	toggleItemFormRow(false);
	document.getElementById('ed_toothLabelTd').style.display = 'none';
	document.getElementById('ed_toothValueTd').style.display = 'none';

	document.getElementById('ed_consumption_uom_label').textContent = getIndexedValue("consumption_uom", id);
	document.getElementById('ed_consumption_uom').value = getIndexedValue("consumption_uom", id);

	if (document.getElementById('ed_markPriorAuthReq'))
		document.getElementById('ed_markPriorAuthReq').checked = false;

	if (itemType == 'Medicine' || itemType == 'NonHospital') {
		document.getElementById('ed_consumption_uom').disabled = itemType == 'Medicine' && nonHospMedicine == 'false' && !useGenerics;
		document.getElementById('ed_admin_strength').disabled = false;
		document.getElementById('ed_frequency').disabled = false;
		document.getElementById('ed_strength').disabled = false;
		document.getElementById('ed_duration').disabled = false;
		document.getElementById('ed_qty').disabled = false;
		document.getElementById('ed_remarks').disabled = false;
		document.getElementById('ed_refills').disabled = false;

		initEditDosageAutoComplete();
		document.getElementById('ed_medicine_route').textContent = truncateText(getIndexedValue("route_name", id),15);
		document.getElementById('ed_medicine_route').title = getIndexedValue("route_name", id);
		document.getElementById('ed_strength').value = getIndexedValue("strength", id);
		document.getElementById('ed_frequency').value = getIndexedValue("frequency", id);
		document.getElementById('ed_admin_strength').value = getIndexedValue("admin_strength", id);
		document.getElementById('ed_duration').value = getIndexedValue("duration", id);
		document.getElementById('ed_refills').value = getIndexedValue("refills", id);

		// enable the duration units only if item is not isssued.
		toggleDurationUnits(issued != 'O', 'ed');
		var duration_units = getIndexedValue("duration_units", id);
		var els = document.getElementsByName("ed_duration_units");
		for (var k=0; k<els.length; k++) {
			if (els[k].value == duration_units) {
				els[k].checked = true;
				break;
			}
		}
		document.getElementById('ed_qty').value = getIndexedValue("medicine_quantity", id);
		document.getElementById('genericNameAnchor_editdialog').innerHTML = getIndexedValue("generic_name", id);

		if (itemType == 'Medicine') {
			document.getElementById('genericNameAnchor_editdialog').href =
				'javascript:showGenericInfo("", "", "editdialog", "' + getIndexedValue("generic_code", id) + '")';
			document.getElementById('genericNameAnchor_editdialog').style.display = 'block';
		}
		document.getElementById('ed_item_form_id').value = getIndexedValue("item_form_id", id);
		document.getElementById('ed_granular_units').value = getIndexedValue("granular_units", id);
		document.getElementById('ed_item_strength').value = getIndexedValue('item_strength', id);
		document.getElementById('ed_item_strength_units').value = getIndexedValue('item_strength_units', id);
	} else {
		document.getElementById('ed_consumption_uom').disabled = true;
		toggleDurationUnits(false, 'ed');
		if (itemType == 'Service') {
			document.getElementById('ed_qty').disabled = false;
			document.getElementById('ed_remarks').disabled = false;
			document.getElementById('ed_qty').value = getIndexedValue('service_qty', id);
			document.getElementById('ed_toothLabelTd').style.display = 'table-cell';
			document.getElementById('ed_toothValueTd').style.display = 'table-cell';

		} else if (itemType == "Instructions") {
			document.getElementById('ed_qty').disabled = true;
			document.getElementById('ed_remarks').disabled = true;
			document.getElementById('ed_qty').value = '';
		} else {
			document.getElementById('ed_remarks').disabled = false;
			document.getElementById('ed_qty').disabled = true;
			document.getElementById('ed_qty').value = '';
		}
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_frequency').disabled = true;
		document.getElementById('ed_strength').disabled = true;
		document.getElementById('ed_duration').disabled = true;
		document.getElementById('ed_refills').disabled = true;

		document.getElementById('ed_admin_strength').value = '';
		document.getElementById('ed_strength').value = '';
		document.getElementById('ed_frequency').value = '';
		document.getElementById('ed_duration').value = '';
		document.getElementById('ed_refills').value = '';

		//document.getElementById('edGenericNameRow').style.display = 'none';
		document.getElementById('genericNameAnchor_editdialog').innerHTML = '';
		document.getElementById('genericNameAnchor_editdialog').style.display = 'none';
		document.getElementById('genericNameAnchor_editdialog').href='';
	}
	document.getElementById('ed_tooth_num_required').value = getIndexedValue('tooth_num_required', id);
	if (document.getElementById('ed_tooth_num_required').value == 'Y') {
		document.getElementById('edToothNumBtnDiv').style.display = 'block';
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'none';
	} else {
		document.getElementById('edToothNumBtnDiv').style.display = 'none';
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'block';
	}
	var tooth_number = tooth_numbering_system == 'U' ? getIndexedValue('tooth_unv_number', id) : getIndexedValue('tooth_fdi_number', id);
	document.getElementById('ed_tooth_number').value = tooth_number;

	var tooth_numbers = tooth_number.split(",");
	var tooth_numbers_text = '';
	var checked_toothNos = 0;
	for (var i=0; i<tooth_numbers.length; i++) {
		if (tooth_numbers_text != '') {
			tooth_numbers_text += ',';
		}
		if (checked_toothNos%10 == 0)
			tooth_numbers_text += '\n';

		checked_toothNos++;
		tooth_numbers_text += tooth_numbers[i];
	}

	document.getElementById('edToothNumberDiv').textContent = tooth_numbers_text;

	document.getElementById('ed_pkg_size_label').textContent = getIndexedValue("pkg_size", id);
   	document.getElementById('ed_price_label').textContent = getIndexedValue("pkg_price", id);
   	document.getElementById('ed_category_payable').value = getIndexedValue('category_payable', id);
   	document.getElementById('ed_category_payable_label').textContent = empty(getIndexedValue("category_payable", id)) ? '' : getIndexedValue("category_payable", id)=='Y'? 'Yes':'No';
   	document.getElementById('ed_insurance_category_name').value = getIndexedValue('insuranceCategoryName', id)
   	document.getElementById('ed_insurance_category_name_label').textContent = getIndexedValue("insuranceCategoryName", id);
   	if(getIndexedValue("category_payable", id)=='N'){
		document.getElementById('ed_category_payable_label').style.color = 'red';
	}else{
		document.getElementById('ed_category_payable_label').style.color = '';
	}
	document.getElementById('ed_package_size').value = getIndexedValue('pkg_size', id);
	document.getElementById('ed_price').value = getIndexedValue('pkg_price', id);
	document.getElementById('ed_ispackage').value = getIndexedValue("ispackage", id);
	document.getElementById('ed_remarks').value = getIndexedValue('item_remarks', id);
	document.getElementById('ed_special_instruction').value = getIndexedValue('special_instr', id);
	document.getElementById('ed_item_master').value = getIndexedValue('item_master', id);

	var isInsurancePatient = document.getElementById('tpa_id').value != '';
	if (isInsurancePatient
		&& ((itemType == 'Medicine' && nonHospMedicine == 'false' && !useGenerics)
				|| itemType == 'Inv.'
				|| itemType == 'Service'
				|| itemType == 'Operation'
				|| itemType == 'Doctor')) {
		document.getElementById('ed_priorAuthLabelTd').style.display = 'block';
		document.getElementById('ed_priorAuth_label').style.display = 'block';
		document.getElementById('ed_category_payable_show').style.display = 'table-row';
		//document.getElementById('ed_category_payable_show').style.visibility='visible';
	} else {
		document.getElementById('ed_priorAuthLabelTd').style.display = 'none';
		document.getElementById('ed_priorAuth_label').style.display = 'none';
		document.getElementById('ed_category_payable_show').style.display = 'none';
	}

	if (document.getElementById('ed_markPriorAuthReqTd') != null) {
		if (isInsurancePatient
				&& (itemType == 'Inv.' || itemType == 'Service' || itemType == 'Operation' || itemType == 'Doctor')) {
			document.getElementById('ed_markPriorAuthReqTd').style.display = 'block';
			document.getElementById('ed_markPriorAuthCheckBox').style.display = 'block';
		}else {
			document.getElementById('ed_markPriorAuthReqTd').style.display = 'none';
			document.getElementById('ed_markPriorAuthCheckBox').style.display = 'none';
		}
	}

	var prior_auth = getIndexedValue('priorAuth', id);
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = getString('js.outpatient.consultation.mgmt.notrequired');
	} else if (prior_auth == 'A') {
		prior_auth_text = getString('js.outpatient.consultation.mgmt.required');
	} else if (prior_auth == 'S') {
		prior_auth_text = getSring('js.outpatient.consultation.mgmt.mayberequired');
	}
	document.getElementById('ed_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('ed_priorAuth').value = prior_auth;

	var preAuthRequired = getIndexedValue("requirePriorAuth", id);
	if (document.getElementById('ed_markPriorAuthReq'))
		document.getElementById('ed_markPriorAuthReq').checked = (preAuthRequired == 'Y');

	var issued = getIndexedValue('issued', id);
	if (issued == 'O') {
		document.getElementById('ed_admin_strength').disabled = true;
		document.getElementById('ed_frequency').disabled = true;
		document.getElementById('ed_strength').disabled = true;
		document.getElementById('ed_duration').disabled = true;
		document.getElementById('ed_qty').disabled = true;
		document.getElementById('ed_remarks').disabled = true;
		document.getElementById('ed_item_strength').disabled = true;
		document.getElementById('ed_item_strength_units').disabled = true;
		document.getElementById('ed_item_form_id').disabled = true;
		document.getElementById('ed_tooth_number').disabled = true;
		document.getElementById('ed_consumption_uom').disabled = true;
		document.getElementById('edToothNumBtnDiv').style.display = 'none';
		document.getElementById('edToothNumDsblBtnDiv').style.display = 'block';
	}
	initEditInstructionAutoComplete('ed');
	document.getElementById('ed_remarks').focus();
	if(mod_ceed_enabled && (roleId == 1 || roleId == 2 || has_right_to_view_ceed_comments)) {
		document.getElementById('ceedcommentsfieldset').style.display = 'block';
		var prescription_id = getIndexedValue('item_prescribed_id', id);
		if(prescription_id in ceedresponsemap) {
			var responseedits = ceedresponsemap[prescription_id];
			var addedcount = 0;
			var ulist = document.getElementById('ceed_response_comments');
			while(ulist.hasChildNodes()) {
				ulist.removeChild(ulist.lastChild);
			}
			for(var i=0;i<responseedits.length;i++) {
				var comment = responseedits[i]["claim_edit_response_comments"];
				if(comment != null && comment != "") {
					var litem = document.createElement("li");
					litem.innerHTML=comment;
					ulist.appendChild(litem);
					addedcount++;
				}
			}
			if(addedcount == 0) {
				document.getElementById('ceedcommentsfieldset').style.display = 'none';
			}
		}
		else {
			document.getElementById('ceedcommentsfieldset').style.display = 'none';
		}
	}
	return false;
}

function checkPreAuthReq() {
}

function editTableRow() {

	var id = document.prescribeForm.editRowId.value;
	var qtyEdited = document.getElementById('ed_qty_edited').value;
	var itemType = document.getElementById('ed_itemType').value;
	var non_hosp_medicine = document.getElementById('ed_non_hosp_medicine').value;
	var send_item_for_erx = getIndexedValue("send_item_for_erx", id);

   	if (itemType == 'Medicine' && non_hosp_medicine == 'false' && send_item_for_erx == 'Y' && !validateAddOrEditERxData()) {
		return false;
	}

	// Even after item is issued Prior auth Request can be made. Need to check.
	var markPriorAuthReqObj = document.getElementById('ed_markPriorAuthReq');

	if (markPriorAuthReqObj != null) {
		if (markPriorAuthReqObj.checked) {
			setHiddenValue(id, "requirePriorAuth", "Y");
		}else {
			setHiddenValue(id, "requirePriorAuth", "N");
		}
	}

	var issued = getIndexedValue("issued", id);
	if (issued == 'O') {
		editItemDialog.cancel();
		return true;
	}
	var row = getChargeRow(id, 'itemsTable');


   	var itemName = document.getElementById('ed_itemName').value;
   	var itemId = document.getElementById('ed_item_id').value;
	var adminStrength = document.getElementById('ed_admin_strength').value;
   	var frequency = document.getElementById('ed_frequency').value;
   	var strength = document.getElementById('ed_strength').value;
   	var duration = document.getElementById('ed_duration').value;
   	var qty = document.getElementById('ed_qty').value;

   	var remarks = document.getElementById('ed_remarks').value;
   	var spl_instruction = document.getElementById('ed_special_instruction').value;
   	var master = document.getElementById('ed_item_master').value;
   	var ispackage = document.getElementById('ed_ispackage').value;
   	var pkg_size = getAmount(document.getElementById('ed_package_size').value);
   	var drugCode = document.getElementById('ed_drug_code_label').value;
   	var price = getPaise(document.getElementById('ed_price').value);
   	var consumption_uom = document.getElementById('ed_consumption_uom').value;
   	var item_form_id = document.getElementById('ed_item_form_id').value;
   	var granular_unit = document.getElementById('ed_granular_units').value;
   	var item_strength = document.getElementById('ed_item_strength').value;
   	var item_form_name = document.getElementById('ed_item_form_id').options[document.getElementById('ed_item_form_id').selectedIndex].text;
   	item_form_name = item_form_id == '' ? '' : item_form_name;
   	var tooth_number = document.getElementById('ed_tooth_number').value;
   	var refills = document.getElementById('ed_refills').value;
	var item_pkg_price = 0;
	var item_unit_price = 0;

	var item_strength_units = document.getElementById('ed_item_strength_units').value;
	item_strength_units = item_strength == '' ? '' : item_strength_units;
   	var strength_unit_name = document.getElementById('ed_item_strength_units').options[document.getElementById('ed_item_strength_units').selectedIndex].text;
   	strength_unit_name = item_strength_units == '' ? '' : strength_unit_name;
   	if (!empty(granular_unit) && granular_unit == 'Y') {
	   	if (strength != '' && (!isDecimal(strength, 2) || strength == 0)) {
	   		showMessage("js.outpatient.consultation.mgmt.dosageshouldbegreater.zeroandnumber.two.in.brackets");
	   		document.getElementById('ed_strength').focus();
	   		return false;
	   	}
	}
   	var	non_hosp_medicine = document.getElementById('ed_non_hosp_medicine').value;
   	if (itemType == 'Medicine' && non_hosp_medicine == 'false') {
		if (mod_eclaim_erx == 'Y') {
			if (!validateEprxFields("ed", false)) return false;
		}else if (mod_eclaim_pbm == 'Y') {
			if (!validatPBMFields("ed", false)) return false;
		}
	}

	var duration_radio_els = document.getElementsByName('ed_duration_units');
	var duration_units;
	for (var k=0; k<duration_radio_els.length; k++) {
		if (duration_radio_els[k].checked) {
			duration_units = duration_radio_els[k].value;
			break;
		}
	}
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);
   	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		showMessage("js.outpatient.consultation.mgmt.durationshouldbegreater.zeroandnum");
		document.getElementById('ed_duration').focus();
		return false;
	}
	if (!empty(duration) && empty(duration_units)) {
		showMessage("js.outpatient.consultation.mgmt.pleaseselectthedurationunits");
		return false;
	}

	if (itemType == 'Service' || (itemType == 'Medicine' && non_hosp_medicine == 'false')) {
		if (qty == '') {
			showMessage("js.outpatient.pending.prescriptions.addshow.enter.qty");
			document.getElementById('ed_qty').focus();
			return false;
		}
	}
	if (qty != '' && (!regExp.test(qty) || qty == 0)) {
		showMessage("js.outpatient.consultation.mgmt.qtyshouldbegreater.number");
		document.getElementById('ed_qty').focus();
		return false;
	}

	if (document.getElementById('ed_tooth_num_required').value == 'Y' && tooth_number == '') {
		showMessage("js.outpatient.consultation.mgmt.toothnumberrequiredfortheservice");
		document.getElementById('ed_tooth_number').focus();
		return false;
	}

	setNodeText(row.cells[ITEM_TYPE], itemType + (itemType == 'Medicine' && non_hosp_medicine == 'true' ? '[Non Hosp]' : ''));
   	setNodeText(row.cells[ITEM_NAME], itemName + (tooth_number == '' ? '' : ('[' + tooth_number + ']')), 20);
   	if (itemType == 'Medicine' || itemType == 'NonHospital') {

		setNodeText(row.cells[ADMIN_STRENGTH], adminStrength, 15);
		var details = "";
   		if (frequency != '' || duration != '')
   			details = frequency + " / " + duration + " " + duration_units;
		setNodeText(row.cells[DETAILS], details, 20);
		setNodeText(row.cells[QTY], qty);
		setNodeText(row.cells[FORM], item_form_name, 15);
		setNodeText(row.cells[STRENGTH], item_strength + ' ' + strength_unit_name, 15);

		setHiddenValue(id, "admin_strength", adminStrength);
		setHiddenValue(id, "frequency", frequency);
		setHiddenValue(id, "strength", strength);
		setHiddenValue(id, "duration", duration);
		setHiddenValue(id, "duration_units", duration_units);
		setHiddenValue(id, "medicine_quantity", qty);
		setHiddenValue(id, "item_form_id", item_form_id);
		setHiddenValue(id, "granular_units", granular_unit)
		setHiddenValue(id, "item_strength", item_strength);
		setHiddenValue(id, "item_strength_units", item_strength_units);
		setHiddenValue(id, "qty", qty);
		setHiddenValue(id, "refills", refills);
		if (pkg_size != '' && price != '' && qty != '') {
			item_unit_price = (price/pkg_size) * qty;
			item_pkg_price = Math.ceil(qty/pkg_size) * price;

			if (qtyEdited == 'true') {
				var discountRS = formatAmountPaise(getPaise(getIndexedValue('medDiscRS', id)));
				discountRS = ((price/pkg_size) * qty * discountRS/100);
	            setHiddenValue(id, "amt", formatAmountPaise(((price/pkg_size) * qty - discountRS)));
			}
		}

	} else {
		if (qtyEdited == 'true') {
			var discount = getIndexedValue('disc', id);
			var amt = price;
			if (itemType == 'Service')
				amt = amt * qty;
			setHiddenValue(id, "amt", formatAmountPaise(amt));
		}
		item_pkg_price = price;
		item_unit_price = itemType == 'Service' ? price * qty : price;
	}
	if (itemType == 'Service') {
		setNodeText(row.cells[QTY], qty);
		setHiddenValue(id, "qty", qty);
	}

	setNodeText(row.cells[PKG_PRICE], item_pkg_price == 0 ? '' : formatAmountPaise(item_pkg_price));
	setNodeText(row.cells[UNIT_PRICE], item_unit_price == 0? '' : formatAmountPaise(item_unit_price));
	setNodeText(row.cells[REMARKS], remarks, 20);
	setNodeText(row.cells[SPECIAL_INSTRUCTION], spl_instruction, 20);
	setNodeText(row.cells[TOOTH_NUMBER], tooth_number);

	setHiddenValue(id, "itemType", itemType);
	setHiddenValue(id, "item_name", itemName);
	setHiddenValue(id, "drug_code", drugCode);
	setHiddenValue(id, "item_id", itemId);
	setHiddenValue(id, "item_remarks", remarks);
	setHiddenValue(id, "special_instr", spl_instruction);
	setHiddenValue(id, "item_master", master);
	setHiddenValue(id, "ispackage", ispackage);
	setHiddenValue(id, "consumption_uom", consumption_uom);
	setHiddenValue(id, "pkg_size", pkg_size);
	setHiddenValue(id, "pkg_price", formatAmountPaise(price));
	setHiddenValue(id, "item_pkg_price", formatAmountPaise(item_pkg_price));
	setHiddenValue(id, "item_unit_price", formatAmountPaise(item_unit_price));
	setHiddenValue(id, "service_qty", qty);
	if (document.getElementById('ed_tooth_num_required').value == 'Y') {
		if (tooth_numbering_system == 'U')
			setHiddenValue(id, 'tooth_unv_number', tooth_number);
		else
			setHiddenValue(id, 'tooth_fdi_number', tooth_number);
	}

	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("edited", id, 'true');
	setRowStyle(id);

	if (itemType == 'Medicine' && (useGenerics || use_store_items == 'N')) {
	} else {
		estimateTotal();
	}

	if (qtyEdited == 'true' && getIndexedValue('item_prescribed_id', id) == '_') {
		var visitId = document.getElementById('patient_id').value;
		if (itemType == 'Medicine') {
			if (non_hosp_medicine == 'false' && use_store_items == 'Y' && prescriptions_by_generics != 'true')
				getSaleItemClaim(visitId, document.prescribeForm);
		} else {
			getItemClaims(visitId, document.prescribeForm);
		}
	}
	editItemDialog.cancel();
	return true;
}
var fieldEdited = false;
function setEdited() {
	fieldEdited = true;
}

function openPrevious() {
	var id = document.prescribeForm.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		if (!editTableRow()) return false;
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditItemDialog(document.getElementsByName('_prescEditAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var id = document.prescribeForm.editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'itemsTable');

	if (fieldEdited) {
		if (!editTableRow()) return false;
		fieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('itemsTable').rows.length-2) {
		showEditItemDialog(document.getElementsByName('_prescEditAnchor')[parseInt(id)+1]);
	}
}

function initFrequencyAutoComplete() {
	if (dosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dosage_name"},
						{key : "per_day_qty"},
					 ]
		};
		// Instantiate first AutoComplete
		dosageAutoComplete = new YAHOO.widget.AutoComplete('d_frequency', 'frequencyContainer', ds);
		dosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		//dosageAutoComplete.typeAhead = true;
		dosageAutoComplete.useShadow = true;
		dosageAutoComplete.minQueryLength = 0;
		dosageAutoComplete.allowBrowserAutocomplete = false;
		dosageAutoComplete.maxResultsDisplayed = 20;
		dosageAutoComplete.resultTypeList = false;
		dosageAutoComplete.formatResult = Insta.autoHighlight;

		dosageAutoComplete.itemSelectEvent.subscribe(setPerDayQty);
		dosageAutoComplete.unmatchedItemSelectEvent.subscribe(checkDosage);
		dosageAutoComplete.textboxChangeEvent.subscribe(clearQty);

	}
}

function setPerDayQty(sType, oArgs) {
	if (document.getElementById('d_granular_units').value == 'Y' ) {
		var record = oArgs[2];
		document.getElementById('d_per_day_qty').value = record.per_day_qty;
	}
	calcQty('d');
	setAutoGeneratedInstruction('d');
}

function checkDosage() {
	if (document.getElementById('d_granular_units').value == 'Y' ) {
		document.getElementById('d_per_day_qty').value = '';
	}
}

function clearQty(){
	if (document.getElementById('d_granular_units').value == 'Y' ) {
		document.getElementById('d_qty').value = '';
	}
	calcQty('d');
	setAutoGeneratedInstruction('d');
}

function initInstructionAutoComplete() {
	if (instructionAutoComplete == null) {
		ds = new YAHOO.util.LocalDataSource({result : presInstructions});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "instruction_desc"}, ]
		};
		// Instantiate first AutoComplete
		instructionAutoComplete = new YAHOO.widget.AutoComplete('d_remarks', 'remarksContainer', ds);
		instructionAutoComplete.minQueryLength = 0;
		instructionAutoComplete.allowBrowserAutocomplete = false;
		instructionAutoComplete.animVert = false;
		instructionAutoComplete.maxResultsDisplayed = 50;
		instructionAutoComplete.queryMatchContains = true;
		instructionAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
			return Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		};
		instructionAutoComplete.resultTypeList = false;
		instructionAutoComplete.autoSnapContainer = false;
		if (document.getElementById('d_remarks').value != '') {
			instructionAutoComplete._bItemSelected = true;
			instructionAutoComplete._sInitInputValue = document.getElementById('d_remarks').value;
		}
	}
}

function calcQty(idPrefix){
	if (document.getElementById(idPrefix + '_granular_units').value == 'Y' ) {
		var qty = '';
		var frequencyName = document.getElementById(idPrefix + '_frequency').value;
		var duration = document.getElementById(idPrefix + '_duration').value;
		var validNumber = /[1-9]/;
		var regExp = new RegExp(validNumber);

		if (!validateMedBlockExceptQty("onchange", idPrefix)) return false;

		var perDayQty = null;
		for (var i=0; i<medDosages.length; i++) {
			var frequency = medDosages[i];
			if (frequencyName.trim().toLowerCase() == frequency.dosage_name.trim().toLowerCase()) {
				perDayQty = frequency.per_day_qty;
			}
		}
		if (perDayQty != null && !empty(duration)) {
			var duration_units_els = document.getElementsByName(idPrefix+'_duration_units');
			var dosage = document.getElementById(idPrefix+'_strength').value;
			dosage = dosage == "" || isNaN(dosage) ? 1 : dosage;
			var duration_units = 'D';
			for (var j=0; j<duration_units_els.length; j++) {
				if (duration_units_els[j].checked) {
					duration_units = duration_units_els[j].value;
					break;
				}
			}
			if (duration_units == 'D')
				qty = Math.ceil(duration * perDayQty * dosage);
			else if (duration_units == 'W')
				qty = Math.ceil((duration * 7) * perDayQty * dosage);
			else if (duration_units == 'M')
				qty = Math.ceil((duration * 30) * perDayQty * dosage);

		}
		document.getElementById(idPrefix + '_qty').value = qty;

		if (idPrefix == 'ed') {
			qtyEdited('ed');
		}
	}
}

function qtyEdited(prefix) {
	document.getElementById(prefix + '_qty_edited').value = 'true';
}

function setAutoGeneratedInstruction(prefix) {
	if (document.getElementById(prefix+'_granular_units').value == 'Y' ) {
		var instruction = 'USE ';
		var numberOfUnit = document.getElementById(prefix +'_strength').value;
		var granularUnit = document.getElementById(prefix +'_consumption_uom').value;
		var frequency = document.getElementById(prefix +'_frequency').value;
		var duration = document.getElementById(prefix +'_duration').value;

		instruction += empty(numberOfUnit) ? ' ' : numberOfUnit + ' ';
		instruction += empty(granularUnit) ? ' ': granularUnit + ' ';
		instruction += empty(frequency) ? ' ': frequency + ' ';
		instruction += 'FOR A DURATION OF ';
		instruction += empty(duration) ? ' ': duration + ' ';
		if (!empty(duration)) {
			var duration_units_els = document.getElementsByName(prefix +'_duration_units');
			var duration_units = 'D';
			for (var j=0; j<duration_units_els.length; j++) {
				if (duration_units_els[j].checked) {
					duration_units = duration_units_els[j].value;
					break;
				}
			}
			instruction += (duration_units == 'D' ? 'Days.' : (duration_units == 'W' ? 'Weeks.': 'Months.'));
		}
		document.getElementById(prefix +'_remarks').value = instruction;
	}
}

var editDosageAutoComplete = null; // dosage autocomplete for edit item dialog.
function initEditDosageAutoComplete() {
	if (editDosageAutoComplete == null) {

		ds = new YAHOO.util.LocalDataSource({result : medDosages});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "dosage_name"},
						{key : "per_day_qty"},
					 ]
		};
		// Instantiate first AutoComplete
		editDosageAutoComplete = new YAHOO.widget.AutoComplete('ed_frequency', 'ed_frequencyContainer', ds);
		editDosageAutoComplete.prehighlightClassName = "yui-ac-prehighlight";
		//dosageAutoComplete.typeAhead = true;
		editDosageAutoComplete.useShadow = true;
		editDosageAutoComplete.minQueryLength = 0;
		editDosageAutoComplete.allowBrowserAutocomplete = false;
		editDosageAutoComplete.maxResultsDisplayed = 20;
		editDosageAutoComplete.resultTypeList = false;

		editDosageAutoComplete.itemSelectEvent.subscribe(editSetPerDayQty);
		editDosageAutoComplete.unmatchedItemSelectEvent.subscribe(editCheckDosage);
		editDosageAutoComplete.textboxChangeEvent.subscribe(editClearQty);
	}
}

function editSetPerDayQty(sType, oArgs) {
	if (document.getElementById('ed_granular_units').value == 'Y' ) {
		var record = oArgs[2];
		document.getElementById('ed_per_day_qty').value = record.per_day_qty;
	}
	calcQty('ed');
	setAutoGeneratedInstruction('ed');
}

function editCheckDosage() {
	if (document.getElementById('ed_granular_units').value == 'Y' ) {
		document.getElementById('ed_per_day_qty').value = '';
	}
}

function editClearQty() {
	if (document.getElementById('ed_granular_units').value == 'Y' ) {
		document.getElementById('ed_qty').value = '';
		setEdited();
	}
	calcQty('ed');
	setAutoGeneratedInstruction('ed');
}

var editInstructionAutoComplete = null; // remarks autocomplete for edit item dialog.
function initEditInstructionAutoComplete() {
	if (editInstructionAutoComplete == null) {
		ds = new YAHOO.util.LocalDataSource({result : presInstructions});
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "instruction_desc"}, ]
		};
		// Instantiate first AutoComplete
		editInstructionAutoComplete = new YAHOO.widget.AutoComplete('ed_remarks', 'ed_remarksContainer', ds);
		editInstructionAutoComplete.minQueryLength = 0;
		editInstructionAutoComplete.allowBrowserAutocomplete = false;
		editInstructionAutoComplete.animVert = false;
		editInstructionAutoComplete.maxResultsDisplayed = 50;
		editInstructionAutoComplete.queryMatchContains = true;
		editInstructionAutoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
			return Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		};
		editInstructionAutoComplete.resultTypeList = false;
		editInstructionAutoComplete.autoSnapContainer = false;
		if (document.getElementById('ed_remarks').value != '') {
			editInstructionAutoComplete._bItemSelected = true;
			editInstructionAutoComplete._sInitInputValue = document.getElementById('ed_remarks').value;
		}
	}
}

function validateMedBlockExceptQty(calledOn, idPrefix) {
	var itemType = (idPrefix == 'd' ? getItemType() : document.getElementById(idPrefix + "_itemType").value);
	if (itemType != 'Medicine' && itemType != 'NonHospital') return;

	var medicineName = document.getElementById(idPrefix + '_itemName').value;
	var duration = document.getElementById(idPrefix + '_duration').value;
	var validNumber = /^[0-9]+$/;
	var regExp = new RegExp(validNumber);

	if (medicineName == '') {
		showMessage("js.outpatient.consultation.mgmt.pleaseenterthemedicinename");
		return false;
	}
	if (duration != '' && (!regExp.test(duration) || duration == 0)) {
		showMessage("js.outpatient.consultation.mgmt.durationshouldbegreater.zeroandnumber");
		document.getElementById(idPrefix + '_duration').focus();
		return false
	}
	return true;
}

function validatePrescriptions() {
	var tooth_num_required = document.getElementsByName('tooth_num_required');
	var tooth_unv_number = document.getElementsByName('tooth_unv_number');
	var tooth_fdi_number = document.getElementsByName('tooth_fdi_number');
	var itemName = document.getElementsByName('item_name');
	var services = new Array();

	var issued = document.getElementsByName("issued");
	var strength_els = document.getElementsByName("strength");
	var granular_units_els = document.getElementsByName("granular_units");
	var medicines = new Array();
	for (var i=0; i<tooth_num_required.length; i++) {
		if (tooth_num_required[i].value == 'Y' && tooth_unv_number[i].value == '' && tooth_fdi_number[i].value == '') {
			services.push(itemName[i].value);
		}
		if (issued[i].value != 'O' && granular_units_els[i].value == 'Y' && strength_els[i].value != ''
			&& (!isDecimal(strength_els[i].value, 2) || strength_els[i].value == 0)) {
			medicines.push(itemName[i].value);
		}
	}
	if (services.length > 0) {
		alert(getString("js.outpatient.consultation.mgmt.thefollwingservices.required")+" "+services.join("\n  * "));
		return false;
	}

	if (medicines.length > 0) {
		alert(getString("js.outpatient.consultation.mgmt.dosageshouldbegreater.zeroandwholenumber") +
				getString("js.outpatient.consultation.mgmt.correctthedosageinformation") + medicines.join("\n * "));
		return false;
	}
	return true;
}


function editDialogGeneric() {
	document.getElementById('genericNameDisplayDialog').style.visibility = 'display';
	genericDialog = new YAHOO.widget.Dialog("genericNameDisplayDialog",
			{
				width:"500px",
				context : ["loadGenInfo", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );
	YAHOO.util.Event.addListener("genericNameCloseBtn", "click", closeGenericDialog, genericDialog, true);
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
			{ fn:closeGenericDialog, scope:genericDialog, correctScope:true } );
	genericDialog.cancelEvent.subscribe(closeGenericDialog);
	genericDialog.cfg.setProperty("keylisteners", kl);
	genericDialog.render();
}

function closeGenericDialog() {
	childDialog = null;
	this.hide();
}

function showGenericInfo(index, prefix, suffix, generic_code) {
	childDialog = genericDialog;
	var anchor = document.getElementById(prefix + "genericNameAnchor" + index + "_" + suffix);
	genericDialog.cfg.setProperty("context", [anchor, "tr", "tl"], false);
	genericDialog.show();
	if (generic_code != "") {
		var ajaxReqObject = new XMLHttpRequest();
		var url=cpath+"/outpatient/OpPrescribeAction.do?_method=getGenericJSON&generic_code="+encodeURIComponent(generic_code);
		getResponseHandlerText(ajaxReqObject, handleGenericResponse, url);
	} else {
		document.getElementById('classification_name').innerHTML = '';
		document.getElementById('sub_classification_name').innerHTML = '';
		document.getElementById('standard_adult_dose').innerHTML = '';
		document.getElementById('criticality').innerHTML = '';
		document.getElementById('generic_name').innerHTML = '';
	}
}

/*
 * Response handler for the ajax call to retrieve generic details like classification and sub-classification
 */
function handleGenericResponse(responseText) {
	if (responseText==null) return;
	if (responseText=="") return;
	var genericDetails;
    eval("var genericDetails = " + responseText);			// response is an array of item batches
    if (genericDetails != null) {
		var genericId = genericDetails.generic_code;
		document.getElementById('classification_name').innerHTML = genericDetails.classificationName;
		if (genericDetails.sub_ClassificationName != null) {
			document.getElementById('sub_classification_name').innerHTML = genericDetails.sub_ClassificationName;
		}
		document.getElementById('standard_adult_dose').innerHTML = genericDetails.standard_adult_dose;
		document.getElementById('criticality').innerHTML = genericDetails.criticality;
		document.getElementById('gen_generic_name').innerHTML = genericDetails.gmaster_name;

	}
}
function initPkgValueCapDialog() {
	var dialogDiv = document.getElementById("valuePkgDialog");
	if (empty(dialogDiv)) return ;
	dialogDiv.style.display = 'block';
	valuePkgDialog = new YAHOO.widget.Dialog("valuePkgDialog",{
			width:"400px",
			text: "Package Value Cap",
			context :["btnValueCap", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onCancel,
	                                                scope:valuePkgDialog,
	                                                correctScope:true } );
	valuePkgDialog.cancelEvent.subscribe(onCancel);
	valuePkgDialog.cfg.queueProperty("keylisteners", escKeyListener);
	valuePkgDialog.render();

}

function onCancel() {
	valuePkgDialog.hide();
	clearPackageDetails();
}

function getPackageDetails(obj){
	var pkgId = document.getElementById('pkgid').value;
	pkgId = encodeURIComponent(pkgId);
	makeAjaxCallforStaticPackage(pkgId);
	showValuePkgDialog(obj);
	return null;
}

function showValuePkgDialog(obj) {
	valuePkgDialog.show();
	valuePkgDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
}

function makeAjaxCallforStaticPackage(pkgId, curPage) {
	var url = cpath +'/master/orderItems.do?method=getOrderPackageDetails&pkg_id='+pkgId;
	if (curPage)
		url += "&pageNum="+curPage;
	url += "&pageSize=10";

	YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: populatePackageDetails,
			failure: failedToPackageDetails,
			argument: []
		});
}

function populatePackageDetails(response) {
	var pkgId = document.getElementById('pkgid').value;
	var activityDescription='';
	var activity_qty='';
	var activity_type='';

 	if (response.responseText != undefined) {
		var pacakgeDetails = eval('(' + response.responseText + ')');
		var table = document.getElementById('staticpackageDetailsTab');
			for (var i=1; i<table.rows.length; ) {
				table.deleteRow(i);
			}
			var dtoList = pacakgeDetails.dtoList;
				generatePaginationSection(pkgId,pacakgeDetails.pageNumber, pacakgeDetails.numPages);

		for(var i=0;i<dtoList.length;i++){
			activityDescription	= dtoList[i].activity_description;
			if(activityDescription == "" || activityDescription == null) {
				activityDescription = 'Doctor Consultation';
			}
			activity_type = dtoList[i].activity_type;
			if (activity_type == 'Doctor') {
				var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", dtoList[i].consultation_type_id);
				activityDescription = consultationType.consultation_type;
			}
			activity_qty = dtoList[i].activity_qty;
			table.innerHTML += '<tr>'+'<td class="formlabel" style="width:10px;">'+activityDescription+'</td>'
			+'<td class="formlabel" style="width:10px;text-align:center;">'+activity_type+'</td>'+
			'<td class="formlabel" style="width:10px;text-align:center;">'+activity_qty+'</td></tr>';
		}
	}
}

function failedToPackageDetails() {
}

function clearPackageDetails(){
	var table= document.getElementById('staticpackageDetailsTab');
	if(table && table.rows.length > 0) {
		for (var i=table.rows.length-1;i>0;i--) {
			table.deleteRow(i);
		}
	}
}

function generatePaginationSection(pkgId,curPage, numPages) {
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';
	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCallforStaticPackage("'+encodeURIComponent(pkgId)+'",'+(curPage-1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}
		if (curPage > 1 && curPage < numPages) {
			var txtEl = document.createTextNode(' | ');
			div.appendChild(txtEl);
		}
		if (curPage < numPages) {
			var txtEl = document.createTextNode('Next>>');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCallforStaticPackage("'+encodeURIComponent(pkgId)+'",'+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}


function initDoctorFavouriteItems() {

	if (!empty(itemAutoComp)) {
		itemAutoComp.destroy();
		itemAutoComp = null;
	}
	if (!empty(itemDocFavAutoComp)) {
		itemDocFavAutoComp.destroy();
		itemDocFavAutoComp = null;
	}

	var non_hosp_medicine = document.getElementById('d_non_hosp_medicine').checked;
	var itemType = getItemType();
	var orgId = document.getElementById('org_id').value;
	var tpaId = document.getElementById('tpa_id').value;
	var consult_doctor_id = document.getElementById('consult_doctor_id').value;
	var ds = new YAHOO.util.XHRDataSource(cpath + '/outpatient/OpPrescribeAction.do');
	ds.scriptQueryAppend = "_method=findDoctorFavouriteItems&searchType=" + itemType + "&org_id=" + orgId + "&consult_doctor_id=" + consult_doctor_id +
					"&center_id=" + centerId + "&p_health_authority=" + health_authority + "&tpa_id=" + tpaId + "&non_hosp_medicine="+ non_hosp_medicine;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "order_code"},
					{key : "item_id"},
					{key : "qty"},
					{key : "generic_code"},
					{key : "drug_code"},
					{key : "generic_name"},
					{key : "ispkg"},
					{key : "master"},
					{key : "item_type"},
					{key : "route_of_admin"},
					{key : "consumption_uom"},
					{key : "prior_auth_required"},
					{key : "item_form_id"},
					{key : "item_strength"},
					{key : "tooth_num_required"},
					{key : "item_strength_units"},
					{key : "granular_units"},
					{key : "remarks"},
					{key : "special_instr"},
					{key : "duration"},
					{key : "duration_units"},
					{key : "frequency"},
					{key : "strength"},
					{key : "admin_strength"},
					{key : "per_day_qty"}
				 ],
		numMatchFields: 2
	};

	itemDocFavAutoComp = new YAHOO.widget.AutoComplete("d_itemName", "itemContainer", ds);
	itemDocFavAutoComp.minQueryLength = 1;
	itemDocFavAutoComp.animVert = false;
	itemDocFavAutoComp.maxResultsDisplayed = 50;
	itemDocFavAutoComp.resultTypeList = false;
	itemDocFavAutoComp.forceSelection = true;

	itemDocFavAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	itemDocFavAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var record = oResultData;
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

		if ( record.item_type == 'Medicine') {
			// show qty only for pharmacy items.
			if (use_store_items == 'Y' && prescriptions_by_generics == 'false')
				highlightedValue += "(" + record.qty + ") ";
			// show generic name along with the medicine name when prescriptions done by brand names.
			if (!useGenerics)
				highlightedValue += (empty(record.generic_name) ? '' : "[" + record.generic_name + "]");
		}
		return highlightedValue;
	}
	itemDocFavAutoComp.dataRequestEvent.subscribe(clearItemDetails);
	itemDocFavAutoComp.itemSelectEvent.subscribe(selectFavItem);
	itemDocFavAutoComp.selectionEnforceEvent.subscribe(clearItemDetails);

	return itemDocFavAutoComp;
}

function selectFavItem(sType, oArgs) {
	var record = oArgs[2];
	document.getElementById('d_item_id').value = record.item_id;
	if (record.item_type == 'Medicine') {
		document.getElementById('d_qty_in_stock').value = record.qty;
		if (!empty(record.generic_name)) {
			document.getElementById('genericNameAnchor_dialog').style.display = 'block';
			document.getElementById('genericNameAnchor_dialog').href = 'javascript:showGenericInfo("", "", "dialog", "'+record.generic_code+'")';
			document.getElementById('genericNameAnchor_dialog').innerHTML = record.generic_name;
			document.getElementById('d_generic_code').value = record.generic_code;
			document.getElementById('d_generic_name').value = record.generic_name;
		}
	}
	var prior_auth = record.prior_auth_required;
	var prior_auth_text = '';
	if (prior_auth == 'N') {
		prior_auth_text = 'Not Required';
	} else if (prior_auth == 'A') {
		prior_auth_text = 'Required';
	} else if (prior_auth == 'S') {
		prior_auth_text = 'May be Required';
	}
	document.getElementById('d_priorAuth_label').textContent = prior_auth_text;
	document.getElementById('d_priorAuth').value = prior_auth;

	var markPriorAuthReqObj = document.getElementById('d_markPriorAuthReq');
	if (markPriorAuthReqObj != null) {
		if (prior_auth == 'A' && !empty(TPArequiresPreAuth) && TPArequiresPreAuth == 'Y') {
			markPriorAuthReqObj.checked = true;
		}else {
			markPriorAuthReqObj.checked = false;
		}
	}

	document.getElementById('d_drug_code').value = empty(record.drug_code) ? '' : record.drug_code;
	document.getElementById('d_consumption_uom').value = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_consumption_uom_label').textContent = empty(record.consumption_uom) ? '' : record.consumption_uom;
	document.getElementById('d_ispackage').value = record.ispkg;
	if (record.ispkg) {
		document.getElementById('pkg_details_button').style.display = 'table-row';
		var pkg_id = record.item_id;
		document.getElementById('pkgid').value = pkg_id;
	} else {
		document.getElementById('pkg_details_button').style.display = 'none';
	}
	document.getElementById('d_item_master').value = record.master;
	document.getElementById('d_item_form_id').value = (record.item_form_id == 0 || record.item_form_id == null) ? '' : record.item_form_id;
	document.getElementById('d_item_strength').value = record.item_strength;
	//document.getElementById('d_item_strength_units').value = record.item_strength_units == 0 ? '' : record.item_strength_units;
	document.getElementById('d_granular_units').value = record.granular_units;
	if (record.item_type == 'Medicine') {
		document.getElementById('d_qty').value = parseInt(record.qty) == 0 ? 1 : record.qty;
	} else if (record.item_type == 'Service') {
		document.getElementById('d_qty').value = 1;
	}

	document.getElementById('d_tooth_num_required').value = record.tooth_num_required;
	if (record.tooth_num_required == 'Y') {
		document.getElementById('d_tooth_number').disabled = false;
		document.getElementById('dToothNumBtnDiv').style.display = 'block';
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'none';
	} else {
		document.getElementById('d_tooth_number').disabled = true;
		document.getElementById('dToothNumBtnDiv').style.display = 'none';
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';
	}

	document.getElementById('d_remarks').value = record.remarks;
	document.getElementById('d_special_instruction').value = record.special_instr;
	document.getElementById('d_medicine_route').value = (record.route_of_admin == 0 || record.route_of_admin == null) ? '' : record.route_of_admin;
	document.getElementById('d_duration').value = record.duration;
	document.getElementById('d_frequency').value = record.frequency;
	document.getElementById('d_strength').value = record.strength;
	document.getElementById('d_admin_strength').value = record.admin_strength;

	setPerDayQtyOnAutoFillingFromFav(record.per_day_qty);
	var d_duration_unitsEls = document.getElementsByName('d_duration_units');
	for (var i=0; i<d_duration_unitsEls.length; i++) {
		if (d_duration_unitsEls[i].value == record.duration_units) {
			d_duration_unitsEls[i].checked = true;
			d_duration_unitsEls[i].onchange();
			break;
		}
	}

	getItemRateDetails();
}

function checkOrUncheckShowFavourite() {
	document.getElementById('d_doctor_favourite').checked = prescribe_by_favourites == 'Y';
}

function setPerDayQtyOnAutoFillingFromFav(per_day_qty) {
	document.getElementById('d_per_day_qty').value = per_day_qty;
	calcQty('d');
	setAutoGeneratedInstruction('d');
}

