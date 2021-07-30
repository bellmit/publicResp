	function init() {
		initVaccinationDialog();
	}

	var vaccinationDialog;
	function initVaccinationDialog() {
		var vaccinationDialogDiv = document.getElementById("vaccinationDialog");
		vaccinationDialogDiv.style.display = 'block';
		vaccinationDialog = new YAHOO.widget.Dialog("vaccinationDialog",{
				width:"400px",
				context :["", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:closevaccinationDialog,
		                                                scope:vaccinationDialog,
		                                                correctScope:true } );
		vaccinationDialog.cfg.queueProperty("keylisteners", escKeyListener);
		vaccinationDialog.render();
	}

	function closevaccinationDialog() {
		vaccinationDialog.hide();
		clearDialog();
	}

	function showAddEditDialog(obj) {
		button = document.getElementById("btnAddItem");
		vaccinationDialog.cfg.setProperty("context",[button, "tr", "br"], false);
		vaccinationDialog.show();
	}

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.VaccinationForm, name, index);
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
	VACCINATION_TYPE_COL=i++; VACCINATION_STATUS_COL = i++; NO_REASON_COL = i++; ADMINISTERED_DATE_COL = i++;REMARKS_COL=i++;
    DELETE_COL=i++; EDIT_COL=i++;

	function addRecord() {
		var dialogId = document.VaccinationForm.dialogId.value;
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
		var vaccinationType = document.getElementById('dialog_vaccination_type').value;
		var vaccinTypeRecord = findInList(reasonListJson,'vaccination_type_id',vaccinationType);
		var vaccinationStatus = document.getElementById('dialog_vaccination_status').value;
		var administeredDate = document.getElementById('dialog_administered_date').value;
		var dueDate = document.getElementById('dialog_due_date').value;
		var reasonId = document.getElementById('dialog_reason_name').value;
		var record = findInList(vaccinationTypeListJson,'reason_id',reasonId);
		var remarks = document.getElementById('dialog_remarks').value;

		if (!doValidateDateField(document.getElementById('dialog_administered_date'), "dd-MM-yyyy")) {
			return false;
		}

		if (!doValidateDateField(document.getElementById('dialog_due_date'), "dd-MM-yyyy")) {
			return false;
		}

		if (empty(vaccinationType)) {
			showMessage("js.clinicaldata.vaccinations.vaccination.required");
			document.getElementById('dialog_vaccination_type').focus();
			return false;
		}
		if (empty(vaccinationStatus)) {
			showMessage("js.clinicaldata.vaccinations.status.required");
			document.getElementById('dialog_vaccination_status').focus();
			return false;
		}
		if (vaccinationStatus != null && vaccinationStatus == 'N') {
			if (empty(reasonId)) {
				showMessage("js.clinicaldata.vaccinations.reason.required");
				document.getElementById('dialog_reason_name').focus();
				return false;
			}
		}
		if (vaccinationStatus == 'Y') {
			if (empty(administeredDate)) {
				showMessage("js.clinicaldata.vaccinations.administereddate.required");
				document.getElementById('dialog_administered_date').focus();
				return false;
			}
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

		var vaccinationType = document.getElementById('dialog_vaccination_type').value;
		var vaccinTypeRecord = findInList(vaccinationTypeListJson,'vaccination_type_id',vaccinationType);
		var vaccinationStatus = document.getElementById('dialog_vaccination_status').value;
		var vaccinStatus = null;
		if (vaccinationStatus == 'Y') {
			vaccinStatus = "Yes";
		} else if (vaccinationStatus == 'N') {
			vaccinStatus = "No";
		} else if (vaccinationStatus == 'R') {
			vaccinStatus = "Refused";
		}
		var administeredDate = document.getElementById('dialog_administered_date').value;
		var dueDate = document.getElementById('dialog_due_date').value;
		var reasonId = document.getElementById('dialog_reason_name').value;
		var record = findInList(reasonListJson,'reason_id',reasonId);
		var remarks = document.getElementById('dialog_remarks').value;

		var appendRemarks = null;
		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[VACCINATION_TYPE_COL], !empty(vaccinTypeRecord) ? vaccinTypeRecord.vaccination_type : '');
		setNodeText(row.cells[VACCINATION_STATUS_COL], !empty(vaccinStatus) ? vaccinStatus : '');
		setNodeText(row.cells[NO_REASON_COL], !empty(record) ? record.reason_name : '');
		setNodeText(row.cells[ADMINISTERED_DATE_COL], administeredDate);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		setHiddenValue(id, "vaccination_type_id", vaccinationType);
		setHiddenValue(id, "vaccination_status", vaccinationStatus);
		setHiddenValue(id, "vaccination_date", administeredDate);
		setHiddenValue(id, "next_due_date", dueDate);
		setHiddenValue(id, "no_reason_id", reasonId);
		setHiddenValue(id, "remarks", remarks);

		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title",getString("js.clinicaldata.vaccinations.editvaccination"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditDialogBox(this)");
		editImg.setAttribute("class", "button");

		var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.clinicaldata.vaccinations.cancelvaccination"));
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
		var vaccinationType = document.getElementById('dialog_vaccination_type').value;
		var vaccinTypeRecord = findInList(vaccinationTypeListJson,'vaccination_type_id',vaccinationType);
		var vaccinationStatus = document.getElementById('dialog_vaccination_status').value;
		var vaccinStatus = null;
		if (vaccinationStatus == 'Y') {
			vaccinStatus = "Yes";
		} else if (vaccinationStatus == 'N') {
			vaccinStatus = "No";
		} else if (vaccinationStatus == 'R') {
			vaccinStatus = "Refused";
		}
		var administeredDate = document.getElementById('dialog_administered_date').value;
		var dueDate = document.getElementById('dialog_due_date').value;
		var reasonId = document.getElementById('dialog_reason_name').value;
		var record = findInList(reasonListJson,'reason_id',reasonId);
		var remarks = document.getElementById('dialog_remarks').value;
		var appendRemarks = null;
		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[VACCINATION_TYPE_COL], !empty(vaccinTypeRecord) ? vaccinTypeRecord.vaccination_type : '');
		setNodeText(row.cells[VACCINATION_STATUS_COL], !empty(vaccinStatus) ? vaccinStatus : '');
		setNodeText(row.cells[NO_REASON_COL], !empty(record) ? record.reason_name : '');
		setNodeText(row.cells[ADMINISTERED_DATE_COL], administeredDate);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		getElementByName(row,"vaccination_type_id").value = vaccinationType;
		getElementByName(row,"vaccination_status").value = vaccinationStatus;
		getElementByName(row,"vaccination_date").value = administeredDate;
		getElementByName(row,"next_due_date").value = dueDate;
		getElementByName(row,"no_reason_id").value = reasonId;
		getElementByName(row,"remarks").value = remarks;

		clearDialog();
		vaccinationDialog.cancel();;
	}

	function clearDialog() {
		document.getElementById('dialog_vaccination_type').value = '';
		document.getElementById('dialog_vaccination_status').value = '';
		document.getElementById('dialog_administered_date').value = '';
		document.getElementById('dialog_due_date').value = '';
		document.getElementById('dialog_reason_name').value = '';
		document.getElementById('dialog_remarks').value = '';
		document.VaccinationForm.dialogId.value = '';
	}

	function openDialogBox() {
		document.getElementById('dialog_vaccination_type').value = '';
		document.getElementById('dialog_vaccination_status').value = '';
		document.getElementById('dialog_administered_date').value = '';
		document.getElementById('dialog_due_date').value = '';
		document.getElementById('dialog_reason_name').value = '';
		document.getElementById('dialog_remarks').value = '';
		document.getElementById('trId').style.display = "none";
		button = document.getElementById("btnAddItem");
		vaccinationDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.vaccinations.addvaccination');
		document.VaccinationForm.dialog_vaccination_type.focus();
		vaccinationDialog.show();
	}

	function cancelDialog() {
		vaccinationDialog.cancel();
		clearDialog();
	}

	function openEditDialogBox(obj) {
		rowObj = findAncestor(obj,"TR");
		rowObj.className = 'selectedRow';
		var reasonName = getElementByName(rowObj,"no_reason_id").value;
		if (!empty(reasonName)) {
			document.getElementById('trId').style.display = "";
		} else {
			document.getElementById('trId').style.display = "none";
		}
		updateGridToDialog(rowObj);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.vaccinations.editvaccination');
		vaccinationDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		document.VaccinationForm.dialog_vaccination_type.focus();
	}

	function updateGridToDialog(rowObj){
		document.VaccinationForm.dialog_vaccination_type.value 	=  getElementByName(rowObj,"vaccination_type_id").value;
		document.VaccinationForm.dialog_vaccination_status.value 	=  getElementByName(rowObj,"vaccination_status").value;
		document.VaccinationForm.dialog_administered_date.value 	=  getElementByName(rowObj,"vaccination_date").value;
		document.VaccinationForm.dialog_due_date.value 	=  getElementByName(rowObj,"next_due_date").value;
		document.VaccinationForm.dialog_reason_name.value 	=  getElementByName(rowObj,"no_reason_id").value;
		document.VaccinationForm.dialog_remarks.value 	=  getElementByName(rowObj,"remarks").value;
		document.VaccinationForm.dialogId.value = getRowItemIndex(rowObj);
		vaccinationDialog.show();
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
			trashImgObj.setAttribute('title',getString('js.clinicaldata.vaccinations.enabledeletevaccination'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
		} else {
			getElementByName(rowObj,"hdeleted").value = 'false';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.vaccinations.deletevaccination'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.clinicaldata.vaccinations.editvaccination"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
		}
	}

	function validateForm() {
		var dataAsOfDate = document.getElementById('data_as_of_date');
		var numRows = getNumItems();
		var allChecked = false;

		if (empty(dataAsOfDate.value)) {
			showMessage("js.clinicaldata.vaccinations.dateasofdate.required");
			document.getElementById('data_as_of_date').focus();
			return false;
		}

/*	if (numRows <= 0) {
			showMessage("there is no records to save in the grid"+"\n"+"pls add one or more records.");
			document.getElementById('btnAddItem').focus();
 		    return false;
		}

		for (var k=0;k<numRows;k++) {
	    	arowObj = getItemRow(k);
	    	if (getElementByName(arowObj,'hdeleted').value == 'false') {
	    		allChecked = true;
	    	}
	    }

	    if (!allChecked) {
	    	showMessage("all row(s) in the grid are deleted \n so no record(s) to save");
 		    return false;
	    }*/

		return true;
	}

	function checkLength(obj,len,field){
		if( obj.value.length  > len ){
			var msg=getString("js.clinicaldata.vaccinations.max");
			msg+=len;
			msg+=getString("js.clinicaldata.vaccinations.charsallowed");
			msg+=field;
			alert(msg);
			obj.focus();
			return false;
		}
		return true;
	}

	function showReason(obj) {
		if (obj != null && obj.value == 'N') {
			document.getElementById('trId').style.display = "";
		} else {
			document.getElementById('trId').style.display = "none";
		}
	}

	function calculateNextDueDate(){
		var vacTypeId = document.getElementById('dialog_vaccination_type').value;
		if (empty(vacTypeId)) {
			showMessage("js.clinicaldata.vaccinations.vaccination.nextdue");
			document.getElementById('dialog_vaccination_type').focus();
			return false;
		}
		if (empty(document.getElementById('dialog_administered_date').value)) {
			showMessage("js.clinicaldata.vaccinations.vaccinationadministered.nextdue");
			document.getElementById('dialog_administered_date').focus();
			return false;
		}
		var vaccinationDate = parseDateStr(document.getElementById('dialog_administered_date').value);
		var vacDay = vaccinationDate.getDay();
		var vacMonth = vaccinationDate.getMonth();
		var vacYear = vaccinationDate.getFullYear();
		var record = findInList(vaccinationTypeListJson,'vaccination_type_id',vacTypeId);
		var freqInMonth = record != null ? record.frequency_in_months : '';

		var nextDueMonth = (vacMonth+1) + (freqInMonth);
		if (nextDueMonth > 12) {
			var quotient = nextDueMonth/12;
			var remainder = nextDueMonth%12;
			vacYear = vacYear+quotient;
			nextDueMonth = remainder;
		}

		vaccinationDate.setMonth(nextDueMonth-1);
		vaccinationDate.setYear(vacYear);
		var vacDateObj = formatDate(vaccinationDate, 'ddmmyyyy', '-');
		document.getElementById('dialog_due_date').value = vacDateObj;
	}

	function checkFrequencyExists() {
		var vacTypeId = document.getElementById('dialog_vaccination_type').value;
		var record = null;
		if (empty(vacTypeId)) {
			showMessage("js.clinicaldata.vaccinations.vaccination.nextdue");
			document.getElementById('dialog_vaccination_type').focus();
			return false;
		} else {
			record = findInList(vaccinationTypeListJson,'vaccination_type_id',vacTypeId);
		}

		if (!empty(record) && !empty(record.frequency_in_months)) {
			document.getElementById('nextDueDate').style.display = "";
		} else {
			document.getElementById('nextDueDate').style.display = "none";
		}
	}
