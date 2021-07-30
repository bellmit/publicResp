
function init() {
	createToolbar(toolbar);
	document.getElementById("sample_no").focus();
}

function markTransferredAndPrint(printObj){
	var checkBox = document.getElementsByName("transferCheck");
	var count = 0;
	var transferDate = document.resultsForm.transferDate.value;
	var transferTime = document.resultsForm.transferTime.value;
	var outSourceIds = document.getElementsByName("outsource_id");
	var outsourceDestEls = document.getElementsByName("outsource_dest_id");
	var sampleNos = document.getElementsByName("sampleNo");
	var errMsg = '';

	for(var i=0; i<checkBox.length; i++) {
		if (checkBox[i].checked) count++;
	}
	if (count==0) {
		showMessage("js.laboratory.transfersample.selecttest");
		return false;
	}
	for(var i=0; i<outSourceIds.length; i++) {
		if (checkBox[i].checked && empty(outSourceIds[i].value)) {
			errMsg += '\n';
			errMsg = errMsg+'Sample No. : '+sampleNos[i].value;
		} else {
			outsourceDestEls[i].value = outSourceIds[i].value;			
		} 
	}	
	if (errMsg != '' ) {
		errMsg = getString("js.laboratory.transfersample.selectoutsourceforsample")+errMsg
		alert(errMsg);
		return false;
	} 
	
	if (document.resultsForm.transferDate.value == "" || document.resultsForm.transferTime.value == "") {
		showMessage("js.laboratory.transfersample.transferdatetime");
		if(document.resultsForm.transferDate.value == "")
			document.resultsForm.transferDate.focus();
		if(document.resultsForm.transferTime.value == "")
			document.resultsForm.transferTime.focus();
		return false;
	} else {
		if (!doValidateDateField(document.resultsForm.transferDate, 'past')) return false;
		if (!validateTime(document.resultsForm.transferTime)) return false;
		for(var j=0; j<checkBox.length; j++) {
			if(checkBox[j].checked) {
				var sampleDateTime = document.getElementById("sampleDateTime"+j).value;
				var parts = sampleDateTime.split(" ");
				if(getDateTime(transferDate, transferTime) - getDateTime(parts[0], parts[1]) < 0){
					showMessage("js.laboratory.transfersample.transfertimecheck");
					return false;
				}
			}
		}
	}
	if(document.resultsForm.transferOtherDetails.value.length > 2000){
		showMessage("js.laboratory.transfersample.charsallowed.otherdetails")
		document.resultsForm.transferOtherDetails.focus();
	    return false;
	}

	if (printObj == 'P')
		document.resultsForm.isPrint.value = 'P';
	else
		document.resultsForm.isPrint.value = '';

	if (gScreenId == 'lab_transfer_sample_barcode')
		document.resultsForm.action = cpath +"/Laboratory/TransferSamplesBarcode.do?_method=saveMarkedTransferredSamples";
	else
		document.resultsForm.action = cpath +"/Laboratory/TransferSamplesManual.do?_method=saveMarkedTransferredSamples";

	document.resultsForm.submit();
}

function printAll(){
	var checkBox = document.getElementsByName("transferCheck");
	var sampleCollectionIds = [];
	var sampleNos = document.getElementsByName("sampleNo");
	var errMsg = '';
	if(checkBox.length == 0) {
		showMessage("js.laboratory.transfersample.selectsampleforprint");
		return false;
	}
	for(var i=0; i<checkBox.length; i++) {
		sampleCollectionIds.push(checkBox[i].value);
	}
	window.open(cpath+"/Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet&sampleCollectionIds="+sampleCollectionIds+"&bulkWorkSheetPrint=Y");
}
