var pkgMarginPaise = 0;
var pkgIncludedPaise = 0;
var packagePaise = 0;
var dynaPackage = {};

function getDynaPackageDetailsList(packageId) {
	var xhttp = new XMLHttpRequest();
	var url = cpath + '/billing/BillAction.do?_method=getDynaPackageDetails&dynaPackageId=' + packageId + '&bedType=' + patientBedType+'&orgId=' + billOrgId;
	xhttp.open("GET", url, false);
	xhttp.send(null);
	if (xhttp.readyState == 4) {
		if ( (xhttp.status == 200) && (xhttp.responseText != null ) ) {
			return eval('('+xhttp.responseText+')');
		}
	}
	return null;
	
}

function setPkgValueCaps() {

	pkgMarginPaise = 0;
	pkgIncludedPaise = 0;
	packagePaise = 0;

	var packageId = mainform.dynaPkgId.value;

	if (packageId != '' && packageId != 0) {

		var packageDetList;
		if (dynaPackage[packageId]) {
			packageDetList = dynaPackage[packageId];
		} else {
			packageDetList = getDynaPackageDetailsList(packageId);
			dynaPackage[packageId] = packageDetList;
		}

		var table = document.getElementById("packageDetailsTab");
		var templateRow = createTemplateRowAfterDelete();

		if (!empty(packageDetList) && packageDetList.length > 0) {
			for (var i = 0; i < packageDetList.length; i++) {
				var categoryName	= packageDetList[i].dyna_pkg_cat_name;
				var amtLimit 		= formatAmountPaise(getPaise(packageDetList[i].amount_limit));
				var qtyLimit		= formatAmountValue(packageDetList[i].qty_limit, true);
				var limitType		= packageDetList[i].limit_type;

				// Display only those categories which are included in package.
				if (packageDetList[i].pkg_included == 'N')
					continue;

				var pkgIncluded	= (packageDetList[i].pkg_included == 'Y') ? 'Yes' : 'No';

				var el = templateRow.cells[0];
				setNodeText(el, categoryName + (limitType == 'U' ? '\n (Unlimited)' : ''));
				el = templateRow.cells[1];
				setNodeText(el, amtLimit);
				el = templateRow.cells[2];
				setNodeText(el, qtyLimit);
				el = templateRow.cells[3];
				setNodeText(el, pkgIncluded);

				if (i+1 < packageDetList.length) {
					var nextRow = table.rows[i+2];
					var row = templateRow.cloneNode(true);
					table.tBodies[0].insertBefore(row, nextRow);
					templateRow = row;
				}
			}
		}

		packagePaise = getElementIdPaise("dynaPkgCharge");

		var pkgMarginRowId = getPackageMarginRowId();

		if (pkgMarginRowId != null) {

			pkgMarginPaise = getIndexedPaise("amt",pkgMarginRowId);

			for (var i=0;i<getNumCharges();i++) {

				var delCharge = getIndexedFormElement(mainform, "delCharge", i);
				var amtIncluded = getIndexedPaise("amount_included", i);

				if (delCharge && "true" == delCharge.value)
					continue;

				if (getIndexedFormElement(mainform, "chargeHeadId", i).value == 'MARPKG')
					continue;

				pkgIncludedPaise += amtIncluded;
			}
		}
	} else {
		createTemplateRowAfterDelete();
	}
}

function createTemplateRowAfterDelete() {
	var table = document.getElementById("packageDetailsTab");
	var rowCount = table.rows.length;
	for (var i = 2; i < rowCount; i++) {
		table.deleteRow(i);
		rowCount--;
		i--;
	}

	var templateRow = table.rows[1];
	for (var c=0; c<=4; c++) {
		var el = templateRow.cells[c];
		setNodeText(el, '');
	}

	return templateRow;
}

function validatePackage() {
	var packageId = mainform.dynaPkgId.value;
	var packageCharge = getElementIdPaise("dynaPkgCharge");

	if (packageId == '' || packageId == 0) {
		showMessage("js.billing.dynapackage.enterpkgname");
		mainform.dynaPkgName.focus();
		return false;
	}

	if (packageCharge == '') {
		mainform.dynaPkgCharge.value = 0;
	}
	return true;
}

function validateDynaPkg() {
	var packageId = mainform.dynaPkgId != null ? mainform.dynaPkgId.value : '';
	if (packageId != '' && packageId != 0) {
		var package = findInList(jDynaPkgNameList, "dyna_package_id", packageId);
		if (origBillStatus == 'A' && package.status == 'I') {
			var ok = confirm("  Warning: Dyna Package : "+package.dyna_package_name+" is inactive. \n "
			 				 +" Do you want to continue with inactive package?");
			if (!ok) {
				mainform.dynaPkgName.focus();
				return false;
			}
		}
	}
	return true;
}

function resetPackageExcludedCharges() {
	for (var i=0;i<getNumCharges();i++) {
		var amtIncluded = getIndexedFormElement(mainform, "amount_included", i);
		var qtyIncluded = getIndexedFormElement(mainform, "qty_included", i);

		var origAmtInc = amtIncluded.value;
		var origQtyInc = qtyIncluded.value;

		var delCharge  = getIndexedFormElement(mainform, "delCharge", i);

		if (delCharge && "true" == delCharge.value)
			continue;

		if (getPaise(origAmtInc) == 0 && getAmount(origQtyInc) == 0)
			continue;

		setIndexedValue("edited", i, 'true');
		setIndexedValue("chargeExcluded", i, 'N');
		setIndexedValue("dynaPackageExcluded", i, 'N');

		setIndexedValue("amount_included", i, 0);
		setIndexedValue("qty_included", i, 0);

		setRowStyle(i);
		chargesEdited++;
	}
}

function setPackageMarginAmount() {

	pkgMarginPaise = packagePaise - pkgIncludedPaise;

	var pkgMarginRowId = getPackageMarginRowId();
	var chargeExcluded = getIndexedValue("chargeExcluded", pkgMarginRowId);

	if (pkgMarginRowId != null && chargeExcluded == 'N') {
		var pkgMargin = formatAmountPaise(pkgMarginPaise);

		var row = getChargeRow(pkgMarginRowId);
		var oldMarginPaise = getIndexedPaise("amt",pkgMarginRowId);

		if (oldMarginPaise != pkgMarginPaise) {
			setNodeText(row.cells[RATE_COL], pkgMargin);
			setNodeText(row.cells[AMT_COL], pkgMargin);

			setIndexedValue("rate", pkgMarginRowId, pkgMargin);
			setIndexedValue("amt", pkgMarginRowId, pkgMargin);
			setIndexedValue("amount_included", pkgMarginRowId, pkgMargin);

			if (typeof(CLAIM_COL) != 'undefined') {
				setNodeText(row.cells[CLAIM_COL], pkgMargin);
				setIndexedValue("insClaimAmt", pkgMarginRowId, pkgMargin);
				setIndexedValue("priInsClaimAmt", pkgMarginRowId, pkgMargin);
			}

			setIndexedValue("edited", pkgMarginRowId, 'true');
			setRowStyle(pkgMarginRowId);
			chargesEdited++;
		}
	}
}

function onChangePkgAmtQty(origAmtIncValue, origQtyIncValue, id, noResetTotals) {
	var amtIncluded = getIndexedFormElement(mainform, "amount_included", id);
	var qtyIncluded = getIndexedFormElement(mainform, "qty_included", id);

	if ((getPaise(amtIncluded.value) != getPaise(origAmtIncValue))
		|| (getAmount(qtyIncluded.value) != getAmount(origQtyIncValue))) {

		pkgIncludedPaise -= getPaise(origAmtIncValue.value); // deduct old amount
		pkgIncludedPaise += getPaise(amtIncluded.value); // include new amount

		if(!noResetTotals)
			resetTotals(true, false);
	}
}


function includeIntoDynaPkg() {
	if (!validatePackage())
		return false;

	if (!selectedItemsForDiscount()) {
		showMessage("js.billing.dynapackage.selectchargetoinclude");
		return false;
	}

	var packageId = mainform.dynaPkgId != null ? mainform.dynaPkgId.value : '';
	if (packageId != '' && packageId != 0)
		var package = findInList(jDynaPkgNameList, "dyna_package_id", packageId);

	var disCheckElmts = document.mainform.discountCheck;
	for(var i=0;i<disCheckElmts.length;i++) {
		if (disCheckElmts[i].checked) {
			var claimAmounts = [];
			var row = getThisRow(disCheckElmts[i]);
			var rowId = getRowChargeIndex(row);
			var remainingAmt = getIndexedValue("amount", rowId);
			var chargeHead = getIndexedValue("chargeHeadId",rowId);
			var insPayable = findInList(jChargeHeads, "CHARGEHEAD_ID", chargeHead).INSURANCE_PAYABLE;

			if(package.excluded_amt_claimable == 'N' && isTpa){
				if(planList.length>0){
					for(var j=0; j<planList.length; j++){
						var discAmt = j == 0 ? getIndexedValue("discount",rowId) : 0.00;

						var claimAmt = calculateClaimAmount(remainingAmt, discAmt,
									getIndexedValue("insuranceCategoryId",rowId),
									getIndexedValue("firstOfCategory", rowId),visitType,
									document.mainform.billNo.value,planList[j].plan_id, insPayable);
						remainingAmt = remainingAmt - claimAmt;
						claimAmounts[j] = claimAmt;
					}
					setNodeText(row.cells[CLAIM_COL], formatAmountPaise(getPaise(claimAmounts[0])));
					setNodeText(row.cells[DED_COL], formatAmountPaise(getPaise(getIndexedValue("amount", rowId)-claimAmounts[0])));
					setHiddenValue(rowId, "insClaimAmt", formatAmountPaise(getPaise(claimAmounts[0])));
					setHiddenValue(rowId, "priInsClaimAmt", formatAmountPaise(getPaise(claimAmounts[0])));
					if(planList.length > 1) {
						setNodeText(row.cells[SEC_CLAIM_COL], formatAmountPaise(getPaise(claimAmounts[1])));
						setHiddenValue(rowId, "secInsClaimAmt", formatAmountPaise(getPaise(claimAmounts[1])));
					}
				}
				else {
					if(sponserType=='N' || sponserType =='C' || (sponserType == 'I' && planList.length==0)){
						setNodeText(row.cells[CLAIM_COL], formatAmountPaise(getPaise(getIndexedValue("amount", rowId))));
						setNodeText(row.cells[DED_COL], formatAmountPaise(getPaise(0)));
						setHiddenValue(rowId, "insClaimAmt", formatAmountPaise(getPaise(getIndexedValue("amount", rowId))));
						setHiddenValue(rowId, "priInsClaimAmt", formatAmountPaise(getPaise(getIndexedValue("amount", rowId))));
					}
				}
			}

			var amtIncluded = getIndexedFormElement(mainform, "amount_included", rowId);
			var qtyIncluded = getIndexedFormElement(mainform, "qty_included", rowId);

			var origAmtInc = amtIncluded.value;
			var origQtyInc = qtyIncluded.value;

			var amountToIncludePaise = (isTpa) ? getIndexedPaise("insClaimAmt", rowId) : getIndexedPaise("amt", rowId);
			
			if ( chargeHead == 'INVITE' ) {
				amountToIncludePaise = getIndexedPaise("amt", rowId);
			}

			if(planList.length > 1 && isTpa) {
				amountToIncludePaise = amountToIncludePaise + getIndexedPaise("secInsClaimAmt", rowId);
			}

			var amountToInclude = formatAmountPaise(amountToIncludePaise);

			var chQty = getIndexedValue("qty", rowId);

			if (isChargeAmountIncludedEditable(rowId)) {

				setIndexedValue("amount_included", rowId, amountToInclude);
				setIndexedValue("qty_included", rowId, 0);

			}else if (isChargeQuantityIncludedEditable(rowId)) {
				setIndexedValue("qty_included", rowId, chQty);
				setIndexedValue("amount_included", rowId, amountToInclude);
			}
			
			var chargeHead = getIndexedValue("chargeHeadId", rowId);
			if (chargeHead == 'BIDIS' || chargeHead == 'ROF' || chargeHead == 'CSTAX') {
				setIndexedValue("chargeExcluded", rowId, "Y");

			}else {
				var chargeExcluded = "P";
				if (amtIncluded != null) {
					if (getPaise(amtIncluded.value) == amountToIncludePaise)
						chargeExcluded = "N";
					else if (getPaise(amtIncluded.value) == 0)
						chargeExcluded = "Y";
				}

				setIndexedValue("chargeExcluded", rowId, chargeExcluded);
				setIndexedValue("dynaPackageExcluded", rowId, chargeExcluded);
			}

			onChangePkgAmtQty(origAmtInc, origQtyInc, rowId, true);
			setIndexedValue("edited", rowId, 'true');
			setRowStyle(rowId);
			chargesEdited++;
			
			if ( chargeHead == 'INVITE' ) {
				//include returns proportionately
				includeReferenceCharges(getIndexedValue("chargeId",rowId),100);
			}
		}
	 }
	 resetTotals(true,false);
}

function excludeFromDynaPkg() {
	if (!validatePackage())
		return false;

	if (!selectedItemsForDiscount()) {
		showMessage("js.billing.dynapackage.selectchargetoexclude");
		return false;
	}

	var packageId = mainform.dynaPkgId != null ? mainform.dynaPkgId.value : '';
	if (packageId != '' && packageId != 0)
		var package = findInList(jDynaPkgNameList, "dyna_package_id", packageId);

	var disCheckElmts = document.mainform.discountCheck;
	for(var i=0;i<disCheckElmts.length;i++) {
		if (disCheckElmts[i].checked) {

			var row = getThisRow(disCheckElmts[i]);
			var rowId = getRowChargeIndex(row);

			if(package.excluded_amt_claimable == 'N' && isTpa){
				setNodeText(row.cells[CLAIM_COL], formatAmountPaise(getPaise(0)));
				setNodeText(row.cells[DED_COL], getIndexedValue("amount", rowId));
				setHiddenValue(rowId, "insClaimAmt", formatAmountPaise(getPaise(0)));
				setHiddenValue(rowId, "priInsClaimAmt", formatAmountPaise(getPaise(0)));

				if(planList.length > 1){
					setNodeText(row.cells[SEC_CLAIM_COL], formatAmountPaise(getPaise(0)));
					setHiddenValue(rowId, "secInsClaimAmt", formatAmountPaise(getPaise(0)));
				}
			}

			var amtIncluded = getIndexedFormElement(mainform, "amount_included", rowId);
			var qtyIncluded = getIndexedFormElement(mainform, "qty_included", rowId);
			var chargeHead = getIndexedValue("chargeHeadId", rowId);

			var origAmtInc = amtIncluded.value;
			var origQtyInc = qtyIncluded.value;

			if (isChargeAmountIncludedEditable(rowId) || isChargeQuantityIncludedEditable(rowId)) {

				setIndexedValue("amount_included", rowId, 0);
				setIndexedValue("qty_included", rowId, 0);

				setIndexedValue("chargeExcluded", rowId, "Y");
				setIndexedValue("dynaPackageExcluded", rowId, "Y");

				onChangePkgAmtQty(origAmtInc, origQtyInc, rowId, true);
				setIndexedValue("edited", rowId, 'true');
				setRowStyle(rowId);
				chargesEdited++;
			}
		}
		
		if ( chargeHead == 'INVITE' ) {
			//include returns proportionately
			excludeReferenceCharges(getIndexedValue("chargeId",rowId),package,i)
		}
	}
	resetTotals(true, false);
}

function onChangePkgCharge() {
	if (!validatePackage())
		return false;

	var oldPkgPaise = getElementIdPaise("oldDynaPkgCharge");
	var newPkgPaise = getElementIdPaise("dynaPkgCharge");

	if (!chkPkgRateVariation())
		return false;

	packagePaise = newPkgPaise;
	if (oldPkgPaise != newPkgPaise)
		resetTotals(true, false);

	mainform.oldDynaPkgCharge.value = newPkgPaise;
	return true;
}

function processPackageCharges() {
	if (!validatePackage())
		return false;

	if (chargesAdded > 0 || chargesEdited > 0) {
		showMessage("js.billing.dynapackage.newchargesaddedoredited.savebeforeprocessing");
		return false;
	}

	/*if ((!empty(dynaPackageProcessed) && dynaPackageProcessed =='Y')
		|| (document.mainform.dynaPkgProcessed != null && trim(document.mainform.dynaPkgProcessed.value) == 'Y')) {

		var ok = confirm("  Warning: Package process is done already. \n "
			  +" Processing again will change previous values \n "
			  +" and new package process will be done. \n\n "
			  +" Do you want to proceed anyway?");
		if (!ok)
			return false;
	}*/

	if (dynaPkgProcessedBefore == 'true') {

		var ok = confirm("  Warning: Package process is done before. \n "
			  +" Now, new package process will be done. \n "
			  +(visitType == 'i' ? " Pkg Excluded bed charges will be deleted. \n " : "")
			  +" The previous values will be changed. \n "
			  +" Do you want to proceed anyway?");
		if (!ok)
			return false;
	}

	document.mainform.buttonAction.value = 'process';
	document.mainform.submit();
	return true;
}

function clearPackageCharges() {
	resetPackageExcludedCharges();
	resetTotals(true, false);
	return true;
}

function clearDynaPkgId() {
	document.mainform.dynaPkgId.value = '';
	document.mainform.dynaPkgCharge.value = '';

	var pkgMarginRowId = getPackageMarginRowId();

	if (pkgMarginRowId != null) {
		cancelCharge(getIndexedFormElement(mainform, "chargeHeadId", pkgMarginRowId));
		clearPackageCharges();
		if (document.mainform.dynaPkgId.value != '' && document.mainform.dynaPkgId.value != 0)
			setIndexedValue("delCharge", pkgMarginRowId, "false");
		else
			setIndexedValue("delCharge", pkgMarginRowId, "true");

		setIndexedValue("edited", pkgMarginRowId, 'true');
		setRowStyle(pkgMarginRowId);
		if (document.mainform.printBill)
			setSelectedIndex(document.mainform.printBill, billPrintDefault);
	}

	document.getElementById("dynaPkgBtnTable").style.display = "none";
	document.getElementById("dynaPkgFilterTable").style.display = "none";
	document.mainform.dynaPkgProcessed.value = "";
	setPkgValueCaps();
	setPackageMarginAmount();
}

function getPackageMarginRowId(cancelled) {
	if (cancelled) {
		var headRowId = null;
		for (var i=0;i<getNumCharges();i++) {
			if (getIndexedFormElement(mainform, "chargeHeadId", i).value == 'MARPKG') {
				headRowId = i;
				break;
			}
		}
		return headRowId;
	}
	return getChargeHeadRowId('MARPKG');
}

function initDynaPackageNames(pkgList) {
	var ds = new YAHOO.util.LocalDataSource(pkgList, { queryMatchContains : true });
	ds.responseSchema = { resultsList : "dynaPkgs",
		fields: [ {key : "dyna_package_name"}, {key: "dyna_package_id"}, {key: "charge"}, {key: "item_code"}]
	};

	var autoComp = new YAHOO.widget.AutoComplete("dynaPkgName", 'dynaPkgName_dropdown', ds);

	autoComp.typeAhead = false;
	autoComp.useShadow = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = true;
	autoComp.forceSelection = true;
	autoComp.animVert = false;
	autoComp._bItemSelected = true;
	autoComp.filterResults = Insta.queryMatchWordStartsWith;
	autoComp.formatResult = Insta.autoHighlightWordBeginnings;
	autoComp.selectionEnforceEvent.subscribe(clearDynaPkgId);
	autoComp.itemSelectEvent.subscribe(onChangeDynaPkgName);
	return autoComp;
}

function onChangeDynaPkgName(sType, aArgs) {
	var oldPkgId = document.mainform.dynaPkgId.value;
	var package = aArgs[2];
	document.mainform.dynaPkgId.value = package[1];
	document.mainform.dynaPkgCharge.value = package[2];
	document.mainform.oldDynaPkgCharge.value = package[2];

	var dynaPkgName = package[0];
	var ratePlanCode = package[3];

	if (!empty(dynaPkgName)) {
		document.getElementById("dynaPkgBtnTable").style.display = "table";
		document.getElementById("dynaPkgFilterTable").style.display = "table";
	}else {
		document.getElementById("dynaPkgBtnTable").style.display = "none";
		document.getElementById("dynaPkgFilterTable").style.display = "none";
	}

	if (allowRateDcr == 'A' || allowRateIncr == 'A')
		document.mainform.dynaPkgCharge.readOnly = false;
	else
		document.mainform.dynaPkgCharge.readOnly = true;

	var pkgMarginRowId = getPackageMarginRowId(true);

	if (pkgMarginRowId == null) {
		addToTable("PKG", "MARPKG", document.mainform.dynaPkgId.value, dynaPkgName,
				0, 1, "", ratePlanCode);

		var rowId = getNumCharges()-1;
		var row = getChargeRow(rowId);

		var amtIncluded = getIndexedFormElement(mainform, "amount_included", rowId);
		var qtyIncluded = getIndexedFormElement(mainform, "qty_included", rowId);

		var origAmtInc = amtIncluded.value;
		var origQtyInc = qtyIncluded.value;

		setIndexedValue("chargeExcluded", rowId, 'N');
		setIndexedValue("amt", rowId, 0);
		setIndexedValue("qty", rowId, 0);
		setIndexedValue("amount_included", rowId, 0);
		setIndexedValue("qty_included", rowId, 0);

		setNodeText(row.cells[AMT_COL], formatAmountPaise(0));
		if (tpaBill) {
			setIndexedValue("insClaimAmt", rowId, 0);
			setIndexedValue("priInsClaimAmt", rowId, 0);

			setNodeText(row.cells[CLAIM_COL], formatAmountPaise(0));
		}
		setNodeText(row.cells[QTY_COL], formatAmountValue(0));

		onChangePkgAmtQty(origAmtInc, origQtyInc, rowId, true);

	}else if (oldPkgId != package[1]) {
		var row = getChargeRow(pkgMarginRowId);
		setNodeText(row.cells[DESC_COL], dynaPkgName, 30);
		setIndexedValue("descriptionId", pkgMarginRowId, document.mainform.dynaPkgId.value);
		setIndexedValue("description", pkgMarginRowId, dynaPkgName);

		setNodeText(row.cells[CODE_COL], ratePlanCode, 10);
		setIndexedValue("actRatePlanItemCode", pkgMarginRowId, ratePlanCode);

		setIndexedValue("edited", pkgMarginRowId, 'true');

		if (document.mainform.dynaPkgId.value != '' && document.mainform.dynaPkgId.value != 0)
			setIndexedValue("delCharge", pkgMarginRowId, "false");
		else
			setIndexedValue("delCharge", pkgMarginRowId, "true");

		var amtIncluded = getIndexedFormElement(mainform, "amount_included", pkgMarginRowId);
		var qtyIncluded = getIndexedFormElement(mainform, "qty_included", pkgMarginRowId);

		var origAmtInc = amtIncluded.value;
		var origQtyInc = qtyIncluded.value;

		setIndexedValue("chargeExcluded", pkgMarginRowId, 'N');
		setIndexedValue("amt", pkgMarginRowId, 0);
		setIndexedValue("qty", pkgMarginRowId, 0);
		setIndexedValue("amount_included", pkgMarginRowId, 0);
		setIndexedValue("qty_included", pkgMarginRowId, 0);

		setNodeText(row.cells[AMT_COL], formatAmountPaise(0));
		if (tpaBill) {
			setIndexedValue("insClaimAmt", pkgMarginRowId, 0);
			setIndexedValue("priInsClaimAmt", pkgMarginRowId, 0);
			setNodeText(row.cells[CLAIM_COL], formatAmountPaise(0));
		}
		setNodeText(row.cells[QTY_COL], formatAmountValue(0));

		onChangePkgAmtQty(origAmtInc, origQtyInc, pkgMarginRowId), true;

		setRowStyle(pkgMarginRowId);
		chargesEdited++;
		//processPackageCharges();
	}
	filterCharges();
	document.mainform.dynaPkgProcessed.value = "N";
	if (document.mainform.printBill)
		setSelectedIndex(document.mainform.printBill, 'CUSTOM-BUILTIN_HTML');
	resetTotals(true, false);
}

function chkPkgRateVariation() {

	var oldPkgPaise = getElementIdPaise("oldDynaPkgCharge");
	var newPkgPaise = getElementIdPaise("dynaPkgCharge");
	var pkgChrgObj = document.mainform.dynaPkgCharge;

	if ( newPkgPaise > oldPkgPaise && (allowRateIncr != 'A') ) {
	 	var msg=getString("js.billing.dynapackage.notauthorized.increasethepackagerateabove");
	 	msg+=formatAmountPaise(oldPkgPaise);
	 	alert(msg);
	 	pkgChrgObj.value = formatAmountPaise(oldPkgPaise);
	 	pkgChrgObj.focus();
	 	return false;
 	}

 	if ( newPkgPaise < oldPkgPaise && (allowRateDcr != 'A') ) {
    	var msg=getString("js.billing.dynapackage.notauthorized.decreasethepackageratebelow");
    	msg+=formatAmountPaise(oldPkgPaise);
    	alert(msg);
       	pkgChrgObj.value = formatAmountPaise(oldPkgPaise);
	 	pkgChrgObj.focus();
	 	return false;
 	}
 	return true;
}

function initDynaPkgValueCapDialog() {

	var dialogDiv = document.getElementById("dynavalueCapDialog");
	dialogDiv.style.display = 'block';
	dynavalueCapDialog = new YAHOO.widget.Dialog("dynavalueCapDialog",{
			width:"400px",
			text: "Package Value Cap",
			context :["btnValueCap", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onCancel,
	                                                scope:dynavalueCapDialog,
	                                                correctScope:true } );
	dynavalueCapDialog.cfg.queueProperty("keylisteners", escKeyListener);
	dynavalueCapDialog.render();

}

function showValueCapDialog() {
	dynavalueCapDialog.cfg.setProperty("context", ["dynavalueCapDialog", "tl", "tl"], false);
	dynavalueCapDialog.show();
}

function onCancel() {
	valueCapDialog.hide();
}

function includeReferenceCharges(chargeId,percentage){
	
	var referenceCharges = filterList(jVisitIssueReturnReferences, 'issue_charge_id', chargeId);
	for(var returns=0;returns<referenceCharges.length;returns++) {
		
		var disCheckElmts = document.mainform.discountCheck;
		for(var index=0;index<disCheckElmts.length;index++) {
			var claimAmounts = [];
			var row = getThisRow(disCheckElmts[index]);
			var rowId = getRowChargeIndex(row);
			var remainingAmt = getIndexedValue("amount", rowId);
			var chargeHead = getIndexedValue("chargeHeadId",rowId);
			var chargeid = getIndexedValue("chargeId",rowId);
			
			if ( referenceCharges[returns].return_charge_id != chargeid ){
				continue;
			}
			var insPayable = findInList(jChargeHeads, "CHARGEHEAD_ID", chargeHead).INSURANCE_PAYABLE;

			var amtIncluded = getIndexedFormElement(mainform, "amount_included", rowId);
			var qtyIncluded = getIndexedFormElement(mainform, "qty_included", rowId);

			var origAmtInc = amtIncluded.value;
			var origQtyInc = qtyIncluded.value;

			var amountToIncludePaise =  getIndexedPaise("amt", rowId);

			var amountToInclude = formatAmountPaise(amountToIncludePaise*percentage/100);

			var chQty = getIndexedValue("qty", rowId);

			if (isChargeAmountIncludedEditable(rowId)) {

				setIndexedValue("amount_included", rowId, amountToInclude);
				setIndexedValue("qty_included", rowId, 0);

			}else if (isChargeQuantityIncludedEditable(rowId)) {
				setIndexedValue("qty_included", rowId, chQty);
				setIndexedValue("amount_included", rowId, amountToInclude);
			}
			
			var chargeHead = getIndexedValue("chargeHeadId", rowId);
			if (chargeHead == 'BIDIS' || chargeHead == 'ROF' || chargeHead == 'CSTAX') {
				setIndexedValue("chargeExcluded", rowId, "Y");
				setIndexedValue("dynaPackageExcluded", rowId, "Y");

			}else {
				var chargeExcluded = "P";
				if (amtIncluded != null) {
					if (getPaise(amtIncluded.value) == amountToIncludePaise && percentage != 0)
						chargeExcluded = "N";
					else if (getPaise(amtIncluded.value) == 0)
						chargeExcluded = "Y";
				}

				setIndexedValue("chargeExcluded", rowId, chargeExcluded);
				setIndexedValue("dynaPackageExcluded", rowId, chargeExcluded);
			}

			onChangePkgAmtQty(origAmtInc, origQtyInc, rowId);
			setIndexedValue("edited", rowId, 'true');
			setRowStyle(rowId);
			chargesEdited++;
		}
	 }
}

function excludeReferenceCharges(chargeId,package,i){

	var referenceCharges = filterList(jVisitIssueReturnReferences, 'issue_charge_id', chargeId);
	for(var returns=0;returns<referenceCharges.length;returns++) {
		
		var disCheckElmts = document.mainform.discountCheck;
		for(var index=0;index<disCheckElmts.length;index++) {

			var row = getThisRow(disCheckElmts[index]);
			var rowId = getRowChargeIndex(row);
			var chargeid = getIndexedValue("chargeId",rowId);
			var chargeHead = getIndexedValue("chargeHeadId", rowId);
			
			if ( referenceCharges[returns].return_charge_id != chargeid ){
				continue;
			}

			if(package.excluded_amt_claimable == 'N' && isTpa){
				setNodeText(row.cells[CLAIM_COL], formatAmountPaise(getPaise(0)));
				setNodeText(row.cells[DED_COL], getIndexedValue("amount", rowId));
				setHiddenValue(rowId, "insClaimAmt", formatAmountPaise(getPaise(0)));
				setHiddenValue(rowId, "priInsClaimAmt", formatAmountPaise(getPaise(0)));

				if(planList.length > 1){
					setNodeText(row.cells[SEC_CLAIM_COL], formatAmountPaise(getPaise(0)));
					setHiddenValue(rowId, "secInsClaimAmt", formatAmountPaise(getPaise(0)));
				}
			}

			var amtIncluded = getIndexedFormElement(mainform, "amount_included", rowId);
			var qtyIncluded = getIndexedFormElement(mainform, "qty_included", rowId);

			var origAmtInc = amtIncluded.value;
			var origQtyInc = qtyIncluded.value;

			if (isChargeAmountIncludedEditable(rowId) || isChargeQuantityIncludedEditable(rowId)) {

				setIndexedValue("amount_included", rowId, 0);
				setIndexedValue("qty_included", rowId, 0);

				setIndexedValue("chargeExcluded", rowId, "Y");
				setIndexedValue("dynaPackageExcluded", rowId, "Y");

				onChangePkgAmtQty(origAmtInc, origQtyInc, rowId);
				setIndexedValue("edited", rowId, 'true');
				setRowStyle(rowId);
				chargesEdited++;
			}
		}
	}
}