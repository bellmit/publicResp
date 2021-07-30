<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Patient Images List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<script>
	function deleteSelected(e) {
		var deleteEl = document.getElementsByName("delete_image");
		for (var i=0; i< deleteEl.length; i++) {
			if (deleteEl[i].checked) {
				return true;
			}
		}
		alert("select at least one image for delete");
		YAHOO.util.Event.stopEvent(e);
		return false;
	}

		var toolbar = {
			Edit: {
				title: "View",
				imageSrc: "icons/View.png",
				href: 'master/GenericImageMaster.do?_method=view',
				onclick: null,
				description: "View Image"
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
</script>
</head>
<body onload="init()">
	<c:set var="images_list" value="${pagedList.dtoList}"/>
	<div class="pageHeader">Generic Images</div>
	<insta:feedback-panel/>
	<insta:paginate numPages="${pagedList.numPages}" curPage="${pagedList.pageNumber}" totalRecords="${pagedList.totalRecords}"/>
	<form action="GenericImageMaster.do" method="POST">
		<input type="hidden" name="_method" value="delete"/>
		<div class="resultList">
			<table class="dataTable" align="center" width="100%" id="resultTable">
				<tr>
					<th style="padding-top: 0px;padding-bottom: 0px"><input type="checkbox" name="checkAllForClose" onclick="return checkOrUncheckAll('delete_image', this)"/></th>
					<th >Image Name</th>
					<th >Content Type</th>
				</tr>
				<c:forEach items="${images_list}" var="images" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{image_id: '${images.map.image_id}'},'');" id="toolbarRow${st.index}">
						<td><input type="checkbox" name="delete_image" id="delete_image" value="${images.map.image_id}"></td>
						<td>${images.map.image_name}</td>
						<td>${images.map.content_type}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<insta:noresults hasResults="${not empty images_list}" message="No Images found."/>
		<table class="screenActions">
			<tr>
				<c:url value="GenericImageMaster.do" var="addurl">
					<c:param name="_method" value="add"/>
				</c:url>
				<td><button type="submit" name="delete" id="delete" accesskey="D" onclick="return deleteSelected(event);">
				<b><u>D</u></b>elete</button>
				</td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="<c:out value='${addurl}' />">Add</a></td>
			</tr>
		</table>
	</form>
</body>
</html>
