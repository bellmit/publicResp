function initDialogs() {
	initGrpDialog();
	initAntibioticDialog();

}

function initGrpDialog() {
	var dialogDiv = document.getElementById("addGrpdialog");
	dialogDiv.style.display = 'block';
	addGrpdialog = new YAHOO.widget.Dialog("addGrpdialog",
			{	width:"300px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('GrpAdd', 'click', addToTableForOrganisam, addGrpdialog, true);
	YAHOO.util.Event.addListener('GrpClose', 'click', handleAddSICancel, addGrpdialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddSICancel,
	                                                scope:addGrpdialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("addGrpdialogFieldsDiv", { keys:13 },
				{ fn:onEnterKeyGrpDialog, scope:addGrpdialog, correctScope:true } );
	addGrpdialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	addGrpdialog.render();
}

function initAntibioticDialog() {
	var dialogDiv = document.getElementById("addAntibioticdialog");
	dialogDiv.style.display = 'block';
	addAntdialog = new YAHOO.widget.Dialog("addAntibioticdialog",
			{	width:"300px",
				context : ["addItemDialog", "tr", "br"],
				visible:false,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('AntAdd', 'click', addToTableForAntibiotic, addAntdialog, true);
	YAHOO.util.Event.addListener('AntClose', 'click', handleAddAntibioCancel, addAntdialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleAddAntibioCancel,
	                                                scope:addAntdialog,
	                                                correctScope:true } );
	var enterKeyListener = new YAHOO.util.KeyListener("addAntibioticDialogFieldsDiv", { keys:13 },
				{ fn:onEnterKeyAntDialog, scope:addAntdialog, correctScope:true } );
	addAntdialog.cfg.setProperty("keylisteners", [enterKeyListener, escKeyListener]);
	addAntdialog.render();
}

// unused code is commented out
/*function initEditGrpDialog() {
	var dialogDiv = document.getElementById("editGrpdialog");
	dialogDiv.style.display = 'block';
	editGrpdialog = new YAHOO.widget.Dialog("editGrpdialog",{
			width:"300px",
			text: "Edit Item",
			context :["itemsTable", "tl", "tl"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleEditSICancel,
	                                                scope:editGrpdialog,
	                                                correctScope:true } );
	editGrpdialog.cfg.queueProperty("keylisteners", escKeyListener);
	editGrpdialog.cancelEvent.subscribe(handleEditSICancel);
	YAHOO.util.Event.addListener('siOk', 'click', editgrpTableRow, editGrpdialog, true);
	YAHOO.util.Event.addListener('siEditCancel', 'click', handleEditSICancel, editGrpdialog, true);
	YAHOO.util.Event.addListener('siEditPrevious', 'click', openPrevious, editGrpdialog, true);
	YAHOO.util.Event.addListener('siEditNext', 'click', openNext, editGrpdialog, true);
	editGrpdialog.render();
}*/

function onEnterKeyGrpDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new item from autocomplete.)
	document.getElementById("org_group_name").blur();
	addToTableForOrganisam();
}

function onEnterKeyAntDialog() {
	// onblur is required. when user tries to enter the new item and forceselection is
	// enabled we need to clear that item which is done using following stmt(i., because when forceselection
	// is enabled only blur of that element it will clears the new item from autocomplete.)
	document.getElementById("antibiotic_name").blur();
	addToTableForAntibiotic();
}

//unused code is commented out

/*function handleEditSICancel() {
	if (childDialog == null) {
		var id = document.abstPanelForm.grp_editRowId.value;
		var row = getChargeRow(id, "grpTable");
		YAHOO.util.Dom.removeClass(row, 'editing');
		parentSIDialog = null;
		siFieldEdited = false;
		this.hide();
	}
}*/

function handleAddSICancel() {
	if (childDialog == null) {
		parentSIDialog = null;
		this.cancel();
	}
}

function handleAddAntibioCancel() {
	if (childDialog == null) {
		parentSIDialog = null;
		this.cancel();
	}
}

var A_GROUP=0, A_TRASH=1;

function addToTableForOrganisam() {

	var grpName = document.getElementById('org_group_name').options[document.getElementById('org_group_name').selectedIndex].text;
   	var grpId = document.getElementById('org_group_name').value;

	if (grpId == '') {
   		alert("Please select the group name");
   		document.getElementById('org_group_name').focus();
   		return false;
   	}
   	if (checkForDuplicates()) {
		alert("Duplicate entry : " + grpName);
		return false;
	}

	var id = getNumCharges('grpTable');
   	var table = document.getElementById("grpTable");
	var templateRow = table.rows[getTemplateRow('grpTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "s_itemRow" + id;

	setNodeText(row.cells[A_GROUP], grpName);

	setHiddenValue(id, "micro_abst_orggr_id", "_");
	setHiddenValue(id, "org_group_id", grpId);

	setRowStyleForOrganisam(id);
	itemsAdded++;
	clearFields("Grp");

	this.align("tr", "tl");
	document.getElementById('org_group_name').focus();
	return id;
}

function addToTableForAntibiotic() {
	var antName = document.getElementById('antibiotic_name').options[document.getElementById('antibiotic_name').selectedIndex].text;
   	var antId = document.getElementById('antibiotic_name').value;

	if (antId == '') {
   		alert("Please select the antibiotic name");
   		document.getElementById('antibiotic_name').focus();
   		return false;
   	}
   	if (checkForDuplicatesAnt()) {
		alert("Duplicate entry : " + antName);
		return false;
	}

	var id = getNumCharges('antTable');
   	var table = document.getElementById("antTable");
	var templateRow = table.rows[getTemplateRow('antTable')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "s_itemRow" + id;

	setNodeText(row.cells[A_GROUP], antName);

	setHiddenValue(id, "antibiotic_id_check", "_");
	setHiddenValue(id, "antibiotic_id", antId);

	setRowStyleForAntibiotic(id);
	itemsAdded++;
	clearFields("Ant");

	this.align("tr", "tl");
	document.getElementById('antibiotic_name').focus();
	return id;
}
var itemsAdded = 0;
function setRowStyleForOrganisam(i) {
	var row = getChargeRow(i, 'grpTable');
	var prescribedId = getIndexedValue("micro_abst_orggr_id", i);

	var flagImgs = row.cells[A_GROUP].getElementsByTagName("img");
	var trashImgs = row.cells[A_TRASH].getElementsByTagName("img");

	var added = (prescribedId.substring(0,1) == "_");
	var cancelled = getIndexedValue("org_deleted", i) == 'true';
	var priorAuth = getIndexedValue("s_priorAuth", i);

	var cls;

	/**
	* cancelled flag takes priority when a prescriptions is of type prior auth required and it is cancelld.
	*/
	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else if (priorAuth == 'A') {
		flagSrc = cpath + '/images/blue_flag.gif';
	} else if (priorAuth == 'S') {
		flagSrc = cpath + "/images/green_flag.gif";
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function setRowStyleForAntibiotic(i) {
	var row = getChargeRow(i, 'antTable');
	var antId = getIndexedValue("antibiotic_id", i);

	var flagImgs = row.cells[A_GROUP].getElementsByTagName("img");
	var trashImgs = row.cells[A_TRASH].getElementsByTagName("img");

	var added = (antId.substring(0,1) == "_");
	var cancelled = getIndexedValue("ant_deleted", i) == 'true';
	var priorAuth = getIndexedValue("s_priorAuth", i);

	var cls;

	/**
	* cancelled flag takes priority when a prescriptions is of type prior auth required and it is cancelld.
	*/
	var flagSrc;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
	} else if (priorAuth == 'A') {
		flagSrc = cpath + '/images/blue_flag.gif';
	} else if (priorAuth == 'S') {
		flagSrc = cpath + "/images/green_flag.gif";
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0])
		flagImgs[0].src = flagSrc;

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function clearFields(name) {
	if (name == 'Grp')
   		document.getElementById('org_group_name').value = '';
   	else
   		document.getElementById('antibiotic_name').value = '';
 }

 function cancelSIItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var oldDeleted =  getIndexedValue("org_deleted", id);
	var isNew = getIndexedValue("micro_abst_orggr_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}

		setIndexedValue("org_deleted", id, newDeleted);
		setRowStyleForOrganisam(id);
	}
	return false;
}

function cancelAntibioticItem(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	var oldDeleted =  getIndexedValue("ant_deleted", id);
	var isNew = getIndexedValue("antibiotic_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		itemsAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}

		setIndexedValue("ant_deleted", id, newDeleted);
		setRowStyleForAntibiotic(id);
	}
	return false;
}

function showEditDialog(obj) {
	parentDialog = editGrpdialog;
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editGrpdialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editGrpdialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	var orgID = getIndexedValue("org_group_id", id);
	document.abstPanelForm.grp_editRowId.value = id;

	document.getElementById('ed_org_group_name').value = orgID;

	return false;
}

//unused code is commented out

/*function openPrevious(id, previous, next) {
	var id = document.abstPanelForm.grp_editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'grpTable');
	if (siFieldEdited) {
		siFieldEdited = false;
		if (!editgrpTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	editgrpTableRow();
	if (id != 0) {
		showEditDialog(document.getElementsByName('gr_editAnchor')[parseInt(id)-1]);
	}
}

function openNext() {
	var id = document.abstPanelForm.grp_editRowId.value;
	id = parseInt(id);
	var row = getChargeRow(id, 'grpTable');
	if (siFieldEdited) {
		siFieldEdited = false;
		if (!editgrpTableRow())
			return false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	editgrpTableRow();
	if (id+1 != document.getElementById('grpTable').rows.length-2) {
		showEditDialog(document.getElementsByName('gr_editAnchor')[parseInt(id)+1]);
	}
}*/

function checkForDuplicates(prefix) {
	var grpIDS = document.getElementsByName("org_group_id");

	var dorgGrpID = document.getElementById("org_group_name").value;

	for (var i=0; i<grpIDS.length-1; i++) {
		if (dorgGrpID == grpIDS[i].value) {
			return true;
		}
	}
	return false;
}

function checkForDuplicatesAnt(prefix) {
	var antIDS = document.getElementsByName("antibiotic_id");

	var dorgaAntID = document.getElementById("antibiotic_name").value;

	for (var i=0; i<antIDS.length-1; i++) {
		if (dorgaAntID == antIDS[i].value) {
			return true;
		}
	}
	return false;
}

var parentSIDialog = null;
var childDialog = null;
function showaddGrpdialog(obj) {
	var row = getThisRow(obj);

	addGrpdialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addGrpdialog.show();
	document.getElementById('org_group_name').focus();
	parentSIDialog = addGrpdialog;

	return false;
}

function showaddAntibioticdialog(obj) {
	var row = getThisRow(obj);

	addAntdialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addAntdialog.show();
	document.getElementById('antibiotic_name').focus();
	parentSIDialog = addAntdialog;

	return false;
}
var siFieldEdited = false;


//unused code is commented out
/*function editgrpTableRow() {
	var id = document.abstPanelForm.grp_editRowId.value;
	var row = getChargeRow(id, 'grpTable');

	var grpID = document.getElementById('ed_org_group_name').value;
	var grpName = document.getElementById('ed_org_group_name').options[document.getElementById('ed_org_group_name').selectedIndex].text;

   if (grpID == '') {
   		alert("Please enter the group name");
   		document.getElementById('ed_org_group_name').focus();
   		return false;
   	}

   	setNodeText(row.cells[A_GROUP], grpName);
	setHiddenValue(id, "org_group_id", grpID);
	YAHOO.util.Dom.removeClass(row, 'editing');

	setIndexedValue("org_edited", id, 'true');
	setRowStyle(id);

	editGrpdialog.cancel();
	return true;
}*/

function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.abstPanelForm, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.abstPanelForm, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.abstPanelForm, name, index);
	if (obj)
		obj.value = value;
	return obj;
}