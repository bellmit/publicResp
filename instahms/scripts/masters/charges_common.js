
function fillValues(tableName, object) {
	if (object.checked) {
			var tableObject = document.getElementById(tableName);
			var index = 0;
			var auditLogRow = document.getElementById('audit_log_row');

			if (auditLogRow)
				index = 3;
			else
				index = 2;
			for (var i=1; i<=tableObject.rows.length-index; i++) {

				var rowObject = tableObject.rows[i];
				var rowId = rowObject.id;
				var names = document.getElementsByName(rowId);
				var el = findFirstChildTextInputEl(rowObject.cells[1]);

				for (var j=1; j<rowObject.cells.length-1; j++) {
					document.getElementById(el.name + j).value = document.getElementById(el.name + 0).value;
				}
			}
			object.checked = false;
	}
}


function fillRowValues(tableName, object) {
	if (object.checked) {
		var tableObject = document.getElementById(tableName);
		var index = 0;
		var taxTypeSelectedIndex = document.getElementsByName("tax_type")[0].selectedIndex;
		var selectedSellingPrice = document.getElementsByName("selling_price")[0].value;
		var selectedSellingPriceExpr = document.getElementsByName("selling_price_expr")[0].value;
		for (var i=1; i<tableObject.rows.length-2; i++) {//excluding header row & checkbox row
			document.getElementsByName("selling_price")[i].value = selectedSellingPrice;
			document.getElementsByName("selling_price_expr")[i].value = selectedSellingPriceExpr;
		}
		object.checked = false;
	}
}

function findFirstChildTextInputEl(node) {
	var i=0;
	while (node != null) {
		if ( node.childNodes[i]  && node.childNodes[i].nodeName == 'INPUT' )
			break;
		i++;
	}
	return node.childNodes[i];
}

function nextFieldOnTab(e, el, tableName) {

	var tableObject = document.getElementById(tableName);
	var auditlogRow = document.getElementById('audit_log_row');
	var skipRows = 2;
	if (auditlogRow)
		skipRows = 3;

	var length = tableObject.rows.length-skipRows;
	var eventTab = isEventTab(e);
	if (eventTab) {

		var row = findAncestor(el, 'TR');
		var noOfCols = row.cells.length - 2;
		var isLastRow = length == row.rowIndex;

		var nextRow = isLastRow ? tableObject.rows[1] :tableObject.rows[row.rowIndex + 1];

		var nextRowEl = findFirstChildTextInputEl(nextRow.cells[1]);
		var id = ((el.id).substring(el.name.length));
		var isLastCol = (noOfCols == id);
		id = isLastRow ? ( isLastCol ? 0 : (parseInt(id)+1) ) : id;

		document.getElementById(nextRowEl.name + id).focus();
		return false;
	}
	return true;
}

function isEventTab(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode == 9 ) {
		return true;
	}
	return false;
}

function validateImportChargesFile(object, inputNameforFile, inputNameforOrg) {

	if (document.getElementById(inputNameforFile).value == '') {
		alert("Please browse and select a file to upload");
		return false;
	}
	var orgId = document.getElementById(inputNameforOrg).value;
	object.form.org_id.value = orgId;
	var fileName = document.getElementById(inputNameforFile).value.split('_');

	if (fileName[1] != 'undefined' && fileName[1] !=null && fileName[1] != '' ) {
		var subPart = fileName[fileName.length-1].split('.');
		if (subPart[1] != 'xls') {
			alert('File does not seeems to be xls file please check the file');
			return false;
		}
	} else {
		alert('File does not seems to be a master charges file please check the file');
		return false;
	}
	if(window.XMLHttpRequest) {
		req = new XMLHttpRequest();
	}
	else if(window.ActiveXObject) {
		req = new ActiveXObject("MSXML2.XMLHTTP");
	}
	req.open("GET", cpath+"/master/ServiceMaster.do?_method=getorgName&org_id="+orgId, true);
	req.setRequestHeader("Content-Type", "text/plain");
	req.send(null);
	req.onreadystatechange = function (){
		if (req.readyState == 4 && req.status == 200) {
			var orgName = req.responseText;
			if (subPart[0] != orgName) {
				if (confirm("The current rate plan "+orgName+" the upload file is having different rate plan.\n "+
				 "Are you sure you want to upload? ")) {
				 	object.form.submit();
				 } else {
				 	return false;
				 }
			} else {
				object.form.submit();
			}
		}
	}

}

function onChangeCheckValue(field, type){
	if(type == 'TAX') {
		if ( field.value == '' ) field.value = 0;
	} else if(type == 'Rate') {
		if ( field.value == '' )  {
			field.value = parseFloat(defaultValue).toFixed(prefDecimalDigits);
		} else {
			if(!isNaN(field.value)) {
				field.value = parseFloat(field.value).toFixed(prefDecimalDigits);
			}
		}
	}
}