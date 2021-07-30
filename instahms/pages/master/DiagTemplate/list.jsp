<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<title>Diagnostic Report List - Insta HMS</title>
	<insta:link type="js" file="dashboardsearch.js"/>

 <script>
		function clearSearch() {
			document.searchForm.templateName.value = "";
			document.searchForm.templateDesc.value = "";
		}
		function init() {
			var toolbar = {
				Edit :	{ title: "Edit Template", imageSrc: "icons/Edit.png", href: 'master/DiagTemplate.do?_method=show'},
				EditTest : {title: "Edit Test", imageSrc: "icons/Edit.png", href: 'master/addeditdiagnostics/show.htm?orgId=ORG0001&orgName=GENERAL'}
			};
			createToolbar(toolbar);
			autoCompleteTests();
			autoCompleteDiagDepartments();
       }
	      var testNames = ${testNames};
	      var diagDepartmentNames = ${diagDepartmentNames};
       function autoCompleteTests() {
	      var datasource = new YAHOO.util.LocalDataSource({result: testNames});
	      datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	      datasource.responseSchema = {
		  resultsList: 'result',
		  fields : [
					{key: "used_in_test_name"},
					{key :"used_in_test_id"}
				]
	     };
	  var autoComp = new YAHOO.widget.AutoComplete('used_in_test_name','testContainer', datasource);
	  autoComp.prehighlightClassName = "yui-ac-prehighlight";
	  autoComp.typeAhead = true;
	  autoComp.allowBrowserAutocomplete = false;
	  autoComp.minQueryLength = 0;
	  autoComp.maxResultsDisplayed = 20;
	  autoComp.autoHighlight = false;
	  autoComp.forceSelection = false;
	 }

	function autoCompleteDiagDepartments(){
	    var datasource = new YAHOO.util.LocalDataSource({result: diagDepartmentNames});
	    datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
	    datasource.responseSchema = {
		resultsList: 'result',
		fields : [
					{key: "ddept_name"},
					{key :"ddept_id"}
				]
	    };
	   var autoComp = new YAHOO.widget.AutoComplete('dept_name','deptContainer', datasource);
	   autoComp.prehighlightClassName = "yui-ac-prehighlight";
	   autoComp.typeAhead = true;
	   autoComp.allowBrowserAutocomplete = false;
	   autoComp.minQueryLength = 0;
	   autoComp.maxResultsDisplayed = 20;
	   autoComp.autoHighlight = false;
	   autoComp.forceSelection = false;

	}
 </script>
</head>

<body onload="init();" class="yui-skin-sam">
	<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
	<h1>Diagnostic Report Templates</h1>
	<insta:feedback-panel/>
	<form name="searchForm" method="GET">
		<input type="hidden" name="_method" value="list">
		<insta:search-lessoptions form="searchForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Template Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="format_name" value="${ifn:cleanHtmlAttribute(param.format_name)}" />
						<input type="hidden" name="format_name@op" value="ilike"/>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Description</div>
					<div class="sboFieldInput">
						<input type="text" name="format_description" value="${ifn:cleanHtmlAttribute(param.format_description)}" />
						<input type="hidden" name="format_description@op" value="ilike"/>
					</div>
				</div>
				<div class="sboField">
				   <div class="sboFieldLabel">Test Name:</div>
				   <div class="sboFieldInput">
				      <input type="text"  id="used_in_test_name" name="used_in_test_name" value="${ifn:cleanHtmlAttribute(param.used_in_test_name)}"/>
				      <input type="hidden" name="used_in_test_name@op" value="ilike">
				       <div id="testContainer"></div>
				   </div>
				</div>
				<div class="sboField">
				   <div class="sboFieldLabel">Diag Department:</div>
				   <div class="sboFieldInput">
				      <input type="text"  id="dept_name" name="dept_name" value="${ifn:cleanHtmlAttribute(param.dept_name)}">
				      <input type="hidden" name="dept_name@op" value="ilike">
				       <div id="deptContainer"></div>
				   </div>
				</div>
			</div>
		</insta:search-lessoptions>
	</form>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" width="100%" cellspacing="0" cellpadding="0" id="resultTable">
			<tr>
				<th>#</th>
				<th>Template Name</th>
				<th>Description</th>
				<th>Used in Test</th>
			</tr>
			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<c:set var="enableEditTestLink">
				<c:choose>
					<c:when test="${not empty record.used_in_test_id}">true</c:when>
					<c:otherwise>false</c:otherwise>
				</c:choose>
				</c:set>

				<tr class="${st.first ? 'firstRow': ''}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{testid: '${record.used_in_test_id}', testformat_id: '${record.testformat_id}'},[true, ${enableEditTestLink}], null);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index+1}</td>
					<td>${record.format_name}</td>
					<td><c:out value="${record.format_description}"/></td>
					<td><c:out value="${record.used_in_test_name}"/></td>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>

	<c:url var="addUrl" value="DiagTemplate.do">
		<c:param name="_method" value="add"/>
	</c:url>
	<div class="screenActions" >
		<a href="${ifn:cleanHtmlAttribute(addUrl)}">Add New Template</a>
	</div>



</body>
</html>

