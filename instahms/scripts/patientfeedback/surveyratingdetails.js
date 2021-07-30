var surveyRatingDialog;

function validate() {
	var rating = document.getElementById('rating_type').value.trim();
	if (empty(rating)) {
		alert('Please enter rating type');
		document.getElementById('rating_type').focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	if(!ratingDetailsRowExists()) return false;

	if(!checkDuplicateRatingValue()) return false;

	return true;
}

function checkDuplicateRatingValue() {
	var ratingValArr = document.getElementsByName('rating_value');
	var deleted= document.getElementsByName('r_deleted');
	var ratingArr = new Array();
	for(var i=0;i<ratingValArr.length;i++) {
		ratingArr.push(ratingValArr[i].value);
	}
	ratingArr.sort();
	var last = ratingArr[0];
	var index = 0;
	for(var i=0;i<ratingArr.length;i++) {
		if((ratingArr[i]== last)) {
			index++;
		}
		last = ratingArr[i];
		if(index > 1) {
			alert("duplicate entry for rating value");
			return false;
		}
	}
	return true;
}

function ratingDetailsRowExists() {
	var table = document.getElementById('resultTable');
	var numRows = table.rows.length;
	var totalRowLen = (numRows-2);
	var deletedRows = document.getElementsByName('r_deleted');
	var index = 0;
	for(var i=0;i<deletedRows.length;i++) {
		if(deletedRows[i].value == 'Y') {
			index++;
		}
	}
	if((totalRowLen < 1) || (totalRowLen == index)) {
		alert("please add rating details");
		showSurveyRatingDialog();
		return false;
	}
	return true;
}

function checkDuplicate(){
	var newRatingName = trimAll(document.ratingMasterForm.rating_type.value);

	if(document.ratingMasterForm._method.value != 'update'){
		for(var i=0;i<chkRatingName.length;i++){
			item = chkRatingName[i];
			if (newRatingName == item.RATING_TYPE){
				alert(document.ratingMasterForm.rating_type.value+" already exists pls enter other name...");
		    	document.ratingMasterForm.rating_type.value='';
		    	document.ratingMasterForm.rating_type.focus();
		    	return false;
			}
		}
	}

	if(document.ratingMasterForm._method.value == 'update'){
		if (backupName != newRatingName){
			for(var i=0;i<chkRatingName.length;i++){
				item = chkRatingName[i];
				if(newRatingName == item.RATING_TYPE){
					alert(document.ratingMasterForm.rating_type.value+" already exists pls enter other name");
			    	document.ratingMasterForm.rating_type.focus();
			    	return false;
	 			}
	 		}
		}
	 }
	return true;
}

function initSurveyRatingDialog() {
	var surveyRatingDialogDiv = document.getElementById("ratingDialog");
	surveyRatingDialogDiv.style.display = 'block';
	surveyRatingDialog = new YAHOO.widget.Dialog("ratingDialog",{
			width:"450px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeSurveyRatingDialog,
	                                                scope:surveyRatingDialog,
	                                                correctScope:true } );
	surveyRatingDialog.cfg.queueProperty("keylisteners", escKeyListener);
	surveyRatingDialog.render();
}

function closeSurveyRatingDialog() {
	clearDialog();
	surveyRatingDialog.hide();
}

function clearDialog(){
	document.getElementById('d_rating_text').value = '';
	document.getElementById('d_rating_value').value = '';
	document.getElementById('dialogId').value = '';
}

var i=0;
	RATING_TEXT_COL=i++; RATING_VALUE_COL = i++;
    DELETE_COL=i++; EDIT_COL=i++;

function showSurveyRatingDialog() {
	var button = document.getElementById("btnAddItem");
	surveyRatingDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('ratingdialogheader').innerHTML = 'Add Survey Rating Details';
	surveyRatingDialog.show();
	document.ratingMasterForm.d_rating_text.focus();
}

function openEditSurveyRatingDialogBox(obj) {
	var rowObj = findAncestor(obj,"TR");
	var index = getRowItemIndex(rowObj);

	document.getElementById('ratingdialogheader').innerHTML = 'Edit Survey Rating Details';
	document.getElementById('dialogId').value = index;
	updateGridToDialog(obj,rowObj);
	document.ratingMasterForm.d_rating_text.focus();
}

function updateGridToDialog(obj,rowObj) {
	document.ratingMasterForm.d_rating_text.value 	=  getElementByName(rowObj,"rating_text").value;
	document.ratingMasterForm.d_rating_value.value =  getElementByName(rowObj,"rating_value").value;
	surveyRatingDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	surveyRatingDialog.show();
}


function cancelDialog() {
	clearDialog();
	surveyRatingDialog.cancel();
}

function addRecord() {
	var id = getNumItems();
	var dialogId = document.ratingMasterForm.dialogId.value;

	if (!empty(dialogId)) {
		var rowObj = getItemRow(dialogId);
		if (!validateDialog()) {
			return false;
		}
		updateRecordToGrid(rowObj);
		cancelDialog();
	} else {
		if (!validateDialog())
			return false;
		addRecordToGrid();
		showSurveyRatingDialog();
	}
}

//generic function to check is a string starting with passed character sequence as an argument of this function.
if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str){
    return this.indexOf(str) == 0;
  };
}

function validateDialog() {
	var ratingText = document.ratingMasterForm.d_rating_text.value;
	var ratingValue = document.ratingMasterForm.d_rating_value.value;

	if(empty(ratingText)) {
		alert("please enter rating text");
		document.ratingMasterForm.d_rating_text.focus();
		return false;
	}

	if(empty(ratingValue)) {
		alert("please enter rating value");
		document.ratingMasterForm.d_rating_value.focus();
		return false;
	} else if(ratingValue.startsWith('0')) {
		alert("enter a postive number");
		document.ratingMasterForm.d_rating_value.value = '';
		return false;
	}
	return true;
}

function addRecordToGrid() {
	var table = document.getElementById("resultTable");
	var id = getNumItems();
	var numRows = table.rows.length;
	var index = numRows-1;
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	var ratingText = document.ratingMasterForm.d_rating_text.value;
	var ratingValue = document.ratingMasterForm.d_rating_value.value;
	setNodeText(row.cells[RATING_TEXT_COL], !empty(ratingText) ? ratingText : '');
	setNodeText(row.cells[RATING_VALUE_COL], !empty(ratingValue) ? ratingValue : '');
	// the field names must match the db field names
	setHiddenValue(id, "rating_text", ratingText);
	setHiddenValue(id, "rating_id", "");
	setHiddenValue(id, "rating_value", ratingValue);
	setHiddenValue(id, "r_deleted", "N");

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", "Edit Rating Details");
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditSurveyRatingDialogBox(this)");
	editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
	deleteImg.setAttribute("title", "Delete Rating Details Row");
	deleteImg.setAttribute("onclick","deleteItem(this)");
	deleteImg.setAttribute("class", "button");

	for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
		row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
	}
	row.cells[DELETE_COL].appendChild(deleteImg);

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}
	row.cells[EDIT_COL].appendChild(editImg);
	clearDialog();
	document.getElementById('rating_text').focus();
}

function updateRecordToGrid(row) {
	var table = document.getElementById("resultTable");
	var ratingText = document.ratingMasterForm.d_rating_text.value;
	var ratingValue = document.ratingMasterForm.d_rating_value.value;

	getElementByName(row, "rating_text").value = ratingText;
	getElementByName(row, "rating_value").value = ratingValue;
	setNodeText(row.cells[RATING_TEXT_COL], !empty(ratingText) ? ratingText : '');
	setNodeText(row.cells[RATING_VALUE_COL], !empty(ratingValue) ? ratingValue : '');

	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", "Edit Rating Details");
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditSurveyRatingDialogBox(this)");
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", "Delete Rating Details Row");
		deleteImg.setAttribute("onclick","deleteItem(this)");
		deleteImg.setAttribute("class", "button");


	for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
		row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
	}
	row.cells[DELETE_COL].appendChild(deleteImg);

	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}
	row.cells[EDIT_COL].appendChild(editImg);
	document.getElementById('dialogId').value = getRowItemIndex(row);
	//clearDialog();
}

function deleteItem(imgObj,deleted) {
	var table = document.getElementById("resultTable");
	var rowObj = getThisRow(imgObj);
	var table = document.getElementById("resultTable");
	var deltedId = rowObj.rowIndex;
	var flag = false;
	var ratingId = getElementByName(rowObj,'rating_id').value;
	var deleteImg = document.createElement("img");
	var editImg = getElementByName(rowObj, "editIcon");
	if(empty(ratingId)) {
		if (!empty(deltedId)) {
			table.deleteRow(deltedId);
		}
	} else {
		if(deleted == 'Y') {
			imgObj.src = cpath + "/icons/Delete1.png";
			imgObj.setAttribute("title", " ");
			imgObj.setAttribute("onclick","deleteItem(this,'N')");
			imgObj.setAttribute("class", "button");
			editImg.src = cpath + "/icons/Edit1.png";
			editImg.setAttribute("title", " ");
			editImg.setAttribute("onclick","openEditSurveyRatingDialogBox(this)");
			editImg.setAttribute("class", "button");
			getElementByName(rowObj, "r_deleted").value = 'Y';
			rowObj.className = 'deletedRow';

		} else {
			imgObj.src = cpath + "/icons/Delete.png";
			imgObj.setAttribute("title", "delete rating details row");
			imgObj.setAttribute("onclick","deleteItem(this,'Y')");
			imgObj.setAttribute("class", "button");
			editImg.src = cpath + "/icons/Edit.png";
			editImg.setAttribute("title", "Edit Survey Rating Details");
			editImg.setAttribute("onclick","openEditSurveyRatingDialogBox(this)");
			editImg.setAttribute("class", "button");
			getElementByName(rowObj, "r_deleted").value = 'N';
			rowObj.className = '';
		}
	}
}

function getNumItems() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("resultTable").rows.length-2;
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num Information.

	return getNumItems() + 1;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.ratingMasterForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getFirstItemRow() {
	// index of the first Information item: 0 is header, 1 is first Information item.
	return 1;
}

function getTemplateRow() {
	// gets the hidden template row index: this follows header row + num Information.
	return getNumItems() + 1;
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getItemRow(i) {
	i = parseInt(i);
	var table = document.getElementById("resultTable");
	return table.rows[i + getFirstItemRow()];
}

