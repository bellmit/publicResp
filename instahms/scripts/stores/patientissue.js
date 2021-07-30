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
var ouserAutoComp;
var isbillable = 0;

function init(){
	initDialog();
	setItems();
	if ( issuetodept == 'N' ) {
		fillUsers('hosp_user');
		Hospital_field = document.stockissueform.hosp_user;
	} else {
		Hospital_field = document.stockissueform.issue_dept;
	}
}

function initpatient(){
	isBatchMrpEnabled(gStoreId);
	Insta.initVisitAcSearch(cpath, "mrno", "mrnoContainer", 'active','all',
	function(type, args) {getPatientDetails();},
	function(type, args) {clearPatientDetails();});
	initDialog();
	initOrderKitAutoComplete();
	//document.getElementById("patientDetails").style.display = 'none';
	//document.getElementById("patientDetails").style.visibility = 'hidden';
	//setItems();
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

function initMrNoAutoComplete() {
	Insta.initPatientAcSearch(cpath, 'mrno', 'mrnoContainer', 'active', null, null);
}

//RC: Clean up required in this method.
function enable(obj,trantype){
	if ( obj == undefined ){
			return;
	}
	obj.disabled = false;
	//obj.className = "required"
	if(obj.name == "mrno"){
		obj.className = "required"
		document.getElementById("patientDetails").style.display = 'block';
		document.getElementById("patientDetails").style.visibility = 'visible'
		setItems();
	}else{
		if(issuetodept == 'N'){
			obj.className = "required"
			document.getElementById('hosp_user').readOnly = false;
			document.getElementById("issueType_user").checked = true;
			document.stockissueform.hosp_user.value = '';
			document.stockissueform.issue_dept.value = '';
			document.stockissueform.issue_ward.value = '';
			setItems();
		} else {
			document.getElementById("issueType_dept").checked = true;
			document.getElementById('issue_dept').readOnly = false;
			document.stockissueform.hosp_user.value = '';
			document.stockissueform.issue_ward.value = '';
			setItems();
		}
	}
	var item_table = document.getElementById("itemListtable");
	var innertablelength = item_table.rows.length-1;

	document.getElementById("items").value = '';
	document.getElementById("item").checked = true;

	if(type != "group")
		clearAllHiddenVariables();
}


function changeItems(item_identifier,trantype){
	var itemUntObj = null;
	if (trantype == 'Hospital'){
		if(identification_type[item_identifier] == 'S'){
	  		document.stockissueform.issuQty.value = '1';
	  		document.stockissueform.issuQty.readOnly = true;
		}
	   	if(identification_type[item_identifier] != 'S'){
	   		document.stockissueform.issuQty.value = '';
	   		document.stockissueform.issuQty.readOnly = false;
	   	}
	   	if (document.getElementById("item").checked) {
		   	document.stockissueform.stocktype.value = stock_type[item_identifier];
		   	document.getElementById("itemBillable").value= item_billable[item_identifier];
		   	if (null != exp_dt[item_identifier]){
		   		document.getElementById("expdt").value= new Date(exp_dt[item_identifier]);
		   	} else{
		   		document.getElementById("expdt").value = "";
		   	}
	   	} else {
	   		document.stockissueform.issuQty.value = '1';
	  		document.stockissueform.issuQty.readOnly = true;
	   	}
	   	itemUntObj = document.stockissueform.item_unit;
	 } else{
	 	if(identification_type[item_identifier] == 'S'){
	  		document.patientissueform.issuQty.value = '1';
	  		document.patientissueform.issuQty.readOnly = true;
		}
	   	if(identification_type[item_identifier] != 'S'){
	   		document.patientissueform.issuQty.value = '';
	   		document.patientissueform.issuQty.readOnly = false;
	   	}
	   	if (document.getElementById("item").checked) {
		   	document.patientissueform.stocktype.value = stock_type[item_identifier];
		   	document.getElementById("itemBillable").value= item_billable[item_identifier];
	   	} else {
	   		document.patientissueform.issuQty.value = '1';
	  		document.patientissueform.issuQty.readOnly = true;
	   	}
		if (itemdetails != null) {
			for (var l=0;l<itemdetails.length;l++) {
				if (itemdetails[l].batch_no == item_identifier) {
					if (showCharges == 'A'){
						document.getElementById("itemMrp").value = (itemdetails[l].issue_rate_expr == null && itemdetails[l].visit_selling_expr == null && itemdetails[l].store_selling_expr == null) ?
								( itemdetails[l].selling_price == null ? parseFloat(itemdetails[l].orig_mrp).toFixed(decDigits) : parseFloat(itemdetails[l].selling_price).toFixed(decDigits) ) : parseFloat(itemdetails[l].mrp).toFixed(decDigits);
						document.getElementById("origRate").value = (itemdetails[l].issue_rate_expr == null && itemdetails[l].visit_selling_expr == null && itemdetails[l].store_selling_expr == null) ?
								( itemdetails[l].selling_price == null ? parseFloat(itemdetails[l].orig_mrp).toFixed(decDigits) : parseFloat(itemdetails[l].selling_price).toFixed(decDigits) ) : parseFloat(itemdetails[l].mrp).toFixed(decDigits);
						document.getElementById("unitMrp").value = parseFloat(itemdetails[l].unit_mrp).toFixed(decDigits);
					}
					document.getElementById("mrp").value = itemdetails[l].issue_rate == null ?
								( itemdetails[l].selling_price == null ? parseFloat(itemdetails[l].orig_mrp).toFixed(decDigits) : parseFloat(itemdetails[l].selling_price).toFixed(decDigits) ) : parseFloat(itemdetails[l].mrp).toFixed(decDigits);
					document.getElementById("unit_mrp").value = parseFloat(itemdetails[l].unit_mrp).toFixed(decDigits);
		    		document.getElementById("txType").value = itemdetails[l].tax_type;
		    		document.getElementById("tax").value = itemdetails[l].tax_rate;
		    		document.getElementById("Unit").value = itemdetails[l].issue_base_unit;
		    		document.getElementById("item_batch_id").value = itemdetails[l].item_batch_id;
		    		getIssueItemPriceUsingExpr(itemdetails[l].medicine_id);
		    		if (null != exp_dt[item_identifier]){
		    			document.getElementById("expdt").value = new Date(itemdetails[l].exp_dt);
		    		} else{
		    			document.getElementById("expdt").value = "";
		    		}

		    		var origMRP = (itemdetails[l].issue_rate_expr == null && itemdetails[l].visit_selling_expr == null && itemdetails[l].store_selling_expr == null) ?
								( itemdetails[l].selling_price == null ? parseFloat(itemdetails[l].orig_mrp).toFixed(decDigits) : parseFloat(itemdetails[l].selling_price).toFixed(decDigits) ) : parseFloat(itemdetails[l].mrp).toFixed(decDigits);
					var mrp = (itemdetails[l].issue_rate_expr == null && itemdetails[l].visit_selling_expr == null && itemdetails[l].store_selling_expr == null) ?
								( itemdetails[l].selling_price == null ? parseFloat(itemdetails[l].orig_mrp).toFixed(decDigits) : parseFloat(itemdetails[l].selling_price).toFixed(decDigits) ) : parseFloat(itemdetails[l].mrp).toFixed(decDigits);
					document.patientissueform.origMRP.value = origMRP;
					var isMrkUpRt = itemdetails[l].is_markup_rate;
		    		var dis = document.getElementById('discount').value;
		    		var categoryDisc = parseFloat(dis);
		    		categoryDisc = (isMrkUpRt == 'Y' && (parseFloat(mrp) < parseFloat(origMRP))) ? 0 : categoryDisc;
						document.patientissueform.discount.value = parseFloat(categoryDisc);
				}
			}
		}
		itemUntObj = document.patientissueform.item_unit;
	}
	document.getElementById("Unit").value = pkgSize[item_identifier];
	var btch = {packageSize: pkgSize[item_identifier], issueUnits: issueUnits[item_identifier], packageUOM: pkgUOM[item_identifier]};
}

var added_item = [];
var added_identifier = [];
var added_controleType = [];


function addItemsToTable() {
	if(gItemType == 'orderkit') {
		var orderkitName = document.getElementById("orderkits").value;
		if(orderkitName == undefined || orderkitName == '' || orderkitName == null) {
			showMessage("js.sales.issues.storesuserissues.orderkit.required");
			document.getElementById("orderkits").focus();
			return false;
		}
		addMedicinesFromOrderKit();
	} else {
		var myform = document.forms[0];
		if ( myform.itemMrp != null && myform.itemMrp.value == '') {
			myform.itemMrp.value = 0;
		}

		if(document.getElementById("itemListtable").rows.length>1){
			for(var i=1;i<=document.getElementById("itemListtable").rows.length-1;i++){

				var currentbatchNo= document.forms[0].batch.value;
				var currentMedicineId=document.forms[0].items.value;
				if ( (currentbatchNo==document.getElementById("item_identifier"+i).value)
						&& (encodeURIComponent(currentMedicineId)==document.getElementById("item_name"+i).value)
						&& ( (parseInt(document.getElementById("dialogId").value) ) != i) ){
					showMessage("js.sales.issues.storesuserissues.duplicateentry");
					return false;
					break;
				}
			}
		}

		// validate rate increase/decrease based on rights
		var origRate = getElementPaise(myform.origRate);
		var newRate = getElementPaise(myform.itemMrp);

		if ((newRate < origRate) && (gAllowRateDecrease != 'A') && (document.getElementById('batch').value != '')) {
			var msg=getString("js.sales.issues.storesuserissues.notauthorized.decreasetheratebelow");
			msg +=formatAmountPaise(origRate);
			alert(msg);
			return false;
		}

		if ((newRate > origRate) && (gAllowRateIncrease != 'A') && (document.getElementById('batch').value != '')) {
			var msg=getString("js.sales.issues.storesuserissues.notauthorized.increasetherateabove");
			msg += formatAmountPaise(origRate);
			alert(msg);
			return false;
		}

		if(checkQty()){
				var valid = true;

				if (myform.issuQty.value == 0) {
					showMessage("js.sales.issues.storesuserissues.issuequantity.notbezero");
					myform.issuQty.focus();
					return false;

				}

				if (!(isAmount(myform.issuQty.value))) {
					showMessage("js.sales.issues.storesuserissues.entervalidquantity");
					return false;
				}

				if (!isValidNumber(myform.issuQty, allowDecimalsForQty)) return false;

				var itemtable = document.getElementById("itemListtable");
				var len = itemtable.rows.length;

				if(!checkItemDetails(len,itemtable))
					return false;
				var item = myform.items.value;
				var batchIndex = myform.batch.value;
				var qty = myform.issuQty.value;
				var pkgSize = myform.pkg_size.value;
				var item_identifier = myform.batch.value;
				var stktype = myform.stocktype.value;
				var pkg_size =  document.getElementById("pkg_size").value ;
				var itembillable = myform.itemBillable.value;
				var issueuom = myform.item_unit.value;
				var chkExpiry = '';
				var isMrkUpRate = '';
				var itemBatchDetails = findInList(itemdetails, 'batch_no', batchIndex);

				if (null != document.patientissueform)
					isMrkUpRate = myform.isMarkUp.value;
				if (('undefined' == document.getElementById("expdt").value) ||
						(null == document.getElementById("expdt").value) ||
						(document.getElementById("expdt").value == "")) {
					myform.expdt.value = "";
					chkExpiry = "";
				} else {
					chkExpiry = itemBatchDetails.exp_dt;
					var daysToExpire = chkExpiry != null ? getDaysToExpire(chkExpiry) : 'woexp';
					if (null != gAllowExpiredSale){
						if ((gAllowExpiredSale != 'Y') && ( daysToExpire < 0 )) {
							showMessage("js.sales.issues.storesuserissues.itembeingissue.salreadyexpired");
							return false;
						}
					}
				}

				var controlTypeName = document.getElementById("control_type_name").value;
				var dialogId = document.getElementById("dialogId").value;
				var len = document.getElementById("itemListtable").rows.length;
				if (hdrugAlertNeeded == 'Y' && controlTypeName != 'Normal' && dialogId == len - 1) {
					var msg=getString("js.sales.issues.storesuserissues.medicinecontroltype");
					msg+=controlTypeName;
					msg+=getString("js.sales.issues.storesuserissues.canonlybesold.prescription");
					msg+="\n";
					msg +=getString("js.sales.issues.storesuserissues.verifythepatientsprescription");
					alert(msg);
				}
				var billNo = document.getElementById('bill_no').value;
				var isTpaBill = getBillIsTpa(billNo);
				var coverdbyinsuranceflag = null;
				var medDiscWithoutInsurance = 0;
				var medDiscWithInsurance = 0;
				if(document.getElementById("coverdbyinsuranceflag") != null) {
					coverdbyinsuranceflag = document.getElementById("coverdbyinsuranceflag").value;
					medDiscWithoutInsurance = myform.medDiscWithoutInsuranceForm.value;
	                medDiscWithInsurance = myform.medDiscWithInsuranceForm.value;
				}

				addToInnerHTML(item,myform.store.value,item_identifier,chkExpiry,qty,pkg_size,stktype,itembillable,"NoGroup", isMrkUpRate, issueuom, controlTypeName, coverdbyinsuranceflag, medDiscWithoutInsurance, medDiscWithInsurance);
				document.forms[0].barCodeId.value = "";
				//resetMedicineDetails ();
			}
			// Insurance 3.0 calculation after add of item
			var billNo = document.getElementById('bill_no').value;
			var isTpaBill = getBillIsTpa(billNo);
			if(isTpaBill)
				onClickProcessInsForIssues('patientissueform');
	}


}

function addToInnerHTML(item,storeId,item_identifier,expdt,qty,pkg_size,stktype,itembillable,transaction, isMrkUpRate, issueuom, controlTypeName, coverdbyinsuranceflag, medDiscWithoutInsurance, medDiscWithInsurance){
	var itemtable = document.getElementById("itemListtable");
	var dialogId  = document.getElementById("dialogId").value;
	var tabLen = itemtable.rows.length;
	added_item[tabLen] = item;
	added_identifier[tabLen] = item_identifier;
	added_controleType[tabLen] = controlTypeName;
	var imgbutton = makeImageButton('itemCheck','itemCheck'+dialogId,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this.id,itemRow'+dialogId+','+dialogId+')');
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	if(coverdbyinsuranceflag != null && coverdbyinsuranceflag != undefined && coverdbyinsuranceflag == "f" && isTpaBill) {
		document.getElementById("itemLabel"+dialogId).innerHTML ='<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+item;
	} else if (stktype == true || stktype=='t' || stktype == 'true' ) {
		document.getElementById("itemLabel"+dialogId).innerHTML ='<img class="flag" src= "'+popurl+'/images/grey_flag.gif"/>'+item;
	}
	else {
		setNodeText(document.getElementById("itemLabel"+dialogId), item, 25);
	}

	if (controlTypeName != 'Normal')
		document.getElementById("controleTypeLabel"+dialogId).innerHTML = "<font color='blue'>"+controlTypeName+"</font>";
	else
		setNodeText(document.getElementById("controleTypeLabel"+dialogId), controlTypeName, 25);

	document.getElementById("identifierLabel"+dialogId).textContent =item_identifier ;
	document.getElementById("pkgUnit"+dialogId).value = document.getElementById("Unit").value;

	setFieldValue("medicineId", getFieldValue("medicine_id"), dialogId);

	document.getElementById("itemBatchId"+dialogId).value = itemBatchId[item_identifier];
	if ((null != expdt) && (expdt != "")){
		if((null != exp_dt[item_identifier]) && (exp_dt[item_identifier] != "")) {
			document.getElementById("expdtLabel"+dialogId).textContent =formatExpiry(exp_dt[item_identifier]) ;
			document.getElementById("exp_dt"+dialogId).value = formatExpiry(exp_dt[item_identifier]);
		}else {
			document.getElementById("expdtLabel"+dialogId).textContent =formatExpiry(new Date(expdt)) ;
			document.getElementById("exp_dt"+dialogId).value = formatExpiry(new Date(expdt));
		}
	} else{
		document.getElementById("expdtLabel"+dialogId).textContent = "";
	}
	document.getElementById("issue_qtyLabel"+dialogId).textContent = qty;
	document.getElementById("pkgSizeLabel"+dialogId).textContent = pkg_size;

	document.getElementById("uomLabel"+dialogId).textContent = issueuom == 'I' ? issueUnits[item_identifier] : pkgUOM[item_identifier];

	document.getElementById("temp_charge_id"+dialogId).value = "_"+dialogId;
	document.getElementById("item_name"+dialogId).value = encodeURIComponent(item) ;
	document.getElementById("storeId"+dialogId).value = storeId;
	document.getElementById("item_identifier"+dialogId).value =item_identifier ;
	//document.getElementById("exp_dt"+dialogId).value = formatExpiry(exp_dt[item_identifier])
	document.getElementById("stype"+dialogId).value =stktype;
	document.getElementById("issue_qty"+dialogId).value = issueuom == 'I' ? parseFloat(qty).toFixed(decDigits) : parseFloat(pkg_size*qty).toFixed(decDigits) ;
	if (document.getElementById("item_billable_hidden"+dialogId) != null){
		document.getElementById("item_billable_hidden"+dialogId).value = itembillable == '' ? item_billable[item_identifier] : itembillable ;
	}
	if ( document.getElementById("hdeleted"+dialogId).value != 'true' )
		document.getElementById("hdeleted"+dialogId).value = "false" ;
	if(document.getElementById("itemRow"+dialogId).firstChild == null) {
		document.getElementById("itemRow"+dialogId).appendChild(imgbutton);
	}

	document.getElementById("itemUnit"+dialogId).value = issueuom;

	if (null != document.patientissueform) {
		var cp = parseFloat(document.getElementById("cp").value);
		var mrp = parseFloat(document.getElementById("mrp").value);
		var origMRP = parseFloat(document.getElementById("origMRP").value);
		if(document.getElementById("Disc").value != '') {
			var categoryDisc = parseFloat(document.getElementById("Disc").value).toFixed(decDigits);
			if (isMrkUpRate == 'Y') {
				// in this case 'mrp' carries mark up rate, 'origMRP carries item's mrp..
				categoryDisc = mrp < origMRP ? 0 : categoryDisc;
			}
			document.getElementById("medDisc"+dialogId).value = parseFloat(categoryDisc + gDefaultDiscountPer).toFixed(decDigits);
		} else {
			document.getElementById("medDisc"+dialogId).value = parseFloat(gDefaultDiscountPer).toFixed(decDigits);
		}

		var disc_amt = 0;

		if(null != document.patientissueform) {
			if(null != document.getElementById("discount"))
				var dis_per = parseFloat(document.getElementById("discount").value).toFixed(decDigits) ;
			var isQty = (issueuom == 'I' ? parseFloat(qty).toFixed(decDigits) : parseFloat(pkg_size*qty).toFixed(decDigits));
			var unitMrp =  parseFloat(document.getElementById("unitMrp").value).toFixed(decDigits);
			var tot_amt = parseFloat(unitMrp * isQty).toFixed(decDigits);
			if(null != document.getElementById("discount"))
				disc_amt = parseFloat((tot_amt * dis_per) / 100).toFixed(decDigits);
		}

		if (showCharges!= 'A')
			document.getElementById('discount').value = parseFloat(dis_per + document.getElementById("medDisc"+dialogId).value).toFixed(decDigits);

		document.getElementById("pkgUnit"+dialogId).value = document.getElementById("Unit").value;
		document.getElementById("category"+dialogId).value = document.getElementById("categoryId").value;
		document.getElementById("insurancecategory"+dialogId).value = empty( document.getElementById("insuranceCategoryId").value ) ? -1 : document.getElementById("insuranceCategoryId").value;

		for(var i=1;i<=document.getElementById("itemListtable").rows.length-1;i++) {
			var currentInsuranceCategory = document.forms[0].insuranceCategoryId.value;
			if ( (currentInsuranceCategory ==document.getElementById("insurancecategory"+i).value && document.getElementById("firstOfCategory"+i).value == "true")
					&& ( (parseInt(document.getElementById("dialogId").value) ) != i)){
				document.getElementById("firstOfCategory"+dialogId).value = "false";
				document.forms[0].isFirstOfCategory.value = "false";
				break;
			}
		}

		document.getElementById("firstOfCategory"+dialogId).value = document.getElementById("isFirstOfCategory").value;
		if (showCharges == 'A') {
			document.getElementById("mrpLabel"+dialogId).textContent = parseFloat(document.getElementById("itemMrp").value).toFixed(decDigits);
			document.getElementById("unitmrpLabel"+dialogId).textContent =document.getElementById("unitMrp").value;
			document.getElementById("pkgMRPLabel"+dialogId).textContent = parseFloat(document.getElementById("origMRP").value).toFixed(decDigits);
		}

		document.getElementById("mrpHid"+dialogId).value = parseFloat(document.getElementById("itemMrp").value).toFixed(decDigits);
		document.getElementById("discountHid"+dialogId).value = (document.getElementById("discount").value);
		document.getElementById("pkgmrp"+dialogId).value = document.getElementById("mrp").value;
		document.getElementById("taxPer"+dialogId).value = document.getElementById("tax").value;
		document.getElementById("taxType"+dialogId).value = document.getElementById("txType").value;
		document.getElementById("isMarkUpRate"+dialogId).value = document.getElementById("isMarkUp").value;
		document.getElementById("originalMRP"+dialogId).value = document.getElementById("origMRP").value;
		document.getElementById("cat_payable"+dialogId).value = coverdbyinsuranceflag;
		document.getElementById("issueRateExpr"+dialogId).value = document.getElementById("issue_rate_expr").value;
		document.getElementById("visitSellingPriceExpr"+dialogId).value = document.getElementById("visit_selling_expr").value;
		document.getElementById("storeSellingPriceExpr"+dialogId).value = document.getElementById("store_selling_expr").value;
		reCalcRowAmounts(dialogId);

	}

	var nextrow =  document.getElementById("tableRow"+(eval(dialogId)+1));
	if (nextrow == null){
		AddRowsToGrid(tabLen);
	}

	if(document.getElementById("medDiscWithoutInsurance"+dialogId)!= null)
		document.getElementById("medDiscWithoutInsurance"+dialogId).value = medDiscWithoutInsurance;
	if(document.getElementById("medDiscWithInsurance"+dialogId)!= null)
		document.getElementById("medDiscWithInsurance"+dialogId).value = medDiscWithInsurance;

	var editButton = document.getElementById("add"+dialogId);
	var eBut =  document.getElementById("addBut"+dialogId);
	if ( document.getElementById("hdeleted"+dialogId).value != 'true' )
		editButton.setAttribute("src",popurl+'/icons/Edit.png');

	eBut.setAttribute("title", getString("js.sales.issues.storesuserissues.edititem"));
	eBut.setAttribute("accesskey", "");

	if(dialogId == (eval(tabLen)-2)){
		handleCancel();
		return false;
	}
	if(transaction == 'NoGroup')
		openDialogBox(eval(dialogId)+1);

}

function checkItemDetails(len,items,batch){
	myform = document.forms[0];
	if((null != added_item) && (added_item.length !=0)){
		for(var i = 1;i< len;i++){
			var checkbox = "itemRow"+i;
			if(added_item[i] == items && added_identifier[i] == batch){
				if(!document.getElementById(checkbox).checked && ((parseInt(document.getElementById("dialogId").value)+1) != i)){
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




function changeContainer(kit,trantype){
	deleteTableRows();
	if(kit == 'kits'){
		if (trantype == 'Hospital'){
			document.stockissueform.issuQty.value = '';
			document.stockissueform.pkg_size.value = '';
			for (i=0;i<document.stockissueform.batch.length; i++){
				document.stockissueform.batch[i].selected = false;
			}
			document.stockissueform.items.value = '';
		} else{
			document.patientissueform.issuQty.value = '';
			document.patientissueform.pkg_size.value = '';
			for (i=0;i<document.patientissueform.batch.length; i++){
				document.patientissueform.batch[i].selected = false;
			}
			document.patientissueform.items.value = '';

		}
		oItemAutoComp.destroy();
		var kitarray = [];
		if(type == 'Hospital'){
			var j = 0;
			for(var i = 0;i<kitlist.length;i++){
				if(kitlist[i].kit_type == 'H'){
					kitarray[j++] = kitlist[i].kit_name;
				}
			}
		}else{
			for(var i = 0;i<kitlist.length;i++){
					kitarray[i] = kitlist[i].kit_name;
			}
		}
		this.oitemSCDS = new YAHOO.widget.DS_JSArray(kitarray);
		oItemAutoComp = new YAHOO.widget.AutoComplete('items','item_dropdown', this.oitemSCDS);
		oItemAutoComp.prehightlightClassName = "yui-ac-prehighlight";
		oItemAutoComp.typeAhead = false;
		oItemAutoComp.useShadow = false;
		oItemAutoComp.allowBrowserAutocomplete = false;
		oItemAutoComp.minQueryLength = 0;
		oItemAutoComp.forceSelection = true;
		oItemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		oItemAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
		oItemAutoComp.textboxFocusEvent.subscribe(function(){
			var sInputValue = YAHOO.util.Dom.get('items').value;
			if(sInputValue.length === 0) {
				var oSelf = this;
				setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
			} });
		oItemAutoComp.itemSelectEvent.subscribe(getKitDetails);
		document.getElementById('kithyper').style.display = "block";
	}else{

			setItems();

		document.getElementById('kithyper').style.display = "none";
	}
}
/*function getKitDetails(){
	var kit = YAHOO.util.Dom.get('items').value;
	var url;
	if(item != ''){
		var ajaxReqObject = newXMLHttpRequest();
		url = "StockUserIssue.do?_method=getKitDetails&kit_name="+encodeURIComponent(kit);
		getResponseHandlerText(ajaxReqObject, fillKitDetails, url);
		getkitItemDetails(kit);
	}
}
function fillKitDetails(responseText){
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;
    eval("kitdetails = " + responseText);
    itemdetails = kitdetails;
    if(kitdetails.length == 0){
    	alert("There is no available stock for the requested item");
    	document.getElementById("items").value = '';
    	document.getElementById("batch").value="";
    	document.getElementById("batch").length = 1;
    	document.getElementById("issuetype").value="";
    	document.getElementById("issuQty").value="";
    	return false;
    }
	var j=1
    for(var t= 0;t<kitdetails.length;t++){
    	document.getElementById('batch').options.length = document.getElementById("batch").options.length+1;
	    document.getElementById("batch").options[j].text = kitdetails[t].kit_identifier+'/1/-';
    	document.getElementById("batch").options[j].value = kitdetails[t].kit_identifier;
    	document.getElementById("issuQty").value='1';
    	document.getElementById("issuQty").readOnly = true;
    	j++;
    }
    if (kitdetails.length > 1) document.getElementById("batch").disabled = false;
    else {
    	document.getElementById("batch").selectedIndex = 1;
    	document.getElementById("batch").disabled = true;
    }
	if(document.getElementById("mrno") != null ){
	if(document.getElementById("bill_no").value == ''){
	if(document.getElementById("mrno").value == ''){
		return true;
	}
		alert("Patient does not have any open credit bill,can not issue this kit");
		document.getElementById("creditbill").style.display = 'none';
		document.getElementById("items").value='';
		return false;
	}else{
		document.getElementById("creditbill").style.display = 'block';
	}
	}else{
		document.getElementById("creditbill").style.display = 'none';
	}
}*/

function cancelRow(imgObj,btn,len){
	var deletedInput = document.getElementById('hdeleted'+len);
	var tot_amt = document.getElementById("totAmt");
	var tot_amount = document.getElementById("totamtLabel"+len);
	if(deletedInput.value == 'false'){
		YAHOO.util.Dom.get(btn).disabled = true;
		deletedInput.value = 'true';
		if (null != document.patientissueform && document.getElementById("firstOfCategory"+len).value == "true"
					&& parseInt(document.getElementById("planId").value)>0)
			alert(getString("js.sales.issues.storesuserissues.deletingfirstitem.insurancecategory")+"\n"+getString("js.sales.issues.storesuserissues.updatepatientcopay.fixedamountpercategory.foradditionalcharges")+"\n"+getString("js.sales.issues.storesuserissues.sameinsurancecategory"));
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
		onClickProcessInsForIssues('patientissueform');
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
	}else{
			var selectedIssueType = getRadioSelection(document.stockissueform.issueType);
			if( selectedIssueType == 'u' && issuetodept == 'N'){
				if(document.getElementById("hosp_user").value == ''){
					showMessage("js.sales.issues.storesuserissues.enterausername");
					document.getElementById("hosp_user").focus();
					button.disabled = false;
					return false;
				}
			}else if(selectedIssueType == 'd'){
				if(document.getElementById("issue_dept").value == ''){
					alert("Please select a Department");
					document.getElementById("issue_dept").focus();
					button.disabled = false;
					return false;
				}
			} else{
				if(document.getElementById("issue_ward").value == ''){
					alert("Please select a Ward");
					document.getElementById("issue_ward").focus();
					button.disabled = false;
					return false;
				}
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
	var myform = document.forms[0];
/*
	if(document.getElementById("kits").checked){
		return true;
	} */

	var item_identifier = document.forms[0].batch[document.forms[0].batch.selectedIndex].value;
	var itemUnit = myform.item_unit.value;
	var expected_qty = myform.issuQty.value ;

	if (itemdetails != null){
		for(var t= 0;t<itemdetails.length;t++){
			if(item_identifier == itemdetails[t].batch_no){
				expected_qty = itemUnit == 'I' ? expected_qty : parseFloat(expected_qty)*pkgSize[itemdetails[t].batch_no];
				if(expected_qty > itemdetails[t].qty){
					if(stock_negative_sale == 'A')
						return true;
					else if(stock_negative_sale == 'D'){
						showMessage("js.sales.issues.storesuserissues.requestedstock.notavailable");
						myform.issuQty.value = '';
						if (((document.getElementById("issue_rate_expr") && document.getElementById("issue_rate_expr").value != '' && document.getElementById("issue_rate_expr").value != null)) ||
								((document.getElementById("visit_selling_expr") && document.getElementById("visit_selling_expr").value != '' && document.getElementById("visit_selling_expr").value != null)) ||
								((document.getElementById("store_selling_expr") && document.getElementById("store_selling_expr").value != '' && document.getElementById("store_selling_expr").value != null))) {
							myform.unitMrp.value = '';
							myform.itemMrp.value ='';
						}
						return false;
					}else if(stock_negative_sale == 'W'){
						if(!confirm("There is not enough stock for the given issue quantity. Are you sure you want to issue?"))return false;
						else return true;
					}

				}else return true;
			}
		}
	}
	return true;
}

function clear_fields() {
	for( var n = 0; n < document.forms.length; n++ ) {
	   for(var i = 0; i < document.forms[n].elements.length; i++) {
	           if( document.forms[n].elements[i].type == 'text') {
	                document.forms[n].elements[i].value = '';
	           }
	    }
	}
}
//Cleanup required in this method.
function onChangeStore(storechanged,trantype){
	var itemTable = document.getElementById('itemListtable');
	var numItems = itemTable.rows.length-1;
	for ( var i=0; i<numItems; i++ ) {
		itemTable.deleteRow(1);
	}
	AddRowsToGrid(1);

	var img_button = document.createElement( 'img' );
	img_button.src = cpath + '/images/delete.jpg';
	var storeObj = null;

	if (trantype == 'Patient'){
		storeObj = document.patientissueform.store;
		storeObj.value = storechanged;
		isBatchMrpEnabled(storeObj.value);
		document.patientissueform.store.value = storechanged;
		document.getElementById('patientDetails').visibility="hidden";
		document.getElementById("patientDetails").style.display = 'none';
		visitIdFromBill = document.getElementById("mrno").value;
		indentStore = storechanged;
		clearPatientDetails();
		document.patientissueform.mrno.value = "";
		enable(document.getElementById("mrno"),"");
	} else if (trantype == 'Hospital'){
		 storeObj = document.stockissueform.store;
		 storeObj.value = storechanged;
		/* if ( issuetodept == 'N' ){
			 enable(document.getElementById("hosp_user"),"");
				document.getElementById("hosp_user_mand").style.display = 'block';
		 } else {
			 enable(document.getElementById("issue_dept"),"");
		 }*/
	   }

	document.getElementById("item").checked = true;
	/*document.getElementById("issue_dept").disabled = true;
	document.getElementById("issue_dept_mand").style.visibility = 'hidden';
	document.getElementById("issue_ward").disabled = true;
	document.getElementById("issue_ward_mand").style.visibility = 'hidden';*/

	getMedicinesForStore(storeObj, setItems);
}

function onKeyPressAddQty(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 || charCode==3 ) {
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

function submitForm(button,type){
	if(validate(button,type)) {

		if ( type == 'Patient') {
			if (isSharedLogIn == 'Y')
				loginDialog.show();
			else {
				button.disabled = true;
				document.patientissueform.action = "StockPatientIssue.do?_method=saveItemsIssued&tran_type="+type;
				document.patientissueform.submit();
			}

		} else{
			document.stockissueform.issued_to.value = Hospital_field.value;
			button.disabled = true;
			document.stockissueform.action = "StockUserIssue.do?_method=saveItemsIssued&tran_type="+type;
			document.stockissueform.submit();
		}
	}
}

/*
 * Take action when the user has typed an invalid mrno
 */
var getPatientInfo = null;
function onInvalidMrno() {
	clearPatientDetails();
	document.patientissueform.mrno.value = "";
	// a timeout is required so that the alert before this does not
	// cause yet another onblur event to interfere with the normal process.
	setTimeout("document.patientissueform.mrno.focus()", 0);
}
var patient = null;
function getPatientDetails() {
    var mrno = document.getElementById("mrno").value;
    var ajaxobj1 = newXMLHttpRequest();
	var url = cpath+'/stores/StockPatientIssue.do?_method=getPatientDetailsJSON&mrno='+mrno+"&storeId="+document.getElementById("store").value;
	getResponseHandlerText(ajaxobj1,patientDetailsResponseHandler,url.toString());



}

function getPatientFromBill(){
	if ((null != visitIdFromBill) && (visitIdFromBill != '') && !noAccess ){
		document.getElementById("mrno").value = visitIdFromBill
		var ajaxobj1 = newXMLHttpRequest();
		var patientIndentNo = document.patientissueform.patient_indent_no_param.value;
		var url = cpath+'/stores/StockPatientIssue.do?_method=getPatientDetailsJSON&mrno='+visitIdFromBill+"&storeId="+indentStore;
		url = url + ( !empty(patientIndentNo) ? "&patient_indent_no="+patientIndentNo : "" );
		getResponseHandlerText(ajaxobj1,patientDetailsResponseHandler,url.toString());

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

	getPatientInfo = getPatDetails(patient.mr_no);
	populateBillType(getPatientInfo.bills);
	setPatientDetails(patient);

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
		onClickProcessInsForIssues('patientissueform');
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
		exp_dt[batch.batch_no] = batch.exp_dt;

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
		if (qtyForBatch > 0)
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
		if ((expiredQty > 0) && !gAllowExpiredSale) {
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
		msg += getString("js.sales.issues.storesuserissues.proceedingwithissue.lcausestockbecomenegative");
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
	//if(isTpaBill) {
	catPayableInfo = getCatPayableStatus(medicineId, false);
	//}
	if(catPayableInfo!= null && catPayableInfo != undefined ) {
		var calimbaleInfo = catPayableInfo.plan_category_payable != null? catPayableInfo.plan_category_payable:"";
		if(calimbaleInfo == 'f') {
			document.getElementById('coverdbyinsurance').innerHTML = "No";
			document.getElementById('coverdbyinsurance').style.color = "red";
		} else {
			document.getElementById('coverdbyinsurance').innerHTML = "Yes";
			document.getElementById('coverdbyinsurance').style.color = "#666666";
		}
		document.getElementById('coverdbyinsuranceflag').value = calimbaleInfo;
		document.getElementById("cat_payable"+rowsLength).value = calimbaleInfo;
		if(calimbaleInfo != null && calimbaleInfo != undefined && calimbaleInfo == "f" && isTpaBill) {
			document.getElementById("itemLabel"+rowsLength).innerHTML ='<img class="flag" src= "'+popurl+'/images/purple_flag.gif"/>'+medicineName;
		} else {
			document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
		}
	} else {
		document.getElementById("itemLabel"+rowsLength).textContent = medicineName;
	}

	var itemRate = isNotNullObj(batch.orig_selling_price) ? batch.orig_selling_price : batch.mrp;
    //document.getElementById("itemLabel"+rowsLength).textContent = medicineName;

    controlTypeName = batch.control_type_name;
    if (controlTypeName != 'Normal')
		document.getElementById("controleTypeLabel"+rowsLength).innerHTML = "<font color='blue'>"+controlTypeName+"</font>";
	else
		setNodeText(document.getElementById("controleTypeLabel"+rowsLength), controlTypeName, 25);

    //document.getElementById("controleTypeLabel"+rowsLength).textContent = batch.control_type_name;
    document.getElementById("identifierLabel"+rowsLength).textContent = batch.batch_no;
    document.getElementById("expdtLabel"+rowsLength).textContent = formatExpiry(batch.exp_dt);
    document.getElementById("issue_qtyLabel"+rowsLength).textContent = ( batch.qty == 1 ? batch.qty : qtyForBatch );//serial item can have one item per batch
    document.getElementById("uomLabel"+rowsLength).textContent = ( indent.uom == '' ? indent.uom_display : indent.uom );
    document.getElementById("pkgSizeLabel"+rowsLength).textContent = batch.issue_base_unit;
    document.getElementById("itemBatchId"+rowsLength).value = batch.item_batch_id;
    document.getElementById("issueRateExpr"+rowsLength).value = batch.issue_rate_expr;
    document.getElementById("visitSellingPriceExpr"+rowsLength).value = batch.visit_selling_expr;
    document.getElementById("storeSellingPriceExpr"+rowsLength).value = batch.store_selling_expr;
    document.getElementById("medicineId"+rowsLength).value = medicineId;


    if (showCharges == 'A') {
	   	document.getElementById("mrpLabel"+rowsLength).textContent = itemRate;
	    document.getElementById("unitmrpLabel"+rowsLength).textContent = parseFloat(itemRate/batch.issue_base_unit).toFixed(2);
	    document.getElementById("pkgMRPLabel"+rowsLength).textContent = batch.mrp;
	    document.getElementById("discountLabel"+rowsLength).textContent = batch.meddisc;
    }

    var imgbutton = makeImageButton('itemCheck','itemCheck'+rowsLength,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this.id,itemRow'+rowsLength+','+rowsLength+')');

	var editButton = document.getElementById("add"+rowsLength);
	var eBut =  document.getElementById("addBut"+rowsLength);
	editButton.setAttribute("src",popurl+'/icons/Edit.png');
	eBut.setAttribute("title", "Edit Item");
	eBut.setAttribute("accesskey", "");
	document.getElementById("itemRow"+rowsLength).appendChild(imgbutton);

	document.getElementById("temp_charge_id"+rowsLength).value = "_"+rowsLength;
	document.getElementById("item_name"+rowsLength).value = encodeURIComponent(medicineName);
	document.getElementById("storeId"+rowsLength).value = document.getElementById("store").value;
	document.getElementById("pkgmrp"+rowsLength).value = batch.mrp;
	document.getElementById("medDisc"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);

	document.getElementById("taxPer"+rowsLength).value = batch.tax;
	document.getElementById("pkgUnit"+rowsLength).value = batch.issue_base_unit;
	document.getElementById("taxType"+rowsLength).value = batch.tax_type;
	document.getElementById("category"+rowsLength).value = batch.category_id;
	document.getElementById("insurancecategory"+rowsLength).value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;
	document.getElementById("isMarkUpRate"+rowsLength).value = '';
	document.getElementById("originalMRP"+rowsLength).value = batch.mrp;
	document.getElementById("itemUnit"+rowsLength).value = indent.qty_unit;//indent process always in issue_units
	document.getElementById("item_identifier"+rowsLength).value = batch.batch_no;
	document.getElementById("exp_dt"+rowsLength).value = batch.exp_dt;
	document.getElementById("stype"+rowsLength).value = document.forms[0].stocktype.value;
	document.getElementById("issue_qty"+rowsLength).value = ( batch.qty == 1 ? batch.qty : (indent.qty_unit == 'I' ? qtyForBatch : qtyForBatch*batch.issue_base_unit ) );
	document.getElementById("mrpHid"+rowsLength).value = itemRate;
	document.getElementById("unitMrpHid"+rowsLength).value = itemRate/batch.issue_base_unit;
	document.getElementById("origUnitMrpHid"+rowsLength).value = itemRate/batch.issue_base_unit;
	document.getElementById("discountHid"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById("indent_item_id"+rowsLength).value = indent.indent_item_id;
	document.getElementById("patient_indent_no"+rowsLength).value = indent.patient_indent_no;
	document.getElementById("hdeleted"+rowsLength).value = false;
	document.getElementById("item_billable_hidden"+rowsLength).value = batch.billable;
	document.getElementById("patper").value = batch.patient_percent;
	document.getElementById("patcatamt").value = batch.patient_amount_per_category;
	document.getElementById("patcap").value = batch.patient_amount_cap;
	document.getElementById("insuranceCategoryId").value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;

	document.getElementById("firstOfCategory"+rowsLength).value = document.getElementById("isFirstOfCategory").value;

	document.getElementById( "medDiscWithoutInsurance"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById( "medDiscWithInsurance"+rowsLength).value = '';

	var discountPlanId = document.getElementById("discountPlanId").title;
	var insuranceCategoryId = batch.insurance_category_id;
	var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";

	if(priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y') {
		var discountPlanDetailsJSON = JSON.parse(discountPlansJSON);
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {
			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];
				if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountPlanId && insuranceCategoryId == item.applicable_to_id ) {
					//document.getElementById( "discountHid"+rowsLength).value =  item["discount_value"];
					document.getElementById("medDiscWithInsurance"+rowsLength).value = item["discount_value"];
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
    document.getElementById("discountHid"+rowsLength).value = discount;

    setSellingPrice(batch, qtyForBatch, 0, medicineId, discount, patient, rowsLength);
    reCalcRowAmounts(rowsLength);
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
		var itemName = document.getElementById('item_name'+i).value;
		var itemIdentifier = document.getElementById('item_identifier'+i).value;

		if ((itemName == batch.medicine_id) && (itemIdentifier == batch.batch_no))
			return i;
	}
	return -1;
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
	var mName = patient.middle_name == null ? '' : patient.middle_name;
	var lName = patient.last_name == null ? '' : patient.last_name;
	setNodeText('patientMrno', patient.mr_no, null, patient.mr_no);
	setNodeText('patientName', patient.patient_name + ' ' + mName + ' ' + lName, null,
		patient.patient_name + ' ' + mName + ' ' + lName);
	var patientAgeSex = patient.age_text + ' / ' + patient.patient_gender +
		(patient.dateofbirth == null ? '' : " (" + formatDate(new Date(patient.dateofbirth)) + ")");
	setNodeText('patientAgeSex', patientAgeSex, null, patientAgeSex);
	var referredBy = patient.refdoctorname == null ? "" : patient.refdoctorname;
	setNodeText('referredBy', referredBy, null, referredBy);
	var admitDate = formatDate(new Date(patient.reg_date)) + " " + formatTime(new Date(patient.reg_time));
	setNodeText('admitDate', admitDate, null, admitDate);

	setNodeText('patientVisitNo', patient.patient_id, null, patient.patient_id);
	setNodeText('patientDept', patient.dept_name, null, patient.dept_name);
	setNodeText('patientDoctor', patient.doctor_name == null ? '' : patient.doctor_name, null,
		patient.doctor_name == null ? '' : patient.doctor_name);

	document.patientissueform.visitId.value = patient.visit_id;
    document.patientissueform.visitType.value = patient.visit_type;
    var patientDetailsPlanDetails = patient.patient_details_plan_details;
    gBedType = patient.alloc_bed_type == null
	||patient.alloc_bed_type == '' ?patient.bill_bed_type:patient.alloc_bed_type;
	if (patient.visit_type == 'i') {
		var bed_type = patient.alloc_bed_type == null
			||patient.alloc_bed_type == '' ?patient.bill_bed_type:patient.alloc_bed_type+'/'+patient.alloc_bed_name;
		setNodeText('patientBedType', bed_type, null, bed_type);
	} else setNodeText('patientBedType', '', null, '');

	document.getElementById("item").checked = true;
	enable(document.getElementById("mrno"), 'Patient');
	document.getElementById("planId").value = patient.plan_id;

	var tpaName = patient.tpa_name == null ? '' : patient.tpa_name;
    var orgId = patient.org_id == null ? '' : patient.org_id;
    var orgName = patient.org_name == null ? '' : patient.org_name;

	var isPrimarySponsorAvailable = patient.sponsor_type!= null || patient.sponsor_type!= "";
	var isSecondarySponsorAvailable = patient.sec_sponsor_type!= null || patient.sec_sponsor_type!= "";
	var planIndex = 0;

    if(isPrimarySponsorAvailable) {
		    if (patient.sponsor_type) {
		    	document.getElementById("primarySponsorRow").style.display = 'table-row';
		    	// node, text, length, title...
		    	setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null,
		  		patient.org_name == null ? "" : patient.org_name);

				if( visitWithPlan() ){
					planIndex = patientDetailsPlanDetails.length;

					document.getElementById('priSponsorType').parentNode.style.display = 'table-cell';
					document.getElementById('priSponsorName').parentNode.style.display = 'table-cell';
					setNodeText("priSponsorType", "TPA/Sponsor:", null, "TPA/Sponsor:");
			  		setNodeText('priSponsorName', patient.tpa_name, null,  patient.tpa_name);

					setNodeText('priIDName', 'Insurance Co.:', null, 'Insurance Co.:');
		  			setNodeText('priID', patient.insurance_co_name, null,  patient.insurance_co_name);
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
		  		var insurance_category = patient.insurance_category == null ? 0 : patient.insurance_category;
				var planId = !visitWithPlan() ? 0 : patientDetailsPlanDetails[0].plan_id;
				if (planId != 0) {
				   	document.getElementById("pritpaextrow").style.display = 'table-row';

				   	document.getElementById('networkPlanTypeLblCell').style.display = corporateInsurance == 'N' ? 'table-cell' : 'none';
  					document.getElementById('networkPlanTypeValueCell').style.display = corporateInsurance == 'N' ? 'table-cell' : 'none';
				} else {
				   	document.getElementById("pritpaextrow").style.display = 'none';
				}


		    }
	}

    planIndex = ( planIndex == 0 ? (patientDetailsPlanDetails.length >= 1 ? patientDetailsPlanDetails.length : 0) : ( patientDetailsPlanDetails.length > 1 ? patientDetailsPlanDetails.length : 0));
	if(isSecondarySponsorAvailable) {
		    if (patient.sec_sponsor_type) {
		    	document.getElementById("secSponsorRow").style.display = 'table-row';
		    	// node, text, length, title...

		  		setNodeText("secSponsorType", "sec.TPA/Sponsor:", null, "sec.TPA/Sponsor:");
		  		setNodeText('secSponsorName', patient.sec_tpa_name, null,  patient.sec_tpa_name);

		  		// Set and display Plan details
		  		var insurance_category = patient.insurance_category == null ? 0 : patient.insurance_category;
				var planId = (planIndex >= 0) ? patientDetailsPlanDetails[planIndex-1].plan_id : 0 ;
				if (planIndex > 0)
				   	document.getElementById("sectpaextrow").style.display = 'table-row';
				else
				   	document.getElementById("sectpaextrow").style.display = 'none';

				if( planIndex > 0 ){
					setNodeText('secIDName', "Insurance Co", null, "Sec. Insurance Co");
		  			setNodeText('secID', patient.sec_insurance_co_name, null,  patient.sec_insurance_co_name);
				  	setNodeText('secPlanType', patientDetailsPlanDetails[planIndex-1].plan_type_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_type_name, null,
				  		patientDetailsPlanDetails[planIndex-1].plan_type_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_type_name);
				  	setNodeText('secPlanname', patientDetailsPlanDetails[planIndex-1].plan_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_name, null,
				  		patientDetailsPlanDetails[planIndex-1].plan_name == null ? "" : patientDetailsPlanDetails[planIndex-1].plan_name);
				  	setNodeText('secPolicyId', patientDetailsPlanDetails[planIndex-1].member_id == null ? "" : patientDetailsPlanDetails[planIndex-1].member_id, null,
				  		patientDetailsPlanDetails[planIndex-1].member_id == null ? "" : patientDetailsPlanDetails[planIndex-1].member_id);
			  	}
		    }
	}

	if(!isPrimarySponsorAvailable  && (orgId != '' && orgName != 'GENERAL')){
		document.getElementById("primarySponsorRow").style.display = 'table-row';
		setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null, patient.org_name == null ? "" : patient.org_name);
	}


	if(document.getElementById("bill_no").value == ''){
		showMessage("js.sales.issues.storesuserissues.patientnothave.unpaidbills");
	  	document.getElementById("items").value='';
	  	document.getElementById("creditbill").style.display = 'none';
	  	return false;
  	} else {
  		document.getElementById("creditbill").style.display = 'block';
  	}

  	if(document.patientissueform)
  		document.patientissueform.orgId.value = patient.org_id;
	setItems();
	setSaleBaseType();
	storeRatePlanId = getStoreRatePlanId(patient.org_id);
}

function populateBillType(patientActiveCreditBills){
	document.getElementById("bill_no").length=0;
	var billNowText = "";
	if(document.getElementById("mrno").value != '') {
		if(patientActiveCreditBills != null && patientActiveCreditBills.length > 0) {
			for(var i=0;i<patientActiveCreditBills.length;i++) {
				var visitid = patientActiveCreditBills[i].visit_id;
				if(document.forms[0].visitId.value == patientActiveCreditBills[i].visit_id ){ visitid = visitid };
				if(patientActiveCreditBills[i].status == 'A' && patientActiveCreditBills[i].payment_status == 'U') {
					addOption(document.getElementById("bill_no"),patientActiveCreditBills[i].bill_no,patientActiveCreditBills[i].bill_no);
				}
			}
		}
		if ((null != billNo) && (billNo != "")){
			setSelectedIndex(document.getElementById("bill_no"),billNo);

		}
		var tab = document.getElementById("table_b");
		var row = tab.rows[0];
		var last_element  = row.cells.length;
		if(row &&  last_element >= 2) {
			row.removeChild(row.cells[1]);
		}
		if(patientActiveCreditBills != null && patientActiveCreditBills.length > 0) {
			var td = row.insertCell(-1);
			for(var i=0;i<patientActiveCreditBills.length;i++) {
				var elA = document.createElement('a');
				var txt = document.createTextNode(" | Bill "+patientActiveCreditBills[i].bill_no);
				if(patientActiveCreditBills[i].status == 'A' && patientActiveCreditBills[i].payment_status == 'U') {
					elA.appendChild(txt);
					elA.setAttribute("title", patientActiveCreditBills[i].bill_no);
					elA.setAttribute("href", cpath+'/billing/BillAction.do?_method=getCreditBillingCollectScreen&billNo='+patientActiveCreditBills[i].bill_no);
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
	if (showCharges == 'A') document.getElementById("totAmt").textContent = parseFloat(0).toFixed(decDigits);

	document.getElementById("creditbill").style.display = 'none';
	document.getElementById("bill_no").length=0;
	if (showCharges == 'A') document.getElementById("totAmt").textContent = parseFloat(0).toFixed(decDigits);

	var tab = document.getElementById("table_b");
	var row = tab.rows[0];
	var tdlen  = row.cells.length;
	if (row.cells[1]) {
		row.removeChild(row.cells[1]);
	}
	clearIndentDetails();
}

function setItems(){
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
		oItemAutoComp.minQueryLength = 0;
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
		oItemAutoComp.containerCollapseEvent.subscribe(getItemDetails);
	}
}

/*
 * Called on selection of an item in the item auto comp
 */
function onSelectItem(type, args) {
	var selItemName = args[2][1];
	document.getElementById("items").value = selItemName;
	document.getElementById("items").focus();
}

function getItemBarCodeDetails (val) {
	if (val == '') {
		resetMedicineDetails();
		document.forms[0].items.value = '';
		return;
	}//alert(val + "---"+itemNamesArray.length);
	var flag = false;
	for (var m=0;m<itemNamesArray.length;m++) {
	     var item = itemNamesArray[m];//alert(val + "== "+item.item_barcode_id);
	     if (val == item.item_barcode_id ) {
	     	var itmName = item.medicine_name;
	     	var elNewItem = matches(itmName, oItemAutoComp);//alert(oAutoComp);
	     	document.forms[0].items.value = itmName;
	     	oItemAutoComp._bItemSelected = true;
//			oItemAutoComp._selectItem(elNewItem);
	     	getItemDetails ();
	     	flag = true;
	     }
	     if (flag) break;
	 }
	 if (!flag) {
	 	resetMedicineDetails();
	 	document.forms[0].items.value = '';
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
		window.open(cpath+'/stores/StockPatientIssue.do?_method=generateGatePassprintForIssue&issNo='+issueid);
	}
}

/*function getkitItemDetails(item) {
	var ajaxReqObject1 = newXMLHttpRequest();
	var url = "StockUserIssue.do?_method=getKitItemDetails&item_name="+item;
	getResponseHandlerText(ajaxReqObject1, handleKitResponse, url);
}
var kitDetails = '';
function handleKitResponse(responseText) {
	if (responseText==null) return;
	if (responseText=="") return;
    eval("kitdet = " + responseText);
    if(kitdet.length>0){
      	kitDetails = kitdet;
	}
}

function kitDetailsList() {
	var len = document.getElementById("table1").rows.length;
	for ( var p=len-1;p>=1;p--){
		document.getElementById("table1").deleteRow(p);
	}
	if (kitDetails !='') {
		toggleBox('kitdetails', 1);
		var itemListTable = document.getElementById("table1");
		if(kitDetails.length>0){
		    var id = 1;
	        for (var i=0; i<kitDetails.length; i++){
				var item = kitDetails[i];
				var row = itemListTable.insertRow(id);
				var cell;
				cell = row.insertCell(-1);
				cell.innerHTML = '<label>' + item.CATEGORY + '</label>'
				cell = row.insertCell(-1);
				cell.innerHTML = '<label>' + item.ITEM_NAME + '</label>'
				cell = row.insertCell(-1);
				cell.innerHTML = '<label>' + item.BILLABLE + '</label>'
				cell = row.insertCell(-1);
				cell.innerHTML = '<label>' + item.QTY + '</label>'
				++id;
			}
		}

	} else toggleBox('nokit', 1);
}
*/

function deleteTableRows(){
	var itemtable = document.getElementById("itemListtable");
	var rows = itemtable.rows.length-2;
	for(var i=1; i<=rows;i++){
		itemtable.deleteRow(-1);
	}

	clearAllHiddenVariables();
}
//RC: This method is not in use cleanup.
function AddGroupItemDetails(trantype){
	if(groupItemDetails == null || groupItemDetails.length == 0)
		return;
	if(trantype == "Hospital"){
	    document.stockissueform.store.value = groupStoreId;
	    type = 'Hospital';
	}else{
		document.patientissueform.store.value = groupStoreId;
		type = 'Patient';
	}
	for(var i=0;i<groupItemDetails.length;i++){
		var item_id = groupItemDetails[i].medicine_id;
		var item_name = groupItemDetails[i].medicine_name;
		var batchIndex =groupItemDetails[i].batch_no;
		var qty = groupItemDetails[i].qty;
		var item_identifier = groupItemDetails[i].batch_no;
		var stktype = groupItemDetails[i].consignment_stock;
		var pkg_size = groupItemDetails[i].issue_base_unit;
		var store_id = groupItemDetails[i].dept_id;
		var item_bill = groupItemDetails[i].billable;
		var exp_dt = [];
		var isMrkUpRate = groupItemDetails[i].is_markup_rate;
		if ((null != groupItemDetails[i].exp_dt) && (groupItemDetails[i].exp_dt != "")){
			exp_dt[groupItemDetails[i].batch_no] = groupItemDetails[i].exp_dt;
		}else{
			exp_dt[groupItemDetails[i].batch_no] = "";
		}
		pkgUOM[groupItemDetails[i].batch_no] = groupItemDetails[i].package_uom;
		issueUnits[groupItemDetails[i].batch_no] = groupItemDetails[i].issue_units;

    	item_ids[groupItemDetails[i].batch_no] = groupItemDetails[i].medicine_id;
    	identification_type[groupItemDetails[i].batch_no] = groupItemDetails[i].identification;
        stock_type[groupItemDetails[i].batch_no] = groupItemDetails[i].consignment_stock;
    	iss_type[groupItemDetails[i].batch_no] = groupItemDetails[i].issue_type;
    	pkgSize[groupItemDetails[i].batch_no] = groupItemDetails[i].issue_base_unit;
    	item_billable[groupItemDetails[i].batch_no] = groupItemDetails[i].billable;

   		if (null != document.patientissueform) {
			document.getElementById("itemMrp").value = (groupItemDetails[i].issue_rate_expr == null && groupItemDetails[i].visit_selling_expr == null && groupItemDetails[i].store_selling_expr == null) ?
							( groupItemDetails[i].selling_price == null ? parseFloat(groupItemDetails[i].orig_mrp).toFixed(decDigits) : parseFloat(groupItemDetails[i].selling_price).toFixed(decDigits) )
							: parseFloat(groupItemDetails[i].mrp).toFixed(decDigits);
			document.getElementById("unitMrp").value = groupItemDetails[i].unit_mrp;
			document.getElementById("mrp").value = groupItemDetails[i].mrp;
			document.getElementById("unit_mrp").value = groupItemDetails[i].unit_mrp;
			document.getElementById("txType").value = groupItemDetails[i].tax_type;
			document.getElementById("tax").value = groupItemDetails[i].tax_rate;
			document.getElementById("Unit").value = groupItemDetails[i].issue_base_unit;
			document.getElementById("origMRP").value = groupItemDetails[i].orig_mrp;
		}

		document.getElementById("item").checked = true;
		changeItems(item_identifier,type);
		qty = grpStoreItem_unit == 'I' ? qty : parseFloat(qty/pkg_size).toFixed(2);
		document.getElementById("dialogId").value = i+1;
		var coverdbyinsuranceflag = null;
		var billNo = document.getElementById('bill_no').value;
		var isTpaBill = getBillIsTpa(billNo);
		coverdbyinsuranceflag = document.getElementById("coverdbyinsuranceflag").value;
		var medDiscWithoutInsurance = myform.medDiscWithoutInsuranceForm.value;
        var medDiscWithInsurance = myform.medDiscWithInsuranceForm.value;
		addToInnerHTML(item_name,store_id,item_identifier,exp_dt[groupItemDetails[i].batch_no],qty,pkg_size,stktype,item_bill,"group", isMrkUpRate, grpStoreItem_unit,coverdbyinsuranceflag, medDiscWithoutInsurance, medDiscWithInsurance);
	}

}
function openDialogBox(id){
	if ( oItemAutoComp == undefined )
		return false;
	gItemType = 'itemname';
	if(document.patientissueform != undefined && document.patientissueform != null) {
		var visitId = document.patientissueform.visitId.value;
		if(visitId != undefined && visitId != '' && visitId != null) {
			document.getElementById("orderKitDetails").style.display = "none";
			document.getElementById("orderKitMissedItemsHeader").style.display = "none";
			document.getElementById("orderKitMissedItems").style.display = "none";
			document.getElementById("itemInfo").innerHTML = "";
			document.getElementById("orderkits").value = "";
			document.getElementById("itemname").checked = true;
			document.getElementById("order").checked = false;
			document.getElementById("itemDetails").style.display = "block";
			document.getElementById("order").disabled = false;
			document.getElementById("itemInfo").style.height = "10px";
		} else {
			document.getElementById("orderKitDetails").style.display = "none";
			document.getElementById("orderKitMissedItemsHeader").style.display = "none";
			document.getElementById("orderKitMissedItems").style.display = "none";
			document.getElementById("itemInfo").innerHTML = "";
			document.getElementById("orderkits").value = "";
			document.getElementById("itemname").checked = false;
			document.getElementById("order").checked = false;
			document.getElementById("itemDetails").style.display = "none";
		}
		document.getElementById("OK").disabled = false;
	}


	var button = document.getElementById("tableRow"+id);
	if (null != document.patientissueform){
		document.patientissueform.items.value = "";
		document.getElementById("pkg_size").value = "";
		document.patientissueform.oldMRP.value = "";
		document.patientissueform.issue_rate_expr.value = "";
		document.patientissueform.visit_selling_expr.value = "";
		document.patientissueform.store_selling_expr.value = "";
		document.getElementById("item_unit").value = "";
		if (id > 0){
			document.getElementById("itemMrp").value="";
			document.getElementById("unitMrp").value = "";
			document.getElementById("issuQty").value = "";
			if(allowDiscount == 'A')
			document.getElementById("discount").value = 0;
			document.getElementById("coverdbyinsuranceflag").value="";
			document.getElementById("coverdbyinsurance").innerHTML="";
			document.patientissueform.batch.length = 1;
		}
		var billNo = document.getElementById('bill_no').value;
		var isTpaBill = getBillIsTpa(billNo);
		if(!isTpaBill) {
			document.getElementById("coverdbyinsurancestatusid").style.display = "none";
		} else {
			document.getElementById("coverdbyinsurancestatusid").style.display = "";
		}
		if (groupItemDetails != null) {
			document.patientissueform.batch.options[0].text = (document.getElementById("identifierLabel"+id).textContent).trim();
			document.patientissueform.batch.options[0].value = document.getElementById("identifierLabel"+id).textContent.trim();
			document.patientissueform.batch.selectedIndex = 0;
		}
		var elNewItem = matches(document.getElementById("itemLabel"+id).textContent, oItemAutoComp);
		oItemAutoComp._selectItem(elNewItem);


		document.patientissueform.items.value = decodeURIComponent(document.getElementById("item_name"+id).value);
		gIndex = id;
		dialog.cfg.setProperty("context",[button, "tr", "br"], false);
		document.getElementById("dialogId").value = id;
		if(document.getElementById('item_identifier'+id) && identification_type[document.getElementById('item_identifier'+id).value] == 'S'){
	  		document.patientissueform.issuQty.value = '1';
	  		document.patientissueform.issuQty.readOnly = true;
		}
		dialog.show();
		setFocus ();
		getItemDetails("patientissueform",id);
		document.getElementById("isMarkUp").value = document.getElementById("isMarkUpRate"+id).value;
		document.getElementById("origMRP").value = document.getElementById("originalMRP"+id).value;

	} else{
		if (groupItemDetails != null) {
			document.stockissueform.batch.options[0].text = (document.getElementById("identifierLabel"+id).textContent).trim();
			document.stockissueform.batch.options[0].value = document.getElementById("identifierLabel"+id).textContent.trim();
			document.stockissueform.batch.selectedIndex = 0;
		}
		var elNewItem = matches(decodeURIComponent(document.getElementById("item_name"+id).value), oItemAutoComp);
		oItemAutoComp._selectItem(elNewItem);

		document.stockissueform.items.value = decodeURIComponent(document.getElementById("item_name"+id).value);
		gIndex = id;
		dialog.cfg.setProperty("context",[button, "tr", "br"], false);
		document.getElementById("dialogId").value = id;


		dialog.show();
		setFocus ();
		getItemDetails("stockissueform",id);

	}

}
function setFocus () {
	if (prefBarCode == 'Y') document.forms[0].barCodeId.focus();
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
	document.getElementById("items").value = "";
	document.forms[0].barCodeId.value = "";
	document.getElementById("issuQty").value = "";
	document.getElementById("pkg_size").value = "";
	document.forms[0].batch.length = 1;
	document.forms[0].batch.selectedIndex = 0;
}
function handleCancel() {
	dialog.cancel();
	if (showCharges == 'A'){
		document.getElementById("mrp").value = '';
		document.getElementById("cp").value = 	'';
		document.getElementById("Disc").value = '';
		document.getElementById("tax").value = '';
		document.getElementById("txType").value = '';
		document.getElementById("Unit").value = '';
	}
	resetMedicineDetails ();
	if (null != document.patientissueform)
		document.patientissueform.save.focus();
	if (null != document.stockissueform)
		document.stockissueform.save.focus();
}
function clearAllHiddenVariables(){
	if (null != document.stockissueform){
		document.stockissueform.storeId.value = "" ;
		document.stockissueform.temp_charge_id.value = "" ;
		document.stockissueform.item_name.value = "" ;
		document.stockissueform.item_identifier.value = "" ;
		document.stockissueform.exp_dt.value = "" ;
		document.stockissueform.issue_qty.value = "" ;
		document.stockissueform.stype.value = "" ;
	} else if (null != document.patientissueform){
		document.patientissueform.storeId.value = "" ;
		document.patientissueform.temp_charge_id.value = "" ;
		document.patientissueform.item_name.value = "" ;
		document.patientissueform.item_identifier.value = "" ;
		document.patientissueform.exp_dt.value = "" ;
		document.patientissueform.issue_qty.value = "" ;
		document.patientissueform.stype.value = "" ;
	}
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



function AddRowsToGrid(tabLen){
	var itemtable = document.getElementById("itemListtable");
	var tdObj="",trObj="";
	var row = "tableRow" + tabLen;
	var deleteLabel			= makeLabel('itemRow','itemRow'+tabLen,'');
	var itemLabel			= makeLabel('itemLabel','itemLabel'+tabLen,'');
	var flagImgLbl			= makeLabel('flagImg','flagImg'+tabLen,'');
	var controleTypeLabel	= makeLabel('controleTypeLabel','controleTypeLabel'+tabLen,'');
	var identifierLabel		= makeLabel('identifierLabel','identifierLabel'+tabLen,'');
	var pkgSizeLabel		= makeLabel('pkgSizeLabel','pkgSizeLabel'+tabLen,'');
	var expdtLabel			= makeLabel('expdtLabel','expdtLabel'+tabLen,'');
	var issue_qtyLabel		= makeLabel('issue_qtyLabel','issue_qtyLabel'+tabLen,'');
	var uomLabel			= makeLabel('uomLabel','uomLabel'+tabLen,'');

	if ((null != document.patientissueform) && (showCharges == 'A')) {
		var mrpLabel			= makeLabel('mrpLabel','mrpLabel'+tabLen,'');
		var unitmrpLabel        = makeLabel('unitmrpLabel', 'unitmrpLabel'+tabLen,'');
		var pkgmrpLabel        = makeLabel('pkgMRPLabel', 'pkgMRPLabel'+tabLen,'');
		var unitamtLabel        = makeLabel('totamtLabel', 'totamtLabel'+tabLen,'');
		var discountLabel      =makeLabel('discountLabel', 'discountLabel'+tabLen,'');
		var patamtLabel			= makeLabel('totpatamtLabel', 'totpatamtLabel'+tabLen, '');
		var insamtLabel			= makeLabel('pri_totinsamtLabel', 'pri_totinsamtLabel'+tabLen, '');
		var sec_insamtLabel			= makeLabel('sec_totinsamtLabel', 'sec_totinsamtLabel'+tabLen, '');
	}
	var unitMrpHid        = makeHidden('unitMrpHid', 'unitMrpHid'+tabLen,'');
	var origUnitMrpHid        = makeHidden('origUnitMrpHid', 'origUnitMrpHid'+tabLen,'');
	var mrpHid            =makeHidden('mrpHid', 'mrpHid'+tabLen,'');
	var discountHid        =makeHidden('discountHid', 'discountHid'+tabLen,'');
	var discountAmtHid        = makeHidden('discountAmtHid', 'discountAmtHid'+tabLen,'');

	var temp_charge_idHidden 	= makeHidden('temp_charge_id','temp_charge_id'+tabLen,'');
	var item_nameHidden 	= makeHidden('item_name','item_name'+tabLen,'');
	var storeIdHidden 		= makeHidden('storeId','storeId'+tabLen,'');
	var item_identifierHidden= makeHidden('item_identifier','item_identifier'+tabLen,'');
	var itemBatchId = makeHidden('itemBatchId','itemBatchId'+tabLen,'');
	var exp_dtHidden 		= makeHidden('exp_dt','exp_dt'+tabLen,'');
	var stypeHidden 		= makeHidden('stype','stype'+tabLen,'');
	var issue_qtyHidden		= makeHidden('issue_qty','issue_qty'+tabLen,'');
	var item_billable_hid 	= makeHidden('item_billable_hidden','item_billable_hidden'+tabLen,'');
	var hdeletedHidden 		= makeHidden('hdeleted','hdeleted'+tabLen,'');

	var pkgmrp_hidden 		=makeHidden('pkgmrp','pkgmrp'+tabLen,'');
	var medDisc_hidden 		=makeHidden('medDisc','medDisc'+tabLen,'');
	var taxPer_hidden 		=makeHidden('taxPer','taxPer'+tabLen,'');
	var pkgUnit_hidden 		=makeHidden('pkgUnit','pkgUnit'+tabLen,'');
	var taxType_hidden 		=makeHidden('taxType','taxType'+tabLen,'');
	var medDiscRS_hidden 	=makeHidden('medDiscRS','medDiscRS'+tabLen,'');
	var amt_hidden 			=makeHidden('amt','amt'+tabLen,'');
	var category_hidden 			=makeHidden('category','category'+tabLen,'');
	var insurance_category_hidden 	=makeHidden('insurancecategory','insurancecategory'+tabLen,'');
	var first_of_category           =makeHidden('firstOfCategory','firstOfCategory'+tabLen, '');
	var patInsClaimAmtHidden        =makeHidden('pri_patIncClaimAmt','pri_patIncClaimAmt'+tabLen, '');
	var sec_patInsClaimAmtHidden        =makeHidden('sec_patIncClaimAmt','sec_patIncClaimAmt'+tabLen, '');
	var isMrkUpRate        =makeHidden('isMarkUpRate','isMarkUpRate'+tabLen, '');
	var originalMRP        =makeHidden('originalMRP','originalMRP'+tabLen, '');
	var itemUnit        =makeHidden('itemUnit','itemUnit'+tabLen, '');
	var indentItemId        =makeHidden('indent_item_id','indent_item_id'+tabLen, '');
	var patIndentNo        =makeHidden('patient_indent_no','patient_indent_no'+tabLen, '');
	var catPayable        =makeHidden('cat_payable','cat_payable'+tabLen, '');
	var medDiscWithoutInsurance        =makeHidden('medDiscWithoutInsurance','medDiscWithoutInsurance'+tabLen, '');
    var medDiscWithInsurance        =makeHidden('medDiscWithInsurance','medDiscWithInsurance'+tabLen, '');
    var issueRateExpr        =makeHidden('issueRateExpr','issueRateExpr'+tabLen, '');
    var visitSellingPriceExpr        =makeHidden('visitSellingPriceExpr','visitSellingPriceExpr'+tabLen, '');
    var storeSellingPriceExpr        =makeHidden('storeSellingPriceExpr','storeSellingPriceExpr'+tabLen, '');
    var medicineId        =makeHidden('medicineId','medicineId'+tabLen, '');

	var buton = makeButton1("addBut", "addBut"+tabLen);
	buton.setAttribute("class", "imgButton");
	buton.setAttribute("onclick","openDialogBox('"+tabLen+"'); return false;");
	buton.setAttribute("title", "Add New Item (Alt_Shift_+)");
	buton.setAttribute("accesskey", "+");
	var itemrowbtn = makeImageButton('add','add'+tabLen,'imgAdd',cpath+'/icons/Add.png');
	buton.appendChild(itemrowbtn);
	trObj = itemtable.insertRow(tabLen);
	trObj.id = row;

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(flagImgLbl);
	tdObj.appendChild(itemLabel);
	tdObj.appendChild(temp_charge_idHidden);
	tdObj.appendChild(item_nameHidden);
	tdObj.appendChild(storeIdHidden);
	tdObj.appendChild(item_billable_hid);

	tdObj.appendChild(pkgmrp_hidden);
	tdObj.appendChild(medDisc_hidden);
	tdObj.appendChild(taxPer_hidden);
	tdObj.appendChild(pkgUnit_hidden);
	tdObj.appendChild(taxType_hidden);
	tdObj.appendChild(medDiscRS_hidden);
	tdObj.appendChild(amt_hidden);
	tdObj.appendChild(category_hidden);
	tdObj.appendChild(insurance_category_hidden);
	tdObj.appendChild(first_of_category);
	tdObj.appendChild(patInsClaimAmtHidden);
	tdObj.appendChild(sec_patInsClaimAmtHidden);
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
    tdObj.appendChild(medicineId);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(controleTypeLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(identifierLabel);
	tdObj.appendChild(item_identifierHidden);
	tdObj.appendChild(itemBatchId);
	tdObj.appendChild(exp_dtHidden);
	tdObj.appendChild(stypeHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(expdtLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(issue_qtyLabel);
	tdObj.appendChild(issue_qtyHidden);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(uomLabel);

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(pkgSizeLabel);

	if ((null != document.patientissueform) && (showCharges == 'A')){
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
		tdObj.appendChild(patamtLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(insamtLabel);
		tdObj = trObj.insertCell(-1);
		tdObj.appendChild(sec_insamtLabel);
	}

	tdObj = trObj.insertCell(-1);
	tdObj.appendChild(deleteLabel);
	tdObj.appendChild(hdeletedHidden);
	tdObj.appendChild(mrpHid);
	tdObj.appendChild(unitMrpHid);
	tdObj.appendChild(origUnitMrpHid);
	tdObj.appendChild(discountHid);
	tdObj.appendChild(discountAmtHid);

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

function checkstoreallocation() {
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
		getMedicinesForStore(storeObj, setItems)
	}
}

function reCalcRowAmounts(rowObj) {
	var pkgUnit = getElementAmount(document.getElementById("pkgUnit"+rowObj));

	// these are user inputs, editable
	var mrpPaise = getElementPaise(document.getElementById('mrpHid'+rowObj));
	var origMrpPaise = getElementPaise(document.getElementById('originalMRP'+rowObj));
	var qty = getElementAmount(document.getElementById('issue_qty'+rowObj));
	var discountPer = getElementAmount(document.getElementById('discountHid'+rowObj));
	if (discountPer > 100){
		discountPer = 100.00;
	}

	/*
	 * Calculations follow. Note that we only use unit rates unlike sales screen. This
	 * means that if package MRP/Rate is not a multiple of 10 paise, then, we can end
	 * up with amount not equal to package MRP when selling a whole package. This is
	 * intentional, so as to keep the billing entry rate*qty=amount intact.
	 */
	var pkgUnit = getElementAmount(document.getElementById("pkgUnit"+rowObj));
	var unitRatePaise = Math.round(mrpPaise / pkgUnit);
	var origUnitRatePaise = Math.round(origMrpPaise / pkgUnit);
	var amtPaise = unitRatePaise * qty;
	var discountPaise = Math.round(amtPaise * discountPer / 100);
	var netAmtPaise = amtPaise - discountPaise;

	document.getElementById("origUnitMrpHid"+rowObj).value = formatAmountPaise(origUnitRatePaise);
	document.getElementById("unitMrpHid"+rowObj).value = formatAmountPaise(unitRatePaise);
	document.getElementById("discountAmtHid"+rowObj).value = formatAmountPaise(discountPaise);
	document.getElementById("amt"+rowObj).value = formatAmountPaise(netAmtPaise);

	if (showCharges == 'A') {
		document.getElementById("totamtLabel"+rowObj).textContent = formatAmountPaise(netAmtPaise);
		document.getElementById("discountLabel"+rowObj).textContent = formatAmountPaise(discountPaise);
	}

	isBillSponsor (rowObj);
}
function getBillIsTpa(billNo){
	var isTpa = false;
	if (null != getPatientInfo) {
		var visitBills = getPatientInfo.bills;
		for(var i = 0;i < visitBills.length ; i++){
			isTpa = visitBills[i].bill_no == billNo && visitBills[i].is_tpa;
			if ( isTpa ) break;
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
		var ajaxReqObject = newXMLHttpRequest();
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

function isInsurence (rowObj) {

	//need to get whether this item is claimable.
	var itemName = document.getElementById("item_name"+rowObj).value;
	var billNo = document.getElementById('bill_no').value;
	var planId = document.getElementById("planId").value;
	var billNo = document.getElementById("bill_no").value;
	var insuranceAppl = "N";

	var url;
	var ajaxReqObject = newXMLHttpRequest();
	//AJAX call to get whether this med. is claimable. If TPA is set, and category of item is claimable, the amt is claimable
	url = "StockPatientIssue.do?_method=isSponsorBill&medName="+itemName+"&billNo="+billNo;
	ajaxReqObject.open("POST",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				insuranceAppl = eval(ajaxReqObject.responseText);
		}
	}
	return insuranceAppl;
}

function onChangeBill () {
	reCalculateInsurAmt ();
	var billNo = document.getElementById('bill_no').value;
	var isTpaBill = getBillIsTpa(billNo);
	if(isTpaBill)
		onClickProcessInsForIssues('patientissueform');
}

function setcorrectAmt(discount,qty,rate,i) {
    var netAmt = rate * qty;
    var dis = (discount * netAmt)/100.00;
    var finalAmt = netAmt - dis;
   /* document.getElementById("discountAmtHid"+i).value = dis;
    document.getElementById("discountLabel"+i).textContent = dis;
    document.getElementById("totamtLabel"+i).textContent = finalAmt;
    document.getElementById("amt"+i).value = finalAmt;*/
    document.getElementById("discountAmtHid"+i).value = parseFloat(dis).toFixed(decDigits);
    document.getElementById("discountLabel"+i).textContent = parseFloat(dis).toFixed(decDigits);
    document.getElementById("totamtLabel"+i).textContent = parseFloat(finalAmt).toFixed(decDigits);
    document.getElementById("amt"+i).value = parseFloat(finalAmt).toFixed(decDigits);
}

function reCalculateInsurAmt () {
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

        var medDiscWithInsurance = 0.00;
        var medDiscWithoutInsurance = 0.00;
        if(document.getElementById("medDiscWithInsurance"+i) != null && document.getElementById("medDiscWithInsurance"+i).value !='') {
        	medDiscWithInsurance = document.getElementById("medDiscWithInsurance"+i).value;
        } else {
        	medDiscWithInsurance = document.getElementById("medDiscWithoutInsurance"+i).value;
        }

        if(document.getElementById("medDiscWithoutInsurance"+i) != null && document.getElementById("medDiscWithoutInsurance"+i).value != '' )
            medDiscWithoutInsurance = document.getElementById("medDiscWithoutInsurance"+i).value;

        if(isTpaBill && medDiscWithInsurance != null && medDiscWithInsurance != undefined && medDiscWithInsurance != '') {
            discount = medDiscWithInsurance;
        } else if(medDiscWithoutInsurance!= null && medDiscWithoutInsurance != undefined && medDiscWithoutInsurance != ''){
            discount = medDiscWithoutInsurance;
        }

        var qty = document.getElementById("issue_qty"+i).value;
        var rate = document.getElementById("mrpLabel"+i).textContent;
        var unitRate = document.getElementById("unitmrpLabel"+i).textContent;
        var pkgUnit = getElementAmount(document.getElementById("pkgUnit"+i));
        var itemUnit = document.getElementById("itemUnit"+i).value;
        var medicineId = document.getElementById("medicineId"+i).value;
        if(itemUnit == 'I') {
        	rate = unitRate;
        }  else {
        	qty = Math.ceil(qty / pkgUnit);
        }
        setSellingPrice(null, qty, rate, medicineId, discount, patient, i);
        rate = getFieldValue("pkgmrp", i);
        setcorrectAmt(discount,qty,rate,i);
		isBillSponsor (i);
	}
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
		document.getElementById("pri_patIncClaimAmt"+rowObj).value = parseFloat(priClaimAmt).toFixed(decDigits);
		document.getElementById("sec_patIncClaimAmt"+rowObj).value = parseFloat(secClaimAmt).toFixed(decDigits);
		//resetTotals();
	//}
	if (showCharges == 'A') resetTotals();
}

function resetTotals(){
	var totalAmountPaise = 0;
	var totalPatAmtPaise = 0;
	var totalClaimAmtPaise = 0;
	var itemTable = document.getElementById("itemListtable");
	var tabLen = itemTable.rows.length;
	for (var i=1;i<=tabLen-1;i++){
		if ((document.getElementById('hdeleted'+i).value != 'true') && (document.getElementById('item_billable_hidden'+i).value == 'true')){
			totalAmountPaise += getLPaise(document.getElementById('totamtLabel'+i));
			totalPatAmtPaise += getLPaise(document.getElementById("totpatamtLabel"+i));
			totalClaimAmtPaise += getLPaise(document.getElementById("pri_totinsamtLabel"+i));
			totalClaimAmtPaise += getLPaise(document.getElementById("sec_totinsamtLabel"+i));
		}
	}
	if(showCharges == 'A'){
		document.getElementById("totAmt").textContent = formatAmountValue(getPaiseReverse(totalAmountPaise));
		document.getElementById("totPatAmt").textContent = formatAmountValue(getPaiseReverse(totalPatAmtPaise));
		document.getElementById("totClaimAmt").textContent = formatAmountValue(getPaiseReverse(totalClaimAmtPaise));
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
	var patientInfo = getPatDetails(patient.mrNo);
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
	var reqObject = newXMLHttpRequest();
	var storeId = document.getElementById("store").value;
	var saleType="";
	var patientId = patient.patient_id;

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
	var thisresp = responseText;
	var itemUntObj = null;
	if (fillItemDetails_common(thisresp)){
		if (null != document.patientissueform){
			if (document.getElementById("items").value == decodeURIComponent(document.getElementById("item_name"+gIndex).value) ){
				if (itemdetails.length > 1){
					setSelectedIndex(document.patientissueform.batch,document.getElementById("identifierLabel"+gIndex).textContent);
				}
				if(showCharges == 'A')  {
					if (document.getElementById("mrpLabel"+gIndex).textContent != ''){
						document.patientissueform.itemMrp.value = parseFloat(document.getElementById("mrpLabel"+gIndex).textContent).toFixed(decDigits);
						document.patientissueform.unitMrp.value = document.getElementById("unitmrpLabel"+gIndex).textContent;
						document.patientissueform.Unit.value = document.getElementById("pkgUnit"+gIndex).value;
						document.getElementById("origRate").value =document.getElementById("mrpLabel"+gIndex).textContent;
						document.patientissueform.origMRP.value = document.getElementById("originalMRP"+gIndex).value;
					}
			 	}else {
			 		document.getElementById("itemMrp").value = itemMrp[document.patientissueform.batch.value];
			 		document.getElementById("origRate").value = itemMrp[document.patientissueform.batch.value];
			 	}
			    document.patientissueform.issuQty.value = document.getElementById("issue_qtyLabel"+gIndex).textContent;
			    if(document.patientissueform.issuQty.value != undefined && document.patientissueform.issuQty.value != null) {
			    	document.getElementById("itemname").checked = true;
			    	document.getElementById("order").checked = false;
			    	document.getElementById("order").disabled = true;
			    }
				if(showCharges == 'A') {
					if(document.getElementById("mrpLabel"+gIndex).textContent != ''){
				 		document.patientissueform.discount.value = document.getElementById('discountHid'+gIndex).value;
				 	}
				} else {
					document.patientissueform.discount.value = document.getElementById('discountHid'+gIndex).value;
				}
			}

			eval("items = " + responseText);
			 if (items.length > 0) {
				document.patientissueform.isMarkUp.value = items[0].is_markup_rate;
				document.patientissueform.control_type_id.value = items[0].control_type_id;
				document.patientissueform.control_type_name.value = items[0].control_type_name;
				document.patientissueform.medicine_id.value = document.getElementById("medicineId"+gIndex).value;
				document.getElementById("issue_rate_expr").value = document.getElementById("issueRateExpr"+gIndex).value;
				document.getElementById("visit_selling_expr").value = document.getElementById("visitSellingPriceExpr"+gIndex).value;
				document.getElementById("store_selling_expr").value = document.getElementById("storeSellingPriceExpr"+gIndex).value;
				if((!isNotNullValue("issue_rate_expr")) || (!isNotNullValue("visit_selling_expr")) || (!isNotNullValue("store_selling_expr"))) {
					document.patientissueform.issue_rate_expr.value = items[0].issue_rate_expr;
					document.patientissueform.visit_selling_expr.value = items[0].visit_selling_expr;
					document.patientissueform.store_selling_expr.value = items[0].store_selling_expr;
					document.patientissueform.medicine_id.value = items[0].medicine_id;
				}
				document.getElementById("item_batch_id").value = itemBatchId[document.patientissueform.batch.value];
			}

			var catPayableInfo = getCatPayableStatus(items[0].medicine_id, false);
			var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";
			document.patientissueform.priCatPayable.value = priCatPayable;

			var dis = document.getElementById('discount').value;
			itemUntObj = document.patientissueform.item_unit;

			if(document.getElementById('addBut'+gIndex).title != 'Edit Item')
				document.patientissueform.discount.value = parseFloat(dis) + gDefaultDiscountPer;
			else
				document.patientissueform.discount.value = parseFloat(dis);

			document.patientissueform.medDiscWithoutInsuranceForm.value = document.patientissueform.discount.value;
            document.patientissueform.medDiscWithInsuranceForm.value = '';

			itemUntObj = document.patientissueform.item_unit;

			var billNo = document.getElementById('bill_no').value;
	        var isTpaBill = getBillIsTpa(billNo);
			if(document.getElementById("medDiscWithoutInsurance"+gIndex) != null
					&& document.getElementById("medDiscWithoutInsurance"+gIndex).value != '') {
				document.patientissueform.medDiscWithInsuranceForm.value = document.getElementById('medDiscWithInsurance'+gIndex).value;
				document.patientissueform.medDiscWithoutInsuranceForm.value = document.getElementById('medDiscWithoutInsurance'+gIndex).value;
				if(isTpaBill && priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y' && document.patientissueform.medDiscWithInsuranceForm.value != '') {
					document.patientissueform.discount.value = document.patientissueform.medDiscWithInsuranceForm.value;
				} else {
					document.patientissueform.discount.value = document.patientissueform.medDiscWithoutInsuranceForm.value;
				}

				 if(document.patientissueform.discount.value == '') {
		            	document.patientissueform.discount.value = 0;
		         }

			}  else {
				//apply insurance discount
	            var discountPlanId = document.getElementById("discountPlanId").title;
	            var insuranceCategoryId = items[0].insurance_category_id;

	            if(document.getElementById('addBut'+gIndex).title != 'Edit Item' && priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y') {
	            	var discountPlanDetailsJSON = JSON.parse(discountPlansJSON);
	                if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {
	                    for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
	                        var item = discountPlanDetailsJSON[j];
	                        if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountPlanId && insuranceCategoryId == item.applicable_to_id ) {
	                        	document.patientissueform.medDiscWithInsuranceForm.value = item["discount_value"];
	                            //document.patientissueform.discount.value =  item["discount_value"];
	                            break;
	                        }
	                    }
	                }
	            }
	            if(document.getElementById('addBut'+gIndex).title != 'Edit Item'  && isTpaBill  && priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y' ) {
	                if(document.patientissueform.medDiscWithInsuranceForm.value == '') {
	                	document.patientissueform.discount.value = document.patientissueform.medDiscWithoutInsuranceForm.value;
	                } else {
	                	document.patientissueform.discount.value = document.patientissueform.medDiscWithInsuranceForm.value;
	                }
	            }

	            if(document.patientissueform.discount.value == '') {
	            	document.patientissueform.discount.value = 0;
	            }
			}

		}else{
			if (itemdetails.length > 1){
				setSelectedIndex(document.stockissueform.batch,document.getElementById("identifierLabel"+gIndex).textContent);

			}
			document.stockissueform.Unit.value = document.getElementById("pkgSizeLabel"+gIndex).value;
			if ( !(itemdetails.length == 1 && identification_type[document.stockissueform.batch.value] == 'S'))
				document.stockissueform.issuQty.value = document.getElementById("issue_qtyLabel"+gIndex).textContent;

			itemUntObj = document.stockissueform.item_unit;
		}

		var itemIdent = null != document.patientissueform ? document.patientissueform.batch.value : document.stockissueform.batch.value;
		var btch = {packageSize: pkgSize[itemIdent], issueUnits: issueUnits[itemIdent], packageUOM: pkgUOM[itemIdent]};
		var storeId = document.getElementById("store").value;
		if(document.getElementById("itemUnit"+gIndex).value != '' && document.getElementById("itemUnit"+gIndex).value != undefined)
			document.getElementById("item_unit").value = document.getElementById("itemUnit"+gIndex).value;
		else {
			if(document.getElementById("item_unit").options.length > 1) {
				document.getElementById("item_unit").value = gStoreSaleUnit;
			} else {
				document.getElementById("item_unit").value = document.getElementById("item_unit").options[0].value;
			}
		}

		document.getElementById("Unit").value = pkgSize[itemIdent];
		if(catPayableInfo!= null && catPayableInfo != undefined ) {
			var calimbaleInfo = catPayableInfo.plan_category_payable != null? catPayableInfo.plan_category_payable:"";
			if(calimbaleInfo == 'f') {
				document.getElementById('coverdbyinsurance').innerHTML = "No";
				document.getElementById('coverdbyinsurance').style.color = "red";
			} else {
				document.getElementById('coverdbyinsurance').innerHTML = "Yes";
				document.getElementById('coverdbyinsurance').style.color = "#666666";
			}
			document.getElementById('coverdbyinsuranceflag').value = calimbaleInfo;
		}
		if(document.patientissueform != undefined) {
			if(items != undefined)
				getIssueItemPriceUsingExpr(items[0].medicine_id);

		}

	}
}

function onChangeDiscount(obj) {
	var billNo = document.getElementById('bill_no').value;
	var medicineId = document.getElementById("medicineId").value;
	getIssueItemPriceUsingExpr(medicineId);
    var isTpaBill = getBillIsTpa(billNo);
    if(document.patientissueform.discount.value == undefined || document.patientissueform.discount.value == null || document.patientissueform.discount.value == '') {
        document.patientissueform.discount.value = '0';
    }
    if(isTpaBill && (document.patientissueform.priCatPayable.value != null &&
    		document.patientissueform.priCatPayable.value!= undefined &&
    		document.patientissueform.priCatPayable.value == 'Y')) {
    	document.patientissueform.medDiscWithInsuranceForm.value = obj.value;
    } else {
    	document.patientissueform.medDiscWithoutInsuranceForm.value = obj.value;
    }

}
function matches(mName, autocomplete) {
	var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
   	elListItem._sResultMatch = mName;
    elListItem._oResultData = mName;
	return elListItem;
}

function calUnitMrp() {
	var edited_rate = document.getElementById("itemMrp").value;
	document.getElementById("issue_rate_expr").value = edited_rate;
	setIssueItemRates(edited_rate);
	var pkg_size = document.getElementById("pkg_size").value;
	document.getElementById("unitMrp").value = parseFloat(edited_rate / pkg_size).toFixed(decDigits);
	return true;
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
	document.getElementById('prescFieldSet').style.display = 'none';
	document.getElementById('prescDetailsDiv').style.display="none";

}

function validateIssueDate(){
	var issueDate  = getDate("issueDate");

	var issueTime = document.getElementById("issueTime");
	if (!validateTime(issueTime))
		return false;
	var issueDateObj = document.patientissueform.issueDate;

	var regDate = new Date(patient.reg_date);
	var issueDateTime = getDateTimeFromField(issueDateObj,issueTime);
	var regDateTime = getDateTime(formatDate(new Date(patient.reg_date)),formatTime(new Date(patient.reg_time)) );

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
	return ( getPatientInfo != null && !empty(getPatientInfo.patient_plan_details) && getPatientInfo.patient_plan_details.length > 1 );
}

function visitWithPlan(){
	return ( getPatientInfo != null && !empty(getPatientInfo.patient_plan_details) && getPatientInfo.patient_plan_details.length > 0 );
}

function getpatPlanDetails(){
	return ( getPatientInfo.patient_plan_details );
}

function calculateClaimAmount(planId,amt,visitType,categoryId, firstOfCategory,discount){
	var claimAmount = 0;
	var ajaxReqObject = newXMLHttpRequest();
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
	var visitBills = getPatientInfo.bills;
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
function getCatPayableStatus(medicineId, asyncStatus) {
	var catPayableStatus = "";
	var ajaxReqObjectCall = newXMLHttpRequest();
	var visitId = document.getElementsByName("visitId")[0].value;
	var visitType = document.getElementsByName("visitType")[0].value;
	var url = "StockPatientIssue.do?_method=getInsuranceCategoryPayableStatus" +
		"&visitId="+visitId +
		"&medicineId="+medicineId+
		"&visitType="+visitType;
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
			var ajaxReqObject = newXMLHttpRequest();
			var url = cpath+'/issues/getmarkuprate.json?';
			var param = 'item_batch_id='+itemBatchId+"&storeId="+document.getElementById("store").value+"&qty="+qty+
							"&medicine_id="+medicine_id+"&bed_type="+gBedType+"&discount="+discount+"&patient_id="+patient.patient_id;
			if(isNotNullObj(patient.store_rate_plan_id)){
				param += "&visitStoreRatePlanId="+patient.store_rate_plan_id;
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
					setIssueItemRates(issueRateBean.sellingPriceBean.mrp);
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
	setFieldValue("origRate", itemRate.toFixed(2));
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
	    			document.getElementById("cp").value = 	'';
	    			document.getElementById("Disc").value = '';
	    			document.getElementById("tax").value = '';
	    			document.getElementById("txType").value = '';
	    			document.getElementById("Unit").value = '';
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
		var ajaxReqObject = newXMLHttpRequest();
		var planId = patient.plan_id;
		var visitId =  patient.visit_id;
		var ratePlanId = patient.org_id;
		var storeRatePlanId = patient.store_rate_plan_id;
		var visitType = patient.visit_type;

		var url = 'StockPatientIssue.do?_method=getOrderKitItemsJSON&order_kit_id='+orderKitId+
		"&storeId="+document.getElementById("store").value+"&planId="+planId+"&visitId="+visitId+"&ratePlanId="+ratePlanId+
		"&storeRatePlanId="+storeRatePlanId+"&visitType="+visitType+"&billable=Y&issueType=CR";
		ajaxReqObject.open("POST",url.toString(), false);
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
	if(parseFloat(totalItemsStatusArray[0]) == parseFloat(totalItemsStatusArray[1])) {
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
 * @param issueUnits
 * @returns
 */
function addOrderKitItems(medicineId, userQty, dialogId, billable, issueUnits) {

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
		exp_dt[batch.batch_no] = batch.exp_dt;

		// if batch is already in grid, skip
	    var dupExists = getDuplicateIndex(batch, -1);
		if (dupExists != -1)
			continue;


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
						break;
					}
				}
			}
			addOrderKitItemsToGrid(medicineId, medicineName, batch, issueUnits, qtyForBatch);
		}


		// if no more to be added finish up
		remQty = remQty - qtyForBatch;
		if (remQty == 0)
			break;
	}

	var msg = "";
	if (remQty > 0) {
		msg= getString("js.sales.issues.storesuserissues.warning.insufficientquantity");
		msg+= medicineName;
		msg+= " : " ;
		msg+= remQty.toFixed(2);
		alert(msg);
		if ((expiredQty > 0) && !gAllowExpiredSale) {
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
		msg += " : ";
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
		document.getElementById('coverdbyinsuranceflag').value = calimbaleInfo;
		document.getElementById("cat_payable"+rowsLength).value = calimbaleInfo;
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

    controlTypeName = batch.control_type_name;
    if (controlTypeName != 'Normal')
		document.getElementById("controleTypeLabel"+rowsLength).innerHTML = "<font color='blue'>"+controlTypeName+"</font>";
	else
		setNodeText(document.getElementById("controleTypeLabel"+rowsLength), controlTypeName, 25);

    document.getElementById("identifierLabel"+rowsLength).textContent = batch.batch_no;
    document.getElementById("expdtLabel"+rowsLength).textContent = formatExpiry(batch.exp_dt);
    document.getElementById("issue_qtyLabel"+rowsLength).textContent = ( batch.qty == 1 ? batch.qty : qtyForBatch );//serial item can have one item per batch
    document.getElementById("uomLabel"+rowsLength).textContent = issue_units;
    document.getElementById("pkgSizeLabel"+rowsLength).textContent = batch.issue_base_unit;
    document.getElementById("itemBatchId"+rowsLength).value = batch.item_batch_id;
    document.getElementById("issueRateExpr"+rowsLength).value = batch.issue_rate_expr;
    document.getElementById("visitSellingPriceExpr"+rowsLength).value = batch.visit_selling_expr;
    document.getElementById("storeSellingPriceExpr"+rowsLength).value = batch.store_selling_expr;


    if (showCharges == 'A') {
	   	document.getElementById("mrpLabel"+rowsLength).textContent = itemRate;
	    document.getElementById("unitmrpLabel"+rowsLength).textContent = parseFloat(itemRate/batch.issue_base_unit).toFixed(2);
	    document.getElementById("pkgMRPLabel"+rowsLength).textContent = batch.mrp;
	    document.getElementById("discountLabel"+rowsLength).textContent = batch.meddisc;
    }

    var imgbutton = makeImageButton('itemCheck','itemCheck'+rowsLength,'imgDelete',cpath+'/icons/Delete.png');
	imgbutton.setAttribute('onclick','cancelRow(this.id,itemRow'+rowsLength+','+rowsLength+')');

	var editButton = document.getElementById("add"+rowsLength);
	var eBut =  document.getElementById("addBut"+rowsLength);
	editButton.setAttribute("src",popurl+'/icons/Edit.png');
	eBut.setAttribute("title", "Edit Item");
	eBut.setAttribute("accesskey", "");
	document.getElementById("itemRow"+rowsLength).appendChild(imgbutton);

	document.getElementById("temp_charge_id"+rowsLength).value = "_"+rowsLength;
	document.getElementById("item_name"+rowsLength).value = encodeURIComponent(medicineName);
	document.getElementById("storeId"+rowsLength).value = document.getElementById("store").value;
	document.getElementById("pkgmrp"+rowsLength).value = batch.mrp;
	document.getElementById("medDisc"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);

	document.getElementById("taxPer"+rowsLength).value = batch.tax;
	document.getElementById("pkgUnit"+rowsLength).value = batch.issue_base_unit;
	document.getElementById("taxType"+rowsLength).value = batch.tax_type;
	document.getElementById("category"+rowsLength).value = batch.category_id;
	document.getElementById("insurancecategory"+rowsLength).value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;
	document.getElementById("isMarkUpRate"+rowsLength).value = '';
	document.getElementById("originalMRP"+rowsLength).value = batch.mrp;
	document.getElementById("itemUnit"+rowsLength).value = 'I';//indent process always in issue_units
	document.getElementById("item_identifier"+rowsLength).value = batch.batch_no;
	document.getElementById("exp_dt"+rowsLength).value = batch.exp_dt;
	document.getElementById("stype"+rowsLength).value = document.forms[0].stocktype.value;
	document.getElementById("issue_qty"+rowsLength).value = ( batch.qty == 1 ? batch.qty : qtyForBatch );
	document.getElementById("mrpHid"+rowsLength).value = itemRate;
	document.getElementById("unitMrpHid"+rowsLength).value = itemRate/batch.issue_base_unit;
	document.getElementById("origUnitMrpHid"+rowsLength).value = itemRate/batch.issue_base_unit;
	document.getElementById("discountHid"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById("hdeleted"+rowsLength).value = false;
	document.getElementById("item_billable_hidden"+rowsLength).value = batch.billable;
	document.getElementById("patper").value = batch.patient_percent;
	document.getElementById("patcatamt").value = batch.patient_amount_per_category;
	document.getElementById("patcap").value = batch.patient_amount_cap;
	document.getElementById("insuranceCategoryId").value = batch.insurance_category_id == null ? 0 : batch.insurance_category_id;

	document.getElementById("firstOfCategory"+rowsLength).value = document.getElementById("isFirstOfCategory").value;

	document.getElementById( "medDiscWithoutInsurance"+rowsLength).value = (parseFloat(batch.meddisc)+gDefaultDiscountPer).toFixed(decDigits);
	document.getElementById( "medDiscWithInsurance"+rowsLength).value = '';

	var discountPlanId = document.getElementById("discountPlanId").title;
	var insuranceCategoryId = batch.insurance_category_id;
	var priCatPayable = catPayableInfo.pri_cat_payable != null? catPayableInfo.pri_cat_payable: "";

	if(priCatPayable != null && priCatPayable != undefined && priCatPayable == 'Y') {
		var discountPlanDetailsJSON = JSON.parse(discountPlansJSON);
		if( discountPlanDetailsJSON  && discountPlanDetailsJSON.length > 0) {
			for (var j=0 ; j< discountPlanDetailsJSON.length; j++) {
				var item = discountPlanDetailsJSON[j];
				if ( item.applicable_type == 'N' &&  item.discount_plan_id == discountPlanId && insuranceCategoryId == item.applicable_to_id ) {
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
    document.getElementById("discountHid"+rowsLength).value = discount;
    setSellingPrice(batch, qtyForBatch, itemRate, medicineId, discount, patient, rowsLength);
    reCalcRowAmounts(rowsLength);
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
    if (null != getPatientInfo) {
    	var visitBills = getPatientInfo.bills;
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
    	issueRateExpr =  getFieldValue("issueRateExpr", id);
    	visitSellingPriceExpr = getFieldValue("visitSellingPriceExpr", id);
    	storeSellingPriceExpr = getFieldValue("storeSellingPriceExpr", id);
    	batchId = getFieldValue("itemBatchId", id);
    }

    if ((isNotNullObj(issueRateExpr)) || (isNotNullObj(visitSellingPriceExpr)) || (isNotNullObj(storeSellingPriceExpr))) {
    	var ajaxReqObject = newXMLHttpRequest();
    	var qty = qtyForBatch;
    	if (qty == undefined || qty == 0 || qty == '') {
			qty = 1;
		}
    	var url = cpath+'/issues/getmarkuprate.json?';
    	var param = 'item_batch_id='+batchId+
						"&storeId="+document.getElementById("store").value+"&qty="+qty+
						"&medicine_id="+medicineId+"&bed_type="+gBedType+"&discount="+discount+"&patient_id="+patient.patient_id;
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
				//itemRate = eval(ajaxReqObject.responseText.mrp);
				eval("issueRateBean = " + ajaxReqObject.responseText);
				itemRate = issueRateBean.sellingPriceBean.mrp;

				setFieldValue("pkgmrp", itemRate.toFixed(2), id);
				setFieldValue("mrpHid", itemRate.toFixed(2), id);
				setFieldValue("originalMRP", itemRate.toFixed(2), id);
				setFieldText("mrpLabel", itemRate.toFixed(2), id);
				setFieldText("unitmrpLabel", (itemRate/getFieldValue("pkgUnit", id)).toFixed(2), id);
				setFieldText("pkgMRPLabel", itemRate.toFixed(2), id);
			}
		}
    }
}
function fillUsers(field){
	var usersArray = [];
	if(hospuserlist != null ){
		usersArray.length = hospuserlist.length;
		for(var k =0;k<hospuserlist.length;k++){
			usersArray[k] = hospuserlist[k].hosp_user_name;
		}
	this.ousersSCDS = new YAHOO.widget.DS_JSArray(usersArray);
	ouserAutoComp = new YAHOO.widget.AutoComplete(field,'hosp_user_dropdown', this.ousersSCDS);
	ouserAutoComp.prehightlightClassName = "yui-ac-prehighlight";
	ouserAutoComp.typeAhead = false;
	ouserAutoComp.useShadow = false;
	ouserAutoComp.allowBrowserAutocomplete = false;
	ouserAutoComp.minQueryLength = 0;
	ouserAutoComp.forceSelection = true;
	ouserAutoComp.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get(field).value;
		if(sInputValue.length === 0) {
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		} });
	}
}
function refreshForm(){
	document.getElementById("refresh").value = true;
	var itemstable = document.getElementById("itemListtable");
	var tablelength = itemstable.rows.length-1;
	
	if(tablelength > 0){
		for(var i =tablelength;i>0;i--){
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
var gId = null;
var gFrom = null;
function getItemDetails(from, id){

	var item = YAHOO.util.Dom.get('items').value;
	var storeid = document.getElementById("store").value;
	gId = id;
	gFrom = from;
	var url;
	if(item != ''){

		var medDetails = findInList(itemNamesArray, 'medicine_name', item);

		var ajaxReqObject = newXMLHttpRequest();
		if(document.forms[0].name == 'stockissueform') {
			setUOMOptions(document.stockissueform.item_unit,medDetails);//set item UOM options
			document.getElementById("pkg_size").value = medDetails.issue_base_unit;
			document.stockissueform.issue_base_unit.value = medDetails.issue_base_unit;

			//get item stock details
			url = "StockUserIssue.do?_method=getItemDetails&item_name="+encodeURIComponent(item)+"&storeid="+storeid;
		}else if (document.forms[0].name == 'patientissueform'){
			setUOMOptions(document.patientissueform.item_unit,medDetails);//set item UOM options
			document.getElementById("pkg_size").value = medDetails.issue_base_unit;
			document.patientissueform.issue_base_unit.value = medDetails.issue_base_unit;

			//pass patient's plan id if available. This will help us get the patient amounts in case of insurance patients
			var planId = document.getElementById("planId").value;
			var visitType = document.getElementById("visitType").value;
			var visitId =  document.getElementById("patientVisitNo").textContent.trim();
			var ratePlan = patient.org_name;
			url = "StockPatientIssue.do?_method=getItemDetails&item_name="+encodeURIComponent(item)+"&storeid="+storeid+"&planId="+planId+"&visitType="+visitType+"&visitId="+visitId+"&store_rate_plan_id="+storeRatePlanId;
		}else{
			if ( medDetails != null ) {
				setUOMOptions(document.stocktransferform.item_unit,medDetails);//set item UOM options
				setElementText('pkg_size',medDetails.issue_base_unit);
				document.stocktransferform.issue_base_unit.value = medDetails.issue_base_unit;
			}
			if ( sterileTransfer )
				url = "SterileStockTransfer.do?_method=getItemDetails&item_name="+encodeURIComponent(item)+"&storeid="+storeid;
			else
				url = "stocktransfer.do?_method=getItemDetails&item_name="+encodeURIComponent(item)+"&storeid="+storeid;
		}
		if ((document.forms[0].name == 'stockissueform') || (document.forms[0].name == 'patientissueform')){
			getResponseHandlerText(ajaxReqObject, fillItemDetailsLocal, url);
		} else{
			getResponseHandlerText(ajaxReqObject, fillItemDetails_common, url);
		}
	} else {
		if(document.forms[0].name == 'stockissueform' ) {
			document.getElementById("batch").value = "";
			document.getElementById("issuQty").value = "";
			document.getElementById("pkg_size").value = "";
			document.getElementById("inventory").value = "";
			document.getElementById("itemBillable").value = "";
			document.getElementById("expdt").value = "";
		} else if( document.forms[0].name == 'stocktransferform' ) {
			document.getElementById("batch").value = "";
			document.getElementById("batch").length = 1;
			document.getElementById("qty").value = "";
			document.getElementById("pkg_size").innerHTML = "";

		}
	}
}
var itemdetails;
var exp_dt = [];
var package_type = [];
var qty_avbl = [];
var item_ids = [];
var qty_available=0;
var identification_type = [];
var iss_type = [];
var stock_type = [];
var issueUnits = [];
var pkgSize = [];
var item_billable = [];
var mrp = [];
var pkgUOM = [];
var itemMrp = [];
var itemBatchId = [];
// RC: Cleanup required in this method.
function fillItemDetails_common(responseText){
	isbillable = 1;
	var type = '';
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;
	if (document.forms[0].name == 'stockissueform') type = 'Hospital';
	if (document.forms[0].name == 'patientissueform') type = 'Patient';
	if (document.getElementById("inventory").value == 'transfer'){
		var selBatchIndex = document.stocktransferform.batch.value;
	}
	eval("itemdetails = " + responseText);
    var index = 1;

    if ( (gFrom == 'stocktransfer' || gFrom == 'patientissueform' || gFrom == 'stockissueform' ) && groupItemDetails != null) {
		var batchNo = gFrom == 'stocktransfer' ? document.stocktransferform.batch.value : gFrom == 'patientissueform' ? document.patientissueform.batch.value: document.stockissueform.batch.value ;
		var tempStock = [];
		for (var i=0; i<itemdetails.length; i++){
			var item = itemdetails[i];
			if ( batchNo == item.batch_no ) {
				tempStock[0] =  itemdetails[i];
			}
		}
		itemdetails = tempStock;
	}

    if(itemdetails.length == 0){
    	showMessage("js.sales.issues.noavailablestock");
    	document.getElementById("items").value = '';
    	document.getElementById("batch").value="";
    	document.getElementById("batch").length = 1;
    	document.getElementById("pkg_size").value = '';
    	if (document.getElementById("itemBillable") != null) document.getElementById("itemBillable").value="";
    	document.getElementById('barCodeId').value = '';
    	return false;
    }

    for(var t= 0;t<itemdetails.length;t++){
    	document.getElementById('barCodeId').value = itemdetails[t].item_barcode_id;
    	if(document.getElementById("inventory").value == 'transfer'){
    		var selBatchIndex = document.stocktransferform.batch.value;
    		document.getElementById("batch").options[0].text = '-- Select --';
	    	document.getElementById("batch").options[0].value = '';
    		document.getElementById("batch").length = 1+itemdetails.length;
	    	document.getElementById("batch").options[index].text = itemdetails[t].batch_no+( itemdetails[t].exp_dt == null ? '' : "/"+formatExpiry(new Date(itemdetails[t].exp_dt)));
	    	document.getElementById("batch").options[index].value = itemdetails[t].batch_no;
	    	exp_dt[itemdetails[t].batch_no] = itemdetails[t].exp_dt;
	    	package_type[itemdetails[t].batch_no] = itemdetails[t].package_type;
	    	qty_avbl[itemdetails[t].batch_no] = itemdetails[t].qty;
	    	item_ids[itemdetails[t].batch_no] = itemdetails[t].medicine_id;
	    	identification_type[itemdetails[t].batch_no] = itemdetails[t].identification;
            stock_type[itemdetails[t].batch_no] = itemdetails[t].consignment_stock;
            mrp[itemdetails[t].batch_no] = parseFloat(parseFloat(itemdetails[t].mrp)/parseFloat(itemdetails[t].issue_base_unit)).toFixed(2);
	    	issueUnits[itemdetails[t].batch_no] = itemdetails[t].issue_units;
	    	pkgUOM[itemdetails[t].batch_no] = itemdetails[t].package_uom;
	    	pkgSize[itemdetails[t].batch_no] = itemdetails[t].issue_base_unit;
	    	itemBatchId[itemdetails[t].batch_no] = itemdetails[t].item_batch_id;
	    	if (itemdetails.length == 1) changeIdentifiers(itemdetails[t].batch_no);

    	}else{
    	if (document.getElementById("mrno") != null) {
    		if (patient == null) {
        		showMessage("js.sales.issues.patientmrno");
        		return false;
        	}

    		if(itemdetails[0].billable){

	    		isbillable++;
	    		if(document.getElementById("bill_no").value == ''){
	    			showMessage("js.sales.issues.nothave.openbills");
	    			document.getElementById("items").value='';
	    			return false;
	    		}
				if(patient.billstatus == 'F'){
					showMessage("js.sales.issues.billfinalized");
		    			document.getElementById("items").value='';
		    			return false;
				}
				if(patient.billstatus == 'S'){
					showMessage("js.sales.issues.billsettled");
		    			document.getElementById("items").value='';
		    			return false;
				}
    			document.getElementById("creditbill").style.display = 'block';
	    	}
	    }

	    var l_mrp = itemdetails[t].issue_rate_expr == null ?
							( itemdetails[t].selling_price == null ? parseFloat(itemdetails[t].orig_mrp).toFixed(decDigits) : parseFloat(itemdetails[t].selling_price).toFixed(decDigits) )
							: parseFloat(itemdetails[t].mrp).toFixed(decDigits);
	    document.getElementById("batch").options[0].text = '-- Select --';
	    document.getElementById("batch").options[0].value = '';
	    document.getElementById("batch").length = 1+itemdetails.length;
	    if(itemdetails[t].qty != 0){
	    	qty_available++;
	    }
	    document.getElementById("batch").options[index].text = itemdetails[t].batch_no+"/"+itemdetails[t].qty+( itemdetails[t].exp_dt == null ? '' : "/"+formatExpiry(new Date(itemdetails[t].exp_dt)));
	    document.getElementById("batch").options[index].value = itemdetails[t].batch_no;
	    itemBatchId[itemdetails[t].batch_no] = itemdetails[t].item_batch_id;

	    	exp_dt[itemdetails[t].batch_no] = itemdetails[t].exp_dt;
	    	item_ids[itemdetails[t].batch_no] = itemdetails[t].medicine_id;
	    	identification_type[itemdetails[t].batch_no] = itemdetails[t].identification;
            stock_type[itemdetails[t].batch_no] = itemdetails[t].consignment_stock;
	    	iss_type[itemdetails[t].batch_no] = itemdetails[t].issue_type;
	    	issueUnits[itemdetails[t].batch_no] = itemdetails[t].issue_units;
	    	pkgSize[itemdetails[t].batch_no] = itemdetails[t].issue_base_unit;
	    	item_billable[itemdetails[t].batch_no] = itemdetails[t].billable;
	    	pkgUOM[itemdetails[t].batch_no] = itemdetails[t].package_uom;

	    	if (null != document.patientissueform) {
	    		document.getElementById("Disc").value=itemdetails[t].meddisc;
				document.getElementById("discount").value=itemdetails[t].meddisc;
    			document.getElementById("itemMrp").value = l_mrp;
    			document.getElementById("mrp").value = l_mrp;
    			document.getElementById("txType").value = itemdetails[t].tax_type;
				document.getElementById("Disc").value=itemdetails[t].meddisc;
				document.getElementById("discount").value=itemdetails[t].meddisc;
				document.getElementById("tax").value=itemdetails[t].tax_rate;
				document.getElementById("patamt").value = itemdetails[t].patient_amount;
				document.getElementById("patper").value = itemdetails[t].patient_percent;
				document.getElementById("patcatamt").value = itemdetails[t].patient_amount_per_category;
				document.getElementById("patcap").value = itemdetails[t].patient_amount_cap;
				document.getElementById("insuranceCategoryId").value = itemdetails[t].insurance_category_id;
				document.getElementById("isFirstOfCategory").value = itemdetails[t].first_of_category;
				document.getElementById("medicine_id").value = itemdetails[t].medicine_id;

				document.getElementById("Unit").value = itemdetails[t].issue_base_unit;
	    		document.getElementById("categoryId").value=itemdetails[t].category_id;
				itemMrp[itemdetails[t].batch_no] = l_mrp;
	    	}

	    	if (null != itemdetails[t].exp_dt){
	    		document.getElementById("expdt").value = new Date(itemdetails[t].exp_dt);
	    	} else{
	    		document.getElementById("expdt").value = "";
	    	}
	    	if (itemdetails.length == 1) changeItems(itemdetails[t].batch_no,type);
    	}
    	index++;
    }
    if (itemdetails.length > 1){
     document.getElementById("batch").disabled = false;
     document.getElementById("batch").selectedIndex = 0;
     if(document.getElementById("inventory").value == 'transfer'){
     		document.getElementById("batch").selectedIndex = 0;
	    	//document.getElementById("exp_dt").innerHTML = '';
			//document.getElementById("pkg_type").innerHTML = '';
			document.getElementById("qty_avbl").innerHTML = '';
			document.getElementById("mrp").innerHTML = '';
			document.getElementById("pkg_size").innerHTML = itemdetails[0].issue_base_unit;
		}

     }
    else {
    	document.getElementById("batch").selectedIndex = 1;
    	document.getElementById("batch").disabled = true;
    	if(document.getElementById("inventory").value == 'transfer'){
	    	//document.getElementById("exp_dt").innerHTML = formatExpiry(new Date(itemdetails[0].exp_dt));
			//document.getElementById("pkg_type").innerHTML = itemdetails[0].package_type;
			document.getElementById("qty_avbl").innerHTML = itemdetails[0].qty;
			document.getElementById("pkg_size").innerHTML = itemdetails[0].issue_base_unit;
			document.getElementById("mrp").innerHTML = parseFloat(parseFloat(itemdetails[0].mrp)/parseFloat(itemdetails[0].issue_base_unit)).toFixed(2);
			//document.getElementById("issue_units").innerHTML = itemdetails[0].issue_units;
		}
    }

    if ((itemdetails[0].identification == 'S') &&  (qty_available == 0) && ((document.forms[0].name == 'stockissueform')||(document.forms[0].name == 'patientissueform'))){
	    		showMessage("js.sales.issues.noavailablestock");
		    	document.getElementById("items").value = '';
		    	document.getElementById("batch").value="";
		    	document.getElementById("batch").length = 1;
		    	document.getElementById("issuQty").value="";
		    	document.getElementById("issue_units").value = '';
		    	document.getElementById("itemBillable").value="";
				document.getElementById('barCodeId').value = '';
		    	return false;
	}

	if (gFrom == 'stocktransfer') {
		setSelectedIndex(document.stocktransferform.batch,document.getElementById("identifierLabel"+gId).textContent == '' ? document.getElementById("batch").value : document.getElementById("identifierLabel"+gId).textContent);
		changeIdentifiers(document.stocktransferform.batch.value);
		document.getElementById("item_unit").value = document.getElementById("itemUnit"+gId).value;
		document.stocktransferform.qty.value = document.getElementById("transferqtyLabel"+gId).textContent;
		setAvblQty(document.stocktransferform.batch.value);
	}else if ( gFrom == 'patientissue') {
		setSelectedIndex(document.patientissueform.batch,document.getElementById("identifierLabel"+gId).textContent);
	}
	return true;
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

function setUOMOptions(obj,medicineDetails) {

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