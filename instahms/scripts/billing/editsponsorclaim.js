var bform = null;
var statusList = null;
function init() {
	bform = document.billsForm;
	// applicable status values for the status dropdown
	if(cancelBillRights == 'A' || roleId == 1 || roleId ==2) {
		if(origStatus == 'O') {
			statusList = [{status:'O',value:1,text:'Open'},
					 	 {status:'S',value:2,text:'Sent'},
					 	 {status:'X',value:3,text:'Cancelled'}];
		}else{
			statusList = [{status:'S',value:1,text:'Sent'},
						  {status:'R',value:2,text:'Received'},
						  {status:'C',value:3,text:'Closed'}];
		}
	} else {
		if(origStatus == 'O') {
			statusList = [{status:'O',value:1,text:'Open'},
						  {status:'S',value:2,text:'Sent'}];
		}else {
			statusList = [{status:'S',value:1,text:'Sent'},
						  {status:'R',value:2,text:'Received'},
						  {status:'C',value:3,text:'Closed'}];
		}
	}
	loadStatus();
}

function cancelBill(checkbox,id) {
	var billSelect = document.getElementById("billSelect"+id);
	var billDelete = document.getElementById("billDelete"+id);
	var billRow = document.getElementById("billRow"+id);
	var img = billRow.cells[0].getElementsByTagName("img");

	var rowToDelete = billDelete.value = billDelete.value == 'false' ? 'true' :'false';
	var src;
	var cls;
	if (rowToDelete == 'true') {
		src = cpath+"/images/red_flag.gif";
		cls = 'delete';
		bform.totalClaimAmt.value = getPaise(bform.totalClaimAmt.value) - getPaise(billSelect.value);
  	} else {
  		src = cpath+"/images/empty_flag.gif";
		cls = '';
		bform.totalClaimAmt.value = getPaise(bform.totalClaimAmt.value) + getPaise(billSelect.value);
  	}

  	if (img && img[0])
		img[0].src = src;
	billRow.className = cls;

	bform.totalClaimAmt.value = getAmount(bform.totalClaimAmt.value)/100;
}

function checkClaimBills() {
	var claimCheckElmts = document.getElementsByName("claimBillCheck");
	for(var j=0;j<claimCheckElmts.length;j++) {
		if(claimCheckElmts[j].checked) return true;
	}
	return false;
}

function validateBillsForm() {
	/*if(!checkClaimBills()) {
		alert("Select any bill");
		return false;
	}*/
	if(!validateStatusDates()) return false;
	bform.submit();
}

function printConsolidatedBill() {
	var sbillNo = billsForm.sponsor_bill_no.value;
	if ( sbillNo == null || sbillNo == '' )
		return false;
	var printerId = bform.printerType.value;
	window.open("../../pages/BillDischarge/SponsorBillList.do?_method=sponsorConsolidatedBillPrint&sponsorBillNo="
			+sbillNo+"&printerType="+printerId);
}

/*
 * Forward only status changes.
 */
function loadStatus() {
	var statusObj = bform.status;
	var len = 0;
	var currentStatus = 0;

	for (var i=0;i<statusList.length; i++) {
		if(statusList[i].status == origStatus) {
			currentStatus = statusList[i].value;
		}
	}

	statusObj.length = 0;
	for(var i=0;i<statusList.length; i++) {
		if(statusList[i].value >= currentStatus) {
			statusObj.length = len + 1;
			var option  = new Option(statusList[i].text, statusList[i].status);
			statusObj[len] = option;
			len = len+1;
		}
	}
	setSelectedIndex(statusObj,origStatus);
	if(origStatus == 'O' && bform.claim_date != null)  bform.claim_date.readOnly = false; else bform.claim_date.readOnly = true;
	if(origStatus == 'S' && bform.sent_date != null)  bform.sent_date.readOnly = false; else bform.sent_date.readOnly = true;
	if(origStatus == 'C' && bform.closed_date != null)  bform.closed_date.readOnly = false; else bform.closed_date.readOnly = true;
}

function validateStatusDates() {
	var statusObj = bform.status;
	if(statusObj.value == 'O') {
		if(bform.claim_date && trimAll(bform.claim_date.value) == '') {
			alert("Enter claim date");
			bform.claim_date.focus();
			return false;
		}
	}else if(statusObj.value == 'S') {
		if(bform.sent_date && trimAll(bform.sent_date.value) == '') {
			alert("Enter sent date");
			bform.sent_date.focus();
			return false;
		}
	}else if(statusObj.value == 'C') {
		if(bform.closed_date && trimAll(bform.closed_date.value) == '') {
			alert("Enter closed date");
			bform.closed_date.focus();
			return false;
		}
	}else if(statusObj.value == 'X') {
		if(bform.cancel_reason && trimAll(bform.cancel_reason.value) == '') {
			alert("Enter cancelled reason");
			bform.cancel_reason.focus();
			return false;
		}
	}

	return true;
}

function onChangeStatus() {
	var statusObj = bform.status;

	if(bform.claim_date != null) {
		if(statusObj.value == 'O') {
			bform.claim_date.readOnly = false;
			bform.sent_date.readOnly = true;
			bform.closed_date.readOnly = true;

			bform.claim_date.value = clmDate==''?currDate:clmDate;
			bform.sent_date.value = sntDate;
			bform.closed_date.value = clsdDate;

		}else if(statusObj.value == 'S') {
			bform.claim_date.readOnly = true;
			bform.sent_date.readOnly = false;
			bform.closed_date.readOnly = true;

			bform.claim_date.value = clmDate;
			bform.sent_date.value = sntDate==''?currDate:sntDate;
			bform.closed_date.value = clsdDate;

		}else if(statusObj.value == 'C') {
			bform.claim_date.readOnly = true;
			bform.sent_date.readOnly = true;
			bform.closed_date.readOnly = false;

			bform.claim_date.value = clmDate;
			bform.sent_date.value = sntDate;
			bform.closed_date.value = clsdDate==''?currDate:clsdDate;

		}else {
			bform.claim_date.readOnly = true;
			bform.sent_date.readOnly = true;
			bform.closed_date.readOnly = true;

			bform.claim_date.value = clmDate;
			bform.sent_date.value = sntDate;
			bform.closed_date.value = clsdDate;
		}
	}else {
		if(statusObj.value == 'O') {
			document.getElementById("claimDt").textContent = clmDate==''?currDate:clmDate;
			document.getElementById("sentDt").textContent = sntDate;
			document.getElementById("closeDt").textContent = clsdDate;
		}else if(statusObj.value == 'S') {
			document.getElementById("claimDt").textContent = clmDate;
			document.getElementById("sentDt").textContent = sntDate==''?currDate:sntDate;
			document.getElementById("closeDt").textContent = clsdDate;
		}else if(statusObj.value == 'C') {
			document.getElementById("claimDt").textContent = clmDate;
			document.getElementById("sentDt").textContent = sntDate;
			document.getElementById("closeDt").textContent = clsdDate==''?currDate:clsdDate;
		}else {
			document.getElementById("claimDt").textContent = clmDate;
			document.getElementById("sentDt").textContent = sntDate;
			document.getElementById("closeDt").textContent = clsdDate;
		}
	}
}