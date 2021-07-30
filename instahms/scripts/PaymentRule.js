
function validate(){
	document.forms[0].submit();
}

function loadChargeHeads(){
	var chargeGroup = document.forms[0].charge_group.value;
	var chargeHead = document.forms[0].charge_head;
	var index = 1;
	var chargeHeadsList = filterList(chargeHeads, 'CHARGEGROUP_ID', chargeGroup);
	chargeHead.length = chargeHeadsList+1;
 	for(var i=0;i<chargeHeadsList.length;i++){
 		var item = chargeHeadsList[i];
 		var optn = document.createElement("OPTION");
 		optn.text = item.CHARGEHEAD_NAME;
		optn.value = item.CHARGEHEAD_ID;
		if (chargeHeadId == item.CHARGEHEAD_ID) {
			optn.selected = true;
		}
		chargeHead.options.add(optn);
 	}

}

function loadActivity(){
 	var chargeHead = document.forms[0].charge_head.value;
 	var activity = document.forms[0].activity_id;
 	var index = 1;
 	if(chargeHead=='LTDIA') {
 		activity.length = laboratoryDetails.length+1;
 		activity.options[0].text="..Select..";
 		activity.options[0].value="";
 		for(var i=0;i<laboratoryDetails.length;i++){
			var item = laboratoryDetails[i];
			activity.options[index].text = item.TEST_NAME;
			activity.options[index].value = item.TEST_ID;
			if (activityId == item.TEST_ID) {
				activity.options[index].selected = true;
			}
			index++;
 		}
 	}else if(chargeHead=='RTDIA'){
 		activity.length = radiologyTestDetails.length+1;
		activity.options[0].text="..Select..";
		activity.options[0].value="";
 		for(var i=0;i<radiologyTestDetails.length;i++){
			var item = radiologyTestDetails[i];
			activity.options[index].text = item.TEST_NAME;
			activity.options[index].value = item.TEST_ID;
			if (activityId == item.TEST_ID) {
				activity.options[index].selected = true;
			}
			index++;
 		}
 	}else if(chargeHead=='SERSNP'){
 		activity.length = serviceDetails.length+1;
 		activity.options[0].text="..Select..";
 		activity.options[0].value="";
 		for(var i=0;i<serviceDetails.length;i++){
			var item = serviceDetails[i];
			activity.options[index].text = item.SERVICE_NAME;
			activity.options[index].value = item.SERVICE_ID;
			if (activityId == item.SERVICE_ID) {
				activity.options[index].selected = true;
			}
			index++;
 		}
 	}else if(chargeHead=='PKGPKG'){
 		activity.length = packageDetails.length+1;
 		activity.options[0].text="..Select..";
 		activity.options[0].value="";
 		for(var i=0;i<packageDetails.length;i++){
			var item = packageDetails[i];
			activity.options[index].text = item.PACKAGE_NAME;
			activity.options[index].value = item.PACKAGE_ID;
			if (activityId == item.PACKAGE_ID) {
				activity.options[index].selected = true;
			}
			index++;
 		}
 	}else{
 		activity.length=0;
 	}
 	if ( document.getElementById("doctorSelect").value != 5){
 		document.getElementById('drExpr').style.display = "none";
 	}
 	if ( document.getElementById("referalDoctorSelect").value != 5){
 		document.getElementById('refExpr').style.display = "none";
 	}
 	if ( document.getElementById("prescribingDoctorSelect").value != 5){
 		document.getElementById('prescExpr').style.display = "none";
 	}

}

var labtestNameArray = [];
var radtestNameArray = [];
var serviceNameArray = [];
function autoCompleteForActivity(){

		var chargeHead = document.getElementById("chargeHeadId").value;
		var oAutoComp;
		labtestNameArray.length = labTestDetails.length;
		radtestNameArray.length = radTestDetails.length;
		serviceNameArray.length = servicesDetails.length;
		for(var i=0;i<labTestDetails.length;i++){
			var item = labTestDetails[i];
			labtestNameArray[i] = item["TEST_NAME"];
		}
		for(var i=0;i<radTestDetails.length;i++){
			var item = radTestDetails[i];
			radtestNameArray[i] = item["TEST_NAME"];
		}
		for ( i=0 ; i< servicesDetails.length; i++) {
			var item = servicesDetails[i]
			serviceNameArray[i] = item["SERVICE_NAME"];
		}

		dataSource = new YAHOO.widget.DS_JSArray(empt);
		oAutoComp = new YAHOO.widget.AutoComplete('activityId', 'activity_dropdown', dataSource);
		oAutoComp.destroy();

		if (chargeHead=='LTDIA') {
			dataSource = new YAHOO.widget.DS_JSArray(labtestNameArray);
			oAutoComp = new YAHOO.widget.AutoComplete('activityId', 'activity_dropdown', dataSource);
			oAutoComp.itemSelectEvent.subscribe(getActivityId);
		} else if (chargeHead=='RTDIA') {
			dataSource = new YAHOO.widget.DS_JSArray(radtestNameArray);
			oAutoComp = new YAHOO.widget.AutoComplete('activityId', 'activity_dropdown', dataSource);
			oAutoComp.itemSelectEvent.subscribe(getActivityId);
		} else if (chargeHead=='SERSNP') {
			dataSource = new YAHOO.widget.DS_JSArray(serviceNameArray);
			oAutoComp = new YAHOO.widget.AutoComplete('activityId', 'activity_dropdown', dataSource);
			oAutoComp.itemSelectEvent.subscribe(getActivityId);
		}

		oAutoComp.allowBrowserAutocomplete = false;
		oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		oAutoComp.typeAhead = false;
		oAutoComp.useShadow = false;
		oAutoComp.minQueryLength = 0;
		oAutoComp.forceSelection = false;
		oAutoComp.animVert = false;


		function getActivityId(){
			var activityName = YAHOO.util.Dom.get('activityId').value;
			for (var i in labTestDetails){
				var lab = labTestDetails[i];
				if (lab.TEST_NAME == activityName){
					document.getElementById("activity_id").value =lab.TEST_ID;
				}
			}

			for (var i in radTestDetails){
				var rad = radTestDetails[i];
				if (rad.TEST_NAME == activityName){
					 document.getElementById("activity_id").value = rad.TEST_ID;
				}
			}

			for (var i in servicesDetails){
				var service = servicesDetails[i];
				if (service.TEST_NAME == activityName){
					 document.getElementById("activity_id").value = service.TEST_ID;
				}
			}
		}

    }

function validate(){
	var chargeHead = document.forms[0].chargeHeadId.value;
	if (chargeHead=='LTDIA')  {
		for(i=0; i<labTestDetails.length; i++){
		   item = labTestDetails[i];
		   var testName = item["TEST_NAME"];
			if (document.searchForm.activityId.value==testName) {
				document.searchForm.testId.value=item["TEST_ID"];
			}
		}
	 }else if(chargeHead=='RTDIA') {
		for(i=0;i<radTestDetails.length;i++) {
			item = radTestDetails[i];
			var radName = item["TEST_NAME"];
			if (document.searchForm.activityId.value==radName) {
				document.searchForm.testId.value=item["TEST_ID"];
			}
		}
	 } else if (chargeHead=='SERSNP') {
		for (i=0; i<servicesDetails.length; i++) {
		   item = servicesDetails[i];
		   var serviceName = item["SERVICE_NAME"];
			if (document.searchForm.activityId.value==serviceName) {
				document.searchForm.serviceId.value=item["SERVICE_ID"];
			}
		}
	}
}


 function reorderPrecedence(){
 	 document.paymentRuleform.method.value="reorderPrecedenceValues";
 	 document.paymentRuleform.action="PaymentRule.do?method=reorderPrecedenceValues";
	 document.paymentRuleform.submit();
 }

 function clearFields(){
	document.forms[0].ratePlanId.selectedIndex = 0;
	document.forms[0].docCategoryId.selectedIndex = 0;
	document.forms[0].refCategoryId.selectedIndex = 0;
	document.forms[0].chargeHeadId.selectedIndex = 0;
	document.forms[0].chargeGroupId.selectedIndex = 0;
 }

 function validateDeletion(index,value){
 	document.paymentRuleform.priorityValue.value=value;
 	if(document.getElementById('checkDelBox'+index).checked)
 	{
		 if(confirm('Do you want to delete?')){
		 	document.paymentRuleform._method.value="deletePaymentRule";
		 	document.paymentRuleform.submit();
				return true;
			}else{
				return false;
			}
	}
}

function selectLessAmountOption(){
	var doctorOptionValue = document.getElementById("doctorSelect").value ;
	var prescribingOptionValue = document.getElementById("prescribingDoctorSelect").value;
	var referalOptionValue = document.getElementById("referalDoctorSelect").value;
	if (doctorOptionValue == "4" || prescribingOptionValue == "4" || referalOptionValue == "4"){
		if (doctorOptionValue == "4"){
			document.getElementById('drExpr').style.display = "none";
			document.getElementById('drExprLbl').style.display = "none";
		}else if (referalOptionValue == "4"){
 			document.getElementById('refExpr').style.display = "none";
 			document.getElementById('refExprLbl').style.display = "none";
		}else if (prescribingOptionValue == "4"){
 			document.getElementById('prescExpr').style.display = "none";
 			document.getElementById('prescExprLbl').style.display = "none";
		}
	}else if (doctorOptionValue == "5" || prescribingOptionValue == "5" || referalOptionValue == "5"){
		if (doctorOptionValue == "5"){
			document.getElementById('drExpr').style.display = "";
			document.getElementById('drExprLbl').style.display = "";
			document.getElementById("dr_payment_value").value = 0;
		} else{
			document.getElementById('drExpr').style.display = "none";
			document.getElementById('drExprLbl').style.display = "none";
		}
		if (referalOptionValue == "5"){
			document.getElementById('refExpr').style.display = "";
			document.getElementById('refExprLbl').style.display = "";
			document.getElementById("ref_payment_value").value = 0;
		} else{
			document.getElementById('refExpr').style.display = "none";
			document.getElementById('refExprLbl').style.display = "none";
		}
		if (prescribingOptionValue == "5"){
			document.getElementById('prescExpr').style.display = "";
			document.getElementById('prescExprLbl').style.display = "";
			document.getElementById("presc_payment_value").value = 0;
		} else{
			document.getElementById('prescExpr').style.display = "none";
			document.getElementById('prescExprLbl').style.display = "none";
		}

	}else{
		document.getElementById("doctorSelect").length = 4;
		document.getElementById("doctorSelect").options[2].value = "4";
		document.getElementById("doctorSelect").options[2].text = "Less than bill amount";
		if (doctorOptionValue != 5){
			document.getElementById('refExpr').style.display = "none";
			document.getElementById('refExprLbl').style.display = "none";
		}

		document.getElementById("referalDoctorSelect").length = 4;
		document.getElementById("referalDoctorSelect").options[2].value = "4";
		document.getElementById("referalDoctorSelect").options[2].text = "Less than bill amount";
		if (referalOptionValue != 5){
			document.getElementById('drExpr').style.display = "none";
			document.getElementById('drExprLbl').style.display = "none";
		}

		document.getElementById("prescribingDoctorSelect").length = 4;
		document.getElementById("prescribingDoctorSelect").options[2].value = "4";
		document.getElementById("prescribingDoctorSelect").options[2].text = "Less than bill amount";
		if (prescribingOptionValue != 5){
			document.getElementById('prescExpr').style.display = "none";
			document.getElementById('prescExprLbl').style.display = "none";
		}

	}

}
	function validateScreen(){
		var chargeId = document.getElementById("charge_head").value;
		var precedance = document.getElementById("precedance").value;
		var doctorCategory = document.getElementById("doctor_category").value;
		var referrerCategory = document.getElementById("referrer_category").value;
		var ratePlan = document.getElementById("rate_plan").value;
		var prescCategory = document.getElementById("prescribed_category").value;
		if (chargeId == ""){
				alert("Select Charge Head");
				return false;
		}

		validatePriority(doctorCategory, referrerCategory, prescCategory, ratePlan, precedance, chargeId);

	}

	function validatePriority(doctorCat, referrerCat, prescCat, ratePlan, newPrecedance, chargeId) {

		var url = cpath +'/master/PaymentRule.do?_method=getPrecedance&chargeId='+chargeId;
		var allStarrule = false;

		if (doctorCat == '*' && referrerCat == '*' && prescCat == '*' && ratePlan == '*') {
			url += '&all=true';
			allStarrule = true;
		} else {
			url += '&all=false';
		}

		if (window.XMLHttpRequest) {
			req = new XMLHttpRequest();
		} else if(window.ActiveXObject) {
			req = new ActiveXObject("MSXML2.XMLHTTP");
		}
		req.open('GET', url, true);
		req.setRequestHeader('Content-Type', 'text/plain');
		req.send(null);
		req.onreadystatechange = function() {
			if (req.readyState == 4 && req.status == 200) {
				var existPrecedance = req.responseText;
				if (existPrecedance == '')
					document.paymentForm.submit();
				existPrecedance = parseInt(existPrecedance);
				if (allStarrule) {
					if (newPrecedance <= existPrecedance) {
						alert('The priority should be greater than '+existPrecedance);
						return false;
					}
				} else {
					if (existPrecedance <= newPrecedance) {
						alert('The priority should  be less than '+existPrecedance);
						return false;
					}
				}
				document.paymentForm.submit();
			}

		}
	}