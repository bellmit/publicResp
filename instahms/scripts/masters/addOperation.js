function validate(){
	var form = document.OPerMasterForm;
	form.operationName.value = trim(form.operationName.value);
	if(form.operationName.value == ''){
		alert('operation name is required');
		form.operationName.focus();
		return false;
	}
	if(form.deptid.options.selectedIndex == 0){
		alert('department name is required');
		form.deptid.focus();
		return false;
	}
	document.forms[0].submit();
	return true;
}

function populteOrgNames(){

	YAHOO.example.orgNamesArray = [];
	YAHOO.example.orgNamesArray.length =orgNames.length;

	for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
				YAHOO.example.orgNamesArray[i] = item["ORG_NAME"];
	}


	YAHOO.example.ACJSArray = new function(){
				  // Instantiate first JS Array DataSource
				  datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.orgNamesArray);
				  // Instantiate first AutoComplete
				  var autoComp = new YAHOO.widget.AutoComplete('orgName','orgContainer', datasource);
				  autoComp.prehighlightClassName = "yui-ac-prehighlight";
				  autoComp.typeAhead = true;
				  // Enable a drop-shadow under the container element
				  autoComp.useShadow = true;
				  // Disable the browser's built-in autocomplete caching mechanism
				  autoComp.allowBrowserAutocomplete = false;
				  // Require user to type at least 0 characters before triggering a query
				  autoComp.minQueryLength = 0;
				  //commas and/or spaces may delimited queries
				  // Display up to 20 results in the container
				  autoComp.maxResultsDisplayed = 20;
				  // Do not automatically highlight the first result item in the container
				  autoComp.autoHighlight = false;
				  // disable force selection,user can type his/her own complaint(which is not there in master)
				  autoComp.forceSelection = true;
				  autoComp.textboxFocusEvent.subscribe(function(){
					   var sInputValue = YAHOO.util.Dom.get('orgName').value;
					   if(sInputValue.length === 0) {
						         var oSelf = this;
						         setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					  }

			     });

			     //autoComp.dataReturnEvent.subscribe(populateOrgId);
			}

}


function populteOperationNames(){
	selectBedTypes();
	YAHOO.example.operationNamesArray = [];
	YAHOO.example.operationNamesArray.length =operationNames.length;

	for(var i=0;i<operationNames.length;i++){
				var item = operationNames[i]
				YAHOO.example.operationNamesArray[i] = item["OPERATION_NAME"];
	}


	YAHOO.example.ACJSArray = new function(){
				  // Instantiate first JS Array DataSource
				  datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.operationNamesArray);
				  // Instantiate first AutoComplete
				  var autoComp = new YAHOO.widget.AutoComplete('operationFilter','operationContainer', datasource);
				  autoComp.prehighlightClassName = "yui-ac-prehighlight";
				  autoComp.typeAhead = true;
				  // Enable a drop-shadow under the container element
				  autoComp.useShadow = true;
				  // Disable the browser's built-in autocomplete caching mechanism
				  autoComp.allowBrowserAutocomplete = false;
				  // Require user to type at least 0 characters before triggering a query
				  autoComp.minQueryLength = 1;
				  //commas and/or spaces may delimited queries
				  // Display up to 20 results in the container
				  autoComp.maxResultsDisplayed = 20;
				  // Do not automatically highlight the first result item in the container
				  autoComp.autoHighlight = false;
				  // disable force selection,user can type his/her own complaint(which is not there in master)
				  autoComp.forceSelection = true;
				  autoComp.textboxFocusEvent.subscribe(function(){
					   var sInputValue = YAHOO.util.Dom.get('operationFilter').value;
					   if(sInputValue.length === 0) {
						         var oSelf = this;
						         setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					  }

			     });

			     //autoComp.dataReturnEvent.subscribe(populateOrgId);
			}

}






function searchValidate(){

	var form = document.OPerMasterForm;
	form.pageNum.value = 1;

	return true;
}


function populateOrgId(){
	 for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
		if(trim(item["ORG_NAME"])==trim(document.OPerMasterForm.orgName.value)){
			document.OPerMasterForm.orgId.value = item["ORG_ID"];
		}
	  }
}


function ValidateGropUpdate(){

	var form = document.OPerMasterForm;


	var checked = false;
	var length = form.groupOperations.length;
	if(length == undefined){
		if(form.groupOperations.checked ){
			checked = true;
		}
	}else{
		for(var i=0;i<length;i++){
			if(form.groupOperations[i].checked){
				checked = true;
				break;
			}
		}
	}


	if(form.updateallOperations.checked){
		checked = true;
	}


	if(!checked){
		alert('at least one operation has to checked for updation');
		return;
	}

	var selceted = false;
	var bedTypeLength = form.groupBeds.length;
	for(var i=0; i<bedTypeLength ; i++){
		if(form.groupBeds.options[i].selected){
				selceted = true;
				break;
		}
	}

	if(!selceted){
		alert('bed Types are required');
		return ;
	}

	if(form.varianceBy.value=="" && form.varianceValue.value ==""){
		alert("Rate Variance value is required ");
		form.varianceBy.focus();
		return ;
	}

	if (form.varianceBy.value>100){
		alert("The percentage should not be more than 100");
		form.varianceBy.focus();
		return ;
	}


	form.method.value = "groupUpdate";
	form.submit();


}

function selectAll(){

	var Allobj = document.forms[0].All.checked;
	var length = document.forms[0].groupOperations.length;

	if(length == undefined){
		document.forms[0].groupOperations.checked = Allobj;

	}else{
		for(var i=0;i<length;i++){
			document.forms[0].groupOperations[i].checked =Allobj
		}
	}


}

function checkPageNum( pageno ){
	if(searchValidate()){
		document.OPerMasterForm.pageNum.value = pageno;
		document.OPerMasterForm.submit();
	}
}


function checkDuplicate(){

	var form = document.OPerMasterForm;
	form.operationName.value = trim(form.operationName.value);

	var contextPath = form.contextPath.value;
	if(form.method.value == 'addNewOperation' ){
		var newOperation = form.operationName.value;
		var ajaxobj = newXMLHttpRequest();
		var url = contextPath+"/pages/masters/insta/operationtheatre/opmast.do?method=checkDuplicate&newOperation="+encodeURIComponent(newOperation);
		getResponseHandlerText(ajaxobj,getresponseForNewOperation,url.toString());
	}else{
		var operationName = form.operationName.value;
		if(orginalOperationName != operationName ){
			var newOperation = form.operationName.value;
			var ajaxobj = newXMLHttpRequest();
			var url = contextPath+"/pages/masters/insta/operationtheatre/opmast.do?method=checkDuplicate&newOperation="+encodeURIComponent(newOperation);
			getResponseHandlerText(ajaxobj,getresponseForNewOperation,url.toString());
		}
	}

}

var orginalOperationName = null;
function keepbackupVarible(){
	var form = document.OPerMasterForm;
	if(form.method.value != 'addNewOperation'){
		//while updating operation defination
		orginalOperationName = form.operationName.value;
	}
}

function getresponseForNewOperation(responseText){
	var form = document.OPerMasterForm;
	eval("var duplicate="+responseText);
	if(duplicate){
		alert("operation is already exists");
		//form.operationName.value = "";
		form.operationName.focus();
	}
}

function selectAllOperations(){

	var form = document.OPerMasterForm;
	var checkStatus =   form.updateallOperations.checked

 	//disable or enable per page checkboxes
	form.All.disabled = checkStatus;
	form.All.checked = false;
	var length = form.groupOperations.length;

	if(length == undefined){
	 		form.groupOperations.checked  = false;
			form.groupOperations.disabled = checkStatus;
	}else{
		for(var i=0;i<length;i++){
			form.groupOperations[i].checked = false;
			form.groupOperations[i].disabled =checkStatus
		}
	}

}

function changeRate(){
	document.forms[0].submit();
}

function selectBedTypes(){
	var disable = document.forms[0].allBedTypes.checked;
	var bedTypesLen = document.forms[0].groupBeds.length;
		//alert(document.forms[0].groupBeds.length);
	for (i=bedTypesLen-1;i>=0;i--){
		document.forms[0].groupBeds[i].selected = disable;
	}
}



function deselectAllBedTypes(){
		document.forms[0].allBedTypes.checked = false;
}

function chgRate(){
	var oid=document.forms[0].ratePlan.value;
	var orgname=document.forms[0].ratePlan.options[document.forms[0].ratePlan.selectedIndex].text;
	var opid=document.forms[0].operationId.value;
	var cp=document.forms[0].contextPath.value;
	var ct=document.forms[0].chargeType.value;
	var ul= cp+"/pages/masters/insta/operationtheatre/opmast.do?method=geteditChargeScreen&OperationId="+opid+"&orgId="+oid+"&orgName="+orgname+"&chargeType="+ct+"&pageNum=";
	//alert(ul);
	document.forms[0].action=ul;
	document.forms[0].submit();
}
