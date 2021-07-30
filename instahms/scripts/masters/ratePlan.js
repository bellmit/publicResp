function init() {
	fillBaseRateSheetDetails();
	enableDateValidity();
}

function AddRow() {
	var baseRateTbl = document.getElementById("baseRateTbl");
	var len = baseRateTbl.rows.length;
	var templateRow = baseRateTbl.rows[len-2];
   	var row = '';
   		row = templateRow.cloneNode(true);
   		row.style.display = '';
   		row.id = len-3;
   		len = row.id;
   	YAHOO.util.Dom.insertBefore(row, templateRow);

	var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width: 20px");
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
    cell1.setAttribute("style", "width: 70px");

    cell1.innerHTML = '<select name="ratesheet" id="ratesheet'+len+'" onchange="checkDuplicateRateSheet('+len+')"></select>';
	loadSelectBox(document.getElementById("ratesheet"+len), rateSheetList, "rate_sheet", "rate_sheet_id","Select");

	var cell2 = document.createElement("TD");
	cell2.setAttribute("style", "width: 60px");
	cell2.innerHTML = '<select name="discORmarkup" id="discORmarkup'+len+'" class="dropdown"/>'+
			 '<option value="">--Select--</option> '+
			 '<option value="I">Increase By</option> '+
			 '<option value="D">Decrease By</option> '+
			 '</select>';

	var cell3 = document.createElement("TD");
	var inp3 = document.createElement("INPUT");
    inp3.setAttribute("type", "text");
    inp3.setAttribute("name", "rateVariation");
    inp3.setAttribute("size", "3");
    inp3.setAttribute("id", "rateVariation"+len);
    inp3.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
    inp3.setAttribute("style", "width: 60px");
    cell3.appendChild(inp3);

    var cell4 = document.createElement("TD");
    cell4.setAttribute("style", "width: 60px");
	cell4.innerHTML = '<select name="roundOff" id="roundOff'+len+'" class="dropdown"/>'+
			 '<option value="0">None</option> '+
			 '<option value="1">1</option> '+
			 '<option value="5">5</option> '+
			 '<option value="10">10</option> '+
			 '<option value="25">25</option> '+
			 '<option value="50">50</option> '+
			 '<option value="100">100</option> '+
			 '</select>';

	var cell5 = document.createElement("TD");
	var inp5 = document.createElement("INPUT");
	inp5.setAttribute("type", "text");
    inp5.setAttribute("name", "priority");
    inp5.setAttribute("size", "3");
    inp5.setAttribute("id", "priority"+len);
    inp5.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
    inp5.setAttribute("style", "width: 60px");
    cell5.appendChild(inp5);


	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	row.appendChild(cell5);

	row.appendChild(cell);
}

function fillBaseRateSheetDetails(){
	for (var i =0; i<baseRateSheetsDetails.length; i++) {
		var baseRateTbl = document.getElementById("baseRateTbl");
		var len = baseRateTbl.rows.length;
		var templateRow = baseRateTbl.rows[len-2];
	   	var row = '';
	   		row = templateRow.cloneNode(true);
	   		row.style.display = '';
	   		row.id = len-3;
	   		len = row.id;
	   	YAHOO.util.Dom.insertBefore(row, templateRow);
	   	var cell = document.createElement("TD");

	    cell.setAttribute("class", 'last');
	    cell.setAttribute("style", "width: 20px");
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
	    inp1.setAttribute("value", "false");
	    cell.appendChild(inp1);

		var cell1 = row.insertCell(-1);
	    cell1.setAttribute("style", "width: 70px");
	    cell1.innerHTML = "<span class='label'>"+baseRateSheetsDetails[i].base_rate_sheet;
	    var inp2 = document.createElement("INPUT");
	    inp2.setAttribute("type", "hidden");
	    inp2.setAttribute("name", "ratesheet");
	    inp2.setAttribute("id", "ratesheet"+len);
	    inp2.setAttribute("value", baseRateSheetsDetails[i].base_rate_sheet_id);
	    cell1.appendChild(inp2);

		var cell2 = document.createElement("TD");
		cell2.setAttribute("style", "width: 60px");
		cell2.innerHTML = '<select name="discORmarkup" id="discORmarkup'+len+'" class="dropdown"/>'+
				 '<option value="">--Select--</option> '+
				 '<option value="I">Increase By</option> '+
				 '<option value="D">Decrease By</option> '+
				 '</select>';


		var cell3 = document.createElement("TD");
		var inp3 = document.createElement("INPUT");
	    inp3.setAttribute("type", "text");
	    inp3.setAttribute("name", "rateVariation");
	    inp3.setAttribute("size", "3");
	    inp3.setAttribute("id", "rateVariation"+len);
	    inp3.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
	    var variation = baseRateSheetsDetails[i].rate_variation_percent+'';
	    variation = variation.replace(/\-/g, "");

	    inp3.setAttribute("value",variation);
	    inp3.setAttribute("style", "width: 60px");
	    cell3.appendChild(inp3);

	    var cell4 = document.createElement("TD");
	    cell4.setAttribute("style", "width: 60px");
		cell4.innerHTML = '<select name="roundOff" id="roundOff'+len+'" class="dropdown"/>'+
				 '<option value="0">None</option> '+
				 '<option value="1">1</option> '+
				 '<option value="5">5</option> '+
				 '<option value="10">10</option> '+
				 '<option value="25">25</option> '+
				 '<option value="50">50</option> '+
				 '<option value="100">100</option> '+
				 '</select>';

		var cell5 = document.createElement("TD");
		var inp5 = document.createElement("INPUT");
		inp5.setAttribute("type", "text");
	    inp5.setAttribute("name", "priority");
	    inp5.setAttribute("size", "3");
	    inp5.setAttribute("id", "priority"+len);
	    inp5.setAttribute("value", baseRateSheetsDetails[i].priority);
	    inp5.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
	    inp5.setAttribute("style", "width: 60px");
	    cell5.appendChild(inp5);

		row.appendChild(cell2);
		row.appendChild(cell3);
		row.appendChild(cell4);
		row.appendChild(cell5);
		row.appendChild(cell);
		if(baseRateSheetsDetails[i].rate_variation_percent>0)
			setSelectedIndex(document.getElementById("discORmarkup"+len), 'I');
		else
			setSelectedIndex(document.getElementById("discORmarkup"+len), 'D');

		setSelectedIndex(document.getElementById("roundOff"+len), baseRateSheetsDetails[i].round_off_amount);
	}
}






function deleteItem(checkBox, rowId) {
	var RateTbl = document.getElementById("baseRateTbl");
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

function enableDateValidity(){
	if (document.RatePlan.has_date_validity.checked){
		document.getElementById('dateValidDiv').style.visibility = 'visible';
	}else{
		document.getElementById('dateValidDiv').style.visibility = 'hidden';
	}
}

function validate(){

	var form = document.RatePlan
	var orgName = trim(form.org_name.value);
	var orgAddress = form.org_address.value;

	if(orgName == ''){
		alert("Rate plan name is required");
		form.org_name.focus();
		return false;
	}

	if(orgAddress.length > 100){
		alert("Address length cannot be greater than 100 characters");
		form.org_address.focus();
		return false;
	}

	if (document.RatePlan.has_date_validity.checked){
		if (document.RatePlan.fromDate.value == ''){
			alert("From Date is required");
			return false;
		}
		if (document.RatePlan.toDate.value == ''){
			alert("To Date is required");
			return false;
		}
	}

	if(!validateDates())
		return false;
	if(!validateRateParameters())
		return false;
	else
		document.RatePlan.submit();
}

function validateDates(){
	if (document.RatePlan.has_date_validity.checked){
		return validateFromToDate(document.RatePlan.fromDate, document.RatePlan.toDate);
	}else {
	return true;
	}
}

function validateRateParameters() {
	var rateSheet = document.getElementsByName("ratesheet");
	var discORmarkup = document.getElementsByName("discORmarkup");
	var rateVariation = document.getElementsByName("rateVariation");
	var roundOff = document.getElementsByName("roundOff");
	var priority = document.getElementsByName("priority");
	var deleted = document.getElementsByName("deleted");
	var baseRateTbl = document.getElementById("baseRateTbl");
	var len = baseRateTbl.rows.length;
	if(len <= 3) {
		alert("Please add atleast one base rate sheet to create a rate plan..");
		return false;
	} else {
		var delCount = 0;
		for(var d=0; d<deleted.length; d++) {
			if(document.getElementById("deleted"+d).value=='true')
				delCount++;
		}
		len = len-delCount;
		if(len <= 3) {
			alert("Please add atleast one base rate sheet to create a rate plan..");
			return false;
		}

		for(var i=0; i<rateSheet.length; i++) {
			if(document.getElementById("deleted"+i).value=='false' )  {
				if(document.getElementById("ratesheet"+i).value=='') {
					alert("Please select rate sheet.");
					return false;
				}
				if(document.getElementById("discORmarkup"+i).value=='') {
					alert("Please select Discount /Markup.");
					return false;
				}
				if(document.getElementById("rateVariation"+i).value=='') {
					alert("Please select Variation %.");
					return false;
				}
				if(document.getElementById("priority"+i).value=='') {
					alert("Please select priority.");
					return false;
				}
			}
		}

		for(var j=0; j<priority.length; j++) {
			if(document.getElementById("deleted"+j).value=='false')
			for(var k=0; k<priority.length; k++) {
				if(document.getElementById("deleted"+k).value=='false' && k!=j) {
					if(document.getElementById("priority"+j).value==document.getElementById("priority"+k).value) {
						alert("Please change the priority it should not be same.");
						return false;
					}
				}
			}
		}
	}
	return true;
}

function checkDuplicateRateSheet(id){
	var rateSheet = document.getElementsByName("ratesheet");
	for(var i=0; i<rateSheet.length;i++) {
			if(i!=id) {
				if(document.getElementById("ratesheet"+id).value==document.getElementById("ratesheet"+i).value) {
					alert("Duplicate rate sheets are not allowed. Please change the rate sheet.");
					document.getElementById("ratesheet"+id).value ='';
					return false;
				}
		}
	}
}