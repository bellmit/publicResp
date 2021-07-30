<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Insurance Company TPA/Sponsor List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	var companyList  = ${insCompList};
	var tpaList      = ${tpaList};

	function deleteTPAs() {
		if(!checkDeleted()){
			alert("Please check any TPA/Sponsor to delete");
			return false;
		}
		document.CompanyTPAForm._method.value = "delete";
		document.CompanyTPAForm.submit();
	}

	function selectOrUnselectAll() {
		var check = document.CompanyTPAForm.deleteAll.checked;
		var deleteElmts = document.getElementsByName("_deleteTPA");
		var companyElmts = document.getElementsByName("_insCompany");
		var tpaElmts = document.getElementsByName("_tpa");
		for(var i=0;i<deleteElmts.length;i++) {
			deleteElmts[i].checked = check;
			if (check) {
				companyElmts[i].value = deleteElmts[i].value.split(":::")[0];
				tpaElmts[i].value = deleteElmts[i].value.split(":::")[1];
			}else {
				companyElmts[i].value = '';
				tpaElmts[i].value = '';
			}
		}
	}

	function onDeleteCheck(deleteElmt, index) {
		var companyElmt = document.getElementById("_insCompany"+index);
		var tpaElmt     = document.getElementById("_tpa"+index);
		if (deleteElmt.checked) {
			companyElmt.value = deleteElmt.value.split(":::")[0];
			tpaElmt.value = deleteElmt.value.split(":::")[1];
		}else {
			companyElmt.value = '';
			tpaElmt.value = '';
		}
	}

	function checkDeleted() {
		var deleteElmts = document.getElementsByName("_deleteTPA");
		for(var i=0;i<deleteElmts.length;i++) {
			if(deleteElmts[i].checked) return true;
		}
		return false;
	}

</script>
</head>
<body onload="initArrays()">
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<h1>Insurance Company TPA/Sponsor Master</h1>
<insta:feedback-panel/>
<form method="GET" name="CompanyTPAForm">

<input type="hidden" name="_method" value="list"/>
<input type="hidden" name="_searchMethod" value="list"/>
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

<insta:search-lessoptions form="CompanyTPAForm" >
	<div class="searchBasicOpts" >
		<div class="sboField">
			<div class="sboFieldLabel">Insurance Company:</div>
			<div class="sboFieldInput">
			<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
				table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="-- Select --" onchange="onChangeInsuranceCompany()" orderby="insurance_co_name"/>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldlabel">TPA/Sponsor:</div>
			<div class="sboFieldInput">
				<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" filtercol="sponsor_type" filtervalue="I" value="${param.tpa_id}"
				table="tpa_master" valuecol="tpa_id" dummyvalue="-- Select --" onchange="onChangeTPA()" orderby="tpa_name"/>
			</div>
		</div>
	</div>
</insta:search-lessoptions>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="resultList">
	<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<th>Select <input type="checkbox" name="deleteAll" onclick="return selectOrUnselectAll()"> All</th>
			<insta:sortablecolumn name="insurance_co_id" title="Insurance Company"/>
			<insta:sortablecolumn name="tpa_id" title="TPA/Sponsor"/>
		</tr>
		<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">

			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">

				<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
				<td align="center">
					<input type="checkbox" name="_deleteTPA" value="${record.insurance_co_id}:::${record.tpa_id}"
						onclick="onDeleteCheck(this, ${st.index}); ">
					<input type="hidden" name="_insCompany"  id="_insCompany${st.index}" >
					<input type="hidden" name="_tpa" id="_tpa${st.index}">
				</td>
				<td>
					${record.insurance_co_name}
				</td>
				<td>
					${record.tpa_name}
				</td>
			</tr>
		</c:forEach>
	</table>
</div>

<c:if test="${empty pagedList.dtoList}"> <insta:noresults hasResults="${hasResults}"/> </c:if>

<c:url value="InsuranceCompanyTPAMaster.do" var="url">
	<c:param name="_method" value="add" />
</c:url>

<div class="screenActions">
	<button type="button" name="deletBtn" accesskey="D" class="button" onclick="return deleteTPAs();">
	<label><u><b>D</b></u>elete</label></button>&nbsp;
	|
	<a href="<c:out value='${url}'/>">Add</a>
</div>

</form>
</body>
</html>
