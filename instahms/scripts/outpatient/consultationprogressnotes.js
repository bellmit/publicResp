var pProgressNotesDialog = null;
function initPreviousProgressNotesDialog() {
	var dialogDiv = document.getElementById("previousProgressNotesDiv");
	dialogDiv.style.display = 'block';
	pProgressNotesDialog = new YAHOO.widget.Dialog("previousProgressNotesDiv",
			{	width:"820px",
				height: '320px',
				context : ["progress_notes_img_div", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('previousNotes_btn', 'click', pProgressNotesDialog.cancel, pProgressNotesDialog, true);
	subscribeEscKeyListener(pProgressNotesDialog);
	pProgressNotesDialog.render();
}

function subscribeEscKeyListener(dialog) {
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:dialog.cancel,
	                                                scope:dialog,
	                                                correctScope:true } );
	dialog.cfg.queueProperty("keylisteners", escKeyListener);
}

function getPreviousProgressNotes(obj) {
	initPreviousProgressNotesDialog();
	pProgressNotesDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	pProgressNotesDialog.show();
	var mrNo = document.getElementById('mr_no').value;
	mrNo = encodeURIComponent(mrNo);
	makeAjaxCall(mrNo);
}

function makeAjaxCall(mrNo, curPage) {

	document.getElementById('progressbar').style.visibility = 'none';
	var url = cpath + '/outpatient/OpPrescribeAction.do?_method=getPreviousProgressNotes';
	url += "&mr_no="+mrNo;
	if (curPage)
		url += "&pageNum="+curPage;
	url += "&pageSize=5";

	YAHOO.util.Connect.asyncRequest('GET', url,
		{ 	success: populatePreviousProgressNotesDialog,
			failure: failedToGetPreviousNotes,
			argument: []
		});
}

function failedToGetPreviousNotes() {
}

function populatePreviousProgressNotesDialog(response) {
	var mrNo = document.getElementById('mr_no').value;
	if (response.responseText != undefined) {
		var previousNotes = eval('(' + response.responseText + ')');
		var table = document.getElementById("previousNotesTable");
		var label = null;
		for (var i=1; i<table.rows.length-2; ) {
			table.deleteRow(i);
		}
		var dtoList = previousNotes.dtoList;
		generatePaginationSection(mrNo,previousNotes.pageNumber, previousNotes.numPages);

		var noNotesRow = table.rows[table.rows.length-1];
		noNotesRow.style.display = dtoList.length == 0 ? 'table-row' : 'none';

		for (var i=0; i<dtoList.length; i++) {
			var record = dtoList[i];
			var templateRow = table.rows[table.rows.length-2];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);

			setNodeText(row.cells[0], record.mod_time ? formatDateTime(new Date(record.mod_time), 'ddmmyyyy HHMM', '-') : '');
			setNodeText(row.cells[1], record.doctor_name);
			setNodeText(row.cells[2], record.notes);
		}

	}
	document.getElementById('progressbar').style.visibility = 'hidden';

}

function generatePaginationSection(mrNo,curPage, numPages) {
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';
	if (numPages <= 1) {

	} else {
		if (curPage > 1) {
			var txtEl = document.createTextNode('<<Prev');
			var label = document.createElement("label");
			label.setAttribute('onclick', 'makeAjaxCall("'+encodeURIComponent(mrNo)+'",'+(curPage-1)+')');
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
			label.setAttribute('onclick', 'makeAjaxCall("'+encodeURIComponent(mrNo)+'",'+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}