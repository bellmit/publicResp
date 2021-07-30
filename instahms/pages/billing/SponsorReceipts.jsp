<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<head>

	<title>Sponsor Receipt List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/sponsorReceipts.js"/>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="paymentTypeDisplay" class="java.util.HashMap"/>
<c:set target="${paymentTypeDisplay}" property="R" value="Receipt"/>
<c:set target="${paymentTypeDisplay}" property="F" value="Refund"/>
<c:set target="${paymentTypeDisplay}" property="S" value="Sponsor"/>
<c:set target="${paymentTypeDisplay}" property="DR" value="Deposit"/>
<c:set target="${paymentTypeDisplay}" property="DF" value="Deposit Return"/>

<jsp:useBean id="modeDisplay" class="java.util.HashMap"/>
<c:set target="${modeDisplay}" property="C" value="Cash"/>
<c:set target="${modeDisplay}" property="R" value="Credit Card"/>
<c:set target="${modeDisplay}" property="B" value="Debit Card"/>
<c:set target="${modeDisplay}" property="Q" value="Cheque"/>
<c:set target="${modeDisplay}" property="D" value="Draft"/>

<jsp:useBean id="recptTypeDisplay" class="java.util.HashMap"/>
<c:set target="${recptTypeDisplay}" property="A" value="Advance"/>
<c:set target="${recptTypeDisplay}" property="S" value="Settlement"/>

<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
<c:set var="receiptList" value="${pagedList.dtoList}"/>

<body onload="init();">

<h1>Sponsor Receipts</h1>

<form action="SponsorReceiptsAction.do" name="sponsorReceipts">

	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="sponsorReceipts" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"> TPA: </div>
			<div class="sboFieldInput">
				<insta:selectdb displaycol="tpa_name" name="sponsor_id" id="tpa" value="${paramValues.sponsor_id[0]}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="--Select--"/>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel" style="white-space:nowrap">Other Hospital: </div>
			<div class="sboFieldInput">
				<insta:selectdb displaycol="hospital_name" name="sponsor_id" id="hospital" value="${paramValues.sponsor_id[1]}"
					table="incoming_hospitals" valuecol="hospital_id" dummyvalue="--Select--"/>
			</div>
		</div>
	</div>

	<div id="optionalFilter" style="clear: both; display : ${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Date:</div>
				<div class="sfField">
						<div class="sfFieldSub">From:</div>
						<insta:datewidget name="date" id="open_date0" value="${paramValues.date[0]}"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="date" id="open_date1" value="${paramValues.date[1]}"/>
						<input type="hidden" name="date@op" value="ge,le"/>
				</div>
			</td>
			<td >
				<div class="sfLabel">Type:</div>
				<div class="sfField">
					<insta:checkgroup name="recpt_type"
						opvalues="A,S" optexts="Advance,Settlement" selValues="${paramValues.recpt_type}"/>
				</div>
			</td>
			<td >
				<div class="sfLabel">Bill Status:</div>
				<div class="sfField">
					<insta:checkgroup name="status" selValues="${paramValues.status}"
					opvalues="O,S,R,C,X" optexts="Open,Sent,Received,Closed,Cancelled"/>
				</div>
			</td>
			<td >
				<div class="sfLabel">Sponsor Type:</div>
				<div class="sfField">
					<insta:checkgroup name="sponsor_type" selValues="${paramValues.sponsor_type}"
					opvalues="S,H" optexts="Sponsor,Other Hospital"/>
				</div>
			</td>
			<td class="last">
				<div class="sfLabel">Counter Type</div>
				<div class="sfField">
					<insta:checkgroup name="counter_type" selValues="${paramValues.counter_type}"
					opvalues="B,P" optexts="Billing counter,Pharmacy counter"/>
				</div>
			</td>
			</tr>
		</table>
	</div>
	</insta:search>
</form>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
	<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>

	<c:if test="${not empty templateList}">
		<c:forEach var="temp" items="${templateList}">
			<c:set var="templateValues" value="${templateValues},${temp.map.template_name}"/>
			<c:set var="templateTexts" value="${templateTexts},${temp.map.template_name}"/>
		</c:forEach>
	</c:if>
	<c:if test="${not empty receiptList}">
		<div align="right">
			<form name="printerSelectForm">
				<insta:selectoptions name="printTemplate" onchange="changePrinter()"
				opvalues="${templateValues}" optexts="${templateTexts}"
				value="${genPrefs.receiptRefundPrintDefault}" />

				<insta:selectdb name="printer" table="printer_definition" valuecol="printer_id"
				displaycol="printer_definition_name" onchange="changePrinter()" value="${pref.map.printer_id}" />
			</form>
		</div>
	</c:if>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
			onmouseover="hideToolBar('');">
			<tr>
				<insta:sortablecolumn name="sponsor_name" title="Sponsor Name"/>
				<insta:sortablecolumn name="receipt_no" title="Receipt No"/>
				<insta:sortablecolumn name="payment_type" title="Type"/>
				<insta:sortablecolumn name="display_date" title="Date"/>
				<insta:sortablecolumn name="amount" title="Amount"/>
				<insta:sortablecolumn name="payment_mode" title="Mode"/>
				<insta:sortablecolumn name="bill_no" title="Bill No"/>
				<th>User Name</th>
			</tr>

			<c:forEach items="${pagedList.dtoList}" var="record" varStatus="st">
				<c:set var="flagColor">
					<c:choose>
					<c:when test="${record.status eq 'O'}">empty</c:when>
					<c:when test="${record.status eq 'S'}">grey</c:when>
					<c:when test="${record.status eq 'R'}">yellow</c:when>
					<c:when test="${record.status eq 'C'}">green</c:when>
					<c:when test="${record.status eq 'X'}">red</c:when>
					</c:choose>
				</c:set>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{sponsor_bill_no:'${record.bill_no}',type:'${record.payment_type}',sponsorReceiptNo:'${record.sponsor_receipt_no}'},'')"
					onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">
					<td>${record.sponsor_name}</td>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						${record.sponsor_receipt_no}
					</td>
					<td>${recptTypeDisplay[record.recpt_type]}</td>
					<td>${record.date}</td>
					<td>${record.sponsor_amount}</td>
					<td>${modeDisplay[record.payment_mode]}</td>
					<td>${record.bill_no}</td>
					<td>${record.username}</td>
				</tr>
			</c:forEach>
		</table>
	</div>

	<c:if test="${param._method == 'list'}">
		<insta:noresults hasResults="${hasResults}"/>
	</c:if>

		<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Sent</div>
		<div class="flag"><img src="${cpath}/images/yellow_flag.gif"/></div>
		<div class="flagText">Received</div>
		<div class="flag"><img src="${cpath}/images/green_flag.gif"/></div>
		<div class="flagText">Closed</div>
		<div class="flag"><img src="${cpath}/images/red_flag.gif"/></div>
		<div class="flagText">Cancelled</div>
	</div>
</body>
</html>
