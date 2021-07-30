function focus(){
	document.tpaMasterForm.tpa_name.focus();
}

function populateCities(){
	var state = document.tpaMasterForm.state.value;
	document.tpaMasterForm.city.length = 1;
	var index = 1;
	for (var i=0; i<cityList.length; i++){
		var item = cityList[i];
		if (state == item['STATE_ID']){
			document.tpaMasterForm.city.length += 1;
			document.tpaMasterForm.city.options[index].text = item['CITY_NAME'];
			document.tpaMasterForm.city.options[index].value = item['CITY_ID'];
			if ('${param._method}' == 'show'){
				if (document.tpaMasterForm.testindg.value == item['CITY_ID']){
					document.tpaMasterForm.city.options[index].selected = true;
				}
			}
			index++;
		}
	}
}

function funSetClaimTemplateId(){
	var method = '${param._method}';


	if (document.tpaMasterForm.tpa_name.value == "") {
		alert("Tpa Name is required");
		document.tpaMasterForm.tpa_name.focus();
		return false;
	}
	if (document.tpaMasterForm.country.value == "") {
		alert("Country Name is required");
		document.tpaMasterForm.country.focus();
		return false;
	}
	if (document.tpaMasterForm.state.value == "") {
		alert("State Name is required");
		document.tpaMasterForm.state.focus();
		return false;
	}
	if (document.tpaMasterForm.city.value == "") {
		alert("City Name is required");
		document.tpaMasterForm.city.focus();
		return false;
	}
	if (document.tpaMasterForm.member_id_validation_status.value == "C" &&
		document.tpaMasterForm.child_dup_memb_id_validity_days.value == ""
	) {
		alert("Validaity count is required");
		document.tpaMasterForm.child_dup_memb_id_validity_days.focus();
		return false;
	}

	var claimTempId = document.getElementById("claimTemplateId").value;

	if(claimTempId == "" || claimTempId== 'null'){
		document.tpaMasterForm.claim_template_id.value=0;
		if(method == "add")
			document.tpaMasterForm.default_claim_template.value="Y";
		else
			document.tpaMasterForm.default_claim_template.value="N";
	}
	else if(claimTempId == "P" ){
		document.tpaMasterForm.claim_template_id.value=0;
		document.tpaMasterForm.default_claim_template.value="P";
	}
	else if(claimTempId == "R" ){
		document.tpaMasterForm.claim_template_id.value=0;
		document.tpaMasterForm.default_claim_template.value="R";
	}
	else if(claimTempId == "Y" ){
		document.tpaMasterForm.claim_template_id.value=0;
		document.tpaMasterForm.default_claim_template.value="Y";
	}else{
		document.tpaMasterForm.claim_template_id.value=claimTempId;
		document.tpaMasterForm.default_claim_template.value="N";
	}

	if(document.getElementById("sponsor_type_id").value==""){
		alert("Please select sponsor type value.");
		document.getElementById("sponsor_type_id").focus();
		return false;
	}

	var sysdate = new Date();
	var date=getFullDay(sysdate.getDate());
	var month=getFullMonth(sysdate.getMonth());
	var year=sysdate.getFullYear();
	var currentDate=date+"-"+month+"-"+year;

	if (document.getElementById("validityEnd_date").value != "") {
		if(getDateDiff(currentDate,document.getElementById("validityEnd_date").value)<0) {
			alert("Validity date should not be less than Currentdate");
			document.getElementById("validityEnd_date").value="";
			return false;
		}
	}

	document.tpaMasterForm.submit();
	return true;
}

function restrictMaxLength(obj) {
	if (obj.value.length > 249) {
		alert('Address cannot be more than 250 characters');
		obj.value = obj.value.substring(0, 249);
	}
}
var backupName = '';

function keepBackUp(){
	if(document.tpaMasterForm._method.value == 'update'){
		backupName = document.tpaMasterForm.tpa_name.value;
	}
}

var tpaHaCodeDialog;
var haCodeAction = '';
var haCodeEditedrowId = '';
function initTpaHaCodeDialog() {
	tpaHaCodeDialog = new YAHOO.widget.Dialog("tpaHaCodeDialog",
		{
		width:"650px",
		context :["btnAddTpaHaCode", "tr", "br"],
		visible:false,
		modal:true,
		constraintoviewport:true,
		});

		var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                              { fn:handleTpaHaCodeCancel,
                                                scope:tpaHaCodeDialog,
                                                correctScope:true } );
		tpaHaCodeDialog.cfg.queueProperty("keylisteners", escKeyListener);

		tpaHaCodeDialog.render();
}

function getTpaHaCodeDialog(id){
	button = document.getElementById("btnAddTpaHaCode"+id);
	tpaHaCodeDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	haCodeAction = 'add';
	tpaHaCodeDialog.show();
}

function editTpaHaCodeDialog(id) {
	button = document.getElementById("haEditBut"+id);
	tpaHaCodeDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	haCodeAction = 'edit';
	haCodeEditedrowId = id;
	tpaHaCodeDialog.show();
	document.tpaMasterForm.health_authority.value = document.getElementById('healthAuth'+id).textContent;
	document.tpaMasterForm.tpa_code.value = document.getElementById('h_tpa_code'+id).value;
	document.tpaMasterForm.enable_eligibility_authorization.value = document.getElementById('h_eligibility_authorization'+id).value;
	document.tpaMasterForm.enable_eligibility_auth_in_xml.value = document.getElementById('h_eligibility_authorization_in_xml'+id).value;
}

function validateTpaHaCodeDialog() {
	var healthAuth = document.tpaMasterForm.health_authority;
	var tpaCode = document.tpaMasterForm.tpa_code;

	if(empty(healthAuth.value)) {
		alert("health authority is required");
		healthAuth.focus();
		return false;
	}

	if(empty(tpaCode.value)) {
		alert("TPA/Sponsor code is required");
		tpaCode.focus();
		return false;
	}

	return true;
}

function checkDuplicateTpaHaCode() {
	var itemListTable = document.getElementById('tpaHaCodeTable');
	var numRows = itemListTable.rows.length-1;
	var healthAuth = document.getElementsByName('h_health_authority');
	var tpaCodes = document.getElementsByName('h_tpa_code');
	var itemDeleted = document.getElementsByName('h_ha_deleted');
	var numRows = itemListTable.rows.length-2;

	var dHealthAuth = document.tpaMasterForm.health_authority.value;
	var dTpaCode = document.tpaMasterForm.tpa_code.value;

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
	var itemListTable = document.getElementById('tpaHaCodeTable');
	var numRows = itemListTable.rows.length-1;
	var healthAuth = document.getElementsByName('h_health_authority');
	var tpaCodes = document.getElementsByName('h_tpa_code');
	var itemDeleted = document.getElementsByName('h_ha_deleted');
	var numRows = itemListTable.rows.length-2;

	var dHealthAuth = document.getElementById('h_health_authority'+index).value;
	var dTpaCode = document.getElementById('h_tpa_code'+index).value;

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

var eligibilityAuthLabelMap = {
	O : "Observation in claim xml",
	T : "Eligibility ID Payer",
	N : "Exclude in claim xml"
}

function AddRecord() {
	if (validateTpaHaCodeDialog() && checkDuplicateTpaHaCode()) {
		var eligibility_authorization = document.tpaMasterForm.enable_eligibility_authorization.value;
	    var eligibility_authorization_label =  eligibility_authorization === "true" ? "Enabled": "Disabled";
	    var eligibility_authorization_in_xml = document.tpaMasterForm.enable_eligibility_auth_in_xml.value;
		if(haCodeAction == 'add') {
			var itemListTable = document.getElementById("tpaHaCodeTable");
		    var numRows = itemListTable.rows.length-1;
		    var id = numRows;
			var row = itemListTable.insertRow(id);

		    var healthAuth = document.tpaMasterForm.health_authority.value;
		    var code = document.tpaMasterForm.tpa_code.value;

		    var tpaId='';
		    if (document.tpaMasterForm.tpa_id != null){
		    		tpaId = document.tpaMasterForm.tpa_id.value ;
		    }

			var cell;
		    cell = row.insertCell(-1);
		    cell.setAttribute("class","forminfo");
		 	cell.innerHTML = '<label id="healthAuth'+id+'">'+healthAuth+'</label>' +
			        '<input type="hidden" name="h_health_authority" id="h_health_authority'+id+'" value="'+healthAuth+'">'+
			        '<input type="hidden" name="h_ha_tpa_code_id" id="h_ha_tpa_code_id'+id+'" value="">'+
			        '<input type="hidden" name="hacodeoldrnew" id="hacodeoldrnew'+id+'" value="new">'+
					'<input type="hidden" name="htpaId" id="htpaId'+id+'" value="'+tpaId+'">'+
		 			'<input type="hidden" name="h_eligibility_authorization" id="h_eligibility_authorization'+id+'" value="'+eligibility_authorization+'">'+
		 			'<input type="hidden" name="h_eligibility_authorization_in_xml" id="h_eligibility_authorization_in_xml'+id+'" value="'+eligibility_authorization_in_xml+'">';

			cell = row.insertCell(-1);
		    cell.setAttribute("align","center");
		    cell.setAttribute("style","width:300px;");
			cell.innerHTML = '<label id="h_ha_code'+id+'">'+code+'</label>'+
					'<input type="hidden" name="h_tpa_code" id="h_tpa_code'+id+'" value="'+code+'">';

			cell = row.insertCell(-1);
			cell.innerHTML='<label id="h_ha_enable_eligibility_authorization'+id+'">'+eligibility_authorization_label+'</label>' +
	        			'<input type="hidden" name="h_eligibility_authorization" id="h_eligibility_authorization'+id+'" value="'+eligibility_authorization+'"/>'
	        	
    			cell = row.insertCell(-1);
			cell.innerHTML='<label id="h_ha_eligibility_authorization_in_xml'+id+'">'+eligibilityAuthLabelMap[eligibility_authorization_in_xml]+'</label>' +
    	        			'<input type="hidden" name="h_eligibility_authorization_in_xml" id="h_eligibility_authorization_in_xml'+id+'" value="'+eligibility_authorization+'"/>'
	        	
	        	cell = row.insertCell(-1);
			cell.innerHTML='<img name = "haDelItem" id="haDelItem'+id+'" ' + 'onclick="deleteTpaHaCodeItem(this, '+id+')" src="'+cpath+'/icons/Delete.png">' +
			              '<input type="hidden" name="h_ha_deleted" id="h_ha_deleted'+id+'"  value="false">';

			cell = row.insertCell(-1);
			cell.innerHTML='<img name = "haEditBut" id="haEditBut'+id+'" ' + 'onclick="editTpaHaCodeDialog('+id+')" src="'+cpath+'/icons/Edit.png">';

		    document.tpaMasterForm.health_authority.options.selectedIndex = 0;
    		document.tpaMasterForm.tpa_code.value = '';
	   	}
	    if(haCodeAction == 'edit') {
	    	if(checkDuplicateTpaHaCode()) {
		    	document.getElementById('healthAuth'+haCodeEditedrowId).textContent = document.tpaMasterForm.health_authority.value;
		    	document.getElementById('h_ha_code'+haCodeEditedrowId).textContent =  document.tpaMasterForm.tpa_code.value;
		    	document.getElementById('h_health_authority'+haCodeEditedrowId).value = document.tpaMasterForm.health_authority.value;
		    	document.getElementById('h_tpa_code'+haCodeEditedrowId).value = document.tpaMasterForm.tpa_code.value;
		    	document.getElementById('h_ha_enable_eligibility_authorization'+haCodeEditedrowId).textContent = eligibility_authorization_label;
		    	document.getElementById('h_eligibility_authorization'+haCodeEditedrowId).value = document.tpaMasterForm.enable_eligibility_authorization.value;
		    	document.getElementById('h_ha_eligibility_authorization_in_xml'+haCodeEditedrowId).textContent = eligibilityAuthLabelMap[document.tpaMasterForm.enable_eligibility_auth_in_xml.value];
		    	document.getElementById('h_eligibility_authorization_in_xml'+haCodeEditedrowId).value = document.tpaMasterForm.enable_eligibility_auth_in_xml.value;
		    	

		    	document.tpaMasterForm.health_authority.options.selectedIndex = 0;
	    		document.tpaMasterForm.tpa_code.value = "";
	    	}
	    }
	  }else {
	  	return false;
	  }

}

function handleTpaHaCodeCancel(){
	document.tpaMasterForm.health_authority.options.selectedIndex = 0;
    document.tpaMasterForm.tpa_code.value = "";
	tpaHaCodeDialog.cancel();
}

function deleteTpaHaCodeItem(checkBox, rowId) {
	var itemListTable = document.getElementById("tpaHaCodeTable");
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
			for (var i=row.cells[5].childNodes.length-1; i>=0; i--) {
				row.cells[5].removeChild(row.cells[5].childNodes[i]);
			}
			row.cells[5].appendChild(img);
			deletedInput.value = 'true';
			row.className = "deleted";
		} else {
			if(allowForUndelete(rowId)) {
				deletedInput.value = 'false';
				document.getElementById('haDelItem'+rowId).src = cpath+"/icons/Delete.png";
				img.setAttribute("onclick", "editTpaHaCodeDialog('"+rowId+"')");
				for (var i=row.cells[5].childNodes.length-1; i>=0; i--) {
					row.cells[5].removeChild(row.cells[5].childNodes[i]);
				}
				row.cells[5].appendChild(img);
				document.getElementById('haEditBut'+rowId).src = cpath+"/icons/Edit.png";
				row.className = "";
			}
		}
}

function validateMembershipId(e) {
	var c = getEventChar(e);
	return (isCharControl(c) || isCharAlpha(c) || isCharNumber(c) ||
			 (charDot == c) || (charHyphen == c)  || (charunderScore == c) ||  (charComma==c) || (charSpace==c));
}

function disableOrEnableLimitIncludesTax(){
	if(document.tpaMasterForm.claim_amount_includes_tax.value=='N'){
		document.tpaMasterForm.limit_includes_tax.disabled=true;
		document.tpaMasterForm.limit_includes_tax.value='N';
	}else{
		document.tpaMasterForm.limit_includes_tax.disabled=false;
	}
}

function disableOrEnableChildMemIdCount(){
	document.tpaMasterForm.child_dup_memb_id_validity_days.disabled = document.tpaMasterForm.member_id_validation_status.value !== 'C';
}