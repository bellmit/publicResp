
function toggleCollapsiblePanel() {
	if (CollapsiblePanel1.isOpen()) CollapsiblePanel1.close();
	else CollapsiblePanel1.open();
}

function toggleVisitCollapsiblePanel() {
	if (VisitCollapsiblePanel1 != null) {
		if (VisitCollapsiblePanel1.isOpen()) VisitCollapsiblePanel1.close();
		else VisitCollapsiblePanel1.open();
	}
}

function openConsentUploadDocumentPopUp(mrNo,uploadLimit){
  window.uplodLimitInMb = uploadLimit;
  window.setUploadConsentDocMode('');
  window.routeParams = {
		patientId :mrNo,
		};
  window.openAddConsentDocument('SYS_HIE','HIE CONSENT');
}

function hidePaymentModeForRegisterNewSample() {
	var numPayments = getNumOfPayments();
	for (i=0; i<numPayments; i++){
		var paymentModeId = "paymentModeId"+i;
		
		$("#"+paymentModeId+" option[value='-8']").remove();
		$("#"+paymentModeId+" option[value='-6']").remove();
		$("#"+paymentModeId+" option[value='-7']").remove();
		$("#"+paymentModeId+" option[value='-9']").remove();
	}
}

// Gender
function salutationChange() {
	var title = getSelText(document.mainform.salutation);
	if (title != "" && document.mainform.salutation.value != "") {
		for(var i=0;i<salutationJSON.length;i++) {
			var item  = salutationJSON[i];
			if(title == item["salutation"]) {
				if (!empty(item["gender"])) {
					for(var k=0; k<document.mainform.patient_gender.options.length;k++) {
						if(document.mainform.patient_gender.options[k].value == item["gender"])
							document.mainform.patient_gender.selectedIndex = k;
					}
				}else {
					document.mainform.patient_gender.selectedIndex = 0;
				}
				break;
			}
		}
	}else {
		document.mainform.patient_gender.selectedIndex = 0;
	}
	if(gScreenId === 'ip_registration') {
  	  document.mainform.ward_id.value = '';
      document.mainform.bed_type.value='';
  }
}

function enableVipStatus() {
	var vip = document.mainform.vip_check;
	var vipStatus = vip.checked ? 'Y' : 'N';
	document.mainform.vip_status.value = vipStatus;
}

function setDeptAllowedGender(deptId) {
	if (deptId != '') {
		var dept = findInList(deptList, 'dept_id', deptId);
		document.mainform.dept_allowed_gender.value = dept.allowed_gender;
	}
}

function enableCaseFileAutoGen() {
	var no = document.getElementById('casefileNo').value;
	if (no == null || no == '') {
		document.mainform.oldRegAutoGenerate.disabled = false;
	}else{
		document.mainform.oldRegAutoGenerate.disabled = true;
	}
}

// Validate case file no.
function checkUniqueCasefileNo() {
	var no = document.getElementById('casefileNo').value;
	if (no == null || no == '') return false;
    var validate = function(response, code) {
    	var responsetext = eval(response);
        if (responsetext == "true") {
        	showMessage( "js.registration.patient.case.file.number.already.exists.string");
            document.mainform.casefileNo.value="";
            document.mainform.casefileNo.focus();
        }
	}
	if (no != null || no != '') {
        Ajax.get(cpath+"/pages/registration/regUtils.do?_method=checkCaseFileNo&casefileno="+no, validate);
	}
}

function enableOldmrno() {
	var autoGen = document.mainform.oldRegAutoGenerate;
	var readonly = document.mainform.casefileNo.readOnly;
	document.mainform.casefileNo.readOnly = !readonly;
}

// Validate old mrno
function checkUniqueOldMrno(){
	var no = document.getElementById('oldmrno').value;
	if (no == null || no == '') return false;
    var validate = function(response, code) {
	    document.mainform.previoushospimg.style.visibility="hidden";
    	var responsetext = eval(response);
        if (responsetext == "true") {
        	alert( oldRegNumFieldText + " "+getString("js.registration.patient.already.exists.string"));
            document.mainform.oldmrno.value="";
            document.mainform.oldmrno.focus();
        }
	}
	if (no != null || no != '') {
    	document.mainform.previoushospimg.style.visibility="visible";
        Ajax.get(cpath+"/pages/registration/regUtils.do?_method=checkOldMrno&oldno="+no, validate);
	}
}

function categoryChange() {
	if (document.mainform.patient_category_id != null) {
		if ((document.mainform.patient_category_id.value=="") && (document.mainform.category_expiry_date!=null)) {
			document.mainform.category_expiry_date.value = '';
			document.mainform.category_expiry_date.disabled = true;
		} else if (document.mainform.category_expiry_date!=null) {
			document.mainform.category_expiry_date.disabled = false;
		}
		checkPassportAsteriskRequired();
	}
}

function initAutocompletes(){
	initAutoArea("patient_area","area_dropdown", "area_id");
	initAutoCityStateCountry("pat_city_name","city_state_country_dropdown", "city_id");
}

formatGeoAutocompleteResult = function(oResultData, sQuery, sResultMatch) {
	var geoData = oResultData;
	var label = [];
	if (geoData.hasOwnProperty("area_name") && geoData["area_name"]) {
		label.push(geoData["area_name"]);
	}
	if (geoData.hasOwnProperty("city_name") && geoData["city_name"]) {
		label.push(geoData["city_name"]);
	}
	if (regPref && ((regPref.hasOwnProperty("enableDistrict") && regPref.enableDistrict == 'Y') || 
			(regPref.hasOwnProperty("enable_district") && regPref.enable_district == 'Y'))) {
		if (geoData.hasOwnProperty("district_name") && geoData["district_name"]) {
			label.push(geoData["district_name"]);
		}
	}
	if (geoData.hasOwnProperty("state_name") && geoData["state_name"]) {
		label.push(geoData["state_name"]);
	}
	if (geoData.hasOwnProperty("country_name") && geoData["country_name"]) {
		label.push(geoData["country_name"]);
	}
	return label.join(" - ");
	//return details;
}

function initAutoArea(patientareaname,areadropdown,areaid){
	var dataSource = new YAHOO.util.XHRDataSource(cpath + '/master/areas/lookup.json');
	dataSource.scriptQueryParam="filterText";
	dataSource.scriptQueryAppend = "page_size=10&sort_order=area_name&contains=true";
	dataSource.connMethodPost=false;
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
	resultsList : "dtoList",
	fields: [{key: "area_name"},
		{key: "area_id"},
		{key: "city_name"},
		{key: "city_id"},
		{key: "district_name"},
		{key: "district_id"},
		{key: "state_name"},
		{key: "state_id"},
		{key: "country_name"},
		{key: "country_id"}]
	};

	oAutoComp = new YAHOO.widget.AutoComplete(patientareaname,areadropdown,dataSource);

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 10;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 1;
	oAutoComp.forceSelection = regPref.allowAutoEntryOfArea == 'Y' ? false : true;
	oAutoComp.resultTypeList= false;
	//oAutoComp._bItemSelected = true;

	oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var record = elItem[2];
		YAHOO.util.Dom.get('state_id').value = record.state_id;
		YAHOO.util.Dom.get('country_id').value = record.country_id;
		if(regPref.enableDistrict == 'Y') {
			if(document.getElementById("districtlbl") != null) {
				document.getElementById("districtlbl").textContent = record.district_name;
			}
			if(document.getElementById("district_id") != null) {
				YAHOO.util.Dom.get('district_id').value = record.district_id;
			}
		}
		document.getElementById("statelbl").textContent = record.state_name;
		document.getElementById("countrylbl").textContent = record.country_name;
		YAHOO.util.Dom.get('pat_city_name').value = record.city_name;
		YAHOO.util.Dom.get('city_id').value = record.city_id;
		YAHOO.util.Dom.get('patient_area').value = record.area_name;
		YAHOO.util.Dom.get('area_id').value = record.area_id;
	});
	oAutoComp.selectionEnforceEvent.subscribe(function() {
		YAHOO.util.Dom.get(areaid).value = '';
	});
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp.formatResult = formatGeoAutocompleteResult;
	return oAutoComp;
}


function initAutoCityStateCountry(patientcityname,citydropdown,patientcityid) {

	var dataSource = new YAHOO.util.XHRDataSource(cpath + '/master/cities/lookup.json');
	dataSource.scriptQueryParam="filterText";
	dataSource.scriptQueryAppend = "page_size=10&sort_order=city_name&contains=true";
	dataSource.connMethodPost=false;
	dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	dataSource.responseSchema = {
		resultsList : "dtoList",
		fields: [{key: "city_name"},
			{key: "city_id"},
			{key: "district_id"},
			{key: "district_name"},
			{key: "state_name"},
			{key: "state_id"},
			{key: "country_name"},
			{key: "country_id"}]
		};

	oAutoComp = new YAHOO.widget.AutoComplete(patientcityname, citydropdown, dataSource);

	oAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp.maxResultsDisplayed = 10;
	oAutoComp.allowBrowserAutocomplete = false;
	oAutoComp.typeAhead = false;
	oAutoComp.useShadow = false;
	oAutoComp.minQueryLength = 1;
	oAutoComp.forceSelection = true;
	oAutoComp.resultTypeList= false;
	//oAutoComp._bItemSelected = true;

	oAutoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var record = elItem[2];
		YAHOO.util.Dom.get(patientcityname).value = record.city_name;
		YAHOO.util.Dom.get(patientcityid).value = record.city_id;
		document.getElementById("statelbl").textContent = record.state_name;
		YAHOO.util.Dom.get('state_id').value = record.state_id;
		YAHOO.util.Dom.get('country_id').value = record.country_id;
		document.getElementById("countrylbl").textContent = record.country_name;
		if(regPref.enableDistrict == 'Y') {
			if(document.getElementById("districtlbl") != null) {
				document.getElementById("districtlbl").textContent = record.district_name;
			}
			if(document.getElementById("district_id") != null) {
				YAHOO.util.Dom.get('district_id').value = record.district_id;
			}	
		}
		YAHOO.util.Dom.get('patient_area').value = '';
		YAHOO.util.Dom.get('area_id').value = '';
	});
	oAutoComp.selectionEnforceEvent.subscribe(function() {
		YAHOO.util.Dom.get('city_id').value = '';
		document.getElementById("statelbl").textContent = '';
		if(regPref.enableDistrict == 'Y') {
			document.getElementById("districtlbl").textContent = '';
			YAHOO.util.Dom.get('district_id').value = '';
		}
		YAHOO.util.Dom.get('state_id').value = '';
		YAHOO.util.Dom.get('country_id').value = '';
		document.getElementById("countrylbl").textContent = '';
	});
	oAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp.formatResult = formatGeoAutocompleteResult;
	return oAutoComp;

}

var wardsList;
// Function used in EditPatientVisit.jsp
function onChangeBedType(bedObj, wardObj) {
	if (bedObj != null) {
		var bedtyp = bedObj.value;
		if (bedtyp != '') {
			var ajaxobj = newXMLHttpRequest();
			var url = cpath+'/pages/registration/regUtils.do?_method=getWardnamesForBedType&selectedbedtype='+encodeURIComponent(bedtyp);
			var freebeds = 0;
			wardObj.length=1;
			wardObj.options[wardObj.length-1].text = getString("js.common.commonselectbox.defaultText");
			wardObj.options[wardObj.length-1].value = "";
			var ajaxobj = newXMLHttpRequest();
			ajaxobj.open("POST",url.toString(), false);
			ajaxobj.send(null);
			if (ajaxobj) {
				if (ajaxobj.readyState == 4) {
					if ( (ajaxobj.status == 200) && (ajaxobj.responseText!=null) ) {
						eval("var wards =" + ajaxobj.responseText);
						if (wards != null && wards != '') {
							var len = wards.length;
							for(var i=0;i<len;i++) {
								var record = wards[i];
								var bedNameObj= null;
								wardObj.length = wardObj.length + 1;
								bedNameObj= record == null? 0:  record.freebeds == null ||  record.freebeds== null ||  record.freebeds == ''? '0' :  (record.freebeds).toString();
								wardObj.options[wardObj.length-1].text = record.ward_name+ " ("+bedNameObj+" beds)";
								wardObj.options[wardObj.length-1].value = record.ward_no;
								freebeds = freebeds + record.freebeds == null || record.freebeds == ''? 0 : record.freebeds;
							}
							wardsList = wards;
						}
						if(freebeds == 0) {
							showMessage("js.registration.patient.no.free.beds.to.allocate.string");
						}
					}
				}
			}
		}else{
			bedObj.selectedIndex = 0;
			wardObj.selectedIndex = 0;
			wardObj.length = 1;
		}
	}
}

function onWardChang(){
 if (gScreenId == 'op_ip_conversion' || gScreenId == 'edit_visit_details'){
  if(wardsList !== undefined && wardsList.length > 0){
     var patientgender = document.mainform.patient_gender.value;
     var ward_id = document.mainform.ward_id.value;
     var filteredList;
     if(ward_id !== undefined || ward_id !== null){
       filteredList = wardsList.filter(ward => document.mainform.ward_id.value === ward.ward_no);
       if(filteredList !== undefined && filteredList.length > 0){
       if(filteredList[0].allowed_gender !== 'ALL' && filteredList[0].allowed_gender !== patientgender && !(patientgender === 'C' || patientgender === 'O' || patientgender === 'N')) {
         var replaceWord;
         if(filteredList[0].allowed_gender === 'F'){
            replaceWord = 'Female';
         }
         if(filteredList[0].allowed_gender === 'M'){
           replaceWord = 'Male';
         }
         alert(getString('js.registration.patient.not.valid.ward.for.gender', replaceWord));
         document.mainform.ward_id.value ='';
         return false;
       }
       }
     }
     else if(wardsList[0].allowed_gender !== 'ALL' && wardsList[0].allowed_gender !== patientgender && !(patientgender === 'C' || patientgender === 'O' || patientgender === 'N')) {
       var replaceWord;
       if(wardsList[0].allowed_gender === 'F'){
          replaceWord = 'Female';
       }
       if(wardsList[0].allowed_gender === 'M'){
         replaceWord = 'Male';
       }
       alert(getString('js.registration.patient.not.valid.ward.for.gender', replaceWord));
       document.mainform.ward_id.value ='';
       return false;
     }
    }
  }
  return true;
}

function referalDoctorAutoComplete(refId, refName, refContainer){
	var referralUrl = cpath+"/master/referraldoctors/lookup.json?page_size=10&sort_order=referal_name&contains=true";
	var dataSource = new YAHOO.util.DataSource(referralUrl);
	dataSource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
	dataSource.responseSchema = {
			resultsList : "dtoList",
			fields: [{key: "referal_name"},
			         {key: "referal_no"},
			         {key: "clinician_id"},
			         {key: "area_name"},
					 {key: "area_id"},
					 {key: "city_name"},
					 {key: "city_id"},
					 {key: "district_name"},
					 {key: "district_id"},
					 {key: "state_name"},
					 {key: "state_id"},
					 {key: "country_name"},
					 {key: "country_id"}]
			};

	var autoComp = new YAHOO.widget.AutoComplete(refName, refContainer ,dataSource);

	autoComp.generateRequest = function(sQuery) {
		var areaFilter = '';
		var cityFilter = '';
		var districtFilter = '';
		var stateFilter = '';
		if(regPref.showReferralDoctorFilter == 'Y') {
			areaFilter = YAHOO.util.Dom.get("referral_filter_area_id").value;
			cityFilter = YAHOO.util.Dom.get("referral_filter_city_id").value;
			if(regPref.enableDistrict == 'Y') {
				districtFilter = YAHOO.util.Dom.get("referral_filter_district_id").value;
			}
			stateFilter = YAHOO.util.Dom.get("referral_filter_state_id").value;
		}

		return "&filterText="+sQuery+"&center_id="+centerId+
				"&area_id="+areaFilter+"&city_id="+cityFilter+
				"&district_id="+districtFilter+"&state_id="+stateFilter;
	};
 	autoComp.prehighlightClassName = "yui-ac-prehighlight";
	autoComp.typeAhead = false;
	autoComp.useShadow = false;
	autoComp.allowBrowserAutocomplete = false;
	autoComp.minQueryLength = 0;
	autoComp.maxResultsDisplayed = 20;
	autoComp.forceSelection = true;
	autoComp.resultTypeList= false;

	autoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
		var referralDetails = oResultData;
		var details = '';
		details = referralDetails.referal_name + (referralDetails.clinician_id ? "("+ referralDetails.clinician_id+")" : '');
		if(regPref.showReferralDoctorFilter == 'Y') {
			details = details +
						(referralDetails.area_name != null ? ' - '+referralDetails.area_name : '')+
						(referralDetails.city_name != null ? ' - '+referralDetails.city_name : '')+
						(regPref.enableDistrict == 'Y' && referralDetails.district_name != null ? ' - '+referralDetails.district_name : '')+
						(referralDetails.state_name != null ? ' - '+referralDetails.state_name : '');
		}
		return details;
	}
 	autoComp.textboxBlurEvent.subscribe(function() {
		var referralName = YAHOO.util.Dom.get(refName).value;
		if(referralName == '') {
			YAHOO.util.Dom.get(refId).value = '';
		}
	});
 	autoComp.itemSelectEvent.subscribe(function(oSelf, elItem) {
		var referralName = YAHOO.util.Dom.get(refName).value;
		if(referralName != '') {
			var record = elItem[2];
			YAHOO.util.Dom.get(refId).value = record.referal_no ? record.referal_no : '';
			YAHOO.util.Dom.get(refName).value = record.referal_name ? record.referal_name : '';
			if (document.mainform.clinician_id!=null) {
				document.mainform.clinician_id.value = record.clinician_id;
				document.getElementById('clinician_label').innerHTML = record.clinician_id;
			}
			if(regPref.showReferralDoctorFilter == 'Y') {
				YAHOO.util.Dom.get('referral_filter_area_id').value = record.area_id;
				YAHOO.util.Dom.get('referral_filter_area').value = record.area_name;
				YAHOO.util.Dom.get('referral_filter_city_id').value = record.city_id;
				YAHOO.util.Dom.get('referral_filter_city').value = record.city_name;
				if(enableDistrict == 'Y') {
					YAHOO.util.Dom.get('referral_filter_district_id').value = record.district_id;
					YAHOO.util.Dom.get('referral_filter_district').value = record.district_name;
				}
				YAHOO.util.Dom.get('referral_filter_state_id').value = record.state_id;
				YAHOO.util.Dom.get('referral_filter_state').value = record.state_name;
			}
		}else{
			YAHOO.util.Dom.get(refId).value = '';
		}
	});
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
			if (item["doctor_license_number"]!=null && item["doctor_license_number"]!="") {
				docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")"+"("+item["doctor_license_number"]+")";
			} else {
				docDeptNameArray[i] = item["doctor_name"]+" ("+item["dept_name"]+")";
			}
		}
	}
	if(document.mainform.doctor_name != null) {
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(docDeptNameArray);
			docAutoComp = new YAHOO.widget.AutoComplete('doctor_name', 'doc_dept_dropdown', dataSource);
			docAutoComp.maxResultsDisplayed = 10;
			docAutoComp.queryMatchContains = true;
			docAutoComp.allowBrowserAutocomplete = false;
			docAutoComp.formatResult = Insta.autoHighlight;
			docAutoComp.prehighlightClassName = "yui-ac-prehighlight";
			docAutoComp.typeAhead = false;
			docAutoComp.useShadow = false;
			docAutoComp.minQueryLength = 0;
			docAutoComp.forceSelection = true;
			docAutoComp._bItemSelected = true;
			docAutoComp.textboxBlurEvent.subscribe(function() {
			var docName = document.mainform.doctor_name.value;
				if(docName == '') {
					document.mainform.doctor_name.removeAttribute("title");
					document.mainform.doctor.value = '';
					if (document.getElementById("docConsultationFees") != null) {
						setDocRevistCharge(document.mainform.doctor.value);
						document.mainform.consFees.value = 0;
						document.mainform.opdocchrg.value = 0;
						document.getElementById("docConsultationFees").textContent = '';
						estimateTotalAmount();
					}else if (document.getElementById("hasOpType") != null && !document.getElementById("hasOpType").disabled) {
						getPatientDoctorVisits(document.mainform.doctor.value);
					}
				}else {
					document.mainform.doctor_name.title = docName;
				}
			});
			docAutoComp.itemSelectEvent.subscribe(function() {
				loadPrvPrescripitons = false;
				var dName = document.mainform.doctor_name.value;
				if(dName != '') {
					for ( var i=0 ; i< jDeptDocList.length; i++){
						if (jDeptDocList[i]["doctor_license_number"]!=null && jDeptDocList[i]["doctor_license_number"]!="") {
							if(dName == jDeptDocList[i]["doctor_name"]+" ("+jDeptDocList[i]["dept_name"]+")"+"("+jDeptDocList[i]["doctor_license_number"]+")"){
								document.mainform.doctor.value = jDeptDocList[i]["doctor_id"];
								if (document.mainform.dept_name.value != jDeptDocList[i]["dept_id"]) {
									if (document.mainform.unit_id != null)
										document.mainform.unit_id.selectedIndex = 0;
										setSelectedIndex(document.mainform.dept_name, jDeptDocList[i]["dept_id"]);
										setDeptAllowedGender(jDeptDocList[i]["dept_id"]);
								}
								break;
							}
						} else {
							if(dName == jDeptDocList[i]["doctor_name"]+" ("+jDeptDocList[i]["dept_name"]+")"){
								document.mainform.doctor.value = jDeptDocList[i]["doctor_id"];
								if (document.mainform.dept_name.value != jDeptDocList[i]["dept_id"]) {
									if (document.mainform.unit_id != null)
										document.mainform.unit_id.selectedIndex = 0;
										setSelectedIndex(document.mainform.dept_name, jDeptDocList[i]["dept_id"]);
										setDeptAllowedGender(jDeptDocList[i]["dept_id"]);
								}
								break;
							}
						}
					}

					if (document.getElementById("docConsultationFees") != null) {
						gSelectedDoctorName = document.mainform.doctor_name.value;
						gSelectedDoctorId = document.mainform.doctor.value;
						setDocRevistCharge(document.mainform.doctor.value);
						getDoctorCharge();
						changeVisitType();
						loadPreviousUnOrderedPrescriptions();
						if (category != null && (category != 'SNP' || !empty(scheduleName)))
							loadSchedulerOrders();
						estimateTotalAmount();
					}else if (document.getElementById("hasOpType") != null && !document.getElementById("hasOpType").disabled) {
						if (gDoctorId != document.mainform.doctor.value) {
							getPatientDoctorVisits(document.mainform.doctor.value);
						}
					}

					if (document.getElementById("docConsultationFees") != null
						&& !empty(visitTypeDependence) && ((visitTypeDependence == 'D' && !empty(gPreviousVisitDoctor) && gPreviousVisitDoctor != document.mainform.doctor.value)
							 || (visitTypeDependence == 'S' && !empty(gPreviousVisitDoctor) && gPreviousVisitDept != dept))
						&& document.mainform.op_type != null
						&& (document.mainform.op_type.value == "F" || document.mainform.op_type.value == "D")) {
						var mrno = document.mrnoform.mrno.value;
						var mainVisitId = document.mainform.main_visit_id.value;
						//Before clearing the registration details set globally the selected doctor
						var doctorId = document.mainform.doctor.value;
						var doctorName = document.mainform.doctor_name.value;
						clearRegDetails();
						gSelectedDoctorName = doctorName;
						gSelectedDoctorId = doctorId;
						document.mrnoform.mrno.value = mrno;
						document.mainform.main_visit_id.value = mainVisitId;
						isDoctorChange = true;
						getRegDetails();
					}
					document.mainform.doctor_name.title = dName;
				}else{
					document.mainform.doctor.value = '';
					document.mainform.doctor_name.removeAttribute("title");
					document.mainform.dept_name.selectedIndex = 0;
					if (document.mainform.unit_id != null)
						document.mainform.unit_id.selectedIndex = 0;
					if (document.getElementById("docConsultationFees") != null) {
						gSelectedDoctorName = document.mainform.doctor_name.value;
						gSelectedDoctorId = document.mainform.doctor.value;
						setDocRevistCharge(document.mainform.doctor.value);
						document.mainform.consFees.value = 0;
						document.mainform.opdocchrg.value = 0;
						document.getElementById("docConsultationFees").textContent = '';
						estimateTotalAmount();
					}else if (document.getElementById("hasOpType") != null && !document.getElementById("hasOpType").disabled) {
						getPatientDoctorVisits(document.mainform.doctor.value);
					}
				}
				loadPrvPrescripitons = true;
				//loadPreviousUnOrderedPrescriptions() is not neededd for scheduler patients but loadSchedulerOrders()
				//by this loadSchedulerOrders() is called,so make sure no autofill by loadPreviousUnOrderedPrescriptions()
				setPatientComplaint();
			});
		}
	}
}

function setDefaultCity() {
	document.mainform.patient_city.value = defaultCity;
	document.mainform.patient_state.value = defaultState;
	document.mainform.country.value = defaultCountry;

	document.mainform.pat_city_name.value = defaultCityName;
	document.getElementById("statelbl").textContent = defaultStateName;
	document.getElementById("countrylbl").textContent = defaultCountryName;
}

function loadDepartmentUnit(unitSelect, deptId) {
	var deptUnitList = filterList(unitList, "dept_id", deptId);
	if(deptUnitSetting != null && deptUnitSetting == 'M') {
		var index = 1;
	 	for (var i=0;i<deptUnitList.length;i++) {
			var item = deptUnitList[i];
			unitSelect.length = parseFloat(index)+parseFloat(1);
			unitSelect.options[index].text = item.unit_name;
			unitSelect.options[index].value = item.unit_id;
			index++;
	 	}
	 }else if(deptUnitSetting != null && deptUnitSetting == 'R') {
	 	var index = 1;
	 	var url = cpath+'/pages/registration/regUtils.do?_method=getDeptUnit&dept_id='+deptId;
		var ajaxobj = newXMLHttpRequest();
		ajaxobj.open("POST",url.toString(), false);
		ajaxobj.send(null);
		if (ajaxobj) {
			if (ajaxobj.readyState == 4) {
				if ( (ajaxobj.status == 200) && (ajaxobj.responseText!=null) ) {
					eval("var unitid = " + ajaxobj.responseText);
					if(unitid != null && unitid != '') {
						for (var i=0;i<deptUnitList.length;i++) {
							var item = deptUnitList[i];
							if (unitid == item.unit_id) {
								unitSelect.length = parseFloat(index)+parseFloat(1);
								unitSelect.options[index].text = item.unit_name;
								unitSelect.options[index].value = item.unit_id;
								index++;
							}
					 	}
				 	} else {
						unitSelect.length = parseFloat(1);
			 		}
				}
			}
		}
	}
}

/*
 * Check if user has entered any DOB. If not, we can use the age.
 * If the user has entered the DOB, that must be used.
 */
function isDOBEntered() {
    var strDay = document.mainform.dobDay.value;
    var strMonth = document.mainform.dobMonth.value;
    var strYear = document.mainform.dobYear.value;

    if ( (strDay == 'DD') && (strMonth == 'MM') && (strYear == 'YY') ) {
        return false;
    }
    if ( (strDay == '') && (strMonth == '') && (strYear == '') ) {
        return false;
    }
    return true;
}

//date of birth validation
function validateDOB() {
     var strDay = document.mainform.dobDay.value;
     var strMonth = document.mainform.dobMonth.value;
     var strYear = document.mainform.dobYear.value;

	if (strDay == "") {
		showMessage("js.registration.patient.date.required");
		setTimeout(document.mainform.dobDay.focus(),0);
		return null;
	}

	if (strMonth == "") {
		showMessage("js.registration.patient.month.required");
		setTimeout(document.mainform.dobMonth.focus(),0);
		return null;
	}

	if (strYear == "") {
		showMessage("js.registration.patient.year.required");
		return null;
	}

    if (!isInteger(strYear)) {
        showMessage("js.registration.patient.invalid.year.not.an.integer.string");
        return null;
    }
    if (!isInteger(strMonth)) {
        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
	    setTimeout(document.mainform.dobMonth.focus(),0);
        return null;
    }
    if (!isInteger(strDay)) {
        showMessage("js.registration.patient.invalid.month.not.an.integer.string");
	    setTimeout(document.mainform.dobDay.focus(),0);
        return null;
    }

    if (parseInt(strDay) > 31) {
        showMessage("js.registration.patient.enter.correct.date.string");
	    setTimeout(document.mainform.dobDay.focus(),0);
        return null;
    }

    if (parseInt(strMonth) > 12) {
        showMessage("js.registration.patient.enter.correct.month.string");
	    setTimeout(document.mainform.dobMonth.focus(),0);
        return null;
    }

    if (strYear.length < 4) {
        var year = convertTwoDigitYear(parseInt(strYear,10));
        if (year < 1900) {
            alert(getString("Invalid year:")+" " + year +
                ". "+getString("js.registration.patient.must.be.two.digit.or.four.digit.year.string"));
	        setTimeout(document.mainform.dobYear.focus(),0);
            return null;
        }
        // silently set the 4-digit year back to the textbox, and get the new value
        document.mainform.dobYear.value = year;
        strYear = year.toString();
    }

    // check if a conversion gives us back the same numbers, or else, correct it
    // For example, 31 Sep will be converted to 01 Oct. We need to warn the user.
    var dob = getDateFromDDMMYY(strDay, strMonth, strYear);
    if (!dob) {
        showMessage("js.registration.patient.invalid.date.specification.string");
	    setTimeout(document.mainform.dobDay.focus(),0);
        return null;
    }

    if (dob > (new Date()) ) {
	    showMessage("js.registration.patient.date.of.birth.and.current.date.check.string");
	    setTimeout(document.mainform.dobDay.focus(),0);
		return null;
    }

    var newDate = dob.getDate();
    var newMonth = dob.getMonth();
    var newYear = dob.getFullYear();

    if ( (parseInt(strDay,10) != newDate) || (parseInt(strMonth,10) != newMonth + 1) ||
         (parseInt(strYear,10) != newYear) )  {

        // clear the new value in the text boxes and warn the user
        document.mainform.dobDay.value = "";
        //document.mainform.dobMonth.value = "";
        //document.mainform.dobYear.value = "";
        showMessage("js.registration.patient.valid.date.check.for.current.month.year.combination.string");
	    setTimeout(document.mainform.dobDay.focus(),0);
        return null;
    }

    return dob;
}

function getDateFromDDMMYY(strDay, strMonth, strYear) {
    if ( !(isInteger(strDay) && isInteger(strMonth) && isInteger(strYear)) ) {
        return null;
    }
    var year = parseInt(strYear, 10);
    var month = parseInt(strMonth, 10);
    var day = parseInt(strDay, 10);
    if (year < 100) {
        year = convertTwoDigitYear(year);
    }

    var dob = new Date(year, month-1, day);
    //alert("For year: " + year + " month " + month + " day " + day + " Got date: " + dob);
    return dob;
}

function convertTwoDigitYear(year) {
    // convert 2 digit years intelligently
    var now = new Date();
    var century = now.getFullYear();
    century = Math.floor(century/100) * 100;
    // say this is 2008. Century gives 2000
    if (year > now.getFullYear() - century) {
        // more than 08, (09 - 99), use last century (eg 1909 1999)
        year += century -100;
    } else {
        // else (eg 05), make it this century (2005)
        year += century;
    }
    return year;
}


function dissableAge(){
	document.mainform.age.disabled = true;
	document.mainform.ageIn.disabled = true;
}

function enableAge(){
	if(allowAgeEntry === 'Y'){
		document.mainform.age.disabled = false;
		document.mainform.ageIn.disabled = false;
	}
	document.mainform.age.value = getString("js.registration.patient.show.age.text");
}

function changeDobFields(value){
	document.mainform.dobDay.value =  getString("js.registration.patient.show.dd.text");
	document.mainform.dobMonth.value = getString("js.registration.patient.show.mm.text");
	document.mainform.dobYear.value =  getString("js.registration.patient.show.yy.text");

	document.mainform.dobDay.disabled  = value;
	document.mainform.dobMonth.disabled  = value;
	document.mainform.dobYear.disabled  = value;

	if(document.mainform.dobHDay) {
		document.mainform.dobHDay.disabled  = value;
		document.mainform.dobHDay.value = getString("js.registration.patient.show.dd.text");
	}
	if(document.mainform.dobHMonth) {
		document.mainform.dobHMonth.disabled  = value;
		document.mainform.dobHMonth.value = getString("js.registration.patient.show.mm.text");
	}
	if(document.mainform.dobHYear) {
		document.mainform.dobHYear.disabled  = value;
		document.mainform.dobHYear.value = getString("js.registration.patient.show.yy.text");
	}
}

function dissableDobAndHijriDob(){
	changeDobFields(true);
}

function enableDobAndHijriDob(){
	changeDobFields(false);
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

function getAge(validate, validatedDob) {
	var dob = null;
	if (validate) {
	    if (!isDOBEntered()) {
	        return;
	    }
    	dob = validateDOB();
    } else {
    	dob = validatedDob;
    }
    if (dob) {
    	try{
    		moment(); // check if moment is present in page or not.
    		var {age, ageUnit : ageIn} = dateOfBirthToAge(dob);
    	}catch(e){
    		var now = new Date();
	        var oneDay = 1000 * 60 * 60 * 24 ;
	        var age = (now - dob) / (oneDay);
	        var ageIn = null;
	        if (age < 31) {
	        	age = Math.floor(age);
	        	ageIn = 'D';
	        } else if (age < 730) {
	        	age = age / 30.43;
	        	age = Math.floor(age);
	        	ageIn = 'M';
	        } else {
	        	age = age / 365.25;
	        	age = Math.floor(age);
	        	ageIn = 'Y';
	        }
    	}

        document.mainform.age.value = age;
        document.mainform.ageIn.value = ageIn;
        if (age >= 1000) {
        	showMessage("js.registration.patient.valid.age.check.string");
        	document.mainform.age.value = "";
        	setTimeout("document.mainform.dobDay.focus()", 0);
        	return false;
        }else{
        	dissableAge();
        }
    }
    return true;
}

function customVisitFieldValidation(visitType, customField) {
	for (var i=1; i<3; i++) {
		var label = eval("visit_custom_list" + i + "_name");
		var labelValidate = eval("visit_custom_list" + i + "_validate");
		var labelObj = eval("document.mainform.visit_custom_list"+i);

		if (customField == label && labelObj != null && labelObj.value == '') {
			if (labelValidate == 'A' || (labelValidate == 'I' && visitType == 'I') ||
					(labelValidate == 'O' && visitType == 'O')) {
				alert(label+ " "+getString("js.registration.patient.is.required.string"));
				return "visit_custom_list"+i;
			}
		}
	}

	for (var i=1; i<10; i++) {
		var label = eval("visit_custom_field" + i + "_name");
		var labelValidate = eval("visit_custom_field" + i + "_validate");
		var labelObj = eval("document.mainform.visit_custom_field"+i);

		if (customField == label && labelObj != null && labelObj.value == '') {
			if (labelValidate == 'A' || (labelValidate == 'I' && visitType == 'I') ||
					(labelValidate == 'O' && visitType == 'O')) {
				alert(label+ " "+getString("js.registration.patient.is.required.string"));
				return "visit_custom_field"+i;
			}
		}

		if(customField == label && labelObj != null && !empty(labelObj.value)) {
			if (i>3 && i<7) {
				if(!doValidateDateField(labelObj))
					return "visit_custom_field"+i;
			} else if (i > 6) {
				if (!isValidNumber(labelObj,"Y",label))
					return "visit_custom_field"+i;

				if(!validateAmount(labelObj,label+" "+getString("js.registration.patient.allowed.only.eight.digit.number")))
					return "visit_custom_field"+i;
			}
		}
	}
}

var visitFieldsList = [];

function getVisitCustomFieldList() {
	var j = 0;
	if (regPref == null) return;
	for (var i=1; i<3; i++) {
		eval("visit_custom_list"+i+"_name=regPref.visit_custom_list"+i+"_name");
		eval("visit_custom_list"+i+"_validate=regPref.visit_custom_list"+i+"_validate");

		var label = eval("visit_custom_list" + i + "_name");

		if (!empty(label)) {
			j++;
			visitFieldsList.length = j;
			visitFieldsList[j-1] = label;
		}
		// HMS-20269 : User is able to edit Additional Visit Information even if 'Edit Regn. Fields(Custom Fields)= No'
		if (allowFieldEdit == 'A') {
			var customListFieldObj = eval("document.mainform.visit_custom_list"+i+"");
			if (customListFieldObj) customListFieldObj.disabled = false;
		}else{
			var customListFieldObj = eval("document.mainform.visit_custom_list"+i+"");
			if (customListFieldObj) customListFieldObj.disabled = true;
		}
	}

	for (var i=1; i<10; i++) {
		eval("visit_custom_field"+i+"_name=regPref.visit_custom_field"+i+"_name");
		eval("visit_custom_field"+i+"_validate=regPref.visit_custom_field"+i+"_validate");

		var label = eval("visit_custom_field" + i + "_name");

		if (!empty(label)) {
			j++;
			visitFieldsList.length = j;
			visitFieldsList[j-1] = label;
		}
		// HMS-20269 : User is able to edit Additional Visit Information even if 'Edit Regn. Fields(Custom Fields)= No'
		if(allowFieldEdit == 'A'){
			var customFieldObj = eval("document.mainform.visit_custom_field"+i+"");
			if (customFieldObj) customFieldObj.readOnly = false;
		}else{
			var customFieldObj = eval("document.mainform.visit_custom_field"+i+"");
			if (customFieldObj) customFieldObj.readOnly = true;
		}
	}
}

var default_gp_first_consultation = null, default_gp_revisit_consultation = null,
	default_sp_first_consultation = null, default_sp_revisit_consultation = null,
	govtIDType = null, govtID = null,

	passportNoLabel = null, passportValidityLabel = null, passportIssueCountryLabel = null, visaValidityLabel = null,
	passportNoValidate = null, passportValidityValidate = null, passportIssueCountryValidate = null, visaValidityValidate = null,
	familyIDLabel = null, familyIDValidate = null, nationalityLabel = null, nationalityValidate = null ;

function getCustomFieldList() {

	for (var i=1; i<10; i++) {
		eval("custom_list"+i+"_name=regPref.custom_list"+i+"_name");
		eval("custom_list"+i+"_validate=regPref.custom_list"+i+"_validate");
	}

	for (var i=1; i<20; i++) {
		eval("custom_field"+i+"_label=regPref.custom_field"+i+"_label");
		eval("custom_field"+i+"_validate=regPref.custom_field"+i+"_validate");
	}

	default_gp_first_consultation	= healthAuthoPref.default_gp_first_consultation;
	default_gp_revisit_consultation	= healthAuthoPref.default_gp_revisit_consultation;
	default_sp_first_consultation	= healthAuthoPref.default_sp_first_consultation;
	default_sp_revisit_consultation	= healthAuthoPref.default_sp_revisit_consultation;

	govtIDType	= regPref.government_identifier_type_label;
	govtID		= regPref.government_identifier_label;

	passportNoLabel				= regPref.passport_no;
	passportValidityLabel		= regPref.passport_validity;
	passportIssueCountryLabel	= regPref.passport_issue_country;
	visaValidityLabel			= regPref.visa_validity;

	passportNoValidate			= regPref.passport_no_validate;
	passportValidityValidate	= regPref.passport_validity_validate;
	passportIssueCountryValidate= regPref.passport_issue_country_validate;
	visaValidityValidate		= regPref.visa_validity_validate;

	familyIDLabel		= regPref.family_id;
	familyIDValidate	= regPref.family_id_validate;

	nationalityLabel = regPref.nationality;
	nationalityValidate = regPref.nationality_validate;
}

var mainPagefieldsList = [];
var mainPageVisitfieldsList = [];
var dialogFieldsList = [];
var dialogVisitFieldsList = [];

function filterCustomFields() {
	var j = 0;
	var k = 0;
	for (var i=0;i<customFieldList.length;i++) {
		if (!empty(customFieldList[i].display) && !empty(customFieldList[i].label)) {
			if (customFieldList[i].display == 'M') {
				j++;
				mainPagefieldsList.length = j;
				mainPagefieldsList[j-1] = customFieldList[i].label;
			}else {
				k++;
				dialogFieldsList.length = k;
				dialogFieldsList[k-1] =  customFieldList[i].label;
			}
		}
	}
}

function filterVisitCustomFields() {
	var j = 0;
	var k = 0;
	for (var i=0;i<visitCustomFieldList.length;i++) {
		if (!empty(visitCustomFieldList[i].display) && !empty(visitCustomFieldList[i].label)) {
			if (visitCustomFieldList[i].display == 'M') {
				j++;
				mainPageVisitfieldsList.length = j;
				mainPageVisitfieldsList[j-1] = visitCustomFieldList[i].label;
			}else {
				k++;
				dialogVisitFieldsList.length = k;
				dialogVisitFieldsList[k-1] =  visitCustomFieldList[i].label;
			}
		}
	}
}

function enableCustomLists() {

	for (var i=0; i<10; i++) {
		var customListFieldObj = eval("document.mainform.custom_list"+i+"_value");
		if (customListFieldObj) customListFieldObj.disabled = false;
	}
}

function markCustomFieldsReadonly(notNewRegistration) {
	if ((allowFieldEdit == 'A' && notNewRegistration) || !notNewRegistration) {

		for (var i=1; i<20; i++) {
			var customFieldObj = eval("document.mainform.custom_field"+i);
			if (customFieldObj) customFieldObj.readOnly = false;
		}

		for (var i=1; i<10; i++) {
			var customListFieldObj = eval("document.mainform.custom_list"+i+"_value");
			if (customListFieldObj) customListFieldObj.disabled = false;
		}

		if (document.mainform.passport_no) document.mainform.passport_no.readOnly = false;
		if (document.mainform.passport_validity) document.mainform.passport_validity.readOnly = false;
		if (document.mainform.passport_issue_country) document.mainform.passport_issue_country.readOnly = false;
		if (document.mainform.visa_validity) document.mainform.visa_validity.readOnly = false;

		if (document.mainform.family_id) document.mainform.family_id.readOnly = false;
		if (document.mainform.nationality_id) document.mainform.nationality_id.readOnly = false;

	}else {

		for (var i=1; i<20; i++) {
			var customFieldObj = eval("document.mainform.custom_field"+i);
			if (customFieldObj) customFieldObj.readOnly = true;
		}

		for (var i=1; i<10; i++) {
			var customListFieldObj = eval("document.mainform.custom_list"+i+"_value");
			if (customListFieldObj) customListFieldObj.disabled = true;
		}

		if (document.mainform.passport_no) document.mainform.passport_no.readOnly = true;
		if (document.mainform.passport_validity) document.mainform.passport_validity.readOnly = true;
		if (document.mainform.passport_issue_country) document.mainform.passport_issue_country.readOnly = true;
		if (document.mainform.visa_validity) document.mainform.visa_validity.readOnly = true;

		if (document.mainform.family_id) document.mainform.family_id.readOnly = true;
		if (document.mainform.nationality_id) document.mainform.nationality_id.readOnly = true;
	}
}

function customFieldValidation(visitType, customField) {

	for (var i=1; i<10; i++) {
		var label = eval("custom_list" + i + "_name");
		var labelValidate = eval("custom_list" + i + "_validate");
		var labelObj = eval("document.mainform.custom_list"+i+"_value");

		if (customField == label && labelObj != null && labelObj.value == '') {
			if (labelValidate == 'A' || (labelValidate == 'I' && visitType == 'I') ||
					(labelValidate == 'O' && visitType == 'O')) {
				alert(label+ " "+getString("js.registration.patient.is.required.string"));
				return "custom_list"+i+"_value";
			}
		}
	}

	for (var i=1; i<20; i++) {
		var label = eval("custom_field" + i + "_label");
		var labelValidate = eval("custom_field" + i + "_validate");
		var labelObj = eval("document.mainform.custom_field"+i);

		if (customField == label && labelObj != null && labelObj.value == '') {
			if (labelValidate == 'A' || (labelValidate == 'I' && visitType == 'I') ||
					(labelValidate == 'O' && visitType == 'O')) {
				alert(label+ " "+getString("js.registration.patient.is.required.string"));
				return "custom_field"+i;
			}
		}

		if(customField == label && labelObj != null && !empty(labelObj.value)) {
			if(i>13 && i<17) {
				if(!doValidateDateField(labelObj))
					return "custom_field"+i;
			} else if (i > 16) {

				if(!isValidNumber(labelObj,"Y",label))
					return "custom_field"+i;

				if(!validateAmount(labelObj,label+" "+getString("js.registration.patient.allowed.only.eight.digit.number")))
					return "custom_field"+i;
			}
		}
	}

	var passportNo = document.mainform.passport_no;
	var passportValidity = document.mainform.passport_validity;
	var passportIssueCountry = document.mainform.passport_issue_country;
	var visaValidity = document.mainform.visa_validity;

	var checkValidate = false;
	var categoryObj = document.getElementById("patient_category_id");
	if (categoryObj != null && categoryObj.value != '') {
		var catDetails = findInList(categoryJSON, "category_id", categoryObj.value);
		if (!empty(catDetails.passport_details_required) &&  catDetails.passport_details_required == "Y") {
			checkValidate = true;
		}
	}

	if (customField == passportNoLabel && passportNo !=null && passportNo.value == "") {
		if (checkValidate && (passportNoValidate=="A"||(passportNoValidate=="I"&& visitType=="I")|| (passportNoValidate=="O"&& visitType=="O"))) {
	  		alert(passportNoLabel + " "+getString("js.registration.patient.is.required.string"));
	  	    return "passport_no";
	  	}
	}

	if (customField == passportValidityLabel && passportValidity !=null && passportValidity.value == "") {
		if (checkValidate && (passportValidityValidate=="A"||(passportValidityValidate=="I"&& visitType=="I")|| (passportValidityValidate=="O"&& visitType=="O"))) {
	  		alert(passportValidityLabel + " "+getString("js.registration.patient.is.required.string"));
	  	    return "passport_validity";
	  	}
	}

	if (customField == passportValidityLabel && passportValidity !=null && passportValidity.value != "") {
		if (passportValidityValidate=="A"||(passportValidityValidate=="I"&& visitType=="I")|| (passportValidityValidate=="O"&& visitType=="O")) {
		  	if (!doValidateDateField(passportValidity, 'future'))
				return "passport_validity";
		}
	}

	if (customField == passportIssueCountryLabel && passportIssueCountry !=null && passportIssueCountry.value == "") {
		if (checkValidate && (passportIssueCountryValidate=="A"||(passportIssueCountryValidate=="I"&& visitType=="I")|| (passportIssueCountryValidate=="O"&& visitType=="O"))) {
	  		alert(passportIssueCountryLabel + " "+getString("js.registration.patient.is.required.string"));
	  	    return "passport_issue_country";
	  	}
	}

	if (customField == visaValidityLabel && visaValidity !=null && visaValidity.value == "") {
		if (checkValidate && (visaValidityValidate=="A"||(visaValidityValidate=="I"&& visitType=="I")|| (visaValidityValidate=="O"&& visitType=="O"))) {
	  		alert(visaValidityLabel + " "+getString("js.registration.patient.is.required.string"));
	  	    return "visa_validity";
	  	}
	}

	if (customField == visaValidityLabel && visaValidity !=null && visaValidity.value != "") {
		if (visaValidityValidate=="A"||(visaValidityValidate=="I"&& visitType=="I")|| (visaValidityValidate=="O"&& visitType=="O")) {
		  	if (!doValidateDateField(visaValidity, 'future'))
				return "visa_validity";
		}
	}

	var familyId = document.mainform.family_id;

	if (customField == familyIDLabel && familyId !=null && familyId.value == "") {
		if (familyIDValidate=="A"||(familyIDValidate=="I"&& visitType=="I")|| (familyIDValidate=="O"&& visitType=="O")) {
	  		alert(familyIDLabel + " "+getString("js.registration.patient.is.required.string"));
	  	    return "family_id";
	  	}
	}

	var nationalityId = document.mainform.nationality_id;
	if(nationalityId != null && nationalityId.value == "" && customField == nationalityLabel) {
		if (nationalityValidate =="A" || (nationalityValidate == "I" && visitType == "I") || (nationalityValidate =="O" && visitType == "O")) {
	  		alert(nationalityLabel + " "+getString("js.registration.patient.is.required.string"));
	  	    return "nationality_id";
	  	}
	}

	return null;
}

function validatePassportCustomFields(custfieldObj, custfieldLabel) {
	var isPrimaryField = false;
	var isSecondaryField = false;

	var isCfDialog = !(typeof cfDialog == "undefined");

	if (mainPagefieldsList.length > 0) {
		for (var i = 0; i < mainPagefieldsList.length; i++) {
			if (custfieldLabel == mainPagefieldsList[i]) {
				isPrimaryField = true;
				break;
			}
		}
	}

	if (!isPrimaryField) {
		if (dialogFieldsList.length > 0) {
			for (var i = 0; i < dialogFieldsList.length; i++) {
				if (custfieldLabel == dialogFieldsList[i]) {
					isSecondaryField = true;
					break;
				}
			}
		}
	}

	if (isPrimaryField) {
		if (CollapsiblePanel1.isOpen()) {
			setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
		} else {
			CollapsiblePanel1.open();
			setTimeout("document.mainform." + custfieldObj + ".focus()", 800);
		}
		if (isCfDialog) cfDialog.hide();
		return false;

	}else if (isSecondaryField) {
		if (!CollapsiblePanel1.isOpen()) CollapsiblePanel1.open();
		if (isCfDialog) cfDialog.show();
		setTimeout("document.mainform." + custfieldObj + ".focus()", 100);
		return false;
	}

	if (isCfDialog) cfDialog.hide();
	return true;
}


function validatePassportDetails() {
	var passportNo = document.mainform.passport_no;
	var passportValidity = document.mainform.passport_validity;
	var passportIssueCountry = document.mainform.passport_issue_country;
	var visaValidity = document.mainform.visa_validity;

	if (!empty(passportNoLabel) && passportNo !=null && passportNo.value == "") {
  		if (passportNoValidate=="A"||(passportNoValidate=="I"&& visitType=="I")|| (passportNoValidate=="O"&& visitType=="O")){
            alert(passportNoLabel + " " + getString("js.registration.patient.is.required.string"));
            if (!validatePassportCustomFields("passport_no", passportNoLabel)) return false;
        }
	}

	if (!empty(passportValidityLabel) && passportValidity !=null && passportValidity.value == "") {
        if (passportValidityValidate=="A"||(passportValidityValidate=="I"&& visitType=="I")|| (passportValidityValidate=="O"&& visitType=="O")) {
            alert(passportValidityLabel + " " + getString("js.registration.patient.is.required.string"));
            if (!validatePassportCustomFields("passport_validity", passportValidityLabel)) return false;
        }
	}

	if (!empty(passportValidityLabel) && passportValidity !=null && passportValidity.value != "") {
	  	if (!doValidateDateField(passportValidity, 'future'))
			if (!validatePassportCustomFields("passport_validity", passportValidityLabel)) return false;
	}

	if (!empty(passportIssueCountryLabel) && passportIssueCountry !=null && passportIssueCountry.value == "") {
        if (passportIssueCountryValidate=="A"||(passportIssueCountryValidate=="I"&& visitType=="I")|| (passportIssueCountryValidate=="O"&& visitType=="O")) {
            alert(passportIssueCountryLabel + " " + getString("js.registration.patient.is.required.string"));
            if (!validatePassportCustomFields("passport_issue_country", passportIssueCountryLabel)) return false;
        }
	}

	if (!empty(visaValidityLabel)  && visaValidity !=null && visaValidity.value == "") {
        if (visaValidityValidate=="A"||(visaValidityValidate=="I"&& visitType=="I")|| (visaValidityValidate=="O"&& visitType=="O")) {
            alert(visaValidityLabel + " " + getString("js.registration.patient.is.required.string"));
            if (!validatePassportCustomFields("visa_validity", visaValidityLabel)) return false;
        }
	}

	if (!empty(visaValidityLabel) && visaValidity !=null && visaValidity.value != "") {
	  	if (!doValidateDateField(visaValidity, 'future'))
			if (!validatePassportCustomFields("visa_validity", visaValidityLabel)) return false;
	}

	return true;
}

function validateOnChangePatientCategory() {
	var categoryObj = document.getElementById("patient_category_id");
	if (categoryObj != null && categoryObj.value != '') {
		var catDetails = findInList(categoryJSON, "category_id", categoryObj.value);
		if (!empty(catDetails.passport_details_required) &&  catDetails.passport_details_required == "Y") {
			return validatePassportDetails();
		}
	}
	return true;
}

function UnFormatTextAreaValues(vText) {
	vRtnText= vText;
	while(vRtnText.indexOf("~") > -1) {
		vRtnText = vRtnText.replace("~"," ");
	}
	while(vRtnText.indexOf("^") > -1) {
		vRtnText = vRtnText.replace("^"," ");
	}
	return  vRtnText;
}

function FormatTextAreaValues(vText) {
	vRtnText= vText;
	while(vRtnText.indexOf("\n") > -1) {
		vRtnText = vRtnText.replace("\n"," ");
	}
	while(vRtnText.indexOf("\r") > -1) {
		vRtnText = vRtnText.replace("\r"," ");
	}
	return vRtnText;
}

function checkAge() {
	var day = document.mainform.dobDay.value;
	var month = document.mainform.dobMonth.value;
	var year = document.mainform.dobYear.value;
	if (day == 'DD' && month == 'MM' && year == 'YY') {
		document.mainform.age.value == 'Age';
	}
}

function validatePatientAge() {
	var msg = getString("js.registration.patient.age.validation.more.than.120.years");
	var ageIn = document.mainform.ageIn.value;
    var age = document.mainform.age.value;
	if(ageIn == 'Y' && age > 120) {
		alert(msg);
		document.mainform.age.focus();
		return false;
	} else if(ageIn == 'M' && (age/12) > 120) {
		alert(msg);
		document.mainform.age.focus();
		return false;
	} else if(ageIn == 'D' && (age/365) > 120) {
		alert(msg);
		document.mainform.age.focus();
		return false;
	}
	return true;
}

function patientDetailsValidation() {
	var isCollapseElmt = !(typeof CollapsiblePanel1 == "undefined");
	var prefCheck = regPref.patientPhoneValidate;
	if (document.mainform.government_identifier != null)
		document.mainform.government_identifier.value  = trim(document.mainform.government_identifier.value);

	document.mainform.patient_name.value = trim(document.mainform.patient_name.value);
	document.mainform.middle_name.value = trim(document.mainform.middle_name.value);
	document.mainform.last_name.value = trim(document.mainform.last_name.value);
	document.mainform.patient_address.value	= FormatTextAreaValues(document.mainform.patient_address.value);
	document.mainform.patient_area.value = trim(document.mainform.patient_area.value);
	document.mainform.pat_city_name.value = trim(document.mainform.pat_city_name.value);
	document.mainform.patient_phone.value = trim(document.mainform.patient_phone.value);

	if (regPref.patientPhoneValidate != 'N') {
		if (screenid == 'ip_registration' && prefCheck == 'I')
		if (trim(document.mainform.patient_phone.value) == '' ){
			showMessage("js.registration.patient.phone.no.required");
			$("#patient_phone_national").focus();
			return false;
		}
	}


    var dobDay = document.mainform.dobDay.value;
    var dobMonth = document.mainform.dobMonth.value;
    var dobYear = document.mainform.dobYear.value;

    if(document.mainform.salutation.value==""){
	    showMessage("js.registration.patient.title.required");
	    document.mainform.salutation.focus();
	    return false;
    }

    if(document.mainform.patient_name.value == "..FirstName.." || document.mainform.patient_name.value == ""){
	    showMessage("js.registration.patient.first.name.required");
	    document.mainform.patient_name.focus();
	    return false;
    }
    
    if(gScreenId === 'ip_registration') {
    	if ((regPref.lastNameRequired === 'Y' || regPref.lastNameRequired === 'I') 
    			&& (document.mainform.last_name.value === "..LastName.." || document.mainform.last_name.value === "")) {
    		showMessage("js.registration.patient.last_name_required");
    		document.mainform.last_name.focus();
    		return false;
    	}
    } else if(gScreenId === 'out_pat_reg') {
    	if ((regPref.lastNameRequired === 'Y' || regPref.lastNameRequired === 'O') 
    			&& (document.mainform.last_name.value === "..LastName.." || document.mainform.last_name.value === "")) {
    		showMessage("js.registration.patient.last_name_required");
    		document.mainform.last_name.focus();
    		return false;
    	}
    } else {
    	if ((regPref.lastNameRequired === 'Y') 
    			&& (document.mainform.last_name.value === "..LastName.." || document.mainform.last_name.value === "")) {
    		showMessage("js.registration.patient.last_name_required");
    		document.mainform.last_name.focus();
    		return false;
    	}
    }

	/*
    if(document.mainform.middle_name.value == "..MiddleName.." || document.mainform.middle_name.value == ""){
        alert("Middle name is required");
        document.mainform.middle_name.focus();
        return false;
    }*/

	if(document.mainform.patient_gender.options.selectedIndex==0){
		showMessage("js.registration.patient.gender.required");
		document.mainform.patient_gender.focus();
		return false;
	}

	 if (!validateSalutationGender()) return false;

	/*
	 * One of DOB or Age must be entered: prefer DOB
	 */
	if (isDOBEntered()) {
		var dob = validateDOB();
		if (!dob) {
			return false;
		}
		if(!getAge(false, dob)){
			return false;
		}

		if(!validatePatientAge()) {
			return false;
		}

		// set the hidden dateOfBirth input value that the backend needs
		document.mainform.dateOfBirth.value =
			dob.getFullYear() + "-" + getFullMonth(dob.getMonth()) + "-" + getFullDay(dob.getDate());
	} else {

		if(document.mainform.age.value=="..Age.." || document.mainform.age.value=="Age") {
			if(agedisable == 'Y' ) {
				showMessage("js.registration.patient.dob.age.required");
				document.mainform.age.focus();
				return false;
			} else if (agedisable == 'N') {
				showMessage("js.registration.patient.dob.required");
				document.mainform.dobDay.focus();
				return false;
			}
		} else {
			if(!validatePatientAge()) {
				return false;
			}
		}
	}


	if (document.mainform.pat_city_name.value == '') {
		showMessage(regPref.enableDistrict == 'Y' ? "js.registration.patient.city.subdistrict.required" : "js.registration.patient.city.required");
		if (isCollapseElmt && CollapsiblePanel1 != null)
			CollapsiblePanel1.open();
		document.mainform.pat_city_name.focus();
		return false;
	}

    if (document.mainform.areaValidate.value=="A" && document.mainform.patient_area.value=="") {
    	showMessage(regPref.enableDistrict == 'Y' ? "js.registration.patient.area.village.required" : "js.registration.patient.area.required");
    	if (isCollapseElmt && CollapsiblePanel1 != null)
    		CollapsiblePanel1.open();
    	document.mainform.patient_area.focus();
    	return false;
    }

    if (document.mainform.addressValidate.value=="A" && document.mainform.patient_address.value=="") {
    	showMessage("js.registration.patient.address.required");
    	if (isCollapseElmt && CollapsiblePanel1 != null)
    		CollapsiblePanel1.open();
    	document.mainform.patient_address.focus();
    	return false;
    }

    if(document.mainform.patient_address.value.length > 250){
		showMessage("js.registration.patient.address.length.check.string");
		if (isCollapseElmt && CollapsiblePanel1 != null)
    		CollapsiblePanel1.open();
		document.mainform.patient_address.focus();
		return false;
	}
	if(gScreenId === 'ip_registration' || gScreenId === 'edit_visit_details') {
	if(!onWardChange(1))
    return false;

   }

	if (document.mainform.validateEmailId.value == "A" && document.mainform.email_id.value == "") {
    	showMessage("js.registration.patient.email.id.required");
    	if (isCollapseElmt && CollapsiblePanel1 != null)
    		CollapsiblePanel1.open();
    	document.mainform.email_id.focus();
    	return false;
    }
    return true;

}

/*
function validateCategoryExpiryDate() {
	if (document.mainform.patient_category_id)
	 	document.mainform.patient_category_id.disabled=false;

	if (document.mainform.patient_category_id) {
		if(document.mainform.patient_category_id.value != '') {
		var elements = document.getElementsByName("category_expiry_date");
			for(var i=0; i<elements.length;i++){
				var obj = elements[i];
				if(obj.getAttribute("name")=="category_expiry_date"){
					var selectedExpDate = document.mainform.category_expiry_date.value;
					if(selectedExpDate!=""){
						myDate = new Date();
						var currDate= formatDueDate(myDate);
						if(getDateDiff(currDate,selectedExpDate)<0) {
							alert(categoryExpirydateText + " "+getString("js.registration.patient.shoul.not.less.than.current.date.string"));
							return false;
						}
					}
				}
			}
		}
	}
	return true;
}
*/

function patientAdditionalFieldsValidation() {

    var prefNextOfKins = document.mainform.nextofkinValidate.value;
    var relationName = document.mainform.relation.value;
    var contactNo = document.mainform.patient_care_oftext.value;
    var addres = document.mainform.patient_careof_address.value;
    var prefPatientPhone = document.mainform.patientPhoneValidate.value;
    var patientPhone = document.mainform.patient_phone.value;
    var addtnlPhone = document.mainform.patient_phone2.value;

	if(prefPatientPhone=="A") {
    	if(patientPhone=="") {
    		showMessage("js.registration.patient.phone.no.required");
    		$("#patient_phone_national").focus();
    		return false;
    	}
    }
  	if(contactNo != "") {
  		if ($("#patient_care_oftext_valid").val() == 'N'){
  			alert("Kin's mobile number is inValid");
  			$("#patient_care_oftext_national").focus();
    		return false;
  		}
    }

    if (prefNextOfKins=="A") {
    	if(relationName=="") {
    		showMessage("js.registration.patient.next.of.kin.relation.name.required");
    		document.mainform.relation.focus();
    		return false;
    	}
    	if(contactNo=="") {
    		showMessage("js.registration.patient.next.kin.relation.contact.required");
    		$("#patient_care_oftext_national").focus();
    		return false;
    	}
    	if(addres=="") {
    		showMessage("js.registration.patient.address.ph.required");
    		if (CollapsiblePanel1 != null) {
    			CollapsiblePanel1.open();
    			if (typeof cfDialog != 'undefined' && cfDialog != null) cfDialog.show();
    		}
    		document.mainform.patient_careof_address.focus();
    		return false;
    	}
    }

    if (addtnlPhone != '' && !validatePhoneNo(addtnlPhone, getString("js.registration.patient.invalid.phoneno"))) {
    	document.mainform.patient_phone2.focus();
    	return false;
    }
	if ( (null != document.mainform.mobilePatAccess ) && document.mainform.mobilePatAccess[0].checked ) {
	    if ( document.mainform.email_id.value == "" ) {
	            showMessage("js.registration.patient.email.id.required");
	            document.mainform.email_id.focus();
	            return false;
	    }
	    if(patientPhone=="") {
    		showMessage("js.registration.patient.phone.no.required");
    		$("#patient_phone_national").focus();
    		return false;
    	}
	}


	if ( (null != document.mainform.portalPatAccess ) && document.mainform.portalPatAccess[0].checked ) {
	    if ( document.mainform.email_id.value == "" ) {
	            showMessage("js.registration.patient.email.id.required");
	            document.mainform.email_id.focus();
	            return false;
	    }

	    if(!(FIC_checkField(" validate-email ", document.mainform.email_id))) {
	            showMessage("js.registration.patient.enter.emai.id.string");
	            document.mainform.email_id.focus();
	            return false;
	    }
     }else if (null != document.mainform.email_id && trim(document.mainform.email_id.value) != '' ) {
     	 if(!(FIC_checkField(" validate-email ", document.mainform.email_id))) {
	            showMessage("js.registration.patient.enter.emai.id.string");
	            document.mainform.email_id.focus();
	            return false;
	    }
     }

     if (null != document.mainform.government_identifier && trim(document.mainform.government_identifier.value) != '' ) {
     	if(!(FIC_checkField(" validate-govt-id ", document.mainform.government_identifier))) {
				alert(govtId_label+" "+
				getString("js.registration.patient.enter.govt.invalid.string")+". "+
				" "+getString("js.registration.patient.enter.govt.format.string")+" : "+
				govtId_pattern);
	            document.mainform.government_identifier.focus();
	            return false;
	    }
     }

     if (null != document.mainform.patient_phone && trim(document.mainform.patient_phone.value) != '' ) {
      	if($("#patient_phone_valid").val() == 'N') {
 				alert(getString("js.registration.patient.mobileNumber") + " " +
 				getString("js.registration.patient.enter.govt.invalid.string")+". ");
 	            $("#patient_phone_national").focus();
 	            return false;
 	    }
      }
     
    if(gScreenId === 'ip_registration') {
    	if ((regPref.maritalStatusRequired === 'Y' || regPref.maritalStatusRequired === 'I')
    			&& (document.mainform.marital_status_id != null && document.mainform.marital_status_id.options.selectedIndex==0)) {
    		showMessage("js.registration.patient.marital.status.required");
    		document.mainform.marital_status_id.focus();
    		return false;
    	}
    	if ((regPref.religionRequired === 'Y' || regPref.religionRequired === 'I')
        		&& (document.mainform.religion_id != null && document.mainform.religion_id.options.selectedIndex==0)) {
        	showMessage("js.registration.patient.religionrequired");
        	document.mainform.religion_id.focus();
        	return false;
        }
    } else if(gScreenId === 'out_pat_reg') {
    	if ((regPref.maritalStatusRequired === 'Y' || regPref.maritalStatusRequired === 'O')
        		&& (document.mainform.marital_status_id != null && document.mainform.marital_status_id.options.selectedIndex==0)) {
        	showMessage("js.registration.patient.marital.status.required");
        	document.mainform.marital_status_id.focus();
        	return false;
        }
        if ((regPref.religionRequired === 'Y' || regPref.religionRequired === 'O')
            	&& (document.mainform.religion_id != null && document.mainform.religion_id.options.selectedIndex==0)) {
            showMessage("js.registration.patient.religionrequired");
            document.mainform.religion_id.focus();
            return false;
        }
    } else {
    	if ((regPref.maritalStatusRequired === 'Y')
        		&& (document.mainform.marital_status_id != null && document.mainform.marital_status_id.options.selectedIndex==0)) {
        	showMessage("js.registration.patient.marital.status.required");
        	document.mainform.marital_status_id.focus();
        	return false;
        }
        if ((regPref.religionRequired === 'Y')
            	&& (document.mainform.religion_id != null && document.mainform.religion_id.options.selectedIndex==0)) {
            showMessage("js.registration.patient.religionrequired");
            document.mainform.religion_id.focus();
            return false;
         }
    }

	if (!isGovtIdUnique()) return false;

	if (typeof cfDialog != 'undefined' && cfDialog != null) cfDialog.hide();
	return true;
}


function formatDueDate(dateMSecs) {
	var dateObj = new Date(dateMSecs);
	var dateStr = formatDate(dateObj, 'ddmmyyyy', '-');
	return dateStr;
}

function validateDeptAllowedGender() {
	var allowedGender = document.mainform.dept_allowed_gender.value;
	var dept_name = document.mainform.dept_name.value;
	if (allowedGender == 'ALL')
		return true;
	var gender = document.mainform.patient_gender.value;
	if (dept_name != '') {
		if (allowedGender != gender) {
			alert(document.mainform.dept_name.options[document.mainform.dept_name.selectedIndex].text
					+ " "+getString("js.registration.patient.is.not.valid.for.string")+" "+document.mainform.patient_gender.options[document.mainform.patient_gender.selectedIndex].text+" patient.");
			document.mainform.dept_name.focus();
			return false;
		}
	}
	return true;
}
/*
 * This method can be used for those places where patient_gender is normal input field
 */

function validateDeptAllowedGenderForPatient(){
	var allowedGender = document.mainform.dept_allowed_gender.value;
	var dept_name = document.mainform.dept_name.value;
	if (allowedGender == 'ALL')
		return true;
	var gender = document.mainform.patient_gender.value;
	var genderText='';
	if(gender!=undefined && gender!=null){
	if('M'==gender)
		genderText='Male';
	else if('F'==gender)
		genderText='Female';
	else if('O'==gender)
		genderText='Others';
	else if('C'==gender)
		genderText='Couple';
	}
	if (dept_name != '') {
		if (allowedGender != gender) {
			alert(document.mainform.dept_name.options[document.mainform.dept_name.selectedIndex].text
					+ " "+getString("js.registration.patient.is.not.valid.for.string")+" "+genderText+" patient.");
			document.mainform.dept_name.focus();
			return false;
		}
	}
	return true;
}
function validateSalutationGender() {
	var salutation = document.mainform.salutation.value;
	if (document.mainform.salutation.options[document.mainform.salutation.selectedIndex].text == '')
		return true;
	var gender = document.mainform.patient_gender.value;
	var salDetails = findInList(salutationJSON, 'salutation_id', salutation);
	if (!empty(salDetails.gender) && gender != salDetails.gender) {
		if (salDetails.gender == 'N')
			return true;
		alert(document.mainform.salutation.options[document.mainform.salutation.selectedIndex].text
				+ " "+getString("js.registration.patient.is.not.valid.for.string")+" "+ document.mainform.patient_gender.options[document.mainform.patient_gender.selectedIndex].text);
		document.mainform.salutation.focus();
		return false;
	}
	return true;
}

function showHideCaseFile() {
	var categoryObj = document.getElementById("patient_category_id");
	if (document.getElementById("caseFileFields") != null && categoryObj != null) {
		if (categoryObj.value != '') {
			var catDetails = findInList(categoryJSON, "category_id", categoryObj.value);
			if (!empty(catDetails.case_file_required) &&  catDetails.case_file_required == "Y") {
				document.getElementById("caseFileFields").style.display = 'block';
				if(document.mainform.oldRegAutoGenerate != null && !document.mainform.oldRegAutoGenerate.disabled) {
					document.mainform.casefileNo.value = '';
					document.mainform.casefileNo.readOnly = false;
					document.mainform.oldRegAutoGenerate.checked = true;
					enableOldmrno();
				}else if(document.mainform.raiseCaseFileIndent != null && !document.mainform.raiseCaseFileIndent.disabled) {
					document.mainform.raiseCaseFileIndent.checked = true;
				}
			}else {
				document.getElementById("caseFileFields").style.display = 'none';
				document.mainform.casefileNo.readOnly = true;
				document.mainform.casefileNo.value = '';
				document.mainform.oldRegAutoGenerate.checked = false;
				enableOldmrno();
				if (document.mainform.raiseCaseFileIndent != null)
					document.mainform.raiseCaseFileIndent.checked = false;
			}
		}else {
			document.getElementById("caseFileFields").style.display = 'block';
			document.mainform.casefileNo.readOnly = false;
			document.mainform.casefileNo.value = '';
			document.mainform.oldRegAutoGenerate.checked = false;
			enableOldmrno();
			if (document.mainform.raiseCaseFileIndent != null)
				document.mainform.raiseCaseFileIndent.checked = false;
		}
	}
}

function setSmsStatusForVaccination() {
	smsForVaccination = document.getElementById('smsForVaccination');
	sms_for_vaccination = document.getElementById('sms_for_vaccination');
	if (smsForVaccination.checked) {
		sms_for_vaccination.value = 'Y'
	} else {
		sms_for_vaccination.value = 'N';
	}
}

function displayVisitMoreButton() {
	if(document.getElementById('displayVisitCustomBtn')) {
		if(!isVisitCustomFieldIsSecondary())
			document.getElementById('displayVisitCustomBtn').style.display = 'none';
		else
			document.getElementById('displayVisitCustomBtn').style.display = 'table-row';
	}
}

function isVisitCustomFieldIsSecondary() {
	var displayVisitMoreButton = false;
	if(visitCustomFieldList.length > 0) {
		for(var i=0;i<visitCustomFieldList.length;i++) {
			if(visitCustomFieldList[i].display == 'D') {
				displayVisitMoreButton = true;
				break;
			}
		}
	}
	return displayVisitMoreButton;
}

function validatePatientIdentification() {

	 if (patientIdentification == 'GP') {
     	if ((null != document.mainform.passport_no && trim(document.mainform.passport_no.value) != '') ||
     						(null != document.mainform.identifier_id && trim(document.mainform.identifier_id.value) != ''))
     		return true;
     	else {
     		if (null != document.mainform.passport_no)
     			validatePassportCustomFields("passport_no", passportNoLabel);
     		else
     			focusOnGovtIdentifier();
     		var passportLabelString = (null != document.mainform.passport_no) ? passportNoLabel : getString("js.registration.patient.passport.label");
     		var govtTypeLabelString = (null != document.mainform.identifier_id) ? govtId_type_label : getString("js.registration.patient.govtidentifier.label");
     		alert(passportLabelString +" "+ getString('js.registration.patient.or.string') +" "+govtTypeLabelString
     				+" "+getString("js.registration.patient.is.required.string"));
     		return false;
     	}
     }
	 else if (patientIdentification == 'G' && (null == document.mainform.identifier_id || trim(document.mainform.identifier_id.value) == '')) {
		var govtTypeLabelString = (null != document.mainform.identifier_id) ? govtId_type_label : getString("js.registration.patient.govtidentifier.label");
  		alert(govtTypeLabelString +" "+getString("js.registration.patient.is.required.string"));
  		return false;
	 }

	return true;
}

function validateGovtIdentifierMandatory() {

	if (govtIdentifierMandatory == 'Y') {
		if (null == document.mainform.government_identifier || trim(document.mainform.government_identifier.value) == '') {
			focusOnGovtIdentifier();
     		alert(govtId_label+" "+getString("js.registration.patient.is.required.string"));
     		return false;
     	}
    }
	return true;
}

function setGovtPattern() {
	var govidentifierElement = document.getElementById('government_identifier');
	if (!empty(document.getElementById('identifier_id'))) {
		govidentifierElement.removeAttribute('disabled');
		var identifierTypeID = document.getElementById('identifier_id').
							options[document.getElementById('identifier_id').selectedIndex].value;
		if (identifierTypeID != '') {
			for (var i=0; i<govtIdentifierTypesJSON.length; i++) {
				if (identifierTypeID == govtIdentifierTypesJSON[i].identifier_id) {
					govtId_pattern = empty(govtIdentifierTypesJSON[i].govt_id_pattern) ? '' : govtIdentifierTypesJSON[i].govt_id_pattern;
					govtIdentifierMandatory = govtIdentifierTypesJSON[i].value_mandatory;
					uniqueGovtID = govtIdentifierTypesJSON[i].unique_id;
					defaultValue = govtIdentifierTypesJSON[i].identifier_type;
				}
			}
		} else {
			govtId_pattern = '';
			govtIdentifierMandatory = '';
			uniqueGovtID = '';
			defaultValue = '';
			govidentifierElement.disabled=true;
		}
			govidentifierElement.value=defaultValue;
			if(defaultValue!=null && defaultValue!=''){
				govidentifierElement.readOnly=true;
			} else {
				govidentifierElement.readOnly=false;
			}
	}
	var govtidstar = document.getElementById('govtidstar');
	if (govtidstar != undefined && govtidstar != null) {
		if (govtIdentifierMandatory == 'Y')
			document.getElementById('govtidstar').style.display = 'inline';
		else
			document.getElementById('govtidstar').style.display = 'none';
	}
}

function onChangeOfOtherId() {
	var otherIdElement = document.getElementById("other_identification_doc_id")
	if (otherIdElement.value == ""){
		document.getElementById("other_identification_doc_value").disabled = true;
		document.getElementById("other_identification_doc_value_label").innerText ="Other Identifier Document Value:";
		}
	else{
		document.getElementById("other_identification_doc_value").disabled = false;
		document.getElementById("other_identification_doc_value_label").innerText = otherIdElement.options[otherIdElement.selectedIndex].label+ " Number";
		}
	document.getElementById("other_identification_doc_value").value = "";
}

function setOnlyGovtPattern() {
	var govidentifierElement = document.getElementById('government_identifier');
	if (!empty(document.getElementById('identifier_id'))) {
		govidentifierElement.removeAttribute('disabled');
		var identifierTypeID = document.getElementById('identifier_id').
							options[document.getElementById('identifier_id').selectedIndex].value;
		if (identifierTypeID != '') {
			for (var i=0; i<govtIdentifierTypesJSON.length; i++) {
				if (identifierTypeID == govtIdentifierTypesJSON[i].identifier_id) {
					govtId_pattern = empty(govtIdentifierTypesJSON[i].govt_id_pattern) ? '' : govtIdentifierTypesJSON[i].govt_id_pattern;
					govtIdentifierMandatory = govtIdentifierTypesJSON[i].value_mandatory;
					uniqueGovtID = govtIdentifierTypesJSON[i].unique_id;
					if(govtIdentifierTypesJSON[i].identifier_type == govidentifierElement.value){
						govidentifierElement.readOnly = true; 
					}else {
						govidentifierElement.readOnly = false; 
					}
				}
			}
		} else {
			govtId_pattern = '';
			govtIdentifierMandatory = '';
			uniqueGovtID = '';
			govidentifierElement.disabled=true;
		}
	}
	var govtidstar = document.getElementById('govtidstar');
	if (govtidstar != undefined && govtidstar != null) {
		if (govtIdentifierMandatory == 'Y')
			document.getElementById('govtidstar').style.display = 'inline';
		else
			document.getElementById('govtidstar').style.display = 'none';
	}
}


function isGovtIdUnique() {
	var govtIdentifier = '';
	var mr_no = '';
	if (screenid == 'reg_general') {
		if (!empty(mrNo))
			mr_no = mrNo;
	} else if (null != document.mrnoform && !empty(document.mrnoform.mrno)) {
		mr_no = document.mrnoform.mrno.value;
	}
	if (document.getElementById('government_identifier') != null)
		govtIdentifier = document.getElementById('government_identifier').value;
	if (uniqueGovtID == 'Y' && govtIdentifier != '') {
		if (!validateGovtIdentifierForBaby(mr_no, govtIdentifier))
			return true;
		var ajaxObj = newXMLHttpRequest();
		var url = cpath + '/pages/registration/regUtils.do?_method=isUniqueGovtID&govt_identifier='+govtIdentifier+'&mr_no='+mr_no;
		ajaxObj.open("GET", url.toString(), false);
		ajaxObj.send(null);
		if (ajaxObj.readyState == 4 && ajaxObj.status == 200 && ajaxObj != null) {
			var status = ajaxObj.responseText;
			if (status == 'false') {
				alert(govtId_label+" "+getString('js.registration.patient.already.exists.string'));
				document.getElementById('government_identifier').focus();
				return false;
			} else {
				return true;
			}
		}
	}
	return true;
}

function focusOnGovtIdentifier() {
	if (null != document.mainform.government_identifier) {
   		if (!CollapsiblePanel1.isOpen()) {
			CollapsiblePanel1.open();
			setTimeout("document.mainform.government_identifier.focus()", 800);
		} else
			setTimeout("document.mainform.government_identifier.focus()", 100);
	}
}

function validateGovtIdentifierForBaby(mrNo, govtIdentifier) {

	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/pages/registration/regUtils.do?_method=getBabyDOBAndMemberIdValidityDetails&mr_no=" + mrNo +
				"&visit_id=" + gVisitId;

	ajaxobj.open("POST", url.toString(), false);
	ajaxobj.send(null);
	if (ajaxobj.readyState == 4) {
		if ((ajaxobj.status == 200) && (ajaxobj.responseText != null)) {
			return babyGovtIdtDetailsHandaler(ajaxobj.responseText, govtIdentifier);
		}
	}

	return true;
}


function babyGovtIdtDetailsHandaler(responseText, govtIdentifier) {
	//var memberId = babyMemeberId;
	eval("babyInfo =" + responseText);
	if (babyInfo != null) {
		var babyDetails = babyInfo.babyDetails;
		var babyVisitDetails = babyInfo.babyVisitDetails;
		var helthAuthPrefs =  babyInfo.helathAuthPrefs;
		var parentMemberId = babyInfo.member_id;
		var parentGovtIdentifier = babyInfo.govtIdentifier;
		if(babyDetails != null && helthAuthPrefs != null) {
			var salutation = babyDetails.salutation;
			salutation = salutation.toUpperCase();
			if(salutation == 'BABY' && govtIdentifier == parentGovtIdentifier) {
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


const ONE_MONTH = 31;
const TWO_YEARS = 730;
const YEAR_MONTHS = 12;
const FIVE_YEARS = 5;

function dateOfBirthToAge(dateOfBirth, date = moment()) {
  let age = date.diff(dateOfBirth, 'days');
  let ageUnit;
  if (age >= 0) {
    if (age < ONE_MONTH) {
      ageUnit = 'D';
    } else if (age < TWO_YEARS) {
      age = date.diff(dateOfBirth, 'months');
      ageUnit = 'M';
    } else {
      age = date.diff(dateOfBirth, 'years');
      ageUnit = 'Y';
    }
  }
  return { age, ageUnit };
}

function getVisibility(prefName){
	if((regPref[prefName]== 'A' ||
			(screenId === "ip_registration" && regPref[prefName]  === "I") ||
			(screenId === "out_pat_reg" && regPref[prefName]  === "O")) &&
			categoryJSON[document.getElementById("patient_category_id").selectedIndex -  1] &&
			categoryJSON[document.getElementById("patient_category_id").selectedIndex -  1].passport_details_required === "Y"
	){
		 return 'visible';
	}
	return 'hidden';
}

function checkPassportAsteriskRequired(){
	let labels = ["passport_no_star", "passport_validity_star",
		"visa_validity_star", "passport_issue_country_star"];
	let labels_pref = ["passport_no_validate", "passport_validity_validate",
		"visa_validity_validate", "passport_issue_country_validate"];
	for(var i = 0; i < labels.length ; i++){
		if(document.getElementById(labels[i]) && document.getElementById("patient_category_id") && document.getElementById("patient_category_id").selectedIndex != 0) {
			document.getElementById(labels[i]).style.visibility = getVisibility(labels_pref[i]);
		} else if(document.getElementById(labels[i])) {
			document.getElementById(labels[i]).style.visibility = 'hidden';
		}
	}
}

function initializePatientGroupsList() {
	var list = document.getElementById("patient_group");
	var len = 0;
	var optn;
	for (var i=0;i<patientConfidentialityCategoriesJSON.length;i++) {
			optn = new Option(patientConfidentialityCategoriesJSON[i].name, patientConfidentialityCategoriesJSON[i].confidentiality_grp_id);
		list.options[len] = optn;
		len++;
	}
	if (patientConfidentialityGroup != "") {
		if (!patientConfidentialityCategoriesJSON.find(category => category.confidentiality_grp_id == patientConfidentialityGroup)) {
			list.options[len] = new Option(patientConfidentialityGroupName, patientConfidentialityGroup);
			list.disabled = true;
		}
		setSelectedIndex(document.mainform.patient_group, patientConfidentialityGroup);
	}
	if (len > 1) {
		var patientGroupTds = document.getElementsByClassName("patient_group_td");
		for (var i=0;i<patientGroupTds.length;i++) {
			patientGroupTds[i].style.display = 'table-cell';
		}
	}
}
