<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<html>

<head>
	<c:set var="issuetodeptonly"
	value='<%= GenericPreferencesDAO.getAllPrefs().get("issue_to_dept_only") %>'
	scope="request" />
	
	<c:if test="${issuetodeptonly == 'N'}">
	<title><insta:ltext key="salesissues.stockuserreturnlist.list.title.all"/></title>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
	<title><insta:ltext key="salesissues.stockuserreturnlist.list.title.deptward"/></title>
</c:if>

	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
		var hospuserlist = ${hospuserlist};
	</script>
	<script>
    var toolbar = {}
		toolbar.Report= {
			title: toolbarOptions["returnprint"]["name"],
			imageSrc: "icons/Report.png",
			href: 'pages/stores/viewstockissues.do?_method=getPrint&report=StoreStockUserReturns',
			target: '_blank',
			onclick: null,
			description: toolbarOptions["returnprint"]["description"]
	};

	var theForm = document.StkretSearchForm;

	function init() {
		theForm = document.StkretSearchForm;
		theForm.user_return_no.focus();
		fillUsers('returned_by');
		createToolbar(toolbar);
	}

</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="stores/storescommon.js" />
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="retList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty retList}"/>
<body onload="init(); showFilterActive(document.StkretSearchForm)">

<c:if test="${issuetodeptonly == 'N'}">
 <h1><insta:ltext key="salesissues.stockuserreturnlist.list.stockuserreturnlist"/></h1>
</c:if>
<c:if test="${issuetodeptonly == 'Y'}">
 <h1><insta:ltext key="salesissues.stockuserreturnlist.list.stockdeptreturnlist"/></h1>
</c:if>

<c:set var="all">
<insta:ltext key="salesissues.stockuserreturnlist.list.all.in.brackets"/>
</c:set>
<c:set var="returnno">
<insta:ltext key="salesissues.stockuserreturnlist.list.returnno"/>
</c:set>
<c:set var="returndate">
<insta:ltext key="salesissues.stockuserreturnlist.list.returndate"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<form name="StkretSearchForm" action="viewuserreturns.do" >
	<input type="hidden" name="_method" value="getStkRet">
	<input type="hidden" name="_searchMethod" value="getStkRet"/>
	<input type="hidden" name="sortOrder" id="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" id="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="StkretSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="salesissues.stockuserreturnlist.list.returnno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="user_return_no" value="${ifn:cleanHtmlAttribute(param.user_return_no)}" onkeypress="return enterNumOnlyzeroToNine(event);">
					<input type="hidden" name="user_return_no@type" value="integer" />
				</div>
	    </div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.stockuserreturnlist.list.fromdate"/></div>
						<div class="sfField">
							<insta:datewidget name="return_date" id="return_date0" value="${paramValues.return_date[0]}"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.stockuserreturnlist.list.todate"/></div>
						<div class="sfField">
							<insta:datewidget name="return_date" id="return_date1" value="${paramValues.return_date[1]}"/>
							<input type="hidden" name="return_date@op" value="ge,le"/>
							<input type="hidden" name="return_date@cast" value="y"/>
					    </div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.stockuserreturnlist.list.tostore"/></div>
						<div class="sfField">
							<insta:selectdb name="to_store" table="stores" valuecol="dept_name" class="dropdown"
								displaycol="dept_name" value="${param.to_store}" dummyvalue="${all}" orderby="dept_name"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.user"/></div>
						
						<div class="sfField">
						<div id="psAutocomplete" style="display: block; float: left; width: 240px">
								<input type="text" name="returned_by" id="returned_by" style="width: 200px" value="${paramValues.returned_by[0]}"/>
								<div id="hosp_user_dropdown" class="scrollingDropDown" style="width: 250px;"></div>
							</div>
							<span id="hosp_user_mand" style="display:block;padding-left:210px;" class="star">&nbsp;</span>
						</div>
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.dept"/></div>
						<div class="sfField">
							<insta:selectdb id="returned_by" name="returned_by" table="department" displaycol="dept_name"
								valuecol="dept_name" value="${paramValues.returned_by[1]}" dummyvalue="${dummyvalue}"/>
						</div>
						
						<div class="sfLabel"><insta:ltext key="salesissues.userissuelist.list.ward"/></div>
						<div class="sfField">
							<insta:selectdb id="returned_by" name="returned_by" table="ward_names" displaycol="ward_name"
								valuecol="ward_name" value="${paramValues.returned_by[2]}" dummyvalue="${dummyvalue}"/>
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
				<insta:sortablecolumn name="user_return_no" title="${returnno}"/>
				<insta:sortablecolumn name="return_date" title="${returndate}"/>
			    <th><insta:ltext key="salesissues.stockuserreturnlist.list.tostore"/></th>
			    <th><insta:ltext key="salesissues.stockuserreturnlist.list.returnedby"/></th>
			    <th><insta:ltext key="salesissues.stockuserreturnlist.list.user"/></th>
			</tr>

			<c:forEach var="ret" items="${retList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{returnNo: '${ret.user_return_no}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>${ret.user_return_no}</td>
					<td><fmt:formatDate value="${ret.return_date}" pattern="dd-MM-yyyy HH:mm:ss"/></td>
					<td>${ret.to_store}</td>
					<td>${ret.returned_by }</td>
					<td>${ret.username}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getStkRet'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

</form>
</body>
</html>
