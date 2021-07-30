	var backupName='';
	var addedRegaents = [];

	function doClose() {
		window.location.href = cpath + "/master/DiagnosticReagentMaster.do?_method=list&sortOrder=test_name" +
					"&sortReverse=false&status=A";
	}

	function keepBackUp() {
		if (document.diagnosticReagentMasterForm._method.value == 'update') {
			testBackupName = document.diagnosticReagentMasterForm.test_id.value;
			reagentBackName = document.diagnosticReagentMasterForm.reagent_id.value;
		}
	}
	 function checkduplicate() {
		//var newTestName = document.forms[0].test_name.value;
		//var newReagentName = document.forms[0].reagent_id.value;

		if(document.diagnosticReagentMasterForm._method.value == 'update' && document.diagnosticReagentMasterForm.test_name.value == '' ){
		       alert("Please enter test name");
		       return false;
		}

		if(document.diagnosticReagentMasterForm._method.value == 'create' || document.diagnosticReagentMasterForm._method.value == 'update'){
			if(document.getElementById("reagentstable").rows.length < 2) {
				alert("Add atleast one row ");
				return false;
			}
		}
		/*if(document.forms[0].method.value == 'update'){
				if((newTestName != '${param.test_id}')){
					for (i=0;i<reagentList.length;i++){
						var item = reagentList[i];
						if(item['TEST_ID'] == newTestName && item['REAGENT_ID'] == newReagentName){
							alert("Reagent with this test name already exists");
							return false;
						}
					}
				}
		}*/

		var reagentTableLen = document.getElementById("reagentstable").rows.length;
		for (var k=1;k<reagentTableLen;k++){
			var qtyValue = document.getElementById("qty"+k).value;
			if (qtyValue == ''|| qtyValue == 0){
				alert("Quantity needed can not be Zero");
				document.getElementById("qty"+k).focus();
				return false;
			}
		}
		return true;
	}//end of function
     function addRow(){

     }
     function fillReagents(){
           //alert(reagents.length);
           if (method == 'show'){
            document.getElementById("testDiv").style.display= 'none';
   	        document.getElementById("testLabel").style.display= 'block';
           }else{
            document.getElementById("testDiv").style.display= 'block';
   	        document.getElementById("testLabel").style.display= 'none';
   	        return true;
           }
		for(var i =0;i<reagents.length;i++){
		document.diagnosticReagentMasterForm.test_id.value = test_id;
		if(reagents[i].status == "false")document.getElementById("status").checked = false;
		else if(reagents[i].status == "true")document.getElementById("status").checked = true;

				var reagentTable = document.getElementById("reagentstable");
				var numRows = reagentTable.rows.length;

				var id = numRows;
			   	var tbody = document.getElementById("reagentstable");
			   	var len = tbody.rows.length;

			   	var row = reagentTable.insertRow(id);
			   	row.id=len;

			    var cell = document.createElement("TD");
			    cell.setAttribute("class", 'last');
				var inp = document.createElement("img");
				inp.setAttribute("src", cpath + "/icons/Delete.png");
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
				cell1.setAttribute("title", reagents[i].reagent_id);
				cell1.setAttribute("style", "max-width: 15em");
				var text4 = document.createTextNode(reagents[i].item_name);
				cell1.appendChild(text4);

				var inp11 = document.createElement("INPUT");
			    inp11.setAttribute("type","hidden");
			    inp11.setAttribute("name","reagent");
			    inp11.setAttribute("id","reagent"+id);
			    inp11.setAttribute("value",reagents[i].reagent_id);
			    cell1.appendChild(inp11);
			    addedRegaents.length = i;
			    addedRegaents[reagents[i].item_name] = reagents[i].item_name;

			    var cell2 = document.createElement("TD");
			    cell2.setAttribute("class", 'border');
				var inp21 = document.createElement("INPUT");
			    inp21.setAttribute("type","text");
			    inp21.setAttribute("name","qty");
			    inp21.setAttribute("size","3");
			    inp21.setAttribute("id","qty"+id);
			    inp21.setAttribute("value",reagents[i].quantity_needed);
    			inp21.setAttribute("onkeypress","return enterNumAndDot(event)");
    			inp21.setAttribute("onblur","return makeingDecValidate(this.value,this,'"+id+"')");
			    cell2.appendChild(inp21);

				row.appendChild(cell1);
				row.appendChild(cell2);
				row.appendChild(cell);
				// document.getElementById("reagentstable").insertRow(len);
		}
     }
	var i = 0;

	 function addReagents(test,reagent,qty) {
		 i++;
		 if (addedRegaents.length >= 0){
		 	if(addedRegaents[reagent.options[reagent.selectedIndex].text] != undefined){
		 		alert("Duplicate Entry");
		 		return false;
		 	}
		 }
			if (method != 'show') {
			if(document.diagnosticReagentMasterForm.test_id.value == ''){
				alert("Select test name");
				document.diagnosticReagentMasterForm.test_id.focus();
				return false;
			}
		}

	 	if (document.diagnosticReagentMasterForm.category_id.value == '') {
	 		alert("Select a Category");
	 		document.diagnosticReagentMasterForm.category_id.focus();
	 		return false;
	 	}
	 	if(document.diagnosticReagentMasterForm.reagent_id.value == ''){
	 		alert("Select a Reagent");
	 		document.diagnosticReagentMasterForm.reagent_id.focus();
	 		return false;
	 	}
	 	if (document.diagnosticReagentMasterForm.quantity_needed.value == '') {
	 		alert("Quantity needed can not be empty");
	 		document.diagnosticReagentMasterForm.quantity_needed.focus();
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
	    cell.setAttribute("class", 'last');
		var inp = document.createElement("img");
		inp.setAttribute("src", cpath + "/icons/Delete.png");
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
		cell1.setAttribute("title", reagent.options[reagent.selectedIndex].value);
		cell1.setAttribute("style", "max-width: 15em");
		var text4 = document.createTextNode(reagent.options[reagent.selectedIndex].text);
		cell1.appendChild(text4);

		var inp11 = document.createElement("INPUT");
	    inp11.setAttribute("type","hidden");
	    inp11.setAttribute("name","reagent");
	    inp11.setAttribute("id","reagent"+id);
	    inp11.setAttribute("value",reagent.options[reagent.selectedIndex].value);
	    cell1.appendChild(inp11);
	    addedRegaents.length = i;
	    addedRegaents[reagent.options[reagent.selectedIndex].text] = reagent.options[reagent.selectedIndex].text;

	    var cell2 = document.createElement("TD");
	    cell2.setAttribute("class", 'border');
		var inp21 = document.createElement("INPUT");
	    inp21.setAttribute("type","text");
	    inp21.setAttribute("name","qty");
	    inp21.setAttribute("size","3");
	    inp21.setAttribute("id","qty"+id);
	    inp21.setAttribute("value",qty.value);
	    inp21.setAttribute("onkeypress","return enterNumAndDot(event)");
	    inp21.setAttribute("onblur","return makeingDecValidate(this.value,this,'"+id+"')");

	    cell2.appendChild(inp21);

		row.appendChild(cell1);
		row.appendChild(cell2);
		row.appendChild(cell);
		//	document.getElementById("reagentstable").insertRow(len);
	}


  function deleteItem(checkBox, rowId) {
		var itemListTable = document.getElementById("reagentstable");
		var row = itemListTable.rows[rowId];
		var deletedInput =
			document.getElementById('hdeleted'+rowId).value = document.getElementById('hdeleted' + rowId).value == 'false' ? 'true' : 'false';
		if (deletedInput == 'true') {
			addClassName(document.getElementById(rowId), "delete");
			document.getElementById('delItem' + rowId).src = cpath + "/icons/Deleted.png";
		} else {
			removeClassName(document.getElementById(rowId), "delete");
			document.getElementById('delItem' + rowId).src = cpath + "/icons/Delete.png";
		}
  }
  	var isuueUnits = [];
	var issueQty = [];

  function getReagentItems(){
 		var category = document.diagnosticReagentMasterForm.category_id.value;
		document.getElementById("quantity_needed").value = '';

		var i=1;
		var exists = 0;
		document.diagnosticReagentMasterForm.reagent_id.options[0].text = '....Reagent....';
		document.diagnosticReagentMasterForm.reagent_id.options[0].value = '';
		document.diagnosticReagentMasterForm.reagent_id.length = 1;
		for(var j = 0;j<itList.length;j++) {
			if(category == itList[j].CATEGORY_ID ) {
				exists = 1;
				document.diagnosticReagentMasterForm.reagent_id.length = i;
				document.diagnosticReagentMasterForm.reagent_id.options[i] = new Option(itList[j].ITEM_NAME, itList[j].ITEM_ID);
				document.diagnosticReagentMasterForm.reagent_id.options[i].text = itList[j].ITEM_NAME;
				document.diagnosticReagentMasterForm.reagent_id.options[i].value = itList[j].ITEM_ID;
				isuueUnits[itList[j].ITEM_ID] = itList[j].ISSUE_UNITS;
				issueQty[itList[j].ITEM_ID] = itList[j].ISSUE_QTY;
				i++;
			}
	    }

  }
	function setIsuueDetails(reagent){
		if(document.diagnosticReagentMasterForm.reagent_id.selectedIndex == 0){
			document.getElementById("issueqty").innerHTML = '';
		}else{
			document.getElementById("issueqty").innerHTML = isuueUnits[reagent];
		}
	}
	function makeingDec(objValue,obj){
	    if (objValue!= '' && objValue!= '.') {
			document.getElementById(obj.name).value = parseFloat(objValue).toFixed(2);
		}
		if (objValue == '.' || objValue == '') document.getElementById(obj.name).value = 0.00;
    }
    function makeingDecValidate(objValue,obj,id){
	    if (objValue!= '' && objValue!= '.') {
			document.getElementById(obj.name+id).value = parseFloat(objValue).toFixed(2);
		}
		if (objValue == '.' || objValue == '') document.getElementById(obj.name+id).value = 0.00;
	}
