function keepBackUp(){
	if(neworedit == 'edit'){
			backupName = document.testEquipmentMaster.equipment_name.value;
	}
}


function validate() {

	document.getElementById('equipment_name').value = document.getElementById('equipment_name').value.trim();
	if (document.getElementById('equipment_name').value == '') {
		alert('Please enter equipment name');
		document.getElementById('equipment_name').focus();
		return false;
	}
	var center = document.getElementById('center_id');
	if (center && center.value == '') {
		alert("Please select the center");
		center.focus();
		return false;
	}
	
	var overbook = document.forms[0].overbook_limit.value;
	
	if(overbook.length > 10){
		alert("Please enter only 10 digits number for overbook Limit");
		document.forms[0].overbook_limit.focus();
		return false;
	}
	
	if (!checkDuplicate()) return false;

	return true;
}
function checkDuplicate(){
var newEquipmentName = trimAll(document.testEquipmentMaster.equipment_name.value);

	if(neworedit != 'edit'){
		for(var i=0;i<chkEquipmentName.length;i++){
			item = chkEquipmentName[i];
				if (newEquipmentName.toLowerCase() == item.equipment_name.toLowerCase()){
					alert(document.testEquipmentMaster.equipment_name.value+" already exists pls enter other name...");
			    	document.testEquipmentMaster.equipment_name.value='';
			    	document.testEquipmentMaster.equipment_name.focus();
			    	return false;
				}
		}
	}
	return true;
}

function initAddDialog() {
	addDialog = new YAHOO.widget.Dialog("addDialog", { width:"350px",
			context: ["resultsTable", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
			});
	document.getElementById("addDialog").style.display = 'block';
	addDialog.render();
	addDialog.cancelEvent.subscribe(onAddDialogCancel);
}

function onAddDialogCancel() {
	addDialog.hide();
}

function showAddDialog(obj){
	addDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	addDialog.show();
	document.getElementById("addResults").focus();
}
var resultsAutoComp = null;
var resultLabel_id = null;
var resultLabel = null;
var resultUnits = null;

function initResultsAutoComplete() {

	if (resultsAutoComp != null)
			resultsAutoComp.destroy();
	
	var ds = new YAHOO.util.LocalDataSource({result: allResults});
	ds.responseType =  YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = { resultsList : "result",
			fields: [ {key: "name"},
			          {key: "test_name"},
			          {key: "id"},
			          {key: "units"}
			        ],
		numMatchFields: 2
	};

	this.resultsAutoComp = new YAHOO.widget.AutoComplete('addResults', 'addResultsAcDropdown',	ds);
	this.resultsAutoComp.typeAhead = false;		// needed because we do any word beginning match.
	this.resultsAutoComp.useShadow = true;
	this.resultsAutoComp.allowBrowserAutocomplete = false;
	this.resultsAutoComp.maxResultsDisplayed = 50;
	this.resultsAutoComp.autoHighlight = true;
	this.resultsAutoComp.resultTypeList = false;
	this.resultsAutoComp.minQueryLength = 1;
	this.resultsAutoComp.forceSelection = true;

	this.resultsAutoComp.filterResults = Insta.queryMatchWordStartsWith;

	this.resultsAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);
		return highlightedValue ;
	}

	this.resultsAutoComp.itemSelectEvent.subscribe(resultSelectHandler, this, true);
	this.resultsAutoComp.unmatchedItemSelectEvent.subscribe(clear, this, true);
	this.resultsAutoComp.textboxKeyEvent.subscribe(clear, this, true);
}

function resultSelectHandler(sType, aArgs) {
	var result = aArgs[2];
	resultLabel_id = result.id;
	resultLabel = result.name;
	resultUnits = result.units;
	document.getElementById("rUnits").innerHTML = resultUnits;
}

function clear() {
	resultLabel_id = null;
	resultLabel = null;
	resultUnits = null;
}


function onAdd(order) {
	if (empty(resultLabel_id)) {
		alert("Please enter result name");
		document.getElementById('addResults').focus();
		return false;
	}
	var table = document.getElementById("resultsTable");
	var numRows = table.rows.length;
	var templateRow = table.rows[numRows-1];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.className = "added";

	//setting nodes
	setNodeText(row.cells[0], resultLabel);
	setNodeText(row.cells[1], resultUnits);

	//set hidden values
	var index = numRows-1;
	var cell = document.getElementById("resultsTable").rows[index].cells[0];

	// the field names must match the db field names
	cell.appendChild(makeHidden('equipment_id', 'equipment_id', document.getElementById("eq_id").value));
	cell.appendChild(makeHidden('resultlabel_id', 'resultlabel_id', resultLabel_id));
	cell.appendChild(makeHidden('new', 'new', "Y"));
	cell.appendChild(makeHidden('deleted', 'deleted', ""));

	restartDialog();
	return numRows-1;
}

function deleteResult(img){
	var row = getThisRow(img);
	var deleted = getElementByName(row, 'deleted');
	var trashImg = row.cells[2].getElementsByTagName("img")[0];
	if( deleted.value == '' ){
		trashImg.src = cpath+"/icons/undo_delete.gif";
		deleted.value = "Y";
	}else
		trashImg.src = cpath+"/icons/delete.gif";
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function restartDialog(){
	this.addDialog.cfg.setProperty("context", [document.getElementById("btnAddItem"), "tr", "tr"], false);
	document.getElementById("addResults").value = "";
	document.getElementById("rUnits").innerHTML = "";
	document.getElementById("addResults").focus();
}

function changeCheckboxValues() {
	var schedule = document.testEquipmentMaster.schedule;
	if (schedule.checked)
		document.testEquipmentMaster.overbook_limit.disabled = false;
		else
			document.testEquipmentMaster.overbook_limit.disabled = true;
			document.testEquipmentMaster.overbook_limit.value=0;
}