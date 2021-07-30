function enable(trantype){
	var item_table = document.getElementById("itemListtable");
	var innertablelength = item_table.rows.length-1;
	if(innertablelength > 0){
		for(var i =innertablelength;i>0;i--){
				item_table.deleteRow(i);
		}
	}
	if (trantype == 'mrno'){
		document.getElementById("patientDetails").style.display = 'block';
		document.getElementById('mrno').disabled = false;
		document.getElementById('mrno').value = '';   //added for Bug : 21398 (if we show mrno/visit id ,should also show Pat det & bill no.. so better option is clear mrno)
	}

	//valReset();
	//document.getElementById("user_issue_nos").length = 1;
	//document.getElementById("user_issue_date").length = 1;
}
function setValues(){
	if(user_type == 'Patient'){
		document.getElementById("mrno").value = user;
	}else {
		var issueTypeObj = document.getElementById("issueType_"+(returnissueType == 'u' ? 'user' : (returnissueType == 'w' ? 'ward' : 'dept')));
		issueTypeObj.checked = 'checked';
		issueTypeObj.value = returnissueType;
		onChangeIssueType(issueTypeObj);
		Hospital_field.value = user;
	}
}
var Hospital_field = null;
function init(){
	Hospital_field = document.UserReturnsForm.hosp_user;
	setValues();
	if ( issuetodept == 'N' ) {
		fillUsers('hosp_user');
	}
	editDialog();
}

function initPatient(){
	Insta.initVisitAcSearch(cpath, "mrno", "mrnoContainer", 'all','all',
	function(type, args) {getPatientDetails();},
	function(type, args) {clearPatientDetails();});
	setValues();
	editDialog();
	initLoginDialog();
}

function initMrNoAutoComplete() {
			Insta.initPatientAcSearch(cpath, 'mrno', 'mrnoContainer', 'all', null, null);
		}

function getStockIssue(user_issue_no,date_time, selected_type){
		if(selected_type == 'issue_no') {
			if ( user_issue_no.value == "" ) {
				document.getElementById("user_issue_date").selectedIndex = 0;
			}
		    for(var i=0;i<issues.length;i++) {
		    	if(user_issue_no.value == issues[i].USER_ISSUE_NO) {
		    		document.getElementById("user_issue_date").value = issues[i].DATE_TIME;
		    		document.getElementById("user_issue_date").selectedIndex = i+1;
		    	}
		    }
		}
		if(selected_type == 'issue_date') {
			if ( date_time.value == "" ) {
				document.getElementById("user_issue_no").selectedIndex = 0;
			}
			 for(var i=0;i<issues.length;i++) {
		    	if(date_time.value == issues[i].DATE_TIME) {
		    		document.getElementById("user_issue_no").value = issues[i].USER_ISSUE_NO;
		    		document.getElementById("user_issue_no").selectedIndex = i+1;
		    	}
		    }
		}
		var ajaxReqObject = newXMLHttpRequest();
		var issueNo = document.getElementById("user_issue_no").value;
		issueNo = issueNo == "" ? '-9999' : issueNo;
		if ((null != document.getElementById("planId") ) && (document.getElementById("planId").value != "0")){
			var planId = document.getElementById("planId").value;
			var url = "StockUserReturn.do?_method=getIssueDetails&user_issue_no=" + issueNo +"&date_time="+document.getElementById("user_issue_date").value+"&plan_id="+planId;
		} else{
			var url = "StockUserReturn.do?_method=getIssueDetails&user_issue_no=" + issueNo +"&date_time="+document.getElementById("user_issue_date").value;
		}
		getResponseHandlerText(ajaxReqObject, fillItemDetails, url);
}
var itemdetails;
function fillItemDetails(responseText){
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;
    eval("itemdetails = " + responseText);
    var itemtable = document.getElementById("itemListtable");
	var len = itemtable.rows.length;
	var tdObj="", hTrObj="";
	hTrObj = itemtable.insertRow(len);
	while(itemtable.childNodes.length > 0){
				itemtable.removeChild(itemtable.childNodes[0]);
			}
	hTrObj = itemtable.insertRow(-1);
	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Return"));
	hTrObj.appendChild(tableHeader);

	var tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Item Name"));
	hTrObj.appendChild(tableHeader);

	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Batch/Serial No"));
	hTrObj.appendChild(tableHeader);

	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Exp. Date"));
	hTrObj.appendChild(tableHeader);

	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Issue Qty"));
	hTrObj.appendChild(tableHeader);

	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Prv Returns"));
	hTrObj.appendChild(tableHeader);

	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Return Qty"));
	hTrObj.appendChild(tableHeader);

	tableHeader = document.createElement("th");
	tableHeader.setAttribute("align", "center");
	tableHeader.appendChild(document.createTextNode("Pkg Size"));
	hTrObj.appendChild(tableHeader);
	if (document.patientreturnsform != null){
		if ((null != showCharges) && (showCharges == 'A')) {
			tableHeader = document.createElement("th");
			tableHeader.setAttribute("style", "text-align:right");
			tableHeader.appendChild(document.createTextNode("Rate"));
			hTrObj.appendChild(tableHeader);
			tableHeader = document.createElement("th");
			tableHeader.setAttribute("style", "text-align:right");
			tableHeader.appendChild(document.createTextNode("Unit Rate"));
			hTrObj.appendChild(tableHeader);
			tableHeader = document.createElement("th");
			tableHeader.setAttribute("style", "text-align:right");
			tableHeader.appendChild(document.createTextNode("Pkg CP"));
			hTrObj.appendChild(tableHeader);
			tableHeader = document.createElement("th");
			tableHeader.setAttribute("style", "text-align:right");
			tableHeader.appendChild(document.createTextNode("Return Amt"));
			hTrObj.appendChild(tableHeader);
			tableHeader = document.createElement("th");
			tableHeader.setAttribute("style", "text-align:right");
			tableHeader.appendChild(document.createTextNode("Pat. Amt"));
			hTrObj.appendChild(tableHeader);
			tableHeader = document.createElement("th");
			tableHeader.setAttribute("style", "text-align:right");
			tableHeader.appendChild(document.createTextNode("Claim Amt"));
			hTrObj.appendChild(tableHeader);

		}
	}

	for(var i = 0;i<itemdetails.length;i++){
		hTrObj = itemtable.insertRow(-1);
		var tableRow =  i;
		var returncheckbox = "returnitem"+i;
		hTrObj.id = tableRow;
		tdObj = hTrObj.insertCell(0);
		if(itemdetails[i].BILLABLE == 't')var billable =1;
		else var billable =0;
		if(itemdetails[i].ISSUE_TYPE == 'Reusable')var type = 0;
		else var type = 1;
		tdObj.innerHTML = '<input type="checkbox" name="returnitem" id="'+returncheckbox+'" onclick="retrunrow(this.id,'+tableRow+','+billable+','+type+')">';

		tdObj = hTrObj.insertCell(1);
		tdObj.innerHTML  = '<label id="item_name'+i+'">'+itemdetails[i].MEDICINE_NAME+'</label>' +
			'<input type="hidden" name="item_id" id="item_id'+i+'"  value="'+itemdetails[i].MEDICINE_ID+'">'+
			'<input type="hidden" name="storeId" id="storeId" value="'+document.getElementById("store").value+'">'+
			'<input type="hidden" name="user_issue_nos" id="user_issue_nos" value="'+itemdetails[i].USER_ISSUE_NO+'">'+
			'<input type="hidden" name="item_issue_no" id="item_issue_no" value="'+itemdetails[i].ITEM_ISSUE_NO+'">';
		setNodeText(tdObj, itemdetails[i].MEDICINE_NAME, 25);
		tdObj = hTrObj.insertCell(2);
		tdObj.innerHTML = '<label>'+itemdetails[i].BATCH_NO+'</label>' +
			'<input type="hidden" name="item_identifier" id="item_identifier" value="'+itemdetails[i].BATCH_NO+'">'+
			'<input type="hidden" name="item_batch_id" id="item_batch_id" value="'+itemdetails[i].ITEM_BATCH_ID+'">'+
			'<input type="hidden" name="exp_dt" id="exp_dt" value="'+itemdetails[i].EXP_DT+'">';

		tdObj = hTrObj.insertCell(3);
		tdObj.innerHTML = '<label>'+formatExpiry(new Date(itemdetails[i].EXP_DT))+'</label>' ;

		tdObj = hTrObj.insertCell(4);
		tdObj.innerHTML = '<label>'+itemdetails[i].QTY+'</label>' ;

		tdObj = hTrObj.insertCell(5);
		tdObj.innerHTML = '<label>'+itemdetails[i].RETURN_QTY+'</label>' ;

		tdObj = hTrObj.insertCell(6);
		tdObj.innerHTML = '<label id='+tableRow+'2>'+itemdetails[i].RETURNQTY+'</label>'+
		'<input type="hidden" class=number" name="returned_qty" id="returned_qty'+i+'" value="'+itemdetails[i].RETURNQTY+'" />'+
		'<input type="hidden" class=number" name="remaining_qty" id="remaining_qty'+i+'" value="'+itemdetails[i].RETURNQTY+'" />'+
		'<input type="hidden" name="itemUnit" id="itemUnit'+i+'" value="I" />'+
		'<input type="hidden" name="issUOM" id="issUOM'+i+'" value="' +itemdetails[i].ISSUE_UOM+'" />'+
		'<input type="hidden" name="packUOM" id="packUOM'+i+'" value="' +itemdetails[i].PACKAGE_UOM+'" />';
		if(itemdetails[i].IDENTIFICATION == 'B'){
			var itemrowbtn = makeButton('editReturnQtyBtn','editReturnQtyBtn'+i,'..');
			itemrowbtn.setAttribute("onclick","editReturnQuantity('"+i+"')");
			tdObj.appendChild(itemrowbtn);
		}

		tdObj = hTrObj.insertCell(7);
		tdObj.innerHTML = '<label>'+itemdetails[i].ISSUE_UNITS+'</label>'+
		'<input type="hidden" name="issue_type" id="issue_type" value="'+itemdetails[i].ISSUE_TYPE+'" />'+
		'<input type="hidden" class=number" name="issue_units" id="issue_units'+i+'" value="'+itemdetails[i].ISSUE_UNITS+'" />'+
		'<input type="hidden" name="claimAmtHid" id="claimAmtHid'+i+'" value="0"/>'+
		'<input type="hidden" name="rowAmtHid" id="rowAmtHid'+i+'" value="0"/>' +
		'<input type="hidden" name="patAmtHid" id="patAmtHid'+i+'" value="0"/>' +
		'<input type="hidden" name="unitMrpHid" id="unitMrpHid'+i+'" value="'+itemdetails[i].UNIT_RATE+'"/>'+
		'<input type="hidden" name="issueDiscHid" id="issueDiscHid'+i+'" value="'+itemdetails[i].DISCOUNT+'"/>'+
		'<input type="hidden" name="pkgMrpHid" id="pkgMrpHid'+i+'" value="'+itemdetails[i].PKG_MRP+'"/>'+
		'<input type="hidden" name="patientInsAmtHid" id="patientInsAmtHid'+i+'" value="'+itemdetails[i].PATIENT_AMOUNT+'"/>' +
		'<input type="hidden" name="patientInsPerHid" id="patientInsPerHid'+i+'" value="'+itemdetails[i].PATIENT_PERCENT+'"/>' +
		'<input type="hidden" name="patientInsClaimHid" id="patientInsClaimHid'+i+'" value="'+itemdetails[i].INSURANCE_CLAIM_AMT+'"/>' +
		'<input type="hidden" name="patientInsCapHid" id="patientInsCapHid'+i+'" value="'+itemdetails[i].PATIENT_AMOUNT_CAP+'"/>'+
		'<input type="hidden" name="netReturnQtyHid" id="netReturnQtyHid'+i+'" value="'+itemdetails[i].QTY+'"/>';
		if (document.patientreturnsform != null){
		if ((null != showCharges) && (showCharges == 'A')) {
			tdObj = hTrObj.insertCell(8);
			tdObj.setAttribute("style","text-align:right");
			tdObj.innerHTML = '<label>'+parseFloat(itemdetails[i].AMOUNT).toFixed(decDigits)+'</label>';
			tdObj = hTrObj.insertCell(9);
			tdObj.setAttribute("style","text-align:right");
			tdObj.innerHTML = '<label id="unitMrp'+i+'">'+parseFloat(itemdetails[i].UNIT_RATE).toFixed(decDigits)+'</label>';

			tdObj = hTrObj.insertCell(10);
			tdObj.setAttribute("style","text-align:right");
			tdObj.innerHTML = '<label>'+parseFloat(itemdetails[i].PACKAGE_CP).toFixed(decDigits)+'</label>' ;
			tdObj = hTrObj.insertCell(11);
			tdObj.setAttribute("style","text-align:right");
			tdObj.innerHTML = '<label id="rowAmt'+i+'">'+'0'+'</label>';
			tdObj = hTrObj.insertCell(12);
			tdObj.setAttribute("style","text-align:right");
			tdObj.innerHTML = '<label id="patAmt'+i+'">'+'0'+'</label>';
			tdObj = hTrObj.insertCell(13);
			tdObj.setAttribute("style","text-align:right");
			tdObj.innerHTML = '<label id="claimAmt'+i+'">'+'0'+'</label>';
		}
	}


	}
	if (itemdetails.length > 0) {
		if (document.getElementById('mrno') != null){
			 document.getElementById("creditbill").style.display = 'block';
	    }
		else document.getElementById("creditbill").style.display = 'none';
	}
}

function getIssueIds(user, store){
		var ajaxObject = newXMLHttpRequest();
		var url = "StockUserReturn.do?_method=getIssueIds&user="+encodeURIComponent(user)+"&store="+encodeURIComponent(store);
		getResponseHandlerText(ajaxObject, fillIssueIds, url);
}
var issues;
function fillIssueIds(responseText) {
	eval(responseText);
	if (responseText==null) return;
	if (responseText=="") return;
    eval("issues = " + responseText);
    var issueId = document.forms[0].user_issue_no;
    var issueDate = document.forms[0].user_issue_date;
	issueId.length = 1;
	issueDate.length = 1;
	issueId.options[0].text = "...Select...";
	issueId.options[0].value = "";
	issueDate.options[0].text = "...Select...";
	issueDate.options[0].value = "";
    if(issues.length > 0) {
    issueId.length = issues.length + 1;
    issueDate.length = issues.length + 1;
    for (var i=0; i<issues.length; i++){
			var item = issues[i];
			issueId.options[i+1].text = item.USER_ISSUE_NO;
			issueId.options[i+1].value = item.USER_ISSUE_NO;
			issueDate.options[i+1].text = item.DATE_TIME;
			issueDate.options[i+1].value = item.DATE_TIME;
		}
    }

}

	function editReturnQuantity(id){
		var button = document.getElementById("editReturnQtyBtn"+id);
		dialog1.cfg.setProperty("context",[button, "tr", "br"], false);
		document.getElementById("dialogId").value = id;
		document.getElementById("remainingQty").textContent = document.getElementById("remaining_qty"+id).value;
		document.getElementById("unit_mrp").value = document.getElementById("unitMrpHid"+id).value;
		//document.getElementById("pkg_mrp").value = document.getElementById("pkgMrpHid"+id).value;
		//document.getElementById("issue_discount").value = document.getElementById("issueDiscHid"+id).value;

		var pkgSize = document.getElementById("issue_units"+id).value;
		var issUOM = document.getElementById("issUOM"+id).value;
		var pckUOM = document.getElementById("packUOM"+id).value;
		var btch = {packageSize: pkgSize, issueUnits: issUOM, packageUOM: pckUOM};
		setUOMOptions(document.getElementById('item_unit'), btch);
		document.getElementById('item_unit').value = document.getElementById("itemUnit"+id).value;
		if (document.getElementById('item_unit').value == 'I')
			document.forms[0].return_qty.value = (allowDecimalsForQty == 'Y')? document.getElementById("returned_qty"+id).value: Math.floor(document.getElementById("returned_qty"+id).value);
		else
			document.forms[0].return_qty.value = (allowDecimalsForQty == 'Y')? document.getElementById("returned_qty"+id).value/pkgSize: Math.floor(document.getElementById("returned_qty"+id).value);


		dialog1.show();
		document.forms[0].return_qty.focus();
	}

	function updateReturnQty(){

		var dialogId = document.getElementById("dialogId").value;
		var update_qty 		= document.forms[0].return_qty.value;
		var returned_qty 	= document.getElementById("returned_qty"+dialogId).value;
		var remainingQty = document.getElementById("remaining_qty"+dialogId).value;
		var itemUnit = document.getElementById('item_unit').value;
		var pkgSize = document.getElementById("issue_units"+dialogId).value;
		if(update_qty == ''){
			alert("Return qty can not be empty");
			return false;
		}
		if(update_qty == 0){
			alert("Return qty can not be zero");
			return false;
		}

		var qtyEntered = update_qty;
		update_qty = itemUnit == 'I' ? update_qty : (update_qty*pkgSize).toFixed(2);

		if (parseFloat(update_qty) > parseFloat(remainingQty) ){
			alert("Return quantity can not be more than remaining quantity");
			return false;
		}
		if (!isValidNumber(document.forms[0].return_qty, allowDecimalsForQty)) return false;

		document.getElementById(dialogId+'2').innerHTML = qtyEntered;


		var rowPkgMrpPaise = getElementIdPaise("unitMrpHid"+dialogId);
		var rowDiscountPaise = getPaise(parseFloat(document.getElementById("issueDiscHid"+dialogId).value)* update_qty);
		var rowAmtPaise = (rowPkgMrpPaise * update_qty) - (rowDiscountPaise)

		document.getElementById("rowAmtHid"+dialogId).value = formatAmountPaise(rowAmtPaise);
		if ((null != showCharges) && (showCharges == 'A'))
			document.getElementById("rowAmt"+dialogId).textContent = formatAmountPaise(rowAmtPaise);

		// also calculate pat amt and claim amt
		calcInsuranceAmts(getPaiseReverse(rowAmtPaise), update_qty, dialogId);
		if ((null != showCharges) && (showCharges == 'A'))
			recalcTotAmt();
		document.getElementById("returned_qty"+dialogId).value = update_qty;
		document.getElementById("itemUnit"+dialogId).value = document.getElementById('item_unit').value;
		dialog1.hide();
		return true;
	}

	function getAmountWithoutPrecisionLoss(rowIssueDisc, update_qty, pkgSize,rowPkgMrp, itemUnit) {
		var amt = itemUnit == 'I'? (rowPkgMrp / update_qty) : (rowPkgMrp/ (update_qty));
		amt = parseFloat(amt*update_qty).toFixed(decDigits);
		return amt;
	}

	function submitReturnForm(button){
//		button.disabled = true;
		if(document.getElementById("itemListtable").rows.length <= 1){
			alert("Please add items to return");
			button.disabled = false;
			return false;
		}
		if(billstatus == 'F'){
			alert("patient bill is finalized,you can not return item");
			button.disabled = false;
	 		return false;
		}
		if(billstatus == 'S'){
			alert("patient bill is settled,you can not return item");
			button.disabled = false;
	   		return false;
		}
		if(billstatus == 'C'){
			alert("patient bill is closed,you can not return item");
			button.disabled = false;
	   		return false;
		}

		for(var l = 0;l<document.getElementById("itemListtable").rows.length-1;l++){
			var checked ;
			if(document.getElementById("returnitem"+l).checked){
				checked = true;
			}
		}
		if(!checked){
				alert("Please select any item to return");
				button.disabled = false;
				return false;
		}
		document.getElementById("_method").value = "create";
		if (document.getElementById('mrno') != null){
			if (isSharedLogIn == 'Y'){
				loginDialog.show();
			} else{
				document.patientreturnsform.submit();
			}
		} else{
			document.UserReturnsForm.return_from.value = Hospital_field.value;
			button.disabled = true;
			document.UserReturnsForm.submit();
		}
	}

function submitHandler() {
	document.getElementById('return').disabled = true;
	document.patientreturnsform.authUser.value = document.getElementById('login_user').value;
	document.patientreturnsform.submit()
}

function popup_mrsearch(){
	window.open(
    '../pages/Common/PatientSearchPopup.do?title=Active%20Patients&mrnoForm=patientreturnsform&mrnoField=mrno&searchType=active',
    'Search','width=655,height=430,scrollbars=yes,left=200,top=200');
    return false;
}//end of popup_mrsearch
function validMr() {
	sub();
	return true;
}
function retrunrow(checkbox,rowid,billable,issuetype){
	if (document.getElementById('mrno' != null)){
		if(billable == 1 && issuetype == 1){
			if(document.getElementById("bill_no").value == ''){
				alert("Patient does not have any open bill, you can not return item(s)");
				document.getElementById(checkbox).checked = false;
				return false;
			}
		}
		if(billable == 1) {
			if(billstatus == 'F'){
				alert("patient bill is finalized,you can not return this item");
		 		document.getElementById(checkbox).checked = false;
		 		return false;
			}
			if(billstatus == 'S'){
				alert("patient bill is settled,you can not return this item");
		   		document.getElementById(checkbox).checked = false;
		   		return false;
			}
		}
	}
	if(document.getElementById(checkbox).checked){
		document.getElementById(rowid).className = 'return';
		document.getElementById(checkbox).value  = rowid;
		//if ((null != showCharges) && (showCharges == 'A')){
			var rowUnitMrp = document.getElementById("unitMrpHid"+rowid).value;
			var rowPkgMrp = document.getElementById("pkgMrpHid"+rowid).value;
			var rowIssueDisc = document.getElementById("issueDiscHid"+rowid).value;
			var update_qty = document.getElementById("returned_qty"+rowid).value
			var rowAmt = parseFloat(rowUnitMrp * update_qty);
			var itemUnit = document.getElementById('item_unit').value;
			var pkgSize = document.getElementById("issue_units"+rowid).value;
			var rowPkgMrpPaise = getElementIdPaise("unitMrpHid"+rowid);
			var rowNetRetQty = document.getElementById("netReturnQtyHid"+rowid).value;
			//var rowAmtPaise = (update_qty == rowNetRetQty)? rowPkgMrpPaise : ((rowPkgMrpPaise/rowNetRetQty) * update_qty);

			var rowDiscountPaise = getPaise(parseFloat(document.getElementById("issueDiscHid"+rowid).value)* update_qty);
			var rowAmtPaise = (rowPkgMrpPaise * update_qty) - (rowDiscountPaise);
			var finAmt = formatAmountPaise(rowAmtPaise);
			if ((null != showCharges) && (showCharges == 'A'))
			document.getElementById("rowAmt"+rowid).textContent=formatAmountValue(finAmt);
			document.getElementById("rowAmtHid"+rowid).value=finAmt;
			calcInsuranceAmts(finAmt,update_qty, rowid);
			if ((null != showCharges) && (showCharges == 'A')) recalcTotAmt();
	}else{
		document.getElementById(rowid).className = '';
		//if ((null != showCharges) && (showCharges == 'A')){
			if ((null != showCharges) && (showCharges == 'A')) document.getElementById("rowAmt"+rowid).textContent=gDefaultVal;
			document.getElementById("rowAmtHid"+rowid).value=0;
			if ((null != showCharges) && (showCharges == 'A')) recalcTotAmt();
		//}

	}
	return true;
}
function getFocus(){
	document.getElementById("search").focus();
}
function getReport(returnid, type){
if(returnid != '0'){
	if(type == 'Patient') {
		window.open(cpath+'/DirectReport.do?report=StoreStockPatientReturns&returnNo='+returnid); }
	else {
		window.open(cpath+'/pages/stores/viewstockissues.do?_method=getPrint&report=StoreStockUserReturns&returnNo='+returnid); }
	}
}
function sub(){
	if (((typeof(document.getElementById("mrno")) != 'undefined')) && (document.getElementById("mrno") != null)){
		document.getElementById("_method").value = "show";
		document.patientreturnsform.submit();
	}
	else{
		document.getElementById("_method").value = "show";
		document.UserReturnsForm.submit();
	}
}
function onChangeStore(storechanged){
   document.forms[0].store.value = storechanged;
	var item_table = document.getElementById("itemListtable");
	var innertablelength = item_table.rows.length-1;
	if(innertablelength > 0){
		for(var i =innertablelength;i>0;i--){
				item_table.deleteRow(i);
		}
	}
	 if (((typeof(document.getElementById("mrno")) != 'undefined')) && (document.getElementById("mrno") != null)) {
	   getIssueIds(document.getElementById("mrno").value,document.forms[0].store.value);
  } else {
  		getIssueIds(Hospital_field.value,document.forms[0].store.value);
  }

}

function formSubmit(){
	if (((typeof(document.getElementById("mrno")) != 'undefined')) && (document.getElementById("mrno") != null)){
		document.getElementById("_method").value = "show";
		document.patientreturnsform.submit();
	}
	else{
		document.getElementById("_method").value = "show";
		document.UserReturnsForm.submit();
	}

}

function searchIssues() {
 getIssueIds(Hospital_field.value,document.forms[0].store.value);
}

function editDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1",
			{
				width:"480px",
				context : ["Return Quantity", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			} );

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                              	scope:dialog1,
	                                              	correctScope:true} );
	dialog1.cfg.queueProperty("keylisteners", escKeyListener);
	dialog1.setHeader("Edit");
	dialog1.render();
}

function handleCancel() {
	dialog1.cancel();
}

/*
 * Take action when the user has typed an invalid mrno
 */
function onInvalidMrno() {
	clearPatientDetails();
	document.patientreturnform.patient_bill.value = "";
	// a timeout is required so that the alert before this does not
	// cause yet another onblur event to interfere with the normal process.
	setTimeout("document.patientreturnform.patient_bill", 0);
}

var patient = null;
function getPatientDetails() {
    var visit_id = document.getElementById("mrno").value;
    var ajaxobj1 = newXMLHttpRequest();
	var url = 'StockPatientReturn.do?_method=getPatientDetailsJSON&visit_id='+visit_id;
	getResponseHandlerText(ajaxobj1,patientDetailsResponseHandler,url.toString());
}

function patientDetailsResponseHandler(responseText){
	eval("var patient =" + responseText);
	clearPatientDetails();
	if (patient == null) {
		document.getElementById("mrno").value = '';
		alert("Invalid MR No, please enter valid MR No.");
		return false;
	}
	getPatientInfo = getPatDetails(patient.mr_no,patient);
	populateBillType(getPatientInfo.bills);
	setPatientDetails(patient);

}


/*
 * Return the patient record, given an mrno. Returns null if patient is not found.
 */
function getPatDetails(mrno,patient) {
	var reqObject = newXMLHttpRequest();
	var storeId = document.getElementById("store").value;
	var saleType="";
	var patientId =document.getElementById("mrno").value!= null?document.getElementById("mrno").value: patient.visit_id == null ? patient.previous_visit_id : patient.visit_id;
	var url = cpath+"/pages/stores/MedicineSalesAjax.do?method=getPatientDetails&patientissue=Y&visitId=" + patientId;
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


function populateBillType(patientActiveCreditBills){
	document.getElementById("bill_no").length=0;
	var billNowText = "";
	if(document.getElementById("mrno").value != '') {
		if(patientActiveCreditBills != null && patientActiveCreditBills.length > 0) {
			for(var i=0;i<patientActiveCreditBills.length;i++) {
				var visitid = patientActiveCreditBills[i].visit_id;
				if(document.forms[0].visitId.value == patientActiveCreditBills[i].visit_id ){ visitid = visitid };
				if(patientActiveCreditBills[i].status == 'A') {
					addOption(document.getElementById("bill_no"),patientActiveCreditBills[i].bill_no,patientActiveCreditBills[i].bill_no);
				}
			}
		}
	}
 }

/*
 * Show the patient details in the patient section for the given patient object
 */
function setPatientDetails(patient) {
	var mName = patient.middle_name == null ? '' : patient.middle_name;
	var lName = patient.last_name == null ? '' : patient.last_name;
	var patientId = patient.patient_id == null ? patient.previous_visit_id : patient.patient_id;
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
	if (patient.visit_type == 'i') {
		var bed_type = patient.alloc_bed_type == null
			||patient.alloc_bed_type == '' ?patient.bill_bed_type:patient.alloc_bed_type+'/'+patient.alloc_bed_name;
		setNodeText('patientBedType', bed_type, null, bed_type);
	} else setNodeText('patientBedType', '', null, '');

	document.patientreturnsform.visitId.value = patient.patient_id;
    document.getElementById("visitType").value = patient.visit_type;
    document.getElementById("planId").value=patient.plan_id;

    document.getElementById("isTpa").value = patient.tpa_id == null ? 'N' : 'Y';

	var tpaName = patient.tpa_name == null ? '' : patient.tpa_name;
    var orgId = patient.org_id == null ? '' : patient.org_id;
    var orgName = patient.org_name == null ? '' : patient.org_name;
    var isPrimarySponsorAvailable = patient.sponsor_type!= null || patient.sponsor_type!= "";
	var isSecondarySponsorAvailable = patient.sec_sponsor_type!= null || patient.sec_sponsor_type!= "";

    if(isPrimarySponsorAvailable) {
		    if (patient.sponsor_type == 'I') {
		    	document.getElementById("primarySponsorRow").style.display = 'table-row';
		    	// node, text, length, title...
		    	setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null,
		  		patient.org_name == null ? "" : patient.org_name);

		  		setNodeText('priSponsorType', "Pri. Sponsor Name:", null, "Pri. Sponsor Name");
		  		setNodeText('priSponsorName', patient.tpa_name, null,  patient.tpa_name);

		  		setNodeText('priIDName', "Insurance Co:", null, "Insurance Co");
		  		setNodeText('priID', patient.insurance_co_name, null,  patient.insurance_co_name);


		  		// Set and display Plan details
		  		var insurance_category = patient.insurance_category == null ? 0 : patient.insurance_category;
				var planId = patient.plan_id == null ? 0 : patient.plan_id;
				if (planId != 0)
				   	document.getElementById("pritpaextrow").style.display = 'table-row';
				else
				   	document.getElementById("pritpaextrow").style.display = 'none';

			  	setNodeText('priPlanType', patient.plan_type_name == null ? "" : patient.plan_type_name, null,
			  		patient.plan_type_name == null ? "" : patient.plan_type_name);
			  	setNodeText('priPlanname', patient.plan_name == null ? "" : patient.plan_name, null,
			  		patient.plan_name == null ? "" : patient.plan_name);
			  	setNodeText('priPolicyId', patient.member_id == null ? "" : patient.member_id, null,
			  		patient.member_id == null ? "" : patient.member_id);

		    } else if(patient.sponsor_type == 'C') {
				document.getElementById("primarySponsorRow").style.display = 'table-row';
		    	// node, text, length, title...
		    	setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null,
		  		patient.org_name == null ? "" : patient.org_name);

		  		setNodeText('priSponsorType', "Pri. Sponsor Name:", null, "Pri. Sponsor Name");
		  		setNodeText('priSponsorName', patient.tpa_name, null,  patient.tpa_name);

		  		setNodeText('priIDName', "Employee ID:", null, "Employee ID");
		  		setNodeText('priID', patient.employee_id, null,  patient.employee_id);
		    } else if(patient.sponsor_type == 'N') {
				document.getElementById("primarySponsorRow").style.display = 'table-row';
		    	// node, text, length, title...
		    	setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null,
		  		patient.org_name == null ? "" : patient.org_name);

		  		setNodeText('priSponsorType', "Pri. Sponsor Name:", null, "Pri. Sponsor Name");
		  		setNodeText('priSponsorName', patient.tpa_name, null,  patient.tpa_name);

		  		setNodeText('priIDName', "National ID:", null, "National ID");
		  		setNodeText('priID', patient.national_id, null,  patient.national_id);
		    }
	}


	if(isSecondarySponsorAvailable) {
		    if (patient.sec_sponsor_type == 'I') {
		    	document.getElementById("secSponsorRow").style.display = 'table-row';
		    	// node, text, length, title...

		  		setNodeText("secSponsorType", "Sec. Sponsor Name", null, "Sec. Sponsor Name");
		  		setNodeText('secSponsorName', patient.sec_tpa_name, null,  patient.sec_tpa_name);

		  		setNodeText('secIDName', "Insurance Co", null, "Sec. Insurance Co");
		  		setNodeText('secID', patient.sec_insurance_co_name, null,  patient.sec_insurance_co_name);

		  		// Set and display Plan details
		  		var insurance_category = patient.insurance_category == null ? 0 : patient.insurance_category;
				var planId = patient.plan_id == null ? 0 : patient.plan_id;
				if (planId != 0)
				   	document.getElementById("sectpaextrow").style.display = 'table-row';
				else
				   	document.getElementById("sectpaextrow").style.display = 'none';

			  	setNodeText('secPlanType', patient.plan_type_name == null ? "" : patient.plan_type_name, null,
			  		patient.plan_type_name == null ? "" : patient.plan_type_name);
			  	setNodeText('secPlanname', patient.plan_name == null ? "" : patient.plan_name, null,
			  		patient.plan_name == null ? "" : patient.plan_name);
			  	setNodeText('secPolicyId', patient.member_id == null ? "" : patient.member_id, null,
			  		patient.member_id == null ? "" : patient.member_id);
		    } else if(patient.sec_sponsor_type == 'C') {
				document.getElementById("secSponsorRow").style.display = 'table-row';
		    	// node, text, length, title...

		  		setNodeText('secSponsorType', "Sec. Sponsor Name", null, "Sec. Sponsor Name");
		  		setNodeText('secSponsorName', patient.sec_tpa_name, null,  patient.sec_tpa_name);

		  		setNodeText('secIDName', "Employee ID", null, "Employee ID");
		  		setNodeText('secID', patient.sec_employee_id, null,  patient.sec_employee_id);
		    } else if(patient.sec_sponsor_type == 'N') {
				document.getElementById("secSponsorRow").style.display = 'table-row';
		    	// node, text, length, title...

		  		setNodeText('secSponsorType', "Sec. Sponsor Name", null, "Sec. Sponsor Name");
		  		setNodeText('secSponsorName', patient.sec_tpa_name, null,  patient.sec_tpa_name);

		  		setNodeText('secIDName', "National ID", null, "National ID");
		  		setNodeText('secID', patient.sec_national_id, null,  patient.sec_national_id);
		    }
	}

	if(!isPrimarySponsorAvailable  && (orgId != '' && orgName != 'GENERAL')){
		document.getElementById("primarySponsorRow").style.display = 'table-row';
		setNodeText('ratePlan', patient.org_name == null ? "" : patient.org_name, null, patient.org_name == null ? "" : patient.org_name);
	}


	if(document.getElementById("bill_no").value == ''){
		alert("Patient does not have any open bill, you can not return item(s)");
	  	document.getElementById("creditbill").style.display = 'block';
	  	return false;
  	}

	getIssueIds(patient.patient_id, document.forms[0].store.value);

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
	setNodeText('custom_field11', '', null, '');
	setNodeText('custom_field12', '', null, '');
	setNodeText('custom_field13', '', null, '');
	setNodeText('tpaName', '', null, '');
	setNodeText('patientInsuranceCo', '', null, '');
	setNodeText('ratePlan', '', null, '');
	setNodeText('planType', '', null, '');
	setNodeText('planname', '', null, '');
	setNodeText('policyId', '', null, '');

	document.patientreturnsform.visitId.value = '';
	document.patientreturnsform.visitType.value = '';
	document.patientreturnsform.planId.value = '';

	document.getElementById("primarySponsorRow").style.display = 'none';
	document.getElementById("pritpaextrow").style.display = 'none';
	document.getElementById("secSponsorRow").style.display = 'none';
	document.getElementById("sectpaextrow").style.display = 'none';
}
/*
 * Return the patient record, given an mrno. Returns null if patient is not found.
 */
function findMrNo(mrno) {
	for (var i=0; i<activemrnos.length; i++) {
		var patientDetails = activemrnos[i];
		if (patientDetails.mrNo == mrno) {
			return patientDetails;
		}
	}
	return null;
}

function resetStore () {
	if (storeID != '')
	document.getElementById("store").value = storeID;
	else
		document.getElementById("store").selectedIndex = 0;
}

function resetUser(){
	if (issuetodept == 'N'){
		document.getElementById("issueType_user").checked = 'checked';
		document.getElementById("issueType_user").value = 'u';
		onChangeIssueType(document.getElementById("issueType_user"));
	}else{
		document.getElementById("issueType_dept").checked = 'checked';
		document.getElementById("issueType_dept").value = 'd';	
		onChangeIssueType(document.getElementById("issueType_dept"));
	}
}
function resetIssueIds() {
	document.getElementById("user_issue_no").value = '';
}

function resetIssueDates() {
	document.getElementById("user_issue_date").value = '';
}

function checkstoreallocation() {
 if(gRoleId != 1 && gRoleId != 2) {
 	if(deptId == "") {
 		alert("There is no assigned store, hence you dont have any access to this screen");
 		document.getElementById("storecheck").style.display = 'none';
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

function recalcTotAmt(){
	var itemtable = document.getElementById("itemListtable");
	var len = itemtable.rows.length;
	var tabLen = len-1;
	var totAmt = 0;
	var totPatAmt = 0;
	var totClaimAmt = 0;
	for (i=0; i<tabLen; i++){
		if (document.getElementById(i).className == 'return'){
			totAmt = totAmt + parseFloat(document.getElementById("rowAmtHid"+i).value);
			totPatAmt = totPatAmt + parseFloat(document.getElementById("patAmtHid"+i).value);
			totClaimAmt = totClaimAmt + parseFloat(document.getElementById("claimAmtHid"+i).value);
		}
	}
	document.getElementById("totAmt").textContent = formatAmountValue(totAmt);
	document.getElementById("totPatAmt").textContent = formatAmountValue(totPatAmt);
	document.getElementById("totClaimAmt").textContent = formatAmountValue(totClaimAmt);
}

function getClaimAmt(amt, patamount, patpercent, patcap, isPost, discount){
	var chgPcAmt = isPost == 'N'? parseFloat(amt)+parseFloat(discount) : parseFloat(amt);

	var claimAmt = (amt-patamount-(chgPcAmt*patpercent/100));
	if(claimAmt<0){
		 claimAmt = 0;
	}else if(patcap != null && patcap > 0 && claimAmt>=patcap){
		claimAmt= amt-patcap;
	}
	return claimAmt;
}

function calcInsuranceAmts(amt,update_qty, id){
	var rowDiscount =parseFloat(document.getElementById("issueDiscHid"+id).value)* update_qty;
	var planId =  null != document.getElementById("planId")? document.getElementById("planId").value : 0 ;
	if (null != document.getElementById("planId") && document.getElementById("planId").value != "0"){
		//if patient has insurance plan

		var isPost = null;
		if ((planId != null && planId != "" && planId != "0")) {
			var pUrl;
			var pAjaxReqObject = newXMLHttpRequest();
			pUrl = cpath+"/pages/stores/MedicineSalesAjax.do?method=isPostDiscountOrPreDiscountPayable&planId="+planId;
			pAjaxReqObject.open("POST",pUrl, false);
			pAjaxReqObject.send(null);
			if (pAjaxReqObject.readyState == 4) {
				if ( (pAjaxReqObject.status == 200) && (pAjaxReqObject.responseText!=null) ) {
						isPost = eval(pAjaxReqObject.responseText);
				}
			}

		}
		var patientAmt = document.getElementById("patientInsAmtHid"+id).value;
		var patientPer = document.getElementById("patientInsPerHid"+id).value;
		var patientCap = document.getElementById("patientInsCapHid"+id).value;
		//claimAmt = parseFloat(document.getElementById("patientInsClaimHid"+id).value);
		claimAmt = getClaimAmt(amt, patientAmt, patientPer, patientCap, isPost, rowDiscount);
		var claimAmtPaise = getPaise(claimAmt);
		var patAmtPaise = getPaise(amt) - claimAmtPaise;
		claimAmt = formatAmountPaise(claimAmtPaise);
		document.getElementById("claimAmtHid"+id).value = claimAmt;
		if ((null != showCharges) && (showCharges == 'A')) document.getElementById("claimAmt"+id).textContent= claimAmt;
		document.getElementById("patientInsClaimHid"+id).value = claimAmt;
		document.getElementById("patAmtHid"+id).value = formatAmountPaise(patAmtPaise);
		if ((null != showCharges) && (showCharges == 'A')) document.getElementById("patAmt"+id).textContent=
			formatAmountPaise(patAmtPaise);
	} else{
		//he may not have insurance but may have a tpa
		var billNo = document.getElementById("bill_no").value;
		var medId = document.getElementById("item_id"+id).value;
		var url;
		var ajaxReqObject = newXMLHttpRequest();
		//AJAX call to get whether this med. is claimable. If TPA is set, and category of item is claimable, the amt is claimable
		url = "StockPatientReturn.do?_method=isSponsorBill&medId="+medId+"&billNo="+billNo;
		ajaxReqObject.open("POST",url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
				getFinalAmts (ajaxReqObject.responseText, id);
			}
		}
	}
}

	function getFinalAmts(responseText, id){
		var isInsurance = "";
		var claimAmt = 0;
		eval(responseText);
		if (responseText==null) return;
		if (responseText=="") return;
		var finalAmt = document.getElementById("rowAmtHid"+id).value ;

		if (responseText == 'true'){
			claimAmt = finalAmt;
		}
		var claimAmtPaise = parseFloat(getPaise(claimAmt));
		claimAmt = formatAmountPaise(Math.round(claimAmtPaise));
		document.getElementById("claimAmtHid"+id).value = parseFloat(claimAmt).toFixed(decDigits);
		if ((null != showCharges) && (showCharges == 'A')) document.getElementById("claimAmt"+id).textContent= parseFloat(claimAmt).toFixed(decDigits);
		document.getElementById("patAmtHid"+id).value = parseFloat(finalAmt - claimAmt).toFixed(decDigits);
		if ((null != showCharges) && (showCharges == 'A')) document.getElementById("patAmt"+id).textContent= parseFloat(finalAmt - claimAmt).toFixed(decDigits);
	}

	function setUOMOptions(obj, btch) {
		var uomOptList = null;
		if ( btch.packageSize > 1) {
			uomOptList = [
						{uom_name: btch.issueUnits, uom_value: 'I'},
						{uom_name: btch.packageUOM, uom_value: 'P'}
					];
		}else if ( btch.packageSize == 1 ) {
			uomOptList = [
						{uom_name: btch.issueUnits, uom_value: 'I'}
					];
		}
		loadSelectBox(obj, uomOptList, 'uom_name', 'uom_value');
	}

function formatExpiry(dateMSecs) {
	var dateStr = '';
	if (dateMSecs != null) {
		var dateObj = new Date(dateMSecs);
		dateStr = formatDate(dateObj, 'monyyyy', '-');
	}
	return dateStr;
}

function onChangeIssueType(obj){
	if(obj.value == 'u' && issuetodept == 'N'){
		Hospital_field = document.UserReturnsForm.hosp_user;
		document.UserReturnsForm.hosp_user.disabled = false;
		document.UserReturnsForm.issue_dept.disabled = true;
		document.UserReturnsForm.issue_ward.disabled = true;
		document.UserReturnsForm.issue_dept.value = "";
		document.UserReturnsForm.issue_ward.value = "";
		
		document.getElementById("hosp_user_mand").style.display = 'block';
		document.getElementById("issue_dept_mand").style.visibility = 'hidden';
		document.getElementById("issue_ward_mand").style.visibility = 'hidden';
	} else if ( obj.value == 'd'){
		Hospital_field = document.UserReturnsForm.issue_dept;
		if ( document.UserReturnsForm.hosp_user ){
			document.UserReturnsForm.hosp_user.disabled = true;
			document.UserReturnsForm.hosp_user.value = '';
			document.getElementById("hosp_user_mand").style.display = 'none';
		}
		document.UserReturnsForm.issue_dept.disabled = false;
		document.UserReturnsForm.issue_ward.disabled = true;
		document.UserReturnsForm.issue_dept.value = "";
		document.UserReturnsForm.issue_ward.value = "";
		
		document.getElementById("issue_dept_mand").style.visibility = 'visible';
		document.getElementById("issue_ward_mand").style.visibility = 'hidden';
	} else{
		Hospital_field = document.UserReturnsForm.issue_ward;
		if ( document.UserReturnsForm.hosp_user ){
			document.UserReturnsForm.hosp_user.disabled = true;
			document.UserReturnsForm.hosp_user.value = '';
			document.getElementById("hosp_user_mand").style.display = 'none';
		}
		document.UserReturnsForm.issue_dept.disabled = true;
		document.UserReturnsForm.issue_ward.disabled = false;
		document.UserReturnsForm.issue_dept.value = "";
		document.UserReturnsForm.issue_ward.value = "";
		
		document.getElementById("issue_dept_mand").style.visibility = 'hidden';
		document.getElementById("issue_ward_mand").style.visibility = 'visible';
	}
	
}