<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<html>
		<head>
			<title>Payment Voucher - Insta HMS</title>
			<meta http-equiv="Content-Type" content="text/html charset=iso-8859-1" >

			<insta:link type="js" file="hmsvalidation.js"/>
			<insta:link type="js" file="ajax.js"/>
			<insta:link type="css" file="widgets.css"/>
			<insta:link type="js" file="dashboardsearch.js" />
			<insta:link type="js" file="payments/voucher.js" />
			<insta:link type="js" file="paymentCommon.js" />
			<insta:js-bundle prefix="ui.label"/>
		</head>

	<body onload="init();hidePaymentModeForPaymentVoucher();">
		<c:set var="payee" value="${requestScope.payeeList}"/>
		<c:set var="payment" value="${requestScope.paymentList}"/>
		<c:set var="counter" value="${requestScope.counterList}"/>
		<c:set var="defaultVoucherPrint" value="<%=GenericPreferencesDAO.getGenericPreferences().getDefault_voucher_print()%>" />
		<c:set var="taxDeductionLabel" value='<%=GenericPreferencesDAO.getAllPrefs().get("tax_deduction_label_for_payment_voucher") %>' />
		<div><!-${vouchers}-></div>
		<html:form method="POST" action="${actionUrl}">
		<html:hidden property="_method" value="makePayment"/>
		<html:hidden property="payeeName" value="${payment.payeeName}"/>
		<html:hidden property="printType" value="summary"/>
		<input type="hidden" name="directPayment" value="${payment.directPayment}"/>
		<html:hidden property="screen" value="${payment.screen}"/>
		<div class="pageHeader">
				${payment.screen} Voucher
		</div>
		<div>
			<insta:feedback-panel/>
		</div>
		<jsp:useBean id="now" class="java.util.Date"/>
		<c:set var="dt" value="${now}"/>
		<fmt:formatDate value="${dt}" pattern="HH:mm" var="time"/>
		<fmt:formatDate value="${dt}" pattern="dd-MM-yyyy" var="date"/>

		<div>
			<fieldset class="fieldsetBorder"><legend>Payee Details </legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Payment Type :</td>
					<c:choose>
					<c:when test="${payment.paymentType=='C'}">
						<td class="forminfo">Cash Voucher
						<html:hidden property="paymentType" value="${payment.paymentType}" /></td>
					</c:when>
					<c:when test="${payment.paymentType=='D'}">
					<td class="forminfo">Doctor Payment
						<html:hidden property="paymentType" value="${payment.paymentType}" /></td>
					</c:when>
					<c:when test="${payment.paymentType=='P'}">
						<td class="forminfo">Prescribing Doctor Payment
						<html:hidden property="paymentType" value="${payment.paymentType}" /></td>
					</c:when>
					<c:when test="${payment.paymentType=='R' || payment.paymentType == 'F'}">
						<td class="forminfo">Referral Doctor Payment
						<html:hidden property="paymentType" value="${payment.paymentType}" /></td>
					</c:when>
					<c:when test="${payment.paymentType=='O'}">
						<td class="forminfo">Out Hospital Payment
						<html:hidden property="paymentType" value="${payment.paymentType}" /></td>
					</c:when>

					<c:when test="${payment.paymentType=='S'}">
						<td class="forminfo">Supplier Payments
						<html:hidden property="paymentType" value="${payment.paymentType}" /></td>
					</c:when>
					</c:choose>
						<td class="formlabel">Payee Name :</td>
						<td class="forminfo">${payment.payeeName}
							<html:hidden property="payeeName" value="${payment.payeeName}" />
							<html:hidden property="payeeId" value="${payment.payeeId}"/>
						</td>
					</tr>

					<tr>
						<td class="formlabel">Date :</td>
						<td class="forminfo">
							<c:choose>
								<c:when test="${ (roleId == 1) || (roleId == 2) || (actionRightsMap.allow_backdate == 'A') }">
										<insta:datewidget name="payDate" valid="past" value="${date}" btnPos="left"
												id="payDate"  editValue="false"/>
										<input type="text" name="payTime" value="${time}" class="timefield" />
								</c:when>
								<c:otherwise>
										<insta:datewidget name="payDate" valid="past" value="${date}" btnPos="left"
											id="payDate"  editValue="true"/>
										<input type="text" name="payTime" value="${time}" class="timefield" readonly/>
								</c:otherwise>
							</c:choose>
						</td>
						<td class="formlabel">Counter :</td>
						<td class="forminfo">
							<insta:selectoptions name="counter" value="${billingcounterId}" opvalues="${billingcounterId},${pharmacyCounterId}" optexts="${billingcounterName},${pharmacyCounterName}"/>
						</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
				</tr>
			</table>
			</fieldSet>
			</div>
					<fieldset class="fieldsetBorder"><legend>Payment Details</legend>
					<table class="formtable" cellpadding="0" cellspacing="0" width="100%">
						<tr>
							<td class="formlabel">Total Amount :</td>
							<td class="frominfo">
								<html:text property="totalAmount" styleId="totalAmount" size="10" readonly="true" value="${payment.amount}"/>
							</td>
							<td class="formlabel">Service Tax: </td>
							<td class="forminfo">
								<html:text property="serviceTax" size="10"  value="0.00"
									onkeypress="return enterNumOnly(event);" onchange="return taxAmount();"
									onblur="return validateNum(this)" styleId="serviceTax" />%
							</td>
							<td class="formlabel">
								<c:choose>
									<c:when test="${taxDeductionLabel == 'WHT' }"><insta:ltext key="ui.label.wht"/>:</c:when>
									<c:otherwise><insta:ltext key="ui.label.tds"/>:</c:otherwise>
								</c:choose>
							</td>
							<td class="forminfo">
								<html:text property="tds" size="10" value="0.00" styleId="tds"
									onkeypress="return enterNumOnly(event);" onchange="return taxAmount();"
									onblur="return validateNum(this)"/>%
							</td>
						</tr>

						<insta:paymentdetails/>

						<tr>
							<td class="formlabel">Round off:
								<input type="checkbox" name="roundoff" onclick="onChangeRoundOff();"/>
							</td>
							<td class="forminfo">
								<input type="text" name="roundOffAmt" readonly value=""/>
							</td>

							<td class="formlabel">Net Payment:</td>
							<td class="forminfo">
								<html:text property="netPayment" size="10" readonly="true" value="${payment.amount}"/>
							</td>

						</tr>
					</table>
				</fieldset>
				<div class="screenActions" style="float:left">
					<button type="button" name="savePrint" accesskey="S" onclick="return formValidation();"/>
						<b><u>S</u></b>ave And Print</button>&nbsp;
				</div>
				<div class="screenActions" style="text-align:right">
						<insta:selectoptions name="voucherPrint" id="voucherPrint"  opvalues="summary,detail"
						optexts="Summary Voucher Print, Detail Voucher Print" value="${defaultVoucherPrint}"/>
						<insta:selectdb name="printDefType" table="printer_definition" valuecol="printer_id"
						  	displaycol="printer_definition_name" 	value="${pref.map.printer_id}"/>
				</div>
	 </html:form>
	</body>
</html>
