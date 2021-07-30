<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Service Sub Group - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ServiceGroup/ServiceSubGroup.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

</head>

<body onload="createToolbar(toolbar);init();">
	<form name="ServiceSubGroupForm">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list">
	<input type="hidden" id="setServiceSubGroupId" value="${ifn:cleanHtmlAttribute(param.service_sub_group_id)}">
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<h1>Service Sub Groups</h1>
	<insta:feedback-panel />

	<insta:search-lessoptions form="ServiceSubGroupForm" >

		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Service Group:</div>
				<div class="sboFieldInput">
					<insta:selectdb id="ssg.service_group_id" name="ssg.service_group_id" value="${service_group_id}"
							dummyvalue="-- Select --"  table="service_groups" class="dropdown"
							valuecol="service_group_id"  displaycol="service_group_name"  filtered="false" />
							<input type="hidden" name="ssg.service_group_id@type" value="integer" />
				</div>
			</div>
			<div class="sboField" style="height: 69">
				<div class="sboFieldLabel">Status:</div>
				<div class="sboFieldInput">
					<insta:checkgroup name="ssg.status" opvalues="A,I" optexts="Active,Inactive" selValue="${status}"/>
						<input type="hidden" name="status@op" value="in" />
				</div>
			</div>
		</div>

	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<table class="resultList"  id="resultTable" onmouseover="hideToolBar('');">
		<tr>
			<th>Service Group Name</th>
			<th>Service Sub Group Name</th>
			<th>Display Order</th>
			<th>Status</th>
		</tr>

		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{serviceSub_group_id: '${record.service_sub_group_id}'},'');"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
				<td>${record.service_group_name}</td>
				<td>${record.service_sub_group_name}</td>
				<td>${record.display_order}</td>
				<td>
					<c:if test="${record.status eq 'A'}">Active</c:if>
					<c:if test="${record.status eq 'I'}">InActive</c:if>
				</td>
			</tr>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${hasResults}"/>

	<c:url var="url" value="ServiceSubGroup.do">
		<c:param name="_method" value="add"/>
	</c:url>
	<c:url var="editurl" value="ServiceSubGroup.do">
		<c:param name="_method" value="edit"/>
	</c:url>

	<table class="formtable">
		<tr>
			<td>
				<a href="<c:out value='${url}' />">Add New Sub Group</a> | <a href="<c:out value='${editurl}' />">Edit Sub Group</a>
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
				<form name="exportForm" action="ServiceSubGroup.do" >
					<input type="hidden" name="_method" value="exportServiceSubGroupDetails" />
					<button type="submit" accesskey="D" ><b><u>D</u></b>ownload</button>
				</form>
			</td>
		</tr>
		<tr>
			<td style="padding-top: 14px">Import:</td>
			<td style="padding-top: 14px">
				<form name="serviceSubGrpUploadForm" action="serviceSubGrpUpload.do" enctype="multipart/form-data" method="POST">
					<input type="hidden" name="method" value="importServiceSubGroupsDetails" />
					<input type="file" name="xlsServiceSubGroupFile" accept="<insta:ltext key="upload.accept.master"/>"/>
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
