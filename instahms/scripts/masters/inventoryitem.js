		function doClose() {
			document.forms[0].method.value = 'list';
			document.forms[0].submit();
			return true;
		}
      function focus(){
      	document.forms[0].item_name.focus();
      }


      function Add() {
	if (validate() == true) {
		var itemListTable = document.getElementById("medtabel");
	    var numRows = itemListTable.rows.length-1;
	    var id = numRows;
		var row = itemListTable.insertRow(id);

	    var minlevel = document.forms[0].min_level.value;
	    var maxlevel = document.forms[0].max_level.value;
	    var dangerlevel = document.forms[0].danger_level.value;
	    var reorderlevel = document.forms[0].reorder_level.value;
	    var deptid = document.forms[0].store_id.value;
	    var deptname = document.forms[0].store_id.options[document.forms[0].store_id.selectedIndex].text;
	    var itemId='';
	    if (document.getElementById("item_id") != null){
	    	itemId = document.getElementById("item_id").value ;
	    }

		var cell;
		cell = row.insertCell(-1);
		cell.innerHTML='<input type="checkbox" name = "delItem" id="delItem'+id+'" ' + 'onclick="deleteItem(this, '+id+')">' +
		              '<input type="hidden" name="hdeleted" id="hdeleted'+id+'"  value="false">';

	    cell = row.insertCell(-1);
	 	cell.innerHTML = '<label>'+deptname+'</label>' +
		        '<input type="hidden" name="hdepartment" id="hdepartment'+id+'" value="'+deptid+'">'+
		        '<input type="hidden" name="hdepartmentname" id="hdepartmentname'+id+'" value="'+deptname+'">'+
		        '<input type="hidden" name="deptoldrnew" id="deptoldrnew'+id+'" value="new">'+
				'<input type="hidden" name="hmedicineId" id="hmedicineId'+id+'" value="'+itemId+'">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="num"   name="hiddenMinLevel" id="hiddenMinLevel'+id+'" value="'+minlevel+'"  maxlength="8">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="num"   name="hiddenMaxLevel" id="hiddenMaxLevel'+id+'" value="'+maxlevel+'"  maxlength="8">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="num"   name="hiddenDangerLevel" id="hiddenDangerLevel'+id+'" value="'+dangerlevel+'"  maxlength="8">';

	    cell = row.insertCell(-1);
	    cell.setAttribute("align","center");
		cell.innerHTML = '<input type="text" class="num"   name="hiddenReorderLevel" id="hiddenReorderLevel'+id+'" value="'+reorderlevel+'"  maxlength="8">';

	    document.forms[0].min_level.value = '';
	    document.forms[0].max_level.value = '';
	    document.forms[0].danger_level.value = '';
	    document.forms[0].reorder_level.value = '';
	    document.forms[0].store_id.options.selectedIndex = 0;
    }else {
    	return false;
    }

}

function validate(){
	if(document.forms[0].store_id.options.selectedIndex==0){
	   alert("Select the Store");
	   document.forms[0].store_id.focus();
	   return false;
	}
    if(document.forms[0].min_level.value==""){
	   alert("Enter the min level");
	   document.forms[0].min_level.focus();
	   return false;
	 }
	 if(document.forms[0].max_level.value==""){
	   alert("Enter the max level");
	   document.forms[0].max_level.focus();
	   return false;
	 }
	 if(document.forms[0].danger_level.value==""){
	   alert("Enter the danger level");
	   document.forms[0].danger_level.focus();
	   return false;
	 }
	 if(document.forms[0].reorder_level.value==""){
	   alert("Enter the reorder level");
	   document.forms[0].reorder_level.focus();
	   return false;
	 }
  	 if(parseInt(document.forms[0].min_level.value,10)>parseInt(document.forms[0].max_level.value,10)){
	   alert("MinLevel should be less than maxLevel");
	   document.forms[0].min_level.focus();
	   return false;
	 }
	 if(parseFloat(document.forms[0].danger_level.value,10) > parseFloat(document.forms[0].min_level.value,10)){
		 alert("Danger level should be less than min level");
		 document.forms[0].danger_level.select();
		 document.forms[0].danger_level.focus();
	     return false;
	 }
	 if(parseFloat(document.forms[0].reorder_level.value,10) > parseFloat(document.forms[0].min_level.value,10)){
		 alert("Reorder level should be less than min level");
		 document.forms[0].reorder_level.focus();
	     return false;
	 }
     if(parseInt(document.forms[0].danger_level.value,10)>parseInt(document.forms[0].reorder_level.value,10)){
	   alert("DangerLevel should be less than reorder level");
	   document.forms[0].danger_level.focus();
	   return false;
	 }
	var itemListTable = document.getElementById("medtabel");
    var numRows = itemListTable.rows.length-1;
    id = numRows;
   	if (numRows > 1) {
		for (var i=1;i<numRows;i++) {
				if (document.forms[0].store_id.value == (document.getElementById('hdepartment'+i).value)) {
					alert(document.getElementById('hdepartmentname'+i).value+" already exists, update if u want");
					return false;
				}
		}
	}
	return true;
}

function deleteItem(checkBox, rowId) {
	var itemListTable = document.getElementById("medtabel");
	var row = itemListTable.rows[rowId];
	var deletedInput = document.getElementById('hdeleted'+rowId);
	if (checkBox.checked) {
		deletedInput.value = 'true';
		row.className = "deleted";
	} else {
		deletedInput.value = 'false';
		row.className = "";
	}
}

function setPackSize(){
	for ( i=0; i<identList.length; i++){
		var item = identList[i];

		if (document.forms[0].category_id.value == item["CATEGORY_ID"]){
			 if (item["IDENTIFICATION"] == 'S'){
				document.forms[0].pkg_size.value = '1';
				document.forms[0].pkg_size.readOnly = true ;
			}else{
				document.forms[0].pkg_size.readOnly = false ;

			}
	}
	}

}


function automanf(){
	var manfCode = document.forms[0].manf_code.value;
	for (i=0; i<manfList.length; i++){
		var item = manfList[i];
		if (manfCode == item["manf_code"]){
			document.forms[0].manf_name.value = item["manf_name"];
		}
	}

	YAHOO.example.manfArray = [];
	YAHOO.example.manfArray.length = 0;
	var j=0;
	for ( i=0; i<manfList.length; i++){

		if (manfList[i].status == 'A' && manfList[i].inventory ) {
			var item = manfList[i];
			YAHOO.example.manfArray[j++] = item["manf_name"];
			YAHOO.example.manfArray.length = YAHOO.example.manfArray.length +1;
		}
	}

	YAHOO.example.ACJSArray = new function(){
	datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.manfArray);
	this.oManfAuto = new YAHOO.widget.AutoComplete('manf_name','manfcontainer',datasource);
	this.oManfAuto.prehighlightClassname = "yui-ac-prehighlight";
	this.oManfAuto.minQueryLength = 0;
	this.oManfAuto.maxResultsDisplayed = 20;
	this.oManfAuto.typeAhead = false;
	this.oManfAuto.useShadow = false;
	this.oManfAuto.allowBrowserAutocomplete = false;
	this.oManfAuto.autoHighlight = false;
	this.oManfAuto.forceSelection = false;
	this.oManfAuto.textboxFocusEvent.subscribe(function(){
		var sInputValue = YAHOO.util.Dom.get("manf_name").value;
		if (sInputValue.length === 0){
			var oSelf = this;
			setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
		}
	});
	this.oManfAuto.textboxBlurEvent.subscribe(setManfCode);
	this.oManfAuto.unmatchedItemSelectEvent.subscribe(checkForInactiveManf);
	}
}

function setManfCode(){

	var manfName = document.forms[0].manf_name.value;
	document.forms[0].manf_code.value = '';
	for (i=0; i<manfList.length; i++){
		var item = manfList[i];
		if (manfName == item["manf_name"]){
			document.forms[0].manf_code.value = item["manf_code"];
		}
	}
}

function checkForInactiveManf() {
	var	manfName = document.forms[0].manf_name.value;
	for (var i=0; i<manfList.length; i++ ) {
	     if ( (manfName == manfList[i].manf_name) && (manfList[i].inventory == 'f' || manfList[i].status != 'A' ) ) {
	     	alert("Manufacturer: '"+manfName+"' exists in master \n if u want to show in list\n pls update Manufacturer Master");
	     	document.forms[0].manf_name.value = '';
	     	document.forms[0].manf_code.value = '';
	     	break;
	     }
	}
}


function validateUpdateMRP (pkg_size) {
	if(pkg_size.value == 0){
		alert("Package size can not be zero");
		pkg_size.value='';
		pkg_size.focus();
		return false;
	}
	if (opeartion == 'show') {
	     if (document.forms[0].pkg_size.value != document.forms[0].originalPkgSize.value) {
		  	document.forms[0].changedPkgSize.value = 'yes';
		  	if(confirm("do you want update the MRP, Cost Price and Selling price with the same ratio")){
		    	document.forms[0].updateMrpCp.value = 'yes';
		    }
		    else document.forms[0].updateMrpCp.value = 'no';
		 }
	 }
	 return true;
}
function makeingDec(objValue,obj){
	if (objValue == '') objValue = 0;
    if (isAmount(objValue)) {
		document.getElementById(obj.name).value = parseFloat(objValue).toFixed(2);
	} else document.getElementById(obj.name).value = 0.00;
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
function init () {
	automanf();
	initItemGroupDialog();
	focus();
}