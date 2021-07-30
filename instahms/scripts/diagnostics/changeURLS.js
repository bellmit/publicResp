	function changeURL(anchor, params, id, toolbar) {
		var row = document.getElementById('toolbarRow' + id);
		var checkBoxes = getElementsByName(row, 'amendResults');
		var testIds = new Array();
		if (checkBoxes != null) {
			for (var i=0; i<checkBoxes.length; i++) {
				if (checkBoxes[i].checked)
					testIds.push(checkBoxes[i].value);
			}
		}

		if (testIds.length == 0) {
			showMessage("js.laboratory.radiology.reportlist.selecttest.reconduct");
			return false;
		}

		var href = anchor.href;
		for (var i=0; i<testIds.length; i++) {
			href = href + "&prescId=" + testIds[i];
		}
		anchor.href = href;
		return true;
}

function changeReconductURL( anchor, params, id, toolbar ){

	var reportId = '';
	var incoming = false;
	for (var paramname in params) {
		var paramvalue = params[paramname]
		if (paramname == 'reportId')
			reportId = paramvalue;
		if ( paramname == 'hospital' && paramvalue == 'incoming' )
			incoming = true;
	}
	if ( incoming ) {
		var href = anchor.href;
		href = href.replace(module+'ReconductTestList',module+'IncomingReconductTestList');
		anchor.href = href;
	}

	// selecting a "no report" row, need to add all the checked test Ids to the URL
	// as parameters.
	var row = document.getElementById('toolbarRow' + id);
	var checkBoxes = getElementsByName(row, 'amendResults');
	var testIds = new Array();
	if (checkBoxes != null) {
		for (var i=0; i<checkBoxes.length; i++) {
			if (checkBoxes[i].checked)
				testIds.push(checkBoxes[i].value);
		}
	}

	if (testIds.length == 0) {
		showMessage("js.laboratory.radiology.reportlist.selecttest.edit");
		return false;
	}

	var href = anchor.href;
	for (var i=0; i<testIds.length; i++) {
		href = href + "&prescId=" + testIds[i];
	}
	anchor.href = href;
	return true;
}