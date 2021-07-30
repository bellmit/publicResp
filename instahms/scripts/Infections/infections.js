	function init() {
		initInfectionDialog();
	}
	var edited = false;

	function openAddAntibioticsTab(infectionId,index,infectionTypeId,infectionType) {
		var mrNo = document.getElementById('mr_no').value;
		window.location.href = cpath+"/clinical/Infections.do?_method=addOrEditAntibiotics&infection_id="+infectionId+"&infection_type_id="+infectionTypeId+"&mr_no="+mrNo+"&infection_type="+infectionType;
	}

	var infectionDialog;
	function initInfectionDialog() {
		var infectionDialogDiv = document.getElementById("infectionDialog");
		infectionDialogDiv.style.display = 'block';
		infectionDialog = new YAHOO.widget.Dialog("infectionDialog",{
				width:"350px",
				context :["", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:closeinfectionDialog,
		                                                scope:infectionDialog,
		                                                correctScope:true } );
		infectionDialog.cfg.queueProperty("keylisteners", escKeyListener);
		infectionDialog.render();
	}

	function closeinfectionDialog() {
		infectionDialog.hide();
		clearDialog();
	}

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.InfectionForm, name, index);
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
	INFECTION_TYPE_COL=i++; EFFECTIVE_DATE_COL = i++; INEFCTION_SITE_COL = i++; INFECTION_ORGANISAM_COL = i++;
	INFECTION_STATUS_COL=i++;ANTI_MICROBIAL_SUSCEPTIBILITY_COL=i++;ANTIBIOTICS_COL=i++;REMARKS_COL=i++;
    DELETE_COL=i++; EDIT_COL=i++;

	function addRecord() {
		var dialogId = document.InfectionForm.dialogId.value;
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
		var infectionType = document.getElementById('dialog_infection_type').value;
		var infectionTypeRecord = findInList(infectionTypeListJson,'infection_type_id',infectionType);
		var effectiveDate = document.getElementById('dialog_effective_date').value;
		var site = document.getElementById('dialog_infection_site').value;
		var infectingOrgan = document.getElementById('dialog_infecting_organism').value;
		var infectionStatus = document.getElementById('dialog_infection_status').value;
		var microbalSuspect = document.getElementById('dialog_microbial_susceptibility').value;
		var antibiotics = "";
		var remarks = document.getElementById('dialog_remarks').value;

		if (!doValidateDateField(document.getElementById('dialog_effective_date'), "dd-MM-yyyy")) {
			return false;
		}
		if (empty(infectionType)) {
			showMessage("js.clinicaldata.infectionsdata.infection.required");
			document.getElementById('dialog_infection_type').focus();
			return false;
		}
		if (empty(infectionStatus)) {
			showMessage("js.clinicaldata.infectionsdata.infection.status.required");
			document.getElementById('dialog_infection_status').focus();
			return false;
		}
		if (empty(effectiveDate)) {
			showMessage("js.clinicaldata.infectionsdata.effectivedate.required");
			document.getElementById('dialog_effective_date').focus();
			return false;
		}

		if (empty(site)) {
			showMessage("js.clinicaldata.infectionsdata.infection.site.required");
			document.getElementById('dialog_infection_site').focus();
			return false;
		}

		if (empty(infectingOrgan)) {
			showMessage("js.clinicaldata.infectionsdata.infecting.organism.required");
			document.getElementById('dialog_infecting_organism').focus();
			return false;
		}

		if (empty(microbalSuspect)) {
			showMessage("js.clinicaldata.infectionsdata.antimicrobial.required");
			document.getElementById('dialog_microbial_susceptibility').focus();
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

		var infectionType = document.getElementById('dialog_infection_type').value;
		var infectionTypeRecord = findInList(infectionTypeListJson,'infection_type_id',infectionType);
		var effectiveDate = document.getElementById('dialog_effective_date').value;
		var site = document.getElementById('dialog_infection_site').value;
		var infectionSites = null;
		if (!empty(site))
			 infectionSites = findInList(infectionsSitesJson,'infection_site_id',site);
		var infectingOrgan = document.getElementById('dialog_infecting_organism').value;
		var infectionStatus = document.getElementById('dialog_infection_status').value;
		var microbalSuspect = document.getElementById('dialog_microbial_susceptibility').value;
		var remarks = document.getElementById('dialog_remarks').value;
		var mrNo = document.InfectionForm.mr_no.value;
		var appendRemarks = null;
		infectStatus = "";

		if (infectionStatus == 'Y') {
			infectStatus = "Yes";
		} else if (infectionStatus == 'N') {
			infectStatus = "No";
		}

		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[INFECTION_TYPE_COL], !empty(infectionTypeRecord) ? infectionTypeRecord.infection_type : '');
		setNodeText(row.cells[INFECTION_STATUS_COL], !empty(infectionStatus) ? infectStatus : '');
		setNodeText(row.cells[EFFECTIVE_DATE_COL], effectiveDate);
		setNodeText(row.cells[INEFCTION_SITE_COL], !empty(infectionSites) ? infectionSites.infection_site_name : '');
		setNodeText(row.cells[INFECTION_ORGANISAM_COL], infectingOrgan);
		setNodeText(row.cells[ANTI_MICROBIAL_SUSCEPTIBILITY_COL], microbalSuspect);
		var rowId = parseInt(id)+1;
		/*var addLink = document.getElementById('addedit'+rowId);
		addLink.setAttribute("href", "javascript:void(0)");
		addLink.setAttribute("id", "addedit"+rowId);
		addLink.setAttribute("title", "Add Antibiotics");
		addLink.setAttribute("onclick", "openAddAntibioticsTab('','"+rowId+"','','')");
		row.cells[ANTIBIOTICS_COL].appendChild(addLink);*/
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);
		setHiddenValue(id, "infection_type_id", infectionType);
		setHiddenValue(id, "infection_type", !empty(infectionTypeRecord) ? infectionTypeRecord.infection_type : '');
		setHiddenValue(id, "infection_status", infectionStatus);
		setHiddenValue(id, "infection_effective_date", effectiveDate);
		setHiddenValue(id, "infection_site_id", !empty(infectionSites) ? infectionSites.infection_site_id : '');
		setHiddenValue(id, "infecting_organism", infectingOrgan);
		setHiddenValue(id, "anti_microbial_susceptibility", microbalSuspect);
		setHiddenValue(id, "remarks", remarks);

		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.clinicaldata.infectionsdata.add.edit.infection"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditDialogBox(this)");
		editImg.setAttribute("class", "button");

		var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.clinicaldata.infectionsdata.cancel.infection"));
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
		edited = true;
	}

	function updateRecordToGrid(row) {
		var infectionId = getElementByName(row,"infection_id").value;
		var hiddenInfectionType = getElementByName(row,"infection_type").value;
		var infectionType = document.getElementById('dialog_infection_type').value;
		var infectionTypeRecord = findInList(infectionTypeListJson,'infection_type_id',infectionType);
		var effectiveDate = document.getElementById('dialog_effective_date').value;
		var site = document.getElementById('dialog_infection_site').value;
		var infectionSites = null;
		if (!empty(site))
			 infectionSites = findInList(infectionsSitesJson,'infection_site_id',site);
		var infectingOrgan = document.getElementById('dialog_infecting_organism').value;
		var infectionStatus = document.getElementById('dialog_infection_status').value;
		var microbalSuspect = document.getElementById('dialog_microbial_susceptibility').value;
		var remarks = document.getElementById('dialog_remarks').value;
		var appendRemarks = null;
		var mrNo = document.InfectionForm.mr_no.value;
		infectStatus = "";

		if (infectionStatus == 'Y') {
			infectStatus = "Yes";
		} else if (infectionStatus == 'N') {
			infectStatus = "No";
		}

		if (remarks.length >= 30) {
			appendRemarks = remarks.substring(0,30) + "...";
		}

		setNodeText(row.cells[INFECTION_TYPE_COL], !empty(infectionTypeRecord) ? infectionTypeRecord.infection_type : '');
		setNodeText(row.cells[INFECTION_STATUS_COL], !empty(infectionStatus) ? infectStatus : '');
		setNodeText(row.cells[EFFECTIVE_DATE_COL], effectiveDate);
		setNodeText(row.cells[INEFCTION_SITE_COL], !empty(infectionSites) ? infectionSites.infection_site_name : '');
		setNodeText(row.cells[INFECTION_ORGANISAM_COL], infectingOrgan);
		setNodeText(row.cells[ANTI_MICROBIAL_SUSCEPTIBILITY_COL], microbalSuspect);
		setNodeText(row.cells[REMARKS_COL], !empty(appendRemarks) ? appendRemarks : remarks);

		getElementByName(row,"infection_type_id").value = infectionType;
		getElementByName(row,"infection_type").value = hiddenInfectionType;
		getElementByName(row,"infection_status").value = infectionStatus;
		getElementByName(row,"infection_effective_date").value = effectiveDate;
		getElementByName(row,"infection_site_id").value = !empty(infectionSites) ? infectionSites.infection_site_id : '';
		getElementByName(row,"infecting_organism").value = infectingOrgan;
		getElementByName(row,"anti_microbial_susceptibility").value = microbalSuspect;
		getElementByName(row,"remarks").value = remarks;

		clearDialog();
		infectionDialog.cancel();
		edited = true;
	}

	function clearDialog() {
		document.getElementById('dialog_infection_type').value = '';
		document.getElementById('dialog_effective_date').value = '';
		document.getElementById('dialog_infection_site').value = '';
		document.getElementById('dialog_infecting_organism').value = '';
		document.getElementById('dialog_microbial_susceptibility').value = '';
		document.getElementById('dialog_remarks').value = '';
		document.InfectionForm.dialogId.value = '';
	}

	function openDialogBox() {
		document.getElementById('dialog_infection_type').value = '';
		document.getElementById('dialog_effective_date').value = '';
		document.getElementById('dialog_infection_site').value = '';
		document.getElementById('dialog_infecting_organism').value = '';
		document.getElementById('dialog_microbial_susceptibility').value = '';
		document.getElementById('dialog_remarks').value = '';
		button = document.getElementById("btnAddItem");
		infectionDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.infectionsdata.add.infection');
		document.InfectionForm.dialog_infection_type.focus();
		infectionDialog.show();
	}

	function cancelDialog() {
		infectionDialog.cancel();
		clearDialog();
	}

	function openEditDialogBox(obj) {
		rowObj = findAncestor(obj,"TR");
		rowObj.className = 'selectedRow';
		updateGridToDialog(rowObj);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.infectionsdata.edit.infection');
		infectionDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		document.InfectionForm.dialog_infection_type.focus();
	}

	function updateGridToDialog(rowObj){
		document.InfectionForm.dialog_infection_type.value 	=  getElementByName(rowObj,"infection_type_id").value;
		document.InfectionForm.dialog_effective_date.value 	=  getElementByName(rowObj,"infection_effective_date").value;
		document.InfectionForm.dialog_infection_site.value 	=  getElementByName(rowObj,"infection_site_id").value;
		document.InfectionForm.dialog_infecting_organism.value 	=  getElementByName(rowObj,"infecting_organism").value;
		document.InfectionForm.dialog_infection_status.value 	=  getElementByName(rowObj,"infection_status").value;
		document.InfectionForm.dialog_microbial_susceptibility.value 	=  getElementByName(rowObj,"anti_microbial_susceptibility").value;
		document.InfectionForm.dialog_remarks.value 	=  getElementByName(rowObj,"remarks").value;
		document.InfectionForm.dialogId.value = getRowItemIndex(rowObj);
		infectionDialog.show();
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
			trashImgObj.setAttribute('title',getString('js.clinicaldata.infectionsdata.enabledelete.infection'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
		} else {
			getElementByName(rowObj,"hdeleted").value = 'false';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.infectionsdata.delete.infection'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.clinicaldata.infectionsdata.edit.infection"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
		}
	}

	function validateForm() {
		var dataAsOfDate = document.getElementById('values_as_of_date');
		var numRows = getNumItems();
		var allChecked = false;

		if (empty(dataAsOfDate.value)) {
			showMessage("js.clinicaldata.infectionsdata.dateasofdate.required");
			document.getElementById('values_as_of_date').focus();
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