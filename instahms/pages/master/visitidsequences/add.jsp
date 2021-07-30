<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.VISIT_ID_SEQUENCE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Visit Id Sequence Preferences - Insta HMS</title>
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
    		var patternId = document.visitidsequencepreferenceform.pattern_id.value;
    		var priority = document.visitidsequencepreferenceform.priority.value;
    		var visitType = document.visitidsequencepreferenceform.visit_type.value;
    		if(patternId == '') {
    			alert('Please enter Pattern Id.');
    			document.visitidsequencepreferenceform.pattern_id.focus();
    			return false;
    		}
    		if(priority == ''){
    			alert('Please enter Priority.');
    			document.visitidsequencepreferenceform.priority.focus();
    			return false
    		}
    		if(visitType == ''){
    			alert('Please select Visit Type.');
    			document.visitidsequencepreferenceform.visit_type.focus();
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
 <h1><insta:ltext key="patient.sequences.visitidsequences.addvisitidsequencepreference"/></h1>
 <c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
 
<form action="${actionUrl}"  name="visitidsequencepreferenceform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.visitidsequences.patternid"/>:</td>
			<td>
				<div id="patternId_wrapper" style="width: 14em; padding-bottom:0.2em">
					<input type="text" id="pattern_id" name="pattern_id" value=""  class="required validate-length" length="50" />
					<div id="patternIdContatiner"></div>
				</div>
				<span class="star" style="padding-left:170px;">*</span>
			</td>
			
			<td class="formlabel"><insta:ltext key="patient.sequences.visitidsequences.priority"/>:</td>
			<td>
				<input type="text" name="priority" value="" length="50" onkeypress="return isNumber(event)"/>
				<span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.visitidsequences.visittype"/>:</td>
			<td>
				<insta:selectoptions name="visit_type" value="" opvalues="i,o" optexts="In Patient,Out Patient" dummyvalue="Select" dummyvalueId=""/>
				<span class="star">*</span>
			</td>
		</tr>
		<tr>	
			<td class="formlabel"><insta:ltext key="patient.sequences.visitidsequences.centerid"/>:</td>
			<td>
				<insta:selectdb name="center_id" value="" table="hospital_center_master" valuecol="center_id" displaycol="center_name" orderby="center_name"/>
			</td>
		</tr>
	</table>
	</fieldset>
	<jsp:include page="/pages/master/hospitalidpatterns/hospitalIdPatternDetail.jsp"/>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.visitidsequences.visitidsequencepreferencelist"/></a>
	</div>
</form>
</body>
</html>
