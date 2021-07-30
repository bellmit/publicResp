function init(){
	initDoctorautocomplete();
	initSIDialog();
	initEditSIDialog();
	fillfollicledetails();
}

function initDoctorautocomplete() {
	var dataSource = new YAHOO.util.LocalDataSource({result : doctors});
	dataSource.responseType = YAHOO.util.DataSourceBase.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : 'result',
		fields : [
			{key : 'doctor_name'},
			{key : 'doctor_id'}
		]
	};
	var doctorAutoComp = new YAHOO.widget.AutoComplete('doctor', 'doctorContainer', dataSource);
	doctorAutoComp.minQueryLength = 0;
	doctorAutoComp.animVert = false;
	doctorAutoComp.maxResultsDisplayed = 50;
	doctorAutoComp.allowBrowserAutocomplete = false;
	doctorAutoComp.resultTypeList = false;
	doctorAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	doctorAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	doctorAutoComp.itemSelectEvent.subscribe(setDoctorId);
	doctorAutoComp.unmatchedItemSelectEvent.subscribe(removeDoctorId);
	if (doctorAutoComp._elTextbox.value != '') {
			doctorAutoComp._bItemSelected = true;
			doctorAutoComp._sInitInputValue = doctorAutoComp._elTextbox.value;
		}
}
function setDoctorId(oSelf, elItem) {
	document.getElementById('doctor_id').value = elItem[2].doctor_id;
}

function removeDoctorId() {
	document.getElementById('doctor_id').value = '';
}

function AddRowL() {
var LTable = document.getElementById("LTable");
	var len = LTable.rows.length;
	var templateRow = LTable.rows[len-2];
   	var row = '';
   		row = templateRow.cloneNode(true);
   		row.style.display = '';
   		row.id = len-3;
   		len = row.id;
   	YAHOO.util.Dom.insertBefore(row, templateRow);

    var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width:20px");
	var inp = document.createElement("img");
	inp.setAttribute("class", "imgDelete");
	inp.setAttribute("src", cpath + "/icons/Delete.png");
	inp.setAttribute("name", "delItemL");
	inp.setAttribute("id", "delItemL"+len);
	inp.setAttribute("onclick", "deleteItemL(this, "+len+")");
	inp.setAttribute("style", "width:16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "ldeleted");
    inp0.setAttribute("id", "ldeleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var cell1 = document.createElement("TD");
    cell1.setAttribute("class", 'border');
    cell1.setAttribute("style", "width:60px");
	var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "text");
    inp1.setAttribute("name", "lsize");
    inp1.setAttribute("size", "3");
    inp1.setAttribute("id", "lsize"+len);
    inp1.setAttribute("style", "width:60px");
    inp1.setAttribute("onkeypress", "return enterNumAndDot(event);");
    inp1.setAttribute("onchange", "return makeingDec(this.value,this)");

    cell1.appendChild(inp1);

     var cell2 = document.createElement("TD");
    cell2.setAttribute("class", 'border');
    cell2.setAttribute("style", "width:60px");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "text");
    inp2.setAttribute("name", "lcount");
    inp2.setAttribute("size", "3");
    inp2.setAttribute("id", "lcount"+len);
    inp2.setAttribute("style", "width:60px");
    inp2.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");

    cell2.appendChild(inp2);

	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell);

}

function AddRowR() {
	var RTable = document.getElementById("RTable");
	var len = RTable.rows.length;
	var templateRow = RTable.rows[len-2];
   	var row = '';
   		row = templateRow.cloneNode(true);
   		row.style.display = '';
   		row.id = len-3;
   		len = row.id;
   	YAHOO.util.Dom.insertBefore(row, templateRow);

    var cell = document.createElement("TD");
    cell.setAttribute("class", 'last');
    cell.setAttribute("style", "width:20px");
	var inp = document.createElement("img");
	inp.setAttribute("class", "imgDelete");
	inp.setAttribute("src", cpath + "/icons/Delete.png");
	inp.setAttribute("name", "delItemR");
	inp.setAttribute("id", "delItemR"+len);
	inp.setAttribute("onclick", "deleteItemR(this, "+len+")");
	inp.setAttribute("style", "width:16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "rdeleted");
    inp0.setAttribute("id", "rdeleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

	var cell1 = document.createElement("TD");
    cell1.setAttribute("class", 'border');
    cell1.setAttribute("style", "width:60px");
	var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "text");
    inp1.setAttribute("name", "rsize");
    inp1.setAttribute("size", "3");
    inp1.setAttribute("id", "rsize"+len);
    inp1.setAttribute("style", "width:60px");
    inp1.setAttribute("onkeypress", "return enterNumAndDot(event)");
    inp1.setAttribute("onchange", "makeingDec(this.value,this)");

    cell1.appendChild(inp1);

    var cell2 = document.createElement("TD");
    cell2.setAttribute("class", 'border');
    cell2.setAttribute("style", "width:60px");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "text");
    inp2.setAttribute("name", "rcount");
    inp2.setAttribute("size", "3");
    inp2.setAttribute("id", "rcount"+len);
    inp2.setAttribute("style", "width:60px");
    inp2.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");

    cell2.appendChild(inp2);

	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell);
}

function fillfollicledetails() {
	for (var i =0; i<follicleDetails.length; i++) {
		if(follicleDetails[i].ovary_position == 'L') {
			var LTable = document.getElementById("LTable");
			var len = LTable.rows.length;
			var templateRow = LTable.rows[len-2];
		   	var row = '';
		   		row = templateRow.cloneNode(true);
		   		row.style.display = '';
		   		row.id = len-3;
		   		len = row.id;
		   	YAHOO.util.Dom.insertBefore(row, templateRow);

		    var cell = document.createElement("TD");
		    cell.setAttribute("class", 'last');
		    cell.setAttribute("style", "width:20px");
			var inp = document.createElement("img");
			inp.setAttribute("class", "imgDelete");
			inp.setAttribute("src", cpath + "/icons/Delete.png");
			inp.setAttribute("name", "delItemL");
			inp.setAttribute("id", "delItemL" + len);
			inp.setAttribute("onclick", "deleteItemL(this, "+len+")");
			inp.setAttribute("style", "width:16px");
			cell.appendChild(inp);

			var inp0 = document.createElement("INPUT");
		    inp0.setAttribute("type", "hidden");
		    inp0.setAttribute("name", "ldeleted");
		    inp0.setAttribute("id", "ldeleted" + len);
		    inp0.setAttribute("value", "false");
		    cell.appendChild(inp0);

		    var cell1 = document.createElement("TD");
		    cell1.setAttribute("class", 'border');
		    cell1.setAttribute("style", "width:60px");
			var inp1 = document.createElement("INPUT");
		    inp1.setAttribute("type", "text");
		    inp1.setAttribute("name", "lsize");
		    inp1.setAttribute("size", "3");
		    inp1.setAttribute("id", "lsize" + len);
		    inp1.setAttribute("value", follicleDetails[i].follicles_size);
		    inp1.setAttribute("style", "width:60px");
		    inp1.setAttribute("onkeypress", "return enterNumAndDot(event)");
		    inp1.setAttribute("onchange", "makeingDec(this.value,this)");
		    cell1.appendChild(inp1);

		    var cell2 = document.createElement("TD");
		    cell2.setAttribute("class", 'border');
		    cell2.setAttribute("style", "width:60px");
			var inp2 = document.createElement("INPUT");
		    inp2.setAttribute("type", "text");
		    inp2.setAttribute("name", "lcount");
		    inp2.setAttribute("size", "3");
		    inp2.setAttribute("id", "lcount" + len);
		    inp2.setAttribute("value", follicleDetails[i].follicles_count);
		    inp2.setAttribute("style", "width:60px");
		    inp2.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
		    cell2.appendChild(inp2);

			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell);
		} else {
			var RTable = document.getElementById("RTable");
			var len = RTable.rows.length;
			var templateRow = RTable.rows[len-2];
		   	var row = '';
		   		row = templateRow.cloneNode(true);
		   		row.style.display = '';
		   		row.id = len-3;
		   		len = row.id;
		   	YAHOO.util.Dom.insertBefore(row, templateRow);

		    var cell = document.createElement("TD");
		    cell.setAttribute("class", 'last');
		    cell.setAttribute("style", "width:20px");
			var inp = document.createElement("img");
			inp.setAttribute("class", "imgDelete");
			inp.setAttribute("src", cpath + "/icons/Delete.png");
			inp.setAttribute("name", "delItemR");
			inp.setAttribute("id", "delItemR" + len);
			inp.setAttribute("onclick", "deleteItemR(this, "+len+")");
			inp.setAttribute("style", "width:16px");
			cell.appendChild(inp);

			var inp0 = document.createElement("INPUT");
		    inp0.setAttribute("type", "hidden");
		    inp0.setAttribute("name", "rdeleted");
		    inp0.setAttribute("id", "rdeleted" + len);
		    inp0.setAttribute("value", "false");
		    cell.appendChild(inp0);

		    var cell1 = document.createElement("TD");
		    cell1.setAttribute("class", 'border');
		    cell1.setAttribute("style", "width:60px");
			var inp1 = document.createElement("INPUT");
		    inp1.setAttribute("type", "text");
		    inp1.setAttribute("name", "rsize");
		    inp1.setAttribute("size", "3");
		    inp1.setAttribute("id", "rsize" + len);
		    inp1.setAttribute("value", follicleDetails[i].follicles_size);
		    inp1.setAttribute("style", "width:60px");
		    inp1.setAttribute("onkeypress", "return enterNumAndDot(event)");
		    inp1.setAttribute("onchange", "makeingDec(this.value,this)");
		    cell1.appendChild(inp1);

		    var cell2 = document.createElement("TD");
		    cell2.setAttribute("class", 'border');
		    cell2.setAttribute("style", "width:60px");
			var inp2 = document.createElement("INPUT");
		    inp2.setAttribute("type", "text");
		    inp2.setAttribute("name", "rcount");
		    inp2.setAttribute("size", "3");
		    inp2.setAttribute("id", "rcount" + len);
		    inp2.setAttribute("value", follicleDetails[i].follicles_count);
		    inp2.setAttribute("style", "width:60px");
		    inp2.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
		    cell2.appendChild(inp2);

			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell);
		}
	}
}

function deleteItemL(checkBox, rowId) {
	var itemListTable = document.getElementById("LTable");
	var row = itemListTable.rows[rowId];
	var deletedInput =
		document.getElementById('ldeleted' + rowId).value = document.getElementById('ldeleted' + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(rowId), "delete");
		document.getElementById('delItemL'+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(rowId), "delete");
		document.getElementById('delItemL'+rowId).src = cpath+'/icons/Delete.png';
	}
}

function deleteItemR(checkBox, rowId) {
	var itemListTable = document.getElementById("RTable");
	var row = itemListTable.rows[rowId];
	var deletedInput =
		document.getElementById('rdeleted' + rowId).value = document.getElementById('rdeleted' + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(rowId), "cancelled");
		document.getElementById('delItemR'+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(rowId), "cancelled");
		document.getElementById('delItemR'+rowId).src = cpath+'/icons/Delete.png';
	}
}
function funValidateAndSubmit() {
	var treatmentDate = document.treatmentForm.treatment_date.value;
	var dayofTreatment = document.treatmentForm.treatment_days_from_start.value;
	if(treatmentDate == '' && dayofTreatment == '') {
		alert("Please enter date and day of treatment to save.");
		return false;
	}
	var method = document.treatmentForm._method.value;
	var ivfCycleID = document.treatmentForm.ivf_cycle_id.value;
	var ivfCycleDailyID = document.treatmentForm.ivf_cycle_daily_id.value;
	var mrNo = document.treatmentForm.mr_no.value;;
	var patientID = document.treatmentForm.patient_id.value;
	if(method=='add') {
		document.treatmentForm.action=cpath+"/IVF/IVFDailyTreatment.do?_method=create&ivf_cycle_id="+ivfCycleID+"&mr_no="+mrNo+
		"&patient_id="+patientID;
	}
	else {
		document.treatmentForm.action=cpath+"/IVF/IVFDailyTreatment.do?_method=update&ivf_cycle_id="+ivfCycleID+"&mr_no="+mrNo+
		"&patient_id="+patientID+"&ivf_cycle_daily_id="+ivfCycleDailyID;
	}

	document.treatmentForm.submit();
}

function onChangeharmoneValues(obj,objvalue,harmone){

	makeingDec(objvalue,obj);

	if(harmone=='fsh'){
		if(!(objvalue>=0 && objvalue<=30)){
			alert("Please enter valid value for FSH between 0 and 30 .");
			setTimeout('document.getElementById("fsh_value").focus()',100);
			document.getElementById("fsh_value").value = '';
			return false;
		}
	}else if(harmone=='lh'){
		if(!(objvalue>=15 && objvalue<=99)){
			alert("Please enter valid value for LH between 15 and 99 .");
			setTimeout('document.getElementById("lh_value").focus()',100);
			document.getElementById("lh_value").value = '';
			return false;
		}
	}else if(harmone=='tsh'){
		if(!(objvalue>=0 && objvalue<=9.99)){
			alert("Please enter valid value for TSH between 0 and 9.99");
			setTimeout('document.getElementById("tsh_value").focus()',100);
			document.getElementById("tsh_value").value='';
			return false;
		}
	}
}