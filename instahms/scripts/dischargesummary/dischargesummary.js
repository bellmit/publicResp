var varVisitType = "";
var mySimpleDialog = null;

// initializes the yui simple dialog for PDF Form template.
function initDialog() {
	if (format != 'P' || mySimpleDialog != null) return;

	mySimpleDialog = new YAHOO.widget.SimpleDialog("dlg", {
	    width: "30em",
	    fixedcenter:true,
	    modal:true,
	    visible:false,
	    draggable:false });
	mySimpleDialog.setHeader("Message!");
	var message = "<table><tr><td>Are you sure you want to open the form?<td></tr>";
	message = message + "<tr><td> Click \"Yes\" to save and open the Pdf Form.<td><tr>";
	message = message + "<tr><td> Click \"No\" to just save the discharge Details.</td></tr>";
	message = message + "<tr><td> Click \"Cancel\" to remain in same window.</td></tr></table>";

	mySimpleDialog.setBody(message);
	mySimpleDialog.cfg.setProperty("icon",YAHOO.widget.SimpleDialog.ICON_HELP);

	var handleYes = function() {
		//when ready, hide the SimpleDialog:
		this.hide();
		var template_id = document.dischargeSummaryForm.formId.value;
		document.dischargeSummaryForm.action = "discharge.do?_method=openEditablePdfForm&template_id="+template_id+"&openPdf=Y";
		document.dischargeSummaryForm.submit();

	}
	var handleNo = function() {
		// it just simply hides the dialog
		this.hide();
		var template_id = document.dischargeSummaryForm.formId.value;
		document.dischargeSummaryForm.action = "discharge.do?_method=openEditablePdfForm&template_id="+template_id+"&openPdf=N";
		document.dischargeSummaryForm.submit();

	}
	var handleCancel = function() {
		//would handle the cancellation of the
		//process.
		//when ready, hide the SimpleDialog:
		this.hide();
	}

	var myButtons = [ { text:"Yes",
	                    handler:handleYes },
	                  { text:"No",
	                  	handler:handleNo },
	                  { text:"Cancel",
	                    handler:handleCancel,
	                    isDefault:true } ];
	mySimpleDialog.cfg.queueProperty("buttons", myButtons);
	//mySimpleDialog.render(document.body);

}

function deleteDischargeSummary() {
	var mrNo = document.dischargeSummaryForm.mrNo.value;
	document.dischargeSummaryForm.action = 'discharge.do?_method=delete';
	document.dischargeSummaryForm.submit();
	//document.dischargeSummaryForm.action = 'discharge.do?_method=delete';
	//document.dischargeSummaryForm.submit();
}

function openEditablePdf() {
	var mrno = document.dischargeSummaryForm.scat.value;
	initDialog();
	mySimpleDialog.render(document.dischargeSummaryForm);
	mySimpleDialog.show();

}

function onClickSave(){
		getDisDocId();
		if (!validateDischargeDateAndTime()) {
			return false;
		}
		if(saveFollowUpDetails()){
			if(format == "F") {
				// replace the non printable characters with space.
				saveDisForm();
			} else if(format == "T") {
				return saveDisHtml();
			} else {
				openEditablePdf();
			}
		}else{
			return false;
		}
}

function validateDischargeDateAndTime() {
  		var disDate = document.dischargeSummaryForm.dischDateForDischSummary.value;
	  	var disTime = document.dischargeSummaryForm.dischTimeForDischSummary.value;

		if (disDate != "") {
			if (!doValidateDateField(document.getElementById("dischDateForDischSummary"))) {
	                return false;
	        }
	    	var msg = validateDateStr(disDate);
			if (msg != null && msg!="") {
				alert(msg);
				return false;
			}
		}
		if (disTime != "") {
			if (!validateTime(document.getElementById("dischTimeForDischSummary"))) {
					return false;
			}
		}
		return true;
}


	function saveDisForm(){
		var mrno = document.dischargeSummaryForm.scat.value;
		var form_id = document.dischargeSummaryForm.formId.value;

		var valueCheck=0;
		var rowLen = 0;
		var tableId = document.getElementById("dischargesummary");
		if(tableId!=null){
			rowLen = tableId.rows.length;
		}
		for(var i=1;i<=rowLen;i++){
			var textValue = document.getElementById("field"+i).value;
			if(textValue == ""){
				valueCheck++;
			}
	   }
	   if(valueCheck == rowLen){
			alert("Please Enter Discharge Summary Details to Save.");
			return false;
	   }
	   if(checkLength()){
	   		document.dischargeSummaryForm.action = "discharge.do?_method=saveDischargeSummaryForm&form_id="+form_id;
			document.dischargeSummaryForm.submit();
	   }else{
			return false;
       }
  }

  function saveDisHtml(){
	var mrno = document.dischargeSummaryForm.scat.value;
  	var patId = document.dischargeSummaryForm.patient_id.value;
	var templateId = document.dischargeSummaryForm.templateId.value;
	document.dischargeSummaryForm.action =
		"discharge.do?_method=saveDischargeSummaryHtml&templateId="+templateId;
	tinyMCE.triggerSave();
	document.dischargeSummaryForm.submit();
	return true;
  }


function validateTime(timeField, index) {

	var strTime = timeField.value;
	var timePattern = /[0-9]:[0-9]/;
	var regExp = new RegExp(timePattern);
	if (strTime == '') {
		return true;
	}
	if (regExp.test(strTime)) {
		var strHours = strTime.split(':')[0];
		var strMinutes = strTime.split(':')[1];
		if (!isInteger(strHours)) {
			alert("Incorrect time format : hour is not a number");
			timeField.focus();
			return false;
		}
		if (!isInteger(strMinutes)) {
			alert("Incorrect time format : minute is not a number");
			timeField.focus();
			return false;
		}
		if ((parseInt(strHours) > 23) || (parseInt(strHours) < 0)) {
			alert("Incorrect hour : please enter 0-23 for hour");
			timeField.focus();
			return false;
		}
		if ((parseInt(strMinutes) > 59) || (parseInt(strMinutes) < 0)) {
			alert("Incorrect minutes : please enter 0-59 for minutes");
			timeField.focus();
			return false;
		}
	} else {
		alert("Incorrect time format : please enter HH:MM");
		timeField.focus();
		return false;
	}
	return true;
}

var filelistJSON = null;

  function funGetImageContent(docId){
		window.open("discharge.do?_method=getImageContent&docId="+docId,"","width=300,height=300,status=no,resizable=no,top=200,left=250,scrollbars=yes,resizable=yes");
  }
  function fundeleteImageContent(docId,mrno,patId){
		document.dischargeSummaryForm.action = "discharge.do?_method=deleteImage&docId="+docId+"&mrNo="+mrno+"&patient_id="+patId;
		document.dischargeSummaryForm.submit();
  }

  function validate(){
		var mrno = document.dischargeSummaryForm.scat.value;
		var PatId = document.dischargeSummaryForm.patient_id.value;
		if(mrno==""){
		alert("Enter MR No.");
		return false;
		}
		document.dischargeSummaryForm.action = "discharge.do?_method=getDischarge&Mrno="+mrno+"&patient_id="+PatId;
		document.dischargeSummaryForm.submit();
	}


function checkLength(){
/*
  var varMaxLength ="3998";
  	var tableId = document.getElementById("dischargesummary");
  	var rowLen = tableId.rows.length;

  	for(var i=1;i<=rowLen;i++){
		var textValue = document.getElementById("field"+i).value;
		if (textValue.length > varMaxLength)
	   	{
			//document.getElementById("field"+i).value =document.getElementById("field"+i).value.substring(0,varMaxLength);
			var caption = document.getElementById("caption"+i).value;
			document.getElementById("field"+i).focus();
			alert("Maximum text input limit reached for "+"\""+caption+"\" field");
			return false;
	   	}
	}
*/
	return true;
  }

function init(){

	var form = document.dischargeSummaryForm;
	var doctorId = 'null';
	var followUpDocId = 'null';

	// populate the discharge summary doctor and followup doctors only when user has rights to add/edit discharge summary.
	if (document.getElementById('hasRights')) {
		autoComplete();

		if ( (doctorId != null) && (doctorId != "") ){
			for ( var i=0 ; i< dischargeDoctors.length; i++){
				if(doctorId == dischargeDoctors[i]["doctor_id"]){
					form.disDoctorId.value = dischargeDoctors[i]["doctor_name"];
					form.doctorId.value = dischargeDoctors[i]["doctor_id"];
				}
			}
		}

		setTimeout("fillFollowUpDetails()", 100);
	}
}

function onClickUploadSave(){
	getDisDocId();
	if (!validateDischargeDateAndTime()) {
		return false;
	}
	if (document.dischargeSummaryForm.docid.value == "") {
		// new report being created, file is required.
		if (document.dischargeSummaryForm.theFile.value == "") {
			alert("Please upload file(s) to save");
			return false;
		}
	}

	if(saveFollowUpDetails()){
		document.dischargeSummaryForm.action="discharge.do?_method=saveDischargeSummaryUploadFiles";
		document.dischargeSummaryForm.submit();
	}else{
		return false;
	}
}

var dischargeDoctorsArray = [];
var dischargeDoctorsIndex = [];
var yuiAutoComplets = [];
function autoComplete(){
		dischargeDoctorsArray.length = dischargeDoctors.length;
		for(var i=0;i<dischargeDoctors.length;i++){
			var item = dischargeDoctors[i]
			dischargeDoctorsArray[i] = item["doctor_name"];
			dischargeDoctorsIndex[item["doctor_name"]] = i;
		}

		YAHOO.example.ACJSArray = new function(){
			  // Instantiate first JS Array DataSource
			  var doctorsJson = {result : dischargeDoctors};
			  var datasource = new YAHOO.util.LocalDataSource(doctorsJson);
			  datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
			  datasource.responseSchema = {
			  	resultsList : "result",
			  	fields : [{key : "doctor_name"}]
			  };

			  //YAHOO.log(YAHOO.lang.dump(dischargeDoctors));
			  // Instantiate first AutoComplete

			  /*
			  * to initialize the container with all the doctors.
			  * if we dont specify this property it will container with the 10 items only.
			  * i.e., whenever we call the autocomplete._aListItems retunrs only items.
			  */
			  //YAHOO.widget.AutoComplete.prototype.maxResultsDisplayed = dischargeDoctors.length;
			  var autoComp = new YAHOO.widget.AutoComplete('disDoctorId','disDocContainer', datasource);
			  autoComp.prehighlightClassName = "yui-ac-prehighlight";
			  autoComp.typeAhead = true;
			  // Enable a drop-shadow under the container element
			  autoComp.useShadow = true;
			  // Disable the browser's built-in autocomplete caching mechanism
			  autoComp.allowBrowserAutocomplete = false;
			  autoComp.maxResultsDisplayed = 20;
			  // Require user to type at least 0 characters before triggering a query
			  autoComp.minQueryLength = 0;
			  // Display up to 20 results in the container
			  //autoComp.maxResultsDisplayed = dischargeDoctors.length;
			  // Do not automatically highlight the first result item in the container
			  autoComp.autoHighlight = false;
			  // disable force selection,user can type his/her own complaint(which is not there in master)
			  autoComp.forceSelection = true;
			  autoComp.resultTypeList = false;
			  autoComp.filterResults = Insta.queryMatchWordStartsWith;
			  autoComp.formatResult = Insta.autoHighlightWordBeginnings;


			  if (disDoctorId != null && disDoctorId != '') {
			  	  var elNewItem = matches(disDoctorId, autoComp, dischargeDoctorsArray, dischargeDoctorsIndex);
				  autoComp._selectItem(elNewItem);
			  }
			  yuiAutoComplets['disDoctorId'] = autoComp;

		}
}

function matches(doctorName, autocomplete, dischargeDoctorsArray, dischargeDoctorsInd) {
	var elListItem = autocomplete._elList.childNodes[0];
    sMatchKey = (autocomplete.dataSource.responseSchema.fields) ?
               (autocomplete.dataSource.responseSchema.fields[0].key || autocomplete.dataSource.responseSchema.fields[0]) : 0;
    var oResult;
    for (var i=0; i<dischargeDoctors.length; i++) {
    	var doctor = dischargeDoctors[i][sMatchKey];
    	if (doctor == doctorName) {
    		oResult = dischargeDoctors[i];
    		elListItem._sResultMatch = doctor;
		    elListItem._oResultData = oResult;
			return elListItem;
    	}
    }
   return null;
}

// if the user entered value is not found in autocomplete list
// on key press enter of the text box, it will clears the textbox.
function clearOnEnter(e, id){
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 ) {
		var autocompleteObj = yuiAutoComplets[id];
		var doctorName = YAHOO.util.Dom.get(id).value;
		var elNewItem = null;
		if (id == 'disDoctorId')
			elNewItem = matches(doctorName, autocompleteObj, dischargeDoctorsArray, dischargeDoctorsIndex);
		else
			elNewItem = matches(doctorName, autocompleteObj, followUpDoctorsArray, followUpDoctorsIndex);

		if (elNewItem != null) {
			autocompleteObj._selectItem(elNewItem);
		} else {
			autocompleteObj._clearSelection();
		}
	}
	return true;
}

function getDisDocId(){
	document.dischargeSummaryForm.doctorId.value="";
	var disDoctorId = document.dischargeSummaryForm.disDoctorId.value;
	for ( var i=0 ; i< dischargeDoctors.length; i++){
		if(disDoctorId == dischargeDoctors[i]["doctor_name"]){
			document.dischargeSummaryForm.doctorId.value = dischargeDoctors[i]["doctor_id"];
			document.dischargeSummaryForm.disDoctorId.value = dischargeDoctors[i]["doctor_name"];
		}
	}
}

var followUpDoctorsArray = [];
var followUpDoctorsIndex = [];
function followUpDocAutoComplete(followUpDocId,followUpDocContainer, txtBoxvalue){

		followUpDoctorsArray.length = dischargeDoctors.length;
		for(var i=0;i<dischargeDoctors.length;i++){
			var item = dischargeDoctors[i]
			followUpDoctorsArray[i] = item["doctor_name"];
			followUpDoctorsIndex[item["doctor_name"]] = i;
		}
		YAHOO.example.ACJSArray = new function(){
			  // Instantiate first JS Array DataSource
			  var followUpDoctorsJson = {result: dischargeDoctors};
			  var datasource = new YAHOO.util.LocalDataSource(followUpDoctorsJson);
			  datasource.responseType = YAHOO.util.LocalDataSource.TYPE_JSON;
			  datasource.responseSchema = {
			  	resultsList : "result",
			  	fields : [{key : "doctor_name"}]
			  };
			  // Instantiate first AutoComplete

			  var autoComp = new YAHOO.widget.AutoComplete(followUpDocId,followUpDocContainer, datasource);
			  autoComp.prehighlightClassName = "yui-ac-prehighlight";
			  autoComp.typeAhead = true;
			  // Enable a drop-shadow under the container element
			  autoComp.useShadow = true;
			  // Disable the browser's built-in autocomplete caching mechanism
			  autoComp.allowBrowserAutocomplete = false;
			  // Require user to type at least 0 characters before triggering a query
			  autoComp.minQueryLength = 0;
			  // Display up to 20 results in the container
			  autoComp.maxResultsDisplayed = 20;
			  // Do not automatically highlight the first result item in the container
			  autoComp.autoHighlight = false;
			  // disable force selection,user can type his/her own complaint(which is not there in master)
			  autoComp.forceSelection = true;
			  autoComp.resultTypeList = false;
			  autoComp.filterResults = Insta.queryMatchWordStartsWith;
			  autoComp.formatResult = Insta.autoHighlightWordBeginnings;


			  if (txtBoxvalue != null && txtBoxvalue != '') {
				  var elNewItem = matches(txtBoxvalue, autoComp, followUpDoctorsArray, followUpDoctorsIndex);
				  autoComp._selectItem(elNewItem);
			  }
			  yuiAutoComplets[followUpDocId] = autoComp;
		}
}


	function addRowToDocotrVisitTable(){
		var doctorVisitTab = document.getElementById("followUpDetailsTab");
		var len = doctorVisitTab.rows.length;
		var followUpDocId = "followUpDocId" + len;
		var followUpDocContainer= "followUpDocContainer" + len;
		addRowToFollowUpTable();
		followUpDocAutoComplete(followUpDocId, followUpDocContainer, null);
	}

	function validateFollowUpDate(inputDateField) {
		var followUpDate = inputDateField.value;
		if (!doValidateDateField(inputDateField, 'future')){
                return false;
        }
	   	return true;
  	}

	function saveFollowUpDetails(){
		var visittablerows = document.getElementById("followUpDetailsTab").rows.length;
		var innerTestTabObj = document.getElementById("innerfollowupTab");
		var innerTabrows = innerTestTabObj.rows.length;

		// clearing the inner html table rows if any exists.
		for (var i=0; i<innerTabrows; i++) {
			innerTestTabObj.deleteRow(-1);
		}
		var intv = 0;
		for (var d=1;d<visittablerows; d++) {

		    var doctor = "followUpDocId" + d;
		    var visitdate = "followUpDate" + d;
		    var doctorremarks = "followUpRemarks" + d;
		    var markedForDelete = document.getElementById('hDeleteDoctor' + d).value;
		    var followUpId = "followUpId"+ d;

			if (YAHOO.util.Dom.get(doctor) == null) {

			} else {
				var doctorname = YAHOO.util.Dom.get(doctor).value;
				var doctorId = '';
				var visitingdate = YAHOO.util.Dom.get(visitdate).value;
				var visitremarks = YAHOO.util.Dom.get(doctorremarks).value;
				var followUpId  = YAHOO.util.Dom.get(followUpId).value;

				if ((visitingdate == "") && (markedForDelete == 'N')) {
					if (visitremarks != "" || doctorname != "") {
						alert("Please select Follow Up Date");
						document.getElementById(visitdate).focus();
						return false;
					}
				}

				if(followUpId == "" || followUpId == null)
					followUpId = "GenerateNewFollowUpId";

				if ((visitingdate != "") && (markedForDelete == 'N') && (followUpId == "GenerateNewFollowUpId")) {
					if (!validateFollowUpDate( YAHOO.util.Dom.get(visitdate) )) return false;

				}

				if((visitingdate != "") && (markedForDelete == 'N')){
					if(visitremarks == ""){
						alert("Please enter Remarks");
						document.getElementById(doctorremarks).focus();
						return false;
					}
					if(doctorname == ""){
						alert("Please select Doctor");
						document.getElementById(doctor).focus();
						return false;
					}
					if(!followUpValidateDate(visitdate)){
						return false;
					}
				}

				for(var k = 0;k<dischargeDoctors.length;k++){
					if(doctorname == dischargeDoctors[k].doctor_name){
						doctorId = dischargeDoctors[k].doctor_id;
					}
				}
				if(markedForDelete == 'Y'){
					if(followUpId != "" && followUpId!= "GenerateNewFollowUpId"){
						addDeleteRowToInnerHtml(followUpId);
					}
				}

				if((visitingdate != "") && (markedForDelete == 'N')){
					innertFollowUpDetails(doctorname, doctorId,visitingdate,visitremarks,followUpId);
					intv++;
					}
				}
		}
		return true;
	}

	function addDeleteRowToInnerHtml(followUpId) {
		var innerTestTabObj = document.getElementById("innerfollowupTab");
		var trObj = "", tdObj = "";
		trObj = innerTestTabObj.insertRow(-1);
		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<input type="hidden" name="deleteFollowUpIds" id="deleteFollowUpIds" value="'+ followUpId +'">';
	}

	function innertFollowUpDetails(followUpDocName, followUpDocId,followUpDate,followUpRemarks,followUpId){
		var innerTestTabObj = document.getElementById("innerfollowupTab");
		var trObj = "", tdObj = "";
		trObj = innerTestTabObj.insertRow(-1);

		tdObj = trObj.insertCell(-1);
		tdObj.innerHTML = '<input type="hidden" name="followUpDoctorName" id="followUpDoctorName" value="'+ followUpDocName +'">';

		tdObj = trObj.insertCell(-1);
		tdObj.innerHTML = '<input type="hidden" name="followUpDoctorId" id="followUpDoctorId" value="'+ followUpDocId +'">';

		tdObj = trObj.insertCell(-1);
		tdObj.innerHTML = '<input type="hidden" name="followUpDate" id="followUpDate" value="'+ followUpDate +'">';

		tdObj = trObj.insertCell(-1);
		tdObj.innerHTML = '<input type="hidden" name="followUpRemarks" id="followUpRemarks" value="'+ followUpRemarks +'">';

		tdObj = trObj.insertCell(-1);
		tdObj.innerHTML = '<input type="hidden" name="followUpId" id="followUpId" value="'+followUpId+'">';

	}
	function followUpValidateDate(followUpDate){

       var date = document.getElementById(followUpDate).value;
		if(date == ""){
			alert("Please select Follow Up Date");
			document.getElementById(followUpDate).focus();
			return false;
		}else{
	        if (!doValidateDateField(document.getElementById(followUpDate))){
	                return false;
	        }
	        var msg = validateDateStr(date);
	        if (msg != null && msg!=""){
	               alert(msg);
	               return false;
	         }
		}
		return true;
	}
    function fillFollowUpDetails(){

		var innerDisFollowupTabObj = document.getElementById("followUpDetailsTab");

		var trObj = "", tdObj = "";
		if (disFollowUpDetails == null || disFollowUpDetails.length == 0) {
			addRowToDocotrVisitTable();
		}

		for(var i=0;i<disFollowUpDetails.length;i++){

			var followup_id 		= disFollowUpDetails[i].FOLLOWUP_ID;
			var followup_date		= disFollowUpDetails[i].FOLLOWUP_DATE;
			var followup_doctor_id 	= disFollowUpDetails[i].FOLLOWUP_DOCTOR_ID;
			var followup_remarks	= disFollowUpDetails[i].FOLLOWUP_REMARKS;

			var followup_doctorname = "";

			var len = i+1;

			if ( (followup_doctor_id != null) && (followup_doctor_id != "") ){
				for ( var j=0 ; j< dischargeDoctors.length; j++){
					if(followup_doctor_id == dischargeDoctors[j]["doctor_id"]){
						//if(len == 1){
						//	document.dischargeSummaryForm.followUpDocId1.value = dischargeDoctors[j]["doctor_name"];
						//}else{
							followup_doctorname = dischargeDoctors[j]["doctor_name"];
						//}
					}
				}
			}
			//if(len == 1){
			//	document.getElementById("followUpDate1").value 		= followup_date;
			//	document.getElementById("followUpRemarks1").value 	= followup_remarks;
			//	document.getElementById("followUpId1").value 		= followup_id;
			//}else{
				var followUpDocId = "followUpDocId" + len;
				var followUpDocContainer= "followUpDocContainer" + len;
				var visitdate = "followUpDate" + len;
				var doctorremarks = "followUpRemarks" + len;
				var followUpId = "followUpId" + len;

				addRowToFollowUpTable();
				followUpDocAutoComplete(followUpDocId,followUpDocContainer, followup_doctorname);

				document.getElementById(followUpDocId).value 	= followup_doctorname;
				document.getElementById(visitdate).value 	= followup_date;
				document.getElementById(doctorremarks).value 	= followup_remarks;
				document.getElementById(followUpId).value 		= followup_id;
			//}
        }
        document.getElementById('disDoctorId').focus();
    }

    function addRowToFollowUpTable() {
    	var doctorVisitTab = document.getElementById("followUpDetailsTab");
		var len = doctorVisitTab.rows.length;
		var tdObj="", trObj="";
		trObj = doctorVisitTab.insertRow(len);
		if (len != 1)
			removeBottomBorder(len-1);

		var followUpDocId = "followUpDocId" + len;
		var followUpDocContainer= "followUpDocContainer" + len;
		var doctorac = "myAutoComplete" + len;
		var visitdate = "followUpDate" + len;
		var doctorremarks = "followUpRemarks" + len;
		var followUpId = "followUpId"+len;
		var followUpDoctorId = "followUpDoctorId"+len;
		var moredoctorvisit = document.getElementById('moredoctorvisit');
		var followUpDetailsTabOrder = parseInt(moredoctorvisit.getAttribute("tabindex"))+len;
		var imgDeleteDoctor = 'imgDeleteTest' + len;
		var imageSrc = cpath + "/icons/Delete.png";
		var hDeleteDoctor = "hDeleteDoctor" + len;


		tdObj = trObj.insertCell(0);
		tdObj.innerHTML = '<div id="'+doctorac+'" style="width:230; padding-bottom:2em;"><input id="'+followUpDocId+'" name="'+followUpDocId+'" type="text" class="first" style="width: 230;" tabindex="'+(followUpDetailsTabOrder)+'" onkeypress="return clearOnEnter(event, this.id);"/> <div id="'+followUpDocContainer+'" class="scrolForContainer"></div></div>';

		tdObj = trObj.insertCell(1);
		tdObj.innerHTML = getDateWidget(visitdate, visitdate, null,'future', null, false, true, followUpDetailsTabOrder+1);
		makePopupCalendar(visitdate);
		document.getElementById(visitdate).setAttribute('style', 'width: 100px;');

		tdObj = trObj.insertCell(2);
		tdObj.innerHTML = '<input type="text" name="'+ doctorremarks +'" id="'+ doctorremarks +'" maxlength="200" style="width : 390;" tabindex="'+(followUpDetailsTabOrder+2)+'"/><input type="hidden" name="'+followUpId+'" id="'+followUpId+'" value="GenerateNewFollowUpId" class="forminput"/>';

		tdObj = trObj.insertCell(3);
		tdObj.setAttribute('class', 'last');
		tdObj.innerHTML = '<a href="javascript:void(0)" onclick="changeElsColor('+len+');" ><img src="'+imageSrc+'" name="'+ imgDeleteDoctor +'" class="imgDelete" id="'+ imgDeleteDoctor +'" tabindex="'+(followUpDetailsTabOrder+3)+'" /></a>' +
						  '<input type="hidden" name="'+ hDeleteDoctor+'" id="'+ hDeleteDoctor +'" value="N"/>'

		moredoctorvisit.setAttribute("tabindex", followUpDetailsTabOrder+4);
    }

    function removeBottomBorder(index) {
    	addClassName('followUpDocId' + index, 'previousEl');
    	addClassName('followUpDate' + index, 'previousEl');
    	addClassName('followUpRemarks' + index, 'previousEl');
    }

    function changeElsColor(index) {
		var row = document.getElementById("followUpDetailsTab").rows[index];
		var markRowForDelete =
			document.getElementById('hDeleteDoctor'+index).value = document.getElementById('hDeleteDoctor'+index).value == 'N' ? 'Y' :'N';

		if (markRowForDelete == 'Y') {
			addClassName('followUpDocId' + index, "delete");
			addClassName('followUpDate' + index, "delete");
			addClassName('followUpRemarks' + index, "delete");
    	} else {
    		removeClassName('followUpDocId' + index, "delete");
    		removeClassName('followUpDate' + index, "delete");
    		removeClassName('followUpRemarks' + index, "delete");
    	}
    }

	function saveOPFollowUpDetails(){
		var visittablerows = document.getElementById("followUpDetailsTab").rows.length;
		if(visittablerows == 2){

			var followUpDate 	  	= document.getElementById("followUpDate1").value ;
			var followUpRemarks 	= document.getElementById("followUpRemarks1").value ;
			var folloUpDocId 		= document.getElementById("followUpDocId1").value ;

			if(followUpDate == "" &&  followUpRemarks == "" && folloUpDocId==""){
				alert("Please enter details to save");
				return false;
			}
		}

		if(saveFollowUpDetails()){
			document.dischargeSummaryForm.action="FollowUpDetails.do?method=saveFollowUpDetails";
			document.dischargeSummaryForm.submit();
		}
	}

function printDischargeSummary(url) {
	var printerId = document.dischargeSummaryForm.printerId.value;
	window.open(url+"&printerId="+printerId);
}
