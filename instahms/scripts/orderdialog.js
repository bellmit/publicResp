/*
 * Functions used by the Order Dialog (add Item). Using Insta.AddOrderDialog namespace to
 * avoid conflicts with other functions used in the main page. Forms and elements
 * defined in AddOrderDialog.jsp are directly referenced here.
 */
YAHOO.util.Event.throwErrors = true;
//YAHOO.widget.Logger.enableBrowserConsole();

Insta.AddOrderDialog = function(contextId, itemList, operItemList, addHandler, docs, anasths,
		ptype, bedType, orgId, prescDoctorName, prescDoctorId,
		regDate, regTime, allowBackdate, fixedOtCharges, discauths, forceSubGroupSelection,
		regPref,anaeTypes, patientId,billNo,isMultiVisitPackageItem ) {
	// initial variables (config)
	this.contextId = contextId;
	this.addHandler = addHandler;
	this.doctorList = docs;
	this.anaesthetistList = anasths;
	this.patientType = ptype;
	this.bedType = bedType;
	this.orgId = orgId;
	this.isInsurance = false;
	this.insurancePlanId = 0;
	this.planIds = [];
	this.orderInsuranceCategoryId = 0;
	this.prescDoctorName = prescDoctorName;
	this.prescDoctorId = prescDoctorId;
	this.regDate = regDate;
	this.regTime = regTime;
	this.itemList = itemList;
	this.operItemList = operItemList;
	this.allowBackdate = allowBackdate;
	this.fixedOtCharges = fixedOtCharges;
	this.discauthList = discauths;
	this.forceSubGroupSelection = forceSubGroupSelection;
	this.anaesthesiaTypesList = anaeTypes;
	this.consultationTypesList = null;

	this.validateTestAdditionalInfo = false;
	this.mandateTestAdditionalInfo = 'N';
	this.additional_test_info = '';

	// config variables set at runtime depending on patient/bill etc.
	this.allowFinalization = false;
	this.allowQtyEdit = false;

	// dialog variables
	this.addDialog = null;
	this.mainFieldSet = document.getElementById('addItemFieldSet');
	this.opFieldSet = document.getElementById('addOperationFieldSet');
	this.docVisitFieldSet = document.getElementById('addDocVisitFieldSet');
	this.discountFieldSet = document.getElementById('addDiscountsFieldSet');
	this.miscFieldSet = document.getElementById('addMisFieldSet');
	this.multiVisitPackageFieldSet = document.getElementById('addMultivisitPackageFieldSet');
	this.addOpAnaesDetailsFieldSet = document.getElementById('addOpAnaesDetailsFieldSet');
	this.addConductingDocFieldSet = document.getElementById('addConductingDocFieldSet');
	this.currentFieldset = null;

	this.itemAutoComp = null;
	this.opItemAutoComp = null;
	this.presDoctorAutoComp = null;
	this.surgeonAutoComp = null;
	this.anaesAutoComp = null;
	this.discauthAutoComp = null;
	this.operationId = null;
	this.condDoctorAutoComp = null;

	// order Item variables, set after selecting the item.
	this.orderItemType = 'Unknown';
	this.orderItemId = null;
	this.orderItemName = null;
	this.orderItemDeptName = null;
	this.orderItemDiscount = null;
	this.pkgHasOp = false;
	this.docVisitsInPack = null;
	this.conductingDoctorsInPack = null;
	this.condDocItemIndexInPack = null;
	this.rateDetails = null;
	this.packageDetails = null;
	this.multiVisitPackageComponentQtyDetails = null;
	this.pkgHasEquipment = false;
	this.itemDetails = null;
	this.multiVisitPackage = false;
	this.isMultiVisitPackageItem = isMultiVisitPackageItem;
	this.isDoctorSelected = false;
	this.isOperationSelected = false;
	this.itemServiceGroupId = null;
//	this.pcakageTotalItemCost = null;

	// transient variables to handle ajax calls, etc.
	this.getChargeRequest = null;
	this.operItems = false;
	this.argument = null;
	this.itemsInput = null;

	this.directChargeSubGroupId = 0;
	this.patientId = patientId;
	this.preAuthReq = 'N';
	this.orderDlgPlanJSON = null;
	this.orderChargeHeadJSON = null;
	this.conductingDoctorReq = null;
	this.conductionRequired = false;
	this.resultEntryApplicable = false;
	this.consExists = false;
	this.restictionType = null;
	this.billNo = billNo;
	this.billOpenDate = null;
	this.toothNumberReq = 'N';
	this.toothNumberEnabled = typeof gToothNumberRequired == 'undefined' ? false : gToothNumberRequired;
	this.toothNumDialog = null;
	this.urgentDisabled = false;

	var orderChargeHeadAjaxObj = newXMLHttpRequest();
	var url = cpath + '/master/orderItems.do?method=getChargeHeadJSON';

	orderChargeHeadAjaxObj.open("POST", url, false);
	orderChargeHeadAjaxObj.send(null);
	if (orderChargeHeadAjaxObj) {
		if (orderChargeHeadAjaxObj.readyState == 4) {
			if ( (orderChargeHeadAjaxObj.status == 200) && (orderChargeHeadAjaxObj.responseText!=null) ) {
				eval("var orderChargeHeadBean = "+orderChargeHeadAjaxObj.responseText);
				if (!empty(orderChargeHeadBean)) {
					this.orderChargeHeadJSON = orderChargeHeadBean;
				}
			}
		}
	}
	// val(accepts): true or false
	this.setValidateTestAdditionalInfo = function(val) {
		this.validateTestAdditionalInfo = val;

		if (val == true)
			// initialize the test additional info dialog.
			initTestAdditionalInfoDialog();
	}



	/*
	 * Public functions
	 */
	this.start = function(obj, operItems, argument, id) {
		// Enable the Add button
		document.orderDialogForm.orderDialogAdd.disabled = false;

		if (!this.validateOrderRatePlan()) return false;

		if(screenid == 'dialysis_order') {
			if(gIsNewItemDeleted) {
				showMessage("js.billing.dialysisorders.approvals.newlyaddeditem.delete");
				return false;
			}
		}

		// operItems: indicates we need to show the operation Items rather than all items.
		// argument is any parameter passed to pass forward in addHandler
		this.operItems = operItems;
		this.argument = argument;
		this.operationId = id;

		this.addDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
		if(gIsInsurance){
			document.getElementById("preAuthLabel").style.display  = 'block';
			document.getElementById("preAuthField").style.display  = 'block';

			document.getElementById("preAuthModeLabel").style.display  = 'block';
			document.getElementById("preAuthModeField").style.display  = 'block';

			if(multiPlanExists){
				document.getElementById("secPreAuthLabel").style.display  = 'block';
				document.getElementById("secPreAuthField").style.display  = 'block';

				document.getElementById("secPreAuthModeLabel").style.display  = 'block';
				document.getElementById("secPreAuthModeField").style.display  = 'block';

				document.getElementById("preAuthNoLbl").style.display ='none';
				document.getElementById("priPreAuthNoLbl").style.display = 'block';

				document.getElementById("preAuthModeLbl").style.display = 'none';
				document.getElementById("priPreAuthModeLbl").style.display = 'block';
			}else{
				document.getElementById("secPreAuthLabel").style.display  = 'none';
				document.getElementById("secPreAuthField").style.display  = 'none';

				document.getElementById("secPreAuthModeLabel").style.display  = 'none';
				document.getElementById("secPreAuthModeField").style.display  = 'none';

				document.getElementById("preAuthNoLbl").style.display = 'block';
				document.getElementById("priPreAuthNoLbl").style.display = 'none';
				document.getElementById("preAuthModeLbl").style.display = 'block';
				document.getElementById("priPreAuthModeLbl").style.display = 'none';
			}
		}else{
			document.getElementById("preAuthLabel").style.display  = 'none';
			document.getElementById("preAuthField").style.display  = 'none';

			document.getElementById("secPreAuthLabel").style.display  = 'none';
			document.getElementById("secPreAuthField").style.display  = 'none';

			document.getElementById("preAuthModeLabel").style.display  = 'none';
			document.getElementById("preAuthModeField").style.display  = 'none';

			document.getElementById("secPreAuthModeLabel").style.display  = 'none';
			document.getElementById("secPreAuthModeField").style.display  = 'none';
		}

		if (operItems) {
			document.getElementById("oPItem").style.display = 'block';
			document.getElementById("item").style.display = 'none';
			this.itemsInput = document.orderDialogForm.orderDialogOpItems;
		} else {
			document.getElementById("oPItem").style.display = 'none';
			document.getElementById("item").style.display = 'block';
			this.itemsInput = document.orderDialogForm.orderDialogItems;
		}

		if (this.allowBackdate != 'A') {
			form.presdate.disabled = true;
			form.prestime.disabled = true;
		}

		this.clearFields(true);
		this.enableButtons('add', false);
		this.addDialog.show();
		this.showAddDialog();
		document.getElementById("orderDialogAddDialog").scrollIntoView(false);

	}

	this.validateOrderRatePlan = function() {
		if (empty(this.orgId)) {
			showMessage("js.registration.patient.rate.plan.required");
			if (document.getElementById('organization') != null)
				document.getElementById('organization').focus();
			if(screenid == 'dialysis_order') {
				document.getElementById('organization_details').focus();
			}
			return false;
		}
		return true;
	}


	/*
	 * Change the org Id to a new one (useful in registration screen, where the org ID can be changed)
	 */
	this.setOrgId = function(orgId) {
		this.orgId = orgId;
	}

	this.setPatientId = function(patientId) {
		this.patientId = patientId;
	}

	this.setBedType = function(bedType) {
		this.bedType = bedType;
	}

	this.setVisitType = function(visitType) {
		this.ptype = visitType;
	}

	this.setUrgentDisabled = function(disabled) {
		this.urgentDisabled = disabled;
	}

	this.setAnaesthesiaTypesList = function(anaeTypes) {
		this.anaesthesiaTypesList = anaeTypes;
	}

	/*
	 * Change the insurance plan Id to a new one. Required for registration, useful for
	 * Billing and Order.
	 */
	this.setInsurance = function(isInsurance, planId) {
		this.isInsurance = isInsurance;
		this.insurancePlanId = planId;
			//* $$$ get details for plan..... add in addorderDialog.
		var planAjaxObj = newXMLHttpRequest();
		var url = cpath + '/master/orderItems.do?method=getAllPlanCharges'
												+ '&visitType=' + this.patientType
												+ '&planId=' + planId;
		planAjaxObj.open("POST", url, true);
		planAjaxObj.send(null);
		if (planAjaxObj) {
			if (planAjaxObj.readyState == 4) {
				if ( (planAjaxObj.status == 200) && (planAjaxObj.responseText!=null) ) {
					eval("var planJSBean = "+planAjaxObj.responseText);
					if (!empty(planJSBean)) {
						this.orderDlgPlanJSON = planJSBean;
					}
				}
			}
		}
	}

	/*
	 * Set the list of items when org ID changes
	 */
	this.setNewItemList = function(newList) {
		this.itemList = newList;
		this.initOrderItemAutoComplete();
		this.initOrderOpItemAutoComplete();
		rateplanwiseitems = newList; // replacing the actual var with the new list.
	}

	this.setNewConsultationTypesList = function(newList) {
		this.consultationTypesList = newList;
		loadSelectBox(form.doctorCharge,this.consultationTypesList,"consultation_type","consultation_type_id",'-- Select --', '');
	}


	this.setNewAnesthesiaTypesList = function(anaeTypes) {
		this.anaesthesiaTypesList = anaeTypes;
		fillAnesthesiaTypes();

	}

	this.setDefaultOptionForSubGroup = function(){
		if(this.forceSubGroupSelection == 'Y'){
			document.getElementById("service_group_id").options[0].text = '-- Select --';
			document.getElementById("service_group_id").options[0].value = 'N';
			document.getElementById("service_sub_group_id").options[0].text = '-- Select --';
			document.getElementById("service_sub_group_id").options[0].value = 'N';
			document.getElementById("service_group_id").focus();
		}
	}

	this.fillAnesthesiaTypes = function(){
		loadSelectBox(document.getElementById("anesthesia_type0"), anaeTypes, 'anesthesia_type_name',
			'anesthesia_type_id','-- Select --', '');
	}


	/*
	 * Object functions (not intended to be public) requiring this to be self.
	 */
	this.initAddDialog = function() {
		document.getElementById("orderDialogAddDialog").style.display = 'block';
		this.addDialog = new YAHOO.widget.Dialog("orderDialogAddDialog", { width:"700px",
				context :[this.contextId, "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:false
				});
		this.addDialog.render();
		subscribeEscKeyEvent(this.addDialog);
		this.addDialog.cancelEvent.subscribe(this.onOrderDialogCancel, this, true);
	}

	/*
	 * Event entry points, with context initialized to this.
	 */
	this.onOrderDialogNext = function() {
		/*
		 * Possible transitions are:
		 *  Normal:    addDialog .
		 *  Operation: addDialog -> opDialog .
		 *  MultiVisitPackage
		 *  if package contains
		 *  Package
		 *   Op+doc:   addDialog -> opDialog -> docVisitDialog .
		 *   Op only:  addDialog -> opDialog .
		 *   doc only: addDialog -> docVisitDialog .
		 *  Direct Charge (discounts): addDialog -> discountDialog
		 *  Direct Charge (miscellaneous): addDialog -> miscDialog
		 */
		if (!this.orderDialogValidate())
			return false;

		if (this.currentFieldSet == this.mainFieldSet) {
			this.currentFieldSet.style.display = 'none';
			if(this.orderItemType == 'Package' && this.multiVisitPackage) {
				this.showMultiVisitPackageDialog();
			} else if (this.orderItemType == 'Operation') {
				this.showOpDialog();
			} else if (this.orderItemType == 'Package' || this.orderItemType == 'Order Sets') {
				if (this.pkgHasOp) {
					this.showOpDialog();
				} else if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0) {
					this.showConductingDoctorDialg();
				} else {
					this.showDocVisitDialog();
				}
			} else if (this.orderItemType == 'Direct Charge') {
				if (this.orderItemId == 'BIDIS') {
					this.showDiscountDialog();
				} else if (this.orderItemId == 'MISOTC') {
					this.showMiscDialog();
				}
			}
		} else if (this.currentFieldSet == this.opFieldSet) {
			this.currentFieldSet.style.display = 'none';
			this.addOpAnaesDetailsFieldSet.style.display = 'none';
			// must be inside a package, show the doc visit dialog here
			if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0) {
				this.showConductingDoctorDialg();
			} else {
				this.showDocVisitDialog();
			}
		} else if (this.currentFieldSet == this.multiVisitPackageFieldSet) {
			this.currentFieldSet.style.display = 'none';
			this.addOpAnaesDetailsFieldSet.style.display = 'none';

			if (this.isOperationSelected) {
				this.showOpDialog();
			} else if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0) {
				this.showConductingDoctorDialg();
			} else if (this.isDoctorSelected) {
				this.showDocVisitDialog();
			}
		} else if (this.currentFieldSet == this.addConductingDocFieldSet) {
			this.currentFieldSet.style.display = 'none';
			this.addOpAnaesDetailsFieldSet.style.display = 'none';
			this.showDocVisitDialog();

		} else {
			alert(getString("js.common.order.invalid.call.of.next.without.current.dialog.string")+" " + this.currentFieldSet);
		}
	}

	this.onOrderDialogPrevious = function() {
		this.currentFieldSet.style.display = 'none';
		this.addOpAnaesDetailsFieldSet.style.display = 'none';
	//	this.pcakageTotalItemCost = this.getTotalPackageItemCost(this.orderItemId);
		if (this.currentFieldSet == this.multiVisitPackageFieldSet || this.currentFieldSet == this.discountFieldSet
				|| this.currentFieldSet == this.miscFieldSet) {
			this.showAddDialog();

		} else if (this.currentFieldSet == this.docVisitFieldSet) {
			if (this.multiVisitPackage) {
				if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0)
					this.showConductingDoctorDialg();
				else
					this.showMultiVisitPackageDialog();
			} else {
				if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0)
					this.showConductingDoctorDialg();
				else if (this.pkgHasOp)
					this.showOpDialog();
				else
					this.showAddDialog();
			}
		} else if (this.currentFieldSet == this.opFieldSet) {
			if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0) {
				this.showConductingDoctorDialg();
			} else if (this.multiVisitPackage) {
				this.showMultiVisitPackageDialog();
			} else {
				this.showAddDialog();
			}
		} else if (this.currentFieldSet == this.addConductingDocFieldSet) {
			if (this.isOperationSelected) {
				this.showOpDialog();
			} else if (this.multiVisitPackage) {
				this.showMultiVisitPackageDialog();
			} else {
				this.showAddDialog();
			}
		}
		document.getElementById('pkg_details_button').style.display = 'table-row';
	}

/*	this.getTotalPackageItemCost = function(itemId) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = "./orderItems.do?_method=getTotalPackageItemCost"
		url = url + "&item_id=" + itemId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return reqObject.responseText;
			}
		}
		return null;
	}*/

	this.currentDialogCancel = function() {
		this.addDialog.cancel();
		this.clearOperAnaesthesiaDetailsTable();
		document.getElementById('pkg_details_button').style.display = 'none';
	}

	// following will by called by YUI whenever cancel happens, through whatever means:
	// dialog.cancel(), Esc key etc.
	this.onOrderDialogCancel = function() {
		clearDoctorVisitsOfPack();
		clearCondDoctorsOfPack();
		hideAllSubDialogBoxes();
		this.clearOperAnaesthesiaDetailsTable();
		var contextId = this.contextId;
		setTimeout(function() {document.getElementById(contextId).focus();}, 100);
		clearMultiVisitTable();
	}



	this.onOrderDialogAdd = function() {
		// final submit button: invoke the add handler after validations
		if (!this.orderDialogValidate()) {
			return false;
		}

		var form = document.orderDialogForm;

		if(!validatePriorAuthMode(form.prior_auth_id, form.prior_auth_mode_id, null, null)) {
			return false;
		}

		var additionalDetails;
//		var addTo = 'orderTable' + this.argument;
		if (this.orderItemType == 'Package') {
			additionalDetails = this.packageDetails;
		}

		var chargeType = '';
		var chargeTypeDisplay = '';
		if (this.orderItemType == 'Doctor') {
			chargeType = this.operItems ? form.otDoctorCharge.value : form.doctorCharge.value;
			chargeTypeDisplay = this.operItems ? getSelText(form.otDoctorCharge) :
				getSelText(form.doctorCharge);
		}

		var fromDate = form.fromDate.value;
		var toDate = form.toDate.value;
		var fromTime = form.fromTime.value;
		var toTime = form.toTime.value;
		var units = form.units.value;
		var quantity = form.quantity.disabled ? "1" : form.quantity.value;
		if (this.orderItemType == 'Direct Charge')
			quantity = form.miscQty.disabled ? "1" : form.miscQty.value;

		if(this.orderItemType == 'Package' && this.multiVisitPackage) {
			this.getChargeRequest = new Array();
			var index = 0;
			var selectedItem = document.getElementsByName('select_item');
			var orderItemQty = document.getElementsByName('order_qty');
			for (var i=0; i<selectedItem.length; i++) {
				if(selectedItem[i].checked) {
					var itemId = this.packageDetails[i].activity_id;
					quantity = orderItemQty[i].value;
					var itemType = this.packageDetails[i].item_type;
					if (itemType == null)		// can happen if package has no components, only operation
						continue;
					var itemName = this.packageDetails[i].activity_description;
					var cType = this.packageDetails[i].charge_head;
					var cTypeDisplay = this.packageDetails[i].chargehead_name;
					var multiVisitPackage = this.packageDetails[i].multi_visit_package;
					var packObId = this.packageDetails[i].pack_ob_id;

					if (itemType == 'Doctor') {
						var vDoctorId = "vDoctorId"+index;
						var doctorNameId = "doctorVisit"+index;
						fromDate = document.getElementById("presdate" + index).value;
						fromTime = document.getElementById("prestime" + index).value;
						itemId = document.getElementById(vDoctorId).value;
						itemName = document.getElementById(doctorNameId).value;
						cType = this.packageDetails[i].consultation_type_id;
						var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", cType);
						cTypeDisplay = consultationType.consultation_type;
						index++;

					} else if (itemType == 'Equipment' || itemType == 'ICU' || itemType == 'Bed') {
						units = this.packageDetails[i].activity_units;
						fromDate = form.presdate.value;
						fromTime = form.prestime.value;
						var noHours = (units == 'H') ? this.packageDetails[i].activity_qty :
							this.packageDetails[i].activity_qty * 24;
						var toDateTime = getExpectedToDateTime(fromDate, fromTime, noHours);
						toDate = toDateTime[0];
						toTime = toDateTime[1];

					} else if (itemType == 'Direct Charge') {
						itemId = this.packageDetails[i].charge_head;
						itemName = this.packageDetails[i].chargehead_name;
						form.miscRate.value = this.packageDetails[i].activity_charge;
						form.miscQty.value = this.packageDetails[i].activity_qty;
					}

					var conductingDoctor = '';
					var conductingDoctorName = '';
					var els = document.getElementsByName('packageActivityIndex');
					for (var j=0; j<els.length; j++) {
						if (parseInt(els[j].value) == i) {
							conductingDoctor = document.getElementsByName('packCondDoctorId')[j].value;
							conductingDoctorName = document.getElementsByName('packConductingDoctor')[j].value;
							break;
						}
					}
					this.setOrder(this.packageDetails[i].item_type,itemId, itemName,
							additionalDetails, cType, cTypeDisplay, fromDate, toDate, fromTime, toTime,
							units, quantity,form.prior_auth_id.value,form.prior_auth_mode_id.value,form.d_tooth_number.value,
							multiVisitPackage,packObId,this.orderItemId,form.sec_prior_auth_id.value,form.sec_prior_auth_mode_id.value,
							conductingDoctor, conductingDoctorName, this.packageDetails[i].mandate_additional_info, this.packageDetails[i].additional_test_info);
				}
			}

		} else if (this.orderItemType == 'Order Sets') {
			this.getChargeRequest = new Array();
			var index = 0;
			var noItemAdded = true;
			for (var i=0; i<this.packageDetails.length; i++) {
				var itemId = this.packageDetails[i].activity_id;
				quantity = this.packageDetails[i].activity_qty;
				var itemType = this.packageDetails[i].item_type;
				if (itemType == null)		// can happen if package has no components, only operation
					continue;
				var itemName = this.packageDetails[i].activity_description;
				var cType = this.packageDetails[i].charge_head;
				var cTypeDisplay = this.packageDetails[i].chargehead_name;
				var packObId = this.packageDetails[i].pack_ob_id;
				var patientType = this.patientType != '' ? this.patientType : this.ptype;

				if (itemType == 'Doctor') {
					cType = this.packageDetails[i].consultation_type_id;
					var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", cType);
					if(patientType !== consultationType.patient_type) {
						continue;
					}
					var vDoctorId = "vDoctorId"+index;
					var doctorNameId = "doctorVisit"+index;
					fromDate = document.getElementById("presdate" + index).value;
					fromTime = document.getElementById("prestime" + index).value;
					itemId = document.getElementById(vDoctorId).value;
					itemName = document.getElementById(doctorNameId).value;
					cTypeDisplay = consultationType.consultation_type;
					index++;

				} else if (itemType == 'Equipment' || itemType == 'ICU' || itemType == 'Bed') {
					units = this.packageDetails[i].activity_units;
					fromDate = form.presdate.value;
					fromTime = form.prestime.value;
					var noHours = (units == 'H') ? this.packageDetails[i].activity_qty :
						this.packageDetails[i].activity_qty * 24;
					var toDateTime = getExpectedToDateTime(fromDate, fromTime, noHours);
					toDate = toDateTime[0];
					toTime = toDateTime[1];

				} else if (itemType == 'Direct Charge') {
					itemId = this.packageDetails[i].charge_head;
					itemName = this.packageDetails[i].chargehead_name;
					form.miscRate.value = this.packageDetails[i].activity_charge;
					form.miscQty.value = this.packageDetails[i].activity_qty;
				}

				var conductingDoctor = '';
				var conductingDoctorName = '';
				var els = document.getElementsByName('packageActivityIndex');
				for (var j=0; j<els.length; j++) {
					if (parseInt(els[j].value) == i) {
						conductingDoctor = document.getElementsByName('packCondDoctorId')[j].value;
						conductingDoctorName = document.getElementsByName('packConductingDoctor').value;
						break;
					}
				}

				this.setOrder(this.packageDetails[i].item_type, itemId, itemName,
					additionalDetails, cType, cTypeDisplay, fromDate, toDate, fromTime, toTime,
					units, quantity,form.prior_auth_id.value,form.prior_auth_mode_id.value,form.d_tooth_number.value,false,
					packObId,this.orderItemId,form.sec_prior_auth_id.value,form.sec_prior_auth_mode_id.value, conductingDoctor, conductingDoctorName,
					this.packageDetails[i].mandate_additional_info, this.packageDetails[i].additional_info_reqts);
				noItemAdded = false;
			}

			if (this.pkgHasOp) {
				noItemAdded = false;
				this.setOrder( "Operation", this.packageDetails[0].operation_id,
						this.packageDetails[0].operation_name, additionalDetails, '', '',
						fromDate, toDate, fromTime, toTime, units, 1,form.prior_auth_id.value, form.prior_auth_mode_id.value,false,
						null,null,form.sec_prior_auth_id.value,form.sec_prior_auth_mode_id.value, null, null,
						'N','');
			}
			if (noItemAdded) {
				showMessage("js.common.order.no.items.to.add")
				this.clearFields();
				if (this.currentFieldSet && this.currentFieldSet != this.mainFieldSet) {
					this.currentFieldSet.style.display = 'none';
				}
				this.showAddDialog();
				this.addDialog.align("tr", "tl");
				document.getElementById("orderDialogAddDialog").scrollIntoView(false);
			}

		} else {
			// clear the input to prevent double click submitting twice.
			var itemValue = this.itemsInput.value;
			this.itemsInput.value = "";
			this.setOrder(this.orderItemType, this.orderItemId, itemValue,
					additionalDetails, chargeType, chargeTypeDisplay,
					fromDate, toDate, fromTime, toTime, units, quantity,form.prior_auth_id.value,form.prior_auth_mode_id.value,
					form.d_tooth_number.value,false,null,null,form.sec_prior_auth_id.value,form.sec_prior_auth_mode_id.value, null, null,
					this.mandateTestAdditionalInfo,this.additional_test_info);
		}
			document.getElementById('pkg_details_button').style.display = 'none';
	}

	this.setOrder = function(itemType, itemId, itemName, additionalDetails, chargeType, chargeTypeDisplay,
								fromDate, toDate, fromTime, toTime, units, quantity,preAuthId, preAuthIdMode,
								toothNumber,multiVisitPackage,packObId,orderItemId, secPreAuthId, secPreAuthMode,
								conductingDoctorId, conductingDoctorName, mandate_test_additional_info, additional_info_reqts) {
		// select handler not called for clear event, so we need to check for empty value and clear the id.
		if (document.orderDialogForm.prescribing_doctor.value == '')
			document.orderDialogForm.prescribing_doctorId.value = '';

		if (document.orderDialogForm.anaesthetist.value == '')
			document.orderDialogForm.anaesthetistId.value = '';

		if (document.orderDialogForm.conducting_doctor.value == '')
			document.orderDialogForm.conducting_doctorId.value = '';

		if (empty(conductingDoctorId)) {
			conductingDoctorId = document.orderDialogForm.conducting_doctorId.value;
			conductingDoctorName = document.orderDialogForm.conducting_doctor.value;
		}

		var order = {itemType: itemType,
			itemId:     itemId,
			itemName:   itemName,
			presDate:   form.presdate.value + ' ' + form.prestime.value,
			fromDate:   fromDate,
			toDate:     toDate,
			fromTime:   fromTime,
			toTime:     toTime,
			from:       fromDate + ' ' + fromTime,
			to:         toDate + ' ' + toTime,
			units:      units,
			quantity:   quantity,
			remarks:    form.remarks.value,
			chargeType: chargeType,           chargeTypeDisplay: chargeTypeDisplay,
			mealTiming: form.mealTiming.value,
			finalized:  form.finalized.checked,

			presDoctorName:   	form.prescribing_doctor.value, presDoctorId: form.prescribing_doctorId.value,
			theatreName:      	getSelText(form.theatre), theatreId: form.theatre.value,
			surgeonName:      	form.surgeon.value, surgeonId: form.surgeonId.value,
			anaesthetistName: 	form.anaesthetist.value, anaesthetistId: form.anaesthetistId.value,
			anaeTypeIds:		document.getElementsByName('anesthesia_type'),
			anaTypeFromDates :  document.getElementsByName('anes_start_date'),
			anaTypeToDates   :  document.getElementsByName('anes_end_date'),
			anaTypeFromTimes :  document.getElementsByName('anes_start_time'),
			anaTypeToTimes   :  document.getElementsByName('anes_end_time'),
			additionalDetails:	additionalDetails,
			addTo:				'orderTable' + this.argument,
			operationRef:		this.operItems ? this.argument : '',
			operationId:		this.operItems ? this.operationId : '',
			prescriptionRef:	'',
			patientId:			this.patientId,
			preAuthNo: preAuthId,
			preAuthModeNo : preAuthIdMode,
			secPreAuthNo : secPreAuthId,
			secPreAuthModeNo : secPreAuthMode,
			insuranceCategoryId: this.orderInsuranceCategoryId,
			condDoctorName:   	conductingDoctorName,
			condDoctorId: conductingDoctorId,
			subType : form.doctorCharge.value,
			presDatePart : form.presdate.value,
			priorAuthReq : this.preAuthReq,
			ratePlan	 : this.orgId,
			toothNumberReq : this.toothNumberReq,
			toothNumber:toothNumber,
			urgent: form.urgent.checked ?'S':'R',
			multiVisitPackage : multiVisitPackage,
			packObId : packObId,
			packageId : orderItemId,
			itemServiceGroupId : this.itemServiceGroupId,
			mandateTestAdditionalInfo : mandate_test_additional_info,
			additionalTestInfo : additional_info_reqts
		}

		if (itemType == 'Direct Charge' && itemId == 'BIDIS') {
			order.rateDetails = [{chargeGroup: 'DIS', chargeHead: 'BIDIS',
				actDescription: form.discDescription.value, actRate: 0, actQuantity: 1, actUnit: "",
				discount: form.discAmount.value, allowDiscount: true, discauthId: form.discauthId.value,
				serviceSubGroupId: this.directChargeSubGroupId
			}];
			return this.addOrder(order, this.packageDetails == null);

		} else if (itemType == 'Direct Charge' && itemId == 'MISOTC') {
			order.rateDetails = [{chargeGroup: 'OTC', chargeHead: 'MISOTC',
				actDescription: form.miscDescription.value, actRate: form.miscRate.value,
				actQuantity: form.miscQty.value, actUnit: "", discount: 0, allowDiscount: true,
				allowRateVariation: true,
				serviceSubGroupId: this.directChargeSubGroupId
			}];
			return this.addOrder(order, this.packageDetails == null);

		} else if (itemType == 'Direct Charge' && itemId == 'CSTAX') {
			order.rateDetails = [{chargeGroup: 'TAX', chargeHead: 'CSTAX',
				actDescription: '', actRate: 0,
				actQuantity: 1, actUnit: "", discount: 0,
				serviceSubGroupId: this.directChargeSubGroupId
			}];
			return this.addOrder(order, true);

		} else if (itemType == 'Direct Charge' && itemId == 'BSTAX') {
			order.rateDetails = [{chargeGroup: 'TAX', chargeHead: 'BSTAX',
				actDescription: '', actRate: 0,
				actQuantity: 1, actUnit: "", discount: 0,
				serviceSubGroupId: this.directChargeSubGroupId
			}];
			return this.addOrder(order, true);
		}
		this.getCharge(order, true);
	}

	this.clearOperAnaesthesiaDetailsTable = function() {
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
		this.addOpAnaesDetailsFieldSet.style.display  = 'none';
	}


	this.clearFields = function(start) {
		var form = document.orderDialogForm;
		// set some defaults
		if (start) {
			// only on start, do some extra clearing.
			form.prescribing_doctor.value = this.prescDoctorName;
			form.prescribing_doctorId.value = this.prescDoctorId;
			this.presDoctorAutoComp._bItemSelected = true;
		} else {
			// else leave the previous value.
		}

		form.presdate.value = formatDate(gServerNow);
		form.prestime.value = formatTime(gServerNow);

		if (this.itemsInput)		// may not have been initialized if ordering from prescriptions
			this.itemsInput.value = "";
		document.getElementById('orderDialogItemType').textContent = '';
		this.orderItemType = 'Unknown';

		form.remarks.value = "";

		document.getElementById('addInfoDoc').style.display = 'none';
		document.getElementById('addInfoOtDoc').style.display = 'none';
		document.getElementById('addInfoMeal').style.display = 'none';
		document.getElementById('addInfoUnits').style.display = '';

		form.doctorCharge.selectedIndex = 0;
		form.otDoctorCharge.selectedIndex = 0;
		form.mealTiming.selectedIndex = 0;
		form.units.selectedIndex = 0;

		form.units.disabled = true;

		form.quantity.value = "";
		form.fromDate.value = "";
		form.fromTime.value = "";
		form.toDate.value = "";
		form.toTime.value = "";

		form.quantity.disabled = true;
		form.fromDate.disabled = true;
		form.fromTime.disabled = true;
		form.toDate.disabled = true;
		form.toTime.disabled = true;

		form.surgeon.value = "";
		form.surgeonId.value = "";
		form.anaesthetist.value = "";
		form.anaesthetistId.value = "";
		form.theatre.selectedIndex = 0;

		form.discDescription.value = "";
		form.discPercent.value = "";
		form.discAmount.value = "";
		form.discauth.value = "";
		form.discauthId.value = "";

		form.miscDescription.value = "";
		form.miscRate.value = "";
		form.miscQty.value = "";

		form.urgent.disabled = true;
		form.urgent.checked = false;

		if (gPrescDocRequired == 'Y' && (this.prescDoctorName == null || this.prescDoctorName == '')) {
			form.prescribing_doctor.focus();
		}else if(this.forceSubGroupSelection == 'Y'){
			form.service_group_id.focus();
			setSelectedIndex(form.service_group_id,"N");
			setSelectedIndex(form.service_sub_group_id,"N");
		} else {
			if(this.itemsInput) this.itemsInput.focus();
		}
		setSelectedIndex(form.service_group_id,"");
		setSelectedIndex(form.service_sub_group_id,"");
		setAutoCompleteFilterValue();
		this.packageDetails = null;
		document.getElementById("itemPrvInfo").textContent = '';
		document.getElementById("itemPrice").textContent = '';
		form.prior_auth_id.value = '' ;
		form.sec_prior_auth_id.value = '';
		form.prior_auth_mode_id.value = '';
		form.sec_prior_auth_mode_id.value = '';
		form.conducting_doctor.value = '';
		form.conducting_doctorId.value = '';
		form.d_tooth_number.value = '';
		document.getElementById('dToothNumberDiv').textContent = '';
		document.getElementById('dToothNumBtnDiv').style.display = 'none';
		document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';

		if(this.multiVisitPackage) {
			var table = document.getElementById('multiVisitPackageDetailsTable');
			var tabLen = table.rows.length -1;
			if(tabLen > 1) {
				for(var i=0;i<tabLen;i++) {
					if(document.getElementById('select_item'+i).checked)
						document.getElementById('select_item'+i).checked = false;
					if(!empty(document.getElementById('order_qty'+i).value))
						document.getElementById('order_qty'+i).value = "";
				}
			}
		}
	}

	/*
	 * Private static functions (no "this" required)
	 */
	function subscribeEscKeyEvent(dialog) {
		var kl = new YAHOO.util.KeyListener(document, { keys:27 },
				{ fn:dialog.cancel, scope:dialog, correctScope:true } );
		dialog.cfg.setProperty("keylisteners", kl);
	}

	this.enableButtons = function(addOrNext, previousEnabled) {
		var addEnabled = (addOrNext == 'add');
		var nextEnabled = !addEnabled;
		document.orderDialogForm.orderDialogAdd.disabled = !addEnabled;
		document.orderDialogForm.orderDialogNext.disabled = !nextEnabled;
		document.orderDialogForm.orderDialogPrevious.disabled = !previousEnabled;
	}

	this.showAddDialog = function() {
		this.currentFieldSet = this.mainFieldSet;
		this.mainButtonsForItemType();

		this.currentFieldSet.style.display = 'block';
		if (this.forceSubGroupSelection == 'Y') {
			document.getElementById("service_group_id").focus();
		} else {
			if(this.itemsInput) this.itemsInput.focus();
		}
		if (document.forms[0].name == 'packagemasterform') {
			disableFields();
		}
			document.getElementById('pkg_details_button').style.display = 'none';
	}

	this.showMultiVisitPackageDialog = function() {
		this.currentFieldSet = this.multiVisitPackageFieldSet;
		var packageItemDetails = this.packageDetails;
		var packCompQtyDet = this.multiVisitPackageComponentQtyDetails;
		var multiVisitPkgItemsTable = document.getElementById("multiVisitPackageDetailsTable");
		if (multiVisitPkgItemsTable.rows.length == 1)
			this.createTableRows(packageItemDetails,packCompQtyDet);

		if ( (this.docVisitsInPack != null && this.docVisitsInPack.length > 0) || this.pkgHasOp
				|| (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0)) {
			this.enableButtons('next', true);
		} else {
			this.enableButtons('add', true);
		}
		this.currentFieldSet.style.display = 'block';
	}


	this.showOpDialog = function() {
		this.currentFieldSet = this.opFieldSet;
		if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0) {
			this.enableButtons('next', true);
		} else if ( (this.docVisitsInPack != null && this.docVisitsInPack.length > 0) &&
			 		(this.orderItemType == 'Package' || this.orderItemType == 'Order Sets')) {
			this.enableButtons('next', true);
		} else {
			this.enableButtons('add', true);
		}

		if (this.fixedOtCharges == 'Y') {
			document.orderDialogForm.theatre.disabled = true;
		} else {
			document.orderDialogForm.theatre.disabled = false;
		}
		this.currentFieldSet.style.display = 'block';
		this.addOpAnaesDetailsFieldSet.style.display = 'block';
		document.orderDialogForm.surgeon.focus();
		document.orderDialogForm.anes_start_date.value = empty(form.fromDate.value) ? formatDate(getServerDate(),'ddmmyyyy','-') :form.fromDate.value;
		document.orderDialogForm.anes_end_date.value = empty(form.toDate.value) ? formatDate(getServerDate(),'ddmmyyyy','-') :form.toDate.value;
		document.orderDialogForm.anes_start_time.value = empty(form.fromTime.value) ? formatTime(getServerDate()) : form.fromTime.value;
		document.orderDialogForm.anes_end_time.value = empty(form.toTime.value) ? formatTime(getServerDate()) : form.toTime.value;
	}

	this.showDocVisitDialog = function() {
		this.currentFieldSet = this.docVisitFieldSet;
		this.enableButtons('add', true);
		this.initDocVisits();

		this.currentFieldSet.style.display = 'block';
		document.getElementById('doctorVisit0').focus();
	}

	this.showConductingDoctorDialg = function() {
		this.currentFieldSet = this.addConductingDocFieldSet;
		if ( (this.docVisitsInPack != null && this.docVisitsInPack.length > 0) &&
			 (this.orderItemType == 'Package' || this.orderItemType == 'Order Sets')) {
			this.enableButtons('next', true);
		} else {
			this.enableButtons('add', true);
		}
		var condDoctorsTable = document.getElementById("conductingDoctors");
		this.initConductingDocs();

		this.currentFieldSet.style.display = 'block';
		document.getElementById('packConductingDoctor0').focus();

	}

	this.showDiscountDialog = function() {
		this.currentFieldSet = this.discountFieldSet;
		this.enableButtons('add', true);
		this.currentFieldSet.style.display = 'block';
		document.orderDialogForm.discDescription.focus();
	}

	this.showMiscDialog = function() {
		this.currentFieldSet = this.miscFieldSet;
		this.enableButtons('add', true);
		this.currentFieldSet.style.display = 'block';
		document.orderDialogForm.miscDescription.focus();
	}

	this.createTableRows = function(packageItemDetails,packCompQtyDetails) {
		var table = document.getElementById('multiVisitPackageDetailsTable');
		var id = table.rows.length -1;

		for (var i=1;i<table.rows.length;) {
			table.deleteRow(i);
		}
		for(var i=0;i<packageItemDetails.length;i++) {
			var row = table.insertRow(-1);
			var activityId = packageItemDetails[i].activity_id;
			var itemType = packageItemDetails[i].item_type;
			var packObId = packageItemDetails[i].pack_ob_id;
			var activityDesc = packageItemDetails[i].activity_description;
			var displayText = activityDesc;
			var consultationTypeId = packageItemDetails[i].consultation_type_id;
			var len = 35;
			var displayTitle = "";
			if (len != null && len > 0) {
				displayText = activityDesc;

				if (displayText == 'Doctor' || itemType == 'Doctor') {
					var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", consultationTypeId);
					displayText = displayText + " (" + consultationType.consultation_type+ ")";
				}
				displayTitle = displayText;
				if (displayText.length > len) {
					displayText = displayText.substring(0, len-2) + '...';
				}
			}
			var itemTotalQty = packageItemDetails[i].activity_qty;
			var hiddenItemType = '<input type="hidden" name="item_type" id="item_type'+i+'" value="'+itemType+'"/>';
			var hiddenTotalQty = '<input type="hidden" name="total_qty" id="total_qty'+i+'" value="'+itemTotalQty+'"/>';
			var innerCell0 = row.insertCell(-1);
			innerCell0.innerHTML = '<input type="checkbox" name="select_item" id="select_item'+i+'" value="'+packObId+'" onclick="enableDisableButtons();"/>'
			var innercell1 = row.insertCell(-1);
			innercell1.innerHTML = '<label title="'+displayTitle+'">'+displayText+'</label>';
			var innercell2 = row.insertCell(-1);
			innercell2.innerHTML = '<label style="text-wrap: normal">'+itemTotalQty+'</label>';
			var consumedQty = 0;

			for(var j=0;j<packCompQtyDetails.length;j++) {
				if(!empty(consultationTypeId)) {
					if (consultationTypeId == packCompQtyDetails[j].item_id) {
						consumedQty = packCompQtyDetails[j].consumed_qty;
					}
				} else {
					if (activityId == packCompQtyDetails[j].item_id) {
						consumedQty = packCompQtyDetails[j].consumed_qty;
					}
				}
			}

			var itemAvailableQty = itemTotalQty-consumedQty;
			var hiddenAvailableQty = '<input type="hidden" name="available_qty" id="available_qty'+i+'" value="'+itemAvailableQty+'"/>';
			var innercell3 = row.insertCell(-1);
			innercell3.innerHTML = '<label>'+itemAvailableQty+'</label>';
			var innercell4 = row.insertCell(-1);
			var hiddenOrderQty = itemAvailableQty > 0 ? 1 : 0;
			if(itemType == 'Service')
				innercell4.innerHTML = '<input type="text" name="order_qty" id="order_qty'+i+'" style="width:80px;" value="">';
			else
				innercell4.innerHTML = '<label>'+hiddenOrderQty+'</label>'+'<input type="hidden" name="order_qty" id="order_qty'+i+'" value="'+hiddenOrderQty+'"/>';
			var innercell5 = row.insertCell(-1);
			innercell5.innerHTML = '&nbsp;'+hiddenItemType+'&nbsp;'+hiddenAvailableQty;
			innercell5.appendChild(makeHidden('conducting_doc_mandatory_pkg', 'conducting_doc_mandatory_pkg'+i, packageItemDetails[i].conducting_doc_mandatory));

			if(itemAvailableQty == '0')
				document.getElementById('select_item'+i).disabled = true;
			else
				document.getElementById('select_item'+i).disabled = false;

		}
	}
	// for the main field set, buttons enabled/disabled needs to be determined both
	// when clicking previous as well as when the item is selected. So, we have a special
	// function for this.
	this.mainButtonsForItemType = function() {

		if (this.orderItemType == 'Unknown') {
			this.enableButtons('add', false);

		} else if (this.orderItemType == 'Operation') {
			this.enableButtons('next', false);

		} else if (this.orderItemType == 'Direct Charge') {
			// next button available for some types of charges
			if ((this.orderItemId == 'BIDIS') || (this.orderItemId == 'MISOTC')) {
				this.enableButtons('next', false);
			} else {
				this.enableButtons('add', false);
			}

		} else if (this.orderItemType == 'Package' || this.orderItemType == 'Order Sets') {

			if (this.multiVisitPackage) {
				this.enableButtons('next', false);
			} else if (this.pkgHasOp) {
				this.enableButtons('next', false);
			}  else if (this.conductingDoctorsInPack != null && this.conductingDoctorsInPack.length > 0) {
				this.enableButtons('next', false);
			} else if ( this.docVisitsInPack != null && this.docVisitsInPack.length > 0 ) {
				this.enableButtons('next', false);
			} else {
				this.enableButtons('add', false);
			}
		} else {
			this.enableButtons('add', false);
		}
	}

	this.initToothNumberDialog = function() {
		var dialogDiv = document.getElementById("toothNumDialog");
		dialogDiv.style.display = 'block';
		this.toothNumDialog = new YAHOO.widget.Dialog("toothNumDialog",
				{	width:"600px",
					context : ["toothNumDialog", "tr", "tl"],
					visible:false,
					modal:true,
					constraintoviewport:true
				});
		YAHOO.util.Event.addListener('toothNumDialog_ok', 'click', this.updateToothNumbers, this.toothNumDialog, true);
		YAHOO.util.Event.addListener('toothNumDialog_close', 'click', this.cancelToothNumDialog, this.toothNumDialog, true);

		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:this.cancelToothNumDialog,
		                                                scope:this.toothNumDialog,
		                                                correctScope:true } );
		this.toothNumDialog.cfg.setProperty("keylisteners", [escKeyListener]);
		this.toothNumDialog.render();
	}

	this.showToothNumberDialog = function(action, obj) {
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
		this.toothNumDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
		document.getElementById('toothNumberDialogTitle').textContent = tooth_numbering_system == 'U' ?
			getString('js.common.order.toothnum_dialog_title_unv') : getString('js.common.order.toothnum_dialog_title_fdi');
		this.toothNumDialog.show();
		childDialog = this.toothNumDialog;
	}

	this.updateToothNumbers = function() {
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
		childDialog = null;
		this.cancel();
	}

	this.cancelToothNumDialog = function() {
		childDialog = null;
		this.cancel();
	}


	this.orderItemSelectHandler = function(sType, aArgs) {
		var item = aArgs[2];
		this.orderItemId = item.id;
		this.orderItemName = document.orderDialogForm.orderDialogItems.value;
		this.orderItemDeptName = item.department;
		this.orderItemType = item.type;
		this.orderInsuranceCategoryId = item.insurance_category_id;
		this.preAuthReq = item.prior_auth_required;
		this.conductingDoctorReq = item.conducting_doc_mandatory;
		this.conductionRequired = item.conduction_applicable;
		this.resultEntryApplicable = item.results_entry_applicable;
		this.toothNumberReq = item.tooth_num_required;
		this.multiVisitPackage = item.multi_visit_package;
		this.itemServiceGroupId = item.groupid;
		this.mandateTestAdditionalInfo = item.mandate_additional_info;
		this.additional_test_info = !empty(item.additional_info_reqts) && item.additional_info_reqts != undefined ? item.additional_info_reqts:'';
		this.packageDetails = null;
		

		document.getElementById("itemPrvInfo").textContent = '';
		document.getElementById("itemPrice").textContent = '';
		var form = document.orderDialogForm;

		if(item.type == 'Package'){
		 	document.getElementById('pkg_details_button').style.display = 'table-row';
		 	var pkg_id = this.orderItemId;
		 	document.getElementById('pkgid').value = pkg_id;
		}else {
			document.getElementById('pkg_details_button').style.display = 'none';
		}

		/*
		 * Set the type description label
		 */
		var typeDesc = '('+item.type ;
		
		if(item.type == 'Order Sets' && item.department == 'IP') {
					typeDesc = '(Care Plan' + '/' + item.department;
		}
		else if(item.type=='Operation'){
			typeDesc='Surgery/Procedure/ ' + item.department;
			
		}
		else {
			if (item.department != '') {
				typeDesc = typeDesc + '/' + item.department;
			}
		}
		typeDesc = typeDesc + ')';

		document.getElementById("orderDialogItemType").textContent = typeDesc;

		this.mainButtonsForItemType();

		if (item.type == 'Direct Charge') {
			this.directChargeSubGroupId = item.subgrpid;

		} else if (item.type == 'Package' || item.type == 'Order Sets') {
			// Figure out whether there is an operation and/or doctor visits in the package
			// within the package.
			var packageId = item.id;
			// todo: non-applicable items should not appear in orderables list
			if (this.patientType == 'o' && item.department == 'IP' ) {
				showMessage("js.common.order.ip.package.can.not.be.ordered.for.op.patient.string")
				this.itemsInput.value = '';
				this.itemsInput.focus();
				return false;
			}
			var packageComponentDetails = this.getPackageComponents(item.id,mrno,this.multiVisitPackage,item.type);
			multiVisitPackageDetails = packageComponentDetails['packComponentDetails'];
			this.packageDetails = packageComponentDetails['packComponentDetails'];
			this.multiVisitPackageComponentQtyDetails = packageComponentDetails['multVisitPackComponentQtyDetails'];
			var opIdInPackage = 'NO_OP_IN_PACK';
			this.docVisitsInPack = new Array();
			this.conductingDoctorsInPack = new Array();
			this.condDocItemIndexInPack = new Array();
			this.pkgHasOp = false;
			var patientType = this.patientType != '' ? this.patientType : this.ptype;

			for (var i=0; i<this.packageDetails.length; i++) {
				if (this.packageDetails[i].operation_id != null && this.packageDetails[i].operation_id!='') {
					opIdInPackage = this.packageDetails[i].operation_id;
					this.pkgHasOp = true;
				}
				if (!this.multiVisitPackage && this.packageDetails[i].item_type === 'Doctor') {
					if(this.packageDetails[i].consultation_type_id) {
						var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", this.packageDetails[i].consultation_type_id);
						if(patientType === consultationType.patient_type) {
							this.docVisitsInPack.push(this.packageDetails[i]);
						}
					} else {
						this.docVisitsInPack.push(this.packageDetails[i]);
					}
				}
				if (!this.multiVisitPackage && this.packageDetails[i].item_type === 'Department') {
					var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", this.packageDetails[i].consultation_type_id);
					this.packageDetails[i].item_type = 'Doctor';
					if(patientType === consultationType.patient_type) {
						this.docVisitsInPack.push(this.packageDetails[i]);
					}
				}
				if (!this.multiVisitPackage
					&& (this.packageDetails[i].conducting_doc_mandatory)
						&& (this.packageDetails[i].item_type == 'Laboratory'
							|| this.packageDetails[i].item_type == 'Radiology'
							|| this.packageDetails[i].item_type == 'Service')) {
					this.conductingDoctorsInPack.push(this.packageDetails[i]);
					this.condDocItemIndexInPack.push(i);
				}

				if (this.packageDetails[i].item_type == 'Equipment') {
					this.pkgHasEquipment = true;
				}
			}

			if (this.packageDetails.length > 0) {
				form.remarks.value = this.packageDetails[0].description;
			}
		} else if(item.type == 'Equipment') {
			this.getItemDetails(item.id);

		} else if(item.type == 'Doctor' && mrno != undefined && !empty(patientInfo)) {
			var patient = patientInfo.patient;
			var gDocVisits = patientInfo.docVisits;
			var gFollowUpDocVisits = patientInfo.followUpDocVisits;
			var gPatientLastIpVisit = patientInfo.patientLastIpVisit;
			var form = document.orderDialogForm;
			var doctorId = item.id;
			if (isRevisitAfterDischarge(doctorId, gPatientLastIpVisit, gFollowUpDocVisits)) {
				setSelectedIndex(form.doctorCharge, '-4');
				if(gFollowUpDocVisits[0] != undefined){
					document.getElementById("itemPrvInfo").textContent
									= "Previous Visit: "+gFollowUpDocVisits[0].doctor;
				}else{
					document.getElementById("itemPrvInfo").textContent = '';
				}
			} else {
				if (doctorId == null) {
					form.doctorCharge.selectedIndex = 0;

				}else if (gDocVisits == null) {
					setSelectedIndex(form.doctorCharge, '-1');

				} else if (regPref.opRevisitSetting == 'R') {
					if (isRevisit(doctorId, gDocVisits)) {
						setSelectedIndex(form.doctorCharge, '-2');
						document.getElementById("itemPrvInfo").textContent = "Previous Visit: "+gDocVisits[0].doctor;
					} else {
						setSelectedIndex(form.doctorCharge, '-1');
					}
				} else {
					form.doctorCharge.selectedIndex = 0;
				}
			}
		}

		/*
		 * Enable/disable buttons add/next/previous
		 */
		this.mainButtonsForItemType();

		/*
		 * Enable/disable from/to dates on the main dialog
		 */
		form.fromDate.disabled = form.fromTime.disabled = !(
				(item.type == 'Equipment' && document.forms[0].name != 'packagemasterform')
				|| (item.type == 'Operation' && this.fixedOtCharges == 'N')
				||( item.type == 'Bed' && document.forms[0].name != 'packagemasterform')
				||( item.type == 'ICU' && document.forms[0].name != 'packagemasterform')
				|| (item.type == 'Package' && this.pkgHasOp && this.fixedOtCharges == 'N')
				|| (item.type == 'Package' && this.pkgHasEquipment)
				|| (item.type == 'Order Sets' && this.pkgHasOp && this.fixedOtCharges == 'N')
				|| (item.type == 'Order Sets' && this.pkgHasEquipment)
				|| item.type == 'Meal'
				|| ( item.type == 'Doctor' && document.forms[0].name != 'packagemasterform'));

		form.toDate.disabled = form.toTime.disabled = !(
				(item.type == 'Equipment' && document.forms[0].name != 'packagemasterform')
				|| (item.type == 'Operation' && this.fixedOtCharges == 'N')
				||( item.type == 'Bed' && document.forms[0].name != 'packagemasterform')
				||( item.type == 'ICU' && document.forms[0].name != 'packagemasterform')
				|| (item.type == 'Order Sets' && this.pkgHasOp && this.fixedOtCharges == 'N' )
				|| (item.type == 'Order Sets' && this.pkgHasEquipment)
				|| (item.type == 'Package' && this.pkgHasOp && this.fixedOtCharges == 'N' )
				|| (item.type == 'Package' && this.pkgHasEquipment));

		if (!form.fromDate.disabled) {
			form.fromDate.value = formatDate(gServerNow);
			form.fromTime.value = formatTime(gServerNow);
		}
		if (!form.toDate.disabled) {
			form.toDate.value = formatDate(gServerNow);
			// force them to select the time.
		}
		/*
		 * Show and enable quantity OR finalization field:
		 *   Finalization: for Operation and Equipment
		 *   Quantity: for Medicine, Consumable, Other Charge, Implant
		 */
		var label = document.getElementById('addInfo2Label');
		if ((item.type == 'Equipment' || item.type == 'Operation' || this.pkgHasEquipment || this.pkgHasOp || item.type == 'Package'))  {
			// show finalized
			var finalized = getString("js.common.order.finalized");
			label.textContent = finalized+':';
			document.getElementById('addInfoQty').style.display = 'none';
			document.getElementById('addInfoFinalized').style.display = 'block';
			form.finalized.checked = true;		// default to finalized.
			if (!this.allowFinalization) {
				form.finalized.disabled = true;
			} else {
				form.finalized.disabled = false;
			}
			if( document.forms[0].name == 'packagemasterform'){
				label.textContent = 'Quantity:';
				document.getElementById('addInfoQty').style.display = 'block';
				document.getElementById('addInfoFinalized').style.display = 'none';

				form.quantity.disabled = false;
			}
			if (item.type == 'Operation' && displayRatePlanAmounts({ratePlan: this.orgId})) {
				var opCharge = this.getOperationCharge(item.id);
				document.getElementById("itemPrice").textContent =
					" Item Price : " + formatAmountValue(
						(opCharge.surg_asstance_charge-opCharge.surg_asst_discount) +
						(opCharge.surgeon_charge - opCharge.surg_discount) +
						(opCharge.anesthetist_charge -  opCharge.anest_discount));
			}
		} else {
			// show quantity, but disable it unless it is required.
			label.textContent = 'Quantity:';
			document.getElementById('addInfoQty').style.display = 'block';
			document.getElementById('addInfoFinalized').style.display = 'none';

			var qtyEnabled = (item.type == 'Consumable' || item.type == 'Medicine'
				|| item.type == 'Other Charge' || item.type == 'Implant' || item.type == 'Service'
				|| (document.forms[0].name == 'packagemasterform' && (item.type == 'Bed' || item.type == 'ICU')));
			var qtyDefault = '1';

			if (!qtyEnabled && this.allowQtyEdit) {
				// allow pretty much everything to be quantity editable
				qtyEnabled |= (item.type == 'Laboratory'
						|| item.type == 'Radiology' || item.type == 'Meal'
						|| item.type == 'Doctor' || item.type == 'Package');
				qtyDefault = '1'
			}

			if(this.isMultiVisitPackageItem == 'true') {
				form.quantity.disabled = false;
			} else {
				form.quantity.disabled = !qtyEnabled;
			}
			if (qtyEnabled)
				form.quantity.value = qtyDefault;
		}

		/*
		 * Enable/disable prescribing doctor: not available for  packages.
		 */
		var prescDrAvlbl = true;

		if(document.forms[0].name == 'packagemasterform'){
			prescDrAvlbl = false;
			form.prescribing_doctor.value = "";
		}
		form.prescribing_doctor.disabled = !prescDrAvlbl;

		if(item.type == 'Laboratory' || item.type == 'Radiology' || item.type == 'Service') {
			if (gScreenId != 'mas_packages' && this.conductingDoctorReq) {
				form.conducting_doctor.disabled = false;
				if (item.type == 'Laboratory' || item.type == 'Radiology') {
					var dep_lab = item.type == 'Laboratory' ? 'DEP_LAB' : 'DEP_RAD';
					this.initOrderDoctorAutoComplete('conducting_doctor', this.doctorList, onSelectConductingDoc, 'dept_id', dep_lab);
				} else {
					this.initOrderDoctorAutoComplete('conducting_doctor', this.doctorList, onSelectConductingDoc);
				}
			} else {
				form.conducting_doctor.disabled = true;
			}
		} else {
			form.conducting_doctor.disabled = true;
		}
		document.getElementById('dToothNumberDiv').textContent = '';
		if (this.toothNumberEnabled && item.type=='Service' && item.tooth_num_required == 'Y') {
			document.getElementById('dToothNumBtnDiv').style.display = 'block';
			document.getElementById('dToothNumDsblBtnDiv').style.display = 'none';
		} else {
			document.getElementById('dToothNumBtnDiv').style.display = 'none';
			document.getElementById('dToothNumDsblBtnDiv').style.display = 'block';
		}

		if(!this.urgentDisabled && (item.type == 'Laboratory' || item.type == 'Radiology')) {
			form.urgent.disabled = false;
		} else {
			form.urgent.disabled = true;
		}

		/*
		 * show/hide additional info
		 */
		label = document.getElementById("addInfoLabel");
		if (item.type == 'Doctor') {
			if (this.operItems) {
				label.textContent = 'OT Doctor Type:';
				document.getElementById('addInfoDoc').style.display = 'none';
				document.getElementById('addInfoOtDoc').style.display = '';
				form.otDoctorCharge.selectedIndex = 0;
			} else {
				label.textContent = 'Consultation Type:';
				document.getElementById('addInfoDoc').style.display = '';
				document.getElementById('addInfoOtDoc').style.display = 'none';

				if(form.doctorCharge.selectedIndex == 0){
					if(this.patientType == 'i'){
						setSelectedIndex(form.doctorCharge, '-3');
					}else if(this.patientType == 'o'){
						setSelectedIndex(form.doctorCharge, '-1');
					}
				}
			}

			document.getElementById('addInfoMeal').style.display = 'none';
			document.getElementById('addInfoUnits').style.display = 'none';

		} else if (item.type == 'Meal') {
			label.textContent = 'Meal Timing:';
			document.getElementById('addInfoOtDoc').style.display = 'none';
			document.getElementById('addInfoDoc').style.display = 'none';
			document.getElementById('addInfoMeal').style.display = '';
			document.getElementById('addInfoUnits').style.display = 'none';
			form.mealTiming.selectedIndex = 0;

		}else {
			document.getElementById('addInfoOtDoc').style.display = 'none';
			document.getElementById('addInfoDoc').style.display = 'none';
			document.getElementById('addInfoMeal').style.display = 'none';
			document.getElementById('addInfoUnits').style.display = '';
			// we show the units dropdown for all other cases, but disable it except for some.
			if ( item.type == 'Equipment' || item.type == 'Operation' ||
				((item.type == 'Package' || item.type == 'Order Sets') && this.pkgHasOp )) {
				if(document.forms[0].name != 'packagemasterform' ){
						form.units.disabled = false;
						form.units.selectedIndex = 0;
						if( item.type != 'Equipment'){
							if(this.fixedOtCharges == 'Y'){
								form.units.disabled = true;
							}
							label.textContent = 'Theatre/Room Charge Type (Hrly/Daily)';
						}
				} else {
					label.textContent = 'Charge Type (Hrly/Daily):';
					form.units.disabled = false;
					form.units.selectedIndex = 0;
				}
				if (item.type == 'Equipment') {
					label.textContent = 'Charge Type (Hrly/Daily)';
					document.orderDialogForm.units.value = this.itemDetails.CHARGE_BASIS;
					if(this.itemDetails.CHARGE_BASIS != 'B'){
						document.orderDialogForm.units.disabled = true;
					}else{
						document.orderDialogForm.units.disabled = false;
					}
				}

			} else {
				label.textContent = 'Charge Type:';
				form.units.disabled = true;
			}
		}
	}

	this.initOrderItemAutoComplete = function() {

		if (this.itemAutoComp != null)
			this.itemAutoComp.destroy();
		// getOrderableItems JSON result
		
		var ds;
		var orderJSONKey = "result";
		ds = new YAHOO.util.LocalDataSource(this.itemList);
		if(typeof enabledOrderableItemApi !== 'undefined' && enabledOrderableItemApi === true) {
		   orderJSONKey = "orderable_items";
		   ds = new YAHOO.util.DataSource(orderableItemUrl);
		}
		 
		ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
		ds.responseSchema = { resultsList : orderJSONKey,
			fields: [ {key: "name"}, {key: "code"}, {key: "type"}, {key: "id"}, {key: "department"},
			          {key: "groupid"},{key: "subgrpid"},{key: "conduction_applicable"},
			          {key: "prior_auth_required"},{key:"insurance_category_id"},{key:"conducting_doc_mandatory"},
			          {key: "results_entry_applicable"},{key: "tooth_num_required"},{key: "multi_visit_package"},
			          {key: "mandate_additional_info"}, {key: "additional_info_reqts"}],
			numMatchFields: 2,
			mainFilterValue: document.orderDialogForm.service_group_id.value,
			subFilterValue : document.orderDialogForm.service_sub_group_id.value,
			mainFilterCol : "groupid",
			subFilterCol :  "subgrpid"// insta specific: to say name and code are to be matched.
		};

		this.itemAutoComp = new YAHOO.widget.AutoComplete('orderDialogItems', 'orderDialogItemsAcDropdown',	ds);
		this.itemAutoComp.typeAhead = false;		// needed because we do any word beginning match.
		this.itemAutoComp.useShadow = true;
		this.itemAutoComp.allowBrowserAutocomplete = false;
		this.itemAutoComp.maxResultsDisplayed = 50;
		this.itemAutoComp.autoHighlight = true;
		this.itemAutoComp.resultTypeList = false;
		this.itemAutoComp.minQueryLength = 1;
		this.itemAutoComp.forceSelection = true;

		if (typeof enabledOrderableItemApi !== 'undefined' && enabledOrderableItemApi === true) {
			this.itemAutoComp.generateRequest = function(sQuery) {
				let serviceGrpId = document.orderDialogForm.service_group_id.value;
				let serviceSubGrpId = document.orderDialogForm.service_sub_group_id.value;
				return "&search_id=" + sQuery + "&service_group_id=" + serviceGrpId + "&service_sub_group_id=" + serviceSubGrpId;
			};
		}
		
		this.itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;

		this.itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
			var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
			// now, add our extra info reg. department to the highlighted value and return that.
			if (oResultData.department != null && oResultData.department != '') {
				if(oResultData.type == 'Order Sets' && oResultData.department == 'IP') {
					highlightedValue += "(Care Plan/IP)";
				}
				else if(oResultData.type=='Operation'){
					highlightedValue += "(Surgery/Procedure/ " +oResultData.department;
				}
				else {
					highlightedValue += " ("+oResultData.type+ "/" +oResultData.department+ ")";
				}
			}
			else
				highlightedValue += " ("+oResultData.type+")";
			return highlightedValue ;
		}

		this.itemAutoComp.itemSelectEvent.subscribe(this.orderItemSelectHandler, this, true);

		this.itemAutoComp.unmatchedItemSelectEvent.subscribe(this.itemClear, this, true);
		this.itemAutoComp.textboxKeyEvent.subscribe(this.itemClear, this, true);
	}

	this.itemClear = function() {
		this.orderItemId = null;
		document.getElementById("orderDialogItemType").textContent = '';
		document.getElementById("orderDialogItemType").textContent = '';
		document.getElementById('addInfoDoc').style.display = 'none';
		document.getElementById('addInfoOtDoc').style.display = 'none';
		document.getElementById('addInfoMeal').style.display = 'none';
		document.getElementById('addInfoUnits').style.display = '';
		document.getElementById("orderDialogNext").disabled = true;
		document.getElementById("orderDialogAdd").disabled = false;

		// clearing of tables is required in the following case.
		// selected package, which contains the doctor visit, conducting doctor, enters all the details
		// using next button. after entering all the details, he traverses back to first dialog using previous button,
		// now in the main dialog if he changes the item, we should clear the details which are entered.
		clearDoctorVisitsOfPack();
		clearCondDoctorsOfPack();
		hideAllSubDialogBoxes();
		this.clearOperAnaesthesiaDetailsTable();
		clearMultiVisitTable();

	}

	this.initOrderOpItemAutoComplete = function() {
		// getOrderableItems JSON result

		if (this.operItemList == null)
			return;

		if (this.opItemAutoComp != null)
			this.opItemAutoComp.destroy();

		var ds = new YAHOO.util.LocalDataSource(this.operItemList);
		ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;

		ds.responseSchema = { resultsList : "result",
			fields: [ {key: "name"}, {key: "type"}, {key: "id"}, {key: "department"},{key: "groupid"},
					  {key: "subgrpid"},{key: "conduction_applicable"},{key: "prior_auth_required"},
					  {key: "insurance_category_id"},{key:"conducting_doc_mandatory"},
					  {key: "results_entry_applicable"}],
			mainFilterValue: document.orderDialogForm.service_group_id.value,
			subFilterValue : document.orderDialogForm.service_sub_group_id.value,
			mainFilterCol : "groupid",
			subFilterCol :  "subgrpid"// insta specific: to say name and code are to be matched.
		};

		this.opItemAutoComp = new YAHOO.widget.AutoComplete('orderDialogOpItems', 'orderDialogOpItemsAcDropdown', ds);
		this.opItemAutoComp.typeAhead = true;
		this.opItemAutoComp.useShadow = true;
		this.opItemAutoComp.allowBrowserAutocomplete = false;
		this.opItemAutoComp.minQueryLength = 0;
		this.opItemAutoComp.maxResultsDisplayed = 20;
		this.opItemAutoComp.autoHighlight = true;
		this.opItemAutoComp.resultTypeList = false;
		this.opItemAutoComp.minQueryLength = 1;

		this.opItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;

		this.opItemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
			var item =  highlight(oResultData.name, oResultData.name + " " + oResultData.type
					+" " + oResultData.department) ;
			item += " ("+oResultData.type+ "/" +oResultData.department+ ")";
			return item;
		}

		this.opItemAutoComp.itemSelectEvent.subscribe(this.orderItemSelectHandler, this, true);

		this.opItemAutoComp.unmatchedItemSelectEvent.subscribe(this.itemClear, this, true);
		this.opItemAutoComp.textboxKeyEvent.subscribe(this.itemClear, this, true);
	}

	var autocompAr = new Array();
	this.initOrderDoctorAutoComplete = function(field, list, selectHandler, filterCol, filterValue) {
		if (!empty(autocompAr[field])){
			autocompAr[field].destroy();
		}

		var filteredList = list;
		if(list !=  null && filterCol != undefined){
			filteredList = { "doctors": filterList(list.doctors, filterCol, filterValue) };
		}
		var ds = new YAHOO.util.LocalDataSource(filteredList,{ queryMatchContains : true });
		ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
		ds.responseSchema = { resultsList : "doctors",
			  fields: [ {key : "doctor_name"}, {key: "doctor_id"} ]
		};

		var autoComp = new YAHOO.widget.AutoComplete(field, field+'AcDropdown', ds);

		autoComp.typeAhead = false;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = true;
		autoComp.forceSelection = true;
		autoComp.animVert = false;
		autoComp.useIFrame = true;
		autoComp.formatResult = Insta.autoHighlight;

		autoComp.itemSelectEvent.subscribe(selectHandler);
		autocompAr[field] = autoComp;

		return autoComp;
	}

	function initDiscAuthAutoComplete(field, list, selectHandler, clearHandler) {

		var ds = new YAHOO.util.LocalDataSource(list,{ queryMatchContains : true });
		ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
		ds.responseSchema = { resultsList : "discount_authorizer",
			  fields: [ {key : "disc_auth_name"}, {key: "disc_auth_id"} ]
		};

		var autoComp = new YAHOO.widget.AutoComplete(field, field+'AcDropdown', ds);

		autoComp.typeAhead = false;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = true;
		autoComp.forceSelection = true;
		autoComp.animVert = false;
		autoComp.useIFrame = true;
		autoComp.formatResult = Insta.autoHighlight;
		autoComp.selectionEnforceEvent.subscribe(clearHandler);
		autoComp.itemSelectEvent.subscribe(selectHandler);
		return autoComp;
	}

	this.getOperationCharge = function(itemId) {
		var url = cpath+"/master/orderItems.do?method=getOperationCharge&id="+encodeURIComponent(itemId)+
		"&type="+this.orderItemType+
		'&bedType='+this.bedType +
		'&orgId='+this.orgId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
				return eval('('+reqObject.responseText+')');
			}
		}
		return null;
	}

	// todo: convert to async call
	this.getPackageComponents = function(packId,mrNo,isMultiVisitPack, itemType = '') {
		var url;
		if(itemType === 'Order Sets') {
			url = cpath + '/patients/orders/getpackagecontents.json?packageId='+packId+'&mr_no='+mrNo+'&multi_visit_package='+isMultiVisitPack;
		} else {
			url = cpath + '/master/orderItems.do?method=getPackageComponents&packageId='+packId+'&mr_no='+mrNo+'&multi_visit_package='+isMultiVisitPack;
		}
		var reqObject = newXMLHttpRequest();
		reqObject.open("GET", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
				return eval('('+reqObject.responseText+')');
			}
		}
		return null;
	}

	this.getItemDetails = function(itemId) {
		var url = cpath+"/master/orderItems.do?method=getItemDetails&id="+itemId+"&type="+this.orderItemType;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ( (reqObject.status == 200) && (reqObject.responseText != null ) ) {
				this.itemDetails = eval('('+reqObject.responseText+')');
			}
		}
		return null;
	}

	/*
	 * Make the ajax call to retrieve the charges with all the required parameters
	 * from the order, chain the response to add the order.
	 * Note that this can also be called while adding prescriptions, ie, should not
	 * rely on anything that has been set in the order dialog (form).
	 */
	this.getCharge = function(order, restartDialog) {
		var orderFirstOfCategory = "true";
		if( document.getElementById(order.addTo) != null) {
			for(var i=0; i< document.getElementById(order.addTo).rows.length; i++) {
				if (getElementByName(document.getElementById(order.addTo).rows[i], "orderCategory") != null) {
					if(this.orderInsuranceCategoryId == getElementByName(document.getElementById(order.addTo).rows[i], "orderCategory").value){
						orderFirstOfCategory = "false";
						break;
					}
				}
			}
		}

		var finalized = order.finalized ? 'F' : 'N';
		var url = cpath + '/master/orderItems.do?method=getItemCharges'+
			'&type='+order.itemType + '&id='+encodeURIComponent(order.itemId) +
			'&bedType='+encodeURIComponent(this.bedType) + '&orgId='+this.orgId +
			'&insurance='+this.isInsurance + '&planId='+this.insurancePlanId +
			'&fromDate='+order.from + '&toDate='+order.to + '&quantity='+ order.quantity +
			'&units='+order.units + '&chargeType='+order.chargeType +
			'&ot='+order.theatreId + '&surgeon='+order.surgeonId + '&anaesthetist='+order.anaesthetistId +
			'&visitType='+this.patientType +'&operationId='+order.operationId +'&finalized='+finalized +
			'&patientId='+order.patientId+'&firstOfCategory='+orderFirstOfCategory +
			'&billNo='+this.billNo+'&multi_visit_package='+order.multiVisitPackage+'&pack_ob_id='+order.packObId +
			'&package_id='+order.packageId;

		if(order.anaeTypeIds) {
			for (var i=0;i<order.anaeTypeIds.length;i++) {
				if(!empty(order.anaeTypeIds[i].value)) {
					url = url+'&anesthesiaType='+order.anaeTypeIds[i].value;
					url = url+'&anesthesiaTypeFromDate='+order.anaTypeFromDates[i].value;
					url = url+'&anesthesiaTypeToDate='+order.anaTypeToDates[i].value;
					url = url+'&anesthesiaTypeFromTime='+order.anaTypeFromTimes[i].value;
					url = url+'&anesthesiaTypeToTime='+order.anaTypeToTimes[i].value;
				}
			}
		}

		if (this.getChargeRequest != null && YAHOO.lang.isArray(this.getChargeRequest)) {
			// getChargeRequest is an array, if template package
			this.getChargeRequest.push(YAHOO.util.Connect.asyncRequest('GET', url,
					{ 	success: this.onGetCharge,
						failure: this.onGetChargeFailure,
						argument: [this, order, restartDialog]}
			));
		} else {
			this.getChargeRequest = YAHOO.util.Connect.asyncRequest('GET', url,
				{	success: this.onGetCharge,
					failure: this.onGetChargeFailure,
					argument: [this, order, restartDialog]}
			);
		}
	}

	/*
	 * getCharge response handler.
	 * Note: "this" is not the add order dialog, so use orderDlg variable
	 * when accessing the order dialog functions/variables.
	 */
	this.onGetCharge = function(response) {
		if (response.responseText != undefined) {
			var orderDlg = response.argument[0];
			var order = response.argument[1];
			var wantRestartDialog = response.argument[2];

			order.rateDetails = eval('(' + response.responseText + ')');

			var isLastOrder = true;
			if (YAHOO.lang.isArray(orderDlg.getChargeRequest)) {
				orderDlg.getChargeRequest.pop();
				if (orderDlg.getChargeRequest.length > 0)
					isLastOrder = false;
			}

			orderDlg.addOrder(order, (wantRestartDialog && isLastOrder));
		}
	}

	this.addOrder = function(order, restartDialog) {
		var success = this.addHandler(order, this.argument);
		if (success && restartDialog) {
			this.clearFields();
			if (this.currentFieldSet && this.currentFieldSet != this.mainFieldSet) {
				this.currentFieldSet.style.display = 'none';
			}
			this.showAddDialog();
			this.addDialog.align("tr", "tl");
			document.getElementById("orderDialogAddDialog").scrollIntoView(false);
		}
	}

	this.onGetChargeFailure = function(response) {
		// do nothing.
	}

	function onSelectPrescribingDoc(sType, aArgs) {
		var doctor = aArgs[2];
		document.orderDialogForm.prescribing_doctorId.value = doctor[1];
	}

	function onSelectSurgeon(sType, aArgs) {
		var doctor = aArgs[2];
		document.orderDialogForm.surgeonId.value = doctor[1];
	}

	function onSelectAnaesthetist(sType, aArgs) {
		var doctor = aArgs[2];
		document.orderDialogForm.anaesthetistId.value = doctor[1];
	}

	function onSelectDiscAuth(sType, aArgs) {
		var discountAuth = aArgs[2];
		document.orderDialogForm.discauthId.value = discountAuth[1];
	}

	function onSelectConductingDoc(sType, aArgs) {
		var doctor = aArgs[2];
		document.orderDialogForm.conducting_doctorId.value = doctor[1];
	}

	function clearDiscAuth() {
		document.orderDialogForm.discauthId.value = '';
	}

	this.onChangeMealTiming = function() {
		var timing = document.orderDialogForm.mealTiming.value;
		if (timing != 'Spl') {
			document.orderDialogForm.fromTime.disabled = true;
			document.orderDialogForm.fromTime.value = "";
		} else {
			document.orderDialogForm.fromTime.disabled = false;
		}
	}

	this.onChangeDiscountPercent = function() {
		var percentage = document.orderDialogForm.discPercent.value;
		if (percentage != '') {
			if (isAmount(percentage)) {
				var per = getAmount(percentage);
				// todo: hardcoded reference to billing.js
				var billAmt = totAmtPaise;
				var totalTaxAmt = totTaxPaise;
				document.orderDialogForm.discAmount.value = formatAmountPaise(per*(billAmt+totTaxPaise)/100);
			} else {
				showMessage("js.common.order.please.enter.number.for.discount.percentage.string");
				document.orderDialogForm.discAmount.value = "";
				document.orderDialogForm.discAmount.focus();
			}
		}
	}

	function clearDoctorVisitsOfPack() {
		var docVisitTable = document.getElementById("docVisits");
		var len = docVisitTable.rows.length;
		for (var i=0; i<len; i++) {
			docVisitTable.deleteRow(0);
		}
	}

	function clearCondDoctorsOfPack() {
		var condDocTable = document.getElementById("conductingDoctors");
		for (var i=1; i<condDocTable.rows.length; ) {
			condDocTable.deleteRow(i);
		}
	}

	function clearMultiVisitTable() {
		var mvTable = document.getElementById("multiVisitPackageDetailsTable");
		for (var i=1; i<mvTable.rows.length; ) {
			mvTable.deleteRow(i);
		}
	}

	function hideAllSubDialogBoxes(){
	   /* Setting all field sets other than main field set to display none
        * Since start will open only main addItemFieldSet
        * In case if operation is selected and added addOperationFieldSet might have set to block
        */
		document.getElementById('addOperationFieldSet').style.display = 'none';
		document.getElementById('addDocVisitFieldSet').style.display = 'none';
		document.getElementById('addDiscountsFieldSet').style.display = 'none';
		document.getElementById('addMisFieldSet').style.display = 'none';
		document.getElementById('addMultivisitPackageFieldSet').style.display = 'none';
		document.getElementById('addOpAnaesDetailsFieldSet').style.display = 'none';
		document.getElementById('addConductingDocFieldSet').style.display = 'none';
	}

	var docVisitsOfPackage = new Array();
	
	this.filterDoctorsByDepartment = function (department_id) {
		filteredList = { "doctors": filterList(this.doctorList.doctors, "dept_id", department_id) };
		return filteredList;
	}

	this.initDocVisits = function() {

		var docVisitTable = document.getElementById("docVisits");
		for (var i=0; i<docVisitTable.rows.length;) {
			docVisitTable.deleteRow(i);
		}
		for (var i=0; i<this.docVisitsInPack.length; i++ ) {

			var docRow = docVisitTable.insertRow(-1);

			makeTextCell(docRow, "Doctor:", "formlabel");
			var doctorVisit = "doctorVisit"+i;
			var doctorVisitContainer = doctorVisit + "AcDropdown";
			var presdate = 'presdate' + i;
			var prestime = 'prestime' + i;
			var cell = docRow.insertCell(-1);
			cell.setAttribute("class", "yui-skin-sam");
			var disabled = "";
			var doctorsList = this.doctorList;
			var doctorNameVal = '';
			var doctorIdVal = '';
			if (this.docVisitsInPack[i].activity_id != 'Doctor') {
				disabled='disabled'
				doctorNameVal = this.docVisitsInPack[i].activity_description;
				doctorIdVal = this.docVisitsInPack[i].activity_id
			}
			
			if (this.docVisitsInPack[i].activity_id === 'Doctor' ) {
				if (this.docVisitsInPack[i].doctor_id) {
					disabled='disabled';
					doctorNameVal = this.docVisitsInPack[i].activity_description;
					doctorIdVal = this.docVisitsInPack[i].doctor_id;
				}
				if (this.docVisitsInPack[i].dept_id) {
					doctorsList = this.filterDoctorsByDepartment(this.docVisitsInPack[i].dept_id);
					if(doctorsList.doctors.length === 0) {
						doctorsList = this.doctorList;
					}
				}
			}
			
			

			cell.innerHTML = '<div><input id="'+ doctorVisit +'" name="'+ doctorVisit +'" type="text" value="'+doctorNameVal+'" '+disabled+' /><div id="'+ doctorVisitContainer +'" ></div></div>';

			makeTextCell(docRow, "Visit Date/Time:", "formlabel");
			var cell = docRow.insertCell(-1);
			cell.innerHTML = '<div style="float: left">' + getDateWidget(presdate, presdate,
					gServerNow, "", null, true, true, null, cpath) + '</div>';
			makePopupCalendar(presdate, "topleft");

			cell.appendChild(makeTextInput(prestime, prestime, "timefield", 5, formatTime(gServerNow)));
			cell.appendChild(makeHidden("doctorHeadId", "doctorHeadId"+i, this.docVisitsInPack[i].consultation_type_id));
			cell.appendChild(makeHidden("vDoctorId", "vDoctorId"+i, doctorIdVal));

			var docRow = docVisitTable.insertRow(-1);
			var cell = docRow.insertCell(-1);
			cell.setAttribute('class', 'formlabel');
			cell.setAttribute('style', 'height: 0px; padding: 0px');
			var cell = docRow.insertCell(-1);
			cell.setAttribute('style', 'height: 15px; padding: 0px; valign: top');
			var consultationType = findInList(allDoctorConsultationTypes, "consultation_type_id", this.docVisitsInPack[i].consultation_type_id);
			cell.appendChild(makeLabel(null, '(' + consultationType.consultation_type + ')' ) );

			this.initOrderDoctorAutoComplete(doctorVisit, doctorsList, function(sType, aArgs) {
				var index = (aArgs[0].getInputEl().getAttribute("id")).replace("doctorVisit", "");
					document.getElementById("vDoctorId"+index).value = aArgs[2][1];
					});

		}
	}

	this.initConductingDocs = function() {
		var condDoctorTable = document.getElementById("conductingDoctors");
		var addHeader = true;
		for (var i=1; i<condDoctorTable.rows.length;) {
			condDoctorTable.deleteRow(i);
		}
		for (var i=0; i<this.conductingDoctorsInPack.length; i++ ) {
			var docRow = condDoctorTable.insertRow(-1);

			var cell = docRow.insertCell(-1);
			cell.appendChild(makeLabel(null, this.conductingDoctorsInPack[i].item_type));

			var cell = docRow.insertCell(-1);
			var item_name = this.conductingDoctorsInPack[i].item_name;
			var item_name_label = makeLabel(null, this.conductingDoctorsInPack[i].item_name);
			cell.appendChild(item_name_label);
			setNodeText(cell, item_name, 40);
			cell.appendChild(makeHidden("packageItemType", "packageItemType"+i, this.conductingDoctorsInPack[i].item_type));
			cell.appendChild(makeHidden("packageName", "packageName"+i, this.conductingDoctorsInPack[i].package_name))
			cell.appendChild(makeHidden("packageItemName", "packageItemName"+i, this.conductingDoctorsInPack[i].item_name));
			cell.appendChild(makeHidden("packageActId", "packageActId"+i, this.conductingDoctorsInPack[i].activity_id));
			// this is required to maintain the association to support the duplicate items.
			cell.appendChild(makeHidden("packageActivityIndex", "packageActivityIndex"+i, this.condDocItemIndexInPack[i]+''));

			var condDoctor = "packConductingDoctor"+i;
			var condCoctorContainer = condDoctor + "AcDropdown";
			var presdate = 'presdate' + i;
			var prestime = 'prestime' + i;
			var cell = docRow.insertCell(-1);
			cell.setAttribute("class", "yui-skin-sam");
			cell.innerHTML = '<div><input id="'+ condDoctor +'" name="packConductingDoctor" type="text" /><div id="'+ condCoctorContainer +'" ></div></div>';
			cell.appendChild(makeHidden("packCondDoctorId", "packCondDoctorId"+i, null ));

			var itemType = this.conductingDoctorsInPack[i].item_type;
			if (itemType == 'Laboratory' || itemType == 'Radiology') {
				this.initOrderDoctorAutoComplete(condDoctor, this.doctorList, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("packConductingDoctor", "");
						document.getElementById("packCondDoctorId"+index).value = aArgs[2][1];
						}, "dept_id", (itemType == 'Laboratory' ? 'DEP_LAB': 'DEP_RAD'));
			} else {
				this.initOrderDoctorAutoComplete(condDoctor, this.doctorList, function(sType, aArgs) {
					var index = (aArgs[0].getInputEl().getAttribute("id")).replace("packConductingDoctor", "");
						document.getElementById("packCondDoctorId"+index).value = aArgs[2][1];
						});
			}


		}
	}

	this.orderDialogValidate = function() {
		if (this.currentFieldSet == null)
			showMessage("js.common.order.undefined.current.fieldset.string");

		if (this.currentFieldSet == this.mainFieldSet) {
			return this.validateMainDetails();
		}

		else if (mod_adv_packages == 'Y' && this.currentFieldSet == this.multiVisitPackageFieldSet)
			return this.validateMultiVisitPackageDetails();

		else if (this.currentFieldSet == this.opFieldSet)
			return this.validateOperationDetails();

		else if (this.currentFieldSet == this.docVisitFieldSet)
			return this.validateDoctorVisitDetails();

		else if (this.currentFieldSet == this.discountFieldSet)
			return this.validateDiscountDetails();

		else if (this.currentFieldSet == this.miscFieldSet)
			return this.validateMiscDetails();
		else if (this.currentFieldSet == this.addConductingDocFieldSet)
			return this.validateConductingDoctorInPack();

		showMessage("js.common.order.unknown.current.fieldset.string");
		return false;
	}

	this.validateConductingDoctorInPack = function() {
		var els = document.getElementsByName('packConductingDoctor');
		for (var i=0; i<els.length; i++) {
			if (els[i].value == '') {
				showMessage("js.common.order.conducting.doctor.required");
				els[i].focus();
				return false;
			}
		}
		return true;
	}

	this.validateMainDetails = function() {
		var form = document.orderDialogForm;
		var item = this.itemsInput.value;
		if (item == '' || this.orderItemId == null || this.orderItemId == ''  ) {
			showMessage("js.common.order.item.required");
			this.itemsInput.focus();
			return false;
		}

		if( this.restictionType == this.orderItemType ){
			alert(getString("js.common.order.can.not.order.string")+" "+this.orderItemType);
			form.orderDialogItems.value = '';
			form.orderDialogItems.focus();
			return false;
		}


		if(this.preAuthReq != 'N' && gIsInsurance){
			var preAuthEntered = form.prior_auth_id.value;
			if(preAuthEntered == ''){
				if(this.preAuthReq == 'A'){
					showMessage("js.common.order.pre.auth.no.required");
					form.prior_auth_id.focus();
					return false;
				}else{
					return confirm(getString("js.common.order.this.item.may.need.pre.auth.number.string")+"\n"+getString("js.common.order.check.plan.details.for.more.details.string"));
				}
			}
		}

		if(this.toothNumberEnabled && this.orderItemType == 'Service' && this.toothNumberReq == 'Y') {
			var toothNumber = form.d_tooth_number.value;
			if(toothNumber == '') {
				showMessage("js.common.order.tooth.no.required");
				form.d_tooth_number.focus();
				return false;
			}
		}

		if (gPrescDocRequired == 'Y' && !form.prescribing_doctor.disabled) {
			if (!validateRequired(form.prescribing_doctor, getString("js.common.order.prescribing.doctor.required")))
				return false;
		}
		if (!form.presdate.disabled) {
			var valid = true;
			valid = valid && validateRequired(form.presdate, getString("js.common.order.order.date.required"));
			valid = valid && validateRequired(form.prestime, getString("js.common.order.order.time.required"));
			if(this.allowBackdate != 'A') {
				valid = valid && doValidateDateField(form.presdate, 'future');
			}
			valid = valid && doValidateDateField(form.presdate, 'past');
			valid = valid && validateTime(form.prestime);

			var regDateTime = getDateTime(this.regDate, this.regTime);
			var presDateTime = getDateTimeFromField(form.presdate, form.prestime);

			if (presDateTime < regDateTime) {
				showMessage("js.common.order.order.date.time.cannot.be.less.tha.admit.time.string");
				form.fromDate.focus();
				return false;
			}

			if ( this.billOpenDate != null ) {//hospital bill is selected,bill open date is the min date for order
				var billOpenDate = getDateTime(this.billOpenDate.split(" ")[0],this.billOpenDate.split(" ")[1]);
				if (presDateTime < billOpenDate) {
					var msg = getString("js.common.order.order.date.time.cannot.be.less.tha.bill.open.time.string");
					msg = msg + " " + this.billOpenDate;
					alert(msg);
					form.fromDate.focus();
					return false;
				}
			} else {//new bill,current date is the min date for order
				var billOpenDate = getServerDate();
				if (presDateTime < billOpenDate) {
					var msg = getString("js.common.order.order.date.time.cannot.be.less.tha.bill.open.time.string");
					msg = msg + " " + formatDate(getServerDate(), "ddmmyyyy", "-")+" "+formatTime(getServerDate(),false);
					alert(msg);
					form.fromDate.focus();
					return false;
				}
			}

			if (!valid)
				return false;
		}
		if (!form.toDate.disabled) {
			var valid = true;
			if (this.allowBackdate != 'A') {
				valid = valid && doValidateDateField(form.toDate, 'future');
				valid = valid && validateTime(form.toTime);
			}
			if (!valid)
			{
				form.toTime.value="";
				form.toDate.value="";
				form.toDate.focus;
				return false;
			}
		}

		if (this.orderItemType == 'Meal' && this.mealTimingsRequired ) {
			if (!validateRequired(form.mealTiming))
				return false;
			if (!validateRequired(form.fromDate))
				return false;
			if (this.allowBackdate != 'A') {
				if (!doValidateDateField(form.fromDate, 'future'))
					return false;
			}
		}
		if ( equipTimingsRequired && (this.orderItemType == 'Equipment' || this.orderItemType == 'Operation' || this.orderItemType == 'Bed'
				|| (this.orderItemType == 'Package' && this.pkgHasOp)
				|| (this.orderItemType == 'Order Sets' && this.pkgHasOp))) {

			if (!validateRequired(form.units))
				return false;

			if (!this.validateFromToDates(form.fromDate, form.fromTime, form.toDate, form.toTime)) {
				return false;
			}

			if (this.allowBackdate != 'A') {
				if (!doValidateDateField(form.fromDate, 'future'))
					return false;
				if (!doValidateDateField(form.toDate, 'future'))
					return false;
			}
		}

		if (this.orderItemType == 'Doctor') {
			var valid = true;
			if (this.operItems)
				valid = valid && validateRequired(form.otDoctorCharge);
			else
				valid = valid && validateRequired(form.doctorCharge);
				valid = valid && validateRequired(form.quantity);

			valid = valid && validateRequired(form.fromDate, getString("js.common.order.start.date.required"));
			valid = valid && validateRequired(form.fromTime, getString("js.common.order.start.time.required"));
			valid = valid && doValidateDateField(form.fromDate, (this.allowBackdate != 'A') ? 'future' : '');
			valid = valid && validateTime(form.fromTime);

			if (!valid)
				return false;
			if(this.regDate == null)//possible in case of package master which does not have regdate
				return true;
			var regDateTime = getDateFromVariable(this.regDate, this.regTime);
			var fromDateTime = getDateTimeFromField(form.fromDate, form.fromTime);

			if (fromDateTime < regDateTime) {
				showMessage("js.common.order.consultation.date.time.cannot.be.less.tha.admit.time.string");
				form.fromDate.focus();
				return false;
			}
		}

		if (!form.quantity.disabled) {
			if (!validateRequired(form.quantity))
				return false;
			if (!validateSignedAmount(form.quantity, "Quantity must be a valid amount"))
				return false;
		}

		// while creating package in the master, do not validate the doctor selection.
		if ( this.conductingDoctorReq &&
			(this.orderItemType == 'Laboratory' || this.orderItemType == 'Radiology' || this.orderItemType == 'Service')
			 && gScreenId != 'mas_packages' && form.conducting_doctor.value == ''){
			showMessage("js.common.order.conducting.doctor.required");
			form.conducting_doctor.focus();
			return false;
		}

		if( !this.checkDuplicates() )
			return false;

		if(mod_adv_packages == 'Y' && !this.validateMultiVisitPackageItems())
			return false;

		return this.checkItemOrderable();
	}
	
	this.checkItemOrderable = function() {
		if(typeof filter === "undefined" || filter === '') {
			return true;
		}
		
		for(var index in this.packageDetails) {
			if(this.packageDetails[index].item_type !== filter) {
				showMessage("js.common.order.cannot.add.order.set.comprising.other.items");
				return false;
			}
		}
		
		return true;
	}

	this.validateMultiVisitPackageItems =function(billNo) {
		var billNo = this.billNo;
		var packageIds = document.getElementsByName('package_id');
		var newElems = document.getElementsByName('new');
		var multiVisitPackArr = document.getElementsByName('multi_visit_package');
		var cancelled = document.getElementsByName('cancelled');
		var orderTable = document.getElementById('orderTable'+0);
		var mvPackBillNo = document.getElementsByName("multi_visit_package_bill_no");
		var patPackIds = document.getElementsByName("mv_pat_package_id");
		var isMultiVisitPackageBill = false;
		var formHiddenpkgIdsLen = 3;

		if(orderTable) {
			if(!empty(billNo) && billNo != 'new' && billNo != 'newInsurance') {
				isMultiVisitPackageBill = !empty(this.getMultiVisitPackageBillDetails(billNo));
				if(this.multiVisitPackage) {
					var billItemsCount = this.getBillItemsCount(billNo);
					if((!empty(billItemsCount) && billItemsCount != '0') && !isMultiVisitPackageBill ) {
						alert(billNo+" "+getString("js.common.order.contains.other.items.can.not.add.multivisit.pacakge.items.in.same.bill.msg"));
						return false;
					}
				}
				var patPackId = 0;
				var packId =0;
				for(var i=0;i<packageIds.length-formHiddenpkgIdsLen;i++) {
					if(billNo == mvPackBillNo[i].value) {
						patPackId = patPackIds[i].value;
						packId = packageIds[i].value;
						break;
					}
				}
				if(!empty(packId) && !empty(patPackId)) {
					var packStatus = this.getPackageStatus(patPackId,packId);
					if(packId == this.orderItemId && (packStatus == 'C' || packStatus == 'X')) {
						showMessage("js.common.order.same.multivisitpackage.with.status.in.progress.not.allowed.in.same.bill.msg");
						return false;
					}
				}
			}

			if (!empty(billNo) && billNo != 'new' && billNo != 'newInsurance') {
				for(var i=0;i<packageIds.length-formHiddenpkgIdsLen;i++) {
					if(billNo == mvPackBillNo[i].value || (billNo != mvPackBillNo[i].value && newElems[i].value == 'Y')
						&& empty(cancelled[i].value)) {
						if (empty(packageIds[i].value) && multiVisitPackArr[i].value === 'true' && this.multiVisitPackage) {
							showMessage("js.common.order.multiple.visit.package.can.not.be.added.with.other.items");
							return false;
						} else if (!empty(packageIds[i].value) && multiVisitPackArr[i].value === 'true' && !this.multiVisitPackage && isMultiVisitPackageBill) {
							showMessage("js.common.order.other.items.can.not.be.added.to.the.order.if.order.consists.multi.visit.package");
							return false;
						} else if(!empty(packageIds[i].value) && multiVisitPackArr[i].value === 'true' && packageIds[i].value != this.orderItemId && isMultiVisitPackageBill) {
							showMessage("js.common.order.not.allowed.in.same.bill.msg");
							return false;
						}
					}
				}
			} else {
				for(var i=0;i<packageIds.length-formHiddenpkgIdsLen;i++) {
					if(empty(cancelled[i].value) && newElems[i].value == 'Y') {
						if (empty(packageIds[i].value) && multiVisitPackArr[i].value === 'true' && this.multiVisitPackage) {
							showMessage("js.common.order.multiple.visit.package.can.not.be.added.with.other.items");
							return false;
						} else if (!empty(packageIds[i].value) && multiVisitPackArr[i].value === 'true' && !this.multiVisitPackage) {
							showMessage("js.common.order.other.items.can.not.be.added.to.the.order.if.order.consists.multi.visit.package");
							return false;
						} else if(!empty(packageIds[i].value) && multiVisitPackArr[i].value === 'true' && packageIds[i].value != this.orderItemId) {
							showMessage("js.common.order.not.allowed.in.same.bill.msg")
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	this.getPackageStatus = function(patPackId,packId) {
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

	this.getMultiVisitPackageBillDetails = function(billNo) {
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

	this.getBillItemsCount = function(billNo) {
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

	this.checkDuplicates = function(){
		var form = document.orderDialogForm;
		this.consExists = false;
		// check for duplicate
		var presDateTime = getDateTimeFromField(form.presdate, form.prestime);
		var itemId = document.getElementsByName("item_id");
		var presDate = document.getElementsByName("pres_date");
		var cancelled = document.getElementsByName("cancelled");
		var type = document.getElementsByName("type");
		var status = document.getElementsByName("status");

		if( allOrdersJSON == null )//in case of quick estimate
			return true;
		if ( itemId.length == 3 )//incase no orders are placed duplicate check is unnecessary
			return true;

		if(this.orderItemType != 'Doctor'){
			for(var i =0;i<itemId.length;i++){
				if( presDate[i].value == form.presdate.value && itemId[i].value == this.orderItemId
					&& ( cancelled[i].value == '' && null != status[i] && status[i].value != 'X' ) ){
					return confirm(document.orderDialogForm.orderDialogItems.value+" "+getString("js.common.order.already.ordered.still.want.to.order.string"));
				}
			}
		}else {
			if( eClaimModule == 'Y' && this.patientType == 'o' && this.isInsurance){
				for(var i =0;i<itemId.length;i++){
					if(  type[i].value.toLowerCase() == 'Doctor'.toLowerCase()
						&& ( cancelled[i].value == '' &&  status[i].value != 'X' ) ){
						this.consExists = true;
						break;
					}
				}
				if( this.consExists ){//only one consultation is allowed per a visit
					alert(this.orderItemName+"["+this.orderItemDeptName+"]"+" "+getString("js.common.order.is.pending.string")+
					             "\n"+getString("js.common.order.can.not.order.more.than.one.consultation.per.visit.string"));
					return false;
				}
				var consultationType = findInList(doctorConsultationTypes, "consultation_type_id", form.doctorCharge.value);
				if( this.consExists && ( consultationType.visit_consultation_type  != -2 && consultationType.visit_consultation_type != -4 )
				 &&  mainConsultations.length > 0 ){//only one main consultation is allowed per an Encounter
					alert(this.orderItemName+"["+this.orderItemDeptName+"]"+" "+getString("js.common.order.consultation.not.allowed.string")+
					            "\n"+getString("js.common.order.since.can.not.order.more.than.one.main.consultation.string"));
					return false;
				}
			}else{
				for(var i =0;i<itemId.length;i++){
					if( presDate[i].value == form.presdate.value && itemId[i].value == this.orderItemId
						&& ( cancelled[i].value == '' && status[i].value != 'X' ) ){
						return confirm(this.orderItemName+" "+getString("js.common.order.already.ordered.still.want.to.order.string"));
					}
				}
			}
		}
		
		// Duplicate Order Sets check
		var packageIds = document.getElementsByName('package_id');
		var newElems = document.getElementsByName('new');
		var multiVisitPackArr = document.getElementsByName('multi_visit_package');
		var cancelled = document.getElementsByName('cancelled');
		var orderTable = document.getElementById('orderTable'+0);
		var formHiddenpkgIdsLen = 3;

		if(!orderTable) {
			return true;
		}
			
			
		for(var i=0;i<packageIds.length-formHiddenpkgIdsLen;i++) {
			if(empty(cancelled[i].value) && newElems[i].value == 'Y' && 
					!empty(packageIds[i].value) && multiVisitPackArr[i].value === 'false' && packageIds[i].value == this.orderItemId) {
				showMessage("js.common.order.cannot.add.multiple.order.sets.of.same.kind")
				return false;
			}
		}
		
		return true;
	}


	this.validateMultiVisitPackageDetails = function() {
		var valid = true;
		var count = 0;
		var billNo = this.billNo;
		var packageIds = document.getElementsByName('pacakage_id');
		var newElems = document.getElementsByName('new');
		var multiVisitPackArr = document.getElementsByName('multi_visit_package');
		var selectBoxes = document.getElementsByName('select_item');
		var itemType = document.getElementsByName('item_type');
		var orderedQty = document.getElementsByName('order_qty');
		var availableQty = document.getElementsByName('available_qty');
		var validNumber = /^[0-9]+$/;
		var regExp = new RegExp(validNumber);
		for(var i=0;i<selectBoxes.length;i++) {
			if(selectBoxes[i].checked) {
				count++;
			}
		}

		if(count <= 0) {
			showMessage("js.common.order.atleast.one.item.should.be.selected");
			return false;
		}

		for(var i=0;i<selectBoxes.length;i++) {
			if(selectBoxes[i].checked && empty(orderedQty[i].value)) {
				showMessage('js.common.order.please.enter.order.qty');
				document.getElementById('order_qty'+i).focus();
				valid = false;
				break;
			}

			if (selectBoxes[i].checked && !empty(orderedQty[i].value) &&
				(!regExp.test(orderedQty[i].value) || orderedQty[i].value == 0)) {
				showMessage("js.common.order.qty.should.be.greater.than.Zero.and.it.should.be.a.whole.number");
				document.getElementById('order_qty'+i).focus();
				valid = false;
				break;
			}

			if(selectBoxes[i].checked && !empty(orderedQty[i].value)
				&& parseInt(orderedQty[i].value) > parseInt(availableQty[i].value)){
				showMessage('js.common.order.qty.can.not.be.more.than.available');
				document.getElementById('order_qty'+i).focus();
				valid = false;
				break;
			}

		}
		return valid;
	}

	this.validateDoctorVisitDetails = function() {
		var valid = true;
		if (this.docVisitsInPack.length > 0) {
			var docVisitTable = document.getElementById("docVisits");
			var len = this.docVisitsInPack.length;
			for (var i=0; i<len; i++ ) {
				var doctorVisit = document.getElementById("doctorVisit"+i);
				var doctorVisitId = document.getElementById("vDoctorId"+i);
				var presDate = document.getElementById("presdate"+i);
				var presTime = document.getElementById("prestime"+i);

				valid = valid && validateRequired(doctorVisitId, getString("js.common.order.doctor.required"));
				if( !valid ){//we pass id field for validation and focus shd be on name field hence this is required
					doctorVisit.focus();
				}
				valid = valid && validateRequired(presDate, getString("js.common.order.consultation.date.required"));
				valid = valid && validateRequired(presTime, getString("js.common.order.consultation.time.required"));

				valid = valid && doValidateDateField(presDate, (this.allowBackdate != 'A') ? 'future' : '');
				valid = valid && validateTime(presTime);
				if (!valid)
					break;

				if (gScreenId != 'mas_packages') {
					var regDateTime = getDateFromVariable(this.regDate, this.regTime);
					var fromDateTime = getDateTimeFromField(presDate, presTime);

					if (fromDateTime < regDateTime) {
						showMessage("js.common.order.visiting.date.time.can.not.be.less.than.admit.time.string");
						presDate.focus();
						return false;
					}
				}
			}
		}
		return valid;
	}

	this.validateOperationDetails = function() {
		var form = document.orderDialogForm;
		var anaesthesiTypes = document.getElementsByName('anesthesia_type');
		var anaesthesiTypesFromDate = document.getElementsByName('anes_start_date');
		var anaesthesiTypesToDate = document.getElementsByName('anes_end_date');
		var anaesthesiTypesFromTime = document.getElementsByName('anes_start_time');
		var anaesthesiTypesToTime = document.getElementsByName('anes_end_time');
		var regDateTime = getDateFromVariable(this.regDate, this.regTime);


		if (form.surgeon.value == "") {
			showMessage("js.common.order.primary.surgeon.required");
			form.surgeon.focus();
			return false;
		}

		if (form.theatre.value == "" && this.fixedOtCharges == 'N') {
			showMessage("js.common.order.operation.theatre.required");
			form.theatre.focus();
			return false;
		}

		if(anaesthesiTypes != null && anaesthesiTypes.length > 0) {
			for( var i=0;i<anaesthesiTypes.length;i++) {
				if(!empty(anaesthesiTypes[i].value)) {
					var anStartDateTime = getDateTime(anaesthesiTypesFromDate[i].value,anaesthesiTypesFromTime[i].value);
					var anEndDateTime = getDateTime(anaesthesiTypesToDate[i].value,anaesthesiTypesToTime[i].value);
					if (anStartDateTime < regDateTime) {
						showMessage("js.common.order.operation.anaesthesia.from.time.less.than.admit.time.string");
						anaesthesiTypesFromDate[i].focus();
						return false;
					}

					if (anEndDateTime < regDateTime) {
						showMessage("js.common.order.operation.anaesthesia.to.time.less.than.admit.time.string");
						anaesthesiTypesToDate[i].focus();
						return false;
					}

					if(empty(anaesthesiTypesFromDate[i].value)) {
						showMessage("js.common.order.operation.anaesthesia.start.date.is.required");
						document.getElementById('anes_start_date'+i).focus();
						return false;
					}

					if(empty(anaesthesiTypesToDate[i].value)) {
						showMessage("js.common.order.operation.anaesthesia.end.date.is.required");
						document.getElementById('anes_end_date'+i).focus();
						return false;
					}

					if(empty(anaesthesiTypesFromTime[i].value)) {
						showMessage("js.common.order.operation.anaesthesia.start.time.is.required");
						document.getElementById('anes_start_time'+i).focus();
						return false;
					}

					if(empty(anaesthesiTypesToTime[i].value)) {
						showMessage("js.common.order.operation.anaesthesia.end.time.is.required");
						document.getElementById('anes_end_time'+i).focus();
						return false;
					}

					var anStartDateTime = getDateTime(anaesthesiTypesFromDate[i].value,anaesthesiTypesFromTime[i].value);
					var anEndDateTime = getDateTime(anaesthesiTypesToDate[i].value,anaesthesiTypesToTime[i].value);

					if (anEndDateTime < anStartDateTime) {
						showMessage("js.common.order.operation.anaesthesia.to.date.can.not.bes.less.than.start.date.string");
						anaesthesiTypesFromDate[i].focus();
						return false;
					}
				}
			}
		}
		return true;
	}

	this.validateDiscountDetails = function() {
		var form = document.orderDialogForm;
		return validateRequired(form.discDescription)
			&& validateRequired(form.discAmount)
			&& validateAmount(form.discAmount)
			&& this.validateDiscountAuth(form.discauthId);
	}

	this.validateDiscountAuth = function(discauthIdObj) {
		if (discauthIdObj.value == -1) {
			showMessage("js.common.order.discount.authorizer.can.not.be.rate.plan.discount.string");
			form.discauth.focus();
			return false;
		}
		return true;
	}

	this.validateMiscDetails = function() {
		var form = document.orderDialogForm;
		return validateRequired(form.miscDescription)
			&& validateRequired(form.miscRate)
			&& validateRequired(form.miscQty)
			&& validateAmount(form.miscRate)
			&& validateSignedAmount(form.miscQty);
	}

	this.validateFromToDates = function(fromDateObj, fromTimeObj, toDateObj, toTimeObj) {

		if(!fromDateObj.disabled && !toDateObj.disabled){
			if (!validateFromToDateTime(fromDateObj, fromTimeObj, toDateObj, toTimeObj, true, true))
				return false;
			}

		// now check if the date/time is more than the admitting date/time
		if (this.regDate && this.regTime) {
			var regDateTime = getDateFromVariable(this.regDate, this.regTime);
			var fromDateTime = getDateTimeFromField(fromDateObj,fromTimeObj);


			if (fromDateTime < regDateTime) {
				showMessage("js.common.order.from.time.less.than.admit.time.string");
				fromTimeObj.focus();
				return false;
			}
		}
		return true;
	}

	//used for enter key listner
	this.handleEnterKey = function() {
		if (this.autoCompsContainerOpen())
			return;
		if (!document.orderDialogForm.orderDialogAdd.disabled) {
			this.onOrderDialogAdd();
		} else if (!document.orderDialogForm.orderDialogNext.disabled) {
			this.onOrderDialogNext();
		}
	}

	this.autoCompsContainerOpen = function() {
		return (this.itemAutoComp != null && this.itemAutoComp.isContainerOpen()) ||
			   (this.opItemAutoComp != null && this.opItemAutoComp.isContainerOpen()) ||
			   (this.presDoctorAutoComp != null && this.presDoctorAutoComp.isContainerOpen()) ||
			   (this.surgeonAutoComp != null && this.surgeonAutoComp.isContainerOpen()) ||
			   (this.anaesAutoComp != null && this.anaesAutoComp.isContainerOpen()) ||
			   (this.disauthAutoComp != null && this.disauthAutoComp.isContainerOpen());
	}

	/*
	 * Initialization: these need to be at the end so that function vars are defined prior
	 * (or, maybe, we need to use prototype)
	 */
	this.setDefaultOptionForSubGroup();
	this.initAddDialog();
	this.initToothNumberDialog();

	this.initOrderItemAutoComplete();
	this.initOrderOpItemAutoComplete();
	this.fillAnesthesiaTypes();

	this.presDoctorAutoComp =
		this.initOrderDoctorAutoComplete('prescribing_doctor', this.doctorList, onSelectPrescribingDoc);
	this.surgeonAutoComp =
		this.initOrderDoctorAutoComplete('surgeon', this.doctorList, onSelectSurgeon,'ot_doctor_flag','Y');
	this.anaesAutoComp =
		this.initOrderDoctorAutoComplete('anaesthetist', this.anaesthetistList, onSelectAnaesthetist,'ot_doctor_flag','Y');
	this.disauthAutoComp =
		initDiscAuthAutoComplete('discauth', this.discauthList, onSelectDiscAuth, clearDiscAuth);
	this.condDoctorAutoComp =
		this.initOrderDoctorAutoComplete('conducting_doctor', this.doctorList, onSelectConductingDoc);

	var form = document.orderDialogForm;
	YAHOO.util.Event.addListener(form.orderDialogAdd, "click", this.onOrderDialogAdd, this, true);
	YAHOO.util.Event.addListener(form.orderDialogCancel, "click", this.currentDialogCancel, this, true);
	YAHOO.util.Event.addListener(form.orderDialogNext, "click", this.onOrderDialogNext, this, true);
	YAHOO.util.Event.addListener(form.orderDialogPrevious, "click", this.onOrderDialogPrevious, this, true);

	YAHOO.util.Event.addListener(form.discPercent, "change", this.onChangeDiscountPercent, this, true);
	YAHOO.util.Event.addListener(form.mealTiming, "change", this.onChangeMealTiming, this, true);

	var enterKeyListener = new YAHOO.util.KeyListener("orderDialogFormFields", { keys:13 },
				{ fn:this.handleEnterKey, scope:this, correctScope:true } );
	this.addDialog.cfg.setProperty("keylisteners", enterKeyListener);

	return this;
}
function filterServiceSubGroup(){
		document.orderDialogForm.orderDialogItems.value = '';
		var serviceGroup = document.orderDialogForm.service_group_id;
		var subGrpListBox = document.orderDialogForm.service_sub_group_id;
		var filteredSubGroupList = filterList(servicesSubGroupsJSON, "service_group_id", serviceGroup.value);
		var defaultText = document.orderDialogForm.service_group_id.options[0].text;
		var defaultValue = document.orderDialogForm.service_group_id.options[0].value;
		loadSelectBox(subGrpListBox, filteredSubGroupList, 'service_sub_group_name', 'service_sub_group_id', defaultText, defaultValue);
		setSelectedSubGroup(subGrpListBox);
		setAutoCompleteFilterValue();
	}
function setAutoCompleteFilterValue(){
		setItemAutoCompleteFilterValue();
		setOPeItemAutoCompFilterValues();
}
function setItemAutoCompleteFilterValue(){
		document.orderDialogForm.orderDialogItems.value = '';
		if(addOrderDialog.itemAutoComp != null){
			addOrderDialog.itemAutoComp.dataSource.responseSchema.mainFilterValue = document.orderDialogForm.service_group_id.value;
			addOrderDialog.itemAutoComp.dataSource.responseSchema.subFilterValue = document.orderDialogForm.service_sub_group_id.value;
		}
}
function setOPeItemAutoCompFilterValues(){
		document.orderDialogForm.orderDialogOpItems.value = '';
		if(addOrderDialog.opItemAutoComp != null){
			addOrderDialog.opItemAutoComp.dataSource.responseSchema.mainFilterValue = document.orderDialogForm.service_group_id.value;
			addOrderDialog.opItemAutoComp.dataSource.responseSchema.subFilterValue = document.orderDialogForm.service_sub_group_id.value;
		}
}
function setSelectedSubGroup(subGrpListBox){
		if(subGrpListBox.length == 2)
			subGrpListBox.selectedIndex = 1;
		else
			subGrpListBox.selectedIndex = 0;
}
var multiVisitPackageDetails = null;
var docVisitsInPackArr;
function enableDisableButtons(){
	addOrderDialog.isDoctorSelected = false;
	docVisitsInPackArr = new Array();
	var selectBoxes = document.getElementsByName('select_item');
	var itemType = document.getElementsByName('item_type');
	var conductingDocMandatory = document.getElementsByName('conducting_doc_mandatory_pkg');

	condDoctorsInPackArr = new Array();
	addOrderDialog.conductingDoctorsInPack = null;
	addOrderDialog.condDocItemIndexInPack = null;
	addOrderDialog.condDocItemIndexInPack = new Array();
	addOrderDialog.conductingDoctorsInPack = new Array();

	for (var i=0;i<selectBoxes.length;i++) {
		if ((conductingDocMandatory[i].value == 'true')
			&&	(selectBoxes[i].checked
				&& (itemType[i].value  == 'Laboratory'
					|| itemType[i].value  == 'Radiology'
					|| itemType[i].value == 'Service'))) {
			addOrderDialog.conductingDoctorsInPack.push(multiVisitPackageDetails[i]);
			addOrderDialog.condDocItemIndexInPack.push(i);
		}
	}


	for(var i=0;i<selectBoxes.length;i++) {
		if(selectBoxes[i].checked && itemType[i].value == 'Doctor') {
			addOrderDialog.isDoctorSelected = true;
			break;
		}
	}

	addOrderDialog.docVisitsInPack = null;
	addOrderDialog.docVisitsInPack = new Array();
	for (var i=0;i<selectBoxes.length;i++) {
		if (selectBoxes[i].checked && itemType[i].value == 'Doctor') {
			addOrderDialog.docVisitsInPack.push(multiVisitPackageDetails[i]);
		}
	}

	for(var i=0;i<selectBoxes.length;i++) {
		if (selectBoxes[i].checked && !empty(addOrderDialog.packageDetails[i].operation_id)){
			addOrderDialog.isOperationSelected = true;
		}
	}
	if (addOrderDialog.conductingDoctorsInPack.length > 0) {
		addOrderDialog.enableButtons('next', true);
	} else if (addOrderDialog.isDoctorSelected) {
		addOrderDialog.enableButtons('next',true);
	} else {
		addOrderDialog.enableButtons('add',true);
	}
}

function calculateClaimAmount(amount,discount,insuranceCategoryId,firstOfCategory,visitType,billNo,planId){
	var claimAmount = 0;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/billing/BillAction.do?_method=getClaimAmount&amount=' + amount +'&discount=' + discount +'&categoryId='
		+insuranceCategoryId +'&firstOfCategory='+firstOfCategory+'&visitType='+visitType+'&billNo='+billNo+'&planId='+planId;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var claimAmt =" + ajaxobj.responseText);
				if (!empty(claimAmt)) {
					claimAmount = claimAmt;
				}
			}
		}
	}
	return claimAmount;
}

function calculateClaimAmount(amount,discount,insuranceCategoryId,firstOfCategory,visitType,billNo,planId,insPayable){
	var claimAmount = 0;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/billing/BillAction.do?_method=getClaimAmount&amount=' + amount +'&discount=' + discount +'&categoryId='
		+insuranceCategoryId +'&firstOfCategory='+firstOfCategory+'&visitType='+visitType+'&billNo='+billNo+'&planId='
		+planId+'&insPayable='+insPayable;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var claimAmt =" + ajaxobj.responseText);
				if (!empty(claimAmt)) {
					claimAmount = claimAmt;
				}
			}
		}
	}
	return claimAmount;
}

function addAnaestiatistTypeRow() {
	var itemsTable = document.getElementById("anaestiatistTypeTable");
	var anaStartTime = orderDialogForm.fromTime.value;
	var anaEndTime = orderDialogForm.toTime.value;
	var id = itemsTable.rows.length-1;
	var rowObj = document.createElement("TR");
	itemsTable.appendChild(rowObj);
	var fromDate = orderDialogForm.fromDate.value;
	var toDate = orderDialogForm.toDate.value;
	fromDate = (empty(fromDate)) ? getServerDate() : parseDateStr(fromDate);
	toDate = (empty(toDate)) ? getServerDate() : parseDateStr(toDate);
	anaStartTime = empty(anaStartTime) ? formatTime(getServerDate()):anaStartTime;
	anaEndTime = empty(anaEndTime) ? formatTime(getServerDate()) : anaEndTime;

	var cell;
	var cell = document.createElement("TD");
	cell.innerHTML = getString("js.common.order.operation.anaesthesia.type.label")+":";
	rowObj.appendChild(cell);
	var cell = document.createElement("TD");
	rowObj.appendChild(cell);
	cell.innerHTML = '<select name="anesthesia_type" id="anesthesia_type'+id+'" class="dropdown">'+
					 '</select>';
	loadSelectBox(document.getElementById("anesthesia_type"+id), anaeTypes, 'anesthesia_type_name',
			'anesthesia_type_id','-- Select --', '');
	cell.appendChild(makeHidden("anaesthesiaTypeId", "anaesthesiaTypeId"+id, ''));
	var cell = document.createElement("TD");
	cell.setAttribute('class', 'formlabel');
	cell.innerHTML=getString("js.common.order.operation.anaesthesia.type.start.time.label")+":";
	rowObj.appendChild(cell);
	var cell = document.createElement("TD");
	rowObj.appendChild(cell);

	cell.innerHTML = getDateWidget('anes_start_date', 'anes_start_date_'+id, fromDate, '', '', true, true, '', cpath);
	makePopupCalendar('anes_start_date_'+id);
	var textInput1 = makeTextInput("anes_start_time", "anes_start_time"+id, "timefield", 5, null);
	cell.appendChild(textInput1);
	textInput1.setAttribute('style','display:inline') ;
	document.getElementById('anes_start_time'+id).value = anaStartTime;

	var cell = document.createElement("TD");
	cell.setAttribute('class', 'formlabel');
	cell.innerHTML=getString("js.common.order.operation.anaesthesia.type.end.time.label")+":";
	rowObj.appendChild(cell);
	var cell = document.createElement("TD");
	rowObj.appendChild(cell);

	cell.innerHTML = getDateWidget('anes_end_date', 'anes_end_date_'+id, toDate, '', '', true, true, '', cpath);
	makePopupCalendar('anes_end_date_'+id);
	var textInput2 = makeTextInput("anes_end_time", "anes_end_time"+id, "timefield", 5, null);
	cell.appendChild(textInput2);
	textInput2.setAttribute('style','display:inline') ;
	document.getElementById('anes_end_time'+id).value = anaEndTime;
}

function getPackageDetails(obj){
	var pkgId = document.getElementById('pkgid').value;
	pkgId = encodeURIComponent(pkgId);
	makeAjaxCallforStaticPackage(pkgId);
	showValuePkgDialog(obj);
	return null;
}

function initPkgValueCapDialog() {
	var dialogDiv = document.getElementById("valuePkgDialog");
	valuePkgDialog.style.display = 'block';
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

function showValuePkgDialog(obj) {
	valuePkgDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	valuePkgDialog.show();
}

function onCancel() {
	valuePkgDialog.hide();
	clearPackageDetails();
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
