function calculateInsClaimAmount(amount,discount,insuranceCategoryId,planIds,visitID,chargeHead){
	var claimAmounts = [];
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + '/billing/BillAction.do?_method=getInsClaimAmount&amount=' + amount +'&discount=' + discount +'&categoryId='
		+insuranceCategoryId+'&planIds='+planIds+'&visit_id='+visitID+'&charge_head='+chargeHead;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				var billChargeClaimList = eval('(' + ajaxobj.responseText + ')');
				if (!empty(billChargeClaimList)) {
					for (var i=0; i<billChargeClaimList.length; i++) {
						claimAmounts[i] = billChargeClaimList[i].insurance_claim_amt;
					}
				}
			}
		}
	}
	return claimAmounts;
}

function getBillChargeClaims(visitID, form){

	var formToken = form._insta_transaction_token.value;
	form._insta_transaction_token.value="";

	YAHOO.util.Connect.setForm(form);

	var url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getBillChargeClaims&visitID='+visitID;
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
			visitLevelAlertMsgs = null;
			var adjTaxAmtsMap = planMap[-2];
			for(var j=0; j<planList.length; j++){
				var planId = planList[j].plan_id;
				var billChgClaimMap = planMap[planId];
				setSponsorAmounts(billChgClaimMap, j+1);

				var adjMap = planMap[-1];
				var visitMap = adjMap[planId];

				var adjStatus = visitMap[-2];
				var message = "";
				if(adjStatus != 'undefined'){
					var visitDedAdjExists = adjStatus & 8;
					var visitMaxCopayAdjExists = adjStatus & 16;
					var visitPerDayLimitAdjExists = adjStatus & 32;
					var visitSpnrLimitAdjExists = adjStatus & 64;
					if(visitDedAdjExists > 0)
						message = null != message ? message + "Visit Deductible," : "Visit Deductible,";
					if(visitMaxCopayAdjExists > 0)
						message = null != message ? message + "Visit Max Copay," : "Visit Max Copay,";
					if(visitPerDayLimitAdjExists > 0)
						message = null != message ? message + "Visit Per Day Limit," : "Visit Per Day Limit,";
					if(visitSpnrLimitAdjExists > 0)
						message = null != message ? message + "Visit Sponsor Limit," : "Visit Sponsor Limit,";
				}
				var priority = planList[j].priority;
				var tpaName = priority == 1 ? priTpaName : secTpaName;
				if(null != visitLevelAlertMsgs) {
					visitLevelAlertMsgs =  visitLevelAlertMsgs + message + "Rules are not adjusted for "+tpaName+ ".\n ";
				}else{
					visitLevelAlertMsgs = message + "Rules are not adjusted for "+tpaName+ ".\n ";
				}
			}
			adjustTaxAmts(adjTaxAmtsMap);

	}
	var multiPlanExists = planList.length > 1;
	setPatientAmounts(multiPlanExists);
	var args = response.argument;
	if (null != args && args.length >1) {
		args[0]._insta_transaction_token.value = args[1];
	}
	resetTotals(true, false);
}

function OnGetBillChargeClaimsFailure(){

}

function adjustTaxAmts(adjTaxAmtsMap){
	
	var table = document.getElementById("chargesTable");

	for (var id=0;id<getNumCharges();id++) {
		var chargeId = null;
		if(getIndexedValue("chargeId", id) != undefined)
			chargeId = getIndexedValue("chargeId", id);
		else
			chargeId = "_"+id;
		
		var chargeGroup = getIndexedValue("chargeGroupId", id)
		
		if(chargeGroup == 'MED' || chargeGroup == 'RET')
			continue;

		if(adjTaxAmtsMap[chargeId] != undefined){
			if(adjTaxAmtsMap[chargeId] == 'Y'){
				var row = table.rows[id+1];
				setNodeText(row.cells[TAX_COL], getIndexedValue("sponsor_tax", id));
				setHiddenValue(id, "tax_amt", getIndexedValue("sponsor_tax", id));
			}
		}		
	}	
}

function setSponsorAmounts(billChgClaimMap, priority){

	var table = document.getElementById("chargesTable");

	for (var id=0;id<getNumCharges();id++) {
		var chargeId = null;
		if(getIndexedValue("chargeId", id) != undefined)
			chargeId = getIndexedValue("chargeId", id);
		else
			chargeId = "_"+id;

		if(billChgClaimMap[chargeId] != undefined){
			var insClaimAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].insurance_claim_amt));
			var sponsorTax = formatAmountPaise(getPaise(billChgClaimMap[chargeId].tax_amt));
			var includeInClaimCalc = billChgClaimMap[chargeId].include_in_claim_calc == true ? 'Y' : 'N';
			var row = table.rows[id+1];
			if(priority == 1){
				setNodeText(row.cells[CLAIM_COL], insClaimAmt);
				setNodeText(row.cells[CLAIM_TAX], sponsorTax);
				setHiddenValue(id, "insClaimAmt", insClaimAmt);
				setHiddenValue(id, "sponsor_tax", sponsorTax);
				setHiddenValue(id, "priInsClaimAmt", insClaimAmt);
				setHiddenValue(id, "priInsClaimTaxAmt", sponsorTax);
				setHiddenValue(id, "priIncludeInClaim", includeInClaimCalc);
			}else{
				setNodeText(row.cells[SEC_CLAIM_COL], insClaimAmt);
				setHiddenValue(id, "secInsClaimAmt", insClaimAmt);
				setHiddenValue(id, "secInsClaimTaxAmt", sponsorTax);
				setHiddenValue(id, "secIncludeInClaim", includeInClaimCalc);
				
				var priTaxPaise = getPaise(getIndexedValue("priInsClaimTaxAmt", id));
				var secTaxPaise = getPaise(sponsorTax);
				var totSponsorTax = formatAmountPaise(priTaxPaise+secTaxPaise);
				setNodeText(row.cells[CLAIM_TAX], totSponsorTax);
				setHiddenValue(id, "sponsor_tax", totSponsorTax);
			}
		}
	}

}

function setPatientAmounts(multiPlanExists){
	var table = document.getElementById("chargesTable");

	for (var id=0;id<getNumCharges();id++) {

		var row = table.rows[id+1];

		var chargeGrp = getIndexedValue("chargeGroupId", id);
		if(chargeGrp == 'RET' || chargeGrp == 'MED')
			continue;

		var insClaimAmt = getIndexedValue("priInsClaimAmt", id);
		var priClaimAmtPaise = getPaise(insClaimAmt);
		var insClaimAmtPaise = priClaimAmtPaise;

		if(multiPlanExists){
			var secClaimAmtPaise = getPaise(getIndexedValue("secInsClaimAmt", id));
			insClaimAmtPaise = insClaimAmtPaise + secClaimAmtPaise;
		}
		var amtPaise = getPaise(getIndexedValue("amt", id));
		var patAmt = amtPaise - insClaimAmtPaise;
		
		var totTaxPaise = getPaise(getIndexedValue("tax_amt", id));
		var sponsorTaxPaise = getPaise(getIndexedValue("sponsor_tax", id));
		var patientTaxPaise = totTaxPaise - sponsorTaxPaise;

		setNodeText(row.cells[DED_COL], formatAmountPaise(patAmt));
		setHiddenValue(id, "insDeductionAmt", formatAmountPaise(patAmt));
		
		setNodeText(row.cells[DED_CLAIM_COL], formatAmountPaise(patientTaxPaise));
	}

}

function onClickProcessIns(salesform) {
	// Changed the method to use the new ajax form post method.
	// Once tested, can be used for hospital bill as well.

	var form = document.getElementById(salesform);
	var url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getMedicineSalesChargeClaims';
//	asyncPostForm(form, url, false,
//			OnGetMedicineSalesChargeClaims, OnGetMedicineSalesChargeClaimsFailure);

	var formToken = form._insta_transaction_token.value;
	form._insta_transaction_token.value="";
	
	var XHR = new XMLHttpRequest();
	XHR.open('POST', url, false);
	XHR.onreadystatechange = function (aEvt) {
		  if (XHR.readyState == 4) {
		     if(XHR.status == 200) {
		    	 OnGetMedicineSalesChargeClaims(XHR);
		     }
		     else {
		    	 OnGetMedicineSalesChargeClaimsFailure();
		     }
		  }
		};
	XHR.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	XHR.send(YAHOO.util.Connect.setForm(form));
	YAHOO.util.Connect.setForm();
	form._insta_transaction_token.value=formToken;
}




function OnGetMedicineSalesChargeClaims(response) {
	var multiPlanExists = false;
	if (response.responseText != undefined) {
		var planList = getpatPlanDetails();

		if (empty(planList)) {
			planList = [];
		}

		var billChgClaimsMap = eval('(' + response.responseText + ')');
		var adjTaxAmtsMap = billChgClaimsMap[-2];
		for(var j=0; j<planList.length; j++){
			var planId = planList[j].plan_id;
			var billChgClaimList = billChgClaimsMap[planId];
			setSalesSponsorAmounts(billChgClaimList, j+1);
		}
		var table = document.getElementById("medList");
		var noOfItems = getNumItems();
		for(var id=1;id<=noOfItems;id++) {
			var row = getRowObject(id);
			var priIncludeInClaim = getElementByName(row, 'priIncludeInClaim').value;
			var secIncludeInClaim = getElementByName(row, 'secIncludeInClaim').value;
			if((priIncludeInClaim == 'N' && secIncludeInClaim == 'N') || (priIncludeInClaim == 'N' && secIncludeInClaim == '')) {
				var insuranceFlagImg = row.cells[ITEM_COL].getElementsByTagName("img")[0];
				insuranceFlagImg.src = cpath + "/images/purple_flag.gif";
				setHiddenValue(row, 'cat_payable', 'f');
			}
		}
		multiPlanExists = planList.length > 1;
		adjustTaxAmtsSales(adjTaxAmtsMap, multiPlanExists);
	}
	setSalesPatientAmounts(multiPlanExists);

	setTotals();

	var args = response.argument;
	if (null != args && args.length >1) {
		args[0]._insta_transaction_token.value = args[1];
	}
}

function OnGetMedicineSalesChargeClaimsFailure() {

}

function setSalesSponsorAmounts(billChgClaimMap, priority) {

	var table = document.getElementById("medList");

	var noOfItems = getNumItems();
	for(var id=1;id<=noOfItems;id++) {
		var chargeId = null;
		//var charge = id+1;
		chargeId = "_"+id;
		if(billChgClaimMap[chargeId] == undefined)
			continue;

		var insClaimAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].insurance_claim_amt));
		var insClaimTaxAmt = 0;
		if(billChgClaimMap[chargeId].tax_amt != undefined && billChgClaimMap[chargeId].tax_amt != null && billChgClaimMap[chargeId].tax_amt !='')
			insClaimTaxAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].tax_amt));
		var includeInClaimCalc = billChgClaimMap[chargeId].include_in_claim_calc == true ? 'Y' : 'N';
		//var row = table.rows[id+1];
		var row = getRowObject(id);
		if(priority == 1){
			setNodeText(row.cells[PRIM_CLAIM_AMT_COL], parseFloat(parseFloat(insClaimAmt)+parseFloat(insClaimTaxAmt)).toFixed(decDigits));
			setNodeText(row.cells[PRIM_CLAIM_TAX_AMT_COL], insClaimTaxAmt);
			setHiddenValue(row, "claimAmt", insClaimAmt);
			setHiddenValue(row, "primclaimAmt", insClaimAmt);
			setHiddenValue(row, "priIncludeInClaim", includeInClaimCalc);
			setHiddenValue(row, "priClaimTaxAmt", insClaimTaxAmt);
		}else{
			setNodeText(row.cells[SEC_CLAIM_AMT_COL], parseFloat(parseFloat(insClaimAmt)+parseFloat(insClaimTaxAmt)).toFixed(decDigits));
			setNodeText(row.cells[SEC_CLAIM_TAX_AMT_COL], insClaimTaxAmt);
			setHiddenValue(row, "secclaimAmt", insClaimAmt);
			setHiddenValue(row, "secIncludeInClaim", includeInClaimCalc);
			setHiddenValue(row, "secClaimTaxAmt", insClaimTaxAmt);
		}
	}
}

function adjustTaxAmtsSales(adjTaxAmtsMap, multiPlanExists){
	
	var table = document.getElementById("medList");
	var noOfItems = getNumItems();
	for (var id=1; id<=noOfItems; id++) {
		var chargeId = null;
		chargeId = "_"+id;
	
		if(adjTaxAmtsMap[chargeId] != undefined){
			if(adjTaxAmtsMap[chargeId] == 'Y'){
				var row = getRowObject(id);
				
				var existingTaxAmount = parseFloat(getElementByName(row, 'tax').value);
				var existingAmount = parseFloat(getElementByName(row, 'amt').value);
				var amtWithoutTax = existingAmount - existingTaxAmount;
				
				var sponsorTaxAmt = parseFloat(getElementByName(row, 'priClaimTaxAmt').value);
				if(multiPlanExists) {
					sponsorTaxAmt += parseFloat(getElementByName(row, 'secClaimTaxAmt').value);
				}
				
				// Set Sponsor tax as total tax (For KSA)
				setNodeText(row.cells[TAX_COL], sponsorTaxAmt);
				setHiddenValue(row, "tax", sponsorTaxAmt);
				
				// Recalculate the Amount after TAX adj.
				setNodeText(row.cells[TOTAL_COL], amtWithoutTax + sponsorTaxAmt);
				setHiddenValue(row, "amt", amtWithoutTax + sponsorTaxAmt);
			}
		}		
	}	
}

function setSalesPatientAmounts(multiPlanExists){
	var table = document.getElementById("medList");

	var noOfItems = getNumItems();

	for (var id=1;id<=noOfItems;id++) {

		//var row = table.rows[id+1];
		var row = getRowObject(id);

		var insClaimAmt = getPaise(getElementByName(row, 'primclaimAmt').value);
		var insClaimTaxAmt = getPaise(getElementByName(row, 'priClaimTaxAmt').value);
		if(multiPlanExists){
			insClaimAmt = (insClaimAmt) + getPaise(getElementByName(row, 'secclaimAmt').value);
			insClaimTaxAmt = (insClaimTaxAmt) + getPaise(getElementByName(row, 'secClaimTaxAmt').value);
		}
		var amt = getElementByName(row, 'amt').value;
		var patAmt = getPaise(amt) - insClaimAmt;
		var totTaxPaise = getPaise(getElementByName(row, 'tax').value);
		patAmt = patAmt - totTaxPaise;
		
		var patientTaxPaise = totTaxPaise - insClaimTaxAmt;

		setNodeText(row.cells[PAT_AMT_COL], parseFloat(parseFloat(formatAmountPaise(patAmt))+parseFloat(formatAmountPaise(patientTaxPaise))).toFixed(decDigits));
		setNodeText(row.cells[PAT_TAX_AMT_COL], formatAmountPaise(patientTaxPaise));
		setHiddenValue(row, "patCalcAmt", formatAmountPaise((patAmt)));
		setHiddenValue(row, "patientTaxAmt", formatAmountPaise(patientTaxPaise));
	}

}

function onClickProcessInsForIssues(issueform, issueTo) {
	// Changed the method to use the new ajax form post method.
	// Once tested, can be used for hospital bill as well.
	var form = document.getElementById(issueform);
	var url;
	
	if(issueTo == 'P'){
		url = cpath + '/patientissues/getissueschargeclaims.json';
	}else{
		url = cpath + '/billing/ajaxCallOnAddingNewItem.do?_method=getIssuesChargeClaims';
	}
//	asyncPostForm(form, url, false,
//			OnGetIssuesChargeClaims, OnGetIssuesChargeClaimsFailure);
	
	if(form._insta_transaction_token) {
		var formToken = form._insta_transaction_token.value;
		form._insta_transaction_token.value="";
	}
	
	
	var XHR = new XMLHttpRequest();
	XHR.open('POST', url, false);
	XHR.onreadystatechange = function (aEvt) {
		  if (XHR.readyState == 4) {
		     if(XHR.status == 200) {
		    	 OnGetIssuesChargeClaims(XHR);
		     }
		     else {
		    	 OnGetIssuesChargeClaimsFailure();
		     }
		  }
		};
	XHR.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	XHR.send(YAHOO.util.Connect.setForm(form));
	YAHOO.util.Connect.setForm();
	if(form._insta_transaction_token) {
		form._insta_transaction_token.value=formToken;
	}
	
}

function OnGetIssuesChargeClaims(response) {
	var multiPlanExists;
	if (response.responseText != undefined) {
		var planList = patient.insurance;
		var billChgClaimsMap = eval('(' + response.responseText + ')');
		var adjTaxAmtsMap = billChgClaimsMap[-2];
		for(var j=0; j<planList.length; j++){
			var planId = planList[j].plan_id;
			var billChgClaimList = billChgClaimsMap[planId];
			setIssuesSponsorAmounts(billChgClaimList, j+1);
		}
		multiPlanExists = planList.length > 1;
		adjustTaxAmtsIssues(adjTaxAmtsMap, multiPlanExists);
	}
	setIssuesPatientAmounts(multiPlanExists);
	resetTotals();

	var args = response.argument;
	if (null != args && args.length >1) {
		args[0]._insta_transaction_token.value = args[1];
	}

}

function OnGetIssuesChargeClaimsFailure() {

}

function setIssuesSponsorAmounts(billChgClaimMap, priority) {

	var table = document.getElementById("itemListtable");

	var noOfItems = table.rows.length - 2;
	var insClaimAmt = 0;
	var insTaxAmt = 0;
	//getNumItems();

	for(var id=1;id<=noOfItems;id++) {
		var chargeId = null;

		//var charge = id+1;
		chargeId = "_"+id;
		var exist = billChgClaimMap[chargeId];
		//alert(exist+' , '+chargeId);
		if(exist == null || exist == undefined || exist == '')
			continue;

		insClaimAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].insurance_claim_amt));
		insTaxAmt = formatAmountPaise(getPaise(billChgClaimMap[chargeId].tax_amt));
		//var row = table.rows[id+1];
		var row = table.rows[id];

		if(priority == 1){
			setNodeText(document.getElementById("pri_totinsamtLabel"+id), parseFloat(insClaimAmt));
			document.getElementById("pri_ins_amt"+id).value = insClaimAmt;
			
			setNodeText(document.getElementById("pri_totinstaxLabel"+id), insTaxAmt);
			document.getElementById("pri_ins_tax"+id).value = insTaxAmt;
		}else{
			setNodeText(document.getElementById("sec_totinsamtLabel"+id), parseFloat(insClaimAmt));
			document.getElementById("sec_ins_amt"+id).value = insClaimAmt;
			
			setNodeText(document.getElementById("sec_totinstaxLabel"+id), insTaxAmt);
			document.getElementById("sec_ins_tax"+id).value = insTaxAmt;
		}
	}
}

function setIssuesPatientAmounts(multiPlanExists){
	var table = document.getElementById("itemListtable");

	var noOfItems = table.rows.length - 2;

	for (var id=1;id<=noOfItems;id++) {

		//var row = table.rows[id+1];
		var row = table.rows[id];

		var insClaimAmt = getPaise(getElementByName(row, 'pri_ins_amt').value);
		var insTaxAmt = getPaise(getElementByName(row, 'pri_ins_tax').value);
		if(multiPlanExists){
			insClaimAmt = (insClaimAmt) + getPaise(getElementByName(row, 'sec_ins_amt').value);
			insTaxAmt = (insTaxAmt) + getPaise(getElementByName(row, 'sec_ins_tax').value);
		}
		var amt = getElementByName(row, 'amt').value;
		var tax = getElementByName(row, 'tax_amt').value;
		var patAmt = getPaise(amt) - insClaimAmt;
		var patTax = getPaise(tax) - insTaxAmt;
		setNodeText(document.getElementById("totpatamtLabel"+id), formatAmountPaise(patAmt));
		setNodeText(document.getElementById("totpattaxLabel"+id), formatAmountPaise(patTax));
		//setHiddenValue(document.getElementById("patIncClaimAmt"+id), formatAmountPaise(getPaise(patAmt)));
	}

}

function adjustTaxAmtsIssues(adjTaxAmtsMap, multiPlanExists){
	
	var table = document.getElementById("itemListtable");
	var noOfItems = table.rows.length - 2;
	for (var id=1; id<=noOfItems; id++) {
		var chargeId = null;
		chargeId = "_"+id;
	
		if(adjTaxAmtsMap[chargeId] != undefined){
			if(adjTaxAmtsMap[chargeId] == 'Y'){
				var row = table.rows[id];
				var sponsorTaxAmt = parseFloat(getElementByName(row, 'pri_ins_tax').value);
				if(multiPlanExists) {
					sponsorTaxAmt += parseFloat(getElementByName(row, 'sec_ins_tax').value);
				}
				
				// Set Sponsor tax as total tax (For KSA)
				setNodeText(document.getElementById("taxamtLabel"+id), sponsorTaxAmt.toFixed(decDigits));
				document.getElementById("tax_amt"+id).value = sponsorTaxAmt;
				
			}
		}		
	}	
}

