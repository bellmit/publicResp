var unsavedTx = false; // set true if there are unsaved transactions. is used to disable or enable the send to tpa button.
function addToTable(){

	var transactionType = document.getElementById('transactionType').value;

	var billStatus = document.getElementById('billStatus').value;

	if ( transactionType == 20 || transactionType == 25 ){
		if (billStatus == 'X'){
			alert("Bill related to this claim is cancelled");
			return false;
		}else if (billStatus == 'C') {
			alert("Bill related to this claim is closed");
			return false;
		}
	}


	var fileName = document.getElementById('attachment[' + file_index + ']').value;
	var fil = document.getElementById('attachment[' + file_index + ']');
	var fi  = fil.files[0];

	if (transactionType == 'NULLx') {
		alert('please select the transaction type');
		return false;
	} else if ((parseInt(transactionType)>=10) && (parseInt(transactionType)<=25)) {
		if (document.getElementById('amount').value == '') {
			alert("please enter the amount");
			return false;
		}
	} else {

	}
	if (isNaN(document.getElementById('amount').value)) {
		alert("invalid amount");
		document.getElementById('amount').focus();
		return false;
	}

	var tab = document.getElementById('transactionDetails');
	var i = tab.rows.length;
	var tr = tab.insertRow(-1);
	var td = '';
	//tr.setAttribute("class", "justAdded");
	var selectTd = tr.insertCell(-1);
	//selectTd.innerHTML =  '<input type="checkbox" name="selectAndSendToTPA" id="selectAndSendToTPA"' +
	//	 'onclick="return enableDateTimeFields('+i+', this);">';

	td = tr.insertCell(-1);
	td.innerHTML = '<input type="text" name="tpaSentDate" id="tpaSentDate'+ i +'"  size="8" readonly/>' +
					'<input type="text"	name="tpaSentTime" id="tpaSentTime'+ i +'" size="4" maxlength="5" ' +
					'onchange="validateTime(this, \''+i+'\')" readonly>';
	//getDateWidget('tpaSentDate', 'tpaSentDate'+i);
	//makePopupCalendar('tpaSentDate'+i);
	//document.getElementById('tpaSentDate'+i).disabled = true;


	td = tr.insertCell(-1);
	td.innerHTML =  '<span class="label">' + document.forms[0].transactionType.options[document.forms[0].transactionType.selectedIndex].text
		+ '<input type="hidden" name="txtTransactionType" id="txtTransactionType" value="'+ transactionType +'"></span>';

	var description = document.getElementById('description').value;
	if (description == '') {
		description = '&nbsp;'
	}
	td = tr.insertCell(-1);
	td.innerHTML =  '<span class="label">' + description + '</span>'
				+ '<input type="hidden" name="txtDescription" id="txtDescription" value="'
				+ document.getElementById('description').value + '">'
				+ '<input type="hidden" name="txtRemainderNotes" id="txtRemainderNotes" value="'
				+ document.getElementById('remainderNotes').value + '">';
	document.getElementById('remainderNotes').value = '';
	documentTD = tr.insertCell(-1);

	var amount = document.getElementById('amount').value;
	if (amount == '') {
		amount = '&nbsp;'
	}
	td = tr.insertCell(-1);
	td.innerHTML =	'<span class="label">' + amount + '</span>'
				+ '<input type="hidden" name="txtAmount" id="txtAmount" value="'
				+ document.getElementById('amount').value +'">'

	if (fileName != '') {
		multi_selector.addWhenClickedAddButton(document.getElementById('attachment['+ file_index++ +']'), documentTD, selectTd);
	} else {
		var sysGenDoc = document.getElementById('generateSupportDoc').value;
		var documentName = '';
		if (sysGenDoc != '') {
			documentName = document.forms[0].generateSupportDoc.options[document.forms[0].generateSupportDoc.selectedIndex].text;
		}
		documentTD.innerHTML = documentName	+ '<input type="hidden" name="fileName" id="fileName" value="'+ fileName +'">'
				+ '<input type="hidden" name="txtSysGenDoc" id="txtSysGenDoc" value="'+ sysGenDoc +'">';
		var img_button = document.createElement( 'img' );
		img_button.src = path + '/images/delete.jpg';
		img_button.alt = 'Select elements to Cancel';
		img_button.title = 'Select elements to Cancel';
		img_button.width = '20';
		img_button.height = '20';

		img_button.onclick= function(){
			if (confirm("do you want to delete the row")) {
				img_button.parentNode.parentNode.parentNode.removeChild(img_button.parentNode.parentNode);
				// enables or disables the sendtoTPA button
				enableAndDisableSentToTpa();
			}
		}
		selectTd.appendChild(img_button);

	}

	document.getElementById('transactionType').value = 'NULLx';
	document.getElementById('description').value = '';
	document.getElementById('amount').value = '';
	if (document.getElementById('attachment['+ file_index +']').disabled) {
		document.getElementById('attachment['+ file_index +']').disabled = false;
	}
	document.getElementById('generateSupportDoc').value = '';
	unsavedTx = true;
	if (!document.getElementById('sendToTPA').disabled) {
		document.getElementById('sendToTPA').disabled = true;
		document.getElementById('sendToTPA').style.color = 'darkgray';
		document.getElementById('maildiv').style.display = 'none';

	}

}

/**********
* is called on click of delete image (after deleting the row).
* sendToTPA is
* A) disabled
* 		if 1)  still finds unsaved transactions. (after deleting the selected row)
* B) enabled
*    	if 1) there are no unsaved transactions and atleast one saved transaction should already be checked to
*		  		send to tpa.
********************/
function enableAndDisableSentToTpa() {
	var exTxRows = document.getElementById('existingTxRows').value;
	var rowsLength = document.getElementById('transactionDetails').rows.length;
	var checkBoxs = document.getElementsByName('selectAndSendToTPA');
	var flag = true;
	for (var i=0; i<checkBoxs.length; i++) {
		if (checkBoxs[i].checked) {
			flag = false;
			break;
		}
	}
	if (parseInt(exTxRows) == (parseInt(rowsLength)-1)) {
		if (!flag) {
			document.getElementById('sendToTPA').disabled = false;
			document.getElementById('sendToTPA').style.color = 'darkgreen';
		} else if (flag) {
			document.getElementById('sendToTPA').disabled = true;
			document.getElementById('sendToTPA').style.color = 'darkgray';
			document.getElementById('maildiv').style.display = 'none';
		}
		unsavedTx = false;
	} else {
		document.getElementById('sendToTPA').disabled = true;
		document.getElementById('sendToTPA').style.color = 'darkgray';
		document.getElementById('maildiv').style.display = 'none';

		unsavedTx = true;
	}

}

/******
* is called on click of selectAndSendToTPA checkbox.
* enables the date and time fields.
* it will disables the sentToTPA button if there are any unsaved transaction(eventhough transactions are
* checked to send to TPA).  if unsaved transactions not found it disables the sentToTPA button on checking of
* checkbox.
*************/
function enableDateTimeFields(index, checkbox) {
	var currDate = new Date();
	var month = currDate.getMonth()+1;
	var day = currDate.getDate();
	var year = currDate.getFullYear();
	var hours = currDate.getHours();
	var minutes = currDate.getMinutes();
	if ((""+month).length == 1) {
		month = "0" + month;
	}
	if ((""+day).length == 1) {
		day = "0" + day;
	}
	if ((""+hours).length == 1) {
		hours = "0" + hours;
	}
	if ((""+minutes).length == 1) {
		minutes = "0" + minutes;
	}

	if (checkbox.checked) {
		document.getElementById("tpaSentDate"+index).readOnly = false;
		document.getElementById("tpaSentTime"+index).readOnly = false;
		if ((document.getElementById("tpaSentDate"+index).value == '') &&
			(document.getElementById("tpaSentTime"+index).value == '')) {
			document.getElementById("tpaSentDate"+index).value = day+"-"+month+"-"+year;
			document.getElementById("tpaSentTime"+index).value = hours+":"+minutes;
		}
		/*** sendToTPA button will be enabled if there are no unsaved transactions
		***	 and if any one of the transaction is checked to send to TPA.
		********/
		if (!unsavedTx) {
			document.getElementById('sendToTPA').disabled = false;
			document.getElementById('sendToTPA').style.color = 'darkgreen';
		}
	} else {
		document.getElementById("tpaSentDate"+index).readOnly = true;
		document.getElementById("tpaSentTime"+index).readOnly = true;
		if ((checkbox.value).split(",")[1] == '') {
			document.getElementById("tpaSentDate"+index).value = '';
			document.getElementById("tpaSentTime"+index).value = '';
		}

		var checkBoxs = document.getElementsByName(checkbox.name);
		var flag = true;
		for (var i=0; i<checkBoxs.length; i++) {
			if (checkBoxs[i].checked) {
				flag = false;
				break;
			}
		}
		/*********** if
		*	1) nothing is checked and
		*	2) there are unsaved transactions
		*	then disable the sendToTPA button.
		***************/
		if ((flag) && !(unsavedTx)) {
			document.getElementById('sendToTPA').disabled = true;
			document.getElementById('sendToTPA').style.color = 'darkgray';
			document.getElementById("maildiv").style.display = 'none'
		}
	}

}

function populateStatuses(status) {
	var stage = document.getElementById('stage').value;
	var statusSelectObj = document.getElementById("status");

	var k = 1;
	for (var i=0; i<jsonStatuses.length; i++) {
		var statusAndStage = jsonStatuses[i];
		if (parseInt(statusAndStage.STAGE_ID) == parseInt(stage)) {
			statusSelectObj.length = k+1;
			statusSelectObj[k].value = statusAndStage.STATUS_ID;
			statusSelectObj[k].text = statusAndStage.STATUS_NAME;
			k++;
		}
	}
	if (status != null) {
		statusSelectObj.value = status;
	}
}

/******
*	this utility function validates time returs false if entered time is not in the pattern HH:MI
*	timeField : is the html form element
*   index : element id index
***********/
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
			alert("Incorrect minute : please enter 0-59 for minute");
			timeField.focus();
			return false;
		}
	} else {
		alert("Incorrect time format : please enter HH:MI");
		timeField.focus();
		return false;
	}
	return true;

}

/***
* called on click of Send to Tpa button
********/
function validateDateAndTime() {

	var timeFields = document.getElementsByName('tpaSentTime');
	var checkboxes = document.getElementsByName("selectAndSendToTPA");
	var dateFieldsArray = document.getElementsByName('tpaSentDate');

	for (var i=0; i<checkboxes.length; i++) {
		if (checkboxes[i].checked) {
			if (!validateSentDateFields(dateFieldsArray[i])) {
				return false;
			}
			if (!validateTime(timeFields[i], (i+1))){
				return false;
			}
		}
	}
	if ((ModeOfCommunication.communication_mode == 'Email') && (ModeOfCommunication.tpaEmailId != '')) {
		if(ModeOfCommunication.host_name == ''){
			alert("Mail Client is not Configured, Please configure it in Generic Preferences");
			return false;
		}
		document.getElementById('maildiv').style.display = 'block';
		document.getElementById("to").value =  ModeOfCommunication.tpaEmailId;
		document.getElementById("from").value = ModeOfCommunication.fromAddress;
		window.scrollBy(0, 350);
	} else {
		document.forms[0].action = path + '/pages/insurance/InsuranceTransactionTrackingScreen.do';
		document.forms[0].method.value = 'updateSentDateAndTime';
		document.forms[0].submit();
	}
}

/****
* is called on double click of description when transaction type is Remainder.
* opens the text editor to write the notes.
***********/
function openEditor() {
	var transType = document.getElementById('transactionType').value;
	var url = path + '/pages/insurance/RemainderNotesEditor.jsp?file_index='+file_index;
	window.open(url,'Popup_Window',"width=700, height=500,screenX=80,screenY=50,left=375,top=250,scrollbars=yes,menubar=0,resizable=yes");
}

/*********
* this function clear the values of controls based on type parameter passed to it, if type passed to
* it is 'sys' then it clears the system generated documents , if type passed to it is 'attachement'
* it will clears the attachment (if selected).
******************/
function clearDocs(type) {
	var sysGenDoc = document.getElementById('generateSupportDoc').value;
	var fileName = document.getElementById('attachment['+ file_index +']').value;
	if (type == 'sys') {
		if (sysGenDoc != '') {
			document.getElementById('generateSupportDoc').value = '';
		}
	} else if (type == 'attachment') {
		if (fileName != '') {
			document.getElementById('attachment['+ file_index +']').value = '';
		}
	}
}

/******* this utility function validates the tpa sent date.
*  sent date should be in the format dd-mm-yyyy.
*  TPA sent date can't be
*   1) before the case open date.
*   2) future date.
***********/
function validateSentDateFields(dateField) {

	var dateStr = dateField.value;
	if (dateStr != '') {
		var msg = validateDateFormat(dateStr);
		if (msg == null) {
			var formattedStrDate = cleanDateStr(dateStr);
			msg = validateDateStr(formattedStrDate, 'past');
			if (msg == null) {
				var caseOpenDate = parseDateStr(document.getElementById('caseOpenDate').value);
				var sentToTpaDate = parseDateStr(formattedStrDate);
				var dateDiff = daysDiff(caseOpenDate, sentToTpaDate);
				if (dateDiff < 0) {
					alert("date can't be before the case open date :"+document.getElementById('caseOpenDate').value);
					dateField.focus();
					return false;
				}
			} else {
				// tpa sent date cannot be the future date.
				alert(msg);
				dateField.focus();
				return false
			}
		} else {
			alert(msg);
			dateField.focus();
			return false;
		}
	}
	return true;
}

/******
* 	is called on click of save button.
*	return false if u try to edit the sent date or time for not sent transactions.
*********/
function checkStageAndStatus(roleid) {
	var timeFields = document.getElementsByName('tpaSentTime');
	var checkboxes = document.getElementsByName("selectAndSendToTPA");
	var dateFieldsArray = document.getElementsByName('tpaSentDate');
	var selectedToEdit = false;

	if (document.getElementById("transactionType").value != "NULLx") {
		if (confirm("unadded transaction found, do u want to ignore and save?")) {
			//addToTable();

		} else {
			return false;
		}
	}
	for (var i=0; i<checkboxes.length; i++) {
		if (checkboxes[i].checked) {
			selectedToEdit = true;
			var documentSent = (checkboxes[i].value).split(',')[1];
			if (documentSent == "") {
				alert("Can't edit the sent date and time of unsent document, please uncheck it to proceed further");
				checkboxes[i].focus();
				return false;
			} else {
				var sentDate = dateFieldsArray[i].value;
				var sentTime = timeFields[i].value;
				if (!validateSentDateFields(dateFieldsArray[i])) {
					return false;
				}
				if (!validateTime(timeFields[i], (i+1))) {
					return false;
				}
			}
		}
	}

	var stage = document.getElementById('hiddenStageId').value;
	var status = document.getElementById('hiddenStatusId').value;
	var selectedStage = document.getElementById('stage').value;
	var selectedStatus = document.getElementById('status').value;
	if ((parseInt(stage) != parseInt(selectedStage)) && (parseInt(selectedStage) == 20)) {
		if (roleid == '') {
			alert("You don't have the Permission to close the Insurance Case");
			document.getElementById('stage').focus();
			return false;
		}
	}

	if ( (parseInt(stage) != parseInt(selectedStage)) || (parseInt(status) != parseInt(selectedStatus)) ) {
		document.getElementById('stageAndStatusFlag').value = 'updated';
	}
	var hiddenInsNo = document.getElementById('hiddenInsNo').value;
	var enteredInsNo = document.getElementById('txtInsuranceNo').value;

	if (hiddenInsNo != enteredInsNo) {
		document.getElementById('hiddenInsFlag').value = 'updated';
	}
	if ((!unsavedTx) && (!selectedToEdit) && (document.getElementById('hiddenInsFlag').value != 'updated')
		&& (document.getElementById('stageAndStatusFlag').value != 'updated')) {
		alert("no changes found");
		return false;
	}
	document.forms[0].submit();
}

function showEditorIcon(type) {
	if (parseInt(type) == 35) {
		document.getElementById('imgReminderNotes').style.display = 'inline';
	} else {
		if (document.getElementById('imgReminderNotes').style.display == 'inline') {
			document.getElementById('imgReminderNotes').style.display = 'none';
			document.forms[0].remainderNotes.value = '';
			document.getElementById('attachment['+file_index+']').value = '';
			document.getElementById('generateSupportDoc').value = '';
			document.getElementById('attachment['+file_index+']').disabled = false;
			document.getElementById('generateSupportDoc').disabled = false;
		}
	}
}

function flushToPrinter() {
	var checkboxes = document.getElementsByName("selectAndSendToTPA");
	var flag = false;
	for (var i=0; i<checkboxes.length; i++) {
		if (checkboxes[i].checked) {
			flag = true;
		}
	}
	if (!flag) {
		alert("please select the document for print");
		return false;
	}

	document.forms[0].action = path + '/pages/insurance/InsuranceTransactionTrackingScreen.do';
	document.forms[0].method.value = 'flushToPrinter';
	document.forms[0].submit();
}

function printCasesheet() {

	var mrNo = document.getElementById("mrNo").value;
	var patName = document.getElementById("patName").value;
	patName =  encodeURIComponent(patName);
	var dept = document.getElementById("deptName").value;
	dept =  encodeURIComponent(dept);
	var billAmount = document.getElementById("billAmount").value;
	var paidByPatient = document.getElementById("paidByPatient").value;
	var insId = document.getElementById("txtInsuranceId").value;
	var patId = document.getElementById("patId").value;
	var admitDoctor = document.getElementById("admitDoctor").value;
	var refDoctor = document.getElementById("refDoctor").value;
	var patient_age = document.getElementById("patient_age").value;
	var url = path + '/pages/insurance/InsuranceTransactionTrackingScreen.do?method=printCaseSheet&mrNo='+
			  mrNo+'&patName='+patName+'&deptName='+dept+'&billAmount='+billAmount+'&paidByPatient='+
			  paidByPatient+'&txtInsuranceId='+insId+'&patId='+patId+'&admitDoctor='+admitDoctor+
			  '&refDoctor='+refDoctor+'&patient_age='+patient_age;
	window.open(url);
}


function mailSend(){
	if (document.forms[0].from.value == '') {
		alert("please enter the From address.");
		document.forms[0].from.focus();
		return false;
	}
	if (document.forms[0].to.value == '') {
		alert("please enter the To address.");
		document.forms[0].to.focus();
		return false;
	}
	if (document.forms[0].subject.value == '') {
		if (confirm("Are you sure you want to send a message without a subject?")) {

		} else {
			document.forms[0].subject.focus();
		}
	}
	document.forms[0].action = path + '/pages/insurance/InsuranceTransactionTrackingScreen.do';
	document.forms[0].method.value = 'sendMail';
	document.forms[0].submit();
}
function enablemaildiv(){
	//window.scrollBy(0, 50);
	if (document.getElementById('maildiv').style.display == 'none') {
		//window.scrollBy(0, 10000);
		document.getElementById('maildiv').style.display = 'block';
		document.getElementById("to").value =  ModeOfCommunication.tpaEmailId;
	} else {
		document.getElementById('maildiv').style.display = 'none';
	}
	window.scrollBy(0, 350);
}

function resetMailInterface() {
	document.forms[0].from.value = '';
	document.forms[0].to.value = '';
	document.forms[0].subject.value = '';
	document.forms[0].mailMessage.value = '';
}

function enableOrDisableSendEmailButn(){
	if (ModeOfCommunication.communication_mode == 'Email') {
		document.getElementById('sendMail').disabled = false;
		document.getElementById('sendMail').style.color = 'darkgreen';
	} else {
		document.getElementById('sendMail').disabled = true;
		document.getElementById('sendMail').style.color = 'darkgray';
	}

}

function closeMailInterface(){
	resetMailInterface();
	document.getElementById('maildiv').style.display = 'none';
}


