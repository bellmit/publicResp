function getDiscountRule(rowIndex,planDetails){

	if ( planDetails == undefined) return undefined;
	var discountRule ;
	var insuranceCatId = getIndexedValue("insuranceCategoryId", rowIndex);
	var chargeHead = getIndexedValue("chargeHeadId", rowIndex);
	var descriptionId = getIndexedValue("descriptionId", rowIndex);
	for ( var i = 0;i<planDetails.length;i++) {
		var planDetail = planDetails[i];
		if ( (planDetail.applicable_type == "N"
				&& insuranceCatId == parseInt(planDetail.applicable_to_id.trim()) )
		  || (planDetail.applicable_type == "C"
				  	&& chargeHead == planDetail.applicable_to_id.trim() )
		  || (planDetail.applicable_type == "I"
					  	&& descriptionId == planDetail.applicable_to_id.trim() )
				) {
				discountRule = planDetail;
				break;
		}
	}

	return discountRule;
}

function applyItemDiscPer(itemDiscountPer,index) {

	var discountPlanDetails;
	if(document.mainform.discountCategory.value != ''){
		discountPlanDetails = getDiscountPlanDetails(document.mainform.discountCategory.value);
	}
	if(document.mainform.itemDiscPer != undefined) {
		if (!validateDecimal(document.mainform.itemDiscPer, getString("js.billing.billlist.discountpercent.validamount"), 2))
			return false;
	}

	if (!validateDiscountWithDynaPackage())
		return false;

	var discountCategoryExists = isDiscountCategoryExists();

	if (!selectedItemsForDiscount() && !discountCategoryExists) {
		if (!discCategoryChange) {
			showMessage("js.billing.billlist.selectcharge.applydiscount");
			return false;
		}
	}

	var claimAmtEdited = false;

	for (var i=0;i<getNumCharges();i++) {
		if ( index != 0 && i != index ) {
			continue;
		}
		
		let chargeGroup = getIndexedValue("chargeGroupId",i);
		let chargeHead = getIndexedValue("chargeHeadId",i);
		var chargeId = getIndexedValue("chargeId",i);

		if( chargeGroup == 'RET' || chargeGroup == 'MED' || chargeHead == 'PKGPKG') {
			continue;
		}

		var disCheck = getIndexedFormElement(mainform, "discountCheck",i);
		var discObj = getIndexedFormElement(mainform, "disc", i);
		var qty, ratePaise,retQty;
		var isdisCatAppl = isDiscountCatgoryApplicable(document.mainform.discountCategory,i);
		if ((disCheck.checked || isPackageSelected(chargeId))&& (isDiscountEditable(i) || isdisCatAppl)) {
			
				if( document.getElementById("_bulk_item_subgroup_id") == null || document.getElementById("_bulk_item_subgroup_id").value == 0 ){
					//tax should not override Discounts
					if (getPaise(discObj.value) > 0 && (getIndexedValue("chargeHeadId",i) != 'BIDIS')) {
						setIndexedValue("overall_discount_auth_name", i, '');
						setIndexedValue("overall_discount_auth", i, '0');
						setIndexedValue("overall_discount_amt", i, '');
					}
	
					if (getIndexedValue("chargeHeadId",i) != 'BIDIS') {
		
						rateObj = getIndexedFormElement(mainform, "rate",i);
						qtyObj = getIndexedFormElement(mainform, "qty",i);
						retQtyObj = getIndexedFormElement(mainform, "returnQty",i);
						existDiscObj = getIndexedFormElement(mainform, "discount",i);
						returnAmtObj = getIndexedFormElement(mainform, "returnAmt",i);
		
						if (rateObj.value == "") { rateObj.value = 0; }
						if (qtyObj.value == "") { qtyObj.value = 0; }
						if (retQtyObj.value == "") { retQtyObj.value = 0; }
						if (existDiscObj.value == "") { existDiscObj.value = 0; }
						if (returnAmtObj.value == "") { returnAmtObj.value = 0; }
		
						ratePaise = getPaise(rateObj.value);
						qty = getAmount(qtyObj.value);
						retQty = getAmount(retQtyObj.value);
						existDisc = getPaise(existDiscObj.value);
						returnAmt = getPaise(returnAmtObj.value);
		
						var discPer = 0;
						var discPaise = 0;
						if ( itemDiscountPer != '0' && itemDiscountPer != '') {
							discPer = getAmount(itemDiscountPer);
							if(getIndexedValue("chargeGroupId",i) == 'ITE') {
								discPaise = (qty+retQty)*ratePaise * (discPer/ 100);
								//discPaise = (-(retQty/qty)*existDisc) + (qty+retQty)*ratePaise * (discPer/ 100);
								//discPaise = ((ratePaise*(-retQty)+returnAmt) + (qty+retQty)*ratePaise) * (discPer/ 100);
							} else {
								discPaise = ((ratePaise * qty) * discPer) / 100;
							}
						} else if (chargeGroup != 'MED' && chargeGroup != 'ITE' && chargeGroup != 'RET'){
							var discountRule = getDiscountRule(i,discountPlanDetails);
		
							if ( discountRule == undefined ) {
								discPer = 0;
								discPaise = 0;
								setIndexedValue("old_overall_discount_auth", i,0);
							}
		
							if ( discountRule != undefined ) {
								if ( discountRule.discount_type == 'P' ) {
									discPer = getAmount(discountRule.discount_value);
									discPaise = ((ratePaise * qty) * discPer) / 100;
								} else {
									discPaise = getPaise(discountRule.discount_value);
								}
							}
						}
		
						if (discPer > 100) {
							showMessage("js.billing.billlist.discountnot.greaterthan100percent");
							return;
						}
						
						if(checkUserDiscountLimit(i, discPaise)) {
							alert(getString("js.billing.billlist.discountnot.greaterthanuserlimit")+userLimit);
							break;
						}
						setNodeText(getChargeRow(i).cells[DISC_COL], formatAmountPaise(discPaise));
						discObj.value = formatAmountPaise(discPaise);
						setIndexedValue("overall_discount_amt", i, formatAmountPaise(discPaise));
		
						var claim = 0;
						if (editform.eClaimAmt != null)
							claim = getPaise(getIndexedValue("insClaimAmt", i));
		
						onChangeDiscount(i);
		
						if (editedAmounts(i, claim))
							claimAmtEdited = true;
				}
			}
			//override tax
			if ( getIndexedValue("chargeHeadId",i) == 'MARPKG' || getIndexedValue("chargeHeadId",i) == 'BIDIS') {
				continue;
			}
			
			if ( document.getElementById("_bulk_item_subgroup_id") ) {
				applyBulkTaxes(document.getElementById("_bulk_item_subgroup_id").value,i);
			}
			
			var chargeId = getIndexedValue("chargeId",i);
			var subGroupIds = document.getElementsByName(chargeId+"_sub_group_id");

		
			var subGroupIdValues = [];

			for (var l=0; l<subGroupIds.length; l++) {
				subGroupIdValues[l] = subGroupIds[l].value;
			}

			getTaxDetails(getChargeRow(i), getIndexedValue("chargeId", i), getIndexedValue("chargeHeadId",i), getIndexedValue("chargeGroupId",i),
					getIndexedValue("descriptionId",i), getIndexedValue("amt",i), getIndexedValue( "consultation_type_id",i),
					getIndexedValue( "op_id",i), subGroupIdValues, mr, billNumber);
		}

	}
	resetTotals(claimAmtEdited, false);
	setPackagesDisplay();
}


function onApplyItemDiscPer(){
	var itemDiscountPer = 0;
	if(document.mainform.itemDiscPer != undefined)
		itemDiscountPer = document.mainform.itemDiscPer.value;
	if(Number(itemDiscountPer) > Number(userPermissibleDiscount)) {
		alert(getString("js.billing.billlist.discountnot.greaterthan.user.permissible.apply")+userPermissibleDiscount+"% for all items selected.");
		return;
	}
	applyItemDiscPer(itemDiscountPer,0);

	var visitID = document.getElementById('visitId').value;
	if ( tpaBill ) {//cash bill don't need it
		getBillChargeClaims(visitID, document.mainform);
	}
}


function isDiscountCategoryExists(){
	var discountCatgeory = document.mainform.discountCategory;
	if(discountCatgeory != undefined && discountCatgeory.value != 0 && discountCatgeory.value != '')
		return true;
	else
		return false;
}


function getDiscountPlanDetails(discCategory){
	return filterList(discountPlansJSON, 'discount_plan_id', discCategory);;
}

var discCategoryChange = false;

function onChangeDiscountCategory(){
	discCategoryChange = true;
	var oldDiscountCategory = mainform.billDiscountCategory.value;
	if(checkDiscountCategoryApplicable()) {
		document.getElementById("discountCategory").options.selectedIndex = 0;
		document.getElementById("discountCategory").focus();
		return false;
	}
	if(oldDiscountCategory != '' && oldDiscountCategory != undefined)
	resetItemDiscounts(oldDiscountCategory);

	selectDiscountCategory();
	applyItemDiscPer(0,0);

	var visitID = document.getElementById('visitId').value;
	if ( tpaBill ) {
		getBillChargeClaims(visitID, document.mainform);
	}
	discCategoryChange = false;
}

function selectDiscountCategory() {
	var discCategory = mainform.discountCategory.value;
	var discCategoryName = mainform.discountCategory.options[mainform.discountCategory.selectedIndex].text;
	discCategoryName = ( discCategoryName == '--Select--') ? '' : discCategoryName;

	mainform.billDiscountCategory.value = discCategory;
	var discountCatLbl = document.getElementById("discountCatLbl");
	if(discCategory != ''){

		if(mainform.itemDiscPer != undefined)
			mainform.itemDiscPer.disabled = true;
		if(mainform.itemDiscPerAdd != undefined)
			mainform.itemDiscPerAdd.disabled = true;
		if(  document.getElementById("_bulk_item_subgroup_id") != null  ){
			 document.getElementById("_bulk_item_subgroup_id").disabled = discCategoryChange;
		}

		mainform.discountAll.disabled = true;
		var chargeHeadIds = document.getElementsByName("chargeHeadId");
		var discElements = document.mainform.discountCheck;
			for(var i=0; i < getNumCharges(); i++){
				var chargeHeadItemExists = false;
				var insuCatId = getIndexedValue ("insuranceCategoryId", i);
				var descrId = getIndexedValue ("descriptionId", i)
				if(discountPlansJSON !=null && discountPlansJSON.length > 0) {
					var discountPlanDetails = filterList(discountPlansJSON, 'discount_plan_id', discCategory);

					for (var j=0 ; j< discountPlanDetails.length; j++){
						var item = discountPlanDetails[j];
						if( (getIndexedValue("chargeGroupId",i) != 'MED' && getIndexedValue("chargeGroupId",i) != 'RET' && getIndexedValue("chargeGroupId",i) != 'ITE') &&
								( (item["applicable_type"] == 'C' &&  chargeHeadIds[i].value == item["applicable_to_id"] )
								|| ( item["applicable_type"] == 'N' && insuCatId == item["applicable_to_id"] )
								|| ( item["applicable_type"] == 'I'
									&& (descrId == item["applicable_to_id"] || (getIndexedValue("chargeGroupId",i) == 'PKG'
										&& getIndexedValue("packId",i) == item["applicable_to_id"])))) ){
							chargeHeadItemExists = true;
							if ( chargeHeadItemExists ) break;
						}
					}
				}
				discElements[i].checked = chargeHeadItemExists;
				discElements[i].disabled = true;
			}
	}else{
		if(mainform.itemDiscPer != undefined)
			mainform.itemDiscPer.disabled = false;
		if(mainform.itemDiscPerAdd != undefined)
			mainform.itemDiscPerAdd.disabled = false;
		if(  document.getElementById("_bulk_item_subgroup_id") != null  ){
			 document.getElementById("_bulk_item_subgroup_id").disabled = false;
		}

		mainform.discountAll.disabled = false;
		var chargeHeadNames = document.getElementsByName("chargeHeadName");
		var discElements = document.mainform.discountCheck;
		for(var i=0; i < getNumCharges(); i++){
			if((getIndexedValue("chargeGroupId",i)) != 'DRG'){
				discElements[i].checked = false;
				discElements[i].disabled = false;
			}
		}
	}
	if(discountCatLbl != undefined){
		document.getElementById("discountCatLbl").textContent = discCategoryName;
	}
}

function discAuthChanged() {
	authChange =true;
}
function discChanged() {
	discChange =true;
}
function qtyChanged() {
	qtyChange =true;
}
function rateChanged() {
	rateChange =true;
}

function checkUserDiscountLimit(index, newDiscount) {
	if(newDiscount <= 0) {
		return false;
	}
	var discountType = getIndexedFormElement(mainform, "isSystemDiscount",index).value;
	var discObj = getIndexedFormElement(mainform, "disc", index);
	if(newDiscount == getPaise(discObj.value)) {
		return false;
	}
	if(discountType == 'N') {
		usrLimitForCalc += getPaise(discObj.value) -newDiscount ;
		if(getPaise(userLimit) + usrLimitForCalc <0) {
			return true;
		}
	}else {
		var discIncreased = getPaise(discObj.value)*userPermissibleDiscount/100;
		usrLimitForCalc += discIncreased - newDiscount;
		if(getPaise(userLimit) + usrLimitForCalc <0) {
			return true;
		}
		setIndexedValue("isSystemDiscount", index,'N');
		setIndexedValue("isSystemDiscountOld", index,'N');
	}
	return false;
}
function isUserLimitExceeded(index) {	
	if(!discChange && !authChange && !qtyChange && !rateChange) {
		return false
	}
	var chargeHead = getIndexedFormElement(mainform, "chargeHeadId",index).value;
	if(chargeHead == 'BIDIS') {
		return false;
	}
	var actRate = editform.eRate.value;
	var actQuantity = editform.eQty.value;
	var applied;
	if (editform.discountType.checked) {
		applied = editform.totalDiscRs.value;
	} else {
		applied = editform.overallDiscRs.value;
	}
	var discountType = getIndexedFormElement(mainform, "isSystemDiscount",index).value;
	var discOverall = getIndexedFormElement(mainform, "old_overall_discount_auth",index).value;
	if(discountType == 'N' || (Number(discOverall) == -1 && authChange) || (discountType == 'Y' && discChange)) {
		var userLimitForItem = formatAmountPaise(getPaise(actRate)*actQuantity*userPermissibleDiscount/100);
		if(Number(applied) > Number(userLimitForItem)) {
			return true;
		}
	}
	return false;
}
function checkDiscLimitEditCharge(index, newDiscount) {
	if(newDiscount <= 0) {
		return false;
	}
	var discOverall = getIndexedFormElement(mainform, "old_overall_discount_auth",index).value;
	var actRate = editform.eRate.value;
	var newQuantity = editform.eQty.value;
	var discountType = getIndexedFormElement(mainform, "isSystemDiscount",index).value;
	var mainActRate = getIndexedFormElement(mainform, "rate", index).value;
	var discObj = getIndexedFormElement(mainform, "disc", index).value;
	var qtyObj = getIndexedFormElement(mainform, "qty", index).value;
	if(discountType == 'N') {
		var changedLimit = formatAmountPaise(getPaise(actRate)*newQuantity*userPermissibleDiscount/100 - getPaise(mainActRate)*qtyObj*userPermissibleDiscount/100 - newDiscount + getPaise(discObj));
		if(getPaise(userLimit) + getPaise(changedLimit) <0) {
			return true;
		}
	}else {
		if(Number(discOverall) == -1 && !authChange && !editform.discountType.checked) {
			return false;
		}
		if(!discChange && !authChange) {
			return false;
		}
		var discIncreased = getPaise(discObj)*userPermissibleDiscount/100;
		var changedLimit1 = formatAmountPaise(getPaise(actRate)*newQuantity*userPermissibleDiscount/100 - getPaise(mainActRate)*qtyObj*userPermissibleDiscount/100 - newDiscount + discIncreased);
		if(getPaise(userLimit) + getPaise(changedLimit1) <0) {
			return true;
		}
		setIndexedValue("isSystemDiscount", index,'N');
		setIndexedValue("isSystemDiscountOld", index,'N');
	}
	return false;
}
function isUserLimitExceedInAddDisc(index, newDiscount) {
	var rateObj1 = getIndexedFormElement(mainform, "rate", index);
	var qtyObj1 = getIndexedFormElement(mainform, "qty", index);
	var retQtyObj1 = getIndexedFormElement(mainform, "returnQty", index);
	if (rateObj1.value == "") { rateObj1.value = 0; }
	if (qtyObj1.value == "") { qtyObj1.value = 0; }
	if (retQtyObj1.value == "") { retQtyObj1.value = 0; }
	var qty1, ratePaise1,retQty1;
	ratePaise1 = getPaise(rateObj1.value);
	qty1 = getAmount(qtyObj1.value);
	retQty1 = getAmount(retQtyObj1.value);
	
	var userLimitForItem = ratePaise1*(qty1+retQty1)*userPermissibleDiscount/100;
	if(Number(newDiscount) > Number(userLimitForItem)) {
		return true;
	}
	return false;
}
function checkDiscountCategoryApplicable() {
	var chargeHeadIds = document.getElementsByName("chargeHeadId");
	var discCategory = mainform.discountCategory.value;
	for (var i = 0; i < getNumCharges(); i++) {
		var chargeHeadItemExists = false;
		var insuCatId = getIndexedValue("insuranceCategoryId", i);
		var descrId = getIndexedValue("descriptionId", i)
		if (discountPlansJSON != null && discountPlansJSON.length > 0) {
			var discountPlanDetails = filterList(discountPlansJSON,
					'discount_plan_id', discCategory);

			for (var j = 0; j < discountPlanDetails.length; j++) {
				var item = discountPlanDetails[j];
				if ((getIndexedValue("delCharge", i) != 'true' && getIndexedValue("chargeGroupId", i) != 'MED'
						&& getIndexedValue("chargeGroupId", i) != 'RET' && getIndexedValue(
						"chargeGroupId", i) != 'ITE')
						&& ((item["applicable_type"] == 'C' && chargeHeadIds[i].value == item["applicable_to_id"])
								|| (item["applicable_type"] == 'N' && insuCatId == item["applicable_to_id"]) 
								|| (item["applicable_type"] == 'I' && descrId == item["applicable_to_id"]))) {
					chargeHeadItemExists = true;

					var isdisCatAppl = isDiscountCatgoryApplicable(
							document.mainform.discountCategory, i);
					if ((isDiscountEditable(i) || isdisCatAppl)
							&& getIndexedValue("chargeHeadId", i) != 'BIDIS') {
						var discPer = 0;
						var discPaise = 0;
						var discountRule = getDiscountRule(i,
								discountPlanDetails);

						if (discountRule != undefined) {
							if (discountRule.discount_type == 'P') {
								discPer = getAmount(discountRule.discount_value);
								if (discPer > userPermissibleDiscount) {
									alert(getString("js.billing.billlist.discountnot.greaterthan.discount.plan.change")+userPermissibleDiscount+"%");
									return true;
								}
								continue;
							} else {
								discPaise = getPaise(discountRule.discount_value);
							}

							if (isUserLimitExceedInAddDisc(i,discPaise)) {
								alert(getString("js.billing.billlist.discountnot.greaterthan.discount.plan.change")+userPermissibleDiscount+"%");
								return true;
							}
						}
					}
					if (chargeHeadItemExists)
						break;
				}
			}
		}
	}
	return false;
}
