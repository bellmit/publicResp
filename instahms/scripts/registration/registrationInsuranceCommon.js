/* Insurance functions used in registration */

var primaryResource = getSchedulerPrimaryResource();



function onChangePrimarySponsor() {
	loadPrvPrescripitons = false;
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (document.getElementById("primary_sponsor_wrapper").checked)
		primarySponsorObj.value = 'I';
	else
		primarySponsorObj.value = "";

	resetPrimarySponsorChange();
	setPrimarySponsorDefaults();
	changeVisitType();
	setSchedulerPriorAuthDetails();
}


function onInsuranceCompanyChange(spnsrIndex){
      	loadRegistrationOtherInsDetails(spnsrIndex);
      	insuCatChange(spnsrIndex);
        RatePlanList();
        ratePlanChange();
        enableRegistrationDisableDocumentUploader(spnsrIndex);
}


function setMultiPlanExists(){
	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if(primarySponsorObj.value == 'I' &&  secondarySponsorObj.value == 'I') {
		multiPlanExists = true;
		document.getElementById("secPriorAuthHeader").style.display = 'table-cell';
		document.getElementById("preAuthHeader").style.display = 'none';
		document.getElementById("priPreAuthHeader").style.display = 'block';
	}else{
		multiPlanExists = false;
		document.getElementById("preAuthHeader").style.display = 'block';
		document.getElementById("priPreAuthHeader").style.display = 'none';
		document.getElementById("secPriorAuthHeader").style.display = 'none';
	}
	orderTableInit(true);
}


function setSchedulerPriorAuthDetails() {
	if (!empty(primaryResource)) {
		var spnsrIndex = getMainSponsorIndex();
		if (spnsrIndex == 'P') {
			var primaryPriorAuthIddObj = document.getElementById("primary_prior_auth_id");
			var primaryPriorAuthModeIdObj = document.getElementById("primary_prior_auth_mode_id");
			if (primaryPriorAuthIddObj != null)
				primaryPriorAuthIddObj.value = !empty(primaryResource.scheduler_prior_auth_no) ? primaryResource.scheduler_prior_auth_no : "";
			if (primaryPriorAuthModeIdObj != null)
				primaryPriorAuthModeIdObj.value = !empty(primaryResource.scheduler_prior_auth_mode_id) ? primaryResource.scheduler_prior_auth_mode_id : "";
		} else if (spnsrIndex == 'S') {
			var secondaryPriorAuthIddObj = document.getElementById("secondary_prior_auth_id");
			var secondaryPriorAuthModeIdObj = document.getElementById("secondary_prior_auth_mode_id");
			if (secondaryPriorAuthIddObj != null)
				secondaryPriorAuthIddObj.value = !empty(primaryResource.scheduler_prior_auth_no) ? primaryResource.scheduler_prior_auth_no : "";
			if (secondaryPriorAuthModeIdObj != null)
				secondaryPriorAuthModeIdObj.value = !empty(primaryResource.scheduler_prior_auth_mode_id) ? primaryResource.scheduler_prior_auth_mode_id : "";
		}
	}
}

function resetPrimarySponsorChange() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	var secondarySponsorWrapperObj = document.getElementById("secondary_sponsor_wrapper");
	if (primarySponsorObj != null && primarySponsorObj.value != '') {
		document.getElementById("primarySponsorGroup").style.display = 'block';
		if(corpInsuranceCheck=='N'){
			secondarySponsorObj.disabled = false;
			secondarySponsorWrapperObj.disabled = false;
		}

		if (primarySponsorObj.value == 'I') {
			if(corpInsuranceCheck == 'Y' && document.getElementById("primary_sponsor_id") != null){
				document.getElementById("primary_sponsor_id").value = '';
				document.getElementById("primary_sponsor_name").value = '';
			}

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
		}else if (primarySponsorObj.value == 'C') {
			document.getElementById("primaryCorporateTab").style.display = 'block';
			document.getElementById("primaryInsuranceTab").style.display = 'none';
			document.getElementById("primaryNationalTab").style.display = 'none';
			resetPrimaryInsuranceDetails();
			resetPrimaryNationalDetails();

		}else {
			document.getElementById("primaryNationalTab").style.display = 'block';
			document.getElementById("primaryInsuranceTab").style.display = 'none';
			document.getElementById("primaryCorporateTab").style.display = 'none';
			resetPrimaryCorporateDetails();
			resetPrimaryInsuranceDetails();
		}
	}else {
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
	resetSponsorDetailsTabs('P');
	resetPrimaryHiddenFieldsRelatedToInsurance();
}

function setAllDefaults() {
	setPrimarySponsorDefaults();
	setSecondarySponsorDefaults();
}

let primarySponsorDetails = {};
function primaryInsuranceAutoComplete(id,name,container){
   var url = cpath+"/master/tpas/lookup.json?";
   var dataSource = new YAHOO.util.DataSource(url);
   dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
   dataSource.responseSchema = {
     	resultsList : "dtoList",
     	fields: [
     	{key: "tpa_name"},
     	{key: "tpa_id"}
     	]
   	};
   	var autoComp = new YAHOO.widget.AutoComplete(name, container ,dataSource);
   	autoComp.generateRequest = function(sQuery) {
   		var categoryId = document.getElementById('patient_category_id').value;
        return "filterText="+sQuery+"&category_id="+categoryId;
    }
    autoComp.prehighlightClassName = "yui-ac-prehighlight";
    autoComp.typeAhead = false;
    autoComp.useShadow = false;
    autoComp.formatResult = Insta.autoHighlight;
    autoComp.allowBrowserAutocomplete = false;
    autoComp.minQueryLength = 0;
    autoComp.maxResultsDisplayed = 20;
    autoComp.forceSelection = true;
    autoComp.resultTypeList= false;

   autoComp.textboxBlurEvent.subscribe(function() {
	  var sponsorName = document.mainform.primary_sponsor_name.value;
		  	if(sponsorName == '') {
			  	document.mainform.primary_sponsor_name.value='';
				  document.mainform.primary_sponsor_id.value='';
          resetSponsorDetailsTabs('P');
			  }
	  });

 	autoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {

    var categoryId = document.getElementById('patient_category_id').value;
    document.getElementById(id).value = elItem[2].tpa_id;
    var sponsorDetailsObject = getSponsorDetails(elItem[2].tpa_id,categoryId);
    primarySponsorDetails = sponsorDetailsObject.insurance_sponsor_details;
  	loadRegistrationInsuCompanyDetails('P');
  	onInsuranceCompanyChange('P');

	});
}

let secondarySponsorDetails = {};
function secondaryInsuranceAutoComplete(id,name,container){
   var url = cpath+"/master/tpas/lookup.json?";
   var dataSource = new YAHOO.util.DataSource(url);
   dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
   dataSource.responseSchema = {
     	resultsList : "dtoList",
     	fields: [
     	{key: "tpa_name"},
     	{key: "tpa_id"}
     	]
   	};
   	var autoComp = new YAHOO.widget.AutoComplete(name, container ,dataSource);
   	autoComp.generateRequest = function(sQuery) {
   		var categoryId = document.getElementById('patient_category_id').value;
        return "filterText="+sQuery+"&category_id="+categoryId;
    }
    autoComp.prehighlightClassName = "yui-ac-prehighlight";
    autoComp.typeAhead = false;
    autoComp.useShadow = false;
    autoComp.formatResult = Insta.autoHighlight;
    autoComp.allowBrowserAutocomplete = false;
    autoComp.minQueryLength = 0;
    autoComp.maxResultsDisplayed = 20;
    autoComp.forceSelection = true;
    autoComp.resultTypeList= false;

   autoComp.textboxBlurEvent.subscribe(function() {
	  var sponsorName = document.mainform.secondary_sponsor_name.value;
		  	if(sponsorName == '') {
			  	document.mainform.secondary_sponsor_name.value='';
				  document.mainform.secondary_sponsor_id.value='';
          resetSponsorDetailsTabs('S');
			  }
	  });

 	autoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
    var categoryId = document.getElementById('patient_category_id').value;
  	document.getElementById(id).value = elItem[2].tpa_id;
    var sponsorDetailsObject = getSponsorDetails(elItem[2].tpa_id,categoryId);
    secondarySponsorDetails = sponsorDetailsObject.insurance_sponsor_details;
  	loadRegistrationInsuCompanyDetails('S');
  	onInsuranceCompanyChange('S');

	});
}
function setPrimarySponsorDefaults() {

	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj != null) {
		var primarySpnsrType = primarySponsorObj.value;

		if (primarySpnsrType == 'I') {
		  primaryInsuranceAutoComplete('primary_sponsor_id','primary_sponsor_name','primary_tpa_dropdown');
		  var visitType = screenid == 'ip_registration'? 'i': 'o';
      if (visitType === 'i') {
          var categoryId = document.getElementById('patient_category_id').value;
          var category = findInList(categoryJSON,'category_id',categoryId);
          if(category!==null && category.primary_ip_sponsor_id !== null){
                var sponsorDetailsObject = getSponsorDetails(category.primary_ip_sponsor_id,categoryId);
                primarySponsorDetails = sponsorDetailsObject.insurance_sponsor_details;
                document.getElementById('primary_sponsor_name').value = primarySponsorDetails.tpa_name;
                document.getElementById('primary_sponsor_id').value = primarySponsorDetails.tpa_id;
                loadRegistrationInsuCompanyDetails('P');
                var insuranceComp=findInList(primarySponsorDetails.insurance_companies,'insurance_co_id',category.primary_ip_insurance_co_id);
                if(insuranceComp !== null){
                    var primaryInsuranceObj = getPrimaryInsuObj();
                    setSelectedIndex(primaryInsuranceObj,insuranceComp.insurance_co_id);
                }
           onInsuranceCompanyChange('P');
          }
       }


		}else if (primarySpnsrType == 'C') {
			loadTpaList('P');
			onCorporateChange('P');
		}else if (primarySpnsrType == 'N') {
			loadTpaList('P');
			onNationalSponsorChange('P');
		}else {}
	}

	if (isModAdvanceIns) {
		var spnsrIndex = getMainSponsorIndex();
		if (spnsrIndex == 'S') policyNoAutoComplete('S', gPatientPolciyNos);
		else policyNoAutoComplete('P', gPatientPolciyNos);

		corporateNoAutoComplete('P', gPatientCorporateIds);
		corporateNoAutoComplete('S', gPatientCorporateIds);

		nationalNoAutoComplete('P', gPatientNationalIds);
		nationalNoAutoComplete('S', gPatientNationalIds);
	}
}

function setSecondarySponsorDefaults() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj != null) {
		var secondarySpnsrType = secondarySponsorObj.value;
		if (secondarySpnsrType == 'I') {
		secondaryInsuranceAutoComplete('secondary_sponsor_id','secondary_sponsor_name','secondary_tpa_dropdown');
	  var visitType = screenid == 'ip_registration'? 'i': 'o';
	  if (visitType === 'i') {
         var categoryId = document.getElementById('patient_category_id').value;
         var category = findInList(categoryJSON,'category_id',categoryId);
         if(category!==null && category.secondary_ip_sponsor_id !== null){
                var sponsorDetailsObject = getSponsorDetails(category.secondary_ip_sponsor_id,categoryId);
                secondarySponsorDetails = sponsorDetailsObject.insurance_sponsor_details;
                document.getElementById('secondary_sponsor_name').value = secondarySponsorDetails.tpa_name;
                document.getElementById('secondary_sponsor_id').value = secondarySponsorDetails.tpa_id;
                loadRegistrationInsuCompanyDetails('S');
                var insuranceComp=findInList(secondarySponsorDetails.insurance_companies,'insurance_co_id',category.secondary_ip_insurance_co_id);
                if(insuranceComp !== null){
                   var secondaryInsuranceObj = getSecondaryInsuObj();
                   setSelectedIndex(secondaryInsuranceObj,insuranceComp.insurance_co_id);
                }
                onInsuranceCompanyChange('S');
            }
      }

		}else if (secondarySpnsrType == 'C') {
			loadTpaList('S');
			onCorporateChange('S');

		}else if (secondarySpnsrType == 'N') {
			loadTpaList('S');
			onNationalSponsorChange('S');
		}else {}
	}

	if (isModAdvanceIns) {
		var spnsrIndex = getMainSponsorIndex();
		if (spnsrIndex == 'S') policyNoAutoComplete('S', gPatientPolciyNos);
		else policyNoAutoComplete('P', gPatientPolciyNos);

		corporateNoAutoComplete('P', gPatientCorporateIds);
		corporateNoAutoComplete('S', gPatientCorporateIds);

		nationalNoAutoComplete('P', gPatientNationalIds);
		nationalNoAutoComplete('S', gPatientNationalIds);
	}
}

function onChangeSecondarySponsor() {
	loadPrvPrescripitons = false;
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (document.getElementById("secondary_sponsor_wrapper").checked)
		secondarySponsorObj.value = 'I';
	else
		secondarySponsorObj.value = '';

	resetSecondarySponsorChange();
	setSecondarySponsorDefaults();
	changeVisitType();
	setSchedulerPriorAuthDetails();
}

function resetSecondarySponsorChange() {

	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj != null && secondarySponsorObj.value != '') {
		document.getElementById("secondarySponsorGroup").style.display = 'block';

		if (secondarySponsorObj.value == 'I') {
			if(document.getElementById("secondary_sponsor_id") != null)
				document.mainform.secondary_sponsor_id.value = '';

			document.getElementById("secondaryInsuranceTab").style.display = 'block';
			document.getElementById("secondaryCorporateTab").style.display = 'none';
			document.getElementById("secondaryNationalTab").style.display = 'none';
			resetSecondaryInsuranceDetails();
			resetSecondaryCorporateDetails();
			resetSecondaryNationalDetails();
		}else if (secondarySponsorObj.value == 'C') {
			document.getElementById("secondaryCorporateTab").style.display = 'block';
			document.getElementById("secondaryInsuranceTab").style.display = 'none';
			document.getElementById("secondaryNationalTab").style.display = 'none';
			resetSecondaryInsuranceDetails();
			resetSecondaryNationalDetails();
		}else {
			document.getElementById("secondaryNationalTab").style.display = 'block';
			document.getElementById("secondaryInsuranceTab").style.display = 'none';
			document.getElementById("secondaryCorporateTab").style.display = 'none';
			resetSecondaryInsuranceDetails();
			resetSecondaryCorporateDetails();
		}
	}else {
		resetSponsorDetailsTabs('S');
		resetSecondaryHiddenFieldsRelatedToInsurance();
		document.getElementById("secondarySponsorGroup").style.display = 'none';
		resetSecondaryInsuranceDetails();
		resetSecondaryCorporateDetails();
		resetSecondaryNationalDetails();
	}
	resetSponsorDetailsTabs('S');
	resetSecondaryHiddenFieldsRelatedToInsurance();

}

function resetPrimaryInsuranceDetails() {
	var primarySponsorIdObj = document.getElementById("primary_sponsor_id");
	if (primarySponsorIdObj != null) {
		document.getElementById("primary_sponsor_id").value = '';
		document.getElementById("primary_sponsor_name").value = '';
		onTpaChange('P');
	}

	var primaryInsuranceIdObj = document.getElementById("primary_insurance_co");
	if (primaryInsuranceIdObj != null) {
		primaryInsuranceIdObj.selectedIndex = 0;
		RatePlanList();
    ratePlanChange();
		//onLoadTpaList('P');
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
	if (primaryPatientRelationshipObj != null) primaryPatientRelationshipObj.value ="";

	var primaryPriorAuthIddObj = document.getElementById("primary_prior_auth_id");
	if (primaryPriorAuthIddObj != null) primaryPriorAuthIddObj.value = "";
	var primaryPriorAuthModeIdObj = document.getElementById("primary_prior_auth_mode_id");
	if (primaryPriorAuthModeIdObj != null) primaryPriorAuthModeIdObj.selectedIndex = 0;

	var primaryInsuranceDocContentObj = document.getElementById("primary_insurance_doc_content_bytea1");
	if (primaryInsuranceDocContentObj != null) primaryInsuranceDocContentObj.value = "";

	resetPrimaryVisitInsuranceDetails();
	resetPrimaryHiddenFieldsRelatedToInsurance();
}

function resetPrimaryCorporateDetails() {

	var primaryCorporateObj = document.getElementById("primary_corporate");
	if (primaryCorporateObj != null) {
		primaryCorporateObj.selectedIndex = 0;
	}

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

	var primaryNationalObj = document.getElementById("primary_national_sponsor");
	if (primaryNationalObj != null) {
		primaryNationalObj.selectedIndex = 0;
		onNationalSponsorChange('P');
	}

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
		document.getElementById("secondary_sponsor_id").value = '';
		document.getElementById("secondary_sponsor_name").value = '';
		onTpaChange('S');
	}

	var secondaryInsuranceIdObj = document.getElementById("secondary_insurance_co");
	if (secondaryInsuranceIdObj != null) {
		secondaryInsuranceIdObj.selectedIndex = 0;
		RatePlanList();
    ratePlanChange();
		//onLoadTpaList('S');
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
	if (secondaryPatientRelationshipObj != null) secondaryPatientRelationshipObj.value ="";

	var secondaryPriorAuthIddObj = document.getElementById("secondary_prior_auth_id");
	if (secondaryPriorAuthIddObj != null) secondaryPriorAuthIddObj.value = "";
	var secondaryPriorAuthModeIdObj = document.getElementById("secondary_prior_auth_mode_id");
	if (secondaryPriorAuthModeIdObj != null) secondaryPriorAuthModeIdObj.selectedIndex = 0;

	var secondaryInsuranceDocContentObj = document.getElementById("secondary_insurance_doc_content_bytea1");
	if (secondaryInsuranceDocContentObj != null) secondaryInsuranceDocContentObj.value = "";

	resetSecondaryVisitInsuranceDetails();
	resetSecondaryHiddenFieldsRelatedToInsurance();
}

function resetSecondaryCorporateDetails() {

	var secondaryCorporateObj = document.getElementById("secondary_corporate");
	if (secondaryCorporateObj != null) {
		secondaryCorporateObj.selectedIndex = 0;
	}

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

	var secondaryNationalObj = document.getElementById("secondary_national_sponsor");
	if (secondaryNationalObj != null) {
		secondaryNationalObj.selectedIndex = 0;
		onNationalSponsorChange('S');
	}

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

function loadRegistrationOtherInsDetails(spnsrIndex){
	var loadTpaOnInsChange = (isModAdvanceIns || isModInsurance);

	var tpaObj = null;
	var insuCompObj = null;
	var planTypeObj = null;
	var planObj = null;
	var tpaNameObj=null;
  var sponsorDetails;
	if (spnsrIndex == 'P') {
		tpaObj = getPrimarySponsorObj();
		tpaNameObj=getPrimarySponsorNameObj();
		insuCompObj = getPrimaryInsuObj();
		planTypeObj = getPrimaryPlanTypeObj();
		planObj = getPrimaryPlanObj();
		sponsorDetails = primarySponsorDetails;

	}else if (spnsrIndex == 'S') {
		tpaObj = getSecondarySponsorObj();
		tpaNameObj=getSecondarySponsorNameObj();
		insuCompObj = getSecondaryInsuObj();
		planTypeObj = getSecondaryPlanTypeObj();
		planObj = getSecondaryPlanObj();
		sponsorDetails = secondarySponsorDetails;
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
           if(!empty(sponsorDetails) && !empty(mainInsCompanyId)) {
              var i = 2;
              var selectedIns = findInList(sponsorDetails.insurance_companies, "insurance_co_id", mainInsCompanyId);
              var  networkPlantypes = selectedIns.network_plan_types
              for(var k = 0; k < networkPlantypes.length; k++){
                     var ele = networkPlantypes[k];
                     var optn = new Option(ele.category_name, ele.category_id);
                     planTypeObj.options.length = i;
                     planTypeObj.options[i-1] = optn;
                     i++;
                     planType = ele.category_id;
                     planNameDetails = ele.plan_names;
               }
            }         
	}

	var ratePlanObj = document.mainform.organization;
	var selectedRatePlan = ratePlanObj.value;
	var insCompDefaultRatePlan = '';

	var selectedIns = findInList(sponsorDetails.insurance_companies, "insurance_co_id", mainInsCompanyId);
	if (!empty(selectedIns) && !empty(selectedIns.default_rate_plan)) {
		insCompDefaultRatePlan = selectedIns.default_rate_plan;
	}
	if (!empty(insCompDefaultRatePlan))
		setSelectedIndex(ratePlanObj, insCompDefaultRatePlan);
	else
		setSelectedIndex(ratePlanObj, selectedRatePlan);

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
		enableRegistrationOtherInsuranceDetailsTab(spnsrIndex);
}

function enableRegistrationDisableDocumentUploader(sponserIndex){

	if (sponserIndex=='P'){
      if(!empty(primarySponsorDetails) && primarySponsorDetails.scanned_doc_required == 'N')
				      document.getElementById("primaryOthersDetailsTab").style.display = 'none';
	}
	if (sponserIndex=='S'){
		if(!empty(secondarySponsorDetails) && secondarySponsorDetails.scanned_doc_required == 'N')
				document.getElementById("secondaryOthersDetailsTab").style.display = 'none';
	}

}

function enableRegistrationOtherInsuranceDetailsTab(sponserIndex){

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

	var item;
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
		item = empty(primarySponsorDetails)?"":primarySponsorDetails;

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
		item = empty(secondarySponsorDetails)?"":secondarySponsorDetails;
	}

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
				enableRegistrationDisableDocumentUploader(sponserIndex);
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

function loadRegistrationInsuCompanyDetails(sponsorIndex){
	var categoryId='';
	if (document.mainform.patient_category_id)
			categoryId = document.mainform.patient_category_id.value;
	var tpaID;
	var insCompObj;
	var insCompID;
  var sponsorDetails;
	if(sponsorIndex == 'P'){
		tpaID = document.mainform.primary_sponsor_id.value;
		insCompObj= getPrimaryInsuObj();
		if(insCompObj != null)
			insCompID=insCompObj.value;
		sponsorDetails = primarySponsorDetails;
	}else if(sponsorIndex == 'S'){
		tpaID = document.mainform.secondary_sponsor_id.value;
		insCompObj= getSecondaryInsuObj();
		if(insCompObj != null)
			insCompID=insCompObj.value;
		sponsorDetails= secondarySponsorDetails;
	}
	var newins =[];
  if(!empty(sponsorDetails)){
      var insCompanyList = sponsorDetails.insurance_companies;
      for (var i = 0; i < insCompanyList.length; i++) {
          var item = {
              'insurance_co_id':insCompanyList[i].insurance_co_id,
              'insurance_co_name':insCompanyList[i].insurance_co_name
              };
          newins.push(item);
      }

      loadSelectBox(insCompObj, newins, 'insurance_co_name',
                 'insurance_co_id' , getString("js.registration.patient.commonselectbox.defaultText"));
      if(newins.length ==1 && insCompID ==''){
           setSelectedIndex(insCompObj, newins[0].insurance_co_id);
      }
      if(insCompID !=''){
         setSelectedIndex(insCompObj,insCompID);
      }
  }

	if(null != insCompObj && !empty(insCompObj.value) && 'P' == sponsorIndex){
		insuPrimaryViewDoc(insCompObj);
		if (screenid == 'Edit_Insurance') {
			primaryPlanTypeAjax = getPlanTypesAjax(insCompObj.value);
			if(typeof insuCatNames !== 'undefined'){
				insuCatNames = insuCatNames.concat(primaryPlanTypeAjax.categoryLists);
			}else{
				insuCatNames = primaryPlanTypeAjax.categoryLists;
			}
		}
	}else{
         document.getElementById('viewinsuranceprimaryruledocs').style.display = 'none';
    }

    if(null != insCompObj && !empty(insCompObj.value) && 'S' == sponsorIndex){
		insuSecondaryViewDoc(insCompObj);
		if (screenid == 'Edit_Insurance') {
			secondaryPlanTypeAjax = getPlanTypesAjax(insCompObj.value);
			if(typeof insuCatNames !== 'undefined'){
				insuCatNames = insuCatNames.concat(secondaryPlanTypeAjax.categoryLists);
			}else{
				insuCatNames = secondaryPlanTypeAjax.categoryLists;
			}
		}
	}else{
         document.getElementById('viewinsurancesecondaryruledocs').style.display = 'none';
  }

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

	//var primaryInsuranceApprovalObj = document.getElementById("primary_insurance_approval");
	//primaryInsuranceApprovalObj.disabled = approvalAmtEnableDisable;

	var primaryPlanTypeObj = document.getElementById("primary_plan_type");
	if (primaryPlanTypeObj != null) primaryPlanTypeObj.disabled = disable;

	var primaryPlanIdObj = document.getElementById("primary_plan_id");
	if (primaryPlanIdObj != null) primaryPlanIdObj.disabled = disable;

	var primaryDrgCheckObj = document.getElementById("primary_drg_check");
	var primaryUseDrgObj = document.getElementById("primary_use_drg");
	if (primaryDrgCheckObj != null) primaryDrgCheckObj.checked = false;
	if (primaryDrgCheckObj != null) primaryDrgCheckObj.disabled = disable;

	var primaryMemberIdObj = document.getElementById("primary_member_id");
	if  (primaryMemberIdObj != null) primaryMemberIdObj.disabled = disable;

	var primaryPolicyValidityStartObj = document.getElementById("primary_policy_validity_start");
	if (primaryPolicyValidityStartObj != null) primaryPolicyValidityStartObj.disabled = disable;

	var primaryPolicyValidityStartOnlyObj = document.getElementById("primary_policy_validity_only_start");
	if (primaryPolicyValidityStartOnlyObj != null) primaryPolicyValidityStartOnlyObj.disabled = disable;

	var primaryPolicyValidityendOnlyObj = document.getElementById("primary_policy_validity_only_end");
	if (primaryPolicyValidityendOnlyObj != null) primaryPolicyValidityendOnlyObj.disabled = disable;

	var primaryInsuranceDocContentObj = document.getElementById("primary_insurance_doc_content_bytea1");
	var primaryInsurancePasteObj = document.getElementById("primarySponsorI");
	var primaryInsuranceViewObj = document.getElementById("viewprimarySponsorI");

	var primaryPolicyValidityEndObj = document.getElementById("primary_policy_validity_end");
	if (primaryPolicyValidityEndObj != null) {
		primaryPolicyValidityEndObj.disabled = disable;
	}

	if (primaryInsuranceDocContentObj != null) primaryInsuranceDocContentObj.disabled = disable;
	if (primaryInsurancePasteObj != null) primaryInsurancePasteObj.style.display = (disable == true ? 'none' : 'block');
	if (primaryInsuranceViewObj != null) primaryInsuranceViewObj.style.display = (disable == true ? 'none' : 'block');

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

	//var primaryCorporateApprovalObj = document.getElementById("primary_corporate_approval");
	//primaryCorporateApprovalObj.disabled = approvalAmtEnableDisable;

	var primaryEmployeeIdObj = document.getElementById("primary_employee_id");
	primaryEmployeeIdObj.disabled = disable;

	var primaryEmployeeNameObj = document.getElementById("primary_employee_name");
	primaryEmployeeNameObj.disabled = disable;

	var primaryEmployeeRelationObj = document.getElementById("primary_employee_relation");
	primaryEmployeeRelationObj.disabled = disable;

	var primaryCorporateDocContentObj = document.getElementById("primary_corporate_doc_content_bytea1");
	if (primaryCorporateDocContentObj != null) primaryCorporateDocContentObj.disabled = disable;

	var primaryCorporatePasteObj = document.getElementById("primarySponsorC");
	if (primaryCorporatePasteObj != null) primaryCorporatePasteObj.style.display = (disable == true ? 'none' : 'block');

	var primaryCorporateViewObj = document.getElementById("viewPrimarySponsorC");
	if (primaryCorporateViewObj != null) primaryCorporateViewObj.style.display = (disable == true ? 'none' : 'block');
}

function enableDisablePrimaryNationalDetails(disable, approvalAmtEnableDisable) {

	var primaryCorporateObj = document.getElementById("primary_national_sponsor");
	primaryCorporateObj.disabled = disable;

	//var primaryNationalApprovalObj = document.getElementById("primary_national_approval");
	//primaryNationalApprovalObj.disabled = approvalAmtEnableDisable;

	var primaryNationalMemberIdObj = document.getElementById("primary_national_member_id");
	primaryNationalMemberIdObj.disabled = disable;

	var primaryNationalMemberNameObj = document.getElementById("primary_national_member_name");
	primaryNationalMemberNameObj.disabled = disable;

	var primaryNationalRelationObj = document.getElementById("primary_national_relation");
	primaryNationalRelationObj.disabled = disable;

	var primaryNationalDocContentObj = document.getElementById("primary_national_doc_content_bytea1");
	if (primaryNationalDocContentObj != null) primaryNationalDocContentObj.disabled = disable;

	var primaryNationalPasteObj = document.getElementById("primarySponsorN");
	if (primaryNationalPasteObj != null) primaryNationalPasteObj.style.display = (disable == true ? 'none' : 'block');

	var primaryNationalViewObj = document.getElementById("viewPrimarySponsorN");
	if (primaryNationalViewObj != null) primaryNationalViewObj.style.display = (disable == true ? 'none' : 'block');
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

	//var secondaryInsuranceApprovalObj = document.getElementById("secondary_insurance_approval");
	//secondaryInsuranceApprovalObj.disabled = approvalAmtEnableDisable;

	var secondaryPlanTypeObj = document.getElementById("secondary_plan_type");
	if (secondaryPlanTypeObj != null) secondaryPlanTypeObj.disabled = disable;

	var secondaryPlanIdObj = document.getElementById("secondary_plan_id");
	if (secondaryPlanIdObj != null) secondaryPlanIdObj.disabled = disable;

	var secondaryDrgCheckObj = document.getElementById("secondary_drg_check");
	var secondaryUseDrgObj = document.getElementById("secondary_use_drg");
	if (secondaryDrgCheckObj != null) secondaryDrgCheckObj.checked = false;
	if (secondaryDrgCheckObj != null) secondaryDrgCheckObj.disabled = disable;

	var secondaryMemberIdObj = document.getElementById("secondary_member_id");
	if (secondaryMemberIdObj != null) secondaryMemberIdObj.disabled = disable;

	var secondaryPolicyValidityStartObj = document.getElementById("secondary_policy_validity_start");
	if (secondaryPolicyValidityStartObj != null) secondaryPolicyValidityStartObj.disabled = disable;

	var secondaryPolicyValidityStartOnlyObj = document.getElementById("secondary_policy_validity_only_start");
	if (secondaryPolicyValidityStartOnlyObj != null) secondaryPolicyValidityStartOnlyObj.disabled = disable;

	var secondaryPolicyValidityendOnlyObj = document.getElementById("secondary_policy_validity_only_end");
	if (secondaryPolicyValidityendOnlyObj != null) secondaryPolicyValidityendOnlyObj.disabled = disable;

	var secondaryInsuranceDocContentObj = document.getElementById("secondary_insurance_doc_content_bytea1");
	var secondaryInsurancePasteObj = document.getElementById("secondarySponsorI");
	var secondaryInsuranceViewObj = document.getElementById("viewsecondarySponsorI");

	var secondaryPolicyValidityEndObj = document.getElementById("secondary_policy_validity_end");
	if (secondaryPolicyValidityEndObj != null) {
		secondaryPolicyValidityEndObj.disabled = disable;
	}

	if (secondaryInsuranceDocContentObj != null) secondaryInsuranceDocContentObj.disabled = disable;
	if (secondaryInsurancePasteObj != null) secondaryInsurancePasteObj.style.display = (disable == true ? 'none' : 'block');
	if (secondaryInsuranceViewObj != null) secondaryInsuranceViewObj.style.display = (disable == true ? 'none' : 'block');

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

	//var secondaryCorporateApprovalObj = document.getElementById("secondary_corporate_approval");
	//secondaryCorporateApprovalObj.disabled = approvalAmtEnableDisable;

	var secondaryEmployeeIdObj = document.getElementById("secondary_employee_id");
	secondaryEmployeeIdObj.disabled = disable;

	var secondaryEmployeeNameObj = document.getElementById("secondary_employee_name");
	secondaryEmployeeNameObj.disabled = disable;

	var secondaryEmployeeRelationObj = document.getElementById("secondary_employee_relation");
	secondaryEmployeeRelationObj.disabled = disable;

	var secondaryCorporateDocContentObj = document.getElementById("secondary_corporate_doc_content_bytea1");
	if (secondaryCorporateDocContentObj != null) secondaryCorporateDocContentObj.disabled = disable;

	var secondaryCorporatePasteObj = document.getElementById("secondarySponsorC");
	if (secondaryCorporatePasteObj != null) secondaryCorporatePasteObj.style.display = (disable == true ? 'none' : 'block');

	var secondaryCorporateViewObj = document.getElementById("viewSecondarySponsorC");
	if (secondaryCorporateViewObj != null) secondaryCorporateViewObj.style.display = (disable == true ? 'none' : 'block');
}

function enableDisableSecondaryNationalDetails(disable, approvalAmtEnableDisable) {

	var secondaryCorporateObj = document.getElementById("secondary_national_sponsor");
	secondaryCorporateObj.disabled = disable;

	//var secondaryNationalApprovalObj = document.getElementById("secondary_national_approval");
	//secondaryNationalApprovalObj.disabled = approvalAmtEnableDisable;

	var secondaryNationalMemberIdObj = document.getElementById("secondary_national_member_id");
	secondaryNationalMemberIdObj.disabled = disable;

	var secondaryNationalMemberNameObj = document.getElementById("secondary_national_member_name");
	secondaryNationalMemberNameObj.disabled = disable;

	var secondaryNationalRelationObj = document.getElementById("secondary_national_relation");
	secondaryNationalRelationObj.disabled = disable;

	var secondaryNationalDocContentObj = document.getElementById("secondary_national_doc_content_bytea1");
	if (secondaryNationalDocContentObj != null) secondaryNationalDocContentObj.disabled = disable;

	var secondaryNationalPasteObj = document.getElementById("secondarySponsorN");
	if (secondaryNationalPasteObj != null) secondaryNationalPasteObj.style.display = (disable == true ? 'none' : 'block');

	var secondaryNationalViewObj = document.getElementById("viewSecondarySponsorN");
	if (secondaryNationalViewObj != null) secondaryNationalViewObj.style.display = (disable == true ? 'none' : 'block');
}

/**********************************************************************/
function getPrimarySponsorObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_sponsor_id");
	}else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate");
	}else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_sponsor");
	}
	return null;
}

function getSecondarySponsorObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_sponsor_id");
	}else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate");
	}else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_sponsor");
	}
	return null;
}

function getPrimarySponsorNameObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_sponsor_name");
	}
	return null;
}

function getSecondarySponsorNameObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_sponsor_name");
	}
	return null;
}

function getPrimarySponsorTypeObj() {
	return  document.getElementById("primary_sponsor");
}

function getSecondarySponsorTypeObj() {
	return  document.getElementById("secondary_sponsor");
}

function getPrimaryInsuObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_co");
	}else return null;
}

function getSecondaryInsuObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_co");
	}else return null;
}

function getPrimaryPlanTypeObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_plan_type");
	}else return null;
}

function getSecondaryPlanTypeObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_plan_type");
	}else return null;
}

function getPrimaryPlanObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_plan_id");
	}else return null;
}

function getSecondaryPlanObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_plan_id");
	}else return null;
}
/*********************************************************************/

function getPrimaryPolicyValidityStartObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_start");
	}else return null;
}

function getSecondaryPolicyValidityStartObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_start");
	}else return null;
}

function getPrimaryPolicyValidityEndObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_end");
	}else return null;
}

function getSecondaryPolicyValidityEndObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_end");
	}else return null;
}

function getPrimaryInsuranceMemberIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_member_id");
	}else return null;
}

function getSecondaryInsuranceMemberIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_member_id");
	} else {
		return null;
	}
}

function getPrimaryMemberIdObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_member_id");
	} else if(primarySponsorObj.value == 'N'){
		return document.getElementById("primary_national_member_id");
	}else if(primarySponsorObj.value == 'C') {
		return document.getElementById("primary_employee_id");
	}else return null;
}

function getSecondaryMemberIdObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_member_id");
	}else if( secondarySponsorObj.value  == 'N'){
		return document.getElementById("secondary_national_member_id");
	}else if(secondarySponsorObj.value  == 'C') {
		return document.getElementById("secondary_employee_id");
	} else {
		return null;
	}
}

function getPrimaryInsurancePolicyNumberObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I')
		return document.getElementById("primary_policy_number");
	return null;
}

function getSecondaryInsurancePolicyNumberObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I')
		return document.getElementById("secondary_policy_number");
	return null;
}

function getPrimaryPatientHolderObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_holder_name");
	}else if( primarySponsorObj.value  == 'N'){
		return document.getElementById("primary_national_member_name");
	}else if(primarySponsorObj.value  == 'C') {
		return document.getElementById("primary_employee_name");
	} else {
		return null;
	}
}

function getSecondaryPatientHolderObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_holder_name");
	}else if( secondarySponsorObj.value  == 'N'){
		return document.getElementById("secondary_national_member_name");
	}else if(secondarySponsorObj.value  == 'C') {
		return document.getElementById("secondary_employee_name");
	} else {
		return null;
	}
}

function getPrimaryPatientRelationObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_patient_relationship");
	}else if( primarySponsorObj.value  == 'N'){
		return document.getElementById("primary_national_relation");
	}else if(primarySponsorObj.value  == 'C') {
		return document.getElementById("primary_employee_relation");
	} else {
		return null;
	}
}

function getSecondaryPatientRelationObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_patient_relationship");
	}else if( secondarySponsorObj.value  == 'N'){
		return document.getElementById("secondary_national_relation");
	}else if(secondarySponsorObj.value  == 'C') {
		return document.getElementById("secondary_employee_relation");
	} else {
		return null;
	}
}
/*********************************************************************/

function getPrimaryApprovalLimitObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_approval");
	}else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_approval");
	}else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_approval");
	}
	return null;
}

function getSecondaryApprovalLimitObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_approval");
	}else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_approval");
	}else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_approval");
	}
	return null;
}

function getPrimaryDRGCheckObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_drg_check");
	}else return null;
}

function getSecondaryDRGCheckObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_drg_check");
	}else return null;
}

function getPrimaryUseDRGObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_use_drg");
	}else return null;
}

function getSecondaryUseDRGObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_use_drg");
	}else return null;
}

function getPrimaryDocContentObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_doc_content_bytea1");
	}else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_doc_content_bytea1");
	}else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_doc_content_bytea1");
	}
	return null;
}

function getSecondaryDocContentObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_doc_content_bytea1");
	}else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_doc_content_bytea1");
	}else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_doc_content_bytea1");
	}
	return null;
}

function getPrimaryDocNameObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_insurance_doc_name1");
	}else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_doc_name1");
	}else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_doc_name1");
	}
	return null;
}

function getSecondaryDocNameObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_doc_name1");
	}else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_doc_name1");
	}else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_doc_name1");
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
	}else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primary_corporate_approval_star");
	}else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primary_national_approval_star");
	}
	return null;
}

function getSecondaryApprovalLimitStarObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_insurance_approval_star");
	}else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondary_corporate_approval_star");
	}else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondary_national_approval_star");
	}
	return null;
}

function getPrimaryPolicyValidityStartStarObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_start_star");
	}else return null;
}

function getSecondaryPolicyValidityStartStarObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_start_star");
	}else return null;
}

function getPrimaryPolicyValidityEndStarObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primary_policy_validity_end_star");
	}else return null;
}

function getSecondaryPolicyValidityEndStarObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondary_policy_validity_end_star");
	}else return null;
}

function getPrimaryUploadRowObj() {
	var primarySponsorObj = document.getElementById("primary_sponsor");
	if (primarySponsorObj.value == 'I') {
		return document.getElementById("primaryInsFile");
	}else if (primarySponsorObj.value == 'C') {
		return document.getElementById("primaryCorporateFile");
	}else if (primarySponsorObj.value == 'N') {
		return document.getElementById("primaryNationalFile");
	}
	return null;
}

function getSecondaryUploadRowObj() {
	var secondarySponsorObj = document.getElementById("secondary_sponsor");
	if (secondarySponsorObj.value == 'I') {
		return document.getElementById("secondaryInsFile");
	}else if (secondarySponsorObj.value == 'C') {
		return document.getElementById("secondaryCorporateFile");
	}else if (secondarySponsorObj.value == 'N') {
		return document.getElementById("secondaryNationalFile");
	}
	return null;
}

var patientRegPlanDetailsDialog;

function initPatientRegPlanDetailsDialog(buttonName) {
    patientRegPlanDetailsDialog = new YAHOO.widget.Dialog('patientRegPlanDetailsDialog', {
    	context:["","tr","br", ["beforeShow", "windowResize"]],
        width:"525px",
        visible: false,
        modal: true,
        constraintoviewport: true,
		close :false,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePatientRegPlanDetailsDialogCancel,
                                               scope:patientRegPlanDetailsDialog,
                                               correctScope:true } );
	scope:patientRegPlanDetailsDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	isPlanDetlDialgInitzld = true;
    patientRegPlanDetailsDialog.render();
}

function handlePatientRegPlanDetailsDialogCancel(){
	 document.getElementById('patientRegPlanDetailsDialog').style.display='none';
	 document.getElementById('patientRegPlanDetailsDialog').style.visibility='hidden';
	 patientRegPlanDetailsDialog.cancel();
}


function showPatientRegPlanDetailsDialog(planName) {
	var button = null;

	if (planName == "primary")
		button = document.getElementById('pd_primary_planButton');
	else if (planName == "secondary")
		button = document.getElementById('pd_secondary_planButton');

	if (button != null) {
		document.getElementById('patientRegPlanDetailsDialog').style.display='block';
		document.getElementById('patientRegPlanDetailsDialog').style.visibility='visible';
		patientRegPlanDetailsDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		getPatientPlanDetails(planName);
		patientRegPlanDetailsDialog.show();
	}
}

function getPatientPlanDetails(planName) {
	var planId = null;
	if (planName == "primary")
		planId = document.getElementById('primary_plan_id').value;
	else if (planName == "secondary")
		planId = document.getElementById('secondary_plan_id').value;
	if (!empty(planId)) {
		var ajaxReqObject = newXMLHttpRequest();
		var url = "./QuickEstimate.do?_method=getPlanDetails"
		url = url + "&plan_id=" + planId;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), false);
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				return handleAjaxResponse(reqObject.responseText,planName);
			}
		}
	}
}

function handleAjaxResponse(responseText,planName) {
	var planExclusion = null;
	var planNotes = null;

	if (responseText != null) {
		eval("var planDetails = " + responseText);
		if (!empty(planDetails.plan_exclusions))
			planExclusion = planDetails.plan_exclusions.split("\n");

		if (planExclusion != null && planExclusion.length > 0) {
			var exclusionChilds = document.getElementById('plan_exclusions').childNodes;
			for (var i=0; i<exclusionChilds.length; ) {
				document.getElementById('plan_exclusions').removeChild(exclusionChilds[i]);
			}
			for (var i=0;i<planExclusion.length;i++) {
				document.getElementById('plan_exclusions').appendChild(document.createTextNode(planExclusion[i]));
				document.getElementById('plan_exclusions').appendChild(document.createElement("br"));
			}
		} else {
			document.getElementById('plan_exclusions').textContent = "";
		}

		if (!empty(planDetails.plan_notes)) {
			planNotes = planDetails.plan_notes.split("\n");
		}

		if (planNotes != null && planNotes.length > 0) {
			var notesChilds = document.getElementById('plan_notes').childNodes;
			for (var i=0; i<notesChilds.length; ) {
				document.getElementById('plan_notes').removeChild(childs[i]);
			}
			for (var i=0;i<planNotes.length;i++) {
				document.getElementById('plan_notes').appendChild(document.createTextNode(planNotes[i]));
				document.getElementById('plan_notes').appendChild(document.createElement("br"));
			}
		} else {
			document.getElementById('plan_notes').textContent = "";
		}

		if (planName =="primary") {
			document.getElementById('primary_plan_div').title = planDetails.plan_name != null ? planDetails.plan_name : "";
		} else {
			document.getElementById('secondary_plan_div').title = planDetails.plan_name != null ? (planDetails.plan_name) : "";
		}
	} else {
		document.getElementById('plan_exclusions').textContent = "";
		document.getElementById('plan_notes').textContent = "";
		if (planName =="primary") {
			document.getElementById('primary_plan_div').title = "";
		}
		else {
			document.getElementById('secondary_plan_div').title = "";
		}
	}
}

function insuPrimaryViewDoc(obj){

     var insuconame=obj.value;
     var docname = findInList(insuCompanyDetails, "insurance_co_id", insuconame);
     var insufilename;
     if(!empty(docname))
     	insufilename=docname.insurance_rules_doc_name;

     if(!empty(insufilename)) {
     if(!empty(insuconame)) {
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
       } else {
              document.getElementById('viewinsuranceprimaryruledocs').style.display = 'none';
           }


}

function insuSecondaryViewDoc(obj){

      var secondaryinsdoc=obj.value;
      var secdocname = findInList(insuCompanyDetails, "insurance_co_id", secondaryinsdoc);
      var secinsufilename;
      if(!empty(secdocname))
      		secinsufilename=secdocname.insurance_rules_doc_name;
      if(!empty(secinsufilename)) {
      if(!empty(secondaryinsdoc)) {
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
