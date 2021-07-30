namelistAuto = null;

// Auto fill fucntion for Sender Id and Riecipient Id
function initSenAndRecAutoComp(){
	initNames('user_r','riecipient_dropdown');
	initNames('user_s', 'sender_dropdown');
}

function initNames(user_name, docDropDown) {

	var nameArray = [];
	nameArray.length = sendRecNameList.length;

	for ( i=0 ; i< sendRecNameList.length; i++){
		var item = sendRecNameList[i]["name"];
		nameArray[i] = item;
	}
		YAHOO.example.ACJSAddArray = new function() {
			var dataSource = new YAHOO.widget.DS_JSArray(nameArray);
			namelistAuto = new YAHOO.widget.AutoComplete(user_name, docDropDown, dataSource);
			namelistAuto.maxResultsDisplayed = 10;
			namelistAuto.queryMatchContains = true;
			namelistAuto.allowBrowserAutocomplete = false;
			namelistAuto.prehighlightClassName = "yui-ac-prehighlight";
			namelistAuto.typeAhead = false;
			namelistAuto.useShadow = false;
			namelistAuto.minQueryLength = 0;
			namelistAuto.forceSelection = false;
			namelistAuto.textboxBlurEvent.subscribe(function() {
			var docName = '';
			if(user_name == 'user_name_s') {
				docName = document.searchForm.user_name_s.value;
				if(docName == '') {
				document.searchForm.user_name_s.value = '';
				}
			} else {
				docName = document.searchForm.user_name_r.value;
				if(docName == '') {
				document.searchForm.user_name_r.value = '';
				}
			}
			});

			namelistAuto.itemSelectEvent.subscribe(function() {
				var dName = '';
				if(user_name == 'user_name_s'){
					dName = document.searchForm.user_name_s.value;
					if(dName != '') {
						for ( var i=0 ; i< sendRecNameList.length; i++){
							if(dName == sendRecNameList[i]["name"]){
								document.searchForm.user_name_s.value = sendRecNameList[i]["login_name"];
								break;
								}
							}
						setResourceSchedule();
						}else{
					document.searchForm.includeResources.value = '';
					}
				}
				else{
					dName = document.searchForm.user_name_r.value;
					if(dName != '') {
						for ( var i=0 ; i< sendRecNameList.length; i++){
							if(dName == sendRecNameList[i]["name"]){
								document.searchForm.user_name_r.value = sendRecNameList[i]["login_name"];
								break;
							}
						}
						setResourceSchedule();
					}else{
					document.searchForm.includeResources.value = '';
					}
				}
			});
		}
}

function validateMessageType(){
	if (document.MessageForm.message_type_id.value == '') {
		alert("Select Message Type");
		return false;
	}

	// var selectedMode = getRadioSelection(document.MessageForm.message_mode);
	if(document.MessageForm.message_mode.value == '') {
		alert("Select Message Mode");
		return false;
	}

	if (document.getElementById("provider_name").selectedIndex == 0) {
		alert("Please select a recipient type");
		return false;
	}
	return true;
}

function saveMessageType(){
  if (validateMessageType()) {
	document.MessageForm.submit();
  }
}

function selectProvider(provider) {
	document.MessageForm.nextProvider.value=provider;
	document.MessageForm.submit();
}

function saveRecipients(provider) {
	document.MessageForm.submit();
}

function allRecipients() {
	var checked = (document.MessageForm._select_all && document.MessageForm._select_all.checked);
	var elements = document.getElementsByName('_selected_recipients');
	for (var i = 0; i < elements.length; i++) {
		elements[i].checked=checked;
	}
	if(document.MessageForm._currentProvider.value =='Patients')
		document.MessageForm._select_all.value = 'false';
}

function clickRecipient(chkBox, recipientId) {
	var removed = document.MessageForm._removed_selections.value;
	if (!empty(chkBox)) {
		if (!chkBox.checked) {
			if (!empty(removed)) {
				removed = removed + ',' + recipientId;
			} else {
				removed = recipientId;
			}
			document.MessageForm._select_all.checked = false;
		} else {
			var removedItems = removed.split(',');
			var removedIndex = -1;
			for (var i = 0; i < removedItems.length; i++) {
				var removedItem = removedItems[i];
				if (removedItem == recipientId) {
					removedIndex = i;
					break;
				}
			}
			if (removedIndex >= 0) {
				removedItems.splice(removedIndex);
				removed = removedItems.join(',');
			}
		}
	}
	document.MessageForm._removed_selections.value = removed;
}

function messageTypeChange(messageTypeEl, messageTypeData) {

	var selectedValue = messageTypeEl.options[messageTypeEl.selectedIndex].value;
	var dataItem = findInList(messageTypeData, "message_type_id", selectedValue);
	var messageMode = dataItem.message_mode;
	if (!empty(messageMode)) {
		setSelectedIndex(document.getElementById("message_mode"), messageMode);
	}
	// load the recipient selection list
	var providerList = providerMap[dataItem.message_type_id];
	var elSelectBox = document.getElementById("provider_name");
	loadSelectBox(elSelectBox, providerList, "name", "name", "----Select----");
	if (providerList && providerList.length == 1) {
		elSelectBox.selectedIndex = 1;
	} else {
		elSelectBox.selectedIndex = 0;
	}
}

function disableRadioGroup (el) {
	for (var i = 0; i < el.length; i++) {
		el[i].disabled = true;
	}
}

function sendMessage() {
	if (validateMessageTemplate()) {
		document.MessageForm.submit();
	}
}

function validateMessageTemplate() {

	var mode = document.MessageForm.message_mode.value;
	var sender = document.MessageForm.message_sender.value;
	var subject = document.MessageForm.message_subject.value;
	var body = document.MessageForm.message_body.value;
	var selectAll = document.MessageForm._select_all.value;

	// at least one address is specified or at least one address is
	// selected from the recipient list

	if (empty(document.MessageForm.message_to.value) &&
		empty(document.MessageForm.message_cc.value) &&
		empty(document.MessageForm.message_bcc.value) &&
		((selectAll!='true') && (empty(document.MessageForm._recipient_count.value) ||
		document.MessageForm._recipient_count.value == 0))
		) {
			alert("You have not selected or specified any message recipients. Please enter any of To, Cc or Bcc addresses");
			return false;
		}

	if (mode == 'EMAIL' && empty(subject)) {
		alert("Please enter a Subject");
		return false;
	}

	if (!empty(document.MessageForm.attachment) && !empty(document.MessageForm.attachment.value)) {
		alert("You have selected a file, but not added it. Please add the file before sending the message.");
		return false;
	}

	if (empty(body)) {
		alert("Please enter a Message");
		return false;
	}

	return true;
}

function onLoadRecipientList() {
	allRecipients();
}

function addAttachment() {

	if (empty(document.MessageForm.attachment.value)) {
		alert("Please select a file to add");
		return false;
	}

	document.MessageForm._method.value = "addAttachment";
	document.MessageForm.submit();
}
function getUpdatedStatus() {
	var ajaxobj = newXMLHttpRequest();
	var url = cpath + "/message/MessageLog.do?_method=getMessageStatus";
	ajaxobj.open("GET", url.toString(), false);
	ajaxobj.send(null);
	window.location.reload(true);
}

function validateDateRange() {
  var theForm = document.searchForm;
  var message_type_id = theForm.message_type_id.value;
  var category_id = theForm.category_id.value;
  var message_sender_id = theForm.message_sender_id.value;
  var message_recipient_id = theForm.message_recipient_id.value;
  var message_mode = theForm.message_mode.value;
  var last_status = theForm.last_status.value;
  var last_sent_date0 = theForm.last_sent_date0.value;
  var last_sent_date1 = theForm.last_sent_date1.value;

  var lastSentDateReqd = (message_type_id == undefined || (message_type_id!=undefined && message_type_id == ""))
  						&& 	(category_id == undefined || (category_id != undefined && category_id == ""))
  						&&	(message_sender_id == undefined  || (message_sender_id!=undefined && message_sender_id == ""))
  						&&	(message_recipient_id == undefined   || (message_recipient_id != undefined && message_recipient_id == ""))
  						&&	(message_mode == undefined   || (message_mode != undefined && message_mode == ""))
  						&&	(last_status == undefined   || (last_status != undefined && last_status == ""))
  						&&	(last_sent_date0 == undefined   || (last_sent_date0 != undefined && last_sent_date0 == ""))
  						&&	(last_sent_date1 == undefined   || (last_sent_date1 != undefined && last_sent_date1 == ""));


if (lastSentDateReqd) {
		alert("Last sent date is required.");
		theForm.last_sent_date0.focus();
		return false;
	}

if (last_sent_date1 == "") {
		last_sent_date1 = formatDate(new Date(), 'ddmmyyyy', '-');
}

if (last_sent_date0 !== undefined && last_sent_date1 !== undefined && daysDiff(parseDateStr(last_sent_date0), parseDateStr(last_sent_date1)) > 31) {
		alert("Last sent date should be within a month.");
		theForm.last_sent_date1.focus();
		return false;
	}
	return true;
}
