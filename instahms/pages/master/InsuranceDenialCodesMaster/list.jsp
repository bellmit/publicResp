<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Denial Codes List - Insta HMS</title>
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
				href: '/master/InsuranceDenialCodes.do?_method=show',
				onclick: null,
				description: "View and/or Edit the contents of this Denial Code"
				}
		};
		function init() {
			createToolbar(toolbar);
			showFilterActive(document.DenialCodesForm);
		}
	</script>

</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>Denial Codes Master</h1>
<insta:feedback-panel/>
<form name="DenialCodesForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search form="DenialCodesForm" optionsId="optionalFilter" closed="${hasResults}">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Denial Code:</div>
				<div class="sboFieldInput">
				<input type="text" name="denial_code" value="${ifn:cleanHtmlAttribute(param.denial_code)}"/>
				<input type="hidden" name="denial_code@op" value="ico"/>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldlabel">Code Type:</div>
				<div class="sboFieldInput">
					<insta:selectdb name="type" table="insurance_denial_code_types" valuecol="type"
						displaycol="type" dummyvalue="..Select.." value="${param.type}"/>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >

			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Code Description:</div>
						<div class="sfField">
							<input type="text" name="code_description" value="${ifn:cleanHtmlAttribute(param.code_description)}"/>
							<input type="hidden" name="code_description@op" value="ico"/>
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
						<div class="sfLabel">Special Denial Code On Correction:</div>
						<div class="sfField">
							<insta:selectoptions name="special_denial_code_on_correction" value="${param.special_denial_code_on_correction}" opvalues="N,Y"
							optexts="No,Yes" dummyvalue="(All)" />
						</div>
					</td>

					<td class="last">&nbsp;</td>
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
			<insta:sortablecolumn name="denial_code" title="Denial Code"/>
			<insta:sortablecolumn name="code_description" title="Code Description"/>
			<th>Example</th>
			<th>Type</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{denial_code: '${record.denial_code}'});" >

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${record.denial_code}
				</td>
				<td>
					<insta:truncLabel value="${record.code_description}" length="50"/>
				</td>
				<td>
					<insta:truncLabel value="${record.example}" length="30"/>
				</td>
				<td>
					<insta:truncLabel value="${record.type}" length="15"/>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<c:if test="${empty pagedList.dtoList}"> <insta:noresults hasResults="${hasResults}"/> </c:if>

<c:url value="InsuranceDenialCodes.do" var="denialUrl">
	<c:param name="_method" value="add" />
</c:url>
<div class="screenActions" style="float:left"><a href="${denialUrl}">Add New Denial Code</a></div>

<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText">Inactive</div>
</div>

</form>
</body>
</html>
