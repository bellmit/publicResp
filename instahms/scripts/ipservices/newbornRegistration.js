function init() {
	setSelectedIndex(document.mainform.billtype, defaultBillType);
	var dept = document.mainform.babyAttendingDeptIP.value;
	initDoctorDept(dept);
	populateBedTypes();

	var date= new Date();
	document.getElementById("dobDay").value=date.getDate();
	document.getElementById("dobMonth").value=date.getMonth()+1;
	document.getElementById("dobYear").value=date.getFullYear();

	if(hijriPref == 'Y') {
		gregorianToHijri();
	}
	disableEnableIndicationForCS();
}

function disableEnableIndicationForCS() {
	 var el = document.getElementById('deliveryType');
	 var deliveryType = el.options[el.selectedIndex].value;
	 if (deliveryType === 'C') {
	 		$('select[name="caesareanIndicationId"]').removeAttr("disabled");
	 } else {
	 		$('select[name="caesareanIndicationId"]').attr("disabled", "disabled");
	 }
}

function populateBedTypes(){
	 var obj = document.getElementById('bedtype');
	 var generalOrgBeds = filterList(bedCharges, 'organization', 'ORG0001');
	 loadSelectBox(obj, generalOrgBeds, 'bedtype', 'bedtype', '-- Select --');
}

var docAutoComp = null;
function initDoctorDept(dept) {

	if (docAutoComp != null) {
		docAutoComp.destroy();
	}

	var docDeptNameArray = [];
	var jDeptDocList = null;

	if(jDocDeptNameList !=null && jDocDeptNameList.length > 0) {
		if (dept != null && dept != '')
			jDeptDocList = filterList(jDocDeptNameList, 'dept_id', dept);
		else
			jDeptDocList = jDocDeptNameList;

		docDeptNameArray.length = jDeptDocList.length;

		for ( i=0 ; i< jDeptDocList.length; i++){
			var item = jDeptDocList[i];
			docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")";
		}
	}
	if(document.mainform.doctor_name != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(docDeptNameArray);
			docAutoComp = new YAHOO.widget.AutoComplete('doctor_name', 'doc_dept_dropdown', dataSource);
			docAutoComp.maxResultsDisplayed = 10;
			docAutoComp.queryMatchContains = true;
			docAutoComp.allowBrowserAutocomplete = false;
			docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			docAutoComp.typeAhead = false;
			docAutoComp.useShadow = false;
			docAutoComp.minQueryLength = 0;
			docAutoComp.forceSelection = true;
			docAutoComp.textboxBlurEvent.subscribe(function() {
			var docName = document.mainform.doctor_name.value;
				if(docName == '') {
					document.mainform.babyAttendingDocIP.value = '';
				}
			});
			docAutoComp.itemSelectEvent.subscribe(function() {
				var dName = document.mainform.doctor_name.value;
				if(dName != '') {
					for ( var i=0 ; i< jDeptDocList.length; i++){
						if(dName == jDeptDocList[i]["doctor_name"]+" ("+jDeptDocList[i]["dept_name"]+")"){
							document.mainform.babyAttendingDocIP.value = jDeptDocList[i]["doctor_id"];
							setSelectedIndex(document.mainform.babyAttendingDeptIP, jDeptDocList[i]["dept_id"]);
							break;
						}
					}
				}else{
					document.mainform.babyAttendingDocIP.value = '';
					document.mainform.babyAttendingDeptIP.selectedIndex = 0;
				}
			});
		}
	}
}

function onChangeDepartment() {
	var deptId = document.mainform.babyAttendingDeptIP.value;
	document.mainform.doctor_name.value = '';
	document.mainform.babyAttendingDocIP.value = '';
	initDoctorDept(deptId);
}

function validate() {

	if(trim(document.mainform.firstName.value) == ""){
	    showMessage("js.registration.newbornregistration.babyname.required");
	    document.mainform.firstName.focus();
	    return false;
    }

	if(document.mainform.gender.options.selectedIndex==0){
		showMessage("js.registration.newbornregistration.gender.required");
		document.mainform.gender.focus();
		return false;
	}

	if(document.mainform.nationalityId != null && document.mainform.nationalityId.options.selectedIndex==0 &&
			nationalityLabel != null && nationalityLabel != '' && nationalityValidate == 'A') {
		showMessage("js.registration.newbornregistration.nationality.required");
		document.mainform.gender.focus();
		return false;
	}

	//var dateOfObj = document.mainform.dateOfBirth;
	var day = document.mainform.dobDay.value;
	var mon = document.mainform.dobMonth.value;
	var year = document.mainform.dobYear.value;
	if(day.length == 1) {
		day = "0" + day;
	}
	if(mon.length == 1) {
		mon = "0" + mon;
	}
	var dateStr = day + "-" + mon + "-" + year;
	var errorStr = validateDateFormat(dateStr);
	if (errorStr != null) {
		alert("Date of Birth is required !");
         document.getElementById('dobDay').style.borderColor = "red";
         document.getElementById('dobMonth').style.borderColor = "red";
         document.getElementById('dobYear').style.borderColor = "red";
		//alert(errorStr);
		dateInput.focus();
		return false;
	}
	dateStr = cleanDateStr(dateStr, 'past');
	errorStr = validateDateStr(dateStr, 'past');

	if (errorStr != null) {
		alert(errorStr);
		dateInput.focus();
		return false;
	}
	var timeOfObj = document.mainform.timeOfBirth;
	var dateOfBirth=document.mainform.dateOfBirth;

	/*if (!doValidateDateField(dateOfObj, 'past')) {
		dateOfObj.focus();
		return false;
	}*/

	if (!validateTime(timeOfObj)) {
		timeOfObj.focus();
		return false;
	}
	var motherRegDateObj = document.mainform.patientRegDate;
	var motherRegTimeObj = document.mainform.patientRegTime;
	document.mainform.dateOfBirth.value = dateStr;
	var babyRegDateTime = getDateTime(dateStr, timeOfObj.value);//getDateTimeFromField(dateOfObj, timeOfObj);
	var motherRegDateTime = getDateTimeFromField(motherRegDateObj, motherRegTimeObj);
	//alert(dateStr);
   // alert(dateOfBirth.value);
    //alert(babyRegDateTime);
	var diff = babyRegDateTime - motherRegDateTime;

	if (diff < 0) {
		showMessage("js.registration.newbornregistration.timeconditions");
		timeOfObj.focus();
		return false;
	}

	var bedtypeObj = document.mainform.bedtype;
	var wardObj = document.mainform.ward;
	if (bedtypeObj.value != '') {
		var valid = validateRequired(wardObj, getString("js.registration.newbornregistration.wardrequired"));
		if(!valid) {
			wardObj.focus();
			return false;
		}
	}

	document.mainform.submit();
	return true;
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
			var expDate = new Date(1937,3 ,14);
			var futureDate=new Date(2077,11,16);
			var entDate = new Date(year,month,day);

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

	var url = cpath + '/pages/registration/regUtils.do?_method=getHijriToGregorian&dobDay=' + day
		+ '&dobMonth=' + month + '&dobYear=' + year;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);

	if (ajaxobj) {
		if (ajaxobj.readyState == 4) {
			if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
				eval("var Date =" + ajaxobj.responseText);
				if (empty(Date.error)) {
					document.getElementById("dobDay").value = Date.day;
					document.getElementById("dobMonth").value = Date.month;
					document.getElementById("dobYear").value = Date.year;
					//getAge(true, null);
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

function checkBabyDOBYear() {
	if (empty(validateDOB())) return false;

	var strYear = document.mainform.dobYear.value;
    if (strYear.length < 4) {
        var year = convertTwoDigitYear(parseInt(strYear,10));
        if (year < 1900) {
            alert(getString("Invalid year:")+" " + year +
                ". "+getString("js.registration.patient.must.be.two.digit.or.four.digit.year.string"));
	        setTimeout("document.mainform.dobYear.focus()",0);
            return null;
        }
        // silently set the 4-digit year back to the textbox, and get the new value
        document.mainform.dobYear.value = year;
    }
    if(hijriPref == 'Y') {
		gregorianToHijri();
	}
    return true;
}
