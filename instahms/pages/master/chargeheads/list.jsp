<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Charge Heads-Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="pagePath" value="<%=URLRoute.CHARGE_HEAD_PATH %>"/>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Charge Head details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

</head>
<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Charge Heads</h1>

	<insta:feedback-panel/>

	<form name="SearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="SearchForm"  >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Charge Group:</div>
					<div class="sboFieldInput">
						<input type="text" name="chargegroup_name" value="${ifn:cleanHtmlAttribute(param.chargegroup_name)}">
						<input type="hidden" name="chargegroup_name@op" value="ico" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Charge Head:</div>
					<div class="sboFieldInput">
						<input type="text" name="chargehead_name" value="${ifn:cleanHtmlAttribute(param.chargehead_name)}">
						<input type="hidden" name="chargehead_name@op" value="ico" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Account Head:</div>
					<div class="sboFieldInput">
						<input type="text" name="account_head_name" value="${ifn:cleanHtmlAttribute(param.account_head_name)}">
						<input type="hidden" name="account_head_name@op" value="ico" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="chargegroup_name" title="Charge Group"/>
					<insta:sortablecolumn name="chargehead_id" title="ID"/>
					<insta:sortablecolumn name="chargehead_name" title="Name"/>
					<insta:sortablecolumn name="account_head_name" title="Account Head"/>
					<insta:sortablecolumn name="display_order" title="Display Order"/>
				</tr>
				<c:forEach var="chead" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{chargehead_id: '${chead.chargehead_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1}</td>
						<td>${chead.chargegroup_name}</td>
						<td>${chead.chargehead_id}</td>
						<td>${chead.chargehead_name}</td>
						<td>${chead.account_head_name}</td>
						<td>${chead.display_order}</td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</div>
		<c:url var="url" value="${pagePath}/add.htm"></c:url>
		
		<div class="screenActions" style="padding-bottom:10px"><a href="<c:out value='${url}'/>">Add New Chargehead</a></div>
	</form>

</body>
</html>