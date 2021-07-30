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
<title>Diag Outsource Master-Insta HMS</title>
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
		EditTest: {
			title: "Edit Test",
			imageSrc: "icons/Edit.png",
			href: 'pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=showOuthouseTestDetails',
			onclick: null,
			description: "Edit or Update Test details"
			},		
	};
	function init()
	{
		createToolbar(toolbar);
		autoCompleteTests();
	}
 	var testNames = ${testNames};
	function autoCompleteTests() {
		var datasource = new YAHOO.util.LocalDataSource({result: testNames});
		datasource.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
		datasource.responseSchema = {
			resultsList: 'result',
			fields : [
						{key: "TEST_NAME"},
						{key : "TEST_ID"}
					]
		};
		var autoComp = new YAHOO.widget.AutoComplete('test_name','testContainer', datasource);
		autoComp.prehighlightClassName = "yui-ac-prehighlight";
		autoComp.typeAhead = true;
		autoComp.allowBrowserAutocomplete = false;
		autoComp.minQueryLength = 0;
		autoComp.maxResultsDisplayed = 20;
		autoComp.autoHighlight = false;
		autoComp.forceSelection = false;
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

	<h1>Diag Outsource Master</h1>

	<insta:feedback-panel/>

	<form name="OutHouseSearchForm" method="GET">

		<input type="hidden" name="_method" value="getDetails"/>
		<input type="hidden" name="_searchMethod" value="getDetails"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search form="OutHouseSearchForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Test Name</div>
			      <div class="sboFieldInput">
				    <input type="text" id="test_name" name="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}" style="width: 150px;"/>
				    <input type="hidden" name="test_name@op" value="ilike" />
				    <div id="testContainer" style="width: 235px"></div>
			    </div>
			</div>
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
			<div class="sboField">
				<div class="sboFieldLabel">Source Center Name</div>
				<div class="sboFieldInput">
					<select name="source_center_id" id="source_center_id" class="dropdown">
						<option value="">-- All --</option>
						<c:forEach items="${centers}" var="center">
							<option value="${center.map.center_id}" ${param['source_center_id'] == center.map.center_id ? 'selected' : ''}>
								${center.map.center_name}
							</option>
						</c:forEach>
					</select>
					<input type="hidden" name="source_center_id@cast" value="y"/>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>					
					<td>
						<div class="sfLabel">Test-Outsource Status</div>
						<div class="sfField">
							<insta:checkgroup name="test_status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.test_status}"/>
							<input type="hidden" name="test_status@op" value="in" />
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
			<table class="resultList" width="100%" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<insta:sortablecolumn name="test_name" title="Test Name"/>
					<th>Source Center Name</th>
					<th>Outsource Name - (Charge)</th>					
				</tr>
				<c:set var="stat" value="${record.status eq 'A'? 'Active':'Inactive'}"/>

				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<c:set var="isEdit" value="${not empty record.test_id}"/>
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',{
						testId:'${record.test_id}',testName:'${record.test_name}'},[true, true, true])"
							id="toolbarRow${st.index}">
						<td>
							${record.test_name}
						</td>
						<td colspan="2">&nbsp;</td>
					</tr>
					<c:set var="outsourceDetailsList" value="${outsourceDetails[record.test_id]}"/>
					<c:if test="${not empty outsourceDetailsList}">
						<c:forEach var="centerList" items="${outsourceDetailsList}">
							<tr>
								<td class="indent" colspan="1">&nbsp;</td>
								<td class="subResult" valign="top">${ifn:cleanHtml(centerList[0].map.center_name)}</td>
							
								<td valign="top" class="subResult">
									<c:forEach var="outsource" items="${centerList}">
										<c:set var="flagColor">
											<c:choose>
												<c:when test="${outsource.map.test_status eq 'I'}">grey</c:when>											
												<c:otherwise>
													empty
												</c:otherwise>
											</c:choose>
										</c:set>
											<img src="${cpath}/images/${flagColor}_flag.gif"/>
											${outsource.map.outsource_name} -&nbsp;&nbsp;(${outsource.map.charge})
										</br>
									</c:forEach>
								</td>
							</tr>
						</c:forEach>						
					</c:if>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getDetails'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
		</div>


		<div class="screenActions" style="float:left">
			<a href="${cpath}/pages/masters/hosp/diagnostics/OutHouseMaster.do?_method=addNewTestToOutHouse">Add New Test</a>&nbsp;
		</div>
	</form>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}">
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive Diag Outsource Test</div>
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
				<th>Export/Import Charges</th>
			</tr>
			<tr>
				<td>
					<table>
						<tr>
							<td>Export: </td>
							<td>
								<form name="exportform" action="OutHouseMaster.do" method="GET"
										style="padding:0; margin:0">
									<div style="float: left">
										<input type="hidden" name="_method" value="exportCharges">
										<button type="submit" accesskey="D"><b><u>D</u></b>ownload</button>
									</div>
									<div style="float: left;white-space: normal">
										<img class="imgHelpText"
											 src="${cpath}/images/help.png"
											 title="Note: The export gives a xls file (comma separated values),which can be edited in a spreadsheet .After editing and saving,the file can be imported back,and the new charges will be updated.Note that this must be done for one Rate Sheet at a time."/>
										</div>
									</div>
								</form>
							</td>
						</tr>
						<tr>
							<td>Import:</td>
							<td>
								<form name="importChargesform" action="OutHouseMaster.do" method="POST"
										enctype="multipart/form-data" style="padding:0; margin:0">
									<input type="hidden" name="_method" value="importCharges">
									<input type="hidden" name="org_id" value="">
									<input type="file" name="uploadChargeFile" id="uploadChargeFile" accept="<insta:ltext key="upload.accept.master"/>"/>
									<button type="button" accesskey="U"
										onclick="return doUpload('importChargesform');"><b><u>U</u></b>pload</button>
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
