<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%--
Note on forms: we have multiple forms on this page. The main reason is the requirement
for the Upload to be an ActionForm (convenient for file uploads), which is different
from the other actions (POST/ GET forms). Also, we cannot cross-nest tables/divs and forms
so we are forced to copy values from one form to the other when submitting.
--%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="widgets.js" />
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/testlist.js" />
<script type="text/javascript">
	// for special chars handing in test name in toolbar
  var tb_names = [];
</script>
<title>Diagnostic Tests List - Insta HMS</title>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:url var="ED" value="addtest.do">
	<c:param name="_method" value="getEditTest" />
</c:url>

<c:url var="EC" value="addtest.do">
	<c:param name="_method" value="editTestCharges" />
	<c:param name="orgId" value="${orgId}" />
</c:url>

<c:set var="testList" value="${pagedList.dtoList}"/>

<body onload="testAutoComplete(); init()" class="yui-skin-sam">

<form method="GET" name="searchform" onsubmit="return searchValidate()">
	<input type="hidden" name="_method" value="listTests">
	<input type="hidden" name="_searchMethod" value="listTests"/>

	<h1>Diagnostic Tests List</h1>
	<insta:feedback-panel/>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Test Name</div>
				<div class="sboFieldInput">
					<input type="text" name="test_name" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}" style="width: 140px"/>
					<input type="hidden" name="test_name@op" value="ilike" />
					<div id="testContainer" style="width: 220px"></div>
				</div>
			</div>
			<div></div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Plan Code</div>
				<div class="sboFieldInput">
					<input type="text" name="item_code" id="item_code" value="${ifn:cleanHtmlAttribute(param.item_code)}"/>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Sheet</div>
				<div class="sboFieldInput">
					<insta:selectdb name="org_id" id="org_id" value="${orgId}"
						table="organization_details" valuecol="org_id" displaycol="org_name"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"
						orderby="org_name"/>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Department</div>
							<div class="sfField">
							<insta:selectdb name="ddept_id" multiple="true" size="5"
								table="diagnostics_departments" valuecol="ddept_id" displaycol="ddept_name"	values="${paramValues.ddept_id}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Items</div>
						<div class="sfField">
							<insta:checkgroup name="applicable" selValues="${paramValues.applicable}"
									opvalues="true,false" optexts="Included Only,Excluded Only"/>
							<input type="hidden" name="applicable@op" value="in"/>
							<input type="hidden" name="applicable@type" value="boolean"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Service Sub Group</div>
						<div class="sfField">
							<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" orderby='service_sub_group_name'/>
							<input type="hidden" name="service_sub_group_id@op" value="in"/>
							<input type="hidden" name="service_sub_group_id@type" value="integer"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="eq" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Alias</div>
							<div class="sfField">
							<input type="text" name="alias_item_code" id="alias_item_code" value="${ifn:cleanHtmlAttribute(param.alias_item_code)}"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>

<form name="listform" action="dummy.do">
	<input type="hidden" name="_method" value="groupUpdate">
	<input type="hidden" name="orgId" value="">

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th style="padding-top: 0px;padding-bottom: 0px;">
				<input type="checkbox" name="allPageTests" disabled onclick="selectAllPageTests()"/></th>
				<insta:sortablecolumn name="test_name" title="Test Name"/>
				<insta:sortablecolumn name="item_code" title="Code"/>
				<insta:sortablecolumn name="ddept_name" title="Department"/>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>
			<c:set var="enableEditTest" value="${urlRightsMap.diag_edit_master_id == 'A'}"/>
			<c:set var="enableRefRanges" value="${urlRightsMap.mas_edit_result_ranges == 'A'}"/>
			<c:set var="enableAuditLog" value="${urlRightsMap.diagnosticTests_audit_log == 'A'}"/>
			<c:set var="enableeditTestTAT" value="${urlRightsMap.mas_test_tat_master == 'A'}"/>						
			<c:forEach var="test" items="${testList}" varStatus="st">
				<script>tb_names[${st.index}] = <insta:jsString value="${test.test_name}"/>;</script>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}" onclick="showToolbar(${st.index}, event, 'resultTable',
						{testid: '${test.test_id}', test_id: '${test.test_id}', orgId: '${ifn:cleanJavaScript(orgId)}',
						test_name: tb_names[${st.index}]}, [${enableEditTest},${enableEditTest},${enableAuditLog},${enableRefRanges && test.conduction_format eq 'V'},${enableeditTestTAT}]);" >
					<td>
						<input type="checkbox" name="selectTest" disabled value="${test.test_id}">
					</td>
					<td>
						<c:if test="${test.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${test.status eq 'A' && test.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${test.status eq 'A' && test.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'> </c:if>
						<c:out value="${test.test_name}"/>
					</td>
					<td>${test.item_code}</td>
					<td>${test.ddept_name}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" align="right">${ifn:afmt(testCharges[test.test_id][bed])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>

<table class="screenActions" width="100%">
	<tr>
		<td>
			<c:if test="${enableEditTest}">
				<a href="${cpath}/pages/masters/hosp/diagnostics/addtest.do?_method=getAddTest&orgId=${ifn:cleanURL(orgId)}">Add New Test</a>
				|
				<a href="${cpath}/master/DiagTemplate.do?_method=list">Report Templates</a> |
			</c:if>
			<c:if test="${urlRightsMap.diagnosticTests_audit_log != 'N'}"> 
				<a href="${cpath}/diagnosticTests/auditlog/AuditLogSearch.do?_method=getSearchScreen">Audit Log Search</a>
			</c:if>
		</td>
		<td align="right">
			<div class="legend" style="display: ${hasResults ? 'block' : 'none'}" >
				<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
				<div class="flagText">Excluded</div>
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Inactive</div>
			</div>
		</td>
	</tr>
</table>

<div id="CollapsiblePanel1" class="CollapsiblePanel">
	<c:if test="${enableEditTest}">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
			</div>

	<table>
		<tr>
			<td valign="top">
				<form name="updateform" action="addtest.do" method="POST">
					<input type="hidden" name="_method" value="groupUpdate">
					<input type="hidden" name="orgId" value="">

					<div style="display:none" id="testListInnerHtml">
						<%-- this holds the hidden inputs for the list of selected tests --%>
					</div>
					<table class="search">
						<tr>
							<th>Select Tests</th>
							<th>Select Bed Types</th>
							<th>
								<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
								<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
								<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
							</th>
						</tr>

						<tr>
							<td>
								<input type="radio" checked name="allTests" onclick="onChangeAllTests()" value="yes">
								All tests <br>
								<input type="radio" name="allTests" onclick="onChangeAllTests()" value="no">
								Selected tests
							</td>

							<td style="padding-left: 1em">
								<select multiple="true" size="5" name="selectBedType">
									<c:forEach items="${bedTypes}" var="bed">
										<option value="${bed}" selected onclick="deselectAllBedTypes();">${bed}</option>
									</c:forEach>
								</select>
								<br/>
								<input type="checkbox" name="allBedTypes" checked value="yes"
										onclick="selectAllBedTypes()"/>Select All
							</td>

							<td valign="top" style="padding-left: 1em">
								<table class="formtable">
									<tr>
										<td>Change type: </td>
										<td>
											<insta:selectoptions name="incType" opvalues="+,-" optexts="Increase,Decrease"
													value="+" style="width: 8em"/>
										</td>
									</tr>
									<tr>
										<td>Amount type: </td>
										<td>
											<insta:selectoptions name="amtType" opvalues="%,A" optexts="Percent,Absolute"
													value="%" style="width: 8em"/>
										</td>
									</tr>
									<tr>
										<td>Amount: </td>
										<td><input type="text" class="validate-number" name="amount"></td>
									</tr>
									<tr>
										<td>Round off: </td>
										<td>
											<select name="roundOff" style="width: 5em" class="dropdown">
												<option value="0">None</option>
												<option value="1">1</option>
												<option value="5">5</option>
												<option value="10">10</option>
												<option value="25">25</option>
												<option value="50">50</option>
												<option value="100">100</option>
											</select>
										</td>
									</tr>

								</table>
							</td>
						</tr>
					</table>
				</form>
			</td>

			<td valign="top">
				<table class="search" style="padding-left: 3em">

				   <tr>
						<th>Export/Import Tests Details</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exporttestform" action="addtest.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportTestDetailsToXls">
												<button type="submit" accesskey="E">
												<b><u>D</u></b>ownload</button>
											</div>
											<div style="float: left;white-space: normal">
												<img class="imgHelpText"
													 src="${cpath}/images/help.png"
													 title="Note: The export gives a XLS file  which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
												</div>
											</div>
										</form>
									</td>
								</tr>
								<tr>
									<td>Import:</td>
									<td>
										<form name="uploadtestform" action="addtest.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="_method" value="importTestDetailsFromXls">
											<input type="file" name="xlsTestFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="F" onclick="return doUpload('uploadtestform')" >
											<b><u>U</u></b>pload</button>
										</form>
									</td>
								</tr>
							</table>
						</td>
					</tr>
					<tr>
						<th>Export/Import Charges</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportform" action="addtest.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportTestChargesCSV">
												<input type="hidden" name="orgId" value="">
												<button type="submit" accesskey="D" onclick="return doExport()">
												<b><u>D</u></b>ownload</button>
											</div>
											<div style="float: left;white-space: normal">
												<img class="imgHelpText"
													 src="${cpath}/images/help.png"
													 title="Note: The export gives a CSV file (comma separated values), which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new charges will be updated.Note that this must be done for one Rate Plan at a time."/>
												</div>
											</div>
										</form>
									</td>
								</tr>
								<tr>
									<td>Import:</td>
									<td>
										<form name="uploadchargesform" action="addtest.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="_method" value="importTestChargesXLS">
											<input type="hidden" name="org_id" value="">
											<input type="file" name="xlsChargeFile" id="xlsChargeFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="U"
												onclick="return validateImportChargesFile(this, 'xlsChargeFile', 'org_id');">
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
		<tr>
			<td colspan="3" align="left" style="vertical-align: bottom;">
				<button type="button" accesskey="C" onclick="return doGroupUpdate()">Update <b><u>C</u></b>harges</button>
			</td>
		</tr>
	</table>
	</c:if>
</div>

<script>
	var testNames = ${requestScope.testnames};
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
	var cpath = '${cpath}';
</script>

</body>
</html>
