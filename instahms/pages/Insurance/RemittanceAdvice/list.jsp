<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Updated Remittance List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View",
				imageSrc : "icons/Edit.png",
				href : "Insurance/RemittanceUpload.do?_method=show",
				onclick : null,
				description : "View the contents of Remittance Advice"
			},
			Update: {
				title: "Update",
				imageSrc: "icons/Change.png",
				href: 'Insurance/RemittanceUpload.do?_method=create&actionBtn=update',
				onclick: null,
				description: "Update the remittance bills & claims"
			}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

	<h1>Remittance Log</h1>

	<insta:feedback-panel/>

	<form name="remform" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="remform" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Insurance Comp Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="insurance_co_name" value="${ifn:cleanHtmlAttribute(param.insurance_co_name)}" />
						<input type="hidden" name="insurance_co_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">TPA/Sponsor Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="tpa_name" value="${ifn:cleanHtmlAttribute(param.tpa_name)}" />
						<input type="hidden" name="tpa_name@op" value="ico"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="detailList" >
			<table class="detailList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="insurance_co_name" title="Insurance Comp Name"/>
					<insta:sortablecolumn name="tpa_name" title="TPA"/>
					<insta:sortablecolumn name="file_name" title="File Name"/>
					<insta:sortablecolumn name="received_date" title="received_date"/>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

					<c:set var="updateEnabled" value="${record.detail_level == 'I'}"/>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable', {remittance_id: '${record.remittance_id}'},
									[true, ${updateEnabled}]);">

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td title="${record.insurance_co_name}"> ${record.insurance_co_name} </td>
						<td title="${record.tpa_name}"> ${record.tpa_name} </td>
						<td title="${record.file_name}"> <insta:truncLabel value="${record.file_name}" length="50" /> </td>
						<td>${record.received_date}</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="Url" value="/Insurance/RemittanceXLUpload.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Upload New Remittance</a>
		</div>
</form>
</body>
</html>




