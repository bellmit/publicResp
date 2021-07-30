	function init() {
		initHospitalizationDialog();
	}

	var HospitalizationDialog;
	function initHospitalizationDialog() {
		var HospitalizationDialogDiv = document.getElementById("hospitalizationDialog");
		HospitalizationDialogDiv.style.display = 'block';
		HospitalizationDialog = new YAHOO.widget.Dialog("hospitalizationDialog",{
				width:"300px",
				context :["", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:closeHospitalizationDialog,
		                                                scope:HospitalizationDialog,
		                                                correctScope:true } );
		HospitalizationDialog.cfg.queueProperty("keylisteners", escKeyListener);
		HospitalizationDialog.render();
	}

	function closeHospitalizationDialog() {
		HospitalizationDialog.hide();
		clearDialog();
	}

	function showAddEditDialog(obj) {
		button = document.getElementById("btnAddItem");
		HospitalizationDialog.cfg.setProperty("context",[button, "tr", "br"], false);
		HospitalizationDialog.show();
	}

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.HospitalizationInformationForm, name, index);
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
	HOSPITAL_COL=i++; ADMISSION_DATE_COL = i++; DISCHARGE_DATE_COL = i++; REASON_COL = i++;REMARKS_COL = i++;
    DELETE_COL=i++; EDIT_COL=i++;

	function addRecord() {
		var dialogId = document.HospitalizationInformationForm.dialogId.value;
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
		var hospitalName = document.getElementById('dialog_hospital_name').value;
		var admissionDate = document.getElementById('dialog_admission_date').value;
		var dischargeDate = document.getElementById('dialog_discharge_date').value;
		var reasonId = document.getElementById('dialog_reason').value;
		var record = findInList(reasonListJson,'reason_id',reasonId);
		var remarks = document.getElementById('dialog_remarks').value;

		if (!doValidateDateField(document.getElementById('dialog_admission_date'), "dd-MM-yyyy")) {
			return false;
		}

		if (!doValidateDateField(document.getElementById('dialog_discharge_date'), "dd-MM-yyyy")) {
			return false;
		}

		if (empty(hospitalName)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.hospital.required");
			document.getElementById('dialog_hospital_name').focus();
			return false;
		}
		if (empty(admissionDate)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.admissiondate.required");
			document.getElementById('dialog_admission_date').focus();
			return false;
		}
		if (empty(reasonId)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.reason.required");
			document.getElementById('dialog_reason').focus();
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

		var hospitalName = document.getElementById('dialog_hospital_name').value;
		var admissionDate = document.getElementById('dialog_admission_date').value;
		var dischargeDate = document.getElementById('dialog_discharge_date').value;
		var reasonId = document.getElementById('dialog_reason').value;
		var record = findInList(reasonListJson,'reason_id',reasonId);
		var remarks = document.getElementById('dialog_remarks').value;

		var appendRemarks = null;
		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[HOSPITAL_COL], hospitalName);
		setNodeText(row.cells[ADMISSION_DATE_COL], admissionDate);
		setNodeText(row.cells[DISCHARGE_DATE_COL], dischargeDate);
		setNodeText(row.cells[REASON_COL], !empty(record) ? record.reason : '');
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		setHiddenValue(id, "hospital_name", hospitalName);
		setHiddenValue(id, "admission_date", admissionDate);
		setHiddenValue(id, "discharge_date", dischargeDate);
		setHiddenValue(id, "reason", !empty(record) ? record.reason : '');
		setHiddenValue(id, "reason_id", reasonId);
		setHiddenValue(id, "remarks", remarks);

		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.clinicaldata.hospitalizationinformation.edithospitalizationdetails"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditDialogBox(this)");
		editImg.setAttribute("class", "button");

		var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.clinicaldata.hospitalizationinformation.cancelhospitalizationdetails"));
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
		var hospitalName = document.getElementById('dialog_hospital_name').value;
		var admissionDate = document.getElementById('dialog_admission_date').value;
		var dischargeDate = document.getElementById('dialog_discharge_date').value;
		var reasonId = document.getElementById('dialog_reason').value;
		var record = findInList(reasonListJson,'reason_id',reasonId);
		var remarks = document.getElementById('dialog_remarks').value;

		var appendRemarks = null;
		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[HOSPITAL_COL], hospitalName);
		setNodeText(row.cells[ADMISSION_DATE_COL], admissionDate);
		setNodeText(row.cells[DISCHARGE_DATE_COL], dischargeDate);
		setNodeText(row.cells[REASON_COL], !empty(record) ? record.reason : '');
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		getElementByName(row,"hospital_name").value = hospitalName;
		getElementByName(row,"admission_date").value = admissionDate;
		getElementByName(row,"discharge_date").value = dischargeDate;
		getElementByName(row,"reason").value = !empty(record) ? record.reason : '';
		getElementByName(row,"reason_id").value = reasonId;
		getElementByName(row,"remarks").value = remarks;

		clearDialog();
		HospitalizationDialog.cancel();;
	}

	function clearDialog() {
		document.getElementById('dialog_hospital_name').value = '';
		document.getElementById('dialog_admission_date').value = '';
		document.getElementById('dialog_discharge_date').value = '';
		document.getElementById('dialog_reason').value = '';
		document.getElementById('dialog_remarks').value = '';
		document.HospitalizationInformationForm.dialogId.value = '';
	}

	function openDialogBox() {
		document.getElementById('dialog_hospital_name').value = '';
		document.getElementById('dialog_admission_date').value = '';
		document.getElementById('dialog_discharge_date').value = '';
		document.getElementById('dialog_reason').value = '';
		document.getElementById('dialog_remarks').value = '';
		button = document.getElementById("btnAddItem");
		HospitalizationDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.hospitalizationinformation.addhospitalizationdetails');
		document.HospitalizationInformationForm.dialog_hospital_name.focus();
		HospitalizationDialog.show();
	}

	function cancelDialog() {
		HospitalizationDialog.cancel();
		clearDialog();
	}

	function openEditDialogBox(obj) {
		rowObj = findAncestor(obj,"TR");
		rowObj.className = 'selectedRow';
		updateGridToDialog(rowObj);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.hospitalizationinformation.edithospitalizationdetails');
		HospitalizationDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		document.HospitalizationInformationForm.dialog_hospital_name.focus();
	}

	function updateGridToDialog(rowObj){
		document.HospitalizationInformationForm.dialog_hospital_name.value 	=  getElementByName(rowObj,"hospital_name").value;
		document.HospitalizationInformationForm.dialog_admission_date.value 	=  getElementByName(rowObj,"admission_date").value;
		document.HospitalizationInformationForm.dialog_discharge_date.value 	=  getElementByName(rowObj,"discharge_date").value;
		document.HospitalizationInformationForm.dialog_reason.value 	=  getElementByName(rowObj,"reason_id").value;
		document.HospitalizationInformationForm.dialog_remarks.value 	=  getElementByName(rowObj,"remarks").value;
		document.HospitalizationInformationForm.dialogId.value = getRowItemIndex(rowObj);
		HospitalizationDialog.show();
	}

	function deleteItem(imgObj) {
		var rowObj = getThisRow(imgObj);
		var deletedInput = getElementByName(rowObj,"hdeleted").value;
		var trashImgObj = imgObj;
		var editImgObj = getElementByName(rowObj,"editIcon");

		if (deletedInput == 'false') {
			var ok = confirm(getString("js.clinicaldata.common.wantto.deleteentry"));
			if (!ok)
				return false;

			getElementByName(rowObj,"hdeleted").value = 'true';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete1.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.hospitalizationinformation.enablecancelhospitalization'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
		} else {
			getElementByName(rowObj,"hdeleted").value = 'false';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.hospitalizationinformation.cancelhospitalizationdetails'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.clinicaldata.hospitalizationinformation.edithospitalizationdetails"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
		}
	}

	function validateForm() {
		var dataAsOfDate = document.getElementById('data_as_of_date');
		var numRows = getNumItems();
		var allChecked = false;

		if (empty(dataAsOfDate.value)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.date.asofdate");
			document.getElementById('data_as_of_date').focus();
			return false;
		}

	/*	if (numRows <= 0) {
			alert("there is no records to save in the grid"+"\n"+"pls add one or more records.");
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
	    	alert("all row(s) in the grid are deleted \n so no record(s) to save");
 		    return false;
	    }*/

		return true;
	}

	function checkLength(obj,len,field){
		if( obj.value.length  > len ){
			var msg=getString("js.clinicaldata.hospitalizationinformation.addoredit.max");
			msg+=len;
			msg+=getString("js.clinicaldata.hospitalizationinformation.addoredit.chars.allowed");
			msg+=field;
			alert(msg);
			obj.focus();
			return false;
		}
		return true;
	}
