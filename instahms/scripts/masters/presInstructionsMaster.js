function validate() {

	var prescriptionInstructionName = document.getElementById('instruction_desc').value.trim();
	if (empty(prescriptionInstructionName)) {
		alert('Please enter Prescription Instruction.');
		document.getElementById('instruction_desc').focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	return true;
}

function checkDuplicate() {

	var newPrescriptionInstructionName = trimAll(document.prescriptionInstructionsMaster.instruction_desc.value);

	if(document.prescriptionInstructionsMaster._method.value != 'update') {
		for(var i=0;i<chkPrescriptionInstructions.length;i++){
			item = chkPrescriptionInstructions[i];
			if (newPrescriptionInstructionName == item.instruction_desc){
				alert("\""+document.prescriptionInstructionsMaster.instruction_desc.value+"\" already exists please enter other Instruction...");
		    	document.prescriptionInstructionsMaster.instruction_desc.value='';
		    	document.prescriptionInstructionsMaster.instruction_desc.focus();
		    	return false;
			}
		}
	}

	if(document.prescriptionInstructionsMaster._method.value == 'update') {
	  		if (backupName != newPrescriptionInstructionName){
				for(var i=0;i<chkPrescriptionInstructions.length;i++){
					item = chkPrescriptionInstructions[i];
					if(newPrescriptionInstructionName == item.instruction_desc){
						alert("\""+document.prescriptionInstructionsMaster.instruction_desc.value+"\" already exists please enter other Instruction.");
				    	document.prescriptionInstructionsMaster.instruction_desc.focus();
				    	return false;
	  				}
	  			}
	 		}
	 	}
	return true;
}

var rAutoComp;
function autoPrescriptionInstructionsMaster() {
	var datasource = new YAHOO.util.LocalDataSource({result: prescriptionInstructions});
	datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	datasource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "instruction_desc"},{key : "instruction_id"} ]
	};
	var rAutoComp = new YAHOO.widget.AutoComplete('instruction_desc','prescriptioninstructioncontainer', datasource);
	rAutoComp.minQueryLength = 0;
 	rAutoComp.maxResultsDisplayed = 20;
 	rAutoComp.forceSelection = false ;
 	rAutoComp.animVert = false;
 	rAutoComp.resultTypeList = false;
 	rAutoComp.typeAhead = false;
 	rAutoComp.allowBroserAutocomplete = false;
 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	rAutoComp.autoHighlight = true;
	rAutoComp.useShadow = false;
 	if (rAutoComp._elTextbox.value != '') {
			rAutoComp._bItemSelected = true;
			rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
	}
}