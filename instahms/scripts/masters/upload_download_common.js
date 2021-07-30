
function initPage() {
	ajaxDetails = getRatePlanAjax();

	orgNamesJSON = ajaxDetails.orgNameJSON;

	OrganizationList();

	MasterList();
	
	onChangeAction();
	
	onChangeMasterNameAction();
	
	onChangeOrganizationId();
	
	onChangeTemplate();
}

function getDetailsAjax() {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath
			+ "/pages/registration/outPatientRegistration.do?_method=getdetailsAJAX";

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function getRatePlanAjax() {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/pages/registration/outPatientRegistration.do?_method=getRatePlan";

	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function validateImportFile() {
	var masterValue = document.getElementById("master").value;
	var actionValue = document.getElementById("action").value
	var codeSystemCategory = document.getElementById("code_system_category_id");
	var codeSystems = document.getElementById("code_systems_id");
	if (!masterValue) {
		alert("Please select a master");
		return false;
	}
	if (masterValue === "CodeSets" && actionValue === "download" && (codeSystemCategory.value === "" || codeSystems.value === "")) {
		alert("Please select both code system and code system category");
		return false;
	}
	if (document.getElementById("action").value == "upload"
			&& document.getElementById("fileUpload").value == "") {
		alert("Please browse and select a file to upload");
		return false;
	}
	if (document.getElementById("isCharges").value == "Y"
			&& document.getElementById("organization").value == "") {
		alert("Please select a rate sheet");
		return false;
	}
}

function onChangeMasterNameAction() {
	var masterName = document.getElementById("master");
	var action = document.getElementById("action");
	var isChargesCheckbox = document.getElementById("isCharges");
	var codeSystemCategoryLable = document.getElementById("codeSystemCategoryLabel");
	var codeSystemCategory = document.getElementById("codeSystemCategoryId");
	var codeSystemsLable = document.getElementById("codeSystemsLabel");
	var codeSystems = document.getElementById("codeSystemId");
	var isCodeSetMaster = masterName.value === "CodeSets";
	var isActionDownload = action.value === "download";
	var isMasterNameCodeSetsVisible = isCodeSetMaster && isActionDownload ? "visible": "hidden";
	codeSystemCategoryLable.style.visibility = isMasterNameCodeSetsVisible;
	codeSystemCategory.style.visibility = isMasterNameCodeSetsVisible;
	codeSystemsLable.style.visibility = isMasterNameCodeSetsVisible;
	codeSystems.style.visibility = isMasterNameCodeSetsVisible;
	isCodeSetMaster ? isChargesCheckbox.setAttribute("disabled",true) : isChargesCheckbox.removeAttribute("disabled");
}

function onChangeOrganizationId() {
	var isChargesCheckbox = document.getElementById("isCharges");
	var orgLabel = document.getElementById("orgLabel");
	var orgId = document.getElementById("orgId");
	if (isChargesCheckbox.checked) {
		isChargesCheckbox.value = 'Y';
		orgLabel.style.visibility = "visible";
		orgId.style.visibility = "visible";
		MasterList();
	} else {
		isChargesCheckbox.value = "N";
		orgLabel.style.visibility = "hidden";
		orgId.style.visibility = "hidden";
		MasterList();
	}
}

function onChangeTemplate() {
	var isTemplateCheckbox = document.getElementById("template");
	if (isTemplateCheckbox.checked) {
		isTemplateCheckbox.value = 'Y';
	} else {
		isTemplateCheckbox.value = "N";
	}
}

function onChangeAction() {
	var template = document.getElementById("template");
	var templateLabel = document.getElementById("templateLabel");
	var fileUpload = document.getElementById("fileUpload");
	var fileUploadLabel = document.getElementById("fileUploadLabel");
	if (document.getElementById("action").value == "download") {
		template.style.visibility = "visible";
		templateLabel.style.visibility = "visible";
		fileUpload.style.visibility = "hidden";
		fileUploadLabel.style.visibility = "hidden";
	} else {
		template.style.visibility = "hidden";
		templateLabel.style.visibility = "hidden";
		fileUploadLabel.style.visibility = "visible";
		fileUpload.style.visibility = "visible";
	}
}

function OrganizationList() {
	var organizationObj = document.getElementById("organization");

	var len = 0;
	for (var i = 0; i < orgNamesJSON.length; i++) {
		optn = new Option(orgNamesJSON[i].org_name, orgNamesJSON[i].org_id);
		organizationObj.options[len] = optn;
		len++;
	}
	setSelectedIndex(organizationObj, "ORG0001");
}

function MasterList() {
	var organizationObj = document.getElementById("master");
	while (organizationObj.options.length > 0) {
		organizationObj.remove(0);
	}
	if (document.getElementById("isCharges").value == "N") {
		organizationObj.options[0] = new Option("-- Select --", "");
		organizationObj.options[1] = new Option("Bulk Patient Data",
				"BulkPatientData");
		organizationObj.options[2] = new Option("Doctor Details",
				"DoctorDefinitionDetails");
		organizationObj.options[3] = new Option("Insurance Company",
				"InsuranceCompany");
		organizationObj.options[4] = new Option("Insurance Plan",
				"InsurancePlan");
		organizationObj.options[5] = new Option("Insurance Plan Type",
				"InsurancePlanType");
		organizationObj.options[6] = new Option("TPA / Sponsor",
				"TpaMaster");
		organizationObj.options[7] = new Option("Code Sets",
				"CodeSets");
	} else {
		organizationObj.options[0] = new Option("-- Select --", "");
		organizationObj.options[1] = new Option("Doctor Charges",
				"DoctorCharges");
	}
}

function setSelectedIndex(opt, set_value) {
	var index = 0;
	if (opt.options) {
		for (var i = 0; i < opt.options.length; i++) {
			var opt_value = opt.options[i].value;
			if (opt_value == set_value) {
				opt.selectedIndex = i;
				return;
			}
		}
	}
}