<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="currType"><fmt:message key="currencyType"/> </c:set>
<c:set var="bedlist" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty bedlist}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Bed Charges-Insta HMS</title>
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="masters/bedmaster.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />

<style type="text/css">
  .status_I {background-color: #E4C89C }
</style>
<script>
	pa = new Array();
</script>
</head>
<body onload="selectUpdateOption()">

<html:form action="/pages/masters/insta/admin/newbedmaster.do?"	onsubmit="return doSearch()" method="GET">

	<html:hidden property="pageNum" />
	<html:hidden property="method" value="searchResults" />

	<h1>Available BedTypes</h1>
	<insta:feedback-panel/>
	<insta:search-lessoptions form="newbedmasterform" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Rate Sheet</div>
				<div class="sboFieldInput">
					<insta:selectdb name="orgId" value="${newbedmasterform.orgId}"
						table="organization_details"
						valuecol="org_id" orderby="org_name" displaycol="org_name"
						filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
				</div>
			</div>
			<div class="sboField" style="height:69">
				<div class="sboFieldLabel">View Charges For</div>
				<div class="sboFieldInput">
					<select name="chargeHead" class="dropdown">
					 	<c:if test="${newbedmasterform.chargeHead != null && not empty newbedmasterform.chargeHead }">
	 					 	<c:choose>
						 	 	<c:when test="${newbedmasterform.chargeHead eq 'BEDCHARGE'}">
									<option value="BEDCHARGE">BEDCHARGE</option>
							  	</c:when>
							  	<c:when test="${newbedmasterform.chargeHead eq 'NURSING'}">
									<option value="NURSING">NURSING CHARGE</option>
							 	 </c:when>
							  	<c:when test="${newbedmasterform.chargeHead eq 'INITIAL'}">
							  		<option value="INITIAL">INITIAL PAYMENT</option>
							  	</c:when>
							 	 <c:when test="${newbedmasterform.chargeHead eq 'DUTY'}">
							  		<option value="DUTY">DUTY DOCTOR CHARGE</option>
							 	 </c:when>
							  	<c:when test="${newbedmasterform.chargeHead eq 'HOURLY'}">
							  		<option value="HOURLY">HOURLY CHARGE</option>
							  	</c:when>
							    <c:when test="${newbedmasterform.chargeHead eq 'LUXARY'}">
						  			<option value="LUXARY">LUXURY CHARGE</option>
	    						</c:when>
						  		<c:when test="${newbedmasterform.chargeHead eq 'MAINTAINANCE'}">
									<option value="MAINTAINANCE">PROFESSIONAL CHARGE</option>
	 						    </c:when>
					 	 	</c:choose>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'BEDCHARGE'}">
								<option value="BEDCHARGE">BEDCHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'NURSING'}">
								<option value="NURSING">NURSING CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'INITIAL'}">
								<option value="INITIAL">INITIAL PAYMENT</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'DUTY'}">
								<option value="DUTY">DUTY DOCTOR CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'HOURLY'}">
								<option value="HOURLY">HOURLY CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'LUXARY'}">
								<option value="LUXARY">LUXURY CHARGE</option>
						</c:if>
						<c:if test="${newbedmasterform.chargeHead ne 'MAINTAINANCE'}">
								<option value="MAINTAINANCE">PROFESSIONAL CHARGE</option>
						</c:if>
					</select>
				</div>
			</div>
		</div>
	</insta:search-lessoptions>

	<table class="formtable" width="100%">

		<c:url var="EC" value="newbedmaster.do">
			<c:param name="method" value="getEditChargesScreen" />
		</c:url>

		<tr>
			<td>
			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
			<div class="resultList">
			<table class="resultList" onmouseover="hideToolBar('')");" id="resultTable">
				<tr>
					<th>Select</th>
					<th>Bed Type</th>
					<th>
					  <c:choose>
						  <c:when test="${newbedmasterform.chargeHead eq 'BEDCHARGE'}">
								Bed Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'NURSING'}">
								Nursing Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'INITIAL'}">
						  		Intial Payment
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'DUTY'}">
						  		Duty Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'HOURLY'}">
						  		Hourly Charge
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'LUXARY'}">
						  		Luxury Charge(%)
						  </c:when>
						  <c:when test="${newbedmasterform.chargeHead eq 'MAINTAINANCE'}">
								Professional Charge
						  </c:when>
					  </c:choose>
					</th>
				</tr>

				<c:choose>
					<c:when test="${chargeHead eq 'BEDCHARGE' }">
						<input type="hidden" name="groupUpdatComponent" value="BEDCHARGE" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.BED_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'NURSING'}">
					<input type="hidden" name="groupUpdatComponent" value="NURSING" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.NURSING_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'INITIAL'}">
					  <input type="hidden" name="groupUpdatComponent" value="INITIAL" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.INITIAL_PAYMENT)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'DUTY' }">
					  <input type="hidden" name="groupUpdatComponent" value="DUTY" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.DUTY_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'HOURLY' }">
					  <input type="hidden" name="groupUpdatComponent" value="HOURLY" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.HOURLY_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'LUXARY' }">
					  <input type="hidden" name="groupUpdatComponent" value="LUXARY" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.LUXARY_TAX)}</td>
							</tr>
						</c:forEach>
					</c:when>
					<c:when test="${chargeHead eq 'MAINTAINANCE'}">
					 <input type="hidden" name="groupUpdatComponent" value="MAINTAINANCE" />
						<c:forEach var="record" items="${bedlist}" varStatus="st">
							<c:set var="flagColor">
								<c:if test="${record.BED_STATUS eq 'A'}">empty</c:if>
								<c:if test="${record.BED_STATUS ne 'A'}">grey</c:if>
							</c:set>
							<script>
								pa[${st.index}] = {};
								pa[${st.index}].bed_type = <insta:jsString value="${record.BEDTYPE}"/>;
							</script>
							<tr onclick="showToolbar(${st.index}, event, 'resultTable',
								{bedType: pa[${ifn:encodeUriComponent(st.index)}].bed_type, orgId: '${newbedmasterform.orgId}'},	[true]);"
								onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
								<td><input type="checkbox" name="groupbedType" value="${record.BEDTYPE}"/></td>
								<td><img src="${cpath}/images/${flagColor}_flag.gif" />${record.BEDTYPE}</td>
								<td class="number" style="text-align: left;">${ifn:afmt(record.MAINTAINANCE_CHARGE)}</td>
							</tr>
						</c:forEach>
					</c:when>
				</c:choose>
			</table>
			</div>
			</td>
		</tr>
		<tr>
		<tr>
			<td>
				<br/>
				<div id="content">
					<h1>
						Bed Type Creation Job status
					</h1>
					<div class="resultList" >
						<table class="resultList" id="schedulerTable">
						<tr onmouseover="hideToolBar();">
							<th>ID</th>
							<th>Bed Type</th>
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
		<tr>
			<td>
				<div class="screenActions" style="float: left;">
					<c:url value="newbedmaster.do" var="addnewbed">
						<c:param name="method" value="getNewScreen"></c:param>
						<c:param name="bedType" value="New" />
					</c:url>
					<a href="${addnewbed}&ICU=N">Add Bed Type</a> &nbsp;|
					<a href="${addnewbed}&ICU=Y">Add ICU Bed Type</a>
				</div>
				<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
					<div class="flag"><img src='${pageContext.request.contextPath}/images/grey_flag.gif'></div>
					<div class="flagText"> Inactive</div>
				</div>
			</td>
		</tr>
	</table>
	<div class="screenActions">
		<button type="button" accesskey="U" onclick="ValidateGropUpdate()"><b><u>U</u></b>pdate</button>
	</div>

</html:form>

	<div id="CollapsiblePanel1" class="CollapsiblePanel">
	    	<div class=" title CollapsiblePanelTab" tabindex="0" style=" border-left:none;">
	        	<div class="fltL " style="width: 230px; margin:5px 0 0 10px;">Group Update</div>
				<div class="fltR txtRT" style="width: 25px; margin:10px 0 0 680px; padding-right:0px;"><img src="${cpath}/images/down.png" /></div>
				<div class="clrboth"></div>
			</div>

	<table>
		<tr>
			<td valign="top">
			<form name="updateform" action="${cpath}/pages/masters/insta/admin/newbedmaster.do" method="POST">
			<input type="hidden" name="method" id="method" value="groupUpdate"/>
			<input type="hidden" name="orgId" />
			<input type="hidden" name="groupUpdatComponent" />
			<input type="hidden" name="chargeHead"/>
			<div style="display:none" id="bedTypeListInnerHtml">
				<%-- this holds the hidden inputs for the list of selected bedtypes --%>
			</div>
			<table class="search"  cellspacing="0" cellpadding="0">
				<tr>
					<td style="padding-top: 0px;padding-bottom: 0px">
					   <input type="checkbox" name="All" onchange="selectAll()"/>Select All BedTypes
					 </td>
				</tr>
				<tr>
					<td>
						<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
						<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
						<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
					</td>
				</tr>
				<tr>
					<td><select name="variaceType" style="width:9em" class="dropdown">
						<option value="+">Increase By</option>
						<option value="-">Decrease By</option>
					</select>&nbsp;<input type="text" size="3" class="validate-number" name="varianceBy" onkeypress="return enterNumOnlyANDdot(event)">% or ${currType}
					<input type="text" size="3" name="varianceValue" class="validate-number" onkeypress="return enterNumOnlyANDdot(event)"></td>
				</tr>
			</table>
			</form>
			</td>
			<td valign="top">
				<table class="search" style="padding-left: 3em">
					<tr>
						<th>Export/Import Bed Charges</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportform" action="${cpath}/pages/masters/insta/admin/newbedmaster.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="method" value="exportBedchargesToCsv">
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
										<form name="importChargesform" action="${cpath}/pages/masters/insta/admin/newbedmaster.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="method" value="importBedchargesFromCsv">
											<input type="hidden" name="orgId" value="">
											<input type="file" name="uploadBedChargesFile" id="uploadBedChargesFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="U"
												onclick="return doUpload('importChargesform');"><b><u>U</u></b>pload</button>
										</form>
									</td>
								</tr>

							</table>
						</td>
					</tr>
					<tr>
						<th>Export/Import ICU Bed Charges</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportICUform" action="${cpath}/pages/masters/insta/admin/newbedmaster.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="method" value="exportIcuBedChargesToCsv">
												<input type="hidden" name="orgId" value="">
												<button type="submit" accesskey="D" onclick="return doExportICUCharges()">
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
										<form name="importICUChargesform" action="${cpath}/pages/masters/insta/admin/newbedmaster.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="method" value="importIcuBedChargesFromCsv">
											<input type="hidden" name="orgId" value="">
											<input type="file" name="uploadICUBedChargesFile" id="uploadICUBedChargesFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="U"
												onclick="return doUploadICUCharges('importChargesform');"><b><u>U</u></b>pload</button>
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
				<button type="button" accesskey="U" onclick="ValidateGropUpdate()"><b><u>U</u></b>pdate</button>
			</td>
		</tr>
	</table>
</div>

<script>
	var cpath = '${cpath}';
	var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen: false});
</script>

</body>
</html>
