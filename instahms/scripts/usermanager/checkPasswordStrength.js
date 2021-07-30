/* this method checks the password strength*/
function checkPasswordStrength(obj) {
	var rules = passwordRules[0];
	var minLen = rules.min_len; var minLower = rules.min_lower; var minUpper = rules.min_upper;
	var minDigits = rules.min_digits; var minSpecialChars = rules.min_special_chars;
	var specialCharList = rules.specail_char_list;
	var password = obj.value;

	var lowerCaseLen = getLowerCaseLettersLen(password);
	var upperCaseLen = getUpperCaseLettersLen(password);
	var digitsLen = getDigitsLen(password);
	var specialCharsLen = getSpecialCharsLen(password,specialCharList);

	if(password.length < minLen) {
		alert("Password length should not be less than "+ minLen + ".");
		obj.focus();
		return false;
	}

	if(lowerCaseLen < minLower) {
		alert("Password should contain atleast "+ minLower + " lower case letters.");
		obj.focus();
		return false;
	}

	if(upperCaseLen < minUpper) {
		alert("Password should contain atleast "+ minUpper + " upper case letters.");
		obj.focus();
		return false;
	}

	if(digitsLen < minDigits) {
		alert("Password should contain atleast "+ minDigits +" numbers.");
		obj.focus();
		return false;
	}
	if(null!=specialCharList && specialCharList !== "") {
		if(checkIfNotExistsSplCharFromList(password,specialCharList)) {
			alert("Password should only contain special characters from " + specialCharList);
			obj.focus();
			return false;
		}
		if(specialCharsLen < minSpecialChars) {
			alert("Password should contain atleast "+ minSpecialChars +" special characters from "+ specialCharList);
			obj.focus();
			return false;
		}
	}
	return true;
}

function getLowerCaseLettersLen(str) {
	var len = 0;
    for(x=0;x<str.length;x++) {
        if(str.charAt(x) >= 'a' && str.charAt(x) <= 'z')
           len++;
    }
    return len;
}

function getUpperCaseLettersLen(str) {
	var len = 0;
	for(x=0;x<str.length;x++) {
        if(str.charAt(x) >= 'A' && str.charAt(x) <= 'Z')
           len++;
    }
    return len;
}

function getDigitsLen(str) {
	var len = 0;
	for(x=0;x<str.length;x++) {
        if(str.charAt(x) >= '0' && str.charAt(x) <= '9')
           len++;
    }
    return len;
}

function getSpecialCharsLen(str,specialCharsList) {
	var len = 0;
	if(null != specialCharsList && specialCharsList !== "") {
		var specialChars = specialCharsList.split("");
		for(x=0;x<str.length;x++) {
			if(!(/^[A-Z0-9]$/i.test(str[x]))){
				for(k=0;k<specialChars.length;k++) {
					if(str[x] == specialChars[k])
						len++;
				}
			}
		}
	}
	return len;
}

function checkPasswordStrengthOnKeyup(strength, pwd) {
    var rules = passwordRules[0];
    var strength = document.getElementById(strength);
    var minLen = rules.min_len;
    var strongRegex = new RegExp("^(?=.{"+minLen+",})(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*\\W).*$", "g");
    var mediumRegex = new RegExp("^(?=.{"+minLen+",})(((?=.*[A-Z])(?=.*[a-z]))|((?=.*[A-Z])(?=.*[0-9]))|((?=.*[a-z])(?=.*[0-9]))).*$", "g");
    var enoughRegex = new RegExp("(?=.{"+minLen+",}).*", "g");
    var pwd = document.getElementById(pwd);
    if(pwd.value.length === 0) {
      strength.innerHTML = '';
    } else if (false == enoughRegex.test(pwd.value)) {
        strength.innerHTML = '<span style="color:#787887;">Should contain atleast '+minLen+' characters!</span>';
    } else if (strongRegex.test(pwd.value)) {
        strength.innerHTML = '<span style="color:green">Strong!</span>';
    } else if (mediumRegex.test(pwd.value)) {
        strength.innerHTML = '<span style="color:#FFA000">Medium!</span>';
    } else {
        strength.innerHTML = '<span style="color:red">Weak!</span>';
    }
}

function checkIfNotExistsSplCharFromList(password,specialCharsList) {
	let passwordArr = [...password];
	let specialCharArr = ([...specialCharsList]).sort();

	for(let i = 0; i<passwordArr.length; i++){
	  if(!(/^[A-Z0-9]$/i.test(passwordArr[i])) && !specialCharArr.includes(passwordArr[i])) {
	    return true;
	  }
	}
	return false;
}
