/*
 * Functions used in list.jsp
 */
function initServiceAc() {
	var datasource = new YAHOO.widget.DS_JSArray(serviceNames);
	var autoComp = new YAHOO.widget.AutoComplete('service_name','serviceAcContainer', datasource);
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

function deselectAllBedTypes(){
	document.updateform.allBedTypes.checked = false;
}

function changeRatePlan(){
	document.searchform.submit();
}

function selectAllPageServices() {
	var checked = document.listform.allPageServices.checked;
	var length = document.listform.selectService.length;

	if (length == undefined) {
		document.listform.selectService.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectService[i].checked = checked;
		}
	}
}

function onChangeAllServices() {
	var val = getRadioSelection(document.updateform.allServices);
	// if allServices = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPageServices.disabled = disabled;
	listform.allPageServices.checked = false;

	var length = listform.selectService.length;

	if (length == undefined) {
		listform.selectService.disabled = disabled;
		listform.selectService.checked  = false;
	} else {
		for (var i=0;i<length;i++) {
			listform.selectService[i].disabled = disabled;
			listform.selectService[i].checked = false;
		}
	}
}

function doGroupUpdate() {

	var updateform = document.updateform;
	var listform = document.listform;
	updateform.org_id.value = document.searchform.org_id.value;

	var anyServices = false;
	var allServices = getRadioSelection(document.updateform.allServices);
	if (allServices == 'yes') {
		anyServices = true;
	} else {
		var div = document.getElementById("serviceListInnerHtml");
		while (div.hasChildNodes()) {
			div.removeChild(div.firstChild);
		}

		var length = listform.selectService.length;
		if (length == undefined) {
			if (listform.selectService.checked ) {
				anyServices = true;
				div.appendChild(makeHidden("selectService", "", listform.selectService.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectService[i].checked){
					anyServices = true;
					div.appendChild(makeHidden("selectService", "", listform.selectService[i].value));
				}
			}
		}
	}

	if (!anyServices) {
		alert('Select at least one Service for updation');
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
		alert("Select any update option");
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
	if (form.xlsServiceFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	}
	form.org_id.value = document.searchform.org_id.value;
	}else{
	var form = document.uploadserviceform;
	if (form.xlsServiceFile.value == "") {
		alert("Please browse and select a file to upload");
		return false;
	   }
	}
	form.submit();
}

var toolBar = {
			Edit : {
				title : 'View/Edit',
				imageSrc : 'icons/Edit.png',
				href  : 'master/ServiceMaster.do?_method=show',
				onclick : null,
				description : 'View and/or Edit the contents of this Service'
				},
			EditCharges : {
				title : 'Edit Charges',
				imageSrc : 'icons/Edit.png',
				href  : 'master/ServiceMaster.do?_method=showCharges',
				onclick : null,
				description : 'View and/or Edit the charges of this Service'
				},
			ServiceAuditLog: {
				title : 'Audit Log',
				imageSrc : 'icons/Edit.png',
				href : 'services/auditlog/AuditLogSearch.do?_method=getAuditLogDetails&al_table=services_audit_log_view',
				description: 'View changes made to this Service'
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
	if (serviceDefaultDuration !== -1) {
		document.getElementById('service_duration').value = serviceDefaultDuration;
	}
	document.inputForm.service_name.focus();
}

function doSave() {
	var form = document.inputForm;
	form.service_name.value = trim(form.service_name.value);
	if(form.service_name.value==""){
		alert('Service Name is required');
		form.service_name.focus();
		return false;
	}
	var deptName = document.getElementById("serv_dept_id").value;
	if(deptName == ""){
		alert('Service Department is required');
		form.serv_dept_id.focus();
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
	var serviceDuration = document.getElementById('service_duration').value;
	if (serviceDuration < 1 || serviceDuration > 300) {
		alert("Service Duration must be between 1 and 300");
		document.getElementById('service_duration').focus();
		return false;
	}
	form.submit();
}

function changeRatePlanAddShow() {
	document.showChargesForm.org_id.value = document.chargesForm.org_id.value;
	document.showChargesForm.submit();
}

function validateAllDiscounts() {
	var len = document.chargesForm.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('unit_charge','discount',i);
	}
	if(!valid) return false;
	else return true;
}

function doClose() {
	window.location.href = cpath + '/master/ServiceMaster.do?_method=list&status=A&sortOrder=service_name&sortReverse=false';
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
	var deptId = document.inputForm.serv_dept_id.value;
	ajaxForOrderCode('Service',deptId,group,subGroup,document.inputForm.service_code);
}

function doSaveCharges() {
	if(!validateAllDiscounts()) return false;
	document.chargesForm.submit();
}
