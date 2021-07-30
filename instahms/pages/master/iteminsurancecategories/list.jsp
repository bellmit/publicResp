<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance Item Category Master - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "master/iteminsurancecategories/show.htm?",
				onclick : null,
				description : "View and/or Edit the contents Of Item Insurance Category"
				}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>

<body onload="init()">
	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
	<h1>Insurance Item Category Master</h1>
	<insta:feedback-panel/>

	<form name="itemInsuranceCategoryMasterForm" method="GET">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="itemInsuranceCategoryMasterForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Insu. Item Categ. Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="insurance_category_name" value="${ifn:cleanHtmlAttribute(param.insurance_category_name)}" />
						<input type="hidden" name="insurance_category_name@op" value="ico"/>
					</div>
				</div>

			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList" >
			<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="insurance_category_name" title="Insurance Item Category Name"/>
					<insta:sortablecolumn name="insurance_payable" title="Insurance Payable"/>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable', {insurance_category_id: '${record.insurance_category_id}'},'');">

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td>
							${record.insurance_category_name}
						</td>
						<td>
							<c:if test="${record.insurance_payable eq 'Y'}">Yes</c:if>
							<c:if test="${record.insurance_payable eq 'N'}">No</c:if>
						</td>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<div class="screenActions" style="float: left">
			<a href="${cpath}/master/iteminsurancecategories/add.htm">Add New Insurance Item Category</a>
		</div>
</form>
</body>
</html>




