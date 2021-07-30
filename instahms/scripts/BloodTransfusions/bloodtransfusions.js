	function init() {
		initBloodTransfusionDialog();
	}

	var transfusionDialog;
	function initBloodTransfusionDialog() {
		var transfusionDialogDiv = document.getElementById("bloodTransfusionDialog");
		transfusionDialogDiv.style.display = '';
		transfusionDialog = new YAHOO.widget.Dialog("bloodTransfusionDialog",{
				width:"400px",
				context :["", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:closeTransfusionDialog,
		                                                scope:transfusionDialog,
		                                                correctScope:true } );
		transfusionDialog.cfg.queueProperty("keylisteners", escKeyListener);
		transfusionDialog.render();
	}

	function closeTransfusionDialog() {
		transfusionDialog.hide();
		clearDialog();
	}

	function showAddEditDialog(obj) {
		button = document.getElementById("btnAddItem");
		transfusionDialog.cfg.setProperty("context",[button, "tr", "br"], false);
		transfusionDialog.show();
	}

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.BloodTransfusionsForm, name, index);
		if (el) {
			if (value == null || value == undefined)
				value = "";
			el.value = value;
		}
	}
	function getNumItems() {
		// header, hidden template row: totally 3 extra
		return document.getElementById("resultTable").rows.length-2;
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

	var i=0;
	TRANSFUSION_DATE_COL=i++; TRANSFUSION_TYPE_COL = i++; BLOOD_BANK_COL = i++; BATCH_NO_COL = i++;EXPIRY_DATE_COL=i++;
    UNITS_TRANSFERRED_COL=i++; HBSAG_HCV_HIV_COL=i++; REACTION_COL=i++; REMARKS_COL=i++; DELETE_COL=i++; EDIT_COL=i++;

	function addRecord() {
		var dialogId = document.BloodTransfusionsForm.dialogId.value;
		if (validate() == true) {
			if (!empty(dialogId)) {
				var rowObj = getItemRow(dialogId);
				updateRecordToGrid(rowObj);
			} else {
				addRecordToGrid();
			}
		} else return false;
	}

	function validate() {
		var transfusionDate = document.getElementById('dialog_transfusion_date').value;
		var transfusionType = document.getElementById('dialog_transfusion_type').value;
		var bllodBank = document.getElementById('dialog_blood_bank').value;
		var batchNo = document.getElementById('dialog_batch_no').value;
		var expiryDate = document.getElementById('dialog_expiry_date').value;
		var transferredUnits = document.getElementById('dialog_transferred_units').value;
		var HbsAg_HCV_HIV = document.getElementById('dialog_HbsAg_HCV_HIV').value;
		var reaction = document.getElementById('dialog_reaction').value;
		var remarks = document.getElementById('dialog_remarks').value;

		if (!doValidateDateField(document.getElementById('dialog_transfusion_date'), "dd-MM-yyyy")) {
			return false;
		}

		if (!doValidateDateField( document.getElementById('dialog_expiry_date'), "dd-MM-yyyy")) {
			return false;
		}

		if (empty(transfusionDate)) {
			showMessage("js.clinicaldata.bloodtransfusions.transfusiondate.required");
			document.getElementById('dialog_transfusion_date').focus();
			return false;
		}
		if (empty(transfusionType)) {
			showMessage("js.clinicaldata.bloodtransfusions.transfusiontype.required");
			document.getElementById('dialog_transfusion_type').focus();
			return false;
		}

		return true;
	}

	function addRecordToGrid() {
		var id = getNumItems();
		var table = document.getElementById("resultTable");
		var len = table.rows.length;
		var tlen = len-2;
		var templateRow = table.rows[getTemplateRow()];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);

		var transfusionDate = document.getElementById('dialog_transfusion_date').value;
		var transfusionType = document.getElementById('dialog_transfusion_type').value;
		var bloodBank = document.getElementById('dialog_blood_bank').value;
		var batchNo = document.getElementById('dialog_batch_no').value;
		var expiryDate = document.getElementById('dialog_expiry_date').value;
		var transferredUnits = document.getElementById('dialog_transferred_units').value;
		var HbsAg_HCV_HIV = document.getElementById('dialog_HbsAg_HCV_HIV').value;
		var reaction = document.getElementById('dialog_reaction').value;
		var remarks = document.getElementById('dialog_remarks').value;
		var text_HbsAg_HCV_HIV = null;
			text_HbsAg_HCV_HIV = "Negative";

		var appendRemarks = null;
		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}
		var appendReaction = null;
		if (reaction.length >= 30) {
			appendRemarks = reaction.substring(0,30) + "...";
		}

		setNodeText(row.cells[TRANSFUSION_DATE_COL], !empty(transfusionDate) ? transfusionDate : '');
		setNodeText(row.cells[TRANSFUSION_TYPE_COL], !empty(transfusionType) ? transfusionType : '');
		setNodeText(row.cells[BLOOD_BANK_COL], !empty(bloodBank) ? bloodBank : '');
		setNodeText(row.cells[BATCH_NO_COL], batchNo);
		setNodeText(row.cells[EXPIRY_DATE_COL], expiryDate);
		setNodeText(row.cells[UNITS_TRANSFERRED_COL], transferredUnits);
		setNodeText(row.cells[HBSAG_HCV_HIV_COL], text_HbsAg_HCV_HIV);
		setNodeText(row.cells[REACTION_COL], reaction);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		setHiddenValue(id, "transfusion_date", transfusionDate);
		setHiddenValue(id, "transfusion_type", transfusionType);
		setHiddenValue(id, "blood_bank", bloodBank);
		setHiddenValue(id, "batch_no", batchNo);
		setHiddenValue(id, "expiry_date", expiryDate);
		setHiddenValue(id, "no_blood_units_transfused", transferredUnits);
		setHiddenValue(id, "check_for_HbsAg_HCV_HIV", "N");
		setHiddenValue(id, "transfusion_reaction", reaction);
		setHiddenValue(id, "remarks", remarks);

		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.clinicaldata.bloodtransfusions.editbloodtransfusion"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditDialogBox(this)");
		editImg.setAttribute("class", "button");

		var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.clinicaldata.bloodtransfusions.cancelbloodtransfusion"));
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
		openDialogBox(row);
	}

	function updateRecordToGrid(row) {
		var transfusionDate = document.getElementById('dialog_transfusion_date').value;
		var transfusionType = document.getElementById('dialog_transfusion_type').value;
		var bloodBank = document.getElementById('dialog_blood_bank').value;
		var batchNo = document.getElementById('dialog_batch_no').value;
		var expiryDate = document.getElementById('dialog_expiry_date').value;
		var transferredUnits = document.getElementById('dialog_transferred_units').value;
		var HbsAg_HCV_HIV = document.getElementById('dialog_HbsAg_HCV_HIV').value;
		var reaction = document.getElementById('dialog_reaction').value;
		var remarks = document.getElementById('dialog_remarks').value;
		var text_HbsAg_HCV_HIV = null;
			text_HbsAg_HCV_HIV = "Negative";

		var appendRemarks = null;
		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[TRANSFUSION_DATE_COL], !empty(transfusionDate) ? transfusionDate : '');
		setNodeText(row.cells[TRANSFUSION_TYPE_COL], !empty(transfusionType) ? transfusionType : '');
		setNodeText(row.cells[BLOOD_BANK_COL], !empty(bloodBank) ? bloodBank : '');
		setNodeText(row.cells[BATCH_NO_COL], batchNo);
		setNodeText(row.cells[EXPIRY_DATE_COL], expiryDate);
		setNodeText(row.cells[UNITS_TRANSFERRED_COL], transferredUnits);
		setNodeText(row.cells[HBSAG_HCV_HIV_COL], text_HbsAg_HCV_HIV);
		setNodeText(row.cells[REACTION_COL], reaction);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		getElementByName(row,"transfusion_date").value = transfusionDate;
		getElementByName(row,"transfusion_type").value = transfusionType;
		getElementByName(row,"blood_bank").value = bloodBank;
		getElementByName(row,"batch_no").value = batchNo;
		getElementByName(row,"expiry_date").value = expiryDate;
		getElementByName(row,"no_blood_units_transfused").value = transferredUnits;
		getElementByName(row,"check_for_HbsAg_HCV_HIV").value = "N";
		getElementByName(row,"transfusion_reaction").value = reaction;
		getElementByName(row,"remarks").value = remarks;
		clearDialog();
		transfusionDialog.cancel();;
	}

	function clearDialog() {
		document.getElementById('dialog_transfusion_date').value = '';
		document.getElementById('dialog_transfusion_type').value = '';
		document.getElementById('dialog_blood_bank').value = '';
		document.getElementById('dialog_batch_no').value = '';
		document.getElementById('dialog_expiry_date').value = '';
		document.getElementById('dialog_transferred_units').value = '';
		document.getElementById('dialog_HbsAg_HCV_HIV').value = '';
		document.getElementById('dialog_reaction').value = '';
		document.getElementById('dialog_remarks').value = ''
		document.BloodTransfusionsForm.dialogId.value = '';
	}

	function openDialogBox() {
		document.getElementById('dialog_transfusion_date').value = '';
		document.getElementById('dialog_transfusion_type').value = '';
		document.getElementById('dialog_blood_bank').value = '';
		document.getElementById('dialog_batch_no').value = '';
		document.getElementById('dialog_expiry_date').value = '';
		document.getElementById('dialog_transferred_units').value = '';
		document.getElementById('dialog_HbsAg_HCV_HIV').value = '';
		document.getElementById('dialog_reaction').value = '';
		document.getElementById('dialog_remarks').value = ''
		button = document.getElementById("btnAddItem");
		transfusionDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.bloodtransfusions.addbloodtransfusion');
		document.BloodTransfusionsForm.dialog_transfusion_date.focus();
		transfusionDialog.show();
	}

	function cancelDialog() {
		transfusionDialog.cancel();
		clearDialog();
	}

	function openEditDialogBox(obj) {
		rowObj = findAncestor(obj,"TR");
		rowObj.className = 'selectedRow';
		updateGridToDialog(rowObj);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.bloodtransfusions.editbloodtransfusion');
		transfusionDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		document.BloodTransfusionsForm.dialog_transfusion_date.focus();
	}

	function updateGridToDialog(rowObj){
		document.BloodTransfusionsForm.dialog_transfusion_date.value 	=  getElementByName(rowObj,"transfusion_date").value;
		document.BloodTransfusionsForm.dialog_transfusion_type.value 	=  getElementByName(rowObj,"transfusion_type").value;
		document.BloodTransfusionsForm.dialog_blood_bank.value 	=  getElementByName(rowObj,"blood_bank").value;
		document.BloodTransfusionsForm.dialog_batch_no.value 	=  getElementByName(rowObj,"batch_no").value;
		document.BloodTransfusionsForm.dialog_expiry_date.value 	=  getElementByName(rowObj,"expiry_date").value;
		document.BloodTransfusionsForm.dialog_transferred_units.value 	=  getElementByName(rowObj,"no_blood_units_transfused").value;
		document.BloodTransfusionsForm.dialog_HbsAg_HCV_HIV.value 	=  getElementByName(rowObj,"check_for_HbsAg_HCV_HIV").value;
		document.BloodTransfusionsForm.dialog_reaction.value 	=  getElementByName(rowObj,"transfusion_reaction").value;
		document.BloodTransfusionsForm.dialog_remarks.value 	=  getElementByName(rowObj,"remarks").value;
		document.BloodTransfusionsForm.dialogId.value = getRowItemIndex(rowObj);
		transfusionDialog.show();
	}

	function deleteItem(imgObj) {
		var rowObj = getThisRow(imgObj);
		var deletedInput = getElementByName(rowObj,"hdeleted").value;
		var trashImgObj = imgObj;
		var editImgObj = getElementByName(rowObj,"editIcon");

		if (deletedInput == 'false') {
			var ok = confirm("do you wish to delete this entry");
			if (!ok)
				return false;

			getElementByName(rowObj,"hdeleted").value = 'true';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete1.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.bloodtransfusions.enableddeletedbloodtransfusion'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
			rowObj.setAttribute("class","deletedRow");
		} else {
			getElementByName(rowObj,"hdeleted").value = 'false';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.bloodtransfusions.deletedbloodtransfusion'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.clinicaldata.bloodtransfusions.editbloodtransfusion"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
			rowObj.setAttribute("class","");
		}
	}

	function validateForm() {
		var dataAsOfDate = document.getElementById('data_as_of_date');
		var numRows = getNumItems();
		var allChecked = false;

		if (empty(dataAsOfDate.value)) {
			showMessage("js.clinicaldata.bloodtransfusions.dateasofdate.required");
			document.getElementById('data_as_of_date').focus();
			return false;
		}
		return true;
	}

	function checkLength(obj,len,field){
		if( obj.value.length  > len ){
			var msg=getString("js.clinicaldata.bloodtransfusions.max");
			msg+=len;
			msg+=getString("js.clinicaldata.bloodtransfusions.charsallowed");
			msg+=field;
			alert(msg);
			obj.focus();
			return false;
		}
		return true;
	}