<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<%
request.setAttribute("paymentModesJSON", new flexjson.JSONSerializer().serialize(
	com.insta.hms.common.ConversionUtils.listBeanToListMap(new com.insta.hms.master.PaymentModes.PaymentModeMasterDAO().listAll())));

%>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<jsp:useBean id="receiptType" class="java.util.HashMap"/>
<c:set target="${receiptType}" property="A" value="advance"/>
<c:set target="${receiptType}" property="S" value="settlement"/>

<jsp:useBean id="paymentType" class="java.util.HashMap"/>
<c:set target="${paymentType}" property="DF" value="refund"/>
<c:set target="${paymentType}" property="DR" value="receipt_settlement"/>
<c:set target="${paymentType}" property="R" value="receipt"/>
<c:set target="${paymentType}" property="S" value="sponsor_receipt"/>
<c:set target="${paymentType}" property="F" value="refund"/>

<jsp:useBean id="sponsorIndex" class="java.util.HashMap"/>
<c:set target="${sponsorIndex}" property="P" value="pri"/>
<c:set target="${sponsorIndex}" property="S" value="sec"/>
<c:set var="no_of_credit_debit_card_digits" value='<%=GenericPreferencesDAO.getAllPrefs().get("no_of_credit_debit_card_digits") %>'/>
<c:set var="incomeTaxCashLimitApplicability" value='<%=GenericPreferencesDAO.getAllPrefs().get("income_tax_cash_limit_applicability") %>'/>
<html>
<head>
	<title><insta:ltext key="billing.receiptno.editreceipt.receipt"/> ${bean.map.receipt_no} - <insta:ltext key="billing.receiptno.editreceipt.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="/billing/EditReceipt.js"/>

	<script>
		var roleId = '${roleId}';
		var editReceiptAmounts = '${actionRightsMap.edit_receipt_amounts}';
		var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
		var jForeignCurrencyList = <%= request.getAttribute("foreignCurrencyListJSON") %>;
		var currencyRowCount = !empty(jForeignCurrencyList) ? 1 : 0;
		var gServerNow = new Date();
		var foreignCurrencyAmt = '${bean.map.currency_amt}';
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var jgetAllCreditTypes = '${getAllCreditTypes}';
		var totalBillAmt = '${bean.map.amount}';
		var paymentRowsUncloned = 2;
		var paymentRows = 6 ;
		var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
		var income_tax_cash_limit_applicability = '${incomeTaxCashLimitApplicability}';
		var mr_no = '${bean.map.mr_no}';
		var visitId = '${bean.map.payment_type != 'DF' && bean.map.payment_type != 'DR' ? bean.map.visit_id : '' }';
		var payType = '${bean.map.payment_type}';
		var visitType='${bill.visitType}';
		var max_centers_inc_default = ${genPrefs.max_centers_inc_default};
		var centerId = '<%=(Integer) session.getAttribute("centerId") %>';
	</script>
	<insta:js-bundle prefix="billing.editreceipt"/>
</head>
<body onload="init();hidePaymentModeForEditReceipt();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="auditLog">
	<insta:ltext key="common.auditlog.auditlogdetails.auditlog"/>
</c:set>
<c:set var="find">
	<insta:ltext key="common.auditlog.auditlogdetails.find"/>
</c:set>
<c:set var="dropdownEditability" value="auto" />
<c:set var="textboxEditability" value="" />
<c:set var="editValue" value="false" />
 <c:if test="${bean.map.payment_mode_id == -2 || bean.map.payment_mode_id == -3 || bean.map.payment_mode_id == -4}">
	<c:set var="textboxEditability" value="readonly" />
	<c:set var="dropdownEditability" value="none" />
	<c:set var="editValue" value="true" />
</c:if>
<c:if test="${genPrefs.max_centers_inc_default > 1 && centerId == 0}">
	<% request.setAttribute("error", "Edit of Receipt is not allowed for default center users."); %>
</c:if>
<form name="receiptNoForm" action="${cpath}/billing/editReceipt.do">
	<input type="hidden" name="_method" value="getReceipt">
	<input type="hidden" name="screen" value="${ifn:cleanHtmlAttribute(screen)}"/>
	<table width="100%">
		<tr>
			<td width="100%"><h1><insta:ltext key="billing.receiptno.editreceipt.editreceipt"/></h1></td>

			<td><insta:ltext key="billing.receiptno.editreceipt.receipt"/>&nbsp;<insta:ltext key="billing.receiptno.editreceipt.no"/>:&nbsp;</td>
			<td><input type="text" name="receiptNo" id="receiptNo" style="width: 80px"></td>
			<td><input type="submit" class="button" value="${find}"></td>
		</tr>
	</table>
</form>
<div><insta:feedback-panel/></div>

<form name="receiptForm" action="${cpath}/billing/editReceipt.do" method="POST">
	<input type="hidden" name="_method" value="updateReceiptDetails">
	<input type="hidden" name="receiptNo" value="${bean.map.receipt_no}"/>
	<input type="hidden" name="billNo" value="${bean.map.payment_type != 'DF' && bean.map.payment_type != 'DR' ? bean.map.bill_no : ''}"/>
	<input type="hidden" name="screen" value="${ifn:cleanHtmlAttribute(screen)}"/>
	<input type="hidden" name="receiptPaymentType" value="${(bean.map.payment_type == 'DF' || bean.map.payment_type == 'DR') ? 'Deposit' : ''}"/>

	<input type="hidden" name="billOpenedDate" value="<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>">
	<input type="hidden" name="billOpenedTime" value="<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/>">

	<input type="hidden" name="billCloseDate" value="<fmt:formatDate value="${bill.closedDate}" pattern="dd-MM-yyyy"/>">
	<input type="hidden" name="billCloseTime" value="<fmt:formatDate value="${bill.closedDate}" pattern="HH:mm"/>">

<c:set var="allowAmountEdit"
	value="${(roleId == 1 || roleId == 2 || actionRightsMap.edit_receipt_amounts == 'A')
					&& (not empty bill && bill.paymentStatus == 'U'
					&& (empty bill.primaryClaimStatus || bill.primaryClaimStatus == 'O')
					&& (empty bill.secondaryClaimStatus || bill.secondaryClaimStatus == 'O')) 
					&& (bean.map.payment_type != 'F')}"/>
<c:set var="allowEdit" value="${roleId == 1 || roleId == 2 || actionRightsMap.edit_receipt_amounts == 'A'}"/>
<c:set var="allowReceiptBackDate" value="${roleId == 1 || roleId == 2 || actionRightsMap.allow_receipt_backdate == 'A'}"/>

<fieldset class="fieldSetBorder">
<table id="receiptTable" class="formtable" align="center">
	<tr>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.receiptno"/>:</td>
		<td class="forminfo">${bean.map.receipt_no}</td>
        <c:set var="isModInsExtEnabled" value ="${preferences.modulesActivatedMap['mod_ins_ext'] eq 'Y'}"/>
		<c:if test="${bean.map.payment_type != 'DF' && bean.map.payment_type != 'DR' && !isModInsExtEnabled}">
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.billno"/>:</td>
		<td class="forminfo">${bean.map.bill_no}</td>
		</c:if>
		<c:if test="${genPrefs.max_centers_inc_default > 1 && (bean.map.payment_type == 'DF' || bean.map.payment_type == 'DR')}">
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.center"/>:</td>
		<td class="forminfo">${bean.map.center_name == null ?  'All Centers' : bean.map.center_name}</td>
		</c:if>
	</tr>
	<tr>
		<c:set var="displaydate"><fmt:formatDate value="${bean.map.display_date}" pattern="dd-MM-yyyy"/></c:set>
		<c:set var="displaytime"><fmt:formatDate value="${bean.map.display_date}" pattern="HH:mm"/></c:set>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.receiptdateortime"/>:</td>
		<td class="forminfo">
			<fmt:formatDate var="displayDate" pattern="dd-MM-yyyy" value="${bean.map.display_date}"/>
			<fmt:formatDate var="displayTime" pattern="HH:mm" value="${bean.map.display_date}"/>
			<c:choose>
				<c:when test="${allowReceiptBackDate}">
					<insta:datewidget name="display_date" value="${displayDate}"  editValue="${editValue}" btnPos="left"/>
					<input type="text" ${textboxEditability} size="4" name="display_time" id="display_time" value="${displayTime}" class="timefield"/>
				</c:when>
				<c:otherwise>
						<label>${displayDate} ${displayTime}</label>
						<input type="hidden" name="display_date" id="display_date" value="${displayDate}"/>
						<input type="hidden" name="display_time" id="display_time" value="${displayTime}"/>
				</c:otherwise>
			</c:choose>
		</td>

		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.counter"/>:</td>
		<c:choose>
			<c:when test="${allowEdit && !(genPrefs.max_centers_inc_default > 1 && centerId == 0)}">
				<td class="forminfo">
					<insta:selectdb name="counter" id="counter" table="counters" filtercol="center_id" filtervalue="${centerId}"
					valuecol="counter_id" displaycol="counter_no" value="${bean.map.counter}"  style="pointer-events: ${dropdownEditability}"/>
				</td>
			</c:when>
		<c:otherwise>
			<td class="forminfo">
				<label>${bean.map.counter_name}</label>
				<input type="hidden" name="counter" id="counter" value="${bean.map.counter}" />
			</td>
		</c:otherwise>
		</c:choose>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.remarks"/>:</td>
		<td class="forminfo">
			<input type="text" name="remarks" id="remarks" maxlength="50" value="${bean.map.remarks}" />
		</td>
	</tr>

	<tr>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.paymenttype"/>:</td>
		<td class="forminfo">
			<c:set var="paymentSelValue" value=""/>
			<c:choose>

			<c:when test="${bean.map.payment_type != 'DF' && bean.map.payment_type != 'DR' && bean.map.payment_type != 'F'}">
				<c:choose>
					<c:when test="${bean.map.payment_type == 'S'}">
						<c:set var="paymentSelValue" value="${sponsorIndex[bean.map.sponsor_index]}_${paymentType[bean.map.payment_type]}_${receiptType[bean.map.recpt_type]}"/>
					</c:when>
					<c:otherwise>
						<c:set var="paymentSelValue" value="${paymentType[bean.map.payment_type]}_${receiptType[bean.map.recpt_type]}"/>
					</c:otherwise>
				</c:choose>
			</c:when>
			<c:otherwise>
				<c:set var="paymentSelValue" value="${paymentType[bean.map.payment_type]}"/>
			</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${allowAmountEdit}">
					<select class="dropdown" name="paymentType" id="paymentType" style="pointer-events: ${dropdownEditability}">
						<c:choose>
							<c:when test="${paymentSelValue == 'refund'}">
									<option value="refund" ${paymentSelValue == 'refund'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.refund"/></option>
							</c:when>
							<c:otherwise>

							<c:if test="${bean.map.payment_type == 'R' || bean.map.payment_type == 'F'}">
								<c:if test="${bill.billType != 'P' || (bill.billType == 'P' && bill.is_tpa)}">
									<option value="receipt_advance" ${paymentSelValue == 'receipt_advance'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.advance"/></option>
								</c:if>
								<option value="receipt_settlement" ${paymentSelValue == 'receipt_settlement'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.settlement"/></option>
							</c:if>

							<c:if test="${bean.map.payment_type == 'S'}">
								<c:if test="${bill.is_tpa && empty bill.sponsorBillNo}">
									<option value="pri_sponsor_receipt_advance" ${paymentSelValue == 'pri_sponsor_receipt_advance'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.pri.sponsoradvance"/></option>
									<option value="pri_sponsor_receipt_settlement" ${paymentSelValue == 'pri_sponsor_receipt_settlement'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.pri.sponsorsettlement"/></option>
									<option value="sec_sponsor_receipt_advance" ${paymentSelValue == 'sec_sponsor_receipt_advance'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.sec.sponsoradvance"/></option>
									<option value="sec_sponsor_receipt_settlement" ${paymentSelValue == 'sec_sponsor_receipt_settlement'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.sec.sponsorsettlement"/></option>
								</c:if>
							</c:if>
							<c:if test="${bean.map.payment_type == 'DR' || bean.map.payment_type == 'DF'}">
								<option value="receipt_settlement" ${paymentSelValue == 'receipt_settlement'?'selected':''}><insta:ltext key="billing.receiptno.editreceipt.sec.sponsorsettlement"/></option>
							</c:if>

							</c:otherwise>
						</c:choose>
								</c:when>
				<c:otherwise>
					<c:choose>
						<c:when test="${bean.map.payment_type == 'DR' || bean.map.payment_type == 'DF'}">
							<label id="paymentType">${bean.map.payment_type == 'DF' ?  'Refund' : 'Settlement'}</label>
						</c:when>
						<c:otherwise>
							<label id="paymentType">${bean.map.payment_type == 'F' ? 'Refund' : (bean.map.recpt_type == 'A' ?  'Advance' : 'Settlement')}</label>
						</c:otherwise>
					</c:choose>
					<input type="hidden" name="paymentType" id="paymentType" value="${paymentSelValue}"/>
				</c:otherwise>
			</c:choose>
		</td>

		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.paymentmode"/>:</td>
		<td class="forminfo"><insta:selectdb name="payment_mode_id" style="pointer-events: ${dropdownEditability}" id="payment_mode_id" table="payment_mode_master"
						valuecol="mode_id" displaycol="payment_mode" value="${bean.map.payment_mode_id}" dummyvalue="${dummyvalue}"
						onChange="enableBankDetails(this);enableCommissionDetails(this);"/></td>

		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.paidby"/>:</td>
		<td class="forminfo"><input type="text" name="paid_by" id="paid_by" value="${bean.map.paid_by}"/></td>
	</tr>

	<tr>
		<c:if test="${not empty foreignCurrencyList}">
		<c:choose>
		<c:when test="${allowAmountEdit}">
			<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.currency"/>:</td>
			<td class="forminfo">
				<insta:selectdb name="currency_id"  id="currency_id" table="foreign_currency"
					valuecol="currency_id" displaycol="currency" value="${bean.map.currency_id}" dummyvalue="${dummyvalue}"
					onchange="getCurrencyDetails();"/>
			</td>

			<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.currencyamt"/>:</td>
			<td class="forminfo">
				<input type="text" name="currency_amt" id="currency_amt" value="${bean.map.currency_amt}" onchange="convertCurrency()"/>
			</td>

			<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.exchangerate"/>:</td>
			<td class="forminfo"><input type="text" name="exchange_rate" id="exchange_rate" value="${bean.map.exchange_rate}"/></td>
		</c:when>
		<c:otherwise>
			<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.currency"/>:</td>
			<td class="forminfo">
				<label>${bean.map.currency}</label>
				<input type="hidden" name="currency_id" id="currency_id" value="${bean.map.currency_id}"/>
			</td>
			<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.currencyamt"/>:</td>
			<td class="forminfo">
				<label>${bean.map.currency_amt}</label>
				<input type="hidden" name="currency_amt" id="currency_amt" value="${bean.map.currency_amt}"/>
			</td>
			<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.exchangerate"/>:</td>
			<td class="forminfo">
				<label>${bean.map.exchange_rate}</label>
				<input type="hidden" name="exchange_rate" id="exchange_rate" value="${bean.map.exchange_rate}"/>
			</td>
		</c:otherwise>
		</c:choose>
		</c:if>
	</tr>
	<tr>
		<c:if test="${not empty foreignCurrencyList}">
		<fmt:formatDate var="exchangeDate" pattern="dd-MM-yyyy" value="${bean.map.exchange_date}"/>
		<fmt:formatDate var="exchangeTime" pattern="HH:mm" value="${bean.map.exchange_date}"/>
		<c:choose>
			<c:when test="${allowEdit && bean.map.currency_amt > 0}">
				<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.exchangedate"/>:</td>
				<td class="forminfo">
					<insta:datewidget name="exchange_date" id="exchange_date" value="${exchangeDate}"  btnPos="left"/>
					<input type="text" size="4" name="exchange_time" id="exchange_time" value="${exchangeTime}" class="timefield"/>
				</td>
			</c:when>
			<c:otherwise>
				<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.exchangedate"/>:</td>
				<td class="forminfo">
					<label>${bean.map.exchange_date}</label>
					<input type="hidden" name="exchange_date" id="exchange_date" value="${exchangeDate}" />
					<input type="hidden" name="exchange_time" id="exchange_time" value="${exchangeTime}"/>
				</td>
			</c:otherwise>
		</c:choose>
		</c:if>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.amount"/>:</td>
		<td class="forminfo">
			<c:choose>
				<c:when test="${allowAmountEdit}">
						<input type="text" ${textboxEditability} name="amount" id="amount" value="${ifn:afmt(ifn:abs(bean.map.amount))}" onchange="enableCommissionDetails(this);"/>
				</c:when>
				<c:otherwise>
						<label id="amount">${ifn:afmt(ifn:abs(bean.map.amount))}</label>
						<input type="hidden" name="amount" ${textboxEditability} id="amount" value="${ifn:afmt(ifn:abs(bean.map.amount))}" />
				</c:otherwise>
			</c:choose>
		</td>
			<c:if test="${ not empty genPrefs.showVAT && genPrefs.showVAT eq 'Y'
								&& (paymentSelValue == 'pri_sponsor_receipt_advance' || paymentSelValue == 'pri_sponsor_receipt_settlement'
								   || paymentSelValue == 'sec_sponsor_receipt_advance' || paymentSelValue == 'sec_sponsor_receipt_settlement')}">
				<td class="formlabel"> <insta:ltext key="billing.receiptno.editreceipt.tdsamt"/>:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${allowAmountEdit}">
							<input type="text" name="tds_amt" id="tds_amt" value="${ifn:afmt(ifn:abs(bean.map.tds_amt))}"/>
						</c:when>
						<c:otherwise>
							<label id="tdsamount">${ifn:afmt(ifn:abs(bean.map.tds_amt))}</label>
							<input type="hidden" name="tds_amt" id="tds_amt" value="${ifn:afmt(ifn:abs(bean.map.tds_amt))}" />
						</c:otherwise>
					</c:choose>
				</td>
			</c:if>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.cardtype"/>:</td>
		<td class="forminfo"><insta:selectdb name="card_type_id" id="card_type_id" table="card_type_master"
						valuecol="card_type_id" displaycol="card_type" value="${bean.map.card_type_id}" dummyvalue="${dummyvalue}" onchange="enableCommissionDetails(this);"/></td>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.bank"/>:</td>
		<td class="forminfo">
			<insta:selectdb name="bank_name" id="bank_name" dummyvalue="${dummyvalue}" disabled="disabled"
					 table="bank_master" displaycol="bank_name" valuecol="bank_name" value="${bean.map.bank_name}" orderby="bank_name"/>
		</td>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.refnum"/>:</td>
		<td class="forminfo"><input type="text" name="reference_no" id="reference_no" value="${bean.map.reference_no}" /></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.bankbatchno"/>:</td>
		<td class="forminfo"><input type="text" name="bank_batch_no" id="bank_batch_no" value="${bean.map.bank_batch_no}" /></td>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.cardauthcode"/>:</td>
		<td class="forminfo"><input type="text" name="card_auth_code" id="card_auth_code" value="${bean.map.card_auth_code}"/></td>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.cardholdername"/>:</td>
		<td class="forminfo"><input type="text" name="card_holder_name" id="card_holder_name" value="${bean.map.card_holder_name}"/></td>
	</tr>

	<tr>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.cardnumber"/>:</td>
		<td class="forminfo"><input type="text" name="card_number" id="card_number" value="${bean.map.card_number}" maxlength=${no_of_credit_debit_card_digits ne 0 ? no_of_credit_debit_card_digits : ""}/></td>

		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.cardexpirydate"/>:</td>
		<td class="forminfo">
			<fmt:formatDate var="cardExpDate" pattern="MM-yy" value="${bean.map.card_expdate}"/>
			<input type="text" name="card_expdate" id="card_expdate" value="${cardExpDate}">
			<img class="imgHelpText" title='<insta:ltext key="billing.receiptno.editreceipt.validcardexpirydate"/>'
			 src="${cpath}/images/help.png"/>
		</td>
		<td class="formlabel">
		<div id="commissionPer_div" style="display: none;" >
		Commission(%):</td>
		<td class="forminfo" id="commissionPer"></td>
		<input type="hidden" name="commissionPercent" id="commissionPercent"  value=""/>
		</div>
		</td>
	</tr>
	
	<tr>
		<td  class="formlabel">
			<div id="commissionAmt_div" style="display: none;" >
				Commission Amount:
			<td class="forminfo" id="commissionAmt"></td>
			<input type="hidden" name="commissionAmount" id="commissionAmount" value=""/>
			</div>
		</td>
	</tr>
	<tr>
	<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.mobNumber"/>:</td>
		<td class="forminfo"><input type="text" ${textboxEditability} readonly value="${bean.map.mob_number}"/></td>
		<td class="formlabel"><insta:ltext key="billing.receiptno.editreceipt.totp"/>:</td>
		<td class="forminfo"><input type="text" ${textboxEditability} readonly value="${bean.map.totp}"/></td>
	</tr>
</table>

<table cellpadding="0" cellspacing="0"  border="0" width="100%">
		<tr>
			<td align="left">
				<button type="button" accesskey="S" onclick="doSave();"><b><u><insta:ltext key="billing.receiptno.editreceipt.s"/></u></b><insta:ltext key="billing.receiptno.editreceipt.ave"/></button>
			<c:if test="${bean.map.payment_type != 'DF' && bean.map.payment_type != 'DR'}">
				<insta:screenlink target="_blank" screenId="all_receipts_audit_log"
						extraParam="?_method=getAuditLogDetails&receipt_no=${bean.map.receipt_no}&al_table=all_receipts_audit_view"
							label="${auditLog}"/>
			</c:if>
			<c:if test="${bean.map.payment_type == 'DF' || bean.map.payment_type == 'DR'}">
				<insta:screenlink target="_blank" screenId="all_receipts_audit_log"
						extraParam="?_method=getAuditLogDetails&deposit_no=${bean.map.receipt_no}&al_table=all_receipts_audit_view"
							label="${auditLog}"/>
			</c:if>
			</td>
		</tr>
</table>

</fieldset>
</form>
</body>
</html>
