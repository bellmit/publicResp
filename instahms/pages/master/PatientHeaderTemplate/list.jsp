<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Patient Header Templates - Insta HMS</title>
		<insta:link type="css" file="widgets.css"/>
		<script language="javascript">
			function checkForType(anchor) {
				if (document.getElementById('type').value == '') {
					alert('Please select the module');
					return false;
				} else {
					anchor.href = anchor.href + "&type="+document.getElementById('type').value;
				}
			}
			function init() {
				var toolbar = {
					Edit :	{ title: "Edit", imageSrc: "icons/Edit.png",  href: 'master/PatientHeaderTemplate.do?_method=show'}
				}
				createToolbar(toolbar);
			}

		</script>

	</head>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<body onload="init();">
		<h1>Patient Header Templates</h1>
		<insta:feedback-panel/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
				<tr>
					<th>#</th>
					<th>Template Name</th>
					<th>User Name</th>
					<th>Reason for Customization</th>
				</tr>
				<c:forEach var="phTemplate" items="${phTemplates}" varStatus="st">
					<tr class="${st.first ? 'firstRow' : ''}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{template_id: '${phTemplate.map.template_id}', type: '${phTemplate.map.type}'},
						null);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}" >
						<td>${(pagedList.pageNumber-1)*pagedList.pageSize+st.index+1 }</td>
						<td>
							<c:if test="${phTemplate.map.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${phTemplate.map.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${phTemplate.map.template_name}
						</td>
						<td>${phTemplate.map.user_name}</td>
						<td>${phTemplate.map.reason}</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<insta:noresults hasResults="${not empty phTemplates}" message="No Templates found."/>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
		<div class="screenActions" >
			<select name="type" id="type" class="dropdown ">
				<option value="">--Select Module--</option>
				<c:forEach var="pTemplateType" items="${pHeaderTypes}">
					<option value="${pTemplateType.type}">${pTemplateType.title}</option>
				</c:forEach>
			</select>
			<a href="PatientHeaderTemplate.do?_method=add" title="Add New Patient Header Template" onclick="return checkForType(this)">Add</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
	</body>
</html>