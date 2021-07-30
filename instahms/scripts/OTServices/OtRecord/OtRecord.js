function init() {
	captureSubmitEvent()
	complaintForm = document.OtRecord;
	//initLoginDialog();
	//CollapsiblePanelForOperations();
}

var collapsiblePanels = {};
function CollapsiblePanelForOperations() {
	for (var k = 0; k< consCompJSON.length; k++) {
		var createCollapsiblePanel = false;
		var opProcId = consCompJSON[k].operation_proc_id;
		var componentConsList = consCompJSON[k].forms.split(',');
		for (var i = 0; i<componentConsList.length; i++) {
		var formId = componentConsList[i];
			if (formId > 0) {
				for (var j=0; j<physicianformsJSON.length; j++) {
					var phyFormId = physicianformsJSON[j].form_id;
					if (phyFormId == formId && physicianformsJSON[j].linked_to != 'patient') {
						createCollapsiblePanel = true;
					}
				}
			}
		}
		if (createCollapsiblePanel) {
			var cp = new Spry.Widget.CollapsiblePanel("CollapsiblePanel"+opProcId, {contentIsOpen:false});
		    collapsiblePanels[opProcId] = cp;
		}
	}
}

function captureSubmitEvent() {
	var form = document.OtRecord;
	form.validateFormSubmit = form.submit;

	form.submit = function validatedSubmit() {
		if (!blockSubmit()) {
			var e = xGetElementById(document.OtRecord);
			YAHOO.util.Event.stopEvent(e);
			return false;
		}
		form.validateFormSubmit();
		return true;
	};
}
function blockSubmit() {
	if (document.getElementById('patient_discharged').value == 'true') {
		alert("Patient is inactive or discharged, editing of OT Record is not allowed.");
		return false;
	}
	return true;
}

// this method gets called from shared login dialog.
function submitHandler() {
	document.getElementById('authUser').value = document.getElementById('login_user').value;
	document.OtRecord.submit();
	return false;
}

// this method called when user click on save button.
function chkFormDetailsEdited(printOtRecord) {
	document.getElementById('printOtRecord').value = printOtRecord;
	if (!validateComplaint()) return false;
	if (!validateSysGenForms()) return false;
	// validate mandatory fields in physician forms.
	if (!validateMandatoryFields()) return false;

	document.OtRecord.submit();
	// do not put any code after this line.
	return true;
}

function setOpenOperationsForm(opProcId , phyForms) {
	var componentConsList = phyForms.split(',');
	for (var i = 0; i<componentConsList.length; i++) {
		var formId = componentConsList[i];
		if (formId > 0 &&  patientFromGroupJSON == 'Y') {
			for (var j=0; j<physicianformsJSON.length; j++) {
				var phyFormId = physicianformsJSON[j].form_id;
				if (phyFormId == formId && physicianformsJSON[j].linked_to != 'patient') {
					var tableId = "phys_form_table" + opProcId + ":" + formId;
					var tableEle = document.getElementById(tableId);
					tableEle.style.display = 'block';
					var buttonId = "phys_form_btn" + opProcId + ":" + formId;
					var buttonEle = document.getElementById(buttonId);
					buttonEle.value = '-';
				}
			}
		}
	}
}

function handleScreenLock(screenId, patientId, loginHandle) {
	var webSocketChannel = `/user/${loginHandle}/topic/actionscreen/lock/${screenId}/${patientId}`;
	var socket = new SockJS(cpath + "/ws/instahms");
	console.log('topic to subscribe: ' + webSocketChannel);
	var stompClient = Stomp.over(socket);
	var subscription;
	stompClient.connect({}, function (){
		 subscription = stompClient.subscribe(webSocketChannel, function(message) {
		 	var lockObj = JSON.parse(message.body);
		 	flashMessage = lockObj.message;
		 	var messageType = lockObj.message_type;
		 	var user = lockObj.user_id;
		 	if(user !== userid) {
		 		disableForm(true);	
		 		showFlashMessage(flashMessage);
		 	}
		 }) ;
	 }, function(){
		 setTimeout(function(){ handleScreenLock(screenId,patientId,loginHandle); }, 1000);
	 });
}

function obtainScreenLock(obtainLockUrl) {
	if (!is_screen_locked) {
		$.ajax({
			"url": obtainLockUrl,
			"success": function(e) {
				removeScreenLock('ot_record', patient_id, obtainLockUrl);
			},
		})
	}
}

function removeScreenLock(screenId, patientId, obtainLockUrl) {
	var text = "Current Session will expire in 5 minutes, if you want to continue the session press Ok";
	setTimeout(function(){
		if(confirm(text)) {
			obtainScreenLock(obtainLockUrl);
		} else {
			setTimeout(function(){removeLock(screenId, patientId)}, 300000);
		}}, 600000);
}

function removeLock(screenId, patientId) {
	var deleteLockUrl = cpath + `/multiuser/actionscreen/deletelock.json?screen_id=${screenId}&patient_id=${patientId}`;
	$.ajax({
			"url": deleteLockUrl,
			"async": false,
			"success": function(e) {
				disableForm(e.is_screen_locked);
			},
		})
}

function disableForm(status) {
	is_screen_locked = status;
	document.getElementById('Save').disabled=status;
	document.getElementById('saveAndPrint').disabled=status;
	$("#OtRecord :input").prop("disabled", status);
}

function showFlashMessage(flashMessage) {
	var alertDiv = document.getElementById("flash");
	alertDiv.style.display = 'block';
	var msgDiv = document.getElementById("msg");
	msgDiv.innerText = flashMessage;
}

function setIsFinalizeAll(event) {
    var isAllSectionsFinalized = event.target.value;
    if(isAllSectionsFinalized) {
        document.getElementById('is_finalizeAll').value = true;
    } else {
        document.getElementById('is_finalizeAll').value = false;
    }
}
