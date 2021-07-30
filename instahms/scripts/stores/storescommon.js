var ouserAutoComp ;
var oItemAutoComp;
var isbillable = 0;

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
			document.getElementById("unit_rate").value = "";

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
var unitMrp = [];
var pkgUOM = [];
var itemMrp = [];
var itemBatchId = [];
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
			unitMrp[itemdetails[t].batch_no] = parseFloat(parseFloat(itemdetails[t].mrp)/parseFloat(itemdetails[t].issue_base_unit)).toFixed(decDigits);
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
			document.getElementById("unit_rate").innerHTML = '';
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

function validMrno(){
}

function clearFields(){
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
