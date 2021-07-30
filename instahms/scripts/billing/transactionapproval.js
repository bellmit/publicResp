
function resetStatus(){
    for(var i=1; i<5; i++)
    	document.getElementById('patientType'+i).checked = false;
    for(var i=1; i<5; i++)
    	document.getElementById('billStatus'+i).checked = false;
    for(var i=1; i<10; i++)
    	document.getElementById('chargeGroup'+i).checked = false;

   	document.getElementById('fdate').value = "";
   	document.getElementById('tdate').value = "";
	return false;
}

function validateSearch(){
	if(document.getElementById('fdate').value == "") {
		alert("From date required");
		return false;
	}
	if(document.getElementById('tdate').value == "") {
		alert("To date required");
		return false;
	}else
		return true;
}

function onEdit(checkBox,rowId){
	enableFieldsToEdit(checkBox, rowId);
	recalcTotals();
}


function enableFieldsToEdit(checkBox,rowId){
		if(checkBox.checked){
			document.getElementById('rate'+rowId).readOnly = false;
			document.getElementById('qty'+rowId).readOnly = false;
			document.getElementById('discount'+rowId).readOnly = false;
		}else{
			document.getElementById('rate'+rowId).readOnly = true;
			document.getElementById('qty'+rowId).readOnly = true;
			document.getElementById('discount'+rowId).readOnly = true;
	}
}


function onDelete(checkBox, rowId) {
	deleteSelectedRow(checkBox, rowId);
	recalcTotals();
}

function disableFields(checkBox, rowId){
	if(checkBox.checked){
		document.getElementById("rate"+rowId).readOnly= true;
		document.getElementById("qty"+rowId).readOnly = true;
		document.getElementById("discount"+rowId).readOnly = true;
	}else{
		document.getElementById("rate"+rowId).readOnly= false;
		document.getElementById("qty"+rowId).readOnly = false;
		document.getElementById("discount"+rowId).readOnly = false;
	}
}

function deleteSelectedRow(checkBox,rowId){
	var table = document.getElementById("table1");
	var row = table.rows[rowId];
	if (checkBox.checked) {
		row.className = "delete";
			document.getElementById('updatedRow'+rowId).checked = false;
			document.getElementById('updatedRow'+rowId).disabled = true;
			document.getElementById('approvedRow'+rowId).checked = false;
			document.getElementById('approvedRow'+rowId).disabled = true;
		disableFields(checkBox, rowId);
	} else {
		row.className = "";
		document.getElementById('updatedRow'+rowId).disabled = false;
		document.getElementById('approvedRow'+rowId).disabled = false;
	}
}

function validateSave(){
	var len = document.getElementById("table1").rows.length - 3;
	var noOfUpdates = 0;
	var noOfDeletes = 0;
	var noOfApprovals = 0;
	for(index=1;index<len;index++) {
		if(document.getElementById('updatedRow'+index).checked) noOfUpdates++;
		if(document.getElementById('deletedRow'+index).checked) noOfDeletes++;
		if(document.getElementById('approvedRow'+index).checked) noOfApprovals++;
	}

	if( (noOfUpdates > 0) || (noOfDeletes > 0) || (noOfApprovals > 0) ){

		document.getElementById('noOfUpdates').value = noOfUpdates;
		document.getElementById('noOfDeletes').value = noOfDeletes;
		document.getElementById('noOfApprovals').value = noOfApprovals;
		var diffAmount = getElementIdAmount("diffAmount");
		var message = "";
		if (noOfUpdates>0)
			message += "Items to be updated: " + noOfUpdates;
		if (noOfDeletes>0)
			message += "\nItems to be deleted: " + noOfDeletes;
		if (noOfApprovals>0)
			message += "\nItems to be approved: " + noOfApprovals;
		if (diffAmount != 0)
			message += "\nTotal increase in revenue: " + diffAmount;

		return confirm(message);
	}else
		return false;
}

function onChangeAmount(changeRowIndex) {
	recalcRowAmounts(changeRowIndex);
	recalcTotals();
}

function recalcRowAmounts(changeRowIndex) {
	if((numberCheck(document.getElementById("rate"+changeRowIndex)))&&(numberCheck(document.getElementById("qty"+changeRowIndex)))&&(numberCheck(document.getElementById("discount"+changeRowIndex)))){
		var changedRate = formatAmountObj(document.getElementById("rate"+changeRowIndex));
		var changedQty = formatAmountObj(document.getElementById("qty"+changeRowIndex));
		var changedDisc = formatAmountObj(document.getElementById("discount"+changeRowIndex));
		var updatedAmt = eval(changedRate*changedQty-changedDisc) ;
	    if(formatAmountValue(updatedAmt) < 0){
	    	alert("Discount should be less");
	    	document.getElementById("discount"+changeRowIndex).value = 0;
	    	return false;
	    }else{
	   		document.getElementById("amount"+changeRowIndex).value = formatAmountValue(updatedAmt);
	    }
	}
}

function recalcTotals() {
	var len = document.getElementById("table1").rows.length - 3;
	var newTotal = 0;
	for(index=1;index<len;index++) {
		if (!document.getElementById('deletedRow'+index).checked) {
			if (document.getElementById('updatedRow'+index).checked)
				newTotal += getElementIdPaise("amount"+index);
			else
				newTotal += getElementIdPaise("origAmount"+index);
		}
	}
	document.getElementById("totalAmount").value=formatAmountPaise(newTotal);
	var origTotal = getElementIdPaise("origTotalAmount");
	document.getElementById("diffAmount").value=formatAmountPaise((newTotal - origTotal));

}

function setOrigAmounts(row) {
	document.getElementById("rate"+row).value = document.getElementById("origRate"+row).value;
	document.getElementById("qty"+row).value = document.getElementById("origQty"+row).value;
	document.getElementById("discount"+row).value = document.getElementById("origDiscount"+row).value;
	document.getElementById("amount"+row).value = document.getElementById("origAmount"+row).value;
}

function enableAmt(rowId){
	if(document.getElementById("updatedRow"+rowId).checked){
		document.getElementById('rate'+rowId).readOnly = false;
		document.getElementById('qty'+rowId).readOnly = false;
		document.getElementById('discount'+rowId).readOnly = false;
	}else{
		document.getElementById('rate'+rowId).readOnly = true;
		document.getElementById('qty'+rowId).readOnly = true;
		document.getElementById('discount'+rowId).readOnly = true;

	}
}

function updateChk(){
	var len = document.getElementById("table1").rows.length - 3;
	var obj;
	for(index=1;index<len;index++) {
		obj = document.getElementById("updatedRow"+index);
		objDel = document.getElementById("deletedRow"+index);
		obj.checked=true;
		if(objDel.checked){
			deleteSelectedRow(obj,index);
			enableFieldsToEdit(obj,index);
		}
		enableFieldsToEdit(obj,index);
	}
	recalcTotals();
}


function deleteChk(){
	var len = document.getElementById("table1").rows.length - 3;
	var obj;
	for(index=1;index<len;index++) {
		obj = document.getElementById("deletedRow"+index);
		obj.checked=true;
		deleteSelectedRow(obj,index);
	}
	recalcTotals();
}

function approveChk(){
	var len = document.getElementById("table1").rows.length - 3;
	var obj;
	for(index=1;index<len;index++) {
		obj = document.getElementById("approvedRow"+index);
		objDel = document.getElementById("deletedRow"+index);
		obj.checked=true;
		if(objDel.checked){
			deleteSelectedRow(obj, index);
		}
	}
	recalcTotals();
}

function updateUnChk(){
	var len = document.getElementById("table1").rows.length - 3;
	var obj;
	for(index=1;index<len;index++) {
		obj = document.getElementById("updatedRow"+index);
		obj.checked=false;
		enableFieldsToEdit(obj,index);
	}
	recalcTotals();
}

function deleteUnChk(){
	var len = document.getElementById("table1").rows.length - 3;
	var obj;
	for(index=1;index<len;index++) {
		obj = document.getElementById("deletedRow"+index);
		obj.checked=false;
		deleteSelectedRow(obj,index);
	}
	recalcTotals();
}

function apprUnChk(){
	var len = document.getElementById("table1").rows.length - 3;
	var obj;
	for(index=1;index<len;index++) {
		obj = document.getElementById("approvedRow"+index);
		obj.checked=false;
	}
	recalcTotals();
}

function resetAll() {
	var len = document.getElementById("table1").rows.length - 3;
	for(index=1;index<len;index++) {
		setOrigAmounts(index);
	}
	recalcTotals();
	return false;
}


function readonlyDate() {
	if (null != document.mainform.postedDate) {
		var len = document.getElementById("table1").rows.length - 4;
		for ( var i=1; i<=len; i++) {
			document.getElementById('postedDate'+i).readOnly = true;
		}
	}
}

var transForm = document.transactionApprovalForm;

function clearSearch(){
	transForm = document.transactionApprovalForm;

	transForm.patTypeAll.checked = true;
	transForm.billStatusAll.checked = true;
	transForm.chrGrpAll.checked = true;
	transForm.chrStatsAll.checked = true;
	transForm.patTypeInsuAll.checked = true;

	enablePatType();
	enableBillStatus();
	enableChargeGrp();
	enableStatus();
	enablePatInsuType();

   	document.getElementById('fdate').value = "";
   	document.getElementById('tdate').value = "";
}

	var toolbar = {
		Edit: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: 'billing/BillAction.do?_method=getCreditBillingCollectScreen',
			onclick: null,
			description: "View and/or Edit Patient Bill"
			}
	};

function init() {
    transForm = document.transactionApprovalForm;
	//enablePatType();
	//enableBillStatus();
	//enableChargeGrp();
	//enableStatus();
	//enablePatInsuType();
	initMrNoAutoComplete(cpath);
	createToolbar(toolbar);
	showFilterActive(document.transactionApprovalForm);
}

function enablePatType() {
	var disabled = transForm.patTypeAll.checked;
	transForm.patTypeIp.disabled = disabled;
    transForm.patTypeOp.disabled=  disabled;

}


function enablePatInsuType() {
	var disabled = transForm.patTypeInsuAll.checked;
	transForm.patTypeInsu.disabled = disabled;
	transForm.patTypeInsuNone.disabled = disabled;

}

function enableBillStatus() {
	var disabled = transForm.billStatusAll.checked;
	transForm.billStatusFinalized.disabled=  disabled;
	transForm.billStatusSettled.disabled = disabled;
	transForm.billStatusClosed.disabled = disabled;
}

function enableChargeGrp() {
	var disabled = transForm.chrGrpAll.checked;
	transForm.chrGrpReg.disabled = disabled;
	transForm.chrGrpDoc.disabled=  disabled;
	transForm.chrGrpOpe.disabled = disabled;
	transForm.chrGrpWard.disabled = disabled;
	transForm.chrGrpIcu.disabled=  disabled;
	transForm.chrGrpOtherchr.disabled = disabled;
	transForm.chrGrpMed.disabled = disabled;
	transForm.chrGrpServ.disabled=  disabled;
	transForm.chrGrpTest.disabled=  disabled;
}

function enableStatus(){
	var disabled = transForm.chrStatsAll.checked;
	transForm.chrStatsActive.disabled = disabled;
	transForm.chrStatsCancelled.disabled=  disabled;
}


function reCalRate(){
var perCen=document.getElementById("perCen").value;
var rRs=document.getElementById("rRs").value;
var rVariation=document.getElementById("rateVariation").value;


var rate="";
var rCalRate="";
var fRate="";
 for(var i=1;i<=document.getElementById("table1").rows.length-4;i++){

 if((document.getElementById("updatedRow"+i).checked==true)&&(perCen!="")){
   rate=document.getElementById("rate"+i).value;
   rCalRate=(rate*perCen/100).toFixed(2);
   if(rVariation=="I"){
     fRate=eval(parseFloat(rate)+ parseFloat(rCalRate));
   }else{
     fRate=eval(parseFloat(rate)-parseFloat(rCalRate));
   }
   if(rRs=="N"){
   document.getElementById("rate"+i).value=fRate.toFixed(2);
   }else{
       fRate=Math.round(fRate);
    if(rRs=="10") {
       fRate= roundNearest(fRate, 10);
    }if(rRs=="25"){
       fRate=eval(fRate*4);
       fRate=roundNearest(fRate, 100)/4;
    }if(rRs=="50"){
       fRate=eval(fRate*2);
       fRate=roundNearest(fRate, 100)/2;
    }if(rRs=="100"){
       fRate=roundNearest(fRate, 100)
    }
     document.getElementById("rate"+i).value=fRate;
  }
    recalcRowAmounts(i, false);
 }

 }
 recalcTotals();
}

function roundNearest(num, acc){
    if ( acc < 0 ) {
        num *= acc;
        num = Math.round(num);
        num /= acc;
        return num;
    } else {
        num /= acc;
        num = Math.round(num);
        num *= acc;
        return num;
    }
}
