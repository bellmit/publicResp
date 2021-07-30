function hotKeys() {
	/**
	* which will adds the keylistener for the document(for key Alt + Shift + P).
	* toggles the Patient Details Collapsible Panel.
	*/
	var addInfoKeyListener = new YAHOO.util.KeyListener(document, { alt:true, shift: true, keys:73 }, { fn:toggleCollapsiblePanel,
					scope:CollapsiblePanel1,
					correctScope:true} );
	addInfoKeyListener.enable();
}

var customFieldList = null;
function initPreRegistration() {
	hotKeys();
	// Custom fields
	getCustomFieldList();
	// Create custom field list and filter according to display preferences.
	setCustomFieldList();

	initializeOnLoad();
	initLightbox();
	initAutocompletes();
	
	// Set patientConfidentialityCategories options
	initializePatientGroupsList();
	
	var mobileAccess = document.mainform.mobilePatAccess;
	if (isPatientPhoneMandate != 'Y' || !mobileAccess) {
		if (mobileAccess && mobileAccess.value == 'N' || !mobileAccess) {
			$(".patient_phone_star").hide();
		}
	}

	if (isPatientEmailMandate != 'Y') {
		if (mobileAccess && mobileAccess.value == 'N' || !mobileAccess) {
			$(".patient_email_star").hide();
		}
	}

	var notNewRegistration = (document.mainform.mr_no.value != null && document.mainform.mr_no.value != "");
	if(notNewRegistration)
		Insta.initMRNoAcSearch(cpath, 'original_mr_no', 'mrnoContainer', 'all', null, null,null,'Save');

	CategoryList();
	categoryChange();
	showHideCaseFile();
	if(notNewRegistration){
		setOnlyGovtPattern();
	} else {
		setGovtPattern();
	}
	checkAge();
	markCustomFieldsReadonly(notNewRegistration);
	document.mainform.salutation.focus();

	if (document.mainform.patient_category_id != null)
		sortDropDown(document.mainform.patient_category_id);

	if (document.mainform.is_duplicate != null) {
		if(originalMrNo == '')
			document.mainform.is_duplicate.checked = false;
		else
			document.mainform.is_duplicate.checked = true;

		document.mainform.original_mr_no.value = originalMrNo;
		isDuplicateChecked();
	}
// for four parts name while we editing registration we can split the middle name & show in different text boxes
	var name_parts_pref = document.getElementById("name_parts_pref").value;
	if(name_parts_pref == 4){
		var middle_name = document.getElementById("middle_name_split").value;
		if(middle_name != null && middle_name !='' && middle_name != undefined){
			if(middle_name.match(" ")){
				document.getElementById("middle_name").value = middle_name.substr(0,middle_name.indexOf(" "));
				document.getElementById("middle_name2").value = middle_name.substr(middle_name.indexOf(" ")+1,middle_name.length-1);
			}
			else
				document.getElementById("middle_name").value = middle_name;
		}
	}
	setOtherDoctype();
	initCard();

}

function isNewGenReg() {
    var radios = document.mrnoform.regType;
    for (var i=0; i<radios.length; i++) {
        if (radios[i].checked == true) {
            var value = radios[i].value;
            return value == 'new';
        }
    }
    return false;
}
function setOtherDoctype(){
	var otherIdElement = document.getElementById("other_identification_doc_id")
	if(otherIdElement.value != ""){
		document.getElementById("other_identification_doc_value_label").innerText = otherIdElement.options[otherIdElement.selectedIndex].label+ " Number";
	}
}

function CategoryList() {
	var categoryObj = document.getElementById("patient_category_id");
	if (categoryObj != null) {
		var optn = new Option(
				getString("js.registration.patient.commonselectbox.defaultText"),
				"");
		categoryObj.options[0] = optn;

		var len = 1;
		for (var i = 0; i < categoryJSON.length; i++) {
			optn = new Option(categoryJSON[i].category_name,
					categoryJSON[i].category_id);
			categoryObj.options[len] = optn;
			len++;
		}
		if (!empty(defaultPatientCategory) || !empty(savedPatientCategoryId)) {
			setSelectedIndex(categoryObj,
					!empty(savedPatientCategoryId) ? savedPatientCategoryId
							: defaultPatientCategory);
		} else {
			// if there is only one category found, then default it.
			if (len == 2)
				categoryObj.options.selectedIndex = 1;
			else
				setSelectedIndex(categoryObj, "");

		}
	}
}

// Create custom field list and filter according to display preferences.
function setCustomFieldList() {
	customFieldList = [{
		display: regPref.custom_list1_show,
		label: custom_list1_name
	}, {
		display: regPref.custom_list2_show,
		label: custom_list2_name
	}, {
		display: regPref.custom_list3_show,
		label: custom_list3_name
	}, {
		display: regPref.custom_list4_show,
		label: custom_list4_name
	}, {
		display: regPref.custom_list5_show,
		label: custom_list5_name
	},{
		display: regPref.custom_list6_show,
		label: custom_list6_name
	},{
		display: regPref.custom_list7_show,
		label: custom_list7_name
	},{
		display: regPref.custom_list8_show,
		label: custom_list8_name
	},{
		display: regPref.custom_list9_show,
		label: custom_list9_name
	},{
		display: regPref.custom_list3_show,
		label: custom_list3_name
	}, {
		display: regPref.custom_field1_show,
		label: custom_field1_label
	}, {
		display: regPref.custom_field2_show,
		label: custom_field2_label
	}, {
		display: regPref.custom_field3_show,
		label: custom_field3_label
	}, {
		display: regPref.custom_field4_show,
		label: custom_field4_label
	}, {
		display: regPref.custom_field5_show,
		label: custom_field5_label
	}, {
		display: regPref.custom_field6_show,
		label: custom_field6_label
	}, {
		display: regPref.custom_field7_show,
		label: custom_field7_label
	}, {
		display: regPref.custom_field8_show,
		label: custom_field8_label
	}, {
		display: regPref.custom_field9_show,
		label: custom_field9_label
	}, {
		display: regPref.custom_field10_show,
		label: custom_field10_label
	}, {
		display: regPref.custom_field11_show,
		label: custom_field11_label
	}, {
		display: regPref.custom_field12_show,
		label: custom_field12_label
	}, {
		display: regPref.custom_field13_show,
		label: custom_field13_label
	}, {
		display: regPref.custom_field14_show,
		label: custom_field14_label
	}, {
		display: regPref.custom_field15_show,
		label: custom_field15_label
	}, {
		display: regPref.custom_field16_show,
		label: custom_field16_label
	}, {
		display: regPref.custom_field17_show,
		label: custom_field17_label
	}, {
		display: regPref.custom_field18_show,
		label: custom_field18_label
	}, {
		display: regPref.custom_field19_show,
		label: custom_field19_label
	}, {
		display: regPref.passport_no_show,
		label: regPref.passport_no
	}, {
		display: regPref.passport_validity_show,
		label: regPref.passport_validity
	}, {
		display: regPref.passport_issue_country_show,
		label: regPref.passport_issue_country
	}, {
		display: regPref.visa_validity_show,
		label: regPref.visa_validity
	}, {
		display: regPref.family_id_show,
		label: regPref.family_id
	}, {
		display: regPref.nationality_show,
		label: regPref.nationality
	}];

	filterCustomFields();
}


function initializeOnLoad() {
	if (document.mainform.mr_no.value != "") { //loaded with existing
		var pdob = document.mainform.pdateofbirth.value;
		if(document.mainform.patient_category_id!=null){
			setSelectedIndex(document.mainform.patient_category_id, document.mainform.categoryId.value);
			document.mainform.patient_category_id.disabled = true;
		}

		var element = document.getElementsByName("category_expiry_date");
		for(var i=0; i<element.length;i++){
			var obj = element[i];
			var expiryDate = document.mainform.categoryExpiryDate.value;
			if(expiryDate!=""){
				if(obj.getAttribute("name")=="category_expiry_date"){
					var yy = expiryDate.substring(0, 4);
					var mm = expiryDate.substring(5, 7);
					var dd = expiryDate.substring(8, expiryDate.length);
					document.mainform.category_expiry_date.value=dd+'-'+mm+'-'+yy;
				}
			}
		}
		if (pdob != '') {
			setGenDateOfBirthFields('dob', parseFloat(pdob));
			dissableAge();
			if(hijriPref == 'Y') {
			    gregorianToHijri();
			}
		}else{
			dissableDobAndHijriDob();
		}
		document.mainform.register.value="Save";

		if(document.mainform.photosize.value==0){
			document.getElementById('viewPhoto').style.display='none';
		}else{
			document.getElementById('viewPhoto').style.display='block';
		}
	} else { //new registration
		document.mainform.patient_name.readOnly = false;
		document.mainform.register.value="Register";
		setDefaultCity();
	}
	if (document.mainform.category_expiry_date!=null)
		document.mainform.category_expiry_date.disabled = true;
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
}

function setGenDateOfBirthFields(prefix, dateNum) {
    var dt = new Date(dateNum);
    dt.setTime( dt.getTime() - new Date().getTimezoneOffset()*60*1000 );
    document.getElementById(prefix+"Year").value = dt.getFullYear();
    document.getElementById(prefix+"Day").value = dt.getDate();
    document.getElementById(prefix+"Month").value = dt.getMonth()+1;
}

function validatePatientRegister() {
	var flag = true;
	var oldMrno = document.getElementById("original_mr_no");
	if (document.getElementById("is_duplicate") != null
			&& document.getElementById("is_duplicate").checked
			&& oldMrno != null && oldMrno.value != '') {
		return flag;
	}
	var name_parts_pref = document.getElementById("name_parts_pref").value;
	var firstName = trim(document.getElementById('patient_name').value);
	if (name_parts_pref == 4){
		var middleName = trim(trim(document.getElementById('middle_name').value)+" "+trim(document.getElementById('middle_name2').value));
	}
	else {
		var middleName = trim(document.getElementById('middle_name').value)
	}
	var lastName = trim(document.getElementById('last_name').value);

	if (middleName == '' || middleName == "..MiddleName..") middleName = '';
	if (lastName == '' || lastName == "..LastName..") lastName = '';

	var gender = document.mainform.patient_gender.options[document.mainform.patient_gender.options.selectedIndex].value;
	var age = document.mainform.age.value;
	var dob = document.mainform.dateOfBirth.value;
	var phno = document.getElementById('patient_phone').value;
	var url = cpath+"/pages/registration/regUtils.do?_method=checkPatientDetailsExists&firstName=" + firstName
			+ "&middleName=" + middleName + "&lastName=" + lastName + "&gender=" + gender + "&age=" + age + "&dob=" + dob +'&phno='+encodeURIComponent(phno);
	var reqObject = newXMLHttpRequest();
	reqObject.open("POST", url.toString(), false);
	reqObject.send(null);
	if (reqObject.readyState == 4) {
		if ((reqObject.status == 200) && (reqObject.responseText != null)) {
			flag = getPatientInfo(reqObject.responseText);
		}
	}
	return flag;
}

function getPatientInfo(responseData) {
	existingPatientDetails = null;
	var existingMrNo = document.mainform.mr_no.value;
	existingMrNo = empty(existingMrNo) ? null : existingMrNo;
	eval("existingPatientDetails =" + responseData);
	if (!empty(existingPatientDetails) && existingMrNo != existingPatientDetails.mr_no) {
		var eobirth = empty(existingPatientDetails.eob) ? '' : existingPatientDetails.eob;
		var dobirth = empty(existingPatientDetails.dob) ? '' : existingPatientDetails.dob;

		var age = empty(existingPatientDetails.age) ? '' : existingPatientDetails.age;
		var agein = empty(existingPatientDetails.agein) ? '' : existingPatientDetails.agein;

		var ageAndDOB = age +" "+ agein;
		if (document.mainform.dateOfBirth.value != '' && document.mainform.dateOfBirth.value != null) {
			ageAndDOB = ageAndDOB + " / " + (empty(dobirth) ? eobirth : dobirth) ;
		}

		var area = empty(existingPatientDetails.patient_area) ? '' : existingPatientDetails.patient_area;
		var middleName = empty(existingPatientDetails.middle_name) ? '' : existingPatientDetails.middle_name;
		var lastName = empty(existingPatientDetails.last_name) ? '' : existingPatientDetails.last_name;
		var phoneNo = empty(existingPatientDetails.patient_phone) ? '' : existingPatientDetails.patient_phone;

		if (confirm(getString("js.registration.patient.patient.with.following.details.already.exists.string")+" " +
					" \n " + " \n"+" "+getString("js.registration.patient.mr.no.string")+" " + existingPatientDetails.mr_no +
					" \n " +getString("js.registration.patient.first.name.string")+" " + existingPatientDetails.patient_name +
					" \n " +getString("js.registration.patient.middle.name.string")+" " + middleName +
					" \n " +getString("js.registration.patient.last.name.string")+" "+ lastName +
					" \n " +getString("js.registration.patient.gender.string")+" "+ existingPatientDetails.patient_gender  +
					" \n " +getString("js.registration.patient.age.date.of.birth.string")+" "+ ageAndDOB)){
			return true;
		}

		return false;
	}

	return true;
}


function updateRecord() {
	if(patientDetailsValidation()==false){
		return false;
	}

	if (document.mainform.patient_category_id != null) {
		if (!validateRequired(document.mainform.patient_category_id, patientCategoryLabel+" is required."))
			return false;
	}

	if(patientAdditionalFieldsValidation()==false){
		return false;
	}

	//if (!validateCategoryExpiryDate()) return false;

	var visitType = "";
	if (mainPagefieldsList.length > 0) {
		for (var i=0;i<mainPagefieldsList.length;i++) {
			var custfieldObj = customFieldValidation(visitType, mainPagefieldsList[i]);
			if (custfieldObj != null) {
				CollapsiblePanel1.open();
				document.getElementsByName(custfieldObj)[0].focus();
				return false;
			}
		}
	}
	if (dialogFieldsList.length > 0) {
		for (var i=0;i<dialogFieldsList.length;i++) {
			var custfieldObj = customFieldValidation(visitType, dialogFieldsList[i]);
			if (custfieldObj != null) {
				CollapsiblePanel1.open();
				document.getElementsByName(custfieldObj)[0].focus();
			  	return false;
			}
		}
	}

	if (!validateOnChangePatientCategory()) return false;

	var notNewRegistration = (document.mainform.mr_no.value != null && document.mainform.mr_no.value != "");
	if(notNewRegistration) {
		if(document.mainform.mr_no.value == document.mainform.original_mr_no.value) {
			showMesssage("js.registration.preregistration.duplicatemrno");
			document.mainform.is_duplicate.checked = false;
			document.mainform.original_mr_no.disabled = true;
			document.mainform.original_mr_no.value='';
			return false;
		}
	}

	enableCustomLists();

	if (!validatePatientIdentification()) return false;

	if (!validateGovtIdentifierMandatory()) return false;

	//if (!patientAdditionalFieldsValidation()) return false;

	if (!validatePatientRegister()) return false;
	
	document.mainform.patient_gender.disabled = false;
	document.mainform.ageIn.disabled = false;

	document.mainform.submit();
	return true;
}

function isDuplicateChecked() {
	var duplicateChk = document.mainform.is_duplicate;
	if(!duplicateChk.checked) {
		document.mainform.original_mr_no.disabled = true;
		document.mainform.original_mr_no.value='';
	}
	else if(allowMarkDuplicate == 'A') {
		document.mainform.original_mr_no.disabled = false;
	}
}

/**
 * This function is used mainly in IncomingSampleRegistration
 * as there are no two DOB section so if hijri module is enabled
 * we will convert existing dob fields to hijri equivalent
 */
function gregorianToHijriISR() {
	var day = document.getElementById("dobDay").value;
	var month = document.getElementById("dobMonth").value;
	var year = document.getElementById("dobYear").value;

	if (day != '' && day != 'DD' && month != '' && month != 'MM' && year != '' && year != 'YY') {
		var ajaxobj = newXMLHttpRequest();

		if (!isInteger(month)) {
			return null;
		}
		if (!isInteger(day)) {
			return null;
		}
		if (!isInteger(year)) {
			return null;
		}
		if (year.length < 4) {
			var stryear = convertTwoDigitYear(parseInt(year, 10));
			if (stryear < 1900) {
				alert(getString("Invalid year:") + " " + stryear +
					". " + getString("js.registration.patient.must.be.two.digit.or.four.digit.year.string"));
				setTimeout("document.mainform.dobYear.focus()", 0);
				return null;
			}
			// silently set the 4-digit year back to the textbox, and get the new value
			document.mainform.dobYear.value = stryear;
			year = stryear;
		}
		//console.log("Entered 2 "+year+"::"+month+"::"+day);
		var expDate = new Date(1937, 3, 14);
		var futureDate = new Date(2077, 11, 16);
		var entDate = new Date(year, month, day);
		//console.log(expDate);
		if ((expDate.getTime() > entDate.getTime()) || (futureDate.getTime() < entDate.getTime())) {
			document.getElementById("dobDay").value = "";
			document.getElementById("dobMonth").value = "";
			document.getElementById("dobYear").value = "";
		} else {
			var url = cpath + '/pages/registration/regUtils.do?_method=getGregorianToHijri&dobDay=' + day
				+ '&dobMonth=' + month + '&dobYear=' + year;
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
						eval("var Date =" + ajaxobj.responseText);
						//alert(ajaxobj.responseText);
						document.getElementById("dobDay").value = Date.day;
						document.getElementById("dobMonth").value = Date.month;
						document.getElementById("dobYear").value = Date.year;
					}
				}
			}
		}
	}
}

function gregorianToHijri() {

	        var day =  document.getElementById("dobDay").value;
		    var month = document.getElementById("dobMonth").value;
		    var year = document.getElementById("dobYear").value;

		if (day != '' && day != 'DD' && month != '' && month != 'MM' && year != '' && year != 'YY') {
			var ajaxobj = newXMLHttpRequest();

		    if (!isInteger(month)) {
		        //showMessage("js.registration.patient.invalid.month.not.an.integer.string");
			    //setTimeout("document.mainform.dobMonth.focus()",0);
		        return null;
		    }
		    if (!isInteger(day)) {
		        //showMessage("js.registration.patient.invalid.month.not.an.integer.string");
			    //setTimeout("document.mainform.dobDay.focus()",0);
		        return null;
		    }
		    if(!isInteger(year)) {
		    	return null;
		    }
			if(year.length < 4) {
		        var stryear = convertTwoDigitYear(parseInt(year,10));
		        if (stryear < 1900) {
		            alert(getString("Invalid year:")+" " + stryear +
		                ". "+getString("js.registration.patient.must.be.two.digit.or.four.digit.year.string"));
			        setTimeout("document.mainform.dobYear.focus()",0);
		            return null;
		        }
		        // silently set the 4-digit year back to the textbox, and get the new value
		        document.mainform.dobYear.value = stryear;
		        year = stryear;
			}
			//console.log("Entered 2 "+year+"::"+month+"::"+day);
			var expDate = new Date(1937,3 ,14);
			var futureDate=new Date(2077,11,16);
			var entDate = new Date(year,month,day);
			//console.log(expDate);
			if((expDate.getTime()>entDate.getTime())||(futureDate.getTime()<entDate.getTime())) {
				document.getElementById("dobHDay").value = "";
				document.getElementById("dobHMonth").value = "";
				document.getElementById("dobHYear").value = "";
			} else {
				var url = cpath + '/pages/registration/regUtils.do?_method=getGregorianToHijri&dobDay=' + day
				+ '&dobMonth=' + month + '&dobYear=' + year;
				ajaxobj.open("POST", url.toString(), false);
				ajaxobj.send(null);
				if (ajaxobj) {
					if (ajaxobj.readyState == 4) {
						if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
							eval("var Date =" + ajaxobj.responseText);
							//alert(ajaxobj.responseText);
							document.getElementById("dobHDay").value = Date.day;
							document.getElementById("dobHMonth").value = Date.month;
							document.getElementById("dobHYear").value = Date.year;
						}
					}
				}
			}
		}
	}

	function hijriToGregorian() {
		var day =  document.getElementById("dobHDay").value;
		var month = document.getElementById("dobHMonth").value;
		var year = document.getElementById("dobHYear").value;

	    if (year != 'YYYY' && !isInteger(year)) {
	        showMessage("js.registration.patient.invalid.year.not.an.integer.string");
	        return null;
	    }
	    if (month != 'MM' && !isInteger(month)) {
	        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
		    //setTimeout("document.mainform.dobHMonth.focus()",0);
	        return null;
	    }
	    if (day != 'DD' && !isInteger(day)) {
	        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
		    //setTimeout("document.mainform.dobHDay.focus()",0);
	        return null;
	    }
	    if (parseInt(day) > 30) {
	        showMessage("js.common.invalid.date.enter.1.30.for.day.string");
	        return null;
	    }

	    if (parseInt(month) > 12) {
	        showMessage("js.common.invalid.date.enter.1.12.for.month.string");
	        return null;
	    }
		if(year.length < 4) {
			alert("Please enter the hijri year as YYYY");
			return null;
		}
		if (day != '' && day != 'DD' && month != '' && month != 'MM' && year != '' && year != 'YYYY') {
			var ajaxobj = newXMLHttpRequest();
			//alert("about to make ajax")

		var url = cpath + '/pages/registration/regUtils.do?_method=getHijriToGregorian&dobDay=' + day
		+ '&dobMonth=' + month + '&dobYear=' + year;
			//alert("1");
			ajaxobj.open("POST", url.toString(), false);
			ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
					eval("var Date =" + ajaxobj.responseText);
					//alert(ajaxobj.responseText);
					if (empty(Date.error)) {
						document.getElementById("dobDay").value = Date.day;
						document.getElementById("dobMonth").value = Date.month;
						document.getElementById("dobYear").value = Date.year;
						getAge(true, null);
					} else {
						document.getElementById("dobDay").value = '';
						document.getElementById("dobMonth").value = '';
						document.getElementById("dobYear").value = '';
					}
				}
			}
		}
	}
}

function calculateAgeAndHijri() {
	if(getAge(true, null)) {
		if(hijriPref == 'Y') {
			gregorianToHijri();
		}
		return true;
	} else
		return false;
}

var initSCConflictDialog;

function initSmartCardConflictDialog() {
	document.getElementById('SmartCardConflictDialog').style.display = 'block';
	initSCConflictDialog = new YAHOO.widget.Dialog('SmartCardConflictDialog', {
	   	context:["SmartCardConflictDialog","tr","br"],
	       visible: false,
	       modal: true,
	       constraintoviewport: true,
	   });

	   var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                            { fn:cancelOnConflict,
	                                              scope:initSCConflictDialog,
	                                              correctScope:true } );
	   initSCConflictDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	   initSCConflictDialog.cancelEvent.subscribe(cancelOnConflict);
	   initSCConflictDialog.render();
	}
var patientInfo;

function updateOnConflict() {
	initSCConflictDialog.hide();
	if(mainform.appointmentId.value=="")
		patientDetailsResponseHandlerWrapper(patientInfo);
	cardOverwrite();
	smartConflict = false;
}

function cancelOnConflict(){
	initSCConflictDialog.hide();
	if(mainform.appointmentId.value=="")
		clearRegDetails();
	smartConflict = false;
	document.getElementById("resource_captured_from").value="manual";
}

var initPatDetailsFromSCDialog;

function initPatDetailsFromSmartCardDialog() {
	document.getElementById('patientDetailsFromSmartCardDialog').style.display = 'block';
	initPatDetailsFromSCDialog = new YAHOO.widget.Dialog('patientDetailsFromSmartCardDialog', {
    	context:["patientDetailsFromSmartCardDialog","tr","br"],
    	fixedcenter: true,
        visible: false,
        modal: true,
        constraintoviewport: true,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePatDetailsFromSCDialogCancel,
                                               scope:initPatDetailsFromSCDialog,
                                               correctScope:true } );
    initPatDetailsFromSCDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
    initPatDetailsFromSCDialog.cancelEvent.subscribe(handlePatDetailsFromSCDialogCancel);
    initPatDetailsFromSCDialog.render();
}

function handlePatDetailsFromSCDialogCancel(){
	initPatDetailsFromSCDialog.hide();
	if (!empty(document.getElementById('identifier_id')))
		document.getElementById('identifier_id').removeAttribute('disabled');
	clearRegDetails();
	document.getElementById("resource_captured_from").value="manual";
}

function showPatientDetailsFmomSCDialog() {
	var button = null;
	button = document.getElementById('readCard');

	if (button != null) {
		initPatDetailsFromSCDialog.cfg.setProperty("context", [button, "tr", "br"], false);
		showPatientDetailsFromSC();
	}
}


function showPatientDetailsFromSC(){
	if(null != dt['photo'] && dt['photo'] != "")
		document.getElementById("patient-card-image").src= "data:image/jpeg;base64," + dt['photo'];
	else
		document.getElementById("patient-card-image").src= cpath + "/images/patienPlaceholder.png";
	document.getElementById("nationalid_sc1").textContent= dt["idNumber"];
	document.getElementById("title_sc1").textContent= getTitle(dt['sex'],dt['maritalStatus']);
	document.getElementById("patientName_sc1").textContent= dt["fullNameEnglish"];
	document.getElementById("gender_sc1").textContent= dt["sex"];
	document.getElementById("nationality_sc1").textContent= dt["nationality"];
	document.getElementById("dob_sc1").textContent= dt["dateOfBirth"];

	initPatDetailsFromSCDialog.show();
}

function initCard(){

	initSmartCardConflictDialog();
	initPatDetailsFromSmartCardDialog();
	initPatDetailsFromSmartCardDialogErr();
}


var initPatDetailsFromSCDialogErr;

function initPatDetailsFromSmartCardDialogErr() {
	document.getElementById('patientDetailsFromSmartCardDialogForErr').style.display = 'block';
	initPatDetailsFromSCDialogErr = new YAHOO.widget.Dialog('patientDetailsFromSmartCardDialogForErr', {
    	context:["patientDetailsFromSmartCardDialogForErr","tr","br"],
    	fixedcenter: true,
        visible: false,
        modal: true,
        constraintoviewport: true,
    });

    var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
                                             { fn:handlePatDetailsFromSCDialogCancelErr,
                                               scope:initPatDetailsFromSCDialogErr,
                                               correctScope:true } );
    initPatDetailsFromSCDialogErr.cfg.queueProperty("keylisteners", [escKeyListener]);
    initPatDetailsFromSCDialogErr.cancelEvent.subscribe(handlePatDetailsFromSCDialogCancelErr);
    initPatDetailsFromSCDialogErr.render();
}

function handlePatDetailsFromSCDialogCancelErr(){
	initPatDetailsFromSCDialogErr.hide();
}

function showPatientDetailsFmomSCDialogErr(errors) {
		document.getElementById("pd_sc_error").textContent=errors;
		initPatDetailsFromSCDialogErr.show();
}

function closeErrDialogue(){
	initPatDetailsFromSCDialogErr.hide();
}

var conflictCheckFlag = false;
var smartCardPatientNotFound = false;
var dt;
function readFromCard(){
	var ajaxReqObject = newXMLHttpRequest();
	var url = '//127.0.0.1:9876/devices/cardReader/read';
	var reqObject = newXMLHttpRequest();
	reqObject.open("GET", url.toString(), false);
	try {
		reqObject.send(null);
		if (reqObject.readyState == 4) {
			if ((reqObject.status == 200) && (reqObject.responseText != null)) {
				smartCardDetails=reqObject.responseText;
				dt=JSON.parse(smartCardDetails);
				if(null != dt["dateOfBirth"]) {		//dt["dateOfBirth"] is of format MM/dd/yyyy
					var day = dt["dateOfBirth"].substring(3,5);
					var month = dt["dateOfBirth"].substring(0,2);
					var year = dt["dateOfBirth"].substring(6,10);
					dt["dateOfBirth"] = day + "/" + month + "/" + year;
				}
				if(null != dt["idNumber"])
					dt["idNumber"] = applyRegEx(dt["idNumber"],smartCardIDPattern);
				if(dt['cardStatus'] == 'The Card Read Successfully' && smartCardDetails != null && smartCardDetails !=""){
					if(mainform.appointmentId.value!=""){
						var dobSys;
						var nameSys;
						var nameSc;
						if(null!=document.getElementById("dobDay")) {
							var dobDay = document.getElementById("dobDay").value;
							var dobMonth = document.getElementById("dobMonth").value;
							var dobYear = document.getElementById("dobYear").value;
							if(dobDay.length == 1)
								dobSys = "0"+dobDay+"/";
							else
								dobSys = dobDay+"/";
							if(dobMonth.length == 1)
								dobSys = dobSys + "0" + dobMonth + "/";
							else
								dobSys = dobSys + dobMonth + "/";
							dobSys = dobSys + dobYear;
						}
						if(null!=document.getElementById("patient_name"))
							nameSys = document.getElementById("patient_name").value;
						if(null!=document.getElementById("middle_name") && document.getElementById("middle_name").value!='..MiddleName..')
							nameSys = nameSys + " " + document.getElementById("middle_name").value;
						if(null!=document.getElementById("last_name") && document.getElementById("last_name").value!='..LastName..')
							nameSys = nameSys + " " + document.getElementById("last_name").value;

						if(null!=dt["fName"])
							nameSc = dt["fName"];
						if(null!=dt["mName"])
							nameSc = nameSc + " " + dt["mName"];
						if(null!=dt["lName"])
							nameSc = nameSc + " " + dt["lName"];

						if(nameSys!=nameSc || document.getElementById("government_identifier").value!=dt["idNumber"] || dobSys!=dt["dateOfBirth"] ) {

							if(dt['photo'] != null && dt['photo'] != "")
								document.getElementById("img_sc").src= "data:image/jpeg;base64," + dt['photo'];
							else
								document.getElementById("img_sc").src= cpath + "/images/patienPlaceholder.png";
							document.getElementById("nationalid_sc").textContent= dt["idNumber"];
							document.getElementById("patientName_sc").textContent= nameSc;
							document.getElementById("dob_sc").textContent= dt['dateOfBirth'];

							document.getElementById("img_sys").src= cpath + "/images/patienPlaceholder.png";
							document.getElementById("nationalid_sys").textContent= document.getElementById("government_identifier").value;
							document.getElementById("patientName_sys").textContent= nameSys;
							document.getElementById("dob_sys").textContent= dobSys;
							initSCConflictDialog.show();
						}
						else
							alert("Card details already filled");
					}
					else
						handleAjaxResponseForCardDetails(smartCardDetails);
				}else if(dt['error'] != ""){
					return showPatientDetailsFmomSCDialogErr(dt['error']);
				}
			}
			else{
				return showPatientDetailsFmomSCDialogErr(reqObject.statusText);
			}
		}else{
			return showPatientDetailsFmomSCDialogErr(reqObject.statusText);
		}
	} catch (ex) {
		alert("InstaNexus App does not seem to be running, Please start the app and try again.");
	}

	if(mainform.appointmentId.value=="") {

			var sqlDOB = dt["dateOfBirth"].substring(6,10) +"-"+ dt["dateOfBirth"].substring(3,5) +"-"+ dt["dateOfBirth"].substring(0,2);

			conflictCheckFlag = true;
			//myAutocomp.itemSelectEvent.subscribe(conflictCheck);
			smartCardPatientNotFound = true;
	}
}


function applyRegEx(data,pattern) {
	var newData = "";
	if(null == pattern || null == data || pattern == "" || data == "")
		return data;
	var dataLen = data.length;
	var patternLen = pattern.length;
	if(patternLen < dataLen) {
		alert("Card data does not match defined pattern");
		return data;
	}
	var dataPos = 0;
	for(var i=0;i<patternLen;i++) {
		if(pattern[i]=='9') {
			if(dataPos < dataLen && data[dataPos] >='0' && data[dataPos] <='9') {
				newData = newData + data[dataPos];
				dataPos = dataPos + 1;
			}
			else {
				alert("Card data does not match defined pattern");
				return data;
			}
		}
		else if(pattern[i]=='x' || pattern[i]=='X') {
			if(dataPos < dataLen && (data[dataPos] >='a' && data[dataPos] <='z') || (data[dataPos] >='A' && data[dataPos] <='Z')) {
				newData = newData + data[dataPos];
				dataPos = dataPos + 1;
			}
			else {
				alert("Card data does not match defined pattern");
				return data;
			}
		}
		else {
			newData = newData + pattern[i];
		}
	}
	if (dataPos != dataLen) {
		alert("Card data does not match defined pattern");
		return data;
	}
	return newData;
}

function noSmartCardPatient(oSelf , sQuery , aResults){
	if(decodeURI(sQuery[1]) != JSON.parse(smartCardDetails)["fName"]) {
		conflictCheckFlag = false;
	}
	if(smartCardPatientNotFound ==true) {
		smartCardPatientNotFound = false;
		if(sQuery[2].length == 0) {
			conflictCheckFlag = false;
			alert("Patient not found, please register as a new patient.");
		}
	}
}

function handleAjaxResponseForCardDetails(smartCardDetails){
	showPatientDetailsFmomSCDialog();
}

function getTitle(sex,maritalStatus){
	 if(sex =='M'){
		 return 'Mr';
	 }
	 else if(sex =='F' && (maritalStatus =='Single' || maritalStatus == 'Not married' || maritalStatus == '')){
		 return 'Miss';
	 }
	 else if(sex=='F' && (maritalStatus =='Married' || maritalStatus == 'Divorced'|| maritalStatus == 'Widowed')){
		 return 'Mrs';
	 }
}

function saveDetailsInRegScreen(){
	cardOverwrite();
	initPatDetailsFromSCDialog.hide();
}

function cardOverwrite() {

	if(null != dt && "" != dt) {
		document.getElementById("patient_name").value = dt["fName"];
		if(regPref.name_parts == 4 && dt["mName1"]) {
			document.getElementById("middle_name").value = dt["mName1"];
			document.getElementById("middle_name2").value = dt["mName2"];
		} else {
			document.getElementById("middle_name").value = dt["mName"];
		}
		document.getElementById("last_name").value = dt["lName"];
		if(null	!=	document.getElementById("government_identifier"))
			document.getElementById("government_identifier").value = dt["idNumber"];
		document.getElementById("dobDay").value = dt["dateOfBirth"].substring(0,2);
		document.getElementById("dobMonth").value = dt["dateOfBirth"].substring(3,5);
		document.getElementById("dobYear").value = dt["dateOfBirth"].substring(6,10);
		calculateAgeAndHijri();
		if(null	!=	document.getElementById("identifier_id")) {
				document.getElementById("identifier_id").disabled = true;
		}
		if(dt["maritalStatus"]=="Married" && dt["sex"]=="F" ) {
			document.getElementById("salutation").value = "SALU0003";
			document.getElementById("patient_gender").value = "F";
		}
		else if(dt["sex"]=="F") {
			document.getElementById("salutation").value = "SALU0004";
			document.getElementById("patient_gender").value = "F";
		}
		else if(dt["sex"]=="M") {
			document.getElementById("salutation").value = "SALU0002";
			document.getElementById("patient_gender").value = "M";
		}

//		document.getElementById("resource_captured_from").value="card";
//		document.getElementById("cardImage").value=dt["photo"];
//		document.getElementById("patPhoto").value=null;
		if(null != dt["address"] && dt["address"] != "")
			document.getElementById("patient_address").value = dt["address"];
// 		if(null != dt["area"] && dt["area"] != "")
// 			document.getElementById("patient_area").value = dt["area"];
		if(null != dt["contactNo"] && dt["contactNo"] != "") {
			document.getElementById("patient_phone2").value=dt["contactNo"];
		}

		if (null != dt["nationality"] && dt["nationality"] != "" && null != document.getElementById('nationality_id')) {
			updateNationalityId(dt["nationality"]);
		}

	}
}

function updateNationalityId(natioanlity) {
	var nationalityCode = cpath + "/master/countries/lookup.json?nationality=" + natioanlity;
	$.ajax({
		url : nationalityCode,
		dataType : 'json',
		success : function(response) {
			if(response && response.nationality) {
				$('#nationality_id').val(response.nationality.country_id);
			}
		}
	});
}

