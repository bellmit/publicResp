
function validateForm(){
	return true;

}

var payeeArray = ["dr_payment","ref_payment","hosp_payment","presc_payment"];
function getFirstPayee(){
	var exists = false;	
	var sPayeeArray = new Array(3);
	var fPayee = document.forms[0].first_payee.value;
	var sIndex = 0;
	for (i=0;i<payeeArray.length; i++){
		if (fPayee != payeeArray[i]){
			sPayeeArray[sIndex]= payeeArray[i];
			sIndex++;
		}
	}
	secondPayee = document.forms[0].second_payee;
	loadPaymentSelectBox(secondPayee, sPayeeArray,"Select","");
	getSecondPayee();

}

function getSecondPayee(){
	var tPayeeArray = new Array(2);
	var sPayee = document.forms[0].second_payee;
	var tIndex = 0;

	for (f=0;f<sPayee.length; f++){
		if (sPayee.value != sPayee[f].value && sPayee[f].value != ""){
			tPayeeArray[tIndex] = sPayee[f].value;
			tIndex++;
		}
	}
	thirdPayee = document.forms[0].third_payee;
	loadPaymentSelectBox(thirdPayee, tPayeeArray,"Select","");
	getThirdPayee();
}


function getThirdPayee(){
	var fPayee = "";
	var tPayee = document.forms[0].third_payee;
	var fIndex = 0;
	for (i = 0; i<tPayee.length;i++){
		if (tPayee.value  != tPayee[i].value && tPayee[i].value != ""){
			fPayee = tPayee[i].value;
		}
	}
	document.getElementById("fpayeelbl").textContent  = getPaymentTypes(fPayee);
	document.getElementById("fourth_payee").value  = fPayee;
}

function loadPaymentSelectBox(selectBox, itemList, title, titleValue) {
	if (itemList == null) {
		selectBox.length = 0;
		selectBox.disabled = true;
		return;
	}
	if (title != null) {
		selectBox.length = itemList.length + 1;
	} else {
		selectBox.length = itemList.length;
	}
	selectBox.disabled = false;
	var index = 0;
	if (title != null) {
		selectBox.options[index].text = ".." + title + "..";
		if (titleValue == null) titleValue = "";
		selectBox.options[index].value = titleValue;
		index++;
	}
	dispNameVar = null;
	valueVar = null;
	for (var i=0; i<itemList.length; i++) {
		var item = itemList[i];
		if ((item != undefined)){
			selectBox.options[index].text = getPaymentTypes(item);
			selectBox.options[index].value = item;
			index++;
		}	
	}
}

function getPaymentTypes(payee){
	if (payee == "dr_payment") dispNameVar = "Conducting Doctor";
	if (payee == "ref_payment") dispNameVar = "Referrer Doctor";
	if (payee == "hosp_payment") dispNameVar = "Hospital";
	if (payee == "presc_payment") dispNameVar = "Prescribing Doctor";
	return dispNameVar;

}
