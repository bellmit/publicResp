<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
	<title>Pharmacy Supplier List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="stores/supp_list.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	
	<script>
		var suppliers = ${suppliers};
		var suppliersCenterapplicable = ${maxCenters > 1 ? 'true' : 'false'};
	</script>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="suppList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty suppList}"/>
<c:set var="methodToUse" value="${(empty method) ? method : '_method'}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>
<body onload="init(); showFilterActive(document.suppListSearchForm)">

<h1>Suppliers List</h1>

<insta:feedback-panel/>

<form name="suppListSearchForm" method="GET">
	<input type="hidden" name="_method" value="getSupplierDashBoard">
	<input type="hidden" name="_searchMethod" value="getSupplierDashBoard"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="suppListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Supplier</div>
				<div class="sboFieldInput">
					<div id="supplier_name_wrapper" style="width:350px">
						<input type="text" name="supplier_name" id="supplier_name" value="${ifn:cleanHtmlAttribute(param.supplier_name)}"/>
						<div id="supplier_name_dropdown"></div>
						<input type="hidden" name="supplier_name@op" value="ilike"/>
					</div>
				</div>
	    	</div>
	  	</div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues.status}"
							opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
				</tr>
		</table>
	  </div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <insta:sortablecolumn name="supplier_name" title="Supplier"/>
				<th>Status</th>
			</tr>
            <c:forEach var="supp" items="${suppList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{supplier_id:'${supp.supplier_code }'},
						[true,true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td><c:out value="${supp.supplier_name}"/></td>
					<td>${statusDisplay[supp.status]}</td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'getSupplierDashBoard'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="screenActions">
		<a href="${cpath }/pages/masters/insta/stores/suppdetails.do?_method=getSupplierDetailsScreen">Add New Supplier</a>
	</div>

</form>
<%-- <insta:CsvDataHandler divid="upload1" action="suppdetails.do"/> --%>
<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
		<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;">
				<img src="${cpath}/images/down.png" />
			</div>
			<div class="clrboth"></div>
		</div>
		<table class="search">
			<tr style="height:5px;"><td>&nbsp;</td></tr>
			<tr>
				<td>Export: </td>
				<td>
					<form name="exportForm" action="${action}" method="GET"
						style="padding:0; margin:0">
						<div style="float: left">
							<input type="hidden" name="_method" value="exportMaster">
							<button type="submit">Download</button>
						</div>
						<div style="float: left;white-space: normal">
							<img class="imgHelpText"
							src="${cpath}/images/help.png"
							title="Note: The export gives a xls file which can be edited in a spreadsheet. After editing and saving, the file can be imported back, and the new changes will be updated."/>
						</div>
					</form>
				</td>
			</tr>

			<tr>
				<td>Import:</td>
				<td>
					<form name="importForm" action="suppdetails.do" method="POST"
						enctype="multipart/form-data">
						<input type="hidden" name="_method" value="importMaster">
						<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
						<button type="button" onclick="doValidate('importForm')">Upload</button>
					</form>
				</td>
			</tr>
		</table>
	</div>
</div>

<script>
	var suppliers = ${suppliers};
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
</script>
</body>
</html>

