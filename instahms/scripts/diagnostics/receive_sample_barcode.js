function init() {
	createToolbar(toolbar);
}

function printAll(){
	var checkBox = document.getElementsByName("receiveCheck");
	var sampleCollectionIds = [];
	if (checkBox.length == 0) {
		showMessage("js.laboratory.receivesample.selectsampleforprint");
		return false;
	}
	for (var i=0; i<checkBox.length; i++) {
		sampleCollectionIds.push(checkBox[i].value);
	}
	window.open(cpath+"/Laboratory/SampleWorkSheetPrint.do?_method=printSampleWorkSheet&sampleCollectionIds="+sampleCollectionIds+"&bulkWorkSheetPrint=Y");
}