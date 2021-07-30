<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
<title>Items Issue Rate List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<script>
	var itemList = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MEDICINE_NAMES_IN_MASTER)%>;
	var manfList = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.GET_MANFNAMES_IN_MASTER)%>;


	var toolbar = {
	View: {
		title: "View/Edit",
		imageSrc: "icons/Edit.png",
		href: '/master/StoreItemIssueRateMaster.do?_method=show',
		onclick: null,
		description: "View/Edit Issue Rate"
	},

};

function init() {
	createToolbar(toolbar);
}

function onDelete() {
	if ( !checkBoxesChecked('delete'))
		return false;
	document.itemListSearchForm._method.value = 'deleteRates';
	document.itemListSearchForm.submit();
}

function validate() {
	if ( document.uploadratesform.xlsRatesFile.value == '') {
		alert("Select a file to upload");
		return false;
	}else {
		return true;
	}
}
</script>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="itemList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty itemList}"/>
<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Active"/>
<c:set target="${statusDisplay}" property="I" value="Inactive"/>

<jsp:useBean id="valueDisplay" class="java.util.HashMap"/>
<c:set target="${valueDisplay}" property="M" value="Medium"/>
<c:set target="${valueDisplay}" property="H" value="High"/>
<c:set target="${valueDisplay}" property="L" value="Low"/>
<body onload="init(); showFilterActive(document.itemListSearchForm)">

<h1>Stores Item Issue Rate Master</h1>
<insta:feedback-panel/>

<form name="itemListSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>


<insta:search form="itemListSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
	  <div class="searchBasicOpts" >
	  	<div class="sboField">
			<div class="sboFieldLabel">Item</div>
			<div class="sboFieldInput">
				<div id="autoItem">
					<input type="text" name="medicine_name" id="item" value="${ifn:cleanHtmlAttribute(param.medicine_name)}" style = "width:35em"/>
					<input type="hidden" name="medicine_name@op" value="ilike" />
					<div id="itemcontainer" style = "width:35em"></div>
				</div>
			</div>
	  	</div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Generic</div>
						<div class="sfField">
							<div id="automanf">
								<input type="text" name="generic_name"  id="generic_name" value="${ifn:cleanHtmlAttribute(param.generic_name)}"/>
								<input type="hidden" name="generic_name@op" value="ilike" />
								<div id="manfcontainer"></div>
							</div>
					    </div>
					</td>
					<td>
						<div class="sfLabel">Manufacturer</div>
						<div class="sfField">
							<div id="automanf">
								<input type="text" name="manf_name"  id="manf_name" value="${ifn:cleanHtmlAttribute(param.manf_name)}"/>
								<input type="hidden" name="manf_name@op" value="ilike" />
								<div id="manfcontainer"></div>
							</div>
					    </div>
					</td>
					<td>
						<div class="sfLabel">Category</div>
						<div class="sfField">
							<insta:selectdb name="category_name" values="${paramValues['category_name']}" dummyvalue="...Select..."
								table="store_category_master" displaycol="category" valuecol="category" orderby="category" />
					    </div>
					</td>
					<td class="last">
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
				<th>
					<input type="checkbox" name="selectAll" onchange="checkOrUncheckAll('delete', this);"/>
			    	<insta:sortablecolumn name="medicine_name" title="Item Name" add_th="false"/>
			    </th>
			    <th>Generic</th>
				<th>Manufacturer</th>
				<th>Category</th>
				<th>Issue Rate Expression</th>
			</tr>
            <c:forEach var="item" items="${itemList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{medicine_id:'${item.medicine_id }'},
						[true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>
						<input type="checkbox" name="delete" value="${item.medicine_id}" />
						<c:choose>
							<c:when test="${item.status eq 'A'}">
								<img src="${cpath}/images/empty_flag.gif"/>
							</c:when>
						<c:otherwise>
								<img src="${cpath}/images/grey_flag.gif"/>
						</c:otherwise>
						</c:choose>
						<insta:truncLabel value="${item.medicine_name}" length="50"/>

					</td>
					<td><c:out value="${item.generic_name}" /></td>
					<td><c:out value="${item.manf_name}"/></td>
					<td><c:out value="${item.category_name}"/></td>
					<td><insta:truncLabel value="${item.issue_rate_expr}" length="50"/></td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>
    </form>

    <div class="screenActions">
    	<button type="button" name="delete" onclick="onDelete();">Delete</button>
		<a href="${cpath }/master/StoreItemIssueRateMaster.do?_method=add">Add Issue Rate </a>
	</div>
	<div class="legend" style="display: ${hasResults ? 'block' : 'none' }">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"> </div>
		<div class="flagText">Active</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"> </div>
		<div class="flagText">Inactive</div>
	</div>


	<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Export/Import</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
			<div class="clrboth"></div>
		</div>

		<table>
			<tr>
			<td valign="top">
				<table class="search" style="padding-left: 3em">
				   <tr>
						<th>Export/Import Item Rates</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportratesform" action="StoreItemIssueRateMaster.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<c:forEach var="p" items="${param}">
													<c:if test="${p.key != '_method' and p.key != 'status'}">
														<c:forEach items="${paramValues[p.key]}" var="value">
															<input type="hidden" name="${p.key}" value="${ifn:cleanHtmlAttribute(value)}"/>
														</c:forEach>
													</c:if>
												</c:forEach>
												<input type="hidden" name="_method" value="exportRatesToXls">

												<button type="submit" accesskey="E" >
												<b><u>D</u></b>ownload</button>
											</div>
											<div style="float: left;white-space: normal">
												<img class="imgHelpText"
													 src="${cpath}/images/help.png"
													 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>

											</div>
										</form>
									</td>
								</tr>
								<tr>
									<td>Import:</td>
									<td>
										<form name="uploadratesform" action="StoreItemIssueRateMaster.do?_method=importRatesFromXls"
										method="POST" enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="file" name="xlsRatesFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="submit" accesskey="F"  onclick="return validate();">
											<b><u>U</u></b>pload</button>
										</form>
									</td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>

			</tr>

		</table>
	</div>
	</div>

<script>
	var cpath = '${cpath}';
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
</script>
</body>
</html>
