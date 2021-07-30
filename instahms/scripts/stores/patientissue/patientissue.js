var oItemAutoComp;
var kitdetails;
var deletedrows;
var gDefaultDiscountPer=0;
var gDefaultDiscountType="";
var ratePer=0;
var gIndex = 0;
var itemNamesArray = '';
var storeRatePlanId = 0;
var Hospital_field = null;
var gRatePlanId = 0;
var gBedType = '';
var oOrderKitAutoComp;
var gOrderKitItems;
var gItemType = 'itemname';
var gUseBathMrp = false;
var gStoreSaleUnit = 'I';
var visitOpenBills = [];
var itemBatchDetails;
var exp_dts = {};
var qty_avbl = {};
var item_ids = {};
var qty_available = 0;
var identification_type = {};
var issueUnits = {};
var pkgSizes = {};
var item_billable = {};
var mrp = {};
var pkgUOMs = {};
var itemBatchIds = {};
var avlQty = {};
var categoryIds = {};
var controlTypeIds = {};
var controlTypeNames = {};
var addedItem = {};
var addedIdentifier = {};
var gItemTaxGroups = {};
var gItemTaxSubGroups = {};
var stock_type = {};
var isPkg = false;

function init(){
	if(visitId != undefined && visitId != '')
		setFieldValue('mrno', visitId);
	getSubgroups();
	taxGroupInit();
	initpatient();
	reInit(getField('mrno'));
	checkStoreAllocation();
	getPatientFromBill();
	getReport(message, gtPass, type, 'Patient', billNo);
}

function initpatient(){
	isBatchMrpEnabled(gStoreId);
	Insta.initVisitAcSearch(cpath, "mrno", "mrnoContainer", 'active','all',
		function(type, args) { getPatientDetails(); },
		function(type, args) { clearPatientDetails(); });
	initDialog();
	initOrderKitAutoComplete();
	initLoginDialog();
}

function isBatchMrpEnabled(storeId) {
	for (var i=0;i<storesJSON.length;i++) {
		if (storesJSON[i].dept_id == parseInt(storeId)) {
			gUseBathMrp = storesJSON[i].use_batch_mrp === 'Y';
			gStoreSaleUnit = storesJSON[i].sale_unit;
		}
	}
}

function getSubgroups(){
	var url = cpath + "/patientissues/taxgroups.json";
	var response = ajaxGetFormObj(null, url, false);
	if(response && response != null) {
		gItemTaxGroups = response.item_groups?response.item_groups:'';
		gItemTaxSubGroups = response.item_subgroups?response.item_subgroups:'';
	}
}

function taxGroupInit() {
	var labelNameOptions = '';
	var count = 1;
	for(var key in gItemTaxGroups) {
		if (gItemTaxGroups.hasOwnProperty(key)) {
			// Set Groupname label.
			labelNameOptions += '<td class="formlabel">';
			labelNameOptions += gItemTaxGroups[key].item_group_name;
			labelNameOptions += ':</td>';
			labelNameOptions += '<td class="forminfo"';
			labelNameOptions += ' colspan="1">';
			labelNameOptions += '<select name="subgroups" id="ed_taxsubgroupid'+gItemTaxGroups[key].item_group_id+'" onchange="onChangeTax();" ' + (allowTaxEdit!= 'A'?" disabled":"")+ '>';
			
			labelNameOptions += '<option value="">--select--</option>';
			//Set subgroup dropdown.
			for(var subkey in gItemTaxSubGroups) {
				if (gItemTaxSubGroups.hasOwnProperty(subkey)) {
					
					for(var i=0; i<gItemTaxSubGroups[subkey].length ; i++) {
						if(gItemTaxSubGroups[subkey][i].item_group_id == gItemTaxGroups[key].item_group_id) {
							labelNameOptions += '<option value='+gItemTaxSubGroups[subkey][i].item_subgroup_id
							labelNameOptions += '>'+gItemTaxSubGroups[subkey][i].item_subgroup_name+'</option>';
						}
					}
				}
			}
			labelNameOptions += '</select>';
			if(count >= 2) {
				labelNameOptions += '</td>';
				labelNameOptions += '</tr>';
				labelNameOptions += '<tr>';
				count = 0;
			} else {
				labelNameOptions += '</td>';
			}
			count++;
		}
	}
	document.getElementById('add_tax_groups').outerHTML = labelNameOptions;
}

function initMrNoAutoComplete() {
	Insta.initPatientAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
}

function resetTaxFields(){
	Object.keys(gItemTaxGroups).map( taxSubgroupId => {
			setFieldValue('ed_taxsubgroupid', "", taxSubgroupId);
			setFieldValue('taxsubgroupid', "", taxSubgroupId);
			setFieldValue('taxname', "", taxSubgroupId);
			setFieldValue('taxrate', "", taxSubgroupId);
			setFieldValue('taxamount', "", taxSubgroupId);
		}
	);
}

function reInit(obj){
	if ( obj == undefined ){
		return;
	}
	obj.disabled = false;
	obj.className = "required"
	document.getElementById("patientDetails").style.display = 'block';
	document.getElementById("patientDetails").style.visibility = 'visible'
	initItemAutoComplete();
	document.getElementById("items").value = '';
	clearAllHiddenVariables();
}

function onChangeBatch(batchNo){
	if (isPkg) {
		fillSelectedBatchDetails(batchNo, getFieldValue('medicine_id'));
		return;
	}
	clearDialogBox('batch');
	fillSelectedBatchDetails(batchNo, getFieldValue('medicine_id'));

	var avilQty = avlQty[getFieldValue('item_batch_id')];
	var medicineId = getFieldValue('medicine_id');
	var itemBatchIdVar = getFieldValue('item_batch_id');
	var issueType = getFieldValue('item_unit');
	var pkgSize = getFieldValue('pkg_size');
	var rate = getFieldValue('mrp');
	var discount = getFieldValue('discount');
	var billNo = getFieldValue('bill_no');
	var issueQty = getFieldValue('issuQty')?getFieldValue('issuQty'):"0";
	if(parseFloat(avilQty) > 0 || gStockNegative == 'A' || gStockNegative == 'W')
		getItemAmounts(medicineId, itemBatchIdVar, issueType, issueQty, pkgSize, rate, discount, 'B', billNo);
}

/**
 * This method is used to add medicines to Grid.
 * 
 * @returns {Boolean}
 */
function addItemsToTable() {
	var discount = document.getElementById("discount");
	if(discount && discount.value > 100) {
		showMessage("js.sales.issues.storesuserissues.discount.greater.than.hundred");
		discount.focus();
		return false;
	}
	if(gItemType == 'orderkit') {
		var orderkitName = getFieldValue('orderkits');
		if(orderkitName == undefined || orderkitName == '' || orderkitName == null) {
			showMessage("js.sales.issues.storesuserissues.orderkit.required");
			getField('orderkits').focus();
			return false;
		}
		addMedicinesFromOrderKit();
	} else {
		if (!isNotNullValue('itemMrp')) {
			setFieldValue('itemMrp', 0);
		}

		if(getField('itemListtable').rows.length>1) {
			for(var i=1; i <= getField('itemListtable').rows.length-1; i++){
				var currentbatchNo = getFieldValue('batch');
				var currentMedicineName = getFieldValue('items');
				if (currentbatchNo == getFieldValue('item_identifier', i)
						&& (encodeURIComponent(currentMedicineName) == getFieldValue('item_name', i))
						&& (parseInt(getFieldValue('dialogId')) != i)){
					showMessage("js.sales.issues.storesuserissues.duplicateentry");
					return false;
				}
			}
		}

		// validate rate increase/decrease based on rights
		var origRate = getElementPaise(getField('origMRP'));
		var newRate = getElementPaise(getField('mrp'));
		var discountAmt = getFieldValue('discount_amt');
		var discount = getFieldValue('discount_per');
		if ((newRate < origRate) && (gAllowRateDecrease != 'A') && (isNotNullValue('batch'))) {
			var msg=getString("js.sales.issues.storesuserissues.notauthorized.decreasetheratebelow");
			msg += formatAmountPaise(origRate);
			alert(msg);
			setFieldValue('itemMrp', getFieldValue('origMRP'));
			setFieldValue('mrp', getFieldValue('origMRP'));
			onChangeRate(getFieldValue('origMRP'));
			return false;
		}

		if ((newRate > origRate) && (gAllowRateIncrease != 'A') && (isNotNullValue('batch'))) {
			var msg=getString("js.sales.issues.storesuserissues.notauthorized.increasetherateabove");
			msg += formatAmountPaise(origRate);
			alert(msg);
			setFieldValue('itemMrp', getFieldValue('origMRP'));
			setFieldValue('mrp', getFieldValue('origMRP'));
			onChangeRate(getFieldValue('origMRP'));
			return false;
		}

		if(checkQty()){
			var valid = true;
			if (getFieldValue('issuQty') == 0) {
				showMessage("js.sales.issues.storesuserissues.issuequantity.notbezero");
				getField('issuQty').focus();
				return false;
			}

			if (!(isAmount(getFieldValue('issuQty')))) {
				showMessage("js.sales.issues.storesuserissues.entervalidquantity");
				return false;
			}

			if (!isValidNumber(getField('issuQty'), allowDecimalsForQty)) return false;

			if (isPkg) {
				if (getFieldValue('issuQty') > document.getElementById('pkg_issue_qty'+
				  getFieldValue('dialogId')).value) {
					showMessage("js.sales.issues.storesuserissues.issueqty.greaterthan.pkgqty");
					return false;
				}
				var pkgDetails = getPackageDetails();
				fillPkgItemTaxAmounts(pkgDetails);
			}

			var itemtable = getField('itemListtable');
			var len = itemtable.rows.length;

			if(!checkItemDetails(len, itemtable))
				return false;
			var item = getFieldValue('items');
			var batch = getFieldValue('batch');
			var qty = getFieldValue('issuQty');
			var pkgSize = getFieldValue('pkg_size');
			var itemBatchId = getFieldValue('item_batch_id');
			var stktype = stock_type[itemBatchId];
			var itembillable = getFieldValue('itemBillable');
			var issueuom = getFieldValue('item_unit');
			var chkExpiry = '';
			var selectedBatchDetails = findInList(itemBatchDetails, 'batch_no', batch);

			if (!isNotNullValue('expdt')) {
				setFieldValue('expdt', '');
				chkExpiry = "";
			} else {
				chkExpiry = selectedBatchDetails.exp_dt;
				var daysToExpire = chkExpiry != null ? getDaysToExpire(chkExpiry) : 'woexp';
				if (null != gAllowExpiredSale){
					if( daysToExpire < 0 ){
						showMessage("js.sales.issues.storesuserissues.itembeingissue.salreadyexpired");
						if ((gAllowExpiredSale != 'Y') ) {
							return false;
						}
					}
				}
			}

			var controlTypeName = getFieldValue('control_type_name');
			var controlTypeId = getFieldValue('control_type_id');
			var dialogId = getFieldValue('dialogId');
			var len = getField('itemListtable').rows.length;
			if (hdrugAlertNeeded == 'Y' && controlTypeName != 'Normal' && dialogId == len - 1) {
				var msg=getString("js.sales.issues.storesuserissues.medicinecontroltype");
				msg+=controlTypeName;
				msg+=getString("js.sales.issues.storesuserissues.canonlybesold.prescription");
				msg+="\n";
				msg +=getString("js.sales.issues.storesuserissues.verifythepatientsprescription");
				alert(msg);
			}
			var billNo = getFieldValue('bill_no');
			var isTpaBill = getBillIsTpa(billNo);
			var coverdbyinsuranceflag = null;
			if(isNotNullValue('coverdbyinsuranceflag')) {
				coverdbyinsuranceflag = getFieldValue("coverdbyinsuranceflag");
			}
			var itemDetails = {
					"medicine_id":getFieldValue('medicine_id'),
					"item_name":item,
					"batch_no":getFieldValue('batch_no'),
					"item_batch_id":getFieldValue('item_batch_id'),
					"exp_date":getFieldValue('expdt'),
					"qty":qty,
					"package_size":pkgSize,
					"unit_size":getFieldValue('unit'),
					"is_billable":itembillable,
					"cat_payable":coverdbyinsuranceflag,
					"control_type_id":controlTypeId,
					"control_type_name":controlTypeName,
					"mrp":getFieldValue('mrp'),
					"unit_mrp":getFieldValue('unit_mrp'),
					"org_mrp":getFieldValue('origMRP'),
					"discount_amt":discountAmt,
					"discount_per":discount,
					"issue_base_unit":getFieldValue('issue_base_unit'),
					"issueuom":issueuom,
					"stktype":stktype,
					"tax_per":getFieldValue('tax_rate'),
					"tax_amt":getFieldValue('tax'),
					"original_tax_amt":getFieldValue('original_tax'),
					"tax_type":getFieldValue('taxType'),
					"amt":getFieldValue('amount'),
					"category_id":getFieldValue('categoryId'),
					"insurance_category_id":getFieldValue('insuranceCategoryId'),
					"billing_group_id":getFieldValue('billingGroupId'),
					"priCatPayable":getFieldValue('priCatPayable'),
					"item_bar_code_id":getFieldValue('barCodeId'),
					"coverdbyinsuranceflag":getFieldValue('coverdbyinsuranceflag'),
					"origUnitMrp":getFieldValue('origUnitMrpHid')
				};
			var dialogId  = getFieldValue('dialogId');
			for(var i =0; i<groupListJSON.length; i++) {
				var itemGroupId = groupListJSON[i].item_group_id;
				copyFieldValue('taxname', 'taxname', groupListJSON[i].item_group_id+dialogId, groupListJSON[i].item_group_id);
				copyFieldValue('taxrate', 'taxrate', groupListJSON[i].item_group_id+dialogId, groupListJSON[i].item_group_id);
				copyFieldValue('taxamount', 'taxamount', groupListJSON[i].item_group_id+dialogId, groupListJSON[i].item_group_id);
				copyFieldValue('taxsubgroupid', 'taxsubgroupid', groupListJSON[i].item_group_id+dialogId, groupListJSON[i].item_group_id);
			}
			addToInnerHTML(itemDetails, dialogId);
		}
		// Insurance 3.0 calculation after add of item
		var billNo = getFieldValue('bill_no');
		var isTpaBill = getBillIsTpa(billNo);
		if(isTpaBill)
			onClickProcessInsForIssues('patientissueform', 'P');
	}
}

function addToInnerHTML(itemDetails, dialogId){
	var itemTable = getField('itemListtable');
	var tabLen = itemTable.rows.length;
	var imgbutton = makeImageButton('itemCheck', 'itemCheck'+dialogId, 'imgDelete', cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','deleteRow(this.id,itemRow'+dialogId+','+dialogId+')');
	var billNo = getFieldValue('bill_no');
	var isTpaBill = getBillIsTpa(billNo);
	addedItem[tabLen] = itemDetails.item_name;
	addedIdentifier[tabLen] = itemDetails.batch_no;
	
	if(itemDetails.coverdbyinsuranceflag != null && itemDetails.coverdbyinsuranceflag != undefined && itemDetails.coverdbyinsuranceflag == "f" && isTpaBill) {
		setFieldHtml('itemLabel', '<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+itemDetails.item_name, dialogId);
	} else if (itemDetails.stktype == true || itemDetails.stktype=='t' || itemDetails.stktype == 'true' ) {
		setFieldHtml('itemLabel', '<img class="flag" src= "'+popurl+'/images/grey_flag.gif"/>'+itemDetails.item_name, dialogId);
	}
	else {
		setNodeText(getField('itemLabel', dialogId), itemDetails.item_name, 25);
	}
	setFieldValue('temp_charge_id', '_'+dialogId, dialogId);
	setFieldValue('item_name', encodeURIComponent(itemDetails.item_name), dialogId);
	setFieldValue('item_bar_code_id', itemDetails.item_bar_code_id, dialogId);
	setFieldValue('pkg_mrp', itemDetails.mrp, dialogId);
	setFieldValue('pkg_unit', itemDetails.issue_base_unit, dialogId);
	setFieldValue('tax_per', itemDetails.tax_per, dialogId);
	setFieldValue('tax_type', itemDetails.tax_type, dialogId);
	setFieldValue('original_tax', itemDetails.original_tax_amt, dialogId);
	setFieldValue('tax_amt', itemDetails.tax_amt, dialogId);
	setFieldValue('amt', itemDetails.amt, dialogId);
	setFieldValue('category', itemDetails.category_id, dialogId);
	setFieldValue('insurancecategory', itemDetails.insurance_category_id, dialogId);
	setFieldValue('billinggroup', itemDetails.billing_group_id, dialogId);
	setFieldValue('original_mrp', itemDetails.org_mrp, dialogId);
	setFieldValue('item_unit', itemDetails.issueuom, dialogId);
	setFieldValue('medicine_id', itemDetails.medicine_id, dialogId);
	setFieldValue('cat_payable', itemDetails.cat_payable, dialogId);
	setFieldValue('issue_base_unit', itemDetails.issue_base_unit, dialogId);
	setFieldValue('control_type_id', itemDetails.control_type_id, dialogId);
	setFieldValue('control_type_name', itemDetails.control_type_name, dialogId);
	setFieldValue('priCatPayable', itemDetails.priCatPayable, dialogId);
	setFieldValue('package_size', itemDetails.issue_base_unit, dialogId);
	setFieldValue('origUnitMrpHid', itemDetails.origUnitMrp, dialogId);
	var taxSubgroupIds = [];
	for(var i =0; i<groupListJSON.length; i++) {
		var itemGroupId = groupListJSON[i].item_group_id;
		if(getFieldValue('ed_taxsubgroupid'+itemGroupId) != null && 
				getFieldValue('ed_taxsubgroupid'+itemGroupId) != '')
			taxSubgroupIds.push(getFieldValue('ed_taxsubgroupid'+itemGroupId));
	}
	if (taxSubgroupIds != undefined && taxSubgroupIds.length > 0) {
		setFieldValue('tax_sub_group_ids', taxSubgroupIds.toString(), dialogId);
	}
	
	if (itemDetails.control_type_name != 'Normal')
		setFieldHtml('controleTypeLabel', "<font color='blue'>"+itemDetails.control_type_name+"</font>", dialogId);
	else
		setNodeText(getField('controleTypeLabel', dialogId), itemDetails.control_type_name, 25);

	setFieldText('identifierLabel', itemDetails.batch_no, dialogId);
	
	setFieldValue('item_identifier', itemDetails.batch_no, dialogId);
	setFieldValue('item_batch_id', itemDetails.item_batch_id, dialogId);
	setFieldValue('stype', itemDetails.stktype, dialogId);
	
	if ((null != itemDetails.exp_date) && (itemDetails.exp_date != '')){
		setFieldText('expdtLabel', formatExpiry(new Date(itemDetails.exp_date)), dialogId);
		setFieldValue('exp_dt', formatExpiry(new Date(itemDetails.exp_date)), dialogId);
	} else{
		setFieldText('expdtLabel', '', dialogId);
		setFieldValue('exp_dt', '', dialogId);
	}
	
	setFieldText('issue_qtyLabel', itemDetails.qty, dialogId);
	setFieldValue('issue_qty', itemDetails.issueuom == 'I' ? parseFloat(itemDetails.qty).toFixed(decDigits) : parseFloat(itemDetails.package_size*itemDetails.qty).toFixed(decDigits), dialogId);
	
	setFieldText('uomLabel', itemDetails.issueuom == 'I' ? issueUnits[itemDetails.item_batch_id] : pkgUOMs[itemDetails.item_batch_id], dialogId);
	setFieldText('pkgSizeLabel', itemDetails.package_size, dialogId);
	
	if (getField('item_billable_hidden', dialogId) != null){
		setFieldValue('item_billable_hidden', itemDetails.is_billable == '' ? item_billable[itemDetails.item_batch_id] : itemDetails.is_billable, dialogId);
	}
	setFieldValue('item_batch_id', itemDetails.item_batch_id, dialogId);
	setFieldValue('unit_mrp', itemDetails.unit_mrp, dialogId);
	
	
	if (getFieldValue('hdeleted', dialogId) != 'true' )
		setFieldValue('hdeleted', 'false', dialogId);
	
	if(getField('itemRow', dialogId).firstChild == null)
		getField('itemRow', dialogId).appendChild(imgbutton);
	
	if (showCharges == 'A') {
		setFieldText('mrpLabel', parseFloat(itemDetails.mrp).toFixed(decDigits), dialogId);
		setFieldText('unitmrpLabel', parseFloat(itemDetails.unit_mrp).toFixed(decDigits), dialogId);
		setFieldText('pkgMRPLabel', parseFloat(itemDetails.mrp).toFixed(decDigits), dialogId);
		setFieldText('taxamtLabel', parseFloat(itemDetails.tax_amt).toFixed(decDigits), dialogId);
		setFieldText('totamtLabel', parseFloat(itemDetails.amt).toFixed(decDigits), dialogId);
		setFieldText('discountLabel', parseFloat(itemDetails.discount_amt).toFixed(decDigits), dialogId);
	}
	setFieldValue('amt', itemDetails.amt, dialogId);
	setFieldValue('mrpHid', itemDetails.mrp, dialogId);
	setFieldValue('discountHid', itemDetails.discount_per, dialogId);
	setFieldValue('discountAmtHid', itemDetails.discount_amt, dialogId);
	setFieldValue('coverdbyinsuranceflag', itemDetails.coverdbyinsuranceflag, dialogId);
	
	reCalcRowAmounts(dialogId);

	var nextrow =  getField('tableRow', (eval(dialogId)+1));
	if (nextrow == null){
		AddRowsToGrid(tabLen);
	}

	var editButton = getField('add', dialogId);
	var eBut =  getField('addBut', dialogId);
	if (getFieldValue('hdeleted', dialogId) != 'true' )
		editButton.setAttribute("src", popurl+'/icons/Edit.png');

	eBut.setAttribute('title', getString("js.sales.issues.storesuserissues.edititem"));
	eBut.setAttribute('accesskey', '');

	if(dialogId == (eval(tabLen)-2)){
		handleCancel();
		return false;
	}
	openDialogBox(eval(dialogId)+1);

}

function checkItemDetails(len, items, batch){
	var myform = document.patientissuedailog;
	if((null != addedItem) && (addedItem.length !=0)){
		for(var i = 1;i< len;i++){
			var checkbox = "itemRow"+i;
			if(addedItem[i] == items && addedIdentifier[i] == batch){
				if(!getField(checkbox).checked && ((parseInt(getFieldValue('dialogId'))+1) != i)) {
					showMessage("js.sales.issues.storesuserissues.duplicateentry");
					return false;
				}
			}
		}
	}

	if(myform.items.value == ''){
		showMessage("js.sales.issues.storesuserissues.selectanitem");
		myform.items.focus();
		return false;
	}
	if(myform.batch.value == ''){
		showMessage("js.sales.issues.storesuserissues.pickabatch.serialno");
		myform.batch.focus();
		return false;
	}
	if(myform.issuQty.value == ''){
		showMessage("js.sales.issues.storesuserissues.issueqty.required");
		myform.issuQty.focus();
		return false;
	}
	if (!isValidNumber(myform.issuQty, allowDecimalsForQty)) return false;

	return true;
}

function deleteRow(imgObj,btn,len){
	var deletedInput = document.getElementById('hdeleted'+len);
	var tot_amt = document.getElementById("totAmt");
	var tot_amount = document.getElementById("totamtLabel"+len);
	if(deletedInput.value == 'false'){
		YAHOO.util.Dom.get(btn).disabled = true;
		deletedInput.value = 'true';
//		if (null != document.patientissueform && document.getElementById("firstOfCategory"+len).value == "true"
//					&& parseInt(document.getElementById("planId").value)>0)
//			alert(getString("js.sales.issues.storesuserissues.deletingfirstitem.insurancecategory")+"\n"+getString("js.sales.issues.storesuserissues.updatepatientcopay.fixedamountpercategory.foradditionalcharges")+"\n"+getString("js.sales.issues.storesuserissues.sameinsurancecategory"));
		document.getElementById("add"+len).disabled = true;
		document.getElementById(imgObj).src = cpath+"/icons/Deleted.png";

		updateEdit(document.getElementById("addBut"+len),'disabled');

	} else {
		deletedInput.value = 'false';
		YAHOO.util.Dom.get(btn).disabled = false;
		document.getElementById("add"+len).disabled = false;
		document.getElementById(imgObj).src = cpath+"/icons/Delete.png";

		updateEdit(document.getElementById("addBut"+len),'enabled');
	}
	if ((null != document.patientissueform) && (showCharges == 'A'))
		resetTotals();

	// Insurance 3.0 calculation of cancel item
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	if(isTpaBill)
		onClickProcessInsForIssues('patientissueform', 'P');
}

//RC: Cleanup required.
function validate(button,type){
	if(type == 'Patient'){
		if(document.getElementById("mrno").value == ''){
			showMessage("js.sales.issues.storesuserissues.enterapatientmrno");
			button.disabled = false;
			document.getElementById("mrno").focus();
			return false;
		}
	}
	var numRows = document.getElementById("itemListtable").rows.length-1;
	var allChecked = false;
	if(numRows <= 1){
		showMessage("js.sales.issues.storesuserissues.additemstoissue");
		button.disabled = false;
		return false;
	}
	if (numRows > 1) {
		for (var k=1;k<=(numRows-1);k++) {
	    	if (document.getElementById("hdeleted"+k).value == 'false') {
	    		allChecked = true;
	    		if ((document.getElementById("item_billable_hidden"+k) != null) && (document.getElementById("item_billable_hidden"+k).value == 'true')) {
					if(type == 'Patient'){
						if(document.getElementById("bill_no").value == ''){
							showMessage("js.sales.issues.storesuserissues.patientnothave.openunpaidbills.youcannotissuebillableitem");
				  			document.getElementById("items").value='';
				  			return false;
						}
					}
				}
	    	}
		}
	    if (!allChecked) {
	    	var msg=getString("js.sales.issues.storesuserissues.allrowinthegrid.arechecked");
	    	msg+="\n";
	    	msg+=getString("js.sales.issues.storesuserissues.sonorecord.save");
	    	alert(msg);
	    	button.disabled = false;
			return false;
	    }
	}
	if (null != document.patientissueform){
		if (!calculateTotal()) {
			return false;
		}

		if( document.patientissueform.issueDate && !validateIssueDate() ) {
			return false;
		}
	}
	return true;
}

function checkQty(){
	var myform = document.patientissuedailog;
	var batchNo = document.patientissuedailog.batch[document.patientissuedailog.batch.selectedIndex].value;
	var itemUnit = myform.item_unit.value;
	var expectedQty = myform.issuQty.value ;

	if (itemBatchDetails != null){
		for(var t= 0; t<itemBatchDetails.length; t++){
			if(batchNo == itemBatchDetails[t].batch_no){
				expectedQty = itemUnit == 'I' ? expectedQty : parseFloat(expectedQty)*pkgSizes[itemBatchDetails[t].item_batch_id];
				if(expectedQty > itemBatchDetails[t].qty){
					if(gStockNegative == 'A')
						return true;
					else if(gStockNegative == 'D'){
						showMessage("js.sales.issues.storesuserissues.requestedstock.notavailable");
						myform.issuQty.value = '';
						myform.unitMrp.value = '';
						myform.itemMrp.value = '';
						return false;
					}else if(gStockNegative == 'W'){
						if(!confirm("There is not enough stock for the given issue quantity. Are you sure you want to issue ?")) return false;
						else return true;
					}

				} else {
					return true;
				} 
			}
		}
	}
	return true;
}

function onChangeStore(storechanged){
	var itemTable = document.getElementById('itemListtable');
	var numItems = itemTable.rows.length-1;
	for ( var i=0; i<numItems; i++ ) {
		itemTable.deleteRow(1);
	}
	AddRowsToGrid(1);

	var img_button = document.createElement('img');
	img_button.src = cpath + '/images/delete.jpg';
	var storeObj = document.patientissueform.store;
	storeObj.value = storechanged;
	isBatchMrpEnabled(storeObj.value);
	document.patientissueform.store.value = storechanged;
	document.getElementById('patientDetails').visibility="hidden";
	document.getElementById("patientDetails").style.display = 'none';
	visitId = document.getElementById("mrno").value;
	indentStore = storechanged;
	clearPatientDetails();
	document.patientissueform.mrno.value = "";
	reInit(getField('mrno'));
	getMedicinesForStore(storeObj, initItemAutoComplete);
}

function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		onChangeQty(e.target.value);
		addItemsToTable();
		return false;
	} else {
		if(gItemType != 'orderkit') {
			if(!enterNumOnlyANDdot(e)){
			   document.getElementById("issuQty").value = '';
			   showMessage("js.sales.issues.storesuserissues.enternumbersanddotonly.negativequantity.notallowed");
				return false;
			}
			return true;
		}
	}
}

function onKeyPressDiscount(e){
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
		onChangeDiscount(e.target);
		addItemsToTable();
		return false;
	} else {
		if(gItemType != 'orderkit') {
			if(!enterNumOnlyANDdot(e)){
			   document.getElementById("discount").value = '';
			   showMessage("js.sales.issues.storesuserissues.enternumbersanddotonly.negativequantity.notallowed");
				return false;
			}
			return true;
		}
	}
}

function submitForm(button,type){
	if(validate(button,type)) {
		if ( type == 'Patient') {
			if (isSharedLogIn == 'Y')
				loginDialog.show();
			else {
				button.disabled = true;
				document.patientissueform.submit();
			}
		}
	}
}

var patient = null;
function getPatientDetails() {
    var visitId = document.getElementById("mrno").value;
    var reqObj = new XMLHttpRequest();
	var storeId = document.getElementById("store").value;
    var url = cpath+'/patientissues/getpatientdetails.json?visit_id='+visitId+'&storeId='+storeId;
    ajaxGETRequest(reqObj, patientDetailsResponseHandler, url.toString(), false);
}

function getPatientFromBill(){
	if ((null != visitId) && (visitId != '') && !noAccess ){
		document.getElementById("mrno").value = visitId;
		var reqObj = new XMLHttpRequest();
		var patientIndentNo = document.patientissueform.patient_indent_no_param.value;
		var storeId = document.getElementById("store").value;
		var url = cpath+'/patientissues/getpatientdetails.json?visit_id='+visitId+'&storeId='+storeId;
		url = url + ( !empty(patientIndentNo) ? "&patient_indent_no="+patientIndentNo : "" );
		ajaxGETRequest(reqObj, patientDetailsResponseHandler, url.toString(), false);
	}
}

var gMedicineBatches = {};
function patientDetailsResponseHandler(responseText){
	eval("patient =" + responseText);
	clearPatientDetails();
	if ( patient == null ) {
		showMessage("js.sales.issues.storesuserissues.patientselectedinactive.selectanactivepatienttocontinue");
		clearPatientDetails();
		document.getElementById("bill_no").length = 0;
		return false;
	}
	patient = patient.patient_details;
	populateBillType(patient.visit.bills, patient);
	visitOpenBills = patient.visit.bills;
	setPatientDetails(patient);
	populatePackages(patient.visit);

	if (document.getElementById("bill_no").value != '' && patient.indentsList != undefined && patient.indentsList.length > 0 && getAutoFillIndents() ) {
		// we get a map of medicine batches from the patient info itself.
			if (patient.medBatches != null) {
				gMedicineBatches = patient.medBatches;
			} else {
				gMedicineBatches = {};
			}

			if (patient.patIndentDetails.length > 0){
				var table = document.getElementById('itemListtable');
				var numItems = table.rows.length;
				for (var i=1; i<numItems; i++) {
					table.deleteRow(1);
				}
				AddRowsToGrid(1);
				addMedicinesFromIndents(patient.patIndentDetails);
			}
			table = document.getElementById('prescInfo');

			numItems = table.rows.length;
			if ( patient.patIndentDetails.length > 0 ) {
				for (var i=1; i<numItems-1; i++) {		// append indents after prescriptions
					table.deleteRow(1);
				}
			}
			var numItems = table.rows.length;
			var indents = patient.indentsList;
			for (var i = 0; i<indents.length; i++){
				var templateRow = table.rows[numItems-1];
				var row = templateRow.cloneNode(true);
				row.style.display = '';
				table.tBodies[0].insertBefore(row, templateRow);

				// doctor name in first column
				setNodeText(row.cells[0], indents[i].patient_indent_no);

				// url to view presc in second col
				var url = cpath + "/stores/PatientIndentView.do?_method=view&stop_doctor_orders=true";
				url += "&patient_indent_no="+ indents[i].patient_indent_no;
				var anch = row.cells[1].getElementsByTagName("A")[0];
				anch.href = url;

				getElementByName(row, "patient_indent_no_ref").value = indents[i].patient_indent_no;
			}

			document.getElementById('prescDetailsDiv').style.display="block";
			showDiv = true;


			if (showDiv) {
				document.getElementById('prescFieldSet').style.display = 'block';
			} else {
				document.getElementById('prescFieldSet').style.display = 'none';
			}
			if (!showDiv) {
				document.getElementById('prescDetailsDiv').style.display="none";
				openItemSearchDialog(document.getElementById('addButton'));
			}
	}
	//insurance 3.0 calculation from issues through indents screen
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	if(isTpaBill)
		onClickProcessInsForIssues('patientissueform', 'P');
}

function addIndentItems(medicineId, userQty, indent, dialogId,billable) {

	var allBatches = gMedicineBatches[medicineId];
	var medicineName = allBatches[0].medicine_name;

	/*
	 * The batches excludes zero stock if preferences disallows it. But for expiry,
	 * Even if it is disallowed to sell, we bring the batch here in order to alert the
	 * user saying expired items are available, but we are not allowed to sell.
	 *
	 * Also, the batch list is already sorted for our convenience:
	 *  Availability (ie, qty > 0 comes first)
	 *    Expiry Date
	 *      Available Quantity
	 */

	var remQty = userQty;
	var expiredQty = 0; var nearingExpiryQty = 0;
	var negativeQty = 0; var diffPkgQty = 0;

	for (b=0; b<allBatches.length; b++) {

		var batch = allBatches[b];
		var avlblQty = 0;
		exp_dts[batch.item_batch_id] = batch.exp_dt;

		// if batch is already in grid, skip
	    var dupExists = getDuplicateIndex(batch, -1);
		if (dupExists != -1)
			continue;

		if (indent.uom == '') {
			avlblQty = batch.qty;
		} else {
			// use only whole packages
			avlblQty = Math.floor(batch.qty/batch.issue_base_unit);
		}

		// check for expiry date for normal sales (ie, not estimate, and not negative stock)
		if (avlblQty > 0) {
			var daysToExpire = batch.exp_dt != null ? getDaysToExpire(batch.exp_dt) : gExpiryWarnDays+1;
			if (daysToExpire <= 0) {
				expiredQty += avlblQty;
				if (gAllowExpiredSale != 'Y') {
					continue;
				}
			} else if (daysToExpire <= gExpiryWarnDays) {
				nearingExpiryQty += avlblQty;
			}
		}

		var qtyForBatch = 0;
		if ((gStockNegative != 'D') && (avlblQty <= 0 || (b == allBatches.length - 1))) {
			// if stock is allowed to be negative, use up all of remQty for the last batch
			// or for the first batch where it is already 0 or negative.
			qtyForBatch = remQty;
			negativeQty = qtyForBatch - avlblQty;
		} else {
			qtyForBatch = Math.min(remQty, avlblQty);
			qtyForBatch = qtyForBatch < 0 ? 0 : qtyForBatch;   // stock can be negative even otherwise.
			if (allowDecimalsForQty == 'Y') {
				// round it off to 2 decimal places to avoid float problems
				qtyForBatch = Math.round(qtyForBatch*100)/100;
			}
		}
		var stktype = document.forms[0].stocktype.value;
		document.getElementById("dialogId").value = dialogId;
		// add to grid
		if (qtyForBatch > 0 && (expiredQty <= 0 || gAllowExpiredSale == 'Y'))
			addIndentItemsToGrid(medicineId,medicineName,batch,indent,qtyForBatch)

		// if no more to be added finish up
		remQty = remQty - qtyForBatch;
		if (remQty == 0)
			break;
	}

	var msg = "";
	if (remQty > 0) {
		msg= getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg+= medicineName;
		msg+= "': " ;
		msg+= remQty.toFixed(2);
		alert(msg);
		if (expiredQty > 0) {
			msg += " (" ;
			msg+= expiredQty;
			msg+=getString("js.sales.issues.storesuserissues.itemsavailable.pastexpirydate");
			alert(msg);
		}
		if (diffPkgQty > 0) {
			msg += " (";
			msg += diffPkgQty;
			msg +=getString("js.sales.issues.storesuserissues.itemsavailable.differentpackagesize");
			alert(msg);
		}
		msg += "\n";
	}

	if (negativeQty > 0 && gStockNegative != 'A') {
		msg +=getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg += medicineName;
		msg += "': ";
		msg	+= negativeQty.toFixed(2);
		msg += getString("js.sales.issues.storesuserissues.proceedingwithissue.causestockbecomenegative");
		msg+="\n";
		alert(msg);
	}

	if (nearingExpiryQty > 0) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor");
		msg += medicineName;
		msg +=getString("js.sales.issues.storesuserissues.aresoontoexpire");
		msg+="\n";
		alert(msg);
	}

	if ((expiredQty > 0) && gAllowExpiredSale) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor");
		msg += medicineName;
		msg +=getString("js.sales.issues.storesuserissues.pastexpirydate");
		alert(msg);
	}
	return msg;
}

function addIndentItemsToGrid(medicineId,medicineName,batch,indent,qtyForBatch){

	var indentNo = indent.patient_indent_no;
	var indentItemId = indent.indent_item_id;

	var itemsTable = document.getElementById("itemListtable");
	var rowsLength = (itemsTable.rows.length)-1;
	var  nextRowLen = eval(rowsLength)+1;
	var nextrow =  document.getElementById("tableRow"+nextRowLen);
	if (nextrow == null){
		AddRowsToGrid(nextRowLen);
	}
	//	added for exclusions
	var catPayableInfo = null;
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	var coverdbyinsuranceflag = null;
	catPayableInfo = getCatPayableStatus(medicineId, false);
	if(catPayableInfo!= null && catPayableInfo != undefined ) {
		var calimbaleInfo = catPayableInfo.plan_category_payable != null? catPayableInfo.plan_category_payable:"";
		if(calimbaleInfo == 'f') {
			document.getElementById('coverdbyinsurance').innerHTML = "No";
			document.getElementById('coverdbyinsurance').style.color = "red";
		} else {
			document.getElementById('coverdbyinsurance').innerHTML = "Yes";
			document.getElementById('coverdbyinsurance').style.color = "#666666";
		}
		document.getElementById("cat_payable"+rowsLength).value = calimbaleInfo;
		if(calimbaleInfo != null && calimbaleInfo != undefined && calimbaleInfo == "f" && isTpaBill) {
			document.getElementById("itemLabel"+rowsLength).innerHTML ='<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+medicineName;
		} else {
			document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
		}
	} else {
		document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
	}





    //document.getElementById("itemLabel"+rowsLength).textContent = medicineName;

    controlTypeName = batch.control_type_name;
    if (controlTypeName != 'Normal')
		document.getElementById("controleTypeLabel"+rowsLength).innerHTML = "<font color='blue'>"+controlTypeName+"</font>";
	else
		setNodeText(document.getElementById("controleTypeLabel"+rowsLength), controlTypeName, 25);

    //document.getElementById("controleTypeLabel"+rowsLength).textContent = batch.control_type_name;
    document.getElementById("uomLabel"+rowsLength).textContent = ( indent.uom == '' ? indent.uom_display : indent.uom );
   document.getElementById("issueRateExpr"+rowsLength).value = batch.issue_rate_expr;
   document.getElementById("visitSellingPriceExpr"+rowsLength).value = batch.visit_selling_expr;
   document.getElementById("storeSellingPriceExpr"+rowsLength).value = batch.store_selling_expr;



    var imgbutton = makeImageButton('itemCheck','itemCheck'+rowsLength,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','deleteRow(this.id,itemRow'+rowsLength+','+rowsLength+')');

	var editButton = document.getElementById("add"+rowsLength);
	var eBut =  document.getElementById("addBut"+rowsLength);
	editButton.setAttribute("src",popurl+'/icons/Edit.png');
	eBut.setAttribute("title", "Edit Item");
	eBut.setAttribute("accesskey", "");
	document.getElementById("itemRow"+rowsLength).appendChild(imgbutton);

	document.getElementById("temp_charge_id"+rowsLength).value = "_"+rowsLength;
	document.getElementById("storeId"+rowsLength).value = document.getElementById("store").value;
	document.getElementById("medDisc"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);

	document.getElementById("isMarkUpRate"+rowsLength).value = '';
	document.getElementById("indent_item_id"+rowsLength).value = indent.indent_item_id;
	document.getElementById("patient_indent_no"+rowsLength).value = indent.patient_indent_no;
	document.getElementById("hdeleted"+rowsLength).value = false;
	document.getElementById("patper").value = batch.patient_percent;
	document.getElementById("patcatamt").value = batch.patient_amount_per_category;
	document.getElementById("patcap").value = batch.patient_amount_cap;
	document.getElementById("insuranceCategoryId").value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;
	document.getElementById("billingGroupId").value = (!batch.billing_group_id) ? '' : batch.billing_group_id;

	document.getElementById("firstOfCategory"+rowsLength).value = document.getElementById("isFirstOfCategory").value;

	document.getElementById( "medDiscWithoutInsurance"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById( "medDiscWithInsurance"+rowsLength).value = '';

	var discountPlanId = document.getElementById("discountPlanId").title;
	var insuranceCategoryId = batch.insurance_category_id;
	var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";

	if(priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y') {
		var discountPlanDetailsJSON = discountPlansJSON;
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {
			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];
				if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountPlanId && insuranceCategoryId == item.applicable_to_id ) {
					document.getElementById("medDiscWithInsurance"+rowsLength).value = item["discount_value"];
					break;
				}
			}
		}
	}


	//TODO rename function
	var amounts = getItemAmountsForOrderkit(medicineId, batch.item_batch_id, gStoreSaleUnit, qtyForBatch, batch.issue_base_unit, null, null, 'B', billNo);

	var amountDetails = amounts.amount_details;
	var taxDetails = amounts.tax_details;

	issueUnits[batch.item_batch_id] = batch.issue_units;






	var discount = 0;

    var medDiscWithInsurance = 0.00;
    var medDiscWithoutInsurance = 0.00;
    if(document.getElementById("medDiscWithInsurance"+rowsLength) != null && document.getElementById("medDiscWithInsurance"+rowsLength).value !='') {
    	medDiscWithInsurance = document.getElementById("medDiscWithInsurance"+rowsLength).value;
    } else {
    	medDiscWithInsurance = document.getElementById("medDiscWithoutInsurance"+rowsLength).value;
    }

    if(document.getElementById("medDiscWithoutInsurance"+rowsLength) != null && document.getElementById("medDiscWithoutInsurance"+rowsLength).value != '' )
        medDiscWithoutInsurance = document.getElementById("medDiscWithoutInsurance"+rowsLength).value;

    if(isTpaBill && medDiscWithInsurance != null && medDiscWithInsurance != undefined && medDiscWithInsurance != '') {
        discount = medDiscWithInsurance;
    } else if(medDiscWithoutInsurance!= null && medDiscWithoutInsurance != undefined && medDiscWithoutInsurance != ''){
        discount = medDiscWithoutInsurance;
    }


    setSellingPrice(batch, qtyForBatch, 0, medicineId, discount, patient, rowsLength);
    



	var taxRate = 0;
	var taxAmt = 0;

	for(var i = 0; i < taxDetails.tax_map.length; i++) {
		var taxMap = taxDetails.tax_map;
		for(var j=0; j < subgroupNamesList.length; j++) {
			if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id]
				&& taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id != null 
					&& taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
				setFieldValue('ed_taxsubgroupid', taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id);
				setFieldValue('taxname', subgroupNamesList[j].item_subgroup_name, subgroupNamesList[j].item_group_id);
				setFieldValue('taxrate', taxMap[i][subgroupNamesList[j].item_subgroup_id].rate, subgroupNamesList[j].item_group_id);
				setFieldValue('taxamount', taxMap[i][subgroupNamesList[j].item_subgroup_id].amount, subgroupNamesList[j].item_group_id);
				setFieldValue('taxsubgroupid', taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id);
				taxRate += parseFloat(taxMap[i][subgroupNamesList[j].item_subgroup_id].rate);
				taxAmt += parseFloat(taxMap[i][subgroupNamesList[j].item_subgroup_id].amount);
			}
		}
	}
	
	// setFieldValue('tax_rate', taxRate);




	var itemDetails = {
		"medicine_id" : medicineId,
		"item_name" : medicineName,
		"batch_no" : batch.batch_no,
		"item_batch_id" : batch.item_batch_id,
		"exp_date" : batch.exp_dt,
		"qty" : ( batch.identification == 'S' ? 1 : qtyForBatch ),
		"package_size" : batch.issue_base_unit,
		//skipping unit_size
		"is_billable" : batch.billable,
		"cat_payable" : calimbaleInfo,
		//skipping control_type_id
		"control_type_name" : controlTypeName,
		"mrp" : amountDetails.mrp,
		"unit_mrp" : amountDetails.unit_mrp,
		"org_mrp" : amountDetails.original_mrp,
		"discount_amt" : amountDetails.discount_amt,
		"discount_per" : amountDetails.discount_per,
		"issue_base_unit" : batch.issue_base_unit,
		"issueuom" : indent.qty_unit,
		"stktype" : document.forms[0].stocktype.value,
		"tax_per" : taxRate,
		"tax_amt" : taxAmt,
		"original_tax_amt" : taxAmt,
		"tax_type" : taxDetails.tax_basis,
		"amt" :  parseFloat(taxDetails.net_amount)-parseFloat(taxDetails.discount_amount),
		"category_id" : batch.category_id,
		"insurance_category_id" : batch.insurance_category_id == null ? 0 : batch.insurance_category_id,
		"billing_group_id" : (!batch.billing_group_id) ? '' : batch.billing_group_id,
		"priCatPayable" : priCatPayable,
		"item_bar_code_id" : "", //sending blank for now
		"coverdbyinsuranceflag" : calimbaleInfo,
		"origUnitMrp" : amountDetails.original_unit_mrp


		// issuebaseunit and package size are set to batch.issue_base_unit in getItemBatchDetails


	}

	for(var i =0; i<groupListJSON.length; i++) {
		var itemGroupId = groupListJSON[i].item_group_id;
		copyFieldValue('taxname', 'taxname', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxrate', 'taxrate', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxamount', 'taxamount', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxsubgroupid', 'taxsubgroupid', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
	}
	addToInnerHTML(itemDetails, rowsLength);
}

/*
 * Find if there is another entry of the same medicineId/Batch in the grid,
 * given the batch object and an index that is ourselves (so that we don't give a duplicate
 * error for ourselves
 */
function getDuplicateIndex(batch, selfIndex) {
	var numItems = document.getElementById("itemListtable").rows.length-1;
	var duplicateCount = 0;

	for (var i=1; i<=numItems; i++) {
		if (i == selfIndex)
			continue;
		var medicineId = document.getElementById('medicine_id'+i).value;
		var itemIdentifier = document.getElementById('item_identifier'+i).value;

		if ((medicineId == batch.medicine_id) && (itemIdentifier == batch.batch_no))
			return i;
	}
	return -1;
}



function isMedicineInGrid(lookupMedicineId) {
	var numItems = document.getElementById("itemListtable").rows.length-1;
	for (var i=1; i<=numItems; i++) {
		var medicineId = document.getElementById('medicine_id'+i).value;

		if (medicineId == lookupMedicineId)
			return true;
	}
	return false;
}


function addMedicinesFromIndents(patientIndents){

	var msg = "";
	for (var p=0; p<patientIndents.length; p++) {
		var indent = patientIndents[p];
		var medicineId = indent.medicine_id;
		var medBatches = gMedicineBatches[medicineId];

		if (medBatches == null || medBatches.length == 0) {
			msg += msg.endsWith("\n") ? "" : "\n";
			msg += getString("js.sales.issues.storesuserissues.warning.nostockavailable");
			msg+=indent.indent_medicine_name;
			msg += "': ";
			msg += indent.qty;
			alert(msg);
			continue;
		}

		var billable = medBatches[0].billable;
		addIndentItems(medicineId, indent.qty,indent,p+1,billable)
	}
}

function getAutoFillIndents() {
	var storeId = document.getElementById("store").value;
	for (var i=0;i<storesJSON.length;i++) {
		if (storesJSON[i].dept_id == storeId) {
			if (storesJSON[i].auto_fill_indents) return true;
			else return false;
		}
	}
}

/*
 * Show the patient details in the patient section for the given patient object
 */
function setPatientDetails(patient) {
	var patientDetails = patient.patient;
	var visitDetails = patient.visit;
	var patientDetailsPlanDetails = patient.insurance;
	
	var mName = patientDetails.middle_name == null ? '' : patientDetails.middle_name;
	var lName = patientDetails.last_name == null ? '' : patientDetails.last_name;
	setNodeText('patientMrno', patientDetails.mr_no, null, patientDetails.mr_no);
	setNodeText('patientName', patientDetails.patient_name + ' ' + mName + ' ' + lName, null,
			patientDetails.patient_name + ' ' + mName + ' ' + lName);
	var patientAgeSex = patientDetails.age_text + ' / ' + patientDetails.patient_gender +
		(patientDetails.dateofbirth == null ? '' : " (" + formatDate(new Date(patientDetails.dateofbirth)) + ")");
	setNodeText('patientAgeSex', patientAgeSex, null, patientAgeSex);
	var referredBy = visitDetails.refdoctorname == null ? "" : visitDetails.refdoctorname;
	setNodeText('referredBy', referredBy, null, referredBy);
	var admitDate = visitDetails.reg_date + " " + visitDetails.reg_time;
	setNodeText('admitDate', admitDate, null, admitDate);

	setNodeText('patientVisitNo', visitDetails.patient_id, null, visitDetails.patient_id);
	setNodeText('patientDept', visitDetails.admitted_dept_name, null, visitDetails.admitted_dept_name);
	setNodeText('patientDoctor', visitDetails.doctor_name == null ? '' : visitDetails.doctor_name, null,
			visitDetails.doctor_name == null ? '' : visitDetails.doctor_name);

	document.patientissueform.visitId.value = visitDetails.patient_id;
    document.patientissueform.visitType.value = visitDetails.visit_type;
    
    gBedType = visitDetails.alloc_bed_type == null
		||visitDetails.alloc_bed_type == '' ? visitDetails.bill_bed_type:visitDetails.alloc_bed_type;
	if (visitDetails.visit_type == 'i') {
		var bed_type = visitDetails.alloc_bed_type == null
			||visitDetails.alloc_bed_type == '' ?visitDetails.bill_bed_type:visitDetails.alloc_bed_type+'/'+visitDetails.alloc_bed_name;
		setNodeText('patientBedType', bed_type, null, bed_type);
	} else setNodeText('patientBedType', '', null, '');

	reInit(getField('mrno'));
	document.getElementById("planId").value = visitDetails.plan_id;

	var orgId = visitDetails.org_id == null ? '' : visitDetails.org_id;
    var orgName = visitDetails.org_name == null ? '' : visitDetails.org_name;

	var isPrimarySponsorAvailable = patientDetailsPlanDetails[0] != null && patientDetailsPlanDetails[0].sponsor_type &&
										(patientDetailsPlanDetails[0].sponsor_type != null || patientDetailsPlanDetails[0].sponsor_type != "");
	
	var isSecondarySponsorAvailable = patientDetailsPlanDetails[1] != null && patientDetailsPlanDetails[1].sponsor_type &&
										(patientDetailsPlanDetails[1].sponsor_type != null || patientDetailsPlanDetails[1].sponsor_type != "");
	var planIndex = 0;

    if(isPrimarySponsorAvailable) {
    	document.getElementById("primarySponsorRow").style.display = 'table-row';
    	// node, text, length, title...
    	setNodeText('ratePlan', visitDetails.org_name == null ? "" : visitDetails.org_name, null,
    			visitDetails.org_name == null ? "" : visitDetails.org_name);

		if( visitWithPlan() ){
			planIndex = patientDetailsPlanDetails.length;

			document.getElementById('priSponsorType').parentNode.style.display = 'table-cell';
			document.getElementById('priSponsorName').parentNode.style.display = 'table-cell';
			setNodeText("priSponsorType", "TPA/Sponsor:", null, "TPA/Sponsor:");
	  		setNodeText('priSponsorName', patientDetailsPlanDetails[0].tpa_name, null,  patientDetailsPlanDetails[0].tpa_name);

			setNodeText('priIDName', 'Insurance Co.:', null, 'Insurance Co.:');
  			setNodeText('priID', patientDetailsPlanDetails[0].insurance_co_name, null,  patientDetailsPlanDetails[0].insurance_co_name);
	  		setNodeText('priPlanType', patientDetailsPlanDetails[0].plan_type_name == null ? "" : patientDetailsPlanDetails[0].plan_type_name, null,
	  				patientDetailsPlanDetails[0].plan_type_name == null ? "" : patientDetailsPlanDetails[0].plan_type_name);
		  	setNodeText('priPlanname', patientDetailsPlanDetails[0].plan_name == null ? "" : patientDetailsPlanDetails[0].plan_name, null,
		  			patientDetailsPlanDetails[0].plan_name == null ? "" : patientDetailsPlanDetails[0].plan_name);
		  	setNodeText('priPolicyId', patientDetailsPlanDetails[0].member_id == null ? "" : patientDetailsPlanDetails[0].member_id, null,
		  			patientDetailsPlanDetails[0].member_id == null ? "" : patientDetailsPlanDetails[0].member_id);

		  	setNodeText('discountPlanId', patientDetailsPlanDetails[0].discount_plan_id == null ? "" : patientDetailsPlanDetails[0].discount_plan_id, null,
					  		patientDetailsPlanDetails[0].discount_plan_id == null ? "" : patientDetailsPlanDetails[0].discount_plan_id);
  		}


  		// Set and display Plan details
  		var insurance_category = patientDetailsPlanDetails[0].plan_type_name == null ? 0 : patientDetailsPlanDetails[0].plan_type_name;
		var planId = !visitWithPlan() ? 0 : patientDetailsPlanDetails[0].plan_id;
		if (planId != 0) {
		   	document.getElementById("pritpaextrow").style.display = 'table-row';

		   	document.getElementById('networkPlanTypeLblCell').style.display = corporateInsurance == 'N' ? 'table-cell' : 'none';
			document.getElementById('networkPlanTypeValueCell').style.display = corporateInsurance == 'N' ? 'table-cell' : 'none';
		} else {
		   	document.getElementById("pritpaextrow").style.display = 'none';
		}
	}

    planIndex = ( planIndex == 0 ? (patientDetailsPlanDetails.length >= 1 ? patientDetailsPlanDetails.length : 0) : ( patientDetailsPlanDetails.length > 1 ? patientDetailsPlanDetails.length : 0));
	if(isSecondarySponsorAvailable) {
		document.getElementById("secSponsorRow").style.display = 'table-row';
    	// node, text, length, title...

  		setNodeText("secSponsorType", "sec.TPA/Sponsor:", null, "sec.TPA/Sponsor:");
  		setNodeText('secSponsorName', patientDetailsPlanDetails[1].tpa_name, null,  patientDetailsPlanDetails[1].tpa_name);

  		// Set and display Plan details
  		var insurance_category = patientDetailsPlanDetails[1].plan_type_name == null ? 0 : patientDetailsPlanDetails[1].plan_type_name;
		var planId = (planIndex >= 0) ? patientDetailsPlanDetails[planIndex-1].plan_id : 0 ;
		if (planIndex > 0)
		   	document.getElementById("sectpaextrow").style.display = 'table-row';
		else
		   	document.getElementById("sectpaextrow").style.display = 'none';

		if( planIndex > 0 ){
			setNodeText('secIDName', "Insurance Co", null, "Sec. Insurance Co");
  			setNodeText('secID', patientDetailsPlanDetails[1].insurance_co_name, null,  patientDetailsPlanDetails[1].insurance_co_name);
		  	setNodeText('secPlanType', patientDetailsPlanDetails[planIndex-1].plan_type_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_type_name, null,
		  		patientDetailsPlanDetails[planIndex-1].plan_type_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_type_name);
		  	setNodeText('secPlanname', patientDetailsPlanDetails[planIndex-1].plan_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_name, null,
		  		patientDetailsPlanDetails[planIndex-1].plan_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_name);
		  	setNodeText('secPolicyId', patientDetailsPlanDetails[planIndex-1].member_id == null ? "" : patientDetailsPlanDetails[planIndex-1].member_id, null,
		  		patientDetailsPlanDetails[planIndex-1].member_id == null ? "" : patientDetailsPlanDetails[planIndex-1].member_id);
	  	}
	}

	if(!isPrimarySponsorAvailable  && (orgId != '' && orgName != 'GENERAL')){
		document.getElementById("primarySponsorRow").style.display = 'table-row';
		setNodeText('ratePlan', visitDetails.org_name == null ? "" : visitDetails.org_name, null, visitDetails.org_name == null ? "" : visitDetails.org_name);
	}


	if(document.getElementById("bill_no").value == ''){
		showMessage("js.sales.issues.storesuserissues.patientnothave.unpaidbills");
	  	document.getElementById("items").value='';
	  	document.getElementById("creditbill").style.display = 'none';
	  	return false;
  	} else {
  		document.getElementById("creditbill").style.display = 'block';
	  }
	  
	var plan_exclusions = document.getElementById('plan_exclusions');
	var plan_notes = document.getElementById('plan_notes');
	var sec_plan_exclusions = document.getElementById('sec_plan_exclusions');
	var sec_plan_notes = document.getElementById('sec_plan_notes');
	if(visitDetails.sponsor_type){
		if(visitDetails.primary_sponsor_id != '' && planIndex >= 0){
			plan_exclusions.innerText = visitDetails.plan_exclusions == null ? "" : visitDetails.plan_exclusions ;
			plan_notes.innerText = visitDetails.plan_notes == null ? "" : visitDetails.plan_notes ;
		}
	}

	if(visitDetails.sec_sponsor_type){
		if(visitDetails.secondary_sponsor_id != '' && planIndex >= 0){
			sec_plan_exclusions.innerText = patientDetailsPlanDetails[planIndex-1].plan_exclusions == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_exclusions;
			sec_plan_notes.innerText = patientDetailsPlanDetails[planIndex-1].plan_notes == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_notes;
		}
	}

  	if(document.patientissueform)
  		document.patientissueform.orgId.value = visitDetails.org_id;
	initItemAutoComplete();
	setSaleBaseType();
	storeRatePlanId = getStoreRatePlanId(visitDetails.org_id);
}

function populateBillType( patientActiveCreditBills, patient ){
	document.getElementById("bill_no").length=0;
	var mrno = document.getElementById("mrno").value;
	var billNowText = "";
	if(document.getElementById("mrno").value != '') {
		if(patientActiveCreditBills != null && patientActiveCreditBills.length > 0) {
			for(var i=0;i<patientActiveCreditBills.length;i++) {
				var visitid = patientActiveCreditBills[i].visit_id;
				if(document.forms[0].visitId.value == patientActiveCreditBills[i].visit_id ){ visitid = visitid };
				if(patientActiveCreditBills[i].status == 'A' && patientActiveCreditBills[i].payment_status == 'U') {
					if (patientActiveCreditBills[i].is_primary_bill == 'Y' && patientActiveCreditBills[i].bill_type == 'C') {
						addOption(document.getElementById("bill_no"),patientActiveCreditBills[i].bill_no + " (Primary) ",patientActiveCreditBills[i].bill_no);
					} else if (patientActiveCreditBills[i].bill_type == 'C'){
						addOption(document.getElementById("bill_no"),patientActiveCreditBills[i].bill_no + " (Secondary) ",patientActiveCreditBills[i].bill_no);
					} else {
						addOption(document.getElementById("bill_no"),patientActiveCreditBills[i].bill_no,patientActiveCreditBills[i].bill_no);
					}
				}
			}
		}
		if ((null != billNo) && (billNo != "")){
			setSelectedIndex(document.getElementById("bill_no"),billNo);
		}
		var isNewUX = getFieldValue('is-new-ux');
		var flowType = patient.visit.visit_type == 'o' ? 'opflow' : 'ipflow';
		var tab = document.getElementById("table_b");
		var row = tab.rows[1];
		var last_element  = row.cells.length;
		if(row &&  last_element >= 1) {
			row.removeChild(row.cells[0]);
		}
		if(patientActiveCreditBills != null && patientActiveCreditBills.length > 0) {
			var td = row.insertCell(-1);
			for(var i=0;i<patientActiveCreditBills.length;i++) {
				var elA = document.createElement('a');
				var txt = document.createTextNode(" | Bill "+patientActiveCreditBills[i].bill_no);
				if(patientActiveCreditBills[i].status == 'A' && patientActiveCreditBills[i].payment_status == 'U') {
					var _patientBillNo = patientActiveCreditBills[i].bill_no;

					var _billLinkPath = isNewUX
						? '/billing/'+ flowType + '/index.htm#/filter/default/' + 'patient/' + patient.visit.mr_no + '/billing/billNo/'
							+ _patientBillNo + '?retain_route_params=true'
						: '/billing/BillAction.do?_method=getCreditBillingCollectScreen&billNo='+_patientBillNo;
					elA.appendChild(txt);
					elA.setAttribute("title", _patientBillNo);
					elA.setAttribute("href", cpath + _billLinkPath);
					td.appendChild(elA);
				}
			}
		}
	}
 }

function addOption(selectbox,text,value ) {
	var optn = document.createElement("OPTION");
	var  selectbox=selectbox;
	optn.text = text;
	optn.value = value;
	selectbox.options.add(optn);
}

function clearPatientDetails() {
	setNodeText('patientMrno', '', null, '');
	setNodeText('patientName', '', null, '');
	setNodeText('patientAgeSex', '', null, '');
	setNodeText('referredBy', '', null, '');
	setNodeText('admitDate', '', null, '');
	setNodeText('patientVisitNo', '', null, '');
	setNodeText('patientDept', '', null, '');
	setNodeText('patientDoctor', '', null, '');
	setNodeText('patientBedType', '', null, '');
	setNodeText('patientInsuranceCo', '', null, '');
	setNodeText('tpa', '', null, '');
	setNodeText('ratePlan', '', null, '');
	setNodeText('planType', '', null, '');
	setNodeText('planname', '', null, '');
	setNodeText('policyId', '', null, '');

	document.getElementById("primarySponsorRow").style.display = 'none';
	document.getElementById("pritpaextrow").style.display = 'none';
	document.getElementById("secSponsorRow").style.display = 'none';
	document.getElementById("sectpaextrow").style.display = 'none';

	document.patientissueform.visitId.value = '';
	document.patientissueform.visitType.value = '';
	document.getElementById("creditbill").style.display = 'none';
	document.getElementById("bill_no").length=0;
	if (showCharges == 'A') {
		document.getElementById("totAmt").textContent = parseFloat(0).toFixed(decDigits);
		document.getElementById("totTax").textContent = parseFloat(0).toFixed(decDigits);
	}

	document.getElementById("creditbill").style.display = 'none';
	document.getElementById("bill_no").length=0;
	if (showCharges == 'A') {
		document.getElementById("totAmt").textContent = parseFloat(0).toFixed(decDigits);
		document.getElementById("totTax").textContent = parseFloat(0).toFixed(decDigits);
	}

	document.getElementById("activePackage").length=0;
	addOption(document.getElementById("activePackage"), "--- Select ---", "");
	isPkg = false;

	var tab = document.getElementById("table_b");
	var row = tab.rows[0];
	var tdlen  = row.cells.length;
	if (row.cells[1]) {
		row.removeChild(row.cells[1]);
	}
	clearIndentDetails();
}

function initItemAutoComplete(){
	if (oItemAutoComp != undefined) {
		oItemAutoComp.destroy();
	}

	if ((typeof(jMedicineNames) != 'undefined') && (jMedicineNames != null)){

		itemNamesArray = jMedicineNames;

		var dataSource = new YAHOO.widget.DS_JSArray(itemNamesArray);
		dataSource.responseSchema = {
			resultsList : "result",
			fields : [ {key : "cust_item_code_with_name"},
			           {key : "medicine_name"},
					   {key : "medicine_id"},
					   {key : "issue_units"},
					   {key : "package_uom"},
					   {key : "issue_base_unit"} ]
	 	};

		oItemAutoComp = new YAHOO.widget.AutoComplete('items','item_dropdown', dataSource);
		oItemAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oItemAutoComp.typeAhead = false;
		oItemAutoComp.useShadow = false;
		oItemAutoComp.allowBrowserAutocomplete = false;
		oItemAutoComp.minQueryLength = 1;
		oItemAutoComp.forceSelection = true;
		oItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		oItemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
		oItemAutoComp.itemSelectEvent.subscribe(onSelectItem);

		oItemAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('items').value;
			if(sInputValue.length === 0) {
				var oSelf = this;
				setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
			} });
		oItemAutoComp.containerCollapseEvent.subscribe(getItemBatchDetails);
	}
}

/*
 * Called on selection of an item in the item auto comp
 */
function onSelectItem(type, args) {
	var selItemName = args[2][1];
	var selItemId = args[2][2];
	setFieldValue("items", selItemName);
	setFieldValue("medicine_id", selItemId);
	if(isNotNullObj(selItemId)) {
		var medDetails = findInList(itemNamesArray, 'medicine_id', selItemId);
		var insuranceCategoryId = medDetails.insurance_category_id;
		setFieldValue('insuranceCategoryId', insuranceCategoryId);
		if (medDetails.billing_group_id) {
			setFieldValue('billingGroupId', medDetails.billing_group_id);
		}
		setFieldValue("barCodeId", medDetails.item_barcode_id);
	}
	document.getElementById("items").focus();
}

function getItemBarCodeDetails (val) {
	if (val == '') {
		resetMedicineDetails();
		setFieldValue('items', '');
		return;
	}
	var flag = false;
	for (var m=0;m<itemNamesArray.length;m++) {
	     var item = itemNamesArray[m];
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, oItemAutoComp);
	     	oItemAutoComp._selectItem(elNewItem);
	     	oItemAutoComp._bItemSelected = true;
	     	setFieldValue('items', itmName);
	     	setFieldValue('medicine_id', item.medicine_id);
	     	setFieldValue('insuranceCategoryId', item.insurance_category_id);
	     	if (item.billing_group_id) {
	     		setFieldValue('billingGroupId', item.billing_group_id);
	     	}
	     	getItemBatchDetails();
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
		clearDialogBox('all');
	 } else {
		 document.getElementById("batch").focus();
	 }
}

function getReport(issueid,gtpass,hospital,type,billNo){
	if(issueid != '0' && issueid !=''){
		if(type == 'Patient') {
			window.open(cpath+'/stores/StockPatientIssuePrint.do?_method=printPatientIssues&issNo='+issueid+'&hospital='+hospital+'&bill_no='+billNo); }
		else {
			window.open(cpath+'/pages/stores/viewstockissues.do?_method=getPrint&report=StoreStockUserIssue&issNo='+issueid+'&hospital='+hospital); }
	}
	if(gtpass == 'true'){
		window.open(cpath+'/patientissues/generategatepass.htm?issNo='+issueid);
	}
}

function openDialogBox(id){
	clearDialogBox('all');
	if ( oItemAutoComp == undefined )
		return false;
	gItemType = 'itemname';
	var visitId = getFieldValue('mrno');
	if(visitId != undefined && visitId != '' && visitId != null) {
		getField('orderKitDetails').style.display = "none";
		getField('orderKitMissedItemsHeader').style.display = "none";
		getField('orderKitMissedItems').style.display = "none";
		setFieldHtml('itemInfo', '');
		setFieldValue('orderkits', '');
		getField('itemname').checked = true;
		getField('order').checked = false;
		getField('itemDetails').style.display = "block";
		if(id < document.getElementById("itemListtable").rows.length -1){
			getField('order').disabled = true;
		}else{
			getField('order').disabled = false;
		}
		getField('itemInfo').style.height = "10px";
	} else {
		getField('orderKitDetails').style.display = "none";
		getField('orderKitMissedItemsHeader').style.display = "none";
		getField('orderKitMissedItems').style.display = "none";
		setFieldHtml('itemInfo', '');
		setFieldValue('orderkits', '');
		getField('itemname').checked = false;
		getField('order').checked = false;
		getField('itemDetails').style.display = "none";
	}
	getField('OK').disabled = false;
	
	var button = getField('tableRow', id);
	setFieldValue('items', '');
	setFieldValue('pkg_size', '');
	setFieldValue('oldMRP', '');
	if (id > 0){
		setFieldValue('itemMrp', '');
		setFieldValue('unitMrp', '');
		setFieldValue('issuQty', '');
		if(allowDiscount == 'A')
			setFieldValue('discount', 0);
		setFieldValue('coverdbyinsuranceflag', '');
		setFieldHtml('coverdbyinsurance', '');
		getField('batch').length = 1;
	}
	var billNo = getFieldValue('bill_no');
	var isTpaBill = getBillIsTpa(billNo);
	if(!isTpaBill) {
		getField('coverdbyinsurancestatusid').style.display = "none";
	} else {
		getField('coverdbyinsurancestatusid').style.display = "";
	}
	var elNewItem = matches(getFieldText('itemLabel', id), oItemAutoComp);
	oItemAutoComp._selectItem(elNewItem);

	setFieldValue('items', decodeURIComponent(getFieldValue('item_name', id)));
	gIndex = id;
	dialog.cfg.setProperty("context",[button, "tr", "br"], false);
	setFieldValue('dialogId', id);
	if(getField('item_identifier', id) && identification_type[getFieldValue('item_batch_id', id)] == 'S'){
		setFieldValue('issuQty', 1);
		getField('issuQty').readOnly = true;
	}else{
		getField('issuQty').readOnly = false;
	}
	dialog.show();
	setFocus();
	var itemUnits = document.getElementById("item_unit");
	if (itemUnits && itemUnits.length > 0) {
		for (var i = 0; i < itemUnits.length; i++) {
			itemUnits.remove(i);
		}
	}
	getItemBatchDetails(id);
	copyFieldValue('origMRP', 'original_mrp', id);
	if (isPkg) {
		document.getElementById("items").disabled = true;
		document.getElementById("itemMrp").disabled = true;
		document.getElementById("discount").disabled = true;
		document.getElementById("item_unit").disabled = true;
		var removeSubGroups = document.getElementsByName("subgroups");
		removeSubGroups.forEach(su => su.disabled = true);
	}
}

function setFocus() {
	if (prefBarCode == 'Y') document.patientissuedailog.barCodeId.focus();
	else document.getElementById("items").focus();
}

function initDialog(){
	dialog = new YAHOO.widget.Dialog("dialog",
		{
			width:"800px",
			context : ["itemListtable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		} );

		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog,
	                                              	correctScope:true} );
		dialog.cfg.queueProperty("keylisteners", escKeyListener);
		dialog.render();
}

function resetMedicineDetails () {
	setFieldValue('items', '');
	setFieldValue('barCodeId', '');
	setFieldValue('issuQty', '');
	setFieldValue('pkg_size', '');
	document.patientissuedailog.batch.length = 1;
	document.patientissuedailog.batch.selectedIndex = 0;
}

function handleCancel() {
	dialog.cancel();
	if (showCharges == 'A'){
		document.getElementById("mrp").value = '';
		document.getElementById("discount").value = '';
		document.getElementById("tax").value = '';
		document.getElementById("taxType").value = '';
		document.getElementById("unit").value = '';
	}
	resetMedicineDetails ();
	clearDialogBox('all');
	document.patientissueform.save.focus();
}

function clearAllHiddenVariables(){
	//document.patientissueform.storeId.value = "" ;
	document.patientissueform.temp_charge_id.value = "" ;
	document.patientissueform.item_name.value = "" ;
	document.patientissueform.item_identifier.value = "" ;
	document.patientissueform.exp_dt.value = "" ;
	document.patientissueform.issue_qty.value = "" ;
	document.patientissueform.stype.value = "" ;
}


function makeButton1(name, id, value){
	var el = document.createElement("button");

	if (name!=null && name!="")
		el.name= name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
}

function AddRowsToGrid(index){
	var itemtable = document.getElementById("itemListtable");
	var tdObj="",trObj="";
	var row = "tableRow" + index;
	var deleteLabel				= makeLabel('itemRow', 'itemRow'+index, '');
	var itemLabel				= makeLabel('itemLabel', 'itemLabel'+index, '');
	var flagImgLbl				= makeLabel('flagImg', 'flagImg'+index, '');
	var controleTypeLabel		= makeLabel('controleTypeLabel', 'controleTypeLabel'+index, '');
	var identifierLabel			= makeLabel('identifierLabel', 'identifierLabel'+index, '');
	var pkgSizeLabel			= makeLabel('pkgSizeLabel', 'pkgSizeLabel'+index, '');
	var expdtLabel				= makeLabel('expdtLabel', 'expdtLabel'+index, '');
	var issue_qtyLabel			= makeLabel('issue_qtyLabel', 'issue_qtyLabel'+index, '');
	var uomLabel				= makeLabel('uomLabel', 'uomLabel'+index, '');
	

	if (showCharges == 'A') {
		var mrpLabel			= makeLabel('mrpLabel', 'mrpLabel'+index, '');
		var unitmrpLabel    	= makeLabel('unitmrpLabel', 'unitmrpLabel'+index, '');
		var pkgmrpLabel     	= makeLabel('pkgMRPLabel', 'pkgMRPLabel'+index, '');
		var unitamtLabel    	= makeLabel('totamtLabel', 'totamtLabel'+index, '');
		var discountLabel   	= makeLabel('discountLabel', 'discountLabel'+index, '');
		var taxLabel				= makeLabel('taxamtLabel', 'taxamtLabel'+index, '');
		var patamtLabel			= makeLabel('totpatamtLabel', 'totpatamtLabel'+index, '');
		var pattaxLabel			= makeLabel('totpattaxLabel', 'totpattaxLabel'+index, '');
		var insamtLabel			= makeLabel('pri_totinsamtLabel', 'pri_totinsamtLabel'+index, '');
		var instaxLabel			= makeLabel('pri_totinstaxLabel', 'pri_totinstaxLabel'+index, '');
		var sec_insamtLabel		= makeLabel('sec_totinsamtLabel', 'sec_totinsamtLabel'+index, '');
		var sec_instaxLabel		= makeLabel('sec_totinstaxLabel', 'sec_totinstaxLabel'+index, '');
	}
	var unitMrpHid        		= makeHidden('unitMrpHid', 'unitMrpHid'+index, '');
	var origUnitMrpHid      	= makeHidden('origUnitMrpHid', 'origUnitMrpHid'+index, '');
	var mrpHid            		= makeHidden('mrpHid', 'mrpHid'+index, '');
	var discountHid        		= makeHidden('discountHid', 'discountHid'+index, '');
	var discountAmtHid      	= makeHidden('discountAmtHid', 'discountAmtHid'+index, '');
	var converedByInsuranceFlag = makeHidden('coverdbyinsuranceflag', 'coverdbyinsuranceflag'+index, '');
	var priTaxHidden			= makeHidden('pri_ins_tax', 'pri_ins_tax'+index, '');
	var secTaxHidden			= makeHidden('sec_ins_tax', 'sec_ins_tax'+index, '');
	var taxSubgroupIds			= makeHidden('tax_sub_group_ids', 'tax_sub_group_ids'+index, '');
	
	

	var temp_charge_idHid   	= makeHidden('temp_charge_id', 'temp_charge_id'+index, '');
	var item_nameHidden 		= makeHidden('item_name', 'item_name'+index, '');
	var itemBarCodeId	 		= makeHidden('item_bar_code_id', 'item_bar_code_id'+index, '');
	var pkgmrp_hidden 			= makeHidden('pkg_mrp', 'pkg_mrp'+index, '');
	var pkgUnit_hidden 			= makeHidden('pkg_unit', 'pkg_unit'+index, '');
	var taxPer_hidden 			= makeHidden('tax_per', 'tax_per'+index, '');
	var taxType_hidden 			= makeHidden('tax_type', 'tax_type'+index, '');
	var taxAmt_hidden 			= makeHidden('tax_amt', 'tax_amt'+index, '');
	var orgTaxAmt_hidden 		= makeHidden('original_tax', 'original_tax'+index, '');
	var amt_hidden 				= makeHidden('amt', 'amt'+index, '');
	var category_hidden 		= makeHidden('category', 'category'+index, '');
	var insCategoryHid      	= makeHidden('insurancecategory', 'insurancecategory'+index, '');
	var bilGroupHid             = makeHidden('billinggroup', 'billinggroup'+index, '');
	var patPkgContentId_hidden  = makeHidden('pat_pkg_content_id','pat_pkg_content_id'+index, '');
	var pkgObjId_hidden         = makeHidden('pack_ob_id','pack_ob_id'+index, '');
	
	
	var item_identifierHid  	= makeHidden('item_identifier', 'item_identifier'+index, '');
	var itemBatchId 			= makeHidden('item_batch_id', 'item_batch_id'+index, '');
	var exp_dtHidden 			= makeHidden('exp_dt', 'exp_dt'+index, '');
	var stypeHidden 			= makeHidden('stype', 'stype'+index, '');
	var issue_qtyHidden			= makeHidden('issue_qty', 'issue_qty'+index, '');
	var pkg_issue_qtyHidden     = makeHidden('pkg_issue_qty', 'pkg_issue_qty'+index, '');
	var item_billable_hid 		= makeHidden('item_billable_hidden', 'item_billable_hidden'+index, '');
	var hdeletedHidden 			= makeHidden('hdeleted', 'hdeleted'+index, '');
	var patInsClaimAmtHid   	= makeHidden('pri_ins_amt','pri_ins_amt'+index, '');
	var secPatInsClaimAmtHid	= makeHidden('sec_ins_amt','sec_ins_amt'+index, '');
	var originalMRP        		= makeHidden('original_mrp','original_mrp'+index, '');
	var itemUnit        		= makeHidden('item_unit','item_unit'+index, '');
	var indentItemId        	= makeHidden('indent_item_id','indent_item_id'+index, '');
	var patIndentNo        		= makeHidden('patient_indent_no','patient_indent_no'+index, '');
	var medicineId        		= makeHidden('medicine_id','medicine_id'+index, '');
	var catPayable        		= makeHidden('cat_payable','cat_payable'+index, '');
	var issueBaseUnitHidden     = makeHidden('issue_base_unit','issue_base_unit'+index, '');
	var contypeIdHid			= makeHidden('control_type_id','control_type_id'+index, '');
	var contypeNameHid     		= makeHidden('control_type_name','control_type_name'+index, '');
	var priCatPayableHid	    = makeHidden('priCatPayable','priCatPayable'+index, '');
	var pkgSizeHid	    		= makeHidden('package_size','package_size'+index, '');
	var unitMrpHid	    		= makeHidden('unit_mrp','unit_mrp'+index, '');
	var medDiscWithoutInsurance        =makeHidden('medDiscWithoutInsurance','medDiscWithoutInsurance'+index, '');
	var medDiscWithInsurance        =makeHidden('medDiscWithInsurance','medDiscWithInsurance'+index, '');
	var issueRateExpr        =makeHidden('issueRateExpr','issueRateExpr'+index, '');
	var visitSellingPriceExpr        =makeHidden('visitSellingPriceExpr','visitSellingPriceExpr'+index, '');
    var storeSellingPriceExpr        =makeHidden('storeSellingPriceExpr','storeSellingPriceExpr'+index, '');
	var storeIdHidden 		= makeHidden('storeId','storeId'+index,'');
	var medDisc_hidden 		=makeHidden('medDisc','medDisc'+index,'');
	var isMrkUpRate        =makeHidden('isMarkUpRate','isMarkUpRate'+index, '');
	var first_of_category           =makeHidden('firstOfCategory','firstOfCategory'+index, '');



	
	var buton = makeButton1("addBut", "addBut"+index);
	buton.setAttribute("class", "imgButton");
	buton.setAttribute("onclick","openDialogBox('"+index+"'); return false;");
	buton.setAttribute("title", "Add New Item (Alt_Shift_+)");
	buton.setAttribute("accesskey", "+");
	var itemrowbtn = makeImageButton('add','add'+index,'imgAdd',cpath+'/icons/Add.png');
	buton.appendChild(itemrowbtn);
	trObj = itemtable.insertRow(index);
	trObj.id = row;

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(flagImgLbl);
	tdObj.appendChild(itemLabel);
	tdObj.appendChild(temp_charge_idHid);
	tdObj.appendChild(item_nameHidden);
	tdObj.appendChild(storeIdHidden);

	tdObj.appendChild(item_billable_hid);

	tdObj.appendChild(pkgmrp_hidden);
	tdObj.appendChild(medDisc_hidden);
	tdObj.appendChild(taxPer_hidden);
	tdObj.appendChild(pkgUnit_hidden);
	tdObj.appendChild(taxType_hidden);
	tdObj.appendChild(taxAmt_hidden);
	tdObj.appendChild(orgTaxAmt_hidden);
	tdObj.appendChild(amt_hidden);
	tdObj.appendChild(category_hidden);
	tdObj.appendChild(insCategoryHid);
	tdObj.appendChild(bilGroupHid);
	tdObj.appendChild(patPkgContentId_hidden);
	tdObj.appendChild(pkgObjId_hidden);
	tdObj.appendChild(first_of_category);

	tdObj.appendChild(patInsClaimAmtHid);
	tdObj.appendChild(secPatInsClaimAmtHid);
	tdObj.appendChild(isMrkUpRate);
	tdObj.appendChild(originalMRP);
	tdObj.appendChild(itemUnit);
	tdObj.appendChild(indentItemId);
	tdObj.appendChild(patIndentNo);
	tdObj.appendChild(catPayable);
	tdObj.appendChild(medDiscWithoutInsurance);
	tdObj.appendChild(medDiscWithInsurance);
	tdObj.appendChild(issueRateExpr);
    tdObj.appendChild(visitSellingPriceExpr);
    tdObj.appendChild(storeSellingPriceExpr);
	tdObj.appendChild(issueBaseUnitHidden);
	tdObj.appendChild(contypeIdHid);
	tdObj.appendChild(contypeNameHid);
	tdObj.appendChild(priCatPayableHid);
    tdObj.appendChild(medicineId);
    tdObj.appendChild(priTaxHidden);
    tdObj.appendChild(secTaxHidden);
    tdObj.appendChild(taxSubgroupIds);
    
	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(controleTypeLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(identifierLabel);
	tdObj.appendChild(item_identifierHid);
	tdObj.appendChild(itemBarCodeId);
	tdObj.appendChild(itemBatchId);
	tdObj.appendChild(exp_dtHidden);
	tdObj.appendChild(stypeHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(expdtLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(issue_qtyLabel);
	tdObj.appendChild(issue_qtyHidden);
	tdObj.appendChild(pkg_issue_qtyHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(uomLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(pkgSizeLabel);
	tdObj.appendChild(pkgSizeHid);
    tdObj.appendChild(unitMrpHid);
    for(var i=0; i<groupListJSON.length; i++) {
    	tdObj.appendChild(makeHidden('taxname'+groupListJSON[i].item_group_id, 'taxname'+groupListJSON[i].item_group_id+index, ''));
    	tdObj.appendChild(makeHidden('taxrate'+groupListJSON[i].item_group_id, 'taxrate'+groupListJSON[i].item_group_id+index, ''));
    	tdObj.appendChild(makeHidden('taxamount'+groupListJSON[i].item_group_id, 'taxamount'+groupListJSON[i].item_group_id+index, ''));
    	tdObj.appendChild(makeHidden('taxsubgroupid'+groupListJSON[i].item_group_id, 'taxsubgroupid'+groupListJSON[i].item_group_id+index, ''));
    }

	if (showCharges == 'A') {
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(mrpLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(unitmrpLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pkgmrpLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(discountLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(unitamtLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(taxLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(patamtLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(pattaxLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(insamtLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(instaxLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(sec_insamtLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(sec_instaxLabel);
	}

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(deleteLabel);
	tdObj.appendChild(hdeletedHidden);
	tdObj.appendChild(mrpHid);
	tdObj.appendChild(unitMrpHid);
	tdObj.appendChild(origUnitMrpHid);
	tdObj.appendChild(discountHid);
	tdObj.appendChild(discountAmtHid);
	tdObj.appendChild(converedByInsuranceFlag);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(buton);
}

/*
 * Given expDt of a batch, get the number of days left for it to expire
 */
function getDaysToExpire(expDt) {
	// expDt in ms is first of the month _after_ which it is going to expire. Convert it to
	// the 1st of next month, the actual date when it is considered expired
	// Example: expDt = Sep 09, we store it as 1-Sep-09, but it is considered expired only
	// on or after 1 Oct 09.
	var dateOfExpiry = new Date(expDt);
	var serverdt = getServerDate();
	var diff = daysDiff(getDatePart(getServerDate()), dateOfExpiry);
	return diff;
}

function checkStoreAllocation() {
 	if (gRoleId != 1 && gRoleId != 2 ) {
 		if (deptId == "") {
	 		showMessage("js.sales.issues.storesuserissues.thereisnoassignedstore");
	 		document.getElementById("storecheck").style.display = 'none';
	 		noAccess = true;
 		}
 	} else if (deptId == "") {
		// no default store for admin/instaadmin. it will pick the first store, need to initialize autocomp
		// based on the first store.
		var storeObj = document.patientissueform.store;
		if (!storeObj)
			storeObj = document.stockissueform.store;
		getMedicinesForStore(storeObj, initItemAutoComplete)
	}
}

function reCalcRowAmounts(rowObj) {
	var pkgUnit = getElementAmount(getField('pkg_unit', rowObj));
	var billNo = document.getElementById('bill_no').value;

	// these are user inputs, editable
	var mrpPaise = getElementPaise(getField('mrpHid', rowObj));
	var origMrpPaise = getElementPaise(getField('original_mrp', rowObj));
	var qty = getElementAmount(getField('issue_qty', rowObj));
	var discountPer = parseFloat(getField('discountHid', rowObj).value);
	if (discountPer > 100){
		discountPer = 100.00;
	}

	/*
	 * Calculations follow. Note that we only use unit rates unlike sales screen. This
	 * means that if package MRP/Rate is not a multiple of 10 paise, then, we can end
	 * up with amount not equal to package MRP when selling a whole package. This is
	 * intentional, so as to keep the billing entry rate*qty=amount intact.
	 */
	var pkgUnit = getElementAmount(getField('pkg_unit', rowObj));
	var unitRatePaise = Math.round(mrpPaise / pkgUnit);
	var origUnitRatePaise = Math.round(origMrpPaise / pkgUnit);
	var amtPaise = unitRatePaise * qty;
	var discountPaise = Math.round(amtPaise * discountPer / 100);
	var netAmtPaise = amtPaise - discountPaise;

	setFieldValue('origUnitMrpHid', formatAmountPaise(origUnitRatePaise), rowObj);
	setFieldValue('unitMrpHid', formatAmountPaise(unitRatePaise), rowObj);
	setFieldValue('discountAmtHid', formatAmountPaise(discountPaise), rowObj);
	setFieldValue('amt', formatAmountPaise(netAmtPaise), rowObj);
	
	if (showCharges == 'A') {
		setFieldText('totamtLabel', formatAmountPaise(netAmtPaise), rowObj);
		setFieldText('discountLabel', formatAmountPaise(discountPaise), rowObj);
	}
	if(!getBillIsTpa(billNo)) {
		var finalAmt = getFieldValue('amt', rowObj);
		var finalTax = getFieldValue('tax_amt', rowObj);
	
		if (showCharges == 'A'){
			if(getFieldValue('item_billable_hidden', rowObj) == 'true') {
				setFieldText('totpatamtLabel', parseFloat(finalAmt).toFixed(decDigits), rowObj);
				setFieldText('totpattaxLabel', parseFloat(finalTax).toFixed(decDigits), rowObj);
				setFieldText('pri_totinsamtLabel', formatAmountPaise(0), rowObj);
				setFieldText('pri_totinstaxLabel', formatAmountPaise(0), rowObj);
				setFieldText('sec_totinsamtLabel', formatAmountPaise(0), rowObj);
				setFieldText('sec_totinstaxLabel', formatAmountPaise(0), rowObj);
			} else {
				setFieldText('totpatamtLabel', formatAmountPaise(0), rowObj);
				setFieldText('totpattaxLabel', formatAmountPaise(0), rowObj);
				setFieldText('pri_totinsamtLabel', formatAmountPaise(0), rowObj);
				setFieldText('pri_totinstaxLabel', formatAmountPaise(0), rowObj);
				setFieldText('sec_totinsamtLabel', formatAmountPaise(0), rowObj);
				setFieldText('sec_totinstaxLabel', formatAmountPaise(0), rowObj);
				setFieldValue('totamtLabel', formatAmountPaise(0), rowObj);
				setFieldValue('discountLabel', formatAmountPaise(0), rowObj);
			}
		}
		setFieldValue('pri_ins_amt', 0, rowObj);
		setFieldValue('pri_ins_tax', 0, rowObj);
		setFieldValue('sec_ins_amt', 0, rowObj);
		setFieldValue('sec_ins_tax', 0, rowObj);
		if (showCharges == 'A') resetTotals();
	}
	
}

function getBillIsTpa(billNo){
	var isTpa = false;
	if (null != patient && null != patient.visit) {
	  if( null != patient.visit.bills){
      var visitBills = patient.visit.bills;
      for(var i = 0;i < visitBills.length ; i++){
        isTpa = visitBills[i].bill_no == billNo && visitBills[i].is_tpa;
        if ( isTpa ) break;
      }
    }
    if (!isTpa && null != patient.visit.multi_package_bills) {
      var multiPackageBills = patient.visit.multi_package_bills;
      for (var j = 0; j < multiPackageBills.length; j++) {
        isTpa = multiPackageBills[j].bill_no === billNo
            && multiPackageBills[j].is_tpa;
        if (isTpa) {
          break;
        }
      }
    }
  }
	return isTpa;
}

function isBillSponsor (rowObj) {

	//need to get whether this item is claimable.
	var itemName = document.getElementById("item_name"+rowObj).value;
	var billNo = document.getElementById('bill_no').value;
	var planId = document.getElementById("planId").value;
	var billNo = document.getElementById("bill_no").value;

	var url;
	if ( (!visitWithPlan() && !hasMoreThanOnePlan()) || !getBillIsTpa(billNo) ) {
		var ajaxReqObject = new XMLHttpRequest();
		//AJAX call to get whether this med. is claimable. If TPA is set, and category of item is claimable, the amt is claimable
		url = "StockPatientIssue.do?_method=isSponsorBill&medName="+itemName+"&billNo="+billNo;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					getFinalAmts (ajaxReqObject.responseText, rowObj);
			}
		}
	}else {
		getFinalAmts ('"Y"', rowObj);
	}
}

function onChangeBill() {
	//reCalculateInsurAmt();
	if (document.getElementById('activePackage').value) {
		getPackageDetails(document.getElementById('activePackage').value);
		return;
	}
	getAllAmounts();
}

function setCorrectAmt(discount,qty,rate,i) {
    var netAmt = rate * qty;
    var dis = (discount * netAmt)/100.00;
    var finalAmt = netAmt - dis;
    document.getElementById("discountAmtHid"+i).value = parseFloat(dis).toFixed(decDigits);
    document.getElementById("discountLabel"+i).textContent = parseFloat(dis).toFixed(decDigits);
    document.getElementById("totamtLabel"+i).textContent = parseFloat(finalAmt).toFixed(decDigits);
    document.getElementById("amt"+i).value = parseFloat(finalAmt).toFixed(decDigits);
}

function reCalculateInsurAmt() {
	var rowCount = document.getElementById("itemListtable").rows.length;
	for (var i=1;i<rowCount-1;i++) {
		var itemName = document.getElementById("item_name"+i).value;
		var catPayable = document.getElementById("cat_payable"+i).value;
		var billNo = document.getElementById('bill_no').value;
		var isTpaBill = getBillIsTpa(billNo);
		itemName = replaceAll(itemName, "%20", " ");
		if(isTpaBill && catPayable == 'f') {
			document.getElementById("itemLabel"+i).innerHTML ='<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+itemName;
		} else {
			document.getElementById("itemLabel"+i).innerHTML =itemName;
		}

		var discount = 0;
        discount = document.getElementById("discountAmtHid"+i).value;

        var qty = document.getElementById("issue_qty"+i).value;
        var rate = document.getElementById("mrpLabel"+i).textContent;
        var unitRate = document.getElementById("unitmrpLabel"+i).textContent;
        var pkgUnit = getElementAmount(document.getElementById("pkg_unit"+i));
        var itemUnit = document.getElementById("item_unit"+i).value;
        var medicineId = document.getElementById("medicine_id"+i).value;
        if(itemUnit == 'I') {
        	rate = unitRate;
        }  else {
        	qty = Math.ceil(qty / pkgUnit);
        }
        setSellingPrice(null, qty, rate, medicineId, discount, patient, i);
        rate = getFieldValue("pkg_mrp", i);
        setCorrectAmt(discount,qty,rate,i);
		isBillSponsor (i);
	}
}

function getAllAmounts() {
	var form = document.getElementById('patientissueform');
	var billNo = getFieldValue('bill_no');
	var isTpaBill = getBillIsTpa(billNo);
	var rowCount = document.getElementById("itemListtable").rows.length;
	var url = cpath + '/patientissues/getbulkitemamountdetails.json';
	setFieldValue('is_tpa', isTpaBill);
	setFieldValue('mr_no_hid', getFieldHtml('patientMrno'));
	if(rowCount > 0)
		asyncPostForm(form, url, true, setAmounts, onFailure);
}

function setAmounts(response) {
	if (response.responseText != undefined) {
		var amountsList = eval('(' + response.responseText + ')');
		amountsList = amountsList.mapList;
		var billNo = document.getElementById('bill_no').value;
		var isTpaBill = getBillIsTpa(billNo);
		if(amountsList.length > 0) {
			for(var i = 0; i < amountsList.length; i++) {
				var itemAmount = amountsList[i];
				var chargeId = itemAmount.temp_charge_id;
				var amountDetails = itemAmount.amount_details;
				var taxDetails = itemAmount.tax_details;
				var taxRate = 0;
				var taxAmt = 0;
				var amt = 0;
				if(isNotNullObj(chargeId)) {
					var rowId = chargeId.replace('_', '');
					setFieldValue('pkg_mrp', amountDetails.mrp, rowId);
					setFieldValue('original_mrp', amountDetails.mrp, rowId);
					setFieldValue('mrpHid', amountDetails.mrp, rowId);
					setFieldHtml('mrpLabel', amountDetails.mrp, rowId);
					setFieldHtml('pkgMRPLabel', amountDetails.mrp, rowId);
					
					setFieldValue('unit_mrp', amountDetails.unit_mrp, rowId);
					setFieldValue('origUnitMrpHid', amountDetails.original_unit_mrp, rowId);
					setFieldHtml('unitmrpLabel', amountDetails.unit_mrp, rowId);
					
					setFieldValue('discountAmtHid', amountDetails.discount_amt, rowId);
					setFieldValue('discountHid', amountDetails.discount_per, rowId);
					setFieldHtml('discountLabel', amountDetails.discount_amt, rowId);
					
					setFieldHtml('totamtLabel', parseFloat(taxDetails.net_amount)-parseFloat(taxDetails.discount_amount), rowId);
					setFieldValue('amt', parseFloat(taxDetails.net_amount)-parseFloat(taxDetails.discount_amount), rowId);
					
					for(var k = 0; k < taxDetails.tax_map.length; k++) {
						var taxMap = taxDetails.tax_map;
						for(var j=0; j < subgroupNamesList.length; j++) {
							if(taxMap[k] && taxMap[k][subgroupNamesList[j].item_subgroup_id] && 
									taxMap[k][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id && taxMap[k][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id != null 
							       && taxMap[k][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
								setFieldValue('taxrate', taxMap[k][subgroupNamesList[j].item_subgroup_id].rate, subgroupNamesList[j].item_group_id+rowId);
								setFieldValue('taxamount', taxMap[k][subgroupNamesList[j].item_subgroup_id].amount, subgroupNamesList[j].item_group_id+rowId);
								setFieldValue('taxsubgroupid', taxMap[k][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id+rowId);
								taxRate += parseFloat(taxMap[k][subgroupNamesList[j].item_subgroup_id].rate);
								taxAmt += parseFloat(taxMap[k][subgroupNamesList[j].item_subgroup_id].amount);
							}
							
						}
					}
					
					setFieldValue('tax_amt', taxAmt, rowId);
					setFieldValue('original_tax', taxAmt, rowId);
					setFieldValue('tax_per', taxRate, rowId);
					setFieldHtml('taxamtLabel', taxAmt.toFixed(decDigits), rowId);
					setFieldHtml('totpattaxLabel', taxAmt.toFixed(decDigits), rowId);
					
					if(isNotNullValue('medicine_id', rowId)) {
						var medicineId = getFieldValue('medicine_id', rowId);
						var medDetails = findInList(itemNamesArray, 'medicine_id', medicineId);
						var insuranceCategoryId = medDetails.insurance_category_id;
						if(visitWithPlan()){
							var priCatPayableDetails = findInList(patient.insurance[0].insurance_plan_details, 'insurance_category_id', insuranceCategoryId);
							if(isNotNullObj(priCatPayableDetails)) {
								var priCatPayable = priCatPayableDetails.category_payable == 'Y' ? 't' : 'f';
								setFieldValue('priCatPayable', priCatPayable, rowId);
								var planCatPayable = priCatPayable;
								if(hasMoreThanOnePlan())  {
									var secCatPayableDetails = findInList(patient.insurance[1].insurance_plan_details, 'insurance_category_id', insuranceCategoryId);
									if(priCatPayableDetails.category_payable == 'N')
										planCatPayable = secCatPayableDetails.category_payable == 'Y' ? 't' : 'f';
								}
								if(planCatPayable != null && planCatPayable != undefined ) {
									if(planCatPayable == 'f') {
										setFieldHtml('coverdbyinsurance', 'No', rowId);
									} else {
										setFieldHtml('coverdbyinsurance', 'Yes');
									}
									
									var stktype = getFieldValue('stype', rowId);
									var medicineName = getFieldValue('item_name', rowId);
									if(planCatPayable == 'f' && isTpaBill) {
										setFieldHtml('itemLabel', '<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+medicineName, rowId);
									} else if (stktype == true || stktype=='t' || stktype == 'true' ) {
										setFieldHtml('itemLabel', '<img class="flag" src= "'+popurl+'/images/grey_flag.gif"/>'+medicineName, rowId);
									}else {
										setFieldText('itemLabel', medicineName, rowId);
									}
									setFieldValue('coverdbyinsuranceflag', planCatPayable, rowId);
								}
							}
						}
					}
					reCalcRowAmounts(rowId);
				}
			}
		}
	}
	var billNo = getFieldValue('bill_no');
	var isTpaBill = getBillIsTpa(billNo);
	if(isTpaBill)
		onClickProcessInsForIssues('patientissueform', 'P');
}

function onFailure() {
	console.error('Unable to fetch amount details.');
}

function replaceAll(str, find, replace) {
	return str.replace(new RegExp(find, 'g'), replace);
}

function getFinalAmts(responseText, rowObj){
	var isInsurance = "";
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;
	var finalAmt = document.getElementById("amt"+rowObj).value ;
	var discount = 0;
	discount = document.getElementById("discountAmtHid"+rowObj).value;

	//if (showCharges == 'A'){
		var patAmt = document.getElementById("patamt").value;
		var patCatAmt = document.getElementById("patcatamt").value;
		var patPer = document.getElementById("patper").value;
		var patCap = document.getElementById("patcap").value;
		var claimAmt = 0;
		var planId = document.getElementById("planId").value;
		var isPost = null;

		var claimAmts = new Array();
		if ( (hasMoreThanOnePlan() || visitWithPlan()) && responseText == "\"Y\"") {
			var visitPlans = getpatPlanDetails();
			var amtAfterClaim = finalAmt;//this is before claim calculation
			for( var j = 0;j<visitPlans.length;j++ ){
				claimAmt = getClaimAmt(visitPlans[j].plan_id,amtAfterClaim,document.patientissueform.visitType.value,document.forms[0].insuranceCategoryId.value, "true",discount);
				if ( document.getElementById("dialogId").value == 1 )
					claimAmts.push(claimAmt);
				if(document.getElementById("dialogId").value!=1) {
					for(var i=1;i<=document.getElementById("itemListtable").rows.length-1;i++) {
						var currentInsuranceCategory = document.forms[0].insuranceCategoryId.value;
			     		if ((currentInsuranceCategory == document.getElementById("insurancecategory"+i).value)
			     			 && ( (parseInt(document.getElementById("dialogId").value) ) != i)) {
			     			claimAmt = getClaimAmt(visitPlans[j].plan_id,amtAfterClaim,document.patientissueform.visitType.value,document.getElementById("insurancecategory"+i).value, "false",discount);
			     			claimAmts.push(claimAmt);
							document.getElementById("firstOfCategory"+rowObj).value = "false";
							document.forms[0].isFirstOfCategory.value = "false";
			     			break;
					 	}
					}
				}
				amtAfterClaim = amtAfterClaim - claimAmt;
				//claim is deducted from aptient amount,now the rest of patient amount is amount for next claim
			}
		} else {
			//check if there is TPA/Sponsor. In that case entire amount is claimable
			if ( responseText == "\"Y\""){
				claimAmt = finalAmt;
				claimAmts.push(finalAmt);
			} else {//if non-sponser bill
				claimAmt = 0;
				claimAmts.push(0);
			}
		}
		var priClaimAmt = 0;
		var secClaimAmt = 0;
		for( var i = 0;i<claimAmts.length;i++ ){
			if( i == 0)
				priClaimAmt = claimAmts[i];
			else
				secClaimAmt = claimAmts[i];
		}
		var patientPortion = parseFloat(finalAmt - (priClaimAmt+secClaimAmt) ).toFixed(decDigits);

		if (showCharges == 'A'){
			if(document.getElementById('item_billable_hidden'+rowObj).value == 'true') {
				document.getElementById("totpatamtLabel"+rowObj).textContent = patientPortion;
				document.getElementById("pri_totinsamtLabel"+rowObj).textContent = parseFloat(priClaimAmt).toFixed(decDigits);
				document.getElementById("sec_totinsamtLabel"+rowObj).textContent = parseFloat(secClaimAmt).toFixed(decDigits);
			} else {
				document.getElementById("totpatamtLabel"+rowObj).textContent = parseFloat(0).toFixed(decDigits);
				document.getElementById("pri_totinsamtLabel"+rowObj).textContent = parseFloat(0).toFixed(decDigits);
				document.getElementById("sec_totinsamtLabel"+rowObj).textContent = parseFloat(0).toFixed(decDigits);
				document.getElementById("amt"+rowObj).value =  parseFloat(0).toFixed(decDigits);
				document.getElementById("totamtLabel"+rowObj).textContent = parseFloat(0).toFixed(decDigits);
				document.getElementById("discountLabel"+rowObj).textContent = parseFloat(0).toFixed(decDigits);
			}
		}
		document.getElementById("pri_ins_amt"+rowObj).value = parseFloat(priClaimAmt).toFixed(decDigits);
		document.getElementById("sec_ins_amt"+rowObj).value = parseFloat(secClaimAmt).toFixed(decDigits);
		//resetTotals();
	//}
	if (showCharges == 'A') resetTotals();
}

function resetTotals(){
	var totalAmountPaise = 0;
	var totalTaxPaise = 0;
	var totalPatAmtPaise = 0;
	var totalClaimAmtPaise = 0;
	var totalClaimTaxPaise = 0;
	var totalPatTaxPaise = 0;
	var itemTable = document.getElementById("itemListtable");
	var tabLen = itemTable.rows.length;
	for (var i=1;i<=tabLen-1;i++){
		if ((document.getElementById('hdeleted'+i).value != 'true') && (document.getElementById('item_billable_hidden'+i).value == 'true')){
			totalAmountPaise += getLPaise(document.getElementById('totamtLabel'+i));
			totalTaxPaise += getLPaise(document.getElementById('taxamtLabel'+i));
			totalPatAmtPaise += getLPaise(document.getElementById("totpatamtLabel"+i));
			totalPatTaxPaise += getLPaise(document.getElementById("totpattaxLabel"+i));
			totalClaimAmtPaise += getLPaise(document.getElementById("pri_totinsamtLabel"+i));
			totalClaimTaxPaise += getLPaise(document.getElementById("pri_totinstaxLabel"+i));
			totalClaimAmtPaise += getLPaise(document.getElementById("sec_totinsamtLabel"+i));
			totalClaimTaxPaise += getLPaise(document.getElementById("sec_totinstaxLabel"+i));
		}
	}
	if(showCharges == 'A'){
		document.getElementById("totAmt").textContent = formatAmountValue(getPaiseReverse(totalAmountPaise + totalTaxPaise));
		document.getElementById("totTax").textContent = formatAmountValue(getPaiseReverse(totalTaxPaise));
		document.getElementById("totPatAmt").textContent = formatAmountValue(getPaiseReverse(totalPatAmtPaise + totalPatTaxPaise));
		document.getElementById("totPatTax").textContent = formatAmountValue(getPaiseReverse(totalPatTaxPaise));
		document.getElementById("totClaimAmt").textContent = formatAmountValue(getPaiseReverse(totalClaimAmtPaise + totalClaimTaxPaise));
		document.getElementById("totClaimTax").textContent = formatAmountValue(getPaiseReverse(totalClaimTaxPaise));
	}

}

function getLPaise(el) {
	if (el && el.textContent && el.textContent != "") {
		return getPaise(el.textContent);
	}
	return 0;
}

function setSaleBaseType (){
	if(orgDetails != undefined && orgDetails != null) {
		for (var j=0; j<orgDetails.length; j++) {
			if (document.patientissueform && document.patientissueform.orgId.value == orgDetails[j].org_id) {
				gDefaultDiscountPer = orgDetails[j]["pharmacy_discount_percentage"];
				gDefaultDiscountType = orgDetails[j]["pharmacy_discount_type"];
			}
		}
	}
}

function calculateTotal(){
	var totalAmount = 0;
	var totalAmountPaise = 0;
	var itemTable = document.getElementById("itemListtable");
	var tabLen = itemTable.rows.length;
	for (var i=1;i<=tabLen-1;i++){
		if ((document.getElementById('hdeleted'+i).value != 'true'))
			totalAmountPaise += getElementPaise(document.getElementById('amt'+i));
	}
	var patientInfo = getPatDetails(patient.patient.mrNo);
	var billNo = document.getElementById('bill_no').value;
	var bill = null;
	for (var i=0; i<patientInfo.bills.length; i++) {
		if (patientInfo.bills[i].bill_no == billNo) {
			bill = patientInfo.bills[i];
			break;
		}
	}
	if (bill != null) {
		var balanceCreditAmount = getPaise(bill.approval_amount) + getPaise(bill.deposit_set_off)
			+ getPaise(bill.total_receipts) - getPaise(bill.total_amount);

		if (totalAmountPaise > balanceCreditAmount) {
			var balanceDisplay = formatAmountPaise(balanceCreditAmount);
			var ok = confirm("Net advances/approved amounts in the bill (" +
					formatAmountValue(getPaiseReverse(balanceCreditAmount)) +
					") is not sufficient to include this sale\n" +
					"Do you want to continue to make the sale?");
			if (!ok)
				return false;
			else
				return true;
		}
	}
	return true;
}


/*
 * Return the patient record, given an mrno. Returns null if patient is not found.
 */
function getPatDetails(mrno) {
	var reqObject = new XMLHttpRequest();
	var storeId = document.getElementById("store").value;
	var saleType="";
	var patientId = patient.visit.patient_id;

	var url = popurl+"/pages/stores/MedicineSalesAjax.do?method=getPatientDetails&patientissue=Y&visitId="+patientId+"&storeId="+document.getElementById("store").value;
	reqObject.open("POST",url.toString(), false);
	reqObject.send(null);

	if (reqObject.readyState == 4) {
		if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
			eval("var patientDetails = "+reqObject.responseText);
			return patientDetails;
		}
	}
	return null;
}

function updateEdit(editObj,status){
	var childEditLen = editObj.childNodes.length;
	for (var i = 0; i < childEditLen; i++){
		var child = editObj.childNodes[i];
		if (child.nodeName == 'IMG'){
			if (status == 'disabled'){
				child.src = cpath+"/icons/Edit1.png";
				editObj.disabled=status;
				break;
			} else{
				child.src = cpath+"/icons/Edit.png";
				editObj.disabled="";
				break;
			}
		}
	}
}

function fillItemDetailsLocal(responseText) {
	if (fillItemBatchDetails(responseText)){
		if (getFieldValue('items') == decodeURIComponent(getFieldValue('item_name', gIndex)) ){
			setFieldValue('issuQty', getFieldText('issue_qtyLabel', gIndex));
		    if(!isNotNullValue('issuQty')) {
		    	document.getElementById("itemname").checked = true;
		    	document.getElementById("order").checked = false;
		    	document.getElementById("order").disabled = true;
		    }
		}
		if(isNotNullValue('medicine_id')) {
			var medicineId = getFieldValue('medicine_id');
			var medDetails = findInList(itemNamesArray, 'medicine_id', medicineId);
			var insuranceCategoryId = medDetails.insurance_category_id;
			if(visitWithPlan()){
				var priCatPayableDetails = findInList(patient.insurance[0].insurance_plan_details, 'insurance_category_id', insuranceCategoryId);
				if(isNotNullObj(priCatPayableDetails)) {
					var priCatPayable = priCatPayableDetails.category_payable == 'Y' ? 't' : 'f';
					setFieldValue('priCatPayable', priCatPayable);
					var planCatPayable = priCatPayable;
					if(hasMoreThanOnePlan())  {
						var secCatPayableDetails = findInList(patient.insurance[1].insurance_plan_details, 'insurance_category_id', insuranceCategoryId);
						if(priCatPayableDetails.category_payable == 'N')
							planCatPayable = secCatPayableDetails.category_payable == 'Y' ? 't' : 'f';
					}
					if(planCatPayable != null && planCatPayable != undefined ) {
						if(planCatPayable == 'f') {
							setFieldHtml('coverdbyinsurance', 'No');
							document.getElementById('coverdbyinsurance').style.color = "red";
						} else {
							setFieldHtml('coverdbyinsurance', 'Yes');
							document.getElementById('coverdbyinsurance').style.color = "#666666";
						}
						setFieldValue('coverdbyinsuranceflag', planCatPayable);
					}
				}
			}
		}
	}
}

function matches(mName, autocomplete) {
	var elListItem = autocomplete._elList.childNodes[0];
	elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
	return elListItem;
}

function submitHandler() {
	document.getElementById('save').disabled = true;
	document.patientissueform.authUser.value = document.getElementById('login_user').value;
	document.patientissueform.action = "StockPatientIssue.do?_method=saveItemsIssued&tran_type=Patient";
	document.patientissueform.submit();
}

function getClaimAmt(planId,amt,visitType,categoryId, firstOfCategory,discount){
	var calculatedClaimAmount = 0;
		//multi-payer
	var calculatedClaimAmount = calculateClaimAmount(planId,amt,visitType,categoryId, firstOfCategory,discount);

	if(calculatedClaimAmount<0){
		 calculatedClaimAmount = 0;
	} else if (patcap != null && patcap != '' && patcap >= 0 && calculatedClaimAmount>=patcap) {
		calculatedClaimAmount= amt-patcap;
	}else if(calculatedClaimAmount>amt){
		calculatedClaimAmount = amt;
	}
	return calculatedClaimAmount;
}

function getStoreRatePlanId(ratePlanId) {
	if(orgDetails != null) {
		var org = findInList(orgDetails, 'org_id', ratePlanId);
		if (org.store_rate_plan_id == null)
			return 0;
		return org.store_rate_plan_id;
	} else {
		return 0;
	}
}

function clearIndentDetails(){
	var table = document.getElementById('itemListtable');

	var numItems = table.rows.length;
	for (var i=1; i<numItems; i++) {
		table.deleteRow(1);
	}
	AddRowsToGrid(1);

	table = document.getElementById('prescInfo');

	numItems = table.rows.length;
	for (var i=1; i<numItems-1; i++) {
		table.deleteRow(1);
	}
	//document.getElementById('prescFieldSet').style.display = 'none';
	document.getElementById('prescDetailsDiv').style.display="none";

}

function validateIssueDate(){
	var issueDate  = document.getElementById('issueDate');
	
	var issueDateStr = issueDate.value;
	if (!doValidateDateField(issueDate, "past")) {
		return false;
	}

	var myarray=issueDateStr.split("-");
    var dt=myarray[0];
    var mth=myarray[1];
    var yr=myarray[2];
    var issueDate = new Date(yr+"-"+mth+"-"+dt);

	var issueTime = document.getElementById("issueTime");
	if (!validateTime(issueTime))
		return false;
	var issueDateObj = document.patientissueform.issueDate;

	var regDate = new Date(patient.visit.reg_date);
	var issueDateTime = getDateTimeFromField(issueDateObj,issueTime);
	var regDateTime = getDateTime(formatDate(new Date(patient.visit.reg_date)),patient.visit.reg_time );

	if ( issueDateTime < regDateTime ) {
		showMessage("js.sales.issues.storesuserissues.issuedatenotlesser.registrationdate");
		return false;
	}

	var billOpenDate = getBillOpenDate(document.getElementById('bill_no').value);
	var billOpenDateTime = new Date(billOpenDate);
	if ( issueDate < billOpenDateTime ) {
		var msg = getString("js.sales.issues.storesuserissues.issuedatenotlesser.bill.open.date");
		msg = msg + " " +formatDate(billOpenDateTime);
		alert(msg);
		return false;
	}
	return true;
}

function hasMoreThanOnePlan(){
	return ( patient != null && !empty(patient.insurance) && patient.insurance.length > 1 );
}

function visitWithPlan(){
	return ( patient != null && !empty(patient.insurance) && patient.insurance.length > 0 );
}

function getpatPlanDetails(){
	return ( patient.insurance );
}

function calculateClaimAmount(planId,amt,visitType,categoryId, firstOfCategory,discount){
	var claimAmount = 0;
	var ajaxReqObject = new XMLHttpRequest();
	url = "StockPatientIssue.do?_method=getClaimAmount&plan_id="+planId+"&amount="+amt+"&visit_type="+visitType+"&category_id="+categoryId+"&foc="+firstOfCategory+"&discount="+discount;
	ajaxReqObject.open("POST",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				claimAmount = eval(ajaxReqObject.responseText);
		}
	}
	return claimAmount;
}


function getBillOpenDate(billNo){
	var visitBills = patient.visit.bills;
	var openDate = null;
	for(var i = 0;i < visitBills.length ; i++){
		if ( visitBills[i].bill_no == billNo ) {
			openDate =  visitBills[i].open_date;
		}
	}
	return openDate;
}

function onChangeIssueType(obj){
	if(obj.value == 'u' && issuetodept == 'N'){
		Hospital_field = document.stockissueform.hosp_user;
		document.stockissueform.hosp_user.disabled = false;
		document.stockissueform.issue_dept.disabled = true;
		document.stockissueform.issue_ward.disabled = true;

		document.stockissueform.issue_dept.value = "";
		document.stockissueform.issue_ward.value = "";
		document.stockissueform.hosp_user.className = "required";

		document.getElementById("hosp_user_mand").style.display = 'block';
		document.getElementById("issue_dept_mand").style.visibility = 'hidden';
		document.getElementById("issue_ward_mand").style.visibility = 'hidden';
	} else if ( obj.value == 'd'){
		Hospital_field = document.stockissueform.issue_dept;
		if( document.stockissueform.hosp_user ){
			document.stockissueform.hosp_user.className = "";
			document.stockissueform.hosp_user.disabled = true;
			document.getElementById("hosp_user_mand").style.display = 'none';
			document.stockissueform.hosp_user.value = "";
		}
		document.stockissueform.issue_dept.disabled = false;
		document.getElementById("issue_dept_mand").style.visibility = 'visible';
		document.stockissueform.issue_ward.disabled = true;
		document.getElementById("issue_ward_mand").style.visibility = 'hidden';
		document.stockissueform.issue_ward.value = "";
	} else{
		Hospital_field = document.stockissueform.issue_ward;
		if ( document.stockissueform.hosp_user ){
			document.stockissueform.hosp_user.disabled = true;
			document.stockissueform.hosp_user.className = "";
			document.stockissueform.hosp_user.value = "";
			document.getElementById("hosp_user_mand").style.display = 'none';
		}
		document.stockissueform.issue_dept.disabled = true;
		document.stockissueform.issue_ward.disabled = false;

		document.stockissueform.issue_dept.value = "";
		document.getElementById("issue_dept_mand").style.visibility = 'hidden';
		document.getElementById("issue_ward_mand").style.visibility = 'visible';
	}

}

/**
 * This method used to get Category Cliamable Staus based on medicineId, visitId and visit type
 *
 * @param medicineId
 * @param asyncStaus
 * @returns {String}
 */
function getCatPayableStatus(medicineId, asyncStatus, pkgId) {
	var catPayableStatus = "";
	var ajaxReqObjectCall = new XMLHttpRequest();
	var visitId = document.getElementsByName("visitId")[0].value;
	var visitType = document.getElementsByName("visitType")[0].value;
	var url = cpath+"/patientissues/getinsurancecategorypaystatus.json" +
		"?visitId="+visitId +
		"&medicineId="+medicineId+
		"&visitType="+visitType;

	if (pkgId) {
		url = url+"&packageId="+pkgId;
	}
	ajaxReqObjectCall.onreadystatechange = function() {
		if (ajaxReqObjectCall.readyState == 4) {
			if ( (ajaxReqObjectCall.status == 200) && (ajaxReqObjectCall.responseText!=null) ) {
				eval("calimbaleInfo = " + ajaxReqObjectCall.responseText)
				catPayableStatus = calimbaleInfo;
			}
		}
	}
	ajaxReqObjectCall.open("GET",url.toString(), asyncStatus);
	ajaxReqObjectCall.send(null);
	return catPayableStatus[0];
}

/**
	This function is use to get issue rate for an item based on issue rate expresstion
**/
function getIssueItemPriceUsingExpr(medicineId) {

	var issueRateExpr = getFieldValue("issue_rate_expr");
	var visitSellingPriceExpr = getFieldValue("visit_selling_expr");
	var storeSellingPriceExpr = getFieldValue("store_selling_expr");
	var billNo = getFieldValue("bill_no");
	var mrp = getFieldValue("mrp");
	if(medicineId == null) {
		var medicine_id = getFieldValue("medicine_id");
	} else {
		medicine_id = medicineId;
	}

	if ((isNotNullObj(issueRateExpr) || isNotNullObj(visitSellingPriceExpr) || isNotNullObj(storeSellingPriceExpr)) && isNotNullObj(medicine_id)) {
		var qty = getFieldValue("issuQty");
		var discount = getFieldValue("discount");
		var itemBatchId = patientissueform.item_batch_id.value;
		if(itemBatchId != undefined && itemBatchId != 'undefined' && itemBatchId != '' && itemBatchId != null) {
			if (qty == undefined || qty == 0 || qty == '') {
				qty = 1;
			}
			var ajaxReqObject = new XMLHttpRequest();
			var url = cpath+'/issues/getmarkuprate.json?';
			var param = 'item_batch_id='+itemBatchId+"&storeId="+document.getElementById("store").value+"&qty="+qty+
							"&medicine_id="+medicine_id+"&bed_type="+gBedType+"&discount="+discount+"&bill_no="+billNo+"&patient_id="+patient.visit.patient_id;
			if(isNotNullObj(patient.visit.store_rate_plan_id)){
				param += "&visitStoreRatePlanId="+patient.visit.store_rate_plan_id;
	    	}
			if(isNotNullObj(issueRateExpr)) {
				param += "&expression_type=I";
	    	} else if(isNotNullObj(visitSellingPriceExpr)) {
	    		param += "&expression_type=V";
	    	} else if(isNotNullObj(storeSellingPriceExpr)) {
	    		param += "&expression_type=S";
	    	}
			param += "&usemrp="+gUseBathMrp;
			ajaxReqObject.open("GET",url.toString()+param, false);
			ajaxReqObject.send(null);
			if (ajaxReqObject.readyState == 4) {
				if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					//issueRate = eval(ajaxReqObject.responseText);
					eval("issueRateBean = " + ajaxReqObject.responseText);
					setIssueItemRates(issueRateBean.sellingPriceBean.map.mrp);
				}
			}
		}
	}
}

function setIssueItemRates(issueRate) {
	if(isNotNullValue("oldMRP")) {
		setFieldValue("oldMRP", getFieldValue("oldMRP"));
	}
	var itemRate = 0;
	if(issueRate != null) {
		itemRate = issueRate;
	} else {
		itemRate = getFieldValue("oldMRP");
	}
	setFieldValue("itemMrp", itemRate.toFixed(2));
	if(itemRate !== 0){
		setFieldValue("unitMrp", (itemRate/getFieldValue("issue_base_unit")).toFixed(2));
		setFieldValue("unit_mrp", (itemRate/getFieldValue("issue_base_unit")).toFixed(2));
	} else {
		setFieldValue("unitMrp", (itemRate).toFixed(2));
		setFieldValue("unit_mrp", (itemRate).toFixed(2));
	}
	setFieldValue("mrp", itemRate.toFixed(2));
	setFieldValue("origMRP", itemRate.toFixed(2));

}

/**
 * This method used to set dialog box according to item type.
 *
 * @returns {Boolean}
 */
function onChangeItemType() {
	var radios = document.getElementsByName('itemtype');
	var itemInfo = document.getElementById("itemInfo");
	var visitId = document.patientissueform.visitId.value;
	if(visitId != undefined && visitId != '' ) {
		for (var i = 0, length = radios.length; i < length; i++) {
		    if (radios[i].checked) {
		    	if(radios[i].value == 'itemname') {
		    		document.getElementById("OK").disabled = false;
		    		document.getElementById("itemDetails").style.display = "block";
		    		if(prefBarCode == 'Y') {
		    			document.getElementById("barCodeId").focus();
		    		} else {
		    			document.getElementById("items").focus();
		    		}
		    		document.getElementById("orderKitDetails").style.display = "none";
		    		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
		    		document.getElementById("orderKitMissedItems").style.display = "none";
		    		document.getElementById("orderkits").value = "";
		    		itemInfo.style.height = '10px';
		    		itemInfo.innerHTML = "";
		    		gItemType = 'itemname';
		    	} else {
		    		document.getElementById("OK").disabled = false;
		    		document.getElementById("itemDetails").style.display = "none";
		    		document.getElementById("orderKitDetails").style.display = "block";
		    		itemInfo.style.height = '150px';
		    		itemInfo.innerHTML = "";
		    		document.getElementById("orderkits").value = "";
		    		document.getElementById("orderkits").focus();
	    			document.getElementById("mrp").value = '';
	    			document.getElementById("discount").value = '';
	    			document.getElementById("tax").value = '';
	    			document.getElementById("taxType").value = '';
	    			document.getElementById("unit").value = '';
	    			document.getElementById("itemMrp").value = '';
	    			document.getElementById("unitMrp").value = '';
	    			resetMedicineDetails ();
	    			gItemType = 'orderkit';
		    	}
		        break;
		    }
		}
	} else {
		showMessage("js.sales.issues.storesuserissues.mrno.required");
		cleanOrderKitDetails();
		handleCancel();
		document.getElementById("mrno").focus();
		return false;
	}

}

/**
 * This method is used to set order kit details based on order kit selection.
 *
 * @param type
 * @param args
 */
function setOrderDetails(type, args) {
	var visitId = document.patientissueform.visitId.value;
	if(visitId != undefined && visitId != '' ) {
		var orderKitName = args[2][0];
		var orderKitId = args[2][1];
		var orderKitItems;
		var ajaxReqObject = new XMLHttpRequest();
		var planId = patient.visit.plan_id;
		var visitId =  patient.visit.patient_id;
		var ratePlanId = patient.visit.org_id;
		var storeRatePlanId = patient.visit.store_rate_plan_id;
		var visitType = patient.visit.visit_type;

		var url = cpath + '/patientissues/getorderkititems.json?order_kit_id='+orderKitId+
		"&storeId="+document.getElementById("store").value+"&planId="+planId+"&visitId="+visitId+"&ratePlanId="+ratePlanId+
		"&storeRatePlanId="+storeRatePlanId+"&visitType="+visitType+"&billable=Y&issueType=CR";
		ajaxReqObject.open("GET",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				orderKitItems = JSON.parse(ajaxReqObject.responseText);
				gOrderKitItems = orderKitItems.order_kit_items;
				gMedicineBatches = orderKitItems.medBatches;
				setOrderKitItems(orderKitItems.order_kit_items_status, orderKitName, orderKitItems.total_items_status, orderKitItems.nonissuableitems);
			}
		}
	}
}

/**
 * This method is used to show the low stock medicines in the order kit.
 *
 * @param obj
 * @param orderKitName
 * @param totalItemsStatus
 */
function setOrderKitItems(obj, orderKitName, totalItemsStatus, nonIssuableItems) {
	var tableRowIndex = 0;
	var isAllItemsAvaiable = true;
	document.getElementById("orderKitMissedItemsHeader").style.display = "block";
	document.getElementById("orderKitMissedItems").style.display = "block";
	var table = document.getElementById("orderKitMissedItems");
	var numItems = table.rows.length;
	var totalItemsStatusArray = totalItemsStatus.split("@");
	if(parseFloat(totalItemsStatusArray[0]) == parseFloat(totalItemsStatusArray[1]) && gStockNegative == 'D') {
		document.getElementById("OK").disabled = true;
	} else {
		document.getElementById("OK").disabled = false;
	}
	var totalUnavaiableItems = totalItemsStatusArray[0]+" / "+totalItemsStatusArray[1];
	for ( var i=0; i<numItems; i++ ) {
		table.deleteRow(0);
	}
	var itemInfo = document.getElementById("itemInfo");
	if(obj != undefined && Object.keys(obj).length !== 0) {
	 	for (var key in obj) {
		  if (obj.hasOwnProperty(key)) {
		    var val = obj[key];
		    var stockArray = val.split("@");
		    var nonIssuableItemName = '';
		    for(var i = 0 ; i < nonIssuableItems.length; i++) {
		    	if(key == nonIssuableItems[i].medicine_name) {
		    		nonIssuableItemName = nonIssuableItems[i];
		    		break;
		    	}
		    }
		    if(nonIssuableItemName != '') {
		    	isAllItemsAvaiable &= false;
		    	var row = table.insertRow(tableRowIndex).outerHTML =
			    	'<td style="white-space: pre-wrap;word-break: break-all;width:350px;padding-left:10px;color:#666666;font-size:11px;border-top: 0px;border-bottom: 0px;">'+key+'</td><td style="text-align:center;border-top: 0px;border-bottom: 0px;">'+
			    	'<b><font color="red">NA</td>';
			    tableRowIndex++;

		    } else if(parseFloat(stockArray[0]) < parseFloat(stockArray[1])) {
		    	isAllItemsAvaiable &= false;
		    	if(parseFloat(stockArray[0]) > 0) {
		    		document.getElementById("OK").disabled = false;
		    	}
		    	var row = table.insertRow(tableRowIndex).outerHTML =
			    	'<td style="white-space: pre-wrap;word-break: break-all;width:350px;padding-left:10px;color:#666666;font-size:11px;border-top: 0px;border-bottom: 0px;">'+key+'</td><td style="text-align:center;border-top: 0px;border-bottom: 0px;">'+
			    	'<b><font color="red">'+parseFloat(stockArray[0])+'</font></b> / '+parseFloat(stockArray[1])+'</td>';
			    tableRowIndex++;
		    }
		  }
		}
	 	if(!isAllItemsAvaiable) {
	 		itemInfo.style.height = '10px';
	 		itemInfo.innerHTML = "<table width='100%' align='left' style='border: 1px #CCCCCC solid;padding: 5px;text-align: left;background-color:white;'>" +
			"<tr><td style='padding-left:7px;font-size:12px;'><b>Items Not Available ("+totalUnavaiableItems+")</b></td></tr></table>";
	 	} else {
	 		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
			document.getElementById("orderKitMissedItems").style.display = "none";
			itemInfo.style.height = '150px';
			itemInfo.innerHTML = "<span style='color: green;padding-left:6px;font-size:12px;'>All items are available of \""+orderKitName+"\"</span>";
	 	}


	} else {
		document.getElementById("orderKitMissedItemsHeader").style.display = "none";
		document.getElementById("orderKitMissedItems").style.display = "none";
		itemInfo.style.height = '150px';
		itemInfo.innerHTML = "<span style='color: green;padding-left:6px;font-size:12px;'>All items are available of \""+orderKitName+"\".</span>";
	}


}

function initOrderKitAutoComplete(){
	if (oOrderKitAutoComp != undefined) {
		oOrderKitAutoComp.destroy();
	}

	if ((typeof(orderKitJSON) != 'undefined') && (orderKitJSON != null)){

		var dataSource1 = new YAHOO.util.LocalDataSource({result: orderKitJSON});
		dataSource1.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		dataSource1.responseSchema = {
			resultsList : "result",
			fields : [ {key : "order_kit_name"}, {key : "order_kit_id"}]
	 	};
		oOrderKitAutoComp = new YAHOO.widget.AutoComplete('orderkits','orderkit_dropdown', dataSource1);
		oOrderKitAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oOrderKitAutoComp.typeAhead = false;
		oOrderKitAutoComp.useShadow = false;
		oOrderKitAutoComp.allowBrowserAutocomplete = false;
		oOrderKitAutoComp.minQueryLength = 0;
		oOrderKitAutoComp.forceSelection = true;
		oOrderKitAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		oOrderKitAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

		oOrderKitAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('orderkits').value;
		});
		oOrderKitAutoComp.itemSelectEvent.subscribe(setOrderDetails);
	}
}

function cleanOrderKitDetails() {
	document.getElementById("orderKitDetails").style.display = "none";
	document.getElementById("orderKitMissedItemsHeader").style.display = "none";
	document.getElementById("orderKitMissedItems").style.display = "none";
	document.getElementById("itemInfo").innerHTML = "";
	document.getElementById("orderkits").value = "";
	document.getElementById("itemname").checked = true;
	document.getElementById("order").checked = false;
	document.getElementById("itemDetails").style.display = "block";
	document.getElementById("itemInfo").style.height = "10px";
}

/**
 * This method is used to add medicines from order kit.
 *
 */
function addMedicinesFromOrderKit(){
	var dialogId = document.getElementById("dialogId").value;
	if(dialogId == undefined || dialogId == '' || dialogId == null) {
		dialogId = 0;
	}
	if(gOrderKitItems != undefined && gOrderKitItems != null) {
		for (var p=0; p<gOrderKitItems.length; p++) {
			var msg = "";
			var medicineId = gOrderKitItems[p].medicine_id;
			var medBatches = gMedicineBatches[medicineId];
			var medicineName = gOrderKitItems[p].medicine_name;
			if (medBatches == null || medBatches.length == 0) {
				msg += msg.endsWith("\n") ? "" : "\n";
				msg += getString("js.sales.issues.storesuserissues.warning.nostockavailable");
				msg += medicineName;
				msg += " : ";
				msg += gOrderKitItems[p].qty_needed;
				alert(msg);
				continue;
			}

			var billable = medBatches[0].billable;
			dialogId = parseInt(dialogId)+p;
			addOrderKitItems(medicineId, gOrderKitItems[p].qty_needed, dialogId+1, billable, gOrderKitItems[p].issue_units);

		}
	}
	var itemtable = document.getElementById("itemListtable");
	var len = itemtable.rows.length;
	openDialogBox(eval(len-1));
}

/**
 * This method is used to add medicines from order kit and validate the medicine qty.
 *
 * @param medicineId
 * @param userQty
 * @param dialogId
 * @param billable
 * @param issueUnit
 * @returns
 */
function addOrderKitItems(medicineId, userQty, dialogId, billable, issueUnit) {

	var allBatches = gMedicineBatches[medicineId];
	var medicineName = allBatches[0].medicine_name;

	/*
	 * The batches excludes zero stock if preferences disallows it. But for expiry,
	 * Even if it is disallowed to sell, we bring the batch here in order to alert the
	 * user saying expired items are available, but we are not allowed to sell.
	 *
	 * Also, the batch list is already sorted for our convenience:
	 *  Availability (ie, qty > 0 comes first)
	 *    Expiry Date
	 *      Available Quantity
	 */

	var remQty = userQty;
	var expiredQty = 0; var nearingExpiryQty = 0;
	var negativeQty = 0; var diffPkgQty = 0;
	var isAlreadyInGrid = isMedicineInGrid(medicineId);
	for (b=0; b<allBatches.length; b++) {

		var batch = allBatches[b];
		var avlblQty = 0;
		exp_dts[batch.item_batch_id] = batch.exp_dt;

		// if batch is already in grid, skip
	    var dupExists = getDuplicateIndex(batch, -1);
		if (dupExists != -1){
			continue;
		}

		// use only whole packages
		avlblQty = batch.qty;

		// check for expiry date for normal sales (ie, not estimate, and not negative stock)
		if (avlblQty > 0) {
			var daysToExpire = batch.exp_dt != null ? getDaysToExpire(batch.exp_dt) : gExpiryWarnDays+1;
			if (daysToExpire <= 0) {
				expiredQty += avlblQty;
				if (gAllowExpiredSale != 'Y') {
					continue;
				}
			} else if (daysToExpire <= gExpiryWarnDays) {
				nearingExpiryQty += avlblQty;
			}
		}

		var qtyForBatch = 0;
		if ((gStockNegative != 'D') && (avlblQty <= 0 || (b == allBatches.length - 1))) {
			// if stock is allowed to be negative, use up all of remQty for the last batch
			// or for the first batch where it is already 0 or negative.
			qtyForBatch = remQty;
			negativeQty = qtyForBatch - avlblQty;
		} else {
			qtyForBatch = Math.min(remQty, avlblQty);
			qtyForBatch = qtyForBatch < 0 ? 0 : qtyForBatch;   // stock can be negative even otherwise.
			if (allowDecimalsForQty == 'Y') {
				// round it off to 2 decimal places to avoid float problems
				qtyForBatch = Math.round(qtyForBatch*100)/100;
			}
		}
		var stktype = document.forms[0].stocktype.value;
		document.getElementById("dialogId").value = dialogId;
		// add to grid
		if (qtyForBatch > 0) {

			if(document.getElementById("itemListtable").rows.length>1){
				for(var i=1;i<=document.getElementById("itemListtable").rows.length-1;i++){
					var currentbatchNo = batch.batch_no;
					var currentMedicineName = medicineName;

					if ( (currentbatchNo==document.getElementById("item_identifier"+i).value)
							&& (encodeURIComponent(currentMedicineName)==document.getElementById("item_name"+i).value)
							&& ( (parseInt(document.getElementById("dialogId").value) ) != i) ){
						var msg = "";
						msg= getString("js.sales.issues.storesuserissues.duplicateentry")+" : ";
						msg+= medicineName;
						alert(msg);
						return false;
					}
				}
			}
			if(expiredQty  == 0 || (expiredQty > 0 && gAllowExpiredSale == 'Y') ){
				addOrderKitItemsToGrid(medicineId, medicineName, batch, issueUnit, qtyForBatch);
			}
		}

		// if no more to be added finish up
		remQty = remQty - qtyForBatch;
		if (remQty == 0)
			break;
	}
	
	var msg = "";
	if(isAlreadyInGrid){
		msg= getString("js.sales.issues.storesuserissues.duplicateentry")+" : ";
		msg+= medicineName;
		alert(msg);
	}else if (remQty > 0) {
		msg= getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg+= medicineName;
		msg+= " : " ;
		msg+= remQty.toFixed(2);
		alert(msg);
		if (expiredQty > 0) {
			msg += " (" ;
			msg+= expiredQty;
			msg+=getString("js.sales.issues.storesuserissues.itemsavailable.pastexpirydate");
			alert(msg);
		}
		if (diffPkgQty > 0) {
			msg += " (";
			msg += diffPkgQty;
			msg +=getString("js.sales.issues.storesuserissues.itemsavailable.differentpackagesize");
			alert(msg);
		}
		msg += "\n";
	}

	if (negativeQty > 0 && gStockNegative != 'A') {
		msg +=getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg += " " + medicineName;
		msg += " : ";
		msg	+= avlblQty < 0 ? (negativeQty + avlblQty).toFixed(2) : negativeQty.toFixed(2);
		msg += getString("js.sales.issues.storesuserissues.proceedingwithissue.causestockbecomenegative");
		msg+="\n";
		alert(msg);
	}

	if (nearingExpiryQty > 0) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor");
		msg += medicineName;
		msg +=getString("js.sales.issues.storesuserissues.aresoontoexpire");
		msg+="\n";
		alert(msg);
	}

	if (expiredQty > 0) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor");
		msg += medicineName;
		msg +=getString("js.sales.issues.storesuserissues.pastexpirydate");
		alert(msg);
	}
	return msg;
}

/**
 * This method is used to add medicines to grid.
 *
 * @param medicineId
 * @param medicineName
 * @param batch
 * @param issue_units
 * @param qtyForBatch
 */
function addOrderKitItemsToGrid(medicineId, medicineName, batch, issue_units, qtyForBatch){
	var itemsTable = document.getElementById("itemListtable");
	var rowsLength = (itemsTable.rows.length)-1;
	var  nextRowLen = eval(rowsLength)+1;
	var nextrow =  document.getElementById("tableRow"+nextRowLen);
	var stktype = batch.consignment_stock;
	if (nextrow == null){
		AddRowsToGrid(nextRowLen);
	}
	//	added for exclusions
	var catPayableInfo = null;
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	var coverdbyinsuranceflag = null;
	catPayableInfo = getCatPayableStatus(medicineId, false);
	if(catPayableInfo!= null && catPayableInfo != undefined ) {
		var calimbaleInfo = catPayableInfo.plan_category_payable != null? catPayableInfo.plan_category_payable:"";
		if(calimbaleInfo == 'f') {
			document.getElementById('coverdbyinsurance').innerHTML = "No";
			document.getElementById('coverdbyinsurance').style.color = "red";
		} else {
			document.getElementById('coverdbyinsurance').innerHTML = "Yes";
			document.getElementById('coverdbyinsurance').style.color = "#666666";
		}
		if(calimbaleInfo != null && calimbaleInfo != undefined && calimbaleInfo == "f" && isTpaBill) {
			document.getElementById("itemLabel"+rowsLength).innerHTML ='<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+medicineName;
		} else if (stktype == true || stktype=='t' || stktype == 'true' ) {
			document.getElementById("itemLabel"+rowsLength).innerHTML ='<img class="flag" src= "'+popurl+'/images/grey_flag.gif"/>'+medicineName;
		}else {
			document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
		}
	} else {
		document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
	}

	var itemRate = isNotNullObj(batch.orig_selling_price) ? batch.orig_selling_price : batch.mrp;


	var response = getItemAmountsForOrderkit(medicineId, batch.item_batch_id, gStoreSaleUnit, qtyForBatch, batch.issue_base_unit, null, null, 'B', billNo);
	var amountDetails = response.amount_details;
	var taxDetails = response.tax_details;

    controlTypeName = batch.control_type_name;

   document.getElementById("issueRateExpr"+rowsLength).value = batch.issue_rate_expr;
   document.getElementById("visitSellingPriceExpr"+rowsLength).value = batch.visit_selling_expr;
   document.getElementById("storeSellingPriceExpr"+rowsLength).value = batch.store_selling_expr;



    var imgbutton = makeImageButton('itemCheck','itemCheck'+rowsLength,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','deleteRow(this.id,itemRow'+rowsLength+','+rowsLength+')');

	var editButton = document.getElementById("add"+rowsLength);
	var eBut =  document.getElementById("addBut"+rowsLength);
	editButton.setAttribute("src",popurl+'/icons/Edit.png');
	eBut.setAttribute("title", "Edit Item");
	eBut.setAttribute("accesskey", "");
	document.getElementById("itemRow"+rowsLength).appendChild(imgbutton);

	document.getElementById("temp_charge_id"+rowsLength).value = "_"+rowsLength;
	document.getElementById("storeId"+rowsLength).value = document.getElementById("store").value;
	// document.getElementById("medDisc"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById("medDisc"+rowsLength).value = (parseFloat(amountDetails.discount_per).toFixed(decDigits));

	document.getElementById("isMarkUpRate"+rowsLength).value = '';
	document.getElementById("hdeleted"+rowsLength).value = false;
	document.getElementById("patper").value = batch.patient_percent;
	document.getElementById("patcatamt").value = batch.patient_amount_per_category;
	document.getElementById("patcap").value = batch.patient_amount_cap;
	document.getElementById("insuranceCategoryId").value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;
	document.getElementById("billingGroupId").value = (!batch.billing_group) ? '' : batch.billing_group_id;

	document.getElementById("firstOfCategory"+rowsLength).value = document.getElementById("isFirstOfCategory").value;

	document.getElementById( "medDiscWithoutInsurance"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById( "medDiscWithInsurance"+rowsLength).value = '';

	var discountPlanId = document.getElementById("discountPlanId").title;
	var insuranceCategoryId = batch.insurance_category_id;
	var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";

	if(priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y') {
		var discountPlanDetailsJSON = discountPlansJSON;
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {
			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];
				if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountPlanId && item.applicable_to_id == insuranceCategoryId) {
					document.getElementById( "medDiscWithInsurance"+rowsLength).value = item["discount_value"];
					break;
				}
			}
		}
	}

	var discount = 0;

    var medDiscWithInsurance = 0.00;
    var medDiscWithoutInsurance = 0.00;
    if(document.getElementById("medDiscWithInsurance"+rowsLength) != null && document.getElementById("medDiscWithInsurance"+rowsLength).value !='') {
    	medDiscWithInsurance = document.getElementById("medDiscWithInsurance"+rowsLength).value;
    } else {
    	medDiscWithInsurance = document.getElementById("medDiscWithoutInsurance"+rowsLength).value;
    }

    if(document.getElementById("medDiscWithoutInsurance"+rowsLength) != null && document.getElementById("medDiscWithoutInsurance"+rowsLength).value != '' )
        medDiscWithoutInsurance = document.getElementById("medDiscWithoutInsurance"+rowsLength).value;

    if(isTpaBill && medDiscWithInsurance != null && medDiscWithInsurance != undefined && medDiscWithInsurance != '') {
        discount = medDiscWithInsurance;
    } else if(medDiscWithoutInsurance!= null && medDiscWithoutInsurance != undefined && medDiscWithoutInsurance != ''){
        discount = medDiscWithoutInsurance;
    }
	

	setSellingPrice(batch, qtyForBatch, itemRate, medicineId, amountDetails.discount_per, patient, rowsLength);


	var taxRate = 0;
	var taxAmt = 0;

	issueUnits[batch.item_batch_id] = batch.issue_units;

	for(var i = 0; i < taxDetails.tax_map.length; i++) {
		var taxMap = taxDetails.tax_map;
		for(var j=0; j < subgroupNamesList.length; j++) {
			if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id]
				&& taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id != null 
					&& taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
				setFieldValue('ed_taxsubgroupid', taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id);
				setFieldValue('taxname', subgroupNamesList[j].item_subgroup_name, subgroupNamesList[j].item_group_id);
				setFieldValue('taxrate', taxMap[i][subgroupNamesList[j].item_subgroup_id].rate, subgroupNamesList[j].item_group_id);
				setFieldValue('taxamount', taxMap[i][subgroupNamesList[j].item_subgroup_id].amount, subgroupNamesList[j].item_group_id);
				setFieldValue('taxsubgroupid', taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id);
				taxRate += parseFloat(taxMap[i][subgroupNamesList[j].item_subgroup_id].rate);
				taxAmt += parseFloat(taxMap[i][subgroupNamesList[j].item_subgroup_id].amount);
			}
		}
	}
	
	// setFieldValue('tax_rate', taxRate);


	var itemDetails = {
		"medicine_id" : medicineId,
		"item_name" : medicineName,
		"batch_no" : batch.batch_no,
		"item_batch_id" : batch.item_batch_id,
		"exp_date" : batch.exp_dt,
		"qty" : ( batch.identification == 'S' ? 1 : qtyForBatch ),
		"package_size" : batch.issue_base_unit,
		"is_billable" : batch.billable,
		"cat_payable" : calimbaleInfo,
		"control_type_name" : controlTypeName,
		"mrp" : amountDetails.mrp,
		"unit_mrp" : amountDetails.unit_mrp,
		"org_mrp" : amountDetails.original_mrp,
		"discount_amt" : amountDetails.discount_amt,
		"discount_per" : amountDetails.discount_per,
		"issue_base_unit" : batch.issue_base_unit,
		"issueuom" : 'I',
		"stktype" : document.forms[0].stocktype.value,
		"tax_per" : taxRate,
		"tax_amt" : taxAmt,
		"original_tax_amt" : taxAmt,
		"tax_type" : taxDetails.tax_basis,
		"amt" :  parseFloat(taxDetails.net_amount)-parseFloat(taxDetails.discount_amount),
		"category_id" : batch.category_id,
		"insurance_category_id" : batch.insurance_category_id == null ? 0 : batch.insurance_category_id,
		"billing_group_id" : (!batch.billing_group_id) ? '' : batch.billing_group_id,
		"priCatPayable" : priCatPayable,
		"item_bar_code_id" : "", //sending blank for now
		"coverdbyinsuranceflag" : calimbaleInfo,
		"origUnitMrp" : amountDetails.original_unit_mrp
	};



	for(var i =0; i<groupListJSON.length; i++) {
		var itemGroupId = groupListJSON[i].item_group_id;
		copyFieldValue('taxname', 'taxname', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxrate', 'taxrate', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxamount', 'taxamount', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxsubgroupid', 'taxsubgroupid', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
	}

	addToInnerHTML(itemDetails, rowsLength);
	var isTpaBill = getBillIsTpa(billNo);
	if(isTpaBill)
		onClickProcessInsForIssues('patientissueform', 'P');

}


function getItemAmountsForOrderkit(medicineId, itemBatchId, issueType, qty, pkgSize, mrp, discount, changeType, billNo) {
	var storeId = getFieldValue('store');
	var visitId = getFieldValue('mrno');
	var billNo = getFieldValue('bill_no');
	var isTpaBill = getBillIsTpa(billNo);
	
	var reqObj = {};
	if(isNotNullObj(medicineId) && isNotNullObj(itemBatchId) && isNotNullObj(issueType) && isNotNullObj(qty)) {
		var issueQty = qty;
		if(issueType == 'P') {
			issueQty = qty * pkgSize;
		}
		var url = cpath+"/patientissues/getitemamountdetails.json";
		
		reqObj['store_id'] = storeId;
		reqObj['visit_id'] = visitId;
		reqObj['medicine_id'] = medicineId;
		reqObj['item_batch_id'] = itemBatchId;
		reqObj['issue_type'] = issueType;
		reqObj['qty'] = issueQty;
		reqObj['pkg_size'] = pkgSize;
		reqObj['bill_no'] = billNo;
		reqObj['is_tpa'] = isTpaBill;
		reqObj['mr_no'] = getFieldHtml('patientMrno');
		
		if(isNotNullObj(mrp)) {
			reqObj['rate'] = mrp;
		}
		if(isNotNullObj(discount)) {
			reqObj['discount_per'] = discount;
		}
		if(isNotNullObj(changeType)) {
			reqObj['change_type'] = changeType;
		}
		if(isTpaBill) {
			if(patient != null && !empty(patient.insurance) && patient.insurance.length > 0) {
				reqObj['tpa_id'] = patient.insurance[0].sponsor_id;
			} 
		}
			
		if(changeType == 'T') {
			var taxSubgroupIds = [];
			for(var i =0; i<groupListJSON.length; i++) {
				var itemGroupId = groupListJSON[i].item_group_id;
				if(getFieldValue('ed_taxsubgroupid'+itemGroupId) != null && 
						getFieldValue('ed_taxsubgroupid'+itemGroupId) != '')
					taxSubgroupIds.push(getFieldValue('ed_taxsubgroupid'+itemGroupId));
			}
			if (taxSubgroupIds != undefined && taxSubgroupIds.length > 0) {
				reqObj['tax_sub_group_ids'] = taxSubgroupIds;
			}
		}
		
		var response = ajaxFormObj(reqObj, url, false);
		return response;
	}
}

function preventOpenDialogOnEnterKey(val) {
    var e = window.event || arguments.callee.caller.arguments[0];

    var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
    if ( charCode==13 || charCode==3 ) {
        getItemBarCodeDetails(val);
        e.preventDefault();
        return false;
    }
    return true;
}
/**
 * This method is used to evaluate the issue item master/Store tariff selling price expression and return the processed value.
 *
 * @param batch
 * @param qtyForBatch
 * @param itemRate
 * @param medicineId
 * @param discount
 * @param patient
 */
function setSellingPrice(batch, qtyForBatch, itemRate, medicineId, discount, patient, id) {
	var issueRateExpr;
    var visitSellingPriceExpr;
    var storeSellingPriceExpr;
    var batchId;
    var storeRatePlanId;
    var billNo = getFieldValue("bill_no");
    if (null != patient && null != patient.visit) {
    	var visitBills = patient.visit.bills;
    	for(var i = 0;i < visitBills.length ; i++){
    		if(billNo === visitBills[i].bill_no)
    			storeRatePlanId = getStoreRatePlanId(visitBills[i].bill_rate_plan_id);
    	}
    }

    if(batch !== undefined && batch !== null) {
    	issueRateExpr = batch.issue_rate_expr;
        visitSellingPriceExpr = batch.visit_selling_expr;
        storeSellingPriceExpr = batch.store_selling_expr;
        batchId = batch.item_batch_id;
    } else {
    	issueRateExpr =  '';//getFieldValue("issueRateExpr", id);
    	visitSellingPriceExpr = '';getFieldValue("visitSellingPriceExpr", id);
    	storeSellingPriceExpr = '';getFieldValue("storeSellingPriceExpr", id);
    	batchId = getFieldValue("itemBatchId", id);
    }

    if ((isNotNullObj(issueRateExpr)) || (isNotNullObj(visitSellingPriceExpr)) || (isNotNullObj(storeSellingPriceExpr))) {
    	var ajaxReqObject = new XMLHttpRequest();
    	var qty = qtyForBatch;
    	if (qty == undefined || qty == 0 || qty == '') {
			qty = 1;
		}
    	var url = cpath+'/issues/getmarkuprate.json?';
    	var param = 'item_batch_id='+batchId+
						"&storeId="+document.getElementById("store").value+"&qty="+qty+
						"&medicine_id="+medicineId+"&bed_type="+gBedType+"&discount="+discount+"&bill_no="+billNo+"&patient_id="+patient.visit.patient_id;
    	if(isNotNullObj(storeRatePlanId)){
    		param += "&visitStoreRatePlanId="+storeRatePlanId;
    	}
    	if(isNotNullObj(issueRateExpr)) {
    		param += "&expression_type=I";
    	} else if(isNotNullObj(visitSellingPriceExpr)) {
    		param += "&expression_type=V";
    	} else if(isNotNullObj(storeSellingPriceExpr)) {
    		param += "&expression_type=S";
    	}
    	param += "&usemrp="+gUseBathMrp;
    	ajaxReqObject.open("GET",url.toString()+param, false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				eval("issueRateBean = " + ajaxReqObject.responseText);
				itemRate = issueRateBean.sellingPriceBean.mrp;

				setFieldValue("pkg_mrp", itemRate.toFixed(2), id);
				setFieldValue("mrpHid", itemRate.toFixed(2), id);
				setFieldValue("original_mrp", itemRate.toFixed(2), id);
				setFieldText("mrpLabel", itemRate.toFixed(2), id);
				setFieldText("unitmrpLabel", (itemRate/getFieldValue("pkg_unit", id)).toFixed(2), id);
				setFieldText("pkgMRPLabel", itemRate.toFixed(2), id);
			}
		}
    }
}

function refreshForm(){
	document.getElementById("refresh").value = true;
	var itemstable = document.getElementById("itemListtable");
	var tablelength = itemstable.rows.length-1;
	
	if(tablelength > 0){
		for(var i=tablelength;i>0;i--){
			itemstable.deleteRow(i);
		}
	}
	
	if (!document.getElementById("user_issue_no")){
		AddRowsToGrid(1);
	}
	if (document.getElementById("mrno") != null){
		document.getElementById("mrno").class = '';
		document.getElementById("mrno").value='';
		document.getElementById("mrno").disabled=false;
	} else{
		Hospital_field.class = 'required';
		if ( deptId != '' ){
			document.getElementById("store").value = deptId;
		} else{
			document.getElementById("store").selectedIndex = 0;
		}
	}

	if ( document.getElementById("user_issue_no") ){
		valReset();
	} else{
		Hospital_field.value='';
		document.getElementById("reason").value = '';
		if ( issuetodept == 'N' ){
			document.getElementById("issueType_user").checked = 'checked';
			onChangeIssueType(document.getElementById("issueType_user"));
		} else{
			document.getElementById("issueType_dept").checked = 'checked';
			onChangeIssueType(document.getElementById("issueType_dept"));
		}
	}

}
function valReset () {
	document.getElementById("user_issue_no").length = 1;
	document.getElementById("user_issue_no").value='no';
	document.getElementById("user_issue_date").length = 1;
	document.getElementById("user_issue_date").value='';
	document.getElementById("creditbill").style.display = 'none';
	if (document.getElementById("Patient_field") != null){
		document.getElementById("patientDetails").style.display = 'none';
		document.getElementById("patientMrno").innerHTML = '';
		document.getElementById("patientName").innerHTML = '';
		document.getElementById("patientAge").innerHTML = '';
		document.getElementById("patientContactNo").innerHTML = '';
		document.getElementById("patientVisitNo").innerHTML = '';
		document.getElementById("patientDept").innerHTML = '';
		document.getElementById("patientDoctor").innerHTML = '';
		document.getElementById("patientBedType").innerHTML = ''
	}
}

function getItemBatchDetails(id){
	if(id != 'containerCollapse' && isNotNullObj(id)) {
		copyFieldValue('medicine_id', 'medicine_id', null, id);
	}
	var storeId = getFieldValue('store');
	if(isNotNullValue('medicine_id')) {
		var medicineId = getFieldValue('medicine_id');
		var medDetails = findInList(itemNamesArray, 'medicine_id', medicineId);
		setUOMOptions(document.patientissuedailog.item_unit, medDetails);
		if(document.patientissuedailog.item_unit.length > 1)
			setFieldValue('item_unit', gStoreSaleUnit);
		else
			document.patientissuedailog.item_unit.selectedIndex = 0;
		setFieldValue('pkg_size', medDetails.issue_base_unit);
		setFieldValue('issue_base_unit', medDetails.issue_base_unit);
		var url = cpath+"/patientissues/getitembatchdetails.json?store_id="+storeId+"&medicine_id="+medicineId;
		var reqObj = new XMLHttpRequest();
		ajaxGETRequest(reqObj, fillItemDetailsLocal, url.toString(), false);
	} 
	if(id != 'containerCollapse' && isNotNullObj(id)) {
		copyRowToDialog(id);
		if(avlQty[getFieldValue('item_batch_id', id)]) {
			setFieldHtml('avlqtylabel', avlQty[getFieldValue('item_batch_id', id)]);
		}
	}
}

function copyRowToDialog(id) {
	copyFieldValue('items', 'items', null, id);
	copyFieldValue('item_bar_code_id', 'barCodeId', null, id);
	setFieldValue('batch', getFieldValue('item_identifier', id));
	copyFieldValue('package_size', 'pkg_size', null, id);
	copyFieldValue('item_billable_hidden', 'itemBillable', null, id);
	copyFieldValue('mrpHid', 'mrp', null, id);
	copyFieldValue('tax_amt', 'tax', null, id);
	copyFieldValue('original_tax', 'original_tax', null, id);
	copyFieldValue('tax_per', 'tax_rate', null, id);
	copyFieldValue('pkg_unit', 'unit', null, id);
	copyFieldValue('unit_mrp', 'unit_mrp', null, id);
	copyFieldValue('tax_type', 'taxType', null, id);
	copyFieldValue('exp_dt', 'expdt', null, id);
	copyFieldValue('category', 'categoryId', null, id);
	copyFieldValue('insurancecategory', 'insuranceCategoryId', null, id);
	copyFieldValue('billinggroup', 'billingGroupId', null, id);
	copyFieldValue('original_mrp', 'origMRP', null, id);
	copyFieldValue('issue_base_unit', 'issue_base_unit', null, id);
	copyFieldValue('control_type_id', 'control_type_id', null, id);
	copyFieldValue('control_type_name', 'control_type_name', null, id);
	copyFieldValue('priCatPayable', 'priCatPayable', null, id);
	copyFieldValue('item_batch_id', 'item_batch_id', null, id);
	copyFieldValue('mrpHid', 'oldMRP', null, id);
	copyFieldValue('medicine_id', 'medicine_id', null, id);
	copyFieldValue('amt', 'amount', null, id);
	copyFieldValue('item_unit', 'item_unit', null, id);
	copyFieldValue('mrpHid', 'itemMrp', null, id);
	copyFieldValue('unit_mrp', 'unitMrp', null, id);
	copyFieldValue('discountAmtHid', 'discount_amt', null, id);
	copyFieldValue('discountHid', 'discount_per', null, id);
	copyFieldValue('coverdbyinsuranceflag', 'coverdbyinsuranceflag', null, id);
	copyFieldValue('discountHid', 'discount', null, id);
	copyFieldValue('item_batch_id', 'item_batch_id', null, id);
	copyFieldValue('item_identifier', 'batch_no', null, id);
	setFieldValue('issuQty', getFieldText('issue_qtyLabel', id));
	for(var i =0; i<groupListJSON.length; i++) {
		copyFieldValue('taxsubgroupid', 'ed_taxsubgroupid', groupListJSON[i].item_group_id, (groupListJSON[i].item_group_id)+''+id);
		copyFieldValue('taxname', 'taxname', groupListJSON[i].item_group_id, (groupListJSON[i].item_group_id)+''+id);
		copyFieldValue('taxrate', 'taxrate', groupListJSON[i].item_group_id, (groupListJSON[i].item_group_id)+''+id);
		copyFieldValue('taxamount', 'taxamount', groupListJSON[i].item_group_id, (groupListJSON[i].item_group_id)+''+id);
		copyFieldValue('taxsubgroupid', 'taxsubgroupid', groupListJSON[i].item_group_id, (groupListJSON[i].item_group_id)+''+id);
	}
}

function clearDialogBox(type) {
	if(type == 'batch') {
		setFieldValue('batch_no', '');
		setFieldValue('mrp', '');
		setFieldValue('unit', '');
		setFieldValue('unit_mrp', '');
		setFieldValue('taxType', '');
		setFieldValue('tax', '');
		setFieldValue('original_tax', '');
		setFieldValue('tax_rate', '');
		setFieldValue('expdt', '');
		setFieldValue('origMRP', '');
		setFieldValue('oldMRP', '');
		setFieldValue('amount', '');
		setFieldValue('issuQty', '');
		setFieldValue('itemMrp', '');
		setFieldValue('unitMrp', '');
		setFieldValue('discount_amt', '');
		setFieldValue('discount_per', '');
		setFieldValue('discount', '');
		setFieldHtml('avlqtylabel', '');
		for(var i =0; i<groupListJSON.length; i++) {
			setFieldValue('ed_taxsubgroupid', '', groupListJSON[i].item_group_id);
			setFieldValue('taxname', '', groupListJSON[i].item_group_id);
			setFieldValue('taxrate', '', groupListJSON[i].item_group_id);
			setFieldValue('taxamount', '', groupListJSON[i].item_group_id);
			setFieldValue('taxsubgroupid', '', groupListJSON[i].item_group_id);
		}
	} else if(type == 'all'){
		setFieldValue('barCodeId', '');
		setFieldValue('items', '');
		setFieldValue('batch', '');
		setFieldValue('batch_no', '');
		setFieldValue('pkg_size', '');
		setFieldValue('itemBillable', '');
		setFieldValue('pkguom', '');
		setFieldValue('mrp', '');
		setFieldValue('unit', '');
		setFieldValue('unit_mrp', '');
		setFieldValue('taxType', '');
		setFieldValue('tax', '');
		setFieldValue('tax_rate', '');
		setFieldValue('expdt', '');
		setFieldValue('categoryId', '');
		setFieldValue('insuranceCategoryId', '');
		setFieldValue('billingGroupId', '');
		setFieldValue('origMRP', '');
		setFieldValue('issue_base_unit', '');
		setFieldValue('control_type_id', '');
		setFieldValue('control_type_name', '');
		setFieldValue('priCatPayable', '');
		setFieldValue('item_batch_id', '');
		setFieldValue('oldMRP', '');
		setFieldValue('medicine_id', '');
		setFieldValue('amount', '');
		setFieldValue('issuQty', '');
		setFieldValue('itemMrp', '');
		setFieldValue('unitMrp', '');
		setFieldValue('discount_amt', '');
		setFieldValue('discount_per', '');
		setFieldValue('coverdbyinsuranceflag', 'false');
		setFieldValue('discount', '');
		var itemUnits = document.getElementById("item_unit");
		if (itemUnits && itemUnits.length > 0) {
			for (var i = 0; i < itemUnits.length; i++) {
				itemUnits.remove(i);
			}
		}
		setFieldValue('item_identifier', '');
		setFieldValue('item_batch_id', '');
		setFieldHtml('avlqtylabel', '');
		var taxSubgroupObj = document.getElementById("ed_taxsubgroupid");
		if (taxSubgroupObj && taxSubgroupObj.length > 0) {
			for (var i = 0; i < taxSubgroupObj.length; i++) {
				taxSubgroupObj.remove(i);
			}
		}
		for(var i =0; i<groupListJSON.length; i++) {
			setFieldValue('ed_taxsubgroupid', '', groupListJSON[i].item_group_id);
			setFieldValue('taxname', '', groupListJSON[i].item_group_id);
			setFieldValue('taxrate', '', groupListJSON[i].item_group_id);
			setFieldValue('taxamount', '', groupListJSON[i].item_group_id);
			setFieldValue('taxsubgroupid', '', groupListJSON[i].item_group_id);
		}
	} else if(type == 'tax') {
		setFieldValue('taxType', '');
		setFieldValue('tax', '');
		setFieldValue('tax_rate', '');
		for(var i =0; i<groupListJSON.length; i++) {
			setFieldValue('ed_taxsubgroupid', '', groupListJSON[i].item_group_id);
			setFieldValue('taxname', '', groupListJSON[i].item_group_id);
			setFieldValue('taxrate', '', groupListJSON[i].item_group_id);
			setFieldValue('taxamount', '', groupListJSON[i].item_group_id);
			setFieldValue('taxsubgroupid', '', groupListJSON[i].item_group_id);
		}
	}
}

function fillItemBatchDetails(responseText){
	if (responseText==null || responseText=='') return;
	eval("itemBatchDetails = " + responseText);
	itemBatchDetails = itemBatchDetails.mapList;
    var index = 1;
    if(itemBatchDetails.length == 0){
    	showMessage("js.sales.issues.noavailablestock");
    	setFieldValue('items', '');
    	setFieldValue('batch', '');
    	setFieldValue('pkg_size', '');
    	setFieldValue('barCodeId', '');
    	if(isNotNullValue('itemBillable')) 
    		setFieldValue('itemBillable', '');
    	document.getElementById("batch").length = 1;
    	return false;
    }
    for(var t= 0; t<itemBatchDetails.length; t++){
    	if (isNotNullValue('mrno')) {
    		if (!isNotNullObj(patient)) {
        		showMessage("js.sales.issues.patientmrno");
        		return false;
        	}

    		if(itemBatchDetails[t].billable){
	    		if(!isNotNullValue('bill_no')){
	    			showMessage("js.sales.issues.nothave.openbills");
	    			setFieldValue('items', '');
	    			return false;
	    		}
    			document.getElementById('creditbill').style.display = 'block';
	    	}
	    }

    	document.getElementById('batch').options[0].text = '-- Select --';
	    document.getElementById('batch').options[0].value = '';
	    document.getElementById('batch').length = 1+itemBatchDetails.length;
	    if(itemBatchDetails[t].qty != 0){
	    	qty_available++;
		}
	    document.getElementById('batch').options[index].text = itemBatchDetails[t].batch_no + "/" + itemBatchDetails[t].qty +( itemBatchDetails[t].exp_dt == null ? '' : "/"+formatExpiry(new Date(itemBatchDetails[t].exp_dt)));
	    document.getElementById('batch').options[index].value = itemBatchDetails[t].batch_no;
	    avlQty[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].qty;
	    itemBatchIds[itemBatchDetails[t].batch_no+'@'+itemBatchDetails[t].medicine_id] = itemBatchDetails[t].item_batch_id;
    	exp_dts[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].exp_dt;
    	item_ids[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].medicine_id;
    	identification_type[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].identification;
    	issueUnits[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].issue_units;
    	pkgSizes[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].issue_base_unit;
    	item_billable[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].billable;
    	pkgUOMs[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].package_uom;
    	categoryIds[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].category_id;
    	controlTypeIds[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].control_type_id;
    	controlTypeNames[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].control_type_name;
    	stock_type[itemBatchDetails[t].item_batch_id] = itemBatchDetails[t].consignment_stock;
       	index++;
    }
    //If item has multiple batches don't select any batch.
    if (itemBatchDetails.length > 1){
    	document.getElementById("batch").disabled = false;
    	document.getElementById("batch").selectedIndex = 0;
    }
    //else select first batch and fill details of that batch.
    else {
    	document.getElementById("batch").selectedIndex = 1;
    	document.getElementById("batch").disabled = true;
    	setFieldValue('batch_no', itemBatchDetails[0].batch_no);
		fillSelectedBatchDetails(itemBatchDetails[0].batch_no, itemBatchDetails[0].medicine_id);
		onChangeBatch(itemBatchDetails[0].batch_no);
    }

    if(itemBatchDetails.length > 0) {
    	if ((itemBatchDetails[0].identification == 'S') &&  (qty_available == 0)){
    		showMessage("js.sales.issues.noavailablestock");
    		setFieldValue('items', '');
    		setFieldValue('batch', '');
    		setFieldValue('issuQty', '');
    		setFieldValue('issue_units', '');
    		setFieldValue('itemBillable', '');
    		setFieldValue('barCodeId', '');
    		document.getElementById('batch').length = 1;
        	return false;
    	}
        var gridBatchId = getFieldText('identifierLabel', gIndex);
        var selBatchId = getFieldValue('batch');
    	setSelectedIndex(document.patientissuedailog.batch, selBatchId);
    }
    return true;
}

function fillSelectedBatchDetails(batchNo, medicineId) {
	var itemBatchIdVal = itemBatchIds[batchNo+'@'+medicineId];
	setFieldValue('itemBillable', item_billable[itemBatchIdVal]);
	setFieldValue('expdt', exp_dts[itemBatchIdVal]);
	setFieldValue('unit', issueUnits[itemBatchIdVal]);
	setFieldValue('categoryId', categoryIds[itemBatchIdVal]);
	setFieldValue('control_type_id', controlTypeIds[itemBatchIdVal]);
	setFieldValue('control_type_name', controlTypeNames[itemBatchIdVal]);
	setFieldValue('item_batch_id', itemBatchIdVal);
	setFieldValue('issue_base_unit', pkgSizes[itemBatchIdVal]);
	setFieldValue('batch_no', batchNo);
	if (batchNo == '') {
		setFieldHtml('avlqtylabel', '');
	} else {
		setFieldHtml('avlqtylabel', avlQty[itemBatchIdVal]);
	}
	if(identification_type[itemBatchIdVal] == 'S') {
		setFieldValue('issuQty', 1);
		document.patientissuedailog.issuQty.readOnly = true;
	} else {
		setFieldValue('issuQty', '');
   		document.patientissuedailog.issuQty.readOnly = false;
	}
	if (null != exp_dts[itemBatchIdVal]){
		setFieldValue('expdt', new Date(exp_dts[itemBatchIdVal]));
	} else{
		setFieldValue('expdt', '');
	}
}

function formatExpiry(dateMSecs) {
	var dateStr = '';
	if (dateMSecs != null) {
		var dateObj = new Date(dateMSecs);
		dateStr = formatDate(dateObj, 'monyyyy', '-');
	}
	return dateStr;
}

function makeLabel(name, id, value) {
	var el = document.createElement('label');
	if (name!=null && name!="")
		el.name = name;
	if (id!=null && id!="")
		el.id = id;
	if (value!=null && value!="")
		el.value = value;
	return el;
}

function setUOMOptions(obj, medicineDetails) {
	var uomOptList = [];
	if ( medicineDetails.issue_units != undefined )
		uomOptList.push({uom_name: medicineDetails.issue_units, uom_value: 'I'});
	if ( medicineDetails.issue_units != medicineDetails.package_uom  && medicineDetails.package_uom != undefined )
			uomOptList.push({uom_name: medicineDetails.package_uom, uom_value: 'P'});
	loadSelectBox(obj, uomOptList, 'uom_name', 'uom_value');
}

function daysDiff(d1, d2) {
	var millisecondsDiff = d2.getTime() - d1.getTime();
	var daysDiff = millisecondsDiff / 60 / 60 / 24 / 1000;
	return daysDiff;
}

/**
 * Validate the Expire Date with Curent Date.
 * 
 * @param expYear
 * @param expMonth
 * @param procuAction
 * @param procuExpireDays
 * @returns {Boolean}
 */
function chkExpireDate(expYear,expMonth, procuAction, procuExpireDays) {
	var curDate =new Date();
	var expDate = new Date(parseInt("20"+expYear.value),parseInt(expMonth.value),0);
	var daysDiffs = parseInt(daysDiff(curDate,expDate));
	if(daysDiffs <= procuExpireDays) {
		if(procuAction == "W") {
			if (confirm(getString("js.stores.procurement.expirydays.warn")+" "+procuExpireDays+" days") == false) {
				expMonth.focus(); 
		    	return false;
		    } else {
		    	return true;
		    }
		} else if(procuAction == "B") {
			alert(getString("js.stores.procurement.expirydays.alert")+" "+procuExpireDays+" days is not allowed");
			expMonth.focus(); 
	    	return false;
		} else {
			return true;
		}
	} else {
		return true;
	}
}

/**
 * This function is used to calculated amounts.
 * 
 * @param rate
 */
function onChangeRate(rate) {
	var avilQty = avlQty[getFieldValue('item_batch_id')];
	var medicineId = getFieldValue('medicine_id');
	var itemBatchIdVar = getFieldValue('item_batch_id');
	var issueType = getFieldValue('item_unit');
	var pkgSize = getFieldValue('pkg_size');
	var qty = getFieldValue('issuQty');
	setFieldValue('mrp', rate);
	var discount = getFieldValue('discount');
	var billNo = getFieldValue('bill_no');
	if(parseFloat(avilQty) > 0 || gStockNegative == 'A' || gStockNegative == 'W')
		getItemAmounts(medicineId, itemBatchIdVar, issueType, qty, pkgSize, rate, discount, 'A', billNo);
}

/**
 * This function is used to get amounts on change qty.
 * 
 * @param qty
 */
function onChangeQty(qty){
	if (isPkg) {
		return;
	}

	var avilQty = avlQty[getFieldValue('item_batch_id')];
	var medicineId = getFieldValue('medicine_id');
	var itemBatchIdVar = getFieldValue('item_batch_id');
	var issueType = getFieldValue('item_unit');
	var pkgSize = getFieldValue('pkg_size');
	var rate = getFieldValue('mrp');
	var discount = getFieldValue('discount');
	var billNo = getFieldValue('bill_no');
	if(parseFloat(avilQty) > 0 || gStockNegative == 'A' || gStockNegative == 'W')
		getItemAmounts(medicineId, itemBatchIdVar, issueType, qty, pkgSize, rate, discount, 'Q', billNo);
}

/**
 * This function is used to get amounts for discount.
 * 
 * @param discount
 */
function onChangeDiscount(discount) {
	var avilQty = avlQty[getFieldValue('item_batch_id')];
	var medicineId = getFieldValue('medicine_id');
	var itemBatchIdVar = getFieldValue('item_batch_id');
	var issueType = getFieldValue('item_unit');
	var pkgSize = getFieldValue('pkg_size');
	var qty = getFieldValue('issuQty');
	var rate = getFieldValue('mrp');
	setFieldValue('discount_per', discount.value);
	var billNo = getFieldValue('bill_no');
	if(parseFloat(avilQty) > 0 || gStockNegative == 'A' || gStockNegative == 'W')
		getItemAmounts(medicineId, itemBatchIdVar, issueType, qty, pkgSize, rate, discount.value, 'D', billNo);
}

/**
 * This method is used to get item amount details.
 * 
 */
function getItemAmounts(medicineId, itemBatchId, issueType, qty, pkgSize, mrp, discount, changeType, billNo) {
	var storeId = getFieldValue('store');
	var visitId = getFieldValue('mrno');
	var billNo = getFieldValue('bill_no');
	var isTpaBill = getBillIsTpa(billNo);
	
	var reqObj = {};
	if(isNotNullObj(medicineId) && isNotNullObj(itemBatchId) && isNotNullObj(issueType) && isNotNullObj(qty)) {
		var issueQty = qty;
		if(issueType == 'P') {
			issueQty = qty * pkgSize;
		}
		var url = cpath+"/patientissues/getitemamountdetails.json";
		
		reqObj['store_id'] = storeId;
		reqObj['visit_id'] = visitId;
		reqObj['medicine_id'] = medicineId;
		reqObj['item_batch_id'] = itemBatchId;
		reqObj['issue_type'] = issueType;
		reqObj['qty'] = issueQty;
		reqObj['pkg_size'] = pkgSize;
		reqObj['bill_no'] = billNo;
		reqObj['is_tpa'] = isTpaBill;
		reqObj['mr_no'] = getFieldHtml('patientMrno');
		
		if(isNotNullObj(patient.visit.store_rate_plan_id)){
			reqObj['visitStoreRatePlanId']= patient.visit.store_rate_plan_id;
    	}
		
		if(isNotNullObj(mrp)) {
			reqObj['rate'] = mrp;
		}
		if(isNotNullObj(discount)) {
			reqObj['discount_per'] = discount;
		}
		if(isNotNullObj(changeType)) {
			reqObj['change_type'] = changeType;
		}
		// if(isNotNullObj(dirty)) {
		// 	reqObj['dirty'] = dirty;
		// }
		if(isTpaBill) {
			if(patient != null && !empty(patient.insurance) && patient.insurance.length > 0) {
				reqObj['tpa_id'] = patient.insurance[0].sponsor_id;
			} 
		}
			
		if(changeType != 'B') {
			var taxSubgroupIds = [];
			for(var i =0; i<groupListJSON.length; i++) {
				var itemGroupId = groupListJSON[i].item_group_id;
				if(getFieldValue('ed_taxsubgroupid'+itemGroupId) != null && 
						getFieldValue('ed_taxsubgroupid'+itemGroupId) != '')
					taxSubgroupIds.push(getFieldValue('ed_taxsubgroupid'+itemGroupId));
			}
			if (taxSubgroupIds != undefined) {
				reqObj['tax_sub_group_ids'] = taxSubgroupIds;
			}
		}
		
		var response = ajaxFormObj(reqObj, url, false);
		fillItemAmounts(response);
	}
}

function onChangeTax() {
	var medicineId = getFieldValue('medicine_id');
	var itemBatchIdVar = getFieldValue('item_batch_id');
	var issueType = getFieldValue('item_unit');
	var pkgSize = getFieldValue('pkg_size');
	var qty = getFieldValue('issuQty');
	var rate = getFieldValue('mrp');
	var discount = getFieldValue('discount_per');
	var billNo = getFieldValue('bill_no');
	getItemAmounts(medicineId, itemBatchIdVar, issueType, qty, pkgSize, rate, discount, 'T', billNo);
}
/**
 * This method is used to fill item amount details.
 * 
 */
function fillItemAmounts(itemAmounts) {
	var amountDetails = itemAmounts.amount_details;
	var taxDetails = itemAmounts.tax_details;
	var taxRate = 0;
	var taxAmt = 0;
	
	setFieldValue('mrp', amountDetails.mrp);
	setFieldValue('itemMrp', amountDetails.mrp);
	setFieldValue('unit_mrp', amountDetails.unit_mrp);
	setFieldValue('origUnitMrpHid', amountDetails.original_unit_mrp);
	setFieldValue('unitMrp', amountDetails.unit_mrp);
	setFieldValue('origMRP', amountDetails.original_mrp);
	setFieldValue('discount_amt', amountDetails.discount_amt);
	setFieldValue('discount_per', amountDetails.discount_per);
	setFieldValue('discount', amountDetails.discount_per);
	
	setFieldValue('amount', parseFloat(taxDetails.net_amount)-parseFloat(taxDetails.discount_amount));
	setFieldValue('taxType', taxDetails.tax_basis);
	
	resetTaxFields();

	for(var i = 0; i < taxDetails.tax_map.length; i++) {
		var taxMap = taxDetails.tax_map;
		for(var j=0; j < subgroupNamesList.length; j++) {
			if(taxMap[i] && taxMap[i][subgroupNamesList[j].item_subgroup_id]
				&& taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id != null 
					&& taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
				setFieldValue('ed_taxsubgroupid', taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id);
				setFieldValue('taxname', subgroupNamesList[j].item_subgroup_name, subgroupNamesList[j].item_group_id);
				setFieldValue('taxrate', taxMap[i][subgroupNamesList[j].item_subgroup_id].rate, subgroupNamesList[j].item_group_id);
				setFieldValue('taxamount', taxMap[i][subgroupNamesList[j].item_subgroup_id].amount, subgroupNamesList[j].item_group_id);
				setFieldValue('taxsubgroupid', taxMap[i][subgroupNamesList[j].item_subgroup_id].tax_sub_group_id, subgroupNamesList[j].item_group_id);
				taxRate += parseFloat(taxMap[i][subgroupNamesList[j].item_subgroup_id].rate);
				taxAmt += parseFloat(taxMap[i][subgroupNamesList[j].item_subgroup_id].amount);
			}
		}
	}
	
	setFieldValue('tax_rate', taxRate);
	setFieldValue('tax', taxAmt);
	setFieldValue('original_tax', taxAmt);
}

function populatePackages(visit) {
	activePackages = visit.packages;
	document.getElementById("activePackage").length=0;
	addOption(document.getElementById("activePackage"), "--- Select ---", "");
	for(var i=0;i<activePackages.length;i++) {
		addOption(document.getElementById("activePackage"), activePackages[i].package_name
		 + ' ' + activePackages[i].presc_date, activePackages[i].pat_package_id);
	}
	multiPackages = visit.multi_packages;
	for(var i=0;i<multiPackages.length;i++) {
		addOption(document.getElementById("activePackage"), multiPackages[i].package_name
		 + ' ' + multiPackages[i].presc_date, multiPackages[i].pat_package_id);
	}
}

function onChangePackage(patPackageId) {
	activePackages = patient.visit.packages;
	multiPackages = patient.visit.multi_packages;
	table = document.getElementById('packageInfo');
	row = table.rows[0];
	isStaticPackage = false;
	if (!patPackageId) {
		populateBillType(patient.visit.bills, patient);
		clearIndentDetails();
		getElementByName(row, "pkg_charge_id_ref").value= '';
		document.getElementById("pkg_ins_cat_id").value= '';
		document.getElementById("package_id").value= '';
		isPkg = false;

		return;
	}

	document.getElementById("bill_no").length=0;
	isPkg = true;

	for(var i=0;i<activePackages.length;i++) {
		if (patPackageId == activePackages[i].pat_package_id) {
			addOption(document.getElementById("bill_no"), activePackages[i].bill_no, activePackages[i].bill_no);
			getElementByName(row, "pkg_charge_id_ref").value = activePackages[i].charge_id;
			document.getElementById("pkg_ins_cat_id").value = activePackages[i].insurance_category_id;
			document.getElementById("package_id").value= activePackages[i].package_id;
			isStaticPackage = true;
			break;
		}
	}

	if (!isStaticPackage) {
		var mvpBills ={};
		for (var i=0;i<multiPackages.length;i++) {
			if (patPackageId == multiPackages[i].pat_package_id) {
				document.getElementById("pkg_ins_cat_id").value = multiPackages[i].insurance_category_id;
				document.getElementById("package_id").value= multiPackages[i].package_id;
				break;
			}
		}
		mvpBills = patient.visit.multi_package_bills;
		for(var i =0; i<mvpBills.length;i++) {
			if(mvpBills[i].pat_package_id == patPackageId) {
				addOption(document.getElementById("bill_no"), mvpBills[i].bill_no, mvpBills[i].bill_no);
			}
		}
		if(document.getElementById("bill_no").length==0) {
			clearIndentDetails();
			alert("No open bill is available for selected package");
			return;
		}
	}


	// Code for populating package items from below.
	clearIndentDetails();
	var pkgDetails = getPackageDetails();
	packageDetailsResponseHandler(pkgDetails);

}

function getPackageDetails() {
	var patPackageId = document.getElementById('activePackage').value;
	var visitId = document.getElementById("mrno").value;
    var reqObj = new XMLHttpRequest();
	var storeId = document.getElementById("store").value;
	var billNo = document.getElementById("bill_no").value;
	var pkgId = document.getElementById("package_id").value;
	var isTpaBill = getBillIsTpa(billNo);
	var tpaId;

	if(isTpaBill) {
		if(patient != null && !empty(patient.insurance) && patient.insurance.length > 0) {
			tpaId = patient.insurance[0].sponsor_id;
		}
	}

	var url = cpath+'/patientissues/packagedetails.json?visit_id='+visitId+
	'&bill_no='+billNo+'&patPkgId='+patPackageId+'&package_id='+pkgId+'&storeId='+storeId;

	url = url + ( !empty(tpaId) ? "&tpa_id="+tpaId : "" );
	var response = ajaxGetFormObj(reqObj, url, false);
	return response;

}

function packageDetailsResponseHandler(responseText) {
	var pkgDetails = responseText;
	var pkgId = document.getElementById('package_id').value;

	if (pkgDetails.medBatches != null) {
		gMedicineBatches = pkgDetails.medBatches;
	} else {
		gMedicineBatches = {};
	}

	if (pkgDetails.pkgItemDetails.length > 0){
		var table = document.getElementById('itemListtable');
		var numItems = table.rows.length;
		for (var i=1; i<numItems; i++) {
			table.deleteRow(1);
		}
		AddRowsToGrid(1);

		// Now call the entry function for  populating package items.
		addMedicinesFromPackage(pkgDetails, pkgId);
	}
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);

	if(isTpaBill)
		onClickProcessInsForIssues('patientissueform', 'P');

	var lastRow = document.getElementById("itemListtable").rows.length - 1;
	document.getElementById('addBut' + lastRow).remove();
}

function addMedicinesFromPackage(pkgDetails, pkgId) {
	var msg = "";
	var pkgItems = pkgDetails.pkgItemDetails;
	for (var p=0; p<pkgItems.length; p++) {
		var item = pkgItems[p];
		var medicineId = item.medicine_id;
		var medBatches = gMedicineBatches[medicineId];
		var amountDetails = pkgDetails.pkgTaxDetails[medicineId];

		if (medBatches == null || medBatches.length == 0) {
			msg += msg.endsWith("\n") ? "" : "\n";
			msg += getString("js.sales.issues.storesuserissues.warning.nostockavailable");
			msg+=item.medicine_name;
			msg += "': ";
			msg += item.qty;
			continue;
		}

		if (!amountDetails) {
			msg += msg.endsWith("\n") ? "" : "\n";
			msg += getString("js.sales.issues.warning.nopkgamountdetail");
			msg += " : " + item.medicine_name;
			continue;
		}

		addPackageItem(medicineId, item,medBatches,p+1,amountDetails, pkgId);
	}
	if(msg !="") {
		alert(msg);
	}
}

function addPackageItem(medicineId, item, medBatches, dialogId, amountDetails, pkgId) {
	var medicineName = item.medicine_name;
	var remQty = item.qty;
	var expiredQty = 0; var nearingExpiryQty = 0;

	for (b=0; b<medBatches.length; b++) {
		var batch = medBatches[b];
		var avlblQty = 0;
		exp_dts[batch.item_batch_id] = batch.exp_dt;

		// if batch is already in grid, skip
		var dupExists = getDuplicateIndex(batch, -1);
		if (dupExists != -1)
			continue;

		if (item.uom == '') {
			avlblQty = batch.qty;
		} else {
			// use only whole packages
			avlblQty = Math.floor(batch.qty/batch.issue_base_unit);
		}

		// check for expiry date for normal sales (ie, not estimate, and not negative stock)
		if (avlblQty > 0) {
			var daysToExpire = batch.exp_dt != null ? getDaysToExpire(batch.exp_dt) : gExpiryWarnDays+1;
			if (daysToExpire <= 0) {
				expiredQty += avlblQty;
				continue;
			} else if (daysToExpire <= gExpiryWarnDays) {
				nearingExpiryQty += avlblQty;
			}
		}

		var qtyForBatch = 0;
		qtyForBatch = Math.min(remQty, avlblQty);
		if (allowDecimalsForQty == 'Y') {
			// round it off to 2 decimal places to avoid float problems
			qtyForBatch = Math.round(qtyForBatch*100)/100;
		}

		document.getElementById("dialogId").value = dialogId;
		// add to grid
		if (qtyForBatch > 0 )
			addPackageItemToGrid(medicineId, item, batch, amountDetails, qtyForBatch, pkgId);

		// if no more to be added finish up
		remQty = remQty - qtyForBatch;
		if (remQty == 0)
			break;

	}

	var msg = "";
	if (remQty > 0) {
		msg= getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg+= medicineName;
		msg+= "': " ;
		msg+= remQty.toFixed(2);
		alert(msg);
		if (expiredQty > 0) {
			msg += " (" ;
			msg+= expiredQty;
			msg+=getString("js.sales.issues.storesuserissues.itemsavailable.pastexpirydate");
			alert(msg);
		}

		msg += "\n";
	}

	if (nearingExpiryQty > 0) {
		msg += getString("js.sales.issues.storesuserissues.warning.someitemsfor");
		msg += medicineName;
		msg +=getString("js.sales.issues.storesuserissues.aresoontoexpire");
		msg+="\n";
		alert(msg);
	}

}

function addPackageItemToGrid(medicineId, pkgItem, batch, amountDetails, qtyForBatch, pkgId ) {

	var medicineName = pkgItem.medicine_name;
	var itemsTable = document.getElementById("itemListtable");
	var rowsLength = (itemsTable.rows.length)-1;
	var  nextRowLen = eval(rowsLength)+1;
	var nextrow =  document.getElementById("tableRow"+nextRowLen);
	if (nextrow == null){
		AddRowsToGrid(nextRowLen);
	}
	//	added for exclusions
	var catPayableInfo = null;
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	catPayableInfo = getCatPayableStatus(medicineId, false ,pkgId);
	if(catPayableInfo!= null && catPayableInfo != undefined ) {
		var calimbaleInfo = catPayableInfo.plan_category_payable != null? catPayableInfo.plan_category_payable:"";
		if(calimbaleInfo == 'f') {
			document.getElementById('coverdbyinsurance').innerHTML = "No";
			document.getElementById('coverdbyinsurance').style.color = "red";
		} else {
			document.getElementById('coverdbyinsurance').innerHTML = "Yes";
			document.getElementById('coverdbyinsurance').style.color = "#666666";
		}
		document.getElementById("cat_payable"+rowsLength).value = calimbaleInfo;
		if(calimbaleInfo != null && calimbaleInfo != undefined && calimbaleInfo == "f" && isTpaBill) {
			document.getElementById("itemLabel"+rowsLength).innerHTML ='<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+medicineName;
		} else {
			document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
		}
	} else {
		document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
	}

    //document.getElementById("itemLabel"+rowsLength).textContent = medicineName;

    controlTypeName = batch.control_type_name;
    if (controlTypeName != 'Normal')
		document.getElementById("controleTypeLabel"+rowsLength).innerHTML = "<font color='blue'>"+controlTypeName+"</font>";
	else
		setNodeText(document.getElementById("controleTypeLabel"+rowsLength), controlTypeName, 25);

	//document.getElementById("controleTypeLabel"+rowsLength).textContent = batch.control_type_name;
	document.getElementById("uomLabel"+rowsLength).textContent = ( pkgItem.uom == '' ? pkgItem.uom_display : pkgItem.uom );
   document.getElementById("issueRateExpr"+rowsLength).value = batch.issue_rate_expr;
   //document.getElementById("visitSellingPriceExpr"+rowsLength).value = batch.visit_selling_expr;
   //document.getElementById("storeSellingPriceExpr"+rowsLength).value = batch.store_selling_expr;



    var imgbutton = makeImageButton('itemCheck','itemCheck'+rowsLength,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','deleteRow(this.id,itemRow'+rowsLength+','+rowsLength+')');

	var editButton = document.getElementById("add"+rowsLength);
	var eBut =  document.getElementById("addBut"+rowsLength);
	editButton.setAttribute("src",popurl+'/icons/Edit.png');
	eBut.setAttribute("title", "Edit Item");
	eBut.setAttribute("accesskey", "");

	document.getElementById("itemRow"+rowsLength).appendChild(imgbutton);

	document.getElementById("temp_charge_id"+rowsLength).value = "_"+rowsLength;
	document.getElementById("storeId"+rowsLength).value = document.getElementById("store").value;
	document.getElementById("medDisc"+rowsLength).value = (parseFloat(amountDetails.discount_per)).toFixed(decDigits);

	document.getElementById("isMarkUpRate"+rowsLength).value = '';
	document.getElementById("hdeleted"+rowsLength).value = false;
	document.getElementById("patper").value = batch.patient_percent;
	document.getElementById("patcatamt").value = batch.patient_amount_per_category;
	document.getElementById("patcap").value = batch.patient_amount_cap;
	document.getElementById("insuranceCategoryId").value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;
	document.getElementById("billingGroupId").value = (!batch.billing_group_id) ? '' : batch.billing_group_id;
	document.getElementById("pat_pkg_content_id"+rowsLength).value = pkgItem.patient_package_content_id;
	document.getElementById("pack_ob_id"+rowsLength).value = pkgItem.pack_ob_id;

	document.getElementById("firstOfCategory"+rowsLength).value = document.getElementById("isFirstOfCategory").value;

	document.getElementById( "medDiscWithoutInsurance"+rowsLength).value = (parseFloat(amountDetails.discount_per)).toFixed(decDigits);
	document.getElementById( "medDiscWithInsurance"+rowsLength).value = '';

	var discountPlanId = document.getElementById("discountPlanId").title;
	var insuranceCategoryId = document.getElementById("pkg_ins_cat_id").value;
	var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";

	if(priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y') {
		var discountPlanDetailsJSON = discountPlansJSON;
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {
			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];
				if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountPlanId && insuranceCategoryId == item.applicable_to_id ) {
					document.getElementById("medDiscWithInsurance"+rowsLength).value = item["discount_value"];
					break;
				} else if (item.applicable_type == 'C' &&  item.discount_plan_id == discountPlanId && item.applicable_to_id == "PKGPKG" ) {
					document.getElementById("medDiscWithInsurance"+rowsLength).value = item["discount_value"];
					break;
				} else if (item.applicable_type == 'I' && item.discount_plan_id == discountPlanId &&  item.applicable_to_id == pkgId ) {
					document.getElementById("medDiscWithInsurance"+rowsLength).value = item["discount_value"];
					break;
				}
			}
		}
	}


	issueUnits[batch.item_batch_id] = batch.issue_units;

	var discount = 0;

    var medDiscWithInsurance = 0.00;
    var medDiscWithoutInsurance = 0.00;
    if(document.getElementById("medDiscWithInsurance"+rowsLength) != null && document.getElementById("medDiscWithInsurance"+rowsLength).value !='') {
		medDiscWithInsurance = parseFloat(document.getElementById("medDiscWithInsurance"+rowsLength).value);
    }

    if(document.getElementById("medDiscWithoutInsurance"+rowsLength) != null && document.getElementById("medDiscWithoutInsurance"+rowsLength).value != '' )
        medDiscWithoutInsurance = parseFloat(document.getElementById("medDiscWithoutInsurance"+rowsLength).value);

    if(isTpaBill && medDiscWithInsurance != null && medDiscWithInsurance != undefined && medDiscWithInsurance != '') {
        discount = medDiscWithInsurance;
    } else if(medDiscWithoutInsurance!= null && medDiscWithoutInsurance != undefined && medDiscWithoutInsurance != ''){
        discount = medDiscWithoutInsurance;
    }

	var taxMap  = amountDetails.tax_map;

	var taxRate = 0;
	var taxAmt = 0;

	for(var i in taxMap) {
		var taxGroup = taxMap[i];
		for(var j=0; j < subgroupNamesList.length; j++) {
			if(taxGroup && taxGroup.tax_sub_group_id != null
				 && taxGroup.tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
				setFieldValue('ed_taxsubgroupid', taxGroup.tax_sub_group_id, subgroupNamesList[j].item_group_id);
				setFieldValue('taxname', subgroupNamesList[j].item_subgroup_name, subgroupNamesList[j].item_group_id);
				setFieldValue('taxrate', taxGroup.rate, subgroupNamesList[j].item_group_id);
				setFieldValue('taxamount', (taxGroup.amount * qtyForBatch), subgroupNamesList[j].item_group_id);
				setFieldValue('taxsubgroupid', taxGroup.tax_sub_group_id, subgroupNamesList[j].item_group_id);
				taxRate += parseFloat(taxGroup.rate);
				taxAmt += parseFloat(taxGroup.amount * qtyForBatch);
			}
		}
	}
	
	var discountAmt = parseFloat(amountDetails.discount_amt * qtyForBatch);
	var discountPerc = amountDetails.discount_per;
	if (medDiscWithInsurance > 0) {
		var discountPlanDiscAmt = parseFloat((amountDetails.unit_mrp * medDiscWithInsurance)/100); 
		discountAmt = discountAmt + parseFloat(discountPlanDiscAmt * qtyForBatch);
		discountPerc = discountPerc + medDiscWithInsurance;
	}
	
	var totalMrp = parseFloat(amountDetails.unit_mrp * qtyForBatch);
	var netAmount = totalMrp + taxAmt;

	var itemDetails = {
		"medicine_id" : medicineId,
		"item_name" : medicineName,
		"batch_no" : batch.batch_no,
		"item_batch_id" : batch.item_batch_id,
		"exp_date" : batch.exp_dt,
		"qty" : ( batch.identification == 'S' ? 1 : qtyForBatch ),
		"package_size" : 1,
		//skipping unit_size
		"is_billable" : batch.billable,
		"cat_payable" : calimbaleInfo,
		//skipping control_type_id
		"control_type_name" : controlTypeName,
		"mrp" : amountDetails.original_mrp,
		"unit_mrp" : amountDetails.unit_mrp,
		"org_mrp" : amountDetails.original_mrp,
		"discount_amt" : discountAmt,
		"discount_per" : discountPerc,
		"issue_base_unit" : 1,
		"issueuom" : pkgItem.qty_unit,
		"stktype" : document.forms[0].stocktype.value,
		"tax_per" : taxRate,
		"tax_amt" : taxAmt,
		"original_tax_amt" : taxAmt,
		"tax_type" : 'M',
		"amt" :  netAmount - discountAmt,
		"category_id" : batch.category_id,
		"insurance_category_id" : batch.insurance_category_id == null ? 0 : batch.insurance_category_id,
		"billing_group_id" : (!batch.billing_group_id) ? '' : batch.billing_group_id,
		"priCatPayable" : priCatPayable,
		"item_bar_code_id" : "", //sending blank for now
		"coverdbyinsuranceflag" : calimbaleInfo,
		"origUnitMrp" : amountDetails.unit_mrp


	}

	for(var i =0; i<groupListJSON.length; i++) {
		var itemGroupId = groupListJSON[i].item_group_id;
		copyFieldValue('taxname', 'taxname', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxrate', 'taxrate', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxamount', 'taxamount', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
		copyFieldValue('taxsubgroupid', 'taxsubgroupid', groupListJSON[i].item_group_id+''+rowsLength, groupListJSON[i].item_group_id);
	}
	addToInnerHTML(itemDetails, rowsLength);
	setFieldValue('pkg_issue_qty', ( batch.identification == 'S' ? 1 : qtyForBatch ), rowsLength);
}

function fillPkgItemTaxAmounts(response) {
	var medicineId = getFieldValue('medicine_id');
	var taxDetails = response.pkgTaxDetails[medicineId];
	var qty = getFieldValue('issuQty');
	var taxRate = 0;
	var taxAmt = 0;

	var taxMap = taxDetails.tax_map;
		for(var i in taxMap) {
			var taxGroup = taxMap[i];
			for(var j=0; j < subgroupNamesList.length; j++) {
				if(taxGroup && taxGroup.tax_sub_group_id != null
					 && taxGroup.tax_sub_group_id == subgroupNamesList[j].item_subgroup_id) {
					setFieldValue('ed_taxsubgroupid', taxGroup.tax_sub_group_id, subgroupNamesList[j].item_group_id);
					setFieldValue('taxname', subgroupNamesList[j].item_subgroup_name, subgroupNamesList[j].item_group_id);
					setFieldValue('taxrate', taxGroup.rate, subgroupNamesList[j].item_group_id);
					setFieldValue('taxamount', (taxGroup.amount * qty), subgroupNamesList[j].item_group_id);
					setFieldValue('taxsubgroupid', taxGroup.tax_sub_group_id, subgroupNamesList[j].item_group_id);
					taxRate += parseFloat(taxGroup.rate);
					taxAmt += parseFloat(taxGroup.amount * qty);
				}
			}
		}

	setFieldValue('tax_rate', taxRate);
	setFieldValue('tax', taxAmt);
	setFieldValue('original_tax', taxAmt);
}
