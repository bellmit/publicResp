/*
 * This function gets called when you manually apply a Discount Plan in sales screen.
 * This will then apply the discount accordingly, to every item in the grid
 */
function onChangeDiscountCategory() {
	disableDiscPer();
	applyItemDiscounts();
}

function applyItemDiscounts() {
	var discountPlanDetails;
	if(billDP != null) {
		discountPlanDetails = getDiscountPlanDetails(billDP);
	} else if(gIsInsuranceBill && insuranceHasDP) {
		discountPlanDetails = getDiscountPlanDetails(insuranceDP);
	} else if(document.salesform.discountCategory.value != ''){
		discountPlanDetails = getDiscountPlanDetails(document.salesform.discountCategory.value);
	}
	var table = document.getElementById("medList");
	for (var i=1;i<=getNumItems();i++) {
		var row = table.rows[i];
		var disc = getElementByName(row,"medDisc");
		var discType = getElementByName(row,"medDiscType");
		var discPer = 0;
		//var discPaise = 0;
		var discountRule = getDiscountRule(row,discountPlanDetails);
		if ( discountRule == undefined ) {
			var medicineId = getElementByName(row,"medicineId").value;
			var allBatches = gMedicineBatches[medicineId];
			var batchNo = getElementByName(row,"batchNo").value;
			for (var b=0; b<allBatches.length; b++) {
				var batch = allBatches[b];
				if (batch.medicine_id == medicineId && batch.batch_no == batchNo) {
					discPer = batch.meddisc+gDefaultDiscountPer;
					break;
				}
			}
		}

		if ( discountRule != undefined ) {
			if ( discountRule.discount_type == 'P' ) {
				discPer = getAmount(discountRule.discount_value);
				//discPaise = ((ratePaise * qty) * discPer) / 100;
			} else {
				//discPaise = getPaise(discountRule.discount_value);
			}
		}
		disc.value = discPer;
		if(!gIsInsuranceBill) {
			setHiddenValue(row, "medDiscWithoutInsurance", discPer);
		} else {
			setHiddenValue(row, "medDiscWithInsurance", discPer);
		}
		discType.value = document.salesform.itemDiscType.value;
		/*
		 * Call the recalc function for rowwise discount from here..it will check whether
		 * it is a MRP based or CP based sale and calculate and apply discount accordingly
		 */
		reCalcRowAmounts(row, false, true, "applyDisc");
	}
	/* call the setTotals method to calculate all the final totals*/
	setTotals();
}

function getDiscountPlanDetails(discCategory){
	return filterList(JSON.parse(discountPlansJSON), 'discount_plan_id', discCategory);
}

function getDiscountRule(row,planDetails) {
	if ( planDetails == undefined) return undefined;
	var discountRule;
	var insuranceCatId = getElementByName(row,"insuranceCategoryId").value;
	var itemCatId = getElementByName(row,"itemCategoryId").value;
	for ( var i = 0;i<planDetails.length;i++) {
		var planDetail = planDetails[i];
		if ( (planDetail.applicable_type == "N"
				&& insuranceCatId == planDetail.applicable_to_id.trim() )
		  || (planDetail.applicable_type == "C"
				  	&& planDetail.applicable_to_id.trim() == "PHCMED" )
		  || (planDetail.applicable_type == "S"
					  	&& itemCatId == planDetail.applicable_to_id.trim() )
				) {
			discountRule = planDetail;
			break;
		}
	}

	return discountRule;
}

function validateDiscPer() {
	if(!document.getElementById('itemDiscPer').disabled) {
		if(document.salesform.itemDiscPer.value != 0 &&
			document.salesform.itemDiscPer.value != "")
			document.salesform.discountCategory.disabled = true;
		else if (discCategoryNeeded)
			document.salesform.discountCategory.disabled = false;
	}
	if (!validateDecimal(document.salesform.itemDiscPer, getString("js.sales.issues.discountpercentage.adecimalnumber"), 2))
		return false;
	return true;
}

function disableDiscPer() {
	if(document.salesform.discountCategory.value != '') {
		if(document.salesform.itemDiscPer != undefined)
			document.salesform.itemDiscPer.disabled = true;
		if(document.salesform.itemDiscPerApply != undefined)
			document.salesform.itemDiscPerApply.disabled = true;
	} else {
		if(document.salesform.itemDiscPer != undefined)
			document.salesform.itemDiscPer.disabled = false;
		if(document.salesform.itemDiscPerApply != undefined)
			document.salesform.itemDiscPerApply.disabled = false;
	}
}
