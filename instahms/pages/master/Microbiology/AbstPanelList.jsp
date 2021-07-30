<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>ABST Panel Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/MicroAbstPanel.do?_method=show',
				onclick: null,
				description: "View and/or Edit ABST Panel details"
				}
		};
		function init()
			{
				createToolbar(toolbar);
				shortImpressionNamesAutoCmplt();
			}

	var autoComplete = null;

		function shortImpressionNamesAutoCmplt() {
			var datasource = new YAHOO.widget.DS_JSArray(${abstPanelNames});
			autoComplete = new YAHOO.widget.AutoComplete('abst_panel_name', 'abstPanelContainer', datasource);
			autoComplete.maxResultsDisplayed = 15;
			autoComplete.allowBrowserAutocomplete = false;
			autoComplete.prehighlightClassName = "yui-ac-prehighlight";
			autoComplete.typeAhead = false;
			autoComplete.useShadow = false;
			autoComplete.minQueryLength = 0;
			autoComplete.animVert = false;
			autoComplete.autoHighlight = false;
		}
	</script>

</head>

<body onload="init()">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>ABST Panel Master</h1>

	<insta:feedback-panel/>

	<form name="MicroAbstPanelForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="MicroAbstPanelForm">
			<table class="searchBasicOpts" >
				<tr>
					<td class="sboField">
						<div class="sboFieldLabel">ABST panel name:</div>
						<div class="sboFieldInput">
							<input type="text" name="abst_panel_name" id="abst_panel_name" value="${ifn:cleanHtmlAttribute(param.abst_panel_name)}">
							<input type="hidden" name="abst_panel_name@op" value="ico" />
							<div id="abstPanelContainer" />
						</div>
					</td>
					<td class="sboField" >
						<div class="sboFieldLabel">Status:</div>
						<div class="sboFieldInput"  style="height:50px">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
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
					<insta:sortablecolumn name="abst_panel_name" title="ABST Panel"/>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{abst_panel_id: '${record.abst_panel_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>${record.abst_panel_name}
						</td>
					</tr>
				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="MicroAbstPanel.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}'/>">Add New Micro ABST Panel</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>