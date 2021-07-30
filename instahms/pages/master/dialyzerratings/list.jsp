<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="generalmasters.dialyzerratings.list.dialyzerratinglist"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>


	<insta:js-bundle prefix="dialysismodule.dialyzerrating"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.dialyzerrating.toolbar");
	</script>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: '/master/dialyzerratings/show.htm?',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
				}
		};
		function init()
		{
			createToolbar(toolbar);
		}
	</script>

	<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>

</head>
<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
<c:set var="dialyzerrating">
 <insta:ltext key="generalmasters.dialyzerratings.list.dialyzerrating"/>
</c:set>

<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>
	<h1><insta:ltext key="generalmasters.dialyzerratings.list.dialyzerrating"/></h1>

	<insta:feedback-panel/>

	<form name="DialRatingSearchForm" method="GET">
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="DialRatingSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height:80px" valign="top">
						<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialyzerratings.list.dialyzerrating"/>:</div>
						<div class="sboFieldInput">
							<input type="text" name="dialyzer_rating" value="${ifn:cleanHtmlAttribute(param.dialyzer_rating)}">
							<input type="hidden" name="dialyzer_rating@op" value="ico" />
						</div>
					</td>
					<td class="sboField" style="height:80px" valign="top">
						<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialyzerratings.list.status"/></div>
						<div class="sboFieldInput">
							<insta:checkgroup name="status" opvalues="A,I" optexts="${status}" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
				</tr>
			</table>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="dialyzer_rating" title="${dialyzerrating}"/>
					<th><insta:ltext key="generalmasters.dialyzerratings.list.description"/></th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
						{dialyzer_rating_id: '${record.dialyzer_rating_id}', dialyzer_rating: '${record.dialyzer_rating}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'> Inactive</c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'> Active</c:if>
							${record.dialyzer_rating}
						</td>
						<td>${record.description}</td>
					</tr>

				</c:forEach>
			</table>

			<insta:noresults hasResults="${hasResults}"/>

		</div>

		<c:url var="url" value="/master/dialyzerratings/add.htm">
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />"><insta:ltext key="generalmasters.dialyzerratings.list.addnewrating"/></a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="generalmasters.dialyzerratings.list.inactive"/></div>
		</div>

	</form>

</body>
</html>
