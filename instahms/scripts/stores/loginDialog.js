function initLoginDialog() {
	var dialogDiv = document.getElementById("loginDiv");
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
		showMessage("js.sales.issues.enterusername");
		return false;
	}
	if (password == '') {
		showMessage("js.sales.issues.password");
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
		if (result == 'invalid') {
			showMessage("js.sales.issues.invalidusername.password");
			return false;
		} else if (result == 'A') {
			loginDialog.cancel();
			submitHandler();
		}else if ( result == "SharedUser") {
			showMessage("js.sales.issues.shareduser.notallowedtoproceed");
			return false;
		}
		else {
			showMessage("js.sales.issues.accessdenied");
			return false;
		}

	}
}

function cancelSubmittingForm() {
}
function cancelLoginDialog() {
	this.cancel();
}