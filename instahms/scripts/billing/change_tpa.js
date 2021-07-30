var isMemberidValidated = false;
function validateDRGCodeWithPlan(spnsrIndex) {

	var useDRGObj = null;
	var drgCheckObj = null;
	var planObj = null;

	if (spnsrIndex == 'P') {
		useDRGObj = getPrimaryUseDRGObj();
		drgCheckObj = getPrimaryDRGCheckObj();
		planObj = getPlanObj('primary');

	} else if (spnsrIndex == 'S') {
		useDRGObj = getSecondaryUseDRGObj();
		drgCheckObj = getSecondaryDRGCheckObj();
		planObj = getPlanObj('secondary');
	}

	if (drgCheckObj && drgCheckObj.checked) {
		if (planObj && planObj.value == '') {
			showMessage("js.registration.patient.drg.code.required.is.only.for.plan.patient");
			drgCheckObj.focus();
			return false;
		}
	}
	return true;
}

function validatePerdiemCodeWithPlan(spnsrIndex) {

	var usePerdiemObj = null;
	var perdiemCheckObj = null;
	var planObj = null;

	if (spnsrIndex == 'P') {
		usePerdiemObj = getPrimaryUsePerdiemObj();
		perdiemCheckObj = getPrimaryPerdiemCheckObj();
		planObj = getPlanObj('primary');

	} else if (spnsrIndex == 'S') {
		usePerdiemObj = getSecondaryUsePerdiemObj();
		perdiemCheckObj = getSecondaryPerdiemCheckObj();
		planObj = getPlanObj('secondary');
	}

	if (perdiemCheckObj && perdiemCheckObj.checked) {
		if (planObj && planObj.value == '') {
			showMessage("js.registration.patient.perdiem.code.required.is.only.for.plan.patient");
			perdiemCheckObj.focus();
			return false;
		}
	}
	return true;
}


function validatePriorAuthId() {
	var priPlanObj = getPlanObj('primary');
	var priAuthIdObj = getPrimaryAuthIdObj();
	var priAuthModeIdObj = getPrimaryAuthModeIdObj();

	var secPlanObj = getPlanObj('secondary');
	var secAuthIdObj = getSecondaryAuthIdObj();
	var secAuthModeIdObj = getSecondaryAuthModeIdObj();

	if (isModAdvanceIns && priPlanObj != null && priPlanObj.value != ''
			&& !empty(priorAuthRequired) && trim(priAuthIdObj.value) == ""
					&& (priorAuthRequired == "A" || (priorAuthRequired == "I"
						&& pvisitType == "i") || (priorAuthRequired == "O" && pvisitType == "o"))) {
		showMessage("js.registration.patient.prior.auth.no.required");
		priAuthIdObj.focus();
		return false;
	}

	if (priAuthModeIdObj != null && !empty(priAuthModeIdObj.value)) {
		if (!validatePriorAuthMode(null, null, priAuthIdObj.name, priAuthModeIdObj.name))
			return false;
	}

	if (isModAdvanceIns && secPlanObj != null && secPlanObj.value != ''
			&& !empty(priorAuthRequired) && trim(secAuthIdObj.value) == ""
					&& (priorAuthRequired == "A" || (priorAuthRequired == "I"
						&& pvisitType == "i") || (priorAuthRequired == "O" && pvisitType == "o"))) {
		showMessage("js.registration.patient.prior.auth.no.required");
		secAuthIdObj.focus();
		return false;
	}

	if (secAuthModeIdObj != null && !empty(secAuthModeIdObj.value)) {
		if (!validatePriorAuthMode(null, null, secAuthIdObj.name, secAuthModeIdObj.name))
			return false;
	}

	return true;
}

function validateInsuranceCardRequired(spnsrIndex) {

	var memberIdObj = null;
	var planIdObj = null;
	var tpaObj = null;
	var docContentObj = null;
	var docNameObj = null;
	var policyEndDateObj = null;
	var fileHtmlId = '';
	var documentUpdateObj = null;
	var previousPlan = null;
	var previousMemberId = null;
	var previousEndDate = null;
	var previousPlan;
	var previousMemberId;
	var previousEndDate;
	var previousTpa;

	if (spnsrIndex == 'P') {
		memberIdObj = getInsuranceMemberIdObj('primary');
		planIdObj = getPlanObj('primary');
		tpaObj = getSponsorObj('primary');
		docContentObj = getPrimaryDocContentObj();
		docNameObj = getPrimaryDocNameObj();
		policyEndDateObj = getPolicyValidityEndObj('primary');
		documentUpdateObj = getPrimaryDocumentUsageObj();
		fileHtmlId = 'primary_sponsor_cardfileLocation' + document.getElementById("primary_sponsor").value;
		previousPlan=gPreviousPlan;
		previousMemberId=gPreviousMemberId;
		previousEndDate=gPreviousEndDate;
		previousTpa=gPreviousPrimaryTpa;

	} else if (spnsrIndex == 'S') {
		memberIdObj = getInsuranceMemberIdObj('secondary');
		planIdObj = getPlanObj('secondary');
		tpaObj = getSponsorObj('secondary');
		docContentObj = getSecondaryDocContentObj();
		docNameObj = getSecondaryDocNameObj();
		policyEndDateObj = getPolicyValidityEndObj('secondary');
		documentUpdateObj = getSecondaryDocumentUsageObj();
		fileHtmlId = 'secondary_sponsor_cardfileLocation' + document.getElementById("secondary_sponsor").value;
		previousPlan=gPreviousSecPlan;
		previousMemberId=gPreviousSecMemberId;
		previousEndDate=gPreviousSecEndDate;
		previousTpa=gPreviousSecondaryTpa;
	}

	if (planIdObj == null || memberIdObj == null) return true;

	var tpaId = (tpaObj != null) ? tpaObj.value : "";
	var tpa = findInList(tpanames, "tpa_id", tpaId);
	var insuranceCardMandatory = !empty(tpa) ? tpa.scanned_doc_required : "N";

	if (planIdObj != null && empty(planIdObj.value)) {
		if (!empty(docContentObj.value)) {
			docContentObj.value = "";
			alert(getString("js.registration.patient.please.select.a.plan"));
			return false;
		}
	}

	// If membership/policy details are corrected and card is not uploaded then existing image remains same.
	if (documentUpdateObj != null && documentUpdateObj.value == 'Update') {
		return true;
	}
	var op_type = pOpType;

	if (insuranceCardMandatory == 'R' && isModAdvanceIns && (!empty(planIdObj.value) || !empty(memberIdObj.value))) {
		if(null != tpa && tpa !=""){
			if (null != previousTpa && tpaId != previousTpa
						&& insuranceCardMandatory == 'R' && docContentObj.value == '' &&
						document.getElementById(fileHtmlId).value == ''){
				showMessage("js.registration.patient.insurance.card.required");
				docContentObj.focus();
				return false;
			}
			for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==tpa.sponsor_type_id){
					if(sponsorTypeList[i].member_id_show == 'Y' && sponsorTypeList[i].validity_period_show == 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '') {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan){
							if(memberIdObj.value == previousMemberId && (parseDateStr(policyEndDateObj.value).getTime() == new Date(previousEndDate).getTime()))
								return true;
							else if(((!empty(memberIdObj.value) && memberIdObj.value != previousMemberId) || (!empty(policyEndDateObj.value) &&
									(parseDateStr(policyEndDateObj.value).getTime() != new Date(previousEndDate).getTime()))) && insuranceCardMandatory == 'R'
									&& docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''){
								showMessage("js.registration.patient.insurance.card.required");
								docContentObj.focus();
								return false;
							}
						}
						else if(null != previousPlan && planIdObj.value !=previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}

					else if(sponsorTypeList[i].member_id_show == 'Y' && sponsorTypeList[i].validity_period_show != 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '') {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan){
							if(memberIdObj.value == previousMemberId)
								return true;
							else if(!empty(memberIdObj.value) && memberIdObj.value != previousMemberId && insuranceCardMandatory == 'R'
									&& docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''){
								showMessage("js.registration.patient.insurance.card.required");
								docContentObj.focus();
								return false;
							}
						}
						else if(null != previousPlan && planIdObj.value !=previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}

					else if(sponsorTypeList[i].member_id_show != 'Y' && sponsorTypeList[i].validity_period_show == 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '') {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value == previousPlan){
							if(parseDateStr(policyEndDateObj.value).getTime() == new Date(previousEndDate).getTime())
								return true;
							else if(!empty(policyEndDateObj.value) && (parseDateStr(policyEndDateObj.value).getTime() != new Date(previousEndDate).getTime())
									&& insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''){
								showMessage("js.registration.patient.insurance.card.required");
								docContentObj.focus();
								return false;
							}
						}
						else if(null != previousPlan && planIdObj.value !=previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}

					else if(sponsorTypeList[i].member_id_show != 'Y' && sponsorTypeList[i].validity_period_show != 'Y'){
						if (null == previousPlan && insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '') {
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
						else if (null != previousPlan && planIdObj.value != previousPlan
									&& insuranceCardMandatory == 'R' && docContentObj.value == '' &&
									document.getElementById(fileHtmlId).value == ''){
							showMessage("js.registration.patient.insurance.card.required");
							docContentObj.focus();
							return false;
						}
					}
					break;
				}
			}
		}
	}
	/*if (insuranceCardMandatory == 'R' && isModAdvanceIns
				&& (!empty(planIdObj.value) || !empty(memberIdObj.value))) {
		if (docNameObj.value == 'Insurance Card') {

			if (planIdObj.value == previousPlan
					&& memberIdObj.value == gPreviousMemberId
						&& parseDateStr(policyEndDateObj.value).getTime() == new Date(gPreviousEndDate).getTime()) {
				// do nothing if the previous plan is selected
				// and the membership validity is not expired.
				return true;
			}

			if (gPatientPolciyNos != null && gPatientPolciyNos != '') {
				for (var k = 0; k < gPatientPolciyNos.length; k++) {
					if (planIdObj.value == gPatientPolciyNos[k].plan_id && memberIdObj.value == gPatientPolciyNos[k].member_id
							&& parseDateStr(policyEndDateObj.value).getTime() == new Date(gPatientPolciyNos[k].policy_validity_end).getTime()) {
						// do nothing if one of the previous plans have been selected
						// and the membership validity is not expired.
						return true;
					}
				}
			}

			if (insuranceCardMandatory == 'R' && docContentObj.value == ''
										&& document.getElementById(fileHtmlId).value == '') {
				showMessage("js.registration.patient.insurance.card.required");
				docContentObj.focus();
				return false;
			}
		}
	}*/
	return true;
}

function validateScannedDocRequired() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var memberIdObj = null;
	var documentUpdateObj = null;
	var validatePrimary = true;
	var validateSecondary = true;


	if (primarySponsorObj != null && !primarySponsorObj.disabled && primarySponsorObj.value != "") {
		var tpaObj = getSponsorObj('primary');
		memberIdObj = getInsuranceMemberIdObj('primary');
		// Check if an existing sponsor details are being updated or new values are being inserted.
		if (primarySponsorObj.value == 'C') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(existingCorporatesList, "sponsor_id", tpaObj.value, "employee_id", memberIdObj.value);
				if (rec != null) {
					validatePrimary = false;
				}
			}
		} else if (primarySponsorObj.value == 'N') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(existingNationalsList, "sponsor_id", tpaObj.value, "national_id", memberIdObj.value);
				if (rec != null) {
					validatePrimary = false;
				}
			}
		} else if (primarySponsorObj.value == 'I') {
			validatePrimary = false;
		}


		if (tpaObj != null && tpaObj.value != "" && validatePrimary) {

			var tpaId = tpaObj.value;
			var tpa = findInList(tpanames, "tpa_id", tpaId);
			var insuranceCardMandatory = !empty(tpa) ? tpa.scanned_doc_required : "N";
			var docContentObj = getPrimaryDocContentObj();
			var fileHtmlId = 'primary_sponsor_cardfileLocation' + primarySponsorObj.value;

			if (insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''
				// If details are corrected and card is not uploaded then existing image remains same.
				&& documentUpdateObj != null && documentUpdateObj.value != 'Update') {
				showMessage("js.registration.patient.sponsor.required.scanned.doc");
				docContentObj.focus();
				return false;
			}
		}
	}

	if (secondarySponsorObj != null && !secondarySponsorObj.disabled && secondarySponsorObj.value != "") {

		var tpaObj = getSponsorObj('secondary');
		memberIdObj = getInsuranceMemberIdObj('secondary');
		documentUpdateObj = getPrimaryDocumentUsageObj();
		if (secondarySponsorObj.value == 'C') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(existingCorporatesList, "sponsor_id", tpaObj.value, "employee_id", memberIdObj.value);
				if (rec != null) {
					validateSecondary = false;
				}
			}
		} else if (secondarySponsorObj.value == 'N') {
			if (tpaObj != null && tpaObj.value != "") {
				var rec = findInList2(existingNationalsList, "sponsor_id", tpaObj.value, "national_id", memberIdObj.value);
				if (rec != null) {
					validateSecondary = false;
				}
			}
		} else if (secondarySponsorObj.value == 'I') {
			validateSecondary = false;
		}

		if (tpaObj != null && tpaObj.value != "" && validateSecondary) {
			memberIdObj = getInsuranceMemberIdObj('secondary');
			documentUpdateObj = getSecondaryDocumentUsageObj();

			// If details are corrected and card is not uploaded then existing image remains same.
			if (documentUpdateObj != null && documentUpdateObj.value == 'Update') {
				return true;
			}

			var tpaId = tpaObj.value;
			var tpa = findInList(tpanames, "tpa_id", tpaId);
			var insuranceCardMandatory = !empty(tpa) ? tpa.scanned_doc_required : "N";
			var docContentObj = getSecondaryDocContentObj();
			var fileHtmlId = 'secondary_sponsor_cardfileLocation' + secondarySponsorObj.value;

			if (insuranceCardMandatory == 'R' && docContentObj.value == '' && document.getElementById(fileHtmlId).value == ''
				// If details are corrected and card is not uploaded then existing image remains same.
				&& documentUpdateObj != null && documentUpdateObj.value != 'Update') {
				showMessage("js.registration.patient.sponsor.required.scanned.doc");
				docContentObj.focus();
				return false;
			}
		}
	}

	return true;
}

function validateSponsor(spnsrIndex) {
	var tpaObj = null;
	var spnsrText = "";
	if (spnsrIndex == 'P') spnsrText = getString("js.registration.patient.primary.sponsor");
	if (spnsrIndex == 'S') spnsrText = getString("js.registration.patient.secondary.sponsor");
	else {}

	var msg = spnsrText + " " + getString("js.common.is.required");

	if (spnsrIndex == 'P')
		tpaObj = getSponsorObj('primary');
	else if (spnsrIndex == 'S')
		tpaObj = getSponsorObj('secondary');

	if (tpaObj != null && tpaObj.value == "") {
		alert(msg);
		tpaObj.focus();
		return false;
	}
	return true;
}

function validateIdDetails(spnsrIndex) {
	var idValue = 0;
	var idObj = null;
	var msg = "";
	var tpaObj = getSponsorObj('primary');
	if (spnsrIndex == 'P') {
		var primarySponsorObj = document.getElementById("primary_sponsor");
		tpaObj = getSponsorObj('primary');
		if (primarySponsorObj != null) {
			if (primarySponsorObj.value == 'C') {
				idObj = document.getElementById("primary_employee_id");
				msg = getString("js.registration.patient.employee.id.required");
			} else if (primarySponsorObj.value == 'N') {
				idObj = document.getElementById("primary_national_member_id");
				msg = getString("js.registration.patient.member.id.required");
			}
		}
	} else if (spnsrIndex == 'S') {
		var secondarySponsorObj = document.getElementById("secondary_sponsor");
		tpaObj = getSponsorObj('secondary');
		if (secondarySponsorObj != null) {
			if (secondarySponsorObj.value == 'C') {
				idObj = document.getElementById("secondary_employee_id");
				msg = getString("js.registration.patient.employee.id.required");
			} else if (secondarySponsorObj.value == 'N') {
				idObj = document.getElementById("secondary_national_member_id");
				msg = getString("js.registration.patient.member.id.required");
			}
		}
	}

	if (tpaObj != null && tpaObj.value != "") {
		if (idObj != null && trim(idObj.value) == '') {
			alert(msg);
			idObj.focus();
			return false;
		}
	}
	return true;
}

function validateBillsApprovalLimit() {
	var priApprovalLimitObj = getApprovalLimitObj('primary');
	var secApprovalLimitObj = getApprovalLimitObj('secondary');
	var totalApprovalLimitPaise = 0;
	totalApprovalLimitPaise += (priApprovalLimitObj != null) ? getPaise(priApprovalLimitObj.value) : 0;
	totalApprovalLimitPaise += (secApprovalLimitObj != null) ? getPaise(secApprovalLimitObj.value) : 0;

	if (totalApprovalLimitPaise != 0 && getPaise(gPatientBillsApprovalTotal) > totalApprovalLimitPaise) {
		alert("Total of all bills approved amount (" + gPatientBillsApprovalTotal +
			") \nexceeds visit approved amount (" + formatAmountPaise(totalApprovalLimitPaise) + ") \n" +
			"Reduce approval amount in the bills and change approval limit.");
		return false;
	}
	return true;
}

function validateInsuranceApprovalAmount(spnsrIndex) {
	var insApprovalAmt = 0;
	var approvalLimitObj = null;
	if (spnsrIndex == 'P') {
		approvalLimitObj = getApprovalLimitObj('primary');
	} else if (spnsrIndex == 'S') {
		approvalLimitObj = getApprovalLimitObj('secondary');
	}

	if (approvalLimitObj != null) {
		if (approvalLimitObj.disabled || approvalLimitObj.readOnly) return true;
		if (trimAll(approvalLimitObj.value) == '') {
			var spnsrText = "";
			var msg = "";
			if (spnsrIndex == 'P') spnsrText = getString("js.registration.patient.primary.sponsor");
			if (spnsrIndex == 'S') spnsrText = getString("js.registration.patient.secondary.sponsor");

			var tpaObj = getSponsorObj('secondary');
			if (tpaObj != null && tpaObj.value != "") {
				msg += " " + spnsrText + " " + getString("js.registration.patient.warning");;
			}
			msg += " " + getString("js.registration.patient.approval.amount.is.given.string") + " \n";
			msg += " " + getString("js.registration.patient.warning.validations.against.approval.amount.will.be.disabled.string") + " \n";
			msg += " " + getString("js.registration.patient.for.blanket.approvals.specify.amount.as.zero.string") + " \n";
			msg += " " + getString("js.registration.patient.do.you.want.tocontinue.to.save.string") + " \n";

			var ok = confirm(msg);
			if (!ok) {
				approvalLimitObj.focus();
				return false;
			} else return true;
		}
		formatAmountObj(approvalLimitObj, false);
		insApprovalAmt = getPaise(approvalLimitObj.value);
	}

	var approvalMsg = getString("js.registration.patient.approval.amount.must.be.a.valid.amount");
	// Ins30 : Corporate insurance validations removed
	/*if(corpInsuranceCheck != 'Y' && approvalLimitObj != null){
		if (!validateAmount(approvalLimitObj, approvalMsg))
		return false;
		}
	*/
	if(approvalLimitObj != null){
		if (!validateAmount(approvalLimitObj, approvalMsg))
		return false;
		}

	if (insApprovalAmt >= 0) return true;
	else return false;
}

function isRatePlanActive(ratePlan) {
	if (empty(ratePlan)) return true;

	var org = findInList(orgNamesJSON, "org_id", ratePlan);
	if (org != null && org.status == 'A') {
		return true;
	}
	return false;
}

function isInsCompanyActive(insCompObj) {
	if (insCompObj == null) return true;
	var insComp = insCompObj.value;
	if (empty(insComp)) return true;

	var inscomp = findInList(insuCompanyDetails, "insurance_co_id", insComp);
	if (inscomp != null && inscomp.status == 'A') {
		return true;
	}
	return false;
}

function isTpaActive(tpaIdObj) {
	if (tpaIdObj == null) return true;
	var tpaId = tpaIdObj.value;
	if (empty(tpaId)) return true;

	var tpa = findInList(tpanames, "tpa_id", tpaId);
	if (tpa != null && tpa.status == 'A') {
		return true;
	}
	return false;
}

function isPlanTypeActive(planTypeObj) {
	if (planTypeObj == null) return true;
	var planType = planTypeObj.value;
	if (empty(planType)) return true;

	var plantype = findInList(insuCatNames, "category_id", planType);
	if (plantype != null && plantype.status == 'A') {
		return true;
	}
	return false;
}

function isPlanActive(planIdObj) {
	if (planIdObj == null) return true;
	var planId = planIdObj.value;
	if (empty(planId)) return true;

	var plan = findInList(policynames, "plan_id", planId);
	if (plan != null && plan.status == 'A') {
		return true;
	}
	return false;
}


function validateInsuranceFields(spnsrIndex) {

	if (!validateSponsor(spnsrIndex)) return false;


	var priInsCompObj = getInsuObj('primary');
	var priTpaObj = getSponsorObj('primary');
	var priPlanObj = getPlanObj('primary');
	var priPlanTypeObj = getPlanTypeObj('primary');
	var priMemberIdObj = getInsuranceMemberIdObj('primary');

	var secInsCompObj = getInsuObj('secondary');
	var secTpaObj = getSponsorObj('secondary');
	var secPlanObj = getPlanObj('secondary');
	var secPlanTypeObj = getPlanTypeObj('secondary');
	var secMemberIdObj = getInsuranceMemberIdObj('secondary');

		if(spnsrIndex == 'P' && (priTpaObj != null && !empty(priTpaObj))
			&& (priInsCompObj != null && priInsCompObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.InsuranceCompany.required");
			priInsCompObj.focus();
			return false;
		}

		if (spnsrIndex == 'P' && (priInsCompObj != null
				&& priInsCompObj.selectedIndex != 0)
					&& (priPlanTypeObj != null && priPlanTypeObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.network.plantype.name.required");
			priPlanTypeObj.focus();
			return false;
		}

		if (spnsrIndex == 'P' && (priPlanObj != null
				&& priPlanObj.selectedIndex == 0 && priPlanTypeObj.selectedIndex != 0)) {
			showMessage("js.registration.patient.plan.name.required");
			priPlanObj.focus();
			return false;
		}

		if(spnsrIndex == 'S' && (secTpaObj != null && !empty(secTpaObj))
			&& (secInsCompObj != null && secInsCompObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.InsuranceCompany.required");
			secInsCompObj.focus();
			return false;
		}

		if (spnsrIndex == 'S' && (secInsCompObj != null
				&& secInsCompObj.selectedIndex != 0)
					&& (secPlanTypeObj != null && secPlanTypeObj.selectedIndex == 0)) {
			showMessage("js.registration.patient.network.plantype.name.required");
			secPlanTypeObj.focus();
			return false;
		}

		if (spnsrIndex == 'S' && (secPlanObj != null
					&& secPlanObj.selectedIndex == 0 && secPlanTypeObj.selectedIndex != 0)) {
			showMessage("js.registration.patient.plan.name.required");
			secPlanObj.focus();
			return false;
		}
		if ((priPlanObj != null && priPlanObj.value != "") || (secPlanObj != null && secPlanObj.value != "")) {

			var memberIdRequiredMsg = memberIdLabel + " " + getString("js.common.is.required");
			var memberIdValidFromRequiredMsg = memberIdValidFromLabel + " " + getString("js.common.is.required");
			var memberIdValidToRequiredMsg = memberIdValidToLabel + " " + getString("js.common.is.required");

			if (spnsrIndex != null) {
				if(!isMemberidValidated){
					if(!checkForMemberID(spnsrIndex)){
			 			return false;
					}
				}
			}

			if(!validateMemberIdRequired(spnsrIndex))
				return false;

			if(!validatePlanPeriodRequired(spnsrIndex))
				return false;

			if(!validatePolicyDetailsRequired(spnsrIndex))
				return false;

			if(!checkForVisitCopayLimit(spnsrIndex))
					return false;

			if (spnsrIndex == 'P') {
				if (!validateDRGCodeWithPlan(spnsrIndex)) {
					return false;
				}

				if (!validatePerdiemCodeWithPlan(spnsrIndex)) {
					return false;
				}

				if (!validateInsuranceCardRequired(spnsrIndex)) {
					return false;
				}

				if (!validateScannedDocRequired()) {
					return false;
				}
			}

			if (spnsrIndex == 'S') {

				if (!validateDRGCodeWithPlan(spnsrIndex)) {
					return false;
				}

				if (!validatePerdiemCodeWithPlan(spnsrIndex)) {
					return false;
				}

				if (!validateInsuranceCardRequired(spnsrIndex)) {
					return false;
				}

				if (!validateScannedDocRequired()) {
					return false;
				}
			}
		}

	if (spnsrIndex == 'P') {

		if (document.getElementById("primary_sponsor").value != "") {
			var tpaObj = getSponsorObj('primary');
			if (tpaObj != null && tpaObj.value != "") {

				if (!validateIdDetails('P')) {
					return false;
				}
				if (!validateInsuranceApprovalAmount('P')) {
					return false;
				}
			}
		}

		if (!isInsCompanyActive(priInsCompObj)) {
			showMessage("js.registration.patient.insurance.company.is.inactive");
			priInsCompObj.focus();
			return false;
		}

		if (!isTpaActive(priTpaObj)) {
			showMessage("js.registration.patient.tpa.is.inactive");
			priTpaObj.focus();
			return false;
		}

	}


	if (spnsrIndex == 'S') {

		if (document.getElementById("secondary_sponsor").value != "") {
			var tpaObj = getSponsorObj('secondary');
			if (tpaObj != null && tpaObj.value != "") {
				if (!validateIdDetails('S')) {
					return false;
				}

				if (!validateInsuranceApprovalAmount('S')) {
					return false;
				}
			}
		}

		if (!isInsCompanyActive(secInsCompObj)) {
			showMessage("js.registration.patient.insurance.company.is.inactive");
			secInsCompObj.focus();
			return false;
		}

		if (!isTpaActive(secTpaObj)) {
			showMessage("js.registration.patient.tpa.is.inactive");
			secTpaObj.focus();
			return false;
		}
	}

	if (isModAdvanceIns) {

		if (spnsrIndex == 'P') {
			if (!isPlanTypeActive(priPlanTypeObj)) {
				showMessage("js.registration.patient.network.plan.type.is.inactive");
				planTypepriPlanTypeObjObj.focus();
				return false;
			}

			if (!isPlanActive(priPlanObj)) {
				showMessage("js.registration.patient.plan.is.inactive");
				priPlanObj.focus();
				return false;
			}
		}

		if (spnsrIndex == 'S') {
			if (!isPlanTypeActive(secPlanTypeObj)) {
				showMessage("js.registration.patient.network.plan.type.is.inactive");
				secPlanTypeObj.focus();
				return false;
			}

			if (!isPlanActive(secPlanObj)) {
				showMessage("js.registration.patient.plan.is.inactive");
				secPlanObj.focus();
				return false;
			}
		}

	}

	return true;
}

function hasPlanVisitCopayLimit(planId) {
	var plan = findInList(policynames, "plan_id", planId);
	var hasVisitCopayLimit = false;
	if (empty(plan))
		return hasVisitCopayLimit;

	hasVisitCopayLimit = ((!empty(document.getElementById("primary_max_copay").value) && getPaise(document.getElementById("primary_max_copay").value) != 0)||
		(!empty(document.getElementById("secondary_max_copay").value) && getPaise(document.getElementById("secondary_max_copay").value) != 0))

	/*var visitType = visit_type;
	if (visitType == 'o')
		hasVisitCopayLimit = (!empty(plan.op_visit_copay_limit) && getPaise(plan.op_visit_copay_limit) != 0);

	else if (visitType == 'i')
		hasVisitCopayLimit = (!empty(plan.ip_visit_copay_limit) && getPaise(plan.ip_visit_copay_limit) != 0);*/

	return hasVisitCopayLimit;
}

function validateMultiSponsorForPlanWithCopay() {
	if (document.getElementById("secondary_sponsor").value != "") {
		var planObj = getPlanObj('secondary');
		if (planObj == null)
			planObj = getPlanObj('primary');

		if (planObj != null) {
			var planId = planObj.value;
			var hasVisitCopayLimit = hasPlanVisitCopayLimit(planId);
			if (hasVisitCopayLimit) {
				var msg = getString("js.registration.patient.plan.has.visit.copay.string");
				msg += " \n";
				msg += getString("js.registration.patient.secondary.sponsor");
				msg += " "+	getString("js.registration.patient.is.not.allowed.string");

				alert(msg);
				document.getElementById("secondary_sponsor").focus();
				return false;
			}
		}
	}
	return true;
}

function validateBillsOrInsuranceRemoval() {

	var existingTPAObj = document.getElementById('existing_tpa_id');
	var billsrequired = document.getElementById('bills_to_change_sponsor_amounts');
	var existingPlanObj = document.getElementById('existing_plan_id');

	var existingDrgCheckObj = document.getElementById('existing_use_drg');
	var existingPerdiemCheckObj = document.getElementById('existing_use_perdiem');

	var existingRatePlanObj = document.getElementById('rate_plan');
	var existingRatePlan = existingRatePlanObj.value;

	var ratePlanObj = document.mainform.organization;
	var ratePlan = ratePlanObj.value;

	var planObj = null;
	var tpaObj = null;
	var drgCheckObj = null;
	var perdiemCheckObj = null;
	var spnsrIndex = getMainSponsorIndex();
	if (spnsrIndex == 'P') {
		planObj = getPlanObj('primary');
		tpaObj = getSponsorObj('primary');
		drgCheckObj = getPrimaryDRGCheckObj();
		perdiemCheckObj = getPrimaryPerdiemCheckObj();
	} else if (spnsrIndex == 'S') {
		planObj = getPlanObj('secondary');
		tpaObj = getSponsorObj('secondary');
		drgCheckObj = getSecondaryDRGCheckObj();
		perdiemCheckObj = getSecondaryPerdiemCheckObj();
	}

	var existingTpaId = (existingTPAObj != null) ? existingTPAObj.value : "";
	var tpaId = (tpaObj != null) ? tpaObj.value : "";

	var existingPlanId = existingPlanObj.value;
	var planId = (planObj != null) ? planObj.value : 0;

	var existingUseDRG = (existingDrgCheckObj != null) ? existingDrgCheckObj.value : "N";
	var useDRG = (drgCheckObj != null) ? drgCheckObj.value : "N";

	var existingUsePerdiem = (existingPerdiemCheckObj != null) ? existingPerdiemCheckObj.value : "N";
	var usePerdiem = (perdiemCheckObj != null) ? perdiemCheckObj.value : "N";

	var visitLimits = document.getElementById("visitLimitsChanged").value;
	var categoryLimitsEdited = document.getElementById('insEdited').value;

	if (existingTpaId == tpaId && existingPlanId == planId
			&& existingRatePlan == ratePlan
				&& existingUseDRG == useDRG && existingUsePerdiem == usePerdiem
				&& visitLimits == 'N' && categoryLimitsEdited == false) {
		var ok = confirm("No changes in sponsor/plan/rate plan details. \nNo Bills claim amounts will be effected.");
		if (!ok) return false;
		else return true;
	}

	var finalizedTPABills = 0;
	var closedTPABills = 0;
	var paidTPABills = 0;
	var notOpenBills = 0;
	var totalTPABills = 0;
	var openTPABills = 0;

	if (!empty(tpaBillsJSON)) {
		totalTPABills = tpaBillsJSON.length;
		for (var i = 0; i < tpaBillsJSON.length; i++) {
			var bill = tpaBillsJSON[i];
			if (bill.payment_status == 'P')
				paidTPABills += 1;
			if (bill.status == 'F')
				finalizedTPABills += 1;
			if (bill.status == 'C')
				closedTPABills += 1;
			if (bill.status != 'A')
				notOpenBills += 1;
			if(bill.status == 'A')
				openTPABills +=1;
		}
	}

	var primarySponsorObj = document.getElementById('primary_sponsor');
	var secondarySponsorObj = document.getElementById('secondary_sponsor');

	var msg = " There are : " + totalTPABills + " TPA bills for this patient. \n ";
	if (finalizedTPABills > 0) msg = msg + finalizedTPABills + " - Finalized" + ", ";
	if (closedTPABills > 0) msg = msg + closedTPABills + " - Closed" + ", ";

	if (totalTPABills > 0 && (paidTPABills > 0 || notOpenBills > 0) && existingTpaId != ""
				&& (primarySponsorObj != null && primarySponsorObj.value == "")) {
		alert(msg + "\n\n" + " TPA cannot be removed as patient \n"
				+ " has some paid/finalized/closed TPA bills. \n"
				+ " Please reopen finalized/closed bills, mark paid bills as unpaid and remove TPA.");
		return false;
	}

	if (totalTPABills > 0) {
		if (notOpenBills > 0) {
			if (billsrequired.value == 'open_bills') {
				var ok = confirm(msg + "\n\n"
						+ " Warning : Adding/Editing Insurance will effect only the Open TPA bill(s). \n"
						+ " There are some finalized/closed TPA bills for the patient. \n"
						+ " claim amounts will not be changed in these. \n"
						+ " Are you sure you want to change the Insurance details?");
				if (!ok) return false;

			} else if (billsrequired.value == 'all_bills') {
				var ok = confirm(msg + "\n\n"
						+ " Warning : There are some finalized/closed TPA bills for the patient. \n"
						+ " These will be reopened if claim is not Sent \n"
						+ " and claim amounts will be changed. \n\n"
						+ " Adding/Editing Insurance will effect all the Open TPA bill(s). \n"
						+ " Do you want to continue to save?");
				if (!ok) return false;
			}
		}

		if (billsrequired.value == 'none') {
			if(openTPABills > 0){
				var ok = confirm(msg + "\n\n" + " Warning : Insurance amounts will change in open bills, But rates will not be changed.\n" + " Do you want to continue to save? ");
				if (!ok) return false;
			}else{
				var ok = confirm(msg + "\n\n" + " Warning : No bills will be effected by Insurance changes. \n" + " Only the Insurance details will be changed. \n" + " Do you want to continue to save? ");
				if (!ok) return false;
			}
		}
	}
	return true;
}

function followUpValidate() {

	if (document.getElementById("primary_sponsor").value != "") {
		var tpaObj = getSponsorObj('primary');
		if (tpaObj != null && tpaObj.value != "") {

			if (!validateInsuranceApprovalAmount('P')) {
				return false;
			}
		}
	}
	if (document.getElementById("secondary_sponsor").value != "") {
		var tpaObj = getSponsorObj('secondary');
		if (tpaObj != null && tpaObj.value != "") {

			if (!validateInsuranceApprovalAmount('S')) {
				return false;
			}
		}
	}

	if (!validateBillsApprovalLimit())
		return false;
	if (document.getElementById("primary_sponsor").value != "") {
		if(!checkForVisitCopayLimit('P'))
			return false;
	}
	if (document.getElementById("secondary_sponsor").value != "") {
		if(!checkForVisitCopayLimit('S'))
			return false;
	}
	disableOrEnableInsuranceFields(false, false);
	document.mainform._method.value = "updateFollowUpApproval";
	document.mainform.submit();
	return true;
}

function validate() {

	var priInsCompObj = getInsuObj('primary');
	var priTpaObj = getSponsorObj('primary');

	var secInsCompObj = getInsuObj('secondary');
	var secTpaObj = getSponsorObj('secondary');

	var priPlanTypeObj =getPlanTypeObj('primary');
	var secPlanTypeObj =getPlanTypeObj('secondary');

	var priPlanObj = getPlanObj('primary');
	var secPlanObj = getPlanObj('secondary');

	if (priInsCompObj != null && priInsCompObj.selectedIndex != 0
					&& priTpaObj != null && empty(priTpaObj.value)) {
		showMessage("js.registration.patient.sponsor.required");
		priTpaObj.focus();
		return false;
	}
	if (secInsCompObj != null && secInsCompObj.selectedIndex != 0
					&& secTpaObj != null && empty(secTpaObj.value)) {
		showMessage("js.registration.patient.sponsor.required");
		secTpaObj.focus();
		return false;
	}

	if (document.getElementById('organization').value == '') {
		showMessage("js.registration.patient.rate.plan.required");
		document.getElementById('organization').focus();
		return false;
	}

	//validate if sponsor type has been selected but sponsor has not been selected
	var primarySponsorObj = document.getElementById('primary_sponsor');
	var secondarySponsorObj = document.getElementById('secondary_sponsor');

	if (primarySponsorObj != null && primarySponsorObj.value != '') {
		if (priTpaObj != null && empty(priTpaObj.value)) {
			var msg = getString("js.registration.patient.primary.sponsor") +
				" " + getString("js.registration.patient.is.required.string");
			alert(msg);
			priTpaObj.focus();
			return false;
		}
	}

	if(priTpaObj != null && !empty(priTpaObj.value) &&
					priInsCompObj != null && priInsCompObj.selectedIndex == 0){
		showMessage("js.registration.patient.InsuranceCompany.required");
		priInsCompObj.focus();
		return false;
	}

	if(priTpaObj != null && !empty(priTpaObj.value) &&
					priPlanTypeObj != null && priPlanTypeObj.selectedIndex == 0){
		showMessage("js.registration.patient.PlanType.required");
		priPlanTypeObj.focus();
		return false;
	}

	if(priTpaObj != null && !empty(priTpaObj.value) &&
					priPlanObj != null && priPlanObj.selectedIndex == 0){
		showMessage("js.registration.patient.Plan.required");
		priPlanObj.focus();
		return false;
	}

	if (secondarySponsorObj != null && secondarySponsorObj.value != '') {
		if (secTpaObj != null && empty(secTpaObj.value)) {
			var msg = getString("js.registration.patient.secondary.sponsor") +
				" " + getString("js.registration.patient.is.required.string");
			alert(msg);
			secTpaObj.focus();
			return false;
		}
	}

	if(secTpaObj != null && !empty(secTpaObj.value) &&
					secInsCompObj != null && secInsCompObj.selectedIndex == 0){
		showMessage("js.registration.patient.InsuranceCompany.required");
		secInsCompObj.focus();
		return false;
	}

	if(secTpaObj != null && !empty(secTpaObj.value) &&
					secPlanTypeObj != null && secPlanTypeObj.selectedIndex == 0){
		showMessage("js.registration.patient.PlanType.required");
		secPlanTypeObj.focus();
		return false;
	}

	if(secTpaObj != null && !empty(secTpaObj.value) &&
					secPlanObj != null && secPlanObj.selectedIndex == 0){
		showMessage("js.registration.patient.Plan.required");
		secPlanObj.focus();
		return false;
	}

	setOtherInsObjects();

	if (!validateInsuranceFields('P'))
		return false;

	if (!validateInsuranceFields('S'))
		return false;

	if (!validatePlan()) return false;

	if (!validateMemberId()) return false;

	//if (!validateMultiSponsorForPlanWithCopay()) return false;

	if (!validateBillsApprovalLimit())
		return false;

	if(!validateBills()) return false;

	if (!validateBillsOrInsuranceRemoval())
		return false;

	// Ins30 : Corporate insurance validations removed
	/*
	if(corpInsuranceCheck == "Y"){
		if (!setSelectedDateForCorpInsurance()) {
	    	alert(getString("js.registration.patient.primary.policy.date.na.validation"));
			document.getElementById("primary_plan_id").focus();
    		return false;
    	}

    	if(!checkCurrentDateWithEndDateForCorpInsurance()) return false;
	}
	*/
	if (isModAdvanceIns) {
		if (!validatePriorAuthId()) {
			return false;
		}
	}

	if (!allowMultiplePlansForMultipleTPAbills()) return false;

	if(document.getElementById("visitLimitsChanged").value == "Y"){
		if(document.getElementById('bills_to_change_sponsor_amounts').value == 'open_bills' && tpaBillsJSON.length >1 && (visitType != 'i' && (visitType == 'o' && pOpType != 'O'))){
			var ok = confirm(getString("js.registration.patient.warning.Open.bills.override.visit.limit.values.in.follow.ups"));

			if (!ok) {
				return false;
			}
		}

		if(document.getElementById('bills_to_change_sponsor_amounts').value == 'all_bills' && tpaBillsJSON.length >1 && (visitType != 'i' && (visitType == 'o' && pOpType != 'O'))){
			var ok = confirm(getString("js.registration.patient.warning.All.bills.override.visit.limit.values.in.follow.ups"));

			if (!ok) {
				return false;
			}
		}

		if(document.getElementById('bills_to_change_sponsor_amounts').value == 'all_bills' && tpaBillsJSON.length >1 && (visitType == 'i' || (visitType == 'o' && pOpType == 'O'))){
			var ok = confirm(getString("js.registration.patient.warning.All.bills.override.visit.limit.values.in.IpOps"));

			if (!ok) {
				return false;
			}
		}

		if(document.getElementById('bills_to_change_sponsor_amounts').value == 'open_bills' && tpaBillsJSON.length >1 && (visitType == 'i' || (visitType == 'o' && pOpType == 'O'))){
			var ok = confirm(getString("js.registration.patient.warning.Open.bills.override.visit.limit.values.in.IpOps"));

			if (!ok) {
				return false;
			}
		}

	}
	//If there are credit notes existing for this visit, they will be cancelled
	if(creditNoteList.length > 0 && creditNoteList != null && creditNoteList != '[]'){
		var ok = confirm("The following credit notes will be cancelled as insurance details are being updated" +
				creditNoteList + ". Kindly note down all credit note " +
				"details and recreate after changing insurance details. " +
				"Do you wish to proceed with the above changes?");
	if (!ok)
		return false;
	}
	if(visit_type == 'o' && oldEditIns == 'false')
		document.mainform._method.value = "updateInsurance";
	else
		document.mainform._method.value = "updateTpa";
	
	document.mainform.submit();
	return true;
}

function validateBills() {
	var pPlanExists = false;
	var sPlanExists = false;
	var billsrequired = document.getElementById('bills_to_change_sponsor_amounts');
	var priPlanId = document.getElementById("primary_plan_id");
	var secPlanId = document.getElementById("secondary_plan_id");

	if(null != priPlanId && priPlanId.value != '') pPlanExists = true;
	if(null != secPlanId && secPlanId.value != '') sPlanExists = true;
	if (billsrequired.value == 'open_bills') {
		if(priPlanExisits != pPlanExists || secPlanExisits != sPlanExists) {
			alert("Adding/Deleting plan is not allowed when only open bills option is selected.\nPlease select other options.");
			return false;
		}
	}
	if (billsrequired.value == 'none') {
		if(priPlanExisits != pPlanExists || secPlanExisits != sPlanExists) {
			alert("Adding/Deleting plan is not allowed when none option is selected.\nPlease select other options.");
			return false;
		}
	}
	return true;
}


function init() {
	gSelectedPatientCategory = patientCategory;

	ajaxDetails = getDetailsAjax();
    //policynames = ajaxDetails.policynames;
    companyTpaList = ajaxDetails.companyTpaList;
    comTpaListAll = ajaxDetails.insCompTpaListAll;
    insuCompanyDetails = ajaxDetails.insuranceCompaniesLists;


	setPlanDetails();
	enableRightsTochangeDynamicCopay();
	initPrimaryInsurancePhotoDialog();
	initSecondaryInsurancePhotoDialog();

	if (isModAdvanceIns) {
		policyNoAutoComplete('S', gPatientPolciyNos);
		policyNoAutoComplete('P', gPatientPolciyNos);
	}

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var primarySponsorWrapperObj = document.getElementById("primary_sponsor_wrapper");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");
	primarySponsorObj.value = gPreviousPrimarySponsorIndex;
	if(primarySponsorObj.value!='')
		primarySponsorWrapperObj.checked = true;

	onChangePrimarySponsor();
	secondarySponsorObj.value = gPreviousSecondarySponsorIndex;
	if(!empty(secondarySponsorObj.value))
		secondarySponsorWrapperObj.checked = true;
	onChangeSecondarySponsor();

	loadInsuranceCompList();
	loadInsurancePolicyDetails();

	var prevRatePlan = gInitRatePlan;
	if (prevRatePlan == null || prevRatePlan == '') {
		prevRatePlan = 'ORG0001';
	}
	var organizationObj = document.mainform.organization;
	setSelectedIndex(organizationObj, prevRatePlan);

	setPriOrSecPlanExists();
	insuPrimaryViewDoc();
	insuSecondaryViewDoc();

	// Ins30 : Corporate insurance validations removed
/*	if(corpInsuranceCheck == "Y")
		setSelectedDateForCorpInsurance(); */
	if ( gPrimaryPolicyStatus == 'I' )
		enableDisablePrimaryPolicyDetails(true,true);

	if ( gSecPolicyStatus == 'I' )
		enableDisableSecondaryPolicyDetails(true,true);

	initInsurancePlanDetailsDialog();
}

var priPlanExisits = false;
var secPlanExisits = false;
function setPriOrSecPlanExists(){
	var primaryPlanId = document.getElementById("primary_plan_id");
	var secondaryPlanId = document.getElementById("secondary_plan_id");
	if(null != primaryPlanId && primaryPlanId.value != '') priPlanExisits = true;
	if(null != secondaryPlanId && secondaryPlanId.value != '') secPlanExisits = true;
}


/*
 * Brings certain larger details via ajax to prevent page load slowdown.
 */
function getDetailsAjax() {
	var ajaxobj = newXMLHttpRequest();
	var url = null;
	url = cpath + "/editVisit/changeTPA.do?_method=getdetailsAJAX&patient_category="+patientCategory+
		"&visit_type="+visit_type;
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return JSON.parse(ajaxobj.responseText);
		}
	}
}

function loadInsuranceCompList(sponsorIndex) {

	var categoryId = patientCategory;

	var priTpaObj = getSponsorObj('primary');
	var secTpaObj = getSponsorObj('secondary');
	var priInsCompObj = getInsuObj('primary');
	var secInsCompObj = getInsuObj('secondary');

	var insCompList = insuCompanyDetails; // the default set: all Ins Comps
	var defaultPriInsComp = "";
	var defaultSecInsComp = "";
	var visitType = visit_type;

	if (categoryId != '') {
		// category is enabled, the list of Insurance Comps. is restricted
		for (var i = 0; i < categoryJSON.length; i++) {
			var item = categoryJSON[i];
			if (categoryId == item.category_id) {
				if (visitType == 'i') {
					if (!empty(item.ip_allowed_insurance_co_ids) && item.ip_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.ip_allowed_insurance_co_ids.split(',');
						var ip_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++) {
							var insurance_details = findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]);
							if (insurance_details != null) {
								ip_allowedInsComps.push(findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]));
							}
						}
						// override the insCompList with allowed Ins Comps.
						insCompList = !empty(ip_allowedInsComps) ? ip_allowedInsComps : insCompList;
					}

					defaultPriInsComp = item.primary_ip_insurance_co_id;
					defaultSecInsComp = item.secondary_ip_insurance_co_id;

					break;
				} else {
					if (!empty(item.op_allowed_insurance_co_ids) && item.op_allowed_insurance_co_ids != '*') {
						var insCompIdList = item.op_allowed_insurance_co_ids.split(',');
						var op_allowedInsComps = [];
						for (var i = 0; i < insCompIdList.length; i++) {
							var insuranceDetails = findInList(insuCompanyDetails, "insurance_co_id", insCompIdList[i]);
							if (insuranceDetails != null) {
								op_allowedInsComps.push(insuranceDetails);								
							}
						}
						// override the insCompList with allowed Ins Comps.
						insCompList = !empty(op_allowedInsComps) ? op_allowedInsComps : insCompList;
					}

					defaultPriInsComp = item.primary_op_insurance_co_id;
					defaultSecInsComp = item.secondary_op_insurance_co_id;

					break;
				}
			}
		}
	}
	var item1;
	if(priInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'P') && !empty(priInsCompObj.value)){
		item1 = findInList(insuCompanyDetails, "insurance_co_id", priInsCompObj.value);
	}
	if(secInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'S') && !empty(secInsCompObj.value)){
		item1 = findInList(insuCompanyDetails, "insurance_co_id", secInsCompObj.value);
	}

	// Empty Ins Comps in ins company dropdown
	var index = 0;
	if (priInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'P')) {
		priInsCompObj.length = 1;
		priInsCompObj.options[index].text = "-- Select --";
		priInsCompObj.options[index].value = "";
	}

	if (secInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'S')) {
		secInsCompObj.length = 1;
		secInsCompObj.options[index].text = "-- Select --";
		secInsCompObj.options[index].value = "";
	}
	//insCompList=sortByKey(insCompList,"insurance_co_name");
	//insuCompanyDetails=sortByKey(insuCompanyDetails,"insurance_co_name");
	insCompList=sortByKey(insCompList,"insurance_co_id");
	insuCompanyDetails=sortByKey(insuCompanyDetails,"insurance_co_id");

	//Removed for Registration performance Improvement in 11.3, However we can probably do away with this check itself. The reasons:
	//1) If the companies associated with a patient category is *, it means all so no check is required.
	//2) If the companies associated are a selected few, we have seen that the insurance companies that are so associated with a patient category cannot be deactivated. Therefore, again the items in insCompList will be part of insuCompanyDetails.
	//This means that the loop used to check for this is not required
	// Add all the allowed InsComps for patient category and insurance company.
	//var startIndex = 0;
	// insuCompIdObjOptions = array containing insurance companies from insCompList that exist in insuCompanyDetails
	// insCompList = insurance companies allowed for the patient category
	// insuCompanyDetails = insurance companies that are active
	/*var insuCompIdObjOptions = [];
	for (var i = 0; i < insCompList.length; i++) {
		var exists = false;
		var item = insCompList[i];
		//for (var k = 0; k < insuCompanyDetails.length; k++) {
		for (var k = startIndex; k < insuCompanyDetails.length; k++) {
			var insItem = insuCompanyDetails[k];
			if (!empty(item) && !empty(insItem) && (item.insurance_co_id == insItem.insurance_co_id)) {
				exists = true;
				startIndex = k + 1; // Since it is sorted, the next match will only be after the current match
				insuCompIdObjOptions.push(item);
				index++;
				break;
			}
		}*/
		// Code moved out of the loop to sort by name before adding to the dropdown
		/*if (exists) {
			index++;
			if (insuCompIdObj != null) {
				insuCompIdObj.length = index + 1;
				insuCompIdObj.options[index].text = item.insurance_co_name;
				insuCompIdObj.options[index].value = item.insurance_co_id;
			}
		}*/
	//}

	//Add to the dropdown
	////Changed for Registration performance Improvement in 11.3
	if (priInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'P')){
		priInsCompObj.length = insCompList.length + 1;
		for (i=0; i < insCompList.length; i++){
			var insCo = insCompList[i];
			if (insCo != null) {
				priInsCompObj.options[i+1].text = insCo.insurance_co_name;
				priInsCompObj.options[i+1].value = insCo.insurance_co_id;
			}
		}
	}

	if (secInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'S')){
		secInsCompObj.length = insCompList.length + 1;
		for (i=0; i < insCompList.length; i++){
			var insCo = insCompList[i];
			if (insCo != null) {
				secInsCompObj.options[i+1].text = insCo.insurance_co_name;
				secInsCompObj.options[i+1].value = insCo.insurance_co_id;
			}
		}
	}

	if(null != item1 && !empty(item1)){
		if (priInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'P')) {
			setSelectedIndex(priInsCompObj, item1.insurance_co_id);
		}
		if (secInsCompObj != null && (sponsorIndex == null || sponsorIndex == 'S')) {
				setSelectedIndex(secInsCompObj, item1.insurance_co_id);
		}
	}
}


var primaryInsurancePhotoDialog;

function initPrimaryInsurancePhotoDialog() {
	primaryInsurancePhotoDialog = new YAHOO.widget.Dialog('primaryInsurancePhotoDialog', {
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
		fn: handlePrimaryInsurancePhotoDialogCancel,
		scope: primaryInsurancePhotoDialog,
		correctScope: true
	});
	primaryInsurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	primaryInsurancePhotoDialog.render();
}

function showPrimaryInsurancePhotoDialog() {
	var button = document.getElementById('_p_plan_card');
	resizeCustom(document.getElementById('pinsuranceImage'), 500, 200);
	primaryInsurancePhotoDialog.cfg.setProperty("context", [button, "tl", "bl"], false);
	document.getElementById('primaryInsurancePhotoDialog').style.display = 'block';
	document.getElementById('primaryInsurancePhotoDialog').style.visibility = 'visible';
	primaryInsurancePhotoDialog.show();
}

function handlePrimaryInsurancePhotoDialogCancel() {
	document.getElementById('primaryInsurancePhotoDialog').style.display = 'none';
	document.getElementById('primaryInsurancePhotoDialog').style.visibility = 'hidden';
	primaryInsurancePhotoDialog.cancel();
}

var secondaryInsurancePhotoDialog;

function initSecondaryInsurancePhotoDialog() {
	secondaryInsurancePhotoDialog = new YAHOO.widget.Dialog('secondaryInsurancePhotoDialog', {
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
		fn: handleSecondaryInsurancePhotoDialogCancel,
		scope: secondaryInsurancePhotoDialog,
		correctScope: true
	});
	secondaryInsurancePhotoDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	secondaryInsurancePhotoDialog.render();
}

function showSecondaryInsurancePhotoDialog() {
	var button = document.getElementById('_s_plan_card');
	resizeCustom(document.getElementById('sinsuranceImage'), 500, 200);
	secondaryInsurancePhotoDialog.cfg.setProperty("context", [button, "tl", "bl"], false);
	document.getElementById('secondaryInsurancePhotoDialog').style.display = 'block';
	document.getElementById('secondaryInsurancePhotoDialog').style.visibility = 'visible';
	secondaryInsurancePhotoDialog.show();
}

function handleSecondaryInsurancePhotoDialogCancel() {
	document.getElementById('secondaryInsurancePhotoDialog').style.display = 'none';
	document.getElementById('secondaryInsurancePhotoDialog').style.visibility = 'hidden';
	secondaryInsurancePhotoDialog.cancel();
}

function onLoadTpaList(spnsrIndex) {
	loadTpaList(spnsrIndex);
	tpaChange(spnsrIndex);
	insuCatChange(spnsrIndex);
	RatePlanList();
	ratePlanChange();

	//if(corpInsuranceCheck == "Y")
		//setSelectedDateForCorpInsurance();
}

/*
 * There are 4 different ways to load the list of TPAs based on:
 *  - Whether Category is enabled or not
 *  - Whether insurance module is enabled or not.
 * If category is enabled, the list of TPAs for new cases is limited to what is allowed by
 * the category. If insurance module is enabled, we can connect to an existing case, or create
 * a new case for any of the (allowed) TPAs.
 */

/* Function called in 4 places, when Insurance company is changed in UI
	(or) existing patient details are loaded (loadInsurancePolicyDetails())
	(or) Member ship autocomplete is changed (loadPolicyDetails)
	(or) patient category is changed (onChangeCategory())
*/

function loadTpaList(spnsrIndex) {

	var loadTpaOnInsChange = (isModAdvanceIns || isModInsurance);

	var tpaObj = null;
	var tpaWrapperObj = null;
	var insuCompObj = null;
	var planTypeObj = null;
	var planObj = null;
	var tpaNameObj=null;
	var memberIdObj=null;

	if (spnsrIndex == 'P') {
		tpaObj = getSponsorObj('primary');
		insuCompObj = getInsuObj('primary');
		planTypeObj = getPlanTypeObj('primary');
		planObj = getPlanObj('primary');
		tpaNameObj=getSponsorNameObj('primary');
		tpaWrapperObj = document.getElementById("primary_sponsor_wrapper");
		memberIdObj = getInsuranceMemberIdObj('primary');

	} else if (spnsrIndex == 'S') {
		tpaObj = getSponsorObj('secondary');
		insuCompObj = getInsuObj('secondary');
		planTypeObj = getPlanTypeObj('secondary');
		planObj = getPlanObj('secondary');
		tpaNameObj=getSponsorNameObj('secondary');
		tpaWrapperObj = document.getElementById("secondary_sponsor_wrapper");
		memberIdObj = getInsuranceMemberIdObj('secondary');
	}

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

	var mainSpnsrIndex = spnsrIndex;
	if (memberIdObj != null)
		memberIdObj.value = "";

	var mainInsuObj = null;
	var mainInsCompanyId = '';
	if (mainSpnsrIndex == 'P') mainInsuObj = getInsuObj('primary');
	else if (mainSpnsrIndex == 'S') mainInsuObj = getInsuObj('secondary');
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
	}

	// if (gIsInsurance) {
		if (empty(mainSpnsrIndex) || mainSpnsrIndex == spnsrIndex) {
			var j = 2;
			// Load plan types related to insurance company
			if (insuCatNames != null){
			for (var i = 0; i < insuCatNames.length; i++) {
				var ele = insuCatNames[i];
				if (ele.insurance_co_id == mainInsCompanyId && ele.status == "A") {
					var optn = new Option(ele.category_name, ele.category_id);
					planTypeObj.options.length = j;
					planTypeObj.options[j - 1] = optn;
					j++;
					planType = ele.category_id;
				}
			}
			}
		}
	//}

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

	// Loading TPAs follows....

	// For revisit if tpa exists, need to set that back after TPAs are loaded.
	var previousTpa = (tpaObj != null) ? tpaObj.value : "";

	var newCaseSuffix = '';
	var tpaList = tpanames; // the default set: all TPAs
	var defaultTpa = "";
	var visitType = visit_type;

	var categoryId = patientCategory;

	if (categoryId != '') {
		// category is enabled, the list of TPAs is restricted
		for (var i = 0; i < categoryJSON.length; i++) {
			var item = categoryJSON[i];
			if (categoryId == item.category_id) {

				if (visitType == 'i') {
					if ((item.ip_allowed_sponsors == null || item.ip_allowed_sponsors == '')) {
						tpaList = [];
						loadSelectBox(insuCompObj, [], 'insurance_co_name',
							'insurance_co_id', getString("js.registration.patient.commonselectbox.defaultText"));
					} else if ((item.ip_allowed_sponsors != '*')) {
						var tpaIdList = item.ip_allowed_sponsors.split(',');
						var ip_allowedTpas = [];
						for (var i = 0; i < tpaIdList.length; i++)
							ip_allowedTpas.push(findInList(tpanames, "tpa_id", tpaIdList[i]));
						// override the tpaList with allowed TPAs.
						tpaList = ip_allowedTpas;
					} else {
						tpaList = tpaList;
					}
					if (spnsrIndex == 'P')
						defaultTpa = item.primary_ip_sponsor_id;
					else if (spnsrIndex == 'S')
						defaultTpa = item.secondary_ip_sponsor_id
					break;
				} else {
					if ((item.op_allowed_sponsors == null || item.op_allowed_sponsors == '')) {
						tpaList = [];
						loadSelectBox(insuCompObj, [], 'insurance_co_name',
							'insurance_co_id', getString("js.registration.patient.commonselectbox.defaultText"));
					} else if ((item.op_allowed_sponsors != '*')) {
						var tpaIdList = item.op_allowed_sponsors.split(',');
						var op_allowedTpas = [];
						for (var i = 0; i < tpaIdList.length; i++)
							op_allowedTpas.push(findInList(tpanames, "tpa_id", tpaIdList[i]));
						// override the tpaList with allowed TPAs.
						tpaList = !empty(op_allowedTpas) ? op_allowedTpas : tpaList;
					} else {
						tpaList = tpaList;
					}
					if (spnsrIndex == 'P')
						defaultTpa = item.primary_op_sponsor_id;
					else if (spnsrIndex == 'S')
						defaultTpa = item.secondary_op_sponsor_id;
					break;
				}
			}
		}
	} else {

	}

	if (tpaObj != null) {
		// Empty TPAs in tpa dropdown
		tpaObj.length = 1;
		var index = 0;

		//tpaObj.options[index].text = getString("js.registration.patient.commonselectbox.defaultText");
		//tpaObj.options[index].value = "";

		// Add all the allowed TPAs for patient category and insurance company as new cases.
		/*var newtpa =[];
		for (var i = 0; i < tpaList.length; i++) {
			var item = tpaList[i];

			item = findInList(tpanames, "tpa_id", item.tpa_id);
			if (sponsorType == 'I' && item.sponsor_type == 'I') {
				for (var k = 0; k < insCompTpaList.length; k++) {
					var insItem = insCompTpaList[k];
					if (!empty(item) && !empty(insItem) && (item.tpa_id == insItem.tpa_id)) {
						newtpa.push(item);
					}
				}
			}else {
				if (sponsorType == item.sponsor_type) {
					//tpaNameObj.value = item.tpa_name;
					//tpaObj.value = item.tpa_id;
				}
			}
		}
		if (!empty(newtpa)){
			tpaList=newtpa;
			if(!empty(insCompanyId) ){
				if(tpaObj.value != ''){
					var exist = findInList(newtpa, "tpa_id", tpaObj.value);
					if(empty(exist)){
						tpaNameObj.value = '';
						tpaObj.value = '';
					}
				}
			}
			if(newtpa.length ==1 && tpaObj.value ==''){
				tpaNameObj.value = newtpa[0].tpa_name;
				tpaObj.value = newtpa[0].tpa_id;
				enableOtherInsuranceDetailsTab(spnsrIndex);
				loadInsuCompanyDetails(spnsrIndex);
			}
		}*/

		// if the patient is for revisit and TPA exists, set it
		if (!empty(previousTpa)) {
			var item2 = findInList(tpanames, "tpa_id", previousTpa);
				tpaNameObj.value = item2.tpa_name;
				tpaObj.value = previousTpa;

			// if there is a default tpa for patient category, set it (doesn't work well if there is a case for the same TPA)
		} else if (!empty(defaultTpa)) {
			var item2 = findInList(tpanames, "tpa_id", defaultTpa);
				tpaNameObj.value = item2.tpa_name;
				tpaObj.value = defaultTpa;
				tpaWrapperObj.checked = true;

			// if there is a default tpa for insurance company, set it
		} else if (insCompTpaList != null && insCompTpaList.length == 1) {
			var item2 = findInList(tpanames, "tpa_id", insCompTpaList[0].tpa_id);
				tpaNameObj.value = item2.tpa_name;
				tpaObj.value = insCompTpaList[0].tpa_id;
			//setSelectedIndex(tpaObj, insCompTpaList[0].tpa_id);
		}
		// Ins30 : Corporate insurance validations removed
		// if(corpInsuranceCheck == 'Y')
			// setSelectedIndexForCorpInsurance1(tpaObj);

	}

	if (insuCompObj != null)
		sortDropDown(insuCompObj);
	if (tpaObj != null){
		if(spnsrIndex == 'P')
			priAutoLoadTpa(tpaList);
		if(spnsrIndex == 'S')
			secAutoLoadTpa(tpaList);
	}
	if (planTypeObj != null)
		sortDropDown(planTypeObj);
	if (planObj)
		sortDropDown(planObj);

	// Ins30 : Corporate insurance validations removed
	// if(corpInsuranceCheck != 'Y'){
		if (planTypeObj != null && planTypeObj.options.length == 2) {
			setSelectedIndex(planTypeObj, planType);
			}
		// } else {
		//	if (planTypeObj != null ) {
		//		setSelectedIndexForCorpInsurance(planTypeObj);
		//	}
		// }

		if(tpaObj != null && !empty(tpaObj.value)){
				enableOtherInsuranceDetailsTab(spnsrIndex);
				loadInsuCompanyDetails(spnsrIndex);
				loadOtherInsDetails(spnsrIndex);
				insuCatChange(spnsrIndex);
				RatePlanList();
				ratePlanChange();
		}
}

function setSelectedIndexForCorpInsurance1(tpaObj) {
				tpaObj.selectedIndex = 1;
				return;
}

function setSelectedIndexForCorpInsurance(planTypeObj) {
				planTypeObj.selectedIndex = 1;
				return;
}

function onCorporateChange(spnsrIndex) {
	if (spnsrIndex == 'P') {
		if (document.getElementById('primary_employee_id'))
			document.getElementById('primary_employee_id').value = '';
	} else if (spnsrIndex == 'S') {
		if (document.getElementById('secondary_employee_id'))
			document.getElementById('secondary_employee_id').value = '';
	}

	var tpaObj = null;
	var tpa = null;
	var approvalLimitObj = null;
	var uploadRowObj = null;
	var uploadCardViewObj = null;

	var employeeIdObj = null;
	var empNameObj = null;
	var empRelationObj = null;

	if (spnsrIndex == 'P') {
		tpaObj = getSponsorObj('primary');
		approvalLimitObj = getApprovalLimitObj('primary');
		uploadRowObj = getPrimaryUploadRowObj();
		uploadCardViewObj = document.getElementById("primaryCardView");

		employeeIdObj = getInsuranceMemberIdObj('primary');
		empNameObj = getPatientHolderObj('primary');
		empRelationObj = getPatientRelationObj('primary');

	} else if (spnsrIndex == 'S') {
		tpaObj = getSponsorObj('secondary');
		approvalLimitObj = getApprovalLimitObj('secondary');
		uploadRowObj = getSecondaryUploadRowObj();
		uploadCardViewObj = document.getElementById("secondaryCardView");

		employeeIdObj = getInsuranceMemberIdObj('secondary');
		empNameObj = getPatientHolderObj('secondary');
		empRelationObj = getPatientRelationObj('secondary');

	}
	if (tpaObj != null) {

		var tpaId = tpaObj.value;
		approvalLimitObj.value = "";
		employeeIdObj.value = "";
		empNameObj.value = "";
		empRelationObj.value = "";

		if (tpaId != '') {
			tpa = findInList(tpanames, "tpa_id", tpaId);
		}
	}
	if (tpa != null && isModAdvanceIns) {
		if (uploadRowObj != null) {
			if (tpa.scanned_doc_required == 'N') {
				uploadRowObj.style.display = 'none';
				if (uploadCardViewObj) uploadCardViewObj.style.display = 'none';
			} else {
				uploadRowObj.style.display = 'table-row';
				if (uploadCardViewObj) uploadCardViewObj.style.display = 'table';
			}
		}
	}
}

function onNationalSponsorChange(spnsrIndex) {
	var tpaObj = null;
	var tpa = null;
	var approvalLimitObj = null;
	var uploadRowObj = null;
	var uploadCardViewObj = null;

	var nationalIdObj = null;
	var citizenNameObj = null;
	var patRelationObj = null;

	if (spnsrIndex == 'P') {
		tpaObj = getSponsorObj('primary');
		approvalLimitObj = getApprovalLimitObj('primary');
		uploadRowObj = getPrimaryUploadRowObj();
		uploadCardViewObj = document.getElementById("primaryCardView");

		nationalIdObj = getInsuranceMemberIdObj('primary');
		citizenNameObj = getPatientHolderObj('primary');
		patRelationObj = getPatientRelationObj('primary');

	} else if (spnsrIndex == 'S') {
		tpaObj = getSponsorObj('secondary');
		approvalLimitObj = getApprovalLimitObj('secondary');
		uploadRowObj = getSecondaryUploadRowObj();
		uploadCardViewObj = document.getElementById("secondaryCardView");

		nationalIdObj = getInsuranceMemberIdObj('secondary');
		citizenNameObj = getPatientHolderObj('secondary');
		patRelationObj = getPatientRelationObj('secondary');
	}

	if (tpaObj != null) {

		var tpaId = tpaObj.value;
		approvalLimitObj.value = "";
		nationalIdObj.value = "";
		citizenNameObj.value = "";
		patRelationObj.value = "";

		if (tpaId != '') {
			tpa = findInList(tpanames, "tpa_id", tpaId);
			approvalLimitObj.value = "";
		}
		if (tpa != null && isModAdvanceIns) {
			if (uploadRowObj != null) {
				if (tpa.scanned_doc_required == 'N') {
					uploadRowObj.style.display = 'none';
					if (uploadCardViewObj) uploadCardViewObj.style.display = 'none';
				} else {
					uploadRowObj.style.display = 'table-row';
					if (uploadCardViewObj) uploadCardViewObj.style.display = 'table';
				}
			}
		}
	}
}

function setPrimarySponsorDefaults() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj != null) {
		var primarySpnsrType = primarySponsorObj.value;

		if (primarySpnsrType == 'I') {
			loadInsuranceCompList('P');
			loadTpaList('P');
			tpaChange('P');
			insuCatChange('P');
			RatePlanList();
			policyChange('P');
		    //resetSponsorDetailsTabs('P');

		} else if (primarySpnsrType == 'C') {
			loadTpaList('P');
			onCorporateChange('P');

		} else if (primarySpnsrType == 'N') {
			loadTpaList('P');
			onNationalSponsorChange('P');
		} else {}
	}

	if (isModAdvanceIns) {
		var spnsrIndex = getMainSponsorIndex();
		if (spnsrIndex == 'S') policyNoAutoComplete('S', gPatientPolciyNos);
		else policyNoAutoComplete('P', gPatientPolciyNos);
	}
}

function setSecondarySponsorDefaults() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj != null) {
		var secondarySpnsrType = secondarySponsorObj.value;
		if (secondarySpnsrType == 'I') {
			loadInsuranceCompList('S');
			loadTpaList('S');
			tpaChange('S');
			insuCatChange('S');
			RatePlanList();
			policyChange('S');
			//resetSponsorDetailsTabs('S');

		} else if (secondarySpnsrType == 'C') {
			loadTpaList('S');
			onCorporateChange('S');

		} else if (secondarySpnsrType == 'N') {
			loadTpaList('S');
			onNationalSponsorChange('S');
		} else {}
	}

	if (isModAdvanceIns) {
		var spnsrIndex = getMainSponsorIndex();
		if (spnsrIndex == 'S') policyNoAutoComplete('S', gPatientPolciyNos);
		else policyNoAutoComplete('P', gPatientPolciyNos);
	}
}

// Function called in 3 places, when TPA is changed (tpaChange())
// (or) Rate plans are loaded (RatePlanList())
// (or) to load existing patient details (loadInsurancePolicyDetails())
var gSelectedTPA = null;
var gSelectedPlan = null;
var gSelectedRatePlan = null;
var gSelectedPatientCategory = null;

function ratePlanChange() {
	var plan = "",
		tpaId = "";
	var isRatePlanChanged = false;
	var isTPAorPlanChanged = false;
	var ratePlanObj = document.mainform.organization;
	var ratePlan = ratePlanObj.value;
	var planObj = null;
	var tpaObj = null;
	var spnsrIndex = getMainSponsorIndex();
	if (spnsrIndex == 'P') {
		planObj = getPlanObj('primary');
		tpaObj = getSponsorObj('primary');
	} else if (spnsrIndex == 'S') {
		planObj = getPlanObj('secondary');
		tpaObj = getSponsorObj('secondary');
	}
	if (planObj != null) plan = planObj.value;
	if (tpaObj != null) tpaId = tpaObj.value;
	if (gSelectedRatePlan == ratePlan && gSelectedTPA == tpaId && gSelectedPlan == plan) return;
	if (gSelectedRatePlan == ratePlan) {} else {
		gSelectedRatePlan = ratePlan;
		gPreviousRatePlan = ratePlan;
		isRatePlanChanged = true;
	}
	if (gSelectedPlan == plan) {} else {
		gSelectedPlan = plan;
		isTPAorPlanChanged = true;
	}
	if (gSelectedTPA == tpaId) {} else {
		gSelectedTPA = tpaId;
		isTPAorPlanChanged = true;
	}
}

function insuCatChange(spnsrIndex) {
	var insApprovalAmtObj = null;
	var planObj = null;
	var insCompObj = null;
	var planTypeObj = null;
	var tpaobj=null;
	if (spnsrIndex == 'P') {
		insApprovalAmtObj = getApprovalLimitObj('primary');
		planObj = getPlanObj('primary');
		insCompObj = getInsuObj('primary');
		planTypeObj = getPlanTypeObj('primary');
		tpaobj= getSponsorObj('primary');
	} else if (spnsrIndex == 'S') {
		insApprovalAmtObj = getApprovalLimitObj('secondary');
		planObj = getPlanObj('secondary');
		insCompObj = getInsuObj('secondary');
		planTypeObj = getPlanTypeObj('secondary');
		tpaobj=getSponsorObj('secondary'); 
	}
	
	
	// if insurance co and plan type are selected bring policyNames via ajax
	if (insCompObj != null && insCompObj.value != ""
		&& planTypeObj != null && planTypeObj.value != "") {
		policynames = mergePolicyNames(policynames, getPolicyNames(spnsrIndex));
	}
      
	if (insApprovalAmtObj) insApprovalAmtObj.value = "";
	if (planObj != null && policynames != null) {
		var selectedInsId = insCompObj.value;
		var selectedCatId = planTypeObj.value;
		var policySelect = planObj;
		// Empty plans
		var len = 1;
		var policyDefault = "";
		var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
		policySelect.options.length = len;
		policySelect.options[len - 1] = optn;
		for (var k = 0; k < policynames.length; k++) {
			var ele = policynames[k];
			if (ele.insurance_co_id == selectedInsId && ele.category_id == selectedCatId && ele.status == "A"
				&& (empty(ele.sponsor_id) ||ele.sponsor_id == tpaobj.value)
				&& ((ele.op_applicable == 'Y' && visit_type =='o') || (ele.ip_applicable == 'Y' && visit_type =='i'))) {
				var optn = new Option(ele.plan_name, ele.plan_id);
				len++;
				policySelect.options.length = len;
				policySelect.options[len - 1] = optn;
				policyDefault = ele.plan_id;
			}
		}
		if (policySelect.options.length == 2) {
			setSelectedIndex(policySelect, policyDefault);
		}
		sortDropDown(planObj);
		policyChange(spnsrIndex);
	}
}
// Function called when Plan type is changed (insuCatChange())
// (or) plan is changed (onPolicyChange())
// (or) Member ship autocomplete is changed (loadPolicyDetails)

function policyChange(spnsrIndex) {
	var approvalAmtObj = null;
	var planObj = null;
	var policyValidityStartObj = null;
	var policyValidityEndObj = null;
	var memberIdObj = null;
	var policyNumberObj = null;
	var policyHolderObj = null;
	var insuranceDocContentObj = null;
	var approvalAmtStarObj = null;
	var policyValidityStartStarObj = null;
	var policyValidityEndStarObj = null;
	var patientRelationship = null;
	var tpaObj = null;

	if (spnsrIndex == 'P') {
		approvalAmtObj = getApprovalLimitObj('primary');
		planObj = getPlanObj('primary');
		policyValidityStartObj = getPolicyValidityStartObj('primary');
		policyValidityEndObj = getPolicyValidityEndObj('primary');
		memberIdObj = getInsuranceMemberIdObj('primary');
		policyNumberObj = getInsurancePolicyNumberObj('primary');
		policyHolderObj = getPatientHolderObj('primary');
		insuranceDocContentObj = getPrimaryDocContentObj();
		approvalAmtStarObj = getPrimaryApprovalLimitStarObj();
		policyValidityStartStarObj = getPrimaryPolicyValidityStartStarObj();
		policyValidityEndStarObj = getPrimaryPolicyValidityEndStarObj();
		patientRelationship = getPatientRelationObj('primary');
		tpaObj = getSponsorObj('primary');
		enableDisablePlanDetails("P",planObj);

	} else if (spnsrIndex == 'S') {
		approvalAmtObj = getApprovalLimitObj('secondary');
		planObj = getPlanObj('secondary');
		policyValidityStartObj = getPolicyValidityStartObj('secondary');
		policyValidityEndObj = getPolicyValidityEndObj('secondary');
		memberIdObj = getInsuranceMemberIdObj('secondary');
		policyNumberObj = getInsurancePolicyNumberObj('secondary');
		policyHolderObj = getPatientHolderObj('secondary');
		insuranceDocContentObj = getSecondaryDocContentObj();
		approvalAmtStarObj = getSecondaryApprovalLimitStarObj();
		policyValidityStartStarObj = getSecondaryPolicyValidityStartStarObj();
		policyValidityEndStarObj = getSecondaryPolicyValidityEndStarObj();
		patientRelationship = getPatientRelationObj('secondary');
		tpaObj = getSponsorObj('secondary');
		enableDisablePlanDetails("S",planObj);
	}
	if (planObj != null) {
		var plan = planObj.value;
		if (!empty(plan)) {

			if(isValidityPeriodEditable(tpaObj)){
				policyValidityStartObj.removeAttribute("disabled");
				policyValidityEndObj.removeAttribute("disabled");
			}
			if(approvalAmtObj != null)
				approvalAmtObj.value = "";

			for (var i = 0; i < policynames.length; i++) {
				if (policynames[i].plan_id == plan) {
					if (!empty(policynames[i].overall_treatment_limit)) {
						if(approvalAmtStarObj != null)
						approvalAmtObj.value = formatAmountPaise(getPaise(policynames[i].overall_treatment_limit));
					}
					if(approvalAmtStarObj != null)
					//approvalAmtStarObj.style.visibility = 'visible';

					//policyValidityStartStarObj.style.visibility = 'visible';
					//policyValidityEndStarObj.style.visibility = 'visible';
					break;
				}
			}
		} else {
			memberIdObj.value = "";
			policyNumberObj.value = "";
			policyHolderObj.value = "";
			policyValidityEndObj.value = "";
			policyValidityStartObj.value = "";
			insuranceDocContentObj.value = "";
			patientRelationship.value = "";
		}
	}
}

function onPolicyChange(spnsrIndex) {
	policyChange(spnsrIndex);
	RatePlanList();
	ratePlanChange();
	// setSelectedDateForCorpInsurance();
	// Ins30 : Corporate insurance validations removed
	/*if(corpInsuranceCheck == "Y"){
		checkCurrentDateWithEndDateForCorpInsurance();
		} */
}

function onInsuCatChange(spnsrIndex) {
	insuCatChange(spnsrIndex);
	RatePlanList();
	ratePlanChange();

	// Ins30 : Corporate insurance validations removed
	/*if(corpInsuranceCheck == "Y")
		setSelectedDateForCorpInsurance();*/
}

function checkCurrentDateWithEndDateForCorpInsurance() {
    var EnteredDate = '';
	if(document.getElementById("primary_policy_validity_end1") != null)
           EnteredDate = document.getElementById("primary_policy_validity_end1").value; //for javascript

    var date = EnteredDate.substring(0, 2);
    var month = EnteredDate.substring(3, 5);
    var year = EnteredDate.substring(6, 10);
    var myDate = new Date(year, month - 1, date);
  	var curDate = new Date();
   	if (gServerNow != null) {
		curDate.setTime(gServerNow);
	}
	curDate.setHours(0);
	curDate.setMinutes(0);
	curDate.setSeconds(0);
	curDate.setMilliseconds(0);

    if(EnteredDate != ''){
    if (myDate < curDate) {
        alert(getString("js.registration.patient.policy.validity.has.expired.corpInsurance"));
        if(document.getElementById("primary_plan_id") != null)
	        document.getElementById("primary_plan_id").focus();
			document.getElementById("primary_plan_id").selectedIndex = '0';

		if(document.getElementById('primary_policy_validity_start') != null)
				document.getElementById('primary_policy_validity_start').textContent = '';

		if(document.getElementById('primary_policy_validity_end') != null)
				document.getElementById('primary_policy_validity_end').textContent = '';

		return false;
     } else {
         return true;
		}
	}
 return true;
}

function RatePlanList() {
	var spnsrIndex = getMainSponsorIndex();
	var planTypeObj = null;
	var planObj = null;
	var insuCompObj = null;
	if (spnsrIndex == 'P') {
		insuCompObj = getInsuObj('primary');
		planTypeObj = getPlanTypeObj('primary');
		planObj = getPlanObj('primary');
	} else if (spnsrIndex == 'S') {
		insuCompObj = getInsuObj('secondary');
		planTypeObj = getPlanTypeObj('secondary');
		planObj = getPlanObj('secondary');
	}
	var categoryId = '';
	var planId = '';
	var insCompanyId = '';
	var catDefaultRatePlan = "";
	var planDefaultRatePlan = "";
	var insCompDefaultRatePlan = "";
	var orgIdList = null;
	var ratePlan = document.getElementById("organization");
	if (insuCompObj) insCompanyId = insuCompObj.value;

	categoryId = patientCategory;

	if (planObj) planId = planObj.value;
	if (categoryId != '') {
		var category = findInList(categoryJSON, "category_id", categoryId);
		if (!empty(category)) {
			if (visit_type == 'i') {
				catDefaultRatePlan = category.ip_rate_plan_id;
				if (category.ip_allowed_rate_plans != '*') orgIdList = category.ip_allowed_rate_plans.split(',');
				if (category.ip_allowed_sponsors == null) {
					if (document.getElementById("primary_insurance_co") != null)
						loadSelectBox(document.getElementById('primary_insurance_co'), [], 'insurance_co_name', 'insurance_co_id', getString("js.registration.patient.commonselectbox.defaultText"));
					if (document.getElementById("secondary_insurance_co") != null)
						loadSelectBox(document.getElementById('secondary_insurance_co'), [], 'insurance_co_name', 'insurance_co_id', getString("js.registration.patient.commonselectbox.defaultText"));
				}
			} else {
				catDefaultRatePlan = category.op_rate_plan_id;
				if (category.op_allowed_rate_plans != '*') orgIdList = category.op_allowed_rate_plans.split(',');
				if (category.op_allowed_sponsors == null) {
					if (document.getElementById("primary_insurance_co") != null)
						loadSelectBox(document.getElementById("primary_insurance_co"), [], 'insurance_co_name', 'insurance_co_id', getString("js.registration.patient.commonselectbox.defaultText"));
					if (document.getElementById("secondary_insurance_co") != null)
						loadSelectBox(document.getElementById("secondary_insurance_co"), [], 'insurance_co_name', 'insurance_co_id', getString("js.registration.patient.commonselectbox.defaultText"));
				}
			}
		}
	}
	// Rate plan related to insurance company
	if (insCompanyId != '') {
		var selectedIns = findInList(insuCompanyDetails, "insurance_co_id", insCompanyId);
		if (!empty(selectedIns) && !empty(selectedIns.default_rate_plan)) {
			insCompDefaultRatePlan = selectedIns.default_rate_plan;
			insCompDefaultRatePlan = isRatePlanActive(insCompDefaultRatePlan) ? insCompDefaultRatePlan : "";
		}
	}
	// Rate plan related to plan
	if (planId != '') {
		var plan = findInList(policynames, "plan_id", planId);
		planDefaultRatePlan = plan.default_rate_plan;
		planDefaultRatePlan = isRatePlanActive(planDefaultRatePlan) ? planDefaultRatePlan : "";
	}
	// If plan default rate plan is empty and insurance company default rate plan exists
	// then company default rate plan is choosen.
	if (empty(planDefaultRatePlan) && !empty(insCompDefaultRatePlan))
		planDefaultRatePlan = insCompDefaultRatePlan;
	// Empty Rate plans
	var optn = new Option(getString("js.registration.patient.commonselectbox.defaultText"), "");
	var len = 1;
	ratePlan.options.length = len;
	ratePlan.options[len - 1] = optn;
	var len = 1;
	if (!empty(patientCategoryFieldLabel) && !empty(planDefaultRatePlan)) {
		if (!empty(orgIdList)) {
			for (var k = 0; k < orgIdList.length; k++) {
				// Not empty plan default rate plan and also category rate plan list containd the plan rate plan,
				// populate the rate plan.
				if (planDefaultRatePlan == orgIdList[k]) {
					var org = findInList(orgNamesJSON, "org_id", orgIdList[k]);
					if (!empty(org)) {
						var optn = new Option(org.org_name, org.org_id);
						len++;
						ratePlan.options.length = len;
						ratePlan.options[len - 1] = optn;
						break;
					}
				}
			}
		} else {
			for (var k = 0; k < orgNamesJSON.length; k++) {
				// Not empty plan default rate plan and also category rate plan list containd the plan rate plan,
				// populate the rate plan.
				if (planDefaultRatePlan == orgNamesJSON[k].org_id) {
					var optn = new Option(orgNamesJSON[k].org_name, orgNamesJSON[k].org_id);
					len++;
					ratePlan.options.length = len;
					ratePlan.options[len - 1] = optn;
					break;
				}
			}
		}
		if (ratePlan.options.length == 1) {
			showMessage("js.registration.patient.valid.rate.plans.against.category.plan.insurance.company");
		}
	} else {
		if (!empty(orgIdList)) {
			for (var k = 0; k < orgIdList.length; k++) {
				var org = null;
				if (orgIdList[k].org_id) org = findInList(orgNamesJSON, "org_id", orgIdList[k].org_id);
				else org = findInList(orgNamesJSON, "org_id", orgIdList[k]);
				if (!empty(org)) {
					var optn = new Option(org.org_name, org.org_id);
					len++;
					ratePlan.options.length = len;
					ratePlan.options[len - 1] = optn;
				}
			}
		} else {
			for (var i = 0; i < orgNamesJSON.length; i++) {
				ratePlan.options.length = len + 1;
				var optn = new Option(orgNamesJSON[i].org_name, orgNamesJSON[i].org_id);
				ratePlan.options[len] = optn;
				len++;
			}
		}
	}

	setSelectedIndex(ratePlan, gPreviousRatePlan);

	if (!empty(catDefaultRatePlan))
		setSelectedIndex(ratePlan, catDefaultRatePlan);

	if (!empty(planDefaultRatePlan))
		setSelectedIndex(ratePlan, planDefaultRatePlan);

	if (ratePlan.options.length == 2)
		ratePlan.selectedIndex = 1;

	sortDropDown(ratePlan);
}

function ratePlanChange() {
	if (gSelectedPatientCategory == patientCategory) {
		gPatientCategoryChanged = false;
	} else {
		gSelectedPatientCategory = patientCategory;
		gPatientCategoryChanged = true;
		return;
	}
	var plan = "",
		tpaId = "";
	var isRatePlanChanged = false;
	var isTPAorPlanChanged = false;
	var ratePlanObj = document.mainform.organization;
	var ratePlan = ratePlanObj.value;
	var planObj = null;
	var tpaObj = null;
	var spnsrIndex = getMainSponsorIndex();
	if (spnsrIndex == 'P') {
		planObj = getPlanObj('primary');
		tpaObj = getSponsorObj('primary');
	} else if (spnsrIndex == 'S') {
		planObj = getPlanObj('secondary');
		tpaObj = getSponsorObj('secondary');
	}
	if (planObj != null) plan = planObj.value;
	if (tpaObj != null) tpaId = tpaObj.value;

	gPreviousRatePlan = ratePlanObj.value;
	if (gSelectedRatePlan == ratePlan && gSelectedTPA == tpaId && gSelectedPlan == plan) return;
	if (gSelectedRatePlan == ratePlan) {} else {
		gSelectedRatePlan = ratePlan;
		isRatePlanChanged = true;
	}
	if (gSelectedPlan == plan) {} else {
		gSelectedPlan = plan;
		isTPAorPlanChanged = true;
	}
	if (gSelectedTPA == tpaId) {} else {
		gSelectedTPA = tpaId;
		isTPAorPlanChanged = true;
	}

	if (ratePlanObj != null && ratePlanObj.options.length == 2) {
		ratePlanObj.options[1].selected = true;
	}
}

function displayEstimatedTotalAmountTable(rateplan) {
	return (!empty(showChargesAllRatePlan) && showChargesAllRatePlan == 'A');
}

function calculateTotalEstimateAmount() {
	var doctorObj = document.mainform.doctor;
	var chargeTypeObj = document.mainform.doctorCharge;
	var doctorCharge = (chargeTypeObj) ? chargeTypeObj.value : '';
	getConsultationTypes(document.mainform.organization.value);
	if (chargeTypeObj) {
		setSelectedIndex(chargeTypeObj, "");
		setSelectedIndex(chargeTypeObj, doctorCharge);
	}
	if (doctorObj) {
		var doctorId = doctorObj.value;
		if (!empty(doctorId)) getDoctorCharge();
	}
	loadPreviousUnOrderedPrescriptions();
	if (trim(document.mrnoform.mrno.value) != "" && gSelectedRatePlan == null && gSelectedTPA == null && gSelectedPlan == null) return;
	if (category != null && (category != 'SNP' || !empty(scheduleName))) loadSchedulerOrders();
	estimateTotalAmount();
}

String.prototype.startsWith = function (str) {
	return this.slice(0, str.length) == str;
};


var primarySponsorDialog = null;

function initprimarySponsorDialog() {
	var dialog = document.getElementById('showPimarySponsorViewDialog');
	dialog.style.display = 'block';
	primarySponsorDialog = new YAHOO.widget.Dialog("showPimarySponsorViewDialog", {
		width: "350px",
		height: "250px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	primarySponsorDialog.render();
}
var secondarySponsorDialog = null;

function initsecondarySponsorDialog() {
	var dialog = document.getElementById('showSecondarySponsorViewDialog');
	dialog.style.display = 'block';
	secondarySponsorDialog = new YAHOO.widget.Dialog("showSecondarySponsorViewDialog", {
		width: "350px",
		height: "250px",
		visible: false,
		modal: true,
		constraintoviewport: true
	});
	secondarySponsorDialog.render();
}

function onTpaChange(spnsrIndex) {
	tpaChange(spnsrIndex);
	ratePlanChange();

	// Ins30 : Corporate insurance validations removed
	/* if(corpInsuranceCheck == "Y")
		setSelectedDateForCorpInsurance(); */
}
// Function called in 3 places, when Insurance company changed (loadTpaList()) (or) TPA is changed in UI
// (or) to load existing patient details (loadInsurancePolicyDetails())

function tpaChange(spnsrIndex) {
	var tpaIdObj = null;
	var insuCompIdObj = null;
	var uploadRowObj = null;
	var uploadCardViewObj = null;

	if (spnsrIndex == 'P') {
		tpaIdObj = getSponsorObj('primary');
		insuCompIdObj = getInsuObj('primary')
		uploadRowObj = getPrimaryUploadRowObj();
		uploadCardViewObj = document.getElementById("primaryCardView");

	} else if (spnsrIndex == 'S') {
		tpaIdObj = getSponsorObj('secondary');
		insuCompIdObj = getInsuObj('secondary');
		uploadRowObj = getSecondaryUploadRowObj();
		uploadCardViewObj = document.getElementById("secondaryCardView");
	}
	if (tpaIdObj != null && tpaIdObj.value != '') {
		gIsInsurance = true;
		var selectedTpaId = tpaIdObj.value;

		for (var i = 0; i < tpanames.length; i++) {
			var tpaValidityDate = new Date(tpanames[i].validity_end_date);
			if (selectedTpaId == tpanames[i].tpa_id) {
				if (!empty(tpanames[i].validity_end_date)) {
					if (daysDiff(getServerDate(), tpaValidityDate) < 0) {
						showMessage("js.registration.patient.tpa.validity.check");
						//tpaIdObj.selectedIndex = 0;
					}
				}
				if (uploadRowObj != null) {
					if (tpanames[i].scanned_doc_required == 'N') {
						uploadRowObj.style.display = 'none';
						if (uploadCardViewObj) uploadCardViewObj.style.display = 'none';
					} else {
						uploadRowObj.style.display = 'table-row';
						if (uploadCardViewObj) uploadCardViewObj.style.display = 'table';
					}

					if (!isModAdvanceIns) {
						uploadRowObj.style.display = 'none';
						if (uploadCardViewObj) uploadCardViewObj.style.display = 'none';
					}
				}
				break;
			}
		}
	} else {
		gIsInsurance = false;
	}
	if (insuCompIdObj != null) sortDropDown(insuCompIdObj);
}

// If the selected Primary sponsor is Insurance && has plan then Primary is Main Insurance
// If the selected Secondary sponsor is Insurance && has plan then Secondary is Main Insurance
// Otherwise Primary Sponsor is Main

function getMainSponsorIndex() {
	if (document.getElementById("primary_member_id") != null && document.getElementById("primary_member_id").value != '') return 'P';
	if (document.getElementById("secondary_member_id") != null && document.getElementById("secondary_member_id").value != '') return 'S';
	if (document.getElementById("primary_plan_id") != null && document.getElementById("primary_plan_id").value != '') return 'P';
	if (document.getElementById("secondary_plan_id") != null && document.getElementById("secondary_plan_id").value != '') return 'S';
	if (document.getElementById("primary_plan_type") != null && document.getElementById("primary_plan_type").value != '') return 'P';
	if (document.getElementById("secondary_plan_type") != null && document.getElementById("secondary_plan_type").value != '') return 'S';
	if (document.getElementById("primary_insurance_co") != null && document.getElementById("primary_insurance_co").value != '') return 'P';
	if (document.getElementById("secondary_insurance_co") != null && document.getElementById("secondary_insurance_co").value != '') return 'S';
	if (document.getElementById("primary_sponsor_id") != null && document.getElementById("primary_sponsor_id").value != '') return 'P';
	if (document.getElementById("secondary_sponsor_id") != null && document.getElementById("secondary_sponsor_id").value != '') return 'S';
	if (document.getElementById("primary_sponsor") != null && document.getElementById("primary_sponsor").value == 'I') return 'P';
	if (document.getElementById("secondary_sponsor") != null && document.getElementById("secondary_sponsor").value == 'I') return 'S';
}

function checkUseDRG(spnsrIndex) {
	var drgCheckObj = null;
	var useDRGobj = null;
	if (spnsrIndex == 'P') {
		drgCheckObj = getPrimaryDRGCheckObj();
		useDRGobj = getPrimaryUseDRGObj();
	} else if (spnsrIndex == 'S') {
		drgCheckObj = getSecondaryDRGCheckObj();
		useDRGobj = getSecondaryUseDRGObj();
	}
	if (drgCheckObj != null) {
		var useDRG = drgCheckObj.checked ? 'Y' : 'N';
		useDRGobj.value = useDRG;
	}
}

function checkUsePerdiem(spnsrIndex) {
	var perdiemCheckObj = null;
	var usePerdiemobj = null;
	if (spnsrIndex == 'P') {
		perdiemCheckObj = getPrimaryPerdiemCheckObj();
		usePerdiemobj = getPrimaryUsePerdiemObj();
	} else if (spnsrIndex == 'S') {
		perdiemCheckObj = getSecondaryPerdiemCheckObj();
		usePerdiemobj = getSecondaryUsePerdiemObj();
	}
	if (perdiemCheckObj != null) {
		var usePerdiem = perdiemCheckObj.checked ? 'Y' : 'N';
		usePerdiemobj.value = usePerdiem;
	}
}

var babyMemeberId;
function validateBabyAge(mrNo,visitId,sponsorObj,memberId) {
	babyMemeberId = memberId;
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/pages/registration/regUtils.do?_method=getBabyDOBAndMemberIdValidityDetails&mr_no=" + mrNo +
						"&visit_id=" + visitId+"&sponsor_type="+sponsorObj.value;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return babyDetailsHandaler(ajaxobj.responseText);
		}
	}
	return true;
}


function babyDetailsHandaler(responseText) {
	var memberId = babyMemeberId
	eval("babyInfo =" + responseText);
	if (babyInfo != null) {
		var babyDetails = babyInfo.babyDetails;
		var babyVisitDetails = babyInfo.babyVisitDetails;
		var helthAuthPrefs =  babyInfo.helathAuthPrefs;
		var parentMemberId = babyInfo.member_id;
		if(babyDetails != null && helthAuthPrefs != null) {
			var salutation = babyDetails.salutation;
			salutation = salutation.toUpperCase();
			if(salutation == 'BABY' && memberId == parentMemberId) {
				var dobInMillis = babyDetails.dateofbirth;
				var child_mother_ins_member_validity_days = helthAuthPrefs.child_mother_ins_member_validity_days;
				var dob = new Date(dobInMillis);
				var serverDate = new Date();
				var diffInDays = (serverDate - dob)/ 60 / 60 / 24 / 1000;
				if(!empty(child_mother_ins_member_validity_days) && diffInDays < child_mother_ins_member_validity_days) {
					return false;
				}
			}
		}
	}
	return true;
}

function checkForMemberID(spnsrIndex) {
	var memberIdObj = null;
	var insuCompObj = null;
	var sponsorObj = null;
	var memberIdLabel=null;

	if (spnsrIndex == 'P') {
		memberIdObj = getInsuranceMemberIdObj('primary');
		insuCompObj = getInsuObj('primary')
		sponsorObj = document.getElementById("primary_sponsor");
		memberIdLabel=document.getElementById("primary_member_id_label").innerHTML;
		var tpaId = document.getElementById("primary_sponsor_id").value;
		var tpa = findInList(tpanames, "tpa_id", tpaId);
		if(tpa != null)
			var memberIdPattern = tpa.member_id_pattern;

	}else if (spnsrIndex == 'S') {
		memberIdObj = getInsuranceMemberIdObj('secondary');
		insuCompObj = getInsuObj('secondary');
		sponsorObj =  document.getElementById("secondary_sponsor");
		memberIdLabel=document.getElementById("secondary_member_id_label").innerHTML;
		var tpaId = document.getElementById("secondary_sponsor_id").value;
		var tpa = findInList(tpanames, "tpa_id", tpaId);
		if(tpa != null)
			var memberIdPattern =  tpa.member_id_pattern;
	}
	memberIdLabel=memberIdLabel.replace(':','');

	if (memberIdObj != null) {
		if(!compareMemberIdAndPattern(memberIdObj, memberIdPattern, memberIdLabel))
			return false;
		var memberId = trimAll(memberIdObj.value);
		var mrNo = document.mainform.mrno.value;
		var visitId = document.mainform.visitId.value;

		if (!empty(memberId)) {
			var companyId = insuCompObj.value;

			var ajaxobj = newXMLHttpRequest();
			var url = cpath + `/patients/tpaMemberCheck.json?member_id=${encodeURIComponent(memberId)}&tpa_id=${tpaId}&exclude_mr_no=${mrNo}`;
			ajaxobj.open("GET", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						var exists =  JSON.parse(ajaxobj.responseText);
						if (exists && exists.parent_child_mr_nos && exists.parent_child_mr_nos.length > 0) {
							let validation_type_text = "";
							let tpa_select_index = 0;
							tpanames.map((tpaname, tpanameIndex) => {
							if(tpaname.tpa_id === tpaId)
									tpa_select_index = tpanameIndex;
							});
							if(tpanames && tpanames[tpa_select_index]){
								let currentTpa = tpanames[tpa_select_index];
								let memIdValidType = currentTpa.tpa_member_id_validation_type;
								let mrNos = exists.parent_child_mr_nos;
								if(memIdValidType === "A"){
									return true;
								}else if(memIdValidType === "B"){
									alert(
											getString('js.common.message.block') + ': ' +
											getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
										);
									memberIdObj.value = "";
									memberIdObj.focus();
									return false;
								}else if(memIdValidType === "W"){
									alert(
											getString('js.common.message.warn') + ': ' +
											getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
										);
									return true;
								}else if(currentTpa.tpa_member_id_validation_type === "C" && mrNo != null && mrNo != ""){
									const not_parent_child = mrNos.filter(item => item.is_parent_child === false);
									if (not_parent_child && not_parent_child.length) {
										alert(
												getString('js.common.message.block') + ': ' +
												getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
											);
										memberIdObj.value = "";
										memberIdObj.focus();
										return false;
									}
									const is_parent_mr_no = mrNos.filter(item => item.is_parent_mr_no === true);
									if(is_parent_mr_no && is_parent_mr_no.length ){
										if(patientDateOfBirth != "" && moment(patientRegDate,  "YYYY-MM-DD").diff(moment(patientDateOfBirth,  "YYYY-MM-DD"), 'days') <= currentTpa.child_dup_memb_id_validity_days){
											return true;	
										}else{
											alert(
													getString('js.common.message.validaity.count.over') + ': ' +
													getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
												);
											memberIdObj.value = "";
											memberIdObj.focus();
											return false;
										}
									}else{
										return true;
									}
								}else{
									alert(
											getString('js.common.message.block') + ': ' +
											getString('js.common.message.duplicate.member.id.detected.triple.placeholder', memberIdLabel, memberId, mrNos[0].mrno)
										);
									memberIdObj.value = "";
									memberIdObj.focus();
									return false;
								}
							}
						}else
							return true;
					}
				}
			}
		}
	}
	return true;
}


function checkForCorporateMemberID(spnsrIndex) {
	var memberIdObj = null;
	var insuCompObj = null;
	var sponsorObj = null;
	var planObj = null;
	if (spnsrIndex == 'P') {
		memberIdObj = document.getElementById("primary_employee_id")
		insuCompObj = getSponsorObj('primary');
		sponsorObj = document.getElementById("primary_sponsor");

	} else if (spnsrIndex == 'S') {
		memberIdObj = document.getElementById("secondary_employee_id")
		insuCompObj = getSponsorObj('secondary');
		sponsorObj = document.getElementById("secondary_sponsor");
	}

	if (memberIdObj != null) {
		var memberId = trimAll(memberIdObj.value);
		var mrNo = document.mainform.mrno.value;
		var visitId = document.mainform.visitId.value;

		if (!empty(memberId)) {
			var companyId = insuCompObj.value;

			if(!empty(mrNo) && !empty(visitId) && !empty(sponsorObj)
				&& !validateBabyAge(mrNo,visitId,sponsorObj,memberId)) {
				return true;
			}

			var ajaxobj = newXMLHttpRequest();
			var url = cpath + "/pages/registration/regUtils.do?_method=checkForDuplicateCorporateMemberId" + "&member_id=" + encodeURIComponent(memberId) + "&sponsor_id=" + companyId + "&mr_no=" + mrNo;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var exists =" + ajaxobj.responseText);
						if (exists == "true") {
							showMessage("js.registration.patient.employee.id.already.exists.string");
							memberIdObj.value = "";
							memberIdObj.focus();
							return false;
						} else
							return true;
					}
				}
			}
		}
	}
	return true;
}

function checkForNationalMemberID(spnsrIndex) {
	var memberIdObj = null;
	var insuCompObj = null;
	var sponsorObj = null;
	var planObj = null;
	if (spnsrIndex == 'P') {
		memberIdObj = document.getElementById('primary_national_member_id');
		insuCompObj = getSponsorObj('primary');
		sponsorObj =  document.getElementById("primary_sponsor");
	} else if (spnsrIndex == 'S') {
		memberIdObj = document.getElementById('secondary_national_member_id')
		insuCompObj = getSponsorObj('secondary');
		sponsorObj = document.getElementById("secondary_sponsor");
	}

	if (memberIdObj != null) {
		var memberId = trimAll(memberIdObj.value);
		var mrNo = document.mainform.mrno.value;
		var visitId = document.mainform.visitId.value;

		if (!empty(memberId)) {
			var companyId = insuCompObj.value;

			if(!empty(mrNo) && !empty(visitId) && !empty(sponsorObj)
				&& !validateBabyAge(mrNo,visitId,sponsorObj,memberId)) {
				return true;
			}

			var ajaxobj = newXMLHttpRequest();
			var url = cpath + "/pages/registration/regUtils.do?_method=checkForDuplicateNationalMemberId" +
				"&member_id=" + encodeURIComponent(memberId) + "&sponsor_id=" + companyId + "&mr_no=" + mrNo
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var exists =" + ajaxobj.responseText);
						if (exists == "true") {
							showMessage("js.registration.patient.member.id.already.exists.string");
							memberIdObj.value = "";
							memberIdObj.focus();
							return false;
						} else
							return true;
					}
				}
			}
		}
	}
	return true;
}


var policyNoAutoComp = null;

function policyNoAutoComplete(spnsrIndex, gPatientPolciyNos) {

	var policyIdObj = null;
	if (spnsrIndex == 'S')
		policyIdObj = document.getElementById('secondary_member_id');
	else
		policyIdObj = document.getElementById('primary_member_id');

	if (policyNoAutoComp != null)
		policyNoAutoComp.destroy();

	var policyNoArray = [];
	if (!empty(gPatientPolciyNos)) {
		policyNoArray.length = gPatientPolciyNos.length;
		for (i = 0; i < gPatientPolciyNos.length; i++) {
			var item = gPatientPolciyNos[i]
			if(null != item.member_id)
			 policyNoArray[i] = item.member_id + "[" + item.insurance_co_name + "]";
		}
	}
	if (policyIdObj) {
		policyIdObj.disabled = policyIdObj.disabled;
	} else {
		return;
	}

	YAHOO.example.ACJSAddArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(policyNoArray);

		if (spnsrIndex == 'S')
			policyNoAutoComp = new YAHOO.widget.AutoComplete('secondary_member_id', 'secondaryMemberIdContainer', datasource);
		else
			policyNoAutoComp = new YAHOO.widget.AutoComplete('primary_member_id', 'primaryMemberIdContainer', datasource);

		policyNoAutoComp.formatResult = Insta.autoHighlight;
		policyNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
		policyNoAutoComp.typeAhead = false;
		policyNoAutoComp.useShadow = false;
		policyNoAutoComp.allowBrowserAutocomplete = false;
		policyNoAutoComp.queryMatchContains = true;
		policyNoAutoComp.minQueryLength = 0;
		policyNoAutoComp.maxResultsDisplayed = 20;
		policyNoAutoComp.forceSelection = false;
		policyNoAutoComp.itemSelectEvent.subscribe(loadPolicyDetails);
		// Tab key press from keyboard, #45951
		policyNoAutoComp.autoHighlight = false;
	}
}


function formatDueDate(dateMSecs) {
	var dateObj = new Date(dateMSecs);
	var dateStr = formatDate(dateObj, 'ddmmyyyy', '-');
	return dateStr;
}
var loadPolicyDetails = function (sType, aArgs) {
	var oData = aArgs[2];
	var acPolicyNo = (oData + "").split('[')[0];
	var acCompanyName = (((oData + "").split("[")[1]) + "").split("]")[0];
	var inputElmt = policyNoAutoComp.getInputEl();
	inputElmt.value = acPolicyNo;
	var spnsrIndex = (inputElmt.name.substr(0, 1)).toUpperCase();
	var insuCompObj = null;
	var planTypeObj = null;
	var planObj = null;
	var policyValidityStartObj = null;
	var policyValidityEndObj = null;
	var memberIdObj = null;
	var policyNumberObj = null;
	var policyHolderObj = null;
	var policyRelationObj = null;
	if (spnsrIndex == 'P') {
		insuCompObj = getInsuObj('primary');
		planTypeObj = getPlanTypeObj('primary');
		planObj = getPlanObj('primary');
		policyValidityStartObj = getPolicyValidityStartObj('primary');
		policyValidityEndObj = getPolicyValidityEndObj('primary');
		memberIdObj = getInsuranceMemberIdObj('primary');
		policyNumberObj = getInsurancePolicyNumberObj('primary');
		policyHolderObj = getPatientHolderObj('primary');
		policyRelationObj = getPatientRelationObj('primary');
	} else if (spnsrIndex == 'S') {
		insuCompObj = getInsuObj('secondary');
		planTypeObj = getPlanTypeObj('secondary');
		planObj = getPlanObj('secondary');
		policyValidityStartObj = getPolicyValidityStartObj('secondary');
		policyValidityEndObj = getPolicyValidityEndObj('secondary');
		memberIdObj = getInsuranceMemberIdObj('secondary');
		policyNumberObj = getInsurancePolicyNumberObj('secondary');
		policyHolderObj = getPatientHolderObj('secondary');
		policyRelationObj = getPatientRelationObj('secondary');
	}
	for (var i = 0; i < gPatientPolciyNos.length; i++) {
		var item = gPatientPolciyNos[i];
		if (acPolicyNo == item.member_id && getCompanyIdForCompanyName(acCompanyName) == item.insurance_co_id) {
			policyHolderObj.disabled = false;
			policyNumberObj.disabled = false;
			setSelectedIndex(insuCompObj, item.insurance_co_id);
			loadTpaList(spnsrIndex);
			tpaChange(spnsrIndex);
			setSelectedIndex(planTypeObj, item.category_id);
			insuCatChange(spnsrIndex);
			setSelectedIndex(planObj, item.plan_id);
			policyChange(spnsrIndex);
			RatePlanList();
			ratePlanChange();
			memberIdObj.value = acPolicyNo;
			policyNumberObj.value = item.policy_number;
			policyHolderObj.value = item.policy_holder_name;
			policyRelationObj.value = item.patient_relationship;
			// #45950
			// Ins30 : Corporate insurance validations removed
			/*if(corpInsuranceCheck == "Y"){
				setSelectedDateForCorpInsurance();
			} else 	{ */
				if(null != item.policy_validity_start && "" != item.policy_validity_start)
						policyValidityStartObj.value = formatDate(new Date(item.policy_validity_start), "ddmmyyyy", "-");
				if(null != item.policy_validity_end && "" != item.policy_validity_end)
					policyValidityEndObj.value = formatDate(new Date(item.policy_validity_end), "ddmmyyyy", "-");
			// }
			break;
		}
	}
}


function getCompanyIdForCompanyName(companyName) {
	if (empty(companyName)) {
		showMessage("js.registration.patient.member.id.valid.check.against.insurance.company");
		return null;
	}

	var companyId = null;
	if (!empty(gPatientPolciyNos)) {
		for (var i = 0; i < gPatientPolciyNos.length; i++) {
			if (gPatientPolciyNos[i].insurance_co_name == companyName) {
				companyId = gPatientPolciyNos[i].insurance_co_id;
				break;
			}
		}
	}
	return companyId;
}

var primaryCorporateNoAutoComp = null;
var secondaryCorporateNoAutoComp = null;

function corporateNoAutoComplete(spnsrIndex, gPatientCorporateIds) {

	var corporateNoArray = [];
	if (!empty(gPatientCorporateIds)) {
		corporateNoArray.length = gPatientCorporateIds.length;
		for (i = 0; i < gPatientCorporateIds.length; i++) {
			var item = gPatientCorporateIds[i]
			corporateNoArray[i] = item.employee_id + "[" + item.tpa_name + "]";
		}
	}

	YAHOO.example.ACJSAddArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(corporateNoArray);

		if (spnsrIndex == 'S') {

			if (secondaryCorporateNoAutoComp != null)
				secondaryCorporateNoAutoComp.destroy();

			secondaryCorporateNoAutoComp = new YAHOO.widget.AutoComplete('secondary_employee_id', 'secondaryCorporateIdContainer', datasource);
			secondaryCorporateNoAutoComp.formatResult = Insta.autoHighlight;
			secondaryCorporateNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			secondaryCorporateNoAutoComp.typeAhead = false;
			secondaryCorporateNoAutoComp.useShadow = false;
			secondaryCorporateNoAutoComp.allowBrowserAutocomplete = false;
			secondaryCorporateNoAutoComp.queryMatchContains = true;
			secondaryCorporateNoAutoComp.minQueryLength = 0;
			secondaryCorporateNoAutoComp.maxResultsDisplayed = 20;
			secondaryCorporateNoAutoComp.forceSelection = false;
			secondaryCorporateNoAutoComp.itemSelectEvent.subscribe(loadPatientCorporateDetails);
		} else {

			if (primaryCorporateNoAutoComp != null)
				primaryCorporateNoAutoComp.destroy();

			primaryCorporateNoAutoComp = new YAHOO.widget.AutoComplete('primary_employee_id', 'primaryCorporateIdContainer', datasource);
			primaryCorporateNoAutoComp.formatResult = Insta.autoHighlight;
			primaryCorporateNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			primaryCorporateNoAutoComp.typeAhead = false;
			primaryCorporateNoAutoComp.useShadow = false;
			primaryCorporateNoAutoComp.allowBrowserAutocomplete = false;
			primaryCorporateNoAutoComp.queryMatchContains = true;
			primaryCorporateNoAutoComp.minQueryLength = 0;
			primaryCorporateNoAutoComp.maxResultsDisplayed = 20;
			primaryCorporateNoAutoComp.forceSelection = false;
			primaryCorporateNoAutoComp.itemSelectEvent.subscribe(loadPatientCorporateDetails);
		}
	}
}

var loadPatientCorporateDetails = function (sType, aArgs) {
	var oData = aArgs[2];
	var acCorporateNo = (oData + "").split('[')[0];
	var acSponsorName = (((oData + "").split("[")[1]) + "").split("]")[0];

	var inputElmt = this.getInputEl()
	var spnsrIndex = (inputElmt.name.substr(0, 1)).toUpperCase();

	var tpaObj = null;
	var employeeIdObj = null;
	var empNameObj = null;
	var empRelationObj = null;

	if (spnsrIndex == 'P') {
		tpaObj = getSponsorObj('primary');
		employeeIdObj = getInsuranceMemberIdObj('primary');
		empNameObj = getPatientHolderObj('primary');
		empRelationObj = getPatientRelationObj('primary');

	} else if (spnsrIndex == 'S') {
		tpaObj = getSponsorObj('secondary');
		employeeIdObj = getInsuranceMemberIdObj('secondary');
		empNameObj = getPatientHolderObj('secondary');
		empRelationObj = getPatientRelationObj('secondary');
	}

	if (!empty(gPatientCorporateIds)) {
		for (var i = 0; i < gPatientCorporateIds.length; i++) {
			var item = gPatientCorporateIds[i];
			if (acCorporateNo == item.employee_id && getSponsorIdForCorporateSponsorName(acSponsorName) == item.sponsor_id) {

				setSelectedIndex(tpaObj, item.sponsor_id);
				loadTpaList(spnsrIndex);
				onCorporateChange(spnsrIndex);

				RatePlanList();
				ratePlanChange();
				employeeIdObj.value = acCorporateNo;
				empNameObj.value = item.employee_name;
				empRelationObj.value = item.patient_relationship;
				break;
			}
		}
	}
}

	function getSponsorIdForCorporateSponsorName(sponsorName) {
		if (empty(sponsorName)) {
			showMessage("js.registration.patient.employee.id.valid.check.against.sponsor");
			return null;
		}

		var sponsorId = null;
		if (!empty(gPatientCorporateIds)) {
			for (var i = 0; i < gPatientCorporateIds.length; i++) {
				if (gPatientCorporateIds[i].tpa_name == sponsorName) {
					sponsorId = gPatientCorporateIds[i].tpa_id;
					break;
				}
			}
		}
		return sponsorId;
	}

var primaryNationalNoAutoComp = null;
var secondaryNationalNoAutoComp = null;

function nationalNoAutoComplete(spnsrIndex, gPatientNationalIds) {

	var nationalNoArray = [];
	if (!empty(gPatientNationalIds)) {
		nationalNoArray.length = gPatientNationalIds.length;
		for (i = 0; i < gPatientNationalIds.length; i++) {
			var item = gPatientNationalIds[i]
			nationalNoArray[i] = item.national_id + "[" + item.tpa_name + "]";
		}
	}

	YAHOO.example.ACJSAddArray = new function () {
		datasource = new YAHOO.widget.DS_JSArray(nationalNoArray);

		if (spnsrIndex == 'S') {

			if (secondaryNationalNoAutoComp != null)
				secondaryNationalNoAutoComp.destroy();

			secondaryNationalNoAutoComp = new YAHOO.widget.AutoComplete('secondary_national_member_id', 'secondaryNationalIdContainer', datasource);
			secondaryNationalNoAutoComp.formatResult = Insta.autoHighlight;
			secondaryNationalNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			secondaryNationalNoAutoComp.typeAhead = false;
			secondaryNationalNoAutoComp.useShadow = false;
			secondaryNationalNoAutoComp.allowBrowserAutocomplete = false;
			secondaryNationalNoAutoComp.queryMatchContains = true;
			secondaryNationalNoAutoComp.minQueryLength = 0;
			secondaryNationalNoAutoComp.maxResultsDisplayed = 20;
			secondaryNationalNoAutoComp.forceSelection = false;
			secondaryNationalNoAutoComp.itemSelectEvent.subscribe(loadPatientNationalDetails);
		} else {

			if (primaryNationalNoAutoComp != null)
				primaryNationalNoAutoComp.destroy();

			primaryNationalNoAutoComp = new YAHOO.widget.AutoComplete('primary_national_member_id', 'primaryNationalIdContainer', datasource);
			primaryNationalNoAutoComp.formatResult = Insta.autoHighlight;
			primaryNationalNoAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			primaryNationalNoAutoComp.typeAhead = false;
			primaryNationalNoAutoComp.useShadow = false;
			primaryNationalNoAutoComp.allowBrowserAutocomplete = false;
			primaryNationalNoAutoComp.queryMatchContains = true;
			primaryNationalNoAutoComp.minQueryLength = 0;
			primaryNationalNoAutoComp.maxResultsDisplayed = 20;
			primaryNationalNoAutoComp.forceSelection = false;
			primaryNationalNoAutoComp.itemSelectEvent.subscribe(loadPatientNationalDetails);
		}
	}
}

var loadPatientNationalDetails = function (sType, aArgs) {
	var oData = aArgs[2];
	var acNationalNo = (oData + "").split('[')[0];
	var acSponsorName = (((oData + "").split("[")[1]) + "").split("]")[0];

	var inputElmt = this.getInputEl()
	var spnsrIndex = (inputElmt.name.substr(0, 1)).toUpperCase();

	var tpaObj = null;
	var nationalIdObj = null;
	var citizenNameObj = null;
	var patRelationObj = null;

	if (spnsrIndex == 'P') {
		tpaObj = getSponsorObj('primary');
		nationalIdObj = getInsuranceMemberIdObj('primary');
		citizenNameObj = getPatientHolderObj('primary');
		patRelationObj = getPatientRelationObj('primary');

	} else if (spnsrIndex == 'S') {
		tpaObj = getSponsorObj('secondary');
		nationalIdObj = getInsuranceMemberIdObj('secondary');
		citizenNameObj = getPatientHolderObj('secondary');
		patRelationObj = getPatientRelationObj('secondary');
	}

	if (!empty(gPatientNationalIds)) {
		for (var i = 0; i < gPatientNationalIds.length; i++) {
			var item = gPatientNationalIds[i];
			if (acNationalNo == item.national_id && getSponsorIdForNationalSponsorName(acSponsorName) == item.sponsor_id) {

				setSelectedIndex(tpaObj, item.sponsor_id);
				loadTpaList(spnsrIndex);
				onNationalSponsorChange(spnsrIndex);

				RatePlanList();
				ratePlanChange();
				nationalIdObj.value = acNationalNo;
				citizenNameObj.value = item.citizen_name;
				patRelationObj.value = item.patient_relationship;
				break;
			}
		}
	}
}

	function getSponsorIdForNationalSponsorName(sponsorName) {
		if (empty(sponsorName)) {
			showMessage("js.registration.patient.national.id.valid.check.against.sponsor");
			return null;
		}

		var sponsorId = null;
		if (!empty(gPatientNationalIds)) {
			for (var i = 0; i < gPatientNationalIds.length; i++) {
				if (gPatientNationalIds[i].tpa_name == sponsorName) {
					sponsorId = gPatientNationalIds[i].tpa_id;
					break;
				}
			}
		}
		return sponsorId;
	}


var gBothInsuranceSponsors = false;

function loadInsurancePolicyDetails() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");

	var loadPreviousInsDetails = false;
	var allowInsurance = true;

	primarySponsorObj.value = gPreviousPrimarySponsorIndex;
	onChangePrimarySponsor();
	secondarySponsorObj.value = gPreviousSecondarySponsorIndex;
	onChangeSecondarySponsor();

	if (gPreviousPrimarySponsorIndex == 'I')
		loadInsuranceDetails('P');
	else if (gPreviousPrimarySponsorIndex == 'C')
		loadCorporateDetails('P');
	else if (gPreviousPrimarySponsorIndex == 'N')
		loadNationalDetails('P');

	if (gPreviousSecondarySponsorIndex == 'I')
		loadInsuranceDetails('S');
	else if (gPreviousSecondarySponsorIndex == 'C')
		loadCorporateDetails('S');
	else if (gPreviousSecondarySponsorIndex == 'N')
		loadNationalDetails('S');

	if (gPreviousSecondarySponsorIndex == 'I' && gPreviousPrimarySponsorIndex == 'I') {
		gBothInsuranceSponsors = true;
		loadInsuranceDetails('S');
		loadInsuranceDetails('P');
	}
}


function loadCorporateDetails(spnsrIndex) {

	var corporateSpnsrObj = null;
	var employeeIdObj = null;
	var employeeNameObj = null;
	var patientRelationshipObj = null;
	var insuranceApprovalObj = null;

	if (spnsrIndex == 'P') {
		loadTpaList('P');
		onCorporateChange('P');
		corporateSpnsrObj = document.getElementById("primary_corporate");
		employeeIdObj = document.getElementById("primary_employee_id");
		employeeNameObj = document.getElementById("primary_employee_name");
		patientRelationshipObj = document.getElementById("primary_employee_relation");
		insuranceApprovalObj = document.getElementById("primary_corporate_approval");

		setSelectedIndex(corporateSpnsrObj, gPreviousPrimaryTpa);
		onCorporateChange('P');
		employeeIdObj.value = gPreviousCorporateEmployeeId;
		employeeNameObj.value = gPreviousCorporateEmployeeName;
		patientRelationshipObj.value = gPreviousCorporateRelation;
		insuranceApprovalObj.value = gPrimaryApproval;

	} else if (spnsrIndex == 'S') {
		loadTpaList('S');
		onCorporateChange('S');
		corporateSpnsrObj = document.getElementById("secondary_corporate");
		employeeIdObj = document.getElementById("secondary_employee_id");
		employeeNameObj = document.getElementById("secondary_employee_name");
		patientRelationshipObj = document.getElementById("secondary_employee_relation");
		insuranceApprovalObj = document.getElementById("secondary_corporate_approval");

		setSelectedIndex(corporateSpnsrObj, gPreviousSecondaryTpa);
		onCorporateChange('S');
		employeeIdObj.value = gPreviousSecCorporateEmployeeId;
		employeeNameObj.value = gPreviousSecCorporateEmployeeName;
		patientRelationshipObj.value = gPreviousSecCorporateRelation;
		insuranceApprovalObj.value = gSecondaryApproval;
	}
}

function loadNationalDetails(spnsrIndex) {

	var nationalSpnsrObj = null;
	var nationalIdObj = null;
	var nationalMemberNameObj = null;
	var patientRelationshipObj = null;
	var insuranceApprovalObj = null;

	if (spnsrIndex == 'P') {
		loadTpaList('P');
		onNationalSponsorChange('P');
		nationalSpnsrObj = document.getElementById("primary_national_sponsor");
		nationalIdObj = document.getElementById("primary_national_member_id");
		nationalMemberNameObj = document.getElementById("primary_national_member_name");
		patientRelationshipObj = document.getElementById("primary_national_relation");
		insuranceApprovalObj = document.getElementById("primary_national_approval");

		setSelectedIndex(nationalSpnsrObj, gPreviousPrimaryTpa);
		onNationalSponsorChange('P');
		nationalIdObj.value = gPreviousNationalId;
		nationalMemberNameObj.value = gPreviousNationalCitizenName;
		patientRelationshipObj.value = gPreviousNationalRelation;
		insuranceApprovalObj.value = gPrimaryApproval;

	} else if (spnsrIndex == 'S') {
		loadTpaList('S');
		onNationalSponsorChange('S');
		nationalSpnsrObj = document.getElementById("secondary_national_sponsor");
		nationalIdObj = document.getElementById("secondary_national_member_id");
		nationalMemberNameObj = document.getElementById("secondary_national_member_name");
		patientRelationshipObj = document.getElementById("secondary_national_relation");
		insuranceApprovalObj = document.getElementById("secondary_national_approval");

		setSelectedIndex(nationalSpnsrObj, gPreviousSecondaryTpa);
		onNationalSponsorChange('S');
		nationalIdObj.value = gPreviousSecNationalId;
		nationalMemberNameObj.value = gPreviousSecNationalCitizenName;
		patientRelationshipObj.value = gPreviousSecNationalRelation;
		insuranceApprovalObj.value = gSecondaryApproval;
	}
}


/**
  * disable (possible values are true/false)
  		= parameter which indicates whether insurance fields are to be disabled (true) or enabled (false).
  * forceEnable (possible values are true/false)
  		= parameter which ignores disable (above parameter) and forcefully enables or disables the insurance fields.
 */

function disableOrEnableInsuranceFields(disable, forceEnable) {

	/** If mod_adv_ins is not enabled, and bill type selected is Prepaid then the insurance fields are disabled.
	   Also if mod_adv_ins is enabled and Visit type selected is Follow up the the insurance fields are disabled. */

	var allowInsurance = true;

	var isFollowUpVisit = (!empty(pOpType) && pOpType != 'M' && pOpType != 'R');

	if (typeof (forceEnable) == 'undefined') {

		if (allowInsurance) {
			if (isFollowUpVisit) disable = true;
			else disable = disable;
		} else
			disable = true;

	} else disable = forceEnable;

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var primarySponsorWrapperObj = document.getElementById("primary_sponsor_wrapper");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");

	var primarySpnsrType = primarySponsorObj.value;
	var secondarySpnsrType = secondarySponsorObj.value;

	primarySponsorObj.disabled = disable;
	primarySponsorWrapperObj.disabled = disable;

	secondarySponsorWrapperObj.disabled = disable;


	if(corpInsuranceCheck = 'Y')
		secondarySponsorObj.disabled = true;

	if (primarySpnsrType == '') {
		secondarySponsorWrapperObj.disabled = true;
		if (secondarySponsorObj != null) secondarySponsorObj.value = '';
	} else if(corpInsuranceCheck = 'N')
		secondarySponsorObj.disabled = disable;

	// var approvalAmtEnableDisable = !isMain && !isRevisit;

	// For bug # 29283 Follow-Up visits, per visit co-pay is not applicable.
	// Hence, enabling approval amount field for follow up also
	var approvalAmtEnableDisable = false;

	if (primarySponsorObj != null) {
		if (primarySpnsrType == 'I') {
			enableDisablePrimaryInsuranceDetails(disable, approvalAmtEnableDisable);

		} else if (primarySpnsrType == 'C') {
			enableDisablePrimaryCorporateDetails(disable, approvalAmtEnableDisable);

		} else if (primarySpnsrType == 'N') {
			enableDisablePrimaryNationalDetails(disable, approvalAmtEnableDisable);
		} else {}
	}
	if (secondarySponsorObj != null) {
		if (secondarySpnsrType == 'I') {
			enableDisableSecondaryInsuranceDetails(disable, approvalAmtEnableDisable);

		} else if (secondarySpnsrType == 'C') {
			enableDisableSecondaryCorporateDetails(disable, approvalAmtEnableDisable);

		} else if (secondarySpnsrType == 'N') {
			enableDisableSecondaryNationalDetails(disable, approvalAmtEnableDisable);
		} else {}
	}

	var organizationObj = document.mainform.organization;
	if (organizationObj != null) organizationObj.disabled = disable;
}

/*
 * This function loads the insurance details:
 * - for new insurance selected
 * - or to default the values of the old one.
 */

function loadInsuranceDetails(spnsrIndex) {

	var loadPreviousInsDetails = true;
	var allowInsurance = true;

	var insCompObj = null;
	var tpaObj = null;
	var tpaNameObj=null;
	var planObj = null;
	var planTypeObj = null;
	var memberIdObj = null;
	var policyStartDtObj = null;
	var policyEndDtObj = null;
	var policyNumberObj = null;
	var policyHolderObj = null;
	var patientRelationshipObj = null;
	var insuranceApprovalObj = null;
	var patientInsPlanObj = null;

	var previousInsCompany = null;
	var previousTpa = null;
	var drgObject = null;
	var perdiemObject = null;
	var authIdObj = null;
	var authModeIdObj = null;

	var patientCategoryId = '';
	var insCompId = '';
	var tpaId = '';
	var planId = '';
	var planTypeId = '';
	var ratePlanId = '';

	var memberId = '';
	var policyNumber = '';
	var policyStart = '';
	var policyEnd = '';
	var holder = '';
	var relation = '';

	var validPatientCategoryId = '';
	var validPlanType = '';
	var validInsComp = '';
	var validTpa = '';
	var validMemberId = '';
	var validRatePlan = '';
	var approvalAmt = '';

	var previousUseDrg = '';
	var previousUsePerdiem = '';

	var previousPlan = null;
	var previousPlanType = null;
	var previousMemberId = null;
	var previousPolicyNumber = null;
	var previousHolder = null;
	var previousRelation = null;
	var previousStartDate = null;
	var previousEndDate = null;
	var previousPatientPolicyId = null;
	var previousPriorauthid = null;
	var previousPriorauthmodeid = null;
	var previousPatientPolicyId = null;
	
	var patientInsrancePlanId = null;

	if (spnsrIndex == 'P') {
		insCompObj = getInsuObj('primary');
		tpaObj = getSponsorObj('primary');
		tpaNameObj= getSponsorNameObj('primary');
		planObj = getPlanObj('primary');
		planTypeObj = getPlanTypeObj('primary');
		memberIdObj = getInsuranceMemberIdObj('primary');
		policyStartDtObj = getPolicyValidityStartObj('primary');
		policyEndDtObj = getPolicyValidityEndObj('primary');
		policyNumberObj = getInsurancePolicyNumberObj('primary');
		policyHolderObj = getPatientHolderObj('primary');
		patientRelationshipObj = getPatientRelationObj('primary');
		insuranceApprovalObj = getApprovalLimitObj('primary');
		patientInsPlanObj = getPatientInsurancePlanObj('primary');

		previousInsCompany = gPreviousPrimaryInsCompany;
		previousTpa = gPreviousPrimaryTpa;
		approvalAmt = gPrimaryApproval;
		drgObject = getPrimaryDRGCheckObj();
		perdiemObject = getPrimaryPerdiemCheckObj();
		authIdObj = document.getElementById("primary_prior_auth_id");
		authModeIdObj = getPrimaryAuthModeIdObj();

		previousPlan = gPreviousPlan;
		previousPlanType = gPreviousPlanType;
		previousMemberId = gPreviousMemberId;
		previousPolicyNumber = gPreviousPolicyNumber;
		previousHolder = gPreviousHolder;
		previousRelation = gPreviousRelation;
		previousStartDate = gPreviousStartDate;
		previousEndDate = gPreviousEndDate;
		previousUseDrg = gUseDrg;
		previousUsePerdiem = gUsePerdiem;
		previousPriorauthid = gPriorAuthId;
		previousPriorauthmodeid = gPriorAuthModeId;
		previousPatientPolicyId = gPreviousPatientPolicyId;
		
		patientInsrancePlanId = gPrimaryPatientInsurancePlanId;

	} else if (spnsrIndex == 'S') {
		insCompObj = getInsuObj('secondary');
		tpaObj = getSponsorObj('secondary');
		tpaNameObj= getSponsorNameObj('secondary');
		planObj = getPlanObj('secondary');
		planTypeObj = getPlanTypeObj('secondary');
		memberIdObj = getInsuranceMemberIdObj('secondary');
		policyStartDtObj = getPolicyValidityStartObj('secondary');
		policyEndDtObj = getPolicyValidityEndObj('secondary');
		policyNumberObj = getInsurancePolicyNumberObj('secondary');
		policyHolderObj = getPatientHolderObj('secondary');
		patientRelationshipObj = getPatientRelationObj('secondary');
		insuranceApprovalObj = getApprovalLimitObj('secondary');
		patientInsPlanObj = getPatientInsurancePlanObj('secondary');

		previousInsCompany = gPreviousSecondaryInsCompany;
		previousTpa = gPreviousSecondaryTpa;
		approvalAmt = gSecondaryApproval;
		drgObject = getSecondaryDRGCheckObj();
		perdiemObject = getSecondaryPerdiemCheckObj();
		authIdObj = document.getElementById("secondary_prior_auth_id");
		authModeIdObj = getSecondaryAuthModeIdObj();

		previousPlan = gPreviousSecPlan;
		previousPlanType = gPreviousSecPlanType;
		previousMemberId = gPreviousSecMemberId;
		previousPolicyNumber = gPreviousSecPolicyNumber;
		previousHolder = gPreviousSecHolder;
		previousRelation = gPreviousSecRelation;
		previousStartDate = gPreviousSecStartDate;
		previousEndDate = gPreviousSecEndDate;
		previousUseDrg = gSecondaryUseDrg;
		previousUsePerdiem = gSecondaryUsePerdiem;
		previousPriorauthid = gPreviousSecPriorauthid;
		previousPriorauthmodeid = gPreviousSecPriorauthmodeid;
		previousPatientPolicyId = gPreviousSecPatientPolicyId;
		
		patientInsrancePlanId = gSecondaryPatientInsurancePlanId;
		
	}
	
	if (previousTpa != "" && previousInsCompany != "" && previousPlanType != "") {
		policynames = mergePolicyNames(policynames, sendPolicyNamesAjax(previousPlanType, previousInsCompany, previousTpa));
	}

	if (previousInsCompany != "") {
		planTypeAjax = getPlanTypesAjax(previousInsCompany);
		if(typeof insuCatNames !== 'undefined'){
			insuCatNames = insuCatNames.concat(planTypeAjax.categoryLists);
		}else{
			insuCatNames = planTypeAjax.categoryLists;
		}	
	}

	patientInsPlanObj.value = patientInsrancePlanId;
	
	validRatePlan = gPreviousRatePlan;
	var organizationObj = document.mainform.organization;
	var patientCategoryObj = document.mainform.patient_category_id;

	var patcategory = findInList(categoryJSON, "category_id", patientCategory);

	patientCategoryId = patientCategory;

	ratePlanId = gPreviousRatePlan;
	tpaId = previousTpa;
	insCompId = previousInsCompany;
	
	
	
	var plan = findInList(policynames, "plan_id", previousPlan);

	if (!empty(validRatePlan) || (plan != null && plan.status == 'A')) {
		planId = previousPlan;
		if ((plan != null && plan.status == 'A'))
			validPlanType = plan.category_id;
		if (empty(validRatePlan))
			validRatePlan = plan.default_rate_plan;
	}


	if (validPlanType == previousPlanType) {
		var plantype = findInList(insuCatNames, "category_id", previousPlanType);

		if (plantype != null && plantype.status == 'A') {
			planTypeId = previousPlanType;
			validInsComp = plantype.insurance_co_id;
		}
	}

	// check if previous rate plan is valid.
	if (validRatePlan == gPreviousRatePlan) {
		var ratePlan = findInList(orgNamesJSON, "org_id", gPreviousRatePlan);
		if (ratePlan != null && ratePlan.status == 'A') {
			ratePlanId = gPreviousRatePlan;
		}
	}

	var tpanameval='';
	var tpaList;
	if (validInsComp == previousInsCompany) {
		var inscomp = findInList(insuCompanyDetails, "insurance_co_id", previousInsCompany);

		if (inscomp != null && inscomp != '' && inscomp.status == 'A') {
			insCompId = previousInsCompany;

			tpaList = filterList(companyTpaList, 'insurance_co_id', previousInsCompany);

			if (empty(tpaList)) {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				if (tpa != null && tpa.status == 'A') {
					tpaId = previousTpa;
					validTpa = tpa.tpa_id;
					tpanameval= tpa.tpa_name;
				}
			} else {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				if (tpa != null && tpa.status == 'A') {
					tpaId = previousTpa;
					validTpa = tpa.tpa_id;
					tpanameval= tpa.tpa_name;
				}
			}

			// Rate plan related to insurance company
			if (empty(ratePlanId) && !empty(inscomp.default_rate_plan)) {
				validRatePlan = inscomp.default_rate_plan;
			}

			if (validRatePlan == gPreviousRatePlan) {
				var ratePlan = findInList(orgNamesJSON, "org_id", gPreviousRatePlan);

				if (ratePlan != null && ratePlan.status == 'A') {
					ratePlanId = gPreviousRatePlan;
				}
			}
		}
	} else {
		if(null == tpaList && empty(tpaList))
			tpaList=tpanames

		var tpa = findInList(tpaList, "tpa_id", previousTpa);
		if (tpa != null && tpa.status == 'A') {
			tpaId = previousTpa;
			validTpa = tpa.tpa_id;
			tpanameval= tpa.tpa_name;
		}
		var ratePlan = orgNamesJSON;
		ratePlanId = gPreviousRatePlan;
	}

	// If mod_adv_ins (or) mod_insurance is enabled, set the insurance company & tpa.
	if (loadPreviousInsDetails) {
		if (isModAdvanceIns) {

			if (patientCategoryObj != null) {
				setSelectedIndex(patientCategoryObj, patientCategoryId);
			}

			if (insCompObj != null) {
				loadInsuranceCompList(spnsrIndex);
				setSelectedIndex(insCompObj, insCompId);
				//loadTpaList(spnsrIndex);
			}

			if (empty(insCompId))
				loadTpaList(spnsrIndex);
			if (tpaObj != null) {
				//setSelectedIndex(tpaObj, tpaId);
				tpaObj.value=tpaId;
				tpaNameObj.value=tpanameval;
				enableOtherInsuranceDetailsTab(spnsrIndex);
				tpaChange(spnsrIndex);
				forEditInsAndfollowUps(spnsrIndex);
			}
		} else if (isModInsurance) {

			if (patientCategoryObj != null) {
				setSelectedIndex(patientCategoryObj, gPreviousPatientCategoryId);
			}

			if (insCompObj != null) {
				loadInsuranceCompList(spnsrIndex);
				setSelectedIndex(insCompObj, previousInsCompany);
				//loadTpaList(spnsrIndex);
			}

			if (empty(previousInsCompany))
				loadTpaList(spnsrIndex);

			if (tpaObj != null) {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				tpaObj.value= previousTpa;
				tpaNameObj.value=tpa.tpa_name;
				tpaChange(spnsrIndex);
				enableOtherInsuranceDetailsTab(spnsrIndex);
				forEditInsAndfollowUps(spnsrIndex);
			}
		} else {
			if (patientCategoryObj != null)
				setSelectedIndex(patientCategoryObj, gPreviousPatientCategoryId);
			setSelectedIndex(insCompObj, previousInsCompany);
			loadTpaList(spnsrIndex);
			//setSelectedIndex(tpaObj, previousTpa);
			if (tpaObj != null) {
				var tpa = findInList(tpanames, "tpa_id", previousTpa);
				tpaObj.value= previousTpa;
				tpaNameObj.value=tpa.tpa_name;
				tpaChange(spnsrIndex);
				enableOtherInsuranceDetailsTab(spnsrIndex);
				forEditInsAndfollowUps(spnsrIndex);
			}
		}
	} else {
		if (patientCategoryObj != null)
			setSelectedIndex(patientCategoryObj, "");
		setSelectedIndex(insCompObj, "");
		setSelectedIndex(tpaObj, "");
	}

	// Set the plan type, plan and the validity dates if the membership id validity has not expired.
	//if (isModAdvanceIns) {
		if (loadPreviousInsDetails) {
			//if (!gBothInsuranceSponsors || (gBothInsuranceSponsors && spnsrIndex == 'P')) {
				loadTpaList(spnsrIndex);
				setSelectedIndex(planTypeObj, planTypeId);
				insuCatChange(spnsrIndex);
				setSelectedIndex(planObj, planId);
				enableDisablePlanDetails(spnsrIndex,planObj);
				setDateObjectsForValidityPeriod();
				policyChange(); // skipped this since the mandatory fields (red star mark and the overall treatment amount not required to be loaded)
				/*if (policyStart != '') policyStartDtObj.value = formatDate(new Date(policyStart), "ddmmyyyy", "-");
				else policyStartDtObj.value = '';

				if (policyEnd != '') policyEndDtObj.value = formatDate(new Date(policyEnd), "ddmmyyyy", "-");
				else policyEndDtObj.value = '';*/

				policyStartDtObj.disabled = false;
				policyEndDtObj.disabled = false;
			}else {
				setSelectedIndex(planTypeObj, "");
				insuCatChange(spnsrIndex);
				setSelectedIndex(planObj, "");
				policyStartDtObj.value = '';
				policyEndDtObj.value = '';
			}
	//}
		//check if plan validity has expired.
	if (!empty(gPatientPolciyNos) && (gPatientPolciyNos.length > 0)) {

		var member = findInList(gPatientPolciyNos, "patient_policy_id", previousPatientPolicyId);
		if (member != null) validMemberId = member.member_id;
		memberId = previousMemberId;
		policyStart = previousStartDate;
		policyEnd = previousEndDate;
		policyNumber = previousPolicyNumber;
		holder = previousHolder;
		relation = previousRelation;

		var item = findInList(tpanames, "tpa_id", tpaId);

		if(null != item && item !=""){
			for(var i=0;i<sponsorTypeList.length;i++){
				if(sponsorTypeList[i].sponsor_type_id==item.sponsor_type_id){
					if(sponsorTypeList[i].validity_period_mandatory == 'Y' && sponsorTypeList[i].member_id_mandatory == 'Y'){
						if (isModAdvanceIns && member != null && validMemberId != null && validMemberId == previousMemberId) {
							var d = new Date(gServerNow);
							d.setHours(0);
							d.setSeconds(0);
							d.setMinutes(0);
							d.setMilliseconds(0);
							var diff = new Date(previousEndDate) - d;
							if (diff >= 0) {
								policyEnd = previousEndDate;
							} else {
								memberId = '';
								policyNumber = '';
								policyStart = '';
								holder = '';
								relation = '';
								alert(getString("js.registration.patient.policy.validity.has.expired.on.string")
										+ " " + formatDate(new Date(previousEndDate), 'ddmmyyyy', '-')
										+ "\n" + getString("js.registration.patient.select.another.string") + " "
										+ "\n" + getString("js.registration.patient.or.string") + "\n"
										+ getString("js.registration.patient.enter.validity.end.date.string"));
								policyEndDtObj.focus();
							}
						}
					}

					if(sponsorTypeList[i].validity_period_editable == 'N'){
						policyStartDtObj.readOnly = true;
						policyEndDtObj.readOnly = true;
					}
				}
			}
		}
	}
	// If mod_adv_ins (or) mod_insurance is enabled, set the member id, policy holder and relationship details.
	if (loadPreviousInsDetails) {
		//if (isModAdvanceIns) {
			//if (!gBothInsuranceSponsors || (gBothInsuranceSponsors && spnsrIndex == 'P')) {
				memberIdObj.value = memberId;
				policyNumberObj.value = policyNumber;
				policyHolderObj.value = holder;
				patientRelationshipObj.value = relation;
			//}
		//}
	} else /*if (isModAdvanceIns)*/ {
		memberIdObj.value = "";
		policyNumberObj.value = "";
		policyHolderObj.value = "";
		patientRelationshipObj.value = "";
	}

	// Set the rate plan -- If mod_adv_ins, set the rate plan according to the above criteria filtered.
	// In other cases i.e if mod_insurance (or) no insurance, set the previous rate plan.
	RatePlanList();

	if (loadPreviousInsDetails) {
		if (previousUseDrg != null && previousUseDrg != '' && previousUseDrg != 'N' && drgObject != null) {
			drgObject.checked = true;
			checkUseDRG(spnsrIndex);
		}

		if (previousUsePerdiem != null && previousUsePerdiem != '' && previousUsePerdiem != 'N' && perdiemObject != null) {
			perdiemObject.checked = true;
			checkUsePerdiem(spnsrIndex);
		}

		if (approvalAmt != null && approvalAmt != '' && insuranceApprovalObj != null) {
			insuranceApprovalObj.value = approvalAmt;
		}

		if (authIdObj != null) {
			//if (!gBothInsuranceSponsors || (gBothInsuranceSponsors && spnsrIndex == 'P')) {
				authIdObj.value = previousPriorauthid;
			//}
		}

		if (authModeIdObj != null) {
			//if (!gBothInsuranceSponsors || (gBothInsuranceSponsors && spnsrIndex == 'P')) {
				setSelectedIndex(authModeIdObj, previousPriorauthmodeid);
			//}
		}
	}
}


function setDocUpdated(currObj, sindex) {
	var primaryDocUpdated = document.getElementById("primaryDocUpdated");
	var secondaryDocUpdated = document.getElementById("secondaryDocUpdated");
	if (isCopyPaste == "true") {
		primaryDocUpdated.value = 'X';
		secondaryDocUpdated.value = 'X';
		return;
	}

	if (sindex == 'P' && currObj.value != null && currObj.value != '') {
		primaryDocUpdated.value = 'Y';
	} else if (sindex == 'P') {
		primaryDocUpdated.value = 'N';
	}
	if (sindex == 'S' && currObj.value != null && currObj.value != '') {
		secondaryDocUpdated.value = 'Y';
	} else if (sindex == 'S') {
		secondaryDocUpdated.value = 'N';
	}
}

/* Insurance functions used in registration */

function onChangePrimarySponsor() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (document.getElementById("primary_sponsor_wrapper").checked)
		primarySponsorObj.value = 'I';
	else
		primarySponsorObj.value = "";
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");
	if (primarySponsorObj != null && primarySponsorObj.value != '') {
		document.getElementById("primarySponsorGroup").style.display = 'block';
		if(corpInsuranceCheck == 'N')
		{
			secondarySponsorObj.disabled = false;
			secondarySponsorWrapperObj.disabled = false;
		}
		if (primarySponsorObj.value == 'I') {
			// Ins30 : Corporate insurance validations removed
			/*if(corpInsuranceCheck == 'Y' && document.getElementById("primary_sponsor_id") != null){
				document.getElementById("primary_sponsor_id").value = '';
				document.getElementById("primary_sponsor_name").value = '';
			}*/
			if(document.getElementById("primary_sponsor_id") != null){
				document.mainform.primary_sponsor_id.value = '';
				document.mainform.primary_sponsor_name.value = '';
				resetSponsorDetailsTabs('P');
				resetPrimaryHiddenFieldsRelatedToInsurance();
			}
			document.getElementById("primaryInsuranceTab").style.display = 'block';
			document.getElementById("primaryCorporateTab").style.display = 'none';
			document.getElementById("primaryNationalTab").style.display = 'none';
			resetPrimaryCorporateDetails();
			resetPrimaryNationalDetails();

		} else if (primarySponsorObj.value == 'C') {
			document.getElementById("primaryCorporateTab").style.display = 'block';
			document.getElementById("primaryInsuranceTab").style.display = 'none';
			document.getElementById("primaryNationalTab").style.display = 'none';
			resetPrimaryInsuranceDetails();
			resetPrimaryNationalDetails();

		} else {
			document.getElementById("primaryNationalTab").style.display = 'block';
			document.getElementById("primaryInsuranceTab").style.display = 'none';
			document.getElementById("primaryCorporateTab").style.display = 'none';
			resetPrimaryCorporateDetails();
			resetPrimaryInsuranceDetails();
		}
	} else {
		resetSponsorDetailsTabs('P');
		resetPrimaryHiddenFieldsRelatedToInsurance();
		resetPrimaryInsuranceDetails();
		resetPrimaryCorporateDetails();
		resetPrimaryNationalDetails();
		document.getElementById("secondary_sponsor").value = '';
		secondarySponsorObj.disabled = true;
		secondarySponsorWrapperObj.disabled = true;
		secondarySponsorWrapperObj.checked = false;
		document.getElementById("primarySponsorGroup").style.display = 'none';
		document.getElementById("secondarySponsorGroup").style.display = 'none';
	}
	setPrimarySponsorDefaults();
}

function onChangeSecondarySponsor() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (document.getElementById("secondary_sponsor_wrapper").checked)
		secondarySponsorObj.value = 'I';
	else
		secondarySponsorObj.value = '';

	if (secondarySponsorObj != null && secondarySponsorObj.value != '') {
		document.getElementById("secondarySponsorGroup").style.display = 'block';

		if (secondarySponsorObj.value == 'I') {
			document.getElementById("secondaryInsuranceTab").style.display = 'block';
			document.getElementById("secondaryCorporateTab").style.display = 'none';
			document.getElementById("secondaryNationalTab").style.display = 'none';
			resetSecondaryCorporateDetails();
			resetSecondaryNationalDetails();

		} else if (secondarySponsorObj.value == 'C') {
			document.getElementById("secondaryCorporateTab").style.display = 'block';
			document.getElementById("secondaryInsuranceTab").style.display = 'none';
			document.getElementById("secondaryNationalTab").style.display = 'none';
			resetSecondaryInsuranceDetails();
			resetSecondaryNationalDetails();
		} else {
			document.getElementById("secondaryNationalTab").style.display = 'block';
			document.getElementById("secondaryInsuranceTab").style.display = 'none';
			document.getElementById("secondaryCorporateTab").style.display = 'none';
			resetSecondaryInsuranceDetails();
			resetSecondaryCorporateDetails();
		}
	} else {
		resetSponsorDetailsTabs('S');
		resetSecondaryHiddenFieldsRelatedToInsurance();
		document.getElementById("secondarySponsorGroup").style.display = 'none';
		resetSecondaryInsuranceDetails();
		resetSecondaryCorporateDetails();
		resetSecondaryNationalDetails();
	}
	setSecondarySponsorDefaults();
}

function resetPrimaryInsuranceDetails() {
	var primarySponsorIdObj = document.getElementById("primary_sponsor_id");
	if (primarySponsorIdObj != null) {
		primarySponsorIdObj.selectedIndex = 0;
		onTpaChange('P');
	}

	var primaryInsuranceIdObj = document.getElementById("primary_insurance_co");
	if (primaryInsuranceIdObj != null) {
		primaryInsuranceIdObj.selectedIndex = 0;
		onLoadTpaList('P');
	}

	var primaryInsuranceApprovalObj = document.getElementById("primary_insurance_approval");
	if (primaryInsuranceApprovalObj != null)
		primaryInsuranceApprovalObj.value = "";

	var primaryPlanTypeObj = document.getElementById("primary_plan_type");
	if (primaryPlanTypeObj != null) {
		primaryPlanTypeObj.selectedIndex = 0;
		onInsuCatChange('P');
	}

	var primaryPlanIdObj = document.getElementById("primary_plan_id");
	if (primaryPlanIdObj != null) {
		primaryPlanIdObj.selectedIndex = 0;
		onPolicyChange('P');
	}

	var primaryDrgCheckObj = document.getElementById("primary_drg_check");
	var primaryUseDrgObj = document.getElementById("primary_use_drg");
	if (primaryDrgCheckObj != null) {
		primaryDrgCheckObj.checked = false;
		checkUseDRG('P');
	}

	var primaryPerdiemCheckObj = document.getElementById("primary_perdiem_check");
	var primaryUsePerdiemObj = document.getElementById("primary_use_perdiem");
	if (primaryPerdiemCheckObj != null) {
		primaryPerdiemCheckObj.checked = false;
		checkUsePerdiem('P');
	}

	var primaryMemberIdObj = document.getElementById("primary_member_id");
	if (primaryMemberIdObj != null) {
		primaryMemberIdObj.value = "";
		checkForMemberID('P');
	}

	var primaryPolicyValidityStartObj = document.getElementById("primary_policy_validity_start");
	if (primaryPolicyValidityStartObj != null) primaryPolicyValidityStartObj.value = "";
	var primaryPolicyValidityEndObj = document.getElementById("primary_policy_validity_end");
	if (primaryPolicyValidityEndObj != null) primaryPolicyValidityEndObj.value = "";

	var primaryPolicyNumberObj = document.getElementById("primary_policy_number");
	if (primaryPolicyNumberObj != null) primaryPolicyNumberObj.value = "";

	var primaryPolicyHolderObj = document.getElementById("primary_policy_holder_name");
	if (primaryPolicyHolderObj != null) primaryPolicyHolderObj.value = "";

	var primaryPatientRelationshipObj = document.getElementById("primary_patient_relationship");
	if (primaryPatientRelationshipObj != null) primaryPatientRelationshipObj.value = "";

	var primaryPriorAuthIddObj = document.getElementById("primary_prior_auth_id");
	if (primaryPriorAuthIddObj != null) primaryPriorAuthIddObj.value = "";
	var primaryPriorAuthModeIdObj = document.getElementById("primary_prior_auth_mode_id");
	if (primaryPriorAuthModeIdObj != null) primaryPriorAuthModeIdObjedIndex = 0;

	var primaryInsuranceDocContentObj = document.getElementById("primary_insurance_doc_content_bytea1");
	if (primaryInsuranceDocContentObj != null) primaryInsuranceDocContentObj.value = "";
}

function resetPrimaryCorporateDetails() {

	var primaryCorporateObj = document.getElementById("primary_corporate");
	if (primaryCorporateObj != null) primaryCorporateObj.selectedIndex = 0;

	var primaryCorporateApprovalObj = document.getElementById("primary_corporate_approval");
	if (primaryCorporateApprovalObj != null) primaryCorporateApprovalObj.value = "";

	var primaryEmployeeIdObj = document.getElementById("primary_employee_id");
	if (primaryEmployeeIdObj != null) primaryEmployeeIdObj.value = "";

	var primaryEmployeeNameObj = document.getElementById("primary_employee_name");
	if (primaryEmployeeNameObj != null) primaryEmployeeNameObj.value = "";

	var primaryEmployeeRelationObj = document.getElementById("primary_employee_relation");
	if (primaryEmployeeRelationObj != null) primaryEmployeeRelationObj.value = "";

	var primaryCorporateDocContentObj = document.getElementById("primary_corporate_doc_content_bytea1");
	if (primaryCorporateDocContentObj != null) primaryCorporateDocContentObj.value = "";
}

function resetPrimaryNationalDetails() {

	var primaryCorporateObj = document.getElementById("primary_national_sponsor");
	if (primaryCorporateObj != null) primaryCorporateObj.selectedIndex = 0;

	var primaryNationalApprovalObj = document.getElementById("primary_national_approval");
	if (primaryNationalApprovalObj != null) primaryNationalApprovalObj.value = "";

	var primaryNationalMemberIdObj = document.getElementById("primary_national_member_id");
	if (primaryNationalMemberIdObj != null) primaryNationalMemberIdObj.value = "";

	var primaryNationalMemberNameObj = document.getElementById("primary_national_member_name");
	if (primaryNationalMemberNameObj != null) primaryNationalMemberNameObj.value = "";

	var primaryNationalRelationObj = document.getElementById("primary_national_relation");
	if (primaryNationalRelationObj != null) primaryNationalRelationObj.value = "";

	var primaryNationalDocContentObj = document.getElementById("primary_national_doc_content_bytea1");
	if (primaryNationalDocContentObj != null) primaryNationalDocContentObj.value = "";
}

function resetSecondaryInsuranceDetails() {
	var secondarySponsorIdObj = document.getElementById("secondary_sponsor_id");
	if (secondarySponsorIdObj != null) {
		secondarySponsorIdObj.selectedIndex = 0;
		onTpaChange('S');
	}

	var secondaryInsuranceIdObj = document.getElementById("secondary_insurance_co");
	if (secondaryInsuranceIdObj != null) {
		secondaryInsuranceIdObj.selectedIndex = 0;
		onLoadTpaList('S');
	}

	var secondaryInsuranceApprovalObj = document.getElementById("secondary_insurance_approval");
	if (secondaryInsuranceApprovalObj != null) secondaryInsuranceApprovalObj.value = "";

	var secondaryPlanTypeObj = document.getElementById("secondary_plan_type");
	if (secondaryPlanTypeObj != null) {
		secondaryPlanTypeObj.selectedIndex = 0;
		onInsuCatChange('S');
	}

	var secondaryPlanIdObj = document.getElementById("secondary_plan_id");
	if (secondaryPlanIdObj != null) {
		secondaryPlanIdObj.selectedIndex = 0;
		onPolicyChange('S');
	}

	var secondaryDrgCheckObj = document.getElementById("secondary_drg_check");
	var secondaryUseDrgObj = document.getElementById("secondary_use_drg");
	if (secondaryDrgCheckObj != null) {
		secondaryDrgCheckObj.checked = false;
		checkUseDRG('S');
	}

	var secondaryPerdiemCheckObj = document.getElementById("secondary_perdiem_check");
	var secondaryUsePerdiemObj = document.getElementById("secondary_use_perdiem");
	if (secondaryPerdiemCheckObj != null) {
		secondaryPerdiemCheckObj.checked = false;
		checkUsePerdiem('S');
	}

	var secondaryMemberIdObj = document.getElementById("secondary_member_id");
	if (secondaryMemberIdObj != null) {
		secondaryMemberIdObj.value = "";
		checkForMemberID('S');
	}

	var secondaryPolicyValidityStartObj = document.getElementById("secondary_policy_validity_start");
	if (secondaryPolicyValidityStartObj != null) secondaryPolicyValidityStartObj.value = "";
	var secondaryPolicyValidityEndObj = document.getElementById("secondary_policy_validity_end");
	if (secondaryPolicyValidityEndObj != null) secondaryPolicyValidityEndObj.value = "";

	var secondaryPolicyNumberObj = document.getElementById("secondary_policy_number");
	if (secondaryPolicyNumberObj != null) secondaryPolicyNumberObj.value = "";

	var secondaryPolicyHolderObj = document.getElementById("secondary_policy_holder_name");
	if (secondaryPolicyHolderObj != null) secondaryPolicyHolderObj.value = "";

	var secondaryPatientRelationshipObj = document.getElementById("secondary_patient_relationship");
	if (secondaryPatientRelationshipObj != null) secondaryPatientRelationshipObj.value = "";

	var secondaryPriorAuthIddObj = document.getElementById("secondary_prior_auth_id");
	if (secondaryPriorAuthIddObj != null) secondaryPriorAuthIddObj.value = "";
	var secondaryPriorAuthModeIdObj = document.getElementById("secondary_prior_auth_mode_id");
	if (secondaryPriorAuthModeIdObj != null) secondaryPriorAuthModeIdObj.selectedIndex = 0;

	var secondaryInsuranceDocContentObj = document.getElementById("secondary_insurance_doc_content_bytea1");
	if (secondaryInsuranceDocContentObj != null) secondaryInsuranceDocContentObj.value = "";
}

function resetSecondaryCorporateDetails() {

	var secondaryCorporateObj = document.getElementById("secondary_corporate");
	if (secondaryCorporateObj != null) secondaryCorporateObj.selectedIndex = 0;

	var secondaryCorporateApprovalObj = document.getElementById("secondary_corporate_approval");
	if (secondaryCorporateApprovalObj != null) secondaryCorporateApprovalObj.value = "";

	var secondaryEmployeeIdObj = document.getElementById("secondary_employee_id");
	if (secondaryEmployeeIdObj != null) secondaryEmployeeIdObj.value = "";

	var secondaryEmployeeNameObj = document.getElementById("secondary_employee_name");
	if (secondaryEmployeeNameObj != null) secondaryEmployeeNameObj.value = "";

	var secondaryEmployeeRelationObj = document.getElementById("secondary_employee_relation");
	if (secondaryEmployeeRelationObj != null) secondaryEmployeeRelationObj.value = "";

	var secondaryCorporateDocContentObj = document.getElementById("secondary_corporate_doc_content_bytea1");
	if (secondaryCorporateDocContentObj != null) secondaryCorporateDocContentObj.value = "";
}

function resetSecondaryNationalDetails() {

	var secondaryCorporateObj = document.getElementById("secondary_national_sponsor");
	if (secondaryCorporateObj != null) secondaryCorporateObj.selectedIndex = 0;

	var secondaryNationalApprovalObj = document.getElementById("secondary_national_approval");
	if (secondaryNationalApprovalObj != null) secondaryNationalApprovalObj.value = "";

	var secondaryNationalMemberIdObj = document.getElementById("secondary_national_member_id");
	if (secondaryNationalMemberIdObj != null) secondaryNationalMemberIdObj.value = "";

	var secondaryNationalMemberNameObj = document.getElementById("secondary_national_member_name");
	if (secondaryNationalMemberNameObj != null) secondaryNationalMemberNameObj.value = "";

	var secondaryNationalRelationObj = document.getElementById("secondary_national_relation");
	if (secondaryNationalRelationObj != null) secondaryNationalRelationObj.value = "";

	var secondaryNationalDocContentObj = document.getElementById("secondary_national_doc_content_bytea1");
	if (secondaryNationalDocContentObj != null) secondaryNationalDocContentObj.value = "";
}


function enableDisablePrimaryInsuranceDetails(disable, approvalAmtEnableDisable) {
	var primarySponsorNameObj = document.getElementById("primary_sponsor_name");
	primarySponsorNameObj.disabled = disable;

	var primarySponsorIdObj = document.getElementById("primary_sponsor_id");
	primarySponsorIdObj.disabled = disable;

	var primaryInsuranceIdObj = document.getElementById("primary_insurance_co");
	if (disable)
		primaryInsuranceIdObj.disabled = disable;
	else
		primaryInsuranceIdObj.removeAttribute("disabled");

	var primaryPlanTypeObj = document.getElementById("primary_plan_type");
	if (primaryPlanTypeObj != null) primaryPlanTypeObj.disabled = disable;

	var primaryPlanIdObj = document.getElementById("primary_plan_id");
	if (primaryPlanIdObj != null) primaryPlanIdObj.disabled = disable;

	var primaryDrgCheckObj = document.getElementById("primary_drg_check");
	var primaryUseDrgObj = document.getElementById("primary_use_drg");
	if (primaryDrgCheckObj != null) primaryDrgCheckObj.checked = false;
	if (primaryDrgCheckObj != null) primaryDrgCheckObj.disabled = disable;

	var primaryPerdiemCheckObj = document.getElementById("primary_perdiem_check");
	var primaryUsePerdiemObj = document.getElementById("primary_use_perdiem");
	//if (primaryPerdiemCheckObj != null) primaryPerdiemCheckObj.checked = false;
	//if (primaryPerdiemCheckObj != null) primaryPerdiemCheckObj.disabled = disable;

	var primaryMemberIdObj = document.getElementById("primary_member_id");
	if (primaryMemberIdObj != null) primaryMemberIdObj.disabled = disable;

	var primaryPolicyValidityStartObj = document.getElementById("primary_policy_validity_start");
	if (primaryPolicyValidityStartObj != null) primaryPolicyValidityStartObj.disabled = disable;

	var primaryPolicyValidityStartOnlyObj = document.getElementById("primary_policy_validity_only_start");
	if (primaryPolicyValidityStartOnlyObj != null) primaryPolicyValidityStartOnlyObj.disabled = disable;

	var primaryPolicyValidityendOnlyObj = document.getElementById("primary_policy_validity_only_end");
	if (primaryPolicyValidityendOnlyObj != null) primaryPolicyValidityendOnlyObj.disabled = disable;

	var primaryInsuranceDocContentObj = document.getElementById("primary_insurance_doc_content_bytea1");
	var primaryInsuranceDocUsageObj = document.getElementById("primary_insurance_document_usage");

	var primaryPolicyValidityEndObj = document.getElementById("primary_policy_validity_end");
	if (primaryPolicyValidityEndObj != null) {
		primaryPolicyValidityEndObj.disabled = disable;
	}

	if (primaryInsuranceDocContentObj != null) primaryInsuranceDocContentObj.disabled = disable;
	if (primaryInsuranceDocUsageObj != null) primaryInsuranceDocUsageObj.disabled = disable;

	var primaryPolicyNumberObj = document.getElementById("primary_policy_number");
	if (primaryPolicyNumberObj != null) primaryPolicyNumberObj.disabled = disable;

	var primaryPolicyHolderObj = document.getElementById("primary_policy_holder_name");
	if (primaryPolicyHolderObj != null) primaryPolicyHolderObj.disabled = disable;

	var primaryPatientRelationshipObj = document.getElementById("primary_patient_relationship");
	if (primaryPatientRelationshipObj != null) primaryPatientRelationshipObj.disabled = disable;

	var primaryPriorAuthIddObj = document.getElementById("primary_prior_auth_id");
	if (primaryPriorAuthIddObj != null) primaryPriorAuthIddObj.disabled = disable;
	var primaryPriorAuthModeIdObj = document.getElementById("primary_prior_auth_mode_id");
	if (primaryPriorAuthModeIdObj != null) {
		if (disable) primaryPriorAuthModeIdObj.disabled = disable;
		else primaryPriorAuthModeIdObj.removeAttribute("disabled");
	}
}

function enableDisablePrimaryCorporateDetails(disable, approvalAmtEnableDisable) {

	var primaryCorporateObj = document.getElementById("primary_corporate");
	primaryCorporateObj.disabled = disable;

	var primaryEmployeeIdObj = document.getElementById("primary_employee_id");
	primaryEmployeeIdObj.disabled = disable;

	var primaryEmployeeNameObj = document.getElementById("primary_employee_name");
	primaryEmployeeNameObj.disabled = disable;

	var primaryEmployeeRelationObj = document.getElementById("primary_employee_relation");
	primaryEmployeeRelationObj.disabled = disable;

	var primaryCorporateDocContentObj = document.getElementById("primary_corporate_doc_content_bytea1");
	if (primaryCorporateDocContentObj != null) primaryCorporateDocContentObj.disabled = disable;

	var primaryCorporateDocUsageObj = document.getElementById("primary_corporate_document_usage");
	if (primaryCorporateDocUsageObj != null) primaryCorporateDocUsageObj.disabled = disable;
}

function enableDisablePrimaryNationalDetails(disable, approvalAmtEnableDisable) {

	var primaryCorporateObj = document.getElementById("primary_national_sponsor");
	primaryCorporateObj.disabled = disable;

	var primaryNationalMemberIdObj = document.getElementById("primary_national_member_id");
	primaryNationalMemberIdObj.disabled = disable;

	var primaryNationalMemberNameObj = document.getElementById("primary_national_member_name");
	primaryNationalMemberNameObj.disabled = disable;

	var primaryNationalRelationObj = document.getElementById("primary_national_relation");
	primaryNationalRelationObj.disabled = disable;

	var primaryNationalDocContentObj = document.getElementById("primary_national_doc_content_bytea1");
	if (primaryNationalDocContentObj != null) primaryNationalDocContentObj.disabled = disable;

	var primaryNationalDocUsageObj = document.getElementById("primary_national_document_usage");
	if (primaryNationalDocUsageObj != null) primaryNationalDocUsageObj.disabled = disable;
}

function enableDisableSecondaryInsuranceDetails(disable, approvalAmtEnableDisable) {
	var secondarySponsorNameObj = document.getElementById("secondary_sponsor_name");
	secondarySponsorNameObj.disabled = disable;

	var secondarySponsorIdObj = document.getElementById("secondary_sponsor_id");
	secondarySponsorIdObj.disabled = disable;

	var secondaryInsuranceIdObj = document.getElementById("secondary_insurance_co");
	if (disable)
		secondaryInsuranceIdObj.disabled = disable;
	else
		secondaryInsuranceIdObj.removeAttribute("disabled");

	var secondaryPlanTypeObj = document.getElementById("secondary_plan_type");
	if (secondaryPlanTypeObj != null) secondaryPlanTypeObj.disabled = disable;

	var secondaryPlanIdObj = document.getElementById("secondary_plan_id");
	if (secondaryPlanIdObj != null) secondaryPlanIdObj.disabled = disable;

	var secondaryDrgCheckObj = document.getElementById("secondary_drg_check");
	var secondaryUseDrgObj = document.getElementById("secondary_use_drg");
	if (secondaryDrgCheckObj != null) secondaryDrgCheckObj.checked = false;
	if (secondaryDrgCheckObj != null) secondaryDrgCheckObj.disabled = disable;

	var secondaryPerdiemCheckObj = document.getElementById("secondary_perdiem_check");
	var secondaryUsePerdiemObj = document.getElementById("secondary_use_perdiem");
	//if (secondaryPerdiemCheckObj != null) secondaryPerdiemCheckObj.checked = false;
	//if (secondaryPerdiemCheckObj != null) secondaryPerdiemCheckObj.disabled = disable;

	var secondaryMemberIdObj = document.getElementById("secondary_member_id");
	if (secondaryMemberIdObj != null) secondaryMemberIdObj.disabled = disable;

	var secondaryPolicyValidityStartObj = document.getElementById("secondary_policy_validity_start");
	if (secondaryPolicyValidityStartObj != null) secondaryPolicyValidityStartObj.disabled = disable;

	var secondaryPolicyValidityStartOnlyObj = document.getElementById("secondary_policy_validity_only_start");
	if (secondaryPolicyValidityStartOnlyObj != null) secondaryPolicyValidityStartOnlyObj.disabled = disable;

	var secondaryPolicyValidityendOnlyObj = document.getElementById("secondary_policy_validity_only_end");
	if (secondaryPolicyValidityendOnlyObj != null) secondaryPolicyValidityendOnlyObj.disabled = disable;

	var secondaryInsuranceDocContentObj = document.getElementById("secondary_insurance_doc_content_bytea1");
	var secondaryInsuranceDocUsageObj = document.getElementById("secondary_insurance_document_usage");

	var secondaryPolicyValidityEndObj = document.getElementById("secondary_policy_validity_end");
	if (secondaryPolicyValidityEndObj != null) {
		secondaryPolicyValidityEndObj.disabled = disable;
	}

	if (secondaryInsuranceDocContentObj != null) secondaryInsuranceDocContentObj.disabled = disable;
	if (secondaryInsuranceDocUsageObj != null) secondaryInsuranceDocUsageObj.disabled = disable;

	var secondaryPolicyNumberObj = document.getElementById("secondary_policy_number");
	if (secondaryPolicyNumberObj != null) secondaryPolicyNumberObj.disabled = disable;

	var secondaryPolicyHolderObj = document.getElementById("secondary_policy_holder_name");
	if (secondaryPolicyHolderObj != null) secondaryPolicyHolderObj.disabled = disable;

	var secondaryPatientRelationshipObj = document.getElementById("secondary_patient_relationship");
	if (secondaryPatientRelationshipObj != null) secondaryPatientRelationshipObj.disabled = disable;

	var secondaryPriorAuthIddObj = document.getElementById("secondary_prior_auth_id");
	if (secondaryPriorAuthIddObj != null) secondaryPriorAuthIddObj.disabled = disable;
	var secondaryPriorAuthModeIdObj = document.getElementById("secondary_prior_auth_mode_id");
	if (secondaryPriorAuthModeIdObj != null) {
		if (disable) secondaryPriorAuthModeIdObj.disabled = disable;
		else secondaryPriorAuthModeIdObj.removeAttribute("disabled");
	}
}

function enableDisableSecondaryCorporateDetails(disable, approvalAmtEnableDisable) {

	var secondaryCorporateObj = document.getElementById("secondary_corporate");
	secondaryCorporateObj.disabled = disable;

	var secondaryEmployeeIdObj = document.getElementById("secondary_employee_id");
	secondaryEmployeeIdObj.disabled = disable;

	var secondaryEmployeeNameObj = document.getElementById("secondary_employee_name");
	secondaryEmployeeNameObj.disabled = disable;

	var secondaryEmployeeRelationObj = document.getElementById("secondary_employee_relation");
	secondaryEmployeeRelationObj.disabled = disable;

	var secondaryCorporateDocContentObj = document.getElementById("secondary_corporate_doc_content_bytea1");
	if (secondaryCorporateDocContentObj != null) secondaryCorporateDocContentObj.disabled = disable;

	var secondaryCorporateDocUsageObj = document.getElementById("secondary_corporate_document_usage");
	if (secondaryCorporateDocUsageObj != null) secondaryCorporateDocUsageObj.disabled = disable;
}

function enableDisableSecondaryNationalDetails(disable, approvalAmtEnableDisable) {

	var secondaryCorporateObj = document.getElementById("secondary_national_sponsor");
	secondaryCorporateObj.disabled = disable;

	var secondaryNationalMemberIdObj = document.getElementById("secondary_national_member_id");
	secondaryNationalMemberIdObj.disabled = disable;

	var secondaryNationalMemberNameObj = document.getElementById("secondary_national_member_name");
	secondaryNationalMemberNameObj.disabled = disable;

	var secondaryNationalRelationObj = document.getElementById("secondary_national_relation");
	secondaryNationalRelationObj.disabled = disable;

	var secondaryNationalDocContentObj = document.getElementById("secondary_national_doc_content_bytea1");
	if (secondaryNationalDocContentObj != null) secondaryNationalDocContentObj.disabled = disable;

	var secondaryNationalDocUsageObj = document.getElementById("secondary_national_document_usage");
	if (secondaryNationalDocUsageObj != null) secondaryNationalDocUsageObj.disabled = disable;
}

/**********************************************************************/

function getSponsorObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_sponsor_id");
	}
	return null;
}

function getSponsorNameObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_sponsor_name");
	}
	return null;
}

function getInsuObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_insurance_co");
	} 
	return null;
}

function getPlanTypeObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_plan_type");
	}  
	return null;
}

function getPatientInsurancePlanObj(prefix) {
	return document.getElementById(prefix+"_patient_insurance_plans_id");
}

function getPlanObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_plan_id");
	}  
	return null;
}

/*********************************************************************/
function getPolicyValidityStartObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_policy_validity_start");
	} else return null;
}

function getPolicyValidityEndObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_policy_validity_end");
	}  
	return null;
}

function getInsuranceMemberIdObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_member_id");
	}  
	return null;
}

function getInsurancePolicyNumberObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I')
		return document.getElementById(prefix+"_policy_number");
	return null;
}

function getPatientHolderObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_policy_holder_name");
	} 
	return null;
}

function getPatientRelationObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_patient_relationship");
	} 
	return null;
}

function getApprovalLimitObj(prefix) {
	var sponsorObj = document.getElementById(prefix+"_sponsor");
	if (sponsorObj.value == 'I') {
		return document.getElementById(prefix+"_insurance_approval");
	} 
	return null;
}

function getPrimaryDRGCheckObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_drg_check");
	} else return null;
}

function getSecondaryDRGCheckObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_drg_check");
	} else return null;
}

function getPrimaryUseDRGObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_use_drg");
	} else return null;
}

function getSecondaryUseDRGObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_use_drg");
	} else return null;
}

function getPrimaryPerdiemCheckObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_perdiem_check");
	} else return null;
}

function getSecondaryPerdiemCheckObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_perdiem_check");
	} else return null;
}

function getPrimaryUsePerdiemObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_use_perdiem");
	} else return null;
}

function getSecondaryUsePerdiemObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_use_perdiem");
	} else return null;
}

function getPrimaryDocContentObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_doc_content_bytea1");
	} else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_doc_content_bytea1");
	} else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_doc_content_bytea1");
	}
	return null;
}

function getSecondaryDocContentObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_doc_content_bytea1");
	} else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_doc_content_bytea1");
	} else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_doc_content_bytea1");
	}
	return null;
}

function getPrimaryDocNameObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_doc_name1");
	} else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_doc_name1");
	} else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_doc_name1");
	}
	return null;
}

function getSecondaryDocNameObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_doc_name1");
	} else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_doc_name1");
	} else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_doc_name1");
	}
	return null;
}

function getPrimaryDocumentUsageObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_document_usage");
	} else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_document_usage");
	} else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_document_usage");
	}
	return null;
}

function getSecondaryDocumentUsageObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_document_usage");
	} else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_document_usage");
	} else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_document_usage");
	}
	return null;
}
/**********************************************************************/

function getPrimaryAuthIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_prior_auth_id");
	}
	return null;
}

function getSecondaryAuthIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_prior_auth_id");
	}
	return null;
}

function getPrimaryAuthModeIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_prior_auth_mode_id");
	}
	return null;
}

function getSecondaryAuthModeIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_prior_auth_mode_id");
	}
	return null;
}

/***********************************************************************/

function getPrimaryApprovalLimitStarObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_approval_star");
	} else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_approval_star");
	} else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_approval_star");
	}
	return null;
}

function getSecondaryApprovalLimitStarObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_approval_star");
	} else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_approval_star");
	} else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_approval_star");
	}
	return null;
}

function getPrimaryPolicyValidityStartStarObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_start_star");
	} else return null;
}

function getSecondaryPolicyValidityStartStarObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_start_star");
	} else return null;
}

function getPrimaryPolicyValidityEndStarObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_end_star");
	} else return null;
}

function getSecondaryPolicyValidityEndStarObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_end_star");
	} else return null;
}

function getPrimaryUploadRowObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primaryInsFile");
	} else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primaryCorporateFile");
	} else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primaryNationalFile");
	}
	return null;
}

function getSecondaryUploadRowObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondaryInsFile");
	} else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondaryCorporateFile");
	} else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondaryNationalFile");
	}
	return null;
}

var gPrimaryPatientInsurancePlanId = null;
var gPreviousPrimaryInsCompany = null;
var gPreviousPrimaryTpa = null;
var gPreviousPlan = null;
var gPreviousPlanType = null;
var gPreviousMemberId = null;
var gPreviousPolicyNumber  = null;
var gPreviousHolder = null;
var gPreviousRelation = null;
var gPreviousStartDate = null;
var gPreviousEndDate = null;
var gPreviousPatientPolicyId = null;
var gPrimaryPolicyStatus = null;
var gSecPolicyStatus = null;

var gPriorAuthId = null;
var gPriorAuthModeId = null;
var gPrimaryApproval = null;
var gUseDrg = null;
var gUsePerdiem = null;

var gSecondaryPatientInsurancePlanId = null;
var gPreviousSecPlan = null;
var gPreviousSecPlanType = null;
var gPreviousSecMemberId = null;
var gPreviousSecPolicyNumber = null;
var gPreviousSecHolder = null;
var gPreviousSecRelation = null;
var gPreviousSecStartDate = null;
var gPreviousSecEndDate = null;
var gPreviousSecPatientPolicyId = null;

var gPreviousSecPriorauthid = null;
var gPreviousSecPriorauthmodeid = null;
var gSecondaryApproval = null;
var gSecondaryUseDrg = null;
var gSecondaryUsePerdiem = null;


function setPlanDetails(){

	if (gPatientPolciyNos.length == 2 ) {

		var primaryPlanDetais = gPatientPolciyNos[0];
		var secondaryPlanDetails = gPatientPolciyNos[1];

		gPrimaryPatientInsurancePlanId = primaryPlanDetais.patient_insurance_plans_id;
		gPreviousPrimaryInsCompany = primaryPlanDetais.insurance_co;
		gPreviousPrimaryTpa = primaryPlanDetais.sponsor_id;
		gPreviousPlan = primaryPlanDetais.plan_id;
		gPreviousPlanType = primaryPlanDetais.plan_type_id;
		gPreviousMemberId = !empty(primaryPlanDetais.member_id) ? primaryPlanDetais.member_id : null;
		gPreviousPolicyNumber = !empty(primaryPlanDetais.policy_number) ? primaryPlanDetais.policy_number : null;
		gPreviousHolder = !empty(primaryPlanDetais.policy_holder_name) ? primaryPlanDetais.policy_holder_name : null;
		gPreviousRelation = !empty(primaryPlanDetais.patient_relationship) ? primaryPlanDetais.patient_relationship : null;
		gPreviousStartDate = primaryPlanDetais.policy_validity_start;
		gPreviousEndDate = primaryPlanDetais.policy_validity_end;
		gPreviousPatientPolicyId = primaryPlanDetais.patient_policy_id;
		gPrimaryPolicyStatus = primaryPlanDetais.status;;
		gSecPolicyStatus = secondaryPlanDetails.status;;

		gPriorAuthId = primaryPlanDetais.prior_auth_id;
		gPriorAuthModeId = primaryPlanDetais.prior_auth_mode_id;
		gPrimaryApproval = primaryPlanDetais.insurance_approval;
		gUseDrg = primaryPlanDetais.use_drg;
		gUsePerdiem = primaryPlanDetais.use_perdiem;


		gSecondaryPatientInsurancePlanId = secondaryPlanDetails.patient_insurance_plans_id;
		gPreviousSecondaryInsCompany = secondaryPlanDetails.insurance_co;
		gPreviousSecondaryTpa = secondaryPlanDetails.sponsor_id;
		gPreviousSecPlan = secondaryPlanDetails.plan_id;
		gPreviousSecPlanType = secondaryPlanDetails.plan_type_id;
		gPreviousSecMemberId = !empty(secondaryPlanDetails.member_id) ? secondaryPlanDetails.member_id : null;
		gPreviousSecPolicyNumber = !empty(secondaryPlanDetails.policy_number) ? secondaryPlanDetails.policy_number : null;
		gPreviousSecHolder = !empty(secondaryPlanDetails.policy_holder_name) ? secondaryPlanDetails.policy_holder_name : null;
		gPreviousSecRelation = !empty(secondaryPlanDetails.patient_relationship) ? secondaryPlanDetails.patient_relationship : null;
		gPreviousSecStartDate = secondaryPlanDetails.policy_validity_start;
		gPreviousSecEndDate = secondaryPlanDetails.policy_validity_end;
		gPreviousSecPatientPolicyId = secondaryPlanDetails.patient_policy_id;

		gPreviousSecPriorauthid = secondaryPlanDetails.prior_auth_id;
		gPreviousSecPriorauthmodeid = secondaryPlanDetails.prior_auth_mode_id;
		gSecondaryApproval = secondaryPlanDetails.insurance_approval;
		gSecondaryUseDrg = secondaryPlanDetails.use_drg;
		gSecondaryUsePerdiem = secondaryPlanDetails.use_perdiem;


		gPreviousPrimarySponsorIndex = 'I';
		gPreviousSecondarySponsorIndex = 'I';

	} else if (gPatientPolciyNos.length == 1) {

		if (!empty(corporate_sponsor_id) || !empty(national_sponsor_id) ) {

			var secondaryPlanDetails = gPatientPolciyNos[0];

			gSecondaryPatientInsurancePlanId = secondaryPlanDetails.patient_insurance_plans_id;
			gPreviousSecondaryInsCompany = secondaryPlanDetails.insurance_co;
			gPreviousSecondaryTpa = secondaryPlanDetails.sponsor_id;
			gPreviousSecPlan = secondaryPlanDetails.plan_id;
			gPreviousSecPlanType = secondaryPlanDetails.plan_type_id;
			gPreviousSecMemberId = !empty(secondaryPlanDetails.member_id) ? secondaryPlanDetails.member_id : null;
			gPreviousSecPolicyNumber = !empty(secondaryPlanDetails.policy_number) ? secondaryPlanDetails.policy_number : null;
			gPreviousSecHolder = !empty(secondaryPlanDetails.policy_holder_name) ? secondaryPlanDetails.policy_holder_name : null;
			gPreviousSecRelation = !empty(secondaryPlanDetails.patient_relationship) ? secondaryPlanDetails.patient_relationship : null;
			gPreviousSecStartDate = secondaryPlanDetails.policy_validity_start;
			gPreviousSecEndDate = secondaryPlanDetails.policy_validity_end;
			gPreviousSecPatientPolicyId = secondaryPlanDetails.patient_policy_id;

			gPreviousSecPriorauthid = secondaryPlanDetails.prior_auth_id;
			gPreviousSecPriorauthmodeid = secondaryPlanDetails.prior_auth_mode_id;
			gSecondaryApproval = secondaryPlanDetails.insurance_approval;
			gSecondaryUseDrg = secondaryPlanDetails.use_drg;
			gSecondaryUsePerdiem = secondaryPlanDetails.use_perdiem;
			gSecPolicyStatus = secondaryPlanDetails.status;;

			gPreviousSecondarySponsorIndex = 'I';
			gPreviousPrimarySponsorIndex = (!empty(corporate_sponsor_id)) ? 'C' : 'N';
			gPreviousPrimaryTpa = primary_sponsor_id;
			gPrimaryApproval = primary_insurance_appoval;

		} else if ((!empty(sec_corporate_sponsor_id) || !empty(sec_national_sponsor_id))
						|| (empty(sec_corporate_sponsor_id) && empty(sec_national_sponsor_id))) {

			var primaryPlanDetais = gPatientPolciyNos[0];

			gPrimaryPatientInsurancePlanId = primaryPlanDetais.patient_insurance_plans_id;
			gPreviousPrimaryInsCompany = primaryPlanDetais.insurance_co;
			gPreviousPrimaryTpa = primaryPlanDetais.sponsor_id;
			gPreviousPlan = primaryPlanDetais.plan_id;
			gPreviousPlanType = primaryPlanDetais.plan_type_id;
			gPreviousMemberId = !empty(primaryPlanDetais.member_id) ? primaryPlanDetais.member_id : null;
			gPreviousPolicyNumber = !empty(primaryPlanDetais.policy_number) ? primaryPlanDetais.policy_number : null;
			gPreviousHolder = !empty(primaryPlanDetais.policy_holder_name) ? primaryPlanDetais.policy_holder_name : null;
			gPreviousRelation = !empty(primaryPlanDetais.patient_relationship) ? primaryPlanDetais.patient_relationship : null;
			gPreviousStartDate = primaryPlanDetais.policy_validity_start;
			gPreviousEndDate = primaryPlanDetais.policy_validity_end;
			gPreviousPatientPolicyId = primaryPlanDetais.patient_policy_id;
			gPrimaryPolicyStatus = primaryPlanDetais.status;;

			gPriorAuthId = primaryPlanDetais.prior_auth_id;
			gPriorAuthModeId = primaryPlanDetais.prior_auth_mode_id;
			gPrimaryApproval = primaryPlanDetais.insurance_approval;
			gUseDrg = primaryPlanDetais.use_drg;
			gUsePerdiem = primaryPlanDetais.use_perdiem;

			gPreviousPrimarySponsorIndex = 'I';
			gPreviousSecondarySponsorIndex = (!empty(sec_corporate_sponsor_id)) ? 'C' : (!empty(sec_national_sponsor_id) ? 'N' : '');
			gPreviousSecondaryTpa = secondary_sponsor_id;
			gSecondaryApproval = secondary_insurance_approval;

		}

	} else {
		gPreviousPrimarySponsorIndex = !empty(gPreviousPrimarySponsorIndex) ? gPreviousPrimarySponsorIndex : '';
		gPreviousSecondarySponsorIndex = !empty(gPreviousSecondarySponsorIndex) ? gPreviousSecondarySponsorIndex : '';
		gPreviousPrimaryTpa = primary_sponsor_id;
		gPreviousSecondaryTpa = secondary_sponsor_id;
		gPrimaryApproval = primary_insurance_appoval;
		gSecondaryApproval = secondary_insurance_approval;
	}

}

function validatePlan() {

	var priInsCompObj = getInsuObj('primary');
	var priTpaObj = getSponsorObj('primary');
	var priPlanObj = getPlanObj('primary');
	var priPlanTypeObj = getPlanTypeObj('primary');
	var priMemberIdObj = getInsuranceMemberIdObj('primary');

	var secInsCompObj = getInsuObj('secondary');
	var secTpaObj = getSponsorObj('secondary');
	var secPlanObj = getPlanObj('secondary');
	var secPlanTypeObj = getPlanTypeObj('secondary');
	var secMemberIdObj = getInsuranceMemberIdObj('secondary');


	if (isModAdvanceIns) {
		if (!empty(priInsCompObj) && priInsCompObj.value !='' && !empty(secInsCompObj) && secInsCompObj.value !=''
							&& !empty(priPlanObj) && priPlanObj.value !='' && !empty(secPlanObj) && secPlanObj.value !='') {

			if (priInsCompObj.value == secInsCompObj.value && priPlanObj.value == secPlanObj.value) {
				var msg = getString("js.registration.patient.plans");
				msg += " "+getString("js.registration.patient.same.isnotallowed");;
				alert(msg);
				return false;
			}
			return true;
		}
		return true;
	}
	return true;
}

function validateMemberId() {

	if (isModAdvanceIns) {

		var priPlanObj = getPlanObj('primary');
		var secPlanObj = getPlanObj('secondary');
		var priInsCompObj = getInsuObj('primary');
		var secInsCompObj = getInsuObj('secondary');
		var priMemberIdObj = getInsuranceMemberIdObj('primary');
		var secMemberIdObj = getInsuranceMemberIdObj('secondary');

		if ((priPlanObj != null && priPlanObj.value != "")
				&& (secPlanObj != null && secPlanObj.value != "")) {

			if (priInsCompObj.value == secInsCompObj.value
						&& (priMemberIdObj != null && priMemberIdObj.value.trim() != '')
						&& (secMemberIdObj != null && secMemberIdObj.value.trim() != '')) {

				if (priMemberIdObj.value.trim() == secMemberIdObj.value.trim()) {
					var memberIdRequiredMsg = memberIdLabel + " " +getString("js.registration.patient.same.isnotallowed");
					alert(memberIdRequiredMsg);
					return false;
				}
				return true;
			}
			return true;
		}
		return true;
	}
	return true;
}

function allowMultiplePlansForMultipleTPAbills() {
	if (moreThanOneTpaBillsExist == 'true') {
		var primarySponsorObj = getSponsorObj('primary');
		var secondarySponsorObj = getSponsorObj('secondary');
		var primaryPlanObj = getPlanObj('primary');
		var secondaryPlanObj = getPlanObj('secondary');

		if (!empty(primarySponsorObj) && !empty(secondarySponsorObj)) {
			if (empty(primaryPlanObj) || empty(secondaryPlanObj)) {
				var alertMsg = getString("js.registration.patient.multiple.tpa.bills");
				alertMsg = alertMsg+"\n"+getString("js.registration.patient.select.plans.for.both.sponsors");
				alert(alertMsg);
				return false;
			}
			return true;
		}
		return true;
	}
	return true;
}

function insuPrimaryViewDoc(){

     var insuconame=document.getElementById('primary_insurance_co').value;
     if(!empty(insuconame)) {
     	var docname = findInList(insuCompanyDetails, "insurance_co_id", insuconame);
     	var insufilename=docname.insurance_rules_doc_name;

		if(!empty(insufilename)) {

			var insUrl = cpath+"/master/InsuranceCompMaster.do?_method=getviewInsuDocument";
			insUrl += "&inscoid=" + insuconame;
				if(document.getElementById('a1')) {
					document.getElementById('a1').href = insUrl;
					document.getElementById('viewinsuranceprimaryruledocs').style.display = 'block';

		      	} else {
			        	var aTag = document.createElement('a');
						aTag.setAttribute('id',"a1");
						aTag.setAttribute('href',insUrl);
						aTag.setAttribute('target','_blank');
						aTag.innerHTML = "Rules Document";
						document.getElementById('viewinsuranceprimaryruledocs').appendChild(aTag);
						document.getElementById('viewinsuranceprimaryruledocs').style.display = 'block';
					}
		}
	  }else {
              document.getElementById('viewinsuranceprimaryruledocs').style.display = 'none';
      }
}

function setSelectedDateForCorpInsurance() {
	var corpInsuPlanId = document.getElementById('primary_plan_id').value;
	document.getElementById('primary_policy_validity_start').textContent='';
	document.getElementById('primary_policy_validity_end').textContent='';
	if(corpInsuPlanId){
	for (var i = 0; i < policynames.length; i++) {
		if (policynames[i].plan_id == corpInsuPlanId) {
		var corpInsuStartDate = policynames[i].insurance_validity_start_date;
		var corpInsuEndDate =	policynames[i].insurance_validity_end_date;
		var corpInsuValStartDate = formatDate(new Date(policynames[i].insurance_validity_start_date));
		var corpInsuValEndDate = formatDate(new Date(policynames[i].insurance_validity_end_date));
			if(corpInsuStartDate){
				document.getElementById('primary_policy_validity_start').textContent=corpInsuValStartDate;
				document.getElementById('primary_policy_validity_end').textContent=corpInsuValEndDate;
				if (document.getElementById('primary_policy_validity_start1') != null) {
					document.getElementById('primary_policy_validity_start1').value = corpInsuValStartDate;
				}
				if (document.getElementById('primary_policy_validity_end1') != null) {
					document.getElementById('primary_policy_validity_end1').value = corpInsuValEndDate;
				}
				return true;
			} else {
				document.getElementById('primary_policy_validity_start').textContent='NA';
				document.getElementById('primary_policy_validity_end').textContent='NA';
				if (document.getElementById('primary_policy_validity_start1') != null) {
					document.getElementById('primary_policy_validity_start1').value = '';
				}
				if (document.getElementById('primary_policy_validity_end1') != null) {
					document.getElementById('primary_policy_validity_end1').value = '';
				}
				//isCorpInsuranceEmpty();
				return false;
				}
			 break;
		}
	 }
	}
	return true;
}

function isCorpInsuranceEmpty(){

	var primaryPolicyValidityStart = document.getElementById('primary_policy_validity_start').value;
	var primaryPolicyValidityStart1 = document.getElementById('primary_policy_validity_start1').value;
	var primaryPolicyValidityEnd = document.getElementById('primary_policy_validity_end').value;
	alert(primaryPolicyValidityStart+ " dddd "+primaryPolicyValidityStart1);
	if((primaryPolicyValidityStart == '') || (primaryPolicyValidityStart1 = '') )
	{alert("dddd");
		alert(getString("js.registration.patient.primary.policy.date.na.validation"));
		document.getElementById('primary_policy_validity_start').textContent='';
		document.getElementById('primary_policy_validity_end').textContent='';
		document.getElementById("primary_plan_id").focus();
		document.getElementById("primary_plan_id").selectedIndex = '0';
	}

}
function insuSecondaryViewDoc(){

      var secondaryinsdoc=document.getElementById('secondary_insurance_co').value;
       if(!empty(secondaryinsdoc)) {
	      var secdocname = findInList(insuCompanyDetails, "insurance_co_id", secondaryinsdoc);
	      var secinsufilename=secdocname.insurance_rules_doc_name;
	      if(!empty(secinsufilename)) {
				var inssecUrl = cpath+"/master/InsuranceCompMaster.do?_method=getviewInsuDocument";
				inssecUrl += "&inscoid=" + secondaryinsdoc;
				if(document.getElementById('b1')) {
					document.getElementById('b1').href = inssecUrl;
					document.getElementById('viewinsurancesecondaryruledocs').style.display = 'block';
		         } else {
			            var aTag = document.createElement('a');
						aTag.setAttribute('id',"b1");
						aTag.setAttribute('href',inssecUrl);
						aTag.setAttribute('target','_blank');
						aTag.innerHTML = "Rules Document";
						document.getElementById('viewinsurancesecondaryruledocs').appendChild(aTag);
						document.getElementById('viewinsurancesecondaryruledocs').style.display = 'block';

					}
	 	 }
       } else{
              document.getElementById('viewinsurancesecondaryruledocs').style.display = 'none';

          }
}

function enableDisableSecondaryPolicyDetails(disable, approvalAmtEnableDisable) {

	var secondaryMemberIdObj = document.getElementById("secondary_member_id");
	if (secondaryMemberIdObj != null) secondaryMemberIdObj.disabled = disable;

	var secondaryPolicyValidityStartObj = document.getElementById("secondary_policy_validity_start");
	if (secondaryPolicyValidityStartObj != null) secondaryPolicyValidityStartObj.readOnly = true;


	var secondaryPolicyValidityEndObj = document.getElementById("secondary_policy_validity_end");
	if (secondaryPolicyValidityEndObj != null) {
		if (trimAll(secondaryPolicyValidityEndObj.value) != '') {
			secondaryPolicyValidityEndObj.readOnly = true;
		} else {
			secondaryPolicyValidityEndObj.readOnly = false;
		}
	}

	var secondaryPolicyNumberObj = document.getElementById("secondary_policy_number");
	if (secondaryPolicyNumberObj != null) secondaryPolicyNumberObj.disabled = disable;

	var secondaryPolicyHolderObj = document.getElementById("secondary_policy_holder_name");
	if (secondaryPolicyHolderObj != null) secondaryPolicyHolderObj.disabled = disable;

	var secondaryPatientRelationshipObj = document.getElementById("secondary_patient_relationship");
	if (secondaryPatientRelationshipObj != null) secondaryPatientRelationshipObj.disabled = disable;

}


function enableDisablePrimaryPolicyDetails(disable, approvalAmtEnableDisable) {
	var primaryMemberIdObj = document.getElementById("primary_member_id");
	if (primaryMemberIdObj != null) primaryMemberIdObj.disabled = disable;

	var primaryPolicyValidityStartObj = document.getElementById("primary_policy_validity_start");
	if (primaryPolicyValidityStartObj != null) primaryPolicyValidityStartObj.readOnly = true;


	var primaryPolicyValidityEndObj = document.getElementById("primary_policy_validity_end");
	if (primaryPolicyValidityEndObj != null) {
		if (trimAll(primaryPolicyValidityEndObj.value) != '') {
			primaryPolicyValidityEndObj.readOnly = true;
		} else {
			primaryPolicyValidityEndObj.readOnly = false;
		}
	}

	var primaryPolicyNumberObj = document.getElementById("primary_policy_number");
	if (primaryPolicyNumberObj != null) primaryPolicyNumberObj.disabled = disable;

	var primaryPolicyHolderObj = document.getElementById("primary_policy_holder_name");
	if (primaryPolicyHolderObj != null) primaryPolicyHolderObj.disabled = disable;

	var primaryPatientRelationshipObj = document.getElementById("primary_patient_relationship");
	if (primaryPatientRelationshipObj != null) primaryPatientRelationshipObj.disabled = disable;

}

function compareMemberIdAndPattern(memberIdObj, memberIdPattern, memberIdLabel){
	var ok;
	var memberId;

	if(memberIdObj !=null){
		memberId = memberIdObj.value == null || memberIdObj.value == "" ? null : memberIdObj.value;
	}
	if(memberIdPattern != null && memberIdPattern != "" && memberId != null){
		if(memberId.length == memberIdPattern.length){
			for(i=0;i<memberId.length;i++){
				var patternChar = memberIdPattern.charAt(i);
				var membChar = memberId.charAt(i);
				if((patternChar == 'x') && (isNaN(membChar) == true)) {
					continue;
				} else if((patternChar == '9') && (isNaN(membChar) == false)) {
					continue;
				} else if(patternChar == membChar) {
					continue;
				}
				ok = confirm(memberIdLabel+" pattern is not matching with pattern:"+memberIdPattern);
				isMemberidValidated = true;
				if(!ok){
					memberIdObj.value='';
					memberIdObj.focus();
					return false;
				} else
					return true;
			}
		} else {
			ok = confirm(memberIdLabel+" pattern is not matching with pattern:"+memberIdPattern);
			isMemberidValidated = true;
			if(!ok){
				memberIdObj.value='';
				memberIdObj.focus();
				return false;
			} else
				return true;
		}
	}
	return true;
}

function getPrimaryPolicyValidityStartObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_start");
	} else return null;
}

function getSecondaryPolicyValidityStartObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_start");
	} else return null;
}

function getPrimaryPolicyValidityEndObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_end");
	} else return null;
}

function getSecondaryPolicyValidityEndObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_end");
	} else return null;
}

function getPrimaryInsuranceMemberIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_member_id");
	} else return null;
}

function getSecondaryInsuranceMemberIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_member_id");
	} else return null;
}

function getPrimaryInsuranceMemberIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_member_id");
	} else return null;
}

function getSecondaryInsuranceMemberIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_member_id");
	} else return null;
}

function getPrimaryInsurancePolicyNumberObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I'){
		return document.getElementById("primary_policy_number");
	}else return null;
}

function getSecondaryInsurancePolicyNumberObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I'){
		return document.getElementById("secondary_policy_number");
	}else return null;
}

function getPrimaryPatientHolderObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_holder_name");
	}else return null;
}

function getSecondaryPatientHolderObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_holder_name");
	}else return null;
}

function getPrimaryPatientRelationObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_patient_relationship");
	}else return null;
}

function getSecondaryPatientRelationObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_patient_relationship");
	}else return null;
}

function getPrimarySponsorObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_sponsor_id");
	}else return null;
}

function getSecondarySponsorObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_sponsor_id");
	}else return null;
}

function getPrimaryInsuObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_co");
	} else return null;
}

function getSecondaryInsuObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_co");
	} else return null;
}

function getPrimarySponsorNameObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_sponsor_name");
	}
}

function getSecondarySponsorNameObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_sponsor_name");
	}
}

function getPrimaryPlanTypeObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_plan_type");
	} else return null;
}

function getSecondaryPlanTypeObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_plan_type");
	} else return null;
}

function getPrimaryPlanObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_plan_id");
	} else return null;
}

function getSecondaryPlanObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_plan_id");
	} else return null;
}



