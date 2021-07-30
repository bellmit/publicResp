<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.RECEIPT_NUMBER_SEQUENCE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Receipt Number Sequence Preferences - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=pattern_id" +
						"&sortReverse=false";
		}
		
		function validateForm() {
    		var priority = document.receiptnumbersequencepreferenceform.priority.value;
    		if(priority == ''){
    			alert('Please enter Priority.');
    			document.receiptnumbersequencepreferenceform.priority.focus();
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
 <h1><insta:ltext key="patient.sequences.receiptnosequences.editreceiptnosequencepreference"/></h1>
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
 
<form action="${actionUrl}"  name="receiptnumbersequencepreferenceform" method="POST">

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.patternid"/>:</td>
			<td>
				<input type="text" name="pattern_id" value="${bean.pattern_id}"  readonly/>
						<span class="star">*</span>
				<input type="hidden" name="receipt_number_seq_id" value="${bean.receipt_number_seq_id}"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.priority"/>:</td>
			<td>
				<input type="text" name="priority" value="${bean.priority}" length="50" onkeypress="return isNumber(event)" />
				<span class="star">*</span>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.centerid"/>:</td>
			<td>
				<insta:selectdb name="center_id" value="${bean.center_id}" table="hospital_center_master" valuecol="center_id" displaycol="center_name" orderby="center_name"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.visittype"/>:</td>
			<td>
				<insta:selectoptions name="visit_type" value="${bean.visit_type}" opvalues="i,o,r,t" optexts="In Patient,Out Patient,Retail,Incoming" dummyvalue="All" dummyvalueId="*"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.billtype"/>:</td>
			<td>
				<insta:selectoptions name="bill_type" value="${bean.bill_type}" opvalues="P,C" optexts="Bill Now,Bill Later" dummyvalue="All" dummyvalueId="*"/>
			</td>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.restrictiontype"/>:</td>
			<td>
				<insta:selectoptions name="restriction_type" value="${bean.restriction_type}" opvalues="P,N,T" optexts="Pharmacy,Hospital,Incoming"
					dummyvalue="All" dummyvalueId="*"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="patient.sequences.receiptnosequences.paymenttype"/>:</td>
			<td>
				<insta:selectoptions name="payment_type" value="${bean.payment_type}" opvalues="R,F,S" optexts="Receipt,Refund,Sponsor Receipts" dummyvalue="All" dummyvalueId="*"/>
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
		<a href="javascript:void(0)" onclick="doClose();"><insta:ltext key="patient.sequences.receiptnosequences.receiptnosequencepreferencelist"/></a>
	</div>
</form>
</body>
</html>
