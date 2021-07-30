function onSelectCenter(){
	selectedCenter = document.getElementById("centerId").value;
	document.getElementById("supplier_id").length = 1;
	var index = 1;
	for(var i=0; i<suppliersList.length; i++) {
		if(suppliersList[i].CENTER_ID == 0 || suppliersList[i].CENTER_ID == selectedCenter) {
			document.getElementById("supplier_id").length = index+1;
			document.getElementById("supplier_id").options[index].value=suppliersList[i].SUPPLIER_CODE;
			document.getElementById("supplier_id").options[index].text=suppliersList[i].SUPPLIER_NAME;
			index = index+1;
		}
	}
}

function init(){
	supplieritemDialog();
}

var supplierDialog;
var newRowinserted = true;
var currentRow = null;

function supplieritemDialog() {
	var centersupplierDIV = document.getElementById("centersupplierDIV");
	centersupplierDIV.style.display = 'block';
	supplierDialog = new YAHOO.widget.Dialog('centersupplierDIV', {
				width:"300px",
				visible:false,
				modal:true,
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:supplierDialog,
	                                                correctScope:true } );
	supplierDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	supplierDialog.cancelEvent.subscribe(cancel);
	supplierDialog.render();
}

function handleSupplierCancel() {
	supplierDialog.cancel();
}

function cancel() {
	newRowinserted = true;
	currentRow = null;
}

function showDialog(obj) {
	document.getElementById('centerId').value = '';
	document.getElementById('supplier_id').value = '';
	supplierDialog.cfg.setProperty("context", [obj, "tl", "bl"], false);
	supplierDialog.show();
}

function addToTable() {
	if(document.getElementById('centerId').value == '') {
		alert("please select Center..");
		return false;
	}
	if(document.getElementById('supplier_id').value == '') {
		alert("please select Supplier..");
		return false;
	}
	var centerNames = document.getElementsByName("center_id");
	var supplierNames = document.getElementsByName("supplier_code");
	for (var i=0;i<supplierNames.length;i++) {
		var id=getsupplierRows();
		if ((i!=id) && (centerNames[i].value == document.getElementById("centerId").value)) {
			alert("Duplicate Entry");
			document.getElementById("supplier_id").focus();
				return false;
		}
	}
		
	var tableObj = document.getElementById('centersupplierTbl');
	var currentRowIndex = -1;
	if (newRowinserted == false) {
		var rowObj = getThisRow(currentRow, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);
	}
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

	tds[0].textContent = document.getElementById('centerId').
			options[document.getElementById('centerId').selectedIndex].text;
	tds[1].textContent = document.getElementById('supplier_id').
			options[document.getElementById('supplier_id').selectedIndex].text;

	getElementByName(newRow, 'center_id').value = document.getElementById('centerId').
			options[document.getElementById('centerId').selectedIndex].value;
	getElementByName(newRow, 'supplier_code').value = document.getElementById('supplier_id').
			options[document.getElementById('supplier_id').selectedIndex].value;

	newRowinserted = true;
	currentRow = null;
	removeClassName(newRow, 'editing');
	supplierDialog.cancel();

}

function changeElsColor(index, obj) {

		var row = document.getElementById("centersupplierTbl").rows[index];
		var trObj = getThisRow(obj);
		var tab = getThisTable(obj);
		var parts = trObj.id.split('row');
		var index = parseInt(parts[1])+1;

		var markRowForDelete = document.getElementById('selectedrow'+index).value == 'false' ? 'true' : 'false';
		document.getElementById('selectedrow'+index).value = document.getElementById('selectedrow'+index).value == 'false' ? 'true' :'false';

		if (markRowForDelete == 'true') {
			addClassName(trObj, 'delete');
	   	}
	   	else {
			removeClassName(trObj, 'delete');
	   	}
}

function getsupplierRows() {
	var table = document.getElementById("centersupplierTbl");
	return  (table.rows.length - 3);
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	return 1;
}