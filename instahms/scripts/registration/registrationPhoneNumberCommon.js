(function(){
		var patientPhone = $("#patient_phone");		
		var patientPhoneNational=$("#patient_phone_national");
		var patientPhoneCountryCode=$("#patient_phone_country_code");
		var patientPhoneHelp=$("#patient_phone_help");
		var patientPhoneError =$("#patient_phone_error");
		var patientPhoneValid = $("#patient_phone_valid");
		//kin
		var kinPhone = $("#patient_care_oftext");		
		var kinPhoneNational= $("#patient_care_oftext_national");
		var kinPhoneCountryCode= $("#patient_care_oftext_country_code");
		var kinPhoneHelp=$("#patient_care_oftext_help");
		var kinPhoneError = $("#patient_care_oftext_error");
		var kinPhoneValid =$("#patient_care_oftext_valid");
	
		var radios = document.mainform.mobilePatAccess;
		if (radios) {
		for (var i = 0, max = radios.length; i < max; i++) {
			radios[i].onclick = function() {
				if (this.value == 'Y') {
					$(".patient_phone_star").show();
					$(".patient_email_star").show();
				} else {
					if (isPatientPhoneMandate != 'Y') {
						$(".patient_phone_star").hide();
					}
					if (isPatientEmailMandate != 'Y') {
						$(".patient_email_star").hide();
					}
				}
			}
		}
	}
		patientPhoneCountryCode.select2();
		kinPhoneCountryCode.select2();
		patientPhoneCountryCode.on('change', function (e) {
			//get text for help menu
		    getExamplePhoneNumber(this.value,patientPhoneHelp,patientPhoneError);
		});
		
		patientPhoneCountryCode.on('select2:select', function (e) {
		    patientPhoneNational.focus();
		});
		kinPhoneCountryCode.on('change', function (e) {
			//get text for help menu
		    getExamplePhoneNumber(this.value,kinPhoneHelp,kinPhoneError);
		});
		kinPhoneCountryCode.on('select2:select', function (e) {
		    kinPhoneNational.focus();
		});		
		
		patientPhoneNational.on('blur',function(e,eventDataObj){

			clearErrorsAndValidatePhoneNumber(patientPhone,patientPhoneValid,
					patientPhoneNational,patientPhoneCountryCode,patientPhoneError,isPatientPhoneMandate,eventDataObj);
				
		});
		kinPhoneNational.on('blur',function(e,eventDataObj){
			//Strict validation 'off' for kin
			if(eventDataObj == null) eventDataObj = {};
			eventDataObj.isStrictValidate = false;
			clearErrorsAndValidatePhoneNumber(kinPhone,kinPhoneValid,
					kinPhoneNational,kinPhoneCountryCode,
					kinPhoneError,isPatientCareOftextMandate,eventDataObj);
				
		});
		// Get help text for patient_phone
		getExamplePhoneNumber(defaultCountryCode,patientPhoneHelp,patientPhoneError);
		// Get help text for patient kin phone
		getExamplePhoneNumber(defaultCountryCode,kinPhoneHelp,kinPhoneError);
		
		//set country and national number of patient_phone
		if(trim(patientPhoneInitialValue) == ''){
            //Don't show error text , But  store the result in patient_phone_valid
			clearPhoneField(patientPhoneCountryCode,patientPhoneNational,defaultCountryCode);
		}
		else{
			insertNumberIntoDOM(patientPhoneInitialValue,patientPhone,patientPhoneCountryCode,
					patientPhoneNational);
		}
		
		//set country and national number of kin phone
		if(trim(kinPhoneInitialValue) == ''){
			 //Don't show error text , But  store the result in patient_care_oftext_valid
			clearPhoneField(kinPhoneCountryCode,kinPhoneNational,defaultCountryCode);
		}
		else{
			insertNumberIntoDOM(kinPhoneInitialValue,kinPhone,kinPhoneCountryCode,
					kinPhoneNational);
		}
		
})();