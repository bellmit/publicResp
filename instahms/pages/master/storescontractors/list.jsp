<%@page import="com.insta.hms.master.URLRoute"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="pagePath" value="<%=URLRoute.CONTRACTOR_MASTER_PATH %>"/>

<html>
<head>
<title>Contractor List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<script>
var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '${pagePath}/show.htm?',
		onclick: null,
		description: "View/Edit Contractor Details"
	},

};


function init() {
	createToolbar(toolbar);
}
</script>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="conList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty conList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<body onload="init();">
<h1>Contractor Master</h1>
<insta:feedback-panel/>

<form name="conListSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="conListSearchForm">
	  <table class="searchBasicOpts" >
	  <tr>
	  <td class="sboField">
		  	<div class="sboField">
				<div class="sboFieldLabel">Contractor</div>
					<div class="sboFieldInput">
							<insta:selectdb name="contractor_id" value="${param.contractor_id}" dummyvalue="...Select..."
									 table="contractor_master" displaycol="contractor_name" valuecol="contractor_id" />
							<input type="hidden" name="contractor_id@type" value="integer" />
					</div>
		    	</div>
		  	</div>
	  	</td>
	  	<td class="sboField" style="height: 70px;">
		  	<div class="sboField">
				<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
								opvalues="A,I" optexts="Active,Inactive"/>
					</div>
		    	</div>
		  	</div>
	  	</td>
	  	</tr>
	  </table>
	</insta:search-lessoptions>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <insta:sortablecolumn name="contractor_name" title="Contractor">Contractor</insta:sortablecolumn>
				<th>Status</th>
				<th>Grade</th>
			</tr>
			<c:forEach var="con" items="${conList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{contractor_id:'${con.contractor_id}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>
						<c:if test="${con.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${con.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:out value="${con.contractor_name}"/>
					</td>
					<td><c:out value="${con.status}"/></td>
					<td><c:out value="${con.grade}" /></td>
				</tr>
			</c:forEach>
		</table>
		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <table class="screenActions">
    	<tr>
			<td><c:url var="Url" value="${pagePath}/add.htm"/>
			<a href="${Url}">Add New Contractor</a>
		</tr>
	</table>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText">Active</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>
</form>
</body>
</html>
