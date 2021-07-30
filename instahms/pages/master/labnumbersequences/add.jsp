<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.LAB_NUMBER_SEQUENCE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Lab Number Sequence Preferences - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="master/sequences/hospitalIdPatterns.js"/>
	<script>
		var cpath = '${cpath}';
		var patternList = ${ifn:convertListToJson(hospitalIdPatternList)};

		function init() {
			initPatternIdAutoComplete();
		}
		
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=pattern_id" +
						"&sortReverse=false";
		}
		
       function validateForm() {
    		var patternId = document.labnumbersequencepreferenceform.pattern_id.value;
    		var priority = document.labnumbersequencepreferenceform.priority.value;
    		if(patternId == '') {
    			alert('Please enter Pattern Id.');
    			document.labnumbersequencepreferenceform.pattern_id.focus();
    			return false;
    		}
    		if(priority == ''){
    			alert('Please enter Priority.');
    			document.labnumbersequencepreferenceform.priority.focus();
    			return false
    		}
    		
    		return true;
    	}
       
       function isNumber(event) {
    	    evt = (event) ? event : window.event;
    	    var charCode = (evt.which) ? evt.which : evt.keyCode;
    	    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
    	        return false;
    	    }
    	    return true;
    	}

   	</script>
</head>

<body onload="init()">
 <h1><insta:ltext key="patient.sequences.labnumbersequences.addlabnumbersequencepreference"/></h1>
 <c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
 
<form action="${actionUrl}"  name="labnumbersequencepreferenceform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.labnumbersequences.patternid"/>:</td>
			<td>
				<div id="patternId_wrapper" style="width: 14em; padding-bottom:0.2em">
					<input type="text" id="pattern_id" name="pattern_id" value="" />
					<div id="patternIdContatiner"></div>
				</div>
				<span class="star" style="padding-left:170px;">*</span>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>	
			<td class="formlabel"><insta:ltext key="patient.sequences.labnumbersequences.priority"/>:</td>
			<td>
				<input type="text" name="priority" value="" onkeypress="return isNumber(event)"/>
				<span class="star">*</span>
			</td>
		</tr>
	</table>
	</fieldset>
	<jsp:include page="/pages/master/hospitalidpatterns/hospitalIdPatternDetail.jsp"/>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.labnumbersequences.labnumbersequencepreferencelist"/></a>
	</div>
</form>
</body>
</html>
