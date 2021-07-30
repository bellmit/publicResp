function setPerdiemInclDetails() {

	var perdiemCode = mainform.per_diem_code.value;

	if (!empty(perdiemCode)) {

		var perdiemDet = findInList(jPerdiemCodeDetailsList, "per_diem_code", perdiemCode);

		var table = document.getElementById("perdiemDetailsTab");
		var templateRow = createTempRowAfterDelete();

		if (!empty(perdiemDet)) {

			document.getElementById("per_diem_code").title = perdiemDet.per_diem_description +" ("+perdiemDet.per_diem_code+")";

			var chlbl = document.getElementById("perdiemChargeLabel");
			var cperlbl = document.getElementById("perdiemCopayPerLabel");
			var camtlbl = document.getElementById("perdiemCopayAmtLabel");
			setNodeText(chlbl, formatAmountPaise(getPaise(perdiemDet.charge)));
			if (!empty(perdiemPlanBean)) {
				setNodeText(cperlbl, getAmount(perdiemPlanBean.perdiem_copay_per));
				setNodeText(camtlbl, formatAmountPaise(getPaise(perdiemPlanBean.perdiem_copay_amount)));
			}else {
				setNodeText(cperlbl, "-");
				setNodeText(camtlbl, "-");
			}

			var subGroupNamesArr	= perdiemDet.service_groups_names.split(",");
			for (var i = 0; i < subGroupNamesArr.length; i++) {

				var subGroupName = subGroupNamesArr[i];
				var el = templateRow.cells[0];
				setNodeText(el, i+1);
				el = templateRow.cells[1];
				setNodeText(el, subGroupName);

				if (i+1 < subGroupNamesArr.length) {
					var nextRow = table.rows[i+2];
					var row = templateRow.cloneNode(true);
					table.tBodies[0].insertBefore(row, nextRow);
					templateRow = row;
				}
			}
		}
	} else {
		createTempRowAfterDelete();
	}
}

function createTempRowAfterDelete() {
	var table = document.getElementById("perdiemDetailsTab");
	var rowCount = table.rows.length;
	for (var i = 2; i < rowCount; i++) {
		table.deleteRow(i);
		rowCount--;
		i--;
	}

	var templateRow = table.rows[1];
	for (var c=0; c<=4; c++) {
		var el = templateRow.cells[c];
		setNodeText(el, '');
	}

	return templateRow;
}

function validatePerdiemCode() {

	var perdiemCodeObj = mainform.per_diem_code;
	var existingPerdiemCodeObj = mainform.existing_per_diem_code;

	var perdiemCode = !empty(perdiemCodeObj) ? perdiemCodeObj.value : "";
	var existingPerdiemCode = !empty(existingPerdiemCodeObj) ? existingPerdiemCodeObj.value : "";

	if (perdiemCode != existingPerdiemCode) {

		if (chargesAdded > 0 || chargesEdited > 0) {
			var msg=getString("js.billing.billingprediem.newchargesadded.edited");
			msg+=" \n";
			msg+=getString("js.billing.billingprediem.saveforeprocessing");
			msg+=" \n";
			msg+=getString("js.billing.billingprediem.resettingback.originalperdiemcode");
			alert(msg);
			setSelectedIndex(perdiemCodeObj, existingPerdiemCode);
			return false;
		}

		if (existingPerdiemCode != "" && perdiemCode == "") {
			var ok = confirm(" This patient is a perdiem patient.\n Are you sure you want to remove perdiem code?");
			if (!ok) {
				perdiemCodeObj.focus();
				return false;
			}
		}

		if (existingPerdiemCode != "" && perdiemCode != "") {
			var ok = confirm(" The perdiem code is changed.\n Do you want to continue with this perdiem code?");
			if (!ok) {
				perdiemCodeObj.focus();
				return false;
			}
		}
	}else {
		var ok = confirm(" Do you want to process bill using perdiem code?");
		if (!ok)
			return false;
	}
	return true;
}

function perdiemProcessing() {

	if (chargesAdded > 0 || chargesEdited > 0) {
		showMessage("js.billing.billingprediem.newchargesaddedoredited.saveforeprocessing");
		return false;
	}

	if (!validatePerdiemCode())
		return false;

	document.mainform.buttonAction.value = 'perdiemProcess';
	document.mainform.submit();
	return true;
}

function initPerdiemInclusionsDialog() {

	var dialogDiv = document.getElementById("perdiemInclusionsDialog");
	dialogDiv.style.display = 'block';
	perdiemDialog = new YAHOO.widget.Dialog("perdiemInclusionsDialog",{
			width:"300px",
			text: "Perdiem Sub Groups",
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onPerdiemCancel,
	                                                scope:perdiemDialog,
	                                                correctScope:true } );
	perdiemDialog.cfg.queueProperty("keylisteners", escKeyListener);
	perdiemDialog.render();
}

function showPerDiemInclDialog() {
	perdiemDialog.cfg.setProperty("context", ["btnPerdiemIncl", "tl", "bl"], false);
	perdiemDialog.show();
}

function onPerdiemCancel() {
	perdiemDialog.hide();
}