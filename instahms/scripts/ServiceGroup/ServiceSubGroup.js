
 function init() {
 	if (document.getElementById('setServiceSubGroupId').value!="") {
 		loadServiceSubGroup();
 		setSelectedIndex(document.forms[0].service_sub_group_id, document.getElementById('setServiceSubGroupId').value);
 	}
 }

 var toolbar = {
	Edit: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/ServiceSubGroup.do?_method=edit',
		onclick: null,
		description: "View and/or Edit Service Sub Group Details"
		}
 };

var editableSubGroup = true;
 function validate() {
 	if ((document.forms[0]._method.value=="update")  && (document.getElementById('service_sub_group_id').value=="")) {
		alert("Pick the Service Sub Group");
		document.getElementById('serviceSub_group_name').focus();
		return false;
	}
 	if (document.getElementById('service_sub_group_name').value=="") {
 		alert("Service Sub Group Name is required");
 		document.getElementById('service_sub_group_name').focus();
 		return false;
 	}
 	if (document.getElementById('service_group_id').selectedIndex==0) {
 		alert("Service Group is required");
 		document.getElementById('service_group_id').focus();
 		return false;
 	}
 	if (document.getElementById('display_order').value=="") {
 		alert("Display Order is required");
 		document.getElementById('display_order').focus();
 		return false;
 	}
 	if (document.getElementById('status').value=="") {
 		alert("Status is required");
 		document.getElementById('status').focus();
 		return false;
 	}

 	if(document.forms[0]._method.value == "update" && !editableSubGroup){
 		alert(document.getElementById('service_sub_group_name').value+" is not editable sub group");
 		return false;
	}

	if (!validateRedemptionCapPercent())
		return false;

 	document.forms[0].submit();
 }

 var serviceSubGroupNameArray = [];
 function serviceSubGroupAutoComplete() {
	if (document.forms[0]._method.value=="update") {
		serviceSubGroupNameArray.length = serviceSubGroupsList.length;
		for (i=0 ; i< serviceSubGroupsList.length; i++) {
			var item = serviceSubGroupsList[i]
			serviceSubGroupNameArray[i] = item["SERVICE_SUB_GROUP_NAME"];
		}

		var datasource = new YAHOO.widget.DS_JSArray(serviceSubGroupNameArray, { queryMatchContains : true } );
		var autoComp = new YAHOO.widget.AutoComplete('serviceSub_group_name','serviceSubGroupContainer',datasource);
		autoComp.formatResult = Insta.autoHighlight;
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.useShadow = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.forceSelection = true;
		autoComp.animVert = false;
		autoComp.itemSelectEvent.subscribe(getServiceSubGroupDetails);
		document.getElementById('service_sub_group_name').focus();
		autoComp.itemSelectEvent.subscribe(function(){
			if(document.getElementById("service_sub_group_name").value == 'Doctor'){
				editableSubGroup = false;
			}else{
				editableSubGroup = true;
			}
		});
	}
 }

 function getServiceSubGroupDetails() {
	var serviceSubGroupName = YAHOO.util.Dom.get('serviceSub_group_name').value;
	for (i=0; i<serviceSubGroupsList.length;i++) {
		var item = serviceSubGroupsList[i];
		if (item["SERVICE_SUB_GROUP_NAME"] == serviceSubGroupName) {
			document.getElementById('serviceSub_group_id').value = item["SERVICE_SUB_GROUP_ID"];
			document.getElementById("service_sub_group_code").value = item["SERVICE_SUB_GROUP_CODE"];
		}
	}
 }

 function searchSubGroup() {
 	if(document.getElementById('serviceSub_group_name').value == '' || document.getElementById('serviceSub_group_id').value == '' ){
 		alert("Please Select Sub Group Name");
 		document.getElementById('serviceSub_group_name').focus();
 		return false;
 	}
	document.forms[0]._method.value = "edit";
	document.forms[0].submit();
}

	function doUpload() {
      if(document.serviceSubGrpUploadForm.xlsServiceSubGroupFile.value == '') {
       	alert('Please browse and select a file to upload');
       	return false;
      }
      return true;
	}

function validateRedemptionCapPercent() {
	var eligibleRedeemObj = document.getElementById("eligible_to_redeem_points");
	var redemCapPerObj = document.getElementById("redemption_cap_percent");

	if (eligibleRedeemObj != null && eligibleRedeemObj.value == "Y") {
		var redemCapPer = (redemCapPerObj != null) ? trim(redemCapPerObj.value) : 0;

		if (redemCapPer == "") {
			alert("Redemption Cap Percent is required");
			redemCapPerObj.focus();
			return false;
		}

		if (!validateDecimal(redemCapPerObj, "Redemption Cap Percent must be a valid number"))
			return false;

		var redeemPer = getAmount(redemCapPerObj.value);
		if (redeemPer > 100) {
			alert("Redemption Cap Percent cannot be greater than 100%");
			redemCapPerObj.focus();
			return false;
		}
	}
	return true;
}
