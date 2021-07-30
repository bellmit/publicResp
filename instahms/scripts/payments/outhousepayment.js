toolbar = {
View :{
	title: "View Bill",
	imageSrc: "icons/View.png",
	href: "billing/BillAction.do?_method=getCreditBillingCollectScreen",
	onclick: null,
	description: "View bill"
	}
};


var ohform = document.outhousetestForm;
var ohSearchform = document.outhouseSearchForm;

function init(){
	ohform = document.outhousetestForm;
	ohSearchform = document.outhouseSearchForm;
	outhouseAutoComplete();
	ohSearchform._outhouseName.focus();
	initMrNoAutoComplete(cpath);
	createToolbar(toolbar);
	initAmountDialog();
}



function getOuthouseCharges(){
	var oh = document.getElementById("outhouseName").value;
	var fdate = document.getElementById("finalized_date0").value;
	var tdate = document.getElementById("finalized_date1").value;

	if(oh==""){
		alert("Select Outsource Name");
		return false;
	}

	if (ohSearchform.outhouse_id.value == ""){
		alert("Select Outsource Name");
		return false;
	}

	if(fdate != "" || tdate != ""){
		var msg = validateDateStr(document.getElementById("finalized_date0").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("finalized_date1").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("finalized_date0").value,document.getElementById("finalized_date1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}

	}
	return true;
}

addItems=0;
function editOhPayment(checkbox, rowId){
	if (itemSelectOption() == "pageItems"){
		addItems = document.getElementById("noOfCharges").value;
		if (checkbox.checked){
			addItems++;
		}else{
			addItems--;
		}
		document.getElementById("noOfCharges").value = addItems;
		var item =  itemCheckedValues(checkbox, rowId);
	}else{
		document.getElementById("noOfCharges").value =  itemCheckedValues(checkbox, rowId);
	}
}

var noOfCharges = 0;
var deleteRows =0;
function  itemCheckedValues(checkbox, rowId){
	var ohamount = 0;
	if (checkbox.checked) {
		ohamount = ohamount + document.getElementsByName("_ohPayment")[(rowId-1)].value;
        if (ohamount == 0) {
	        alert("Zero charges cannot be selected payment");
	        checkbox.checked = false;
        } else {
        	var ohAmt = document.getElementsByName("_ohPayment")[(rowId-1)];
			var totAmt =  document.getElementById("allTotAmt").textContent;
			var amount = getPaise(totAmt) + getElementPaise(ohAmt);
			document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
        	noOfCharges++;
        }
	} else {
		var ohAmt = document.getElementsByName("_ohPayment")[(rowId-1)];
		var totAmt =  document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totAmt) - getElementPaise(ohAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
		noOfCharges--;
	}
	return noOfCharges;
}

function itemSelectOption(){
	var selectItems = document.getElementsByName("_selectItems");
	var selectValue;
	for (var i=0;i<selectItems.length;i++){
		if (selectItems[i].checked){
				selectValue = selectItems[i].value;
		}
	}
	return selectValue;
}

function saveCharges(){
	if (validateOhPayments()){
		var dateElem = document.outhouseSearchForm.finalized_date;
		for (var d=0;d<dateElem.length;d++){
			document.outhousetestForm.appendChild(makeHidden("finalized_date","finalized_date"+d,
						dateElem[d].value));
		}
		document.outhousetestForm.appendChild(makeHidden("finalized_date@op","finalized_date@op","ge,le"));
		document.outhousetestForm.appendChild(makeHidden("finalized_date@type","","date"));
		document.outhousetestForm.appendChild(makeHidden("finalized_date@cast","","y"));

		ohform.submit();
		return true;
	}else{
		return false;
	}
}

function validateOhPayments(){
	var selectOption = document.getElementById("allCharges").value;
	if (selectOption == "all"){
		return true;
	}else if (selectOption == "pageItems"){
		if (document.getElementById("noOfCharges").value <=0){
			alert("Select Items to save");
			return false;
		}else{
			return true;
		}
	}else{
		if (noOfCharges<=0){
			alert("Select Item to Save");
			return false;
		}else{
			ohform._noOfCharges.value=noOfCharges;
			return true;
		}
	}
	var checkboxes = document.getElementsByName("statusCheck");
	for (var i =0; i<checkboxes.length; i++) {
		if (checkboxes[i].checked) {
			if (document.getElementsByName("_ohPayment")[i].value == 0) {
				alert("Zero charges cannot be selected payment");
				return false;
			}
		}
	}
}

function saveCheckAll(){
	var elem = document.getElementsByName("statusCheck");
	for (i=0;i<elem.length;i++){
		if (elem[i].checked) return true;
		else return false;
	}
}
function deleteDoctorCharge(checkBox,rowno){
	if(checkBox.checked){
		deleteRows++;
	}else{
			deleteRows--;
	}

}


function deleteCharges(){
	if(deleteRows > 0){
		ohform._deleteRows.value = deleteRows;
		ohform._method.value="deleteOhCharge";
	}else if (ohform.deleteAll.checked){
		if (deleteCheckAll()){
			var len = ohform.deleteCharge.length;
			for (var i=0;i<len;i++){
				ohform.deleteRows.value= len;
				ohform._method.value="deleteOhCharge";
			}
		}else {
			ohform.deleteAll.checked=false;
			alert("Select Items to Delete");
			return false;
		}
	}else {
		alert("Select Items to Delete");
		return false;
	}
	parameters();
	ohform.submit();
}

function deleteCheckAll(){
	var elem = document.getElementsByName("deleteCharge");
	for (i=0;i<elem.length; i++){
		if(elem[i].checked) return true;
		else return false;
	}
}

function validateAmount(amount,fees,actFees){
	if (parseFloat(fees.value)>parseFloat(amount)) {
		alert("out house payment should not be greater than Billed Amount");
		return false;
	}
	return true;
}

function checkAllCharges(){
	var addRows = 0 ;
		var checkall= ohform.checkAll.checked;
		var len = ohform.statusCheck.length;

		if (len == undefined){
				if (checkall){
					addRows++;
				}
			ohform.statusCheck.checked=checkall;
		}else{
			for (var i=0;i<len;i++){
				ohform.statusCheck[i].checked=checkall;
				if (ohform.statusCheck[i].checked)
					addRows++;
				else
					addRows--;
			}
		}
		if (addRows > 0)
			 noOfCharges = addRows;
		else
		    noOfCharges = 0;
}

function deleteAllCharges(){
	var delRows = 0;
	var checkall= ohform.deleteAll.checked;
	var len = ohform.deleteCharge.length;

	if (len == undefined){
			if (checkall){
				delRows++;
			}
		ohform.deleteCharge.checked=checkall;
	}else{
		for (var i=0;i<len;i++){
			ohform.deleteCharge[i].checked=checkall;
			if (ohform.deleteCharge[i].checked)
				delRows++;
			else
				delRows--;
		}
	}
	if (delRows > 0)
		deleteRows = delRows;
	else
		deleteRows = 0;
}
var ohAutoComp = null;
function outhouseAutoComplete(){
	for (var i in outhouseList){
		var oh = outhouseList[i];
		if(document.getElementById("outhouseId").value == oh.OH_ID){
			document.getElementById("outhouseName").value = oh.OH_NAME;
		}
	}
	var outhouseName = "outhouseName";
	var outhouseContainer = "outhouseList";

	if (ohAutoComp == null){
		var ohArray = [];
		if (outhouseList != null){
			for (var i in outhouseList){
				var ohlist = outhouseList[i];
				var len = ohArray.length;
				ohArray.length = len+1;
				ohArray[len] = ohlist.OH_NAME;
			}
		}
	this.oDoc = new YAHOO.widget.DS_JSArray(ohArray);
	ohAutoComp = new YAHOO.widget.AutoComplete(outhouseName, outhouseContainer, this.oDoc);
	ohAutoComp.preheighlightClassName = "yui-ac-preheighlight";
	ohAutoComp.typeAHead = true;
	ohAutoComp.useShadow = false;
	ohAutoComp.allowBrowserAutocomplete = false;
	ohAutoComp.autoHighlight = true;
	ohAutoComp.minQueryLength = 0;
	ohAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	ohAutoComp.formatResult = Insta.autoHighlightWordBeginnings;

	ohAutoComp.itemSelectEvent.subscribe(getOuthouseId);
	}

	function getOuthouseId(){
		var ohName = YAHOO.util.Dom.get(outhouseName).value;
		for (var i in outhouseList){
			var oh = outhouseList[i];
			if (ohName == oh.OH_NAME) {
				document.getElementById("outhouseId").value = oh.OH_ID;
			}
		}
	}
}

function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}


function parameters(){

	var dateElem = document.outhouseSearchForm.finalized_date;
	for (var d=0;d<dateElem.length;d++){
		document.outhousetestForm.appendChild(makeHidden("_finalized_date","_finalized_date"+d,
					dateElem[d].value));
	}
	document.outhousetestForm.appendChild(makeHidden("_finalized_date@op","_finalized_date@op","ge,le"));

}

function initAmountDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1",
			{	width:"300px",
				context : ["insurance", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
			} );
	YAHOO.util.Event.addListener('editDialogCancel', 'click', handleCancel, dialog1, true);
	YAHOO.util.Event.addListener('editOk', 'click', handleOk, dialog1, true);
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
	var obj = getIndexedFormElement(mainform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}

function getChargeRow(i) {
	i = parseInt(i);
	var table = document.getElementById("paymentTable");
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
	var ohFee = document.getElementById('_dialog_ohFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(ohform, '_billAmount', id).value;
	if (validateAmount(billAmount, document.getElementById('_dialog_ohFees'))) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[7], ohFee);
		setHiddenValue(ohform, id, "_ohPayment", ohFee);
		this.cancel();
	}
}

function openPrevious() {
	var ohFee = document.getElementById('_dialog_ohFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(ohform, '_billAmount', id).value;
	if (validateAmount(billAmount, document.getElementById('_dialog_ohFees'))) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[7], ohFee);
		setHiddenValue(ohform, id, "_ohPayment", ohFee);
		this.cancel();
		if (parseInt(id) != 0)
			showEditChargeDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var ohFee = document.getElementById('_dialog_ohFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(ohform, '_billAmount', id).value;
	if (validateAmount(billAmount, document.getElementById('_dialog_ohFees'))) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[7], ohFee);
		setHiddenValue(ohform, id, "_ohPayment", ohFee);
		this.cancel();
		if (parseInt(id) != document.getElementById('paymentTable').rows.length-2) {
			showEditChargeDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
		}
	}
}



function showEditChargeDialog(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	document.getElementById('editRowId').value = id;
	dialog1.cfg.setProperty("context", [obj, "tr", "tl"], false);
	dialog1.show();
	document.getElementById('_dialog_ohFees').value = getIndexedFormElement(ohform, '_ohPayment', id).value;
	var checkbox = getIndexedFormElement(ohform, 'statusCheck', id);
	if (checkbox.checked) {
		document.getElementById('_dialog_ohFees').readOnly = true;
	} else {
		document.getElementById('_dialog_ohFees').readOnly = false;
	}
	return false;
}

var update_status= false;
function updateStatus(){
	update_status= true;
	window.onbeforeunload = saveBeforeExit;
}

function saveBeforeExit(){
	if (update_status) {
		return "Edited doctor charges are not effected ";
	}
}

function onCheckRadio(obj){
	var statEle =  document.getElementsByName("statusCheck");
	var amount;
	if (obj =="all"){
		var ohId = document.getElementById("outhouseId").value;
		var mrno = document.getElementById("mrno").value;
		var fromDate = document.getElementById("finalized_date0").value;
		var toDate = document.getElementById("finalized_date1").value;
		var url =cpath+'/pages/payments/OuthouseTestsPayment.do?_method=outhouseTotalAmount&outsource_dest='+ohId+'&mr_no='+mrno+'&_fromDate='+fromDate+'&_toDate='+toDate;

		var reqObj = newXMLHttpRequest();
		reqObj.open("POST", url.toString(), false);
		reqObj.send(null);
		if (reqObj.readyState == 4){
			if ( (reqObj.status == 200) && (reqObj.responseText)){
				amount = eval(reqObj.responseText);
			}
		}
	}

	for (i=0;i<statEle.length;i++){
		if  (obj =="all") {
			noOfCharges = 0;
			document.getElementsByName("statusCheck")[i].checked=true;
			document.getElementsByName("statusCheck")[i].disabled=true;
			document.getElementById('allTotAmt').textContent = amount;
			document.getElementById("allCharges").value = obj;
		}else if(obj == "pageItems"){
			amount = selectPageItemsTotal();
			document.getElementsByName("statusCheck")[i].checked=true;
			document.getElementsByName("statusCheck")[i].disabled=false;
			document.getElementById('allTotAmt').textContent = amount;
			document.getElementById("allCharges").value = obj;
		}else{
			document.getElementsByName("statusCheck")[i].checked=false;
			document.getElementsByName("statusCheck")[i].disabled=false;
			document.getElementById('allTotAmt').textContent = 0;
			document.getElementById("allCharges").value = obj;
		}
	}
}

function selectPageItemsTotal(){
	var itemsChk = document.getElementsByName("statusCheck");
	var itemAmt = document.getElementsByName("_ohPayment");
	var items = 0;
	var itemsTotal = 0;
	for (var i=0;i<itemsChk.length;i++){
		itemsChk[i].checked = true;
		if (itemsChk[i].checked){
			itemsTotal += getPaise(itemAmt[i].value);
			items++;
		}
	}
	document.getElementById("noOfCharges").value = items;
	return getPaiseReverse(itemsTotal);

}
