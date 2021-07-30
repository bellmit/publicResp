function init() {
	initInsurancePhotoDialog();
	enableOtherInsuranceDetailsTab();
}

var insurancePhotoDialog;

function initInsurancePhotoDialog() {
	insurancePhotoDialog = new YAHOO.widget.Dialog('insurancePhotoDialog', {
		width: "550px",
		height: "250px",
		visible: false,
		modal: true,
		constraintoviewport: true,
		close: false,
	});

	var escKeyListener = new YAHOO.util.KeyListener(document, {
		keys: 27
	}, {
		fn: handleInsurancePhotoDialogCancel,
		scope: insurancePhotoDialog,
		correctScope: true
	});
	insurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	insurancePhotoDialog.render();
}

function showInsurancePhotoDialog() {
	var button = document.getElementById('_plan_card_td');
	resizeCustom(document.getElementById('insuranceImage'), 500, 200);
	insurancePhotoDialog.cfg.setProperty("context", [button, "tl", "bl"], false);
	document.getElementById('insurancePhotoDialog').style.display = 'block';
	document.getElementById('insurancePhotoDialog').style.visibility = 'visible';
	insurancePhotoDialog.show();
}

function handleInsurancePhotoDialogCancel() {
	document.getElementById('insurancePhotoDialog').style.display = 'none';
	document.getElementById('insurancePhotoDialog').style.visibility = 'hidden';
	insurancePhotoDialog.cancel();
}

function validateDuplicateMemberID() {
	var memberId = trimAll(document.getElementById('policy_no').value);
	var mrNo = document.getElementById("mrno").value;
	if (!empty(memberId)) {
		var companyId = document.getElementById('insurance_co_id').value;

		var ajaxobj = newXMLHttpRequest();
		var url = cpath + "/pages/registration/regUtils.do?_method=checkForPatientMemberId&member_id=" + memberId +
							"&companyId=" + companyId + "&mrno=" + mrNo;
		ajaxobj.open("POST", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var exists =" + ajaxobj.responseText);
					if (exists == "true") {
						showMessage("js.registration.patient.membership.id.already.exists.string");
						document.getElementById('policy_no').value = "";
						document.getElementById('policy_no').focus();
						return false;
					}else
						return true;
				}
			}
		}
	}
	return true;
}

function validate() {
	if (!empty(modAdvInsurance) && modAdvInsurance == 'Y') {
		if(!validateMemberIdRequired()) return false;
		if(!validatePolicyDetailsRequired()) return false;
		if(!validatePlanPeriodRequired()) return false;
		setOtherInsObjects();
	}

	return true;
}

////////////////////////////////////// Sponsor Type master related changes



function enableOtherInsuranceDetailsTab(){

	var tpaId;
	var membershiptabObj;
	var memberidobjhidden;
	var memberidobj;
	var policydetailsobj;
	var validitydateobjhidden;
	var policydetailshiddenobj;
	var memberidstarobj;
	var validitystartstarobj;
	var validityendstarobj;
	var policynumberstarobj;
	var policyholderstarobj;
	var relationshipobj;
	var planenddateobj;
	var planstartdateobj;
	var planenddateobjlabel;
	var planstartdateobjlabel;
	var memberIdtab;
	var onlyValidityTab;
	var validityonlydateobjhidden;
	var validityonlystartstarobj;
	var validityonlyendstarobj;
	var validityonlydateobjhidden1;
	var otherDetailsTab;

	resetHiddenFields();
	memberIdtab=document.getElementById("memberShipValidityTab");
	memberidobjhidden=document.getElementById("member_id_hidden");
	memberidobj=document.getElementById("member_id_label");
	policydetailsobj=document.getElementById("policyDetailsTab");
	validitydateobjhidden=document.getElementById("policy_validity_hidden");
	policydetailshiddenobj=document.getElementById("policy_Details_hidden");
	memberidstarobj=document.getElementById("member_id_star");
	validitystartstarobj=document.getElementById("policy_validity_start_star");
	validityendstarobj=document.getElementById("policy_validity_end_star");
	policynumberstarobj=document.getElementById("policy_number_star");
	policyholderstarobj=document.getElementById("policy_holder_name_star");
	relationshipobj=document.getElementById("patient_relationship_star");
	planstartdateobj=document.getElementById("validity_start_period_tab");
	planenddateobj=document.getElementById("validity_end_period_tab");
	planstartdateobjlabel=document.getElementById("validity_start_period_label");
	planenddateobjlabel=document.getElementById("validity_end_period_label");
	onlyValidityTab=document.getElementById("onlyValidityTab");
	validityonlydateobjhidden=document.getElementById("policy_validity_only_hidden");
	validityonlystartstarobj=document.getElementById("policy_validity_start_only_star");
	validityonlyendstarobj=document.getElementById("policy_validity_end_only_star");
	validityonlydateobjhidden1=document.getElementById("policy_validity_only_hidden1");
	otherDetailsTab=document.getElementById("OthersDetailsTab");


	var item = findInList(tpanames, "tpa_id", tpaID);
	if(null != item && item !=""){
		for(var i=0;i<sponsorTypeList.length;i++){
			if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
				if(sponsorTypeList[i].member_id_show == 'Y'){
					memberIdtab.style.display = 'block';
					planstartdateobjlabel.innerHTML="Validity Start:";
					planenddateobjlabel.innerHTML="Validity End:";
					planstartdateobj.style.visibility = 'visible';
					planenddateobj.style.visibility = 'visible';
					if(null != sponsorTypeList[i].member_id_label && !empty(sponsorTypeList[i].member_id_label))
						memberidobj.innerHTML=sponsorTypeList[i].member_id_label+":";
					if(sponsorTypeList[i].member_id_mandatory == 'Y'){
						memberidobjhidden.value = 'Y';
						if(memberidobj !=null)
							memberidstarobj.style.visibility = 'visible';
					}
				}
				if(sponsorTypeList[i].validity_period_show == 'Y'){
					if(sponsorTypeList[i].member_id_show != 'Y'){
						onlyValidityTab.style.display = 'block';
						validityonlydateobjhidden1.value="Y";
						if(sponsorTypeList[i].validity_period_mandatory == 'Y'){
							validityonlydateobjhidden.value = 'Y';
							validityonlystartstarobj.style.visibility = 'visible';
							validityonlyendstarobj.style.visibility = 'visible';
						}
					}else if(sponsorTypeList[i].member_id_show == 'Y'){
						if(sponsorTypeList[i].validity_period_mandatory == 'Y'){
							validitydateobjhidden.value = 'Y';
							if(validitystartstarobj != null)
								validitystartstarobj.style.visibility = 'visible';
							if(validityendstarobj != null)
								validityendstarobj.style.visibility = 'visible';
						}
					}
				}else{
					if(sponsorTypeList[i].member_id_show == 'Y'){
						planstartdateobj.style.visibility = 'hidden';
						planenddateobj.style.visibility = 'hidden';
						planstartdateobjlabel.innerHTML="";
						planenddateobjlabel.innerHTML="";
						validitystartstarobj.style.visibility = 'hidden';
						validityonlystartstarobj.style.visibility = 'hidden';
						validityendstarobj.style.visibility = 'hidden';
						validityonlyendstarobj.style.visibility = 'hidden';
					}else{
						memberIdtab.style.display = 'none';
						onlyValidityTab.style.display = 'none';
					}
				}
				if(sponsorTypeList[i].policy_id_show == 'Y'){
					policydetailsobj.style.display = 'block';
						if(sponsorTypeList[i].policy_id_mandatory == 'Y'){
							policydetailshiddenobj.value = 'Y';
						if(policynumberstarobj != null)
							policynumberstarobj.style.visibility = 'visible';
						if(policyholderstarobj != null)
							policyholderstarobj.style.visibility = 'visible';
						if(relationshipobj != null)
							relationshipobj.style.visibility = 'visible';
					}
				}
//				setDateObjectsForValidityPeriod();
				break;
			}
			else{
				memberidobj.innerHTML="Membership ID"+":";
				memberIdtab.style.display = 'none';
				policydetailsobj.style.display = 'none';
				validitystartstarobj.style.visibility = 'hidden';
				validityonlystartstarobj.style.visibility = 'hidden';
				validityendstarobj.style.visibility = 'hidden';
				validityonlyendstarobj.style.visibility = 'hidden';
				memberIdtab.style.display = 'none';
				memberidobjhidden.value = '';
				memberidstarobj.style.visibility = 'hidden';
				policydetailshiddenobj.value = '';
				policynumberstarobj.style.visibility = 'hidden';
				policyholderstarobj.style.visibility = 'hidden';
				relationshipobj.style.visibility = 'hidden';
				onlyValidityTab.style.display = 'none';
			}
		}
		if(item.scanned_doc_required == 'N')
			otherDetailsTab.style.display = 'none';
	}
}

function resetHiddenFields(){
	var memberidobjhidden=document.getElementById("primary_member_id_hidden");
	if (memberidobjhidden != null) memberidobjhidden.value = "";

	var validityperiodobjhidden=document.getElementById("primary_policy_validity_hidden");
	if (validityperiodobjhidden != null) validityperiodobjhidden.value = "";

	var policydetailshiddenobj=document.getElementById("primary_policy_Details_hidden");
	if (policydetailshiddenobj != null) policydetailshiddenobj.value = "";

	var validityonlydateobjhidden=document.getElementById("primary_policy_validity_only_hidden");
	if (validityonlydateobjhidden != null) validityonlydateobjhidden.value = "";

	var validityonlydateobjhidden1=document.getElementById("primary_policy_validity_only_hidden1");
	if (validityonlydateobjhidden1 != null) validityonlydateobjhidden1.value = "";

}


function validateMemberIdRequired(){
	var memberidobjhidden;
	var memberidobj;
	var memberidobjlabel;

	memberidobj = document.getElementById("policy_no");
	memberidobjhidden=document.getElementById("member_id_hidden").value;
	memberIdLabel=document.getElementById("member_id_label").innerHTML;

	memberIdLabel=memberIdLabel.replace(':','');
	if (memberidobjhidden == 'Y' && memberidobj != null && memberidobj.value == "") {
			alert(memberIdLabel +  " " +getString("js.common.is.required"));
			memberidobj.focus();
			return false;
	}
	return true;
}

function validatePlanPeriodRequired(){
	var validityperiodhidden;
	var policyStartDateObj;
	var	policyEndDateObj;
	var validityonlydateobjhidden;
	var policyStartDateObjonly;
	var	policyEndDateObjonly;
	var validityonlydateobjhidden1;

	policyStartDateObj = document.getElementById("policy_validity_start");
	policyEndDateObj = document.getElementById("policy_validity_end");
	validityperiodhidden=document.getElementById("policy_validity_hidden").value;
	validityonlydateobjhidden=document.getElementById("policy_validity_only_hidden").value;
	validityonlydateobjhidden1=document.getElementById("policy_validity_only_hidden1").value;
	policyStartDateObjonly=document.getElementById("policy_validity_start_only");
	policyEndDateObjonly=document.getElementById("policy_validity_end_only");
	memberIdValidFromLabel=document.getElementById("validity_start_period_label").innerHTML;
	memberIdValidToLabel=document.getElementById("validity_end_period_label").innerHTML;

	memberIdValidFromLabel=memberIdValidFromLabel.replace(':','');
	memberIdValidToLabel=memberIdValidToLabel.replace(':','');
	var memberIdValidFromRequiredMsg =memberIdValidFromLabel  + " " +getString("js.common.is.required");
	var memberIdValidToRequiredMsg = memberIdValidToLabel + " " +getString("js.common.is.required");

	if (validityperiodhidden == 'Y' || validityonlydateobjhidden == 'Y') {
		if (policyStartDateObj != null) {
				var	fromDt;
				var toDt

			  if(validityonlydateobjhidden == 'Y'){
				if (!validateRequired(policyStartDateObjonly, memberIdValidFromRequiredMsg)) return false;
				if (!validateRequired(policyEndDateObjonly, memberIdValidToRequiredMsg)) return false;
				fromDt = getDateFromField(policyStartDateObjonly);
				toDt = getDateFromField(policyEndDateObjonly);
			  }else {
				if (!validateRequired(policyStartDateObj, memberIdValidFromRequiredMsg)) return false;
				if (!validateRequired(policyEndDateObj, memberIdValidToRequiredMsg)) return false;
				fromDt = getDateFromField(policyStartDateObj);
				toDt = getDateFromField(policyEndDateObj);
			  }

				var dateCompareMsg = memberIdValidToLabel+" "+getString("js.common.message.cannot.be.less.than")+" "+memberIdValidFromLabel;

				if ((toDt != null) && (fromDt != null)) {
					if (fromDt > toDt) {
						alert(dateCompareMsg);
						policyEndDateObj.focus();
						return false;
					}
				}

				var memberIdValidFromFutureValidate = memberIdValidFromLabel + " " +getString("js.common.message.date.invalid.future");
				var memberIdValidToPastValidate = memberIdValidToLabel + " " +getString("js.common.message.date.invalid.past");
				var curDate = new Date();
				if (gServerNow != null) {
					curDate.setTime(gServerNow);
				}
				curDate.setHours(0);
				curDate.setMinutes(0);
				curDate.setSeconds(0);
				curDate.setMilliseconds(0);

				if (fromDt > curDate) {
					alert(memberIdValidFromFutureValidate);
					policyStartDateObj.focus();
					return false;
				}

				if (toDt < curDate) {
					alert(memberIdValidToPastValidate);
					policyEndDateObj.focus();
					return false;
				}
			 }
		}

		var item = findInList(tpanames, "tpa_id", tpaID);
		if(null != item && item !=""){
			for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
					if(sponsorTypeList[i].validity_period_show == 'Y' && sponsorTypeList[i].validity_period_mandatory != 'Y'){
						var	fromDt;
						var toDt;
							if(validityonlydateobjhidden1.value == "Y"){
								if(!empty(policyStartDateObjonly.value) && empty(policyEndDateObjonly.value))
									if (!validateRequired(policyEndDateObjonly, memberIdValidToRequiredMsg)) return false;
								if(empty(policyStartDateObjonly.value) && !empty(policyEndDateObjonly.value))
									if (!validateRequired(policyStartDateObjonly, memberIdValidFromRequiredMsg)) return false;
								fromDt = getDateFromField(policyStartDateObjonly);
								toDt = getDateFromField(policyEndDateObjonly)
							}
							else{
								if(empty(policyStartDateObj.value) && !empty(policyEndDateObj.value))
									if (!validateRequired(policyStartDateObj, memberIdValidFromRequiredMsg)) return false;
								if(!empty(policyStartDateObj.value) && empty(policyEndDateObj.value))
									if (!validateRequired(policyEndDateObj, memberIdValidToRequiredMsg)) return false;
								fromDt = getDateFromField(policyStartDateObj);
								toDt = getDateFromField(policyEndDateObj);
							}
							if(!empty(policyStartDateObj.value) && !empty(policyEndDateObj.value)){
								var dateCompareMsg = memberIdValidToLabel+" "+getString("js.common.message.cannot.be.less.than")+" "+memberIdValidFromLabel;
								if ((toDt != null) && (fromDt != null)) {
									if (fromDt > toDt) {
										alert(dateCompareMsg);
										policyEndDateObj.focus();
										return false;
									}
								}

								var memberIdValidFromFutureValidate = memberIdValidFromLabel + " " +getString("js.common.message.date.invalid.future");
								var memberIdValidToPastValidate = memberIdValidToLabel + " " +getString("js.common.message.date.invalid.past");
								var curDate = new Date();
								if (gServerNow != null) {
									curDate.setTime(gServerNow);
								}
								curDate.setHours(0);
								curDate.setMinutes(0);
								curDate.setSeconds(0);
								curDate.setMilliseconds(0);

								if (fromDt > curDate) {
									alert(memberIdValidFromFutureValidate);
									policyStartDateObj.focus();
									return false;
								}

								if (toDt < curDate) {
									alert(memberIdValidToPastValidate);
									policyEndDateObj.focus();
									return false;
								}
							}
					}
					break;
				}
			}
		}

	return true;
}

function validatePolicyDetailsRequired(){
	var policyDetailsobjhidden;
	var policynumberobj;
	var policyholderobj;
	var relationshipobj;
	var policynolabel;
	var policyholderlabel;
	var relationshiplabel;

	policyDetailsobjhidden=document.getElementById("policy_Details_hidden").value;
	policynumberobj = document.getElementById("policy_number");
	policyholderobj=document.getElementById("policy_holder_name");
	relationshipobj=document.getElementById("patient_relationship");

	if (policyDetailsobjhidden == 'Y') {
		if(null != policynumberobj  && policynumberobj.value==""){
			alert(getString("js.registration.patient.policy.number.required"));
			policynumberobj.focus();
			return false;
		}
		if(null != policyholderobj && policyholderobj.value==""){
			alert(getString("js.registration.patient.policy.holder.required"));
			policyholderobj.focus();
			return false;
		}
		if(null != relationshipobj && relationshipobj.value==""){
			alert(getString("js.registration.patient.relationship.required"));
			relationshipobj.focus();
			return false;
		}
	}
	return true;
}

function setDateObjectsForValidityPeriod(){
	var validityonlydateobjhidden1=document.getElementById("primary_policy_validity_only_hidden1").value;
	if(validityonlydateobjhidden1 == 'Y'){
		var primarystartperiod=document.getElementById("policy_validity_start");
		var primarystartperiodonly=document.getElementById("policy_validity_start_only");
		primarystartperiodonly.value=primarystartperiod.value;

		var primaryendperiod=document.getElementById("policy_validity_end");
		var primaryendperiodonly=document.getElementById("policy_validity_end_only");
		primaryendperiodonly.value=primaryendperiod.value;
	}
}



function setOtherInsObjects(){
	var validityonlydateobjhidden1=document.getElementById("policy_validity_only_hidden1").value;
	if(validityonlydateobjhidden1 == 'Y'){
		var primarystartperiod=document.getElementById("policy_validity_start");
		var primarystartperiodonly=document.getElementById("policy_validity_start_only");
		primarystartperiod.value=primarystartperiodonly.value;

		var primaryendperiod=document.getElementById("policy_validity_end");
		var primaryendperiodonly=document.getElementById("policy_validity_end_only");
		primaryendperiod.value=primaryendperiodonly.value;
	}
}
