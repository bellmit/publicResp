function getPreAuthForm() {
		var tpaID = document.getElementById("tpaID");
		var tpaName = document.getElementById("tpaName");
		var tpaPdfForm = document.getElementById("tpaPdfForm");
		var insuranceNo = document.getElementById("insuranceNo");
		var estimate = document.getElementById("estimate");
		var preAuthId = document.getElementById("preAuthId");
		var claimNo = document.getElementById("claimNo");
		xmlDataLen = preAuthDetailsJSON.length;
		var documentAtrbs = preAuthDetailsJSON[0];
		tpaID.value = documentAtrbs.TPA_ID;
		tpaName.value = documentAtrbs.TPA_NAME;
		tpaPdfForm.value = documentAtrbs.TPA_PDF_FORM;
		if(documentAtrbs.PREAUTH_AMOUNT != undefined){
			estimate.value = documentAtrbs.PREAUTH_AMOUNT;
		}
		if(documentAtrbs.PRE_AUTH_ID == undefined){
			document.getElementById("actionName").value="SAVE";
			document.preauth.print.disabled=true;
		}
		else{
			document.getElementById("actionName").value="UPDATE";
		}
		if(documentAtrbs.PRE_AUTH_ID == undefined){
			preAuthId.value = "";
		}
		else{
			preAuthId.value = documentAtrbs.PRE_AUTH_ID;
		}
		if(documentAtrbs.CLAIM_NO == undefined){
			claimNo.value = "";
		}
		else{
			claimNo.value = documentAtrbs.CLAIM_NO;
		}
		if(documentAtrbs.INSURANCE_NO != undefined){
			insuranceNo.value = documentAtrbs.INSURANCE_NO;
		}
	}

	function validate(action){
		document.preauth.saveAction.value = action;
		if(numberCheck(document.getElementById("estimate")))
			return validateNum(document.getElementById("estimate"));
		else{
			document.preauth.estimate.focus();
			return false;
		}
	}

	function getFormPrint(preauthId){
	    var mrNo =  document.getElementById("mrno").value;
	    var tpaForm =  document.getElementById("tpaPdfForm").value;
	    if((document.getElementById("saveAction").value == "Print") && (preauthId != "")){
			var url="./InsurancePreauthForm.do?method=printPreAuthFormPdf&preAuthID="+preauthId+"&mrNo="+mrNo+"&tpaPdfForm="+tpaForm;
			window.open(url);
			window.location.href="#";
		}
	}

	function getPreAuthTableDetails(){
	if(preAuthTableDetailsJSON != null){
		var documentAtrbsNames = preAuthTableDetailsJSON.names;
		var documentAtrbsValues = preAuthTableDetailsJSON.values;
		var elmtLen = documentAtrbsNames.length;
		for(var i= 0; i<elmtLen;i++){
			var elName = documentAtrbsNames[i];
			if(elName == "pre_auth_id") { }
			else{
			document.getElementsByName(elName)[0].value= documentAtrbsValues[i];
			}
		}
		}
		else{
			loadKnownFormDetails();
		}
	}

	function loadKnownFormDetails(){
		var tpaName = document.getElementById("tpaName").value;
		var knownAttribs = preAuthKnownJSON[0];
		if(tpaName == "TTK"){
			document.getElementsByName("ttkidcardno")[0].value = knownAttribs.INSURANCE_NO;
			document.getElementsByName("patname")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("age")[0].value = knownAttribs.PATIENT_AGE +" " + knownAttribs.PATIENT_AGE_IN;
			document.getElementsByName("sex")[0].value = knownAttribs.GENDER;
			document.getElementsByName("mobileno")[0].value = knownAttribs.PATIENT_PHONE;
			document.getElementsByName("occupation")[0].value = knownAttribs.OCCUPATION;
			document.getElementsByName("chiefcomplaints")[0].value = knownAttribs.PATIENT_AILMENT;
		}
		if(tpaName == "BAJAJ ALLIANZ"){
			document.getElementsByName("idcardno")[0].value = knownAttribs.INSURANCE_NO;
			document.getElementsByName("beneficiaryname")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("agegender")[0].value = knownAttribs.PATIENT_AGE + " " + knownAttribs.PATIENT_AGE_IN+" / "+knownAttribs.GENDER;
			document.getElementsByName("treatingdoctor")[0].value = knownAttribs.DOCTOR_NAME;
			document.getElementsByName("presentailmentdetails")[0].value = knownAttribs.PATIENT_AILMENT;
			document.getElementsByName("city")[0].value = knownAttribs.CITY_NAME;
			document.getElementsByName("state")[0].value = knownAttribs.STATE_NAME;
		}
		if(tpaName == "UNITED INSURANCE"){
			document.getElementsByName("nameofpatient")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("employeemobile")[0].value = knownAttribs.PATIENT_PHONE;
			document.getElementsByName("treatingphysician")[0].value = knownAttribs.DOCTOR_NAME;
			document.getElementsByName("dateofadmission")[0].value = knownAttribs.REG_DATE;
			document.getElementsByName("symptomsonadmission")[0].value = knownAttribs.PATIENT_AILMENT;
		}
		if(tpaName == "MEDI ASSIST"){
			document.getElementsByName("patientnamemediassist")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("phno")[0].value = knownAttribs.PATIENT_PHONE;
			document.getElementsByName("age")[0].value = knownAttribs.PATIENT_AGE+" "+knownAttribs.PATIENT_AGE_IN;
			document.getElementsByName("gender")[0].value = knownAttribs.GENDER;
			document.getElementsByName("tpaidnum")[0].value = knownAttribs.INSURANCE_NO;
			document.getElementsByName("tratingdocdetails")[0].value = knownAttribs.DOCTOR_NAME+" & "+knownAttribs.DOCTOR_MOBILE;
			document.getElementsByName("admissiondate")[0].value = knownAttribs.REG_DATE;
			document.getElementsByName("insurednamepage2")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("insuredphonenopage2")[0].value = knownAttribs.PATIENT_PHONE;
			document.getElementsByName("ailmentearlierhistory")[0].value = knownAttribs.PATIENT_AILMENT;
		}
		if(tpaName == "STAR HEALTH"){
			document.getElementsByName("policyno")[0].value = knownAttribs.INSURANCE_NO;
			document.getElementsByName("patientname")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("mobileno")[0].value = knownAttribs.PATIENT_PHONE;
			document.getElementsByName("age")[0].value = knownAttribs.PATIENT_AGE+" "+knownAttribs.PATIENT_AGE_IN;
			document.getElementsByName("sex")[0].value = knownAttribs.GENDER;
			document.getElementsByName("consultantname")[0].value = knownAttribs.DOCTOR_NAME;
			document.getElementsByName("consultantmobileno")[0].value = knownAttribs.DOCTOR_MOBILE;
			document.getElementsByName("dateofadmission")[0].value = knownAttribs.REG_DATE;
			document.getElementsByName("pastillness")[0].value = knownAttribs.PATIENT_AILMENT;
		}
		if(tpaName == "RAKSHA"){
			document.getElementsByName("policyno")[0].value = knownAttribs.INSURANCE_NO;
			document.getElementsByName("patientname")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("patname")[0].value = knownAttribs.PATIENT_NAME;
			document.getElementsByName("phonenumber")[0].value = knownAttribs.PATIENT_PHONE;
			document.getElementsByName("age")[0].value = knownAttribs.PATIENT_AGE+" "+knownAttribs.PATIENT_AGE_IN;
			document.getElementsByName("sex")[0].value = knownAttribs.GENDER;
			document.getElementsByName("treatingdoctor")[0].value = knownAttribs.DOCTOR_NAME;
			document.getElementsByName("contactno")[0].value = knownAttribs.DOCTOR_MOBILE;
			document.getElementsByName("admissiondate")[0].value = knownAttribs.REG_DATE;
			document.getElementsByName("pastailment")[0].value = knownAttribs.PATIENT_AILMENT;
		}
	}


function onClickValidate(action){
//	var action = obj.name;
	if(action == 'new'){
		var pdfformid = document.preauthform.tpaPdfForm.value;
		if( pdfformid == null || pdfformid == ''){
			alert("No preauth");
			//return false;
		}
		document.preauthform.method.value = "newPreauthForm";
	}else if(action == 'edit'){
		document.preauthform.method.value = "editPreauthForm";
		document.forms[0].submit();
	}else if(action == 'print'){
		var insuranceId = document.preauthform.insuranceId.value;
		var url = "InsurancePreauthForm.do?method=printPreauthForm&insuranceId="+insuranceId;
		window.open(url,'_blank');
	}else if(action == 'back'){
		var url = "../../Insurance/InsuranceDashboard.do?_method=list&filterClosed=true&status=A&status=P&status=F&sortOrder=insurance_id&sortReverse=true";
		window.open(url,'_self');
	}
	return true;
}


