function init(){
	initSIDialog();
	initEditSIDialog();
	fillharmonelevelDetails();
}

function AddRowLHL() {
	var LHLTable = document.getElementById("LHLTable");
	var len = LHLTable.rows.length;
	var templateRow = LHLTable.rows[len-2];
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
	inp.setAttribute("name", "delItemLHL");
	inp.setAttribute("id", "delItemLHL"+len);
	inp.setAttribute("onclick", "deleteItemLHL(this, "+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "lhldeleted");
    inp0.setAttribute("id", "lhldeleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var cell1 = document.createElement("TD");
    cell1.setAttribute("class", 'border');
    cell1.setAttribute("style", "width: 60px");
	var inp1 = document.createElement("INPUT");

    cell1.appendChild(inp1);

    var cell2 = document.createElement("TD");
    cell2.setAttribute("class", 'border');
    cell2.setAttribute("style", "width: 60px");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "text");
    inp2.setAttribute("name", "e2");
    inp2.setAttribute("size", "3");
    inp2.setAttribute("id", "e2"+len);
    inp2.setAttribute("style", "width: 60px");
    inp2.setAttribute("onkeypress", "return enterNumOnly(event)");
    inp2.setAttribute("onchange", "return makeingDec(this.value,this);");

    cell2.appendChild(inp2);

    var cell3 = document.createElement("TD");
    cell3.setAttribute("class", 'border');
    cell3.setAttribute("style", "width: 60px");
	var inp3 = document.createElement("INPUT");
    inp3.setAttribute("type", "text");
    inp3.setAttribute("name", "prog");
    inp3.setAttribute("size", "3");
    inp3.setAttribute("id", "prog"+len);
    inp3.setAttribute("style", "width: 60px");
	inp3.setAttribute("onkeypress", "return enterNumOnly(event)");
	inp3.setAttribute("onchange", "return makeingDec(this.value,this);");

    cell3.appendChild(inp3);

    var cell4 = document.createElement("TD");
    cell4.setAttribute("class", 'border');
    cell4.setAttribute("style", "width: 60px");
	var inp4 = document.createElement("INPUT");
    inp4.setAttribute("type", "text");
    inp4.setAttribute("name", "betahcg");
    inp4.setAttribute("size", "3");
    inp4.setAttribute("id", "betahcg"+len);
    inp4.setAttribute("style", "width: 60px");
    inp4.setAttribute("onkeypress", "return enterNumOnly(event)");
    inp4.setAttribute("onchange", "return makeingDec(this.value,this);");

    cell4.appendChild(inp4);

	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	row.appendChild(cell);
	var lhdel = document.getElementById("lhldeleted"+len);
    var lhdelTabOrder = parseInt(lhdel.getAttribute("tabindex"));

    var lhdt = "lhdate"+len;
    cell1.innerHTML = getDateWidget(lhdt, lhdt, null,null, null, true, true, lhdelTabOrder+1, cpath);
	makePopupCalendar(lhdt);
	document.getElementById(lhdt).setAttribute('style', 'width: 100px;');
}


function fillharmonelevelDetails() {
		for (var i =0; i<harmonelevelsDetails.length; i++) {
				var LHLTable = document.getElementById("LHLTable");
				var len = LHLTable.rows.length;
				var templateRow = LHLTable.rows[len-2];
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
				inp.setAttribute("name", "delItemLHL");
				inp.setAttribute("id", "delItemLHL" + len);
				inp.setAttribute("onclick", "deleteItemLHL(this, "+len+")");
				inp.setAttribute("style", "width:16px");
				cell.appendChild(inp);

				var inp0 = document.createElement("INPUT");
			    inp0.setAttribute("type", "hidden");
			    inp0.setAttribute("name", "lhldeleted");
			    inp0.setAttribute("id", "lhldeleted" + len);
			    inp0.setAttribute("value", "false");
			    cell.appendChild(inp0);

			    var cell1 = document.createElement("TD");
			    cell1.setAttribute("class", 'border');
			    cell1.setAttribute("style", "width: 60px");
				var inp1 = document.createElement("INPUT");

			    cell1.appendChild(inp1);

			    var cell2 = document.createElement("TD");
			    cell2.setAttribute("class", 'border');
			    cell2.setAttribute("style", "width: 60px");
				var inp2 = document.createElement("INPUT");
			    inp2.setAttribute("type", "text");
			    inp2.setAttribute("name", "e2");
			    inp2.setAttribute("size", "3");
			    inp2.setAttribute("id", "e2" + len);
			    inp2.setAttribute("value", harmonelevelsDetails[i].e2_value);
			    inp2.setAttribute("style","width:60px");
			    inp2.setAttribute("onkeypress", "return enterNumOnly(event)");
			    inp2.setAttribute("onchange", "return makeingDec(this.value,this);");
			    cell2.appendChild(inp2);

			    var cell3 = document.createElement("TD");
			    cell3.setAttribute("class", 'border');
			    cell3.setAttribute("style", "width: 60px");
				var inp3 = document.createElement("INPUT");
			    inp3.setAttribute("type", "text");
			    inp3.setAttribute("name", "prog");
			    inp3.setAttribute("size", "3");
			    inp3.setAttribute("id", "prog" + len);
			    inp3.setAttribute("value", harmonelevelsDetails[i].prog_value);
			    inp3.setAttribute("style","width:60px");
			    inp3.setAttribute("onkeypress","return enterNumOnly(event)");
			    inp3.setAttribute("onchange", "return makeingDec(this.value,this);");
			    cell3.appendChild(inp3);

			    var cell4 = document.createElement("TD");
			    cell4.setAttribute("class", 'border');
			    cell4.setAttribute("style", "width: 60px");
				var inp4 = document.createElement("INPUT");
			    inp4.setAttribute("type", "text");
			    inp4.setAttribute("name", "betahcg");
			    inp4.setAttribute("size", "3");
			    inp4.setAttribute("id", "betahcg" + len);
			    inp4.setAttribute("value", harmonelevelsDetails[i].beta_hcg_value);
			    inp4.setAttribute("style","width:60px");
			    inp4.setAttribute("onkeypress", "return enterNumOnly(event)");
			    inp4.setAttribute("onchange", "return makeingDec(this.value,this);");
			    cell4.appendChild(inp4);

				row.appendChild(cell1);
				row.appendChild(cell2);
				row.appendChild(cell3);
				row.appendChild(cell4);
				row.appendChild(cell);

				var lhdel = document.getElementById("lhldeleted"+len);
			    var lhdelTabOrder = parseInt(lhdel.getAttribute("tabindex"));

			    var lhdt = "lhdate"+len;
			    cell1.innerHTML = getDateWidget(lhdt, lhdt, new Date(harmonelevelsDetails[i].blood_test_date),null, null, true, true, lhdelTabOrder+1, cpath);
				makePopupCalendar(lhdt);
				document.getElementById(lhdt).setAttribute('style', 'width: 100px;');
			}
	}

	function deleteItemLHL(checkBox, rowId) {
	var itemListTable = document.getElementById("LHLTable");
	var row = itemListTable.rows[rowId];
	var deletedInput =
		document.getElementById('lhldeleted' + rowId).value = document.getElementById('lhldeleted' + rowId).value == 'false' ? 'true' : 'false';
	if (deletedInput == 'true') {
		addClassName(document.getElementById(rowId), "delete");
		document.getElementById('delItemLHL'+rowId).src = cpath+'/icons/Deleted.png';
	} else {
		removeClassName(document.getElementById(rowId), "delete");
		document.getElementById('delItemLHL'+rowId).src = cpath+'/icons/Delete.png';
	}
}

function funValidateAndSubmit() {
	var LHLTable = document.getElementById("LHLTable");
	var len = LHLTable.rows.length-3;
	for(var i=0;i<len;i++){
		if(validateDateFormat(document.getElementById('lhdate'+i).value)!=null){
			alert("Please select correct date format to continue..");
			return false;
		}
	}
	var method = document.treatmentForm._method.value;
	var ivfCycleID = document.treatmentForm.ivf_cycle_id.value;
	var mrNo = document.treatmentForm.mr_no.value;;
	var patientID = document.treatmentForm.patient_id.value;
	document.treatmentForm.action=cpath+"/IVF/IVFCycleCompletion.do?_method=saveLutealDetails&ivf_cycle_id="+ivfCycleID+"&mr_no="+mrNo+
		"&patient_id="+patientID;
	document.treatmentForm.submit();
}