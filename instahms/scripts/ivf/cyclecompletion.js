function init() {
	OnChangeTreatCycle();
	getTotalOocytes();
}

function fillembryotransferdetails() {
		for (var i =0; i<embryoDetails.length; i++) {
			if(embryoDetails[i].embryo_op == 'T') {
				var ETTable = document.getElementById("ETTable");
				var len = ETTable.rows.length;
				var templateRow = ETTable.rows[len-2];
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
				inp.setAttribute("name", "delItemT");
				inp.setAttribute("id", "delItemT" + len);
				inp.setAttribute("onclick", "deleteItemT(this, "+len+")");
				inp.setAttribute("style", "width:16px");
				cell.appendChild(inp);

				var inp0 = document.createElement("INPUT");
			    inp0.setAttribute("type", "hidden");
			    inp0.setAttribute("name", "tdeleted");
			    inp0.setAttribute("id", "tdeleted" + len);
			    inp0.setAttribute("value", "false");
			    cell.appendChild(inp0);

			    var cell1 = document.createElement("TD");
			    cell1.setAttribute("class", 'border');
			    cell1.setAttribute("style", "width: 60px");
				var inp1 = document.createElement("INPUT");
			    inp1.setAttribute("type", "text");
			    inp1.setAttribute("name", "etnumber");
			    inp1.setAttribute("size", "3");
			    inp1.setAttribute("id", "etnumber" + len);
			    inp1.setAttribute("value", embryoDetails[i].emb_number);
			    inp1.setAttribute("style","width:60px");
			    inp1.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
			    cell1.appendChild(inp1);

			    var inp4 = document.createElement("INPUT");
			    inp4.setAttribute("type", "hidden");
			    inp4.setAttribute("name", "embryoidt");
			    inp4.setAttribute("id", "embryoidt" + len);
			    inp4.setAttribute("value", embryoDetails[i].ivf_cycle_embryo_id);
			    cell1.appendChild(inp4);

			    var cell2 = document.createElement("TD");
			    cell2.setAttribute("style", "width: 60px");

			    cell2.innerHTML = '<select name="etgrade" id="etgrade'+len+'" class="dropdown"/>'+
						 '<option value="">--Select--</option> '+
						 '<option value="A">A</option> '+
						 '<option value="B">B</option>'+
						 '<option value="C">C</option>'+
						 '</select>';

				row.appendChild(cell1);
				row.appendChild(cell2);
				row.appendChild(cell);
				setSelectedIndex(document.getElementById("etgrade"+len), embryoDetails[i].emb_grade);
			}
		}
	}

function AddRowET() {
	var ETTable = document.getElementById("ETTable");
	var len = ETTable.rows.length;
	var templateRow = ETTable.rows[len-2];
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
	inp.setAttribute("name", "delItemT");
	inp.setAttribute("id", "delItemT"+len);
	inp.setAttribute("onclick", "deleteItemT(this, "+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "tdeleted");
    inp0.setAttribute("id", "tdeleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var cell1 = document.createElement("TD");
    cell1.setAttribute("class", 'border');
    cell1.setAttribute("style", "width: 60px");
	var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "text");
    inp1.setAttribute("name", "etnumber");
    inp1.setAttribute("size", "3");
    inp1.setAttribute("id", "etnumber"+len);
    inp1.setAttribute("style", "width: 60px");
    inp1.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
    cell1.appendChild(inp1);

    var inp4 = document.createElement("INPUT");
    inp4.setAttribute("type", "hidden");
    inp4.setAttribute("name", "embryoidt");
    inp4.setAttribute("id", "embryoidt" + len);
    inp4.setAttribute("value", "");
    cell1.appendChild(inp4);

    var cell2 = document.createElement("TD");
    cell2.setAttribute("style", "width: 60px");

    cell2.innerHTML = '<select name="etgrade" id="etgrade'+len+'" class="dropdown"/>'+
			 '<option value="">--Select--</option> '+
			 '<option value="A">A</option> '+
			 '<option value="B">B</option>'+
			 '<option value="C">C</option>'+
			 '</select>';

	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell);
}

function deleteItemT(checkBox, rowId) {
	var itemListTable = document.getElementById("ETTable");
	var row = itemListTable.rows[rowId];
	var deletedInput =
		document.getElementById('tdeleted' + rowId).value = document.getElementById('tdeleted' + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(rowId), "cancelled");
		document.getElementById('delItemT'+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(rowId), "cancelled");
		document.getElementById('delItemT'+rowId).src = cpath+'/icons/Delete.png';
	}
}

function AddRowEF() {
	var EFTable = document.getElementById("EFTable");
	var len = EFTable.rows.length;
	var templateRow = EFTable.rows[len-2];
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
	inp.setAttribute("name", "delItemF");
	inp.setAttribute("id", "delItemF"+len);
	inp.setAttribute("onclick", "deleteItemF(this, "+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "fdeleted");
    inp0.setAttribute("id", "fdeleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var cell1 = document.createElement("TD");
    cell1.setAttribute("class", 'border');
    cell1.setAttribute("style", "width: 60px");
	var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "text");
    inp1.setAttribute("name", "efnumber");
    inp1.setAttribute("size", "3");
    inp1.setAttribute("id", "efnumber"+len);
    inp1.setAttribute("style", "width: 60px");
    inp1.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
    cell1.appendChild(inp1);

    var inp4 = document.createElement("INPUT");
    inp4.setAttribute("type", "hidden");
    inp4.setAttribute("name", "embryoidf");
    inp4.setAttribute("id", "embryoidf" + len);
    inp4.setAttribute("value", "");
    cell1.appendChild(inp4);

    var cell2 = document.createElement("TD");
    cell2.setAttribute("class", 'border');
    cell2.setAttribute("style", "width: 60px");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "text");
    inp2.setAttribute("name", "efstate");
    inp2.setAttribute("size", "3");
    inp2.setAttribute("id", "efstate"+len);
    inp2.setAttribute("style", "width: 60px");
	 cell2.appendChild(inp2);

    var cell3 = document.createElement("TD");
    cell3.setAttribute("style", "width: 60px");
    cell3.innerHTML = '<select name="efgrade" id="efgrade'+len+'" class="dropdown"/>'+
			 '<option value="">--Select--</option> '+
			 '<option value="A">A</option> '+
			 '<option value="B">B</option>'+
			 '<option value="C">C</option>'+
			 '</select>';

	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell);
}

function deleteItemF(checkBox, rowId) {
	var itemListTable = document.getElementById("EFTable");
	var row = itemListTable.rows[rowId];
	var deletedInput =
		document.getElementById('fdeleted' + rowId).value = document.getElementById('fdeleted' + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(rowId), "cancel");
		document.getElementById('delItemF'+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(rowId), "cancel");
		document.getElementById('delItemF'+rowId).src = cpath+'/icons/Delete.png';
	}
}

function funValidateAndSubmit() {
	if(!validateTime(document.CycleCompletion.rhcguhcg_time)) {
		alert("Please enter correct time format..");
		document.CycleCompletion.rhcguhcg_time.value='';
		return false;
	}
	var method = document.CycleCompletion._method.value;
	var ivfCycleID = document.CycleCompletion.ivf_cycle_id.value;
	var mrNo = document.CycleCompletion.mr_no.value;;
	var patientID = document.CycleCompletion.patient_id.value;

	document.CycleCompletion.action=cpath+"/IVF/IVFCycleCompletion.do?_method=update&ivf_cycle_id="+ivfCycleID+"&mr_no="+mrNo+
		"&patient_id="+patientID;
	document.CycleCompletion.submit();
}

function OnChangeTreatCycle() {
	var treatCycle = document.getElementById("final_treatment_cycle").value;
	var IUIDIV = document.getElementById("IUIdiv");
	var IVFDIV = document.getElementById("IVFdiv");
	if(treatCycle == 'IU') {
		IVFDIV.style.display="none";
		IUIDIV.style.display="block";
	} else {
		IUIDIV.style.display="none";
		IVFDIV.style.display="block";
	}
}

function fillembryofrozendetails() {
	for (var i =0; i<embryoDetails.length; i++) {
		if(embryoDetails[i].embryo_op == 'F') {
			var EFTable = document.getElementById("EFTable");
			var len = EFTable.rows.length;
			var templateRow = EFTable.rows[len-2];
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
			inp.setAttribute("name", "delItemF");
			inp.setAttribute("id", "delItemF" + len);
			inp.setAttribute("onclick", "deleteItemF(this, "+len+")");
			inp.setAttribute("style","width:16px");
			cell.appendChild(inp);

			var inp0 = document.createElement("INPUT");
		    inp0.setAttribute("type", "hidden");
		    inp0.setAttribute("name", "fdeleted");
		    inp0.setAttribute("id", "fdeleted" + len);
		    inp0.setAttribute("value", "false");
		    cell.appendChild(inp0);

		    var cell1 = document.createElement("TD");
		    cell1.setAttribute("class", 'border');
		    cell1.setAttribute("style", "width: 60px");
			var inp1 = document.createElement("INPUT");
		    inp1.setAttribute("type", "text");
		    inp1.setAttribute("name", "efnumber");
		    inp1.setAttribute("size", "3");
		    inp1.setAttribute("id", "efnumber" + len);
		    inp1.setAttribute("value", embryoDetails[i].emb_number);
		    inp1.setAttribute("style","width:60px");
		    inp1.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
		    cell1.appendChild(inp1);

		    var inp4 = document.createElement("INPUT");
		    inp4.setAttribute("type", "hidden");
		    inp4.setAttribute("name", "embryoidf");
		    inp4.setAttribute("id", "embryoidf" + len);
		    inp4.setAttribute("value", embryoDetails[i].ivf_cycle_embryo_id);
		    cell1.appendChild(inp4);

		    var cell2 = document.createElement("TD");
		    cell2.setAttribute("class", 'border');
		    cell2.setAttribute("style", "width: 60px");
			var inp2 = document.createElement("INPUT");
		    inp2.setAttribute("type", "text");
		    inp2.setAttribute("name", "efstate");
		    inp2.setAttribute("size", "3");
		    inp2.setAttribute("id", "efstate" + len);
		    inp2.setAttribute("value", embryoDetails[i].emb_state);
		    inp2.setAttribute("style","width:60px");
		    cell2.appendChild(inp2);

		    var cell3 = document.createElement("TD");
		    cell3.setAttribute("style", "width: 40px");

		    cell3.innerHTML = '<select name="efgrade" id="efgrade'+len+'" class="dropdown"/>'+
					 '<option value="">--Select--</option> '+
					 '<option value="A">A</option> '+
					 '<option value="B">B</option>'+
					 '<option value="C">C</option>'+
					 '</select>';

			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell3);
			row.appendChild(cell);
			setSelectedIndex(document.getElementById("efgrade"+len), embryoDetails[i].emb_grade);
		}
	}
}

function getTotalOocytes(){
	var totalOocyte = 0;
	for(var i=0; i<OOCyteDetails.length; i++){
		totalOocyte = totalOocyte + OOCyteDetails[i].oocyte_number;
	}
	if(totalOocyte != 0) {
		document.getElementById("total_Oocyte").textContent = totalOocyte;
		getFertilizedRateperc();
		getCleavageRateperc();
	}
}
function getFertilizedRateperc(){
	var totOocyte = document.getElementById("total_Oocyte").textContent;
	if(totOocyte!="") {
		var fertRatenum = document.getElementById("fertilization_rate_number").value;
		var fertRatePerc = fertRatenum/totOocyte;
		fertRatePerc = Math.round(fertRatePerc*100);
		document.getElementById("fertilizationRatePerc").textContent = fertRatePerc;
		document.getElementById("fertilization_rate_perc").value = fertRatePerc;
	}
}
function getCleavageRateperc(){
	var totOocyte = document.getElementById("total_Oocyte").textContent;
	if(totOocyte!="") {
		var cleavageRatenum = document.getElementById("cleavage_rate_number").value;
		var cleavageRatePerc = cleavageRatenum/totOocyte;
		cleavageRatePerc = Math.round(cleavageRatePerc*100);
		document.getElementById("cleavageRatePerc").textContent = cleavageRatePerc;
		document.getElementById("cleavage_rate_perc").value = cleavageRatePerc;
	}
}