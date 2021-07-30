<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@page import="com.insta.hms.master.URLRoute"%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Code Sets - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="script" file="dashboardColors.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.CODE_SETS_PATH%>" />
<script type="text/javascript">
	function init() {
	}
	function validate() {
		var codeSystemId = document.getElementById('code_systems_id');
		var codeSystemCategoryId = document.getElementById("code_system_category_id");
		if (codeSystemCategoryId.selectedIndex == 0) {
			alert("Select Master Name");
			document.getElementById("code_system_category_id").focus();
			return false;
		}
		if (codeSystemId.selectedIndex == 0) {
			alert("Select code system");
			document.getElementById('code_systems_id').focus();
			return false;
		}
		return true;
	}
</script>
</head>
<body onload="init()">
	<h1>Code Sets Master</h1>
	<form method="GET" name="searchform">
		<insta:search-lessoptions form="searchform">
			<div class="searchBasicOpts">
				<div class="sboField">
					<div class="sboFieldLabel">Master Name :</div>
					<div class="sboFieldInput">
						<insta:selectdb name="code_system_category_id"
							id="code_system_category_id" value="${param.code_system_category_id}"
							table="code_system_categories" valuecol="id" orderby="label"
							displaycol="label" filtered="true" filtercol="status"
							filtervalue="A" dummyvalue="-- Select --" />
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel">Code System :</div>
					<div class="sboFieldInput">
						<insta:selectdb name="code_systems_id" id="code_systems_id"
							value="${param.code_systems_id}" table="code_systems" valuecol="id"
							orderby="label" displaycol="label" filtered="true"
							filtercol="status" filtervalue="A" dummyvalue="-- Select --" />
					</div>
				</div>
			</div>
		</insta:search-lessoptions>
		<insta:paginate curPage="${pagedList.pageNumber}"
			numPages="${pagedList.numPages}"
			totalRecords="${pagedList.totalRecords}" />
	</form>

	<form name="codeSetsForm" method="POST" action="${cpath}/master/codesets/save.htm">
		<div class="resultList">
			<c:if test="${codeSystemCategoryLabel != '' && codeSystemsLabel != ''}"><h1>${codeSystemCategoryLabel}-${codeSystemsLabel}</h1></c:if>
			<table class="resultList" cellspacing="" cellpadding=""
				id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<th>Sl No.</th>
					<insta:sortablecolumn name="entity_name" title="Entity Name" />
					<th>Code</th>
					<th>Code Description</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr>
						<input type="hidden" id="code_set_id" name="code_set_id"
							value="${record.code_set_id}" />
						<input type="hidden" id="entity_id" name="entity_id"
							value="${record.entity_id}" />
						<input type="hidden" id="code_system_id" name="code_system_id"
							value="${codeSystemsId}" />
						<input type="hidden" id="code_system_category_id"
							name="code_system_category_id" value="${codeSystemCategoryId}" />
						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}
						</td>
						<td>${record.entity_name}</td>
						<td><input type="text" name="code" id="code" maxlength="100"
							value="${record.code}" style="width: 100px;"
							<c:if test="${codeSystemsId == ''}"> disabled</c:if> /></td>
						<td><input type="text" name="code_description"
							id="code_description" maxlength="200"
							value="${record.code_description}" style="width: 200px;"
							<c:if test="${codeSystemsId == ''}"> disabled</c:if> /></td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div class="screenActions">
			<button type="submit" accesskey="S" onclick="validate();" <c:if test="${codeSystemsId == ''}"> disabled</c:if> >
				<b><u>S</u></b>ave
			</button>
			|
			<a href="${cpath}/master/codesets/defaultcodesets.htm">Default Code Sets</a>
			|
			<a href="${cpath}/bulkUploadDownload.htm">Group Update</a>
		</div>
	</form>
</body>
</html>