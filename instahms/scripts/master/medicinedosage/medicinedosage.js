function validate(event) {
	var qty = document.getElementById("per_day_qty").value;
	var dosage = document.getElementById("dosage_name").value;
	if (dosage == '') {
		alert("Dosage Name is mandatory");
		document.getElementById("dosage_name").focus();
		YAHOO.util.Event.stopEvent(event);
		return false;
	}
	if (!isAmount(qty)) {
		alert("Per Day Qty is not valid. \n 1) It should be numeric. Ex: 4 \n 2) Only two digits after decimal point. Ex:4.50");
		document.getElementById("per_day_qty").focus();
		YAHOO.util.Event.stopEvent(event);
		return false;
	}
}
