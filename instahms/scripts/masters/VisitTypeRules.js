


function setRows(visitType) {
	var baseItemTbl = document.getElementById("baseItemTbl"+visitType);
	var len = baseItemTbl.rows.length;
	var templateRow = baseItemTbl.rows[len-2];
   	var row = '';
   		row = templateRow.cloneNode(true);
   		row.style.display = '';
   		len = len-3;
   		row.id = visitType + len;
   	YAHOO.util.Dom.insertBefore(row, templateRow);

	var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width: 40px");
	var inp = document.createElement("img");
	inp.setAttribute("class", "imgDelete");
	inp.setAttribute("src", cpath + "/icons/Delete.png");
	inp.setAttribute("name", "delItem");
	inp.setAttribute("id", "delItem"+visitType+len);
	inp.setAttribute("onclick", "deleteItem(this,'"+visitType+"',"+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);
   	var previousRuleVisitType = (len>0) ? getSelectedVisitType(visitType,len-1) : ""; 

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "deleted");
    inp0.setAttribute("id", "deleted"+visitType+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "hidden");
    inp1.setAttribute("name", "addedNew");
    inp1.setAttribute("id", "addedNew"+visitType+len);
    inp1.setAttribute("value", "false");
    cell.appendChild(inp1);

    var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "hidden");
    inp1.setAttribute("name", "rule_details_id");
    inp1.setAttribute("id", "addedNew"+visitType+len);
    inp1.setAttribute("value", visitTypeRule['rule_details_id']);
    cell.appendChild(inp1);
    


	var valid_from = visitTypeRule['valid_from'];
	var cell1 = document.createElement("TD");
	var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "number");
    inp1.setAttribute("name", "valid_from");
    inp1.setAttribute("min", "0");
    inp1.setAttribute("max", "365");
    inp1.setAttribute("class", "numeric");
    inp1.setAttribute("id", "valid_from"+visitType+len);
    inp1.setAttribute("onChange","validateMaxAndMinDays('"+visitType+"',"+len+");updatePreviousMaxDays('"+visitType+"',"+len+");");
    inp1.setAttribute("value", valid_from);
    inp1.setAttribute("style", "width: 80px");
    cell1.appendChild(inp1);
	
	var valid_to = visitTypeRule['valid_to'];
	var cell2 = document.createElement("TD");
	// cell3.setAttribute("style", "width: 150px");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "number");
    inp2.setAttribute("name", "valid_to");
    inp2.setAttribute("min", "0");
    inp2.setAttribute("max", "365");
    inp2.setAttribute("class", "numeric");
    inp2.setAttribute("id", "valid_to"+visitType+len);
    inp2.setAttribute("onChange","validateMaxAndMinDays('"+visitType+"',"+len+");updateNextMaxDays('"+visitType+"',"+len+");");
    inp2.setAttribute("value", valid_to);
    inp2.setAttribute("style", "width: 80px");
    cell2.appendChild(inp2);
	
	var prevMainVisitType = visitTypeRule['prev_main_visit_type'];
	var inp3 = document.createElement("INPUT");
    inp3.setAttribute("type", "hidden");
    inp3.setAttribute("name", "prev_main_visit_type");
    inp3.setAttribute("class", "");
    inp3.setAttribute("id", "prev_main_visit_type"+visitType+len);
    inp3.setAttribute("value", prevMainVisitType);
    inp3.setAttribute("style", "width: 80px");
    cell2.appendChild(inp3);
    
    var opVisitType = visitTypeRule['op_visit_type'];
    var cell2select = document.createElement("select");
	cell2select.setAttribute("name", "op_visit_type");
	cell2select.setAttribute("id", "op_visit_type"+visitType+len);
	cell2select.setAttribute("class", "dropdown");
	cell2select.setAttribute("onchange", "updateMainVisitType('"+visitType+"',"+len+")");
	var cell2selectoption = document.createElement("option");
	if(opVisitType == "F") {
	  cell2selectoption.setAttribute("selected", "selected");
	}
	cell2selectoption.setAttribute("value", "F");
	cell2selectoption.innerHTML = "Follow up";
	cell2select.appendChild(cell2selectoption);
	var selectoption = document.createElement("option");
	selectoption.setAttribute("value", "R");
	if(opVisitType == "R") {
	  selectoption.setAttribute("selected", "selected");
	}
	if(previousRuleVisitType == "F") {
	  $("#baseItemTbl"+visitType+" #addresults").hide();
	}
	selectoption.innerHTML = "Revisit";
	cell2select.appendChild(selectoption);
    var cell3 = document.createElement("TD");

	cell3.appendChild(cell2select);
	   
	row.appendChild(cell1);
    row.appendChild(cell2);
	row.appendChild(cell3);

	row.appendChild(cell);

}
function updateMainVisitType(visitType, currentRowId) {
	if ($("#baseItemTbl"+visitType+" select[name=op_visit_type]").length == 2 
			&& $("#baseItemTbl"+visitType+" input[name=deleted][value=false] ").length == 2) {
		  $("#op_visit_type"+visitType+"0").val("F");
		  $("#op_visit_type"+visitType+"1").val("R");
		  $("#baseItemTbl"+visitType+" #addresults").hide();

		  return false;
	}
	//if the current selected visit type is revisit, then post_limit_visit must be main visit
	if ($("select#op_visit_type"+visitType+currentRowId).val()=='R') {
		$("#postLimitVisit").val('M');
		//hide the plus link, no more rules in this block
		$("#baseItemTbl"+visitType+" #addresults").hide();
	} else {
		$("#baseItemTbl"+visitType+" #addresults").show();
	}
	
} 

function updatePreviousMaxDays(visitType, rowId) {
	var maxDaysPreviousRow = "#valid_to"+visitType+(Number(rowId)-1);
	var minDays = "#valid_from"+visitType+rowId;

	if($(maxDaysPreviousRow).length>0) {
		if ((Number($(minDays).val()) >= Number($(maxDaysPreviousRow).val())) || 
				(Number($(minDays).val()) - Number($(maxDaysPreviousRow).val())) > 1 ||
				(Number($(minDays).val()) - Number($(maxDaysPreviousRow).val())) <= 0 ) {
        	$(maxDaysPreviousRow).val((Number($(minDays).val())-1<0)?0:Number($(minDays).val())-1);
        	validateMaxAndMinDays(visitType,Number(rowId)-1);
        	if((Number($(minDays).val()) <= Number($(maxDaysPreviousRow).val()))) {
        		$(minDays).val(Number($(maxDaysPreviousRow).val())+1);
   	     	}
       }
	}
}

function updateNextMaxDays(visitType, rowId) {
	var minDaysNextRow = "#valid_from"+visitType+(Number(rowId)+1);
	var maxDays = "#valid_to"+visitType+rowId;
	
	if($(minDaysNextRow).length>0) {
		if ((Number($(maxDays).val()) >= Number($(minDaysNextRow).val())) || 
				(Number($(minDaysNextRow).val()) - Number($(maxDays).val())) > 1 ) {
			$(minDaysNextRow).val(Number($(maxDays).val())+1);
			validateMaxAndMinDays(visitType,Number(rowId)+1);
		}
	}
}

function validateMaxAndMinDays(visitType, rowId) {
	var minDays = "#valid_from"+visitType+rowId;
	var maxDays = "#valid_to"+visitType+rowId;
	var minDaysPrevRow = "#valid_from"+visitType+(Number(rowId)-1);
	var maxDaysPrevRow = "#valid_to"+visitType+(Number(rowId)-1);
	var minDaysNextRow = "#valid_from"+visitType+(Number(rowId)+1);
	var maxDaysNextRow = "#valid_to"+visitType+(Number(rowId)+1);
	var minDaysValue = $(minDays).val();
	var maxDaysValue = $(maxDays).val();
	if($(minDays).val()>0) {
		if (Number($(maxDays).val()) < Number($(minDays).val()) ) {
		  $(maxDays).val(Number($(minDays).val())+1);
		  updateNextMaxDays(visitType, rowId);
		}
	}else{
		$(minDays).val(0);
	}
	if($(maxDays).val()>0) {
	  if ($(minDays).val()<0) {
	     $(minDays).val((Number($(maxDays).val())-1<0)?0:Number($(maxDays).val())-1); 
	  }	
	}else{
		$(maxDays).val(0);
	}

}
function getSelectedVisitType(visitType, rowId) {
	return ($("select#op_visit_type"+visitType+rowId).length>0) ? 
			$("select#op_visit_type"+visitType+rowId).val() : "";	
}

function AddRow(visitType) {
	var baseItemTbl = document.getElementById("baseItemTbl"+visitType);
	//check, is there an entry, get the previously selected visit type
	var len = baseItemTbl.rows.length;
	var templateRow = baseItemTbl.rows[len-2];
   	row = '';
   	row = templateRow.cloneNode(true);
   	row.style.display = '';
   	len = len-3;
   	var previousRuleVisitType = (len>0) ? getSelectedVisitType(visitType,len-1) : ""; 
   	row.id = visitType + len
   	YAHOO.util.Dom.insertBefore(row, templateRow);
    var previousRowMaxDays = (len>0) ? document.getElementById("valid_to"+visitType+(len-1)).value : 0;
	var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width: 40px");
	var inp = document.createElement("img");
	inp.setAttribute("class", "imgDelete");
	inp.setAttribute("src", cpath + "/icons/Delete.png");
	inp.setAttribute("name", "delItem");
	inp.setAttribute("id", "delItem"+visitType+len);
	inp.setAttribute("onclick", "deleteItem(this,'"+visitType+"',"+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "deleted");
    inp0.setAttribute("id", "deleted"+visitType+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "hidden");
    inp1.setAttribute("name", "addedNew");
    inp1.setAttribute("id", "addedNew"+visitType+len);
    inp1.setAttribute("value", "true");
    cell.appendChild(inp1);

	var cell1 = document.createElement("TD");
	inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "number");
    inp1.setAttribute("name", "valid_from");
    inp1.setAttribute("min", "0");
    inp1.setAttribute("max", "365");
    inp1.setAttribute("class", "numeric");
    inp1.setAttribute("onChange","validateMaxAndMinDays('"+visitType+"',"+len+");updatePreviousMaxDays('"+visitType+"',"+len+");");
    inp1.setAttribute("id", "valid_from"+visitType+len);
    inp1.setAttribute("value", (previousRowMaxDays>=0 && len>0) ? Number(previousRowMaxDays)+1:0);
    inp1.setAttribute("style", "width: 80px");
    cell1.appendChild(inp1);
	
	
	var cell2 = document.createElement("TD");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "number");
    inp2.setAttribute("name", "valid_to");
    inp2.setAttribute("class", "numeric");
    inp2.setAttribute("min", "0");
    inp2.setAttribute("max", "365");
    inp2.setAttribute("id", "valid_to"+visitType+len);
    inp2.setAttribute("value", (previousRowMaxDays>=0 && len>0) ? Number(previousRowMaxDays)+2:0);
    inp2.setAttribute("onChange","validateMaxAndMinDays('"+visitType+"',"+len+");updateNextMaxDays('"+visitType+"',"+len+");");
    inp2.setAttribute("style", "width: 80px");
    cell2.appendChild(inp2);
	
    var inp3 = document.createElement("INPUT");
    inp3.setAttribute("type", "hidden");
    inp3.setAttribute("name", "prev_main_visit_type");
    inp3.setAttribute("class", "");
    inp3.setAttribute("id", "prev_main_visit_type"+visitType+len);
    inp3.setAttribute("value", visitType);
    inp3.setAttribute("style", "width: 80px");
    cell.appendChild(inp3);
	

    var cell2select = document.createElement("select");
	cell2select.setAttribute("name", "op_visit_type");
	cell2select.setAttribute("id", "op_visit_type"+visitType+len);
	cell2select.setAttribute("class", "dropdown");
	cell2select.setAttribute("onchange", "updateMainVisitType('"+visitType+"',"+len+")");
	var cell2selectoption = document.createElement("option");
	cell2selectoption.setAttribute("value", "F");
	cell2selectoption.innerHTML = "Follow up";
	cell2select.appendChild(cell2selectoption);

	var cell2selectoption = document.createElement("option");
	cell2selectoption.setAttribute("value", "R");
	if(previousRuleVisitType != "" && previousRuleVisitType == "F") {
	  cell2selectoption.setAttribute("selected", "selected");
	  $("#baseItemTbl"+visitType+" #addresults").hide();
	  $("#postLimitVisit").val("M");
	} else if(previousRuleVisitType == "R"){
	  $("#op_visit_type"+visitType+(len-1)).val("F");
	}
	cell2selectoption.innerHTML = "Revisit";
	cell2select.appendChild(cell2selectoption);
    var cell3 = document.createElement("TD");
	cell3.appendChild(cell2select);
	  
	row.appendChild(cell1);
    row.appendChild(cell2);
	row.appendChild(cell3);

	row.appendChild(cell);

}



function deleteItem(checkBox, visitType, rowId) {
	var RateTbl = document.getElementById("baseItemTbl"+visitType);
	var row = RateTbl.rows[rowId];
	var deletedInput =
		document.getElementById('deleted' + visitType + rowId).value = document.getElementById('deleted' + visitType + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(visitType+rowId), "delete");
		document.getElementById('delItem'+visitType+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(visitType+rowId), "delete");
		document.getElementById('delItem'+visitType+rowId).src = cpath+'/icons/Delete.png';
	}
	updateMainVisitType(visitType, rowId);
}

function isNumber(number,msg, fieldId){

	if(number==""){
	    return false;
	}
	if(number.search(/^\d{1,}$/)==-1){
		alert(msg);
		document.getElementById(fieldId).value = 0;
		return false;
	}

	return true;
}


function validateForm(){
	
	  var rule_name = document.getElementById("rule_name").value.trim();
	   
	   if(!rule_name){
		  alert("Please enter rule name");
	  	  document.getElementById("rule_name").focus();
		  return false;
	   }
	   
	  
    for(var k=0; k<opVisitTypeRuleMaster.length; k++){
    	
    	var list = opVisitTypeRuleMaster[k];
    	if(document.getElementById('rule_name1').value != list.rule_name){
    		if(document.getElementById('rule_name').value == list.rule_name){
    			alert("Rule name already exist, please choose another name.");
    			document.getElementById('rule_name').focus();
    			 return false;
    		}
    	}
    }
		 
    
  	
  	var countOpRows = 0;
  	var countIpRows = 0;
  	var countOpFollowups = 0;
  	var countOpVisit = 0;
  	var countIpFollowups = 0;
  	var countIpVisit = 0;
  	var applicable_type = document.getElementsByName("prev_main_visit_type");
   	for(var i=0; i<applicable_type.length;i++) {
   		if(document.getElementById('deletedI'+i) != undefined && document.getElementById('deletedI'+i).value =="false"){
   			if (!document.getElementById('op_visit_typeI'+i).value.trim()) {
           		 alert("Please select anyone value in Applicable Category label "+(i+1));
          	     document.getElementById('op_visit_type'+i).focus();
           	     return false;
          	}
       		if (document.getElementById('op_visit_typeI'+i).value == 'F'){
       			countIpFollowups++;
       		}else{
       			countIpVisit++;
       		}
   			countIpRows++;
   		}
   		if(document.getElementById('deletedO'+i) != undefined && document.getElementById('deletedO'+i).value =="false"){
   			if (!document.getElementById('op_visit_typeO'+i).value.trim()) {
   				alert("Please select anyone value in Applicable Category label "+(i+1));
   				document.getElementById('op_visit_type'+i).focus();
   				return false;
   			}
   			if (document.getElementById('op_visit_typeO'+i).value == 'F'){
       			countOpFollowups++;
       		}else{
       			countOpVisit++;
       		}
   			countOpRows++;
   		}
   	} 
   	var ipMainVisitLimit = document.getElementById("ip_main_visit_limit");
   	var opMainVisitLimit = document.getElementById("op_main_visit_limit");
   	if(countOpRows == 0 && opMainVisitLimit.value >0){
   		alert("Please add atleast one Op rule.");
		return false;
   	}
   	if(countOpRows > 0 && opMainVisitLimit.value == 0){
   		alert("There are Op rules, Op visit limit can't be zero. Please add Op visit limit.");
   		$("#op_main_visit_limit").focus();
   		return false;
   	}
   	if(countIpRows == 0 && ipMainVisitLimit.value > 0){
   		alert("Please add atleast one Ip rule.");
   		return false;
   	}
   	if(countIpRows > 0 && ipMainVisitLimit.value == 0){
   		alert("There are Ip rules, Ip visit limit can't be zero. Please add Ip visit limit.");
   		$("#ip_main_visit_limit").focus();
   		return false;
   	}
   	
   	if(countOpFollowups>1 || countIpFollowups>1 || countOpVisit>1 || countIpVisit>1) {
   		alert("You can't have 2 follow up or 2 revits rules in same type.");
   		return false;
   	} 
   	if(ipMainVisitLimit.value == "" || Number(ipMainVisitLimit.value) < 0){
   		ipMainVisitLimit.value = 0;
   	}
   	if(opMainVisitLimit.value == "" || Number(opMainVisitLimit.value) < 0){
   		opMainVisitLimit.value = 0;
   	}
    document.OpVisitTypeRulesMasterForm.submit();
	return true; 
}

//Applicability 

function init() {
	var centersWithOutDefult = filterListWithExcludingValues(centers,'center_id', 0);
	loadSelectBox(document.getElementById("centers"), centersWithOutDefult, 'center_name', 'center_id','--Select Center--' , '');
	loadSelectBox(document.getElementById("tpas"), tpa, 'tpa_name', 'tpa_id','--Select Sponsor--' , '');
	loadSelectBox(document.getElementById("departments"), departments, 'dept_name', 'dept_id','--Select Department--' , '');
	loadSelectBox(document.getElementById("doctors"), {}, 'doctor_name', 'doctor_id','--Select Doctor--' , '');
	loadSelectBox(document.getElementById("rules"), rules, 'rule_name', 'rule_id','--Select Rule--' , '');
	

	 insertIntoSelectBox(document.getElementById("doctors"), 1, "All", "*"); 
	 insertIntoSelectBox(document.getElementById("doctors"), 2, "Not Applicable", "#"); 
	 insertIntoSelectBox(document.getElementById("tpas"), 1, "All", "*"); 
	 insertIntoSelectBox(document.getElementById("tpas"), 2, "Cash", "$"); 
	 insertIntoSelectBox(document.getElementById("departments"), 1, "All", "*"); 
	 insertIntoSelectBox(document.getElementById("centers"), 1, "All", "-1"); 
	 
	 $('select.dropdown').change(function(){
		 if($(this).val()!="") {
			 $(this).css("border","");
		 }else{
			 $(this).css("border","1px solid red");
		 }
	 });
	 finalDoctors = doctors;
}

function filterDoctorsWithCenter(){
	var selectedCenterId = $("#centers").val();
	var doctorIds = [];
	var centerFilteredDoctors = [];  
	var centers = [];  
	if (selectedCenterId>0) {
		centers.push(Number(selectedCenterId));
		centers.push(0);
		centerFilteredDoctors = filterListWithValues(centerDoctors,'center_id', centers);
		[].forEach.call(centerFilteredDoctors, function(doctor){
			doctorIds.push(doctor.doctor_id);
		});
		finalDoctors = filterListWithValues(doctors,'doctor_id',doctorIds);
	} else if(selectedCenterId == -1) {
		finalDoctors = doctors;
	}
	loadDoctors();
}

function loadDoctors(){
	if($("#departments").val()){
    	var deptDoctors = filterList(finalDoctors,'dept_id',$("#departments").val())
    	loadSelectBox(document.getElementById("doctors"), deptDoctors, 'doctor_name', 'doctor_id','--Select Doctor--' , '');
   	    insertIntoSelectBox(document.getElementById("doctors"), 1, "All", "*"); 
	    insertIntoSelectBox(document.getElementById("doctors"), 2, "Not Applicable", "#"); 
	}
}

function validateApplicability() {
	if (!$("#rules").val() || !$("#doctors").val() 
			|| !$("#departments").val() || !$("#tpas").val() || $("#centers").val() == '') {

		var selects = $("select[required=required]");

		[].forEach.call(selects, function(select) {
		  if(select.value == '') {
			select.style.border="1px solid red";
		  }
		});
		alert("All fields are mandatory!");
		
		return false;
	} 
	var selectedValues = $("#centers").val()+$("#tpas").val()+$("#departments").val();
	if ($('input[name="'+selectedValues+$("#doctors").val()+'"').length>0) {
		alert("Applicability combination already exists!");
		return false;
	}
	
	if($("#doctors").val() == '#'){
		if($('input[name="'+selectedValues+'*'+'"').length>0){
			alert("Applicability combination is already exists with 'All' doctors");
			return false;
		}
	}

	if($("#doctors").val() == '*'){
		if($('input[name="'+selectedValues+'#'+'"').length>0){
			alert("Applicability combination is already exists with 'Doctors Not Applicable'");
			return false;
		}
	}

    document.OpVisitTypeRulesApplicability.submit();
    return true;
}

function confirmDeleteApplicbility(applicabiltyId) {
	if(confirm("Are you sure, you want to delete?")) {
		window.location.href= cpath+'/master/followuprulesapplicability/delete.htm?rule_applicability_id='+applicabiltyId;
	}
}

function deleteConfirmation(ruleId) {
	if(confirm("Are you sure, you want to delete?")) {
		$("form[name=OpVisitTypeRulesMasterForm]").attr("action",cpath+"/master/visittyperules/delete.htm");
		$("input[name=_method]").val("delete");
	    document.OpVisitTypeRulesMasterForm.submit();
	}
}
