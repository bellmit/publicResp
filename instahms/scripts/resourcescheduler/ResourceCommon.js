//this method is for one table.
function getTemplateRowWithoutIndex() {
	// gets the hidden template row index: this follows header row + num Information.
	return getNumItemsWithoutIndex() + 1;
}

function getNumItemsWithoutIndex() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("resultTable").rows.length-2;
}

//calculating unavailable timings depending upon available timings.

function calculateUnavailableTimings(table,index,columnDayOfWeek) {
	var ids = table.rows.length-2;
	// -- deleting Not Available Rows from the grid... if exists.
	for (var t=ids;t>=1;t--) {
		var r = table.rows[t];
		var r_status = getElementByName(r,"availability_status").value;
		if (r_status == 'N') {
			table.deleteRow(t);
		}
	}

	var tableLength = table.rows.length-2;
	var longValuesArray = new Array();// creating the long values array of given available times.
	longValuesArray.length = tableLength;
	var outerArray = new Array();
		outerArray.length = tableLength;

	// -- taking Backup Of All Available Rows from the grid...
	for (var i=1;i<=tableLength;i++) {
		var rowObj = table.rows[i];
		var j=0;
		var innerArray = new Array();
			innerArray.length = 11;
		var d = new Date();
		var fromTime = getElementByName(rowObj,"from_time").value;
		var fromArr = fromTime.split(":");
			d.setHours(fromArr[0]);
			d.setMinutes(fromArr[1]);
			d.setSeconds(0);
		longValuesArray[i-1] = d.getTime();//pusing long values of corresponding available start time to the array.
		//in outerArray taking all available rows backup..
		innerArray[j] = d.getTime();j++;
		innerArray[j] = "false";j++;
		if (columnDayOfWeek == "Y")
			innerArray[j] = getElementByName(rowObj,"day_of_week").value;j++;
		innerArray[j] = getElementByName(rowObj,"from_time").value;j++;
		innerArray[j] = getElementByName(rowObj,"to_time").value;j++;
		innerArray[j] = getElementByName(rowObj,"availability_status").value;j++;
		innerArray[j] = getElementByName(rowObj,"center_id") != null ? getElementByName(rowObj,"center_id").value : '';j++;
		innerArray[j] = getElementByName(rowObj,"center_name") != null ? getElementByName(rowObj,"center_name").value:'';j++;
		innerArray[j] = getElementByName(rowObj,"visit_mode") !=null ? getElementByName(rowObj,"visit_mode").value : '';j++;
		innerArray[j] = getElementByName(rowObj,"remarks").value;j++;
		outerArray[i-1] = innerArray;
	}
	//sorting all available rows on from time.
	//ex.->suppose 16:00,18:00 and 08:00,10:00,and 12:00,14:00 are 3 sets of inputs then all sets are being
	//sorted out by from time.

	longValuesArray.sort();//sorting the long values of corresponding available start time.
	var finalArray = new Array();// this array will contain all available timings sorted by start time.
	for (var m=0;m<longValuesArray.length;m++) {
		for (var n=0;n<outerArray.length;n++) {
			if (outerArray[n][0] == longValuesArray[m]) {
				finalArray[m] = outerArray[n];
			}
		}
	}
	// deleting all available rows from grid after taking backup..
	for (var c=tableLength;c>=1;c--) {
		table.deleteRow(c);
	}

	// iterating through backup array which contains all availability details and
	// calculating the not available timings and then not available and available timings are appended to the grid.

	var p_endtime = null;
	var defaultStartTime = '00:00';
	var defaultEndTime = '23:59';
	for (var a =0;a<finalArray.length;a++) {
		var startTime = finalArray[a][3];
		var endTime = finalArray[a][4];
		var centerId = finalArray[a][6];
		var centerName = finalArray[a][7];
		var a_visitMode = finalArray[a][8];
		var a_remarks = finalArray[a][9];
		if (finalArray.length > 1) {
			for (var b=a+1;b<=a+1;b++) {
				if (b < finalArray.length) {
					var n_startTime = finalArray[b][3];
					var n_endTime = finalArray[b][4];
					var n_centerId = finalArray[b][6];
					var n_centerName = finalArray[b][7];
					var n_visitMode = finalArray[b][8];
					var n_remarks = finalArray[b][9];
					if (defaultStartTime != startTime && b == 1) {
						insertRowValues(table,index,"N",defaultStartTime,startTime,"","","","");
						insertRowValues(table,index,"A",startTime,endTime,centerId,centerName,a_visitMode,a_remarks);
					} else {
						if (b == 1)
							insertRowValues(table,index,"A",startTime,endTime,centerId,centerName,a_visitMode,a_remarks);
					}
					if (endTime != n_startTime) {
						insertRowValues(table,index,"N",endTime,n_startTime,"","","","");
						insertRowValues(table,index,"A",n_startTime,n_endTime,n_centerId,n_centerName,n_visitMode,n_remarks);
						p_endtime = n_endTime;
					} else {
						if (defaultEndTime != n_endTime && a == finalArray.length-1) {
							insertRowValues(table,index,"A",n_startTime,n_endTime,n_centerId,n_centerName,n_visitMode,n_remarks);
							insertRowValues(table,index,"N",n_endTime,defaultEndTime,"","","","");
							p_endtime = n_endTime;
						} else {
							insertRowValues(table,index,"A",n_startTime,n_endTime,n_centerId,n_centerName,n_visitMode,n_remarks);
							p_endtime = n_endTime;
						}
					}
				}
			}
			if (defaultEndTime != endTime && a == finalArray.length-1) {
				insertRowValues(table,index,"N",p_endtime,defaultEndTime,"","","","");
			} else {}
		} else {
			if (defaultStartTime != startTime) {
				insertRowValues(table,index,"N",defaultStartTime,startTime,"","","","");
				insertRowValues(table,index,"A",startTime,endTime,centerId,centerName,a_visitMode,a_remarks);
			} else {
				insertRowValues(table,index,"A",startTime,endTime,centerId,centerName,a_visitMode,a_remarks);
			}
			if (defaultEndTime != endTime) {
				insertRowValues(table,index,"N",endTime,defaultEndTime,"","","","");
			} else {}
		}
	}
}

  // function which will append a row to the grid depending upon availabilty status.

function insertRowValues(table,index,status,startTime,endTime,centerId,centerName,visitMode,remarks) {
	var templateRow = null;
	if (!empty(index+''))
		templateRow = table.rows[getTemplateRow(index)];
	else
		templateRow = table.rows[getTemplateRowWithoutIndex()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	
	if (status == 'N') {
		setNodeText(row.cells[FROM_TIME_COL], !empty(startTime) ? startTime : '');
		setNodeText(row.cells[TO_TIME_COL], !empty(endTime) ? endTime : '');
		setNodeText(row.cells[STATUS_COL], "Not Available");
		if (gResourceType == 'DOC' && max_centers_inc_default >1) {
			setNodeText(row.cells[CENTER_COL], !empty(centerName) ? centerName : '');
		}
		if(gResourceType == 'DOC'){
			setNodeText(row.cells[VISIT_MODE_COL], getVisitMode(visitMode));
		}
		setNodeText(row.cells[REMARKS_COL], remarks);
		getElementByName(row,"from_time").value = startTime;
		getElementByName(row,"to_time").value = endTime;
		getElementByName(row,"availability_status").value = "N";
		row.setAttribute("class","notAvailableRow");
		if (getElementByName(row,"center_id") != null) {
			getElementByName(row,"center_id").value = centerId;
			getElementByName(row,"center_name").value = centerName;
		}
		if(getElementByName(row,"visit_mode") != null)
			getElementByName(row,"visit_mode").value = visitMode;
		
		getElementByName(row,"remarks").value = remarks;
		if (!empty(index+''))
			getElementByName(row,"day_of_week").value = index;
		getElementByName(row,"default_value").value = "false";

		var disableDeleteImg = document.createElement("img");
			disableDeleteImg.setAttribute("src", cpath + "/icons/Delete1.png");
			disableDeleteImg.setAttribute("title", "");
			disableDeleteImg.setAttribute("onclick","");
			disableDeleteImg.setAttribute("class", "button");

		for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
			row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
		}
		row.cells[DELETE_COL].appendChild(disableDeleteImg);
	} else {
		setNodeText(row.cells[FROM_TIME_COL], !empty(startTime) ? startTime : '');
		setNodeText(row.cells[TO_TIME_COL], !empty(endTime) ? endTime : '');
		setNodeText(row.cells[STATUS_COL], "Available");
		if (gResourceType == 'DOC' && max_centers_inc_default >1) {
			setNodeText(row.cells[CENTER_COL], !empty(centerName) ? centerName : '');
		}
		if(gResourceType == 'DOC'){
			setNodeText(row.cells[VISIT_MODE_COL], getVisitMode(visitMode));
		}
		setNodeText(row.cells[REMARKS_COL], remarks);
		getElementByName(row,"from_time").value = startTime;
		getElementByName(row,"to_time").value = endTime;
		getElementByName(row,"availability_status").value = "A";
		if (getElementByName(row,"center_id") != null) {
			getElementByName(row,"center_id").value = centerId;
			getElementByName(row,"center_name").value = centerName;
		}
		if (getElementByName(row,"visit_mode") != null) 
			getElementByName(row,"visit_mode").value=visitMode;
		getElementByName(row,"remarks").value = remarks;
		if (!empty(index+''))
			getElementByName(row,"day_of_week").value = index;
		getElementByName(row,"default_value").value = "false";

		var deleteImg = document.createElement("img");
			deleteImg.setAttribute("src", cpath + "/icons/Delete.png");
			deleteImg.setAttribute("title", getString("js.scheduler.resourceavailability.deletetimingsrow"));
		if (!empty(index+''))
			deleteImg.setAttribute("onclick","deleteItem(this,"+index+')');
		else
			deleteImg.setAttribute("onclick","deleteItem(this)");
		deleteImg.setAttribute("class", "button");
		if(gResourceType != 'DOC'){
			for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
				row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
			}
			row.cells[DELETE_COL].appendChild(deleteImg);
		}
		//To show delete button disable for other center in user login center
		if(gResourceType == 'DOC'){
			var loginCenterId= document.getElementById("login_center_id").value;
			if(loginCenterId == 0 || centerId == loginCenterId){

				for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
					row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
				}
				row.cells[DELETE_COL].appendChild(deleteImg);
			}
			else{
				var disableDeleteImg = document.createElement("img");
				disableDeleteImg.setAttribute("src", cpath + "/icons/Delete1.png");
				disableDeleteImg.setAttribute("title", "");
				disableDeleteImg.setAttribute("onclick","");
				disableDeleteImg.setAttribute("class", "button");
				for (var i=row.cells[DELETE_COL].childNodes.length-1; i>=0; i--) {
					row.cells[DELETE_COL].removeChild(row.cells[DELETE_COL].childNodes[i]);
				}
				row.cells[DELETE_COL].appendChild(disableDeleteImg);
			}
		}
		
	}
	
	for (var i=row.cells[EDIT_COL].childNodes.length-1; i>=0; i--) {
		row.cells[EDIT_COL].removeChild(row.cells[EDIT_COL].childNodes[i]);
	}

	var editImg = document.createElement("img");
		editImg.setAttribute("src", cpath + "/icons/Edit.png");
		editImg.setAttribute("title", getString("js.scheduler.resourceavailability.edittimings"));
		editImg.setAttribute("name", "editIcon");
		editImg.setAttribute("id", "editIcon"+index);
		if (!empty(index+''))
			editImg.setAttribute("onclick","openEditResTimingsDialogBox(this," +index+')');
		else
			editImg.setAttribute("onclick","openEditResTimingsDialogBox(this)");
		editImg.setAttribute("class", "button");

	row.cells[EDIT_COL].appendChild(editImg);
}

/** 
 * function to enable or disable the visit
 * mode on the basis of doctor's availability 
 * */
function enableVisitMode(resourceName,doctorJSON,index){
	var doctorObject = filterList(doctorJSON,"doctor_id",resourceName);
	var mode = !doctorObject.length || doctorObject[0].available_for_online_consults != "Y";
	if(index !== undefined)
		document.getElementById('dialog_visit_mode'+index).disabled = mode;
	else
		document.getElementById('dialog_visit_mode').disabled = mode;	
}

var visitModes = {
	"I": "In Person",
	"O": "Online",
	"B": "Both"
};
function getVisitMode(visitMode){
	return visitModes.hasOwnProperty(visitMode) ?  visitModes[visitMode] : '';
}