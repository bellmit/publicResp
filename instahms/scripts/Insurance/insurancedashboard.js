var theForm ;
var toolbar = {
	Edit: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: 'Insurance/AddOrEditCase.do?_method=addshow',
		onclick: null,
		description: "View and/or Edit Insurance Case"
	},
	Upload: {
		title: "Upload",
		imageSrc: "icons/Upload.png",
		href: 'Insurance/UploadReceivedDocs.do?_method=add',
		onclick: null,
		description: "Upload Received Insurance Docs"
	},
	Send: {
		title: "Send",
		imageSrc: "icons/Send.png",
		href: 'Insurance/SendToTpa.do?_method=show',
		onclick: null,
		description: "Send Insurance Docs"
	},
	History: {
		title: "History",
		imageSrc: "icons/History.png",
		href: 'Insurance/InsuranceHistory.do?_method=show',
		onclick: null,
		description: "History of Sent and Received Insurance Docs"
	}
};

function initMrnoAutoComplete(){
	Insta.initMRNoAcSearch(contextPath, "mr_no", "mrnoContainer", "all"
	);

	createToolbar(toolbar);
}

function init() {
	theForm = document.InsuranceCaseForm;
	enableStage();
	if(visitConnectedFromAction == ""){
		document.forms[0].visitConnected[0].checked=true;
	}else{
		for (var i=0; i < document.forms[0].visitConnected.length; i++) {
			if(document.forms[0].visitConnected[i].value == visitConnectedFromAction)
				document.forms[0].visitConnected[i].checked=true;
		}
	}
}

function enableStage() {
	var disabled = theForm.statusAll.checked;

	theForm.statusPreAuth.disabled = disabled;
	theForm.statusCaseApproved.disabled = disabled;
	theForm.statusClaimProcessing.disabled = disabled;
	theForm.statusClosed.disabled = disabled;
}

function clearSearch() {
	theForm.statusPreAuth.checked = true;
	theForm.statusCaseApproved.checked = true;
	theForm.statusClaimProcessing.checked = true;
	theForm.tpaforms.value = "";

	theForm.mrno.value = "";
	theForm.firstName.value="";
	theForm.lastName.value="";

	enableStage() ;
}

function doSearch(){
	document.getElementById("searchField").value="Search";
	return true;
}

function addToDashBoard(){
	var mrNo = document.getElementById("mrNoAdmit").value.trim();
	var selectedTPA = document.getElementById("tpaIdAdmit").value;

	if(mrNo == ""){
		document.getElementById("patNameAdmit").value = "";
		alert("Please Enter MR No to Add");
		document.forms[0].mrNoAdmit.focus();
		return false;
	}
	if(selectedTPA == ""){
		alert("Please select TPA to Add");
		document.forms[0].tpaIdAdmit.focus();
		return false;
	}
	var patName = document.getElementById("patNameAdmit").value.trim();
	if( patName == ""){
		return false;
	}
	if(onChangeMrno(document.forms[0].mrNoAdmit)){
		document.forms[0].action = "../pages/insurance.do?method=addToDashBoard";
		document.forms[0].submit();
	}else return false;
}



function getPatientDetails(){
  var mrno = theForm.mrNoAdmit.value;
  if(mrno != ''){
	var ajaxReqObject = newXMLHttpRequest();
	var url = '../pages/registration/quickregistration.do?method=getPatientDetailsJSON&mrno='+mrno;
	getResponseHandlerText(ajaxReqObject, handleAjaxResponseForpatientDetails, url.toString());
  }
}

function handleAjaxResponseForpatientDetails(responseText){
	eval("var patient =" + responseText);
	if (patient == null) {
		alert("Enter valid MRNO");
		document.forms[0].mrNoAdmit.focus();
		resetFieldValues();
		return false;
	}
	theForm.patNameAdmit.value = patient.firstName +' '+patient.lastName;
	return true;
}

function resetFieldValues(){
	theForm.mrNoAdmit.value = "";
	theForm.patNameAdmit.value = "";
}

function getSelectedPage(){
	alert("Sorry!! Yet to Implement");
	return false;
}


function fetchInsuranceDetails(){
	if((document.getElementById("mrnoSearch").value != "") || (document.getElementById("patNameSearch").value != "") || (document.getElementById("tpaIdSearch").value != "") || (document.getElementById("stageSearch").value != "") ){
		document.forms[0].action = "insurance.do?method=fetchDetailsBasesOnSearch";
		document.forms[0].submit();
	}else{
		alert("Please Enter any criteria to search");
		return false;
	}
}

function displayInDashBoard(){
	var formData = insuranceDetails;
	insertRowsInDashBoard(formData);
}

	function insertRowsInDashBoard(formData){

		var preAuthPrefTimer = "0";
		var claimPrefTimer = "0";
		var finalClaimPrefTimer = "0";

		var myTable	= document.getElementById("insDashBoard");
		if(myTable.rows.length>1){
			for(d=0;myTable.rows.length>1;d++){
				myTable.deleteRow(-1);
			}
		}
		var tpaSearch = document.getElementById("tpaIdSearch");

		for(var t=0;t<tpaSearch.length;t++){
			if(tpaSearch[t].value == selectedSearchTpaId){
				document.forms[0].tpaIdSearch.selectedIndex=t;
			}
		}

		var stgSearch = document.getElementById("stageSearch");

		for(var s=0;s<stgSearch.length;s++){
			if(stgSearch[s].value == selectedSearchStage){
				document.forms[0].stageSearch.selectedIndex=s;
			}
		}

		var timerRef = new Array();
		for(var i=0;i<timerReferences.length;i++){
			timerRef[i] = new Array();
			timerRef[i][0] = timerReferences[i].INSURANCE_ID;
			timerRef[i][1] = timerReferences[i].TIMER_REF;
		}

		for(var i=0;i<InsurancePreferences.length;i++){
			preAuthPrefTimer 		= InsurancePreferences[i].PREAUTH_TIMER;
			claimPrefTimer   		= InsurancePreferences[i].CLAIM_TIMER;
			finalClaimPrefTimer   	= InsurancePreferences[i].FINALCLAIM_TIMER;
		}

		for(var i=0;i<formData.length;i++){
			var mrNo 		= formData[i].MR_NO;
			var patName 	= formData[i].PATIENT_NAME;
			var patDeptName = formData[i].DEPT_NAME;
			var tpa 		= formData[i].TPA_NAME;
			var estimateAmt = formData[i].ESTIMATE_AMOUNT;
			var ApprovalAmt	= formData[i].APPROVED_LIMIT;
			var BillAmt		= formData[i].CLAIM_AMOUNT;
			var stage		= formData[i].STAGE_NAME;
			var status 		= formData[i].STATUS_NAME;
			var hrs 		= formData[i].HRS;

			var insuranceId = formData[i].INSURANCE_ID;
			var patId		= formData[i].PATIENT_ID;
			var preAuthId	= formData[i].PRE_AUTH_ID;

			var est_transaction_type		= formData[i].EST_TRANSACTION_TYPE;
			var preauth_transaction_type	= formData[i].PREAUTH_TRANSACTION_TYPE;
			var claim_transaction_type		= formData[i].CLAIM_TRANSACTION_TYPE;


			var globalTimerRef = "";

			for(var t=0;t<timerRef.length;t++){
				if(timerRef[t][0] == insuranceId){
					globalTimerRef = timerRef[t][1];
				}
			}

			var patName1 = encodeURIComponent(patName);
			var patDeptName1 = encodeURIComponent(patDeptName);

			row = myTable.insertRow(-1);
			row.id = "status"+i;
			var message = "";

			var rowid=document.getElementById(row.id);
          	if(hrs != 'null' || hrs !=""){
	           	if(globalTimerRef == "60"){
	           		if(parseInt(preAuthPrefTimer) != 0 ){
						if(parseInt(hrs) > (parseInt(preAuthPrefTimer)) * 60){
							row.style.background = "#FFA07A";
							timerRef = Math.round((parseInt(hrs) - ((parseInt(preAuthPrefTimer)) * 60))/60);
							message = "Prior Auth approval overdue by - "+ timerRef + " hrs.";
						}
					}
				}else if(globalTimerRef == "80"){
					if(parseInt(claimPrefTimer) != 0 ){
						if(parseInt(hrs) > (parseInt(claimPrefTimer)) * 60){
							row.style.background = "#FFA07A";
							timerRef = Math.round((parseInt(hrs) - ((parseInt(claimPrefTimer)) * 60))/60);
							message = "Claim approval overdue by - "+ timerRef + " hrs.";
						}
					}
				}else if(globalTimerRef == "90"){
					if(parseInt(finalClaimPrefTimer) != 0 ){
						if(parseInt(hrs) > (parseInt(finalClaimPrefTimer)) * 60){
							row.style.background = "#FFA07A";
							timerRef = Math.round((parseInt(hrs) - ((parseInt(finalClaimPrefTimer)) * 60))/60);
							message = "Final-Claim approval overdue by - "+ timerRef + " hrs.";
						}
					}
				}
			}
			cell=row.insertCell(-1);
			cell.align="left";
			cell.colspan="1";
			cell.innerHTML="<span class='label'>"+mrNo;

			cell=row.insertCell(-1);
			cell.align="left";
			cell.colspan="1";
			cell.innerHTML="<span class='label'>"+patName;

			cell=row.insertCell(-1);
			cell.align="left";
			cell.colspan="1";
			cell.innerHTML="<span class='label'>"+tpa;

			cell=row.insertCell(-1);
			cell.align="right";
			cell.colspan="1";
			if(estimateAmt == undefined ){
				cell.innerHTML="<a href='../pages/insurance/EstimateAction.do?method=getEstimationPrerequisitesScreen&insuranceID="+insuranceId+"&moduleId=mod_insurance' title='Click to Add Estimate'>"+"New"+"</a>";
			}else{
				if(est_transaction_type == "S"){
					cell.innerHTML="<a href='../pages/insurance/EstimateAction.do?method=getEstimationScreen&insuranceID="+insuranceId+"&moduleId=mod_insurance' title='Click to View/Update Estimate'>"+estimateAmt+"</a>";
				}else if(est_transaction_type == "T"){
					cell.innerHTML="<a href=''  onclick='return checkForEstimateUserInput("+insuranceId+");' >"+estimateAmt+"</a>";
				}else{
					cell.innerHTML="<a href='../pages/insurance/EstimateAction.do?method=getEstimationPrerequisitesScreen&insuranceID="+insuranceId+"&moduleId=mod_insurance' title='Click to Add Estimate'>"+"New"+"</a>";
				}
			}
			cell=row.insertCell(-1);
			cell.align="right";
			cell.colspan="1";
			if(patId == undefined){
				patId = "";
			}

			if(preauth_transaction_type == "S"){
				cell.innerHTML="<a href='../pages/insurance/InsurancePreauthForm.do?method=getPreAuthForm&mrno="+mrNo+"&patId="+patId+"&insuranceId="+insuranceId+"&patName="+patName1+"&patDeptName="+patDeptName1+"' title='Click to View/Update Pre Auth'>"+estimateAmt+"</a>";
			}else if(preauth_transaction_type == "T"){
				cell.innerHTML="<a href='' id="+insuranceId+","+mrNo+","+patId+","+patName1+","+patDeptName1+" onclick='return checkForPreAuthUserInput(this);' >"+estimateAmt+"</a>";
			}else{
				cell.innerHTML="<a href='../pages/insurance/InsurancePreauthForm.do?method=getPreAuthForm&mrno="+mrNo+"&patId="+patId+"&insuranceId="+insuranceId+"&patName="+patName1+"&patDeptName="+patDeptName1+"' title='Click to Add Pre Auth'>"+"New"+"</a>";
			}

			cell=row.insertCell(-1);
			cell.align="right";
			cell.colspan="1";
			if(ApprovalAmt == undefined){
				cell.innerHTML="<span class='label'>"+"-";
			}else{
				cell.innerHTML="<a href='../pages/insurance.do?method=getTransactionForm&insuranceId="+insuranceId+"&transactionType=15' >"+ApprovalAmt+"</a>";
			}

			cell=row.insertCell(-1);
			cell.align="right";
			cell.colspan="1";
			if(BillAmt == undefined ){
				cell.innerHTML="<span class='label'>"+"-";
			}else{
				cell.innerHTML="<a href='' onclick='return getSelectedPage();'>"+BillAmt+"</a>";
			}

			cell=row.insertCell(-1);
			cell.align="left";
			cell.colspan="1";
			if(stage == undefined || stage == "" || stage == "NULLx"){
				cell.innerHTML="<span class='label'>"+"New";
			}else{
				cell.innerHTML="<span class='label'>"+stage;
			}

			cell=row.insertCell(-1);
			cell.align="left";
			cell.colspan="1";
			if(status == undefined || status == "" || status == "NULLx"){
				cell.innerHTML="<span class='label'>"+"New";
			}else{
				cell.innerHTML="<span class='label'>"+status;
			}

			cell=row.insertCell(-1);
			cell.align="left";
			cell.colspan="1";
			var url = '../pages/insurance/InsuranceTransactionTrackingScreen.do?method=getTransactionDetails&mrno='+mrNo+'&patId='+patId+'&insuranceId='+insuranceId+'&patName='+patName1+'&patDeptName='+patDeptName1+'&message='+message;
			cell.innerHTML="<a href='"+url+"'>"+"Edit"+"</a>";
		 }
	}
	function getPage(anchor,offset){
		var href1 = anchor.getAttribute("href");

		if((document.getElementById("mrnoSearch").value != "") || (document.getElementById("patNameSearch").value != "") || (document.getElementById("tpaIdSearch").value != "") || (document.getElementById("stageSearch").value != "") ){
			href1 = href1 + "method=fetchDetailsBasesOnSearch&offsetval="+offset;
		}else{
			href1 = href1+"method=getDashBoardScreen&offsetval="+offset;
		}
		anchor.setAttribute("href", href1);
	}
	function clearFields(){
		document.forms[0].mrNoAdmit.value="";
		document.forms[0].patNameAdmit.value="";
		document.forms[0].tpaIdAdmit.value="";
	}
	var selectedinsuranceId = "";
	var selectedMrno ="";
	var selectedPatId= "";
	var selectedPatName="";
	var selectedPatDeptName = "";
	var newwinStatus = false;

	function checkForPreAuthUserInput(obj){
		var idArray 	= obj.id.split(",");
		selectedinsuranceId	= idArray[0];
		selectedMrno	= idArray[1];
		selectedPatId	= idArray[2];
		selectedPatName = idArray[3];
		selectedPatDeptName = idArray[4];

		if(newwinStatus ==  false){
			var win=window.open("",'abcWin','width=400, height=100, screenX=500,screenY=500,left=500,top=500,scrollbars=2,menubar=0');
			win.document.write("<html><head><title>Select </title><\/head>");
			win.document.write("<body bgcolor=#D9EABB  onunload='window.opener.changeWinStatus()'>Click  ");
			win.document.write("<a href='javascript:window.opener.getPreAuthTransactionPage();window.close()'>here</a>&nbsp;&nbsp;");
			win.document.write("to view Uploaded Files <br/>");
			win.document.write("Click ");
			win.document.write("<a href='javascript:window.opener.getPreAuthPage();window.close()' >here</a>&nbsp;&nbsp;<\/body><\html>");
			win.document.write("to open a New Prior Auth form.<br/> ");
			newwinStatus = true;
		}
		return false;
	}
	function getPreAuthTransactionPage(){
		document.forms[0].action="../pages/insurance.do?method=getTransactionForm&insuranceId="+selectedinsuranceId+"&transactionType=10";
		document.forms[0].submit();
	}
	function getPreAuthPage(){

		document.forms[0].action="../pages/insurance/InsurancePreauthForm.do?method=getPreAuthForm&mrno="+selectedMrno+"&patId="+selectedPatId+"&insuranceId="+selectedinsuranceId+"&patName="+selectedPatName+"&patDeptName="+selectedPatDeptName;
		document.forms[0].submit();
	}
	function checkForEstimateUserInput(insuranceId){
		if(newwinStatus ==  false){
			var win=window.open("",'abcWin','width=400, height=100, screenX=500,screenY=500,left=500,top=500,scrollbars=2,menubar=0');
			win.document.write("<html><head><title>Select </title><\/head>");
			win.document.write("<body bgcolor=#D9EABB  onunload='window.opener.changeWinStatus()'>Click  ");
			win.document.write("<a href='javascript:window.opener.getEstimateTransactionPage("+insuranceId+");window.close()'>here</a>&nbsp;&nbsp;");
			win.document.write(" to view Uploaded Files <br/>");
			win.document.write("Click ");
			win.document.write("<a href='javascript:window.opener.getEstimatePage("+insuranceId+");window.close()' >here</a><\/body><\html>");
			win.document.write("to open a New Estimate form.<br/> ");
			newwinStatus = true;
		}
		return false;
	}
	function getEstimateTransactionPage(insuranceId){
		document.forms[0].action="../pages/insurance.do?method=getTransactionForm&insuranceId="+insuranceId+"&transactionType=5";
		document.forms[0].submit();
	}
	function getEstimatePage(insuranceId){
		document.forms[0].action="../pages/insurance/EstimateAction.do?method=getEstimationPrerequisitesScreen&insuranceID="+insuranceId+"&moduleId=mod_insurance";
		document.forms[0].submit();
	}
	function changeWinStatus(){
		newwinStatus = false;
		return false;
	}

	function getOtherActionScreen(insuranceId,mrNo,patId,patName,deptName,tpaPdfForm,index,billType,billNo,billStatus){
		var selectedAction = document.getElementById("otherActions"+index).value;

		if(selectedAction == "EditEstimate"){
			document.forms[0].action="../pages/insurance/EstimateAction.do?method=getEstimationScreen&insuranceID="+insuranceId+"&moduleId=mod_insurance";
			document.forms[0].submit();
		}else if(selectedAction == "NewEstimate"){
			document.forms[0].action="../pages/insurance/EstimateAction.do?method=getEstimationPrerequisitesScreen&insuranceID="+insuranceId+"&moduleId=mod_insurance";
			document.forms[0].submit();
		}else if(selectedAction == "PreAuth"){
			if(tpaPdfForm == "" || tpaPdfForm == " "){
				document.getElementById("otherActions"+index).value ="";
				alert("System Generated Format not available,Please select a Prior Auth form in Tpa Master (or) Please scan and upload using Transaction Screen");
				return false;
			}else{
				document.forms[0].action="../pages/insurance/InsurancePreauthForm.do?method=getPreAuthForm&mrno="+mrNo+"&patId="+patId+"&insuranceId="+insuranceId+"&patName="+patName+"&patDeptName="+deptName;
				document.forms[0].submit();
			}
		}else if(selectedAction == "EditClaim"){
			if (billStatus == 'X'){
				alert("Bill related to this claim cancelled");
				return false;
			}else if ( billStatus == 'C'){
				alert("Bill related to this claim closed");
				return false;
			}

			if((billType == "C") && (patId !="")){
				document.forms[0].action="BillDischarge/InsuranceClaimAction.do?method=getInsuranceClaimScreen&mrNo="+mrNo+"&insuranceID="+insuranceId+"&visitId="+patId+"&billNo="+billNo+"&do=edit";
				document.forms[0].submit();
			}else{
				document.getElementById("otherActions"+index).value ="";
				if(patId == ""){
					alert("No Visit connected to case. \nPlease connect the case to latest insurance visit from 'edit visit details'screen");
					return false;
				}else if(billType != "C"){
					alert("No Credit bill available");
					return false;
				}
			}
		}else if(selectedAction == "ViewClaim"){
			if((billType == "C") && (patId !="")){
				document.forms[0].action="BillDischarge/InsuranceClaimAction.do?method=getInsuranceClaimScreen&mrNo="+mrNo+"&insuranceID="+insuranceId+"&visitId="+patId+"&billNo="+billNo+"&do=view";
				document.forms[0].submit();
			}else{
				document.getElementById("otherActions"+index).value ="";
				if(patId == ""){
					alert("No Visit connected to case. \nPlease connect the case to latest insurance visit from 'edit visit details'screen");
					return false;
				}else if(billType != "C"){
					alert("No Credit bill available");
					return false;
				}
			}
		}else if(selectedAction == "Bill"){
			if((billType == "C") && (patId !="")){
				window.open("./Enquiry/billprint.do?method=billPrint&billNo="+billNo+"&printerType=0&detailed=DET");
			}else{
				document.getElementById("otherActions"+index).value ="";
				if(patId == ""){
					alert("No Visit connected to case. \nPlease connect the case to latest insurance visit from 'edit visit details'screen");
					return false;
				}else if(billType != "C"){
					alert("No Credit bill available");
					return false;
				}
			}
		}
	}

	function popup_mrsearch(){
		window.open(
	    '../pages/Common/PatientSearchPopup.do?title=All%20Patients&mrnoForm=InsuranceCaseForm&mrnoField=mrNoAdmit&searchType=all',
	    'Search',
	    'width=655,height=430,scrollbars=yes,left=200,top=200');

	    return false;
	}//end of popup_mrsearch





