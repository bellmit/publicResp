<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title><insta:ltext key="billing.searchdepositreceipts.search.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="billing.depositreceiptlist"/>
	<script>
	var toolbarOptions = getToolbarBundle("js.billing.depositreceiptlist.toolbar");
		var depositTempList = ${jsonDepositTempList};
		var roleId = '${roleId}';
		var receiptPrintRights = '${urlRightsMap.receipt_print}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/deposit_receipt_list.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<jsp:include page="/pages/Common/BillNoPrefix.jsp" />

	<style type="text/css">
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
		.status_DR { background-color: #EAD6BB }
		.status_DF { background-color: #DDDA8A}
		table.legend { border-collapse: collapse; margin-left: 6px; }
		table.legend td { border: 1px solid grey; padding: 2px 5px;}
	</style>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="receiptPrintRights" value="${urlRightsMap.receipt_print}"/>

<%-- some convenience variables initialized here for display purpose --%>
<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="P" value="Bill Now"/>
<c:set target="${typeDisplay}" property="C" value="Bill Later"/>
<c:set target="${typeDisplay}" property="M" value="Other"/>
<c:set target="${typeDisplay}" property="R" value="Pharmacy Return"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="S" value="Settled"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="modeDisplay" class="java.util.HashMap"/>
<c:set target="${modeDisplay}" property="C" value="Cash"/>
<c:set target="${modeDisplay}" property="R" value="Credit Card"/>
<c:set target="${modeDisplay}" property="B" value="Debit Card"/>
<c:set target="${modeDisplay}" property="Q" value="Cheque"/>
<c:set target="${modeDisplay}" property="D" value="Draft"/>

<jsp:useBean id="recptTypeDisplay" class="java.util.HashMap"/>
<c:set target="${recptTypeDisplay}" property="A" value="Advance"/>
<c:set target="${recptTypeDisplay}" property="S" value="Settlement"/>

<jsp:useBean id="paymentTypeDisplay" class="java.util.HashMap"/>
<c:set target="${paymentTypeDisplay}" property="DR" value="Deposit"/>
<c:set target="${paymentTypeDisplay}" property="DF" value="Deposit Return"/>

<c:set var="method_name" value= "getScreen"/>
<c:set var="depositReceiptList" value="${pagedList.dtoList}"/>
<c:set var="hasResult" value="${not empty depositReceiptList}"/>

<body onload="init()">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="receiptno">
<insta:ltext key="billing.searchdepositreceipts.search.receiptno"/>
</c:set>
<c:set var="type">
<insta:ltext key="billing.searchdepositreceipts.search.type"/>
</c:set>
<c:set var="date">
<insta:ltext key="billing.searchdepositreceipts.search.date"/>
</c:set>
<c:set var="amount">
<insta:ltext key="billing.searchdepositreceipts.search.amount"/>
</c:set>
<c:set var="mode">
<insta:ltext key="billing.searchdepositreceipts.search.mode"/>
</c:set>
<c:set var="pharmacyText">
<insta:ltext key="billing.searchdepositreceipts.search.pharmacy"/>
</c:set>
<c:set var="depositText">
<insta:ltext key="billing.searchdepositreceipts.search.deposit"/>
</c:set>
<c:set var="paymenttype">
<insta:ltext key="billing.searchdepositreceipts.search.deposits"/>,
<insta:ltext key="billing.searchdepositreceipts.search.depositreturns"/>
</c:set>
<c:set var="countertype">
<insta:ltext key="billing.searchdepositreceipts.search.billingcounter"/>,
<insta:ltext key="billing.searchdepositreceipts.search.pharmacycounter"/>
</c:set>
	<c:choose><c:when test="${not empty param.title}">
		<div class="pageHeader">${ifn:cleanHtml(param.title)}</div>
	</c:when><c:otherwise>
		<div class="pageHeader"><insta:ltext key="billing.searchdepositreceipts.search.search"/> ${category eq 'pharmacy' ? pharmacyText : depositText} <insta:ltext key="billing.searchdepositreceipts.search.receipts"/></div>
	</c:otherwise></c:choose>

<form name="DepositReceiptSearchForm" method="GET">
	<input type="hidden" name="_method" value="getDepositReceiptsListScreen">
	<input type="hidden" name="_searchMethod" value="getDepositReceiptsListScreen"/>
	<insta:search form="DepositReceiptSearchForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="ui.label.mrno"/>:</div>
			<div class="sboFieldInput">
				<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
				<div id="mrnoContainer"></div>
			</div>
		</div>
	</div>
<div id="optionalFilter" style="clear: both; display : ${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel"><insta:ltext key="billing.searchdepositreceipts.search.receiptdate"/>:</div>
				<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="billing.searchdepositreceipts.search.from"/>:</div>
						<insta:datewidget name="display_date" id="display_date0" value="${paramValues.display_date[0]}"/>
					</div>
					<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.searchdepositreceipts.search.to"/>:</div>
							<insta:datewidget name="display_date" id="display_date1" value="${paramValues.display_date[1]}"/>
							<input type="hidden" name="display_date@op" value="ge,le"/>
							<input type="hidden" name="display_date@cast" value="y"/>
					</div>
					<div class="sfLabel"><insta:ltext key="billing.searchdepositreceipts.search.counter"/>:</div>
					<div class="sfField">
						<insta:selectdb name="counter" table="counters"  dummyvalue="ALL COUNTERS"
						valuecol="counter_id" displaycol="counter_no" value="${param.counter}" />
					</div>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="billing.searchdepositreceipts.search.paymenttype"/></div>
					<div class="sfField">
						<insta:checkgroup name="payment_type" selValues="${paramValues.payment_type}"
							opvalues="DR,DF" optexts="${paymenttype}"/>
						</div>
				<c:if test="${multiCentered && centerId == 0}">
					<div class="sfLabel"><insta:ltext key="billing.searchdepositreceipts.search.center"/></div>
					<div class="sfField">
						<select class="dropdown" name="receipt_center_id" id="receipt_center_id">
							<option value="">ALL CENTERS</option>
							<c:forEach items="${centers}" var="center">
								<option value="${center.map.center_id}"
									${param.receipt_center_id == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
							</c:forEach>
						</select>
						<input type="hidden" name="receipt_center_id@cast" value="y"/>
					</div>
				</c:if>
				</td>
				<td>
					<div class="sfLabel"><insta:ltext key="billing.searchdepositreceipts.search.countertype"/></div>
					<div class="sfField">
						<insta:checkgroup name="counter_type" selValues="${paramValues.counter_type}"
						opvalues="B,P" optexts="${countertype}"/>
					</div>
				</td>
				<td class="last">&nbsp;</td>
				<td class="last">&nbsp;</td>
			</tr>
	</table>
</div>
</insta:search>
</form>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<c:choose>
		<c:when test="${empty (depositReceiptList) && (param.method!=method_name)}">
			<p><insta:ltext key="billing.searchdepositreceipts.search.noresultfound"/> </p>
		</c:when>
		<c:otherwise>

			<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
			<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>

		<%-- 	<c:if test="${not empty templateList}">
				<c:forEach var="temp" items="${templateList}">
					<c:set var="templateValues" value="${templateValues},${temp.map.template_name}"/>
					<c:set var="templateTexts" value="${templateTexts},${temp.map.template_name} (Receipt/Refund)"/>
				</c:forEach>
			</c:if>--%>
			<c:if test="${not empty depositTempList}">
				<c:forEach items="${depositTempList}" var="depTemp">
					<c:set var="templateValues" value="${templateValues},${depTemp.map.template_name}" />
					<c:set var="templateTexts" value="${templateTexts},${depTemp.map.template_name} (Deposit)" />
				</c:forEach>
			</c:if>
			<c:if test="${not empty depositReceiptList && (receiptPrintRights == 'A' || roleId == '1' || roleId == '2')}">
				<div align="right">
					<form name="printerSelectForm">
					<insta:selectoptions name="printTemplate" onchange="changePrinter()"
					opvalues="${templateValues}" optexts="${templateTexts}"
					value="${genPrefs.depositReceiptRefundPrintDefault}" />

					<insta:selectdb name="printer" table="printer_definition" valuecol="printer_id"
					displaycol="printer_definition_name" onchange="changePrinter()" value="${pref.map.printer_id}" />
				</form>
				</div>
			</c:if>

			<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
				onmouseover="hideToolBar('');">
				<tr>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<insta:sortablecolumn name="receipt_no" title="${receiptno}"/>
					<insta:sortablecolumn name="payment_type" title="${type}"/>
					<insta:sortablecolumn name="display_date" title="${date}"/>
					<insta:sortablecolumn name="amount" title="${amount}"/>
					<insta:sortablecolumn name="payment_mode" title="${mode}"/>
					<th><insta:ltext key="billing.searchdepositreceipts.search.username"/></th>
				</tr>

				<c:forEach var="r" items="${depositReceiptList}" varStatus="st">
					<c:set var="i" value="${st.index}"/>
					<c:set var="flagColor">
					<c:choose>
						<c:when test="${r.payment_type == 'DR'}">green</c:when>
						<c:when test="${r.payment_type == 'DF'}">yellow</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
					</c:set>

					<c:set var="editReceiptEnable" value ="${r.receipt_no !='' }"/>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{type: '${r.payment_type}',
						al_table: 'all_receipts_audit_view',deposit_no:'${r.receipt_no}', mrno:'${r.mr_no}'},
						[${editReceiptEnable},${true},${editReceiptEnable}])"
						onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">

						<td>${r.mr_no}</td>
						<td><insta:truncLabel value="${r.patient_full_name}" length="30"/></td>
						<td>${r.receipt_no}</td>
						<td><img src="${cpath}/images/${flagColor}_flag.gif"/>
							${paymentTypeDisplay[r.payment_type]}
						</td>
						<td><fmt:formatDate value="${r.display_date}" pattern="dd-MM-yyyy"/></td>
						<td style="text-align: right">${r.amount}</td>
						<td>${r.payment_mode}</td>
						<td>${r.username}</td>
					</tr>
				</c:forEach>
			</table>
		</c:otherwise>
		</c:choose>
	<div class="legend" style="display:${hasResult ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/green_flag.gif"/></div>
		<div class="flagText"><insta:ltext key="billing.searchdepositreceipts.search.depositreceipts"/></div>
		<div class="flag"><img src="${cpath}/images/yellow_flag.gif"/></div>
		<div class="flagText"><insta:ltext key="billing.searchdepositreceipts.search.depositrefunds"/></div>
	</div>
</body>
</html>

