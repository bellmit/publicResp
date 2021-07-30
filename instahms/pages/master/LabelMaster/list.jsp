<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="generalmasters.labelmaster.list.title"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
	<insta:js-bundle prefix="generalmasters.labelmaster"/>
   <script>
		var toolbarOptions = getToolbarBundle("js.generalmasters.labelmaster.toolbar");
	</script>
	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'master/LabelMaster.do?_method=show',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
			}
		};
		function init()
		{
			createToolbar(toolbar);
			autoLabelMaster();
		}
		var label = <%= request.getAttribute("labelsList") %>;
		var rAutoComp;
		function autoLabelMaster() {
			var datasource = new YAHOO.util.LocalDataSource({result: label});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [ {key : "LABEL_SHORT"},{key : "LABEL_ID"},{key : "LABEL_MSG"} ]
			};
			var rAutoComp = new YAHOO.widget.AutoComplete('label_short','labelcontainer', datasource);
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
<c:set var="labelname">
   <insta:ltext key="generalmasters.labelmaster.list.labelname"/>
</c:set>
<c:set var="status">
   <insta:ltext key="generalmasters.labelmaster.list.active"/>,
   <insta:ltext key="generalmasters.labelmaster.list.inactive"/>
</c:set>
<body onload="init()">
 <c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1><insta:ltext key="generalmasters.labelmaster.list.label"/></h1>

	<insta:feedback-panel/>

	<form name="LabelSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="LabelSearchForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField" style="height: 70px;width: 250px">
						<div class="sboField" style="height:69px">
							<div class="sboFieldLabel"><insta:ltext key="generalmasters.labelmaster.list.labelname"/></div>
								<div class="sboFieldInput">
									<input type="text" name="label_short" id="label_short" value="${ifn:cleanHtmlAttribute(param.label_short)}" style = "width:15em" >
									<input type="hidden" name="region_name@op" value="ico" />
									<div id="labelcontainer" style = "width:32em"></div>
								</div>
						</div>
		 			</td>
					<td class="sboField" style="height: 70px">
					<div class="sboField">
						<div class="sboFieldLabel"><insta:ltext key="generalmasters.labelmaster.list.status"/></div>
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
					<insta:sortablecolumn name="label_short" title="${labelname}"/>
					<th><insta:ltext key="generalmasters.labelmaster.list.labeldescription"/></th>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{label_id: '${record.map.label_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.map.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.map.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<insta:truncLabel value="${record.map.label_short}" length="50"/>
						</td>
						<td><insta:truncLabel value="${record.map.label_msg}" length="100"/></td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>

		<c:url var="url" value="LabelMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />"><insta:ltext key="generalmasters.labelmaster.list.add"/></a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="generalmasters.labelmaster.list.inactive"/></div>
		</div>
	</form>

</body>
</html>