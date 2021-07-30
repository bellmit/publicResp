<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.PHARMACY_BILL_ID_SEQUENCE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<c:set var="hospBean" value="${hospIdPatternDetail[0]}" scope="request"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Pharmacy Bill Id Sequence Preferences - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="master/sequences/hospitalIdPatterns.js"/>
	<script>
		var cpath = '${cpath}';
		var patternList = ${ifn:convertListToJson(hospitalIdPatternList)};

		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=pattern_id" +
						"&sortReverse=false";
		}
		
		 function validateForm() {
	    		var patternId = document.pharmacybillidsequencepreferenceform.pattern_id.value;
	    		var priority = document.pharmacybillidsequencepreferenceform.priority.value;
	    		var visitType = document.pharmacybillidsequencepreferenceform.visit_type.value;
	    		if(patternId == '') {
	    			alert('Please enter Pattern Id.');
	    			document.pharmacybillidsequencepreferenceform.pattern_id.focus();
	    			return false;
	    		}
	    		if(priority == ''){
	    			alert('Please enter Priority.');
	    			document.pharmacybillidsequencepreferenceform.priority.focus();
	    			return false
	    		}
	    		if(visitType == ''){
	    			alert('Please select Visit Type.');
	    			document.pharmacybillidsequencepreferenceform.visit_type.focus();
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

<body>
 <h1><insta:ltext key="patient.sequences.pharmacybillidsequences.editpharmacybillidsequencepreference"/></h1>
 <c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
 
<form action="${actionUrl}"  name="pharmacybillidsequencepreferenceform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.pharmacybillidsequences.patternid"/>:</td>
			<td>
				<input type="text" id="pattern_id" name="pattern_id" value="${bean.pattern_id}"  readonly/>
				<span class="star">*</span>
				<input type="hidden" name="pharmacy_bill_seq_id" value="${bean.pharmacy_bill_seq_id}"/>
			</td>
			
			<td class="formlabel"><insta:ltext key="patient.sequences.pharmacybillidsequences.priority"/>:</td>
			<td>
				<input type="text" name="priority" value="${bean.priority}" onkeypress="return isNumber(event)"/>
				<span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.pharmacybillidsequences.visittype"/>:</td>
			<td>
				<insta:selectoptions name="visit_type" value="${bean.visit_type}" opvalues="i,o,r" optexts="In Patient,Out Patient,Retail" dummyvalue="All" dummyvalueId="*"/>
			</td>
		</tr>
		<tr>	
			<td class="formlabel"><insta:ltext key="patient.sequences.pharmacybillidsequences.deptid"/>:</td>
			<td>
				<insta:selectdb name="dept_id" value="${bean.dept_id}" table="stores" valuecol="dept_id" displaycol="dept_name" 
					dummyvalue="All"	dummyvalueId="*" orderby="dept_name"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.pharmacybillidsequences.billtype"/>:</td>
			<td>
				<insta:selectoptions name="bill_type" value="${bean.bill_type}" opvalues="P,C" optexts="Bill Now,Bill Later" dummyvalue="All" dummyvalueId="*"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.pharmacybillidsequences.saletype"/>:</td>
			<td>
				<insta:selectoptions name="sale_type" value="${bean.sale_type}" opvalues="S,R" optexts="Sales,Returns" dummyvalue="All" dummyvalueId="*"/>
			</td>
		</tr>
	</table>
	</fieldset>
	<jsp:include page="/pages/master/hospitalidpatterns/hospitalIdPatternDetail.jsp"/>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm();"><b><u>S</u></b>ave</button>
		&nbsp;|&nbsp;
		<a href="${cpath}/${pagePath}/add.htm">Add</a>
		&nbsp;|&nbsp;
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.pharmacybillidsequences.pharmacybillidsequencepreferencelist"/></a>
	</div>
</form>
</body>
</html>
