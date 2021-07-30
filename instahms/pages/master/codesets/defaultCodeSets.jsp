<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@page import="com.insta.hms.master.URLRoute"%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Default Code Sets - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="script" file="dashboardColors.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.CODE_SETS_PATH%>" />
<script type="text/javascript">
	function validate() {
		var codeSystemId = document.getElementById(${record.code_system_category_id}+'code_systems_id');
		if (codeSystemId.selectedIndex == 0) {
			alert("Select code system");
			document.getElementById('code_systems_id').focus();
			return false;
		}
		return true;
	}
	function setCode(codeSystemCategoryId) {
		var codeDesc = document.getElementById(codeSystemCategoryId+'_code_description');
		var codeDescValue = codeDesc.options[codeDesc.selectedIndex].value;
		var code = document.getElementById(codeSystemCategoryId+'_code');
		code.value = codeDescValue;
		
	}
	function setCodeDesc(codeSystemCategoryId) {
		var code = document.getElementById(codeSystemCategoryId+'_code');
		var codeValue = code.options[code.selectedIndex].value;
		var codeDesc = document.getElementById(codeSystemCategoryId+'_code_description');
		codeDesc.value = codeValue;
	}
</script>
</head>
<body onload="">
	<h1>Default Code Sets</h1>
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
	</form>

	<form name="defaultCodeSetsForm" method="POST" action="${cpath}/master/codesets/saveDefaultCodeSets.htm">
		<div class="resultList">
			<input type="hidden" id="code_system_category_id" name="code_system_category_id" value="${param.code_system_category_id}"/>
			<input type="hidden" id="code_system_id" name="code_system_id" value="${param.code_systems_id}"/>
			<c:if test="${codeSystemsLabel != ''}"><h1> Default codes - ${codeSystemsLabel}</h1></c:if>
			<table class="resultList" cellspacing="" cellpadding=""
				id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<insta:sortablecolumn name="code_system_category_label" title="Master Name" />
					<th>Code</th>
					<th>Code Description</th>
				</tr>
				<c:forEach var="record" items="${records}" varStatus="st">
					<tr>
							
						<input type="hidden" id="code_sys_cat_id" name="code_sys_cat_id"
							value="${record.code_system_category_id}" />
							
						<input type="hidden" id="code_sys_id"
							name="code_sys_id" value="${record.code_system_id}" />
							
						<td>${record.master_name}</td>
						<td>
							<select name="${record.code_system_category_id}_code" id="${record.code_system_category_id}_code"
								style="width:100px" class="dropdown" onchange="setCodeDesc(${record.code_system_category_id})">
          						<option value="">-- Select --</option>
         	 					<c:forEach items="${record.all_codes}" var="allCodes">
          							<option value="${allCodes.code}" ${record.code == allCodes.code ? 'selected' :''}>
          								${allCodes.code}
          							</option>
          						</c:forEach>
        					</select>
						</td>
						<td>
							<select name="${record.code_system_category_id}_code_description" id="${record.code_system_category_id}_code_description"
								 style="width:250px" class="dropdown" onchange="setCode(${record.code_system_category_id})">
          						<option value="">-- Select --</option>
         	 					<c:forEach items="${record.all_codes}" var="allCodes">
          							<option value="${allCodes.code}" ${record.code_description == allCodes.code_desc ? 'selected' :''}>
          								${allCodes.code_desc}
          							</option>
          						</c:forEach>
        					</select>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div class="screenActions">
			<button type="submit" accesskey="S" onclick="validate();" <c:if test="${codeSystemsId == ''}"> disabled</c:if> >
				<b><u>S</u></b>ave
			</button>
			|
			<a href="${cpath}/master/codesets/list.htm">Code Sets</a>
		</div>
	</form>
</body>
</html>