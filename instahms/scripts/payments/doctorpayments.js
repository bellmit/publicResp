
var doctorform = document.DoctorPaymentSearchForm;
var docpayform = document.doctorPaymentForm;
var toolbar = {
View : {
title :	 "View Bill" ,
		 imageSrc: "icons/View.png",
		 href: "billing/BillAction.do?_method=getCreditBillingCollectScreen",
		 onclick: null,
		 description: "View then Bill for details of payments"
	   }
};


function init(){
	doctorform = document.DoctorPaymentSearchForm;
	docpayform = document.doctorPaymentForm;
	initDoctordept();
	doctorform._doctorName.focus();
	createToolbar(toolbar);
	initTpaDialog();
	initMrNoAutoComplete(cpath);
}
function enableAll(){
	doctorform.cgAll.checked = true;
}

function enableChargeGroup(){

	var disabled =  document.getElementById("cgAll").checked;
	var groups = document.getElementsByName("charge_group");
	for(var i=0; i< groups.length; i++) {
		if( groups[i].value != "")
			groups[i].disabled = disabled;
	}
}

function onKeyPressMrno(e) {
	if (isEventEnterOrTab(e)) {
		return onChangeMrno();
	} else {
		return true;
	}
}

function onChangeMrno() {
	var mrnoBox =  doctorform.mrno;

	var valid = addPrefix(mrnoBox, gMrNoPrefix, gMrNoDigits);

	if (!valid) {
		alert("Invalid MR No. Format");
		doctorform.mrno.value = ""
			doctorform.mrno.focus();
		return false;
	}
}

function getDoctorCharges(){
	var doc=document.getElementById("doctor").value;
	var fin_fDate = document.getElementById("finalized_date0").value;
	var fin_tDate = document.getElementById("finalized_date1").value;

	var closed_fDate = document.getElementById("closed_date0").value;
	var closed_tDate = document.getElementById("closed_date1").value;

	if(doc==""){
		alert("Select Doctor Name");
		return false;
	}

	if (doctorform.payee_doctor_id.value == ""){
		alert("Select Dcotor Name");
		return false;
	}

	if(fin_fDate != "" || fin_tDate != ""){
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
	if(closed_fDate != "" || closed_tDate != ""){
		var msg = validateDateStr(document.getElementById("closed_date0").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("closed_date1").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("closed_date0").value,document.getElementById("closed_date1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}

	}
	return true;
}


addItems=0;
function editDoctorFees(checkbox,rowId){
	if (itemSelectOption() == "pageItems"){
		addItems = document.getElementById("noOfCharges").value;
		if (checkbox.checked){
			addItems++;
		}else{
			addItems--;
		}
		document.getElementById("noOfCharges").value = addItems;
		var items = itemCheckedValues(checkbox, rowId);
	}else{
		document.getElementById("noOfCharges").value = itemCheckedValues(checkbox, rowId);
	}
}

var deleteRows =0;
var noOfCharges = 0;
function itemCheckedValues(checkbox, rowId){
	var docamount = 0;
	if (checkbox.checked) {
		docamount = docamount+ document.getElementsByName("_doctorFees")[(rowId-1)].value;

		noOfCharges++;
		var docAmt = document.getElementsByName("_doctorFees")[(rowId-1)];
		var totAmt = document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totAmt) + getElementPaise(docAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);

	} else {
		noOfCharges--;
		var docAmt = document.getElementsByName("_doctorFees")[(rowId-1)];
		var totAmt = document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totAmt) - getElementPaise(docAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
	}
	return noOfCharges;
}



function validateAmount(amount, fees, actFees){
	if (parseFloat(fees) > parseFloat(amount)) {
		alert("Doctor Fees should not be greater than Billed Amount");
		return false;
	}
	return true;
}

function validatePayments(){
	var doc = document.getElementById("doctor").value;
	if(doc==""){
		alert("Select Doctor Name");
		return false;
	}

	var selectOpt = document.getElementById("allCharges").value;
	if (selectOpt == "all"){
		return true;
	}else if(selectOpt == "pageItems"){
		var chkLen = document.getElementById("noOfCharges").value;
		if (chkLen <=0){
			alert("Select Items to Save");
			return false;
		}else{
			return true;
		}
	}else{
		if(noOfCharges > 0){
			document.getElementById("noOfCharges").value=noOfCharges;

		}else{
			alert("Select items to save");
			return false;
		}
	}

	return true;

}

function saveCheckAll(){
	var elem = document.getElementsByName("statusCheck");
	for (i=0;i<elem.length; i++){
		if(elem[i].checked)	return true;
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
	var doc = document.getElementById("doctor").value;

	if(doc==""){
		alert("Select Doctor Name");
		return false;
	}
	if(deleteRows > 0){
		document.getElementById("deleteRows").value = deleteRows;
		document.doctorPaymentForm._method.value="deleteDoctorCharge";
	}else if (document.doctorPaymentForm.deleteAll.checked){
		if (deleteAll()){
			var len = document.doctorPaymentForm.deleteCharge.length;
			for (var i=0;i<len;i++){
				document.getElementById("deleteRows").value= len;
				document.doctorPaymentForm._method.value="deleteDoctorCharge";
			}
		}else{
			document.doctorPaymentForm.deleteAll.checked=false;
			alert("Select item to Delete");
			return false;
		}
	}else {
		alert("Select Items to Delete");
		return false;
	}
	update_status = false;
	document.doctorPaymentForm.submit();
}

function deleteAll(){
	var elem  = document.getElementsByName("deleteCharge");
	for (i=0;i<elem.length;i++){
		if (elem[i].checked) return true;
		else return false;
	}
}

function deleteAllCharges(){

	var delRows = 0;
	var checkall= document.doctorPaymentForm.deleteAll.checked;
	var len = document.doctorPaymentForm.deleteCharge.length;

	if (len == undefined){
		if (checkall) {
			delRows++;
		}
		document.doctorPaymentForm.deleteCharge.checked=checkall;
	}else{
		for (var i=0;i<len;i++){
			document.doctorPaymentForm.deleteCharge[i].checked=checkall;
			if (document.doctorPaymentForm.deleteCharge[i].checked)
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

var docAutoComp = null;
function initDoctordept(){

	if (docAutoComp != null) {
			docAutoComp.destroy();
		}

		var docDeptNameArray = [];
		var jDeptDocList = null;
		var doctors = "hospDoctor";
		var doctorContainer = "hospDoctorlist";

		if(jDocDeptNameList !=null && jDocDeptNameList.length > 0) {
			jDeptDocList = jDocDeptNameList;

			docDeptNameArray.length = jDeptDocList.length;

			for ( i=0 ; i< jDeptDocList.length; i++){
				var item = jDeptDocList[i];
				if (item["doctor_license_number"]!=null && item["doctor_license_number"]!="") {
					docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")"+"("+item["doctor_license_number"]+")";
				} else {
					docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")";
				}
			}
		}

		if(doctorform.hospDoctor.value != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(docDeptNameArray);
			docAutoComp = new YAHOO.widget.AutoComplete(doctors, doctorContainer, dataSource);
			docAutoComp.maxResultsDisplayed = 10;
			docAutoComp.queryMatchContains = true;
			docAutoComp.allowBrowserAutocomplete = false;
			docAutoComp.formatResult = Insta.autoHighlight;
			docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			docAutoComp.typeAhead = false;
			docAutoComp.useShadow = false;
			docAutoComp.minQueryLength = 0;
			docAutoComp.forceSelection = true;
			docAutoComp._bItemSelected = true;
			docAutoComp.itemSelectEvent.subscribe(getDoctorId);
			docAutoComp.selectionEnforceEvent.subscribe(clearHiddenId);
			}
		}
		function getDoctorId(){
		var doctorName = YAHOO.util.Dom.get(doctors).value;
		var doctorId = document.getElementById("doctor").value;
		for (var i in jDeptDocList){
			var doc = jDeptDocList[i];
			if (doc["doctor_name"]+" ("+doc["dept_name"]+")" == doctorName ||
				doc["doctor_name"]+" ("+doc["dept_name"]+")"+"("+doc["doctor_license_number"]+")" == doctorName){
				doctorform.payee_doctor_id.value = doc["doctor_id"];

				document.getElementById("doctorName").value = doc["doctor_name"];
				}
			}
		}
		function clearHiddenId(){
			doctorform.payee_doctor_id.value = '';
		}
}


function getCharges(charge){
	var url = null;
	url = cpath+"/pages/payments/DoctorPayments.do?_method=getChargeHeadValues&chargeHead="+charge+"&screen="+screenType;

	var ajaxObj = newXMLHttpRequest();
	getResponseHandlerText(ajaxObj, chargeHeadAutocomplete, url.toString());
}
var chargeAutoComp = null;
function chargeHeadAutocomplete(responseText){
	eval("var chargeHeadsList ="+responseText);
	if (chargeHeadsList == null){
		//	alert("no values");
	}else{
		var charges =  "chargeHeads";
		var chargeContainer = "chargeHeadlist";
		if (chargeAutoComp != null){
			chargeAutoComp.destroy();
		}
		var chargeArray = [];
		if(chargeHeadsList != null){
			for (var i  in chargeHeadsList){
				var ch  = chargeHeadsList[i];
				var len = chargeArray.length;
				chargeArray.length = len+1;
				chargeArray[len] = ch.activity_name;
			}
		}

		oChargeSCDS = new YAHOO.widget.DS_JSArray(chargeArray);
		chargeAutoComp = new YAHOO.widget.AutoComplete(charges, chargeContainer, oChargeSCDS);
		chargeAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		chargeAutoComp.typeAhead = true;
		chargeAutoComp.useShadow = false;
		chargeAutoComp.allowBrowserAutocomplete = false;
		chargeAutoComp.autoHighlight = true;
		chargeAutoComp.minQueryLength=0;
		chargeAutoComp.forceSelection = true;

		chargeAutoComp.itemSelectEvent.subscribe(getChargeId);
		chargeAutoComp.selectionEnforceEvent.subscribe(clearHidden);
	}

	function getChargeId(){
		var chargeName = YAHOO.util.Dom.get(charges).value;
		var chargeId = document.getElementById("activityId").value;
		for (var i in chargeHeadsList){
			var charge = chargeHeadsList[i];
			if (charge.activity_name == chargeName){
				 document.getElementById("activityId").value = charge.activity_id;
			}
		}
	}

	function clearHidden(){
		 document.getElementById("activityId").value = '';
	}
}

function initTpaDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1", {
			width:"300px",
			context : ["insurance", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
		} );
	YAHOO.util.Event.addListener('editDialogOk', "click", insertAmount, dialog1, true);
	YAHOO.util.Event.addListener('editDialogCancel', "click", handleCancel, dialog1, true);
	YAHOO.util.Event.addListener('editDialogPrevious', 'click', openPrevious, dialog1, true);
	YAHOO.util.Event.addListener('editDialogNext', 'click', openNext, dialog1, true);
	dialog1.render();
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
	var table = document.getElementById("payResultTable");
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

function openPrevious() {
	var doctorFee = document.getElementById('_dialog_doctorFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(docpayform, '_billAmount', id).value;
	if (validateAmount(billAmount, doctorFee)) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[10], doctorFee);
		setHiddenValue(docpayform, id, "_doctorFees", doctorFee);
		this.cancel();
		if (parseInt(id) != 0)
			showEditChargeDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var doctorFee = document.getElementById('_dialog_doctorFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(docpayform, '_billAmount', id).value;
	if (validateAmount(billAmount, doctorFee)) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[10], doctorFee);
		setHiddenValue(docpayform, id, "_doctorFees", doctorFee);
		this.cancel();
		if (parseInt(id) != document.getElementById('payResultTable').rows.length-2) {
			showEditChargeDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
		}
	}
}

function handleCancel() {
	this.cancel();
}

function showEditChargeDialog(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	document.getElementById('editRowId').value = id;
	dialog1.cfg.setProperty("context", [obj, "tr", "tl"], false);
	document.getElementById("tpaname").textContent = getIndexedFormElement(docpayform, '_tpaName', id).value;
	document.getElementById("billAmt").textContent = getIndexedFormElement(docpayform, '_billTotalAmount', id).value;
	document.getElementById("claimAmt").textContent = getIndexedFormElement(docpayform, '_actualClaimAmount', id).value;
	var tpaStatus = getIndexedFormElement(doctorPaymentForm, '_claimStatus', id).value;
	if(tpaStatus == 'O') tpaStatus = "Open";
	else if(tpaStatus == 'S') tpaStatus = "Sent";
	else if(tpaStatus == 'R') tpaStatus = "Received";
	document.getElementById("tapRecStatus").textContent = tpaStatus;
	document.getElementById("receAmt").textContent = getIndexedFormElement(docpayform, '_claimReceivedAmount', id).value;
	dialog1.show();
	document.getElementById('_dialog_doctorFees').value = getIndexedFormElement(docpayform, '_doctorFees', id).value;
	var checkbox = getIndexedFormElement(docpayform, 'statusCheck', id);
	if (checkbox.checked) {
		document.getElementById('_dialog_doctorFees').readOnly = true;
	} else {
		document.getElementById('_dialog_doctorFees').readOnly = false;
	}
	return false;
}


function onCheckRadio(obj){
	var statEle =  document.getElementsByName("statusCheck");
	for (i=0;i<statEle.length;i++) {
		if (obj =="all") {
			noOfCharges = 0;
			document.getElementsByName("statusCheck")[i].checked = true;
			document.getElementsByName("statusCheck")[i].disabled = true;
			document.getElementById("doctorFees"+(i+1)).value = document.getElementById("originalDoctorFees"+(i+1)).value;
			document.getElementById("allTotAmt").textContent = 0;
			document.getElementById("allCharges").value = obj;
			document.getElementById("allTotAmt").textContent = totalAmount;


		}else if(obj == "pageItems"){
			amount = selectPageItemsTotal();
			document.getElementsByName("statusCheck")[i].disabled = false;
			document.getElementsByName("statusCheck")[i].checked = true;
			document.getElementById("allTotAmt").textContent = amount;
			document.getElementById("allCharges").value = obj;
		} else {
			document.getElementsByName("statusCheck")[i].disabled = false;
			document.getElementsByName("statusCheck")[i].checked = false;
			document.getElementById("allTotAmt").textContent = 0;
			document.getElementById("allCharges").value = obj;
		}
	}
}

function onPostPayments(){

	if (validatePayments()){

		var elm = document.forms['DoctorPaymentSearchForm'].visit_type;
		var visitType = {};
		for (var i =0; i<elm.length; i++){
			if (elm[i].checked){
				visitType[i] = makeHidden("visit_type","visit_type"+i, elm[i].value);
				document.forms['doctorPaymentForm'].appendChild(visitType[i]);
			}
		}
		document.forms['doctorPaymentForm'].appendChild(makeHidden("visit_type@op", "","in"));

		var cgElem = document.forms['DoctorPaymentSearchForm'].charge_group;
		var cgGroup = {};
		for (var j=0;j<cgElem.length; j++){
			if (cgElem[j].checked){
				cgGroup[j] = makeHidden("charge_group","charge_group"+j, cgElem[j].value);
				document.forms['doctorPaymentForm'].appendChild(cgGroup[j]);
			}
		}
		document.forms['doctorPaymentForm'].appendChild(makeHidden("charge_group@op", "", "in"));

		var dateElem = document.forms['DoctorPaymentSearchForm'].finalized_date;
		for (var d=0;d<dateElem.length;d++){
			document.forms['doctorPaymentForm'].appendChild(makeHidden("finalized_date","finalized_date"+d,
						dateElem[d].value));
		}
		document.forms['doctorPaymentForm'].appendChild(makeHidden("finalized_date@op","","ge,le"));
		document.forms['doctorPaymentForm'].appendChild(makeHidden("finalized_date@type","","date"));
		document.forms['doctorPaymentForm'].appendChild(makeHidden("finalized_date@cast","","y"));

		var dateElem = document.forms['DoctorPaymentSearchForm'].closed_date;
		for (var d=0;d<dateElem.length;d++){
			document.forms['doctorPaymentForm'].appendChild(makeHidden("closed_date","closed_date"+d,
						dateElem[d].value));
		}
		document.forms['doctorPaymentForm'].appendChild(makeHidden("closed_date@op","","ge,le"));
		document.forms['doctorPaymentForm'].appendChild(makeHidden("closed_date@type","","date"));
		document.forms['doctorPaymentForm'].appendChild(makeHidden("closed_date@cast","","y"));

		var insElem = document.forms['DoctorPaymentSearchForm'].insurancestatus;
		for (var e=0;e<insElem.length;e++){
			if (insElem[e].checked){
				document.forms['doctorPaymentForm'].appendChild(makeHidden("insurancestatus","insurancestatus"+e,
							insElem[e].value));
			}
		}
		document.forms['doctorPaymentForm'].appendChild(makeHidden("insurancestatus@op","", "in"));

		var chargeHead = makeHidden("chargehead_id","chargehead_id",
				document.forms['DoctorPaymentSearchForm'].chargehead_id.value);

		var description = makeHidden("act_description","act_description",
				document.forms['DoctorPaymentSearchForm'].act_description.value);

		document.forms['doctorPaymentForm'].appendChild(chargeHead);
		document.forms['doctorPaymentForm'].appendChild(description);

		var billElem = document.forms['DoctorPaymentSearchForm'].bill_status;
		var billStatusArray = [];
		for (var b=0; b<billElem.length; b++) {
			if (billElem[b].checked) {
				billStatusArray[b] = makeHidden('bill_status', 'bill_status', billElem[b].value);
				document.forms['doctorPaymentForm'].appendChild(billStatusArray[b]);
			}
		}
		document.forms['doctorPaymentForm'].appendChild(makeHidden('bill_status@op', '', 'in'));

		update_status = false;
		document.forms['doctorPaymentForm'].submit();

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
				return "Edited doctor charges are not effected ";
			}
		}
	}
}

function itemSelectOption(){
	var selectItems = document.getElementsByName("_selectItems");
	var selectValue;
	for (var i=0;i<selectItems.length; i++){
		if (selectItems[i].checked){
			selectValue = selectItems[i].value;
		}
	}
	return selectValue;
}

function selectPageItemsTotal(){
	var itemsChk = document.getElementsByName("statusCheck");
	var itemAmt = document.getElementsByName("_doctorFees");
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

function insertAmount() {
	var doctorFee = document.getElementById('_dialog_doctorFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(docpayform, '_billAmount', id).value;
	if (validateAmount(billAmount, doctorFee)) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[10], doctorFee);
		setHiddenValue(docpayform, id, "_doctorFees", doctorFee);
		this.cancel();
	}
}
