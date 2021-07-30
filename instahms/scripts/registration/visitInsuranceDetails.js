var cachedPlanDetails = {};

function enableRightsTochangeDynamicCopay(){
	if ( (roleId == 1) || (roleId == 2) ) {
		allowCopayChange = 'A';
	}
}

function checkForVisitCopayLimit(spnsrIndex){
	if (spnsrIndex == 'P') {
		var priVisitCopayObj= getPrimaryCoPayPercentageObj();
	}
	if (spnsrIndex == 'S') {
		var secVisitCopayObj= getSecondaryCoPayPercentageObj();
	}

	if(spnsrIndex == 'P' && priVisitCopayObj != null && priVisitCopayObj.value != "" && (parseFloat(priVisitCopayObj.value)>parseFloat(100))){
		showMessage("js.registration.patient.copay.greater.alert");
		priVisitCopayObj.focus();
		return false;
	}

	if(spnsrIndex == 'S' && secVisitCopayObj != null && secVisitCopayObj.value != "" && (parseFloat(secVisitCopayObj.value)>parseFloat(100))){
		showMessage("js.registration.patient.copay.greater.alert");
		secVisitCopayObj.focus();
		return false;
	}
	return true;
}

function resetPrimaryVisitInsuranceDetails() {

	var primaryPlanLimitObj = document.getElementById("primary_plan_limit");
	if (primaryPlanLimitObj != null) primaryPlanLimitObj.value = "";

	var primaryPlanUtilizationObj = document.getElementById("primary_plan_utilization");
	if (primaryPlanUtilizationObj != null) primaryPlanUtilizationObj.value = "";

	var primaryAvailableLimitObj = document.getElementById("primary_available_limit");
	if (primaryAvailableLimitObj != null) primaryAvailableLimitObj.value = "";

	var primaryVisitLimitObj = document.getElementById("primary_visit_limit");
	if (primaryVisitLimitObj != null) primaryVisitLimitObj.value = "";

	var primaryVisitDeductibleObj = document.getElementById("primary_visit_deductible");
	if (primaryVisitDeductibleObj != null) primaryVisitDeductibleObj.value = "";

	var primaryVisitCopayObj = document.getElementById("primary_visit_copay");
	if (primaryVisitCopayObj != null) primaryVisitCopayObj.value = "";

	var primaryMaxCopayObj = document.getElementById("primary_max_copay");
	if (primaryMaxCopayObj != null) primaryMaxCopayObj.value = "";

	var primaryPerDayLimitObj = document.getElementById("primary_perday_limit");
	if (primaryPerDayLimitObj != null) primaryPerDayLimitObj.value = "";

}


function resetSecondaryVisitInsuranceDetails() {
	var secondaryPlanLimitObj = document.getElementById("secondary_plan_limit");
	if (secondaryPlanLimitObj != null) secondaryPlanLimitObj.value = "";

	var secondaryPlanUtilizationObj = document.getElementById("secondary_plan_utilization");
	if (secondaryPlanUtilizationObj != null) secondaryPlanUtilizationObj.value = "";

	var secondaryAvailableLimitObj = document.getElementById("secondary_available_limit");
	if (secondaryAvailableLimitObj != null) secondaryAvailableLimitObj.value = "";

	var secondaryVisitLimitObj = document.getElementById("secondary_visit_limit");
	if (secondaryVisitLimitObj != null) secondaryVisitLimitObj.value = "";

	var secondaryVisitDeductibleObj = document.getElementById("secondary_visit_deductible");
	if (secondaryVisitDeductibleObj != null) secondaryVisitDeductibleObj.value = "";

	var secondaryVisitCopayObj = document.getElementById("secondary_visit_copay");
	if (secondaryVisitCopayObj != null) secondaryVisitCopayObj.value = "";

	var secondaryMaxCopayObj = document.getElementById("secondary_max_copay");
	if (secondaryMaxCopayObj != null) secondaryMaxCopayObj.value = "";

	var secondaryPerDayLimitObj = document.getElementById("secondary_perday_limit");
	if (secondaryPerDayLimitObj != null) secondaryPerDayLimitObj.value = "";
}

function getPrimaryPlanLimitObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_plan_limit");
	}
	return null;
}

function getSecondaryPlanLimitObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_plan_limit");
	}
	return null;
}

function getPrimaryPlanAvailableLimitObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_available_limit");
	}
	return null;
}

function getSecondaryPlanAvailableLimitObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_available_limit");
	}
	return null;
}

function getPrimaryPlanUtilizationObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_plan_utilization");
	}
	return null;
}

function getSecondaryPlanUtilizationObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_plan_utilization");
	}
	return null;
}

function getPrimaryPlanVisitLimitObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_visit_limit");
	}
	return null;
}

function getSecondaryPlanVisitLimitObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_visit_limit");
	}
	return null;
}

function getPrimaryVisitDeductibleObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_visit_deductible");
	}
	return null;
}

function getSecondaryVisitDeductibleObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_visit_deductible");
	}
	return null;
}

function getPrimaryMaxCoPayObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_max_copay");
	}
	return null;
}

function getSecondaryMaxCoPayObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_max_copay");
	}
	return null;
}

function getPrimaryCoPayPercentageObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_visit_copay");
	}
	return null;
}

function getSecondaryCoPayPercentageObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_visit_copay");
	}
	return null;
}

function getPrimarylimitIncludeFollowupFlagObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_limits_include_followUps");
	}
	return null;
}

function getSecondarylimitIncludeFollowupFlagObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_limits_include_followUps");
	}
	return null;
}

function getPrimaryPerDayLimitObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_perday_limit");
	}
	return null;
}

function getSecondaryPerDayLimitObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_perday_limit");
	}
	return null;
}

function getPrimarySponserLimitHiddenObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_visit_sponser_limit_hidden");
	}
	return null;
}

function getSecondarySponserLimitHiddenObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_visit_sponser_limit_hidden");
	}
	return null;
}

function getPrimaryPolicyValidityOnlyStartObj(){
	var praimrySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_only_start");
	}
	return null;
}

function getPrimaryPolicyValidityOnlyEndObj(){
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_only_end");
	}
	return null;
}

function getSecondaryPolicyValidityOnlyStartObj(){
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_only_start");
	}
	return null;
}

function getSecondaryPolicyValidityOnlyEndObj(){
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_only_end");
	}
	return null;
}

function enableDisablePlanDetails(spnsrIndex,planObj){
	var planLimitObj=null;
	var planUtilizationObj=null;
	var planVisitLimitObj=null;
	var visitDeductibleObj=null;
	var maxCoPayObj=null;
	var coPayPercentageObj=null;
	var limitIncludeFollowupFlagObj=null;
	var perDayLimitObj=null;
	var sponserLimitHiddenObj=null;
	var policyValidityStartObj = null;
	var policyValidityEndObj = null;
	var dateSet;
	var previousPlanVisitDate=false;
	var memberIdObj = null;
	var policyNumberObj = null;
	var policyHolderObj = null;
	var patRelationship = null ;

	if (spnsrIndex == 'P') {
		planLimitObj=getPrimaryPlanLimitObj();
		availableLimitObj=getPrimaryPlanAvailableLimitObj();
		planUtilizationObj= getPrimaryPlanUtilizationObj();
		planVisitLimitObj= getPrimaryPlanVisitLimitObj();
		visitDeductibleObj= getPrimaryVisitDeductibleObj();
		maxCoPayObj= getPrimaryMaxCoPayObj();
		coPayPercentageObj=getPrimaryCoPayPercentageObj();
		limitIncludeFollowupFlagObj=getPrimarylimitIncludeFollowupFlagObj();
		perDayLimitObj=getPrimaryPerDayLimitObj();
		sponserLimitHiddenObj=getPrimarySponserLimitHiddenObj();
		policyValidityStartObj = getPrimaryPolicyValidityStartObj();
		policyValidityEndObj = getPrimaryPolicyValidityEndObj();
		memberIdObj = getPrimaryInsuranceMemberIdObj();
		policyNumberObj = getPrimaryInsurancePolicyNumberObj();
		policyHolderObj = getPrimaryPatientHolderObj();
		patRelationship = getPrimaryPatientRelationObj();

		if (planObj != null) {
			if (empty(planObj.value)) {
				document.getElementById("primary_plan_limit").disabled = true;
				document.getElementById("primary_plan_utilization").disabled = true;
				//document.getElementById("primary_available_limit").disabled = true;
				document.getElementById("primary_visit_limit").disabled = true;
				document.getElementById("primary_visit_deductible").disabled = true;
				document.getElementById("primary_visit_copay").disabled = true;
				document.getElementById("primary_max_copay").disabled = true;
				if(document.getElementById("primary_perday_limit")!= null){
					document.getElementById("primary_perday_limit").disabled = true;}
				document.getElementById("pd_primary_insuranceButton").disabled = true;
			} else {
				document.getElementById("primary_plan_limit").disabled = false;
				document.getElementById("primary_plan_utilization").disabled = false;
				//document.getElementById("primary_available_limit").disabled = false;
				document.getElementById("primary_visit_limit").disabled = false;
				document.getElementById("primary_visit_deductible").disabled = false;
				document.getElementById("primary_visit_copay").disabled = false;
				document.getElementById("primary_max_copay").disabled = false;
				if(document.getElementById("primary_perday_limit")!= null){
				 document.getElementById("primary_perday_limit").disabled = false;}
				document.getElementById("pd_primary_insuranceButton").disabled = false;
			}
			document.getElementById("primary_plan_limit").value = "";
			document.getElementById("primary_plan_utilization").value = "";
			document.getElementById("primary_available_limit").textContent = "";
			document.getElementById("primary_visit_limit").value = "";
			document.getElementById("primary_visit_deductible").value = "";
			document.getElementById("primary_visit_copay").value = "";
			document.getElementById("primary_max_copay").value = "";
			document.getElementById("primary_limits_include_followUps").value = "";
			document.getElementById("primary_visit_sponser_limit_hidden").value = "";
			document.getElementById("insEdited").value = "";
			if(document.getElementById("primary_perday_limit")!= null){
				 document.getElementById("primary_perday_limit").value = "";}
			document.getElementById("primary_policy_validity_start").value = "";
			document.getElementById("primary_policy_validity_end").value = "";
			dateSet=checkForDateObjectSetting(spnsrIndex);
			memberIdObj.value = '';
			policyNumberObj.value = '';
			policyHolderObj.value = '';
			patRelationship.value = '';
		}
	}else if (spnsrIndex == 'S') {
		planLimitObj=getSecondaryPlanLimitObj();
		availableLimitObj=getSecondaryPlanAvailableLimitObj();
		planUtilizationObj= getSecondaryPlanUtilizationObj();
		planVisitLimitObj= getSecondaryPlanVisitLimitObj();
		visitDeductibleObj= getSecondaryVisitDeductibleObj();
		maxCoPayObj= getSecondaryMaxCoPayObj();
		coPayPercentageObj=getSecondaryCoPayPercentageObj();
		limitIncludeFollowupFlagObj=getSecondarylimitIncludeFollowupFlagObj();
		perDayLimitObj=getSecondaryPerDayLimitObj();
		sponserLimitHiddenObj=getSecondarySponserLimitHiddenObj();
		policyValidityStartObj = getSecondaryPolicyValidityStartObj();
		policyValidityEndObj = getSecondaryPolicyValidityEndObj();
		memberIdObj = getSecondaryInsuranceMemberIdObj();
		policyNumberObj = getSecondaryInsurancePolicyNumberObj();
		policyHolderObj = getSecondaryPatientHolderObj();
		patRelationship = getSecondaryPatientRelationObj();

		if (planObj != null){
			if (empty(planObj.value)){
				document.getElementById("secondary_plan_limit").disabled = true;
				document.getElementById("secondary_plan_utilization").disabled = true;
				//document.getElementById("secondary_available_limit").disabled = true;
				document.getElementById("secondary_visit_limit").disabled = true;
				document.getElementById("secondary_visit_deductible").disabled = true;
				document.getElementById("secondary_visit_copay").disabled = true;
				document.getElementById("secondary_max_copay").disabled = true;
				if(document.getElementById("secondary_perday_limit")!= null){
				 document.getElementById("secondary_perday_limit").disabled = true;}
				document.getElementById("pd_secondary_insuranceButton").disabled = true;
			} else {
				document.getElementById("secondary_plan_limit").disabled = false;
				document.getElementById("secondary_plan_utilization").disabled = false;
				//document.getElementById("secondary_available_limit").disabled = false;
				document.getElementById("secondary_visit_limit").disabled = false;
				document.getElementById("secondary_visit_deductible").disabled = false;
				document.getElementById("secondary_visit_copay").disabled = false;
				document.getElementById("secondary_max_copay").disabled = false;
				if(document.getElementById("secondary_perday_limit")!= null){
				 document.getElementById("secondary_perday_limit").disabled = false;}
				document.getElementById("pd_secondary_insuranceButton").disabled = false;

			}
			document.getElementById("secondary_plan_limit").value = "";
			document.getElementById("secondary_plan_utilization").value = "";
			document.getElementById("secondary_available_limit").textContent = "";
			document.getElementById("secondary_visit_limit").value = "";
			document.getElementById("secondary_visit_deductible").value = "";
			document.getElementById("secondary_visit_copay").value = "";
			document.getElementById("secondary_max_copay").value = "";
			document.getElementById("secondary_limits_include_followUps").value = "";
			document.getElementById("secondary_visit_sponser_limit_hidden").value = "";
			document.getElementById("insEdited").value = "";
			if(document.getElementById("secondary_perday_limit")!= null){
				 document.getElementById("secondary_perday_limit").value = "";}
			document.getElementById("secondary_policy_validity_start").value = "";
			document.getElementById("secondary_policy_validity_end").value = "";
			dateSet=checkForDateObjectSetting(spnsrIndex);
			memberIdObj.value = '';
			policyNumberObj.value = '';
			policyHolderObj.value = '';
			patRelationship.value = '';
		}
	}
	if (planObj != null) {
		var plan =  planObj.value;
		clearHiddenFeilds(spnsrIndex);
		if (!empty(plan)) {
			var visitType = screenid == 'ip_registration'? 'i': 'o';
			if(screenid == 'Edit_Insurance')
				visitType=pvisitType;
			var op_type='';
			if (null != document.mainform.op_type){
				op_type=document.mainform.op_type.value;
			}
			getPatientInsurancePlanDetails(plan,spnsrIndex);
			var newIns=true;
			//For edit Insurance Screen and during registration (For followUp's) , get the visit limits from previous visit.
			if ((null != visitId && !empty(visitId)  && gPatientPolciyNos!=null) || (op_type == 'F' || op_type == 'D')){
				//For primary
				if (spnsrIndex == 'P') {
					if(gPatientPolciyNos[0]!=null){
						var primaryPlanDetais = gPatientPolciyNos[0];
						//Check if the selected plan name is equal to the previous visit plan, if not consider it as new insurance name
						if(primaryPlanDetais.plan_id == plan){
							var planitem=findInList(policynames, "plan_id", primaryPlanDetais.plan_id);
							if(null != limitIncludeFollowupFlagObj){
								limitIncludeFollowupFlagObj.value=planitem.limits_include_followup;
							}
							if(screenid == 'Edit_Insurance' && null != limitIncludeFollowupFlagObj && !empty(limitIncludeFollowupFlagObj.value)
						   			&& limitIncludeFollowupFlagObj.value == 'Y' && (pOpType == 'F' || pOpType == 'D')){
						   	   // disableVisitLimitFields(spnsrIndex,true);
						  	}else if(null != limitIncludeFollowupFlagObj && !empty(limitIncludeFollowupFlagObj.value) && limitIncludeFollowupFlagObj.value == 'Y' &&
						   			(op_type == 'F' || op_type == 'D')){
						   		// disableVisitLimitFields(spnsrIndex,true);
						   	}
							planLimitObj.value = primaryPlanDetais.plan_limit;
							planUtilizationObj.value=primaryPlanDetais.utilization_amount;
							availableLimitObj.textContent=planLimitObj.value;
							if(primaryPlanDetais.utilization_amount != null && primaryPlanDetais.utilization_amount != "")
									availableLimitObj.textContent=formatAmountPaise(getPaise(planLimitObj.value-planUtilizationObj.value));
							if(null != limitIncludeFollowupFlagObj && !empty(limitIncludeFollowupFlagObj.value) &&
										limitIncludeFollowupFlagObj.value == 'Y' && visitType != "i"){
								if(primaryPlanDetais.episode_limit !=null && primaryPlanDetais.episode_limit !="")
										planVisitLimitObj.value = primaryPlanDetais.episode_limit;
								if(primaryPlanDetais.episode_deductible  !=null && primaryPlanDetais.episode_deductible !="")
										visitDeductibleObj.value = primaryPlanDetais.episode_deductible;
								if(primaryPlanDetais.episode_max_copay_percentage  !=null && primaryPlanDetais.episode_max_copay_percentage !="")
										maxCoPayObj.value = primaryPlanDetais.episode_max_copay_percentage;
								if(primaryPlanDetais.episode_copay_percentage  !=null && primaryPlanDetais.episode_copay_percentage !="")
									    coPayPercentageObj.value =primaryPlanDetais.episode_copay_percentage;
							}else{
								if(primaryPlanDetais.visit_limit !=null && primaryPlanDetais.visit_limit !="")
										planVisitLimitObj.value = primaryPlanDetais.visit_limit;
								if(primaryPlanDetais.visit_deductible  !=null && primaryPlanDetais.visit_deductible !="")
										visitDeductibleObj.value =primaryPlanDetais.visit_deductible;
								if(primaryPlanDetais.visit_max_copay_percentage  !=null && primaryPlanDetais.visit_max_copay_percentage !="")
										maxCoPayObj.value = primaryPlanDetais.visit_max_copay_percentage;
								if(primaryPlanDetais.visit_copay_percentage  !=null && primaryPlanDetais.visit_copay_percentage !="")
										coPayPercentageObj.value =primaryPlanDetais.visit_copay_percentage;
							}
							if (visitType == 'i'){
									perDayLimitObj.value = primaryPlanDetais.visit_per_day_limit;
							}
							if(dateSet){
								if(primaryPlanDetais.policy_validity_end != null && primaryPlanDetais.policy_validity_end != "")
									policyValidityEndObj.value = formatDate(new Date(primaryPlanDetais.policy_validity_end), 'ddmmyyyy','-');
								if(primaryPlanDetais.policy_validity_start != null && primaryPlanDetais.policy_validity_start != "")
									policyValidityStartObj.value = formatDate(new Date(primaryPlanDetais.policy_validity_start), 'ddmmyyyy','-');
							}

							memberIdObj.value = primaryPlanDetais.member_id;
							policyNumberObj.value = primaryPlanDetais.policy_number;
							policyHolderObj.value = primaryPlanDetais.policy_holder_name;
							patRelationship.value = primaryPlanDetais.patient_relationship;
							//Flag used to check if its old insu visit limits or new.
							newIns=false;
						}else if(gPatientPolciyNos!=null && gAllPatientPolciyNos !=null){
						 	for(var x=0; x< gAllPatientPolciyNos.length; x++){
						 		if(gAllPatientPolciyNos[x].plan_id ==plan){
						 			if(gAllPatientPolciyNos[x].policy_validity_end != null && gAllPatientPolciyNos[x].policy_validity_end != "")
										policyValidityEndObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_end), 'ddmmyyyy','-');
									if(gAllPatientPolciyNos[x].policy_validity_start != null && gAllPatientPolciyNos[x].policy_validity_start != "")
										policyValidityStartObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_start), 'ddmmyyyy','-');
									memberIdObj.value = gAllPatientPolciyNos[x].member_id;
									policyNumberObj.value = gAllPatientPolciyNos[x].policy_number;
									policyHolderObj.value = gAllPatientPolciyNos[x].policy_holder_name;
									patRelationship.value = gAllPatientPolciyNos[x].patient_relationship;
									previousPlanVisitDate=true;
						 		}
						 	}
						 }
					}
				}
				//For secondary
				if (spnsrIndex == 'S') {
					if(gPatientPolciyNos[1]!=null){
						var secondaryPlanDetails = gPatientPolciyNos[1];
						//Check if the selected plan name is equal to the previous visit plan, if not consider it as new insurance name
						if(secondaryPlanDetails.plan_id == plan){
							var planitem=findInList(policynames, "plan_id", secondaryPlanDetails.plan_id);
							limitIncludeFollowupFlagObj.value=planitem.limits_include_followup;
							if(screenid == 'Edit_Insurance' && null != limitIncludeFollowupFlagObj && !empty(limitIncludeFollowupFlagObj.value)
						   			&& limitIncludeFollowupFlagObj.value == 'Y' && (pOpType == 'F' || pOpType == 'D')){
						   	   // disableVisitLimitFields(spnsrIndex,true);
						  	}else if(null != limitIncludeFollowupFlagObj && !empty(limitIncludeFollowupFlagObj.value)
						  			&& limitIncludeFollowupFlagObj.value == 'Y' && (op_type == 'F' || op_type == 'D')){
						   		//disableVisitLimitFields(spnsrIndex,true);
						   	}
							planLimitObj.value = secondaryPlanDetails.plan_limit;
							planUtilizationObj.value=secondaryPlanDetails.utilization_amount;
							availableLimitObj.textContent=planLimitObj.value;
							if(secondaryPlanDetails.utilization_amount != null && secondaryPlanDetails.utilization_amount != "")
									availableLimitObj.textContent=formatAmountPaise(getPaise(planLimitObj.value-planUtilizationObj.value));
							if(null != limitIncludeFollowupFlagObj && !empty(limitIncludeFollowupFlagObj.value) &&
									limitIncludeFollowupFlagObj.value == 'Y' && visitType != "i"){
								if(secondaryPlanDetails.episode_limit !=null && secondaryPlanDetails.episode_limit !="")
										planVisitLimitObj.value = secondaryPlanDetails.episode_limit;
								if(secondaryPlanDetails.episode_deductible  !=null && secondaryPlanDetails.episode_deductible !="")
										visitDeductibleObj.value = secondaryPlanDetails.episode_deductible;
								if(secondaryPlanDetails.episode_max_copay_percentage  !=null && secondaryPlanDetails.episode_max_copay_percentage !="")
										maxCoPayObj.value = secondaryPlanDetails.episode_max_copay_percentage;
								if(secondaryPlanDetails.episode_copay_percentage  !=null && secondaryPlanDetails.episode_copay_percentage !="")
									    coPayPercentageObj.value =secondaryPlanDetails.episode_copay_percentage;
							}else{
								if(secondaryPlanDetails.visit_limit !=null && secondaryPlanDetails.visit_limit !="")
										planVisitLimitObj.value = secondaryPlanDetails.visit_limit;
								if(secondaryPlanDetails.visit_deductible  !=null && secondaryPlanDetails.visit_deductible !="")
										visitDeductibleObj.value =secondaryPlanDetails.visit_deductible;
								if(secondaryPlanDetails.visit_max_copay_percentage  !=null && secondaryPlanDetails.visit_max_copay_percentage !="")
										maxCoPayObj.value = secondaryPlanDetails.visit_max_copay_percentage;
								if(secondaryPlanDetails.visit_copay_percentage  !=null && secondaryPlanDetails.visit_copay_percentage !="")
										coPayPercentageObj.value =secondaryPlanDetails.visit_copay_percentage;
							}
							if (visitType == 'i'){
								perDayLimitObj.value = secondaryPlanDetails.visit_per_day_limit;
							}
							if(dateSet){
								if(secondaryPlanDetails.policy_validity_end != null && secondaryPlanDetails.policy_validity_end != "")
									policyValidityEndObj.value = formatDate(new Date(secondaryPlanDetails.policy_validity_end), 'ddmmyyyy','-');
								if(secondaryPlanDetails.policy_validity_start != null && secondaryPlanDetails.policy_validity_start != "")
									policyValidityStartObj.value = formatDate(new Date(secondaryPlanDetails.policy_validity_start), 'ddmmyyyy','-');
							}
							memberIdObj.value = secondaryPlanDetails.member_id;
							policyNumberObj.value = secondaryPlanDetails.policy_number;
							policyHolderObj.value = secondaryPlanDetails.policy_holder_name;
							patRelationship.value = secondaryPlanDetails.patient_relationship;
							//Flag used to check if its old insu visit limits or new.
							newIns=false;
						}else if(gPatientPolciyNos!=null && gAllPatientPolciyNos !=null){
						 	for(var x=0; x< gAllPatientPolciyNos.length; x++){
						 		if(gAllPatientPolciyNos[x].plan_id ==plan){
						 			if(gAllPatientPolciyNos[x].policy_validity_end != null && gAllPatientPolciyNos[x].policy_validity_end != "")
										policyValidityEndObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_end), 'ddmmyyyy','-');
									if(gAllPatientPolciyNos[x].policy_validity_start != null && gAllPatientPolciyNos[x].policy_validity_start != "")
										policyValidityStartObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_start), 'ddmmyyyy','-');
									memberIdObj.value = gAllPatientPolciyNos[x].member_id;
									policyNumberObj.value = gAllPatientPolciyNos[x].policy_number;
									policyHolderObj.value = gAllPatientPolciyNos[x].policy_holder_name;
									patRelationship.value = gAllPatientPolciyNos[x].patient_relationship;
									previousPlanVisitDate=true;
						 		}
						 	}
						 }
					}
				}//secondary ends
				sponserLimitHiddenObj.value=planVisitLimitObj.value;
			}
			//For Edit insurance if user changes the insurance/plan name..
			//During registration (main visit) and also for revist, get the plan limits from plan master.
		if(newIns){
			for (var i = 0; i < policynames.length; i++) {
				if (policynames[i].plan_id == plan) {
					disableVisitLimitFields(spnsrIndex,false);
					if (visitType == 'o'){
						 if(planLimitObj != null && !empty(policynames[i].op_plan_limit)){
						 		planLimitObj.value =formatAmountPaise(getPaise(policynames[i].op_plan_limit ));
						 		availableLimitObj.textContent=formatAmountPaise(getPaise(policynames[i].op_plan_limit));
						 		if(planUtilizationObj!=null && !empty(planUtilizationObj.value))
						 			availableLimitObj.textContent=formatAmountPaise(getPaise(planLimitObj.value-planUtilizationObj.value));
						 }
						 if((availableLimitObj !=null && !empty(availableLimitObj.value)) &&
						 	(policynames[i].op_visit_limit !=null && policynames[i].op_visit_limit != "")){
						 		planVisitLimitObj.value=formatAmountPaise(getPaise(Math.min(availableLimitObj.value,formatAmountPaise(getPaise(policynames[i].op_visit_limit)))));}
						 else{
						 		if(policynames[i].op_visit_limit !=null && policynames[i].op_visit_limit != "")
						 			planVisitLimitObj.value=formatAmountPaise(getPaise(policynames[i].op_visit_limit));
						 }
						 if(visitDeductibleObj != null && !empty(policynames[i].op_visit_deductible))
							 	visitDeductibleObj.value=formatAmountPaise(getPaise(policynames[i].op_visit_deductible));
						 if(maxCoPayObj != null && !empty(policynames[i].op_visit_copay_limit))
						 		maxCoPayObj.value=formatAmountPaise(getPaise(policynames[i].op_visit_copay_limit));
						 if(coPayPercentageObj != null && !empty(policynames[i].op_copay_percent))
						        coPayPercentageObj.value=policynames[i].op_copay_percent;
						 if(null != limitIncludeFollowupFlagObj){
							 limitIncludeFollowupFlagObj.value=policynames[i].limits_include_followup; 
						 }
						 if(policynames[i].limits_include_followup=="Y"){
						 	if((availableLimitObj !=null && !empty(availableLimitObj.value)) &&
						 	(policynames[i].op_episode_limit !=null && policynames[i].op_episode_limit != "")){
						 		planVisitLimitObj.value=formatAmountPaise(getPaise(Math.min(availableLimitObj.value,formatAmountPaise(getPaise(policynames[i].op_episode_limit)))));}
						 	else{
						 		if(policynames[i].op_episode_limit !=null && policynames[i].op_episode_limit != "")
						 			planVisitLimitObj.value=formatAmountPaise(getPaise(policynames[i].op_episode_limit));
						 	}
						 }
					}
					if (visitType == 'i'){
						 if(planLimitObj != null && !empty(policynames[i].ip_plan_limit)){
						 		planLimitObj.value =formatAmountPaise(getPaise(policynames[i].ip_plan_limit ));
						 		availableLimitObj.textContent=formatAmountPaise(getPaise(policynames[i].ip_plan_limit));
						 		if(planUtilizationObj!=null && !empty(planUtilizationObj.value))
						 		    availableLimitObj.textContent=formatAmountPaise(getPaise((planLimitObj.value-planUtilizationObj.value)));
						 }
						 if((availableLimitObj !=null && !empty(availableLimitObj.value)) &&
						 	(policynames[i].ip_visit_limit !=null && policynames[i].ip_visit_limit != "")){
						 		planVisitLimitObj.value=formatAmountPaise(getPaise(Math.min(availableLimitObj.value,formatAmountPaise(getPaise(policynames[i].ip_visit_limit )))));}
						 else{
								if(policynames[i].ip_visit_limit !=null && policynames[i].ip_visit_limit != "")
									planVisitLimitObj.value=formatAmountPaise(getPaise(policynames[i].ip_visit_limit ));
						 }
						 if(visitDeductibleObj != null && !empty(policynames[i].ip_visit_deductible))
						 		visitDeductibleObj.value=formatAmountPaise(getPaise(policynames[i].ip_visit_deductible ));
						 if(maxCoPayObj != null && !empty(policynames[i].ip_visit_copay_limit))
						 		maxCoPayObj.value=formatAmountPaise(getPaise(policynames[i].ip_visit_copay_limit));
						 if(coPayPercentageObj != null && !empty(policynames[i].ip_copay_percent))
						 		coPayPercentageObj.value=policynames[i].ip_copay_percent;
						 if(perDayLimitObj != null && !empty(policynames[i].ip_per_day_limit))
						 		perDayLimitObj.value=formatAmountPaise(getPaise(policynames[i].ip_per_day_limit));
					}
					if(dateSet){
						if(gPatientPolciyNos!=null && spnsrIndex == 'P' && gPatientPolciyNos[0]!=null && gPatientPolciyNos[0].plan_id == plan){
								var primaryPlanDetais = gPatientPolciyNos[0];
								if(primaryPlanDetais.policy_validity_end != null && primaryPlanDetais.policy_validity_end != "")
									policyValidityEndObj.value = formatDate(new Date(primaryPlanDetais.policy_validity_end), 'ddmmyyyy','-');
								if(primaryPlanDetais.policy_validity_start != null && primaryPlanDetais.policy_validity_start != "")
									policyValidityStartObj.value = formatDate(new Date(primaryPlanDetais.policy_validity_start), 'ddmmyyyy','-');
						 }
						 else if(gPatientPolciyNos!=null && spnsrIndex == 'S' && gPatientPolciyNos[1]!=null && gPatientPolciyNos[1].plan_id == plan){
								var secondaryPlanDetais = gPatientPolciyNos[1];
								if(secondaryPlanDetais.policy_validity_end != null && secondaryPlanDetais.policy_validity_end != "")
									policyValidityEndObj.value = formatDate(new Date(secondaryPlanDetais.policy_validity_end), 'ddmmyyyy','-');
								if(secondaryPlanDetais.policy_validity_start != null && secondaryPlanDetais.policy_validity_start != "")
									policyValidityStartObj.value = formatDate(new Date(secondaryPlanDetais.policy_validity_start), 'ddmmyyyy','-');
						 }
						 else if(gPatientPolciyNos!=null && gAllPatientPolciyNos !=null){
						 		for(var x=0; x< gAllPatientPolciyNos.length; x++){
						 			if(gAllPatientPolciyNos[x].plan_id ==plan){
						 				if(gAllPatientPolciyNos[x].policy_validity_end != null && gAllPatientPolciyNos[x].policy_validity_end != "")
											policyValidityEndObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_end), 'ddmmyyyy','-');
										if(gAllPatientPolciyNos[x].policy_validity_start != null && gAllPatientPolciyNos[x].policy_validity_start != "")
											policyValidityStartObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_start), 'ddmmyyyy','-');
										memberIdObj.value = gAllPatientPolciyNos[x].member_id;
										policyNumberObj.value = gAllPatientPolciyNos[x].policy_number;
										policyHolderObj.value = gAllPatientPolciyNos[x].policy_holder_name;
										patRelationship.value = gAllPatientPolciyNos[x].patient_relationship;
										previousPlanVisitDate=true;
						 			}
						 		}
						 }
						else{ //if(gPatientPolciyNos == null || gPatientPolciyNos[i].plan_id != plan){
							if(policynames[i].insurance_validity_end_date != null && policynames[i].insurance_validity_end_date != "")
								policyValidityEndObj.value = formatDate(new Date(policynames[i].insurance_validity_end_date), 'ddmmyyyy','-');
							if(policynames[i].insurance_validity_start_date != null && policynames[i].insurance_validity_start_date != "")
						    	policyValidityStartObj.value =formatDate(new Date(policynames[i].insurance_validity_start_date), 'ddmmyyyy','-');
						 }

						  if(!previousPlanVisitDate){
						 	if(policynames[i].insurance_validity_end_date != null && policynames[i].insurance_validity_end_date != "")
								policyValidityEndObj.value = formatDate(new Date(policynames[i].insurance_validity_end_date), 'ddmmyyyy','-');
							if(policynames[i].insurance_validity_start_date != null && policynames[i].insurance_validity_start_date != "")
						    	policyValidityStartObj.value =formatDate(new Date(policynames[i].insurance_validity_start_date), 'ddmmyyyy','-');
						 }
					}
					if(gPatientPolciyNos!=null && gAllPatientPolciyNos !=null){
						 for(var x=0; x< gAllPatientPolciyNos.length; x++){
						 	if(gAllPatientPolciyNos[x].plan_id ==plan){
						 		if(gAllPatientPolciyNos[x].policy_validity_end != null && gAllPatientPolciyNos[x].policy_validity_end != "")
									policyValidityEndObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_end), 'ddmmyyyy','-');
								if(gAllPatientPolciyNos[x].policy_validity_start != null && gAllPatientPolciyNos[x].policy_validity_start != "")
									policyValidityStartObj.value = formatDate(new Date(gAllPatientPolciyNos[x].policy_validity_start), 'ddmmyyyy','-');
								memberIdObj.value = gAllPatientPolciyNos[x].member_id;
								policyNumberObj.value = gAllPatientPolciyNos[x].policy_number;
								policyHolderObj.value = gAllPatientPolciyNos[x].policy_holder_name;
								patRelationship.value = gAllPatientPolciyNos[x].patient_relationship;
								previousPlanVisitDate=true;
						 	}
						 }
					}
					sponserLimitHiddenObj.value=planVisitLimitObj.value;
					break;
				}
			}
		  }
		   setDateObjectsForValidityPeriod();
		} else {
			clearHiddenFeilds(spnsrIndex);
		}
	}
}

function onChangeutilizationLimit(obj,type){
	  var planUtilizationLimit=obj.value;
	  var availableLimit=getPaise(0);
	  var planLimit;
	  var sponserLimitHiddenObj;
	  var visitLimit;
	  if(type=='P'){
	  	planLimit=formatAmountPaise(getPaise(document.getElementById('primary_plan_limit').value));
	  	visitLimit=formatAmountPaise(getPaise(document.getElementById('primary_visit_limit').value));
	  	sponserLimitHiddenObj=formatAmountPaise(getPaise(document.getElementById('primary_visit_sponser_limit_hidden').value));
	  	if(parseInt(planLimit)>parseInt(planUtilizationLimit)){
	  		availableLimit=formatAmountPaise(getPaise(planLimit-planUtilizationLimit));
	  	}
	  	document.getElementById('primary_available_limit').textContent =formatAmountPaise(getPaise(availableLimit));
	  	if(Math.round(sponserLimitHiddenObj * 100) == Math.round(0 * 100))
	  		document.getElementById('primary_visit_limit').value =formatAmountPaise(getPaise(availableLimit));
	  	else
	  		document.getElementById('primary_visit_limit').value =formatAmountPaise(getPaise(Math.min(availableLimit,sponserLimitHiddenObj)));
	  	document.getElementById('primary_plan_utilization').value=formatAmountPaise(getPaise(planUtilizationLimit));

	  	if(parseInt(planUtilizationLimit)>parseInt(document.getElementById('primary_plan_limit').value) ||
	  			parseInt(planUtilizationLimit)==parseInt(document.getElementById('primary_plan_limit').value))
	  		showMessage("js.registration.patient.planUtilization.greater.alert");
	  }
	  else if(type=='S'){
	  	planLimit=formatAmountPaise(getPaise(document.getElementById('secondary_plan_limit').value));
	  	visitLimit=formatAmountPaise(getPaise(document.getElementById('secondary_visit_limit').value));
	  	sponserLimitHiddenObj=formatAmountPaise(getPaise(document.getElementById('secondary_visit_sponser_limit_hidden').value));
	  	if(parseInt(planLimit)>parseInt(planUtilizationLimit)){
	  	 	availableLimit=formatAmountPaise(getPaise(planLimit-planUtilizationLimit));
	  	}
	  	document.getElementById('secondary_available_limit').textContent =formatAmountPaise(getPaise(availableLimit));
	  	if(Math.round(sponserLimitHiddenObj * 100) == Math.round(0 * 100))
	  		document.getElementById('secondary_visit_limit').value =formatAmountPaise(getPaise(availableLimit));
	  	else
	  		document.getElementById('secondary_visit_limit').value =formatAmountPaise(getPaise(Math.min(availableLimit,sponserLimitHiddenObj)));
	  	document.getElementById('secondary_plan_utilization').value=formatAmountPaise(getPaise(planUtilizationLimit));
	  	if(parseInt(planUtilizationLimit)>parseInt(document.getElementById('secondary_plan_limit').value)||
	  			parseInt(planUtilizationLimit)==parseInt(document.getElementById('primary_plan_limit').value))
	  		showMessage("js.registration.patient.planUtilization.greater.alert");
	  }
	  document.getElementById("visitLimitsChanged").value="Y";
}

function onChangeAvaliableLimit(obj,type){
	  var availableLimit=obj.value;
	  var visitLimit;
	  if(type=='P'){
	  	document.getElementById('primary_available_limit').value =formatAmountPaise(getPaise(availableLimit));
	  	visitLimit=formatAmountPaise(getPaise(document.getElementById('primary_visit_limit').value));
	  	document.getElementById('primary_visit_limit').value =formatAmountPaise(getPaise(Math.min(availableLimit,visitLimit)));
	  }
	  else if(type=='S'){
	  	document.getElementById('secondary_available_limit').value =formatAmountPaise(getPaise(availableLimit));
	  	visitLimit=formatAmountPaise(getPaise(document.getElementById('secondary_visit_limit').value));
	  	document.getElementById('secondary_visit_limit').value =formatAmountPaise(getPaise(Math.min(availableLimit,visitLimit)));
	  }
	  document.getElementById("visitLimitsChanged").value="Y";
}

function onPlanLimitChange(obj,type){
	var planlimit=obj.value;
	var availableLimit=getPaise(0);
	var utilizationlimit;
	var visitlimit;
	if(type=='P'){
		document.getElementById('primary_plan_limit').value =formatAmountPaise(getPaise(planlimit));
		utilizationlimit=formatAmountPaise(getPaise(document.getElementById('primary_plan_utilization').value));
		visitlimit=document.getElementById('primary_visit_limit').value;
		if(parseInt(planlimit)>parseInt(utilizationlimit)){
			availableLimit=formatAmountPaise(getPaise(planlimit-utilizationlimit));
	  	}
	  	document.getElementById('primary_available_limit').textContent =formatAmountPaise(getPaise(availableLimit));
	  	if(!empty(planlimit)){
	  		if(empty(visitlimit))
				document.getElementById('primary_visit_limit').value = availableLimit;
			else
				document.getElementById('primary_visit_limit').value=formatAmountPaise(getPaise(Math.min(availableLimit,visitlimit)));
		}else{
			document.getElementById('primary_visit_limit').value = document.getElementById('primary_visit_sponser_limit_hidden').value;
		}
	}
	else if(type=='S'){
		document.getElementById('secondary_plan_limit').value =formatAmountPaise(getPaise(planlimit));
		utilizationlimit=formatAmountPaise(getPaise(document.getElementById('secondary_plan_utilization').value));
		visitlimit=document.getElementById('secondary_visit_limit').value;
		if(parseInt(planlimit)>parseInt(utilizationlimit)){
			availableLimit=formatAmountPaise(getPaise(planlimit-utilizationlimit));
	  	}
	  	document.getElementById('secondary_available_limit').textContent =formatAmountPaise(getPaise(availableLimit));
	  	if(!empty(planlimit)){
	  		if(empty(visitlimit))
				document.getElementById('secondary_visit_limit').value = availableLimit;
			else
				document.getElementById('secondary_visit_limit').value=formatAmountPaise(getPaise(Math.min(availableLimit,visitlimit)));
		}else{
			document.getElementById('secondary_visit_limit').value = document.getElementById('secondary_visit_sponser_limit_hidden').value;
		}
	}
	document.getElementById("visitLimitsChanged").value="Y";
}

var insurancePlanDetailsDialog;

function initInsurancePlanDetailsDialog() {
	document.getElementById('insurancePlanDetailsDialog').style.display = 'block';
    insurancePlanDetailsDialog = new YAHOO.widget.Dialog('insurancePlanDetailsDialog', {
    	context:["insurancePlanDetailsDialog","tr","br"],
        visible: false,
        modal: true,
        constraintoviewport: true,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handleInsurancePlanDetailsDialogCancel,
                                               scope:insurancePlanDetailsDialog,
                                               correctScope:true } );
	insurancePlanDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	insurancePlanDetailsDialog.cancelEvent.subscribe(handleInsurancePlanDetailsDialogCancel);
    insurancePlanDetailsDialog.render();
}

function handleInsurancePlanDetailsDialogCancel(){
	 insurancePlanDetailsDialog.hide();
}


function showInsurancePlanDetailsDialog(planName) {
	var button = null;
	clearInsurancePlanDetailsFields();
	if (planName == "primary")
		button = document.getElementById('pd_primary_insuranceButton');
	else if (planName == "secondary")
		button = document.getElementById('pd_secondary_insuranceButton');

	if (button != null) {
		insurancePlanDetailsDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		showPatientInsurancePlanDetailsDialog(planName);
		insurancePlanDetailsDialog.show();
	}
}

function getPatientInsurancePlanDetails(planId,sponserType) {
	if (!empty(planId)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url='';
		var op_type='';
		var mrno='';
		var visittype='';
		var mainVisitId='';

		if (null != document.mainform.op_type){
			op_type = document.mainform.op_type.value;
		}
		if(screenid !="Edit_Insurance"){
			if (null != document.mrnoform.mrno){
				mrno = document.mrnoform.mrno.value;
			}
		}
		if(screenid != "Edit_Insurance" && null != visitType && !empty(visitType))
				visittype=visitType;
		if (null != document.mainform.main_visit_id){
				mainVisitId=document.mainform.main_visit_id.value;
		}

		if(null != visitId && !empty(visitId)){//edit Insurance screen
			url =cpath+"/editVisit/changeTPA.do?_method=getInsurancePlanDetails";
			url = url + "&plan_id=" + planId +"&visitType=" + visitType +"&visitId="+visitId;
		}else if(screenid == "ip_registration"){//For IP registration screen
			url = "./IpRegistration.do?_method=getInsurancePlanDetails";
			url = url + "&plan_id=" + planId +"&visitType=i" +"&visitId="+visitId;
		}else if(screenid == "out_pat_reg" ){// For OSP screen
			url = "./outPatientRegistration.do?_method=getInsurancePlanDetails";
			url = url + "&plan_id=" + planId +"&visitType=" + visitType +"&visitId="+visitId;
		}
		
		if (cachedPlanDetails.hasOwnProperty(planId + visitType + visitId)) {
			return handleAjaxResponseForInsuranceDetails(cachedPlanDetails[planId + visitType + visitId], planId, sponserType);
		}
		cachedPlanDetails[planId + visitType + visitId] = null;
		
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				cachedPlanDetails[planId + visitType + visitId] = reqObject.responseText;
				return handleAjaxResponseForInsuranceDetails(reqObject.responseText,planId,sponserType);
			}
		}
	}
}

function handleAjaxResponseForInsuranceDetails(responseText,planId,sponserType) {
	var planTD;
	if(sponserType=="P")
		planTD = document.getElementById("primaryPlanTD");
	else if(sponserType=="S")
		planTD = document.getElementById("secondaryPlanTD");

	if (responseText != null) {
		eval("var insPlanDetails = " + responseText);
		if (insPlanDetails != null && insPlanDetails.length > 0) {
			for (var i=0; i<insPlanDetails.length; i++) {
				var disableAttr='';

				/*for(var j=0;j<itemCatlist.length;j++){
					if(insPlanDetails[i].insurance_category_id==itemCatlist[j].insurance_category_id){
						disableAttr=itemCatlist[j].insurance_payable;
					}
				}*/

				disableAttr = insPlanDetails[i].category_payable;

				var inp0 = document.createElement("INPUT");
			    inp0.setAttribute("type", "hidden");
			    inp0.setAttribute("id", sponserType+"_cat_id" + i);
			    inp0.setAttribute("name", sponserType+"_cat_id");
			    inp0.setAttribute("value",insPlanDetails[i].insurance_category_id);
			    document.getElementsByName("cat_id").length

				var inp1 = document.createElement("INPUT");
			    inp1.setAttribute("type", "hidden");
			    inp1.setAttribute("id", sponserType+"_cat_name" + i);
			    inp1.setAttribute("name", sponserType+"_cat_name");
			    inp1.setAttribute("value", insPlanDetails[i].insurance_category_name);

				var inp2 = document.createElement("INPUT");
			    inp2.setAttribute("type", "hidden");
			    inp2.setAttribute("id", sponserType+"_sponser_limit" + i);
			    inp2.setAttribute("name", sponserType+"_sponser_limit");
			    inp2.setAttribute("value", null!=insPlanDetails[i].per_treatment_limit && insPlanDetails[i].per_treatment_limit!=""? insPlanDetails[i].per_treatment_limit:'');

				var inp3 = document.createElement("INPUT");
			    inp3.setAttribute("type", "hidden");
			    inp3.setAttribute("id", sponserType+"_cat_deductible" + i);
			    inp3.setAttribute("name", sponserType+"_cat_deductible");
			    inp3.setAttribute("value",null!=insPlanDetails[i].patient_amount_per_category && insPlanDetails[i].patient_amount_per_category!=""? insPlanDetails[i].patient_amount_per_category:'');

				var inp4 = document.createElement("INPUT");
			    inp4.setAttribute("type", "hidden");
			    inp4.setAttribute("id", sponserType+"_item_deductible" + i);
			    inp4.setAttribute("name", sponserType+"_item_deductible");
			    inp4.setAttribute("value", null!=insPlanDetails[i].patient_amount && insPlanDetails[i].patient_amount!="" ?insPlanDetails[i].patient_amount:'');

				var inp5 = document.createElement("INPUT");
			    inp5.setAttribute("type", "hidden");
			    inp5.setAttribute("id", sponserType+"_copay_percent" + i);
			    inp5.setAttribute("name", sponserType+"_copay_percent");
			    inp5.setAttribute("value", null!=insPlanDetails[i].patient_percent && insPlanDetails[i].patient_percent!="" ?insPlanDetails[i].patient_percent:'');

				var inp6 = document.createElement("INPUT");
			    inp6.setAttribute("type", "hidden");
			    inp6.setAttribute("id", sponserType+"_max_copay" + i);
			    inp6.setAttribute("name", sponserType+"_max_copay");
			    inp6.setAttribute("value", null!=insPlanDetails[i].patient_amount_cap && insPlanDetails[i].patient_amount_cap!="" ?insPlanDetails[i].patient_amount_cap:'');

			    var inp7 = document.createElement("INPUT");
			    inp7.setAttribute("type", "hidden");
			    inp7.setAttribute("id", sponserType+"_ins_payable" + i);
			    inp7.setAttribute("name", sponserType+"_ins_payable");
			    inp7.setAttribute("value",disableAttr);

			    var inp8 = document.createElement("INPUT");
			    inp8.setAttribute("type", "hidden");
			    inp8.setAttribute("id", sponserType+"_system_category" + i);
			    inp8.setAttribute("name", sponserType+"_system_category");
			    inp8.setAttribute("value", insPlanDetails[i].system_category);
			    
				planTD.appendChild(inp0);
			    planTD.appendChild(inp1);
			    planTD.appendChild(inp2);
			    planTD.appendChild(inp3);
			    planTD.appendChild(inp4);
			    planTD.appendChild(inp5);
			    planTD.appendChild(inp6);
			    planTD.appendChild(inp7);
			    planTD.appendChild(inp8);
			}
		}
	}
}

var sponserRef="";
function showPatientInsurancePlanDetailsDialog(sponserType) {
	var elements;
	var type='';

	if(sponserType == "primary"){
		elements=document.getElementsByName("P_cat_id");
		type="P";
		sponserRef="P";
	}
	else if (sponserType == "secondary"){
		elements=document.getElementsByName("S_cat_id");
		type="S";
		sponserRef="S";
	}

	for (var i=0; i<elements.length; i++) {

		var LTable = document.getElementById('pd_insDialogTable1');
   		var len = LTable.rows.length;
		var templateRow = LTable.rows[len-1];
   		var row = '';
   		row = templateRow.cloneNode(true);
   		var sysCat = document.getElementById(type+"_system_category"+i).value;
   		var insCatID = document.getElementById(type+"_cat_id"+i).value;
   		if(sysCat == 'Y'  && insCatID != -2) {
	   		row.style.display = 'none';
   		} else {
   			row.style.display = '';
   		}
   		YAHOO.util.Dom.insertBefore(row, templateRow);


		var cell1 = document.createElement("TD");
		cell1.setAttribute("style", "width: 60px");
		cell1.setAttribute("class", 'border');
		cell1.setAttribute("cellspacing","10");
		cell1.setAttribute("cellpadding","5");
		var value=type+"_cat_name"+i;
		var txtEl = document.createTextNode(document.getElementById(value).value);
		var inp1 = document.createElement("label");
		inp1.appendChild(txtEl);
		cell1.appendChild(inp1);

		var cell2 = document.createElement("TD");
		cell2.setAttribute("class", 'border');
		cell2.setAttribute("style", "width: 60px");
		cell2.setAttribute("cellspacing","10");
		cell2.setAttribute("cellpadding","5");
		var inp2 = document.createElement("INPUT");
		inp2.setAttribute("type", "text");
		inp2.setAttribute("name", type+"_esponser_limit");
		inp2.setAttribute("class","numeric");
		inp2.setAttribute("size", "8");
		var inspayablevalue=type+"_ins_payable"+i;
		if(document.getElementById(inspayablevalue).value=='N' || allowCopayChange != 'A')
				inp2.setAttribute("disabled", true);
		inp2.setAttribute("id", type+"_esponser_limit" + i);
		var value=type+"_sponser_limit"+i;
		inp2.setAttribute("value", document.getElementById(value).value);
		inp2.setAttribute("style","width:60px");
		inp2.setAttribute("onkeypress", "return enterNumOnly(event)");
		inp2.setAttribute("onchange", "onChangeUpdateInsuEditFlag(this)");
		cell2.appendChild(inp2);

		var cell3 = document.createElement("TD");
		cell3.setAttribute("class", 'border');
		cell3.setAttribute("style", "width: 60px");
		cell3.setAttribute("cellspacing","10");
		cell3.setAttribute("cellpadding","5");
		var inp3 = document.createElement("INPUT");
		inp3.setAttribute("type", "text");
		inp3.setAttribute("name", type+"_ecat_deductible");
		inp3.setAttribute("class","numeric");
		inp3.setAttribute("size", "8");
		var inspayablevalue=type+"_ins_payable"+i;
		inp3.setAttribute("id", type+"_ecat_deductible" + i);
		var value=type+"_cat_deductible"+i;
		inp3.setAttribute("value", document.getElementById(value).value);
		if(document.getElementById(inspayablevalue).value=='N' || allowCopayChange != 'A')
				inp3.setAttribute("disabled", true);
		inp3.setAttribute("style","width:60px");
		inp3.setAttribute("onkeypress", "return enterNumOnly(event)");
		inp3.setAttribute("onchange", "onChangeUpdateInsuEditFlag(this)");
		cell3.appendChild(inp3);

		var cell4 = document.createElement("TD");
		cell4.setAttribute("class", 'border');
		cell4.setAttribute("style", "width: 60px");
		cell4.setAttribute("cellspacing","10");
		cell4.setAttribute("cellpadding","5");
		var inp4 = document.createElement("INPUT");
		inp4.setAttribute("type", "text");
		inp4.setAttribute("name", type+"_eitem_deductible");
		inp4.setAttribute("class","numeric");
		inp4.setAttribute("size", "8");
		var inspayablevalue=type+"_ins_payable"+i;
		inp4.setAttribute("id", type+"_eitem_deductible" + i);
		var value=type+"_item_deductible"+i;
		inp4.setAttribute("value", document.getElementById(value).value);
		if(document.getElementById(inspayablevalue).value=='N' || allowCopayChange != 'A')
			inp4.setAttribute("disabled", true);
		inp4.setAttribute("style","width:60px");
		inp4.setAttribute("onkeypress", "return enterNumOnly(event)");
		inp4.setAttribute("onchange", "onChangeUpdateInsuEditFlag(this)");
		cell4.appendChild(inp4);

		var cell5 = document.createElement("TD");
		cell5.setAttribute("class", 'border');
		cell5.setAttribute("style", "width: 60px");
		cell5.setAttribute("cellspacing","10");
		cell5.setAttribute("cellpadding","5");
		var inp5 = document.createElement("INPUT");
		inp5.setAttribute("type", "text");
		inp5.setAttribute("name", type+"_ecopay_percent");
		inp5.setAttribute("class","numeric");
		inp5.setAttribute("size", "8");
		var inspayablevalue=type+"_ins_payable"+i;
		inp5.setAttribute("id", type+"_ecopay_percent" + i);
		var value=type+"_copay_percent"+i;
		inp5.setAttribute("value", document.getElementById(value).value);
		if(document.getElementById(inspayablevalue).value=='N' || allowCopayChange != 'A')
			inp5.setAttribute("disabled", true);
		inp5.setAttribute("style","width:60px");
		inp5.setAttribute("onkeypress", "return enterNumOnly(event)");
		inp5.setAttribute("onchange", "return onCopayPercentageChange(this,'"+type+"');onChangeUpdateInsuEditFlag(this);");
		cell5.appendChild(inp5);

		var cell6 = document.createElement("TD");
		cell6.setAttribute("class", 'border');
		cell6.setAttribute("style", "width: 60px");
		cell6.setAttribute("cellspacing","10");
		cell6.setAttribute("cellpadding","5");
		var inp6 = document.createElement("INPUT");
		inp6.setAttribute("type", "text");
		inp6.setAttribute("name", type+"_emax_copay");
		inp6.setAttribute("class","numeric");
		inp6.setAttribute("size", "8");
		var inspayablevalue=type+"_ins_payable"+i;
		if(document.getElementById(inspayablevalue).value=='N' || allowCopayChange != 'A')
			inp6.setAttribute("disabled", true);
		inp6.setAttribute("id", type+"_emax_copay" + i);
		var value=type+"_max_copay"+i;
		inp6.setAttribute("value", document.getElementById(value).value);
		inp6.setAttribute("style","width:60px");
		inp6.setAttribute("onkeypress", "return enterNumOnly(event)");
		inp6.setAttribute("onchange", "onChangeUpdateInsuEditFlag(this)");
		cell6.appendChild(inp6);

		var cell7 = document.createElement("TD");
		var inp7 = document.createElement("INPUT");
		inp7.setAttribute("type", "hidden");
		inp7.setAttribute("name", type+"_ecat_id");
		inp7.setAttribute("class","numeric");
		inp7.setAttribute("size", "8");
		var value=type+"_cat_id"+i;
		inp7.setAttribute("id", type+"_ecat_id" + i);
		inp7.setAttribute("value", document.getElementById(value).value);
		cell7.appendChild(inp7);

		row.appendChild(cell1);
		row.appendChild(cell2);
		row.appendChild(cell3);
		row.appendChild(cell4);
		row.appendChild(cell5);
		row.appendChild(cell6);
		row.appendChild(cell7);
	}
}

function clearInsurancePlanDetailsFields() {
	var table = document.getElementById("pd_insDialogTable1");
    for(var i = table.rows.length-2 ; i >= 0; i--)
    {
   	 table.deleteRow(i);
    }
}

function clearHiddenFeilds(spnsrIndex){
  		var loop_through=0;
		if(spnsrIndex == "P"){
			loop_through=document.getElementsByName("P_cat_id").length;
			type="P";
			d = document.getElementById("primaryPlanTD");
		}
		else if (spnsrIndex == "S"){
			loop_through=document.getElementsByName("S_cat_id").length;
			type="S";
			d = document.getElementById("secondaryPlanTD");
		}
		for (var i=0; i<loop_through; i++) {
			var element =document.getElementById(type+"_cat_id"+i);
			d.removeChild(element);
			var element1 =document.getElementById(type+"_cat_name"+i);
			d.removeChild(element1);
			var element2 =document.getElementById(type+"_sponser_limit"+i);
			d.removeChild(element2);
			var element3 =document.getElementById(type+"_cat_deductible"+i);
			d.removeChild(element3);
			var element4 =document.getElementById(type+"_item_deductible"+i);
			d.removeChild(element4);
			var element5 =document.getElementById(type+"_copay_percent"+i);
			d.removeChild(element5);
			var element6 =document.getElementById(type+"_max_copay"+i);
			d.removeChild(element6);
			var element7 =document.getElementById(type+"_ins_payable"+i);
			d.removeChild(element7);
			var element8 =document.getElementById(type+"_system_category"+i);
			d.removeChild(element8);
		}
}
function savePatientInsPlanDetails(){

	if(!validatedetails()) return false;

	if(document.getElementById('insEdited').value =="true"){
		if(sponserRef == "P"){
			elements=document.getElementsByName("P_cat_id");
			type="P";
		}
		else if (sponserRef == "S"){
			elements=document.getElementsByName("S_cat_id");
			type="S";
		}
		for (var i=0; i<elements.length; i++) {

			document.getElementById(type+"_cat_id"+i).value=document.getElementById(type+"_ecat_id"+i).value;
			document.getElementById(type+"_sponser_limit"+i).value=document.getElementById(type+"_esponser_limit"+i).value;
			document.getElementById(type+"_cat_deductible"+i).value=document.getElementById(type+"_ecat_deductible"+i).value;
			document.getElementById(type+"_item_deductible"+i).value=document.getElementById(type+"_eitem_deductible"+i).value;
			document.getElementById(type+"_copay_percent"+i).value=document.getElementById(type+"_ecopay_percent"+i).value;
			document.getElementById(type+"_max_copay"+i).value=document.getElementById(type+"_emax_copay"+i).value;

		}
	}
	insurancePlanDetailsDialog.cancel();
}

function onChangeUpdateInsuEditFlag(obj){
	document.getElementById('insEdited').value ="true";
}

function onVisitLimitChange(obj,type){
	var visitlimit=obj.value;
	var availablelimit;
	if(type=='P'){
		if(document.getElementById('primary_available_limit').textContent != ''){
			availablelimit=formatAmountPaise(getPaise(document.getElementById('primary_available_limit').textContent)) ;
			document.getElementById('primary_visit_limit').value =formatAmountPaise(getPaise(visitlimit));
			if(parseInt(visitlimit)>parseInt(availablelimit))
				showMessage("js.registration.patient.visitlimit.greater.alert");
		}
	}
	else if(type=='S'){
		if(document.getElementById('secondary_available_limit').textContent != ''){
			availablelimit=formatAmountPaise(getPaise(document.getElementById('secondary_available_limit').textContent)) ;
		 	document.getElementById('secondary_visit_limit').value =formatAmountPaise(getPaise(visitlimit));
		 	if(parseInt(visitlimit)>parseInt(availablelimit))
				showMessage("js.registration.patient.visitlimit.greater.alert");
		}
	}

	document.getElementById("visitLimitsChanged").value="Y";
}

function onVisitdeductibleChange(obj,type){
	var visitdeductible=obj.value;
	if(type=='P'){
		document.getElementById('primary_visit_deductible').value =formatAmountPaise(getPaise(visitdeductible));
	}
	else if(type=='S'){
	 	document.getElementById('secondary_visit_deductible').value =formatAmountPaise(getPaise(visitdeductible));
	}
	document.getElementById("visitLimitsChanged").value="Y";
}

function onMaxCopayChange(obj,type){
	var maxcopay=obj.value;
	if(type=='P'){
		document.getElementById('primary_max_copay').value =formatAmountPaise(getPaise(maxcopay));
	}
	else if(type=='S'){
	 	document.getElementById('secondary_max_copay').value =formatAmountPaise(getPaise(maxcopay));
	}
	document.getElementById("visitLimitsChanged").value="Y";
}

function onPerDayLimitChange(obj,type){
	var perdaylimit=obj.value;
	if(type=='P'){
		document.getElementById('primary_perday_limit').value =formatAmountPaise(getPaise(perdaylimit));
	}
	else if(type=='S'){
	 	document.getElementById('secondary_perday_limit').value =formatAmountPaise(getPaise(perdaylimit));
	}
	document.getElementById("visitLimitsChanged").value="Y";
}

function onVisitCopayPercChange(obj,type){
	var visitcopayperc=obj.value;
	if(type=='P'){
		document.getElementById('primary_visit_copay').value =formatAmountPaise(getPaise(visitcopayperc));
	}
	else if(type=='S'){
	 	document.getElementById('secondary_visit_copay').value =formatAmountPaise(getPaise(visitcopayperc));
	}
	document.getElementById("visitLimitsChanged").value="Y";
}

function onCopayPercentageChange(obj,type){
	var copaypercentage=obj.value;
	if(type == "P"){
		elements=document.getElementsByName("P_cat_id");
	}
	else if (type == "S"){
		elements=document.getElementsByName("S_cat_id");
	}
	for (var i=0; i<elements.length; i++) {
		var obj=document.getElementById(type+"_ecopay_percent"+i).value;
		if(parseFloat(obj)>parseFloat(100)){
			document.getElementById('copayFlag').value ="true";
			return false;
		}
		else{
			document.getElementById('copayFlag').value ="";
			document.getElementById('insEdited').value ="true";
		}
	}
	return true;
}

function validatedetails(){
	if(document.getElementById('copayFlag').value == "true"){
		showMessage("js.registration.patient.dynamic.copay.alert");
		return false;
	}
	return true;
}
////////////////////////////////////////////////////////// Sponsor master changes starts here

function enableOtherInsuranceDetailsTab(sponserIndex){

	var tpaId;
	var planTypeobj;
	var membershiptabObj;
	var memberidobjhidden;
	var memberidobj;
	var policydetailsobj;
	var priorAuthobj;
	var useDrgPerdiemObj;
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
	var drgtab;
	var validityonlydateobjhidden1;
	var planLimitRow;
	var visitDeductibleCellLbl;
	var visitDeductibleCellInfo;
	var visitCopayCellLbl;
	var visitCopayCellInfo;
	var maxCopayRow;

	var validityStartDateObj;
	var validityEndDateObj;

	var moreButtonDiv;

	var moreButtonLbl;

	if(sponserIndex=='P'){
		resetPrimaryHiddenFieldsRelatedToInsurance();
		document.getElementById("primaryInsuranceOtherDetailsTab").style.display = 'block';
		document.getElementById("primaryOthersDetailsTab").style.display = 'block';
		document.getElementById("primaryInsFile").style.display = 'table-row';
		tpaId=document.getElementById("primary_sponsor_id").value;
		planTypeobj=document.getElementById('primarynetworkPlanType');
		memberIdtab=document.getElementById("primaryMememberShipValidityTab");
		memberidobjhidden=document.getElementById("primary_member_id_hidden");
		memberidobj=document.getElementById("primary_member_id_label");
		policydetailsobj=document.getElementById("primaryPolicyDetailsTab");
		priorAuthobj=document.getElementById("primaryPriorAuthDetailsTab");

		useDrgPerdiemObj=document.getElementById("primarydrgPerdiemTab");
		validitydateobjhidden=document.getElementById("primary_policy_validity_hidden");
		policydetailshiddenobj=document.getElementById("primary_policy_Details_hidden");
		memberidstarobj=document.getElementById("primary_member_id_star");
		validitystartstarobj=document.getElementById("primary_policy_validity_start_star");
		validityendstarobj=document.getElementById("primary_policy_validity_end_star");
		policynumberstarobj=document.getElementById("primary_policy_number_star");
		policyholderstarobj=document.getElementById("primary_policy_holder_name_star");
		relationshipobj=document.getElementById("primary_patient_relationship_star");
		planstartdateobj=document.getElementById("primary_validity_start_period_tab");
		planenddateobj=document.getElementById("primary_validity_end_period_tab");
		planstartdateobjlabel=document.getElementById("primary_validity_start_period_label");
		planenddateobjlabel=document.getElementById("primary_validity_end_period_label");
		onlyValidityTab=document.getElementById("primaryOnlyValidityTab");
		validityonlydateobjhidden=document.getElementById("primary_policy_validity_only_hidden");
		validityonlystartstarobj=document.getElementById("primary_policy_validity_only_start_star");
		validityonlyendstarobj=document.getElementById("primary_policy_validity_only_end_star");
		drgtab=document.getElementById("primarydrgPerdiumDetailsTab");
		validityonlydateobjhidden1=document.getElementById("primary_policy_validity_only_hidden1");

		planLimitRow = document.getElementById("primary_planLimitRow");
		visitDeductibleCellLbl = document.getElementById("primary_visitDeductibleCellLbl");
		visitDeductibleCellInfo = document.getElementById("primary_visitDeductibleCellInfo");
		visitCopayCellLbl = document.getElementById("primary_visitCopayCellLbl");
		visitCopayCellInfo = document.getElementById("primary_visitCopayCellInfo");
		maxCopayRow = document.getElementById("primary_maxCopayRow");
		validityStartDateObj = document.getElementById("primary_policy_validity_start");
		validityEndDateObj = document.getElementById("primary_policy_validity_end");

		moreButtonDiv = document.getElementById("primary_insurance_div");
		moreButtonLbl = document.getElementById("primary_moreButtonLbl");
	}
	
	if(sponserIndex=='S'){
		resetSecondaryHiddenFieldsRelatedToInsurance();
		document.getElementById("secondaryInsuranceOtherDetailsTab").style.display = 'block';
		document.getElementById("secondaryOthersDetailsTab").style.display = 'block';
		document.getElementById("secondaryInsFile").style.display = 'table-row';
		tpaId=document.getElementById("secondary_sponsor_id").value;
		planTypeobj=document.getElementById('secondarynetworkPlanType');
		memberIdtab=document.getElementById("secondaryMememberShipValidityTab");
		memberidobjhidden=document.getElementById("secondary_member_id_hidden");
		memberidobj=document.getElementById("secondary_member_id_label");
		policydetailsobj=document.getElementById("secondaryPolicyDetailsTab");
		priorAuthobj=document.getElementById("secondaryPriorAuthDetailsTab");
		useDrgPerdiemObj=document.getElementById("secondarydrgPerdiemTab");
		validitydateobjhidden=document.getElementById("secondary_policy_validity_hidden");
		policydetailshiddenobj=document.getElementById("secondary_policy_Details_hidden");
		memberidstarobj=document.getElementById("secondary_member_id_star");
		validitystartstarobj=document.getElementById("secondary_policy_validity_start_star");
		validityendstarobj=document.getElementById("secondary_policy_validity_end_star");
		policynumberstarobj=document.getElementById("secondary_policy_number_star");
		policyholderstarobj=document.getElementById("secondary_policy_holder_name_star");
		relationshipobj=document.getElementById("secondary_patient_relationship_star");
		planstartdateobj=document.getElementById("secondary_validity_start_period_tab");
		planenddateobj=document.getElementById("secondary_validity_end_period_tab");
		planstartdateobjlabel=document.getElementById("secondary_validity_start_period_label");
		planenddateobjlabel=document.getElementById("secondary_validity_end_period_label");
		onlyValidityTab=document.getElementById("secondaryOnlyValidityTab");
		validityonlydateobjhidden=document.getElementById("secondary_policy_validity_only_hidden");
		validityonlystartstarobj=document.getElementById("secondary_policy_validity_only_start_star");
		validityonlyendstarobj=document.getElementById("secondary_policy_validity_only_end_star");
		drgtab=document.getElementById("secondarydrgPerdiumDetailsTab");
		validityonlydateobjhidden1=document.getElementById("secondary_policy_validity_only_hidden1");

		planLimitRow = document.getElementById("secondary_planLimitRow");
		visitDeductibleCellLbl = document.getElementById("secondary_visitDeductibleCellLbl");
		visitDeductibleCellInfo = document.getElementById("secondary_visitDeductibleCellInfo");
		visitCopayCellLbl = document.getElementById("secondary_visitCopayCellLbl");
		visitCopayCellInfo = document.getElementById("secondary_visitCopayCellInfo");
		maxCopayRow = document.getElementById("secondary_maxCopayRow");
		validityStartDateObj = document.getElementById("secondary_policy_validity_start");
		validityEndDateObj = document.getElementById("secondary_policy_validity_end");

		moreButtonDiv = document.getElementById("secondary_insurance_div");
		moreButtonLbl = document.getElementById("secondary_moreButtonLbl");
	}
	
	var item = findInList(tpanames, "tpa_id", tpaId);
	if(null != item && item !=""){
		for(var i=0;i<sponsorTypeList.length;i++){
			if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
				if(null != sponsorTypeList[i].plan_type_label && !empty(sponsorTypeList[i].plan_type_label))
					planTypeobj.innerHTML=sponsorTypeList[i].plan_type_label+":";
				if(sponsorTypeList[i].member_id_show == 'Y'){
					memberIdtab.style.display = 'block';
					planstartdateobjlabel.innerHTML=getString("js.registration.patient.commonlabel.validitystart")+":";
					planenddateobjlabel.innerHTML=getString("js.registration.patient.commonlabel.validityend")+":";
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
				if(sponsorTypeList[i].prior_auth_show == 'Y'){
					priorAuthobj.style.display = 'block';
				}else{
					// TODO: Review why this is like this
					// drgtab.style.display = 'block';
				}

				if(sponsorTypeList[i].visit_limits_show == 'Y'){
					planLimitRow.style.visibility = "visible";
					visitDeductibleCellLbl.style.visibility = "visible";
					visitDeductibleCellInfo.style.visibility = "visible";
					visitCopayCellLbl.style.visibility = "visible";
					visitCopayCellInfo.style.visibility = "visible";
					maxCopayRow.style.visibility = "visible";

					moreButtonLbl.style.visibility = "visible";
					moreButtonDiv.style.visibility = "visible";
				}else{
					planLimitRow.style.visibility = "hidden";
					visitDeductibleCellLbl.style.visibility = "hidden";
					visitDeductibleCellInfo.style.visibility = "hidden";
					visitCopayCellLbl.style.visibility = "hidden";
					visitCopayCellInfo.style.visibility = "hidden";
					maxCopayRow.style.visibility = "hidden";

					moreButtonLbl.style.visibility = "hidden";
					moreButtonDiv.style.visibility = "hidden";

				}
				if(regPref.allow_drg_perdiem == 'Y')
					useDrgPerdiemObj.style.display = 'block';

				if(sponsorTypeList[i].validity_period_editable == 'N'){
					validityStartDateObj.readOnly = true;
					validityEndDateObj.readOnly = true;
				}
				enableDisableDocumentUploader(sponserIndex);
				break;
			}
			else{
				planTypeobj.innerHTML=getString("js.registration.patient.planType")+":";
				memberidobj.innerHTML=getString("js.registration.patient.membershipId")+":";
				memberIdtab.style.display = 'none';
				policydetailsobj.style.display = 'none';
				priorAuthobj.style.display = 'none';
				drgtab.style.display = 'none';
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
				useDrgPerdiemObj.style.display = 'none';
			}
		}
	}
}

function isValidityPeriodEditable(tpaObj){
	var editable = false;
	if(null != tpaObj){
		var tpaId = tpaObj.value;
		var item = findInList(tpanames, "tpa_id", tpaId);
		if(null != item && item !=""){
			for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
					if(sponsorTypeList[i].validity_period_editable == 'Y'){
						editable = true;
					}
				}
			}
		}
	}
	return editable;
}

function resetPrimaryHiddenFieldsRelatedToInsurance(){
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

	var validityStartDateObj = document.getElementById("primary_policy_validity_start");
    var validityEndDateObj = document.getElementById("primary_policy_validity_end");
    if(validityStartDateObj != null) validityStartDateObj.readOnly = false;
    if(validityEndDateObj != null) validityEndDateObj.readOnly = false;
}

function resetSecondaryHiddenFieldsRelatedToInsurance(){
	var memberidobjhidden=document.getElementById("secondary_member_id_hidden");
	if (memberidobjhidden != null) memberidobjhidden.value = "";

	var validityperiodobjhidden=document.getElementById("secondary_policy_validity_hidden");
	if (validityperiodobjhidden != null) validityperiodobjhidden.value = "";

	var policydetailshiddenobj=document.getElementById("secondary_policy_Details_hidden");
	if (policydetailshiddenobj != null) policydetailshiddenobj.value = "";

	var validityonlydateobjhidden=document.getElementById("secondary_policy_validity_only_hidden");
	if (validityonlydateobjhidden != null) validityonlydateobjhidden.value = "";

	var validityonlydateobjhidden1=document.getElementById("secondary_policy_validity_only_hidden1");
	if (validityonlydateobjhidden1 != null) validityonlydateobjhidden1.value = "";

	var validityStartDateObj = document.getElementById("secondary_policy_validity_start");
	var validityEndDateObj = document.getElementById("secondary_policy_validity_end");
	if(validityStartDateObj != null) validityStartDateObj.readOnly = false;
	if(validityEndDateObj != null) validityEndDateObj.readOnly = false;
}

function validateMemberIdRequired(sponserIndex){
	var memberidobjhidden;
	var memberidobj;
	var memberidobjlabel;

	if(sponserIndex=='P'){
		memberidobj = getPrimaryInsuranceMemberIdObj();
		memberidobjhidden=document.getElementById("primary_member_id_hidden").value;
		memberIdLabel=document.getElementById("primary_member_id_label").innerHTML;
	}

	if(sponserIndex=='S'){
		memberidobj = getSecondaryInsuranceMemberIdObj();
		memberidobjhidden=document.getElementById("secondary_member_id_hidden").value;
		memberIdLabel=document.getElementById("secondary_member_id_label").innerHTML;
	}
	memberIdLabel=memberIdLabel.replace(':','');
	if (memberidobjhidden == 'Y' && memberidobj != null && memberidobj.value == "") {
			alert(memberIdLabel +  " " +getString("js.common.is.required"));
			memberidobj.focus();
			return false;
	}
	return true;
}

function validatePlanPeriodRequired(sponserIndex){
	var validityperiodhidden;
	var policyStartDateObj;
	var	policyEndDateObj;
	var validityonlydateobjhidden;
	var policyStartDateObjonly;
	var	policyEndDateObjonly;
	var validityonlydateobjhidden1;
	var tpaId;

	if(sponserIndex=='P'){
		tpaId=document.getElementById("primary_sponsor_id").value;
		policyStartDateObj = getPrimaryPolicyValidityStartObj();
		policyEndDateObj = getPrimaryPolicyValidityEndObj();
		validityperiodhidden=document.getElementById("primary_policy_validity_hidden").value;
		validityonlydateobjhidden=document.getElementById("primary_policy_validity_only_hidden").value;
		validityonlydateobjhidden1=document.getElementById("primary_policy_validity_only_hidden").value;
		policyStartDateObjonly=document.getElementById("primary_policy_validity_only_start");
		policyEndDateObjonly=document.getElementById("primary_policy_validity_only_end");
		memberIdValidFromLabel=document.getElementById("primary_validity_start_period_label").innerHTML;
		memberIdValidToLabel=document.getElementById("primary_validity_end_period_label").innerHTML;
	}

	if(sponserIndex=='S'){
		tpaId=document.getElementById("secondary_sponsor_id").value;
		policyStartDateObj = getSecondaryPolicyValidityStartObj();
		policyEndDateObj = getSecondaryPolicyValidityEndObj();
		validityperiodhidden=document.getElementById("secondary_policy_validity_hidden").value;
		validityonlydateobjhidden=document.getElementById("secondary_policy_validity_only_hidden").value;
		validityonlydateobjhidden1=document.getElementById("secondary_policy_validity_only_hidden1").value;
		policyStartDateObjonly=document.getElementById("secondary_policy_validity_only_start");
		policyEndDateObjonly=document.getElementById("secondary_policy_validity_only_end");
		memberIdValidFromLabel=document.getElementById("secondary_validity_start_period_label").innerHTML;
		memberIdValidToLabel=document.getElementById("secondary_validity_end_period_label").innerHTML;
	}
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
		var item = findInList(tpanames, "tpa_id", tpaId);
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

function validatePolicyDetailsRequired(sponserIndex){
	var policyDetailsobjhidden;
	var policynumberobj;
	var policyholderobj;
	var relationshipobj;
	var policynolabel;
	var policyholderlabel;
	var relationshiplabel;

	if(sponserIndex=='P'){
		policyDetailsobjhidden=document.getElementById("primary_policy_Details_hidden").value;
		policynumberobj = document.getElementById("primary_policy_number");
		policyholderobj=document.getElementById("primary_policy_holder_name");
		relationshipobj=document.getElementById("primary_patient_relationship");
	}
	if(sponserIndex=='S'){
		policyDetailsobjhidden=document.getElementById("secondary_policy_Details_hidden").value;
		policynumberobj = document.getElementById("secondary_policy_number");
		policyholderobj=document.getElementById("secondary_policy_holder_name");
		relationshipobj=document.getElementById("secondary_patient_relationship");
	}

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

function sortByKey(array, key) {
	if(null != array){
	    return array.sort(function(a, b) {
	        var x = (a!=null || a!=undefined)?a[key]:''; var y = (b!=null || b!=undefined)?b[key]:'';
	        return ((x < y) ? -1 : ((x > y) ? 1 : 0));
	    });
    }
}

var pritpaAutoComp = null;
function priAutoLoadTpa(tpaList){

	if (pritpaAutoComp != null) {
		pritpaAutoComp.destroy();
	}
	var tpaNameArray = [];
	for (var i=0; i<tpaList.length; i++) {
		tpaNameArray.push(tpaList[i]);
	}
	tpaNameArray=sortByKey(tpaNameArray,"tpa_name");

	var ds = null;
	ds=new YAHOO.util.LocalDataSource({result: tpaNameArray});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = {
		resultsList : "result",
		fields : [	{key : "tpa_name"},
					{key : "tpa_id"}
			 	]
	};

	pritpaAutoComp = new YAHOO.widget.AutoComplete('primary_sponsor_name', 'primary_tpa_dropdown', ds);
	pritpaAutoComp.maxResultsDisplayed = 10;
	pritpaAutoComp.queryMatchContains = true;
	pritpaAutoComp.allowBrowserAutocomplete = false;
	pritpaAutoComp.formatResult = Insta.autoHighlight;
	pritpaAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	pritpaAutoComp.typeAhead = false;
	pritpaAutoComp.useShadow = false;
	pritpaAutoComp.minQueryLength = 0;
	pritpaAutoComp.resultTypeList = false;
	pritpaAutoComp.forceSelection = true;
	pritpaAutoComp._bItemSelected = true;

	if (pritpaAutoComp._elTextbox.value != '') {
		pritpaAutoComp._bItemSelected = true;
		pritpaAutoComp._sInitInputValue = pritpaAutoComp._elTextbox.value;
	}

	pritpaAutoComp.itemSelectEvent.subscribe(setPriTpaID);
	pritpaAutoComp.textboxBlurEvent.subscribe(function() {
	var sponsorName = document.mainform.primary_sponsor_name.value;
			if(sponsorName == '') {
				document.mainform.primary_sponsor_name.value='';
				document.mainform.primary_sponsor_id.value='';
				loadInsuranceCompList('P');
				resetSponsorDetailsTabs('P');
			}
		});
}

function setPriTpaID(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;	RatePlanList();
	ratePlanChange();

	document.getElementById("primary_sponsor_id").value = sArgs[2].tpa_id;
	loadInsuCompanyDetails('P');
	loadOtherInsDetails('P');
	insuCatChange('P');
	RatePlanList();
	ratePlanChange();
	enableDisableDocumentUploader('P');
}

//Sends AJAX Request to bring policyNames based on insurance company and plan type selected
function getPolicyNames(spnsrIndex) {
	var prefix;
	if (spnsrIndex == 'P') {
		prefix = 'primary';
	} else {
		prefix = 'secondary';
	}
	var insuranceCoId = document.getElementById(prefix + "_insurance_co").value;
	var categoryId = document.getElementById(prefix + '_plan_type').value;
	var sponsorId = document.getElementById(prefix + '_sponsor_id').value;
	return sendPolicyNamesAjax(categoryId, insuranceCoId, sponsorId);
}

//keeping a track of previous ids so that duplicate API call can be avoided
var previousInsuranceCoId = "";
var previousSponsorId = "";
var previousCategoryId = "";
function sendPolicyNamesAjax(categoryId, insuranceCoId, sponsorId) {
	
	if (insuranceCoId != "" && categoryId != null && sponsorId != null
			&& (previousInsuranceCoId != insuranceCoId || previousSponsorId != sponsorId
			|| previousCategoryId != categoryId)) {
		
		
		var ajaxobj = newXMLHttpRequest();
		var url = null;
		url = cpath + "/master/insuranceplans/plansByInscoAndCat.json?category_id=" + categoryId 
				+ "&insurance_co_id=" + insuranceCoId + "&sponsor_id=" + sponsorId;

		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				//assign policynames
				var responseObj = JSON.parse(ajaxobj.responseText);
				//update previous Ids to prevent duplicate API calls
				previousInsuranceCoId = insuranceCoId;
				previousSponsorId = sponsorId;
				previousCategoryId = categoryId;
				return responseObj.planList;
			}
		}
	} else if (policynames != null){
		return policynames;
	} else {
		return [];
	}

}



//Sends AJAX Request to bring planTypes based on insurance company

function getPlanTypesAjax(insCompanyId) {
	if (insCompanyId != "" && insCompanyId != null) {
		var ajaxobj = newXMLHttpRequest();
		var url = null;
		url = cpath + "/editVisit/changeTPA.do?_method=getInsurancePlanType";
		url = url + "&visitId="+visitId +"&insCompanyId=" + insCompanyId;
		ajaxobj.open("GET", url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				return JSON.parse(ajaxobj.responseText);
			}
		}
	}
}

// concatenates policynames lists and removes duplicate entries
function mergePolicyNames(policyListA, policyListB) {
	if (policyListA != null && policyListB != null) {
		policyListA = policyListA.concat(policyListB);
		//remove duplicates
		for(var i=0; i<policyListA.length; i++) {
			for(var j=i+1; j<policyListA.length; j++) {
				if (policyListA[i].plan_id == policyListA[j].plan_id) {
					policyListA.splice(j,j);
				}
			}
		}
		return policyListA;
	} else if (policyListA == null && policyListB != null) {
		return policyListB;
	} else if (policyListA != null && policyListB == null) {
		return policyListA;
	}
}

function loadInsuCompanyDetails(sponsorIndex){
	var categoryId='';
	if (document.mainform.patient_category_id)
			categoryId = document.mainform.patient_category_id.value;
	var tpaID;
	var insCompObj;
	var insCompID;
	if(sponsorIndex == 'P'){
		tpaID = document.mainform.primary_sponsor_id.value;
		insCompObj= getPrimaryInsuObj();
		if(insCompObj != null)
			insCompID=insCompObj.value;
	}else if(sponsorIndex == 'S'){
		tpaID = document.mainform.secondary_sponsor_id.value;
		insCompObj= getSecondaryInsuObj();
		if(insCompObj != null)
			insCompID=insCompObj.value;
	}
	if(!empty(tpaID)){
	  var allowedInsComps=loadInsuranceCompListBasedOnPatCat(sponsorIndex);
	  var tpaInsCompList = filterList(companyTpaList, 'tpa_id', tpaID);
	  if (screenid == 'Edit_Insurance') {
	   tpaInsCompList = filterList(comTpaListAll, 'tpa_id', tpaID);
	  }
	  var newins =[];
	  for (var i = 0; i < allowedInsComps.length; i++) {
	     var item = allowedInsComps[i];
	     item = findInList(insuCompanyDetails, "insurance_co_id", item.insurance_co_id);
	     for (var k = 0; k < tpaInsCompList.length; k++) {
	        var insItem = tpaInsCompList[k];
	        if (!empty(item) && !empty(insItem) && (item.insurance_co_id == insItem.insurance_co_id)){
	           newins.push(insItem);
	        }
	     }
	  }
	  if (!empty(newins)){
	      newins=sortByKey(newins,"insurance_co_name");
        loadSelectBox(insCompObj, newins, 'insurance_co_name',
   							'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));

   			var exist=[];
   			if(!empty(tpaID) ){
   				if(insCompID != ''){
   					exist = findInList(newins, "insurance_co_id", insCompID);
   					if(!empty(exist)){
   						setSelectedIndex(insCompObj,0);
   					}else{
   						insCompID ='';
   					}
   				}
   			}
   			if(newins.length ==1 && insCompID ==''){
   				setSelectedIndex(insCompObj, newins[0].insurance_co_id);
   			}
   			if(insCompID !=''){
   				setSelectedIndex(insCompObj,insCompID);
   			}
   		}else{
   			if(empty(tpaInsCompList)){
   				allowedInsComps=sortByKey(allowedInsComps,"insurance_co_name");
   				loadSelectBox(insCompObj, allowedInsComps, 'insurance_co_name',
   							'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
   			}else{
   				loadSelectBox(insCompObj, newins, 'insurance_co_name',
   							'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
   			}
   			if(insCompID !=''){
   				setSelectedIndex(insCompObj,insCompID);
   			}
   		}
   	}else{
   		allowedInsComps=sortByKey(allowedInsComps,"insurance_co_name");
   		loadSelectBox(insCompObj, allowedInsComps, 'insurance_co_name',
   				'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
   	}

	if(null != insCompObj && !empty(insCompObj.value) && 'P' == sponsorIndex){
		insuPrimaryViewDoc(insCompObj);
		if (screenid == 'Edit_Insurance') {
			primaryPlanTypeAjax = getPlanTypesAjax(insCompObj.value);
			 insuCatNames = primaryPlanTypeAjax.categoryLists;
		}
	}else{
         document.getElementById('viewinsuranceprimaryruledocs').style.display = 'none';
   }

    if(null != insCompObj && !empty(insCompObj.value) && 'S' == sponsorIndex){
		 insuSecondaryViewDoc(insCompObj);
		if (screenid == 'Edit_Insurance') {
			secondaryPlanTypeAjax = getPlanTypesAjax(insCompObj.value);
			 insuCatNames = secondaryPlanTypeAjax.categoryLists;

		}
	}else{
         document.getElementById('viewinsurancesecondaryruledocs').style.display = 'none';
  }

}

function loadOtherInsDetails(spnsrIndex){
	var loadTpaOnInsChange = (isModAdvanceIns || isModInsurance);

	var tpaObj = null;
	var insuCompObj = null;
	var planTypeObj = null;
	var planObj = null;
	var tpaNameObj=null;

	if (spnsrIndex == 'P') {
		tpaObj = getPrimarySponsorObj();
		tpaNameObj=getPrimarySponsorNameObj();
		insuCompObj = getPrimaryInsuObj();
		planTypeObj = getPrimaryPlanTypeObj();
		planObj = getPrimaryPlanObj();

	}else if (spnsrIndex == 'S') {
		tpaObj = getSecondarySponsorObj();
		tpaNameObj=getSecondarySponsorNameObj();
		insuCompObj = getSecondaryInsuObj();
		planTypeObj = getSecondaryPlanTypeObj();
		planObj = getSecondaryPlanObj();
	}

	var categoryId = '';
	if (document.mainform.patient_category_id)
		categoryId = document.mainform.patient_category_id.value;

	var insCompanyId = '';
	if (insuCompObj != null)
		insCompanyId = insuCompObj.value;

	var planType = '';
	if (planTypeObj != null) {
		planType = planTypeObj.value;

		// Empty plan types in plan type dropdown
		var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
		planTypeObj.options.length = 1;
		planTypeObj.options[0] = optn;
	}

	var plan = '';
	if (planObj != null) {
		plan = planObj.value;

		// Empty plans in plan dropdown
		var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
		planObj.options.length = 1;
		planObj.options[0] = optn;
	}

	var mainSpnsrIndex = spnsrIndex; // getMainSponsorIndex();

	var mainInsuObj = null;
	var mainInsCompanyId = '';
	if (mainSpnsrIndex == 'P') mainInsuObj = getPrimaryInsuObj();
	else if (mainSpnsrIndex == 'S') mainInsuObj = getSecondaryInsuObj();
	if (mainInsuObj != null)
		mainInsCompanyId = mainInsuObj.value;

	// gIsInsurance - advance insurance and company not empty, variable for first of category check & display patient amounts.
	if (isModAdvanceIns && mainInsCompanyId != '') gIsInsurance = true;
	else gIsInsurance = false;

	if (!gIsInsurance) {
		if (planTypeObj != null) {
			planTypeObj.selectedIndex = 0;
			insuCatChange(spnsrIndex);
		}
		if (document.mainform.op_type != null
				&& (document.mainform.op_type.value != "F" && document.mainform.op_type.value != "D"))
			if (tpaObj != null) tpaObj.disabled = false;
	}

		if (empty(mainSpnsrIndex) || mainSpnsrIndex == spnsrIndex) {

			var j = 2;
			// Load plan types related to insurance company
			if (insuCatNames != null){
			for (var i = 0; i < insuCatNames.length; i++) {
				var ele = insuCatNames[i];
				if (ele.insurance_co_id == mainInsCompanyId && ele.status == "A") {
					var hasPlanMappedToSponsor = false;
					if (typeof networkTypeSponsorIdListMap === 'undefined') {
						hasPlanMappedToSponsor = true;
					}
					var sponsorIdList = typeof networkTypeSponsorIdListMap !== 'undefined'
						&& networkTypeSponsorIdListMap[ele.category_id]
						? networkTypeSponsorIdListMap[ele.category_id] : [];
					for (var k = 0; k < sponsorIdList.length; k++) {
						if (!tpaObj || !tpaObj.value) {
							break;
						}
						if (sponsorIdList[k] === null || sponsorIdList[k] === '' || sponsorIdList[k] === tpaObj.value) {
							hasPlanMappedToSponsor = true;
							break;
						}
					}
					if (!hasPlanMappedToSponsor) {
						continue;
					}
					var optn = new Option(ele.category_name, ele.category_id);
					planTypeObj.options.length = j;
					planTypeObj.options[j-1] = optn;
					j++;
					planType = ele.category_id;
				}
			}
			}
		}

	var ratePlanObj = document.mainform.organization;
	var selectedRatePlan = ratePlanObj.value;
	var insCompDefaultRatePlan = '';

	var selectedIns = findInList(insuCompanyDetails, "insurance_co_id", mainInsCompanyId);
	if (!empty(selectedIns) && !empty(selectedIns.default_rate_plan)) {
		insCompDefaultRatePlan = selectedIns.default_rate_plan;
	}
	if (!empty(insCompDefaultRatePlan))
		setSelectedIndex(ratePlanObj, insCompDefaultRatePlan);
	else
		setSelectedIndex(ratePlanObj, selectedRatePlan);

	var insCompTpaList = filterList(companyTpaList, 'insurance_co_id', insCompanyId);
	if (empty(insCompTpaList)) {
		insCompTpaList = tpanames;
	}

	if (planTypeObj != null)
		sortDropDown(planTypeObj);
	if (planObj)
		sortDropDown(planObj);

	if(corpInsuranceCheck != 'Y'){
			if (document.mainform.patient_category_id &&
					 planTypeObj != null && planTypeObj.options.length == 2) {
				setSelectedIndex(planTypeObj, planType);
			}
			else if(null == document.mainform.patient_category_id &&
					 planTypeObj != null && planTypeObj.options.length == 2) {
				setSelectedIndex(planTypeObj, planType);
			}
		}else
		{
			if (document.mainform.patient_category_id &&
					 planTypeObj != null) {
				setSelectedIndexForCorpInsurance(planTypeObj);
			}
		}

		enableOtherInsuranceDetailsTab(spnsrIndex);
}



var sectpaAutoComp = null;
function secAutoLoadTpa(tpaList){

	if (sectpaAutoComp != null) {
		sectpaAutoComp.destroy();
	}
	var tpaNameArray = [];
	for (var i=0; i<tpaList.length; i++) {
		tpaNameArray.push(tpaList[i]);
	}

	tpaNameArray=sortByKey(tpaNameArray,"tpa_name");

	var ds = null;
	ds=new YAHOO.util.LocalDataSource({result: tpaNameArray});
	ds.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	ds.responseSchema = {
		resultsList : "result",
		fields : [	{key : "tpa_name"},
					{key : "tpa_id"}
			 	]
	};

	sectpaAutoComp = new YAHOO.widget.AutoComplete('secondary_sponsor_name', 'secondary_tpa_dropdown', ds);
	sectpaAutoComp.maxResultsDisplayed = 10;
	sectpaAutoComp.queryMatchContains = true;
	sectpaAutoComp.allowBrowserAutocomplete = false;
	sectpaAutoComp.formatResult = Insta.autoHighlight;
	sectpaAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	sectpaAutoComp.typeAhead = false;
	sectpaAutoComp.useShadow = false;
	sectpaAutoComp.minQueryLength = 0;
	sectpaAutoComp.resultTypeList = false;
	sectpaAutoComp.forceSelection = true;
	sectpaAutoComp._bItemSelected = true;

	if (sectpaAutoComp._elTextbox.value != '') {
		sectpaAutoComp._bItemSelected = true;
		sectpaAutoComp._sInitInputValue = sectpaAutoComp._elTextbox.value;
	}

	sectpaAutoComp.itemSelectEvent.subscribe(setSecTpaID);
	sectpaAutoComp.textboxBlurEvent.subscribe(function() {
	var sponsorName = document.mainform.secondary_sponsor_name.value;
			if(sponsorName == '') {
				document.mainform.secondary_sponsor_name.value='';
				document.mainform.secondary_sponsor_id.value='';
				loadInsuranceCompList('S');
				resetSponsorDetailsTabs('S');
			}
		});
}

function setSecTpaID(oSelf, sArgs) {
	var elId = sArgs[0]._elTextbox.id;
	document.getElementById("secondary_sponsor_id").value = sArgs[2].tpa_id;
	loadInsuCompanyDetails('S');
	loadOtherInsDetails('S');
	insuCatChange('S');
	RatePlanList();
	ratePlanChange();
	enableDisableDocumentUploader('S');
}

function resetSponsorDetailsTabs(sponsorIndex){
	if(sponsorIndex == 'P'){
		document.mainform.primary_sponsor_name.value='';
		document.mainform.primary_sponsor_id.value='';
		document.mainform.primary_insurance_co.value='';
		//Removed for Registration performance Improvement in 11.3
		//loadInsuranceCompList('P');
		document.getElementById("primaryInsuranceOtherDetailsTab").style.display = 'none';
		document.getElementById("primaryOthersDetailsTab").style.display = 'none';
		document.getElementById("primaryPolicyDetailsTab").style.display = 'none';
		document.getElementById("primaryPriorAuthDetailsTab").style.display = 'none';
		document.getElementById("primaryOnlyValidityTab").style.display = 'none';
		document.getElementById("primarydrgPerdiumDetailsTab").style.display = 'none';
		document.getElementById("primaryMememberShipValidityTab").style.display = 'none';
		document.getElementById("primarydrgPerdiemTab").style.display = 'none';
	}
	if(sponsorIndex == 'S'){
		document.mainform.secondary_sponsor_name.value='';
		document.mainform.secondary_sponsor_id.value='';
		document.mainform.secondary_insurance_co.value='';
		//Removed for Registration performance Improvement in 11.3
		//loadInsuranceCompList('S');
		document.getElementById("secondaryInsuranceOtherDetailsTab").style.display = 'none';
		document.getElementById("secondaryOthersDetailsTab").style.display = 'none';
		document.getElementById("secondaryPolicyDetailsTab").style.display = 'none';
		document.getElementById("secondaryPriorAuthDetailsTab").style.display = 'none';
		document.getElementById("secondaryOnlyValidityTab").style.display = 'none';
		document.getElementById("secondarydrgPerdiumDetailsTab").style.display = 'none';
		document.getElementById("secondaryMememberShipValidityTab").style.display = 'none';
		document.getElementById("secondarydrgPerdiemTab").style.display = 'none';
	}
}

function forFollowUps(){
	if (null != document.mainform.op_type){
			var op_type = document.mainform.op_type.value;
			if(op_type == "F" || op_type == "D")
				document.getElementById('insEdited').value ="true";
	}
}

function checkForSponsorMasterSettingForfurtherValidations(sponsorIndex){
	var tpaid;
	if(sponsorIndex == 'P'){
		tpaid=document.getElementById("primary_sponsor_id").value;
	}
	if(sponsorIndex == 'S'){
		tpaid=document.getElementById("secondary_sponsor_id").value;
	}

	var item = findInList(tpanames, "tpa_id", tpaid);

	if(null != item && item !=""){
		for(var i=0;i<sponsorTypeList.length;i++){
			if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
				if(sponsorTypeList[i].validity_period_mandatory != 'Y' && sponsorTypeList[i].member_id_mandatory != 'Y')
					return false;
			}
		}
	}
	return false;
}

function setOtherInsObjects(){
	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if(primarySponsorObj.value == 'I' ||  secondarySponsorObj.value == 'I') {
		var primarySponsorIdObj = document.getElementById("primary_sponsor_id");
		var validityonlydateobjhidden1=document.getElementById("primary_policy_validity_only_hidden1").value;
		if(primarySponsorIdObj!=null){
			if(validityonlydateobjhidden1 == 'Y'){
				var primarystartperiod=document.getElementById("primary_policy_validity_start");
				var primarystartperiodonly=document.getElementById("primary_policy_validity_only_start");
				primarystartperiod.value=primarystartperiodonly.value;

				var primaryendperiod=document.getElementById("primary_policy_validity_end");
				var primaryendperiodonly=document.getElementById("primary_policy_validity_only_end");
				primaryendperiod.value=primaryendperiodonly.value;
			}
		}

		var secondarySponsorIdObj = document.getElementById("secondary_sponsor_id");
		var validityonlydateobjhidden1=document.getElementById("secondary_policy_validity_only_hidden1").value;
		if(secondarySponsorIdObj!=null){
			if(validityonlydateobjhidden1 == 'Y'){
				var secondarystartperiod=document.getElementById("secondary_policy_validity_start");
				var secondarystartperiodonly=document.getElementById("secondary_policy_validity_only_start");
				secondarystartperiod.value=secondarystartperiodonly.value;

				var secondaryendperiod=document.getElementById("secondary_policy_validity_end");
				var secondaryendperiodonly=document.getElementById("secondary_policy_validity_only_end");
				secondaryendperiod.value=secondaryendperiodonly.value;
			}
	    }
    }
}

function setDateObjectsForValidityPeriod(){
	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if(primarySponsorObj.value == 'I' ||  secondarySponsorObj.value == 'I') {
		var primarySponsorIdObj = document.getElementById("primary_sponsor_id");
		var validityonlydateobjhidden1=document.getElementById("primary_policy_validity_only_hidden1").value;
		if(primarySponsorIdObj!=null){
			if(validityonlydateobjhidden1 == 'Y'){
				var primarystartperiod=document.getElementById("primary_policy_validity_start");
				var primarystartperiodonly=document.getElementById("primary_policy_validity_only_start");
				primarystartperiodonly.value=primarystartperiod.value;

				var primaryendperiod=document.getElementById("primary_policy_validity_end");
				var primaryendperiodonly=document.getElementById("primary_policy_validity_only_end");
				primaryendperiodonly.value=primaryendperiod.value;
			}
		}
		var secondarySponsorIdObj = document.getElementById("secondary_sponsor_id");
		var validityonlydateobjhidden1=document.getElementById("secondary_policy_validity_only_hidden1").value;
		if(secondarySponsorIdObj!=null){
			if(validityonlydateobjhidden1 == 'Y'){
				var secondarystartperiod=document.getElementById("secondary_policy_validity_start");
				var secondarystartperiodonly=document.getElementById("secondary_policy_validity_only_start");
				secondarystartperiodonly.value=secondarystartperiod.value;

				var secondaryendperiod=document.getElementById("secondary_policy_validity_end");
				var secondaryendperiodonly=document.getElementById("secondary_policy_validity_only_end");
				secondaryendperiodonly.value=secondaryendperiod.value;
			}
		}
	}
}


function loadInsuranceCompListBasedOnPatCat(spnsrIndex) {

	var categoryId = '';
	if (document.mainform.patient_category_id)
		categoryId = document.mainform.patient_category_id.value;
		//patientCategory

	var tpaIdObj = null;
	var insuCompIdObj = null;

	if (spnsrIndex == 'P') {
		tpaIdObj = getPrimarySponsorObj();
		insuCompIdObj = getPrimaryInsuObj();

	}else if (spnsrIndex == 'S') {
		tpaIdObj = getSecondarySponsorObj();
		insuCompIdObj = getSecondaryInsuObj();
	}

	var insCompList=insuCompanyDetails; // the default set: all Ins Comps
	var defaultInsComp = "";
	var visitType = screenid == 'ip_registration'? 'i': 'o';

	if(screenid == 'Edit_Insurance'){
		visitType=pvisitType;
		categoryId=patientCategory;

	}
	// category is enabled, the list of Insurance Comps. is restricted
	if (categoryId != '') {
		insCompList = insuCompanyDetails;
		var item = findInList(categoryJSON, "category_id", categoryId);
		if(null != item && !empty(item)){
			if (visitType == 'i') {
					if (!empty(item.ip_allowed_insurance_co_ids) && item.ip_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.ip_allowed_insurance_co_ids.split(',');
						var ip_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++) {
							var insurance_details = findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]);
							if (insurance_details != null) {
								ip_allowedInsComps.push(insurance_details);								
							}							
						}
						// override the insCompList with allowed Ins Comps.
						insCompList =  !empty(ip_allowedInsComps) ? ip_allowedInsComps : insCompList;
					}
				 } else {
				 	if (!empty(item.op_allowed_insurance_co_ids) && item.op_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.op_allowed_insurance_co_ids.split(',');
						var op_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++) {
							var insurance_details = findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]);
							if (insurance_details != null) {
								op_allowedInsComps.push(insurance_details);								
							}			
						}
						// override the insCompList with allowed Ins Comps.
						insCompList =  !empty(op_allowedInsComps) ? op_allowedInsComps : insCompList;
					}
				}
		}
	}
	return insCompList;
}

function checkForDateObjectSetting(sponsorIndex){
	var sponsorIdObj;

	if(sponsorIndex == 'P')
		sponsorIdObj = document.getElementById("primary_sponsor_id");
	else if(sponsorIndex == 'S')
		sponsorIdObj = document.getElementById("secondary_sponsor_id");

	if(null != sponsorIdObj && sponsorIdObj != ''){
		var item = findInList(tpanames, "tpa_id", sponsorIdObj.value);
		if(null != item && item !=""){
			for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
						if(sponsorTypeList[i].validity_period_show == 'Y')
							return true;
				}
			}
		}
	}
}

function getAllTPArelatedToInsComp(insCompanyId){

	var tpaList=[];
	var tpaItems=[];
	var tpa_allowed = [];

	for(var x=0; x<companyTpaList.length;x++){
		if(insCompanyId == companyTpaList[x].insurance_co_id)
			tpaItems.push(companyTpaList[x].tpa_id);
	}
	for(var z=0; z<tpaItems.length;z++){
		tpa_allowed.push(findInList(tpanames, "tpa_id", tpaItems[z]));
	}
	tpaList = !empty(tpa_allowed) ? tpa_allowed : tpanames;

	return tpaList;
}

function resetTpaList(sponsorIndex){
	loadInsuranceCompListBasedOnPatCat(sponsorIndex);
	if (tpaObj != null){
		if(sponsorIndex == 'P')
			priAutoLoadTpa(tpaList);
		if(sponsorIndex == 'S')
			secAutoLoadTpa(tpaList);
	}
}

function forEditInsAndfollowUps(sponsorIndex){
	var categoryId='';
	if (document.mainform.patient_category_id)
			categoryId = document.mainform.patient_category_id.value;
	var tpaID;
	var insCompID;

	if(sponsorIndex == 'P'){
		tpaID = document.mainform.primary_sponsor_id.value;
		insCompID= document.getElementById("primary_insurance_co");
	}else if(sponsorIndex == 'S'){
		tpaID = document.mainform.secondary_sponsor_id.value;
		insCompID= document.getElementById("secondary_insurance_co");
	}
	if(!empty(tpaID)){
		var allowedInsComps=loadInsuranceCompListBasedOnPatCat(sponsorIndex);
		var tpaInsCompList = filterList(companyTpaList, 'tpa_id', tpaID);
		if (empty(tpaInsCompList)) {
			tpaInsCompList = tpanames;
		}
		var newins =[];
		for (var i = 0; i < allowedInsComps.length; i++) {
			var item = allowedInsComps[i];
			item = findInList(insuCompanyDetails, "insurance_co_id", item.insurance_co_id);
			for (var k = 0; k < tpaInsCompList.length; k++) {
				var insItem = tpaInsCompList[k];
				if (!empty(item) && !empty(insItem) && (item.insurance_co_id == insItem.insurance_co_id)){
						newins.push(item);
				}
			}
		}
		if (!empty(newins)){
			var temp=insCompID.value;
			allowedInsComps=sortByKey(newins,"insurance_co_name");
			loadSelectBox(insCompID, allowedInsComps, 'insurance_co_name',
							'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
			if(!empty(tpaID) ){
				if(insCompID.value != ''){
					var exist = findInList(newins, "insurance_co_id", insCompID.value);
					if(empty(exist)){
						setSelectedIndex(insCompID,0);
					}
				}
				else if(temp != ''){
					var exist = findInList(newins, "insurance_co_id", temp);
					if(!empty(exist)){
						setSelectedIndex(insCompID,temp);
					}
				}
			}
			if(newins.length ==1 && insCompID.value ==''){
				setSelectedIndex(insCompID, newins[0].insurance_co_id);
			}
		}
	}else{
		allowedInsComps=sortByKey(allowedInsComps,"insurance_co_name");
		loadSelectBox(insCompID, allowedInsComps, 'insurance_co_name',
				'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
	}
}


function disableVisitLimitFields(spnsrIndex,readOnlyBool){

	if(spnsrIndex == 'P'){
		document.getElementById("primary_plan_limit").readOnly = readOnlyBool;
		document.getElementById("primary_plan_utilization").readOnly = readOnlyBool;
		document.getElementById("primary_available_limit").readOnly = readOnlyBool;
		document.getElementById("primary_visit_limit").readOnly = readOnlyBool;
		document.getElementById("primary_visit_deductible").readOnly = readOnlyBool;
		document.getElementById("primary_visit_copay").readOnly = readOnlyBool;
		document.getElementById("primary_max_copay").readOnly = readOnlyBool;
		if(document.getElementById("primary_perday_limit")!= null){
			document.getElementById("primary_perday_limit").readOnly = readOnlyBool;}
		document.getElementById("pd_primary_insuranceButton").disabled = readOnlyBool;
	}

	else if(spnsrIndex == 'S'){
		document.getElementById("secondary_plan_limit").readOnly = readOnlyBool;
		document.getElementById("secondary_plan_utilization").readOnly = readOnlyBool;
		document.getElementById("secondary_available_limit").readOnly = readOnlyBool;
		document.getElementById("secondary_visit_limit").readOnly = readOnlyBool;
		document.getElementById("secondary_visit_deductible").readOnly = readOnlyBool;
		document.getElementById("secondary_visit_copay").readOnly = readOnlyBool;
		document.getElementById("secondary_max_copay").readOnly = readOnlyBool;
		if(document.getElementById("secondary_perday_limit")!= null){
			document.getElementById("secondary_perday_limit").readOnly = readOnlyBool;}
		document.getElementById("pd_secondary_insuranceButton").disabled = readOnlyBool;
	}
}


function resetInsCompList(spnsrIndex) {

	var categoryId = '';
	if (document.mainform.patient_category_id)
		categoryId = document.mainform.patient_category_id.value;

	var tpaIdObj = null;
	var tpaIdWrapperObj = null;
	var insuCompIdObj = null;

	if (spnsrIndex == 'P') {
		tpaIdObj = getPrimarySponsorObj();
		tpaIdWrapperObj = document.getElementById("primary_sponsor_wrapper");
		insuCompIdObj = getPrimaryInsuObj();

	}else if (spnsrIndex == 'S') {
		tpaIdObj = getSecondarySponsorObj();
		tpaIdWrapperObj = document.getElementById("secondary_sponsor_wrapper");
		insuCompIdObj = getSecondaryInsuObj();
	}

	var insCompList = insuCompanyDetails; // the default set: all Ins Comps
	var defaultInsComp = "";
	var visitType = screenid == 'ip_registration'? 'i': 'o';

	if (categoryId != '') {
		// category is enabled, the list of Insurance Comps. is restricted
		for (var i = 0; i < categoryJSON.length; i++) {
			var item = categoryJSON[i];
			if (categoryId == item.category_id) {
				if (visitType == 'i') {
					if (!empty(item.ip_allowed_insurance_co_ids) && item.ip_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.ip_allowed_insurance_co_ids.split(',');
						var ip_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++)
						ip_allowedInsComps.push(findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]));
						// override the insCompList with allowed Ins Comps.
						insCompList =  !empty(ip_allowedInsComps) ? ip_allowedInsComps : insCompList;
					}
					if (spnsrIndex == 'P')
						defaultInsComp = item.primary_ip_insurance_co_id;
					else if (spnsrIndex == 'S')
						defaultInsComp = item.secondary_ip_insurance_co_id;
					break;
				 } else {
				 	if (!empty(item.op_allowed_insurance_co_ids) && item.op_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.op_allowed_insurance_co_ids.split(',');
						var op_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++)
						op_allowedInsComps.push(findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]));
						// override the insCompList with allowed Ins Comps.
						insCompList =  !empty(op_allowedInsComps) ? op_allowedInsComps : insCompList;
					}
					if (spnsrIndex == 'P')
						defaultInsComp = item.primary_op_insurance_co_id;
					else if (spnsrIndex == 'S')
						defaultInsComp = item.secondary_op_insurance_co_id;
					break;
				}
			}
		}
	}
	var item1;
	if(insuCompIdObj != null && !empty(insuCompIdObj.value)){
		item1 = findInList(insuCompanyDetails, "insurance_co_id", insuCompIdObj.value);
	}
	// Empty Ins Comps in ins company dropdown
	var index = 0;
	if (insuCompIdObj != null) {
		insuCompIdObj.length = 1;
		insuCompIdObj.options[index].text = "-- Select --";
		insuCompIdObj.options[index].value = "";
	}

	// Add all the allowed InsComps for patient category and insurance company.
	for (var i = 0; i < insCompList.length; i++) {
		var exists = false;
		var item = insCompList[i];
		for (var k = 0; k < insuCompanyDetails.length; k++) {
			var insItem = insuCompanyDetails[k];
			if (!empty(item) && !empty(insItem) && (item.insurance_co_id == insItem.insurance_co_id)) {
				exists = true;
				break;
			}
		}
		if (exists) {
			index++;
			if (insuCompIdObj != null) {
				insuCompIdObj.length = index + 1;
				insuCompIdObj.options[index].text = item.insurance_co_name;
				insuCompIdObj.options[index].value = item.insurance_co_id;
			}
		}
	}
	if(screenid != 'Edit_Insurance'){
		if(allowBillNowInsurance == 'true' || (document.mainform.bill_type != null && document.mainform.bill_type.value == 'C')){
			// if there is a default ins. company for patient category, set it
			if (insuCompIdObj != null) {
				if (!empty(defaultInsComp)) {
					tpaIdWrapperObj.checked = true;
					setSelectedIndex(insuCompIdObj, defaultInsComp);

				} else if (document.mainform.patient_category_id && insCompList.length == 1) {
					setSelectedIndex(insuCompIdObj, insCompList[0].insurance_co_id);
				}
				insuCompIdObj.removeAttribute("disabled");
				if(null != item1 && !empty(item1)){
					setSelectedIndex(insuCompIdObj, item1.insurance_co_id);
				}
			}

		} else {
			if (insuCompIdObj != null) {
				setSelectedIndex(insuCompIdObj, "");
				insuCompIdObj.disabled = true;
			}
		}
	}else if (screenid == 'Edit_Insurance'){
		insuCompIdObj.removeAttribute("disabled");
		if(null != item1 && !empty(item1)){
			setSelectedIndex(insuCompIdObj, item1.insurance_co_id);
		}
	}
}

function enableDisableDocumentUploader(sponserIndex){
	var tpaId;
	var item;
	if (sponserIndex=='P'){
		 tpaId=document.getElementById("primary_sponsor_id").value;
		 item = findInList(tpanames, "tpa_id", tpaId);
		 if(item.scanned_doc_required == 'N')
				document.getElementById("primaryOthersDetailsTab").style.display = 'none';
	}
	if (sponserIndex=='S'){
		tpaId=document.getElementById("secondary_sponsor_id").value;
		item = findInList(tpanames, "tpa_id", tpaId);
		if(item.scanned_doc_required == 'N')
				document.getElementById("secondaryOthersDetailsTab").style.display = 'none';
	}

}
