<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default() %>' scope="request"/>

<head>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="billing.accountingexport.items.exportaccountingdata.instahms"/></title>
	<script>
		function onSubmit(option) {
			document.inputform.format.value = option;
			if (!validateFromToDate(document.inputform.fromDate, document.inputform.toDate))
				return false
			if (option == 'details') {
				var diff = getDate('toDate') - getDate('fromDate');
				var milliSecForMonth = 1000 * 60 * 60 * 24 * 31;
				if (diff > milliSecForMonth) {
					showMessage('js.billing.accountsexport.selectdaterange');
					document.getElementById('toDate').focus();
					return false;
				}
			}
			if (!validateTime(document.getElementById('fromTime'))) {
					return false;
			}
			if (!validateTime(document.getElementById('toTime'))) {
					return false;
			}
			var enable = document.inputform.useVoucherDate.checked;
			if (enable)
				validateRequired(document.inputform.voucherDate, getString("js.billing.accountsexport.voucherdate.required"));
			if (document.getElementById('exportFor').value == '') {
				showMessage("js.billing.accountsexport.selectexportfor");
				document.getElementById('exportFor').focus();
				return false;
			}
			var exportItems = document.inputform.exportItems;
			var flag = false;
			for (var i=0; i<exportItems.length; i++) {
				var item = exportItems[i];
				if (item.checked)
					flag = true;
			}
			if (flag) {
				document.inputform.submit();
			} else {
				showMessage("js.billing.accountsexport.selectoneexportitem");
				return false;
			}
		}

		function onInit() {
			setSelDateRange();
			setDateRangeMonth(document.inputform.voucherDate);
			enableVoucherDate();
			if (${empty paramValues.exportItems or paramValues.exportItems[0] == ''}) {
				enableCheckGroupAll(document.inputform.exportItems[0]);
			}
			// if there only one option in the exportFor dropdown, then set that as the default.
			if (document.getElementById('exportFor').options.length == 2) {
				document.getElementById('exportFor').options.selectedIndex = 1;
			}
		}

		function enableVoucherDate() {
			var enable = document.inputform.useVoucherDate.checked;
			document.inputform.voucherDate.disabled = !enable;
		}

		function setSelDateRange() {
			var formEl = document.inputform._sel;
			var len = formEl.length;
			for (var i=0; i<len; i++) {		//>
				if (formEl[i].checked) {
					formEl[i].onclick();
				}
			}
		}

	</script>
	<insta:js-bundle prefix="billing.accountsexport"/>
</head>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
	<body onload="onInit()">
	<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
		<div class="pageHeader"><insta:ltext key="billing.accountingexport.items.accountingexport"/></div>
		<form name="inputform" method="GET"
			action="${cpath}/pages/BillDischarge/AccountingExportAction.do">
			<input type="hidden" name="_method" value="getVouchers">
			<input type="hidden" name="format" value="summary">

			<div class="infoPanel">
				<div class="img"><img src="${cpath}/images/information.png"/></div>
				<div class="txt"><insta:ltext key="billing.accountingexport.items.longtemplate"/><br/> <insta:ltext key="billing.accountingexport.items.longtemplate1"/>
				</div>
				<div style="clear: both"></div>
			</div>

			<table>
				<tr>
					<td colspan="2"><insta:ltext key="billing.accountingexport.items.selectadaterange"/></td>
				</tr>
				<tr>
					<td valign="top">
						<input checked type="radio" id="pd" name="_sel" onclick="setDateRangeYesterday(fromDate, toDate)">
						<label for="pd"><insta:ltext key="billing.accountingexport.items.yesterday"/></label>
						<br/>

						<input type="radio" id="td" name="_sel" onclick="setDateRangeToday(fromDate, toDate)">
						<label for="td"><insta:ltext key="billing.accountingexport.items.today"/></label>
						<br/>

						<input type="radio" id="pm" name="_sel" onclick="setDateRangePreviousMonth(fromDate, toDate)">
						<label for="pm"><insta:ltext key="billing.accountingexport.items.previousmonth"/></label>
						<br/>

						<input type="radio" id="tm" name="_sel" onclick="setDateRangeMonth(fromDate, toDate)">
						<label for="tm"><insta:ltext key="billing.accountingexport.items.thismonth"/></label>
						<br/>

						<input type="radio" id="pfy"
									name="_sel" onclick="setDateRangePreviousFinancialYear(fromDate, toDate)">
						<label for="pfy"><insta:ltext key="billing.accountingexport.items.previousfinancialyear"/></label>
						<br/>

						<input type="radio" id="tfy" name="_sel" onclick="setDateRangeFinancialYear(fromDate, toDate)">
						<label for="tfy"><insta:ltext key="billing.accountingexport.items.thisfinancialyear"/></label>
						<br/>
					</td>

					<td valign="top" style="padding-left: 2em; vertical-align: top">
						<table style="white-space:nowrap">
							<tr>
								<td align="right"><insta:ltext key="billing.accountingexport.items.from"/>:</td>
								<td><insta:datewidget name="fromDate"/><input type="text" name="fromTime" id="fromTime" class="timefield"/></td>
							</tr>
							<tr>
								<td align="right"><insta:ltext key="billing.accountingexport.items.to"/>:</td>
								<td><insta:datewidget name="toDate"/><input type="text" name="toTime" id="toTime" class="timefield"></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>

			<fieldset class="fieldSetBorder" style="margin-top: 10px">
				<legend class="fieldSetLabel"><insta:ltext key="billing.accountingexport.items.exportitems"/></legend>
				<div style="float: left">
					<div style="clear:both"></div>
					<div style="width: 100px; float: left; padding: 5px 0px 0px 5px; text-align: right"><insta:ltext key="billing.accountingexport.items.exportfor"/> :</div>
					<div style="width: 200px; float: left; padding: 5px 0px 0px 5px">
						<select name="exportFor" id="exportFor" class="dropdown">
							<option value="">${select}</option>
							<c:if test="${acc_prefs.map.all_centers_same_comp_name != 'Y'}">
								<c:forEach items="${centers}" var="center">
									<option value="C${center.map.center_id}">
										<c:set var="CCompanyName" value=""/>
										<c:if test="${not empty center.map.accounting_company_name}">
											<c:set var="CCompanyName" value="-(${center.map.accounting_company_name})"/>
										</c:if>
										${center.map.center_name}${CCompanyName}
									</option>
								</c:forEach>
							</c:if>
							<c:forEach items="${accGroups}" var="accGroup">
								<c:choose>
									<c:when test="${acc_prefs.map.all_centers_same_comp_name != 'Y' && accGroup.map.account_group_id == 1}">
										<!-- do not display the hospital account group -->
									</c:when>
									<c:otherwise>
										<option value="A${accGroup.map.account_group_id}">
											<c:set var="ACompanyName" value=""/>
											<c:if test="${not empty accGroup.map.accounting_company_name}">
												<c:set var="ACompanyName" value="-(${accGroup.map.accounting_company_name})"/>
											</c:if>
											${accGroup.map.account_group_name}${ACompanyName}
										</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</div>
					<div style="clear:both"></div>
					<div style="width: 100px; float: left; padding: 12px 0px 0px 5px; text-align: right"><insta:ltext key="billing.accountingexport.items.voucherfromdate"/>:</div>
					<div style="width: 200px; float: left; padding: 10px 0px 0px 5px">
						<insta:datewidget id="voucherFromDate" name="voucherFromDate" />
					</div>
					<div style="clear:both"></div>
					<div style="width: 100px; float: left; padding: 12px 0px 0px 5px; text-align: right"><insta:ltext key="billing.accountingexport.items.vouchertodate"/>:</div>
					<div style="width: 200px; float: left; padding: 10px 0px 0px 5px">
						<insta:datewidget id="voucherToDate" name="voucherToDate" />
					</div>
					<div style="clear:both"></div>
					<div style="width: 100px; float: left; padding: 12px 0px 0px 5px; text-align: right"><insta:ltext key="billing.accountingexport.items.setvoucherdates"/>:</div>
					<div style="width: 200px; float: left; padding: 10px 0px 0px 5px">
						<input type="checkbox" name="useVoucherDate" value="Y" onclick="enableVoucherDate()">
						<insta:datewidget name="voucherDate" value="01-04-2009"/>
						<img class="imgHelpText" onmouseout="document.getElementById('NoteDiv').style.display='none'" onmouseover="document.getElementById('NoteDiv').style.display='inline'" src="${cpath}/images/help.png"/>
							<div class="helpText" id="NoteDiv" style="display:none"><insta:ltext key="billing.accountingexport.items.onlyeducationalmode"/><br>
						<insta:ltext key="billing.accountingexport.items.validvoucherdateofmonth"/>.</div>
					</div>
				</div>

				<div style="float: left">
					<input type="checkbox" name="exportItems" id="exportItems"
						${empty paramValues.exportItems or paramValues.exportItems[0] == '' ?'checked':''}
						value="" onclick="enableCheckGroupAll(this)"><insta:ltext key="billing.accountingexport.items.all"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Bills"><insta:ltext key="billing.accountingexport.items.bills"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Receipts"><insta:ltext key="billing.accountingexport.items.receipts"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Deposits"><insta:ltext key="billing.accountingexport.items.deposits"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Payment Vouchers"><insta:ltext key="billing.accountingexport.items.paymentvouchers"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Payments Due"><insta:ltext key="billing.accountingexport.items.paymentsdue"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Consolidated Sponsor Receipts"><insta:ltext key="billing.accountingexport.items.consolidatedsponsorreceipts"/><br/>
				</div>
				<div class="float: left">
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Invoices"><insta:ltext key="billing.accountingexport.items.storesinvoices"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Returns with Debit"><insta:ltext key="billing.accountingexport.items.storesreturnswithdebit"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Consignment Stock Issued"><insta:ltext key="billing.accountingexport.items.storesconsignmentstockissued"/><br/>
					<input type="checkbox" name="exportItems" id="exportItems" value="Stores Consignment Stock Returns"><insta:ltext key="billing.accountingexport.items.storesconsignmentstockreturns"/>
				</div>
			</fieldset>

			<table class="screenActions">
				<tr>
						<td>
							<button type="button" accesskey="X" name="XML Report" class="button" onclick="return onSubmit('tallyxml')">
							<label><b><u><insta:ltext key="billing.accountingexport.items.x"/></u></b><insta:ltext key="billing.accountingexport.items.mlexport"/></label></button>&nbsp;
						</td>
						<td>
							<button type="button" name="viewDetails" class="button" accesskey="D" onclick="return onSubmit('details')">
							<label><insta:ltext key="billing.accountingexport.items.view"/> <b><u><insta:ltext key="billing.accountingexport.items.d"/></u></b><insta:ltext key="billing.accountingexport.items.etails"/></label></button>&nbsp;
						</td>
						<td>
							<button  type="button" name="viewSummary" class="button" accesskey="S" onclick="return onSubmit('summary')">
							<label><insta:ltext key="billing.accountingexport.items.view"/> <b><u><insta:ltext key="billing.accountingexport.items.s"/></u></b><insta:ltext key="billing.accountingexport.items.ummary"/></label></button>&nbsp;
						</td>
				  </tr>
			</table>

		</form>
	</body>
</html>

