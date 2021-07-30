function initAddShow() {
	if (document.getElementById('serviceSubGroup').value!="") {
		loadServiceSubGroup();
		setSelectedIndex(document.getElementById('service_sub_group_id'), document.getElementById('serviceSubGroup').value);
	}
	document.anaesthesiaform.anesthesia_type_name.focus();
	var baseUnit = document.getElementById('base_unit');
	if (method == 'show') {
		if (!empty(baseUnit.value)) {
			document.getElementById('base_unit_select').checked = true;
			enableDisableFields('base');
		} else {
			document.getElementById('slab_based_select').checked = true;
			enableDisableFields('slab');
		}
	} else {
		document.getElementById('slab_based_select').checked = true;
		enableDisableFields('slab');
	}
}

function enableDisableFields(str) {
	if (str == 'base') {
		document.getElementById('show_min_duration').value = '';
		document.getElementById('show_slab_1_threshold').value = '';
		document.getElementById('show_incr_duration').value = '';
		document.getElementById('min_duration').value = '0';
		document.getElementById('slab_1_threshold').value = '0';
		document.getElementById('incr_duration').value = '0';
		document.getElementById('show_base_unit').disabled = false;
		document.getElementById('show_min_duration').disabled = true;
		document.getElementById('show_slab_1_threshold').disabled = true;
		document.getElementById('show_incr_duration').disabled = true;
	} else if (str == 'slab') {
		document.getElementById('show_base_unit').value = '';
		document.getElementById('base_unit').value = '';
		document.getElementById('show_base_unit').disabled = true;
		document.getElementById('show_min_duration').disabled = false;
		document.getElementById('show_slab_1_threshold').disabled = false;
		document.getElementById('show_incr_duration').disabled = false;
	}
}

function populateHiddenVar(value,obj) {
	document.getElementById(value).value = obj.value;
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
function changeRatePlanAddShow() {
	document.showform.org_id.value = document.anaesthesiaChargesForm.org_id.value;
	document.showform.submit();
}

function initAnaesthesiaAc() {
	var datasource = new YAHOO.widget.DS_JSArray(anaesthesiaNames);
	var autoComp = new YAHOO.widget.AutoComplete('anesthesia_type_name','anaesthesiaContainer', datasource);
	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = true;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 1;
	autoComp.maxResultsDisplayed = 20;
	autoComp.autoHighlight = false;
	autoComp.forceSelection = false;
}

function selectAllPageEquipments() {
	var checked = document.listform.allPageAnaesthesiaTypes.checked;
	var length = document.listform.selectAnesthesia.length;

	if (length == undefined) {
		document.listform.selectAnesthesia.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectAnesthesia[i].checked = checked;
		}
	}
}

var toolBar = {
		 Edit: {
			title : 'View/Edit',
			imageSrc : 'icons/Edit.png',
			href : '/master/AnaesthesiaTypeMaster.do?_method=show',
			onclick : null,
			description : 'View and/or Edit the contents of this Anaesthesia Type'
		 },
 EditCharges: {
			title : 'Edit Charges',
			imageSrc : 'icons/Edit.png',
			href : '/master/AnaesthesiaTypeMaster.do?_method=showCharges',
			onclick : null,
			description : 'View and/or Edit the charges of this Anaesthesia Type'
		 }
	};

function init() {
	createToolbar(toolBar);
}

function checkDuplicates() {
var name = document.getElementById('anesthesia_type_name').value;
var id = document.anaesthesiaform.anesthesia_type_id.value;
	for (var i = 0; i<namesList.length; i++) {
		var dbName = namesList[i].ANESTHESIA_TYPE_NAME;
		var dbId = namesList[i].ANESTHESIA_TYPE_ID;
		if (id != dbId) {
			if (name == dbName) {
				alert("Anesthesia Name already exists plz enter another name");
				document.getElementById('anesthesia_type_name').focus();
				return false;
			}
		}
	}
	return true;
}

function validateSubmit(){
	var radioObj = document.getElementById('base_unit_select');
	var form = document.anaesthesiaform;
	form.anesthesia_type_name.value = trim(form.anesthesia_type_name.value);

	if(form.anesthesia_type_name.value==""){
		alert('anesthesia type name is required');
		form.anesthesia_type_name.focus();
		return false;
	}
	if (!checkDuplicates()){
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

	if(empty(document.getElementById("duration_unit_minutes").value)) {
		alert("Unit Size is required");
		document.getElementById("duration_unit_minutes").focus();
		return false;
	}
	if(document.getElementById("duration_unit_minutes").value == 0) {
		alert("Unit Size cannot be set to 0");
		document.getElementById("duration_unit_minutes").focus();
		return false;
	}

	var isInsuranceCatIdSelected = false;
	var insuranceCatId = document.getElementById('insurance_category_id');
	for (var i=0; i<insuranceCatId.options.length; i++) {
	  if (insuranceCatId.options[i].selected) {
		  isInsuranceCatIdSelected = true;
	  }
	}
	if (!isInsuranceCatIdSelected) {
		alert("Please select at least one insurance category");
		return false;
	}

	if (!radioObj.checked) {
		if(empty(document.getElementById("show_min_duration").value)) {
			alert("Minimum Duration is required");
			document.getElementById("show_min_duration").focus();
			return false;
		}
		if(empty(document.getElementById("show_slab_1_threshold").value)) {
			alert("Slab 1 Threshold is required");
			document.getElementById("show_slab_1_threshold").focus();
			return false;
		}
		if(empty(document.getElementById("show_incr_duration").value)) {
			alert("Incr Duration is required");
			document.getElementById("show_incr_duration").focus();
			return false;
		}
	} else {
		if(empty(document.getElementById("show_base_unit").value)) {
			alert("Base Unit is required");
			document.getElementById("show_base_unit").focus();
			return false;
		}

	/*	if(document.getElementById("show_base_unit").value == 0) {
			alert("Base Unit cannot be set to 0");
			document.getElementById("show_base_unit").focus();
			return false;
		}*/
	}

	form.submit();
}

function validateAllDiscounts() {
	var len = document.anaesthesiaChargesForm.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('min_charge','min_charge_discount',i);
		valid = valid && validateDiscount('incr_charge','incr_charge_discount',i);
		valid = valid && validateDiscount('slab_1_charge' ,'slab_1_charge_discount', i);

	}
	if(!valid) return false;
	else return true;
}


function onChangeAllAnaesthesiaTypes() {
	var val = getRadioSelection(document.updateform.allAnaesthesiaTypes);
	// if allOperations = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPageAnaesthesiaTypes.disabled = disabled;
	listform.allPageAnaesthesiaTypes.checked = false;

	if(listform.selectAnesthesia !=null){
		var length = listform.selectAnesthesia.length;

		if (length == undefined) {
			listform.selectAnesthesia.disabled = disabled;
			listform.selectAnesthesia.checked  = false;
		} else {
			for (var i=0;i<length;i++) {
				listform.selectAnesthesia[i].disabled = disabled;
				listform.selectAnesthesia[i].checked = false;
			}
		}
	}
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

function doGroupUpdate() {
	if(document.listform.selectAnesthesia == null){
		alert("No Anesthesia Type to update charges");
		return false;
	}

	var updateform = document.updateform;
	var listform = document.listform;
	updateform.org_id.value = document.searchform.org_id.value;
	updateform.chargeType.value = document.searchform._chargeType.value;

	var anyAnaesthesiaTypes = false;
	var allAnaesthesiaTypes = getRadioSelection(document.updateform.allAnaesthesiaTypes);
	if (allAnaesthesiaTypes == 'yes') {
		anyAnaesthesiaTypes = true;
	} else {
		var div = document.getElementById("anaesthesiaTypeListInnerHtml");
		while (div.hasChildNodes()) {
			div.removeChild(div.firstChild);
		}

		var length = listform.selectAnesthesia.length;
		if (length == undefined) {
			if (listform.selectAnesthesia.checked ) {
				anyAnaesthesiaTypes = true;
				div.appendChild(makeHidden("selectAnesthesia", "", listform.selectAnesthesia.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectAnesthesia[i].checked){
					anyAnaesthesiaTypes = true;
					div.appendChild(makeHidden("selectAnesthesia", "", listform.selectAnesthesia[i].value));
				}
			}
		}
	}

	if (!anyAnaesthesiaTypes) {
		alert('Select at least one Anaesthesia Type for updation');
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

	if(formType == "AnaesthesiaUploadForm"){
		var form = document.AnaesthesiaUploadForm;
		if (form.xlsAnaesthesiaFile.value == "") {
			alert("Please browse and select a file to upload");
			return false;
		}
		form.org_id.value = document.searchform.org_id.value;
	}else{
		var form = document.AnaesthesiaDetailsUploadForm;
		if (form.xlsDetailsFile.value == "") {
			alert("Please browse and select a file to upload");
			return false;
		}
	}
	form.submit();
}

function validateChargesSubmit() {
	var form = document.anaesthesiaChargesForm;
	if(!validateAllDiscounts()) return false;

	form.submit();
}
