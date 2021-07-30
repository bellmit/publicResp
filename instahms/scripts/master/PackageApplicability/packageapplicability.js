function init() {
	initAddSponsorDialog();
	initEditSponsorDialog();
	initAddCenterDialog();
	initEditCenterDialog();
	initAddPlanDialog();
	initEditPlanDialog();
	var app_for_sponsors = document.getElementsByName('applicable_for_sponsors');
	var selected_for;
	for (var i=0; i<app_for_sponsors.length; i++) {
		if (app_for_sponsors[i].checked) {
			selected_for = app_for_sponsors[i].value;
			break;
		}
	}
	toggleSponsorAddIcon(selected_for == 'few');
	if (max_centers > 1) {
		var app_for_centers = document.getElementsByName('applicable_for_centers');
		var checked = false;
		for (var i=0; i<app_for_centers.length; i++) {
			if (app_for_centers[i].checked) {
				selected_for = app_for_centers[i].value;
				break;
			}
		}
		toggleCenterAddIcon(selected_for == 'few');
	}
	var app_for_plans = document.getElementsByName('applicable_for_plans');
	for (var i=0; i<app_for_plans.length; i++) {
		if (app_for_plans[i].checked) {
			selected_for = app_for_plans[i].value;
			break;
		}
	}
	togglePlanAddIcon(selected_for == 'few');
}

function allowAddingSponsor(val) {
	toggleSponsorAddIcon(val == 'few');
	checkNoneTpa(val == 'none');
}

function checkNoneTpa(checkedtrue) {
	document.getElementById('app_for_none_plans').checked = checkedtrue;
}

function toggleSponsorAddIcon(enableAdd) {
	document.getElementById('btnAddSponsor').disabled = !enableAdd;
	document.getElementById('sponsorAddIconEnabled').style.display = enableAdd ? 'block' : 'none';
	document.getElementById('sponsorAddIconDisabled').style.display = !enableAdd ? 'block' : 'none';
}

function allowAddingPlan(val) {
	togglePlanAddIcon(val == 'few');
}

function togglePlanAddIcon(enableAdd) {
	document.getElementById('btnAddPlan').disabled = !enableAdd;
	document.getElementById('planAddIconEnabled').style.display = enableAdd ? 'block' : 'none';
	document.getElementById('planAddIconDisabled').style.display = !enableAdd ? 'block' : 'none';
}

function allowAddingCenter(val) {
	toggleCenterAddIcon(val == 'few');
}

function toggleCenterAddIcon(enableAdd) {
	document.getElementById('btnAddCenter').disabled = !enableAdd;
	document.getElementById('centerAddIconEnabled').style.display = enableAdd ? 'block' : 'none';
	document.getElementById('centerAddIconDisabled').style.display = !enableAdd ? 'block' : 'none';
}


function saveForm() {
	var app_for_centers = document.getElementsByName('applicable_for_centers');
	if (max_centers > 1) {
		var checked = false;
		var selected_for_center;
		for (var i=0; i<app_for_centers.length; i++) {
			if (app_for_centers[i].checked) {
				checked = true;
				selected_for_center = app_for_centers[i].value;
				break;
			}
		}
		if (!checked) {
			alert("Please select the package applicability for centers");
			return false;
		}
		var sel_for_delete = document.getElementsByName("cntr_delete");
		var centersFound = false;
		for (var c=0; c<sel_for_delete.length-1; c++) {
			if (sel_for_delete[c].value == 'false') {
				centersFound = true;
				break;
			}
		}
		if (selected_for_center == 'few' && !centersFound) {
			alert("Please enter atleast one center");
			return false;
		}
	}
	var app_for_sponsors = document.getElementsByName('applicable_for_sponsors');
	var checked = false;
	var selected_for_sponsor;
	for (var i=0; i<app_for_sponsors.length; i++) {
		if (app_for_sponsors[i].checked) {
			checked = true;
			selected_for_sponsor = app_for_sponsors[i].value;
			break;
		}
	}
	if (!checked) {
		alert("Please select the package applicability for sponsors");
		return false;
	}
	var sel_for_delete = document.getElementsByName("tpa_delete");
	var tpasFound = false;
	for (var c=0; c<sel_for_delete.length-1; c++) {
		if (sel_for_delete[c].value == 'false') {
			tpasFound = true;
			break;
		}
	}
	if (selected_for_sponsor == 'few' && !tpasFound) {
		alert("Please enter atleast one sponsor");
		return false;
	}

	var app_for_plans = document.getElementsByName('applicable_for_plans');
	var checked = false;
	var selected_for_plan;
	for (var i=0;i<app_for_plans.length; i++){
		if (app_for_plans[i].checked){
			checked = true;
			selected_for_plan = app_for_plans[i].value;
			break;
		}
	}
	if (!checked){
		alert("Please select the package applicability for plans");
		return false;
	}
	var sel_for_delete = document.getElementsByName("plan_delete");
	var plansFound = false;
	for (var c=0;c<sel_for_delete.length-1; c++){
		if (sel_for_delete[c].value == 'false'){
			plansFound = true;
			break;
		}
	}
	if (selected_for_plan == 'few' && !plansFound) {
		alert("Please enter atleast one plan");
		return false;
	}
	if (selected_for_sponsor !='none' && selected_for_plan == 'none'){
		alert("Plan applicability cannot be none if Sponsor applicability is set.");
		return false;
	}
	document.package_applicable_form.submit();
	return true;
}

function populateCities() {
	var stateId = document.getElementById('d_state').value;
	var city = document.getElementById('d_city');
	city.options.length = 1;
	for (var i=0; i<citiesJSON.length; i++) {
		var record = citiesJSON[i]
		if (empty(stateId) || stateId == record.state_id) {
			var len = city.options.length;
			city.options.length = len+1;
			city.options[len].text = record.city_name;
			city.options[len].value = record.city_id;
		}
	}
}

function populateEligiblePlans() {
	var plan = document.getElementById('d_plan');
	plan.options.length = 1;
	for (var i=0; i<eligiblePlans.length; i++) {
		var record = eligiblePlans[i]
			var len = plan.options.length;
			plan.options.length = len+1;
			plan.options[len].text = record.plan_name;
			plan.options[len].value = record.plan_id;
	}
}


var addSponsorDialog = null;
function initAddSponsorDialog() {
	var dialogDiv = document.getElementById("addSponsorDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addSponsorDialog = new YAHOO.widget.Dialog("addSponsorDialog",
			{	width:"400px",
				context : ["addSponsorDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('sp_ok', 'click', addToSponsorTable, addSponsorDialog, true);
	YAHOO.util.Event.addListener('sp_cancel', 'click', cancelSposorAddDialog, addSponsorDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelSposorAddDialog,
	                                                scope:addSponsorDialog,
	                                                correctScope:true } );
	addSponsorDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addSponsorDialog.render();
}

function cancelSposorAddDialog() {
	addSponsorDialog.cancel();
}

function cancelPlanAddDialog() {
	addPlanDialog.cancel();
}

var spIndex = 0;
var SPONSOR_NAME = spIndex++, SPONSOR_STATUS = spIndex++, SPONSOR_TRASH_COL = spIndex++, SPONSOR_EDIT_COL = spIndex++;
function showAddSponsorDialog(obj) {
	var row = getThisRow(obj);

	clearSponsorFields();
	addSponsorDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addSponsorDialog.show();

	return false;
}

var planIndex = 0;
var PLAN_NAME = planIndex++, PLAN_STATUS = planIndex++, PLAN_TRASH_COL = planIndex++, PLAN_EDIT_COL = planIndex++;
function showAddPlanDialog(obj) {
	var row = getThisRow(obj);	
	clearPlanFields();
	addPlanDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addPlanDialog.show();

	return false;
}

function isDuplicate(type, id) {
	var els = document.getElementsByName(type == 'S' ? 'tpa_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'tpa_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}

function foundDuplicate(type, id, index) {
	var els = document.getElementsByName(type == 'S' ? 'tpa_id' : 'center_id');
	var delete_el = document.getElementsByName(type == 'S' ? 'tpa_delete' : 'cntr_delete');
	for (var i=0; i<els.length; i++) {
		if (index != i && els[i].value == id && delete_el[i].value == 'false')
			return true;
	}
	return false;
}

var sponsorAdded = 0;
function addToSponsorTable() {
	var sponsor_name = document.getElementById('d_tpa').options[document.getElementById('d_tpa').options.selectedIndex].text;
   	var sponsor_id = document.getElementById('d_tpa').value;
   	var status = document.getElementById('d_tpa_status').value;
   	if (isDuplicate('S', sponsor_id)) {
   		alert('Sponsor already exists');
   		return false;
   	}

	var id = getNumCharges('sponsors_table');
   	var table = document.getElementById("sponsors_table");
	var templateRow = table.rows[getTemplateRow('sponsors_table')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	setNodeText(row.cells[SPONSOR_STATUS], status == 'A' ? 'Active' : 'Inactive');
	setNodeText(row.cells[SPONSOR_NAME], sponsor_name);

	setHiddenValue(id, "package_sponsor_id", "_");
	setHiddenValue(id, "tpa_name", sponsor_name);
	setHiddenValue(id, "tpa_id", sponsor_id);
	setHiddenValue(id, "tpa_status", status);

	sponsorAdded++;
	clearSponsorFields();
	setSponsorRowStyle(id);
	addSponsorDialog.align("tr", "tl");
	document.getElementById('d_tpa').focus();
	return id;
}

var planAdded = 0;
function addToPlanTable() {
	var plan_name = document.getElementById('d_plan').options[document.getElementById('d_plan').options.selectedIndex].text;
   	var plan_id = document.getElementById('d_plan').value;
   	var status = document.getElementById('d_plan_status').value;
   	if (isDuplicate('S', plan_id)) {
   		alert('plan already exists');
   		return false;
   	}

	var id = getNumCharges('plans_table');
   	var table = document.getElementById("plans_table");
	var templateRow = table.rows[getTemplateRow('plans_table')];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
   	row.id = "itemRow" + id;

   	setNodeText(row.cells[PLAN_STATUS], status == 'A' ? 'Active' : 'Inactive');
	setNodeText(row.cells[PLAN_NAME], plan_name);

	setHiddenValue(id, "package_plan_id", "_");
	setHiddenValue(id, "plan_name", plan_name);
	setHiddenValue(id, "plan_id", plan_id);
	setHiddenValue(id, "plan_status", status);

	planAdded++;
	clearPlanFields();
	setPlanRowStyle(id);
	addPlanDialog.align("tr", "tl");
	document.getElementById('d_plan').focus();
	return id;
}

function clearSponsorFields() {
	document.getElementById('d_tpa').value = '';
	document.getElementById('d_tpa_status').value = 'A';
}

function clearPlanFields() {
	document.getElementById('d_plan_status').value = 'A';
	var plan = document.getElementById('d_plan');
	var fewSponsorSelected = document.getElementById("app_for_few_sponsor").checked;
	var tpa_id ='';
	var ajaxobj = newXMLHttpRequest();
	url = cpath+'/master/PackageApplicabilityAction.do?_method=getEligiblePlans&packId='+packId+'&tpaId='
	if (fewSponsorSelected){
		var table = document.getElementById("sponsors_table");
		for (i = 1; i <= table.rows.length-2;i++){
			var row = table.rows[i];
			if (i == 1){
				tpa_id = getElementByName(row,'tpa_id').value;
			}
			else {
				tpa_id = tpa_id +','+getElementByName(row,'tpa_id').value;
			}
		}
	}
	else {
		tpa_id= 'all';
	}
	url +=tpa_id
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				var exists =  JSON.parse(ajaxobj.responseText);
				plan.options.length = 1;
				for (i=0;i<exists.length;i++){
					var len = plan.options.length;
					plan.options.length = len+1;
					plan.options[len].text = exists[i].plan_name;
					plan.options[len].value = exists[i].plan_id;
				}
				
		}
	}
	}
}


var editSponsorDialog = null;
function initEditSponsorDialog() {
	var dialogDiv = document.getElementById("editSponsorDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	editSponsorDialog = new YAHOO.widget.Dialog("editSponsorDialog",
			{	width:"400px",
				context : ["editSponsorDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('sp_edit_ok', 'click', editSponsorRow, editSponsorDialog, true);
	YAHOO.util.Event.addListener('sp_edit_cancel', 'click', cancelSposorEditDialog, editSponsorDialog, true);
	YAHOO.util.Event.addListener('sp_edit_previous', 'click', openPreviousSposor, editSponsorDialog, true);
	YAHOO.util.Event.addListener('sp_edit_next', 'click', openNextSposor, editSponsorDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelSposorEditDialog,
	                                                scope:editSponsorDialog,
	                                                correctScope:true } );
	editSponsorDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	editSponsorDialog.render();

}


var editPlanDialog = null;
function initEditPlanDialog() {
	var dialogDiv = document.getElementById("editPlanDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	editPlanDialog = new YAHOO.widget.Dialog("editPlanDialog",
			{	width:"400px",
				context : ["editPlanDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('plan_edit_ok', 'click', editPlanRow, editPlanDialog, true);
	YAHOO.util.Event.addListener('plan_edit_cancel', 'click', cancelPlanEditDialog, editPlanDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelPlanEditDialog,
	                                                scope:editPlanDialog,
	                                                correctScope:true } );
	editPlanDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	editPlanDialog.render();

}

function showEditSponsorDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editSponsorDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editSponsorDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('sponsor_edit_row_id').value = id;
	document.getElementById('ed_sponsor_label').textContent = getIndexedValue('tpa_name', id);
	document.getElementById('ed_tpa_status').value = getIndexedValue('tpa_status', id);

	return false;

}

function showEditPlanDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editPlanDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editPlanDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('plan_edit_row_id').value = id;
	document.getElementById('ed_plan_label').textContent = getIndexedValue('plan_name', id);
	document.getElementById('ed_plan_status').value = getIndexedValue('plan_status', id);

	return false;

}

function editSponsorRow() {
	var id = document.getElementById('sponsor_edit_row_id').value;
	var row = getChargeRow(id, 'sponsors_table');

	var status = document.getElementById('ed_tpa_status').value;
	setNodeText(row.cells[SPONSOR_STATUS], status == 'A' ? 'Active' : 'Inactive');
	setHiddenValue(id, "tpa_status", status);

	setIndexedValue("tpa_edited", id, 'true');
	setSponsorRowStyle(id);

	editSponsorDialog.cancel();
	return true;
}

function editPlanRow() {
	var id = document.getElementById('plan_edit_row_id').value;
	var row = getChargeRow(id, 'plans_table');

	var status = document.getElementById('ed_plan_status').value;
	setNodeText(row.cells[PLAN_STATUS], status == 'A' ? 'Active' : 'Inactive');
	setHiddenValue(id, "plan_status", status);

	setIndexedValue("plan_edited", id, 'true');
	setPlanRowStyle(id);

	editPlanDialog.cancel();
	return true;
}

var sponsorFieldEdited = false;
function setSponsorFieldEdited() {
	sponsorFieldEdited = true;
}

var planFieldEdited = false;
function setPlanFieldEdited() {
	planFieldEdited = true;
}

function openPreviousSposor() {
	var id = document.getElementById('sponsor_edit_row_id').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'sponsors_table');

	if (sponsorFieldEdited) {
		if (!editSponsorRow()) return false;
		sponsorFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditSponsorDialog(document.getElementsByName('_editSponsorAnchor')[parseInt(id)-1]);
	}
}

function openNextSposor() {
	var id = document.getElementById('sponsor_edit_row_id').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'sponsors_table');

	if (sponsorFieldEdited) {
		if (!editSponsorRow()) return false;
		sponsorFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('sponsors_table').rows.length-2) {
		showEditSponsorDialog(document.getElementsByName('_editSponsorAnchor')[parseInt(id)+1]);
	}
}

function cancelSposorEditDialog() {
	sponsorFieldEdited = false;
	var id = document.getElementById("sponsor_edit_row_id").value;
	var row = getChargeRow(id, "sponsors_table");
	YAHOO.util.Dom.removeClass(row, 'editing');
	editSponsorDialog.cancel();
}

function cancelPlanEditDialog() {
	sponsorFieldEdited = false;
	var id = document.getElementById("plan_edit_row_id").value;
	var row = getChargeRow(id, "plans_table");
	YAHOO.util.Dom.removeClass(row, 'editing');
	editSponsorDialog.cancel();
}

var addPlanDialog = null;
function initAddPlanDialog() {
	var dialogDiv = document.getElementById("addPlanDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addPlanDialog = new YAHOO.widget.Dialog("addPlanDialog",
			{	width:"400px",
				context : ["addPlanDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('plan_ok', 'click', addToPlanTable, addPlanDialog, true);
	YAHOO.util.Event.addListener('plan_cancel', 'click', cancelPlanAddDialog, addPlanDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelPlanAddDialog,
	                                                scope:addPlanDialog,
	                                                correctScope:true } );
	addPlanDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addPlanDialog.render();
}



var addCenterDialog = null;
function initAddCenterDialog() {
	var dialogDiv = document.getElementById("addCenterDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	addCenterDialog = new YAHOO.widget.Dialog("addCenterDialog",
			{	width:"400px",
				context : ["addCenterDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('d_center_ok', 'click', addToCenterTable, addCenterDialog, true);
	YAHOO.util.Event.addListener('d_center_cancel', 'click', cancelCenterDialog, addCenterDialog, true);
	YAHOO.util.Event.addListener('d_search_centers', 'click', getCenters, addCenterDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelCenterDialog,
	                                                scope:addCenterDialog,
	                                                correctScope:true } );
	addCenterDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	addCenterDialog.render();
}

function showAddCenterDialog(obj) {
	var row = getThisRow(obj);

	clearCenterFields();
	addCenterDialog.cfg.setProperty('context', [obj, 'tr', 'tl'], false);
	addCenterDialog.show();

	return false;
}

function showAllCenters(obj) {
	document.getElementById('d_state').value = '';
	document.getElementById('d_city').value = '';
	if (obj.checked) {
		searchCenters(1);
	} else {
		var ctable = document.getElementById('avlbl_centers_table');
		for (var i=1; i<ctable.rows.length-1; ) {
			ctable.deleteRow(i);
		}
		var div = document.getElementById('paginationDiv');
		div.innerHTML = '';
	}

}

function getCenters() {
	searchCenters(1);
}

function searchCenters(currentPage) {
	var ctable = document.getElementById('avlbl_centers_table');
	for (var i=1; i<ctable.rows.length-1; ) {
		ctable.deleteRow(i);
	}
	var curPage = 1;
	if (!empty(currentPage))
		curPage = parseInt(currentPage);
	var stateId = document.getElementById('d_state').value;
	var cityId = document.getElementById('d_city').value;

	var centers = null;
	if (!empty(cityId))
		centers = filterList(centersJSON, "city_id", cityId);
	else if (!empty(stateId))
		centers = filterList(centersJSON, "state_id", stateId);
	else if (empty(cityId) && empty(stateId))
		centers = centersJSON;

	var numPages = 0;
	if (!empty(centers)) {
		numPages = centers.length/10;
	}
	generatePaginationSection(curPage, numPages);
	var offSet = curPage == 1 ? 0 : ((curPage-1)*10);
	if (!empty(centers)) {
		for (var i=offSet; i<offSet+10; i++) {
			if (i==centers.length) break;
			if (centers[i].status != 'A') continue;

			var id = getNumCharges('avlbl_centers_table');
		   	var table = document.getElementById("avlbl_centers_table");
		   	var templateRow = table.rows[getTemplateRow('avlbl_centers_table')];
			var row = templateRow.cloneNode(true);
			row.style.display = '';
			table.tBodies[0].insertBefore(row, templateRow);
		   	row.id = "avbl_center_row" + id;

			setHiddenValue(id, "d_center_name", centers[i].center_name);
			setHiddenValue(id, "d_state_name", centers[i].state_name);
			setHiddenValue(id, "d_city_name", centers[i].city_name);

			document.getElementsByName('d_center_chkbox')[id].value = centers[i].center_id;
			setNodeText(row.cells[1], centers[i].state_name, 20);
			setNodeText(row.cells[2], centers[i].city_name, 20);
			setNodeText(row.cells[3], centers[i].center_name, 20);
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
			label.setAttribute('onclick', 'searchCenters('+(curPage-1)+')');
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
			label.setAttribute('onclick', 'searchCenters('+(curPage+1)+')');
			label.setAttribute('style', 'text-decoration:none; color:#336699; cursor: pointer');

			label.appendChild(txtEl);
			div.appendChild(label);
		}

	}

}

function cancelCenterDialog() {
	addCenterDialog.cancel();
}

var centerIndex = 0;
var CNTR_STATE_NAME = centerIndex++, CNTR_CITY_NAME = centerIndex++, CENTER_NAME = centerIndex++,
	CENTER_STATUS = centerIndex++, CNTR_TRASH_COL = centerIndex++;
function addToCenterTable() {
	var center_chkboxes = document.getElementsByName('d_center_chkbox');
	var state_name = document.getElementsByName('d_state_name');
	var city_name = document.getElementsByName('d_city_name');
	var center_name = document.getElementsByName('d_center_name');
	for (var i=0; i<center_chkboxes.length-1; i++) {
		var center_id = center_chkboxes[i].value;

		if (!center_chkboxes[i].checked) continue;
		if (isDuplicate('C', center_id)) continue;

		var id = getNumCharges('centers_table');
	   	var table = document.getElementById("centers_table");
		var templateRow = table.rows[getTemplateRow('centers_table')];
		var row = templateRow.cloneNode(true);
		row.style.display = '';
		table.tBodies[0].insertBefore(row, templateRow);
	   	row.id = "centerRow" + id;

		setNodeText(row.cells[CNTR_STATE_NAME], state_name[i].value);
		setNodeText(row.cells[CNTR_CITY_NAME], city_name[i].value);
		setNodeText(row.cells[CENTER_NAME], center_name[i].value);
		setNodeText(row.cells[CENTER_STATUS], 'Active');

		setHiddenValue(id, "package_center_id", "_");
		setHiddenValue(id, "center_name", center_name[i].value);
		setHiddenValue(id, "state_name", state_name[i].value);
		setHiddenValue(id, "city_name", city_name[i].value);
		setHiddenValue(id, "center_id", center_id);
		setHiddenValue(id, "center_status", "A");


	}
	clearCenterFields();
	addCenterDialog.align("tr", "tl");
	return id;
}

function clearCenterFields() {
	var table = document.getElementById('avlbl_centers_table');
	for (var i=1; i<table.rows.length-1; ) {
		table.deleteRow(i);
	}
	document.getElementById('d_state').value = '';
	document.getElementById('d_city').value = '';
	populateCities();
	document.getElementById('show_all_centers_chkbox').checked = false;
	var div = document.getElementById('paginationDiv');
	div.innerHTML = '';
}

var editCenterDialog = null;
function initEditCenterDialog() {
	var dialogDiv = document.getElementById("editCenterDialog");
	if (dialogDiv == undefined) return;

	dialogDiv.style.display = 'block';
	editCenterDialog = new YAHOO.widget.Dialog("editCenterDialog",
			{	width:"400px",
				context : ["editCenterDialog", "tr", "br"],
				visible:false,
				modal:true,
				fixedcenter: false,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('edit_center_ok', 'click', editCenterRow, editCenterDialog, true);
	YAHOO.util.Event.addListener('edit_center_cancel', 'click', cancelEditCenterDialog, editCenterDialog, true);
	YAHOO.util.Event.addListener('edit_center_previous', 'click', openPreviousCenter, editCenterDialog, true);
	YAHOO.util.Event.addListener('edit_center_next', 'click', openNextCenter, editCenterDialog, true);

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelEditCenterDialog,
	                                                scope:editCenterDialog,
	                                                correctScope:true } );
	editCenterDialog.cfg.setProperty("keylisteners", [escKeyListener]);
	editCenterDialog.render();
}

function showEditCenterDialog(obj) {
	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);
	editCenterDialog.cfg.setProperty("context", [obj, "tr", "tl"], false);
	editCenterDialog.show();

	YAHOO.util.Dom.addClass(row, 'editing');
	document.getElementById('center_edit_row_id').value = id;
	document.getElementById('ed_center_label').textContent = getIndexedValue('center_name', id);
	document.getElementById('ed_state_label').textContent = getIndexedValue('state_name', id);
	document.getElementById('ed_city_label').textContent = getIndexedValue('city_name', id);
	document.getElementById('ed_center_status').value = getIndexedValue('center_status', id);

	return false;

}

function editCenterRow() {
	var id = document.getElementById('center_edit_row_id').value;
	var row = getChargeRow(id, 'centers_table');

	var status = document.getElementById('ed_center_status').value;
	setNodeText(row.cells[CENTER_STATUS], status == 'A' ? 'Active' : 'Inactive');
	setHiddenValue(id, "center_status", status);

	setIndexedValue("cntr_edited", id, 'true');
	setCenterRowStyle(id);

	editCenterDialog.cancel();
	return true;
}

var centerFieldEdited = false;
function setCenterFieldEdited() {
	centerFieldEdited = true;
}

function openPreviousCenter() {
	var id = document.getElementById('center_edit_row_id').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'centers_table');

	if (centerFieldEdited) {
		if (!editCenterRow()) return false;
		centerFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}
	if (id != 0) {
		showEditCenterDialog(document.getElementsByName('_editCenterAnchor')[parseInt(id)-1]);
	}
}

function openNextCenter() {
	var id = document.getElementById('center_edit_row_id').value;
	id = parseInt(id);
	var row = getChargeRow(id, 'centers_table');

	if (centerFieldEdited) {
		if (!editCenterRow()) return false;
		centerFieldEdited = false;
	} else {
		YAHOO.util.Dom.removeClass(row, 'editing');
		this.cancel();
	}

	if (id+1 != document.getElementById('centers_table').rows.length-2) {
		showEditCenterDialog(document.getElementsByName('_editCenterAnchor')[parseInt(id)+1]);
	}
}

function cancelEditCenterDialog() {
	sponsorFieldEdited = false;
	var id = document.getElementById("center_edit_row_id").value;
	var row = getChargeRow(id, "centers_table");
	YAHOO.util.Dom.removeClass(row, 'editing');
	editCenterDialog.cancel();
}

function setCenterRowStyle(i) {
	var row = getChargeRow(i, 'centers_table');
	var packCenterId = getIndexedValue("package_center_id", i);

 	var flagImgs = row.cells[CNTR_STATE_NAME].getElementsByTagName("img");
	var trashImgs = row.cells[CNTR_TRASH_COL].getElementsByTagName("img");

	var added = (packCenterId.substring(0,1) == "_");
	var cancelled = getIndexedValue("cntr_delete", i) == 'true';
	var edited = getIndexedValue("cntr_edited", i) == 'true';


	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var flagSrc;
	var showFlag;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
		showFlag = true;
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
		showFlag = false;
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0]) {
		flagImgs[0].src = flagSrc;
		flagImgs[0].style.display = showFlag ? 'block' : 'none';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function cancelCenter(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	if (foundDuplicate('C', getIndexedValue("center_id", id), id)) {
		alert("Center alreday exists.");
		return false;
	}
	var oldDeleted =  getIndexedValue("cntr_delete", id);
	var isNew = getIndexedValue("package_center_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		sponsorAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("cntr_delete", id, newDeleted);
		setIndexedValue("cntr_edited", id, "true");
		setCenterRowStyle(id)
	}

	return false;
}


function setSponsorRowStyle(i) {
	var row = getChargeRow(i, 'sponsors_table');
	var packSponsorId = getIndexedValue("package_sponsor_id", i);

 	var flagImgs = row.cells[SPONSOR_NAME].getElementsByTagName("img");
	var trashImgs = row.cells[SPONSOR_TRASH_COL].getElementsByTagName("img");

	var added = (packSponsorId.substring(0,1) == "_");
	var cancelled = getIndexedValue("tpa_delete", i) == 'true';
	var edited = getIndexedValue("tpa_edited", i) == 'true';


	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var flagSrc;
	var showFlag;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
		showFlag = true;
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
		showFlag = false;
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0]) {
		flagImgs[0].src = flagSrc;
		flagImgs[0].style.display = showFlag ? 'block' : 'none';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}

function setPlanRowStyle(i) {
	var row = getChargeRow(i, 'plans_table');
	var packPlanId = getIndexedValue("package_plan_id", i);

 	var flagImgs = row.cells[PLAN_NAME].getElementsByTagName("img");
	var trashImgs = row.cells[PLAN_TRASH_COL].getElementsByTagName("img");

	var added = (packPlanId.substring(0,1) == "_");
	var cancelled = getIndexedValue("plan_delete", i) == 'true';
	var edited = getIndexedValue("plan_edited", i) == 'true';


	var cls;
	if (added) {
		cls = 'added';
	} else if (edited || cancelled) {
		cls = 'edited';
	} else {
		cls = '';
	}

	var flagSrc;
	var showFlag;
	if (cancelled) {
		flagSrc = cpath + '/images/red_flag.gif';
		showFlag = true;
	} else {
		flagSrc = cpath + '/images/empty_flag.gif';
		showFlag = false;
	}

	var trashSrc;
	if (cancelled) {
		trashSrc = cpath + '/icons/undo_delete.gif';
	} else {
		trashSrc = cpath + '/icons/delete.gif';
	}

	row.className = cls;

	if (flagImgs && flagImgs[0]) {
		flagImgs[0].src = flagSrc;
		flagImgs[0].style.display = showFlag ? 'block' : 'none';
	}

	if (trashImgs && trashImgs[0])
		trashImgs[0].src = trashSrc;
}


function cancelSponsor(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	if (foundDuplicate('S', getIndexedValue("tpa_id", id), id)) {
		alert("Sponsor alreday exists.");
		return false;
	}
	var oldDeleted =  getIndexedValue("tpa_delete", id);
	var isNew = getIndexedValue("package_sponsor_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		sponsorAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("tpa_delete", id, newDeleted);
		setIndexedValue("tpa_edited", id, "true");
		setSponsorRowStyle(id);
	}

	return false;
}

function cancelPlan(obj) {

	var row = getThisRow(obj);
	var id = getRowChargeIndex(row);

	if (foundDuplicate('S', getIndexedValue("plan_id", id), id)) {
		alert("Plan alreday exists.");
		return false;
	}
	var oldDeleted =  getIndexedValue("plan_delete", id);
	var isNew = getIndexedValue("package_plan_id", id) == '_';

	if (isNew) {
		// just delete the row, don't marke it as deleted
		row.parentNode.removeChild(row);
		sponsorAdded--;

	} else {
		var newDeleted;
		if (oldDeleted == 'true'){
			newDeleted = 'false';
		} else {
			newDeleted = 'true';
		}
		setIndexedValue("plan_delete", id, newDeleted);
		setIndexedValue("plan_edited", id, "true");
		setPlanRowStyle(id);
	}

	return false;
}

function getNumCharges(tableId) {
	// header, hidden template row: totally 2 extra
	return document.getElementById(tableId).rows.length-2;
}

function getFirstItemRow() {
	// index of the first charge item: 0 is header, 1 is first charge item.
	return 1;
}

function getTemplateRow(tableId) {
	// gets the hidden template row index: this follows header row + num charges.
	return getNumCharges(tableId) + 1;
}

function getChargeRow(i, tableId) {
	i = parseInt(i);
	var table = document.getElementById(tableId);
	return table.rows[i + getFirstItemRow()];
}

function getRowChargeIndex(row) {
	return row.rowIndex - getFirstItemRow();
}

function getThisRow(node) {
	return findAncestor(node, "TR");
}

function getIndexedPaise(name, index) {
	return getElementPaise(getIndexedFormElement(document.package_applicable_form, name, index));
}

function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(document.package_applicable_form, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(document.package_applicable_form, name, index);
	if (obj)
		return obj.value;
	else
	return null;
}


function setHiddenValue(index, name, value) {
	var el = getIndexedFormElement(document.package_applicable_form, name, index);
	if (el) {
		if (value == null || value == undefined)
			value = "";
		el.value = value;
	}
}


