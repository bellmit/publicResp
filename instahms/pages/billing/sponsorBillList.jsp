<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Sponsor Bill List - Insta HMS</title>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="dashboardsearch.js"/>
<insta:link type="script" file="billing/sponsorbillList.js"/>

</head>

<c:set var="sponsorList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty sponsorList}"/>

<body onload="init();">

<h1>Sponsor Consolidated Claim Bills</h1>

<form name="sponsorBillForm" method="GET">
	<input type="hidden" name="_method" value="getSponsorBills">
	<input type="hidden" name="_searchMethod" value="getSponsorBills"/>
	<input type="hidden" name="sponsor_type" value="${ifn:cleanHtmlAttribute(param.sponsor_type)}">

	<insta:search form="sponsorBillForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"> TPA: </div>
			<div class="sboFieldInput">
				<insta:selectdb displaycol="tpa_name" name="sponsor_id" id="tpa" value="${paramValues.sponsor_id[0]}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="--Select--"
					onchange="changeSponsorType('S');"/>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel" style="white-space:nowrap">  Other Hospital: </div>
			<div class="sboFieldInput">
				<insta:selectdb displaycol="hospital_name" name="sponsor_id" id="hospital" value="${paramValues.sponsor_id[1]}"
					table="incoming_hospitals" valuecol="hospital_id" dummyvalue="--Select--"
					onchange="changeSponsorType('H');"/>
			</div>
		</div>
	</div>

	<div id="optionalFilter" style="clear: both; display : ${hasResult ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Sponsor Bill No:</div>
				<div class="sfField">
					<input type="text" name="sponsor_bill_no" value="${ifn:cleanHtmlAttribute(param.sponsor_bill_no)}">
				</div>
			</td>
			<td>
				<div class="sfLabel">Claim Date:</div>
				<div class="sfField">
						<div class="sfFieldSub">From:</div>
						<insta:datewidget name="claim_date" id="open_date0" value="${paramValues.claim_date[0]}"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="claim_date" id="open_date1" value="${paramValues.claim_date[1]}"/>
						<input type="hidden" name="claim_date@op" value="ge,le"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Status:</div>
				<div class="sfField">
					<insta:checkgroup name="status" selValues="${paramValues.status}"
						opvalues="O,S,R,C,X" optexts="Open,Sent,Received,Closed,Cancelled"/>
				</div>
			</td>
			</tr>
		</table>
	</div>
	</insta:search>
</form>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
		onmouseover="hideToolBar('');">
			<tr>
				<insta:sortablecolumn name="sponsor_name" title="TPA / Other Hospital"/>
				<insta:sortablecolumn name="sponsor_bill_no" title="Sponsor Claim No"/>
				<insta:sortablecolumn name="claim_date" title="Claim Date"/>
				<th style="text-align:right">Amount</th>
			</tr>
			<c:forEach var="record" items="${sponsorList}" varStatus="st">
				<c:set var="i" value="${st.index}"/>
					<c:set var="flagColor">
					<c:choose>
						<c:when test="${record.status eq 'O'}">empty</c:when>
						<c:when test="${record.status eq 'S'}">grey</c:when>
						<c:when test="${record.status eq 'R'}">yellow</c:when>
						<c:when test="${record.status eq 'C'}">green</c:when>
						<c:when test="${record.status eq 'X'}">red</c:when>
					</c:choose>
					</c:set>
					<c:set var="addEnable" value ="${record.status eq 'O'}"/>
					<c:set var="viewEnable" value="${record.status ne 'S' && record.status ne 'R'} "/>
					<c:set var="editEnable" value ="${record.status eq 'S' || record.status eq 'R'}"/>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{sponsor_bill_no:'${record.sponsor_bill_no}', status: '${record.status}'},
					[${addEnable},${viewEnable},${editEnable}])"
					onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">
						<td>${record.sponsor_name}</td>
						<td>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							${record.sponsor_bill_no}
						</td>
						<td><fmt:formatDate value="${record.claim_date}" pattern="dd-MM-yyyy"/></td>
						<td style="text-align:right">${record.claim_amt}</td>
					</tr>
			</c:forEach>
		</table>

		<insta:noresults hasResults="${hasResults}"/>
	</div>

	<div class="screenActions" style="float:left">
		<a href="./addSponsorBill.do?_method=add">Raise New Consolidated Claim</a>
	</div>

	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
		<div class="flagText">Sent</div>
		<div class="flag"><img src="${cpath}/images/yellow_flag.gif"></img></div>
		<div class="flagText">Received</div>
		<div class="flag"><img src="${cpath}/images/green_flag.gif"></img></div>
		<div class="flagText">Closed</div>
		<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
		<div class="flagText">Cancelled</div>
	</div>
</body>
</html>
