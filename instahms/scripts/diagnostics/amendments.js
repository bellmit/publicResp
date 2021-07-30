
//amending phase will need this


function initAmendReasonDialog() {

	var dialogDiv = document.getElementById("amendmentReasonDialog");
	dialogDiv.style.display = 'block';
	amendReasonDialog = new YAHOO.widget.Dialog("amendmentReasonDialog",{
			width:"250px",
			text: "Amendment Reason",
			context :["amendment_reason", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onCancelAmendDialog,
	                                                scope:amendReasonDialog,
	                                                correctScope:true } );
	amendReasonDialog.cfg.queueProperty("keylisteners", escKeyListener);
	amendReasonDialog.render();

}

function onCancelAmendDialog() {
	amendReasonDialog.hide();
}

var amendFormat = 'V';
function askAmendmentReason(obj,format,commonIndex){
	amendFormat = format;
	getAmendemntReasonDialog(obj,commonIndex,true);
}

function addNewResultRow(index){
	var remarkIdx=7, origRamarkIdx=8, origAmendBtIdx=2;
		if (methodologyExists == 'Y')
			remarkIdx=8, origRamarkIdx=9, origAmendBtIdx=3;

	var table = document.getElementById("resultsTable");
	var row = table.rows[index];
	var nextRow = row.cloneNode(true);

	table.tBodies[0].insertBefore(nextRow,row);
	var remarksCell = Dom.getChildren(nextRow.cells[remarkIdx]);
	var defaultValue = "";

	//new row should be in fresh state
	refreshRevisedRow( nextRow,row );
	//default value should be shown
	defaultValue = getElementByName(nextRow, "defaultValue").value;
	if (!empty(defaultValue)) {
		var obj = getElementByName(nextRow, "dum_resultvalue");
		var height = (defaultValue.length/30)*18;
		obj.value = defaultValue;
		obj.setAttribute("style", "width: 200px; height: "+height+"; background-color: ");
		getElementByName(nextRow, "resultvalue").value = defaultValue;
	}	
	var remarksCellExists = (remarksCell[0].name == 'otherDetails');
	var originalRemarksCell = Dom.getChildren(row.cells[origRamarkIdx]);
	var orginalAmendBtCell = Dom.getChildren(row.cells[origAmendBtIdx]);
	orginalAmendBtCell[0].disabled = true;
	getElementByName(row,"test_detail_status").value = 'A';
	getElementByName(row,"dum_resultvalue").readOnly = true;
	getElementByName(row,"dum_resultvalue").disabled = true;
	getElementByName(row,"withinNormal").disabled = true;
	originalRemarksCell[0].innerHTML = '';//other details r not editable

	row.style.textDecoration = 'line-through';

    //test conduction status depends on new result
	var completedHiddenCells = document.getElementsByName("completed");
	var completedSelectBx = document.getElementsByName("dum_completed");
	var prescribedIdCells = document.getElementsByName("prescribedid");

	NoCompleteRvalidateCkBx(revertAmendments());
}

function refreshRevisedRow( rRow,aRow ) {

	getElementByName(rRow,"withinNormal").value = '';
	getElementByName(rRow,"seviarity").value = '';
	getElementByName(rRow,"withinNormal").disabled = false;
	getElementByName(rRow,"test_detail_status").value = 'RP';
	getElementByName(rRow,"test_details_id").value = '';
	getElementByName(rRow,"original_test_details_id").value = getElementByName(aRow,"test_details_id").value;
	getElementByName(rRow,"dum_resultvalue").value = '';
	getElementByName(rRow,"resultvalue").value = '';
	if ( !empty(getElementByName(rRow,"dum_resultvalue")) )
		getElementByName(rRow,"dum_resultvalue").style.backgroundColor = '';
	if( empty(trim(getElementByName(aRow,"expression").value)) ){
		getElementByName(rRow,"dum_resultvalue").readOnly = false;
		getElementByName(rRow,"dum_resultvalue").disabled = false;
	}
	getElementByName(rRow,"amendBtn").value = 'Delete';
	getElementByName(rRow,"amendBtn").setAttribute("onclick","deleteRow(this)");
	var amendCellChilds = Dom.getChildren(getThisCell(getElementByName(rRow,"amendBtn")));
	amendCellChilds[1].innerHTML = '';//amendment reason box is not needed for revised result
	getElementByName(rRow,"amendment_reason").value = '';
	getElementByName(rRow,"newResult").value = 'Y';
	rRow.getElementsByTagName("div")[0].style.display = 'block';
	getElementByName(rRow,"remarks").value = '';
	getElementByName(rRow,"resultDisclaimer").value = '';

}
function deleteRow(newResult){
	var table = getThisTable(newResult);
	var row = getThisRow(newResult);
	var index = row.rowIndex;
	var originalResultRowIndex = index+1;


	var completedHiddenCells = document.getElementsByName("completed");
	var completedSelectBx = document.getElementsByName("dum_completed");
	var prescribedIdCells = document.getElementsByName("prescribedid");
	var testDetailsId = document.getElementsByName("test_details_id");
	var testDetailsStatus  = document.getElementsByName("test_detail_status");

	var orginalAmendBtCells = document.getElementsByName("amendBtn");
	if(getElementByName(row,"newResult").value == 'Y'){
		var newTestDetailId = getElementByName(row,"test_details_id").value;
		table.tBodies[0].removeChild(row);
		originalResultRowIndex = originalResultRowIndex-1;
		row = table.rows[originalResultRowIndex];
		row.style.textDecoration = '';
		getElementByName( row ,"amendBtn" ).disabled = false;
		getElementByName( row,"test_detail_status").value = 'S';
		getElementByName( row,"amendment_reason").value = '';
		getElementByName(row,"deleted_new_test_details_id").value = newTestDetailId;

	}else{
		if(getElementByName(row,"deleted").value == 'Y'){
			getElementByName(row,"deleted").value = "N";
			YAHOO.util.Dom.removeClass(row, 'cancelled');
		}else{
			getElementByName(row,"deleted").value = "Y";
			YAHOO.util.Dom.addClass(row, 'cancelled');
		}

	}
	NoCompleteRvalidateCkBx(revertAmendments());
}

function addNewTemplateRow(index){
	var table = document.getElementById("templatesTable");
	var row = table.rows[index];
	var nextRow = row.cloneNode(true);

	table.tBodies[0].insertBefore(nextRow,row);

	//amendement reason into amended row
	getElementByName(row,"amendment_reason").value= document.getElementById("eAmendmentReason").value;

	//new row should be in fresh state
	refreshRevisedRow( nextRow,row );

	var nextrowTemlateDivs = nextRow.cells[1].getElementsByTagName("div");
	nextrowTemlateDivs[0].style.display = 'block';//editable template link
	nextrowTemlateDivs[1].style.display = 'none';//disabled templeate link

	var nextrowTemlateListDivs = nextRow.cells[3].getElementsByTagName("div");
	for(var i =0;i<nextrowTemlateListDivs.length;i++){//choose another template action for newly added row
		if(nextrowTemlateListDivs[i].id == getElementByName(nextRow,"showAnotherTemplate").value){
			nextrowTemlateListDivs[i].style.display = 'block';
			nextrowTemlateListDivs[0].style.display = 'block';
		}
	}
	var nextrowTemlateDivs = getElementByName(nextRow,"test_details_id").value;

	var orginalAmendBtCell = Dom.getChildren(row.cells[2]);
	orginalAmendBtCell[0].disabled = true;
	row.style.textDecoration = 'line-through';

	var amendedEls = document.getElementsByName("test_details_id");
	var detailStatusEls = document.getElementsByName("test_detail_status");
	for(var i =0;i<amendedEls.length;i++){
		if ( amendedEls[i].value == getElementByName(row,"test_details_id").value )
			detailStatusEls[i].value = 'A';
	}

    //test conduction status depends on new result
	var completedHiddenCells = document.getElementsByName("completed");
	var completedSelectBx = document.getElementsByName("dum_completed");
	var prescribedIdCells = document.getElementsByName("prescribedid");

	NoCompleteRvalidateCkBx(revertAmendments());
}


var addAmendedRow = false;
function getAmendemntReasonDialog(obj,commonIndex,addRow){
	addAmendedRow = addRow;
	var row = getThisRow(obj,'TR');
	var index = getRowIndex(row);
	document.getElementById("amendmentReasonDialog").display = 'block';
	document.getElementById("eAmendmentReason").value = getElementByName(row, "amendment_reason" ).value;
	document.getElementById("editedAmendIdx").value = row.rowIndex ;
	document.getElementById("amendingTable").value = getThisTable(obj).id;
	amendReasonDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	amendReasonDialog.show();
}

function setAmendedReason(){
	var index = document.getElementById("editedAmendIdx").value;
	var amendReason= document.getElementsByName("amendment_reason");
	var testDetailsStatus = document.getElementsByName("test_detail_status");
	var thisRow = document.getElementById(document.getElementById("amendingTable").value).rows[parseInt(index)];//it includes header row
	getElementByName(thisRow,"amendment_reason").value = document.getElementById("eAmendmentReason").value;

	if(empty(trim(document.getElementById("eAmendmentReason").value))){
		showMessage("js.laboratory.radiology.amendmentreason.cannotempty");
		return false;
	}

	onCancelAmendDialog();
	if(addAmendedRow){//no need add a row if already amended
		if(amendFormat == 'V')
			addNewResultRow(parseInt(index));
		else
			addNewTemplateRow(parseInt(index));
	}
}

function revertAmendments(){
	var amendBts = document.getElementsByName("amendBtn");
	var revert = true;
	for(var i = 0;i<amendBts.length;i++){
		if(amendBts[i].value == 'Delete'){
			revert = false;
			break;
		}
	}
	return revert;
}

function NoCompleteRvalidateCkBx(status){
	//comple all/validate all checkboxes are required
	document.getElementById("markAllCompleted").disabled = status;
	document.getElementById("markAllCompleted").checked = status;

	if ( document.getElementById("markAllValidated") ) {
		document.getElementById("markAllValidated").disabled = status;
		document.getElementById("markAllValidated").checked = status;
	}
}

