<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<meta name="i18nSupport" content="true"/>
<title>
   <insta:ltext key="generalmasters.regularexpression.show.title" />
</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkPatternName = ${ifn:convertListToJson(RegExpList)};

	function validate() {
		var patternName = document.getElementById('pattern_name').value.trim();
		if (empty(patternName)) {
		    alert(getString("js.generalmasters.regularexpression.show.patternname.required"));
			document.getElementById('pattern_name').focus();
			return false;
		}
		var patternExp = document.getElementById('regexp_pattern').value.trim();
		if (empty(patternExp)) {
		    alert(getString("js.generalmasters.regularexpression.show.patternexp.required"));
			document.getElementById('regexp_pattern').focus();
			return false;
		}
	   if(patternExp.substring(0,2) != '/^' || patternExp.substring(patternExp.length-2,patternExp.length) != '$/'){
	        showMessage("js.generalmasters.regularexpression.show.patternexp.validate");
	        document.getElementById('regexp_pattern').focus();
	        return false;
	   }
	   if(patternExp.length == 4){
	        showMessage("js.generalmasters.regularexpression.show.patternexp.validate.length");
	        document.getElementById('regexp_pattern').focus();
	        return false;
	   }
	   var objDesc = document.getElementById('pattern_desc').value.trim();
	   var length = objDesc.length;
	   var fixedLen = 500;
	   if (length > fixedLen) {
		  alert(getString("js.generalmasters.regularexpression.show.patterndesc.length.check")+" "+fixedLen+" "+getString("js.generalmasters.regularexpression.show.patterndesc.length.check.character"));
		  document.regExp.pattern_desc.focus();
		  return false;
	   }

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newpatternName = trimAll(document.regExp.pattern_name.value);
		
			for(var i=0;i<chkPatternName.length;i++){
				item = chkPatternName[i];
				if (newpatternName == item.pattern_name){
					showMessage("js.generalmasters.regularexpression.show.patternname.checkduplicate");
			    	document.regExp.pattern_name.value='';
			    	document.regExp.pattern_name.focus();
			    	return false;
				}
			}

		return true;
	}
</script>
<c:set var="regExpHelp"
   value="/^ indicates the beginning of line.
 $/ indicates the end of line.
 Regular Expression is in between /^ and $/.
 Following is the list of metacharacters which can be used in Regular Expressions.
      \d - Any digit, short for [0-9].
      \D - A non-digit, short for [^0-9].
      \s - A whitespace character.
      \S - A non-whitespace character.
      \w - A word character, short for [a-zA-Z_0-9].
      \W - A non-word character [^\w].
      \b - Matches a word boundary where a word character is [a-zA-Z0-9_].
      [..] - Matches any single character in brackets.
      [^..] - Matches any single character not in brackets.
      \t - Matches a tab (U+0009).
      \v - Matches a vertical tab (U+000B).
      + - Matches the preceding character 1 or more times. Equivalent to {1,}.
      * - Matches the preceding character 0 or more times. Equivalent to {0,}.
      ? - Matches the preceding character 0 or 1 time. Equivalent to {0,1}.
      . - (The decimal point) matches any single character except the newline character.

Examples:-
Valid Regular Expressions are /^\d+$/ - Numerics,
                             /^\w$/ - AlphaNumerics,
                             /^[a-z0-9_-]{3,16}$/-lowercase text with numerics,underscore or hyphen
                                                               and length should be between 3 and 16.
                             /^\d{5}$/ - 5 digit Numerics,
                             /^(\d{1,2})-(\d{1,2})-(\d{4})$/ - Date format dd-MM-yyyy ."/>
<insta:js-bundle prefix="generalmasters.regularexpression"/>
</head>
<c:set var="patternexp">
  <insta:ltext key="generalmasters.regularexpression.show.patternexp" />
</c:set>
<c:set var="status">
   <insta:ltext key="generalmasters.labelmaster.list.active"/>,
   <insta:ltext key="generalmasters.labelmaster.list.inactive"/>
</c:set>
<c:set var="add">
 <insta:ltext key="generalmasters.regularexpression.show.add"/>
</c:set>
<c:set var="edit">
 <insta:ltext key="generalmasters.regularexpression.show.edit"/>
</c:set>
<body>

<form action="create.htm" method="POST" name="regExp">
	
	

	<h1>Add <insta:ltext key="generalmasters.regularexpression.list.pattern" /> </h1>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel"><insta:ltext key="generalmasters.regularexpression.show.patterndetails" /></legend>

		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.regularexpression.list.patternname" />:</td>
				<td colspan="2">
					<input type="text" name="pattern_name" id="pattern_name" value="" maxlength="50" class="required" title="${labelmsg}"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">${patternexp }:</td>
				<td colspan="2">
					<input type="text" name="regexp_pattern" id="regexp_pattern" value="" maxlength="100" class="required" title="${labelmsg}"><span class="star">*</span>
				    <img class="imgHelpText" src="${cpath}/images/help.png" title="${regExpHelp}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.labelmaster.list.status"/></td>
				<td colspan="2"><insta:selectoptions name="status" id="status" value="" opvalues="A,I" optexts="${status}" /></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.regularexpression.list.patterndescription" />:</td>
				<td colspan="2">
					<textarea name="pattern_desc" id="pattern_desc" cols="30" rows="5" title='<insta:ltext key="generalmasters.regularexpression.list.patterndescription"/>'></textarea>
				</td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u><insta:ltext key="generalmasters.regularexpression.show.s"/></u></b><insta:ltext key="generalmasters.regularexpression.show.ave"/></button>
		| <a href="list.htm?sortOrder=pattern_id&sortReverse=false&status=A"><insta:ltext key="generalmasters.regularexpression.show.expressionlist" /></a>
	</div>
</form>

</body>
</html>
