<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
<title>Items List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="masters/storesitemmaster.js" />
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

<script>
	var itemList = ${medicineNamesListJSON};
	var manfList = ${manfacturers};
	var itemDetailsList =${medicineListJSON};
 </script>
 <script type="text/javascript">
 	function changeStatus(obj) {
		document.exportForm.code_status.value = obj.value;
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

<h1>Stores Item Master</h1>
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
						<div id="itemcontainer" style = "width:35em"></div>
						<input type="hidden" name="medicine_name@op" value="ilike" />
					</div>
				</div>
	    	</div>
	  	</div>
	  </div>
	  <div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
	  	<table class="searchFormTable">
				<tr>
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
							<insta:selectdb name="category_name" values="${paramValues['category']}" dummyvalue="...Select..."
								table="store_category_master" displaycol="category" valuecol="category" orderby="category" onchange="autoItem();"/>
					    </div>
					</td>
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
		<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
			    <insta:sortablecolumn name="medicine_name" title="Item Name" />
				<th>Manufacturer</th>
				<th>Form</th>
				<th>Package Size</th>
				<th>Package Type</th>
				<th>Category</th>
				<th>Unit UOM</th>
				<th>Value</th>
				<th>Strength</th>
			</tr>
            <c:forEach var="item" items="${itemList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{medicine_id:'${item.medicine_id }'},
						[true,${noOfStoreRatePlans > 0}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>

					<td>
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
					<td><c:out value="${item.manf_name}"/></td>
					<td><insta:truncLabel value="${item.item_form_name}" length="15"/></td>
					<td><c:out value="${item.issue_base_unit}"/></td>
					<td><c:out value="${item.package_type}"/></td>
					<td><c:out value="${item.category_name}"/></td>
					<td>${item.issue_units}</td>
					<td>${valueDisplay[item.value]}</td>
					<td><insta:truncLabel value="${item.item_strength}" length="15"/></td>
				</tr>
			</c:forEach>
		</table>

		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

    </div>

    <div class="screenActions">
		<a href="${cpath }/master/StoresItemMaster.do?_method=add">Add New Item </a>
	</div>
	<div class="legend" style="display: ${hasResults ? 'block' : 'none' }">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"> </div>
		<div class="flagText">Active</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"> </div>
		<div class="flagText">Inactive</div>
	</div>
	</form>

	<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
   	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
			<div class="clrboth"></div>
		</div>
		<table class="search">
			<tr style="height:5px;"><td>&nbsp;</td></tr>
			<tr>
				<th>Export/Import Item Details</th>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td valign="top">
					<form name="codeTypeForm">
						<table>
							<tr>
								<td align="left">Code Type:</td>
								<td style="width:700px;">
									<select name="code_type" id="code_type" class="dropdown" onchange="changeCodeType(this)">
										<c:forEach var="codetypes" items="${codeTypes}">
											<option value="${codetypes.map.code_type}">${codetypes.map.code_type}</option>
										</c:forEach>
									</select>
								</td>
							</tr>
							<tr>
								<td align="left">Status:</td>
								<td style="width:700px;">
									<select name="code_type_status" id="code_type_status" class="dropdown" onchange="changeStatus(this)">
										<option value="All">---All---</option>
										<option value="A">Active</option>
										<option value="I">In Active</option>
									</select>
								</td>
							</tr>
						</table>
					</form>
				</td>
			</tr>
			<tr style="height:5px;"><td>&nbsp;</td></tr>
			<tr>
				<td valign="top">
					<table class="search">
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportForm" action="StoresItemMaster.do" method="GET"
									style="padding:0; margin:0">
									<div style="float: left">
										<input type="hidden" name="_method" value="exportMaster">
										<input type="hidden" name="code_type" value="code_type">
										<input type="hidden" name="code_status" id="code_status" value=""/>
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
								<form name="importForm" action="StoresItemMaster.do" method="POST"
									enctype="multipart/form-data">
									<div style="float: left">
									<input type="hidden" name="_method" value="importMaster">
									<input type="hidden" name="code_type" value="code_type">
									<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<button type="button" onclick="doValidate('importForm')">Upload</button>
									</div>
									<div style="float: left;white-space: normal">
										<img class="imgHelpText"
										src="${cpath}/images/help.png"
										title="Note:While updating Item-wise Reorder Levels please ensure that Danger Level Qty < Min Level Qty < Reorder Level Qty < Max Level Qty."/>
									</div>
									
								</form>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr style="height:5px;"><td>&nbsp;</td></tr>
			<tr>
				<th>Export/Import Health Authority Code Type</th>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportitemhealthcodetypeform" action="StoresItemMaster.do" method="GET"
										style="padding:0; margin:0">
									<div style="float: left">
										<input type="hidden" name="_method" value="exportItemHealthCodeTypeDetailsToXls">
										<button type="submit">
										<b><u>D</u></b>ownload</button>
									</div>
									<div style="float: left;white-space: normal">
										<img class="imgHelpText"
											 src="${cpath}/images/help.png"
											 title="Note: The export gives a CVS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
										</div>
									</div>
								</form>
							</td>
						</tr>
						<tr>
							<td>Import:</td>
							<td>
								<form name="uploaditemhealthcodetypeform" action="StoresItemMaster.do" method="POST"
										enctype="multipart/form-data" style="padding:0; margin:0">
									<input type="hidden" name="_method" value="importItemHealthCodeTypeDetailsFromXls">
									<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<button type="button"  onclick="return doUpload('uploaditemhealthcodetypeform')" >
									<b><u>U</u></b>pload</button>
								</form>
							</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr style="height:5px;"><td>&nbsp;</td></tr>
			<tr>
				<th>Export/Import Item Code Details</th>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportitemcodeform" action="StoresItemMaster.do" method="GET"
										style="padding:0; margin:0">
									<div style="float: left">
										<input type="hidden" name="_method" value="exportItemCodeDetailsToXls">
										<button type="submit" accesskey="E">
										<b><u>D</u></b>ownload</button>
									</div>
									<div style="float: left;white-space: normal">
										<img class="imgHelpText"
											 src="${cpath}/images/help.png"
											 title="Note: The export gives a CVS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
										</div>
									</div>
								</form>
							</td>
						</tr>
						<tr>
							<td>Import:</td>
							<td>
								<form name="uploaditemcodeform" action="StoresItemMaster.do" method="POST"
										enctype="multipart/form-data" style="padding:0; margin:0">
									<input type="hidden" name="_method" value="importItemCodeDetailsFromXls">
									<input type="file" name="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<button type="button" accesskey="F" onclick="return doUpload('uploaditemcodeform')" >
									<b><u>U</u></b>pload</button>
								</form>
							</td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</div>
	</div>

	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
	</script>
</body>
</html>
