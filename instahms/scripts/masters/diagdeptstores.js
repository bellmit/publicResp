function onSelectCenter(){
	selectedCenter = document.getElementById("centerId").value;
	document.getElementById("storeId").length = 1;
	var index = 1;
	for(var i=0; i<storeList.length; i++) {
		if(storeList[i].CENTER_ID == selectedCenter) {
			document.getElementById("storeId").length = index+1;
			document.getElementById("storeId").options[index].value=storeList[i].DEPT_ID;
			document.getElementById("storeId").options[index].text=storeList[i].DEPT_NAME;
			index = index+1;
		}
	}
}

function init(){
	diagdeptDialog();
}

var deptDialog;
var newRowinserted = true;
var currentRow = null;

function diagdeptDialog() {
	var diagdeptDIV = document.getElementById("diagdeptDIV");
	diagdeptDIV.style.display = 'block';
	deptDialog = new YAHOO.widget.Dialog('diagdeptDIV', {
				width:"300px",
				visible:false,
				modal:true,
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:deptDialog,
	                                                correctScope:true } );
	deptDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	deptDialog.cancelEvent.subscribe(cancel);
	deptDialog.render();
}

function handleCancel() {
	deptDialog.cancel();
}

function cancel() {
	newRowinserted = true;
	currentRow = null;
}

function showDialog(obj) {
	document.getElementById('centerId').value = '';
	document.getElementById('storeId').value = '';
	deptDialog.cfg.setProperty("context", [obj, "tr", "bl"], false);
	deptDialog.show();
}

function addToTable() {

	if(document.getElementById('centerId').value == '') {
		alert("please select Center..");
		return false;
	}
	if(document.getElementById('storeId').value == '') {
		alert("please select Store..");
		return false;
	}
	var tableObj = document.getElementById('diagdeptTbl');
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
	tds[1].textContent = document.getElementById('storeId').
			options[document.getElementById('storeId').selectedIndex].text;

	getElementByName(newRow, 'center_id').value = document.getElementById('centerId').
			options[document.getElementById('centerId').selectedIndex].value;
	getElementByName(newRow, 'store_id').value = document.getElementById('storeId').
			options[document.getElementById('storeId').selectedIndex].value;

	newRowinserted = true;
	currentRow = null;
	removeClassName(newRow, 'editing');
	deptDialog.cancel();

}

function changeElsColor(index, obj) {

		var row = document.getElementById("diagdeptTbl").rows[index];
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

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getFirstItemRow() {
	return 1;
}