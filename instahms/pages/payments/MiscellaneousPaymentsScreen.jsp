<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Misc Payments - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html charset=iso-8859-1" >
	<insta:link type="js" file="hmsvalidation.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="payments/miscpayment.js" />

	<script>
		var centerId = <%= session.getAttribute("centerId") %>;
		var maxcenters = <%=com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>;
	</script>

</head>
<body onload="init();hidePaymentModeForPaymentVoucher();">
	<div class="pageHeader">Miscellaneous Payments</div>
	<c:set var="counter" value="${requestScope.counterList}"/>
	<form method="GET" action="${cpath}/pages/payments/MiscPayments.do" name="miscPaymentForm" autocomplete="off">
		<input type="hidden" name="_method" value="saveMiscPayments"/>
		<input type="hidden" name="payAll" value="${payment.paymentType}"/>
		<input type="hidden" name="screenAction"  id="screenAction"/>
			<fieldset class="fieldSetBorder"><legend>Payees</legend>
				<table class="formtable" width="100%" >
					<tr>
						<td class="formlabel"><b>Payee Name : </b></td>
						<td><input type="text" name="name" size="15" maxlength="30" onkeypress="return enterAlpha(event);"
								onblur="capWords(this)" />
						</td>
						<td class="formlabel">
							<b>Date :</b>
						<td>
						<td>
							<c:set var="readonly" value="true"/>
							<c:if test="${ (roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A') }">
								<c:set var="readonly" value="false"/>
							</c:if>
							<insta:datewidget name="paydate" valid="past" value="today" btnPos="left" id="payDate"
								editValue="${readonly}"/>
							<input type="hidden" name="payTime" size="4"
								value='<fmt:formatDate value="${serverNow}" pattern="HH:mm"/>'/>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
					</tr>
				</table>
			</fieldset>
			<fieldset	class="fieldSetBorder"><legend>Posting Payments</legend>
				<div class="detailList" style="margin: 10px 0px 5px 0px;">
					<table class="detailList" cellspacing="0" cellpadding="0" id="paymentsTable" border="0" width="100%">
						<tr>
							<th>Account Head</th>
							<th>Description / Purpose</th>
							<th>Category</th>
							<th align="right">Amount</th>
							<th></th>
							<th></th>
						</tr>
						<tr style="display: none">
							<td>
								<img src="${cpath}/images/empty_flag.gif"/>
								<label></label>
								<input type="hidden" name="accountHead" id="accountHead"/>
								<input type="hidden" name="isNew" id="isNew" value="true"/>
								<input type="hidden" name="edited" value='false'/>
							</td>
							<td>
								<label></label>
								<input type="hidden" name="description" id="description"/>
							</td>
							<td>
								<label></label>
								<input type="hidden" name="category" id="category"/>
							</td>
							<td class="number">
								<label></label>
								<input type="hidden" name="amount" id="amount"/>
							</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Item" onclick="return cancelPayment(this);" title="Cancel Item">
									<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
								</a>
							</td>
							<td style="text-align: center">
								<input type="hidden" name="delPayment" id="delPayment" value="false" />
								<a name="_editAnchor" href="javascript:Edit" onclick="return showEditPaymentDialog(this);" title="Edit Payment Details">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</td>

						</tr>
						<tr class="footer" >
							<td colspan="5" style="text-align: right">
								Total Amount:&nbsp;<label id="lblTotalAmt" style="font-weight: bold;">0</label>
							</td>
							<td style="text-align: center">
								<button type="button" name="btnAddItem" id="btnAddItem" title="Add New Payment"
									onclick="addPaymentDialog(this); return false;"
									accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
							</td>
						</tr>
					</table>
				</div>
			</fieldset>
		<div class="screenActions">
			<button type="button" name="pay" id="pay"  accesskey="S"  class="button" onclick="return validateForm(this);"  value="save">
			<b><u>S</u></b>ave</button>
		<button type="button" name="voucher" id="voucher" accesskey="V" class="button" onclick="return validateForm(this);" value="voucher">
			Create<b><u>V</u></b>oucher</button>
			 <img class="imgHelpText" title="When click on CreateVoucher button this amount is added with any existing amount of this payee ." src="${cpath}/images/help.png"/>
			<a href="PaymentDashboard.do?_method=getPaymentDues">Back to PaymentDues Dashboard</a>
		</div>
		<div id="addPaymentDialog" style="display: none">
			<div class="bd">
				<input type="hidden" id="editRowId" value=""/>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="titleForAdd">Add Miscellaneous Payments</legend>
					<legend class="fieldSetLabel" id="titleForEdit">Edit Miscellaneous Payments</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Account Head</td>
							<td><select name="_dAccountHead" id="_dAccountHead" class="dropdown">
								</select></td>
						</tr>
						<tr>
							<td class="formlabel">Description / Purpose</td>
							<td><input type="text" name="_dDescription" id="_dDescription" maxlength="200"></td>
						</tr>
						<tr>
							<td class="formlabel">Category</td>
							<td><input type="text" name="_dCategory" id="_dCategory" maxlength="250"/></td>
						</tr>
						<tr>
							<td class="formlabel">Amount</td>
							<td><input type="text" name="_dAmount" id="_dAmount" size="15" maxlength="13"
								class="number" onkeypress="return enterNumAndDotAndMinus(event);"
								onchange="return makeingDec(this.value,this)"
								onblur="if (this.value != '') {roundEnteredNumber(this.value,2); }"/></td>
						</tr>
					</table>
					<table style="margin-top: 10px;">
						<tr>
							<td>
								<button type="button" name="Add" id="Add" value="Add" accessKey="A" style="display: block; float:left">
									<b><u>A</u></b>dd
								</button>
								<button type="button" name="Ok" id="Ok" value="Ok" accesskey="O" style="display: block; float:left">
									<b><u>O</u></b>k
								</button>
								<button type="button" name="Previous" id="Previous" accessKey="P" style="display: none; float: left">
									&lt;&lt;<b><u>P</u></b>revous
								</button>
								<button type="button" name="Next" id="Next" accessKey="N" style="display: none; float: left">
									<b><u>N</u></b>ext&gt;&gt;
								</button>

								<button type="button" name="Close" id="Close" value="Close" accessKey="C">
									<b><u>C</u></b>ancel
								</button>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
	</form>
	<script>
		var accountHeadList = ${accountHeadsJSON};
	</script>
</body>
</html>
