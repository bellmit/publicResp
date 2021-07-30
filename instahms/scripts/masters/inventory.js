
	  function doClose() {
		window.location.href = cpath+"/master/InventoryKitMaster.do?method=list";
	  }
      function focus(){
      	document.forms[0].category.focus();
      }
      function checkBillable(){
      var itemTable = document.getElementById("itemTable");
		var numRows = itemTable.rows.length;
		if(numRows > 1){
		if(document.getElementById("kit_type").value == 'H'){
			for(var i =0;i<1;i++){
				if(kitListJson[i]["billable"]){
					alert("Kit has some billable items can not be a hospital type");
					document.getElementById("kit_type").value = 'P';
				}
			  }
			}
		}

      }
      function addItem(){
     	var itemTable = document.getElementById("itemTable");
		var numRows = itemTable.rows.length;
      if (validate()){
	    var itemTable = document.getElementById("itemTable");
		var numRows = itemTable.rows.length;

		var id = numRows;

	   	var tbody = document.getElementById("itemTable");
	   	var len = tbody.rows.length;

		var oldrowIndex = len - 1;
	   	var row = document.createElement("TR");
	   	row.id="itemRow"+len;
	    var oldRow=document.getElementById("itemRow"+oldrowIndex);

		var category = document.forms[0].category;
		var item  = document.forms[0].item;
		var qty = document.forms[0].Quantity.value;
		var issue_type = document.forms[0].issue_type.options[document.forms[0].issue_type.selectedIndex].text;

		var catText = category.options[category.selectedIndex].text;
		var catVal = category.value;
		var itemText = item.options[item.selectedIndex].text;
		var itemVal = item.value;


		var cell = document.createElement("TD");
		var inp = document.createElement("INPUT");
		inp.setAttribute("type","checkbox");
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
		cell1.setAttribute("class", "label");
		cell1.setAttribute("title", catText);
		cell1.setAttribute("style", "max-width: 15em");
		var text4 = document.createTextNode(catText);
		cell1.appendChild(text4);

		var inp11 = document.createElement("INPUT");
	    inp11.setAttribute("type","hidden");
	    inp11.setAttribute("name","category_id");
	    inp11.setAttribute("id","category_id"+id);
	    inp11.setAttribute("value",catVal);
	    cell1.appendChild(inp11);

	    var inp = document.createElement("INPUT");
	    inp.setAttribute("type","hidden");
	    inp.setAttribute("name","deptoldrnew");
	    inp.setAttribute("id","deptoldrnew"+id);
	    inp.setAttribute("value","new");
	    cell1.appendChild(inp);


		var cell2 = document.createElement("TD");
		cell2.setAttribute("class", "label");
		cell2.setAttribute("title", itemText);
		cell2.setAttribute("style", "max-width: 15em");
		var text4 = document.createTextNode(itemText);
		cell2.appendChild(text4);

		var inp11 = document.createElement("INPUT");
	    inp11.setAttribute("type","hidden");
	    inp11.setAttribute("name","item_id");
	    inp11.setAttribute("id","item_id"+id);
	    inp11.setAttribute("value",itemVal);
	    cell2.appendChild(inp11);

	    var cell3 = document.createElement("TD");
//		cell3.setAttribute("class", "label");
//		cell3.setAttribute("title", qty);
		cell3.setAttribute("style", "max-width: 15em");
//		var text4 = document.createTextNode(qty);
//		cell3.appendChild(text4);

		var inp11 = document.createElement("INPUT");
	    inp11.setAttribute("type","text");
	    inp11.setAttribute("name","qty");
	    inp11.setAttribute("id","qty"+id);
	    inp11.setAttribute("class","num");
	     inp11.setAttribute("onkeypress","return enterNumOnlyzeroToNine(event)");
	    inp11.setAttribute("value",qty);
	    cell3.appendChild(inp11);

	    var cell4 = document.createElement("TD");
		cell4.setAttribute("class", "label");
		cell4.setAttribute("title", issue_type);
		cell4.setAttribute("style", "max-width: 15em");
		var text4 = document.createTextNode(issue_type);
		cell4.appendChild(text4);

		row.appendChild(cell);
		row.appendChild(cell1);
		row.appendChild(cell2);
		row.appendChild(cell3);
		row.appendChild(cell4);
		row.className = "newRow";
		document.getElementById("tbody1").insertBefore(row, oldRow.nextSibling);
		}
      }
      function resetCatogery(){
      	document.forms[0].category.selectedIndex = 0;
      	getkitItems();getIssueType();
      	var table = document.getElementById("itemTable");
      	var length = table.rows.length;
      	if(kit_id == ''){
      	if(length > 1){
      		for(var l = 1;l<length;l++){
      			table.deleteRow(l);
      		}
      	}
      	}
      }
   function getIssueType(){
		var category = document.forms[0].category.value;
		if (category == ''){document.forms[0].issue_type.value = "empty";}
		for ( i=0; i<issueTypeList.length;i++){
		var item = issueTypeList[i];
			if (category == item["CATEGORY_ID"]){
				document.forms[0].issue_type.value = item["ISSUE_TYPE"];
			}
		}
   }

   function getkitItems(){
	   var category = document.forms[0].category.value;
	   var obj = document.forms[0].item;
	   if(document.getElementById("kit_type").value == 'H')var type = 'f';
	   else var type = 't';
	   var k = 0;
		obj.length=1;
		for ( i=0; i<itemList.length;i++){
			var item = itemList[i];
				if (category == item["CATEGORY_ID"]){
					if (type == 'f') {
						if (type == item["BILLABLE"]) {
						k++;
						obj.length = k+1;
						obj.options[k].value = item["ITEM_ID"];
						obj.options[k].text = item["ITEM_NAME"];
						}
					} else {
						k++;
						obj.length = k+1;
						obj.options[k].value = item["ITEM_ID"];
						obj.options[k].text = item["ITEM_NAME"];
					}
				}
		}
   }


	function deleteItem(checkBox, rowId) {
	var itemListTable = document.getElementById("itemTable");
	var row = itemListTable.rows[rowId];
	var deletedInput = document.getElementById('hdeleted'+rowId);
	if (checkBox.checked) {
		deletedInput.value = 'true';
		row.className = "deleted";
	} else {
		deletedInput.value = 'false';
		row.className = "";
	}
	//resetTotals();
}


function validate(){
	if (document.forms[0].category.value == '' ){
		alert("Category is required");
		return false;
	}
	if (document.forms[0].item.value == '' ){
		alert("Item is required");
		return false;
	}
	if (document.forms[0].Quantity.value == '' ){
		alert("Quantity is required");
		return false;
	}

	var itemListTable = document.getElementById("itemTable");
	var numRows = itemListTable.rows.length;
	if (numRows > 1) {
	    for(var k=1;k<numRows;k++){
	    	if (document.forms[0].item.value == document.getElementById('item_id'+k).value ) {
	    		alert(document.forms[0].item.options[document.forms[0].item.selectedIndex].text+" already exists if u want update the qty");
	    		return false;
	    	}
	    }
    }

	return true;
}



