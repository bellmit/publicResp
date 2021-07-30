<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<html>
<head>
	<c:set var="issuetodeptonly"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("issue_to_dept_only") %>'
	scope="request" />
	<c:if test="${issuetodeptonly == 'N'}">
			<title><insta:ltext key="salesissues.userissuelist.list.title"/></title>
	</c:if>
	<c:if test="${issuetodeptonly == 'Y'}">
	<title><insta:ltext key="salesissues.userissuelist.list.viewdeptstockissueinsta"/></title>
	</c:if>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
		var hospuserlist = ${hospuserlist};
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/view_stk_iss.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="stores/storescommon.js" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="stkList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty stkList}"/>
<body onload="init(); showFilterActive(document.StkIssSearchForm)">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="issueno">
<insta:ltext key="salesissues.userissuelist.list.issueno"/>
</c:set>
<c:set var="issuedate">
<insta:ltext key="salesissues.userissuelist.list.issuedate"/>
</c:set>
<c:set var="all">
<insta:ltext key="salesissues.userissuelist.list.all.in.brackets"/>
</c:set>

<c:if test="${issuetodeptonly == 'N'}">
<h1><insta:ltext key="salesissues.userissuelist.list.userissuelist"/></h1>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
<h1><insta:ltext key="salesissues.userissuelist.list.deptissuelist"/></h1>
</c:if>

<insta:feedback-panel/>

<form name="StkIssSearchForm" method="GET">
	<input type="hidden" name="_method" value="getStkIss">
	<input type="hidden" name="_searchMethod" value="getStkIss"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="StkIssSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="salesissues.userissuelist.list.issueno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="user_issue_no" value="${ifn:cleanHtmlAttribute(param.user_issue_no)}" onkeypress="return enterNumOnlyzeroToNine(event);">
					<input type="hidden" name="user_issue_no@type" value="integer"/>
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="issue_date" id="issue_date0" value="${paramValues.issue_date[0]}"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="issue_date" id="issue_date1" value="${paramValues.issue_date[1]}"/>
							<input type="hidden" name="issue_date@op" value="ge,le"/>
							<input type="hidden" name="issue_date@cast" value="y"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.fromstore"/></div>
						<div class="sfField">
							<insta:selectdb name="from_store" table="stores" valuecol="dept_name" class="dropdown"
								displaycol="dept_name" value="${param.from_store}" dummyvalue="${all}" orderby="dept_name"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.user"/></div>
						<div  class="sfField">
							<div id="psAutocomplete" style="display: block; float: left; width: 240px">
								<input type="text" name="issued_to" id="issued_to" style="width: 200px" value="${paramValues.issued_to[0]}"/>
								<div id="hosp_user_dropdown" class="scrollingDropDown" style="width: 250px;"></div>
							</div>
							<span id="hosp_user_mand" style="display:block;padding-left:210px;" class="star">&nbsp;</span>
						</div>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.dept"/></div>
						<div class="sfField">
							<insta:selectdb id="issued_to" name="issued_to" table="department" displaycol="dept_name"
								valuecol="dept_name" value="${paramValues.issued_to[1]}" dummyvalue="${dummyvalue}"/>
						</div>
						
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.ward"/></div>
						<div class="sfField">
							<insta:selectdb id="issued_to" name="issued_to" table="ward_names" displaycol="ward_name"
								valuecol="ward_name" value="${paramValues.issued_to[2]}" dummyvalue="${dummyvalue}"/>
						</div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<insta:sortablecolumn name="user_issue_no" title="${issueno}"/>
				<insta:sortablecolumn name="issue_date" title="${issuedate}"/>
			    <th><insta:ltext key="salesissues.userissuelist.list.fromstore"/></th>
			    <th><insta:ltext key="salesissues.userissuelist.list.issuedto"/></th>
			    <th><insta:ltext key="salesissues.userissuelist.list.user"/></th>
			</tr>

			<c:forEach var="iss" items="${stkList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{issNo: '${iss.user_issue_no}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${iss.user_issue_no}</td>
					<td><fmt:formatDate value="${iss.issue_date}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
					<td>${iss.from_store}</td>
					<td>${iss.issued_to}</td>
					<td>${iss.username}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'stkList'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>


</form>
</body>
</html>