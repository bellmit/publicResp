function initLoginDialog() {
	var dialogDiv = document.getElementById("loginDiv");
	if (!dialogDiv) return;
	dialogDiv.style.display = 'block';
	loginDialog = new YAHOO.widget.Dialog("loginDiv",
			{	width:"300px",
				visible:false,
				fixedcenter: true,
				modal:true,
				constraintoviewport:true
			});
	YAHOO.util.Event.addListener('submitForm', 'click', checkForValidUser, loginDialog, true);
	YAHOO.util.Event.addListener('cancelSubmit', 'click', cancelLoginDialog, loginDialog, true);
	var escKeyListener = new YAHOO.util.KeyListener(document, { keys:27 },
	                                              { fn:cancelLoginDialog,
	                                                scope:loginDialog,
	                                                correctScope:true } );
	loginDialog.cfg.queueProperty("keylisteners", escKeyListener);
	loginDialog.render();
}

function submitOnEnter(e) {
	e = (e) ? e : event;
	var charCode = (e.charCode) ? e.charCode : ( (e.which) ? e.which : e.keyCode);
	if ( charCode==13 ) {
		checkForValidUser();
	}
}

function checkForValidUser() {
	var userName = document.getElementById('login_user').value;
	var password = document.getElementById('login_password').value;
	if (userName == '') {
		alert("Please enter the UserName");
		return false;
	}
	if (password == '') {
		alert("Please enter the Password");
		return false;
	}
	var url = cpath + '/pages/UserAuthenticationCheck.do?_method=isAuthenticatedUserAndHasAccess&login_user='+userName+'&login_password='+password+'&action_id='+actionId;
	YAHOO.util.Connect.asyncRequest('GET', url,
					{ 	success: saveActivities,
						failure: cancelSubmittingForm,
					}
	);
}

function saveActivities(response) {
	if (response.responseText != undefined) {
		var result = response.responseText;
		if (result == 'A') {
			loginDialog.cancel();
			submitHandler();
		} else if (result == 'S') {
			alert("Shared user not allowed to proceed.");
			return false;
		} else if (result == 'U') {
			alert("You are not authorized to do this action.");
			return false;
		} else if (result == 'N') {
			alert("You do not belong to this center.");
			return false;
		} else {
			alert("Invalid UserName/Password.");
			return false;
		}
	}
}

function cancelSubmittingForm() {
}
function cancelLoginDialog() {
	this.cancel();
}