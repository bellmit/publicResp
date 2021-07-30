var pResultsDialog = null;
function initPreviousResultsDialog() {
	var dialogDiv = document.getElementById("previousResultsDiv");
	if ( dialogDiv )
		dialogDiv.style.display = 'block';
	pResultsDialog = new YAHOO.widget.Dialog("previousResultsDiv",
			{	width:"800px",
				height: '320px',
				context : ["previousResultsDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('previousResults_btn', 'click', pResultsDialog.cancel, pResultsDialog, true);
	subscribeEscKeyListener(pResultsDialog);
	pResultsDialog.render();
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

function getPreviousResults(obj, testDateTime, resultLabel, category, methodId, methodName, resultLblId) {
	pResultsDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	pResultsDialog.show();

	resultLabel = encodeURIComponent(resultLabel);
	testDateTime = encodeURIComponent(testDateTime);
	makeAjaxCall(testDateTime, resultLabel, category, methodId, methodName, resultLblId);
}

function makeAjaxCall(testDateTime, resultLabel, category, methodId, methodName, resultLblId, curPage) {

	document.getElementById('progressbar').style.visibility = 'none';
	var mrNo = document.getElementById('mrno').value;
	//this collectionCenterMrNo is useful in internal lab flow for conduction center, to fetch previous results.
	if (category == 'DEP_LAB')
		mrNo = empty(mrNo) ? collectionCenterMrNo : mrNo;
	var url = cpath + (category == 'DEP_LAB' ? '/Laboratory' : '/Radiology') + '/editresults.do?_method=getPreviousResults&mrNo='+mrNo;
	url += "&testDateTime="+testDateTime;
	url += "&resultlabel="+resultLabel;
	url += "&mr_no="+mrNo;
	url += "&method_id="+methodId;
	url += "&resultLblId="+resultLblId;

	if (curPage)
		url += "&pageNum="+curPage;
	url += "&pageSize=5";

	YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: populatePreviousResultsDialog,
			failure: failedToGetPreviousResults,
			argument: [resultLabel, testDateTime, category, methodId, methodName, resultLblId]
		});
}

function failedToGetPreviousResults() {
}

function populatePreviousResultsDialog(response) {
	document.getElementById('testResultLabel').textContent = decodeURIComponent(response.argument[0]);
	var resultLabel = decodeURIComponent(response.argument[0]);
	setNodeText('testResultLabel', resultLabel, 32, resultLabel);
	
	var testDateTime = decodeURIComponent(response.argument[1]);
	var category = response.argument[2];
	var methodId = response.argument[3];
	var methodName = decodeURIComponent(response.argument[4]);
	var resultLblId = response.argument[5];
	document.getElementById('testMethodLbl').innerHTML = methodName;

	if (response.responseText != undefined) {
		var previousResults = eval('(' + response.responseText + ')');
		var table = document.getElementById("previousResultsTable");
		var label = null;
		for (var i=1; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		var dtoList = previousResults.dtoList;
		generatePaginationSection(testDateTime, resultLabel, category, methodId, methodName, resultLblId, previousResults.pageNumber, previousResults.numPages);

		var noResultsRow = table.rows[table.rows.length-1];
		noResultsRow.style.display = dtoList.length == 0 ? 'table-row' : 'none';

		for (var i=0; i<dtoList.length; i++) {
			var record = dtoList[i];
			var templateRow = table.rows[table.rows.length-2];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);

			setNodeText(row.cells[0], record.pat_id);
			setNodeText(row.cells[1], record.test_name, 25, record.test_name);
			setNodeText(row.cells[2], record.conducted_date ? formatDate(new Date(record.conducted_date), 'ddmmyyyy', '-') : '');
			setNodeText(row.cells[3], record.sample_date ? formatDateTime(new Date(record.sample_date)) : '');
			setNodeText(row.cells[4], record.report_value + ' ' + record.units, 20, record.report_value + ' ' + record.units);
			if (record.report_name != '') {
				var url = cpath + '/pages/DiagnosticModule/DiagReportPrint.do?_method=printReport&reportId='+record.report_id;
				row.cells[5].innerHTML = '<a href="'+ url +'" title="View Report" target="_blank">' + record.report_name + '</a>';
			}
		}

	}
	document.getElementById('progressbar').style.visibility = 'hidden';

}

function generatePaginationSection(testDateTime, resultLabel, category, methodId, methodName, resultLblId, curPage, numPages) {
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';

	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCall("'+encodeURIComponent(testDateTime)+'", "'+encodeURIComponent(resultLabel)+'", "'+category+'", "'+methodId+'", "'+encodeURIComponent(methodName)+'", "'+resultLblId+'", '+(curPage-1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}
		if (curPage > 1 && curPage < numPages) {
			var txtEl = document.createTextNode(' | ');
			div.appendChild(txtEl);
		}
		if (curPage < numPages) {
			var txtEl = document.createTextNode('Next>>');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCall("'+encodeURIComponent(testDateTime)+'", "'+encodeURIComponent(resultLabel)+'", "'+category+'","'+methodId+'", "'+encodeURIComponent(methodName)+'", "'+resultLblId+'", '+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}