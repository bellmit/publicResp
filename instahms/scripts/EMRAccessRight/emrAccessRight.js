function init(){
var rType = document.getElementById('rule_type').value.trim();
	if(rType=="DOC"){
		if(document.getElementById('selUserOnly').checked) {
			document.getElementById("centUsrDisplay").style.display="block";
			document.getElementById("deptUsrDisplay").style.display="block";
			document.getElementById("roleUsrDisplay").style.display="block";
		}
		if((document.getElementById('selUserOnly').checked) && (document.getElementById('allCenters').checked)){
			document.getElementById("centerboxDisplay").style.display="none";
		}else if((document.getElementById('selUserOnly').checked) && (document.getElementById('selCentersOnly').checked)) {
			document.getElementById("centerboxDisplay").style.display="block";
		}
		if((document.getElementById('selUserOnly').checked) && (document.getElementById('selDepartmentsOnly').checked)) {
			document.getElementById("deptboxDisplay").style.display="block";
		}
		if(document.getElementById('selRoleOnly').checked) {
			document.getElementById("roleboxDisplay").style.display="block";
		}
		if(document.getElementById('selUsersOnly').checked) {
			document.getElementById("userboxDisplay").style.display="block";
		}

	}else{
		if(document.getElementById('selDocOnly').checked) {
			document.getElementById("centUsrDisplay").style.display="block";
			document.getElementById("deptUsrDisplay").style.display="block";
			//document.getElementById("roleUsrDisplay").style.display="block";
			document.getElementById("docTypeDisplay").style.display="block";
		}
		if((document.getElementById('selDocOnly').checked) && (document.getElementById('allCenters').checked)){
			document.getElementById("centerboxDisplay").style.display="none";
		}else if((document.getElementById('selDocOnly').checked) && (document.getElementById('selCentersOnly').checked)) {
			document.getElementById("centerboxDisplay").style.display="block";
		}
		if((document.getElementById('selDocOnly').checked) && (document.getElementById('selDepartmentsOnly').checked)) {
			document.getElementById("deptboxDisplay").style.display="block";
		}
		/*if(document.getElementById('selRoleOnly').checked) {
			document.getElementById("roleboxDisplay").style.display="block";
		}*/
		/*if(document.getElementById('selDocsOnly').checked) {
			document.getElementById("docboxDisplay").style.display="block";
		}*/
		if((document.getElementById('selDocOnly').checked) && document.getElementById('selDocsOnly').checked){
			document.getElementById("selDocsOnly").checked = true;
			document.getElementById("docboxDisplay").style.display="block";
		}else{
			document.getElementById("selDocsOnly").checked = false;
			document.getElementById("docboxDisplay").style.display="none";
		}
	}
	//document.forms[0].doc_type_name.focus();
	var rule_id = document.getElementById('rule_id').value;
	for (var i = 0; i<consultationDocRulesMap.length; i++) {
		if (consultationDocRulesMap[i].rule_id == rule_id) {
			//alert("found rule_id :" + rule_id);
			populatePageOptions(consultationDocRulesMap[i],consultationRuleDetailsMap);
		}
	}

}
function validate(){
		var mName = document.getElementById('_method').value.trim();
		var rType = document.getElementById('rule_type').value.trim();
		if(rType == "DOC"){
		var dSubType;
		var dType = document.getElementById('doc_type_id').value.trim();
			if(dType == "SYS_CONSULT"){
				if(mName == "create"){
					dSubType = document.getElementById('docSubType').value.trim();
				}else{
					dSubType = document.getElementById('doc_sub_type').value.trim();
				}
					if(dSubType == "0" || dSubType == ""){
						alert("Please select the Document Subtype");
						return false;
					}
			}
		}

			if((rType == "DOC") && (document.getElementById('selUserOnly').checked)){
				if(document.getElementById('allCenters').checked){
					document.getElementById("centerboxDisplay").style.display="none";
				}else if(document.getElementById('selCentersOnly').checked) {
						//document.getElementById("centerboxDisplay").style.display="block";
						var centerId = document.getElementById('center_id').value.trim();
						if(centerId == ""){
							alert("Please select the center from the list");
							return false;
						}
				}
				if(document.getElementById('selDepartmentsOnly').checked) {
						var departmentId = document.getElementById('dept_id').value.trim();
						if(departmentId == ""){
							alert("Please select the department from the list");
							return false;
						}
				}
				if(document.getElementById('selRoleOnly').checked) {
						var roleId = document.getElementById('role_id').value.trim();
						if(roleId == ""){
							alert("Please select the role from the list");
							return false;
						}
				}
				if(document.getElementById('selUsersOnly').checked) {
					var userId = document.getElementById('emp_username').value.trim();
					if(userId == ""){
						alert("Please select the user from the list");
						return false;
					}
				}
			}
			if((rType == "ROLE") && (document.getElementById('selDocOnly').checked)){
				if(document.getElementById('allCenters').checked){
					document.getElementById("centerboxDisplay").style.display="none";
				}else if(document.getElementById('selCentersOnly').checked) {
						//document.getElementById("centerboxDisplay").style.display="block";
						var centerId = document.getElementById('center_id').value.trim();
						if(centerId == ""){
							alert("Please select the center from the list");
							return false;
						}
				}
				if(document.getElementById('selDepartmentsOnly').checked) {
						var departmentId = document.getElementById('dept_id').value.trim();
						if(departmentId == ""){
							alert("Please select the department from the list");
							return false;
						}
				}
				/*if(document.getElementById('selRoleOnly').checked) {
						var roleId = document.getElementById('role_id').value.trim();
						if(roleId == ""){
							alert("Please select the role from the list");
							return false;
						}
				}*/
				if(document.getElementById('selDocsOnly').checked) {
					var docId = document.getElementById('doc_type_id').value.trim();
					if(docId == ""){
						alert("Please select the documents from the list");
						return false;
					}
				}
			}
		//return true;
}
function displayUserAndDocDetails(radioId){
	var rType = document.getElementById('rule_type').value.trim();

	if(radioId.id=="selUserOnly" || radioId.id=="selDocOnly"){
		document.getElementById("centUsrDisplay").style.display="block";
		document.getElementById("deptUsrDisplay").style.display="block";
		if(rType == "DOC"){
		document.getElementById("roleUsrDisplay").style.display="block";
		}
		if(rType == "ROLE"){
			document.getElementById("docTypeDisplay").style.display="block";
		}
	}else{
		document.getElementById("centUsrDisplay").style.display="none";
		document.getElementById("deptUsrDisplay").style.display="none";
		if(rType == "DOC"){
		document.getElementById("roleUsrDisplay").style.display="none";
		}
		if(rType == "ROLE"){
			document.getElementById("docTypeDisplay").style.display="none";
		}
	}
}
function displayCenterDetails(radioId){

			if(radioId.id=="selCentersOnly"){
			document.getElementById("centerboxDisplay").style.display="block";
			}else{
			document.getElementById("centerboxDisplay").style.display="none";
			}
}
function displayDepartmentDetails(radioId){

			if(radioId.id=="selDepartmentsOnly"){
			document.getElementById("deptboxDisplay").style.display="block";
			}else{
			document.getElementById("deptboxDisplay").style.display="none";
			}
}
function displayRoleDetails(radioId){
var rType = document.getElementById('rule_type').value.trim();

	if(rType=="DOC"){
		if(radioId.id=="selRoleOnly"){
				document.getElementById("roleboxDisplay").style.display="block";
				}else{
				document.getElementById("roleboxDisplay").style.display="none";
		}
		if(radioId.id=="selUsersOnly"){
			document.getElementById("userboxDisplay").style.display="block";
		}else{
			document.getElementById("userboxDisplay").style.display="none";
		}
	}else{
		if(document.getElementById("selDocsOnly").checked == true){
			document.getElementById("docboxDisplay").style.display="block";
		}else{
			document.getElementById("docboxDisplay").style.display="none";
		}
		/*if(radioId.id=="selDocsOnly"){
			document.getElementById("docboxDisplay").style.display="block";
		}else{
			document.getElementById("docboxDisplay").style.display="none";
		}*/
	}
}

function clearSelected(fieldId){
    var elements = document.getElementById(fieldId).options;
    for(var i = 0; i < elements.length; i++){
      elements[i].selected = false;
    }
}

function getDocRuleDetails(obj) {
	var prevDocSubType = document.getElementById('doc_subtype').value;
	var subtype = '';
	if (!empty(obj)) {
		subtype = obj.value;
	}
	var ajaxobj = newXMLHttpRequest();
	var ruleid = '';
	var url = cpath + "/master/EMRAccessRight.do?_method=getRuleDetailsBasedonDocSubType&rule_type=DOC&doc_type_id=SYS_CONSULT&doc_sub_type=" + subtype;
	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			ruleid= (ajaxobj.responseText);
			if(ruleid != null && ruleid!=''){
			window.location = cpath + "/master/EMRAccessRight.do?_method=show&rule_type=DOC&doc_type_id=SYS_CONSULT&rule_id="+ruleid;
			}else{
			//alert("No Rule is created for this Document Subtype");
			//window.location = cpath + "/master/EMRAccessRight.do?_method=add&rule_type=DOC&doc_type_id=SYS_CONSULT";
			var createRule = confirm("No Rule is created for this Document Subtype. Are you sure you want to Create rule ?")
			if(createRule){
				goforNewConsultantRule();
			}else{
				document.getElementById('doc_sub_type').value = prevDocSubType;
				init();
			}

			}
		}
	}
	return true;
}

function populatePageOptions(consultationRule,consultationRuleDetails) {
	var usersVal = '';
	var rolesVal = '';
	var centersVal = '';
	var departmentsVal = '';
	var documentTypeVal = '';
	var userIds = new Array();
	var roleIds = new Array();
	var centerIds = new Array();
	var departmentIds = new Array();
	var documentTypeIds = new Array();

	if(!empty(consultationRuleDetailsMap)){
	for (var key in consultationRuleDetailsMap) {
		if (key == 'users') {
		usersVal = consultationRuleDetailsMap[key];
			for (var i = 0; i<usersVal.length; i++) {
				userIds[i] = usersVal[i].entity_id;
				//document.getElementById('emp_username').value = usersVal[i].entity_id;
			}
		}else if(key == 'roles'){
		rolesVal = consultationRuleDetailsMap[key];
			for (var i = 0; i<rolesVal.length; i++) {
				roleIds[i] = rolesVal[i].entity_id;
			}
		}else if(key == 'centers'){
		centersVal = consultationRuleDetailsMap[key];
			for (var i = 0; i<centersVal.length; i++) {
				centerIds[i] = centersVal[i].entity_id;
			}
		}else if(key == 'departments'){
		departmentsVal = consultationRuleDetailsMap[key];
			for (var i = 0; i<departmentsVal.length; i++) {
				departmentIds[i] = departmentsVal[i].entity_id;
			}
		}else if(key == 'documentType'){
		documentTypeVal = consultationRuleDetailsMap[key];
			for (var i = 0; i<documentTypeVal.length; i++) {
				documentTypeIds[i] = documentTypeVal[i].entity_id;
			}
		}
	}
	}
	if("DOC"==consultationRule.rule_type){
	if("3"==consultationRule.user_access){
	document.getElementById('allUsers').checked=true;
	document.getElementById('authorOnly').checked=false;
	document.getElementById('selUserOnly').checked=false;
	document.getElementById("centUsrDisplay").style.display="none";
	document.getElementById("deptUsrDisplay").style.display="none";
	document.getElementById("roleUsrDisplay").style.display="none";
	}
	if("2"==consultationRule.user_access){
	document.getElementById('allUsers').checked=false;
	document.getElementById('authorOnly').checked=true;
	document.getElementById('selUserOnly').checked=false;
	document.getElementById("centUsrDisplay").style.display="none";
	document.getElementById("deptUsrDisplay").style.display="none";
	document.getElementById("roleUsrDisplay").style.display="none";
	}
	if("1"==consultationRule.user_access || "0"==consultationRule.user_access){
	document.getElementById('allUsers').checked=false;
	document.getElementById('authorOnly').checked=false;
	document.getElementById('selUserOnly').checked=true;
	document.getElementById("centUsrDisplay").style.display="block";
	document.getElementById("deptUsrDisplay").style.display="block";
	document.getElementById("roleUsrDisplay").style.display="block";
	}
	if("3"==consultationRule.center_access || "0"==consultationRule.center_access){
	document.getElementById('allCenters').checked=true;
	document.getElementById('sameCenterOnly').checked=false;
	document.getElementById('selCentersOnly').checked=false;
	document.getElementById("centerboxDisplay").style.display="none";
	}
	if("2"==consultationRule.center_access){
	document.getElementById('allCenters').checked=false;
	document.getElementById('sameCenterOnly').checked=true;
	document.getElementById('selCentersOnly').checked=false;
	document.getElementById("centerboxDisplay").style.display="none";
	}
	if("1"==consultationRule.center_access){
	document.getElementById('allCenters').checked=false;
	document.getElementById('sameCenterOnly').checked=false;
	document.getElementById('selCentersOnly').checked=true;
	document.getElementById("centerboxDisplay").style.display="block";
	clearMultiSelectSelections(center_id);
	}
	if("3"==consultationRule.dept_access || "0"==consultationRule.dept_access){
	document.getElementById('allDepartments').checked=true;
	document.getElementById('sameDepartmentOnly').checked=false;
	document.getElementById('selDepartmentsOnly').checked=false;
	document.getElementById("deptboxDisplay").style.display="none";
	}
	if("2"==consultationRule.dept_access){
	document.getElementById('allDepartments').checked=false;
	document.getElementById('sameDepartmentOnly').checked=true;
	document.getElementById('selDepartmentsOnly').checked=false;
	document.getElementById("deptboxDisplay").style.display="none";
	}
	if("1"==consultationRule.dept_access){
	document.getElementById('allDepartments').checked=false;
	document.getElementById('sameDepartmentOnly').checked=false;
	document.getElementById('selDepartmentsOnly').checked=true;
	document.getElementById("deptboxDisplay").style.display="block";
	clearMultiSelectSelections(dept_id);
	}
	if("3"==consultationRule.role_access){
	document.getElementById('allroles').checked=true;
	document.getElementById('sameRoleOnly').checked=false;
	document.getElementById('selRoleOnly').checked=false;
	document.getElementById('selUsersOnly').checked=false;
	document.getElementById("roleboxDisplay").style.display="none";
	}
	if("2"==consultationRule.role_access){
	document.getElementById('allroles').checked=false;
	document.getElementById('sameRoleOnly').checked=true;
	document.getElementById('selRoleOnly').checked=false;
	document.getElementById('selUsersOnly').checked=false;
	document.getElementById("roleboxDisplay").style.display="none";
	}
	if("1"==consultationRule.role_access){
	document.getElementById('allroles').checked=false;
	document.getElementById('sameRoleOnly').checked=false;
	document.getElementById('selRoleOnly').checked=true;
	document.getElementById('selUsersOnly').checked=false;
	document.getElementById("roleboxDisplay").style.display="block";
	document.getElementById("userboxDisplay").style.display="none";
	clearMultiSelectSelections(role_id);
	}
	if("0"==consultationRule.role_access){
	document.getElementById('allroles').checked=false;
	document.getElementById('sameRoleOnly').checked=false;
	document.getElementById('selRoleOnly').checked=false;
	document.getElementById('selUsersOnly').checked=true;
	document.getElementById("roleboxDisplay").style.display="none";
	document.getElementById("userboxDisplay").style.display="block";
	clearMultiSelectSelections(emp_username);
	}

	if(document.getElementById('selUserOnly').checked){
		if((document.getElementById('selUserOnly').checked) && (document.getElementById('selCentersOnly').checked)) {
			document.getElementById("centerboxDisplay").style.display="block";
			clearMultiSelectSelections(center_id);
			//alert("centerboxDisplay::"+centerIds);
			setMultipleSelectedIndexs(center_id, centerIds);
			//for (var i = 0; i<centersVal.length; i++) {
			//document.getElementById('center_id').value = centersVal[i].entity_id;
			//}
		}else{
			document.getElementById("centerboxDisplay").style.display="none";
		}
		if((document.getElementById('selUserOnly').checked) && (document.getElementById('selDepartmentsOnly').checked)) {
			document.getElementById("deptboxDisplay").style.display="block";
			clearMultiSelectSelections(dept_id);
			//alert("deptboxDisplay::"+departmentIds);
			setMultipleSelectedIndexs(dept_id, departmentIds);
		}else{
			document.getElementById("deptboxDisplay").style.display="none";
		}
		if((document.getElementById('selUserOnly').checked) && (document.getElementById('selRoleOnly').checked)) {
			document.getElementById("roleboxDisplay").style.display="block";
			clearMultiSelectSelections(role_id);
			//alert("roleboxDisplay::"+roleIds);
			setMultipleSelectedIndexs(role_id, roleIds);
		}else{
			document.getElementById("roleboxDisplay").style.display="none";
		}
		if((document.getElementById('selUserOnly').checked) && (document.getElementById('selUsersOnly').checked)) {
			document.getElementById("userboxDisplay").style.display="block";
			clearMultiSelectSelections(emp_username);
			//alert("userboxDisplay::"+userIds);
			setMultipleSelectedIndexs(emp_username, userIds);
		}else{
			document.getElementById("userboxDisplay").style.display="none";
		}
	}
	}else{
	if("3"==consultationRule.doc_access){
	document.getElementById('allDocuments').checked=true;
	document.getElementById('ownerOnly').checked=false;
	document.getElementById('selDocOnly').checked=false;
	document.getElementById("centUsrDisplay").style.display="none";
	document.getElementById("deptUsrDisplay").style.display="none";
	document.getElementById("docTypeDisplay").style.display="none";
	}
	if("2"==consultationRule.doc_access){
	document.getElementById('allDocuments').checked=false;
	document.getElementById('ownerOnly').checked=true;
	document.getElementById('selDocOnly').checked=false;
	document.getElementById("centUsrDisplay").style.display="none";
	document.getElementById("deptUsrDisplay").style.display="none";
	document.getElementById("docTypeDisplay").style.display="none";
	}
	if("1"==consultationRule.doc_access || "0"==consultationRule.doc_access){
	document.getElementById('allDocuments').checked=false;
	document.getElementById('ownerOnly').checked=false;
	document.getElementById('selDocOnly').checked=true;
	document.getElementById("centUsrDisplay").style.display="block";
	document.getElementById("deptUsrDisplay").style.display="block";
	document.getElementById("docTypeDisplay").style.display="block";
	}
	if("3"==consultationRule.center_access || "0"==consultationRule.center_access){
	document.getElementById('allCenters').checked=true;
	if(document.getElementById('sameCenterOnly')!=null){
	document.getElementById('sameCenterOnly').checked=false;
	document.getElementById('selCentersOnly').checked=false;
	}
	document.getElementById("centerboxDisplay").style.display="none";
	}
	if("2"==consultationRule.center_access){
	document.getElementById('allCenters').checked=false;
	document.getElementById('sameCenterOnly').checked=true;
	document.getElementById('selCentersOnly').checked=false;
	document.getElementById("centerboxDisplay").style.display="none";
	}
	if("1"==consultationRule.center_access){
	document.getElementById('allCenters').checked=false;
	document.getElementById('sameCenterOnly').checked=false;
	document.getElementById('selCentersOnly').checked=true;
	document.getElementById("centerboxDisplay").style.display="block";
	clearMultiSelectSelections(center_id);
	}
	if("3"==consultationRule.dept_access || "0"==consultationRule.dept_access){
	document.getElementById('allDepartments').checked=true;
	document.getElementById('sameDepartmentOnly').checked=false;
	document.getElementById('selDepartmentsOnly').checked=false;
	document.getElementById("deptboxDisplay").style.display="none";
	}
	if("2"==consultationRule.dept_access){
	document.getElementById('allDepartments').checked=false;
	document.getElementById('sameDepartmentOnly').checked=true;
	document.getElementById('selDepartmentsOnly').checked=false;
	document.getElementById("deptboxDisplay").style.display="none";
	}
	if("1"==consultationRule.dept_access){
	document.getElementById('allDepartments').checked=false;
	document.getElementById('sameDepartmentOnly').checked=false;
	document.getElementById('selDepartmentsOnly').checked=true;
	document.getElementById("deptboxDisplay").style.display="block";
	clearMultiSelectSelections(dept_id);
	}
	if("4"==consultationRule.role_access){
	document.getElementById('selDocsOnly').checked=true;
	document.getElementById("docboxDisplay").style.display="block";
	clearMultiSelectSelections(doc_type_id);
	}
	if("0"==consultationRule.role_access){
	document.getElementById('selDocsOnly').checked=false;
	document.getElementById("docboxDisplay").style.display="none";
	clearMultiSelectSelections(doc_type_id);
	}

		if(document.getElementById('selCentersOnly')!= null && document.getElementById('selDocOnly').checked){
			if((document.getElementById('selDocOnly').checked) && (document.getElementById('selCentersOnly').checked)) {
				document.getElementById("centerboxDisplay").style.display="block";
				clearMultiSelectSelections(center_id);
				//alert("centerboxDisplay::"+centerIds);
				setMultipleSelectedIndexs(center_id, centerIds);
				//for (var i = 0; i<centersVal.length; i++) {
				//document.getElementById('center_id').value = centersVal[i].entity_id;
				//}
			}else{
				document.getElementById("centerboxDisplay").style.display="none";
			}
			if((document.getElementById('selDocOnly').checked) && (document.getElementById('selDepartmentsOnly').checked)) {
				document.getElementById("deptboxDisplay").style.display="block";
				clearMultiSelectSelections(dept_id);
				//alert("deptboxDisplay::"+departmentIds);
				setMultipleSelectedIndexs(dept_id, departmentIds);
			}else{
				document.getElementById("deptboxDisplay").style.display="none";
			}
			if((document.getElementById('selDocOnly').checked) && (document.getElementById('selDocsOnly').checked)) {
				document.getElementById("docboxDisplay").style.display="block";
				clearMultiSelectSelections(doc_type_id);
				//alert("docboxDisplay::"+documentTypeIds);
				setMultipleSelectedIndexs(doc_type_id, documentTypeIds);
			}else{
				document.getElementById("docboxDisplay").style.display="none";
			}
		}
	}
}

function goforNewConsultantRule(){
var rType = document.getElementById('rule_type').value.trim();
	if(rType=="DOC"){
		clearMultiSelectSelections(center_id);
		clearMultiSelectSelections(dept_id);
		clearMultiSelectSelections(role_id);
		clearMultiSelectSelections(emp_username);
		document.getElementById("centUsrDisplay").style.display="none";
		document.getElementById("deptUsrDisplay").style.display="none";
		document.getElementById("roleUsrDisplay").style.display="none";
		document.getElementById('allUsers').checked=true;
	}else{
		clearMultiSelectSelections(center_id);
		clearMultiSelectSelections(dept_id);
		clearMultiSelectSelections(doc_type_id);
		document.getElementById("centUsrDisplay").style.display="none";
		document.getElementById("deptUsrDisplay").style.display="none";
		document.getElementById("docTypeDisplay").style.display="none";
		document.getElementById('allDocuments').checked=true;
	}
}

