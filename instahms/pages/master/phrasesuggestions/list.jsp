<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Phrase Suggestions Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="/masters/phraseSuggestions.js" />

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<c:set var="pagepath" value="<%= URLRoute.PHRASE_SUGGESTIONS_PATH %>" />
	
	
	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagepath}/show.htm?',
				onclick: null,
				description: "View and/or Edit Phrase Suggestions details"
				}
		};
		function init()
		{	
			createToolbar(toolbar);
			autoPhraseSuggestionMaster();
		}
		var	phraseSuggestions = JSON.parse('${ifn:cleanJavaScript(ifn:convertListToJson(phraseSuggestionsList))}');
	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Phrase Suggestions Master</h1>

	<insta:feedback-panel/>

	<form name="PhraseSuggestionsSearchForm" method="GET">
		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="PhraseSuggestionsSearchForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Phrase Suggestions:</div>
					<div class="sboFieldInput">
						<input type="text" name="phrase_suggestions_desc" id="phrase_suggestions_desc" value="${ifn:cleanHtmlAttribute(param.phrase_suggestions_desc)}" style = "width:32em" >
						<input type="hidden" name="phrase_suggestions_desc@op" value="ico" />
						<div id="phrasesuggestionscontainer" style = "width:32em"></div>
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table  class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">Phrase Suggestions Category:</div>
							<div class="sfField">
							<select class="dropdown" name="phrase_suggestions_category_id" id="phrase_suggestions_category_id" >
								<option value="">-- Select --</option>
								<c:forEach items="${phraseSuggCategoryList}" var="PhSugCatList">
									<option value="${PhSugCatList.phrase_suggestions_category_id}">${PhSugCatList.phrase_suggestions_category}</option>
								</c:forEach>
							</select>
								<input type="hidden" name="phrase_suggestions_category_id@type" value="integer" /> 
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Status:</div>
							<div class="sfField">
								<insta:checkgroup name="phrasestatus" optexts="Active,Inactive" opvalues="A,I" selValues="${paramValues.phrasestatus}"/>
								<input type="hidden" name="phrasestatus@op" value="in">
							</div>
						</td>
						<td class="last">
							<div class="sfLabel">Department: </div>
							<div class="sfField">
							<select class="dropdown" name="dept_id" id="dept_id" >
								<option value="">-- Select --</option>
								<c:forEach items="${phraseSuggestionsDeptList}" var="PhraseSugDeptList">
									<option value="${PhraseSugDeptList.dept_id}">${PhraseSugDeptList.dept_name}</option>
								</c:forEach>
							</select>
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
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<th></th>
					<insta:sortablecolumn name="phrase_suggestions_desc" title="Phrase Suggestions Name"/>
					<th>Department</th>
					<th>Phrase Suggestions Category</th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{phrase_suggestions_id: '${record.phrase_suggestions_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td> <c:if test="${record.phrasestatus == 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						</td>
						<td><insta:truncLabel value="${record.phrase_suggestions_desc}" length="30"/></td>
						<td>${record.dept_name}</td>
						<td>${record.phrase_suggestions_category}</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

		<c:url var="url" value="${pagepath}/add.htm"></c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add Phrase Suggestions</a></div>

		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>
	</form>

</body>
</html>