<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<%
	String msg = (String) request.getAttribute("msg");
	if (msg == null) {
		msg = "";
	}
%>
<html>
<head>
<title>Outsource Master-Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />


<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<c:set var="cpath">
	${pageContext.request.contextPath }
</c:set>
<c:set var="centralLabMod" value="${centralLabModule.map.activation_status}" />

<style type="text/css">

	.status_I{background-color: #E4C89C}
	.teststatus_I{background-color: #BDA680}

</style>

<script>
	var toolbar = {
		EditOutHouse:{
			title: "Edit Out House",
			imageSrc: "icons/Edit.png",
			href: 'pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=getOutHouseDetails',
			onclick: null,
			description: "Edit or Update Out House details"
			},
		EditInternalLab:{
			title: "Edit Internal Lab",
			imageSrc: "icons/Edit.png",
			href: 'pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=showInternalLab',
			onclick: null,
			description: "Edit or Update Internal Lab Details"
		}
	};
	function init()
	{
		createToolbar(toolbar);
	}

	function doUpload(formType) {
		if (formType == "importouthouseform") {
			var form = document.importouthouseform;
			if (form.uploadOutHouseDetailsFile.value == '') {
				alert("Please browse and select a file to upload");
				return false;
			}
		} else {
			var form = document.importChargesform;
			if (form.uploadChargeFile.value == '') {
				alert("Please browse and select a file to upload");
				return false;
			}
		}
		form.submit();
	}
</script>

</head>

<body onload="init();" class="yui-skin-sam">

	<c:set var="hasResults" value="${not empty pagedList.dtoList}"></c:set>

	<h1>Outsource Master</h1>

	<insta:feedback-panel/>

	<form name="OutHouseSearchForm" method="GET">

		<input type="hidden" name="_method" value="getOutHouseList"/>
		<input type="hidden" name="_searchMethod" value="getOutHouseList"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="OutHouseSearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField" style="padding-bottom:50px">
				<div class="sboFieldLabel">Outsource</div>
				<div class="sboFieldInput">
					<select name="outsource_name" multiple="true" size="4">
						<c:forEach var="record" items="${outHouseNames}">
							<option value="${record.OUTSOURCE_NAME}" ${param['outsource_name'] == record.OUTSOURCE_NAME ? 'selected' : ''}>
							   <insta:truncLabel value="${record.OUTSOURCE_NAME}" length="15"/>
							</option>
						</c:forEach>
					</select>
					<input type="hidden" name="outsource_name@op" value="ico" />
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">OutSource Type</div>
				<div class="sboFieldInput">
					<insta:checkgroup name="outsource_dest_type" opvalues="O,C" optexts="Outhouse,Internal Lab"
								selValues="${paramValues.outsource_dest_type}"/>
							<input type="hidden" name="outsource_dest_type@op" value="in" />
				</div>
			</div>

		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Outsource Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
							<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
					<td class="last">&nbsp;
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<insta:sortablecolumn name="outsource_name" title="Outsource Name"/>
				</tr>
				<c:set var="stat" value="${record.status eq 'A'? 'Active':'Inactive'}"/>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<c:set var="isOutHouse" value="${record.outsource_dest_type eq 'O' ||
														 record.outsource_dest_type eq 'IO'}"/>
					<c:set var="isInternalLab" value="${record.outsource_dest_type eq 'C'}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',{outsourceDestId:'${record.outsource_dest_id }',
						ohId:'${record.oh_id}',status:'${record.status}'}, [${isOutHouse},${isInternalLab}])"
							id="toolbarRow${st.index}">
						<td>
							<c:if test="${record.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							${record.outsource_name}</td>
					</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getOutHouseList'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>


		<div class="screenActions" style="float:left"><a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=addNewOutHouse">Add New Outsource</a>				
		<c:if test="${max_centers_inc_default > 1 && centralLabMod eq 'Y'}">
		| &nbsp;<a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=addNewInternalLab">Add New Internal Lab</a>
		</c:if>
		</div>
	</form>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive Out Source</div>
	</div>

	<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
   	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
			<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
			<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
			<div class="clrboth"></div>
		</div>
		<table class="search">
			<tr>
				<th>Export/Import Outsource Details</th>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportouthouseform" action="OutHouseMaster.do" method="GET"
										style="padding:0; margin:0">
									<div style="float: left">
										<input type="hidden" name="_method" value="exportOutHouseDetails">
										<button type="submit" accesskey="E">
										<b><u>D</u></b>ownload</button>
									</div>
									<div style="float: left;white-space: normal">
										<img class="imgHelpText"
											 src="${cpath}/images/help.png"
											 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
										</div>
									</div>
								</form>
							</td>
						</tr>
						<tr>
							<td>Import:</td>
							<td>
								<form name="importouthouseform" action="OutHouseMaster.do" method="POST"
										enctype="multipart/form-data" style="padding:0; margin:0">
									<input type="hidden" name="_method" value="importOutHouseDetails">
									<input type="file" name="uploadOutHouseDetailsFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<button type="button" accesskey="F" onclick="return doUpload('importouthouseform');" >
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
		var cpath = '${cpath}';
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
	</script>

	<%
		String heads[] = {"testName","outhouseName","charge"};
		String headNames[] = {"Test Name","OutHouse Name","Charge"};
		request.setAttribute("heads",heads);
		request.setAttribute("headNames",headNames);
	%>
</body>
</html>