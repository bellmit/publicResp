<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
<head>
	<title>Services-Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="masters/service.js" />
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="masters/charges_common.js" />
	<script type="text/javascript">
		// to handle special chars in services names for toolbar
		var tb_service_names = [];
	</script>
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<c:set var="serviceList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="initServiceAc(); selectAllBedTypes(); init()" class="yui-skin-sam">

<h1>Services</h1>
<insta:feedback-panel/>

<form action="ServiceMaster.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="list" />
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Service Name</div>
				<div class="sboFieldInput">
					<input type="text" id="service_name" name="service_name" value="${ifn:cleanHtmlAttribute(param.service_name)}" style="width: 140px"/>
					<input type="hidden" name="service_name@op" value="ilike" />
					<div style="width: 195px" id="serviceAcContainer"></div>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Plan Code</div>
				<div class="sboFieldInput">
					<input type="text" name="item_code" value="${ifn:cleanHtmlAttribute(param.item_code)}"/>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Sheet</div>
				<div class="sboFieldInput">
					<insta:selectdb name="org_id" id="org_id" value="${org_id}"
						table="organization_details" valuecol="org_id"
						displaycol="org_name" orderby="org_name"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Department</div>
							<div class="sfField">
							<insta:selectdb name="dept_name" multiple="true" size="5" table="services_departments" valuecol="department" displaycol="department"
									orderby="department" values="${paramValues.dept_name}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Items</div>
						<div class="sfField" style="width:190px">
							<insta:checkgroup name="applicable" selValues="${paramValues.applicable}"
									opvalues="true,false" optexts="Included Only,Excluded Only"/>
							<input type="hidden" name="applicable@op" value="in"/>
							<input type="hidden" name="applicable@type" value="boolean"/>
						</div>
					</td>
					<td>
							<div class="sfLabel">Treatment Code Type</div>
							<div class="sfField">
								<insta:selectdb name="code_type" table="mrd_supported_codes" valuecol="code_type"
								displaycol="code_type" dummyvalue="--Select--" filtervalue="Treatment"
								filtercol="code_category" value="${param.code_type}"/>
							</div>
					</td>
					<td>
							<div class="sfLabel">Service Sub Group</div>
							<div class="sfField">
								<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name"   />
								<input type="hidden" name="service_sub_group_id@type" value="integer" />
							</div>
					</td>
					<td class="last">
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
								<input type="hidden" name="status@op" value="in" />
						</div>
					</td>
					<td class="last">&nbsp;</td>
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
				<input type="checkbox" name="allPageServices" disabled onclick="selectAllPageServices()"/></th>
				<insta:sortablecolumn name="service_name" title="Service Name"/>
				<insta:sortablecolumn name="item_code" title="Code"/>
				<insta:sortablecolumn name="dept_name" title="Department"/>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>
			<c:forEach var="service" items="${serviceList}" varStatus="st">
			<script>tb_service_names[${st.index}] = <insta:jsString value = "${service.service_name}"/>;</script>
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				id="toolbarRow${st.index}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
				{service_id:'${service.service_id}', org_id:'${ifn:cleanJavaScript(org_id)}', service_name: tb_service_names[${st.index}]},
				'');" >
					<td>
						<input type="checkbox" name="selectService" disabled value="${service.service_id}">
					</td>
					<td>
						<c:if test="${service.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${service.status eq 'A' && service.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${service.status eq 'A' && service.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
							<insta:truncLabel value="${service.service_name}" length="50"/>
					</td>
					<td>${service.item_code}</td>
					<td>${service.dept_name}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" align="right">${ifn:afmt(charges[service.service_id][bed])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>

<br/>
<div id="content">
	<h1>
		Service Creation Job status
	</h1>
	<div class="resultList" >
		<table class="resultList" id="schedulerTable">
		<tr onmouseover="hideToolBar();">
			<th>ID</th>
			<th>Service ID</th>
			<th>Status</th>
			<th>Error</th>
			<th>Retry</th>
		</tr>
		<c:forEach var="record" items="${masterCronJobDetails}">
			<tr>
				<td>${record.id}</td>
				<td>${record.entity_id}</td>
				<td id="entity_status_${record.entity_id}">${record.status == 'P'? 'Processing': record.status == 'S'? 'Success': 'Failed'}</td>
				<td id="error_status_${record.entity_id}">${record.error_message}</td>
				<td id="retry_job_${record.entity_id}"><c:if test="${record.status == 'F'}">
					<button type="button" onclick="retryJobSchedule('${record.entity_id}');">
					<b><u>R</u></b>etry</button>
					</c:if></td>
			</tr>
		</c:forEach>
		</table>
	</div>

	<c:if test="${empty masterCronJobDetails}">
		<insta:noresults hasResults="${not empty masterCronJobDetails}"/>
	</c:if>
</div>

<table class="screenActions" width="100%">
	<tr>
		<td>
			<a href="${cpath}/master/ServiceMaster.do?_method=add">Add New Service</a>
			|
			<a href="${cpath}/master/ServiceTemplate.do?_method=list">Service Report Templates</a>
			<c:if test="${urlRightsMap.services_audit_log != 'N'}"> |
				<a href="${cpath}/services/auditlog/AuditLogSearch.do?_method=getSearchScreen">Audit Log Search</a>
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
					<form name="updateform" action="ServiceMaster.do" method="POST">
						<input type="hidden" name="_method" value="groupUpdate">
						<input type="hidden" name="org_id" value="">

						<div style="display:none" id="serviceListInnerHtml">
							<%-- this holds the hidden inputs for the list of selected services --%>
						</div>

						<table class="search">
							<tr>
								<th>Select Services</th>
								<th>Select Bed Types</th>
								<th>
									<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
									<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
									<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
								</th>
							</tr>

							<tr>
								<td>
									<input type="radio" checked name="allServices" onclick="onChangeAllServices()" value="yes">
									All Services <br>
									<input type="radio" name="allServices" onclick="onChangeAllServices()" value="no">
									Selected Services
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
											<td><input type="text" onkeypress="return enterNumOnlyANDdot(event);" name="amount"></td>
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
						<th>Export/Import Service Details</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportserviceform" action="ServiceMaster.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportServiceDetailsToXls">
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
										<form name="uploadserviceform" action="ServiceMasterUpload.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="method" value="importServiceDetailsFromXls">
											<input type="file" name="xlsServiceFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="F" onclick="return doUpload('uploadserviceform')" >
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
											<form name="exportform" action="ServiceMaster.do" method="GET"
													style="padding:0; margin:0">
												<div style="float: left;">
													<input type="hidden" name="_method" value="exportChargesToXls">
													<input type="hidden" name="org_id" value="">
													<button type="submit" accesskey="D" onclick="return doExport()"><b><u>D</u></b>ownload</button>
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
											<form name="uploadform" action="ServiceMasterUpload.do" method="POST"
													enctype="multipart/form-data" style="padding:0; margin:0">
												<input type="hidden" name="method" value="importChargesFromXls">
												<input type="hidden" name="org_id" value="">
												<input type="file" name="xlsServiceFile" id="xlsServiceFile" accept="<insta:ltext key="upload.accept.master"/>"/>
												<button type="button" accesskey="U"
													onclick="return validateImportChargesFile(this, 'xlsServiceFile', 'org_id');"><b><u>U</u></b>pload</button>
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
	var serviceNames = ${namesJSON};
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
</script>

</body>
</html>
