<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Linen User Master</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<script>
	var toolbar = {
		View: {
			title: "View/Edit",
			imageSrc: "icons/Edit.png",
			href: '/master/LinenUserMaster.do?_method=show',
			onclick: null,
			description: "View/Edit Linen User Details"
		}
	};
</script>
</head>

<body onload="createToolbar(toolbar);">
<c:set var="catUserList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty catUserList}"/>
	<h1>Linen User Master</h1>
	<insta:feedback-panel/>
	<form name="LinenUserCategoryForm">
		<input type="hidden" name="_method" value="list">
		<insta:search form="LinenUserCategoryForm" optionsId="optionalFilter" closed="${hasResults}" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Category User:</div>
					<div class="sboFieldInput">
						<insta:selectdb id="category_user_id" name="category_user_id" value="${bean.map.category_user_id}"  
							class="dropdown" style="width:140px;" valuecol="category_user_id" table="linen_user_master"
							displaycol="category_user_name"  dummyvalue="-- Select --"  filtered="false"/>
					</div>
				</div>
			</div>
		</insta:search>
	
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th>Category</th>
				<th>Category User Name</th>
			</tr>
			
			 <c:forEach var="cat" items="${catUserList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{category_user_id:'${cat.category_user_id }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td><c:out value="${cat.category_name}"/></td>
					<td><c:out value="${cat.category_user_name}"/></td>
				</tr>
			</c:forEach>
		</table>
	
		<table class="formtable">
			<c:url var="url" value="LinenUserMaster.do">
				<c:param name="_method" value="add"></c:param>
			</c:url>
			<tr>
				<td>
					<a href="<c:out value='${url}' />">Add New Linen User</a>
				</td>
			</tr>
		</table>
		
	</form>
</body>

</html>
