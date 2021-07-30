var cform = null;
function init() {
	cform = document.billsForm;
}

function changeSponsorType(type) {
	if(type == 'H') {
		if(document.getElementById('sponsor_id1').value != '') {
			document.getElementById('sponsor_id0').options.selectedIndex=0;
			document.claimSearchForm.sponsor_type.value = 'H';
		}else {
			document.claimSearchForm.sponsor_type.value = '';
		}
	}
	if(type == 'S') {
		if(document.getElementById('sponsor_id0').value != '') {
			document.getElementById('sponsor_id1').options.selectedIndex=0;
			document.claimSearchForm.sponsor_type.value = 'S';
		}else {
			document.claimSearchForm.sponsor_type.value = '';
		}
	}
}

function validateSponsor() {
	var tpa = document.getElementById("sponsor_id0").value;
	var hospital = document.getElementById("sponsor_id1").value;
	if (tpa == '' && hospital == '') {
		alert("Please select TPA (OR) Other Hospital.");
		document.getElementById("sponsor_id0").focus();
		return false;
	}
	return true;
}

function printConsolidatedBill() {
	if ( sbillNo == null || sbillNo == '' )
		return false;
	var printerId = cform.printType.value;
	window.open("../../pages/BillDischarge/SponsorBillList.do?_method=sponsorConsolidatedBillPrint&sponsorBillNo="
			+sbillNo+"&printerType="+printerId);
}

function chooseBill(checkbox, id) {
	if(checkbox.checked) {
		document.getElementById("claimBill"+id).value = checkbox.value;
		cform.totalClaimAmt.value = getPaise(cform.totalClaimAmt.value)
										+ getPaise(document.getElementById("claimAmt"+id).value);
	}else {
		document.getElementById("claimBill"+id).value = '';
		cform.totalClaimAmt.value = getPaise(cform.totalClaimAmt.value)
										- getPaise(document.getElementById("claimAmt"+id).value);
	}
	cform.totalClaimAmt.value = getAmount(cform.totalClaimAmt.value)/100;
}

function selectAllBills() {
	var len = cform.claimBillCheck.length;
	if(len == undefined)  len = 1;
	if(cform.selectAll.checked) {
		cform.totalClaimAmt.value = 0;
		for(var i=0;i<len;i++) {
			document.getElementById("claimBillCheck"+i).checked = true;
			chooseBill(document.getElementById("claimBillCheck"+i), i);
		}
	}else {
		for(var i=0;i<len;i++) {
			document.getElementById("claimBillCheck"+i).checked = false;
			chooseBill(document.getElementById("claimBillCheck"+i), i);
		}
	}
}

function checkClaimBills() {
	var billCheckElmts = document.getElementsByName("claimBillCheck");
	for(var j=0;j<billCheckElmts.length;j++) {
		if(billCheckElmts[j].checked) return true;
	}
	return false;
}

function validateClaimForm() {
	if(!checkClaimBills()) {
		alert("Select any bill");
		return false;
	}
	var bill = cform.selectedBill.value;
	if(bill == 'New') cform._method.value = "createBill";
	else cform._method.value = "addMoreBills";
	cform.submit();
}

function searchClaimBills() {
	if(cform.fdate.value == '') {
		alert("Enter date");
		cform.fdate.focus();
		return false;
	}
	if(cform.tdate.value == '') {
		alert("Enter date");
		cform.tdate.focus();
		return false;
	}
	if(!doValidateDateField(cform.fdate, 'past')) return false;
	if(!doValidateDateField(cform.tdate, 'past')) return false;
	var flag = false;
	for(var i=0;i<cform.sponsor_type.length;i++) {
		if(cform.sponsor_type[i].checked)
			flag = true;
	}
	if(!flag) {
		alert("Please select sponsor type");
		cform.sponsor_type[0].focus();
		return false;
	}
	cform._method.value = "getClaimBills";
	return true;
}