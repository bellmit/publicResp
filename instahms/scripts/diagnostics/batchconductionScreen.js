function init(){
	resultFocus();
	isStoreExist();
	popupPrint();
	if(!validateResultsRt)
		disbleFields();
	initDisclaimerDialog();
	initImpresionACS();
	initNoGrowthTemplateACS();
	toggleCollapsiblePanel();
	initTestDetailsDialog();
	initOrganismDetailsDialog();
	initAlertReasonDialog();
	initAmendReasonDialog();
	selectCommonConductingDoctor();
}

function resultFocus(){

	var results = document.getElementsByName("dum_resultvalue");
	if ( document.getElementById("resultsTable") && document.getElementById("resultsTable").rows.length > 0 ) {
		for ( var i = 0;i<results.length;i++ ){
			if ( results[i].type != 'hidden' && !results[i].readOnly && !results[i].disabled ) {
				results[i].focus();
				break;
			}
		}
	}
}
function toggleCollapsiblePanel() {
	var noGrowthTemplates = document.getElementsByName("nogrowth_template_id");
	for(var i = 0;i<noGrowthTemplates.length;i++){
		CollapsiblePanel1[i].close();
		CollapsiblePanel2[i].close();
		if(document.getElementById("growth_exists_N"+i).checked)
			CollapsiblePanel1[i].open();
		else if(document.getElementById("growth_exists_Y"+i).checked)
			CollapsiblePanel2[i].open();
		else{
			CollapsiblePanel1[i].close();
			CollapsiblePanel2[i].close();
		}
	}
}

function validateReferenceRanges() {
	var resultsVal = document.getElementsByName("withinNormal");
	var seviarityVal = document.getElementsByName("dum_resultvalue");

	for (var i = 1;i < resultsVal.length && i < seviarityVal.length; i++) {
		var norm = resultsVal[i].value;

		if((norm == '***'|| norm == '###')) {
			alert("The result entered is beyond the improbable range, please check the results.");
			seviarityVal[i].focus();
			return false;
		}
	}
	return true;
}

function showSpecimanAdequecy(obj,index) {
	if(!empty(obj) &&  obj.value=="P") {
		document.getElementById('adequecyId'+index).style.display = 'block';
		document.getElementById('smear_received'+index).style.display = 'block';
	} else {
		document.getElementById('adequecyId'+index).style.display = 'none';
		document.getElementById('smear_received'+index).style.display = 'none';
	}
}
function IsExistImpressionName() {
	var table = document.getElementsByName('rtestId');
	for(var i=0 ;i<table.length;i++) {
    	shortImpression = document.getElementById('impression'+i);
    	var impression;
    	if (shortImpression != null && !empty(shortImpression.value)) {
    		var ajaxReqObject = newXMLHttpRequest();
			var url =cpath + "/Laboratory/editresults.do?_method=checkRecords&impression="+shortImpression.value;
			ajaxReqObject.open("POST",url.toString(), false);
			ajaxReqObject.send(null);
			if (ajaxReqObject.readyState == 4) {
				if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
					impression = ajaxReqObject.responseText;
				}
			}

			if (empty(impression)) {
				showMessage("js.laboratory.radiology.batchconduction.add.shortimpression");
				document.getElementById('impression'+i).focus();
				return false;
			}
    	}
    }
    return true;
}
function submitvalues(obj){
	var form = document.diagcenterform;
	var checkBox = form.selectTest;
	if (!validateReferenceRanges()) return false;
	var selectedPresArray = [];
	var index = 0;
	var isOneCheckisEnabled = false;
	var negitiveStockReagents = [];
    var regIndex = 0;
	var impression = '';
	var table = document.getElementsByName('rtestId');

	for(var i=0;i<table.length;i++) {
		var conductedinreportformat = document.getElementById('conductedinreportformat'+i);
		impression = document.getElementById('impression'+i);
		if (impression != null && empty(impression.value) && conductedinreportformat != null && conductedinreportformat.value == 'H') {
			document.getElementById('histo_impression_id'+i).value = '';
		} else if (impression != null && empty(impression.value) && conductedinreportformat != null && conductedinreportformat.value == 'C') {
			document.getElementById('cyto_impression_id'+i).value = '';
		}
	}

	document.diagcenterform.printerId.value = document.diagcenterform.printer.value;

    //data validations begins
		var sampleDate = form.dateOfSample;
		if(sampleDate!=null){
			if(sampleDate.length == undefined){
				var selectedSampleDate = form.dateOfSample.value;
				if(selectedSampleDate!=""){
					if(!doValidateDateField(form.dateOfSample, null)){
						form.dateOfSample.focus();
						return false;
					}
				}
			}else{
				for(i=0;i<sampleDate.length;i++){
					var selectedSampleDate = sampleDate[i].value;
					if(selectedSampleDate!=""){
						if(!doValidateDateField(sampleDate[i], null)){
							sampleDate[i].focus();
							return false;
						}
					}
				}
			}
		}

		var dateOfInvestigation = form.dateOfInvestigation;
		if(dateOfInvestigation!=null){
			if(dateOfInvestigation.length  == undefined){
				var date = dateOfInvestigation.value;
				if(date !=""){
					if(!doValidateDateField(dateOfInvestigation, null)){
						dateOfInvestigation.focus();
						return false;
					}
				}
			}else{
				for(var i=0;i<dateOfInvestigation.length;i++){
					var date =  dateOfInvestigation[i].value;
					if(date != ""){
						if(!doValidateDateField(dateOfInvestigation[i], null)){
							dateOfInvestigation[i].focus();
							return false;
						}
					}
				}
			}
		}

		var condDocMandatory = document.getElementsByName("conducting_doc_mandatory");
		var completed = document.getElementsByName("completed");
		var doctor = document.getElementsByName("doctor");
		var prescribedId = document.getElementsByName("prescribedid");
		var commonIndex = document.getElementsByName("commonIndex");
		var sampleNo = document.getElementsByName("sampleno");
		var condRprtFrmt = document.getElementsByName("conductedinreportformat");
		var resultsChk = document.getElementsByName("dum_resultvalue");
		var amendRslt = document.getElementsByName("amendment_reason");
		var atLeastOneResult = false;

	     if(resultsChk!=null){
	     	for(var j=0; j< resultsChk.length; j++){
	     		var resultsCount = 0;
	     		if(prescribedId[j].value != ''){
	     			for(var i=j+1; i<resultsChk.length; i++){
	     				if(prescribedId[i].value == '' && amendRslt[i].value == '')
	     					resultsCount++;
	     				else
	     					break;
	     			}
	     			var resultsIdx = j+resultsCount;
	     			for(var k=j+1; k <= resultsIdx; k++){
	     				if(trimAll(resultsChk[k].value) != ''){
	     					atLeastOneResult = true;
	     					break;
	     				}
	     			}
	     			 if(resultsCount != '0' && condRprtFrmt[j].value == 'N' && (completed[j].value == 'V' || completed[j].value == 'C' || completed[j].value == 'RC' || completed[j].value == 'RV') && !atLeastOneResult){
						alert("Enter At Least One Result Value for Test");
						return false;
					}
				j = j+resultsCount;
				atLeastOneResult = false;
	     		}
	     	}
	     }


		if(condDocMandatory!=null){
			for(var i =0;i<completed.length;i++){
				if(prescribedId[i].value != '' && //possible for  result rows
					(condDocMandatory[i].value == 'O' || condDocMandatory[i].value == 'C' ) &&
					(completed[i].value == 'V' || completed[i].value == 'C' || completed[i].value == 'CC') &&  doctor[i].value == ''){
					 		showMessage("js.laboratory.radiology.batchconduction.required.conductingdoctor");
					 		showTestDetails(completed[i],prescribedId[i].value,commonIndex[i].value,sampleNo[i].value);
					 		document.getElementById("tdDoctor").focus();
							return false;
					 	}

			}
		}

		var completedDropDown = document.getElementsByName("dum_completed");

	   //checkboxes active and inactive checking begins
		if(completedDropDown !=null){
		   if(completedDropDown.length == undefined){
				if(completedDropDown[i].value != 'NA' && !completedDropDown.disabled){
					isOneCheckisEnabled = true;
				}
		   }else{
				for( i=0;i<completedDropDown.length;i++ ){
					if(completedDropDown[i].value != 'NA' && !completedDropDown[i].disabled){
						isOneCheckisEnabled = true
					}
				}
			}
		}

		if(!isOneCheckisEnabled){
			showMessage("js.laboratory.radiology.batchconduction.nochangesmade");
			return false;
		}
		if (!IsExistImpressionName()) {
			return false;
		}
	//data validations ends
	if(obj.name == 'sanvprint')
		form.saveandprint.value = 'Y';
	else
		form.saveandprint.value = 'N';
	form.submit();
}

function onChangeOfCompletion(value){
	var id = "completedCheckbox"+value
	if(document.getElementById(id).checked){
		document.getElementById("dum_completed"+value).value = 'Y';
	}else{
		document.getElementById("dum_completed"+value).value = 'N';
	}
}
function openwindow(el,formatid,prescribedId,testId, revisionNumber){
	return openwindow(el,formatid,prescribedId,testId,0, revisionNumber);
}
function openwindow(el,formatid,prescribedId,testId,testDetailsId, revisionNumber){
  if(formatid != "") {
  		var row = getThisRow(el);
		var contextPath = document.diagcenterform.contextpath.value;
		var category = document.diagcenterform.category.value;
		var mrno = document.diagcenterform.mrno.value;
		document.getElementById("updatedTemplateRowIndex").value = row.rowIndex;
		var path = contextPath + "/Diagnostics/TemplatePopup.do?_method=getTemplateEditor";
		path = path + "&formatid=" + formatid + "&prescribedid=" + prescribedId +
			"&testDetailsId="+getElementByName(row,"test_details_id").value+"&testId=" + testId +
			"&updatedRow="+row.rowIndex+"&revisionNumber="+revisionNumber+"&category="+category+"&mrno="+mrno;

		document.getElementById("Save").disabled = true;
		document.getElementById("SaveAndPrint").disabled = true;
		window.open(path,'Popup_Window',"width=700, height=700,screenX=80,screenY=50,left=300,top=50,scrollbars=yes,menubar=0,resizable=yes");
	}
  return false;
}

function chekNormalRange(value){

	var obj = "range"+value;
	var checked = document.getElementById(obj).checked;
	var dobj = "withinNormal"+value;
	if(checked){
		document.getElementById(dobj).value = 'N';
	}else{
		document.getElementById(dobj).value = 'Y';
	}

}

function disbleFields(){
	var selectTestChecks = document.getElementsByName("selectTest");
	for( var i = 0; i < selectTestChecks.length; i++){
		if(selectTestChecks[i].disabled) {
			var id = (selectTestChecks[i].id).substring(5,(selectTestChecks[i].id).length);
			var divElmts = document.getElementById("div"+id).getElementsByTagName("*");
			for (var x = 0; x < divElmts.length; x++) {
				divElmts[x].disabled = true;
			}
		}
	}
}

function isStoreExist(){
var testNames = document.getElementsByName('testname');
for(var i=0; i< reExist.length; i++) {
	if(reExist[i] == true) {
		if(storeExist[i] == false) {
			var message = getString("js.diagnostics.batchconduction.departmentassociatedwith");
			message+=" ";
			message+= testNames[i].value;
			message+=" ";
			message+=getString("js.diagnostics.batchconduction.testdoesnothaveastore");
			document.getElementById('Save').disabled = true;
			document.getElementById('SaveAndPrint').disabled = true;
			document.getElementById('warnDiv').textContent = message;
			document.getElementById('warnMsgDiv').style.display = 'block';
			alert(message);
			return false;
		}
	}
}
return true;
}

function toggleDisabled(el){
	try {
		el.disabled = !el.disabled;
	}
	catch(E){
	}
	if (el.childNodes && el.childNodes.length > 0) {
		for (var x = 0; x < el.childNodes.length; x++) {
			toggleDisabled(el.childNodes[x]);
		}
	}
}

function disbleField(divId){
	var divElement = document.getElementById(divId);
	toggleDisabled(divElement);
}


function isEventEnter(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 ) {
		return true;
	}
	return false;
}

function isEventCtrlPlusEnter(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 && e.ctrlKey) {
		return true;
	}
	return false;
}

function nextFieldOnEnter(obj,index,e) {

	var resultFields = document.getElementsByName("dum_resultvalue");
	var row = getThisRow(obj);

	if ( row == undefined )
		return true;

	var rowIndex = row.rowIndex;
	//if the event is ctrl+Enter we need to insert new line.
	if (isEventCtrlPlusEnter(e)) {
		var cursorBeforepart = obj.value.substring(0, obj.selectionStart);
		var cursorAfterpart = obj.value.substring(obj.selectionEnd, obj.value.length);
		obj.value = cursorBeforepart + "\n" ;
		obj.value = obj.value + "" + cursorAfterpart;
		return true;
	}

	if (isEventEnter(e)) {
		e.preventDefault();
		for ( var i=rowIndex;i<resultFields.length;i++ ) {
			if ( resultFields[i].type != 'hidden' && !resultFields[i].disabled &&  !resultFields[i].readOnly ) {
				resultFields[i].focus();
				break;
			}
		}
	}


	return true;
}

function setSiviarity(mn_normal,mx_normal,mn_critical,mx_critical,mn_improbable,mx_improbable,
					  result,withinNormal,seviarity,resultrange,countOfRanges,expression) {

	var valueIdx=4, severityIdx=5;
	if (methodologyExists == 'Y')
		valueIdx=5, severityIdx=6;

	var row = getThisRow(result);
	var id = getRowIndex(row);
	var parsedValue = NaN;
	if (!isNaN(result.value))
		parsedValue = parseFloat(result.value);
	var childs = Dom.getChildren(row.cells[severityIdx]);
	var childs1 = Dom.getChildren(row.cells[valueIdx]);
	var r_withinnormal_el = childs[0];
	var r_seviarity_el = childs[1];
	var resultVal = childs1[0];

	var resultValueChilds = Dom.getChildren(row.cells[valueIdx]);
	var originalResultVal = resultValueChilds[1].value;
	resultValueChilds[1].value = result.value;

	if( empty(result.value) || empty(resultrange) || result.value == originalResultVal )
		return true;

	if ((mx_improbable!= '') && parsedValue > parseFloat(mx_improbable)) {
		r_withinnormal_el.value  = "###";
    	r_seviarity_el.value  = "###";
    	resultVal.style.backgroundColor = improbableResult;
	} else if ((mx_critical!= '') && parsedValue > parseFloat(mx_critical)) {
		r_withinnormal_el.value  = "##";
    	r_seviarity_el.value  = "##";
    	resultVal.style.backgroundColor = criticalResult;
	} else if ((mx_normal!= '') && parsedValue > parseFloat(mx_normal)) {
		r_withinnormal_el.value  = "#";
    	r_seviarity_el.value  = "#";
    	resultVal.style.backgroundColor = abnormalResult;
	} else if ((mn_improbable!= '') && parsedValue < parseFloat(mn_improbable)) {
		r_withinnormal_el.value  = "***";
    	r_seviarity_el.value  = "***";
    	resultVal.style.backgroundColor = improbableResult;
	} else if ((mn_critical!= '') && parsedValue < parseFloat(mn_critical)) {
		r_withinnormal_el.value  = "**";
    	r_seviarity_el.value  = "**";
    	resultVal.style.backgroundColor = criticalResult;
	} else if ((mn_normal!= '') && parsedValue < parseFloat(mn_normal)) {
		r_withinnormal_el.value  = "*";
    	r_seviarity_el.value  = "*";
    	resultVal.style.backgroundColor = abnormalResult;
	} else if ((mn_normal!= '') || (mx_normal!= '')) {
		r_withinnormal_el.value  = "Y";
    	r_seviarity_el.value  = "Y";
    	resultVal.style.backgroundColor = normalResult;
	} else {
		// no ranges defined, force the user to select
		r_withinnormal_el.value  = "";
    	r_seviarity_el.value  = "";
    	if ( expression ) {
			//do nothing
    	} else {
    		resultVal.style.backgroundColor = normalResult;
    	}
	}

	return true;
}
function getRowIndex(row) {
	return row.rowIndex - 1;
}
function getIndexedValue(name, index) {
	var obj = getIndexedFormElement(diagcenterform, name, index);
	if (obj)
		return obj.value;
	else
		return null;
}
function setIndexedValue(name, index, value) {
	var obj = getIndexedFormElement(diagcenterform, name, index);
	if (obj)
		obj.value = value;
	return obj;
}

function setSeviarity(mn_normal,mx_normal,mn_critical,mx_critical,mn_improbable,mx_improbable,
					  obj,withinNormal,seviarity,resultrange,countOfRanges,expression){
	var valueIdx=4;
		if (methodologyExists == 'Y')
			valueIdx=5;

	var childs = Dom.getChildren(getThisCell(obj));
	var r_seviarity_el = childs[1];
	r_seviarity_el.value = obj.value;

	var row = getThisRow(obj);
	var id = getRowIndex(row);
	var parsedValue = obj.value;
	var childs1 = Dom.getChildren(row.cells[valueIdx]);
	var resultVal = childs1[0];


	if ((parsedValue != '') && parsedValue == '###') {
		//resultVal.setAttribute("style","width:60px;background-color:"+improbableResult);
		resultVal.style.backgroundColor = improbableResult;
	} else if ((parsedValue != '') && parsedValue == '##') {
		//resultVal.setAttribute("style","width:60px;background-color:"+criticalResult);
		resultVal.style.backgroundColor = criticalResult;
	} else if ((parsedValue != '') && parsedValue == '#') {
		//resultVal.setAttribute("style","width:60px;background-color:"+abnormalResult);
		resultVal.style.backgroundColor = abnormalResult;
	} else if ((parsedValue != '') && parsedValue == '***') {
		//resultVal.setAttribute("style","width:60px;background-color:"+improbableResult);
		resultVal.style.backgroundColor = improbableResult;
	} else if ((parsedValue != '') && parsedValue == '**') {
    	//resultVal.setAttribute("style","width:60px;background-color:"+criticalResult);
    	resultVal.style.backgroundColor = criticalResult;
	} else if ((parsedValue != '') && parsedValue == '*') {
    	resultVal.style.backgroundColor = abnormalResult;
	} else if ((parsedValue != '') && parsedValue == 'Y') {
		if( expression )
			resultVal.style.backgroundColor = '';
		else
			resultVal.style.backgroundColor = normalResult;
	} else {
		// no ranges defined, force the user to select
		if( expression )
			resultVal.style.backgroundColor = '';
		else
			resultVal.style.backgroundColor = normalResult;
	}

}

/**
	To mark all tests as completed
**/
function markAllComplete(){
	var completed = document.getElementsByName("dum_completed");
	var markAll = document.diagcenterform.markAllCompleted.checked;

	for(var i = 0;i<completed.length;i++){
		if(completed[i].value != 'NA'){
			if(markAll){
				completed[i].value = "C";
				if(completed[i].value != 'C')
					completed[i].value = "RC";
			}else{
				completed[i].selectedIndex = completed[i].options[0].style.display == 'none' ? 1 : 0;
			}
			setCompletedStatus(completed[i]);
		}
	}
}

function markAllValidate(ckel){
	var completed = document.getElementsByName("dum_completed");
	if( !document.diagcenterform.markAllCompleted.disabled )
		document.diagcenterform.markAllCompleted.checked = ckel.checked;

	for(var i = 0;i<completed.length;i++){
		if(completed[i].value != 'NA'){
			if(ckel.checked){
				completed[i].value = "V";
				if(completed[i].value != 'V')
					completed[i].value = 'RV';
			}else{
				if(document.diagcenterform.markAllCompleted.checked &&
					document.diagcenterform.markAllCompleted.disabled){
					completed[i].value = "C";
					if(completed[i].value != 'C')
						completed[i].value = "RC";
				}else
					completed[i].selectedIndex = completed[i].options[0].style.display == 'none' ? 1 : 0;
			}

			setCompletedStatus(completed[i]);
		}
	}

}
function showDiscliamerDialog(btnObj,index){

	var row = getThisRow(btnObj,'TR');
	var id = getRowIndex(row);
	document.getElementById("discliamerDialog").display = 'block';
	document.getElementById("eResultsDiscliamer").value = ""
	document.getElementById("eResultsDiscliamer").value = getElementByName(row, "resultDisclaimer" ).value;
	document.getElementById("eRemarks").value = ""
	document.getElementById("eRemarks").value = getElementByName(row, "remarks" ).value;
	document.getElementById("editedResultIdx").value = index ;
	discliamerDialog.cfg.setProperty("context", [btnObj, "tr", "br"], false);
	discliamerDialog.show();
}


function initDisclaimerDialog() {

	var dialogDiv = document.getElementById("discliamerDialog");
	dialogDiv.style.display = 'block';
	discliamerDialog = new YAHOO.widget.Dialog("discliamerDialog",{
			width:"350px",
			text: "Result Disclaimer",
			context :["otherDetails", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:onCancel,
	                                                scope:discliamerDialog,
	                                                correctScope:true } );
	discliamerDialog.cfg.queueProperty("keylisteners", escKeyListener);
	discliamerDialog.render();

}


function onCancel() {
	discliamerDialog.hide();
}

function setDisclaimer(){
	document.getElementById("resultDisclaimer"+document.getElementById("editedResultIdx").value).value =
		 document.getElementById("eResultsDiscliamer").value;
	 document.getElementById("remarks"+document.getElementById("editedResultIdx").value).value =
		 document.getElementById("eRemarks").value;
	discliamerDialog.hide();
}
var impIndex = 0;
function initImpresionACS(){
	var impresions = document.getElementsByName("impression_id");
	for(var i = 0;i<impresions.length;i++){
		impIndex = i;
		Insta.initImpressionAcSearch(cpath, 'impression'+i, 'impresContainer'+i, 'active',impIndex, null, null);
	}
}

var autoComp = new Array();
Insta.initImpressionAcSearch = function(cpath, searchComp, searchDropdown, status,impressionIndex,
		selectCallback, invalidCallback, autoSnapContainer) {
	var ds = new YAHOO.util.XHRDataSource(cpath + "/Laboratory/editresults.do");
	ds.scriptQueryAppend = "_method=findImpressionJson&searchType=impressionId&status=" + status;
	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	ds.responseSchema = {
		resultsList : "result",
		fields : [  {key : "short_impression"},
					{key : "impression_id"},
					{key : "impression_details"} ]
	};
	autoComp[impressionIndex] = new YAHOO.widget.AutoComplete(searchComp, searchDropdown, ds);

	autoComp[impressionIndex].minQueryLength = 1;
	autoComp[impressionIndex].animVert = false;
	autoComp[impressionIndex].maxResultsDisplayed = 50;
	autoComp[impressionIndex].forceSelection = false;
	autoComp[impressionIndex].resultTypeList = false;
	autoComp[impressionIndex].autoSnapContainer = !autoSnapContainer;
	autoComp[impressionIndex]._bItemSelected = true;
	autoComp[impressionIndex].formatResult = function(oResultData, sQuery, sResultMatch) {
		var impression = oResultData;
		var queryComps = sQuery.split(" ", 2);

		var reStarts = [];
		var reEnds = [];


		for (var i=0; i<queryComps.length; i++) {
			var escapedComp = Insta.escape(queryComps[i]);
			reStarts[i] = new RegExp('^' + escapedComp, 'i');
			reEnds[i] =   new RegExp(escapedComp + '$', 'i');
		}

		var details = highlight(impression.short_impression, reEnds);
		return details;
	}

	if (!empty(selectCallback))
		autoComp[impressionIndex].itemSelectEvent.subscribe(selectCallback);

	if (!empty(invalidCallback))
		autoComp[impressionIndex].selectionEnforceEvent.subscribe(invalidCallback);

	autoComp[impressionIndex].itemSelectEvent.subscribe(onSelectImpression);
	return autoComp[impressionIndex];
}

var alertDialog;

function initAlertReasonDialog() {

	var dialogDiv = document.getElementById("alertDialog");
	dialogDiv.style.display = 'block';
	alertDialog = new YAHOO.widget.Dialog("alertDialog",{
			width:"450px",
			context :["", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeAlertDialog,
	                                                scope:alertDialog,
	                                                correctScope:true } );
	alertDialog.cfg.queueProperty("keylisteners", escKeyListener);
	alertDialog.render();
	//alertDialog.cancelEvent.subscribe(onTestDialogCancel);

}

function closeAlertDialog() {
	alertDialog.hide();
}
var impreDetails ;
function showAlertDialog(index,impressionDetails) {
	var button = document.getElementById("showAlertDialog"+index);
	impreDetails = impressionDetails;
	alertDialog.cfg.setProperty("context",[button, "tl", "bl"], false);
	alertDialog.show();
}

function handleNo() {
	alertDialog.cancel();
}

function handleYes() {
	var id = document.getElementById('alertDialogId').value;
	document.getElementById('impressionDetails'+id).value = impreDetails;
	alertDialog.cancel();
}

function onSelectImpression(sType,aArgs){
	var rowIndex = (aArgs[0].getInputEl().getAttribute("id")).replace("impression","");
	var impressionId = aArgs[2]["impression_id"];
	var impressionDetails = aArgs[2]["impression_details"];
	var conductionFormat = document.getElementById('conductedinreportformat'+rowIndex);
	if (conductionFormat != null && conductionFormat.value == 'H') {
		document.getElementById('histo_impression_id'+rowIndex).value = impressionId;
	} else if (conductionFormat != null && conductionFormat.value == 'C') {
		document.getElementById('cyto_impression_id'+rowIndex).value = impressionId;
	}
	var impressionDetailsUI = document.getElementById("impressionDetails"+rowIndex).value;
	var impression = '';
	var ajaxReqObject = newXMLHttpRequest();
	var url =cpath + "/Laboratory/editresults.do?_method=checkRecords&impressionDetails="+impressionDetailsUI ;
	ajaxReqObject.open("POST",url.toString(), false);
	ajaxReqObject.send(null);
	if (ajaxReqObject.readyState == 4) {
		if ( (ajaxReqObject.status == 200) && (ajaxReqObject.responseText!=null) ) {
			impression = ajaxReqObject.responseText;
		}
	}
	if (empty(impression) && !empty(impressionDetailsUI)) {
		document.getElementById("alertDialogId").value = rowIndex;
		showAlertDialog(rowIndex,impressionDetails);
	}

}

/*
var oAutoComp = new Array();
function initImpressionsAutoComplete(impressionIndex) {
	var dataSource;

	dataSource = new YAHOO.widget.DS_JSArray(impressionsArray);
	 	dataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "short_impression"},
					{key : "impression_id"},
					{key : "impression_details"} ]
 	};

	oAutoComp[impressionIndex] = new YAHOO.widget.AutoComplete('impression'+impressionIndex, 'impresContainer'+impressionIndex, dataSource);
	oAutoComp[impressionIndex].maxResultsDisplayed = 25;
	oAutoComp[impressionIndex].allowBrowserAutocomplete = false;
	oAutoComp[impressionIndex].prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp[impressionIndex].minQueryLength = 1;
	oAutoComp[impressionIndex].forceSelection = false;
	oAutoComp[impressionIndex].animVert = false;
	oAutoComp[impressionIndex].filterResults = Insta.queryMatchWordStartsWith;
	oAutoComp[impressionIndex].formatResult = Insta.autoHighlightWordBeginnings;
	oAutoComp[impressionIndex].itemSelectEvent.subscribe(onSelectImpression);
}*/

var noGrowthIndex = 0;
function initNoGrowthTemplateACS(){
	var noGrowthTemplates = document.getElementsByName("nogrowth_template_id");
	for(var i = 0;i<noGrowthTemplates.length;i++){
		noGrowthIndex = i;
		initNoGrowthTemplateAutoComplete(i);
		initGrowthTemplateAutoComplete(i);
	}
}

var oNoGrowthAutoComp = new Array();
function initNoGrowthTemplateAutoComplete(noGrowthIndex) {
	var grdataSource;
	var noGrAutoComp = oNoGrowthAutoComp[noGrowthIndex];

	grdataSource = new YAHOO.widget.DS_JSArray(noGrowthTemplatesArray);
	 	grdataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "nogrowth_template_name"},
					{key : "nogrowth_template_id"},
					{key : "nogrowth_template_detailed"} ]
 	};
	noGrAutoComp = new YAHOO.widget.AutoComplete('nogrowth_template'+noGrowthIndex,
			'nogrowthTemplateContainer'+noGrowthIndex, grdataSource);
	noGrAutoComp.maxResultsDisplayed = 25;
	noGrAutoComp.allowBrowserAutocomplete = false;
	noGrAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	noGrAutoComp.minQueryLength = 1;
	noGrAutoComp.forceSelection = true;
	noGrAutoComp.animVert = false;
	noGrAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	noGrAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	noGrAutoComp.itemSelectEvent.subscribe(onSelectNoGrowthTemplate);
}

function onSelectNoGrowthTemplate(sType,aArgs){
	var rowIndex = (aArgs[0].getInputEl().getAttribute("id")).replace("nogrowth_template","");
	var noGrowthTemplate = aArgs[2];
	document.getElementById("nogrowth_template_id"+rowIndex).value = noGrowthTemplate[1];
	document.getElementById("nogrowth_report_comment"+rowIndex).value = noGrowthTemplate[2];

}

var oGrowthAutoComp = new Array();
function initGrowthTemplateAutoComplete(noGrowthIndex) {
	var grdataSource;
	var grAutoComp = oNoGrowthAutoComp[noGrowthIndex];

	grdataSource = new YAHOO.widget.DS_JSArray(growthTemplatesArray);
	 	grdataSource.responseSchema = {
		resultsList : "result",
		fields : [  {key : "growth_template_name"},
					{key : "growth_template_id"},
					{key : "growth_template_detailed"} ]
 	};
	grAutoComp = new YAHOO.widget.AutoComplete('growth_template'+noGrowthIndex,
			'growthTemplateContainer'+noGrowthIndex, grdataSource);
	grAutoComp.maxResultsDisplayed = 25;
	grAutoComp.allowBrowserAutocomplete = false;
	grAutoComp.prehighlightClassName = "yui-ac-prehighlight";
	grAutoComp.minQueryLength = 1;
	grAutoComp.forceSelection = true;
	grAutoComp.animVert = false;
	grAutoComp.filterResults = Insta.queryMatchWordStartsWith;
	grAutoComp.formatResult = Insta.autoHighlightWordBeginnings;
	grAutoComp.itemSelectEvent.subscribe(onSelectGrowthTemplate);
}

function onSelectGrowthTemplate(sType,aArgs){
	var rowIndex = (aArgs[0].getInputEl().getAttribute("id")).replace("growth_template","");
	var noGrowthTemplate = aArgs[2];
	document.getElementById("growth_template_id"+rowIndex).value = noGrowthTemplate[1];
	document.getElementById("growth_report_comment"+rowIndex).value = noGrowthTemplate[2];

}

function changeOrganism(orgGrpEL){
	var organism_id = document.getElementById("eOrganism_id");
	var filteredOrganismList = filterList(microOrganismArray, "org_group_id", orgGrpEL.value);
	loadSelectBox(organism_id, filteredOrganismList, 'organism_name', 'organism_id', '--Select--', '');
}

function changeABSTPanel(orgGrpEL){
	var abstPanel = document.getElementById("eAbst_panel_id");
	var filteredABSTPanelsList = filterList(microAbstPanelArray, "org_group_id", orgGrpEL.value);
	loadSelectBox(abstPanel, filteredABSTPanelsList, 'abst_panel_name', 'abst_panel_id', '--Select--', '');
}

function fillAntibiotics(abstPanelEL){
	var table = document.getElementById("antiBoiticTable");
	clearAntibioticTable();
	var filteredAntiBioticList = filterList(microAbstAntiBioticArray, "abst_panel_id", abstPanelEL.value);
	var tr;
	var cell;
	for(var i =0;i<filteredAntiBioticList.length;i++){
		tr = document.createElement("TR")
		cell = document.createElement("TD");
		cell.innerHTML = '<label id="eAntibioticNameLable">'+filteredAntiBioticList[i].antibiotic_name+'</label>'+
						 '<input type="hidden" name="eAntibiotic_name" id="eAntibiotic_name" value="'+filteredAntiBioticList[i].antibiotic_name+'"'+
						 '<input type="hidden" name="eAntibiotic_id" id="eAntibiotic_id" value="'+filteredAntiBioticList[i].antibiotic_id+'"';
		tr.appendChild(cell);
		cell = document.createElement("TD");
		cell.innerHTML = '<input type="text" name="eAnti_results" id="eAnti_results"/>';

		tr.appendChild(cell);
		cell = document.createElement("TD");
		cell.innerHTML = '<select name="eSusceptibility" id="eSusceptibility" class="dropdown"/>'+
						 '<option value="N">--Select--</option> '+
						 '<option value="R">RESISTANT</option> '+
						 '<option value="I">INTERMEDIATE</option>'+
						 '<option value="S">SENSITIVE</option>'+
						 '</select>';
		tr.appendChild(cell);
		table.appendChild(tr);
	}

}

function clearAntibioticTable() {
	var table = document.getElementById('antiBoiticTable');
	var numRows = table.rows.length;
	var lastRowIndex = numRows-1;
	for (var index = lastRowIndex; index >0; index--) {
		var row = table.rows[index];
		row.parentNode.removeChild(row);
	}
}
function checkLength(obj,len,field){
	if( obj.value.length  > len ){
		showMessage("Max "+len+" characters are allowed in "+field);
		obj.value = (obj.value).substring(0,200);
		obj.focus();
		return false;
	}
	return true;
}

function validateGrowth(ckEl,index){
	if(ckEl.checked){
		if(ckEl.value == 'Y' && document.getElementById("growth_exists_N"+index).checked){
			showMessage("js.laboratory.radiology.batchconduction.sametestnothave.growth.nogrowth");
			ckEl.checked = false;
			ckEl.focus();
		}else if(ckEl.value == 'N' && document.getElementById("growth_exists_Y"+index).checked){
			showMessage("js.laboratory.radiology.batchconduction.sametestnothave.growth.nogrowth");
			ckEl.checked = false;
			ckEl.focus();
		}
	}
}

var testDetailsDialog;

function initOrganismDetailsDialog() {

	var dialogDiv = document.getElementById("organismDetailsDialog");
	dialogDiv.style.display = 'block';
	organismDetailsDialog = new YAHOO.widget.Dialog("organismDetailsDialog",{
			width:"800px",
			text: "Organism Details",
			context :["loadDialog", "tr", "br"],
			visible:false,
			modal:true,
			constraintoviewport:true
		});
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:closeOrganismDialog,
	                                                scope:organismDetailsDialog,
	                                                correctScope:true } );
	organismDetailsDialog.cfg.queueProperty("keylisteners", escKeyListener);
	organismDetailsDialog.render();
}

function subscribeEscKeyEvent(dialog) {
	var kl = new YAHOO.util.KeyListener(document, { keys:27 },
		{ fn:dialog.cancel, scope:dialog, correctScope:true } );
	dialog.cfg.setProperty("keylisteners", kl);
}

function closeOrganismDialog() {
	organismDetailsDialog.cancel();
}

var addoredit = '';
function showOrganismDialog(obj,index,mode){
	clearOrganismDialog();
	document.getElementById("editedOrganismTableIndex").value = index;
	addoredit = mode;
	if(mode == 'E'){
		var row = getThisRow(obj);
		var rowIndex = row.rowIndex-1;

		var childs = Dom.getChildren(row.cells[0]);
		document.getElementById("eOrg_group_id").value = childs[2].value;
		document.getElementById("eOrganism_id").value = childs[3].value;
		document.getElementById("eAbst_panel_id").value = childs[4].value;
		document.getElementById("eComments").value = childs[5].value;
		document.getElementById("eResistance_marker").value = childs[6].value;
		if(childs[5].value != ''){
			fillAntibiotics(childs[5]);
		}

		var eAntibiotic_id = document.getElementsByName("eAntibiotic_id");
		var eAntibiotic_name = document.getElementsByName("eAntibiotic_name");
		var eAnti_results = document.getElementsByName("eAnti_results");
		var eSusceptibility = document.getElementsByName("eSusceptibility");

		var antibiotic_id = document.getElementsByName("antibiotic_id");
		var antibiotic_name = document.getElementsByName("antibiotic_name");
		var anti_results = document.getElementsByName("anti_results");
		var susceptibility = document.getElementsByName("susceptibility");

		for(var i = 0;i<eAntibiotic_id.length;i++){
			var antibioticRow = getThisRow(eAntibiotic_id[i]);
			row.getElementsByTagName("label").innerHTML = antibiotic_name[rowIndex].value;
			eAntibiotic_id[i].value = antibiotic_id[rowIndex].value;
			eAntibiotic_name[i].value = antibiotic_name[rowIndex].value;
			eAnti_results[i].value = anti_results[rowIndex].value;
			eSusceptibility[i].value = susceptibility[rowIndex].value;
			rowIndex++;
		}
	}
	organismDetailsDialog.cfg.setProperty("context", [obj, "tr", "br"], false);
	organismDetailsDialog.show();
}

function clearOrganismDialog(){
	document.getElementById("eOrg_group_id").value = '';
	document.getElementById("eOrganism_id").value = '';
	document.getElementById("eAbst_panel_id").value = '';
	document.getElementById("eComments").value = '';
	document.getElementById("eResistance_marker").value = '';
	clearAntibioticTable();
}

function onOkOrgDialog(){

	var org_prescribed_id = document.getElementsByName("org_prescribed_id");
	var org_group_id = document.getElementsByName("org_group_id");
	var abst_panel_id = document.getElementsByName("abst_panel_id");
	var organism_id = document.getElementsByName("organism_id");
	var comments = document.getElementsByName("comments");
	var resistance_marker = document.getElementsByName("resistance_marker");

	if(addoredit == 'A'){
		var index = document.getElementById("editedOrganismTableIndex").value;
		var table = document.getElementById("organismTable"+index);

		var numOfRows = table.rows.length;
		var templateRow = table.rows[numOfRows-1];
		var row = templateRow.cloneNode(true);

		row.style.display = 'table-row';
		table.tBodies[0].insertBefore(row, templateRow);

		var childs = Dom.getChildren(row.cells[0]);
		var org_prescibed_id = childs[0].value;;

		//set organism grop,organism,abst panel,comments,resistent marker details
		setNodeText(row.cells[0], empty(getSelValue(document.getElementById("eOrg_group_id"))) ? '' : getSelText(document.getElementById("eOrg_group_id")));
		setNodeText(row.cells[1], empty(getSelValue(document.getElementById("eOrganism_id"))) ? '' : getSelText(document.getElementById("eOrganism_id")));
		setNodeText(row.cells[2], empty(getSelValue(document.getElementById("eAbst_panel_id"))) ? '' : getSelText(document.getElementById("eAbst_panel_id")));
		setNodeText(row.cells[3], document.getElementById("eResistance_marker").value);
		setNodeText(row.cells[4], document.getElementById("eComments").value, 32);

		var eAntibiotic_id = document.getElementById("eAntibiotic_id");
		var eAntibiotic_name = document.getElementById("eAntibioticNameLable");
		var eAnti_results = document.getElementById("eAnti_results");
		var eSusceptibility = empty(getSelValue(document.getElementById("eSusceptibility"))) ? '' : getSelText(document.getElementById("eSusceptibility"));
		setNodeText(row.cells[5], eAntibiotic_name.textContent, 32);
		setNodeText(row.cells[6], eAnti_results.value);
		setNodeText(row.cells[7], eSusceptibility);

		row.cells[0].appendChild(makeHidden('org_prescribed_id', 'org_prescribed_id', org_prescibed_id));
		row.cells[0].appendChild(makeHidden('antibioticRow', 'antibioticRow', "N"));
		row.cells[0].appendChild(makeHidden('org_group_id', 'org_group_id', document.getElementById("eOrg_group_id").value));
		row.cells[0].appendChild(makeHidden('organism_id', 'organism_id', document.getElementById("eOrganism_id").value));
		row.cells[0].appendChild(makeHidden('abst_panel_id', 'abst_panel_id', document.getElementById("eAbst_panel_id").value));
		row.cells[0].appendChild(makeHidden('comments', 'comments', document.getElementById("eComments").value));
		row.cells[0].appendChild(makeHidden('resistance_marker', 'resistance_marker', document.getElementById("eResistance_marker").value));

		// set  antibiotic details


	/*	for(var i =0;i<eAntibiotic_id.length;i++){
				var cell = row.cells[4];
				cell.innerHTML = ' "'+eAntibiotic_name[i].value+'" '+
							'<input type="text" name="anti_results" value="'+eAnti_results[i].value+'"/>'+
							'<insta:selectoptions name="susceptibility" optexts="RESISTANT,INTERMEDIATE,SENSITIVE"'+
							'opvalues="R,I,S" dummyvalue="--Select--" value="'+eSusceptibility[i].value+'" />'+
							'<br/>';
				row.appendChild(cell);

				cell = document.createElement('TD');
				row.appendChild(cell);

				cell = document.createElement('TD');
				row.appendChild(cell);

		}*/
		document.getElementById("fieldSet"+index).style.height = "";
	}else{
	}
	organismDetailsDialog.hide();
}
function validateTestDetailsEdit(){
	if(!doValidateDateField(document.getElementById("tdTestDate"))) return false;

	if(!doValidateTimeField(document.getElementById("tdTestTime"))) return false;

	 if(sampleFlow == 'N')
		if(!doValidateDateField(document.getElementById("sampleDate"))) return false;

	return true;
}

function setCompletedStatus(dumCompleteEl){
	var cell = getThisCell(dumCompleteEl);
	var completeElChilds = Dom.getChildren(cell);
	completeElChilds[1].value = dumCompleteEl.value;
}

function signOffReports(printneeded){
	var chekBox = document.diagcenterform.signoff;
	var checked = false;
	if (chekBox.checked) checked = true;
	if (checked) {
		if (!validateReferenceRanges()) return false;
	}
	if (!checked) {
		showMessage("js.laboratory.radiology.batchconduction.select.report");
		return false;
	}

	var category = document.getElementById("category").value == 'DEP_LAB' ?'Laboratory' :'Radiology';
	document.diagcenterform.action = cpath+"/"+category+"/editresults.do?_method=signOffReports&printNeeded="+printneeded;
	document.diagcenterform.submit();
}

function selectAllReports(allChkBx){
	var signOffEls = document.getElementsByName("signoff");
	for(var i =0;i<signOffEls.length;i++){
		signOffEls[i].checked = allChkBx.checked;
	}
}

function setDoctor(condDoctor){
	var row = getThisRow(condDoctor);
	var hiddenDoctorEl = Dom.getChildren(row.cells[1]);
	hiddenDoctorEl[1].value = condDoctor.value;
}

function setResultsValue(dumResult){
	var cell = getThisCell(dumResult);
	var hiddenResultValEl = Dom.getChildren(cell);
	hiddenResultValEl[1].value = dumResult.value;
}

function setCommonConductingDoctor(commonCondDoctorEL){
	var hiddenDoctorEls = document.getElementsByName("doctor");
	var dumCompletedEl = document.getElementsByName("dum_completed");
	var prescribedId = document.getElementsByName("prescribedid");
	var condDocMandatory = document.getElementsByName("conducting_doc_mandatory");
	var category = document.getElementById("category").value;
	var editableConductingDocFld = true;
	for(var i =0 ;i<hiddenDoctorEls.length;i++){
		editableConductingDocFld = false;
		for(var j =0;j<testandresultsJSON.length;j++){
			if(testandresultsJSON[j].prescribedId == prescribedId[i].value){
			    if(category == 'DEP_RAD'){
			       if((condDocMandatory[i].value == 'C' || condDocMandatory[i].value == 'O') &&  empty(hiddenDoctorEls[i].value))
			        editableConductingDocFld = (testandresultsJSON[j].condctionStatus == 'N' || testandresultsJSON[j].condctionStatus == 'P' || testandresultsJSON[j].condctionStatus == 'MA' || testandresultsJSON[j].condctionStatus == 'CR' || testandresultsJSON[j].condctionStatus == 'CC' || testandresultsJSON[j].condctionStatus == 'TS');
			        else
					editableConductingDocFld = (testandresultsJSON[j].condctionStatus == 'N' || testandresultsJSON[j].condctionStatus == 'MA');
                    }
				else{
				    editableConductingDocFld = (testandresultsJSON[j].condctionStatus == 'N' || testandresultsJSON[j].condctionStatus == 'P');
				  }
				if(editableConductingDocFld)
				break;
			}
		}
		if(editableConductingDocFld){
			hiddenDoctorEls[i].value = commonCondDoctorEL.value;
		}
	}
}

function selectCommonConductingDoctor() {
	var hiddenDoctorEls = document.getElementsByName("doctor");
	var singleDoctor = true;
	var doctor = null;
	for (var i=0; i<hiddenDoctorEls.length; i++) {
		doctor = hiddenDoctorEls[0].value;
		if (doctor != hiddenDoctorEls[i].value && hiddenDoctorEls[i].value != '') {
			singleDoctor = false;
			break;
		}
	}

	if (singleDoctor) {
		document.getElementById('commonConductingDoctor').value = doctor;
	}
	var isCondDocMandatory = true;
	var condDocMandatory = document.getElementsByName("conducting_doc_mandatory");
	var prescribedId = document.getElementsByName("prescribedid");
	
	if(condDocMandatory != null){
		for(var k =0; k<condDocMandatory.length; k++){
			if(prescribedId[k].value != '' && ! (condDocMandatory[k].value == 'O' || condDocMandatory[k].value == 'C' )) {
				isCondDocMandatory = false;
				break;
			}
		}
	}
	
	// Defaulting to single doctor
	if((conductingDoctorsLenght == 1 && ! reportID) && isCondDocMandatory ) {
		document.getElementById('commonConductingDoctor')[1].selected = true;
		setCommonConductingDoctor(document.getElementById('commonConductingDoctor'));
	}
}

function enableResultEntry() {
	var resultslbl = document.getElementsByName("result_lbl_check");
	var resultsVal = document.getElementsByName("dum_resultvalue");
	for (var i = 1; i < resultsVal.length; i++) {
		if(!empty(document.getElementById("result_lbl_check"+i)) && document.getElementById("result_lbl_check"+i) != undefined) {
			if (document.getElementById("result_lbl_check"+i).checked) {
				document.getElementById("script"+i).style.backgroundColor = '';
				document.getElementById("script"+i).disabled = false;
				if (document.getElementById("calc_res_expr"+i) != '') {
					document.getElementById("calc_res_expr"+i).value = "N";
				} else {
					document.getElementById("calc_res_expr"+i).value = "";
				}
			} else {
				document.getElementById("script"+i).disabled = true;
				if (document.getElementById("calc_res_expr"+i) != '') {
					document.getElementById("calc_res_expr"+i).value = "Y";
				} else {
					document.getElementById("calc_res_expr"+i).value = "";
				}
			}
		}
	}
}

/*TODO: Can we replace the above function to this
 *
 *
 * function enableResultEntry1(obj) {
	var rowObj = getThisRow(obj);
	if (rowObj.checked) {
		getElementByName(rowObj, 'calc_res_expr').setAttribute = ("value",  "N");
		getElementByName(rowObj, 'dum_resultvalue').removeAttribute = ("readonly", "false");
		getElementByName(rowObj, 'dum_resultvalue').style.backgroundColor = '';
	} else {
		getElementByName(rowObj, 'calc_res_expr').value = 'Y';
		getElementByName(rowObj, 'dum_resultvalue').readOnly = true;
		getElementByName(rowObj, 'dum_resultvalue').style.backgroundColor = '';
	}
}*/
