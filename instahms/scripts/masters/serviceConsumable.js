var backupName='';
var addedRegaents = [];

	function doClose() {
		window.location.href = cpath + "/master/ServiceConsumableMaster.do?_method=list&sortOrder=service_name" +
						"&sortReverse=false&status=A";
	}

	function keepBackUp() {
		if (document.serviceConsumableMasterForm._method.value == 'update') {
			testBackupName = document.serviceConsumableMasterForm.service_id.value;
			reagentBackName = document.serviceConsumableMasterForm.consumable_id.value;
		}
	}

	function checkduplicate() {
	  if(document.serviceConsumableMasterForm._method.value == 'update' && document.serviceConsumableMasterForm.service_name.value == ''){
	       alert("Please enter service name");
	       return false;
	  }
		if (document.serviceConsumableMasterForm._method.value == 'create' || document.serviceConsumableMasterForm._method.value == 'update') {
			if (document.getElementById("reagentstable").rows.length < 2) {
				alert("Add atleast one row ");
				return false;
			}
		}
		var reagentTableLen = document.getElementById("reagentstable").rows.length;
		for (var k=1; k<reagentTableLen; k++) {
			var consumableQtyObj = document.getElementById("qty"+k);
			var consumableQty = consumableQtyObj.value;
			if (empty(consumableQty)) consumableQtyObj.value = "0";
			if (!validateAmount(consumableQtyObj, "Quantity must be a valid amount"))
				return false;

			if (consumableQty == 0) {
				alert("Quantity can not be Zero");
				consumableQtyObj.focus();
				return false;
			}
		}

	return true;
   }//end of function

   function addRow(){
   }

   	function fillReagents() {
        if (method == 'show') {
	        document.getElementById("testDiv").style.display = 'none';
    	    document.getElementById("testLabel").style.display = 'block';
        } else {
		     document.getElementById("testDiv").style.display = 'block';
	         document.getElementById("testLabel").style.display = 'none';
	    	 return true;
		}
		for (var i =0; i<consumables.length; i++) {
			document.serviceConsumableMasterForm.service_id.value = service_id;

			if (consumables[i].status == "false") document.getElementById("status").checked = true;
			else if(consumables[i].status == "true")document.getElementById("status").checked = false;

			var reagentTable = document.getElementById("reagentstable");
			var len = reagentTable.rows.length;
			var row = reagentTable.insertRow(len);
			row.id = len;

		    var cell = document.createElement("TD");
		    cell.setAttribute("class", 'last');
			var inp = document.createElement("img");
			inp.setAttribute("class", "imgDelete");
			inp.setAttribute("src", cpath + "/icons/Delete.png");
			inp.setAttribute("name", "delItem");
			inp.setAttribute("id", "delItem" + len);
			inp.setAttribute("onclick", "deleteItem(this, "+len+")");
			cell.appendChild(inp);

			var inp0 = document.createElement("INPUT");
		    inp0.setAttribute("type", "hidden");
		    inp0.setAttribute("name", "hdeleted");
		    inp0.setAttribute("id", "hdeleted" + len);
		    inp0.setAttribute("value", "false");
		    cell.appendChild(inp0);

		    var cell1 = document.createElement("TD");
		    cell1.setAttribute("id", "consumable" + len);
			cell1.setAttribute("class", "label border");
			cell1.setAttribute("title", consumables[i].consumable_id);
//			cell1.setAttribute("style", "max-width: 15em");
			var text4 = document.createTextNode(consumables[i].item_name);
			cell1.appendChild(text4);

			var inp11 = document.createElement("INPUT");
		    inp11.setAttribute("type", "hidden");
		    inp11.setAttribute("name", "consumable");
		    inp11.setAttribute("id", "consumable" + len);
		    inp11.setAttribute("value", consumables[i].consumable_id);
		    cell1.appendChild(inp11);
		    addedRegaents.length = i;
		    addedRegaents[consumables[i].item_name] = consumables[i].item_name;

		    var cell2 = document.createElement("TD");
		    cell2.setAttribute("class", 'border');
			var inp21 = document.createElement("INPUT");
		    inp21.setAttribute("type", "text");
		    inp21.setAttribute("name", "qty");
		    inp21.setAttribute("size", "3");
		    inp21.setAttribute("id", "qty" + len);
		    inp21.setAttribute("value", consumables[i].quantity_needed);
		    cell2.appendChild(inp21);

			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell);
			// document.getElementById("reagentstable").insertRow(len);
		}
	}

	var i = 0;
	function addReagents(test,consumable,qty){
		i++;
		if (addedRegaents.length >= 0) {
			if (addedRegaents[consumable.options[consumable.selectedIndex].text] != undefined) {
	 			alert("Duplicate Entry");
	 			return false;
			}
		}
		if (method != 'show') {
			if (document.serviceConsumableMasterForm.service_id.value == '') {
				alert("Select service name");
				document.serviceConsumableMasterForm.service_id.focus();
				return false;
			}
		}

	 	if (document.serviceConsumableMasterForm.category_id.value == '') {
	 		alert("Select a category");
	 		document.serviceConsumableMasterForm.category_id.focus();
	 		return false;
	 	}
	  	if (document.serviceConsumableMasterForm.consumable_id.value == '') {
	 		alert("Select a Consumable");
	 		document.serviceConsumableMasterForm.consumable_id.focus();
	 		return false;
	 	}

	 	var qtyObj = document.serviceConsumableMasterForm.quantity_needed;
		var qty = qtyObj.value;
		if (empty(qty)) qtyObj.value = "0";
		if (!validateAmount(qtyObj, "Quantity must be a valid amount"))
			return false;

		if (qty == 0) {
			alert("Quantity needed can not be Zero");
			qtyObj.focus();
			return false;
		}

	 	var reagentTable = document.getElementById("reagentstable");
		var len = reagentTable.rows.length;
	   	var row = reagentTable.insertRow(len);
	   	row.id = len;

	    var cell = document.createElement("TD");
	    cell.setAttribute("class", 'last');
		var inp = document.createElement("img");
		inp.setAttribute("class", "imgDelete");
		inp.setAttribute("src", cpath + "/icons/Delete.png");
		inp.setAttribute("name", "delItem");
		inp.setAttribute("id", "delItem"+len);
		inp.setAttribute("onclick", "deleteItem(this, "+len+")");
		cell.appendChild(inp);

		var inp0 = document.createElement("INPUT");
	    inp0.setAttribute("type", "hidden");
	    inp0.setAttribute("name", "hdeleted");
	    inp0.setAttribute("id", "hdeleted"+len);
	    inp0.setAttribute("value", "false");
	    cell.appendChild(inp0);

	    var cell1 = document.createElement("TD");
		cell1.setAttribute("id", "consumable" + len);
		cell1.setAttribute("class", "label border");
		cell1.setAttribute("title", consumable.options[consumable.selectedIndex].value);
		//cell1.setAttribute("style", "max-width: 15em");
		var text4 = document.createTextNode(consumable.options[consumable.selectedIndex].text);
		cell1.appendChild(text4);

		var inp11 = document.createElement("INPUT");
	    inp11.setAttribute("type", "hidden");
	    inp11.setAttribute("name", "consumable");
	    inp11.setAttribute("id", "consumable"+len);
	    inp11.setAttribute("value", consumable.options[consumable.selectedIndex].value);
	    cell1.appendChild(inp11);
	    addedRegaents.length = i;
	    addedRegaents[consumable.options[consumable.selectedIndex].text] = consumable.options[consumable.selectedIndex].text;

	    var cell2 = document.createElement("TD");
	    cell2.setAttribute("class", 'border');
		var inp21 = document.createElement("INPUT");
	    inp21.setAttribute("type", "text");
	    inp21.setAttribute("name", "qty");
	    inp21.setAttribute("size", "3");
	    inp21.setAttribute("id", "qty"+len);
	    inp21.setAttribute("value", qty);

	    cell2.appendChild(inp21);

		row.appendChild(cell1);
		row.appendChild(cell2);
		row.appendChild(cell);
	}


	function deleteItem(checkBox, rowId) {
		var itemListTable = document.getElementById("reagentstable");
		var row = itemListTable.rows[rowId];
		var deletedInput =
			document.getElementById('hdeleted' + rowId).value = document.getElementById('hdeleted' + rowId).value == 'false' ? 'true' : 'false';
		if (deletedInput == 'true') {
			addClassName(document.getElementById(rowId), "delete");
			document.getElementById('delItem'+rowId).src = cpath+'/icons/Deleted.png';
		} else {
			removeClassName(document.getElementById(rowId), "delete");
			document.getElementById('delItem'+rowId).src = cpath+'/icons/Delete.png';
		}
	}
	var isuueUnits = [];
	var issueQty = [];

	function getConsumableItems(){
		var category = document.serviceConsumableMasterForm.category_id.value;
		document.getElementById("quantity_needed").value = '';
		var i=1;
		document.serviceConsumableMasterForm.consumable_id.options[0].text = '....Consumable....';
		document.serviceConsumableMasterForm.consumable_id.options[0].value = '';
		document.serviceConsumableMasterForm.consumable_id.length = 1;
		for (var j = 0; j<itList.length; j++) {
			if (category == itList[j].CATEGORY_ID ) {
				document.serviceConsumableMasterForm.consumable_id.length = i;
				document.serviceConsumableMasterForm.consumable_id.options[i] = new Option(itList[j].ITEM_NAME, itList[j].ITEM_ID);
				document.serviceConsumableMasterForm.consumable_id.options[i].text = itList[j].ITEM_NAME;
				document.serviceConsumableMasterForm.consumable_id.options[i].value = itList[j].ITEM_ID;
				isuueUnits[itList[j].ITEM_ID] = itList[j].ISSUE_UNITS;
				issueQty[itList[j].ITEM_ID] = itList[j].ISSUE_QTY;
				i++;
			}
		}
	}

	function setIsuueDetails(consumable) {
		if (document.serviceConsumableMasterForm.consumable_id.selectedIndex == 0) {
			document.getElementById("issueqty").innerHTML = '';
		} else {
			document.getElementById("issueqty").innerHTML = isuueUnits[consumable];
		}
	}


	function makeingDec(objValue,obj){
	    if (objValue!= '' && objValue!= '.') {
			document.getElementById(obj.name).value = parseFloat(objValue).toFixed(2);
		}
		if (objValue == '.' || objValue == '') document.getElementById(obj.name).value = 0.00;
	}


	function makeingDecValidate(objValue,obj,id) {
		if (objValue!= '' && objValue!= '.') {
			document.getElementById(obj.name+id).value = parseFloat(objValue).toFixed(2);
		}
		if (objValue == '.' || objValue == '') document.getElementById(obj.name+id).value = 0.00;
	}