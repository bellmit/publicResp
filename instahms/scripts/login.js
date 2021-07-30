/*
 * This page checks all the validation for
 * 	a) login.
 *  b) changing password.
 *  c) password expired.
 *	d) password going to expire.
 */

function validate() {
	var strUser = document.forms[0].userId.value;
	var strPwd = document.forms[0].password.value;
	var strHospital = document.forms[0].hospital.value;

	if (strHospital == '') {
		document.forms[0].hospital.focus();
		$('.login-success').html(
				"<div style=\"color:#FA4545\">Please Enter Hospital</div>");
		$('.error').html('Please Enter Hospital');
		return false;
	}
	if (strUser == '') {
		document.forms[0].userId.focus();
		$('.login-success').html(
				"<div style=\"color:#FA4545\">Please Enter User Name</div>");
		$('.error').html('Please Enter User Name');
		return false;
	}
	if (strPwd == '') {
		document.forms[0].password.focus();
		$('.login-success').html(
				"<div style=\"color:#FA4545\">Please Enter Password</div>");
		$('.error').html('Please Enter Password');
		return false;
	}
	return true;
}

function imageExists(url, callback) {
	   var img = new Image();
	   img.onload = function() { callback(true); };
	   img.onerror = function() { callback(false); };
	   img.src = url;
}

function updateLogo() {
	var userSchema = getUserSchema();
	if (!userSchema) {
		$('.login-card-image-logo img').attr("src","./images/hospitalLogo/Logo.png");
		return;
	}
	imageExists("./images/hospitalLogo/" + userSchema + "Logo.png", function(exists) {
		if (exists) {
			$('.login-card-image-logo img').attr("src","./images/hospitalLogo/" + userSchema + "Logo.png");
		} else {
			$('.login-card-image-logo img').attr("src","./images/hospitalLogo/Logo.png");
		}
	});
}

function setUserSchema(userSchema) {
	if (typeof(Storage) === "undefined") {
		return;
	}
	localStorage.setItem("user_schema"+"@"+cpath, userSchema);
}

function getUserSchema() {
	if (typeof(Storage) === "undefined") {
		return "";
	}
	return localStorage.getItem("user_schema"+"@"+cpath);
}

function init() {
	if (loginStatus == 'errorInUpdatePassword') {
		document.getElementById('oldpwd').value = '';
		document.getElementById('changePasswordDiv').style.display = 'block';
	}
	updateLogo();
	if (ssoLoginOnly) {
		return;
	}
	if (!notifyPasswordChange && loginStatus != 'blockUser' && loginStatus != 'errorInUpdatePassword') {
		clear();
		if (window.location.hash.length > 0) {
			document.getElementById('hashFragment').value = window.location.hash;
		}
	}
}

function clear() {
	document.forms[0].password.value = '';

	var userSchema = getUserSchema();
	document.forms[0].hospital.value = userSchema;
	hospitalListner();
	document.forms[0].userId.focus();
	updateLogo();
}

function changePassword() {
	document.getElementById('oldpwd').value = '';
	updateLogo();
	document.getElementById('changePasswordDiv').style.display = 'block';
	document.getElementById('blockUser').style.display = 'none';
	document.getElementById('oldpwd').focus();
}

function submitFun() {
	var oldpwd = document.getElementById('oldpwd').value;
	var newpwd = document.getElementById('pwd').value;
	var cpwd = document.getElementById('cpwd').value;

	if (oldpwd == '') {
		document.getElementById('oldpwd').focus();
		$('.error').html('Please Enter Password Old password');
		return false;
	}

	if (newpwd == '') {
		document.getElementById('pwd').focus();
		$('.error').html('Please Enter Password New password');
		return false;
	}

	if (cpwd == '') {
		document.getElementById('cpwd').focus();
		$('.error').html('Please Enter Password Confirm password');
		return false;
	}

	if (!passequal()) {
		return false;
	}

	if (oldpwd == newpwd) {
		document.getElementById('pwd').value = '';
		document.getElementById('cpwd').value = ''
		document.getElementById('pwd').focus();
		$('.error').html(
				'Old password is same as new password, please try again.');
		return false;
	}

  	if(!checkPasswordStrength(document.forms[0].pwd)) {
			return false;
	}
	if (document.changePasswordForm) {
		document.changePasswordForm.action = cpath
				+ '/pages/AdminModule/ChangePassword.do';
		document.changePasswordForm.method.value = 'updatePassword';
		document.changePasswordForm.submit();
	}
}

function passequal() {
	var str = document.getElementById('pwd').value;
	var str1 = document.getElementById('cpwd').value;

	if (str == '') {
		document.getElementById('pwd').focus();
		return false;
	}
	if (!(str == str1)) {
		document.getElementById('pwd').value = '';
		document.getElementById('cpwd').value = '';
		document.getElementById('pwd').focus();
		$('.error').html('Password doesn\'t match, please try again.');
		return false;
	}
	return true;
}

function initialize() {
	if(document.getElementById("oldpwd") !== null) {
		document.getElementById('oldpwd').value = '';
		document.getElementById('oldpwd').focus();
	}
}

$(function(){
	$("button[name='windows-sso-button']").click(function(ev){
		ev.preventDefault();
		window.location.href= cpath + "/home.do";
	})
});
