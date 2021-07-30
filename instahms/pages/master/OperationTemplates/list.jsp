<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<title>Surgery/Procedure Templates - Insta HMS</title>
		<insta:link type="css" file="widgets.css"/>
		<insta:link type="js" file="widgets.js"/>
		<insta:link type="script" file="dashboardColors.js"/>
		<insta:link type="js" file="dashboardsearch.js"/>
		<style>
			.scrolForContainer .yui-ac-content{
				 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
				 width: 500px;
			}
		</style>
		<script type="text/javascript">
			var operations = ${operations};
			function deleteSelected(e) {
				var deleteEl = document.getElementsByName("_deleteCode");
				for (var i=0; i< deleteEl.length; i++) {
					if (deleteEl[i].checked) {
						document.forms[0]._method.value = 'delete';
						document.forms[0].submit();
						return true;
					}
				}
				alert("select at least one to delete");
				YAHOO.util.Event.stopEvent(e);
				return false;
			}

			function markDelete(obj) {
				var row = YAHOO.util.Dom.getAncestorByTagName(obj, 'tr');
				getElementByName(row, '_deleted').value = obj.checked ? 'Y' : 'N';
			}

			function init() {
				var dataSource = new YAHOO.util.LocalDataSource({result : operations});
				dataSource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
				dataSource.responseSchema = {
					resultsList : "result",
					fields : [  {key : "operation_name"},
								{key : "op_id"},
								{key : "dept_name"}
							 ]
				};

				var autoComplete = new YAHOO.widget.AutoComplete("operation_name", 'opContainer', dataSource);
				autoComplete.minQueryLength = 0;
				autoComplete.maxResultsDisplayed = 50;
				autoComplete.forceSelection = false;
				autoComplete.resultTypeList = false;
				autoComplete.typeAhead = false;
				autoComplete.useShadow = false;
				autoComplete.animVert = false;
				autoComplete.formatResult = function(oResultData, sQuery, sResultMatch) {
					var operation = oResultData;
					return operation.operation_name + " [" + operation.dept_name + "]";
				}
			}

		</script>
	</head>
	<body onload="init()">
		<c:set var="hasResult" value="${not empty pagedList.dtoList ? 'true': 'false'}"/>
		<div class="pageHeader">Surgery/Procedure Templates</div>
		<insta:feedback-panel/>
		<form name="operationTemplatesForm" method="GET">

			<input type="hidden" name="_method" value="list"/>
			<input type="hidden" name="_searchMethod" value="list"/>

			<insta:search-lessoptions form="operationTemplatesForm" >

			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Surgery/Procedure</div>
					<div class="sboFieldInput">
						<div id="autocomplete">
							<input type="text" name="operation_name" id="operation_name" value="${ifn:cleanHtmlAttribute(param.operation_name)}"/>
							<input type="hidden" name="operation_name@op" value="ilike" />
							<div id="opContainer" class="scrolForContainer"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Templates</div>
					<div class="sboFieldInput">
						<select name="_ot_template" class="dropdown">
							<option value="">-- All --</option>
							<c:forEach var="template" items="${templatesList}">
								<c:set var="value" value="${template.map.template_id},${template.map.format}"/>
								<option value="${value}" ${param._ot_template == value ? 'selected' : ''}>
									${template.map.template_name}
								</option>
							</c:forEach>
						</select>
					</div>
				</div>
			</div>
			</insta:search-lessoptions>

			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

			<div class="resultList">
				<table class="resultList" id="resultTable">
					<tr onmouseover="hideToolBar();">
						<th width="10%">Select</th>
						<th width="45%">Surgery/Procedure</th>
						<th width="45%">Template</th>
					</tr>
					<tr>
						<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
							<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index %2 == 0 ? 'even' : 'odd'}"
								id="toolbarRow${st.index}" >

								<td>
									<input type="checkbox" name="_deleteCode" onclick="markDelete(this);"/>
									<input type="hidden" name="_deleted" value="N" />
									<input type="hidden" name="_op_id" value="${record.op_id}" />
									<input type="hidden" name="_template_id" value="${record.template_id}" />
									<input type="hidden" name="_format" value="${record.format}">
								</td>
								<td><insta:truncLabel value="${record.operation_name}" length="60"/></td>
								<td><insta:truncLabel value="${record.template_name}" length="60"/></td>
							</tr>
						</c:forEach>
					</tr>
				</table>
			</div>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

			<c:url var="url" value="OperationTemplates.do">
				<c:param name="_method" value="add"/>
			</c:url>

			<div class="screenActions" style="float: left">
				<input type="button" name="delete" value="Delete" onclick="deleteSelected(event);"/> |
				<a href="<c:out value='${url}' />">Add Surgery/Procedure Template</a>
			</div>
		</form>
	</body>
</html>
