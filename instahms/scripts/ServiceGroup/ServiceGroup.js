
 var toolbar = {
	Edit: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/ServiceGroup.do?_method=edit',
		onclick: null,
		description: "View and/or Edit Service Group Details"
		}
 };

var editableGroup = true;
	var serviceGroupNameArray = [];
	function serviceGroupAutoComplete() {
		if (document.forms[0]._method.value=="update") {
			serviceGroupNameArray.length = serviceGroupsList.length;
			for (i=0 ; i< serviceGroupsList.length; i++) {
				var item = serviceGroupsList[i]
				serviceGroupNameArray[i] = item["SERVICE_GROUP_NAME"];
			}

			var datasource = new YAHOO.widget.DS_JSArray(serviceGroupNameArray, { queryMatchContains : true } );
			var autoComp = new YAHOO.widget.AutoComplete('serviceGroup_name','serviceGroupContainer',datasource);
			autoComp.formatResult = Insta.autoHighlight;
			autoComp.prehighlightClassName = "yui-ac-prehighlight";
			autoComp.useShadow = true;
			autoComp.allowBrowserAutocomplete = false;
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.forceSelection = true;
			autoComp.animVert = false;
			autoComp.itemSelectEvent.subscribe(getServiceGroupDetails);
			document.getElementById('service_group_name').focus();
			autoComp.itemSelectEvent.subscribe(function(){
				if(document.getElementById("serviceGroup_name").value == 'Doctor'){
					editableGroup = false;
				}else{
					editableGroup = true;
				}
			});
		}
	}

	function getServiceGroupDetails() {
		var serviceGroupName = YAHOO.util.Dom.get('serviceGroup_name').value;
		for (i=0; i<serviceGroupsList.length;i++) {
			var item = serviceGroupsList[i];
			if (item["SERVICE_GROUP_NAME"] == serviceGroupName) {
				document.getElementById('serviceGroup_id').value = item["SERVICE_GROUP_ID"];
				document.getElementById("service_group_code").value = item["SERVICE_GROUP_CODE"];
				editableGroup = (document.getElementById('serviceGroup_id').value > 0);
			}
		}
	}

	function validate() {
		if ((document.forms[0]._method.value=="update")  && (document.getElementById('service_group_id').value=="")) {
			alert("Pick the service group");
			document.getElementById('serviceGroup_name').focus();
			return false;
		}
		if (document.getElementById('service_group_name').value=="") {
			alert("Service Group Name is required");
			document.getElementById('service_group_name').focus();
			return false;
		}
		if (document.getElementById('display_order').value=="") {
			alert("Display Order is required");
			document.getElementById('display_order').focus();
			return false;
		}
		document.forms[0].submit();
	}

	function searchGroup() {
		if(document.forms[0].serviceGroup_name.value == '' || document.forms[0].serviceGroup_id.value == ''){
			alert("please select Group name");
			document.forms[0].serviceGroup_name.focus();
			return false;
		}
		document.forms[0]._method.value = "edit";
		document.forms[0].submit();
	}

	function doUpload() {
       if(document.serviceGrpUploadForm.xlsServiceGroupFile.value == '') {
	       	alert('Please browse and select a file to upload');
	       	return false;
       }
       return true;
	}