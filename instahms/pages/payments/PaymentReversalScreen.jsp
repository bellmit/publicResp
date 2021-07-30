<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
		<head>
			<title>Payment Reversal - Insta HMS</title>
			<meta http-equiv="Content-Type" content="text/html charset=iso-8859-1" >

			<insta:link type="js" file="hmsvalidation.js"/>
			<insta:link type="css" file="widgets.css"/>
			<insta:link type="js" file="payments/paymentreversal.js" />
			<insta:link type="js" file="paymentCommon.js" />


			<style>
				.scrolForContainer .yui-ac-content{
					 max-height:11em;width:190px;overflow:auto;overflow-x:auto; /* scrolling */
				}
			</style>
		</head>

	<body onload="init()">
		<h1>Payment Reversal</h1>
		<insta:feedback-panel/>
		<c:set var="counter" value="${requestScope.counterList}"/>
		<form method="POST" action="${cpath}/pages/payments/PaymentReversal.do" name="paymentreversal">
		<input type="hidden" name="_method" value="reversalPayment"/>
		<input type="hidden" name="payAll" value="${payment.paymentType}"/>
		<input type="hidden" name="payeeId" id="payeeId" value=""/>

		<jsp:useBean id="now" class="java.util.Date"/>
		<c:set var="dt" value="${now}"/>
		<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
		<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>
		<fieldset class="fieldsetBorder"><legend>Payee Details</legend>
			<table class="formtable" >
				<tr>
					<td colspan="5" class="formlabel">Date:</td>
					<td>
						<c:choose>
							<c:when test="${ (roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A') }">
								<insta:datewidget name="payDate" valid="past" value="${date}" btnPos="left"
									id="payDate"	 editValue="false"/>
								 <input type="text" name="payTime" value="${time}" class="timefield" />
							</c:when>
							<c:otherwise>
								<insta:datewidget name="payDate" valid="past" value="${date}" btnPos="left"
									id="payDate"	 editValue="true"/>
								 <input type="text" name="payTime" value="${time}" class="timefield" readonly/>
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
				<tr>
					<td class="formlabel">Payment Type:</td>
					<td>
						<insta:selectoptions name="paymentType" value="" dummyvalue="-- Select --"
							opvalues="C,D,P,F,R,O,S"	optexts="Miscellaneous Payments,Doctor Payments,Prescribing Doctor Payments,Referral Doctor Payments,Doctor as Referral Doctor Payments,Out house Payments,Supplier Payments"
							onchange="onSelectPaymentType(this.value);"/>
					</td>
					<td class="formlabel">Payee Name:</td>
					<td valign="top">
						<div name="payeeNameDiv" class="autoComplete">
						<input type="text" name="payeeName" id="payees" value="" style="width: 190px"/>
						<div id="payeeNameContainer" class="scrolForContainer">	</div>
						</div>
					</td>
					<td class="formlabel">Counter</td>
					<td>
						<insta:selectoptions name="counter" id="counter" value="${billingcounterId}" opvalues="${billingcounterId},${pharmacyCounterId}" optexts="${billingcounterName},${pharmacyCounterName}"/>
					</td>
				</tr>
				<tr id="acheadrow" >
					<td class="formlabel" >Account Head:</td>
					<td >
						<insta:selectdb name="accountHead" table="bill_account_heads" value=""
						displaycol="account_head_name" valuecol="account_head_id" />
					</td>
				</tr>
			</table>
		</fieldset>
		<fieldset class="fieldsetBorder"><legend>Payment Details</legend>
			<table class="detailList" cellspacing="0" cellpadding="0" id="paymentsTable" border="0" width="100%">
				<tr>
					<th>Reason</th>
					<th>Voucher No</th>
					<th class="number">Amount</th>
					<th></th>
					<th></th>
				</tr>
				<tr style="display: none">
					<td>
						<img src="${cpath}/images/empty_flag.gif"/>
						<label></label>
						<input type="hidden" name="description" value=""/>
					</td>
					<td>
						<label></label>
						<input type="hidden" name="voucherNo" value=""/>
					</td>
					<td class="number">
						<label></label>
						<input type="hidden" name="amount" />
					</td>
					<td>
						<a href="javascript:Cancel Item" onclick="return cancelPayment(this);" title="Cancel Item">
							<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
						</a>
					</td>
					<td>
						<input type="hidden" name="delPayment" id="delPayment" value="false" />
						<a name="_editAnchor" href="javascript:Edit" onclick="return showEditPaymentDialog(this);" title="Edit Payment Details">
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</tr>
				<tr class="footer" >
					<td>TDS: <input type="text" name="tds" id="tds" class="number"
						onkeypress="return enterNumOnly(event);" value=""/></td>
					<td colspan="3" style="text-align: right">
						Total Amount:&nbsp;<label id="lblTotalAmt" style="font-weight: bold;">0</label>
					</td>
					<td style="text-align: center">
						<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Payment"
							onclick="addPaymentDialog(this); return false;"
							accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>
				</tr>
			</table>
		</fieldset>
		<fieldset	class="fieldsetBorder"><legend>Payment Modes</legend>
			<table class="formtable" >

				<insta:paymentdetails/>

			</table>
		</fieldset>
		<div class="screenActions">
			<button type="button" name="pay" id="pay" accesskey="R" class="button" onclick="return validateForm();"/>
				<label><b><u>R</u></b>eturn</label></button>&nbsp;
		</div>
		<div style="visibility: hidden" id="addPaymentDialog">
			<div class="bd">
				<input type="hidden" name="editRowId" id="editRowId" value=""/>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="titleForAdd">Add</legend>
					<legend class="fieldSetLabel" id="titleForEdit">Edit</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Reason</td>
							<td><input type="text" name="_dReason" id="_dReason"/></td>
						</tr>
						<tr>
							<td class="formlabel">Voucher No.</td>
							<td><input type="text" name="_dVoucherNo" id="_dVoucherNo"/></td>
						</tr>
						<tr>
							<td class="formlabel">Amount</td>
							<td><input type="text" name="_dAmount" id="_dAmount" class="number"
									onkeypress="return enterNumOnly(event);" value=""/></td>
						</tr>
					</table>
					<table style="margin-top: 10px">
						<tr>
							<td>
								<button type="button" name="Ok" id="Ok" value="Ok" accesskey="O" style="display: none; float:left">
										<b>O</b>k</button>
								<button type="button" name="Add" id="Add" value="Add" accessKey="A" style="display: block; float:left">
										<b><u>A</u></b>dd
									</button>
								<button type="button" name="Previous" id="Previous" accessKey="P" style="display: none; float: left">
									&lt;&lt;<b><u>P</u></b>revious</button>
								<button type="button" name="Next" id="Next" accessKey="N" style="display: none; float: left;">
									<b><u>N</u></b>ext&gt;&gt;</button>
								<button type="button" name="Close" id="Close" accessKey="C" >
									<b><u>C</u></b>lose</button>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
	 </form>
	</body>
</html>
