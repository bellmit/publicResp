function searchValidate(){
	var form = document.equipmentform;
	form.pageNum.value = 1;

	return true;
}

function initEquipmentAc() {

	YAHOO.example.equipmentNamesArray = [];
	YAHOO.example.equipmentNamesArray.length =equipmentNames.length;

	for (var i=0;i<equipmentNames.length;i++) {
		var item = equipmentNames[i]
			YAHOO.example.equipmentNamesArray[i] = item["equipment_name"];
	}

	YAHOO.example.ACJSArray = new function() {
		// Instantiate first JS Array DataSource
		datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.equipmentNamesArray);
		var autoComp = new YAHOO.widget.AutoComplete('equipment_name','equipmentContainer', datasource);
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = true;
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 1;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = false;
		autoComp.forceSelection = false;
		autoComp.textboxFocusEvent.subscribe(function() {
				var sInputValue = YAHOO.util.Dom.get('equipment_name').value;
				if(sInputValue.length === 0) {
					var oSelf = this;
					setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
				}

		});
	}
}

function validateSubmit(){

	var form = document.equipmentform;
	form.equipment_name.value = trim(form.equipment_name.value);

	if(form.equipment_name.value==""){
		alert('equipment name is required');
		form.equipment_name.focus();
		return false;
	}
	if (!checkDuplicates()){
		return false;
	}
	if(form.dept_id.selectedIndex == 0){
		alert('department name is required');
		form.dept_id.focus();
		return false;
	}
	if (document.getElementById('service_group_id').selectedIndex==0) {
		alert("Service Group is required");
		document.getElementById('service_group_id').focus();
		return false;
	}
	if (document.getElementById('serviceSubGroupId').selectedIndex==0) {
		alert("Service Sub Group is required");
		document.getElementById('serviceSubGroupId').focus();
		return false;
	}
	if(document.getElementById("duration_unit_minutes").value == 0) {
		alert("Unit Size cannot be set to 0");
		document.getElementById("duration_unit_minutes").focus();
		return false;
	}

	form.submit();
}

function checkPageNum( pageno ){
	if(searchValidate()){
		document.equipmentform.pageNum.value = pageno;
		document.equipmentform.submit();
	}
}


function selectAllPageEquipments() {
	var checked = document.listform.allPageOperations.checked;
	var length = document.listform.selectEquipment.length;

	if (length == undefined) {
		document.listform.selectEquipment.checked = checked;
	} else {
		for (var i=0;i<length;i++) {
			document.listform.selectEquipment[i].checked = checked;
		}
	}
}

function onChangeAllEquipments() {
	var val = getRadioSelection(document.updateform.allEquipment);
	// if allOperations = yes, then disable the page selections
	var disabled = (val == 'yes');

	var listform = document.listform;
	listform.allPageOperations.disabled = disabled;
	listform.allPageOperations.checked = false;

	if(listform.selectEquipment !=null){
		var length = listform.selectEquipment.length;

		if (length == undefined) {
			listform.selectEquipment.disabled = disabled;
			listform.selectEquipment.checked  = false;
		} else {
			for (var i=0;i<length;i++) {
				listform.selectEquipment[i].disabled = disabled;
				listform.selectEquipment[i].checked = false;
			}
		}
	}
}

function doGroupUpdate() {
	if(document.listform.selectEquipment == null){
		alert("No Equipments to update charges");
		return false;
	}

	var updateform = document.updateform;
	var listform = document.listform;
	updateform.org_id.value = document.searchform.org_id.value;
	updateform.chargeType.value = document.searchform._chargeType.value;

	var anyOperations = false;
	var allOperations = getRadioSelection(document.updateform.allEquipment);
	if (allOperations == 'yes') {
		anyOperations = true;
	} else {
		var div = document.getElementById("equipmentListInnerHtml");
		while (div.hasChildNodes()) {
			div.removeChild(div.firstChild);
		}

		var length = listform.selectEquipment.length;
		if (length == undefined) {
			if (listform.selectEquipment.checked ) {
				anyOperations = true;
				div.appendChild(makeHidden("selectEquipment", "", listform.selectEquipment.value));
			}
		} else {
			for (var i=0;i<length;i++) {
				if (listform.selectEquipment[i].checked){
					anyOperations = true;
					div.appendChild(makeHidden("selectEquipment", "", listform.selectEquipment[i].value));
				}
			}
		}
	}

	if (!anyOperations) {
		alert('Select at least one Operation for updation');
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


function doExport() {
	document.exportform.org_id.value = document.searchform.org_id.value;
	return true;
}

function doUpload(formType) {

	if(formType == "uploadform"){
		var form = document.uploadform;
		if (form.xlsEquipmentFile.value == "") {
			alert("Please browse and select a file to upload");
			return false;
		}
		form.org_id.value = document.searchform.org_id.value;
	}else{
		var form = document.uploadequipmentform;
		if (form.xlsEquipmentFile.value == "") {
			alert("Please browse and select a file to upload");
			return false;
		}
	}
	form.submit();
}

function initAddShow() {	
	
	if(neworedit == 'new'){
		getDepartments();
		//setSelectedIndex(document.getElementById('dept_id'), document.getElementById('departmentId').value);
	  }
	if (document.getElementById('service_group_id').value!="") {
		loadServiceSubGroup();
		setSelectedIndex(document.getElementById('serviceSubGroupId'), document.getElementById('serviceSubGroup').value);
	 }
	document.equipmentform.equipment_name.focus();
}

function getDepartments(){
	var dept_id = 	document.equipmentform.dept_id;
	loadSelectBox(dept_id,jdepartments,'dept_name','dept_id');
}


function loadSelectBox(selectBox, itemList,dispNameVar, valueVar){
	// clearset the size of the select box
	selectBox.length = itemList.length + 1;
	selectBox.disabled = false;
	index = 1;
	for (var i=0; i<itemList.length; i++) {
		var item = itemList[i];
		selectBox.options[index].text = item[dispNameVar];
		selectBox.options[index].value = item[valueVar];
		index++;
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

function changeRatePlan(){
	document.searchform.submit();
}


function changeRatePlanAddShow() {
	document.showform.org_id.value = document.chargesForm.org_id.value;
	document.showform.submit();
}

function updateOption() {
	for (var i=0; i<updateform.updateTable.length ; i++) {
		if(updateform.updateTable[i].checked){
			return true;
		}
	}
	return false;
}

function validateAllDiscounts() {
	var len = document.chargesForm.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('daily_charge','daily_charge_discount',i);
		valid = valid && validateDiscount('min_charge','min_charge_discount',i);
		valid = valid && validateDiscount('incr_charge','incr_charge_discount',i);
		valid = valid && validateDiscount('slab_1_charge','slab_1_charge_discount',i);
	}
	if(!valid) return false;
	else return true;
}

var toolBar = {
		 Edit: {
			title : 'View/Edit',
			imageSrc : 'icons/Edit.png',
			href : 'master/equipment/show.htm?',
			onclick : null,
			description : 'View and/or Edit the contents of this Equipment'
		 },
  EditCharges: {
			title : 'Edit Charges',
			imageSrc : 'icons/Edit.png',
			href : 'master/equipment/editcharge.htm?',
			onclick : null,
			description : 'View and/or Edit the charges of this Equipment'
		 }
	};

function init() {
	createToolbar(toolBar);
}

function doClose() {
	window.location.href = cpath + '/master/equipment.htm?status=A&org_id=ORG0001&sortOrder=equipment_name&sortReverse=false';
}

function checkDuplicates() {
var name = document.getElementById('equipment_name').value;
var id = document.equipmentform.equip_id.value;
	for (var i = 0; i<namesList.length; i++) {
		var dbName = namesList[i].equipment_name;
		var dbId = namesList[i].eq_id;
		if (id != dbId) {
			if (name == dbName) {
				alert("Equipment name already exists, Please enter another name.");
				document.getElementById('equipment_name').focus();
				return false;
			}
		}
	}
	return true;
}

function loadServiceSubGroup() {
	var serviceGroupId = document.getElementById('service_group_id').value;
	var index = 1;
	document.getElementById("serviceSubGroupId").length = 1;
	for (var i=0; i<serviceSubGroupsList.length; i++) {
		var item = serviceSubGroupsList[i];
	 	if (serviceGroupId == item["service_group_id"]) {
	 		document.getElementById("serviceSubGroupId").length = document.getElementById("serviceSubGroupId").length+1;
	 		document.getElementById("serviceSubGroupId").options[index].text = item["service_sub_group_name"];
	  		document.getElementById("serviceSubGroupId").options[index].value = item["service_sub_group_id"];
	 		index++;
	 	}
	}
}
function getOrderCode(){
	var group = document.getElementById("service_group_id").value;
	var subGroup = document.getElementById("serviceSubGroupId").value;
	var deptId = document.equipmentform.dept_id.value;
	ajaxForOrderCode('Equipment',deptId,group,subGroup,document.equipmentform.equipment_code);
}

function submitCharges() {
	var form = document.chargesForm;

	var taxCharge = form.tax;
	if(taxCharge.length == undefined){
		 if(parseFloat(taxCharge.value) >100){
			alert("Tax(%) can not be more than 100");
			taxCharge.focus();
			return false;
		 }
	}else{
		for(var i=0; i<taxCharge.length;i++){
			if(parseFloat(taxCharge[i].value) > 100){
				alert("one of the Tax(%) field is more than 100");
				taxCharge[i].focus();
				return false;
			}
		}
	}

	if(!validateAllDiscounts()) return false;

	form.submit();
}