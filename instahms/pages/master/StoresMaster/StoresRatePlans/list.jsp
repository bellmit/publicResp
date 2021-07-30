<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Store Tariffs - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<script type="text/javascript">
		var toolBar = {
			Edit : {
				title : "View/Edit",
				imageSrc : "icons/Edit.png",
				href : "/pages/master/StoresMaster/StoresRatePlans.do?_method=show",
				description : "View and/or Edit the contents of Rate Plan"
				},
			EditRates : {
				title : "Edit Rates",
				imageSrc : "icons/Edit.png",
				href : "/pages/master/StoresMaster/StoreItemRates.do?_method=list",
				description : "View and/or Edit Rates"
				}
		};

		function init() {
			createToolbar(toolBar);
			var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
			var codeType = document.getElementById("code_type").value;
			document.exportratesform.code_type.value = codeType;
			document.uploadratesform.code_type.value = codeType;
		}

		function validate() {
			if ( document.uploadratesform.xlsRatesFile.value == '') {
				alert("Select a file to upload");
				return false;
			}else {
				return true;
			}
		}

		function changeCodeType(obj) {
			document.exportratesform.code_type.value = obj.value;
			document.uploadratesform.code_type.value = obj.value;
		}
		
		function changeStoreTariff(obj) {
			document.exportratesform.store_rate_plan_id.value = obj.value;
			document.uploadratesform.store_rate_plan_id.value = obj.value;
		}
		
	</script>
</head>
<body onload="init()">

<c:set var="hasResults" value="${not empty list.dtoList ? 'true' : 'false'}"/>

<h1>Store Tariffs</h1>

<insta:feedback-panel/>

<form name="storeRatePlanForm" method="GET">

	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search-lessoptions form="storeRatePlanForm" >
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Tariffs Name</div>
					<div class="sboFieldInput">
						<input type="text" name="store_rate_plan_name" value="${ifn:cleanHtmlAttribute(param.store_rate_plan_name)}" />
						<input type="hidden" name="store_rate_plan_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">Status</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="ico"/>
					</div>
				</div>
			</div>
		</insta:search-lessoptions>

	<insta:paginate curPage="${list.pageNumber}" numPages="${list.numPages}" totalRecords="${list.totalRecords}"/>

	<div class="resultList" >
		<table class="resultList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<insta:sortablecolumn name="store_rate_plan_name" title="Tariffs Name"/>
			<th>Remarks</th>

		</tr>
		<c:forEach var="record" items="${list.dtoList}" varStatus="st">
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable', {store_rate_plan_id: '${record.store_rate_plan_id}'},'');">

				<td>${(list.pageNumber - 1) * list.pageSize + (st.index + 1)}</td>
				<td>
					<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
					<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
					${record.store_rate_plan_name}
				</td>
				<td><insta:truncLabel value="${record.remarks}" length="50"/></td>
			</tr>
		</c:forEach>
		</table>
	</div>

		<c:if test="${empty list.dtoList}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>

		<c:url var="Url" value="StoresRatePlans.do">
			<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float: left;">
			<a href="${Url}">Add New Store Tariffs</a>
		</div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive</div>
		</div>

</form>

<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Export/Import</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
			<div class="clrboth"></div>
		</div>

		<table class="search">
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
								<td align="left">Store Tariff:</td>
								<td style="width:700px;">
									<select name="code_type" id="code_type" class="dropdown" onchange="changeStoreTariff(this)">
										<c:forEach var="storeRatePlan" items="${storeRatePlans}">
											<option value="${storeRatePlan.map.store_rate_plan_id}">${storeRatePlan.map.store_rate_plan_name}</option>
										</c:forEach>
										<option value="all">(All)</option>
									</select>
								</td>
							</tr>
						</table>
					</form>
				</td>
			</tr>
			<tr style="height:5px;"><td>&nbsp;</td></tr>
			<tr>
				<th>Export/Import Item Rates</th>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td valign="top">
					<table class="search">
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportratesform" action="StoresRatePlans.do" method="GET"
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
										<input type="hidden" name="code_type" id="code_type" value="">
										<input type="hidden" name="store_rate_plan_id" id="store_rate_plan_id" value="${storeRatePlans[0].map.store_rate_plan_id}">
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
								<form name="uploadratesform" action="StoresRatePlans.do?_method=importRatesFromXls"
								method="POST" enctype="multipart/form-data" style="padding:0; margin:0">
									<input type="file" name="xlsRatesFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<input type="hidden" name="code_type" id="code_type" value="">
									<input type="hidden" name="store_rate_plan_id" id="store_rate_plan_id" value="${storeRatePlans[0].map.store_rate_plan_id}">
									<button type="submit" accesskey="F"  onclick="return validate();">
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

</body>
</html>
