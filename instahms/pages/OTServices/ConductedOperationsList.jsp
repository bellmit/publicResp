<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Conducted Surgeries/Procedures List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ajax.js"/>
	<script>
		var cpath = '${cpath}';
		var operations = ${operations};
		var operationToolbar = {
			AddDoc: { title: 'View/Add Doc', imageSrc: 'icons/Edit.png',
					href: 'otservices/OperationDocumentsList.do?_method=searchOperationDocuments',
					show: ${urlRightsMap.conduct_or_add_operation_doc == 'A'}
			}

		};

		function init() {
			createToolbar(operationToolbar);
			initMrNoAutoComplete('${cpath}');
			initOperationAutoComplete();
		}

		function initOperationAutoComplete() {
			var ds = new YAHOO.util.LocalDataSource({result:operations});
		 	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

			ds.responseSchema = {
				resultsList : "result",
				fields : [  {key : "operation_name"},{key : "op_id"} ]
			};

			var autoComp = new YAHOO.widget.AutoComplete('operation', 'opConatainer', ds);
			autoComp.minQueryLength = 0;
			autoComp.animVert = false;
			autoComp.maxResultsDisplayed = 20;
			autoComp.resultTypeList = false;
			autoComp.forceSelection = false;
		}

	</script>
</head>
<body onload="init();ajaxForPrintUrls();">
<h1>Conducted Surgeries/Procedures List</h1>
<insta:feedback-panel/>
<form method="GET"  action="${cpath}/otservices/ConductedOperations.do" name="operationSearchForm">
	<c:set var="operationsList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty operationsList}"/>
	<input type="hidden" name="_method" value="conductedList"/>
	<input type="hidden" name="_searchMethod" value="conductedList"/>

	<insta:search form="operationSearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Mr No:</div>
				<div class="sboFieldInput">
					<div id="mrNoAutocomplete">
						<input type="text" name="mr_no" id="mrno" size="10" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
			<div class="sboField">
			<div class="sboFieldLabel">Surgery/Procedure Name</div>
				<div class="sboFieldInput">
				<div id="opAutocomplete">
					<input type="text" name="operation" id="operation" value="${ifn:cleanHtmlAttribute(param.operation)}"/>
					<div id="opConatainer" style="width: 600px;"></div>
				</div>
				<input type="hidden" name="operation@op" value="ilike">
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Surgery/Procedure Scheduled Date</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="start_datetime" valid="past" id="start_datetime0" value="${paramValues.start_datetime[0]}" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="start_datetime" valid="past" id="start_datetime1" value="${paramValues.start_datetime[1]}" />
								<input type="hidden" name="start_datetime@op" value="ge,le">
								<input type="hidden" name="start_datetime@type" value="date">
								<input type="hidden" name="start_datetime@cast" value="y">
							</div>
					</td>
					<td>
						<div class="sfLabel">Department</div>
						<div class="sfField">
							<insta:selectdb name="dept_id" table="department" dummyvalue="----Department----"
								valuecol="dept_id"  displaycol="dept_name"
								value="${param.dept_id}"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Type:</div>
						<div class="sfField">
							<insta:checkgroup name="visit_type" opvalues="i,o" optexts="IP,OP"
									selValues="${paramValues.visit_type}"/>
						</div>
					</td>

				</tr>
				<tr>

					<td>
						<div class="sfLabel">Order Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="prescribed_date" valid="past"	id="prescribed_date0" value="${paramValues.prescribed_date[0]}" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="prescribed_date" valid="past"	id="prescribed_date1" value="${paramValues.prescribed_date[1]}" />
							<input type="hidden" name="prescribed_date@op" value="ge,le">
							<input type="hidden" name="prescribed_date@type" value="date">
							<input type="hidden" name="prescribed_date@cast" value="y">
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
<form name="signOffReports" action="${cpath}/otservices/ConductedOperations.do?" method="POST">
	<input type="hidden" name="_method" value="signOffSelectedReports"/>
	<div class="resultList">
		<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<insta:sortablecolumn name="mr_no" title="MR No."/>
				<insta:sortablecolumn name="patient_id" title="Visit Id"/>
				<th>Patient Name</th>
				<insta:sortablecolumn name="prescribed_date" title="Order Date"/>
				<th>Surgery/Procedure Name</th>
				<th>Report</th>
			</tr>
			<c:forEach items="${operationsList}" var="operation" varStatus="st">

				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{prescription_id: '${operation.prescription_id}'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${operation.mr_no}</td>
					<td>${operation.patient_id}</td>
					<td><insta:truncLabel value="${operation.patient_name}" length="15"/></td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${operation.prescribed_date}"/></td>
					<td><insta:truncLabel value="${operation.operation}" length="15"/></td>
					<td><c:if test="${operation.doc_count > 0}"><img src="${cpath}/icons/filewithtick.png" width="16px" height="16px"/></c:if></td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<insta:noresults hasResults="${hasResults}"/>
</form>
</body>
</html>
