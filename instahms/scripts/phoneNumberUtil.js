/**
 * phoneObj -  The concatenated number is stored in this
 * phoneValidObj - Whether the number is valid or not
 * phoneNationalObj - The national part of phonenumber
 * phoneCountryObj- The country code part of phonenumber
 * isMandate - whether the field is mandatory
 * eventDataObj.isStrictValidate - If this is false ,then we only check when  the number is empty.
 */
function clearErrorsAndValidatePhoneNumber(phoneObj,phoneValidObj,phoneNationalObj,phoneCountryObj,phoneErrorObj,isMandate,eventDataObj){
	//changes border and updates the error msg's accordingly
	function changeBorderAndUpdateError(color, phoneValue){
		if (phoneValue && phoneObj.val() != phoneValue) {
			//Values have changes since validation started, skip this result.
			return;
		}
		if(color == "Y"){//success
			//No need to set true as we already set
		}
		else if(color == "N"){//failed and show error
			phoneValidObj.val("N");
			if(eventDataObj == null ||  eventDataObj.isShowErrorText != false){//show error text
				phoneErrorObj.css("visibility","");
				if(phoneCountryObj.val() == '+'){// show only if country_code is not selected
					phoneErrorObj.text("Please enter a valid country code");
					$("[aria-labelledby = select2-" + phoneCountryObj.attr("id") + "-container]").css("border-color","red");
				}
				else{
					phoneNationalObj.css("border-color","red");
					phoneErrorObj.text("Please enter a valid mobile number");
				}
			}
		}
		else{//clear existing error
			phoneObj.val("");
			phoneValidObj.val("Y"); //set to true by default
			phoneNationalObj.css("border-color","");
			phoneErrorObj.css("visibility","hidden");
			// select2 drop down
			$("[aria-labelledby = select2-" + phoneCountryObj.attr("id") + "-container]").css("border-color","");
		}
	}
	//reset
	changeBorderAndUpdateError();

	if(phoneNationalObj.val() !=''){ //number is not empty
		if (!isNumberStartsWithPlus(phoneNationalObj.val())) {
			phoneObj.val(phoneCountryObj.val() + phoneNationalObj.val());
		} else {
			phoneObj.val(phoneNationalObj.val());
		}
		if(phoneCountryObj.val() == '+' || phoneObj.val().length > 16){
			changeBorderAndUpdateError("N", phoneObj.val());
			return;
		}
		if(eventDataObj == null ||  eventDataObj.isStrictValidate != false){// strict validation is enabled
			validatePhoneNumber(phoneObj.val(),changeBorderAndUpdateError);
		}

	}
	else{
		if(isMandate == 'Y'){ //empty and  mandatory
			changeBorderAndUpdateError("N", phoneObj.val());
		}
	}
}

function clearErrorsAndValidatePhoneNumberSingleField(phoneObj,phoneValidObj,tempPhoneNationalObj,phoneNationalObj,phoneCountryObj,phoneErrorObj,isMandate,eventDataObj){
	clearErrorsAndValidatePhoneNumber(phoneObj,phoneValidObj,tempPhoneNationalObj,phoneCountryObj,phoneErrorObj,isMandate,eventDataObj);
	if(phoneValidObj.val() == "N"){
		phoneNationalObj.css("border-color","red");
	}else{
		phoneNationalObj.css("border-color","");
	}
}

function validatePhoneNumber(number,callback) {
	var phoneValue = number;
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.onreadystatechange = function() {
	    if (this.readyState == 4 && this.status == 200) {
	    	try{
		    	var json = JSON.parse(this.responseText);
		    	if(json['result'] == 'true'){
		    		callback('Y', phoneValue);
		    	}
		    	else{
		    		callback('N', phoneValue);
		    	}
	    	}
	    	catch(e){
	    		callback('Y', phoneValue); //allow as not to block flow
	    	}
	    }
	};
    var url = cpath + "/common/phonenumber.do?_method=isValidNumber&number=" + encodeURIComponent(number);
    //syncronous for a reason
    ajaxobj.open("GET", url.toString(), false);
    ajaxobj.send();
}

function getExamplePhoneNumber(countryCode,helpObj,errorObj) {
	if(countryCode == '+') {
		helpObj.prop("title","Choose country code from dropdown and enter phone number");
		return;
	}

	var ajaxobj = newXMLHttpRequest();
	ajaxobj.onreadystatechange = function() {
	    if (this.readyState == 4 && this.status == 200) {
	    	try{
		    	var json = JSON.parse(this.responseText);
		    	var num= json['result'];
		    	helpObj.prop("title","Please enter phone number here. Example : " + num);
	    	}
	    	catch(e){
	    		console.log('Country code for Default center is not set or is Invalid');
	    	}
	    }
	};
    var url = cpath + "/common/phonenumber.do?_method=getExampleNumber&countryCode=" + encodeURIComponent(countryCode.substr(1));
    ajaxobj.open("GET", url.toString(), true);
    ajaxobj.timeout = 2000; // time in milliseconds
    ajaxobj.send();
}

/*
 * Insert the phoneNumber into the phoneDom handling the obselete numbers properly
 * It fetches the country code and national number from phone number by doing AJAX call
 */
function insertNumberIntoDOM(phoneNumber,phoneObj,phoneCountryObj,phoneNationalObj){
	phoneNumber = (phoneNumber != null)?phoneNumber:'';
	var ajaxobj = newXMLHttpRequest();
	ajaxobj.onreadystatechange = function() {
	    if (this.readyState == 4 && this.status == 200) {
	    	try{
		    	var json = JSON.parse(this.responseText);
		    	if(phoneCountryObj.val() != "+" + json['country_code']){
		    		phoneCountryObj.val("+" + json['country_code']).trigger("change");
		    	}
		    	phoneNationalObj.val((json['national'] != null)? json['national']:'').trigger("blur");
	    	}
	    	catch(e){
	    		console.log('Invalid response');
	    	}
	    }
	};
    var url = cpath + "/common/phonenumber.do?_method=getNationalAndCountryCode&number=" + encodeURIComponent(phoneNumber);
    ajaxobj.open("GET", url.toString(), true);
    ajaxobj.timeout = 2000; // time in milliseconds
    ajaxobj.send();
}

function clearPhoneField(phoneCountryObj,phoneNationalObj,defaultCountryCode){
	if(phoneCountryObj.val() != defaultCountryCode ){
		phoneCountryObj.val(defaultCountryCode ).trigger("change");
	}
	phoneNationalObj.val("").trigger("blur",[{isShowErrorText: false}]);//Do not show error text
}

function getNationalAndCountryCode(phoneNumberObj, countryCodeObj){
	var phoneNumber = (phoneNumberObj.val() != null)?phoneNumberObj.val():'';
	var ajaxobj = newXMLHttpRequest();

	ajaxobj.onreadystatechange = function() {
	    if (this.readyState == 4 && this.status == 200) {
	    	try{
		    	var json = JSON.parse(this.responseText);
		    	countryCodeObj.val( "+" + json['country_code']);
		    	phoneNumberObj.val(json['national'] != null? json['national']:'');
	    	}
	    	catch(e){
	    		console.log('Invalid response');
	    	}
	    }
	};
	var url = cpath + "/common/phonenumber.do?_method=getNationalAndCountryCode&number=" + encodeURIComponent(phoneNumber);
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send();
}

function isNumberStartsWithPlus(number) {
	var PLUS_CHARS_ = '+\uFF0B';
	var VALID_START_CHAR_PATTERN_ = new RegExp('[' + PLUS_CHARS_+ ']');
	var success = false;
	var start = number.search(VALID_START_CHAR_PATTERN_);
	if (start >= 0) {
		success = true;
	} else {
		success = false;
	}
	return success;
}

function clearPhoneErrors(phoneNational, phoneError){
	if(phoneNational && phoneNational.length){
		phoneNational.css("border-color","");
	}
	if(phoneError && phoneError.length){
		phoneError.css("visibility","hidden");
	}
}