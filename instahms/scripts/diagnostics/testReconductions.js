
function prevTestsValidation(name) {
	var print = name == 'SavePrint'?'Y':'N';
	var category = document.ipform.category;
	var path = '';

	var valid = checkRemarks();//mandetory for test in Revision Phase/Reconduction after Signoff
	valid = valid && validateReconduction();//alert user to pick atleast one test

	if( valid ){
		if (category.value == 'DEP_LAB')
			path = cpath + '/DiagnosticLabModule/Lab'+ ( regType == 'incoming' ? 'Incoming':'' )+'ReconductTestList.do';
		else
			path = cpath + '/DiagnosticLabModule/Radiology'+ ( regType == 'incoming' ? 'Incoming':'' )+'ReconductTestList.do';
		document.ipform.action = path;
		document.ipform.reconduction.value = reconductStatus;
		document.ipform.submit();
	}
}

function validateReconduction(){
	var reconducted = document.getElementsByName("reconduct");
	var valid = false;
	for(var i = 0;i<reconducted.length;i++){
		if ( reconducted[i].checked ){
			return true;
		}
	}

	if ( !valid ) {
		showMessage("js.laboratory.radiology.reconducttest.notest");
		return false;
	}
}

function checkRemarks(){
	var recoducted = document.getElementsByName("reconduct");
	var conductionStatus = document.getElementsByName("conductionStatus");
	var remarks = document.getElementsByName("reconducted_reason");
	var statusArry = ["S","RP","RC","RV"];
	for(var i = 0;i<recoducted.length;i++){
		if( recoducted[i].checked && statusArry.contains(conductionStatus[i].value) && empty(remarks[i].value) ){
			showMessage("js.laboratory.radiology.reconducttest.remarks.notbeempty");
			return false;
		}
	}

	return true;
}

Array.prototype.contains = function(element){
	for (var i = 0; i < this.length; i++) {
		if (this[i] == element)
			return true;
	}
}

function toggleConduction(index,value) {
	var checkBoxObj = document.getElementById("sample"+index);
	reconductStatus = true;

	if(checkBoxObj.checked){

		document.getElementById("isWithNewSample"+index).value = 'NO SAMPLE';
	}else{
		document.getElementById("isWithNewSample"+index).value = 'NO CHANGE';
	}
}

function changeReConduction(index,value){
    document.getElementById("uncheck"+index).checked=false;
	var varible = "isWithNewSample"+index;
	reconductStatus = true;
	document.getElementById(varible).value = value;
}

function unCheck(index,value){
       reconductStatus=false;
       document.getElementById("isWithNewSample"+index).value = 'NO CHANGE';
	document.getElementById("existingSample"+index).checked=false;
	document.getElementById("newSample"+index).checked=false;
	document.getElementById("uncheck"+index).checked=false;
}

function markAllForReconduction(markAll){
	var reconductCkBxEls = document.getElementsByName("reconduct");
	var marked_for_reconduction = document.getElementsByName("marked_for_reconduction");
	for(var i = 0;i<reconductCkBxEls.length;i++){
		reconductCkBxEls[i].checked = !reconductCkBxEls[i].disabled && markAll.checked;
		marked_for_reconduction[i].value = reconductCkBxEls[i].checked ? 'Y' : 'N';
	}
}

function markForReconduction(ckBxEl){
	var cell = getThisCell(ckBxEl);
	var children = Dom.getChildren(cell);
	children[0].checked = ckBxEl.checked;
	children[1].value = children[0].checked ? 'Y' : 'N';
}
