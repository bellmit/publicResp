<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Theatre/Room Master-Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="dashboardsearch.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />

<insta:link type="js" file="masters/addTheatre.js" />
<c:set value="${pageContext.request.contextPath}" var="cpath" />
<style type="text/css">
  .status_I{background-color: #E4C89C}
</style>
<script type="text/javascript">
function changeRate(){
document.forms[0].submit();
}
</script>
</head>
<body onload="init();" >
<html:form action="/pages/masters/insta/operationtheatre/TheatMast.do?"
  onsubmit="return searchValidate()" method="GET" styleId="operationTheaterForm">
	<html:hidden property="pageNum" />
	<input type="hidden" name="_method" value="getTheatMast"/>
	<input type="hidden" name="_searchMethod" value="getTheatMast"/>
	<c:url var="EC" value="TheatMast.do">
		<c:param name="_method" value="geteditChargeScreen" />
	</c:url>

	<c:set var="theatreList" value="${pagedList.dtoList}" />
	<c:set var="emptyChrgNames" value=""/>

	<c:forEach var="map" items="${theatreList}">
		<c:choose>
			<c:when test="${requestScope.chargeType eq 'DC' || param.chargeType eq 'DC' }">
				<c:forEach var="chargeList" items="${map.dailyCharge}" varStatus="status">
					<c:if test="${status.index > 0}">
						<c:forEach var="charge" items="${chargeList}">
							<c:set var="emptyChrgNames" value="${charge}"/>
						</c:forEach>
					</c:if>
				</c:forEach>
			</c:when>
			<c:when test="${requestScope.chargeType eq 'HC' ||param.chargeType eq 'HC'}">
				<c:forEach var="chargeList" items="${map.hourlyCharge}" varStatus="status">
					<c:if test="${status.index > 0}">
						<c:forEach var="charge" items="${chargeList}">
							<c:set var="emptyChrgNames" value="${charge}"/>
						</c:forEach>
					</c:if>
				</c:forEach>
			</c:when>
			<c:when test="${requestScope.chargeType eq 'IC' ||param.chargeType eq 'IC'}">
				<c:forEach var="chargeList" items="${map.incrCharge}" varStatus="status">
					<c:if test="${status.index > 0}">
						<c:forEach var="charge" items="${chargeList}">
							<c:set var="emptyChrgNames" value="${charge}"/>
						</c:forEach>
					</c:if>
				</c:forEach>
			</c:when>
		</c:choose>
	</c:forEach>
		<c:set var="hasResult"	 value="${not empty emptyChrgNames}"/>
	<div class="pageheader">Theatre/Room Master</div>
	<insta:search form="operationTheaterForm" optionsId="optionalFilter" closed="${hasResult}">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel">View Charges For:</div>
			<div class="sboFieldInput">
				<insta:selectoptions name="chargeType" opvalues="DC,HC,IC"  value="${param.chargeType}"
				optexts="Daily Charge,Min Charge,Incr Charge" onchange="changeRate();"/>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Rate Sheet:</div>
			<div class="sboFieldInput">
				<insta:selectdb name="orgId" value="${TheaMasterForm.orgId}"
				table="organization_details" valuecol="org_id" orderby="org_name"
				displaycol="org_name" onchange="changeRate();"
				filtered="true" filtercol="status,is_rate_sheet" filtervalue="A,Y" />
			</div>
		</div>
	</div>
	<c:if test="${ multiCenters }">
		<div class="sboFieldLabel">Center:</div>
		<div class="sboFieldInput">
			<select class="dropdown" name="centerId" id="centerId">
				<option value="">-- Select --</option>
				<c:forEach items="${centers}" var="center">
					<option value="${center.map.center_id}"
						${param.centerId == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
				</c:forEach>
			</select>
			<input type="hidden" name="centerId@cast" value="y"/>
		</div>
	</c:if>
	<div id="optionalFilter" style="clear: both; display: ${hasResult ? 'none' : 'block'}">
		<table class="searchFormTable">
			<tr>
				<td class="last">
					<div class="sfLabel">Status</div>
					<div class="sfField">
						<insta:checkgroup name="status" selValues="${paramValues.status}"
						optexts="Active ,Inactive" opvalues="A,I"/>
					</div>
				</td>
			</tr>
		</table>
	</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}"
	totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" id="theatreListTable" onmouseover="hideToolBar()">
				<c:forEach var="map" items="${theatreList}">
					<c:set var="rowCount" value="1" />
					<%--daily charge --%>
					<c:set var="i" value="0"/>
					<c:forEach var="chargeList" items="${map.dailyCharge}">
					<%-- color coding for status --%>
						<c:choose>
								<c:when test="${chargeList[0]=='I'}">
										<c:set var="flagColor" value="grey"/>
								</c:when>
								<c:otherwise>
										<c:set var="flagColor" value="empty"/>
								</c:otherwise>
						</c:choose>
					<c:choose>
							<c:when test="${rowCount eq 1}">
								<tr onmouseover="hideToolBar()">
									<th>Select</th>
									<th>Theatre/Room Name</th>
									<th>Store</th>
									<c:if test="${multiCenters}">
										<th>Center</th>
									</c:if>
									<c:forEach var="bed" items="${chargeList}">
										<th class="number">${bed}</th>
									</c:forEach>
								</tr>

								<c:set var="rowCount" value="2" />
								<input type="hidden" name="groupUpdatComponent" value="DC" />
							</c:when>
							<c:otherwise>
							<tr class="${i == 0 ?'firstRow' : ''} ${i %2 ==0 ? 'even' : 'odd'}" id="toolbarRow${i}"
								onclick="showToolbar(${i},event,'theatreListTable', {theatreId :'${chargeList[1]}',orgId :'${TheaMasterForm.orgId}',orgName: '${TheaMasterForm.orgName}', chargeType:'${TheaMasterForm.chargeType}',status:'${chargeList[0]}',store_id: '${chargeList[3]}', center_id: '${multiCenters ? chargeList[4] : '' }'})" onmouseover="hideToolBar(${i})">
									<c:set var="colCount" value="0" />
									<c:forEach var="charge" items="${chargeList}" varStatus="st">
										<c:choose>
											<c:when test="${colCount eq 0}">
												<c:set var="colCount" value="1" />
											</c:when>
											<c:when test="${colCount eq 1}">
												<td><input type="checkbox" name="groupTheatres"
													value="${charge}"></td>
												<c:set var="colCount" value="2" />
												<c:set var="theatreId" value="${charge}" />
											</c:when>
											<c:when test="${colCount eq 2}">
											<td><img src='${cpath}/images/${flagColor}_flag.gif'/>${charge}</td>
												<c:set var="colCount" value="3" />
											</c:when>
											<c:when test="${colCount eq 3}">
												<td><insta:getStoreName store_id="${charge}"/></td>
												<c:set var="colCount" value="4" />
											</c:when>
											<c:when test="${colCount eq 4 && multiCenters}">
											<td><insta:getCenterName center_id="${charge}"/></td>
												<c:set var="colCount" value="5" />
											</c:when>
											<c:otherwise>
												<td class="number">${ifn:afmt(charge)}</td>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</tr>
								<c:set var="i" value="${i+1}"/>
							</c:otherwise>
						</c:choose>
					</c:forEach>
						<%--hourly charge --%>
						<c:set var="i" value="0"/>
						<c:forEach var="chargeList" items="${map.hourlyCharge}">
						<c:choose>
								<c:when test="${chargeList[0]=='I'}">
										<c:set var="flagColor" value="grey"/>
								</c:when>
								<c:otherwise>
										<c:set var="flagColor" value="empty"/>
								</c:otherwise>
						</c:choose>
							<c:choose>
								<c:when test="${rowCount eq 1}">
									<tr onmouseover="hideToolBar()">
										<th>Select</th>
										<th>Theatre Name</th>
										<th>Store</th>
										<c:forEach var="bed" items="${chargeList}">
											<th class="number">${bed}</th>
										</c:forEach>
									</tr>
									<c:set var="rowCount" value="2" />
									<input type="hidden" name="groupUpdatComponent" value="HC" />
								</c:when>
								<c:otherwise>
								<tr  class="${i == 0 ?'firstRow' : ''} ${i %2 ==0 ? 'even' : 'odd'}" id="toolbarRow${i}"
									   onclick="showToolbar(${i},event,'theatreListTable', {theatreId :'${chargeList[1]}',orgId :'${TheaMasterForm.orgId}',orgName: '${TheaMasterForm.orgName}', chargeType:'${TheaMasterForm.chargeType}',status:'${chargeList[0]}', store_id: '${chargeList[3] }'})" onmouseover="hideToolBar(${i})">
										<c:set var="colCount" value="0" />
										<c:forEach var="charge" items="${chargeList}">
											<c:choose>
												<c:when test="${colCount eq 0}">
													<c:set var="colCount" value="1" />
												</c:when>
												<c:when test="${colCount eq 1}">
													<td><input type="checkbox" name="groupTheatres"
														value="${charge}"></td>
													<c:set var="colCount" value="2" />
													<c:set var="theatreId" value="${charge}" />
												</c:when>
												<c:when test="${colCount eq 2}">
												<td><img src="${cpath}/images/${flagColor}_flag.gif"/>${charge}	</td>
													<c:set var="colCount" value="3" />
												</c:when>
												<c:when test="${colCount eq 3}">
												<td><insta:getStoreName store_id="${charge}"/></td>
												<c:set var="colCount" value="4" />
												</c:when>
												<c:otherwise>
													<td class="number">${ifn:afmt(charge)}</td>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</tr>
									<c:set var="i" value="${i+1}"/>
									</c:otherwise>
							</c:choose>
						</c:forEach>
							<%--Incremental  charge --%>

						<c:set var="i" value="0"/>
						<c:forEach var="chargeList" items="${map.incrCharge}">
								<c:choose>
									<c:when test="${chargeList[0]=='I'}">
										<c:set var="flagColor" value="grey"/>
									</c:when>
									<c:otherwise>
										<c:set var="flagColor" value="empty"/>
									</c:otherwise>
								</c:choose>
							<c:choose>
								<c:when test="${rowCount eq 1}">
									<tr onmouseover="hideToolBar()">
										<th>Select</th>
										<th>Theatre Name</th>
										<th>Store</th>
										<c:forEach var="bed" items="${chargeList}">
											<th class="number">${bed}</th>
										</c:forEach>
									</tr>
									<c:set var="rowCount" value="2" />
									<input type="hidden" name="groupUpdatComponent" value="IC" />
								</c:when>
								<c:otherwise>
								<tr  class="${i == 0 ?'firstRow' : ''} ${i % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${i}"
									onclick="showToolbar(${i},event,'theatreListTable', {theatreId :'${chargeList[1]}',orgId :'${TheaMasterForm.orgId}',orgName: '${TheaMasterForm.orgName}', chargeType:'${TheaMasterForm.chargeType}',status:'${chargeList[0]}', store_id: '${chargeList[3] }'})" onmouseover="hideToolBar(${i})">
										<c:set var="colCount" value="0" />
										<c:forEach var="charge" items="${chargeList}">
											<c:choose>
												<c:when test="${colCount eq 0}">
													<c:set var="colCount" value="1" />
												</c:when>
												<c:when test="${colCount eq 1}">
													<td><input type="checkbox" name="groupTheatres"
														value="${charge}"></td>
													<c:set var="colCount" value="2" />
													<c:set var="theatreId" value="${charge}" />
												</c:when>
												<c:when test="${colCount eq 2}">
												<td><img src="${cpath}/images/${flagColor}_flag.gif"/>${charge}</td>
												<td>
													<c:set var="colCount" value="3" />
												</c:when>
												<c:when test="${colCount eq 3}">
													<td><insta:getStoreName store_id="${charge}"/></td>
													<c:set var="colCount" value="4" />
												</c:when>
												<c:otherwise>
													<td class="number">${ifn:afmt(charge)}</td>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</tr>
									<c:set var="i" value="${i+1}"/>
								</c:otherwise>
							</c:choose>
						</c:forEach>
				</c:forEach>
		</table>
		<div class="screenActions" style="float:left">
				<a href="${cpath}/pages/masters/insta/operationtheatre/TheatMast.do?_method=getNewTheatreScreen">
					Add New Theatre/Room</a>
		</div>
		<div style="float:right">
				<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
				<div class="flagText">Inactive Theatre/Room</div>
		</div>
	</div>
	<dl class="accordion">
			<dt><span class="clrboth">Group Update</span></dt>
			<dd>
					<div class="bd">
						<table class="search" width="100%">
							<tr>
								<th>Theatres/Rooms</th>
								<th>Bed type</th>
								<th>
									<input type="radio" name="updateTable" value="UPDATECHARGE">Update Charge <br/>
									<input type="radio" name="updateTable" value="UPDATEDISCOUNT">Update Discount <br/>
									<input type="radio" name="updateTable" value="DISCOUNTCHARGE">Apply Discount On Charge <br/>
								</th>
							</tr>
							<tr>
								<td>
									<input type="radio" name="theatre" id="All" value="all" onchange="selectAll(this)"/>
										Select All Theatres/Rooms
									</br>
									<input type="radio" name="theatre" id="single"  value="single" checked
									onchange="selectAll(this)"/>Select Single Theatre/Room
								</td>
								<td>
									<select multiple="true" size="5" name="groupBeds" value="${paramValues.groupBeds}">
										<c:forEach items="${bedTypes}" var="bed">
											<option value="${bed.BED_TYPE}" onclick="deselectbedtypes();">${bed.BED_TYPE}</option>
										</c:forEach>
									</select></br>
									<input type=checkbox name="allBedTypes" title="To select all bed types."
										checked="false" onclick="selectBedTypes();"/> All</td>
								</td>
								<td>
									<table>
										<tr>
											<td>Charge Type:</td>
											<td>
												 <insta:selectoptions name="incType" opvalues="+,-" optexts="Increase,Decrease"
												    value="+" style="width: 8em"/>
											</td>
										</tr>
										<tr>
											<td>Amount Type:</td>
											<td>
												<insta:selectoptions name="amtType" opvalues="%,A" optexts="Percent,Absolute"
				                      value="%" style="width: 8em"/>
											</td>
										</tr>
										<tr>
											<td>Amount:</td>
										  <td><input type="text" class="validate-number" name="amount"></td>
										</tr>
										<tr>
											<td>Round off: </td>
											<td><insta:selectoptions name="round" opvalues="0,1,5,10,25,50,100"
												optexts="None,1,5,10,25,50,100" value="mount" style="width: 5em"/></td>
										</tr>
									</table>
								</td>
							</tr>
							<tr>
								<td>
									<input type="button" name="save" value="Update Charges"	onclick="ValidateGropUpdate()">
								</td>
							</tr>
						</table>
					</div>
				</dd>
		</dl>
</html:form>
</body>
</html>
