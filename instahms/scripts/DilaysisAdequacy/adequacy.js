	function init() {
		initAdequacyDialog();
	}

	var adequacyDialog;
	function initAdequacyDialog() {
		var adequacyDialogDiv = document.getElementById("adequacyDialog");
		adequacyDialogDiv.style.display = 'block';
		adequacyDialog = new YAHOO.widget.Dialog("adequacyDialog",{
				width:"300px",
				context :["", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
		                                              { fn:closeAdequacyDialog,
		                                                scope:adequacyDialog,
		                                                correctScope:true } );
		adequacyDialog.cfg.queueProperty("keylisteners", escKeyListener);
		adequacyDialog.render();
	}

	function closeAdequacyDialog() {
		adequacyDialog.hide();
		clearDialog();
	}

	function showAddEditDialog(obj) {
		button = document.getElementById("btnAddItem");
		adequacyDialog.cfg.setProperty("context",[button, "tr", "br"], false);
		adequacyDialog.show();
	}

	function setHiddenValue(index, name, value) {
		var el = getIndexedFormElement(document.adequacyForm, name, index);
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
	ADEQUACY_DATE_COL=i++; ADEQUACY_URR_COL= i++; ADEQUACY_KTV_COL = i++;
    DELETE_COL=i++; EDIT_COL=i++;

	function addRecord() {
		var dialogId = document.adequacyForm.dialogId.value;
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
		var adequacyDate = document.getElementById('dialog_values_as_of_date').value;
		var diaUrr = document.getElementById('dialog_urr').value
		var dailKtv = document.getElementById('dialog_ktv').value;
		var numRows = getNumItems();

		if (empty(adequacyDate)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.observationdate.required");
			document.getElementById('dialog_values_as_of_date').focus();
			return false;
		}
		if (empty(diaUrr)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.urr.required");
			document.getElementById('dialog_urr').focus();
			return false;
		}
		if (empty(dailKtv)) {
			showMessage("js.clinicaldata.hospitalizationinformation.addoredit.kt.v.required");
			document.getElementById('dialog_ktv').focus();
			return false;
		}

		var selectedDate =  parseDateStr(document.getElementById('dialog_values_as_of_date').value);
		var dilaogId = document.adequacyForm.dialogId.value;
		if (empty(dilaogId)) {
		    for(var i=0;i<numRows;i++) {
		    	var rowObj = getItemRow(i);
		    	var adequacyDate =  parseDateStr(getElementByName(rowObj,"values_as_of_date").value);
		    	if (adequacyDate-selectedDate == 0) {
		    		showMessage("adequacy date can't be duplicate.");
		    		document.getElementById('dialog_values_as_of_date').focus();
		    		document.getElementById('dialog_values_as_of_date').value = '';
		    		return false;
		    	}
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

		var adequacyDate = document.getElementById('dialog_values_as_of_date').value;
		var dialUrr = document.getElementById('dialog_urr').value
		var dialKtv = document.getElementById('dialog_ktv').value;

		setNodeText(row.cells[ADEQUACY_DATE_COL], adequacyDate);
		setNodeText(row.cells[ADEQUACY_URR_COL], dialUrr);
		setNodeText(row.cells[ADEQUACY_KTV_COL], dialKtv);
		setHiddenValue(id, "values_as_of_date", adequacyDate);
		setHiddenValue(id, "urr", dialUrr);
		setHiddenValue(id, "ktv", dialKtv);

		var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title",getString("js.clinicaldata.dialysisadequacy.editdialysis"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("onclick","openEditDialogBox(this)");
		editImg.setAttribute("class", "button");

		var deleteImg = document.createElement("img");
		deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
		deleteImg.setAttribute("title", getString("js.clinicaldata.dialysisadequacy.canceldialysis"));
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
		var adequacyDate = document.getElementById('dialog_values_as_of_date').value;
		var dialUrr = document.getElementById('dialog_urr').value
		var dialKtv = document.getElementById('dialog_ktv').value;

		setNodeText(row.cells[ADEQUACY_DATE_COL], adequacyDate);
		setNodeText(row.cells[ADEQUACY_URR_COL], dialUrr);
		setNodeText(row.cells[ADEQUACY_KTV_COL], dialKtv);

		getElementByName(row,"values_as_of_date").value = adequacyDate;
		getElementByName(row,"urr").value = dialUrr;
		getElementByName(row,"ktv").value = dialKtv;

		clearDialog();
		adequacyDialog.cancel();;
	}

	function clearDialog() {
		document.getElementById('dialog_values_as_of_date').value = '';
		document.getElementById('dialog_urr').value = '';
		document.getElementById('dialog_ktv').value = '';
		document.adequacyForm.dialogId.value = '';
	}

	function openDialogBox() {
		document.getElementById('dialog_values_as_of_date').value = '';
		document.getElementById('dialog_urr').value = '';
		document.getElementById('dialog_ktv').value = '';
		document.getElementById('dialog_values_as_of_date').disabled = false;
		button = document.getElementById("btnAddItem");
		adequacyDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.dialysisadequacy.adddialysis');
		document.adequacyForm.dialog_values_as_of_date.focus();
		adequacyDialog.show();
	}

	function cancelDialog() {
		adequacyDialog.cancel();
		clearDialog();
	}

	function openEditDialogBox(obj) {
		rowObj = findAncestor(obj,"TR");
		rowObj.className = 'selectedRow';
		updateGridToDialog(rowObj);
		if (disableAdequacyDate() == false) {
			document.getElementById('dialog_values_as_of_date').disabled = true;
		} else {
			document.getElementById('dialog_values_as_of_date').disabled = false;
		}
		document.getElementById('itemdialogheader').innerHTML = getString('js.clinicaldata.dialysisadequacy.editdialysis');
		adequacyDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
		document.adequacyForm.dialog_values_as_of_date.focus();
	}

	function disableAdequacyDate() {
		var adequacyDate = document.getElementById('dialog_values_as_of_date').value;
		var mrNo = document.getElementById('mr_no').value;
		var ajaxReqObject = newXMLHttpRequest();
			var url ="./DialysisAdequacy.do?_method=getDupliacteAdequacyDetails&values_as_of_date="+adequacyDate ;
			url = url + "&mr_no="+mrNo;
			var reqObject = newXMLHttpRequest();
			reqObject.open("POST",url.toString(), false);
			reqObject.send(null);
			if (reqObject.readyState == 4) {
				if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
					if(reqObject.responseText == 'true')
					return false;
				}
			}
		return true;
	}

	function updateGridToDialog(rowObj){
		document.adequacyForm.dialog_values_as_of_date.value 	=  getElementByName(rowObj,"values_as_of_date").value;
		document.adequacyForm.dialog_urr.value 	=  getElementByName(rowObj,"urr").value;
		document.adequacyForm.dialog_ktv.value 	=  getElementByName(rowObj,"ktv").value;
		document.adequacyForm.dialogId.value = getRowItemIndex(rowObj);
		adequacyDialog.show();
	}

	function deleteItem(imgObj) {
		var rowObj = getThisRow(imgObj);
		var deletedInput = getElementByName(rowObj,"hdeleted").value;
		var trashImgObj = imgObj;
		var editImgObj = getElementByName(rowObj,"editIcon");

		if (deletedInput == 'false') {
			getElementByName(rowObj,"hdeleted").value = 'true';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete1.png");
			trashImgObj.setAttribute('title',getString('js.clinicaldata.dialysisadequacy.enabledeleteddialysis'));
			editImgObj.setAttribute('src',cpath+'/icons/Edit1.png');
			editImgObj.setAttribute('onclick','');
			editImgObj.setAttribute('title','');
			editImgObj.setAttribute('class','');
		} else {
			getElementByName(rowObj,"hdeleted").value = 'false';
			trashImgObj.setAttribute('src',cpath+"/icons/Delete.png");
			editImgObj.setAttribute('src',cpath+'/icons/Edit.png');
			editImgObj.setAttribute('title',getString("js.clinicaldata.dialysisadequacy.editdialysis"));
			editImgObj.setAttribute('onclick',"openEditDialogBox(this)");
			editImgObj.setAttribute('class','button');
		}
	}

	function validateForm() {
		var allChecked = false;
		var numRows = getNumItems();

		if (numRows <= 0) {
			var msg=getString("js.clinicaldata.hospitalizationinformation.addoredit.norecordsingrid");
			msg+=\n;
			msg+=getString("js.clinicaldata.hospitalizationinformation.addoredit.addmorerecord");

			alert(msg);
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
	    	showMessage("js.clinicaldata.hospitalizationinformation.addoredit.allrowstosave");
 		    return false;
	    }
		return true;
	}

	function checkLength(obj,len,field){
		if( obj.value.length  > len ){
			var msg=getString("js.clinicaldata.hospitalizationinformation.addoredit.max");
			msg+=len;
			msg+=getString("js.clinicaldata.hospitalizationinformation.addoredit.charsallowed");
			msg+=field;
			alert(msg);
			obj.focus();
			return false;
		}
		return true;
	}
