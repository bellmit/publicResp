<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Diagnostic Reagents List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var testNames = ${requestScope.testnames};
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/master/DiagnosticReagentMaster.do?_method=show",
				onclick : null,
				description : "View and/or Edit the contents of this Reagent"
				}
		};

		function init() {
			createToolbar(toolBar);
			testAutoComplete();
		}

		function testAutoComplete() {
			YAHOO.example.testNamesArray = [];
			YAHOO.example.testNamesArray.length =testNames.length;

			for (var i=0;i<testNames.length;i++) {
				var item = testNames[i]
					YAHOO.example.testNamesArray[i] = item["TEST_NAME"];
			}

			YAHOO.example.ACJSArray = new function() {
				// Instantiate first JS Array DataSource
				datasource = new YAHOO.widget.DS_JSArray(YAHOO.example.testNamesArray);
				var autoComp = new YAHOO.widget.AutoComplete('test_name','testContainer', datasource);
				autoComp.prehighlightClassName = "yui-ac-prehighlight";
				autoComp.typeAhead = true;
				autoComp.useShadow = true;
				autoComp.allowBrowserAutocomplete = false;
				autoComp.minQueryLength = 1;
				autoComp.maxResultsDisplayed = 20;
				autoComp.autoHighlight = false;
				autoComp.forceSelection = false;
				autoComp.textboxFocusEvent.subscribe(function() {
						var sInputValue = YAHOO.util.Dom.get('test_name').value;
						if(sInputValue.length === 0) {
							var oSelf = this;
							setTimeout(function(){oSelf.sendQuery(sInputValue);},0);
						}

				});
			}
		}
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<h1>Diagnostic Reagent Master</h1>

<insta:feedback-panel/>

<form name="DiaReagentForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="DiaReagentForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Test Name</div>
					<div class="sboFieldInput">
						<input type="text" name="test_name" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}" />
						<input type="hidden" name="test_name@op" value="ico"/>
					<div id="testContainer" style="width: 220px"></div>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status</div>
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
			<insta:sortablecolumn name="test_name" title="Test Name"/>
			<th>Reagent Name</th>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {test_id: '${record.test_id}',testName: '${record.test_name}'},'');">

				<td>
					${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
				</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${record.test_name}
				</td>
				<td>${record.item_name}</td>
			</tr>
		</c:forEach>
		</table>
	</div>

		<c:if test="${empty pagedList.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="DiagnosticReagentMaster.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left">
			<a href="${Url}">Add New Diagnostic Reagent</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>
</body>
</html>
