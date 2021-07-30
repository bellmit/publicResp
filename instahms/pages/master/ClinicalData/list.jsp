<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Clinical Data Lab Results Master List - Insta HMS</title>

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>

	<script type="text/javascript">
		var toolbar = {
			Edit: {
				title: "View/Edit",
				imageSrc: "icons/Edit.png",
				href: 'master/ClinicalDataLabResultsMaster.do?_method=show',
				onclick: null,
				description: "View and/or Edit Clinical Lab Data Result details"
				}
		};
		function init()
		{
			createToolbar(toolbar);
			resultNameAutoComplete();
		}

		var resultNamesAndIds = <%= request.getAttribute("resultNamesAndIds") %> ;
		var resultlabelIds = <%= request.getAttribute("resultlabelIds") %>
		var autoComp = null;

		function resultNameAutoComplete() {
			var datasource = new YAHOO.util.LocalDataSource({result: resultNamesAndIds});
			datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
			datasource.responseSchema = {
				resultsList : "result",
				fields : [  {key : "result_test_name"},{key : "resultlabel"},{key : "resultlabel_id"},{key : "units"} ]
			};
			autoComp = new YAHOO.widget.AutoComplete('resultlabel','resultContainer', datasource);
			autoComp.minQueryLength = 0;
			autoComp.maxResultsDisplayed = 20;
			autoComp.forceSelection = true ;
			autoComp.animVert = false;
			autoComp.resultTypeList = false;
			autoComp.autoHighlight = false;

			autoComp.itemSelectEvent.subscribe(function() {
				var resultTestNames = document.getElementById("resultlabel").value;
				if(resultTestNames != '') {
					for ( var i=0 ; i< resultNamesAndIds.length; i++){
						if(resultTestNames == resultNamesAndIds[i]["result_test_name"]){
							document.getElementById("resultlabel").value = resultNamesAndIds[i]["resultlabel"];
							break;
						}
					}
				}else{
					document.getElementById("resultlabel").value = "";
				}
			});

			// autoComp.itemSelectEvent.subscribe(setClinicalDetails);
		}

	</script>

</head>

<body onload="init();">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Clinical Data Lab Results Master</h1>

	<insta:feedback-panel/>

	<form name="ClinicalLabDataSearchForm" method="GET">

		<input type="hidden" name="_method" value="list"/>
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="ClinicalLabDataSearchForm">

			<div class="searchBasicOpts" >
				<div class="sboField" style="height: 69px">
					<div class="sboFieldLabel">Result Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="resultlabel" id="resultlabel" value="${ifn:cleanHtmlAttribute(param.resultlabel)}">
						<input type="hidden" name="resultlabel@op" value="ico" />
						<div id="resultContainer" style="width: 400px"></div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel" >Status:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
						<input type="hidden" name="status@op" value="in" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="resultlabel" title="Result Name"/>
					<insta:sortablecolumn name="units" title="Units"/>
					<insta:sortablecolumn name="display_order" title="Display Order"/>
				</tr>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{resultlabel_id: '${record.resultlabel_id}'},'');" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1 }</td>
						<td>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<insta:truncLabel value="${record.result_test_name}" length="50"/>
						</td>
						<td>
							${record.units}
						</td>
						<td>
							${record.display_order}
						</td>
					</tr>

				</c:forEach>

			</table>

			<c:if test="${param._method == 'list'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>

		<c:url var="url" value="ClinicalDataLabResultsMaster.do">
				<c:param name="_method" value="add"/>
		</c:url>
		<div class="screenActions" style="float:left">
			<a href="<c:out value='${url}' />">Add New Result</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

	</form>

</body>
</html>