function keepBackUp(){
	if(document.feedbackSectionSearchForm._method.value == 'updateSection'){
		backupName = document.feedbackSectionSearchForm.section_title.value;
		sectionOrderBackupName  = document.getElementById('section_order').value;;
	}
	initQuestionRatingDialog();
	document.getElementById('d_question_rating').disabled = true;
}

function hideUnHideInactiveQuestions(obj) {
	var hideQuestions = document.getElementsByName('hide_question');
	var questionStatus = document.getElementsByName('question_status');
	var index = 1;

	if(obj.checked) {
		for(var i=1;i<hideQuestions.length;i++) {
			if(questionStatus[i-1].value == 'I') {
				document.getElementById('hide_question_row'+i).style.display = 'none';
			}
		}
	} else {
		for(var i=1;i<hideQuestions.length;i++) {
			if(questionStatus[i-1].value == 'I') {
				document.getElementById('hide_question_row'+i).style.display = '';
			}
		}
	}
}

//generic function to check is a string starting with passed character sequence as an argument of this function.
if (typeof String.prototype.startsWith != 'function') {
  String.prototype.startsWith = function (str){
    return this.indexOf(str) == 0;
  };
}


function validate() {
	var sectionTitle = document.getElementById('section_title').value.trim();
	var sectionOrder = document.getElementById('section_order').value;

	if (empty(sectionTitle)) {
		alert('Please enter section title');
		document.getElementById('section_title').focus();
		return false;
	}

	if (empty(sectionOrder)) {
		alert('Please enter section order');
		document.getElementById('section_order').focus();
		return false;
	}

	if(!checkDuplicateQuestionOrderValue()) return false;

	if(!checkSectionOrderDuplicate()) return false;

	if (!checkDuplicateSectionTitle()) return false;

	if(!imposeMaxLength(document.feedbackSectionSearchForm.section_instructions,'Section Instructions')) return false;

	return true;
}

function checkSectionOrderDuplicate() {
	var newSectionOrder = document.getElementById('section_order').value;

	if(newSectionOrder.length > 1 && newSectionOrder.startsWith('0')) {
		newSectionOrder = newSectionOrder.substring(1,newSectionOrder.length);
	}

	if(document.feedbackSectionSearchForm._method.value != 'updateSection'){
		for(var i=0;i<chkSectionName.length;i++){
			item = chkSectionName[i];
			if (newSectionOrder == item.SECTION_ORDER){
				alert("duplicate value for section order.");
		    	document.feedbackSectionSearchForm.section_order.value='';
		    	document.feedbackSectionSearchForm.section_order.focus();
		    	return false;
			}
		}
	}

	if(document.feedbackSectionSearchForm._method.value == 'updateSection'){
		for(var i=0;i<chkSectionName.length;i++){
			if (sectionOrderBackupName != newSectionOrder){
				item = chkSectionName[i];
				if (newSectionOrder == item.SECTION_ORDER){
					alert("duplicate value for section order.");
			    	document.feedbackSectionSearchForm.section_order.value='';
			    	document.feedbackSectionSearchForm.section_order.focus();
			    	return false;
				}
			}
		}
	}

	return true;
}

function checkDuplicateSectionTitle(){
	var newSectionName = trimAll(document.feedbackSectionSearchForm.section_title.value);

	if(document.feedbackSectionSearchForm._method.value != 'updateSection'){
		for(var i=0;i<chkSectionName.length;i++){
			item = chkSectionName[i];
			if (newSectionName == item.SECTION_TITLE){
				alert(document.feedbackSectionSearchForm.section_title.value+" already exists pls enter other name...");
		    	document.feedbackSectionSearchForm.section_title.value='';
		    	document.feedbackSectionSearchForm.section_title.focus();
		    	return false;
			}
		}
	}

	if(document.feedbackSectionSearchForm._method.value == 'updateSection'){
	  		if (backupName != newSectionName){
				for(var i=0;i<chkSectionName.length;i++){
					item = chkSectionName[i];
					if(newSectionName == item.SECTION_TITLE){
						alert(document.feedbackSectionSearchForm.section_title.value+" already exists pls enter other name");
				    	document.feedbackSectionSearchForm.section_title.focus();
				    	return false;
	  				}
	  			}
	 		}
	 	}
		return true;
	}

function imposeMaxLength(obj,text){
	var objDesc = obj.value;
	var newLines = objDesc.split("\n").length;
	var length = objDesc.length + newLines;
	var fixedLen = (text == 'Section Instructions') ? 3000 : 1000;
	if (length > fixedLen) {
		alert(text+" can not be more than" +fixedLen +" characters");
		obj.focus();
		return false;
	}
	return true;
}

var questionDialog;
function initQuestionRatingDialog() {
	var questionDialogDiv = document.getElementById("questionDialog");
	questionDialogDiv.style.display = 'block';
	questionDialog = new YAHOO.widget.Dialog("questionDialog",{
			width:"700px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeQuestionDialog,
	                                                scope:questionDialog,
	                                                correctScope:true } );
	questionDialog.cfg.queueProperty("keylisteners", escKeyListener);
	questionDialog.render();
}

function closeQuestionDialog() {
	clearDialog();
	questionDialog.hide();
}

function cancelDialog() {
	clearDialog();
	questionDialog.cancel();
}

function clearDialog(){
	document.feedbackSectionSearchForm.d_question_detail.value = '';
	document.getElementById('d_question_order').value = '';
	document.getElementById('d_question_category').value = '';
	document.getElementById('d_response_type').value = '';
	document.getElementById('d_question_rating').value = '';
	document.getElementById('d_question_status').value = 'A';
	document.getElementById('d_question_rating').disabled = true;
	document.getElementById('dialogId').value = '';
	document.getElementById('question_rating_span').style.display = 'none';
}
function sortNumber(a,b) {
	return a-b;
}
function checkDuplicateQuestionOrderValue() {
	var questionOrderVal = document.getElementsByName('question_order');

	var orderArr = new Array();
	for(var i=0;i<questionOrderVal.length;i++) {
		if(!empty(questionOrderVal))
			orderArr.push(questionOrderVal[i].value);
	}
	orderArr.sort(sortNumber);
	var index = 0;
	var last = orderArr[0];
	for(var i=0;i<orderArr.length;i++) {
		if((orderArr[i] == last)) {
			index++;
		}
		last = orderArr[i];
		if(index > 1) {
			alert("duplicate entry for question order value");
			return false;
		}
	}
	return true;
}


var i=0;
	QUESTION_DETAIL_COL=i++;QUESTION_STATUS_COL=i++; QUESTION_ORDER_COL = i++;QUESTION_CATEGORY_COL=i++;QUESTION_RATING_TYPE_COL=i++;
    DELETE_COL=i++; EDIT_COL=i++;

function showQuestionDialog() {
	var button = document.getElementById("btnAddItem");
	questionDialog.cfg.setProperty("context",[button, "tr", "br"], false);
	document.getElementById('questiondialogheader').innerHTML = 'Add Question Details';
	document.feedbackSectionSearchForm.d_response_type.disabled = false;
	questionDialog.show();
	document.feedbackSectionSearchForm.d_question_detail.focus();
}

function openEditQuestionDetailsDialogBox(obj) {
	var rowObj = findAncestor(obj,"TR");
	var index = getRowItemIndex(rowObj);
	var questionResponseTypeValue = getElementByName(rowObj, "response_type").value;
	var questionId = getElementByName(rowObj, "question_id").value;

	document.getElementById('questiondialogheader').innerHTML = 'Edit Question Details';
	document.getElementById('dialogId').value = index;
	if(empty(questionId)) {
		document.feedbackSectionSearchForm.d_response_type.disabled = false;
		if(questionResponseTypeValue == 'R') {
			document.feedbackSectionSearchForm.d_question_rating.disabled = false;
			document.getElementById('question_rating_span').style.display = '';
		} else {
			document.feedbackSectionSearchForm.d_question_rating.disabled = true;
			document.getElementById('question_rating_span').style.display = 'none';
		}
	} else {
		document.feedbackSectionSearchForm.d_response_type.disabled = true;
		document.feedbackSectionSearchForm.d_question_rating.disabled = true;
		if(questionResponseTypeValue == 'R') {
			document.getElementById('question_rating_span').style.display = '';
		} else {
			document.getElementById('question_rating_span').style.display = 'none';
		}
	}
	updateGridToDialog(obj,rowObj);
	document.feedbackSectionSearchForm.d_question_detail.focus();
}

function enableRating(obj) {
	if(obj.value == 'R') {
		document.feedbackSectionSearchForm.d_question_rating.disabled = false;
		document.getElementById('question_rating_span').style.display = '';
	} else {
		document.feedbackSectionSearchForm.d_question_rating.disabled = true;
		document.feedbackSectionSearchForm.d_question_rating.value = '';
		document.getElementById('question_rating_span').style.display = 'none';
	}
}

function validateDialog() {
	var questionDetails = document.feedbackSectionSearchForm.d_question_detail.value.trim();
	var questionOrder = document.feedbackSectionSearchForm.d_question_order.value;
	var questionCategory = document.feedbackSectionSearchForm.d_question_category.value;
	var questionResponseType = document.feedbackSectionSearchForm.d_response_type.value;
	var questionRating = document.feedbackSectionSearchForm.d_question_rating.value;

	if(empty(questionDetails)) {
		alert("please enter question details");
		document.feedbackSectionSearchForm.d_question_detail.focus();
		return false;
	}

	if(!imposeMaxLength(document.feedbackSectionSearchForm.d_question_detail,"Question Details")) return false;

	if(empty(questionOrder)) {
		alert("please enter question order");
		document.feedbackSectionSearchForm.d_question_detail.focus();
		return false;
	} else if(questionOrder.length > 1 && questionOrder.startsWith('0')) {
		alert("enter a postive number");
		document.feedbackSectionSearchForm.d_question_order.value = '';
		document.feedbackSectionSearchForm.d_question_order.focus();
		return false;
	}

	if(empty(questionCategory)) {
		alert("please select a question category");
		document.feedbackSectionSearchForm.d_question_category.focus();
		return false;
	}

	if(empty(questionResponseType)) {
		alert("please select a response type");
		document.feedbackSectionSearchForm.d_response_type.focus();
		return false;
	}
	if(questionResponseType == 'R') {
		if(empty(questionRating)) {
			alert("please select a question rating");
			document.feedbackSectionSearchForm.d_question_rating.focus();
			return false;
		}
	}
	return true;
}

function addRecord() {
	var id = getNumItems();
	var dialogId = document.feedbackSectionSearchForm.dialogId.value;

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
		showQuestionDialog();
	}
}

function updateGridToDialog(obj,rowObj) {
	document.feedbackSectionSearchForm.d_question_detail.value	=  getElementByName(rowObj,"question_detail").value;
	document.feedbackSectionSearchForm.d_question_order.value =  getElementByName(rowObj,"question_order").value;
	document.feedbackSectionSearchForm.d_question_category.value =  getElementByName(rowObj,"category_id").value;
	document.feedbackSectionSearchForm.d_response_type.value =  getElementByName(rowObj,"response_type").value;
	document.feedbackSectionSearchForm.d_question_status.value =  getElementByName(rowObj,"q_status").value;
	if(document.feedbackSectionSearchForm.d_response_type.value == 'R')
		document.feedbackSectionSearchForm.d_question_rating.value =  getElementByName(rowObj,"rating_type_id").value;
	questionDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	questionDialog.show();
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
	var questionDetails = document.feedbackSectionSearchForm.d_question_detail.value.trim();
	var questionStatusValue =  document.feedbackSectionSearchForm.d_question_status.value;
	var questionStatusText = document.feedbackSectionSearchForm.d_question_status.options[document.feedbackSectionSearchForm.d_question_status.selectedIndex].text;
	var questionOrder = document.feedbackSectionSearchForm.d_question_order.value;
	var questionCategoryText = document.feedbackSectionSearchForm.d_question_category.options[document.feedbackSectionSearchForm.d_question_category.selectedIndex].title;
	var questionCategoryValue = document.feedbackSectionSearchForm.d_question_category.value;
	var questionResponseTypeText = document.feedbackSectionSearchForm.d_response_type.options[document.feedbackSectionSearchForm.d_response_type.selectedIndex].text;
	var questionResponseTypeValue = document.feedbackSectionSearchForm.d_response_type.value;
	var questionRatingText = document.feedbackSectionSearchForm.d_question_rating.options[document.feedbackSectionSearchForm.d_question_rating.selectedIndex].title;
	var questionRatingValue = document.feedbackSectionSearchForm.d_question_rating.value;
	setNodeText(row.cells[QUESTION_DETAIL_COL], (!empty(questionDetails) ? questionDetails : ''), 35, questionDetails);
	setNodeText(row.cells[QUESTION_STATUS_COL],questionStatusText);
	setNodeText(row.cells[QUESTION_ORDER_COL], !empty(questionOrder) ? questionOrder : '');
	setNodeText(row.cells[QUESTION_CATEGORY_COL], (!empty(questionCategoryText) ? questionCategoryText : ''), 35, questionCategoryText);
	if(questionResponseTypeValue == 'R')
		setNodeText(row.cells[QUESTION_RATING_TYPE_COL], (!empty(questionRatingText) ? questionRatingText : ''), 35, questionRatingText);
	else
		setNodeText(row.cells[QUESTION_RATING_TYPE_COL], !empty(questionResponseTypeText) ? questionResponseTypeText : '');

	// the field names must match the db field names
	setHiddenValue(id, "question_detail", questionDetails);
	setHiddenValue(id, "q_status", questionStatusValue);
	setHiddenValue(id, "question_order", questionOrder);
	setHiddenValue(id, "category_id", questionCategoryValue);
	setHiddenValue(id, "response_type", questionResponseTypeValue);
	setHiddenValue(id, "rating_type_id", questionRatingValue);
	setHiddenValue(id, "r_deleted", "N");

	var editImg = document.createElement("img");
	editImg.setAttribute("src", cpath + "/icons/Edit.png");
	editImg.setAttribute("title", "Edit Question Details");
	editImg.setAttribute("name", "editIcon");
	editImg.setAttribute("id", "editIcon");
	editImg.setAttribute("onclick","openEditQuestionDetailsDialogBox(this)");
	editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
	deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
	deleteImg.setAttribute("title", "Delete Question Details Row");
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
	document.feedbackSectionSearchForm.d_question_detail.focus();
}

function updateRecordToGrid(row) {
	var table = document.getElementById("resultTable");
	var questionDetails = document.feedbackSectionSearchForm.d_question_detail.value.trim();
	var questionStatusValue =  document.feedbackSectionSearchForm.d_question_status.value;
	var questionStatusText = document.feedbackSectionSearchForm.d_question_status.options[document.feedbackSectionSearchForm.d_question_status.selectedIndex].text;
	var questionOrder = document.feedbackSectionSearchForm.d_question_order.value;
	var questionCategoryText = document.feedbackSectionSearchForm.d_question_category.options[document.feedbackSectionSearchForm.d_question_category.selectedIndex].title;
	var questionCategoryValue = document.feedbackSectionSearchForm.d_question_category.value;
	var questionResponseTypeText = document.feedbackSectionSearchForm.d_response_type.options[document.feedbackSectionSearchForm.d_response_type.selectedIndex].text;
	var questionResponseTypeValue = document.feedbackSectionSearchForm.d_response_type.value;
	var questionRatingText = document.feedbackSectionSearchForm.d_question_rating.options[document.feedbackSectionSearchForm.d_question_rating.selectedIndex].title;
	var questionRatingValue = document.feedbackSectionSearchForm.d_question_rating.value;

	getElementByName(row, "question_detail").value = questionDetails;
	getElementByName(row, "q_status").value = questionStatusValue;
	getElementByName(row, "question_order").value = questionOrder;
	getElementByName(row, "category_id").value = questionCategoryValue;
	getElementByName(row, "response_type").value = questionResponseTypeValue;
	getElementByName(row, "rating_type_id").value = (questionResponseTypeValue == 'R') ? questionRatingValue : '';
	getElementByName(row, "r_deleted").value = "N";

	var questionId = getElementByName(row, "question_id").value

	setNodeText(row.cells[QUESTION_DETAIL_COL], (!empty(questionDetails) ? questionDetails : ''), 35, questionDetails);
	setNodeText(row.cells[QUESTION_STATUS_COL], questionStatusText);
	setNodeText(row.cells[QUESTION_ORDER_COL], !empty(questionOrder) ? questionOrder : '');
	setNodeText(row.cells[QUESTION_CATEGORY_COL], (!empty(questionCategoryText) ? questionCategoryText : ''), 35, questionCategoryText);
	if(questionResponseTypeValue == 'R')
		setNodeText(row.cells[QUESTION_RATING_TYPE_COL], (!empty(questionRatingText) ? questionRatingText : ''), 35, questionRatingText);
	else
		setNodeText(row.cells[QUESTION_RATING_TYPE_COL], !empty(questionResponseTypeText) ? questionResponseTypeText : '');

	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", "Edit Question Details");
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditQuestionDetailsDialogBox(this)");
		editImg.setAttribute("class", "button");

	var deleteImg = document.createElement("img");
		if(empty(questionId)) {
			deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
			deleteImg.setAttribute("title", "Delete Question Details Row");
			deleteImg.setAttribute("onclick","deleteItem(this)");
		}
		else {
			deleteImg.setAttribute("src", cpath + "/icons/Delete1.png");
			deleteImg.setAttribute("title", "");
			deleteImg.setAttribute("onclick","");
		}
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
	var deltedId = rowObj.rowIndex;
	var flag = false;
	var deleteImg = document.createElement("img");
	var editImg = getElementByName(rowObj, "editIcon");
	var questionId = getElementByName(rowObj, "question_id").value
	if(empty(questionId)) {
		if (!empty(deltedId)) {
			table.deleteRow(deltedId);
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
	var el = getIndexedFormElement(document.feedbackSectionSearchForm, name, index);
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


