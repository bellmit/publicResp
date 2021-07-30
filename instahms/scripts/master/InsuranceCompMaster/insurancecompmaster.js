function validateForm() {
	if (document.insuranceCompMasterForm.insurance_co_name.value=="") {
		alert("Insurance company name is required");
		document.insuranceCompMasterForm.insurance_co_name.focus();
		return false;
	}
	return true;
}

function init() {
	initInsCompHaCodeDialog();
}

var insCompHaCodeDialog;
var haCodeAction = '';
var haCodeEditedrowId = '';
function initInsCompHaCodeDialog() {
	insCompHaCodeDialog = new YAHOO.widget.Dialog("insCompHaCodeDialog",
		{
		width:"400px",
		context :["btnAddInsCompHaCode", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true,
		});

		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                              { fn:handleInsCompHaCodeCancel,
                                                scope:insCompHaCodeDialog,
                                                correctScope:true } );
		insCompHaCodeDialog.cfg.queueProperty("keylisteners", escKeyListener);

		insCompHaCodeDialog.render();
}

function getInsCompHaCodeDialog(id){
	button = document.getElementById("btnAddInsCompHaCode"+id);
	insCompHaCodeDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	haCodeAction = 'add';
	insCompHaCodeDialog.show();
}

function editInsCompHaCodeDialog(id) {
	button = document.getElementById("haEditBut"+id);
	insCompHaCodeDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	haCodeAction = 'edit';
	haCodeEditedrowId = id;
	insCompHaCodeDialog.show();
	document.insuranceCompMasterForm.health_authority.value = document.getElementById('healthAuth'+id).textContent;
	document.insuranceCompMasterForm.insurance_co_code.value = document.getElementById('h_ins_comp_code'+id).value;
}

function validateInsCompHaCodeDialog() {
	var healthAuth = document.insuranceCompMasterForm.health_authority;
	var insCompCode = document.insuranceCompMasterForm.insurance_co_code;

	if(empty(healthAuth.value)) {
		alert("health authority is required");
		healthAuth.focus();
		return false;
	}

	if(empty(insCompCode.value)) {
		alert("insurance company code is required");
		insCompCode.focus();
		return false;
	}

	return true;
}

function checkDuplicateInsCompHaCode() {
	var itemListTable = document.getElementById('insCompHaCodeTable');
	var numRows = itemListTable.rows.length-1;
	var healthAuth = document.getElementsByName('h_health_authority');
	var insCompCodes = document.getElementsByName('h_ins_comp_code');
	var itemDeleted = document.getElementsByName('h_ha_deleted');
	var numRows = itemListTable.rows.length-2;

	var dHealthAuth = document.insuranceCompMasterForm.health_authority.value;
	var dincCompCode = document.insuranceCompMasterForm.insurance_co_code.value;

	for(var i=0;i<numRows;i++) {
		if(haCodeAction == 'edit' && haCodeEditedrowId == i+1) continue;
		if(itemDeleted[i].value == 'false') {
			if(healthAuth[i].value == dHealthAuth) {
				alert("dupliacte health authority is not allowed");
				return false;
			}
		}
	}
	return true;
}

function allowForUndelete(index) {
	var itemListTable = document.getElementById('insCompHaCodeTable');
	var numRows = itemListTable.rows.length-1;
	var healthAuth = document.getElementsByName('h_health_authority');
	var h_ins_comp_code = document.getElementsByName('h_ins_comp_code');
	var itemDeleted = document.getElementsByName('h_ha_deleted');
	var numRows = itemListTable.rows.length-2;

	var dHealthAuth = document.getElementById('h_health_authority'+index).value;
	var dInsCompCode = document.getElementById('h_ins_comp_code'+index).value;

	for(var i=0;i<numRows;i++) {
		if(index == i+1) {
			continue;
		}

		if(healthAuth[i].value == dHealthAuth) {
			alert("dupliacte health authority is not allowed");
			return false;
		}
	}

	return true;
}
function AddRecord() {
	if (validateInsCompHaCodeDialog() && checkDuplicateInsCompHaCode()) {
		if(haCodeAction == 'add') {
			var itemListTable = document.getElementById("insCompHaCodeTable");
		    var numRows = itemListTable.rows.length-1;
		    var id = numRows;
			var row = itemListTable.insertRow(id);

		    var healthAuth = document.insuranceCompMasterForm.health_authority.value;
		    var code = document.insuranceCompMasterForm.insurance_co_code.value;

		    var insCompId='';
		    if (document.insuranceCompMasterForm.insurance_co_id != null){
		    	tpaId = document.insuranceCompMasterForm.insurance_co_id.value ;
		    }

			var cell;
		    cell = row.insertCell(-1);
		    cell.setAttribute("class","forminfo");
		 	cell.innerHTML = '<label id="healthAuth'+id+'">'+healthAuth+'</label>' +
			        '<input type="hidden" name="h_health_authority" id="h_health_authority'+id+'" value="'+healthAuth+'">'+
			        '<input type="hidden" name="h_ha_insurance_co_code_id" id="h_ha_insurance_co_code_id'+id+'" value="">'+
			        '<input type="hidden" name="hacodeoldrnew" id="hacodeoldrnew'+id+'" value="new">'+
					'<input type="hidden" name="hInsCompId" id="hInsCompId'+id+'" value="'+insCompId+'">';

			cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
		    cell.setAttribute("style","width:300px;");
			cell.innerHTML = '<label id="h_ha_code'+id+'">'+code+'</label>'+
					'<input type="hidden" name="h_ins_comp_code" id="h_ins_comp_code'+id+'" value="'+code+'">';

			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "haDelItem" id="haDelItem'+id+'" ' + 'onclick="deleteInsCompHaCodeItem(this, '+id+')" src="'+cpath+'/icons/Delete.png">' +
			              '<input type="hidden" name="h_ha_deleted" id="h_ha_deleted'+id+'"  value="false">';

			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "haEditBut" id="haEditBut'+id+'" ' + 'onclick="editInsCompHaCodeDialog('+id+')" src="'+cpath+'/icons/Edit.png">';

		    document.insuranceCompMasterForm.health_authority.options.selectedIndex = 0;
    		document.insuranceCompMasterForm.insurance_co_code.value = '';
	   	}
	    if(haCodeAction == 'edit') {
	    	if(checkDuplicateInsCompHaCode()) {
		    	document.getElementById('healthAuth'+haCodeEditedrowId).textContent = document.insuranceCompMasterForm.health_authority.value;
		    	document.getElementById('h_ha_code'+haCodeEditedrowId).textContent =  document.insuranceCompMasterForm.insurance_co_code.value;
		    	document.getElementById('h_health_authority'+haCodeEditedrowId).value = document.insuranceCompMasterForm.health_authority.value;
		    	document.getElementById('h_ins_comp_code'+haCodeEditedrowId).value =  document.insuranceCompMasterForm.insurance_co_code.value;

		    	document.insuranceCompMasterForm.health_authority.options.selectedIndex = 0;
	    		document.insuranceCompMasterForm.insurance_co_code.value = "";
	    	}
	    }
	  }else {
	  	return false;
	  }

}

function handleInsCompHaCodeCancel(){
	document.insuranceCompMasterForm.health_authority.options.selectedIndex = 0;
    document.insuranceCompMasterForm.insurance_co_code.value = "";
	insCompHaCodeDialog.cancel();
}

function deleteInsCompHaCodeItem(checkBox, rowId) {
	var itemListTable = document.getElementById("insCompHaCodeTable");
	var row = itemListTable.rows[rowId];
	var img = document.createElement("img");
	img.setAttribute("name", "haEditBut");
	img.setAttribute("id", "haEditBut"+rowId);
	img.setAttribute("style", "cursor:pointer;");
	img.setAttribute("src", cpath + "/icons/Edit1.png");
	img.setAttribute("class", "button");

	var deletedInput = document.getElementById('h_ha_deleted'+rowId);
		if (deletedInput.value == 'false') {
			document.getElementById('haDelItem'+rowId).src = cpath+"/icons/Deleted.png";
			document.getElementById('haEditBut'+rowId).src = cpath+"/icons/Edit1.png";
			for (var i=row.cells[3].childNodes.length-1; i>=0; i--) {
				row.cells[3].removeChild(row.cells[3].childNodes[i]);
			}
			row.cells[3].appendChild(img);
			deletedInput.value = 'true';
			row.className = "deleted";
		} else {
			if(allowForUndelete(rowId)) {
				deletedInput.value = 'false';
				document.getElementById('haDelItem'+rowId).src = cpath+"/icons/Delete.png";
				img.setAttribute("onclick", "editInsCompHaCodeDialog('"+rowId+"')");
				for (var i=row.cells[3].childNodes.length-1; i>=0; i--) {
					row.cells[3].removeChild(row.cells[3].childNodes[i]);
				}
				row.cells[3].appendChild(img);
				document.getElementById('haEditBut'+rowId).src = cpath+"/icons/Edit.png";
				row.className = "";
			}
		}
}
