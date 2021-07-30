function getTaxDetails(row, chargeId, chargeHead, chargeGroup, itemId, amount, consultationTypeID, opId, subGrpIds, mrNo, billNo, editD) {
	
	if(chargeHead == 'PHCMED' || chargeHead == 'PHCRET')
		return;
	
	var url = cpath + "/billing/itemtax.json";
	
	var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    
	urlEncodedDataPairs.push(encodeURIComponent("charge_id") + '=' + encodeURIComponent(chargeId));
	urlEncodedDataPairs.push(encodeURIComponent("charge_head") + '=' + encodeURIComponent(chargeHead));
	urlEncodedDataPairs.push(encodeURIComponent("charge_group") + '=' + encodeURIComponent(chargeGroup));
	urlEncodedDataPairs.push(encodeURIComponent("amount") + '=' + encodeURIComponent(amount));
	urlEncodedDataPairs.push(encodeURIComponent("item_id") + '=' + encodeURIComponent(itemId));
	urlEncodedDataPairs.push(encodeURIComponent("consultation_type_id") + '=' + encodeURIComponent(consultationTypeID));
	urlEncodedDataPairs.push(encodeURIComponent("op_id") + '=' + encodeURIComponent(opId));
	urlEncodedDataPairs.push(encodeURIComponent("sub_group_ids") + '=' + encodeURIComponent(subGrpIds));
	urlEncodedDataPairs.push(encodeURIComponent("mr_no") + '=' + encodeURIComponent(mrNo));
	urlEncodedDataPairs.push(encodeURIComponent("bill_no") + '=' + encodeURIComponent(billNo));
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
	
	var ajaxReqObject = newXMLHttpRequest();
    
    ajaxReqObject.addEventListener('load', function(event) {
    	if (ajaxReqObject.readyState == 4) {
    		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) && (ajaxReqObject.responseText != undefined) ) {
    			if(null != editD && undefined != editD && editD){
					//row is not really row here :(
					if(chargeGroup == 'PKG'){
						setEditFormTaxAmt(JSON.parse(ajaxReqObject.response),row, chargeId);
					} else {
						setEditFormTaxAmt(JSON.parse(ajaxReqObject.response),row);
					}
    			}else{
    				setTaxDetails(JSON.parse(ajaxReqObject.response),row);
    			}
    		}
    	}
    });
	
    ajaxReqObject.addEventListener('error', function(event) {
    	if(onFailure)
    		onFailure();
    });
    
    ajaxReqObject.open("POST",url.toString(), false);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    ajaxReqObject.send(urlEncodedData);
	
}

function getPackageTaxDetails(row, chargeIds, chargeHead, chargeGroup, itemId, amounts, consultationTypeID, opId, subGrpIds, mrNo, billNo, editD){

	if(chargeGroup == 'MED' || chargeGroup == 'RET')
		return;
	
	var url = cpath + "/billing/itemtax.json";
	
	var urlEncodedDataPairs = [];
    var urlEncodedData = "";
    
	urlEncodedDataPairs.push(encodeURIComponent("charge_id") + '=' + encodeURIComponent(chargeIds[0]));
	urlEncodedDataPairs.push(encodeURIComponent("charge_head") + '=' + encodeURIComponent(chargeHead));
	urlEncodedDataPairs.push(encodeURIComponent("charge_group") + '=' + encodeURIComponent(chargeGroup));
	amounts.forEach(amount => urlEncodedDataPairs.push(encodeURIComponent("amount") + '=' + encodeURIComponent(amount)))
	urlEncodedDataPairs.push(encodeURIComponent("item_id") + '=' + encodeURIComponent(itemId));
	urlEncodedDataPairs.push(encodeURIComponent("consultation_type_id") + '=' + encodeURIComponent(consultationTypeID));
	urlEncodedDataPairs.push(encodeURIComponent("op_id") + '=' + encodeURIComponent(opId));
	urlEncodedDataPairs.push(encodeURIComponent("sub_group_ids") + '=' + encodeURIComponent(subGrpIds));
	urlEncodedDataPairs.push(encodeURIComponent("mr_no") + '=' + encodeURIComponent(mrNo));
	urlEncodedDataPairs.push(encodeURIComponent("bill_no") + '=' + encodeURIComponent(billNo));
	urlEncodedDataPairs.push(encodeURIComponent("isBulk") + '=' + encodeURIComponent('true'));
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
	
	var ajaxReqObject = newXMLHttpRequest();
    
    ajaxReqObject.addEventListener('load', function(event) {
    	if (ajaxReqObject.readyState == 4) {
    		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null) && (ajaxReqObject.responseText != undefined) ) {
    			if(null != editD && undefined != editD && editD){
					//row is not really row here :(
					taxes = JSON.parse(ajaxReqObject.response);
					let taxIndex = 0;
					chargeIds.forEach(chargeId => setEditFormTaxAmt(taxes[taxIndex++],row, chargeId))
					
    			}else{
					let taxAmounts = JSON.parse(ajaxReqObject.response);
					let taxIndex = 0;
					chargeIds.forEach(chargeId => setTaxDetails(taxAmounts[taxIndex++],getChargeRowByChargeId(chargeId)));	
    			}
    		}
    	}
    });
	
    ajaxReqObject.addEventListener('error', function(event) {
    	if(onFailure)
    		onFailure();
    });
    
    ajaxReqObject.open("POST",url.toString(), false);
    ajaxReqObject.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    ajaxReqObject.send(urlEncodedData);
}

function setEditFormTaxAmt(response, id, chargeId){
	if (response != undefined) {
		var taxMap = response;
		var i = 0;

		if(chargeId){
			var taxAmtObj = document.getElementById("e_tax_amt_" + chargeId + "_" +id);
			// var subGrpObjVal = document.getElementById("sub_group_id_" + chargeId + "_" +id).value;
			var subGrpObjVal = document.getElementById("sub_group_id"+id).value;
		} else {
			var taxAmtObj = document.getElementById("e_tax_amt"+id);
			var subGrpObjVal = document.getElementById("sub_group_id"+id).value;
		}
		for (var key in taxMap) {
		  if (taxMap.hasOwnProperty(subGrpObjVal)) {
			var val = taxMap[subGrpObjVal];
			var totalTaxAmtPaise = getPaise(val.amount);
		    i++;
		  }
		}
		taxAmtObj.value = formatAmountPaise(totalTaxAmtPaise);
	}
}

function setTaxDetails(response, row){
	if (response != undefined) {
		var taxMap = response;
		var totalTaxAmtPaise = 0;
		var i = 0;
		var taxCell = row.cells[TAX_COL];
		var amtCell = row.cells[AMT_COL];
		var idx = getRowChargeIndex(row);

		var chargeId = getIndexedValue("chargeId", idx);

		if (null != chargeId && "" != chargeId && undefined != chargeId)
			chargeId = (chargeId.substring(0, 1) == '_') ? idx : chargeId;

		var editDialogTaxSubgroups = document.getElementsByName("sub_group_id");

		var keys = [];
		if (editDialogTaxSubgroups.length) {
			for (var index = 0; index < editDialogTaxSubgroups.length; index++) {
				keys.push(editDialogTaxSubgroups[index].value);
			}
		} else {
			keys = Object.keys(taxMap);
		}
		
		for (var keyIndex = 0; keyIndex < keys.length; keyIndex++) {
		  if (taxMap.hasOwnProperty(keys[keyIndex])) {
			  
			var sgGpId = document.getElementById(chargeId+"_sub_group_id"+i);
			var taxAmt = document.getElementById(chargeId+"_tax_amt"+i);
			var taxRate = document.getElementById(chargeId+"_tax_rate"+i);
		    
		    if(sgGpId != undefined) {
		    	amtCell.removeChild(sgGpId);
		    }
		    if(taxAmt != undefined) {
		    	amtCell.removeChild(taxAmt);
		    }
		    if(taxRate != undefined) {
		    	amtCell.removeChild(taxRate);
		    }
		    
			var val = taxMap[keys[keyIndex]];
		    amtCell.appendChild(makeHidden(chargeId+"_sub_group_id", chargeId+"_sub_group_id"+i, keys[keyIndex]));
		    amtCell.appendChild(makeHidden(chargeId+"_tax_amt", chargeId+"_tax_amt"+i, formatAmountPaise(getPaise(val.amount))));
		    amtCell.appendChild(makeHidden(chargeId+"_tax_rate", chargeId+"_tax_rate"+i, val.rate));
		    
		    if(isNewChargeItem(row))
		    	amtCell.appendChild(makeHidden(chargeId+"_charge_tax_id", chargeId+"_charge_tax_id"+i, ""));
		    
		    for(var k=0; k<taxSubGroupsList.length; k++){
				if(taxSubGroupsList[k].item_subgroup_id == keys[keyIndex]){
					var itemGrpId = taxSubGroupsList[k].item_group_id;
					amtCell.appendChild(makeHidden(chargeId+"_item_group_id", chargeId+"_item_group_id"+i, itemGrpId));
				}
			}
		    
		    totalTaxAmtPaise += getPaise(val.amount);
		    i++;
		  }
		}
		var totTax = document.getElementById("total_tax_"+chargeId);
		if(totTax != undefined) amtCell.removeChild(totTax);
		
		var taxesCount = document.getElementById("taxesCnt_"+chargeId);
		if(taxesCount != undefined) amtCell.removeChild(taxesCount);
		
		amtCell.appendChild(makeHidden("total_tax_"+chargeId, "total_tax_"+chargeId, formatAmountPaise(totalTaxAmtPaise)));
		setNodeText(taxCell, formatAmountPaise(totalTaxAmtPaise));
		
		amtCell.appendChild(makeHidden("taxesCnt_"+chargeId, "taxesCnt_"+chargeId, i));
		
		setIndexedValue("tax_amt",idx, formatAmountPaise(totalTaxAmtPaise));
		setIndexedValue("original_tax_amt",idx, formatAmountPaise(totalTaxAmtPaise));
		
	}
	
}

function isNewChargeItem(row){
	var idx = getRowChargeIndex(row);
	var itemChgId = getIndexedValue("chargeId", idx);
	var isNew = (itemChgId.substring(0,1) == '_');
	return isNew;
}


function applyBulkTaxes(subGroup,i) {
		var subGrpDetails = findInList(taxSubGroupsList, 'item_subgroup_id', subGroup); 
		var disCheck = getIndexedFormElement(mainform, "discountCheck",i);
		var discObj = getIndexedFormElement(mainform, "disc", i);
		var qty, ratePaise,retQty;
		var isdisCatAppl = isDiscountCatgoryApplicable(document.mainform.discountCategory,i);
		var row = getChargeRow(i);
		var amtCell = row.cells[AMT_COL];
		
		if( subGroup > 0 ) {
			
			var chargeId = getIndexedValue("chargeId",i);
			crateTaxParams(i,amtCell,chargeId,subGrpDetails.item_group_id,subGroup);
			setIndexedValue("edited", i, 'true');
			chargesEdited++;
			setRowStyle(i);
			
		}
}

function crateTaxParams(index,parentObj,chargeId,taxgrpId,subGrpId){
	//first clear tax fields if any 
	
	var sgGpId = document.getElementById(chargeId+"_sub_group_id"+0);
	var taxAmt = document.getElementById(chargeId+"_tax_amt"+0);
	var taxRate = document.getElementById(chargeId+"_tax_rate"+0);
    
    if(sgGpId != undefined) {
    	parentObj.removeChild(sgGpId);
    }
    if(taxAmt != undefined) {
    	parentObj.removeChild(taxAmt);
    }
    if(taxRate != undefined) {
    	parentObj.removeChild(taxRate);
    }
	
	//add new child now
	parentObj.appendChild(makeHidden(chargeId+"_sub_group_id", chargeId+"_sub_group_id"+0, subGrpId));
 	
}

function onchangeBulkTaxSubGroup(subGrpObj){
	
	itemSelectionOptions(subGrpObj.value != 0);
	if ( subGrpObj.value == 0 ) {
		
		mainform.discountCategory.disabled = false;
		if (mainform.itemDiscPer) {
			mainform.itemDiscPer.disabled = false;
		}
	} else {
		
		mainform.discountCategory.disabled = true;
		if ( mainform.itemDiscPer ) {
			mainform.itemDiscPer.disabled = true;
		}
	}
	
}

function itemSelectionOptions(requiresSelection){
	
	if ( !requiresSelection ){
		requiresSelection = mainform.billDiscountCategory.value == '' ;
	}
	var discElements = document.mainform.discountCheck;
	for(var i=0; i < getNumCharges(); i++){
		discElements[i].disabled = !requiresSelection;//enable
	}
	mainform.discountAll.disabled = !requiresSelection;
	
}

