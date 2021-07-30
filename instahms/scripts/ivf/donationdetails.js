function init(){
	donationDialog();
}

var donDialog;
var newRowinserted = true;
var currentRow = null;

function donationDialog() {
	var donationDIV = document.getElementById("donationDIV");
	donationDIV.style.display = 'block';
	donDialog = new YAHOO.widget.Dialog('donationDIV', {
				width:"300px",
				visible:false,
				modal:true,
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:donDialog,
	                                                correctScope:true } );
	donDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	donDialog.cancelEvent.subscribe(cancel);
	donDialog.render();
}

function handleCancel() {
	donDialog.cancel();
}

function cancel() {
	newRowinserted = true;
	currentRow = null;
}

function showDialog(obj) {
	document.getElementById('recipientMRNo').value = '';
	document.getElementById('donationDate').value = '';
	document.getElementById('donationType').value = '';
	document.getElementById('donationStatus').value = '';
	document.getElementById('donationRemarks').value = '';
	donDialog.cfg.setProperty("context", [obj, "tr", "bl"], false);
	donDialog.show();
}

function addToTable() {

	if(document.getElementById('recipientMRNo').value == '') {
		alert("Recipient MRNo should not be empty");
		return false;
	}
	var currentRowIndex = -1;
	if (newRowinserted == false) {
		var rowObj = getThisRow(currentRow, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);
	}

	var tableObj = document.getElementById('donationTbl');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-2];
	var newRow = '';

	if (newRowinserted) {
		var id = rowsLength-2;
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-3);
		getElementByName(newRow, 'selectedrow').id = 'selectedrow'+id;
		getElementByName(newRow, 'added').id = 'added'+id;
		getElementByName(newRow, 'added').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
		newRow = getThisRow(currentRow, 'TR');
	}

	var tds = newRow.getElementsByTagName('td');

	var recipient_mr_no = document.getElementById('recipientMRNo').value;
	var donation_date = document.getElementById('donationDate').value.trim();
	var donation_type = document.getElementById('donationType').
			options[document.getElementById('donationType').selectedIndex].text;
	var donation_status = document.getElementById('donationStatus').
			options[document.getElementById('donationStatus').selectedIndex].text;
	var donation_remarks = document.getElementById('donationRemarks').value;

	tds[0].textContent = recipient_mr_no;
	tds[1].textContent = donation_date;
	tds[2].textContent = donation_type;
	tds[3].textContent = donation_status;
	tds[4].textContent = donation_remarks;

	getElementByName(newRow, 'recipient_mr_no').value = recipient_mr_no;
	getElementByName(newRow, 'donation_date').value = donation_date;
	getElementByName(newRow, 'donation_type').value = document.getElementById('donationType').
				options[document.getElementById('donationType').selectedIndex].value;;
	getElementByName(newRow, 'donation_status').value = document.getElementById('donationStatus').
				options[document.getElementById('donationStatus').selectedIndex].value;
	getElementByName(newRow, 'remarks').value = donation_remarks;

	newRowinserted = true;
	currentRow = null;
	removeClassName(newRow, 'editing');
	donDialog.cancel();

}

function onEdit(obj) {
	donDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	var trObj = getThisRow(obj, 'TR');
	var id = getRowChargeIndex(trObj);
	addClassName(trObj, 'editing');
	var tds = trObj.getElementsByTagName('td');

	document.getElementById('recipientMRNo').value = tds[0].textContent;
	document.getElementById('donationDate').value = tds[1].textContent;
	setSelectedIndex(document.getElementById('donationType'), getElementByName(trObj, 'donation_type').value);
	setSelectedIndex(document.getElementById('donationStatus'),getElementByName(trObj, 'donation_status').value);
	document.getElementById('donationRemarks').value = tds[4].textContent;

	newRowinserted = false;
	currentRow = obj;
	donDialog.show();
}

function changeElsColor(index, obj) {

		var row = document.getElementById("donationTbl").rows[index];
		var trObj = getThisRow(obj);
		var tab = getThisTable(obj);
		var parts = trObj.id.split('row');
		var index = parseInt(parts[1])+1;

		var markRowForDelete = document.getElementById('selectedrow'+index).value == 'false' ? 'true' : 'false';
		document.getElementById('selectedrow'+index).value = document.getElementById('selectedrow'+index).value == 'false' ? 'true' :'false';

		if (markRowForDelete == 'true') {
			addClassName(trObj, 'cancelled');
	   	}
	   	else {
			removeClassName(trObj, 'cancelled');
	   	}
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	return 1;
}

function funSubmit(){
	var mrNo = document.patientDonation.donor_mr_no.value;
	document.patientDonation.action = cpath+"/IVF/DonationDetails.do?_method=update&mr_no="+mrNo;
	document.patientDonation.submit();
}
