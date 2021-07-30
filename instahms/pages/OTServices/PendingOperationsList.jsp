<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Pending Surgery/Procedure List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ajax.js"/>
	<script>
		var cpath = '${cpath}';
		var advOtMod = '${preferences.modulesActivatedMap['mod_advanced_ot']}';
		var conductOrAddOpDoc = '${urlRightsMap.conduct_or_add_operation_doc}';
		var operationNames = ${operations};
		var operationToolbar = {
			Edit : {title: 'Edit', imageSrc: 'icons/Edit.png', href: 'otservices/EditOperation.do?_method=getOperationsConductionScreen',
				show: (conductOrAddOpDoc == 'A' && advOtMod != 'Y')
			},
			AddDoc: { 	title: 'View/Add Doc', imageSrc: 'icons/Edit.png',
						href: 'otservices/OperationDocumentsList.do?_method=searchOperationDocuments',
						show: (conductOrAddOpDoc == 'A' && advOtMod != 'Y')
			},

			OTmanagement : {title: 'OT Management', imageSrc: 'icons/Edit.png', href: 'otservices/OtManagement.do?_method=getOtManagementScreen',
				show: (conductOrAddOpDoc == 'A' && advOtMod =='Y')
			}

		};


		function init() {
			createToolbar(operationToolbar);
			initMrNoAutoComplete('${cpath}');
			OperationAutocomplete();
		}

		function OperationAutocomplete() {
	 	var ds = new YAHOO.util.LocalDataSource({result:operationNames});
	 	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "operation_name"},{key : "op_id"} ]
		};

		var autoComp = new YAHOO.widget.AutoComplete('operation', 'opConatainer', ds);
		autoComp.minQueryLength = 0;
		autoComp.animVert = false;
		autoComp.maxResultsDisplayed = 20;
		autoComp.resultTypeList = false; // making the result available as object literal.
		autoComp.forceSelection = false;
	}
</script>
</head>
<body onload="init();ajaxForPrintUrls();">
<h1>Pending Surgery/Procedure List</h1>
<insta:feedback-panel/>
<form method="GET"  action="${cpath}/otservices/PendingOperations.do" name="operationSearchForm">
	<c:set var="operationsList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty operationsList}"/>
	<input type="hidden" name="_method" value="pendingList"/>
	<input type="hidden" name="_searchMethod" value="pendingList"/>

	<insta:search form="operationSearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mr_no@op" value="ilike" />
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
							<insta:datewidget name="start_datetime" valid="past"	id="start_datetime0" value="${paramValues.start_datetime[0]}" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="start_datetime" valid="past"	id="start_datetime1" value="${paramValues.start_datetime[1]}" />
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
					<td class="last">&nbsp;</td>
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

	<div class="resultList">
		<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<th>#</th>
				<insta:sortablecolumn name="mr_no" title="MR No."/>
				<insta:sortablecolumn name="patient_id" title="Visit Id"/>
				<th>Patient Name</th>
				<th>Surgery/Procedure Name</th>
				<insta:sortablecolumn name="prescribed_date" title="Order Date"/>
				<th>Remarks</th>
				<th>Report</th>
			</tr>
			<c:forEach items="${operationsList}" var="operation" varStatus="st">
				<c:set var="billPaid" value="${ operation.bill_type == 'C' || operation.payment_status != 'U' }"/>
				<c:choose>
					<c:when test="${billPaid}">
						<c:set var="blockUnpaid" value="false"/>
					</c:when>
					<c:otherwise>
						<c:set var="blockUnpaid" value="${ directBillingPrefs.Operation.map.block_unpaid == 'Y' }"/>
					</c:otherwise>
				</c:choose>
				<c:choose>
					<c:when test="${!billPaid}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
					<c:otherwise>
						<c:set var="flagColor" value="empty"/>
					</c:otherwise>
				</c:choose>
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{prescription_id: '${operation.prescribed_id}', visit_id: '${operation.patient_id}',visitId: '${operation.patient_id}'},
						[${!blockUnpaid}, true,${!blockUnpaid}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
					<td>${operation.mr_no}</td>
					<td>${operation.patient_id}</td>
					<td><insta:truncLabel value="${operation.patient_name}" length="25"/></td>
					<td><img src="${cpath}/images/${flagColor}_flag.gif"/><insta:truncLabel value="${operation.operation}" length="25"/></td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${operation.prescribed_date}"/></td>
					<td style="white-space: normal;"><insta:truncLabel value="${operation.remarks}" length="15"/></td>
					<td><c:if test="${operation.doc_count > 0}"><img src="${cpath}/icons/filewithtick.png" width="16px" height="16px"/></c:if></td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<insta:noresults hasResults="${hasResults}"/>
	<div class="legend" style="margin-top: 10px;">
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'/></div>
		<div class="flagText">Un-Paid bills</div>
	</div>

</body>
</html>
