<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="depositType" value="${not empty param.depositType ? param.depositType : 'R'}"/>
<c:set var="URl" value="${(actionId eq 'patient_deposits_collection') ? 'DepositsCollection' : 'Deposits' }"/>
<c:set var="depositAvailableFor" value="${prefs.deposit_avalibility}"/>
<c:set var="chequeRealization" value="${prefs.depositChequeRealizationFlow}"/>
<c:set var="no_of_credit_debit_card_digits" value='<%=GenericPreferencesDAO.getAllPrefs().get("no_of_credit_debit_card_digits") %>'/>
<c:set var="tax_sub_grps_supported" value = '<%=GenericPreferencesDAO.getAllPrefs().get("tax_sub_groups_supported") %>'/>
<c:set var="tax_selection_mandatory"  value='${centerPrefs.map.tax_selection_mandatory}'/>
<c:set var="incomeTaxCashLimitApplicability" value='<%=GenericPreferencesDAO.getAllPrefs().get("income_tax_cash_limit_applicability") %>'/>
<c:set var="cashDepositTransactionLimitAmt" value="${cashDepositTransactionLimit}"/>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>' scope="request"/>
<c:set var="counterCenter" value="${counterCenter}"/>
<c:set var="preselectPatPackageId" value='${preselectPatPackageId}' />


<c:choose>
	<c:when test="${not empty param.mrNo}">
		  <c:set var="mrNo" value="${param.mrNo}" />
	</c:when>
	<c:otherwise>
		<c:if test="${not empty param.mrNoR}">
		  <c:set var="mrNo" value="${param.mrNoR}" />
		</c:if>
		<c:if test="${not empty param.mrNoF}">
		  <c:set var="mrNo" value="${param.mrNoF}" />
		</c:if>
	</c:otherwise>
</c:choose>
<html>
<head>
	<title><insta:ltext key="billing.collect.refunddeposits.details.depositscollectorrefund.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<script>
		var patPackDetailsJson = <%= request.getAttribute("patPackDetailsJson") %>;
		var packageDeposits = <%= request.getAttribute("packDepositJson") %>;
		var billNumber='${mrNo}'; //for paytm order-id/Bill-no using MR-no
		var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
		var loadToolBar = false;
		var ipDeposits = 0;
		var taxSubGroupsList = <%= request.getAttribute("taxSubGroupsJSON") %>;
		var taxMandatory = '${tax_selection_mandatory}';
		var income_tax_cash_limit_applicability = '${incomeTaxCashLimitApplicability}';
		var cashDepositTransactionLimitAmt =  ${empty cashDepositTransactionLimitAmt ? 0 : cashDepositTransactionLimitAmt};
		var max_centers_inc_default = ${max_centers_inc_default};
		var centerId = '<%=(Integer) session.getAttribute("centerId") %>';
		var counterCenter =  ${empty counterCenter ? 0 : counterCenter};
		var preselectPatPackageId = ${empty preselectPatPackageId ? false : preselectPatPackageId};
	</script>	
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="sockjs.min.js"/>
	<insta:link type="script" file="stomp.min.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="phoneNumberUtil.js"/>
	<insta:link type="script" file="billing/deposits.js"/>
	<insta:link type="js" file="select2.min.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="billPaymentCommon.js"/>
	
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="css" file="select2.min.css"/>
	<insta:link type="css" file="select2Override.css"/>

	<style type="text/css">
		td.forminfo { font-weight: bold; }
		form { padding: 0px; margin: 0px; }
		table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse;}
		table.detailFormTable td.label { padding: 0px 2px 0px 2px; overflow: hidden; }
		.stwMain { margin: 5px 7px }
		tr.deleted {background-color: #F2DCDC; color: gray; }
		tr.deleted input {background-color: #F2DCDC; color: gray;}
		tr.newRow {background-color: #E9F2C2; }
	</style>
	<insta:js-bundle prefix="billing.depositlist"/>
	<insta:js-bundle prefix="billing.deposits"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<insta:js-bundle prefix="billing.salucro"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body class="yui-skin-sam" onload="init();ajaxForPrintUrls();loadProcessPaymentDialog(); hidePaymentModeForDeposit();">

<jsp:include page="/pages/dialogBox/processPaymentDialog.jsp"></jsp:include>

<c:set var="depositavailable">
<insta:ltext key="billing.collect.refunddeposits.details.hospital"/>,
<insta:ltext key="billing.collect.refunddeposits.details.pharmacy"/>
</c:set>
<h1 style="float: left"><insta:ltext key="billing.collect.refunddeposits.details.collectorrefunddeposits"/></h1>

<c:if test="${counterCenter != centerId}">
	<% request.setAttribute("error", "User’s collection counter is not valid with Logged-in center"); %>
</c:if>

<c:if test="${max_centers_inc_default>1 && centerId == 0}">
	<% request.setAttribute("error", "Collect / Refund Deposits are not allowed for default center users."); %>
</c:if>


<insta:patientsearch searchType="mrNo" searchUrl="${URl}.do"  buttonLabel="Find"
	 searchMethod="collectOrRefundDeposits" fieldName="mrNo" depositType="${depositType}"/>

<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${mrNo}" />
<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="billing.collect.refunddeposits.details.otherdetails"/></legend>
	<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
    	<tr>
      		<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.address"/>:</td>
      		<td class="forminfo">${patient.patient_address}</td>
      		<td class="formlabel"></td>
      		<td></td>
      		<td class="formlabel"></td>
      		<td></td>
    	</tr>
   </table>
</fieldset>
<form name="mainform" method="POST" action="${cpath}/pages/BillDischarge/${URl}.do">
	<input type="hidden" name="_method" value="saveCollectOrRefundDeposits">
	<input type="hidden" name="deposit_type" value="${ifn:cleanHtmlAttribute(depositType)}" />
	<input type="hidden" name="mr_no"  value="${ifn:cleanHtmlAttribute(mrNo)}"/>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="billing.collect.refunddeposits.details.currentdepositdetails"/></legend>
		<table class="formtable"  border="0">
			<c:set var="packTotalDeposit" value="0"/>
			<c:set var="packTotalDepositSetOffs" value="0"/>
			<c:if test="${not empty packDeposits}">
				<c:forEach var="packDeposit" items="${packDeposits}">
					<c:set var="packTotalDeposit" value="${packTotalDeposit+packDeposit.total_deposits}"/>
					<c:set var="packTotalDepositSetOffs" value="${packTotalDepositSetOffs+packDeposit.total_set_offs}"/>
				</c:forEach>
			</c:if>
			<tr>
				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.totaldeposits"/>:</td>
				<td class="forminfo">${depositDetails.hosp_total_deposits-packTotalDeposit}</td>
				<c:if test="${chequeRealization == 'Y'}">
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.unrealizedcheq"/>:</td>
					<td class="forminfo">${depositDetails.hosp_unrealized_amount}</td>
				</c:if>
				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.depositsetoffs"/>:</td>
				<td class="forminfo">${depositDetails.hosp_total_setoffs-packTotalDepositSetOffs}</td>
				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.availabledeposits"/>:
					<input type="hidden" name="avlblDeposits" value="${depositDetails.hosp_total_balance}"/>
				</td>
				<td class="forminfo" id="availableDepositId">${depositDetails.hosp_total_balance-(packTotalDeposit-packTotalDepositSetOffs)}</td>
			</tr>
			<c:if test="${not empty ipDepositDetails}">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.iptotaldeposits"/>:</td>
					<td class="forminfo">${ipDepositDetails.total_ip_deposits}</td>
					<c:if test="${chequeRealization == 'Y'}">
						<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.ipunrealizedcheq"/>:</td>
						<td class="forminfo">${ipDepositDetails.ip_unrealized_amount}</td>
					</c:if>
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.ipdepositsetoffs"/>:</td>
					<td class="forminfo">${ipDepositDetails.total_ip_set_offs}</td>
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.ipavailabledeposits"/>:
						<input type="hidden" name="avlblIPDeposits" value="${ipDepositDetails.total_ip_deposits-ipDepositDetails.total_ip_set_offs}"/>
					</td>
					<td class="forminfo" id="availableIPDepositId">${ipDepositDetails.total_ip_deposits-ipDepositDetails.total_ip_set_offs-ipDepositDetails.total_ip_set_offs_non_ip_bill}</td>
				</tr>
			</c:if>
			<c:if test="${not empty packDeposits}">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.totalpackagedeposits"/>:</td>
					<td class="forminfo">${packTotalDeposit}</td>
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.packagedepositsetoffs"/>:</td>
					<td class="forminfo">${packTotalDepositSetOffs}</td>
					<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.availablepackagedeposits"/>:</td>
					<td class="forminfo" id="availablePackageDepositId">${packTotalDeposit-packTotalDepositSetOffs}</td>
				</tr>
			</c:if>
		</table>
	</fieldset>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="billing.collect.refunddeposits.details.depositspayerdetails"/></legend>
		<table border="0" class="formtable" >
			<tr>
				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.depositpayername"/>:</td>
				<td><input type="text" name="deposit_payer" maxlength="100"></td>
				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.phonenumber"/>:</td>
				

				<td style="width:250px;">
					<div>
						<input type="hidden" id="payer_phone" name="payer_phone"/>
						<input type="hidden" id="phone_valid" value="N"/>
						<select id="payer_phone_country_code" class="dropdown" style="width:76px;" name="payer_phone_country_code">
							<c:if test="${empty defaultCountryCode}">				
									<option value='+' selected> - Select - </option>
							</c:if>
							<c:forEach items="${countryList}" var="list">
								<c:choose>
									<c:when test="${ list[0] == defaultCountryCode}">				
										<option value='+${list[0]}' selected> +${list[0]}(${ list[1]})  </option>	
									</c:when>	
									<c:otherwise>
										<option value='+${list[0]}'> +${list[0]}(${list[1]})  </option>	
									</c:otherwise>
								</c:choose>
														
							</c:forEach>
						</select>
						<input type="text" class="field phoneField" id="payer_phone_national" onkeypress="return enterNumOnlyzeroToNine(event)" maxlength ="15" 
								 />
						<img class="imgHelpText" id="payer_phone_help" src="${cpath}/images/help.png"/>
					</div>
					<div>
						<span style="visibility:hidden;color:#f00" id="phone_error"></span>	
					</div>
				</td>

				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.payeraddress"/>:</td>
				<td><input type="text" name="payer_address" size="50" maxlength="300"></td>
			</tr>
		</table>
	</fieldset>
	
	<c:if test="${tax_sub_grps_supported gt 0}">
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="billing.collect.refunddeposits.details.taxdetails"/></legend>
		<table border="0" class="formtable" >
		<tr>			
			<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.taxsubgrp"/>:</td>			
			<td>
				<select id ="tax_subgrp_primary" name ="tax_subgrp_primary" class="dropdown" onChange ="setTaxDetailsDeposits();">
					<option value ="">--Select--</option>
					<c:forEach items="${taxSubGroups}" var="taxSubGroupslist">
						<option value='${taxSubGroupslist.map.item_subgroup_id}'}>${taxSubGroupslist.map.item_subgroup_name}</option>
					</c:forEach>
				</select>
				<c:if test="${tax_selection_mandatory eq 'Y'}">
					<span class="star">*</span>
				</c:if>
			</td>
			<c:if test="${tax_sub_grps_supported gt 1}">
				<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.taxsubgrp"/>:</td>	
				<td>
				<select id ="tax_subgrp_secondary" name ="tax_subgrp_secondary" class="dropdown" onChange ="setTaxDetailsDeposits();">
					<option value ="">--Select--</option>
					<c:forEach items="${taxSubGroups}" var="taxSubGroupslist">
						<option value='${taxSubGroupslist.map.item_subgroup_id}'}>${taxSubGroupslist.map.item_subgroup_name}</option>
					</c:forEach>
				</select>
				<c:if test="${tax_selection_mandatory eq 'Y'}">
					<span class="star">*</span>
				</c:if>
				</td>
			</c:if>			
			<td class="formlabel">
				<insta:ltext key ="billing.collect.refunddeposits.details.netamount"/>:
			</td>
			<td class="forminfo">
				<label id="deposit_amount"> </label>
			</td>	
			<td class="formlabel">
				<insta:ltext key ="billing.collect.refunddeposits.details.totaltaxamt"/>:
			</td>
			<td class="forminfo">
				<label id="total_tax_amount"> </label>
			</td>	
		</tr>	
		</table>
	</fieldset>
	</c:if>
	<c:set var="showPayments" value="true" />

	<dl class="accordion">
		<dt>
		<span><insta:ltext key="billing.collect.refunddeposits.details.payments"/></span>
		<div class="clrboth"></div>
		</dt>
		<dd class="open" id="payDD">
		<div class="bd">
			<div class='stwContent <c:if test="${not showPayments}">stwHidden</c:if>' id="payment_content">
				<table border="0" class="formtable" >
					<c:if test="${depositAvailableFor != 'B'}">	<%-- separate deposits for Ph and Hosp --%>
						<tr>
							<td class="formlabel"><insta:ltext key="billing.collect.refunddeposits.details.depositfor"/>:</td>
						    <td>
								<insta:selectoptions  name="depositAvailable" value="${depositAvailableFor}"
									opvalues="H,P" optexts="${depositavailable}"/>
						    </td>
						    <td></td>
						    <td></td>
						    <td></td>
						    <td></td>
						</tr>
					</c:if>
				</table>

				<c:set var="isrefund" value="${depositType == 'F'}"/>
				<insta:billPaymentDetails formName="mainform" isBillNowPayment="true" isRefundPayment="${isrefund}"/>

			</div>
		</div>
		</dd>
	</dl>

	<c:url var="depositDashboardURL" value="Deposits.do">
		<c:param value="getDeposits" name="_method"/>
		<c:param value="${mrNo}" name="mr_no"/>
	</c:url>

	<table cellpadding="0" cellspacing="0"  border="0" width="100%">
		<tr>
			<td>
				<button type="button" id="saveButton" accesskey="S" onclick="return doSave();"><b><u><insta:ltext key="billing.collect.refunddeposits.details.s"/></u></b><insta:ltext key="billing.collect.refunddeposits.details.ave"/></button>
				| <a href='<c:out value="${depositDashboardURL}"/>'><insta:ltext key="billing.collect.refunddeposits.details.depositslist"/></a>
			</td>

			<td align="right">
				<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
				<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>
				<c:if test="${not empty depositTempList}">
					<c:forEach items="${depositTempList}" var="depTemp">
						<c:set var="templateValues" value="${templateValues},${depTemp.map.template_name}" />
						<c:set var="templateTexts" value="${templateTexts},${depTemp.map.template_name} (Deposit)" />
					</c:forEach>
				</c:if>
					<insta:selectoptions name="printTemplate"
						opvalues="${templateValues}" optexts="${templateTexts}"
						value="${prefs.depositReceiptRefundPrintDefault}" />

					<insta:selectdb name="printer" table="printer_definition" valuecol="printer_id"
						displaycol="printer_definition_name" value="${pref.map.printer_id}" />
			</td>
		</tr>
	</table>

</form>
</body>
</html>

