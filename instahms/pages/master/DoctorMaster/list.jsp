<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Available Doctors-Insta HMS</title>
<insta:link type="js" file="date_go.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="js" file="masters/Adddoctor.js" />
<insta:link type="js" file="masters/charges_common.js" />
	<script>
		var op_prescribe = ${urlRightsMap.op_prescribe == 'A'};
		var doctorCenterapplicable = ${max_centers_inc_default > 1 ? 'true' : 'false'};
	</script>

</head>

<c:set var="cpath">${pageContext.request.contextPath }</c:set>
<c:set var="doctorList" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

<body onload="autoCompleteDoctors();selectAllBedTypes(); init();" class="yui-skin-sam">
	<h1>Available Doctors</h1>
	<insta:feedback-panel/>

	<form action="DoctorMaster.do" method="GET" name="searchform">
		<input type="hidden" name="_method" value="list" />
		<input type="hidden" name="_searchMethod" value="list"/>
		<input type="hidden" name="orgName"/>
		<input type="hidden" name="doctorId" />
		<input type="hidden" name="pageNum"/>

		<c:url var="ED" value="doctor.do">
			<c:param name="method" value="geteditDoctorScreen" />
		</c:url>

		<c:url var="EC" value="doctor.do">
			<c:param name="method" value="geteditChargeScreen" />
		</c:url>
			<insta:search form="searchform" optionsId="optionalFilter" closed="${hasResults}">
				<div class="searchBasicOpts">
					<div class="sboField">
						<div class="sboFieldLabel">Doctor Name</div>
						<div class="sboFieldInput">
							<input type="text" id="doctor_name" name="doctor_name" value="${ifn:cleanHtmlAttribute(param.doctor_name)}"
								style="width:140px;"/>
								<input type="hidden" name="doctor_name@op" value="ilike"/>
								<div id="doctorContainer"></div>
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel">Rate Sheet</div>
						<div class="sboFieldInput">
							<insta:selectdb name="org_id" id="org_id" value="${orgId}"
								table="organization_details" valuecol="org_id"
								orderby="org_name" displaycol="org_name"
								filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y"/>
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel">Payment Category</div>
						<div class="sboFieldInput">
							<insta:selectdb name="payment_category" value="${param.payment_category}" table="category_type_master" valuecol="cat_id"
								displaycol="cat_name" dummyvalue="-- Select --"/>
								<input type="hidden" name="payment_category@type" value="integer">
						</div>
					</div>
					<div class="sboField">
						<div class="sboFieldLabel">View Charges For</div>
						<div class="sboFieldInput">
							<select name="_charge_type" class="dropdown" style="width : 220px">
								<c:forEach var="chrgValue" items="${chargeValues}">
								<option value="${chrgValue}" ${chrgValue == param._charge_type ? 'selected':''}>${chargeMap[chrgValue]}</option>
								</c:forEach>
							</select>
						</div>
					</div>
				</div>

				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Department</div>
								<div class="sfField">
									<insta:selectdb name="dept_id" multiple="true" table="department" valuecol="dept_id" displaycol="dept_name"
											orderby="dept_name" values="${paramValues.dept_id}" />
									<input type="hidden" name="dept_id@op" value="in"/>
								</div>
							</td>
							<td>
								<div class="sfLabel">Status</div>
								<div class="sfField">
									<insta:checkgroup name="status" opvalues="A,I" optexts="Active,Inactive" selValues="${paramValues.status}"/>
										<input type="hidden" name="status@op" value="in" />
								</div>
							</td>
							<c:if test="${max_centers_inc_default > 1 && centerId eq 0}">
								<td class="last">
								<div class="sfLabel">Center</div>
									<div class="sfField">
										<select class="dropdown" name="_center_id" id="center_id">
											<option value="-1" param._center_id eq -1 ? 'selected' : ''> -- Select -- </option>
											<c:forEach items="${centers}" var="center">
												<option value="${center.map.center_id}"
													${param._center_id eq center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
											</c:forEach>
										</select>
										<input type="hidden" name="_center_id@cast" value="y"/>
									</div>
								</td>
							</c:if>
						</tr>
					</table>
				</div>
			</insta:search>
			<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	</form>
	<form name="listform" action="dummy.do">
		<input type="hidden" name="_method" value="groupUpdate"/>
		<input type="hidden" name="org_id" value=""/>
		<c:url var="ED" value="DoctorMaster.do">
			<c:param name="_method" value="geteditDoctorScreen" />
		</c:url>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" >
				<tr onmouseover="hideToolBar();">
					<th style="padding-top: 0px;padding-bottom: 0px;"><input type="checkbox" name="allPageOperations" disabled onclick="selectAllPageDoctors()"/></th>
					<insta:sortablecolumn name="doctor_name" title="Doctor Name"/>
					<insta:sortablecolumn name="dept_name" title="Department"/>
					<c:choose>
						<c:when test="${charge_type eq 'op_charge' || charge_type eq 'op_revisit_charge' ||
							charge_type eq 'private_cons_charge' || charge_type eq 'private_cons_revisit_charge' }">
							<th style="width: 2em;">Consultation Charge</th>
						</c:when>
						<c:otherwise>
							<c:forEach var="bed" items="${bedTypes}">
									<th style="width: 2em; overflow: hidden">${ifn:cleanHtml(fn:substring(bed,0,6))}</th>
								</c:forEach>
						</c:otherwise>
					</c:choose>
					<!--<c:if test="${max_centers_inc_default > 1}">
							<insta:sortablecolumn name="center_name" title="Center Name"/>
					</c:if>
					-->
				</tr>
				<c:forEach var="doctor" items="${pagedList.dtoList}" varStatus="st">

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}"
							onclick="showToolbar(${st.index}, event, 'resultTable',
								{doctor_id: '${doctor.doctor_id}', org_id: '${ifn:cleanJavaScript(orgId)}', mode: 'update'}, '');" >
						<td>
							<input type="checkbox" name="selectDoctor" disabled value="${doctor.doctor_id}">
						</td>
						<td>
							<c:if test="${doctor.status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
							<c:if test="${doctor.status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
							${doctor.doctor_name}
						</td>
						<td>
							${doctor.dept_name}
						</td>
						<c:choose>
							<c:when test="${charge_type eq 'op_charge' || charge_type eq 'op_revisit_charge' || charge_type eq 'private_cons_charge' || charge_type eq 'private_cons_revisit_charge' }">
								<td class="number">${ifn:afmt(charges[doctor.doctor_id][orgId].map[charge_type])}</td>
							</c:when>
							<c:otherwise>
								<c:forEach var="bed" items="${bedTypes}">
									<td class="number">${ifn:afmt(charges[doctor.doctor_id][bed].map[charge_type])}</td>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
				<br/>
			</table>
		</div>
	</form>
	<table class="screen">
			<tr>
				<td width="90%">
	<a href="${cpath}/master/DoctorMasterCharges.do?_method=add&orgId=${ifn:cleanURL(orgId)}">Add New Doctor</a>
	<br/>
	            </td>
	            <td>&nbsp;</td>
				<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
				<td width="10%" align="center">
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Inactive</div>
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
							<form name="updateform" action="DoctorMaster.do" method="POST">
								<input type="hidden" name="_method" value="groupUpdate"/>
								<input type="hidden" name="org_id" value=""/>
								<input type="hidden" name="charge_type" value=""/>

								<div style="display:none" id="doctorListInnerHtml">
									<%-- this holds the hidden inputs for the list of selected equipments --%>
								</div>
								<table title="GROUP UPDATE" class="search" border="0" width="100%">
									<tr>
										<th>Select Doctors</th>
										<th>Select Bed Types</th>
										<th>
											<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
											<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
											<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
										</th>
									</tr>
									<tr>
										<td>
											<input type="radio" checked name="allDoctors" onclick="onChangeAllDoctors()" value="yes">
											All Doctors <br>
											<input type="radio" name="allDoctors" onclick="onChangeAllDoctors()" value="no">
											Selected Doctors
										</td>
										<td style="padding-left: 1em">
											<select multiple="true" size="5" name="selectBedType">
												<c:forEach items="${bedTypes}" var="bed">
													<option value="${bed}"  onclick="selectbedtypes();">${bed}</option>
												</c:forEach>
											</select>

											<br/>
											<input type="checkbox" name="allBedTypes"
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
						<th>Export/Import Doctor Details</th>
					</tr>
					<tr>
						<td>
							<table>
								<tr>
									<td>Export: </td>
									<td>
										<form name="exportdoctorform" action="DoctorMaster.do" method="GET"
												style="padding:0; margin:0">
											<div style="float: left">
												<input type="hidden" name="_method" value="exportDoctorDetailsToXls">
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
										<form name="uploaddoctorform" action="DoctorMasterUpload.do" method="POST"
												enctype="multipart/form-data" style="padding:0; margin:0">
											<input type="hidden" name="method" value="importDoctorDetailsFromXls">
											<input type="file" name="xlsDoctorFile" accept="<insta:ltext key="upload.accept.master"/>"/>
											<button type="button" accesskey="F" onclick="return doUpload('uploaddoctorform')" >
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
													<form name="exportform" action="DoctorMaster.do" method="GET"
															style="padding:0; margin:0">
														<div style="float: left">
															<input type="hidden" name="_method" value="exportChargesToXls">
															<input type="hidden" name="org_id" value="">
															<button type="submit" accesskey="D" onclick="return doExport()"><b><u>D</u></b>ownload</button>
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
													<form name="uploadChargesform" action="DoctorMasterUpload.do" method="POST"
															enctype="multipart/form-data" style="padding:0; margin:0">
														<input type="hidden" name="method" value="importChargesFromXls">
														<input type="hidden" name="org_id" value="">
														<input type="file" name="xlsFile" id="xlsFile" accept="<insta:ltext key="upload.accept.master"/>"/>
														<button type="button" accesskey="U"
															onclick="return validateImportChargesFile(this, 'xlsFile', 'org_id');"><b><u>U</u></b>pload</button>
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
						<td style="vertical-align: bottom;">
							<button type="button" accesskey="C" onclick="return ValidateGropUpdate()">Update <b><u>C</u></b>harges</button>
						</td>
					</tr>
				</table>
			</div>
		</div>



	<script>
		var doctorNames = ${requestScope.doctorNames};
		var CollapsiblePanel1 = new Spry.Widget.CollapsiblePanel("CollapsiblePanel1", {contentIsOpen:false});
		var cpath = '${cpath}';
	</script>

</body>
</html>