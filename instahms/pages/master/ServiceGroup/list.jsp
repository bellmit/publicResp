<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Service Group - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="js" file="ServiceGroup/ServiceGroup.js"/>
</head>

<body onload="createToolbar(toolbar);">
	<h1>Service Group Master</h1>
	<insta:feedback-panel />
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<form name="ServiceGroupForm">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list">

		<insta:search-lessoptions form="ServiceGroupForm" >

			<table class="formtable">
				<tr>
					<td><div class="sfLabel"><b>Status:</b></div><br>
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
					</td>
				</tr>
			</table>


		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<table class="resultList"  id="resultTable" onmouseover="hideToolBar('');">
			<tr>
				<th>Service Group Name</th>
				<th>Display Order</th>
				<th>Status</th>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{serviceGroup_id: '${record.service_group_id}'},'');"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${record.service_group_name}</td>
					<td>${record.display_order}</td>
					<td>
						<c:if test="${record.status eq 'A'}">Active</c:if>
						<c:if test="${record.status eq 'I'}">InActive</c:if>
					</td>
				</tr>
			</c:forEach>
		</table>
			<insta:noresults hasResults="${hasResults}"/>

		<c:url var="url" value="ServiceGroup.do">
			<c:param name="_method" value="add"/>
		</c:url>
		<c:url var="editurl" value="ServiceGroup.do">
			<c:param name="_method" value="edit"/>
		</c:url>

		<table class="formtable">
			<tr>
				<td>
					<a href="<c:out value='${url}' />">Add New Group</a> | <a href="<c:out value='${editurl}' />">Edit Group</a>
				</td>
			</tr>
		</table>
	</form>
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Export/Import</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
			</div>
	<fieldset class="fieldSetBorder">
	<table style="padding-top: 11px">
		<tr>
			<td>Export:</td>
			<td>
				<form name="exportForm" action="ServiceGroup.do" >
					<input type="hidden" name="_method" value="exportServiceGroupDetails" />
					<button type="submit" accesskey="D" ><b><u>D</u></b>ownload</button>
				</form>
			</td>
		</tr>
		<tr>
			<td style="padding-top: 14px">Import:</td>
			<td style="padding-top: 14px">
				<form name="serviceGrpUploadForm" action="serviceGrpUpload.do" enctype="multipart/form-data" method="POST">
					<input type="hidden" name="method" value="importServiceGroupsDetails" />
					<input type="file" name="xlsServiceGroupFile" accept="<insta:ltext key="upload.accept.master"/>"/>
					<button type="submit" onclick="return doUpload();" accesskey="U"><b><u>U</u></b>pload</button>
				</form>
			</td>
		</tr>
	</table>
	</fieldset>
	</div>

	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
	</script>
</body>

</html>
