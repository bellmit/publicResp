<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta http-equiv="Cache-Control" content="no-cache"/>
		<title>Password Preferences - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>

		<script>
			var maxLoginAttempt = '${bean.map.max_login_attempt}';
			function onSave() {
				var minLen = document.forms[0].min_len.value;
				var minUpper = document.forms[0].min_upper.value;
				var minLower = document.forms[0].min_lower.value;
				var minDigits = document.forms[0].min_digits.value;
				var minSpecialChars = document.forms[0].min_special_chars.value;
				var specialCharList = document.forms[0].specail_char_list.value;
				var lockUser = document.getElementById("lock_user_for_failed_login");
				var LoginAttempt = document.getElementById("max_login_attempt").value;

				if(minLen == '') {
					alert("Minimum length should not be empty, Please enter some value.");
					return false;
				}
				if(minUpper == '') {
					alert("Minimum uppercase letters should not be empty, Please enter 0 or some value.");
					return false;
				}
				if(minLower == '') {
					alert("Minimum lowercase letters should not be empty, Please enter 0 or some value.");
					return false;
				}
				if(minDigits == '') {
					alert("Minimum digits should not be empty, Please enter 0 or some value.");
					return false;
				}
				if(minSpecialChars == '') {
					alert("Minimum Special characters should not be empty, Please enter 0 or some value.");
					return false;
				}
				if(minSpecialChars != 0) {
					if(trim(specialCharList)=='') {
						alert("Please enter special character list like @#$%...");
						return false;
					}
				}

				if(lockUser.checked && LoginAttempt=='') {
					alert("Please enter maximum login attempt.");
					document.getElementById("max_login_attempt").focus();
					return false;
				}

				document.forms[0].specail_char_list.value = trim(specialCharList);
				document.forms[0].submit();
			}

			function setLoginAttempt() {
				var lockUser = document.getElementById("lock_user_for_failed_login");
				if(lockUser.checked) {
					document.getElementById("loginAttempt").style.display = 'block';
					document.getElementById("loginAttemptLbl").style.display = 'block';
				} else {
					document.getElementById("loginAttempt").style.display = 'none';
					document.getElementById("loginAttemptLbl").style.display = 'none';
					document.getElementById("max_login_attempt").value= '';
				}
			}

			function init() {
				if(maxLoginAttempt != null && maxLoginAttempt != '') {
					document.getElementById("lock_user_for_failed_login").checked = true;
					document.getElementById("loginAttempt").style.display = 'block';
					document.getElementById("loginAttemptLbl").style.display = 'block';
				} else {
					document.getElementById("lock_user_for_failed_login").checked = false;;
					document.getElementById("loginAttempt").style.display = 'none';
					document.getElementById("loginAttemptLbl").style.display = 'none';
				}
			}
		</script>
	</head>
	<body class="yui-skin-sam" onload="init();">
		<form name="passwordRuleForm" action="PasswordPreferences.do?method=update" method="POST">
			<h1>Password Preferences</h1>
			<insta:feedback-panel />
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Password Preferences</legend>
				<table class="formtable">
					<tr>
						<td class="formlabel">Minimum Length:</td>
						<td>
							<input type="text" name="min_len" id="min_len" value="${bean.map.min_len}"
								class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
						</td>
						<td class="formlabel">Minimum Lower Case Letters:</td>
						<td>
							<input type="text" name="min_lower" id="min_lower" value="${bean.map.min_lower}"
								class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
						</td>
						<td class="formlabel">Minimum Upper Case Letters:</td>
						<td>
							<input type="text" name="min_upper" id="min_upper" value="${bean.map.min_upper}"
								class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
						</td>
					</tr>
					<tr>
						<td class="formlabel">Minimum Digits:</td>
						<td>
							<input type="text" name="min_digits" id="min_digits" value="${bean.map.min_digits}"
								class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
						</td>
						<td class="formlabel">Minimum Special Characters:</td>
						<td>
							<input type="text" name="min_special_chars" id="min_special_chars" value="${bean.map.min_special_chars}"
								class="number" onkeypress="return enterNumOnlyzeroToNine(event)" />
						</td>
						<td class="formlabel">Special Character List:</td>
						<td>
							<input type="text" name="specail_char_list" id="specail_char_list"
							value="${fn:escapeXml(bean.map.specail_char_list)}"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Last Password Frequency:</td>
						<td>
							<input type="text" name="last_password_frequency" id="last_password_frequency"  class="number"
								value="${bean.map.last_password_frequency}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
						</td>
						<td class="formlabel">Password Change Frequency Days:</td>
						<td>
							<input type="text" name="password_change_freq_days" id="password_change_freq_days"  class="number"
								value="${bean.map.password_change_freq_days}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
						</td>
						<td class="formlabel">Password Change Notify Days:</td>
						<td>
							<input type="text" name="password_change_notify_days" id="password_change_notify_days"  class="number"
								value="${bean.map.password_change_notify_days}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel">
							<input type="checkbox" name="lock_user_for_failed_login" id="lock_user_for_failed_login"
								onchange="setLoginAttempt()"/>
							<label for="lock_user_for_failed_login">Lock User For Failed Login</label>
						</td>
						<td class="forminfo"></td>
						<td class="formlabel"><div id="loginAttemptLbl">Maximum Login Attempt:</div></td>
						<td>
						<div id="loginAttempt">
							<table class="formtable">
								<tr>
									<td class="forminfo">
										<input type="text" name="max_login_attempt" id="max_login_attempt" class="number"
											value="${bean.map.max_login_attempt}" onkeypress="return enterNumOnlyzeroToNine(event)" />
									</td>
								</tr>
							</table>
						</div>
						</td>
					</tr>
				</table>
			</fieldset>
			<div class="screenActions">
				<button type="button" name="save"  accesskey="S" class="button" onclick="onSave();"><b><u>S</u></b>ave</button>
			</div>
		</form>
	</body>
</html>