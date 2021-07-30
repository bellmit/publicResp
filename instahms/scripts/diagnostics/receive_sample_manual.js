function initAll() {
	initMrNoAutoComplete(cpath);
	autoCompleteTest();
	autoCompleteCollectionCenter();
}

function autoCompleteTest() {
	dataSource = new YAHOO.util.LocalDataSource(allTestNames)
	dataSource.responseSchema = {fields : ["TEST"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('test_name', 'test_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function autoCompleteCollectionCenter() {
	dataSource = new YAHOO.util.LocalDataSource(collectionCenters)
	dataSource.responseSchema = {fields : ["COLLECTION_CENTER"]};
	oAutoComp1 = new YAHOO.widget.AutoComplete('collection_center', 'collection_center_container', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
}

function doSearch() {
	if (!doValidateDateField(document.getElementById("sample_date0"), 'past')) {
		document.getElementById("sample_date0").focus();
		return false;
	}
	if((document.getElementById("receipt_date0").value != "")){
		if(document.getElementById("receipt_time0").value != "") {
			if (!validateTime(document.getElementById("receipt_time0"))) return false;
		}
	} else if(document.getElementById("receipt_time0").value != "") {
		showMessage("js.laboratory.receivesample.receiptdate");
		document.getElementById("receipt_date0").focus();
		return false;
	}
	if((document.getElementById("receipt_date1").value != "")){
		if(document.getElementById("receipt_time1").value != "") {
			if (!validateTime(document.getElementById("receipt_time1"))) return false;
		}
	} else if(document.getElementById("receipt_time1").value != "") {
		showMessage("js.laboratory.receivesample.receiptdate");
		document.getElementById("receipt_date1").focus();
		return false;
	}

	if((document.getElementById("transfer_date0").value != "")){
		if(document.getElementById("transfer_time0").value != "") {
			if (!validateTime(document.getElementById("transfer_time0"))) return false;
		}
	} else if(document.getElementById("transfer_time0").value != "") {
		showMessage("js.laboratory.receivesample.transferreddate");
		document.getElementById("transfer_date0").focus();
		return false;
	}
	if((document.getElementById("transfer_date1").value != "")){
   		if(document.getElementById("transfer_time1").value != "") {
			if (!validateTime(document.getElementById("receipt_time1"))) return false;
		}
	} else if(document.getElementById("transfer_time1").value != "") {
		showMessage("js.laboratory.receivesample.transferreddate");
		document.getElementById("transfer_date1").focus();
		return false;
	}
	return true;
}

function markReceivedAndPrint(printObj){
	var checkBox = document.getElementsByName("receiveCheck");
	var count = 0;	
	for(var i=0; i<checkBox.length; i++) {
		if(checkBox[i].checked) count++;
	}
	if (count == 0) {
		showMessage("js.laboratory.receivesample.selecttest")
		return false;
	}

	document.resultsForm.action = cpath +"/Laboratory/ReceiveAndSplitSamples.do?"
								        + "_method=saveMarkedReceivedSamples&isRequestFromCheckBox=true";
	document.resultsForm.submit();
}
