var toolbar = {
View:{
		title:"View Bill",
		imageSrc: "icons/View.png",
		href: "billing/BillAction.do?_method=getCreditBillingCollectScreen",
		onclick: null,
		description: " View billed amount"
	 }
};


var doctorform = document.prescribingDrPaymentForm;
var presDocSearchForm = document.PresDoctorPaymentSearchForm;


function init(){
	doctorform = document.prescribingDrPaymentForm;
	presDocSearchForm = document.PresDoctorPaymentSearchForm;
	initDoctordept();
	presDocSearchForm._doctorName.focus();
	initTpaDialog();
	initMrNoAutoComplete(cpath);
	createToolbar(toolbar);
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
	var fdate = document.getElementById("bc_posted_date0").value;
	var tdate = document.getElementById("bc_posted_date1").value;


	if(doc==""){
		alert("Select Doctor Name");
		return false;
	}

	if (presDocSearchForm.prescribing_dr_id.value == ""){
		alert("Select Doctor Name");
		return false;
	}

	if(fdate != "" || tdate != ""){
		var msg = validateDateStr(document.getElementById("bc_posted_date0").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("bc_posted_date1").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("bc_posted_date0").value,
					document.getElementById("bc_posted_date1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}

	}

	return true;
}


var addItems = 0;
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

var deleteRows = 0;
var noOfCharges = 0;
function itemCheckedValues(checkbox, rowId){
	var docamount = 0;
	if(checkbox.checked){
		docamount = docamount+ document.getElementsByName("_doctorFees")[(rowId-1)].value;
		noOfCharges++;

		var docAmt = document.getElementsByName("_doctorFees")[(rowId-1)];
		var totAmt = document.getElementById('allTotAmt').textContent;
		var amount = getPaise(totAmt) + getElementPaise(docAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
		document.getElementsByName("_doctorFees")[(rowId-1)].readOnly = true;

	}else{
		noOfCharges--;
		var docAmt = document.getElementsByName("_doctorFees")[(rowId-1)];
		var totAmt = document.getElementById('allTotAmt').textContent;
		var amount = getPaise(totAmt) + getElementPaise(docAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
		document.getElementsByName("_doctorFees")[(rowId-1)].readOnly = false;
	}
	return noOfCharges;
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
	}else if (selectOpt == "pageItems"){
		if (document.getElementById("noOfCharges").value <= 0){
			alert("Select Items to save");
			return false;
		}else{
			return true;
		}
	}else{
		if(noOfCharges > 0){
			doctorform._noOfCharges.value=noOfCharges;

		}else{
			alert("Select Items to Save");
			return false;
		}
	}

	return true;
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
		doctorform._deleteRows.value = deleteRows;
			doctorform._method.value="deletePrescribingDrCharges";
	}else if (doctorform.deleteAll.checked){
		if (deleteCheckAll()){
			var len = doctorform.deleteCharge.length;
			for (var i=0;i<len;i++){
				doctorform._deleteRows.value= len;
				doctorform._method.value="deletePrescribingDrCharges";
			}
		}else{
			doctorform.deleteAll.checked = false;
			alert("Select items to delete");
			return false;
		}

	}else {
		alert("Select Items to delete");
		return false;
	}
	doctorform.submit();
}
function deleteCheckAll(){
	var elem = document.getElementsByName("deleteCharge");
	for (i=0;i<elem.length; i++){
		if (elem[i].checked) return true;
		else return false;
	}
}


function validateAmount(amount,fees,actFees){
	if (parseFloat(fees) > parseFloat(amount)) {
		alert("Doctor Fees should not be greater than Billed Amount");
		return false;
	}
	return true;
}

function deleteAllCharges(){
	var delRows = 0;
	var checkall= doctorform.deleteAll.checked;
	var len = doctorform.deleteCharge.length;
	if (len == undefined){
			if (checkall) {
				delRows++;
			}
		doctorform.deleteCharge.checked=checkall;
	}else{
		for (var i=0;i<len;i++){
			doctorform.deleteCharge[i].checked=checkall;
			if (doctorform.deleteCharge[i].checked)
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

		if(document.getElementById("hospDoctor").value != null) {
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
				document.getElementById("doctor").value = doc["prescribing_dr_id"];

				document.getElementById("doctorName").value = doc["doctor_name"];
				}
			}
		}
		function clearHiddenId(){
			document.getElementById("doctor").value = '';
		}
}

function getCharges(charge){
	var url = null;
	url = cpath+"/pages/payments/PrescribingDrPayment.do?_method=getChargeHeadValues&chargeHead="+charge+"&screen="+screenType;
	var ajaxObj = newXMLHttpRequest();
	getResponseHandlerText(ajaxObj, chargeHeadAutocomplete, url.toString());
}


var chargeAutoComp = null;
function chargeHeadAutocomplete(responseText){
	eval("var chargeHeadsList ="+responseText);

	if (chargeHeadsList == null){
	}else{
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
		chargeAutoComp = new YAHOO.widget.AutoComplete("chargeHeads", "chargeHeadList", oChargeSCDS);
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
		var chargeName = YAHOO.util.Dom.get("chargeHeads").value;
		for (var i in chargeHeadsList){
			var charge = chargeHeadsList[i];
			if (charge.activity_name == chargeName){
				document.getElementById("activityId").value = charge.activity_id;
			}
		}
	}
	function clearHidden(){
		 document.getElementById("activityId").value ='';
	}
}

function initTpaDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1",
			{	width:"300px",
				context : ["insurance", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('editDialogCancel', "click", handleCancel, dialog1, true);
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

function handleOk() {
	var doctorFee = document.getElementById('_dialog_doctorFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(doctorform, '_billAmount', id).value;
	if (validateAmount(billAmount, doctorFee)) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[10], doctorFee);
		setHiddenValue(doctorform, id, "_doctorFees", doctorFee);
		this.cancel();
	}
}

function openPrevious() {
	var doctorFee = document.getElementById('_dialog_doctorFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(doctorform, '_billAmount', id).value;
	if (validateAmount(billAmount, doctorFee)) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[10], doctorFee);
		setHiddenValue(doctorform, id, "_doctorFees", doctorFee);
		this.cancel();
		if (parseInt(id) != 0)
			showEditChargeDialog(document.getElementsByName('_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var doctorFee = document.getElementById('_dialog_doctorFees').value;
	var id = document.getElementById('editRowId').value;
	var billAmount = getIndexedFormElement(doctorform, '_billAmount', id).value;
	if (validateAmount(billAmount, doctorFee)) {
		var row =  getChargeRow(id);
		setNodeText(row.cells[10], doctorFee);
		setHiddenValue(doctorform, id, "_doctorFees", doctorFee);
		this.cancel();
		if (parseInt(id) != document.getElementById('payResultTable').rows.length-2) {
			showEditChargeDialog(document.getElementsByName('_editAnchor')[parseInt(id)+1]);
		}
	}
}

function handleCancel() {
	this.cancel();
}


function getTPADetails(id, billAmt, claimAmt, tpaStatus, receivedAmt, tpaName){
	hideToolBar();
	var row = findAncestor(id, "TR");
	dialog1.cfg.setProperty("context",[row.cells[8], "tr", "br"], false);
	document.getElementById("billAmt").textContent = "Bill Amount :"+billAmt;
	document.getElementById("tpaname").textContent = "TPA Name :"+tpaName;
	document.getElementById("claimAmt").textContent = "Claim Amount :"+claimAmt;
	if(tpaStatus == 'O') tpaStatus = "Open";
	else if(tpaStatus == 'S') tpaStatus = "Sent";
	else if(tpaStatus == 'R') tpaStatus = "Received";
	document.getElementById("tapRecStatus").textContent = "TPA Received Status :"+tpaStatus;
	document.getElementById("receAmt").textContent = "Received Amount :"+receivedAmt;
	dialog1.show();
}

function showEditChargeDialog(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	document.getElementById('editRowId').value = id;
	dialog1.cfg.setProperty("context", [obj, "tr", "tl"], false);
	document.getElementById("tpaname").textContent = getIndexedFormElement(doctorform, '_tpaName', id).value;
	document.getElementById("billAmt").textContent = getIndexedFormElement(doctorform, '_billTotalAmount', id).value;
	document.getElementById("claimAmt").textContent = getIndexedFormElement(doctorform, '_actualClaimAmount', id).value;
	var tpaStatus = getIndexedFormElement(doctorform, '_claimStatus', id).value;
	if (tpaStatus == 'O') tpaStatus = "Open";
	else if (tpaStatus == 'S') tpaStatus = "Sent";
	else if (tpaStatus == 'R') tpaStatus = "Received";
	document.getElementById("tapRecStatus").textContent = tpaStatus;
	document.getElementById("receAmt").textContent = getIndexedFormElement(doctorform, '_claimReceivedAmount', id).value;
	dialog1.show();
	document.getElementById('_dialog_doctorFees').value = getIndexedFormElement(doctorform, '_doctorFees', id).value;
	var checkbox = getIndexedFormElement(doctorform, 'statusCheck', id);
	if (checkbox.checked) {
		document.getElementById('_dialog_doctorFees').readOnly = true;
	} else {
		document.getElementById('_dialog_doctorFees').readOnly = false;
	}
	return false;
}

function onCheckRadio(obj){
	var statEle =  document.getElementsByName("statusCheck");
	for (i=0;i<statEle.length;i++){
		if (obj =="all"){
			noOfCharges = 0;
			document.getElementsByName("statusCheck")[i].checked=true;
			document.getElementsByName("statusCheck")[i].disabled=true;
			document.getElementById("doctorFees"+(i+1)).value=document.getElementById("originalDoctorFees"+(i+1)).value;
			doctorform._allCharges.value = obj;
			document.getElementById('allTotAmt').textContent = totalAmount;
		}else if(obj == "pageItems"){
			amount = selectPageItemsTotal();
			document.getElementsByName("statusCheck")[i].checked=true;
			document.getElementsByName("statusCheck")[i].disabled=false;
			document.getElementById('allTotAmt').textContent = amount;
			doctorform._allCharges.value = obj;

		}else{
			document.getElementsByName("statusCheck")[i].checked=false;
			document.getElementsByName("statusCheck")[i].disabled=false;
			document.getElementById('allTotAmt').textContent = 0;
			doctorform._allCharges.value = obj;
		}
	}
}


function onPostPayments(){

	if (validatePayments()){

		var elm = document.forms['PresDoctorPaymentSearchForm'].visit_type;
		var visitType = {};
		for (var i =0; i<elm.length; i++){
			if (elm[i].checked){
				visitType[i] = makeHidden("visit_type","visit_type"+i, elm[i].value);
				document.forms['prescribingDrPaymentForm'].appendChild(visitType[i]);
			}
		}
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("visit_type@op", "","in"));

		var cgElem = document.forms['PresDoctorPaymentSearchForm'].charge_group;
		var cgGroup = {};
		for (var j=0;j<cgElem.length; j++){
			if (cgElem[j].checked){
				cgGroup[j] = makeHidden("charge_group","charge_group"+j, cgElem[j].value);
				document.forms['prescribingDrPaymentForm'].appendChild(cgGroup[j]);
			}
		}
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("charge_group@op", "", "in"));

		var dateElem = document.forms['PresDoctorPaymentSearchForm'].bc_posted_date;
		for (var d=0;d<dateElem.length;d++){
			document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("bc_posted_date","bc_posted_date"+d,
						dateElem[d].value));
		}
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("bc_posted_date@op","","ge,le"));
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("bc_posted_date@type","","date"));
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("bc_posted_date@cast","","y"));

		var insElem = document.forms['PresDoctorPaymentSearchForm'].insurancestatus;
		for (var e=0;e<insElem.length;e++){
			if (insElem[e].checked){
				document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("insurancestatus","insurancestatus"+e,
							insElem[e].value));
			}
		}
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden("insurancestatus@op","", "in"));

		var chargeHead = makeHidden("chargehead_id","chargehead_id",
				document.forms['PresDoctorPaymentSearchForm'].chargehead_id.value);

		var description = makeHidden("act_description","act_description",
				document.forms['PresDoctorPaymentSearchForm'].act_description.value);

		document.forms['prescribingDrPaymentForm'].appendChild(chargeHead);
		document.forms['prescribingDrPaymentForm'].appendChild(description);

		var billElem = document.forms['PresDoctorPaymentSearchForm'].bill_status;
		var billStatusArray = [];
		for (var b=0; b<billElem.length; b++) {
			if (billElem[b].checked) {
				billStatusArray[b] = makeHidden('bill_status', 'bill_status', billElem[b].value);
				document.forms['prescribingDrPaymentForm'].appendChild(billStatusArray[b]);
			}
		}
		document.forms['prescribingDrPaymentForm'].appendChild(makeHidden('bill_status@op', '', 'in'));

		document.forms['prescribingDrPaymentForm'].submit();

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
			if (items[i].checked == true && items[i].value == "all"){
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
