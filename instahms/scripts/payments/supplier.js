var toolbar = {
Print :{
		title:"Print PO",
		imageSrc: "icons/Print.png",
		href:'',
		onclick: null,
		target: "_blank",
		description: "to view the supplier purchase order report",
	  }
};


var supplierForm = document.supplierPaymentForm;
var supplierSearchForm = document.supplierPaymentSearchForm;

function init(){
	supplierForm = document.supplierPaymentForm;
	supplierSearchForm = document.supplierPaymentSearchForm;
	allSuppliers();
	supplierSearchForm._supplierName.focus();
	createToolbar(toolbar);
	getPrint();
	initDescriptionDialog();
}

function enableStatus(){
	var disabled = supplierForm.statusAll.checked;
	supplierForm.statusActive.disabled = disabled;
	supplierForm.statusCancelled.disabled = disabled;
	supplierForm.statusClosed.disabled = disabled;
}

function getSupplierCharges(){
	var supplier = document.getElementById("suppliers").value
	if (supplier == ""){
		alert("Select Supplier Name");
		return false;
	}

	if (supplierSearchForm.supplier_id.value == ""){
		alert("Select Supplier Name");
		return false;
	}

	var fDate = document.getElementById("invoice_date0").value;
	var toDate = document.getElementById("invoice_date1").value;
	if (document.getElementById("invoice_date").checked){
		document.getElementById("invoice_date").value = "invoice";
	}if (document.getElementById("due_date").checked){
		document.getElementById("due_date").value = "due";
	}

	if ((document.getElementById("invoice_date").checked) ||
			(document.getElementById("due_date").checked)){
		if (fDate == ""){
			alert("Select from date");
			return false;
		}
		if (toDate == ""){
			alert("Select to date");
			return false;
		}
	}

	if (fDate!="" || toDate!=""){

		if ((!document.getElementById("invoice_date").checked) &&
				(!document.getElementById("due_date").checked)){
			alert("Select date type");
			return false;
		}
		if(getDateDiff(fDate,toDate)<0){
			alert("From date should not greater than Todate");
			return false;
		}
	}

		document.supplierPaymentSearchForm._method.value = "searchSupplierCharges";
		document.supplierPaymentSearchForm.submit();
		return true;
}

function directPayments(){
	var dirchk = document.supplierPaymentSearchForm._directPayment.checked;

	if(dirchk){
         if (parseInt(maxcenters) >1 && parseInt(centerId) == 0) {
         	alert('Default Center user cannot add payments');
         	document.supplierPaymentSearchForm._directPayment.checked = false;
         	return false;
         }
		 document.getElementById("directDiv").style.display="block";
		 document.getElementById("paymentContentDiv").style.display="none";
		 document.getElementById("optionalFilter").style.display="none";

		 document.getElementById("viewContentDiv").style.display="none";

	}else{
		 document.getElementById("directDiv").style.display="none";
		 document.getElementById("paymentContentDiv").style.display="block";
		 document.getElementById("optionalFilter").style.display="block";
		 document.getElementById("viewContentDiv").style.display="block";
	}

}


function addNewRow(){
	var directPaymentTable = document.getElementById("directTable");
	var numRows = directPaymentTable.rows.length;

	var id = numRows-1;
	var rows = directPaymentTable.insertRow(-1);
	var cells;

	cells = rows.insertCell(-1);
	cells.align="center";
	cells.innerHTML='<input type="text" name="description" id="description_'+id+'" size="40"/>';
	cells = rows.insertCell(-1);
	cells.align="center";
	cells.innerHTML='<input type="text" name="amount" id="amount_'+id+'" size="15"  onkeypress="return enterNumOnly(event);"  onblur="roundEnteredNumber(this.value,2,'+id+');"/>'+
		'<input type="hidden" name="add" id="add_'+id+'" value="true" onclick="return saveDirectPayments(id)"/>';

}


function deleteRows(){
	var tablen = document.getElementById("directTable").rows.length;
	if(tablen!=2){
		document.getElementById("directTable").deleteRow(-1);
	}
}

function roundEnteredNumber(number,decimal,id){
	if (number != ''){
		document.getElementById("amount_"+id).value = roundNumber(number,decimal);
	}
}

function saveDirectPayments(id){
    var amountIsZero = false;
	var supplier = document.getElementById("suppliers").value
	if (supplier == ""){
		alert("Select Supplier Name");
		return false;
	}

	var amt = document.getElementsByName("amount");
	for(i=0;i<amt.length;i++){
		if(amt[i].value == ""){
			alert("Enter the amount ");
			amt[i].focus();
			return false;
		}else {
			var enteredAmt = amt[i].value;
			for (j=0;j<enteredAmt.length;j++){
				if (enteredAmt[j]!= "0" && enteredAmt[j]!= "."){
					amountIsZero = false;
					break;
				}else{
					amountIsZero = true;
				}
			}
			if (amountIsZero){
				alert("Entered amount should be greater than zero");
				amt[i].focus();
				return false;
			}
		}
	}
	var paydate = supplierForm.payDate.value;
	if(paydate==""){
		alert("Select Date as Current Date");
		return false;
	}

	var msg = validateDateStr(paydate,"past");
	if (msg != null){
		alert(msg);
		return false;
	}

	var supplierId = document.forms['supplierPaymentSearchForm'].supplier_id.value;
	document.forms['supplierPaymentForm'].supplier_id.value = supplierId;
	document.supplierPaymentForm._method.value="makeDirectPayment";
	document.supplierPaymentForm.submit();
	return true;
}
addItems =0;
function editSupplierPayment(checkbox,rowId){
	var selectValue = selectOptions();
	if (selectValue == "pgItems"){
		addItems = supplierForm.addCharge.value;
		if (!checkbox.checked){
			addItems--;
		}else{
			addItems++;
		}
		 supplierForm.addCharge.value  = addItems;
		 var chkItems = checkedValues(checkbox,rowId);
	}else{
		 supplierForm.addCharge.value =  checkedValues(checkbox,rowId);
	}
}

var addCharge=0;
var deleteCharge =0;
function checkedValues(checkbox,rowId){
	var amount = 0;
	if(checkbox.checked){
		addCharge++;
		amount = amount + document.getElementsByName("_pendingAmount")[(rowId-1)].value;
		var amt = document.getElementsByName("_pendingAmount")[(rowId-1)];
		var totalAmt = document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totalAmt) + getElementPaise(amt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
	}else{
		addCharge--;
		var amt = document.getElementsByName("_pendingAmount")[(rowId-1)];
		var totalAmt = document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totalAmt) - getElementPaise(amt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
	}
	return addCharge;
}

function selectOptions(){
	var selectItems = document.getElementsByName("_selectItems");
	var selectValue;
	for (var i=0;i<selectItems.length; i++){
			if (selectItems[i].checked){
				selectValue = selectItems[i].value;
			}
	}
	return selectValue;
}

function checkedItems(){
	var checkbox = document.getElementsByName("paymentCheckBox");
	for(var i=0;i<checkbox.length;i++){
		if (checkbox[i].checked){
			addCharge++;
		}else{
			addCharge--;
		}
	}
	return addCharge;
}

function deleteSupplierPayment(checkBox,rowId){
	if(checkBox.checked){
		deleteCharge++;
	}else{
		deleteCharge--;
	}
}


function validateSupplier(){
	if (document.getElementById("allCharges").value  == "all"){
	//	supplierForm.addCharge.value = checkedItems();
		return true;
	}else if (document.getElementById("allCharges").value == 'pgItems'){
		var itemsLen = supplierForm.addCharge.value;
		if (itemsLen<=0){
			alert("Select items to save");
			return false;
		}else{
			return true;
		}
	}else {
		if(addCharge>0){
			supplierForm.addCharge.value = addCharge;
			return true;
		}else{
			alert("Select items to save");
			return false;
		}
	}
}

function saveCharges(){
	if (validateSupplier()){
		document.supplierPaymentForm._method.value="createPayments";
		var dateType = '';
		var invoiceType = document.supplierPaymentSearchForm._invoice_type;
		var invoiceDate = document.supplierPaymentSearchForm._invoice_date;
		for (var i=0; i<invoiceType.length; i++) {
			if (invoiceType[i].checked) {
				dateType = invoiceType[i].value;
			}
		}

		if (dateType == 'due') {
			for (var i=0; i<invoiceDate.length; i++) {
				document.supplierPaymentForm.appendChild(makeHidden("due_date", "due_date"+i, invoiceDate[i].value));
			}
			document.supplierPaymentForm.appendChild(makeHidden("due_date@op", "", "ge,le"));
			document.supplierPaymentForm.appendChild(makeHidden("due_date@type", "", "date"));
			document.supplierPaymentForm.appendChild(makeHidden("due_date@cast", "", "y"));
		} else if(dateType == 'invoice') {
			for (var i=0; i<invoiceDate.length; i++) {
				document.supplierPaymentForm.appendChild(makeHidden("invoice_date", "invoice_date"+i, invoiceDate[i].value));
			}
			document.supplierPaymentForm.appendChild(makeHidden("invoice_date@op", "", "ge,le"));
			document.supplierPaymentForm.appendChild(makeHidden("invoice_date@type", "", "date"));
			document.supplierPaymentForm.appendChild(makeHidden("invoice_date@cast", "", "y"));
		}
		document.supplierPaymentForm.submit();
		return true;
	}else{
		return false;
	}


}

function deleteCharges(){
	if(deleteCharge>0){
		supplierForm.deleteCharge.value = deleteCharge;
	}else{
		alert("Select items to Delete");
		return false;
	}
	document.supplierPaymentForm._method.value="deleteSupplierCharge";
	document.supplierPaymentForm.submit();
	return true;

}

//Function for resetting the values

function resetAll(){

	supplierForm.supplier.value = "all"; //For resetting the supplier name
	supplierForm.fdate.value = "";
	supplierForm.tdate.value = "";
}


function deleteAllCharges(){

	var delRows = 0;
	var checkall= supplierForm.deleteAll.checked;
	var len = supplierForm.paidCheckBox.length;


	if (len == undefined){
			if (checkall) {
				delRows++;
			}
		supplierForm.paidCheckBox.checked=checkall;
	}else{
		for (var i=0;i<len;i++){
			supplierForm.paidCheckBox[i].checked=checkall;
			if (supplierForm.paidCheckBox[i].checked)
				delRows++;
			else
				delRows--;
		}
	}
	if (delRows > 0)
		deleteCharge = delRows;
	else
		deleteCharge = 0;
}

var supplierAutoComp = null;
function allSuppliers(){
	for (var i in suppliersList){
		var suppliers = suppliersList[i];
		if (document.getElementById("supplierCode").value==suppliers.SUPPLIER_CODE){
			document.getElementById("suppliers").value=suppliers.SUPPLIER_NAME;
		}
	}
	if (supplierAutoComp == null){
		var supplierArray = [];
		if(suppliersList != null){
			for (var i in suppliersList){
				var supplier = suppliersList[i];
				var len = supplierArray.length;
				supplierArray.length = len+1;
				//supplierArray[len] = supplier.SUPPLIER_NAME;
				if(supplier.CUST_SUPPLIER_CODE != ''){
					supplierArray[len] = supplier.SUPPLIER_NAME+' - '+supplier.CUST_SUPPLIER_CODE;
	        	}else{
	        		supplierArray[len] = supplier.SUPPLIER_NAME;
	        	}
			}
		}
		this.oDocSCDS = new YAHOO.widget.DS_JSArray(supplierArray);
		supplierAutoComp = new YAHOO.widget.AutoComplete("suppliers","supplierContainer", this.oDocSCDS);
		//supplierAutoComp.maxResultsDisplayed = 20;
		supplierAutoComp.allowBrowserAutocomplete = false;
		supplierAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		supplierAutoComp.typeAhead = true;
		supplierAutoComp.useShadow = false;
	    //supplierAutoComp.animVert = false;
		supplierAutoComp.minQueryLength = 0;
		//supplierAutoComp.forceSelection = true;
		supplierAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	    supplierAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	    supplierAutoComp.itemSelectEvent.subscribe(getSupplierId);
	    supplierAutoComp.selectionEnforceEvent.subscribe(clearHiddenId);

	}

	function getSupplierId(){
		var supplierName = YAHOO.util.Dom.get("suppliers").value;
		supplierName = supplierName.split(' - ');
		supplierName = supplierName[0];

		for (var i in suppliersList) {
			var sup  = suppliersList[i];
			if (sup.SUPPLIER_NAME == supplierName){
				document.getElementById("supplierCode").value = sup.SUPPLIER_CODE;
				document.getElementById("supplierName").value = sup.SUPPLIER_NAME;
				document.getElementById("suppliers").value = supplierName;
			}
		}
	}
	function clearHiddenId(){
		document.getElementById("supplierCode").value = '';
	}
}
function getPrint(){
	toolbar.Print.href="pages/stores/poscreen.do?_method=generatePOprint";
}

var setHref = function(params, id, enableList){
	if (empty(gToolbar)) return ;

	var i=0;

	for (var key in gToolbar){
		var data = gToolbar[key];
		var anchor = document.getElementById("toolbarAction"+key);
		var href = data.href;
		if (!empty(anchor)){
			for (var paramname in params){
				var paramvalue = params[paramname];
				href +="&"+paramname+"="+paramvalue;
			}
		if (key == 'Print'){
			var printHref = "";
			if (params['invoiceType'] == 'I'){
				printHref = "DirectReport.do?report=InventoryPOReport";
				href = printHref+href;
			}else if (params['invoiceType'] == 'P'){
				printHref = "DirectReport.do?report=poreport";
				href = printHref+href;
			}else {
				href="DirectReport.do";
			}
		}
		anchor.href = cpath +"/"+href;
		if (enableList)
			enableToolbarItem(key, enableList[i]);
		else
			enableToolbarItem(key, true);
		}else {
			debug("No anchor  for "+ 'toolbarAction'+key + ":");
		}
		i++;
	}

}

function initDescriptionDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1",
			{	width:"300px",
				context : ["insurance", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
			} );
	YAHOO.util.Event.addListener('editOk', 'click', handleOk, dialog1, true);
	YAHOO.util.Event.addListener('editDialogCancel', 'click', handleCancel, dialog1, true);
	YAHOO.util.Event.addListener('editDialogPrevious', 'click', openPrevious, dialog1, true);
	YAHOO.util.Event.addListener('editDialogNext', 'click', openNext, dialog1, true);
	dialog1.render();
}


function handleCancel() {
	this.cancel();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	// index of the first doctor fee item: 0 is header, 1 is first fee item.
	return 1;
}

function setIndexedValue(form, name, index, value) {
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.supplierPaymentForm, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function getChargeRow(i) {
	i = parseInt(i);
	var table = document.getElementById("resultTable");
	return table.rows[i + getFirstItemRow()];
}

function setHiddenValue(mainform, index, name, value) {
	var el = getIndexedFormElement(mainform, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function handleOk() {
	var ohFee = document.getElementById('_dialog_description').value;
	if (ohFee.length > 50) {
		alert("Sorry, Description should be less than 50 characters");
		return false;
	}
	var id = document.getElementById('editRowId').value;
	var row =  getChargeRow(id);
	setNodeText(row.cells[5], ohFee, 15, ohFee);
	setHiddenValue(supplierForm, id, "_sdescription", ohFee);
	this.cancel();
}

function openPrevious() {
	var ohFee = document.getElementById('_dialog_description').value;
	if (ohFee.length > 50) {
		alert("Sorry, Description should be less than 50 characters");
		return false;
	}
	var id = document.getElementById('editRowId').value;
	var row =  getChargeRow(id);
	setNodeText(row.cells[5], ohFee, 15, ohFee);
	setHiddenValue(supplierForm, id, "_sdescription", ohFee);
	this.cancel();
	if (parseInt(id) != 0)
		showDescriptionDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
}

function openNext() {
	var ohFee = document.getElementById('_dialog_description').value;
	if (ohFee.length > 50) {
		alert("Sorry, Description should be less than 50 characters");
		return false;
	}
	var id = document.getElementById('editRowId').value;
	var row =  getChargeRow(id);
	setNodeText(row.cells[5], ohFee, 15, ohFee);
	setHiddenValue(supplierForm, id, "_sdescription", ohFee);
	this.cancel();
	if (parseInt(id) != document.getElementById('resultTable').rows.length-2) {
		showDescriptionDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
	}
}

function showDescriptionDialog(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	document.getElementById('editRowId').value = id;
	dialog1.cfg.setProperty("context", [obj, "tr", "tl"], false);
	dialog1.show();
	document.getElementById('_dialog_description').value = getIndexedFormElement(supplierForm, '_sdescription', id).value;
	return false;
}


function onCheckRadio(obj){
	var statEle =  document.getElementsByName("paymentCheckBox");
	var amount;
	if (obj =="all") {
		var supplierId = document.getElementById("supplier_id").value;
		var invoiceType = document.supplierPaymentSearchForm._invoice_type;
		var isCashPurchasechkd = document.getElementById('cashPurchase').value;
		var isCashpurchaseRslt = isCashpurchaseResult;

		if (isCashpurchaseRslt == 'Y')
			isCashPurchasechkd = 'Y';
		else
			isCashPurchasechkd = 'N';

		var dateType;
		for (var i=0;i<invoiceType.length;i++){
			if (invoiceType[i].checked ){
				dateType = invoiceType[i].value;
			}
		}
		var fromDate = document.getElementById("invoice_date0").value;
		var toDate = document.getElementById("invoice_date1").value;
		var url = cpath + '/pages/payments/SupplierPayments.do?_method=getAllSupplierAmount&supplier_id='
										+supplierId+'&_dateType='+dateType+'&_fromDate='+fromDate+'&_toDate='+toDate+'&_cash_purchase='+isCashPurchasechkd;

		var reqObj = newXMLHttpRequest();
		reqObj.open("POST", url.toString(), false)
		reqObj.send(null);
		if (reqObj.readyState == 4) {
			if ( (reqObj.status == 200) && (reqObj.responseText)){
				amount = eval(reqObj.responseText);
			}
		}
	}

	for (i=0;i<statEle.length;i++) {
		if (obj =="all") {
			document.getElementsByName("paymentCheckBox")[i].checked = true;
			document.getElementsByName("paymentCheckBox")[i].disabled = true;
			document.getElementById("allTotAmt").textContent = amount;
			document.getElementById("allCharges").value = obj;
		}else if (obj =="pgItems") {
		amount = selectPageItems();
			document.getElementsByName("paymentCheckBox")[i].checked = true;
			document.getElementsByName("paymentCheckBox")[i].disabled = false;
			document.getElementById("allTotAmt").textContent = amount;
			document.getElementById("allCharges").value = obj;
		} else {
			document.getElementsByName("paymentCheckBox")[i].checked = false;
			document.getElementsByName("paymentCheckBox")[i].disabled = false;
			document.getElementById("allTotAmt").textContent = 0;
			document.getElementById("allCharges").value = obj;
		}
	}
}

var update_status= false;
function updateStatus(){
	update_status= true;
	window.onbeforeunload = saveBeforeExit;
}

function saveBeforeExit(){
	if (update_status) {
		var items = document.getElementsByName("_selectItems");
		for( var i=0;i<items.length;i++){
			if (items[i].checked == true && items[i].value != "item"){
				return "Edited descriptions are not effected ";
			}
		}
	}
}

function selectPageItems(){
	var itemsChk = document.getElementsByName("paymentCheckBox");
	var itemAmt = document.getElementsByName("_pendingAmount");
	var items = 0;
	var itemsTotal = 0;
	for (var i=0;i<itemsChk.length;i++){
			itemsChk[i].checked = true;
		if (itemsChk[i].checked){
			itemsTotal += getPaise(itemAmt[i]);
			items++;
		}
	}
	supplierForm.addCharge.value = items;
	return getPaiseReverse(itemsTotal);
}

function setCashPurchase(){
	if ((document.getElementById("cashPurchase").checked)){
		document.getElementById("cashPurchase").value = "Y";
	} else{
		document.getElementById("cashPurchase").value = "N";
	}
}

