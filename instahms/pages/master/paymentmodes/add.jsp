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
		
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortReverse=false";
		}

		function setEnableDisable() {
			var disable = '${not empty bean && bean.map.mode_id == -1}';
			if (disable == 'true')
				document.paymentmodemasterform.status.setAttribute("disabled","disabled");
			else
				document.paymentmodemasterform.status.removeAttribute("disabled");
		}

	</script>
</head>
<body>

 <h1>Add Payment Mode</h1>
 
 <c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/> 
<form action="${actionUrl}"  name="paymentmodemasterform" method="POST">

	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<legend> 
     Mode Details
    </legend>
	<table class="formtable" >
		<tr>
			<td class="formlabel">Payment Mode:</td>
			<td>
				<input type="text" name="payment_mode" value="" maxlength="30" />
			</td>
			<td class="formlabel">Special Account Name: </td>
			<td><input type="text" name="spl_account_name" value="" class="required" title="Special Account Name required" maxlength="100"/></td>
		</tr>
		<tr>
			<td class="formlabel">Card Type Required:</td>
			<td>
				<insta:selectoptions name="card_type_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Bank Name Required:</td>
			<td>
				<insta:selectoptions name="bank_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Ref No Required:</td>
			<td>
				<insta:selectoptions name="ref_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Bank Batch Required:</td>
			<td>
				<insta:selectoptions name="bank_batch_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Card Auth Code Required:</td>
			<td>
				<insta:selectoptions name="card_auth_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Card Holder Name Required:</td>
			<td>
				<insta:selectoptions name="card_holder_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Card Number Required:</td>
			<td>
				<insta:selectoptions name="card_number_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Card Expiry Date Required:</td>
			<td>
				<insta:selectoptions name="card_expdate_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel"></td>
			<td></td>
		</tr>
		<tr>
			<td class="formlabel">Realization Required:</td>
			<td>
				<insta:selectoptions name="realization_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">Status</td>
			<td>
				<insta:selectoptions name="status" value="" opvalues="A,I" optexts="Active,InActive"/>
			</td>
			<td class="formlabel">Display Order:</td>
			<td>
				<input type="text" name="displayorder" value="" onkeypress="return enterNumOnlyzeroToNine(event)"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Mobile Number:</td>
			<td>
				<insta:selectoptions name="mobile_number_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
			<td class="formlabel">OTP:</td>
			<td>
				<insta:selectoptions name="totp_required" value="" opvalues="Y,N" optexts="Yes,No"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Transaction Limit:</td>
			<td>
				<input type="text" name="transaction_limit" class="validate-number" style="text-align: left;" value=""  onkeypress="return enterNumOnlyANDdot(event)" maxlength="13"/>
			</td> 
			<!--  <td class="formlabel">Payment More Than Transaction Limit:</td>
            <td><insta:selectoptions name="allow_payments_more_than_transaction_limit" value="" opvalues="A,W,B" optexts="Allow,Warn,Block"/>
            </td>-->
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();">Payment Mode List</a>
	</div>

</form>
</body>
</html>
