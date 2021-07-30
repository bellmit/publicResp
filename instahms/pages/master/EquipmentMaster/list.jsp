<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>

<html>
<head>
	<title>Equipment - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<insta:link type="js" file="masters/addEquipmet.js" />
	<insta:link type="js" file="masters/charges_common.js" />
</head>

<c:set var="hasResults" value="${not empty pagedList.dtoList}" />
<c:set var="equipmentList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body onload="initEquipmentAc();selectAllBedTypes(); init();"class="yui-skin-sam">

<div class="pageHeader">Equipment Master</div>
<insta:feedback-panel/>

<form action="EquipmentMaster.do" method="GET" name="searchform">
	<input type="hidden" name="_method" value="list" />
	<input type="hidden" name="_searchMethod" value="list" />

	<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">Equipment Name</div>
				<div class="sboFieldInput">
					<input type="text" id="equipment_name" name="equipment_name" value="${ifn:cleanHtmlAttribute(param.equipment_name)}" style="width: 140px"/>
					<input type="hidden" name="equipment_name@op" value="ilike" />
						<div style="width: 195px" id="equipmentContainer"></div>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Rate Sheet</div>
				<div class="sboFieldInput">
					<insta:selectdb name="org_id" id="org_id" value="${org_id}"
						table="organization_details" valuecol="org_id"
						orderby="org_name" displaycol="org_name"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y" />
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">View Charges</div>
				<div class="sboFieldInput">
					<insta:selectoptions name="_chargeType" value="${chargeType}" opvalues="daily_charge,min_charge,incr_charge"
						optexts="Daily Charge ,Min Charge, Incr Charge"/>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Department</div>
							<div class="sfField">
								<insta:selectdb name="dept_id" multiple="true" size="5" table="department" valuecol="dept_id" displaycol="dept_name"
									orderby="dept_name" values="${paramValues.dept_id}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Service Sub Group</div>
						<div class="sfField">
								<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" value=""
								table="service_sub_groups" class="dropdown"   dummyvalue="-- Select --"
								valuecol="service_sub_group_id"  displaycol="service_sub_group_name" />

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
					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>

<form name="listform" action="dummy.do">
	<input type="hidden" name="_method" value="groupUpdate">
	<input type="hidden" name="org_id" value="">

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th style="padding-top: 0px;padding-bottom: 0px;">
					<input type="checkbox" name="allPageOperations" disabled onclick="selectAllPageEquipments()"/>
				</th>
				<insta:sortablecolumn name="equipment_name" title="Equipment Name"/>
				<insta:sortablecolumn name="dept_name" title="Department"/>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>
			<c:forEach var="equipment" items="${equipmentList}" varStatus="st">

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{equip_id: '${equipment.eq_id}', org_id: '${ifn:cleanJavaScript(org_id)}'}, '');" >
					<td>
						<input type="checkbox" name="selectEquipment" disabled value="${equipment.eq_id}">
					</td>
					<td>
						<c:if test="${equipment.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${equipment.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						${equipment.equipment_name}
					</td>
					<td>${equipment.dept_name}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" align="right">${ifn:afmt(charges[equipment.eq_id][bed].map[chargeType])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
</form>

<insta:noresults hasResults="${hasResults}"/>

<table class="screenActions">
	<tr>
		<td width="80%">
			<a href="${cpath}/master/EquipmentMaster.do?_method=add">Add New Equipment</a>
		</td>

	<td>&nbsp;</td>
	<div class="legend" style="display: ${hasResults ? 'block' : 'none'}" >
		<td width="20%" align="right">
			<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
			<div class="flagText">Inactive Equipments</div>
				</td>
	</div>

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
					<form name="updateform" action="EquipmentMaster.do" method="POST">
						<input type="hidden" name="_method" value="groupUpdate">
						<input type="hidden" name="org_id" value="">
						<input type="hidden" name="chargeType" value="">

						<div style="display:none" id="equipmentListInnerHtml">
							<%-- this holds the hidden inputs for the list of selected equipments --%>
						</div>

						<table class="search">
							<tr>
								<th>Select Equipments</th>
								<th>Select Bed Types</th>
								<th>
									<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
									<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
									<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
								</th>
							</tr>

							<tr>
								<td>
									<input type="radio" checked name="allEquipment" onclick="onChangeAllEquipments()" value="yes">
									All Equipments <br>
									<input type="radio" name="allEquipment" onclick="onChangeAllEquipments()" value="no">
									Selected Equipments
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
						<th>Export/Import Equipment Details</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportequipmentform" action="EquipmentMaster.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportEquipmentDetailsToXls">
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
										<form name="uploadequipmentform" action="EquipmentMasterUpload.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="method" value="importEquipmentDetailsFromXls">
											<input type="file" name="xlsEquipmentFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="F" onclick="return doUpload('uploadequipmentform')" >
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
											<form name="exportform" action="EquipmentMaster.do" method="GET"
													style="padding:0; margin:0">
												<div style="float: left;">
													<input type="hidden" name="_method" value="exportChargesToXls">
													<input type="hidden" name="org_id" value="">
													<button type="submit" accesskey="D" onclick="return doExport()"><b><u>D</u></b>ownload</button>
												</div>
												<div style="float: left;white-space: normal">
													<img class="imgHelpText"
														 src="${cpath}/images/help.png"
														 title="Note: The export gives a XLS file which can be edited in a spreadsheet like MS Excel. After editing and saving, the file can be imported back, and the new charges will be updated.Note that this must be done for one Rate Sheet at a time.
														 "/>
												</div>
											</form>
										</td>
									</tr>
									<tr>
										<td>Import:</td>
										<td>
											<form name="uploadform" action="EquipmentMasterUpload.do" method="POST"
													enctype="multipart/form-data" style="padding:0; margin:0">
												<input type="hidden" name="method" value="importChargesFromXLS">
												<input type="hidden" name="org_id" value="">
												<input type="file" name="xlsEquipmentFile" id="xlsEquipmentFile" accept="<insta:ltext key="upload.accept.master"/>"/>
												<button type="button" accesskey="U"
													onclick="return validateImportChargesFile(this, 'xlsEquipmentFile', 'org_id');"><b><u>U</u></b>pload</button>
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
				<td colspan="3" style="vertical-align: bottom;">
					<button type="button" accesskey="C" onclick="return doGroupUpdate()">Update <b><u>C</u></b>harges</button>
				</td>
			</tr>
		</table>
</div>
</div>

<script>
	var equipmentNames = ${namesJSON};
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
	var cpath = '${cpath}';
</script>

</body>
</html>

