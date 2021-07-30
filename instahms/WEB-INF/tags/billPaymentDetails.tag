<%@tag import="com.bob.hms.common.RequestContext"%>
<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<%@attribute name="formName" required="true" %>
<%@attribute name="dateCounterRequired" required="false" %>
<%@attribute name="isBillNowPayment" required="false" %>
<%@attribute name="defaultPaymentType" required="false" %>
<%@attribute name="isRefundPayment" required="false" %>
<%@attribute name="isInsuredPayment" required="false" %>
<%@attribute name="isPrimarySponsorPayment" required="false" %>
<%@attribute name="isSecondarySponsorPayment" required="false" %>
<%@attribute name="isPaymentTypeByJs" required="false" %>
<%@attribute name="primarySponsor" required="false" %>
<%@attribute name="secondarySponsor" required="false" %>
<%@attribute name="hasRewardPointsEligibility" required="false" %>
<%@attribute name="availableRewardPoints" required="false" %>
<%@attribute name="availableRewardPointsAmount" required="false" %>
<%@attribute name="origBillStatus" required="false" %>

<c:set var="default_payment_type" value=""/>

<c:set var="is_bill_now" value="false"/>
<c:set var="is_refund" value="false"/>
<c:set var="is_insured" value="false"/>
<c:set var="is_pri_sponsor" value="false"/>
<c:set var="is_sec_sponsor" value="false"/>
<c:set var="is_pay_by_js" value="false"/>

<c:if test="${empty dateCounterRequired}"><c:set var="dateCounterRequired" value="true"/></c:if>
<c:if test="${not empty defaultPaymentType}"><c:set var="default_payment_type" value="${defaultPaymentType}"/></c:if>

<c:if test="${not empty isBillNowPayment && isBillNowPayment}"><c:set var="is_bill_now" value="true"/></c:if>
<c:if test="${not empty isRefundPayment && isRefundPayment}"><c:set var="is_refund" value="true"/></c:if>
<c:if test="${not empty isInsuredPayment && isInsuredPayment}"><c:set var="is_insured" value="true"/></c:if>
<c:if test="${not empty isPrimarySponsorPayment && isPrimarySponsorPayment}"><c:set var="is_pri_sponsor" value="true"/></c:if>
<c:if test="${not empty isSecondarySponsorPayment && isSecondarySponsorPayment}"><c:set var="is_sec_sponsor" value="true"/></c:if>
<c:if test="${not empty isPaymentTypeByJs && isPaymentTypeByJs}"><c:set var="is_pay_by_js" value="true"/></c:if>

<c:set var="paymentScreenId" value="${screenId}" />
<jsp:useBean id="serverNow" class="java.util.Date"/>

<%
request.setAttribute("paymentModesJSON", new flexjson.JSONSerializer().serialize(
	com.insta.hms.common.ConversionUtils.listBeanToListMap(
			new com.insta.hms.master.PaymentModes.PaymentModeMasterDAO().listAll(null, "status","A"))));

request.setAttribute("foreignCurrencyList", new com.insta.hms.master.ForeignCurrency.ForeignCurrencyDAO().listAll(null, "status","A"));
request.setAttribute("foreignCurrencyListJSON", new flexjson.JSONSerializer().serialize(
	com.insta.hms.common.ConversionUtils.copyListDynaBeansToMap(
			new com.insta.hms.master.ForeignCurrency.ForeignCurrencyDAO().listAll(null, "status","A"))));

request.setAttribute("genPrefs", com.insta.hms.master.GenericPreferences.GenericPreferencesDAO.getGenericPreferences());
request.setAttribute("sessionId", com.insta.hms.usermanager.UserDAO.getUserBean(RequestContext.getUserName()).get("login_handle"));

%>

<script type="text/javascript">

	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);

	String.prototype.startsWith = function (str){
    	return this.slice(0, str.length) == str;
	};

	var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
	var jForeignCurrencyList = <%= request.getAttribute("foreignCurrencyListJSON") %>;
	var jpatPackDetailsJson = <%= request.getAttribute("patPackDetailsJson") %>;
	var sessionId = '<%= request.getAttribute("sessionId") %>';

	// Currency row (1)
	var currencyRowCount = !empty(jForeignCurrencyList) ? 1 : 0;
	var packageRowCount = !empty(jpatPackDetailsJson) ? 1 : 0;
	var paymentRowsUncloned = 2; // Date row(1), Add btn Row(2)
	var paymentRowsCloned = 6; // Fields rows -- Payment row(1), Pay row (2), Card row(3), Bank row(4), Card No. row(5) , commision row()
	var extraRow = 1; // Extra break row

	if (!${dateCounterRequired}) {
		paymentRowsUncloned = 1; // Add btn row(1)
	}

	var applicableToIPRowCount = ${(!empty(isDepositApplicableToIP) && isDepositApplicableToIP == 'true') ? 1 : 0}

	var paymentRows = paymentRowsCloned + extraRow + currencyRowCount + packageRowCount + applicableToIPRowCount;
	var documentForm = document.${formName};

	var writeOffAmountRights = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_writeoff}';
	var allowReceiptBackDate = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_receipt_backdate}';
	var allowRefundRights = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_refund}';
	var screeId = '${paymentScreenId}';
	var isReturns = ${is_refund};
	var jgetAllCreditTypes = '${getAllCreditTypes}';
	
	YAHOO.util.Event.onContentReady("content", function() {enableBankDetails(document.getElementById("paymentModeId0"))});
		
</script>

<c:set var="paymentShow" value="false"/>

<c:choose>
	<c:when test="${fn:startsWith(paymentScreenId, 'pharma')}">
		<c:set var="paymentShow" value="${pharmacyCounterId != null && pharmacyCounterId != ''}"/>
	</c:when>
	<c:otherwise>
		<c:set var="paymentShow" value="${billingcounterId != null && billingcounterId != ''}"/>
	</c:otherwise>
</c:choose>

<c:if test="${!paymentShow}">
	<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
		<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
			<img src="${cpath}/images/alert.png"/>
		</div>
		<div style="float: left; margin-top: 10px">
			No Payment Counter assigned for User.
		</div>
	</div>
</c:if>

<table id="paymentsTable" class="formtable" style="display : ${paymentShow ? '' :'none'}">

	<!-- Row to display Date/Time and User Counter -->

	<c:if test="${dateCounterRequired}">
		<tr>
			<td class="formlabel">Date/Time:</td>
			<fmt:formatDate var="dateVal" value="${serverNow}" pattern="dd-MM-yyyy"/>
			<fmt:formatDate var="timeVal" value="${serverNow}" pattern="HH:mm"/>
			<c:choose>
				<c:when test="${actionRightsMap.allow_receipt_backdate == 'A' || roleId == 1 || roleId ==2}">
					<td>
						<insta:datewidget name="payDate" value="${dateVal}" valid="past" btnPos="left" />
						<input type="text" name="payTime" class="timefield" value="${timeVal}"/>
					</td>
				</c:when>
				<c:otherwise>
					<td class="forminfo">${dateVal} ${timeVal}</td>
				</c:otherwise>
			</c:choose>
			<td class="formlabel">Counter:</td>
			<td class="forminfo">
				<c:choose>
					<c:when test="${fn:startsWith(paymentScreenId, 'pharma')}">
						<b><c:out value="${pharmacyCounterName}"/></b>
						<input type="hidden" name="counterId" id="counterId" value="${pharmacyCounterId}" >
						<input type="hidden" name="counterName" id="counterName" value="${pharmacyCounterName}" >
					</c:when>
					<c:otherwise>
						<b><c:out value="${billingcounterName}"/></b>
						<input type="hidden" name="counterId" id="counterId" value="${billingcounterId}" >
						<input type="hidden" name="counterName" id="counterName" value="${billingcounterName}" >
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
	</c:if>
	<!-- Row to display Payment Type, Mode and Narration -->
	<!-- repeat the following rows i.e 6 + 1(currency row) for every new payment mode -->

	<tr>
		<td class="formlabel">Payment type:</td>
		<td>
			<input type="hidden" name="mvPackageId" id="mvPackageId" value="">
			<input type="hidden" name="primarySponsor" id="primarySponsor" value="${primarySponsor}">
			<input type="hidden" name="secondarySponsor" id="secondarySponsor" value="${secondarySponsor}">
			
			<select class="dropdown" name="paymentType" id="paymentType" onchange="setColor(this); disableTaxFeilds(); validateAmountOnPaymentMode();">
				<c:choose>
					<c:when test="${is_pay_by_js}">
						<!-- here options are populated by populatePaymentType() method -->
					</c:when>
					<c:otherwise>
						<c:choose>
							<c:when test="${is_refund}">
								<option value="refund" ${default_payment_type == 'F'?'selected':''}>Refund</option>
							</c:when>
							<c:otherwise>
								<c:if test="${is_bill_now && !is_insured}">
									<option value="receipt_settlement" ${default_payment_type == 'R'?'selected':''}>Settlement</option>
								</c:if>
								<c:if test="${!is_bill_now || is_insured}">
									<option value="receipt_advance" ${default_payment_type == 'A'?'selected':''}>Advance</option>
									<option value="receipt_settlement" ${default_payment_type == 'R'?'selected':''}>Settlement</option>
								</c:if>
								<option value="refund" ${default_payment_type == 'F'?'selected':''}>Refund</option>
								<c:if test="${is_pri_sponsor}">
									<option value="pri_sponsor_receipt_advance">Pri. Sponsor Advance</option>
									<option value="pri_sponsor_receipt_settlement" ${default_payment_type == 'SR'?'selected':''}>Pri. Sponsor Settlement</option>
								</c:if>
								<c:if test="${is_sec_sponsor}">
									<option value="sec_sponsor_receipt_advance">Sec. Sponsor Advance</option>
									<option value="sec_sponsor_receipt_settlement">Sec. Sponsor Settlement</option>
								</c:if>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>
			</select>
		</td>
		<td class="formlabel">Mode:</td>
		<td>
			<insta:selectdb name="paymentModeId" id="paymentModeId0" onchange="enableBankDetails(this);enableCommissionDetails(this);
				showLoyaltyPopup(this);showProcessPaymentBtn(this); showRewardPoints(this); hidePaymentModeType(this); depositSettlementRestriction(this);" table="payment_mode_master" 
				displaycol="payment_mode" valuecol="mode_id" value="-1" orderby="displayorder"/>
		</td>
	    <td class="formlabel redeemPointsTD" style="display:none;"><insta:ltext key="billing.patientbill.details.rewardpoints"/>:</td>
		<td class="forminfo redeemPointsTD" style="display:none;">
			<input type="text" name="rewardPointsRedeemed" id="rewardPointsRedeemed0" 
				value="0" onchange="return onChangeRedeemPoints(this);" />
			<b>(Max: ${availableRewardPoints})</b>
		</td>
		<td class="formlabel">EDC Machine:</td>
		<td class="forminfo">
			<insta:selectdb name="edcMachine" id="edcMachine0" dummyvalue="-- Select --" disabled="disabled" filtercol="center_id,status" filtervalue="${centerId},A"
				 table="edc_machine_master" displaycol="display_name" valuecol="imei" orderby="edc_id" onchange="setLocalStorage(this)"/>
				 <input type="hidden" name="edcMachine@type" value="integer">
		</td>
		<td class="formlabel"></td>
		<td class="forminfo">
			<button type="button" name="processPayment" onclick="showProcessPaymentDialog(this);" hidden id="processPayment0">Process Payment</button>
		</td>
		<input type="hidden" name="plutusTxnId" id="plutusTxnId0" value="-1">
	</tr>

	<!-- Currency row included if foreign currency master has any currency types -->

	<c:if test="${not empty foreignCurrencyList}">
		<tr id="payRefsCurrencyTr">
			<td class="formlabel">Foreign Currency:</td>
			<td class="forminfo">
				<insta:selectdb name="currencyId" id="currencyId" dummyvalue="-- Select --" onchange="getCurrencyDetails(this);"
					 table="foreign_currency" displaycol="currency" valuecol="currency_id"/>
			</td>
			<td class="formlabel">Foreign Currency Amt:</td>
			<td class="forminfo">
				<input type="text" name="currencyAmt" id="currencyAmt" onblur="convertCurrency(this)" onchange="convertCurrency(this)" />
			</td>
			<td class="formlabel">Exchange Rate:</td>
			<td class="forminfo">
				<label id="exchangeRateDateTimelbl"></label>
				<input type="hidden" name="exchangeRate" id="exchangeRate"/>
				<input type="hidden" name="exchangeDateTime" id="exchangeDateTime"/>
			</td>
		</tr>
	</c:if>
	
	
	<tr>
		<td class="formlabel">Pay:</td>
		<td class="forminfo">
			<input type="text" name="totPayingAmt" id="totPayingAmt0" onKeyUp="return validateAmountOnPaymentMode();" onchange="return enableCommissionDetails(this);">
			<b  class="redeemPointsTD" style="display:none;">(<insta:ltext key="billing.patientbill.details.eligible"/>: <label name="lblAvailableRewardPointsAmount" id="lblAvailableRewardPointsAmount"> ${availableRewardPointsAmount} </label>)</b>
		</td>
		
		<c:if test="${paymentScreenId == 'credit_bill_collection' && is_pri_sponsor}">
			<c:if test="${not empty genPrefs.showVAT && genPrefs.showVAT eq 'Y'}">
				<td class="formlabel">TDS:</td>
				<td class="forminfo">
					<input type="text" name="tdsAmt"/>
				</td>
			</c:if>
			<td class="formlabel">Paid By:</td>
			<td class="forminfo">
				<input type="text" name="paidBy" maxlength="50"/>
			</td>
		</c:if>
		<td class="formlabel">Narration:</td>
		<td class="forminfo">
			<input type="text" name="paymentRemarks" id="paymentRemarks" maxlength="100">
		</td>
		<td class="forminfo">
			<input type="hidden" name="paymentTransactionId" id="paymentTransactionId">
		</td>
	</tr>

	<tr id="payRefsTr">
		<td class="formlabel">Card Type:</td>
		<td class="forminfo">
			<insta:selectdb name="cardTypeId" id="cardTypeId0" dummyvalue="-- Select --" disabled="disabled"
				 table="card_type_master" displaycol="card_type" valuecol="card_type_id" onchange="enableCommissionDetails(this);"/>
		</td>
		<td class="formlabel">Bank:</td>
		<td class="forminfo">
			<insta:selectdb name="bankName" id="bankName" dummyvalue="-- Select --" disabled="disabled"
				 table="bank_master" displaycol="bank_name" valuecol="bank_name" orderby="bank_name"/>
		</td>
		<td class="formlabel">Ref Number:</td>
		<td class="forminfo">
			<input type="text" name="refNumber" id="refNumber" disabled="disabled"/>
		</td>
	</tr>
	<tr id="payRefsBankTr">
		<td class="formlabel">Bank Batch No:</td>
		<td class="forminfo"><input type="text" name="bankBatchNo" id="bankBatchNo" disabled="disabled"></td>
		<td class="formlabel">Card Auth Code:</td>
		<td class="forminfo"><input type="text" name="cardAuthCode" id="cardAuthCode" disabled="disabled"></td>
		<td class="formlabel">Card Holder Name:</td>
		<td class="forminfo"><input type="text" name="cardHolderName" id="cardHolderName" disabled="disabled"></td>
	</tr>
	<tr id="payRefsCardTr">
		<td class="formlabel">Card Number:</td>
		<td class="forminfo"><input type="text" name="cardNumber" id="cardNumber" disabled="disabled"  maxlength=${genPrefs.no_of_credit_debit_card_digits ne 0 ? genPrefs.no_of_credit_debit_card_digits : ""}/></></td>
		<td class="formlabel">Card Expiry Date:</td>
		<td class="forminfo">
			<input type="text" name="cardExpDate" id="cardExpDate" disabled="disabled">
		</td>
		
		<td class="formlabel">Mobile Number:</td>
		<td class="forminfo"><input type="text" name="mobNumber" id="mobNumber0" maxlength=16 onkeypress="return enterPhone(event)" disabled="disabled"></td>
		<td class="formlabel">OTP:</td>
		<td class="forminfo"><input type="text" name="totp" id="totp0" maxlength=10 disabled="disabled"></td>
	</tr> 
	<tr>
		<div id="commissionPer_div" style="display: none;" >
			<td class="formlabel"><label class="Per"></label></td>
			<td class="forminfo">			
				<label class="commissionPer" ></label>	
				<input type="hidden" name="commissionPer" id="commissionPer" value="">
			</td>			
		</div>		
		<div id="commissionAmt_div" style="display: none;" >
			<td  class="formlabel"><label class="Amt"></label></td>
			<td class="forminfo">				
				<label class="commissionAmt"></label>		
				<input type="hidden" name="commissionAmt" id="commissionAmt" value="">
			</td>
		</div>		
	</tr>
	
	<c:if test="${not empty patientMultiVisitPackageDetails}">
		<tr>
			<td class="formlabel">Applicable To Package:</td>
			<td class="forminfo"><input type="checkbox" name="multi_visit_package" id="multi_visit_package" onclick="disablePackage(this)"></td>
			<td class="formlabel">Apply To Package:</td>
			<td class="forminfo" colspan="3">
				<select name="patientPackageId" id="patientPackageId" class="dropdown" disabled="disabled" onchange="assignPackageIdValue(this)">
					<option value="">-- Select --</option>
					<c:forEach var="record" items="${patientMultiVisitPackageDetails}">
						<c:set var="isdiscontinued" value="${record.is_discontinued == '' ? false : true}"/>
					    <c:if test="${(record.status ==  'P' && isdiscontinued == 'false') || 
						    ((isdiscontinued == 'true' || record.status ==  'C')  && (record.total_deposits-record.total_set_offs)>0) }">
							<option value="${record.patient_package_id}" data-status="${record.status}" data-discontinued="${record.is_discontinued == '' ? false : true}" data-package-id="${record.package_id}">
						  	${record.package_name}, Total Deposit: ${record.total_deposits}, Deposit Balance: ${record.total_deposits-record.total_set_offs} ${record.status!='P' ? ', Closed': ''} ${record.is_discontinued!='' ? ', '.concat(record.is_discontinued):'.'}  						
                        	</option>
						</c:if>
					</c:forEach>
				</select>
			</td>
		</tr>
	</c:if>

	<c:if test="${not empty isDepositApplicableToIP && isDepositApplicableToIP eq true}">
		<tr>
			<td class="formlabel">Applicable To IP:</td>
			<input type="hidden" name="applicableToIp" id="applicableToIp" value="">
			<td class="forminfo"><input type="checkbox" name="applicable_to_ip" id="applicable_to_ip" value="i" onchange="onCheckApplicableToIP(this)"></td>
		</tr>
	</c:if>
	<tr style="white-space:nowrap;padding:0px 0px 0px 0px;">
		<td colspan="6" style="white-space:nowrap;padding:0px 0px 0px 0px;">
			<hr style="border-top:1px #e0e0e0 solid; border-bottom:none;border-left:none;padding:0px 0px 0px 0px; "/>
		</td>
	</tr>
	<tr style="white-space:nowrap;padding:0px 0px 0px 0px;">
		<td colspan="6" style="white-space:nowrap;padding:0px 0px 0px 0px;">
			<button type="button" onclick="return onAddPayMode(this);" id="addPaymentMode">Add Payment Mode</button>
			<button type="button" id="deletePayMode" disabled
				onclick="return onDeletePayMode(this);">Remove Payment Mode</button>
			<button type="button" onclick="showProcessPaymentDialog(this);" hidden id="processPayment">Process Payment</button>
		</td>
	</tr>
</table>
<script>YAHOO.util.Event.addListener(window, "load", function() {setColor(getIndexedFormElement(documentForm, 'paymentType', 0));}); </script>
