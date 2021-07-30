	function init() {
		initAntibioticDialog();
		antibioticsAutoComplete();
	}

	var itemAutoComp = null;

function antibioticsAutoComplete() {
	var ds = new YAHOO.util.XHRDataSource(cpath + '/clinical/Infections.do');
	ds.scriptQueryAppend = "_method=findItems";
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "item_name"},
					{key : "item_id"},
					{key : "route_of_admin"},
					{key : 'item_form_id'},
					{key : 'item_strength'}
				 ],
		numMatchFields: 2
	};

	itemAutoComp = new YAHOO.widget.AutoComplete("dialog_medicine_name", "medicineContainer", ds);
	itemAutoComp.minQueryLength = 1;
	itemAutoComp.animVert = false;
	itemAutoComp.maxResultsDisplayed = 50;
	itemAutoComp.resultTypeList = false;
	itemAutoComp.forceSelection = true;

	itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;

	itemAutoComp.itemSelectEvent.subscribe(selectItem);
	return itemAutoComp;
}


function selectItem(sType, oArgs) {
	var record = oArgs[2];
	if (!empty(record)) {
		if (!empty(record.item_name))
			document.getElementById('dialog_medicine_name').value = record.item_name;

		if (!empty(record.item_id))
			document.getElementById('dialog_op_medicine_pres_id').value = record.item_id;

		if (!empty(record.item_form_name)) {
				document.getElementById('dialog_form').value = record.item_form_name;
				document.getElementById('dialog_form').disabled = true;
		} else {
			document.getElementById('dialog_form').value = '';
			document.getElementById('dialog_form').disabled = false;
		}

		if (!empty(record.item_strength)) {
			document.getElementById('dialog_strength').value = record.item_strength;
			document.getElementById('dialog_strength').disabled = true;
		} else {
			document.getElementById('dialog_strength').value = '';
			document.getElementById('dialog_strength').disabled = false;
		}
	}
}

	function closevaccinationDialog() {
		vaccinationDialog.hide();
		clearDialog();
	}


	var antibioticDialog;
	function initAntibioticDialog() {
		var antibioticDialogDiv = document.getElementById("antibioticDialog");
		antibioticDialogDiv.style.display = 'block';
		antibioticDialog = new YAHOO.widget.Dialog("antibioticDialog",{
				width:"600px",
				context :["", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:closeantibioticDialog,
		                                                scope:antibioticDialog,
		                                                correctScope:true } );
		antibioticDialog.cfg.queueProperty("keylisteners", escKeyListener);
		antibioticDialog.render();
	}

	function closeantibioticDialog() {
		antibioticDialog.hide();
		clearDialog();
	}

	function showAddEditDialog(obj) {
		button = document.getElementById("btnAddItem");
		antibioticDialog.cfg.setProperty("context",[button, "tr", "br"], false);
		antibioticDialog.show();
	}

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.AntibioticForm, name, index);
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
	ANTIBIOTIC_COL=i++; FORM_COL = i++; STRENGTH_COL = i++; DOSAGE_COL = i++;
	DURATION_COL=i++;FREQUENCY_COL=i++;REMARKS_COL=i++;
    DELETE_COL=i++; EDIT_COL=i++;

	function addRecord() {
		var dialogId = document.AntibioticForm.dialogId.value;
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
		var antibiotic = document.getElementById('dialog_medicine_name').value;
		/*var form = document.getElementById('dialog_form').value;
		var strength = document.getElementById('dialog_strength').value;
		var dosage = document.getElementById('dialog_dosage').value;
		var testDosage = document.getElementById('dialog_test_dosage').value;
		var duration = document.getElementById('dialog_duration').value;
		var frequency = document.getElementById('dialog_frequency').value;
		var remarks = document.getElementById('dialog_remarks').value;*/

		if (empty(antibiotic)) {
			showMessage("js.clinicaldata.infectionsdata.antibiotic.required");
			document.getElementById('dialog_op_medicine_pres_id').focus();
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

		var antibiotic = document.getElementById('dialog_medicine_name').value;
		var antibioticId = document.getElementById('dialog_op_medicine_pres_id').value;
		var form = document.getElementById('dialog_form').value;
		var strength = document.getElementById('dialog_strength').value;
		var dosage = document.getElementById('dialog_dosage').value;
		var testDosage = document.getElementById('dialog_test_dosage').value;
		var duration = document.getElementById('dialog_duration').value;
		var frequency = document.getElementById('dialog_frequency').value;
		var remarks = document.getElementById('dialog_remarks').value;
		var appendRemarks = null;

		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[ANTIBIOTIC_COL], antibiotic);
		setNodeText(row.cells[FORM_COL], form);
		setNodeText(row.cells[STRENGTH_COL], strength);
		setNodeText(row.cells[DOSAGE_COL], dosage);
		setNodeText(row.cells[DURATION_COL], duration);
		setNodeText(row.cells[FREQUENCY_COL], frequency);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		setHiddenValue(id, "medicine_name", antibiotic);
		setHiddenValue(id, "op_medicine_pres_id", antibioticId);
		setHiddenValue(id, "infection_form_name", form);
		setHiddenValue(id, "strength", strength);
		setHiddenValue(id, "dosage", dosage);
		setHiddenValue(id, "test_dosage", testDosage);
		setHiddenValue(id, "duration", duration);
		setHiddenValue(id, "frequency", frequency);
		setHiddenValue(id, "remarks", remarks);

		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.clinicaldata.infectionsdata.edit.antibiotic"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditDialogBox(this)");
		editImg.setAttribute("class", "button");

		var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.clinicaldata.infectionsdata.cancel.antibiotic"));
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
		var antibiotic = document.getElementById('dialog_medicine_name').value;
		var antibioticId = document.getElementById('dialog_op_medicine_pres_id').value;
		var form = document.getElementById('dialog_form').value;
		var strength = document.getElementById('dialog_strength').value;
		var dosage = document.getElementById('dialog_dosage').value;
		var testDosage = document.getElementById('dialog_test_dosage').value;
		var duration = document.getElementById('dialog_duration').value;
		var frequency = document.getElementById('dialog_frequency').value;
		var remarks = document.getElementById('dialog_remarks').value;
		var appendRemarks = null;

		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[ANTIBIOTIC_COL], antibiotic);
		setNodeText(row.cells[FORM_COL], form);
		setNodeText(row.cells[STRENGTH_COL], strength);
		setNodeText(row.cells[DOSAGE_COL], dosage);
		setNodeText(row.cells[DURATION_COL], duration);
		setNodeText(row.cells[FREQUENCY_COL], frequency);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		getElementByName(row,"medicine_name").value = antibiotic;
		getElementByName(row,"op_medicine_pres_id").value = antibioticId;
		getElementByName(row,"infection_form_name").value = form;
		getElementByName(row,"strength").value = strength;
		getElementByName(row,"dosage").value = dosage;
		getElementByName(row,"test_dosage").value = testDosage;
		getElementByName(row,"duration").value = duration;
		getElementByName(row,"frequency").value = frequency;
		getElementByName(row,"remarks").value = remarks;

		clearDialog();
		antibioticDialog.cancel();;
	}

	function clearDialog() {
		document.getElementById('dialog_medicine_name').value = '';
		document.getElementById('dialog_op_medicine_pres_id').value = '';
		document.getElementById('dialog_form').value = '';
		document.getElementById('dialog_frequency').value = '';
		document.getElementById('dialog_strength').value = '';
		document.getElementById('dialog_dosage').value = '';
		document.getElementById('dialog_test_dosage').value = '';
		document.getElementById('dialog_duration').value = '';
		document.getElementById('dialog_frequency').value = '';
		document.getElementById('dialog_remarks').value = '';
		document.AntibioticForm.dialogId.value = '';
	}

	function openDialogBox() {
		document.getElementById('dialog_medicine_name').value = '';
		document.getElementById('dialog_op_medicine_pres_id').value = '';
		document.getElementById('dialog_form').value = '';
		document.getElementById('dialog_frequency').value = '';
		document.getElementById('dialog_strength').value = '';
		document.getElementById('dialog_dosage').value = '';
		document.getElementById('dialog_test_dosage').value = '';
		document.getElementById('dialog_duration').value = '';
		document.getElementById('dialog_remarks').value = '';
		button = document.getElementById("btnAddItem");
		antibioticDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.infectionsdata.add.antibiotic');
		document.AntibioticForm.dialog_medicine_name.focus();
		antibioticDialog.show();
	}

	function cancelDialog() {
		antibioticDialog.cancel();
		clearDialog();
	}

	function openEditDialogBox(obj) {
		rowObj = findAncestor(obj,"TR");
		rowObj.className = 'selectedRow';
		updateGridToDialog(rowObj);
		if (itemAutoComp._elTextbox.value != '') {
			itemAutoComp._bItemSelected = true;
			itemAutoComp._sInitInputValue = itemAutoComp._elTextbox.value;
			document.AntibioticForm.dialog_op_medicine_pres_id.value  = getElementByName(rowObj,"op_medicine_pres_id").value;
			document.AntibioticForm.dialog_medicine_name.value  = getElementByName(rowObj,"medicine_name").value;
			document.getElementById('dialog_op_medicine_pres_id').value = getElementByName(rowObj,"op_medicine_pres_id").value;
		}

		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.infectionsdata.edit.antibiotic');
		antibioticDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		document.AntibioticForm.dialog_medicine_name.focus();
	}

	function updateGridToDialog(rowObj){
		document.AntibioticForm.dialog_medicine_name.value = getElementByName(rowObj,"medicine_name").value;
		document.AntibioticForm.dialog_op_medicine_pres_id.value 	=  getElementByName(rowObj,"op_medicine_pres_id").value;
		document.AntibioticForm.dialog_form.value 	=  getElementByName(rowObj,"infection_form_name").value;
		document.AntibioticForm.dialog_strength.value 	=  getElementByName(rowObj,"strength").value;
		document.AntibioticForm.dialog_dosage.value 	=  getElementByName(rowObj,"dosage").value;
		document.AntibioticForm.dialog_test_dosage.value 	=  getElementByName(rowObj,"test_dosage").value;
		document.AntibioticForm.dialog_duration.value 	=  getElementByName(rowObj,"duration").value;
		document.AntibioticForm.dialog_frequency.value 	=  getElementByName(rowObj,"frequency").value;
		document.AntibioticForm.dialog_remarks.value 	=  getElementByName(rowObj,"remarks").value;
		document.AntibioticForm.dialogId.value = getRowItemIndex(rowObj);
		antibioticDialog.show();
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
			trashImgObj.setAttribute('title',getString('js.clinicaldata.infectionsdata.enabledelete.antibiotic'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
		} else {
			getElementByName(rowObj,"hdeleted").value = 'false';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.infectionsdata.delete.antibiotic'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.clinicaldata.infectionsdata.edit.antibiotic"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
		}
	}

	function checkLength(obj,len,field){
		if( obj.value.length  > len ){
			var msg=getString("js.clinicaldata.infectionsdata.max");
			msg+=len;
			msg+=getString("js.clinicaldata.infectionsdata.charsallowed");
			msg+=field;
			alert(msg);
			obj.focus();
			return false;
		}
		return true;
	}
