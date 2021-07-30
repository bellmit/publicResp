/*
 * Functions in use by NewBill.jsp
 */

function newBillCreate() {
	if (document.newbillform.visitId.value=="") {
		showMessage("js.billing.newbill.entervisitno");
		document.patientSearch.patient_id.focus();
		return false;
	}

	var radios=document.getElementsByName('creditprepaid');
	if(!radios[0].checked && !radios[1].checked){
		showMessage("js.billing.newbill.selectcreditbill.prepaidbill");
		return false;
	}
	document.newbillform.submit();
}

function reset(){
	document.getElementById('prepaid').checked = true;
	document.getElementById('credit').checked = false;
	if (document.getElementById('istpa'))
		document.getElementById('istpa').checked = false;
}

function enableDisabledTpa() {
	var prepaidSelect = document.getElementById("prepaid");
	var tpaCheck =  document.getElementById("istpa");
	if (tpaCheck != null) {
		if (prepaidSelect.checked && allowBillNowInsurance == 'false') {
			 tpaCheck.disabled = true;
		}else
			tpaCheck.disabled = false;
	}
}
