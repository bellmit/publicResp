<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Miscellaneous Settings - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="masters/printmaster.js"/>
<insta:link type="js" file="registration/registrationCommon.js"/>

<script>
	function checkStatus(event){
		YAHOO.util.Event.stopEvent(event);
	}
	function chk1(e){
	  var key=0;
	  if(window.event || !e.which)
	  {
		 key = e.keyCode;
   	  }
	  else
	  {
		 key = e.which;
	  }
      if(document.forms[0].hospital_terms_conditions.value.length<3900 || key==8)
      {
        key=key;
        return true;
      }
      else
      {
       key=0;
       return false;
     }
}
function chklen1(){
  if(document.forms[0].hospital_terms_conditions.value.length > 3900){
  	alert("PO terms & conditions should be 3900 characters only");
  	var s = document.forms[0].hospital_terms_conditions.value;
  	s = s.substring(0,3900);
  	document.forms[0].hospital_terms_conditions.value = s;
  }
}
function validate() {
	document.forms[0].hospital_terms_conditions.value = FormatTextAreaValues(document.forms[0].hospital_terms_conditions.value);
	return true;
}
</script>
<insta:js-bundle prefix="registration.patient"/>
</head>

<body class="yui-skin-sam">
<h1>Miscellaneous Settings</h1>
<insta:feedback-panel/>
<form action="MiscellaneousSettings.do?_method=${param._method == 'add'?'create' : 'update'}" method="POST" >
	<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel">Settings </legend>
		<table class="formtable" width="100%">
		<tr>
		    <td class="formlabel">Credit Period:</td>
		    <td><input type="text" name="credit_period" value="${bean.map.credit_period}" maxlength="10"> </td>
		</tr>
		<tr>
		   <td class="formlabel">Indent Process No:</td>
		   <td><input type="text" name="indent_process_no" value="${bean.map.indent_process_no}" maxlength="10"> </td>
		</tr>
		<tr>
		   <td class="formlabel">Delivery:</td>
		   <td><input type="text" name="delivery" value="${bean.map.delivery}" maxlength="10"> </td>
		</tr>
		<tr>
		   <td class="formlabel">PO Terms and Conditions	:	</td>
		   <td><textarea name="hospital_terms_conditions" id="hospital_terms_conditions" rows="5" cols="80" onkeypress="return chk1(event);"
			  onblur="chklen1();"><c:out value="${bean.map.hospital_terms_conditions}"/></textarea>(max 3900 characters)
		   </td>
		</tr>
	 </table>
</fieldset>
<div class="screenActions">
		<button type="submit" accesskey="S" name="save" class="button" onclick="validate();"><b><u>S</u></b>ave</button>
    </div>
</form>

</body>
</html>
