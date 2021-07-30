var addCounterMappingDialog;

function init(){
	initDialog();
}
function addCounterMapping(obj) {
	document.getElementById('addCounterMappingDialog').style.display='block';
	document.getElementById('addCounterMappingDialog').style.visibility='visible';
	var button = document.getElementById("addCounter");
	addCounterMappingDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	addCounterMappingDialog.show();
}	

function initDialog() {
	addCounterMappingDialog = new YAHOO.widget.Dialog('addCounterMappingDialog', {
        width:"500px",
        visible: false,
        modal: true,
        constraintoviewport: true

    });
    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                          { fn:handleDialogCancel,
                                            scope:addCounterMappingDialog,
                                            correctScope:true } );
    var entKeyListener = new YAHOO.util.KeyListener(document, { keys:13 },
                                          { fn:addToTable,
                                            scope:addCounterMappingDialog,
                                            correctScope:true } );
    addCounterMappingDialog.cfg.queueProperty("keylisteners", [escKeyListener,entKeyListener]);
    addCounterMappingDialog.cancelEvent.subscribe(handleDialogCancel);
    addCounterMappingDialog.render();
}


function handleDialogCancel() {
	document.getElementById('addCounterMappingDialog').style.display = 'none';
	document.getElementById('addCounterMappingDialog').style.visibility = 'hidden';
	document.getElementById("user_center").options.selectedIndex = 0;
	addCounterMappingDialog.hide();
}

function addToTable() {
	var mappingCenterId = document.getElementById('user_center').options[document.getElementById('user_center').selectedIndex].value;
	var mappingCenterName = document.getElementById('user_center').options[document.getElementById('user_center').selectedIndex].text;
	if (trim(mappingCenterId) == '') {
		alert('Please Select a Center, Or Close it');
		document.getElementById("user_center").focus();
		return false;
	} 
	var alreadyMappedCenterIds = document.getElementsByName("center_id");
	for (var i=0;i<alreadyMappedCenterIds.length;i++) {
		if(alreadyMappedCenterIds[i].value == mappingCenterId) {
			alert("Duplicate Center mapping not allowed");
			document.getElementById("user_center").options.selectedIndex = 0;
			document.getElementById("user_counter").options.selectedIndex = 0;
			document.getElementById("user_center").focus();
			return false;
		}
	}
	
	var mappingCounterId = document.getElementById('user_counter').options[document.getElementById('user_counter').selectedIndex].value;
	var mappingCounterName = document.getElementById('user_counter').options[document.getElementById('user_counter').selectedIndex].text;
	if (trim(mappingCounterId) == '') {
		alert('Please Select a Counter, Or Close it');
		document.getElementById("user_center").focus();
		return false;
	} 
    insertNewRow(mappingCenterId, mappingCenterName, mappingCounterId, mappingCounterName);
    document.getElementById("user_center").options.selectedIndex = 0;
    document.getElementById("user_counter").options.selectedIndex = 0;
}
function insertNewRow(centerId, centerName, counterId, counterName) {		
	var tableObj = document.getElementById('mappedBillingCounterId');
	var len = tableObj.rows.length;
	var templateRow = tableObj.rows[len-1];
   	var row = '';
   	row = templateRow.cloneNode(true);
	row.style.display = '';
	YAHOO.util.Dom.insertBefore(row, templateRow);

	
	var x1 = document.createElement("INPUT");
	x1.setAttribute("type", "hidden");
	x1.setAttribute("name", "center_counter_id");
	x1.setAttribute("value", "");
	
	var x2 = document.createElement("INPUT");
	x2.setAttribute("type", "hidden");
	x2.setAttribute("name", "emp_username");
	x2.setAttribute("value", document.billingCounterCenterMappingForm.userName.value);
	
	var x3 = document.createElement("INPUT");
	x3.setAttribute("type", "hidden");
	x3.setAttribute("name", "center_id");
	x3.setAttribute("value", centerId);
	
	var x4 = document.createElement("INPUT");
	x4.setAttribute("type", "hidden");
	x4.setAttribute("name", "deleted");
	x4.setAttribute("id", centerId+"_deleted");
	x4.setAttribute("value", false);
	
	var x5 = document.createElement("INPUT");
	x5.setAttribute("type", "hidden");
	x5.setAttribute("name", "counter_id");
	x5.setAttribute("value", counterId);
	
	var x6 = document.createElement("INPUT");
	x6.setAttribute("type", "hidden");
	x6.setAttribute("name", "default_counter");
	x6.setAttribute("value", true);
	
	var x7 = document.createElement("INPUT");
	x7.setAttribute("type", "hidden");
	x7.setAttribute("name", "created_by");
	x7.setAttribute("value", document.billingCounterCenterMappingForm.loggedInUserid.value);

	var cell1 = document.createElement("TD");
	cell1.innerHTML = centerName;
	cell1.appendChild(x1);
	cell1.appendChild(x2);
	cell1.appendChild(x3);
	cell1.appendChild(x4);
	cell1.appendChild(x5);
	cell1.appendChild(x6);
	cell1.appendChild(x7);

	var cntrLbl = document.createElement("LABEL");
    var textTreatLbl = document.createTextNode(counterName);
    cntrLbl.appendChild(textTreatLbl);
	
	var cell2 = document.createElement("TD");
	cell2.appendChild(cntrLbl);
	
	var cell3 = document.createElement("TD");
	cell3.setAttribute("style", "text-align:right;");
    var img = document.createElement("img");
    img.setAttribute("src", cpath + "/icons/delete.gif");
    var anchor = document.createElement("A");
    anchor.setAttribute("name", "trashIcon");
    anchor.setAttribute("href", "javascript:Cancel Item");
    anchor.setAttribute("title", "Cancel Billing Counter Mapping");
    anchor.setAttribute("onclick", " return changeElsColor('"+centerId+"_deleted', this);");
    anchor.appendChild(img);
    cell3.appendChild(anchor);
	
	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	
}

function changeElsColor(elementId, obj) {

var trObj = getThisRow(obj);
var trashimgObj = trObj.cells[2].getElementsByTagName("img")[0];
var markRowForDelete = document.getElementById(elementId).value == 'false' ? 'true'
		: 'false';
document.getElementById(elementId).value = document
		.getElementById(elementId).value == 'false' ? 'true'
		: 'false';

if (markRowForDelete == 'true') {
	trashimgObj.src = cpath + '/icons/undo_delete.gif';
} else {
	trashimgObj.src = cpath + '/icons/delete.gif';
}
return false;
}

function populateCounters(thisData) {
	var i=0;
	var counterSelect = document.createElement('select');
	var prevCounterSel = document.getElementById('user_counter');
	prevCounterSel.parentNode.replaceChild(counterSelect,prevCounterSel);
	counterSelect.id = "user_counter";
	counterSelect.name = "user_counter";
	counterSelect.setAttribute ('class','dropdown');
	counterSelect.disabled = true;
	var selectedCenterId = thisData.options[thisData.selectedIndex].value;
	for(var j=0; j<countersList.length; j++) {
		var catEle = countersList[j];
		if(catEle.center_id == selectedCenterId) {
			counterSelect.removeAttribute("disabled");
			counterSelect.length = i;
			var catOpt = document.createElement('option');
			catOpt.text = catEle.counter_no;
			catOpt.value = catEle.counter_id;
			counterSelect.appendChild(catOpt);
			i++
		}
	}
	if(counterSelect.length< 1) {
		counterSelect.title = 'No Counter is created for Selected Center...';
	 	var catEmptOpt = document.createElement('option');
		catEmptOpt.text = 'No Counter Avlb.';
		catEmptOpt.value = '';
		counterSelect.appendChild(catEmptOpt);
	} else {
		counterSelect.title = '';
	}
	sortDropDown(counterSelect);
}
function validateForm(){
	var mappedCounterId = document.getElementsByName("center_counter_id");
	if(mappedCounterId.length == 0) {
		alert("Please add Counter Mapping for save the Page");
		return false;
	}
	document.billingCounterCenterMappingForm.submit();
	return true;
}