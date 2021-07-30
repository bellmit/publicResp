<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>SavedSearchesList - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardLookup.js"/>
	
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/SavedSearches/show.htm?',
				onclick: null,
				description: "View and/or Edit Search details"
			}
		};
		function init(){
			createToolbar(toolbar);
		}
	</script>

</head>

<body onload="init()">
    <h1>Saved Searches List</h1>
	<insta:feedback-panel/>

	<form name="SavedSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="SavedSearchForm" >
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">Search Name:</div>
						<div class="sboFieldInput">
							<input type="text" name="search_name" value="${ifn:cleanHtmlAttribute(param.search_name)}">
						</div>
					</td>

					<td class="sboField" >
						<div class="sboFieldLabel">User Name:</div>
						<div class="sboFieldInput" >
							<input type="text" name="user_name" value="${ifn:cleanHtmlAttribute(param.user_name)}">
						</div>
					</td>
				</tr>
			</table>
		</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form action="${cpath}/master/SavedSearches/delete.htm" autocomplete="off">
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th width="30px">
						<input type="checkbox" name="_checkAllForClose" onclick="return checkOrUncheckAll('_deleteSearch', this)"/>
					</th>
					<th width="30px">#</th>
					<insta:sortablecolumn name="search_name" title="Search Name"/>
					<insta:sortablecolumn name="user_name" title="User Name"/>
					<insta:sortablecolumn name="screen_name" title="Screen Name"/>
				</tr>
				  <c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				  	<c:set var="allowModify" value="${roleId == 1 || roleId == 2 || userid == record.user_name}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{search_id: '${record.search_id}'}, [${allowModify}]);" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="_deleteSearch" value="${record.search_id}" ${allowModify ? '' : 'disabled' }></td>
                        <td>${(pagedlist.pageNumber-1) * pagedlist.pageSize + st.index + 1 }</td>
						<td>${record.search_name}</td>
						<td>${record.user_name}</td>
						<td>${record.screen_name}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<insta:noresults hasResults="${not empty pagedList.dtoList}"/>
		<div class="screenActions" style="display: ${not empty pagedList.dtoList}">
			<button type="submit" accesskey="D" onclick="return checkBoxesChecked('_deleteSearch', event);">
			<b><u>D</u></b>elete</button>
		</div>
	</form>

</body>
</html>

