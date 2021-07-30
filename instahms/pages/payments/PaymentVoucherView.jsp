<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<html>
	<head>
  	<title>View Payment Voucher - Insta HMS</title>
		<meta http-equiv="Content-Type" content="text/html charset=iso-8859-1" >

		<insta:link type="css" file="widgets.css"/>
		<script>
				function getPrint(){
				var voucher = document.getElementById("voucherType").value;
				var payType = document.getElementById('paymentType').value;
				var printerType=document.getElementById("printDefType").value;
					if (document.forms[0].voucherPrint != null){
						document.forms[0].printType.value = document.forms[0].voucherPrint.value;
						window.open(document.forms[0].action+"?_method=printVoucher&voucherno="+document.forms[0].voucherno.value+"&printType="+document.forms[0].printType.value+"&voucherType="+voucher+"&paymentType="+payType+"&printDefType="+printerType);
					}else{
						window.open(document.forms[0].action+"?_method=printVoucher&voucherno="
							+document.forms[0].voucherno.value+"&printType=summary&voucherType=P&printDefType="+printerType);
					}
					document.forms[0].target = "_blank";
				}

		</script>
	</head>
	<c:set var="vouchers" value="${vouchersList}"/>
	<body>
		<div><!-${vouchers}-></div>
		<c:set var="actionUrl" value="/pages/payments/PaymentVoucherPrint.do" />
		<c:set var="defaultVoucherPrint"  value="<%=GenericPreferencesDAO.getGenericPreferences().getDefault_voucher_print() %>"/>
		<c:set var="taxDeductionLabel" value='<%=GenericPreferencesDAO.getAllPrefs().get("tax_deduction_label_for_payment_voucher") %>' />
		<html:form method="GET" action="${actionUrl}">
		<html:hidden property="method" value="printVoucher"/>
		<html:hidden property="printType" value="summary"/>
		<c:set var="amt" value="0"/>
			<div>
				<div>
					<table class="formtabel">
						<c:set var="vno" value=""/>
						<c:forEach var="v" items="${vouchers}">
						<c:choose>
						<c:when test="${vno!=v.map.voucher_no}">
							<tr>
								<td class="pageHeader">${v.map.payment_type}</td>
								<c:set var="payType" value="${v.map.payment_type}" />
 									<input type="hidden" name="voucherType" id="voucherType" value="${v.map.voucher_category}"/>
									<input type="hidden" name="paymentType" id="paymentType" value="${v.map.paymenttype}"/>
							</tr>
					</table>

					<fieldset class="fieldSetBorder">
					<table class="formtable">
						<tr>
							<td class="formlabel">Voucher No:</td>
							<td class="forminfo">${v.map.voucher_no}
									<input type="hidden" id="voucherno" name="voucherno" value="${v.map.voucher_no}"/>
							</td>
							<td class="formlabel">Payee Name:</td>
							<td class="forminfo">${v.map.payee_name}</td>
							<td class="formlabel">Date:</td>
							<td class="forminfo"><fmt:formatDate value="${v.map.date}" pattern="dd-MM-yyyy"/></td>
						</tr>

				    </c:when>
						</c:choose>
						<tr>
							<td class="formlabel">Description :</td>
							<td style="font-weight:bold;white-space:nowrap;">${v.map.description}</td>
							<td class="formlabel">Category :</td>
							<td class="forminfo">${v.map.category}</td>
							<td class="formlabel">Amount:</td>
							<td class="forminfo">${v.map.amount}</td>
						</tr>
							<c:set var="vno"	value="${v.map.voucher_no}"/>
						<c:set var="amt" value="${amt+v.map.amount}"/>
						</c:forEach>
					</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					<table class="formtable">
						<c:forEach var="v1" items="${vouchers}">
							<c:if test="${vno==v1.map.voucher_no}">
						<tr>
							<td class="formlabel">Mode of Payment:</td>
							<td class="forminfo">${v1.map.payment_mode}</td>
							<c:if test="${not empty v1.map.bank}">
								<td class="formlabel">Bank:</td>
								<td class="forminfo">${v1.map.bank}</td>
							</c:if>
							<c:if test="${not empty v1.map.reference_no}">
								<td class="formlabel">Reference No</td>
								<td class="forminfo">${v1.map.reference_no}</td>
							</c:if>
						</tr>
						<tr>
							<td class="formlabel">Remarks:</td>
							<td class="forminfo">${v1.map.remarks}</td>
						</tr>
						<tr>
							<td class="formlabel">Tax Amount:</td>
							<td class="forminfo">${v1.map.tax_amount}</td>
							<c:choose>
							<c:when test="${v1.map.payment_type eq 'Supplier Payments'}">
							<td class="formlabel">VAT Amount:</td>
							<td class="forminfo">${v1.map.tds_amount}</td>
							</c:when>
							<c:otherwise>
							<td class="formlabel">
								<c:choose>
									<c:when test="${taxDeductionLabel == 'WHT' }"><insta:ltext key="ui.label.wht"/>&nbsp<insta:ltext key="ui.label.amount"/>:</c:when>
									<c:otherwise><insta:ltext key="ui.label.tds"/>&nbsp<insta:ltext key="ui.label.amount"/>:</c:otherwise>
								</c:choose>
							</td>
							<td class="forminfo">${v1.map.tds_amount}</td>
							</c:otherwise>
							</c:choose>
							<td class="formlabel">Total Amount:</td>
							<td class="forminfo">${(amt - v1.map.tax_amount) + v1.map.tds_amount}</td>
				 	 	</tr>
					  </c:if>
						<c:set var="vno"	value="${v.map.voucher_no}"/>
						</c:forEach>
					</table>
				</fieldset>
				</div>
			</div>
			<div class="screenActions" style="float:left">
				<button type="button" name="print" accesskey="P" class="button" onclick="return getPrint();"/>
					<u><b>P</b></u>rint</button>&nbsp;
			</div>
			<div style="float: right">
				<table class="screenActions">
					<tr>
						<td>
							<insta:selectoptions name="voucherPrint" id="voucherPrint" opvalues="summary,detail"
							optexts="Summary Voucher Print, Detail Voucher Print" value="${defaultVoucherPrint}"/></td>
						<td>&nbsp;</td>
						<td><insta:selectdb name="printDefType" id="printDefType" table="printer_definition"
							valuecol="printer_id"  displaycol="printer_definition_name"
							value="${pref.map.printer_id}"/></td>
					</td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</div>

	 </html:form>
	</body>
</html>
