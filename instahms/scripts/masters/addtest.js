function changeElsColor(index, obj) {

		var row = document.getElementById("formatTable").rows[index];
		var trObj = getThisRow(obj);
		if(document.getElementById('hl7_interface')) {
			var trashimgObj = trObj.cells[14].getElementsByTagName("img")[0];
		} else {
			var trashimgObj = trObj.cells[13].getElementsByTagName("img")[0];
		}
		var resultlableId = getElementByName(trObj, 'resultlabel_id').value;
		var tab = getThisTable(obj);
		var parts = trObj.id.split('row');
		var index = parseInt(parts[1])+1;

		var markRowForDelete = document.getElementById('selectedrow'+index).value == 'false' ? 'true' : 'false';
		document.getElementById('selectedrow'+index).value = document.getElementById('selectedrow'+index).value == 'false' ? 'true' :'false';

		if (markRowForDelete == 'true') {
			trashimgObj.src = cpath+'/icons/undo_delete.gif';
		} else {
			trashimgObj.src = cpath+'/icons/delete.gif';
		}

	/*	if (markRowForDelete == 'true') {
			addClassName(trObj, 'cancelled');
	   	}
	   	else {
			removeClassName(trObj, 'cancelled');
	   	}*/

	   	return false;
	}

function removeBottomBorder(index) {

   		addClassName('resultlabel' + index, 'previousEl');
   		addClassName('units' + index, 'previousEl');
   		addClassName('remarks' + index, 'previousEl');
   		addClassName('order' + index, 'previousEl');
   		addClassName('expression' + index, 'previousEl');
    }

function getdetails(){
	var ddeptid = 	document.addtest.ddept_id;
	loadSelectBox(ddeptid,jdiagdepartments,'ddept_name','ddept_id');
}


function loadSelectBox(selectBox, itemList,dispNameVar, valueVar){
	// clearset the size of the select box
	selectBox.length = itemList.length + 1;
	selectBox.disabled = false;
	index = 1;
	for (var i=0; i<itemList.length; i++) {
		var item = itemList[i];
		selectBox.options[index].text = item[dispNameVar];
		selectBox.options[index].value = item[valueVar];
		index++;
	}
}

function checkformat(){
	var checkedvalue  = getRadioSelection(document.addtest.reportGroup);
	if(checkedvalue=='T'){
		document.getElementById('formatdiv').style.display = 'block';
		document.getElementById('reportvalues').style.display = 'none';
		document.getElementById("resultsValidation").disabled = true;
		if (document.getElementById('showOrHideCenterApplicability') != null) {
			document.getElementById('showOrHideCenterApplicability').style.display ='none';
		}
		if (document.getElementById('showOrHideResultRanges') != null) {
			document.getElementById('showOrHideResultRanges').style.display ='none';
		}
	}else if(checkedvalue=='V'){
		document.getElementById('formatdiv').style.display = 'none';
		document.getElementById('reportvalues').style.display = 'block';
		document.getElementById("resultsValidation").disabled = false;
		if (document.getElementById('showOrHideCenterApplicability') != null) {
			document.getElementById('showOrHideCenterApplicability').style.display ='block';
		}
		if (document.getElementById('showOrHideResultRanges') != null) {
			document.getElementById('showOrHideResultRanges').style.display ='block';
		}
	}else{
		document.getElementById('reportvalues').style.display = 'none';
		document.getElementById("resultsValidation").disabled = true;
		document.getElementById('formatdiv').style.display = 'none';
		if (document.getElementById('showOrHideCenterApplicability') != null) {
			document.getElementById('showOrHideCenterApplicability').style.display ='none';
		}
		if (document.getElementById('showOrHideResultRanges') != null) {
			document.getElementById('showOrHideResultRanges').style.display ='none';
		}
	}

}

function submitValues(){

	/*if (document.getElementById('statId').checked == true) {
		document.getElementById('stat').value = 'Y';
	} else {
		document.getElementById('stat').value = 'N';
	}*/
	
	if(validateForm()){
			innerHtml();
			return true;
	}else{
		return false;
	}
}

function validateForm(){
	var checkedvalue  = getRadioSelection(document.addtest.reportGroup);

	var testname = trim(document.getElementById('test_name').value);
	var specimen = trim(document.addtest.specimen.value);
	//var diagcode = trim(document.addtest.diagCode.value)
//	var statCharge = trim(document.addtest.statCharge.value)
//	var scheduleCharge = trim(document.addtest.scheduleCharge.value)

	if (document.addtest.ddept_id.value==""){
		alert('Department is required');
		document.addtest.ddept_id.focus();
		return false;
	}
	if(testname==""){
		alert('Test Name is required');
		document.addtest.test_name.focus();
		return false;
	} else {
		document.addtest.test_name.value = testname;
	}

	if (document.getElementById('service_group_id').selectedIndex==0) {
		alert("Service Group is required");
		document.getElementById('service_group_id').focus();
		return false;
	}
	if (document.getElementById('test_duration').value === "" 
		|| document.getElementById('test_duration').value === null 
		|| document.getElementById('test_duration').value <= 0){
		alert("Test Duration is Required");
		document.getElementById('test_duration').focus();
		return false;
	}
	if (document.getElementById('test_duration').value > 300) {
		alert("Test Duration cannot exceed 300 min");
		document.getElementById('test_duration').focus();
		return false;
	}
	if (document.getElementById('serviceSubGroupId').selectedIndex==0) {
		alert("Service Sub Group is required");
		document.getElementById('serviceSubGroupId').focus();
		return false;
	}

	if (neworedit == 'new') {
		for(i=0; i<testLists.length; i++){
			var item = testLists[i];
			if((item["ddept_id"] == document.addtest.ddept_id.value) &&(item["test_name"] == document.addtest.test_name.value)){
				alert("Test Already Exists");
				return false;
			}
		}
	}
  /*
	if(document.addtest.diagCode.value==""){
		alert('Test Code is required');
		document.addtest.diagCode.focus();
		return false;
	}*/

	if(document.addtest.sample_needed.selectedIndex==0){
		alert('Sample Need is required');
		document.addtest.sample_needed.focus();
		return false;
	}

	if(document.getElementById("mandate_additional_info").value == 'O'){
		if (document.addtest.test_additional_info.value.trim() == '') {
			alert('Please enter the Additional Test Information Remarks for the test');
			document.addtest.test_additional_info.focus();
				return false;
		}
	}
	
	var resultsEntryApplicable = document.getElementsByName('results_entry_applicable');
	var conductingDocMandatoryOption = document.getElementById('conducting_doc_mandatory').
				options[document.getElementById('conducting_doc_mandatory').selectedIndex].value;

	for (var i=0; i<resultsEntryApplicable.length; i++) {
		if (resultsEntryApplicable[i].checked && resultsEntryApplicable[i].value == 'false') {
			if (conductingDocMandatoryOption == 'C') {
				alert('Please select conducting doctor required at Order and Conduction level \n for the result entry not required test');
				document.getElementById('conducting_doc_mandatory').focus();
				return false;
			}
		}
	}

	var sampleneed = document.addtest.sample_needed.options[document.addtest.sample_needed.selectedIndex].value;
	if(sampleneed=='y')
		document.addtest.specimen.value = specimen;

	var repFormats = document.getElementsByName("reportGroup");
	if(repFormats[1].checked){

		var myTable = document.getElementById('formatTable');
		var length = myTable.rows.length-1;    //2

		for (i=1; i< length; i++){

			if(!document.getElementById("selectedrow"+i).value == 'true'){
				if(document.getElementById("resultlabel"+i).value == ""){
					alert("Enter Result Label Name or Select Empty Label(s) to delete");
					return false;
				}

				if(document.getElementById("order"+i).value == ""){
					alert("Display order is required for Result Labels")
					return false;
				}
			}
		}
		
	}else if(repFormats[0].checked){
		//for template validation
			var selected = false;
			var length = document.forms[0].formatName.length;
			for(var i=0;i<length;i++){
					if(document.forms[0].formatName[i].selected){
						selected = true;
						break;
					}
			}

			if(!selected){
				alert('At least one Template has to selected');
				return;
			}
	} else {
		var formatSelected = false;
		for ( var i = 0;i<repFormats.length;i++ ) {
			formatSelected = repFormats[i].checked;
			if ( formatSelected )
				break;
		}
		if ( !formatSelected && !repFormats[0].disabled && !repFormats[1].disabled) {
			alert("Select Conduction format");
			return;
		}
	}

	/*if (!validateStandardTAT())
		return false;*/

	if (neworedit == "new") {

/**		if (statCharge == "") {
			alert('Stat Charge is required');
			document.addtest.statCharge.focus();
			return false;
		} else if (!isDecimal(statCharge)) {
			alert('Stat Charge must be a number');
			document.addtest.statCharge.focus();
			return false;
		} else {
			document.addtest.statCharge.value = statCharge;
		}

		if (scheduleCharge == "") {
			alert('Schedule Charge is required');
			document.addtest.scheduleCharge.focus();
			return false;
		} else if (!isDecimal(scheduleCharge)) {
			alert('Schedule Charge must be a number');
			document.addtest.scheduleCharge.focus();
			return false;
		} else {
			document.addtest.scheduleCharge.value = scheduleCharge;
		} */

	}
	return true;
}

function disableTestAddnlInfo() {
	if (document.addtest.mandate_additional_info.value == 'N') {
		document.addtest.test_additional_info.readOnly = true;
	} else {
		document.addtest.test_additional_info.readOnly = false;
	}
}

function validateStandardTAT() {
	var stdTAT = document.forms[0].stdTAT.value;
	var stdTATUnit = document.forms[0].stdTATUnits.value;
	if(stdTAT == '' && stdTATUnit != '') {
		alert("Please enter Standard TAT");
		return false;
	}
	if(stdTAT != '' && stdTATUnit == '') {
		alert("Please enter Standard TAT Unit");
		return false;
	}
	if(stdTATUnit == 'H') {
		if(stdTAT > 23) {
			alert("Invalid standard TAT. Please enter a value between 0 and 23");
			return false;
		}
	}
	if(stdTATUnit == 'M') {
		if(stdTAT > 59) {
			alert("Invalid standard TAT. Please enter a value between 0 and 59");
			return false;
		}
	}
	return true;
}

function innerHtml(){
	var checkedvalue  = getRadioSelection(document.addtest.reportGroup);
	var mandateVal = document.getElementById('mandate_additional_info').value;

	if(checkedvalue=='V'){
	    //when test has results.
		var myTable = document.getElementById('formatTable');
		var length = myTable.rows.length-1; //2
		var oTable = document.getElementById('tabdisplay');

		for (var i=1; i<length;i++) {

			var row = myTable.rows[i];
			var deleteid = "selectedrow"+i;
			var addedId = "added"+i;

			var deleteObj = document.getElementById(deleteid);
			var addedObj = document.getElementById(addedId);

			var deleteVal = (deleteObj.value == 'true');
			var added = (addedObj.value == 'Y');

		      try{
					var resultOp;
					if (added && deleteVal) {
						resultOp = "addDelete";
					}else if (added) {
						resultOp = 'add';
					} else {
						// it is already in the database, but user could have deleted or modified it

						if (deleteVal)
							resultOp = 'del';
						else
							resultOp = 'mod';
					}

					var oTR = oTable.insertRow(-1);

					var oTD = oTR.insertCell(-1);
					var obj = document.createElement('input');
					obj.type = 'hidden';
					obj.name = 'resultOp';
					obj.value = resultOp;
					oTD.appendChild(obj);

				}catch(e){
					alert(e);
					return false;
					throw e;
				}

		}
	}
}



var deptId;
var deptName;
var sampleNeed;
var testName;
var testId;
var testCode;
var specimen;
var houseStatus;
var reportType;
var formatName;
function init(){
	initializeDialog();
	disableSpec();
	depTestAutoComplete();
	disableTestAddnlInfo();
	if (neworedit == 'new') {
		getdetails();
	}
	if (neworedit == 'edit') {
		checkformat();
	}
	if (document.getElementById('service_group_id').value!="") {
		loadServiceSubGroup();
		setSelectedIndex(document.getElementById('serviceSubGroupId'), document.getElementById('serviceSubGroup').value);
	}
	onChangeCondReq();
	onChangeResultEntryAppl();
}

function loadEdit(){
	checkformat();
}
function isResultRangeAvailable(resultlabel_id){
	var exists = false;
	for(var i = 0;i < testsRangesJSON.length; i++){
		if(testsRangesJSON[i].resultlabel_id == resultlabel_id)
			exists = true;
	}
	return exists;
}

function disableSpec(){
	if(document.addtest.sample_needed.value == 'n'){
		document.addtest.specimen.disabled=true;
		document.addtest.specimen.value = "";
	}else{
		document.addtest.specimen.disabled=false;
	}

}

function populateOrgNames(){
	YAHOO.example.orgNamesArray = [];
	YAHOO.example.orgNamesArray.length =orgNames.length;

	for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
				YAHOO.example.orgNamesArray[i] = item["ORG_NAME"];
	}


	YAHOO.example.ACJSArray = new function(){
				  // Instantiate first JS Array DataSource
				  datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.orgNamesArray);
				  // Instantiate first AutoComplete
				  var autoComp = new YAHOO.widget.AutoComplete('orgName','orgContainer', datasource);
				  autoComp.prehighlightClassName = "yui-ac-prehighlight";
				  autoComp.typeAhead = true;
				  // Enable a drop-shadow under the container element
				  autoComp.useShadow = true;
				  // Disable the browser's built-in autocomplete caching mechanism
				  autoComp.allowBrowserAutocomplete = false;
				  // Require user to type at least 0 characters before triggering a query
				  autoComp.minQueryLength = 0;
				  //commas and/or spaces may delimited queries
				  // Display up to 20 results in the container
				  autoComp.maxResultsDisplayed = 20;
				  // Do not automatically highlight the first result item in the container
				  autoComp.autoHighlight = false;
				  // disable force selection,user can type his/her own complaint(which is not there in master)
				  autoComp.forceSelection = true;
				  autoComp.textboxFocusEvent.subscribe(function(){
					   var sInputValue = YAHOO.util.Dom.get('orgName').value;
					   if(sInputValue.length === 0) {
						         var oSelf = this;
						         setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
					  }

			     });

			     //autoComp.dataReturnEvent.subscribe(populateOrgId);
			}

}


function populateTestId() {
document.addtest.filterTestId.value="";
	if(trim(document.addtest.testFilter.value) != ""){
		for(var i=0;i<testNames.length;i++){
			var item = testNames[i];
			if (trim(item["TEST_NAME"]) == trim(document.addtest.testFilter.value)){
				document.addtest.filterTestId.value = item["TEST_ID"];
			}
		}
	}

}

function populateOrgId(){
	 for(var i=0;i<orgNames.length;i++){
				var item = orgNames[i]
		if(trim(item["ORG_NAME"])==trim(document.addtest.orgName.value)){
			document.addtest.orgId.value = item["ORG_ID"];
		}
	  }
}

function populateOrgName(){
	var form = document.addtest;
}

function checkPageNum( pageno ){
	if(searchValidate()){
		document.addtest.pageNum.value = pageno;
		document.addtest.submit();
	}
}

function doClose() {
	window.location.href = cpath + '/master/diagnostics.htm?status=A&sortOrder=test_name&sortReverse=false';
}

function loadServiceSubGroup() {
	var serviceGroupId = document.getElementById('service_group_id').value;
	var index = 1;
	document.getElementById("serviceSubGroupId").length = 1;
	for (var i=0; i<serviceSubGroupsList.length; i++) {
		var item = serviceSubGroupsList[i];
	 	if (serviceGroupId == item["service_group_id"]) {
	 		document.getElementById("serviceSubGroupId").length = document.getElementById("serviceSubGroupId").length+1;
	 		document.getElementById("serviceSubGroupId").options[index].text = item["service_sub_group_name"];
	  		document.getElementById("serviceSubGroupId").options[index].value = item["service_sub_group_id"];
	 		index++;
	 	}
	}
}
function getOrderCode(){
	var group = document.getElementById("service_group_id").value;
	var subGroup = document.getElementById("serviceSubGroupId").value;
	var deptId = document.addtest.ddept_id.value;
	ajaxForOrderCode('Diag',deptId,group,subGroup,document.addtest.diag_code);
}

var resultRangesDialog;

function initializeDialog() {
	resultRangesDialog = new YAHOO.widget.Dialog('resultRangesDIV', {
		        visible: false,
		        modal: true,
		        constraintoviewport: true
		});

	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:handleCancel,
	                                                scope:resultRangesDialog,
	                                                correctScope:true } );
	resultRangesDialog.cfg.queueProperty("keylisteners", [escKeyListener]);
	resultRangesDialog.cancelEvent.subscribe(cancel);
	resultRangesDialog.render();

}

function handleCancel() {

	resultRangesDialog.cancel();
	newRowinserted = true;
	currentRow = null;
}

function cancel() {
	newRowinserted = true;
	currentRow = null;
}
function showDialog(obj) {
	document.getElementById('fieldLabel').innerHTML = "Add Results";
	loadCodes();
	document.getElementById('resultlabel').value = '';
	document.getElementById('resultlabel_short').value = '';
	document.getElementById('units').value = '';
	expression = document.getElementById('expression').value = '';
	resultCode = document.getElementById('result_code').value = '';
	order = document.getElementById('order').value = '';
	if(document.getElementById('hl7_interface'))
		hl7InterfaceCode = document.getElementById('hl7_interface').value = '';
	document.getElementById('dataAllowed_V').checked = true;
	showSource();
	document.getElementById('source').value = '';
	document.getElementById('methodology').value = '';
	document.getElementById('dlgDefaultValue').value = '';
	document.getElementById('dlgDefaultValue').setAttribute("style", "width: 440px; height: 88px");
	resultRangesDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	resultRangesDialog.show();
	document.getElementById("resultlabel").focus();
}

function loadCodes() {
	var selectboxObj = document.getElementById('code_type');
	selectboxObj.length = codeTypesJSON.length +1;
	selectboxObj.options[0].text = '-- Select --';
	selectboxObj.options[0].value = '';
	selectboxObj.options[0].selected = true;
	for(var i=0; i<codeTypesJSON.length; i++) {
		selectboxObj.options[i+1].text = codeTypesJSON[i].code_type;
		selectboxObj.options[i+1].value = codeTypesJSON[i].code_type;
	}
}

var newRowinserted = true;
var currentRow = null;

function addToTable() {

	var resultName = document.getElementById('resultlabel').value;
	var resultShortName = document.getElementById('resultlabel_short').value;
	var methodId = document.getElementById('methodology').options[document.getElementById('methodology').selectedIndex].value;
	var methodName = document.getElementById('methodology').options[document.getElementById('methodology').selectedIndex].text;
	var unit = document.getElementById('units').value;
	var dataAllowed = document.getElementById("dataAllowed_V").checked ? 'V' : 'L';
	var source = document.getElementById('source').value;
	var expression = document.getElementById('expression').value;

	var codeType = document.getElementById('code_type').value;
	var resultCode = document.getElementById('result_code').value;
	var order = document.getElementById('order').value;
	var dlgDefaultValue = document.getElementById('dlgDefaultValue').value;

	if(document.getElementById('hl7_interface'))
		var hl7InterfaceCode = document.getElementById('hl7_interface').value;
	var currentRowIndex = -1;
	if (newRowinserted == false) {
		var rowObj = getThisRow(currentRow, 'TR');
		var parts = rowObj.id.split('row');
		currentRowIndex = parseInt(parts[1]);
	}
	if (trim(resultName) == '') {
		alert('Result Name should not be empty');
		return false;
	} else {
		var resultLabels = document.getElementsByName('resultLabel');
		var methodologies = document.getElementsByName('methodId');
		for (var i=0; i<resultLabels.length-1; i++) {

			if (newRowinserted == false && i == currentRowIndex) {
				//nothing to do
			} else if (resultLabels[i].value == resultName && methodologies[i].value == methodId) {
				alert("Result Name and Methodology should not be duplicate");
				return false;
			}
		}
	}

	if(document.getElementById('dataAllowed_L').checked && empty(document.getElementById('source').value) ){
		alert("Source can not be empty incase Data Allowed is List")
		return false;
	}
	var observationCode = document.getElementById('code_type').value;
	var resultCode = document.getElementById('result_code').value;
		if (observationCode != '' && resultCode == '') {
			alert('Result code is required');
			document.getElementById('result_code').focus();
			return false;
		}

	//if (!isExpressionResult(loggedCenter,expression)) return false;
	var tableObj = document.getElementById('formatTable');
	var rowsLength = tableObj.rows.length;
	var templateRow = tableObj.rows[rowsLength-1];//2
	var newRow = '';

	if (newRowinserted) {
		var id = rowsLength-1;//2
		newRow = templateRow.cloneNode(true);
		newRow.style.display = '';
		newRow.id = 'row'+(rowsLength-2);//3
		getElementByName(newRow, 'selectedrow').id = 'selectedrow'+id;
		getElementByName(newRow, 'added').id = 'added'+id;
		getElementByName(newRow, 'added').value = 'Y';
		YAHOO.util.Dom.insertBefore(newRow, templateRow);
	} else {
		newRow = getThisRow(currentRow, 'TR');
		disableSpec();
	}

	var tds = newRow.getElementsByTagName('td');

	setNodeText(tds[0], resultName);
	setNodeText(tds[1], resultShortName);
	setNodeText(tds[2], methodId == '' ? '' : methodName);
	setNodeText(tds[3], unit);
	setNodeText(tds[4], dataAllowed == 'V' ?'Any Value':'List');
	setNodeText(tds[5], dataAllowed == 'V' ? dlgDefaultValue : '', 16);
	setNodeText(tds[6], dataAllowed == 'V' ? '' :source,16);
	setNodeText(tds[8], expression,25);
	setNodeText(tds[9], codeType);
	setNodeText(tds[10], resultCode,16);
	setNodeText(tds[11], order);
	if(document.getElementById('hl7_interface'))
		setNodeText(tds[12], hl7InterfaceCode);

	getElementByName(newRow, 'resultLabel').value = resultName;
	getElementByName(newRow, 'resultLabelShort').value = resultShortName;
	getElementByName(newRow, 'units').value = unit;
	getElementByName(newRow, 'data_allowed').value = dataAllowed;
	getElementByName(newRow, 'defaultValue').value = dataAllowed == 'V' ? dlgDefaultValue : '';
	getElementByName(newRow, 'source_if_list').value = dataAllowed == 'V' ? '' :source;
	getElementByName(newRow, 'expression').value = expression;
	getElementByName(newRow, 'code_type').value = codeType;
	getElementByName(newRow, 'result_code').value = resultCode;
	getElementByName(newRow, 'order').value = order;
	getElementByName(newRow, 'methodId').value = methodId;
	if(document.getElementById('hl7_interface'))
		getElementByName(newRow, 'hl7_interface').value = hl7InterfaceCode;

	newRowinserted = true;
	currentRow = null;
	removeClassName(newRow, 'editing');
	resultRangesDialog.cancel();
}

function onEdit(obj) {
	document.getElementById('fieldLabel').innerHTML = "Edit Results";
	resultRangesDialog.cfg.setProperty('context', [obj, 'tr', 'br'], false);
	var trObj = getThisRow(obj, 'TR');
	addClassName(trObj, 'editing');
	loadCodes();
	document.getElementById('resultlabel').value = getElementByName(trObj, 'resultLabel').value;
	document.getElementById('resultlabel_short').value = getElementByName(trObj, 'resultLabelShort').value;
	document.getElementById('units').value = getElementByName(trObj, 'units').value;
	if(getElementByName(trObj, 'data_allowed').value == 'V')
		document.getElementById('dataAllowed_V').checked = true;
	else
		document.getElementById('dataAllowed_L').checked = true;
	showSource();
	document.getElementById('dlgDefaultValue').setAttribute("style", "width: 440px; height: 88px");
	document.getElementById('dlgDefaultValue').value = getElementByName(trObj, 'defaultValue').value;
	document.getElementById('source').value = getElementByName(trObj, 'source_if_list').value;
	document.getElementById('expression').value = getElementByName(trObj, 'expression').value;
	document.getElementById('code_type').value = getElementByName(trObj, 'code_type').value;
	document.getElementById('result_code').value = getElementByName(trObj, 'result_code').value;
	document.getElementById('order').value = getElementByName(trObj, 'order').value;
	document.getElementById('methodology').value = getElementByName(trObj, 'methodId').value;
	if(document.getElementById('hl7_interface'))
		document.getElementById('hl7_interface').value = getElementByName(trObj, 'hl7_interface').value;

	newRowinserted = false;
	currentRow = obj;
	getResultCodes();
	resultRangesDialog.show();
}


function getResultCodes()	{
	var type = document.getElementById('code_type').value;
	var dataSource = new YAHOO.util.XHRDataSource(cpath + "/master/addeditdiagnostics/getCodesListOfCodeType.json?");
    dataSource.scriptQueryAppend = "&codeType=" + encodeURIComponent(type);
    dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    dataSource.responseSchema = {
        resultsList: "result",
        fields: [{
            key: "code"
        }]
    };
    var oAutoComp = new YAHOO.widget.AutoComplete('result_code', 'divContainer', dataSource);
    oAutoComp.minQueryLength = 1;
    oAutoComp.forceSelection = false;
    oAutoComp.allowBrowserAutocomplete = false;
    oAutoComp.resultTypeList = false;
    oAutoComp.maxResultsDisplayed = 15;
    var reArray = [];
    oAutoComp.formatResult = function (oResultData, sQuery, sResultMatch) {
        var escapedComp = Insta.escape(sQuery);
        reArray[0] = new RegExp('^' + escapedComp, 'i');
        reArray[1] = new RegExp("\\s" + escapedComp, 'i');
        var det = highlight(oResultData.code, reArray);
        return det;
    };
    oAutoComp.textboxChangeEvent.subscribe(function () {
        trtFieldEdited = true;
        loincFieldEdited = true;
    });
    oAutoComp.setHeader(' Code ');
    return oAutoComp;
}

function checkLength(obj,len,field){
	if( obj.value.length  > len ){
		alert("Max "+len+" characters are allowed in "+field);
		obj.focus();
		return false;
	}
	return true;
}

function setDependentTest(sType,aArgs){
	var dependentTest = aArgs[2];
	document.getElementById("dependent_test_id").value = dependentTest[1];
}

function showSource(){
	if(document.getElementById("dataAllowed_V").checked) {
		document.getElementById("sourceTr").style.display = 'none';
		document.getElementById("defaultTxtTr").style.display = 'table-row';

	} else {
		document.getElementById("sourceTr").style.display = 'table-row';
		document.getElementById("defaultTxtTr").style.display = 'none';
	}
}


function depTestAutoComplete() {
	var dataSource = new YAHOO.util.LocalDataSource(testLists);
	dataSource.responseSchema = {resultsList : "result",
								 fields : [ {key :["test_name"]},{key :["test_id"]}] };
	var oAutoComp1 = new YAHOO.widget.AutoComplete('dep_test_name', 'deptestContainer', dataSource);
	oAutoComp1.maxResultsDisplayed = 15;
	oAutoComp1.allowBrowserAutocomplete = false;
	oAutoComp1.prehighlightClassName = "yui-ac-prehighlight";
	oAutoComp1.typeAhead = false;
	oAutoComp1.useShadow = false;
	oAutoComp1.minQueryLength = 0;
	oAutoComp1.animVert = false;
	oAutoComp1.itemSelectEvent.subscribe(setDependentTest);
	oAutoComp1.textboxBlurEvent.subscribe(function() {
		if ( document.getElementById("dep_test_name").value == '' )
			document.getElementById("dependent_test_id").value = null;
	});
}

function onChangeCondReq(){
	var condAppl = getRadioSelection(document.forms[0].conduction_applicable);
	var resultEntryAppl = document.forms[0].results_entry_applicable;
	var reportGroup = document.forms[0].reportGroup;
	var reportGroupValues = getRadioSelection(reportGroup);
	
	if(condAppl == 'false') {
		for (var i=0; i<resultEntryAppl.length; i++) {
			resultEntryAppl[i].checked = i > 0;
			resultEntryAppl[i].disabled = true;
		}
		for(var j=0; j<reportGroup.length; j++) {
			reportGroup[j].disabled = true;
			reportGroup[j].checked = false;
		}
		document.forms[0].conductionInstructions.disabled = true;
		document.forms[0].resultsValidation.disabled = true;
		document.forms[0].addButton.src = cpath +"/icons/Add1.png";
		document.getElementById("addresults").disabled = true;
		document.forms[0].formatName.disabled = true;
		document.forms[0].isconfidential.disabled = true;
		document.forms[0].result_validity_period.disabled = true;
		document.forms[0].result_validity_period.value = null;
		document.forms[0].result_validity_period_units.disabled = true;
	} else {
		for (var i=0; i<resultEntryAppl.length; i++) {
			resultEntryAppl[i].disabled = false;
		}
		for(var j=0; j<reportGroup.length; j++) {
			reportGroup[j].disabled = false;
		}
		document.forms[0].conductionInstructions.disabled = false;
		if(reportGroupValues == 'V')
			document.forms[0].resultsValidation.disabled = false;
		document.forms[0].addButton.src = cpath +"/icons/Add.png";
		document.getElementById("addresults").disabled = false;
		document.forms[0].formatName.disabled = false;
		document.forms[0].isconfidential.disabled = false;
		document.forms[0].result_validity_period.disabled = false;
		document.forms[0].result_validity_period_units.disabled = false;
	}
	onChangeResultEntryAppl();
}

function onChangeResultEntryAppl() {
	var resultEntryAppl = getRadioSelection(document.forms[0].results_entry_applicable);
	var reportGroup = document.forms[0].reportGroup;
	var reportGroupValues = getRadioSelection(reportGroup);
	if(resultEntryAppl == 'false') {
		for(var j=0; j<reportGroup.length; j++) {
			reportGroup[j].disabled = true;
			reportGroup[j].checked = false;
		}
		document.forms[0].resultsValidation.disabled = true;
		document.forms[0].addButton.src = cpath +"/icons/Add1.png";
		document.getElementById("addresults").disabled = true;
		document.forms[0].formatName.disabled = true;
		document.forms[0].isconfidential.disabled = true;
		document.forms[0].result_validity_period.disabled = true;
		document.forms[0].result_validity_period.value = null;
		document.forms[0].result_validity_period_units.disabled = true;
	} else {
		for(var j=0; j<reportGroup.length; j++) {
			reportGroup[j].disabled = false;
		}
		if(reportGroupValues == 'V')
			document.forms[0].resultsValidation.disabled = false;

		document.forms[0].addButton.src = cpath +"/icons/Add.png";
		document.getElementById("addresults").disabled = false;
		document.forms[0].formatName.disabled = false;
		document.forms[0].isconfidential.disabled = false;
		document.forms[0].result_validity_period.disabled = false;
		document.forms[0].result_validity_period_units.disabled = false;
	}
}
