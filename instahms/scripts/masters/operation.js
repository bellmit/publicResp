/*
 * Functions used in list.jsp
 */
function initOperationAc() {
	var datasource = new YAHOO.widget.DS_JSArray(operationNames);
	var autoComp = new YAHOO.widget.AutoComplete('operation_name','operationAcContainer', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 1;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = false;
	autoComp.forceSelection = false;
}


function selectAllBedTypes(){
	var selected = document.updateform.allBedTypes.checked;
	var bedTypesLen = document.updateform.selectBedType.length;

	for (i=bedTypesLen-1;i>=0;i--) {
		document.updateform.selectBedType[i].selected = selected;
	}
}
function retryJobSchedule(operationId) {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/master/addeditdiagnostics/retrychargeschedule.json?entity=OPERATION&entity_id="+operationId;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			document.getElementById('entity_status_'+operationId).innerHTML = 'Processing';
			document.getElementById('error_status_'+operationId).innerHTML = '';
			document.getElementById('retry_job_'+operationId).innerHTML = '';
			alert("Retry processing");
			return JSON.parse(ajaxobj.responseText);
		}
	}
}
function deselectAllBedTypes(){
	document.updateform.allBedTypes.checked = false;
}

function changeRatePlan(){
	document.searchform.submit();
}

function selectAllPageOperations() {
	var checked = document.listform.allPageOperations.checked;
	var length = document.listform.selectOperation.length;

	if (length == undefined) {
		document.listform.selectOperation.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectOperation[i].checked = checked;
		}
	}
}

function onChangeAllOperations() {
	var val = getRadioSelection(document.updateform.allOperations);
	// if allOperations = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPageOperations.disabled = disabled;
	listform.allPageOperations.checked = false;

	var length = listform.selectOperation.length;

	if (length == undefined) {
		listform.selectOperation.disabled = disabled;
		listform.selectOperation.checked  = false;
	} else {
		for (var i=0;i<length;i++) {
			listform.selectOperation[i].disabled = disabled;
			listform.selectOperation[i].checked = false;
		}
	}
}

function doGroupUpdate() {

	var updateform = document.updateform;
	var listform = document.listform;
	updateform.org_id.value = document.searchform.org_id.value;
	updateform.chargeType.value = document.searchform._chargeType.value;

	var anyOperations = false;
	var allOperations = getRadioSelection(document.updateform.allOperations);
	if (allOperations == 'yes') {
		anyOperations = true;
	} else {
		var div = document.getElementById("operationListInnerHtml");
		while (div.hasChildNodes()) {
			div.removeChild(div.firstChild);
		}

		var length = listform.selectOperation.length;
		if (length == undefined) {
			if (listform.selectOperation.checked ) {
				anyOperations = true;
				div.appendChild(makeHidden("selectOperation", "", listform.selectOperation.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectOperation[i].checked){
					anyOperations = true;
					div.appendChild(makeHidden("selectOperation", "", listform.selectOperation[i].value));
				}
			}
		}
	}

	if (!anyOperations) {
		alert('Select at least one Surgery/Procedure for updation');
		return;
	}

	var anyBedTypes = false;
	if (updateform.allBedTypes.checked) {
		anyBedTypes = true;
	} else {
		var bedTypeLength = updateform.selectBedType.length;

		for (var i=0; i<bedTypeLength ; i++) {
			if(updateform.selectBedType.options[i].selected){
				anyBedTypes = true;
				break;
			}
		}
	}

	if (!anyBedTypes) {
		alert('Select at least one Bed Type for updation');
		return ;
	}

	if (!updateOption()) {
		alert("Select any update Surgery/Procedure");
		updateform.updateTable[0].focus();
		return ;
	}

	if (updateform.amount.value=="") {
		alert("Value required for Amount");
		updateform.amount.focus();
		return ;
	}

	if(updateform.amtType.value == '%') {
		if(getAmount(updateform.amount.value) > 100){
			alert("Discount percent cannot be more than 100");
			updateform.amount.focus();
			return false;
		}
	}

	updateform.submit();
}

function updateOption() {
	for (var i=0; i<updateform.updateTable.length ; i++) {
		if(updateform.updateTable[i].checked){
			return true;
		}
	}
	return false;
}

function doExport() {
	document.exportform.org_id.value = document.searchform.org_id.value;
	return true;
}

function doUpload(formType) {

   if(formType == "uploadform"){
	var form = document.uploadform;
	if (form.xlsOperationFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	}
	form.org_id.value = document.searchform.org_id.value;
	}else{
	 var form = document.uploadoperationform;
	if (form.xlsOperationFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	  }
	}
	form.submit();
}

var toolBar = {
		Edit : {
			title : 'View/Edit',
			imageSrc : 'images/Edit.png',
			href : '/master/OperationMaster.do?_method=show',
			onclick : null,
			description : 'View and/or Edit the contents of this Surgery/Procedure'
		},
EditCharges : {
			title : 'Edit Charges',
			imageSrc : 'images/Edit.png',
			href : '/master/OperationMaster.do?_method=showCharges',
			onclick : null,
			description : 'View and/or Edit the charges of this Surgery/Procedure'
		},
		OperationAuditLog : {
			title : 'Audit Log',
			imageSrc : 'images/Edit.png',
			href : 'operations/auditlog/AuditLogSearch.do?_method=getAuditLogDetails&al_table=operation_master_audit_log_view',
			description: 'View changes made to this item'
		}
	};

function init() {
	createToolbar(toolBar);
}
/****************************************************
 * Functions used in addshow.jsp
 ****************************************************/

function initAddShow() {
	if (document.getElementById('serviceSubGroup').value!="") {
		loadServiceSubGroup();
		setSelectedIndex(document.getElementById('service_sub_group_id'), document.getElementById('serviceSubGroup').value);
	}
	if (operationDefaultDuration !== -1) {
		document.getElementById('operation_duration').value = operationDefaultDuration;
	}
	document.inputForm.operation_name.focus();
}

function doSave() {
	var form = document.inputForm;
	form.operation_name.value = trim(form.operation_name.value);
	if(form.operation_name.value==""){
		alert('Surgery/Procedure Name is required');
		form.operation_name.focus();
		return false;
	}

	form.dept_id.value = trim(form.dept_id.value);
	if(form.dept_id.value=="") {
		alert('Department is required');
		form.dept_id.focus();
		return false;
	}
	if (document.getElementById('service_group_id').selectedIndex==0) {
		alert("Service Group is required");
		document.getElementById('service_group_id').focus();
		return false;
	}
	if (document.getElementById('service_sub_group_id').selectedIndex==0) {
		alert("Service Sub Group is required");
		document.getElementById('service_sub_group_id').focus();
		return false;
	}
	var operationDuration = document.getElementById('operation_duration').value;
	if (operationDuration < 1 || operationDuration > 300) {
		alert("Operation Duration must be between 1 and 300");
		document.getElementById('operation_duration').focus();
		return false;
	}

	if (!checkDuplicates()) return false;

	form.submit();
}

function changeRatePlanAddShow() {
	document.showform.org_id.value = document.chargesForm.org_id.value;
	document.showform.submit();
}

function validateAllDiscounts() {
	if (masterJobCount != undefined && masterJobCount > 0) {
		alert("Surgery/Procedure charge scheduler in progress");
		return false;
	}
	var len = document.chargesForm.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('surgeon_charge','surg_discount',i);
		valid = valid && validateDiscount('anesthetist_charge','anest_discount',i);
		valid = valid && validateDiscount('surg_asstance_charge','surg_asst_discount',i);
	}
	if(!valid) return false;
	else return true;
}

function doClose() {
	window.location.href = cpath + '/master/OperationMaster.do?_method=list&status=A&sortOrder=operation_name&sortReverse=false';
}

function loadServiceSubGroup() {
	var serviceGroupId = document.getElementById('service_group_id').value;
	var index = 1;
	document.getElementById("service_sub_group_id").length = 1;
	for (var i=0; i<serviceSubGroupsList.length; i++) {
		var item = serviceSubGroupsList[i];
	 	if (serviceGroupId == item["SERVICE_GROUP_ID"]) {
	 		document.getElementById("service_sub_group_id").length = document.getElementById("service_sub_group_id").length+1;
	 		document.getElementById("service_sub_group_id").options[index].text = item["SERVICE_SUB_GROUP_NAME"];
	  		document.getElementById("service_sub_group_id").options[index].value = item["SERVICE_SUB_GROUP_ID"];
	 		index++;
	 	}
	}
}
function getOrderCode(){
	var group = document.getElementById("service_group_id").value;
	var subGroup = document.getElementById("service_sub_group_id").value;
	var deptId = document.inputForm.dept_id.value;
	ajaxForOrderCode('Operation',deptId,group,subGroup,document.inputForm.operation_code);
}

function checkDuplicates() {

	var deptId = document.getElementById('dept_id').value;
	var namesAndIds = operationNames[deptId];
	var operationName = document.getElementById('operation_name').value;
	var opId = document.getElementById('OPID').value;

	for (var i=0; i<namesAndIds.length; i++) {
		if (opId != namesAndIds[i]['op_id']) {
			if (operationName == namesAndIds[i]["operation_name"]) {
				alert("Surgery/Procedure Name already exists Please enter another name");
				return false;
			}
		}
	}

	return true;

}

function submitCharges() {

	if(!validateAllDiscounts()) return false;

	document.chargesForm.submit();
}
