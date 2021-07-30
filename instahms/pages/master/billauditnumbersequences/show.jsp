<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.BILL_AUDIT_NUMBER_SEQUENCE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Audit Control Sequence Preferences - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=pattern_id" +
						"&sortReverse=false";
		}
		
		function validateForm() {
    		var priority = document.auditcontrolsequencepreferenceform.priority.value;
    		if(priority == ''){
    			alert('Please enter Priority.');
    			document.auditcontrolsequencepreferenceform.priority.focus();
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
<c:set var="hospBean" value="${hospIdPatternDetail[0]}" scope="request"/>
<body>
 <h1><insta:ltext key="patient.sequences.auditcontolsequences.editauditcontrolsequencepreference"/></h1>
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
 
<form action="${actionUrl}"  name="auditcontrolsequencepreferenceform" method="POST">

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.patternid"/>:</td>
			<td>
				<input type="text" name="pattern_id" value="${bean.pattern_id}"  readonly/>
						<span class="star">*</span>
				<input type="hidden" name="bill_audit_number_seq_id" value="${bean.bill_audit_number_seq_id}"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.priority"/>:</td>
			<td>
				<input type="text" name="priority" value="${bean.priority}" length="50" onkeypress="return isNumber(event)" />
				<span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.centerid"/>:</td>
			<td>
				<insta:selectdb name="center_id" value="${bean.center_id}" table="hospital_center_master" valuecol="center_id" displaycol="center_name" orderby="center_name"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.visittype"/>:</td>
			<td>
				<insta:selectoptions name="visit_type" value="${bean.visit_type}" opvalues="i,o,r,t" optexts="In Patient,Out Patient,Retail,Incoming" dummyvalue="All" dummyvalueId="*"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.billtype"/>:</td>
			<td>
				<insta:selectoptions name="bill_type" value="${bean.bill_type}" opvalues="P,C" optexts="Bill Now,Bill Later" dummyvalue="All" dummyvalueId="*"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.restrictiontype"/>:</td>
			<td>
				<insta:selectoptions name="restriction_type" value="${bean.restriction_type}" opvalues="P,N,T" optexts="Pharmacy,Hospital,Incoming"
					dummyvalue="All" dummyvalueId="*"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.creditnote"/>:</td>
			<td>
				<insta:selectoptions name="is_credit_note" value="${bean.is_credit_note}" opvalues="t,f" optexts="Yes,No" dummyvalue="All" dummyvalueId="*"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.auditcontolsequences.tpa"/>:</td>
			<td>
				<insta:selectoptions name="is_tpa" value="${bean.is_tpa}" opvalues="t,f" optexts="Yes,No" dummyvalue="All" dummyvalueId="*"/>
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
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.auditcontolsequences.auditcontrolsequencepreferencelist"/></a>
	</div>
</form>
</body>
</html>
