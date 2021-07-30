
var doctorform = document.referralDoctorPaymentForm;
var doctorSearchForm = document.RefDoctorPaymentSearchForm;

var toolbar = {
View : {
		title: "View Bill",
		imageSrc: "icons/View.png",
		href: "billing/BillAction.do?_method=getCreditBillingCollectScreen",
		onclik: null,
		description: "To view the bill for actuall billed amount ",
	   }
};




function init(){
	doctorform = document.referralDoctorPaymentForm;
	doctorSearchForm = document.RefDoctorPaymentSearchForm;
	refDoctorAutocomplete();
	initTpaDialog();
	initMrNoAutoComplete(cpath);
	createToolbar(toolbar);
	doctorSearchForm._doctorName.focus();
}

function enablePayments(){
	var len = doctorform._refPayments.length;
	var refPay = document.getElementById("refPayment").value;
	for (i=0;i<len;i++){
		if(doctorform._refPayments[i].value==refPay && refPay=='update'){
			doctorform._refPayments[i].checked=true;
			document.getElementById("editPaymentsDiv").style.display="none";
			setDateRangeMonth(document.forms[0].fromDate, document.forms[0].toDate);
		}
		if(doctorform._refPayments[i].value==refPay && refPay=='edit') {
			doctorform._refPayments[i].checked=true;
			document.getElementById("editPaymentsDiv").style.display="block";
		document.getElementById("updateRefpaymentsDiv").style.display="none";
		}
	}
}


function hideRefPayments(ref){
		if(ref.value=='update'){
		document.getElementById("editPaymentsDiv").style.display="none";
		document.getElementById("updateRefpaymentsDiv").style.display="block";
		document.getElementById("viewDiv").style.display="none";
		document.getElementById("refChargesDiv").style.display="none";
			setDateRangeMonth(document.forms[0].fromDate, document.forms[0].toDate);
		}

		if (ref.value=='edit') {
		document.getElementById("editPaymentsDiv").style.display="block";
		document.getElementById("updateRefpaymentsDiv").style.display="none";
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
	var fdate = document.getElementById("posted_date0").value;
	var tdate = document.getElementById("posted_date1").value;

	if(document.getElementById('refDoctors').value==""){
		alert("Select Doctor Name");
		return false;
	}

	if (doctorSearchForm.reference_docto_id.value == "") {
		alert("Select Doctor Name");
		return false;
	}

	if(fdate != "" || tdate != ""){
		var msg = validateDateStr(document.getElementById("posted_date0").value,"past");
		if (msg == null){
		}else{
			alert("From "+msg);
			return false;
		}

		var msg = validateDateStr(document.getElementById("posted_date1").value,"past");
		if (msg == null){
		}else{
			alert("To "+msg);
			return false;
		}


		if (getDateDiff(document.getElementById("posted_date0").value,document.getElementById("posted_date1").value)<0){
			alert("From date should not greater than Todate");
			return false;
		}

	}
	return true;
}


var addItems = 0;
function editDoctorFees(checkbox,rowId){
	if (selectOptions() == "pageItems"){
		addItems = document.getElementById("noOfCharges").value;
		if (checkbox.checked){
			addItems++;
		}else{
			addItems--;
		}
		document.getElementById("noOfCharges").value = addItems;
		var item = itemCheckedValues(checkbox, rowId);
	}else{
		document.getElementById("noOfCharges").value = itemCheckedValues(checkbox, rowId);
	}
}

var deleteRows =0;
var noOfCharges = 0;
function itemCheckedValues(checkbox, rowId){
	var docamount = 0;
	if(checkbox.checked){
		docamount = docamount+ document.getElementsByName("_doctorFees")[(rowId-1)].value;

		noOfCharges++;
		var docAmt = document.getElementsByName("_doctorFees")[(rowId-1)];
		var totAmt =  document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totAmt) + getElementPaise(docAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
		document.getElementsByName("_doctorFees")[(rowId-1)].readOnly = true;

	}else{
		noOfCharges--;
		var docAmt = document.getElementsByName("_doctorFees")[(rowId-1)];
		var totAmt =  document.getElementById("allTotAmt").textContent;
		var amount = getPaise(totAmt) + getElementPaise(docAmt);
		document.getElementById("allTotAmt").textContent = formatAmountPaise(amount);
		document.getElementsByName("_doctorFees")[(rowId-1)].readOnly = false;
	}
	return noOfCharges;
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


function validatePayments(){
	var doc = document.getElementById("doctor").value;
	if(doc==""){
		alert("Select Doctor Name");
		return false;
	}
	var selectOption = document.getElementById("allCharges").value;
	if (selectOption == "all"){
		return true;
	}else if (selectOption == 'pageItems'){
		var chkLen = document.getElementById("noOfCharges").value;
		if (chkLen <=0){
			alert("Select Items to Save");
			return false;
		}else{
			return true;
		}
	} else{

		if(noOfCharges > 0){
			doctorform._noOfCharges.value=noOfCharges;
		}else {
			alert("Select items to save");
			return false;
		}
	}

	return true;
}

function saveCheckAll(){
	var elem = document.getElementsByName("checkAll");
	for (i=0;i<elem.lenght;i++){
		if(elem[i].checked) return true;
		else return false;
	}
}

function validateAmount(amount,fees,actFees){
	if (parseFloat(fees) > parseFloat(amount)) {
		alert("Referal Fees should not be greater than Billed Amount");
		return false;
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
	if(deleteRows > 0){
		doctorform._deleteRows.value = deleteRows;
		doctorform._method.value="deleteDoctorCharge";
	}else if (doctorform.deleteAll.checked){
		if (deleteCheckAll()){
			len  = doctorform.deleteCharge.length;
			for(var i=0;i<len;i++){
				doctorform._deleteRows.value = len;
				doctorform._method.value="deleteDoctorCharge";
			}
		}else{
			doctorform.deleteAll.checked=false;
			alert("Select Items to Delete");
			return false;
		}
	}else{
		alert("Select Items to Delete");
		return false;
	}
	doctorform.submit();
}

function deleteCheckAll(){
	var elem = document.getElementsByName("deleteCharge");
	for (i=0;i<elem.length;i++){
		if (elem[i].checked) return true;
		else return false;
	}
}

function deleteAllCharges(){
	var delRows = 0;
	var checkall= doctorform.deleteAll.checked;
	var len = doctorform.deleteCharge.length;
	if (len == undefined){
			if(checkall){
				delRows++;
			}
		document.referralDoctorPaymentForm.deleteCharge.checked=checkall;
	}else{
		for (var i=0;i<len;i++){
			document.referralDoctorPaymentForm.deleteCharge[i].checked=checkall;
			if (document.referralDoctorPaymentForm.deleteCharge[i].checked)
				delRows++;
			else delRows--;
		}
	}
	if(delRows > 0)
		deleteRows = delRows;
	else
		deleteRows = 0;
}

var oDocAutoCom = null;
function refDoctorAutocomplete(){

	for (var i in refDoctorList){
		var refDoc = refDoctorList[i];
		if(document.getElementById("doctor").value == refDoc.DOCTOR_ID){
			document.getElementById("refDoctors").value = refDoc.DOCTOR_NAME.concat(" ("+refDoc.DOCTYPE+")");
		}
	}
	var refDoctors = "refDoctors";
	var refDoctorContainer = "refDoctorList";

	if (oDocAutoCom == null) {
		var refDoctorsArray = [];
		var refType = [];
		var refDocTypeArray = [];
		var ref = [];
		var typeLen = 0;
		if (refDoctorList != null) {
			for (var i in refDoctorList){
				var refDoctor = refDoctorList[i];
				var len = refDoctorsArray.length;
				typeLen = refType.length;
					refDoctorsArray.length = len+1;
					refType.length=typeLen+1;
					refDoctorsArray[len] = refDoctor.DOCTOR_NAME;
					refType[typeLen] = refDoctor.DOCTYPE;
					refDocTypeArray[len] = refDoctorsArray[len].concat(" ("+refType[typeLen])+")" ;
			}
		}

	this.oDoc = new YAHOO.widget.DS_JSArray(refDocTypeArray);
	oDocAutoCom = new YAHOO.widget.AutoComplete(refDoctors, refDoctorContainer, this.oDoc);
	oDocAutoCom.preheighlightClassName = "yui-ac-prehighlight";
	oDocAutoCom.typeAHead = true;
	oDocAutoCom.useShadow = false;
	oDocAutoCom.allowBrowserAutocomplete = false;
	oDocAutoCom.autoHighlight= true;
	oDocAutoCom.minQueryLength = 0;
    oDocAutoCom.filterResults = Insta.queryMatchWordStartsWith;
	oDocAutoCom.formatResult = Insta.autoHighlightWordBeginnings;

	oDocAutoCom.itemSelectEvent.subscribe(getRefDocId);
	oDocAutoCom.selectionEnforceEvent.subscribe(clearHiddenId);

	}

	function getRefDocId(){
		var refDocName = YAHOO.util.Dom.get(refDoctors).value;
		for (var i in refDoctorList){
			var refDoc = refDoctorList[i];
			if(refDoc.DOCTOR_NAME.concat(" ("+refDoc.DOCTYPE)+")" == refDocName) {
				doctorSearchForm.reference_docto_id.value = refDoc.DOCTOR_ID;
				document.getElementById("doctorName").value=refDoc.DOCTOR_NAME;
			}
		}
	}

	function clearHiddenId(){
		doctorSearchForm.reference_docto_id.value = '';
	}
}


function initTpaDialog() {
	dialog1 = new YAHOO.widget.Dialog("dialog1",
			{	width:"300px",
				context : ["insurance", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true,
			} );
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
	var checkbox = getIndexedFormElement(doctorform, '_statusCheck', id);
	if (checkbox.checked) {
		document.getElementById('_dialog_doctorFees').readOnly = true;
	} else {
		document.getElementById('_dialog_doctorFees').readOnly = false;
	}
	return false;
}

function getCharges(charge){
	var ajaxObject = newXMLHttpRequest();
	var url = null;
	url  = cpath+"/pages/payments/ReferralDoctorPayments.do?_method=getChargeHeadValues&chargeHead="+charge+"&screen=Payment";
	var ajaxObj = newXMLHttpRequest();
	getResponseHandlerText(ajaxObj, chargeHeadAutocomplete, url.toString());
}


var chargeAutoComp = null;
function chargeHeadAutocomplete(responseText){
	eval("var chargeHeadsList="+responseText);
	if (chargeHeadsList == null){
	}else{
		if (chargeAutoComp != null){
				chargeAutoComp.destroy();
		}

		var chargeArray = {result: chargeHeadsList};
		oChargeDS = new YAHOO.util.LocalDataSource(chargeArray);
		oChargeDS.responseType=YAHOO.util.LocalDataSource.TYPE_JSON;
		oChargeDS.responseSchema = {
			resultsList : "result",
			fields: [{key: "activity_name"},
					 {key: "activity_id"} ]
		};

		chargeAutoComp = new YAHOO.widget.AutoComplete("chargeHeads", "chargeHeadList", oChargeDS);
		chargeAutoComp.preheiglightClassName = "yui-ac-prehighlight";
		chargeAutoComp.typeAhead = true;
		chargeAutoComp.useShadow = true;
		chargeAutoComp.allowBrowserAutocomplete = false;
		chargeAutoComp.autoHighlight = true;
		chargeAutoComp.minQueryLength = 0;
		chargeAutoComp.forceSelection = true;
		chargeAutoComp.resultTypeList = false;

		chargeAutoComp.itemSelectEvent.subscribe(getChargeId);
		chargeAutoComp.selectionEnforceEvent.subscribe(clearHidden);
	}

	function getChargeId(){
		var chargeName = YAHOO.util.Dom.get("chargeHeads").value;
		for (var i in ChargeHeadsList){
			var charge = ChargeHeadsList[i];
			if (charge.activity_name == chargeName){
				document.getElementById("activityId").value = charge.activity_id;
			}
		}
	}

	function clearHidden(){
		document.getElementById("activityId").value = '';
	}
}

function onCheckRadio(obj){
	var statEle =  document.getElementsByName("_statusCheck");
	for (i=0;i<statEle.length;i++){
		if (obj =="all"){
			noOfCharges = 0;
			document.getElementsByName("_statusCheck")[i].checked=true;
			document.getElementsByName("_statusCheck")[i].disabled=true;
			document.getElementById("doctorFees"+(i+1)).value=document.getElementById("originalDoctorFees"+(i+1)).value;
			document.getElementById("allCharges").value = obj;
			document.getElementById("allTotAmt").textContent = totalAmount;
		}else if(obj =="pageItems"){
			amount = pageItemsTotal();
			document.getElementsByName("_statusCheck")[i].checked = true;
			document.getElementsByName("_statusCheck")[i].disabled = false;
			document.getElementById("allTotAmt").textContent = amount;
			document.getElementById("allCharges").value = obj;
		}else{
			document.getElementsByName("_statusCheck")[i].checked=false;
			document.getElementsByName("_statusCheck")[i].disabled=false;
			document.getElementById("allTotAmt").textContent=0;
			document.getElementById("allCharges").value = obj;
		}
	}
}

function pageItemsTotal(){
	var itemChk = document.getElementsByName("_statusCheck");
	var docAmount = document.getElementsByName("_doctorFees");
	var items = 0;
	var itemsTotal = 0;
	for (var i=0;i<itemChk.length;i++){
		itemChk[i].checked =  true;
		if (itemChk[i].checked){
			itemsTotal +=  getPaise(docAmount[i].value);
			items++;
		}else{
			 itemsTotal -=  getPaise(docAmount[i].value);
			 items--;
		}
	}
	document.getElementById("noOfCharges").value = items;
	return getPaiseReverse(itemsTotal);
}


function onPostPayments(){

	if (validatePayments()){

		var elm = document.forms['RefDoctorPaymentSearchForm'].visit_type;
		var visitType = {};
		for (var i =0; i<elm.length; i++){
			if (elm[i].checked){
				visitType[i] = makeHidden("visit_type","visit_type"+i, elm[i].value);
				document.forms['referralDoctorPaymentForm'].appendChild(visitType[i]);
			}
		}
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("visit_type@op", "","in"));

		var cgElem = document.forms['RefDoctorPaymentSearchForm'].charge_group;
		var cgGroup = {};
		for (var j=0;j<cgElem.length; j++){
			if (cgElem[j].checked){
				cgGroup[j] = makeHidden("charge_group","charge_group"+j, cgElem[j].value);
				document.forms['referralDoctorPaymentForm'].appendChild(cgGroup[j]);
			}
		}
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("charge_group@op", "", "in"));

		var dateElem = document.forms['RefDoctorPaymentSearchForm'].posted_date;
		for (var d=0;d<dateElem.length;d++){
			document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("posted_date","posted_date"+d,
						dateElem[d].value));
		}
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("posted_date@op","","ge,le"));
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("posted_date@type","","date"));
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("posted_date@cast","","y"));

		var insElem = document.forms['RefDoctorPaymentSearchForm'].insurancestatus;
		for (var e=0;e<insElem.length;e++){
			if (insElem[e].checked){
				document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("insurancestatus","insurancestatus"+e,
							insElem[e].value));
			}
		}
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden("insurancestatus@op","", "in"));

		var chargeHead = makeHidden("chargehead_id","chargehead_id",
				document.forms['RefDoctorPaymentSearchForm'].chargehead_id.value);

		var description = makeHidden("act_description","act_description",
				document.forms['RefDoctorPaymentSearchForm'].act_description.value);

		document.forms['referralDoctorPaymentForm'].appendChild(chargeHead);
		document.forms['referralDoctorPaymentForm'].appendChild(description);

		var billElem = document.forms['RefDoctorPaymentSearchForm'].bill_status;
		var billStatusArray = [];
		for (var b=0; b<billElem.length; b++) {
			if (billElem[b].checked) {
				billStatusArray[b] = makeHidden('bill_status', 'bill_status', billElem[b].value);
				document.forms['referralDoctorPaymentForm'].appendChild(billStatusArray[b]);
			}
		}
		document.forms['referralDoctorPaymentForm'].appendChild(makeHidden('bill_status@op', '', 'in'));

		document.forms['referralDoctorPaymentForm'].submit();

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

