<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Discount Authorizer Master List - Insta HMS</title>
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
				href : "/master/DiscountAuthorizerMaster.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of this Discount"
				}
		};

		function init() {
			createToolbar(toolBar);
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Discount Authorizer Master</h1>

<insta:feedback-panel/>

<form name="AuthorizerForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="AuthorizerForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Authorizer Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="disc_auth_name" value="${ifn:cleanHtmlAttribute(param.disc_auth_name)}" />
						<input type="hidden" name="disc_auth_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="disc_auth_name" title="Authorizer Name"/>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable', {disc_auth_id: '${record.disc_auth_id}'},[${record.disc_auth_id != -1}],'');">

					<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${record.disc_auth_name}
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="DiscountAuthorizerMaster.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left;">
			<a href="${Url}">Add New Discount Authorizer</a></b>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>

</body>
</html>
