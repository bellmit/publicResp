//Clears sponsor_approval_detail_id.value in order for the item to be considered as a new item
function clearDetailsIfCopy() {
	//if user is on copy Gl page
	if(copy == true){
		document.PatientApprovalForm.sponsor_approval_id.value = "";
		//dont insert items that are marked to be deleted
		markedItems = document.getElementsByName('deleted');
		rowLength = document.getElementById("patientApprovalTable").rows.length - 3;
		
		//sponsor_approval_detail_value on being set to null is treated as a newitem in mapNewBeans function
		for(index = 0; index<rowLength; index++)
		{
			document.getElementById("sponsor_approval_detail_id" + index).value = "";
		}		
	}
		
}
//clears set fields, used for copying a GL
function initIfCopy()
{
	if(copy == true){
		document.PatientApprovalForm.priority.value = "";
		document.PatientApprovalForm.validity_start.value = "";
		document.PatientApprovalForm.validity_end.value = "";
		document.PatientApprovalForm.approved_by.value = "";
		document.PatientApprovalForm.approval_status.value = "N";
		document.PatientApprovalForm.approval_no.value = "";
		document.getElementById('priority').value = "";
		//Clear existed priority to re-check duplicate priority correctly
		existedPriority = "";
		rowLength = document.getElementById("patientApprovalTable").rows.length - 3;
		for(index = rowLength - 1; index > 0; index--)
		{
			//if item is deleted then remove it
			if(details[index].item_status == "X")
			{	
				document.getElementById("patientApprovalTable").deleteRow(index + 1);
			}		
		}
	}
}


function patientApprovalFormValidate(btnVal){
	var mrNoObj = document.getElementById('mr_no');
	var centerIdObj = document.getElementById('primary_center_id');
	var tpaIdObj = document.getElementById('sponsor_id');
	var approvalNoObj = document.getElementById('approval_no');
	var orgIdObj = document.getElementById('org_id');
	var priorityObj = document.getElementById('priority');
	var validityStartObj = document.getElementById('validity_start');
	var validityEndObj = document.getElementById('validity_end');
	var from_date = validityStartObj.value.trim();
    var to_date = validityEndObj.value.trim();
    var from_date_array = from_date.split('-');
    var to_date_array = to_date.split('-');

    var fromDate = new Date();
    fromDate.setFullYear(from_date_array[2],from_date_array[1]-1,from_date_array[0]);
    var toDate = new Date();
    toDate.setFullYear(to_date_array[2],to_date_array[1]-1,to_date_array[0]);

    if (!validateRequired(mrNoObj,"MrNo is required"))
		return false;

    if (!validateRequired(centerIdObj,"Primary Center is required"))
		return false;

    if (!validateRequired(tpaIdObj,"Sponsor is required"))
		return false;

    if (!validateRequired(approvalNoObj,"Approval Id is required"))
		return false;

	if (!validateRequired(orgIdObj,"Rate Plan is required"))
		return false;

	if (!validateRequired(priority,"Priority is required"))
		return false;

	if (!validateRequired(validityStartObj,"Validity From is required"))
		return false;

	if (!validateRequired(validityEndObj,"Validity To is required"))
		return false;

	if(null != validityEndObj && !empty(validityEndObj.value)) {
		var validityEndValue = validityEndObj.value;
		var curdate= new Date();
		curdate.setHours(0);
		curdate.setMinutes(0);
		curdate.setSeconds(0);
		curdate.setMilliseconds(0);
		curdate = curdate.getTime();

		var validityEnd =new Date(getDateFromField(validityEndObj));
		var validityTo=validityEnd.getTime();

		if (validityTo < curdate) {
			/*alert("Validity To cannot be Past Date");
			 document.getElementById('validity_end').value="";
		     document.getElementById('validity_end').focus();*/
			//return false;
		}
	}

	if(from_date && to_date) {
	   if (fromDate > toDate) {
	      alert("Validity To date cannot be less than Validity From date ");
	      document.getElementById('validity_end').value="";
	      document.getElementById('validity_end').focus();
	      return false;
	   }
    }
	checkDuplicatePriority(priorityObj);
	var validateAprDet = validatePatientApprovalDetails();
	if(!validateAprDet)
		return false;

	if(btnVal == 'A') {
		setApprovalStatus();
	} else if(btnVal == 'P') {
		document.PatientApprovalForm._method.value='processPreviousMonthsOrders';
	}
	clearDetailsIfCopy();
	document.PatientApprovalForm.submit();
	return true;
}

function setApprovalStatus(){
	document.getElementById('approval_status').value = 'Y';
	document.getElementById('approved_by').value = approvedBy;
}

var patientApprovalDialogForm;
var patientApprovalDialog;
var gRowUnderEdit = -1;
var gRowItems = [];
var items = null;
var gColIndexes = [];
var dataGrid = null;

var noOfItems = 0;

function init() {
	initItemAutoComplete();
	dataGrid = initDataGrid("patientApprovalTable", details); // , null /*{actions:{insert:true, delete:true, edit:true}}*/);
	dataGrid.subscribe({
		onEditRow: function(row) {
			var hidId = document.getElementById("applicable_to_id").value;
			var hidName = document.getElementById("applicable_to_name").value;
			var hidActivity = document.getElementById("activity_type").value;
			var applicableTo = document.getElementById("applicable_to");
			onChangeApplicableto(applicableTo);
			/*if (applicableTo.value == 'S') {
				document.getElementById("applicable_to_id_sg").value = hidId;
				document.getElementById("applicable_to_id").value = hidId;
				document.getElementById("applicable_to_name").value = hidName;
			} else if (applicableTo.value == 'I') { */
				document.getElementById("applicable_to_name_item").value = hidName;
				document.getElementById("applicable_to_id").value = hidId;
				document.getElementById("applicable_to_name").value = hidName;
				document.getElementById("activity_type").value = hidActivity;
			//}
				clearItemsDiv();
				createItemDivs(hidId, hidName);
				document.getElementById("applicable_to_name_item").value = "";
				document.getElementById("applicable_to_id").value = "";

		},
		onDeleteRow : function(row) {
		},
		onAddRow : function(row) {
			var applicableTo = document.getElementById("applicable_to");
			onChangeApplicableto(applicableTo);
			var st = document.getElementById("item_status");
			if (null != st) {
				st.value = "A";
			}
		},
		onUpdateRow : function(row, data) {
		}
	});
	patientApprovalDialogForm = document.patientApprovalDialogForm;
	patientApprovalDialog = dataGrid.getEditor();
	var cl = 0;
	gColIndexes.applicable_to_name = cl++;
	gColIndexes.limit_value = cl++;
	gColIndexes.copay_value = cl++;
	TRASH_COL = cl++;
	EDIT_COL = cl++;
	if(method == 'add'){
		document.getElementById('primary_center_id').value = primaryCenterId;
	}

	truncateItemName();
	initIfCopy();
}

function truncateItemName(){
	var table = document.getElementById("patientApprovalTable");
	var rows = table.rows;
	var len = table.rows.length - 2;
	for(var i=0; i<len-1; i++) {
		var rowObj = table.rows[i+1];
		var tds = rowObj.getElementsByTagName('td');
		var itemName = document.getElementById("applicable_to_name"+i).value;

		var hiddenObj = getElementByAttribute(rowObj, "lbl_applicable_to_name", "id");
		setNodeText(hiddenObj, truncateText(itemName, 80));

		if (itemName.length > 80) {
			tds[0].setAttribute("title", itemName);
		}
	}
}

function createItemDivs(itemIds, itemNames){
	var itemNameList = itemNames.split(';');
	var itemIdList = itemIds.split(';');
	var parentdiv = document.getElementById('parent_div');
	noOfItems = itemNameList.length;

	for(var i=0; i<itemNameList.length; i++){

		var childDiv = document.createElement("div");
		childDiv.setAttribute("id", 'child_div_'+i);

		var appId = document.createElement("INPUT");
	    appId.setAttribute("type", "hidden");
	    appId.setAttribute("name", "item_applcable_to_id");
	    appId.setAttribute("id", "item_applcable_to_id"+i);
	    appId.setAttribute("value", itemIdList[i]);

	    var appName = document.createElement("INPUT");
	    appName.setAttribute("type", "hidden");
	    appName.setAttribute("name", "item_applcable_to_name");
	    appName.setAttribute("id", "item_applcable_to_name"+i);
	    appName.setAttribute("value", itemNameList[i]);

	    var cancelled = document.createElement("INPUT");
	    cancelled.setAttribute("type", "hidden");
	    cancelled.setAttribute("name", "cancelled");
	    cancelled.setAttribute("id", "cancelled"+i);
	    cancelled.setAttribute("value", false);

	    childDiv.appendChild(appId);
	    childDiv.appendChild(appName);
	    childDiv.appendChild(cancelled);

		parentdiv.appendChild(childDiv);

		var itemLabel = document.createElement("label");
		itemLabel.setAttribute("name", "child_item_name");
		itemLabel.setAttribute("id", "child_item_name"+i);

		var txtEl = document.createTextNode(itemNameList[i]+" ");
		itemLabel.appendChild(txtEl);

		childDiv.appendChild(itemLabel);

		var inp = document.createElement("img");
		inp.setAttribute("src", cpath + "/images/Cancel.png");
		inp.setAttribute("name", "delItem");
		inp.setAttribute("id", "delItem"+i);
		inp.setAttribute("title", "Cancel Item");
		inp.setAttribute("onclick", "deleteItem(this, child_item_name"+i+", "+"child_div_"+i+" , "+i+")");
		itemLabel.appendChild(inp);
	}

}

function initItemAutoComplete() {
	// getOrderableItems JSON result
	getItems();
	var ds = new YAHOO.util.LocalDataSource({result : items});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;

	ds.responseSchema = { resultsList : "result",
			fields: [ {key: "name"}, {key: "code"}, {key: "type"}, {key: "id"}, {key: "department"},
			          {key: "groupid"},{key: "subgrpid"},{key: "conduction_applicable"},
			          {key: "prior_auth_required"},{key:"insurance_category_id"},{key:"conducting_doc_mandatory"},
			          {key: "results_entry_applicable"},{key: "tooth_num_required"},{key: "multi_visit_package"}],  };

	var rAutoComp = new YAHOO.widget.AutoComplete('applicable_to_name_item','itemcontainer', ds);
	rAutoComp.minQueryLength = 0;
 	rAutoComp.maxResultsDisplayed = 20;
    rAutoComp.forceSelection = true;
 	rAutoComp.animVert = false;
 	rAutoComp.resultTypeList = false;
 	rAutoComp.typeAhead = true;
 	rAutoComp.allowBrowserAutocomplete = false;
 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
	rAutoComp.autoHighlight = false;
	rAutoComp.useShadow = false;
	rAutoComp.queryMatchContains = true;

	var myHandler = function(type, args) {
		var objData = args[2];

 		document.getElementById("applicable_to_id").value = objData.id;
 		document.getElementById("activity_type").value = objData.type;
 		document.getElementById("applicable_to_name_item").value = objData.name;
        document.getElementById("applicable_to_name").value = objData.name;
       // checkDuplicateServiceGroupAndItem(document.getElementById("applicable_to_id"));
	};

	var clearHandler = function(type, args) {
		var ac = document.getElementById("applicable_to_name_item");
		if (empty(ac.value)) {
			document.getElementById("applicable_to_id").value = '';
			document.getElementById("applicable_to_name").value = '';
			document.getElementById("activity_type").value = '';
		}
	}

	rAutoComp.itemSelectEvent.subscribe(myHandler);
	rAutoComp.textboxBlurEvent.subscribe(clearHandler);
}

function getItems() {
	var url =  cpath + "/master/orderItems.do?method=getOrderableItems&"+getString("js.common.message.insta.software.version")+"&"+sesHospitalId+"&mts="+masterTimeStamp;
		url = url + "&filter=&filter=Equipment,Laboratory,Radiology,Service,Operation,Package,MultiVisitPackage&orderable=Y";
		url = url + "&orgId="+document.getElementById("org_id").value+"&visitType=o";
		url = url + "&tpa_id="+document.getElementById("sponsor_id").value+"&isMultiVisitPackage=true";

	var ajaxReqObject = newXMLHttpRequest();
		ajaxReqObject.open("POST", url.toString(), false);
		ajaxReqObject.send(null);
		if (ajaxReqObject.readyState == 4) {
			if ((ajaxReqObject.status == 200) && (ajaxReqObject.responseText != null)) {
				eval(ajaxReqObject.responseText);
				items = rateplanwiseitems.result;
			}
		}
}

// TODO : remove unused
function showAddPatientApprovalDialog() {
	resetDetails();
	gRowUnderEdit = -1;
	document.getElementById("prevDialog").disabled = true;
	document.getElementById("nextDialog").disabled = true;

	button = document.getElementById("btnAddPatientApproval");
	patientApprovalDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	patientApprovalDialog.show();
}


//TODO : remove unused
function resetDetails(){

	if ( patientApprovalDialogForm.applicable_to ) {
		setSelectedIndex(patientApprovalDialogForm.applicable_to, 0);
		document.getElementById("itemAutoComp").style.display = "none";
		document.getElementById("service_grp_category").style.display = "none";
		document.getElementById("applicable_to_type").style.display = "block";
		document.getElementById("applicable_to_name_item").value = '';
		document.getElementById("applicable_to_id_sg").value = '';
		document.getElementById("applicable_to_type").value = '';
		document.getElementById("applicable_to_type").disabled = true;
	}

	if ( patientApprovalDialogForm.limit_type ) {
		setSelectedIndex(patientApprovalDialogForm.limit_type, 0);
		patientApprovalDialogForm.limit_value.value = '';
		patientApprovalDialogForm.limit_value.disabled = true;
	}

	if ( patientApprovalDialogForm.copay_type ) {
		setSelectedIndex(patientApprovalDialogForm.copay_type, 0);
		patientApprovalDialogForm.copay_value.value = '';
		patientApprovalDialogForm.copay_value.disabled = true;
	}
}

function onDialogSave () {
	if (dlgValidate ()) {
		dlgSave();
	}
}

function dlgSave() {
	var data = {
			applicable_to : '',
			applicable_to_id: '',
			applicable_to_name:'',
			limit_type:'',
			limit_value:'',
			copay_type:'',
			copay_value:'',
			item_status:''};
	formToObject(document.patientApprovalDialogForm, data)
	var editedRow = dataGrid.getEditedRow();
	if (editedRow == -1) {
		dataGrid.insertDataRow(data);
		addMultipleItemsToGrid();
		clearFormAll(document.patientApprovalDialogForm);
	} else {
		dataGrid.updateDataRow(editedRow, data);
		addMultipleItemsToGrid();
		clearFormAll(document.patientApprovalDialogForm);
	}
	//addDialogToGrid(dataGrid, data);

/*	var rownumber = gRowUnderEdit;
	if (rownumber == -1) {

		addDialogToGrid();
		patientApprovalDialog.cancel();
		showAddPatientApprovalDialog();		// add another
	} else {
  		setDataFromDialogToGrid();
		patientApprovalDialog.cancel();	// save and stay
    }
*/

}

function addMultipleItemsToGrid(){
	var rowNum;
	var editedRow = dataGrid.getEditedRow();
	if (editedRow == -1) {
		rowNum = document.getElementById("patientApprovalTable").rows.length-4;
	} else {
		rowNum = editedRow;
	}
	//var rowNum = document.getElementById("patientApprovalTable").rows.length-4;
	var itemApplicableToIds = document.getElementsByName("item_applcable_to_id");

	var lbl_applicable_to_name = document.getElementById("lbl_applicable_to_name");
	var applicable_to_name = document.getElementById("applicable_to_name"+rowNum);
	var applicable_to_id = document.getElementById("applicable_to_id"+rowNum);

	for(var i=0; i<itemApplicableToIds.length; i++){
		var itemId = document.getElementById("item_applcable_to_id"+i).value;
		var itemName = document.getElementById("item_applcable_to_name"+i).value;
		var isCancelled = document.getElementById("cancelled"+i).value;

		if(isCancelled == "false"){
			if(applicable_to_id.value == ''){
				applicable_to_id.value = itemId;
				applicable_to_name.value = itemName;
			}
			else{
				applicable_to_id.value = applicable_to_id.value + ";" + itemId;
				applicable_to_name.value = applicable_to_name.value + ";" + itemName;
			}
		}
	}

	document.getElementById("item_status"+rowNum).value = 'A';
	var row = document.getElementById("patientApprovalTable").rows[rowNum+1];
	var hiddenObj = getElementByAttribute(row, "lbl_applicable_to_name", "id");

	setNodeText(hiddenObj, truncateText(applicable_to_name.value, 80));
	var tds = row.getElementsByTagName('td');
	if(applicable_to_name.value.length > 80){
		tds[0].setAttribute("title",applicable_to_name.value);
	}


	clearItemsDiv();
}

// TODO : remove unused

function addDialogToGrid(grid, data) {
	var rowIndex = grid.insertDataRow(data);
	var table = document.getElementById("patientApprovalTable");
	objectToHidden(data, table.rows[rowIndex+1]);
/*	var id = addRow();
	var row = getItemRow(id);
	YAHOO.util.Dom.addClass(row, 'added');
	setHiddenFieldsFromdialogToGrid(getItemRow(id)); */
}

// TODO : remove unused
function addRow() {
	var id = getNumItems();
	var table = document.getElementById("patientApprovalTable");
	var templateRow = table.rows[getTemplateRow()];
	var row = templateRow.cloneNode(true);
	row.style.display = '';
	table.tBodies[0].insertBefore(row, templateRow);
	row.id = "itmRow"+id;
	return id;
}

//TODO : remove unused
function setHiddenFieldsFromdialogToGrid(row) {
	formToHidden(patientApprovalDialogForm, row);
/*
 * Make row labels from the hidden values in the row.
 */
	var copayType = document.patientApprovalDialogForm.copay_type.value;
	var copayValue = document.patientApprovalDialogForm.copay_value.value;
	rowHiddenToLabels(row, gColIndexes);
	if(copayType == 'P' && copayValue != null && copayValue != "") {
		var copaytxt = copayValue+'%';
		setNodeText(row.cells[gColIndexes.copay_value], copaytxt);
	}
}

function getNumItems() {
	// header, hidden template row: totally 3 extra
	return document.getElementById("patientApprovalTable").rows.length-2;
}

//TODO : remove unused
function getTemplateRow() {
	return getNumItems() + 1;
}

function getItemRow(i) {
	i = parseInt(i);
	var table = document.getElementById("patientApprovalTable");
	return table.rows[i + getFirstItemRow()];
}

function getFirstItemRow() {
	return 1;
}

function getRowItemIndex(row) {
	return row.rowIndex - getFirstItemRow();
}


function onDialogCancel() {
	patientApprovalDialog.cancel();
	if (gRowUnderEdit != -1) {
		var row = getItemRow(gRowUnderEdit);
		YAHOO.util.Dom.removeClass(row, 'editing');
	}
	clearItemsDiv();
}

// TODO : remove unused
function showEditPatientApprovalDialog(obj) {
	resetDetails();
	var row = findAncestor(obj, "TR");
	YAHOO.util.Dom.addClass(row, 'editing');
	setDataFromGridToDialog(row);
	disableSponsorHeaderLevelDetails(row);
}

//TODO : remove unused
function setDataFromGridToDialog (rowObj) {
	hiddenToForm(rowObj, patientApprovalDialogForm);
	var applicableTo = getElementByName(rowObj, "applicable_to");

	onChangeApplicableto(applicableTo);
	setApplicableToValues(rowObj);
	gRowUnderEdit = getRowItemIndex(rowObj);
	document.getElementById("prevDialog").disabled = false;
	document.getElementById("nextDialog").disabled = false;
	button = rowObj.cells[EDIT_COL];
	patientApprovalDialog.cfg.setProperty("context", [button, "tr", "br"], false);
	patientApprovalDialog.show();
}

//TODO : remove unused
function setApplicableToValues(rowObj) {
	var applicableTo = getElementByName(rowObj, "applicable_to").value;
	if(applicableTo == 'S') {
		document.getElementById("applicable_to_id_sg").value = getElementByName(rowObj, "applicable_to_id").value;
	} else if(applicableTo == 'I') {
		document.getElementById("applicable_to_name_item").value = getElementByName(rowObj, "applicable_to_name").value;
	} else {
		document.getElementById("applicable_to_name_item").value = '';
		document.getElementById("applicable_to_id_sg").value = '';
		document.getElementById("applicable_to_type").value = '';
		document.getElementById("applicable_to_type").disabled = true;
	}
}
//TODO : remove unused
function disableSponsorHeaderLevelDetails(rowObj ) {
	var applicableTo = getElementByName(rowObj, "applicable_to").value;
	var limitType = getElementByName(rowObj, "limit_type").value;
	var copayType = getElementByName(rowObj, "copay_type").value;

	if(limitType == '')
		document.patientApprovalDialogForm.limit_value.disabled = true;
	else
		document.patientApprovalDialogForm.limit_value.disabled = false;

	if(copayType == '')
		document.patientApprovalDialogForm.copay_value.disabled = true;
	else
		document.patientApprovalDialogForm.copay_value.disabled = false;
}

// TODO : remove unused
function setDataFromDialogToGrid () {
	var row = getItemRow(gRowUnderEdit);
	setHiddenFieldsFromdialogToGrid(row);
	YAHOO.util.Dom.addClass(row, 'edited');
	YAHOO.util.Dom.removeClass(row, 'editing');
}

// TODO : remove unused
function onDialogPrevNext(doNext) {
	if (!dlgValidate())
		return false;

	dlgSave();
	var gridLen = getNumItems();
	patientApprovalDialog.cancel();

	var index;
	if (!doNext) {
		index = gRowUnderEdit-1;
		if (index == -1) return;

	} else {
		index = gRowUnderEdit+1;
		if (index == gridLen || index == '')
			return;
	}
	var rowObj = getItemRow(index);
	showEditPatientApprovalDialog(rowObj);
}

function onChangeApplicableto(applicablieToObj) {
	var applicableTo = applicablieToObj.value;
	document.getElementById("applicable_to_id").value = '';
	document.getElementById("applicable_to_name").value = '';
	document.getElementById("activity_type").value = '';
	/*if(applicableTo == '') {
		document.getElementById("itemAutoComp").style.display = "none";
		document.getElementById("service_grp_category").style.display = "none";
		document.getElementById("applicable_to_type").style.display = "block";
		document.getElementById("applicable_to_type").value = '';
	} else if(applicableTo == 'S') {
		document.getElementById("applicable_to_name_item").value="";
		document.getElementById("applicable_to_type").style.display = "none";
		document.getElementById("service_grp_category").style.display = "block";
		document.getElementById("itemAutoComp").style.display = "none";
	} else *
	if(applicableTo == 'I') { */
		//document.getElementById("applicable_to_id_sg").value='';
		//document.getElementById("applicable_to_type").style.display = "none";
		document.getElementById("itemAutoComp").style.display = "block";
		//document.getElementById("service_grp_category").style.display = "none";
	//}
}

/*function onChangeServiceGroup(serviceGroupObj) {
	document.getElementById("applicable_to_id").value = serviceGroupObj.options[serviceGroupObj.selectedIndex].value;
	document.getElementById("applicable_to_name").value = serviceGroupObj.options[serviceGroupObj.selectedIndex].text;
} */

function checkDuplicateServiceGroupAndItem(applicablieToObj){
	var table = document.getElementById("patientApprovalTable");
	var applicableCategory = document.getElementById("applicable_to").value;
	for(var k=1; k<table.rows.length; k++) {
		var rowObj = table.rows[k];
		var rowApplicableToId = getElementByName(rowObj,"applicable_to_id");
		var rowApplicableCategory = getElementByName(rowObj,"applicable_to");
		var rowStatus = getElementByName(rowObj, "item_status");
		if(rowApplicableToId != null && !empty(rowApplicableToId) && rowApplicableToId != "undefined" &&
				rowApplicableCategory != null && !empty(rowApplicableCategory) && rowApplicableCategory != "undefined" ) {
			if (empty(rowStatus) || rowStatus.value == "X") {
				continue;
			}
			if((rowApplicableToId.value == applicablieToObj.value) && (rowApplicableCategory.value == applicableCategory)) {
				alert("Applicable To should not be duplicated");
				document.getElementById("applicable_to_id").value = "";
				document.getElementById("applicable_to_name").value = "";
				/*if(applicableCategory == 'S'){
					document.getElementById("applicable_to_id_sg").value =""
				} else {*/
					document.getElementById("activity_type").value = "";
					document.getElementById("applicable_to_name_item").value="";
				//}
				break;
			}
		}
	}
}

function onchangeLimitType(limitTypeObj) {
	var limitType = limitTypeObj.value;
	if(limitType == '' || limitType == 'Q' || limitType == 'A') {
		document.getElementById('limit_value').value = "";
	}
}

function onchangeCopayType(copayTypeObj) {
	var copayType = copayTypeObj.value;
	if(copayType == '' || copayType == 'P' || copayType == 'A') {
		document.getElementById('copay_value').value = "";
	}
}

function onchangeRatePlan(ratePlanListObj){
	document.getElementById('org_id').value = ratePlanListObj.value;
	document.getElementById('org_name').value = ratePlanListObj.options[ratePlanListObj.selectedIndex].text;
}

function onKeyPressAddQty(e,type) {
	var elType = null;
	if(type == 'limit')
		elType = document.getElementById('limit_type').value;
	else
		elType = document.getElementById('copay_type').value;

	if(elType == 'Q') {
		return enterNumOnlyzeroToNine(e);
	} else {
		e = (e) ? e : event;
		var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
		if ( charCode == 13 || charCode == 3 ) {
			return false;
		} else {
		return enterNumAndDot(e);
		}
	}
}

function dlgValidate(){

	var applicableToValue = document.getElementById('applicable_to_id').value;
	var limitTypeObj = document.getElementById('limit_type');
	var limitValueObj = document.getElementById('limit_value');
	var copayTypeObj = document.getElementById('copay_type');
	var copayValueObj = document.getElementById('copay_value');
	var applObjLength = document.getElementById('parent_div').childNodes.length;
	var itemApplicableToIds = document.getElementsByName("item_applcable_to_id");

	if (!validateRequired(limitTypeObj,"Limit is required"))
		return false;

	if (!validateRequired(limitValueObj,"Limit Value is required"))
		return false;

	if(limitTypeObj.value == 'A') {
		if(!validateSignedAmount(limitValueObj, "Limit Value must be a valid value"))
			return false;
	} else {
		if(!validateAmount(limitValueObj, "Limit Value must be a valid value"))
			return false;
	}

	if (!validateRequired(copayTypeObj,"Copay is required"))
		return false;

	if (!validateRequired(copayValueObj,"Copay Value is required"))
		return false;

	if(copayTypeObj.value == 'A') {
		if(!validateSignedAmount(copayValueObj, "Copay Value must be a valid value")) return false;
	} else {
		if(!validateSignedAmount(copayValueObj, "Copay Value must be a valid value")) return false;
		if(copayValueObj.value != '' && copayValueObj.value < 0 || copayValueObj.value > 100 ) {
			alert(' Copay Value should be in between 0 to 100 ');
			return false;
		}
	}

	if(applicableToValue != null && applicableToValue != ""){
		for(var i=0; i<itemApplicableToIds.length; i++){
			var itemId = document.getElementById("item_applcable_to_id"+i).value;
			if(itemId == applicableToValue)
				break;
		}
		alert('Please click on Add Item icon');
		return false;
	}

	if (applObjLength == 0 || (applObjLength != 0 && cancelledItem())){
		alert('Item is required');
		document.getElementById("applicable_to_name_item").focus();
		return false;
	}

	return true;
}

function validatePatientApprovalDetails(){
	var patApprTabLen = document.getElementById("patientApprovalTable").rows.length;
	var markedForDelete = document.getElementsByName('deleted');
	var itemStatus = document.getElementsByName('item_status');
	for (var i=0; i<markedForDelete.length-1; i++) {
		if (markedForDelete[i].value == 'true' || itemStatus[i].value == 'X') {
			patApprTabLen = patApprTabLen-1;
		}
	}
	if(patApprTabLen <= 3){
		alert('Please add atleast one item');
		return false;
	}
	return true;
}

function setApprovals(obj, idx){

	var ok = confirm(" Warning: Before changing Approval Id, Please make sure current Approval details are saved... ");

	if(ok){
		var mrNo = document.getElementById("mr_no").value;
		window.location.href = cpath+"/Insurance/PatientSponsorsApproval.do?_method=show&sponsor_approval_id="+obj.value+"&mr_no="+mrNo;
	}else{
		obj.checked = false;

		var approvalId = document.getElementById("sponsor_approval_id").value;
		var approvalNos = document.getElementsByName("selected_sponsor_approval_id");

		for(var i=0; i<approvalNos.length; i++){
			var approval = approvalNos[i];
			if(approval.value == approvalId){
				approval.checked = true;
			}
		}
	}
}

function checkDuplicatePriority(priorityObj) {
	var priority = priorityObj.value;
	if (!empty(patientApprovalJson)) {
		for (var i=0; i<patientApprovalJson.length; i++) {
			var patientApprovals = patientApprovalJson[i];
			if(existedPriority != priority){
				if(patientApprovals.priority == priority) {
					alert("Priority should not be duplicated");
					document.getElementById("priority").value = "";
					document.getElementById("priority").focus();
					break;
				}
			}
		}
	}
}

function clearItemsDiv(){
	var el = document.getElementById("parent_div");
	while ( el.firstChild ) el.removeChild( el.firstChild );
	noOfItems = 0;
}

/*function clearItemsDiv(){
	var rowNum ;
	/*var itemLbl = document.getElementsByName('child_item_name');

	if(itemLbl != null && itemLbl != 'undefined' && itemLbl != ""){
		for(var i=0; i<itemLbl.length ;i++){
			itemLbl[i].textContent = "";
		}
	}

	var editedRow = dataGrid.getEditedRow();
	if (editedRow == -1) {
		rowNum = document.getElementById("patientApprovalTable").rows.length-3;
	} else {
		rowNum = editedRow;
	}

	var parentdiv = document.createElement("div");
	parentdiv.setAttribute("id", 'parent_div');
	patientApprovalDialogForm = document.patientApprovalDialogForm;
	patientApprovalDialogForm.appendChild(parentdiv);
} */

function addItem(){

	if(document.getElementById("applicable_to_id").value == '')
		return false;

	var applName = document.getElementById("applicable_to_name").value;
	if(!validateItem()){
		document.getElementById("applicable_to_name_item").value = "";
		document.getElementById("applicable_to_id").value = "";
		document.getElementById("applicable_to_name").value ="";
		alert("Item should not be duplicated");
		return false;
	}

	var rowNum ;
	var editedRow = dataGrid.getEditedRow();
	if (editedRow == -1) {
		rowNum = document.getElementById("patientApprovalTable").rows.length-3;
	} else {
		rowNum = editedRow;
	}
	var applId = document.getElementById("applicable_to_id").value;
	var applName = document.getElementById("applicable_to_name").value;
	var actType = document.getElementById("activity_type").value;
	var parentdiv = document.getElementById('parent_div');
	var appToObj = document.getElementById("applicable_to");

	var dialogTable = document.getElementById("dialogTable");
	var lastRowIndex = dialogTable.rows.length-1;
	var lastCell = dialogTable.rows[lastRowIndex].cells[0];

	lastCell.appendChild(parentdiv);

	if(applId != null && applId != '') {
		var childDiv = document.createElement("div");
		childDiv.setAttribute("id", 'child_div_'+noOfItems);

		var appId = document.createElement("INPUT");
	    appId.setAttribute("type", "hidden");
	    appId.setAttribute("name", "item_applcable_to_id");
	    appId.setAttribute("id", "item_applcable_to_id"+noOfItems);
	    appId.setAttribute("value", applId);

	    var appName = document.createElement("INPUT");
	    appName.setAttribute("type", "hidden");
	    appName.setAttribute("name", "item_applcable_to_name");
	    appName.setAttribute("id", "item_applcable_to_name"+noOfItems);
	    appName.setAttribute("value", applName);

	    var cancelled = document.createElement("INPUT");
	    cancelled.setAttribute("type", "hidden");
	    cancelled.setAttribute("name", "cancelled");
	    cancelled.setAttribute("id", "cancelled"+noOfItems);
	    cancelled.setAttribute("value", false);

	    childDiv.appendChild(appId);
	    childDiv.appendChild(appName);
	    childDiv.appendChild(cancelled);

		parentdiv.appendChild(childDiv);

		var itemLabel = document.createElement("label");
		itemLabel.setAttribute("name", "child_item_name");
		itemLabel.setAttribute("id", "child_item_name"+noOfItems);

		var txtEl = document.createTextNode(applName+" ");
		itemLabel.appendChild(txtEl);

		childDiv.appendChild(itemLabel);

		 var inp = document.createElement("img");
			inp.setAttribute("src", cpath + "/images/Cancel.png");
			inp.setAttribute("name", "delItem");
			inp.setAttribute("id", "delItem"+noOfItems);
			inp.setAttribute("title", "Cancel Item");
			inp.setAttribute("onclick", "deleteItem(this, child_item_name"+noOfItems+", "+"child_div_"+noOfItems+" , "+noOfItems+")");
			itemLabel.appendChild(inp);

	}
	document.getElementById("applicable_to_name_item").value="";
	noOfItems++;
	onChangeApplicableto(appToObj);
}

function deleteItem(imgobj, itemLabel, childDiv, index){

	childDiv.style.display = "none";
	document.getElementById("cancelled"+index).value = true;
}

function validateItem(){
	var applId = document.getElementById("applicable_to_id").value;
	var itemApplicableToIds = document.getElementsByName("item_applcable_to_id");
	for(var i=0; i<itemApplicableToIds.length; i++){
		var itemId = document.getElementById("item_applcable_to_id"+i).value;
		var cancelled = document.getElementById("cancelled"+i).value;
		if(itemId == applId && cancelled == 'false')
			return false;
	}

	var table = document.getElementById("patientApprovalTable");

	for(var k=1; k<table.rows.length; k++) {
		var rowObj = table.rows[k];
		var rowApplicableToId = getElementByName(rowObj,"applicable_to_id");

		var rowStatus = getElementByName(rowObj, "item_status");
		if(rowApplicableToId != null && !empty(rowApplicableToId) && rowApplicableToId != "undefined" ) {
			if (empty(rowStatus) || rowStatus.value == "X") {
				continue;
			}

			var applicableToIds = rowApplicableToId.value.split(';');

			for(var i=0; i<applicableToIds.length; i++){
				if(applicableToIds[i] == applId) {
					return false;
				}
			}
		}
	}

	return true;
}

function cancelledItem(){
	var cancelledItems = document.getElementsByName("cancelled");
	for(var i=0; i<cancelledItems.length; i++){
		var cancelled = document.getElementById("cancelled"+i).value;
		if(cancelled == "false")
			return false;
	}
	return true;
}