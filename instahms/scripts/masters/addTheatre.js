var toolbar = {
Edit: {
		title: "Edit Theatre/Room",
		imageSrc: "icons/Edit.png",
		href:"pages/masters/insta/operationtheatre/TheatMast.do?_method=geteditChargeScreen",
		onclik: null,
		description: "Edit Theatre/Room",
	  },
EditCharges: {
		title: "Edit Charges",
		imageSrc: "icons/Edit.png",
		href:"pages/masters/insta/operationtheatre/TheatMast.do?_method=showCharges",
		onclik: null,
		description: "Edit Theatre/Room charges ",
	  }
};

function init(){
selectBedTypes();
createToolbar(toolbar);
}

function validate(){
	var form = document.TheaMasterForm;

	form.theatreName.value = trim(form.theatreName.value);

	if(form.theatreName.value == ""){
		alert("Theatre/Room Name is  required");
		form.theatreName.focus();
		return false;
	}
	if ( multiCenters && form.centerId.value == "" ) {
		alert("Please select any Center");
		form.centerId.focus();
		return false;
	}
	if(empty(form.storeId.value)){
		alert("Store Name is  required");
		form.storeId.focus();
		return false;
	}
	if(form.unitSize.value == 0){
		alert("Unit Size cannot be set to 0");
		form.unitSize.focus();
		return false;
	}
	var overbook = form.overbook_limit.value;
	if(overbook.length > 10){
		alert("Please enter only 10 digits number for overbook Limit");
		form.overbook_limit.focus();
		return false;
	}

	var isInsuranceCatIdSelected = false;
	var insuranceCatId = form.insurance_category_id;
	for (var i=0; i<insuranceCatId.options.length; i++) {
	  if (insuranceCatId.options[i].selected) {
		  isInsuranceCatIdSelected = true;
	  }
	}
	if (!isInsuranceCatIdSelected) {
		alert("Please select at least one insurance category");
		return false;
	}

	form.submit();

}

function loadCenters(obj) {
	var list = null;
	var storeObj = document.getElementById('storeId');
	if(!empty(obj)) {
		list = filterList(storesJson,"center_id",obj.value);
		if(!empty(list)) {
			storeObj.length = 1;
			for(var i=0;i<list.length;i++) {
				storeObj.options[i+1] =  new Option(list[i]['dept_name'],list[i]['dept_id']);
			}
		}
	}
}

function changeCheckboxValues() {
	var schedule = document.TheaMasterForm.schedule;
	if (schedule.checked)
		document.TheaMasterForm.overbook_limit.disabled = false;
		else
			document.TheaMasterForm.overbook_limit.disabled = true;
			document.TheaMasterForm.overbook_limit.value=0;
}

function enableDisableCheckbox() {
	var schedule = document.TheaMasterForm.schedule;
	if (schedule.checked) {
		document.TheaMasterForm.overbook_limit.disabled = false;
	} else {
		document.TheaMasterForm.overbook_limit.disabled = true;
	}
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

function searchValidate(){
	var form = document.TheaMasterForm;
	form.pageNum.value = 1;

	return true;
}

function populateOrgId(){
	 for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
		if(trim(item["ORG_NAME"])==trim(document.TheaMasterForm.orgName.value)){
			document.TheaMasterForm.orgId.value = item["ORG_ID"];
		}
	  }
}

function checkPageNum( pageno ){
	if(searchValidate()){
		document.TheaMasterForm.pageNum.value = pageno;
		document.TheaMasterForm.submit();
	}
}

function ValidateGropUpdate(){

	var form = document.TheaMasterForm;

	var checked = false;
	var length = form.groupTheatres.length;
	if(length == undefined){
		if(form.groupTheatres.checked ){
			checked = true;
		}
	}else{
		for(var i=0;i<length;i++){
			if(form.groupTheatres[i].checked){
				checked = true;
				break;
			}
		}
	}

	if(!checked){
		alert('at least one Theatre/Room has to checked for updation');
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

	if (!updateOption()) {
		alert("Select any update option");
		form.updateTable[0].focus();
		return ;
	}

	if(form.amount.value==""){
		alert("Rate Variance value is required ");
		form.amount.focus();
		return ;
	}

	if(form.amtType.value == '%') {
		if(getAmount(form.amount.value) > 100){
			alert("Discount percent cannot be more than 100");
			form.amount.focus();
			return false;
		}
	}

	form._method.value = "groupUpdate";
	form.submit();

}

function updateOption() {
	for (var i=0; i<document.TheaMasterForm.updateTable.length ; i++) {
		if(document.TheaMasterForm.updateTable[i].checked){
			return true;
		}
	}
	return false;
}


function selectAll(obj){
	var length = document.TheaMasterForm.groupTheatres.length;
	if (obj.value == 'all'){
		if(length == undefined){
			document.TheaMasterForm.groupTheatres.checked = obj.checked;
		}else{
			for(var i=0;i<length;i++){
				document.TheaMasterForm.groupTheatres[i].checked =obj.checked;
			}
		}
	}else {
		if(length != undefined){
			for(var i=0;i<length;i++){
				document.TheaMasterForm.groupTheatres[i].checked =false;
			}
		}

	}

}


function checkDuplicate(){
	var form = document.TheaMasterForm;
    form.theatreName.value = trim(form.theatreName.value);
    var theatreName = form.theatreName.value;
	var contextPath = form.contextPath.value;
	if(form._method.value == 'addNewTheatre'){
		var ajaxobj = newXMLHttpRequest();
		var url = contextPath+"/pages/masters/insta/operationtheatre/TheatMast.do?_method=checkDuplicate&newTheatre="+encodeURIComponent(theatreName);

		getResponseHandlerText(ajaxobj,getresponseForNewTheatre,url.toString());
	}else {
		var theatrename = form.theatreName.value;
		if (originalOperationTheatreName != theatrename){
			var newTheatreName = form.theatreName.value;
			var ajaxobj = newXMLHttpRequest();
			var url = contextPath+"/pages/masters/insta/operationtheatre/TheatMast.do?_method=checkDuplicate&newTheatre="+encodeURIComponent(newTheatreName);
			getResponseHandlerText(ajaxobj,getresponseForNewTheatre,url.toString());

		}

	}
}


function getresponseForNewTheatre(responseText){
	var form = document.TheaMasterForm;
	if(responseText == 'true'){
		alert("Theatre Name already exists");
		form.theatreName.value = '';
		form.theatreName.focus();
	}
}

var originalOperationTheatreName = null;
function keepbackupValue(){
	var form = document.TheaMasterForm;
	if (form.method.value != 'addNewTheater'){
		originalOperationTheatreName = form.theatreName.value;
	}
	enableDisableCheckbox();
}
function changeRatePlan(){
	var tid=document.TheaMasterForm.theatreID.value;
	var ctyp=document.TheaMasterForm.chargeType.value;
	var orgid=document.TheaMasterForm.ratePlan.value;
	var orgname=document.TheaMasterForm.ratePlan.options[document.TheaMasterForm.ratePlan.selectedIndex].text;
	var pno=document.TheaMasterForm.pageNo.value;
	var cp=document.TheaMasterForm.contextPath.value;
	var ul=cp+"/pages/masters/insta/operationtheatre/TheatMast.do?_method=showCharges&theatreId="+tid+"&orgId="+orgid+"&orgName="+orgname+"&chargeType="+ctyp+"&pageNum="+pno;
	document.TheaMasterForm.action=ul;
	document.TheaMasterForm.submit();
}
function selectBedTypes(){
	var disable = document.TheaMasterForm.allBedTypes.checked;
	var bedTypesLen = document.TheaMasterForm.groupBeds.length;
	for (i=bedTypesLen-1;i>=0;i--){
		document.TheaMasterForm.groupBeds[i].selected = disable;
	}
}

function deselectbedtypes(){
document.TheaMasterForm.allBedTypes.checked=false;
}

function validateAllDiscounts() {
	var len = document.TheaMasterForm.ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('dailyCharge','dailyChargeDiscount',i);
		valid = valid && validateDiscount('minCharge','minChargeDiscount',i);
		valid = valid && validateDiscount('incrCharge','incrChargeDiscount',i);
		valid = valid && validateDiscount('slab1Charge','slab1ChargeDiscount',i);

	}
	if(!valid) return false;
	else return true;
}

function validateCharges() {
	var form = document.TheaMasterForm;
	if(!validateAllDiscounts()) return false;
	form.submit();
}