
function setRows() {
	var baseItemTbl = document.getElementById("baseItemTbl");
	var len = baseItemTbl.rows.length;
	var templateRow = baseItemTbl.rows[len-2];
   	var row = '';
   		row = templateRow.cloneNode(true);
   		row.style.display = '';
   		row.id = len-3;
   		len = row.id;
   	YAHOO.util.Dom.insertBefore(row, templateRow);

	var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width: 40px");
	var inp = document.createElement("img");
	inp.setAttribute("class", "imgDelete");
	inp.setAttribute("src", cpath + "/icons/Delete.png");
	inp.setAttribute("name", "delItem");
	inp.setAttribute("id", "delItem"+len);
	inp.setAttribute("onclick", "deleteItem(this, "+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "deleted");
    inp0.setAttribute("id", "deleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "hidden");
    inp1.setAttribute("name", "addedNew");
    inp1.setAttribute("id", "addedNew"+len);
    inp1.setAttribute("value", "true");
    cell.appendChild(inp1);
    
	var cell1 = row.insertCell(-1);
   // cell1.setAttribute("style", "width: 180px");

  
    var select = '';
    //var select1='';
    var discount_value1 = '';
    var discount_type1 = '';
    var priority1 = '';
    var selectValue = '';
  
    
    var map0 = {"key":" ","value":"--Select--"};
    var map1 = {"key":"C","value":"Chargehead"};
    var map2 = {"key":"N","value":"Insurance Category"};
    var map3 = {"key":"I","value":"Item"};
    var map4 = {"key":"S","value":"Item Category"};
   
    var applicable_type_value = discountPlanDetails["applicable_type"].trim();
    var arrayList = [map0, map1, map2, map3, map4];
    for(var k=0; k<arrayList.length; k++){
    	if(applicable_type_value == arrayList[k]["key"]){
    		selectValue = arrayList[k]["key"];
    		select = arrayList[k]["value"];
    		
    	}
    }
  
	    select0 = discountPlanDetails["applicable_type"].trim();
		discount_value1 = discountPlanDetails["discount_value"];
		discount_type1 = discountPlanDetails["discount_type"].trim();
		priority1 = discountPlanDetails["priority"];
			
	var discount_plan_detail_id = discountPlanDetails["discount_plan_detail_id"];
	var inp1 = document.createElement("INPUT");
	    inp1.setAttribute("type", "hidden");
	    inp1.setAttribute("name", "discount_plan_detail_id");
	    inp1.setAttribute("id", "discount_plan_detail_id"+len);
	    inp1.setAttribute("value", discount_plan_detail_id);
	    cell.appendChild(inp1);
		
	   
	   
    cell1.innerHTML = '<select name="applicable_type" id="applicable_type'+len+'" class="dropdown" onchange="setValues('+len+')" ></select>';
	loadSelectBox(document.getElementById("applicable_type"+len), arrayList, "value", "key");
	document.getElementById("applicable_type"+len).value = selectValue;
	//document.getElementById('applicable_type'+len).setAttribute('style', 'width: 180px');
	
	var cell2 = document.createElement("TD");
	//cell2.setAttribute("style", "width: 280px");
	
	var applicable_type_value = discountPlanDetails["applicable_type"].trim();
	var applicable_to_id = discountPlanDetails["applicable_to_id"].trim();
	var inputApplicableToIdValue = document.createElement("INPUT");
	    inputApplicableToIdValue.setAttribute("type", "hidden");
	    inputApplicableToIdValue.setAttribute("name", "applicable_to_id_value");
	    inputApplicableToIdValue.setAttribute("id", "applicable_to_id_value"+len);
	    inputApplicableToIdValue.setAttribute("value", applicable_to_id);
	    
	var inputApplicableToIdSub = document.createElement("INPUT");
	    inputApplicableToIdSub.setAttribute("type", "hidden");
	    inputApplicableToIdSub.setAttribute("name", "applicable_to_id_subgroup");
	    inputApplicableToIdSub.setAttribute("id", "applicable_to_id_subgroup"+len);
	    //inputApplicableToIdSub.setAttribute("value", applicable_to_id_subgroup);
	  	


	    if(applicable_type_value == "I"){
	    	var inp2 = document.createElement("INPUT");
	    	inp2.setAttribute("type", "hidden");
	    	inp2.setAttribute("name", "applicable_to_id");
	    	inp2.setAttribute("size", "3");
	    	inp2.setAttribute("id", "applicable_to_id" + len);

	    	var inp21 = document.createElement("INPUT");
	    	inp21.setAttribute("type", "text");
	    	inp21.setAttribute("name", "applicable_to_name");
	    	inp21.setAttribute("id", "applicable_to_name" + len);
	    	inp21.setAttribute("size", "3");
		    
	        items.result.some(function (item, i) {
	            if (item.id == applicable_to_id) {
	            	 inp2.setAttribute("value", item.id);
	            	 inp21.setAttribute("value", item.name);
	                 return true;
	            }
	        });
		  

		    inp2.setAttribute("style", "width: 250px");
			inp21.setAttribute("style", "width: 250px");
			inp21.setAttribute("onblur", "checkDuplicateItem(" + len + ")");
			cell2.appendChild(inp21);
			cell2.appendChild(inp2);
		   
		    inputApplicableToIdSub.setAttribute("value", discountPlanDetails["applicable_to_id_subgroup"]);
		    
	    }else{
			var inp2 = document.createElement("INPUT");
			inp2.setAttribute("name", "applicable_to_id");
			inp2.setAttribute("size", "3");
			inp2.setAttribute("id", "applicable_to_id" + len);
			inp2.setAttribute("type", "text");
		
			inp2.setAttribute("value", applicable_to_id);
		
			inp2.setAttribute("style", "width: 250px");
			cell2.appendChild(inp2);
		
	    }
	
	cell2.appendChild(inputApplicableToIdValue); 
    cell2.appendChild(inputApplicableToIdSub);
    
	var iDiv = document.createElement('div');
	iDiv.id = 'itemcontainer'+len;
	iDiv.style = 'width:21em';
	cell2.appendChild(iDiv);
	
	
	var num = discount_value1;
	
	var cell3 = document.createElement("TD");
	// cell3.setAttribute("style", "width: 150px");
	var inp3 = document.createElement("INPUT");
    inp3.setAttribute("type", "text");
    inp3.setAttribute("name", "discount_value");
    inp3.setAttribute("class", "numeric");
    inp3.setAttribute("id", "discount_value"+len);
    inp3.setAttribute("value", num.toFixed(2));
    inp3.setAttribute("style", "width: 80px");
    cell3.appendChild(inp3);

    var cell4 = document.createElement("TD");
   // cell4.setAttribute("style", "width: 180px");
   
	
     if(applicable_type_value == "I"){
		var cell2select = document.createElement("select");
		cell2select.setAttribute("name", "discount_type");
		cell2select.setAttribute("id", "discount_type"+len);
		cell2select.setAttribute("class", "dropdown");
		var cell2selectoption = document.createElement("option");
		cell2selectoption.setAttribute("value", "P");
		cell2selectoption.innerHTML = "Percentage";
		cell2select.appendChild(cell2selectoption);
		var cell2selectoption = document.createElement("option");
		cell2selectoption.setAttribute("value", "A");
		if(discount_type1 == "A")
		cell2selectoption.setAttribute("selected", "selected");
		cell2selectoption.innerHTML = "Amount";
		cell2select.appendChild(cell2selectoption);
	
		cell4.appendChild(cell2select);
		
	
	}else{
		
		cell4.innerHTML = '<select name="discount_type" id="discount_type'+len+'" class="dropdown"/>'+
							'<option value="P">Percentage</option> '+
							'<option value="A">Amount</option> '+
						  '</select>';
	}

	var cell5 = document.createElement("TD");
	//cell5.setAttribute("style", "width: 100px");
	var inp5 = document.createElement("INPUT");
	inp5.setAttribute("type", "text");
    inp5.setAttribute("name", "priority");
    inp5.setAttribute("size", "3");
    inp5.setAttribute("id", "priority"+len);
    inp5.setAttribute("value", priority1);
    inp5.setAttribute("style", "width: 80px");
    inp5.setAttribute("onblur", "checkDuplicatePriority("+len+")");
    cell5.appendChild(inp5);

    row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	row.appendChild(cell5);

	row.appendChild(cell);
	
}

function AddRow() {
	var baseItemTbl = document.getElementById("baseItemTbl");
	var len = baseItemTbl.rows.length;
	var templateRow = baseItemTbl.rows[len-2];
   	row = '';
   	row = templateRow.cloneNode(true);
   	row.style.display = '';
   	row.id = len-3;
   	len = row.id;
   	YAHOO.util.Dom.insertBefore(row, templateRow);

	var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width: 40px");
	var inp = document.createElement("img");
	inp.setAttribute("class", "imgDelete");
	inp.setAttribute("src", cpath + "/icons/Delete.png");
	inp.setAttribute("name", "delItem");
	inp.setAttribute("id", "delItem"+len);
	inp.setAttribute("onclick", "deleteItem(this, "+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "deleted");
    inp0.setAttribute("id", "deleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "hidden");
    inp1.setAttribute("name", "addedNew");
    inp1.setAttribute("id", "addedNew"+len);
    inp1.setAttribute("value", "true");
    cell.appendChild(inp1);

	var cell1 = row.insertCell(-1);
   // cell1.setAttribute("style", "width: 300px");

  
    var map0 = {"key":" ","value":"-Select--"};
    var map1 = {"key":"C","value":"Chargehead"};
    var map2 = {"key":"N","value":"Insurance category"};
    var map3 = {"key":"I","value":"Item"};
    var map4 = {"key":"S","value":"Item Category"};
   
    var arrayList = [map0, map1, map2, map3, map4];
    
    cell1.innerHTML = '<select name="applicable_type" id="applicable_type'+len+'" class="dropdown" onchange="setValues('+len+')" ></select>';
	loadSelectBox(document.getElementById("applicable_type"+len), arrayList, "value", "key");
   // document.getElementById('applicable_type'+len).setAttribute("style", "width: 150px");
 
	var cell2 = document.createElement("TD");
	//cell2.setAttribute("style", "width: 280px");
	

	var cell2select = document.createElement("select");
	cell2select.setAttribute("name", "applicable_to_id");
	cell2select.setAttribute("id", "applicable_to_id"+len);
	cell2select.setAttribute("class", "dropdown");
	cell2select.setAttribute("style", "width: 250px");
	cell2select.setAttribute("onblur", "checkDuplicateItem("+len+")");
	var cell2selectoption = document.createElement("option");
	cell2selectoption.setAttribute("value", "");
	cell2selectoption.innerHTML = "--Select--";
	cell2select.appendChild(cell2selectoption);
	
	cell2.appendChild(cell2select);
	
	var inputApplicableToIdValue = document.createElement("INPUT");
    inputApplicableToIdValue.setAttribute("type", "hidden");
    inputApplicableToIdValue.setAttribute("name", "applicable_to_id_value");
    inputApplicableToIdValue.setAttribute("id", "applicable_to_id_value"+len);
    inputApplicableToIdValue.setAttribute("value", "");
    
    var inputApplicableToIdSub = document.createElement("INPUT");
    inputApplicableToIdSub.setAttribute("type", "hidden");
    inputApplicableToIdSub.setAttribute("name", "applicable_to_id_subgroup");
    inputApplicableToIdSub.setAttribute("id", "applicable_to_id_subgroup"+len);
    inputApplicableToIdSub.setAttribute("value", "");
    
    cell2.appendChild(inputApplicableToIdValue);
    cell2.appendChild(inputApplicableToIdSub);
	
	var iDiv = document.createElement('div');
	iDiv.id = 'itemcontainer'+len;
	iDiv.style = 'width:21em';
	cell2.appendChild(iDiv);
	
	var cell3 = document.createElement("TD");
	// cell3.setAttribute("style", "width: 100px");
	var inp3 = document.createElement("INPUT");
    inp3.setAttribute("type", "text");
    inp3.setAttribute("name", "discount_value");
    inp3.setAttribute("size", "3");
    inp3.setAttribute("class", "numeric");
    inp3.setAttribute("id", "discount_value"+len);
    inp3.setAttribute("style", "width: 80px");
    cell3.appendChild(inp3);

    var cell4 = document.createElement("TD");
   // cell4.setAttribute("style", "width: 170px");
  
	if(document.getElementById('applicable_type'+len).value != " "){
		
		
	cell4.innerHTML = '<select name="discount_type" id="discount_type'+len+'" class="dropdown"/>'+
					   '<option value="P">Percentage</option> '+
					   '<option value="A">Amount</option> '+
					  '</select>';
	}else{
		
		cell4.innerHTML = '<select name="discount_type" id="discount_type'+len+'" class="dropdown" disabled />'+
						   '<option value="P">Percentage</option> '+
						   '<option value="A">Amount</option> '+
						  '</select>';
	}

	var cell5 = document.createElement("TD");
	// cell5.setAttribute("style", "width: 20px");
	var inp5 = document.createElement("INPUT");
	inp5.setAttribute("type", "text");
    inp5.setAttribute("name", "priority");
    inp5.setAttribute("size", "3");
    inp5.setAttribute("id", "priority"+len);
    inp5.setAttribute("style", "width: 80px");
    inp5.setAttribute("onblur", "checkDuplicatePriority("+len+")");
    cell5.appendChild(inp5);

  
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	row.appendChild(cell5);

	row.appendChild(cell);
}



function deleteItem(checkBox, rowId) {
	var RateTbl = document.getElementById("baseItemTbl");
	var row = RateTbl.rows[rowId];
	var deletedInput =
		document.getElementById('deleted' + rowId).value = document.getElementById('deleted' + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(rowId), "delete");
		document.getElementById('delItem'+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(rowId), "delete");
		document.getElementById('delItem'+rowId).src = cpath+'/icons/Delete.png';
	}
}

function setSelectedValues(id){
    var applicable_to_id_id = document.getElementById('applicable_to_id'+id).value;
   
	var prevSel = document.getElementById('applicable_to_id'+id);
	prevSel.length = 0;
	
	if(document.getElementById("applicable_type"+id).value == "I"){
		 
		var cell2 = document.createElement("INPUT");
		cell2.setAttribute("type", "text");
		cell2.setAttribute("name", "applicable_to_id");
		cell2.setAttribute("id", "applicable_to_id"+id);
		cell2.setAttribute("onblur", "checkDuplicateItem("+id+")");
		cell2.setAttribute("style", "width: 250px");
		cell2.setAttribute("value", applicable_to_id_id);
		
	    prevSel.parentNode.replaceChild(cell2,prevSel);
	    autoDiscountPlanMaster(id);
	   
	    //document.getElementById('discount_type'+id).disabled=false;
	}
	document.getElementById('applicable_to_id'+id).className= 'dropdown';
    
	if(document.getElementById("applicable_type"+id).value == "N"){
		
		var cell2select = document.createElement("select");
		cell2select.setAttribute("name", "applicable_to_id");
		cell2select.setAttribute("id", "applicable_to_id"+id);
		cell2select.setAttribute("class", "dropdown");
		cell2select.setAttribute("style", "width: 250px");
		cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
		var cell2selectoption = document.createElement("option");
		cell2selectoption.setAttribute("value", "");
		cell2selectoption.innerHTML = "--Select--";
		
		cell2select.appendChild(cell2selectoption);
		
		
		for(var k=0; k<insuranceCategoryList.length; k++){
    	
			var list = insuranceCategoryList[k];
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", list.insurance_category_id);
			cell2selectoption.text = list.insurance_category_name;
			if(list.insurance_category_id==applicable_to_id_id)
			cell2selectoption.setAttribute("selected", "selected");
			
			cell2select.appendChild(cell2selectoption);
			
		}
		prevSel.parentNode.replaceChild(cell2select,prevSel);
		//document.getElementById('discount_type'+id).disabled= true;
		document.getElementById('discount_type'+id).remove(1);
    }

 	if(document.getElementById("applicable_type"+id).value == "S"){
		
		var cell2select = document.createElement("select");
		cell2select.setAttribute("name", "applicable_to_id");
		cell2select.setAttribute("id", "applicable_to_id"+id);
		cell2select.setAttribute("class", "dropdown");
		cell2select.setAttribute("style", "width: 250px");
		cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
		var cell2selectoption = document.createElement("option");
		cell2selectoption.setAttribute("value", "");
		cell2selectoption.innerHTML = "--Select--";
		
		cell2select.appendChild(cell2selectoption);
		
		
		for(var k=0; k<itemCategoryList.length; k++){
    	
			var list = itemCategoryList[k];
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", list.category_id);
			cell2selectoption.text = list.category;
			if(list.category_id==applicable_to_id_id)
			cell2selectoption.setAttribute("selected", "selected");
			
			cell2select.appendChild(cell2selectoption);
			
		}
		prevSel.parentNode.replaceChild(cell2select,prevSel);
		//document.getElementById('discount_type'+id).disabled= true;
		document.getElementById('discount_type'+id).remove(1);
    }
	
	if(document.getElementById("applicable_type"+id).value == "C"){
		var cell2select = document.createElement("select");
		cell2select.setAttribute("name", "applicable_to_id");
		cell2select.setAttribute("id", "applicable_to_id"+id);
		cell2select.setAttribute("class", "dropdown");
		cell2select.setAttribute("style", "width: 250px");
		cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
		var cell2selectoption = document.createElement("option");
		cell2selectoption.setAttribute("value", "");
		cell2selectoption.innerHTML = "--Select--";
		
		cell2select.appendChild(cell2selectoption);
		
		
		for(var k=0; k<chargeHeadList.length; k++){
    	
			var list = chargeHeadList[k];
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", list.chargehead_id);
			cell2selectoption.text = list.chargehead_name;
			if(list.chargehead_id==applicable_to_id_id)
			cell2selectoption.setAttribute("selected", "selected");
			
			cell2select.appendChild(cell2selectoption);
			
		}
		prevSel.parentNode.replaceChild(cell2select,prevSel);
		//document.getElementById('discount_type'+id).disabled= true;
		document.getElementById('discount_type'+id).remove(1);
    }
	
	if(document.getElementById("applicable_type"+id).value == " "){
		var cell2select = document.createElement("select");
		cell2select.setAttribute("name", "applicable_to_id");
		cell2select.setAttribute("id", "applicable_to_id"+id);
		cell2select.setAttribute("class", "dropdown");
		cell2select.setAttribute("style", "width: 250px");
		cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
		var cell2selectoption = document.createElement("option");
		cell2selectoption.setAttribute("value", "");
		cell2selectoption.innerHTML = "--Select--";
		cell2select.appendChild(cell2selectoption);
		
		prevSel.parentNode.replaceChild(cell2select,prevSel);
		
		//document.getElementById('discount_type'+id).disabled= true;
		document.getElementById('discount_type'+id).remove(1);
	}

}

function setValues(id){
	   
		var prevSel = document.getElementById('applicable_to_id'+id);
		prevSel.length = 0;
		if(document.getElementById("applicable_type"+id).value == "I"){
			 
			var cell21 = document.createElement("INPUT");
			cell21.setAttribute("type", "hidden");
			cell21.setAttribute("name", "applicable_to_id");
			cell21.setAttribute("id", "applicable_to_id"+id);
			cell21.setAttribute("style", "width: 250px;");
			var cell2 = document.createElement("INPUT");
			cell2.setAttribute("type", "text");
			cell2.setAttribute("name", "applicable_to_name");
			cell2.setAttribute("id", "applicable_to_name"+id);
			cell2.setAttribute("onblur", "checkDuplicateItem("+id+")");
			cell2.setAttribute("style", "width: 250px;margin-top: -10px");
			prevSel.parentNode.appendChild(cell21);
			prevSel.parentNode.replaceChild(cell2, prevSel);
		    autoDiscountPlanMaster(id);
		    
		    var discTypeObj = document.getElementById('discount_type'+id);
		    discTypeObj.disabled=false;
		    if(discTypeObj.childElementCount < 2) {
			    var selectoption = document.createElement("option");
				selectoption.setAttribute("value", "A");
				selectoption.innerHTML = "Amount";
				discTypeObj.appendChild(selectoption);
		    }
		    
		}
		document.getElementById('applicable_to_id'+id).className= 'dropdown';
	    
		if(document.getElementById("applicable_type"+id).value == "N"){
			
			var cell2select = document.createElement("select");
			cell2select.setAttribute("name", "applicable_to_id");
			cell2select.setAttribute("id", "applicable_to_id"+id);
			cell2select.setAttribute("class", "dropdown");
			cell2select.setAttribute("style", "width: 250px");
			cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", "");
			cell2selectoption.innerHTML = "--Select--";
			
			cell2select.appendChild(cell2selectoption);
			
			
			for(var k=0; k<insuranceCategoryList.length; k++){
	    	
				var list = insuranceCategoryList[k];
				var cell2selectoption = document.createElement("option");
				cell2selectoption.setAttribute("value", list.insurance_category_id);
				cell2selectoption.text = list.insurance_category_name;
				
				cell2select.appendChild(cell2selectoption);
				
			}
			prevSel.parentNode.replaceChild(cell2select,prevSel);
			document.getElementById('discount_type'+id).disabled= false;
			var applicable_to_name_tmp =  document.getElementById('applicable_to_name'+id)
			if(applicable_to_name_tmp != null){
				applicable_to_name_tmp.remove();
			}
			//document.getElementById('discount_type'+id).value="P";
			document.getElementById('discount_type'+id).remove(1);
	    }

		if(document.getElementById("applicable_type"+id).value == "S"){
			
			var cell2select = document.createElement("select");
			cell2select.setAttribute("name", "applicable_to_id");
			cell2select.setAttribute("id", "applicable_to_id"+id);
			cell2select.setAttribute("class", "dropdown");
			cell2select.setAttribute("style", "width: 250px");
			cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", "");
			cell2selectoption.innerHTML = "--Select--";
			
			cell2select.appendChild(cell2selectoption);
			
			
			for(var k=0; k<itemCategoryList.length; k++){
	    	
				var list = itemCategoryList[k];
				var cell2selectoption = document.createElement("option");
				cell2selectoption.setAttribute("value", list.category_id);
				cell2selectoption.text = list.category;
				
				cell2select.appendChild(cell2selectoption);
				
			}
			prevSel.parentNode.replaceChild(cell2select,prevSel);
			document.getElementById('discount_type'+id).disabled= false;
			var applicable_to_name_tmp =  document.getElementById('applicable_to_name'+id)
			if(applicable_to_name_tmp != null){
				applicable_to_name_tmp.remove();
			}
			//document.getElementById('discount_type'+id).value="P";
			document.getElementById('discount_type'+id).remove(1);
	    }
		
		if(document.getElementById("applicable_type"+id).value == "C"){
			var cell2select = document.createElement("select");
			cell2select.setAttribute("name", "applicable_to_id");
			cell2select.setAttribute("id", "applicable_to_id"+id);
			cell2select.setAttribute("class", "dropdown");
			cell2select.setAttribute("style", "width: 250px");
			cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", "");
			cell2selectoption.innerHTML = "--Select--";
			
			cell2select.appendChild(cell2selectoption);
			
			
			for(var k=0; k<chargeHeadList.length; k++){
	    	
				var list = chargeHeadList[k];
				var cell2selectoption = document.createElement("option");
				cell2selectoption.setAttribute("value", list.chargehead_id);
				cell2selectoption.text = list.chargehead_name;
				
				cell2select.appendChild(cell2selectoption);
				
			}
			prevSel.parentNode.replaceChild(cell2select,prevSel);
			var applicable_to_name_tmp = document.getElementById('applicable_to_name'+id);
			if( applicable_to_name_tmp != null ){
				applicable_to_name_tmp.remove();
			}
			document.getElementById('discount_type'+id).disabled= false;
			//document.getElementById('discount_type'+id).value="P";
			document.getElementById('discount_type'+id).remove(1);
	    }
		
		if(document.getElementById("applicable_type"+id).value == " "){
			var cell2select = document.createElement("select");
			cell2select.setAttribute("name", "applicable_to_id");
			cell2select.setAttribute("id", "applicable_to_id"+id);
			cell2select.setAttribute("class", "dropdown");
			cell2select.setAttribute("style", "width: 250px");
			cell2select.setAttribute("onchange", "checkDuplicateItem("+id+")");
			var cell2selectoption = document.createElement("option");
			cell2selectoption.setAttribute("value", "");
			cell2selectoption.innerHTML = "--Select--";
			cell2select.appendChild(cell2selectoption);
			
			prevSel.parentNode.replaceChild(cell2select,prevSel);
			var applicable_to_name_tmp = document.getElementById('applicable_to_name'+id)
			if(applicable_to_name_tmp != null ) {
				applicable_to_name_tmp.remove();
			}
			document.getElementById('discount_type'+id).disabled= false;
			document.getElementById('discount_type'+id).remove(1);
		}
	
 }


function checkDuplicateItem(id){
	
	var applicable_to_id = document.getElementsByName("applicable_to_id");
	var applicable_type_value = document.getElementById('applicable_type'+id).value;
 	for(var i=0; i<applicable_to_id.length;i++) {
 		if(i !=id){
 			if(applicable_type_value != 'I'){
	 			if(document.getElementById('applicable_to_id'+i).value != "" &&  document.getElementById('applicable_type'+i).value == applicable_type_value){
			 		if(document.getElementById('applicable_to_id'+i).value == document.getElementById('applicable_to_id'+id).value){
		     	    	alert("Please  select another value in Applicable To label.");
		     	    	document.getElementById('applicable_to_id'+id).value = "";
		     	    	break;
			 		}
	 			}
 			}else{
 				if(document.getElementById('applicable_to_id'+id).value == ''){
 					break;
 				}
 				if(document.getElementById('applicable_to_id'+i).value != "" &&  document.getElementById('applicable_type'+i).value == applicable_type_value){
 					if(document.getElementById('applicable_to_id'+i).value == document.getElementById('applicable_to_id'+id).value){
		     	    	alert("Please  select another value in Applicable To label.");
		     	    	document.getElementById('applicable_to_id'+id).value = "";
		     	    	document.getElementById('applicable_to_name'+id).value = "";
		     	    	break;
			 		}	
	 			}
 			}
 		}
 	}	
}


function checkDuplicatePriority(id){
	if(document.getElementById('priority'+id).value == 0){
			document.getElementById('priority'+id).value = "";
		}
	var priority = document.getElementsByName("priority");
 	for(var i=0; i<priority.length;i++) {
 		if(i !=id){
	 		if(document.getElementById('priority'+i).value != ""){
	 			if(document.getElementById('priority'+i).value == document.getElementById('priority'+id).value){
	     	    	alert("Please  select another value in priority label.");
	     	    	document.getElementById('priority'+id).value = "";
	     	    	break;
		 		}
			}
 		}
 	}
}

function validateForm(){
	
	  var discount_plan_name = document.getElementById("discount_plan_name").value.trim();
	   
	   if(!discount_plan_name){
		  alert("Please enter discount plan name");
	  	  document.getElementById("discount_plan_name").focus();
		  return false;
	   }
	   
	  
    for(var k=0; k<discountPlanbean.length; k++){
    	
    	var list = discountPlanbean[k];
    	if(document.getElementById('discount_plan_name1').value != list.discount_plan_name){
    		if(document.getElementById('discount_plan_name').value == list.discount_plan_name){
    			alert("Discount plan name already exist, please choose another name.");
    			document.getElementById('discount_plan_name').focus();
    			 return false;
    		}
    	}
    }
		 
	  
	  var from_date = document.getElementById('validity_start').value.trim();
      var to_date = document.getElementById('validity_end').value.trim();
      var from_date_array = from_date.split('-'); 
      var to_date_array = to_date.split('-');
      
      var Date1 = new Date();
      Date1.setFullYear(from_date_array[2],from_date_array[1]-1,from_date_array[0]);
      var Date2 = new Date();
      Date2.setFullYear(to_date_array[2],to_date_array[1]-1,to_date_array[0]);
      
      if(to_date){
    	  if(!from_date){
        	  alert("Valid From date is required. ");
    		  document.getElementById('validity_start').focus();
              return false; 
          }
      }
      if(from_date && to_date){
	      if (Date1 > Date2)
	      {
	    	  alert("To date cannot be less than from date ");
			  document.getElementById('validity_end').focus();
	          return false;
	      }
      }
      
      
    var discount_value = document.getElementsByName("discount_value");
  	for(var i=0; i<discount_value.length;i++) {
  		if(document.getElementById('deleted'+i).value =="false"){
	      	 if (isNaN(document.getElementById('discount_value'+i).value.trim())) {
	           	 alert("Please enter only numbers in Discount label. ");
	          	 document.getElementById('discount_value'+i).focus();
	          	 return false;
	         }
	      	 if(document.getElementById('discount_value'+i).value.trim() < 0){
	      		 alert("Negative value is not allowed.");
	          	 document.getElementById('discount_value'+i).focus();
	          	 return false;
	      	 }
	      	var discountValue = parseFloat(document.getElementById('discount_value'+i).value.trim());
	      	if(document.getElementById('discount_type'+i).value.trim() == 'P'){
		      	 if(discountValue > 100){
		      		 alert("Discount value cannot be greater than  100 ");
		      		 document.getElementById('discount_value'+i).focus();
		             return false;
		      	 }
	      	}
  		}
  	}
  	
  	var priority = document.getElementsByName("priority");
  	var pattern = /^[0-9]*$/;
  	for(var i=0; i<priority.length;i++) {
     
      	/* if (isNaN(document.getElementById('priority'+i).value.trim())) {
           	 alert("Please enter only numbers in priority label. ");
          	 document.getElementById('priority'+i).focus();
          	 return false;
          }*/
      	 if(!pattern.test(document.getElementById('priority'+i).value.trim())){
	      	alert("Please enter only numbers in priority label.");
	     	document.getElementById('priority'+i).focus();
	     	return false;
  		 }
  	}
  	
  	var countRows = 0;
  	var applicable_type = document.getElementsByName("applicable_type");
   	for(var i=0; i<applicable_type.length;i++) {
   		if(document.getElementById('deleted'+i).value =="false"){
       		 if (!document.getElementById('applicable_type'+i).value.trim()) {
            		 alert("Please select anyone value in Applicable Category label "+(i+1));
           		     document.getElementById('applicable_type'+i).focus();
           		     return false;
          	 }
       		countRows++;
   		}
   	} 
   	if(countRows == 0){
   		alert("Please add atleast one rule to create a discount plan.. ");
		return false;
   	}
   	
   	var applicable_to_id = document.getElementsByName("applicable_to_id");
   	for(var i=0; i<applicable_to_id.length;i++) {
   		if(document.getElementById('deleted'+i).value =="false"){
       	 	if (!document.getElementById('applicable_to_id'+i).value.trim()) {
            	 	alert("Please select anyone value in Applicable To label "+(i+1));
           		    document.getElementById('applicable_to_id'+i).focus();
           		    return false;
          	 }
   		}
   	} 
   	
   	var priority = document.getElementsByName("priority");
   	for(var i=0; i<priority.length;i++) {
   		if(document.getElementById('deleted'+i).value =="false"){
       	 	 if (!document.getElementById('priority'+i).value.trim()) {
            		 alert("Please enter value in Priority label "+(i+1));
           		     document.getElementById('priority'+i).focus();
           		     return false;
          	 }
       	 }
   	} 
   	
   
    document.DiscountPlanMasterForm.submit();
	return true; 
}

