<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Linen Change</title>
<insta:link type="js" file="LinenChange/LinenChange.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<script>
		var linenUsersNames = ${linenUsers};
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '/Linen/LinenChange.do?_method=show',
				onclick: null,
				description: "View and/or Edit Linen Usage Details"
				}
		};
</script>
</head>

<c:set var="linenUsageDetailsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty linenUsageDetailsList}"/>

<body onload="userAutoComplete(user,userContainer);createToolbar(toolbar);" class="yui-skin-sam">
	<h1>Linen Change</h1>
	<insta:feedback-panel/>
	<form name="LinenChangeForm">
	<input type="hidden" name="_method" value="list">
	<insta:search form="LinenUserCategoryForm" optionsId="optionalFilter" closed="${hasResults}" >
	
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Linen User:</div>
				<div class="sboFieldInput">
					<div id="userAutocomplete">
						<input type="text" id="user"
							class="field" style="width:140px;" />
						<input type="hidden" name="linen_user_id"  id="linen_user_id" />
						<div id="userContainer"></div>
					</div>
				</div>
			</div>
		</div>
		
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
		</div>
	
	</insta:search>
	
	<table>
		<tr>
			<td>&nbsp;</td>
		</tr>
	</table>
	
		
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable">
		
		<tr onmouseover="hideToolBar();">
			<th>Linen User</th>
		</tr>
		
		<c:forEach var="linenUsageDetails" items="${linenUsageDetailsList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
					{linen_user_id: '${linenUsageDetails.linen_user_id}'},'');"
				onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
				
				<td>${linenUsageDetails.category_user_name}</td>

			</tr>
		</c:forEach>
		
	</table>
		
	<insta:noresults hasResults="${hasResults}"/>
	
	<table class="formtable">
		<c:url var="url" value="LinenChange.do">
			<c:param name="_method" value="addNewItem"/>
		</c:url>
		<tr>
			<td colspan="6">
				<a href="<c:out value='${url}' />">Add New Items</a>
			</td>
		</tr>
	</table>
	
	</form>
	
</body>

</html>
