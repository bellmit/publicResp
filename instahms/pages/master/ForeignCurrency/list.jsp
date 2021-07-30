<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Foreign Currency</title>
	<insta:link type="js" file="master/ForeignCurrency/ForeignCurrency.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="currencyList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="createToolbar(toolBar);">

<form name="searchform">
	<h1>Foreign Currency</h1>
	<insta:feedback-panel />
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list">

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Currency</div>
				<div class="sboFieldInput">
					<insta:selectdb id="currency_id" name="currency_id" value="${param.currency_id}"
						table="foreign_currency" class="dropdown"   dummyvalue="-- Select --"
						valuecol="currency_id"  displaycol="currency"  filtered="false" />
					<input type="hidden" name="currency_id@type" value="integer">
					<input type="hidden" name="currency_id@cast" value="y">
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
								selValues="${paramValues.status}" />
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr>
			<th width="45px">Currency</th>
			<th width="25px" class="number">Currency Value</th>
			<th width="45px">&nbsp;</th>
			<th width="35px" align="right">Last Updated Date</th>
		</tr>

		<c:forEach var="currency" items="${currencyList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{currency_id: '${currency.currency_id}'}, '');" >
				<td width="45px">
					<c:if test="${currency.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${currency.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${currency.currency}
				</td>
				<td width="25px" class="number">${currency.conversion_rate}</td>
				<td width="45px">&nbsp;</td>
				<td width="35px"><fmt:formatDate value="${currency.mod_time}" pattern="dd-MM-yyyy HH:mm" /></td>
			</tr>
		</c:forEach>
	</table>
</div>

<insta:noresults hasResults="${hasResults}"/>

<c:url var="addUrl" value="ForeignCurrency.do">
	<c:param name="_method" value="add"></c:param>
</c:url>

<div class="screenActions" style="float:left">
	<a href="<c:out value='${addUrl}' />">Add New Currency</a>
</div>
<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText">Inactive</div>
</div>
<br/>
<br/>

<div id="CollapsiblePanel1" class="CollapsiblePanel">
	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
		<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Update Currency</div>
		<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
		<div class="clrboth"></div>
	</div>

	<table>
	<tr>
	<td>
	<table class="search">
		<tr>
			<th>Export/Import Currency Details</th>
		</tr>
		<tr>
			<td>
			<table>
				<tr>
					<td>Export: </td>
					<td>
						<form name="exportcurrencyform" action="ForeignCurrency.do" method="GET"
								style="padding:0; margin:0">
							<div style="float: left">
								<input type="hidden" name="_method" value="exportCurrencyDetailsToXls">
								<button type="submit" accesskey="E">
								<b><u>D</u></b>ownload</button>
							</div>
							<div style="float: left;white-space: normal">
								<img class="imgHelpText"
									 src="${cpath}/images/help.png"
									 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
								</div>
							</div>
						</form>
					</td>
				</tr>

				<tr>
					<td>Import:</td>
					<td>
						<form name="uploadcurrencyform" action="ForeignCurrency.do" method="POST"
								enctype="multipart/form-data" style="padding:0; margin:0">
							<input type="hidden" name="_method" value="importCurrencyDetailsFromXls">
							<input type="file" name="xlsCurrencyFile" accept="<insta:ltext key="upload.accept.master"/>"/>
							<button type="button" accesskey="F" onclick="return doUpload('uploadcurrencyform')" >
							<b><u>U</u></b>pload</button>
						</form>
					</td>
				</tr>
			</table>
			</td>
		</tr>
	</table>
	</td>
	</tr>
	</table>
</div>
<script>
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:true});
</script>
</body>
</html>
