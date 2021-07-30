<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
<title>Package UOM List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

<script>

var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/PackageUOM.do?_method=show',
		onclick: null,
		description: "View/Edit Pkg UOM"
	},

};

function init() {
	createToolbar(toolbar);
}

/* function onDelete() {
	if ( !checkBoxesChecked('_delete'))
		return false;
	document.itemListSearchForm._method.value = 'deleteUOMs';
	document.itemListSearchForm.submit();
}

function changeFlagValue(index, checked) {
	document.getElementById('_deleteflag'+index).value = checked;
}

function setDeletedValue(checked) {
	var deleted = document.getElementsByName("_deleteflag");
	for (var i=0; i<deleted.length; i++) {
		deleted[i].value = checked;
	}
} */




</script>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="itemList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty itemList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>


<body onload="init();">

<h1>Package UOM Master</h1>
<insta:feedback-panel/>

<form name="itemListSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


	<insta:search-lessoptions form="itemListSearchForm" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Package UOM:</div>
				<div class="sboFieldInput">
					<input type="text" name="package_uom" value="${ifn:cleanHtmlAttribute(param.package_uom)}">
					<input type="hidden" name="package_uom@op" value="ico" />
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Unit UOM:</div>
				<div class="sboFieldInput">
					<input type="text" name="issue_uom" value="${ifn:cleanHtmlAttribute(param.issue_uom)}">
					<input type="hidden" name="issue_uom@op" value="ico" />
				</div>
			</div>
		</div>
	</insta:search-lessoptions>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">

			    <th>
			    <%--
			    	<input type="checkbox" name="_selectAll" onclick="setDeletedValue(this.checked);checkOrUncheckAll('_delete', this)"/>
			    --%>
			    	Package UOM
			    </th>
				<th>Unit UOM</th>
				<th>Pkg Size</th>
			</tr>
            <c:forEach var="item" items="${itemList}" varStatus="st">
            		<script>
					var packageUomStr${st.index};
					packageUomStr${st.index} = <insta:jsString value="${item.package_uom}"/>;
					var issueUomStr${st.index} = <insta:jsString value="${item.issue_uom}"/>;
				</script>

            	<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick='showToolbar(${st.index}, event, "resultTable",
						{package_uom: packageUomStr${st.index}, issue_uom: issueUomStr${st.index} },
						[true]);'
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>
						<%--
							<input type="hidden" name="_deleteOnPacakageUOM" id="_deleteOnPackageUOM${st.index}" value="${item.package_uom}">
							<input type="hidden" name="_deleteOnIsssueUOM" id="_deleteOnIsssueUOM${st.index}" value="${item.issue_uom}">
							<input type="hidden" name="_deleteflag" value="false" id="_deleteflag${st.index}"/>
							<input type="checkbox" name="_delete" value="${item.package_uom}" onclick="changeFlagValue('${st.index}', this.checked)"/>
						--%>
						<c:choose>
							<c:when test="${'A' eq 'A'}">
								<img src="${cpath}/images/empty_flag.gif"/>
							</c:when>
						<c:otherwise>
								<img src="${cpath}/images/grey_flag.gif"/>
						</c:otherwise>
						</c:choose>
						<insta:truncLabel value="${item.package_uom}" length="50"/>
					</td>
					<td><c:out value="${item.issue_uom}" /></td>
					<td><c:out value="${item.package_size}"/></td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>
    </form>

    <div class="screenActions">
    	<%--
    		<button type="button" name="delete" onclick="onDelete();">Delete</button>|
    	--%>
		<a href="${cpath }/master/PackageUOM.do?_method=add">Add Package UOM </a>
	</div>
	<div class="legend" style="display: ${hasResults ? 'block' : 'none' }">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"> </div>
		<div class="flagText">Active</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"> </div>
		<div class="flagText">Inactive</div>
	</div>

<insta:CsvDataHandler divid="upload1" action="PackageUOM.do"/>

<script>
	var cpath = '${cpath}';
</script>
</body>
</html>
