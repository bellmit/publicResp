var toolbar = {
	edit: {
		title: "View/Edit",
		imageSrc: 'icons/Edit.png',
		href: 'pages/masters/insta/admin/newbedmaster.do?method=getEditChargesScreen',
		onclick: null,
		description: null
	}
};


function populateOrgId(){
	//alert(document.DocForm.orgName.value);
	 for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
		if(trim(item["ORG_NAME"])==trim(document.newbedmasterform.orgName.value)){
			document.newbedmasterform.orgId.value = item["ORG_ID"];
		}
	  }
}

function doSearch(){
	var form = document.newbedmasterform;

	return true;
}


function checkPageNum( pageno ){
	document.newbedmasterform.pageNum.value = pageno;
	if(doSearch()){
		document.newbedmasterform.submit();
	}
}




function validate(){
 	var form = document.newbedmasterform;
	if(form.method.value =='addNewBed'){
		form.bedtype.value = trim(form.bedtype.value);


		if(form.bedtype.value ==""){
			alert("New bed name is required");
			form.bedtype.focus();
			return false;
		}

		if(form.baseBeadForCharges.selectedIndex == 0){
			alert("Base bedtype is required to apply chages for new bed");
			form.baseBeadForCharges.focus();
			return false;
		}
	}


	if(form.bedtype.value == 'GENERAL' ){
		if(form.status.value=='I'){
			alert('"GENERAL" BedType cannot be deactivated');
			form.status.focus();
			return false;
		}
	}

	var luxaryCharge = form.luxaryCharge;
	if(luxaryCharge.length == undefined){
		 if(parseFloat(luxaryCharge.value) >100){
			alert("luxury Tax(%) can not be more than 100");
			luxaryCharge.focus();
			return false;
		 }
	}else{
		for(var i=0; i<luxaryCharge.length;i++){
			if(parseFloat(luxaryCharge[i].value) > 100){
				alert("One of the luxury Tax(%) field is more than 100");
				luxaryCharge[i].focus();
				return false;
			}
		}
	}

	if(!validateAllDiscounts()) return false;
}

function selectAll(){
	var Allobj = document.updateform.All.checked;
	var length = document.newbedmasterform.groupbedType.length;

	if(length == undefined ){
		document.newbedmasterform.groupbedType.checked = Allobj;
	}else{
		for(var i=0; i<length;i++ ){
			document.newbedmasterform.groupbedType[i].checked = Allobj;
		}
	}
}



function ValidateGropUpdate(){

	var form = document.newbedmasterform;
	var updateForm = document.updateform;
	if(doSearch()){
		updateForm.method.value='groupUpdate';


		if(updateForm.varianceBy.value=="" && updateForm.varianceValue.value ==""){
			alert("Rate Variance value is required ");
			updateForm.varianceBy.focus();
			return ;
		}

		if(updateForm.variaceType.value == 'Decr'){
			if (updateForm.varianceBy.value>100){
				alert("The percentage should not be more than 100");
				updateForm.varianceBy.focus();
				return ;
			}
		}


		var checked = false;
		var div = document.getElementById("bedTypeListInnerHtml");
		while (div.hasChildNodes())
			div.removeChild(div.firstChild);
		var length = form.groupbedType.length;
		if(length == undefined){
			if(form.groupbedType.checked ){
				checked = true;
				div.appendChild(makeHidden("groupbedType", "", form.groupbedType.value));
			}
		}else{
			for(var i=0;i<length;i++){
				if(form.groupbedType[i].checked){
					checked = true;
					div.appendChild(makeHidden("groupbedType", "", form.groupbedType[i].value));
				}
			}
		}
		if(!checked){
			alert('At least one bedType has to checked for updation');
			return;
		}

		if (!updateOption(updateForm)) {
			alert("Select any update option");
			updateForm.updateTable[0].focus();
			return ;
		}

		if(getAmount(updateForm.varianceBy.value) > 100){
			alert("Discount percent cannot be more than 100");
			updateForm.varianceBy.focus();
			return false;
		}

		updateForm.orgId.value = form.orgId.value;
		updateForm.groupUpdatComponent.value = form.groupUpdatComponent.value;
		updateForm.chargeHead.value = form.chargeHead.value;
		updateForm.submit();
	}

}

function updateOption(form) {
	for (var i=0; i<form.updateTable.length ; i++) {
		if(form.updateTable[i].checked){
			return true;
		}
	}
	return false;
}


function checkDuplicate(){

	var form = document.newbedmasterform;
	form.bedtype.value = trim(form.bedtype.value);

	var contextPath = form.contextPath.value;
	if(form.method.value == 'addNewBed' ){
		var newBedType = form.bedtype.value;
		var isAnd = checkAmpersandOnBlur();
		if(isAnd)
			return false;
		var ajaxobj = newXMLHttpRequest();
		var url = contextPath+"/pages/masters/insta/admin/newbedmaster.do?method=checkDuplicate&newBedType="+encodeURIComponent(newBedType);
		getResponseHandlerText(ajaxobj,getresponseForNewBedType,url.toString());
	}
}

function getresponseForNewBedType(responseText){
	var form = document.newbedmasterform;
	var bedType = (form.bedtype.value);
	eval("var duplicate="+responseText);
	if(duplicate){
		alert("bedType '"+bedType+"' already exists,please enter a different bedtype name");
		form.bedtype.value = "";
		form.bedtype.focus();
	}
}


function changeRatePlan(){

	var bedtype=document.forms[0].bedtype.value;
	var orgid=document.forms[0].ratePlan.value;
	var orgname=document.forms[0].ratePlan.options[document.forms[0].ratePlan.selectedIndex].text;

	var cp=document.forms[0].contextPath.value;
	var ul=cp+"/pages/masters/insta/admin/newbedmaster.do?method=getEditChargesScreen&bedType="+encodeURIComponent(bedtype)+"&orgId="+orgid+"&orgName="+orgname;
	document.forms[0].action=ul;
	document.forms[0].submit();
}
function changeRate(){
	document.forms[0].submit();
}

function selectUpdateOption() {
	createToolbar(toolbar);
	if(document.forms[0].chargeHead.value == 'LUXARY') {
		for (var i=0; i<document.forms[0].updateTable.length ; i++) {
			if(document.forms[0].updateTable[i].value != 'UPDATECHARGE')
			document.forms[0].updateTable[i].disabled = true;
		}
	}
}

function validateAllDiscounts() {
	var len = document.forms[0].ids.value;
	var valid = true;
	for(var i=0;i<len;i++) {
		valid = valid && validateDiscount('bedCharge','bedChargeDiscount',i);
		valid = valid && validateDiscount('nursingCharge','nursingChargeDiscount',i);
		valid = valid && validateDiscount('dutyCharge','dutyChargeDiscount',i);
		valid = valid && validateDiscount('profCharge','profChargeDiscount',i);
		valid = valid && validateDiscount('hourlyCharge','hourlyChargeDiscount',i);
		valid = valid && validateDiscount('daycareSlab1Charge','daycareSlab1ChargeDiscount',i);
		valid = valid && validateDiscount('daycareSlab2Charge','daycareSlab2ChargeDiscount',i);
		valid = valid && validateDiscount('daycareSlab3Charge','daycareSlab3ChargeDiscount',i);
	}
	if(!valid) return false;
	else return true;
}

function doClose() {
	window.location.href = cpath + "/pages/masters/insta/admin/newbedmaster.do?method=getdetails&status=A&sortReverse=false";
}

function checkUsage(obj){
	if ( inUse ) {
		alert ( "One of the active patients uses this as billing bed type.Cannot mark it non billable");
		obj.value = 'Y';
		return false;
	}
	return true;
}

function fillRatePlanDetails(bedType){
		if(derivedRatePlanDetails.length>0) {
			document.getElementById("ratePlanDiv").style.display = 'block' ;
			for (var i =0; i<derivedRatePlanDetails.length; i++) {
				var ratePlanTbl = document.getElementById("ratePlanTbl");
				var len = ratePlanTbl.rows.length;
				var templateRow = ratePlanTbl.rows[len-1];
			   	var row = '';
			   		row = templateRow.cloneNode(true);
			   		row.style.display = '';
			   		row.id = len-2;
			   		len = row.id;
			   	YAHOO.util.Dom.insertBefore(row, templateRow);

				var cell1 = row.insertCell(-1);
			    cell1.setAttribute("style", "width: 70px");
			    if(derivedRatePlanDetails[i].is_override=='Y')
		    		cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/blue_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
		    	else
		    		cell1.innerHTML = '<span class="label"><img src="'+cpath+'/images/empty_flag.gif"/>&nbsp;'+derivedRatePlanDetails[i].org_name;
			    var inp2 = document.createElement("INPUT");
			    inp2.setAttribute("type", "hidden");
			    inp2.setAttribute("name", "ratePlanId");
			    inp2.setAttribute("id", "ratePlanId"+len);
			    inp2.setAttribute("value", derivedRatePlanDetails[i].org_id);
			    cell1.appendChild(inp2);

				var cell2 = row.insertCell(-1);
			    cell2.setAttribute("style", "width: 70px");
			    cell2.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].discormarkup;

			    var cell3 = row.insertCell(-1);
			    cell3.setAttribute("style", "width: 40px");
			    cell3.innerHTML = "<span class='label'>"+derivedRatePlanDetails[i].rate_variation_percent;

				var orgId = derivedRatePlanDetails[i].org_id;
				var cell4 = row.insertCell(-1);
				var orgName = derivedRatePlanDetails[i].org_name;
				var baserateSheet = derivedRatePlanDetails[i].base_rate_sheet_id

				var url = cpath + '/pages/masters/insta/admin/newbedmaster.do?method=getChargesOverrideScreen&bedType='+encodeURIComponent(bedType)+
						'&orgId='+orgId+'&fromItemMaster=true&org_name='+orgName+'&baseRateSheet='+baserateSheet;
					cell4.innerHTML = '<a href="'+ url +'" title="Edit Charge" target="_blank">Edit Charge</a>';
			}
		}
	}

	function doUpload(formType) {
		if (formType == "uploadbedTypeform") {
			var form = document.uploadbedTypeform;
			if (form.bedTypeDetailsFile.value == '') {
				alert("Please browse and select a file to upload");
				return false;
			}
		} else {
			document.importChargesform.orgId.value = document.newbedmasterform.orgId.value;
			var form = document.importChargesform;
			if (form.uploadBedChargesFile.value == '') {
				alert("Please browse and select a file to upload");
				return false;
			}
		}
		form.submit();
	}

	function doExport() {
		document.exportform.orgId.value = document.newbedmasterform.orgId.value;
		return true;
	}

	function doExportICUCharges() {
		document.exportICUform.orgId.value = document.newbedmasterform.orgId.value;
		return true;
	}

	function doUploadICUCharges() {
		document.importICUChargesform.orgId.value = document.newbedmasterform.orgId.value;
		var form = document.importICUChargesform;
		if (form.uploadICUBedChargesFile.value == '') {
			alert("Please browse and select a file to upload");
			return false;
		}
		form.submit();
	}

	function checkAmpersand(e)
	{
		var key=0;
		if(window.event || !e.which){
			key = e.keyCode;
		}else{
			key = e.which;
		}
		if(key!=38){
			key=key;
			return true;
		}else{
			key=0;
			return false;
		}
	}

	function checkAmpersandOnBlur(){
		var form = document.newbedmasterform
		var bedType = form.bedtype.value;
		if(bedType.indexOf("&")>=0){
			alert('& is not allowed in Bed type name');
			form.bedtype.value = "";
			form.bedtype.focus();
			return true;
		}
		else
			return false;
	}