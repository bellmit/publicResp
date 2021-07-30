<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>PCS ICD Codes List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '/master/ICDPCSMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit the contents of this PCS ICD Codes"
				}
		};
		function init() {

			createToolbar(toolbar);
			showFilterActive(document.PCSICDCodesForm);
		}
	</script>

</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>PSC ICD Codes (Treatment Codes)</h1>
<insta:feedback-panel/>
<form name="PCSICDCodesForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search form="PCSICDCodesForm" optionsId="optionalFilter" closed="${hasResults}">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">ICD Code:</div>
				<div class="sboFieldInput">
				<input type="text" name="icd_code" value="${ifn:cleanHtmlAttribute(param.icd_code)}"/>
				<input type="hidden" name="icd_code@op" value="ico"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >

			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">ICD Description:</div>
						<div class="sfField">
							<input type="text" name="icd_description" value="${ifn:cleanHtmlAttribute(param.icd_description)}"/>
							<input type="hidden" name="icd_description@op" value="ico"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Status:</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>

					<td class="last">
						<div class="sfLabel">Code Type:</div>
						<div class="sfField">
							<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type" 
							displaycol="code_type" dummyvalue="..Select.."/>
						</div>
					</td>
					<td class="last">&nbsp;</td>
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>



<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="icd_code" title="ICD Code"/>
			<insta:sortablecolumn name="icd_description" title="ICD Description"/>
			<th>Code Type</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{icd_code: '${record.icd_code}'}, '');" >
						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${record.icd_code}
						</td>
						<td>
							${record.icd_description}
						</td>
						<td>
								${record.code_type}
						</td>
			</tr>
		</c:forEach>
	</table>
</div>

	<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
	</c:if>

	<c:url var="url" value="ICDPCSMaster.do">
			<c:param name="_method" value="add"/>
	</c:url>

	<div class="screenActions" style="float: left">
			<div><a href="<c:out value='${url}' />">Add New PCS ICD Code</a>
			| <a href="./ICDUpload.do?_method=add&icd_type=pcs">PCS ICD Master Upload</a></div>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
	</div>

</form>

</body>
</html>
