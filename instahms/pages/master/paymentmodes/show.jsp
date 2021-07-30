<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.PAYMENT_MODE_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Payment Mode - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function keepBackUp(){
				backupName = document.paymentmodemasterform.payment_mode.value;
				setEnableDisable();
		}

		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortReverse=false";
		}

		function setEnableDisable() {
			var disable = '${not empty bean && bean.mode_id == -1}';
			if (disable == 'true')
				document.paymentmodemasterform.status.setAttribute("disabled","disabled");
			else
				document.paymentmodemasterform.status.removeAttribute("disabled");
		}
		
		Insta.masterData=${ifn:convertListToJson(paymentModeDetails)};

	</script>
</head>
<body onload= "keepBackUp();" >

        <h1 style="float:left">Edit Payment Mode</h1>
        <c:url var="searchUrl" value="${pagePath}/show.htm"/>
        <insta:findbykey keys="payment_mode,mode_id" fieldName="mode_id" method="show" url="${searchUrl}"/>
        
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>    
<form action="${actionUrl}"  name="paymentmodemasterform" method="POST">
		<input type="hidden" name="mode_id" value="${bean.mode_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<legend> 
      Mode Details
    </legend>
	<table class="formtable" >
		<tr>
			<td class="formlabel">Payment Mode:</td>
			<td>
				<input type="text" name="payment_mode" value="${bean.payment_mode}" maxlength="30"/>
			</td>
			<td class="formlabel">Special Account Name: </td>
			<td><input type="text" name="spl_account_name" value="${bean.spl_account_name}" class="required" title="Special Account Name required" maxlength="100"/></td>
		</tr>
		<tr>
			<td class="formlabel">Card Type Required:</td>
			<td>
				<insta:selectoptions name="card_type_required" value="${bean.card_type_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Bank Name Required:</td>
			<td>
				<insta:selectoptions name="bank_required" value="${bean.bank_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Ref No Required:</td>
			<td>
				<insta:selectoptions name="ref_required" value="${bean.ref_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Bank Batch Required:</td>
			<td>
				<insta:selectoptions name="bank_batch_required" value="${bean.bank_batch_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Card Auth Code Required:</td>
			<td>
				<insta:selectoptions name="card_auth_required" value="${bean.card_auth_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Card Holder Name Required:</td>
			<td>
				<insta:selectoptions name="card_holder_required" value="${bean.card_holder_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Card Number Required:</td>
			<td>
				<insta:selectoptions name="card_number_required" value="${bean.card_number_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Card Expiry Date Required:</td>
			<td>
				<insta:selectoptions name="card_expdate_required" value="${bean.card_expdate_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel"></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel">Realization Required:</td>
			<td>
				<insta:selectoptions name="realization_required" value="${bean.realization_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Status</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,InActive"/>
			</td>
			<td class="formlabel">Display Order:</td>
			<td>
				<input type="text" name="displayorder" value="${bean.displayorder}" onkeypress="return enterNumOnlyzeroToNine(event)"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Mobile Number:</td>
			<td>
				<insta:selectoptions name="mobile_number_required" value="${bean.mobile_number_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">OTP:</td>
			<td>
				<insta:selectoptions name="totp_required" value="${bean.totp_required}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Transaction Limit:</td>
			<td>
				<input type="text" name="transaction_limit" class="validate-number" style="text-align: left;" value="${bean.transaction_limit}"  onkeypress="return enterNumOnlyANDdot(event)" maxlength="13"/>
			</td> 
			<!--<td class="formlabel">Payment More Than Transaction Limit:</td>
            <td><insta:selectoptions name="allow_payments_more_than_transaction_limit" value="${bean.allow_payments_more_than_transaction_limit}" opvalues="A,W,B" optexts="Allow,Warn,Block"/>
            </td>
			</td>-->
            <td class="formlabel">Sponsor Applicable:</td>
			<td>
				<insta:selectoptions name="sponsor_applicable" value="${bean.sponsor_applicable}" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<a href="${cpath}/${pagePath}/add.htm">Add</a>
		|
		<a href="javascript:void(0)" onclick="doClose();">Payment Mode List</a>
	</div>

</form>
</body>
</html>
