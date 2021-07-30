function initRules() {

	document.getElementById("activityIdAutoComplete").style.display = 'block';
	var action = document.getElementById("_method").value;

	if (action == 'create') {
		var activityTypeObj = document.getElementById("activity_type");
		var actType = activityTypeObj.value;
		initActivityIdAutoComplete('activity_id_auto_fld', 'activityIdContainer', actType);

	}else {

		var categoryObj = document.getElementById("dyna_pkg_cat_id");
		var chargeGrpObj = document.getElementById("chargegroup_id");
		if (chargeGrpObj.value != '*')
			onChangeChargeGroup();

		var chargeHeadObj	= document.getElementById("chargehead_id");
		setSelectedIndex(chargeHeadObj, beanChargeHead);

		var serviceGrpObj = document.getElementById("service_group_id");
		if (serviceGrpObj.value != '*')
			onChangeServiceGroup();

		var serviceSubGrpObj = document.getElementById("service_sub_group_id");
		setSelectedIndex(serviceSubGrpObj, beanServSubGrp);

		var activityTypeObj = document.getElementById("activity_type");
		if (activityTypeObj.value != '*')
			onChangeActivityType();

		var actType = activityTypeObj.value;
		initActivityIdAutoComplete('activity_id_auto_fld', 'activityIdContainer', actType);

		var activityIdAutoObj = document.getElementById("activity_id_auto_fld");
		var activityIdObj = document.getElementById("activity_id");
		activityIdAutoObj.value = beanActName;
		activityIdObj.value = beanActId;
	}
}

function validateSubmit() {
	var priorityObj 		= document.getElementById("priority");
	var categoryObj 		= document.getElementById("dyna_pkg_cat_id");

	if (!validateInteger(priorityObj, "Priority must be a valid number"))
		return false;

	if (categoryObj.value == '') {
		alert("Select the category");
		categoryObj.focus();
		return false;
	}
	document.dynaPkgRuleForm.submit();
	return true;
}

function onChangeChargeGroup() {
	var chargeGrpObj 		= document.getElementById("chargegroup_id");
	var chargeHeadObj	 	= document.getElementById("chargehead_id");

	var chargeGroupDetails = filterList(chargeGroupHeadList, 'chargegroup_id', chargeGrpObj.value);
	if (!empty(chargeGroupDetails)) {
		loadSelectBox(chargeHeadObj, chargeGroupDetails, 'chargehead_name', 'chargehead_id', '(All)', '*');

	}else if (chargeGrpObj.value == '*') {
		loadSelectBox(chargeHeadObj, chargeGroupHeadList, 'chargehead_name', 'chargehead_id', '(All)', '*');
	}
	removeDupsAndSortDropDown(chargeHeadObj);
}

function onChangeServiceGroup() {
	var serviceGrpObj 	= document.getElementById("service_group_id");
	var serviceSubGrpObj = document.getElementById("service_sub_group_id");

	var serviceGroupDetails = filterList(chargeGroupHeadList, 'service_group_id', serviceGrpObj.value);
	if (!empty(serviceGroupDetails)) {
		loadSelectBox(serviceSubGrpObj, serviceGroupDetails, 'service_sub_group_name', 'service_sub_group_id', '(All)', '*');

	}else if (serviceGrpObj.value == '*') {
		loadSelectBox(serviceSubGrpObj, chargeGroupHeadList, 'service_sub_group_name', 'service_sub_group_id', '(All)', '*');
	}
	removeDupsAndSortDropDown(serviceSubGrpObj);
}

function onChangeActivityType() {
	var activityTypeObj 	= document.getElementById("activity_type");
	var actType = activityTypeObj.value;

	initActivityIdAutoComplete('activity_id_auto_fld', 'activityIdContainer', actType);
}

function isInArray(objArr, val) {
	for (var k=0; k<objArr.length; k++) {
   	if (objArr[k][1].value == val)
   		return true;
   }
	return false;
}

function removeDupsAndSortDropDown(obj) {
	var objArr = new Array();
	if (!empty(obj)) {
		objArr = new Array();
		var objValue = obj.value;
		var i = 0;
    	for (var n=0; n<obj.options.length; n++) {
    		if (!empty(obj.options[n].value)) {
				if (!isInArray(objArr, obj.options[n].value)) {
     				objArr[i] = new Array(obj.options[n].text,
     								{text: obj.options[n].text, value: obj.options[n].value});
     				i++;
     			}
     		}
    	}
    	objArr.sort();

		var len = 0;
		if (objArr.length > 0) {
	    	for (var n=0; n<objArr.length; n++) {
	    		var optn = new Option(objArr[n][1].text, objArr[n][1].value);
				len++;
				obj.options.length = len;
				obj.options[len - 1] = optn;
	    	}
		}
    	setSelectedIndex(obj, objValue);
    }
}

var oAutoComp = null;
function initActivityIdAutoComplete(field, container, actType) {

	if (oAutoComp != null) {
		oAutoComp.destroy();
		document.getElementById(field).value = "(All)";
		document.getElementById("activity_id").value = "*";
	}

	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/master/DynaPackageRulesMaster.do");
	var queryParams = "_method=getActivityList&activityType=" +actType;
	dataSource.scriptQueryAppend = queryParams;
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
	    resultsList: "result",
	    fields: [{ key: "activity_name" }, { key: "activity_id" }]
	};

	oAutoComp = new YAHOO.widget.AutoComplete(field, container, dataSource);
	oAutoComp.minQueryLength = 0;
	oAutoComp.forceSelection = true;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.resultTypeList = false;
	oAutoComp.maxResultsDisplayed = 15;
	oAutoComp.itemSelectEvent.subscribe(setActivityDetails);
}

var setActivityDetails = function (sType, aArgs) {
	var oData = aArgs[2];
	if (!empty(oData))
		document.getElementById("activity_id").value = aArgs[2].activity_id;
	else
		document.getElementById("activity_id").value = "";
}

function reorderPriorityValues(){
	document.dynaruleform._method.value="reorderPriority";
	document.dynaruleform.submit();
	return true;
}

function deleteRules() {
	document.dynaruleform._method.value = "";
	if (!checkRuleSelection()){
		alert("Please check any rule to delete");
		return false;
	}
	var ok = confirm("Do you want to delete selected rule(s) ?");
	if (!ok)
		return false;

	document.dynaruleform._method.value = "deleteRules";
	document.dynaruleform.submit();
	return true;
}

function checkRuleSelection() {
	var ruleDeleteElmts = document.getElementsByName("_ruleDelete");
	for(var i=0;i<ruleDeleteElmts.length;i++) {
		if(ruleDeleteElmts[i].checked) return true;
	}
	return false;
}

function selectOrUnselectAll() {
	var check = document.dynaruleform.ruleDeleteAll.checked;
	var ruleDeleteElmts = document.getElementsByName("_ruleDelete");
	for(var i=0;i<ruleDeleteElmts.length;i++) {
		ruleDeleteElmts[i].checked = check;
	}
}
