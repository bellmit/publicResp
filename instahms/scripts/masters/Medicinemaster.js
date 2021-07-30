 function makeingDec(objValue,obj){
    if (objValue!= '') {
		document.getElementById(obj.id).value = parseFloat(objValue).toFixed(2);
	}
 }


var oAutoComp3;
/**
*  this method contains genericNames AutoComplete
*
*/
function initGenericAutoComplete(jGenericNames) {
    YAHOO.example.ACJSAddArray = new function() {
	var dataSource = new YAHOO.widget.DS_JSArray(jGenericNames);
	oAutoComp3 = new YAHOO.widget.AutoComplete('generic_name', 'generic_dropdown', dataSource);
	oAutoComp3.maxResultsDisplayed = 20;
	oAutoComp3.allowBrowserAutocomplete = false;
	oAutoComp3.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp3.typeAhead = false;
	oAutoComp3.useShadow = false;
	oAutoComp3.minQueryLength = 0;
  }

}

var oAutoComp4;
/**
*  this method contains manufacturerNames AutoComplete
*
*/
function initManfAutoComplete(jManfNames) {

	var manufNames = [];
	var j = 0;
	for (var i=0; i<jManfNames.length; i++ ) {
		 if (jManfNames[i].STATUS == 'A' && jManfNames[i].PHARMACY == 't') manufNames[j++] = jManfNames[i].MANF_NAME;
	}

    YAHOO.example.ACJSAddArray = new function() {
	var dataSource = new YAHOO.widget.DS_JSArray(manufNames);
	oAutoComp4 = new YAHOO.widget.AutoComplete('manf_name', 'manf_dropdown', dataSource);
	oAutoComp4.maxResultsDisplayed = 20;
	oAutoComp4.allowBrowserAutocomplete = false;
	oAutoComp4.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp4.typeAhead = false;
	oAutoComp4.useShadow = false;
	oAutoComp4.minQueryLength = 0;
	oAutoComp4.unmatchedItemSelectEvent.subscribe(function(){onSelectManf(jManfNames);});
  }
}

function onSelectManf(jManfNames) {
	var	manfName = document.masterForm.manf_name.value;
	for (var i=0; i<jManfNames.length; i++ ) {
	     if ( (manfName == jManfNames[i].MANF_NAME) && (jManfNames[i].PHARMACY == 'f' || jManfNames[i].STATUS != 'A' ) ) {
	     	alert("Manufacturer: '"+manfName+"' exists in master \n if u want to show in list\n pls update Manufacturer Master");
	     	document.masterForm.manf_name.value = '';
	     	break;
	     }
	}
}

var oAutoComp5;
/**
*  this method contains CategoryNames AutoComplete
*
*/
var categoryNames = [];
function initCategoryAutoComplete(jCategoryNames) {
	var j = 0;
	for (var i=0; i<jCategoryNames.length; i++ ) {
		 if (jCategoryNames[i].STATUS == 'A' ) categoryNames[j++] = jCategoryNames[i].CATEGORY_NAME;
	}

    YAHOO.example.ACJSAddArray = new function() {
	var dataSource = new YAHOO.widget.DS_JSArray(categoryNames);
	oAutoComp5 = new YAHOO.widget.AutoComplete('category_name', 'category_dropdown', dataSource);
	oAutoComp5.maxResultsDisplayed = 20;
	oAutoComp5.allowBrowserAutocomplete = false;
	oAutoComp5.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp5.typeAhead = false;
	oAutoComp5.useShadow = false;
	oAutoComp5.minQueryLength = 0;
	oAutoComp5.textboxBlurEvent.subscribe(onSelectCategory);
  }

}
function onSelectCategory() {
	var	catName = document.masterForm.category_name.value;
	var match = false;
	for (var i=0; i<jCategoryNames.length; i++ ) {
	     if ( catName == jCategoryNames[i].CATEGORY_NAME) {
	     	match = true;
	     	if (contains(catName)) {
		     	if (jCategoryNames[i].CLAIMABLE == 't') document.forms[0].claimable.checked = true;
		     	else document.forms[0].claimable.checked = false;
		       	document.forms[0].claimable.disabled = true;
	     		break;
		     } else {
		     	alert("Category: '"+catName+"' exists in master \n if u want to show in list\n pls update Category Master");
	     		document.masterForm.category_name.value = '';
	     		break;
		     }
	     }
	}
	if (!match) {
		document.forms[0].claimable.disabled = false;
		document.forms[0].claimable.checked = true;
	}
}
function contains(element) {
	for (var i = 0; i < categoryNames.length; i++) {
		if (categoryNames[i] == element) {
			return true;
		}
	}
return false;
}

function funcClose(){
	document.forms[0].action = "medicine.do?_method=viewMedicines";
	document.forms[0].submit();
	return true;
}

function deleteItem(checkBox, rowId) {
	var itemListTable = document.getElementById("medtabel");
	var row = itemListTable.rows[rowId];
	var deletedInput = document.getElementById('hdeleted'+rowId);
	if (checkBox.checked) {
		deletedInput.value = 'true';
		row.className = "delete";
	} else {
		deletedInput.value = 'false';
		row.className = "";
	}
	//resetTotals();
}

function Add() {
	if (validate() == true) {
		var itemListTable = document.getElementById("medtabel");
	    var numRows = itemListTable.rows.length-1;
	    var id = numRows;
		var row = itemListTable.insertRow(id);

	    var minlevel = document.masterForm.min_level.value;
	    var maxlevel = document.masterForm.max_level.value;
	    var dangerlevel = document.masterForm.danger_level.value;
	    var reorderlevel = document.masterForm.reorder_level.value;
	    var deptid = document.masterForm.dept_id.value;
	    var deptname = document.masterForm.dept_id.options[document.masterForm.dept_id.selectedIndex].text;

		var cell;
		cell = row.insertCell(-1);
		cell.innerHTML='<input type="checkbox" name = "delItem" id="delItem'+id+'" ' + 'onclick="deleteItem(this, '+id+')">' +
		              '<input type="hidden" name="hdeleted" id="hdeleted'+id+'"  value="false">';

	    cell = row.insertCell(-1);
	 	cell.innerHTML = '<label>'+deptname+'</label>' +
		        '<input type="hidden" name="hdepartment" id="hdepartment'+id+'" value="'+deptid+'">'+
		        '<input type="hidden" name="hdepartmentname" id="hdepartmentname'+id+'" value="'+deptname+'">'+
		        '<input type="hidden" name="deptoldrnew" id="deptoldrnew'+id+'" value="new">'+
				'<input type="hidden" name="hmedicineId" id="hmedicineId'+id+'" value="'+medID+'">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="number"   name="hiddenMinLevel" id="hiddenMinLevel'+id+'" value="'+minlevel+'"  maxlength="8">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="number"   name="hiddenMaxLevel" id="hiddenMaxLevel'+id+'" value="'+maxlevel+'"  maxlength="8">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="number"   name="hiddenDangerLevel" id="hiddenDangerLevel'+id+'" value="'+dangerlevel+'"  maxlength="8">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="number"   name="hiddenReorderLevel" id="hiddenReorderLevel'+id+'" value="'+reorderlevel+'"  maxlength="8">';

	    document.masterForm.min_level.value = '';
	    document.masterForm.max_level.value = '';
	    document.masterForm.danger_level.value = '';
	    document.masterForm.reorder_level.value = '';
	    document.masterForm.dept_id.options.selectedIndex = 0;
    }else {
    	return false;
    }

}

function validate(){
	if(document.masterForm.dept_id.options.selectedIndex==0){
	   alert("Select the Store");
	   document.masterForm.dept_id.focus();
	   return false;
	}
    if(document.masterForm.min_level.value==""){
	   alert("Enter the min level");
	   document.masterForm.min_level.focus();
	   return false;
	 }
	 if(document.masterForm.max_level.value==""){
	   alert("Enter the max level");
	   document.masterForm.max_level.focus();
	   return false;
	 }
	 if(document.masterForm.danger_level.value==""){
	   alert("Enter the danger level");
	   document.masterForm.danger_level.focus();
	   return false;
	 }
	 if(document.masterForm.reorder_level.value==""){
	   alert("Enter the reorder level");
	   document.masterForm.reorder_level.focus();
	   return false;
	 }
  	 if(parseInt(document.masterForm.min_level.value,10)>parseInt(document.masterForm.max_level.value,10)){
	   alert("MinLevel should be less than maxLevel");
	   document.masterForm.min_level.focus();
	   return false;
	 }
	 if(parseFloat(document.masterForm.danger_level.value,10) > parseFloat(document.masterForm.min_level.value,10)){
		 alert("Danger level should be less than min level");
		 document.masterForm.danger_level.select();
		 document.masterForm.danger_level.focus();
	     return false;
	 }
	 if(parseFloat(document.masterForm.reorder_level.value,10) > parseFloat(document.masterForm.min_level.value,10)){
		 alert("Reorder level should be less than min level");
		 document.masterForm.reorder_level.focus();
	     return false;
	 }
     if(parseInt(document.masterForm.danger_level.value,10)>parseInt(document.masterForm.reorder_level.value,10)){
	   alert("DangerLevel should be less than reorder level");
	   document.masterForm.danger_level.focus();
	   return false;
	 }
	var itemListTable = document.getElementById("medtabel");
    var numRows = itemListTable.rows.length-1;
    id = numRows;
   	if (numRows > 1) {
		for (var i=1;i<numRows;i++) {
				if (document.masterForm.dept_id.value == (document.getElementById('hdepartment'+i).value)) {
					alert(document.getElementById('hdepartmentname'+i).value+" already exists, update if u want");
					return false;
				}
		}
	}
	return true;
}

function addValues() {
    if (check() == true) {
	var itemListTable = document.getElementById("medtabel");
    var numRows = itemListTable.rows.length-1;
    id = numRows;
   	if (numRows > 1) {
		for (var i=1;i<numRows;i++) {
			if (!(document.getElementById("delItem"+i).checked)) {
			for (var l=1;l<numRows;l++) {
				if (l!=i) {
					if (!(document.getElementById("delItem"+l).checked)) {
			    			if ((document.getElementById('hdepartment'+i).value) == document.getElementById('hdepartment'+l).value) {
					    		alert(document.getElementById('hdepartmentname'+l).value+" has duplicate entries");
			                    document.getElementById('delItem'+l).checked = true;
			                    deleteItem(document.getElementById('delItem'+l),l);
					    		return false;
				    	    }
			    	    }
		           }
	         }
           }
         }
     }
     if (document.masterForm.operation.value == 'update') {
	     if (document.masterForm.issue_base_unit.value != document.masterForm.originalPkgSize.value) {
		  	document.masterForm.changedPkgSize.value = 'yes';
		  	if(confirm("Do you want update the MRP, Cost Price and Selling price with the same ratio")){
		    	document.masterForm.updateMrpCp.value = 'yes';
		    }
		    else document.masterForm.updateMrpCp.value = 'no';
		 }
	 }
	 document.forms[0].action = "medicine.do?_method=saveMedicineDetails";
	 document.forms[0].submit();
     return true;
    } else return false;
  }

   function check(){
      document.getElementById("medicine_name").value=trimAll(document.getElementById("medicine_name").value);
	  var medicineName=document.getElementById("medicine_name").value;
      msg="Medicine Name ";
	  if(medicineName==""){
	      alert("Medicine Name Should Not Be Empty");
   		  document.getElementById("medicine_name").focus();
		  return false;
	  }

	   if(document.getElementById("medicine_short_name").value==""){
	      alert("Short Name Should Not Be Empty");
   		  document.getElementById("medicine_short_name").focus();
		  return false;
	  }

	  document.getElementById("therapatic_use").value=trimAll(document.getElementById("therapatic_use").value);
	  var therapaticUse=document.getElementById("therapatic_use").value;
	  if(therapaticUse.length>60){
		alert("Therapatic name length should be less than 60 characters");
		document.getElementById("therapatic_use").focus();
		return false;
	  }

   	  if(trimAll(document.getElementById("manf_name").value)==""){
		alert("Select the manufacturer name");
		document.masterForm.manf_name.focus();
		return false;
	  }

	  if (document.masterForm.issue_base_unit.value == "") {
			alert("Enter Package Size");
			document.masterForm.issue_base_unit.focus();
			return false;
	  }

	  if (document.masterForm.issue_base_unit.value == 0) {
			alert("Package Unit Cannot be zero");
			document.masterForm.issue_base_unit.focus();
			return false;
	  }

	  if(trimAll(document.getElementById("category_name").value)==""){
		document.masterForm.category_name.value = 'general';
	  }


	  document.getElementById("composition").value = trimAll(document.getElementById("composition").value);
	  return true;
  }

  function checkduplicate(){
		var medicineId=medID;
		for(var i=0;i<chkMedList.length;i++){
			item = chkMedList[i];
			if(medicineId!=item.MEDICINE_ID){
			    if (trimAll(document.masterForm.medicine_name.value)==item.MEDICINE_NAME) {
			    	alert(item.MEDICINE_NAME+" already exists please enter other name");
			    	document.masterForm.medicine_name.value='';
			    	document.getElementById('medicine_name').focus();
			    	return false;
			    }
		     }
		}
		document.masterForm.medicine_short_name.value = document.masterForm.medicine_name.value;
}

function storeDup(){
	var itemListTable = document.getElementById("medtabel");
    var numRows = itemListTable.rows.length-1;
    id = numRows;
   	if (numRows > 1) {
		for (var i=1;i<numRows;i++) {
				if (document.masterForm.dept_id.value == (document.getElementById('hdepartment'+i).value)) {
					alert(document.getElementById('hdepartmentname'+i).value+" already exists, update if you want");
					document.masterForm.dept_id.options.selectedIndex = 0;
					return false;
				}
		}
	}
}

function initItemGroupDialog(){
	itemDialog = new YAHOO.widget.Dialog("itemDialog",
			{
			width:"600px",
			context :["btnAddItems", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true,
			buttons : [ { text:"Save", handler:Add, isDefault:true },
							{ text:"Cancel", handler:handleCancel } ]
			});
itemDialog.render();
}

function handleCancel(){
	this.cancel();
}

function getItemGroupDialog(id){
	button = document.getElementById("btnAddItems"+id);
	itemDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	itemDialog.show();
}






