<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title><insta:ltext key="billing.depositrealization.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="script" file="billing/deposit_realization_list.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_A.type_P { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }
	</style>
	<script>
		var userNameList = <%= request.getAttribute("userNameList") %>;
		var cpath= "${cpath}";
	</script>
	<insta:js-bundle prefix="billing.depositrealization"/>
</head>

<c:set var="depositRealizationList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty depositRealizationList}"/>

<body class="yui-skin-sam" onload="init()">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="depositno">
<insta:ltext key="billing.depositrealization.list.depositno"/>
</c:set>
<c:set var="depositdate">
<insta:ltext key="billing.depositrealization.list.depositdate"/>
</c:set>
<c:set var="bank">
<insta:ltext key="billing.depositrealization.list.bank"/>
</c:set>
<c:set var="referenceno">
<insta:ltext key="billing.depositrealization.list.referenceno"/>
</c:set>
<c:set var="username">
<insta:ltext key="billing.depositrealization.list.username"/>
</c:set>
<h1><insta:ltext key="billing.depositrealization.list.depositrealization"/></h1>
<form method="GET" name="depositRealizationForm">
<input type="hidden" name="_method" value="getDepositRealizationScreen">
<input type="hidden" name="_searchMethod" value="getDepositRealizationScreen">

<insta:search form="depositRealizationForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="billing.depositrealization.list.patientname.or.mrno"/>:</div>
			<div class="sboFieldInput">
				<div id="mrnoAutoComplete">
					<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
					<div id="mrnoContainer"></div>
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="billing.depositrealization.list.depositno"/>:</div>
			<div class="sboFieldInput">
				<input type="text" name="deposit_no" value="${ifn:cleanHtmlAttribute(param.deposit_no)}"/>
			</div>
		</div>
	</div>
	<div id="optionalFilter" style="clear:both; display: ${hasResult ? 'none': 'block'}">
		<table class="searchFormTable">
			<tr>
				<td>
					<div class="sfLabel"><insta:ltext key="billing.depositrealization.list.depositdate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.depositrealization.list.from"/> :</div>
							<insta:datewidget name="deposit_date" id="deposit_date0" value="${paramValues.deposit_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="billing.depositrealization.list.to"/>:</div>
							<insta:datewidget name="deposit_date" id="deposit_date1" value="${paramValues.deposit_date[1]}"/>
							<input type="hidden" name="deposit_date@op" value="ge,le" />
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="billing.depositrealization.list.bank"/>:</div>
						<div class="sfField">
							<input type="text" name="bank_name" value="${ifn:cleanHtmlAttribute(param.bank_name)}" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="billing.depositrealization.list.username"/>:</div>
						<div class="sfField">
							<div id="userName_wrapper" >
								<input type="text" name="username" id="username" value="${ifn:cleanHtmlAttribute(param.username)}"/>
								<div id="userName_dropdown"></div>
							</div>
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
		</table>
	</div>
</insta:search>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%">
				<tr>
					<th><insta:ltext key="billing.depositrealization.list.select"/> <input type="checkbox" name="realizeAll" onclick="return selectOrUnselectAll()"> <insta:ltext key="billing.depositrealization.list.all"/></th>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<insta:sortablecolumn name="deposit_no" title="${depositno}"/>
					<insta:sortablecolumn name="deposit_date" title="${depositdate}"/>
					<insta:sortablecolumn name="bank_name" title="${bank}"/>
					<insta:sortablecolumn name="reference_no" title="${referenceno}"/>
					<insta:sortablecolumn name="username" title="${username}"/>
					<th class="number"><insta:ltext key="billing.depositrealization.list.depositamt"/></th>
				</tr>

				<c:set var="depositURL" value="DepositRealization.do?_method=getDepositRealizationScreen"/>

				<c:forEach var="deposit" items="${depositRealizationList}" varStatus="st">
					<tr>
						<td align="center"><input type="checkbox" name="_realizeDeposit" value="${deposit.deposit_no}"></td>
						<td>${deposit.mr_no}</td>
						<td>${deposit.name}</td>
						<td>${deposit.deposit_no}</td>
						<td><fmt:formatDate value="${deposit.deposit_date}" pattern="dd-MM-yyyy"/></td>
						<td>${deposit.bank_name}</td>
						<td>${deposit.reference_no}</td>
						<td>${deposit.username}</td>
						<td class="number">${deposit.amount}</td>
					</tr>

			</c:forEach>
		</table>
			<insta:noresults hasResults="${hasResults}"/>

	</div>
	<div class="screenActions">
		<button type="button" name="realizeBtn" accesskey="R" class="button" onclick="return realize()">
		<label><u><b><insta:ltext key="billing.depositrealization.list.r"/></b></u><insta:ltext key="billing.depositrealization.list.ealize"/></label></button>&nbsp;
	</div>
	</form>
</body>
</html>
