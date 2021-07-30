YAHOO.util.Event.onContentReady('content', initDuplicatePatientDialog);
var dupDialog = null;
function initDuplicatePatientDialog() {
	var dialogDiv = document.getElementById("duplicatePatientsDiv");
	if ( dialogDiv )
		dialogDiv.style.display = 'block';
	dupDialog = new YAHOO.widget.Dialog("duplicatePatientsDiv",
			{	width:"450px",
				height: '270px',
				context : ["duplicatePatientsDiv", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('dup_btn', 'click', dupDialog.cancel, dupDialog, true);
	subscribeEscKeyListener(dupDialog);
	dupDialog.render();
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

var originalMrNo;

function getDuplicatePatients(obj, mrNo) {
	dupDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	dupDialog.show();
	originalMrNo = mrNo;
	makeAjaxCall(mrNo);
}

function makeAjaxCall(mrNo,curPage) {
	var url = cpath +'/emr/EMRMainDisplay.do?_method=getDuplicatePatientDetails&mrNo='+mrNo;
	if (curPage)
		url += "&pageNum="+curPage;
	url += "&pageSize=5";

	YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: populateDuplicatePatients,
			failure: failedToGetDuplicatePatients
		});
}

function failedToGetDuplicatePatients() {
}

function populateDuplicatePatients(response) {
	if (response.responseText != undefined) {
		var dupPatients = eval('(' + response.responseText + ')');
		var table = document.getElementById("dupPatientTable");
		for (var i=1; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		var dtoList = dupPatients.dtoList;
		generatePaginationSection(dupPatients.pageNumber, dupPatients.numPages);

		var noResultsRow = table.rows[table.rows.length-1];
		noResultsRow.style.display = dtoList.length == 0 ? 'table-row' : 'none';

		for (var i=0; i<dtoList.length; i++) {
			var record = dtoList[i];
			var templateRow = table.rows[table.rows.length-2];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);

			setNodeText(row.cells[0], record.mr_no);
			setNodeText(row.cells[1], record.patient_full_name);
			setNodeText(row.cells[2], '('+record.age+'/'+record.patient_gender+')');
		}
	}
}

function generatePaginationSection(curPage, numPages) {
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';

	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCall("'+encodeURIComponent(originalMrNo)+'",'+(curPage-1)+')');
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
			label.setAttribute('onclick', 'makeAjaxCall("'+encodeURIComponent(originalMrNo)+'",'+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}