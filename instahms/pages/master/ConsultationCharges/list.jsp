<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Consultation Types - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="script" file="masters/consultationCharges.js" />
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
</head>
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>
<body onload="createToolbar(toolbar);">
	<h1>Consultation Types</h1>
	<insta:feedback-panel />
	<form name="ConsultationChargesForm">
		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="_searchMethod" value="list">

		<insta:search form="ConsultationChargesForm" optionsId="optionalFilter" closed="${hasResults}">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel">Consultation Type</div>
					<div class="sboFieldInput">
						<insta:selectdb id="consultation_type_id" name="consultation_type_id" value="${param.consultation_type_id}"
							table="consultation_types" class="dropdown"   dummyvalue="-- Select --"
							valuecol="consultation_type_id"  displaycol="consultation_type"  filtered="false" />
					</div>
				</div>
				<input type="hidden" name="consultation_type_id@type" value="integer" />
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
							<div class="sfLabel">Items</div>
							<div class="sfField" style="width:190px">
								<insta:checkgroup name="applicable" selValues="${paramValues.applicable}"
										opvalues="true,false" optexts="Included Only,Excluded Only"/>
								<input type="hidden" name="applicable@op" value="in"/>
								<input type="hidden" name="applicable@type" value="boolean" />
							</div>
						</td>
						<td >
							<div class="sfLabel">Status</div>
							<div class="sfField">
								<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive"
									selValues="${paramValues.status}"/>
									<input type="hidden" name="status@op" value="in" />
							</div>
						</td>
						<td class="last">
						<div class="sfLabel">Type</div>
						<div class="sfField">
							<insta:checkgroup name="patient_type" opvalues="i,o,ot" optexts="In Patient,Out Patient,OT"
								selValues="${paramValues.patient_type}"/>
								<input type="hidden" name="patient_type@op" value="in" />
						</div></td>
					</tr>
				</table>
			</div>
		</insta:search>
		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
		<table class="resultList"  id="resultTable" cellspacing="0" cellpadding="0" onmouseover="hideToolBar('');">
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px;">
				<input type="checkbox" name="allPageConsultations" disabled onclick="selectAllPageConsultations()"/></th>
				<th>Consultation Type</th>
				<th>Code</th>
				<c:forEach var="bed" items="${bedTypes}">
					<th style="width: 2em; overflow: hidden" class="number">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
				</c:forEach>
			</tr>

			<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{consultation_type_id: '${record.consultation_type_id}',org_id: '${ifn:cleanJavaScript(org_id)}'},'');"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>
						<input type="checkbox" name="selectConsultation" disabled value="${record.consultation_type_id}">
					</td>
					<td>
						<c:if test="${record.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
						<c:if test="${record.status eq 'A' && record.applicable eq true}"><img src='${cpath}/images/empty_flag.gif'></c:if>
						<c:if test="${record.status eq 'A' && record.applicable eq false}"><img src='${cpath}/images/purple_flag.gif'></c:if>
						${record.consultation_type}
					</td>
					<td>${record.consultation_code}</td>
					<c:forEach var="bed" items="${bedTypes}">
						<td class="number" >${ifn:afmt(charges[record.consultation_type_id][bed].map['charge'])}</td>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
		</div>
		<insta:noresults hasResults="${hasResults}"/>

		<c:url var="url" value="consultCharges.do">
			<c:param name="_method" value="add"/>
			<c:param name="org_id" value="ORG0001"></c:param>
		</c:url>
		<table class="screenActions" width="100%">
			<tr>
				<td><a href="<c:out value='${url}'/>">Add Consultation Type</a></td>
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
	</form>

	<div class="resultList">
	<div id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Export/Import</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
			</div>
			<fieldset class="fieldSetBorder">
			<table width="100%">
				<tr>
				<td valign="top">
					<form name="updateform" action="consultCharges.do" method="POST">
						<input type="hidden" name="_method" value="groupUpdate">
						<input type="hidden" name="org_id" value="">
						<input type="hidden" name="chargeType" value="">

						<div style="display:none" id="equipmentListInnerHtml">
							<%-- this holds the hidden inputs for the list of selected equipments --%>
						</div>

						<table class="search">
							<tr>
								<th>Select Consultation Types</th>
								<th>Select Bed Types</th>
								<th>
									<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
									<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
									<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
								</th>
							</tr>

							<tr>
								<td>
									<input type="radio" checked name="allConsultations" onclick="onChangeAllConsultations()" value="yes">
									All Consultation Types <br>
									<input type="radio" name="allConsultations" onclick="onChangeAllConsultations()" value="no">
									Selected Consultaton Type
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
					<th align="left" colspan="2"><u>Export/Import Consultation Details</u></th>
				</tr>
				<tr>
					<td style="padding-top: 10px">Export:</td>
					<td style="padding-top: 10px">
						<form name="exportDetailsForm" action="consultCharges.do" method="GET">
							<input type="hidden" name="_method" value="exportConsultationDetails" />
							<button type="submit">Export</button>
						</form>
					</td>
				</tr>
				<tr>
					<td style="padding-top: 10px;">Import:</td>
					<td>
					<form name="consDetailsUpload" action="consultationUpload.do" enctype="multipart/form-data" method="post">
						<input type="hidden" name="orgId" value="" />
						<input type="hidden" name="_method" value="importConsultationDetails" />
						<input type="file" name="xlsConsultaionDetails" accept="<insta:ltext key="upload.accept.master"/>"/>
						<button type="button" accesskey="U"
							onclick="doUpload('consDetailsUpload')"><b><u>U</u></b>pload</button>
					</form>
					</td>
				</tr>
				<tr>
					<th align="left" colspan="2" style="padding-top: 10px"><u>Export/Import Consultation Charges</u></th>
				</tr>
				<tr>
					<td style="padding-top: 10px;">Export:</td>
					<td style="padding-top: 10px;">
						<form name="DetailsForm" action="consultCharges.do" method="GET">
							<input type="hidden" name="_method" value="exportConsultationCharges" />
							<input type="hidden" name="orgId" value="" />
							<button type="submit" onclick="doExport()">Export</button>
						</form>
					</td>
				</tr>
				<tr>
					<td style="padding-top: 10px;">Import:</td>
					<td>
					<form name="DetailsUpload" action="consultationUpload.do" enctype="multipart/form-data" method="post">
						<input type="hidden" name="_method" value="importConsultationCharges" />
						<input type="hidden" name="org_id" value="" />
						<input type="file" name="xlsConsultaionCharges" id="xlsConsultaionCharges" accept="<insta:ltext key="upload.accept.master"/>" />
						<button type="button" accesskey="U"
							onclick="return validateImportChargesFile(this, 'xlsConsultaionCharges', 'org_id');"><b><u>U</u></b>pload</button>
					</form>
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
			</fieldset>
	</div>
	</div>

	<script>
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
		var cpath = '${cpath}';
	</script>

</body>

</html>
