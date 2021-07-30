<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Dynamic Package</title>
	<insta:link type="js" file="master/DynaPackage/DynaPackage.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="dynaPackageList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="createToolbar(toolBar); selectAllBedTypes(); loadDynapackageNamesList();">

<form name="searchform">
	<h1>Dynamic Packages</h1>
	<insta:feedback-panel />
	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Dynamic Package</div>
				<div class="sboFieldInput">
					<input type="text" name="dyna_package_name" id="dyna_package_name" style="width: 210px;" value="${ifn:cleanHtmlAttribute(param.dyna_package_name)}"/>
					<div style="width: 225px" name="dynanamesContainer" id="dynanamesContainer"></div>
				</div>
			</div>
			<div class="sboField" style="padding-left: 65px">
				<div class="sboFieldLabel">Rate Sheet</div>
				<div class="sboFieldInput">
					<insta:selectdb name="org_id" id="org_id" value="${org_id}"
						table="organization_details" valuecol="org_id"
						orderby="org_name" displaycol="org_name"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y" />
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Items</div>
						<div class="sfField" style="width:190px">
							<insta:checkgroup name="applicable" selValues="${paramValues.applicable}"
									opvalues="true,false" optexts="Included Only,Excluded Only"/>
							<input type="hidden" name="applicable@op" value="in"/>
							<input type="hidden" name="applicable@type" value="boolean" />
						</div>
					</td>
					<td class="last">
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
								selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
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
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px;">
					<input type="checkbox" name="allPagePackages" disabled onclick="selectAllPagePackages()"/></th>
				<th>Package Name</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden;text-align:right;">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>

			<c:forEach var="dynaPackageDetails" items="${dynaPackageList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{dyna_package_id: '${dynaPackageDetails.dyna_package_id}', org_id: '${ifn:cleanJavaScript(org_id)}'}, '');" >
					<td>
						<input type="checkbox" name="selectPackage" disabled value="${dynaPackageDetails.dyna_package_id}">
					</td>
					<td>
						<c:if test="${dynaPackageDetails.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${dynaPackageDetails.status eq 'A' && dynaPackageDetails.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${dynaPackageDetails.status eq 'A' && dynaPackageDetails.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${dynaPackageDetails.dyna_package_name }
					</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" >${ifn:afmt(charges[dynaPackageDetails.dyna_package_id][bed].map['charge'])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>

<c:url var="addUrl" value="DynaPackage.do">
	<c:param name="_method" value="add"></c:param>
</c:url>
<table class="formtable" width="100%">
	<tr>
		<td>
			<br/>
			<div id="content">
				<h1>
					DynaPackage Creation Job status
				</h1>
				<div class="resultList" >
					<table class="resultList" id="schedulerTable">
					<tr onmouseover="hideToolBar();">
						<th>ID</th>
						<th>DynaPackage ID</th>
						<th>Status</th>
						<th>Error</th>
					</tr>
					<c:forEach var="record" items="${masterCronJobDetails}">
						<tr>
							<td>${record.id}</td>
							<td>${record.entity_id}</td>
							<td id="entity_status_${record.entity_id}">${record.status == 'P'? 'Processing': record.status == 'S'? 'Success': 'Failed'}</td>
							<td id="error_status_${record.entity_id}">${record.error_message}</td>
						</tr>
					</c:forEach>
					</table>
				</div>
				<c:if test="${empty masterCronJobDetails}">
					<insta:noresults hasResults="${not empty masterCronJobDetails}"/>
				</c:if>
			</div>
		</td>
	</tr>
</table>
<table class="screenActions" width="100%">
	<tr>
		<td><a href="<c:out value='${addUrl}'/>">Add New Package</a></td>
	   	<td  align="right">
			<img src='${cpath}/images/purple_flag.gif'>
				Excluded&nbsp;
			<img src='${cpath}/images/grey_flag.gif'>
				Inactive
	  	</td>
	</tr>
</table>
	<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
			</div>

			<table>
			<tr>
				<td valign="top">
					<form name="updateform" action="DynaPackage.do" method="POST">
						<input type="hidden" name="_method" value="groupUpdate">
						<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}">

						<div style="display:none" id="PackageListInnerHtml">
							<%-- this holds the hidden inputs for the list of selected packages --%>
						</div>

						<table class="search">
							<tr>
								<th>Select Packages</th>
								<th>Select Bed Types</th>
								<th>
									<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
								</th>
							</tr>

							<tr>
								<td>
									<input type="radio" checked name="allPackages" onclick="onChangeAllPackages()" value="yes">
									All Packages <br>
									<input type="radio" name="allPackages" onclick="onChangeAllPackages()" value="no">
									Selected Packages
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
												<select name="round" style="width: 5em" class="dropdown">
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
						<th>Export/Import Package Details</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportpackageform" action="DynaPackage.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportPackageDetailsToXls">
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
										<form name="uploadpackageform" action="DynaPkgUpload.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="_method" value="importDynaPkgDetailsFromXls">
											<input type="hidden" name="orgId" value="" />
											<input type="file" name="xlsDetailsForm" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="F" onclick="return doUpload('uploadpackageform')" >
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
											<form name="exportform" action="DynaPackage.do" method="GET"
													style="padding:0; margin:0">
												<div style="float: left;">
													<input type="hidden" name="_method" value="exportChargesToXls">
													<input type="hidden" name="org_id" value="">
													<button type="submit" accesskey="D" onclick="return doExport();"><b><u>D</u></b>ownload</button>
												</div>
												<div style="float: left;white-space: normal">
													<img class="imgHelpText"
														 src="${cpath}/images/help.png"
														 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated.Note that this must be done for one Rate Sheet at a time."/>
												</div>
											</form>
										</td>
									</tr>

									<tr>
										<td>Import:</td>
										<td>
											<form name="uploadform" action="DynaPkgUpload.do" method="POST"
													enctype="multipart/form-data" style="padding:0; margin:0">
												<input type="hidden" name="_method" value="importChargesFromXls">
												<input type="hidden" name="org_id" value="">
												<input type="file" name="xlsChargesForm" id="xlsChargesForm" accept="<insta:ltext key="upload.accept.master"/>"/>
												<button type="button" accesskey="U"
													onclick="return validateImportChargesFile(this, 'xlsChargesForm', 'org_id');"><b><u>U</u></b>pload</button>
											</form>
										</td>
									</tr>
								</table>
							</td>
						</tr>

						<tr>
							<th>Export/Import Category Limits</th>
						</tr>
						<tr>
							<td>
								<table>
									<tr>
										<td>Export: </td>
										<td>
											<form name="exportlimits" action="DynaPackage.do" method="GET"
													style="padding:0; margin:0">
												<div style="float: left;">
													<input type="hidden" name="org_id" value="">
													<input type="hidden" name="_method" value="exportLimitsToCsv">
													<button type="submit" onclick="return doExportLimits();">Download</button>
												</div>
												<div style="float: left;white-space: normal">
													<img class="imgHelpText"
														 src="${cpath}/images/help.png"
														 title="Note: The export gives a CSV file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new changes will be updated."/>
												</div>
											</form>
										</td>
									</tr>

									<tr>
										<td>Import:</td>
										<td>
											<form name="importlimits" action="DynaPkgUpload.do" method="POST"
													enctype="multipart/form-data" style="padding:0; margin:0">
												<input type="hidden" name="_method" value="importLimitsFromCsv">
												<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(org_id)}"/>
												<input type="file" name="csvLimitsFile" accept="<insta:ltext key="upload.accept.master"/>"/>
												<button type="button"
													onclick="return validateImportLimitsFile(this, 'csvLimitsFile');">Upload</button>
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
				<td colspan="3"  style="vertical-align: bottom;">
					<button type="button" accesskey="C" onclick="return doGroupUpdate()">
					Update <b><u>C</u></b>harges</button>
				</td>
			</tr>
		</table>
	</div>
	</div>
	<script>
		var cpath = '${cpath}';
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
		var dynapackNamesList = ${dynapackageNamesJSON};
	</script>

</body>
</html>
