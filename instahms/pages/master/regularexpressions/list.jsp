<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="generalmasters.regularexpression.list.title"/></title>
	<c:set var="pagePath" value="<%=URLRoute.REGULAR_EXPRESSION_PATH %>" />

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
	<insta:js-bundle prefix="generalmasters.regularexpression"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.generalmasters.regularexpression.toolbar");
	</script>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
			}
		};
		function init()
		{
			createToolbar(toolbar);
			autoRegExpMaster();
		}
		var listbean  = ${ifn:convertListToJson(RegExpList)};
		
		var rAutoComp;
		function autoRegExpMaster() {
			var datasource = new YAHOO.util.LocalDataSource({result: listbean});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [ {key : "pattern_name"},{key : "pattern_id"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('pattern_name','patterncontainer', datasource);
			rAutoComp.minQueryLength = 0;
		 	rAutoComp.maxResultsDisplayed = 20;
		 	rAutoComp.forceSelection = false ;
		 	rAutoComp.animVert = false;
		 	rAutoComp.resultTypeList = false;
		 	rAutoComp.typeAhead = false;
		 	rAutoComp.allowBroserAutocomplete = false;
		 	rAutoComp.prehighlightClassname = "yui-ac-prehighlight";
			rAutoComp.autoHighlight = true;
			rAutoComp.useShadow = false;
		 	if (rAutoComp._elTextbox.value != '') {
					rAutoComp._bItemSelected = true;
					rAutoComp._sInitInputValue = rAutoComp._elTextbox.value;
			}
		}
	</script>
</head>
<c:set var="patternname">
   <insta:ltext key="generalmasters.regularexpression.list.patternname"/>
</c:set>
<c:set var="status">
   <insta:ltext key="generalmasters.regularexpression.list.active"/>,
   <insta:ltext key="generalmasters.regularexpression.list.inactive"/>
</c:set>
<body onload="init()">
 <c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1><insta:ltext key="generalmasters.regularexpression.list.pattern" /></h1>

	<insta:feedback-panel/>

	<form name="RegExpSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="RegExpSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px;width: 250px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel">${patternname }</div>
								<div class="sboFieldInput">
									<input type="text" name="pattern_name" id="pattern_name" value="${ifn:cleanHtmlAttribute(param.pattern_name)}" style = "width:15em" >
									<input type="hidden" name="region_name@op" value="ico" />
									<div id="patterncontainer" style = "width:32em"></div>
								</div>
						</div>
		 			</td>
					<td class="sboField" style="height: 70px">
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="generalmasters.regularexpression.list.status"/></div>
							<div class="sboFieldInput">
								<insta:checkgroup name="status" optexts="${status}" opvalues="A,I" selValues="${paramValues.status}"/>

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
					<insta:sortablecolumn name="pattern_name" title="${patternname}"/>
					<th><insta:ltext key="generalmasters.regularexpression.list.patternexpression" /></th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{pattern_id: '${record.pattern_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<insta:truncLabel value="${record.pattern_name}" length="50"/>
						</td>
						<td><insta:truncLabel value="${record.regexp_pattern}" length="100"/></td>
					</tr>
				</c:forEach>
			</table>
			
				<insta:noresults hasResults="${hasResults}"/>
			
		</div>

		
		<div class="screenActions" style="float:left"><a href="${cpath}/${pagePath}/add.htm"><insta:ltext key="generalmasters.regularexpression.list.add" /></a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="generalmasters.regularexpression.list.inactive"/></div>
		</div>
	</form>

</body>
</html>
