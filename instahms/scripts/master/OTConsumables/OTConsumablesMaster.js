var i = 0;
		var addedRegaents = [];
	 function addReagents(test,consumable,qty){
	 i++;
	 if(addedRegaents.length >= 0){
	 	if(addedRegaents[consumable.options[consumable.selectedIndex].text] != undefined){
	 		alert("Duplicate Entry");
	 		return false;
	 	}
	 }
	 	if (document.getElementById("_method").value != 'create') {
			if(document.otconsumablesform.op_id.value == ''){
				alert("Select Operation name");
				document.otconsumablesform.service_id.focus();
				return false;
			}
		}

	 	if(document.otconsumablesform.category_id.value == ''){
	 		alert("Select a catogery");
	 		document.otconsumablesform.category_id.focus();
	 		return false;
	 	}
	 	if(document.otconsumablesform.consumable_id.value == ''){
	 		alert("Select a Consumable");
	 		document.otconsumablesform.consumable_id.focus();
	 		return false;
	 	}
	 	if(document.otconsumablesform.qty_needed.value == ''){
	 		alert("Quantity needed can not be empty");
	 		document.otconsumablesform.qty_needed.focus();
	 		return false;
	 	}
	 	var reagentTable = document.getElementById("reagentstable");
		var numRows = reagentTable.rows.length;

		var id = numRows;
	  	var tbody = document.getElementById("reagentstable");
	  	var len = tbody.rows.length;

	   	var row = reagentTable.insertRow(id);
	   	row.id=len;

	    var cell = document.createElement("TD");
		cell.setAttribute("class", "last");
		var inp = document.createElement("img");
		inp.setAttribute("src", cPath + "/icons/Delete.png");
		inp.setAttribute("name","delItem");
		inp.setAttribute("id","delItem"+id);
		inp.setAttribute("onclick","deleteItem(this, "+id+")");
		cell.appendChild(inp);

		var inp0 = document.createElement("INPUT");
	    inp0.setAttribute("type","hidden");
	    inp0.setAttribute("name","hdeleted");
	    inp0.setAttribute("id","hdeleted"+id);
	    inp0.setAttribute("value","false");
	    cell.appendChild(inp0);

	    var cell1 = document.createElement("TD");
		cell1.setAttribute("class", "label border");
		cell1.setAttribute("title", consumable.options[consumable.selectedIndex].value);
		cell1.setAttribute("style", "max-width: 15em ");
		var text4 = document.createTextNode(consumable.options[consumable.selectedIndex].text);
		cell1.appendChild(text4);

		var inp11 = document.createElement("INPUT");
	    inp11.setAttribute("type","hidden");
	    inp11.setAttribute("name","consumable");
	    inp11.setAttribute("id","consumable"+id);
	    inp11.setAttribute("value",consumable.options[consumable.selectedIndex].value);
	    cell1.appendChild(inp11);
	    addedRegaents.length = i;
	    addedRegaents[consumable.options[consumable.selectedIndex].text] = consumable.options[consumable.selectedIndex].text;

	    var cell2 = document.createElement("TD");
		cell2.setAttribute("class", 'border');
		var inp21 = document.createElement("INPUT");
	    inp21.setAttribute("type","text");
	    inp21.setAttribute("name","qty");
	    inp21.setAttribute("size","3");
	    inp21.setAttribute("id","qty"+id);
	    inp21.setAttribute("value",qty.value);
	    inp21.setAttribute("class","number");
	   	inp21.setAttribute("style", 'border-left : 1px #cad6e3 solid');
	    inp21.setAttribute("onkeypress","return enterNumAndDot(event)");
	    inp21.setAttribute("onblur","return makeingDecValidate(this)");

	    cell2.appendChild(inp21);

		row.appendChild(cell1);
		row.appendChild(cell2);
		row.appendChild(cell);
		//document.getElementById("reagentstable").insertRow(len);
      }
var isuueUnits = [];
var issueQty = [];

      function getConsumableItems(){
 		var category = document.otconsumablesform.category_id.value;
		document.getElementById("qty_needed").value = '';
			var i=1;
			var exists = 0;
			document.otconsumablesform.consumable_id.options[0].text = '....Consumable....';
			document.otconsumablesform.consumable_id.options[0].value = '';
			document.otconsumablesform.consumable_id.length = 1;
			for(var j = 0;j<itList.length;j++){
				if(category == itList[j].CATEGORY_ID ){
					exists = 1;
					document.otconsumablesform.consumable_id.length = i;
					document.otconsumablesform.consumable_id.options[i] = new Option(itList[j].ITEM_NAME, itList[j].ITEM_ID);
					document.otconsumablesform.consumable_id.options[i].text = itList[j].ITEM_NAME;
					document.otconsumablesform.consumable_id.options[i].value = itList[j].ITEM_ID;
					isuueUnits[itList[j].ITEM_ID] = itList[j].ISSUE_UNITS;
					issueQty[itList[j].ITEM_ID] = itList[j].ISSUE_QTY;
					i++;
				}
			}
			if(exists != 1){
					document.otconsumablesform.consumable_id.length = 1;
					document.otconsumablesform.consumable_id.options[0].text = '....Consumable....';
					document.otconsumablesform.consumable_id.options[0].value = '';
				}

	}
	function setIsuueDetails(consumable){
		if(document.otconsumablesform.consumable_id.selectedIndex == 0){
			document.getElementById("issueqty").innerHTML = '';
		}else{
			document.getElementById("issueqty").innerHTML = isuueUnits[consumable];
		}
	}
	 function checkduplicate(){
	  if(document.otconsumablesform.op_id.value == '' && document.otconsumablesform._method.value == 'create'){
	     alert("Please enter operation name");
         return false;
       }
		if(document.otconsumablesform._method.value == 'create' || document.otconsumablesform._method.value == 'update'){
			if(document.getElementById("reagentstable").rows.length < 2) {
				alert("Add atleast one row ");
				return false;
			}
		}

		var reagentTableLen = document.getElementById("reagentstable").rows.length;
		for (var k=1;k<reagentTableLen;k++){
			var qtyValue = document.getElementById("qty"+k).value;
			if (qtyValue == '' || qtyValue == 0){
				alert("Quantity needed can not be Zero");
				document.getElementById("qty"+k).focus();
				return false;
			}
		}
		return true;
      }//end of function
   function deleteItem(checkBox, rowId) {
		var itemListTable = document.getElementById("reagentstable");
		var row = itemListTable.rows[rowId];
		var deletedInput =
			document.getElementById('hdeleted'+rowId).value = document.getElementById('hdeleted'+rowId).value == 'false' ? 'true' : 'false';
		if (deletedInput == 'true') {
			document.getElementById('delItem' + rowId).src = cPath + "/icons/Deleted.png";
			addClassName(document.getElementById(rowId), "delete");
		} else {
			document.getElementById('delItem' + rowId).src = cPath + "/icons/Delete.png";
			removeClassName(document.getElementById(rowId), "delete");

		}
  }

  function doClose() {
  		window.location.href = cPath + "/master/OTConsumablesMaster.do?_method=list&" +
  			"sortOrder=operation_name&sortReverse=false&status=A";
  }

  function makeingDec(object) {
  	objValue = object.value;
  	if(objValue != '' && objValue != ('.')) {
  		document.getElementById(object.name).value = parseFloat(objValue).toFixed(2);
  	} else {
  		document.getElementById(object.name).value = 0.00;
  	}
  }

  function makeingDecValidate(qtyObj) {
  	qtyValue = qtyObj.value;
  	if(qtyValue != '' && qtyValue != '.') {
		document.getElementById(qtyObj.id).value = parseFloat(qtyValue).toFixed(2);
  	} else {
  		document.getElementById(qtyObj.id).value = 0.00;
  	}
  }
