<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title> Prescriptions Medicine Master - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	var itemToolbar = {
		Edit : {title : 'Edit',
		imageSrc : 'icons/Edit.png',
		href: 'master/Medicine/PrescriptionsMaster.do?_method=show'
		}
	};
	function init(){
		createToolbar(itemToolbar);
	}

</script>
</head>
<body onload=init();>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Medicines</h1>

<insta:feedback-panel/>
<form name="searchForm" action="PrescriptionsMaster.do">
	<input type="hidden" name="_method" value="list">
	<insta:search-lessoptions form="searchForm" >
		<table class="searchBasicOpts" >
			<tr>
				<td class="sboField">
					<div class="sboFieldLabel">Medicine Name</div>
					<div class="sboFieldInput">
						<input type="text" name="medicine_name" value="${ifn:cleanHtmlAttribute(param.medicine_name)}"/>
						<input type="hidden" name="medicine_name@op" value="ilike"/>
					</div>
				</td>
			</tr>
		  </table>
	</insta:search-lessoptions>
</form>

<form name="MedicineDosageForm" method="GET" onsubmit="return checkBoxesChecked('checked', event);">

	<input type="hidden" name="_method" value="delete"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" >
		<tr>
			<th>#</th>
			<insta:sortablecolumn name="medicine_name" title="Medicine"/>
			<th>Delete</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{medicine_name: '${record["medicine_name"]}'});"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>${ifn:cleanHtml(record['medicine_name'])}</td>
				<td><input type="checkbox" name="checked" value="${ifn:cleanHtmlAttribute(record['medicine_name'])}"></td>
			</tr>
		</c:forEach>
		</table>
	</div>

	<insta:noresults hasResults="${hasResults}" message="Medicines not found"/>

	<table  class="screenActions">
		<tr>
			<td><button type="submit" name="delete" accesskey="D" ><b><u>D</u></b>elete</button></td>
			<td>&nbsp;</td>
			<td>|</td>
			<td>&nbsp;</td>
				<c:url var="Url" value="PrescriptionsMaster.do">
					<c:param name="_method" value="add"/>
				</c:url>
			<td><a href="${Url}">Add Medicine</a></td>
		</tr>
	</table>

</form>

</body>
</html>
