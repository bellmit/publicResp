function setTestDocRowEdited(obj) {
	var row = getThisRow(obj);
	addClassName(row, 'edited');
	getElementByName(row, 'ad_test_row_edited').value = 'true';
}


var tadColIndex=0;
var TAD_PKG_NAME = tadColIndex++,
TAD_TEST_NAME = tadColIndex++,
TAD_ADDITIONAL_INFO = tadColIndex++,
TAD_NOTES = tadColIndex++,
TAD_BROWSE = tadColIndex++,
TAD_TRASH_COL = tadColIndex++,
TAD_PLUS_COL = tadColIndex++;

function addRowToTestAdtnlDocs(order, prescId) {
	var orderTable = order.addTo;
	var ad_table = document.getElementById('ad_test_info_table');
	if (empty(ad_table)) return ;
	
	if (order.itemType == 'Package') {
		var recordsAdded = false;
		for (var i=0; i<addOrderDialog.packageDetails.length; i++) {
			var activityType = addOrderDialog.packageDetails[i].item_type;
			var item = null;
			if (activityType == 'Laboratory' || activityType == 'Radiology')
				item = findInList2(rateplanwiseitems.result, 'type', activityType, 'id', addOrderDialog.packageDetails[i].activity_id);
			
			// if item not applicable for rate plan then skip
			if (empty(item)) continue;
			
			if (addOrderDialog.packageDetails[i].mandate_additional_info == 'O') {
				recordsAdded = true;
				var id = ad_table.rows.length-2;
				var templateRow = ad_table.rows[ad_table.rows.length-1];
				var row = templateRow.cloneNode(true);
	
				row.style.display = '';
				addClassName(row, 'added');
				addClassName(row, 'mainRow');
				document.getElementById('testInfoDialog').style.display = 'block';
				ad_table.tBodies[0].insertBefore(row, templateRow);
				setNodeText(row.cells[TAD_PKG_NAME], order.itemName, 20);
				setNodeText(row.cells[TAD_TEST_NAME], addOrderDialog.packageDetails[i].item_name, 20);
				setNodeText(row.cells[TAD_ADDITIONAL_INFO], addOrderDialog.packageDetails[i].additional_info_reqts, 20);
	
				var activityType = addOrderDialog.packageDetails[i].item_type;
				getElementByName(row, 'ad_main_row_id').value = prescId;
				getElementByName(row, 'ad_test_id').value = addOrderDialog.packageDetails[i].activity_id;
				getElementByName(row, 'ad_package_activity_index').value = i;
				getElementByName(row, 'ad_test_category').value = (activityType == 'Laboratory' ? 'DEP_LAB' : 'DEP_RAD');
				getElementByName(row, 'ad_test_name').value = addOrderDialog.packageDetails[i].item_name;
				getElementByName(row, 'ad_test_info_reqts').value = addOrderDialog.packageDetails[i].additional_info_reqts;
				
				var plusImg = getElementByName(row, 'btnAddItem');
				plusImg.style.display = 'none';
				cloneTestDocRow(plusImg, templateRow, true);
			}
			
		}
		if (recordsAdded) {
			renameFileUploadEl();
		}
	} else if ((order.itemType == 'Laboratory' || order.itemType == 'Radiology') && order.mandateTestAdditionalInfo == 'O') {
		document.getElementById('testInfoDialog').style.display = 'block';
		var item = null;
		if (order.itemType == 'Laboratory' || order.itemType == 'Radiology')
			item = findInList2(rateplanwiseitems.result, 'type', order.itemType, 'id', order.itemId);
		
		// if item not applicable for rate plan then skip
		if (empty(item)) return;
		
		var id = ad_table.rows.length-2;
		var templateRow = ad_table.rows[ad_table.rows.length-1];
		var row = templateRow.cloneNode(true);

		row.style.display = '';
		addClassName(row, 'added');
		addClassName(row, 'mainRow');
		ad_table.tBodies[0].insertBefore(row, templateRow);

		setNodeText(row.cells[1], order.itemName, 20);
		setNodeText(row.cells[2], order.additionalTestInfo, 20);
		
		getElementByName(row, 'ad_main_row_id').value = prescId;
		getElementByName(row, 'ad_test_id').value = order.itemId;
		getElementByName(row, 'ad_test_category').value = order.itemType == 'Laboratory' ? 'DEP_LAB' : 'DEP_RAD';
		getElementByName(row, 'ad_test_name').value = order.itemName;
		getElementByName(row, 'ad_test_info_reqts').value = order.additionalTestInfo;
		
		var plusImg = getElementByName(row, 'btnAddItem');
		plusImg.style.display = 'none';
		cloneTestDocRow(plusImg, templateRow, true);
		
		renameFileUploadEl();
	}
}

// called when a new row is added or deleted..
function renameFileUploadEl() {
	var table = document.getElementById('ad_test_info_table');
	var uploadEls = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', table);
	for (var i=0; i<uploadEls.length-1; i++) {
		var el = uploadEls[i];
		var row = getThisRow(el);
		
		var id = row.rowIndex - 1;
		el.setAttribute("name", "ad_test_file_upload[" + id + "]");
	}
}

function cloneTestDocRow(obj, addBeforeThisRow, isDummyRow) {
	var templateRow = getThisRow(obj);
	
	var ad_table = document.getElementById('ad_test_info_table');
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	ad_table.tBodies[0].insertBefore(row, empty(addBeforeThisRow) ? templateRow : addBeforeThisRow);
	
	var uploadEl = YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', row)[0];
	var trashImg = row.cells[TAD_TRASH_COL].getElementsByTagName("A")[0];
	var plusImg = getElementByName(row, 'btnAddItem');
	
	removeClassName(row, "mainRow");
	if (isDummyRow) {
		addClassName(row, "dummyRow");
		uploadEl.style.display = 'none';
		trashImg.style.display = 'none';
		plusImg.style.display = 'block';
	} else {
		removeClassName(row, "dummyRow");
		trashImg.style.display = 'block';
		uploadEl.style.display = 'block';
		plusImg.style.display = 'none';
	}
	
	addClassName(row.cells[TAD_PKG_NAME], 'indent');
	addClassName(row.cells[TAD_TEST_NAME], 'indent');
	setNodeText(row.cells[TAD_ADDITIONAL_INFO], 'indent');
	
	setNodeText(row.cells[TAD_PKG_NAME], '');
	setNodeText(row.cells[TAD_TEST_NAME], '');
	setNodeText(row.cells[TAD_ADDITIONAL_INFO], '');
	
	// hide the clinical notes icon.
	var notesImg = row.cells[TAD_NOTES].getElementsByTagName("A")[0];
	if (notesImg) {
		notesImg.style.display = 'none';
	}
	
	// cloned using + button
	if (empty(isDummyRow)) {
		renameFileUploadEl();
	}
}

function cancelTestAdtnlDoc(obj) {
	var row = getThisRow(obj);
	
	var table = document.getElementById('ad_test_info_table');
	var docId = getElementByName(row, "ad_test_doc_id").value;
	var cancelled = getElementByName(row, "ad_test_doc_delete").value;
	
	if (cancelled == 'false') {
		if (!allowTestAdtnlDocRowDelete(row)) {
			alert("You are not allowed to delete all the documents. Atleast one document is required.");
			return false;
		}
		cancelled = 'true';
	} else {
		cancelled = 'false'
	}
	
	if (empty(docId)) {
		// doc id can be empty, in the following cases.. 
		// if a new row is added against the already saved order item.
		// or for the newly ordered item.
		if (!YAHOO.util.Dom.hasClass(row, 'mainRow')) {
			// delete the row directly if it is not a main record.
			row.parentNode.removeChild(row);
			renameFileUploadEl(); // rename the file elements again since one row is physically deleted from the grid.
			return false;
		}
	}
	
	getElementByName(row, 'ad_test_doc_delete').value = cancelled;
	getElementByName(row, 'ad_test_row_edited').value = 'true';
	addClassName(row, 'edited');
		
	var trashImg = row.cells[TAD_TRASH_COL].getElementsByTagName("img");
	if (trashImg && trashImg[0]) {
		if (cancelled == 'true')
			trashImg[0].src =  cpath+"/icons/undo_delete.gif";
		else 
			trashImg[0].src = cpath+"/icons/delete.gif";
	}
		
	return false;
}

// allow deleting of selected test document, if atleast one document or clinical notes found in remaining rows.
function allowTestAdtnlDocRowDelete(row) {
	var noOfFileUploads = 1; 
	if (YAHOO.util.Dom.hasClass(row, 'mainRow')) {
		var clinical_notes = getElementByName(row, 'ad_clinical_notes').value;
		var testInformation = getElementByName(row, 'ad_test_info_reqts').value;
		if (!empty(clinical_notes)) return true;
		if (!empty(testInformation)) return true;
		
		// search for any row, which is containing a exising or newly added document. 
		// if found, currently selected document allowed for delete.
		var nextRow = YAHOO.util.Dom.getNextSibling(row);
		while (!YAHOO.util.Dom.hasClass(nextRow, 'dummyRow')) {
			var deleted = getElementByName(nextRow, "ad_test_doc_delete").value;
			var docId = getElementByName(nextRow, "ad_test_doc_id").value;
			var fileName = !empty(docId) ? '' : YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', nextRow)[0].value;
			
			if (deleted == 'false' && 
					(!empty(docId) || !empty(fileName)) ) return true; 
			
			if (empty(docId) && deleted == 'false') {
				noOfFileUploads++;
			}
			
			nextRow = YAHOO.util.Dom.getNextSibling(nextRow);
		}
	} else {
		// this is some where in the middle row selected, 
		// search for document upwards and downwards
		var nextRow = YAHOO.util.Dom.getNextSibling(row);
		while (!YAHOO.util.Dom.hasClass(nextRow, 'dummyRow')) {
			var deleted = getElementByName(nextRow, "ad_test_doc_delete").value;
			var docId = getElementByName(nextRow, "ad_test_doc_id").value;
			var fileName = !empty(docId) ? '' : YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', nextRow)[0].value;
			
			if (deleted == 'false' && 
					(!empty(docId) || !empty(fileName)) ) return true; 
			
			if (empty(docId) && deleted == 'false') {
				noOfFileUploads++;
			}
			
			nextRow = YAHOO.util.Dom.getNextSibling(nextRow);
		}
		// no document found downwards. so search continues upwards  
		var prevRow = YAHOO.util.Dom.getPreviousSibling(row);
		while (prevRow.rowIndex != 0 && !YAHOO.util.Dom.hasClass(prevRow, 'dummyRow')) {
			var deleted = getElementByName(prevRow, "ad_test_doc_delete").value;
			var docId = getElementByName(prevRow, "ad_test_doc_id").value;
			var fileName = !empty(docId) ? '' : YAHOO.util.Dom.getElementsByClassName("testFileUpload", 'input', prevRow)[0].value;
			
			if (deleted == 'false' && 
					(!empty(docId) || !empty(fileName)) ) return true; 
			
			if (empty(docId) && deleted == 'false') {
				noOfFileUploads++;
			}
			
			// reached main row, even if clinical notes found allow deleting the document.
			if (YAHOO.util.Dom.hasClass(prevRow, "mainRow")) {
				if (!empty(getElementByName(prevRow,"ad_clinical_notes").value))
					return true;
				if (!empty(getElementByName(prevRow,"ad_test_info_reqts").value))
					return true;
			}
			
			prevRow = YAHOO.util.Dom.getPreviousSibling(prevRow);
		}
	}
	// more than one empty document rows exists. so no harm deleting the selected row.
	if (noOfFileUploads > 1)
		return true;
	return false;
}


// main order item is deleted, so delete the related test additional docs as well.
function deleteTestAdditionalDocs(mainRow) {

	var table = document.getElementById('ad_test_info_table');
	if (empty(table)) return;

	var type = getElementByName(mainRow, 'type').value;
	if (!(type == 'test' || type == 'package')) return;

	var prescId = getElementByName(mainRow, type == 'test' ? 'test.prescId' : 'package.prescId').value;

	var ar = new Array();
	var indexEls = document.getElementsByName('ad_main_row_id');
	for (var i=0; i<indexEls.length; i++) {
		if (!empty(indexEls[i].value)) {
			if (indexEls[i].value == prescId) {
				ar.push(findAncestor(indexEls[i], 'TR'));
			}
		}
	}

	for (var i=0; i<ar.length; i++) {
		table.deleteRow(ar[i].rowIndex);
	}
}

var testAdInfoDialog = null;
function initTestAdditionalInfoDialog() {
	var dialogDiv = document.getElementById("addTestAddiotionalInfoDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	testAdInfoDialog = new YAHOO.widget.Dialog("addTestAddiotionalInfoDialog",
			{	width:"600px",
				context : ["addTestAddiotionalInfoDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('adTestAdInfoOk', 'click', updateTestAdInfo, testAdInfoDialog, true);
	YAHOO.util.Event.addListener('adTestAdInfoCancel', 'click', cancelTestAdInfo, testAdInfoDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelTestAdInfo,
	                                                scope:testAdInfoDialog,
	                                                correctScope:true } );
	testAdInfoDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	testAdInfoDialog.cancelEvent.subscribe(cancelTestAdInfo);
	testAdInfoDialog.render();
}


function showTestAdInfoDialog(obj) {
	var row = getThisRow(obj);
	
	addClassName(row, 'editing');
	testAdInfoDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	testAdInfoDialog.show();
	document.getElementById('adTestAdInfoRowId').value = row.rowIndex - 1;
	document.getElementById('d_test_additional_info').value = getElementByName(row, 'ad_clinical_notes').value;
	document.getElementById('test_info').innerHTML = getElementByName(row, 'ad_test_info_reqts').value;
	document.getElementById('d_test_additional_info').focus();
	return false;
}

function updateTestAdInfo() {
	var id = document.getElementById('adTestAdInfoRowId').value;
	var table = document.getElementById('ad_test_info_table');
	var row = table.rows[parseInt(id) + 1]

	addClassName(row, 'edited');
	var notes = document.getElementById('d_test_additional_info').value;
	var testInfo = getElementByName(row, 'ad_test_info_reqts').value;
	notes = notes.replace(/^\s*[\r\n]*/gm, "");
	document.getElementsByName('ad_test_info_reqts')[id].value = testInfo;
	document.getElementsByName('ad_clinical_notes')[id].value = notes;
	document.getElementsByName('ad_notes_entered')[id].value = 'true';
	document.getElementsByName('ad_test_row_edited')[id].value = 'true';
	
	setNodeText(row.cells[TAD_ADDITIONAL_INFO], testInfo, 20);
	setNodeText(row.cells[TAD_NOTES], notes, 30);
	

	testAdInfoDialog.cancel();
	return false;
}

function cancelTestAdInfo() {
	var id = document.getElementById('adTestAdInfoRowId').value;
	var table = document.getElementById('ad_test_info_table');
	var row = table.rows[parseInt(id) + 1]

	removeClassName(row, 'editing');
	testAdInfoDialog.hide();
	return false;
}