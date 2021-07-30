<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="i18nSupport" content="true"/>
	<title><insta:ltext key="generalmasters.dialysisprepmaster.list.dialysisprepmastertypelist"/></title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<insta:js-bundle prefix="dialysismodule.dialysisprep"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.dialysismodule.dialysisprep.toolbar");
	</script>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: toolbarOptions["editvisit"]["name"],
				imageSrc: "icons/Edit.png",
				href: 'master/DialysisPrepMaster.do?_method=show',
				onclick: null,
				description: toolbarOptions["editvisit"]["description"]
				}
		};
		function init()
		{
			createToolbar(toolbar);
			prepParamAutoComplete();
		}

		var prepParamAndParamIds = <%= request.getAttribute("prepParamAndParamIds") %> ;
		var autoComp = null;

		function prepParamAutoComplete() {
			var datasource = new YAHOO.util.LocalDataSource({result: prepParamAndParamIds});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "prep_param"},{key : "prep_param_id"}]
			};
			autoComp = new YAHOO.widget.AutoComplete('prep_param','prepContainer', datasource);
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.forceSelection = true ;
			autoComp.animVert = false;
			autoComp.resultTypeList = false;
			autoComp.autoHighlight = false;

		}

	</script>
<insta:js-bundle prefix="clinicaldata.commonvalidations"/>
	<insta:js-bundle prefix="dialysismodule.commonvalidations"/>
</head>

<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>
	<c:set var="prepparam">
 		<insta:ltext key="generalmasters.dialysisprepmaster.list.prepparam"/>
	</c:set>

	<c:set var="status">
 <insta:ltext key="generalmasters.dialyzertypes.list.active"/>,
 <insta:ltext key="generalmasters.dialyzertypes.list.inactive"/>
</c:set>

<c:set var="prep">
 <insta:ltext key="generalmasters.dialysisprepmaster.list.pre"/>,
 <insta:ltext key="generalmasters.dialysisprepmaster.list.post"/>
</c:set>

	<c:set var="prepstate">
 <insta:ltext key="generalmasters.dialysisprepmaster.list.prepstate"/>
</c:set>
	<h1><insta:ltext key="generalmasters.dialysisprepmaster.list.dialysisprepmasterlist"/></h1>

	<insta:feedback-panel/>

	<form name="DialysisPrepSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="DialysisPrepSearchForm">

			<div class="searchBasicOpts" >
				<div class="sboField" style="height: 69px">
					<div class="sboFieldLabel"><insta:ltext key="generalmasters.dialysisprepmaster.list.prepparam"/></div>
					<div class="sboFieldInput">
						<input type="text" name="prep_param" id="prep_param" value="${ifn:cleanHtmlAttribute(param.prep_param)}">
						<input type="hidden" name="prep_param@op" value="ico" />
						<div id="prepContainer" style="width: 400px"></div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel" ><insta:ltext key="generalmasters.dialysisprepmaster.list.status"/></div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="${status}" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel" ><insta:ltext key="generalmasters.dialysisprepmaster.list.prepstate"/></div>
					<div class="sboFieldInput">
						<insta:checkgroup name="prep_state" opvalues="pre,post" optexts="${prep}" selValues="${paramValues.prep_state}"/>
						<input type="hidden" name="prep_state@op" value="in" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="prep_param" title="${prepparam}"/>
					<insta:sortablecolumn name="prep_state" title="${prepstate}"/>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{prep_param_id: '${record.prep_param_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<insta:truncLabel value="${record.prep_param}" length="50"/>
						</td>
						<td>
							${record.prep_state}
						</td>

					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="DialysisPrepMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left">
			<a href="<c:out value='${url}' />"><insta:ltext key="generalmasters.dialysisprepmaster.list.addnewdialysisprep"/></a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText"><insta:ltext key="generalmasters.dialysisprepmaster.list.inactive"/></div>
		</div>

	</form>

</body>
</html>