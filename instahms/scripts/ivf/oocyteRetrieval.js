function init(){
	calDuration();
	filloocyteRetrievalDetails();
}

function AddRowOocyte() {
	var OTABLE = document.getElementById("OTABLE");
	var len = OTABLE.rows.length;
	var templateRow = OTABLE.rows[len-2];
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
	inp.setAttribute("name", "delItemO");
	inp.setAttribute("id", "delItemO"+len);
	inp.setAttribute("onclick", "deleteItemO(this, "+len+")");
	inp.setAttribute("style", "width: 16px");
	cell.appendChild(inp);

	var inp0 = document.createElement("INPUT");
    inp0.setAttribute("type", "hidden");
    inp0.setAttribute("name", "odeleted");
    inp0.setAttribute("id", "odeleted"+len);
    inp0.setAttribute("value", "false");
    cell.appendChild(inp0);

    var cell1 = document.createElement("TD");
    cell1.setAttribute("class", 'border');
    cell1.setAttribute("style", "width: 60px");
	var inp1 = document.createElement("INPUT");
    inp1.setAttribute("type", "text");
    inp1.setAttribute("name", "otype");
    inp1.setAttribute("size", "3");
    inp1.setAttribute("id", "otype"+len);
    inp1.setAttribute("style", "width: 60px");
    cell1.appendChild(inp1);

    var cell2 = document.createElement("TD");
    cell2.setAttribute("class", 'border');
    cell2.setAttribute("style", "width: 60px");
	var inp2 = document.createElement("INPUT");
    inp2.setAttribute("type", "text");
    inp2.setAttribute("name", "onumber");
    inp2.setAttribute("size", "3");
    inp2.setAttribute("id", "onumber"+len);
    inp2.setAttribute("onchange", "return getTotalOocyte()");
    inp2.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
    inp2.setAttribute("style", "width: 60px");

    cell2.appendChild(inp2);

	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell);
}


function filloocyteRetrievalDetails() {
		for (var i =0; i<retrievalDetails.length; i++) {
				var OTABLE = document.getElementById("OTABLE");
				var len = OTABLE.rows.length;
				var templateRow = OTABLE.rows[len-2];
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
				inp.setAttribute("name", "delItemO");
				inp.setAttribute("id", "delItemO" + len);
				inp.setAttribute("onclick", "deleteItemO(this, "+len+")");
				inp.setAttribute("style", "width:16px");
				cell.appendChild(inp);

				var inp0 = document.createElement("INPUT");
			    inp0.setAttribute("type", "hidden");
			    inp0.setAttribute("name", "odeleted");
			    inp0.setAttribute("id", "odeleted" + len);
			    inp0.setAttribute("value", "false");
			    cell.appendChild(inp0);

			    var cell1 = document.createElement("TD");
			    cell1.setAttribute("class", 'border');
			    cell1.setAttribute("style", "width: 60px");
				var inp1 = document.createElement("INPUT");
			    inp1.setAttribute("type", "text");
			    inp1.setAttribute("name", "otype");
			    inp1.setAttribute("size", "3");
			    inp1.setAttribute("id", "otype" + len);
			    inp1.setAttribute("value", retrievalDetails[i].oocyte_type);
			    inp1.setAttribute("style","width:60px");
			    cell1.appendChild(inp1);

			    var cell2 = document.createElement("TD");
			    cell2.setAttribute("class", 'border');
			    cell2.setAttribute("style", "width: 60px");
				var inp2 = document.createElement("INPUT");
			    inp2.setAttribute("type", "text");
			    inp2.setAttribute("name", "onumber");
			    inp2.setAttribute("size", "3");
			    inp2.setAttribute("id", "onumber" + len);
			    inp2.setAttribute("value", retrievalDetails[i].oocyte_number);
			    inp2.setAttribute("onchange", "return getTotalOocyte()");
			    inp2.setAttribute("onkeypress", "return enterNumOnlyzeroToNine(event)");
			    inp2.setAttribute("style","width:60px");
			    cell2.appendChild(inp2);

				row.appendChild(cell1);
				row.appendChild(cell2);
				row.appendChild(cell);
			}
			getTotalOocyte();
	}

	function deleteItemO(checkBox, rowId) {
		var itemListTable = document.getElementById("OTABLE");
		var row = itemListTable.rows[rowId];
		var deletedInput =
			document.getElementById('odeleted' + rowId).value = document.getElementById('odeleted' + rowId).value == 'false' ? 'true' : 'false';
		if (deletedInput == 'true') {
			addClassName(document.getElementById(rowId), "delete");
			document.getElementById('delItemO'+rowId).src = cpath+'/icons/Deleted.png';
		} else {
			removeClassName(document.getElementById(rowId), "delete");
			document.getElementById('delItemO'+rowId).src = cpath+'/icons/Delete.png';
		}
		getTotalOocyte();
	}

	function getTotalOocyte(){

		var otype = document.CycleCompletion.otype;
		var onumber = document.CycleCompletion.onumber;
		var odeleted = document.CycleCompletion.odeleted;
		var totalOocyte = 0;

		if(otype!=null && onumber!=null && otype.length>1 && onumber.length>1) {
			for(var i=0;i<otype.length;i++){
				if(odeleted[i].value!='true')
				totalOocyte = totalOocyte + parseInt(onumber[i].value);
			}
		}else {
			if(odeleted!=null && odeleted.value!='true')
				totalOocyte = onumber.value;
		}
			document.getElementById("total_oocyte").textContent = totalOocyte;
	}

	function calDuration(){
		var opuDateObj = document.CycleCompletion.opuDate;
		var startTimeObj = document.CycleCompletion.startTime;
		var endTimeObj = document.CycleCompletion.endTime;

		if(!validateTime(startTimeObj)){
			document.CycleCompletion.startTime.value='';
			return false;
		}
		if(!validateTime(endTimeObj)){
			document.CycleCompletion.endTime.value='';
			return false;
		}
		if(opuDateObj.value!='' && startTimeObj.value!='' && endTimeObj.value!='') {
			var startDateTime = getDateTimeFromField(opuDateObj,startTimeObj);
			var endDateTime = getDateTimeFromField(opuDateObj,endTimeObj);

			var diff = endDateTime - startDateTime;
			diff = parseFloat(diff/3600).toFixed(2);
			document.getElementById("opu_duration").value = diff/1000;
			document.getElementById("opu_duration").textContent = diff/1000 +' hrs';
		}else {
			document.getElementById("opu_duration").value = '';
			document.getElementById("opu_duration").textContent = '';
		}
	}